import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

interface BreadcrumbItem {
  id: string;
  label: string;
}

/**
 * A navigation trail showing the current location in a hierarchy.
 *
 * State:
 * - items: { id, label }[]  (last item = current page)
 */
const TLBreadcrumb: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const items = (state.items as BreadcrumbItem[]) ?? [];

  const handleNavigate = useCallback((itemId: string) => {
    sendCommand('navigate', { itemId });
  }, [sendCommand]);

  return (
    <nav id={controlId} className="tlBreadcrumb" aria-label="Breadcrumb">
      <ol className="tlBreadcrumb__list">
        {items.map((item, index) => {
          const isLast = index === items.length - 1;
          return (
            <li key={item.id} className="tlBreadcrumb__entry">
              {index > 0 && (
                <svg className="tlBreadcrumb__separator" viewBox="0 0 16 16"
                  width="16" height="16" aria-hidden="true">
                  <path d="M6 4l4 4-4 4" fill="none" stroke="currentColor"
                    strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
              )}
              {isLast ? (
                <span className="tlBreadcrumb__current" aria-current="page">{item.label}</span>
              ) : (
                <button
                  type="button"
                  className="tlBreadcrumb__item"
                  onClick={() => handleNavigate(item.id)}
                >
                  {item.label}
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
