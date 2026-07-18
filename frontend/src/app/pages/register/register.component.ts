import { Component, signal, WritableSignal } from '@angular/core'
import { Router, RouterLink } from '@angular/router'
import {
  AbstractControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms'
import { HttpErrorResponse } from '@angular/common/http'
import { FormFactoryService } from '../../services/factory/form.factory'
import { AuthStore } from '../../api/stores/auth.store'
import { LogoComponent } from '../../components/logo/logo.component'
import { LabelComponent } from '../../components/label/label.component'
import { TextInputComponent } from '../../components/inputs/text-input/text-input.component'
import { PasswordInputComponent } from '../../components/inputs/password-input/password-input.component'
import { ButtonComponent } from '../../components/button/button.component'
import { AlertComponent } from '../../components/alert/alert.component'

function passwordsMatchValidator(
  control: AbstractControl,
): ValidationErrors | null {
  const password = control.get('password')?.value
  const confirmPassword = control.get('confirmPassword')?.value
  return password === confirmPassword ? null : { passwordMismatch: true }
}

@Component({
  selector: 'app-register',
  imports: [
    RouterLink,
    ReactiveFormsModule,
    LogoComponent,
    LabelComponent,
    TextInputComponent,
    PasswordInputComponent,
    ButtonComponent,
    AlertComponent,
  ],
  templateUrl: './register.component.html',
})
export class RegisterComponent {
  form: FormGroup
  loading: WritableSignal<boolean> = signal(false)
  errorMessage: WritableSignal<string> = signal('')

  constructor(
    private formFactory: FormFactoryService,
    private authStore: AuthStore,
    private router: Router,
  ) {
    this.form = this.formFactory
      .create()
      .control('email', '', [Validators.required, Validators.email])
      .control('password', '', [Validators.required, Validators.minLength(8)])
      .control('confirmPassword', '', [Validators.required])
      .build({ validators: [passwordsMatchValidator] })
  }

  get passwordStrength(): number {
    const value: string = this.form.get('password')?.value ?? ''
    let score = 0
    if (value.length >= 8) {
      score++
    }
    if (/[A-Za-z]/.test(value) && /[0-9]/.test(value)) {
      score++
    }
    if (value.length >= 12) {
      score++
    }
    return score
  }

  async onSubmit(): Promise<void> {
    if (this.form.invalid || this.loading()) {
      return
    }
    this.loading.set(true)
    this.errorMessage.set('')
    try {
      await this.authStore.register({
        email: this.form.value.email,
        password: this.form.value.password,
      })
      await this.router.navigate(['/onboarding'])
    } catch (error) {
      if (error instanceof HttpErrorResponse && error.status === 409) {
        this.errorMessage.set('Diese E-Mail wird bereits verwendet.')
      } else {
        this.errorMessage.set(
          'Registrierung fehlgeschlagen. Bitte versuche es erneut.',
        )
      }
    } finally {
      this.loading.set(false)
    }
  }
}
