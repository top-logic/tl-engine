import { React, startPointerDrag, useI18N } from 'tl-react-bridge';
import { hsvToHex, hexToHsv } from './colorUtils';

const { useCallback, useEffect, useRef, useState } = React;

const I18N_KEYS = {
  'js.colorInput.saturationBrightness': 'Saturation and brightness',
  'js.colorInput.saturationBrightnessValue': 'Saturation {0} %, brightness {1} %',
  'js.colorInput.hue': 'Hue',
};

/** The hues of the spectrum, top to bottom; a datum of the drawing, not a design value. */
const SPECTRUM = 'linear-gradient(to bottom, #f00, #ff0, #0f0, #0ff, #00f, #f0f, #f00)';

/** Hue, saturation and brightness: [0, 360], [0, 1], [0, 1]. */
type Hsv = [number, number, number];

interface ColorMixerProps {
  /** Current color as '#RRGGBB'. */
  color: string;
  /** Called when the user changes the color. */
  onColorChange: (hex: string) => void;
  /** Whether the mixer is out of sight (its tab is not selected); it keeps its state. */
  hidden?: boolean;
}

/** Rounds to the 1 % grid the keys step on. */
function snap(fraction: number): number {
  return Math.round(fraction * 100) / 100;
}

/** Keeps a fraction within [0, 1]. */
function unit(fraction: number): number {
  return Math.max(0, Math.min(1, fraction));
}

/**
 * HSV color mixer: a field for saturation (across) and brightness (up), and a vertical hue slider.
 *
 * <p>Both are `role="slider"`s. At the field the arrows change saturation (left, right) and brightness (up, down)
 * by 1 %, Page up and Page down the brightness by 10 %, Home and End set the saturation to 0 and 100 %. At the hue
 * slider the arrows move the handle by 1 degree in the direction they point (down and right: further along the
 * spectrum, which is drawn from the top), Page up and Page down by 10, Home and End to 0 and 360.</p>
 *
 * <p>The mixer remembers the hue over black and grey, and the saturation over black: a color that carries no hue
 * does not lose the one the user chose, so lifting black brightens in the hue shown at the slider. Its colors are
 * data and stay inline; the geometry and the handles are the design system's (tl-color-picker).</p>
 */
