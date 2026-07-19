import { HttpClient } from '@angular/common/http'
import { Observable } from 'rxjs'
import { Injectable, inject } from '@angular/core'

@Injectable({
  providedIn: 'root',
})
export class Gateway<T> {
  private readonly http = inject(HttpClient)

  get<T>(url: string): Observable<T> {
    return this.http.get<T>(url)
  }

  post<T>(url: string, body: T): Observable<T> {
    return this.http.post<T>(url, body)
  }

  delete<T>(url: string, id: string): Observable<T> {
    return this.http.delete<T>(url + '/' + id)
  }
}
