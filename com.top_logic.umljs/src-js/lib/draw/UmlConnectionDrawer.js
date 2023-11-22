import {
  drawPath,
  setVisibilityStyles
} from './SVGDrawUtil';

import {
  attr as svgAttr
} from 'tiny-svg';

export function drawAssociation(parentGfx, element) {
  var attributes = {
    stroke: 'black',
    markerEnd: 'association-marker'
  };

  return drawConnection(parentGfx, element, attributes);
}

export function drawInheritance(parentGfx, element) {
  var attributes = {
    stroke: 'gray',
    markerEnd: 'inheritance-marker'
  }

  return drawConnection(parentGfx, element, attributes);
}

export function drawRealization(parentGfx, element) {
  var attributes = {
    stroke: 'black',
    markerEnd: 'realization-marker',
    'stroke-dasharray': '5,10,5'
  }

  return drawConnection(parentGfx, element, attributes);
}

export function drawDependency(parentGfx, element) {
  var attributes = {
    stroke: 'black',
    markerEnd: 'dependency-marker',
    'stroke-dasharray': '5,10,5'
  }

  return drawConnection(parentGfx, element, attributes);
}

export function drawAggregation(parentGfx, element) {
  var attributes = {
    stroke: 'black',
    markerStart: 'aggregation-marker-reversed'
  }

  return drawConnection(parentGfx, element, attributes);
}

export function drawComposition(parentGfx, element) {
  var attributes = {
    stroke: 'black',
    markerStart: 'composition-marker-reversed'
  }

  return drawConnection(parentGfx, element, attributes);
}

function drawConnection(parentGfx, element, attributes) {
  var connection = drawPath(parentGfx, element.waypoints, attributes);
  
  setVisibilityStyles(parentGfx, element);
  
  return connection;
};
