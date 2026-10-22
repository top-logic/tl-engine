/**
 * Suggestions offered while the user types: a list under an editor, computed by the server for the
 * text typed and the caret position in it, which the user may ignore or pick an entry of. Picking
 * an entry replaces a range of the text by the text of the entry.
 *
 * <p>{@link useSuggestions} holds the behaviour every editor offering suggestions shares - when to
 * ask, which answer is current, the keys of the list, picking an entry - without knowing the
 * editor: the editor names its text and caret, asks the server, and enters a picked text the way it
 * enters typed text. The list is drawn by the part `Listbox`.</p>
 */
import React from 'react';
import type { SuggestionEntryJson } from '../state/control-state';

const { useCallback, useEffect, useMemo, useRef, useState } = React;

/** The command asking a control for suggestions. Must match {@code SuggestArguments#COMMAND}. */
export const CMD_SUGGEST = 'suggest';

/**
 * The argument of {@link CMD_SUGGEST} holding the text typed. Must match
 * {@code SuggestArguments#TEXT}.
 */
export const ARG_SUGGEST_TEXT = 'text';

/**
 * The argument of {@link CMD_SUGGEST} holding the caret position in the text. Must match
 * {@code SuggestArguments#CARET}.
 */
export const ARG_SUGGEST_CARET = 'caret';

/**
 * The span the suggestions are asked for after the last keystroke: short enough that the list
 * follows the typing, long enough that a burst of keystrokes asks once.
 */
export const SUGGEST_DEBOUNCE_MS = 200;

/** The answer of the server: the suggestions, and the text and caret they were computed for. */
export interface SuggestionsAnswer {
  /** The suggestions, in the order they are offered. */
  entries: readonly SuggestionEntryJson[];

  /** The text the suggestions were computed for. */
  text: string;

  /** The caret position in {@link SuggestionsAnswer.text} the suggestions were computed for. */
  caret: number;
}

/** Options of {@link useSuggestions}. */
export interface SuggestionsOptions {
  /** The ID the IDs of the list and its entries are derived from, e.g. the ID of the editor. */
  id: string;

  /** Whether the editor offers suggestions; with `false` the hook asks nothing and shows nothing. */
  enabled: boolean;

  /** The text the editor shows. */
  text: string;

  /** The last answer of the server, `null` if none has arrived. */
  answer: SuggestionsAnswer | null;

  /** The caret position in the editor, read when the suggestions are asked for explicitly. */
  getCaret: () => number;

  /**
   * Asks the server for the suggestions to the given text with the caret at the given position.
   * The answer arrives as {@link SuggestionsOptions.answer}.
   */
  request: (text: string, caret: number) => void;

  /**
   * Enters the text resulting from picking an entry, the way the editor enters typed text, and puts
   * the caret at the given position.
   */
  apply: (text: string, caret: number) => void;

  /** The span the suggestions are asked for after the last keystroke; absent: {@link SUGGEST_DEBOUNCE_MS}. */
  debounceMs?: number;
}

/**
 * The attributes making the editor a combobox (WAI-ARIA 1.2) controlling the list of suggestions.
 */
export interface ComboboxAttributes {
  role: 'combobox';
  'aria-autocomplete': 'list';
  'aria-expanded': boolean;
  'aria-controls': string | undefined;
  'aria-activedescendant': string | undefined;
}

/** What {@link useSuggestions} gives the editor and the list. */
export interface Suggestions {
  /** Whether the list is shown: there is a current, non-empty answer the user has not dismissed. */
  open: boolean;

  /** The suggestions shown, empty while the list is closed. */
  entries: readonly SuggestionEntryJson[];

  /** The position of the active entry in {@link Suggestions.entries}, -1 if none is active. */
  activeIndex: number;

  /** The ID of the list. */
  listboxId: string;

  /** The ID of the entry at the given position. */
  optionId: (index: number) => string;

  /** The attributes of the editor, see {@link ComboboxAttributes}. */
  comboboxAttributes: ComboboxAttributes;

  /**
   * Tells that the user changed the text by typing; asks for the suggestions to the given text and
   * caret after {@link SuggestionsOptions.debounceMs}.
   */
  changed: (text: string, caret: number) => void;

  /**
   * The key handler of the editor, to be called before its own. Returns whether it handled the
   * key; it then has prevented the default action of the key and stopped its propagation, and the
   * editor does nothing more with it.
   */
  onKeyDown: (event: React.KeyboardEvent) => boolean;

  /** Picks the entry at the given position. */
  pick: (index: number) => void;

  /** Makes the entry at the given position the active one, e.g. while the pointer rests on it. */
  activate: (index: number) => void;

  /** Closes the list until the suggestions are asked for again, e.g. when the editor is left. */
  dismiss: () => void;
}

/**
 * The text resulting from picking the given suggestion in the given text, with the caret behind the
 * inserted text.
 */
export function applySuggestion(text: string, entry: SuggestionEntryJson): { text: string; caret: number } {
  return {
    text: text.substring(0, entry.from) + entry.text + text.substring(entry.to),
    caret: entry.from + entry.text.length,
  };
}

