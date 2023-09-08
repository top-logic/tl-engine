import { Label, Shape } from 'diagram-js/lib/model';

LabelEditingProvider.$inject = [
  'eventBus',
  'elementRegistry',
  'directEditing',
  'resizeHandles',
  'textRenderer',
  'modeling',
  'canvas'
];

export default function LabelEditingProvider(eventBus, registry, directEditing, resizeHandles, textRenderer, modeling, canvas) {
  this.textRenderer = textRenderer;
  this.modeling = modeling;
  this._canvas = canvas;

  directEditing.registerProvider(this);

  eventBus.on('element.dblclick', function(event) {
    var element = event.element;

    if(element instanceof Shape) {
      directEditing.activate(element);
    }
  });

  eventBus.on([
    'element.mousedown',
    'drag.init',
    'canvas.viewbox.changing',
    'autoPlace',
    'popupMenu.open'
  ], function(event) {

    if (directEditing.isActive()) {
      directEditing.complete();
    }
  });

  eventBus.on([ 'commandStack.changed' ], function(e) {
    if (directEditing.isActive()) {
      directEditing.cancel();
    }
  });


  eventBus.on('directEditing.activate', function(event) {
    resizeHandles.removeResizers();
  });

  eventBus.on('create.end', 500, function(event) {
    var element = event.shape,
        canExecute = event.context.canExecute,
        isTouch = event.isTouch;

    if (isTouch) {
      return;
    }

    if (!canExecute) {
      return;
    }

    directEditing.activate(element);
  });
}

LabelEditingProvider.prototype.activate = function(element) {
  var text;

  if(element.type == "class") {
    text = element.businessObject.name || '';
  } else {
    text = element.businessObject || '';
  }

  var canvas = this._canvas;
  var bbox = canvas.getAbsoluteBBox(element);

  return {
    bounds: {
        x: bbox.x,
        y: bbox.y,
        width: Math.max(element.width, 200),
        height: Math.max(element.height, 100)
    },
    text: text,
    style: {
      fontSize: 12 + 'px',
      fontFamily: 'Monospace',
      fontWeight: 'normal',
      paddingTop: 5 + 'px'
    }
  }
};

LabelEditingProvider.prototype.update = function(element, newLabel) {
  this.modeling.updateLabel(element, newLabel.trim());
};
