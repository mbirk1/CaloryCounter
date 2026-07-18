import { Component, signal, WritableSignal } from '@angular/core'
import { Router } from '@angular/router'
import { FormGroup, ReactiveFormsModule, Validators } from '@angular/forms'
import { FormFactoryService } from '../../services/factory/form.factory'
import { AuthStore } from '../../api/stores/auth.store'
import { LabelComponent } from '../../components/label/label.component'
import { NumberInputComponent } from '../../components/inputs/number-input/number-input.component'
import {
  SelectComponent,
  SelectOption,
} from '../../components/inputs/select/select.component'
import { ButtonComponent } from '../../components/button/button.component'
import { AlertComponent } from '../../components/alert/alert.component'

@Component({
  selector: 'app-onboarding',
  imports: [
    ReactiveFormsModule,
    LabelComponent,
    NumberInputComponent,
    SelectComponent,
    ButtonComponent,
    AlertComponent,
  ],
  templateUrl: './onboarding.component.html',
})
export class OnboardingComponent {
  form: FormGroup
  loading: WritableSignal<boolean> = signal(false)
  errorMessage: WritableSignal<string> = signal('')

  activityLevels: SelectOption[] = [
    { value: 'SEDENTARY', label: 'Sitzend' },
    { value: 'LIGHTLY_ACTIVE', label: 'Leicht aktiv' },
    { value: 'MODERATELY_ACTIVE', label: 'Mäßig aktiv' },
    { value: 'VERY_ACTIVE', label: 'Sehr aktiv' },
    { value: 'EXTRA_ACTIVE', label: 'Extrem aktiv' },
  ]

  constructor(
    private formFactory: FormFactoryService,
    private authStore: AuthStore,
    private router: Router,
  ) {
    this.form = this.formFactory
      .create()
      .control('height', '', [Validators.required, Validators.min(1)])
      .control('weight', '', [Validators.required, Validators.min(1)])
      .control('activityLevel', '', [Validators.required])
      .build()
  }

  async onSubmit(): Promise<void> {
    if (this.form.invalid || this.loading()) {
      return
    }
    this.loading.set(true)
    this.errorMessage.set('')
    try {
      await this.authStore.updateProfile({
        height: Number(this.form.value.height),
        weight: Number(this.form.value.weight),
        activityLevel: this.form.value.activityLevel,
      })
      await this.router.navigate(['/dashboard'])
    } catch {
      this.errorMessage.set(
        'Profil konnte nicht gespeichert werden. Bitte versuche es erneut.',
      )
    } finally {
      this.loading.set(false)
    }
  }
}
