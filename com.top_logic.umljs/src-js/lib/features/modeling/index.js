import CommandModule from 'diagram-js/lib/command';
import CroppingConnectionDocking from 'diagram-js/lib/layout/CroppingConnectionDocking';

import ElementFactory from './ElementFactory';
import UmlLayouter from './UmlLayouter';
import UmlUpdater from './UmlUpdater';
import Modeling from './Modeling';

export default {
  __init__: [
    'umlUpdater',
    'modeling'
  ],
  __depends__: [
    CommandModule
  ],
  connectionDocking: [ 'type', CroppingConnectionDocking ],
  elementFactory: [ 'type', ElementFactory ],
  modeling: [ 'type', Modeling ],
  umlUpdater: [ 'type', UmlUpdater ],
  layouter: [ 'type', UmlLayouter ]
};
