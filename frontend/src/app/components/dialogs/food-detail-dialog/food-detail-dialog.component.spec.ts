import { TestBed } from '@angular/core/testing'
import { DialogRef, DIALOG_DATA } from '@angular/cdk/dialog'
import { FoodDetailDialogComponent } from './food-detail-dialog.component'
import { FoodModel } from '../../../models/FoodModel'

describe('FoodDetailDialogComponent', () => {
  let dialogRefSpy: jasmine.SpyObj<DialogRef>

  const food = (overrides: Partial<FoodModel> = {}): FoodModel => ({
    uuid: 'food-1',
    name: 'Tofu',
    calory: 120,
    grams: 100,
    ...overrides,
  })

  function create(data: FoodModel) {
    dialogRefSpy = jasmine.createSpyObj<DialogRef>('DialogRef', ['close'])

    TestBed.configureTestingModule({
      imports: [FoodDetailDialogComponent],
      providers: [
        { provide: DialogRef, useValue: dialogRefSpy },
        { provide: DIALOG_DATA, useValue: data },
      ],
    })

    const fixture = TestBed.createComponent(FoodDetailDialogComponent)
    fixture.detectChanges()
    return fixture
  }

  it('shows the vegan badge for a VEGAN food', () => {
    const fixture = create(food({ diet: 'VEGAN' }))

    const badge = (fixture.nativeElement as HTMLElement).querySelector(
      '[aria-label="Vegan"]',
    )
    expect(badge).not.toBeNull()
  })

  it('shows the vegetarian badge for a VEGETARIAN food', () => {
    const fixture = create(food({ diet: 'VEGETARIAN' }))

    const badge = (fixture.nativeElement as HTMLElement).querySelector(
      '[aria-label="Vegetarisch"]',
    )
    expect(badge).not.toBeNull()
  })

  it('shows the non-vegetarian badge for a NON_VEGETARIAN food', () => {
    const fixture = create(food({ diet: 'NON_VEGETARIAN' }))

    const badge = (fixture.nativeElement as HTMLElement).querySelector(
      '[aria-label="Nicht vegetarisch"]',
    )
    expect(badge).not.toBeNull()
  })

  it('shows no diet badge for an UNKNOWN food', () => {
    const fixture = create(food({ diet: 'UNKNOWN' }))

    const el = fixture.nativeElement as HTMLElement
    expect(el.querySelector('[aria-label="Vegan"]')).toBeNull()
    expect(el.querySelector('[aria-label="Vegetarisch"]')).toBeNull()
    expect(el.querySelector('[aria-label="Nicht vegetarisch"]')).toBeNull()
  })

  it('closes the dialog when the close button is clicked', () => {
    const fixture = create(food())

    const closeButton = (
      fixture.nativeElement as HTMLElement
    ).querySelector<HTMLButtonElement>('button[aria-label="Schließen"]')
    closeButton?.click()

    expect(dialogRefSpy.close).toHaveBeenCalled()
  })

  it('renders no image element when imageUrl is missing', () => {
    const fixture = create(food({ imageUrl: undefined }))

    const img = (fixture.nativeElement as HTMLElement).querySelector('img')
    expect(img).toBeNull()
  })

  it('shows a dash instead of null/undefined for missing nutrient values', () => {
    const fixture = create(food({ fiber: undefined }))

    const text = (fixture.nativeElement as HTMLElement).textContent ?? ''
    expect(text).toContain('–')
    expect(text).not.toContain('null')
    expect(text).not.toContain('undefined')
  })
})
