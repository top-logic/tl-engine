// The wire contract of TLSlider, checked against the MUI adapter.

import { describe, it, expect, vi, afterEach, beforeEach } from 'vitest';
import { screen, cleanup, fireEvent } from '@testing-library/react';
import { CMD_VALUE_CHANGED, VALUE_DEBOUNCE_MS, fieldLabel } from 'tl-react-bridge';
import type { SliderStateJson } from 'tl-react-bridge';
import MuiSliderAdapter from './MuiSliderAdapter';
import { CONTROL_ID, fakeDebounceTimers, mountAdapter, settle } from './wire-test-support';
import type { MountOptions } from './wire-test-support';

/** The argument of {@link CMD_VALUE_CHANGED} holding the value. */
const ARG_VALUE = 'value';

/** The ID of the range input inside the control. */
const INPUT_ID = CONTROL_ID + '-input';

/** The ID of the form field around the slider. */
const FIELD_ID = 'f1';

/** The width of the track in the tests, in pixels: one pixel per value from 0 to 100. */
const TRACK_WIDTH = 100;

function mountSlider(state: Partial<SliderStateJson>, options?: MountOptions) {
  return mountAdapter(MuiSliderAdapter, { value: 20, valueLabel: '20 %', ...state }, options);
}

function changedTo(value: number) {
  return [CMD_VALUE_CHANGED, { [ARG_VALUE]: value }];
}

function slider(): HTMLInputElement {
  return screen.getByRole('slider') as HTMLInputElement;
}

/**
 * The root of the MUI slider, laid out as a track of {@link TRACK_WIDTH} pixels, with the pointer
 * capture jsdom does not implement.
 */
function track(): HTMLElement {
  const root = document.querySelector('.MuiSlider-root') as HTMLElement;
  root.setPointerCapture = () => {};
  root.releasePointerCapture = () => {};
  root.hasPointerCapture = () => false;
  root.getBoundingClientRect = () => ({
    left: 0, top: 0, right: TRACK_WIDTH, bottom: 10, width: TRACK_WIDTH, height: 10, x: 0, y: 0, toJSON() {},
  });
  return root;
}

/**
 * The pointer events the MUI slider drags its handle with, which jsdom does not implement: a mouse
 * event with the ID and the type of its pointer.
 */
class TestPointerEvent extends MouseEvent {
  readonly pointerId: number;
  readonly pointerType: string;

  constructor(type: string, init: PointerEventInit = {}) {
    super(type, init);
    this.pointerId = init.pointerId ?? 0;
    this.pointerType = init.pointerType ?? 'mouse';
  }
}

beforeEach(() => {
  vi.stubGlobal('PointerEvent', TestPointerEvent);
});

afterEach(() => {
  cleanup();
  vi.useRealTimers();
  vi.unstubAllGlobals();
});

describe('TLSlider as MUI Slider', () => {
  it('renders an MUI slider of the range of the state, labelled by the form field', () => {
    const setInputId = vi.fn();
    mountSlider({ min: 10, max: 50, step: 5, cssClass: 'my-slider' },
      { fieldLabel: fieldLabel(FIELD_ID, CONTROL_ID, setInputId) });

    const input = slider();
    expect(input.id).toBe(INPUT_ID);
    expect(input.min).toBe('10');
    expect(input.max).toBe('50');
    expect(input.step).toBe('5');
    expect(input.value).toBe('20');
    expect(input.getAttribute('aria-valuetext')).toBe('20 %');
    expect(input.getAttribute('aria-labelledby')).toBe(FIELD_ID + '-label');
    expect(setInputId).toHaveBeenCalledWith(INPUT_ID);
    expect(input.closest('.MuiSlider-root')!.classList).toContain('my-slider');
    expect(document.getElementById(CONTROL_ID)!.querySelector('output')!.textContent).toBe('20 %');
  });

  it('sends a dragged value after the debounce, not before', async () => {
    fakeDebounceTimers();
    const sent = mountSlider({});

    fireEvent.pointerDown(track(), { pointerId: 1, button: 0, clientX: 30, clientY: 5 });
    expect(slider().value).toBe('30');
    vi.advanceTimersByTime(VALUE_DEBOUNCE_MS - 1);
    await settle();
    expect(sent).not.toHaveBeenCalled();

    vi.advanceTimersByTime(1);
    await settle();
    expect(sent.mock.calls).toEqual([changedTo(30)]);
  });

  it('sends the value at once when the handle is released, and only once', async () => {
    fakeDebounceTimers();
    const sent = mountSlider({ debounceMs: 1000 });

    fireEvent.pointerDown(track(), { pointerId: 1, button: 0, clientX: 40, clientY: 5 });
    fireEvent.pointerUp(document, { pointerId: 1, button: 0, clientX: 40, clientY: 5 });
    await settle();
    expect(sent.mock.calls).toEqual([changedTo(40)]);

    vi.advanceTimersByTime(1000);
    await settle();
    expect(sent.mock.calls).toEqual([changedTo(40)]);
  });

  it('sends a value changed by key at once', async () => {
    const sent = mountSlider({ step: 10 });

    fireEvent.change(slider(), { target: { value: '30' } });
    await settle();

    expect(sent.mock.calls).toEqual([changedTo(30)]);
  });

  it('puts the handle at the lower bound without a value, showing no text', () => {
    mountSlider({ value: null, valueLabel: undefined, min: 5 });

    expect(slider().value).toBe('5');
    expect(document.getElementById(CONTROL_ID)!.querySelector('output')).toBeNull();
  });

  it('marks an invalid value', () => {
    mountSlider({ hasError: true, errorMessage: 'Zu hoch', mandatory: true });

    expect(slider().getAttribute('aria-invalid')).toBe('true');
    expect(slider().getAttribute('aria-required')).toBe('true');
    expect(slider().closest('.MuiSlider-root')!.classList).toContain('MuiSlider-colorError');
  });

  it('shows the value label as text while read-only', () => {
    mountSlider({ editable: false });

    expect(screen.queryByRole('slider')).toBeNull();
    expect(document.getElementById(CONTROL_ID)!.textContent).toBe('20 %');
  });

  it('accepts no input and sends nothing while disabled', async () => {
    const sent = mountSlider({ editable: false, disabled: true });

    expect(slider().disabled).toBe(true);
    fireEvent.pointerDown(track(), { pointerId: 1, button: 0, clientX: 70, clientY: 5 });
    fireEvent.pointerUp(document, { pointerId: 1, button: 0, clientX: 70, clientY: 5 });
    await new Promise(resolve => setTimeout(resolve, VALUE_DEBOUNCE_MS + 50));
    await settle();

    expect(slider().value).toBe('20');
    expect(sent).not.toHaveBeenCalled();
  });

  it('renders nothing while hidden', () => {
    mountSlider({ hidden: true });

    expect(document.getElementById(CONTROL_ID)).toBeNull();
  });
});
