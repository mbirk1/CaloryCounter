import { HttpClient } from '@angular/common/http'
import { Observable } from 'rxjs'
import { Injectable } from '@angular/core'

@Injectable({
  providedIn: 'root',
})
export class Gateway<T> {
  constructor(private http: HttpClient) {}

  async get<T>(url: string): Promise<Observable<T>> {
    return this.http.get<T>(url)
  }

  async post<T>(url: string, body: T): Promise<Observable<T>> {
    return this.http.post<T>(url, body)
  }
}
