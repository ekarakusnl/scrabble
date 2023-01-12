import { Component, OnInit } from '@angular/core';
import { Globals } from '../common/globals';
import { AuthenticationService } from '../service/authentication.service';

@Component({
  selector: 'app-main-sidebar',
  templateUrl: './main-sidebar.component.html',
  styleUrls: ['./main-sidebar.component.css']
})
export class MainSidebarComponent implements OnInit {

  imageResourceURL: string = Globals.GATEWAY_URL;

  userId: number;
  username: string;

  constructor(
    private authenticationService: AuthenticationService
  ) { }

  ngOnInit(): void {
    this.userId = this.authenticationService.getUserId();
    this.username = this.authenticationService.getUsername();
  }

}
