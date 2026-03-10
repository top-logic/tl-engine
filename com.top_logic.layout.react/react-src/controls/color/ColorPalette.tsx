import { React } from 'tl-react-bridge';

const { useCallback, useRef } = React;

interface ColorPaletteProps {
  /** Flat array of hex color strings (row-major). null = empty slot. */
  colors: (string | null)[];
  /** Number of columns in the grid. */
  columns: number;
  /** Called when a color cell is clicked. */
  onSelect: (hex: string) => void;
  /** Called when a color cell is double-clicked (immediate confirm). */
  onConfirm: (hex: string) => void;
  /** Called when two cells are swapped via drag-and-drop. */
  onSwap: (fromIndex: number, toIndex: number) => void;
}

/**
 * A grid of color cells. Supports:
 * - Click to preview a color
 * - Double-click to confirm immediately
 * - Drag-and-drop between cells to swap their colors
 */
const ColorPalette: React.FC<ColorPaletteProps> = ({
  colors,
  columns,
  onSelect,
  onConfirm,
  onSwap,
}) => {
  const dragIndex = useRef<number | null>(null);

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
      if (dragIndex.current !== null && dragIndex.current !== index) {
        onSwap(dragIndex.current, index);
      }
      dragIndex.current = null;
    },
    [onSwap]
  );

  return (
    <div
      className="tlColorInput__palette"
      style={{ gridTemplateColumns: `repeat(${columns}, 1fr)` }}
    >
      {colors.map((hex, i) => (
        <div
          key={i}
          className={
            'tlColorInput__paletteCell' +
            (hex == null ? ' tlColorInput__paletteCell--empty' : '')
          }
          style={hex != null ? { backgroundColor: hex } : undefined}
          title={hex ?? ''}
          draggable={hex != null}
          onClick={hex != null ? () => onSelect(hex) : undefined}
          onDoubleClick={hex != null ? () => onConfirm(hex) : undefined}
          onDragStart={hex != null ? handleDragStart(i) : undefined}
          onDragOver={handleDragOver}
          onDrop={handleDrop(i)}
        />
      ))}
    </div>
  );
};

export default ColorPalette;
