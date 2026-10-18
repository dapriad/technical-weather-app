import { Component, computed, input } from '@angular/core';
import { CardModule } from 'primeng/card';
import { ProgressBarModule } from 'primeng/progressbar';

import { Forecast } from '../../models/forecast.model';

@Component({
  imports: [CardModule, ProgressBarModule],
  selector: 'app-forecast-card',
  styleUrl: './forecast-card.scss',
  templateUrl: './forecast-card.html',
})
export class ForecastCard {
  readonly municipalityName = input.required<string>();
  readonly dateLabel = input.required<string>();
  readonly forecast = input.required<Forecast>();

  readonly unitLabel = computed(() =>
    this.forecast().temperatureUnit === 'G_FAH' ? '°F' : '°C',
  );
}