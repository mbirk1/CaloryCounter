import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnDestroy,
} from '@angular/core'
import { FormControl, ReactiveFormsModule } from '@angular/forms'
import { debounceTime, distinctUntilChanged, Subscription } from 'rxjs'
import { ButtonComponent } from '../../components/button/button.component'
import { FoodTableComponent } from '../../components/food-table/food-table.component'
import { TextInputComponent } from '../../components/inputs/text-input/text-input.component'
import { Dialog } from '@angular/cdk/dialog'
import { AddFoodDialogComponent } from '../../components/dialogs/add-food-dialog/add-food-dialog.component'
import { CsvImportProgressDialogComponent } from '../../components/dialogs/csv-import-progress-dialog/csv-import-progress-dialog.component'
import { CsvImportService } from '../../services/csv-import.service'
import { FoodStore } from '../../api/stores/food.store'

const SEARCH_DEBOUNCE_MS = 300

interface DietFilterOption {
  value: string | null
  label: string
}

@Component({
  selector: 'app-food',
  imports: [
    ButtonComponent,
    FoodTableComponent,
    ReactiveFormsModule,
    TextInputComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './food.component.html',
})
export class FoodComponent implements OnDestroy {
  private readonly dialog = inject(Dialog)
  protected readonly csvImportService = inject(CsvImportService)
  protected readonly foodStore = inject(FoodStore)

  protected readonly searchControl = new FormControl('')
  private readonly searchSubscription: Subscription

  protected readonly dietOptions: DietFilterOption[] = [
    { value: null, label: 'Alle' },
    { value: 'VEGAN', label: 'Vegan' },
    { value: 'VEGETARIAN', label: 'Vegetarisch' },
    { value: 'NON_VEGETARIAN', label: 'Nicht vegetarisch' },
  ]

  constructor() {
    this.searchSubscription = this.searchControl.valueChanges
      .pipe(debounceTime(SEARCH_DEBOUNCE_MS), distinctUntilChanged())
      .subscribe((value) => this.foodStore.setSearch(value ?? ''))
  }

  ngOnDestroy(): void {
    this.searchSubscription.unsubscribe()
  }

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

  onDietFilterSelected(diet: string | null): void {
    this.foodStore.setDiet(diet)
  }

  isDietActive(diet: string | null): boolean {
    return this.foodStore.diet() === diet
  }
}
