import { Component } from '@angular/core'
import { RouterLink } from '@angular/router'
import { LogoComponent } from '../../components/logo/logo.component'
import { CardComponent } from '../../components/card/card.component'

@Component({
  selector: 'app-landing',
  imports: [RouterLink, LogoComponent, CardComponent],
  templateUrl: './landing.component.html',
})
export class LandingComponent {
  features = [
    {
      icon: 'restaurant',
      title: 'Lebensmittel- & Rezept-Tracking',
      description:
        'Erfasse deine Mahlzeiten in Sekunden und lege eigene Rezepte an.',
    },
    {
      icon: 'monitoring',
      title: 'Individuelles Dashboard',
      description:
        'Alle deine Fortschritte auf einen Blick, abgestimmt auf dein Aktivitätslevel.',
    },
    {
      icon: 'database',
      title: 'Zentrale Datenbank',
      description:
        'Greife auf eine gemeinsame Lebensmittel- und Rezeptdatenbank zu.',
    },
  ]
}
