import { ChangeDetectionStrategy, Component, inject } from '@angular/core'
import { ButtonComponent } from '../../components/button/button.component'
import { FoodTableComponent } from '../../components/food-table/food-table.component'
import { Dialog } from '@angular/cdk/dialog'
import { AddFoodDialogComponent } from '../../components/dialogs/add-food-dialog/add-food-dialog.component'
import { CsvImportProgressDialogComponent } from '../../components/dialogs/csv-import-progress-dialog/csv-import-progress-dialog.component'
import { CsvImportService } from '../../services/csv-import.service'

@Component({
  selector: 'app-food',
  imports: [ButtonComponent, FoodTableComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './food.component.html',
})
export class FoodComponent {
  private readonly dialog = inject(Dialog)
  protected readonly csvImportService = inject(CsvImportService)

  handleFoodDialog() {
    this.dialog.open(AddFoodDialogComponent, {})
  }

  onCsvFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement
    const file = input.files?.[0]
    input.value = ''
    if (!file) {
      return
    }

    this.csvImportService.upload(file)
    this.dialog.open(CsvImportProgressDialogComponent, { disableClose: true })
  }
}
