import {
  Component,
  input,
  InputSignal,
  ChangeDetectionStrategy,
} from '@angular/core'

@Component({
  selector: 'app-label',
  imports: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './label.component.html',
})
export class LabelComponent {
  public text: InputSignal<string> = input.required()
}
