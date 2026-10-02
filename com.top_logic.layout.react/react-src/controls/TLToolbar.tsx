import { React, useTLState, TLChild, useCloseOnOutsidePress, useFocusTrap, useI18N, rootClassName, tooltipProps, createPortal, ThemeIcon, usePopover, useMergeRefs, anchoredOverlayProps } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { ButtonDefaults, useButtonDefaults, buttonClassName } from './button/ButtonDefaults';
import { useRovingMenu } from './menu/Menu';

const { useCallback, useRef, useState, useEffect, useLayoutEffect, useMemo } = React;

const I18N_KEYS = {
  'js.toolbar.label': 'Toolbar',
  'js.toolbar.overflow': 'More actions',
};

/** Icon of the overflow menu trigger. */
const OVERFLOW_ICON = 'css:bi bi-three-dots';

/** Icon after the label of a labeled menu trigger. */
const CHEVRON_ICON = 'css:fa-solid fa-chevron-down';

/** The groups of a toolbar without any: one array, so that it is the same in every render. */
const NO_GROUPS: CliqueGroup[] = [];

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
 * The pass of the measurement a render belongs to: `full` lays every unit out inline with labels,
 * `compact` lays them out inline as icon buttons. Absent once the widths of the current groups are
 * known.
 */
type MeasurePhase = 'full' | 'compact' | null;

/** The widths read in the `full` pass, with the groups they were read for. */
interface FullWidths {
  groups: CliqueGroup[];
  widths: number[];
}

/**
 * Renders the given items of a clique group inline (side by side).
 */
const InlineGroup: React.FC<{ units: PlacedUnit[] }> = ({ units }) => {
  if (units.length === 0) return null;

  return (
    <div className="tl-toolbar__group">
      {units.map(placed => (
        <span key={placed.index} data-tlunit={placed.index}>
          <TLChild control={placed.unit.item} />
        </span>
      ))}
    </div>
  );
};

/**
 * Renders a clique group as a dropdown menu: a tl-button opening a tl-menu, whose entries are the
 * group's buttons, presented as menu items (ButtonDefaults `menu-item`).
 */
