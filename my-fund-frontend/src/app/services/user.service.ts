import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {User} from "../models/User.model";


@Injectable({
  providedIn: 'root',

})
export class UserService {

  constructor(private http: HttpClient) {
  }

  getCurrent(): Observable<User> {
    return this.http.get<User>(`/api/users/current-user`);
  }

}

