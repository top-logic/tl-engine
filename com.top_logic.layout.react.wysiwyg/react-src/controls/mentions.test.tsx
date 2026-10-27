import { afterAll, afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { React } from 'tl-react-bridge';
import { createRoot } from 'react-dom/client';
import type { Root } from 'react-dom/client';
import { Editor } from '@tiptap/core';
import StarterKit from '@tiptap/starter-kit';
import { HtmlPassthrough } from './HtmlPassthrough';
import { ObjectLink } from './ObjectLink';
import {
  ObjectMention, useMentions, OBJECT_MENTION, PRESENTATION_CHIP, PRESENTATION_LINK,
} from './mentions';
import type { MentionAnswer, MentionConfig, Mentions } from './mentions';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

afterAll(async () => {
  // The bridge entry adds a tooltip host to the page. It goes while the test environment exists, so
  // that the DOM observer of the bridge does not see its removal by the teardown of the environment.
  document.body.replaceChildren();
  await Promise.resolve();
});

/** The extensions of the editor the mentions are tried in, as TLWysiwygEditor combines them. */
function baseExtensions() {
  return [
    HtmlPassthrough,
    StarterKit.configure({ link: false, trailingNode: false }),
    ObjectLink.configure({ openOnClick: false }),
    ObjectMention,
  ];
}

/** A stored chip. */
const CHIP = '<a class="tlObject" data-mention="@" href="?x">Anna</a>';

/** A stored object link without a mention. */
const OBJECT_LINK = '<a class="tlObject" href="?y">Text</a>';

/** The mentions the editor offers: people as chips, tickets as links. */
const MENTIONS: MentionConfig[] = [
  { trigger: '@', presentation: PRESENTATION_CHIP },
  { trigger: '#', presentation: PRESENTATION_LINK },
];

/** The answer to the mention `@an`. */
const ANNA: MentionAnswer = { trigger: '@', query: 'an', entries: [{ label: 'Anna', link: '?x' }, { label: 'Andreas', link: '?z' }] };

/** The answer to the mention `#ti`. */
const TICKET: MentionAnswer = { trigger: '#', query: 'ti', entries: [{ label: 'Ticket 1', link: '?t' }] };

describe('ObjectMention', () => {
  let editor: Editor | null = null;

  afterEach(() => {
    editor?.destroy();
    editor = null;
  });

  function open(html: string): Editor {
    editor = new Editor({ extensions: baseExtensions(), content: html });
    return editor;
  }

  it('opens a stored chip as a chip and writes it back unchanged', () => {
    const ed = open('<p>Hi ' + CHIP + '</p>');
    const paragraph = ed.getJSON().content?.[0];
    expect(paragraph?.content?.map((node) => node.type)).toEqual(['text', OBJECT_MENTION]);
    expect(ed.getHTML()).toBe('<p>Hi ' + CHIP + '</p>');
  });

  it('keeps an object link without mention an ordinary link', () => {
    const ed = open('<p>See ' + OBJECT_LINK + '</p>');
    const nodes = ed.getJSON().content?.[0].content ?? [];
    expect(nodes.map((node) => node.type)).toEqual(['text', 'text']);
    const link = nodes[1];
    expect((link as { text?: string }).text).toBe('Text');
    expect(link.marks?.map((mark) => mark.type)).toEqual(['link']);
    expect(link.marks?.[0].attrs).toMatchObject({ href: '?y', class: 'tlObject' });
    expect(ed.getHTML()).toBe('<p>See ' + OBJECT_LINK + '</p>');
  });

  it('gives an ordinary web link the target and relation of the link extension', () => {
    const ed = open('<p>See <a href="https://example.com">web</a></p>');
    expect(ed.getHTML()).toBe(
      '<p>See <a target="_blank" rel="noopener noreferrer nofollow" href="https://example.com">web</a></p>');
  });

  it('removes a chip with Backspace as a whole', () => {
    const ed = open('<p>Hi ' + CHIP + '</p>');
    ed.commands.focus('end');
    ed.view.dom.dispatchEvent(new KeyboardEvent('keydown', { key: 'Backspace', bubbles: true, cancelable: true }));
    expect(ed.getHTML()).toBe('<p>Hi </p>');
  });
});

describe('useMentions', () => {
  /** What the harness rendered last. */
  let current: { editor: Editor | null; mentions: Mentions | null };
  let container: HTMLDivElement;
  let root: Root;
  let request: ReturnType<typeof vi.fn<(trigger: string, query: string) => void>>;

  /**
   * An editor offering {@link MENTIONS}, with the given answer of the server. The editor is the one
   * of the library itself: the React binding of the library would bring a React of its own here.
   */
  const Harness: React.FC<{ answer: MentionAnswer | null }> = ({ answer }) => {
    const mentions = useMentions({ id: 'ed', mentions: MENTIONS, answer, request });
    const elementRef = React.useRef<HTMLDivElement>(null);
    current.mentions = mentions;
    React.useEffect(() => {
      const editor = new Editor({
        element: elementRef.current,
        extensions: [...baseExtensions(), ...mentions.extensions],
        content: '<p></p>',
      });
      current.editor = editor;
      return () => {
        current.editor = null;
        editor.destroy();
      };
    }, [mentions.extensions]);
    return <div ref={elementRef} />;
  };

  beforeEach(() => {
    globalThis.IS_REACT_ACT_ENVIRONMENT = true;
    vi.useFakeTimers();
    request = vi.fn<(trigger: string, query: string) => void>();
    current = { editor: null, mentions: null };
    container = document.createElement('div');
    document.body.appendChild(container);
    root = createRoot(container);
  });

  afterEach(async () => {
    React.act(() => root.unmount());
    container.remove();
    vi.useRealTimers();
    // The bridge observes the removal of the container; its observer runs before the document goes.
    await Promise.resolve();
  });

  async function render(answer: MentionAnswer | null) {
    await React.act(async () => root.render(<Harness answer={answer} />));
  }

  function editor(): Editor {
    const ed = current.editor;
    if (ed === null) throw new Error('No editor.');
    return ed;
  }

  function mentions(): Mentions {
    const m = current.mentions;
    if (m === null) throw new Error('No mentions.');
    return m;
  }

  /** Types the given text at the cursor, character by character. */
  async function type(text: string) {
    for (const ch of text) {
      await React.act(async () => {
        editor().commands.insertContent(ch);
      });
    }
  }

  /** Waits the given span out. */
  async function wait(ms: number) {
    await React.act(async () => {
      vi.advanceTimersByTime(ms);
    });
  }

  /** Presses the given key in the editor. */
  async function press(key: string) {
    await React.act(async () => {
      editor().view.dom.dispatchEvent(new KeyboardEvent('keydown', { key, bubbles: true, cancelable: true }));
    });
  }

  it('asks nothing while ordinary text is typed', async () => {
    await render(null);
    editor().commands.focus(null, { scrollIntoView: false });
    await type('Hello mail@example.com and more');
    await wait(1000);
    expect(request).not.toHaveBeenCalled();
    expect(mentions().list).toBeNull();
  });

  it('asks with the trigger and the query alone once the typing pauses', async () => {
    await render(null);
    editor().commands.focus(null, { scrollIntoView: false });
    await type('Hi @an');
    await wait(1000);
    expect(request).toHaveBeenCalledTimes(1);
    expect(request).toHaveBeenCalledWith('@', 'an');
  });

  it('shows only the answer to the mention typed', async () => {
    await render(null);
    editor().commands.focus(null, { scrollIntoView: false });
    await type('Hi @an');

    await render({ ...ANNA, query: 'a' });
    expect(mentions().list).toBeNull();

    await render({ ...ANNA, trigger: '#' });
    expect(mentions().list).toBeNull();

    await render(ANNA);
    expect(mentions().list?.entries.map((entry) => entry.label)).toEqual(['Anna', 'Andreas']);
    expect(mentions().ariaAttributes).toMatchObject({ 'aria-expanded': 'true', 'aria-controls': 'ed-mentions' });
  });

  it('replaces trigger and query by a chip and a space', async () => {
    await render(ANNA);
    editor().commands.focus(null, { scrollIntoView: false });
    await type('Hi @an');
    await render(ANNA);

    await press('ArrowDown');
    expect(mentions().list?.activeIndex).toBe(0);
    expect(mentions().ariaAttributes['aria-activedescendant']).toBe('ed-mentions-0');
    await press('Enter');

    expect(editor().getHTML()).toBe('<p>Hi ' + CHIP + ' </p>');
    expect(mentions().list).toBeNull();
  });

  it('replaces trigger and query by the linked label and a space', async () => {
    await render(TICKET);
    editor().commands.focus(null, { scrollIntoView: false });
    await type('See #ti');
    await render(TICKET);

    await React.act(async () => mentions().list?.onPick(0));

    expect(editor().getHTML()).toBe('<p>See <a class="tlObject" href="?t">Ticket 1</a> </p>');
    await type('x');
    expect(editor().getHTML()).toBe('<p>See <a class="tlObject" href="?t">Ticket 1</a> x</p>');
  });

  it('moves the active entry around, and leaves Enter to the editor without one', async () => {
    await render(ANNA);
    editor().commands.focus(null, { scrollIntoView: false });
    await type('Hi @an');
    await render(ANNA);

    await press('ArrowUp');
    expect(mentions().list?.activeIndex).toBe(1);
    await press('ArrowDown');
    expect(mentions().list?.activeIndex).toBe(0);
    await press('Escape');
    expect(mentions().list).toBeNull();
    expect(editor().getHTML()).toBe('<p>Hi @an</p>');
  });
});
