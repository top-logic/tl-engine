/**
 * Pure color-conversion and validation utilities for the TLColorInput control.
 *
 * Color spaces:
 *  - RGB: r, g, b in [0, 255]
 *  - HSV: h in [0, 360), s in [0, 1], v in [0, 1]
 *  - Hex: '#RRGGBB' string
 */

/** Clamps a number to the 0-255 byte range. */
export function clampByte(n: number): number {
  return Math.max(0, Math.min(255, Math.round(n)));
}

/** Returns true if `hex` is a valid 7-char hex color like '#ab12ef'. */
export function isValidHex(hex: string): boolean {
  return /^#[0-9a-fA-F]{6}$/.test(hex);
}

/**
 * Parses a '#RRGGBB' string into [r, g, b] integers.
 * Returns [0,0,0] for invalid input.
 */
export function hexToRgb(hex: string): [number, number, number] {
  if (!isValidHex(hex)) return [0, 0, 0];
  const n = parseInt(hex.slice(1), 16);
  return [(n >> 16) & 0xff, (n >> 8) & 0xff, n & 0xff];
}

/** Formats [r, g, b] integers into a '#RRGGBB' string (uppercase). */
export function rgbToHex(r: number, g: number, b: number): string {
  const toHex = (v: number) => clampByte(v).toString(16).padStart(2, '0');
  return '#' + toHex(r) + toHex(g) + toHex(b);
}

/**
 * Converts RGB to HSV.
 *
 * @returns [h, s, v] where h in [0, 360), s in [0, 1], v in [0, 1].
 */
export function rgbToHsv(r: number, g: number, b: number): [number, number, number] {
  const rn = r / 255;
  const gn = g / 255;
  const bn = b / 255;
  const max = Math.max(rn, gn, bn);
  const min = Math.min(rn, gn, bn);
  const delta = max - min;

  let h = 0;
  if (delta !== 0) {
    if (max === rn) {
      h = ((gn - bn) / delta) % 6;
    } else if (max === gn) {
      h = (bn - rn) / delta + 2;
    } else {
      h = (rn - gn) / delta + 4;
    }
    h *= 60;
    if (h < 0) h += 360;
  }

  const s = max === 0 ? 0 : delta / max;
  return [h, s, max];
}

/**
 * Converts HSV to RGB.
 *
 * @param h Hue in [0, 360)
 * @param s Saturation in [0, 1]
 * @param v Value in [0, 1]
 * @returns [r, g, b] integers in [0, 255].
 */
export function hsvToRgb(h: number, s: number, v: number): [number, number, number] {
  const c = v * s;
  const x = c * (1 - Math.abs(((h / 60) % 2) - 1));
  const m = v - c;

  let rn = 0, gn = 0, bn = 0;
  if (h < 60)       { rn = c; gn = x; bn = 0; }
  else if (h < 120) { rn = x; gn = c; bn = 0; }
  else if (h < 180) { rn = 0; gn = c; bn = x; }
  else if (h < 240) { rn = 0; gn = x; bn = c; }
  else if (h < 300) { rn = x; gn = 0; bn = c; }
  else              { rn = c; gn = 0; bn = x; }

  return [
    Math.round((rn + m) * 255),
    Math.round((gn + m) * 255),
    Math.round((bn + m) * 255),
  ];
}

/** Converts a hex color to HSV. Convenience wrapper. */
export function hexToHsv(hex: string): [number, number, number] {
  return rgbToHsv(...hexToRgb(hex));
}

/** Converts HSV to a hex color. Convenience wrapper. */
export function hsvToHex(h: number, s: number, v: number): string {
  return rgbToHex(...hsvToRgb(h, s, v));
}
