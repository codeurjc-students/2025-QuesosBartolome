import { Routes } from '@angular/router';
import { CheeseListComponent } from './components/cheese-list.component/cheese-list.component';
import { LoginComponent } from './components/log-in/login.component';
import { RegisterComponent } from './components/register/register.component';
import { UserPageComponent } from './components/userPage/userPage.component';
import { AboutUsComponent } from './components/about-Us/aboutUs.component';
import { CheeseDetailsComponent } from './components/cheese-details/cheese-details.component';
import { MyOrderComponent } from './components/myOrder/myOrder.component';
import { OrdersComponent } from './components/orders/orders.component';
import { ClientsComponent } from './components/clients/clients.component';
import { ErrorPageComponent } from './components/errorPage/errorPage.component';

export const routes: Routes = [
  { path: '', redirectTo: '/cheeses', pathMatch: 'full' },
  { path: 'cheeses', component: CheeseListComponent },
  { path: 'auth/login', component: LoginComponent },
  { path: 'auth/register', component: RegisterComponent },
  { path: 'user', component: UserPageComponent },
  { path: 'about-us', component: AboutUsComponent },
  { path: 'cheeses/:id', component: CheeseDetailsComponent },
  { path: 'myorder', component: MyOrderComponent },
  { path: 'orders', component: OrdersComponent },
  { path: 'users', component: ClientsComponent },
  { path: 'error', component: ErrorPageComponent },
  { path: '**', component: ErrorPageComponent }
];
