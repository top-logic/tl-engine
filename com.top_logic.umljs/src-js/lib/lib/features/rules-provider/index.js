import RulesModule from 'diagram-js/lib/features/rules';

import UmlRulesProvider from './UmlRulesProvider';

export default {
  __depends__: [
    RulesModule
  ],
  __init__: [ 'umlRulesProvider' ],
  umlRulesProvider: [ 'type', UmlRulesProvider ]
};
