/*!
 * uml-js - uml-viewer v1.0.0
 *
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 *
 * Date: 2022-05-09
 */

(function (global, factory) {
  typeof exports === 'object' && typeof module !== 'undefined' ? module.exports = factory() :
  typeof define === 'function' && define.amd ? define(factory) :
  (global.UmlJS = factory());
}(this, (function () { 'use strict';

  /**
   * Set attribute `name` to `val`, or get attr `name`.
   *
   * @param {Element} el
   * @param {String} name
   * @param {String} [val]
   * @api public
   */

  var indexOf = [].indexOf;

  var indexof = function(arr, obj){
    if (indexOf) return arr.indexOf(obj);
    for (var i = 0; i < arr.length; ++i) {
      if (arr[i] === obj) return i;
    }
    return -1;
  };

  /**
   * Taken from https://github.com/component/classes
   *
   * Without the component bits.
   */

  /**
   * Whitespace regexp.
   */

  var re = /\s+/;

  /**
   * toString reference.
   */

  var toString = Object.prototype.toString;

  /**
   * Wrap `el` in a `ClassList`.
   *
   * @param {Element} el
   * @return {ClassList}
   * @api public
   */

  function classes(el) {
    return new ClassList(el);
  }

  /**
   * Initialize a new ClassList for `el`.
   *
   * @param {Element} el
   * @api private
   */

  function ClassList(el) {
    if (!el || !el.nodeType) {
      throw new Error('A DOM element reference is required');
    }
    this.el = el;
    this.list = el.classList;
  }

  /**
   * Add class `name` if not already present.
   *
   * @param {String} name
   * @return {ClassList}
   * @api public
   */

  ClassList.prototype.add = function (name) {
    // classList
    if (this.list) {
      this.list.add(name);
      return this;
    }

    // fallback
    var arr = this.array();
    var i = indexof(arr, name);
    if (!~i) arr.push(name);
    this.el.className = arr.join(' ');
    return this;
  };

  /**
   * Remove class `name` when present, or
   * pass a regular expression to remove
   * any which match.
   *
   * @param {String|RegExp} name
   * @return {ClassList}
   * @api public
   */

  ClassList.prototype.remove = function (name) {
    if ('[object RegExp]' == toString.call(name)) {
      return this.removeMatching(name);
    }

    // classList
    if (this.list) {
      this.list.remove(name);
      return this;
    }

    // fallback
    var arr = this.array();
    var i = indexof(arr, name);
    if (~i) arr.splice(i, 1);
    this.el.className = arr.join(' ');
    return this;
  };

  /**
   * Remove all classes matching `re`.
   *
   * @param {RegExp} re
   * @return {ClassList}
   * @api private
   */

  ClassList.prototype.removeMatching = function (re) {
    var arr = this.array();
    for (var i = 0; i < arr.length; i++) {
      if (re.test(arr[i])) {
        this.remove(arr[i]);
      }
    }
    return this;
  };

  /**
   * Toggle class `name`, can force state via `force`.
   *
   * For browsers that support classList, but do not support `force` yet,
   * the mistake will be detected and corrected.
   *
   * @param {String} name
   * @param {Boolean} force
   * @return {ClassList}
   * @api public
   */

  ClassList.prototype.toggle = function (name, force) {
    // classList
    if (this.list) {
      if ('undefined' !== typeof force) {
        if (force !== this.list.toggle(name, force)) {
          this.list.toggle(name); // toggle again to correct
        }
      } else {
        this.list.toggle(name);
      }
      return this;
    }

    // fallback
    if ('undefined' !== typeof force) {
      if (!force) {
        this.remove(name);
      } else {
        this.add(name);
      }
    } else {
      if (this.has(name)) {
        this.remove(name);
      } else {
        this.add(name);
      }
    }

    return this;
  };

  /**
   * Return an array of classes.
   *
   * @return {Array}
   * @api public
   */

  ClassList.prototype.array = function () {
    var className = this.el.getAttribute('class') || '';
    var str = className.replace(/^\s+|\s+$/g, '');
    var arr = str.split(re);
    if ('' === arr[0]) arr.shift();
    return arr;
  };

  /**
   * Check if class `name` is present.
   *
   * @param {String} name
   * @return {ClassList}
   * @api public
   */

  ClassList.prototype.has = ClassList.prototype.contains = function (name) {
    return this.list ? this.list.contains(name) : !!~indexof(this.array(), name);
  };

  /**
   * Remove all children from the given element.
   */
  function clear(el) {

    var c;

    while (el.childNodes.length) {
      c = el.childNodes[0];
      el.removeChild(c);
    }

    return el;
  }

  /**
   * Element prototype.
   */

  var proto = Element.prototype;

  /**
   * Vendor function.
   */

  var vendor = proto.matchesSelector
    || proto.webkitMatchesSelector
    || proto.mozMatchesSelector
    || proto.msMatchesSelector
    || proto.oMatchesSelector;

  /**
   * Expose `match()`.
   */

  var matchesSelector = match;

  /**
   * Match `el` to `selector`.
   *
   * @param {Element} el
   * @param {String} selector
   * @return {Boolean}
   * @api public
   */

  function match(el, selector) {
    if (vendor) return vendor.call(el, selector);
    var nodes = el.parentNode.querySelectorAll(selector);
    for (var i = 0; i < nodes.length; ++i) {
      if (nodes[i] == el) return true;
    }
    return false;
  }

  var closest = function (element, selector, checkYoSelf) {
    var parent = checkYoSelf ? element : element.parentNode;

    while (parent && parent !== document) {
      if (matchesSelector(parent, selector)) return parent;
      parent = parent.parentNode;
    }
  };

  var bind = window.addEventListener ? 'addEventListener' : 'attachEvent',
      unbind = window.removeEventListener ? 'removeEventListener' : 'detachEvent',
      prefix = bind !== 'addEventListener' ? 'on' : '';

  /**
   * Bind `el` event `type` to `fn`.
   *
   * @param {Element} el
   * @param {String} type
   * @param {Function} fn
   * @param {Boolean} capture
   * @return {Function}
   * @api public
   */

  var bind_1 = function(el, type, fn, capture){
    el[bind](prefix + type, fn, capture || false);
    return fn;
  };

  /**
   * Unbind `el` event `type`'s callback `fn`.
   *
   * @param {Element} el
   * @param {String} type
   * @param {Function} fn
   * @param {Boolean} capture
   * @return {Function}
   * @api public
   */

  var unbind_1 = function(el, type, fn, capture){
    el[unbind](prefix + type, fn, capture || false);
    return fn;
  };

  var componentEvent = {
  	bind: bind_1,
  	unbind: unbind_1
  };

  /**
   * Expose `parse`.
   */

  var domify = parse;

  /**
   * Tests for browser support.
   */

  var innerHTMLBug = false;
  var bugTestDiv;
  if (typeof document !== 'undefined') {
    bugTestDiv = document.createElement('div');
    // Setup
    bugTestDiv.innerHTML = '  <link/><table></table><a href="/a">a</a><input type="checkbox"/>';
    // Make sure that link elements get serialized correctly by innerHTML
    // This requires a wrapper element in IE
    innerHTMLBug = !bugTestDiv.getElementsByTagName('link').length;
    bugTestDiv = undefined;
  }

  /**
   * Wrap map from jquery.
   */

  var map = {
    legend: [1, '<fieldset>', '</fieldset>'],
    tr: [2, '<table><tbody>', '</tbody></table>'],
    col: [2, '<table><tbody></tbody><colgroup>', '</colgroup></table>'],
    // for script/link/style tags to work in IE6-8, you have to wrap
    // in a div with a non-whitespace character in front, ha!
    _default: innerHTMLBug ? [1, 'X<div>', '</div>'] : [0, '', '']
  };

  map.td =
  map.th = [3, '<table><tbody><tr>', '</tr></tbody></table>'];

  map.option =
  map.optgroup = [1, '<select multiple="multiple">', '</select>'];

  map.thead =
  map.tbody =
  map.colgroup =
  map.caption =
  map.tfoot = [1, '<table>', '</table>'];

  map.polyline =
  map.ellipse =
  map.polygon =
  map.circle =
  map.text =
  map.line =
  map.path =
  map.rect =
  map.g = [1, '<svg xmlns="http://www.w3.org/2000/svg" version="1.1">','</svg>'];

  /**
   * Parse `html` and return a DOM Node instance, which could be a TextNode,
   * HTML DOM Node of some kind (<div> for example), or a DocumentFragment
   * instance, depending on the contents of the `html` string.
   *
   * @param {String} html - HTML string to "domify"
   * @param {Document} doc - The `document` instance to create the Node for
   * @return {DOMNode} the TextNode, DOM Node, or DocumentFragment instance
   * @api private
   */

  function parse(html, doc) {
    if ('string' != typeof html) throw new TypeError('String expected');

    // default to the global `document` object
    if (!doc) doc = document;

    // tag name
    var m = /<([\w:]+)/.exec(html);
    if (!m) return doc.createTextNode(html);

    html = html.replace(/^\s+|\s+$/g, ''); // Remove leading/trailing whitespace

    var tag = m[1];

    // body support
    if (tag == 'body') {
      var el = doc.createElement('html');
      el.innerHTML = html;
      return el.removeChild(el.lastChild);
    }

    // wrap map
    var wrap = map[tag] || map._default;
    var depth = wrap[0];
    var prefix = wrap[1];
    var suffix = wrap[2];
    var el = doc.createElement('div');
    el.innerHTML = prefix + html + suffix;
    while (depth--) el = el.lastChild;

    // one element
    if (el.firstChild == el.lastChild) {
      return el.removeChild(el.firstChild);
    }

    // several elements
    var fragment = doc.createDocumentFragment();
    while (el.firstChild) {
      fragment.appendChild(el.removeChild(el.firstChild));
    }

    return fragment;
  }

  var proto$1 = typeof Element !== 'undefined' ? Element.prototype : {};
  var vendor$1 = proto$1.matches
    || proto$1.matchesSelector
    || proto$1.webkitMatchesSelector
    || proto$1.mozMatchesSelector
    || proto$1.msMatchesSelector
    || proto$1.oMatchesSelector;

  function query(selector, el) {
    el = el || document;

    return el.querySelector(selector);
  }

  function delta(a, b) {
    return {
      x: a.x - b.x,
      y: a.y - b.y
    };
  }

  /**
   * Get the logarithm of x with base 10
   * @param  {Integer} value
   */
  function log10(x) {
    return Math.log(x) / Math.log(10);
  }

  /**
   * Get step size for given range and number of steps.
   *
   * @param {Object} range - Range.
   * @param {number} range.min - Range minimum.
   * @param {number} range.max - Range maximum.
   */
  function getStepSize(range, steps) {

    var minLinearRange = log10(range.min),
        maxLinearRange = log10(range.max);

    var absoluteLinearRange = Math.abs(minLinearRange) + Math.abs(maxLinearRange);

    return absoluteLinearRange / steps;
  }

  function cap(range, scale) {
    return Math.max(range.min, Math.min(range.max, scale));
  }

  /**
   * Flatten array, one level deep.
   *
   * @param {Array<?>} arr
   *
   * @return {Array<?>}
   */

  var nativeToString = Object.prototype.toString;
  var nativeHasOwnProperty = Object.prototype.hasOwnProperty;

  function isUndefined(obj) {
    return obj === undefined;
  }

  function isArray(obj) {
    return nativeToString.call(obj) === '[object Array]';
  }

  function isObject(obj) {
    return nativeToString.call(obj) === '[object Object]';
  }

  function isNumber(obj) {
    return nativeToString.call(obj) === '[object Number]';
  }

  function isFunction(obj) {
    return nativeToString.call(obj) === '[object Function]';
  }

  /**
   * Return true, if target owns a property with the given key.
   *
   * @param {Object} target
   * @param {String} key
   *
   * @return {Boolean}
   */
  function has(target, key) {
    return nativeHasOwnProperty.call(target, key);
  }

  /**
   * Iterate over collection; returning something
   * (non-undefined) will stop iteration.
   *
   * @param  {Array|Object} collection
   * @param  {Function} iterator
   *
   * @return {Object} return result that stopped the iteration
   */
  function forEach(collection, iterator) {

    if (isUndefined(collection)) {
      return;
    }

    var convertKey = isArray(collection) ? toNum : identity;

    for (var key in collection) {

      if (has(collection, key)) {
        var val = collection[key];

        var result = iterator(val, convertKey(key));

        if (result === false) {
          return;
        }
      }
    }
  }

  /**
   * Reduce collection, returning a single result.
   *
   * @param  {Object|Array} collection
   * @param  {Function} iterator
   * @param  {Any} result
   *
   * @return {Any} result returned from last iterator
   */
  function reduce(collection, iterator, result) {

    forEach(collection, function (value, idx) {
      result = iterator(result, value, idx);
    });

    return result;
  }

  /**
   * Return true if every element in the collection
   * matches the criteria.
   *
   * @param  {Object|Array} collection
   * @param  {Function} matcher
   *
   * @return {Boolean}
   */
  function every(collection, matcher) {

    return reduce(collection, function (matches, val, key) {
      return matches && matcher(val, key);
    }, true);
  }

  function identity(arg) {
    return arg;
  }

  function toNum(arg) {
    return Number(arg);
  }

  /**
   * Debounce fn, calling it only once if
   * the given time elapsed between calls.
   *
   * @param  {Function} fn
   * @param  {Number} timeout
   *
   * @return {Function} debounced function
   */
  function debounce(fn, timeout) {

    var timer;

    var lastArgs;
    var lastThis;

    var lastNow;

    function fire() {

      var now = Date.now();

      var scheduledDiff = lastNow + timeout - now;

      if (scheduledDiff > 0) {
        return schedule(scheduledDiff);
      }

      fn.apply(lastThis, lastArgs);

      timer = lastNow = lastArgs = lastThis = undefined;
    }

    function schedule(timeout) {
      timer = setTimeout(fire, timeout);
    }

    return function () {

      lastNow = Date.now();

      for (var _len = arguments.length, args = Array(_len), _key = 0; _key < _len; _key++) {
        args[_key] = arguments[_key];
      }

      lastArgs = args;
      lastThis = this;

      // ensure an execution is scheduled
      if (!timer) {
        schedule(timeout);
      }
    };
  }

  /**
   * Bind function against target <this>.
   *
   * @param  {Function} fn
   * @param  {Object}   target
   *
   * @return {Function} bound function
   */
  function bind$2(fn, target) {
    return fn.bind(target);
  }

  var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

  /**
   * Convenience wrapper for `Object.assign`.
   *
   * @param {Object} target
   * @param {...Object} others
   *
   * @return {Object} the target
   */
  function assign(target) {
    for (var _len = arguments.length, others = Array(_len > 1 ? _len - 1 : 0), _key = 1; _key < _len; _key++) {
      others[_key - 1] = arguments[_key];
    }

    return _extends.apply(undefined, [target].concat(others));
  }

  var sign = Math.sign || function(n) {
    return n >= 0 ? 1 : -1;
  };

  var RANGE = { min: 0.2, max: 4 },
      NUM_STEPS = 10;

  var DELTA_THRESHOLD = 0.1;

  var DEFAULT_SCALE = 0.75;

  /**
   * An implementation of zooming and scrolling within the
   * {@link Canvas} via the mouse wheel.
   *
   * Mouse wheel zooming / scrolling may be disabled using
   * the {@link toggle(enabled)} method.
   *
   * @param {Object} [config]
   * @param {Boolean} [config.enabled=true] default enabled state
   * @param {Number} [config.scale=.75] scroll sensivity
   * @param {EventBus} eventBus
   * @param {Canvas} canvas
   */
  function ZoomScroll(config, eventBus, canvas) {

    config = config || {};

    this._enabled = false;

    this._canvas = canvas;
    this._container = canvas._container;

    this._handleWheel = bind$2(this._handleWheel, this);

    this._totalDelta = 0;
    this._scale = config.scale || DEFAULT_SCALE;

    var self = this;

    eventBus.on('canvas.init', function(e) {
      self._init(config.enabled !== false);
    });
  }

  ZoomScroll.$inject = [
    'config.zoomScroll',
    'eventBus',
    'canvas'
  ];

  ZoomScroll.prototype.scroll = function scroll(delta$$1) {
    this._canvas.scroll(delta$$1);
  };


  ZoomScroll.prototype.reset = function reset() {
    this._canvas.zoom('fit-viewport');
  };

  /**
   * Zoom depending on delta.
   *
   * @param {number} delta - Zoom delta.
   * @param {Object} position - Zoom position.
   */
  ZoomScroll.prototype.zoom = function zoom(delta$$1, position) {

    // zoom with half the step size of stepZoom
    var stepSize = getStepSize(RANGE, NUM_STEPS * 2);

    // add until threshold reached
    this._totalDelta += delta$$1;

    if (Math.abs(this._totalDelta) > DELTA_THRESHOLD) {
      this._zoom(delta$$1, position, stepSize);

      // reset
      this._totalDelta = 0;
    }
  };


  ZoomScroll.prototype._handleWheel = function handleWheel(event$$1) {
    // event is already handled by '.djs-scrollable'
    if (closest(event$$1.target, '.djs-scrollable', true)) {
      return;
    }

    var element = this._container;

    event$$1.preventDefault();

    // pinch to zoom is mapped to wheel + ctrlKey = true
    // in modern browsers (!)

    var isZoom = event$$1.ctrlKey;

    var isHorizontalScroll = event$$1.shiftKey;

    var factor = -1 * this._scale,
        delta$$1;

    if (isZoom) {
      factor *= event$$1.deltaMode === 0 ? 0.020 : 0.32;
    } else {
      factor *= event$$1.deltaMode === 0 ? 1.0 : 16.0;
    }

    if (isZoom) {
      var elementRect = element.getBoundingClientRect();

      var offset = {
        x: event$$1.clientX - elementRect.left,
        y: event$$1.clientY - elementRect.top
      };

      delta$$1 = (
        Math.sqrt(
          Math.pow(event$$1.deltaY, 2) +
          Math.pow(event$$1.deltaX, 2)
        ) * sign(event$$1.deltaY) * factor
      );

      // zoom in relative to diagram {x,y} coordinates
      this.zoom(delta$$1, offset);
    } else {

      if (isHorizontalScroll) {
        delta$$1 = {
          dx: factor * event$$1.deltaY,
          dy: 0
        };
      } else {
        delta$$1 = {
          dx: factor * event$$1.deltaX,
          dy: factor * event$$1.deltaY
        };
      }

      this.scroll(delta$$1);
    }
  };

  /**
   * Zoom with fixed step size.
   *
   * @param {number} delta - Zoom delta (1 for zooming in, -1 for out).
   * @param {Object} position - Zoom position.
   */
  ZoomScroll.prototype.stepZoom = function stepZoom(delta$$1, position) {

    var stepSize = getStepSize(RANGE, NUM_STEPS);

    this._zoom(delta$$1, position, stepSize);
  };


  /**
   * Zoom in/out given a step size.
   *
   * @param {number} delta - Zoom delta. Can be positive or negative.
   * @param {Object} position - Zoom position.
   * @param {number} stepSize - Step size.
   */
  ZoomScroll.prototype._zoom = function(delta$$1, position, stepSize) {
    var canvas = this._canvas;

    var direction = delta$$1 > 0 ? 1 : -1;

    var currentLinearZoomLevel = log10(canvas.zoom());

    // snap to a proximate zoom step
    var newLinearZoomLevel = Math.round(currentLinearZoomLevel / stepSize) * stepSize;

    // increase or decrease one zoom step in the given direction
    newLinearZoomLevel += stepSize * direction;

    // calculate the absolute logarithmic zoom level based on the linear zoom level
    // (e.g. 2 for an absolute x2 zoom)
    var newLogZoomLevel = Math.pow(10, newLinearZoomLevel);

    canvas.zoom(cap(RANGE, newLogZoomLevel), position);
  };


  /**
   * Toggle the zoom scroll ability via mouse wheel.
   *
   * @param  {Boolean} [newEnabled] new enabled state
   */
  ZoomScroll.prototype.toggle = function toggle(newEnabled) {

    var element = this._container;
    var handleWheel = this._handleWheel;

    var oldEnabled = this._enabled;

    if (typeof newEnabled === 'undefined') {
      newEnabled = !oldEnabled;
    }

    // only react on actual changes
    if (oldEnabled !== newEnabled) {

      // add or remove wheel listener based on
      // changed enabled state
      componentEvent[newEnabled ? 'bind' : 'unbind'](element, 'wheel', handleWheel, false);
    }

    this._enabled = newEnabled;

    return newEnabled;
  };


  ZoomScroll.prototype._init = function(newEnabled) {
    this.toggle(newEnabled);
  };

  var ZoomScrollModule = {
    __init__: [ 'zoomScroll' ],
    zoomScroll: [ 'type', ZoomScroll ]
  };

  var CURSOR_CLS_PATTERN = /^djs-cursor-.*$/;


  function set(mode) {
    var classes$$1 = classes(document.body);

    classes$$1.removeMatching(CURSOR_CLS_PATTERN);

    if (mode) {
      classes$$1.add('djs-cursor-' + mode);
    }
  }

  function unset() {
    set(null);
  }

  var TRAP_PRIORITY = 5000;

  /**
   * Installs a click trap that prevents a ghost click following a dragging operation.
   *
   * @return {Function} a function to immediately remove the installed trap.
   */
  function install(eventBus, eventName) {

    eventName = eventName || 'element.click';

    function trap() {
      return false;
    }

    eventBus.once(eventName, TRAP_PRIORITY, trap);

    return function() {
      eventBus.off(eventName, trap);
    };
  }

  function toPoint(event) {

    if (event.pointers && event.pointers.length) {
      event = event.pointers[0];
    }

    if (event.touches && event.touches.length) {
      event = event.touches[0];
    }

    return event ? {
      x: event.clientX,
      y: event.clientY
    } : null;
  }

  var THRESHOLD = 15;


  /**
   * Move the canvas via mouse.
   *
   * @param {EventBus} eventBus
   * @param {Canvas} canvas
   */
  function MoveCanvas(eventBus, canvas) {

    var context;


    // listen for move on element mouse down;
    // allow others to hook into the event before us though
    // (dragging / element moving will do this)
    eventBus.on('element.mousedown', 500, function(e) {
      return handleStart(e.originalEvent);
    });


    function handleMove(event$$1) {

      var start = context.start,
          position = toPoint(event$$1),
          delta$$1 = delta(position, start);

      if (!context.dragging && length(delta$$1) > THRESHOLD) {
        context.dragging = true;

        install(eventBus);

        set('grab');
      }

      if (context.dragging) {

        var lastPosition = context.last || context.start;

        delta$$1 = delta(position, lastPosition);

        canvas.scroll({
          dx: delta$$1.x,
          dy: delta$$1.y
        });

        context.last = position;
      }

      // prevent select
      event$$1.preventDefault();
    }


    function handleEnd(event$$1) {
      componentEvent.unbind(document, 'mousemove', handleMove);
      componentEvent.unbind(document, 'mouseup', handleEnd);

      context = null;

      unset();
    }

    function handleStart(event$$1) {
      // event is already handled by '.djs-draggable'
      if (closest(event$$1.target, '.djs-draggable')) {
        return;
      }


      // reject non-left left mouse button or modifier key
      if (event$$1.button || event$$1.ctrlKey || event$$1.shiftKey || event$$1.altKey) {
        return;
      }

      context = {
        start: toPoint(event$$1)
      };

      componentEvent.bind(document, 'mousemove', handleMove);
      componentEvent.bind(document, 'mouseup', handleEnd);

      // we've handled the event
      return true;
    }
  }


  MoveCanvas.$inject = [
    'eventBus',
    'canvas'
  ];



  // helpers ///////

  function length(point) {
    return Math.sqrt(Math.pow(point.x, 2) + Math.pow(point.y, 2));
  }

  var MoveCanvasModule = {
    __init__: [ 'moveCanvas' ],
    moveCanvas: [ 'type', MoveCanvas ]
  };

  var DEFAULT_RENDER_PRIORITY = 1000;

  /**
   * The base implementation of shape and connection renderers.
   *
   * @param {EventBus} eventBus
   * @param {Number} [renderPriority=1000]
   */
  function BaseRenderer(eventBus, renderPriority) {
    var self = this;

    renderPriority = renderPriority || DEFAULT_RENDER_PRIORITY;

    eventBus.on([ 'render.shape', 'render.connection' ], renderPriority, function(evt, context) {
      var type = evt.type,
          element = context.element,
          visuals = context.gfx;

      if (self.canRender(element)) {
        if (type === 'render.shape') {
          return self.drawShape(visuals, element);
        } else {
          return self.drawConnection(visuals, element);
        }
      }
    });

    eventBus.on([ 'render.getShapePath', 'render.getConnectionPath'], renderPriority, function(evt, element) {
      if (self.canRender(element)) {
        if (evt.type === 'render.getShapePath') {
          return self.getShapePath(element);
        } else {
          return self.getConnectionPath(element);
        }
      }
    });
  }

  /**
   * Should check whether *this* renderer can render
   * the element/connection.
   *
   * @param {element} element
   *
   * @returns {Boolean}
   */
  BaseRenderer.prototype.canRender = function() {};

  /**
   * Provides the shape's snap svg element to be drawn on the `canvas`.
   *
   * @param {djs.Graphics} visuals
   * @param {Shape} shape
   *
   * @returns {Snap.svg} [returns a Snap.svg paper element ]
   */
  BaseRenderer.prototype.drawShape = function() {};

  /**
   * Provides the shape's snap svg element to be drawn on the `canvas`.
   *
   * @param {djs.Graphics} visuals
   * @param {Connection} connection
   *
   * @returns {Snap.svg} [returns a Snap.svg paper element ]
   */
  BaseRenderer.prototype.drawConnection = function() {};

  /**
   * Gets the SVG path of a shape that represents it's visual bounds.
   *
   * @param {Shape} shape
   *
   * @return {string} svg path
   */
  BaseRenderer.prototype.getShapePath = function() {};

  /**
   * Gets the SVG path of a connection that represents it's visual bounds.
   *
   * @param {Connection} connection
   *
   * @return {string} svg path
   */
  BaseRenderer.prototype.getConnectionPath = function() {};

  function ensureImported(element, target) {

    if (element.ownerDocument !== target.ownerDocument) {
      try {
        // may fail on webkit
        return target.ownerDocument.importNode(element, true);
      } catch (e) {
        // ignore
      }
    }

    return element;
  }

  /**
   * appendTo utility
   */

  /**
   * Append a node to a target element and return the appended node.
   *
   * @param  {SVGElement} element
   * @param  {SVGElement} target
   *
   * @return {SVGElement} the appended node
   */
  function appendTo(element, target) {
    return target.appendChild(ensureImported(element, target));
  }

  /**
   * append utility
   */

  /**
   * Append a node to an element
   *
   * @param  {SVGElement} element
   * @param  {SVGElement} node
   *
   * @return {SVGElement} the element
   */
  function append(target, node) {
    appendTo(node, target);
    return target;
  }

  /**
   * attribute accessor utility
   */

  var LENGTH_ATTR = 2;

  var CSS_PROPERTIES = {
    'alignment-baseline': 1,
    'baseline-shift': 1,
    'clip': 1,
    'clip-path': 1,
    'clip-rule': 1,
    'color': 1,
    'color-interpolation': 1,
    'color-interpolation-filters': 1,
    'color-profile': 1,
    'color-rendering': 1,
    'cursor': 1,
    'direction': 1,
    'display': 1,
    'dominant-baseline': 1,
    'enable-background': 1,
    'fill': 1,
    'fill-opacity': 1,
    'fill-rule': 1,
    'filter': 1,
    'flood-color': 1,
    'flood-opacity': 1,
    'font': 1,
    'font-family': 1,
    'font-size': LENGTH_ATTR,
    'font-size-adjust': 1,
    'font-stretch': 1,
    'font-style': 1,
    'font-variant': 1,
    'font-weight': 1,
    'glyph-orientation-horizontal': 1,
    'glyph-orientation-vertical': 1,
    'image-rendering': 1,
    'kerning': 1,
    'letter-spacing': 1,
    'lighting-color': 1,
    'marker': 1,
    'marker-end': 1,
    'marker-mid': 1,
    'marker-start': 1,
    'mask': 1,
    'opacity': 1,
    'overflow': 1,
    'pointer-events': 1,
    'shape-rendering': 1,
    'stop-color': 1,
    'stop-opacity': 1,
    'stroke': 1,
    'stroke-dasharray': 1,
    'stroke-dashoffset': 1,
    'stroke-linecap': 1,
    'stroke-linejoin': 1,
    'stroke-miterlimit': 1,
    'stroke-opacity': 1,
    'stroke-width': LENGTH_ATTR,
    'text-anchor': 1,
    'text-decoration': 1,
    'text-rendering': 1,
    'unicode-bidi': 1,
    'visibility': 1,
    'word-spacing': 1,
    'writing-mode': 1
  };


  function getAttribute(node, name) {
    if (CSS_PROPERTIES[name]) {
      return node.style[name];
    } else {
      return node.getAttributeNS(null, name);
    }
  }

  function setAttribute(node, name, value) {
    var hyphenated = name.replace(/([a-z])([A-Z])/g, '$1-$2').toLowerCase();

    var type = CSS_PROPERTIES[hyphenated];

    if (type) {
      // append pixel unit, unless present
      if (type === LENGTH_ATTR && typeof value === 'number') {
        value = String(value) + 'px';
      }

      node.style[hyphenated] = value;
    } else {
      node.setAttributeNS(null, name, value);
    }
  }

  function setAttributes(node, attrs) {

    var names = Object.keys(attrs), i, name;

    for (i = 0, name; (name = names[i]); i++) {
      setAttribute(node, name, attrs[name]);
    }
  }

  /**
   * Gets or sets raw attributes on a node.
   *
   * @param  {SVGElement} node
   * @param  {Object} [attrs]
   * @param  {String} [name]
   * @param  {String} [value]
   *
   * @return {String}
   */
  function attr$1(node, name, value) {
    if (typeof name === 'string') {
      if (value !== undefined) {
        setAttribute(node, name, value);
      } else {
        return getAttribute(node, name);
      }
    } else {
      setAttributes(node, name);
    }

    return node;
  }

  /**
   * Clear utility
   */
  function index(arr, obj) {
    if (arr.indexOf) {
      return arr.indexOf(obj);
    }


    for (var i = 0; i < arr.length; ++i) {
      if (arr[i] === obj) {
        return i;
      }
    }

    return -1;
  }

  var re$1 = /\s+/;

  var toString$1 = Object.prototype.toString;

  function defined(o) {
    return typeof o !== 'undefined';
  }

  /**
   * Wrap `el` in a `ClassList`.
   *
   * @param {Element} el
   * @return {ClassList}
   * @api public
   */

  function classes$1(el) {
    return new ClassList$1(el);
  }

  function ClassList$1(el) {
    if (!el || !el.nodeType) {
      throw new Error('A DOM element reference is required');
    }
    this.el = el;
    this.list = el.classList;
  }

  /**
   * Add class `name` if not already present.
   *
   * @param {String} name
   * @return {ClassList}
   * @api public
   */

  ClassList$1.prototype.add = function(name) {

    // classList
    if (this.list) {
      this.list.add(name);
      return this;
    }

    // fallback
    var arr = this.array();
    var i = index(arr, name);
    if (!~i) {
      arr.push(name);
    }

    if (defined(this.el.className.baseVal)) {
      this.el.className.baseVal = arr.join(' ');
    } else {
      this.el.className = arr.join(' ');
    }

    return this;
  };

  /**
   * Remove class `name` when present, or
   * pass a regular expression to remove
   * any which match.
   *
   * @param {String|RegExp} name
   * @return {ClassList}
   * @api public
   */

  ClassList$1.prototype.remove = function(name) {
    if ('[object RegExp]' === toString$1.call(name)) {
      return this.removeMatching(name);
    }

    // classList
    if (this.list) {
      this.list.remove(name);
      return this;
    }

    // fallback
    var arr = this.array();
    var i = index(arr, name);
    if (~i) {
      arr.splice(i, 1);
    }
    this.el.className.baseVal = arr.join(' ');
    return this;
  };

  /**
   * Remove all classes matching `re`.
   *
   * @param {RegExp} re
   * @return {ClassList}
   * @api private
   */

  ClassList$1.prototype.removeMatching = function(re) {
    var arr = this.array();
    for (var i = 0; i < arr.length; i++) {
      if (re.test(arr[i])) {
        this.remove(arr[i]);
      }
    }
    return this;
  };

  /**
   * Toggle class `name`, can force state via `force`.
   *
   * For browsers that support classList, but do not support `force` yet,
   * the mistake will be detected and corrected.
   *
   * @param {String} name
   * @param {Boolean} force
   * @return {ClassList}
   * @api public
   */

  ClassList$1.prototype.toggle = function(name, force) {
    // classList
    if (this.list) {
      if (defined(force)) {
        if (force !== this.list.toggle(name, force)) {
          this.list.toggle(name); // toggle again to correct
        }
      } else {
        this.list.toggle(name);
      }
      return this;
    }

    // fallback
    if (defined(force)) {
      if (!force) {
        this.remove(name);
      } else {
        this.add(name);
      }
    } else {
      if (this.has(name)) {
        this.remove(name);
      } else {
        this.add(name);
      }
    }

    return this;
  };

  /**
   * Return an array of classes.
   *
   * @return {Array}
   * @api public
   */

  ClassList$1.prototype.array = function() {
    var className = this.el.getAttribute('class') || '';
    var str = className.replace(/^\s+|\s+$/g, '');
    var arr = str.split(re$1);
    if ('' === arr[0]) {
      arr.shift();
    }
    return arr;
  };

  /**
   * Check if class `name` is present.
   *
   * @param {String} name
   * @return {ClassList}
   * @api public
   */

  ClassList$1.prototype.has =
  ClassList$1.prototype.contains = function(name) {
    return (
      this.list ?
        this.list.contains(name) :
        !! ~index(this.array(), name)
    );
  };

  function remove$1(element) {
    var parent = element.parentNode;

    if (parent) {
      parent.removeChild(element);
    }

    return element;
  }

  /**
   * Clear utility
   */

  /**
   * Removes all children from the given element
   *
   * @param  {DOMElement} element
   * @return {DOMElement} the element (for chaining)
   */
  function clear$1(element) {
    var child;

    while ((child = element.firstChild)) {
      remove$1(child);
    }

    return element;
  }

  var ns = {
    svg: 'http://www.w3.org/2000/svg'
  };

  /**
   * DOM parsing utility
   */

  var SVG_START = '<svg xmlns="' + ns.svg + '"';

  function parse$1(svg) {

    var unwrap = false;

    // ensure we import a valid svg document
    if (svg.substring(0, 4) === '<svg') {
      if (svg.indexOf(ns.svg) === -1) {
        svg = SVG_START + svg.substring(4);
      }
    } else {
      // namespace svg
      svg = SVG_START + '>' + svg + '</svg>';
      unwrap = true;
    }

    var parsed = parseDocument(svg);

    if (!unwrap) {
      return parsed;
    }

    var fragment = document.createDocumentFragment();

    var parent = parsed.firstChild;

    while (parent.firstChild) {
      fragment.appendChild(parent.firstChild);
    }

    return fragment;
  }

  function parseDocument(svg) {

    var parser;

    // parse
    parser = new DOMParser();
    parser.async = false;

    return parser.parseFromString(svg, 'text/xml');
  }

  /**
   * Create utility for SVG elements
   */


  /**
   * Create a specific type from name or SVG markup.
   *
   * @param {String} name the name or markup of the element
   * @param {Object} [attrs] attributes to set on the element
   *
   * @returns {SVGElement}
   */
  function create(name, attrs) {
    var element;

    if (name.charAt(0) === '<') {
      element = parse$1(name).firstChild;
      element = document.importNode(element, true);
    } else {
      element = document.createElementNS(ns.svg, name);
    }

    if (attrs) {
      attr$1(element, attrs);
    }

    return element;
  }

  /**
   * Geometry helpers
   */

  // fake node used to instantiate svg geometry elements
  var node = create('svg');

  function extend(object, props) {
    var i, k, keys = Object.keys(props);

    for (i = 0; (k = keys[i]); i++) {
      object[k] = props[k];
    }

    return object;
  }

  /**
   * Create matrix via args.
   *
   * @example
   *
   * createMatrix({ a: 1, b: 1 });
   * createMatrix();
   * createMatrix(1, 2, 0, 0, 30, 20);
   *
   * @return {SVGMatrix}
   */
  function createMatrix(a, b, c, d, e, f) {
    var matrix = node.createSVGMatrix();

    switch (arguments.length) {
    case 0:
      return matrix;
    case 1:
      return extend(matrix, a);
    case 6:
      return extend(matrix, {
        a: a,
        b: b,
        c: c,
        d: d,
        e: e,
        f: f
      });
    }
  }

  function createTransform(matrix) {
    if (matrix) {
      return node.createSVGTransformFromMatrix(matrix);
    } else {
      return node.createSVGTransform();
    }
  }

  /**
   * Serialization util
   */

  var TEXT_ENTITIES = /([&<>]{1})/g;
  var ATTR_ENTITIES = /([\n\r"]{1})/g;

  var ENTITY_REPLACEMENT = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '\''
  };

  function escape(str, pattern) {

    function replaceFn(match, entity) {
      return ENTITY_REPLACEMENT[entity] || entity;
    }

    return str.replace(pattern, replaceFn);
  }

  function serialize(node, output) {

    var i, len, attrMap, attrNode, childNodes;

    switch (node.nodeType) {
    // TEXT
    case 3:
      // replace special XML characters
      output.push(escape(node.textContent, TEXT_ENTITIES));
      break;

    // ELEMENT
    case 1:
      output.push('<', node.tagName);

      if (node.hasAttributes()) {
        attrMap = node.attributes;
        for (i = 0, len = attrMap.length; i < len; ++i) {
          attrNode = attrMap.item(i);
          output.push(' ', attrNode.name, '="', escape(attrNode.value, ATTR_ENTITIES), '"');
        }
      }

      if (node.hasChildNodes()) {
        output.push('>');
        childNodes = node.childNodes;
        for (i = 0, len = childNodes.length; i < len; ++i) {
          serialize(childNodes.item(i), output);
        }
        output.push('</', node.tagName, '>');
      } else {
        output.push('/>');
      }
      break;

    // COMMENT
    case 8:
      output.push('<!--', escape(node.nodeValue, TEXT_ENTITIES), '-->');
      break;

    // CDATA
    case 4:
      output.push('<![CDATA[', node.nodeValue, ']]>');
      break;

    default:
      throw new Error('unable to handle node ' + node.nodeType);
    }

    return output;
  }

  /**
   * innerHTML like functionality for SVG elements.
   * based on innerSVG (https://code.google.com/p/innersvg)
   */


  function set$1(element, svg) {

    var parsed = parse$1(svg);

    // clear element contents
    clear$1(element);

    if (!svg) {
      return;
    }

    if (!isFragment(parsed)) {
      // extract <svg> from parsed document
      parsed = parsed.documentElement;
    }

    var nodes = slice(parsed.childNodes);

    // import + append each node
    for (var i = 0; i < nodes.length; i++) {
      appendTo(nodes[i], element);
    }

  }

  function get(element) {
    var child = element.firstChild,
        output = [];

    while (child) {
      serialize(child, output);
      child = child.nextSibling;
    }

    return output.join('');
  }

  function isFragment(node) {
    return node.nodeName === '#document-fragment';
  }

  function innerSVG(element, svg) {

    if (svg !== undefined) {

      try {
        set$1(element, svg);
      } catch (e) {
        throw new Error('error parsing SVG: ' + e.message);
      }

      return element;
    } else {
      return get(element);
    }
  }


  function slice(arr) {
    return Array.prototype.slice.call(arr);
  }

  /**
   * transform accessor utility
   */

  function wrapMatrix(transformList, transform) {
    if (transform instanceof SVGMatrix) {
      return transformList.createSVGTransformFromMatrix(transform);
    }

    return transform;
  }


  function setTransforms(transformList, transforms) {
    var i, t;

    transformList.clear();

    for (i = 0; (t = transforms[i]); i++) {
      transformList.appendItem(wrapMatrix(transformList, t));
    }
  }

  /**
   * Get or set the transforms on the given node.
   *
   * @param {SVGElement} node
   * @param  {SVGTransform|SVGMatrix|Array<SVGTransform|SVGMatrix>} [transforms]
   *
   * @return {SVGTransform} the consolidated transform
   */
  function transform(node, transforms) {
    var transformList = node.transform.baseVal;

    if (transforms) {

      if (!Array.isArray(transforms)) {
        transforms = [ transforms ];
      }

      setTransforms(transformList, transforms);
    }

    return transformList.consolidate();
  }

  function getRelativeClosePoint(point1, point2, t) {
    return {
      x: (1-t)*point1.x + t*point2.x,
      y: (1-t)*point1.y + t*point2.y
    }
  }

  function componentsToPath(elements) {
    return elements.join(',').replace(/,?([A-z]),?/g, '$1');
  }

  function toSVGPoints(points) {
    var result = '';

    for (var i = 0, p; (p = points[i]); i++) {
      result += p.x + ',' + p.y + ' ';
    }

    return result;
  }

  function createLine(points, attrs) {

    var line = create('polyline');
    attr$1(line, { points: toSVGPoints(points) });

    if (attrs) {
      attr$1(line, attrs);
    }

    return line;
  }

  function drawText(parentGfx, text, options, textRenderer) {
    var svgText = textRenderer.createText(text, options);

    classes$1(svgText).add('djs-label');
    append(parentGfx, svgText);

    return svgText;
  }

  function drawLine(parentGfx, source, target) {
    var line = create('line');

    attr$1(line, {
      x1: source.x,
      y1: source.y,
      x2: target.x,
      y2: target.y,
      stroke: 'black'
     });

    append(parentGfx, line);

    return line;
  }

  function drawRectangle(parentGfx, width, height, attributes) {
    var rectangle = getRectangle(width, height, attributes);

    append(parentGfx, rectangle);

    return rectangle;
  }

  function drawPath(parentGfx, waypoints, attributes) {
    var d = getPathData(waypoints);

    var backgroundPath = getBackgroundPath(getCroppedEndWaypoints(waypoints));
    var path = getPath(d, attributes);

    var group = getGroup([backgroundPath, path]);

    append(parentGfx, group);

    return group;
  }

  function getPath(d, attributes) {
    var path = create('path');

    addPath(path, d);
    addPathMarkers(path, attributes);
    addPathTransformation(path, attributes);
    addStyles(path, attributes);

    return path;
  }
  function getBackgroundPath(waypoints) {
    var dBackground = getPathData(waypoints);

    return getPath(dBackground, { stroke: 'white', 'stroke-width': 10 });
  }
  function getCroppedEndWaypoints(waypoints) {
    var length = waypoints.length;
    var newWaypoints = waypoints.slice();

    if(length >= 2) {
      newWaypoints[0] = getRelativeClosePoint(waypoints[0], waypoints[1], 0.1);
      newWaypoints[length - 1] = getRelativeClosePoint(waypoints[length - 2], waypoints[length - 1], 0.9);
    }

    return newWaypoints;
  }
  function getPathData(waypoints) {
    var pathData = 'M ' + waypoints[0].x + ',' + waypoints[0].y + ' ';

    for (var i = 1; i < waypoints.length; i++) {
      pathData += 'L ' + waypoints[i].x + ',' + waypoints[i].y + ' ';
    }

    return pathData;
  }
  function getGroup(elements) {
      var group = create('g');

      elements.forEach(function(element) {
          append(group, element);
      });

      return group;
  }
  function addPath(path, d) {
    attr$1(path, { d: d });
  }
  function addPathTransformation(path, attributes) {
    if('transform' in attributes) {
      attr$1(path, { transform: attributes.transform });
    }
  }
  function addStyles(svgElement, attributes) {
    if('stroke' in attributes) {
      attr$1(svgElement, { stroke: attributes.stroke });
    }

    if('stroke-width' in attributes) {
      attr$1(svgElement, { 'stroke-width': attributes['stroke-width'] });
    }

    if('fill' in attributes) {
      attr$1(svgElement, { fill: attributes.fill });
    }

    if('stroke-dasharray' in attributes) {
      attr$1(svgElement, { 'stroke-dasharray': attributes['stroke-dasharray'] });
    }
  }
  function addPathMarkers(path, attributes) {
    if('markerEnd' in attributes) {
      attr$1(path, { 'marker-end': 'url(#' + attributes.markerEnd + ')' });
    }

    if('markerStart' in attributes) {
      attr$1(path, { 'marker-start': 'url(#' + attributes.markerStart + ')' });
    }
  }
  function getRectangle(width, height, attributes) {
    var rectangle = create('rect');

    addSize(rectangle, width, height);
    addStyles(rectangle, attributes);

    return rectangle;
  }
  function addSize(shape, width, height) {
    attr$1(shape, {
      width: width,
      height: height
    });
  }
  function getRectPath(shape) {
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

  function drawClass(parentGfx, element, textRenderer) {
    var businessObject = element.businessObject || {};

    var rectangle = drawRectangle(parentGfx, element.width, element.height, {
      fill: 'none',
      stroke: 'black'
    });

    var centerLabelStyle = getCenterLabelStyle(element);
    centerLabelStyle.y = 5;

    if('stereotypes' in element) {
      element.stereotypes.forEach(function(stereotype) {
        var svgStereotype = drawText(parentGfx, '&lt;&lt;' + stereotype + '&gt;&gt;', centerLabelStyle, textRenderer);

        centerLabelStyle.y += svgStereotype.getBBox().height;
      });
    }

    if('name' in element) {
      var svgName = drawText(parentGfx, element.name, centerLabelStyle, textRenderer);

      centerLabelStyle.y += svgName.getBBox().height;
    }

    var hasAttributes = element.labels.some(function(label) {
      return label.labelType === 'property' || label.labelType === 'classifier';
    });

    if(hasAttributes) {
      if('name' in element) {
        drawClassSeparator(parentGfx, element.width, centerLabelStyle.y);
      }
    }

    return rectangle;
  }
  function drawLabel(parentGfx, element, textRenderer) {
    return drawText(parentGfx, element.businessObject + '', getGeneralLabelStyle(), textRenderer);
  }
  function drawClassSeparator(parentGfx, width, y) {
    drawLine(parentGfx, {
      x: 0,
      y: y
    }, {
      x: width,
      y: y
    });
  }
  function getCenterLabelStyle(element) {
    return assign(getGeneralLabelStyle(), {
      x: element.width / 2,
      'text-anchor': 'middle',
    });
  }

  function getGeneralLabelStyle() {
    return {
      dy: '.75em'
    };
  }

  function drawAssociation(parentGfx, element) {
    var attributes = {
      stroke: 'black',
      markerEnd: 'association-marker'
    };

    return drawConnection(parentGfx, element, attributes);
  }

  function drawInheritance(parentGfx, element) {
    var attributes = {
      stroke: 'gray',
      markerEnd: 'inheritance-marker'
    };

    return drawConnection(parentGfx, element, attributes);
  }

  function drawRealization(parentGfx, element) {
    var attributes = {
      stroke: 'black',
      markerEnd: 'realization-marker',
      'stroke-dasharray': '5,10,5'
    };

    return drawConnection(parentGfx, element, attributes);
  }

  function drawDependency(parentGfx, element) {
    var attributes = {
      stroke: 'black',
      markerEnd: 'dependency-marker',
      'stroke-dasharray': '5,10,5'
    };

    return drawConnection(parentGfx, element, attributes);
  }

  function drawAggregation(parentGfx, element) {
    var attributes = {
      stroke: 'black',
      markerStart: 'aggregation-marker-reversed'
    };

    return drawConnection(parentGfx, element, attributes);
  }

  function drawComposition(parentGfx, element) {
    var attributes = {
      stroke: 'black',
      markerStart: 'composition-marker-reversed'
    };

    return drawConnection(parentGfx, element, attributes);
  }

  function drawConnection(parentGfx, element, attributes) {
    return drawPath(parentGfx, element.waypoints, attributes);
  }

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

  function initMarkerDefinitions(canvas) {
    var defs = query('defs', canvas._svg);

    if (!defs) {
      defs = create('defs');

      append(canvas._svg, defs);
    }

    markers.forEach(function(marker) {
      var markerCopy = marker.element.cloneNode(true);

      addMarkerDefinition(defs, marker.name, marker.element, false);
      addMarkerDefinition(defs, marker.name + '-reversed', markerCopy, true);
    });
  }

  function addMarkerDefinition(definitions, id, markerElement, atStart) {
    var svgMarker = create('marker');

    append(svgMarker, markerElement);

    attr$1(svgMarker, {
      id: id,
      viewBox: '0 0 10 10',
      refX: getMarkerRefX(atStart),
      refY: 5,
      markerWidth: 5,
      markerHeight: 5,
      orient: 'auto'
    });

    append(definitions, svgMarker);
  }
  function getMarkerRefX(atStart) {
    if(atStart) {
      return 0;
    } else {
      return 10;
    }
  }

  function createCommonjsModule(fn, module) {
  	return module = { exports: {} }, fn(module, module.exports), module.exports;
  }

  var inherits_browser = createCommonjsModule(function (module) {
  if (typeof Object.create === 'function') {
    // implementation from standard node.js 'util' module
    module.exports = function inherits(ctor, superCtor) {
      ctor.super_ = superCtor;
      ctor.prototype = Object.create(superCtor.prototype, {
        constructor: {
          value: ctor,
          enumerable: false,
          writable: true,
          configurable: true
        }
      });
    };
  } else {
    // old school shim for old browsers
    module.exports = function inherits(ctor, superCtor) {
      ctor.super_ = superCtor;
      var TempCtor = function () {};
      TempCtor.prototype = superCtor.prototype;
      ctor.prototype = new TempCtor();
      ctor.prototype.constructor = ctor;
    };
  }
  });

  inherits_browser(UmlRenderer, BaseRenderer);

  UmlRenderer.$inject = ['eventBus', 'canvas', 'textRenderer'];

  function UmlRenderer(eventBus, canvas, textRenderer) {
    BaseRenderer.call(this, eventBus);

    this.textRenderer = textRenderer;

    this.drawShapeHandlers = {
      "class": drawClass,
      "label": drawLabel
    };

    this.drawConnectionHandlers = {
      "association": drawAssociation,
      "inheritance": drawInheritance,
      "realization": drawRealization,
      "dependency": drawDependency,
      "aggregation": drawAggregation,
      "composition": drawComposition
    };

    initMarkerDefinitions(canvas);
  }

  UmlRenderer.prototype.canRender = function(element) {
    return true;
  };

  UmlRenderer.prototype.drawShape = function(parentGfx, element) {
    var type = element.shapeType;
    var drawShapeHandler = this.drawShapeHandlers[type];

    return drawShapeHandler(parentGfx, element, this.textRenderer);
  };

  UmlRenderer.prototype.drawConnection = function(parentGfx, element) {
    var type = element.connectionType;
    var drawConnectionHandler = this.drawConnectionHandlers[type];

    return drawConnectionHandler(parentGfx, element);
  };

  UmlRenderer.prototype.getShapePath = function(element) {
      return getRectPath(element);
  };

  function TextRenderer() {
    this._style = {
      fontFamily: 'DroidSansMono',
      fontSize: 12
    };
  }

  TextRenderer.prototype.createText = function(text, options) {
    var svgText = create('text');

    var options = assign(options, this._style);

    attr$1(svgText, options);

    innerSVG(svgText, text);

    return svgText;
  };

  TextRenderer.prototype.getDimensions  = function(text, options) {
      var helperText = create('text');

      attr$1(helperText, this._style);

      var helperSvg = getHelperSvg();

      append(helperSvg, helperText);

      helperText.textContent = text;
      var bbox = helperText.getBBox();

      remove$1(helperText);

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
  }
  function createHelperSvg() {
    var helperSvg = create('svg');

    attr$1(helperSvg, getHelperSvgOptions());

    document.body.appendChild(helperSvg);

    return helperSvg;
  }
  function getHelperSvgOptions() {
    return {
      id: 'helper-svg',
      width: 0,
      height: 0,
      style: 'visibility: hidden; position: fixed'
    };
  }

  var DrawModule = {
    __init__: [ 'umlRenderer'],
    umlRenderer: ['type', UmlRenderer],
    textRenderer: [ 'type', TextRenderer]
  };

  /**
   * Computes the distance between two points
   *
   * @param  {Point}  p
   * @param  {Point}  q
   *
   * @return {Number}  distance
   */
  function pointDistance(a, b) {
    if (!a || !b) {
      return -1;
    }

    return Math.sqrt(
      Math.pow(a.x - b.x, 2) +
      Math.pow(a.y - b.y, 2)
    );
  }


  /**
   * Returns true if the point r is on the line between p and q
   *
   * @param  {Point}  p
   * @param  {Point}  q
   * @param  {Point}  r
   * @param  {Number} [accuracy=5] accuracy for points on line check (lower is better)
   *
   * @return {Boolean}
   */
  function pointsOnLine(p, q, r, accuracy) {

    if (typeof accuracy === 'undefined') {
      accuracy = 5;
    }

    if (!p || !q || !r) {
      return false;
    }

    var val = (q.x - p.x) * (r.y - p.y) - (q.y - p.y) * (r.x - p.x),
        dist = pointDistance(p, q);

    // @see http://stackoverflow.com/a/907491/412190
    return Math.abs(val / dist) <= accuracy;
  }

  /**
   * This file contains portions that got extraced from Snap.svg (licensed Apache-2.0).
   *
   * @see https://github.com/adobe-webplatform/Snap.svg/blob/master/src/path.js
   */

  /* eslint no-fallthrough: "off" */

  var math = Math,
      PI = math.PI;

  /**
   * Convert the given bounds to a { top, left, bottom, right } descriptor.
   *
   * @param {Bounds|Point} bounds
   *
   * @return {Object}
   */
  function asTRBL(bounds) {
    return {
      top: bounds.y,
      right: bounds.x + (bounds.width || 0),
      bottom: bounds.y + (bounds.height || 0),
      left: bounds.x
    };
  }


  // orientation utils //////////////////////

  /**
   * Get orientation of the given rectangle with respect to
   * the reference rectangle.
   *
   * A padding (positive or negative) may be passed to influence
   * horizontal / vertical orientation and intersection.
   *
   * @param {Bounds} rect
   * @param {Bounds} reference
   * @param {Point|Number} padding
   *
   * @return {String} the orientation; one of top, top-left, left, ..., bottom, right or intersect.
   */
  function getOrientation(rect, reference, padding) {

    padding = padding || 0;

    // make sure we can use an object, too
    // for individual { x, y } padding
    if (!isObject(padding)) {
      padding = { x: padding, y: padding };
    }


    var rectOrientation = asTRBL(rect),
        referenceOrientation = asTRBL(reference);

    var top = rectOrientation.bottom + padding.y <= referenceOrientation.top,
        right = rectOrientation.left - padding.x >= referenceOrientation.right,
        bottom = rectOrientation.top - padding.y >= referenceOrientation.bottom,
        left = rectOrientation.right + padding.x <= referenceOrientation.left;

    var vertical = top ? 'top' : (bottom ? 'bottom' : null),
        horizontal = left ? 'left' : (right ? 'right' : null);

    if (horizontal && vertical) {
      return vertical + '-' + horizontal;
    } else {
      return horizontal || vertical || 'intersect';
    }
  }

  var MIN_SEGMENT_LENGTH = 20,
      POINT_ORIENTATION_PADDING = 5;

  var round = Math.round;

  function needsTurn(orientation, startDirection) {
    return !{
      t: /top/,
      r: /right/,
      b: /bottom/,
      l: /left/,
      h: /./,
      v: /./
    }[startDirection].test(orientation);
  }

  function canLayoutStraight(direction, targetOrientation) {
    return {
      t: /top/,
      r: /right/,
      b: /bottom/,
      l: /left/,
      h: /left|right/,
      v: /top|bottom/
    }[direction].test(targetOrientation);
  }

  function getSegmentBendpoints(a, b, directions) {
    var orientation = getOrientation(b, a, POINT_ORIENTATION_PADDING);

    var startDirection = directions.split(':')[0];

    var xmid = round((b.x - a.x) / 2 + a.x),
        ymid = round((b.y - a.y) / 2 + a.y);

    var segmentEnd, segmentDirections;

    var layoutStraight = canLayoutStraight(startDirection, orientation),
        layoutHorizontal = /h|r|l/.test(startDirection),
        layoutTurn = false;

    var turnNextDirections = false;

    if (layoutStraight) {
      segmentEnd = layoutHorizontal ? { x: xmid, y: a.y } : { x: a.x, y: ymid };

      segmentDirections = layoutHorizontal ? 'h:h' : 'v:v';
    } else {
      layoutTurn = needsTurn(orientation, startDirection);

      segmentDirections = layoutHorizontal ? 'h:v' : 'v:h';

      if (layoutTurn) {

        if (layoutHorizontal) {
          turnNextDirections = ymid === a.y;

          segmentEnd = {
            x: a.x + MIN_SEGMENT_LENGTH * (/l/.test(startDirection) ? -1 : 1),
            y: turnNextDirections ? ymid + MIN_SEGMENT_LENGTH : ymid
          };
        } else {
          turnNextDirections = xmid === a.x;

          segmentEnd = {
            x: turnNextDirections ? xmid + MIN_SEGMENT_LENGTH : xmid,
            y: a.y + MIN_SEGMENT_LENGTH * (/t/.test(startDirection) ? -1 : 1)
          };
        }

      } else {
        segmentEnd = {
          x: xmid,
          y: ymid
        };
      }
    }

    return {
      waypoints: getBendpoints(a, segmentEnd, segmentDirections).concat(segmentEnd),
      directions:  segmentDirections,
      turnNextDirections: turnNextDirections
    };
  }

  function getStartSegment(a, b, directions) {
    return getSegmentBendpoints(a, b, directions);
  }

  function getEndSegment(a, b, directions) {
    var invertedSegment = getSegmentBendpoints(b, a, invertDirections(directions));

    return {
      waypoints: invertedSegment.waypoints.slice().reverse(),
      directions: invertDirections(invertedSegment.directions),
      turnNextDirections: invertedSegment.turnNextDirections
    };
  }

  function getMidSegment(startSegment, endSegment) {

    var startDirection = startSegment.directions.split(':')[1],
        endDirection = endSegment.directions.split(':')[0];

    if (startSegment.turnNextDirections) {
      startDirection = startDirection == 'h' ? 'v' : 'h';
    }

    if (endSegment.turnNextDirections) {
      endDirection = endDirection == 'h' ? 'v' : 'h';
    }

    var directions = startDirection + ':' + endDirection;

    var bendpoints = getBendpoints(
      startSegment.waypoints[startSegment.waypoints.length - 1],
      endSegment.waypoints[0],
      directions
    );

    return {
      waypoints: bendpoints,
      directions: directions
    };
  }

  function invertDirections(directions) {
    return directions.split(':').reverse().join(':');
  }

  /**
   * Handle simple layouts with maximum two bendpoints.
   */
  function getSimpleBendpoints(a, b, directions) {

    var xmid = round((b.x - a.x) / 2 + a.x),
        ymid = round((b.y - a.y) / 2 + a.y);

    // one point, right or left from a
    if (directions === 'h:v') {
      return [ { x: b.x, y: a.y } ];
    }

    // one point, above or below a
    if (directions === 'v:h') {
      return [ { x: a.x, y: b.y } ];
    }

    // vertical segment between a and b
    if (directions === 'h:h') {
      return [
        { x: xmid, y: a.y },
        { x: xmid, y: b.y }
      ];
    }

    // horizontal segment between a and b
    if (directions === 'v:v') {
      return [
        { x: a.x, y: ymid },
        { x: b.x, y: ymid }
      ];
    }

    throw new Error('invalid directions: can only handle varians of [hv]:[hv]');
  }


  /**
   * Returns the mid points for a manhattan connection between two points.
   *
   * @example h:h (horizontal:horizontal)
   *
   * [a]----[x]
   *         |
   *        [x]----[b]
   *
   * @example h:v (horizontal:vertical)
   *
   * [a]----[x]
   *         |
   *        [b]
   *
   * @example h:r (horizontal:right)
   *
   * [a]----[x]
   *         |
   *    [b]-[x]
   *
   * @param  {Point} a
   * @param  {Point} b
   * @param  {String} directions
   *
   * @return {Array<Point>}
   */
  function getBendpoints(a, b, directions) {
    directions = directions || 'h:h';

    if (!isValidDirections(directions)) {
      throw new Error(
        'unknown directions: <' + directions + '>: ' +
        'must be specified as <start>:<end> ' +
        'with start/end in { h,v,t,r,b,l }'
      );
    }

    // compute explicit directions, involving trbl dockings
    // using a three segmented layouting algorithm
    if (isExplicitDirections(directions)) {
      var startSegment = getStartSegment(a, b, directions),
          endSegment = getEndSegment(a, b, directions),
          midSegment = getMidSegment(startSegment, endSegment);

      return [].concat(
        startSegment.waypoints,
        midSegment.waypoints,
        endSegment.waypoints
      );
    }

    // handle simple [hv]:[hv] cases that can be easily computed
    return getSimpleBendpoints(a, b, directions);
  }

  /**
   * Create a connection between the two points according
   * to the manhattan layout (only horizontal and vertical) edges.
   *
   * @param {Point} a
   * @param {Point} b
   *
   * @param {String} [directions='h:h'] specifies manhattan directions for each point as {adirection}:{bdirection}.
                     A directionfor a point is either `h` (horizontal) or `v` (vertical)
   *
   * @return {Array<Point>}
   */
  function connectPoints(a, b, directions) {

    var points = getBendpoints(a, b, directions);

    points.unshift(a);
    points.push(b);

    return withoutRedundantPoints(points);
  }

  function isValidDirections(directions) {
    return directions && /^h|v|t|r|b|l:h|v|t|r|b|l$/.test(directions);
  }

  function isExplicitDirections(directions) {
    return directions && /t|r|b|l/.test(directions);
  }


  /**
   * Return list of waypoints with redundant ones filtered out.
   *
   * @example
   *
   * Original points:
   *
   *   [x] ----- [x] ------ [x]
   *                         |
   *                        [x] ----- [x] - [x]
   *
   * Filtered:
   *
   *   [x] ---------------- [x]
   *                         |
   *                        [x] ----------- [x]
   *
   * @param  {Array<Point>} waypoints
   *
   * @return {Array<Point>}
   */
  function withoutRedundantPoints(waypoints) {
    return waypoints.reduce(function(points, p, idx) {

      var previous = points[points.length - 1],
          next = waypoints[idx + 1];

      if (!pointsOnLine(previous, next, p, 0)) {
        points.push(p);
      }

      return points;
    }, []);
  }

  UmlImporter.$inject = ['elementFactory', 'canvas', 'textRenderer', 'modeling', 'layouter', 'commandStack'];

  function UmlImporter(elementFactory, canvas, textRenderer, modeling, layouter, commandStack) {
    this.textRenderer = textRenderer;
    this.elementFactory = elementFactory;
    this.canvas = canvas;
    this.modeling = modeling;
    this.layouter = layouter;
    this.commandStack = commandStack;
  }

  UmlImporter.prototype.import = function(graphData) {
    var textRenderer = this.textRenderer;
    var elementFactory = this.elementFactory;
    var canvas = this.canvas;
    var modeling = this.modeling;
    var layouter = this.layouter;
    var commandStack = this.commandStack;

    var root = elementFactory.createRoot();
    canvas.setRootElement(root);

    var nodeMap = createNodes(graphData.graph.nodes, elementFactory, canvas, textRenderer, root, modeling);

    createEdges(graphData.graph.edges, nodeMap, elementFactory, canvas, commandStack, root, textRenderer, modeling);
  };

  function createNodes(nodes, elementFactory, canvas, textRenderer, parent, modeling) {
    return new Map(nodes.map(function(node) {
      var businessObject = node.businessObject || {};
      var id = node.id;

      return [id, createClass(canvas, parent, elementFactory, node, businessObject, textRenderer, modeling)];
    }));
  }
  function createClass(canvas, parent, elementFactory, node, businessObject, textRenderer, modeling) {
    delete node.id;

    return modeling.createClass(node, parent);
  }
  function createEdges(edges, nodeMap, elementFactory, canvas, commandStack, root, textRenderer, modeling) {
    return edges.forEach(function(edge) {
      var attrs = {
        connectionType: edge.type,
        waypoints: getWaypoints(edge, nodeMap)
      };

      assign(attrs, getEdgeLabels(edge));

      var connection = modeling.createConnection(nodeMap.get(edge.source), nodeMap.get(edge.target), attrs, root);
    });
  }
  function addLabel(labelName, fromObject, toObject) {
    if(labelName in fromObject) {
      toObject[labelName] = fromObject[labelName];
    }
  }
  function getEdgeLabels(edge) {
    var labels = {};

    addLabel('sourceName', edge, labels);
    addLabel('targetName', edge, labels);
    addLabel('sourceCardinality', edge, labels);
    addLabel('targetCardinality', edge, labels);

    return labels;
  }
  function getWaypoints(edge, nodeMap) {
    var waypoints;

    if('waypoints' in edge) {
      waypoints = withoutRedundantPoints(edge.waypoints);
    } else {
      waypoints = connectPoints(getRectangleCenterPoint(nodeMap.get(edge.source)), getRectangleCenterPoint(nodeMap.get(edge.target)));
    }

    return roundPoints(waypoints);
  }
  function getRectangleCenterPoint(point) {
    return {
      x: point.x + point.width / 2,
      y: point.y + point.height / 2
    };
  }
  function roundPoints(points) {
    points.forEach(function(point) {
      point.x = Math.round(point.x),
      point.y = Math.round(point.y);
    });

    return points;
  }

  var ImporterModule = {
    umlImporter: [ 'type', UmlImporter ]
  };

  var CoreModule = {
    __depends__: [
      DrawModule,
      ImporterModule
    ]
  };

  var CLASS_PATTERN = /^class /;

  function isClass(fn) {
    return CLASS_PATTERN.test(fn.toString());
  }

  function isArray$1(obj) {
    return Object.prototype.toString.call(obj) === '[object Array]';
  }

  function annotate() {
    var args = Array.prototype.slice.call(arguments);

    if (args.length === 1 && isArray$1(args[0])) {
      args = args[0];
    }

    var fn = args.pop();

    fn.$inject = args;

    return fn;
  }

  // Current limitations:
  // - can't put into "function arg" comments
  // function /* (no parenthesis like this) */ (){}
  // function abc( /* xx (no parenthesis like this) */ a, b) {}
  //
  // Just put the comment before function or inside:
  // /* (((this is fine))) */ function(a, b) {}
  // function abc(a) { /* (((this is fine))) */}
  //
  // - can't reliably auto-annotate constructor; we'll match the
  // first constructor(...) pattern found which may be the one
  // of a nested class, too.

  var CONSTRUCTOR_ARGS = /constructor\s*[^(]*\(\s*([^)]*)\)/m;
  var FN_ARGS = /^function\s*[^(]*\(\s*([^)]*)\)/m;
  var FN_ARG = /\/\*([^*]*)\*\//m;

  function parse$2(fn) {

    if (typeof fn !== 'function') {
      throw new Error('Cannot annotate "' + fn + '". Expected a function!');
    }

    var match = fn.toString().match(isClass(fn) ? CONSTRUCTOR_ARGS : FN_ARGS);

    // may parse class without constructor
    if (!match) {
      return [];
    }

    return match[1] && match[1].split(',').map(function (arg) {
      match = arg.match(FN_ARG);
      return match ? match[1].trim() : arg.trim();
    }) || [];
  }

  function Module() {
    var providers = [];

    this.factory = function (name, factory) {
      providers.push([name, 'factory', factory]);
      return this;
    };

    this.value = function (name, value) {
      providers.push([name, 'value', value]);
      return this;
    };

    this.type = function (name, type) {
      providers.push([name, 'type', type]);
      return this;
    };

    this.forEach = function (iterator) {
      providers.forEach(iterator);
    };
  }

  var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

  function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

  function Injector(modules, parent) {
    parent = parent || {
      get: function get(name, strict) {
        currentlyResolving.push(name);

        if (strict === false) {
          return null;
        } else {
          throw error('No provider for "' + name + '"!');
        }
      }
    };

    var currentlyResolving = [];
    var providers = this._providers = Object.create(parent._providers || null);
    var instances = this._instances = Object.create(null);

    var self = instances.injector = this;

    var error = function error(msg) {
      var stack = currentlyResolving.join(' -> ');
      currentlyResolving.length = 0;
      return new Error(stack ? msg + ' (Resolving: ' + stack + ')' : msg);
    };

    /**
     * Return a named service.
     *
     * @param {String} name
     * @param {Boolean} [strict=true] if false, resolve missing services to null
     *
     * @return {Object}
     */
    var get = function get(name, strict) {
      if (!providers[name] && name.indexOf('.') !== -1) {
        var parts = name.split('.');
        var pivot = get(parts.shift());

        while (parts.length) {
          pivot = pivot[parts.shift()];
        }

        return pivot;
      }

      if (hasProp(instances, name)) {
        return instances[name];
      }

      if (hasProp(providers, name)) {
        if (currentlyResolving.indexOf(name) !== -1) {
          currentlyResolving.push(name);
          throw error('Cannot resolve circular dependency!');
        }

        currentlyResolving.push(name);
        instances[name] = providers[name][0](providers[name][1]);
        currentlyResolving.pop();

        return instances[name];
      }

      return parent.get(name, strict);
    };

    var fnDef = function fnDef(fn) {
      var locals = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : {};

      if (typeof fn !== 'function') {
        if (isArray$1(fn)) {
          fn = annotate(fn.slice());
        } else {
          throw new Error('Cannot invoke "' + fn + '". Expected a function!');
        }
      }

      var inject = fn.$inject || parse$2(fn);
      var dependencies = inject.map(function (dep) {
        if (hasProp(locals, dep)) {
          return locals[dep];
        } else {
          return get(dep);
        }
      });

      return {
        fn: fn,
        dependencies: dependencies
      };
    };

    var instantiate = function instantiate(Type) {
      var _fnDef = fnDef(Type),
          dependencies = _fnDef.dependencies,
          fn = _fnDef.fn;

      return new (Function.prototype.bind.apply(fn, [null].concat(_toConsumableArray(dependencies))))();
    };

    var invoke = function invoke(func, context, locals) {
      var _fnDef2 = fnDef(func, locals),
          dependencies = _fnDef2.dependencies,
          fn = _fnDef2.fn;

      return fn.call.apply(fn, [context].concat(_toConsumableArray(dependencies)));
    };

    var createPrivateInjectorFactory = function createPrivateInjectorFactory(privateChildInjector) {
      return annotate(function (key) {
        return privateChildInjector.get(key);
      });
    };

    var createChild = function createChild(modules, forceNewInstances) {
      if (forceNewInstances && forceNewInstances.length) {
        var fromParentModule = Object.create(null);
        var matchedScopes = Object.create(null);

        var privateInjectorsCache = [];
        var privateChildInjectors = [];
        var privateChildFactories = [];

        var provider;
        var cacheIdx;
        var privateChildInjector;
        var privateChildInjectorFactory;
        for (var name in providers) {
          provider = providers[name];

          if (forceNewInstances.indexOf(name) !== -1) {
            if (provider[2] === 'private') {
              cacheIdx = privateInjectorsCache.indexOf(provider[3]);
              if (cacheIdx === -1) {
                privateChildInjector = provider[3].createChild([], forceNewInstances);
                privateChildInjectorFactory = createPrivateInjectorFactory(privateChildInjector);
                privateInjectorsCache.push(provider[3]);
                privateChildInjectors.push(privateChildInjector);
                privateChildFactories.push(privateChildInjectorFactory);
                fromParentModule[name] = [privateChildInjectorFactory, name, 'private', privateChildInjector];
              } else {
                fromParentModule[name] = [privateChildFactories[cacheIdx], name, 'private', privateChildInjectors[cacheIdx]];
              }
            } else {
              fromParentModule[name] = [provider[2], provider[1]];
            }
            matchedScopes[name] = true;
          }

          if ((provider[2] === 'factory' || provider[2] === 'type') && provider[1].$scope) {
            /* jshint -W083 */
            forceNewInstances.forEach(function (scope) {
              if (provider[1].$scope.indexOf(scope) !== -1) {
                fromParentModule[name] = [provider[2], provider[1]];
                matchedScopes[scope] = true;
              }
            });
          }
        }

        forceNewInstances.forEach(function (scope) {
          if (!matchedScopes[scope]) {
            throw new Error('No provider for "' + scope + '". Cannot use provider from the parent!');
          }
        });

        modules.unshift(fromParentModule);
      }

      return new Injector(modules, self);
    };

    var factoryMap = {
      factory: invoke,
      type: instantiate,
      value: function value(_value) {
        return _value;
      }
    };

    modules.forEach(function (module) {

      function arrayUnwrap(type, value) {
        if (type !== 'value' && isArray$1(value)) {
          value = annotate(value.slice());
        }

        return value;
      }

      // (vojta): handle wrong inputs (modules)
      if (module instanceof Module) {
        module.forEach(function (provider) {
          var name = provider[0];
          var type = provider[1];
          var value = provider[2];

          providers[name] = [factoryMap[type], arrayUnwrap(type, value), type];
        });
      } else if ((typeof module === 'undefined' ? 'undefined' : _typeof(module)) === 'object') {
        if (module.__exports__) {
          var clonedModule = Object.keys(module).reduce(function (m, key) {
            if (key.substring(0, 2) !== '__') {
              m[key] = module[key];
            }
            return m;
          }, Object.create(null));

          var privateInjector = new Injector((module.__modules__ || []).concat([clonedModule]), self);
          var getFromPrivateInjector = annotate(function (key) {
            return privateInjector.get(key);
          });
          module.__exports__.forEach(function (key) {
            providers[key] = [getFromPrivateInjector, key, 'private', privateInjector];
          });
        } else {
          Object.keys(module).forEach(function (name) {
            if (module[name][2] === 'private') {
              providers[name] = module[name];
              return;
            }

            var type = module[name][0];
            var value = module[name][1];

            providers[name] = [factoryMap[type], arrayUnwrap(type, value), type];
          });
        }
      }
    });

    // public API
    this.get = get;
    this.invoke = invoke;
    this.instantiate = instantiate;
    this.createChild = createChild;
  }

  // helpers /////////////////

  function hasProp(obj, prop) {
    return Object.hasOwnProperty.call(obj, prop);
  }

  // apply default renderer with lowest possible priority
  // so that it only kicks in if noone else could render
  var DEFAULT_RENDER_PRIORITY$1 = 1;

  /**
   * The default renderer used for shapes and connections.
   *
   * @param {EventBus} eventBus
   * @param {Styles} styles
   */
  function DefaultRenderer(eventBus, styles) {
    //
    BaseRenderer.call(this, eventBus, DEFAULT_RENDER_PRIORITY$1);

    this.CONNECTION_STYLE = styles.style([ 'no-fill' ], { strokeWidth: 5, stroke: 'fuchsia' });
    this.SHAPE_STYLE = styles.style({ fill: 'white', stroke: 'fuchsia', strokeWidth: 2 });
  }

  inherits_browser(DefaultRenderer, BaseRenderer);


  DefaultRenderer.prototype.canRender = function() {
    return true;
  };

  DefaultRenderer.prototype.drawShape = function drawShape(visuals, element) {

    var rect = create('rect');
    attr$1(rect, {
      x: 0,
      y: 0,
      width: element.width || 0,
      height: element.height || 0
    });
    attr$1(rect, this.SHAPE_STYLE);

    append(visuals, rect);

    return rect;
  };

  DefaultRenderer.prototype.drawConnection = function drawConnection(visuals, connection) {

    var line = createLine(connection.waypoints, this.CONNECTION_STYLE);
    append(visuals, line);

    return line;
  };

  DefaultRenderer.prototype.getShapePath = function getShapePath(shape) {

    var x = shape.x,
        y = shape.y,
        width = shape.width,
        height = shape.height;

    var shapePath = [
      ['M', x, y],
      ['l', width, 0],
      ['l', 0, height],
      ['l', -width, 0],
      ['z']
    ];

    return componentsToPath(shapePath);
  };

  DefaultRenderer.prototype.getConnectionPath = function getConnectionPath(connection) {
    var waypoints = connection.waypoints;

    var idx, point, connectionPath = [];

    for (idx = 0; (point = waypoints[idx]); idx++) {

      // take invisible docking into account
      // when creating the path
      point = point.original || point;

      connectionPath.push([ idx === 0 ? 'M' : 'L', point.x, point.y ]);
    }

    return componentsToPath(connectionPath);
  };


  DefaultRenderer.$inject = [ 'eventBus', 'styles' ];

  /**
   * A component that manages shape styles
   */
  function Styles() {

    var defaultTraits = {

      'no-fill': {
        fill: 'none'
      },
      'no-border': {
        strokeOpacity: 0.0
      },
      'no-events': {
        pointerEvents: 'none'
      }
    };

    var self = this;

    /**
     * Builds a style definition from a className, a list of traits and an object of additional attributes.
     *
     * @param  {String} className
     * @param  {Array<String>} traits
     * @param  {Object} additionalAttrs
     *
     * @return {Object} the style defintion
     */
    this.cls = function(className, traits, additionalAttrs) {
      var attrs = this.style(traits, additionalAttrs);

      return assign(attrs, { 'class': className });
    };

    /**
     * Builds a style definition from a list of traits and an object of additional attributes.
     *
     * @param  {Array<String>} traits
     * @param  {Object} additionalAttrs
     *
     * @return {Object} the style defintion
     */
    this.style = function(traits, additionalAttrs) {

      if (!isArray(traits) && !additionalAttrs) {
        additionalAttrs = traits;
        traits = [];
      }

      var attrs = reduce(traits, function(attrs, t) {
        return assign(attrs, defaultTraits[t] || {});
      }, {});

      return additionalAttrs ? assign(attrs, additionalAttrs) : attrs;
    };

    this.computeStyle = function(custom, traits, defaultStyles) {
      if (!isArray(traits)) {
        defaultStyles = traits;
        traits = [];
      }

      return self.style(traits || [], assign({}, defaultStyles, custom || {}));
    };
  }

  var DrawModule$1 = {
    __init__: [ 'defaultRenderer' ],
    defaultRenderer: [ 'type', DefaultRenderer ],
    styles: [ 'type', Styles ]
  };

  /**
   * Failsafe remove an element from a collection
   *
   * @param  {Array<Object>} [collection]
   * @param  {Object} [element]
   *
   * @return {Number} the previous index of the element
   */
  function remove$2(collection, element) {

    if (!collection || !element) {
      return -1;
    }

    var idx = collection.indexOf(element);

    if (idx !== -1) {
      collection.splice(idx, 1);
    }

    return idx;
  }

  /**
   * Fail save add an element to the given connection, ensuring
   * it does not yet exist.
   *
   * @param {Array<Object>} collection
   * @param {Object} element
   * @param {Number} idx
   */
  function add(collection, element, idx) {

    if (!collection || !element) {
      return;
    }

    if (typeof idx !== 'number') {
      idx = -1;
    }

    var currentIdx = collection.indexOf(element);

    if (currentIdx !== -1) {

      if (currentIdx === idx) {
        // nothing to do, position has not changed
        return;
      } else {

        if (idx !== -1) {
          // remove from current position
          collection.splice(currentIdx, 1);
        } else {
          // already exists in collection
          return;
        }
      }
    }

    if (idx !== -1) {
      // insert at specified position
      collection.splice(idx, 0, element);
    } else {
      // push to end
      collection.push(element);
    }
  }

  function getType(element) {

    if ('waypoints' in element) {
      return 'connection';
    }

    if ('x' in element) {
      return 'shape';
    }

    return 'root';
  }

  function round$1(number, resolution) {
    return Math.round(number * resolution) / resolution;
  }

  function ensurePx(number) {
    return isNumber(number) ? number + 'px' : number;
  }

  /**
   * Creates a HTML container element for a SVG element with
   * the given configuration
   *
   * @param  {Object} options
   * @return {HTMLElement} the container element
   */
  function createContainer(options) {

    options = assign({}, { width: '100%', height: '100%' }, options);

    var container = options.container || document.body;

    // create a <div> around the svg element with the respective size
    // this way we can always get the correct container size
    // (this is impossible for <svg> elements at the moment)
    var parent = document.createElement('div');
    parent.setAttribute('class', 'djs-container');

    assign(parent.style, {
      position: 'relative',
      overflow: 'hidden',
      width: ensurePx(options.width),
      height: ensurePx(options.height)
    });

    container.appendChild(parent);

    return parent;
  }

  function createGroup(parent, cls, childIndex) {
    var group = create('g');
    classes$1(group).add(cls);

    var index = childIndex !== undefined ? childIndex : parent.childNodes.length - 1;

    // must ensure second argument is node or _null_
    // cf. https://developer.mozilla.org/en-US/docs/Web/API/Node/insertBefore
    parent.insertBefore(group, parent.childNodes[index] || null);

    return group;
  }

  var BASE_LAYER = 'base';


  var REQUIRED_MODEL_ATTRS = {
    shape: [ 'x', 'y', 'width', 'height' ],
    connection: [ 'waypoints' ]
  };

  /**
   * The main drawing canvas.
   *
   * @class
   * @constructor
   *
   * @emits Canvas#canvas.init
   *
   * @param {Object} config
   * @param {EventBus} eventBus
   * @param {GraphicsFactory} graphicsFactory
   * @param {ElementRegistry} elementRegistry
   */
  function Canvas(config, eventBus, graphicsFactory, elementRegistry) {

    this._eventBus = eventBus;
    this._elementRegistry = elementRegistry;
    this._graphicsFactory = graphicsFactory;

    this._init(config || {});
  }

  Canvas.$inject = [
    'config.canvas',
    'eventBus',
    'graphicsFactory',
    'elementRegistry'
  ];


  Canvas.prototype._init = function(config) {

    var eventBus = this._eventBus;

    // Creates a <svg> element that is wrapped into a <div>.
    // This way we are always able to correctly figure out the size of the svg element
    // by querying the parent node.
    //
    // (It is not possible to get the size of a svg element cross browser @ 2014-04-01)
    //
    // <div class="djs-container" style="width: {desired-width}, height: {desired-height}">
    //   <svg width="100%" height="100%">
    //    ...
    //   </svg>
    // </div>

    // html container
    var container = this._container = createContainer(config);

    var svg = this._svg = create('svg');
    attr$1(svg, { width: '100%', height: '100%' });

    append(container, svg);

    var viewport = this._viewport = createGroup(svg, 'viewport');

    this._layers = {};

    // debounce canvas.viewbox.changed events
    // for smoother diagram interaction
    if (config.deferUpdate !== false) {
      this._viewboxChanged = debounce(bind$2(this._viewboxChanged, this), 300);
    }

    eventBus.on('diagram.init', function() {

      /**
       * An event indicating that the canvas is ready to be drawn on.
       *
       * @memberOf Canvas
       *
       * @event canvas.init
       *
       * @type {Object}
       * @property {SVGElement} svg the created svg element
       * @property {SVGElement} viewport the direct parent of diagram elements and shapes
       */
      eventBus.fire('canvas.init', {
        svg: svg,
        viewport: viewport
      });

    }, this);

    // reset viewbox on shape changes to
    // recompute the viewbox
    eventBus.on([
      'shape.added',
      'connection.added',
      'shape.removed',
      'connection.removed',
      'elements.changed'
    ], function() {
      delete this._cachedViewbox;
    }, this);

    eventBus.on('diagram.destroy', 500, this._destroy, this);
    eventBus.on('diagram.clear', 500, this._clear, this);
  };

  Canvas.prototype._destroy = function(emit) {
    this._eventBus.fire('canvas.destroy', {
      svg: this._svg,
      viewport: this._viewport
    });

    var parent = this._container.parentNode;

    if (parent) {
      parent.removeChild(this._container);
    }

    delete this._svg;
    delete this._container;
    delete this._layers;
    delete this._rootElement;
    delete this._viewport;
  };

  Canvas.prototype._clear = function() {

    var self = this;

    var allElements = this._elementRegistry.getAll();

    // remove all elements
    allElements.forEach(function(element) {
      var type = getType(element);

      if (type === 'root') {
        self.setRootElement(null, true);
      } else {
        self._removeElement(element, type);
      }
    });

    // force recomputation of view box
    delete this._cachedViewbox;
  };

  /**
   * Returns the default layer on which
   * all elements are drawn.
   *
   * @returns {SVGElement}
   */
  Canvas.prototype.getDefaultLayer = function() {
    return this.getLayer(BASE_LAYER, 0);
  };

  /**
   * Returns a layer that is used to draw elements
   * or annotations on it.
   *
   * Non-existing layers retrieved through this method
   * will be created. During creation, the optional index
   * may be used to create layers below or above existing layers.
   * A layer with a certain index is always created above all
   * existing layers with the same index.
   *
   * @param {String} name
   * @param {Number} index
   *
   * @returns {SVGElement}
   */
  Canvas.prototype.getLayer = function(name, index) {

    if (!name) {
      throw new Error('must specify a name');
    }

    var layer = this._layers[name];

    if (!layer) {
      layer = this._layers[name] = this._createLayer(name, index);
    }

    // throw an error if layer creation / retrival is
    // requested on different index
    if (typeof index !== 'undefined' && layer.index !== index) {
      throw new Error('layer <' + name + '> already created at index <' + index + '>');
    }

    return layer.group;
  };

  /**
   * Creates a given layer and returns it.
   *
   * @param {String} name
   * @param {Number} [index=0]
   *
   * @return {Object} layer descriptor with { index, group: SVGGroup }
   */
  Canvas.prototype._createLayer = function(name, index) {

    if (!index) {
      index = 0;
    }

    var childIndex = reduce(this._layers, function(childIndex, layer) {
      if (index >= layer.index) {
        childIndex++;
      }

      return childIndex;
    }, 0);

    return {
      group: createGroup(this._viewport, 'layer-' + name, childIndex),
      index: index
    };

  };

  /**
   * Returns the html element that encloses the
   * drawing canvas.
   *
   * @return {DOMNode}
   */
  Canvas.prototype.getContainer = function() {
    return this._container;
  };


  // markers //////////////////////

  Canvas.prototype._updateMarker = function(element, marker, add$$1) {
    var container;

    if (!element.id) {
      element = this._elementRegistry.get(element);
    }

    // we need to access all
    container = this._elementRegistry._elements[element.id];

    if (!container) {
      return;
    }

    forEach([ container.gfx, container.secondaryGfx ], function(gfx) {
      if (gfx) {
        // invoke either addClass or removeClass based on mode
        if (add$$1) {
          classes$1(gfx).add(marker);
        } else {
          classes$1(gfx).remove(marker);
        }
      }
    });

    /**
     * An event indicating that a marker has been updated for an element
     *
     * @event element.marker.update
     * @type {Object}
     * @property {djs.model.Element} element the shape
     * @property {Object} gfx the graphical representation of the shape
     * @property {String} marker
     * @property {Boolean} add true if the marker was added, false if it got removed
     */
    this._eventBus.fire('element.marker.update', { element: element, gfx: container.gfx, marker: marker, add: !!add$$1 });
  };


  /**
   * Adds a marker to an element (basically a css class).
   *
   * Fires the element.marker.update event, making it possible to
   * integrate extension into the marker life-cycle, too.
   *
   * @example
   * canvas.addMarker('foo', 'some-marker');
   *
   * var fooGfx = canvas.getGraphics('foo');
   *
   * fooGfx; // <g class="... some-marker"> ... </g>
   *
   * @param {String|djs.model.Base} element
   * @param {String} marker
   */
  Canvas.prototype.addMarker = function(element, marker) {
    this._updateMarker(element, marker, true);
  };


  /**
   * Remove a marker from an element.
   *
   * Fires the element.marker.update event, making it possible to
   * integrate extension into the marker life-cycle, too.
   *
   * @param  {String|djs.model.Base} element
   * @param  {String} marker
   */
  Canvas.prototype.removeMarker = function(element, marker) {
    this._updateMarker(element, marker, false);
  };

  /**
   * Check the existence of a marker on element.
   *
   * @param  {String|djs.model.Base} element
   * @param  {String} marker
   */
  Canvas.prototype.hasMarker = function(element, marker) {
    if (!element.id) {
      element = this._elementRegistry.get(element);
    }

    var gfx = this.getGraphics(element);

    return classes$1(gfx).has(marker);
  };

  /**
   * Toggles a marker on an element.
   *
   * Fires the element.marker.update event, making it possible to
   * integrate extension into the marker life-cycle, too.
   *
   * @param  {String|djs.model.Base} element
   * @param  {String} marker
   */
  Canvas.prototype.toggleMarker = function(element, marker) {
    if (this.hasMarker(element, marker)) {
      this.removeMarker(element, marker);
    } else {
      this.addMarker(element, marker);
    }
  };

  Canvas.prototype.getRootElement = function() {
    if (!this._rootElement) {
      this.setRootElement({ id: '__implicitroot', children: [] });
    }

    return this._rootElement;
  };



  // root element handling //////////////////////

  /**
   * Sets a given element as the new root element for the canvas
   * and returns the new root element.
   *
   * @param {Object|djs.model.Root} element
   * @param {Boolean} [override] whether to override the current root element, if any
   *
   * @return {Object|djs.model.Root} new root element
   */
  Canvas.prototype.setRootElement = function(element, override) {

    if (element) {
      this._ensureValid('root', element);
    }

    var currentRoot = this._rootElement,
        elementRegistry = this._elementRegistry,
        eventBus = this._eventBus;

    if (currentRoot) {
      if (!override) {
        throw new Error('rootElement already set, need to specify override');
      }

      // simulate element remove event sequence
      eventBus.fire('root.remove', { element: currentRoot });
      eventBus.fire('root.removed', { element: currentRoot });

      elementRegistry.remove(currentRoot);
    }

    if (element) {
      var gfx = this.getDefaultLayer();

      // resemble element add event sequence
      eventBus.fire('root.add', { element: element });

      elementRegistry.add(element, gfx, this._svg);

      eventBus.fire('root.added', { element: element, gfx: gfx });
    }

    this._rootElement = element;

    return element;
  };



  // add functionality //////////////////////

  Canvas.prototype._ensureValid = function(type, element) {
    if (!element.id) {
      throw new Error('element must have an id');
    }

    if (this._elementRegistry.get(element.id)) {
      throw new Error('element with id ' + element.id + ' already exists');
    }

    var requiredAttrs = REQUIRED_MODEL_ATTRS[type];

    var valid = every(requiredAttrs, function(attr$$1) {
      return typeof element[attr$$1] !== 'undefined';
    });

    if (!valid) {
      throw new Error(
        'must supply { ' + requiredAttrs.join(', ') + ' } with ' + type);
    }
  };

  Canvas.prototype._setParent = function(element, parent, parentIndex) {
    add(parent.children, element, parentIndex);
    element.parent = parent;
  };

  /**
   * Adds an element to the canvas.
   *
   * This wires the parent <-> child relationship between the element and
   * a explicitly specified parent or an implicit root element.
   *
   * During add it emits the events
   *
   *  * <{type}.add> (element, parent)
   *  * <{type}.added> (element, gfx)
   *
   * Extensions may hook into these events to perform their magic.
   *
   * @param {String} type
   * @param {Object|djs.model.Base} element
   * @param {Object|djs.model.Base} [parent]
   * @param {Number} [parentIndex]
   *
   * @return {Object|djs.model.Base} the added element
   */
  Canvas.prototype._addElement = function(type, element, parent, parentIndex) {

    parent = parent || this.getRootElement();

    var eventBus = this._eventBus,
        graphicsFactory = this._graphicsFactory;

    this._ensureValid(type, element);

    eventBus.fire(type + '.add', { element: element, parent: parent });

    this._setParent(element, parent, parentIndex);

    // create graphics
    var gfx = graphicsFactory.create(type, element, parentIndex);

    this._elementRegistry.add(element, gfx);

    // update its visual
    graphicsFactory.update(type, element, gfx);

    eventBus.fire(type + '.added', { element: element, gfx: gfx });

    return element;
  };

  /**
   * Adds a shape to the canvas
   *
   * @param {Object|djs.model.Shape} shape to add to the diagram
   * @param {djs.model.Base} [parent]
   * @param {Number} [parentIndex]
   *
   * @return {djs.model.Shape} the added shape
   */
  Canvas.prototype.addShape = function(shape, parent, parentIndex) {
    return this._addElement('shape', shape, parent, parentIndex);
  };

  /**
   * Adds a connection to the canvas
   *
   * @param {Object|djs.model.Connection} connection to add to the diagram
   * @param {djs.model.Base} [parent]
   * @param {Number} [parentIndex]
   *
   * @return {djs.model.Connection} the added connection
   */
  Canvas.prototype.addConnection = function(connection, parent, parentIndex) {
    return this._addElement('connection', connection, parent, parentIndex);
  };


  /**
   * Internal remove element
   */
  Canvas.prototype._removeElement = function(element, type) {

    var elementRegistry = this._elementRegistry,
        graphicsFactory = this._graphicsFactory,
        eventBus = this._eventBus;

    element = elementRegistry.get(element.id || element);

    if (!element) {
      // element was removed already
      return;
    }

    eventBus.fire(type + '.remove', { element: element });

    graphicsFactory.remove(element);

    // unset parent <-> child relationship
    remove$2(element.parent && element.parent.children, element);
    element.parent = null;

    eventBus.fire(type + '.removed', { element: element });

    elementRegistry.remove(element);

    return element;
  };


  /**
   * Removes a shape from the canvas
   *
   * @param {String|djs.model.Shape} shape or shape id to be removed
   *
   * @return {djs.model.Shape} the removed shape
   */
  Canvas.prototype.removeShape = function(shape) {

    /**
     * An event indicating that a shape is about to be removed from the canvas.
     *
     * @memberOf Canvas
     *
     * @event shape.remove
     * @type {Object}
     * @property {djs.model.Shape} element the shape descriptor
     * @property {Object} gfx the graphical representation of the shape
     */

    /**
     * An event indicating that a shape has been removed from the canvas.
     *
     * @memberOf Canvas
     *
     * @event shape.removed
     * @type {Object}
     * @property {djs.model.Shape} element the shape descriptor
     * @property {Object} gfx the graphical representation of the shape
     */
    return this._removeElement(shape, 'shape');
  };


  /**
   * Removes a connection from the canvas
   *
   * @param {String|djs.model.Connection} connection or connection id to be removed
   *
   * @return {djs.model.Connection} the removed connection
   */
  Canvas.prototype.removeConnection = function(connection) {

    /**
     * An event indicating that a connection is about to be removed from the canvas.
     *
     * @memberOf Canvas
     *
     * @event connection.remove
     * @type {Object}
     * @property {djs.model.Connection} element the connection descriptor
     * @property {Object} gfx the graphical representation of the connection
     */

    /**
     * An event indicating that a connection has been removed from the canvas.
     *
     * @memberOf Canvas
     *
     * @event connection.removed
     * @type {Object}
     * @property {djs.model.Connection} element the connection descriptor
     * @property {Object} gfx the graphical representation of the connection
     */
    return this._removeElement(connection, 'connection');
  };


  /**
   * Return the graphical object underlaying a certain diagram element
   *
   * @param {String|djs.model.Base} element descriptor of the element
   * @param {Boolean} [secondary=false] whether to return the secondary connected element
   *
   * @return {SVGElement}
   */
  Canvas.prototype.getGraphics = function(element, secondary) {
    return this._elementRegistry.getGraphics(element, secondary);
  };


  /**
   * Perform a viewbox update via a given change function.
   *
   * @param {Function} changeFn
   */
  Canvas.prototype._changeViewbox = function(changeFn) {

    // notify others of the upcoming viewbox change
    this._eventBus.fire('canvas.viewbox.changing');

    // perform actual change
    changeFn.apply(this);

    // reset the cached viewbox so that
    // a new get operation on viewbox or zoom
    // triggers a viewbox re-computation
    this._cachedViewbox = null;

    // notify others of the change; this step
    // may or may not be debounced
    this._viewboxChanged();
  };

  Canvas.prototype._viewboxChanged = function() {
    this._eventBus.fire('canvas.viewbox.changed', { viewbox: this.viewbox() });
  };


  /**
   * Gets or sets the view box of the canvas, i.e. the
   * area that is currently displayed.
   *
   * The getter may return a cached viewbox (if it is currently
   * changing). To force a recomputation, pass `false` as the first argument.
   *
   * @example
   *
   * canvas.viewbox({ x: 100, y: 100, width: 500, height: 500 })
   *
   * // sets the visible area of the diagram to (100|100) -> (600|100)
   * // and and scales it according to the diagram width
   *
   * var viewbox = canvas.viewbox(); // pass `false` to force recomputing the box.
   *
   * console.log(viewbox);
   * // {
   * //   inner: Dimensions,
   * //   outer: Dimensions,
   * //   scale,
   * //   x, y,
   * //   width, height
   * // }
   *
   * // if the current diagram is zoomed and scrolled, you may reset it to the
   * // default zoom via this method, too:
   *
   * var zoomedAndScrolledViewbox = canvas.viewbox();
   *
   * canvas.viewbox({
   *   x: 0,
   *   y: 0,
   *   width: zoomedAndScrolledViewbox.outer.width,
   *   height: zoomedAndScrolledViewbox.outer.height
   * });
   *
   * @param  {Object} [box] the new view box to set
   * @param  {Number} box.x the top left X coordinate of the canvas visible in view box
   * @param  {Number} box.y the top left Y coordinate of the canvas visible in view box
   * @param  {Number} box.width the visible width
   * @param  {Number} box.height
   *
   * @return {Object} the current view box
   */
  Canvas.prototype.viewbox = function(box) {

    if (box === undefined && this._cachedViewbox) {
      return this._cachedViewbox;
    }

    var viewport = this._viewport,
        innerBox,
        outerBox = this.getSize(),
        matrix,
        transform$$1,
        scale,
        x, y;

    if (!box) {
      // compute the inner box based on the
      // diagrams default layer. This allows us to exclude
      // external components, such as overlays
      innerBox = this.getDefaultLayer().getBBox();

      transform$$1 = transform(viewport);
      matrix = transform$$1 ? transform$$1.matrix : createMatrix();
      scale = round$1(matrix.a, 1000);

      x = round$1(-matrix.e || 0, 1000);
      y = round$1(-matrix.f || 0, 1000);

      box = this._cachedViewbox = {
        x: x ? x / scale : 0,
        y: y ? y / scale : 0,
        width: outerBox.width / scale,
        height: outerBox.height / scale,
        scale: scale,
        inner: {
          width: innerBox.width,
          height: innerBox.height,
          x: innerBox.x,
          y: innerBox.y
        },
        outer: outerBox
      };

      return box;
    } else {

      this._changeViewbox(function() {
        scale = Math.min(outerBox.width / box.width, outerBox.height / box.height);

        var matrix = this._svg.createSVGMatrix()
          .scale(scale)
          .translate(-box.x, -box.y);

        transform(viewport, matrix);
      });
    }

    return box;
  };


  /**
   * Gets or sets the scroll of the canvas.
   *
   * @param {Object} [delta] the new scroll to apply.
   *
   * @param {Number} [delta.dx]
   * @param {Number} [delta.dy]
   */
  Canvas.prototype.scroll = function(delta) {

    var node = this._viewport;
    var matrix = node.getCTM();

    if (delta) {
      this._changeViewbox(function() {
        delta = assign({ dx: 0, dy: 0 }, delta || {});

        matrix = this._svg.createSVGMatrix().translate(delta.dx, delta.dy).multiply(matrix);

        setCTM(node, matrix);
      });
    }

    return { x: matrix.e, y: matrix.f };
  };


  /**
   * Gets or sets the current zoom of the canvas, optionally zooming
   * to the specified position.
   *
   * The getter may return a cached zoom level. Call it with `false` as
   * the first argument to force recomputation of the current level.
   *
   * @param {String|Number} [newScale] the new zoom level, either a number, i.e. 0.9,
   *                                   or `fit-viewport` to adjust the size to fit the current viewport
   * @param {String|Point} [center] the reference point { x: .., y: ..} to zoom to, 'auto' to zoom into mid or null
   *
   * @return {Number} the current scale
   */
  Canvas.prototype.zoom = function(newScale, center) {

    if (!newScale) {
      return this.viewbox(newScale).scale;
    }

    if (newScale === 'fit-viewport') {
      return this._fitViewport(center);
    }

    var outer,
        matrix;

    this._changeViewbox(function() {

      if (typeof center !== 'object') {
        outer = this.viewbox().outer;

        center = {
          x: outer.width / 2,
          y: outer.height / 2
        };
      }

      matrix = this._setZoom(newScale, center);
    });

    return round$1(matrix.a, 1000);
  };

  function setCTM(node, m) {
    var mstr = 'matrix(' + m.a + ',' + m.b + ',' + m.c + ',' + m.d + ',' + m.e + ',' + m.f + ')';
    node.setAttribute('transform', mstr);
  }

  Canvas.prototype._fitViewport = function(center) {

    var vbox = this.viewbox(),
        outer = vbox.outer,
        inner = vbox.inner,
        newScale,
        newViewbox;

    // display the complete diagram without zooming in.
    // instead of relying on internal zoom, we perform a
    // hard reset on the canvas viewbox to realize this
    //
    // if diagram does not need to be zoomed in, we focus it around
    // the diagram origin instead

    if (inner.x >= 0 &&
        inner.y >= 0 &&
        inner.x + inner.width <= outer.width &&
        inner.y + inner.height <= outer.height &&
        !center) {

      newViewbox = {
        x: 0,
        y: 0,
        width: Math.max(inner.width + inner.x, outer.width),
        height: Math.max(inner.height + inner.y, outer.height)
      };
    } else {

      newScale = Math.min(1, outer.width / inner.width, outer.height / inner.height);
      newViewbox = {
        x: inner.x + (center ? inner.width / 2 - outer.width / newScale / 2 : 0),
        y: inner.y + (center ? inner.height / 2 - outer.height / newScale / 2 : 0),
        width: outer.width / newScale,
        height: outer.height / newScale
      };
    }

    this.viewbox(newViewbox);

    return this.viewbox(false).scale;
  };


  Canvas.prototype._setZoom = function(scale, center) {

    var svg = this._svg,
        viewport = this._viewport;

    var matrix = svg.createSVGMatrix();
    var point = svg.createSVGPoint();

    var centerPoint,
        originalPoint,
        currentMatrix,
        scaleMatrix,
        newMatrix;

    currentMatrix = viewport.getCTM();

    var currentScale = currentMatrix.a;

    if (center) {
      centerPoint = assign(point, center);

      // revert applied viewport transformations
      originalPoint = centerPoint.matrixTransform(currentMatrix.inverse());

      // create scale matrix
      scaleMatrix = matrix
        .translate(originalPoint.x, originalPoint.y)
        .scale(1 / currentScale * scale)
        .translate(-originalPoint.x, -originalPoint.y);

      newMatrix = currentMatrix.multiply(scaleMatrix);
    } else {
      newMatrix = matrix.scale(scale);
    }

    setCTM(this._viewport, newMatrix);

    return newMatrix;
  };


  /**
   * Returns the size of the canvas
   *
   * @return {Dimensions}
   */
  Canvas.prototype.getSize = function() {
    return {
      width: this._container.clientWidth,
      height: this._container.clientHeight
    };
  };


  /**
   * Return the absolute bounding box for the given element
   *
   * The absolute bounding box may be used to display overlays in the
   * callers (browser) coordinate system rather than the zoomed in/out
   * canvas coordinates.
   *
   * @param  {ElementDescriptor} element
   * @return {Bounds} the absolute bounding box
   */
  Canvas.prototype.getAbsoluteBBox = function(element) {
    var vbox = this.viewbox();
    var bbox;

    // connection
    // use svg bbox
    if (element.waypoints) {
      var gfx = this.getGraphics(element);

      bbox = gfx.getBBox();
    }
    // shapes
    // use data
    else {
      bbox = element;
    }

    var x = bbox.x * vbox.scale - vbox.x * vbox.scale;
    var y = bbox.y * vbox.scale - vbox.y * vbox.scale;

    var width = bbox.width * vbox.scale;
    var height = bbox.height * vbox.scale;

    return {
      x: x,
      y: y,
      width: width,
      height: height
    };
  };

  /**
   * Fires an event in order other modules can react to the
   * canvas resizing
   */
  Canvas.prototype.resized = function() {

    // force recomputation of view box
    delete this._cachedViewbox;

    this._eventBus.fire('canvas.resized');
  };

  var ELEMENT_ID = 'data-element-id';


  /**
   * @class
   *
   * A registry that keeps track of all shapes in the diagram.
   */
  function ElementRegistry(eventBus) {
    this._elements = {};

    this._eventBus = eventBus;
  }

  ElementRegistry.$inject = [ 'eventBus' ];

  /**
   * Register a pair of (element, gfx, (secondaryGfx)).
   *
   * @param {djs.model.Base} element
   * @param {SVGElement} gfx
   * @param {SVGElement} [secondaryGfx] optional other element to register, too
   */
  ElementRegistry.prototype.add = function(element, gfx, secondaryGfx) {

    var id = element.id;

    this._validateId(id);

    // associate dom node with element
    attr$1(gfx, ELEMENT_ID, id);

    if (secondaryGfx) {
      attr$1(secondaryGfx, ELEMENT_ID, id);
    }

    this._elements[id] = { element: element, gfx: gfx, secondaryGfx: secondaryGfx };
  };

  /**
   * Removes an element from the registry.
   *
   * @param {djs.model.Base} element
   */
  ElementRegistry.prototype.remove = function(element) {
    var elements = this._elements,
        id = element.id || element,
        container = id && elements[id];

    if (container) {

      // unset element id on gfx
      attr$1(container.gfx, ELEMENT_ID, '');

      if (container.secondaryGfx) {
        attr$1(container.secondaryGfx, ELEMENT_ID, '');
      }

      delete elements[id];
    }
  };

  /**
   * Update the id of an element
   *
   * @param {djs.model.Base} element
   * @param {String} newId
   */
  ElementRegistry.prototype.updateId = function(element, newId) {

    this._validateId(newId);

    if (typeof element === 'string') {
      element = this.get(element);
    }

    this._eventBus.fire('element.updateId', {
      element: element,
      newId: newId
    });

    var gfx = this.getGraphics(element),
        secondaryGfx = this.getGraphics(element, true);

    this.remove(element);

    element.id = newId;

    this.add(element, gfx, secondaryGfx);
  };

  /**
   * Return the model element for a given id or graphics.
   *
   * @example
   *
   * elementRegistry.get('SomeElementId_1');
   * elementRegistry.get(gfx);
   *
   *
   * @param {String|SVGElement} filter for selecting the element
   *
   * @return {djs.model.Base}
   */
  ElementRegistry.prototype.get = function(filter) {
    var id;

    if (typeof filter === 'string') {
      id = filter;
    } else {
      id = filter && attr$1(filter, ELEMENT_ID);
    }

    var container = this._elements[id];
    return container && container.element;
  };

  /**
   * Return all elements that match a given filter function.
   *
   * @param {Function} fn
   *
   * @return {Array<djs.model.Base>}
   */
  ElementRegistry.prototype.filter = function(fn) {

    var filtered = [];

    this.forEach(function(element, gfx) {
      if (fn(element, gfx)) {
        filtered.push(element);
      }
    });

    return filtered;
  };

  /**
   * Return all rendered model elements.
   *
   * @return {Array<djs.model.Base>}
   */
  ElementRegistry.prototype.getAll = function() {
    return this.filter(function(e) { return e; });
  };

  /**
   * Iterate over all diagram elements.
   *
   * @param {Function} fn
   */
  ElementRegistry.prototype.forEach = function(fn) {

    var map = this._elements;

    Object.keys(map).forEach(function(id) {
      var container = map[id],
          element = container.element,
          gfx = container.gfx;

      return fn(element, gfx);
    });
  };

  /**
   * Return the graphical representation of an element or its id.
   *
   * @example
   * elementRegistry.getGraphics('SomeElementId_1');
   * elementRegistry.getGraphics(rootElement); // <g ...>
   *
   * elementRegistry.getGraphics(rootElement, true); // <svg ...>
   *
   *
   * @param {String|djs.model.Base} filter
   * @param {Boolean} [secondary=false] whether to return the secondary connected element
   *
   * @return {SVGElement}
   */
  ElementRegistry.prototype.getGraphics = function(filter, secondary) {
    var id = filter.id || filter;

    var container = this._elements[id];
    return container && (secondary ? container.secondaryGfx : container.gfx);
  };

  /**
   * Validate the suitability of the given id and signals a problem
   * with an exception.
   *
   * @param {String} id
   *
   * @throws {Error} if id is empty or already assigned
   */
  ElementRegistry.prototype._validateId = function(id) {
    if (!id) {
      throw new Error('element must have an id');
    }

    if (this._elements[id]) {
      throw new Error('element with id ' + id + ' already added');
    }
  };

  /**
   * An empty collection stub. Use {@link RefsCollection.extend} to extend a
   * collection with ref semantics.
   *
   * @class RefsCollection
   */

  /**
   * Extends a collection with {@link Refs} aware methods
   *
   * @memberof RefsCollection
   * @static
   *
   * @param  {Array<Object>} collection
   * @param  {Refs} refs instance
   * @param  {Object} property represented by the collection
   * @param  {Object} target object the collection is attached to
   *
   * @return {RefsCollection<Object>} the extended array
   */
  function extend$1(collection, refs, property, target) {

    var inverseProperty = property.inverse;

    /**
     * Removes the given element from the array and returns it.
     *
     * @method RefsCollection#remove
     *
     * @param {Object} element the element to remove
     */
    Object.defineProperty(collection, 'remove', {
      value: function(element) {
        var idx = this.indexOf(element);
        if (idx !== -1) {
          this.splice(idx, 1);

          // unset inverse
          refs.unset(element, inverseProperty, target);
        }

        return element;
      }
    });

    /**
     * Returns true if the collection contains the given element
     *
     * @method RefsCollection#contains
     *
     * @param {Object} element the element to check for
     */
    Object.defineProperty(collection, 'contains', {
      value: function(element) {
        return this.indexOf(element) !== -1;
      }
    });

    /**
     * Adds an element to the array, unless it exists already (set semantics).
     *
     * @method RefsCollection#add
     *
     * @param {Object} element the element to add
     * @param {Number} optional index to add element to
     *                 (possibly moving other elements around)
     */
    Object.defineProperty(collection, 'add', {
      value: function(element, idx) {

        var currentIdx = this.indexOf(element);

        if (typeof idx === 'undefined') {

          if (currentIdx !== -1) {
            // element already in collection (!)
            return;
          }

          // add to end of array, as no idx is specified
          idx = this.length;
        }

        // handle already in collection
        if (currentIdx !== -1) {

          // remove element from currentIdx
          this.splice(currentIdx, 1);
        }

        // add element at idx
        this.splice(idx, 0, element);

        if (currentIdx === -1) {
          // set inverse, unless element was
          // in collection already
          refs.set(element, inverseProperty, target);
        }
      }
    });

    // a simple marker, identifying this element
    // as being a refs collection
    Object.defineProperty(collection, '__refs_collection', {
      value: true
    });

    return collection;
  }


  function isExtended(collection) {
    return collection.__refs_collection === true;
  }

  var extend_1 = extend$1;

  var isExtended_1 = isExtended;

  var collection = {
  	extend: extend_1,
  	isExtended: isExtended_1
  };

  function hasOwnProperty(e, property) {
    return Object.prototype.hasOwnProperty.call(e, property.name || property);
  }

  function defineCollectionProperty(ref, property, target) {

    var collection$$1 = collection.extend(target[property.name] || [], ref, property, target);

    Object.defineProperty(target, property.name, {
      enumerable: property.enumerable,
      value: collection$$1
    });

    if (collection$$1.length) {

      collection$$1.forEach(function(o) {
        ref.set(o, property.inverse, target);
      });
    }
  }


  function defineProperty(ref, property, target) {

    var inverseProperty = property.inverse;

    var _value = target[property.name];

    Object.defineProperty(target, property.name, {
      configurable: property.configurable,
      enumerable: property.enumerable,

      get: function() {
        return _value;
      },

      set: function(value) {

        // return if we already performed all changes
        if (value === _value) {
          return;
        }

        var old = _value;

        // temporary set null
        _value = null;

        if (old) {
          ref.unset(old, inverseProperty, target);
        }

        // set new value
        _value = value;

        // set inverse value
        ref.set(_value, inverseProperty, target);
      }
    });

  }

  /**
   * Creates a new references object defining two inversly related
   * attribute descriptors a and b.
   *
   * <p>
   *   When bound to an object using {@link Refs#bind} the references
   *   get activated and ensure that add and remove operations are applied
   *   reversely, too.
   * </p>
   *
   * <p>
   *   For attributes represented as collections {@link Refs} provides the
   *   {@link RefsCollection#add}, {@link RefsCollection#remove} and {@link RefsCollection#contains} extensions
   *   that must be used to properly hook into the inverse change mechanism.
   * </p>
   *
   * @class Refs
   *
   * @classdesc A bi-directional reference between two attributes.
   *
   * @param {Refs.AttributeDescriptor} a property descriptor
   * @param {Refs.AttributeDescriptor} b property descriptor
   *
   * @example
   *
   * var refs = Refs({ name: 'wheels', collection: true, enumerable: true }, { name: 'car' });
   *
   * var car = { name: 'toyota' };
   * var wheels = [{ pos: 'front-left' }, { pos: 'front-right' }];
   *
   * refs.bind(car, 'wheels');
   *
   * car.wheels // []
   * car.wheels.add(wheels[0]);
   * car.wheels.add(wheels[1]);
   *
   * car.wheels // [{ pos: 'front-left' }, { pos: 'front-right' }]
   *
   * wheels[0].car // { name: 'toyota' };
   * car.wheels.remove(wheels[0]);
   *
   * wheels[0].car // undefined
   */
  function Refs(a, b) {

    if (!(this instanceof Refs)) {
      return new Refs(a, b);
    }

    // link
    a.inverse = b;
    b.inverse = a;

    this.props = {};
    this.props[a.name] = a;
    this.props[b.name] = b;
  }

  /**
   * Binds one side of a bi-directional reference to a
   * target object.
   *
   * @memberOf Refs
   *
   * @param  {Object} target
   * @param  {String} property
   */
  Refs.prototype.bind = function(target, property) {
    if (typeof property === 'string') {
      if (!this.props[property]) {
        throw new Error('no property <' + property + '> in ref');
      }
      property = this.props[property];
    }

    if (property.collection) {
      defineCollectionProperty(this, property, target);
    } else {
      defineProperty(this, property, target);
    }
  };

  Refs.prototype.ensureRefsCollection = function(target, property) {

    var collection$$1 = target[property.name];

    if (!collection.isExtended(collection$$1)) {
      defineCollectionProperty(this, property, target);
    }

    return collection$$1;
  };

  Refs.prototype.ensureBound = function(target, property) {
    if (!hasOwnProperty(target, property)) {
      this.bind(target, property);
    }
  };

  Refs.prototype.unset = function(target, property, value) {

    if (target) {
      this.ensureBound(target, property);

      if (property.collection) {
        this.ensureRefsCollection(target, property).remove(value);
      } else {
        target[property.name] = undefined;
      }
    }
  };

  Refs.prototype.set = function(target, property, value) {

    if (target) {
      this.ensureBound(target, property);

      if (property.collection) {
        this.ensureRefsCollection(target, property).add(value);
      } else {
        target[property.name] = value;
      }
    }
  };

  var refs = Refs;

  var objectRefs = refs;

  var Collection = collection;
  objectRefs.Collection = Collection;

  var parentRefs = new objectRefs({ name: 'children', enumerable: true, collection: true }, { name: 'parent' }),
      labelRefs = new objectRefs({ name: 'labels', enumerable: true, collection: true }, { name: 'labelTarget' }),
      attacherRefs = new objectRefs({ name: 'attachers', collection: true }, { name: 'host' }),
      outgoingRefs = new objectRefs({ name: 'outgoing', collection: true }, { name: 'source' }),
      incomingRefs = new objectRefs({ name: 'incoming', collection: true }, { name: 'target' });

  /**
   * @namespace djs.model
   */

  /**
   * @memberOf djs.model
   */

  /**
   * The basic graphical representation
   *
   * @class
   *
   * @abstract
   */
  function Base() {

    /**
     * The object that backs up the shape
     *
     * @name Base#businessObject
     * @type Object
     */
    Object.defineProperty(this, 'businessObject', {
      writable: true
    });


    /**
     * Single label support, will mapped to multi label array
     *
     * @name Base#label
     * @type Object
     */
    Object.defineProperty(this, 'label', {
      get: function() {
        return this.labels[0];
      },
      set: function(newLabel) {

        var label = this.label,
            labels = this.labels;

        if (!newLabel && label) {
          labels.remove(label);
        } else {
          labels.add(newLabel, 0);
        }
      }
    });

    /**
     * The parent shape
     *
     * @name Base#parent
     * @type Shape
     */
    parentRefs.bind(this, 'parent');

    /**
     * The list of labels
     *
     * @name Base#labels
     * @type Label
     */
    labelRefs.bind(this, 'labels');

    /**
     * The list of outgoing connections
     *
     * @name Base#outgoing
     * @type Array<Connection>
     */
    outgoingRefs.bind(this, 'outgoing');

    /**
     * The list of incoming connections
     *
     * @name Base#incoming
     * @type Array<Connection>
     */
    incomingRefs.bind(this, 'incoming');
  }


  /**
   * A graphical object
   *
   * @class
   * @constructor
   *
   * @extends Base
   */
  function Shape() {
    Base.call(this);

    /**
     * The list of children
     *
     * @name Shape#children
     * @type Array<Base>
     */
    parentRefs.bind(this, 'children');

    /**
     * @name Shape#host
     * @type Shape
     */
    attacherRefs.bind(this, 'host');

    /**
     * @name Shape#attachers
     * @type Shape
     */
    attacherRefs.bind(this, 'attachers');
  }

  inherits_browser(Shape, Base);


  /**
   * A root graphical object
   *
   * @class
   * @constructor
   *
   * @extends Shape
   */
  function Root() {
    Shape.call(this);
  }

  inherits_browser(Root, Shape);


  /**
   * A label for an element
   *
   * @class
   * @constructor
   *
   * @extends Shape
   */
  function Label() {
    Shape.call(this);

    /**
     * The labeled element
     *
     * @name Label#labelTarget
     * @type Base
     */
    labelRefs.bind(this, 'labelTarget');
  }

  inherits_browser(Label, Shape);


  /**
   * A connection between two elements
   *
   * @class
   * @constructor
   *
   * @extends Base
   */
  function Connection() {
    Base.call(this);

    /**
     * The element this connection originates from
     *
     * @name Connection#source
     * @type Base
     */
    outgoingRefs.bind(this, 'source');

    /**
     * The element this connection points to
     *
     * @name Connection#target
     * @type Base
     */
    incomingRefs.bind(this, 'target');
  }

  inherits_browser(Connection, Base);


  var types = {
    connection: Connection,
    shape: Shape,
    label: Label,
    root: Root
  };

  /**
   * Creates a new model element of the specified type
   *
   * @method create
   *
   * @example
   *
   * var shape1 = Model.create('shape', { x: 10, y: 10, width: 100, height: 100 });
   * var shape2 = Model.create('shape', { x: 210, y: 210, width: 100, height: 100 });
   *
   * var connection = Model.create('connection', { waypoints: [ { x: 110, y: 55 }, {x: 210, y: 55 } ] });
   *
   * @param  {String} type lower-cased model name
   * @param  {Object} attrs attributes to initialize the new model instance with
   *
   * @return {Base} the new model instance
   */
  function create$1(type, attrs) {
    var Type = types[type];
    if (!Type) {
      throw new Error('unknown type: <' + type + '>');
    }
    return assign(new Type(), attrs);
  }

  /**
   * A factory for diagram-js shapes
   */
  function ElementFactory() {
    this._uid = 12;
  }


  ElementFactory.prototype.createRoot = function(attrs) {
    return this.create('root', attrs);
  };

  ElementFactory.prototype.createLabel = function(attrs) {
    return this.create('label', attrs);
  };

  ElementFactory.prototype.createShape = function(attrs) {
    return this.create('shape', attrs);
  };

  ElementFactory.prototype.createConnection = function(attrs) {
    return this.create('connection', attrs);
  };

  /**
   * Create a model element with the given type and
   * a number of pre-set attributes.
   *
   * @param  {String} type
   * @param  {Object} attrs
   * @return {djs.model.Base} the newly created model instance
   */
  ElementFactory.prototype.create = function(type, attrs) {

    attrs = assign({}, attrs || {});

    if (!attrs.id) {
      attrs.id = type + '_' + (this._uid++);
    }

    return create$1(type, attrs);
  };

  var FN_REF = '__fn';

  var DEFAULT_PRIORITY = 1000;

  var slice$1 = Array.prototype.slice;

  /**
   * A general purpose event bus.
   *
   * This component is used to communicate across a diagram instance.
   * Other parts of a diagram can use it to listen to and broadcast events.
   *
   *
   * ## Registering for Events
   *
   * The event bus provides the {@link EventBus#on} and {@link EventBus#once}
   * methods to register for events. {@link EventBus#off} can be used to
   * remove event registrations. Listeners receive an instance of {@link Event}
   * as the first argument. It allows them to hook into the event execution.
   *
   * ```javascript
   *
   * // listen for event
   * eventBus.on('foo', function(event) {
   *
   *   // access event type
   *   event.type; // 'foo'
   *
   *   // stop propagation to other listeners
   *   event.stopPropagation();
   *
   *   // prevent event default
   *   event.preventDefault();
   * });
   *
   * // listen for event with custom payload
   * eventBus.on('bar', function(event, payload) {
   *   console.log(payload);
   * });
   *
   * // listen for event returning value
   * eventBus.on('foobar', function(event) {
   *
   *   // stop event propagation + prevent default
   *   return false;
   *
   *   // stop event propagation + return custom result
   *   return {
   *     complex: 'listening result'
   *   };
   * });
   *
   *
   * // listen with custom priority (default=1000, higher is better)
   * eventBus.on('priorityfoo', 1500, function(event) {
   *   console.log('invoked first!');
   * });
   *
   *
   * // listen for event and pass the context (`this`)
   * eventBus.on('foobar', function(event) {
   *   this.foo();
   * }, this);
   * ```
   *
   *
   * ## Emitting Events
   *
   * Events can be emitted via the event bus using {@link EventBus#fire}.
   *
   * ```javascript
   *
   * // false indicates that the default action
   * // was prevented by listeners
   * if (eventBus.fire('foo') === false) {
   *   console.log('default has been prevented!');
   * };
   *
   *
   * // custom args + return value listener
   * eventBus.on('sum', function(event, a, b) {
   *   return a + b;
   * });
   *
   * // you can pass custom arguments + retrieve result values.
   * var sum = eventBus.fire('sum', 1, 2);
   * console.log(sum); // 3
   * ```
   */
  function EventBus() {
    this._listeners = {};

    // cleanup on destroy on lowest priority to allow
    // message passing until the bitter end
    this.on('diagram.destroy', 1, this._destroy, this);
  }


  /**
   * Register an event listener for events with the given name.
   *
   * The callback will be invoked with `event, ...additionalArguments`
   * that have been passed to {@link EventBus#fire}.
   *
   * Returning false from a listener will prevent the events default action
   * (if any is specified). To stop an event from being processed further in
   * other listeners execute {@link Event#stopPropagation}.
   *
   * Returning anything but `undefined` from a listener will stop the listener propagation.
   *
   * @param {String|Array<String>} events
   * @param {Number} [priority=1000] the priority in which this listener is called, larger is higher
   * @param {Function} callback
   * @param {Object} [that] Pass context (`this`) to the callback
   */
  EventBus.prototype.on = function(events, priority, callback, that) {

    events = isArray(events) ? events : [ events ];

    if (isFunction(priority)) {
      that = callback;
      callback = priority;
      priority = DEFAULT_PRIORITY;
    }

    if (!isNumber(priority)) {
      throw new Error('priority must be a number');
    }

    var actualCallback = callback;

    if (that) {
      actualCallback = bind$2(callback, that);

      // make sure we remember and are able to remove
      // bound callbacks via {@link #off} using the original
      // callback
      actualCallback[FN_REF] = callback[FN_REF] || callback;
    }

    var self = this;

    events.forEach(function(e) {
      self._addListener(e, {
        priority: priority,
        callback: actualCallback,
        next: null
      });
    });
  };


  /**
   * Register an event listener that is executed only once.
   *
   * @param {String} event the event name to register for
   * @param {Function} callback the callback to execute
   * @param {Object} [that] Pass context (`this`) to the callback
   */
  EventBus.prototype.once = function(event, priority, callback, that) {
    var self = this;

    if (isFunction(priority)) {
      that = callback;
      callback = priority;
      priority = DEFAULT_PRIORITY;
    }

    if (!isNumber(priority)) {
      throw new Error('priority must be a number');
    }

    function wrappedCallback() {
      var result = callback.apply(that, arguments);

      self.off(event, wrappedCallback);

      return result;
    }

    // make sure we remember and are able to remove
    // bound callbacks via {@link #off} using the original
    // callback
    wrappedCallback[FN_REF] = callback;

    this.on(event, priority, wrappedCallback);
  };


  /**
   * Removes event listeners by event and callback.
   *
   * If no callback is given, all listeners for a given event name are being removed.
   *
   * @param {String|Array<String>} events
   * @param {Function} [callback]
   */
  EventBus.prototype.off = function(events, callback) {

    events = isArray(events) ? events : [ events ];

    var self = this;

    events.forEach(function(event) {
      self._removeListener(event, callback);
    });

  };


  /**
   * Create an EventBus event.
   *
   * @param {Object} data
   *
   * @return {Object} event, recognized by the eventBus
   */
  EventBus.prototype.createEvent = function(data) {
    var event = new InternalEvent();

    event.init(data);

    return event;
  };


  /**
   * Fires a named event.
   *
   * @example
   *
   * // fire event by name
   * events.fire('foo');
   *
   * // fire event object with nested type
   * var event = { type: 'foo' };
   * events.fire(event);
   *
   * // fire event with explicit type
   * var event = { x: 10, y: 20 };
   * events.fire('element.moved', event);
   *
   * // pass additional arguments to the event
   * events.on('foo', function(event, bar) {
   *   alert(bar);
   * });
   *
   * events.fire({ type: 'foo' }, 'I am bar!');
   *
   * @param {String} [name] the optional event name
   * @param {Object} [event] the event object
   * @param {...Object} additional arguments to be passed to the callback functions
   *
   * @return {Boolean} the events return value, if specified or false if the
   *                   default action was prevented by listeners
   */
  EventBus.prototype.fire = function(type, data) {

    var event,
        firstListener,
        returnValue,
        args;

    args = slice$1.call(arguments);

    if (typeof type === 'object') {
      event = type;
      type = event.type;
    }

    if (!type) {
      throw new Error('no event type specified');
    }

    firstListener = this._listeners[type];

    if (!firstListener) {
      return;
    }

    // we make sure we fire instances of our home made
    // events here. We wrap them only once, though
    if (data instanceof InternalEvent) {
      // we are fine, we alread have an event
      event = data;
    } else {
      event = this.createEvent(data);
    }

    // ensure we pass the event as the first parameter
    args[0] = event;

    // original event type (in case we delegate)
    var originalType = event.type;

    // update event type before delegation
    if (type !== originalType) {
      event.type = type;
    }

    try {
      returnValue = this._invokeListeners(event, args, firstListener);
    } finally {
      // reset event type after delegation
      if (type !== originalType) {
        event.type = originalType;
      }
    }

    // set the return value to false if the event default
    // got prevented and no other return value exists
    if (returnValue === undefined && event.defaultPrevented) {
      returnValue = false;
    }

    return returnValue;
  };


  EventBus.prototype.handleError = function(error) {
    return this.fire('error', { error: error }) === false;
  };


  EventBus.prototype._destroy = function() {
    this._listeners = {};
  };

  EventBus.prototype._invokeListeners = function(event, args, listener) {

    var returnValue;

    while (listener) {

      // handle stopped propagation
      if (event.cancelBubble) {
        break;
      }

      returnValue = this._invokeListener(event, args, listener);

      listener = listener.next;
    }

    return returnValue;
  };

  EventBus.prototype._invokeListener = function(event, args, listener) {

    var returnValue;

    try {
      // returning false prevents the default action
      returnValue = invokeFunction(listener.callback, args);

      // stop propagation on return value
      if (returnValue !== undefined) {
        event.returnValue = returnValue;
        event.stopPropagation();
      }

      // prevent default on return false
      if (returnValue === false) {
        event.preventDefault();
      }
    } catch (e) {
      if (!this.handleError(e)) {
        console.error('unhandled error in event listener');
        console.error(e.stack);

        throw e;
      }
    }

    return returnValue;
  };

  /*
   * Add new listener with a certain priority to the list
   * of listeners (for the given event).
   *
   * The semantics of listener registration / listener execution are
   * first register, first serve: New listeners will always be inserted
   * after existing listeners with the same priority.
   *
   * Example: Inserting two listeners with priority 1000 and 1300
   *
   *    * before: [ 1500, 1500, 1000, 1000 ]
   *    * after: [ 1500, 1500, (new=1300), 1000, 1000, (new=1000) ]
   *
   * @param {String} event
   * @param {Object} listener { priority, callback }
   */
  EventBus.prototype._addListener = function(event, newListener) {

    var listener = this._getListeners(event),
        previousListener;

    // no prior listeners
    if (!listener) {
      this._setListeners(event, newListener);

      return;
    }

    // ensure we order listeners by priority from
    // 0 (high) to n > 0 (low)
    while (listener) {

      if (listener.priority < newListener.priority) {

        newListener.next = listener;

        if (previousListener) {
          previousListener.next = newListener;
        } else {
          this._setListeners(event, newListener);
        }

        return;
      }

      previousListener = listener;
      listener = listener.next;
    }

    // add new listener to back
    previousListener.next = newListener;
  };


  EventBus.prototype._getListeners = function(name) {
    return this._listeners[name];
  };

  EventBus.prototype._setListeners = function(name, listener) {
    this._listeners[name] = listener;
  };

  EventBus.prototype._removeListener = function(event, callback) {

    var listener = this._getListeners(event),
        nextListener,
        previousListener,
        listenerCallback;

    if (!callback) {
      // clear listeners
      this._setListeners(event, null);

      return;
    }

    while (listener) {

      nextListener = listener.next;

      listenerCallback = listener.callback;

      if (listenerCallback === callback || listenerCallback[FN_REF] === callback) {
        if (previousListener) {
          previousListener.next = nextListener;
        } else {
          // new first listener
          this._setListeners(event, nextListener);
        }
      }

      previousListener = listener;
      listener = nextListener;
    }
  };

  /**
   * A event that is emitted via the event bus.
   */
  function InternalEvent() { }

  InternalEvent.prototype.stopPropagation = function() {
    this.cancelBubble = true;
  };

  InternalEvent.prototype.preventDefault = function() {
    this.defaultPrevented = true;
  };

  InternalEvent.prototype.init = function(data) {
    assign(this, data || {});
  };


  /**
   * Invoke function. Be fast...
   *
   * @param {Function} fn
   * @param {Array<Object>} args
   *
   * @return {Any}
   */
  function invokeFunction(fn, args) {
    return fn.apply(null, args);
  }

  /**
   * SVGs for elements are generated by the {@link GraphicsFactory}.
   *
   * This utility gives quick access to the important semantic
   * parts of an element.
   */

  /**
   * Returns the visual part of a diagram element
   *
   * @param {Snap<SVGElement>} gfx
   *
   * @return {Snap<SVGElement>}
   */
  function getVisual(gfx) {
    return query('.djs-visual', gfx);
  }

  /**
   * Returns the children for a given diagram element.
   *
   * @param {Snap<SVGElement>} gfx
   * @return {Snap<SVGElement>}
   */
  function getChildren(gfx) {
    return gfx.parentNode.childNodes[1];
  }

  /**
   * @param {SVGElement} element
   * @param {Number} x
   * @param {Number} y
   */
  function translate(gfx, x, y) {
    var translate = createTransform();
    translate.setTranslate(x, y);

    transform(gfx, translate);
  }

  /**
   * A factory that creates graphical elements
   *
   * @param {EventBus} eventBus
   * @param {ElementRegistry} elementRegistry
   */
  function GraphicsFactory(eventBus, elementRegistry) {
    this._eventBus = eventBus;
    this._elementRegistry = elementRegistry;
  }

  GraphicsFactory.$inject = [ 'eventBus' , 'elementRegistry' ];


  GraphicsFactory.prototype._getChildren = function(element) {

    var gfx = this._elementRegistry.getGraphics(element);

    var childrenGfx;

    // root element
    if (!element.parent) {
      childrenGfx = gfx;
    } else {
      childrenGfx = getChildren(gfx);
      if (!childrenGfx) {
        childrenGfx = create('g');
        classes$1(childrenGfx).add('djs-children');

        append(gfx.parentNode, childrenGfx);
      }
    }

    return childrenGfx;
  };

  /**
   * Clears the graphical representation of the element and returns the
   * cleared visual (the <g class="djs-visual" /> element).
   */
  GraphicsFactory.prototype._clear = function(gfx) {
    var visual = getVisual(gfx);

    clear(visual);

    return visual;
  };

  /**
   * Creates a gfx container for shapes and connections
   *
   * The layout is as follows:
   *
   * <g class="djs-group">
   *
   *   <!-- the gfx -->
   *   <g class="djs-element djs-(shape|connection)">
   *     <g class="djs-visual">
   *       <!-- the renderer draws in here -->
   *     </g>
   *
   *     <!-- extensions (overlays, click box, ...) goes here
   *   </g>
   *
   *   <!-- the gfx child nodes -->
   *   <g class="djs-children"></g>
   * </g>
   *
   * @param {Object} parent
   * @param {String} type the type of the element, i.e. shape | connection
   * @param {Number} [parentIndex] position to create container in parent
   */
  GraphicsFactory.prototype._createContainer = function(type, childrenGfx, parentIndex) {
    var outerGfx = create('g');
    classes$1(outerGfx).add('djs-group');

    // insert node at position
    if (typeof parentIndex !== 'undefined') {
      prependTo$1(outerGfx, childrenGfx, childrenGfx.childNodes[parentIndex]);
    } else {
      append(childrenGfx, outerGfx);
    }

    var gfx = create('g');
    classes$1(gfx).add('djs-element');
    classes$1(gfx).add('djs-' + type);

    append(outerGfx, gfx);

    // create visual
    var visual = create('g');
    classes$1(visual).add('djs-visual');

    append(gfx, visual);

    return gfx;
  };

  GraphicsFactory.prototype.create = function(type, element, parentIndex) {
    var childrenGfx = this._getChildren(element.parent);
    return this._createContainer(type, childrenGfx, parentIndex);
  };

  GraphicsFactory.prototype.updateContainments = function(elements) {

    var self = this,
        elementRegistry = this._elementRegistry,
        parents;

    parents = reduce(elements, function(map, e) {

      if (e.parent) {
        map[e.parent.id] = e.parent;
      }

      return map;
    }, {});

    // update all parents of changed and reorganized their children
    // in the correct order (as indicated in our model)
    forEach(parents, function(parent) {

      var children = parent.children;

      if (!children) {
        return;
      }

      var childGfx = self._getChildren(parent);

      forEach(children.slice().reverse(), function(c) {
        var gfx = elementRegistry.getGraphics(c);

        prependTo$1(gfx.parentNode, childGfx);
      });
    });
  };

  GraphicsFactory.prototype.drawShape = function(visual, element) {
    var eventBus = this._eventBus;

    return eventBus.fire('render.shape', { gfx: visual, element: element });
  };

  GraphicsFactory.prototype.getShapePath = function(element) {
    var eventBus = this._eventBus;

    return eventBus.fire('render.getShapePath', element);
  };

  GraphicsFactory.prototype.drawConnection = function(visual, element) {
    var eventBus = this._eventBus;

    return eventBus.fire('render.connection', { gfx: visual, element: element });
  };

  GraphicsFactory.prototype.getConnectionPath = function(waypoints) {
    var eventBus = this._eventBus;

    return eventBus.fire('render.getConnectionPath', waypoints);
  };

  GraphicsFactory.prototype.update = function(type, element, gfx) {
    // Do not update root element
    if (!element.parent) {
      return;
    }

    var visual = this._clear(gfx);

    // redraw
    if (type === 'shape') {
      this.drawShape(visual, element);

      // update positioning
      translate(gfx, element.x, element.y);
    } else
    if (type === 'connection') {
      this.drawConnection(visual, element);
    } else {
      throw new Error('unknown type: ' + type);
    }

    if (element.hidden) {
      attr$1(gfx, 'display', 'none');
    } else {
      attr$1(gfx, 'display', 'block');
    }
  };

  GraphicsFactory.prototype.remove = function(element) {
    var gfx = this._elementRegistry.getGraphics(element);

    // remove
    remove$1(gfx.parentNode);
  };


  // helpers //////////////////////

  function prependTo$1(newNode, parentNode, siblingNode) {
    parentNode.insertBefore(newNode, siblingNode || parentNode.firstChild);
  }

  var CoreModule$1 = {
    __depends__: [ DrawModule$1 ],
    __init__: [ 'canvas' ],
    canvas: [ 'type', Canvas ],
    elementRegistry: [ 'type', ElementRegistry ],
    elementFactory: [ 'type', ElementFactory ],
    eventBus: [ 'type', EventBus ],
    graphicsFactory: [ 'type', GraphicsFactory ]
  };

  /**
   * Bootstrap an injector from a list of modules, instantiating a number of default components
   *
   * @ignore
   * @param {Array<didi.Module>} bootstrapModules
   *
   * @return {didi.Injector} a injector to use to access the components
   */
  function bootstrap(bootstrapModules) {

    var modules = [],
        components = [];

    function hasModule(m) {
      return modules.indexOf(m) >= 0;
    }

    function addModule(m) {
      modules.push(m);
    }

    function visit(m) {
      if (hasModule(m)) {
        return;
      }

      (m.__depends__ || []).forEach(visit);

      if (hasModule(m)) {
        return;
      }

      addModule(m);

      (m.__init__ || []).forEach(function(c) {
        components.push(c);
      });
    }

    bootstrapModules.forEach(visit);

    var injector = new Injector(modules);

    components.forEach(function(c) {

      try {
        // eagerly resolve component (fn or string)
        injector[typeof c === 'string' ? 'get' : 'invoke'](c);
      } catch (e) {
        console.error('Failed to instantiate component');
        console.error(e.stack);

        throw e;
      }
    });

    return injector;
  }

  /**
   * Creates an injector from passed options.
   *
   * @ignore
   * @param  {Object} options
   * @return {didi.Injector}
   */
  function createInjector(options) {

    options = options || {};

    var configModule = {
      'config': ['value', options]
    };

    var modules = [ configModule, CoreModule$1 ].concat(options.modules || []);

    return bootstrap(modules);
  }


  /**
   * The main diagram-js entry point that bootstraps the diagram with the given
   * configuration.
   *
   * To register extensions with the diagram, pass them as Array<didi.Module> to the constructor.
   *
   * @class djs.Diagram
   * @memberOf djs
   * @constructor
   *
   * @example
   *
   * <caption>Creating a plug-in that logs whenever a shape is added to the canvas.</caption>
   *
   * // plug-in implemenentation
   * function MyLoggingPlugin(eventBus) {
   *   eventBus.on('shape.added', function(event) {
   *     console.log('shape ', event.shape, ' was added to the diagram');
   *   });
   * }
   *
   * // export as module
   * export default {
   *   __init__: [ 'myLoggingPlugin' ],
   *     myLoggingPlugin: [ 'type', MyLoggingPlugin ]
   * };
   *
   *
   * // instantiate the diagram with the new plug-in
   *
   * import MyLoggingModule from 'path-to-my-logging-plugin';
   *
   * var diagram = new Diagram({
   *   modules: [
   *     MyLoggingModule
   *   ]
   * });
   *
   * diagram.invoke([ 'canvas', function(canvas) {
   *   // add shape to drawing canvas
   *   canvas.addShape({ x: 10, y: 10 });
   * });
   *
   * // 'shape ... was added to the diagram' logged to console
   *
   * @param {Object} options
   * @param {Array<didi.Module>} [options.modules] external modules to instantiate with the diagram
   * @param {didi.Injector} [injector] an (optional) injector to bootstrap the diagram with
   */
  function Diagram(options, injector) {

    // create injector unless explicitly specified
    this.injector = injector = injector || createInjector(options);

    // API

    /**
     * Resolves a diagram service
     *
     * @method Diagram#get
     *
     * @param {String} name the name of the diagram service to be retrieved
     * @param {Boolean} [strict=true] if false, resolve missing services to null
     */
    this.get = injector.get;

    /**
     * Executes a function into which diagram services are injected
     *
     * @method Diagram#invoke
     *
     * @param {Function|Object[]} fn the function to resolve
     * @param {Object} locals a number of locals to use to resolve certain dependencies
     */
    this.invoke = injector.invoke;

    // init

    // indicate via event


    /**
     * An event indicating that all plug-ins are loaded.
     *
     * Use this event to fire other events to interested plug-ins
     *
     * @memberOf Diagram
     *
     * @event diagram.init
     *
     * @example
     *
     * eventBus.on('diagram.init', function() {
     *   eventBus.fire('my-custom-event', { foo: 'BAR' });
     * });
     *
     * @type {Object}
     */
    this.get('eventBus').fire('diagram.init');
  }


  /**
   * Destroys the diagram
   *
   * @method  Diagram#destroy
   */
  Diagram.prototype.destroy = function() {
    this.get('eventBus').fire('diagram.destroy');
  };

  /**
   * Clear the diagram, removing all contents.
   */
  Diagram.prototype.clear = function() {
    this.get('eventBus').fire('diagram.clear');
  };

  inherits_browser(Viewer, Diagram);

  function Viewer(options) {
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
      parentNode = query(parentNode);
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

  return Viewer;

})));
