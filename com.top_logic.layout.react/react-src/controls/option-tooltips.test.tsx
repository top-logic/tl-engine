import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { act } from 'react';

vi.mock('../bridge/sse-client', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../bridge/sse-client')>()),
  connect: () => {},
  subscribe: () => {},
  unsubscribe: () => {},
}));

// The bridge first, as the page loads it: the bridge entry reaches it through modules that the SSE client imports
// as well, and these must see the mocked SSE client.
import { mount, unmount } from '../bridge/tl-react-bridge';
import { OPTION_TOOLTIP_PREFIX, TOOLTIP_ATTR, register, registerPart } from 'tl-react-bridge';
import TLChoiceGroup from './TLChoiceGroup';
import TLOptionChips from './TLOptionChips';
import TLSegmentedChoice from './TLSegmentedChoice';
import CheckboxPart from './form/CheckboxPart';
import ChipPart from './chip/ChipPart';
import ButtonPart from './button/ButtonPart';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

/** The ID of the mounted select field. */
const GROUP_ID = 'group';

/** The label of the choice of no option. */
const NONE = 'None';

const ALPHA = { value: 'a', label: 'Alpha' };
const BETA = { value: 'b', label: 'Beta' };
const OPTIONS = [ALPHA, BETA];

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  if (typeof globalThis.CSS === 'undefined') {
    globalThis.CSS = { escape: (value: string) => value } as unknown as typeof CSS;
  }
  registerPart('Checkbox', CheckboxPart);
  registerPart('Chip', ChipPart);
  registerPart('Button', ButtonPart);
  register('TLChoiceGroup', TLChoiceGroup);
  register('TLOptionChips', TLOptionChips);
  register('TLSegmentedChoice', TLSegmentedChoice);
});

afterEach(async () => {
  act(() => unmount(GROUP_ID));
  document.getElementById(GROUP_ID)?.remove();
  vi.unstubAllGlobals();
  await settle();
});

afterAll(async () => {
  document.body.replaceChildren();
  await settle();
});

/** Mounts the select field drawn by the given module with the given state. */
function mountField(module: string, state: Record<string, unknown>): void {
  vi.stubGlobal('fetch', vi.fn(async () =>
    new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } })));
  const element = document.createElement('div');
  element.id = GROUP_ID;
  document.body.appendChild(element);
  act(() => mount(GROUP_ID, module,
    { value: [], options: OPTIONS, emptyOptionLabel: NONE, ...state }, 'w', '/ctx'));
}

/** Mounts the choice group with the given state. */
function mountGroup(state: Record<string, unknown>): void {
  mountField('TLChoiceGroup', { display: 'radio', ...state });
}

/** Waits until the translations the group asked for have arrived. */
async function settle(): Promise<void> {
  await act(async () => {
    for (let round = 0; round < 5; round++) {
      await new Promise<void>(resolve => setTimeout(resolve, 0));
    }
  });
}

/** The tooltip declaration of the given option. */
function optionTooltip(optionValue: string): string {
  return `key:${OPTION_TOOLTIP_PREFIX}${optionValue}`;
}

/** The tooltip declared closest around the given element, `null` for none. */
function declaredAt(element: Element): string | null {
  return element.closest(`[${TOOLTIP_ATTR}]`)?.getAttribute(TOOLTIP_ATTR) ?? null;
}

describe('the explanation of an option of a choice group', () => {
  it('is declared as tooltip of its radio button with its text, but not of the choice of no option', () => {
    mountGroup({});

    const radios = [...document.querySelectorAll<HTMLInputElement>('input[type="radio"]')];
    expect(radios.map(declaredAt)).toEqual([null, optionTooltip('a'), optionTooltip('b')]);
    const label = [...document.querySelectorAll('label')].find(candidate => candidate.textContent === 'Beta')!;
    expect(label.getAttribute(TOOLTIP_ATTR)).toBe(optionTooltip('b'));
  });

  it('is declared as tooltip of its checkbox and of the label beside it', () => {
    mountGroup({ multiSelect: true });

    const boxes = [...document.querySelectorAll<HTMLInputElement>('input[type="checkbox"]')];
    expect(boxes.map(box => box.getAttribute(TOOLTIP_ATTR))).toEqual([optionTooltip('a'), optionTooltip('b')]);
    const text = [...document.querySelectorAll('span')].find(candidate => candidate.textContent === 'Alpha'
      && candidate.hasAttribute(TOOLTIP_ATTR))!;
    expect(text.getAttribute(TOOLTIP_ATTR)).toBe(optionTooltip('a'));
  });
});

describe('the explanation of an option chip', () => {
  it('is declared as tooltip of the chip of every option', () => {
    mountField('TLOptionChips', { display: 'chips', value: [ALPHA] });

    const chips = [...document.getElementById(GROUP_ID)!.querySelectorAll('[aria-pressed]')];
    expect(chips.map(chip => chip.textContent)).toEqual(['Alpha', 'Beta']);
    expect(chips.map(chip => chip.getAttribute(TOOLTIP_ATTR))).toEqual([optionTooltip('a'), optionTooltip('b')]);
  });
});

describe('the explanation of a segment', () => {
  it('is declared as tooltip of the segment of every option', () => {
    mountField('TLSegmentedChoice', { display: 'segmented', value: [BETA] });

    const segments = [...document.querySelectorAll('.tl-segmented__segment')];
    expect(segments.map(segment => segment.textContent)).toEqual(['Alpha', 'Beta']);
    expect(segments.map(segment => segment.getAttribute(TOOLTIP_ATTR)))
      .toEqual([optionTooltip('a'), optionTooltip('b')]);
  });
});
