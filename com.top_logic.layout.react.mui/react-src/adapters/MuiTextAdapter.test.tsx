// The wire contract of TLText, checked against the MUI adapter.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { cleanup } from '@testing-library/react';
import { TOOLTIP_ATTR, replacePart } from 'tl-react-bridge';
import type { TextStateJson } from 'tl-react-bridge';
import MuiTextAdapter from './MuiTextAdapter';
import { CONTROL_ID, mountAdapter, styleRules } from './wire-test-support';
import MuiChipAdapter from './MuiChipAdapter';

// A text drawn as a pill.
replacePart('Chip', MuiChipAdapter);

function mountText(state: Partial<TextStateJson>) {
  return mountAdapter(MuiTextAdapter, { text: 'Hallo', ...state });
}

/** The text color the MUI styles declare for the given element, without resolving variables. */
function declaredColor(element: Element): string {
  let declared = '';
  for (const rule of styleRules()) {
    if (element.matches(rule.selectorText)) {
      declared = rule.style.getPropertyValue('color') || declared;
    }
  }
  return declared;
}

function root(): HTMLElement {
  return document.getElementById(CONTROL_ID)!;
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe('TLText as MUI Typography', () => {
  it('renders running text as body2 in the primary text color', () => {
    mountText({ cssClass: 'my-text' });

    const text = root();
    expect(text.tagName).toBe('SPAN');
    expect(text.textContent).toBe('Hallo');
    expect(text.classList).toContain('MuiTypography-body2');
    expect(text.classList).toContain('my-text');
    expect(text.classList).not.toContain('MuiTypography-noWrap');
  });

  it('maps each typographic role to an MUI variant', () => {
    const expected: Record<TextStateJson.Variant, string> = {
      body: 'body2', title: 'h6', headline: 'h5', display: 'h4', label: 'subtitle2', caption: 'caption',
    };
    for (const [variant, muiVariant] of Object.entries(expected)) {
      mountText({ variant: variant as TextStateJson.Variant });
      expect(root().classList).toContain('MuiTypography-' + muiVariant);
      expect(root().tagName).toBe('SPAN');
      cleanup();
    }
  });

  it('draws each tone in its palette color', () => {
    const expected: Record<TextStateJson.Tone, string> = {
      primary: 'var(--mui-palette-text-primary)',
      secondary: 'var(--mui-palette-text-secondary)',
      helper: 'var(--mui-palette-text-secondary)',
      placeholder: 'var(--mui-palette-text-disabled)',
      success: 'var(--mui-palette-success-main)',
      warning: 'var(--mui-palette-warning-main)',
      error: 'var(--mui-palette-error-main)',
      'on-color': 'var(--tl-text-on-brand)',
    };
    for (const [tone, color] of Object.entries(expected)) {
      mountText({ tone: tone as TextStateJson.Tone });
      expect(declaredColor(root()), tone).toBe(color);
      cleanup();
    }
  });

  it('truncates on a single line with overflow ellipsis', () => {
    mountText({ overflow: 'ellipsis' });

    expect(root().classList).toContain('MuiTypography-noWrap');
  });

  it('carries its ARIA role and the rich tooltip of the control', () => {
    mountText({ role: 'alert', hasTooltip: true });

    expect(root().getAttribute('role')).toBe('alert');
    expect(root().getAttribute(TOOLTIP_ATTR)).toBe('key:tooltip');
  });

  it('declares no tooltip without one', () => {
    mountText({});

    expect(root().hasAttribute(TOOLTIP_ATTR)).toBe(false);
  });

  it('draws a text with a color role as the part Chip of that role, inside a span with role and tooltip', () => {
    mountText({ colorRole: 'success', role: 'status', hasTooltip: true, cssClass: 'my-class' });

    expect(root().tagName).toBe('SPAN');
    expect(root().getAttribute('role')).toBe('status');
    expect(root().getAttribute(TOOLTIP_ATTR)).toBe('key:tooltip');
    expect(root().classList).toContain('my-class');
    const chip = root().querySelector('.MuiChip-root')!;
    expect(chip.classList).toContain('MuiChip-sizeSmall');
    expect(chip.classList).toContain('MuiChip-colorSuccess');
    expect(chip.textContent).toBe('Hallo');
  });

  it('draws a pill without a role of its own in the role of its tone', () => {
    mountText({ appearance: 'pill', tone: 'warning' });

    expect(root().querySelector('.MuiChip-colorWarning')).not.toBeNull();
  });

  it('never draws an empty text as a pill', () => {
    mountText({ text: '', appearance: 'pill', colorRole: 'error' });

    expect(root().querySelector('.MuiChip-root')).toBeNull();
    expect(root().classList).toContain('MuiTypography-root');
  });

  it('renders nothing while hidden', () => {
    mountText({ hidden: true });

    expect(document.getElementById(CONTROL_ID)).toBeNull();
  });
});
