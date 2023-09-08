import {
  append as svgAppend,
  attr as svgAttr,
  create as svgCreate,
  classes as svgClasses,
  innerSVG
} from 'tiny-svg';

import { getRelativeClosePoint } from './MathUtil';

import {
  componentsToPath
} from 'diagram-js/lib/util/RenderUtil';

export function drawText(parentGfx, text, options, textRenderer) {
  var svgText = textRenderer.createText(text, options);

  svgClasses(svgText).add('djs-label');
  svgAppend(parentGfx, svgText);

  return svgText;
}

export function drawLine(parentGfx, source, target) {
  var line = svgCreate('line');

  svgAttr(line, {
    x1: source.x,
    y1: source.y,
    x2: target.x,
    y2: target.y,
    stroke: 'black'
   });

  svgAppend(parentGfx, line);

  return line;
}

export function drawRectangle(parentGfx, width, height, attributes) {
  var rectangle = getRectangle(width, height, attributes);

  svgAppend(parentGfx, rectangle);

  return rectangle;
}

export function drawPath(parentGfx, waypoints, attributes) {
  var d = getPathData(waypoints);

  var backgroundPath = getBackgroundPath(getCroppedEndWaypoints(waypoints));
  var path = getPath(d, attributes);

  var group = getGroup([backgroundPath, path]);

  svgAppend(parentGfx, group);

  return group;
}

export function getPath(d, attributes) {
  var path = svgCreate('path');

  addPath(path, d);
  addPathMarkers(path, attributes);
  addPathTransformation(path, attributes);
  addStyles(path, attributes);

  return path;
};

function getBackgroundPath(waypoints) {
  var dBackground = getPathData(waypoints);

  return getPath(dBackground, { stroke: 'white', 'stroke-width': 10 });
};

function getCroppedEndWaypoints(waypoints) {
  var length = waypoints.length;
  var newWaypoints = waypoints.slice();

  if(length >= 2) {
    newWaypoints[0] = getRelativeClosePoint(waypoints[0], waypoints[1], 0.1);
    newWaypoints[length - 1] = getRelativeClosePoint(waypoints[length - 2], waypoints[length - 1], 0.9);
  }

  return newWaypoints;
};

function getPathData(waypoints) {
  var pathData = 'M ' + waypoints[0].x + ',' + waypoints[0].y + ' ';

  for (var i = 1; i < waypoints.length; i++) {
    pathData += 'L ' + waypoints[i].x + ',' + waypoints[i].y + ' ';
  }

  return pathData;
};

function getGroup(elements) {
    var group = svgCreate('g');

    elements.forEach(function(element) {
        svgAppend(group, element);
    });

    return group;
};

function addPath(path, d) {
  svgAttr(path, { d: d });
};

function addPathTransformation(path, attributes) {
  if('transform' in attributes) {
    svgAttr(path, { transform: attributes.transform });
  }
};

function addStyles(svgElement, attributes) {
  if('stroke' in attributes) {
    svgAttr(svgElement, { stroke: attributes.stroke });
  }

  if('stroke-width' in attributes) {
    svgAttr(svgElement, { 'stroke-width': attributes['stroke-width'] });
  }

  if('fill' in attributes) {
    svgAttr(svgElement, { fill: attributes.fill });
  }

  if('stroke-dasharray' in attributes) {
    svgAttr(svgElement, { 'stroke-dasharray': attributes['stroke-dasharray'] });
  }
};

function addPathMarkers(path, attributes) {
  if('markerEnd' in attributes) {
    svgAttr(path, { 'marker-end': 'url(#' + attributes.markerEnd + ')' });
  }

  if('markerStart' in attributes) {
    svgAttr(path, { 'marker-start': 'url(#' + attributes.markerStart + ')' });
  }
};

function getRectangle(width, height, attributes) {
  var rectangle = svgCreate('rect');

  addSize(rectangle, width, height);
  addStyles(rectangle, attributes);

  return rectangle;
};

function addSize(shape, width, height) {
  svgAttr(shape, {
    width: width,
    height: height
  });
};

export function getRectPath(shape) {
  var x = shape.x,
      y = shape.y,
      width = shape.width,
      height = shape.height;

  var rectPath = [
    ['M', x, y],
    ['l', width, 0],
    ['l', 0, height],
    ['l', -width, 0],
    ['z']
  ];

  return componentsToPath(rectPath);
}
