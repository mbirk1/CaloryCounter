import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  InputSignal,
  Signal,
} from '@angular/core'
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome'
import { IconDefinition } from '@fortawesome/fontawesome-svg-core'
import {
  faDrumstickBite,
  faLeaf,
  faSeedling,
} from '@fortawesome/free-solid-svg-icons'

interface DietPresentation {
  icon: IconDefinition
  label: string
  colorClass: string
}

// UNKNOWN/missing deliberately renders nothing rather than a dedicated "unknown" icon - most
// imported legacy foods have no diet data at all, and flagging that on every one of them would
// be more noise than signal.
const DIET_PRESENTATIONS: Record<string, DietPresentation> = {
  VEGAN: { icon: faLeaf, label: 'Vegan', colorClass: 'text-green-600' },
  VEGETARIAN: {
    icon: faSeedling,
    label: 'Vegetarisch',
    colorClass: 'text-lime-600',
  },
  NON_VEGETARIAN: {
    icon: faDrumstickBite,
    label: 'Nicht vegetarisch',
    colorClass: 'text-gray-400',
  },
}

@Component({
  selector: 'app-diet-badge',
  imports: [FontAwesomeModule],
  templateUrl: './diet-badge.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: '',
})
export class DietBadgeComponent {
  diet: InputSignal<string | undefined> = input<string | undefined>()

  protected readonly presentation: Signal<DietPresentation | undefined> =
    computed(() => {
      const value = this.diet()
      return value ? DIET_PRESENTATIONS[value] : undefined
    })
}
