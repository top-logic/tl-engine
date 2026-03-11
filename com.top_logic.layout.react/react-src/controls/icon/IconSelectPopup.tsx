import { React, useI18N } from 'tl-react-bridge';

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
  anchorRef: React.RefObject<HTMLButtonElement>;
  currentValue: string | null;
  icons: IconEntry[];
  iconsLoaded: boolean;
  onSelect: (encoded: string | null) => void;
  onCancel: () => void;
  onLoadIcons: () => Promise<void>;
}

/** Renders a single icon from its encoded form as an <i> or <img> element. */
function IconPreview({ encoded, className }: { encoded: string; className?: string }) {
  if (encoded.startsWith('css:')) {
    const cssClass = encoded.substring(4);
    return <i className={cssClass + (className ? ' ' + className : '')} />;
  }
  if (encoded.startsWith('colored:')) {
    const cssClass = encoded.substring(8);
    return <i className={cssClass + (className ? ' ' + className : '')} />;
  }
  if (encoded.startsWith('/') || encoded.startsWith('theme:')) {
    return <img src={encoded} alt="" className={className} style={{ width: '1em', height: '1em' }} />;
  }
  // Fallback: try as CSS class
  return <i className={encoded + (className ? ' ' + className : '')} />;
}

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

  // Close on Escape
  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onCancel();
    };
    document.addEventListener('keydown', handler);
    return () => document.removeEventListener('keydown', handler);
  }, [onCancel]);

  // Close on click outside
  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (popupRef.current && !popupRef.current.contains(e.target as Node)) {
        onCancel();
      }
    };
    const timer = setTimeout(() => document.addEventListener('mousedown', handler), 0);
    return () => {
      clearTimeout(timer);
      document.removeEventListener('mousedown', handler);
    };
  }, [onCancel]);

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

  const handleSearchChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(e.target.value);
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

  const handleAdvancedInputChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    setAdvancedInput(e.target.value);
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

  return (
    <div
      className="tlIconSelect__popup"
      ref={popupRef}
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
        <span className="tlIconSelect__searchIcon" aria-hidden="true">
          <i className="fa-solid fa-magnifying-glass" />
        </span>
        <input
          ref={searchRef}
          type="text"
          className="tlIconSelect__search"
          value={searchTerm}
          onChange={handleSearchChange}
          placeholder={i18n['js.iconSelect.filterPlaceholder']}
          aria-label={i18n['js.iconSelect.filterPlaceholder']}
        />
        {currentValue && (
          <button
            className="tlIconSelect__resetBtn"
            onClick={handleClear}
            title={i18n['js.iconSelect.clear']}
          >
            &times;
          </button>
        )}
      </div>

      {/* Icon grid */}
      <div
        className="tlIconSelect__grid"
        style={tab === 'advanced' ? { maxHeight: '160px' } : undefined}
      >
        {!iconsLoaded && !loadError && (
          <div className="tlIconSelect__loading">
            <span className="tlIconSelect__spinner" />
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
                title={icon.label}
                onClick={() =>
                  tab === 'simple'
                    ? handleSimpleSelect(variant.encoded)
                    : handleAdvancedIconClick(variant.encoded)
                }
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
            <input
              className="tlIconSelect__editInput"
              type="text"
              value={advancedInput}
              onChange={handleAdvancedInputChange}
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
          <button className="tlIconSelect__btn tlIconSelect__btn--ok" onClick={handleOk}>
            {i18n['js.iconSelect.ok']}
          </button>
        </div>
      )}
    </div>
  );
};

export default IconSelectPopup;
export { IconPreview };
