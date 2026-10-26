import { React, ThemeIcon, Menu, usePart, pressClosedSurface, ICON_LIST_CLOSED, ICON_REMOVE } from 'tl-react-bridge';

const { useState, useRef, useCallback, useLayoutEffect } = React;

/** A filter criteria set the table offers under a name: a preset. */
export interface FilterPreset {
  id: string;
  label: string;
  /** Whether the preset is the user's own and can be deleted; a declared one cannot. */
  deletable: boolean;
}

/** The attribute naming the preset a chip of the row stands for. */
const PRESET_ATTR = 'data-tl-preset';

/** The class of the chip opening the menu of the presets that do not fit (the design system gives it `flex: none`). */
const MORE_CLASS = 'tl-table__more';

/** The classes of a row of elements that centers its children: the entry of a menu followed by its remove action. */
const ENTRY_CLASS = 'tl-stack tl-stack--row tl-stack--align-center';

/** The keys that delete the focused entry of the menu; the entry says so in `aria-keyshortcuts`. */
const DELETE_KEYS = ['Delete', 'Backspace'];

/**
 * The presets of the row if it shows `count` of them: the first `count` in declared order. The active preset
 * always belongs to the row: one beyond the first `count` takes the last place, the preset it displaces goes
 * to the menu, and without room for any preset the active one is the one that stays.
 */
export function rowOf<T extends { id: string }>(presets: readonly T[], activeId: string, count: number): T[] {
  const active = presets.findIndex((preset) => preset.id === activeId);
  const size = Math.min(presets.length, Math.max(count, active >= 0 ? 1 : 0));
  const row = presets.slice(0, size);
  if (active >= size) {
    row[size - 1] = presets[active];
  }
  return row;
}

/**
 * How many presets the row can show in the room given: the most for which the row - its chips, the chip
 * of the menu if some presets are left out, and the gaps between them - fits. A row that fits for no number
 * shows as few as {@link rowOf} allows.
 *
 * @param widths The width of the chip of each preset, by its ID.
 * @param room The width the row has.
 * @param gap The distance between two chips.
 * @param moreWidth The width of the chip of the menu.
 */
export function fittingCount<T extends { id: string }>(
  presets: readonly T[], activeId: string, widths: ReadonlyMap<string, number>, room: number, gap: number,
  moreWidth: number,
): number {
  for (let count = presets.length; count > 0; count--) {
    const row = rowOf(presets, activeId, count);
    const more = row.length < presets.length;
    const chips = row.reduce((sum, preset) => sum + (widths.get(preset.id) ?? 0), 0) + (more ? moreWidth : 0);
    const items = row.length + (more ? 1 : 0);
    if (chips + gap * Math.max(items - 1, 0) <= room) {
      return count;
    }
  }
  return 0;
}

/** The element of a chip that takes the focus: the chip itself, or the toggle inside a removable one. */
function focusTarget(chip: HTMLElement): HTMLElement | null {
  return chip.matches('button, [tabindex]') ? chip : chip.querySelector<HTMLElement>('button');
}

/** The width of the given element as laid out. */
function widthOf(element: Element): number {
  return element.getBoundingClientRect().width;
}

/**
 * What the filter bar needs to know of a preset to measure it: the label it was measured with, so that a
 * preset renamed is measured again.
 */
interface Measured {
  label: string;
  width: number;
}

export interface FilterPresetsProps {
  presets: readonly FilterPreset[];
  /** The ID of the preset in force; empty if none is. */
  activeId: string;
  /** What changes the width of a chip without a change of the presets (the density of the table). */
  densityKey: string;
  /** The attribute marking the chip of a preset that can be deleted. */
  chipAttr: string;
  /** The text of the chip of the menu, `{0}` standing for the number of presets in it. */
  moreText: string;
  /** The tooltip of the chip of the preset in force: it withdraws the preset. */
  clearLabel: string;
  /** The name of the means to delete a preset. */
  deleteLabel: string;
  /** Applies the preset, or withdraws it if it is the one in force. */
  onToggle(id: string): void;
  /** Deletes the preset; the event is the one of the remove action. */
  onDelete(id: string, event: React.SyntheticEvent<HTMLElement>): void;
}

/**
 * The presets of the filter bar: the chips that fit in the row of the bar, and behind a chip "+ n more" the
 * presets that do not, in a menu.
 *
 * <p>How many chips fit follows from the width: the bar offers what the other elements of its row (search,
 * saving) leave, and the widths of the chips are read once they have been laid out. A preset never measured
 * is laid out first - all of them are, before the first paint, as the layout effect measures and the state
 * it sets is rendered before the browser draws - then the row is cut back. The widths are kept, so a change
 * of the width of the bar needs no new layout of the presets. The chip of the menu only ever counts as wide as
 * the widest it has been, so that the digits of its count cannot make the row switch between two numbers.</p>
 *
 * <p>The menu holds plain entries, one per preset that is not in the row; the preset in force is never among
 * them, as the row keeps it, so no entry is "in force". A preset of the user's own carries its remove action
 * at the end of its entry (for the pointer: it is no stop of the keyboard); the key Delete or Backspace on the
 * focused entry deletes it.</p>
 *
 * <p>The component renders again only with a change of its props, so a table that changes for another reason
 * (scrolling, selection) neither measures nor touches the observer.</p>
 */
