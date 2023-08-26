import { Component } from '@angular/core';

import { TranslateService } from '@ngx-translate/core';
import { AuthenticationService } from './service/authentication.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'Scrabble by Gamecity';

  constructor(
    private translateService: TranslateService,
    private authenticationService: AuthenticationService
  ) {
    const preferredLanguage = this.authenticationService.getPreferredLanguage();
    if (preferredLanguage !== null) {
      this.translateService.use(preferredLanguage);
    } else {
      translateService.setDefaultLang('en');
      translateService.use('en');
    }
  }

}
