import { TestBed } from '@angular/core/testing'
import { Dialog } from '@angular/cdk/dialog'
import { RecipeTableComponent } from './recipe-table.component'
import { RecipeStore } from '../../api/stores/recipe.store'
import { RecipeModel } from '../../models/RecipeModel'
import { RecipeDetailDialogComponent } from '../dialogs/recipe-detail-dialog/recipe-detail-dialog.component'

describe('RecipeTableComponent', () => {
  let recipeStoreSpy: jasmine.SpyObj<RecipeStore>
  let dialogSpy: jasmine.SpyObj<Dialog>

  const recipe = (overrides: Partial<RecipeModel> = {}): RecipeModel => ({
    uuid: 'recipe-1',
    name: 'Bowl',
    servings: 2,
    foods: [],
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

  function create(recipes: RecipeModel[]) {
    recipeStoreSpy = jasmine.createSpyObj<RecipeStore>('RecipeStore', [
      'delete',
    ])
    Object.defineProperty(recipeStoreSpy, 'recipes', {
      value: () => recipes,
    })
    dialogSpy = jasmine.createSpyObj<Dialog>('Dialog', ['open'])

    TestBed.configureTestingModule({
      imports: [RecipeTableComponent],
      providers: [
        { provide: RecipeStore, useValue: recipeStoreSpy },
        { provide: Dialog, useValue: dialogSpy },
      ],
    })

    const fixture = TestBed.createComponent(RecipeTableComponent)
    fixture.componentRef.setInput('columnHeaders', [
      'Name',
      'Diät',
      'Kalorien gesamt',
      'Kalorien pro Portion',
      'Actions',
    ])
    fixture.detectChanges()
    return fixture
  }

  it('renders totalCalories and caloriesPerServing directly from the model', () => {
    const fixture = create([
      recipe({ totalCalories: 300, caloriesPerServing: 150 }),
    ])

    const text = (fixture.nativeElement as HTMLElement).textContent ?? ''
    expect(text).toContain('300')
    expect(text).toContain('150')
  })

  it('shows the diet badge for the recipe', () => {
    const fixture = create([recipe({ diet: 'VEGAN' })])

    const badge = (fixture.nativeElement as HTMLElement).querySelector(
      '[aria-label="Vegan"]',
    )
    expect(badge).not.toBeNull()
  })

  it('opens the recipe detail dialog with the row data when "Details anzeigen" is clicked', () => {
    const target = recipe({ uuid: 'recipe-42' })
    const fixture = create([target])

    const button = (
      fixture.nativeElement as HTMLElement
    ).querySelector<HTMLButtonElement>('button[aria-label="Details anzeigen"]')
    button?.click()

    expect(dialogSpy.open).toHaveBeenCalledWith(RecipeDetailDialogComponent, {
      data: target,
    })
  })

  it('calls RecipeStore.delete with the clicked row id', () => {
    const fixture = create([recipe({ uuid: 'recipe-99' })])
    const component = fixture.componentInstance

    component.delete('recipe-99')

    expect(recipeStoreSpy.delete).toHaveBeenCalledWith('recipe-99')
  })
})
