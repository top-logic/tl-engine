// Chart.js control registration for the tl-react-chartjs bundle.
//
// IMPORTANT: All components MUST import React from 'tl-react-bridge' (not 'react').

import { register } from 'tl-react-bridge';
import TLChart from './controls/TLChart';

register('TLChart', TLChart);
