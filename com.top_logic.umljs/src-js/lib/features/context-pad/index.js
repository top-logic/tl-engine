import ContextPadModule from 'diagram-js/lib/features/context-pad';
import ModelingModule from 'diagram-js/lib/features/modeling';

import ConnectModule from '../connect';

import ContextPadProvider from './ContextPadProvider';

export default {
  __depends__: [
    ContextPadModule,
    ConnectModule,
    ModelingModule
  ],
  __init__: [ 'contextPadProvider' ],
  contextPadProvider: [ 'type', ContextPadProvider ]
};
