import { Component, input, InputSignal } from '@angular/core'

@Component({
  selector: 'app-button',
  imports: [],
  templateUrl: './button.component.html',
})
export class ButtonComponent {
  text: InputSignal<string> = input<string>('')
  style: InputSignal<string> = input<string>(
    'bg-button-primary hover:bg-button-primary-hover text-white rounded-lg p-2',
  )
  type: InputSignal<string> = input.required<string>()
}
