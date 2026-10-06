// The JSX runtime the build hands to library code compiled with the automatic runtime (Material UI).
//
// Prebuilt library code calls `jsx(type, props, key)` with the children inside the props and the
// key as a separate argument. The runtime must keep those children, with and without a key.

import { describe, it, expect, afterEach } from 'vitest';
import { render, screen, cleanup } from '@testing-library/react';
import { jsx, jsxs, Fragment } from './react-jsx-runtime-shim';

afterEach(() => {
  cleanup();
});

describe('react/jsx-runtime shim', () => {
  it('keeps the children of a keyed element', () => {
    render(jsx('ul', { children: [jsx('li', { children: 'first' }, 'a'), jsx('li', { children: 'second' }, 'b')] }));

    expect(screen.getAllByRole('listitem').map(item => item.textContent)).toEqual(['first', 'second']);
  });

  it('keeps the children when the key argument is explicitly undefined', () => {
    render(jsx('p', { children: 'text' }, undefined));

    expect(screen.getByText('text').tagName).toBe('P');
  });

  it('renders static children lists and fragments', () => {
    render(jsx(Fragment, { children: jsxs('div', { role: 'group', children: ['one', jsx('b', { children: 'two' })] }, 'k') }));

    expect(screen.getByRole('group').textContent).toBe('onetwo');
  });
});
