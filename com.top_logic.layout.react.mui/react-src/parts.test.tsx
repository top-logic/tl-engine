// The part registry of the bridge: which component draws a part, depending on registerPart and
// replacePart; and the TopLogic drawing of the parts, which the controls bundle registers.

import React from 'react';
import { describe, it, expect, vi, afterEach } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { TOOLTIP_ATTR } from 'tl-react-bridge';
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
    const { default: DisclosureTogglePart } = await import('../../com.top_logic.layout.react/react-src/controls/button/DisclosureTogglePart');
    const { default: CheckboxPart } = await import('../../com.top_logic.layout.react/react-src/controls/form/CheckboxPart');
    const { default: TextInputPart } = await import('../../com.top_logic.layout.react/react-src/controls/form/TextInputPart');
    const { default: FieldValuePart } = await import('../../com.top_logic.layout.react/react-src/controls/form/FieldValuePart');
    const { default: ChipPart } = await import('../../com.top_logic.layout.react/react-src/controls/chip/ChipPart');
    const { default: BadgePart } = await import('../../com.top_logic.layout.react/react-src/controls/chip/BadgePart');
    const { default: DividerPart } = await import('../../com.top_logic.layout.react/react-src/controls/divider/DividerPart');
    const { default: ProgressBarPart } = await import('../../com.top_logic.layout.react/react-src/controls/progress/ProgressBarPart');
    const { default: SpinnerPart } = await import('../../com.top_logic.layout.react/react-src/controls/progress/SpinnerPart');
    // A chip draws its remove button with the part Button, a disclosure toggle its busy indicator
    // with the part Spinner.
    const { registerPart } = await import('tl-react-bridge');
    vi.spyOn(console, 'warn').mockImplementation(() => {});
    registerPart('Button', ButtonPart);
    registerPart('Spinner', SpinnerPart);
    return {
      ButtonPart, MenuTriggerPart, DisclosureTogglePart, CheckboxPart, TextInputPart, FieldValuePart, ChipPart, BadgePart,
      DividerPart, ProgressBarPart, SpinnerPart,
    };
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

  it('draws a disclosure toggle as the toggle of the design system, named by its label', async () => {
    const { DisclosureTogglePart } = await defaults();
    const onClick = vi.fn();

    render(<>
      <DisclosureTogglePart expanded={false} label="Aufklappen" onClick={onClick} aria-controls="body" />
      <DisclosureTogglePart expanded label="Zuklappen" tabIndex={-1} className="x" />
    </>);

    const collapsed = screen.getByRole('button', { name: 'Aufklappen' });
    expect(collapsed.className).toBe('tl-toggle');
    expect(collapsed.getAttribute('type')).toBe('button');
    expect(collapsed.getAttribute('aria-expanded')).toBe('false');
    expect(collapsed.getAttribute('aria-controls')).toBe('body');
    expect(collapsed.getAttribute(TOOLTIP_ATTR)).toBe('text:Aufklappen');
    expect(collapsed.hasAttribute('aria-busy')).toBe(false);
    expect(collapsed.querySelector('i.fa-chevron-right.tl-icon-sm')).not.toBeNull();
    collapsed.click();
    expect(onClick).toHaveBeenCalledTimes(1);

    const expanded = screen.getByRole('button', { name: 'Zuklappen' });
    expect(expanded.className).toBe('tl-toggle x');
    expect(expanded.getAttribute('aria-expanded')).toBe('true');
    expect(expanded.getAttribute('tabindex')).toBe('-1');
  });

  it('marks a disclosure toggle opening toward the end of the line', async () => {
    const { DisclosureTogglePart } = await defaults();

    render(<DisclosureTogglePart expanded opens="end" label="Seitenleiste zuklappen" />);

    expect(screen.getByRole('button', { name: 'Seitenleiste zuklappen' }).className)
      .toBe('tl-toggle tlDisclosureToggle--end');
  });

  it('shows a busy indicator instead of the chevron while the content is being loaded', async () => {
    const { DisclosureTogglePart } = await defaults();

    render(<DisclosureTogglePart expanded={false} loading label="Aufklappen" />);

    const toggle = screen.getByRole('button', { name: 'Aufklappen' });
    expect(toggle.getAttribute('aria-busy')).toBe('true');
    const spinner = toggle.querySelector('.tlSpinner')!;
    expect(spinner.className).toBe('tlSpinner tlSpinner--small');
    expect(spinner.getAttribute('aria-hidden')).toBe('true');
    expect(toggle.querySelector('i.fa-chevron-right')).toBeNull();
  });

  it('draws a checkbox as the box of the design system, named by its attributes', async () => {
    const { CheckboxPart } = await defaults();
    const ref = React.createRef<HTMLInputElement>();
    const onChange = vi.fn();

    render(<CheckboxPart id="b1" ref={ref} name="n" tabIndex={-1} className="x" checked={false}
      onChange={onChange} aria-label="Zeile auswählen" />);

    const box = screen.getByRole('checkbox', { name: 'Zeile auswählen' }) as HTMLInputElement;
    expect(ref.current).toBe(box);
    expect(box.id).toBe('b1');
    expect(box.name).toBe('n');
    expect(box.className).toBe('tl-checkbox x');
    expect(box.getAttribute('tabindex')).toBe('-1');
    expect(box.hasAttribute('aria-checked')).toBe(false);
    expect(box.hasAttribute('aria-readonly')).toBe(false);
    expect(box.hasAttribute('aria-invalid')).toBe(false);

    await userEvent.click(box);
    expect(onChange.mock.calls).toEqual([[true]]);
    // Controlled: the box shows the state it is given.
    expect(box.checked).toBe(false);
  });

  it('draws an indeterminate checkbox, which stays indeterminate when pressed', async () => {
    const { CheckboxPart } = await defaults();
    const onChange = vi.fn();

    const { rerender } = render(<CheckboxPart checked={false} indeterminate onChange={onChange} aria-label="Alle" />);

    const box = screen.getByRole('checkbox') as HTMLInputElement;
    expect(box.indeterminate).toBe(true);
    expect(box.getAttribute('aria-checked')).toBe('mixed');
    await userEvent.click(box);
    expect(onChange.mock.calls).toEqual([[true]]);
    expect(box.indeterminate).toBe(true);

    rerender(<CheckboxPart checked indeterminate={false} onChange={onChange} aria-label="Alle" />);
    expect(box.indeterminate).toBe(false);
    expect(box.checked).toBe(true);
    expect(box.hasAttribute('aria-checked')).toBe(false);
  });

  it('keeps a read-only checkbox as it is and calls nothing', async () => {
    const { CheckboxPart } = await defaults();
    const onChange = vi.fn();

    render(<CheckboxPart checked readOnly onChange={onChange} aria-label="Aktiv" />);

    const box = screen.getByRole('checkbox') as HTMLInputElement;
    expect(box.getAttribute('aria-readonly')).toBe('true');
    expect(box.disabled).toBe(false);
    await userEvent.click(box);
    expect(box.checked).toBe(true);
    expect(onChange).not.toHaveBeenCalled();
  });

  it('draws a disabled checkbox natively disabled', async () => {
    const { CheckboxPart } = await defaults();
    const onChange = vi.fn();

    render(<CheckboxPart checked={false} disabled onChange={onChange} aria-label="Aktiv" />);

    const box = screen.getByRole('checkbox') as HTMLInputElement;
    expect(box.disabled).toBe(true);
    await userEvent.setup({ pointerEventsCheck: 0 }).click(box);
    expect(onChange).not.toHaveBeenCalled();
  });

  it('marks the state of the value as the design system reads it', async () => {
    const { CheckboxPart } = await defaults();

    render(<>
      <CheckboxPart checked={false} validation="error" required aria-label="Fehler" />
      <CheckboxPart checked={false} validation="warning" aria-label="Warnung" />
    </>);

    const invalid = screen.getByRole('checkbox', { name: 'Fehler' });
    expect(invalid.getAttribute('aria-invalid')).toBe('true');
    expect(invalid.getAttribute('aria-required')).toBe('true');
    const questionable = screen.getByRole('checkbox', { name: 'Warnung' });
    expect(questionable.getAttribute('data-tl-state')).toBe('warning');
    expect(questionable.hasAttribute('aria-invalid')).toBe(false);
  });

  it('draws a switch, which is never indeterminate', async () => {
    const { CheckboxPart } = await defaults();

    render(<CheckboxPart display="switch" checked indeterminate aria-label="An" />);

    const toggle = screen.getByRole('switch', { name: 'An' }) as HTMLInputElement;
    expect(toggle.className).toBe('tl-checkbox tl-checkbox--switch');
    expect(toggle.indeterminate).toBe(false);
    expect(toggle.hasAttribute('aria-checked')).toBe(false);
  });

  it('draws a checkbox with a label in the label of the design system, which presses the box', async () => {
    const { CheckboxPart } = await defaults();
    const onChange = vi.fn();

    render(<CheckboxPart checked label="Name" className="x" onChange={onChange} />);

    const box = screen.getByRole('checkbox', { name: 'Name' });
    expect(box.className).toBe('tl-checkbox');
    const label = box.closest('label')!;
    expect(label.className).toBe('tl-choice tl-type-body x');
    await userEvent.click(screen.getByText('Name'));
    expect(onChange.mock.calls).toEqual([[false]]);
  });

  it('draws content other than a text as the label as it is given, and a small box like any other', async () => {
    const { CheckboxPart } = await defaults();

    render(<CheckboxPart checked={false} small label={<><i className="icon" /><b>Option</b></>} />);

    const box = screen.getByRole('checkbox', { name: 'Option' });
    expect(box.className).toBe('tl-checkbox');
    const label = box.closest('label')!;
    expect(Array.from(label.children).map(child => child.tagName)).toEqual(['INPUT', 'I', 'B']);
  });

  it('draws a text box as the input of the design system in its group, named by its attributes', async () => {
    const { TextInputPart } = await defaults();
    const ref = React.createRef<HTMLInputElement | HTMLTextAreaElement>();
    const onChange = vi.fn();

    render(<TextInputPart id="t1" ref={ref} name="n" className="x" value="ab" placeholder="Suche"
      onChange={onChange} aria-label="Name" />);

    const box = screen.getByRole('textbox', { name: 'Name' }) as HTMLInputElement;
    expect(ref.current).toBe(box);
    expect(box.id).toBe('t1');
    expect(box.name).toBe('n');
    expect(box.type).toBe('text');
    expect(box.placeholder).toBe('Suche');
    expect(box.className).toBe('tl-field tl-type-body');
    expect(box.parentElement!.className).toBe('tl-field-group x');
    expect(box.hasAttribute('aria-invalid')).toBe(false);
    expect(box.hasAttribute('aria-required')).toBe(false);
    expect(box.hasAttribute(TOOLTIP_ATTR)).toBe(false);

    await userEvent.type(box, 'c');
    expect(onChange.mock.calls).toEqual([['abc']]);
    // Controlled: the box shows the text it is given.
    expect(box.value).toBe('ab');
  });

  it('keeps the input in place while icon and action come and go', async () => {
    const { TextInputPart } = await defaults();

    const { rerender } = render(<TextInputPart value="" aria-label="Suche" />);
    const box = screen.getByRole('textbox', { name: 'Suche' });
    box.focus();

    rerender(<TextInputPart value="a" aria-label="Suche" icon="css:my-icon"
      action={<button type="button" className="tl-button">X</button>} />);

    expect(screen.getByRole('textbox', { name: 'Suche' })).toBe(box);
    expect(document.activeElement).toBe(box);
    const group = box.parentElement!;
    expect(Array.from(group.children).map(child => child.className))
      .toEqual(['tl-field-group__icon', 'tl-field tl-type-body', 'tl-button']);
    expect(group.querySelector('.tl-field-group__icon')!.getAttribute('aria-hidden')).toBe('true');
    expect(group.querySelector('.tl-field-group__icon i.my-icon')).not.toBeNull();
  });

  it('marks the state of the value and the tooltip as the design system reads them', async () => {
    const { TextInputPart } = await defaults();

    render(<>
      <TextInputPart value="" validation="error" required tooltip="Falsch" aria-label="Fehler" />
      <TextInputPart value="" validation="warning" aria-label="Warnung" />
    </>);

    const error = screen.getByRole('textbox', { name: 'Fehler' });
    expect(error.getAttribute('aria-invalid')).toBe('true');
    expect(error.getAttribute('aria-required')).toBe('true');
    expect(error.getAttribute(TOOLTIP_ATTR)).toBe('text:Falsch');
    const warning = screen.getByRole('textbox', { name: 'Warnung' });
    expect(warning.getAttribute('data-tl-state')).toBe('warning');
    expect(warning.hasAttribute('aria-invalid')).toBe(false);
  });

  it('reports no input while disabled or read-only', async () => {
    const { TextInputPart } = await defaults();
    const onChange = vi.fn();

    render(<>
      <TextInputPart value="a" disabled onChange={onChange} aria-label="Inaktiv" />
      <TextInputPart value="a" readOnly onChange={onChange} aria-label="Lesend" />
    </>);

    const inactive = screen.getByRole('textbox', { name: 'Inaktiv' }) as HTMLInputElement;
    const readOnly = screen.getByRole('textbox', { name: 'Lesend' }) as HTMLInputElement;
    expect(inactive.disabled).toBe(true);
    expect(readOnly.readOnly).toBe(true);
    await userEvent.setup({ pointerEventsCheck: 0 }).type(inactive, 'x');
    await userEvent.type(readOnly, 'x');

    expect(onChange).not.toHaveBeenCalled();
  });

  it('draws a multi-line box as the text area of the design system, without icon, action and key handler', async () => {
    const { TextInputPart } = await defaults();
    const onKeyDown = vi.fn();

    render(<TextInputPart value={'a\nb'} multiline icon="css:my-icon" action={<button type="button">X</button>}
      onKeyDown={onKeyDown} aria-label="Text" />);

    const area = screen.getByRole('textbox', { name: 'Text' });
    expect(area.tagName).toBe('TEXTAREA');
    expect(area.className).toBe('tl-field tl-field--multiline tl-type-body');
    expect(area.getAttribute('rows')).toBe('3');
    expect(document.querySelector('.my-icon')).toBeNull();
    expect(screen.queryByRole('button')).toBeNull();
    await userEvent.type(area, '{Enter}');
    expect(onKeyDown).not.toHaveBeenCalled();
  });

  it('draws a number box with its bounds, the keyboard asked for and a password box masked', async () => {
    const { TextInputPart } = await defaults();

    render(<>
      <TextInputPart value="1" type="number" min={0} max={255} inputMode="numeric" aria-label="Rot" />
      <TextInputPart value="geheim" type="password" aria-label="Passwort" />
    </>);

    const number = screen.getByRole('spinbutton', { name: 'Rot' }) as HTMLInputElement;
    expect(number.min).toBe('0');
    expect(number.max).toBe('255');
    expect(number.getAttribute('inputmode')).toBe('numeric');
    expect((screen.getByLabelText('Passwort') as HTMLInputElement).type).toBe('password');
  });

  it('draws a field value as the value of the design system, with class and attributes', async () => {
    const { FieldValuePart } = await defaults();

    render(<FieldValuePart id="v1" text="Hallo" className="x" aria-describedby="hint" />);

    const value = document.getElementById('v1')!;
    expect(value.tagName).toBe('SPAN');
    expect(value.className).toBe('tl-field-value tl-type-body x');
    expect(value.getAttribute('aria-describedby')).toBe('hint');
    expect(Array.from(value.children).map(child => child.className)).toEqual(['tl-field-value__text']);
    expect(value.textContent).toBe('Hallo');
    expect(screen.queryByRole('link')).toBeNull();
  });

  it('draws a field value pointing somewhere as a link opening it in a window of its own', async () => {
    const { FieldValuePart } = await defaults();

    render(<FieldValuePart id="v1" text="a@b.de" href="mailto:a@b.de" />);

    const link = screen.getByRole('link', { name: 'a@b.de' });
    expect(link.id).toBe('v1');
    expect(link.className).toBe('tl-field-value tl-type-body');
    expect(link.getAttribute('href')).toBe('mailto:a@b.de');
    expect(link.getAttribute('target')).toBe('_blank');
    expect(link.getAttribute('rel')).toBe('noopener noreferrer');
  });

  it('draws a multi-line field value one line per text, without a link', async () => {
    const { FieldValuePart } = await defaults();

    render(<FieldValuePart id="v1" text={'a\n\nc'} multiline href="https://example.org" />);

    const value = document.getElementById('v1')!;
    expect(value.tagName).toBe('DIV');
    expect(value.className).toBe('tl-field-value tl-type-body tl-field-value--multiline');
    expect(Array.from(value.children).map(line => line.textContent)).toEqual(['a', ' ', 'c']);
    expect(screen.queryByRole('link')).toBeNull();
  });
  it('draws a field value with an icon ahead of its text', async () => {
    const { FieldValuePart } = await defaults();

    render(<FieldValuePart id="v1" text="Alpha" icon="css:fa-solid fa-star" />);

    const value = document.getElementById('v1')!;
    expect(Array.from(value.children).map(child => child.className)).toEqual(['fa-solid fa-star tl-icon-sm', 'tl-field-value__text']);
  });

  it('draws a field value running an action as a link that does not navigate', async () => {
    const { FieldValuePart } = await defaults();
    const onClick = vi.fn();

    render(<FieldValuePart id="v1" text="Alpha" onClick={onClick} />);
    const link = screen.getByRole('link', { name: 'Alpha' });
    const event = new MouseEvent('click', { bubbles: true, cancelable: true });
    link.dispatchEvent(event);

    expect(link.className).toBe('tl-field-value tl-type-body');
    expect(link.getAttribute('target')).toBeNull();
    expect(onClick).toHaveBeenCalledTimes(1);
    expect(event.defaultPrevented).toBe(true);
  });

  it('draws a chip showing a value as a pill of its color role, with icon, label and detail', async () => {
    const { ChipPart } = await defaults();

    render(<ChipPart id="c1" label="Alpha" detail="2 KB" icon="css:fa-solid fa-star" tone="success" className="x"
      tooltip="Tipp" data-role="value" />);

    const chip = document.getElementById('c1')!;
    expect(chip.tagName).toBe('SPAN');
    expect(chip.className).toBe('tl-pill tl-pill--success tl-type-label x');
    expect(chip.getAttribute(TOOLTIP_ATTR)).toBe('text:Tipp');
    expect(chip.getAttribute('data-role')).toBe('value');
    expect(Array.from(chip.children).map(child => child.className))
      .toEqual(['fa-solid fa-star tl-pill__icon tl-icon-sm', 'tl-pill__label', 'tlChip__detail tl-nums']);
    expect(chip.textContent).toBe('Alpha2 KB');
    expect(screen.queryByRole('button')).toBeNull();
  });

  it('draws a neutral chip as a pill without a role class', async () => {
    const { ChipPart } = await defaults();

    render(<ChipPart id="c1" label="Alpha" tone="neutral" />);

    expect(document.getElementById('c1')!.className).toBe('tl-pill tl-type-label');
  });

  it('draws a chip running an action as a pill that is a button', async () => {
    const { ChipPart } = await defaults();
    const onClick = vi.fn();

    render(<ChipPart label="report.pdf" onClick={onClick} />);
    const chip = screen.getByRole('button', { name: 'report.pdf' });
    await userEvent.click(chip);

    expect(chip.className).toBe('tl-pill tl-type-label');
    expect(chip.hasAttribute('aria-pressed')).toBe(false);
    expect(onClick).toHaveBeenCalledTimes(1);
  });

  it('draws a toggle as the chip of the design system, its color role as a pill inside', async () => {
    const { ChipPart } = await defaults();
    const onClick = vi.fn();

    render(<ChipPart label="Beta" tone="error" pressed={false} onClick={onClick} />);
    const chip = screen.getByRole('button', { name: 'Beta' });
    await userEvent.click(chip);

    expect(chip.className).toBe('tl-chip tl-type-label');
    expect(chip.getAttribute('aria-pressed')).toBe('false');
    expect(chip.firstElementChild!.className).toBe('tl-pill tl-pill--error');
    expect(chip.querySelector('.tl-chip__label')!.textContent).toBe('Beta');
    expect(onClick).toHaveBeenCalledTimes(1);
  });

  it('draws a disabled toggle natively disabled', async () => {
    const { ChipPart } = await defaults();
    const onClick = vi.fn();

    render(<ChipPart label="Beta" pressed onClick={onClick} disabled />);
    const chip = screen.getByRole('button', { name: 'Beta' });
    await userEvent.setup({ pointerEventsCheck: 0 }).click(chip);

    expect(chip.getAttribute('aria-pressed')).toBe('true');
    expect((chip as HTMLButtonElement).disabled).toBe(true);
    expect(onClick).not.toHaveBeenCalled();
  });

  it('draws a removable chip with the remove button of the part Button inside the pill', async () => {
    const { ChipPart } = await defaults();
    const onRemove = vi.fn();

    render(<ChipPart id="c1" label="Alpha" onRemove={onRemove} removeLabel="Alpha entfernen" />);
    const remove = screen.getByRole('button', { name: 'Alpha entfernen' });
    await userEvent.click(remove);

    const chip = document.getElementById('c1')!;
    expect(chip.className).toBe('tl-pill tl-select__chip tl-type-label');
    expect(remove.parentElement).toBe(chip);
    expect(remove.className).toBe('tl-button tl-button--ghost tl-type-body tl-button--sm tl-button--icon');
    expect(screen.getAllByRole('button')).toEqual([remove]);
    expect(onRemove).toHaveBeenCalledTimes(1);
  });

  it('draws a removable toggle with its toggle beside the remove button, each pressed on its own', async () => {
    const { ChipPart } = await defaults();
    const onClick = vi.fn();
    const onRemove = vi.fn();

    render(<ChipPart id="c1" label="Meine" pressed tooltip="Filter aufheben" onClick={onClick} onRemove={onRemove}
      removeLabel="Filter löschen" />);
    const toggle = screen.getByRole('button', { name: 'Meine' });
    const remove = screen.getByRole('button', { name: 'Filter löschen' });
    await userEvent.click(remove);

    const chip = document.getElementById('c1')!;
    expect(chip.className).toBe('tl-chip tl-select__chip tl-type-label');
    expect(chip.hasAttribute('aria-pressed')).toBe(false);
    expect(toggle.className).toBe('tlChip__action');
    expect(toggle.getAttribute('aria-pressed')).toBe('true');
    expect(toggle.getAttribute(TOOLTIP_ATTR)).toBe('text:Filter aufheben');
    expect(onRemove).toHaveBeenCalledTimes(1);
    expect(onClick).not.toHaveBeenCalled();
    await userEvent.click(toggle);
    expect(onClick).toHaveBeenCalledTimes(1);
  });

  it('puts the tab index on the toggle and on the remove button of a chip', async () => {
    const { ChipPart } = await defaults();

    render(<ChipPart id="c1" label="Meine" pressed onClick={vi.fn()} onRemove={vi.fn()} removeLabel="Löschen"
      tabIndex={-1} />);

    expect(screen.getAllByRole('button').map(button => button.getAttribute('tabindex'))).toEqual(['-1', '-1']);
    expect(document.getElementById('c1')!.hasAttribute('tabindex')).toBe(false);
  });

  it('draws a badge as the badge of the design system', async () => {
    const { BadgePart } = await defaults();

    render(<BadgePart id="b1" content="12" className="x" aria-label="12 neu" />);

    const badge = document.getElementById('b1')!;
    expect(badge.className).toBe('tl-badge tl-type-label-strong tl-nums x');
    expect(badge.textContent).toBe('12');
    expect(badge.getAttribute('aria-label')).toBe('12 neu');
  });

  it('draws a divider as the divider of the design system, a vertical one marked so', async () => {
    const { DividerPart } = await defaults();

    const { container } = render(<>
      <DividerPart className="x" data-testid="h" />
      <DividerPart orientation="vertical" />
    </>);

    const [horizontal, vertical] = Array.from(container.querySelectorAll('hr'));
    expect(horizontal.className).toBe('tl-divider x');
    expect(horizontal.hasAttribute('aria-orientation')).toBe(false);
    expect(horizontal.getAttribute('data-testid')).toBe('h');
    expect(vertical.className).toBe('tl-divider tl-divider--vertical');
    expect(vertical.getAttribute('aria-orientation')).toBe('vertical');
    expect(screen.getAllByRole('separator')).toHaveLength(2);
  });

  it('draws a determinate progress bar as the progress bar of the design system, filled by its share', async () => {
    const { ProgressBarPart } = await defaults();

    render(<ProgressBarPart id="p1" value={0.754} label="3 / 4" className="x" aria-label="Fortschritt" />);

    const bar = screen.getByRole('progressbar');
    expect(bar.id).toBe('p1');
    expect(bar.className).toBe('tl-progress x');
    expect(bar.getAttribute('aria-valuenow')).toBe('75');
    expect(bar.getAttribute('aria-valuetext')).toBe('3 / 4');
    expect(bar.getAttribute('aria-label')).toBe('Fortschritt');
    expect(bar.hasAttribute('aria-busy')).toBe(false);
    expect((bar.querySelector('.tl-progress__fill') as HTMLElement).style.width).toBe('75%');
    expect(bar.querySelector('.tl-progress__label')!.textContent).toBe('3 / 4');
  });

  it('draws a progress bar without a value as indeterminate, busy, without fill width and label', async () => {
    const { ProgressBarPart } = await defaults();

    render(<ProgressBarPart />);

    const bar = screen.getByRole('progressbar');
    expect(bar.getAttribute('aria-busy')).toBe('true');
    expect(bar.hasAttribute('aria-valuenow')).toBe(false);
    expect(bar.hasAttribute('aria-valuetext')).toBe(false);
    expect((bar.querySelector('.tl-progress__fill') as HTMLElement).style.width).toBe('');
    expect(bar.querySelector('.tl-progress__label')).toBeNull();
  });

  it('draws a spinner with a label as a progress bar named by it, one without as decoration', async () => {
    const { SpinnerPart } = await defaults();

    const { container } = render(<>
      <SpinnerPart label="Laden…" className="x" />
      <SpinnerPart size="small" />
    </>);

    const named = screen.getByRole('progressbar', { name: 'Laden…' });
    expect(named.className).toBe('tlSpinner x');
    expect(named.hasAttribute('aria-hidden')).toBe(false);
    const decoration = container.querySelectorAll('.tlSpinner')[1];
    expect(decoration.className).toBe('tlSpinner tlSpinner--small');
    expect(decoration.getAttribute('aria-hidden')).toBe('true');
    expect(decoration.hasAttribute('role')).toBe(false);
    expect(screen.getAllByRole('progressbar')).toEqual([named]);
  });
});
