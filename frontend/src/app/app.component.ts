import { Component } from '@angular/core';
import { CheeseListComponent } from './components/cheese-list.component';


@Component({
  selector: 'app-root',
  imports: [CheeseListComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'frontend';
}
