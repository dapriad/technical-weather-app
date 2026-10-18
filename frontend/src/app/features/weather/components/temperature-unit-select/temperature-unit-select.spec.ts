import { TestBed } from '@angular/core/testing';

import { TemperatureUnitSelect } from './temperature-unit-select';

describe('TemperatureUnitSelect', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TemperatureUnitSelect],
    }).compileComponents();
  });

  it('creates the component', () => {
    const fixture = TestBed.createComponent(TemperatureUnitSelect);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('defaults to Celsius', () => {
    const fixture = TestBed.createComponent(TemperatureUnitSelect);
    expect(fixture.componentInstance.value()).toBe('G_CEL');
  });

  it('emits the selected unit', () => {
    const fixture = TestBed.createComponent(TemperatureUnitSelect);
    const component = fixture.componentInstance;
    const spy = vi.fn();

    component.change.subscribe(spy);
    (component as any).onChange({ originalEvent: null, value: 'G_FAH' });

    expect(spy).toHaveBeenCalledWith('G_FAH');
    expect(component.value()).toBe('G_FAH');
  });

  it('resets to Celsius when the value is cleared', () => {
    const fixture = TestBed.createComponent(TemperatureUnitSelect);
    const component = fixture.componentInstance;
    const spy = vi.fn();

    component.change.subscribe(spy);
    (component as any).onChange({ originalEvent: null, value: null });

    expect(spy).toHaveBeenCalledWith('G_CEL');
    expect(component.value()).toBe('G_CEL');
  });
});