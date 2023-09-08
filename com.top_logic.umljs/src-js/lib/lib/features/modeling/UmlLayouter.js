import {
  getMid
} from 'diagram-js/lib/layout/LayoutUtil';

import { assign } from 'min-dash';

import inherits from 'inherits';

import BaseLayouter from 'diagram-js/lib/layout/BaseLayouter';

import {
  repairConnection,
  withoutRedundantPoints
} from 'diagram-js/lib/layout/ManhattanLayout';

inherits(UmlLayouter, BaseLayouter);

export default function UmlLayouter() {

}

UmlLayouter.prototype.layoutConnection = function(connection, hints) {
  hints = hints || {};

  var source = connection.source,
      target = connection.target,
      waypoints = connection.waypoints,
      start = hints.connectionStart,
      end = hints.connectionEnd;

  var manhattanOptions = hints;

  assign(manhattanOptions, {
    preferredLayouts: ['v:v']
  });

  var updatedWaypoints = withoutRedundantPoints(repairConnection(
      source, target,
      start, end,
      waypoints,
      manhattanOptions
    ));

  return updatedWaypoints;
};
