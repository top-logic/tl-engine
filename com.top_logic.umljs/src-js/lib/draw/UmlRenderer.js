import BaseRenderer from 'diagram-js/lib/draw/BaseRenderer';

import {
  drawClass,
  drawLabel
 } from './UmlShapeDrawer';

import {
  drawAssociation,
  drawInheritance,
  drawRealization,
  drawDependency,
  drawAggregation,
  drawComposition
} from './UmlConnectionDrawer';

import {
  initMarkerDefinitions
} from './UmlConnectionMarker';

import {
  getRectPath
} from './SVGDrawUtil'

import inherits from 'inherits';

inherits(UmlRenderer, BaseRenderer);

UmlRenderer.$inject = ['eventBus', 'canvas', 'textRenderer', 'layouter'];

export default function UmlRenderer(eventBus, canvas, textRenderer, layouter) {
  BaseRenderer.call(this, eventBus);

  this.textRenderer = textRenderer;
  this._layouter = layouter;

  this.drawShapeHandlers = {
    "class": drawClass,
    "label": drawLabel
  };

  this.drawConnectionHandlers = {
    "association": drawAssociation,
    "inheritance": drawInheritance,
    "realization": drawRealization,
    "dependency": drawDependency,
    "aggregation": drawAggregation,
    "composition": drawComposition
  };

  initMarkerDefinitions(canvas);
}

UmlRenderer.prototype.canRender = function(element) {
  return true;
};

UmlRenderer.prototype.drawShape = function(parentGfx, element) {
  var type = element.shapeType;
  var drawShapeHandler = this.drawShapeHandlers[type];

  return drawShapeHandler(parentGfx, element, this.textRenderer);
};

UmlRenderer.prototype.drawConnection = function(parentGfx, element) {
  var type = element.connectionType;
  var drawConnectionHandler = this.drawConnectionHandlers[type];

  return drawConnectionHandler(parentGfx, element);
};

UmlRenderer.prototype.getShapePath = function(element) {
    return getRectPath(element);
};
