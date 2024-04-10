import {Injectable} from "@angular/core";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs";
import {Router} from "@angular/router";


const jsonPayloadHttpOptions = {
  headers: new HttpHeaders({'Content-Type': 'application/json'})
};

@Injectable({
  providedIn: 'root',

})
export class AuthService {
  constructor(private http: HttpClient, private router: Router) {
  }

  register(username: string, email: string, password: string): Observable<any> {
    return this.http.post(`/register`,
      {
        username,
        email,
        password,
      },
      jsonPayloadHttpOptions
    );
  }

  login(email: string, password: string): Observable<any> {
    const formData = new FormData();
    formData.append('username', email);
    formData.append('password', password);

    return this.http.post(`/signin`,
      formData,
    );
  }

  logout() {
    return this.http.post(`/logout`, {}).subscribe({
      next: () => {
      this.router.navigate(['/'])
      },
      error: (response) => {
        console.log(26, response.error)
      }
    });
  }

}
