// The part Badge drawn with Material UI: the MUI Badge, standing on its own.

import { describe, it, expect, afterEach } from 'vitest';
import { cleanup, render } from '@testing-library/react';
import type { BadgePartProps } from 'tl-react-bridge';
import { createMuiRoot, createPageThemes } from '../MuiRoot';
import MuiBadgeAdapter from './MuiBadgeAdapter';
import { styleRules } from './wire-test-support';

/** The root wrapper the adapter is drawn below: the one of the MUI default theme. */
const MuiRoot = createMuiRoot(createPageThemes({}));

function draw(props: Partial<BadgePartProps>) {
  render(<MuiRoot><MuiBadgeAdapter id="b1" content="3" {...props} /></MuiRoot>);
  return document.getElementById('b1')!;
}

afterEach(() => {
  cleanup();
});

describe('the part Badge as MUI Badge', () => {
  it('draws the content in a primary badge, with class and attributes on the root', () => {
    const badge = draw({ content: '12', className: 'x', 'aria-label': '12 neu' });

    expect(badge.classList).toContain('MuiBadge-root');
    expect(badge.classList).toContain('x');
    expect(badge.getAttribute('aria-label')).toBe('12 neu');
    const marker = badge.querySelector('.MuiBadge-badge')!;
    expect(marker.classList).toContain('MuiBadge-colorPrimary');
    expect(marker.textContent).toBe('12');
  });

  it('stands on its own instead of over the corner of an element it wraps', () => {
    const badge = draw({});

    expect(badge.children).toHaveLength(1);
    const rules = styleRules().filter(rule => Array.from(badge.classList).some(name =>
      rule.selectorText.startsWith('.' + name + ' .MuiBadge-badge')));
    expect(rules.map(rule => rule.style.position)).toContain('static');
  });

  it('shows a content that is no number as it is', () => {
    expect(draw({ content: 'neu' }).querySelector('.MuiBadge-badge')!.textContent).toBe('neu');
  });
});
