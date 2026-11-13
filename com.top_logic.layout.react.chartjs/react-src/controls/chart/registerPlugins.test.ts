import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { Chart as ChartJS } from 'chart.js';
import type { ChartConfiguration } from 'chart.js';
import './registerPlugins';

/** The texts drawn on the canvas of the chart under test. */
let drawnTexts: string[];

/**
 * A 2D context that draws nothing but records its texts: jsdom has no canvas implementation. Properties are kept,
 * every method is a no-op, `measureText` gives every text a width.
 */
function recordingContext(canvas: HTMLCanvasElement): CanvasRenderingContext2D {
  const fields: Record<string | symbol, unknown> = {
    canvas,
    fillText: (text: string) => { drawnTexts.push(String(text)); },
    measureText: (text: string) => ({ width: String(text).length * 6 }),
    getLineDash: () => [],
    getTransform: () => ({ a: 1, b: 0, c: 0, d: 1, e: 0, f: 0 }),
  };
  return new Proxy(fields, {
    get: (target, key) => (key in target ? target[key] : () => undefined),
    set: (target, key, value) => { target[key] = value; return true; },
  }) as unknown as CanvasRenderingContext2D;
}

let charts: ChartJS[];

beforeEach(() => {
  drawnTexts = [];
  charts = [];
  vi.spyOn(HTMLCanvasElement.prototype, 'getContext')
    .mockImplementation(function (this: HTMLCanvasElement) { return recordingContext(this); } as any);
});

afterEach(() => {
  charts.forEach(chart => chart.destroy());
  vi.restoreAllMocks();
  document.body.innerHTML = '';
});

/** Draws a scatter chart without legend, ticks and animation, so that the only texts on the canvas are point labels. */
function drawScatter(data: unknown[], datalabels?: Record<string, unknown>): void {
  const canvas = document.createElement('canvas');
  canvas.width = 400;
  canvas.height = 300;
  document.body.appendChild(canvas);
  const config = {
    type: 'scatter',
    data: { datasets: [{ data }] },
    options: {
      responsive: false,
      animation: false,
      scales: { x: { display: false }, y: { display: false } },
      plugins: { legend: { display: false }, ...(datalabels ? { datalabels } : {}) },
    },
  } as unknown as ChartConfiguration;
  charts.push(new ChartJS(canvas, config));
}

describe('labels of the data points', () => {
  it('are off by default', () => {
    expect(ChartJS.defaults.plugins.datalabels?.display).toBe(false);

    drawScatter([{ x: 3, y: 5, label: 'Projekt A' }, { x: 1, y: 2, label: 'Projekt B' }]);

    expect(drawnTexts).toEqual([]);
  });

  it('show the field label of a data point when turned on', () => {
    drawScatter([{ x: 3, y: 5, label: 'Projekt A' }, { x: 1, y: 2, label: 'Projekt B' }], { display: true });

    expect(drawnTexts).toContain('Projekt A');
    expect(drawnTexts).toContain('Projekt B');
  });

  it('show the value of a data point without the field label', () => {
    drawScatter([{ x: 3, y: 5 }], { display: true });

    expect(drawnTexts).toEqual(['x: 3, y: 5']);
  });

  it('show the value of a plain number', () => {
    const formatter = ChartJS.defaults.plugins.datalabels?.formatter as (value: unknown, context: unknown) => unknown;

    expect(formatter(7, {})).toBe('7');
    expect(formatter({ x: 3, y: 5, label: 'Projekt A' }, {})).toBe('Projekt A');
  });
});
