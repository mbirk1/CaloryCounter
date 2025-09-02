import { Component, input, InputSignal } from '@angular/core'

@Component({
  selector: 'app-checkbox',
  imports: [],
  templateUrl: './checkbox.component.html',
})
export class CheckboxComponent {
  text: InputSignal<string> = input.required<string>()
}
