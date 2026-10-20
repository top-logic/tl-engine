// react-src/controls/chart/useThemeDefaults.ts
import { React, useTokenSwitch } from 'tl-react-bridge';

/** The categories of the design system the series are coloured with: `--tl-category-1-surface` to `-8-surface`. */
const CATEGORIES = 8;

/** The role of the grid lines. */
const GRID = '--tl-border-separator';

/** The role of the labels and tick marks. */
const TEXT = '--tl-text-secondary';

/** The surface behind the labels of a radial scale (what the ticks of a radar sit on), see {@link BACKDROP_ALPHA}. */
const BACKDROP = '--tl-surface-layer';

/** The colours of the theme a chart paints with, as `#RRGGBB`; undefined where a token is not declared or not a colour. */
interface ThemeColors {
  /** The category colours, one entry per category: the series index stays aligned with the category. */
  palette: (string | undefined)[];
  gridColor?: string;
  textColor?: string;
  backdropColor?: string;
}

/** Alpha of the fill of a bar, an area of a pie: the colour with a hex suffix. */
const FILL_ALPHA = 'cc';

/** Alpha of the points of a line or radar. */
const POINT_ALPHA = '33';

/** Alpha of the backdrop of the tick labels of a radial scale (Chart.js draws it white at 75 % by default). */
const BACKDROP_ALPHA = 'bf';

/** The chart types with a category and a value axis (`x`, `y`) and those with a radial one (`r`). */
const CARTESIAN = ['bar', 'line', 'scatter', 'bubble'];
const RADIAL = ['radar', 'polarArea'];

const HEX = /^#([0-9a-f]{3}|[0-9a-f]{6})$/i;
const RGB = /^rgba?\(\s*([\d.]+)[\s,]+([\d.]+)[\s,]+([\d.]+)\s*(?:[,/]\s*[\d.]+%?\s*)?\)$/i;

/**
 * A token value as `#rrggbb` - the form the package declares its colours in, which takes a hex alpha suffix. A short hex
 * is expanded and an `rgb()` converted; any other value (no token, no colour, a hex with alpha) is `undefined`, and the
 * chart does not make a colour up.
 */
function toHex(value: string): string | undefined {
  const color = value.trim();
  const hex = HEX.exec(color);
  if (hex !== null) {
    const digits = hex[1].length === 3 ? hex[1].replace(/./g, ch => ch + ch) : hex[1];
    return '#' + digits.toLowerCase();
  }
  const rgb = RGB.exec(color);
  if (rgb !== null) {
    return '#' + [rgb[1], rgb[2], rgb[3]]
      .map(channel => Math.min(255, Math.round(Number(channel))).toString(16).padStart(2, '0'))
      .join('');
  }
  return undefined;
}

/** The colours of the theme, read from the custom properties at the document root as the browser computes them now. */
function readThemeColors(): ThemeColors {
  const root = getComputedStyle(document.documentElement);
  const read = (name: string) => toHex(root.getPropertyValue(name));
  const backdrop = read(BACKDROP);
  return {
    palette: Array.from({ length: CATEGORIES }, (_, i) => read(`--tl-category-${i + 1}-surface`)),
    gridColor: read(GRID),
    textColor: read(TEXT),
    backdropColor: backdrop === undefined ? undefined : backdrop + BACKDROP_ALPHA,
  };
}

const sameColors = (a: ThemeColors, b: ThemeColors) =>
  a.gridColor === b.gridColor && a.textColor === b.textColor && a.backdropColor === b.backdropColor
  && a.palette.every((color, i) => color === b.palette[i]);

/**
 * Sets `group.key` of the options to the colour unless the options have a value there (a colour, or a scriptable
 * function) or the theme has no such colour. Nothing is written otherwise: a key set to `undefined` would replace the
 * default object of Chart.js when the config is merged.
 */
function paint(options: any, group: string, key: string, color: string | undefined): void {
  if (color !== undefined && options[group]?.[key] === undefined) {
    options[group] = { ...options[group], [key]: color };
  }
}

/** A scale with the colours of the theme where the server gave none. Scale colours do not follow the root options. */
function themedScale(scale: any, radial: boolean, theme: ThemeColors): any {
  const { gridColor, textColor, backdropColor } = theme;
  const themed = { ...scale };
  paint(themed, 'grid', 'color', gridColor);
  paint(themed, 'border', 'color', gridColor);
  paint(themed, 'ticks', 'color', textColor);
  paint(themed, 'title', 'color', textColor);
  if (radial) {
    paint(themed, 'angleLines', 'color', gridColor);
    paint(themed, 'pointLabels', 'color', textColor);
    paint(themed, 'ticks', 'backdropColor', backdropColor);
  }
  return themed;
}

