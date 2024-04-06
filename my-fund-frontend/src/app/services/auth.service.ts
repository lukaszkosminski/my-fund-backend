import {Injectable} from "@angular/core";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs";

const API = '/api';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root',
})
export class AuthService {


  constructor(private http: HttpClient) {}


  register(username: string, email: string, password: string): Observable<any> {
    return this.http.post(`${API}/create-user`,
      {
        username,
        email,
        password,
      },
      httpOptions
    );
  }
}
