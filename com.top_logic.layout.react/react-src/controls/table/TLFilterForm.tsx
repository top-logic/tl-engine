import { React, useTLState, useTLCommand, useI18N, TLChild, usePart } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { FilterFooter, firstFieldOrFocusable } from './TLFilterList';

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
 * The filter UI of an application applies explicitly (state `applies`): its footer offers "Apply" at its end, and Enter
 * in a field of the form applies as well.</p>
 *
 * <p>The scroll area keeps a margin (tl-filter-list__form) that holds the focus rings of the fields at its edges.</p>
 *
 * <p>Removing the filter replaces the form by a new one. The button removing it is disabled by its own click, and the
 * form it stood under is gone: the focus goes to the first field of the new form.</p>
 */
const TLFilterForm: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);
  const Button = usePart('Button');

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
    firstFieldOrFocusable(scrollRef.current)?.focus();
  }, [formId]);

  const handleClear = useCallback(() => {
    refocus.current = true;
    sendCommand(CMD_CLEAR, {});
  }, [sendCommand]);

  // The filter UI of an application applies on Enter in a field, as the dialog it replaces did - not in a text area,
  // where Enter is text, nor on a button, which Enter presses, nor where a field has used the key itself (a list
  // choosing its option).
  const handleKeyDown = useCallback((event: React.KeyboardEvent<HTMLDivElement>) => {
    if (!applies || event.key !== 'Enter' || event.defaultPrevented || event.nativeEvent.isComposing
        || event.shiftKey || event.ctrlKey || event.altKey || event.metaKey) {
      return;
    }
    const target = event.target as HTMLElement;
    if (target.closest('textarea, button, a[href], [contenteditable="true"]')) {
      return;
    }
    event.preventDefault();
    sendCommand(CMD_APPLY, {});
  }, [applies, sendCommand]);

  return (
    <>
      <div ref={scrollRef} id={controlId} className="tl-picker__scroll tl-filter-list__form" onKeyDown={handleKeyDown}>
        {form && <TLChild control={form} />}
      </div>
      <FilterFooter active={active} clearLabel={i18n['js.table.clear']} onClear={handleClear}>
        {applies && (
          <Button
            label={i18n['js.table.filterApply']}
            display="label-only"
            appearance="primary"
            onClick={() => sendCommand(CMD_APPLY, {})}
          />
        )}
      </FilterFooter>
    </>
  );
};

export default TLFilterForm;
