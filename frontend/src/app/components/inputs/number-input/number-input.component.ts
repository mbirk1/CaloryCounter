import {
  ChangeDetectorRef,
  Component,
  forwardRef,
  inject,
  Input,
  ChangeDetectionStrategy,
} from '@angular/core'
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms'

@Component({
  selector: 'app-number-input',
  imports: [],
  templateUrl: './number-input.component.html',
  styles: '',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => NumberInputComponent),
      multi: true,
    },
  ],
})
export class NumberInputComponent implements ControlValueAccessor {
  private readonly changeDetectorRef = inject(ChangeDetectorRef)

  @Input() placeholder: string = ''
  @Input() type: string = 'text'

  value: string = ''
  disabled = false

  onChange = (value: string) => {}
  onTouched = () => {}

  onInput(event: Event) {
    const input = event.target as HTMLInputElement
    this.value = input.value
    this.onChange(this.value)
    this.onTouched()
  }

  writeValue(value: string): void {
    this.value = value
    this.changeDetectorRef.markForCheck()
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled
  }
}
