import { fakeAsync, TestBed, tick } from '@angular/core/testing'
import { signal } from '@angular/core'
import { Dialog } from '@angular/cdk/dialog'
import { FoodComponent } from './food.component'
import { CsvImportProgressDialogComponent } from '../../components/dialogs/csv-import-progress-dialog/csv-import-progress-dialog.component'
import { CsvImportService } from '../../services/csv-import.service'
import { FoodStore } from '../../api/stores/food.store'

describe('FoodComponent', () => {
  let dialogSpy: jasmine.SpyObj<Dialog>
  let csvImportServiceSpy: jasmine.SpyObj<CsvImportService>
  let foodStoreSpy: jasmine.SpyObj<FoodStore>

  beforeEach(() => {
    dialogSpy = jasmine.createSpyObj<Dialog>('Dialog', ['open'])
    csvImportServiceSpy = jasmine.createSpyObj<CsvImportService>(
      'CsvImportService',
      ['upload'],
    )
    foodStoreSpy = jasmine.createSpyObj<FoodStore>('FoodStore', [
      'setSearch',
      'setDiet',
    ])
    Object.defineProperty(foodStoreSpy, 'diet', {
      value: signal(null),
      configurable: true,
    })

    TestBed.configureTestingModule({
      providers: [
        { provide: Dialog, useValue: dialogSpy },
        { provide: CsvImportService, useValue: csvImportServiceSpy },
        { provide: FoodStore, useValue: foodStoreSpy },
      ],
    })
  })

  function create(): FoodComponent {
    return TestBed.createComponent(FoodComponent).componentInstance
  }

  function fileInputEventFor(file: File | undefined): Event {
    const input = document.createElement('input')
    input.type = 'file'
    if (file) {
      const dataTransfer = new DataTransfer()
      dataTransfer.items.add(file)
      input.files = dataTransfer.files
    }
    return { target: input } as unknown as Event
  }

  it('starts the upload and opens the progress dialog with disableClose so an in-progress import cannot be dismissed', () => {
    const component = create()
    const file = new File(['code\tproduct_name\n'], 'import.csv', {
      type: 'text/csv',
    })

    component.onCsvFileSelected(fileInputEventFor(file))

    expect(csvImportServiceSpy.upload).toHaveBeenCalledWith(file)
    expect(dialogSpy.open).toHaveBeenCalledWith(
      CsvImportProgressDialogComponent,
      {
        disableClose: true,
      },
    )
  })

  it('does nothing when no file was selected', () => {
    const component = create()

    component.onCsvFileSelected(fileInputEventFor(undefined))

    expect(csvImportServiceSpy.upload).not.toHaveBeenCalled()
    expect(dialogSpy.open).not.toHaveBeenCalled()
  })

  it('debounces search input before calling the store', fakeAsync(() => {
    const component = create() as unknown as {
      searchControl: { setValue: (value: string) => void }
    }

    component.searchControl.setValue('Ap')
    component.searchControl.setValue('Apf')
    component.searchControl.setValue('Apfel')
    tick(299)
    expect(foodStoreSpy.setSearch).not.toHaveBeenCalled()

    tick(1)
    expect(foodStoreSpy.setSearch).toHaveBeenCalledTimes(1)
    expect(foodStoreSpy.setSearch).toHaveBeenCalledWith('Apfel')
  }))

  it('does not re-trigger the store for the same search value in a row', fakeAsync(() => {
    const component = create() as unknown as {
      searchControl: { setValue: (value: string) => void }
    }

    component.searchControl.setValue('Apfel')
    tick(300)
    component.searchControl.setValue('Apfel')
    tick(300)

    expect(foodStoreSpy.setSearch).toHaveBeenCalledTimes(1)
  }))

  it('forwards diet filter selections to the store', () => {
    const component = create()

    component.onDietFilterSelected('VEGAN')

    expect(foodStoreSpy.setDiet).toHaveBeenCalledWith('VEGAN')
  })

  it('reports which diet option is currently active based on the store', () => {
    Object.defineProperty(foodStoreSpy, 'diet', {
      value: signal('VEGAN'),
      configurable: true,
    })
    const component = create()

    expect(component.isDietActive('VEGAN')).toBeTrue()
    expect(component.isDietActive('VEGETARIAN')).toBeFalse()
    expect(component.isDietActive(null)).toBeFalse()
  })
})
