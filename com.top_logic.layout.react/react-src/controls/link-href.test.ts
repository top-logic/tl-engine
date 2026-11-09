import { describe, expect, it } from 'vitest';

import { linkHref } from './link-href';

describe('linkHref', () => {
  it('links a web address as it is written', () => {
    expect(linkHref('url', 'https://www.top-logic.com/')).toBe('https://www.top-logic.com/');
    expect(linkHref('url', 'docs/help.html')).toBe('docs/help.html');
  });

  it('makes no link of an address running script', () => {
    expect(linkHref('url', 'javascript:alert(1)')).toBeNull();
    expect(linkHref('url', ' JavaScript:alert(1)')).toBeNull();
    expect(linkHref('url', 'data:text/html,<script>alert(1)</script>')).toBeNull();
  });

  it('makes no link of an empty value', () => {
    expect(linkHref('url', '')).toBeNull();
  });
});
