// The part DisclosureToggle drawn with Material UI: an icon button with a chevron turning toward the
// content, a spinner while the content is being loaded.

import React from 'react';
import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup, render } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { TOOLTIP_ATTR, replacePart } from 'tl-react-bridge';
import type { DisclosureTogglePartProps } from 'tl-react-bridge';
import { createMuiRoot, createPageThemes } from '../MuiRoot';
import MuiDisclosureToggleAdapter from './MuiDisclosureToggleAdapter';
import MuiSpinnerAdapter from './MuiSpinnerAdapter';

// The toggle shows the part Spinner while its content is being loaded.
replacePart('Spinner', MuiSpinnerAdapter);

/** The root wrapper the adapter is drawn below: the one of the MUI default theme. */
const MuiRoot = createMuiRoot(createPageThemes({}));

/** The label of the toggle. */
const LABEL = 'Aufklappen';

function draw(props: Partial<DisclosureTogglePartProps>) {
  const element = <MuiRoot><MuiDisclosureToggleAdapter expanded={false} label={LABEL} {...props} /></MuiRoot>;
  const result = render(element);
  return {
    toggle: () => screen.getByRole('button', { name: props.label ?? LABEL }),
    redraw: (next: Partial<DisclosureTogglePartProps>) =>
      result.rerender(<MuiRoot><MuiDisclosureToggleAdapter expanded={false} label={LABEL} {...props} {...next} /></MuiRoot>),
  };
}

/** The transform of the chevron of the given toggle. */
function chevronTransform(toggle: HTMLElement): string {
  return getComputedStyle(toggle.querySelector('svg')!).transform;
}

afterEach(() => {
  cleanup();
});

describe('the part DisclosureToggle as MUI icon button', () => {
  it('draws a small icon button named by its label, with passed-through attributes', () => {
    const { toggle } = draw({ id: 't1', className: 'x', tabIndex: -1, 'aria-controls': 'body' });

    const button = toggle();
    expect(button.id).toBe('t1');
    expect(button.classList).toContain('MuiIconButton-root');
    expect(button.classList).toContain('MuiIconButton-sizeSmall');
    expect(button.classList).toContain('x');
    expect(button.classList).not.toContain('tl-toggle');
    expect(button.getAttribute('aria-controls')).toBe('body');
    expect(button.getAttribute('tabindex')).toBe('-1');
    expect(button.getAttribute(TOOLTIP_ATTR)).toBe('text:' + LABEL);
    expect(button.querySelector('svg.MuiSvgIcon-root')).not.toBeNull();
  });

  it('marks whether the content is shown and turns the chevron toward it', () => {
    const { toggle, redraw } = draw({});

    expect(toggle().getAttribute('aria-expanded')).toBe('false');
    expect(chevronTransform(toggle())).toBe('');

    redraw({ expanded: true });
    expect(toggle().getAttribute('aria-expanded')).toBe('true');
    expect(chevronTransform(toggle())).toBe('rotate(90deg)');
  });

  it('turns the chevron back to the start for content opening toward the end of the line', () => {
    const { toggle } = draw({ expanded: true, opens: 'end' });

    expect(chevronTransform(toggle())).toBe('rotate(180deg)');
  });

  it('calls the handler when pressed, but not while disabled', async () => {
    const onClick = vi.fn();
    const { toggle, redraw } = draw({ onClick });

    await userEvent.click(toggle());
    expect(onClick).toHaveBeenCalledTimes(1);

    redraw({ disabled: true });
    expect((toggle() as HTMLButtonElement).disabled).toBe(true);
    toggle().click();
    expect(onClick).toHaveBeenCalledTimes(1);
  });
});
