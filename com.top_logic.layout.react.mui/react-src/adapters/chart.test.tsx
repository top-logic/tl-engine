// The chart (spec 2026-10-20 wave 5d §8): its series take their colours from the categories of the theme, grid and
// labels from the border and text roles, and the chart reads them again when the mode switches. Chart.js paints onto a
// canvas, which jsdom cannot (no 2D context), so the colours are checked where the control computes them
// (useThemeDefaults, from custom properties at the document root, as textOn is checked), and the DOM around the canvas
// through the control: status lines, the name of the canvas, the tooltip and the reset button.
//
// Grid, axis lines, tick labels and titles are checked on a real Chart.js chart over a context that records nothing:
// Chart.js routes those colours to its global defaults, so the config of each scale has to carry them.
//
// Review focus 3: ten series -> series 9 and 10 take category 1 and 2; after data-tl-mode="dark" the series, the grid
// and the labels have the values of the dark mode without a reload; a series with a colour of its own keeps it.

import { describe, it, expect, vi, afterEach, beforeAll, beforeEach } from 'vitest';
import { act, cleanup, renderHook, render, waitFor } from '@testing-library/react';
import { React, registerPart } from 'tl-react-bridge';
import ButtonPart from '../../../com.top_logic.layout.react/react-src/controls/button/ButtonPart';
import { useThemeDefaults } from '../../../com.top_logic.layout.react.chartjs/react-src/controls/chart/useThemeDefaults';
import ChartTooltip from '../../../com.top_logic.layout.react.chartjs/react-src/controls/chart/ChartTooltip';
import TLChart from '../../../com.top_logic.layout.react.chartjs/react-src/controls/TLChart';
import { Chart, registerables } from '../../../com.top_logic.layout.react.chartjs/node_modules/chart.js';
import { CONTROL_ID, mountAdapter, settle } from './wire-test-support';

Chart.register(...registerables);

/** The eight categories of a mode as `#RRGGBB`: the tokens of the package are hex. */
const LIGHT = ['#c0392b', '#d68910', '#27ae60', '#16a085', '#2874a6', '#5b2c6f', '#a93f92', '#884ea0'];
const DARK = ['#e57368', '#f0b27a', '#58d68d', '#48c9b0', '#5dade2', '#af7ac5', '#d98cc9', '#bb8fce'];

const COLORS = {
  light: { categories: LIGHT, grid: '#d0d5dd', text: '#475467' },
  dark: { categories: DARK, grid: '#344054', text: '#98a2b3' },
};

/** The custom properties of a mode at the document root, as the stylesheets of the package declare them. */
function applyTokens(mode: 'light' | 'dark') {
  const root = document.documentElement;
  COLORS[mode].categories.forEach((color, i) => root.style.setProperty(`--tl-category-${i + 1}-surface`, color));
  root.style.setProperty('--tl-border-separator', COLORS[mode].grid);
  root.style.setProperty('--tl-text-secondary', COLORS[mode].text);
}

/** Switches the mode as the user menu does: the attribute, and with it the tokens. */
function switchMode(mode: 'light' | 'dark') {
  act(() => {
    applyTokens(mode);
    document.documentElement.setAttribute('data-tl-mode', mode);
  });
}

/** The context of a canvas that answers every call with nothing and measures every text 10 wide. */
function recordingContext(canvas: HTMLCanvasElement): CanvasRenderingContext2D {
  const members: Record<string | symbol, unknown> = { canvas, measureText: () => ({ width: 10 }) };
  return new Proxy(members, {
    get: (target, name) => (name in target ? target[name] : () => undefined),
  }) as unknown as CanvasRenderingContext2D;
}

/** Gives every canvas of the page such a context, and the resize observer a responsive chart installs. */
function stubContexts() {
  vi.spyOn(HTMLCanvasElement.prototype, 'getContext').mockImplementation(function (this: HTMLCanvasElement) {
    return recordingContext(this);
  } as any);
  vi.stubGlobal('ResizeObserver', class { observe() {} unobserve() {} disconnect() {} });
}

