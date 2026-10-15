// The part Chip drawn with Material UI: a small MUI Chip, a toggle filled or outlined, removable by
// MUI's own delete.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup, render } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { TOOLTIP_ATTR } from 'tl-react-bridge';
import type { ChipPartProps } from 'tl-react-bridge';
import { createMuiRoot, createPageThemes } from '../MuiRoot';
import MuiChipAdapter from './MuiChipAdapter';

/** The root wrapper the adapter is drawn below: the one of the MUI default theme. */
const MuiRoot = createMuiRoot(createPageThemes({}));

function draw(props: Partial<ChipPartProps>) {
  render(<MuiRoot><MuiChipAdapter id="c1" label="Alpha" {...props} /></MuiRoot>);
  return document.getElementById('c1')!;
}

afterEach(() => {
  cleanup();
});

describe('the part Chip as MUI Chip', () => {
  it('draws a value as a small filled chip of its color role, with icon, detail, class and attributes', () => {
    const chip = draw({
      colorRole: 'success', icon: 'css:fa-solid fa-star', detail: '2 KB', tooltip: 'Tipp', className: 'x',
      'data-role': 'value',
    });

    expect(chip.classList).toContain('MuiChip-root');
    expect(chip.classList).toContain('MuiChip-sizeSmall');
    expect(chip.classList).toContain('MuiChip-filled');
    expect(chip.classList).toContain('MuiChip-colorSuccess');
    expect(chip.classList).toContain('x');
    expect(chip.getAttribute('data-role')).toBe('value');
    expect(chip.getAttribute(TOOLTIP_ATTR)).toBe('text:Tipp');
    expect(chip.querySelector('.MuiChip-icon')!.className).toContain('fa-star');
    expect(chip.querySelector('.MuiChip-label')!.textContent).toBe('Alpha2 KB');
    expect(screen.queryByRole('button')).toBeNull();
  });

  it('maps the color roles to the MUI colors, the ones without a counterpart to the default', () => {
    expect(draw({ colorRole: 'brand' }).classList).toContain('MuiChip-colorPrimary');
    cleanup();
    expect(draw({ colorRole: 'category-3' }).classList).toContain('MuiChip-colorDefault');
    cleanup();
    expect(draw({ colorRole: 'neutral' }).classList).toContain('MuiChip-colorDefault');
  });

  it('draws a toggle filled and primary while pressed, outlined while not', async () => {
    const onClick = vi.fn();
    const pressed = draw({ pressed: true, onClick });

    expect(screen.getByRole('button', { name: 'Alpha' })).toBe(pressed);
    expect(pressed.getAttribute('aria-pressed')).toBe('true');
    expect(pressed.classList).toContain('MuiChip-filled');
    expect(pressed.classList).toContain('MuiChip-colorPrimary');
    await userEvent.click(pressed);
    expect(onClick).toHaveBeenCalledTimes(1);
    cleanup();

    const released = draw({ pressed: false, colorRole: 'info', onClick });
    expect(released.getAttribute('aria-pressed')).toBe('false');
    expect(released.classList).toContain('MuiChip-outlined');
    expect(released.classList).toContain('MuiChip-colorInfo');
  });

  it('draws a toggle in the color of the state of its value', () => {
    expect(draw({ pressed: false, colorRole: 'success', validation: 'error', onClick: vi.fn() }).classList)
      .toContain('MuiChip-colorError');
    cleanup();
    expect(draw({ pressed: true, validation: 'warning', onClick: vi.fn() }).classList)
      .toContain('MuiChip-colorWarning');
  });

  it('removes its value by the delete icon, which does not press the chip', async () => {
    const onClick = vi.fn();
    const onRemove = vi.fn();
    const chip = draw({ onClick, onRemove, removeLabel: 'Alpha entfernen' });

    const icon = chip.querySelector('.MuiChip-deleteIcon')!;
    expect(icon.getAttribute(TOOLTIP_ATTR)).toBe('text:Alpha entfernen');
    expect(icon.getAttribute('aria-hidden')).toBe('true');
    expect(chip.getAttribute('aria-keyshortcuts')).toBe('Backspace Delete');
    // The name of the chip is its label alone.
    expect(screen.getByRole('button', { name: 'Alpha' })).toBe(chip);
    await userEvent.click(icon);

    expect(onRemove).toHaveBeenCalledTimes(1);
    expect(onClick).not.toHaveBeenCalled();
  });

  it('removes its value by the Delete key on the focused chip', async () => {
    const onRemove = vi.fn();
    const chip = draw({ onRemove, removeLabel: 'Alpha entfernen' });

    chip.focus();
    await userEvent.keyboard('{Delete}');

    expect(onRemove).toHaveBeenCalledTimes(1);
  });

  it('takes the given tab index', () => {
    expect(draw({ onRemove: vi.fn(), removeLabel: 'Alpha entfernen', tabIndex: -1 }).getAttribute('tabindex')).toBe('-1');
  });

  it('offers no removal without onRemove', () => {
    const chip = draw({ removeLabel: 'Alpha entfernen' });

    expect(chip.querySelector('.MuiChip-deleteIcon')).toBeNull();
    expect(chip.hasAttribute('aria-keyshortcuts')).toBe(false);
  });

  it('presses nothing while disabled', async () => {
    const onClick = vi.fn();
    const chip = draw({ pressed: false, onClick, disabled: true });

    await userEvent.setup({ pointerEventsCheck: 0 }).click(chip);

    expect(chip.classList).toContain('Mui-disabled');
    expect(onClick).not.toHaveBeenCalled();
  });
});
