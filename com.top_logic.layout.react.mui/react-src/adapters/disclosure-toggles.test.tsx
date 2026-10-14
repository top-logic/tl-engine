// The toggles a control draws to show or hide content - the toggle of a form group, of a node in a
// tree, of a row in a tree table, of the sidebar - drawn by the MUI adapter of the part
// DisclosureToggle; and the help button of a form field, drawn by the MUI adapter of the part Button:
// one replacement of a part reaches all of them.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { replacePart } from 'tl-react-bridge';
import type { FormLayout } from 'tl-react-bridge';
import TLFormGroup from '../../../com.top_logic.layout.react/react-src/controls/TLFormGroup';
import TLFormField from '../../../com.top_logic.layout.react/react-src/controls/TLFormField';
import TLTreeView from '../../../com.top_logic.layout.react/react-src/controls/TLTreeView';
import TLTableView from '../../../com.top_logic.layout.react/react-src/controls/TLTableView';
import TLSidebar from '../../../com.top_logic.layout.react/react-src/controls/TLSidebar';
import MuiDisclosureToggleAdapter from './MuiDisclosureToggleAdapter';
import MuiCheckboxAdapter from './MuiCheckboxAdapter';
import MuiTextInputAdapter from './MuiTextInputAdapter';
import MuiButtonAdapter from './MuiButtonAdapter';
import { CONTROL_ID, childControl, mountAdapter, settle } from './wire-test-support';
import MuiChipAdapter from './MuiChipAdapter';
import MuiBadgeAdapter from './MuiBadgeAdapter';

replacePart('DisclosureToggle', MuiDisclosureToggleAdapter);
replacePart('Button', MuiButtonAdapter);
// The table looks up the selection boxes of its rows as well.
replacePart('Checkbox', MuiCheckboxAdapter);
// The table looks up the boxes of its search as well.
replacePart('TextInput', MuiTextInputAdapter);
// The table draws its named filters as chips, the sidebar its counts as badges.
replacePart('Chip', MuiChipAdapter);
replacePart('Badge', MuiBadgeAdapter);

/** The labels of the toggles: the keys, as the test stub translates each key to itself. */
const LABEL_GROUP_EXPAND = 'js.formGroup.expand';
const LABEL_GROUP_COLLAPSE = 'js.formGroup.collapse';
const LABEL_TREE_EXPAND = 'js.treeView.expand';
const LABEL_TREE_COLLAPSE = 'js.treeView.collapse';
const LABEL_ROW_EXPAND = 'js.table.expandRow';
const LABEL_ROW_COLLAPSE = 'js.table.collapseRow';
const LABEL_SIDEBAR_EXPAND = 'js.sidebar.expand';
const LABEL_SIDEBAR_COLLAPSE = 'js.sidebar.collapse';
const LABEL_HELP = 'js.formField.help';

/** The commands the controls send for their toggles. */
const CMD_TOGGLE_COLLAPSE = 'toggleCollapse';
const CMD_EXPAND = 'expand';
const CMD_COLLAPSE = 'collapse';

/** A form being edited. */
const EDIT_FORM: FormLayout = { readOnly: false, resolvedLabelPosition: 'top' };

/** A resize observer that never reports: jsdom lays nothing out. */
class NoResizeObserver {
  observe() {}
  unobserve() {}
  disconnect() {}
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

/** Asserts that the given toggle is an icon button of Material UI and not the toggle of TopLogic. */
function expectMuiToggle(toggle: HTMLElement) {
  expect(toggle.classList).toContain('MuiIconButton-root');
  expect(toggle.classList).not.toContain('tl-toggle');
}

describe('the toggle of a form group as MUI icon button', () => {
  it('discloses the body of the group and asks the server to collapse it', async () => {
    const sent = mountAdapter(TLFormGroup, { collapsible: true, collapsed: false, children: [childControl('x', 'Inhalt')] });
    await settle();

    const toggle = screen.getByRole('button', { name: LABEL_GROUP_COLLAPSE });
    expectMuiToggle(toggle);
    expect(toggle.getAttribute('aria-expanded')).toBe('true');
    expect(toggle.getAttribute('aria-controls')).toBe(CONTROL_ID + '-body');
    await userEvent.click(toggle);
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_TOGGLE_COLLAPSE, expect.anything());
  });

  it('is collapsed while the body is hidden', async () => {
    mountAdapter(TLFormGroup, { collapsible: true, collapsed: true, children: [childControl('x', 'Inhalt')] });
    await settle();

    const toggle = screen.getByRole('button', { name: LABEL_GROUP_EXPAND });
    expect(toggle.getAttribute('aria-expanded')).toBe('false');
    expect(document.getElementById(CONTROL_ID + '-body')!.hidden).toBe(true);
  });

  it('has no toggle when the group cannot be collapsed', async () => {
    mountAdapter(TLFormGroup, { collapsible: false, children: [childControl('x', 'Inhalt')] });
    await settle();

    expect(screen.queryByRole('button')).toBeNull();
  });
});

