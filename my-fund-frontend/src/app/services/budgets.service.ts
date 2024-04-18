import {Injectable} from "@angular/core";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs";
import {Budget} from "../models/Budget.model";

const jsonPayloadHttpOptions = {
  headers: new HttpHeaders({'Content-Type': 'application/json'})
};

@Injectable({
  providedIn: 'root',

})
export class BudgetsService {

  constructor(private http: HttpClient) {
  }

  getAll() {
    return this.http.get<Budget[]>(`/api/budgets`);
  }

  create(budget: Budget): Observable<Budget> {
    return this.http.post<Budget>(`/api/budgets/create-budget`, {...budget}, jsonPayloadHttpOptions);
  }
}

