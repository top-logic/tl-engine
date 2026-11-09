import { React, useI18N, usePart, pressClosedSurface, ICON_COLUMNS } from 'tl-react-bridge';
import { PickerPopover } from '../picker/PickerPopover';
import { radioArrowTarget } from '../radioGroupKey';

const { useState, useEffect, useRef, useCallback, useId } = React;

const I18N_KEYS = {
  'js.table.view': 'View',
  'js.table.viewDensity': 'Density',
  'js.table.viewDensityDefault': 'As everywhere',
  'js.table.viewDensityNormal': 'Normal',
  'js.table.viewDensityCompact': 'Compact',
  'js.table.viewStripes': 'Stripes',
  'js.table.viewColumnLines': 'Column lines',
  'js.table.viewChooseColumns': 'Choose and order columns …',
  'js.table.viewReset': 'Reset',
};

/** The density of a table: the empty word follows the density of the page, the others hold the table to theirs. */
export type TableDensity = '' | 'normal' | 'compact';

/** What the view of a table consists of: the entries that take effect at once. */
export interface TableViewValues {
  density: TableDensity;
  striped: boolean;
  columnLines: boolean;
}

/** The densities in the order of their segments, each with the key of its label. */
const DENSITIES: ReadonlyArray<{ value: TableDensity; key: keyof typeof I18N_KEYS }> = [
  { value: '', key: 'js.table.viewDensityDefault' },
  { value: 'normal', key: 'js.table.viewDensityNormal' },
  { value: 'compact', key: 'js.table.viewDensityCompact' },
];

export interface TableViewOptionsProps extends TableViewValues {
  /**
   * Whether the table keeps the view of the user: the density and the two switches are offered only then, since a
   * choice that is lost at once is none.
   */
  viewOptions: boolean;

  /** Whether the table offers the column selection: the entry opening it is there only then. */
  columnSelect: boolean;

  /** Called at once with all three values whenever one of them is changed. */
  onChange(values: TableViewValues): void;

  /** Called when the entry for the column selection is pressed. */
  onColumns(): void;

  /** Called when the entry resetting the view is pressed. */
  onReset(): void;
}

/**
 * The content of the popover "View" of a table: the density as three segments and stripes and column lines as
 * switches (where the table keeps the view, see `viewOptions`), the entry opening the column selection (where the
 * table offers it) and the reset.
 *
 * <p>A change takes effect at once and is sent with the other two values. The values shown are held here until
 * the server echoes others, so that a second change, made before the echo of the first has arrived, is sent with
 * the first one and not against the old state.</p>
 *
 * <p>The segments are a radio group as in the segmented choice: one of them is the tab stop, the arrows move the
 * selection (and with it the focus) and wrap around at the ends.</p>
 */
