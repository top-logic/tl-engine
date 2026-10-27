/*
 * References to application objects typed into the text: a mention.
 *
 * Typing the trigger character of a mention (e.g. `@`) at the start of a word starts the mention;
 * the text typed after it is the query. While a mention is typed, the editor asks the server for
 * the objects matching the query - with the trigger and the query alone, after a pause in typing -
 * and offers them in the part `Listbox` at the trigger. Picking an object replaces trigger and
 * query by a reference to the object, followed by a space. Ordinary typing asks nothing.
 *
 * A reference is an object link (`<a class="tlObject" href="...">label</a>`), presented in one of
 * two ways, as the mention is configured:
 *
 * - `chip`: a unit standing for the object ({@link ObjectMention}), removed as a whole, its text
 *   not editable. It is stored with the trigger in the attribute {@link DATA_MENTION}, so that a
 *   stored chip opens as a chip again.
 * - `link`: an ordinary link whose text is edited like the text around it.
 */
import { React, usePart, SUGGEST_DEBOUNCE_MS } from 'tl-react-bridge';
import type { ListboxEntry, ListboxPartProps } from 'tl-react-bridge';
import { Extension, Node, mergeAttributes } from '@tiptap/react';
import type { Editor, Range } from '@tiptap/react';
import { PluginKey } from '@tiptap/pm/state';
import { Suggestion, exitSuggestion } from '@tiptap/suggestion';
import { TL_OBJECT } from './ObjectLink';

const { useCallback, useEffect, useMemo, useRef, useState } = React;

/**
 * State listing the mentions the editor offers, each a {@link MentionConfig}; absent for an editor
 * offering none. Must match {@code ReactWysiwygControl#MENTIONS}.
 */
export const STATE_MENTIONS = 'mentions';

/** Field of a mention holding its trigger character. Must match {@code ReactWysiwygControl#MENTION_TRIGGER}. */
export const MENTION_TRIGGER = 'trigger';

/**
 * Field of a mention holding the presentation of the references it inserts. Must match
 * {@code ReactWysiwygControl#MENTION_PRESENTATION}.
 */
export const MENTION_PRESENTATION = 'presentation';

/** The presentation of a reference as a chip. Must match {@code MentionPresentation#CHIP}. */
export const PRESENTATION_CHIP = 'chip';

/** The presentation of a reference as an ordinary link. Must match {@code MentionPresentation#LINK}. */
export const PRESENTATION_LINK = 'link';

/** The command asking for the objects of a mention. Must match {@code MentionArguments#COMMAND}. */
export const CMD_MENTION = 'mention';

/** The {@link CMD_MENTION} argument holding the trigger character. Must match {@code MentionArguments#TRIGGER}. */
export const ARG_TRIGGER = 'trigger';

/** The {@link CMD_MENTION} argument holding the text typed after the trigger. Must match {@code MentionArguments#QUERY}. */
export const ARG_QUERY = 'query';

/**
 * State holding the answer to the last {@link CMD_MENTION} command, a {@link MentionAnswer}. Must
 * match {@code ReactWysiwygControl#MENTION_OPTIONS}.
 */
export const STATE_MENTION_OPTIONS = 'mentionOptions';

/** Field of the answer holding the trigger it is for. Must match {@code ReactWysiwygControl#OPTIONS_TRIGGER}. */
export const OPTIONS_TRIGGER = 'trigger';

/** Field of the answer holding the query it is for. Must match {@code ReactWysiwygControl#OPTIONS_QUERY}. */
export const OPTIONS_QUERY = 'query';

/** Field of the answer listing the objects offered. Must match {@code ReactWysiwygControl#OPTIONS_ENTRIES}. */
export const OPTIONS_ENTRIES = 'entries';

/** Field of an offered object holding its label. Must match {@code ReactWysiwygControl#ENTRY_LABEL}. */
export const ENTRY_LABEL = 'label';

/** Field of an offered object holding the destination of a link to it. Must match {@code ReactWysiwygControl#ENTRY_LINK}. */
export const ENTRY_LINK = 'link';

/** Attribute of the anchor of a chip holding the trigger of the mention that inserted it. */
export const DATA_MENTION = 'data-mention';

/** Node of a reference presented as a chip. */
export const OBJECT_MENTION = 'objectMention';

/** Name of the mark of a reference presented as a link: the link of the editor. */
const LINK = 'link';

