import { TestBed } from '@angular/core/testing';

import { Forecast } from '../../models/forecast.model';
import { ForecastCard } from './forecast-card';

describe('ForecastCard', () => {
  const forecast: Forecast = {
    averageTemperature: 73,
    temperatureUnit: 'G_FAH',
    precipitationProbability: [
      { probability: 5, period: '00-06' },
      { probability: 10, period: '06-12' },
    ],
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ForecastCard],
    }).compileComponents();
  });

  it('renders municipality, temperature and precipitation', () => {
    const fixture = TestBed.createComponent(ForecastCard);

    fixture.componentRef.setInput('municipalityName', 'Madrid');
    fixture.componentRef.setInput('dateLabel', 'Miércoles, 21 de octubre de 2026');
    fixture.componentRef.setInput('forecast', forecast);
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Madrid');
    expect(text).toContain('73');
    expect(text).toContain('°F');
    expect(text).toContain('Temperatura media');
    expect(text).toContain('Probabilidad de lluvia');
    expect(text).toContain('Madrugada');
    expect(text).toContain('00–06 h');
    expect(text).toContain('10%');
    expect(text).toContain('Datos de AEMET');
    expect(text).not.toContain('No hay datos de precipitación disponibles');
  });

  it('shows an empty state when precipitation data is unavailable', () => {
    const fixture = TestBed.createComponent(ForecastCard);

    fixture.componentRef.setInput('municipalityName', 'Lugo');
    fixture.componentRef.setInput('dateLabel', 'Jueves, 22 de octubre de 2026');
    fixture.componentRef.setInput('forecast', {
      averageTemperature: 11,
      temperatureUnit: 'G_CEL',
      precipitationProbability: [],
    } satisfies Forecast);
    fixture.detectChanges();

    const element: HTMLElement = fixture.nativeElement;
    expect(element.textContent).toContain(
      'No hay datos de precipitación disponibles para esta franja horaria.',
    );
    expect(element.querySelectorAll('.precipitation-item')).toHaveLength(0);
  });
});
