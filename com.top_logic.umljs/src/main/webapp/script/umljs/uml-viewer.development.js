/*!
 * uml-js - uml-viewer v1.0.0
 *
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 *
 * Date: 2023-11-27
 */

(function (global, factory) {
  typeof exports === 'object' && typeof module !== 'undefined' ? module.exports = factory() :
  typeof define === 'function' && define.amd ? define(factory) :
  (global = typeof globalThis !== 'undefined' ? globalThis : global || self, global.UmlJS = factory());
})(this, (function () { 'use strict';

  function _mergeNamespaces$1(n, m) {
    m.forEach(function (e) {
      e && typeof e !== 'string' && !Array.isArray(e) && Object.keys(e).forEach(function (k) {
        if (k !== 'default' && !(k in n)) {
          var d = Object.getOwnPropertyDescriptor(e, k);
          Object.defineProperty(n, k, d.get ? d : {
            enumerable: true,
            get: function () { return e[k]; }
          });
        }
      });
    });
    return Object.freeze(n);
  }

  /**
   * Flatten array, one level deep.
   *
   * @param {Array<?>} arr
   *
   * @return {Array<?>}
   */

  const nativeToString$1 = Object.prototype.toString;
  const nativeHasOwnProperty$1 = Object.prototype.hasOwnProperty;

  function isUndefined$1(obj) {
    return obj === undefined;
  }

  function isArray$2(obj) {
    return nativeToString$1.call(obj) === '[object Array]';
  }

  /**
   * Return true, if target owns a property with the given key.
   *
   * @param {Object} target
   * @param {String} key
   *
   * @return {Boolean}
   */
  function has$1(target, key) {
    return nativeHasOwnProperty$1.call(target, key);
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
  function forEach$1(collection, iterator) {

    let val,
        result;

    if (isUndefined$1(collection)) {
      return;
    }

    const convertKey = isArray$2(collection) ? toNum$1 : identity$1;

    for (let key in collection) {

      if (has$1(collection, key)) {
        val = collection[key];

        result = iterator(val, convertKey(key));

        if (result === false) {
          return val;
        }
      }
    }
  }


  function identity$1(arg) {
    return arg;
  }

  function toNum$1(arg) {
    return Number(arg);
  }

  /**
   * Assigns style attributes in a style-src compliant way.
   *
   * @param {Element} element
   * @param {...Object} styleSources
   *
   * @return {Element} the element
   */
  function assign$1(element, ...styleSources) {
    const target = element.style;

    forEach$1(styleSources, function(style) {
      if (!style) {
        return;
      }

      forEach$1(style, function(value, key) {
        target[key] = value;
      });
    });

    return element;
  }

  /**
   * Taken from https://github.com/component/classes
   *
   * Without the component bits.
   */

  /**
   * toString reference.
   */

  const toString$1 = Object.prototype.toString;

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

  /**
   * Initialize a new ClassList for `el`.
   *
   * @param {Element} el
   * @api private
   */

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
    this.list.add(name);
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
    if ('[object RegExp]' == toString$1.call(name)) {
      return this.removeMatching(name);
    }

    this.list.remove(name);
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
    const arr = this.array();
    for (let i = 0; i < arr.length; i++) {
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
    if ('undefined' !== typeof force) {
      if (force !== this.list.toggle(name, force)) {
        this.list.toggle(name); // toggle again to correct
      }
    } else {
      this.list.toggle(name);
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
    return Array.from(this.list);
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
    return this.list.contains(name);
  };

  /**
   * Remove all children from the given element.
   */
  function clear$1(el) {

    var c;

    while (el.childNodes.length) {
      c = el.childNodes[0];
      el.removeChild(c);
    }

    return el;
  }

  /**
   * @param { HTMLElement } element
   * @param { String } selector
   *
   * @return { boolean }
   */
  function matches(element, selector) {
    return element && typeof element.matches === 'function' && element.matches(selector);
  }

  /**
   * Closest
   *
   * @param {Element} el
   * @param {String} selector
   * @param {Boolean} checkYourSelf (optional)
   */
  function closest(element, selector, checkYourSelf) {
    var currentElem = checkYourSelf ? element : element.parentNode;

    while (currentElem && currentElem.nodeType !== document.DOCUMENT_NODE &&
        currentElem.nodeType !== document.DOCUMENT_FRAGMENT_NODE) {

      if (matches(currentElem, selector)) {
        return currentElem;
      }

      currentElem = currentElem.parentNode;
    }

    return matches(currentElem, selector) ? currentElem : null;
  }

  var componentEvent = {};

  var bind$1, unbind$1, prefix;

  function detect () {
    bind$1 = window.addEventListener ? 'addEventListener' : 'attachEvent';
    unbind$1 = window.removeEventListener ? 'removeEventListener' : 'detachEvent';
    prefix = bind$1 !== 'addEventListener' ? 'on' : '';
  }

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

  var bind_1 = componentEvent.bind = function(el, type, fn, capture){
    if (!bind$1) detect();
    el[bind$1](prefix + type, fn, capture || false);
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

  var unbind_1 = componentEvent.unbind = function(el, type, fn, capture){
    if (!unbind$1) detect();
    el[unbind$1](prefix + type, fn, capture || false);
    return fn;
  };

  var event = /*#__PURE__*/_mergeNamespaces$1({
    __proto__: null,
    bind: bind_1,
    unbind: unbind_1,
    'default': componentEvent
  }, [componentEvent]);

  /**
   * Expose `parse`.
   */

  var domify = parse$1;

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

  function parse$1(html, doc) {
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
    var wrap = Object.prototype.hasOwnProperty.call(map, tag) ? map[tag] : map._default;
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

  var domify$1 = domify;

  function query(selector, el) {
    el = el || document;

    return el.querySelector(selector);
  }

  /**
   * @typedef {import('../util/Types').Point} Point
   * @typedef {import('../util/Types').Rect} Rect
   */



  /**
   * @param {Point} a
   * @param {Point} b
   * @return {Point}
   */
  function delta(a, b) {
    return {
      x: a.x - b.x,
      y: a.y - b.y
    };
  }

  /**
   * Get the logarithm of x with base 10.
   *
   * @param {number} x
   */
  function log10(x) {
    return Math.log(x) / Math.log(10);
  }

  /**
   * Get step size for given range and number of steps.
   *
   * @param {Object} range
   * @param {number} range.min
   * @param {number} range.max
   * @param {number} steps
   */
  function getStepSize(range, steps) {

    var minLinearRange = log10(range.min),
        maxLinearRange = log10(range.max);

    var absoluteLinearRange = Math.abs(minLinearRange) + Math.abs(maxLinearRange);

    return absoluteLinearRange / steps;
  }

  /**
   * @param {Object} range
   * @param {number} range.min
   * @param {number} range.max
   * @param {number} scale
   */
  function cap(range, scale) {
    return Math.max(range.min, Math.min(range.max, scale));
  }

  /**
   * Flatten array, one level deep.
   *
   * @template T
   *
   * @param {T[][]} arr
   *
   * @return {T[]}
   */

  const nativeToString = Object.prototype.toString;
  const nativeHasOwnProperty = Object.prototype.hasOwnProperty;

  function isUndefined(obj) {
    return obj === undefined;
  }

  function isArray$1(obj) {
    return nativeToString.call(obj) === '[object Array]';
  }

  function isObject(obj) {
    return nativeToString.call(obj) === '[object Object]';
  }

  function isNumber(obj) {
    return nativeToString.call(obj) === '[object Number]';
  }

  /**
   * @param {any} obj
   *
   * @return {boolean}
   */
  function isFunction(obj) {
    const tag = nativeToString.call(obj);

    return (
      tag === '[object Function]' ||
      tag === '[object AsyncFunction]' ||
      tag === '[object GeneratorFunction]' ||
      tag === '[object AsyncGeneratorFunction]' ||
      tag === '[object Proxy]'
    );
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
   * @template T
   * @typedef { (
   *   ((e: T) => boolean) |
   *   ((e: T, idx: number) => boolean) |
   *   ((e: T, key: string) => boolean) |
   *   string |
   *   number
   * ) } Matcher
   */

  /**
   * @template T
   * @template U
   *
   * @typedef { (
   *   ((e: T) => U) | string | number
   * ) } Extractor
   */


  /**
   * @template T
   * @typedef { (val: T, key: any) => boolean } MatchFn
   */

  /**
   * @template T
   * @typedef { T[] } ArrayCollection
   */

  /**
   * @template T
   * @typedef { { [key: string]: T } } StringKeyValueCollection
   */

  /**
   * @template T
   * @typedef { { [key: number]: T } } NumberKeyValueCollection
   */

  /**
   * @template T
   * @typedef { StringKeyValueCollection<T> | NumberKeyValueCollection<T> } KeyValueCollection
   */

  /**
   * @template T
   * @typedef { KeyValueCollection<T> | ArrayCollection<T> } Collection
   */

  /**
   * Find element in collection.
   *
   * @template T
   * @param {Collection<T>} collection
   * @param {Matcher<T>} matcher
   *
   * @return {Object}
   */
  function find(collection, matcher) {

    const matchFn = toMatcher(matcher);

    let match;

    forEach(collection, function(val, key) {
      if (matchFn(val, key)) {
        match = val;

        return false;
      }
    });

    return match;

  }


  /**
   * Iterate over collection; returning something
   * (non-undefined) will stop iteration.
   *
   * @template T
   * @param {Collection<T>} collection
   * @param { ((item: T, idx: number) => (boolean|void)) | ((item: T, key: string) => (boolean|void)) } iterator
   *
   * @return {T} return result that stopped the iteration
   */
  function forEach(collection, iterator) {

    let val,
        result;

    if (isUndefined(collection)) {
      return;
    }

    const convertKey = isArray$1(collection) ? toNum : identity;

    for (let key in collection) {

      if (has(collection, key)) {
        val = collection[key];

        result = iterator(val, convertKey(key));

        if (result === false) {
          return val;
        }
      }
    }
  }


  /**
   * Reduce collection, returning a single result.
   *
   * @template T
   * @template V
   *
   * @param {Collection<T>} collection
   * @param {(result: V, entry: T, index: any) => V} iterator
   * @param {V} result
   *
   * @return {V} result returned from last iterator
   */
  function reduce(collection, iterator, result) {

    forEach(collection, function(value, idx) {
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

    return !!reduce(collection, function(matches, val, key) {
      return matches && matcher(val, key);
    }, true);
  }


  /**
   * @template T
   * @param {Matcher<T>} matcher
   *
   * @return {MatchFn<T>}
   */
  function toMatcher(matcher) {
    return isFunction(matcher) ? matcher : (e) => {
      return e === matcher;
    };
  }


  function identity(arg) {
    return arg;
  }

  function toNum(arg) {
    return Number(arg);
  }

  /* global setTimeout clearTimeout */

  /**
   * @typedef { {
   *   (...args: any[]): any;
   *   flush: () => void;
   *   cancel: () => void;
   * } } DebouncedFunction
   */

  /**
   * Debounce fn, calling it only once if the given time
   * elapsed between calls.
   *
   * Lodash-style the function exposes methods to `#clear`
   * and `#flush` to control internal behavior.
   *
   * @param  {Function} fn
   * @param  {Number} timeout
   *
   * @return {DebouncedFunction} debounced function
   */
  function debounce(fn, timeout) {

    let timer;

    let lastArgs;
    let lastThis;

    let lastNow;

    function fire(force) {

      let now = Date.now();

      let scheduledDiff = force ? 0 : (lastNow + timeout) - now;

      if (scheduledDiff > 0) {
        return schedule(scheduledDiff);
      }

      fn.apply(lastThis, lastArgs);

      clear();
    }

    function schedule(timeout) {
      timer = setTimeout(fire, timeout);
    }

    function clear() {
      if (timer) {
        clearTimeout(timer);
      }

      timer = lastNow = lastArgs = lastThis = undefined;
    }

    function flush() {
      if (timer) {
        fire(true);
      }

      clear();
    }

    /**
     * @type { DebouncedFunction }
     */
    function callback(...args) {
      lastNow = Date.now();

      lastArgs = args;
      lastThis = this;

      // ensure an execution is scheduled
      if (!timer) {
        schedule(timeout);
      }
    }

    callback.flush = flush;
    callback.cancel = clear;

    return callback;
  }

  /**
   * Bind function against target <this>.
   *
   * @param  {Function} fn
   * @param  {Object}   target
   *
   * @return {Function} bound function
   */
  function bind(fn, target) {
    return fn.bind(target);
  }

  /**
   * Convenience wrapper for `Object.assign`.
   *
   * @param {Object} target
   * @param {...Object} others
   *
   * @return {Object} the target
   */
  function assign(target, ...others) {
    return Object.assign(target, ...others);
  }

  /**
   * @typedef {import('../../core/Canvas').default} Canvas
   * @typedef {import('../../core/EventBus').default} EventBus
   *
   * @typedef {import('../../util/Types').Point} Point
   */

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
   * @param {boolean} [config.enabled=true] default enabled state
   * @param {number} [config.scale=.75] scroll sensivity
   * @param {EventBus} eventBus
   * @param {Canvas} canvas
   */
  function ZoomScroll(config, eventBus, canvas) {

    config = config || {};

    this._enabled = false;

    this._canvas = canvas;
    this._container = canvas._container;

    this._handleWheel = bind(this._handleWheel, this);

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

  /**
   * @param {Point} delta
   */
  ZoomScroll.prototype.scroll = function scroll(delta) {
    this._canvas.scroll(delta);
  };


  ZoomScroll.prototype.reset = function reset() {
    this._canvas.zoom('fit-viewport');
  };

  /**
   * Zoom depending on delta.
   *
   * @param {number} delta
   * @param {Point} position
   */
  ZoomScroll.prototype.zoom = function zoom(delta, position) {

    // zoom with half the step size of stepZoom
    var stepSize = getStepSize(RANGE, NUM_STEPS * 2);

    // add until threshold reached
    this._totalDelta += delta;

    if (Math.abs(this._totalDelta) > DELTA_THRESHOLD) {
      this._zoom(delta, position, stepSize);

      // reset
      this._totalDelta = 0;
    }
  };


  ZoomScroll.prototype._handleWheel = function handleWheel(event) {

    // event is already handled by '.djs-scrollable'
    if (closest(event.target, '.djs-scrollable', true)) {
      return;
    }

    var element = this._container;

    event.preventDefault();

    // pinch to zoom is mapped to wheel + ctrlKey = true
    // in modern browsers (!)

    var isZoom = event.ctrlKey;

    var isHorizontalScroll = event.shiftKey;

    var factor = -1 * this._scale,
        delta;

    if (isZoom) {
      factor *= event.deltaMode === 0 ? 0.020 : 0.32;
    } else {
      factor *= event.deltaMode === 0 ? 1.0 : 16.0;
    }

    if (isZoom) {
      var elementRect = element.getBoundingClientRect();

      var offset = {
        x: event.clientX - elementRect.left,
        y: event.clientY - elementRect.top
      };

      delta = (
        Math.sqrt(
          Math.pow(event.deltaY, 2) +
          Math.pow(event.deltaX, 2)
        ) * sign(event.deltaY) * factor
      );

      // zoom in relative to diagram {x,y} coordinates
      this.zoom(delta, offset);
    } else {

      if (isHorizontalScroll) {
        delta = {
          dx: factor * event.deltaY,
          dy: 0
        };
      } else {
        delta = {
          dx: factor * event.deltaX,
          dy: factor * event.deltaY
        };
      }

      this.scroll(delta);
    }
  };

  /**
   * Zoom with fixed step size.
   *
   * @param {number} delta Zoom delta (1 for zooming in, -1 for zooming out).
   * @param {Point} position
   */
  ZoomScroll.prototype.stepZoom = function stepZoom(delta, position) {

    var stepSize = getStepSize(RANGE, NUM_STEPS);

    this._zoom(delta, position, stepSize);
  };


  /**
   * Zoom in/out given a step size.
   *
   * @param {number} delta
   * @param {Point} position
   * @param {number} stepSize
   */
  ZoomScroll.prototype._zoom = function(delta, position, stepSize) {
    var canvas = this._canvas;

    var direction = delta > 0 ? 1 : -1;

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
   * @param {boolean} [newEnabled] new enabled state
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
      event[newEnabled ? 'bind' : 'unbind'](element, 'wheel', handleWheel, false);
    }

    this._enabled = newEnabled;

    return newEnabled;
  };


  ZoomScroll.prototype._init = function(newEnabled) {
    this.toggle(newEnabled);
  };

  /**
   * @type { import('didi').ModuleDeclaration }
   */
  var ZoomScrollModule = {
    __init__: [ 'zoomScroll' ],
    zoomScroll: [ 'type', ZoomScroll ]
  };

  var CURSOR_CLS_PATTERN = /^djs-cursor-.*$/;

  /**
   * @param {string} mode
   */
  function set$1(mode) {
    var classes = classes$1(document.body);

    classes.removeMatching(CURSOR_CLS_PATTERN);

    if (mode) {
      classes.add('djs-cursor-' + mode);
    }
  }

  function unset() {
    set$1(null);
  }

  /**
   * @typedef {import('../core/EventBus').EventBus} EventBus
   */

  var TRAP_PRIORITY = 5000;

  /**
   * Installs a click trap that prevents a ghost click following a dragging operation.
   *
   * @param {EventBus} eventBus
   * @param {string} [eventName='element.click']
   *
   * @return {() => void} a function to immediately remove the installed trap.
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

  /**
   * @typedef {import('../util/Types').Point} Point
   */


  /**
   * @param {Event} event
   *
   * @return {Point|null}
   */
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

  /**
   * @typedef {import('../../core/Canvas').default} Canvas
   * @typedef {import('../../core/EventBus').default} EventBus
   */

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


    function handleMove(event) {

      var start = context.start,
          button = context.button,
          position = toPoint(event),
          delta$1 = delta(position, start);

      if (!context.dragging && length(delta$1) > THRESHOLD) {
        context.dragging = true;

        if (button === 0) {
          install(eventBus);
        }

        set$1('grab');
      }

      if (context.dragging) {

        var lastPosition = context.last || context.start;

        delta$1 = delta(position, lastPosition);

        canvas.scroll({
          dx: delta$1.x,
          dy: delta$1.y
        });

        context.last = position;
      }

      // prevent select
      event.preventDefault();
    }


    function handleEnd(event$1) {
      event.unbind(document, 'mousemove', handleMove);
      event.unbind(document, 'mouseup', handleEnd);

      context = null;

      unset();
    }

    function handleStart(event$1) {

      // event is already handled by '.djs-draggable'
      if (closest(event$1.target, '.djs-draggable')) {
        return;
      }

      var button = event$1.button;

      // reject right mouse button or modifier key
      if (button >= 2 || event$1.ctrlKey || event$1.shiftKey || event$1.altKey) {
        return;
      }

      context = {
        button: button,
        start: toPoint(event$1)
      };

      event.bind(document, 'mousemove', handleMove);
      event.bind(document, 'mouseup', handleEnd);

      // we've handled the event
      return true;
    }

    this.isActive = function() {
      return !!context;
    };

  }


  MoveCanvas.$inject = [
    'eventBus',
    'canvas'
  ];



  // helpers ///////

  function length(point) {
    return Math.sqrt(Math.pow(point.x, 2) + Math.pow(point.y, 2));
  }

  /**
   * @type { import('didi').ModuleDeclaration }
   */
  var MoveCanvasModule = {
    __init__: [ 'moveCanvas' ],
    moveCanvas: [ 'type', MoveCanvas ]
  };

  var DEFAULT_RENDER_PRIORITY$1 = 1000;

  /**
   * @typedef {import('../core/Types').ElementLike} Element
   * @typedef {import('../core/Types').ConnectionLike} Connection
   * @typedef {import('../core/Types').ShapeLike} Shape
   *
   * @typedef {import('../core/EventBus').default} EventBus
   */

  /**
   * The base implementation of shape and connection renderers.
   *
   * @param {EventBus} eventBus
   * @param {number} [renderPriority=1000]
   */
  function BaseRenderer(eventBus, renderPriority) {
    var self = this;

    renderPriority = renderPriority || DEFAULT_RENDER_PRIORITY$1;

    eventBus.on([ 'render.shape', 'render.connection' ], renderPriority, function(evt, context) {
      var type = evt.type,
          element = context.element,
          visuals = context.gfx,
          attrs = context.attrs;

      if (self.canRender(element)) {
        if (type === 'render.shape') {
          return self.drawShape(visuals, element, attrs);
        } else {
          return self.drawConnection(visuals, element, attrs);
        }
      }
    });

    eventBus.on([ 'render.getShapePath', 'render.getConnectionPath' ], renderPriority, function(evt, element) {
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
   * Checks whether an element can be rendered.
   *
   * @param {Element} element The element to be rendered.
   *
   * @return {boolean} Whether the element can be rendered.
   */
  BaseRenderer.prototype.canRender = function(element) {};

  /**
   * Draws a shape.
   *
   * @param {SVGElement} visuals The SVG element to draw the shape into.
   * @param {Shape} shape The shape to be drawn.
   *
   * @return {SVGElement} The SVG element of the shape drawn.
   */
  BaseRenderer.prototype.drawShape = function(visuals, shape) {};

  /**
   * Draws a connection.
   *
   * @param {SVGElement} visuals The SVG element to draw the connection into.
   * @param {Connection} connection The connection to be drawn.
   *
   * @return {SVGElement} The SVG element of the connection drawn.
   */
  BaseRenderer.prototype.drawConnection = function(visuals, connection) {};

  /**
   * Gets the SVG path of the graphical representation of a shape.
   *
   * @param {Shape} shape The shape.
   *
   * @return {string} The SVG path of the shape.
   */
  BaseRenderer.prototype.getShapePath = function(shape) {};

  /**
   * Gets the SVG path of the graphical representation of a connection.
   *
   * @param {Connection} connection The connection.
   *
   * @return {string} The SVG path of the connection.
   */
  BaseRenderer.prototype.getConnectionPath = function(connection) {};

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
  function attr(node, name, value) {
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
   * Taken from https://github.com/component/classes
   *
   * Without the component bits.
   */

  /**
   * toString reference.
   */

  const toString = Object.prototype.toString;

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

  ClassList.prototype.add = function(name) {
    this.list.add(name);
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

  ClassList.prototype.remove = function(name) {
    if ('[object RegExp]' == toString.call(name)) {
      return this.removeMatching(name);
    }

    this.list.remove(name);
    return this;
  };

  /**
    * Remove all classes matching `re`.
    *
    * @param {RegExp} re
    * @return {ClassList}
    * @api private
    */

  ClassList.prototype.removeMatching = function(re) {
    const arr = this.array();
    for (let i = 0; i < arr.length; i++) {
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

  ClassList.prototype.toggle = function(name, force) {
    if ('undefined' !== typeof force) {
      if (force !== this.list.toggle(name, force)) {
        this.list.toggle(name); // toggle again to correct
      }
    } else {
      this.list.toggle(name);
    }
    return this;
  };

  /**
    * Return an array of classes.
    *
    * @return {Array}
    * @api public
    */

  ClassList.prototype.array = function() {
    return Array.from(this.list);
  };

  /**
    * Check if class `name` is present.
    *
    * @param {String} name
    * @return {ClassList}
    * @api public
    */

  ClassList.prototype.has =
   ClassList.prototype.contains = function(name) {
     return this.list.contains(name);
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
  function clear(element) {
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

  function parse(svg) {

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
  function create$1(name, attrs) {
    var element;

    if (name.charAt(0) === '<') {
      element = parse(name).firstChild;
      element = document.importNode(element, true);
    } else {
      element = document.createElementNS(ns.svg, name);
    }

    if (attrs) {
      attr(element, attrs);
    }

    return element;
  }

  /**
   * Geometry helpers
   */

  // fake node used to instantiate svg geometry elements
  var node = null;

  function getNode() {
    if (node === null) {
      node = create$1('svg');
    }

    return node;
  }

  function extend$1(object, props) {
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
    var matrix = getNode().createSVGMatrix();

    switch (arguments.length) {
    case 0:
      return matrix;
    case 1:
      return extend$1(matrix, a);
    case 6:
      return extend$1(matrix, {
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
      return getNode().createSVGTransformFromMatrix(matrix);
    } else {
      return getNode().createSVGTransform();
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


  function set(element, svg) {

    var parsed = parse(svg);

    // clear element contents
    clear(element);

    if (!svg) {
      return;
    }

    if (!isFragment(parsed)) {

      // extract <svg> from parsed document
      parsed = parsed.documentElement;
    }

    var nodes = slice$1(parsed.childNodes);

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
        set(element, svg);
      } catch (e) {
        throw new Error('error parsing SVG: ' + e.message);
      }

      return element;
    } else {
      return get(element);
    }
  }


  function slice$1(arr) {
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

  /**
   * @typedef {(string|number)[]} Component
   *
   * @typedef {import('../util/Types').Point} Point
   */

  /**
   * @param {Component[] | Component[][]} elements
   *
   * @return {string}
   */
  function componentsToPath(elements) {
    return elements.flat().join(',').replace(/,?([A-z]),?/g, '$1');
  }

  /**
   * @param {Point} point
   *
   * @return {Component[]}
   */
  function move(point) {
    return [ 'M', point.x, point.y ];
  }

  /**
   * @param {Point} point
   *
   * @return {Component[]}
   */
  function lineTo(point) {
    return [ 'L', point.x, point.y ];
  }

  /**
   * @param {Point} p1
   * @param {Point} p2
   * @param {Point} p3
   *
   * @return {Component[]}
   */
  function curveTo(p1, p2, p3) {
    return [ 'C', p1.x, p1.y, p2.x, p2.y, p3.x, p3.y ];
  }

  /**
   * @param {Point[]} waypoints
   * @param {number} [cornerRadius]
   * @return {Component[][]}
   */
  function drawPath$1(waypoints, cornerRadius) {
    const pointCount = waypoints.length;

    const path = [ move(waypoints[0]) ];

    for (let i = 1; i < pointCount; i++) {

      const pointBefore = waypoints[i - 1];
      const point = waypoints[i];
      const pointAfter = waypoints[i + 1];

      if (!pointAfter || !cornerRadius) {
        path.push(lineTo(point));

        continue;
      }

      const effectiveRadius = Math.min(
        cornerRadius,
        vectorLength(point.x - pointBefore.x, point.y - pointBefore.y),
        vectorLength(pointAfter.x - point.x, pointAfter.y - point.y)
      );

      if (!effectiveRadius) {
        path.push(lineTo(point));

        continue;
      }

      const beforePoint = getPointAtLength(point, pointBefore, effectiveRadius);
      const beforePoint2 = getPointAtLength(point, pointBefore, effectiveRadius * .5);

      const afterPoint = getPointAtLength(point, pointAfter, effectiveRadius);
      const afterPoint2 = getPointAtLength(point, pointAfter, effectiveRadius * .5);

      path.push(lineTo(beforePoint));
      path.push(curveTo(beforePoint2, afterPoint2, afterPoint));
    }

    return path;
  }

  function getPointAtLength(start, end, length) {

    const deltaX = end.x - start.x;
    const deltaY = end.y - start.y;

    const totalLength = vectorLength(deltaX, deltaY);

    const percent = length / totalLength;

    return {
      x: start.x + deltaX * percent,
      y: start.y + deltaY * percent
    };
  }

  function vectorLength(x, y) {
    return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
  }

  /**
   * @param {Point[]} points
   * @param {number|Object} [attrs]
   * @param {number} [radius]
   *
   * @return {SVGElement}
   */
  function createLine(points, attrs, radius) {

    if (isNumber(attrs)) {
      radius = attrs;
      attrs = null;
    }

    if (!attrs) {
      attrs = {};
    }

    const line = create$1('path', attrs);

    if (isNumber(radius)) {
      line.dataset.cornerRadius = String(radius);
    }

    return updateLine(line, points);
  }

  /**
   * @param {SVGElement} gfx
   * @param {Point[]} points
   *
   * @return {SVGElement}
   */
  function updateLine(gfx, points) {

    const cornerRadius = parseInt(gfx.dataset.cornerRadius, 10) || 0;

    attr(gfx, {
      d: componentsToPath(drawPath$1(points, cornerRadius))
    });

    return gfx;
  }

  function drawText(parentGfx, text, options, textRenderer) {
    var svgText = textRenderer.createText(text, options);

    classes(svgText).add('djs-label');
    append(parentGfx, svgText);

    return svgText;
  }

  function drawLine(parentGfx, source, target) {
    var line = create$1('line');

    attr(line, {
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
    var path = create$1('path');

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
      var group = create$1('g');
      
      classes(group).add('djs-visual');

      elements.forEach(function(element) {
          append(group, element);
      });

      return group;
  }
  function addPath(path, d) {
    attr(path, { d: d });
  }
  function addPathTransformation(path, attributes) {
    if('transform' in attributes) {
      attr(path, { transform: attributes.transform });
    }
  }
  function addStyles(svgElement, attributes) {
    if('stroke' in attributes) {
      attr(svgElement, { stroke: attributes.stroke });
    }

    if('stroke-width' in attributes) {
      attr(svgElement, { 'stroke-width': attributes['stroke-width'] });
    }

    if('fill' in attributes) {
      attr(svgElement, { fill: attributes.fill });
    }

    if('stroke-dasharray' in attributes) {
      attr(svgElement, { 'stroke-dasharray': attributes['stroke-dasharray'] });
    }
  }
  function addPathMarkers(path, attributes) {
    if('markerEnd' in attributes) {
      attr(path, { 'marker-end': 'url(#' + attributes.markerEnd + ')' });
    }

    if('markerStart' in attributes) {
      attr(path, { 'marker-start': 'url(#' + attributes.markerStart + ')' });
    }
  }
  function getRectangle(width, height, attributes) {
    var rectangle = create$1('rect');

    addSize(rectangle, width, height);
    addStyles(rectangle, attributes);

    return rectangle;
  }
  function addSize(shape, width, height) {
    attr(shape, {
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

  function addHiddenElementStyles(element) {
    element.style.setProperty('opacity', 0.3);
  }
  function removeHiddenElementStyles(element) {
    element.style.removeProperty('opacity');
  }
  function setVisibilityStyles(parentGfx, element) {
    var groupElement = parentGfx.closest('.djs-group');
    
    if(element.parent && element.parent.isVisible) {
  	  if(element.isVisible) {
  		removeHiddenElementStyles(groupElement);
  	  } else {
  		addHiddenElementStyles(groupElement);
  	  }
    } else {
  	removeHiddenElementStyles(groupElement);
    }
  }

  function drawClass(parentGfx, element, textRenderer) {
    element.businessObject || {};

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
    
    setVisibilityStyles(parentGfx, element);

    return rectangle;
  }
  function drawLabel(parentGfx, element, textRenderer) {
    var text = drawText(parentGfx, element.businessObject + '', getGeneralLabelStyle(), textRenderer);
    
    setVisibilityStyles(parentGfx, element);
    
    return text;
  }
  function drawClassSeparator(parentGfx, width, y) {
    return drawLine(parentGfx, {
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
    var connection = drawPath(parentGfx, element.waypoints, attributes);
    
    setVisibilityStyles(parentGfx, element);
    
    return connection;
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
      defs = create$1('defs');

      append(canvas._svg, defs);
    }

    markers.forEach(function(marker) {
      var markerCopy = marker.element.cloneNode(true);

      addMarkerDefinition(defs, marker.name, marker.element, false);
      addMarkerDefinition(defs, marker.name + '-reversed', markerCopy, true);
    });
  }

  function addMarkerDefinition(definitions, id, markerElement, atStart) {
    var svgMarker = create$1('marker');

    append(svgMarker, markerElement);

    attr(svgMarker, {
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

  function getDefaultExportFromCjs (x) {
  	return x && x.__esModule && Object.prototype.hasOwnProperty.call(x, 'default') ? x['default'] : x;
  }

  var inherits$1 = {exports: {}};

  var inherits_browser = {exports: {}};

  var hasRequiredInherits_browser;

  function requireInherits_browser () {
  	if (hasRequiredInherits_browser) return inherits_browser.exports;
  	hasRequiredInherits_browser = 1;
  	if (typeof Object.create === 'function') {
  	  // implementation from standard node.js 'util' module
  	  inherits_browser.exports = function inherits(ctor, superCtor) {
  	    if (superCtor) {
  	      ctor.super_ = superCtor;
  	      ctor.prototype = Object.create(superCtor.prototype, {
  	        constructor: {
  	          value: ctor,
  	          enumerable: false,
  	          writable: true,
  	          configurable: true
  	        }
  	      });
  	    }
  	  };
  	} else {
  	  // old school shim for old browsers
  	  inherits_browser.exports = function inherits(ctor, superCtor) {
  	    if (superCtor) {
  	      ctor.super_ = superCtor;
  	      var TempCtor = function () {};
  	      TempCtor.prototype = superCtor.prototype;
  	      ctor.prototype = new TempCtor();
  	      ctor.prototype.constructor = ctor;
  	    }
  	  };
  	}
  	return inherits_browser.exports;
  }

  try {
    var util = require('util');
    /* istanbul ignore next */
    if (typeof util.inherits !== 'function') throw '';
    inherits$1.exports = util.inherits;
  } catch (e) {
    /* istanbul ignore next */
    inherits$1.exports = requireInherits_browser();
  }

  var inheritsExports = inherits$1.exports;
  var inherits = /*@__PURE__*/getDefaultExportFromCjs(inheritsExports);

  inherits(UmlRenderer, BaseRenderer);

  UmlRenderer.$inject = ['eventBus', 'canvas', 'textRenderer', 'layouter'];

  function UmlRenderer(eventBus, canvas, textRenderer, layouter) {
    BaseRenderer.call(this, eventBus);

    this.textRenderer = textRenderer;
    this._layouter = layouter;

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
    var svgText = create$1('text');

    var options = assign(options, this._style);

    attr(svgText, options);

    innerSVG(svgText, text);

    return svgText;
  };

  TextRenderer.prototype.getDimensions  = function(text, options) {
      var helperText = create$1('text');

      attr(helperText, this._style);

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
    var helperSvg = create$1('svg');

    attr(helperSvg, getHelperSvgOptions());

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

  var DrawModule$1 = {
    __init__: [ 'umlRenderer'],
    umlRenderer: ['type', UmlRenderer],
    textRenderer: [ 'type', TextRenderer]
  };

  /**
   * @typedef {import('../util/Types').Axis} Axis
   * @typedef {import('../util/Types').Point} Point
   * @typedef {import('../util/Types').Rect} Rect
   */

  /**
   * Computes the distance between two points.
   *
   * @param {Point} a
   * @param {Point} b
   *
   * @return {number} The distance between the two points.
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
   * Returns true if the point r is on the line between p and q.
   *
   * @param {Point} p
   * @param {Point} q
   * @param {Point} r
   * @param {number} [accuracy=5] The accuracy with which to check (lower is better).
   *
   * @return {boolean}
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
   * Convert the given bounds to a { top, left, bottom, right } descriptor.
   *
   * @param {Point|Rect} bounds
   *
   * @return {RectTRBL}
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
   * @param {Rect} rect
   * @param {Rect} reference
   * @param {Point|number} padding
   *
   * @return {DirectionTRBL} the orientation; one of top, top-left, left, ..., bottom, right or intersect.
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

  /**
   * @typedef {import('../util/Types').Point} Point
   * @typedef {import('../util/Types').Rect} Rect
   */

  var MIN_SEGMENT_LENGTH = 20,
      POINT_ORIENTATION_PADDING = 5;

  var round$1 = Math.round;

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

    var xmid = round$1((b.x - a.x) / 2 + a.x),
        ymid = round$1((b.y - a.y) / 2 + a.y);

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

    var xmid = round$1((b.x - a.x) / 2 + a.x),
        ymid = round$1((b.y - a.y) / 2 + a.y);

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
   * @param {Point} a
   * @param {Point} b
   * @param {string} directions
   *
   * @return {Point[]}
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
   * @param {string} [directions='h:h'] Specifies manhattan directions for each
   * point as {direction}:{direction}. A direction for a point is either
   * `h` (horizontal) or `v` (vertical).
   *
   * @return {Point[]}
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
   * @param {Point[]} waypoints
   *
   * @return {Point[]}
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
    this.layouter;
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

      modeling.createConnection(nodeMap.get(edge.source), nodeMap.get(edge.target), attrs, root);
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

  var CoreModule$1 = {
    __depends__: [
      DrawModule$1,
      ImporterModule
    ]
  };

  const CLASS_PATTERN = /^class[ {]/;


  /**
   * @param {function} fn
   *
   * @return {boolean}
   */
  function isClass(fn) {
    return CLASS_PATTERN.test(fn.toString());
  }

  /**
   * @param {any} obj
   *
   * @return {boolean}
   */
  function isArray(obj) {
    return Array.isArray(obj);
  }

  /**
   * @param {any} obj
   * @param {string} prop
   *
   * @return {boolean}
   */
  function hasOwnProp(obj, prop) {
    return Object.prototype.hasOwnProperty.call(obj, prop);
  }

  /**
   * @typedef {import('./index').InjectAnnotated } InjectAnnotated
   */

  /**
   * @template T
   *
   * @params {[...string[], T] | ...string[], T} args
   *
   * @return {T & InjectAnnotated}
   */
  function annotate(...args) {

    if (args.length === 1 && isArray(args[0])) {
      args = args[0];
    }

    args = [ ...args ];

    const fn = args.pop();

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

  const CONSTRUCTOR_ARGS = /constructor\s*[^(]*\(\s*([^)]*)\)/m;
  const FN_ARGS = /^(?:async\s+)?(?:function\s*[^(]*)?(?:\(\s*([^)]*)\)|(\w+))/m;
  const FN_ARG = /\/\*([^*]*)\*\//m;

  /**
   * @param {unknown} fn
   *
   * @return {string[]}
   */
  function parseAnnotations(fn) {

    if (typeof fn !== 'function') {
      throw new Error(`Cannot annotate "${fn}". Expected a function!`);
    }

    const match = fn.toString().match(isClass(fn) ? CONSTRUCTOR_ARGS : FN_ARGS);

    // may parse class without constructor
    if (!match) {
      return [];
    }

    const args = match[1] || match[2];

    return args && args.split(',').map(arg => {
      const argMatch = arg.match(FN_ARG);
      return (argMatch && argMatch[1] || arg).trim();
    }) || [];
  }

  /**
   * @typedef { import('./index').ModuleDeclaration } ModuleDeclaration
   * @typedef { import('./index').ModuleDefinition } ModuleDefinition
   * @typedef { import('./index').InjectorContext } InjectorContext
   */

  /**
   * Create a new injector with the given modules.
   *
   * @param {ModuleDefinition[]} modules
   * @param {InjectorContext} [parent]
   */
  function Injector(modules, parent) {
    parent = parent || {
      get: function(name, strict) {
        currentlyResolving.push(name);

        if (strict === false) {
          return null;
        } else {
          throw error(`No provider for "${ name }"!`);
        }
      }
    };

    const currentlyResolving = [];
    const providers = this._providers = Object.create(parent._providers || null);
    const instances = this._instances = Object.create(null);

    const self = instances.injector = this;

    const error = function(msg) {
      const stack = currentlyResolving.join(' -> ');
      currentlyResolving.length = 0;
      return new Error(stack ? `${ msg } (Resolving: ${ stack })` : msg);
    };

    /**
     * Return a named service.
     *
     * @param {string} name
     * @param {boolean} [strict=true] if false, resolve missing services to null
     *
     * @return {any}
     */
    function get(name, strict) {
      if (!providers[name] && name.indexOf('.') !== -1) {
        const parts = name.split('.');
        let pivot = get(parts.shift());

        while (parts.length) {
          pivot = pivot[parts.shift()];
        }

        return pivot;
      }

      if (hasOwnProp(instances, name)) {
        return instances[name];
      }

      if (hasOwnProp(providers, name)) {
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
    }

    function fnDef(fn, locals) {

      if (typeof locals === 'undefined') {
        locals = {};
      }

      if (typeof fn !== 'function') {
        if (isArray(fn)) {
          fn = annotate(fn.slice());
        } else {
          throw error(`Cannot invoke "${ fn }". Expected a function!`);
        }
      }

      const inject = fn.$inject || parseAnnotations(fn);
      const dependencies = inject.map(dep => {
        if (hasOwnProp(locals, dep)) {
          return locals[dep];
        } else {
          return get(dep);
        }
      });

      return {
        fn: fn,
        dependencies: dependencies
      };
    }

    function instantiate(Type) {
      const {
        fn,
        dependencies
      } = fnDef(Type);

      // instantiate var args constructor
      const Constructor = Function.prototype.bind.apply(fn, [ null ].concat(dependencies));

      return new Constructor();
    }

    function invoke(func, context, locals) {
      const {
        fn,
        dependencies
      } = fnDef(func, locals);

      return fn.apply(context, dependencies);
    }

    /**
     * @param {Injector} childInjector
     *
     * @return {Function}
     */
    function createPrivateInjectorFactory(childInjector) {
      return annotate(key => childInjector.get(key));
    }

    /**
     * @param {ModuleDefinition[]} modules
     * @param {string[]} [forceNewInstances]
     *
     * @return {Injector}
     */
    function createChild(modules, forceNewInstances) {
      if (forceNewInstances && forceNewInstances.length) {
        const fromParentModule = Object.create(null);
        const matchedScopes = Object.create(null);

        const privateInjectorsCache = [];
        const privateChildInjectors = [];
        const privateChildFactories = [];

        let provider;
        let cacheIdx;
        let privateChildInjector;
        let privateChildInjectorFactory;

        for (let name in providers) {
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
                fromParentModule[name] = [ privateChildInjectorFactory, name, 'private', privateChildInjector ];
              } else {
                fromParentModule[name] = [ privateChildFactories[cacheIdx], name, 'private', privateChildInjectors[cacheIdx] ];
              }
            } else {
              fromParentModule[name] = [ provider[2], provider[1] ];
            }
            matchedScopes[name] = true;
          }

          if ((provider[2] === 'factory' || provider[2] === 'type') && provider[1].$scope) {
            /* jshint -W083 */
            forceNewInstances.forEach(scope => {
              if (provider[1].$scope.indexOf(scope) !== -1) {
                fromParentModule[name] = [ provider[2], provider[1] ];
                matchedScopes[scope] = true;
              }
            });
          }
        }

        forceNewInstances.forEach(scope => {
          if (!matchedScopes[scope]) {
            throw new Error('No provider for "' + scope + '". Cannot use provider from the parent!');
          }
        });

        modules.unshift(fromParentModule);
      }

      return new Injector(modules, self);
    }

    const factoryMap = {
      factory: invoke,
      type: instantiate,
      value: function(value) {
        return value;
      }
    };

    /**
     * @param {ModuleDefinition} moduleDefinition
     * @param {Injector} injector
     */
    function createInitializer(moduleDefinition, injector) {

      const initializers = moduleDefinition.__init__ || [];

      return function() {
        initializers.forEach(initializer => {

          // eagerly resolve component (fn or string)
          if (typeof initializer === 'string') {
            injector.get(initializer);
          } else {
            injector.invoke(initializer);
          }
        });
      };
    }

    /**
     * @param {ModuleDefinition} moduleDefinition
     */
    function loadModule(moduleDefinition) {

      const moduleExports = moduleDefinition.__exports__;

      // private module
      if (moduleExports) {
        const nestedModules = moduleDefinition.__modules__;

        const clonedModule = Object.keys(moduleDefinition).reduce((clonedModule, key) => {

          if (key !== '__exports__' && key !== '__modules__' && key !== '__init__' && key !== '__depends__') {
            clonedModule[key] = moduleDefinition[key];
          }

          return clonedModule;
        }, Object.create(null));

        const childModules = (nestedModules || []).concat(clonedModule);

        const privateInjector = createChild(childModules);
        const getFromPrivateInjector = annotate(function(key) {
          return privateInjector.get(key);
        });

        moduleExports.forEach(function(key) {
          providers[key] = [ getFromPrivateInjector, key, 'private', privateInjector ];
        });

        // ensure child injector initializes
        const initializers = (moduleDefinition.__init__ || []).slice();

        initializers.unshift(function() {
          privateInjector.init();
        });

        moduleDefinition = Object.assign({}, moduleDefinition, {
          __init__: initializers
        });

        return createInitializer(moduleDefinition, privateInjector);
      }

      // normal module
      Object.keys(moduleDefinition).forEach(function(key) {

        if (key === '__init__' || key === '__depends__') {
          return;
        }

        if (moduleDefinition[key][2] === 'private') {
          providers[key] = moduleDefinition[key];
          return;
        }

        const type = moduleDefinition[key][0];
        const value = moduleDefinition[key][1];

        providers[key] = [ factoryMap[type], arrayUnwrap(type, value), type ];
      });

      return createInitializer(moduleDefinition, self);
    }

    /**
     * @param {ModuleDefinition[]} moduleDefinitions
     * @param {ModuleDefinition} moduleDefinition
     *
     * @return {ModuleDefinition[]}
     */
    function resolveDependencies(moduleDefinitions, moduleDefinition) {

      if (moduleDefinitions.indexOf(moduleDefinition) !== -1) {
        return moduleDefinitions;
      }

      moduleDefinitions = (moduleDefinition.__depends__ || []).reduce(resolveDependencies, moduleDefinitions);

      if (moduleDefinitions.indexOf(moduleDefinition) !== -1) {
        return moduleDefinitions;
      }

      return moduleDefinitions.concat(moduleDefinition);
    }

    /**
     * @param {ModuleDefinition[]} moduleDefinitions
     *
     * @return { () => void } initializerFn
     */
    function bootstrap(moduleDefinitions) {

      const initializers = moduleDefinitions
        .reduce(resolveDependencies, [])
        .map(loadModule);

      let initialized = false;

      return function() {

        if (initialized) {
          return;
        }

        initialized = true;

        initializers.forEach(initializer => initializer());
      };
    }

    // public API
    this.get = get;
    this.invoke = invoke;
    this.instantiate = instantiate;
    this.createChild = createChild;

    // setup
    this.init = bootstrap(modules);
  }


  // helpers ///////////////

  function arrayUnwrap(type, value) {
    if (type !== 'value' && isArray(value)) {
      value = annotate(value.slice());
    }

    return value;
  }

  function e(e,t){t&&(e.super_=t,e.prototype=Object.create(t.prototype,{constructor:{value:e,enumerable:!1,writable:!0,configurable:!0}}));}

  /**
   * Returns the surrounding bbox for all elements in
   * the array or the element primitive.
   *
   * @param {Element|Element[]} elements
   * @param {boolean} [stopRecursion=false]
   *
   * @return {Rect}
   */
  function getBBox(elements, stopRecursion) {

    stopRecursion = !!stopRecursion;
    if (!isArray$1(elements)) {
      elements = [ elements ];
    }

    var minX,
        minY,
        maxX,
        maxY;

    forEach(elements, function(element) {

      // If element is a connection the bbox must be computed first
      var bbox = element;
      if (element.waypoints && !stopRecursion) {
        bbox = getBBox(element.waypoints, true);
      }

      var x = bbox.x,
          y = bbox.y,
          height = bbox.height || 0,
          width = bbox.width || 0;

      if (x < minX || minX === undefined) {
        minX = x;
      }
      if (y < minY || minY === undefined) {
        minY = y;
      }

      if ((x + width) > maxX || maxX === undefined) {
        maxX = x + width;
      }
      if ((y + height) > maxY || maxY === undefined) {
        maxY = y + height;
      }
    });

    return {
      x: minX,
      y: minY,
      height: maxY - minY,
      width: maxX - minX
    };
  }

  /**
   * Get the element's type
   *
   * @param {Element} element
   *
   * @return {'connection' | 'shape' | 'root'}
   */
  function getType(element) {

    if ('waypoints' in element) {
      return 'connection';
    }

    if ('x' in element) {
      return 'shape';
    }

    return 'root';
  }

  /**
   * @param {Element} element
   *
   * @return {boolean}
   */
  function isFrameElement(element) {
    return !!(element && element.isFrame);
  }

  /**
   * @typedef {import('../core/EventBus').default} EventBus
   * @typedef {import('./Styles').default} Styles
   */

  // apply default renderer with lowest possible priority
  // so that it only kicks in if noone else could render
  var DEFAULT_RENDER_PRIORITY = 1;

  /**
   * The default renderer used for shapes and connections.
   *
   * @param {EventBus} eventBus
   * @param {Styles} styles
   */
  function DefaultRenderer(eventBus, styles) {

    BaseRenderer.call(this, eventBus, DEFAULT_RENDER_PRIORITY);

    this.CONNECTION_STYLE = styles.style([ 'no-fill' ], { strokeWidth: 5, stroke: 'fuchsia' });
    this.SHAPE_STYLE = styles.style({ fill: 'white', stroke: 'fuchsia', strokeWidth: 2 });
    this.FRAME_STYLE = styles.style([ 'no-fill' ], { stroke: 'fuchsia', strokeDasharray: 4, strokeWidth: 2 });
  }

  e(DefaultRenderer, BaseRenderer);


  /**
   * @private
   */
  DefaultRenderer.prototype.canRender = function() {
    return true;
  };

  /**
   * @private
   */
  DefaultRenderer.prototype.drawShape = function drawShape(visuals, element, attrs) {
    var rect = create$1('rect');

    attr(rect, {
      x: 0,
      y: 0,
      width: element.width || 0,
      height: element.height || 0
    });

    if (isFrameElement(element)) {
      attr(rect, assign({}, this.FRAME_STYLE, attrs || {}));
    } else {
      attr(rect, assign({}, this.SHAPE_STYLE, attrs || {}));
    }

    append(visuals, rect);

    return rect;
  };

  /**
   * @private
   */
  DefaultRenderer.prototype.drawConnection = function drawConnection(visuals, connection, attrs) {

    var line = createLine(connection.waypoints, assign({}, this.CONNECTION_STYLE, attrs || {}));
    append(visuals, line);

    return line;
  };

  /**
   * @private
   */
  DefaultRenderer.prototype.getShapePath = function getShapePath(shape) {

    var x = shape.x,
        y = shape.y,
        width = shape.width,
        height = shape.height;

    var shapePath = [
      [ 'M', x, y ],
      [ 'l', width, 0 ],
      [ 'l', 0, height ],
      [ 'l', -width, 0 ],
      [ 'z' ]
    ];

    return componentsToPath(shapePath);
  };

  /**
   * @private
   */
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
     * Builds a style definition from a className, a list of traits and an object
     * of additional attributes.
     *
     * @param {string} className
     * @param {string[]} [traits]
     * @param {Object} [additionalAttrs]
     *
     * @return {Object} the style definition
     */
    this.cls = function(className, traits, additionalAttrs) {
      var attrs = this.style(traits, additionalAttrs);

      return assign(attrs, { 'class': className });
    };

    /**
     * Builds a style definition from a list of traits and an object of additional
     * attributes.
     *
     * @param {string[]} [traits]
     * @param {Object} additionalAttrs
     *
     * @return {Object} the style definition
     */
    this.style = function(traits, additionalAttrs) {

      if (!isArray$1(traits) && !additionalAttrs) {
        additionalAttrs = traits;
        traits = [];
      }

      var attrs = reduce(traits, function(attrs, t) {
        return assign(attrs, defaultTraits[t] || {});
      }, {});

      return additionalAttrs ? assign(attrs, additionalAttrs) : attrs;
    };


    /**
     * Computes a style definition from a list of traits and an object of
     * additional attributes, with custom style definition object.
     *
     * @param {Object} custom
     * @param {string[]} [traits]
     * @param {Object} defaultStyles
     *
     * @return {Object} the style definition
     */
    this.computeStyle = function(custom, traits, defaultStyles) {
      if (!isArray$1(traits)) {
        defaultStyles = traits;
        traits = [];
      }

      return self.style(traits || [], assign({}, defaultStyles, custom || {}));
    };
  }

  /**
   * @type { import('didi').ModuleDeclaration }
   */
  var DrawModule = {
    __init__: [ 'defaultRenderer' ],
    defaultRenderer: [ 'type', DefaultRenderer ],
    styles: [ 'type', Styles ]
  };

  /**
   * Failsafe remove an element from a collection
   *
   * @param {Array<Object>} [collection]
   * @param {Object} [element]
   *
   * @return {number} the previous index of the element
   */
  function remove(collection, element) {

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
   * @param {number} [idx]
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

  /**
   * @typedef {import('./Types').ConnectionLike} ConnectionLike
   * @typedef {import('./Types').RootLike} RootLike
   * @typedef {import('./Types').ParentLike } ParentLike
   * @typedef {import('./Types').ShapeLike} ShapeLike
   *
   * @typedef { {
   *   container?: HTMLElement;
   *   deferUpdate?: boolean;
   *   width?: number;
   *   height?: number;
   * } } CanvasConfig
   * @typedef { {
   *   group: SVGElement;
   *   index: number;
   *   visible: boolean;
   * } } CanvasLayer
   * @typedef { {
   *   [key: string]: CanvasLayer;
   * } } CanvasLayers
   * @typedef { {
   *   rootElement: ShapeLike;
   *   layer: CanvasLayer;
   * } } CanvasPlane
   * @typedef { {
   *   scale: number;
   *   inner: Rect;
   *   outer: Dimensions;
   * } & Rect } CanvasViewbox
   *
   * @typedef {import('./ElementRegistry').default} ElementRegistry
   * @typedef {import('./EventBus').default} EventBus
   * @typedef {import('./GraphicsFactory').default} GraphicsFactory
   *
   * @typedef {import('../util/Types').Dimensions} Dimensions
   * @typedef {import('../util/Types').Point} Point
   * @typedef {import('../util/Types').Rect} Rect
   * @typedef {import('../util/Types').RectTRBL} RectTRBL
   */

  function round(number, resolution) {
    return Math.round(number * resolution) / resolution;
  }

  function ensurePx(number) {
    return isNumber(number) ? number + 'px' : number;
  }

  function findRoot(element) {
    while (element.parent) {
      element = element.parent;
    }

    return element;
  }

  /**
   * Creates a HTML container element for a SVG element with
   * the given configuration
   *
   * @param {CanvasConfig} options
   *
   * @return {HTMLElement} the container element
   */
  function createContainer(options) {

    options = assign({}, { width: '100%', height: '100%' }, options);

    const container = options.container || document.body;

    // create a <div> around the svg element with the respective size
    // this way we can always get the correct container size
    // (this is impossible for <svg> elements at the moment)
    const parent = document.createElement('div');
    parent.setAttribute('class', 'djs-container djs-parent');

    assign$1(parent, {
      position: 'relative',
      overflow: 'hidden',
      width: ensurePx(options.width),
      height: ensurePx(options.height)
    });

    container.appendChild(parent);

    return parent;
  }

  function createGroup(parent, cls, childIndex) {
    const group = create$1('g');
    classes(group).add(cls);

    const index = childIndex !== undefined ? childIndex : parent.childNodes.length - 1;

    // must ensure second argument is node or _null_
    // cf. https://developer.mozilla.org/en-US/docs/Web/API/Node/insertBefore
    parent.insertBefore(group, parent.childNodes[index] || null);

    return group;
  }

  const BASE_LAYER = 'base';

  // render plane contents behind utility layers
  const PLANE_LAYER_INDEX = 0;
  const UTILITY_LAYER_INDEX = 1;


  const REQUIRED_MODEL_ATTRS = {
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
   * @param {CanvasConfig|null} config
   * @param {EventBus} eventBus
   * @param {GraphicsFactory} graphicsFactory
   * @param {ElementRegistry} elementRegistry
   */
  function Canvas(config, eventBus, graphicsFactory, elementRegistry) {
    this._eventBus = eventBus;
    this._elementRegistry = elementRegistry;
    this._graphicsFactory = graphicsFactory;

    /**
     * @type {number}
     */
    this._rootsIdx = 0;

    /**
     * @type {CanvasLayers}
     */
    this._layers = {};

    /**
     * @type {CanvasPlane[]}
     */
    this._planes = [];

    /**
     * @type {RootLike|null}
     */
    this._rootElement = null;

    this._init(config || {});
  }

  Canvas.$inject = [
    'config.canvas',
    'eventBus',
    'graphicsFactory',
    'elementRegistry'
  ];

  /**
   * Creates a <svg> element that is wrapped into a <div>.
   * This way we are always able to correctly figure out the size of the svg element
   * by querying the parent node.

   * (It is not possible to get the size of a svg element cross browser @ 2014-04-01)

   * <div class="djs-container" style="width: {desired-width}, height: {desired-height}">
   *   <svg width="100%" height="100%">
   *    ...
   *   </svg>
   * </div>
   *
   * @param {CanvasConfig} config
   */
  Canvas.prototype._init = function(config) {

    const eventBus = this._eventBus;

    // html container
    const container = this._container = createContainer(config);

    const svg = this._svg = create$1('svg');
    attr(svg, { width: '100%', height: '100%' });

    append(container, svg);

    const viewport = this._viewport = createGroup(svg, 'viewport');

    // debounce canvas.viewbox.changed events when deferUpdate is set
    // to help with potential performance issues
    if (config.deferUpdate) {
      this._viewboxChanged = debounce(bind(this._viewboxChanged, this), 300);
    }

    eventBus.on('diagram.init', () => {

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

    });

    // reset viewbox on shape changes to
    // recompute the viewbox
    eventBus.on([
      'shape.added',
      'connection.added',
      'shape.removed',
      'connection.removed',
      'elements.changed',
      'root.set'
    ], () => {
      delete this._cachedViewbox;
    });

    eventBus.on('diagram.destroy', 500, this._destroy, this);
    eventBus.on('diagram.clear', 500, this._clear, this);
  };

  Canvas.prototype._destroy = function() {
    this._eventBus.fire('canvas.destroy', {
      svg: this._svg,
      viewport: this._viewport
    });

    const parent = this._container.parentNode;

    if (parent) {
      parent.removeChild(this._container);
    }

    delete this._svg;
    delete this._container;
    delete this._layers;
    delete this._planes;
    delete this._rootElement;
    delete this._viewport;
  };

  Canvas.prototype._clear = function() {

    const allElements = this._elementRegistry.getAll();

    // remove all elements
    allElements.forEach(element => {
      const type = getType(element);

      if (type === 'root') {
        this.removeRootElement(element);
      } else {
        this._removeElement(element, type);
      }
    });

    // remove all planes
    this._planes = [];
    this._rootElement = null;

    // force recomputation of view box
    delete this._cachedViewbox;
  };

  /**
   * Returns the default layer on which
   * all elements are drawn.
   *
   * @return {SVGElement}  The SVG element of the layer.
   */
  Canvas.prototype.getDefaultLayer = function() {
    return this.getLayer(BASE_LAYER, PLANE_LAYER_INDEX);
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
   * @param {string} name The name of the layer.
   * @param {number} [index] The index of the layer.
   *
   * @return {SVGElement} The SVG element of the layer.
   */
  Canvas.prototype.getLayer = function(name, index) {

    if (!name) {
      throw new Error('must specify a name');
    }

    let layer = this._layers[name];

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
   * For a given index, return the number of layers that have a higher index and
   * are visible.
   *
   * This is used to determine the node a layer should be inserted at.
   *
   * @param {number} index
   *
   * @return {number}
   */
  Canvas.prototype._getChildIndex = function(index) {
    return reduce(this._layers, function(childIndex, layer) {
      if (layer.visible && index >= layer.index) {
        childIndex++;
      }

      return childIndex;
    }, 0);
  };

  /**
   * Creates a given layer and returns it.
   *
   * @param {string} name
   * @param {number} [index=0]
   *
   * @return {CanvasLayer}
   */
  Canvas.prototype._createLayer = function(name, index) {

    if (typeof index === 'undefined') {
      index = UTILITY_LAYER_INDEX;
    }

    const childIndex = this._getChildIndex(index);

    return {
      group: createGroup(this._viewport, 'layer-' + name, childIndex),
      index: index,
      visible: true
    };
  };


  /**
   * Shows a given layer.
   *
   * @param {string} name The name of the layer.
   *
   * @return {SVGElement} The SVG element of the layer.
   */
  Canvas.prototype.showLayer = function(name) {

    if (!name) {
      throw new Error('must specify a name');
    }

    const layer = this._layers[name];

    if (!layer) {
      throw new Error('layer <' + name + '> does not exist');
    }

    const viewport = this._viewport;
    const group = layer.group;
    const index = layer.index;

    if (layer.visible) {
      return group;
    }

    const childIndex = this._getChildIndex(index);

    viewport.insertBefore(group, viewport.childNodes[childIndex] || null);

    layer.visible = true;

    return group;
  };

  /**
   * Hides a given layer.
   *
   * @param {string} name The name of the layer.
   *
   * @return {SVGElement} The SVG element of the layer.
   */
  Canvas.prototype.hideLayer = function(name) {

    if (!name) {
      throw new Error('must specify a name');
    }

    const layer = this._layers[name];

    if (!layer) {
      throw new Error('layer <' + name + '> does not exist');
    }

    const group = layer.group;

    if (!layer.visible) {
      return group;
    }

    remove$1(group);

    layer.visible = false;

    return group;
  };


  Canvas.prototype._removeLayer = function(name) {

    const layer = this._layers[name];

    if (layer) {
      delete this._layers[name];

      remove$1(layer.group);
    }
  };

  /**
   * Returns the currently active layer. Can be null.
   *
   * @return {CanvasLayer|null} The active layer of `null`.
   */
  Canvas.prototype.getActiveLayer = function() {
    const plane = this._findPlaneForRoot(this.getRootElement());

    if (!plane) {
      return null;
    }

    return plane.layer;
  };


  /**
   * Returns the plane which contains the given element.
   *
   * @param {ShapeLike|ConnectionLike|string} element The element or its ID.
   *
   * @return {RootLike|undefined} The root of the element.
   */
  Canvas.prototype.findRoot = function(element) {
    if (typeof element === 'string') {
      element = this._elementRegistry.get(element);
    }

    if (!element) {
      return;
    }

    const plane = this._findPlaneForRoot(
      findRoot(element)
    ) || {};

    return plane.rootElement;
  };

  /**
   * Return a list of all root elements on the diagram.
   *
   * @return {(RootLike)[]} The list of root elements.
   */
  Canvas.prototype.getRootElements = function() {
    return this._planes.map(function(plane) {
      return plane.rootElement;
    });
  };

  Canvas.prototype._findPlaneForRoot = function(rootElement) {
    return find(this._planes, function(plane) {
      return plane.rootElement === rootElement;
    });
  };


  /**
   * Returns the html element that encloses the
   * drawing canvas.
   *
   * @return {HTMLElement} The HTML element of the container.
   */
  Canvas.prototype.getContainer = function() {
    return this._container;
  };


  // markers //////////////////////

  Canvas.prototype._updateMarker = function(element, marker, add) {
    let container;

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
        if (add) {
          classes(gfx).add(marker);
        } else {
          classes(gfx).remove(marker);
        }
      }
    });

    /**
     * An event indicating that a marker has been updated for an element
     *
     * @event element.marker.update
     * @type {Object}
     * @property {Element} element the shape
     * @property {SVGElement} gfx the graphical representation of the shape
     * @property {string} marker
     * @property {boolean} add true if the marker was added, false if it got removed
     */
    this._eventBus.fire('element.marker.update', { element: element, gfx: container.gfx, marker: marker, add: !!add });
  };


  /**
   * Adds a marker to an element (basically a css class).
   *
   * Fires the element.marker.update event, making it possible to
   * integrate extension into the marker life-cycle, too.
   *
   * @example
   *
   * ```javascript
   * canvas.addMarker('foo', 'some-marker');
   *
   * const fooGfx = canvas.getGraphics('foo');
   *
   * fooGfx; // <g class="... some-marker"> ... </g>
   * ```
   *
   * @param {ShapeLike|ConnectionLike|string} element The element or its ID.
   * @param {string} marker The marker.
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
   * @param {ShapeLike|ConnectionLike|string} element The element or its ID.
   * @param {string} marker The marker.
   */
  Canvas.prototype.removeMarker = function(element, marker) {
    this._updateMarker(element, marker, false);
  };

  /**
   * Check whether an element has a given marker.
   *
   * @param {ShapeLike|ConnectionLike|string} element The element or its ID.
   * @param {string} marker The marker.
   */
  Canvas.prototype.hasMarker = function(element, marker) {
    if (!element.id) {
      element = this._elementRegistry.get(element);
    }

    const gfx = this.getGraphics(element);

    return classes(gfx).has(marker);
  };

  /**
   * Toggles a marker on an element.
   *
   * Fires the element.marker.update event, making it possible to
   * integrate extension into the marker life-cycle, too.
   *
   * @param {ShapeLike|ConnectionLike|string} element The element or its ID.
   * @param {string} marker The marker.
   */
  Canvas.prototype.toggleMarker = function(element, marker) {
    if (this.hasMarker(element, marker)) {
      this.removeMarker(element, marker);
    } else {
      this.addMarker(element, marker);
    }
  };

  /**
   * Returns the current root element.
   *
   * Supports two different modes for handling root elements:
   *
   * 1. if no root element has been added before, an implicit root will be added
   * and returned. This is used in applications that don't require explicit
   * root elements.
   *
   * 2. when root elements have been added before calling `getRootElement`,
   * root elements can be null. This is used for applications that want to manage
   * root elements themselves.
   *
   * @return {RootLike} The current root element.
   */
  Canvas.prototype.getRootElement = function() {
    const rootElement = this._rootElement;

    // can return null if root elements are present but none was set yet
    if (rootElement || this._planes.length) {
      return rootElement;
    }

    return this.setRootElement(this.addRootElement(null));
  };

  /**
   * Adds a given root element and returns it.
   *
   * @param {RootLike} [rootElement] The root element to be added.
   *
   * @return {RootLike} The added root element or an implicit root element.
   */
  Canvas.prototype.addRootElement = function(rootElement) {
    const idx = this._rootsIdx++;

    if (!rootElement) {
      rootElement = {
        id: '__implicitroot_' + idx,
        children: [],
        isImplicit: true
      };
    }

    const layerName = rootElement.layer = 'root-' + idx;

    this._ensureValid('root', rootElement);

    const layer = this.getLayer(layerName, PLANE_LAYER_INDEX);

    this.hideLayer(layerName);

    this._addRoot(rootElement, layer);

    this._planes.push({
      rootElement: rootElement,
      layer: layer
    });

    return rootElement;
  };

  /**
   * Removes a given root element and returns it.
   *
   * @param {RootLike|string} rootElement element or element ID
   *
   * @return {RootLike|undefined} removed element
   */
  Canvas.prototype.removeRootElement = function(rootElement) {

    if (typeof rootElement === 'string') {
      rootElement = this._elementRegistry.get(rootElement);
    }

    const plane = this._findPlaneForRoot(rootElement);

    if (!plane) {
      return;
    }

    // hook up life-cycle events
    this._removeRoot(rootElement);

    // clean up layer
    this._removeLayer(rootElement.layer);

    // clean up plane
    this._planes = this._planes.filter(function(plane) {
      return plane.rootElement !== rootElement;
    });

    // clean up active root
    if (this._rootElement === rootElement) {
      this._rootElement = null;
    }

    return rootElement;
  };


  /**
   * Sets a given element as the new root element for the canvas
   * and returns the new root element.
   *
   * @param {RootLike} rootElement The root element to be set.
   *
   * @return {RootLike} The set root element.
   */
  Canvas.prototype.setRootElement = function(rootElement) {

    if (rootElement === this._rootElement) {
      return;
    }

    let plane;

    if (!rootElement) {
      throw new Error('rootElement required');
    }

    plane = this._findPlaneForRoot(rootElement);

    // give set add semantics for backwards compatibility
    if (!plane) {
      rootElement = this.addRootElement(rootElement);
    }

    this._setRoot(rootElement);

    return rootElement;
  };


  Canvas.prototype._removeRoot = function(element) {
    const elementRegistry = this._elementRegistry,
          eventBus = this._eventBus;

    // simulate element remove event sequence
    eventBus.fire('root.remove', { element: element });
    eventBus.fire('root.removed', { element: element });

    elementRegistry.remove(element);
  };


  Canvas.prototype._addRoot = function(element, gfx) {
    const elementRegistry = this._elementRegistry,
          eventBus = this._eventBus;

    // resemble element add event sequence
    eventBus.fire('root.add', { element: element });

    elementRegistry.add(element, gfx);

    eventBus.fire('root.added', { element: element, gfx: gfx });
  };


  Canvas.prototype._setRoot = function(rootElement, layer) {

    const currentRoot = this._rootElement;

    if (currentRoot) {

      // un-associate previous root element <svg>
      this._elementRegistry.updateGraphics(currentRoot, null, true);

      // hide previous layer
      this.hideLayer(currentRoot.layer);
    }

    if (rootElement) {

      if (!layer) {
        layer = this._findPlaneForRoot(rootElement).layer;
      }

      // associate element with <svg>
      this._elementRegistry.updateGraphics(rootElement, this._svg, true);

      // show root layer
      this.showLayer(rootElement.layer);
    }

    this._rootElement = rootElement;

    this._eventBus.fire('root.set', { element: rootElement });
  };

  Canvas.prototype._ensureValid = function(type, element) {
    if (!element.id) {
      throw new Error('element must have an id');
    }

    if (this._elementRegistry.get(element.id)) {
      throw new Error('element <' + element.id + '> already exists');
    }

    const requiredAttrs = REQUIRED_MODEL_ATTRS[type];

    const valid = every(requiredAttrs, function(attr) {
      return typeof element[attr] !== 'undefined';
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
   * @param {string} type
   * @param {ConnectionLike|ShapeLike} element
   * @param {ShapeLike} [parent]
   * @param {number} [parentIndex]
   *
   * @return {ConnectionLike|ShapeLike} The added element.
   */
  Canvas.prototype._addElement = function(type, element, parent, parentIndex) {

    parent = parent || this.getRootElement();

    const eventBus = this._eventBus,
          graphicsFactory = this._graphicsFactory;

    this._ensureValid(type, element);

    eventBus.fire(type + '.add', { element: element, parent: parent });

    this._setParent(element, parent, parentIndex);

    // create graphics
    const gfx = graphicsFactory.create(type, element, parentIndex);

    this._elementRegistry.add(element, gfx);

    // update its visual
    graphicsFactory.update(type, element, gfx);

    eventBus.fire(type + '.added', { element: element, gfx: gfx });

    return element;
  };

  /**
   * Adds a shape to the canvas.
   *
   * @param {ShapeLike} shape The shape to be added
   * @param {ParentLike} [parent] The shape's parent.
   * @param {number} [parentIndex] The index at which to add the shape to the parent's children.
   *
   * @return {ShapeLike} The added shape.
   */
  Canvas.prototype.addShape = function(shape, parent, parentIndex) {
    return this._addElement('shape', shape, parent, parentIndex);
  };

  /**
   * Adds a connection to the canvas.
   *
   * @param {ConnectionLike} connection The connection to be added.
   * @param {ParentLike} [parent] The connection's parent.
   * @param {number} [parentIndex] The index at which to add the connection to the parent's children.
   *
   * @return {ConnectionLike} The added connection.
   */
  Canvas.prototype.addConnection = function(connection, parent, parentIndex) {
    return this._addElement('connection', connection, parent, parentIndex);
  };


  /**
   * Internal remove element
   */
  Canvas.prototype._removeElement = function(element, type) {

    const elementRegistry = this._elementRegistry,
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
    remove(element.parent && element.parent.children, element);
    element.parent = null;

    eventBus.fire(type + '.removed', { element: element });

    elementRegistry.remove(element);

    return element;
  };


  /**
   * Removes a shape from the canvas.
   *
   * @fires ShapeRemoveEvent
   * @fires ShapeRemovedEvent
   *
   * @param {ShapeLike|string} shape The shape or its ID.
   *
   * @return {ShapeLike} The removed shape.
   */
  Canvas.prototype.removeShape = function(shape) {

    /**
     * An event indicating that a shape is about to be removed from the canvas.
     *
     * @memberOf Canvas
     *
     * @event ShapeRemoveEvent
     * @type {Object}
     * @property {ShapeLike} element The shape.
     * @property {SVGElement} gfx The graphical element.
     */

    /**
     * An event indicating that a shape has been removed from the canvas.
     *
     * @memberOf Canvas
     *
     * @event ShapeRemovedEvent
     * @type {Object}
     * @property {ShapeLike} element The shape.
     * @property {SVGElement} gfx The graphical element.
     */
    return this._removeElement(shape, 'shape');
  };


  /**
   * Removes a connection from the canvas.
   *
   * @fires ConnectionRemoveEvent
   * @fires ConnectionRemovedEvent
   *
   * @param {ConnectionLike|string} connection The connection or its ID.
   *
   * @return {ConnectionLike} The removed connection.
   */
  Canvas.prototype.removeConnection = function(connection) {

    /**
     * An event indicating that a connection is about to be removed from the canvas.
     *
     * @memberOf Canvas
     *
     * @event ConnectionRemoveEvent
     * @type {Object}
     * @property {ConnectionLike} element The connection.
     * @property {SVGElement} gfx The graphical element.
     */

    /**
     * An event indicating that a connection has been removed from the canvas.
     *
     * @memberOf Canvas
     *
     * @event ConnectionRemovedEvent
     * @type {Object}
     * @property {ConnectionLike} element The connection.
     * @property {SVGElement} gfx The graphical element.
     */
    return this._removeElement(connection, 'connection');
  };


  /**
   * Returns the graphical element of an element.
   *
   * @param {ShapeLike|ConnectionLike|string} element The element or its ID.
   * @param {boolean} [secondary=false] Whether to return the secondary graphical element.
   *
   * @return {SVGElement} The graphical element.
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
   * ```javascript
   * canvas.viewbox({ x: 100, y: 100, width: 500, height: 500 })
   *
   * // sets the visible area of the diagram to (100|100) -> (600|100)
   * // and and scales it according to the diagram width
   *
   * const viewbox = canvas.viewbox(); // pass `false` to force recomputing the box.
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
   * const zoomedAndScrolledViewbox = canvas.viewbox();
   *
   * canvas.viewbox({
   *   x: 0,
   *   y: 0,
   *   width: zoomedAndScrolledViewbox.outer.width,
   *   height: zoomedAndScrolledViewbox.outer.height
   * });
   * ```
   *
   * @param {Rect} [box] The viewbox to be set.
   *
   * @return {CanvasViewbox} The set viewbox.
   */
  Canvas.prototype.viewbox = function(box) {

    if (box === undefined && this._cachedViewbox) {
      return this._cachedViewbox;
    }

    const viewport = this._viewport,
          outerBox = this.getSize();
    let innerBox,
        matrix,
        activeLayer,
        transform$1,
        scale,
        x, y;

    if (!box) {

      // compute the inner box based on the
      // diagrams active layer. This allows us to exclude
      // external components, such as overlays

      activeLayer = this._rootElement ? this.getActiveLayer() : null;
      innerBox = activeLayer && activeLayer.getBBox() || {};

      transform$1 = transform(viewport);
      matrix = transform$1 ? transform$1.matrix : createMatrix();
      scale = round(matrix.a, 1000);

      x = round(-matrix.e || 0, 1000);
      y = round(-matrix.f || 0, 1000);

      box = this._cachedViewbox = {
        x: x ? x / scale : 0,
        y: y ? y / scale : 0,
        width: outerBox.width / scale,
        height: outerBox.height / scale,
        scale: scale,
        inner: {
          width: innerBox.width || 0,
          height: innerBox.height || 0,
          x: innerBox.x || 0,
          y: innerBox.y || 0
        },
        outer: outerBox
      };

      return box;
    } else {

      this._changeViewbox(function() {
        scale = Math.min(outerBox.width / box.width, outerBox.height / box.height);

        const matrix = this._svg.createSVGMatrix()
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
   * @param {Point} [delta] The scroll to be set.
   *
   * @return {Point}
   */
  Canvas.prototype.scroll = function(delta) {

    const node = this._viewport;
    let matrix = node.getCTM();

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
   * Scrolls the viewbox to contain the given element.
   * Optionally specify a padding to be applied to the edges.
   *
   * @param {ShapeLike|ConnectionLike|string} element The element to scroll to or its ID.
   * @param {RectTRBL|number} [padding=100] The padding to be applied. Can also specify top, bottom, left and right.
   */
  Canvas.prototype.scrollToElement = function(element, padding) {
    let defaultPadding = 100;

    if (typeof element === 'string') {
      element = this._elementRegistry.get(element);
    }

    // set to correct rootElement
    const rootElement = this.findRoot(element);

    if (rootElement !== this.getRootElement()) {
      this.setRootElement(rootElement);
    }

    // element is rootElement, do not change viewport
    if (rootElement === element) {
      return;
    }

    if (!padding) {
      padding = {};
    }
    if (typeof padding === 'number') {
      defaultPadding = padding;
    }

    padding = {
      top: padding.top || defaultPadding,
      right: padding.right || defaultPadding,
      bottom: padding.bottom || defaultPadding,
      left: padding.left || defaultPadding
    };

    const elementBounds = getBBox(element),
          elementTrbl = asTRBL(elementBounds),
          viewboxBounds = this.viewbox(),
          zoom = this.zoom();
    let dx, dy;

    // shrink viewboxBounds with padding
    viewboxBounds.y += padding.top / zoom;
    viewboxBounds.x += padding.left / zoom;
    viewboxBounds.width -= (padding.right + padding.left) / zoom;
    viewboxBounds.height -= (padding.bottom + padding.top) / zoom;

    const viewboxTrbl = asTRBL(viewboxBounds);

    const canFit = elementBounds.width < viewboxBounds.width && elementBounds.height < viewboxBounds.height;

    if (!canFit) {

      // top-left when element can't fit
      dx = elementBounds.x - viewboxBounds.x;
      dy = elementBounds.y - viewboxBounds.y;

    } else {

      const dRight = Math.max(0, elementTrbl.right - viewboxTrbl.right),
            dLeft = Math.min(0, elementTrbl.left - viewboxTrbl.left),
            dBottom = Math.max(0, elementTrbl.bottom - viewboxTrbl.bottom),
            dTop = Math.min(0, elementTrbl.top - viewboxTrbl.top);

      dx = dRight || dLeft;
      dy = dBottom || dTop;

    }

    this.scroll({ dx: -dx * zoom, dy: -dy * zoom });
  };

  /**
   * Gets or sets the current zoom of the canvas, optionally zooming to the
   * specified position.
   *
   * The getter may return a cached zoom level. Call it with `false` as the first
   * argument to force recomputation of the current level.
   *
   * @param {number|'fit-viewport'} [newScale] The new zoom level, either a number,
   * i.e. 0.9, or `fit-viewport` to adjust the size to fit the current viewport.
   * @param {Point} [center] The reference point { x: ..., y: ...} to zoom to.
   *
   * @return {number} The set zoom level.
   */
  Canvas.prototype.zoom = function(newScale, center) {

    if (!newScale) {
      return this.viewbox(newScale).scale;
    }

    if (newScale === 'fit-viewport') {
      return this._fitViewport(center);
    }

    let outer,
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

    return round(matrix.a, 1000);
  };

  function setCTM(node, m) {
    const mstr = 'matrix(' + m.a + ',' + m.b + ',' + m.c + ',' + m.d + ',' + m.e + ',' + m.f + ')';
    node.setAttribute('transform', mstr);
  }

  Canvas.prototype._fitViewport = function(center) {

    const vbox = this.viewbox(),
          outer = vbox.outer,
          inner = vbox.inner;
    let newScale,
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

    const svg = this._svg,
          viewport = this._viewport;

    const matrix = svg.createSVGMatrix();
    const point = svg.createSVGPoint();

    let centerPoint,
        originalPoint,
        currentMatrix,
        scaleMatrix,
        newMatrix;

    currentMatrix = viewport.getCTM();

    const currentScale = currentMatrix.a;

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
   * Returns the size of the canvas.
   *
   * @return {Dimensions} The size of the canvas.
   */
  Canvas.prototype.getSize = function() {
    return {
      width: this._container.clientWidth,
      height: this._container.clientHeight
    };
  };


  /**
   * Returns the absolute bounding box of an element.
   *
   * The absolute bounding box may be used to display overlays in the callers
   * (browser) coordinate system rather than the zoomed in/out canvas coordinates.
   *
   * @param {ShapeLike|ConnectionLike} element The element.
   *
   * @return {Rect} The element's absolute bounding box.
   */
  Canvas.prototype.getAbsoluteBBox = function(element) {
    const vbox = this.viewbox();
    let bbox;

    // connection
    // use svg bbox
    if (element.waypoints) {
      const gfx = this.getGraphics(element);

      bbox = gfx.getBBox();
    }

    // shapes
    // use data
    else {
      bbox = element;
    }

    const x = bbox.x * vbox.scale - vbox.x * vbox.scale;
    const y = bbox.y * vbox.scale - vbox.y * vbox.scale;

    const width = bbox.width * vbox.scale;
    const height = bbox.height * vbox.scale;

    return {
      x: x,
      y: y,
      width: width,
      height: height
    };
  };

  /**
   * Fires an event so other modules can react to the canvas resizing.
   */
  Canvas.prototype.resized = function() {

    // force recomputation of view box
    delete this._cachedViewbox;

    this._eventBus.fire('canvas.resized');
  };

  var ELEMENT_ID = 'data-element-id';

  /**
   * @typedef {import('./Types').ElementLike} ElementLike
   *
   * @typedef {import('./EventBus').default} EventBus
   *
   * @typedef { (element: ElementLike, gfx: SVGElement) => boolean|any } ElementRegistryFilterCallback
   * @typedef { (element: ElementLike, gfx: SVGElement) => any } ElementRegistryForEachCallback
   */

  /**
   * A registry that keeps track of all shapes in the diagram.
   *
   * @class
   * @constructor
   *
   * @param {EventBus} eventBus
   */
  function ElementRegistry(eventBus) {

    /**
     * @type { {
     *   [id: string]: {
     *     element: ElementLike;
     *     gfx?: SVGElement;
     *     secondaryGfx?: SVGElement;
     *   }
     * } }
     */
    this._elements = {};

    this._eventBus = eventBus;
  }

  ElementRegistry.$inject = [ 'eventBus' ];

  /**
   * Add an element and its graphical representation(s) to the registry.
   *
   * @param {ElementLike} element The element to be added.
   * @param {SVGElement} gfx The primary graphical representation.
   * @param {SVGElement} [secondaryGfx] The secondary graphical representation.
   */
  ElementRegistry.prototype.add = function(element, gfx, secondaryGfx) {

    var id = element.id;

    this._validateId(id);

    // associate dom node with element
    attr(gfx, ELEMENT_ID, id);

    if (secondaryGfx) {
      attr(secondaryGfx, ELEMENT_ID, id);
    }

    this._elements[id] = { element: element, gfx: gfx, secondaryGfx: secondaryGfx };
  };

  /**
   * Remove an element from the registry.
   *
   * @param {ElementLike|string} element
   */
  ElementRegistry.prototype.remove = function(element) {
    var elements = this._elements,
        id = element.id || element,
        container = id && elements[id];

    if (container) {

      // unset element id on gfx
      attr(container.gfx, ELEMENT_ID, '');

      if (container.secondaryGfx) {
        attr(container.secondaryGfx, ELEMENT_ID, '');
      }

      delete elements[id];
    }
  };

  /**
   * Update an elements ID.
   *
   * @param {ElementLike|string} element The element or its ID.
   * @param {string} newId The new ID.
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
   * Update the graphical representation of an element.
   *
   * @param {ElementLike|string} filter The element or its ID.
   * @param {SVGElement} gfx The new graphical representation.
   * @param {boolean} [secondary=false] Whether to update the secondary graphical representation.
   */
  ElementRegistry.prototype.updateGraphics = function(filter, gfx, secondary) {
    var id = filter.id || filter;

    var container = this._elements[id];

    if (secondary) {
      container.secondaryGfx = gfx;
    } else {
      container.gfx = gfx;
    }

    if (gfx) {
      attr(gfx, ELEMENT_ID, id);
    }

    return gfx;
  };

  /**
   * Get the element with the given ID or graphical representation.
   *
   * @example
   *
   * ```javascript
   * elementRegistry.get('SomeElementId_1');
   *
   * elementRegistry.get(gfx);
   * ```
   *
   * @param {string|SVGElement} filter The elements ID or graphical representation.
   *
   * @return {ElementLike|undefined} The element.
   */
  ElementRegistry.prototype.get = function(filter) {
    var id;

    if (typeof filter === 'string') {
      id = filter;
    } else {
      id = filter && attr(filter, ELEMENT_ID);
    }

    var container = this._elements[id];
    return container && container.element;
  };

  /**
   * Return all elements that match a given filter function.
   *
   * @param {ElementRegistryFilterCallback} fn The filter function.
   *
   * @return {ElementLike[]} The matching elements.
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
   * Return the first element that matches the given filter function.
   *
   * @param {ElementRegistryFilterCallback} fn The filter function.
   *
   * @return {ElementLike|undefined} The matching element.
   */
  ElementRegistry.prototype.find = function(fn) {
    var map = this._elements,
        keys = Object.keys(map);

    for (var i = 0; i < keys.length; i++) {
      var id = keys[i],
          container = map[id],
          element = container.element,
          gfx = container.gfx;

      if (fn(element, gfx)) {
        return element;
      }
    }
  };

  /**
   * Get all elements.
   *
   * @return {ElementLike[]} All elements.
   */
  ElementRegistry.prototype.getAll = function() {
    return this.filter(function(e) { return e; });
  };

  /**
   * Execute a given function for each element.
   *
   * @param {ElementRegistryForEachCallback} fn The function to execute.
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
   * Return the graphical representation of an element.
   *
   * @example
   *
   * ```javascript
   * elementRegistry.getGraphics('SomeElementId_1');
   *
   * elementRegistry.getGraphics(rootElement); // <g ...>
   *
   * elementRegistry.getGraphics(rootElement, true); // <svg ...>
   * ```
   *
   * @param {ElementLike|string} filter The element or its ID.
   * @param {boolean} [secondary=false] Whether to return the secondary graphical representation.
   *
   * @return {SVGElement} The graphical representation.
   */
  ElementRegistry.prototype.getGraphics = function(filter, secondary) {
    var id = filter.id || filter;

    var container = this._elements[id];
    return container && (secondary ? container.secondaryGfx : container.gfx);
  };

  /**
   * Validate an ID and throw an error if invalid.
   *
   * @param {string} id
   *
   * @throws {Error} Error indicating that the ID is invalid or already assigned.
   */
  ElementRegistry.prototype._validateId = function(id) {
    if (!id) {
      throw new Error('element must have an id');
    }

    if (this._elements[id]) {
      throw new Error('element with id ' + id + ' already added');
    }
  };

  var objectRefs = {exports: {}};

  var collection = {};

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
  function extend(collection, refs, property, target) {

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

  collection.extend = extend;

  collection.isExtended = isExtended;

  var Collection = collection;

  function hasOwnProperty(e, property) {
    return Object.prototype.hasOwnProperty.call(e, property.name || property);
  }

  function defineCollectionProperty(ref, property, target) {

    var collection = Collection.extend(target[property.name] || [], ref, property, target);

    Object.defineProperty(target, property.name, {
      enumerable: property.enumerable,
      value: collection
    });

    if (collection.length) {

      collection.forEach(function(o) {
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
  function Refs$1(a, b) {

    if (!(this instanceof Refs$1)) {
      return new Refs$1(a, b);
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
  Refs$1.prototype.bind = function(target, property) {
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

  Refs$1.prototype.ensureRefsCollection = function(target, property) {

    var collection = target[property.name];

    if (!Collection.isExtended(collection)) {
      defineCollectionProperty(this, property, target);
    }

    return collection;
  };

  Refs$1.prototype.ensureBound = function(target, property) {
    if (!hasOwnProperty(target, property)) {
      this.bind(target, property);
    }
  };

  Refs$1.prototype.unset = function(target, property, value) {

    if (target) {
      this.ensureBound(target, property);

      if (property.collection) {
        this.ensureRefsCollection(target, property).remove(value);
      } else {
        target[property.name] = undefined;
      }
    }
  };

  Refs$1.prototype.set = function(target, property, value) {

    if (target) {
      this.ensureBound(target, property);

      if (property.collection) {
        this.ensureRefsCollection(target, property).add(value);
      } else {
        target[property.name] = value;
      }
    }
  };

  var refs = Refs$1;

  objectRefs.exports = refs;

  objectRefs.exports.Collection = collection;

  var objectRefsExports = objectRefs.exports;
  var Refs = /*@__PURE__*/getDefaultExportFromCjs(objectRefsExports);

  var parentRefs = new Refs({ name: 'children', enumerable: true, collection: true }, { name: 'parent' }),
      labelRefs = new Refs({ name: 'labels', enumerable: true, collection: true }, { name: 'labelTarget' }),
      attacherRefs = new Refs({ name: 'attachers', collection: true }, { name: 'host' }),
      outgoingRefs = new Refs({ name: 'outgoing', collection: true }, { name: 'source' }),
      incomingRefs = new Refs({ name: 'incoming', collection: true }, { name: 'target' });

  /**
   * @typedef {import('./Types').Element} Element
   * @typedef {import('./Types').Shape} Shape
   * @typedef {import('./Types').Root} Root
   * @typedef {import('./Types').Label} Label
   * @typedef {import('./Types').Connection} Connection
   */

  /**
   * The basic graphical representation
   *
   * @class
   * @constructor
   */
  function ElementImpl() {

    /**
     * The object that backs up the shape
     *
     * @name Element#businessObject
     * @type Object
     */
    Object.defineProperty(this, 'businessObject', {
      writable: true
    });


    /**
     * Single label support, will mapped to multi label array
     *
     * @name Element#label
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
     * @name Element#parent
     * @type Shape
     */
    parentRefs.bind(this, 'parent');

    /**
     * The list of labels
     *
     * @name Element#labels
     * @type Label
     */
    labelRefs.bind(this, 'labels');

    /**
     * The list of outgoing connections
     *
     * @name Element#outgoing
     * @type Array<Connection>
     */
    outgoingRefs.bind(this, 'outgoing');

    /**
     * The list of incoming connections
     *
     * @name Element#incoming
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
   * @extends ElementImpl
   */
  function ShapeImpl() {
    ElementImpl.call(this);

    /**
     * Indicates frame shapes
     *
     * @name ShapeImpl#isFrame
     * @type boolean
     */

    /**
     * The list of children
     *
     * @name ShapeImpl#children
     * @type Element[]
     */
    parentRefs.bind(this, 'children');

    /**
     * @name ShapeImpl#host
     * @type Shape
     */
    attacherRefs.bind(this, 'host');

    /**
     * @name ShapeImpl#attachers
     * @type Shape
     */
    attacherRefs.bind(this, 'attachers');
  }

  e(ShapeImpl, ElementImpl);


  /**
   * A root graphical object
   *
   * @class
   * @constructor
   *
   * @extends ElementImpl
   */
  function RootImpl() {
    ElementImpl.call(this);

    /**
     * The list of children
     *
     * @name RootImpl#children
     * @type Element[]
     */
    parentRefs.bind(this, 'children');
  }

  e(RootImpl, ShapeImpl);


  /**
   * A label for an element
   *
   * @class
   * @constructor
   *
   * @extends ShapeImpl
   */
  function LabelImpl() {
    ShapeImpl.call(this);

    /**
     * The labeled element
     *
     * @name LabelImpl#labelTarget
     * @type Element
     */
    labelRefs.bind(this, 'labelTarget');
  }

  e(LabelImpl, ShapeImpl);


  /**
   * A connection between two elements
   *
   * @class
   * @constructor
   *
   * @extends ElementImpl
   */
  function ConnectionImpl() {
    ElementImpl.call(this);

    /**
     * The element this connection originates from
     *
     * @name ConnectionImpl#source
     * @type Element
     */
    outgoingRefs.bind(this, 'source');

    /**
     * The element this connection points to
     *
     * @name ConnectionImpl#target
     * @type Element
     */
    incomingRefs.bind(this, 'target');
  }

  e(ConnectionImpl, ElementImpl);


  var types = {
    connection: ConnectionImpl,
    shape: ShapeImpl,
    label: LabelImpl,
    root: RootImpl
  };

  /**
   * Creates a root element.
   *
   * @overlord
   *
   * @example
   *
   * ```javascript
   * import * as Model from 'diagram-js/lib/model';
   *
   * const root = Model.create('root', {
   *   x: 100,
   *   y: 100,
   *   width: 100,
   *   height: 100
   * });
   * ```
   *
   * @param {'root'} type
   * @param {any} [attrs]
   *
   * @return {Root}
   */

  /**
   * Creates a connection.
   *
   * @overlord
   *
   * @example
   *
   * ```javascript
   * import * as Model from 'diagram-js/lib/model';
   *
   * const connection = Model.create('connection', {
   *   waypoints: [
   *     { x: 100, y: 100 },
   *     { x: 200, y: 100 }
   *   ]
   * });
   * ```
   *
   * @param {'connection'} type
   * @param {any} [attrs]
   *
   * @return {Connection}
   */

  /**
   * Creates a shape.
   *
   * @overlord
   *
   * @example
   *
   * ```javascript
   * import * as Model from 'diagram-js/lib/model';
   *
   * const shape = Model.create('shape', {
   *   x: 100,
   *   y: 100,
   *   width: 100,
   *   height: 100
   * });
   * ```
   *
   * @param {'shape'} type
   * @param {any} [attrs]
   *
   * @return {Shape}
   */

  /**
   * Creates a label.
   *
   * @example
   *
   * ```javascript
   * import * as Model from 'diagram-js/lib/model';
   *
   * const label = Model.create('label', {
   *   x: 100,
   *   y: 100,
   *   width: 100,
   *   height: 100,
   *   labelTarget: shape
   * });
   * ```
   *
   * @param {'label'} type
   * @param {Object} [attrs]
   *
   * @return {Label}
   */
  function create(type, attrs) {
    var Type = types[type];
    if (!Type) {
      throw new Error('unknown type: <' + type + '>');
    }
    return assign(new Type(), attrs);
  }

  /**
   * @typedef {import('../model/Types').Element} Element
   * @typedef {import('../model/Types').Connection} Connection
   * @typedef {import('../model/Types').Label} Label
   * @typedef {import('../model/Types').Root} Root
   * @typedef {import('../model/Types').Shape} Shape
   */

  /**
   * A factory for model elements.
   *
   * @template {Connection} [T=Connection]
   * @template {Label} [U=Label]
   * @template {Root} [V=Root]
   * @template {Shape} [W=Shape]
   */
  function ElementFactory() {
    this._uid = 12;
  }

  /**
   * Create a root element.
   *
   * @param {Partial<Root>} [attrs]
   *
   * @return {V} The created root element.
   */
  ElementFactory.prototype.createRoot = function(attrs) {
    return this.create('root', attrs);
  };

  /**
   * Create a label.
   *
   * @param {Partial<Label>} [attrs]
   *
   * @return {U} The created label.
   */
  ElementFactory.prototype.createLabel = function(attrs) {
    return this.create('label', attrs);
  };

  /**
   * Create a shape.
   *
   * @param {Partial<Shape>} [attrs]
   *
   * @return {W} The created shape.
   */
  ElementFactory.prototype.createShape = function(attrs) {
    return this.create('shape', attrs);
  };

  /**
   * Create a connection.
   *
   * @param {Partial<Connection>} [attrs]
   *
   * @return {T} The created connection.
   */
  ElementFactory.prototype.createConnection = function(attrs) {
    return this.create('connection', attrs);
  };

  /**
   * Create a root element.
   *
   * @overlord
   * @param {'root'} type
   * @param {Partial<Root>} [attrs]
   * @return {V}
   */
  /**
   * Create a shape.
   *
   * @overlord
   * @param {'shape'} type
   * @param {Partial<Shape>} [attrs]
   * @return {W}
   */
  /**
   * Create a connection.
   *
   * @overlord
   * @param {'connection'} type
   * @param {Partial<Connection>} [attrs]
   * @return {T}
   */
  /**
   * Create a label.
   *
   * @param {'label'} type
   * @param {Partial<Label>} [attrs]
   * @return {U}
   */
  ElementFactory.prototype.create = function(type, attrs) {

    attrs = assign({}, attrs || {});

    if (!attrs.id) {
      attrs.id = type + '_' + (this._uid++);
    }

    return create(type, attrs);
  };

  var FN_REF = '__fn';

  var DEFAULT_PRIORITY = 1000;

  var slice = Array.prototype.slice;

  /**
   * @typedef { {
   *   stopPropagation(): void;
   *   preventDefault(): void;
   *   cancelBubble: boolean;
   *   defaultPrevented: boolean;
   *   returnValue: any;
   * } } Event
   */

  /**
   * @template E
   *
   * @typedef { (event: E & Event, ...any) => any } EventBusEventCallback
   */

  /**
   * @typedef { {
   *  priority: number;
   *  next: EventBusListener | null;
   *  callback: EventBusEventCallback<any>;
   * } } EventBusListener
   */

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

    /**
     * @type { Record<string, EventBusListener> }
     */
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
   * @template T
   *
   * @param {string|string[]} events to subscribe to
   * @param {number} [priority=1000] listen priority
   * @param {EventBusEventCallback<T>} callback
   * @param {any} [that] callback context
   */
  EventBus.prototype.on = function(events, priority, callback, that) {

    events = isArray$1(events) ? events : [ events ];

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
      actualCallback = bind(callback, that);

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
   * Register an event listener that is called only once.
   *
   * @template T
   *
   * @param {string|string[]} events to subscribe to
   * @param {number} [priority=1000] the listen priority
   * @param {EventBusEventCallback<T>} callback
   * @param {any} [that] callback context
   */
  EventBus.prototype.once = function(events, priority, callback, that) {
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
      wrappedCallback.__isTomb = true;

      var result = callback.apply(that, arguments);

      self.off(events, wrappedCallback);

      return result;
    }

    // make sure we remember and are able to remove
    // bound callbacks via {@link #off} using the original
    // callback
    wrappedCallback[FN_REF] = callback;

    this.on(events, priority, wrappedCallback);
  };


  /**
   * Removes event listeners by event and callback.
   *
   * If no callback is given, all listeners for a given event name are being removed.
   *
   * @param {string|string[]} events
   * @param {EventBusEventCallback} [callback]
   */
  EventBus.prototype.off = function(events, callback) {

    events = isArray$1(events) ? events : [ events ];

    var self = this;

    events.forEach(function(event) {
      self._removeListener(event, callback);
    });

  };


  /**
   * Create an event recognized be the event bus.
   *
   * @param {Object} data Event data.
   *
   * @return {Event} An event that will be recognized by the event bus.
   */
  EventBus.prototype.createEvent = function(data) {
    var event = new InternalEvent();

    event.init(data);

    return event;
  };


  /**
   * Fires an event.
   *
   * @example
   *
   * ```javascript
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
   * ```
   *
   * @param {string} [type] event type
   * @param {Object} [data] event or event data
   * @param {...any} [args] additional arguments the callback will be called with.
   *
   * @return {any} The return value. Will be set to `false` if the default was prevented.
   */
  EventBus.prototype.fire = function(type, data) {
    var event,
        firstListener,
        returnValue,
        args;

    args = slice.call(arguments);

    if (typeof type === 'object') {
      data = type;
      type = data.type;
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

  /**
   * Handle an error by firing an event.
   *
   * @param {Error} error The error to be handled.
   *
   * @return {boolean} Whether the error was handled.
   */
  EventBus.prototype.handleError = function(error) {
    return this.fire('error', { error: error }) === false;
  };


  EventBus.prototype._destroy = function() {
    this._listeners = {};
  };

  /**
   * @param {Event} event
   * @param {any[]} args
   * @param {EventBusListener} listener
   *
   * @return {any}
   */
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

  /**
   * @param {Event} event
   * @param {any[]} args
   * @param {EventBusListener} listener
   *
   * @return {any}
   */
  EventBus.prototype._invokeListener = function(event, args, listener) {

    var returnValue;

    if (listener.callback.__isTomb) {
      return returnValue;
    }

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
    } catch (error) {
      if (!this.handleError(error)) {
        console.error('unhandled error in event listener', error);

        throw error;
      }
    }

    return returnValue;
  };

  /**
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
   * @param {string} event
   * @param {EventBusListener} newListener
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


  /**
   * @param {string} name
   *
   * @return {EventBusListener}
   */
  EventBus.prototype._getListeners = function(name) {
    return this._listeners[name];
  };

  /**
   * @param {string} name
   * @param {EventBusListener} listener
   */
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
   * @param {any[]} args
   *
   * @return {any}
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
   * Returns the visual part of a diagram element.
   *
   * @param {SVGElement} gfx
   *
   * @return {SVGElement}
   */
  function getVisual(gfx) {
    return gfx.childNodes[0];
  }

  /**
   * Returns the children for a given diagram element.
   *
   * @param {SVGElement} gfx
   * @return {SVGElement}
   */
  function getChildren(gfx) {
    return gfx.parentNode.childNodes[1];
  }

  /**
   * @param {SVGElement} gfx
   * @param {number} x
   * @param {number} y
   */
  function translate(gfx, x, y) {
    var translate = createTransform();
    translate.setTranslate(x, y);

    transform(gfx, translate);
  }

  /**
   * @typedef {import('./Types').ConnectionLike} ConnectionLike
   * @typedef {import('./Types').ElementLike} ElementLike
   * @typedef {import('./Types').ShapeLike} ShapeLike
   *
   * @typedef {import('./ElementRegistry').default} ElementRegistry
   * @typedef {import('./EventBus').default} EventBus
   */

  /**
   * A factory that creates graphical elements.
   *
   * @param {EventBus} eventBus
   * @param {ElementRegistry} elementRegistry
   */
  function GraphicsFactory(eventBus, elementRegistry) {
    this._eventBus = eventBus;
    this._elementRegistry = elementRegistry;
  }

  GraphicsFactory.$inject = [ 'eventBus' , 'elementRegistry' ];

  /**
   * @param { { parent?: any } } element
   * @return {SVGElement}
   */
  GraphicsFactory.prototype._getChildrenContainer = function(element) {

    var gfx = this._elementRegistry.getGraphics(element);

    var childrenGfx;

    // root element
    if (!element.parent) {
      childrenGfx = gfx;
    } else {
      childrenGfx = getChildren(gfx);
      if (!childrenGfx) {
        childrenGfx = create$1('g');
        classes(childrenGfx).add('djs-children');

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

    clear$1(visual);

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
   *   <g class="djs-element djs-(shape|connection|frame)">
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
   * @param {string} type the type of the element, i.e. shape | connection
   * @param {SVGElement} childrenGfx
   * @param {number} [parentIndex] position to create container in parent
   * @param {boolean} [isFrame] is frame element
   *
   * @return {SVGElement}
   */
  GraphicsFactory.prototype._createContainer = function(
      type, childrenGfx, parentIndex, isFrame
  ) {
    var outerGfx = create$1('g');
    classes(outerGfx).add('djs-group');

    // insert node at position
    if (typeof parentIndex !== 'undefined') {
      prependTo(outerGfx, childrenGfx, childrenGfx.childNodes[parentIndex]);
    } else {
      append(childrenGfx, outerGfx);
    }

    var gfx = create$1('g');
    classes(gfx).add('djs-element');
    classes(gfx).add('djs-' + type);

    if (isFrame) {
      classes(gfx).add('djs-frame');
    }

    append(outerGfx, gfx);

    // create visual
    var visual = create$1('g');
    classes(visual).add('djs-visual');

    append(gfx, visual);

    return gfx;
  };

  /**
   * Create a graphical element.
   *
   * @param { 'shape' | 'connection' | 'label' | 'root' } type The type of the element.
   * @param {ElementLike} element The element.
   * @param {number} [parentIndex] The index at which to add the graphical element to its parent's children.
   *
   * @return {SVGElement} The graphical element.
   */
  GraphicsFactory.prototype.create = function(type, element, parentIndex) {
    var childrenGfx = this._getChildrenContainer(element.parent);
    return this._createContainer(type, childrenGfx, parentIndex, isFrameElement(element));
  };

  /**
   * Update the containments of the given elements.
   *
   * @param {ElementLike[]} elements The elements.
   */
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

      var childrenGfx = self._getChildrenContainer(parent);

      forEach(children.slice().reverse(), function(child) {
        var childGfx = elementRegistry.getGraphics(child);

        prependTo(childGfx.parentNode, childrenGfx);
      });
    });
  };

  /**
   * Draw a shape.
   *
   * @param {SVGElement} visual The graphical element.
   * @param {ShapeLike} element The shape.
   *
   * @return {SVGElement}
   */
  GraphicsFactory.prototype.drawShape = function(visual, element) {
    var eventBus = this._eventBus;

    return eventBus.fire('render.shape', { gfx: visual, element: element });
  };

  /**
   * Get the path of a shape.
   *
   * @param {ShapeLike} element The shape.
   *
   * @return {string} The path of the shape.
   */
  GraphicsFactory.prototype.getShapePath = function(element) {
    var eventBus = this._eventBus;

    return eventBus.fire('render.getShapePath', element);
  };

  /**
   * Draw a connection.
   *
   * @param {SVGElement} visual The graphical element.
   * @param {ConnectionLike} element The connection.
   *
   * @return {SVGElement}
   */
  GraphicsFactory.prototype.drawConnection = function(visual, element) {
    var eventBus = this._eventBus;

    return eventBus.fire('render.connection', { gfx: visual, element: element });
  };

  /**
   * Get the path of a connection.
   *
   * @param {ConnectionLike} connection The connection.
   *
   * @return {string} The path of the connection.
   */
  GraphicsFactory.prototype.getConnectionPath = function(connection) {
    var eventBus = this._eventBus;

    return eventBus.fire('render.getConnectionPath', connection);
  };

  /**
   * Update an elements graphical representation.
   *
   * @param {'shape'|'connection'} type
   * @param {ElementLike} element
   * @param {SVGElement} gfx
   */
  GraphicsFactory.prototype.update = function(type, element, gfx) {

    // do NOT update root element
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
      attr(gfx, 'display', 'none');
    } else {
      attr(gfx, 'display', 'block');
    }
  };

  /**
   * Remove a graphical element.
   *
   * @param {ElementLike} element The element.
   */
  GraphicsFactory.prototype.remove = function(element) {
    var gfx = this._elementRegistry.getGraphics(element);

    // remove
    remove$1(gfx.parentNode);
  };


  // helpers //////////

  function prependTo(newNode, parentNode, siblingNode) {
    var node = siblingNode || parentNode.firstChild;

    // do not prepend node to itself to prevent IE from crashing
    // https://github.com/bpmn-io/bpmn-js/issues/746
    if (newNode === node) {
      return;
    }

    parentNode.insertBefore(newNode, node);
  }

  /**
   * @type { import('didi').ModuleDeclaration }
   */
  var CoreModule = {
    __depends__: [ DrawModule ],
    __init__: [ 'canvas' ],
    canvas: [ 'type', Canvas ],
    elementRegistry: [ 'type', ElementRegistry ],
    elementFactory: [ 'type', ElementFactory ],
    eventBus: [ 'type', EventBus ],
    graphicsFactory: [ 'type', GraphicsFactory ]
  };

  /**
   * @typedef {import('didi').InjectionContext} InjectionContext
   * @typedef {import('didi').LocalsMap} LocalsMap
   * @typedef {import('didi').ModuleDeclaration} ModuleDeclaration
   *
   * @typedef { {
   *   modules?: ModuleDeclaration[];
   * } & Record<string, any> } DiagramOptions
   */

  /**
   * Bootstrap an injector from a list of modules, instantiating a number of default components
   *
   * @param {ModuleDeclaration[]} modules
   *
   * @return {Injector} a injector to use to access the components
   */
  function bootstrap(modules) {
    var injector = new Injector(modules);

    injector.init();

    return injector;
  }

  /**
   * Creates an injector from passed options.
   *
   * @param {DiagramOptions} [options]
   *
   * @return {Injector}
   */
  function createInjector(options) {

    options = options || {};

    /**
     * @type { ModuleDeclaration }
     */
    var configModule = {
      'config': [ 'value', options ]
    };

    var modules = [ configModule, CoreModule ].concat(options.modules || []);

    return bootstrap(modules);
  }


  /**
   * The main diagram-js entry point that bootstraps the diagram with the given
   * configuration.
   *
   * To register extensions with the diagram, pass them as Array<Module> to the constructor.
   *
   * @class
   * @constructor
   *
   * @example Creating a plug-in that logs whenever a shape is added to the canvas.
   *
   * ```javascript
   * // plug-in implementation
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
   * ```
   *
   * Use the plug-in in a Diagram instance:
   *
   * ```javascript
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
   * ```
   *
   * @param {DiagramOptions} [options]
   * @param {Injector} [injector] An (optional) injector to bootstrap the diagram with.
   */
  function Diagram(options, injector) {

    this._injector = injector = injector || createInjector(options);

    // API

    /**
     * Resolves a diagram service.
     *
     * @template T
     *
     * @param {string} name The name of the service to get.
     * @param {boolean} [strict=true] If false, resolve missing services to null.
     *
     * @return {T|null}
     */
    this.get = injector.get;

    /**
     * Executes a function with its dependencies injected.
     *
     * @template T
     *
     * @param {Function} func function to be invoked
     * @param {InjectionContext} [context] context of the invocation
     * @param {LocalsMap} [locals] locals provided
     *
     * @return {T|null}
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
     * ```javascript
     * eventBus.on('diagram.init', function() {
     *   eventBus.fire('my-custom-event', { foo: 'BAR' });
     * });
     * ```
     *
     * @type {Object}
     */
    this.get('eventBus').fire('diagram.init');
  }


  /**
   * Destroys the diagram
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

  inherits(Viewer, Diagram);

  function Viewer(options) {
    this._container = this._createContainer(options);

    this._init(this._container, options);
  }

  Viewer.prototype._coreModules = [
    CoreModule$1
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
      container = domify$1('<div id="' + options.containerID + '"class="umljs-container"></div>');
    } else {
      container = domify$1('<div class="umljs-container"></div>');
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

}));
