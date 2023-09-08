import { domify, query as domQuery } from 'min-dom';

import ZoomScrollModule from 'diagram-js/lib/navigation/zoomscroll';
import MoveCanvasModule from 'diagram-js/lib/navigation/movecanvas';

import CoreModule from './core';

import Diagram from 'diagram-js';

import { assign } from 'min-dash';

import inherits from 'inherits';

inherits(Viewer, Diagram);

export default function Viewer(options) {
  this._container = this._createContainer(options);

  this._init(this._container, options);
}

Viewer.prototype._coreModules = [
  CoreModule
];

Viewer.prototype._viewModules = [
  ZoomScrollModule,
  MoveCanvasModule
];

Viewer.prototype._modules = [].concat(
  Viewer.prototype._coreModules,
  Viewer.prototype._viewModules
);

Viewer.prototype.importJSON = function(graphData) {
  this.get('umlImporter').import(graphData);
};

Viewer.prototype._createContainer = function(options) {
  var container;

  if('containerID' in options) {
    container = domify('<div id="' + options.containerID + '"class="umljs-container"></div>');
  } else {
    container = domify('<div class="umljs-container"></div>');
  }

  assign(container.style, {
    width: '100%',
    height: '100%',
    position: 'relative'
  });

  return container;
};

Viewer.prototype._init = function(container, options) {
  var diagramOptions = {
    canvas: { container: container },
    modules: this._modules
  };

  Diagram.call(this, diagramOptions);

  if(options && options.parent) {
    this.attachTo(options.parent);
  }
};

Viewer.prototype.attachTo = function(parentNode) {
  if (!parentNode) {
    throw new Error('parentNode required');
  }

  // ensure we detach from the
  // previous, old parent
  this.detach();

  // unwrap jQuery if provided
  if (parentNode.get && parentNode.constructor.prototype.jquery) {
    parentNode = parentNode.get(0);
  }

  if (typeof parentNode === 'string') {
    parentNode = domQuery(parentNode);
  }
  parentNode.appendChild(this._container);

  this._emit('attach', {});

  this.get('canvas').resized();
};

Viewer.prototype.detach = function() {

  var container = this._container,
      parentNode = container.parentNode;

  if (!parentNode) {
    return;
  }

  this._emit('detach', {});

  parentNode.removeChild(container);
};

Viewer.prototype._emit = function(type, event) {
  return this.get('eventBus').fire(type, event);
};
