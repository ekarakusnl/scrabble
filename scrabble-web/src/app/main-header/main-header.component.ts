import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { AuthenticationService } from '../service/authentication.service';

@Component({
  selector: 'app-main-header',
  templateUrl: './main-header.component.html',
  styleUrls: ['./main-header.component.css']
})
export class MainHeaderComponent implements OnInit {

  userId: number;

  constructor(
    private authenticationService: AuthenticationService,
    private translateService: TranslateService,
  ) { }

  ngOnInit(): void {
    this.userId = this.authenticationService.getUserId();
  }

  logout(): void {
    this.authenticationService.logout();
  }

  getLanguage() {
    const language = this.translateService.currentLang;
    return language == 'en' ? 'us' : language;
  }

  setLanguage(language: string) {
    this.translateService.use(language);
  }

}
