package de.birk.calory.usecase.food.duplicate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.birk.calory.adapter.primary.model.DuplicateAnalysisJobStatusDto;
import de.birk.calory.adapter.secondary.DuplicateClusterRepository;
import de.birk.calory.adapter.secondary.FoodRepository;
import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.adapter.secondary.model.NameSimilarityPair;
import de.birk.calory.domain.food.Food;
import de.birk.calory.domain.food.FoodSource;
import de.birk.calory.domain.food.duplicate.ClusterStatus;
import de.birk.calory.domain.food.duplicate.DuplicateCluster;
import de.birk.calory.exception.InvalidSortParameterException;
import de.birk.calory.usecase.food.converter.FoodPersistenceConverter;

/**
 * Usecase that runs the FR-2 duplicate-candidate analysis for a single {@link FoodSource} as an
 * asynchronous job (FR-3) and persists every resulting cluster as {@code PENDING}, ready for
 * human review (FR-4).
 *
 * @author Marius Birk
 */
@Component
public class AnalyzeFoodDuplicatesUsecase {

  private static final Logger LOG = LoggerFactory.getLogger(AnalyzeFoodDuplicatesUsecase.class);

  private final FoodRepository foodRepository;
  private final DuplicateClusterRepository clusterRepository;
  private final DuplicateAnalysisJobRegistry jobRegistry;
  private final DuplicateAnalysisDispatcher dispatcher;
  private final DuplicateClusterBuilder clusterBuilder;
  private final DuplicateAnalysisJobStatusDtoConverter statusDtoConverter;
  private final FoodPersistenceConverter foodPersistenceConverter;
  private final DuplicateClusterPersistenceConverter clusterPersistenceConverter;
  private final double nameSimilarityThreshold;
  private final BigDecimal macroTolerancePercent;

  /**
   * Basic constructor that takes all collaborators needed to run a duplicate analysis, with the
   * two FR-2 thresholds read from configuration.
   *
   * @param foodRepository the food repository, used both for the candidate-pair search and to
   *     load the full food items referenced by it
   * @param clusterRepository persists proposed clusters and looks up already-rejected ones
   * @param jobRegistry tracks the progress of running and finished analysis jobs
   * @param dispatcher triggers the actual analysis asynchronously
   * @param nameSimilarityThreshold the minimum trigram name-similarity for a candidate pair
   * @param macroTolerancePercent the maximum allowed relative deviation between two present
   *     macro/calory values, as a percentage, e.g. {@code 10} for 10%
   */
  @Autowired
  public AnalyzeFoodDuplicatesUsecase(
      FoodRepository foodRepository,
      DuplicateClusterRepository clusterRepository,
      DuplicateAnalysisJobRegistry jobRegistry,
      DuplicateAnalysisDispatcher dispatcher,
      @Value("${food.duplicate-detection.name-similarity-threshold}")
      double nameSimilarityThreshold,
      @Value("${food.duplicate-detection.macro-tolerance-percent}")
      double macroTolerancePercent) {
    this(
        foodRepository, clusterRepository, jobRegistry, dispatcher, new DuplicateClusterBuilder(),
        nameSimilarityThreshold, BigDecimal.valueOf(macroTolerancePercent));
  }

  /**
   * Package-private constructor that additionally allows tests to inject a (possibly mocked)
   * cluster builder and explicit threshold values, bypassing property injection.
   *
   * @param foodRepository the food repository, used both for the candidate-pair search and to
   *     load the full food items referenced by it
   * @param clusterRepository persists proposed clusters and looks up already-rejected ones
   * @param jobRegistry tracks the progress of running and finished analysis jobs
   * @param dispatcher triggers the actual analysis asynchronously
   * @param clusterBuilder groups candidate pairs into clusters
   * @param nameSimilarityThreshold the minimum trigram name-similarity for a candidate pair
   * @param macroTolerancePercent the maximum allowed relative deviation between two present
   *     macro/calory values, as a percentage
   */
  AnalyzeFoodDuplicatesUsecase(
      FoodRepository foodRepository,
      DuplicateClusterRepository clusterRepository,
      DuplicateAnalysisJobRegistry jobRegistry,
      DuplicateAnalysisDispatcher dispatcher,
      DuplicateClusterBuilder clusterBuilder,
      double nameSimilarityThreshold,
      BigDecimal macroTolerancePercent) {
    this.foodRepository = foodRepository;
    this.clusterRepository = clusterRepository;
    this.jobRegistry = jobRegistry;
    this.dispatcher = dispatcher;
    this.clusterBuilder = clusterBuilder;
    this.statusDtoConverter = new DuplicateAnalysisJobStatusDtoConverter();
    this.foodPersistenceConverter = new FoodPersistenceConverter();
    this.clusterPersistenceConverter = new DuplicateClusterPersistenceConverter();
    this.nameSimilarityThreshold = nameSimilarityThreshold;
    this.macroTolerancePercent = macroTolerancePercent;
  }