/** The ids of the axes Chart.js creates itself for the types of the chart: none for a pie or a doughnut. */
function defaultAxes(config: any): string[] {
  const types: string[] = [config.type, ...(config.data?.datasets ?? []).map((ds: any) => ds.type)].filter(Boolean);
  const axes: string[] = [];
  if (types.some(type => CARTESIAN.includes(type))) axes.push('x', 'y');
  if (types.some(type => RADIAL.includes(type))) axes.push('r');
  return axes;
}

/**
 * Merges the colours of the theme into a Chart.js config: the series that have no colour of their own take the
 * categories of the theme in turn (the ninth series the first again), grid, axis lines, tick labels and titles of every
 * scale the separator and the secondary text, the legend, the title and the subtitle the secondary text. Colours in
 * the config - of a series, a scale, a plugin - take precedence. A category that is not a readable colour leaves its
 * series uncoloured; it does not shift the others.
 *
 * The colours are written into the config of this chart, never into `Chart.defaults`: several charts, in different
 * themes, share a page.
 */
export function useThemeDefaults(config: any): any {
  // Read when the control renders, and again when the mode or the theme switches: a canvas takes no `var()`.
  const theme = useTokenSwitch(readThemeColors, readThemeColors, sameColors);
  return React.useMemo(() => {
    if (!config) return config;

    const { palette, textColor } = theme;
    const merged = { ...config };
    const data = merged.data ? { ...merged.data } : {};

    // Apply the categories to datasets that don't have explicit colors.
    if (data.datasets) {
      const isPieType = merged.type === 'pie' || merged.type === 'doughnut' || merged.type === 'polarArea';
      const categoryColor = (i: number) => palette[i % palette.length];

      data.datasets = data.datasets.map((ds: any, i: number) => {
        const result = { ...ds };
        if (!result.backgroundColor) {
          if (isPieType || ds.type === 'pie' || ds.type === 'doughnut' || ds.type === 'polarArea') {
            // Pie/doughnut/polarArea: one color per data point.
            const dataLen = Array.isArray(result.data) ? result.data.length : 0;
            const point = (j: number, alpha: string) => {
              const color = categoryColor(j);
              return color === undefined ? undefined : color + alpha;
            };
            result.backgroundColor = Array.from({ length: dataLen }, (_, j) => point(j, FILL_ALPHA));
            if (!result.borderColor) {
              result.borderColor = Array.from({ length: dataLen }, (_, j) => point(j, ''));
            }
          } else {
            // Bar/line/etc.: one color per dataset.
            const color = categoryColor(i);
            if (color === undefined) return result;
            const isLineType = merged.type === 'line' || merged.type === 'radar'
              || ds.type === 'line' || ds.type === 'radar';
            if (isLineType) {
              // Line/radar: no fill by default, use transparent background for points.
              result.backgroundColor = color + POINT_ALPHA;
              if (result.fill === undefined) {
                result.fill = false;
              }
            } else {
              result.backgroundColor = color + FILL_ALPHA;
            }
            if (!result.borderColor) {
              result.borderColor = color;
            }
          }
        }
        return result;
      });
    }

    merged.data = data;

    // Scales: Chart.js routes the colours of grid, axis line, ticks and title to its global defaults, not to the
    // options of the chart, so every scale gets them in its own config: the ones the server configures, and the ones
    // Chart.js creates for the type of the chart.
    const options = { ...merged.options };
    const scales = { ...options.scales };
    for (const id of new Set([...Object.keys(scales), ...defaultAxes(merged)])) {
      scales[id] = themedScale(scales[id], id === 'r' || scales[id]?.type === 'radialLinear', theme);
    }
    if (Object.keys(scales).length > 0) {
      options.scales = scales;
    }

    // Labels of the legend follow the root option `color`; title and subtitle have their own.
    if (textColor !== undefined) {
      options.color ??= textColor;
      options.plugins = { ...options.plugins };
      paint(options.plugins, 'title', 'color', textColor);
      paint(options.plugins, 'subtitle', 'color', textColor);
    }
    merged.options = options;
    return merged;
  }, [config, theme]);
}
