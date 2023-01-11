import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { AuthenticationService } from '../service/authentication.service';
import { ToastService } from '../service/toast.service';

@Injectable()
export class CustomHttpInterceptor implements HttpInterceptor {
  constructor(
    private authenticationService: AuthenticationService,
    private toastService: ToastService,
    private translateService: TranslateService,
    ) { }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (this.authenticationService.isAuthenticated() && this.authenticationService.getToken()) {
      request = request.clone({
        headers: new HttpHeaders({
          'Authorization': this.authenticationService.getToken()
        })
      });
    }
    return next.handle(request).pipe(
      catchError(response => {
        if (response instanceof HttpErrorResponse) {
          if (response.status === 401) {
            this.authenticationService.logout();
          } else if (response.status === 500) {
              if (response.error.code === 2012) {
                  // error 2012 has the language parameter which needs to be translated
                  this.toastService.error(this.translateService.instant('error.' + response.error.code,
                      { 0: response.error.params[0], 1: this.translateService.instant('game.bag.language.' + response.error.params[1]) }));
              } else {
                  this.toastService.error(this.translateService.instant('error.' + response.error.code,
                      response.error.params));
              }
          } else {
            this.toastService.error(response.error);
          }
        } else {
          this.toastService.error(response.error);
        }
        return throwError(response);
      })
    );;
  }
}