import { React, useTLState, TLChild, useCloseOnOutsidePress, useStandaloneKeyboardScope, useFocusTrap, useI18N, rootClassName, tooltipProps, createPortal } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { ThemeIcon } from './icon/ThemeIcon';
import { ButtonDefaults, useButtonDefaults } from './button/ButtonDefaults';

const { useCallback, useRef, useState, useEffect, useLayoutEffect, useMemo } = React;

const I18N_KEYS = {
  'js.toolbar.overflow': 'More actions',
};

/** Icon of the overflow menu trigger. */
const OVERFLOW_ICON = 'css:bi bi-three-dots';

/** Modifier class presenting every icon-carrying button by its icon alone. */
const COMPACT_CLASS = 'tlToolbar--compact';

/** Custom property stating the width of an icon-only toolbar trigger. */
const TRIGGER_SIZE_PROPERTY = '--tl-toolbar-trigger-size';

/** Tolerance (px) against sub-pixel rounding when comparing measured widths. */
const EPSILON = 0.5;

/** The end at which the toolbar collapses; mirrors the server-side `ToolbarOverflow`. */
type OverflowEnd = 'none' | 'trailing' | 'leading';

interface CliqueGroup {
  name: string;
  display: 'inline' | 'menu';
  label?: string;
  icon?: string;
  items: unknown[];
  subGroups?: CliqueGroup[];
}

/**
 * The smallest part of a toolbar that can be moved into the overflow menu on its own: a single
 * item of an inline group, or a whole menu group (which keeps its items together as one section).
 */
interface ToolbarUnit {
  /** Position of the owning group in the visible groups. */
  groupIndex: number;

  /** The item of an inline group; absent when the unit is a whole menu group. */
  item?: unknown;
}

/** A unit together with its position in the unit sequence, which is also its measurement key. */
interface PlacedUnit {
  index: number;
  unit: ToolbarUnit;
}

/** Widths measured while every unit is laid out inline, in both presentations. */
interface Metrics {
  /** Per-unit width with labels shown. */
  full: number[];

  /** Per-unit width in the compact (icon-only) presentation. */
  compact: number[];

  /** Gap between the flex children of the toolbar. */
  gap: number;

  /** Gap between the items of an inline group. */
  itemGap: number;

  /** Width of a group separator. */
  separator: number;

  /** Width of the overflow trigger. */
  trigger: number;
}

/** The presentation the toolbar currently renders in. */
interface ToolbarLayout {
  /** Whether buttons show their icon alone. */
  compact: boolean;

  /** How many units sit in the overflow menu. */
  overflowCount: number;

  /** The toolbar's full natural width, which it states as its own width. */
  width: number | null;
}

const INITIAL_LAYOUT: ToolbarLayout = { compact: false, overflowCount: 0, width: null };

/**
 * Renders the given items of a clique group inline (side by side).
 */
const InlineGroup: React.FC<{ units: PlacedUnit[] }> = ({ units }) => {
  if (units.length === 0) return null;

  return (
    <div className="tlToolbar__group tlToolbar__group--inline">
      {units.map(placed => (
        <span key={placed.index} className="tlToolbar__item" data-tlunit={placed.index}>
          <TLChild control={placed.unit.item} />
        </span>
      ))}
    </div>
  );
};

/**
 * Renders a clique group as a dropdown menu.
 */