/**
 * The suggestions of an editor: asks for them while the user types and shows the current answer.
 *
 * <p><b>Asking.</b> Each change of the text by typing ({@link Suggestions.changed}) asks for the
 * suggestions to the new text and caret once the user pauses ({@link SuggestionsOptions.debounceMs}).
 * Alt+ArrowDown and Ctrl+Space ask at once, for the text and caret as they are. Picking an entry
 * asks at once for the resulting text and caret, so that the next suggestions follow.</p>
 *
 * <p><b>Showing.</b> The list shows an answer computed for exactly the text the editor shows and the
 * caret it was last asked for. Any other answer is outdated - the user typed on, and a newer
 * question is under way - and the list stays closed until that arrives. An empty answer closes the
 * list. The entries are shown as the server gives them.</p>
 *
 * <p><b>Keys while the list is open.</b> ArrowDown and ArrowUp move the active entry, wrapping
 * around at the ends; no entry is active when the list opens. Enter picks the active entry; without
 * an active entry, Enter is left to the editor (a new line, the end of the edit). Escape closes the
 * list. A handled key goes no further: Enter does not submit the form around, Escape does not close
 * the dialog around. ArrowLeft, ArrowRight, Home and End move the caret away from where the list was
 * computed for and close it. Every other key types on.</p>
 */
export function useSuggestions(options: SuggestionsOptions): Suggestions {
  const { id, enabled, text, answer, debounceMs = SUGGEST_DEBOUNCE_MS } = options;
  const optionsRef = useRef(options);
  optionsRef.current = options;

  // The caret the suggestions were last asked for: the answer must be computed for it.
  const [caret, setCaret] = useState<number | null>(null);
  // Closed by the user since the suggestions were last asked for.
  const [dismissed, setDismissed] = useState(false);
  const [activeIndex, setActiveIndex] = useState(-1);
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const cancelTimer = useCallback(() => {
    if (timerRef.current !== null) {
      clearTimeout(timerRef.current);
      timerRef.current = null;
    }
  }, []);

  useEffect(() => cancelTimer, [cancelTimer]);

  const requestNow = useCallback((t: string, c: number) => {
    cancelTimer();
    setCaret(c);
    setDismissed(false);
    optionsRef.current.request(t, c);
  }, [cancelTimer]);

  const changed = useCallback((t: string, c: number) => {
    if (!optionsRef.current.enabled) return;
    cancelTimer();
    // The list computed for the caret before is outdated from now on.
    setCaret(c);
    timerRef.current = setTimeout(() => {
      timerRef.current = null;
      setDismissed(false);
      optionsRef.current.request(t, c);
    }, debounceMs);
  }, [cancelTimer, debounceMs]);

  const current = enabled && answer !== null && answer.text === text && answer.caret === caret;
  const entries = current ? answer.entries : NO_ENTRIES;
  const open = current && !dismissed && entries.length > 0;

  // A new list starts without an active entry.
  useEffect(() => {
    setActiveIndex(-1);
  }, [entries]);

  const listboxId = id + '-suggestions';
  const optionId = useCallback((index: number) => listboxId + '-' + index, [listboxId]);

  const pick = useCallback((index: number) => {
    const entry = entries[index];
    if (entry === undefined) return;
    const result = applySuggestion(text, entry);
    optionsRef.current.apply(result.text, result.caret);
    requestNow(result.text, result.caret);
  }, [entries, text, requestNow]);

  const dismiss = useCallback(() => {
    cancelTimer();
    setDismissed(true);
  }, [cancelTimer]);

  const onKeyDown = useCallback((event: React.KeyboardEvent): boolean => {
    if (!optionsRef.current.enabled) return false;
    const handled = () => {
      event.preventDefault();
      event.stopPropagation();
      return true;
    };
    const plain = !event.altKey && !event.ctrlKey && !event.metaKey && !event.shiftKey;
    if ((event.key === 'ArrowDown' && event.altKey && !event.ctrlKey && !event.metaKey)
      || (event.key === ' ' && event.ctrlKey && !event.altKey && !event.metaKey)) {
      requestNow(text, optionsRef.current.getCaret());
      return handled();
    }
    if (!open) return false;
    switch (event.key) {
      case 'ArrowDown':
        if (!plain) return false;
        setActiveIndex(index => (index + 1) % entries.length);
        return handled();
      case 'ArrowUp':
        if (!plain) return false;
        setActiveIndex(index => (index <= 0 ? entries.length : index) - 1);
        return handled();
      case 'Enter':
        if (!plain || activeIndex < 0) return false;
        pick(activeIndex);
        return handled();
      case 'Escape':
        dismiss();
        return handled();
      case 'ArrowLeft':
      case 'ArrowRight':
      case 'Home':
      case 'End':
        dismiss();
        return false;
      default:
        return false;
    }
  }, [open, entries, activeIndex, text, pick, dismiss, requestNow]);

  const activeId = open && activeIndex >= 0 ? optionId(activeIndex) : undefined;
  const comboboxAttributes = useMemo<ComboboxAttributes>(() => ({
    role: 'combobox',
    'aria-autocomplete': 'list',
    'aria-expanded': open,
    'aria-controls': open ? listboxId : undefined,
    'aria-activedescendant': activeId,
  }), [open, listboxId, activeId]);

  return {
    open,
    entries,
    activeIndex: open ? activeIndex : -1,
    listboxId,
    optionId,
    comboboxAttributes,
    changed,
    onKeyDown,
    pick,
    activate: setActiveIndex,
    dismiss,
  };
}

/** The entries of a closed list, one instance, so that it does not reset the active entry. */
const NO_ENTRIES: readonly SuggestionEntryJson[] = Object.freeze([]);
