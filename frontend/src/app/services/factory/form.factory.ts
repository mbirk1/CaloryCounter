import { Injectable, inject } from '@angular/core'
import {
  AbstractControlOptions,
  FormBuilder,
  FormGroup,
  ValidatorFn,
} from '@angular/forms'

@Injectable({
  providedIn: 'root',
})
export class FormFactoryService {
  private readonly fb = inject(FormBuilder)

  create(): DynamicFormBuilder {
    return new DynamicFormBuilder(this.fb)
  }
}

export class DynamicFormBuilder {
  private controls: { [key: string]: [any, ValidatorFn[]?] } = {}

  constructor(private fb: FormBuilder) {}

  control(
    name: string,
    defaultValue: any = '',
    validators: ValidatorFn[] = [],
  ): this {
    this.controls[name] = [defaultValue, validators]
    return this
  }

  build(options?: AbstractControlOptions): FormGroup {
    return this.fb.group(this.controls, options)
  }
}
