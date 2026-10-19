// The progress bars and spinners a control draws - the bar of a progress control and of a job
// status, the bar of a dropdown loading its options, the spinner of a tree node and of the icon popup
// loading their content - drawn by the MUI adapters of the parts ProgressBar and Spinner: one
// replacement of a part reaches all of them.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { replacePart } from 'tl-react-bridge';
import TLProgress from '../../../com.top_logic.layout.react/react-src/controls/TLProgress';
import TLJobStatus from '../../../com.top_logic.layout.react/react-src/controls/TLJobStatus';
import TLDropdownSelect from '../../../com.top_logic.layout.react/react-src/controls/TLDropdownSelect';
import TLTreeView from '../../../com.top_logic.layout.react/react-src/controls/TLTreeView';
import IconSelectPopup from '../../../com.top_logic.layout.react/react-src/controls/icon/IconSelectPopup';
import MuiProgressBarAdapter from './MuiProgressBarAdapter';
import MuiSpinnerAdapter from './MuiSpinnerAdapter';
import MuiButtonAdapter from './MuiButtonAdapter';
import MuiTextInputAdapter from './MuiTextInputAdapter';
import MuiFieldValueAdapter from './MuiFieldValueAdapter';
import MuiChipAdapter from './MuiChipAdapter';
import MuiDisclosureToggleAdapter from './MuiDisclosureToggleAdapter';
import { CONTROL_ID, childControl, mountAdapter, settle } from './wire-test-support';

replacePart('ProgressBar', MuiProgressBarAdapter);
replacePart('Spinner', MuiSpinnerAdapter);
// The job status draws its cancel button, the dropdown its filter, value and chips, the tree and
// the icon popup their toggles, filter and buttons.
replacePart('Button', MuiButtonAdapter);
replacePart('TextInput', MuiTextInputAdapter);
replacePart('FieldValue', MuiFieldValueAdapter);
replacePart('Chip', MuiChipAdapter);
replacePart('DisclosureToggle', MuiDisclosureToggleAdapter);

/** The texts of the controls: the keys, as the test stub translates each key to itself. */
const LABEL_DROPDOWN_LOADING = 'js.dropdownSelect.loading';
const LABEL_ICONS_LOADING = 'js.iconSelect.loading';
const LABEL_TREE_LOADING = 'js.tree.loading';
const LABEL_TREE_EXPAND = 'js.treeView.expand';

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

/** Asserts that the given element is a bar of Material UI and not the one of TopLogic. */
function expectMuiBar(bar: HTMLElement) {
  expect(bar.classList).toContain('MuiLinearProgress-root');
  expect(document.querySelector('.tl-progress')).toBeNull();
}

describe('the progress control (TLProgress) as MUI progress bar', () => {
  it('draws the fraction as a determinate MUI bar with the label beside it, on the element of the control', () => {
    mountAdapter(TLProgress, { fraction: 0.75, label: '3 / 4', cssClass: 'my-progress' });

    const bar = screen.getByRole('progressbar', { name: '3 / 4' });
    expectMuiBar(bar);
    expect(bar.classList).toContain('MuiLinearProgress-determinate');
    expect(bar.getAttribute('aria-valuenow')).toBe('75');
    const root = document.getElementById(CONTROL_ID)!;
    expect(root.classList).toContain('my-progress');
    expect(root.textContent).toBe('3 / 4');
  });

  it('draws an indeterminate MUI bar without a fraction, busy and without a value', () => {
    mountAdapter(TLProgress, { fraction: null });

    const bar = screen.getByRole('progressbar');
    expectMuiBar(bar);
    expect(bar.classList).toContain('MuiLinearProgress-indeterminate');
    expect(bar.hasAttribute('aria-valuenow')).toBe(false);
    expect(bar.getAttribute('aria-busy')).toBe('true');
    expect(document.getElementById(CONTROL_ID)!.textContent).toBe('');
  });

  it('draws nothing while hidden', () => {
    mountAdapter(TLProgress, { fraction: 0.5, hidden: true });

    expect(screen.queryByRole('progressbar')).toBeNull();
  });
});

