import { React, useI18N, usePart, usePopupTrigger, useRovingMenu, TLChild, ThemeIcon, ICON_COLUMNS, ICON_NEXT, ICON_PREVIOUS, ICON_RESET, ICON_VIEW_SETTINGS } from 'tl-react-bridge';
import type { TableDensity } from 'tl-react-bridge';
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
  'js.table.viewBack': 'Back',
  // The entry resetting the arrangement of the columns of this table: columns, order, widths, frozen, sorting, grouping.
  'js.table.columnsReset': 'Reset columns',
  // The title of the first page, the columns, and the name of the gear opening it.
  'js.table.columns': 'Columns',
};

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
   * Whether the page is not the one showing: it stays in the popover, hidden, so that what it holds stays as it is,
   * and its entries leave the roving.
   */
  hidden?: boolean;

  /** Receives the way back, so that the focus can go to it when the page shows. */
  backRef?: React.Ref<HTMLElement>;

  /**
   * Called by the way back in the head of the page; the way back is there only with it, where the page stands behind
   * the page "Columns".
   */
  onBack?(): void;

  /** Called at once with all three values whenever one of them is changed. */
  onChange(values: TableViewValues): void;

  /**
   * Called when the entry resetting the columns is pressed; the entry is there only with it, as the last of the menu,
   * where the page stands alone and the table keeps its state.
   */
  onReset?(): void;
}

/** The stops of the roving over the menu, in the order of the page: the density is one of them. */
type ViewStop = 'density' | 'stripes' | 'columnLines' | 'reset';

/**
 * The page "View" of the popover of a table, the person's view, the same for all tables: a head with the title, behind
 * the way back where the page stands behind the page "Columns", and under it a menu named by the title: the density as
 * three segments, stripes and column lines as switches, and, where the page stands alone and the table keeps its
 * state, the reset of its columns as the last entry.
 *
 * <p>Every entry is a menu item and the whole row acts: the density a `menuitemradio` per segment, the two switches a
 * `menuitemcheckbox` whose switch at the right is a drawing of the state, no input, the reset a `menuitem`. Up,
 * Down, Home and End move over the entries (useRovingMenu), where the three segments are one stop - the one in
 * force; Left and Right on a segment choose the density, wrapping around at the ends, and leave the stop where it
 * is. One entry is in the tab order, the one last focused. The reset sends its command and closes the popover.</p>
 *
 * <p>A change takes effect at once and is sent with the other two values. The values shown are held here until
 * the caller hands others, so that a second change, made before the first one has arrived, is sent with the first one
 * and not against the old state.</p>
 */
