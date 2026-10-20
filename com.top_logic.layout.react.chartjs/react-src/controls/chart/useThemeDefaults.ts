// react-src/controls/chart/useThemeDefaults.ts
import { React, useTokenSwitch } from 'tl-react-bridge';
import type { Chart, Plugin } from 'chart.js';

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

/**
 * Paints one scale of an update: `target` is the config Chart.js merged for the scale, with the colours it copied from
 * its global defaults; `server` is the scale of the same id in the config of the server. A colour the server set there -
 * a value or a scriptable function - stays; every other colour is the theme's, where the theme has one.
 */
function paintScale(target: any, server: any, radial: boolean, theme: ThemeColors): void {
  const { gridColor, textColor, backdropColor } = theme;
  const over = (group: string, key: string, color: string | undefined) => {
    if (color !== undefined && server?.[group]?.[key] === undefined) {
      (target[group] ??= {})[key] = color;
    }
  };
  over('grid', 'color', gridColor);
  over('border', 'color', gridColor);
  over('ticks', 'color', textColor);
  over('title', 'color', textColor);
  if (radial) {
    over('angleLines', 'color', gridColor);
    over('pointLabels', 'color', textColor);
    over('ticks', 'backdropColor', backdropColor);
  }
}

/** What a chart paints its scales with: the config of the server and the colours of the theme, as last rendered. */
interface Painting {
  config: any;
  theme: ThemeColors;
}

/**
 * The plugin of one chart that paints its scales, handed to that chart and never registered with Chart.js.
 *
 * Chart.js builds scales from the config and from the datasets (an axis id of its own), binds a dataset without an axis
 * id to the first scale of the axis, and copies the colours of grid, axis line, tick labels and titles from its global
 * defaults into every scale. The scales of the config are therefore left as the server sent them: the plugin paints
 * every scale Chart.js built once it has merged and built them (`beforeUpdate`), on each update - a new config, a
 * resize, a switch of the mode. Spokes, point labels and the backdrop of the tick labels go to a radial scale only.
 */
function scalePainter(painting: { current: Painting }): Plugin {
  return {
    id: 'tlThemeScales',
    beforeUpdate(chart: Chart) {
      const { config, theme } = painting.current;
      const merged = (chart.config.options as any)?.scales ?? {};
      for (const scale of Object.values(chart.scales)) {
        const target = merged[scale.id];
        if (target !== undefined) {
          paintScale(target, config?.options?.scales?.[scale.id], scale.axis === 'r', theme);
        }
      }
    },
  };
}

/**
 * Merges the colours of the theme into a Chart.js config: the series that have no colour of their own take the
 * categories of the theme in turn (the ninth series the first again), grid, axis lines, tick labels and titles of every
 * scale the separator and the secondary text, the legend, the title and the subtitle the secondary text. Colours in
 * the config - of a series, a scale, a plugin - take precedence. A category that is not a readable colour gives its
 * series the neutral separator colour; it does not shift the others. Without any readable category nothing is coloured,
 * and the Colors plugin of Chart.js, which would make a palette up, is off unless the server turns it on.
 *
 * The scales of the config stay as the server sent them, so Chart.js builds the same scales as without the theme; the
 * returned config carries, under `plugins`, the plugin of this chart that paints them (see {@link scalePainter}). The
 * colours go into the config of this chart, never into `Chart.defaults`: several charts, in different themes, share a
 * page.
 */
export function useThemeDefaults(config: any): any {
  // Read when the control renders, and again when the mode or the theme switches: a canvas takes no `var()`.
  const theme = useTokenSwitch(readThemeColors, readThemeColors, sameColors);

  // react-chartjs-2 hands a chart its plugins once, when it creates it; the painter reads the latest config and theme
  // at each update the render causes.
  const painting = React.useRef<Painting>({ config, theme });
  React.useLayoutEffect(() => {
    painting.current = { config, theme };
  }, [config, theme]);
  const painter = React.useMemo(() => scalePainter(painting), []);

  return React.useMemo(() => {
    if (!config) return config;

    const { palette, gridColor, textColor } = theme;
    const merged = { ...config };
    const data = merged.data ? { ...merged.data } : {};

    // Apply the categories to datasets that don't have explicit colors. A category that is not a readable colour gives
    // its series or point the neutral separator colour, so it stays visibly uncategorised and the others keep their
    // index; with no readable category at all (no design system) nothing is made up.
    if (data.datasets && palette.some(color => color !== undefined)) {
      const isPieType = merged.type === 'pie' || merged.type === 'doughnut' || merged.type === 'polarArea';
      const categoryColor = (i: number) => palette[i % palette.length] ?? gridColor;

      data.datasets = data.datasets.map((ds: any, i: number) => {
        const result = { ...ds };
        if (!result.backgroundColor) {
          if (isPieType || ds.type === 'pie' || ds.type === 'doughnut' || ds.type === 'polarArea') {
            // Pie/doughnut/polarArea: one color per data point.
            const dataLen = Array.isArray(result.data) ? result.data.length : 0;
            const points = Array.from({ length: dataLen }, (_, j) => categoryColor(j));
            // No colour at all for a point (no neutral colour either): the dataset is left alone, no array holds a gap.
            if (points.some(color => color === undefined)) return result;
            result.backgroundColor = points.map(color => color + FILL_ALPHA);
            if (!result.borderColor) {
              result.borderColor = points;
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
    merged.plugins = [painter];

    const options = { ...merged.options };

    // Labels of the legend follow the root option `color`; title and subtitle have their own.
    options.plugins = { ...options.plugins };
    if (textColor !== undefined) {
      options.color ??= textColor;
      paint(options.plugins, 'title', 'color', textColor);
      paint(options.plugins, 'subtitle', 'color', textColor);
    }
    // The Colors plugin of Chart.js paints series that have no colour with a palette of its own. TopLogic defines every
    // value of the design, no foreign palette: a series without a colour stays without one.
    options.plugins.colors = { ...options.plugins.colors, enabled: options.plugins.colors?.enabled ?? false };
    merged.options = options;
    return merged;
  }, [config, theme, painter]);
}
