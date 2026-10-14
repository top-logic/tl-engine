// The wire contract of TLAvatar, checked against the MUI adapter.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup } from '@testing-library/react';
import { avatarCategory, tooltipProps } from 'tl-react-bridge';
import type { AvatarStateJson } from 'tl-react-bridge';
import type { ThemeOptions } from '@mui/material/styles';
import MuiAvatarAdapter, { CATEGORY_ATTRIBUTE, SIZE_ATTRIBUTE } from './MuiAvatarAdapter';
import { CONTROL_ID, mountAdapter, styleRules } from './wire-test-support';

/** A display name and the category it maps to. */
const NAME = 'Ada Lovelace';
const CATEGORY = avatarCategory(NAME);

/** The attribute and value of the tooltip of the name. */
const [TOOLTIP_ATTR, TOOLTIP] = Object.entries(tooltipProps(NAME))[0];

function mountAvatar(state: Partial<AvatarStateJson>, theme?: ThemeOptions) {
  return mountAdapter(MuiAvatarAdapter, state, { theme });
}

function root(): HTMLElement {
  return document.getElementById(CONTROL_ID)!;
}

/** The values the style rules matching the given element give the given property, in rule order. */
function declaredValues(element: Element, property: string): string[] {
  return styleRules()
    .filter(rule => element.matches(rule.selectorText))
    .map(rule => rule.style.getPropertyValue(property))
    .filter(Boolean);
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe('TLAvatar as MUI Avatar', () => {
  it('shows the picture with the name as its text alternative', () => {
    mountAvatar({ url: '/images/ada.png', name: NAME });

    const image = screen.getByRole('img', { name: NAME });
    expect(image.tagName).toBe('IMG');
    expect(image.getAttribute('src')).toBe('/images/ada.png');
    expect(root().classList).toContain('MuiAvatar-root');
    expect(root().getAttribute(TOOLTIP_ATTR)).toBe(TOOLTIP);
  });

  it('loads the picture the control serves from its data endpoint, with the revision', () => {
    mountAvatar({ hasData: true, dataRevision: 3 });

    const image = root().querySelector('img')!;
    expect(image.getAttribute('src')).toMatch(/react-api\/data\?controlId=c1.*&rev=3$/);
    expect(image.getAttribute('alt')).toBe('');
  });

  it('shows the initials on the category of the name, named for assistive technology', () => {
    mountAvatar({ name: NAME, cssClass: 'my-avatar' });

    const avatar = screen.getByRole('img', { name: NAME });
    expect(avatar).toBe(root());
    expect(avatar.textContent).toBe('AL');
    expect(avatar.getAttribute(CATEGORY_ATTRIBUTE)).toBe(String(CATEGORY));
    expect(avatar.getAttribute(TOOLTIP_ATTR)).toBe(TOOLTIP);
    expect(avatar.classList).toContain('my-avatar');
    expect(declaredValues(avatar, 'background-color')).toContain(`var(--tl-category-${CATEGORY}-subtle)`);
  });

  it('shows the empty neutral circle without a name, hidden from assistive technology', () => {
    mountAvatar({});

    const avatar = root();
    expect(avatar.getAttribute('aria-hidden')).toBe('true');
    expect(avatar.hasAttribute(CATEGORY_ATTRIBUTE)).toBe(false);
    expect(avatar.hasAttribute(TOOLTIP_ATTR)).toBe(false);
    expect(avatar.textContent).toBe('');
    expect(avatar.querySelector('svg')).toBeNull();
    expect(declaredValues(avatar, 'background-color')).toContain('var(--tl-surface-interactive-selected)');
  });

  it('takes the diameter of its size, medium without one', () => {
    mountAvatar({ name: NAME, size: 'x-large' });
    expect(root().getAttribute(SIZE_ATTRIBUTE)).toBe('x-large');
    expect(declaredValues(root(), 'width')).toContain('var(--tl-size-avatar-xl)');
    cleanup();

    mountAvatar({ name: NAME });
    expect(root().getAttribute(SIZE_ATTRIBUTE)).toBe('medium');
    expect(declaredValues(root(), 'width')).toContain('var(--tl-size-avatar-md)');
  });

  it('renders nothing while hidden', () => {
    mountAvatar({ name: NAME, hidden: true });

    expect(root()).toBeNull();
  });

  it('lets the style overrides of the theme restyle a category', () => {
    const selector = `&[${CATEGORY_ATTRIBUTE}="${CATEGORY}"]`;
    mountAvatar({ name: NAME }, {
      components: { MuiAvatar: { styleOverrides: { root: { [selector]: { backgroundColor: 'rgb(1, 2, 3)' } } } } },
    });

    const values = declaredValues(root(), 'background-color');
    expect(values).toContain(`var(--tl-category-${CATEGORY}-subtle)`);
    expect(values[values.length - 1]).toBe('rgb(1, 2, 3)');
    expect(getComputedStyle(root()).backgroundColor).toBe('rgb(1, 2, 3)');
  });
});
