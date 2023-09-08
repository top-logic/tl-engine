import {
  append as svgAppend,
  attr as svgAttr,
  create as svgCreate
} from 'tiny-svg';

import { query as domQuery } from 'min-dom';

import { getPath } from './SVGDrawUtil';

var associationMarker = {
  name: 'association-marker',
  element: getPath('M 0,0 L 10,5 L 0,10', { stroke: 'black', fill: 'none' })
};

var inheritanceMarker = {
  name: 'inheritance-marker',
  element: getPath('M 0,0 L 10,5 L 0,10 Z', { stroke: 'black', fill: 'white' })
};

var realizationMarker = {
  name: 'realization-marker',
  element: getPath('M 0,0 L 10,5 L 0,10 Z', { stroke: 'black', fill: 'white' })
};

var dependencyMarker = {
  name: 'dependency-marker',
  element: getPath('M 0,0 L 10,5 L 0,10', { stroke: 'black', fill: 'none' })
};

var aggregationMarker = {
  name: 'aggregation-marker',
  element: getPath('M 0,5 L 5,0 L 10,5 L 5,10 Z', { stroke: 'black', fill: 'white', transform: 'rotate(180,5,5)' })
};

var compositionMarker = {
  name: 'composition-marker',
  element: getPath('M 0,5 L 5,0 L 10,5 L 5,10 Z', { stroke: 'black', fill: 'black', transform: 'rotate(180,5,5)' })
};

var markers = [
  associationMarker,
  inheritanceMarker,
  realizationMarker,
  dependencyMarker,
  aggregationMarker,
  compositionMarker
];

export function initMarkerDefinitions(canvas) {
  var defs = domQuery('defs', canvas._svg);

  if (!defs) {
    defs = svgCreate('defs');

    svgAppend(canvas._svg, defs);
  }

  markers.forEach(function(marker) {
    var markerCopy = marker.element.cloneNode(true);

    addMarkerDefinition(defs, marker.name, marker.element, false);
    addMarkerDefinition(defs, marker.name + '-reversed', markerCopy, true);
  });
}

function addMarkerDefinition(definitions, id, markerElement, atStart) {
  var svgMarker = svgCreate('marker');

  svgAppend(svgMarker, markerElement);

  svgAttr(svgMarker, {
    id: id,
    viewBox: '0 0 10 10',
    refX: getMarkerRefX(atStart),
    refY: 5,
    markerWidth: 5,
    markerHeight: 5,
    orient: 'auto'
  });

  svgAppend(definitions, svgMarker);
};

function getMarkerRefX(atStart) {
  if(atStart) {
    return 0;
  } else {
    return 10;
  }
}
