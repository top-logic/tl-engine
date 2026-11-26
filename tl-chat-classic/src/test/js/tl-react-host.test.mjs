// Tests the selector rewrite of script/tl-react-host.js: the theme blocks of UIThemeService and the
// sheets of the design system are in the same cascade layer, so on the host of a classic page, as on
// a React page, the specificity decides which of them sets a role (--tl-*).
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import vm from 'node:vm';

const source = readFileSync(new URL('../../main/webapp/script/tl-react-host.js', import.meta.url), 'utf8');
const window = {};
vm.runInNewContext(source, { window });
const { scopedSelector } = window.TLReactHost;

/** The selectors of the theme blocks that UIThemeService writes. */
const THEME_SELECTORS = [':root[data-theme="dark"]', ':root:not([data-theme])'];

/** The selectors of the role blocks in tokens.css of the design system. */
const DESIGN_SYSTEM_SELECTORS = [
  ':root',
  '[data-tl-mode="light"]',
  '[data-tl-mode="dark"]',
  '[data-tl-density="normal"]',
  '[data-tl-density="compact"]',
];

/** The items of a selector list, split at the commas that are not inside a () or a []. */
function items(selector) {
  const result = [];
  let depth = 0;
  let start = 0;
  for (let i = 0; i < selector.length; i++) {
    const c = selector[i];
    if (c === '(' || c === '[') depth++;
    else if (c === ')' || c === ']') depth--;
    else if (c === ',' && depth === 0) {
      result.push(selector.slice(start, i).trim());
      start = i + 1;
    }
  }
  result.push(selector.slice(start).trim());
  return result;
}

/** The index after the bracket that closes the one at the given index. */
function closing(selector, open) {
  let depth = 0;
  for (let i = open; i < selector.length; i++) {
    if (selector[i] === '(' || selector[i] === '[') depth++;
    else if (selector[i] === ')' || selector[i] === ']') {
      depth--;
      if (depth === 0) return i + 1;
    }
  }
  throw new Error('Unbalanced: ' + selector);
}

/** The specificity [ids, classes, types] of one complex selector, for the forms used here. */
function specificity(selector) {
  const result = [0, 0, 0];
  let i = 0;
  while (i < selector.length) {
    const c = selector[i];
    if (c === '[') {
      result[1]++;
      i = closing(selector, i);
    } else if (c === '#' || c === '.') {
      result[c === '#' ? 0 : 1]++;
      i = i + 1 + /^[\w-]*/.exec(selector.slice(i + 1))[0].length;
    } else if (c === ':') {
      const name = /^:([\w-]+)/.exec(selector.slice(i))[1];
      i += 1 + name.length;
      if (selector[i] === '(') {
        const end = closing(selector, i);
        const argument = selector.slice(i + 1, end - 1);
        i = end;
        if (name === 'where') continue;
        if (name === 'not' || name === 'is') {
          const max = items(argument).map(specificity).sort(compare).pop();
          max.forEach((value, n) => { result[n] += value; });
          continue;
        }
      }
      result[1]++;
    } else if (/[a-z]/i.test(c)) {
      result[2]++;
      i += /^[\w-]+/.exec(selector.slice(i))[0].length;
    } else {
      i++;
    }
  }
  return result;
}

function compare(a, b) {
  return a[0] - b[0] || a[1] - b[1] || a[2] - b[2];
}

/**
 * The items of the rewritten selector that match the host, the scope root: those that name :scope
 * and have no combinator.
 */
function hostItems(selector) {
  return items(scopedSelector(selector))
    .filter((item) => item.includes(':scope') && !/\s/.test(item.replace(/\[[^\]]*\]/g, '')));
}

test('every theme block and every role block of the design system reaches the host', () => {
  for (const selector of [...THEME_SELECTORS, ...DESIGN_SYSTEM_SELECTORS]) {
    assert.notEqual(hostItems(selector).length, 0, selector + ' -> ' + scopedSelector(selector));
  }
});

test('a theme block has a higher specificity at the host than a role block of the design system', () => {
  for (const theme of THEME_SELECTORS) {
    for (const designSystem of DESIGN_SYSTEM_SELECTORS) {
      const themeSpecificity = hostItems(theme).map(specificity).sort(compare)[0];
      const designSystemSpecificity = hostItems(designSystem).map(specificity).sort(compare).pop();
      assert.ok(compare(themeSpecificity, designSystemSpecificity) > 0,
        scopedSelector(theme) + ' ' + themeSpecificity + ' must win over '
        + scopedSelector(designSystem) + ' ' + designSystemSpecificity);
    }
  }
});
