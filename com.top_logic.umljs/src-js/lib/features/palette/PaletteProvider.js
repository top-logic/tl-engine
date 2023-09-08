PaletteProvider.$inject = [
  'create',
  'elementFactory',
  'lassoTool',
  'palette',
  'textRenderer',
  'eventBus'
];

export default function PaletteProvider(create, elementFactory, lassoTool, palette, textRenderer, eventBus) {
  this._create = create;
  this._elementFactory = elementFactory;
  this._lassoTool = lassoTool;
  this._palette = palette;
  this._textRenderer = textRenderer;
  this._eventBus = eventBus;

  palette.registerProvider(this);
}

PaletteProvider.prototype.getPaletteEntries = function() {
  var create = this._create,
      elementFactory = this._elementFactory,
      lassoTool = this._lassoTool,
      textRenderer = this._textRenderer,
      eventBus = this._eventBus;

  function createShapeAction(type, group, className, title) {
    function createListener(event) {
      var shape = elementFactory.createClass({
        width: 200,
        height: 100,
        name: "Name"
      });

      create.start(event, shape, type);
    };

    return {
      group: group,
      className: className,
      title: title,
      action: {
        dragstart: createListener,
        click: createListener
      }
    };
  };

  return {
    'lasso-tool': {
      group: 'tools',
      className: 'palette-icon-lasso-tool',
      title: 'Activate Lasso Tool',
      action: {
        click: function(event) {
          lassoTool.activateSelection(event);
        }
      }
    },
    'tool-separator': {
      group: 'tools',
      separator: true
    },
    'create-class': createShapeAction('class', 'create', 'palette-icon-create-class', 'Create Class'),
    'create-enumeration': createShapeAction('enumeration', 'create', 'palette-icon-create-enumeration', 'Create Enumeration')
  };
};
