// The part FieldValue drawn with Material UI: a Typography, a Link for a value pointing somewhere or
// running an action.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup, render } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import type { FieldValuePartProps } from 'tl-react-bridge';
import { createMuiRoot, createPageThemes } from '../MuiRoot';
import MuiFieldValueAdapter from './MuiFieldValueAdapter';

/** The root wrapper the adapter is drawn below: the one of the MUI default theme. */
const MuiRoot = createMuiRoot(createPageThemes({}));

function draw(props: Partial<FieldValuePartProps>) {
  render(<MuiRoot><MuiFieldValueAdapter id="v1" text="" {...props} /></MuiRoot>);
  return document.getElementById('v1')!;
}

afterEach(() => {
  cleanup();
});

describe('the part FieldValue as MUI Typography', () => {
  it('draws a single-line value in a span that does not wrap, with class and attributes', () => {
    const value = draw({ text: 'Hallo', className: 'x', 'aria-describedby': 'hint', 'data-role': 'value' });

    expect(value.tagName).toBe('SPAN');
    expect(value.textContent).toBe('Hallo');
    expect(value.classList).toContain('MuiTypography-body2');
    expect(value.classList).toContain('MuiTypography-noWrap');
    expect(value.classList).toContain('x');
    expect(value.getAttribute('aria-describedby')).toBe('hint');
    expect(value.getAttribute('data-role')).toBe('value');
    expect(screen.queryByRole('link')).toBeNull();
  });

  it('draws a value pointing somewhere as a link opening it in a window of its own', () => {
    const value = draw({ text: 'a@b.de', href: 'mailto:a@b.de' });

    expect(screen.getByRole('link', { name: 'a@b.de' })).toBe(value);
    expect(value.classList).toContain('MuiLink-root');
    expect(value.getAttribute('href')).toBe('mailto:a@b.de');
    expect(value.getAttribute('target')).toBe('_blank');
    expect(value.getAttribute('rel')).toBe('noopener noreferrer');
  });

  it('draws a multi-line value one block per line, without a link', () => {
    const value = draw({ text: 'a\n\nc', multiline: true, href: 'https://example.org' });

    expect(value.tagName).toBe('DIV');
    expect(Array.from(value.children).map(line => line.textContent)).toEqual(['a', ' ', 'c']);
    expect(screen.queryByRole('link')).toBeNull();
  });

  it('draws a preformatted multi-line value without wrapping its lines', () => {
    const value = draw({ text: 'a\n\tb', multiline: true, preformatted: true });

    expect(getComputedStyle(value).whiteSpace).toBe('pre');
    expect(Array.from(value.children).map(line => line.textContent)).toEqual(['a', '\tb']);
  });

  it('draws the action of a multi-line value beside it, in a grid carrying the class', () => {
    const value = draw({ text: 'a\nb', multiline: true, preformatted: true, className: 'x',
      action: <button type="button">Kopieren</button> });

    const grid = value.parentElement!;
    expect(grid.classList).toContain('x');
    expect(value.classList).not.toContain('x');
    expect(getComputedStyle(grid).display).toBe('grid');
    expect(screen.getByRole('button', { name: 'Kopieren' }).parentElement).toBe(grid);
  });

  it('draws an empty value as an empty element', () => {
    const value = draw({ text: '' });

    expect(value.textContent).toBe('');
  });

  it('draws a value running an action as a link that is a button', async () => {
    const onClick = vi.fn();
    const value = draw({ text: 'Alpha', onClick });

    expect(screen.getByRole('button', { name: 'Alpha' })).toBe(value);
    expect(value.classList).toContain('MuiLink-root');
    expect(value.hasAttribute('href')).toBe(false);
    await userEvent.click(value);
    expect(onClick).toHaveBeenCalledTimes(1);
  });

  it('draws the icon ahead of the text', () => {
    const value = draw({ text: 'Alpha', icon: 'css:fa-solid fa-star' });

    expect(value.firstElementChild!.className).toBe('fa-solid fa-star tl-icon-sm');
    expect(value.textContent).toBe('Alpha');
  });

  it('draws a multi-line value without icon and action', () => {
    const value = draw({ text: 'a\nb', multiline: true, icon: 'css:fa-solid fa-star', onClick: vi.fn() });

    expect(value.tagName).toBe('DIV');
    expect(value.querySelector('i')).toBeNull();
    expect(screen.queryByRole('button')).toBeNull();
  });
});
