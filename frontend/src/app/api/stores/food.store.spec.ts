import { TestBed } from '@angular/core/testing'
import { provideHttpClient } from '@angular/common/http'
import { provideHttpClientTesting } from '@angular/common/http/testing'
import { FoodStore } from './food.store'

describe('FoodStore', () => {
  let store: FoodStore

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    })
    store = TestBed.inject(FoodStore)
  })

  it('starts with no search, no diet filter, and name ascending', () => {
    expect(store.search()).toBeNull()
    expect(store.diet()).toBeNull()
    expect(store.sort()).toBe('name')
    expect(store.direction()).toBe('asc')
  })

  it('trims the search term and treats a blank value as no filter', () => {
    store.setSearch('  Apfel  ')
    expect(store.search()).toBe('Apfel')

    store.setSearch('   ')
    expect(store.search()).toBeNull()
  })

  it('sets and clears the diet filter', () => {
    store.setDiet('VEGAN')
    expect(store.diet()).toBe('VEGAN')

    store.setDiet(null)
    expect(store.diet()).toBeNull()
  })

  it('starts a newly selected sort field ascending', () => {
    store.setSort('calory')

    expect(store.sort()).toBe('calory')
    expect(store.direction()).toBe('asc')
  })

  it('toggles direction when sorting by the already-active field again', () => {
    store.setSort('name')
    expect(store.sort()).toBe('name')
    expect(store.direction()).toBe('desc')

    store.setSort('name')
    expect(store.direction()).toBe('asc')
  })

  it('resets to ascending when switching to a different field', () => {
    store.setSort('name')
    expect(store.direction()).toBe('desc')

    store.setSort('grams')
    expect(store.sort()).toBe('grams')
    expect(store.direction()).toBe('asc')
  })
})
