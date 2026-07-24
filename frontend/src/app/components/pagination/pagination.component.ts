import {
  ChangeDetectionStrategy,
  Component,
  input,
  InputSignal,
  output,
  OutputEmitterRef,
} from '@angular/core'

@Component({
  selector: 'app-pagination',
  imports: [],
  templateUrl: './pagination.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: '',
})
export class PaginationComponent {
  page: InputSignal<number> = input.required<number>()
  totalPages: InputSignal<number> = input.required<number>()

  previous: OutputEmitterRef<void> = output<void>()
  next: OutputEmitterRef<void> = output<void>()

  onPrevious(): void {
    this.previous.emit()
  }

  onNext(): void {
    this.next.emit()
  }
}
