import { React, useI18N, usePart, usePopupTrigger, useRovingMenu, TLChild, ICON_COLUMNS, ICON_PREVIOUS } from 'tl-react-bridge';
import { PickerPopover } from '../picker/PickerPopover';
import { radioArrowTarget } from '../radioGroupKey';

const { useState, useEffect, useLayoutEffect, useRef, useCallback, useId } = React;

const I18N_KEYS = {
  'js.table.view': 'View',
  'js.table.viewDensity': 'Density',
  'js.table.viewDensityDefault': 'As everywhere',
  'js.table.viewDensityNormal': 'Normal',
  'js.table.viewDensityCompact': 'Compact',
  'js.table.viewStripes': 'Stripes',
  'js.table.viewColumnLines': 'Column lines',
  'js.table.viewColumns': 'Columns · {0} of {1}',
  'js.table.viewBack': 'Back',
  'js.table.viewReset': 'Reset',
  // The title of the second page: the columns.
  'js.table.columns': 'Columns',
};

/** Fills the placeholders {0} and {1} of a translated text. */
function format(template: string, first: number, second: number): string {
  return template.replace('{0}', String(first)).replace('{1}', String(second));
}

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

  /** How many of the columns the user can choose the table displays: the "n" of the entry "Columns · n of m". */
  columnsShown: number;

  /** How many columns the user can choose, displayed or not: the "m" of the entry. */
  columnsTotal: number;

  /** Receives the entry for the column selection, so that the focus can return to it from the second page. */
  columnsEntryRef?: React.Ref<HTMLButtonElement>;

  /**
   * Whether the page is not the one showing: it stays in the popover, hidden, so that what it holds stays as it is,
   * and its entries leave the roving.
   */
  hidden?: boolean;

  /** Called at once with all three values whenever one of them is changed. */
  onChange(values: TableViewValues): void;

  /** Called when the entry for the column selection is pressed. */
  onColumns(): void;

  /** Called when the entry resetting the view is pressed. */
  onReset(): void;
}

/** The stops of the roving over the first page, in the order of the page: the density is one of them. */
type ViewStop = 'density' | 'stripes' | 'columnLines' | 'columns' | 'reset';

/**
 * The first page of the popover "View" of a table, a menu: the density as three segments and stripes and column lines
 * as switches (where the table keeps the view, see `viewOptions`), the entry opening the column selection as the
 * second page (where the table offers it; it tells how many of the columns are shown) and the reset.
 *
 * <p>Every entry is a menu item and the whole row acts: the density a `menuitemradio` per segment, the two switches a
 * `menuitemcheckbox` whose switch at the right is a drawing of the state, no input, the other two a `menuitem`. Up,
 * Down, Home and End move over the entries (useRovingMenu), where the three segments are one stop - the one in
 * force; Left and Right on a segment choose the density, wrapping around at the ends, and leave the stop where it
 * is. One entry is in the tab order, the one last focused. The reset sends its command and closes the popover.</p>
 *
 * <p>A change takes effect at once and is sent with the other two values. The values shown are held here until
 * the server echoes others, so that a second change, made before the echo of the first has arrived, is sent with
 * the first one and not against the old state.</p>
 */