const MenuGroup: React.FC<{ group: CliqueGroup; align?: 'start' | 'end'; unitIndex?: number }> =
    ({ group, align = 'end', unitIndex }) => {
  const [open, setOpen] = useState(false);
  const triggerRef = useRef<HTMLButtonElement>(null);
  const menuRef = useRef<HTMLDivElement | null>(null);
  // The trigger looks like the buttons beside it.
  const inheritedAppearance = useButtonDefaults().appearance ?? 'ghost';

  const handleToggle = useCallback(() => {
    setOpen(prev => !prev);
  }, []);
  const close = useCallback(() => setOpen(false), []);

  // Placed at the trigger edge the group grows away from: a trailing group opens to the left of its
  // right edge, a leading one to the right of its left edge.
  const { setFloating, style } = usePopover({
    open,
    anchor: triggerRef.current,
    placement: align === 'start' ? 'bottom-start' : 'bottom-end',
  });
  const setMenuRefs = useMergeRefs<HTMLDivElement>([menuRef, setFloating]);

  // Close on a press outside the dropdown. A press on the trigger is left to the trigger, which
  // toggles the dropdown on its click.
  useCloseOnOutsidePress(open, [menuRef, triggerRef], close);

  // While open, trap focus in the dropdown (initial focus on the first item) and restore it to
  // the trigger when it closes, so keystrokes can't leak to the background.
  useFocusTrap(open, menuRef, 'first');

  // Arrows, Home, End move between the entries; Escape closes.
  const { onKeyDown } = useRovingMenu(menuRef, open, close);

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
      <div className="tl-toolbar__group" data-tlunit={unitIndex}>
        <span>
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
    <div className="tl-toolbar__group" data-tlunit={unitIndex}>
      <button
        ref={triggerRef}
        type="button"
        className={buttonClassName({ appearance: inheritedAppearance, icon: iconOnly })}
        // Don't steal focus from the content (e.g. a table) on mouse-open: the menu acts on the
        // current selection, so focus should return there after a command's dialog closes. The
        // menu's own focus trap still moves focus into the dropdown while it is open.
        onMouseDown={(e) => e.preventDefault()}
        onClick={handleToggle}
        aria-expanded={open}
        aria-haspopup="menu"
        aria-label={iconOnly ? label : undefined}
        {...tooltipProps(iconOnly ? label : undefined)}
      >
        {iconOnly
          ? <ThemeIcon encoded={group.icon!} className="tl-button__icon tl-icon-md" />
          : <>
              <span className="tl-button__label">{label}</span>
              <ThemeIcon encoded={CHEVRON_ICON} className="tl-button__icon tl-icon-sm" />
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
          ref={setMenuRefs}
          className="tl-popover tl-menu"
          role="menu"
          hidden={!open}
          style={open ? style : undefined}
          onClick={close}
          onKeyDown={onKeyDown}
          {...anchoredOverlayProps}
        >
          <ButtonDefaults appearance="menu-item" iconOnly={false}>
            {visibleItems.map((item, i) => <TLChild key={i} control={item} />)}
            {sections.map((items, si) => (
              <React.Fragment key={`sub-${si}`}>
                {(visibleItems.length > 0 || si > 0) && <hr className="tl-divider" />}
                {items.map((item, i) => <TLChild key={i} control={item} />)}
              </React.Fragment>
            ))}
          </ButtonDefaults>
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
 *
 * The buttons drop their labels themselves: the toolbar tells them through ButtonDefaults
 * (`iconOnly`). The widths of both presentations are therefore read in two passes, one render
 * each: first with labels, then compact.
 */
const TLToolbar: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const groups = (state.groups as CliqueGroup[] | undefined) ?? NO_GROUPS;
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
  const fullRef = useRef<FullWidths | null>(null);
  const [measurePhase, setMeasurePhase] = useState<MeasurePhase>(null);
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

  // The widths of the current groups are not known yet: this render lays everything out inline,
  // with labels (`full`) or compact, which is what the measuring effect below reads. The first
  // render after a change of the groups is already the `full` pass.
  const phase: MeasurePhase = collapsible && measuredRef.current !== groups
    ? (measurePhase ?? 'full')
    : null;
  const measuring = phase !== null;
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
    if (!root || !collapsible) {
      // Nothing to measure: a pass begun before must not leave the buttons compact.
      if (measurePhase !== null) setMeasurePhase(null);
      return;
    }

    if (phase === 'full') {
      fullRef.current = { groups, widths: readUnits(root, units.length) };
      setMeasurePhase('compact');
      return;
    }
    if (phase === 'compact') {
      const full = fullRef.current;
      if (!full || full.groups !== groups) {
        // The groups changed between the passes: the widths read do not belong together.
        setMeasurePhase('full');
        return;
      }
      metricsRef.current = metricsOf(root, full.widths, readUnits(root, units.length));
      measuredRef.current = groups;
      fullRef.current = null;
      setMeasurePhase(null);
    }
    const metrics = metricsRef.current;
    if (!metrics) return;

    const natural = widthOf(metrics, metrics.full, 0, units.length, false);
    const stated = Math.ceil(natural);
    if (phase === 'compact') {
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
    <ButtonDefaults appearance={appearance} iconOnly={collapsible && (phase === 'compact' || compact)}>
      <div
        id={controlId}
        ref={rootRef}
        className={rootClassName(state, 'tl-toolbar')}
        role="toolbar"
        aria-label={i18n['js.toolbar.label']}
        data-tl-overflow={collapsible ? overflowEnd : undefined}
        data-tl-compact={compact ? '' : undefined}
        style={toolbarStyle}
      >
        {overflowEnd === 'leading' && trigger}
        {shownGroups.map((shown, i) => (
          <React.Fragment key={shown.group.name}>
            {i > 0 && <hr className="tl-divider tl-divider--vertical" aria-orientation="vertical" />}
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
 * Reads the width of every unit in the presentation the toolbar currently renders, while all
 * units are laid out inline. A unit whose button is hidden reads zero.
 */
function readUnits(root: HTMLElement, unitCount: number): number[] {
  const widths = new Array<number>(unitCount).fill(0);
  root.querySelectorAll<HTMLElement>('[data-tlunit]').forEach(element => {
    const index = Number(element.dataset.tlunit);
    if (index >= 0 && index < unitCount) {
      widths[index] = element.getBoundingClientRect().width;
    }
  });
  return widths;
}

/**
 * The metrics of the toolbar: the unit widths of both passes, with the gaps and the separator
 * width read from the stylesheet and the width of the overflow trigger.
 */
function metricsOf(root: HTMLElement, full: number[], compact: number[]): Metrics {
  const rootStyle = getComputedStyle(root);
  const gap = parseFloat(rootStyle.columnGap) || 0;
  const groupElement = root.querySelector('.tl-toolbar__group');
  const itemGap = groupElement ? (parseFloat(getComputedStyle(groupElement).columnGap) || gap) : gap;
  const separatorElement = root.querySelector('.tl-divider--vertical');
  const separator = separatorElement ? separatorElement.getBoundingClientRect().width : 1;
  // The trigger is rendered only once something overflows; it is an icon button, whose width is the
  // size-control token - read, not guessed.
  return { full, compact, gap, itemGap, separator, trigger: tokenPx(rootStyle, '--tl-size-control') };
}

/** A length token in pixels; a token stated in rem is converted by the root font size. */
function tokenPx(style: CSSStyleDeclaration, name: string): number {
  const rem = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16;
  const v = style.getPropertyValue(name).trim();
  const n = parseFloat(v);
  return isNaN(n) ? 2 * rem : v.endsWith('rem') ? n * rem : n;
}

export default TLToolbar;
