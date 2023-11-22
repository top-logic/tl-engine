import inherits from 'inherits';

import UpdateLabelHandler from '../label-editing/cmd/UpdateLabelHandler';
import LayoutConnectionLabelsHandler from './cmd/LayoutConnectionLabelsHandler';
import VisibilityHandler from './cmd/VisibilityHandler';

import { isShape, isLabel, isConnection } from '../../util/ModelUtil';

import BaseModeling from 'diagram-js/lib/features/modeling/Modeling';

import { assign } from 'min-dash';

inherits(Modeling, BaseModeling);

Modeling.$inject = [
  'eventBus',
  'elementFactory',
  'elementRegistry',
  'commandStack',
  'textRenderer',
  'canvas',
  'selection',
  'graphicsFactory',
  'layouter'
];

export default function Modeling(eventBus, elementFactory, elementRegistry, commandStack, textRenderer, canvas, selection, graphicsFactory, layouter) {
  this.canvas = canvas;
  this._textRenderer = textRenderer;
  this._graphicsFactory = graphicsFactory;
  this._elementRegistry = elementRegistry;
  this._selection = selection;
  this._layouter = layouter;

  BaseModeling.call(this, eventBus, elementFactory, commandStack);
}

Modeling.prototype.getHandlers = function() {
  var handlers = BaseModeling.prototype.getHandlers.call(this);

  handlers['element.updateLabel'] = UpdateLabelHandler;
  handlers['layout.connection.labels'] = LayoutConnectionLabelsHandler;
  handlers['element.visibility'] = VisibilityHandler;

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

Modeling.prototype.setVisibility = function(element, isVisible) {
  this._commandStack.execute('element.visibility', {
    element: element,
    visibility: isVisible
  });
};