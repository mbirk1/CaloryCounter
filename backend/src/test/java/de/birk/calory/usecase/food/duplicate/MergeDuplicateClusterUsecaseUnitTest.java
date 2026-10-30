package de.birk.calory.usecase.food.duplicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.birk.calory.adapter.primary.model.DuplicateClusterDto;
import de.birk.calory.adapter.secondary.DuplicateClusterMemberRepository;
import de.birk.calory.adapter.secondary.DuplicateClusterRepository;
import de.birk.calory.adapter.secondary.FoodRepository;
import de.birk.calory.adapter.secondary.RecipeFoodEntryRepository;
import de.birk.calory.adapter.secondary.model.DuplicateClusterMemberPersistence;
import de.birk.calory.adapter.secondary.model.DuplicateClusterPersistence;
import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.exception.ValidationException;

@ExtendWith(MockitoExtension.class)
public class MergeDuplicateClusterUsecaseUnitTest {

  @Mock
  private DuplicateClusterRepository clusterRepository;

  @Mock
  private DuplicateClusterMemberRepository memberRepository;

  @Mock
  private RecipeFoodEntryRepository recipeFoodEntryRepository;

  @Mock
  private FoodRepository foodRepository;

  private FoodPersistence food(UUID id, String name) {
    return FoodPersistence.builder().id(id).name(name)
        .calory(BigDecimal.TEN).grams(BigDecimal.valueOf(100)).build();
  }

  private DuplicateClusterPersistence clusterWith(
      UUID clusterId, String status, UUID canonicalId, UUID nonCanonicalId) {
    DuplicateClusterPersistence cluster =
        new DuplicateClusterPersistence(clusterId, "BLS", status, Instant.now(), null);
    cluster.setMembers(List.of(
        new DuplicateClusterMemberPersistence(
            cluster, food(canonicalId, "Apfel"), new BigDecimal("0.8"), true),
        new DuplicateClusterMemberPersistence(
            cluster, food(nonCanonicalId, "Apfel rot"), new BigDecimal("0.8"), false)
    ));
    return cluster;
  }

  @Test
  public void mergesTheNonCanonicalMemberWhenThereIsNoConflictTest() {
    // Arrange
    UUID clusterId = UUID.randomUUID();
    UUID canonicalId = UUID.randomUUID();
    UUID nonCanonicalId = UUID.randomUUID();
    DuplicateClusterPersistence initial = clusterWith(clusterId, "PENDING", canonicalId, nonCanonicalId);
    DuplicateClusterPersistence resolved =
        new DuplicateClusterPersistence(clusterId, "BLS", "CONFIRMED", Instant.now(), Instant.now());
    resolved.setMembers(List.of(new DuplicateClusterMemberPersistence(
        resolved, food(canonicalId, "Apfel"), new BigDecimal("0.8"), true)));
    when(clusterRepository.findById(clusterId)).thenReturn(Optional.of(initial), Optional.of(resolved));
    when(recipeFoodEntryRepository.findRecipeIdsByFoodId(nonCanonicalId)).thenReturn(List.of());
    MergeDuplicateClusterUsecase usecase = new MergeDuplicateClusterUsecase(
        clusterRepository, memberRepository, recipeFoodEntryRepository, foodRepository);

    // Act
    DuplicateClusterDto result = usecase.merge(clusterId, canonicalId);

    // Assert
    verify(recipeFoodEntryRepository).repointNonConflicting(nonCanonicalId, canonicalId);
    verify(recipeFoodEntryRepository, never()).deleteByFoodId(nonCanonicalId);
    verify(memberRepository).deleteByFoodId(nonCanonicalId);
    verify(foodRepository).deleteById(nonCanonicalId);
    verify(clusterRepository).updateStatus(eq(clusterId), eq("CONFIRMED"), any(Instant.class));
    assertThat(result.getStatus()).isEqualTo("CONFIRMED");
    assertThat(result.getMembers()).hasSize(1);
  }

  @Test
  public void discardsTheConflictingRecipeRowWhenBothMembersAreAlreadyInTheSameRecipeTest() {
    // Arrange - FR-5.2: a recipe already references both the canonical and non-canonical food.
    UUID clusterId = UUID.randomUUID();
    UUID canonicalId = UUID.randomUUID();
    UUID nonCanonicalId = UUID.randomUUID();
    UUID conflictingRecipeId = UUID.randomUUID();
    DuplicateClusterPersistence initial = clusterWith(clusterId, "PENDING", canonicalId, nonCanonicalId);
    DuplicateClusterPersistence resolved =
        new DuplicateClusterPersistence(clusterId, "BLS", "CONFIRMED", Instant.now(), Instant.now());
    resolved.setMembers(List.of(new DuplicateClusterMemberPersistence(
        resolved, food(canonicalId, "Apfel"), new BigDecimal("0.8"), true)));
    when(clusterRepository.findById(clusterId)).thenReturn(Optional.of(initial), Optional.of(resolved));
    when(recipeFoodEntryRepository.findRecipeIdsByFoodId(nonCanonicalId))
        .thenReturn(List.of(conflictingRecipeId));
    MergeDuplicateClusterUsecase usecase = new MergeDuplicateClusterUsecase(
        clusterRepository, memberRepository, recipeFoodEntryRepository, foodRepository);

    // Act
    usecase.merge(clusterId, canonicalId);

    // Assert
    verify(recipeFoodEntryRepository).repointNonConflicting(nonCanonicalId, canonicalId);
    verify(recipeFoodEntryRepository).deleteByFoodId(nonCanonicalId);
    verify(foodRepository).deleteById(nonCanonicalId);
  }