const MenuGroup: React.FC<{ group: CliqueGroup; align?: 'start' | 'end'; unitIndex?: number }> =
    ({ group, align = 'end', unitIndex }) => {
  const [open, setOpen] = useState(false);
  const [menuStyle, setMenuStyle] = useState<React.CSSProperties>({});
  const triggerRef = useRef<HTMLButtonElement>(null);
  const menuRef = useRef<HTMLDivElement>(null);

  const handleToggle = useCallback(() => {
    setOpen(prev => !prev);
  }, []);

  // Position the dropdown relative to the trigger via fixed coordinates. The dropdown is
  // rendered through a portal into document.body so it escapes any clipping ancestor (e.g. a
  // scrollable split-panel child); fixed positioning then keeps it anchored to the trigger.
  useLayoutEffect(() => {
    if (!open) return;
    const update = () => {
      const t = triggerRef.current;
      if (!t) return;
      const r = t.getBoundingClientRect();
      // Anchor the menu to the trigger edge the group grows away from: a trailing group opens
      // to the left of its right edge, a leading one to the right of its left edge. Anchoring
      // via a single edge avoids measuring the menu width and keeps it inside the viewport.
      setMenuStyle(align === 'start'
        ? {
          position: 'fixed',
          top: r.bottom + 4,
          left: Math.max(8, r.left),
          right: 'auto',
        }
        : {
          position: 'fixed',
          top: r.bottom + 4,
          right: Math.max(8, window.innerWidth - r.right),
          left: 'auto',
        });
    };
    update();
    window.addEventListener('resize', update);
    window.addEventListener('scroll', update, true);
    return () => {
      window.removeEventListener('resize', update);
      window.removeEventListener('scroll', update, true);
    };
  }, [open, align]);

  // Close on a press outside the dropdown. A press on the trigger is left to the trigger, which
  // toggles the dropdown on its click.
  useCloseOnOutsidePress(open, [menuRef, triggerRef], () => setOpen(false));

  // Close on Escape (via the shared keyboard dispatcher).
  useStandaloneKeyboardScope(open, { ESCAPE: () => setOpen(false) });

  // While open, trap focus in the dropdown (initial focus on the first item) and restore it to
  // the trigger when it closes, so keystrokes can't leak to the background.
  useFocusTrap(open, menuRef, 'first');

  const visibleItems = group.items.filter(item => item != null);
  const sections = (group.subGroups ?? [])
    .map(sub => sub.items.filter(item => item != null))
    .filter(items => items.length > 0);
  if (visibleItems.length === 0 && sections.length === 0) return null;

  // Single item: render directly without dropdown. An icon-triggered menu (e.g. the
  // burger overflow) always stays a menu, so its trigger icon remains stable regardless
  // of how many items are currently enabled.
  if (visibleItems.length === 1 && sections.length === 0 && !group.icon) {
    return (
      <div className="tlToolbar__group tlToolbar__group--inline" data-tlunit={unitIndex}>
        <span className="tlToolbar__item">
          <TLChild control={visibleItems[0]} />
        </span>
      </div>
    );
  }

  // An icon (e.g. a burger "☰") renders as a compact icon-only trigger; the label is kept
  // as the accessible name instead of visible text (which would not be internationalized).
  const label = group.label ?? group.name;
  const iconOnly = !!group.icon;

  return (
    <div className="tlToolbar__group tlToolbar__group--menu" data-tlunit={unitIndex}>
      <button
        ref={triggerRef}
        type="button"
        className={'tlToolbar__menuTrigger' + (iconOnly ? ' tlToolbar__menuTrigger--icon' : '')}
        // Don't steal focus from the content (e.g. a table) on mouse-open: the menu acts on the
        // current selection, so focus should return there after a command's dialog closes. The
        // menu's own focus trap still moves focus into the dropdown while it is open.
        onMouseDown={(e) => e.preventDefault()}
        onClick={handleToggle}
        aria-expanded={open}
        aria-haspopup="true"
        aria-label={iconOnly ? label : undefined}
        {...tooltipProps(iconOnly ? label : undefined)}
      >
        {iconOnly
          ? <ThemeIcon encoded={group.icon!} className="tlToolbar__menuIcon" />
          : <>
              <span>{label}</span>
              <svg className="tlToolbar__chevron" viewBox="0 0 24 24" aria-hidden="true">
                <polyline points="6,9 12,15 18,9" />
              </svg>
            </>
        }
      </button>
      {/* The dropdown stays mounted (only hidden when closed) so its item controls keep
          their live SSE subscription. If items were mounted lazily on open, they would read
          the toolbar's build-time snapshot and miss any executability change (e.g. a row
          getting selected) that happened while the menu was closed - showing stale
          (disabled) entries. It is portaled to document.body so a clipping ancestor cannot
          cut it off; React context (and thus the child controls) propagates through the
          portal. */}
      {createPortal(
        <div
          ref={menuRef}
          className="tlToolbar__dropdown"
          role="menu"
          hidden={!open}
          style={open ? menuStyle : undefined}
          onClick={() => setOpen(false)}
        >
          {visibleItems.map((item, i) => (
            <div key={i} className="tlToolbar__dropdownItem" role="menuitem">
              <TLChild control={item} />
            </div>
          ))}
          {sections.map((items, si) => (
            <React.Fragment key={`sub-${si}`}>
              {(visibleItems.length > 0 || si > 0) && <hr className="tlToolbar__dropdownSeparator" />}
              {items.map((item, i) => (
                <div key={i} className="tlToolbar__dropdownItem" role="menuitem">
                  <TLChild control={item} />
                </div>
              ))}
            </React.Fragment>
          ))}
        </div>,
        document.body
      )}
    </div>
  );
};

