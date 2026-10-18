import { TemperatureUnit } from './temperature-unit.model';

export interface PrecipitationProbability {
  probability: number;
  period: string;
}

export interface Forecast {
  averageTemperature: number;
  temperatureUnit: TemperatureUnit;
  precipitationProbability: PrecipitationProbability[];
}