  @Test
  public void mergesEveryNonCanonicalMemberInAClusterOfThreeTest() {
    // Arrange
    UUID clusterId = UUID.randomUUID();
    UUID canonicalId = UUID.randomUUID();
    UUID nonCanonicalOne = UUID.randomUUID();
    UUID nonCanonicalTwo = UUID.randomUUID();
    DuplicateClusterPersistence initial =
        new DuplicateClusterPersistence(clusterId, "BLS", "PENDING", Instant.now(), null);
    initial.setMembers(List.of(
        new DuplicateClusterMemberPersistence(
            initial, food(canonicalId, "Apfel"), new BigDecimal("0.8"), true),
        new DuplicateClusterMemberPersistence(
            initial, food(nonCanonicalOne, "Apfel rot"), new BigDecimal("0.8"), false),
        new DuplicateClusterMemberPersistence(
            initial, food(nonCanonicalTwo, "Apfel gruen"), new BigDecimal("0.8"), false)
    ));
    DuplicateClusterPersistence resolved =
        new DuplicateClusterPersistence(clusterId, "BLS", "CONFIRMED", Instant.now(), Instant.now());
    resolved.setMembers(List.of(new DuplicateClusterMemberPersistence(
        resolved, food(canonicalId, "Apfel"), new BigDecimal("0.8"), true)));
    when(clusterRepository.findById(clusterId)).thenReturn(Optional.of(initial), Optional.of(resolved));
    when(recipeFoodEntryRepository.findRecipeIdsByFoodId(any())).thenReturn(List.of());
    MergeDuplicateClusterUsecase usecase = new MergeDuplicateClusterUsecase(
        clusterRepository, memberRepository, recipeFoodEntryRepository, foodRepository);

    // Act
    usecase.merge(clusterId, canonicalId);

    // Assert
    verify(foodRepository, times(2)).deleteById(any());
    verify(foodRepository).deleteById(nonCanonicalOne);
    verify(foodRepository).deleteById(nonCanonicalTwo);
    verify(foodRepository, never()).deleteById(canonicalId);
    // Each deletion must be flushed immediately: a later member's clearAutomatically bulk
    // queries (repointNonConflicting/deleteByFoodId) would otherwise discard an earlier
    // member's still-pending, not-yet-flushed removal before it ever reaches the database -
    // a real bug this exact regression test was added for after catching it in manual testing.
    verify(foodRepository, times(2)).flush();
  }

  @Test
  public void throwsWhenTheClusterDoesNotExistTest() {
    // Arrange
    UUID clusterId = UUID.randomUUID();
    when(clusterRepository.findById(clusterId)).thenReturn(Optional.empty());
    MergeDuplicateClusterUsecase usecase = new MergeDuplicateClusterUsecase(
        clusterRepository, memberRepository, recipeFoodEntryRepository, foodRepository);

    // Act & Assert
    assertThatThrownBy(() -> usecase.merge(clusterId, UUID.randomUUID()))
        .isInstanceOf(NoSuchElementException.class);
    verify(foodRepository, never()).deleteById(any());
  }

  @Test
  public void rejectsAnAlreadyResolvedClusterTest() {
    // Arrange
    UUID clusterId = UUID.randomUUID();
    UUID canonicalId = UUID.randomUUID();
    UUID nonCanonicalId = UUID.randomUUID();
    DuplicateClusterPersistence resolved =
        clusterWith(clusterId, "CONFIRMED", canonicalId, nonCanonicalId);
    when(clusterRepository.findById(clusterId)).thenReturn(Optional.of(resolved));
    MergeDuplicateClusterUsecase usecase = new MergeDuplicateClusterUsecase(
        clusterRepository, memberRepository, recipeFoodEntryRepository, foodRepository);

    // Act & Assert
    assertThatThrownBy(() -> usecase.merge(clusterId, canonicalId))
        .isInstanceOf(ValidationException.class);
    verify(foodRepository, never()).deleteById(any());
  }

  @Test
  public void rejectsACanonicalFoodIdThatIsNotAClusterMemberTest() {
    // Arrange
    UUID clusterId = UUID.randomUUID();
    UUID canonicalId = UUID.randomUUID();
    UUID nonCanonicalId = UUID.randomUUID();
    UUID foreignId = UUID.randomUUID();
    DuplicateClusterPersistence initial = clusterWith(clusterId, "PENDING", canonicalId, nonCanonicalId);
    when(clusterRepository.findById(clusterId)).thenReturn(Optional.of(initial));
    MergeDuplicateClusterUsecase usecase = new MergeDuplicateClusterUsecase(
        clusterRepository, memberRepository, recipeFoodEntryRepository, foodRepository);

    // Act & Assert
    assertThatThrownBy(() -> usecase.merge(clusterId, foreignId))
        .isInstanceOf(ValidationException.class);
    verify(foodRepository, never()).deleteById(any());
  }
}
