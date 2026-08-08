import {
  Component,
  forwardRef,
  input,
  InputSignal,
  signal,
  WritableSignal,
} from '@angular/core'
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms'

@Component({
  selector: 'app-password-input',
  imports: [],
  templateUrl: './password-input.component.html',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => PasswordInputComponent),
      multi: true,
    },
  ],
})
export class PasswordInputComponent implements ControlValueAccessor {
  placeholder: InputSignal<string> = input<string>('')

  value: string = ''
  disabled = false
  visible: WritableSignal<boolean> = signal(false)

  onChange = (value: string) => {}

  onTouched = () => {}

  toggleVisibility(): void {
    this.visible.update((current) => !current)
  }

  onInput(event: Event) {
    const input = event.target as HTMLInputElement
    this.value = input.value
    this.onChange(this.value)
    this.onTouched()
  }

  writeValue(value: string): void {
    this.value = value ?? ''
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
