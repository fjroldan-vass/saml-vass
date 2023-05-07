import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiToken } from './api-token';
import { firstValueFrom, lastValueFrom } from 'rxjs';


@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  apiToken!: string;
  logoutSuccess!: boolean
  apiResult!: string;
  showUnauthorizedMessage!: boolean;

  constructor(private httpClient: HttpClient) {
  }

  ngOnInit(): void {
    const request = this.httpClient.get('http://localhost:8443/auth/token');
    firstValueFrom(request).then(
      (r: any) => {
        // Handle success
        debugger
        this.handleTokenSuccess(r);
      },
      (error) => {
        debugger
        // Handle error
        this.handleTokenError(error);
      }
    );
  }

  handleTokenSuccess(apiToken: ApiToken) {
    this.apiToken = apiToken.token;
    localStorage.setItem("apiToken", apiToken.token);
    this.callApi();
  }

  handleTokenError(error: HttpErrorResponse) {

    if (error.status === 401) {
      this.showUnauthorizedMessage = true;
      setTimeout(() => window.location.replace('http://localhost:8443/saml/login'), 4000);
    }
  }

  callApi() {
    const apiToken = localStorage.getItem("apiToken");

    this.httpClient.get('/service/api/mycontroller/', {
      headers: {
        "x-auth-token": apiToken ? apiToken : ''
      }
    }).subscribe(r => this.apiResult = JSON.stringify(r));
  }

  logout() {
    console.log('logout');
    localStorage.removeItem('apiToken');
    this.httpClient.get('service/saml/logout').subscribe(() => this.logoutSuccess = true);
  }

}
