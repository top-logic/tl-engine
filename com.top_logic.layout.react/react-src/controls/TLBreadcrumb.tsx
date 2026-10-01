import { React, useTLState, useTLCommand, useI18N, rootClassName, ThemeIcon } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { buttonClassName } from './button/ButtonDefaults';

const { useCallback } = React;

const I18N_KEYS = {
  'js.breadcrumb.label': 'Breadcrumb',
};

/** The glyph between two entries; decoration only, hidden from assistive technology. */
const SEPARATOR_ICON = 'css:fa-solid fa-chevron-right';

interface BreadcrumbItem {
  id: string;
  label: string;
}

/**
 * A navigation trail showing the current location in a hierarchy (tl-breadcrumb).
 *
 * Every entry before the current page is a link button that jumps back to it; the current page is
 * plain text marked `aria-current="page"` and no jump target.
 *
 * State:
 * - items: { id, label }[]  (last item = current page)
 */
const TLBreadcrumb: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

  const items = (state.items as BreadcrumbItem[]) ?? [];

  const handleNavigate = useCallback((itemId: string) => {
    sendCommand('navigate', { itemId });
  }, [sendCommand]);

  return (
    <nav id={controlId} className={rootClassName(state, 'tl-breadcrumb')} aria-label={i18n['js.breadcrumb.label']}>
      <ol className="tl-breadcrumb__list">
        {items.map((item, index) => {
          const isLast = index === items.length - 1;
          return (
            <li key={item.id} className="tl-breadcrumb__entry">
              {index > 0 && (
                <span className="tl-breadcrumb__separator" aria-hidden="true">
                  <ThemeIcon encoded={SEPARATOR_ICON} className="tl-icon-sm" />
                </span>
              )}
              {isLast ? (
                <span className="tl-breadcrumb__current tl-type-body" aria-current="page">{item.label}</span>
              ) : (
                <button
                  type="button"
                  className={buttonClassName({ appearance: 'link' })}
                  onClick={() => handleNavigate(item.id)}
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
};

export default TLBreadcrumb;
