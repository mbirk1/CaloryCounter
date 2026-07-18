import { Component, input, InputSignal } from '@angular/core'

@Component({
  selector: 'app-card',
  imports: [],
  templateUrl: './card.component.html',
})
export class CardComponent {
  title: InputSignal<string> = input<string>('')
  panelClass: InputSignal<string> = input<string>('bg-white rounded-xl p-5')
}
