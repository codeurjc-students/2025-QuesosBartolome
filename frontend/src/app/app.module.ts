import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule} from '@angular/common/http';
import { AppComponent } from './app.component';
import { CheeseListComponent } from './components/cheese-list.component';

@NgModule({
  imports: [BrowserModule, HttpClientModule,AppComponent, CheeseListComponent],
  bootstrap: [AppComponent],
})
export class AppModule {}
