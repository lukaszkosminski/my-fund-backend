import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {IAppVersion} from "../models/App.model";

@Injectable({
  providedIn: 'root',
})
export class AppService {
  constructor(private http: HttpClient) {
  }

  getVersion() {
    return this.http.get<IAppVersion>(`version`);
  }
}

