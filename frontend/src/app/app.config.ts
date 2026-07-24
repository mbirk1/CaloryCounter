import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core'
import { provideRouter } from '@angular/router'

import { routes } from './app.routes'
import {
  provideHttpClient,
  withInterceptors,
  withXhr,
} from '@angular/common/http'
import { authInterceptor } from './api/interceptors/auth.interceptor'

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideHttpClient(withXhr()),
    provideRouter(routes),
  ],
}
