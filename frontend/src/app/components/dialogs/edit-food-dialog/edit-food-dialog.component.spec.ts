import { TestBed, fakeAsync, tick } from '@angular/core/testing'
import { DialogRef, DIALOG_DATA } from '@angular/cdk/dialog'
import { EditFoodDialogComponent } from './edit-food-dialog.component'
import { FoodStore } from '../../../api/stores/food.store'
import { FoodModel } from '../../../models/FoodModel'

describe('EditFoodDialogComponent', () => {
  let dialogRefSpy: jasmine.SpyObj<DialogRef<boolean>>
  let foodStoreSpy: jasmine.SpyObj<FoodStore>

  const food: FoodModel = {
    uuid: 'food-1',
    name: 'Hafermilch',
    calory: 45,
    grams: 100,
    diet: 'VEGAN',
    fat: 1.5,
    saturatedFat: 0.2,
    carbohydrates: 6.7,
    sugar: 4.1,
    fiber: 0.8,
    protein: 1,
    salt: 0.11,
    sodium: 0.04,
  }

  function create(data: FoodModel = food) {
    dialogRefSpy = jasmine.createSpyObj<DialogRef<boolean>>('DialogRef', [
      'close',
    ])
    foodStoreSpy = jasmine.createSpyObj<FoodStore>('FoodStore', ['update'])

    TestBed.configureTestingModule({
      imports: [EditFoodDialogComponent],
      providers: [
        { provide: DialogRef, useValue: dialogRefSpy },
        { provide: DIALOG_DATA, useValue: data },
        { provide: FoodStore, useValue: foodStoreSpy },
      ],
    })

    const fixture = TestBed.createComponent(EditFoodDialogComponent)
    fixture.detectChanges()
    return fixture
  }

  it('prefills the form controls with the given food item values', () => {
    const fixture = create()
    const component = fixture.componentInstance

    expect(component.food.value.name).toBe('Hafermilch')
    expect(component.food.value.grams).toBe('100')
    expect(component.food.value.calory).toBe('45')
    expect(component.food.value.diet).toBe('VEGAN')
    expect(component.food.value.fat).toBe('1.5')
    expect(component.food.value.sodium).toBe('0.04')
  })

  it('prefills optional macronutrient fields as empty when missing', () => {
    const fixture = create({
      uuid: 'food-2',
      name: 'Manuell',
      calory: 50,
      grams: 100,
    })
    const component = fixture.componentInstance

    expect(component.food.value.fat).toBe('')
    expect(component.food.value.sodium).toBe('')
  })

  it('submits the updated values and closes the dialog on success', fakeAsync(() => {
    const fixture = create()
    const component = fixture.componentInstance
    foodStoreSpy.update.and.resolveTo(true)

    component.food.patchValue({ name: 'Hafermilch Bio', calory: '48' })
    component.onSubmit()
    tick()

    expect(foodStoreSpy.update).toHaveBeenCalledWith(
      'food-1',
      jasmine.objectContaining({ name: 'Hafermilch Bio', calory: 48 }),
    )
    expect(dialogRefSpy.close).toHaveBeenCalledWith(true)
  }))

  it('sends undefined for an emptied optional macronutrient field', fakeAsync(() => {
    const fixture = create()
    const component = fixture.componentInstance
    foodStoreSpy.update.and.resolveTo(true)

    component.food.patchValue({ fat: '' })
    component.onSubmit()
    tick()

    expect(foodStoreSpy.update).toHaveBeenCalledWith(
      'food-1',
      jasmine.objectContaining({ fat: undefined }),
    )
  }))

  it('keeps the dialog open and does not clear the form when the update fails', fakeAsync(() => {
    const fixture = create()
    const component = fixture.componentInstance
    foodStoreSpy.update.and.resolveTo(false)

    component.food.patchValue({ name: 'Hafermilch Bio' })
    component.onSubmit()
    tick()

    expect(dialogRefSpy.close).not.toHaveBeenCalled()
    expect(component.food.value.name).toBe('Hafermilch Bio')
  }))

  it('does not submit when the form is invalid', fakeAsync(() => {
    const fixture = create()
    const component = fixture.componentInstance

    component.food.patchValue({ name: '' })
    component.onSubmit()
    tick()

    expect(foodStoreSpy.update).not.toHaveBeenCalled()
  }))
})
