import { TestBed } from '@angular/core/testing'
import { DialogRef, DIALOG_DATA } from '@angular/cdk/dialog'
import {
  ConfirmDialogComponent,
  ConfirmDialogData,
} from './confirm-dialog.component'

describe('ConfirmDialogComponent', () => {
  let dialogRefSpy: jasmine.SpyObj<DialogRef<boolean>>

  function create(data: ConfirmDialogData) {
    dialogRefSpy = jasmine.createSpyObj<DialogRef<boolean>>('DialogRef', [
      'close',
    ])

    TestBed.configureTestingModule({
      imports: [ConfirmDialogComponent],
      providers: [
        { provide: DialogRef, useValue: dialogRefSpy },
        { provide: DIALOG_DATA, useValue: data },
      ],
    })

    const fixture = TestBed.createComponent(ConfirmDialogComponent)
    fixture.detectChanges()
    return fixture
  }

  it('shows the given title and message', () => {
    const fixture = create({
      title: 'Lebensmittel löschen',
      message: 'Möchtest du "Tofu" wirklich löschen?',
    })

    const text = (fixture.nativeElement as HTMLElement).textContent ?? ''
    expect(text).toContain('Lebensmittel löschen')
    expect(text).toContain('Möchtest du "Tofu" wirklich löschen?')
  })

  it('falls back to default button labels when none are given', () => {
    const fixture = create({ title: 'Titel', message: 'Nachricht' })

    const text = (fixture.nativeElement as HTMLElement).textContent ?? ''
    expect(text).toContain('Bestätigen')
    expect(text).toContain('Abbrechen')
  })

  it('closes with true when the confirm button is clicked', () => {
    const fixture = create({
      title: 'Titel',
      message: 'Nachricht',
      confirmText: 'Löschen',
    })

    const buttons = (
      fixture.nativeElement as HTMLElement
    ).querySelectorAll<HTMLButtonElement>('button')
    const confirmButton = Array.from(buttons).find((button) =>
      (button.textContent ?? '').includes('Löschen'),
    )
    confirmButton?.click()

    expect(dialogRefSpy.close).toHaveBeenCalledWith(true)
  })

  it('closes with false when the cancel button is clicked', () => {
    const fixture = create({ title: 'Titel', message: 'Nachricht' })

    const buttons = (
      fixture.nativeElement as HTMLElement
    ).querySelectorAll<HTMLButtonElement>('button')
    const cancelButton = Array.from(buttons).find((button) =>
      (button.textContent ?? '').includes('Abbrechen'),
    )
    cancelButton?.click()

    expect(dialogRefSpy.close).toHaveBeenCalledWith(false)
  })
})
