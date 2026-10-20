// The rich text editor (spec 2026-10-20 wave 5d §7): Tiptap is headless - the library gives the writing surface
// (.ProseMirror) and the commands, the editor draws toolbar, menus and link popup from the parts of the design system.
// The test mounts the editor as the page does, through the registry its bundle registers it in, and checks the classes
// of the package and the surface of the library, so that an update of the library cannot break the styles unnoticed.
//
// Review focus 2: Enter and Space on "Bold" with selected text make the selection bold and leave selection and focus in
// the text; a click does the same.

import { describe, it, expect, vi, afterEach, beforeAll } from 'vitest';
import { cleanup, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { React, getComponent, register, registerPart } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import ButtonPart from '../../../com.top_logic.layout.react/react-src/controls/button/ButtonPart';
import MenuTriggerPart from '../../../com.top_logic.layout.react/react-src/controls/button/MenuTriggerPart';
import DividerPart from '../../../com.top_logic.layout.react/react-src/controls/divider/DividerPart';
import TextInputPart from '../../../com.top_logic.layout.react/react-src/controls/form/TextInputPart';
import '../../../com.top_logic.layout.react.wysiwyg/react-src/wysiwyg-entry';
import { CONTROL_ID, mountAdapter, patchState, settle } from './wire-test-support';
import type { ChildControlJson } from 'tl-react-bridge';

/** The module name the bundle of the editor registers it under. */
const MODULE = 'TLWysiwygEditor';

/** What the test reads of the Tiptap editor, which keeps itself at the element of its surface. */
interface TestEditor {
  getHTML(): string;
  isActive(name: string, attributes?: Record<string, unknown>): boolean;
  state: { selection: { from: number; to: number } };
  commands: { setTextSelection(range: { from: number; to: number }): boolean };
  view: { focus(): void; dom: HTMLElement };
}

/** The text the editor starts with in most tests. */
const TEXT = '<p>Hello world</p>';

/** The positions of "world" in {@link TEXT}: the paragraph opens at 0, its text starts at 1. */
const WORLD = { from: 7, to: 12 };

/** The names of the toggle buttons: the keys, as the test stub translates each key to itself. */
const TOGGLES = [
  'js.wysiwyg.bold', 'js.wysiwyg.italic', 'js.wysiwyg.underline', 'js.wysiwyg.strikethrough',
  'js.wysiwyg.blockquote', 'js.wysiwyg.codeBlock',
];

/** The entries of the paragraph format menu, in their order. */
const FORMATS = [
  'js.wysiwyg.paragraph', 'js.wysiwyg.heading1', 'js.wysiwyg.heading2', 'js.wysiwyg.heading3',
  'js.wysiwyg.heading4', 'js.wysiwyg.heading5', 'js.wysiwyg.heading6',
];

/** The module name of the stand-in for the toolbar of the commands the server configures for the editor. */
const COMMANDS = 'WysiwygTestCommands';

/** The stand-in for the toolbar of the server's commands: a toolbar of the package, as `TLToolbar` draws its root. */
register(COMMANDS, ({ controlId }: TLCellProps) => (
  <div id={controlId} className="tl-toolbar" role="toolbar" aria-label="commands" />
));

beforeAll(() => {
  // The TopLogic drawing of the parts: the test asks for the classes of the package.
  registerPart('Button', ButtonPart);
  registerPart('MenuTrigger', MenuTriggerPart);
  registerPart('Divider', DividerPart);
  registerPart('TextInput', TextInputPart);
});

afterEach(() => {
  cleanup();
  document.body.replaceChildren();
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
});

/** The component the bundle of the editor registers under {@link MODULE}. */
function registeredEditor(): React.FC<TLCellProps> {
  const component = getComponent(MODULE);
  expect(component, 'the bundle registers the editor').toBeDefined();
  return component as React.FC<TLCellProps>;
}

/**
 * Mounts the editor registered under {@link MODULE} with the given state and returns the editor and its surface, once
 * the translations have arrived.
 */
async function mountEditor(state: Record<string, unknown>): Promise<{ editor: TestEditor; surface: HTMLElement }> {
  mountAdapter(registeredEditor(), state);
  await settle();
  const surface = document.querySelector<HTMLElement>('.ProseMirror');
  expect(surface).not.toBeNull();
  const editor = (surface as unknown as { editor: TestEditor }).editor;
  return { editor, surface: surface! };
}

/** Puts the selection on "world", with the focus in the text. */
function selectWorld(editor: TestEditor) {
  editor.view.focus();
  editor.commands.setTextSelection(WORLD);
}

/** The selection of the editor as positions. */
function selection(editor: TestEditor): { from: number; to: number } {
  const { from, to } = editor.state.selection;
  return { from, to };
}

/** Every class of the page that starts with the old prefix of the editor, and every attribute Radix sets. */
function leftovers(): string[] {
  const found: string[] = [];
  document.querySelectorAll('*').forEach(element => {
    element.classList.forEach(name => {
      if (name.startsWith('tlWysiwyg')) found.push('class ' + name);
    });
    Array.from(element.attributes).forEach(attribute => {
      if (attribute.name.startsWith('data-radix')) found.push('attribute ' + attribute.name);
    });
  });
  return found;
}

/**
 * Gives every element a box, as a browser does for what it shows: jsdom gives none, and the menus rove over the
 * entries that have one, the popup focuses a field that has one.
 */
function layOut() {
  vi.spyOn(HTMLElement.prototype, 'getClientRects').mockImplementation(function (this: HTMLElement) {
    const rects = [new DOMRect(0, 0, 10, 10)];
    return Object.assign(rects, { item: (index: number) => rects[index] ?? null }) as unknown as DOMRectList;
  });
}

describe('rich text editor', () => {
  it('the frame is a field of the package, the toolbar a wrapping toolbar, the surface prose', async () => {
    await mountEditor({ value: TEXT, editable: true });

    const root = document.getElementById(CONTROL_ID)!;
    expect(root.classList.contains('tl-field')).toBe(true);
    expect(root.classList.contains('tl-rich-text')).toBe(true);
    expect(root.hasAttribute('aria-invalid')).toBe(false);

    // The toolbar is the first child of the frame and named in the language of the user.
    const toolbar = within(root).getByRole('toolbar', { name: 'js.wysiwyg.toolbar' });
    expect(toolbar).toBe(root.firstElementChild);
    expect(toolbar.classList.contains('tl-toolbar')).toBe(true);
    expect(toolbar.classList.contains('tl-toolbar--wrap')).toBe(true);

    // Groups of the toolbar, a vertical divider between two of them.
    const kinds = Array.from(toolbar.children).map(child =>
      child.classList.contains('tl-toolbar__group') ? 'group'
        : child.classList.contains('tl-divider') && child.classList.contains('tl-divider--vertical') ? 'divider'
          : child.className);
    expect(kinds).toEqual(['group', 'divider', 'group', 'divider', 'group', 'divider', 'group', 'divider', 'group']);
    toolbar.querySelectorAll(':scope > .tl-divider').forEach(divider => {
      expect(divider.getAttribute('aria-orientation')).toBe('vertical');
    });

    // The toggles say whether they are pressed; every button of the toolbar is a ghost icon button of the package.
    const toggles = within(toolbar).getAllByRole('button').filter(button => button.hasAttribute('aria-pressed'));
    expect(toggles.map(button => button.getAttribute('aria-label'))).toEqual(TOGGLES);
    expect(toggles.map(button => button.getAttribute('aria-pressed'))).toEqual(TOGGLES.map(() => 'false'));
    within(toolbar).getAllByRole('button').forEach(button => {
      expect(button.className).toMatch(/\btl-button\b/);
      expect(button.className).toMatch(/\btl-button--ghost\b/);
      expect(button.className).toMatch(/\btl-button--icon\b/);
    });

    // The writing surface of the library carries the prose of the package; without an error it states none.
    const surface = root.querySelector('.ProseMirror')!;
    expect(surface.classList.contains('tl-prose')).toBe(true);
    expect(surface.getAttribute('role')).toBe('textbox');
    expect(surface.hasAttribute('aria-invalid')).toBe(false);

    expect(leftovers()).toEqual([]);
  });

  it('an error marks the frame and the textbox aria-invalid, not a class', async () => {
    await mountEditor({ value: TEXT, editable: true, hasError: true });

    // The frame shows it (the border of tl-field), the textbox announces it.
    const root = document.getElementById(CONTROL_ID)!;
    expect(root.getAttribute('aria-invalid')).toBe('true');
    expect(root.className).toBe('tl-field tl-rich-text');
    const surface = within(root).getByRole('textbox');
    expect(surface.classList.contains('ProseMirror')).toBe(true);
    expect(surface.getAttribute('aria-invalid')).toBe('true');
  });

  it('an error the server reports later reaches the textbox, and leaves it when cleared', async () => {
    const { surface } = await mountEditor({ value: TEXT, editable: true });
    expect(surface.hasAttribute('aria-invalid')).toBe(false);

    patchState({ hasError: true });
    expect(document.getElementById(CONTROL_ID)!.getAttribute('aria-invalid')).toBe('true');
    expect(surface.getAttribute('aria-invalid')).toBe('true');
    // The editor is not re-created for it: the same surface, still a textbox of prose.
    expect(document.querySelector('.ProseMirror')).toBe(surface);
    expect(surface.getAttribute('role')).toBe('textbox');
    expect(surface.classList.contains('tl-prose')).toBe(true);

    patchState({ hasError: false });
    expect(document.getElementById(CONTROL_ID)!.hasAttribute('aria-invalid')).toBe(false);
    expect(surface.hasAttribute('aria-invalid')).toBe(false);
  });

  it('the commands of the server are a toolbar of their own, alone in a group that may shrink', async () => {
    const commands = { controlId: 'c2', module: COMMANDS, state: {} } as unknown as ChildControlJson;
    await mountEditor({ value: TEXT, editable: true, toolbar: commands });

    const toolbar = screen.getByRole('toolbar', { name: 'js.wysiwyg.toolbar' });
    const nested = document.getElementById('c2')!;
    const group = nested.parentElement!;
    // The package lets exactly this group shrink below its content: `.tl-toolbar__group:has(> .tl-toolbar)`.
    expect(group.parentElement).toBe(toolbar);
    expect(group.className).toBe('tl-toolbar__group');
    expect(Array.from(group.children)).toEqual([nested]);
    expect(group.matches('.tl-toolbar__group:has(> .tl-toolbar)')).toBe(true);
    // Before the history, after the link group.
    const groups = Array.from(toolbar.querySelectorAll(':scope > .tl-toolbar__group'));
    expect(groups.indexOf(group)).toBe(groups.length - 2);
    expect(toolbar.querySelectorAll('.tl-toolbar__group:has(> .tl-toolbar)')).toHaveLength(1);
  });

  it('the view is prose without frame', async () => {
    mountAdapter(registeredEditor(), { value: TEXT, editable: false });
    await settle();

    const root = document.getElementById(CONTROL_ID)!;
    expect(root.classList.contains('tl-prose')).toBe(true);
    expect(root.classList.contains('tl-field')).toBe(false);
    expect(root.classList.contains('tl-rich-text')).toBe(false);
    expect(root.querySelector('p')?.textContent).toBe('Hello world');
    expect(screen.queryByRole('toolbar')).toBeNull();
    expect(leftovers()).toEqual([]);
  });

  it('Enter and Space on Bold make the selection bold, selection and focus stay in the text', async () => {
    const user = userEvent.setup();
    const { editor, surface } = await mountEditor({ value: TEXT, editable: true });
    selectWorld(editor);
    const bold = screen.getByRole('button', { name: 'js.wysiwyg.bold' });

    // The keyboard reaches the button: the user has tabbed to it.
    bold.focus();
    await user.keyboard('{Enter}');

    expect(editor.getHTML()).toBe('<p>Hello <strong>world</strong></p>');
    await waitFor(() => expect(document.activeElement).toBe(surface));
    expect(selection(editor)).toEqual(WORLD);
    await waitFor(() => expect(bold.getAttribute('aria-pressed')).toBe('true'));

    bold.focus();
    await user.keyboard(' ');

    expect(editor.getHTML()).toBe(TEXT);
    await waitFor(() => expect(document.activeElement).toBe(surface));
    expect(selection(editor)).toEqual(WORLD);
    await waitFor(() => expect(bold.getAttribute('aria-pressed')).toBe('false'));
  });

  it('a click on Bold makes the selection bold and leaves the focus in the text', async () => {
    const user = userEvent.setup();
    const { editor, surface } = await mountEditor({ value: TEXT, editable: true });
    selectWorld(editor);
    expect(document.activeElement).toBe(surface);

    await user.click(screen.getByRole('button', { name: 'js.wysiwyg.bold' }));

    expect(editor.getHTML()).toBe('<p>Hello <strong>world</strong></p>');
    expect(document.activeElement).toBe(surface);
    expect(selection(editor)).toEqual(WORLD);
  });

  it('the paragraph format is a menu of radio entries, exactly one checked', async () => {
    const user = userEvent.setup();
    layOut();
    const { editor, surface } = await mountEditor({ value: TEXT, editable: true });
    selectWorld(editor);

    const trigger = screen.getByRole('button', { name: 'js.wysiwyg.heading' });
    expect(trigger.getAttribute('aria-haspopup')).toBe('menu');
    expect(trigger.getAttribute('aria-expanded')).toBe('false');
    await user.click(trigger);

    const menu = screen.getByRole('menu');
    expect(menu.classList.contains('tl-popover')).toBe(true);
    expect(menu.classList.contains('tl-menu')).toBe(true);
    expect(trigger.getAttribute('aria-expanded')).toBe('true');
    let radios = within(menu).getAllByRole('menuitemradio');
    expect(radios.map(radio => radio.textContent)).toEqual(FORMATS);
    expect(radios.filter(radio => radio.getAttribute('aria-checked') === 'true').map(radio => radio.textContent))
      .toEqual(['js.wysiwyg.paragraph']);
    expect(radios.filter(radio => radio.getAttribute('aria-checked') === 'false')).toHaveLength(FORMATS.length - 1);
    expect(leftovers()).toEqual([]);

    await user.click(radios[2]);

    expect(editor.getHTML()).toBe('<h2>Hello world</h2>');
    expect(screen.queryByRole('menu')).toBeNull();
    await waitFor(() => expect(document.activeElement).toBe(surface));

    // A radio entry is no toggle: choosing the level in force keeps it.
    await user.click(trigger);
    await user.click(within(screen.getByRole('menu')).getAllByRole('menuitemradio')[2]);
    expect(editor.getHTML()).toBe('<h2>Hello world</h2>');
    await waitFor(() => expect(document.activeElement).toBe(surface));

    // Opened again from the keyboard, the menu checks the format at the cursor; Escape closes it and gives the focus
    // back to the text, not to the trigger the keyboard had opened it from.
    trigger.focus();
    await user.keyboard('{Enter}');
    radios = within(screen.getByRole('menu')).getAllByRole('menuitemradio');
    expect(radios.filter(radio => radio.getAttribute('aria-checked') === 'true').map(radio => radio.textContent))
      .toEqual(['js.wysiwyg.heading2']);
    await user.keyboard('{Escape}');

    expect(screen.queryByRole('menu')).toBeNull();
    await waitFor(() => expect(document.activeElement).toBe(surface));
    expect(selection(editor)).toEqual(WORLD);
  });

  it('the list kind is a menu of radio entries', async () => {
    const user = userEvent.setup();
    layOut();
    const { editor } = await mountEditor({ value: TEXT, editable: true });
    selectWorld(editor);

    await user.click(screen.getByRole('button', { name: 'js.wysiwyg.lists' }));
    let radios = within(screen.getByRole('menu')).getAllByRole('menuitemradio');
    expect(radios.map(radio => radio.textContent)).toEqual(['js.wysiwyg.bulletList', 'js.wysiwyg.orderedList']);
    expect(radios.map(radio => radio.getAttribute('aria-checked'))).toEqual(['false', 'false']);

    await user.click(radios[1]);
    expect(editor.isActive('orderedList')).toBe(true);

    await user.click(screen.getByRole('button', { name: 'js.wysiwyg.lists' }));
    radios = within(screen.getByRole('menu')).getAllByRole('menuitemradio');
    expect(radios.map(radio => radio.getAttribute('aria-checked'))).toEqual(['false', 'true']);
  });

  it('the link popup is a popover with a labelled field; applying sets the link and gives the focus back to the text', async () => {
    const user = userEvent.setup();
    layOut();
    const { editor, surface } = await mountEditor({ value: TEXT, editable: true });
    selectWorld(editor);

    const trigger = screen.getByRole('button', { name: 'js.wysiwyg.link' });
    expect(trigger.getAttribute('aria-haspopup')).toBe('dialog');
    await user.click(trigger);

    const popup = screen.getByRole('dialog');
    expect(popup.classList.contains('tl-popover')).toBe(true);
    expect(popup.classList.contains('tl-popover--content')).toBe(true);
    const field = within(popup).getByLabelText('js.wysiwyg.linkUrl') as HTMLInputElement;
    expect(field.tagName).toBe('INPUT');
    expect(field.type).toBe('url');
    expect(field.classList.contains('tl-field')).toBe(true);
    expect(popup.querySelector('label')!.htmlFor).toBe(field.id);
    expect(document.activeElement).toBe(field);
    expect(leftovers()).toEqual([]);

    const apply = within(popup).getByRole('button', { name: 'js.wysiwyg.linkApply' });
    expect(apply.className).toMatch(/\btl-button--primary\b/);
    expect((apply as HTMLButtonElement).disabled).toBe(true);
    // No link at the cursor, nothing to remove.
    expect(within(popup).queryByRole('button', { name: 'js.wysiwyg.linkRemove' })).toBeNull();

    await user.type(field, 'https://example.com');
    expect((apply as HTMLButtonElement).disabled).toBe(false);
    await user.click(apply);

    expect(screen.queryByRole('dialog')).toBeNull();
    expect(surface.querySelector('a')?.getAttribute('href')).toBe('https://example.com');
    expect(surface.querySelector('a')?.textContent).toBe('world');
    await waitFor(() => expect(document.activeElement).toBe(surface));
    expect(selection(editor)).toEqual(WORLD);
  });

  it('on a link the popup offers removal; Escape closes it and gives the focus back to the text', async () => {
    const user = userEvent.setup();
    layOut();
    const { editor, surface } = await mountEditor({
      value: '<p>Hello <a href="https://example.com">world</a></p>', editable: true,
    });
    selectWorld(editor);

    // Opened from the keyboard: the focus comes back to the text, not to the trigger.
    screen.getByRole('button', { name: 'js.wysiwyg.link' }).focus();
    await user.keyboard('{Enter}');
    const popup = screen.getByRole('dialog');
    expect((within(popup).getByLabelText('js.wysiwyg.linkUrl') as HTMLInputElement).value).toBe('https://example.com');
    const remove = within(popup).getByRole('button', { name: 'js.wysiwyg.linkRemove' });
    expect(remove.className).toMatch(/\btl-button--danger\b/);

    await user.keyboard('{Escape}');

    expect(screen.queryByRole('dialog')).toBeNull();
    await waitFor(() => expect(document.activeElement).toBe(surface));
    expect(selection(editor)).toEqual(WORLD);
    expect(surface.querySelector('a')).not.toBeNull();

    await user.click(screen.getByRole('button', { name: 'js.wysiwyg.link' }));
    await user.click(within(screen.getByRole('dialog')).getByRole('button', { name: 'js.wysiwyg.linkRemove' }));

    expect(screen.queryByRole('dialog')).toBeNull();
    expect(surface.querySelector('a')).toBeNull();
    await waitFor(() => expect(document.activeElement).toBe(surface));
  });
});
