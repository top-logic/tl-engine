import RulesModule from 'diagram-js/lib/features/rules';
import DraggingModule from 'diagram-js/lib/features/dragging';
// import SelectionModule from 'diagram-js/lib/features/selection';

import Create from './Create';


export default {
  __depends__: [
    DraggingModule
  ],
  create: [ 'type', Create ]
};
