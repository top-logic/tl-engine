// react-src/controls/chart/useThemeDefaults.ts
import { React } from 'tl-react-bridge';

interface ThemeColors {
  palette?: string[];
  gridColor?: string;
  textColor?: string;
  backgroundColor?: string;
}

/**
 * Merges theme color defaults into a Chart.js config.
 * Explicit colors in the config take precedence.
 */
export function useThemeDefaults(
  config: any,
  themeColors: ThemeColors
): any {
  return React.useMemo(() => {
    if (!config || !themeColors) return config;

    const merged = { ...config };
    const data = merged.data ? { ...merged.data } : {};

    // Apply palette to datasets that don't have explicit colors.
    if (themeColors.palette && data.datasets) {
      data.datasets = data.datasets.map((ds: any, i: number) => {
        const color = themeColors.palette![i % themeColors.palette!.length];
        const result = { ...ds };
        if (!result.backgroundColor) {
          result.backgroundColor = color + 'cc'; // 80% opacity
        }
        if (!result.borderColor) {
          result.borderColor = color;
        }
        return result;
      });
    }

    merged.data = data;

    // Apply theme to scales and grid.
    const options = { ...merged.options };
    if (themeColors.textColor || themeColors.gridColor) {
      const scales = { ...(options.scales || {}) };
      for (const axis of Object.keys(scales)) {
        scales[axis] = { ...scales[axis] };
        if (themeColors.gridColor && !scales[axis].grid?.color) {
          scales[axis].grid = { ...scales[axis].grid, color: themeColors.gridColor };
        }
        if (themeColors.textColor && !scales[axis].ticks?.color) {
          scales[axis].ticks = { ...scales[axis].ticks, color: themeColors.textColor };
        }
      }
      options.scales = scales;
    }

    merged.options = options;
    return merged;
  }, [config, themeColors]);
}