describe('the bar of a job status (TLJobStatus) as MUI progress bar', () => {
  it('draws the share of a running job as a determinate MUI bar', () => {
    mountAdapter(TLJobStatus, { status: 'running', fraction: 0.4 });

    const bar = screen.getByRole('progressbar');
    expectMuiBar(bar);
    expect(bar.getAttribute('aria-valuenow')).toBe('40');
    expect(bar.closest('.tl-job-status__bar')).not.toBeNull();
  });

  it('draws a running job without a share as an indeterminate MUI bar', () => {
    mountAdapter(TLJobStatus, { status: 'running', fraction: null });

    const bar = screen.getByRole('progressbar');
    expect(bar.classList).toContain('MuiLinearProgress-indeterminate');
    expect(bar.getAttribute('aria-busy')).toBe('true');
  });

  it('draws no bar for a finished job that never knew its share', () => {
    mountAdapter(TLJobStatus, { status: 'completed', fraction: null });

    expect(screen.queryByRole('progressbar')).toBeNull();
  });
});

describe('the loading options of a dropdown (TLDropdownSelect) as MUI progress bar', () => {
  async function openDropdown(state: Record<string, unknown>) {
    // jsdom lays nothing out and has no scrolling; the list scrolls the highlighted option into view.
    Element.prototype.scrollIntoView = () => {};
    mountAdapter(TLDropdownSelect, { value: [], ...state });
    await settle();
    await userEvent.click(screen.getByRole('combobox'));
    await settle();
  }

  it('shows an indeterminate MUI bar with the loading text while the options are being loaded', async () => {
    await openDropdown({ options: [], optionsLoaded: false });

    const bar = screen.getByRole('progressbar', { name: LABEL_DROPDOWN_LOADING });
    expectMuiBar(bar);
    expect(bar.classList).toContain('MuiLinearProgress-indeterminate');
    expect(bar.closest('.tl-status')!.textContent).toBe(LABEL_DROPDOWN_LOADING);
  });

  it('shows no bar once the options are loaded', async () => {
    await openDropdown({ options: [{ value: 'a', label: 'Alpha' }], optionsLoaded: true });

    expect(screen.queryByRole('progressbar')).toBeNull();
    expect(screen.getAllByRole('option')).toHaveLength(1);
  });
});

describe('the loading children of a tree node (TLTreeView) as MUI progress bar', () => {
  function node(loading: boolean) {
    return {
      id: 'n1', depth: 0, expandable: true, expanded: false, leaf: false, loading, selected: false,
      content: childControl('n1-c', 'Knoten'),
    };
  }

  it('shows a status line below a node whose children are being loaded, its MUI bar hidden from assistive technology', async () => {
    mountAdapter(TLTreeView, { nodes: [node(true)] });
    await settle();

    const status = screen.getByRole('treeitem', { name: LABEL_TREE_LOADING });
    const bar = status.querySelector<HTMLElement>('.MuiLinearProgress-root')!;
    expect(bar.classList).toContain('MuiLinearProgress-indeterminate');
    expect(bar.closest('[aria-hidden="true"]')).not.toBeNull();
    expect(screen.queryByRole('progressbar')).toBeNull();
    // The toggle stays a chevron: the status line says that the node is loading.
    const toggle = screen.getByRole('button', { name: LABEL_TREE_EXPAND });
    expect(toggle.querySelector('.MuiCircularProgress-root')).toBeNull();
  });

  it('shows no status line below a node that is not being loaded', async () => {
    mountAdapter(TLTreeView, { nodes: [node(false)] });
    await settle();

    expect(screen.queryByRole('treeitem', { name: LABEL_TREE_LOADING })).toBeNull();
    expect(document.querySelector('.MuiLinearProgress-root')).toBeNull();
  });
});

describe('the busy indicator of the icon popup as MUI spinner', () => {
  const ICONS = [{ prefix: 'house', label: 'Haus', variants: [{ encoded: 'css:fa-solid fa-house' }] }];

  function mountPopup(iconsLoaded: boolean) {
    const anchorRef = { current: document.createElement('button') };
    mountAdapter(() => (
      <IconSelectPopup anchorRef={anchorRef} currentValue={null} icons={iconsLoaded ? ICONS : []}
        iconsLoaded={iconsLoaded} onSelect={() => {}} onCancel={() => {}} onLoadIcons={async () => {}} />
    ), {});
  }

  it('shows a medium MUI spinner named by the loading text while the icons are being loaded', async () => {
    mountPopup(false);
    await settle();

    const spinner = screen.getByRole('progressbar', { name: LABEL_ICONS_LOADING });
    expect(spinner.classList).toContain('MuiCircularProgress-root');
    expect(spinner.style.width).toBe('24px');
    expect(spinner.closest('.tl-status')).not.toBeNull();
    expect(document.querySelector('.tl-spinner')).toBeNull();
  });

  it('shows no spinner once the icons are loaded', async () => {
    mountPopup(true);
    await settle();

    expect(screen.queryByRole('progressbar')).toBeNull();
    expect(screen.getAllByRole('option')).toHaveLength(1);
  });
});
