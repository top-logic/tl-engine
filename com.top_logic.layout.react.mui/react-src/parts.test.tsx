// The part registry of the bridge: which component draws a part, depending on registerPart and
// replacePart; and the TopLogic drawing of the parts, which the controls bundle registers.

import React from 'react';
import { describe, it, expect, vi, afterEach } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import type { ButtonPartProps, MenuTriggerPartProps } from 'tl-react-bridge';

/** The address of a link. */
const LINK_ADDRESS = 'https://example.com/handbuch';

/** Draws a button part as a marker naming the drawing, so a test sees which component drew it. */
function drawing(name: string): React.FC<ButtonPartProps> {
  return ({ label }) => <span data-testid="part">{`${name}:${label}`}</span>;
}

/** The registry, loaded afresh for each test: the registrations are global to a page. */
async function load() {
  vi.resetModules();
  return await import('tl-react-bridge');
}

/** Renders the part `Button` as a control does, through usePart. */
function renderButton(usePart: Awaited<ReturnType<typeof load>>['usePart']) {
  function Probe() {
    const Button = usePart('Button');
    return <Button label="Speichern" display="label-only" appearance="secondary" />;
  }
  render(<Probe />);
  return screen.getByTestId('part').textContent;
}

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});

describe('the part registry', () => {
  it('fails to look up a part nothing is registered for', async () => {
    const { usePart } = await load();

    expect(() => usePart('Button')).toThrow(/No component registered for the part: Button/);
  });

  it('looks up the registered default of a part', async () => {
    const { registerPart, usePart } = await load();

    registerPart('Button', drawing('default'));

    expect(renderButton(usePart)).toBe('default:Speichern');
    // Another part is not affected.
    expect(() => usePart('MenuTrigger')).toThrow(/MenuTrigger/);
  });

  it('draws a part with its replacement, registered after the default', async () => {
    const { registerPart, replacePart, usePart } = await load();

    registerPart('Button', drawing('default'));
    replacePart('Button', drawing('library'));

    expect(renderButton(usePart)).toBe('library:Speichern');
  });

  it('draws a part with its replacement, registered before the default', async () => {
    const { registerPart, replacePart, usePart } = await load();

    replacePart('Button', drawing('library'));
    registerPart('Button', drawing('default'));

    expect(renderButton(usePart)).toBe('library:Speichern');
  });

  it('warns about a second replacement, which wins', async () => {
    const warn = vi.spyOn(console, 'warn').mockImplementation(() => {});
    const { replacePart, usePart } = await load();

    replacePart('Button', drawing('first'));
    expect(warn).not.toHaveBeenCalled();
    replacePart('Button', drawing('second'));

    expect(warn).toHaveBeenCalledTimes(1);
    expect(renderButton(usePart)).toBe('second:Speichern');
  });

  it('warns about a second registration, which wins', async () => {
    const warn = vi.spyOn(console, 'warn').mockImplementation(() => {});
    const { registerPart, usePart } = await load();

    registerPart('Button', drawing('first'));
    registerPart('Button', drawing('second'));

    expect(warn).toHaveBeenCalledTimes(1);
    expect(renderButton(usePart)).toBe('second:Speichern');
  });
});

