import RuleProvider from 'diagram-js/lib/features/rules/RuleProvider';

import { isShape, isLabel, isRoot } from '../../util/ModelUtil';

import inherits from 'inherits';

inherits(UmlRulesProvider, RuleProvider);

UmlRulesProvider.$inject = [ 'eventBus' ];

export default function UmlRulesProvider(eventBus) {
  RuleProvider.call(this, eventBus);
}

UmlRulesProvider.prototype.init = function() {
  this.addRule('shape.resize', function(context) {
    var shape = context.shape;

    return (isShape(shape) && !isLabel(shape));
  });

  this.addRule('elements.move', function(context) {
    var shapes = context.shapes;

    if(shapes.some(function(shape) {
      return isLabel(shape);
    })) {
      return false;
    }

    if(isLabel(context.target) || (isShape(context.target) && !isRoot(context.target))) {
      return false;
    }

    return true;
  });
}
