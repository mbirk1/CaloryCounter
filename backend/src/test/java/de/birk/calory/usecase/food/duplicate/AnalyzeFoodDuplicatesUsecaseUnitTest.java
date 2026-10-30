package de.birk.calory.usecase.food.duplicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.birk.calory.adapter.primary.model.DuplicateAnalysisJobStatusDto;
import de.birk.calory.adapter.secondary.DuplicateClusterRepository;
import de.birk.calory.adapter.secondary.FoodRepository;
import de.birk.calory.adapter.secondary.model.DuplicateClusterPersistence;
import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.adapter.secondary.model.NameSimilarityPair;
import de.birk.calory.domain.food.Food;
import de.birk.calory.domain.food.FoodSource;
import de.birk.calory.domain.food.duplicate.DuplicateClusterMember;
import de.birk.calory.exception.InvalidSortParameterException;

@ExtendWith(MockitoExtension.class)
public class AnalyzeFoodDuplicatesUsecaseUnitTest {

  private static final double THRESHOLD = 0.5;
  private static final BigDecimal TOLERANCE = BigDecimal.TEN;

  @Mock
  private FoodRepository foodRepository;

  @Mock
  private DuplicateClusterRepository clusterRepository;

  @Mock
  private DuplicateAnalysisJobRegistry jobRegistry;

  @Mock
  private DuplicateAnalysisDispatcher dispatcher;

  @Mock
  private DuplicateClusterBuilder clusterBuilder;

  private NameSimilarityPair pair(UUID first, UUID second, String score) {
    NameSimilarityPair pair = mock(NameSimilarityPair.class);
    when(pair.getFirstFoodId()).thenReturn(first);
    when(pair.getSecondFoodId()).thenReturn(second);
    when(pair.getNameSimilarity()).thenReturn(new BigDecimal(score));
    return pair;
  }

  private FoodPersistence food(UUID id, String name) {
    return FoodPersistence.builder().id(id).name(name)
        .calory(BigDecimal.TEN).grams(BigDecimal.valueOf(100)).build();
  }

  @Test
  public void startAnalysisCreatesAJobAndDispatchesItTest() {
    // Arrange
    DuplicateAnalysisJobStatus status =
        new DuplicateAnalysisJobStatus(UUID.randomUUID(), FoodSource.OPENFOODFACTS);
    when(jobRegistry.createJob(FoodSource.OPENFOODFACTS)).thenReturn(status);
    AnalyzeFoodDuplicatesUsecase usecase = new AnalyzeFoodDuplicatesUsecase(
        foodRepository, clusterRepository, jobRegistry, dispatcher, clusterBuilder, THRESHOLD,
        TOLERANCE);

    // Act
    DuplicateAnalysisJobStatusDto result = usecase.startAnalysis("OPENFOODFACTS");

    // Assert
    assertThat(result.getJobId()).isEqualTo(status.getJobId());
    assertThat(result.getSource()).isEqualTo("OPENFOODFACTS");
    verify(dispatcher).dispatch(any(Runnable.class));
  }

  @Test
  public void startAnalysisRejectsAnInvalidSourceTest() {
    // Arrange
    AnalyzeFoodDuplicatesUsecase usecase = new AnalyzeFoodDuplicatesUsecase(
        foodRepository, clusterRepository, jobRegistry, dispatcher, clusterBuilder, THRESHOLD,
        TOLERANCE);

    // Act & Assert
    assertThatThrownBy(() -> usecase.startAnalysis("NOT_A_SOURCE"))
        .isInstanceOf(InvalidSortParameterException.class);
    verify(dispatcher, never()).dispatch(any());
  }

  @Test
  public void runAnalysisPersistsEveryProposedClusterAndCompletesTest() {
    // Arrange
    UUID a = UUID.randomUUID();
    UUID b = UUID.randomUUID();
    NameSimilarityPair candidatePair = pair(a, b, "0.80");
    when(foodRepository.findNameSimilarPairs(THRESHOLD, "BLS"))
        .thenReturn(List.of(candidatePair));
    when(foodRepository.findAllById(anySet()))
        .thenReturn(List.of(food(a, "Apfel"), food(b, "Apfel rot")));
    when(clusterRepository.findBySourceAndStatus("BLS", "REJECTED")).thenReturn(List.of());
    DuplicateClusterMember member = new DuplicateClusterMember(
        new Food("Apfel", BigDecimal.TEN, BigDecimal.valueOf(100)), new BigDecimal("0.80"), true);
    when(clusterBuilder.build(any(), any(), eq(TOLERANCE), eq(Set.of())))
        .thenReturn(List.of(
            new ProposedCluster(List.of(member, member)),
            new ProposedCluster(List.of(member, member))));
    AnalyzeFoodDuplicatesUsecase usecase = new AnalyzeFoodDuplicatesUsecase(
        foodRepository, clusterRepository, jobRegistry, dispatcher, clusterBuilder, THRESHOLD,
        TOLERANCE);
    DuplicateAnalysisJobStatus status = new DuplicateAnalysisJobStatus(UUID.randomUUID(), FoodSource.BLS);

    // Act
    usecase.runAnalysis(FoodSource.BLS, status);

    // Assert
    verify(clusterRepository, times(2)).save(any(DuplicateClusterPersistence.class));
    assertThat(status.getState()).isEqualTo(DuplicateAnalysisJobState.COMPLETED);
    assertThat(status.getClustersFound()).isEqualTo(2);
  }

