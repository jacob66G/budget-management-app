import { Component } from '@angular/core';
import {MatIconModule} from '@angular/material/icon';
import {MatListModule} from '@angular/material/list';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-side-nav-bar',
  imports: [MatIconModule, MatListModule, RouterLink],
  templateUrl: './side-nav-bar.component.html',
  styleUrl: './side-nav-bar.component.css'
})
export class SideNavBarComponent {

}
