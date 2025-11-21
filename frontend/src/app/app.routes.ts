import { Routes } from '@angular/router';
import { CheeseListComponent } from './components/cheese-list.component/cheese-list.component';
import { LoginComponent } from './components/log-in/login.component';
import { RegisterComponent } from './components/register/register.component';

export const routes: Routes = [
  { path: '', redirectTo: '/cheeses', pathMatch: 'full' },
  { path: 'cheeses', component: CheeseListComponent },
  { path: 'auth/login', component: LoginComponent },
  { path: 'auth/register', component: RegisterComponent },
];