  @Test
  public void runAnalysisCompletesWithZeroClustersWhenNoCandidatesExistTest() {
    // Arrange
    when(foodRepository.findNameSimilarPairs(THRESHOLD, "MANUAL")).thenReturn(List.of());
    when(foodRepository.findAllById(anySet())).thenReturn(List.of());
    when(clusterRepository.findBySourceAndStatus("MANUAL", "REJECTED")).thenReturn(List.of());
    when(clusterBuilder.build(any(), any(), any(), any())).thenReturn(List.of());
    AnalyzeFoodDuplicatesUsecase usecase = new AnalyzeFoodDuplicatesUsecase(
        foodRepository, clusterRepository, jobRegistry, dispatcher, clusterBuilder, THRESHOLD,
        TOLERANCE);
    DuplicateAnalysisJobStatus status =
        new DuplicateAnalysisJobStatus(UUID.randomUUID(), FoodSource.MANUAL);

    // Act
    usecase.runAnalysis(FoodSource.MANUAL, status);

    // Assert
    verify(clusterRepository, never()).save(any());
    assertThat(status.getState()).isEqualTo(DuplicateAnalysisJobState.COMPLETED);
    assertThat(status.getClustersFound()).isZero();
  }

  @Test
  public void runAnalysisFailsTheJobWhenTheRepositoryThrowsTest() {
    // Arrange
    when(foodRepository.findNameSimilarPairs(THRESHOLD, "OPENFOODFACTS"))
        .thenThrow(new IllegalStateException("db down"));
    AnalyzeFoodDuplicatesUsecase usecase = new AnalyzeFoodDuplicatesUsecase(
        foodRepository, clusterRepository, jobRegistry, dispatcher, clusterBuilder, THRESHOLD,
        TOLERANCE);
    DuplicateAnalysisJobStatus status =
        new DuplicateAnalysisJobStatus(UUID.randomUUID(), FoodSource.OPENFOODFACTS);

    // Act
    usecase.runAnalysis(FoodSource.OPENFOODFACTS, status);

    // Assert
    assertThat(status.getState()).isEqualTo(DuplicateAnalysisJobState.FAILED);
    assertThat(status.getErrorMessage()).isEqualTo("db down");
    verify(clusterRepository, never()).save(any());
  }

  @Test
  public void runAnalysisExcludesFoodIdSetsOfAlreadyRejectedClustersTest() {
    // Arrange - proves the REJECTED clusters found for this source are actually passed through
    // to the builder as the exclusion set.
    UUID rejectedA = UUID.randomUUID();
    UUID rejectedB = UUID.randomUUID();
    DuplicateClusterPersistence rejectedCluster = mock(DuplicateClusterPersistence.class);
    de.birk.calory.adapter.secondary.model.DuplicateClusterMemberPersistence memberA =
        mock(de.birk.calory.adapter.secondary.model.DuplicateClusterMemberPersistence.class);
    de.birk.calory.adapter.secondary.model.DuplicateClusterMemberPersistence memberB =
        mock(de.birk.calory.adapter.secondary.model.DuplicateClusterMemberPersistence.class);
    FoodPersistence foodA = food(rejectedA, "A");
    FoodPersistence foodB = food(rejectedB, "B");
    when(memberA.getFood()).thenReturn(foodA);
    when(memberB.getFood()).thenReturn(foodB);
    when(rejectedCluster.getMembers()).thenReturn(List.of(memberA, memberB));

    when(foodRepository.findNameSimilarPairs(THRESHOLD, "BLS")).thenReturn(List.of());
    when(foodRepository.findAllById(anySet())).thenReturn(List.of());
    when(clusterRepository.findBySourceAndStatus("BLS", "REJECTED"))
        .thenReturn(List.of(rejectedCluster));
    when(clusterBuilder.build(any(), any(), any(), any())).thenReturn(List.of());
    AnalyzeFoodDuplicatesUsecase usecase = new AnalyzeFoodDuplicatesUsecase(
        foodRepository, clusterRepository, jobRegistry, dispatcher, clusterBuilder, THRESHOLD,
        TOLERANCE);
    DuplicateAnalysisJobStatus status = new DuplicateAnalysisJobStatus(UUID.randomUUID(), FoodSource.BLS);

    // Act
    usecase.runAnalysis(FoodSource.BLS, status);

    // Assert
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Set<Set<UUID>>> excludedCaptor = ArgumentCaptor.forClass(Set.class);
    verify(clusterBuilder).build(any(), any(), any(), excludedCaptor.capture());
    assertThat(excludedCaptor.getValue()).containsExactly(Set.of(rejectedA, rejectedB));
  }
}
