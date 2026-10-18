import { TestBed } from '@angular/core/testing';

import { Municipality } from '../../models/municipality.model';
import { MunicipalityAutocomplete } from './municipality-autocomplete';

describe('MunicipalityAutocomplete', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MunicipalityAutocomplete],
    }).compileComponents();
  });

  it('creates the component', () => {
    const fixture = TestBed.createComponent(MunicipalityAutocomplete);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('emits the selected municipality', () => {
    const fixture = TestBed.createComponent(MunicipalityAutocomplete);
    const component = fixture.componentInstance;
    const selected: Municipality = { id: '28079', name: 'Madrid' };

    const spy = vi.fn();
    component.selected.subscribe(spy);

    (component as any).onSelect({ originalEvent: null, value: selected });

    expect(spy).toHaveBeenCalledWith(selected);
  });

  it('emits typing when the user edits the input', () => {
    const fixture = TestBed.createComponent(MunicipalityAutocomplete);
    const component = fixture.componentInstance;
    const spy = vi.fn();

    component.typing.subscribe(spy);
    (component as any).onKeyDown(new KeyboardEvent('keydown', { key: 'a' }));

    expect(spy).toHaveBeenCalled();
  });

  it('does not emit typing when confirming with Enter', () => {
    const fixture = TestBed.createComponent(MunicipalityAutocomplete);
    const component = fixture.componentInstance;
    const spy = vi.fn();

    component.typing.subscribe(spy);
    (component as any).onKeyDown(new KeyboardEvent('keydown', { key: 'Enter' }));

    expect(spy).not.toHaveBeenCalled();
  });

  it('does not emit typing when navigating with arrows', () => {
    const fixture = TestBed.createComponent(MunicipalityAutocomplete);
    const component = fixture.componentInstance;
    const spy = vi.fn();

    component.typing.subscribe(spy);
    (component as any).onKeyDown(new KeyboardEvent('keydown', { key: 'ArrowDown' }));

    expect(spy).not.toHaveBeenCalled();
  });
});