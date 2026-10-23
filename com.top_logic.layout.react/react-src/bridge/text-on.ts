/**
 * The readable text on a color the server sends as data - the color of a calendar event, for one.
 *
 * Such a color is no role of the design system, so no derived text role exists for it. The text is
 * chosen in the browser by the rule `derive` applies to the surfaces of a theme: of the two candidates
 * the theme declares (`--tl-text-contrast-light`, `--tl-text-contrast-dark`), the one with the higher
 * WCAG contrast to the color, the dark one on a tie.
 */

/** The candidate roles, as custom properties at the document root. */
const LIGHT_CANDIDATE = '--tl-text-contrast-light';
const DARK_CANDIDATE = '--tl-text-contrast-dark';

/** A color as its red, green and blue channels, 0 to 255; alpha is ignored (counts as opaque). */
type Rgb = [number, number, number];

/** `#rgb`, `#rgba`, `#rrggbb`, `#rrggbbaa`. */
const HEX = /^#([0-9a-f]{3,4}|[0-9a-f]{6}|[0-9a-f]{8})$/i;

/** `rgb()` / `rgba()` with comma- or space-separated channels, as a computed style serializes it. */
const RGB = /^rgba?\(\s*([\d.]+)[\s,]+([\d.]+)[\s,]+([\d.]+)\s*(?:[,/]\s*[\d.]+%?\s*)?\)$/i;

/**
 * The text role readable on the given color: `var(--tl-text-contrast-light)` or
 * `var(--tl-text-contrast-dark)`.
 *
 * @param color Any CSS color, as the server sends it (hex, `rgb()`, a name, …).
 * @return `null` where the color cannot be resolved, or the design system declares no candidates:
 *         then no readable text can be promised, and the caller draws a role of its own instead
 *         (the category, the brand) rather than the color.
 */
export function textOn(color: string): string | null {
  const surface = resolve(color);
  if (surface === null) {
    return null;
  }
  const root = getComputedStyle(document.documentElement);
  const light = resolve(root.getPropertyValue(LIGHT_CANDIDATE));
  const dark = resolve(root.getPropertyValue(DARK_CANDIDATE));
  if (light === null || dark === null) {
    return null;
  }
  return contrast(dark, surface) >= contrast(light, surface)
    ? `var(${DARK_CANDIDATE})`
    : `var(${LIGHT_CANDIDATE})`;
}

/**
 * The channels of a CSS color: a hex or `rgb()` value directly, any other through the browser - the
 * computed style of a probe element. `null` for a value that is no color, or one the browser does
 * not compute to `rgb()`.
 */
function resolve(value: string): Rgb | null {
  const color = value.trim();
  if (color === '') {
    return null;
  }
  const direct = parse(color);
  if (direct !== null) {
    return direct;
  }
  const probe = document.createElement('span');
  probe.style.color = color;
  if (probe.style.color === '') {
    return null;
  }
  const host = document.body ?? document.documentElement;
  host.appendChild(probe);
  try {
    return parse(getComputedStyle(probe).color);
  } finally {
    probe.remove();
  }
}

/** The channels of a hex or `rgb()` value; `null` for any other. */
function parse(color: string): Rgb | null {
  const hex = HEX.exec(color);
  if (hex !== null) {
    const digits = hex[1];
    const short = digits.length <= 4;
    const channel = (i: number) => short
      ? parseInt(digits[i] + digits[i], 16)
      : parseInt(digits.slice(2 * i, 2 * i + 2), 16);
    return [channel(0), channel(1), channel(2)];
  }
  const rgb = RGB.exec(color);
  if (rgb !== null) {
    return [Number(rgb[1]), Number(rgb[2]), Number(rgb[3])];
  }
  return null;
}

/** The WCAG contrast ratio of two colors, 1 to 21. */
function contrast(a: Rgb, b: Rgb): number {
  const la = luminance(a);
  const lb = luminance(b);
  return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
}

/** The relative luminance of a color (WCAG 2). */
function luminance([r, g, b]: Rgb): number {
  const linear = (c: number) => {
    const s = c / 255;
    return s <= 0.04045 ? s / 12.92 : ((s + 0.055) / 1.055) ** 2.4;
  };
  return 0.2126 * linear(r) + 0.7152 * linear(g) + 0.0722 * linear(b);
}
