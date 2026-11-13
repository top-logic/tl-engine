// react-src/controls/chart/registerPlugins.ts
import { Chart as ChartJS, registerables } from 'chart.js';
import zoomPlugin from 'chartjs-plugin-zoom';
import ChartDataLabels from 'chartjs-plugin-datalabels';

/*
 * The chart types, elements and plugins every chart of the bundle is drawn with: all of Chart.js, zoom and pan, and
 * the labels of the data points.
 *
 * The labels of the data points (chartjs-plugin-datalabels) are off by default, so a chart is labelled only when its
 * configuration turns them on: `options.plugins.datalabels.display` for the whole chart, `datalabels.display` of a
 * dataset for its points. The text of a label is the field `label` of a data point that is an object
 * (`{x: 3, y: 5, label: 'A'}`), otherwise the value - the default formatter of the plugin.
 */
ChartJS.register(...registerables, zoomPlugin, ChartDataLabels);
ChartJS.defaults.set('plugins.datalabels', { display: false });
