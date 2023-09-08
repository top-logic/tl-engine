import {
  append as svgAppend,
  attr as svgAttr,
  create as svgCreate,
  classes as svgClasses,
  remove as svgRemove,
  innerSVG
} from 'tiny-svg';

import { assign } from 'min-dash';

export default function TextRenderer() {
  this._style = {
    fontFamily: 'DroidSansMono',
    fontSize: 12
  };
}

TextRenderer.prototype.createText = function(text, options) {
  var svgText = svgCreate('text');

  var options = assign(options, this._style);

  svgAttr(svgText, options);

  innerSVG(svgText, text);

  return svgText;
};

TextRenderer.prototype.getDimensions  = function(text, options) {
    var helperText = svgCreate('text');

    svgAttr(helperText, this._style);

    var helperSvg = getHelperSvg();

    svgAppend(helperSvg, helperText);

    helperText.textContent = text;
    var bbox = helperText.getBBox();

    svgRemove(helperText);

    return bbox;
};

TextRenderer.prototype.getWidth = function(text, options) {
  return this.getDimensions(text, options).width;
};

TextRenderer.prototype.getHeight = function(text, options) {
  return this.getDimensions(text, options).height;
};

function getHelperSvg() {
  var helperSvg = document.getElementById('helper-svg');

  if (!helperSvg) {
    return createHelperSvg();
  }

  return helperSvg;
};

function createHelperSvg() {
  var helperSvg = svgCreate('svg');

  svgAttr(helperSvg, getHelperSvgOptions());

  document.body.appendChild(helperSvg);

  return helperSvg;
};

function getHelperSvgOptions() {
  return {
    id: 'helper-svg',
    width: 0,
    height: 0,
    style: 'visibility: hidden; position: fixed'
  };
}