/**
 * A chart over a canvas whose 2D context records nothing: jsdom has none, and Chart.js needs one to lay out and to
 * measure text. Draws once; `repaint` hands it the config of the hook again, as react-chartjs-2 does after a render.
 */
function drawChart(themed: any): { chart: Chart; repaint: (next: any) => void } {
  const canvas = document.createElement('canvas');
  canvas.width = 300;
  canvas.height = 150;
  stubContexts();
  const calm = (config: any) => ({ ...config, options: { ...config.options, responsive: false, animation: false } });
  const chart = new Chart(canvas, calm(themed));
  return {
    chart,
    repaint: (next) => {
      chart.data = next.data;
      chart.options = calm(next).options;
      chart.update();
    },
  };
}

/** A bar chart of the given number of series, none with a colour of its own. */
function barChart(series: number) {
  return {
    type: 'bar',
    data: {
      labels: ['a', 'b'],
      datasets: Array.from({ length: series }, (_, i) => ({ label: `s${i + 1}`, data: [i, i + 1] })),
    },
    options: { scales: { y: { beginAtZero: true } } },
  };
}

beforeAll(() => {
  registerPart('Button', ButtonPart);
});

beforeEach(() => {
  document.documentElement.setAttribute('data-tl-mode', 'light');
  applyTokens('light');
});

afterEach(() => {
  cleanup();
  document.body.replaceChildren();
  const root = document.documentElement;
  root.removeAttribute('data-tl-mode');
  root.removeAttribute('style');
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
});

