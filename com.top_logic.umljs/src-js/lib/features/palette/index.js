import PaletteModule from 'diagram-js/lib/features/palette';
import LassoToolModule from 'diagram-js/lib/features/lasso-tool';

import CreateModule from '../create';

import PaletteProvider from './PaletteProvider';

export default {
  __depends__: [
    CreateModule,
    PaletteModule,
    LassoToolModule
  ],
  __init__: [ 'paletteProvider' ],
  paletteProvider: [ 'type', PaletteProvider ]
};