describe('the toggle of a tree node as MUI icon button', () => {
  function node(id: string, extra: Record<string, unknown>) {
    return {
      id, depth: 0, expandable: false, expanded: false, leaf: true, loading: false, selected: false,
      content: childControl(id + '-c', id), ...extra,
    };
  }

  it('expands a collapsed node, out of the tab order of the tree', async () => {
    const sent = mountAdapter(TLTreeView, {
      nodes: [node('n1', { expandable: true, leaf: false }), node('n2', {})],
    });
    await settle();

    const toggles = screen.getAllByRole('button');
    // The leaf has no toggle.
    expect(toggles).toHaveLength(1);
    const toggle = screen.getByRole('button', { name: LABEL_TREE_EXPAND });
    expectMuiToggle(toggle);
    expect(toggle.getAttribute('aria-expanded')).toBe('false');
    expect(toggle.getAttribute('tabindex')).toBe('-1');
    await userEvent.click(toggle);
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_EXPAND, { nodeId: 'n1' });
  });

  it('collapses an expanded node', async () => {
    const sent = mountAdapter(TLTreeView, { nodes: [node('n1', { expandable: true, expanded: true, leaf: false })] });
    await settle();

    const toggle = screen.getByRole('button', { name: LABEL_TREE_COLLAPSE });
    expect(toggle.getAttribute('aria-expanded')).toBe('true');
    await userEvent.click(toggle);
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_COLLAPSE, { nodeId: 'n1' });
  });

  it('shows a spinner while the children of the node are being loaded', async () => {
    mountAdapter(TLTreeView, { nodes: [node('n1', { expandable: true, leaf: false, loading: true })] });
    await settle();

    const toggle = screen.getByRole('button', { name: LABEL_TREE_EXPAND });
    expect(toggle.getAttribute('aria-busy')).toBe('true');
    expect(toggle.querySelector('.MuiCircularProgress-root')).not.toBeNull();
  });
});

describe('the toggle of a tree table row as MUI icon button', () => {
  function row(index: number, extra: Record<string, unknown>) {
    return { id: 'r' + index, index, selected: false, cells: { name: childControl('c' + index, 'Zeile') }, treeDepth: 0, ...extra };
  }

  function mountTreeTable(rows: unknown[]) {
    vi.stubGlobal('ResizeObserver', NoResizeObserver);
    return mountAdapter(TLTableView, {
      columns: [{ name: 'name', label: 'Name', width: 120, sortable: false }],
      rows,
      totalRowCount: rows.length,
      treeMode: true,
    });
  }

  it('expands a collapsed row; a row without children has no toggle', async () => {
    const sent = mountTreeTable([row(0, { expandable: true, expanded: false }), row(1, { expandable: false })]);
    await settle();

    const toggle = screen.getByRole('button', { name: LABEL_ROW_EXPAND });
    expectMuiToggle(toggle);
    expect(toggle.getAttribute('aria-expanded')).toBe('false');
    expect(toggle.classList).toContain('tlTableView__treeToggle');
    expect(screen.getAllByRole('button', { name: /js\.table\.(expand|collapse)Row/ })).toHaveLength(1);
    await userEvent.click(toggle);
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_EXPAND, { rowIndex: 0, expanded: true });
  });

  it('collapses an expanded row', async () => {
    const sent = mountTreeTable([row(0, { expandable: true, expanded: true })]);
    await settle();

    const toggle = screen.getByRole('button', { name: LABEL_ROW_COLLAPSE });
    expect(toggle.getAttribute('aria-expanded')).toBe('true');
    await userEvent.click(toggle);
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_EXPAND, { rowIndex: 0, expanded: false });
  });
});

describe('the toggle of the sidebar as MUI icon button', () => {
  it('collapses the expanded sidebar', async () => {
    const sent = mountAdapter(TLSidebar, { items: [], collapsed: false, drawerOpen: false });
    await settle();

    const toggle = screen.getByRole('button', { name: LABEL_SIDEBAR_COLLAPSE });
    expectMuiToggle(toggle);
    expect(toggle.getAttribute('aria-expanded')).toBe('true');
    expect(toggle.getAttribute('aria-controls')).toBe(CONTROL_ID + '-nav');
    expect(getComputedStyle(toggle.querySelector('svg')!).transform).toBe('rotate(180deg)');
    await userEvent.click(toggle);
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_TOGGLE_COLLAPSE, expect.anything());
  });

  it('is collapsed while the sidebar is', async () => {
    mountAdapter(TLSidebar, { items: [], collapsed: true, drawerOpen: false });
    await settle();

    expect(screen.getByRole('button', { name: LABEL_SIDEBAR_EXPAND }).getAttribute('aria-expanded')).toBe('false');
  });
});

describe('the help button of a form field as MUI icon button', () => {
  it('discloses the help text, drawn by the part Button', async () => {
    mountAdapter(TLFormField, { label: 'Name', field: childControl('i1', 'Eingabe'), helpText: 'Der volle Name.' },
      { formLayout: EDIT_FORM });
    await settle();

    const help = screen.getByRole('button', { name: LABEL_HELP });
    expect(help.classList).toContain('MuiIconButton-root');
    expect(help.classList).toContain('tl-form-field__help');
    expect(help.getAttribute('aria-expanded')).toBe('false');
    expect(help.getAttribute('aria-controls')).toBe(CONTROL_ID + '-help');
    expect(document.getElementById(CONTROL_ID + '-help')!.hidden).toBe(true);

    await userEvent.click(help);

    expect(help.getAttribute('aria-expanded')).toBe('true');
    expect(document.getElementById(CONTROL_ID + '-help')!.hidden).toBe(false);
  });

  it('has no help button for a field without help text', async () => {
    mountAdapter(TLFormField, { label: 'Name', field: childControl('i1', 'Eingabe') }, { formLayout: EDIT_FORM });
    await settle();

    expect(screen.queryByRole('button', { name: LABEL_HELP })).toBeNull();
  });
});
