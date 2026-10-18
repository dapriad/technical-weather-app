import { Routes } from '@angular/router';
import { WeatherPage } from './features/weather/pages/weather-page/weather-page';

export const routes: Routes = [
  { path: '', component: WeatherPage },
  { path: '**', redirectTo: '' },
];