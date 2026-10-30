import { TestBed } from '@angular/core/testing'
import { DialogRef, DIALOG_DATA } from '@angular/cdk/dialog'
import { RecipeDetailDialogComponent } from './recipe-detail-dialog.component'
import { RecipeModel } from '../../../models/RecipeModel'
import { FoodModel } from '../../../models/FoodModel'

describe('RecipeDetailDialogComponent', () => {
  let dialogRefSpy: jasmine.SpyObj<DialogRef>

  const food = (overrides: Partial<FoodModel> = {}): FoodModel => ({
    uuid: 'food-1',
    name: 'Tofu',
    calory: 120,
    grams: 100,
    ...overrides,
  })

  const recipe = (overrides: Partial<RecipeModel> = {}): RecipeModel => ({
    uuid: 'recipe-1',
    name: 'Bowl',
    servings: 2,
    foods: [{ food: food(), amountGrams: 150 }],
    totalCalories: 300,
    totalFat: 10,
    totalSaturatedFat: 2,
    totalCarbohydrates: 20,
    totalSugar: 5,
    totalFiber: 3,
    totalProtein: 15,
    totalSalt: 1,
    totalSodium: 0.5,
    caloriesPerServing: 150,
    fatPerServing: 5,
    saturatedFatPerServing: 1,
    carbohydratesPerServing: 10,
    sugarPerServing: 2.5,
    fiberPerServing: 1.5,
    proteinPerServing: 7.5,
    saltPerServing: 0.5,
    sodiumPerServing: 0.25,
    diet: 'VEGAN',
    ...overrides,
  })

  function create(data: RecipeModel) {
    dialogRefSpy = jasmine.createSpyObj<DialogRef>('DialogRef', ['close'])

    TestBed.configureTestingModule({
      imports: [RecipeDetailDialogComponent],
      providers: [
        { provide: DialogRef, useValue: dialogRefSpy },
        { provide: DIALOG_DATA, useValue: data },
      ],
    })

    const fixture = TestBed.createComponent(RecipeDetailDialogComponent)
    fixture.detectChanges()
    return fixture
  }

  it('shows the recipe name and diet badge', () => {
    const fixture = create(recipe({ diet: 'VEGAN' }))

    const el = fixture.nativeElement as HTMLElement
    expect(el.textContent).toContain('Bowl')
    expect(el.querySelector('[aria-label="Vegan"]')).not.toBeNull()
  })

  it('shows total and per-serving calories', () => {
    const fixture = create(
      recipe({ totalCalories: 300, caloriesPerServing: 150 }),
    )

    const text = (fixture.nativeElement as HTMLElement).textContent ?? ''
    expect(text).toContain('300')
    expect(text).toContain('150')
  })

  it('lists each contained food with its amount', () => {
    const fixture = create(
      recipe({ foods: [{ food: food({ name: 'Tofu' }), amountGrams: 150 }] }),
    )

    const text = (fixture.nativeElement as HTMLElement).textContent ?? ''
    expect(text).toContain('Tofu')
    expect(text).toContain('150')
  })

  it('closes the dialog when the close button is clicked', () => {
    const fixture = create(recipe())

    const closeButton = (
      fixture.nativeElement as HTMLElement
    ).querySelector<HTMLButtonElement>('button[aria-label="Schließen"]')
    closeButton?.click()

    expect(dialogRefSpy.close).toHaveBeenCalled()
  })

  it('shows a dash instead of null/undefined for a missing per-serving nutrient', () => {
    const fixture = create(
      recipe({ fiberPerServing: undefined as unknown as number }),
    )

    const text = (fixture.nativeElement as HTMLElement).textContent ?? ''
    expect(text).toContain('–')
  })
})
