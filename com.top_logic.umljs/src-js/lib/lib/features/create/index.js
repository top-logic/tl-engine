import DraggingModule from 'diagram-js/lib/features/dragging';

import Create from './Create';


export default {
  __depends__: [
    DraggingModule
  ],
  create: [ 'type', Create ]
};
