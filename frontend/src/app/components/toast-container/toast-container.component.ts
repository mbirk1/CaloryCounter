import { ChangeDetectionStrategy, Component, inject } from '@angular/core'
import { ToastService } from '../../services/toast.service'

@Component({
  selector: 'app-toast-container',
  imports: [],
  templateUrl: './toast-container.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: '',
})
export class ToastContainerComponent {
  protected readonly toastService = inject(ToastService)

  dismiss(id: number): void {
    this.toastService.dismiss(id)
  }
}
