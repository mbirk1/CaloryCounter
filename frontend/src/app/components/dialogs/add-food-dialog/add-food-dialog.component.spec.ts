import { TestBed, fakeAsync, tick } from '@angular/core/testing'
import { DialogRef, DIALOG_DATA } from '@angular/cdk/dialog'
import { AddFoodDialogComponent } from './add-food-dialog.component'
import { FoodStore } from '../../../api/stores/food.store'

describe('AddFoodDialogComponent', () => {
  let dialogRefSpy: jasmine.SpyObj<DialogRef<boolean>>
  let foodStoreSpy: jasmine.SpyObj<FoodStore>

  function create() {
    dialogRefSpy = jasmine.createSpyObj<DialogRef<boolean>>('DialogRef', [
      'close',
    ])
    foodStoreSpy = jasmine.createSpyObj<FoodStore>('FoodStore', ['save'])

    TestBed.configureTestingModule({
      imports: [AddFoodDialogComponent],
      providers: [
        { provide: DialogRef, useValue: dialogRefSpy },
        { provide: DIALOG_DATA, useValue: undefined },
        { provide: FoodStore, useValue: foodStoreSpy },
      ],
    })

    const fixture = TestBed.createComponent(AddFoodDialogComponent)
    fixture.detectChanges()
    return fixture
  }

  it('starts with only the base fields required, everything else optional and empty', () => {
    const fixture = create()
    const component = fixture.componentInstance

    expect(component.food.value.name).toBe('')
    expect(component.food.value.diet).toBe('UNKNOWN')
    expect(component.food.value.brand).toBe('')
    expect(component.food.value.category).toBe('')
    expect(component.food.value.imageUrl).toBe('')
    expect(component.food.value.fat).toBe('')
    expect(component.food.value.vitaminC).toBe('')
    expect(component.food.value.iron).toBe('')

    expect(component.food.valid).toBe(false)
    component.food.patchValue({ name: 'Tofu', calory: '100', grams: '100' })
    expect(component.food.valid).toBe(true)
  })

  it('submits with brand/category/image/macros converted to undefined when left empty', fakeAsync(() => {
    const fixture = create()
    const component = fixture.componentInstance
    foodStoreSpy.save.and.resolveTo(true)

    component.food.patchValue({ name: 'Tofu', calory: '100', grams: '100' })
    component.onSubmit()
    tick()

    expect(foodStoreSpy.save).toHaveBeenCalledWith(
      jasmine.objectContaining({
        name: 'Tofu',
        calory: 100,
        grams: 100,
        brand: undefined,
        category: undefined,
        imageUrl: undefined,
        fat: undefined,
        vitaminC: undefined,
        iron: undefined,
      }),
    )
  }))

  it('submits brand, category, image URL and macronutrients when filled in', fakeAsync(() => {
    const fixture = create()
    const component = fixture.componentInstance
    foodStoreSpy.save.and.resolveTo(true)

    component.food.patchValue({
      name: 'Hafermilch',
      calory: '45',
      grams: '100',
      diet: 'VEGAN',
      brand: 'Oatly',
      category: 'Plant-based beverages',
      imageUrl: 'https://example.com/oatly.png',
      fat: '1.5',
      sodium: '0.04',
    })
    component.onSubmit()
    tick()

    expect(foodStoreSpy.save).toHaveBeenCalledWith(
      jasmine.objectContaining({
        name: 'Hafermilch',
        diet: 'VEGAN',
        brand: 'Oatly',
        category: 'Plant-based beverages',
        imageUrl: 'https://example.com/oatly.png',
        fat: 1.5,
        sodium: 0.04,
      }),
    )
  }))

  it('submits micronutrients when filled in, converted to numbers', fakeAsync(() => {
    const fixture = create()
    const component = fixture.componentInstance
    foodStoreSpy.save.and.resolveTo(true)

    component.food.patchValue({
      name: 'Hafer ganzes Korn, roh',
      calory: '343',
      grams: '100',
      vitaminC: '0',
      iron: '4.7',
      potassium: '429',
    })
    component.onSubmit()
    tick()

    expect(foodStoreSpy.save).toHaveBeenCalledWith(
      jasmine.objectContaining({
        vitaminC: 0,
        iron: 4.7,
        potassium: 429,
      }),
    )
  }))

  it('closes the dialog when the creation succeeds', fakeAsync(() => {
    const fixture = create()
    const component = fixture.componentInstance
    foodStoreSpy.save.and.resolveTo(true)

    component.food.patchValue({ name: 'Tofu', calory: '100', grams: '100' })
    component.onSubmit()
    tick()

    expect(dialogRefSpy.close).toHaveBeenCalledWith(true)
  }))

  it('keeps the dialog open with entered values when the creation fails', fakeAsync(() => {
    const fixture = create()
    const component = fixture.componentInstance
    foodStoreSpy.save.and.resolveTo(false)

    component.food.patchValue({ name: 'Tofu', calory: '100', grams: '100' })
    component.onSubmit()
    tick()

    expect(dialogRefSpy.close).not.toHaveBeenCalled()
    expect(component.food.value.name).toBe('Tofu')
  }))

  it('does not submit when the form is invalid', fakeAsync(() => {
    const fixture = create()
    const component = fixture.componentInstance

    component.food.patchValue({ name: '' })
    component.onSubmit()
    tick()

    expect(foodStoreSpy.save).not.toHaveBeenCalled()
  }))

  it('shows an image preview once an image URL is entered', () => {
    const fixture = create()
    const component = fixture.componentInstance

    component.food.patchValue({ imageUrl: 'https://example.com/oatly.png' })
    fixture.detectChanges()

    const img = (fixture.nativeElement as HTMLElement).querySelector('img')
    expect(img?.getAttribute('src')).toBe('https://example.com/oatly.png')
  })

  it('hides the image preview again after the image fails to load', () => {
    const fixture = create()
    const component = fixture.componentInstance

    component.food.patchValue({ imageUrl: 'https://example.com/broken.png' })
    fixture.detectChanges()
    component.onImageError()
    fixture.detectChanges()

    const img = (fixture.nativeElement as HTMLElement).querySelector('img')
    expect(img).toBeNull()
  })

  it('shows no image preview when the field is empty', () => {
    const fixture = create()

    const img = (fixture.nativeElement as HTMLElement).querySelector('img')
    expect(img).toBeNull()
  })
})
