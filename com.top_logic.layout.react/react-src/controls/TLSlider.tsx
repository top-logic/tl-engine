import { React, useTLFieldValue, rootClassName, VALUE_DEBOUNCE_MS } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

/**
 * A number field rendered as a handle travelling along a track.
 *
 * The value is a number: the server sends the number itself in `state.value` and receives back the
 * number the handle stands on, between `state.min` and `state.max` and on the grid of `state.step`.
 * The text beside the handle is `state.valueLabel`, the value written by the server in the format
 * the field asks for, so the slider shows the digits and separators of the user's locale without
 * formatting anything itself.
 *
 * Dragging moves the handle at once - the local value is updated on every move - while the server
 * hears the value only once the drag settles: the send is debounced by `state.debounceMs`
 * (defaulting to VALUE_DEBOUNCE_MS) and flushed when the pointer or the key is released, so a drag
 * across the track is one round-trip rather than one per pixel. The text follows a moment behind,
 * being the server's answer to the value it was given.
 *
 * A field holding no value puts the handle at the lower bound and shows no text, so an empty value
 * is not read as the smallest one.
 */
const TLSlider: React.FC<TLCellProps> = ({ controlId, state }) => {
  const [value, setValue, flushValue] = useTLFieldValue({
    debounceMs: (state.debounceMs as number) ?? VALUE_DEBOUNCE_MS,
  });

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      setValue(Number(e.target.value));
    },
    [setValue]
  );

  const handleCommit = useCallback(() => { void flushValue(); }, [flushValue]);

  const min = (state.min as number) ?? 0;
  const max = (state.max as number) ?? 100;
  const step = (state.step as number) ?? 1;
  const label = (state.valueLabel as string) ?? '';
  const number = typeof value === 'number' ? value : null;

  if (state.editable === false) {
    return (
      <span id={controlId} className={rootClassName(state, 'tlSlider tlSlider--immutable')}>
        {label}
      </span>
    );
  }

  const hasError = state.hasError === true;
  const hasWarnings = state.hasWarnings === true;
  const errorMessage = state.errorMessage as string | undefined;

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
        onChange={handleChange}
        onPointerUp={handleCommit}
        onKeyUp={handleCommit}
        onBlur={handleCommit}
        disabled={state.disabled === true}
        aria-valuetext={label || undefined}
        aria-invalid={hasError || undefined}
        title={hasError && errorMessage ? errorMessage : undefined}
      />
      <output className="tlSlider__value">{label}</output>
    </span>
  );
};

export default TLSlider;
