import { afterAll, afterEach, describe, expect, it, vi } from 'vitest';
import { Editor } from '@tiptap/core';
import { NodeSelection } from '@tiptap/pm/state';
import StarterKit from '@tiptap/starter-kit';
import { HtmlPassthrough } from './HtmlPassthrough';
import { FollowObjectLinks, ObjectLink } from './ObjectLink';
import { ObjectMention } from './mentions';

/** A paragraph with a chip, an object link and an ordinary link (to a place in the page, which jsdom can follow). */
const CONTENT = '<p>Hi <a class="tlObject" data-mention="@" href="?x">Anna</a> see '
  + '<a class="tlObject" href="?y">Text</a> or <a href="#web">web</a></p>';

afterAll(async () => {
  // The bridge entry adds a tooltip host to the page. It goes while the test environment exists, so
  // that the DOM observer of the bridge does not see its removal by the teardown of the environment.
  document.body.replaceChildren();
  await Promise.resolve();
});

describe('FollowObjectLinks', () => {
  let editor: Editor | null = null;
  let element: HTMLDivElement | null = null;

  afterEach(async () => {
    editor?.destroy();
    editor = null;
    element?.remove();
    element = null;
    // The bridge (loaded with the mentions) observes the removal; its observer runs while the
    // document exists.
    await Promise.resolve();
  });

  function open(follow: (href: string) => void): Editor {
    element = document.createElement('div');
    document.body.appendChild(element);
    editor = new Editor({
      element,
      extensions: [
        HtmlPassthrough,
        StarterKit.configure({ link: false, trailingNode: false }),
        ObjectLink.configure({ openOnClick: false }),
        ObjectMention,
        FollowObjectLinks.configure({ follow }),
      ],
      content: CONTENT,
    });
    // The cursor stands at the start of the text.
    editor.commands.setTextSelection(1);
    return editor;
  }

  /**
   * Clicks the given element of the given editor the way a pointer does, with the given modifiers.
   * The pointer stands at the start of the element (jsdom has no layout to find it).
   */
  function click(ed: Editor, target: Element, init: MouseEventInit): { defaultPrevented: boolean } {
    ed.view.posAtCoords = () => ({ pos: ed.view.posAtDOM(target, 0), inside: -1 });
    const options = { bubbles: true, cancelable: true, button: 0, ...init };
    const down = new MouseEvent('mousedown', options);
    target.dispatchEvent(down);
    target.dispatchEvent(new MouseEvent('mouseup', options));
    const up = new MouseEvent('click', options);
    target.dispatchEvent(up);
    return { defaultPrevented: down.defaultPrevented && up.defaultPrevented };
  }

  function anchor(ed: Editor, selector: string): Element {
    const found = ed.view.dom.querySelector(selector);
    if (found === null) throw new Error('No ' + selector);
    return found;
  }

  it.each([
    ['a chip', 'a[data-mention]', '?x'],
    ['an object link', 'a.tlObject:not([data-mention])', '?y'],
  ])('follows %s with Ctrl held and leaves the selection alone', (_name, selector, href) => {
    const follow = vi.fn<(href: string) => void>();
    const ed = open(follow);
    const before = ed.state.selection;

    const result = click(ed, anchor(ed, selector), { ctrlKey: true });

    expect(follow).toHaveBeenCalledTimes(1);
    expect(follow).toHaveBeenCalledWith(href);
    expect(result.defaultPrevented).toBe(true);
    expect(ed.state.selection).not.toBeInstanceOf(NodeSelection);
    expect(ed.state.selection.eq(before)).toBe(true);
  });

  it('follows an object link with Cmd held', () => {
    const follow = vi.fn<(href: string) => void>();
    const ed = open(follow);
    click(ed, anchor(ed, 'a.tlObject:not([data-mention])'), { metaKey: true });
    expect(follow).toHaveBeenCalledWith('?y');
  });

  // ProseMirror's own handling of a press (placing the cursor, selecting the paragraph on a Ctrl
  // click) needs a layout jsdom does not have; what is asserted is that the press reaches it.
  it('leaves a plain click on an object link and a Ctrl click on a web link to the editor', () => {
    const follow = vi.fn<(href: string) => void>();
    const ed = open(follow);
    expect(click(ed, anchor(ed, 'a.tlObject:not([data-mention])'), {}).defaultPrevented).toBe(false);
    expect(click(ed, anchor(ed, 'a[href="#web"]'), { ctrlKey: true }).defaultPrevented).toBe(false);
    expect(follow).not.toHaveBeenCalled();
  });
});
