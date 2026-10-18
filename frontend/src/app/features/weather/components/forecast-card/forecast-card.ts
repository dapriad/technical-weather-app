import { Component, computed, input } from '@angular/core';
import { CardModule } from 'primeng/card';
import { ProgressBarModule } from 'primeng/progressbar';

import { Forecast, PrecipitationProbability } from '../../models/forecast.model';

const PERIOD_LABELS: Record<string, string> = {
  '00-06': 'Madrugada',
  '06-12': 'Mañana',
  '12-18': 'Tarde',
  '18-24': 'Noche',
};

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

  protected periodLabel(period: string): string {
    return PERIOD_LABELS[period] ?? period;
  }

  protected periodRange(period: string): string {
    const [start, end] = period.split('-');
    return start && end ? `${start}–${end} h` : period;
  }

  protected precipitationLabel(item: PrecipitationProbability): string {
    const period = this.periodLabel(item.period).toLowerCase();
    return `Probabilidad de lluvia del ${item.probability}% en la ${period} (${this.periodRange(item.period)})`;
  }
}