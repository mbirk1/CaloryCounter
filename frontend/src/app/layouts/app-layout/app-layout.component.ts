import { Component } from '@angular/core'
import { RouterOutlet } from '@angular/router'
import { HeaderComponent } from '../../pages/header/header.component'
import { FooterComponent } from '../../pages/footer/footer.component'

@Component({
  selector: 'app-app-layout',
  imports: [RouterOutlet, HeaderComponent, FooterComponent],
  templateUrl: './app-layout.component.html',
})
export class AppLayoutComponent {}
