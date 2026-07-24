import { Component } from '@angular/core'
import { RouterLink, RouterOutlet } from '@angular/router'
import { LogoComponent } from '../../components/logo/logo.component'
import { FooterComponent } from '../../pages/footer/footer.component'

@Component({
  selector: 'app-public-layout',
  imports: [RouterLink, RouterOutlet, LogoComponent, FooterComponent],
  templateUrl: './public-layout.component.html',
})
export class PublicLayoutComponent {}