describe('chart colours', () => {
  it('series 9 and 10 take category 1 and 2, the grid and the labels the roles of the theme', () => {
    const config = barChart(10);
    const { result } = renderHook(() => useThemeDefaults(config));

    const datasets = result.current.data.datasets;
    datasets.forEach((dataset: any, i: number) => {
      expect(dataset.borderColor, `series ${i + 1}`).toBe(LIGHT[i % 8]);
      expect(dataset.backgroundColor, `series ${i + 1}`).toBe(LIGHT[i % 8] + 'cc');
    });
    expect(datasets[8].borderColor).toBe(datasets[0].borderColor);
    expect(datasets[9].borderColor).toBe(datasets[1].borderColor);
  });

  it('reads the tokens again when data-tl-mode switches, without a reload', async () => {
    const config = barChart(10);
    const { result } = renderHook(() => useThemeDefaults(config));
    expect(result.current.data.datasets[0].borderColor).toBe(LIGHT[0]);

    switchMode('dark');

    await waitFor(() => expect(result.current.data.datasets[0].borderColor).toBe(DARK[0]));
    const datasets = result.current.data.datasets;
    datasets.forEach((dataset: any, i: number) => {
      expect(dataset.borderColor, `series ${i + 1}`).toBe(DARK[i % 8]);
      expect(dataset.backgroundColor, `series ${i + 1}`).toBe(DARK[i % 8] + 'cc');
    });
    expect(datasets[8].borderColor).toBe(DARK[0]);
    expect(datasets[9].borderColor).toBe(DARK[1]);

    switchMode('light');
    await waitFor(() => expect(result.current.data.datasets[0].borderColor).toBe(LIGHT[0]));
  });

  it('reads the tokens again when data-tl-theme switches (a customer theme)', async () => {
    const config = barChart(2);
    const { result } = renderHook(() => useThemeDefaults(config));

    act(() => {
      document.documentElement.style.setProperty('--tl-category-1-surface', '#123456');
      document.documentElement.setAttribute('data-tl-theme', 'customer');
    });

    await waitFor(() => expect(result.current.data.datasets[0].borderColor).toBe('#123456'));
    document.documentElement.removeAttribute('data-tl-theme');
  });

  it('a series with a colour of its own keeps it, the others take the theme', () => {
    const config = barChart(3);
    config.data.datasets[1] = { ...config.data.datasets[1], backgroundColor: '#112233', borderColor: '#445566' } as any;
    const { result } = renderHook(() => useThemeDefaults(config));

    const datasets = result.current.data.datasets;
    expect(datasets[1].backgroundColor).toBe('#112233');
    expect(datasets[1].borderColor).toBe('#445566');
    expect(datasets[0].borderColor).toBe(LIGHT[0]);
    expect(datasets[2].borderColor).toBe(LIGHT[2]);
  });

  it('an unreadable category gives its series the neutral separator colour and does not shift the others', () => {
    document.documentElement.style.setProperty('--tl-category-2-surface', 'not-a-colour');
    const { result } = renderHook(() => useThemeDefaults(barChart(10)));

    const datasets = result.current.data.datasets;
    expect(datasets[1].borderColor).toBe(COLORS.light.grid);
    expect(datasets[1].backgroundColor).toBe(COLORS.light.grid + 'cc');
    expect(datasets[2].borderColor).toBe(LIGHT[2]);
    expect(datasets[8].borderColor).toBe(LIGHT[0]);
    expect(datasets[9].borderColor).toBe(COLORS.light.grid);
  });

  it('a pie with one unreadable category draws that point neutral, and no array holds a gap', () => {
    document.documentElement.style.setProperty('--tl-category-2-surface', 'not-a-colour');
    const pie = { type: 'pie', data: { labels: ['a', 'b', 'c'], datasets: [{ data: [1, 2, 3] }] } };
    const { result } = renderHook(() => useThemeDefaults(pie));

    const dataset = result.current.data.datasets[0];
    expect(dataset.borderColor).toEqual([LIGHT[0], COLORS.light.grid, LIGHT[2]]);
    expect(dataset.backgroundColor).toEqual([LIGHT[0] + 'cc', COLORS.light.grid + 'cc', LIGHT[2] + 'cc']);
  });

  it('a pie takes one category per data point, a line draws its points transparent', () => {
    const pie = { type: 'pie', data: { labels: ['a', 'b', 'c'], datasets: [{ data: [1, 2, 3] }] } };
    const { result: pieResult } = renderHook(() => useThemeDefaults(pie));
    expect(pieResult.current.data.datasets[0].borderColor).toEqual([LIGHT[0], LIGHT[1], LIGHT[2]]);
    expect(pieResult.current.data.datasets[0].backgroundColor).toEqual([LIGHT[0] + 'cc', LIGHT[1] + 'cc', LIGHT[2] + 'cc']);

    const line = { type: 'line', data: { datasets: [{ data: [1, 2] }] } };
    const { result: lineResult } = renderHook(() => useThemeDefaults(line));
    expect(lineResult.current.data.datasets[0].borderColor).toBe(LIGHT[0]);
    expect(lineResult.current.data.datasets[0].backgroundColor).toBe(LIGHT[0] + '33');
    expect(lineResult.current.data.datasets[0].fill).toBe(false);
  });

  it('without a design system no colour is made up: no array, no key, the Colors plugin of Chart.js off', () => {
    document.documentElement.removeAttribute('style');
    const pie = { type: 'pie', data: { labels: ['a', 'b'], datasets: [{ data: [1, 2] }] } };
    const { result: bar } = renderHook(() => useThemeDefaults(barChart(2)));
    const { result: pieResult } = renderHook(() => useThemeDefaults(pie));

    for (const themed of [bar.current, pieResult.current]) {
      expect(themed.data.datasets[0].borderColor).toBeUndefined();
      expect(themed.data.datasets[0].backgroundColor).toBeUndefined();
      expect(themed.options.color).toBeUndefined();
      expect(themed.options.plugins.colors.enabled).toBe(false);
    }
    expect(bar.current.options.scales?.y?.grid).toBeUndefined();
  });

  it('the Colors plugin stays off for a chart with colours, unless the server turns it on', () => {
    const { result } = renderHook(() => useThemeDefaults(barChart(1)));
    expect(result.current.options.plugins.colors.enabled).toBe(false);

    const on = { ...barChart(1), options: { plugins: { colors: { enabled: true } } } };
    const { result: server } = renderHook(() => useThemeDefaults(on));
    expect(server.current.options.plugins.colors.enabled).toBe(true);
  });

  it('a token written as rgb() or a short hex is read as a hex colour', () => {
    document.documentElement.style.setProperty('--tl-category-1-surface', 'rgb(18, 52, 86)');
    document.documentElement.style.setProperty('--tl-category-2-surface', '#abc');
    const { result } = renderHook(() => useThemeDefaults(barChart(2)));

    expect(result.current.data.datasets[0].borderColor).toBe('#123456');
    expect(result.current.data.datasets[1].borderColor).toBe('#aabbcc');
  });
});

