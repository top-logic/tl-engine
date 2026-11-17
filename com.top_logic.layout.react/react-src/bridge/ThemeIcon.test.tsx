import React, { act } from 'react';
import { createRoot } from 'react-dom/client';
import type { Root } from 'react-dom/client';
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it } from 'vitest';
import { ThemeIcon } from './ThemeIcon';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

/** The encoded form of image data, as the server sends an image stored in the model. */
const DATA_URI = 'data:image/png;base64,AQID';

let container: HTMLDivElement;
let root: Root;

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
});

afterAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = false;
});

beforeEach(() => {
  container = document.createElement('div');
  document.body.appendChild(container);
  root = createRoot(container);
});

afterEach(() => {
  act(() => root.unmount());
  container.remove();
});

function render(encoded: string, className?: string): Element | null {
  act(() => root.render(<ThemeIcon encoded={encoded} className={className} />));
  return container.firstElementChild;
}

describe('ThemeIcon', () => {
  it('renders image data as image with the data URI as source', () => {
    const icon = render(DATA_URI, 'tl-icon-sm');

    expect(icon?.tagName).toBe('IMG');
    expect(icon?.getAttribute('src')).toBe(DATA_URI);
    expect(icon?.getAttribute('class')).toBe('tl-icon-sm');
  });

  it('renders the URL of an image file as image', () => {
    const icon = render('/app/themes/core/icons/edit.png');

    expect(icon?.tagName).toBe('IMG');
    expect(icon?.getAttribute('src')).toBe('/app/themes/core/icons/edit.png');
  });

  it('renders an icon font class unchanged', () => {
    const icon = render('css:fa-solid fa-home', 'tl-icon-sm');

    expect(icon?.tagName).toBe('I');
    expect(icon?.getAttribute('class')).toBe('fa-solid fa-home tl-icon-sm');
  });

  it('renders nothing for the invisible image', () => {
    expect(render('none')).toBeNull();
  });
});
