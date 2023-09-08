import SelectionModule from 'diagram-js/lib/features/selection';
import RulesModule from 'diagram-js/lib/features/rules';
import DraggingModule from 'diagram-js/lib/features/dragging';

import Connect from './Connect';

export default {
  __depends__: [
    SelectionModule,
    RulesModule,
    DraggingModule
  ],
  connect: [ 'type', Connect ]
};
