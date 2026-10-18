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

  it('emits the selected unit', () => {
    const fixture = TestBed.createComponent(TemperatureUnitSelect);
    const component = fixture.componentInstance;
    const spy = vi.fn();

    component.change.subscribe(spy);
    (component as any).onChange({ originalEvent: null, value: 'G_FAH' });

    expect(spy).toHaveBeenCalledWith('G_FAH');
    expect(component.value()).toBe('G_FAH');
  });
});