/**
 * A toolbar that renders clique groups with separators and dropdown menus.
 *
 * State:
 * - groups: CliqueGroup[]
 * - overflow: 'none' | 'trailing' | 'leading' (the end at which commands that do not fit collapse)
 *
 * A collapsing toolbar states its full natural width as its own width, so what its host offers
 * it - the host sizes itself from that stated width - does not depend on what it currently shows.
 * Granted less, it first drops the labels of the buttons that carry an icon, then moves the units
 * that still do not fit - single items of an inline group, whole menu groups - into an overflow
 * menu at the collapsing end.
 */
const TLToolbar: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const groups = (state.groups as CliqueGroup[]) ?? [];
  const overflowEnd = (state.overflow as OverflowEnd) ?? 'none';
  const collapsible = overflowEnd !== 'none';
  const i18n = useI18N(I18N_KEYS);

  // The buttons of a toolbar are ghost unless the container the toolbar sits in has chosen
  // otherwise: a window footer or a panel button bar says secondary, an app bar says ghost.
  const inherited = useButtonDefaults();
  const appearance = inherited.appearance ?? 'ghost';

  const rootRef = useRef<HTMLDivElement>(null);
  const metricsRef = useRef<Metrics | null>(null);
  const measuredRef = useRef<CliqueGroup[] | null>(null);
  const [layout, setLayout] = useState<ToolbarLayout>(INITIAL_LAYOUT);
  const [, setResizeTick] = useState(0);

  // Groups with at least one item, and the unit sequence they decompose into.
  const { visibleGroups, units } = useMemo(() => {
    const shown = groups.filter(g => g.items.some(item => item != null));
    const sequence: ToolbarUnit[] = [];
    shown.forEach((group, groupIndex) => {
      if (group.display === 'menu') {
        sequence.push({ groupIndex });
      } else {
        group.items.filter(item => item != null)
          .forEach(item => sequence.push({ groupIndex, item }));
      }
    });
    return { visibleGroups: shown, units: sequence };
  }, [groups]);

  // The widths of the current groups are not known yet: this render lays everything out inline
  // and uncompacted, which is what the measuring pass below reads.
  const measuring = collapsible && measuredRef.current !== groups;
  const settled = collapsible && !measuring && metricsRef.current != null;
  const compact = settled && layout.compact;
  const overflowCount = settled ? layout.overflowCount : 0;

  /** The width the toolbar occupies when it shows the units of the given range. */
  const widthOf = useCallback((metrics: Metrics, widths: number[], from: number, to: number,
      withTrigger: boolean) => {
    let content = 0;
    let groupCount = 0;
    let previousGroup = -1;
    let shownInGroup = 0;
    for (let i = from; i < to; i++) {
      const unit = units[i];
      if (unit.groupIndex !== previousGroup) {
        groupCount++;
        previousGroup = unit.groupIndex;
        shownInGroup = 0;
      }
      const width = widths[i];
      if (width <= 0) {
        // The button of this unit is hidden, so the item is dropped from the layout altogether:
        // it takes neither width nor a gap.
        continue;
      }
      if (shownInGroup > 0) {
        content += metrics.itemGap;
      }
      content += width;
      shownInGroup++;
    }
    const separators = Math.max(0, groupCount - 1);
    // Flex children of the toolbar: the groups, the separators between them, the trigger.
    const children = groupCount + separators + (withTrigger ? 1 : 0);
    return content + separators * metrics.separator + Math.max(0, children - 1) * metrics.gap
      + (withTrigger ? metrics.trigger : 0);
  }, [units]);

  useLayoutEffect(() => {
    const root = rootRef.current;
    if (!root || !collapsible) return;

    if (measuring) {
      metricsRef.current = measureUnits(root, units.length);
      measuredRef.current = groups;
    }
    const metrics = metricsRef.current;
    if (!metrics) return;

    const natural = widthOf(metrics, metrics.full, 0, units.length, false);
    const stated = Math.ceil(natural);
    if (measuring) {
      // The measuring render leaves the toolbar unconstrained, so that the widths just read are
      // the natural ones. State the width the settled render gives it before asking how much is
      // granted - a stated width is what the host sizes itself from, a flex base size is not.
      root.style.width = stated + 'px';
      root.style.flexShrink = '';
    }
    const available = root.getBoundingClientRect().width;
    // A toolbar of a hidden host measures zero - keep the presentation it has.
    if (available <= 0) return;

    let nextCompact = false;
    let nextOverflow = 0;
    if (available + EPSILON < natural) {
      nextCompact = true;
      if (available + EPSILON < widthOf(metrics, metrics.compact, 0, units.length, false)) {
        // Collapse units from the collapsing end until the rest fits beside the trigger.
        nextOverflow = units.length;
        for (let collapsed = 1; collapsed <= units.length; collapsed++) {
          const from = overflowEnd === 'leading' ? collapsed : 0;
          const to = overflowEnd === 'leading' ? units.length : units.length - collapsed;
          if (widthOf(metrics, metrics.compact, from, to, true) <= available + EPSILON) {
            nextOverflow = collapsed;
            break;
          }
        }
      }
    }
    if (layout.compact !== nextCompact || layout.overflowCount !== nextOverflow
        || layout.width !== stated) {
      setLayout({ compact: nextCompact, overflowCount: nextOverflow, width: stated });
    }
  });

  // The available width is what the host grants, so the presentation follows the host's size.
  useEffect(() => {
    const root = rootRef.current;
    if (!root || !collapsible || typeof ResizeObserver === 'undefined') return;
    const observer = new ResizeObserver(() => setResizeTick(tick => tick + 1));
    observer.observe(root);
    return () => observer.disconnect();
  }, [collapsible]);

  if (visibleGroups.length === 0) return null;

  const firstShown = overflowEnd === 'leading' ? overflowCount : 0;
  const afterShown = overflowEnd === 'leading' ? units.length : units.length - overflowCount;

  // The units still laid out inline, cut into the groups they belong to.
  const shownGroups: { group: CliqueGroup; units: PlacedUnit[] }[] = [];
  for (let i = firstShown; i < afterShown; i++) {
    const unit = units[i];
    const last = shownGroups[shownGroups.length - 1];
    if (last && last.group === visibleGroups[unit.groupIndex]) {
      last.units.push({ index: i, unit });
    } else {
      shownGroups.push({ group: visibleGroups[unit.groupIndex], units: [{ index: i, unit }] });
    }
  }

  // The collapsed units, each group contributing one section of the overflow menu.
  const overflowSections: CliqueGroup[] = [];
  const collapsedFrom = overflowEnd === 'leading' ? 0 : units.length - overflowCount;
  const collapsedTo = overflowEnd === 'leading' ? overflowCount : units.length;
  for (let i = collapsedFrom; i < collapsedTo; i++) {
    const unit = units[i];
    const group = visibleGroups[unit.groupIndex];
    const items = unit.item !== undefined ? [unit.item] : group.items.filter(item => item != null);
    const last = overflowSections[overflowSections.length - 1];
    if (last && last.name === group.name) {
      last.items.push(...items);
    } else {
      overflowSections.push({ name: group.name, display: 'inline', items });
    }
  }

  const overflowGroup: CliqueGroup = {
    name: 'overflow',
    display: 'menu',
    label: i18n['js.toolbar.overflow'],
    icon: OVERFLOW_ICON,
    items: [],
    subGroups: overflowSections,
  };
  const trigger = overflowCount > 0
    ? <MenuGroup group={overflowGroup} align={overflowEnd === 'leading' ? 'start' : 'end'} />
    : null;

  const className = rootClassName(state, 'tlToolbar',
    collapsible && 'tlToolbar--collapsible',
    compact && COMPACT_CLASS);

  // While measuring, the toolbar takes the width it needs, so that the widths read from it are
  // the natural ones of its units. Afterwards it states that natural width as its own width and
  // gives it up again down to its overflow trigger.
  let toolbarStyle: React.CSSProperties | undefined;
  if (measuring) {
    toolbarStyle = { width: 'auto', flexShrink: 0 };
  } else if (settled && layout.width != null) {
    toolbarStyle = { width: layout.width };
  }

  return (
    <ButtonDefaults appearance={appearance}>
      <div
        id={controlId}
        ref={rootRef}
        className={className}
        role="toolbar"
        style={toolbarStyle}
      >
        {overflowEnd === 'leading' && trigger}
        {shownGroups.map((shown, i) => (
          <React.Fragment key={shown.group.name}>
            {i > 0 && <span className="tlToolbar__separator" aria-hidden="true" />}
            {shown.group.display === 'menu'
              ? <MenuGroup group={shown.group} unitIndex={shown.units[0].index} />
              : <InlineGroup units={shown.units} />
            }
          </React.Fragment>
        ))}
        {overflowEnd === 'trailing' && trigger}
      </div>
    </ButtonDefaults>
  );
};

