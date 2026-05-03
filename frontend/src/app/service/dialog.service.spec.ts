import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { DialogService } from './dialog.service';

describe('DialogService (unit)', () => {

  let service: DialogService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DialogService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should show alert and emit dialog state', () => {
    let state: any;
    service.dialogState$.subscribe(s => state = s);

    service.alert('Test message');

    expect(state).toBeTruthy();
    expect(state.type).toBe('alert');
    expect(state.message).toBe('Test message');
    expect(state.confirmLabel).toBe('OK');
  });

  it('should clear alert automatically after timeout', fakeAsync(() => {
    let state: any;
    service.dialogState$.subscribe(s => state = s);

    service.alert('Auto dismiss');
    expect(state).not.toBeNull();

    tick(3500);

    expect(state).toBeNull();
  }));

  it('should clear previous timeout when alert is called again', fakeAsync(() => {
    let state: any;
    service.dialogState$.subscribe(s => state = s);

    service.alert('First');
    tick(1000);
    service.alert('Second');

    tick(3500);

    expect(state).toBeNull();
  }));

  it('should show confirm dialog with message', () => {
    let state: any;
    service.dialogState$.subscribe(s => state = s);
    const onConfirm = jasmine.createSpy('onConfirm');

    service.confirm('Are you sure?', onConfirm);

    expect(state).toBeTruthy();
    expect(state.type).toBe('confirm');
    expect(state.message).toBe('Are you sure?');
    expect(state.onConfirm).toBe(onConfirm);
  });

  it('should do nothing in accept when no dialog is shown', () => {
    let state: any = 'initial';
    service.dialogState$.subscribe(s => state = s);

    service.accept();

    expect(state).toBeNull();
  });

  it('should close dialog and call onConfirm when accepting a confirm dialog', () => {
    const onConfirm = jasmine.createSpy('onConfirm');
    service.confirm('Confirm?', onConfirm);
    let state: any;
    service.dialogState$.subscribe(s => state = s);

    service.accept();

    expect(state).toBeNull();
    expect(onConfirm).toHaveBeenCalled();
  });

  it('should close alert dialog without calling onConfirm when accepting', () => {
    const onConfirm = jasmine.createSpy('onConfirm');
    service.alert('Alert!');
    let state: any;
    service.dialogState$.subscribe(s => state = s);

    service.accept();

    expect(state).toBeNull();
    expect(onConfirm).not.toHaveBeenCalled();
  });

  it('should do nothing in cancel when no dialog is shown', () => {
    let state: any = 'initial';
    service.dialogState$.subscribe(s => state = s);

    service.cancel();

    expect(state).toBeNull();
  });

  it('should close dialog and call onCancel when cancelling a confirm dialog with onCancel', () => {
    const onConfirm = jasmine.createSpy('onConfirm');
    const onCancel = jasmine.createSpy('onCancel');
    service.confirm('Sure?', onConfirm, onCancel);
    let state: any;
    service.dialogState$.subscribe(s => state = s);

    service.cancel();

    expect(state).toBeNull();
    expect(onCancel).toHaveBeenCalled();
    expect(onConfirm).not.toHaveBeenCalled();
  });

  it('should close dialog without calling onCancel when cancelling a confirm without onCancel', () => {
    const onConfirm = jasmine.createSpy('onConfirm');
    service.confirm('Sure?', onConfirm);
    let state: any;
    service.dialogState$.subscribe(s => state = s);

    service.cancel();

    expect(state).toBeNull();
    expect(onConfirm).not.toHaveBeenCalled();
  });

});
