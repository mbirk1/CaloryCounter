package de.birk.calory.adapter.primary;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.birk.calory.adapter.primary.model.DuplicateAnalysisJobStatusDto;
import de.birk.calory.adapter.primary.model.DuplicateClusterDto;
import de.birk.calory.adapter.primary.model.MergeClusterRequestDto;
import de.birk.calory.adapter.primary.model.PageResponseDto;
import de.birk.calory.exception.InvalidSortParameterException;
import de.birk.calory.usecase.food.duplicate.AnalyzeFoodDuplicatesUsecase;
import de.birk.calory.usecase.food.duplicate.FindDuplicateAnalysisJobStatusUsecase;
import de.birk.calory.usecase.food.duplicate.FindDuplicateClustersUsecase;
import de.birk.calory.usecase.food.duplicate.MergeDuplicateClusterUsecase;
import de.birk.calory.usecase.food.duplicate.RejectDuplicateClusterUsecase;

/**
 * RestController for the "Datenqualität" duplicate-detection review workflow (FR-2 through
 * FR-6): starting a source's analysis, polling it, listing proposed clusters, and confirming
 * (merging) or rejecting them.
 *
 * @author Marius Birk
 */
@RestController
@RequestMapping("/api/food/duplicates")
//TODO Marius Should be outsourced to env variable
@CrossOrigin(origins = "http://localhost:4200")
public class FoodDuplicateRestController {

  private final AnalyzeFoodDuplicatesUsecase analyzeFoodDuplicatesUsecase;
  private final FindDuplicateAnalysisJobStatusUsecase findDuplicateAnalysisJobStatusUsecase;
  private final FindDuplicateClustersUsecase findDuplicateClustersUsecase;
  private final MergeDuplicateClusterUsecase mergeDuplicateClusterUsecase;
  private final RejectDuplicateClusterUsecase rejectDuplicateClusterUsecase;

  /**
   * Every necessary usecase needed to complete the incoming requests.
   *
   * @param analyzeFoodDuplicatesUsecase Analyze Food Duplicates Usecase
   * @param findDuplicateAnalysisJobStatusUsecase Find Duplicate Analysis Job Status Usecase
   * @param findDuplicateClustersUsecase Find Duplicate Clusters Usecase
   * @param mergeDuplicateClusterUsecase Merge Duplicate Cluster Usecase
   * @param rejectDuplicateClusterUsecase Reject Duplicate Cluster Usecase
   */
  public FoodDuplicateRestController(
      AnalyzeFoodDuplicatesUsecase analyzeFoodDuplicatesUsecase,
      FindDuplicateAnalysisJobStatusUsecase findDuplicateAnalysisJobStatusUsecase,
      FindDuplicateClustersUsecase findDuplicateClustersUsecase,
      MergeDuplicateClusterUsecase mergeDuplicateClusterUsecase,
      RejectDuplicateClusterUsecase rejectDuplicateClusterUsecase) {
    this.analyzeFoodDuplicatesUsecase = analyzeFoodDuplicatesUsecase;
    this.findDuplicateAnalysisJobStatusUsecase = findDuplicateAnalysisJobStatusUsecase;
    this.findDuplicateClustersUsecase = findDuplicateClustersUsecase;
    this.mergeDuplicateClusterUsecase = mergeDuplicateClusterUsecase;
    this.rejectDuplicateClusterUsecase = rejectDuplicateClusterUsecase;
  }

  /**
   * Starts an asynchronous duplicate-candidate analysis for a single source and immediately
   * returns the (still running) job status.
   *
   * <p>There is currently no role model in this application, so this endpoint is reachable by
   * any authenticated user.
   *
   * @param source the name of the {@code FoodSource} enum constant to analyze
   * @return 202 Accepted with the freshly created job status
   * @throws InvalidSortParameterException if {@code source} is not a valid {@code FoodSource}
   */
  //TODO Marius Restrict to an admin role once a role model exists
  @PostMapping("/analyze")
  public ResponseEntity<DuplicateAnalysisJobStatusDto> analyze(
      @RequestParam("source") String source) {
    DuplicateAnalysisJobStatusDto status = this.analyzeFoodDuplicatesUsecase.startAnalysis(source);
    return new ResponseEntity<>(status, HttpStatus.ACCEPTED);
  }

  @GetMapping("/analyze/{jobId}")
  public DuplicateAnalysisJobStatusDto getAnalysisStatus(@PathVariable UUID jobId) {
    return this.findDuplicateAnalysisJobStatusUsecase.findById(jobId);
  }

  /**
   * Returns a page of proposed duplicate clusters with a given status, for the FR-4 review view.
   *
   * @param status the name of the {@code ClusterStatus} enum constant to filter by, defaults to
   *     {@code PENDING}
   * @param page the zero-based page index, defaults to 0
   * @param size the page size, defaults to 20
   * @return the requested page of clusters
   */
  @GetMapping
  public PageResponseDto<DuplicateClusterDto> getClusters(
      @RequestParam(defaultValue = "PENDING") String status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return this.findDuplicateClustersUsecase.findByStatus(status, page, size);
  }

  @PostMapping("/{clusterId}/merge")
  public DuplicateClusterDto merge(
      @PathVariable UUID clusterId, @RequestBody MergeClusterRequestDto request) {
    return this.mergeDuplicateClusterUsecase.merge(clusterId, request.getCanonicalFoodId());
  }

  @PostMapping("/{clusterId}/reject")
  public DuplicateClusterDto reject(@PathVariable UUID clusterId) {
    return this.rejectDuplicateClusterUsecase.reject(clusterId);
  }
}
