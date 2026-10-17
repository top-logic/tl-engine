import { React, useI18N, usePart, tooltipProps, useTabList, ThemeIcon, ICON_SEARCH, ICON_CLEAR } from 'tl-react-bridge';
import { PickerPopover } from '../picker/PickerPopover';
import { useRovingFocus } from '../useRovingFocus';

const { useState, useCallback, useEffect, useRef, useMemo, useId } = React;

const I18N_KEYS = {
  'js.iconSelect.chooseIcon': 'Choose icon',
  'js.iconSelect.icons': 'Icons',
  'js.iconSelect.simpleTab': 'Simple',
  'js.iconSelect.advancedTab': 'Advanced',
  'js.iconSelect.filterPlaceholder': 'Filter icons…',
  'js.iconSelect.noResults': 'No icons found',
  'js.iconSelect.loading': 'Loading…',
  'js.iconSelect.loadError': 'The icons could not be loaded.',
  'js.iconSelect.retry': 'Load again',
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

const TABS: Tab[] = ['simple', 'advanced'];

interface IconSelectPopupProps {
  /** The element the popup hangs under and gives the focus back to. */
  anchorRef: React.RefObject<HTMLElement | null>;
  currentValue: string | null;
  icons: IconEntry[];
  iconsLoaded: boolean;
  onSelect: (encoded: string | null) => void;
  /** Called on Cancel, Escape and a press outside. Must close the popup at once. */
  onCancel: () => void;
  onLoadIcons: () => Promise<void>;
}

/** The focus starts in the filter. */
function filterField(root: HTMLElement): HTMLElement | null {
  return root.querySelector<HTMLElement>('input');
}

/**
 * The icon chooser popup (the picker shell) with two tabs: Simple takes an icon from the grid at once, Advanced
 * puts the class of an icon into a field that OK sends. The grid is one tab stop; arrows move within it.
 */
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
  const searchRef = useRef<HTMLInputElement>(null);
  const gridRef = useRef<HTMLDivElement>(null);
  const list = useTabList({ idPrefix: 'tl-icon-' + useId(), ids: TABS, selected: tab, onSelect: id => setTab(id as Tab) });
  // One panel serves both tabs: the filter and the grid are the same for each.
  const panel = list.panelProps(tab);

  // Load icons if not loaded; a failure shows the retry row, which clears the error and so loads again.
  useEffect(() => {
    if (!iconsLoaded && !loadError) {
      onLoadIcons().catch(() => setLoadError(true));
    }
  }, [iconsLoaded, loadError, onLoadIcons]);

  // The filter is there from the start; once the icons have arrived the focus is in it again.
  useEffect(() => {
    if (iconsLoaded && searchRef.current) {
      searchRef.current.focus();
    }
  }, [iconsLoaded]);

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

  // The cells in grid order: one per variant of each icon.
  const cells = useMemo(
    () => filteredIcons.flatMap((icon) => icon.variants.map((variant) => ({ encoded: variant.encoded, label: icon.label }))),
    [filteredIcons]
  );
  const selectedAt = cells.findIndex((cell) => cell.encoded === currentValue);
  const roving = useRovingFocus(gridRef, '[role="option"]', cells.length, selectedAt);

  // Emptying the filter keeps the focus in it, so that the next filter can be typed right away.
  const handleClearSearch = useCallback(() => {
    setSearchTerm('');
    searchRef.current?.focus();
  }, []);

  const choose = useCallback(
    (encoded: string) => {
      if (tab === 'simple') {
        onSelect(encoded);
      } else {
        setAdvancedInput(encoded);
      }
    },
    [tab, onSelect]
  );

  const handleOk = useCallback(() => {
    onSelect(advancedInput || null);
  }, [advancedInput, onSelect]);

  const handleClear = useCallback(() => {
    onSelect(null);
  }, [onSelect]);

  const handleRetry = useCallback(() => {
    setLoadError(false);
    // The retry button leaves with the error; the focus goes to the filter, not to the page.
    searchRef.current?.focus();
  }, []);

  const handleCellKey = (encoded: string) => (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      choose(encoded);
    }
  };

  return (
    <PickerPopover
      anchorRef={anchorRef}
      label={i18n['js.iconSelect.chooseIcon']}
      className="tl-icon-picker"
      initialFocus={filterField}
      onClose={onCancel}
    >
      <div className="tl-tabs tl-picker__tabs">
        <div className="tl-tabs__list" role="tablist" onKeyDown={list.onListKeyDown}>
          <button {...list.tabProps('simple', { controls: panel.id })} className="tl-tabs__tab tl-type-body">
            {i18n['js.iconSelect.simpleTab']}
          </button>
          <button {...list.tabProps('advanced', { controls: panel.id })} className="tl-tabs__tab tl-type-body">
            {i18n['js.iconSelect.advancedTab']}
          </button>
        </div>
        <div {...panel} hidden={false} className="tl-tabs__panel">
          <div className="tl-icon-picker__filter">
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

          {!iconsLoaded && !loadError && (
            <div className="tl-status tl-type-body" role="status">
              <Spinner label={i18n['js.iconSelect.loading']} />
              <span aria-hidden="true">{i18n['js.iconSelect.loading']}</span>
            </div>
          )}
          {loadError && (
            <div className="tl-status tl-type-body" role="alert">
              <span>{i18n['js.iconSelect.loadError']}</span>
              <Button label={i18n['js.iconSelect.retry']} display="label-only" appearance="link" onClick={handleRetry} />
            </div>
          )}
          {iconsLoaded && cells.length === 0 && (
            <div className="tl-status tl-type-body" role="status">{i18n['js.iconSelect.noResults']}</div>
          )}
          {iconsLoaded && cells.length > 0 && (
            <div
              ref={gridRef}
              className="tl-icon-picker__grid"
              role="listbox"
              aria-label={i18n['js.iconSelect.icons']}
              onKeyDown={roving.onKeyDown}
              onFocus={roving.onFocus}
            >
              {cells.map((cell, i) => (
                <div
                  key={cell.encoded}
                  className="tl-icon-picker__cell"
                  role="option"
                  aria-selected={i === selectedAt}
                  aria-label={cell.label}
                  tabIndex={roving.tabIndexOf(i)}
                  {...tooltipProps(cell.label)}
                  onClick={() => choose(cell.encoded)}
                  onKeyDown={handleCellKey(cell.encoded)}
                >
                  <ThemeIcon encoded={cell.encoded} className="tl-icon-md" />
                </div>
              ))}
            </div>
          )}

          {tab === 'advanced' && (
            <div className="tl-icon-picker__advanced">
              <label className="tl-type-label">
                {i18n['js.iconSelect.classLabel']}
                <TextInput
                  aria-label={i18n['js.iconSelect.classLabel']}
                  value={advancedInput}
                  onChange={setAdvancedInput}
                />
              </label>
              <div className="tl-icon-picker__preview">
                <span className="tl-type-label">{i18n['js.iconSelect.previewLabel']}</span>
                {advancedInput && <ThemeIcon encoded={advancedInput} className="tl-icon-lg" />}
                <span className="tl-type-body">
                  {advancedInput.startsWith('css:') ? advancedInput.substring(4) : advancedInput}
                </span>
              </div>
            </div>
          )}
        </div>
      </div>

      {tab === 'advanced' && (
        <div className="tl-button-bar tl-picker__actions">
          <Button label={i18n['js.iconSelect.clear']} display="label-only" appearance="ghost" danger onClick={handleClear} />
          <Button label={i18n['js.iconSelect.cancel']} display="label-only" appearance="secondary" onClick={onCancel} />
          <Button label={i18n['js.iconSelect.ok']} display="label-only" appearance="primary" onClick={handleOk} />
        </div>
      )}
    </PickerPopover>
  );
};

export default IconSelectPopup;