export const TableViewOptions: React.FC<TableViewOptionsProps> = ({
  density, striped, columnLines, viewOptions, columnSelect, onChange, onColumns, onReset,
}) => {
  const i18n = useI18N(I18N_KEYS);
  const Checkbox = usePart('Checkbox');
  const headingId = useId();
  const segmentRefs = useRef<Array<HTMLButtonElement | null>>([]);

  const [shown, setShown] = useState<TableViewValues>({ density, striped, columnLines });
  // The server wins: what it sends replaces what was shown.
  useEffect(() => {
    setShown({ density, striped, columnLines });
  }, [density, striped, columnLines]);
  // The values last chosen, which the next change builds on even before the render of the first one.
  const latest = useRef(shown);
  latest.current = shown;

  const change = useCallback((patch: Partial<TableViewValues>) => {
    const next = { ...latest.current, ...patch };
    latest.current = next;
    setShown(next);
    onChange(next);
  }, [onChange]);

  const selectedIndex = Math.max(0, DENSITIES.findIndex((entry) => entry.value === shown.density));

  const handleKeyDown = (event: React.KeyboardEvent) => {
    const next = radioArrowTarget(event, selectedIndex, DENSITIES.length);
    if (next === null) {
      return;
    }
    segmentRefs.current[next]?.focus();
    change({ density: DENSITIES[next].value });
  };

  return (
    <>
      <div className="tl-table-view-options__title tl-type-label-strong">{i18n['js.table.view']}</div>
      {viewOptions && (
        <>
          <div id={headingId} className="tl-menu__header tl-type-label">{i18n['js.table.viewDensity']}</div>
          <div
            role="radiogroup"
            aria-labelledby={headingId}
            className="tl-segmented tl-table-view-options__segments"
            onKeyDown={handleKeyDown}
          >
            {DENSITIES.map((entry, index) => (
              <button
                key={entry.value}
                ref={(element) => {
                  segmentRefs.current[index] = element;
                }}
                type="button"
                role="radio"
                aria-checked={index === selectedIndex}
                // One segment is the tab stop; within the group the arrows move on.
                tabIndex={index === selectedIndex ? 0 : -1}
                className="tl-segmented__segment tl-type-label"
                onClick={() => change({ density: entry.value })}
              >
                <span className="tl-segmented__label">{i18n[entry.key]}</span>
              </button>
            ))}
          </div>
          <Checkbox
            display="switch"
            className="tl-table-view-options__switch"
            label={i18n['js.table.viewStripes']}
            checked={shown.striped}
            onChange={(checked) => change({ striped: checked })}
          />
          <Checkbox
            display="switch"
            className="tl-table-view-options__switch"
            label={i18n['js.table.viewColumnLines']}
            checked={shown.columnLines}
            onChange={(checked) => change({ columnLines: checked })}
          />
          <hr className="tl-divider" />
        </>
      )}
      {columnSelect && (
        <button type="button" className="tl-menu__item tl-type-body" onClick={onColumns}>
          <span className="tl-menu__label">{i18n['js.table.viewChooseColumns']}</span>
        </button>
      )}
      <button type="button" className="tl-menu__item tl-type-body" onClick={onReset}>
        <span className="tl-menu__label">{i18n['js.table.viewReset']}</span>
      </button>
    </>
  );
};

/** The focus starts on the segment of the density in force, where the table offers the density, else on the first entry. */
function selectedSegment(root: HTMLElement): HTMLElement | null {
  return root.querySelector<HTMLElement>('[role="radio"][tabindex="0"]') ?? root.querySelector<HTMLElement>('button');
}

export interface TableViewButtonProps extends TableViewValues {
  /** Whether the button sits in a column heading, drawn small there. */
  inCell?: boolean;
  viewOptions: boolean;
  columnSelect: boolean;
  onChange(values: TableViewValues): void;
  /** Called when the column selection is chosen; the popover closes on its own. */
  onColumns(): void;
  onReset(): void;
}

/**
 * The gear of the table: the strip that places it (`tl-table__columns`), the trigger of the part `MenuTrigger`
 * announcing a dialog, and the popover {@link TableViewOptions} it opens under itself. Escape and a press outside
 * close the popover, and the focus returns to the gear; so does a second press of the gear
 * (its press is a press outside, so the click that follows must not open the popover again).
 */
export const TableViewButton: React.FC<TableViewButtonProps> = ({ inCell, columnSelect, onColumns, ...view }) => {
  const i18n = useI18N(I18N_KEYS);
  const MenuTrigger = usePart('MenuTrigger');
  const anchorRef = useRef<HTMLButtonElement | null>(null);
  const [open, setOpen] = useState(false);
  const close = useCallback(() => setOpen(false), []);

  return (
    <div className="tl-table__columns">
      <MenuTrigger
        ref={anchorRef}
        appearance="ghost"
        icon={ICON_COLUMNS}
        label={i18n['js.table.view']}
        small={inCell}
        haspopup="dialog"
        expanded={open}
        // In a heading, the gestures of the heading itself (sorting, dragging) are none of the button's business.
        onMouseDown={(e) => e.stopPropagation()}
        onClick={(e) => {
          e.stopPropagation();
          e.preventDefault();
          // The press of this click has closed the popover this gear opens: leave it closed.
          if (!pressClosedSurface()) {
            setOpen(true);
          }
        }}
      />
      {open && (
        <PickerPopover
          anchorRef={anchorRef}
          label={i18n['js.table.view']}
          className="tl-menu tl-table-view-options"
          initialFocus={selectedSegment}
          onClose={close}
        >
          <TableViewOptions
            {...view}
            columnSelect={columnSelect}
            onColumns={() => {
              setOpen(false);
              onColumns();
            }}
          />
        </PickerPopover>
      )}
    </div>
  );
};
