import inherits from 'inherits';

import { isShape, isLabel } from '../../../../util/ModelUtil';

HideElementHandler.$inject = [
  'graphicsFactory',
  'elementRegistry',
  'selection'
];

export default function HideElementHandler(graphicsFactory, elementRegistry, selection) {
  this._graphicsFactory = graphicsFactory;
  this._elementRegistry = elementRegistry;
  this._selection = selection;
};

HideElementHandler.prototype.execute = function(context) {
  var element = context.element;

  this.hide(element);

  this._selection.deselect(element);
}

HideElementHandler.prototype.hide = function(element) {
  element.hidden = true;

  if(isShape(element)) {
    if(isLabel(element)) {
      this.hideLabel(element);
    } else {
      this.hideShape(element);
    }
  } else {
    this.hideConnection(element);
  }
};

HideElementHandler.prototype.hideLabel = function(label) {
  this.hideChildren(label);

  this.updateGfx(label, 'shape');
};

HideElementHandler.prototype.hideConnection = function(connection) {
  this.hideChildren(connection);

  this.updateGfx(connection, 'connection');
};

HideElementHandler.prototype.hideShape = function(shape) {
  this.hideAttachedEdges(shape);
  this.hideChildren(shape);

  this.updateGfx(shape, 'shape');
};

HideElementHandler.prototype.hideAttachedEdges = function(shape) {
  this.hideIncomingAttachedEdges(shape);
  this.hideOutgoingAttachedEdges(shape);
};

HideElementHandler.prototype.hideIncomingAttachedEdges = function(shape) {
  var hideElementHandler = this;

  if('incoming' in shape) {
    shape.incoming.forEach(function(edge) {
      hideElementHandler.hide(edge);
    });
  }
};

HideElementHandler.prototype.hideOutgoingAttachedEdges = function(shape) {
  var hideElementHandler = this;

  if('outgoing' in shape) {
    shape.outgoing.forEach(function(edge) {
      hideElementHandler.hide(edge);
    });
  }
};

HideElementHandler.prototype.hideChildren = function(element) {
  var hideElementHandler = this;

  if('children' in element) {
    element.children.forEach(function(child) {
      hideElementHandler.hide(child);
    });
  }
};

HideElementHandler.prototype.updateGfx = function(element, type) {
  var gfx = this._elementRegistry.getGraphics(element, false);

  this._graphicsFactory.update(type, element, gfx);
};
