import { TestBed } from '@angular/core/testing'
import { DietBadgeComponent } from './diet-badge.component'

describe('DietBadgeComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [DietBadgeComponent],
    })
  })

  function create(diet: string | undefined) {
    const fixture = TestBed.createComponent(DietBadgeComponent)
    fixture.componentRef.setInput('diet', diet)
    fixture.detectChanges()
    return fixture
  }

  it('shows a labeled leaf icon for VEGAN', () => {
    const fixture = create('VEGAN')
    const el = (fixture.nativeElement as HTMLElement).querySelector('span')

    expect(el).not.toBeNull()
    expect(el?.getAttribute('aria-label')).toBe('Vegan')
  })

  it('shows a labeled seedling icon for VEGETARIAN', () => {
    const fixture = create('VEGETARIAN')
    const el = (fixture.nativeElement as HTMLElement).querySelector('span')

    expect(el).not.toBeNull()
    expect(el?.getAttribute('aria-label')).toBe('Vegetarisch')
  })

  it('shows a labeled drumstick icon for NON_VEGETARIAN', () => {
    const fixture = create('NON_VEGETARIAN')
    const el = (fixture.nativeElement as HTMLElement).querySelector('span')

    expect(el).not.toBeNull()
    expect(el?.getAttribute('aria-label')).toBe('Nicht vegetarisch')
  })

  it('renders nothing for UNKNOWN', () => {
    const fixture = create('UNKNOWN')
    const el = (fixture.nativeElement as HTMLElement).querySelector('span')

    expect(el).toBeNull()
  })

  it('renders nothing when diet is undefined', () => {
    const fixture = create(undefined)
    const el = (fixture.nativeElement as HTMLElement).querySelector('span')

    expect(el).toBeNull()
  })
})