const FilterPresetsRow: React.FC<FilterPresetsProps> = ({
  presets, activeId, densityKey, chipAttr, moreText, clearLabel, deleteLabel, onToggle, onDelete,
}) => {
  const Chip = usePart('Chip');
  const Button = usePart('Button');
  const groupRef = useRef<HTMLSpanElement>(null);
  const moreRef = useRef<HTMLElement | null>(null);
  const [open, setOpen] = useState(false);
  /** How many presets the row shows; null while all of them are laid out to be measured. */
  const [count, setCount] = useState<number | null>(null);
  const measured = useRef(new Map<string, Measured>());
  const moreWidth = useRef(0);
  const measuredFor = useRef(densityKey);
  /** The preset chosen in the menu, which takes the focus once it stands in the row. */
  const pendingFocus = useRef<string | null>(null);
  /** The ID of the preset in force at the last render, to see a change of it. */
  const lastActive = useRef(activeId);
  /** The one observer of this mount, and what it observes. */
  const observer = useRef<ResizeObserver | null>(null);
  const observed = useRef(new Set<Element>());
  /** Withdraws the wish for the focus once the focus goes elsewhere. */
  const dropFocusWish = useRef<(() => void) | null>(null);

  const row = rowOf(presets, activeId, count ?? presets.length);
  const rowIds = new Set(row.map((preset) => preset.id));
  const menu = presets.filter((preset) => !rowIds.has(preset.id));

  /** Reads the chips that stand in the row and recomputes how many fit. */
  const measure = () => {
    const group = groupRef.current;
    const bar = group?.parentElement;
    if (!group || !bar) {
      return;
    }
    if (measuredFor.current !== densityKey) {
      measuredFor.current = densityKey;
      measured.current.clear();
      moreWidth.current = 0;
    }
    for (const chip of group.querySelectorAll<HTMLElement>(`[${PRESET_ATTR}]`)) {
      const preset = presets.find((candidate) => candidate.id === chip.getAttribute(PRESET_ATTR));
      const width = widthOf(chip);
      if (preset && width > 0) {
        measured.current.set(preset.id, { label: preset.label, width });
      }
    }
    const more = group.querySelector(`.${MORE_CLASS}`);
    if (more) {
      moreWidth.current = Math.max(moreWidth.current, widthOf(more));
    }

    const barWidth = widthOf(bar);
    const unknown = presets.some((preset) => measured.current.get(preset.id)?.label !== preset.label);
    if (barWidth <= 0 || (unknown && count === null)) {
      // Not laid out (hidden, or no layout at all), or laid out but not measurable: all of them stay.
      if (count !== null) {
        setCount(null);
      }
      return;
    }
    if (unknown) {
      // A preset new since the row was cut back: lay out all of them, then measure.
      setCount(null);
      return;
    }

    const style = getComputedStyle(bar);
    const others = Array.from(bar.children).filter((child) => child !== group && widthOf(child) > 0);
    const room = barWidth - (parseFloat(style.paddingLeft) || 0) - (parseFloat(style.paddingRight) || 0)
      - others.reduce((sum, child) => sum + widthOf(child), 0)
      - (parseFloat(style.columnGap) || 0) * others.length
      // A part of a pixel of rounding must not wrap the group into a row of its own.
      - 1;
    const widths = new Map(Array.from(measured.current, ([id, entry]) => [id, entry.width] as const));
    const fitting = fittingCount(presets, activeId, widths, room, parseFloat(getComputedStyle(group).columnGap) || 0,
      moreWidth.current);
    const next = fitting >= presets.length ? null : fitting;
    if (next !== count) {
      setCount(next);
    }
  };
  const measureRef = useRef(measure);
  measureRef.current = measure;

  // After every render, before the browser paints: what is laid out is read, and a row that has to change is
  // rendered again before anything shows. Idempotent - a state equal to the one rendered sets nothing - so
  // the chain of renders ends as soon as the row and the measures agree.
  useLayoutEffect(() => {
    measure();
    if (typeof ResizeObserver === 'undefined') {
      return;
    }
    // One observer for the mount; what it watches follows the elements that are there now.
    observer.current ??= new ResizeObserver(() => measureRef.current());
    const group = groupRef.current;
    const now = new Set<Element>(group?.querySelectorAll(`[${PRESET_ATTR}], .${MORE_CLASS}`) ?? []);
    if (group?.parentElement) {
      now.add(group.parentElement);
    }
    for (const element of observed.current) {
      if (!now.has(element)) {
        observer.current.unobserve(element);
        observed.current.delete(element);
      }
    }
    for (const element of now) {
      if (!observed.current.has(element)) {
        observer.current.observe(element);
        observed.current.add(element);
      }
    }
  });
  useLayoutEffect(() => () => {
    observer.current?.disconnect();
    observer.current = null;
    observed.current.clear();
    dropFocusWish.current?.();
  }, []);

  // The menu is for the presets that are not in the row: with none left, it closes.
  const menuEmpty = menu.length === 0;
  useLayoutEffect(() => {
    if (menuEmpty && open) {
      setOpen(false);
    }
  }, [menuEmpty, open]);

  // The preset chosen in the menu stands in the row once the server has applied it: the focus goes there, as
  // the chip of the menu may be gone with the last preset in it.
  useLayoutEffect(() => {
    const changed = lastActive.current !== activeId;
    lastActive.current = activeId;
    const chosen = pendingFocus.current;
    if (chosen === null) {
      return;
    }
    if (chosen !== activeId) {
      // Another preset has come into force than the one chosen: the wish is out of date.
      if (changed) {
        dropFocusWish.current?.();
      }
      return;
    }
    dropFocusWish.current?.();
    const active = document.activeElement;
    if (active && active !== document.body && active !== moreRef.current) {
      return;
    }
    const chip = Array.from(groupRef.current?.querySelectorAll<HTMLElement>(`[${PRESET_ATTR}]`) ?? [])
      .find((candidate) => candidate.getAttribute(PRESET_ATTR) === chosen);
    (chip ? focusTarget(chip) : null)?.focus();
  });

  const close = useCallback(() => setOpen(false), []);

  const choose = (id: string) => {
    dropFocusWish.current?.();
    pendingFocus.current = id;
    // The wish ends as soon as the focus goes to somewhere outside of the filter bar.
    const leaves = (event: FocusEvent) => {
      const bar = groupRef.current?.parentElement;
      if (!(event.target instanceof Node) || !bar?.contains(event.target)) {
        dropFocusWish.current?.();
      }
    };
    document.addEventListener('focusin', leaves);
    dropFocusWish.current = () => {
      document.removeEventListener('focusin', leaves);
      pendingFocus.current = null;
      dropFocusWish.current = null;
    };
    setOpen(false);
    onToggle(id);
  };

  const remove = (id: string, event: React.SyntheticEvent<HTMLElement>) => {
    setOpen(false);
    onDelete(id, event);
  };

  return (
    <>
      <span ref={groupRef} className="tl-chip-group">
        {row.map((preset) => {
          const isActive = preset.id === activeId;
          // Only a filter of the user's own can be deleted; a declared one is part of the
          // table and offers no remove affordance.
          return (
            <Chip
              key={preset.id}
              label={preset.label}
              pressed={isActive}
              tooltip={isActive ? clearLabel : preset.label}
              onClick={() => onToggle(preset.id)}
              onRemove={preset.deletable ? (event) => onDelete(preset.id, event) : undefined}
              removeLabel={deleteLabel}
              {...{ [PRESET_ATTR]: preset.id }}
              {...(preset.deletable ? { [chipAttr]: true } : {})}
            />
          );
        })}
        {!menuEmpty && (
          <Chip
            ref={moreRef}
            className={MORE_CLASS}
            label={<>{moreText.replace('{0}', String(menu.length))}<ThemeIcon encoded={ICON_LIST_CLOSED} className="tl-icon-sm" /></>}
            aria-haspopup="menu"
            aria-expanded={open}
            // The press that closes the menu is the press of this chip as well: its click must not open it again.
            onClick={() => {
              if (!pressClosedSurface()) {
                setOpen((wasOpen) => !wasOpen);
              }
            }}
          />
        )}
      </span>
      <Menu open={open && !menuEmpty} anchor={open ? moreRef.current : null} onClose={close}>
        {menu.map((preset) => {
          const entry = (
            <Button
              label={preset.label}
              display="label-only"
              appearance="menu-item"
              aria-keyshortcuts={preset.deletable ? DELETE_KEYS.join(' ') : undefined}
              onClick={() => choose(preset.id)}
            />
          );
          return preset.deletable ? (
            <div
              key={preset.id}
              role="group"
              aria-label={preset.label}
              className={ENTRY_CLASS}
              onKeyDown={(event) => {
                if (DELETE_KEYS.includes(event.key)) {
                  remove(preset.id, event);
                }
              }}
            >
              {entry}
              <Button
                label={deleteLabel}
                image={ICON_REMOVE}
                display="icon-only"
                appearance="ghost"
                small
                // For the pointer: the keyboard deletes by the key on the entry.
                tabIndex={-1}
                onClick={(event) => remove(preset.id, event)}
              />
            </div>
          ) : <React.Fragment key={preset.id}>{entry}</React.Fragment>;
        })}
      </Menu>
    </>
  );
};

export const FilterPresets = React.memo(FilterPresetsRow);
