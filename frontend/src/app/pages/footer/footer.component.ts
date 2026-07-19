import { Component, ChangeDetectionStrategy } from '@angular/core'

@Component({
  selector: 'app-footer',
  imports: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './footer.component.html',
})
export class FooterComponent {
  year = new Date().getFullYear()
}
