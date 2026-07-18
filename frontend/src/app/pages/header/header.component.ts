import { Component, inject, signal, WritableSignal } from '@angular/core'
import { Router, RouterLink, RouterLinkActive } from '@angular/router'
import { AuthStore } from '../../api/stores/auth.store'
import { LogoComponent } from '../../components/logo/logo.component'

@Component({
  selector: 'app-header',
  imports: [RouterLink, RouterLinkActive, LogoComponent],
  templateUrl: './header.component.html',
  styles: '',
})
export class HeaderComponent {
  authStore = inject(AuthStore)
  menuOpen: WritableSignal<boolean> = signal(false)

  constructor(private router: Router) {}

  toggleMenu(): void {
    this.menuOpen.update((open) => !open)
  }

  closeMenu(): void {
    this.menuOpen.set(false)
  }

  async logout(): Promise<void> {
    this.closeMenu()
    await this.authStore.logout()
    await this.router.navigate(['/login'])
  }
}
