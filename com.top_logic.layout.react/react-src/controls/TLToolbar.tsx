import { React, useTLState, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback, useRef, useState, useEffect } = React;

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
  const triggerRef = useRef<HTMLButtonElement>(null);
  const menuRef = useRef<HTMLDivElement>(null);

  const handleToggle = useCallback(() => {
    setOpen(prev => !prev);
  }, []);

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

  // Close on Escape.
  useEffect(() => {
    if (!open) return;
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setOpen(false);
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [open]);

  const visibleItems = group.items.filter(item => item != null);
  if (visibleItems.length === 0) return null;

  // Single item: render directly without dropdown.
  if (visibleItems.length === 1 && !group.subGroups?.length) {
    return (
      <div className="tlToolbar__group tlToolbar__group--inline">
        <span className="tlToolbar__item">
          <TLChild control={visibleItems[0]} />
        </span>
      </div>
    );
  }

  return (
    <div className="tlToolbar__group tlToolbar__group--menu">
      <button
        ref={triggerRef}
        type="button"
        className="tlToolbar__menuTrigger"
        onClick={handleToggle}
        aria-expanded={open}
        aria-haspopup="true"
      >
        {group.icon && <i className={group.icon} aria-hidden="true" />}
        <span>{group.label ?? group.name}</span>
        <svg className="tlToolbar__chevron" viewBox="0 0 24 24" aria-hidden="true">
          <polyline points="6,9 12,15 18,9" />
        </svg>
      </button>
      {open && (
        <div ref={menuRef} className="tlToolbar__dropdown" role="menu">
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
        </div>
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
