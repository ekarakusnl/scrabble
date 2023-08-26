import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './auth.guard';

import { LoginComponent } from './login/login.component';
import { CreateGameComponent } from './create-game/create-game.component';
import { LobbyComponent } from './lobby/lobby.component';
import { LoungeComponent } from './lounge/lounge.component';
import { GameComponent } from './game/game.component';
import { UserProfileComponent } from './user-profile/user-profile.component';

const routes: Routes = [
  { path: '', component: LobbyComponent },
  { path: 'login', component: LoginComponent },
  {
    path: 'lobby', component: LobbyComponent,
    canActivate: [AuthGuard],
    data: {
      role: 'ROLE_USER'
    },
  },
  {
    path: 'lounge', component: LoungeComponent,
    canActivate: [AuthGuard],
    data: {
      role: 'ROLE_USER'
    },
  },
  {
    path: 'create-game', component: CreateGameComponent,
    canActivate: [AuthGuard],
    data: {
      role: 'ROLE_USER'
    },
  },
  {
    path: 'games/:id', component: GameComponent,
    canActivate: [AuthGuard],
    data: {
      role: 'ROLE_USER'
    },
  },
  {
    path: 'user-profile', component: UserProfileComponent,
    canActivate: [AuthGuard],
    data: {
      role: 'ROLE_USER'
    },
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
