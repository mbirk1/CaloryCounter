package de.birk.calory.adapter.primary;

import java.io.IOException;
import java.util.List;
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
import org.springframework.web.multipart.MultipartFile;

import de.birk.calory.adapter.primary.annotations.DeleteRequest;
import de.birk.calory.adapter.primary.annotations.GetRequest;
import de.birk.calory.adapter.primary.annotations.PostRequest;
import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.primary.model.FoodDto;
import de.birk.calory.adapter.primary.model.ImportJobStatusDto;
import de.birk.calory.adapter.primary.model.PageResponseDto;
import de.birk.calory.usecase.food.CreateFoodUsecase;
import de.birk.calory.usecase.food.DeleteFoodUsecase;
import de.birk.calory.usecase.food.FindFoodUsecase;
import de.birk.calory.usecase.food.importer.FindImportJobStatusUsecase;
import de.birk.calory.usecase.food.importer.FoodCsvImportUsecase;

/**
 * RestController for all food related requests.
 *
 * @author Marius Birk
 */
@RestController
@RequestMapping("/api/food")
//TODO Marius Should be outsourced to env variable
@CrossOrigin(origins = "http://localhost:4200")
public class FoodRestController {
  private final FindFoodUsecase findFoodUsecase;
  private final CreateFoodUsecase createFoodUsecase;
  private final DeleteFoodUsecase deleteFoodUsecase;
  private final FoodCsvImportUsecase foodCsvImportUsecase;
  private final FindImportJobStatusUsecase findImportJobStatusUsecase;


  /**
   *  Every necessary usecase needed to complete the incoming requests.
   *
   * @param findFoodUsecase Find Food Usecase
   * @param createFoodUsecase Create Food Usecase
   * @param deleteFoodUsecase Delete Foor Usecase
   * @param foodCsvImportUsecase Food Csv Import Usecase
   * @param findImportJobStatusUsecase Find Import Job Status Usecase
   */
  public FoodRestController(
      FindFoodUsecase findFoodUsecase,
      CreateFoodUsecase createFoodUsecase,
      DeleteFoodUsecase deleteFoodUsecase,
      FoodCsvImportUsecase foodCsvImportUsecase,
      FindImportJobStatusUsecase findImportJobStatusUsecase) {
    this.findFoodUsecase = findFoodUsecase;
    this.createFoodUsecase = createFoodUsecase;
    this.deleteFoodUsecase = deleteFoodUsecase;
    this.foodCsvImportUsecase = foodCsvImportUsecase;
    this.findImportJobStatusUsecase = findImportJobStatusUsecase;
  }

  /**
   * Returns a page of food items, ordered by name.
   *
   * @param page the zero-based page index, defaults to 0
   * @param size the page size, defaults to 20
   * @return the requested page of food items
   */
  @GetRequest()
  public PageResponseDto<FoodDetailsDto> getAllFoods(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return this.findFoodUsecase.findAllFoods(page, size);
  }

  @GetRequest("/{id}")
  public FoodDetailsDto getFood(@PathVariable UUID id) {
    return this.findFoodUsecase.findFoodById(id);
  }

  @PostRequest
  public FoodDetailsDto createFood(@RequestBody FoodDto foodDto) {
    return this.createFoodUsecase.createFood(foodDto);
  }

  @DeleteRequest("/{id}")
  public List<FoodDetailsDto> deleteFood(@PathVariable UUID id) {
    return this.deleteFoodUsecase.deleteFood(id);
  }

  /**
   * Starts an asynchronous CSV import of food items and immediately returns the (still running)
   * job status.
   *
   * <p>There is currently no role model in this application, so this endpoint is reachable by
   * any authenticated user.
   *
   * @param file the uploaded CSV (or gzip-compressed CSV) file
   * @return 202 Accepted with the freshly created job status
   * @throws IOException if the uploaded file cannot be staged for the import
   */
  //TODO Marius Restrict to an admin role once a role model exists
  @PostMapping("/import")
  public ResponseEntity<ImportJobStatusDto> importFoods(@RequestParam("file") MultipartFile file)
      throws IOException {
    ImportJobStatusDto status = this.foodCsvImportUsecase.startImport(file);
    return new ResponseEntity<>(status, HttpStatus.ACCEPTED);
  }

  @GetMapping("/import/{jobId}")
  public ImportJobStatusDto getImportStatus(@PathVariable UUID jobId) {
    return this.findImportJobStatusUsecase.findById(jobId);
  }
}
