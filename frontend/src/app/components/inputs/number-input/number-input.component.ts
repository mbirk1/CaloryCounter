import { Component, forwardRef, Input } from '@angular/core'
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms'

@Component({
  selector: 'app-number-input',
  imports: [],
  templateUrl: './number-input.component.html',
  styles: '',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => NumberInputComponent),
      multi: true,
    },
  ],
})
export class NumberInputComponent implements ControlValueAccessor {
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
