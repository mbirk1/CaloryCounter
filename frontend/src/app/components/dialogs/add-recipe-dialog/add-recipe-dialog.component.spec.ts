import { TestBed, fakeAsync, tick } from '@angular/core/testing'
import { DialogRef, DIALOG_DATA } from '@angular/cdk/dialog'
import { AddRecipeDialogComponent } from './add-recipe-dialog.component'
import { FoodStore } from '../../../api/stores/food.store'
import { RecipeStore } from '../../../api/stores/recipe.store'
import { FoodModel } from '../../../models/FoodModel'

const SEARCH_DEBOUNCE_MS = 300

describe('AddRecipeDialogComponent', () => {
  let dialogRefSpy: jasmine.SpyObj<DialogRef<boolean>>
  let foodStoreSpy: jasmine.SpyObj<FoodStore>
  let recipeStoreSpy: jasmine.SpyObj<RecipeStore>

  const tofu: FoodModel = {
    uuid: 'food-1',
    name: 'Tofu',
    calory: 120,
    grams: 100,
  }
  const reis: FoodModel = {
    uuid: 'food-2',
    name: 'Reis',
    calory: 130,
    grams: 100,
  }
  const veganFoodWithFat: FoodModel = {
    uuid: 'food-vegan',
    name: 'Tofu Vegan',
    calory: 200,
    grams: 100,
    fat: 10,
    diet: 'VEGAN',
  }
  const vegetarianFoodNoFat: FoodModel = {
    uuid: 'food-veg',
    name: 'Käse',
    calory: 300,
    grams: 100,
    diet: 'VEGETARIAN',
  }

  function create() {
    dialogRefSpy = jasmine.createSpyObj<DialogRef<boolean>>('DialogRef', [
      'close',
    ])
    foodStoreSpy = jasmine.createSpyObj<FoodStore>('FoodStore', ['searchFoods'])
    foodStoreSpy.searchFoods.and.resolveTo([])
    recipeStoreSpy = jasmine.createSpyObj<RecipeStore>('RecipeStore', ['save'])

    TestBed.configureTestingModule({
      imports: [AddRecipeDialogComponent],
      providers: [
        { provide: DialogRef, useValue: dialogRefSpy },
        { provide: DIALOG_DATA, useValue: undefined },
        { provide: FoodStore, useValue: foodStoreSpy },
        { provide: RecipeStore, useValue: recipeStoreSpy },
      ],
    })

    const fixture = TestBed.createComponent(AddRecipeDialogComponent)
    fixture.detectChanges()
    return fixture
  }

  it('defaults servings to 1 and starts with no foods selected', () => {
    const fixture = create()
    const component = fixture.componentInstance

    expect(component.recipe.value.servings).toBe('1')
    expect(component.entries.length).toBe(0)
  })

  it('debounces the search input and calls FoodStore.searchFoods (not the shared setSearch)', fakeAsync(() => {
    const fixture = create()
    const component = fixture.componentInstance
    foodStoreSpy.searchFoods.and.resolveTo([tofu])

    component.searchControl.setValue('to')
    tick(SEARCH_DEBOUNCE_MS - 1)
    expect(foodStoreSpy.searchFoods).not.toHaveBeenCalled()

    tick(1)
    tick()
    fixture.detectChanges()

    expect(foodStoreSpy.searchFoods).toHaveBeenCalledWith('to')
    expect(foodStoreSpy.searchFoods).toHaveBeenCalledTimes(1)
  }))

  it('shows search results and adds a food to the entries list on click', fakeAsync(() => {
    const fixture = create()
    const component = fixture.componentInstance
    foodStoreSpy.searchFoods.and.resolveTo([tofu, reis])

    component.searchControl.setValue('e')
    tick(SEARCH_DEBOUNCE_MS)
    tick()
    fixture.detectChanges()

    expect(component.searchResults()).toEqual([tofu, reis])

    component.addFood(tofu)
    fixture.detectChanges()

    expect(component.entries.length).toBe(1)
    expect(component.entries.at(0).value.foodId).toBe('food-1')
    expect(component.entries.at(0).value.name).toBe('Tofu')
    expect(component.searchResults()).toEqual([])
    expect(component.searchControl.value).toBe('')
  }))

  it('filters already-selected foods out of new search results', fakeAsync(() => {
    const fixture = create()
    const component = fixture.componentInstance
    component.addFood(tofu)
    foodStoreSpy.searchFoods.and.resolveTo([tofu, reis])

    component.searchControl.setValue('e')
    tick(SEARCH_DEBOUNCE_MS)
    tick()
    fixture.detectChanges()

    expect(component.searchResults()).toEqual([reis])
  }))

  it('removes a food from the entries list', () => {
    const fixture = create()
    const component = fixture.componentInstance
    component.addFood(tofu)
    component.addFood(reis)

    component.removeFood(0)

    expect(component.entries.length).toBe(1)
    expect(component.entries.at(0).value.foodId).toBe('food-2')
  })

  it("defaults a newly added food's amount to 100g", () => {
    const fixture = create()
    const component = fixture.componentInstance

    component.addFood(tofu)

    expect(component.entries.at(0).get('amountGrams')?.value).toBe('100')
    expect(component.entries.at(0).get('amountGrams')?.valid).toBe(true)
  })

  it('requires a positive amount for each selected food', () => {
    const fixture = create()
    const component = fixture.componentInstance
    component.addFood(tofu)
    const amountControl = component.entries.at(0).get('amountGrams')

    amountControl?.setValue('')
    expect(amountControl?.valid).toBe(false)
    expect(component.recipe.valid).toBe(false)

    amountControl?.setValue('150')
    expect(amountControl?.valid).toBe(true)
  })

  it('requires a positive integer for servings', () => {
    const fixture = create()
    const component = fixture.componentInstance
    const servingsControl = component.recipe.get('servings')

    servingsControl?.setValue('0')
    expect(servingsControl?.valid).toBe(false)

    servingsControl?.setValue('4')
    expect(servingsControl?.valid).toBe(true)
  })

  it('submits the selected foods as foodId/amountGrams entries plus servings', fakeAsync(() => {
    const fixture = create()
    const component = fixture.componentInstance
    recipeStoreSpy.save.and.resolveTo(true)
    component.addFood(tofu)
    component.entries.at(0).get('amountGrams')?.setValue('150')
    component.recipe.patchValue({ name: 'Bowl', servings: '3' })

    component.onSubmit()
    tick()

    expect(recipeStoreSpy.save).toHaveBeenCalledWith({
      name: 'Bowl',
      servings: 3,
      foods: [{ foodId: 'food-1', amountGrams: 150 }],
    })
  }))

  it('closes the dialog when the creation succeeds', fakeAsync(() => {
    const fixture = create()
    const component = fixture.componentInstance
    recipeStoreSpy.save.and.resolveTo(true)
    component.addFood(tofu)
    component.entries.at(0).get('amountGrams')?.setValue('150')
    component.recipe.patchValue({ name: 'Bowl' })

    component.onSubmit()
    tick()

    expect(dialogRefSpy.close).toHaveBeenCalledWith(true)
  }))

  it('keeps the dialog open when the creation fails', fakeAsync(() => {
    const fixture = create()
    const component = fixture.componentInstance
    recipeStoreSpy.save.and.resolveTo(false)
    component.addFood(tofu)
    component.entries.at(0).get('amountGrams')?.setValue('150')
    component.recipe.patchValue({ name: 'Bowl' })

    component.onSubmit()
    tick()

    expect(dialogRefSpy.close).not.toHaveBeenCalled()
  }))

  it('does not submit when the form is invalid', fakeAsync(() => {
    const fixture = create()
    const component = fixture.componentInstance

    component.recipe.patchValue({ name: '' })
    component.onSubmit()
    tick()

    expect(recipeStoreSpy.save).not.toHaveBeenCalled()
  }))

  it('disables the submit button while the form is invalid', () => {
    const fixture = create()
    const component = fixture.componentInstance
    component.recipe.patchValue({ name: '' })
    fixture.detectChanges()

    const submitButton = (
      fixture.nativeElement as HTMLElement
    ).querySelector<HTMLButtonElement>('button[type="submit"]')

    expect(submitButton?.disabled).toBe(true)

    component.recipe.patchValue({ name: 'Bowl' })
    component.addFood(tofu)
    fixture.detectChanges()

    expect(submitButton?.disabled).toBe(false)
  })

  it('shows a validation message for a required field once touched', () => {
    const fixture = create()
    const component = fixture.componentInstance
    component.recipe.get('name')?.markAsTouched()
    fixture.detectChanges()

    const text = (fixture.nativeElement as HTMLElement).textContent ?? ''
    expect(text).toContain('Bitte gib einen Namen für das Rezept ein.')
  })

  it('marks all fields as touched when an invalid submit is attempted', fakeAsync(() => {
    const fixture = create()
    const component = fixture.componentInstance

    expect(component.recipe.get('name')?.touched).toBe(false)
    component.onSubmit()
    tick()
    fixture.detectChanges()

    expect(component.recipe.get('name')?.touched).toBe(true)
    const text = (fixture.nativeElement as HTMLElement).textContent ?? ''
    expect(text).toContain('Bitte gib einen Namen für das Rezept ein.')
  }))

  it('closes the dialog when the close button is clicked', () => {
    const fixture = create()

    const closeButton = (
      fixture.nativeElement as HTMLElement
    ).querySelector<HTMLButtonElement>('button[aria-label="Schließen"]')
    closeButton?.click()

    expect(dialogRefSpy.close).toHaveBeenCalled()
  })

  describe('nutritionSummary()', () => {
    it('is vacuously VEGAN with zero totals when no food is selected', () => {
      const fixture = create()
      const component = fixture.componentInstance

      const summary = component.nutritionSummary()

      expect(summary.totalCalories).toBe(0)
      expect(summary.diet).toBe('VEGAN')
    })

    it('scales each food proportionally to its amount and sums across entries', () => {
      const fixture = create()
      const component = fixture.componentInstance
      component.addFood(veganFoodWithFat)
      component.addFood(vegetarianFoodNoFat)
      component.entries.at(0).get('amountGrams')?.setValue('50')
      component.entries.at(1).get('amountGrams')?.setValue('200')

      const summary = component.nutritionSummary()

      // veganFoodWithFat: 200cal/100g * 50g = 100; vegetarianFoodNoFat: 300cal/100g * 200g = 600
      expect(summary.totalCalories).toBe(700)
      expect(summary.totalAmountGrams).toBe(250)
    })

    it("skips a food's null macro instead of treating it as zero", () => {
      const fixture = create()
      const component = fixture.componentInstance
      component.addFood(veganFoodWithFat)
      component.addFood(vegetarianFoodNoFat)
      component.entries.at(0).get('amountGrams')?.setValue('50')
      component.entries.at(1).get('amountGrams')?.setValue('200')

      const summary = component.nutritionSummary()

      // Only veganFoodWithFat has fat set (10g/100g * 50g = 5) - vegetarianFoodNoFat's
      // missing fat must not count as 0.
      expect(summary.totalFat).toBe(5)
    })

    it('divides totals by servings for the per-serving values', () => {
      const fixture = create()
      const component = fixture.componentInstance
      component.addFood(veganFoodWithFat)
      component.entries.at(0).get('amountGrams')?.setValue('100')
      component.recipe.get('servings')?.setValue('4')

      const summary = component.nutritionSummary()

      expect(summary.totalCalories).toBe(200)
      expect(summary.caloriesPerServing).toBe(50)
    })

    it('reports the least strict (weakest-link) diet across selected foods', () => {
      const fixture = create()
      const component = fixture.componentInstance
      component.addFood(veganFoodWithFat)

      expect(component.nutritionSummary().diet).toBe('VEGAN')

      component.addFood(vegetarianFoodNoFat)

      expect(component.nutritionSummary().diet).toBe('VEGETARIAN')
    })
  })
})