export const TableViewOptions: React.FC<TableViewOptionsProps> = ({
  density, striped, columnLines, hidden, backRef, onBack, onChange, onReset,
}) => {
  const i18n = useI18N(I18N_KEYS);
  const Button = usePart('Button');
  const titleId = useId();
  const headingId = useId();
  const menuRef = useRef<HTMLDivElement | null>(null);
  const segmentRefs = useRef<Array<HTMLButtonElement | null>>([]);

  const [shown, setShown] = useState<TableViewValues>({ density, striped, columnLines });
  // What the page holds replaces what was shown: another table, or the person's value from elsewhere.
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

  // The entries are the tab stop one at a time: the one last focused, else the first.
  const [tabStop, setTabStop] = useState<ViewStop>('density');
  const stopProps = (stop: ViewStop) => ({
    tabIndex: stop === tabStop ? 0 : -1,
    onFocus: () => setTabStop(stop),
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
    <div className="tl-table-view-options__page" hidden={hidden}>
      <div className="tl-table-view-options__title tl-type-label-strong">
        {onBack && (
          <Button
            ref={backRef}
            label={i18n['js.table.viewBack']}
            image={ICON_PREVIOUS}
            display="icon-only"
            appearance="ghost"
            small
            className="tl-table-view-options__back"
            onClick={onBack}
          />
        )}
        {/* The text alone names the menu, not the way back. */}
        <span id={titleId}>{i18n['js.table.view']}</span>
      </div>
      <div ref={menuRef} role="menu" aria-labelledby={titleId} className="tl-table-view-options__menu" onKeyDown={roveKeyDown}>
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
              onFocus={() => setTabStop('density')}
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
        {onReset && (
          <>
            <hr className="tl-divider" />
            <button
              type="button"
              role="menuitem"
              className="tl-menu__item tl-type-body"
              onClick={onReset}
              {...stopProps('reset')}
            >
              <ThemeIcon encoded={ICON_RESET} className="tl-menu__icon tl-icon-sm" />
              <span className="tl-menu__label">{i18n['js.table.columnsReset']}</span>
            </button>
          </>
        )}
      </div>
    </div>
  );
};

/**
 * Where the focus starts, on the page that shows: the density in force on the page "View"; on the page "Columns" the
 * search of the selection where it has arrived with one, else the row "View" at the foot of the page, not the reset
 * above it. The rows of the selection take no focus here: the handle of the first one shows only under the pointer.
 */
function initialFocus(root: HTMLElement): HTMLElement | null {
  const page = root.querySelector<HTMLElement>('.tl-table-view-options__page:not([hidden])');
  return page?.querySelector<HTMLElement>('[role="menuitemradio"][aria-checked="true"]')
    ?? page?.querySelector<HTMLElement>('input')
    ?? page?.querySelector<HTMLElement>('.tl-menu__item--nav')
    ?? null;
}

export interface TableViewButtonProps extends TableViewValues {
  /** Whether the button sits in a column heading, drawn small there. */
  inCell?: boolean;

  /** Whether the table keeps its state: the arrangement of its columns can then be reset. */
  canReset: boolean;
  columnSelect: boolean;

  /**
   * The column selection the server holds for the page "Columns", a child control; `null` while it holds none. It
   * arrives after `onColumns`, and it is released after `onColumnsClose`.
   */
  columnSelection: unknown;

  onChange(values: TableViewValues): void;

  /** Called when the popover opens where the table offers the column selection: the server builds it. */
  onColumns(): void;

  /**
   * Called when the popover closes after `onColumns` - on Escape, on a press outside, on a second press of the gear,
   * after the reset, or with the button going away -, so that the server releases the column selection.
   */
  onColumnsClose(): void;

  /** Called when the entry resetting the columns is pressed: on the page "Columns", or on the page "View" alone. */
  onReset(): void;
}

/** The ID of the child control the server holds, `null` for none. */
function controlIdOf(child: unknown): string | null {
  return (child as { controlId?: string } | null)?.controlId ?? null;
}

/** The two pages of the popover: the columns, and the view. */
type ViewPage = 'columns' | 'view';

/**
 * The gear of the table: the strip that places it (`tl-table__columns`), the trigger of the part `MenuTrigger`
 * announcing a dialog (usePopupTrigger), and the popover it opens under itself with two pages, one at a time. First the
 * columns, the setting a table is adjusted by most: the column selection the server holds as a child control, at the
 * foot the reset of the columns, where the table keeps its state, and the row "View" leading to the second page, the
 * person's view ({@link TableViewOptions}) under a head with the way back. A table without column selection has the
 * view alone, with the reset as its last entry. The gear is named after the page it opens. The pages are the popover's
 * own, every gesture takes effect at once, and there is no dialog.
 *
 * <p>The column selection lives while the popover is open: the server is asked for it when the popover opens and told
 * to release it when the popover closes, not when the pages change. Escape and a press outside close the popover from
 * either page, and the focus returns to the gear; so does a second press of the gear (its press is a press outside,
 * so the click that follows does not open the popover again). Opened again, the popover starts on the columns.</p>
 *
 * <p>The focus follows the page: the way back takes it on the view, the row "View" on the way back. The selection
 * arrives a round trip after the opening; until then the focus waits on the row "View", and the selection takes it
 * from there into its search, if it has one.</p>
 */
export const TableViewButton: React.FC<TableViewButtonProps> = ({
  inCell, canReset, columnSelect, columnSelection, onColumns, onColumnsClose, onReset, ...values
}) => {
  const i18n = useI18N(I18N_KEYS);
  const MenuTrigger = usePart('MenuTrigger');
  const anchorRef = useRef<HTMLButtonElement | null>(null);
  // The row "View" at the foot of the columns, where the focus waits; not the reset above it.
  const footRef = useRef<HTMLButtonElement | null>(null);
  const backRef = useRef<HTMLElement | null>(null);
  const [open, setOpen] = useState(false);
  // Whether the view shows in front of the columns; a table without column selection shows it alone.
  const [viewShown, setViewShown] = useState(false);
  const page: ViewPage = columnSelect && !viewShown ? 'columns' : 'view';

  // Whether the server was asked for the column selection and not yet told to release it. Refs: read by the closing
  // handlers, which a surface keeps from the render that opened it, and by the cleanup.
  const columnsAsked = useRef(false);
  const onColumnsCloseRef = useRef(onColumnsClose);
  onColumnsCloseRef.current = onColumnsClose;

  // The selections of the server are told apart by their ID. When the popover opens again within one round trip of
  // its closing (a second press of the gear), the server may still hold the selection of the earlier opening: that one
  // was released - the client said so - and is not drawn; the page waits for the new one.
  const released = useRef(new Set<string>());
  const currentId = controlIdOf(columnSelection);
  const currentIdRef = useRef(currentId);
  currentIdRef.current = currentId;
  const selectionId = currentId !== null && !released.current.has(currentId) ? currentId : null;

  const close = useCallback(() => {
    if (columnsAsked.current) {
      columnsAsked.current = false;
      if (currentIdRef.current !== null) {
        released.current.add(currentIdRef.current);
      }
      onColumnsCloseRef.current();
    }
    setViewShown(false);
    setOpen(false);
  }, []);
  const show = () => {
    if (columnSelect) {
      columnsAsked.current = true;
      onColumns();
    }
    setOpen(true);
  };
  const trigger = usePopupTrigger(open, (next) => (next ? show() : close()), 'dialog');

  // Like every menu entry, the reset closes the popover after it has acted.
  const reset = () => {
    onReset();
    close();
  };

  // The button goes away - the table redraws its heading, the table is closed - while the popover is open: the server
  // releases the selection nobody shows any more.
  useEffect(() => () => {
    if (columnsAsked.current) {
      onColumnsCloseRef.current();
    }
  }, []);

  // The focus follows the page: the control that was pressed is gone with its page.
  const shownPage = useRef(page);
  useLayoutEffect(() => {
    if (shownPage.current === page) {
      return;
    }
    shownPage.current = page;
    (page === 'view' ? backRef : footRef).current?.focus();
  }, [page]);

  // The focus follows the selection on the columns, once per selection. A selection going away with the focus leaves
  // it on the row "View"; a selection arriving while the focus waits there takes it into its search - and has none for
  // it without a search (ten columns or fewer).
  useLayoutEffect(() => {
    if (!open || page !== 'columns') {
      return;
    }
    const foot = footRef.current;
    if (document.activeElement === null || document.activeElement === document.body) {
      foot?.focus();
    }
    if (selectionId !== null && document.activeElement === foot) {
      document.getElementById(selectionId)?.querySelector<HTMLElement>('input')?.focus();
    }
  }, [selectionId]);

  return (
    <div className="tl-table__columns">
      <MenuTrigger
        ref={anchorRef}
        appearance="ghost"
        icon={ICON_COLUMNS}
        label={i18n[columnSelect ? 'js.table.columns' : 'js.table.view']}
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
          initialFocus={initialFocus}
          onClose={close}
          // The pages differ in height, and the selection fills the columns when it arrives: each is placed anew, on
          // the side with room for it; within a page the side stays, also while the search narrows the list.
          layoutKey={page + ':' + (selectionId ?? '')}
        >
          {/* Hidden, not unmounted, while the other page shows: what the page holds stays as it is. */}
          {columnSelect && (
            <div className="tl-table-view-options__page" hidden={page !== 'columns'}>
              <div className="tl-table-view-options__title tl-type-label-strong">{i18n['js.table.columns']}</div>
              {selectionId !== null && <TLChild control={columnSelection} />}
              <hr className="tl-divider" />
              {/* The page is no menu: plain buttons, no menu items. */}
              {canReset && (
                <button type="button" className="tl-menu__item tl-type-body" onClick={reset}>
                  <ThemeIcon encoded={ICON_RESET} className="tl-menu__icon tl-icon-sm" />
                  <span className="tl-menu__label">{i18n['js.table.columnsReset']}</span>
                </button>
              )}
              <button
                ref={footRef}
                type="button"
                className="tl-menu__item tl-menu__item--nav tl-type-body"
                onClick={() => setViewShown(true)}
              >
                <ThemeIcon encoded={ICON_VIEW_SETTINGS} className="tl-menu__icon tl-icon-sm" />
                <span className="tl-menu__label">{i18n['js.table.view']}</span>
                <ThemeIcon encoded={ICON_NEXT} className="tl-icon-sm tl-menu__chevron" />
              </button>
            </div>
          )}
          <TableViewOptions
            {...values}
            hidden={page !== 'view'}
            backRef={backRef}
            onBack={columnSelect ? () => setViewShown(false) : undefined}
            onReset={!columnSelect && canReset ? reset : undefined}
          />
        </PickerPopover>
      )}
    </div>
  );
};