const ColorMixer: React.FC<ColorMixerProps> = ({ color, onColorChange, hidden }) => {
  const i18n = useI18N(I18N_KEYS);
  const [hsv, setHsv] = useState<Hsv>(() => hexToHsv(color));
  const [h, s, v] = hsv;
  // The color the mixer reported last: a color that comes back unchanged is no news to it.
  const emitted = useRef(color.toLowerCase());

  useEffect(() => {
    if (color.toLowerCase() === emitted.current) return;
    emitted.current = color.toLowerCase();
    const [nh, ns, nv] = hexToHsv(color);
    setHsv(([oh, os]) => [ns > 0 && nv > 0 ? nh : oh, nv > 0 ? ns : os, nv]);
  }, [color]);

  const svRef = useRef<HTMLDivElement>(null);
  const hueRef = useRef<HTMLDivElement>(null);

  const apply = useCallback(
    (next: Hsv) => {
      const hex = hsvToHex(next[0], next[1], next[2]);
      emitted.current = hex;
      setHsv(next);
      onColorChange(hex);
    },
    [onColorChange]
  );

  // --- SV field ---
  const handleSVPointer = useCallback(
    (clientX: number, clientY: number) => {
      const rect = svRef.current?.getBoundingClientRect();
      if (!rect) return;
      apply([h, unit((clientX - rect.left) / rect.width), unit(1 - (clientY - rect.top) / rect.height)]);
    },
    [h, apply]
  );

  const onSVDown = useCallback(
    (e: React.PointerEvent) => {
      e.preventDefault();
      handleSVPointer(e.clientX, e.clientY);
      // Every move has already reported its color, so neither the release nor a cancelled gesture
      // has anything left to do: the color is the one the pointer last stood on.
      startPointerDrag(e, {
        cursor: 'crosshair',
        onMove: (ev) => handleSVPointer(ev.clientX, ev.clientY),
      });
    },
    [handleSVPointer]
  );

  const onSVKeyDown = (e: React.KeyboardEvent) => {
    let next: Hsv;
    switch (e.key) {
      case 'ArrowRight': next = [h, unit(snap(s + 0.01)), v]; break;
      case 'ArrowLeft': next = [h, unit(snap(s - 0.01)), v]; break;
      case 'ArrowUp': next = [h, s, unit(snap(v + 0.01))]; break;
      case 'ArrowDown': next = [h, s, unit(snap(v - 0.01))]; break;
      case 'PageUp': next = [h, s, unit(snap(v + 0.1))]; break;
      case 'PageDown': next = [h, s, unit(snap(v - 0.1))]; break;
      case 'Home': next = [h, 0, v]; break;
      case 'End': next = [h, 1, v]; break;
      default: return;
    }
    e.preventDefault();
    if (next[1] !== s || next[2] !== v) apply(next);
  };

  // --- Hue slider ---
  const handleHuePointer = useCallback(
    (clientY: number) => {
      const rect = hueRef.current?.getBoundingClientRect();
      if (!rect) return;
      apply([unit((clientY - rect.top) / rect.height) * 360, s, v]);
    },
    [s, v, apply]
  );

  const onHueDown = useCallback(
    (e: React.PointerEvent) => {
      e.preventDefault();
      handleHuePointer(e.clientY);
      startPointerDrag(e, {
        cursor: 'pointer',
        onMove: (ev) => handleHuePointer(ev.clientY),
      });
    },
    [handleHuePointer]
  );

  const onHueKeyDown = (e: React.KeyboardEvent) => {
    const now = Math.round(h);
    let next: number;
    switch (e.key) {
      case 'ArrowDown':
      case 'ArrowRight': next = now + 1; break;
      case 'ArrowUp':
      case 'ArrowLeft': next = now - 1; break;
      case 'PageDown': next = now + 10; break;
      case 'PageUp': next = now - 10; break;
      case 'Home': next = 0; break;
      case 'End': next = 360; break;
      default: return;
    }
    e.preventDefault();
    next = Math.max(0, Math.min(360, next));
    if (next !== now) apply([next, s, v]);
  };

  const pureHue = hsvToHex(h, 1, 1);
  const percentS = Math.round(s * 100);
  const percentV = Math.round(v * 100);

  return (
    <div className="tl-color-picker__mixer" hidden={hidden}>
      <div
        ref={svRef}
        className="tl-color-picker__field"
        style={{ background: `linear-gradient(to top, #000, transparent), linear-gradient(to right, #fff, ${pureHue})` }}
        role="slider"
        tabIndex={0}
        aria-label={i18n['js.colorInput.saturationBrightness']}
        aria-valuemin={0}
        aria-valuemax={100}
        aria-valuenow={percentS}
        aria-valuetext={i18n['js.colorInput.saturationBrightnessValue']
          .replace('{0}', String(percentS)).replace('{1}', String(percentV))}
        onPointerDown={onSVDown}
        onKeyDown={onSVKeyDown}
      >
        <div
          className="tl-color-picker__handle"
          style={{ left: `${s * 100}%`, top: `${(1 - v) * 100}%` }}
        />
      </div>

      <div
        ref={hueRef}
        className="tl-color-picker__hue"
        style={{ background: SPECTRUM }}
        role="slider"
        tabIndex={0}
        aria-label={i18n['js.colorInput.hue']}
        aria-orientation="vertical"
        aria-valuemin={0}
        aria-valuemax={360}
        aria-valuenow={Math.round(h)}
        onPointerDown={onHueDown}
        onKeyDown={onHueKeyDown}
      >
        <div
          className="tl-color-picker__handle"
          style={{ left: '50%', top: `${(h / 360) * 100}%` }}
        />
      </div>
    </div>
  );
};

export default ColorMixer;
