import { React, useTLState, useTLFieldValue, rootClassName, VALUE_DEBOUNCE_MS, useFieldLabelProps, fieldInputId } from 'tl-react-bridge';
import type { TLCellProps, SliderStateJson } from 'tl-react-bridge';
import { showsValueOnly } from './form/fieldState';

const { useCallback } = React;

/**
 * The custom property on the input holding the share of the range the value covers, a number from 0
 * to 1; the track draws the value part up to it.
 */
const FILL_PROPERTY = '--tlSlider-fill';

/**
 * The share of the range from min to max that the value covers, kept within 0 and 1. An empty
 * range covers nothing.
 */
function fillFraction(value: number, min: number, max: number): number {
  if (!(max > min) || !Number.isFinite(value)) {
    return 0;
  }
  return Math.min(1, Math.max(0, (value - min) / (max - min)));
}

/**
 * A number field rendered as a handle travelling along a track.
 *
 * The state is described by SliderStateJson.
 *
 * The value is a number: the server sends the number itself in `state.value` and receives back the
 * number the handle stands on, between `state.min` and `state.max` and on the grid of `state.step`.
 * The text beside the handle is `state.valueLabel`, the value written by the server in the format
 * the field asks for, so the slider shows the digits and separators of the user's locale without
 * formatting anything itself.
 *
 * The text takes the width of the wider of the bounds, `state.minLabel` and `state.maxLabel`
 * written in the same format: both stand invisibly in the same grid cell as the value, so the box
 * is as wide as the widest of the three and the track keeps its length while the value changes.
 *
 * Dragging moves the handle at once - the local value is updated on every move - while the server
 * hears the value only once the drag settles: the send is debounced by `state.debounceMs`
 * (defaulting to VALUE_DEBOUNCE_MS) and flushed when the pointer or the key is released, so a drag
 * across the track is one round-trip rather than one per pixel. The text follows a moment behind,
 * being the server's answer to the value it was given.
 *
 * The track is filled from the lower bound up to the handle; the control writes the share of the
 * range the value covers on the input (FILL_PROPERTY), from which the stylesheet sizes that part.
 *
 * A field holding no value puts the handle at the lower bound and shows no text, so an empty value
 * is not read as the smallest one.
 *
 * A read-only field shows the text only; a disabled field renders the track with its handle as an
 * inactive input (native `disabled`, see showsValueOnly).
 */
const TLSlider: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<SliderStateJson>>();
  const inputId = fieldInputId(controlId);
  const labelProps = useFieldLabelProps(controlId, inputId);
  const [value, setValue, flushValue] = useTLFieldValue({
    debounceMs: state.debounceMs ?? VALUE_DEBOUNCE_MS,
  });

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      setValue(Number(e.target.value));
    },
    [setValue]
  );

  const handleCommit = useCallback(() => { void flushValue(); }, [flushValue]);

  const min = state.min ?? 0;
  const max = state.max ?? 100;
  const step = state.step ?? 1;
  const label = state.valueLabel ?? '';
  const minLabel = state.minLabel ?? '';
  const maxLabel = state.maxLabel ?? '';
  const number = typeof value === 'number' ? value : null;
  const fillStyle = { [FILL_PROPERTY]: String(fillFraction(number ?? min, min, max)) } as React.CSSProperties;

  if (showsValueOnly(state)) {
    return (
      <span id={controlId} className={rootClassName(state, 'tlSlider tlSlider--immutable')}>
        {label}
      </span>
    );
  }

  const hasError = state.hasError === true;
  const hasWarnings = state.hasWarnings === true;
  const errorMessage = state.errorMessage;

  return (
    <span
      id={controlId}
      className={rootClassName(
        state,
        'tlSlider',
        hasError && 'tlSlider--error',
        !hasError && hasWarnings && 'tlSlider--warning'
      )}
    >
      <input
        className="tlSlider__input"
        type="range"
        min={min}
        max={max}
        step={step}
        value={number ?? min}
        style={fillStyle}
        onChange={handleChange}
        onPointerUp={handleCommit}
        onKeyUp={handleCommit}
        onBlur={handleCommit}
        disabled={state.disabled === true}
        aria-valuetext={label || undefined}
        aria-invalid={hasError || undefined}
        title={hasError && errorMessage ? errorMessage : undefined}
        id={inputId}
        {...labelProps}
      />
      <output className="tlSlider__value">
        <span className="tlSlider__valueText">{label}</span>
        <span className="tlSlider__valueSizer" aria-hidden="true">{minLabel}</span>
        <span className="tlSlider__valueSizer" aria-hidden="true">{maxLabel}</span>
      </output>
    </span>
  );
};

export default TLSlider;