/** Attribute of {@link OBJECT_MENTION} holding the destination of the link. */
const HREF = 'href';

/** Attribute of {@link OBJECT_MENTION} holding the label shown. */
const LABEL = 'label';

/**
 * Priority of the rule reading a chip: before the link mark, which takes every anchor, and after
 * the rule taking an element that acts in the page as opaque source (see HtmlPassthrough).
 */
const MENTION_PRIORITY = 100;

/** Prefix of the keys of the suggestion plugins, one per trigger. */
const PLUGIN_KEY_PREFIX = 'mention';

/** How a mention presents the references it inserts. */
export type MentionPresentation = typeof PRESENTATION_CHIP | typeof PRESENTATION_LINK;

/** A mention the editor offers, as announced in {@link STATE_MENTIONS}. */
export interface MentionConfig {
  [MENTION_TRIGGER]: string;
  [MENTION_PRESENTATION]: MentionPresentation;
}

/** An object offered for a mention. */
export interface MentionEntry {
  [ENTRY_LABEL]: string;
  [ENTRY_LINK]: string;
}

/** The answer of the server to {@link CMD_MENTION}, see {@link STATE_MENTION_OPTIONS}. */
export interface MentionAnswer {
  [OPTIONS_TRIGGER]: string;
  [OPTIONS_QUERY]: string;
  [OPTIONS_ENTRIES]: MentionEntry[];
}

/** The mention being typed. */
export interface TypedMention {
  /** The mention. */
  mention: MentionConfig;

  /** The text typed after the trigger. */
  query: string;

  /** The range of the text holding trigger and query, replaced by the reference picked. */
  range: Range;

  /** The editor the mention is typed in. */
  editor: Editor;

  /** The element the list of objects stands at: the trigger and the query typed. */
  anchor: Element;

  /** Ends the mention without a reference, leaving the text typed as it is. */
  dismiss: () => void;
}

/** What the suggestion plugins tell about the mention typed. */
export interface MentionListener {
  /** The mention being typed, `null` when none is typed any more. */
  changed: (typed: TypedMention | null) => void;

  /** A key pressed while a mention is typed; returns whether it is handled. */
  keyDown: (event: KeyboardEvent) => boolean;
}

/** The mentions announced in the given state value, without malformed entries. */
export function mentionConfigs(value: unknown): MentionConfig[] {
  if (!Array.isArray(value)) {
    return [];
  }
  return value.filter((entry): entry is MentionConfig => !!entry
    && typeof entry[MENTION_TRIGGER] === 'string' && entry[MENTION_TRIGGER] !== ''
    && (entry[MENTION_PRESENTATION] === PRESENTATION_CHIP || entry[MENTION_PRESENTATION] === PRESENTATION_LINK));
}

/**
 * A reference to an object presented as a chip: an anchor naming the object, inserted and removed
 * as a whole. Stored as `<a class="tlObject" data-mention="<trigger>" href="<link>">label</a>`.
 */
export const ObjectMention = Node.create({
  name: OBJECT_MENTION,
  group: 'inline',
  inline: true,
  atom: true,
  selectable: true,

  addAttributes() {
    return {
      [DATA_MENTION]: {
        default: null,
        parseHTML: (element: HTMLElement) => element.getAttribute(DATA_MENTION),
      },
      [HREF]: {
        default: null,
        parseHTML: (element: HTMLElement) => element.getAttribute(HREF),
      },
      [LABEL]: {
        default: '',
        rendered: false,
        parseHTML: (element: HTMLElement) => element.textContent || '',
      },
    };
  },

  parseHTML() {
    return [{ tag: 'a.' + TL_OBJECT + '[' + DATA_MENTION + ']', priority: MENTION_PRIORITY }];
  },

  renderHTML({ node, HTMLAttributes }) {
    return ['a', mergeAttributes({ class: TL_OBJECT }, HTMLAttributes), String(node.attrs[LABEL])];
  },

  renderText({ node }) {
    return String(node.attrs[LABEL]);
  },

  addKeyboardShortcuts() {
    return {
      // The chip goes as a whole, whatever the browser would do with the uneditable element.
      Backspace: ({ editor }) => {
        const { selection } = editor.state;
        const before = selection.empty ? selection.$from.nodeBefore : null;
        if (before === null || before.type.name !== OBJECT_MENTION) {
          return false;
        }
        return editor.commands.deleteRange({ from: selection.from - before.nodeSize, to: selection.from });
      },
    };
  },
});

