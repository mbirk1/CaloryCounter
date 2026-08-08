package de.birk.calory.adapter.secondary;

import java.util.Optional;
import java.util.UUID;

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
}
