import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { AppComponent } from './app.component';
import { CheeseListComponent } from './components/cheese-list.component/cheese-list.component';

@NgModule({
  declarations: [],
  imports: [
    BrowserModule,
    HttpClientModule,
    NgbModule,
    AppComponent,
    CheeseListComponent
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
