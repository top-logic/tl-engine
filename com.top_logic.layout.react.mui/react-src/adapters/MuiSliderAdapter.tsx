import {
  React, useTLState, useTLFieldValue, useFieldLabelProps, fieldInputId, rootClassName, tooltipProps,
  VALUE_DEBOUNCE_MS,
} from 'tl-react-bridge';
import type { TLCellProps, SliderStateJson } from 'tl-react-bridge';
import Slider from '@mui/material/Slider';
import Box from '@mui/material/Box';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import type { SxProps, Theme } from '@mui/material/styles';
import { MuiFieldValue, fieldAriaProps, showsValueOnly } from './field';

const { useCallback } = React;

/** The smallest value of a slider whose state names none. */
const DEFAULT_MIN = 0;

/** The largest value of a slider whose state names none. */
const DEFAULT_MAX = 100;

/** The distance between two values of a slider whose state names none. */
const DEFAULT_STEP = 1;

/** The track takes the width the text beside it leaves. */
const SLIDER_SX: SxProps<Theme> = { flex: '1 1 auto' };

/**
 * The text beside the track keeps its width and shows its digits in columns of equal width. The
 * value and the two bounds stand in the same grid cell, so the box is as wide as the widest of them.
 */
const VALUE_SX: SxProps<Theme> = { flexShrink: 0, display: 'inline-grid', fontVariantNumeric: 'tabular-nums' };

/** A text in the grid cell of {@link VALUE_SX}, at its end. */
const VALUE_PART_SX: SxProps<Theme> = { gridArea: '1 / 1', justifySelf: 'end' };

/** A bound written only to give the box beside the track its width. */
const VALUE_SIZER_SX: SxProps<Theme> = { gridArea: '1 / 1', justifySelf: 'end', visibility: 'hidden' };

/**
 * Renders the state of a TopLogic slider (module name `TLSlider`) with the MUI `Slider`.
 *
 * <p>The value is a number. Moving the handle shows the new value at once and sends it as
 * `valueChanged` after `debounceMs` (default {@link VALUE_DEBOUNCE_MS}); releasing the handle
 * (`onChangeCommitted`, also after a change by key) or leaving the slider sends a value still held
 * back at once - as TLSlider does, a drag across the track is one round trip. Mapping from the
 * control state:</p>
 * <ul>
 * <li>value → `value`; no value puts the handle at the lower bound and shows no text;</li>
 * <li>min, max, step → `min`, `max`, `step` (defaults 0, 100, 1);</li>
 * <li>valueLabel → the value label of the handle (`valueLabelDisplay="auto"`, shown while the
 *     handle is hovered or moved), the text beside the track and the `aria-valuetext`: the value
 *     the server wrote in the format of the field;</li>
 * <li>minLabel, maxLabel → invisible texts in the same grid cell as the text beside the track, so
 *     that it takes the width of the wider bound and the track keeps its length while the value
 *     changes, as with TLSlider;</li>
 * <li>hasError → color `error`, `aria-invalid` and the error message as tooltip; hasWarnings →
 *     color `warning`; mandatory → `aria-required`;</li>
 * <li>a field that is not editable shows the value label as text, as TLSlider does; disabled →
 *     `disabled`;</li>
 * <li>hidden → nothing is rendered; the configured CSS class → className of the `Slider`.</li>
 * </ul>
 *
 * <p>The control ID is on the element around the `Slider`; its range input carries
 * {@link fieldInputId} and is labelled by the surrounding form field ({@link useFieldLabelProps}).</p>
 *
 * <p>Not used: label (rendered by the form field), placeholder, tooltip and nullable.</p>
 */
const MuiSliderAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<SliderStateJson>>();
  const inputId = fieldInputId(controlId);
  const labelProps = useFieldLabelProps(controlId, inputId);
  const [value, setValue, flush] = useTLFieldValue({
    debounceMs: state.debounceMs ?? VALUE_DEBOUNCE_MS,
  });

  const handleChange = useCallback((_event: Event, newValue: number | number[]) => {
    setValue(newValue as number);
  }, [setValue]);

  const handleCommit = useCallback(() => { void flush(); }, [flush]);

  if (state.hidden === true) {
    return null;
  }

  const label = state.valueLabel ?? '';

  if (showsValueOnly(state)) {
    return <MuiFieldValue id={controlId} className={rootClassName(state)} text={label} />;
  }

  const min = state.min ?? DEFAULT_MIN;
  const minLabel = state.minLabel ?? '';
  const maxLabel = state.maxLabel ?? '';
  const hasError = state.hasError === true;
  const color = hasError ? 'error' : state.hasWarnings === true ? 'warning' : 'primary';

  return (
    <Stack id={controlId} direction="row" spacing={2} sx={{ alignItems: 'center' }}>
      <Slider
        value={typeof value === 'number' ? value : min}
        min={min}
        max={state.max ?? DEFAULT_MAX}
        step={state.step ?? DEFAULT_STEP}
        onChange={handleChange}
        onChangeCommitted={handleCommit}
        onBlur={handleCommit}
        disabled={state.disabled === true}
        color={color}
        size="small"
        valueLabelDisplay={label === '' ? 'off' : 'auto'}
        valueLabelFormat={() => label}
        getAriaValueText={label === '' ? undefined : () => label}
        className={rootClassName(state)}
        sx={SLIDER_SX}
        slotProps={{
          input: {
            id: inputId,
            ...labelProps,
            ...fieldAriaProps(state),
            ...tooltipProps(hasError ? state.errorMessage : undefined),
          },
        }}
      />
      {(label !== '' || minLabel !== '' || maxLabel !== '') && (
        <Typography component="output" variant="body2" sx={VALUE_SX}>
          <Box component="span" sx={VALUE_PART_SX}>{label}</Box>
          <Box component="span" sx={VALUE_SIZER_SX} aria-hidden="true">{minLabel}</Box>
          <Box component="span" sx={VALUE_SIZER_SX} aria-hidden="true">{maxLabel}</Box>
        </Typography>
      )}
    </Stack>
  );
};

export default MuiSliderAdapter;
