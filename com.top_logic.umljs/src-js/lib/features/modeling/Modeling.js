import inherits from 'inherits';

import UpdateLabelHandler from '../label-editing/cmd/UpdateLabelHandler';
import LayoutConnectionLabelsHandler from './cmd/LayoutConnectionLabelsHandler';
import HideElementHandler from './cmd/HideElementHandler';

import BaseModeling from 'diagram-js/lib/features/modeling/Modeling';
import { Label, Shape, Connection } from 'diagram-js/lib/model';

import { assign } from 'min-dash';

inherits(Modeling, BaseModeling);

Modeling.$inject = [
  'eventBus',
  'elementFactory',
  'commandStack',
  'textRenderer',
  'canvas'
];

export default function Modeling(eventBus, elementFactory, commandStack, textRenderer, canvas) {
  this.canvas = canvas;
  this._textRenderer = textRenderer;

  BaseModeling.call(this, eventBus, elementFactory, commandStack);
}

Modeling.prototype.getHandlers = function() {
  var handlers = BaseModeling.prototype.getHandlers.call(this);

  handlers['element.updateLabel'] = UpdateLabelHandler;
  handlers['layout.connection.labels'] = LayoutConnectionLabelsHandler;
  handlers['element.hide'] = HideElementHandler;

  return handlers;
};

Modeling.prototype.updateLabel = function(element, newLabel) {
  this._commandStack.execute('element.updateLabel', {
    element: element,
    newLabel: newLabel
  });
};

Modeling.prototype.connect = function(source, target, connectionType, attrs, hints) {
  return this.createConnection(source, target, assign({
    connectionType: connectionType
  }, attrs), source.parent);
};

Modeling.prototype.hide = function(element) {
  var context = {
    element: element
  }

  this._commandStack.execute('element.hide', context);
};
