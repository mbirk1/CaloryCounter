package de.birk.calory.adapter.secondary;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.birk.calory.adapter.secondary.model.FoodPersistence;

/**
 * Basic Spring Boot Repository.
 *
 * @author Marius Birk
 */
@Repository
public interface FoodRepository extends JpaRepository<FoodPersistence, UUID> {

  Optional<FoodPersistence> findByExternalId(String externalId);

  /**
   * Bulk-deletes every food item imported by a specific CSV import job, used to roll back a job
   * that failed partway through. A hand-written {@code @Modifying} query is used instead of a
   * derived {@code deleteByImportJobId(...)}, which Spring Data would implement by first loading
   * every matching row into memory one by one before deleting each individually - far too slow
   * for a job that may have already imported millions of rows.
   *
   * <p>Called from the CSV import's {@code @Async} background thread, which has no ambient
   * transaction of its own - {@code @Modifying} queries need an explicit transaction to run in,
   * or Hibernate rejects them with a {@code TransactionRequiredException}.
   *
   * @param jobId the import job whose rows should be removed
   */
  @Transactional
  @Modifying(clearAutomatically = true)
  @Query("delete from FoodPersistence f where f.importJobId = :jobId")
  void deleteByImportJobId(@Param("jobId") UUID jobId);

  /**
   * Updates only the {@code diet} column of an already-imported row, identified by its
   * {@code externalId}. Used to backfill the diet field on a re-import of a file that was
   * originally imported before the diet column existed: a plain re-import already skips
   * duplicate rows (matched by the unique {@code external_id} constraint) without touching them,
   * so without this targeted update a re-import alone would never enrich existing rows with a
   * diet value. This intentionally only ever touches the single {@code diet} column, rather than
   * a full upsert of every column, to avoid a partially re-run or interrupted import silently
   * overwriting other, already-correct columns with stale data from the re-imported file.
   *
   * @param externalId the external id identifying the existing row
   * @param diet the name of the {@code Diet} enum constant to set
   */
  @Transactional
  @Modifying(clearAutomatically = true)
  @Query("update FoodPersistence f set f.diet = :diet where f.externalId = :externalId")
  void updateDietByExternalId(@Param("externalId") String externalId, @Param("diet") String diet);

  /**
   * Finds a page of food items whose name contains {@code search} (case-insensitive) and whose
   * diet matches {@code diet}, both optional - a {@code null} value for either skips that
   * condition entirely rather than matching nothing.
   *
   * <p>{@code :search} is explicitly cast to {@code string} before being passed to {@code
   * lower(...)}: PostgreSQL's JDBC driver can't infer a parameter's type purely from a {@code ?
   * is null} check, and defaults an ambiguous, always-null parameter to {@code bytea} - without
   * the cast, every request with no search term (i.e. every default page load) fails at the
   * database with {@code function lower(bytea) does not exist}.
   *
   * @param search a case-insensitive substring to match against the name, or {@code null} to
   *     not filter by name
   * @param diet the name of the {@code Diet} enum constant to filter by, or {@code null} to not
   *     filter by diet
   * @param pageable the pagination and (server-validated) sort parameters
   * @return the matching page of food items
   */
  @Query("select f from FoodPersistence f where "
      + "(:search is null or lower(f.name) like lower(concat('%', CAST(:search as string), '%'))) "
      + "and (:diet is null or f.diet = :diet)")
  Page<FoodPersistence> search(
      @Param("search") String search, @Param("diet") String diet, Pageable pageable);
}
