import {
  computed,
  inject,
  Injectable,
  Signal,
  signal,
  WritableSignal,
} from '@angular/core'
import { HttpClient } from '@angular/common/http'
import { firstValueFrom } from 'rxjs'
import { Gateway } from '../gateways/gateway'
import { API_ENDPOINTS } from '../../../environment/endpoints'
import {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  UpdateProfileRequest,
} from '../../models/AuthModels'
import { UserModel } from '../../models/UserModel'

// Holds the access token only in memory (a plain signal, never localStorage/sessionStorage) to
// keep it out of reach of XSS attacks. The refresh token lives in an HttpOnly cookie the backend
// sets and is never touched from here. On a full page reload the token is gone, which is why
// bootstrap() silently tries a refresh before any guard decides on the auth state.
@Injectable({ providedIn: 'root' })
export class AuthStore {
  private readonly accessTokenSignal: WritableSignal<string | null> =
    signal(null)
  private readonly userSignal: WritableSignal<UserModel | null> = signal(null)
  private readonly initializedSignal: WritableSignal<boolean> = signal(false)
  private bootstrapPromise: Promise<void> | null = null

  readonly accessToken: Signal<string | null> = computed(() =>
    this.accessTokenSignal(),
  )
  readonly user: Signal<UserModel | null> = computed(() => this.userSignal())
  readonly isAuthenticated: Signal<boolean> = computed(
    () => this.accessTokenSignal() !== null,
  )
  readonly profileCompleted: Signal<boolean> = computed(
    () => this.userSignal()?.profileCompleted ?? false,
  )

  private readonly http = inject(HttpClient)
  private readonly gateway = inject<Gateway<unknown>>(Gateway)

  async register(request: RegisterRequest): Promise<void> {
    const response = await firstValueFrom(
      this.gateway.post<AuthResponse>(API_ENDPOINTS.auth.register, request),
    )
    this.accessTokenSignal.set(response.accessToken)
    await this.loadCurrentUser()
  }

  async login(request: LoginRequest): Promise<void> {
    const response = await firstValueFrom(
      this.gateway.post<AuthResponse>(API_ENDPOINTS.auth.login, request),
    )
    this.accessTokenSignal.set(response.accessToken)
    await this.loadCurrentUser()
  }

  async logout(): Promise<void> {
    try {
      await firstValueFrom(
        this.http.post<void>(API_ENDPOINTS.auth.logout, null, {
          withCredentials: true,
        }),
      )
    } finally {
      this.accessTokenSignal.set(null)
      this.userSignal.set(null)
    }
  }

  async refresh(): Promise<boolean> {
    try {
      const response = await firstValueFrom(
        this.http.post<AuthResponse>(API_ENDPOINTS.auth.refresh, null, {
          withCredentials: true,
        }),
      )
      this.accessTokenSignal.set(response.accessToken)
      return true
    } catch {
      this.accessTokenSignal.set(null)
      this.userSignal.set(null)
      return false
    }
  }

  async updateProfile(request: UpdateProfileRequest): Promise<UserModel> {
    const user = await firstValueFrom(
      this.gateway.put<UserModel>(API_ENDPOINTS.users.profile, request),
    )
    this.userSignal.set(user)
    return user
  }

  // Called once per app load (from the route guards) to restore the session from the refresh
  // cookie before the first navigation decision is made. Safe to call repeatedly, including
  // concurrently: Angular runs multiple canActivate guards on the same route in parallel rather
  // than sequentially, so authGuard and profileCompleteGuard can both call this at the same
  // instant - the in-flight promise is shared so only one refresh/me round trip ever happens.
  bootstrap(): Promise<void> {
    if (this.initializedSignal()) {
      return Promise.resolve()
    }
    if (!this.bootstrapPromise) {
      this.bootstrapPromise = this.performBootstrap()
    }
    return this.bootstrapPromise
  }

  private async performBootstrap(): Promise<void> {
    const refreshed = await this.refresh()
    if (refreshed) {
      await this.loadCurrentUser()
    }
    this.initializedSignal.set(true)
  }

  private async loadCurrentUser(): Promise<void> {
    const user = await firstValueFrom(
      this.gateway.get<UserModel>(API_ENDPOINTS.users.me),
    )
    this.userSignal.set(user)
  }
}
