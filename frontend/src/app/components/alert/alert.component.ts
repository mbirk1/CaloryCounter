import { Component, input, InputSignal } from '@angular/core'

export type AlertVariant = 'error' | 'success' | 'info'

@Component({
  selector: 'app-alert',
  imports: [],
  templateUrl: './alert.component.html',
})
export class AlertComponent {
  message: InputSignal<string> = input.required<string>()
  variant: InputSignal<AlertVariant> = input<AlertVariant>('error')
}
