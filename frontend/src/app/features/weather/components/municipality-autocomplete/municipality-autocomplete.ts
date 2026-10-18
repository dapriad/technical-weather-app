import { Component, input, model, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AutoCompleteCompleteEvent, AutoCompleteSelectEvent } from 'primeng/autocomplete';
import { AutoCompleteModule } from 'primeng/autocomplete';

import { Municipality } from '../../models/municipality.model';

const NON_TYPING_KEYS = new Set([
  'Enter',
  'Escape',
  'Tab',
  'ArrowUp',
  'ArrowDown',
  'ArrowLeft',
  'ArrowRight',
  'Shift',
  'Control',
  'Alt',
  'Meta',
]);

@Component({
  imports: [AutoCompleteModule, FormsModule],
  selector: 'app-municipality-autocomplete',
  styleUrl: './municipality-autocomplete.scss',
  templateUrl: './municipality-autocomplete.html',
})
export class MunicipalityAutocomplete {
  readonly suggestions = input<Municipality[]>([]);
  readonly value = model<string | null>(null);

  readonly search = output<string>();
  readonly selected = output<Municipality>();
  readonly cleared = output<void>();
  readonly typing = output<void>();

  protected onSearch(event: AutoCompleteCompleteEvent): void {
    this.search.emit(event.query);
  }

  protected onSelect(event: AutoCompleteSelectEvent): void {
    this.selected.emit(event.value as Municipality);
  }

  protected onClear(): void {
    this.cleared.emit();
  }

  protected onKeyDown(event: KeyboardEvent): void {
    if (NON_TYPING_KEYS.has(event.key)) {
      return;
    }

    this.typing.emit();
  }
}