export const TableViewOptions: React.FC<TableViewOptionsProps> = ({
  density, striped, columnLines, viewOptions, columnSelect, columnsShown, columnsTotal, columnsEntryRef, hidden,
  onChange, onColumns, onReset,
}) => {
  const i18n = useI18N(I18N_KEYS);
  const titleId = useId();
  const headingId = useId();
  const menuRef = useRef<HTMLDivElement | null>(null);
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

  // The entries are the tab stop one at a time: the one last focused while there is one, else the first.
  const stops: ViewStop[] = [
    ...(viewOptions ? ['density', 'stripes', 'columnLines'] as const : []),
    ...(columnSelect ? ['columns'] as const : []),
    'reset',
  ];
  const [focusStop, setFocusStop] = useState<ViewStop | null>(null);
  const tabStop = focusStop !== null && stops.includes(focusStop) ? focusStop : stops[0];
  const stopProps = (stop: ViewStop) => ({
    tabIndex: stop === tabStop ? 0 : -1,
    onFocus: () => setFocusStop(stop),
  });

  // The segments of the density are one stop: only the one in force takes the arrows from above and below.
  const { onKeyDown: roveKeyDown } = useRovingMenu(
    menuRef,
    true,
    (entry) => entry.getAttribute('role') !== 'menuitemradio' || entry.getAttribute('aria-checked') === 'true',
  );

  // Left and Right choose the density; the focus goes with the choice, the stop stays the density.
  const handleDensityKeyDown = (event: React.KeyboardEvent) => {
    if (event.key !== 'ArrowLeft' && event.key !== 'ArrowRight') {
      return;
    }
    const next = radioArrowTarget(event, selectedIndex, DENSITIES.length);
    if (next === null) {
      return;
    }
    segmentRefs.current[next]?.focus();
    change({ density: DENSITIES[next].value });
  };

  return (
    <div ref={menuRef} role="menu" aria-labelledby={titleId} className="tl-table-view-options__page" hidden={hidden} onKeyDown={roveKeyDown}>
      <div id={titleId} role="presentation" className="tl-table-view-options__title tl-type-label-strong">
        {i18n['js.table.view']}
      </div>
      {viewOptions && (
        <>
          <div id={headingId} role="presentation" className="tl-menu__header tl-type-label">
            {i18n['js.table.viewDensity']}
          </div>
          <div
            role="group"
            aria-labelledby={headingId}
            className="tl-segmented tl-table-view-options__segments"
            onKeyDown={handleDensityKeyDown}
          >
            {DENSITIES.map((entry, index) => (
              <button
                key={entry.value}
                ref={(element) => {
                  segmentRefs.current[index] = element;
                }}
                type="button"
                role="menuitemradio"
                aria-checked={index === selectedIndex}
                // The segment in force is the stop of the density; the others are reached with Left and Right.
                tabIndex={index === selectedIndex && tabStop === 'density' ? 0 : -1}
                onFocus={() => setFocusStop('density')}
                className="tl-segmented__segment tl-type-label"
                onClick={() => change({ density: entry.value })}
              >
                <span className="tl-segmented__label">{i18n[entry.key]}</span>
              </button>
            ))}
          </div>
          <button
            type="button"
            role="menuitemcheckbox"
            aria-checked={shown.striped}
            className="tl-menu__item tl-menu__item--check tl-type-body"
            onClick={() => change({ striped: !latest.current.striped })}
            {...stopProps('stripes')}
          >
            <span className="tl-menu__label">{i18n['js.table.viewStripes']}</span>
            <span className="tl-checkbox tl-checkbox--switch" aria-hidden="true" />
          </button>
          <button
            type="button"
            role="menuitemcheckbox"
            aria-checked={shown.columnLines}
            className="tl-menu__item tl-menu__item--check tl-type-body"
            onClick={() => change({ columnLines: !latest.current.columnLines })}
            {...stopProps('columnLines')}
          >
            <span className="tl-menu__label">{i18n['js.table.viewColumnLines']}</span>
            <span className="tl-checkbox tl-checkbox--switch" aria-hidden="true" />
          </button>
          <hr className="tl-divider" />
        </>
      )}
      {columnSelect && (
        <button
          ref={columnsEntryRef}
          type="button"
          role="menuitem"
          className="tl-menu__item tl-type-body"
          onClick={onColumns}
          {...stopProps('columns')}
        >
          <span className="tl-menu__label">
            {format(i18n['js.table.viewColumns'], columnsShown, columnsTotal)}
            <span aria-hidden="true"> ›</span>
          </span>
        </button>
      )}
      <button
        type="button"
        role="menuitem"
        className="tl-menu__item tl-type-body"
        onClick={onReset}
        {...stopProps('reset')}
      >
        <span className="tl-menu__label">{i18n['js.table.viewReset']}</span>
      </button>
    </div>
  );
};