  /**
   * Starts an asynchronous analysis of a single source, immediately returning a job status that
   * can be polled for progress.
   *
   * <p>Parses {@code source} here (rather than the caller doing it and passing a
   * {@link FoodSource}) so the primary adapter never has to depend on a domain type directly -
   * only this usecase layer is allowed to.
   *
   * @param source the name of the {@code FoodSource} enum constant to analyze
   * @return the freshly created, still running job status
   * @throws InvalidSortParameterException if {@code source} is not a valid {@code FoodSource}
   */
  public DuplicateAnalysisJobStatusDto startAnalysis(String source) {
    FoodSource resolvedSource = parseSource(source);
    DuplicateAnalysisJobStatus status = this.jobRegistry.createJob(resolvedSource);
    LOG.info(
        "Duplikat-Analyse-Job {} gestartet fuer Quelle {}", status.getJobId(), resolvedSource);
    this.dispatcher.dispatch(() -> runAnalysis(resolvedSource, status));
    return this.statusDtoConverter.toDto(status);
  }

  private FoodSource parseSource(String source) {
    try {
      return FoodSource.valueOf(source);
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new InvalidSortParameterException(
          "Invalid source '" + source + "', allowed values are " + List.of(FoodSource.values()));
    }
  }

  /**
   * Performs the actual analysis. Package-private: only called by
   * {@link DuplicateAnalysisDispatcher} from its {@code @Async} method, or directly from tests.
   *
   * @param source the food source to analyze
   * @param status the job status to update while analyzing
   */
  void runAnalysis(FoodSource source, DuplicateAnalysisJobStatus status) {
    try {
      List<NameSimilarityPair> pairs =
          this.foodRepository.findNameSimilarPairs(this.nameSimilarityThreshold, source.name());

      Set<UUID> candidateIds = new HashSet<>();
      List<NameSimilarityCandidate> candidates = new ArrayList<>(pairs.size());
      for (NameSimilarityPair pair : pairs) {
        candidateIds.add(pair.getFirstFoodId());
        candidateIds.add(pair.getSecondFoodId());
        candidates.add(new NameSimilarityCandidate(
            pair.getFirstFoodId(), pair.getSecondFoodId(), pair.getNameSimilarity()));
      }

      Map<UUID, Food> foodsById = this.foodRepository.findAllById(candidateIds).stream()
          .collect(Collectors.toMap(
              FoodPersistence::getId, this.foodPersistenceConverter::convertFromDto));

      Set<Set<UUID>> alreadyRejected = this.clusterRepository
          .findBySourceAndStatus(source.name(), ClusterStatus.REJECTED.name())
          .stream()
          .map(cluster -> cluster.getMembers().stream()
              .map(member -> member.getFood().getId())
              .collect(Collectors.toSet()))
          .collect(Collectors.toSet());

      List<ProposedCluster> proposed = this.clusterBuilder.build(
          candidates, foodsById, this.macroTolerancePercent, alreadyRejected);

      for (ProposedCluster cluster : proposed) {
        DuplicateCluster domainCluster = new DuplicateCluster(
            UUID.randomUUID(), source, ClusterStatus.PENDING, cluster.members(), Instant.now(),
            null);
        this.clusterRepository.save(
            this.clusterPersistenceConverter.convertFromEntity(domainCluster));
      }

      status.setClustersFound(proposed.size());
      status.complete();
      LOG.info(
          "Duplikat-Analyse-Job {} abgeschlossen: {} Cluster vorgeschlagen fuer Quelle {}",
          status.getJobId(), proposed.size(), source);
    } catch (RuntimeException e) {
      LOG.error("Duplikat-Analyse-Job {} fehlgeschlagen", status.getJobId(), e);
      status.fail(e.getMessage());
    }
  }
}
