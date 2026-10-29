import { React, useTLState, useTLCommand, useI18N, rootClassName, ThemeIcon } from 'tl-react-bridge';
import type { TLCellProps, BreadcrumbStateJson } from 'tl-react-bridge';
import { buttonClassName } from './button/buttonClassName';

const { useCallback } = React;

const I18N_KEYS = {
  'js.breadcrumb.label': 'Breadcrumb',
};

/** The glyph between two entries; ThemeIcon renders a font glyph aria-hidden, so it is decoration only. */
const SEPARATOR_ICON = 'css:fa-solid fa-chevron-right';

/** The command jumping back to an item, see BreadcrumbStateJson. */
const NAVIGATE_COMMAND = 'navigate';

/** The argument of {@link NAVIGATE_COMMAND} naming the item. */
const ITEM_ID_ARG = 'itemId';

/** One entry of a trail: what it is called, and the id a jump back names it by. */
export interface BreadcrumbEntry {
  id: string;
  label: string;
}

/**
 * The trail itself (tl-breadcrumb): every entry before the current page is a link button that jumps back to it; the
 * current page is plain text marked `aria-current="page"` and no jump target.
 *
 * Rendered by TLBreadcrumb and by the bar of TLAdaptiveDetail, which builds its trail from the levels it shows.
 */
export const Breadcrumb: React.FC<{
  id?: string;
  className: string;
  label: string;
  items: BreadcrumbEntry[];
  onNavigate: (id: string) => void;
}> = ({ id, className, label, items, onNavigate }) => (
  <nav id={id} className={className} aria-label={label}>
    <ol className="tl-breadcrumb__list">
      {items.map((item, index) => {
        const isLast = index === items.length - 1;
        return (
          <li key={item.id} className="tl-breadcrumb__entry">
            {index > 0 && (
              <ThemeIcon encoded={SEPARATOR_ICON} className="tl-breadcrumb__separator tl-icon-sm" />
            )}
            {isLast ? (
              <span className="tl-breadcrumb__current tl-type-body" aria-current="page">{item.label}</span>
            ) : (
              <button
                type="button"
                className={buttonClassName({ appearance: 'link' })}
                onClick={() => onNavigate(item.id)}
              >
                <span className="tl-button__label">{item.label}</span>
              </button>
            )}
          </li>
        );
      })}
    </ol>
  </nav>
);

/**
 * A navigation trail showing the current location in a hierarchy (tl-breadcrumb).
 *
 * The state is described by BreadcrumbStateJson.
 */
const TLBreadcrumb: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<BreadcrumbStateJson>>();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

  const handleNavigate = useCallback((itemId: string) => {
    sendCommand(NAVIGATE_COMMAND, { [ITEM_ID_ARG]: itemId });
  }, [sendCommand]);

  return (
    <Breadcrumb
      id={controlId}
      className={rootClassName(state, 'tl-breadcrumb')}
      label={i18n['js.breadcrumb.label']}
      items={state.items ?? []}
      onNavigate={handleNavigate}
    />
  );
};

export default TLBreadcrumb;