/** The focus starts on the segment of the density in force, where the table offers the density, else on the first entry. */
function selectedSegment(root: HTMLElement): HTMLElement | null {
  return root.querySelector<HTMLElement>('[role="menuitemradio"][aria-checked="true"]')
    ?? root.querySelector<HTMLElement>('[role^="menuitem"]');
}

export interface TableViewButtonProps extends TableViewValues {
  /** Whether the button sits in a column heading, drawn small there. */
  inCell?: boolean;
  viewOptions: boolean;
  columnSelect: boolean;
  columnsShown: number;
  columnsTotal: number;

  /**
   * The column selection the server holds for the second page, a child control; `null` while it holds none. It
   * arrives after `onColumns`, and it is released after `onColumnsClose`.
   */
  columnSelection: unknown;

  onChange(values: TableViewValues): void;

  /** Called when the second page opens: the server builds the column selection. */
  onColumns(): void;

  /**
   * Called when the second page closes - by its way back, on Escape, on a press outside - so that the server releases
   * the column selection. Not called for the first page.
   */
  onColumnsClose(): void;
  onReset(): void;
}

/** The ID of the child control the server holds, `null` for none. */
function controlIdOf(child: unknown): string | null {
  return (child as { controlId?: string } | null)?.controlId ?? null;
}

/** The two pages of the popover: the view, and the columns. */
type ViewPage = 'view' | 'columns';

/**
 * The gear of the table: the strip that places it (`tl-table__columns`), the trigger of the part `MenuTrigger`
 * announcing a dialog (usePopupTrigger), and the popover it opens under itself with two pages, one at a time: the
 * view ({@link TableViewOptions}) and, behind its entry "Columns", the column selection the server holds as a child
 * control, under a head with the way back. The pages are the popover's own, the selection takes effect with every
 * gesture, and there is no dialog.
 *
 * <p>Escape and a press outside close the popover - also from the second page, which then tells the server - and the
 * focus returns to the gear; so does a second press of the gear (its press is a press outside, so the click that
 * follows does not open the popover again). The focus follows the page: it starts on the way back of the second
 * page, and returns to the entry "Columns" on the way back.</p>
 */