/**
 * Reads the width of every unit, in the full and in the compact presentation.
 *
 * <p>Called while all units are laid out inline; the compact presentation is measured by putting
 * the toolbar into it for the duration of the measurement.</p>
 */
function measureUnits(root: HTMLElement, unitCount: number): Metrics {
  const readUnits = () => {
    const widths = new Array<number>(unitCount).fill(0);
    root.querySelectorAll<HTMLElement>('[data-tlunit]').forEach(element => {
      const index = Number(element.dataset.tlunit);
      if (index >= 0 && index < unitCount) {
        widths[index] = element.getBoundingClientRect().width;
      }
    });
    return widths;
  };

  const full = readUnits();
  root.classList.add(COMPACT_CLASS);
  const compact = readUnits();
  root.classList.remove(COMPACT_CLASS);

  const rootStyle = getComputedStyle(root);
  const gap = parseFloat(rootStyle.columnGap) || 0;
  const groupElement = root.querySelector('.tlToolbar__group--inline');
  const itemGap = groupElement
    ? (parseFloat(getComputedStyle(groupElement).columnGap) || gap)
    : gap;
  const separatorElement = root.querySelector('.tlToolbar__separator');
  const separator = separatorElement ? separatorElement.getBoundingClientRect().width : 1;

  return { full, compact, gap, itemGap, separator, trigger: triggerSize(rootStyle) };
}

/**
 * The width of the overflow trigger, which is absent as long as nothing overflows: the square
 * icon-button size the stylesheet states, in pixels.
 */
function triggerSize(rootStyle: CSSStyleDeclaration): number {
  const rem = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16;
  const declared = rootStyle.getPropertyValue(TRIGGER_SIZE_PROPERTY).trim();
  const size = parseFloat(declared);
  if (isNaN(size)) {
    return 2 * rem;
  }
  return declared.endsWith('rem') ? size * rem : size;
}

export default TLToolbar;
