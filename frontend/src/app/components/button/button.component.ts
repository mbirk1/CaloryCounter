import {
  Component,
  input,
  InputSignal,
  ChangeDetectionStrategy,
} from '@angular/core'

@Component({
  selector: 'app-button',
  imports: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './button.component.html',
})
export class ButtonComponent {
  text: InputSignal<string> = input<string>('')
  style: InputSignal<string> = input<string>(
    'bg-button-primary hover:bg-button-primary-hover text-white rounded-lg p-2',
  )
  type: InputSignal<string> = input.required<string>()
  disabled: InputSignal<boolean> = input<boolean>(false)
  loading: InputSignal<boolean> = input<boolean>(false)
}
