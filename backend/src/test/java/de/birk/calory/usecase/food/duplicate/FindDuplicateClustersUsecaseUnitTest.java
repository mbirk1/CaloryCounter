package de.birk.calory.usecase.food.duplicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import de.birk.calory.adapter.primary.model.DuplicateClusterDto;
import de.birk.calory.adapter.primary.model.PageResponseDto;
import de.birk.calory.adapter.secondary.DuplicateClusterRepository;
import de.birk.calory.adapter.secondary.model.DuplicateClusterMemberPersistence;
import de.birk.calory.adapter.secondary.model.DuplicateClusterPersistence;
import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.exception.InvalidSortParameterException;

@ExtendWith(MockitoExtension.class)
public class FindDuplicateClustersUsecaseUnitTest {

  @Mock
  private DuplicateClusterRepository clusterRepository;

  private DuplicateClusterPersistence pendingCluster() {
    DuplicateClusterPersistence cluster = new DuplicateClusterPersistence(
        UUID.randomUUID(), "BLS", "PENDING", Instant.now(), null);
    FoodPersistence foodA = FoodPersistence.builder().id(UUID.randomUUID()).name("Apfel")
        .calory(BigDecimal.TEN).grams(BigDecimal.valueOf(100)).build();
    FoodPersistence foodB = FoodPersistence.builder().id(UUID.randomUUID()).name("Apfel rot")
        .calory(BigDecimal.TEN).grams(BigDecimal.valueOf(100)).build();
    cluster.setMembers(List.of(
        new DuplicateClusterMemberPersistence(cluster, foodA, new BigDecimal("0.8"), true),
        new DuplicateClusterMemberPersistence(cluster, foodB, new BigDecimal("0.8"), false)
    ));
    return cluster;
  }

  @Test
  public void findsAPageOfPendingClustersTest() {
    // Arrange
    DuplicateClusterPersistence cluster = pendingCluster();
    when(clusterRepository.findByStatus(eq("PENDING"), any()))
        .thenReturn(new PageImpl<>(List.of(cluster), PageRequest.of(0, 20), 1));
    FindDuplicateClustersUsecase usecase = new FindDuplicateClustersUsecase(clusterRepository);

    // Act
    PageResponseDto<DuplicateClusterDto> result = usecase.findByStatus("PENDING", 0, 20);

    // Assert
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getStatus()).isEqualTo("PENDING");
    assertThat(result.getTotalElements()).isEqualTo(1);
  }

  @Test
  public void rejectsAnInvalidStatusTest() {
    // Arrange
    FindDuplicateClustersUsecase usecase = new FindDuplicateClustersUsecase(clusterRepository);

    // Act & Assert
    assertThatThrownBy(() -> usecase.findByStatus("NOT_A_STATUS", 0, 20))
        .isInstanceOf(InvalidSortParameterException.class);
  }
}