/** A chart of titles on every level, so that every colour of a scale and a plugin is there to read. */
function titledChart(type: string, scales: Record<string, unknown> | undefined) {
  return {
    type,
    data: { labels: ['a', 'b', 'c'], datasets: [{ label: 's1', data: [1, 2, 3] }, { label: 's2', data: [3, 2, 1] }] },
    options: {
      scales,
      plugins: { title: { display: true, text: 'Title' }, subtitle: { display: true, text: 'Subtitle' } },
    },
  };
}

/** The titles of the axes, so that the colour of an axis title is on a scale that draws one. */
const AXIS_TITLES = { x: { title: { display: true, text: 'X' } }, y: { title: { display: true, text: 'Y' } } };

describe('chart scales (a real Chart.js chart)', () => {
  /** The colours of a cartesian scale, as Chart.js resolved them. */
  function cartesian(chart: Chart, id: string) {
    const options = (chart.scales[id] as any).options;
    return { grid: options.grid.color, border: options.border.color, ticks: options.ticks.color, title: options.title.color };
  }

  it('grid, axis line, tick labels and axis title of a bar chart take the theme, and follow the mode', async () => {
    const { result } = renderHook(() => useThemeDefaults(titledChart('bar', AXIS_TITLES)));
    const { chart, repaint } = drawChart(result.current);

    for (const id of ['x', 'y']) {
      expect(cartesian(chart, id), id).toEqual(
        { grid: COLORS.light.grid, border: COLORS.light.grid, ticks: COLORS.light.text, title: COLORS.light.text });
    }
    expect((chart as any).titleBlock.options.color).toBe(COLORS.light.text);
    expect((chart.options as any).plugins.subtitle.color).toBe(COLORS.light.text);
    expect((chart as any).legend.options.labels.color).toBe(COLORS.light.text);

    switchMode('dark');
    await waitFor(() => expect(result.current.options.scales.y.grid.color).toBe(COLORS.dark.grid));
    repaint(result.current);

    for (const id of ['x', 'y']) {
      expect(cartesian(chart, id), id).toEqual(
        { grid: COLORS.dark.grid, border: COLORS.dark.grid, ticks: COLORS.dark.text, title: COLORS.dark.text });
    }
    expect((chart as any).titleBlock.options.color).toBe(COLORS.dark.text);
    expect((chart.options as any).plugins.subtitle.color).toBe(COLORS.dark.text);
    expect((chart as any).legend.options.labels.color).toBe(COLORS.dark.text);
    chart.destroy();
  });

  it('the axes Chart.js creates itself are themed too: a line chart without scales in its config', () => {
    const { result } = renderHook(() => useThemeDefaults(titledChart('line', undefined)));
    const { chart } = drawChart(result.current);

    for (const id of ['x', 'y']) {
      expect(cartesian(chart, id), id).toMatchObject({ grid: COLORS.light.grid, ticks: COLORS.light.text });
    }
    chart.destroy();
  });

  it('a radar chart themes its radial scale: lines, spokes, labels, tick backdrop', async () => {
    document.documentElement.style.setProperty('--tl-surface-layer', '#ffffff');
    const { result } = renderHook(() => useThemeDefaults(titledChart('radar', undefined)));
    const { chart, repaint } = drawChart(result.current);
    const radial = () => (chart.scales.r as any).options;

    expect(radial().grid.color).toBe(COLORS.light.grid);
    expect(radial().angleLines.color).toBe(COLORS.light.grid);
    expect(radial().pointLabels.color).toBe(COLORS.light.text);
    expect(radial().ticks.color).toBe(COLORS.light.text);
    expect(radial().ticks.backdropColor).toBe('#ffffffbf');
    expect(Object.keys(chart.scales)).toEqual(['r']);

    act(() => {
      document.documentElement.style.setProperty('--tl-surface-layer', '#101828');
    });
    switchMode('dark');
    await waitFor(() => expect(result.current.options.scales.r.grid.color).toBe(COLORS.dark.grid));
    repaint(result.current);

    expect(radial().grid.color).toBe(COLORS.dark.grid);
    expect(radial().angleLines.color).toBe(COLORS.dark.grid);
    expect(radial().pointLabels.color).toBe(COLORS.dark.text);
    expect(radial().ticks.backdropColor).toBe('#101828bf');
    chart.destroy();
  });

  it('only the scales Chart.js builds are themed: datasets with axis ids of their own add no x or y', () => {
    const line = {
      type: 'line',
      data: {
        labels: ['a', 'b'],
        datasets: [
          { label: 'l', data: [1, 2], yAxisID: 'left' },
          { label: 'r', data: [2, 1], yAxisID: 'right' },
        ],
      },
    };
    const { result } = renderHook(() => useThemeDefaults(line));
    expect(Object.keys(result.current.options.scales).sort()).toEqual(['left', 'right', 'x']);

    const { chart } = drawChart(result.current);
    expect(Object.keys(chart.scales).sort()).toEqual(['left', 'right', 'x']);
    for (const id of ['x', 'left', 'right']) {
      expect(cartesian(chart, id), id).toMatchObject({ grid: COLORS.light.grid, ticks: COLORS.light.text });
    }
    chart.destroy();
  });

  it('a scatter with its own x id gains no x; a configured scale next to the default axes is themed as well', () => {
    const scatter = { type: 'scatter', data: { datasets: [{ label: 's', data: [{ x: 1, y: 2 }], xAxisID: 'xs' }] } };
    const { result: scatterResult } = renderHook(() => useThemeDefaults(scatter));
    const { chart: scatterChart } = drawChart(scatterResult.current);
    expect(Object.keys(scatterChart.scales).sort()).toEqual(['xs', 'y']);
    expect(cartesian(scatterChart, 'xs')).toMatchObject({ grid: COLORS.light.grid });
    scatterChart.destroy();

    const served = { ...barChart(1), options: { scales: { left: { axis: 'y', type: 'linear' } } } };
    const { result: servedResult } = renderHook(() => useThemeDefaults(served));
    const { chart } = drawChart(servedResult.current);
    // Chart.js builds x and y for the bar chart next to the configured scale; every scale it built is themed.
    expect(Object.keys(chart.scales).sort()).toEqual(['left', 'x', 'y']);
    for (const id of Object.keys(chart.scales)) {
      expect(cartesian(chart, id), id).toMatchObject({ grid: COLORS.light.grid, ticks: COLORS.light.text });
    }
    chart.destroy();
  });

  it('a pie gets no axes', () => {
    const { result } = renderHook(() => useThemeDefaults(titledChart('pie', undefined)));
    expect(result.current.options.scales).toBeUndefined();

    const { chart } = drawChart(result.current);
    expect(Object.keys(chart.scales)).toEqual([]);
    expect((chart as any).titleBlock.options.color).toBe(COLORS.light.text);
    chart.destroy();
  });

  it('colours of the server on a scale or a plugin win, the rest of the scale takes the theme', () => {
    const config = titledChart('bar', {
      y: { grid: { color: '#111111' }, ticks: { color: '#222222' }, title: { display: true, text: 'Y', color: '#333333' } },
    });
    (config.options.plugins.title as any).color = '#444444';
    const { result } = renderHook(() => useThemeDefaults(config));
    const { chart } = drawChart(result.current);

    expect(cartesian(chart, 'y')).toEqual({ grid: '#111111', border: COLORS.light.grid, ticks: '#222222', title: '#333333' });
    expect(cartesian(chart, 'x')).toMatchObject({ grid: COLORS.light.grid, ticks: COLORS.light.text });
    expect((chart as any).titleBlock.options.color).toBe('#444444');
    chart.destroy();
  });

  it('a chart of the page does not change the defaults of Chart.js, which every other chart shares', () => {
    const before = JSON.stringify([Chart.defaults.color, Chart.defaults.borderColor]);
    const { result } = renderHook(() => useThemeDefaults(titledChart('bar', undefined)));
    const { chart } = drawChart(result.current);

    expect(JSON.stringify([Chart.defaults.color, Chart.defaults.borderColor])).toBe(before);
    chart.destroy();
  });
});

