import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { MatSidenavModule } from '@angular/material/sidenav';
import { RouterOutlet } from '@angular/router';
import { Header } from "../header/header";
import { SideNavBarComponent } from "../sidebar/side-nav-bar/side-nav-bar.component";
import { Footer } from "../footer/footer";

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    MatSidenavModule,
    Header,
    SideNavBarComponent,
    Footer
],
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.scss'
})
export class MainLayout {

}
