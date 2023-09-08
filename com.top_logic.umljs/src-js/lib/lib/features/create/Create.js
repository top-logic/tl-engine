var LOW_PRIORITY = 750;

var MARKER_OK = 'drop-ok',
    MARKER_NOT_OK = 'drop-not-ok',
    MARKER_ATTACH = 'attach-ok',
    MARKER_NEW_PARENT = 'new-parent';

import {
  append as svgAppend,
  attr as svgAttr,
  classes as svgClasses,
  create as svgCreate,
  remove as svgRemove
} from 'tiny-svg';

import {
  translate
} from 'diagram-js/lib/util/SvgTransformUtil';

export default function Create(
    eventBus, dragging, modeling,
    canvas, styles, graphicsFactory) {

  /** set drop marker on an element */
  function setMarker(element, marker) {

    [ MARKER_ATTACH, MARKER_OK, MARKER_NOT_OK, MARKER_NEW_PARENT ].forEach(function(m) {

      if (m === marker) {
        canvas.addMarker(element, m);
      } else {
        canvas.removeMarker(element, m);
      }
    });
  }

  function createVisual(shape) {
    var group, preview, visual;

    group = svgCreate('g');
    svgAttr(group, styles.cls('djs-drag-group', [ 'no-events' ]));

    svgAppend(canvas.getDefaultLayer(), group);

    preview = svgCreate('g');
    svgClasses(preview).add('djs-dragger');

    svgAppend(group, preview);

    translate(preview, shape.width / -2, shape.height / -2);

    var visualGroup = svgCreate('g');
    svgClasses(visualGroup).add('djs-visual');

    svgAppend(preview, visualGroup);

    visual = visualGroup;

    // hijack renderer to draw preview
    graphicsFactory.drawShape(visual, shape);

    return group;
  }

  eventBus.on('create.move', function(event) {

    var context = event.context,
        hover = event.hover,
        shape = context.shape,
        canExecute;

    var position = {
      x: event.x,
      y: event.y
    };

    canExecute = context.canExecute = hover;

    // ignore hover visually if canExecute is null
    if (hover && canExecute !== null) {
      context.target = hover;

      if (canExecute && canExecute.attach) {
        setMarker(hover, MARKER_ATTACH);
      } else {
        setMarker(hover, canExecute ? MARKER_NEW_PARENT : MARKER_NOT_OK);
      }
    }
  });

  eventBus.on('create.move', LOW_PRIORITY, function(event) {

    var context = event.context,
        shape = context.shape,
        visual = context.visual;

    // lazy init drag visual once we received the first real
    // drag move event (this allows us to get the proper canvas local coordinates)
    if (!visual) {
      visual = context.visual = createVisual(shape);
    }

    translate(visual, event.x, event.y);
  });


  eventBus.on([ 'create.end', 'create.out', 'create.cleanup' ], function(event) {
    var context = event.context,
        target = context.target;

    if (target) {
      setMarker(target, null);
    }
  });

  eventBus.on('create.ended', function(event) {
    eventBus.fire('create' + '.' + event.context.type, {
      context: {
        bounds: {
          x: event.x,
          y: event.y,
          width: event.shape.width,
          height: event.shape.height
        }
      }
    });
  });

  eventBus.on('create.end', function(event) {
    return true;
  });


  eventBus.on('create.cleanup', function(event) {
    var context = event.context;

    if (context.visual) {
      svgRemove(context.visual);
    }
  });

  // API
  this.start = function(event, shape, type) {

    dragging.init(event, 'create', {
      cursor: 'grabbing',
      autoActivate: true,
      data: {
        shape: shape,
        context: {
          shape: shape,
          type: type
        }
      }
    });
  };
}

Create.$inject = [
  'eventBus',
  'dragging',
  'modeling',
  'canvas',
  'styles',
  'graphicsFactory'
];
