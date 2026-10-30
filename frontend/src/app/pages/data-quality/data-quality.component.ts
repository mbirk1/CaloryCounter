import {
  ChangeDetectionStrategy,
  Component,
  inject,
  signal,
  WritableSignal,
} from '@angular/core'
import { DecimalPipe } from '@angular/common'
import { ButtonComponent } from '../../components/button/button.component'
import { AlertComponent } from '../../components/alert/alert.component'
import { CardComponent } from '../../components/card/card.component'
import { DuplicateStore } from '../../api/stores/duplicate.store'
import { DuplicateClusterModel } from '../../models/DuplicateClusterModel'

interface SourceOption {
  value: string
  label: string
}

@Component({
  selector: 'app-data-quality',
  imports: [ButtonComponent, AlertComponent, CardComponent, DecimalPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './data-quality.component.html',
})
export class DataQualityComponent {
  protected readonly duplicateStore = inject(DuplicateStore)

  protected readonly sources: SourceOption[] = [
    { value: 'MANUAL', label: 'Manuell' },
    { value: 'OPENFOODFACTS', label: 'OpenFoodFacts' },
    { value: 'BLS', label: 'BLS' },
  ]

  // Per-cluster canonical selection, keyed by cluster id - a user's radio choice overrides the
  // suggested canonical member without mutating the cluster data itself.
  private readonly selectionsSignal: WritableSignal<Record<string, string>> =
    signal({})

  selectedCanonicalId(cluster: DuplicateClusterModel): string | undefined {
    const selected = this.selectionsSignal()[cluster.id]
    if (selected) {
      return selected
    }
    const suggested = cluster.members.find(
      (member) => member.suggestedCanonical,
    )
    return (suggested ?? cluster.members[0])?.food.uuid
  }

  selectCanonical(cluster: DuplicateClusterModel, foodId: string): void {
    this.selectionsSignal.update((selections) => ({
      ...selections,
      [cluster.id]: foodId,
    }))
  }

  analyze(source: string): void {
    void this.duplicateStore.analyze(source)
  }

  async merge(cluster: DuplicateClusterModel): Promise<void> {
    const canonicalId = this.selectedCanonicalId(cluster)
    if (!canonicalId) {
      return
    }
    await this.duplicateStore.merge(cluster.id, canonicalId)
  }

  async reject(cluster: DuplicateClusterModel): Promise<void> {
    await this.duplicateStore.reject(cluster.id)
  }
}
