import { Component, forwardRef, input, InputSignal } from '@angular/core'
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms'

@Component({
  selector: 'app-checkbox',
  imports: [],
  templateUrl: './checkbox.component.html',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => CheckboxComponent),
      multi: true,
    },
  ],
})
export class CheckboxComponent implements ControlValueAccessor {
  text: InputSignal<string> = input.required<string>()
  value = false
  private onTouched = () => {}
  private onChanged = (val: boolean) => {}

  writeValue(value: boolean): void {
    this.value = value
  }

  registerOnChange(fn: any): void {
    this.onChanged = fn
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn
  }

  onInputChange(event: Event): void {
    const checked = (event.target as HTMLInputElement).checked
    this.value = checked
    this.onChanged(checked)
    this.onTouched()
  }
}
