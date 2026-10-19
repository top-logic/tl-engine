import { React, useI18N, useCloseOnOutsidePress, useStandaloneKeyboardScope, usePart, createPortal, anchoredOverlayProps, tooltipProps, ICON_SEARCH, ICON_CLEAR } from 'tl-react-bridge';

const { useState, useCallback, useEffect, useRef, useLayoutEffect, useMemo } = React;

const I18N_KEYS = {
  'js.iconSelect.simpleTab': 'Simple',
  'js.iconSelect.advancedTab': 'Advanced',
  'js.iconSelect.filterPlaceholder': 'Filter icons\u2026',
  'js.iconSelect.noResults': 'No icons found',
  'js.iconSelect.loading': 'Loading\u2026',
  'js.iconSelect.loadError': 'Failed to load. Click to retry.',
  'js.iconSelect.classLabel': 'Class',
  'js.iconSelect.previewLabel': 'Preview',
  'js.iconSelect.cancel': 'Cancel',
  'js.iconSelect.ok': 'OK',
  'js.iconSelect.clear': 'Clear icon',
  'js.iconSelect.clearFilter': 'Clear filter',
};

/** One style variant of an icon. */
interface IconVariant {
  encoded: string;
}

/** An icon entry from the server. */
export interface IconEntry {
  prefix: string;
  label: string;
  terms?: string[];
  variants: IconVariant[];
}

type Tab = 'simple' | 'advanced';

interface IconSelectPopupProps {
  anchorRef: React.RefObject<HTMLButtonElement | null>;
  currentValue: string | null;
  icons: IconEntry[];
  iconsLoaded: boolean;
  onSelect: (encoded: string | null) => void;
  onCancel: () => void;
  onLoadIcons: () => Promise<void>;
}

// Re-export ThemeIcon as IconPreview for backwards compatibility.
import { ThemeIcon as IconPreview } from 'tl-react-bridge';
export { IconPreview };

