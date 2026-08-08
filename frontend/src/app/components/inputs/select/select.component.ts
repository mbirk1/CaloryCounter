import { Component, forwardRef, input, InputSignal } from '@angular/core'
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms'

export interface SelectOption {
  value: string
  label: string
}

@Component({
  selector: 'app-select',
  imports: [],
  templateUrl: './select.component.html',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => SelectComponent),
      multi: true,
    },
  ],
})
export class SelectComponent implements ControlValueAccessor {
  options: InputSignal<SelectOption[]> = input<SelectOption[]>([])
  placeholder: InputSignal<string> = input<string>('Bitte wählen...')

  value: string = ''
  disabled = false

  onChange = (value: string) => {}

  onTouched = () => {}

  onSelect(event: Event) {
    const select = event.target as HTMLSelectElement
    this.value = select.value
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
