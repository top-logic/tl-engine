import RuleProvider from 'diagram-js/lib/features/rules/RuleProvider';

import { Shape, Label, Root } from 'diagram-js/lib/model';

import inherits from 'inherits';

inherits(UmlRulesProvider, RuleProvider);

UmlRulesProvider.$inject = [ 'eventBus' ];

export default function UmlRulesProvider(eventBus) {
  RuleProvider.call(this, eventBus);
}

UmlRulesProvider.prototype.init = function() {
  this.addRule('shape.resize', function(context) {
    var shape = context.shape;

    return (shape instanceof Shape && !(shape instanceof Label));
  });

  this.addRule('elements.move', function(context) {
    var shapes = context.shapes;

    if(shapes.some(function(shape) {
      return shape instanceof Label;
    })) {
      return false;
    }

    if(context.target instanceof Label || (context.target instanceof Shape && !(context.target instanceof Root))) {
      return false;
    }

    return true;
  });
}
