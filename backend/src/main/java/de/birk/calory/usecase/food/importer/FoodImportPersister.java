package de.birk.calory.usecase.food.importer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import de.birk.calory.adapter.secondary.FoodRepository;
import de.birk.calory.adapter.secondary.model.FoodPersistence;

/**
 * Batch-persistence, rollback and re-import backfill logic shared by every food import path
 * ({@link FoodCsvImportUsecase}, {@code BlsFoodImportUsecase}), extracted out of the CSV importer
 * since none of it depends on the source file format - only the file parsing and row mapping
 * differ between import paths.
 *
 * @author Marius Birk
 */
@Component
public class FoodImportPersister {

  private static final Logger LOG = LoggerFactory.getLogger(FoodImportPersister.class);

  private final FoodRepository foodRepository;

  public FoodImportPersister(FoodRepository foodRepository) {
    this.foodRepository = foodRepository;
  }

  /**
   * Persists a batch in a single call, relying on Spring Data's default per-repository-method
   * transaction boundary - each {@code saveAll()} call therefore already runs in its own short
   * transaction without needing an explicit {@code @Transactional} here.
   *
   * <p>Re-import safety: rather than pre-checking every row against the unique constraint on
   * {@code external_id} (far too slow at import volume), batches are inserted optimistically. If
   * a batch fails - because the file (or part of it) was already imported before, or because of
   * any other row-level data error (e.g. a value that still slips past a row mapper's own
   * plausibility checks and violates a column constraint) - it is retried record by record so
   * only the genuinely bad rows are skipped, instead of one bad row in a batch taking the rest
   * down with it. The broader {@code DataAccessException} is caught here rather than only its
   * unique-constraint-specific subtype, since Hibernate's PostgreSQL exception translation
   * surfaces different SQLState classes (e.g. a numeric overflow) as other, unrelated
   * {@code DataAccessException} subclasses.
   *
   * @param batch the batch to persist
   * @param status the job status to update
   */
  public void persistBatch(List<FoodPersistence> batch, FoodImportJobStatus status) {
    try {
      this.foodRepository.saveAll(batch);
      status.incrementImported(batch.size());
    } catch (DataAccessException e) {
      LOG.debug(
          "Import-Job {}: Batch mit {} Zeilen konnte nicht als Ganzes gespeichert werden "
              + "({}), Fallback auf zeilenweises Speichern",
          status.getJobId(), batch.size(), e.getMostSpecificCause().getMessage());
      persistOneByOne(batch, status);
    }
  }

  /**
   * Persists rows one at a time, so a single bad row in an otherwise-good batch can be skipped
   * without discarding the rest.
   *
   * <p>Backfill note: a row that fails here because it duplicates an already-imported {@code
   * external_id} additionally has its {@code diet} column updated on the existing row (see
   * {@link FoodRepository#updateDietByExternalId}). This is the deliberately chosen way to
   * backfill the diet field onto rows that were imported before the diet column existed: a plain
   * re-import of the same file already reaches every previously-imported row via this
   * duplicate-handling path, so no separate backfill script or endpoint is needed - re-running
   * the import is enough. Only the diet column is touched, never a full-row upsert, so an
   * interrupted or partial re-import can't silently overwrite other, already-correct columns.
   *
   * @param batch the batch to persist record by record
   * @param status the job status to update
   */
  private void persistOneByOne(List<FoodPersistence> batch, FoodImportJobStatus status) {
    for (FoodPersistence food : batch) {
      try {
        this.foodRepository.save(food);
        status.incrementImported(1);
      } catch (DataAccessException e) {
        if (food.getExternalId() != null) {
          LOG.debug(
              "Import-Job {}: Duplikat fuer externalId '{}' erkannt, Diaet-Backfill auf "
                  + "'{}' wird angewendet",
              status.getJobId(), food.getExternalId(), food.getDiet());
          this.foodRepository.updateDietByExternalId(food.getExternalId(), food.getDiet());
        }
        status.incrementSkipped(1);
      }
    }
  }

  /**
   * Rolls back everything the job has already imported and marks it {@code FAILED} - {@code
   * status.fail()} runs in a {@code finally} block so the job is guaranteed to leave {@code
   * RUNNING} even if the rollback delete itself throws (e.g. a lost DB connection); a stray
   * orphaned row is a far smaller problem than a job hanging in {@code RUNNING} forever, which
   * would leave the client polling indefinitely with no way to ever learn the import failed.
   *
   * @param status the job status to roll back and mark as failed
   */
  public void rollbackAndFail(FoodImportJobStatus status) {
    try {
      this.foodRepository.deleteByImportJobId(status.getJobId());
    } catch (RuntimeException e) {
      LOG.error(
          "Import-Job {} Rollback fehlgeschlagen, moeglicherweise verbleiben "
              + "bereits importierte Zeilen in der Datenbank",
          status.getJobId(), e);
    } finally {
      status.fail();
    }
  }
}