describe('chart control', () => {
  /** The canvas of jsdom has no 2D context; the stand-in lets the chart lay out and draw. */
  beforeEach(stubContexts);

  it('no data is a status line, an error an alert, both of the package and centred by its root', async () => {
    mountAdapter(TLChart, { chartConfig: { type: 'bar', data: { datasets: [] } }, noDataMessage: 'Keine Daten' });
    await settle();

    const root = document.getElementById(CONTROL_ID)!;
    expect(root.classList.contains('tl-chart')).toBe(true);
    const status = root.querySelector('.tl-status')!;
    expect(status.getAttribute('role')).toBe('status');
    expect(status.textContent).toBe('Keine Daten');
    expect(root.querySelector('canvas')).toBeNull();
  });

  it('an error is announced as an alert', async () => {
    mountAdapter(TLChart, { chartConfig: barChart(1), error: 'Ungültig' });
    await settle();

    const root = document.getElementById(CONTROL_ID)!;
    const status = root.querySelector('.tl-status')!;
    expect(status.getAttribute('role')).toBe('alert');
    expect(status.textContent).toBe('Ungültig');
    expect(root.querySelector('canvas')).toBeNull();
  });

  it('the canvas is an image with a name in the language of the user, and no class of the old block is left', async () => {
    mountAdapter(TLChart, { chartConfig: barChart(2), zoomEnabled: true });
    await settle();

    const root = document.getElementById(CONTROL_ID)!;
    expect(root.classList.contains('tl-chart')).toBe(true);
    const canvas = root.querySelector('canvas')!;
    expect(canvas.getAttribute('role')).toBe('img');
    expect(canvas.getAttribute('aria-label')).toBe('js.chart.label');
    expect(root.hasAttribute('aria-label')).toBe(false);

    const reset = root.querySelector('.tl-chart__zoom-reset')!;
    expect(reset.querySelector('button')!.textContent).toContain('js.chart.resetZoom');

    const old: string[] = [];
    document.querySelectorAll('*').forEach(element => element.classList.forEach(name => {
      if (name.startsWith('tlReactChart')) old.push(name);
    }));
    expect(old).toEqual([]);
  });

  it('without zoom there is no reset button', async () => {
    mountAdapter(TLChart, { chartConfig: barChart(1), zoomEnabled: false });
    await settle();

    expect(document.querySelector('.tl-chart__zoom-reset')).toBeNull();
  });
});

describe('chart tooltip', () => {
  const content = { html: '<b>38</b> Aufträge', datasetIndex: 1, index: 2 };
  const position = { x: 40, y: 80, visible: true, datasetIndex: 1, index: 2 };

  it('has the surface of a popover, lies at the data point and names itself a tooltip', () => {
    render(<ChartTooltip content={content} position={position} />);

    const tooltip = document.querySelector('.tl-chart__tooltip') as HTMLElement;
    expect(tooltip).not.toBeNull();
    expect(tooltip.classList.contains('tl-popover')).toBe(true);
    expect(tooltip.classList.contains('tl-type-label')).toBe(true);
    expect(tooltip.getAttribute('role')).toBe('tooltip');
    expect(tooltip.style.left).toBe('40px');
    expect(tooltip.style.top).toBe('70px');
    expect(tooltip.innerHTML).toBe('<b>38</b> Aufträge');
  });

  it('waits for the content of the data point under the pointer', () => {
    render(<ChartTooltip content={{ ...content, index: 1 }} position={position} />);
    expect(document.querySelector('.tl-chart__tooltip')).toBeNull();
  });
});