const IconSelectPopup: React.FC<IconSelectPopupProps> = ({
  anchorRef,
  currentValue,
  icons,
  iconsLoaded,
  onSelect,
  onCancel,
  onLoadIcons,
}) => {
  const i18n = useI18N(I18N_KEYS);
  const TextInput = usePart('TextInput');
  const Button = usePart('Button');
  const Spinner = usePart('Spinner');
  const [tab, setTab] = useState<Tab>('simple');
  const [searchTerm, setSearchTerm] = useState('');
  const [advancedInput, setAdvancedInput] = useState(currentValue ?? '');
  const [loadError, setLoadError] = useState(false);
  const [position, setPosition] = useState<{ top: number; left: number } | null>(null);
  const popupRef = useRef<HTMLDivElement>(null);
  const searchRef = useRef<HTMLInputElement>(null);

  // Position popup relative to anchor
  useLayoutEffect(() => {
    if (!anchorRef.current || !popupRef.current) return;
    const anchorRect = anchorRef.current.getBoundingClientRect();
    const popupRect = popupRef.current.getBoundingClientRect();
    let top = anchorRect.bottom + 4;
    let left = anchorRect.left;
    if (top + popupRect.height > window.innerHeight) {
      top = anchorRect.top - popupRect.height - 4;
    }
    if (left + popupRect.width > window.innerWidth) {
      left = Math.max(0, anchorRect.right - popupRect.width);
    }
    setPosition({ top, left });
  }, [anchorRef]);

  // Load icons if not loaded
  useEffect(() => {
    if (!iconsLoaded && !loadError) {
      onLoadIcons().catch(() => setLoadError(true));
    }
  }, [iconsLoaded, loadError, onLoadIcons]);

  // Focus search on open
  useEffect(() => {
    if (iconsLoaded && searchRef.current) {
      searchRef.current.focus();
    }
  }, [iconsLoaded]);

  // Close on Escape (via the shared keyboard dispatcher).
  useStandaloneKeyboardScope(true, { ESCAPE: onCancel });

  // Close on a press outside the popup.
  useCloseOnOutsidePress(true, [popupRef], onCancel);

  // Filter icons by search term
  const filteredIcons = useMemo(() => {
    if (!searchTerm) return icons;
    const lower = searchTerm.toLowerCase();
    return icons.filter(
      (icon) =>
        icon.prefix.toLowerCase().includes(lower) ||
        icon.label.toLowerCase().includes(lower) ||
        (icon.terms != null && icon.terms.some((t) => t.includes(lower)))
    );
  }, [icons, searchTerm]);

  // Emptying the filter keeps the focus in it, so that the next filter can be typed right away.
  const handleClearSearch = useCallback(() => {
    setSearchTerm('');
    searchRef.current?.focus();
  }, []);

  const handleSimpleSelect = useCallback(
    (encoded: string) => {
      onSelect(encoded);
    },
    [onSelect]
  );

  const handleAdvancedIconClick = useCallback((encoded: string) => {
    setAdvancedInput(encoded);
  }, []);

  const handleOk = useCallback(() => {
    onSelect(advancedInput || null);
  }, [advancedInput, onSelect]);

  const handleClear = useCallback(() => {
    onSelect(null);
  }, [onSelect]);

  const handleRetry = useCallback(async (e: React.MouseEvent) => {
    e.preventDefault();
    setLoadError(false);
    try {
      await onLoadIcons();
    } catch {
      setLoadError(true);
    }
  }, [onLoadIcons]);

  // Portaled to the document body, so that no clipping or transformed ancestor - a popover, a
  // scrolling panel - confines the fixed popup; the anchored-overlay mark keeps it part of the
  // surface around the field for focus trap and outside press.
  return createPortal(
    <div
      className="tlIconSelect__popup"
      ref={popupRef}
      {...anchoredOverlayProps}
      style={
        position
          ? { top: position.top, left: position.left, visibility: 'visible' }
          : { visibility: 'hidden' }
      }
    >
      {/* Tab bar */}
      <div className="tlIconSelect__tabs">
        <button
          className={
            'tlIconSelect__tab' + (tab === 'simple' ? ' tlIconSelect__tab--active' : '')
          }
          onClick={() => setTab('simple')}
        >
          {i18n['js.iconSelect.simpleTab']}
        </button>
        <button
          className={
            'tlIconSelect__tab' + (tab === 'advanced' ? ' tlIconSelect__tab--active' : '')
          }
          onClick={() => setTab('advanced')}
        >
          {i18n['js.iconSelect.advancedTab']}
        </button>
      </div>

      {/* Search */}
      <div className="tlIconSelect__searchWrapper">
        <TextInput
          ref={searchRef}
          icon={ICON_SEARCH}
          value={searchTerm}
          onChange={setSearchTerm}
          placeholder={i18n['js.iconSelect.filterPlaceholder']}
          aria-label={i18n['js.iconSelect.filterPlaceholder']}
          action={searchTerm && (
            <Button
              label={i18n['js.iconSelect.clearFilter']}
              image={ICON_CLEAR}
              display="icon-only"
              appearance="ghost"
              small
              onClick={handleClearSearch}
            />
          )}
        />
      </div>

      {/* Icon grid */}
      <div
        className="tlIconSelect__grid"
        role="listbox"
      >
        {!iconsLoaded && !loadError && (
          <div className="tlIconSelect__loading">
            <Spinner label={i18n['js.iconSelect.loading']} />
          </div>
        )}
        {loadError && (
          <div className="tlIconSelect__noResults">
            <a href="#" onClick={handleRetry}>
              {i18n['js.iconSelect.loadError']}
            </a>
          </div>
        )}
        {iconsLoaded && filteredIcons.length === 0 && (
          <div className="tlIconSelect__noResults">{i18n['js.iconSelect.noResults']}</div>
        )}
        {iconsLoaded &&
          filteredIcons.map((icon) =>
            icon.variants.map((variant) => (
              <div
                key={variant.encoded}
                className={
                  'tlIconSelect__iconCell' +
                  (variant.encoded === currentValue
                    ? ' tlIconSelect__iconCell--selected'
                    : '')
                }
                role="option"
                aria-selected={variant.encoded === currentValue}
                tabIndex={0}
                aria-label={icon.label}
                {...tooltipProps(icon.label)}
                onClick={() =>
                  tab === 'simple'
                    ? handleSimpleSelect(variant.encoded)
                    : handleAdvancedIconClick(variant.encoded)
                }
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    tab === 'simple'
                      ? handleSimpleSelect(variant.encoded)
                      : handleAdvancedIconClick(variant.encoded);
                  }
                }}
              >
                <IconPreview encoded={variant.encoded} />
              </div>
            ))
          )}
      </div>

      {/* Advanced: edit area */}
      {tab === 'advanced' && (
        <div className="tlIconSelect__advancedArea">
          <div className="tlIconSelect__editRow">
            <span className="tlIconSelect__editLabel">
              {i18n['js.iconSelect.classLabel']}
            </span>
            <TextInput
              aria-label={i18n['js.iconSelect.classLabel']}
              value={advancedInput}
              onChange={setAdvancedInput}
            />
          </div>
          <div className="tlIconSelect__previewArea">
            <span className="tlIconSelect__editLabel">
              {i18n['js.iconSelect.previewLabel']}
            </span>
            <div className="tlIconSelect__previewIcon">
              {advancedInput && <IconPreview encoded={advancedInput} />}
            </div>
            <span className="tlIconSelect__previewLabel">
              {advancedInput
                ? advancedInput.startsWith('css:')
                  ? advancedInput.substring(4)
                  : advancedInput
                : ''}
            </span>
          </div>
        </div>
      )}

      {/* Advanced: action buttons */}
      {tab === 'advanced' && (
        <div className="tlIconSelect__actions">
          <button className="tlIconSelect__btn tlIconSelect__btn--cancel" onClick={onCancel}>
            {i18n['js.iconSelect.cancel']}
          </button>
          <button className="tlIconSelect__btn tlIconSelect__btn--clear" onClick={handleClear}>
            {i18n['js.iconSelect.clear']}
          </button>
          <button className="tlIconSelect__btn tlIconSelect__btn--ok" onClick={handleOk}>
            {i18n['js.iconSelect.ok']}
          </button>
        </div>
      )}
    </div>,
    document.body
  );
};

export default IconSelectPopup;
