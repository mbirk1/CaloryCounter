import { ComponentFixture, TestBed } from '@angular/core/testing'
import { signal } from '@angular/core'
import { DataQualityComponent } from './data-quality.component'
import { DuplicateStore } from '../../api/stores/duplicate.store'
import { DuplicateClusterModel } from '../../models/DuplicateClusterModel'
import { FoodModel } from '../../models/FoodModel'

describe('DataQualityComponent', () => {
  let duplicateStoreSpy: jasmine.SpyObj<DuplicateStore>

  function food(uuid: string, name: string, calory: number): FoodModel {
    return { uuid, name, calory, grams: 100 }
  }

  function cluster(
    overrides: Partial<DuplicateClusterModel> = {},
  ): DuplicateClusterModel {
    return {
      id: 'cluster-1',
      source: 'BLS',
      status: 'PENDING',
      members: [
        {
          food: food('food-1', 'Apfel', 52),
          similarityScore: 0.8,
          suggestedCanonical: true,
        },
        {
          food: food('food-2', 'Apfel rot', 52),
          similarityScore: 0.8,
          suggestedCanonical: false,
        },
      ],
      createdAt: '2026-01-01T00:00:00Z',
      ...overrides,
    }
  }

  function create(
    clusters: DuplicateClusterModel[] = [cluster()],
  ): ComponentFixture<DataQualityComponent> {
    duplicateStoreSpy = jasmine.createSpyObj<DuplicateStore>('DuplicateStore', [
      'analyze',
      'merge',
      'reject',
    ])
    Object.defineProperty(duplicateStoreSpy, 'clusters', {
      value: signal(clusters),
      configurable: true,
    })
    Object.defineProperty(duplicateStoreSpy, 'isAnalyzing', {
      value: signal(false),
      configurable: true,
    })
    Object.defineProperty(duplicateStoreSpy, 'analysisPhase', {
      value: signal('idle'),
      configurable: true,
    })
    Object.defineProperty(duplicateStoreSpy, 'analysisJobStatus', {
      value: signal(null),
      configurable: true,
    })
    Object.defineProperty(duplicateStoreSpy, 'analysisErrorMessage', {
      value: signal(null),
      configurable: true,
    })
    duplicateStoreSpy.merge.and.resolveTo(true)
    duplicateStoreSpy.reject.and.resolveTo(true)

    TestBed.configureTestingModule({
      imports: [DataQualityComponent],
      providers: [{ provide: DuplicateStore, useValue: duplicateStoreSpy }],
    })
    const fixture = TestBed.createComponent(DataQualityComponent)
    fixture.detectChanges()
    return fixture
  }

  it('defaults the canonical selection to the suggested member', () => {
    const component = create().componentInstance

    expect(component.selectedCanonicalId(cluster())).toBe('food-1')
  })

  it('falls back to the first member when none is suggested', () => {
    const noSuggestion = cluster({
      members: [
        {
          food: food('food-3', 'Birne', 57),
          similarityScore: 0.7,
          suggestedCanonical: false,
        },
        {
          food: food('food-4', 'Birne Williams', 57),
          similarityScore: 0.7,
          suggestedCanonical: false,
        },
      ],
    })
    const component = create([noSuggestion]).componentInstance

    expect(component.selectedCanonicalId(noSuggestion)).toBe('food-3')
  })

  it('overrides the canonical selection when the user picks a different member', () => {
    const component = create().componentInstance
    const testCluster = cluster()

    component.selectCanonical(testCluster, 'food-2')

    expect(component.selectedCanonicalId(testCluster)).toBe('food-2')
  })

  it('triggers analysis for the given source', () => {
    const component = create().componentInstance

    component.analyze('BLS')

    expect(duplicateStoreSpy.analyze).toHaveBeenCalledWith('BLS')
  })

  it('merges the cluster using the currently selected canonical food', async () => {
    const component = create().componentInstance
    const testCluster = cluster()

    await component.merge(testCluster)

    expect(duplicateStoreSpy.merge).toHaveBeenCalledWith('cluster-1', 'food-1')
  })

  it('merges using an explicitly selected non-suggested canonical food', async () => {
    const component = create().componentInstance
    const testCluster = cluster()
    component.selectCanonical(testCluster, 'food-2')

    await component.merge(testCluster)

    expect(duplicateStoreSpy.merge).toHaveBeenCalledWith('cluster-1', 'food-2')
  })

  it('rejects the cluster', async () => {
    const component = create().componentInstance
    const testCluster = cluster()

    await component.reject(testCluster)

    expect(duplicateStoreSpy.reject).toHaveBeenCalledWith('cluster-1')
  })

  it('renders a card per pending cluster with member names', () => {
    const fixture = create()

    const text = (fixture.nativeElement as HTMLElement).textContent ?? ''
    expect(text).toContain('Apfel')
    expect(text).toContain('Apfel rot')
  })

  it('shows a hint when there are no pending clusters', () => {
    const fixture = create([])

    const text = (fixture.nativeElement as HTMLElement).textContent ?? ''
    expect(text).toContain('Keine offenen Duplikat-Vorschläge vorhanden.')
  })
})
