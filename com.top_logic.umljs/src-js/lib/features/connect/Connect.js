import {
  getElementLineIntersection
} from 'diagram-js/lib/layout/LayoutUtil';

import {
  getMid
} from 'diagram-js/lib/layout/LayoutUtil';

var MARKER_OK = 'connect-ok',
    MARKER_NOT_OK = 'connect-not-ok';

import {
  append as svgAppend,
  attr as svgAttr,
  create as svgCreate,
  remove as svgRemove
} from 'tiny-svg';


export default function Connect(
    eventBus, dragging, modeling,
    rules, canvas, graphicsFactory) {

    function canConnect(source, target) {
      return true;
    }

  function crop(start, end, source, target) {

    var sourcePath = graphicsFactory.getShapePath(source),
        targetPath = target && graphicsFactory.getShapePath(target),
        connectionPath = graphicsFactory.getConnectionPath({ waypoints: [ start, end ] });

    start = getElementLineIntersection(sourcePath, connectionPath, true) || start;
    end = (target && getElementLineIntersection(targetPath, connectionPath, false)) || end;

    return [ start, end ];
  }


  // event handlers

  eventBus.on('connect.move', function(event) {

    var context = event.context,
        source = context.source,
        target = context.target,
        visual = context.visual,
        sourcePosition = context.sourcePosition,
        endPosition,
        waypoints;

    // update connection visuals during drag

    endPosition = {
      x: event.x,
      y: event.y
    };

    waypoints = crop(sourcePosition, endPosition, source, target);

    svgAttr(visual, { 'points': [ waypoints[0].x, waypoints[0].y, waypoints[1].x, waypoints[1].y ] });
  });

  eventBus.on('connect.hover', function(event) {
    var context = event.context,
        source = context.source,
        hover = event.hover,
        canExecute;

    canExecute = context.canExecute = canConnect(source, hover);

    // simply ignore hover
    if (canExecute === null) {
      return;
    }

    context.target = hover;

    canvas.addMarker(hover, canExecute ? MARKER_OK : MARKER_NOT_OK);
  });

  eventBus.on([ 'connect.out', 'connect.cleanup' ], function(event) {
    var context = event.context;

    if (context.target) {
      canvas.removeMarker(context.target, context.canExecute ? MARKER_OK : MARKER_NOT_OK);
    }

    context.target = null;
    context.canExecute = false;
  });

  eventBus.on('connect.cleanup', function(event) {
    var context = event.context;

    if (context.visual) {
      svgRemove(context.visual);
    }
  });

  eventBus.on('connect.start', function(event) {
    var context = event.context,
        visual;

    visual = svgCreate('polyline');
    svgAttr(visual, {
      'stroke': '#333',
      'strokeDasharray': [ 1 ],
      'strokeWidth': 2,
      'pointer-events': 'none'
    });

    svgAppend(canvas.getDefaultLayer(), visual);

    context.visual = visual;
  });

  eventBus.on('connect.end', function(event) {

    var context = event.context,
        source = context.source,
        type = context.type,
        sourcePosition = context.sourcePosition,
        target = context.target,
        targetPosition = {
          x: event.x,
          y: event.y
        },
        canExecute = context.canExecute || canConnect(source, target);

    if (!canExecute) {
      return false;
    }

    var attrs = null,
        hints = {
          connectionStart: sourcePosition,
          connectionEnd: targetPosition
        };

    if (typeof canExecute === 'object') {
      attrs = canExecute;
    }
  });

  this.start = function(event, source, type, sourcePosition, autoActivate) {

    if (typeof sourcePosition !== 'object') {
      autoActivate = sourcePosition;
      sourcePosition = getMid(source);
    }

    dragging.init(event, 'connect', {
      autoActivate: autoActivate,
      data: {
        shape: source,
        context: {
          type: type,
          source: source,
          sourcePosition: sourcePosition
        }
      }
    });
  };
}

Connect.$inject = [
  'eventBus',
  'dragging',
  'modeling',
  'rules',
  'canvas',
  'graphicsFactory'
];
