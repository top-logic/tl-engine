import { React, useTLState, TLChild, useStandaloneKeyboardScope } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { createPortal } from 'react-dom';
import { ThemeIcon } from './icon/ThemeIcon';

const { useCallback, useRef, useState, useEffect, useLayoutEffect } = React;

interface CliqueGroup {
  name: string;
  display: 'inline' | 'menu';
  label?: string;
  icon?: string;
  items: unknown[];
  subGroups?: CliqueGroup[];
}

/**
 * Renders a clique group's items inline (side by side).
 */
const InlineGroup: React.FC<{ group: CliqueGroup }> = ({ group }) => {
  const visibleItems = group.items.filter(item => item != null);
  if (visibleItems.length === 0) return null;

  return (
    <div className="tlToolbar__group tlToolbar__group--inline">
      {visibleItems.map((item, i) => (
        <span key={i} className="tlToolbar__item">
          <TLChild control={item} />
        </span>
      ))}
    </div>
  );
};

/**
 * Renders a clique group as a dropdown menu.
 */
const MenuGroup: React.FC<{ group: CliqueGroup }> = ({ group }) => {
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
      // Right-align the menu to the trigger's right edge (anchoring via `right` avoids
      // measuring the menu width and keeps it inside the viewport on the right).
      setMenuStyle({
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
  }, [open]);

  // Close on outside click.
  useEffect(() => {
    if (!open) return;
    const handleMouseDown = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node) &&
          triggerRef.current && !triggerRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', handleMouseDown);
    return () => document.removeEventListener('mousedown', handleMouseDown);
  }, [open]);

  // Close on Escape (via the shared keyboard dispatcher).
  useStandaloneKeyboardScope(open, { ESCAPE: () => setOpen(false) });

  const visibleItems = group.items.filter(item => item != null);
  if (visibleItems.length === 0) return null;

  // Single item: render directly without dropdown. An icon-triggered menu (e.g. the
  // burger overflow) always stays a menu, so its trigger icon remains stable regardless
  // of how many items are currently enabled.
  if (visibleItems.length === 1 && !group.subGroups?.length && !group.icon) {
    return (
      <div className="tlToolbar__group tlToolbar__group--inline">
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
    <div className="tlToolbar__group tlToolbar__group--menu">
      <button
        ref={triggerRef}
        type="button"
        className={'tlToolbar__menuTrigger' + (iconOnly ? ' tlToolbar__menuTrigger--icon' : '')}
        onClick={handleToggle}
        aria-expanded={open}
        aria-haspopup="true"
        aria-label={iconOnly ? label : undefined}
        title={iconOnly ? label : undefined}
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
          {group.subGroups?.map((sub, si) => (
            <React.Fragment key={`sub-${si}`}>
              <hr className="tlToolbar__dropdownSeparator" />
              {sub.items.map((item, i) => (
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
 */
const TLToolbar: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const groups = (state.groups as CliqueGroup[]) ?? [];

  // Filter out empty groups.
  const visibleGroups = groups.filter(g => g.items.some(item => item != null));
  if (visibleGroups.length === 0) return null;

  return (
    <div id={controlId} className="tlToolbar" role="toolbar">
      {visibleGroups.map((group, i) => (
        <React.Fragment key={group.name}>
          {i > 0 && <span className="tlToolbar__separator" aria-hidden="true" />}
          {group.display === 'menu'
            ? <MenuGroup group={group} />
            : <InlineGroup group={group} />
          }
        </React.Fragment>
      ))}
    </div>
  );
};

export default TLToolbar;
