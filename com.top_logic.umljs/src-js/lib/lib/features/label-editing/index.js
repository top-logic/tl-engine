import ChangeSupportModule from 'diagram-js/lib/features/change-support';
import ResizeModule from 'diagram-js/lib/features/resize';
import DirectEditingModule from 'diagram-js-direct-editing';

import LabelEditingProvider from './LabelEditingProvider';

export default {
  __depends__: [
    ChangeSupportModule,
    ResizeModule,
    DirectEditingModule
  ],
  __init__: [
    'labelEditingProvider',
  ],
  labelEditingProvider: [ 'type', LabelEditingProvider ]
};
