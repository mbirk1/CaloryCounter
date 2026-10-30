import { TestBed } from '@angular/core/testing'
import { RecipeNutrientSummaryComponent } from './recipe-nutrient-summary.component'

describe('RecipeNutrientSummaryComponent', () => {
  function create() {
    TestBed.configureTestingModule({
      imports: [RecipeNutrientSummaryComponent],
    })

    const fixture = TestBed.createComponent(RecipeNutrientSummaryComponent)
    return fixture
  }

  it('shows total and per-serving calories plus the servings count', () => {
    const fixture = create()
    fixture.componentRef.setInput('totalCalories', 300)
    fixture.componentRef.setInput('caloriesPerServing', 150)
    fixture.componentRef.setInput('servings', 2)
    fixture.detectChanges()

    const text = (fixture.nativeElement as HTMLElement).textContent ?? ''
    expect(text).toContain('300')
    expect(text).toContain('150')
    expect(text).toContain('2')
  })

  it('shows the total amount of grams next to the servings count', () => {
    const fixture = create()
    fixture.componentRef.setInput('servings', 3)
    fixture.componentRef.setInput('totalAmountGrams', 450)
    fixture.detectChanges()

    const text = (fixture.nativeElement as HTMLElement).textContent ?? ''
    expect(text).toContain('3 Portionen')
    expect(text).toContain('450 g gesamt')
  })

  it('shows each macro value with a "g" unit', () => {
    const fixture = create()
    fixture.componentRef.setInput('fatPerServing', 5)
    fixture.componentRef.setInput('proteinPerServing', 12)
    fixture.detectChanges()

    const text = (fixture.nativeElement as HTMLElement).textContent ?? ''
    expect(text).toContain('5 g')
    expect(text).toContain('12 g')
  })

  it('shows a dash instead of null/undefined for a missing macro value', () => {
    const fixture = create()
    fixture.componentRef.setInput('fatPerServing', undefined)
    fixture.detectChanges()

    const text = (fixture.nativeElement as HTMLElement).textContent ?? ''
    expect(text).toContain('–')
  })
})
