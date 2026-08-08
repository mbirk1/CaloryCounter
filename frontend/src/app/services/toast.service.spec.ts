import { fakeAsync, tick } from '@angular/core/testing'
import { ToastService } from './toast.service'

describe('ToastService', () => {
  it('adds a success toast', () => {
    const service = new ToastService()

    service.success('Lebensmittel wurde erfolgreich gelöscht.')

    expect(service.toasts()).toHaveSize(1)
    expect(service.toasts()[0].message).toBe(
      'Lebensmittel wurde erfolgreich gelöscht.',
    )
    expect(service.toasts()[0].variant).toBe('success')
  })

  it('adds an error toast', () => {
    const service = new ToastService()

    service.error('Löschen fehlgeschlagen. Bitte versuche es erneut.')

    expect(service.toasts()).toHaveSize(1)
    expect(service.toasts()[0].variant).toBe('error')
  })

  it('assigns each toast a distinct id', () => {
    const service = new ToastService()

    service.success('Erste Meldung')
    service.success('Zweite Meldung')

    const ids = service.toasts().map((toast) => toast.id)
    expect(new Set(ids).size).toBe(2)
  })

  it('removes a toast automatically after 4 seconds', fakeAsync(() => {
    const service = new ToastService()

    service.success('Verschwindet gleich')
    expect(service.toasts()).toHaveSize(1)

    tick(4000)

    expect(service.toasts()).toHaveSize(0)
  }))

  it('does not remove other toasts before their own timeout', fakeAsync(() => {
    const service = new ToastService()

    service.success('Erste Meldung')
    tick(2000)
    service.success('Zweite Meldung')
    tick(2000)

    // the first toast's 4s have elapsed, the second's 2s have not
    expect(service.toasts()).toHaveSize(1)
    expect(service.toasts()[0].message).toBe('Zweite Meldung')

    tick(2000)
    expect(service.toasts()).toHaveSize(0)
  }))

  it('can dismiss a toast manually', () => {
    const service = new ToastService()
    service.success('Meldung')
    const id = service.toasts()[0].id

    service.dismiss(id)

    expect(service.toasts()).toHaveSize(0)
  })
})
