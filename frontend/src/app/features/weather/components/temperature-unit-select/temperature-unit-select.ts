import { Component, model, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SelectChangeEvent, SelectModule } from 'primeng/select';

import { TemperatureUnit } from '../../models/temperature-unit.model';

interface TemperatureUnitOption {
  label: string;
  value: TemperatureUnit;
}

@Component({
  imports: [SelectModule, FormsModule],
  selector: 'app-temperature-unit-select',
  styleUrl: './temperature-unit-select.scss',
  templateUrl: './temperature-unit-select.html',
})
export class TemperatureUnitSelect {
  protected readonly options: TemperatureUnitOption[] = [
    { label: 'Celsius (°C)', value: 'G_CEL' },
    { label: 'Fahrenheit (°F)', value: 'G_FAH' },
  ];

  readonly value = model<TemperatureUnit>('G_CEL');
  readonly change = output<TemperatureUnit>();

  protected onChange(event: SelectChangeEvent): void {
    const value = (event.value ?? 'G_CEL') as TemperatureUnit;
    this.value.set(value);
    this.change.emit(value);
  }
}