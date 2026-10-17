import { React, tooltipProps, useI18N } from 'tl-react-bridge';
import { useRovingFocus } from '../useRovingFocus';

const { useCallback, useRef } = React;

const I18N_KEYS = { 'js.colorInput.colors': 'Colors' };

/** MIME type used to identify color drops from the preview swatch. */
export const COLOR_DRAG_TYPE = 'application/x-tl-color';

interface ColorPaletteProps {
  /** Flat array of hex color strings (row-major). null = empty slot. */
  colors: (string | null)[];
  /** Number of columns in the grid. */
  columns: number;
  /** The color of the draft: the cell holding it is selected; null: none. */
  selected: string | null;
  /** Called when a color cell is clicked or chosen by Space. */
  onSelect: (hex: string) => void;
  /** Called when a color cell is double-clicked or chosen by Enter (immediate confirm). */
  onConfirm: (hex: string) => void;
  /** Called when two cells are swapped via drag-and-drop. */
  onSwap: (fromIndex: number, toIndex: number) => void;
  /** Called when an external color is dropped onto a cell. */
  onReplace: (index: number, hex: string) => void;
}

/**
 * A grid of color cells (a listbox). Supports:
 * - Click or Space to preview a color
 * - Double-click or Enter to confirm immediately
 * - Arrow keys, Home and End among the filled cells (one tab stop: the selected cell, else the first)
 * - Drag-and-drop between cells to swap their colors
 *
 * Only a filled cell is an option; an empty cell is a drop target for the drag and nothing else, so a palette
 * without a filled cell has no tab stop.
 */
const ColorPalette: React.FC<ColorPaletteProps> = ({
  colors,
  columns,
  selected,
  onSelect,
  onConfirm,
  onSwap,
  onReplace,
}) => {
  const i18n = useI18N(I18N_KEYS);
  const dragIndex = useRef<number | null>(null);
  const containerRef = useRef<HTMLDivElement>(null);

  const filled = colors.filter(c => c != null);
  const wanted = selected?.toUpperCase();
  const selectedAt = wanted == null ? -1 : filled.findIndex(c => c!.toUpperCase() === wanted);
  const roving = useRovingFocus(containerRef, '[role="option"]', filled.length, selectedAt);

  const handleDragStart = useCallback(
    (index: number) => (e: React.DragEvent) => {
      dragIndex.current = index;
      e.dataTransfer.effectAllowed = 'move';
    },
    []
  );

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
  }, []);

  const handleDrop = useCallback(
    (index: number) => (e: React.DragEvent) => {
      e.preventDefault();
      const externalHex = e.dataTransfer.getData(COLOR_DRAG_TYPE);
      if (externalHex) {
        onReplace(index, externalHex);
      } else if (dragIndex.current !== null && dragIndex.current !== index) {
        onSwap(dragIndex.current, index);
      }
      dragIndex.current = null;
    },
    [onSwap, onReplace]
  );

  const handleOptionKey = (hex: string) => (e: React.KeyboardEvent) => {
    if (e.key === ' ') {
      e.preventDefault();
      onSelect(hex);
    } else if (e.key === 'Enter') {
      e.preventDefault();
      onConfirm(hex);
    }
  };

  let position = -1;
  return (
    <div
      ref={containerRef}
      className="tl-color-picker__palette"
      role="listbox"
      aria-label={i18n['js.colorInput.colors']}
      style={{ gridTemplateColumns: `repeat(${columns}, var(--tl-size-control))` }}
      onKeyDown={roving.onKeyDown}
      onFocus={roving.onFocus}
    >
      {colors.map((hex, i) => {
        if (hex == null) {
          return (
            <div
              key={i}
              className="tl-swatch tl-swatch--color"
              data-tl-state="empty"
              onDragOver={handleDragOver}
              onDrop={handleDrop(i)}
            />
          );
        }
        position++;
        return (
          <div
            key={i}
            className="tl-swatch tl-swatch--color"
            role="option"
            aria-selected={position === selectedAt}
            aria-label={hex.toUpperCase()}
            tabIndex={roving.tabIndexOf(position)}
            style={{ backgroundColor: hex }}
            {...tooltipProps(hex)}
            draggable
            onClick={() => onSelect(hex)}
            onDoubleClick={() => onConfirm(hex)}
            onKeyDown={handleOptionKey(hex)}
            onDragStart={handleDragStart(i)}
            onDragOver={handleDragOver}
            onDrop={handleDrop(i)}
          />
        );
      })}
    </div>
  );
};

export default ColorPalette;
