import { React } from 'tl-react-bridge';
import { hsvToHex, hexToHsv } from './colorUtils';

const { useCallback, useRef } = React;

interface ColorMixerProps {
  /** Current color as '#RRGGBB'. */
  color: string;
  /** Called when the user changes the color by dragging. */
  onColorChange: (hex: string) => void;
}

/**
 * HSV color mixer with a 2D saturation/value field and a vertical hue slider.
 *
 * The SV field uses CSS gradients (no image files):
 *  - Background: pure hue color
 *  - Overlay 1: linear-gradient(to right, white, transparent) for saturation
 *  - Overlay 2: linear-gradient(to bottom, transparent, black) for value
 *
 * The hue slider uses a CSS rainbow gradient.
 * All drag interactions use the Pointer Events API.
 */
const ColorMixer: React.FC<ColorMixerProps> = ({ color, onColorChange }) => {
  const [h, s, v] = hexToHsv(color);

  const svRef = useRef<HTMLDivElement>(null);
  const hueRef = useRef<HTMLDivElement>(null);

  // --- SV field drag ---
  const handleSVPointer = useCallback(
    (clientX: number, clientY: number) => {
      const rect = svRef.current?.getBoundingClientRect();
      if (!rect) return;
      const newS = Math.max(0, Math.min(1, (clientX - rect.left) / rect.width));
      const newV = Math.max(0, Math.min(1, 1 - (clientY - rect.top) / rect.height));
      onColorChange(hsvToHex(h, newS, newV));
    },
    [h, onColorChange]
  );

  const onSVDown = useCallback(
    (e: React.PointerEvent) => {
      e.preventDefault();
      (e.target as HTMLElement).setPointerCapture(e.pointerId);
      handleSVPointer(e.clientX, e.clientY);
    },
    [handleSVPointer]
  );

  const onSVMove = useCallback(
    (e: React.PointerEvent) => {
      if (e.buttons === 0) return;
      handleSVPointer(e.clientX, e.clientY);
    },
    [handleSVPointer]
  );

  // --- Hue slider drag ---
  const handleHuePointer = useCallback(
    (clientY: number) => {
      const rect = hueRef.current?.getBoundingClientRect();
      if (!rect) return;
      const ratio = Math.max(0, Math.min(1, (clientY - rect.top) / rect.height));
      const newH = ratio * 360;
      onColorChange(hsvToHex(newH, s, v));
    },
    [s, v, onColorChange]
  );

  const onHueDown = useCallback(
    (e: React.PointerEvent) => {
      e.preventDefault();
      (e.target as HTMLElement).setPointerCapture(e.pointerId);
      handleHuePointer(e.clientY);
    },
    [handleHuePointer]
  );

  const onHueMove = useCallback(
    (e: React.PointerEvent) => {
      if (e.buttons === 0) return;
      handleHuePointer(e.clientY);
    },
    [handleHuePointer]
  );

  const pureHue = hsvToHex(h, 1, 1);

  return (
    <div className="tlColorInput__mixer">
      {/* SV field */}
      <div
        ref={svRef}
        className="tlColorInput__svField"
        style={{ backgroundColor: pureHue }}
        onPointerDown={onSVDown}
        onPointerMove={onSVMove}
      >
        <div
          className="tlColorInput__svHandle"
          style={{ left: `${s * 100}%`, top: `${(1 - v) * 100}%` }}
        />
      </div>

      {/* Hue slider */}
      <div
        ref={hueRef}
        className="tlColorInput__hueSlider"
        onPointerDown={onHueDown}
        onPointerMove={onHueMove}
      >
        <div
          className="tlColorInput__hueHandle"
          style={{ top: `${(h / 360) * 100}%` }}
        />
      </div>
    </div>
  );
};

export default ColorMixer;
