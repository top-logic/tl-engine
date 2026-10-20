// react-src/controls/chart/useThemeDefaults.ts
import { React } from 'tl-react-bridge';

/** The categories of the design system the series are coloured with: `--tl-category-1-surface` to `-8-surface`. */
const CATEGORIES = 8;

/** The role of the grid lines. */
const GRID = '--tl-border-separator';

/** The role of the labels and tick marks. */
const TEXT = '--tl-text-secondary';

/** The attributes of the document root that switch the tokens: the mode and the theme of a customer. */
const TOKEN_SWITCHES = ['data-tl-mode', 'data-tl-theme'];

/** The colours of the theme a chart paints with, as `#RRGGBB`; empty where the design system declares none. */
interface ThemeColors {
  /** The category colours, in the order of the series. */
  palette: string[];
  gridColor?: string;
  textColor?: string;
}

/** Alpha of the fill of a bar, an area of a pie: the colour with a hex suffix. */
const FILL_ALPHA = 'cc';

/** Alpha of the points of a line or radar. */
const POINT_ALPHA = '33';

const HEX = /^#([0-9a-f]{3}|[0-9a-f]{6})$/i;
const RGB = /^rgba?\(\s*([\d.]+)[\s,]+([\d.]+)[\s,]+([\d.]+)\s*(?:[,/]\s*[\d.]+%?\s*)?\)$/i;

/**
 * A token value as `#rrggbb` - the form the package declares its colours in, which takes a hex alpha suffix. A short hex
 * is expanded and an `rgb()` converted; any other value (no token, no colour) is `undefined`, and the chart does not
 * make a colour up.
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
  const palette: string[] = [];
  for (let i = 1; i <= CATEGORIES; i++) {
    const color = toHex(root.getPropertyValue(`--tl-category-${i}-surface`));
    if (color !== undefined) {
      palette.push(color);
    }
  }
  return {
    palette,
    gridColor: toHex(root.getPropertyValue(GRID)),
    textColor: toHex(root.getPropertyValue(TEXT)),
  };
}

const sameColors = (a: ThemeColors, b: ThemeColors) =>
  a.gridColor === b.gridColor && a.textColor === b.textColor
  && a.palette.length === b.palette.length && a.palette.every((color, i) => color === b.palette[i]);

/**
 * The colours of the theme as state, read when the control renders and again whenever the document root switches the
 * mode or the theme: a canvas takes no `var()`, so the chart reads the values itself and paints anew.
 */
function useThemeColors(): ThemeColors {
  const [colors, setColors] = React.useState(readThemeColors);
  React.useEffect(() => {
    const read = () => setColors(previous => {
      const next = readThemeColors();
      return sameColors(previous, next) ? previous : next;
    });
    read();
    const observer = new MutationObserver(read);
    observer.observe(document.documentElement, { attributes: true, attributeFilter: TOKEN_SWITCHES });
    return () => observer.disconnect();
  }, []);
  return colors;
}

/**
 * Merges the colours of the theme into a Chart.js config: the series that have no colour of their own take the
 * categories of the theme in turn (the ninth series the first again), the grid the separator, the labels the secondary
 * text. Colours in the config - of a series, or `color` and `borderColor` among its options - take precedence.
 */
export function useThemeDefaults(config: any): any {
  const theme = useThemeColors();
  return React.useMemo(() => {
    if (!config) return config;

    const { palette, gridColor, textColor } = theme;
    const merged = { ...config };
    const data = merged.data ? { ...merged.data } : {};

    // Apply the categories to datasets that don't have explicit colors.
    if (palette.length > 0 && data.datasets) {
      const isPieType = merged.type === 'pie' || merged.type === 'doughnut' || merged.type === 'polarArea';

      data.datasets = data.datasets.map((ds: any, i: number) => {
        const result = { ...ds };
        if (!result.backgroundColor) {
          if (isPieType || ds.type === 'pie' || ds.type === 'doughnut' || ds.type === 'polarArea') {
            // Pie/doughnut/polarArea: one color per data point.
            const dataLen = Array.isArray(result.data) ? result.data.length : 0;
            result.backgroundColor = Array.from({ length: dataLen }, (_, j) =>
              palette[j % palette.length] + FILL_ALPHA
            );
            if (!result.borderColor) {
              result.borderColor = Array.from({ length: dataLen }, (_, j) =>
                palette[j % palette.length]
              );
            }
          } else {
            // Bar/line/etc.: one color per dataset.
            const color = palette[i % palette.length];
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

    // Grid lines and labels: the root options of Chart.js are what the scales, the legend and the title default to
    // (`borderColor` for the grid and the axis lines, `color` for ticks, legend and title), also for the scales the
    // chart creates itself.
    const options = { ...merged.options };
    if (gridColor && !options.borderColor) {
      options.borderColor = gridColor;
    }
    if (textColor && !options.color) {
      options.color = textColor;
    }
    merged.options = options;
    return merged;
  }, [config, theme]);
}
