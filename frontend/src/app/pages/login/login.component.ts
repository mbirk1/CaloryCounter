import { Component, inject, signal, WritableSignal } from '@angular/core'
import { Router, RouterLink } from '@angular/router'
import { FormGroup, ReactiveFormsModule, Validators } from '@angular/forms'
import { HttpErrorResponse } from '@angular/common/http'
import { FormFactoryService } from '../../services/factory/form.factory'
import { AuthStore } from '../../api/stores/auth.store'
import { LogoComponent } from '../../components/logo/logo.component'
import { LabelComponent } from '../../components/label/label.component'
import { TextInputComponent } from '../../components/inputs/text-input/text-input.component'
import { PasswordInputComponent } from '../../components/inputs/password-input/password-input.component'
import { ButtonComponent } from '../../components/button/button.component'
import { AlertComponent } from '../../components/alert/alert.component'

@Component({
  selector: 'app-login',
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
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private readonly formFactory = inject(FormFactoryService)
  private readonly authStore = inject(AuthStore)
  private readonly router = inject(Router)

  form: FormGroup
  loading: WritableSignal<boolean> = signal(false)
  errorMessage: WritableSignal<string> = signal('')

  constructor() {
    this.form = this.formFactory
      .create()
      .control('email', '', [Validators.required, Validators.email])
      .control('password', '', [Validators.required])
      .build()
  }

  async onSubmit(): Promise<void> {
    if (this.form.invalid || this.loading()) {
      return
    }
    this.loading.set(true)
    this.errorMessage.set('')
    try {
      await this.authStore.login(this.form.value)
      await this.router.navigate([
        this.authStore.profileCompleted() ? '/dashboard' : '/onboarding',
      ])
    } catch (error) {
      if (error instanceof HttpErrorResponse && error.status === 401) {
        this.errorMessage.set('E-Mail oder Passwort ist falsch.')
      } else {
        this.errorMessage.set(
          'Anmeldung fehlgeschlagen. Bitte versuche es erneut.',
        )
      }
    } finally {
      this.loading.set(false)
    }
  }
}
