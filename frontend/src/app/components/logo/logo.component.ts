import { Component, input, InputSignal } from '@angular/core'

@Component({
  selector: 'app-logo',
  imports: [],
  templateUrl: './logo.component.html',
})
export class LogoComponent {
  size: InputSignal<number> = input<number>(48)
}
