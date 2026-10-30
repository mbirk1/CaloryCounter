import {
  computed,
  inject,
  Injectable,
  resource,
  ResourceRef,
  Signal,
} from '@angular/core'
import { HttpErrorResponse } from '@angular/common/http'
import { Gateway } from '../gateways/gateway'
import { firstValueFrom } from 'rxjs'
import { API_ENDPOINTS } from '../../../environment/endpoints'
import { ToastService } from '../../services/toast.service'
import { CreateRecipePayload, RecipeModel } from '../../models/RecipeModel'

@Injectable({
  providedIn: 'root',
})
export class RecipeStore {
  private readonly recipeGateway = inject<Gateway<RecipeModel>>(Gateway)
  private readonly toastService = inject(ToastService)

  recipeResource: ResourceRef<RecipeModel[] | undefined> = resource({
    loader: async (): Promise<RecipeModel[]> =>
      firstValueFrom(
        this.recipeGateway.get<RecipeModel[]>(API_ENDPOINTS.recipe),
      ),
  })

  readonly recipes: Signal<RecipeModel[]> = computed((): RecipeModel[] => {
    return this.recipeResource.value() ?? []
  })

  // Returns whether the creation succeeded, so the add dialog knows whether to close itself
  // (success) or stay open with the entered values intact (failure) - mirrors FoodStore.save().
  async save(payload: CreateRecipePayload): Promise<boolean> {
    try {
      await firstValueFrom(
        this.recipeGateway.post<RecipeModel>(API_ENDPOINTS.recipe, payload),
      )
      this.toastService.success('Rezept wurde erfolgreich angelegt.')
      this.recipeResource.reload()
      return true
    } catch (error) {
      this.toastService.error(
        this.mapErrorToMessage(
          error,
          'Anlegen fehlgeschlagen. Bitte versuche es erneut.',
          'Ein referenziertes Lebensmittel wurde nicht gefunden.',
        ),
      )
      return false
    }
  }

  public delete(id: string): void {
    firstValueFrom(
      this.recipeGateway.delete<RecipeModel>(API_ENDPOINTS.recipe, id),
    )
      .then(() => {
        this.toastService.success('Rezept wurde erfolgreich gelöscht.')
        this.recipeResource.reload()
      })
      .catch((error: unknown) => {
        this.toastService.error(
          this.mapErrorToMessage(
            error,
            'Löschen fehlgeschlagen. Bitte versuche es erneut.',
            'Dieses Rezept wurde bereits gelöscht.',
          ),
        )
      })
  }

  // A 404 means something different depending on the caller - a missing referenced food on
  // save() vs. an already-deleted recipe on delete() - so the message is passed in per call
  // rather than hardcoded here, unlike FoodStore's mapErrorToMessage (which only ever sees a
  // 404 from one call site).
  private mapErrorToMessage(
    error: unknown,
    genericMessage: string,
    notFoundMessage: string,
  ): string {
    if (!(error instanceof HttpErrorResponse)) {
      return genericMessage
    }
    switch (error.status) {
      case 404:
        return notFoundMessage
      case 406:
        return 'Bitte überprüfe deine Eingaben.'
      case 0:
        return 'Verbindung zum Server fehlgeschlagen. Bitte versuche es erneut.'
      default:
        return genericMessage
    }
  }
}