/**
 * The extension detecting the mentions typed: a suggestion plugin per trigger, reporting to the
 * given listener.
 */
export function mentionSuggestions(mentions: readonly MentionConfig[], listener: MentionListener) {
  return Extension.create({
    name: 'mentionSuggestions',

    addProseMirrorPlugins() {
      const editor = this.editor;
      return mentions.map((mention) => {
        const pluginKey = new PluginKey(PLUGIN_KEY_PREFIX + mention[MENTION_TRIGGER]);
        const typed = (range: Range, query: string, decoration: Element | null): TypedMention => ({
          mention,
          query,
          range,
          editor,
          anchor: decoration || editor.view.dom,
          dismiss: () => {
            if (!editor.isDestroyed) {
              exitSuggestion(editor.view, pluginKey);
            }
          },
        });
        return Suggestion({
          editor,
          pluginKey,
          char: mention[MENTION_TRIGGER],
          render: () => ({
            onStart: (props) => listener.changed(typed(props.range, props.query, props.decorationNode)),
            onUpdate: (props) => listener.changed(typed(props.range, props.query, props.decorationNode)),
            onExit: () => listener.changed(null),
            onKeyDown: ({ event }) => listener.keyDown(event),
          }),
        });
      });
    },
  });
}

/**
 * Replaces the trigger and query of the given mention by a reference to the given object, followed
 * by a space, and puts the cursor behind it.
 */
export function insertMention(typed: TypedMention, entry: MentionEntry): void {
  const label = entry[ENTRY_LABEL];
  const link = entry[ENTRY_LINK];
  const reference = typed.mention[MENTION_PRESENTATION] === PRESENTATION_CHIP
    ? { type: OBJECT_MENTION, attrs: { [DATA_MENTION]: typed.mention[MENTION_TRIGGER], [HREF]: link, [LABEL]: label } }
    : { type: 'text', text: label, marks: [{ type: LINK, attrs: { href: link, class: TL_OBJECT } }] };
  // The space carries no mark, so that typing on does not extend a link.
  typed.editor.chain().focus().insertContentAt(typed.range, [reference, { type: 'text', text: ' ' }]).run();
}

/** Options of {@link useMentions}. */
export interface MentionsOptions {
  /** The ID the IDs of the list and its entries are derived from, e.g. the ID of the editor. */
  id: string;

  /** The mentions offered; none: no mention is detected. */
  mentions: readonly MentionConfig[];

  /** The last answer of the server, `null` if none has arrived. */
  answer: MentionAnswer | null;

  /** Asks the server for the objects of the mention with the given trigger matching the given query. */
  request: (trigger: string, query: string) => void;

  /** The span the objects are asked for after the last keystroke; absent: the one of the suggestions of a text box, {@link SUGGEST_DEBOUNCE_MS}. */
  debounceMs?: number;
}

/** What {@link useMentions} gives the editor and the list. */
export interface Mentions {
  /** The extensions detecting the mentions typed, empty when no mention is offered. */
  extensions: Extension[];

  /** Whether the list is shown: there is a non-empty answer to the mention currently typed. */
  open: boolean;

  /** The props of the part `Listbox` showing the objects, `null` while the list is closed. */
  list: Omit<ListboxPartProps, 'aria-labelledby'> | null;

  /** The attributes of the editable element while the list is open, empty otherwise. */
  ariaAttributes: Record<string, string>;

  /** Ends the mention typed, if any, e.g. when the editor is left. */
  dismiss: () => void;
}

/**
 * The mentions of an editor: detects a mention typed, asks the server for its objects, shows the
 * answer and inserts the object picked.
 *
 * <p>The answer shown is the one computed for exactly the trigger and the query typed; any other
 * answer is outdated, and the list stays closed until the current one arrives. While the list is
 * open, ArrowDown and ArrowUp move the active entry, wrapping around at the ends; no entry is
 * active when the list opens. Enter picks the active entry; without an active entry, Enter is left
 * to the editor. Escape ends the mention and closes the list.</p>
 */