describe('the TopLogic drawing of the parts', () => {
  /** The default drawings, rendered with the given props. */
  async function defaults() {
    const { default: ButtonPart } = await import('../../com.top_logic.layout.react/react-src/controls/button/ButtonPart');
    const { default: MenuTriggerPart } = await import('../../com.top_logic.layout.react/react-src/controls/button/MenuTriggerPart');
    return { ButtonPart, MenuTriggerPart };
  }

  it('draws a button with the classes of the design system', async () => {
    const { ButtonPart } = await defaults();

    render(<ButtonPart label="Löschen" display="label-only" appearance="primary" danger className="x" />);

    const button = screen.getByRole('button', { name: 'Löschen' });
    expect(button.className).toBe('tl-button tl-button--primary tl-type-body tl-button--danger x');
    expect(button.querySelector('.tl-button__label')?.textContent).toBe('Löschen');
  });

  it('draws an icon-only button named by its label, with passed-through attributes', async () => {
    const { ButtonPart } = await defaults();
    const ref = React.createRef<HTMLButtonElement>();

    render(<ButtonPart ref={ref} label="Schließen" image="css:fa-solid fa-xmark" display="icon-only"
      appearance="ghost" small aria-describedby="hint" />);

    const button = screen.getByRole('button', { name: 'Schließen' });
    expect(ref.current).toBe(button);
    expect(button.className).toBe('tl-button tl-button--ghost tl-type-body tl-button--sm tl-button--icon');
    expect(button.getAttribute('aria-describedby')).toBe('hint');
    expect(button.querySelector('.tl-button__label')).toBeNull();
    expect(button.querySelector('i.fa-xmark.tl-icon-sm')).not.toBeNull();
  });

  it('draws a link as an anchor with the classes of a button', async () => {
    const { ButtonPart } = await defaults();
    const ref = React.createRef<HTMLElement>();

    render(<ButtonPart ref={ref} label="Öffnen" image="css:fa-solid fa-arrow-up-right-from-square"
      display="icon-only" appearance="ghost" small href={LINK_ADDRESS} target="_blank" />);

    const link = screen.getByRole('link', { name: 'Öffnen' });
    expect(link.tagName).toBe('A');
    expect(ref.current).toBe(link);
    expect(link.className).toBe('tl-button tl-button--ghost tl-type-body tl-button--sm tl-button--icon');
    expect(link.getAttribute('href')).toBe(LINK_ADDRESS);
    expect(link.getAttribute('target')).toBe('_blank');
    expect(link.getAttribute('rel')).toBe('noopener noreferrer');
    expect(link.hasAttribute('type')).toBe(false);
  });

  it('draws a disabled link as an anchor without address', async () => {
    const { ButtonPart } = await defaults();

    render(<ButtonPart label="Öffnen" display="label-only" appearance="link" href={LINK_ADDRESS} disabled />);

    const anchor = document.querySelector('a')!;
    expect(anchor.hasAttribute('href')).toBe(false);
    expect(anchor.getAttribute('aria-disabled')).toBe('true');
    expect(anchor.hasAttribute('rel')).toBe(false);
  });

  it('draws a toggle in a menu as a checkbox entry, an active button as the current entry', async () => {
    const { ButtonPart } = await defaults();

    render(<>
      <ButtonPart label="Fett" display="label-only" appearance="menu-item" checked={false} />
      <ButtonPart label="Hell" display="label-only" appearance="menu-item" active />
    </>);

    const toggle = screen.getByRole('menuitemcheckbox', { name: 'Fett' });
    expect(toggle.getAttribute('aria-checked')).toBe('false');
    expect(toggle.hasAttribute('aria-pressed')).toBe(false);
    const current = screen.getByRole('menuitem', { name: 'Hell' });
    expect(current.getAttribute('aria-current')).toBe('true');
    expect(current.className).toBe('tl-menu__item tl-type-body-strong');
  });

  it('draws a menu trigger with its label and a chevron, or with its icon alone', async () => {
    const { MenuTriggerPart } = await defaults();
    const props: MenuTriggerPartProps = { appearance: 'ghost', expanded: false, label: 'Mehr' };

    render(<>
      <MenuTriggerPart {...props} />
      <MenuTriggerPart {...props} label="Aktionen" icon="css:bi bi-three-dots" expanded />
    </>);

    const labeled = screen.getByRole('button', { name: 'Mehr' });
    expect(labeled.className).toBe('tl-button tl-button--ghost tl-type-body');
    expect(labeled.getAttribute('aria-haspopup')).toBe('menu');
    expect(labeled.getAttribute('aria-expanded')).toBe('false');
    expect(labeled.querySelector('i.fa-chevron-down')).not.toBeNull();
    const iconOnly = screen.getByRole('button', { name: 'Aktionen' });
    expect(iconOnly.className).toBe('tl-button tl-button--ghost tl-type-body tl-button--icon');
    expect(iconOnly.getAttribute('aria-expanded')).toBe('true');
    expect(iconOnly.textContent).toBe('');
  });
});
