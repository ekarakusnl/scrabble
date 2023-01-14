import { Component } from '@angular/core';

import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'Scrabble by Gamecity';

  constructor(
    private translateService: TranslateService,
  ) {
    const language = localStorage.getItem('locale') || 'en';
    translateService.setDefaultLang('en');
    translateService.use(language);
  }

}
