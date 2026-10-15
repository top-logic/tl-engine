/** A chip that takes the focus itself, e.g. one whose value a key removes. */
const FOCUSABLE_CHIP = 'button:not([disabled]), [tabindex]:not([tabindex="-1"])';

/**
 * What takes the focus of each remaining chip after the removal of another (see
 * useFocusAfterRemove): the chip itself where it takes the focus, else its remove button, the last
 * button within it.
 *
 * <p>The chips are found by an attribute their control marks them with, so that the targets are the
 * same whichever implementation of the part `Chip` draws them.</p>
 *
 * @param container the element holding the chips
 * @param chipAttr the attribute marking a chip
 */
export function chipFocusTargets(container: HTMLElement | null, chipAttr: string): HTMLElement[] {
  const chips = container ? Array.from(container.querySelectorAll<HTMLElement>(`[${chipAttr}]`)) : [];
  return chips.flatMap((chip) => {
    if (chip.matches(FOCUSABLE_CHIP)) return [chip];
    const buttons = chip.querySelectorAll<HTMLElement>('button');
    return buttons.length > 0 ? [buttons[buttons.length - 1]] : [];
  });
}
