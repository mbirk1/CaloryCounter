import { Component, input, InputSignal } from '@angular/core'

@Component({
  selector: 'app-label',
  imports: [],
  templateUrl: './label.component.html',
  styleUrl: './label.component.css',
})
export class LabelComponent {
  public text: InputSignal<string> = input.required()
}
