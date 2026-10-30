package de.birk.calory.usecase.food.duplicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import de.birk.calory.adapter.secondary.DuplicateClusterRepository;
import de.birk.calory.adapter.secondary.model.DuplicateClusterMemberPersistence;
import de.birk.calory.adapter.secondary.model.DuplicateClusterPersistence;
import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.exception.ValidationException;

@ExtendWith(MockitoExtension.class)
public class RejectDuplicateClusterUsecaseUnitTest {

  @Mock
  private DuplicateClusterRepository clusterRepository;

  private DuplicateClusterPersistence clusterWithStatus(UUID clusterId, String status) {
    DuplicateClusterPersistence cluster =
        new DuplicateClusterPersistence(clusterId, "BLS", status, Instant.now(), null);
    FoodPersistence foodA = FoodPersistence.builder().id(UUID.randomUUID()).name("Apfel")
        .calory(java.math.BigDecimal.TEN).grams(java.math.BigDecimal.valueOf(100)).build();
    FoodPersistence foodB = FoodPersistence.builder().id(UUID.randomUUID()).name("Apfel rot")
        .calory(java.math.BigDecimal.TEN).grams(java.math.BigDecimal.valueOf(100)).build();
    cluster.setMembers(List.of(
        new DuplicateClusterMemberPersistence(
            cluster, foodA, new java.math.BigDecimal("0.8"), true),
        new DuplicateClusterMemberPersistence(
            cluster, foodB, new java.math.BigDecimal("0.8"), false)
    ));
    return cluster;
  }

  @Test
  public void rejectsAPendingClusterTest() {
    // Arrange
    UUID clusterId = UUID.randomUUID();
    DuplicateClusterPersistence initial = clusterWithStatus(clusterId, "PENDING");
    DuplicateClusterPersistence resolved = clusterWithStatus(clusterId, "REJECTED");
    when(clusterRepository.findById(clusterId))
        .thenReturn(Optional.of(initial), Optional.of(resolved));
    RejectDuplicateClusterUsecase usecase = new RejectDuplicateClusterUsecase(clusterRepository);

    // Act
    DuplicateClusterDto result = usecase.reject(clusterId);

    // Assert
    verify(clusterRepository).updateStatus(eq(clusterId), eq("REJECTED"), any(Instant.class));
    assertThat(result.getStatus()).isEqualTo("REJECTED");
  }

  @Test
  public void throwsWhenTheClusterDoesNotExistTest() {
    // Arrange
    UUID clusterId = UUID.randomUUID();
    when(clusterRepository.findById(clusterId)).thenReturn(Optional.empty());
    RejectDuplicateClusterUsecase usecase = new RejectDuplicateClusterUsecase(clusterRepository);

    // Act & Assert
    assertThatThrownBy(() -> usecase.reject(clusterId))
        .isInstanceOf(NoSuchElementException.class);
    verify(clusterRepository, never()).updateStatus(any(), any(), any());
  }

  @Test
  public void rejectsAnAlreadyResolvedClusterTest() {
    // Arrange
    UUID clusterId = UUID.randomUUID();
    DuplicateClusterPersistence resolved = clusterWithStatus(clusterId, "CONFIRMED");
    when(clusterRepository.findById(clusterId)).thenReturn(Optional.of(resolved));
    RejectDuplicateClusterUsecase usecase = new RejectDuplicateClusterUsecase(clusterRepository);

    // Act & Assert
    assertThatThrownBy(() -> usecase.reject(clusterId)).isInstanceOf(ValidationException.class);
    verify(clusterRepository, never()).updateStatus(any(), any(), any());
  }
}
