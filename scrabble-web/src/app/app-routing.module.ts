import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './auth.guard';

import { LoginComponent } from './login/login.component';
import { CreateGameComponent } from './create-game/create-game.component';
import { GamesComponent } from './games/games.component';
import { MyGamesComponent } from './my-games/my-games.component';
import { GameLoungeComponent } from "./game-lounge/game-lounge.component";
import { GameComponent } from './game/game.component';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: 'games', component: GamesComponent,
    canActivate: [AuthGuard],
    data: {
      role: 'ROLE_USER'
    },
  },
  {
    path: 'my-games', component: MyGamesComponent,
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
    path: 'lounges/:id', component: GameLoungeComponent,
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
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
