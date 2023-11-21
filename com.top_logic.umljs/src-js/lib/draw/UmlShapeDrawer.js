import { drawRectangle, drawText, drawLine, updateVisibility } from './SVGDrawUtil';

import {
  append as svgAppend,
  attr as svgAttr,
  create as svgCreate,
  classes as svgClasses,
  innerSVG
} from 'tiny-svg';

import { assign } from 'min-dash';

var PADDING = 5;

export function drawClass(parentGfx, element, textRenderer) {
  var businessObject = element.businessObject || {};

  var rectangle = drawRectangle(parentGfx, element.width, element.height, {
    fill: 'none',
    stroke: 'black'
  });

  var centerLabelStyle = getCenterLabelStyle(element);
  centerLabelStyle.y = 5;

  if('stereotypes' in element) {
    element.stereotypes.forEach(function(stereotype) {
      var svgStereotype = drawText(parentGfx, '&lt;&lt;' + stereotype + '&gt;&gt;', centerLabelStyle, textRenderer);

      centerLabelStyle.y += svgStereotype.getBBox().height;
    });
  }

  if('name' in element) {
    var svgName = drawText(parentGfx, element.name, centerLabelStyle, textRenderer);

    centerLabelStyle.y += svgName.getBBox().height;
  }
  
  var hasAttributes = element.labels.some(function(label) {
    return label.labelType === 'property' || label.labelType === 'classifier';
  });

  if(hasAttributes) {
    if('name' in element) {
      var separator = drawClassSeparator(parentGfx, element.width, centerLabelStyle.y);
    }
  }
  
  updateVisibility(parentGfx, element);

  return rectangle;
};

export function drawLabel(parentGfx, element, textRenderer) {
  var text = drawText(parentGfx, element.businessObject + '', getGeneralLabelStyle(), textRenderer);
  
  updateVisibility(parentGfx, element);
  
  return text;
};

function drawClassSeparator(parentGfx, width, y) {
  return drawLine(parentGfx, {
    x: 0,
    y: y
  }, {
    x: width,
    y: y
  });
};

function getCenterLabelStyle(element) {
  return assign(getGeneralLabelStyle(), {
    x: element.width / 2,
    'text-anchor': 'middle',
  });
}

function getClassNameStyle(element) {
  var nameLabelStyle = getCenterLabelStyle(element);

  if('modifiers' in element) {
    if(element.modifiers.indexOf('abstract') != -1) {
      assign(nameLabelStyle, getAbstractClassLabelStyle());
    }
  }

  return nameLabelStyle;
};

function getGeneralLabelStyle() {
  return {
    dy: '.75em'
  };
};

function getAbstractClassLabelStyle() {
  return {
    'font-style': 'italic'
  };
};
