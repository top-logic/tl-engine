UpdateLabelHandler.$inject = [
  'modeling',
  'textRenderer'
];

export default function UpdateLabelHandler(modeling, textRenderer) {
  this._modeling = modeling;
  this._textRenderer = textRenderer;
}

UpdateLabelHandler.prototype.postExecute = function(context) {
  var newLabel = context.newLabel;
  var element = context.element;

  var dimensions = this._textRenderer.getDimensions(newLabel, {});

  if(element.type == "class") {
    element.businessObject.name = newLabel;
  } else {
    element.businessObject = newLabel;

    element.width = dimensions.width;
    element.height = dimensions.height;
  }

  this._modeling.resizeShape(element, {
    x: element.x,
    y: element.y,
    width: Math.max(element.width, 10),
    height: Math.max(element.height, 10)
  });
};