export const TableViewButton: React.FC<TableViewButtonProps> = ({
  inCell, columnSelect, columnsShown, columnsTotal, columnSelection, onColumns, onColumnsClose, ...view
}) => {
  const i18n = useI18N(I18N_KEYS);
  const MenuTrigger = usePart('MenuTrigger');
  const Button = usePart('Button');
  const anchorRef = useRef<HTMLButtonElement | null>(null);
  const columnsEntryRef = useRef<HTMLButtonElement | null>(null);
  const backRef = useRef<HTMLElement | null>(null);
  const [open, setOpen] = useState(false);
  const [page, setPage] = useState<ViewPage>('view');
  // Read by the closing handlers, which a surface keeps from the render that opened it, and by the cleanup.
  const pageRef = useRef(page);
  pageRef.current = page;
  const openRef = useRef(open);
  openRef.current = open;
  const onColumnsCloseRef = useRef(onColumnsClose);
  onColumnsCloseRef.current = onColumnsClose;

  // The selections of the server are told apart by their ID. Between a press of the entry and the arrival of what it
  // asked for, the server may still hold the selection of an earlier press (the way back and the entry again within one
  // round trip): that one was released - the client said so - and is neither drawn nor waited for. `pending` counts
  // the presses of the entry whose selection has not arrived; while it is above zero, a release (`null`) is the
  // server catching up and no reason to go back to the first page.
  const released = useRef(new Set<string>());
  const pending = useRef(0);
  const arrived = useRef<string | null>(null);
  const currentId = controlIdOf(columnSelection);
  const selection = currentId !== null && !released.current.has(currentId) ? columnSelection : null;

  const releaseSelection = useCallback(() => {
    const id = controlIdOf(columnSelection);
    if (id !== null) {
      released.current.add(id);
    }
  }, [columnSelection]);

  const close = useCallback(() => {
    if (pageRef.current === 'columns') {
      releaseSelection();
      onColumnsClose();
    }
    pending.current = 0;
    setPage('view');
    setOpen(false);
  }, [onColumnsClose, releaseSelection]);
  const trigger = usePopupTrigger(open, (next) => (next ? setOpen(true) : close()), 'dialog');

  const showColumns = useCallback(() => {
    pending.current++;
    setPage('columns');
    onColumns();
  }, [onColumns]);
  const showView = useCallback(() => {
    releaseSelection();
    onColumnsClose();
    setPage('view');
  }, [onColumnsClose, releaseSelection]);

  // The server wins: a selection it released - the table left the display, another selection replaced it - takes the
  // popover back to the first page.
  useEffect(() => {
    if (selection !== null) {
      if (currentId !== arrived.current) {
        arrived.current = currentId;
        pending.current = Math.max(0, pending.current - 1);
      }
    } else if (page === 'columns' && pending.current === 0) {
      setPage('view');
    }
  }, [selection, currentId, page]);

  // The button goes away - the table redraws its heading, the table is closed - while the second page is open: the
  // server releases the selection nobody shows any more.
  useEffect(() => () => {
    if (openRef.current && pageRef.current === 'columns') {
      onColumnsCloseRef.current();
    }
  }, []);

  // The focus follows the page: the entry that was pressed is gone with its page.
  const shownPage = useRef(page);
  useLayoutEffect(() => {
    if (shownPage.current === page) {
      return;
    }
    shownPage.current = page;
    (page === 'columns' ? backRef : columnsEntryRef).current?.focus();
  }, [page]);

  return (
    <div className="tl-table__columns">
      <MenuTrigger
        ref={anchorRef}
        appearance="ghost"
        icon={ICON_COLUMNS}
        label={i18n['js.table.view']}
        small={inCell}
        haspopup={trigger.haspopup}
        expanded={trigger.expanded}
        // In a heading, the gestures of the heading itself (sorting, dragging) are none of the button's business.
        onMouseDown={(e) => e.stopPropagation()}
        onClick={(e) => {
          e.stopPropagation();
          e.preventDefault();
          trigger.onClick();
        }}
      />
      {open && (
        <PickerPopover
          anchorRef={anchorRef}
          label={i18n[page === 'columns' ? 'js.table.columns' : 'js.table.view']}
          className="tl-menu tl-table-view-options"
          initialFocus={selectedSegment}
          onClose={close}
        >
          {/* Hidden, not unmounted, while the other page shows: what the page holds stays as it is. */}
          <TableViewOptions
            {...view}
            hidden={page !== 'view'}
            columnSelect={columnSelect}
            columnsShown={columnsShown}
            columnsTotal={columnsTotal}
            columnsEntryRef={columnsEntryRef}
            onColumns={showColumns}
            // Like every menu entry, the reset closes the popover after it has acted.
            onReset={() => {
              view.onReset();
              close();
            }}
          />
          {page === 'columns' && (
            <div className="tl-table-view-options__page">
              <div className="tl-table-view-options__title tl-type-label-strong">
                <Button
                  ref={backRef}
                  label={i18n['js.table.viewBack']}
                  image={ICON_PREVIOUS}
                  display="icon-only"
                  appearance="ghost"
                  small
                  className="tl-table-view-options__back"
                  onClick={showView}
                />
                {i18n['js.table.columns']}
              </div>
              {selection !== null && <TLChild control={selection} />}
            </div>
          )}
        </PickerPopover>
      )}
    </div>
  );
};