export function useMentions(options: MentionsOptions): Mentions {
  const { id, mentions, answer, debounceMs = SUGGEST_DEBOUNCE_MS } = options;
  const optionsRef = useRef(options);
  optionsRef.current = options;

  const [typed, setTyped] = useState<TypedMention | null>(null);
  const [activeIndex, setActiveIndex] = useState(-1);

  const trigger = typed === null ? null : typed.mention[MENTION_TRIGGER];
  const query = typed === null ? null : typed.query;

  // The objects are asked for once the typing pauses, for the trigger and the query alone.
  useEffect(() => {
    if (trigger === null || query === null) {
      return undefined;
    }
    const timer = setTimeout(() => optionsRef.current.request(trigger, query), debounceMs);
    return () => clearTimeout(timer);
  }, [trigger, query, debounceMs]);

  const current = answer !== null && trigger !== null
    && answer[OPTIONS_TRIGGER] === trigger && answer[OPTIONS_QUERY] === query;
  const entries = current ? answer[OPTIONS_ENTRIES] : NO_ENTRIES;
  const open = entries.length > 0;

  // A new list starts without an active entry.
  useEffect(() => {
    setActiveIndex(-1);
  }, [entries]);

  const listboxId = id + '-mentions';
  const optionId = useCallback((index: number) => listboxId + '-' + index, [listboxId]);

  const pick = useCallback((index: number) => {
    const entry = entries[index];
    if (typed === null || entry === undefined) {
      return;
    }
    insertMention(typed, entry);
  }, [typed, entries]);

  const typedRef = useRef(typed);
  typedRef.current = typed;
  const dismiss = useCallback(() => typedRef.current?.dismiss(), []);

  // The key handler of the plugins reads the list as it is when the key is pressed.
  const keysRef = useRef({ open, entries, activeIndex, pick });
  keysRef.current = { open, entries, activeIndex, pick };

  const listener = useMemo<MentionListener>(() => ({
    changed: setTyped,
    keyDown: (event: KeyboardEvent) => {
      const keys = keysRef.current;
      if (!keys.open) {
        return false;
      }
      const plain = !event.altKey && !event.ctrlKey && !event.metaKey && !event.shiftKey;
      const handled = () => {
        // The key is the list's: Enter does not submit the form around, Escape does not close the
        // dialog around.
        event.stopPropagation();
        return true;
      };
      switch (event.key) {
        case 'ArrowDown':
          if (!plain) return false;
          setActiveIndex((index) => (index + 1) % keys.entries.length);
          return handled();
        case 'ArrowUp':
          if (!plain) return false;
          setActiveIndex((index) => (index <= 0 ? keys.entries.length : index) - 1);
          return handled();
        case 'Enter':
          if (!plain || keys.activeIndex < 0) return false;
          keys.pick(keys.activeIndex);
          return handled();
        case 'Escape':
          // The plugin ends the mention.
          return handled();
        default:
          return false;
      }
    },
  }), []);

  // The configuration of the mentions, which the extensions are built for.
  const mentionsKey = JSON.stringify(mentions);
  const extensions = useMemo(
    () => mentions.length === 0 ? [] : [mentionSuggestions(mentions, listener)],
    [mentionsKey, listener]);

  const listEntries = useMemo<ListboxEntry[]>(
    () => entries.map((entry, index) => ({ id: optionId(index), label: entry[ENTRY_LABEL] })),
    [entries, optionId]);

  const activeId = open && activeIndex >= 0 ? optionId(activeIndex) : undefined;
  const ariaAttributes = useMemo<Record<string, string>>(() => {
    if (!open) {
      return NO_ATTRIBUTES;
    }
    const attributes: Record<string, string> = {
      'aria-autocomplete': 'list',
      'aria-expanded': 'true',
      'aria-controls': listboxId,
    };
    if (activeId !== undefined) {
      attributes['aria-activedescendant'] = activeId;
    }
    return attributes;
  }, [open, listboxId, activeId]);

  const list = open && typed !== null
    ? {
      id: listboxId,
      anchor: typed.anchor,
      entries: listEntries,
      activeIndex,
      onPick: pick,
      onActivate: setActiveIndex,
      onDismiss: dismiss,
    }
    : null;

  return { extensions, open, list, ariaAttributes, dismiss };
}

/** The list of objects offered for a mention, drawn by the part `Listbox`. */
export const MentionList: React.FC<ListboxPartProps> = (props) => {
  const Listbox = usePart('Listbox');
  return <Listbox {...props} />;
};

/** The entries of a closed list, one instance, so that it does not reset the active entry. */
const NO_ENTRIES: readonly MentionEntry[] = Object.freeze([]);

/** The attributes of the editable element while no list is shown. */
const NO_ATTRIBUTES: Record<string, string> = Object.freeze({}) as Record<string, string>;
