import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http'
import { inject } from '@angular/core'
import { Router } from '@angular/router'
import { catchError, from, switchMap, throwError } from 'rxjs'
import { AuthStore } from '../stores/auth.store'

// Attaches the in-memory access token to every request. On a 401 from a non-auth endpoint it
// tries exactly one silent refresh (the refresh token cookie is sent automatically) and replays
// the original request - if that also fails, the session is gone and the user is sent to /login.
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const authStore = inject(AuthStore)
  const router = inject(Router)

  const withToken = (token: string | null) =>
    token
      ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : request

  return next(withToken(authStore.accessToken())).pipe(
    catchError((error: unknown) => {
      const isUnauthorized =
        error instanceof HttpErrorResponse && error.status === 401
      const isAuthRequest = request.url.includes('/auth/')
      if (!isUnauthorized || isAuthRequest) {
        return throwError(() => error)
      }

      return from(authStore.refresh()).pipe(
        switchMap((refreshed) => {
          if (!refreshed) {
            void router.navigate(['/login'])
            return throwError(() => error)
          }
          return next(withToken(authStore.accessToken()))
        }),
      )
    }),
  )
}
