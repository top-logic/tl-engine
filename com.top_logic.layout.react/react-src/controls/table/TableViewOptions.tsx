import { React, useI18N, usePart, usePopupTrigger, TLChild, ThemeIcon, ICON_COLUMNS, ICON_RESET } from 'tl-react-bridge';
import { PickerPopover } from '../picker/PickerPopover';

const { useState, useEffect, useLayoutEffect, useRef, useCallback } = React;

const I18N_KEYS = {
  // The entry resetting the arrangement of the columns of this table: columns, order, widths, frozen, sorting, grouping.
  'js.table.columnsReset': 'Reset columns',
  // The title of the popover, and the name of the gear opening it.
  'js.table.columns': 'Columns',
};

export interface TableViewButtonProps {
  /** Whether the button sits in a column heading, drawn small there. */
  inCell?: boolean;

  /** Whether the table keeps its state: the arrangement of its columns can then be reset. */
  canReset: boolean;
  columnSelect: boolean;

  /**
   * The column selection the server holds, a child control; `null` while it holds none. It arrives after `onColumns`,
   * and it is released after `onColumnsClose`.
   */
  columnSelection: unknown;

  /** Called when the popover opens where the table offers the column selection: the server builds it. */
  onColumns(): void;

  /**
   * Called when the popover closes after `onColumns` - on Escape, on a press outside, on a second press of the gear,
   * after the reset, or with the button going away -, so that the server releases the column selection.
   */
  onColumnsClose(): void;

  /** Called when the entry resetting the columns is pressed. */
  onReset(): void;
}

/** The ID of the child control the server holds, `null` for none. */
function controlIdOf(child: unknown): string | null {
  return (child as { controlId?: string } | null)?.controlId ?? null;
}

/**
 * The gear of the table: the strip that places it (`tl-table__columns`), the trigger of the part `MenuTrigger`
 * announcing a dialog (usePopupTrigger), and the popover it opens under itself: the column selection the server holds
 * as a child control, and below it, where the table keeps its state, the reset of the columns. The gear is there where
 * the table has either, and is named after the columns whichever it opens. How tables look - density, stripes, column
 * lines - is not set here but in the user menu, for all tables of the page at once. Every gesture takes effect at once,
 * and there is no dialog.
 *
 * <p>The column selection lives while the popover is open: the server is asked for it when the popover opens and told
 * to release it when the popover closes. Escape and a press outside close the popover, and the focus returns to the
 * gear; so does a second press of the gear (its press is a press outside, so the click that follows does not open the
 * popover again).</p>
 *
 * <p>The focus waits on the popover itself, never on the reset below the selection. The selection arrives a round trip
 * after the opening; it takes the focus from the popover into its search, if it has one.</p>
 */
export const TableViewButton: React.FC<TableViewButtonProps> = ({
  inCell, canReset, columnSelect, columnSelection, onColumns, onColumnsClose, onReset,
}) => {
  const i18n = useI18N(I18N_KEYS);
  const MenuTrigger = usePart('MenuTrigger');
  const anchorRef = useRef<HTMLButtonElement | null>(null);
  // The column page inside the popover, which leads to the popover (its surface) for the focus.
  const pageRef = useRef<HTMLDivElement | null>(null);
  const [open, setOpen] = useState(false);

  // Whether the server was asked for the column selection and not yet told to release it. Refs: read by the closing
  // handlers, which a surface keeps from the render that opened it, and by the cleanup.
  const columnsAsked = useRef(false);
  const onColumnsCloseRef = useRef(onColumnsClose);
  onColumnsCloseRef.current = onColumnsClose;

  // The selections of the server are told apart by their ID. When the popover opens again within one round trip of
  // its closing (a second press of the gear), the server may still hold the selection of the earlier opening: that one
  // was released - the client said so - and is not drawn; the popover waits for the new one.
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

  // The focus follows the selection, once per selection. A selection arriving while the focus waits on the popover
  // takes it into its search - and has none for it without a search (ten columns or fewer). A selection going away
  // with the focus leaves it on the popover.
  useLayoutEffect(() => {
    const popover = pageRef.current?.closest<HTMLElement>('.tl-table-view-options');
    if (!open || !popover) {
      return;
    }
    if (document.activeElement === null || document.activeElement === document.body) {
      popover.focus();
    }
    if (selectionId !== null && document.activeElement === popover) {
      document.getElementById(selectionId)?.querySelector<HTMLElement>('input')?.focus();
    }
  }, [selectionId]);

  return (
    <div className="tl-table__columns">
      <MenuTrigger
        ref={anchorRef}
        appearance="ghost"
        icon={ICON_COLUMNS}
        label={i18n['js.table.columns']}
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
          label={i18n['js.table.columns']}
          className="tl-menu tl-table-view-options"
          // No entry takes the focus on opening: the popover does, and the selection moves it on when it arrives.
          initialFocus={() => null}
          onClose={close}
          // The selection fills the popover when it arrives: it is placed anew, on the side with room for it; the side
          // stays while the search narrows the list.
          layoutKey={selectionId ?? ''}
        >
          <div ref={pageRef} className="tl-table-view-options__page">
            {columnSelect && (
              <>
                <div className="tl-table-view-options__title tl-type-label-strong">{i18n['js.table.columns']}</div>
                {selectionId !== null && <TLChild control={columnSelection} />}
              </>
            )}
            {canReset && (
              <>
                {columnSelect && <hr className="tl-divider" />}
                {/* The popover is no menu: a plain button, no menu item. */}
                <button type="button" className="tl-menu__item tl-type-body" onClick={reset}>
                  <ThemeIcon encoded={ICON_RESET} className="tl-menu__icon tl-icon-sm" />
                  <span className="tl-menu__label">{i18n['js.table.columnsReset']}</span>
                </button>
              </>
            )}
          </div>
        </PickerPopover>
      )}
    </div>
  );
};
