import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Budget } from '../models/Budget.model';
import { Category } from '../models/Category.model';

const jsonPayloadHttpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
};

@Injectable({
  providedIn: 'root',
})
export class CategoriesService {
  constructor(private http: HttpClient) {}

  getAll() {
    return this.http.get<Category[]>(`/api/categories`);
  }

  create(category: Category): Observable<Category> {
    return this.http.post<Category>(
      `/api/categories`,
      { ...category },
      jsonPayloadHttpOptions
    );
  }

  delete(category: Category): Observable<Category> {
    return this.http.delete<Category>(`/api/categories/${category.id}`);
  }
}
