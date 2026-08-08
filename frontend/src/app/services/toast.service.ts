import { Injectable, Signal, signal, WritableSignal } from '@angular/core'

export type ToastVariant = 'success' | 'error'

export interface Toast {
  id: number
  message: string
  variant: ToastVariant
}

const TOAST_DURATION_MS = 4000

@Injectable({
  providedIn: 'root',
})
export class ToastService {
  private readonly toastsSignal: WritableSignal<Toast[]> = signal([])
  private nextId = 0

  readonly toasts: Signal<Toast[]> = this.toastsSignal.asReadonly()

  success(message: string): void {
    this.show(message, 'success')
  }

  error(message: string): void {
    this.show(message, 'error')
  }

  dismiss(id: number): void {
    this.toastsSignal.update((toasts) =>
      toasts.filter((toast) => toast.id !== id),
    )
  }

  private show(message: string, variant: ToastVariant): void {
    const id = this.nextId++
    this.toastsSignal.update((toasts) => [...toasts, { id, message, variant }])
    setTimeout(() => this.dismiss(id), TOAST_DURATION_MS)
  }
}
