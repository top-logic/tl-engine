import { React, useTLState, useTLCommand, CMD_VALUE_CHANGED, rootClassName } from 'tl-react-bridge';
import type { TLCellProps, DropdownSelectState } from 'tl-react-bridge';
import { ARG_OPTION, CMD_GOTO, OptionImage, ReadonlyValue, withPill } from './selectOptions';
import type { OptionDescriptor } from './selectOptions';

const { useCallback, useEffect, useLayoutEffect, useMemo, useRef, useState } = React;

/** Where the marker stands: the offset and the width of the segment it is under. */
interface MarkerPosition {
  left: number;
  width: number;
}

/**
 * A select field whose options are the segments of one bar, the selected one marked by a slider
 * that moves to it.
 *
 * The bar reads as one control whose position is the value, which suits a few mutually exclusive
 * options that belong together as one setting. A field taking one value follows the radio-group
 * pattern: the arrow keys move the selection from segment to segment. A field taking several has
 * no single position to mark, so it drops the marker and fills every segment that is on.
 *
 * The server hands this control the complete option list as soon as it is displayed - there is no
 * moment at which it could ask for it.
 */
const TLSegmentedChoice: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<DropdownSelectState>();
  const sendCommand = useTLCommand();

  const value = (state.value ?? []) as OptionDescriptor[];
  const options = (state.options ?? []) as OptionDescriptor[];
  const multiSelect = state.multiSelect === true;
  const mandatory = state.mandatory === true;
  const editable = state.editable !== false;
  const hasError = state.hasError === true;
  const hasWarnings = state.hasWarnings === true;

  // Tracks the latest selection so that a second click lands on what the first one produced, even
  // while the echo of the first has not arrived yet.
  const valueRef = useRef(value);
  valueRef.current = value;

  const rootRef = useRef<HTMLDivElement | null>(null);
  const segmentRefs = useRef<(HTMLButtonElement | null)[]>([]);

  const [marker, setMarker] = useState<MarkerPosition | null>(null);

  const selectedIds = useMemo(() => new Set(value.map((v) => v.value)), [value]);

  /** The segment the marker stands under, or -1 while nothing is selected. */
  const selectedIndex = useMemo(
    () => options.findIndex((o) => selectedIds.has(o.value)),
    [options, selectedIds]
  );

  // The marker is measured rather than computed: the segments are as wide as their labels, and
  // only the browser knows how wide that is.
  const measureMarker = useCallback(() => {
    if (multiSelect || selectedIndex < 0) {
      setMarker(null);
      return;
    }
    const segment = segmentRefs.current[selectedIndex];
    if (!segment) {
      setMarker(null);
      return;
    }
    setMarker({ left: segment.offsetLeft, width: segment.offsetWidth });
  }, [multiSelect, selectedIndex]);

  useLayoutEffect(measureMarker, [measureMarker, options, editable]);

  // A bar that is resized - by its container, by a font that arrives late - re-measures, so the
  // marker keeps standing under its segment.
  useEffect(() => {
    const root = rootRef.current;
    if (!root || typeof ResizeObserver === 'undefined') return;
    const observer = new ResizeObserver(measureMarker);
    observer.observe(root);
    return () => observer.disconnect();
  }, [measureMarker, editable]);

  const send = useCallback(
    (selection: OptionDescriptor[]) => {
      valueRef.current = selection;
      sendCommand(CMD_VALUE_CHANGED, { value: selection.map((v) => v.value) });
    },
    [sendCommand]
  );

  const choose = useCallback(
    (option: OptionDescriptor) => {
      const selection = valueRef.current;
      const selected = selection.some((v) => v.value === option.value);
      if (multiSelect) {
        send(selected ? selection.filter((v) => v.value !== option.value) : [...selection, option]);
        return;
      }
      if (!selected) {
        send([option]);
      } else if (!mandatory) {
        // The only way to empty a single-valued field: there is no separate clear button to do it.
        send([]);
      }
    },
    [multiSelect, mandatory, send]
  );

  /** Leads to the place the given option is displayed at. */
  const goto = useCallback(
    (optionValue: string) => {
      sendCommand(CMD_GOTO, { [ARG_OPTION]: optionValue });
    },
    [sendCommand]
  );

  // The radio pattern: the arrows move the selection, so the bar is operated without the pointer.
  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent) => {
      if (multiSelect || options.length === 0) return;
      let step = 0;
      if (e.key === 'ArrowRight' || e.key === 'ArrowDown') {
        step = 1;
      } else if (e.key === 'ArrowLeft' || e.key === 'ArrowUp') {
        step = -1;
      } else {
        return;
      }
      e.preventDefault();
      e.stopPropagation();
      const from = selectedIndex < 0 ? (step > 0 ? -1 : 0) : selectedIndex;
      const next = (from + step + options.length) % options.length;
      segmentRefs.current[next]?.focus();
      send([options[next]]);
    },
    [multiSelect, options, selectedIndex, send]
  );

  if (!editable) {
    return (
      <div
        id={controlId}
        className={rootClassName(state, 'tlSegmentedChoice', 'tlSegmentedChoice--immutable')}
      >
        {value.map((v) => (
          <ReadonlyValue key={v.value} option={v} onGoto={goto} />
        ))}
      </div>
    );
  }

  // Exactly one segment is reachable by Tab; within the bar the arrows move on, which is what makes
  // a group of radios one stop in the tab order rather than one stop per option.
  const tabStop = selectedIndex < 0 ? 0 : selectedIndex;

  return (
    <div
      id={controlId}
      ref={rootRef}
      role={multiSelect ? 'group' : 'radiogroup'}
      className={rootClassName(
        state,
        'tlSegmentedChoice',
        hasError && 'tlSegmentedChoice--error',
        !hasError && hasWarnings && 'tlSegmentedChoice--warning'
      )}
      onKeyDown={handleKeyDown}
    >
      {marker && (
        <span
          className="tlSegmentedChoice__marker"
          style={{ left: marker.left, width: marker.width }}
          aria-hidden="true"
        />
      )}
      {options.map((option, index) => {
        const selected = selectedIds.has(option.value);
        return (
          <button
            key={option.value}
            ref={(element) => {
              segmentRefs.current[index] = element;
            }}
            type="button"
            role={multiSelect ? undefined : 'radio'}
            aria-checked={multiSelect ? undefined : selected}
            aria-pressed={multiSelect ? selected : undefined}
            tabIndex={multiSelect || index === tabStop ? 0 : -1}
            className={
              'tlSegmentedChoice__segment' +
              (selected ? ' tlSegmentedChoice__segment--selected' : '')
            }
            onClick={() => choose(option)}
          >
            {withPill(option.color, (
              <>
                <OptionImage image={option.image} />
                <span className="tlSegmentedChoice__segmentLabel">{option.label}</span>
              </>
            ))}
          </button>
        );
      })}
    </div>
  );
};

export default TLSegmentedChoice;
