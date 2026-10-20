// The chart (spec 2026-10-20 wave 5d §8): its series take their colours from the categories of the theme, grid and
// labels from the border and text roles, and the chart reads them again when the mode switches. Chart.js paints onto a
// canvas, which jsdom cannot (no 2D context), so the colours are checked where the control computes them
// (useThemeDefaults, from custom properties at the document root, as textOn is checked) and the DOM around the canvas
// is checked through the control: status lines, the name of the canvas, the tooltip and the reset button.
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
import { CONTROL_ID, mountAdapter, settle } from './wire-test-support';

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
    expect(result.current.options.borderColor).toBe(COLORS.light.grid);
    expect(result.current.options.color).toBe(COLORS.light.text);
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
    expect(result.current.options.borderColor).toBe(COLORS.dark.grid);
    expect(result.current.options.color).toBe(COLORS.dark.text);

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

  it('colours of its own in the options of the server keep the grid and the labels', () => {
    const config = { ...barChart(1), options: { color: '#aabbcc', borderColor: '#ddeeff' } };
    const { result } = renderHook(() => useThemeDefaults(config));

    expect(result.current.options.color).toBe('#aabbcc');
    expect(result.current.options.borderColor).toBe('#ddeeff');
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

  it('without a design system no colour is made up', () => {
    document.documentElement.removeAttribute('style');
    const { result } = renderHook(() => useThemeDefaults(barChart(2)));

    expect(result.current.data.datasets[0].borderColor).toBeUndefined();
    expect(result.current.data.datasets[0].backgroundColor).toBeUndefined();
    expect(result.current.options.color).toBeUndefined();
  });

  it('a token written as rgb() or a short hex is read as a hex colour', () => {
    document.documentElement.style.setProperty('--tl-category-1-surface', 'rgb(18, 52, 86)');
    document.documentElement.style.setProperty('--tl-category-2-surface', '#abc');
    const { result } = renderHook(() => useThemeDefaults(barChart(2)));

    expect(result.current.data.datasets[0].borderColor).toBe('#123456');
    expect(result.current.data.datasets[1].borderColor).toBe('#aabbcc');
  });
});

describe('chart control', () => {
  /** Chart.js cannot draw into jsdom: the canvas answers no context, and the chart reports it on the console. */
  beforeEach(() => {
    vi.spyOn(HTMLCanvasElement.prototype, 'getContext').mockReturnValue(null);
    vi.spyOn(console, 'error').mockImplementation(() => {});
  });

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
