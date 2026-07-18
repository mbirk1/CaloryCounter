import { HttpClient } from '@angular/common/http'
import { Observable } from 'rxjs'
import { Injectable } from '@angular/core'

// Not generic at the class level - a single request can return a different shape than the
// resource type it's "about" (e.g. POST /auth/login sends a LoginRequest and returns an
// AuthResponse, GET /food returns a list while POST /food returns a single item). Callers pick
// the response type per call via an explicit type argument instead.
@Injectable({
  providedIn: 'root',
})
export class Gateway {
  constructor(private http: HttpClient) {}

  get<T>(url: string): Observable<T> {
    return this.http.get<T>(url, { withCredentials: true })
  }

  post<T>(url: string, body: unknown): Observable<T> {
    return this.http.post<T>(url, body, { withCredentials: true })
  }

  put<T>(url: string, body: unknown): Observable<T> {
    return this.http.put<T>(url, body, { withCredentials: true })
  }

  delete<T>(url: string, id: string): Observable<T> {
    return this.http.delete<T>(url + '/' + id, { withCredentials: true })
  }
}
