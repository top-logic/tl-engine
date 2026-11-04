import { React, useTLState, useTLCommand, useI18N, TLChild, firstFocusable } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { FilterFooter } from './TLFilterList';

const { useEffect, useRef, useCallback } = React;

const I18N_KEYS = {
  'js.table.clear': 'Remove filter',
  'js.table.filterApply': 'Apply',
};

/** Reads the form and filters the table by it; offered only by a form that applies explicitly. */
const CMD_APPLY = 'apply';

/** Removes the filter of the column; the server answers with a new, empty form. */
const CMD_CLEAR = 'clear';

/**
 * The content of the filter popover of a column whose filter is a form (see ReactFilterFormControl): the form in the
 * scroll area of the popover (tl-picker__scroll), and below it the footer removing the filter - disabled while the
 * column is not filtered.
 *
 * <p>The fields of a built-in filter take effect by themselves - a text after a pause or on Enter, a choice at once.
 * The filter UI of an application applies explicitly (state `applies`): its footer offers "Apply" as well.</p>
 *
 * <p>Removing the filter replaces the form by an empty one. The button removing it is disabled by its own click, and
 * the form it stood under is gone: the focus goes to the first field of the new form.</p>
 */
const TLFilterForm: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

  // A child control, as the server sends it; its ID changes when the form is replaced.
  const form = state.form as { controlId: string } | undefined;
  const active = state.active === true;
  const applies = state.applies === true;

  const scrollRef = useRef<HTMLDivElement | null>(null);
  const refocus = useRef(false);
  const formId = form?.controlId;
  useEffect(() => {
    if (!refocus.current || scrollRef.current === null) {
      return;
    }
    refocus.current = false;
    firstFocusable(scrollRef.current)?.focus();
  }, [formId]);

  const handleClear = useCallback(() => {
    refocus.current = true;
    sendCommand(CMD_CLEAR, {});
  }, [sendCommand]);

  return (
    <>
      <div ref={scrollRef} id={controlId} className="tl-picker__scroll">
        {form && <TLChild control={form} />}
      </div>
      <FilterFooter active={active} clearLabel={i18n['js.table.clear']} onClear={handleClear}>
        {applies && (
          <button type="button" className="tl-button tl-button--primary tl-type-label" onClick={() => sendCommand(CMD_APPLY, {})}>
            {i18n['js.table.filterApply']}
          </button>
        )}
      </FilterFooter>
    </>
  );
};

export default TLFilterForm;
