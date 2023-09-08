/*!
 * uml-js - uml-modeler v1.0.0
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
   * Flatten array, one level deep.
   *
   * @param {Array<?>} arr
   *
   * @return {Array<?>}
   */
  function flatten(arr) {
    return Array.prototype.concat.apply([], arr);
  }

  var nativeToString = Object.prototype.toString;
  var nativeHasOwnProperty = Object.prototype.hasOwnProperty;

  function isUndefined(obj) {
    return obj === undefined;
  }

  function isDefined(obj) {
    return obj !== undefined;
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

  function isString(obj) {
    return nativeToString.call(obj) === '[object String]';
  }

  /**
   * Ensure collection is an array.
   *
   * @param {Object} obj
   */
  function ensureArray(obj) {

    if (isArray(obj)) {
      return;
    }

    throw new Error('must supply array');
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
   * Find element in collection.
   *
   * @param  {Array|Object} collection
   * @param  {Function|Object} matcher
   *
   * @return {Object}
   */
  function find(collection, matcher) {

    matcher = toMatcher(matcher);

    var match;

    forEach(collection, function (val, key) {
      if (matcher(val, key)) {
        match = val;

        return false;
      }
    });

    return match;
  }

  /**
   * Find element in collection.
   *
   * @param  {Array|Object} collection
   * @param  {Function} matcher
   *
   * @return {Array} result
   */
  function filter(collection, matcher) {

    var result = [];

    forEach(collection, function (val, key) {
      if (matcher(val, key)) {
        result.push(val);
      }
    });

    return result;
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
   * Return collection without element.
   *
   * @param  {Array} arr
   * @param  {Function} matcher
   *
   * @return {Array}
   */
  function without(arr, matcher) {

    if (isUndefined(arr)) {
      return [];
    }

    ensureArray(arr);

    matcher = toMatcher(matcher);

    return arr.filter(function (el, idx) {
      return !matcher(el, idx);
    });
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

  /**
   * Return true if some elements in the collection
   * match the criteria.
   *
   * @param  {Object|Array} collection
   * @param  {Function} matcher
   *
   * @return {Boolean}
   */
  function some(collection, matcher) {

    return !!find(collection, matcher);
  }

  /**
   * Transform a collection into another collection
   * by piping each member through the given fn.
   *
   * @param  {Object|Array}   collection
   * @param  {Function} fn
   *
   * @return {Array} transformed collection
   */
  function map(collection, fn) {

    var result = [];

    forEach(collection, function (val, key) {
      result.push(fn(val, key));
    });

    return result;
  }

  /**
   * Get the collections keys.
   *
   * @param  {Object|Array} collection
   *
   * @return {Array}
   */
  function keys(collection) {
    return collection && Object.keys(collection) || [];
  }

  /**
   * Shorthand for `keys(o).length`.
   *
   * @param  {Object|Array} collection
   *
   * @return {Number}
   */
  function size(collection) {
    return keys(collection).length;
  }

  /**
   * Get the values in the collection.
   *
   * @param  {Object|Array} collection
   *
   * @return {Array}
   */
  function values(collection) {
    return map(collection, function (val) {
      return val;
    });
  }

  /**
   * Group collection members by attribute.
   *
   * @param  {Object|Array} collection
   * @param  {Function} extractor
   *
   * @return {Object} map with { attrValue => [ a, b, c ] }
   */
  function groupBy(collection, extractor) {
    var grouped = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};


    extractor = toExtractor(extractor);

    forEach(collection, function (val) {
      var discriminator = extractor(val) || '_';

      var group = grouped[discriminator];

      if (!group) {
        group = grouped[discriminator] = [];
      }

      group.push(val);
    });

    return grouped;
  }

  function uniqueBy(extractor) {

    extractor = toExtractor(extractor);

    var grouped = {};

    for (var _len = arguments.length, collections = Array(_len > 1 ? _len - 1 : 0), _key = 1; _key < _len; _key++) {
      collections[_key - 1] = arguments[_key];
    }

    forEach(collections, function (c) {
      return groupBy(c, extractor, grouped);
    });

    var result = map(grouped, function (val, key) {
      return val[0];
    });

    return result;
  }

  /**
   * Sort collection by criteria.
   *
   * @param  {Object|Array} collection
   * @param  {String|Function} extractor
   *
   * @return {Array}
   */
  function sortBy(collection, extractor) {

    extractor = toExtractor(extractor);

    var sorted = [];

    forEach(collection, function (value, key) {
      var disc = extractor(value, key);

      var entry = {
        d: disc,
        v: value
      };

      for (var idx = 0; idx < sorted.length; idx++) {
        var d = sorted[idx].d;


        if (disc < d) {
          sorted.splice(idx, 0, entry);
          return;
        }
      }

      // not inserted, append (!)
      sorted.push(entry);
    });

    return map(sorted, function (e) {
      return e.v;
    });
  }

  /**
   * Create an object pattern matcher.
   *
   * @example
   *
   * const matcher = matchPattern({ id: 1 });
   *
   * var element = find(elements, matcher);
   *
   * @param  {Object} pattern
   *
   * @return {Function} matcherFn
   */
  function matchPattern(pattern) {

    return function (el) {

      return every(pattern, function (val, key) {
        return el[key] === val;
      });
    };
  }

  function toExtractor(extractor) {
    return isFunction(extractor) ? extractor : function (e) {
      return e[extractor];
    };
  }

  function toMatcher(matcher) {
    return isFunction(matcher) ? matcher : function (e) {
      return e === matcher;
    };
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
  function bind(fn, target) {
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

  /**
   * Pick given properties from the target object.
   *
   * @param {Object} target
   * @param {Array} properties
   *
   * @return {Object} target
   */
  function pick(target, properties) {

    var result = {};

    var obj = Object(target);

    forEach(properties, function (prop) {

      if (prop in obj) {
        result[prop] = target[prop];
      }
    });

    return result;
  }

  /**
   * Set attribute `name` to `val`, or get attr `name`.
   *
   * @param {Element} el
   * @param {String} name
   * @param {String} [val]
   * @api public
   */
  function attr(el, name, val) {
    // get
    if (arguments.length == 2) {
      return el.getAttribute(name);
    }

    // remove
    if (val === null) {
      return el.removeAttribute(name);
    }

    // set
    el.setAttribute(name, val);

    return el;
  }

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

  var bind$1 = window.addEventListener ? 'addEventListener' : 'attachEvent',
      unbind = window.removeEventListener ? 'removeEventListener' : 'detachEvent',
      prefix = bind$1 !== 'addEventListener' ? 'on' : '';

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

  var unbind_1 = function(el, type, fn, capture){
    el[unbind](prefix + type, fn, capture || false);
    return fn;
  };

  var componentEvent = {
  	bind: bind_1,
  	unbind: unbind_1
  };

  /**
   * Module dependencies.
   */



  /**
   * Delegate event `type` to `selector`
   * and invoke `fn(e)`. A callback function
   * is returned which may be passed to `.unbind()`.
   *
   * @param {Element} el
   * @param {String} selector
   * @param {String} type
   * @param {Function} fn
   * @param {Boolean} capture
   * @return {Function}
   * @api public
   */

  // Some events don't bubble, so we want to bind to the capture phase instead
  // when delegating.
  var forceCaptureEvents = ['focus', 'blur'];

  var bind$1$1 = function(el, selector, type, fn, capture){
    if (forceCaptureEvents.indexOf(type) !== -1) capture = true;

    return componentEvent.bind(el, type, function(e){
      var target = e.target || e.srcElement;
      e.delegateTarget = closest(target, selector, true, el);
      if (e.delegateTarget) fn.call(el, e);
    }, capture);
  };

  /**
   * Unbind event `type`'s callback `fn`.
   *
   * @param {Element} el
   * @param {String} type
   * @param {Function} fn
   * @param {Boolean} capture
   * @api public
   */

  var unbind$1 = function(el, type, fn, capture){
    if (forceCaptureEvents.indexOf(type) !== -1) capture = true;

    componentEvent.unbind(el, type, fn, capture);
  };

  var delegateEvents = {
  	bind: bind$1$1,
  	unbind: unbind$1
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

  var map$1 = {
    legend: [1, '<fieldset>', '</fieldset>'],
    tr: [2, '<table><tbody>', '</tbody></table>'],
    col: [2, '<table><tbody></tbody><colgroup>', '</colgroup></table>'],
    // for script/link/style tags to work in IE6-8, you have to wrap
    // in a div with a non-whitespace character in front, ha!
    _default: innerHTMLBug ? [1, 'X<div>', '</div>'] : [0, '', '']
  };

  map$1.td =
  map$1.th = [3, '<table><tbody><tr>', '</tr></tbody></table>'];

  map$1.option =
  map$1.optgroup = [1, '<select multiple="multiple">', '</select>'];

  map$1.thead =
  map$1.tbody =
  map$1.colgroup =
  map$1.caption =
  map$1.tfoot = [1, '<table>', '</table>'];

  map$1.polyline =
  map$1.ellipse =
  map$1.polygon =
  map$1.circle =
  map$1.text =
  map$1.line =
  map$1.path =
  map$1.rect =
  map$1.g = [1, '<svg xmlns="http://www.w3.org/2000/svg" version="1.1">','</svg>'];

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
    var wrap = map$1[tag] || map$1._default;
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

  var matchesSelector$1 = match$1;

  /**
   * Match `el` to `selector`.
   *
   * @param {Element} el
   * @param {String} selector
   * @return {Boolean}
   * @api public
   */

  function match$1(el, selector) {
    if (!el || el.nodeType !== 1) return false;
    if (vendor$1) return vendor$1.call(el, selector);
    var nodes = el.parentNode.querySelectorAll(selector);
    for (var i = 0; i < nodes.length; i++) {
      if (nodes[i] == el) return true;
    }
    return false;
  }

  function query(selector, el) {
    el = el || document;

    return el.querySelector(selector);
  }

  function all(selector, el) {
    el = el || document;

    return el.querySelectorAll(selector);
  }

  function remove(el) {
    el.parentNode && el.parentNode.removeChild(el);
  }

  function __stopPropagation(event) {
    if (!event || typeof event.stopPropagation !== 'function') {
      return;
    }

    event.stopPropagation();
  }


  function getOriginal(event) {
    return event.originalEvent || event.srcEvent;
  }


  function stopPropagation(event, immediate) {
    __stopPropagation(event, immediate);
    __stopPropagation(getOriginal(event), immediate);
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

  function isMac() {
    return (/mac/i).test(navigator.platform);
  }

  function isPrimaryButton(event) {
    // button === 0 -> left áka primary mouse button
    return !(getOriginal(event) || event).button;
  }

  function hasPrimaryModifier(event) {
    var originalEvent = getOriginal(event) || event;

    if (!isPrimaryButton(event)) {
      return false;
    }

    // Use alt as primary modifier key for mac OS
    if (isMac()) {
      return originalEvent.metaKey;
    } else {
      return originalEvent.ctrlKey;
    }
  }


  function hasSecondaryModifier(event) {
    var originalEvent = getOriginal(event) || event;

    return isPrimaryButton(event) && originalEvent.shiftKey;
  }

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

  function clone(element) {
    return element.cloneNode(true);
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


  function set(element, svg) {

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
        set(element, svg);
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

  function updateLine(gfx, points) {
    attr$1(gfx, { points: toSVGPoints(points) });

    return gfx;
  }

  function allowAll(e) { return true; }

  var LOW_PRIORITY = 500;

  /**
   * A plugin that provides interaction events for diagram elements.
   *
   * It emits the following events:
   *
   *   * element.hover
   *   * element.out
   *   * element.click
   *   * element.dblclick
   *   * element.mousedown
   *   * element.contextmenu
   *
   * Each event is a tuple { element, gfx, originalEvent }.
   *
   * Canceling the event via Event#preventDefault()
   * prevents the original DOM operation.
   *
   * @param {EventBus} eventBus
   */
  function InteractionEvents(eventBus, elementRegistry, styles) {

    var HIT_STYLE = styles.cls('djs-hit', [ 'no-fill', 'no-border' ], {
      stroke: 'white',
      strokeWidth: 15
    });

    /**
     * Fire an interaction event.
     *
     * @param {String} type local event name, e.g. element.click.
     * @param {DOMEvent} event native event
     * @param {djs.model.Base} [element] the diagram element to emit the event on;
     *                                   defaults to the event target
     */
    function fire(type, event, element) {

      if (isIgnored(type, event)) {
        return;
      }

      var target, gfx, returnValue;

      if (!element) {
        target = event.delegateTarget || event.target;

        if (target) {
          gfx = target;
          element = elementRegistry.get(gfx);
        }
      } else {
        gfx = elementRegistry.getGraphics(element);
      }

      if (!gfx || !element) {
        return;
      }

      returnValue = eventBus.fire(type, {
        element: element,
        gfx: gfx,
        originalEvent: event
      });

      if (returnValue === false) {
        event.stopPropagation();
        event.preventDefault();
      }
    }

    // (nikku): document this
    var handlers = {};

    function mouseHandler(localEventName) {
      return handlers[localEventName];
    }

    function isIgnored(localEventName, event) {

      var filter$$1 = ignoredFilters[localEventName] || isPrimaryButton;

      // only react on left mouse button interactions
      // except for interaction events that are enabled
      // for secundary mouse button
      return !filter$$1(event);
    }

    var bindings = {
      mouseover: 'element.hover',
      mouseout: 'element.out',
      click: 'element.click',
      dblclick: 'element.dblclick',
      mousedown: 'element.mousedown',
      mouseup: 'element.mouseup',
      contextmenu: 'element.contextmenu'
    };

    var ignoredFilters = {
      'element.contextmenu': allowAll
    };


    // manual event trigger

    /**
     * Trigger an interaction event (based on a native dom event)
     * on the target shape or connection.
     *
     * @param {String} eventName the name of the triggered DOM event
     * @param {MouseEvent} event
     * @param {djs.model.Base} targetElement
     */
    function triggerMouseEvent(eventName, event, targetElement) {

      // i.e. element.mousedown...
      var localEventName = bindings[eventName];

      if (!localEventName) {
        throw new Error('unmapped DOM event name <' + eventName + '>');
      }

      return fire(localEventName, event, targetElement);
    }


    var elementSelector = 'svg, .djs-element';

    // event registration

    function registerEvent(node, event, localEvent, ignoredFilter) {

      var handler = handlers[localEvent] = function(event) {
        fire(localEvent, event);
      };

      if (ignoredFilter) {
        ignoredFilters[localEvent] = ignoredFilter;
      }

      handler.$delegate = delegateEvents.bind(node, elementSelector, event, handler);
    }

    function unregisterEvent(node, event, localEvent) {

      var handler = mouseHandler(localEvent);

      if (!handler) {
        return;
      }

      delegateEvents.unbind(node, event, handler.$delegate);
    }

    function registerEvents(svg) {
      forEach(bindings, function(val, key) {
        registerEvent(svg, key, val);
      });
    }

    function unregisterEvents(svg) {
      forEach(bindings, function(val, key) {
        unregisterEvent(svg, key, val);
      });
    }

    eventBus.on('canvas.destroy', function(event) {
      unregisterEvents(event.svg);
    });

    eventBus.on('canvas.init', function(event) {
      registerEvents(event.svg);
    });


    eventBus.on([ 'shape.added', 'connection.added' ], function(event) {
      var element = event.element,
          gfx = event.gfx,
          hit;

      if (element.waypoints) {
        hit = createLine(element.waypoints);
      } else {
        hit = create('rect');
        attr$1(hit, {
          x: 0,
          y: 0,
          width: element.width,
          height: element.height
        });
      }

      attr$1(hit, HIT_STYLE);

      append(gfx, hit);
    });

    // Update djs-hit on change.
    // A low priortity is necessary, because djs-hit of labels has to be updated
    // after the label bounds have been updated in the renderer.
    eventBus.on('shape.changed', LOW_PRIORITY, function(event) {

      var element = event.element,
          gfx = event.gfx,
          hit = query('.djs-hit', gfx);

      attr$1(hit, {
        width: element.width,
        height: element.height
      });
    });

    eventBus.on('connection.changed', function(event) {

      var element = event.element,
          gfx = event.gfx,
          hit = query('.djs-hit', gfx);

      updateLine(hit, element.waypoints);
    });


    // API

    this.fire = fire;

    this.triggerMouseEvent = triggerMouseEvent;

    this.mouseHandler = mouseHandler;

    this.registerEvent = registerEvent;
    this.unregisterEvent = unregisterEvent;
  }


  InteractionEvents.$inject = [
    'eventBus',
    'elementRegistry',
    'styles'
  ];


  /**
   * An event indicating that the mouse hovered over an element
   *
   * @event element.hover
   *
   * @type {Object}
   * @property {djs.model.Base} element
   * @property {SVGElement} gfx
   * @property {Event} originalEvent
   */

  /**
   * An event indicating that the mouse has left an element
   *
   * @event element.out
   *
   * @type {Object}
   * @property {djs.model.Base} element
   * @property {SVGElement} gfx
   * @property {Event} originalEvent
   */

  /**
   * An event indicating that the mouse has clicked an element
   *
   * @event element.click
   *
   * @type {Object}
   * @property {djs.model.Base} element
   * @property {SVGElement} gfx
   * @property {Event} originalEvent
   */

  /**
   * An event indicating that the mouse has double clicked an element
   *
   * @event element.dblclick
   *
   * @type {Object}
   * @property {djs.model.Base} element
   * @property {SVGElement} gfx
   * @property {Event} originalEvent
   */

  /**
   * An event indicating that the mouse has gone down on an element.
   *
   * @event element.mousedown
   *
   * @type {Object}
   * @property {djs.model.Base} element
   * @property {SVGElement} gfx
   * @property {Event} originalEvent
   */

  /**
   * An event indicating that the mouse has gone up on an element.
   *
   * @event element.mouseup
   *
   * @type {Object}
   * @property {djs.model.Base} element
   * @property {SVGElement} gfx
   * @property {Event} originalEvent
   */

  /**
   * An event indicating that the context menu action is triggered
   * via mouse or touch controls.
   *
   * @event element.contextmenu
   *
   * @type {Object}
   * @property {djs.model.Base} element
   * @property {SVGElement} gfx
   * @property {Event} originalEvent
   */

  var InteractionEventsModule = {
    __init__: [ 'interactionEvents' ],
    interactionEvents: [ 'type', InteractionEvents ]
  };

  /**
   * Adds an element to a collection and returns true if the
   * element was added.
   *
   * @param {Array<Object>} elements
   * @param {Object} e
   * @param {Boolean} unique
   */
  function add(elements, e, unique) {
    var canAdd = !unique || elements.indexOf(e) === -1;

    if (canAdd) {
      elements.push(e);
    }

    return canAdd;
  }


  /**
   * Iterate over each element in a collection, calling the iterator function `fn`
   * with (element, index, recursionDepth).
   *
   * Recurse into all elements that are returned by `fn`.
   *
   * @param  {Object|Array<Object>} elements
   * @param  {Function} fn iterator function called with (element, index, recursionDepth)
   * @param  {Number} [depth] maximum recursion depth
   */
  function eachElement(elements, fn, depth) {

    depth = depth || 0;

    if (!isArray(elements)) {
      elements = [ elements ];
    }

    forEach(elements, function(s, i) {
      var filter$$1 = fn(s, i, depth);

      if (isArray(filter$$1) && filter$$1.length) {
        eachElement(filter$$1, fn, depth + 1);
      }
    });
  }


  /**
   * Collects self + child elements up to a given depth from a list of elements.
   *
   * @param  {djs.model.Base|Array<djs.model.Base>} elements the elements to select the children from
   * @param  {Boolean} unique whether to return a unique result set (no duplicates)
   * @param  {Number} maxDepth the depth to search through or -1 for infinite
   *
   * @return {Array<djs.model.Base>} found elements
   */
  function selfAndChildren(elements, unique, maxDepth) {
    var result = [],
        processedChildren = [];

    eachElement(elements, function(element, i, depth) {
      add(result, element, unique);

      var children = element.children;

      // max traversal depth not reached yet
      if (maxDepth === -1 || depth < maxDepth) {

        // children exist && children not yet processed
        if (children && add(processedChildren, children, unique)) {
          return children;
        }
      }
    });

    return result;
  }


  /**
   * Return self + ALL children for a number of elements
   *
   * @param  {Array<djs.model.Base>} elements to query
   * @param  {Boolean} allowDuplicates to allow duplicates in the result set
   *
   * @return {Array<djs.model.Base>} the collected elements
   */
  function selfAndAllChildren(elements, allowDuplicates) {
    return selfAndChildren(elements, !allowDuplicates, -1);
  }


  /**
   * Gets the the closure for all selected elements,
   * their enclosed children and connections.
   *
   * @param {Array<djs.model.Base>} elements
   * @param {Boolean} [isTopLevel=true]
   * @param {Object} [existingClosure]
   *
   * @return {Object} newClosure
   */
  function getClosure(elements, isTopLevel, closure) {

    if (isUndefined(isTopLevel)) {
      isTopLevel = true;
    }

    if (isObject(isTopLevel)) {
      closure = isTopLevel;
      isTopLevel = true;
    }


    closure = closure || {};

    var allShapes = copyObject(closure.allShapes),
        allConnections = copyObject(closure.allConnections),
        enclosedElements = copyObject(closure.enclosedElements),
        enclosedConnections = copyObject(closure.enclosedConnections);

    var topLevel = copyObject(
      closure.topLevel,
      isTopLevel && groupBy(elements, function(e) { return e.id; })
    );


    function handleConnection(c) {
      if (topLevel[c.source.id] && topLevel[c.target.id]) {
        topLevel[c.id] = [ c ];
      }

      // not enclosed as a child, but maybe logically
      // (connecting two moved elements?)
      if (allShapes[c.source.id] && allShapes[c.target.id]) {
        enclosedConnections[c.id] = enclosedElements[c.id] = c;
      }

      allConnections[c.id] = c;
    }

    function handleElement(element) {

      enclosedElements[element.id] = element;

      if (element.waypoints) {
        // remember connection
        enclosedConnections[element.id] = allConnections[element.id] = element;
      } else {
        // remember shape
        allShapes[element.id] = element;

        // remember all connections
        forEach(element.incoming, handleConnection);

        forEach(element.outgoing, handleConnection);

        // recurse into children
        return element.children;
      }
    }

    eachElement(elements, handleElement);

    return {
      allShapes: allShapes,
      allConnections: allConnections,
      topLevel: topLevel,
      enclosedConnections: enclosedConnections,
      enclosedElements: enclosedElements
    };
  }

  /**
   * Returns the surrounding bbox for all elements in
   * the array or the element primitive.
   *
   * @param {Array<djs.model.Shape>|djs.model.Shape} elements
   * @param {Boolean} stopRecursion
   */
  function getBBox(elements, stopRecursion) {

    stopRecursion = !!stopRecursion;
    if (!isArray(elements)) {
      elements = [elements];
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
   * Returns all elements that are enclosed from the bounding box.
   *
   *   * If bbox.(width|height) is not specified the method returns
   *     all elements with element.x/y > bbox.x/y
   *   * If only bbox.x or bbox.y is specified, method return all elements with
   *     e.x > bbox.x or e.y > bbox.y
   *
   * @param {Array<djs.model.Shape>} elements List of Elements to search through
   * @param {djs.model.Shape} bbox the enclosing bbox.
   *
   * @return {Array<djs.model.Shape>} enclosed elements
   */
  function getEnclosedElements(elements, bbox) {

    var filteredElements = {};

    forEach(elements, function(element) {

      var e = element;

      if (e.waypoints) {
        e = getBBox(e);
      }

      if (!isNumber(bbox.y) && (e.x > bbox.x)) {
        filteredElements[element.id] = element;
      }
      if (!isNumber(bbox.x) && (e.y > bbox.y)) {
        filteredElements[element.id] = element;
      }
      if (e.x > bbox.x && e.y > bbox.y) {
        if (isNumber(bbox.width) && isNumber(bbox.height) &&
            e.width + e.x < bbox.width + bbox.x &&
            e.height + e.y < bbox.height + bbox.y) {

          filteredElements[element.id] = element;
        } else if (!isNumber(bbox.width) || !isNumber(bbox.height)) {
          filteredElements[element.id] = element;
        }
      }
    });

    return filteredElements;
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


  // helpers ///////////////////////////////

  function copyObject(src1, src2) {
    return assign({}, src1 || {}, src2 || {});
  }

  var LOW_PRIORITY$1 = 500;


  /**
   * @class
   *
   * A plugin that adds an outline to shapes and connections that may be activated and styled
   * via CSS classes.
   *
   * @param {EventBus} eventBus
   * @param {Styles} styles
   * @param {ElementRegistry} elementRegistry
   */
  function Outline(eventBus, styles, elementRegistry) {

    this.offset = 6;

    var OUTLINE_STYLE = styles.cls('djs-outline', [ 'no-fill' ]);

    var self = this;

    function createOutline(gfx, bounds) {
      var outline = create('rect');

      attr$1(outline, assign({
        x: 10,
        y: 10,
        width: 100,
        height: 100
      }, OUTLINE_STYLE));

      append(gfx, outline);

      return outline;
    }

    // A low priortity is necessary, because outlines of labels have to be updated
    // after the label bounds have been updated in the renderer.
    eventBus.on([ 'shape.added', 'shape.changed' ], LOW_PRIORITY$1, function(event) {
      var element = event.element,
          gfx = event.gfx;

      var outline = query('.djs-outline', gfx);

      if (!outline) {
        outline = createOutline(gfx, element);
      }

      self.updateShapeOutline(outline, element);
    });

    eventBus.on([ 'connection.added', 'connection.changed' ], function(event) {
      var element = event.element,
          gfx = event.gfx;

      var outline = query('.djs-outline', gfx);

      if (!outline) {
        outline = createOutline(gfx, element);
      }

      self.updateConnectionOutline(outline, element);
    });
  }


  /**
   * Updates the outline of a shape respecting the dimension of the
   * element and an outline offset.
   *
   * @param  {SVGElement} outline
   * @param  {djs.model.Base} element
   */
  Outline.prototype.updateShapeOutline = function(outline, element) {

    attr$1(outline, {
      x: -this.offset,
      y: -this.offset,
      width: element.width + this.offset * 2,
      height: element.height + this.offset * 2
    });

  };


  /**
   * Updates the outline of a connection respecting the bounding box of
   * the connection and an outline offset.
   *
   * @param  {SVGElement} outline
   * @param  {djs.model.Base} element
   */
  Outline.prototype.updateConnectionOutline = function(outline, connection) {

    var bbox = getBBox(connection);

    attr$1(outline, {
      x: bbox.x - this.offset,
      y: bbox.y - this.offset,
      width: bbox.width + this.offset * 2,
      height: bbox.height + this.offset * 2
    });

  };


  Outline.$inject = ['eventBus', 'styles', 'elementRegistry'];

  var OutlineModule = {
    __init__: [ 'outline' ],
    outline: [ 'type', Outline ]
  };

  /**
   * A service that offers the current selection in a diagram.
   * Offers the api to control the selection, too.
   *
   * @class
   *
   * @param {EventBus} eventBus the event bus
   */
  function Selection(eventBus) {

    this._eventBus = eventBus;

    this._selectedElements = [];

    var self = this;

    eventBus.on([ 'shape.remove', 'connection.remove' ], function(e) {
      var element = e.element;
      self.deselect(element);
    });

    eventBus.on([ 'diagram.clear' ], function(e) {
      self.select(null);
    });
  }

  Selection.$inject = [ 'eventBus' ];


  Selection.prototype.deselect = function(element) {
    var selectedElements = this._selectedElements;

    var idx = selectedElements.indexOf(element);

    if (idx !== -1) {
      var oldSelection = selectedElements.slice();

      selectedElements.splice(idx, 1);

      this._eventBus.fire('selection.changed', { oldSelection: oldSelection, newSelection: selectedElements });
    }
  };


  Selection.prototype.get = function() {
    return this._selectedElements;
  };

  Selection.prototype.isSelected = function(element) {
    return this._selectedElements.indexOf(element) !== -1;
  };


  /**
   * This method selects one or more elements on the diagram.
   *
   * By passing an additional add parameter you can decide whether or not the element(s)
   * should be added to the already existing selection or not.
   *
   * @method Selection#select
   *
   * @param  {Object|Object[]} elements element or array of elements to be selected
   * @param  {boolean} [add] whether the element(s) should be appended to the current selection, defaults to false
   */
  Selection.prototype.select = function(elements, add) {
    var selectedElements = this._selectedElements,
        oldSelection = selectedElements.slice();

    if (!isArray(elements)) {
      elements = elements ? [ elements ] : [];
    }

    // selection may be cleared by passing an empty array or null
    // to the method
    if (add) {
      forEach(elements, function(element) {
        if (selectedElements.indexOf(element) !== -1) {
          // already selected
          return;
        } else {
          selectedElements.push(element);
        }
      });
    } else {
      this._selectedElements = selectedElements = elements.slice();
    }

    this._eventBus.fire('selection.changed', { oldSelection: oldSelection, newSelection: selectedElements });
  };

  var MARKER_HOVER = 'hover',
      MARKER_SELECTED = 'selected';


  /**
   * A plugin that adds a visible selection UI to shapes and connections
   * by appending the <code>hover</code> and <code>selected</code> classes to them.
   *
   * @class
   *
   * Makes elements selectable, too.
   *
   * @param {EventBus} events
   * @param {SelectionService} selection
   * @param {Canvas} canvas
   */
  function SelectionVisuals(events, canvas, selection, styles) {

    this._multiSelectionBox = null;

    function addMarker(e, cls) {
      canvas.addMarker(e, cls);
    }

    function removeMarker(e, cls) {
      canvas.removeMarker(e, cls);
    }

    events.on('element.hover', function(event) {
      addMarker(event.element, MARKER_HOVER);
    });

    events.on('element.out', function(event) {
      removeMarker(event.element, MARKER_HOVER);
    });

    events.on('selection.changed', function(event) {

      function deselect(s) {
        removeMarker(s, MARKER_SELECTED);
      }

      function select(s) {
        addMarker(s, MARKER_SELECTED);
      }

      var oldSelection = event.oldSelection,
          newSelection = event.newSelection;

      forEach(oldSelection, function(e) {
        if (newSelection.indexOf(e) === -1) {
          deselect(e);
        }
      });

      forEach(newSelection, function(e) {
        if (oldSelection.indexOf(e) === -1) {
          select(e);
        }
      });
    });
  }

  SelectionVisuals.$inject = [
    'eventBus',
    'canvas',
    'selection',
    'styles'
  ];

  function SelectionBehavior(
      eventBus, selection, canvas,
      elementRegistry) {

    eventBus.on('create.end', 500, function(e) {

      // select the created shape after a
      // successful create operation
      if (e.context.canExecute) {
        selection.select(e.context.shape);
      }
    });

    eventBus.on('connect.end', 500, function(e) {

      // select the connect end target
      // after a connect operation
      if (e.context.canExecute && e.context.target) {
        selection.select(e.context.target);
      }
    });

    eventBus.on('shape.move.end', 500, function(e) {
      var previousSelection = e.previousSelection || [];

      var shape = elementRegistry.get(e.context.shape.id);

      // make sure at least the main moved element is being
      // selected after a move operation
      var inSelection = find(previousSelection, function(selectedShape) {
        return shape.id === selectedShape.id;
      });

      if (!inSelection) {
        selection.select(shape);
      }
    });

    // Shift + click selection
    eventBus.on('element.click', function(event) {

      var element = event.element;

      // do not select the root element
      // or connections
      if (element === canvas.getRootElement()) {
        element = null;
      }

      var isSelected = selection.isSelected(element),
          isMultiSelect = selection.get().length > 1;

      // mouse-event: SELECTION_KEY
      var add = hasPrimaryModifier(event);

      // select OR deselect element in multi selection
      if (isSelected && isMultiSelect) {
        if (add) {
          return selection.deselect(element);
        } else {
          return selection.select(element);
        }
      } else
      if (!isSelected) {
        selection.select(element, add);
      } else {
        selection.deselect(element);
      }
    });
  }

  SelectionBehavior.$inject = [
    'eventBus',
    'selection',
    'canvas',
    'elementRegistry'
  ];

  var SelectionModule = {
    __init__: [ 'selectionVisuals', 'selectionBehavior' ],
    __depends__: [
      InteractionEventsModule,
      OutlineModule
    ],
    selection: [ 'type', Selection ],
    selectionVisuals: [ 'type', SelectionVisuals ],
    selectionBehavior: [ 'type', SelectionBehavior ]
  };

  /**
   * A service that provides rules for certain diagram actions.
   *
   * The default implementation will hook into the {@link CommandStack}
   * to perform the actual rule evaluation. Make sure to provide the
   * `commandStack` service with this module if you plan to use it.
   *
   * Together with this implementation you may use the {@link RuleProvider}
   * to implement your own rule checkers.
   *
   * This module is ment to be easily replaced, thus the tiny foot print.
   *
   * @param {Injector} injector
   */
  function Rules(injector) {
    this._commandStack = injector.get('commandStack', false);
  }

  Rules.$inject = [ 'injector' ];


  /**
   * Returns whether or not a given modeling action can be executed
   * in the specified context.
   *
   * This implementation will respond with allow unless anyone
   * objects.
   *
   * @param {String} action the action to be checked
   * @param {Object} [context] the context to check the action in
   *
   * @return {Boolean} returns true, false or null depending on whether the
   *                   operation is allowed, not allowed or should be ignored.
   */
  Rules.prototype.allowed = function(action, context) {
    var allowed = true;

    var commandStack = this._commandStack;

    if (commandStack) {
      allowed = commandStack.canExecute(action, context);
    }

    // map undefined to true, i.e. no rules
    return allowed === undefined ? true : allowed;
  };

  var RulesModule = {
    __init__: [ 'rules' ],
    rules: [ 'type', Rules ]
  };

  var CURSOR_CLS_PATTERN = /^djs-cursor-.*$/;


  function set$1(mode) {
    var classes$$1 = classes(document.body);

    classes$$1.removeMatching(CURSOR_CLS_PATTERN);

    if (mode) {
      classes$$1.add('djs-cursor-' + mode);
    }
  }

  function unset() {
    set$1(null);
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

  function center(bounds) {
    return {
      x: bounds.x + (bounds.width / 2),
      y: bounds.y + (bounds.height / 2)
    };
  }


  function delta(a, b) {
    return {
      x: a.x - b.x,
      y: a.y - b.y
    };
  }

  /* global TouchEvent */

  var round = Math.round;

  var DRAG_ACTIVE_CLS = 'djs-drag-active';


  function preventDefault(event$$1) {
    event$$1.preventDefault();
  }

  function isTouchEvent(event$$1) {
    // check for TouchEvent being available first
    // (i.e. not available on desktop Firefox)
    return typeof TouchEvent !== 'undefined' && event$$1 instanceof TouchEvent;
  }

  function getLength(point) {
    return Math.sqrt(Math.pow(point.x, 2) + Math.pow(point.y, 2));
  }

  /**
   * A helper that fires canvas localized drag events and realizes
   * the general "drag-and-drop" look and feel.
   *
   * Calling {@link Dragging#activate} activates dragging on a canvas.
   *
   * It provides the following:
   *
   *   * emits life cycle events, namespaced with a prefix assigned
   *     during dragging activation
   *   * sets and restores the cursor
   *   * sets and restores the selection
   *   * ensures there can be only one drag operation active at a time
   *
   * Dragging may be canceled manually by calling {@link Dragging#cancel}
   * or by pressing ESC.
   *
   *
   * ## Life-cycle events
   *
   * Dragging can be in three different states, off, initialized
   * and active.
   *
   * (1) off: no dragging operation is in progress
   * (2) initialized: a new drag operation got initialized but not yet
   *                  started (i.e. because of no initial move)
   * (3) started: dragging is in progress
   *
   * Eventually dragging will be off again after a drag operation has
   * been ended or canceled via user click or ESC key press.
   *
   * To indicate transitions between these states dragging emits generic
   * life-cycle events with the `drag.` prefix _and_ events namespaced
   * to a prefix choosen by a user during drag initialization.
   *
   * The following events are emitted (appropriately prefixed) via
   * the {@link EventBus}.
   *
   * * `init`
   * * `start`
   * * `move`
   * * `end`
   * * `ended` (dragging already in off state)
   * * `cancel` (only if previously started)
   * * `canceled` (dragging already in off state, only if previously started)
   * * `cleanup`
   *
   *
   * @example
   *
   * function MyDragComponent(eventBus, dragging) {
   *
   *   eventBus.on('mydrag.start', function(event) {
   *     console.log('yes, we start dragging');
   *   });
   *
   *   eventBus.on('mydrag.move', function(event) {
   *     console.log('canvas local coordinates', event.x, event.y, event.dx, event.dy);
   *
   *     // local drag data is passed with the event
   *     event.context.foo; // "BAR"
   *
   *     // the original mouse event, too
   *     event.originalEvent; // MouseEvent(...)
   *   });
   *
   *   eventBus.on('element.click', function(event) {
   *     dragging.init(event, 'mydrag', {
   *       cursor: 'grabbing',
   *       data: {
   *         context: {
   *           foo: "BAR"
   *         }
   *       }
   *     });
   *   });
   * }
   */
  function Dragging(eventBus, canvas, selection) {

    var defaultOptions = {
      threshold: 5,
      trapClick: true
    };

    // the currently active drag operation
    // dragging is active as soon as this context exists.
    //
    // it is visually _active_ only when a context.active flag is set to true.
    var context;

    /* convert a global event into local coordinates */
    function toLocalPoint(globalPosition) {

      var viewbox = canvas.viewbox();

      var clientRect = canvas._container.getBoundingClientRect();

      return {
        x: viewbox.x + (globalPosition.x - clientRect.left) / viewbox.scale,
        y: viewbox.y + (globalPosition.y - clientRect.top) / viewbox.scale
      };
    }

    // helpers

    function fire(type, dragContext) {
      dragContext = dragContext || context;

      var event$$1 = eventBus.createEvent(
        assign(
          {},
          dragContext.payload,
          dragContext.data,
          { isTouch: dragContext.isTouch }
        )
      );

      // default integration
      if (eventBus.fire('drag.' + type, event$$1) === false) {
        return false;
      }

      return eventBus.fire(dragContext.prefix + '.' + type, event$$1);
    }

    // event listeners

    function move(event$$1, activate) {
      var payload = context.payload,
          displacement = context.displacement;

      var globalStart = context.globalStart,
          globalCurrent = toPoint(event$$1),
          globalDelta = delta(globalCurrent, globalStart);

      var localStart = context.localStart,
          localCurrent = toLocalPoint(globalCurrent),
          localDelta = delta(localCurrent, localStart);


      // activate context explicitly or once threshold is reached
      if (!context.active && (activate || getLength(globalDelta) > context.threshold)) {

        // fire start event with original
        // starting coordinates

        assign(payload, {
          x: round(localStart.x + displacement.x),
          y: round(localStart.y + displacement.y),
          dx: 0,
          dy: 0
        }, { originalEvent: event$$1 });

        if (false === fire('start')) {
          return cancel();
        }

        context.active = true;

        // unset selection and remember old selection
        // the previous (old) selection will always passed
        // with the event via the event.previousSelection property
        if (!context.keepSelection) {
          payload.previousSelection = selection.get();
          selection.select(null);
        }

        // allow custom cursor
        if (context.cursor) {
          set$1(context.cursor);
        }

        // indicate dragging via marker on root element
        canvas.addMarker(canvas.getRootElement(), DRAG_ACTIVE_CLS);
      }

      stopPropagation(event$$1);

      if (context.active) {

        // update payload with actual coordinates
        assign(payload, {
          x: round(localCurrent.x + displacement.x),
          y: round(localCurrent.y + displacement.y),
          dx: round(localDelta.x),
          dy: round(localDelta.y)
        }, { originalEvent: event$$1 });

        // emit move event
        fire('move');
      }
    }

    function end(event$$1) {
      var previousContext,
          returnValue = true;

      if (context.active) {

        if (event$$1) {
          context.payload.originalEvent = event$$1;

          // suppress original event (click, ...)
          // because we just ended a drag operation
          stopPropagation(event$$1);
        }

        // implementations may stop restoring the
        // original state (selections, ...) by preventing the
        // end events default action
        returnValue = fire('end');
      }

      if (returnValue === false) {
        fire('rejected');
      }

      previousContext = cleanup(returnValue !== true);

      // last event to be fired when all drag operations are done
      // at this point in time no drag operation is in progress anymore
      fire('ended', previousContext);
    }


    // cancel active drag operation if the user presses
    // the ESC key on the keyboard

    function checkCancel(event$$1) {

      if (event$$1.which === 27) {
        preventDefault(event$$1);

        cancel();
      }
    }


    // prevent ghost click that might occur after a finished
    // drag and drop session

    function trapClickAndEnd(event$$1) {

      var untrap;

      // trap the click in case we are part of an active
      // drag operation. This will effectively prevent
      // the ghost click that cannot be canceled otherwise.
      if (context.active) {

        untrap = install(eventBus);

        // remove trap after minimal delay
        setTimeout(untrap, 400);

        // prevent default action (click)
        preventDefault(event$$1);
      }

      end(event$$1);
    }

    function trapTouch(event$$1) {
      move(event$$1);
    }

    // update the drag events hover (djs.model.Base) and hoverGfx (Snap<SVGElement>)
    // properties during hover and out and fire {prefix}.hover and {prefix}.out properties
    // respectively

    function hover(event$$1) {
      var payload = context.payload;

      payload.hoverGfx = event$$1.gfx;
      payload.hover = event$$1.element;

      fire('hover');
    }

    function out(event$$1) {
      fire('out');

      var payload = context.payload;

      payload.hoverGfx = null;
      payload.hover = null;
    }


    // life-cycle methods

    function cancel(restore) {
      var previousContext;

      if (!context) {
        return;
      }

      var wasActive = context.active;

      if (wasActive) {
        fire('cancel');
      }

      previousContext = cleanup(restore);

      if (wasActive) {
        // last event to be fired when all drag operations are done
        // at this point in time no drag operation is in progress anymore
        fire('canceled', previousContext);
      }
    }

    function cleanup(restore) {
      var previousContext,
          endDrag;

      fire('cleanup');

      // reset cursor
      unset();

      if (context.trapClick) {
        endDrag = trapClickAndEnd;
      } else {
        endDrag = end;
      }

      // reset dom listeners
      componentEvent.unbind(document, 'mousemove', move);

      componentEvent.unbind(document, 'dragstart', preventDefault);
      componentEvent.unbind(document, 'selectstart', preventDefault);

      componentEvent.unbind(document, 'mousedown', endDrag, true);
      componentEvent.unbind(document, 'mouseup', endDrag, true);

      componentEvent.unbind(document, 'keyup', checkCancel);

      componentEvent.unbind(document, 'touchstart', trapTouch, true);
      componentEvent.unbind(document, 'touchcancel', cancel, true);
      componentEvent.unbind(document, 'touchmove', move, true);
      componentEvent.unbind(document, 'touchend', end, true);

      eventBus.off('element.hover', hover);
      eventBus.off('element.out', out);

      // remove drag marker on root element
      canvas.removeMarker(canvas.getRootElement(), DRAG_ACTIVE_CLS);

      // restore selection, unless it has changed
      var previousSelection = context.payload.previousSelection;

      if (restore !== false && previousSelection && !selection.get().length) {
        selection.select(previousSelection);
      }

      previousContext = context;

      context = null;

      return previousContext;
    }

    /**
     * Initialize a drag operation.
     *
     * If `localPosition` is given, drag events will be emitted
     * relative to it.
     *
     * @param {MouseEvent|TouchEvent} [event]
     * @param {Point} [localPosition] actual diagram local position this drag operation should start at
     * @param {String} prefix
     * @param {Object} [options]
     */
    function init(event$$1, relativeTo, prefix, options) {

      // only one drag operation may be active, at a time
      if (context) {
        cancel(false);
      }

      if (typeof relativeTo === 'string') {
        options = prefix;
        prefix = relativeTo;
        relativeTo = null;
      }

      options = assign({}, defaultOptions, options || {});

      var data = options.data || {},
          originalEvent,
          globalStart,
          localStart,
          endDrag,
          isTouch;

      if (options.trapClick) {
        endDrag = trapClickAndEnd;
      } else {
        endDrag = end;
      }

      if (event$$1) {
        originalEvent = getOriginal(event$$1) || event$$1;
        globalStart = toPoint(event$$1);

        stopPropagation(event$$1);

        // prevent default browser dragging behavior
        if (originalEvent.type === 'dragstart') {
          preventDefault(originalEvent);
        }
      } else {
        originalEvent = null;
        globalStart = { x: 0, y: 0 };
      }

      localStart = toLocalPoint(globalStart);

      if (!relativeTo) {
        relativeTo = localStart;
      }

      isTouch = isTouchEvent(originalEvent);

      context = assign({
        prefix: prefix,
        data: data,
        payload: {},
        globalStart: globalStart,
        displacement: delta(relativeTo, localStart),
        localStart: localStart,
        isTouch: isTouch
      }, options);

      // skip dom registration if trigger
      // is set to manual (during testing)
      if (!options.manual) {

        // add dom listeners

        if (isTouch) {
          componentEvent.bind(document, 'touchstart', trapTouch, true);
          componentEvent.bind(document, 'touchcancel', cancel, true);
          componentEvent.bind(document, 'touchmove', move, true);
          componentEvent.bind(document, 'touchend', end, true);
        } else {
          // assume we use the mouse to interact per default
          componentEvent.bind(document, 'mousemove', move);

          // prevent default browser drag and text selection behavior
          componentEvent.bind(document, 'dragstart', preventDefault);
          componentEvent.bind(document, 'selectstart', preventDefault);

          componentEvent.bind(document, 'mousedown', endDrag, true);
          componentEvent.bind(document, 'mouseup', endDrag, true);
        }

        componentEvent.bind(document, 'keyup', checkCancel);

        eventBus.on('element.hover', hover);
        eventBus.on('element.out', out);
      }

      fire('init');

      if (options.autoActivate) {
        move(event$$1, true);
      }
    }

    // cancel on diagram destruction
    eventBus.on('diagram.destroy', cancel);


    // API

    this.init = init;
    this.move = move;
    this.hover = hover;
    this.out = out;
    this.end = end;

    this.cancel = cancel;

    // for introspection

    this.context = function() {
      return context;
    };

    this.setOptions = function(options) {
      assign(defaultOptions, options);
    };
  }

  Dragging.$inject = [
    'eventBus',
    'canvas',
    'selection'
  ];

  function getGfx(target) {
    var node = closest(target, 'svg, .djs-element', true);
    return node;
  }


  /**
   * Browsers may swallow the hover event if users are to
   * fast with the mouse.
   *
   * @see http://stackoverflow.com/questions/7448468/why-cant-i-reliably-capture-a-mouseout-event
   *
   * The fix implemented in this component ensure that we
   * have a hover state after a successive drag.move event.
   *
   * @param {EventBus} eventBus
   * @param {Dragging} dragging
   * @param {ElementRegistry} elementRegistry
   */
  function HoverFix(eventBus, dragging, elementRegistry) {

    var self = this;

    // we wait for a specific sequence of events before
    // emitting a fake drag.hover event.
    //
    // Event Sequence:
    //
    // drag.start
    // drag.move
    // drag.move >> ensure we are hovering
    //
    eventBus.on('drag.start', function(event) {

      eventBus.once('drag.move', function() {

        eventBus.once('drag.move', function(event) {

          self.ensureHover(event);
        });
      });
    });

    /**
     * Make sure we are god damn hovering!
     *
     * @param {Event} dragging event
     */
    this.ensureHover = function(event) {

      if (event.hover) {
        return;
      }

      var originalEvent = event.originalEvent,
          position,
          target,
          element,
          gfx;

      if (!(originalEvent instanceof MouseEvent)) {
        return;
      }

      position = toPoint(originalEvent);

      // damn expensive operation, ouch!
      target = document.elementFromPoint(position.x, position.y);

      gfx = getGfx(target);

      if (gfx) {
        element = elementRegistry.get(gfx);

        dragging.hover({ element: element, gfx: gfx });
      }
    };

  }

  HoverFix.$inject = [
    'eventBus',
    'dragging',
    'elementRegistry'
  ];

  var DraggingModule = {
    __init__: [
      'hoverFix'
    ],
    __depends__: [
      SelectionModule
    ],
    dragging: [ 'type', Dragging ],
    hoverFix: [ 'type', HoverFix ]
  };

  /**
   * Adds support for previews of moving/resizing elements.
   */
  function PreviewSupport(elementRegistry, canvas, styles) {
    this._elementRegistry = elementRegistry;
    this._canvas = canvas;
    this._styles = styles;
  }

  PreviewSupport.$inject = [
    'elementRegistry',
    'canvas',
    'styles'
  ];


  /**
   * Returns graphics of an element.
   *
   * @param {djs.model.Base} element
   *
   * @return {SVGElement}
   */
  PreviewSupport.prototype.getGfx = function(element) {
    return this._elementRegistry.getGraphics(element);
  };

  /**
   * Adds a move preview of a given shape to a given svg group.
   *
   * @param {djs.model.Base} element
   * @param {SVGElement} group
   *
   * @return {SVGElement} dragger
   */
  PreviewSupport.prototype.addDragger = function(shape, group) {
    var gfx = this.getGfx(shape);

    // clone is not included in tsvg for some reason
    var dragger = clone(gfx);
    var bbox = gfx.getBoundingClientRect();

    // remove markers from connections
    if (isConnection(shape)) {
      removeMarkers(dragger);
    }

    attr$1(dragger, this._styles.cls('djs-dragger', [], {
      x: bbox.top,
      y: bbox.left
    }));

    append(group, dragger);

    return dragger;
  };

  /**
   * Adds a resize preview of a given shape to a given svg group.
   *
   * @param {djs.model.Base} element
   * @param {SVGElement} group
   *
   * @return {SVGElement} frame
   */
  PreviewSupport.prototype.addFrame = function(shape, group) {

    var frame = create('rect', {
      class: 'djs-resize-overlay',
      width:  shape.width,
      height: shape.height,
      x: shape.x,
      y: shape.y
    });

    append(group, frame);

    return frame;
  };


  // helpers //////////////////////

  /**
   * Removes all svg marker references from an SVG.
   *
   * @param {SVGElement} gfx
   */
  function removeMarkers(gfx) {

    if (gfx.children) {

      forEach(gfx.children, function(child) {

        // recursion
        removeMarkers(child);

      });

    }

    gfx.style.markerStart = '';
    gfx.style.markerEnd = '';

  }

  /**
   * Checks if an element is a connection.
   */
  function isConnection(element) {
    return element.waypoints;
  }

  var PreviewSupportModule = {
    __init__: [ 'previewSupport' ],
    previewSupport: [ 'type', PreviewSupport ]
  };

  var LOW_PRIORITY$2 = 500,
      MEDIUM_PRIORITY = 1250,
      HIGH_PRIORITY = 1500;

  var round$1 = Math.round;

  function mid(element) {
    return {
      x: element.x + round$1(element.width / 2),
      y: element.y + round$1(element.height / 2)
    };
  }

  /**
   * A plugin that makes shapes draggable / droppable.
   *
   * @param {EventBus} eventBus
   * @param {Dragging} dragging
   * @param {Modeling} modeling
   * @param {Selection} selection
   * @param {Rules} rules
   */
  function MoveEvents(
      eventBus, dragging, modeling,
      selection, rules) {

    // rules

    function canMove(shapes, delta, position, target) {

      return rules.allowed('elements.move', {
        shapes: shapes,
        delta: delta,
        position: position,
        target: target
      });
    }


    // move events

    // assign a high priority to this handler to setup the environment
    // others may hook up later, e.g. at default priority and modify
    // the move environment.
    //
    // This sets up the context with
    //
    // * shape: the primary shape being moved
    // * shapes: a list of shapes to be moved
    // * validatedShapes: a list of shapes that are being checked
    //                    against the rules before and during move
    //
    eventBus.on('shape.move.start', HIGH_PRIORITY, function(event) {

      var context = event.context,
          shape = event.shape,
          shapes = selection.get().slice();

      // move only single shape if the dragged element
      // is not part of the current selection
      if (shapes.indexOf(shape) === -1) {
        shapes = [ shape ];
      }

      // ensure we remove nested elements in the collection
      // and add attachers for a proper dragger
      shapes = removeNested(shapes);

      // attach shapes to drag context
      assign(context, {
        shapes: shapes,
        validatedShapes: shapes,
        shape: shape
      });
    });


    // assign a high priority to this handler to setup the environment
    // others may hook up later, e.g. at default priority and modify
    // the move environment
    //
    eventBus.on('shape.move.start', MEDIUM_PRIORITY, function(event) {

      var context = event.context,
          validatedShapes = context.validatedShapes,
          canExecute;

      canExecute = context.canExecute = canMove(validatedShapes);

      // check if we can move the elements
      if (!canExecute) {
        return false;
      }
    });

    // assign a low priority to this handler
    // to let others modify the move event before we update
    // the context
    //
    eventBus.on('shape.move.move', LOW_PRIORITY$2, function(event) {

      var context = event.context,
          validatedShapes = context.validatedShapes,
          hover = event.hover,
          delta = { x: event.dx, y: event.dy },
          position = { x: event.x, y: event.y },
          canExecute;

      // check if we can move the elements
      canExecute = canMove(validatedShapes, delta, position, hover);

      context.delta = delta;
      context.canExecute = canExecute;

      // simply ignore move over
      if (canExecute === null) {
        context.target = null;

        return;
      }

      context.target = hover;
    });

    eventBus.on('shape.move.end', function(event) {

      var context = event.context;

      var delta = context.delta,
          canExecute = context.canExecute,
          isAttach = canExecute === 'attach',
          shapes = context.shapes;

      if (!canExecute) {
        return false;
      }

      // ensure we have actual pixel values deltas
      // (important when zoom level was > 1 during move)
      delta.x = round$1(delta.x);
      delta.y = round$1(delta.y);

      modeling.moveElements(shapes, delta, context.target, {
        primaryShape: context.shape,
        attach: isAttach
      });
    });


    // move activation

    eventBus.on('element.mousedown', function(event) {

      var originalEvent = getOriginal(event);

      if (!originalEvent) {
        throw new Error('must supply DOM mousedown event');
      }

      return start(originalEvent, event.element);
    });


    function start(event, element, activate) {

      // do not move connections or the root element
      if (element.waypoints || !element.parent) {
        return;
      }

      var referencePoint = mid(element);

      dragging.init(event, referencePoint, 'shape.move', {
        cursor: 'grabbing',
        autoActivate: activate,
        data: {
          shape: element,
          context: {}
        }
      });

      // we've handled the event
      return true;
    }

    // API

    this.start = start;
  }

  MoveEvents.$inject = [
    'eventBus',
    'dragging',
    'modeling',
    'selection',
    'rules'
  ];


  /**
   * Return a filtered list of elements that do not contain
   * those nested into others.
   *
   * @param  {Array<djs.model.Base>} elements
   *
   * @return {Array<djs.model.Base>} filtered
   */
  function removeNested(elements) {

    var ids = groupBy(elements, 'id');

    return filter(elements, function(element) {
      while ((element = element.parent)) {

        // parent in selection
        if (ids[element.id]) {
          return false;
        }
      }

      return true;
    });
  }

  /**
   * @param {<SVGElement>} element
   * @param {Number} x
   * @param {Number} y
   * @param {Number} angle
   * @param {Number} amount
   */
  function transform$1(gfx, x, y, angle, amount) {
    var translate = createTransform();
    translate.setTranslate(x, y);

    var rotate = createTransform();
    rotate.setRotate(angle, 0, 0);

    var scale = createTransform();
    scale.setScale(amount || 1, amount || 1);

    transform(gfx, [ translate, rotate, scale ]);
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
   * @param {SVGElement} element
   * @param {Number} angle
   */
  function rotate(gfx, angle) {
    var rotate = createTransform();
    rotate.setRotate(angle, 0, 0);

    transform(gfx, rotate);
  }

  var LOW_PRIORITY$3 = 499;

  var MARKER_DRAGGING = 'djs-dragging',
      MARKER_OK = 'drop-ok',
      MARKER_NOT_OK = 'drop-not-ok',
      MARKER_NEW_PARENT = 'new-parent',
      MARKER_ATTACH = 'attach-ok';


  /**
   * Provides previews for moving shapes when moving.
   *
   * @param {EventBus} eventBus
   * @param {ElementRegistry} elementRegistry
   * @param {Canvas} canvas
   * @param {Styles} styles
   */
  function MovePreview(
      eventBus, elementRegistry, canvas,
      styles, previewSupport) {

    function getVisualDragShapes(shapes) {
      var elements = getAllDraggedElements(shapes);

      var filteredElements = removeEdges(elements);

      return filteredElements;
    }

    function getAllDraggedElements(shapes) {
      var allShapes = selfAndAllChildren(shapes, true);

      var allConnections = map(allShapes, function(shape) {
        return (shape.incoming || []).concat(shape.outgoing || []);
      });

      return flatten(allShapes.concat(allConnections));
    }

    /**
     * Sets drop marker on an element.
     */
    function setMarker(element, marker) {

      [ MARKER_ATTACH, MARKER_OK, MARKER_NOT_OK, MARKER_NEW_PARENT ].forEach(function(m) {

        if (m === marker) {
          canvas.addMarker(element, m);
        } else {
          canvas.removeMarker(element, m);
        }
      });
    }

    /**
     * Make an element draggable.
     *
     * @param {Object} context
     * @param {djs.model.Base} element
     * @param {Boolean} addMarker
     */
    function makeDraggable(context, element, addMarker) {

      previewSupport.addDragger(element, context.dragGroup);

      if (addMarker) {
        canvas.addMarker(element, MARKER_DRAGGING);
      }

      if (context.allDraggedElements) {
        context.allDraggedElements.push(element);
      } else {
        context.allDraggedElements = [ element ];
      }
    }

    // assign a low priority to this handler
    // to let others modify the move context before
    // we draw things
    eventBus.on('shape.move.start', LOW_PRIORITY$3, function(event) {

      var context = event.context,
          dragShapes = context.shapes,
          allDraggedElements = context.allDraggedElements;

      var visuallyDraggedShapes = getVisualDragShapes(dragShapes);

      if (!context.dragGroup) {
        var dragGroup = create('g');
        attr$1(dragGroup, styles.cls('djs-drag-group', [ 'no-events' ]));

        var defaultLayer = canvas.getDefaultLayer();

        append(defaultLayer, dragGroup);

        context.dragGroup = dragGroup;
      }

      // add previews
      visuallyDraggedShapes.forEach(function(shape) {
        previewSupport.addDragger(shape, context.dragGroup);
      });

      // cache all dragged elements / gfx
      // so that we can quickly undo their state changes later
      if (!allDraggedElements) {
        allDraggedElements = getAllDraggedElements(dragShapes);
      } else {
        allDraggedElements = flatten([
          allDraggedElements,
          getAllDraggedElements(dragShapes)
        ]);
      }

      // add dragging marker
      forEach(allDraggedElements, function(e) {
        canvas.addMarker(e, MARKER_DRAGGING);
      });

      context.allDraggedElements = allDraggedElements;

      // determine, if any of the dragged elements have different parents
      context.differentParents = haveDifferentParents(dragShapes);
    });

    // update previews
    eventBus.on('shape.move.move', LOW_PRIORITY$3, function(event) {

      var context = event.context,
          dragGroup = context.dragGroup,
          target = context.target,
          parent = context.shape.parent,
          canExecute = context.canExecute;

      if (target) {
        if (canExecute === 'attach') {
          setMarker(target, MARKER_ATTACH);
        } else if (context.canExecute && target && target.id !== parent.id) {
          setMarker(target, MARKER_NEW_PARENT);
        } else {
          setMarker(target, context.canExecute ? MARKER_OK : MARKER_NOT_OK);
        }
      }

      translate(dragGroup, event.dx, event.dy);
    });

    eventBus.on([ 'shape.move.out', 'shape.move.cleanup' ], function(event) {
      var context = event.context,
          target = context.target;

      if (target) {
        setMarker(target, null);
      }
    });

    // remove previews
    eventBus.on('shape.move.cleanup', function(event) {

      var context = event.context,
          allDraggedElements = context.allDraggedElements,
          dragGroup = context.dragGroup;


      // remove dragging marker
      forEach(allDraggedElements, function(e) {
        canvas.removeMarker(e, MARKER_DRAGGING);
      });

      if (dragGroup) {
        clear$1(dragGroup);
      }
    });


    // API //////////////////////

    /**
     * Make an element draggable.
     *
     * @param {Object} context
     * @param {djs.model.Base} element
     * @param {Boolean} addMarker
     */
    this.makeDraggable = makeDraggable;
  }

  MovePreview.$inject = [
    'eventBus',
    'elementRegistry',
    'canvas',
    'styles',
    'previewSupport'
  ];


  // helpers //////////////////////

  /**
   * returns elements minus all connections
   * where source or target is not elements
   */
  function removeEdges(elements) {

    var filteredElements = filter(elements, function(element) {

      if (!isConnection$1(element)) {
        return true;
      } else {

        return (
          find(elements, matchPattern({ id: element.source.id })) &&
          find(elements, matchPattern({ id: element.target.id }))
        );
      }
    });

    return filteredElements;
  }

  function haveDifferentParents(elements) {
    return size(groupBy(elements, function(e) { return e.parent && e.parent.id; })) !== 1;
  }

  /**
   * Checks if an element is a connection.
   */
  function isConnection$1(element) {
    return element.waypoints;
  }

  var MoveModule = {
    __depends__: [
      InteractionEventsModule,
      SelectionModule,
      OutlineModule,
      RulesModule,
      DraggingModule,
      PreviewSupportModule
    ],
    __init__: [
      'move',
      'movePreview'
    ],
    move: [ 'type', MoveEvents ],
    movePreview: [ 'type', MovePreview ]
  };

  var LOW_PRIORITY$4 = 250;

  /**
   * The tool manager acts as middle-man between the available tool's and the Palette,
   * it takes care of making sure that the correct active state is set.
   *
   * @param  {Object}    eventBus
   * @param  {Object}    dragging
   */
  function ToolManager(eventBus, dragging) {
    this._eventBus = eventBus;
    this._dragging = dragging;

    this._tools = [];
    this._active = null;
  }

  ToolManager.$inject = [ 'eventBus', 'dragging' ];

  ToolManager.prototype.registerTool = function(name, events) {
    var tools = this._tools;

    if (!events) {
      throw new Error('A tool has to be registered with it\'s "events"');
    }

    tools.push(name);

    this.bindEvents(name, events);
  };

  ToolManager.prototype.isActive = function(tool) {
    return tool && this._active === tool;
  };

  ToolManager.prototype.length = function(tool) {
    return this._tools.length;
  };

  ToolManager.prototype.setActive = function(tool) {
    var eventBus = this._eventBus;

    if (this._active !== tool) {
      this._active = tool;

      eventBus.fire('tool-manager.update', { tool: tool });
    }
  };

  ToolManager.prototype.bindEvents = function(name, events) {
    var eventBus = this._eventBus,
        dragging = this._dragging;

    var eventsToRegister = [];

    eventBus.on(events.tool + '.init', function(event) {
      var context = event.context;

      // Active tools that want to reactivate themselves must do this explicitly
      if (!context.reactivate && this.isActive(name)) {
        this.setActive(null);

        dragging.cancel();
        return;
      }

      this.setActive(name);

    }, this);

    // [ricardo]: add test cases
    forEach(events, function(event) {
      eventsToRegister.push(event + '.ended');
      eventsToRegister.push(event + '.canceled');
    });

    eventBus.on(eventsToRegister, LOW_PRIORITY$4, function(event) {
      var originalEvent = event.originalEvent;

      // We defer the de-activation of the tool to the .activate phase,
      // so we're able to check if we want to toggle off the current
      // active tool or switch to a new one
      if (!this._active) {
        return;
      }

      if (originalEvent && closest(originalEvent.target, '.group[data-group="tools"]')) {
        return;
      }

      this.setActive(null);
    }, this);
  };

  var ToolManagerModule = {
    __depends__: [
      DraggingModule
    ],
    __init__: [ 'toolManager' ],
    toolManager: [ 'type', ToolManager ]
  };

  var LASSO_TOOL_CURSOR = 'crosshair';


  function LassoTool(
      eventBus, canvas, dragging,
      elementRegistry, selection, toolManager) {

    this._selection = selection;
    this._dragging = dragging;

    var self = this;

    // lasso visuals implementation

    /**
    * A helper that realizes the selection box visual
    */
    var visuals = {

      create: function(context) {
        var container = canvas.getDefaultLayer(),
            frame;

        frame = context.frame = create('rect');
        attr$1(frame, {
          class: 'djs-lasso-overlay',
          width:  1,
          height: 1,
          x: 0,
          y: 0
        });

        append(container, frame);
      },

      update: function(context) {
        var frame = context.frame,
            bbox = context.bbox;

        attr$1(frame, {
          x: bbox.x,
          y: bbox.y,
          width: bbox.width,
          height: bbox.height
        });
      },

      remove: function(context) {

        if (context.frame) {
          remove$1(context.frame);
        }
      }
    };

    toolManager.registerTool('lasso', {
      tool: 'lasso.selection',
      dragging: 'lasso'
    });

    eventBus.on('lasso.selection.end', function(event) {
      var target = event.originalEvent.target;

      // only reactive on diagram click
      // on some occasions, event.hover is not set and we have to check if the target is an svg
      if (!event.hover && !(target instanceof SVGElement)) {
        return;
      }

      eventBus.once('lasso.selection.ended', function() {
        self.activateLasso(event.originalEvent, true);
      });
    });

    // lasso interaction implementation

    eventBus.on('lasso.end', function(event) {

      var bbox = toBBox(event);

      var elements = elementRegistry.filter(function(element) {
        return element;
      });

      self.select(elements, bbox);
    });

    eventBus.on('lasso.start', function(event) {

      var context = event.context;

      context.bbox = toBBox(event);
      visuals.create(context);
    });

    eventBus.on('lasso.move', function(event) {

      var context = event.context;

      context.bbox = toBBox(event);
      visuals.update(context);
    });

    eventBus.on('lasso.cleanup', function(event) {

      var context = event.context;

      visuals.remove(context);
    });


    // event integration

    eventBus.on('element.mousedown', 1500, function(event) {

      if (hasSecondaryModifier(event)) {
        self.activateLasso(event.originalEvent);

        // we've handled the event
        return true;
      }
    });
  }

  LassoTool.$inject = [
    'eventBus',
    'canvas',
    'dragging',
    'elementRegistry',
    'selection',
    'toolManager'
  ];


  LassoTool.prototype.activateLasso = function(event, autoActivate) {

    this._dragging.init(event, 'lasso', {
      autoActivate: autoActivate,
      cursor: LASSO_TOOL_CURSOR,
      data: {
        context: {}
      }
    });
  };

  LassoTool.prototype.activateSelection = function(event) {

    this._dragging.init(event, 'lasso.selection', {
      trapClick: false,
      cursor: LASSO_TOOL_CURSOR,
      data: {
        context: {}
      }
    });
  };

  LassoTool.prototype.select = function(elements, bbox) {
    var selectedElements = getEnclosedElements(elements, bbox);

    this._selection.select(values(selectedElements));
  };

  LassoTool.prototype.toggle = function() {
    if (this.isActive()) {
      this._dragging.cancel();
    } else {
      this.activateSelection();
    }
  };

  LassoTool.prototype.isActive = function() {
    var context = this._dragging.context();

    return context && /^lasso/.test(context.prefix);
  };



  function toBBox(event) {

    var start = {

      x: event.x - event.dx,
      y: event.y - event.dy
    };

    var end = {
      x: event.x,
      y: event.y
    };

    var bbox;

    if ((start.x <= end.x && start.y < end.y) ||
        (start.x < end.x && start.y <= end.y)) {

      bbox = {
        x: start.x,
        y: start.y,
        width:  end.x - start.x,
        height: end.y - start.y
      };
    } else if ((start.x >= end.x && start.y < end.y) ||
               (start.x > end.x && start.y <= end.y)) {

      bbox = {
        x: end.x,
        y: start.y,
        width:  start.x - end.x,
        height: end.y - start.y
      };
    } else if ((start.x <= end.x && start.y > end.y) ||
               (start.x < end.x && start.y >= end.y)) {

      bbox = {
        x: start.x,
        y: end.y,
        width:  end.x - start.x,
        height: start.y - end.y
      };
    } else if ((start.x >= end.x && start.y > end.y) ||
               (start.x > end.x && start.y >= end.y)) {

      bbox = {
        x: end.x,
        y: end.y,
        width:  start.x - end.x,
        height: start.y - end.y
      };
    } else {

      bbox = {
        x: end.x,
        y: end.y,
        width:  0,
        height: 0
      };
    }
    return bbox;
  }

  var LassoToolModule = {
    __depends__: [
      ToolManagerModule
    ],
    __init__: [ 'lassoTool' ],
    lassoTool: [ 'type', LassoTool ]
  };

  var TOGGLE_SELECTOR = '.djs-palette-toggle',
      ENTRY_SELECTOR = '.entry',
      ELEMENT_SELECTOR = TOGGLE_SELECTOR + ', ' + ENTRY_SELECTOR;

  var PALETTE_OPEN_CLS = 'open',
      PALETTE_TWO_COLUMN_CLS = 'two-column';


  /**
   * A palette containing modeling elements.
   */
  function Palette(eventBus, canvas) {

    this._eventBus = eventBus;
    this._canvas = canvas;

    this._providers = [];

    var self = this;

    eventBus.on('tool-manager.update', function(event$$1) {
      var tool = event$$1.tool;

      self.updateToolHighlight(tool);
    });

    eventBus.on('i18n.changed', function() {
      self._update();
    });

    eventBus.on('diagram.init', function() {

      self._diagramInitialized = true;

      // initialize + update once diagram is ready
      if (self._providers.length) {
        self._init();

        self._update();
      }
    });
  }

  Palette.$inject = [ 'eventBus', 'canvas' ];


  /**
   * Register a provider with the palette
   *
   * @param  {PaletteProvider} provider
   */
  Palette.prototype.registerProvider = function(provider) {
    this._providers.push(provider);

    // postpone init / update until diagram is initialized
    if (!this._diagramInitialized) {
      return;
    }

    if (!this._container) {
      this._init();
    }

    this._update();
  };


  /**
   * Returns the palette entries for a given element
   *
   * @return {Array<PaletteEntryDescriptor>} list of entries
   */
  Palette.prototype.getEntries = function() {

    var entries = {};

    // loop through all providers and their entries.
    // group entries by id so that overriding an entry is possible
    forEach(this._providers, function(provider) {
      var e = provider.getPaletteEntries();

      forEach(e, function(entry, id) {
        entries[id] = entry;
      });
    });

    return entries;
  };


  /**
   * Initialize
   */
  Palette.prototype._init = function() {
    var canvas = this._canvas,
        eventBus = this._eventBus;

    var parent = canvas.getContainer(),
        container = this._container = domify(Palette.HTML_MARKUP),
        self = this;

    parent.appendChild(container);

    delegateEvents.bind(container, ELEMENT_SELECTOR, 'click', function(event$$1) {

      var target = event$$1.delegateTarget;

      if (matchesSelector$1(target, TOGGLE_SELECTOR)) {
        return self.toggle();
      }

      self.trigger('click', event$$1);
    });

    // prevent drag propagation
    componentEvent.bind(container, 'mousedown', function(event$$1) {
      event$$1.stopPropagation();
    });

    // prevent drag propagation
    delegateEvents.bind(container, ENTRY_SELECTOR, 'dragstart', function(event$$1) {
      self.trigger('dragstart', event$$1);
    });

    eventBus.on('canvas.resized', this._layoutChanged, this);

    eventBus.fire('palette.create', {
      container: container
    });
  };

  /**
   * Update palette state.
   *
   * @param  {Object} [state] { open, twoColumn }
   */
  Palette.prototype._toggleState = function(state) {

    state = state || {};

    var parent = this._getParentContainer(),
        container = this._container;

    var eventBus = this._eventBus;

    var twoColumn;

    var cls = classes(container);

    if ('twoColumn' in state) {
      twoColumn = state.twoColumn;
    } else {
      twoColumn = this._needsCollapse(parent.clientHeight, this._entries || {});
    }

    // always update two column
    cls.toggle(PALETTE_TWO_COLUMN_CLS, twoColumn);

    if ('open' in state) {
      cls.toggle(PALETTE_OPEN_CLS, state.open);
    }

    eventBus.fire('palette.changed', {
      twoColumn: twoColumn,
      open: this.isOpen()
    });
  };

  Palette.prototype._update = function() {

    var entriesContainer = query('.djs-palette-entries', this._container),
        entries = this._entries = this.getEntries();

    clear(entriesContainer);

    forEach(entries, function(entry, id) {

      var grouping = entry.group || 'default';

      var container = query('[data-group=' + grouping + ']', entriesContainer);
      if (!container) {
        container = domify('<div class="group" data-group="' + grouping + '"></div>');
        entriesContainer.appendChild(container);
      }

      var html = entry.html || (
        entry.separator ?
          '<hr class="separator" />' :
          '<div class="entry" draggable="true"></div>');


      var control = domify(html);
      container.appendChild(control);

      if (!entry.separator) {
        attr(control, 'data-action', id);

        if (entry.title) {
          attr(control, 'title', entry.title);
        }

        if (entry.className) {
          addClasses(control, entry.className);
        }

        if (entry.imageUrl) {
          control.appendChild(domify('<img src="' + entry.imageUrl + '">'));
        }
      }
    });

    // open after update
    this.open();
  };


  /**
   * Trigger an action available on the palette
   *
   * @param  {String} action
   * @param  {Event} event
   */
  Palette.prototype.trigger = function(action, event$$1, autoActivate) {
    var entries = this._entries,
        entry,
        handler,
        originalEvent,
        button = event$$1.delegateTarget || event$$1.target;

    if (!button) {
      return event$$1.preventDefault();
    }

    entry = entries[attr(button, 'data-action')];

    // when user clicks on the palette and not on an action
    if (!entry) {
      return;
    }

    handler = entry.action;

    originalEvent = event$$1.originalEvent || event$$1;

    // simple action (via callback function)
    if (isFunction(handler)) {
      if (action === 'click') {
        handler(originalEvent, autoActivate);
      }
    } else {
      if (handler[action]) {
        handler[action](originalEvent, autoActivate);
      }
    }

    // silence other actions
    event$$1.preventDefault();
  };

  Palette.prototype._layoutChanged = function() {
    this._toggleState({});
  };

  /**
   * Do we need to collapse to two columns?
   *
   * @param {Number} availableHeight
   * @param {Object} entries
   *
   * @return {Boolean}
   */
  Palette.prototype._needsCollapse = function(availableHeight, entries) {

    // top margin + bottom toggle + bottom margin
    // implementors must override this method if they
    // change the palette styles
    var margin = 20 + 10 + 20;

    var entriesHeight = Object.keys(entries).length * 46;

    return availableHeight < entriesHeight + margin;
  };

  /**
   * Close the palette
   */
  Palette.prototype.close = function() {

    this._toggleState({
      open: false,
      twoColumn: false
    });
  };


  /**
   * Open the palette
   */
  Palette.prototype.open = function() {
    this._toggleState({ open: true });
  };


  Palette.prototype.toggle = function(open) {
    if (this.isOpen()) {
      this.close();
    } else {
      this.open();
    }
  };

  Palette.prototype.isActiveTool = function(tool) {
    return tool && this._activeTool === tool;
  };

  Palette.prototype.updateToolHighlight = function(name) {
    var entriesContainer,
        toolsContainer;

    if (!this._toolsContainer) {
      entriesContainer = query('.djs-palette-entries', this._container);

      this._toolsContainer = query('[data-group=tools]', entriesContainer);
    }

    toolsContainer = this._toolsContainer;

    forEach(toolsContainer.children, function(tool) {
      var actionName = tool.getAttribute('data-action');

      if (!actionName) {
        return;
      }

      var toolClasses = classes(tool);

      actionName = actionName.replace('-tool', '');

      if (toolClasses.contains('entry') && actionName === name) {
        toolClasses.add('highlighted-entry');
      } else {
        toolClasses.remove('highlighted-entry');
      }
    });
  };


  /**
   * Return true if the palette is opened.
   *
   * @example
   *
   * palette.open();
   *
   * if (palette.isOpen()) {
   *   // yes, we are open
   * }
   *
   * @return {boolean} true if palette is opened
   */
  Palette.prototype.isOpen = function() {
    return classes(this._container).has(PALETTE_OPEN_CLS);
  };

  /**
   * Get container the palette lives in.
   *
   * @return {Element}
   */
  Palette.prototype._getParentContainer = function() {
    return this._canvas.getContainer();
  };


  /* markup definition */

  Palette.HTML_MARKUP =
    '<div class="djs-palette">' +
      '<div class="djs-palette-entries"></div>' +
      '<div class="djs-palette-toggle"></div>' +
    '</div>';


  // helpers //////////////////////

  function addClasses(element, classNames) {

    var classes$$1 = classes(element);

    var actualClassNames = isArray(classNames) ? classNames : classNames.split(/\s+/g);
    actualClassNames.forEach(function(cls) {
      classes$$1.add(cls);
    });
  }

  var PaletteModule = {
    __init__: [ 'palette' ],
    palette: [ 'type', Palette ]
  };

  var LOW_PRIORITY$5 = 750;

  var MARKER_OK$1 = 'drop-ok',
      MARKER_NOT_OK$1 = 'drop-not-ok',
      MARKER_ATTACH$1 = 'attach-ok',
      MARKER_NEW_PARENT$1 = 'new-parent';


  /**
   * Adds the ability to create new shapes via drag and drop.
   *
   * Create must be activated via {@link Create#start}. From that
   * point on, create will invoke `shape.create` and `shape.attach`
   * rules to query whether or not creation or attachment on a certain
   * position is allowed.
   *
   * If create or attach is allowed and a source is given, Create it
   * will invoke `connection.create` rules to query whether a connection
   * can be drawn between source and new shape. During rule evaluation
   * the target is not attached yet, however
   *
   *   hints = { targetParent, targetAttach }
   *
   * are passed to the evaluating rules.
   *
   *
   * ## Rule Return Values
   *
   * Return values interpreted from  `shape.create`:
   *
   *   * `true`: create is allowed
   *   * `false`: create is disallowed
   *   * `null`: create is not allowed but should be ignored visually
   *
   * Return values interpreted from `shape.attach`:
   *
   *   * `true`: attach is allowed
   *   * `Any`: attach is allowed with the constraints
   *   * `false`: attach is disallowed
   *
   * Return values interpreted from `connection.create`:
   *
   *   * `true`: connection can be created
   *   * `Any`: connection with the given attributes can be created
   *   * `false`: connection can't be created
   *
   *
   * @param {EventBus} eventBus
   * @param {Dragging} dragging
   * @param {Rules} rules
   * @param {Modeling} modeling
   * @param {Canvas} canvas
   * @param {Styles} styles
   * @param {GraphicsFactory} graphicsFactory
   */
  function Create(
      eventBus, dragging, rules, modeling,
      canvas, styles, graphicsFactory) {

    // rules

    function canCreate(shape, target, source, position) {

      if (!target) {
        return false;
      }

      var ctx = {
        source: source,
        shape: shape,
        target: target,
        position: position
      };

      var create$$1,
          attach,
          connect;

      attach = rules.allowed('shape.attach', ctx);

      if (!attach) {
        create$$1 = rules.allowed('shape.create', ctx);
      }

      if (create$$1 || attach) {

        connect = source && rules.allowed('connection.create', {
          source: source,
          target: shape,
          hints: {
            targetParent: target,
            targetAttach: attach
          }
        });
      }

      if (create$$1 || attach) {
        return {
          attach: attach,
          connect: connect
        };
      }

      return false;
    }


    /** set drop marker on an element */
    function setMarker(element, marker) {

      [ MARKER_ATTACH$1, MARKER_OK$1, MARKER_NOT_OK$1, MARKER_NEW_PARENT$1 ].forEach(function(m) {

        if (m === marker) {
          canvas.addMarker(element, m);
        } else {
          canvas.removeMarker(element, m);
        }
      });
    }


    // visual helpers

    function createVisual(shape) {
      var group, preview, visual;

      group = create('g');
      attr$1(group, styles.cls('djs-drag-group', [ 'no-events' ]));

      append(canvas.getDefaultLayer(), group);

      preview = create('g');
      classes$1(preview).add('djs-dragger');

      append(group, preview);

      translate(preview, shape.width / -2, shape.height / -2);

      var visualGroup = create('g');
      classes$1(visualGroup).add('djs-visual');

      append(preview, visualGroup);

      visual = visualGroup;

      // hijack renderer to draw preview
      graphicsFactory.drawShape(visual, shape);

      return group;
    }


    // event handlers

    eventBus.on('create.move', function(event) {

      var context = event.context,
          hover = event.hover,
          shape = context.shape,
          source = context.source,
          canExecute;

      var position = {
        x: event.x,
        y: event.y
      };

      canExecute = context.canExecute = hover && canCreate(shape, hover, source, position);

      // ignore hover visually if canExecute is null
      if (hover && canExecute !== null) {
        context.target = hover;

        if (canExecute && canExecute.attach) {
          setMarker(hover, MARKER_ATTACH$1);
        } else {
          setMarker(hover, canExecute ? MARKER_NEW_PARENT$1 : MARKER_NOT_OK$1);
        }
      }
    });

    eventBus.on('create.move', LOW_PRIORITY$5, function(event) {

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

    eventBus.on('create.end', function(event) {
      var context = event.context,
          source = context.source,
          shape = context.shape,
          target = context.target,
          canExecute = context.canExecute,
          attach = canExecute && canExecute.attach,
          connect = canExecute && canExecute.connect,
          position = {
            x: event.x,
            y: event.y
          };

      if (!canExecute) {
        return false;
      }

      if (connect) {
        // invoke append if connect is set via rules
        shape = modeling.appendShape(source, shape, position, target, {
          attach: attach,
          connection: connect === true ? {} : connect
        });
      } else {
        // invoke create, if connect is not set
        shape = modeling.createShape(shape, position, target, {
          attach: attach
        });
      }

      // make sure we provide the actual attached
      // shape with the context so that selection and
      // other components can use it right after the create
      // operation ends
      context.shape = shape;
    });


    eventBus.on('create.cleanup', function(event) {
      var context = event.context;

      if (context.visual) {
        remove$1(context.visual);
      }
    });

    // API

    this.start = function(event, shape, source) {

      dragging.init(event, 'create', {
        cursor: 'grabbing',
        autoActivate: true,
        data: {
          shape: shape,
          context: {
            shape: shape,
            source: source
          }
        }
      });
    };
  }

  Create.$inject = [
    'eventBus',
    'dragging',
    'rules',
    'modeling',
    'canvas',
    'styles',
    'graphicsFactory'
  ];

  var CreateModule = {
    __depends__: [
      DraggingModule,
      SelectionModule,
      RulesModule
    ],
    create: [ 'type', Create ]
  };

  /**
   * Util that provides unique IDs.
   *
   * @class djs.util.IdGenerator
   * @constructor
   * @memberOf djs.util
   *
   * The ids can be customized via a given prefix and contain a random value to avoid collisions.
   *
   * @param {String} prefix a prefix to prepend to generated ids (for better readability)
   */
  function IdGenerator(prefix) {

    this._counter = 0;
    this._prefix = (prefix ? prefix + '-' : '') + Math.floor(Math.random() * 1000000000) + '-';
  }

  /**
   * Returns a next unique ID.
   *
   * @method djs.util.IdGenerator#next
   *
   * @returns {String} the id
   */
  IdGenerator.prototype.next = function() {
    return this._prefix + (++this._counter);
  };

  // document wide unique overlay ids
  var ids = new IdGenerator('ov');

  var LOW_PRIORITY$6 = 500;


  /**
   * A service that allows users to attach overlays to diagram elements.
   *
   * The overlay service will take care of overlay positioning during updates.
   *
   * @example
   *
   * // add a pink badge on the top left of the shape
   * overlays.add(someShape, {
   *   position: {
   *     top: -5,
   *     left: -5
   *   },
   *   html: '<div style="width: 10px; background: fuchsia; color: white;">0</div>'
   * });
   *
   * // or add via shape id
   *
   * overlays.add('some-element-id', {
   *   position: {
   *     top: -5,
   *     left: -5
   *   }
   *   html: '<div style="width: 10px; background: fuchsia; color: white;">0</div>'
   * });
   *
   * // or add with optional type
   *
   * overlays.add(someShape, 'badge', {
   *   position: {
   *     top: -5,
   *     left: -5
   *   }
   *   html: '<div style="width: 10px; background: fuchsia; color: white;">0</div>'
   * });
   *
   *
   * // remove an overlay
   *
   * var id = overlays.add(...);
   * overlays.remove(id);
   *
   *
   * You may configure overlay defaults during tool by providing a `config` module
   * with `overlays.defaults` as an entry:
   *
   * {
   *   overlays: {
   *     defaults: {
   *       show: {
   *         minZoom: 0.7,
   *         maxZoom: 5.0
   *       },
   *       scale: {
   *         min: 1
   *       }
   *     }
   * }
   *
   * @param {Object} config
   * @param {EventBus} eventBus
   * @param {Canvas} canvas
   * @param {ElementRegistry} elementRegistry
   */
  function Overlays(config, eventBus, canvas, elementRegistry) {

    this._eventBus = eventBus;
    this._canvas = canvas;
    this._elementRegistry = elementRegistry;

    this._ids = ids;

    this._overlayDefaults = assign({
      // no show constraints
      show: null,

      // always scale
      scale: true
    }, config && config.defaults);

    /**
     * Mapping overlayId -> overlay
     */
    this._overlays = {};

    /**
     * Mapping elementId -> overlay container
     */
    this._overlayContainers = [];

    // root html element for all overlays
    this._overlayRoot = createRoot(canvas.getContainer());

    this._init();
  }


  Overlays.$inject = [
    'config.overlays',
    'eventBus',
    'canvas',
    'elementRegistry'
  ];


  /**
   * Returns the overlay with the specified id or a list of overlays
   * for an element with a given type.
   *
   * @example
   *
   * // return the single overlay with the given id
   * overlays.get('some-id');
   *
   * // return all overlays for the shape
   * overlays.get({ element: someShape });
   *
   * // return all overlays on shape with type 'badge'
   * overlays.get({ element: someShape, type: 'badge' });
   *
   * // shape can also be specified as id
   * overlays.get({ element: 'element-id', type: 'badge' });
   *
   *
   * @param {Object} search
   * @param {String} [search.id]
   * @param {String|djs.model.Base} [search.element]
   * @param {String} [search.type]
   *
   * @return {Object|Array<Object>} the overlay(s)
   */
  Overlays.prototype.get = function(search) {

    if (isString(search)) {
      search = { id: search };
    }

    if (isString(search.element)) {
      search.element = this._elementRegistry.get(search.element);
    }

    if (search.element) {
      var container = this._getOverlayContainer(search.element, true);

      // return a list of overlays when searching by element (+type)
      if (container) {
        return search.type ? filter(container.overlays, matchPattern({ type: search.type })) : container.overlays.slice();
      } else {
        return [];
      }
    } else
    if (search.type) {
      return filter(this._overlays, matchPattern({ type: search.type }));
    } else {
      // return single element when searching by id
      return search.id ? this._overlays[search.id] : null;
    }
  };

  /**
   * Adds a HTML overlay to an element.
   *
   * @param {String|djs.model.Base}   element   attach overlay to this shape
   * @param {String}                  [type]    optional type to assign to the overlay
   * @param {Object}                  overlay   the overlay configuration
   *
   * @param {String|DOMElement}       overlay.html                 html element to use as an overlay
   * @param {Object}                  [overlay.show]               show configuration
   * @param {Number}                  [overlay.show.minZoom]       minimal zoom level to show the overlay
   * @param {Number}                  [overlay.show.maxZoom]       maximum zoom level to show the overlay
   * @param {Object}                  overlay.position             where to attach the overlay
   * @param {Number}                  [overlay.position.left]      relative to element bbox left attachment
   * @param {Number}                  [overlay.position.top]       relative to element bbox top attachment
   * @param {Number}                  [overlay.position.bottom]    relative to element bbox bottom attachment
   * @param {Number}                  [overlay.position.right]     relative to element bbox right attachment
   * @param {Boolean|Object}          [overlay.scale=true]         false to preserve the same size regardless of
   *                                                               diagram zoom
   * @param {Number}                  [overlay.scale.min]
   * @param {Number}                  [overlay.scale.max]
   *
   * @return {String}                 id that may be used to reference the overlay for update or removal
   */
  Overlays.prototype.add = function(element, type, overlay) {

    if (isObject(type)) {
      overlay = type;
      type = null;
    }

    if (!element.id) {
      element = this._elementRegistry.get(element);
    }

    if (!overlay.position) {
      throw new Error('must specifiy overlay position');
    }

    if (!overlay.html) {
      throw new Error('must specifiy overlay html');
    }

    if (!element) {
      throw new Error('invalid element specified');
    }

    var id = this._ids.next();

    overlay = assign({}, this._overlayDefaults, overlay, {
      id: id,
      type: type,
      element: element,
      html: overlay.html
    });

    this._addOverlay(overlay);

    return id;
  };


  /**
   * Remove an overlay with the given id or all overlays matching the given filter.
   *
   * @see Overlays#get for filter options.
   *
   * @param {String} [id]
   * @param {Object} [filter]
   */
  Overlays.prototype.remove = function(filter$$1) {

    var overlays = this.get(filter$$1) || [];

    if (!isArray(overlays)) {
      overlays = [ overlays ];
    }

    var self = this;

    forEach(overlays, function(overlay) {

      var container = self._getOverlayContainer(overlay.element, true);

      if (overlay) {
        remove(overlay.html);
        remove(overlay.htmlContainer);

        delete overlay.htmlContainer;
        delete overlay.element;

        delete self._overlays[overlay.id];
      }

      if (container) {
        var idx = container.overlays.indexOf(overlay);
        if (idx !== -1) {
          container.overlays.splice(idx, 1);
        }
      }
    });

  };


  Overlays.prototype.show = function() {
    setVisible(this._overlayRoot);
  };


  Overlays.prototype.hide = function() {
    setVisible(this._overlayRoot, false);
  };

  Overlays.prototype.clear = function() {
    this._overlays = {};

    this._overlayContainers = [];

    clear(this._overlayRoot);
  };

  Overlays.prototype._updateOverlayContainer = function(container) {
    var element = container.element,
        html = container.html;

    // update container left,top according to the elements x,y coordinates
    // this ensures we can attach child elements relative to this container

    var x = element.x,
        y = element.y;

    if (element.waypoints) {
      var bbox = getBBox(element);
      x = bbox.x;
      y = bbox.y;
    }

    setPosition(html, x, y);

    attr(container.html, 'data-container-id', element.id);
  };


  Overlays.prototype._updateOverlay = function(overlay) {

    var position = overlay.position,
        htmlContainer = overlay.htmlContainer,
        element = overlay.element;

    // update overlay html relative to shape because
    // it is already positioned on the element

    // update relative
    var left = position.left,
        top = position.top;

    if (position.right !== undefined) {

      var width;

      if (element.waypoints) {
        width = getBBox(element).width;
      } else {
        width = element.width;
      }

      left = position.right * -1 + width;
    }

    if (position.bottom !== undefined) {

      var height;

      if (element.waypoints) {
        height = getBBox(element).height;
      } else {
        height = element.height;
      }

      top = position.bottom * -1 + height;
    }

    setPosition(htmlContainer, left || 0, top || 0);
  };


  Overlays.prototype._createOverlayContainer = function(element) {
    var html = domify('<div class="djs-overlays" style="position: absolute" />');

    this._overlayRoot.appendChild(html);

    var container = {
      html: html,
      element: element,
      overlays: []
    };

    this._updateOverlayContainer(container);

    this._overlayContainers.push(container);

    return container;
  };


  Overlays.prototype._updateRoot = function(viewbox) {
    var scale = viewbox.scale || 1;

    var matrix = 'matrix(' +
    [
      scale,
      0,
      0,
      scale,
      -1 * viewbox.x * scale,
      -1 * viewbox.y * scale
    ].join(',') +
    ')';

    setTransform(this._overlayRoot, matrix);
  };


  Overlays.prototype._getOverlayContainer = function(element, raw) {
    var container = find(this._overlayContainers, function(c) {
      return c.element === element;
    });


    if (!container && !raw) {
      return this._createOverlayContainer(element);
    }

    return container;
  };


  Overlays.prototype._addOverlay = function(overlay) {

    var id = overlay.id,
        element = overlay.element,
        html = overlay.html,
        htmlContainer,
        overlayContainer;

    // unwrap jquery (for those who need it)
    if (html.get && html.constructor.prototype.jquery) {
      html = html.get(0);
    }

    // create proper html elements from
    // overlay HTML strings
    if (isString(html)) {
      html = domify(html);
    }

    overlayContainer = this._getOverlayContainer(element);

    htmlContainer = domify('<div class="djs-overlay" data-overlay-id="' + id + '" style="position: absolute">');

    htmlContainer.appendChild(html);

    if (overlay.type) {
      classes(htmlContainer).add('djs-overlay-' + overlay.type);
    }

    overlay.htmlContainer = htmlContainer;

    overlayContainer.overlays.push(overlay);
    overlayContainer.html.appendChild(htmlContainer);

    this._overlays[id] = overlay;

    this._updateOverlay(overlay);
    this._updateOverlayVisibilty(overlay, this._canvas.viewbox());
  };


  Overlays.prototype._updateOverlayVisibilty = function(overlay, viewbox) {
    var show = overlay.show,
        minZoom = show && show.minZoom,
        maxZoom = show && show.maxZoom,
        htmlContainer = overlay.htmlContainer,
        visible = true;

    if (show) {
      if (
        (isDefined(minZoom) && minZoom > viewbox.scale) ||
        (isDefined(maxZoom) && maxZoom < viewbox.scale)
      ) {
        visible = false;
      }

      setVisible(htmlContainer, visible);
    }

    this._updateOverlayScale(overlay, viewbox);
  };


  Overlays.prototype._updateOverlayScale = function(overlay, viewbox) {
    var shouldScale = overlay.scale,
        minScale,
        maxScale,
        htmlContainer = overlay.htmlContainer;

    var scale, transform = '';

    if (shouldScale !== true) {

      if (shouldScale === false) {
        minScale = 1;
        maxScale = 1;
      } else {
        minScale = shouldScale.min;
        maxScale = shouldScale.max;
      }

      if (isDefined(minScale) && viewbox.scale < minScale) {
        scale = (1 / viewbox.scale || 1) * minScale;
      }

      if (isDefined(maxScale) && viewbox.scale > maxScale) {
        scale = (1 / viewbox.scale || 1) * maxScale;
      }
    }

    if (isDefined(scale)) {
      transform = 'scale(' + scale + ',' + scale + ')';
    }

    setTransform(htmlContainer, transform);
  };


  Overlays.prototype._updateOverlaysVisibilty = function(viewbox) {

    var self = this;

    forEach(this._overlays, function(overlay) {
      self._updateOverlayVisibilty(overlay, viewbox);
    });
  };


  Overlays.prototype._init = function() {

    var eventBus = this._eventBus;

    var self = this;


    // scroll/zoom integration

    function updateViewbox(viewbox) {
      self._updateRoot(viewbox);
      self._updateOverlaysVisibilty(viewbox);

      self.show();
    }

    eventBus.on('canvas.viewbox.changing', function(event) {
      self.hide();
    });

    eventBus.on('canvas.viewbox.changed', function(event) {
      updateViewbox(event.viewbox);
    });


    // remove integration

    eventBus.on([ 'shape.remove', 'connection.remove' ], function(e) {
      var element = e.element;
      var overlays = self.get({ element: element });

      forEach(overlays, function(o) {
        self.remove(o.id);
      });

      var container = self._getOverlayContainer(element);

      if (container) {
        remove(container.html);
        var i = self._overlayContainers.indexOf(container);
        if (i !== -1) {
          self._overlayContainers.splice(i, 1);
        }
      }
    });


    // move integration

    eventBus.on('element.changed', LOW_PRIORITY$6, function(e) {
      var element = e.element;

      var container = self._getOverlayContainer(element, true);

      if (container) {
        forEach(container.overlays, function(overlay) {
          self._updateOverlay(overlay);
        });

        self._updateOverlayContainer(container);
      }
    });


    // marker integration, simply add them on the overlays as classes, too.

    eventBus.on('element.marker.update', function(e) {
      var container = self._getOverlayContainer(e.element, true);
      if (container) {
        classes(container.html)[e.add ? 'add' : 'remove'](e.marker);
      }
    });


    // clear overlays with diagram

    eventBus.on('diagram.clear', this.clear, this);
  };



  // helpers /////////////////////////////

  function createRoot(parentNode) {
    var root = domify(
      '<div class="djs-overlay-container" style="position: absolute; width: 0; height: 0;" />'
    );

    parentNode.insertBefore(root, parentNode.firstChild);

    return root;
  }

  function setPosition(el, x, y) {
    assign(el.style, { left: x + 'px', top: y + 'px' });
  }

  function setVisible(el, visible) {
    el.style.display = visible === false ? 'none' : '';
  }

  function setTransform(el, transform) {

    el.style['transform-origin'] = 'top left';

    [ '', '-ms-', '-webkit-' ].forEach(function(prefix) {
      el.style[prefix + 'transform'] = transform;
    });
  }

  var OverlaysModule = {
    __init__: [ 'overlays' ],
    overlays: [ 'type', Overlays ]
  };

  var entrySelector = '.entry';


  /**
   * A context pad that displays element specific, contextual actions next
   * to a diagram element.
   *
   * @param {Object} config
   * @param {Boolean|Object} [config.scale={ min: 1.0, max: 1.5 }]
   * @param {Number} [config.scale.min]
   * @param {Number} [config.scale.max]
   * @param {EventBus} eventBus
   * @param {Overlays} overlays
   */
  function ContextPad(config, eventBus, overlays) {

    this._providers = [];

    this._eventBus = eventBus;
    this._overlays = overlays;

    var scale = isDefined(config && config.scale) ? config.scale : {
      min: 1,
      max: 1.5
    };

    this._overlaysConfig = {
      position: {
        right: -9,
        top: -6
      },
      scale: scale
    };

    this._current = null;

    this._init();
  }

  ContextPad.$inject = [
    'config.contextPad',
    'eventBus',
    'overlays'
  ];


  /**
   * Registers events needed for interaction with other components
   */
  ContextPad.prototype._init = function() {

    var eventBus = this._eventBus;

    var self = this;

    eventBus.on('selection.changed', function(e) {

      var selection = e.newSelection;

      if (selection.length === 1) {
        self.open(selection[0]);
      } else {
        self.close();
      }
    });

    eventBus.on('elements.delete', function(event$$1) {
      var elements = event$$1.elements;

      forEach(elements, function(e) {
        if (self.isOpen(e)) {
          self.close();
        }
      });
    });

    eventBus.on('element.changed', function(event$$1) {
      var element = event$$1.element,
          current = self._current;

      // force reopen if element for which we are currently opened changed
      if (current && current.element === element) {
        self.open(element, true);
      }
    });
  };


  /**
   * Register a provider with the context pad
   *
   * @param  {ContextPadProvider} provider
   */
  ContextPad.prototype.registerProvider = function(provider) {
    this._providers.push(provider);
  };


  /**
   * Returns the context pad entries for a given element
   *
   * @param {djs.element.Base} element
   *
   * @return {Array<ContextPadEntryDescriptor>} list of entries
   */
  ContextPad.prototype.getEntries = function(element) {
    var entries = {};

    // loop through all providers and their entries.
    // group entries by id so that overriding an entry is possible
    forEach(this._providers, function(provider) {
      var e = provider.getContextPadEntries(element);

      forEach(e, function(entry, id) {
        entries[id] = entry;
      });
    });

    return entries;
  };


  /**
   * Trigger an action available on the opened context pad
   *
   * @param  {String} action
   * @param  {Event} event
   * @param  {Boolean} [autoActivate=false]
   */
  ContextPad.prototype.trigger = function(action, event$$1, autoActivate) {

    var element = this._current.element,
        entries = this._current.entries,
        entry,
        handler,
        originalEvent,
        button = event$$1.delegateTarget || event$$1.target;

    if (!button) {
      return event$$1.preventDefault();
    }

    entry = entries[attr(button, 'data-action')];
    handler = entry.action;

    originalEvent = event$$1.originalEvent || event$$1;

    // simple action (via callback function)
    if (isFunction(handler)) {
      if (action === 'click') {
        return handler(originalEvent, element, autoActivate);
      }
    } else {
      if (handler[action]) {
        return handler[action](originalEvent, element, autoActivate);
      }
    }

    // silence other actions
    event$$1.preventDefault();
  };


  /**
   * Open the context pad for the given element
   *
   * @param {djs.model.Base} element
   * @param {Boolean} force if true, force reopening the context pad
   */
  ContextPad.prototype.open = function(element, force) {
    if (!force && this.isOpen(element)) {
      return;
    }

    this.close();
    this._updateAndOpen(element);
  };


  ContextPad.prototype._updateAndOpen = function(element) {

    var entries = this.getEntries(element),
        pad = this.getPad(element),
        html = pad.html;

    forEach(entries, function(entry, id) {
      var grouping = entry.group || 'default',
          control = domify(entry.html || '<div class="entry" draggable="true"></div>'),
          container;

      attr(control, 'data-action', id);

      container = query('[data-group=' + grouping + ']', html);
      if (!container) {
        container = domify('<div class="group" data-group="' + grouping + '"></div>');
        html.appendChild(container);
      }

      container.appendChild(control);

      if (entry.className) {
        addClasses$1(control, entry.className);
      }

      if (entry.title) {
        attr(control, 'title', entry.title);
      }

      if (entry.imageUrl) {
        control.appendChild(domify('<img src="' + entry.imageUrl + '">'));
      }
    });

    classes(html).add('open');

    this._current = {
      element: element,
      pad: pad,
      entries: entries
    };

    this._eventBus.fire('contextPad.open', { current: this._current });
  };


  ContextPad.prototype.getPad = function(element) {
    if (this.isOpen()) {
      return this._current.pad;
    }

    var self = this;

    var overlays = this._overlays;

    var html = domify('<div class="djs-context-pad"></div>');

    var overlaysConfig = assign({
      html: html
    }, this._overlaysConfig);

    delegateEvents.bind(html, entrySelector, 'click', function(event$$1) {
      self.trigger('click', event$$1);
    });

    delegateEvents.bind(html, entrySelector, 'dragstart', function(event$$1) {
      self.trigger('dragstart', event$$1);
    });

    // stop propagation of mouse events
    componentEvent.bind(html, 'mousedown', function(event$$1) {
      event$$1.stopPropagation();
    });

    this._overlayId = overlays.add(element, 'context-pad', overlaysConfig);

    var pad = overlays.get(this._overlayId);

    this._eventBus.fire('contextPad.create', { element: element, pad: pad });

    return pad;
  };


  /**
   * Close the context pad
   */
  ContextPad.prototype.close = function() {
    if (!this.isOpen()) {
      return;
    }

    this._overlays.remove(this._overlayId);

    this._overlayId = null;

    this._eventBus.fire('contextPad.close', { current: this._current });

    this._current = null;
  };

  /**
   * Check if pad is open. If element is given, will check
   * if pad is opened with given element.
   *
   * @param {Element} element
   * @return {Boolean}
   */
  ContextPad.prototype.isOpen = function(element) {
    return !!this._current && (!element ? true : this._current.element === element);
  };




  // helpers //////////////////////

  function addClasses$1(element, classNames) {

    var classes$$1 = classes(element);

    var actualClassNames = isArray(classNames) ? classNames : classNames.split(/\s+/g);
    actualClassNames.forEach(function(cls) {
      classes$$1.add(cls);
    });
  }

  var ContextPadModule = {
    __depends__: [
      InteractionEventsModule,
      OverlaysModule
    ],
    contextPad: [ 'type', ContextPad ]
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


  var ALIGNED_THRESHOLD = 2;

  /**
   * Returns whether two points are in a horizontal or vertical line.
   *
   * @param {Point} a
   * @param {Point} b
   *
   * @return {String|Boolean} returns false if the points are not
   *                          aligned or 'h|v' if they are aligned
   *                          horizontally / vertically.
   */
  function pointsAligned(a, b) {
    if (Math.abs(a.x - b.x) <= ALIGNED_THRESHOLD) {
      return 'h';
    }

    if (Math.abs(a.y - b.y) <= ALIGNED_THRESHOLD) {
      return 'v';
    }

    return false;
  }


  /**
   * Returns true if the point p is inside the rectangle rect
   *
   * @param  {Point}  p
   * @param  {Rect}   rect
   * @param  {Number} tolerance
   *
   * @return {Boolean}
   */
  function pointInRect(p, rect, tolerance) {
    tolerance = tolerance || 0;

    return p.x > rect.x - tolerance &&
           p.y > rect.y - tolerance &&
           p.x < rect.x + rect.width + tolerance &&
           p.y < rect.y + rect.height + tolerance;
  }

  /**
   * Returns a point in the middle of points p and q
   *
   * @param  {Point}  p
   * @param  {Point}  q
   *
   * @return {Point} middle point
   */
  function getMidPoint(p, q) {
    return {
      x: Math.round(p.x + ((q.x - p.x) / 2.0)),
      y: Math.round(p.y + ((q.y - p.y) / 2.0))
    };
  }

  var BENDPOINT_CLS = 'djs-bendpoint';
  var SEGMENT_DRAGGER_CLS = 'djs-segment-dragger';

  function toCanvasCoordinates(canvas, event) {

    var position = toPoint(event),
        clientRect = canvas._container.getBoundingClientRect(),
        offset;

    // canvas relative position

    offset = {
      x: clientRect.left,
      y: clientRect.top
    };

    // update actual event payload with canvas relative measures

    var viewbox = canvas.viewbox();

    return {
      x: viewbox.x + (position.x - offset.x) / viewbox.scale,
      y: viewbox.y + (position.y - offset.y) / viewbox.scale
    };
  }

  function addBendpoint(parentGfx, cls) {
    var groupGfx = create('g');
    classes$1(groupGfx).add(BENDPOINT_CLS);

    append(parentGfx, groupGfx);

    var visual = create('circle');
    attr$1(visual, {
      cx: 0,
      cy: 0,
      r: 4
    });
    classes$1(visual).add('djs-visual');

    append(groupGfx, visual);

    var hit = create('circle');
    attr$1(hit, {
      cx: 0,
      cy: 0,
      r: 10
    });
    classes$1(hit).add('djs-hit');

    append(groupGfx, hit);

    if (cls) {
      classes$1(groupGfx).add(cls);
    }

    return groupGfx;
  }

  function createParallelDragger(parentGfx, position, alignment) {
    var draggerGfx = create('g');

    append(parentGfx, draggerGfx);

    var width = 14,
        height = 3,
        padding = 6,
        hitWidth = width + padding,
        hitHeight = height + padding;

    var visual = create('rect');
    attr$1(visual, {
      x: -width / 2,
      y: -height / 2,
      width: width,
      height: height
    });
    classes$1(visual).add('djs-visual');

    append(draggerGfx, visual);

    var hit = create('rect');
    attr$1(hit, {
      x: -hitWidth / 2,
      y: -hitHeight / 2,
      width: hitWidth,
      height: hitHeight
    });
    classes$1(hit).add('djs-hit');

    append(draggerGfx, hit);

    rotate(draggerGfx, alignment === 'h' ? 90 : 0, 0, 0);

    return draggerGfx;
  }


  function addSegmentDragger(parentGfx, segmentStart, segmentEnd) {

    var groupGfx = create('g'),
        mid = getMidPoint(segmentStart, segmentEnd),
        alignment = pointsAligned(segmentStart, segmentEnd);

    append(parentGfx, groupGfx);

    createParallelDragger(groupGfx, mid, alignment);

    classes$1(groupGfx).add(SEGMENT_DRAGGER_CLS);
    classes$1(groupGfx).add(alignment === 'h' ? 'vertical' : 'horizontal');

    translate(groupGfx, mid.x, mid.y);

    return groupGfx;
  }

  var commonjsGlobal = typeof window !== 'undefined' ? window : typeof global !== 'undefined' ? global : typeof self !== 'undefined' ? self : {};

  function createCommonjsModule(fn, module) {
  	return module = { exports: {} }, fn(module, module.exports), module.exports;
  }

  var css_escape = createCommonjsModule(function (module, exports) {
  (function(root, factory) {
  	// https://github.com/umdjs/umd/blob/master/returnExports.js
  	{
  		// For Node.js.
  		module.exports = factory(root);
  	}
  }(typeof commonjsGlobal != 'undefined' ? commonjsGlobal : commonjsGlobal, function(root) {

  	if (root.CSS && root.CSS.escape) {
  		return root.CSS.escape;
  	}

  	// https://drafts.csswg.org/cssom/#serialize-an-identifier
  	var cssEscape = function(value) {
  		if (arguments.length == 0) {
  			throw new TypeError('`CSS.escape` requires an argument.');
  		}
  		var string = String(value);
  		var length = string.length;
  		var index = -1;
  		var codeUnit;
  		var result = '';
  		var firstCodeUnit = string.charCodeAt(0);
  		while (++index < length) {
  			codeUnit = string.charCodeAt(index);
  			// Note: there’s no need to special-case astral symbols, surrogate
  			// pairs, or lone surrogates.

  			// If the character is NULL (U+0000), then the REPLACEMENT CHARACTER
  			// (U+FFFD).
  			if (codeUnit == 0x0000) {
  				result += '\uFFFD';
  				continue;
  			}

  			if (
  				// If the character is in the range [\1-\1F] (U+0001 to U+001F) or is
  				// U+007F, […]
  				(codeUnit >= 0x0001 && codeUnit <= 0x001F) || codeUnit == 0x007F ||
  				// If the character is the first character and is in the range [0-9]
  				// (U+0030 to U+0039), […]
  				(index == 0 && codeUnit >= 0x0030 && codeUnit <= 0x0039) ||
  				// If the character is the second character and is in the range [0-9]
  				// (U+0030 to U+0039) and the first character is a `-` (U+002D), […]
  				(
  					index == 1 &&
  					codeUnit >= 0x0030 && codeUnit <= 0x0039 &&
  					firstCodeUnit == 0x002D
  				)
  			) {
  				// https://drafts.csswg.org/cssom/#escape-a-character-as-code-point
  				result += '\\' + codeUnit.toString(16) + ' ';
  				continue;
  			}

  			if (
  				// If the character is the first character and is a `-` (U+002D), and
  				// there is no second character, […]
  				index == 0 &&
  				length == 1 &&
  				codeUnit == 0x002D
  			) {
  				result += '\\' + string.charAt(index);
  				continue;
  			}

  			// If the character is not handled by one of the above rules and is
  			// greater than or equal to U+0080, is `-` (U+002D) or `_` (U+005F), or
  			// is in one of the ranges [0-9] (U+0030 to U+0039), [A-Z] (U+0041 to
  			// U+005A), or [a-z] (U+0061 to U+007A), […]
  			if (
  				codeUnit >= 0x0080 ||
  				codeUnit == 0x002D ||
  				codeUnit == 0x005F ||
  				codeUnit >= 0x0030 && codeUnit <= 0x0039 ||
  				codeUnit >= 0x0041 && codeUnit <= 0x005A ||
  				codeUnit >= 0x0061 && codeUnit <= 0x007A
  			) {
  				// the character itself
  				result += string.charAt(index);
  				continue;
  			}

  			// Otherwise, the escaped character.
  			// https://drafts.csswg.org/cssom/#escape-a-character
  			result += '\\' + string.charAt(index);

  		}
  		return result;
  	};

  	if (!root.CSS) {
  		root.CSS = {};
  	}

  	root.CSS.escape = cssEscape;
  	return cssEscape;

  }));
  });

  /**
   * This file contains portions that got extraced from Snap.svg (licensed Apache-2.0).
   *
   * @see https://github.com/adobe-webplatform/Snap.svg/blob/master/src/path.js
   */

  /* eslint no-fallthrough: "off" */

  var has$2 = 'hasOwnProperty',
      p2s = /,?([a-z]),?/gi,
      toFloat = parseFloat,
      math = Math,
      PI = math.PI,
      mmin = math.min,
      mmax = math.max,
      pow = math.pow,
      abs = math.abs,
      pathCommand = /([a-z])[\s,]*((-?\d*\.?\d*(?:e[-+]?\d+)?[\s]*,?[\s]*)+)/ig,
      pathValues = /(-?\d*\.?\d*(?:e[-+]?\\d+)?)[\s]*,?[\s]*/ig;

  function is(o, type) {
    type = String.prototype.toLowerCase.call(type);

    if (type == 'finite') {
      return isFinite(o);
    }

    if (type == 'array' && (o instanceof Array || Array.isArray && Array.isArray(o))) {
      return true;
    }

    return (type == 'null' && o === null) ||
           (type == typeof o && o !== null) ||
           (type == 'object' && o === Object(o)) ||
           Object.prototype.toString.call(o).slice(8, -1).toLowerCase() == type;
  }

  function clone$1(obj) {

    if (typeof obj == 'function' || Object(obj) !== obj) {
      return obj;
    }

    var res = new obj.constructor;

    for (var key in obj) if (obj[has$2](key)) {
      res[key] = clone$1(obj[key]);
    }

    return res;
  }

  function repush(array, item) {
    for (var i = 0, ii = array.length; i < ii; i++) if (array[i] === item) {
      return array.push(array.splice(i, 1)[0]);
    }
  }

  function cacher(f, scope, postprocessor) {

    function newf() {

      var arg = Array.prototype.slice.call(arguments, 0),
          args = arg.join('\u2400'),
          cache = newf.cache = newf.cache || {},
          count = newf.count = newf.count || [];

      if (cache[has$2](args)) {
        repush(count, args);
        return postprocessor ? postprocessor(cache[args]) : cache[args];
      }

      count.length >= 1e3 && delete cache[count.shift()];
      count.push(args);
      cache[args] = f.apply(scope, arg);

      return postprocessor ? postprocessor(cache[args]) : cache[args];
    }
    return newf;
  }

  function parsePathString(pathString) {

    if (!pathString) {
      return null;
    }

    var pth = paths(pathString);

    if (pth.arr) {
      return clone$1(pth.arr);
    }

    var paramCounts = { a: 7, c: 6, o: 2, h: 1, l: 2, m: 2, r: 4, q: 4, s: 4, t: 2, v: 1, u: 3, z: 0 },
        data = [];

    if (is(pathString, 'array') && is(pathString[0], 'array')) { // rough assumption
      data = clone$1(pathString);
    }

    if (!data.length) {

      String(pathString).replace(pathCommand, function(a, b, c) {
        var params = [],
            name = b.toLowerCase();

        c.replace(pathValues, function(a, b) {
          b && params.push(+b);
        });

        if (name == 'm' && params.length > 2) {
          data.push([b].concat(params.splice(0, 2)));
          name = 'l';
          b = b == 'm' ? 'l' : 'L';
        }

        if (name == 'o' && params.length == 1) {
          data.push([b, params[0]]);
        }

        if (name == 'r') {
          data.push([b].concat(params));
        } else while (params.length >= paramCounts[name]) {
          data.push([b].concat(params.splice(0, paramCounts[name])));
          if (!paramCounts[name]) {
            break;
          }
        }
      });
    }

    data.toString = paths.toString;
    pth.arr = clone$1(data);

    return data;
  }

  function paths(ps) {
    var p = paths.ps = paths.ps || {};

    if (p[ps]) {
      p[ps].sleep = 100;
    } else {
      p[ps] = {
        sleep: 100
      };
    }

    setTimeout(function() {
      for (var key in p) if (p[has$2](key) && key != ps) {
        p[key].sleep--;
        !p[key].sleep && delete p[key];
      }
    });

    return p[ps];
  }

  function box(x, y, width, height) {
    if (x == null) {
      x = y = width = height = 0;
    }

    if (y == null) {
      y = x.y;
      width = x.width;
      height = x.height;
      x = x.x;
    }

    return {
      x: x,
      y: y,
      width: width,
      w: width,
      height: height,
      h: height,
      x2: x + width,
      y2: y + height,
      cx: x + width / 2,
      cy: y + height / 2,
      r1: math.min(width, height) / 2,
      r2: math.max(width, height) / 2,
      r0: math.sqrt(width * width + height * height) / 2,
      path: rectPath(x, y, width, height),
      vb: [x, y, width, height].join(' ')
    };
  }

  function pathToString() {
    return this.join(',').replace(p2s, '$1');
  }

  function pathClone(pathArray) {
    var res = clone$1(pathArray);
    res.toString = pathToString;
    return res;
  }

  function findDotsAtSegment(p1x, p1y, c1x, c1y, c2x, c2y, p2x, p2y, t) {
    var t1 = 1 - t,
        t13 = pow(t1, 3),
        t12 = pow(t1, 2),
        t2 = t * t,
        t3 = t2 * t,
        x = t13 * p1x + t12 * 3 * t * c1x + t1 * 3 * t * t * c2x + t3 * p2x,
        y = t13 * p1y + t12 * 3 * t * c1y + t1 * 3 * t * t * c2y + t3 * p2y,
        mx = p1x + 2 * t * (c1x - p1x) + t2 * (c2x - 2 * c1x + p1x),
        my = p1y + 2 * t * (c1y - p1y) + t2 * (c2y - 2 * c1y + p1y),
        nx = c1x + 2 * t * (c2x - c1x) + t2 * (p2x - 2 * c2x + c1x),
        ny = c1y + 2 * t * (c2y - c1y) + t2 * (p2y - 2 * c2y + c1y),
        ax = t1 * p1x + t * c1x,
        ay = t1 * p1y + t * c1y,
        cx = t1 * c2x + t * p2x,
        cy = t1 * c2y + t * p2y,
        alpha = (90 - math.atan2(mx - nx, my - ny) * 180 / PI);

    return {
      x: x,
      y: y,
      m: { x: mx, y: my },
      n: { x: nx, y: ny },
      start: { x: ax, y: ay },
      end: { x: cx, y: cy },
      alpha: alpha
    };
  }

  function bezierBBox(p1x, p1y, c1x, c1y, c2x, c2y, p2x, p2y) {

    if (!is(p1x, 'array')) {
      p1x = [p1x, p1y, c1x, c1y, c2x, c2y, p2x, p2y];
    }

    var bbox = curveBBox.apply(null, p1x);

    return box(
      bbox.min.x,
      bbox.min.y,
      bbox.max.x - bbox.min.x,
      bbox.max.y - bbox.min.y
    );
  }

  function isPointInsideBBox(bbox, x, y) {
    return x >= bbox.x &&
      x <= bbox.x + bbox.width &&
      y >= bbox.y &&
      y <= bbox.y + bbox.height;
  }

  function isBBoxIntersect(bbox1, bbox2) {
    bbox1 = box(bbox1);
    bbox2 = box(bbox2);
    return isPointInsideBBox(bbox2, bbox1.x, bbox1.y)
      || isPointInsideBBox(bbox2, bbox1.x2, bbox1.y)
      || isPointInsideBBox(bbox2, bbox1.x, bbox1.y2)
      || isPointInsideBBox(bbox2, bbox1.x2, bbox1.y2)
      || isPointInsideBBox(bbox1, bbox2.x, bbox2.y)
      || isPointInsideBBox(bbox1, bbox2.x2, bbox2.y)
      || isPointInsideBBox(bbox1, bbox2.x, bbox2.y2)
      || isPointInsideBBox(bbox1, bbox2.x2, bbox2.y2)
      || (bbox1.x < bbox2.x2 && bbox1.x > bbox2.x
          || bbox2.x < bbox1.x2 && bbox2.x > bbox1.x)
      && (bbox1.y < bbox2.y2 && bbox1.y > bbox2.y
          || bbox2.y < bbox1.y2 && bbox2.y > bbox1.y);
  }

  function base3(t, p1, p2, p3, p4) {
    var t1 = -3 * p1 + 9 * p2 - 9 * p3 + 3 * p4,
        t2 = t * t1 + 6 * p1 - 12 * p2 + 6 * p3;
    return t * t2 - 3 * p1 + 3 * p2;
  }

  function bezlen(x1, y1, x2, y2, x3, y3, x4, y4, z) {

    if (z == null) {
      z = 1;
    }

    z = z > 1 ? 1 : z < 0 ? 0 : z;

    var z2 = z / 2,
        n = 12,
        Tvalues = [-.1252,.1252,-.3678,.3678,-.5873,.5873,-.7699,.7699,-.9041,.9041,-.9816,.9816],
        Cvalues = [0.2491,0.2491,0.2335,0.2335,0.2032,0.2032,0.1601,0.1601,0.1069,0.1069,0.0472,0.0472],
        sum = 0;

    for (var i = 0; i < n; i++) {
      var ct = z2 * Tvalues[i] + z2,
          xbase = base3(ct, x1, x2, x3, x4),
          ybase = base3(ct, y1, y2, y3, y4),
          comb = xbase * xbase + ybase * ybase;

      sum += Cvalues[i] * math.sqrt(comb);
    }

    return z2 * sum;
  }


  function intersectLines(x1, y1, x2, y2, x3, y3, x4, y4) {

    if (
      mmax(x1, x2) < mmin(x3, x4) ||
        mmin(x1, x2) > mmax(x3, x4) ||
        mmax(y1, y2) < mmin(y3, y4) ||
        mmin(y1, y2) > mmax(y3, y4)
    ) {
      return;
    }

    var nx = (x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4),
        ny = (x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4),
        denominator = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);

    if (!denominator) {
      return;
    }

    var px = nx / denominator,
        py = ny / denominator,
        px2 = +px.toFixed(2),
        py2 = +py.toFixed(2);

    if (
      px2 < +mmin(x1, x2).toFixed(2) ||
        px2 > +mmax(x1, x2).toFixed(2) ||
        px2 < +mmin(x3, x4).toFixed(2) ||
        px2 > +mmax(x3, x4).toFixed(2) ||
        py2 < +mmin(y1, y2).toFixed(2) ||
        py2 > +mmax(y1, y2).toFixed(2) ||
        py2 < +mmin(y3, y4).toFixed(2) ||
        py2 > +mmax(y3, y4).toFixed(2)
    ) {
      return;
    }

    return { x: px, y: py };
  }

  function findBezierIntersections(bez1, bez2, justCount) {
    var bbox1 = bezierBBox(bez1),
        bbox2 = bezierBBox(bez2);

    if (!isBBoxIntersect(bbox1, bbox2)) {
      return justCount ? 0 : [];
    }

    var l1 = bezlen.apply(0, bez1),
        l2 = bezlen.apply(0, bez2),
        n1 = ~~(l1 / 5),
        n2 = ~~(l2 / 5),
        dots1 = [],
        dots2 = [],
        xy = {},
        res = justCount ? 0 : [];

    for (var i = 0; i < n1 + 1; i++) {
      var p = findDotsAtSegment.apply(0, bez1.concat(i / n1));
      dots1.push({ x: p.x, y: p.y, t: i / n1 });
    }

    for (i = 0; i < n2 + 1; i++) {
      p = findDotsAtSegment.apply(0, bez2.concat(i / n2));
      dots2.push({ x: p.x, y: p.y, t: i / n2 });
    }

    for (i = 0; i < n1; i++) {

      for (var j = 0; j < n2; j++) {
        var di = dots1[i],
            di1 = dots1[i + 1],
            dj = dots2[j],
            dj1 = dots2[j + 1],
            ci = abs(di1.x - di.x) < .01 ? 'y' : 'x',
            cj = abs(dj1.x - dj.x) < .01 ? 'y' : 'x',
            is = intersectLines(di.x, di.y, di1.x, di1.y, dj.x, dj.y, dj1.x, dj1.y);

        if (is) {

          if (xy[is.x.toFixed(0)] == is.y.toFixed(0)) {
            continue;
          }

          xy[is.x.toFixed(0)] = is.y.toFixed(0);

          var t1 = di.t + abs((is[ci] - di[ci]) / (di1[ci] - di[ci])) * (di1.t - di.t),
              t2 = dj.t + abs((is[cj] - dj[cj]) / (dj1[cj] - dj[cj])) * (dj1.t - dj.t);

          if (t1 >= 0 && t1 <= 1 && t2 >= 0 && t2 <= 1) {

            if (justCount) {
              res++;
            } else {
              res.push({
                x: is.x,
                y: is.y,
                t1: t1,
                t2: t2
              });
            }
          }
        }
      }
    }

    return res;
  }


  /**
   * Find or counts the intersections between two SVG paths.
   *
   * Returns a number in counting mode and a list of intersections otherwise.
   *
   * A single intersection entry contains the intersection coordinates (x, y)
   * as well as additional information regarding the intersecting segments
   * on each path (segment1, segment2) and the relative location of the
   * intersection on these segments (t1, t2).
   *
   * The path may be an SVG path string or a list of path components
   * such as `[ [ 'M', 0, 10 ], [ 'L', 20, 0 ] ]`.
   *
   * @example
   *
   * var intersections = findPathIntersections(
   *   'M0,0L100,100',
   *   [ [ 'M', 0, 100 ], [ 'L', 100, 0 ] ]
   * );
   *
   * // intersections = [
   * //   { x: 50, y: 50, segment1: 1, segment2: 1, t1: 0.5, t2: 0.5 }
   * //
   *
   * @param {String|Array<PathDef>} path1
   * @param {String|Array<PathDef>} path2
   * @param {Boolean} [justCount=false]
   *
   * @return {Array<Intersection>|Number}
   */
  function findPathIntersections(path1, path2, justCount) {
    path1 = pathToCurve(path1);
    path2 = pathToCurve(path2);

    var x1, y1, x2, y2, x1m, y1m, x2m, y2m, bez1, bez2,
        res = justCount ? 0 : [];

    for (var i = 0, ii = path1.length; i < ii; i++) {
      var pi = path1[i];

      if (pi[0] == 'M') {
        x1 = x1m = pi[1];
        y1 = y1m = pi[2];
      } else {

        if (pi[0] == 'C') {
          bez1 = [x1, y1].concat(pi.slice(1));
          x1 = bez1[6];
          y1 = bez1[7];
        } else {
          bez1 = [x1, y1, x1, y1, x1m, y1m, x1m, y1m];
          x1 = x1m;
          y1 = y1m;
        }

        for (var j = 0, jj = path2.length; j < jj; j++) {
          var pj = path2[j];

          if (pj[0] == 'M') {
            x2 = x2m = pj[1];
            y2 = y2m = pj[2];
          } else {

            if (pj[0] == 'C') {
              bez2 = [x2, y2].concat(pj.slice(1));
              x2 = bez2[6];
              y2 = bez2[7];
            } else {
              bez2 = [x2, y2, x2, y2, x2m, y2m, x2m, y2m];
              x2 = x2m;
              y2 = y2m;
            }

            var intr = findBezierIntersections(bez1, bez2, justCount);

            if (justCount) {
              res += intr;
            } else {

              for (var k = 0, kk = intr.length; k < kk; k++) {
                intr[k].segment1 = i;
                intr[k].segment2 = j;
                intr[k].bez1 = bez1;
                intr[k].bez2 = bez2;
              }

              res = res.concat(intr);
            }
          }
        }
      }
    }

    return res;
  }


  function rectPath(x, y, w, h, r) {
    if (r) {
      return [
        ['M', +x + (+r), y],
        ['l', w - r * 2, 0],
        ['a', r, r, 0, 0, 1, r, r],
        ['l', 0, h - r * 2],
        ['a', r, r, 0, 0, 1, -r, r],
        ['l', r * 2 - w, 0],
        ['a', r, r, 0, 0, 1, -r, -r],
        ['l', 0, r * 2 - h],
        ['a', r, r, 0, 0, 1, r, -r],
        ['z']
      ];
    }

    var res = [['M', x, y], ['l', w, 0], ['l', 0, h], ['l', -w, 0], ['z']];
    res.toString = pathToString;

    return res;
  }

  function ellipsePath(x, y, rx, ry, a) {
    if (a == null && ry == null) {
      ry = rx;
    }

    x = +x;
    y = +y;
    rx = +rx;
    ry = +ry;

    if (a != null) {
      var rad = Math.PI / 180,
          x1 = x + rx * Math.cos(-ry * rad),
          x2 = x + rx * Math.cos(-a * rad),
          y1 = y + rx * Math.sin(-ry * rad),
          y2 = y + rx * Math.sin(-a * rad),
          res = [['M', x1, y1], ['A', rx, rx, 0, +(a - ry > 180), 0, x2, y2]];
    } else {
      res = [
        ['M', x, y],
        ['m', 0, -ry],
        ['a', rx, ry, 0, 1, 1, 0, 2 * ry],
        ['a', rx, ry, 0, 1, 1, 0, -2 * ry],
        ['z']
      ];
    }

    res.toString = pathToString;

    return res;
  }


  function pathToAbsolute(pathArray) {
    var pth = paths(pathArray);

    if (pth.abs) {
      return pathClone(pth.abs);
    }

    if (!is(pathArray, 'array') || !is(pathArray && pathArray[0], 'array')) { // rough assumption
      pathArray = parsePathString(pathArray);
    }

    if (!pathArray || !pathArray.length) {
      return [['M', 0, 0]];
    }

    var res = [],
        x = 0,
        y = 0,
        mx = 0,
        my = 0,
        start = 0,
        pa0;

    if (pathArray[0][0] == 'M') {
      x = +pathArray[0][1];
      y = +pathArray[0][2];
      mx = x;
      my = y;
      start++;
      res[0] = ['M', x, y];
    }

    var crz = pathArray.length == 3 &&
        pathArray[0][0] == 'M' &&
        pathArray[1][0].toUpperCase() == 'R' &&
        pathArray[2][0].toUpperCase() == 'Z';

    for (var r, pa, i = start, ii = pathArray.length; i < ii; i++) {
      res.push(r = []);
      pa = pathArray[i];
      pa0 = pa[0];

      if (pa0 != pa0.toUpperCase()) {
        r[0] = pa0.toUpperCase();

        switch (r[0]) {
        case 'A':
          r[1] = pa[1];
          r[2] = pa[2];
          r[3] = pa[3];
          r[4] = pa[4];
          r[5] = pa[5];
          r[6] = +pa[6] + x;
          r[7] = +pa[7] + y;
          break;
        case 'V':
          r[1] = +pa[1] + y;
          break;
        case 'H':
          r[1] = +pa[1] + x;
          break;
        case 'R':
          var dots = [x, y].concat(pa.slice(1));

          for (var j = 2, jj = dots.length; j < jj; j++) {
            dots[j] = +dots[j] + x;
            dots[++j] = +dots[j] + y;
          }

          res.pop();
          res = res.concat(catmulRomToBezier(dots, crz));
          break;
        case 'O':
          res.pop();
          dots = ellipsePath(x, y, pa[1], pa[2]);
          dots.push(dots[0]);
          res = res.concat(dots);
          break;
        case 'U':
          res.pop();
          res = res.concat(ellipsePath(x, y, pa[1], pa[2], pa[3]));
          r = ['U'].concat(res[res.length - 1].slice(-2));
          break;
        case 'M':
          mx = +pa[1] + x;
          my = +pa[2] + y;
        default:

          for (j = 1, jj = pa.length; j < jj; j++) {
            r[j] = +pa[j] + ((j % 2) ? x : y);
          }
        }
      } else if (pa0 == 'R') {
        dots = [x, y].concat(pa.slice(1));
        res.pop();
        res = res.concat(catmulRomToBezier(dots, crz));
        r = ['R'].concat(pa.slice(-2));
      } else if (pa0 == 'O') {
        res.pop();
        dots = ellipsePath(x, y, pa[1], pa[2]);
        dots.push(dots[0]);
        res = res.concat(dots);
      } else if (pa0 == 'U') {
        res.pop();
        res = res.concat(ellipsePath(x, y, pa[1], pa[2], pa[3]));
        r = ['U'].concat(res[res.length - 1].slice(-2));
      } else {

        for (var k = 0, kk = pa.length; k < kk; k++) {
          r[k] = pa[k];
        }
      }
      pa0 = pa0.toUpperCase();

      if (pa0 != 'O') {
        switch (r[0]) {
        case 'Z':
          x = +mx;
          y = +my;
          break;
        case 'H':
          x = r[1];
          break;
        case 'V':
          y = r[1];
          break;
        case 'M':
          mx = r[r.length - 2];
          my = r[r.length - 1];
        default:
          x = r[r.length - 2];
          y = r[r.length - 1];
        }
      }
    }

    res.toString = pathToString;
    pth.abs = pathClone(res);

    return res;
  }

  function lineToCurve(x1, y1, x2, y2) {
    return [
      x1, y1, x2,
      y2, x2, y2
    ];
  }

  function qubicToCurve(x1, y1, ax, ay, x2, y2) {
    var _13 = 1 / 3,
        _23 = 2 / 3;

    return [
      _13 * x1 + _23 * ax,
      _13 * y1 + _23 * ay,
      _13 * x2 + _23 * ax,
      _13 * y2 + _23 * ay,
      x2,
      y2
    ];
  }

  function arcToCurve(x1, y1, rx, ry, angle, large_arc_flag, sweep_flag, x2, y2, recursive) {

    // for more information of where this math came from visit:
    // http://www.w3.org/TR/SVG11/implnote.html#ArcImplementationNotes
    var _120 = PI * 120 / 180,
        rad = PI / 180 * (+angle || 0),
        res = [],
        xy,
        rotate = cacher(function(x, y, rad) {
          var X = x * math.cos(rad) - y * math.sin(rad),
              Y = x * math.sin(rad) + y * math.cos(rad);

          return { x: X, y: Y };
        });

    if (!recursive) {
      xy = rotate(x1, y1, -rad);
      x1 = xy.x;
      y1 = xy.y;
      xy = rotate(x2, y2, -rad);
      x2 = xy.x;
      y2 = xy.y;

      var x = (x1 - x2) / 2,
          y = (y1 - y2) / 2;

      var h = (x * x) / (rx * rx) + (y * y) / (ry * ry);

      if (h > 1) {
        h = math.sqrt(h);
        rx = h * rx;
        ry = h * ry;
      }

      var rx2 = rx * rx,
          ry2 = ry * ry,
          k = (large_arc_flag == sweep_flag ? -1 : 1) *
              math.sqrt(abs((rx2 * ry2 - rx2 * y * y - ry2 * x * x) / (rx2 * y * y + ry2 * x * x))),
          cx = k * rx * y / ry + (x1 + x2) / 2,
          cy = k * -ry * x / rx + (y1 + y2) / 2,
          f1 = math.asin(((y1 - cy) / ry).toFixed(9)),
          f2 = math.asin(((y2 - cy) / ry).toFixed(9));

      f1 = x1 < cx ? PI - f1 : f1;
      f2 = x2 < cx ? PI - f2 : f2;
      f1 < 0 && (f1 = PI * 2 + f1);
      f2 < 0 && (f2 = PI * 2 + f2);

      if (sweep_flag && f1 > f2) {
        f1 = f1 - PI * 2;
      }
      if (!sweep_flag && f2 > f1) {
        f2 = f2 - PI * 2;
      }
    } else {
      f1 = recursive[0];
      f2 = recursive[1];
      cx = recursive[2];
      cy = recursive[3];
    }

    var df = f2 - f1;

    if (abs(df) > _120) {
      var f2old = f2,
          x2old = x2,
          y2old = y2;

      f2 = f1 + _120 * (sweep_flag && f2 > f1 ? 1 : -1);
      x2 = cx + rx * math.cos(f2);
      y2 = cy + ry * math.sin(f2);
      res = arcToCurve(x2, y2, rx, ry, angle, 0, sweep_flag, x2old, y2old, [f2, f2old, cx, cy]);
    }

    df = f2 - f1;

    var c1 = math.cos(f1),
        s1 = math.sin(f1),
        c2 = math.cos(f2),
        s2 = math.sin(f2),
        t = math.tan(df / 4),
        hx = 4 / 3 * rx * t,
        hy = 4 / 3 * ry * t,
        m1 = [x1, y1],
        m2 = [x1 + hx * s1, y1 - hy * c1],
        m3 = [x2 + hx * s2, y2 - hy * c2],
        m4 = [x2, y2];

    m2[0] = 2 * m1[0] - m2[0];
    m2[1] = 2 * m1[1] - m2[1];

    if (recursive) {
      return [m2, m3, m4].concat(res);
    } else {
      res = [m2, m3, m4].concat(res).join().split(',');
      var newres = [];

      for (var i = 0, ii = res.length; i < ii; i++) {
        newres[i] = i % 2 ? rotate(res[i - 1], res[i], rad).y : rotate(res[i], res[i + 1], rad).x;
      }

      return newres;
    }
  }

  // http://schepers.cc/getting-to-the-point
  function catmulRomToBezier(crp, z) {
    var d = [];

    for (var i = 0, iLen = crp.length; iLen - 2 * !z > i; i += 2) {
      var p = [
        { x: +crp[i - 2], y: +crp[i - 1] },
        { x: +crp[i], y: +crp[i + 1] },
        { x: +crp[i + 2], y: +crp[i + 3] },
        { x: +crp[i + 4], y: +crp[i + 5] }
      ];

      if (z) {

        if (!i) {
          p[0] = { x: +crp[iLen - 2], y: +crp[iLen - 1] };
        } else if (iLen - 4 == i) {
          p[3] = { x: +crp[0], y: +crp[1] };
        } else if (iLen - 2 == i) {
          p[2] = { x: +crp[0], y: +crp[1] };
          p[3] = { x: +crp[2], y: +crp[3] };
        }

      } else {

        if (iLen - 4 == i) {
          p[3] = p[2];
        } else if (!i) {
          p[0] = { x: +crp[i], y: +crp[i + 1] };
        }

      }

      d.push(['C',
        (-p[0].x + 6 * p[1].x + p[2].x) / 6,
        (-p[0].y + 6 * p[1].y + p[2].y) / 6,
        (p[1].x + 6 * p[2].x - p[3].x) / 6,
        (p[1].y + 6*p[2].y - p[3].y) / 6,
        p[2].x,
        p[2].y
      ]);
    }

    return d;
  }

  // Returns bounding box of cubic bezier curve.
  // Source: http://blog.hackers-cafe.net/2009/06/how-to-calculate-bezier-curves-bounding.html
  // Original version: NISHIO Hirokazu
  // Modifications: https://github.com/timo22345
  function curveBBox(x0, y0, x1, y1, x2, y2, x3, y3) {
    var tvalues = [],
        bounds = [[], []],
        a, b, c, t, t1, t2, b2ac, sqrtb2ac;

    for (var i = 0; i < 2; ++i) {

      if (i == 0) {
        b = 6 * x0 - 12 * x1 + 6 * x2;
        a = -3 * x0 + 9 * x1 - 9 * x2 + 3 * x3;
        c = 3 * x1 - 3 * x0;
      } else {
        b = 6 * y0 - 12 * y1 + 6 * y2;
        a = -3 * y0 + 9 * y1 - 9 * y2 + 3 * y3;
        c = 3 * y1 - 3 * y0;
      }

      if (abs(a) < 1e-12) {

        if (abs(b) < 1e-12) {
          continue;
        }

        t = -c / b;

        if (0 < t && t < 1) {
          tvalues.push(t);
        }

        continue;
      }

      b2ac = b * b - 4 * c * a;
      sqrtb2ac = math.sqrt(b2ac);

      if (b2ac < 0) {
        continue;
      }

      t1 = (-b + sqrtb2ac) / (2 * a);

      if (0 < t1 && t1 < 1) {
        tvalues.push(t1);
      }

      t2 = (-b - sqrtb2ac) / (2 * a);

      if (0 < t2 && t2 < 1) {
        tvalues.push(t2);
      }
    }

    var j = tvalues.length,
        jlen = j,
        mt;

    while (j--) {
      t = tvalues[j];
      mt = 1 - t;
      bounds[0][j] = (mt * mt * mt * x0) + (3 * mt * mt * t * x1) + (3 * mt * t * t * x2) + (t * t * t * x3);
      bounds[1][j] = (mt * mt * mt * y0) + (3 * mt * mt * t * y1) + (3 * mt * t * t * y2) + (t * t * t * y3);
    }

    bounds[0][jlen] = x0;
    bounds[1][jlen] = y0;
    bounds[0][jlen + 1] = x3;
    bounds[1][jlen + 1] = y3;
    bounds[0].length = bounds[1].length = jlen + 2;

    return {
      min: { x: mmin.apply(0, bounds[0]), y: mmin.apply(0, bounds[1]) },
      max: { x: mmax.apply(0, bounds[0]), y: mmax.apply(0, bounds[1]) }
    };
  }

  function pathToCurve(path, path2) {
    var pth = !path2 && paths(path);

    if (!path2 && pth.curve) {
      return pathClone(pth.curve);
    }

    var p = pathToAbsolute(path),
        p2 = path2 && pathToAbsolute(path2),
        attrs = { x: 0, y: 0, bx: 0, by: 0, X: 0, Y: 0, qx: null, qy: null },
        attrs2 = { x: 0, y: 0, bx: 0, by: 0, X: 0, Y: 0, qx: null, qy: null },
        processPath = function(path, d, pcom) {
          var nx, ny;

          if (!path) {
            return ['C', d.x, d.y, d.x, d.y, d.x, d.y];
          }

          !(path[0] in { T: 1, Q: 1 }) && (d.qx = d.qy = null);

          switch (path[0]) {
          case 'M':
            d.X = path[1];
            d.Y = path[2];
            break;
          case 'A':
            path = ['C'].concat(arcToCurve.apply(0, [d.x, d.y].concat(path.slice(1))));
            break;
          case 'S':
            if (pcom == 'C' || pcom == 'S') {
              // In 'S' case we have to take into account, if the previous command is C/S.
              nx = d.x * 2 - d.bx;
              // And reflect the previous
              ny = d.y * 2 - d.by;
              // command's control point relative to the current point.
            }
            else {
              // or some else or nothing
              nx = d.x;
              ny = d.y;
            }
            path = ['C', nx, ny].concat(path.slice(1));
            break;
          case 'T':
            if (pcom == 'Q' || pcom == 'T') {
              // In 'T' case we have to take into account, if the previous command is Q/T.
              d.qx = d.x * 2 - d.qx;
              // And make a reflection similar
              d.qy = d.y * 2 - d.qy;
              // to case 'S'.
            }
            else {
              // or something else or nothing
              d.qx = d.x;
              d.qy = d.y;
            }
            path = ['C'].concat(qubicToCurve(d.x, d.y, d.qx, d.qy, path[1], path[2]));
            break;
          case 'Q':
            d.qx = path[1];
            d.qy = path[2];
            path = ['C'].concat(qubicToCurve(d.x, d.y, path[1], path[2], path[3], path[4]));
            break;
          case 'L':
            path = ['C'].concat(lineToCurve(d.x, d.y, path[1], path[2]));
            break;
          case 'H':
            path = ['C'].concat(lineToCurve(d.x, d.y, path[1], d.y));
            break;
          case 'V':
            path = ['C'].concat(lineToCurve(d.x, d.y, d.x, path[1]));
            break;
          case 'Z':
            path = ['C'].concat(lineToCurve(d.x, d.y, d.X, d.Y));
            break;
          }

          return path;
        },

        fixArc = function(pp, i) {

          if (pp[i].length > 7) {
            pp[i].shift();
            var pi = pp[i];

            while (pi.length) {
              pcoms1[i] = 'A'; // if created multiple C:s, their original seg is saved
              p2 && (pcoms2[i] = 'A'); // the same as above
              pp.splice(i++, 0, ['C'].concat(pi.splice(0, 6)));
            }

            pp.splice(i, 1);
            ii = mmax(p.length, p2 && p2.length || 0);
          }
        },

        fixM = function(path1, path2, a1, a2, i) {

          if (path1 && path2 && path1[i][0] == 'M' && path2[i][0] != 'M') {
            path2.splice(i, 0, ['M', a2.x, a2.y]);
            a1.bx = 0;
            a1.by = 0;
            a1.x = path1[i][1];
            a1.y = path1[i][2];
            ii = mmax(p.length, p2 && p2.length || 0);
          }
        },

        pcoms1 = [], // path commands of original path p
        pcoms2 = [], // path commands of original path p2
        pfirst = '', // temporary holder for original path command
        pcom = ''; // holder for previous path command of original path

    for (var i = 0, ii = mmax(p.length, p2 && p2.length || 0); i < ii; i++) {
      p[i] && (pfirst = p[i][0]); // save current path command

      if (pfirst != 'C') // C is not saved yet, because it may be result of conversion
      {
        pcoms1[i] = pfirst; // Save current path command
        i && (pcom = pcoms1[i - 1]); // Get previous path command pcom
      }
      p[i] = processPath(p[i], attrs, pcom); // Previous path command is inputted to processPath

      if (pcoms1[i] != 'A' && pfirst == 'C') pcoms1[i] = 'C'; // A is the only command
      // which may produce multiple C:s
      // so we have to make sure that C is also C in original path

      fixArc(p, i); // fixArc adds also the right amount of A:s to pcoms1

      if (p2) { // the same procedures is done to p2
        p2[i] && (pfirst = p2[i][0]);

        if (pfirst != 'C') {
          pcoms2[i] = pfirst;
          i && (pcom = pcoms2[i - 1]);
        }

        p2[i] = processPath(p2[i], attrs2, pcom);

        if (pcoms2[i] != 'A' && pfirst == 'C') {
          pcoms2[i] = 'C';
        }

        fixArc(p2, i);
      }

      fixM(p, p2, attrs, attrs2, i);
      fixM(p2, p, attrs2, attrs, i);

      var seg = p[i],
          seg2 = p2 && p2[i],
          seglen = seg.length,
          seg2len = p2 && seg2.length;

      attrs.x = seg[seglen - 2];
      attrs.y = seg[seglen - 1];
      attrs.bx = toFloat(seg[seglen - 4]) || attrs.x;
      attrs.by = toFloat(seg[seglen - 3]) || attrs.y;
      attrs2.bx = p2 && (toFloat(seg2[seg2len - 4]) || attrs2.x);
      attrs2.by = p2 && (toFloat(seg2[seg2len - 3]) || attrs2.y);
      attrs2.x = p2 && seg2[seg2len - 2];
      attrs2.y = p2 && seg2[seg2len - 1];
    }

    if (!p2) {
      pth.curve = pathClone(p);
    }

    return p2 ? [p, p2] : p;
  }

  var intersect = findPathIntersections;

  var round$2 = Math.round,
      max = Math.max;


  function circlePath(center, r) {
    var x = center.x,
        y = center.y;

    return [
      ['M', x, y],
      ['m', 0, -r],
      ['a', r, r, 0, 1, 1, 0, 2 * r],
      ['a', r, r, 0, 1, 1, 0, -2 * r],
      ['z']
    ];
  }

  function linePath(points) {
    var segments = [];

    points.forEach(function(p, idx) {
      segments.push([ idx === 0 ? 'M' : 'L', p.x, p.y ]);
    });

    return segments;
  }


  var INTERSECTION_THRESHOLD = 10;

  function getBendpointIntersection(waypoints, reference) {

    var i, w;

    for (i = 0; (w = waypoints[i]); i++) {

      if (pointDistance(w, reference) <= INTERSECTION_THRESHOLD) {
        return {
          point: waypoints[i],
          bendpoint: true,
          index: i
        };
      }
    }

    return null;
  }

  function getPathIntersection(waypoints, reference) {

    var intersections = intersect(circlePath(reference, INTERSECTION_THRESHOLD), linePath(waypoints));

    var a = intersections[0],
        b = intersections[intersections.length - 1],
        idx;

    if (!a) {
      // no intersection
      return null;
    }

    if (a !== b) {

      if (a.segment2 !== b.segment2) {
        // we use the bendpoint in between both segments
        // as the intersection point

        idx = max(a.segment2, b.segment2) - 1;

        return {
          point: waypoints[idx],
          bendpoint: true,
          index: idx
        };
      }

      return {
        point: {
          x: (round$2(a.x + b.x) / 2),
          y: (round$2(a.y + b.y) / 2)
        },
        index: a.segment2
      };
    }

    return {
      point: {
        x: round$2(a.x),
        y: round$2(a.y)
      },
      index: a.segment2
    };
  }

  /**
   * Returns the closest point on the connection towards a given reference point.
   *
   * @param  {Array<Point>} waypoints
   * @param  {Point} reference
   *
   * @return {Object} intersection data (segment, point)
   */
  function getApproxIntersection(waypoints, reference) {
    return getBendpointIntersection(waypoints, reference) || getPathIntersection(waypoints, reference);
  }

  /**
   * A service that adds editable bendpoints to connections.
   */
  function Bendpoints(
      eventBus, canvas, interactionEvents,
      bendpointMove, connectionSegmentMove) {

    function getConnectionIntersection(waypoints, event$$1) {
      var localPosition = toCanvasCoordinates(canvas, event$$1),
          intersection = getApproxIntersection(waypoints, localPosition);

      return intersection;
    }

    function isIntersectionMiddle(intersection, waypoints, treshold) {
      var idx = intersection.index,
          p = intersection.point,
          p0, p1, mid, aligned, xDelta, yDelta;

      if (idx <= 0 || intersection.bendpoint) {
        return false;
      }

      p0 = waypoints[idx - 1];
      p1 = waypoints[idx];
      mid = getMidPoint(p0, p1),
      aligned = pointsAligned(p0, p1);
      xDelta = Math.abs(p.x - mid.x);
      yDelta = Math.abs(p.y - mid.y);

      return aligned && xDelta <= treshold && yDelta <= treshold;
    }

    function activateBendpointMove(event$$1, connection) {
      var waypoints = connection.waypoints,
          intersection = getConnectionIntersection(waypoints, event$$1);

      if (!intersection) {
        return;
      }

      if (isIntersectionMiddle(intersection, waypoints, 10)) {
        connectionSegmentMove.start(event$$1, connection, intersection.index);
      } else {
        bendpointMove.start(event$$1, connection, intersection.index, !intersection.bendpoint);
      }

      // we've handled the event
      return true;
    }

    function bindInteractionEvents(node, eventName, element) {

      componentEvent.bind(node, eventName, function(event$$1) {
        interactionEvents.triggerMouseEvent(eventName, event$$1, element);
        event$$1.stopPropagation();
      });
    }

    function getBendpointsContainer(element, create$$1) {

      var layer = canvas.getLayer('overlays'),
          gfx = query('.djs-bendpoints[data-element-id="' + css_escape(element.id) + '"]', layer);

      if (!gfx && create$$1) {
        gfx = create('g');
        attr$1(gfx, { 'data-element-id': element.id });
        classes$1(gfx).add('djs-bendpoints');

        append(layer, gfx);

        bindInteractionEvents(gfx, 'mousedown', element);
        bindInteractionEvents(gfx, 'click', element);
        bindInteractionEvents(gfx, 'dblclick', element);
      }

      return gfx;
    }

    function createBendpoints(gfx, connection) {
      connection.waypoints.forEach(function(p, idx) {
        var bendpoint = addBendpoint(gfx);

        append(gfx, bendpoint);

        translate(bendpoint, p.x, p.y);
      });

      // add floating bendpoint
      addBendpoint(gfx, 'floating');
    }

    function createSegmentDraggers(gfx, connection) {

      var waypoints = connection.waypoints;

      var segmentStart,
          segmentEnd;

      for (var i = 1; i < waypoints.length; i++) {

        segmentStart = waypoints[i - 1];
        segmentEnd = waypoints[i];

        if (pointsAligned(segmentStart, segmentEnd)) {
          addSegmentDragger(gfx, segmentStart, segmentEnd);
        }
      }
    }

    function clearBendpoints(gfx) {
      forEach(all('.' + BENDPOINT_CLS, gfx), function(node) {
        remove$1(node);
      });
    }

    function clearSegmentDraggers(gfx) {
      forEach(all('.' + SEGMENT_DRAGGER_CLS, gfx), function(node) {
        remove$1(node);
      });
    }

    function addHandles(connection) {

      var gfx = getBendpointsContainer(connection);

      if (!gfx) {
        gfx = getBendpointsContainer(connection, true);

        createBendpoints(gfx, connection);
        createSegmentDraggers(gfx, connection);
      }

      return gfx;
    }

    function updateHandles(connection) {

      var gfx = getBendpointsContainer(connection);

      if (gfx) {
        clearSegmentDraggers(gfx);
        clearBendpoints(gfx);
        createSegmentDraggers(gfx, connection);
        createBendpoints(gfx, connection);
      }
    }

    eventBus.on('connection.changed', function(event$$1) {
      updateHandles(event$$1.element);
    });

    eventBus.on('connection.remove', function(event$$1) {
      var gfx = getBendpointsContainer(event$$1.element);

      if (gfx) {
        remove$1(gfx);
      }
    });

    eventBus.on('element.marker.update', function(event$$1) {

      var element = event$$1.element,
          bendpointsGfx;

      if (!element.waypoints) {
        return;
      }

      bendpointsGfx = addHandles(element);

      if (event$$1.add) {
        classes$1(bendpointsGfx).add(event$$1.marker);
      } else {
        classes$1(bendpointsGfx).remove(event$$1.marker);
      }
    });

    eventBus.on('element.mousemove', function(event$$1) {

      var element = event$$1.element,
          waypoints = element.waypoints,
          bendpointsGfx,
          floating,
          intersection;

      if (waypoints) {
        bendpointsGfx = getBendpointsContainer(element, true);
        floating = query('.floating', bendpointsGfx);

        if (!floating) {
          return;
        }

        intersection = getConnectionIntersection(waypoints, event$$1.originalEvent);

        if (intersection) {
          translate(floating, intersection.point.x, intersection.point.y);
        }
      }
    });

    eventBus.on('element.mousedown', function(event$$1) {

      var originalEvent = event$$1.originalEvent,
          element = event$$1.element,
          waypoints = element.waypoints;

      if (!waypoints) {
        return;
      }

      return activateBendpointMove(originalEvent, element, waypoints);
    });

    eventBus.on('selection.changed', function(event$$1) {
      var newSelection = event$$1.newSelection,
          primary = newSelection[0];

      if (primary && primary.waypoints) {
        addHandles(primary);
      }
    });

    eventBus.on('element.hover', function(event$$1) {
      var element = event$$1.element;

      if (element.waypoints) {
        addHandles(element);
        interactionEvents.registerEvent(event$$1.gfx, 'mousemove', 'element.mousemove');
      }
    });

    eventBus.on('element.out', function(event$$1) {
      interactionEvents.unregisterEvent(event$$1.gfx, 'mousemove', 'element.mousemove');
    });

    // update bendpoint container data attribute on element ID change
    eventBus.on('element.updateId', function(context) {
      var element = context.element,
          newId = context.newId;

      if (element.waypoints) {
        var bendpointContainer = getBendpointsContainer(element);

        if (bendpointContainer) {
          attr$1(bendpointContainer, { 'data-element-id': newId });
        }
      }
    });

    // API

    this.addHandles = addHandles;
    this.updateHandles = updateHandles;
    this.getBendpointsContainer = getBendpointsContainer;
  }

  Bendpoints.$inject = [
    'eventBus',
    'canvas',
    'interactionEvents',
    'bendpointMove',
    'connectionSegmentMove'
  ];

  var MARKER_OK$2 = 'connect-ok',
      MARKER_NOT_OK$2 = 'connect-not-ok',
      MARKER_CONNECT_HOVER = 'connect-hover',
      MARKER_CONNECT_UPDATING = 'djs-updating';

  var COMMAND_BENDPOINT_UPDATE = 'connection.updateWaypoints',
      COMMAND_RECONNECT_START = 'connection.reconnectStart',
      COMMAND_RECONNECT_END = 'connection.reconnectEnd';

  var round$3 = Math.round;


  /**
   * A component that implements moving of bendpoints
   */
  function BendpointMove(
      injector, eventBus, canvas,
      dragging, graphicsFactory, rules,
      modeling) {


    // optional connection docking integration
    var connectionDocking = injector.get('connectionDocking', false);


    // API

    this.start = function(event, connection, bendpointIndex, insert) {

      var type,
          context,
          waypoints = connection.waypoints,
          gfx = canvas.getGraphics(connection);

      if (!insert && bendpointIndex === 0) {
        type = COMMAND_RECONNECT_START;
      } else
      if (!insert && bendpointIndex === waypoints.length - 1) {
        type = COMMAND_RECONNECT_END;
      } else {
        type = COMMAND_BENDPOINT_UPDATE;
      }

      context = {
        connection: connection,
        bendpointIndex: bendpointIndex,
        insert: insert,
        type: type
      };

      dragging.init(event, 'bendpoint.move', {
        data: {
          connection: connection,
          connectionGfx: gfx,
          context: context
        }
      });
    };


    // DRAGGING IMPLEMENTATION


    function redrawConnection(data) {
      graphicsFactory.update('connection', data.connection, data.connectionGfx);
    }

    function filterRedundantWaypoints(waypoints) {

      // alter copy of waypoints, not original
      waypoints = waypoints.slice();

      var idx = 0,
          point,
          previousPoint,
          nextPoint;

      while (waypoints[idx]) {
        point = waypoints[idx];
        previousPoint = waypoints[idx - 1];
        nextPoint = waypoints[idx + 1];

        if (pointDistance(point, nextPoint) === 0 ||
            pointsOnLine(previousPoint, nextPoint, point)) {

          // remove point, if overlapping with {nextPoint}
          // or on line with {previousPoint} -> {point} -> {nextPoint}
          waypoints.splice(idx, 1);
        } else {
          idx++;
        }
      }

      return waypoints;
    }

    eventBus.on('bendpoint.move.start', function(e) {

      var context = e.context,
          connection = context.connection,
          originalWaypoints = connection.waypoints,
          waypoints = originalWaypoints.slice(),
          insert = context.insert,
          idx = context.bendpointIndex;

      context.originalWaypoints = originalWaypoints;

      if (insert) {
        // insert placeholder for bendpoint to-be-added
        waypoints.splice(idx, 0, null);
      }

      connection.waypoints = waypoints;

      // add dragger gfx
      context.draggerGfx = addBendpoint(canvas.getLayer('overlays'));
      classes$1(context.draggerGfx).add('djs-dragging');

      canvas.addMarker(connection, MARKER_CONNECT_UPDATING);
    });

    eventBus.on('bendpoint.move.hover', function(e) {
      var context = e.context;

      context.hover = e.hover;

      if (e.hover) {
        canvas.addMarker(e.hover, MARKER_CONNECT_HOVER);

        // asks whether reconnect / bendpoint move / bendpoint add
        // is allowed at the given position
        var allowed = context.allowed = rules.allowed(context.type, context);

        if (allowed) {
          canvas.removeMarker(context.hover, MARKER_NOT_OK$2);
          canvas.addMarker(context.hover, MARKER_OK$2);

          context.target = context.hover;
        } else if (allowed === false) {
          canvas.removeMarker(context.hover, MARKER_OK$2);
          canvas.addMarker(context.hover, MARKER_NOT_OK$2);

          context.target = null;
        }
      }
    });

    eventBus.on([
      'bendpoint.move.out',
      'bendpoint.move.cleanup'
    ], function(e) {

      // remove connect marker
      // if it was added
      var hover = e.context.hover;

      if (hover) {
        canvas.removeMarker(hover, MARKER_CONNECT_HOVER);
        canvas.removeMarker(hover, e.context.target ? MARKER_OK$2 : MARKER_NOT_OK$2);
      }
    });

    eventBus.on('bendpoint.move.move', function(e) {

      var context = e.context,
          moveType = context.type,
          connection = e.connection,
          source, target;

      connection.waypoints[context.bendpointIndex] = { x: e.x, y: e.y };

      if (connectionDocking) {

        if (context.hover) {
          if (moveType === COMMAND_RECONNECT_START) {
            source = context.hover;
          }

          if (moveType === COMMAND_RECONNECT_END) {
            target = context.hover;
          }
        }

        connection.waypoints = connectionDocking.getCroppedWaypoints(connection, source, target);
      }

      // add dragger gfx
      translate(context.draggerGfx, e.x, e.y);

      redrawConnection(e);
    });

    eventBus.on([
      'bendpoint.move.end',
      'bendpoint.move.cancel'
    ], function(e) {

      var context = e.context,
          hover = context.hover,
          connection = context.connection;

      // remove dragger gfx
      remove$1(context.draggerGfx);
      context.newWaypoints = connection.waypoints.slice();
      connection.waypoints = context.originalWaypoints;
      canvas.removeMarker(connection, MARKER_CONNECT_UPDATING);

      if (hover) {
        canvas.removeMarker(hover, MARKER_OK$2);
        canvas.removeMarker(hover, MARKER_NOT_OK$2);
      }
    });

    eventBus.on('bendpoint.move.end', function(e) {

      var context = e.context,
          waypoints = context.newWaypoints,
          bendpointIndex = context.bendpointIndex,
          bendpoint = waypoints[bendpointIndex],
          allowed = context.allowed,
          hints;

      // ensure we have actual pixel values bendpoint
      // coordinates (important when zoom level was > 1 during move)
      bendpoint.x = round$3(bendpoint.x);
      bendpoint.y = round$3(bendpoint.y);

      if (allowed && context.type === COMMAND_RECONNECT_START) {
        modeling.reconnectStart(context.connection, context.target, bendpoint);
      } else
      if (allowed && context.type === COMMAND_RECONNECT_END) {
        modeling.reconnectEnd(context.connection, context.target, bendpoint);
      } else
      if (allowed !== false && context.type === COMMAND_BENDPOINT_UPDATE) {

        // pass hints on the actual moved bendpoint
        // this is useful for connection and label layouting
        hints = {
          bendpointMove: {
            insert: e.context.insert,
            bendpointIndex: bendpointIndex
          }
        };

        modeling.updateWaypoints(context.connection, filterRedundantWaypoints(waypoints), hints);
      } else {
        redrawConnection(e);

        return false;
      }
    });

    eventBus.on('bendpoint.move.cancel', function(e) {
      redrawConnection(e);
    });
  }

  BendpointMove.$inject = [
    'injector',
    'eventBus',
    'canvas',
    'dragging',
    'graphicsFactory',
    'rules',
    'modeling'
  ];

  function roundBounds(bounds) {
    return {
      x: Math.round(bounds.x),
      y: Math.round(bounds.y),
      width: Math.round(bounds.width),
      height: Math.round(bounds.height)
    };
  }


  function roundPoint(point) {

    return {
      x: Math.round(point.x),
      y: Math.round(point.y)
    };
  }


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


  /**
   * Convert a { top, left, bottom, right } to an objects bounds.
   *
   * @param {Object} trbl
   *
   * @return {Bounds}
   */
  function asBounds(trbl) {
    return {
      x: trbl.left,
      y: trbl.top,
      width: trbl.right - trbl.left,
      height: trbl.bottom - trbl.top
    };
  }


  /**
   * Get the mid of the given bounds or point.
   *
   * @param {Bounds|Point} bounds
   *
   * @return {Point}
   */
  function getMid(bounds) {
    return roundPoint({
      x: bounds.x + (bounds.width || 0) / 2,
      y: bounds.y + (bounds.height || 0) / 2
    });
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


  // intersection utils //////////////////////

  /**
   * Get intersection between an element and a line path.
   *
   * @param {PathDef} elementPath
   * @param {PathDef} linePath
   * @param {Boolean} cropStart crop from start or end
   *
   * @return {Point}
   */
  function getElementLineIntersection(elementPath, linePath, cropStart) {

    var intersections = getIntersections(elementPath, linePath);

    // recognize intersections
    // only one -> choose
    // two close together -> choose first
    // two or more distinct -> pull out appropriate one
    // none -> ok (fallback to point itself)
    if (intersections.length === 1) {
      return roundPoint(intersections[0]);
    } else if (intersections.length === 2 && pointDistance(intersections[0], intersections[1]) < 1) {
      return roundPoint(intersections[0]);
    } else if (intersections.length > 1) {

      // sort by intersections based on connection segment +
      // distance from start
      intersections = sortBy(intersections, function(i) {
        var distance = Math.floor(i.t2 * 100) || 1;

        distance = 100 - distance;

        distance = (distance < 10 ? '0' : '') + distance;

        // create a sort string that makes sure we sort
        // line segment ASC + line segment position DESC (for cropStart)
        // line segment ASC + line segment position ASC (for cropEnd)
        return i.segment2 + '#' + distance;
      });

      return roundPoint(intersections[cropStart ? 0 : intersections.length - 1]);
    }

    return null;
  }


  function getIntersections(a, b) {
    return intersect(a, b);
  }

  var MARKER_CONNECT_HOVER$1 = 'connect-hover',
      MARKER_CONNECT_UPDATING$1 = 'djs-updating';


  function axisAdd(point, axis, delta) {
    return axisSet(point, axis, point[axis] + delta);
  }

  function axisSet(point, axis, value) {
    return {
      x: (axis === 'x' ? value : point.x),
      y: (axis === 'y' ? value : point.y)
    };
  }

  function axisFenced(position, segmentStart, segmentEnd, axis) {

    var maxValue = Math.max(segmentStart[axis], segmentEnd[axis]),
        minValue = Math.min(segmentStart[axis], segmentEnd[axis]);

    var padding = 20;

    var fencedValue = Math.min(Math.max(minValue + padding, position[axis]), maxValue - padding);

    return axisSet(segmentStart, axis, fencedValue);
  }

  function flipAxis(axis) {
    return axis === 'x' ? 'y' : 'x';
  }

  /**
   * Get the docking point on the given element.
   *
   * Compute a reasonable docking, if non exists.
   *
   * @param  {Point} point
   * @param  {djs.model.Shape} referenceElement
   * @param  {String} moveAxis (x|y)
   *
   * @return {Point}
   */
  function getDocking(point, referenceElement, moveAxis) {

    var referenceMid,
        inverseAxis;

    if (point.original) {
      return point.original;
    } else {
      referenceMid = getMid(referenceElement);
      inverseAxis = flipAxis(moveAxis);

      return axisSet(point, inverseAxis, referenceMid[inverseAxis]);
    }
  }

  /**
   * A component that implements moving of bendpoints
   */
  function ConnectionSegmentMove(
      injector, eventBus, canvas,
      dragging, graphicsFactory, rules,
      modeling) {

    // optional connection docking integration
    var connectionDocking = injector.get('connectionDocking', false);


    // API

    this.start = function(event, connection, idx) {

      var context,
          gfx = canvas.getGraphics(connection),
          segmentStartIndex = idx - 1,
          segmentEndIndex = idx,
          waypoints = connection.waypoints,
          segmentStart = waypoints[segmentStartIndex],
          segmentEnd = waypoints[segmentEndIndex],
          direction,
          axis;

      direction = pointsAligned(segmentStart, segmentEnd);

      // do not move diagonal connection
      if (!direction) {
        return;
      }

      // the axis where we are going to move things
      axis = direction === 'v' ? 'y' : 'x';

      if (segmentStartIndex === 0) {
        segmentStart = getDocking(segmentStart, connection.source, axis);
      }

      if (segmentEndIndex === waypoints.length - 1) {
        segmentEnd = getDocking(segmentEnd, connection.target, axis);
      }

      context = {
        connection: connection,
        segmentStartIndex: segmentStartIndex,
        segmentEndIndex: segmentEndIndex,
        segmentStart: segmentStart,
        segmentEnd: segmentEnd,
        axis: axis
      };

      dragging.init(event, {
        x: (segmentStart.x + segmentEnd.x)/2,
        y: (segmentStart.y + segmentEnd.y)/2
      }, 'connectionSegment.move', {
        cursor: axis === 'x' ? 'resize-ew' : 'resize-ns',
        data: {
          connection: connection,
          connectionGfx: gfx,
          context: context
        }
      });
    };

    /**
     * Crop connection if connection cropping is provided.
     *
     * @param {Connection} connection
     * @param {Array<Point>} newWaypoints
     *
     * @return {Array<Point>} cropped connection waypoints
     */
    function cropConnection(connection, newWaypoints) {

      // crop connection, if docking service is provided only
      if (!connectionDocking) {
        return newWaypoints;
      }

      var oldWaypoints = connection.waypoints,
          croppedWaypoints;

      // temporary set new waypoints
      connection.waypoints = newWaypoints;

      croppedWaypoints = connectionDocking.getCroppedWaypoints(connection);

      // restore old waypoints
      connection.waypoints = oldWaypoints;

      return croppedWaypoints;
    }

    // DRAGGING IMPLEMENTATION

    function redrawConnection(data) {
      graphicsFactory.update('connection', data.connection, data.connectionGfx);
    }

    function updateDragger(context, segmentOffset, event) {

      var newWaypoints = context.newWaypoints,
          segmentStartIndex = context.segmentStartIndex + segmentOffset,
          segmentStart = newWaypoints[segmentStartIndex],
          segmentEndIndex = context.segmentEndIndex + segmentOffset,
          segmentEnd = newWaypoints[segmentEndIndex],
          axis = flipAxis(context.axis);

      // make sure the dragger does not move
      // outside the connection
      var draggerPosition = axisFenced(event, segmentStart, segmentEnd, axis);

      // update dragger
      translate(context.draggerGfx, draggerPosition.x, draggerPosition.y);
    }

    /**
     * Filter waypoints for redundant ones (i.e. on the same axis).
     * Returns the filtered waypoints and the offset related to the segment move.
     *
     * @param {Array<Point>} waypoints
     * @param {Integer} segmentStartIndex of moved segment start
     *
     * @return {Object} { filteredWaypoints, segmentOffset }
     */
    function filterRedundantWaypoints(waypoints, segmentStartIndex) {

      var segmentOffset = 0;

      var filteredWaypoints = waypoints.filter(function(r, idx) {
        if (pointsOnLine(waypoints[idx - 1], waypoints[idx + 1], r)) {

          // remove point and increment offset
          segmentOffset = idx <= segmentStartIndex ? segmentOffset - 1 : segmentOffset;
          return false;
        }

        // dont remove point
        return true;
      });

      return {
        waypoints: filteredWaypoints,
        segmentOffset: segmentOffset
      };
    }

    eventBus.on('connectionSegment.move.start', function(e) {

      var context = e.context,
          connection = e.connection,
          layer = canvas.getLayer('overlays');

      context.originalWaypoints = connection.waypoints.slice();

      // add dragger gfx
      context.draggerGfx = addSegmentDragger(layer, context.segmentStart, context.segmentEnd);
      classes$1(context.draggerGfx).add('djs-dragging');

      canvas.addMarker(connection, MARKER_CONNECT_UPDATING$1);
    });

    eventBus.on('connectionSegment.move.move', function(e) {

      var context = e.context,
          connection = context.connection,
          segmentStartIndex = context.segmentStartIndex,
          segmentEndIndex = context.segmentEndIndex,
          segmentStart = context.segmentStart,
          segmentEnd = context.segmentEnd,
          axis = context.axis;

      var newWaypoints = context.originalWaypoints.slice(),
          newSegmentStart = axisAdd(segmentStart, axis, e['d' + axis]),
          newSegmentEnd = axisAdd(segmentEnd, axis, e['d' + axis]);

      // original waypoint count and added / removed
      // from start waypoint delta. We use the later
      // to retrieve the updated segmentStartIndex / segmentEndIndex
      var waypointCount = newWaypoints.length,
          segmentOffset = 0;

      // move segment start / end by axis delta
      newWaypoints[segmentStartIndex] = newSegmentStart;
      newWaypoints[segmentEndIndex] = newSegmentEnd;

      var sourceToSegmentOrientation,
          targetToSegmentOrientation;

      // handle first segment
      if (segmentStartIndex < 2) {
        sourceToSegmentOrientation = getOrientation(connection.source, newSegmentStart);

        // first bendpoint, remove first segment if intersecting
        if (segmentStartIndex === 1) {

          if (sourceToSegmentOrientation === 'intersect') {
            newWaypoints.shift();
            newWaypoints[0] = newSegmentStart;
            segmentOffset--;
          }
        }

        // docking point, add segment if not intersecting anymore
        else {
          if (sourceToSegmentOrientation !== 'intersect') {
            newWaypoints.unshift(segmentStart);
            segmentOffset++;
          }
        }
      }

      // handle last segment
      if (segmentEndIndex > waypointCount - 3) {
        targetToSegmentOrientation = getOrientation(connection.target, newSegmentEnd);

        // last bendpoint, remove last segment if intersecting
        if (segmentEndIndex === waypointCount - 2) {

          if (targetToSegmentOrientation === 'intersect') {
            newWaypoints.pop();
            newWaypoints[newWaypoints.length - 1] = newSegmentEnd;
          }
        }

        // last bendpoint, remove last segment if intersecting
        else {
          if (targetToSegmentOrientation !== 'intersect') {
            newWaypoints.push(segmentEnd);
          }
        }
      }

      // update connection waypoints
      context.newWaypoints = connection.waypoints = cropConnection(connection, newWaypoints);

      // update dragger position
      updateDragger(context, segmentOffset, e);

      // save segmentOffset in context
      context.newSegmentStartIndex = segmentStartIndex + segmentOffset;

      // redraw connection
      redrawConnection(e);
    });

    eventBus.on('connectionSegment.move.hover', function(e) {

      e.context.hover = e.hover;
      canvas.addMarker(e.hover, MARKER_CONNECT_HOVER$1);
    });

    eventBus.on([
      'connectionSegment.move.out',
      'connectionSegment.move.cleanup'
    ], function(e) {

      // remove connect marker
      // if it was added
      var hover = e.context.hover;

      if (hover) {
        canvas.removeMarker(hover, MARKER_CONNECT_HOVER$1);
      }
    });

    eventBus.on('connectionSegment.move.cleanup', function(e) {

      var context = e.context,
          connection = context.connection;

      // remove dragger gfx
      if (context.draggerGfx) {
        remove$1(context.draggerGfx);
      }

      canvas.removeMarker(connection, MARKER_CONNECT_UPDATING$1);
    });

    eventBus.on([
      'connectionSegment.move.cancel',
      'connectionSegment.move.end'
    ], function(e) {
      var context = e.context,
          connection = context.connection;

      connection.waypoints = context.originalWaypoints;

      redrawConnection(e);
    });

    eventBus.on('connectionSegment.move.end', function(e) {

      var context = e.context,
          connection = context.connection,
          newWaypoints = context.newWaypoints,
          newSegmentStartIndex = context.newSegmentStartIndex;

      // ensure we have actual pixel values bendpoint
      // coordinates (important when zoom level was > 1 during move)
      newWaypoints = newWaypoints.map(function(p) {
        return {
          original: p.original,
          x: Math.round(p.x),
          y: Math.round(p.y)
        };
      });

      // apply filter redunant waypoints
      var filtered = filterRedundantWaypoints(newWaypoints, newSegmentStartIndex);

      // get filtered waypoints
      var filteredWaypoints = filtered.waypoints,
          croppedWaypoints = cropConnection(connection, filteredWaypoints),
          segmentOffset = filtered.segmentOffset;

      var hints = {
        segmentMove: {
          segmentStartIndex: context.segmentStartIndex,
          newSegmentStartIndex: newSegmentStartIndex + segmentOffset
        }
      };

      modeling.updateWaypoints(connection, croppedWaypoints, hints);
    });
  }

  ConnectionSegmentMove.$inject = [
    'injector',
    'eventBus',
    'canvas',
    'dragging',
    'graphicsFactory',
    'rules',
    'modeling'
  ];

  var abs$1= Math.abs,
      round$4 = Math.round;

  var TOLERANCE = 10;


  function BendpointSnapping(eventBus) {

    function snapTo(values$$1, value) {

      if (isArray(values$$1)) {
        var i = values$$1.length;

        while (i--) if (abs$1(values$$1[i] - value) <= TOLERANCE) {
          return values$$1[i];
        }
      } else {
        values$$1 = +values$$1;
        var rem = value % values$$1;

        if (rem < TOLERANCE) {
          return value - rem;
        }

        if (rem > values$$1 - TOLERANCE) {
          return value - rem + values$$1;
        }
      }

      return value;
    }

    function mid(element) {
      if (element.width) {
        return {
          x: round$4(element.width / 2 + element.x),
          y: round$4(element.height / 2 + element.y)
        };
      }
    }

    // connection segment snapping //////////////////////

    function getConnectionSegmentSnaps(context) {

      var snapPoints = context.snapPoints,
          connection = context.connection,
          waypoints = connection.waypoints,
          segmentStart = context.segmentStart,
          segmentStartIndex = context.segmentStartIndex,
          segmentEnd = context.segmentEnd,
          segmentEndIndex = context.segmentEndIndex,
          axis = context.axis;

      if (snapPoints) {
        return snapPoints;
      }

      var referenceWaypoints = [
        waypoints[segmentStartIndex - 1],
        segmentStart,
        segmentEnd,
        waypoints[segmentEndIndex + 1]
      ];

      if (segmentStartIndex < 2) {
        referenceWaypoints.unshift(mid(connection.source));
      }

      if (segmentEndIndex > waypoints.length - 3) {
        referenceWaypoints.unshift(mid(connection.target));
      }

      context.snapPoints = snapPoints = { horizontal: [] , vertical: [] };

      forEach(referenceWaypoints, function(p) {
        // we snap on existing bendpoints only,
        // not placeholders that are inserted during add
        if (p) {
          p = p.original || p;

          if (axis === 'y') {
            snapPoints.horizontal.push(p.y);
          }

          if (axis === 'x') {
            snapPoints.vertical.push(p.x);
          }
        }
      });

      return snapPoints;
    }

    eventBus.on('connectionSegment.move.move', 1500, function(event) {
      var context = event.context,
          snapPoints = getConnectionSegmentSnaps(context),
          x = event.x,
          y = event.y,
          sx, sy;

      if (!snapPoints) {
        return;
      }

      // snap
      sx = snapTo(snapPoints.vertical, x);
      sy = snapTo(snapPoints.horizontal, y);


      // correction x/y
      var cx = (x - sx),
          cy = (y - sy);

      // update delta
      assign(event, {
        dx: event.dx - cx,
        dy: event.dy - cy,
        x: sx,
        y: sy
      });
    });


    // bendpoint snapping //////////////////////

    function getBendpointSnaps(context) {

      var snapPoints = context.snapPoints,
          waypoints = context.connection.waypoints,
          bendpointIndex = context.bendpointIndex;

      if (snapPoints) {
        return snapPoints;
      }

      var referenceWaypoints = [ waypoints[bendpointIndex - 1], waypoints[bendpointIndex + 1] ];

      context.snapPoints = snapPoints = { horizontal: [] , vertical: [] };

      forEach(referenceWaypoints, function(p) {
        // we snap on existing bendpoints only,
        // not placeholders that are inserted during add
        if (p) {
          p = p.original || p;

          snapPoints.horizontal.push(p.y);
          snapPoints.vertical.push(p.x);
        }
      });

      return snapPoints;
    }


    eventBus.on('bendpoint.move.move', 1500, function(event) {

      var context = event.context,
          snapPoints = getBendpointSnaps(context),
          target = context.target,
          targetMid = target && mid(target),
          x = event.x,
          y = event.y,
          sx, sy;

      if (!snapPoints) {
        return;
      }

      // snap
      sx = snapTo(targetMid ? snapPoints.vertical.concat([ targetMid.x ]) : snapPoints.vertical, x);
      sy = snapTo(targetMid ? snapPoints.horizontal.concat([ targetMid.y ]) : snapPoints.horizontal, y);


      // correction x/y
      var cx = (x - sx),
          cy = (y - sy);

      // update delta
      assign(event, {
        dx: event.dx - cx,
        dy: event.dy - cy,
        x: event.x - cx,
        y: event.y - cy
      });
    });
  }


  BendpointSnapping.$inject = [ 'eventBus' ];

  var BendpointsModule = {
    __depends__: [
      DraggingModule,
      RulesModule
    ],
    __init__: [ 'bendpoints', 'bendpointSnapping' ],
    bendpoints: [ 'type', Bendpoints ],
    bendpointMove: [ 'type', BendpointMove ],
    connectionSegmentMove: [ 'type', ConnectionSegmentMove ],
    bendpointSnapping: [ 'type', BendpointSnapping ]
  };

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
  function add$1(collection, element, idx) {

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
   * Fail save get the index of an element in a collection.
   *
   * @param {Array<Object>} collection
   * @param {Object} element
   *
   * @return {Number} the index or -1 if collection or element do
   *                  not exist or the element is not contained.
   */
  function indexOf$1(collection, element) {

    if (!collection || !element) {
      return -1;
    }

    return collection.indexOf(element);
  }

  /**
   * Remove from the beginning of a collection until it is empty.
   *
   * This is a null-safe operation that ensures elements
   * are being removed from the given collection until the
   * collection is empty.
   *
   * The implementation deals with the fact that a remove operation
   * may touch, i.e. remove multiple elements in the collection
   * at a time.
   *
   * @param {Array<Object>} [collection]
   * @param {Function} removeFn
   *
   * @return {Array<Object>} the cleared collection
   */
  function saveClear(collection, removeFn) {

    if (typeof removeFn !== 'function') {
      throw new Error('removeFn iterator must be a function');
    }

    if (!collection) {
      return;
    }

    var e;

    while ((e = collection[0])) {
      removeFn(e);
    }

    return collection;
  }

  var DEFAULT_PRIORITY = 1000;

  /**
   * A utility that can be used to plug-in into the command execution for
   * extension and/or validation.
   *
   * @param {EventBus} eventBus
   *
   * @example
   *
   * import inherits from 'inherits';
   *
   * import CommandInterceptor from 'diagram-js/lib/command/CommandInterceptor';
   *
   * function CommandLogger(eventBus) {
   *   CommandInterceptor.call(this, eventBus);
   *
   *   this.preExecute(function(event) {
   *     console.log('command pre-execute', event);
   *   });
   * }
   *
   * inherits(CommandLogger, CommandInterceptor);
   *
   */
  function CommandInterceptor(eventBus) {
    this._eventBus = eventBus;
  }

  CommandInterceptor.$inject = [ 'eventBus' ];

  function unwrapEvent(fn, that) {
    return function(event) {
      return fn.call(that || null, event.context, event.command, event);
    };
  }

  /**
   * Register an interceptor for a command execution
   *
   * @param {String|Array<String>} [events] list of commands to register on
   * @param {String} [hook] command hook, i.e. preExecute, executed to listen on
   * @param {Number} [priority] the priority on which to hook into the execution
   * @param {Function} handlerFn interceptor to be invoked with (event)
   * @param {Boolean} unwrap if true, unwrap the event and pass (context, command, event) to the
   *                          listener instead
   * @param {Object} [that] Pass context (`this`) to the handler function
   */
  CommandInterceptor.prototype.on = function(events, hook, priority, handlerFn, unwrap, that) {

    if (isFunction(hook) || isNumber(hook)) {
      that = unwrap;
      unwrap = handlerFn;
      handlerFn = priority;
      priority = hook;
      hook = null;
    }

    if (isFunction(priority)) {
      that = unwrap;
      unwrap = handlerFn;
      handlerFn = priority;
      priority = DEFAULT_PRIORITY;
    }

    if (isObject(unwrap)) {
      that = unwrap;
      unwrap = false;
    }

    if (!isFunction(handlerFn)) {
      throw new Error('handlerFn must be a function');
    }

    if (!isArray(events)) {
      events = [ events ];
    }

    var eventBus = this._eventBus;

    forEach(events, function(event) {
      // concat commandStack(.event)?(.hook)?
      var fullEvent = [ 'commandStack', event, hook ].filter(function(e) { return e; }).join('.');

      eventBus.on(fullEvent, priority, unwrap ? unwrapEvent(handlerFn, that) : handlerFn, that);
    });
  };


  var hooks = [
    'canExecute',
    'preExecute',
    'preExecuted',
    'execute',
    'executed',
    'postExecute',
    'postExecuted',
    'revert',
    'reverted'
  ];

  /*
   * Install hook shortcuts
   *
   * This will generate the CommandInterceptor#(preExecute|...|reverted) methods
   * which will in term forward to CommandInterceptor#on.
   */
  forEach(hooks, function(hook) {

    /**
     * {canExecute|preExecute|preExecuted|execute|executed|postExecute|postExecuted|revert|reverted}
     *
     * A named hook for plugging into the command execution
     *
     * @param {String|Array<String>} [events] list of commands to register on
     * @param {Number} [priority] the priority on which to hook into the execution
     * @param {Function} handlerFn interceptor to be invoked with (event)
     * @param {Boolean} [unwrap=false] if true, unwrap the event and pass (context, command, event) to the
     *                          listener instead
     * @param {Object} [that] Pass context (`this`) to the handler function
     */
    CommandInterceptor.prototype[hook] = function(events, priority, handlerFn, unwrap, that) {

      if (isFunction(events) || isNumber(events)) {
        that = unwrap;
        unwrap = handlerFn;
        handlerFn = priority;
        priority = events;
        events = null;
      }

      this.on(events, hook, priority, handlerFn, unwrap, that);
    };
  });

  var LOW_PRIORITY$7 = 250,
      HIGH_PRIORITY$1 = 1400;


  /**
   * A handler that makes sure labels are properly moved with
   * their label targets.
   *
   * @param {didi.Injector} injector
   * @param {EventBus} eventBus
   * @param {Modeling} modeling
   */
  function LabelSupport(injector, eventBus, modeling) {

    CommandInterceptor.call(this, eventBus);

    var movePreview = injector.get('movePreview', false);

    // remove labels from the collection that are being
    // moved with other elements anyway
    eventBus.on('shape.move.start', HIGH_PRIORITY$1, function(e) {

      var context = e.context,
          shapes = context.shapes,
          validatedShapes = context.validatedShapes;

      context.shapes = removeLabels(shapes);
      context.validatedShapes = removeLabels(validatedShapes);
    });

    // add labels to visual's group
    movePreview && eventBus.on('shape.move.start', LOW_PRIORITY$7, function(e) {

      var context = e.context,
          shapes = context.shapes;

      var labels = [];

      forEach(shapes, function(element) {

        forEach(element.labels, function(label) {

          if (!label.hidden && context.shapes.indexOf(label) === -1) {
            labels.push(label);
          }

          if (element.labelTarget) {
            labels.push(element);
          }
        });
      });

      forEach(labels, function(label) {
        movePreview.makeDraggable(context, label, true);
      });

    });

    // add all labels to move closure
    this.preExecuted('elements.move', HIGH_PRIORITY$1, function(e) {
      var context = e.context,
          closure = context.closure,
          enclosedElements = closure.enclosedElements;

      var enclosedLabels = [];

      // find labels that are not part of
      // move closure yet and add them
      forEach(enclosedElements, function(element) {
        forEach(element.labels, function(label) {

          if (!enclosedElements[label.id]) {
            enclosedLabels.push(label);
          }
        });
      });

      closure.addAll(enclosedLabels);
    });


    this.preExecute([
      'connection.delete',
      'shape.delete'
    ], function(e) {

      var context = e.context,
          element = context.connection || context.shape;

      saveClear(element.labels, function(label) {
        modeling.removeShape(label, { nested: true });
      });
    });


    this.execute('shape.delete', function(e) {

      var context = e.context,
          shape = context.shape,
          labelTarget = shape.labelTarget;

      // unset labelTarget
      if (labelTarget) {
        context.labelTargetIndex = indexOf$1(labelTarget.labels, shape);
        context.labelTarget = labelTarget;

        shape.labelTarget = null;
      }
    });

    this.revert('shape.delete', function(e) {

      var context = e.context,
          shape = context.shape,
          labelTarget = context.labelTarget,
          labelTargetIndex = context.labelTargetIndex;

      // restore labelTarget
      if (labelTarget) {
        add$1(labelTarget.labels, shape, labelTargetIndex);

        shape.labelTarget = labelTarget;
      }
    });

  }

  inherits_browser(LabelSupport, CommandInterceptor);

  LabelSupport.$inject = [
    'injector',
    'eventBus',
    'modeling'
  ];


  /**
   * Return a filtered list of elements that do not
   * contain attached elements with hosts being part
   * of the selection.
   *
   * @param  {Array<djs.model.Base>} elements
   *
   * @return {Array<djs.model.Base>} filtered
   */
  function removeLabels(elements) {

    return filter(elements, function(element) {

      // filter out labels that are move together
      // with their label targets
      return elements.indexOf(element.labelTarget) === -1;
    });
  }

  var LabelSupportModule = {
    __init__: [ 'labelSupport'],
    labelSupport: [ 'type', LabelSupport ]
  };

  /**
   * Adds change support to the diagram, including
   *
   * <ul>
   *   <li>redrawing shapes and connections on change</li>
   * </ul>
   *
   * @param {EventBus} eventBus
   * @param {Canvas} canvas
   * @param {ElementRegistry} elementRegistry
   * @param {GraphicsFactory} graphicsFactory
   */
  function ChangeSupport(
      eventBus, canvas, elementRegistry,
      graphicsFactory) {


    // redraw shapes / connections on change

    eventBus.on('element.changed', function(event) {

      var element = event.element;

      // element might have been deleted and replaced by new element with same ID
      // thus check for parent of element except for root element
      if (element.parent || element === canvas.getRootElement()) {
        event.gfx = elementRegistry.getGraphics(element);
      }

      // shape + gfx may have been deleted
      if (!event.gfx) {
        return;
      }

      eventBus.fire(getType(element) + '.changed', event);
    });

    eventBus.on('elements.changed', function(event) {

      var elements = event.elements;

      elements.forEach(function(e) {
        eventBus.fire('element.changed', { element: e });
      });

      graphicsFactory.updateContainments(elements);
    });

    eventBus.on('shape.changed', function(event) {
      graphicsFactory.update('shape', event.element, event.gfx);
    });

    eventBus.on('connection.changed', function(event) {
      graphicsFactory.update('connection', event.element, event.gfx);
    });
  }

  ChangeSupport.$inject = [
    'eventBus',
    'canvas',
    'elementRegistry',
    'graphicsFactory'
  ];

  var ChangeSupportModule = {
    __init__: [ 'changeSupport'],
    changeSupport: [ 'type', ChangeSupport ]
  };

  var max$1 = Math.max,
      min = Math.min;

  var DEFAULT_CHILD_BOX_PADDING = 20;

  /**
   * Resize the given bounds by the specified delta from a given anchor point.
   *
   * @param {Bounds} bounds the bounding box that should be resized
   * @param {String} direction in which the element is resized (nw, ne, se, sw)
   * @param {Point} delta of the resize operation
   *
   * @return {Bounds} resized bounding box
   */
  function resizeBounds(bounds, direction, delta) {

    var dx = delta.x,
        dy = delta.y;

    switch (direction) {

    case 'nw':
      return {
        x: bounds.x + dx,
        y: bounds.y + dy,
        width: bounds.width - dx,
        height: bounds.height - dy
      };

    case 'sw':
      return {
        x: bounds.x + dx,
        y: bounds.y,
        width: bounds.width - dx,
        height: bounds.height + dy
      };

    case 'ne':
      return {
        x: bounds.x,
        y: bounds.y + dy,
        width: bounds.width + dx,
        height: bounds.height - dy
      };

    case 'se':
      return {
        x: bounds.x,
        y: bounds.y,
        width: bounds.width + dx,
        height: bounds.height + dy
      };

    default:
      throw new Error('unrecognized direction: ' + direction);
    }
  }


  function applyConstraints(attr, trbl, resizeConstraints) {

    var value = trbl[attr],
        minValue = resizeConstraints.min && resizeConstraints.min[attr],
        maxValue = resizeConstraints.max && resizeConstraints.max[attr];

    if (isNumber(minValue)) {
      value = (/top|left/.test(attr) ? min : max$1)(value, minValue);
    }

    if (isNumber(maxValue)) {
      value = (/top|left/.test(attr) ? max$1 : min)(value, maxValue);
    }

    return value;
  }

  function ensureConstraints(currentBounds, resizeConstraints) {

    if (!resizeConstraints) {
      return currentBounds;
    }

    var currentTrbl = asTRBL(currentBounds);

    return asBounds({
      top: applyConstraints('top', currentTrbl, resizeConstraints),
      right: applyConstraints('right', currentTrbl, resizeConstraints),
      bottom: applyConstraints('bottom', currentTrbl, resizeConstraints),
      left: applyConstraints('left', currentTrbl, resizeConstraints)
    });
  }


  function getMinResizeBounds(direction, currentBounds, minDimensions, childrenBounds) {

    var currentBox = asTRBL(currentBounds);

    var minBox = {
      top: /n/.test(direction) ? currentBox.bottom - minDimensions.height : currentBox.top,
      left: /w/.test(direction) ? currentBox.right - minDimensions.width : currentBox.left,
      bottom: /s/.test(direction) ? currentBox.top + minDimensions.height : currentBox.bottom,
      right: /e/.test(direction) ? currentBox.left + minDimensions.width : currentBox.right
    };

    var childrenBox = childrenBounds ? asTRBL(childrenBounds) : minBox;

    var combinedBox = {
      top: min(minBox.top, childrenBox.top),
      left: min(minBox.left, childrenBox.left),
      bottom: max$1(minBox.bottom, childrenBox.bottom),
      right: max$1(minBox.right, childrenBox.right)
    };

    return asBounds(combinedBox);
  }

  function asPadding(mayBePadding, defaultValue) {
    if (typeof mayBePadding !== 'undefined') {
      return mayBePadding;
    } else {
      return DEFAULT_CHILD_BOX_PADDING;
    }
  }

  function addPadding(bbox, padding) {
    var left, right, top, bottom;

    if (typeof padding === 'object') {
      left = asPadding(padding.left);
      right = asPadding(padding.right);
      top = asPadding(padding.top);
      bottom = asPadding(padding.bottom);
    } else {
      left = right = top = bottom = asPadding(padding);
    }

    return {
      x: bbox.x - left,
      y: bbox.y - top,
      width: bbox.width + left + right,
      height: bbox.height + top + bottom
    };
  }


  /**
   * Is the given element part of the resize
   * targets min boundary box?
   *
   * This is the default implementation which excludes
   * connections and labels.
   *
   * @param {djs.model.Base} element
   */
  function isBBoxChild(element) {

    // exclude connections
    if (element.waypoints) {
      return false;
    }

    // exclude labels
    if (element.type === 'label') {
      return false;
    }

    return true;
  }

  /**
   * Return children bounding computed from a shapes children
   * or a list of prefiltered children.
   *
   * @param  {djs.model.Shape|Array<djs.model.Shape>} shapeOrChildren
   * @param  {Number|Object} padding
   *
   * @return {Bounds}
   */
  function computeChildrenBBox(shapeOrChildren, padding) {

    var elements;

    // compute based on shape
    if (shapeOrChildren.length === undefined) {
      // grab all the children that are part of the
      // parents children box
      elements = filter(shapeOrChildren.children, isBBoxChild);

    } else {
      elements = shapeOrChildren;
    }

    if (elements.length) {
      return addPadding(getBBox(elements), padding);
    }
  }

  var DEFAULT_MIN_WIDTH = 10;


  /**
   * A component that provides resizing of shapes on the canvas.
   *
   * The following components are part of shape resize:
   *
   *  * adding resize handles,
   *  * creating a visual during resize
   *  * checking resize rules
   *  * committing a change once finished
   *
   *
   * ## Customizing
   *
   * It's possible to customize the resizing behaviour by intercepting 'resize.start'
   * and providing the following parameters through the 'context':
   *
   *   * minDimensions ({ width, height }): minimum shape dimensions
   *
   *   * childrenBoxPadding ({ left, top, bottom, right } || number):
   *     gap between the minimum bounding box and the container
   *
   * f.ex:
   *
   * ```javascript
   * eventBus.on('resize.start', 1500, function(event) {
   *   var context = event.context,
   *
   *  context.minDimensions = { width: 140, height: 120 };
   *
   *  // Passing general padding
   *  context.childrenBoxPadding = 30;
   *
   *  // Passing padding to a specific side
   *  context.childrenBoxPadding.left = 20;
   * });
   * ```
   */
  function Resize(eventBus, rules, modeling, dragging) {

    this._dragging = dragging;
    this._rules = rules;

    var self = this;


    /**
     * Handle resize move by specified delta.
     *
     * @param {Object} context
     * @param {Point delta
     */
    function handleMove(context, delta) {

      var shape = context.shape,
          direction = context.direction,
          resizeConstraints = context.resizeConstraints,
          newBounds;

      context.delta = delta;

      newBounds = resizeBounds(shape, direction, delta);

      // ensure constraints during resize
      context.newBounds = ensureConstraints(newBounds, resizeConstraints);

      // update + cache executable state
      context.canExecute = self.canResize(context);
    }

    /**
     * Handle resize start.
     *
     * @param  {Object} context
     */
    function handleStart(context) {

      var resizeConstraints = context.resizeConstraints,
          // evaluate minBounds for backwards compatibility
          minBounds = context.minBounds;

      if (resizeConstraints !== undefined) {
        return;
      }

      if (minBounds === undefined) {
        minBounds = self.computeMinResizeBox(context);
      }

      context.resizeConstraints = {
        min: asTRBL(minBounds)
      };
    }

    /**
     * Handle resize end.
     *
     * @param  {Object} context
     */
    function handleEnd(context) {
      var shape = context.shape,
          canExecute = context.canExecute,
          newBounds = context.newBounds;

      if (canExecute) {
        // ensure we have actual pixel values for new bounds
        // (important when zoom level was > 1 during move)
        newBounds = roundBounds(newBounds);

        // perform the actual resize
        modeling.resizeShape(shape, newBounds);
      }
    }


    eventBus.on('resize.start', function(event) {
      handleStart(event.context);
    });

    eventBus.on('resize.move', function(event) {
      var delta = {
        x: event.dx,
        y: event.dy
      };

      handleMove(event.context, delta);
    });

    eventBus.on('resize.end', function(event) {
      handleEnd(event.context);
    });

  }


  Resize.prototype.canResize = function(context) {
    var rules = this._rules;

    var ctx = pick(context, [ 'newBounds', 'shape', 'delta', 'direction' ]);

    return rules.allowed('shape.resize', ctx);
  };

  /**
   * Activate a resize operation.
   *
   * You may specify additional contextual information and must specify a
   * resize direction during activation of the resize event.
   *
   * @param {MouseEvent} event
   * @param {djs.model.Shape} shape
   * @param {Object|String} contextOrDirection
   */
  Resize.prototype.activate = function(event, shape, contextOrDirection) {
    var dragging = this._dragging,
        context,
        direction;

    if (typeof contextOrDirection === 'string') {
      contextOrDirection = {
        direction: contextOrDirection
      };
    }

    context = assign({ shape: shape }, contextOrDirection);

    direction = context.direction;

    if (!direction) {
      throw new Error('must provide a direction (nw|se|ne|sw)');
    }

    dragging.init(event, 'resize', {
      autoActivate: true,
      cursor: 'resize-' + (/nw|se/.test(direction) ? 'nwse' : 'nesw'),
      data: {
        shape: shape,
        context: context
      }
    });
  };

  Resize.prototype.computeMinResizeBox = function(context) {
    var shape = context.shape,
        direction = context.direction,
        minDimensions,
        childrenBounds;

    minDimensions = context.minDimensions || {
      width: DEFAULT_MIN_WIDTH,
      height: DEFAULT_MIN_WIDTH
    };

    // get children bounds
    childrenBounds = computeChildrenBBox(shape, context.childrenBoxPadding);

    // get correct minimum bounds from given resize direction
    // basically ensures that the minBounds is max(childrenBounds, minDimensions)
    return getMinResizeBounds(direction, shape, minDimensions, childrenBounds);
  };


  Resize.$inject = [
    'eventBus',
    'rules',
    'modeling',
    'dragging'
  ];

  var MARKER_RESIZING = 'djs-resizing',
      MARKER_RESIZE_NOT_OK = 'resize-not-ok';

  var LOW_PRIORITY$8 = 500;


  /**
   * Provides previews for resizing shapes when resizing.
   *
   * @param {EventBus} eventBus
   * @param {Canvas} canvas
   * @param {PreviewSupport} previewSupport
   */
  function ResizePreview(eventBus, canvas, previewSupport) {

    /**
     * Update resizer frame.
     *
     * @param {Object} context
     */
    function updateFrame(context) {

      var shape = context.shape,
          bounds = context.newBounds,
          frame = context.frame;

      if (!frame) {
        frame = context.frame = previewSupport.addFrame(shape, canvas.getDefaultLayer());

        canvas.addMarker(shape, MARKER_RESIZING);
      }

      if (bounds.width > 5) {
        attr$1(frame, { x: bounds.x, width: bounds.width });
      }

      if (bounds.height > 5) {
        attr$1(frame, { y: bounds.y, height: bounds.height });
      }

      if (context.canExecute) {
        classes$1(frame).remove(MARKER_RESIZE_NOT_OK);
      } else {
        classes$1(frame).add(MARKER_RESIZE_NOT_OK);
      }
    }

    /**
     * Remove resizer frame.
     *
     * @param {Object} context
     */
    function removeFrame(context) {
      var shape = context.shape,
          frame = context.frame;

      if (frame) {
        remove$1(context.frame);
      }

      canvas.removeMarker(shape, MARKER_RESIZING);
    }

    // add and update previews
    eventBus.on('resize.move', LOW_PRIORITY$8, function(event) {
      updateFrame(event.context);
    });

    // remove previews
    eventBus.on('resize.cleanup', function(event) {
      removeFrame(event.context);
    });

  }

  ResizePreview.$inject = [
    'eventBus',
    'canvas',
    'previewSupport'
  ];

  var HANDLE_OFFSET = -2,
      HANDLE_SIZE = 5,
      HANDLE_HIT_SIZE = 20;

  var CLS_RESIZER = 'djs-resizer';


  /**
   * This component is responsible for adding resize handles.
   *
   * @param {EventBus} eventBus
   * @param {Canvas} canvas
   * @param {Selection} selection
   * @param {Resize} resize
   */
  function ResizeHandles(eventBus, canvas, selection, resize) {

    this._resize = resize;
    this._canvas = canvas;

    var self = this;

    eventBus.on('selection.changed', function(e) {
      var newSelection = e.newSelection;

      // remove old selection markers
      self.removeResizers();

      // add new selection markers ONLY if single selection
      if (newSelection.length === 1) {
        forEach(newSelection, bind(self.addResizer, self));
      }
    });

    eventBus.on('shape.changed', function(e) {
      var shape = e.element;

      if (selection.isSelected(shape)) {
        self.removeResizers();

        self.addResizer(shape);
      }
    });
  }


  ResizeHandles.prototype.makeDraggable = function(element, gfx, direction) {
    var resize = this._resize;

    function startResize(event$$1) {
      // only trigger on left mouse button
      if (isPrimaryButton(event$$1)) {
        resize.activate(event$$1, element, direction);
      }
    }

    componentEvent.bind(gfx, 'mousedown', startResize);
    componentEvent.bind(gfx, 'touchstart', startResize);
  };


  ResizeHandles.prototype._createResizer = function(element, x, y, rotation, direction) {
    var resizersParent = this._getResizersParent();

    var group = create('g');
    classes$1(group).add(CLS_RESIZER);
    classes$1(group).add(CLS_RESIZER + '-' + element.id);
    classes$1(group).add(CLS_RESIZER + '-' + direction);

    append(resizersParent, group);

    var origin = -HANDLE_SIZE + HANDLE_OFFSET;

    // Create four drag indicators on the outline
    var visual = create('rect');
    attr$1(visual, {
      x: origin,
      y: origin,
      width: HANDLE_SIZE,
      height: HANDLE_SIZE
    });
    classes$1(visual).add(CLS_RESIZER + '-visual');

    append(group, visual);

    var hit = create('rect');
    attr$1(hit, {
      x: origin,
      y: origin,
      width: HANDLE_HIT_SIZE,
      height: HANDLE_HIT_SIZE
    });
    classes$1(hit).add(CLS_RESIZER + '-hit');

    append(group, hit);

    transform$1(group, x, y, rotation);

    return group;
  };

  ResizeHandles.prototype.createResizer = function(element, direction) {
    var resizer;

    var trbl = asTRBL(element);

    if (direction === 'nw') {
      resizer = this._createResizer(element, trbl.left, trbl.top, 0, direction);
    } else if (direction === 'ne') {
      resizer = this._createResizer(element, trbl.right, trbl.top, 90, direction);
    } else if (direction === 'se') {
      resizer = this._createResizer(element, trbl.right, trbl.bottom, 180, direction);
    } else {
      resizer = this._createResizer(element, trbl.left, trbl.bottom, 270, direction);
    }

    this.makeDraggable(element, resizer, direction);
  };

  // resize handles implementation ///////////////////////////////

  /**
   * Add resizers for a given element.
   *
   * @param {djs.model.Shape} shape
   */
  ResizeHandles.prototype.addResizer = function(shape) {
    var resize = this._resize;

    if (!resize.canResize({ shape: shape })) {
      return;
    }

    this.createResizer(shape, 'nw');
    this.createResizer(shape, 'ne');
    this.createResizer(shape, 'se');
    this.createResizer(shape, 'sw');
  };

  /**
   * Remove all resizers
   */
  ResizeHandles.prototype.removeResizers = function() {
    var resizersParent = this._getResizersParent();

    clear$1(resizersParent);
  };

  ResizeHandles.prototype._getResizersParent = function() {
    return this._canvas.getLayer('resizers');
  };

  ResizeHandles.$inject = [
    'eventBus',
    'canvas',
    'selection',
    'resize'
  ];

  var ResizeModule = {
    __depends__: [
      RulesModule,
      DraggingModule,
      PreviewSupportModule
    ],
    __init__: [
      'resize',
      'resizePreview',
      'resizeHandles'
    ],
    resize: [ 'type', Resize ],
    resizePreview: [ 'type', ResizePreview ],
    resizeHandles: [ 'type', ResizeHandles ]
  };

  var min$1 = Math.min,
      max$2 = Math.max;

  function preventDefault$1(e) {
    e.preventDefault();
  }

  function stopPropagation$1(e) {
    e.stopPropagation();
  }

  function isTextNode(node) {
    return node.nodeType === Node.TEXT_NODE;
  }

  function toArray(nodeList) {
    return [].slice.call(nodeList);
  }

  /**
   * Initializes a container for a content editable div.
   *
   * Structure:
   *
   * container
   *   parent
   *     content
   *     resize-handle
   *
   * @param {object} options
   * @param {DOMElement} options.container The DOM element to append the contentContainer to
   * @param {Function} options.keyHandler Handler for key events
   * @param {Function} options.resizeHandler Handler for resize events
   */
  function TextBox(options) {
    this.container = options.container;

    this.parent = domify(
      '<div class="djs-direct-editing-parent">' +
        '<div class="djs-direct-editing-content" contenteditable="true"></div>' +
      '</div>'
    );

    this.content = query('[contenteditable]', this.parent);

    this.keyHandler = options.keyHandler || function() {};
    this.resizeHandler = options.resizeHandler || function() {};

    this.autoResize = bind(this.autoResize, this);
    this.handlePaste = bind(this.handlePaste, this);
  }


  /**
   * Create a text box with the given position, size, style and text content
   *
   * @param {Object} bounds
   * @param {Number} bounds.x absolute x position
   * @param {Number} bounds.y absolute y position
   * @param {Number} [bounds.width] fixed width value
   * @param {Number} [bounds.height] fixed height value
   * @param {Number} [bounds.maxWidth] maximum width value
   * @param {Number} [bounds.maxHeight] maximum height value
   * @param {Number} [bounds.minWidth] minimum width value
   * @param {Number} [bounds.minHeight] minimum height value
   * @param {Object} [style]
   * @param {String} value text content
   *
   * @return {DOMElement} The created content DOM element
   */
  TextBox.prototype.create = function(bounds, style, value, options) {
    var self = this;

    var parent = this.parent,
        content = this.content,
        container = this.container;

    options = this.options = options || {};

    style = this.style = style || {};

    var parentStyle = pick(style, [
      'width',
      'height',
      'maxWidth',
      'maxHeight',
      'minWidth',
      'minHeight',
      'left',
      'top',
      'backgroundColor',
      'position',
      'overflow',
      'border',
      'wordWrap',
      'textAlign',
      'outline',
      'transform'
    ]);

    assign(parent.style, {
      width: bounds.width + 'px',
      height: bounds.height + 'px',
      maxWidth: bounds.maxWidth + 'px',
      maxHeight: bounds.maxHeight + 'px',
      minWidth: bounds.minWidth + 'px',
      minHeight: bounds.minHeight + 'px',
      left: bounds.x + 'px',
      top: bounds.y + 'px',
      backgroundColor: '#ffffff',
      position: 'absolute',
      overflow: 'visible',
      border: '1px solid #ccc',
      boxSizing: 'border-box',
      wordWrap: 'normal',
      textAlign: 'center',
      outline: 'none'
    }, parentStyle);

    var contentStyle = pick(style, [
      'fontFamily',
      'fontSize',
      'fontWeight',
      'lineHeight',
      'padding',
      'paddingTop',
      'paddingRight',
      'paddingBottom',
      'paddingLeft'
    ]);

    assign(content.style, {
      boxSizing: 'border-box',
      width: '100%',
      outline: 'none',
      wordWrap: 'break-word'
    }, contentStyle);

    if (options.centerVertically) {
      assign(content.style, {
        position: 'absolute',
        top: '50%',
        transform: 'translate(0, -50%)'
      }, contentStyle);
    }

    content.innerText = value;

    componentEvent.bind(content, 'keydown', this.keyHandler);
    componentEvent.bind(content, 'mousedown', stopPropagation$1);
    componentEvent.bind(content, 'paste', self.handlePaste);

    if (options.autoResize) {
      componentEvent.bind(content, 'input', this.autoResize);
    }

    if (options.resizable) {
      this.resizable(style);
    }

    container.appendChild(parent);

    // set selection to end of text
    this.setSelection(content.lastChild, content.lastChild && content.lastChild.length);

    return parent;
  };

  /**
   * Intercept paste events to remove formatting from pasted text.
   */
  TextBox.prototype.handlePaste = function(e) {
    var self = this;

    var options = this.options,
        style = this.style;

    e.preventDefault();

    var text;

    if (e.clipboardData) {

      // Chrome, Firefox, Safari
      text = e.clipboardData.getData('text/plain');
    } else {

      // Internet Explorer
      text = window.clipboardData.getData('Text');
    }

    // insertHTML command not supported by Internet Explorer
    var success = document.execCommand('insertHTML', false, text);

    if (!success) {

      // Internet Explorer
      var range = this.getSelection(),
          startContainer = range.startContainer,
          endContainer = range.endContainer,
          startOffset = range.startOffset,
          endOffset = range.endOffset,
          commonAncestorContainer = range.commonAncestorContainer;

      var childNodesArray = toArray(commonAncestorContainer.childNodes);

      var container,
          offset;

      if (isTextNode(commonAncestorContainer)) {
        var containerTextContent = startContainer.textContent;

        startContainer.textContent =
          containerTextContent.substring(0, startOffset)
          + text
          + containerTextContent.substring(endOffset);

        container = startContainer;
        offset = startOffset + text.length;

      } else if (startContainer === this.content && endContainer === this.content) {
        var textNode = document.createTextNode(text);

        this.content.insertBefore(textNode, childNodesArray[startOffset]);

        container = textNode;
        offset = textNode.textContent.length;
      } else {
        var startContainerChildIndex = childNodesArray.indexOf(startContainer),
            endContainerChildIndex = childNodesArray.indexOf(endContainer);

        childNodesArray.forEach(function(childNode, index) {

          if (index === startContainerChildIndex) {
            childNode.textContent =
              startContainer.textContent.substring(0, startOffset) +
              text +
              endContainer.textContent.substring(endOffset);
          } else if (index > startContainerChildIndex && index <= endContainerChildIndex) {
            remove(childNode);
          }
        });

        container = startContainer;
        offset = startOffset + text.length;
      }

      if (container && offset !== undefined) {

        // is necessary in Internet Explorer
        setTimeout(function() {
          self.setSelection(container, offset);
        });
      }
    }

    if (options.autoResize) {
      var hasResized = this.autoResize(style);

      if (hasResized) {
        this.resizeHandler(hasResized);
      }
    }
  };

  /**
   * Automatically resize element vertically to fit its content.
   */
  TextBox.prototype.autoResize = function() {
    var parent = this.parent,
        content = this.content;

    var fontSize = parseInt(this.style.fontSize) || 12;

    if (content.scrollHeight > parent.offsetHeight ||
        content.scrollHeight < parent.offsetHeight - fontSize) {
      var bounds = parent.getBoundingClientRect();

      var height = content.scrollHeight;
      parent.style.height = height + 'px';

      this.resizeHandler({
        width: bounds.width,
        height: bounds.height,
        dx: 0,
        dy: height - bounds.height
      });
    }
  };

  /**
   * Make an element resizable by adding a resize handle.
   */
  TextBox.prototype.resizable = function() {
    var self = this;

    var parent = this.parent,
        resizeHandle = this.resizeHandle;

    var minWidth = parseInt(this.style.minWidth) || 0,
        minHeight = parseInt(this.style.minHeight) || 0,
        maxWidth = parseInt(this.style.maxWidth) || Infinity,
        maxHeight = parseInt(this.style.maxHeight) || Infinity;

    if (!resizeHandle) {
      resizeHandle = this.resizeHandle = domify(
        '<div class="djs-direct-editing-resize-handle"></div>'
      );

      var startX, startY, startWidth, startHeight;

      var onMouseDown = function(e) {
        preventDefault$1(e);
        stopPropagation$1(e);

        startX = e.clientX;
        startY = e.clientY;

        var bounds = parent.getBoundingClientRect();

        startWidth = bounds.width;
        startHeight = bounds.height;

        componentEvent.bind(document, 'mousemove', onMouseMove);
        componentEvent.bind(document, 'mouseup', onMouseUp);
      };

      var onMouseMove = function(e) {
        preventDefault$1(e);
        stopPropagation$1(e);

        var newWidth = min$1(max$2(startWidth + e.clientX - startX, minWidth), maxWidth);
        var newHeight = min$1(max$2(startHeight + e.clientY - startY, minHeight), maxHeight);

        parent.style.width = newWidth + 'px';
        parent.style.height = newHeight + 'px';

        self.resizeHandler({
          width: startWidth,
          height: startHeight,
          dx: e.clientX - startX,
          dy: e.clientY - startY
        });
      };

      var onMouseUp = function(e) {
        preventDefault$1(e);
        stopPropagation$1(e);

        componentEvent.unbind(document,'mousemove', onMouseMove, false);
        componentEvent.unbind(document, 'mouseup', onMouseUp, false);
      };

      componentEvent.bind(resizeHandle, 'mousedown', onMouseDown);
    }

    assign(resizeHandle.style, {
      position: 'absolute',
      bottom: '0px',
      right: '0px',
      cursor: 'nwse-resize',
      width: '0',
      height: '0',
      borderTop: (parseInt(this.style.fontSize) / 4 || 3) + 'px solid transparent',
      borderRight: (parseInt(this.style.fontSize) / 4 || 3) + 'px solid #ccc',
      borderBottom: (parseInt(this.style.fontSize) / 4 || 3) + 'px solid #ccc',
      borderLeft: (parseInt(this.style.fontSize) / 4 || 3) + 'px solid transparent'
    });

    parent.appendChild(resizeHandle);
  };


  /**
   * Clear content and style of the textbox, unbind listeners and
   * reset CSS style.
   */
  TextBox.prototype.destroy = function() {
    var parent = this.parent,
        content = this.content,
        resizeHandle = this.resizeHandle;

    // clear content
    content.innerText = '';

    // clear styles
    parent.removeAttribute('style');
    content.removeAttribute('style');

    componentEvent.unbind(content, 'keydown', this.keyHandler);
    componentEvent.unbind(content, 'mousedown', stopPropagation$1);
    componentEvent.unbind(content, 'input', this.autoResize);
    componentEvent.unbind(content, 'paste', this.handlePaste);

    if (resizeHandle) {
      resizeHandle.removeAttribute('style');

      remove(resizeHandle);
    }

    remove(parent);
  };


  TextBox.prototype.getValue = function() {
    return this.content.innerText;
  };


  TextBox.prototype.getSelection = function() {
    var selection = window.getSelection(),
        range = selection.getRangeAt(0);

    return range;
  };


  TextBox.prototype.setSelection = function(container, offset) {
    var range = document.createRange();

    if (container === null) {
      range.selectNodeContents(this.content);
    } else {
      range.setStart(container, offset);
      range.setEnd(container, offset);
    }

    var selection = window.getSelection();

    selection.removeAllRanges();
    selection.addRange(range);
  };

  /**
   * A direct editing component that allows users
   * to edit an elements text directly in the diagram
   *
   * @param {EventBus} eventBus the event bus
   */
  function DirectEditing(eventBus, canvas) {

    this._eventBus = eventBus;

    this._providers = [];
    this._textbox = new TextBox({
      container: canvas.getContainer(),
      keyHandler: bind(this._handleKey, this),
      resizeHandler: bind(this._handleResize, this)
    });
  }

  DirectEditing.$inject = [ 'eventBus', 'canvas' ];


  /**
   * Register a direct editing provider

   * @param {Object} provider the provider, must expose an #activate(element) method that returns
   *                          an activation context ({ bounds: {x, y, width, height }, text }) if
   *                          direct editing is available for the given element.
   *                          Additionally the provider must expose a #update(element, value) method
   *                          to receive direct editing updates.
   */
  DirectEditing.prototype.registerProvider = function(provider) {
    this._providers.push(provider);
  };


  /**
   * Returns true if direct editing is currently active
   *
   * @return {Boolean}
   */
  DirectEditing.prototype.isActive = function() {
    return !!this._active;
  };


  /**
   * Cancel direct editing, if it is currently active
   */
  DirectEditing.prototype.cancel = function() {
    if (!this._active) {
      return;
    }

    this._fire('cancel');
    this.close();
  };


  DirectEditing.prototype._fire = function(event, context) {
    this._eventBus.fire('directEditing.' + event, context || { active: this._active });
  };

  DirectEditing.prototype.close = function() {
    this._textbox.destroy();

    this._fire('deactivate');

    this._active = null;

    this.resizable = undefined;
  };


  DirectEditing.prototype.complete = function() {

    var active = this._active;

    if (!active) {
      return;
    }

    var text = this.getValue();

    var bounds = this.$textbox.getBoundingClientRect();

    if (text !== active.context.text || this.resizable) {
      active.provider.update(active.element, text, active.context.text, {
        x: bounds.top,
        y: bounds.left,
        width: bounds.width,
        height: bounds.height
      });
    }

    this._fire('complete');

    this.close();
  };


  DirectEditing.prototype.getValue = function() {
    return this._textbox.getValue();
  };


  DirectEditing.prototype._handleKey = function(e) {

    // stop bubble
    e.stopPropagation();

    var key = e.keyCode || e.charCode;

    // ESC
    if (key === 27) {
      e.preventDefault();
      return this.cancel();
    }

    // Enter
    if (key === 13 && !e.shiftKey) {
      e.preventDefault();
      return this.complete();
    }
  };


  DirectEditing.prototype._handleResize = function(event) {
    this._fire('resize', event);
  };


  /**
   * Activate direct editing on the given element
   *
   * @param {Object} ElementDescriptor the descriptor for a shape or connection
   * @return {Boolean} true if the activation was possible
   */
  DirectEditing.prototype.activate = function(element) {
    if (this.isActive()) {
      this.cancel();
    }

    // the direct editing context
    var context;

    var provider = find(this._providers, function(p) {
      return (context = p.activate(element)) ? p : null;
    });

    // check if activation took place
    if (context) {
      this.$textbox = this._textbox.create(
        context.bounds,
        context.style,
        context.text,
        context.options
      );

      this._active = {
        element: element,
        context: context,
        provider: provider
      };

      if (context.options && context.options.resizable) {
        this.resizable = true;
      }

      this._fire('activate');
    }

    return !!context;
  };

  var DirectEditingModule = {
    __depends__: [
      InteractionEventsModule
    ],
    __init__: [ 'directEditing' ],
    directEditing: [ 'type', DirectEditing ]
  };

  var MARKER_OK$3 = 'connect-ok',
      MARKER_NOT_OK$3 = 'connect-not-ok';


  function Connect(
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

      attr$1(visual, { 'points': [ waypoints[0].x, waypoints[0].y, waypoints[1].x, waypoints[1].y ] });
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

      canvas.addMarker(hover, canExecute ? MARKER_OK$3 : MARKER_NOT_OK$3);
    });

    eventBus.on([ 'connect.out', 'connect.cleanup' ], function(event) {
      var context = event.context;

      if (context.target) {
        canvas.removeMarker(context.target, context.canExecute ? MARKER_OK$3 : MARKER_NOT_OK$3);
      }

      context.target = null;
      context.canExecute = false;
    });

    eventBus.on('connect.cleanup', function(event) {
      var context = event.context;

      if (context.visual) {
        remove$1(context.visual);
      }
    });

    eventBus.on('connect.start', function(event) {
      var context = event.context,
          visual;

      visual = create('polyline');
      attr$1(visual, {
        'stroke': '#333',
        'strokeDasharray': [ 1 ],
        'strokeWidth': 2,
        'pointer-events': 'none'
      });

      append(canvas.getDefaultLayer(), visual);

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

  var ConnectModule = {
    __depends__: [
      SelectionModule,
      RulesModule,
      DraggingModule
    ],
    connect: [ 'type', Connect ]
  };

  /**
   * A service that offers un- and redoable execution of commands.
   *
   * The command stack is responsible for executing modeling actions
   * in a un- and redoable manner. To do this it delegates the actual
   * command execution to {@link CommandHandler}s.
   *
   * Command handlers provide {@link CommandHandler#execute(ctx)} and
   * {@link CommandHandler#revert(ctx)} methods to un- and redo a command
   * identified by a command context.
   *
   *
   * ## Life-Cycle events
   *
   * In the process the command stack fires a number of life-cycle events
   * that other components to participate in the command execution.
   *
   *    * preExecute
   *    * preExecuted
   *    * execute
   *    * executed
   *    * postExecute
   *    * postExecuted
   *    * revert
   *    * reverted
   *
   * A special event is used for validating, whether a command can be
   * performed prior to its execution.
   *
   *    * canExecute
   *
   * Each of the events is fired as `commandStack.{eventName}` and
   * `commandStack.{commandName}.{eventName}`, respectively. This gives
   * components fine grained control on where to hook into.
   *
   * The event object fired transports `command`, the name of the
   * command and `context`, the command context.
   *
   *
   * ## Creating Command Handlers
   *
   * Command handlers should provide the {@link CommandHandler#execute(ctx)}
   * and {@link CommandHandler#revert(ctx)} methods to implement
   * redoing and undoing of a command.
   *
   * A command handler _must_ ensure undo is performed properly in order
   * not to break the undo chain. It must also return the shapes that
   * got changed during the `execute` and `revert` operations.
   *
   * Command handlers may execute other modeling operations (and thus
   * commands) in their `preExecute` and `postExecute` phases. The command
   * stack will properly group all commands together into a logical unit
   * that may be re- and undone atomically.
   *
   * Command handlers must not execute other commands from within their
   * core implementation (`execute`, `revert`).
   *
   *
   * ## Change Tracking
   *
   * During the execution of the CommandStack it will keep track of all
   * elements that have been touched during the command's execution.
   *
   * At the end of the CommandStack execution it will notify interested
   * components via an 'elements.changed' event with all the dirty
   * elements.
   *
   * The event can be picked up by components that are interested in the fact
   * that elements have been changed. One use case for this is updating
   * their graphical representation after moving / resizing or deletion.
   *
   * @see CommandHandler
   *
   * @param {EventBus} eventBus
   * @param {Injector} injector
   */
  function CommandStack(eventBus, injector) {

    /**
     * A map of all registered command handlers.
     *
     * @type {Object}
     */
    this._handlerMap = {};

    /**
     * A stack containing all re/undoable actions on the diagram
     *
     * @type {Array<Object>}
     */
    this._stack = [];

    /**
     * The current index on the stack
     *
     * @type {Number}
     */
    this._stackIdx = -1;

    /**
     * Current active commandStack execution
     *
     * @type {Object}
     */
    this._currentExecution = {
      actions: [],
      dirty: []
    };


    this._injector = injector;
    this._eventBus = eventBus;

    this._uid = 1;

    eventBus.on([
      'diagram.destroy',
      'diagram.clear'
    ], function() {
      this.clear(false);
    }, this);
  }

  CommandStack.$inject = [ 'eventBus', 'injector' ];


  /**
   * Execute a command
   *
   * @param {String} command the command to execute
   * @param {Object} context the environment to execute the command in
   */
  CommandStack.prototype.execute = function(command, context) {
    if (!command) {
      throw new Error('command required');
    }

    var action = { command: command, context: context };

    this._pushAction(action);
    this._internalExecute(action);
    this._popAction(action);
  };


  /**
   * Ask whether a given command can be executed.
   *
   * Implementors may hook into the mechanism on two ways:
   *
   *   * in event listeners:
   *
   *     Users may prevent the execution via an event listener.
   *     It must prevent the default action for `commandStack.(<command>.)canExecute` events.
   *
   *   * in command handlers:
   *
   *     If the method {@link CommandHandler#canExecute} is implemented in a handler
   *     it will be called to figure out whether the execution is allowed.
   *
   * @param  {String} command the command to execute
   * @param  {Object} context the environment to execute the command in
   *
   * @return {Boolean} true if the command can be executed
   */
  CommandStack.prototype.canExecute = function(command, context) {

    var action = { command: command, context: context };

    var handler = this._getHandler(command);

    var result = this._fire(command, 'canExecute', action);

    // handler#canExecute will only be called if no listener
    // decided on a result already
    if (result === undefined) {
      if (!handler) {
        return false;
      }

      if (handler.canExecute) {
        result = handler.canExecute(context);
      }
    }

    return result;
  };


  /**
   * Clear the command stack, erasing all undo / redo history
   */
  CommandStack.prototype.clear = function(emit) {
    this._stack.length = 0;
    this._stackIdx = -1;

    if (emit !== false) {
      this._fire('changed');
    }
  };


  /**
   * Undo last command(s)
   */
  CommandStack.prototype.undo = function() {
    var action = this._getUndoAction(),
        next;

    if (action) {
      this._pushAction(action);

      while (action) {
        this._internalUndo(action);
        next = this._getUndoAction();

        if (!next || next.id !== action.id) {
          break;
        }

        action = next;
      }

      this._popAction();
    }
  };


  /**
   * Redo last command(s)
   */
  CommandStack.prototype.redo = function() {
    var action = this._getRedoAction(),
        next;

    if (action) {
      this._pushAction(action);

      while (action) {
        this._internalExecute(action, true);
        next = this._getRedoAction();

        if (!next || next.id !== action.id) {
          break;
        }

        action = next;
      }

      this._popAction();
    }
  };


  /**
   * Register a handler instance with the command stack
   *
   * @param {String} command
   * @param {CommandHandler} handler
   */
  CommandStack.prototype.register = function(command, handler) {
    this._setHandler(command, handler);
  };


  /**
   * Register a handler type with the command stack
   * by instantiating it and injecting its dependencies.
   *
   * @param {String} command
   * @param {Function} a constructor for a {@link CommandHandler}
   */
  CommandStack.prototype.registerHandler = function(command, handlerCls) {

    if (!command || !handlerCls) {
      throw new Error('command and handlerCls must be defined');
    }

    var handler = this._injector.instantiate(handlerCls);
    this.register(command, handler);
  };

  CommandStack.prototype.canUndo = function() {
    return !!this._getUndoAction();
  };

  CommandStack.prototype.canRedo = function() {
    return !!this._getRedoAction();
  };

  // stack access  //////////////////////

  CommandStack.prototype._getRedoAction = function() {
    return this._stack[this._stackIdx + 1];
  };


  CommandStack.prototype._getUndoAction = function() {
    return this._stack[this._stackIdx];
  };


  // internal functionality //////////////////////

  CommandStack.prototype._internalUndo = function(action) {
    var self = this;

    var command = action.command,
        context = action.context;

    var handler = this._getHandler(command);

    // guard against illegal nested command stack invocations
    this._atomicDo(function() {
      self._fire(command, 'revert', action);

      if (handler.revert) {
        self._markDirty(handler.revert(context));
      }

      self._revertedAction(action);

      self._fire(command, 'reverted', action);
    });
  };


  CommandStack.prototype._fire = function(command, qualifier, event) {
    if (arguments.length < 3) {
      event = qualifier;
      qualifier = null;
    }

    var names = qualifier ? [ command + '.' + qualifier, qualifier ] : [ command ],
        i, name, result;

    event = this._eventBus.createEvent(event);

    for (i = 0; (name = names[i]); i++) {
      result = this._eventBus.fire('commandStack.' + name, event);

      if (event.cancelBubble) {
        break;
      }
    }

    return result;
  };

  CommandStack.prototype._createId = function() {
    return this._uid++;
  };

  CommandStack.prototype._atomicDo = function(fn) {

    var execution = this._currentExecution;

    execution.atomic = true;

    try {
      fn();
    } finally {
      execution.atomic = false;
    }
  };

  CommandStack.prototype._internalExecute = function(action, redo) {
    var self = this;

    var command = action.command,
        context = action.context;

    var handler = this._getHandler(command);

    if (!handler) {
      throw new Error('no command handler registered for <' + command + '>');
    }

    this._pushAction(action);

    if (!redo) {
      this._fire(command, 'preExecute', action);

      if (handler.preExecute) {
        handler.preExecute(context);
      }

      this._fire(command, 'preExecuted', action);
    }

    // guard against illegal nested command stack invocations
    this._atomicDo(function() {

      self._fire(command, 'execute', action);

      if (handler.execute) {
        // actual execute + mark return results as dirty
        self._markDirty(handler.execute(context));
      }

      // log to stack
      self._executedAction(action, redo);

      self._fire(command, 'executed', action);
    });

    if (!redo) {
      this._fire(command, 'postExecute', action);

      if (handler.postExecute) {
        handler.postExecute(context);
      }

      this._fire(command, 'postExecuted', action);
    }

    this._popAction(action);
  };


  CommandStack.prototype._pushAction = function(action) {

    var execution = this._currentExecution,
        actions = execution.actions;

    var baseAction = actions[0];

    if (execution.atomic) {
      throw new Error('illegal invocation in <execute> or <revert> phase (action: ' + action.command + ')');
    }

    if (!action.id) {
      action.id = (baseAction && baseAction.id) || this._createId();
    }

    actions.push(action);
  };


  CommandStack.prototype._popAction = function() {
    var execution = this._currentExecution,
        actions = execution.actions,
        dirty = execution.dirty;

    actions.pop();

    if (!actions.length) {
      this._eventBus.fire('elements.changed', { elements: uniqueBy('id', dirty) });

      dirty.length = 0;

      this._fire('changed');
    }
  };


  CommandStack.prototype._markDirty = function(elements) {
    var execution = this._currentExecution;

    if (!elements) {
      return;
    }

    elements = isArray(elements) ? elements : [ elements ];

    execution.dirty = execution.dirty.concat(elements);
  };


  CommandStack.prototype._executedAction = function(action, redo) {
    var stackIdx = ++this._stackIdx;

    if (!redo) {
      this._stack.splice(stackIdx, this._stack.length, action);
    }
  };


  CommandStack.prototype._revertedAction = function(action) {
    this._stackIdx--;
  };


  CommandStack.prototype._getHandler = function(command) {
    return this._handlerMap[command];
  };

  CommandStack.prototype._setHandler = function(command, handler) {
    if (!command || !handler) {
      throw new Error('command and handler required');
    }

    if (this._handlerMap[command]) {
      throw new Error('overriding handler for command <' + command + '>');
    }

    this._handlerMap[command] = handler;
  };

  var CommandModule = {
    commandStack: [ 'type', CommandStack ]
  };

  function dockingToPoint(docking) {
    // use the dockings actual point and
    // retain the original docking
    return assign({ original: docking.point.original || docking.point }, docking.actual);
  }


  /**
   * A {@link ConnectionDocking} that crops connection waypoints based on
   * the path(s) of the connection source and target.
   *
   * @param {djs.core.ElementRegistry} elementRegistry
   */
  function CroppingConnectionDocking(elementRegistry, graphicsFactory) {
    this._elementRegistry = elementRegistry;
    this._graphicsFactory = graphicsFactory;
  }

  CroppingConnectionDocking.$inject = [ 'elementRegistry', 'graphicsFactory' ];


  /**
   * @inheritDoc ConnectionDocking#getCroppedWaypoints
   */
  CroppingConnectionDocking.prototype.getCroppedWaypoints = function(connection, source, target) {

    source = source || connection.source;
    target = target || connection.target;

    var sourceDocking = this.getDockingPoint(connection, source, true),
        targetDocking = this.getDockingPoint(connection, target);

    var croppedWaypoints = connection.waypoints.slice(sourceDocking.idx + 1, targetDocking.idx);

    croppedWaypoints.unshift(dockingToPoint(sourceDocking));
    croppedWaypoints.push(dockingToPoint(targetDocking));

    return croppedWaypoints;
  };

  /**
   * Return the connection docking point on the specified shape
   *
   * @inheritDoc ConnectionDocking#getDockingPoint
   */
  CroppingConnectionDocking.prototype.getDockingPoint = function(connection, shape, dockStart) {

    var waypoints = connection.waypoints,
        dockingIdx,
        dockingPoint,
        croppedPoint;

    dockingIdx = dockStart ? 0 : waypoints.length - 1;
    dockingPoint = waypoints[dockingIdx];

    croppedPoint = this._getIntersection(shape, connection, dockStart);

    return {
      point: dockingPoint,
      actual: croppedPoint || dockingPoint,
      idx: dockingIdx
    };
  };


  // helpers //////////////////////

  CroppingConnectionDocking.prototype._getIntersection = function(shape, connection, takeFirst) {

    var shapePath = this._getShapePath(shape),
        connectionPath = this._getConnectionPath(connection);

    return getElementLineIntersection(shapePath, connectionPath, takeFirst);
  };

  CroppingConnectionDocking.prototype._getConnectionPath = function(connection) {
    return this._graphicsFactory.getConnectionPath(connection);
  };

  CroppingConnectionDocking.prototype._getShapePath = function(shape) {
    return this._graphicsFactory.getShapePath(shape);
  };

  CroppingConnectionDocking.prototype._getGfx = function(element) {
    return this._elementRegistry.getGraphics(element);
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

  inherits_browser(ElementFactory$1, ElementFactory);

  function ElementFactory$1() {
    ElementFactory.call(this);
  }

  ElementFactory$1.prototype.createEnumeration = function(attrs) {
    var newAttrs = {
      shapeType: 'enumeration',
      businessObject: {}
    };

    assign(newAttrs, attrs);

    return this.createShape(newAttrs);
  };

  ElementFactory$1.prototype.createClass = function(attrs) {
    var newAttrs = {
      shapeType: 'class',
      businessObject: {}
    };

    assign(newAttrs, attrs);

    return this.createShape(newAttrs);
  };

  ElementFactory$1.prototype.createUmlLabel = function(attrs) {
    var newAttrs = {
      shapeType: 'label'
    };

    assign(newAttrs, attrs);

    return this.createLabel(newAttrs);
  };

  ElementFactory$1.prototype.createInheritance = function(attrs) {
    var newAttrs = {
      connectionType: 'inheritance'
    };

    assign(newAttrs, attrs);

    return this.createConnection(newAttrs);
  };

  ElementFactory$1.prototype.createAssociation = function(attrs) {
    var newAttrs = {
      connectionType: 'association'
    };

    assign(newAttrs, attrs);

    return this.createConnection(newAttrs);
  };

  ElementFactory$1.prototype.createAggregation = function(attrs) {
    var newAttrs = {
      connectionType: 'aggregation'
    };

    assign(newAttrs, attrs);

    return this.createConnection(newAttrs);
  };

  ElementFactory$1.prototype.createComposition = function(attrs) {
    var newAttrs = {
      connectionType: 'composition'
    };

    assign(newAttrs, attrs);

    return this.createConnection(newAttrs);
  };

  ElementFactory$1.prototype.createDependency = function(attrs) {
    var newAttrs = {
      connectionType: 'dependency'
    };

    assign(newAttrs, attrs);

    return this.createConnection(newAttrs);
  };

  ElementFactory$1.prototype.createRealization = function(attrs) {
    var newAttrs = {
      connectionType: 'realization'
    };

    assign(newAttrs, attrs);

    return this.createConnection(newAttrs);
  };

  /**
   * A base connection layouter implementation
   * that layouts the connection by directly connecting
   * mid(source) + mid(target).
   */
  function BaseLayouter() {}


  /**
   * Return the new layouted waypoints for the given connection.
   *
   * The connection passed is still unchanged; you may figure out about
   * the new connection start / end via the layout hints provided.
   *
   * @param {djs.model.Connection} connection
   * @param {Object} [hints]
   * @param {Point} [hints.connectionStart]
   * @param {Point} [hints.connectionEnd]
   *
   * @return {Array<Point>} the layouted connection waypoints
   */
  BaseLayouter.prototype.layoutConnection = function(connection, hints) {

    hints = hints || {};

    return [
      hints.connectionStart || getMid(connection.source),
      hints.connectionEnd || getMid(connection.target)
    ];
  };

  var MIN_SEGMENT_LENGTH = 20,
      POINT_ORIENTATION_PADDING = 5;

  var round$5 = Math.round;

  var INTERSECTION_THRESHOLD$1 = 20,
      ORIENTATION_THRESHOLD = {
        'h:h': 20,
        'v:v': 20,
        'h:v': -10,
        'v:h': -10
      };

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

    var xmid = round$5((b.x - a.x) / 2 + a.x),
        ymid = round$5((b.y - a.y) / 2 + a.y);

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

    var xmid = round$5((b.x - a.x) / 2 + a.x),
        ymid = round$5((b.y - a.y) / 2 + a.y);

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


  /**
   * Connect two rectangles using a manhattan layouted connection.
   *
   * @param {Bounds} source source rectangle
   * @param {Bounds} target target rectangle
   * @param {Point} [start] source docking
   * @param {Point} [end] target docking
   *
   * @param {Object} [hints]
   * @param {String} [hints.preserveDocking=source] preserve docking on selected side
   * @param {Array<String>} [hints.preferredLayouts]
   * @param {Point|Boolean} [hints.connectionStart] whether the start changed
   * @param {Point|Boolean} [hints.connectionEnd] whether the end changed
   *
   * @return {Array<Point>} connection points
   */
  function connectRectangles(source, target, start, end, hints) {

    var preferredLayouts = hints && hints.preferredLayouts || [];

    var preferredLayout = without(preferredLayouts, 'straight')[0] || 'h:h';

    var threshold = ORIENTATION_THRESHOLD[preferredLayout] || 0;

    var orientation = getOrientation(source, target, threshold);

    var directions = getDirections(orientation, preferredLayout);

    start = start || getMid(source);
    end = end || getMid(target);

    var directionSplit = directions.split(':');

    // compute actual docking points for start / end
    // this ensures we properly layout only parts of the
    // connection that lies in between the two rectangles
    var startDocking = getDockingPoint(start, source, directionSplit[0], invertOrientation(orientation)),
        endDocking = getDockingPoint(end, target, directionSplit[1], orientation);

    return connectPoints(startDocking, endDocking, directions);
  }


  /**
   * Repair the connection between two rectangles, of which one has been updated.
   *
   * @param {Bounds} source
   * @param {Bounds} target
   * @param {Point} [start]
   * @param {Point} [end]
   * @param {Array<Point>} waypoints
   * @param {Object} [hints]
   * @param {Array<String>} [hints.preferredLayouts] list of preferred layouts
   * @param {Boolean} [hints.connectionStart]
   * @param {Boolean} [hints.connectionEnd]
   *
   * @return {Array<Point>} repaired waypoints
   */
  function repairConnection(source, target, start, end, waypoints, hints) {

    if (isArray(start)) {
      waypoints = start;
      hints = end;

      start = getMid(source);
      end = getMid(target);
    }

    hints = assign({ preferredLayouts: [] }, hints);
    waypoints = waypoints || [];

    var preferredLayouts = hints.preferredLayouts,
        preferStraight = preferredLayouts.indexOf('straight') !== -1,
        repairedWaypoints;

    // just layout non-existing or simple connections
    // attempt to render straight lines, if required

    if (preferStraight) {
      // attempt to layout a straight line
      repairedWaypoints = layoutStraight(source, target, start, end, hints);
    }

    if (!repairedWaypoints) {
      // check if we layout from start or end
      if (hints.connectionEnd) {
        repairedWaypoints = _repairConnectionSide(target, source, end, waypoints.slice().reverse());
        repairedWaypoints = repairedWaypoints && repairedWaypoints.reverse();
      } else
      if (hints.connectionStart) {
        repairedWaypoints = _repairConnectionSide(source, target, start, waypoints);
      } else
      // or whether nothing seems to have changed
      if (waypoints && waypoints.length) {
        repairedWaypoints = waypoints;
      }
    }

    // simply reconnect if nothing else worked
    if (!repairedWaypoints) {
      repairedWaypoints = connectRectangles(source, target, start, end, hints);
    }

    return repairedWaypoints;
  }


  function inRange(a, start, end) {
    return a >= start && a <= end;
  }

  function isInRange(axis, a, b) {
    var size$$1 = {
      x: 'width',
      y: 'height'
    };

    return inRange(a[axis], b[axis], b[axis] + b[size$$1[axis]]);
  }

  /**
   * Layout a straight connection
   *
   * @param {Bounds} source
   * @param {Bounds} target
   * @param {Point} start
   * @param {Point} end
   * @param {Object} [hints]
   *
   * @return {Array<Point>} waypoints if straight layout worked
   */
  function layoutStraight(source, target, start, end, hints) {
    var axis = {},
        primaryAxis,
        orientation;

    orientation = getOrientation(source, target);

    // only layout a straight connection if shapes are
    // horizontally or vertically aligned
    if (!/^(top|bottom|left|right)$/.test(orientation)) {
      return null;
    }

    if (/top|bottom/.test(orientation)) {
      primaryAxis = 'x';
    }

    if (/left|right/.test(orientation)) {
      primaryAxis = 'y';
    }

    if (hints.preserveDocking === 'target') {

      if (!isInRange(primaryAxis, end, source)) {
        return null;
      }

      axis[primaryAxis] = end[primaryAxis];

      return [
        {
          x: axis.x !== undefined ? axis.x : start.x,
          y: axis.y !== undefined ? axis.y : start.y,
          original: {
            x: axis.x !== undefined ? axis.x : start.x,
            y: axis.y !== undefined ? axis.y : start.y
          }
        },
        {
          x: end.x,
          y: end.y
        }
      ];

    } else {

      if (!isInRange(primaryAxis, start, target)) {
        return null;
      }

      axis[primaryAxis] = start[primaryAxis];

      return [
        {
          x: start.x,
          y: start.y
        },
        {
          x: axis.x !== undefined ? axis.x : end.x,
          y: axis.y !== undefined ? axis.y : end.y,
          original: {
            x: axis.x !== undefined ? axis.x : end.x,
            y: axis.y !== undefined ? axis.y : end.y
          }
        }
      ];
    }

  }


  /**
   * Repair a connection from one side that moved.
   *
   * @param {Bounds} moved
   * @param {Bounds} other
   * @param {Point} newDocking
   * @param {Array<Point>} points originalPoints from moved to other
   *
   * @return {Array<Point>} the repaired points between the two rectangles
   */
  function _repairConnectionSide(moved, other, newDocking, points) {

    function needsRelayout(moved, other, points) {

      if (points.length < 3) {
        return true;
      }

      if (points.length > 4) {
        return false;
      }

      // relayout if two points overlap
      // this is most likely due to
      return !!find(points, function(p, idx) {
        var q = points[idx - 1];

        return q && pointDistance(p, q) < 3;
      });
    }

    function repairBendpoint(candidate, oldPeer, newPeer) {

      var alignment = pointsAligned(oldPeer, candidate);

      switch (alignment) {
      case 'v':
        // repair vertical alignment
        return { x: candidate.x, y: newPeer.y };
      case 'h':
        // repair horizontal alignment
        return { x: newPeer.x, y: candidate.y };
      }

      return { x: candidate.x, y: candidate. y };
    }

    function removeOverlapping(points, a, b) {
      var i;

      for (i = points.length - 2; i !== 0; i--) {

        // intersects (?) break, remove all bendpoints up to this one and relayout
        if (pointInRect(points[i], a, INTERSECTION_THRESHOLD$1) ||
            pointInRect(points[i], b, INTERSECTION_THRESHOLD$1)) {

          // return sliced old connection
          return points.slice(i);
        }
      }

      return points;
    }


    // (0) only repair what has layoutable bendpoints

    // (1) if only one bendpoint and on shape moved onto other shapes axis
    //     (horizontally / vertically), relayout

    if (needsRelayout(moved, other, points)) {
      return null;
    }

    var oldDocking = points[0],
        newPoints = points.slice(),
        slicedPoints;

    // (2) repair only last line segment and only if it was layouted before

    newPoints[0] = newDocking;
    newPoints[1] = repairBendpoint(newPoints[1], oldDocking, newDocking);


    // (3) if shape intersects with any bendpoint after repair,
    //     remove all segments up to this bendpoint and repair from there

    slicedPoints = removeOverlapping(newPoints, moved, other);

    if (slicedPoints !== newPoints) {
      return _repairConnectionSide(moved, other, newDocking, slicedPoints);
    }

    return newPoints;
  }


  /**
   * Returns the manhattan directions connecting two rectangles
   * with the given orientation.
   *
   * Will always return the default layout, if it is specific
   * regarding sides already (trbl).
   *
   * @example
   *
   * getDirections('top'); // -> 'v:v'
   * getDirections('intersect'); // -> 't:t'
   *
   * getDirections('top-right', 'v:h'); // -> 'v:h'
   * getDirections('top-right', 'h:h'); // -> 'h:h'
   *
   *
   * @param {String} orientation
   * @param {String} defaultLayout
   *
   * @return {String}
   */
  function getDirections(orientation, defaultLayout) {

    // don't override specific trbl directions
    if (isExplicitDirections(defaultLayout)) {
      return defaultLayout;
    }

    switch (orientation) {
    case 'intersect':
      return 't:t';

    case 'top':
    case 'bottom':
      return 'v:v';

    case 'left':
    case 'right':
      return 'h:h';

    // 'top-left'
    // 'top-right'
    // 'bottom-left'
    // 'bottom-right'
    default:
      return defaultLayout;
    }
  }

  function isValidDirections(directions) {
    return directions && /^h|v|t|r|b|l:h|v|t|r|b|l$/.test(directions);
  }

  function isExplicitDirections(directions) {
    return directions && /t|r|b|l/.test(directions);
  }

  function invertOrientation(orientation) {
    return {
      'top': 'bottom',
      'bottom': 'top',
      'left': 'right',
      'right': 'left',
      'top-left': 'bottom-right',
      'bottom-right': 'top-left',
      'top-right': 'bottom-left',
      'bottom-left': 'top-right',
    }[orientation];
  }

  function getDockingPoint(point, rectangle, dockingDirection, targetOrientation) {

    // ensure we end up with a specific docking direction
    // based on the targetOrientation, if <h|v> is being passed

    if (dockingDirection === 'h') {
      dockingDirection = /left/.test(targetOrientation) ? 'l' : 'r';
    }

    if (dockingDirection === 'v') {
      dockingDirection = /top/.test(targetOrientation) ? 't' : 'b';
    }

    if (dockingDirection === 't') {
      return { original: point, x: point.x, y: rectangle.y };
    }

    if (dockingDirection === 'r') {
      return { original: point, x: rectangle.x + rectangle.width, y: point.y };
    }

    if (dockingDirection === 'b') {
      return { original: point, x: point.x, y: rectangle.y + rectangle.height };
    }

    if (dockingDirection === 'l') {
      return { original: point, x: rectangle.x, y: point.y };
    }

    throw new Error('unexpected dockingDirection: <' + dockingDirection + '>');
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

  inherits_browser(UmlLayouter, BaseLayouter);

  function UmlLayouter() {

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

  inherits_browser(UmlUpdater, CommandInterceptor);

  UmlUpdater.$inject = [
    'eventBus', 'connectionDocking', 'commandStack'
  ];

  function UmlUpdater(eventBus, connectionDocking, commandStack) {
    CommandInterceptor.call(this, eventBus);

    function cropConnection(e) {
      var context = e.context,
      connection;

      if (!context.cropped) {
        connection = context.connection;
        connection.waypoints = connectionDocking.getCroppedWaypoints(connection);
        context.cropped = true;
      }
    }

    this.executed([
      'connection.layout',
      'connection.create',
      'connection.reconnectEnd',
      'connection.reconnectStart'
    ], cropConnection);

    this.postExecuted([
      'connection.layout',
      'connection.reconnectEnd',
      'connection.reconnectStart',
      'connection.updateWaypoints',
      'connection.move'
    ], function(event) {
      commandStack.execute('layout.connection.labels', event.context);
    });

    this.reverted([ 'connection.layout' ], function(event) {
      delete event.context.cropped;
    });

    eventBus.on('element.hover', 1500, function(event) {
      if(event.element instanceof Label && event.element.labelType === 'classifier') {
        return false;
      }
    });

    var delegatedClassifierEvents = ['element.click', 'element.mousedown', 'element.mouseup', 'element.contextmenu', 'element.dblclick' ];

    eventBus.on(delegatedClassifierEvents, 1500, function(event) {
      if(event.element instanceof Label && event.element.labelType === 'classifier') {
        event.stopPropagation();

        eventBus.fire(event.type, {
          element: event.element.parent,
          originalEvent: event.originalEvent,
          srcEvent: event.srcEvent
        });
      }
    });
  }

  UpdateLabelHandler.$inject = [
    'modeling',
    'textRenderer'
  ];

  function UpdateLabelHandler(modeling, textRenderer) {
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

  function getEuclidianDistance(point1, point2) {
    var xDifference = point1.x - point2.x;
    var yDifference = point1.y - point2.y;

    return Math.sqrt(xDifference*xDifference + yDifference*yDifference);
  }
  function getRelativeClosePoint(point1, point2, t) {
    return {
      x: (1-t)*point1.x + t*point2.x,
      y: (1-t)*point1.y + t*point2.y
    }
  }
  function getAbsoluteClosePoint(point1, point2, absoluteDistance) {
    var distance = getEuclidianDistance(point1, point2);

    return getRelativeClosePoint(point1, point2, absoluteDistance / distance);
  }

  LayoutConnectionLabelsHandler.$inject = [
    'modeling'
  ];

  function LayoutConnectionLabelsHandler(modeling) {
    this._modeling = modeling;
  }
  LayoutConnectionLabelsHandler.prototype.preExecute = function(context) {
    var modeling = this._modeling;

    setLabels(context.connection);

    function setLabels(connection) {
      moveSourceLabels(modeling, connection);
      moveTargetLabels(modeling, connection);
    }
    function moveSourceLabels(modeling, connection) {
      var sourceLabels = getSourceLabels(connection);
      var existsMarker = existsAnyMarker(connection, 'source');

      var connectionPartSource = connection.waypoints[0];
      var connectionPartTarget = connection.waypoints[1];

      var direction = getStraightLinePartDirection(connectionPartSource, connectionPartTarget);
      var referencePoint = getReferencePoint(connectionPartSource, connectionPartTarget, direction, existsMarker);

      for(var i=0; i<sourceLabels.length; i++) {
        var label = sourceLabels[i];

        fixReferencePoint(direction, referencePoint, label);

        moveShape(modeling, label, referencePoint);

        adjustReferenceLabelPoint(direction, referencePoint, label);
      }
    }
    function moveShape(modeling, shape, point) {
      modeling.moveShape(shape, {
        x: point.x - shape.x,
        y: point.y - shape.y
      });
    }
    function fixReferencePoint(direction, point, label) {
      if(direction === 'left') {
        point.x -= label.width;
      } else if(direction === 'top') {
        point.y -= label.height;
      }
    }
    function adjustReferenceLabelPoint(direction, point, label) {
      if(direction === 'left') {
        point.x -= 5;
      } else if(direction === 'right') {
        point.x += label.width + 5;
      } else if(direction === 'top') {
        point.y -= 5;
      } else if(direction === 'bottom') {
        point.y += label.height + 5;
      }
    }
    function moveTargetLabels(modeling, connection) {
      var targetLabels = getTargetLabels(connection);
      var existsMarker = existsAnyMarker(connection, 'target');

      var connectionPartSource = connection.waypoints[connection.waypoints.length-1];
      var connectionPartTarget = connection.waypoints[connection.waypoints.length-2];

      var direction = getStraightLinePartDirection(connectionPartSource, connectionPartTarget);
      var referencePoint = getReferencePoint(connectionPartSource, connectionPartTarget, direction, existsMarker);

      for(var i=0; i<targetLabels.length; i++) {
        var label = targetLabels[i];

        fixReferencePoint(direction, referencePoint, label);

        moveShape(modeling, label, referencePoint);

        adjustReferenceLabelPoint(direction, referencePoint, label);
      }
    }
    function getReferencePoint(connectionPartSource, connectionPartTarget, direction, existsMarker) {
      var point = getAbsoluteClosePoint(connectionPartSource, connectionPartTarget, getCardinalityDistance(existsMarker));

      if(isHorizontal(direction)) {
        point.y += 5;
      } else {
        point.x += 5;
      }

      return point;
    }
    function getSourceLabels(connection) {
      var sources = connection.labels.filter(function(label) {
        return label.labelType === 'sourceCard' || label.labelType === 'sourceName';
      });

      return sources.sort(function(label1, label2) {
        return label1.labelType.localeCompare(label2.labelType);
      });
    }
    function getTargetLabels(connection) {
      var targets = connection.labels.filter(function(label) {
        return label.labelType === 'targetCard' || label.labelType === 'targetName';
      });

      return targets.sort(function(label1, label2) {
        return label1.labelType.localeCompare(label2.labelType);
      });
    }
    function existsAnyMarker(connection, labelType) {
      var connectionType = connection.connectionType;

      if(labelType === 'source') {
        return connectionType === 'aggregation' || connectionType === 'composition';
      } else {
        return connectionType === 'association' || connectionType === 'inheritance';
      }
    }
    function getStraightLinePartDirection(source, target) {
      if(source.y === target.y) {
        return getHorizontalDirection(source, target);
      } else {
        return getVerticalDirection(source, target);
      }
    }
    function getHorizontalDirection(source, target) {
      if(source.x < target.x) {
        return 'right';
      } else {
        return 'left';
      }
    }
    function getVerticalDirection(source, target) {
      if(source.y < target.y) {
        return 'bottom';
      } else {
        return 'top';
      }
    }
    function isHorizontal(direction) {
      return direction === 'right' || direction === 'left';
    }
    function getCardinalityDistance(existsMarker) {
      if(existsMarker) {
        return 15;
      } else {
        return 5;
      }
    }};

  HideElementHandler.$inject = [
    'graphicsFactory',
    'elementRegistry',
    'selection'
  ];

  function HideElementHandler(graphicsFactory, elementRegistry, selection) {
    this._graphicsFactory = graphicsFactory;
    this._elementRegistry = elementRegistry;
    this._selection = selection;
  }
  HideElementHandler.prototype.execute = function(context) {
    var element = context.element;

    this.hide(element);

    this._selection.deselect(element);
  };

  HideElementHandler.prototype.hide = function(element) {
    element.hidden = true;

    if(element instanceof Shape) {
      if(element instanceof Label) {
        this.hideLabel(element);
      } else {
        this.hideShape(element);
      }
    } else {
      this.hideConnection(element);
    }
  };

  HideElementHandler.prototype.hideLabel = function(label) {
    this.hideChildren(label);

    this.updateGfx(label, 'shape');
  };

  HideElementHandler.prototype.hideConnection = function(connection) {
    this.hideChildren(connection);

    this.updateGfx(connection, 'connection');
  };

  HideElementHandler.prototype.hideShape = function(shape) {
    this.hideAttachedEdges(shape);
    this.hideChildren(shape);

    this.updateGfx(shape, 'shape');
  };

  HideElementHandler.prototype.hideAttachedEdges = function(shape) {
    this.hideIncomingAttachedEdges(shape);
    this.hideOutgoingAttachedEdges(shape);
  };

  HideElementHandler.prototype.hideIncomingAttachedEdges = function(shape) {
    var hideElementHandler = this;

    if('incoming' in shape) {
      shape.incoming.forEach(function(edge) {
        hideElementHandler.hide(edge);
      });
    }
  };

  HideElementHandler.prototype.hideOutgoingAttachedEdges = function(shape) {
    var hideElementHandler = this;

    if('outgoing' in shape) {
      shape.outgoing.forEach(function(edge) {
        hideElementHandler.hide(edge);
      });
    }
  };

  HideElementHandler.prototype.hideChildren = function(element) {
    var hideElementHandler = this;

    if('children' in element) {
      element.children.forEach(function(child) {
        hideElementHandler.hide(child);
      });
    }
  };

  HideElementHandler.prototype.updateGfx = function(element, type) {
    var gfx = this._elementRegistry.getGraphics(element, false);

    this._graphicsFactory.update(type, element, gfx);
  };

  /**
   * A handler that implements reversible appending of shapes
   * to a source shape.
   *
   * @param {canvas} Canvas
   * @param {elementFactory} ElementFactory
   * @param {modeling} Modeling
   */
  function AppendShapeHandler(modeling) {
    this._modeling = modeling;
  }

  AppendShapeHandler.$inject = [ 'modeling' ];


  // api //////////////////////


  /**
   * Creates a new shape
   *
   * @param {Object} context
   * @param {ElementDescriptor} context.shape the new shape
   * @param {ElementDescriptor} context.source the source object
   * @param {ElementDescriptor} context.parent the parent object
   * @param {Point} context.position position of the new element
   */
  AppendShapeHandler.prototype.preExecute = function(context) {

    var source = context.source;

    if (!source) {
      throw new Error('source required');
    }

    var target = context.target || source.parent,
        shape = context.shape;

    shape = context.shape =
      this._modeling.createShape(
        shape,
        context.position,
        target, { attach: context.attach });

    context.shape = shape;
  };

  AppendShapeHandler.prototype.postExecute = function(context) {
    var parent = context.connectionParent || context.shape.parent;

    if (!existsConnection(context.source, context.shape)) {

      // create connection
      this._modeling.connect(context.source, context.shape, context.connection, parent);
    }
  };


  function existsConnection(source, target) {
    return some(source.outgoing, function(c) {
      return c.target === target;
    });
  }

  var round$6 = Math.round;


  /**
   * A handler that implements reversible addition of shapes.
   *
   * @param {canvas} Canvas
   */
  function CreateShapeHandler(canvas) {
    this._canvas = canvas;
  }

  CreateShapeHandler.$inject = [ 'canvas' ];


  // api //////////////////////


  /**
   * Appends a shape to a target shape
   *
   * @param {Object} context
   * @param {djs.model.Base} context.parent the parent object
   * @param {Point} context.position position of the new element
   */
  CreateShapeHandler.prototype.execute = function(context) {

    var shape = context.shape,
        positionOrBounds = context.position,
        parent = context.parent,
        parentIndex = context.parentIndex;

    if (!parent) {
      throw new Error('parent required');
    }

    if (!positionOrBounds) {
      throw new Error('position required');
    }

    // (1) add at event center position _or_ at given bounds
    if (positionOrBounds.width !== undefined) {
      assign(shape, positionOrBounds);
    } else {
      assign(shape, {
        x: positionOrBounds.x - round$6(shape.width / 2),
        y: positionOrBounds.y - round$6(shape.height / 2)
      });
    }

    // (2) add to canvas
    this._canvas.addShape(shape, parent, parentIndex);

    return shape;
  };


  /**
   * Undo append by removing the shape
   */
  CreateShapeHandler.prototype.revert = function(context) {

    // (3) remove form canvas
    this._canvas.removeShape(context.shape);
  };

  /**
   * A handler that implements reversible deletion of shapes.
   *
   */
  function DeleteShapeHandler(canvas, modeling) {
    this._canvas = canvas;
    this._modeling = modeling;
  }

  DeleteShapeHandler.$inject = [ 'canvas', 'modeling' ];


  /**
   * - Remove connections
   * - Remove all direct children
   */
  DeleteShapeHandler.prototype.preExecute = function(context) {

    var modeling = this._modeling;

    var shape = context.shape;

    // remove connections
    saveClear(shape.incoming, function(connection) {
      // To make sure that the connection isn't removed twice
      // For example if a container is removed
      modeling.removeConnection(connection, { nested: true });
    });

    saveClear(shape.outgoing, function(connection) {
      modeling.removeConnection(connection, { nested: true });
    });

    // remove child shapes and connections
    saveClear(shape.children, function(child) {
      if (isConnection$2(child)) {
        modeling.removeConnection(child, { nested: true });
      } else {
        modeling.removeShape(child, { nested: true });
      }
    });
  };

  /**
   * Remove shape and remember the parent
   */
  DeleteShapeHandler.prototype.execute = function(context) {
    var canvas = this._canvas;

    var shape = context.shape,
        oldParent = shape.parent;

    context.oldParent = oldParent;

    // remove containment
    context.oldParentIndex = indexOf$1(oldParent.children, shape);

    // remove shape
    canvas.removeShape(shape);

    return shape;
  };


  /**
   * Command revert implementation
   */
  DeleteShapeHandler.prototype.revert = function(context) {

    var canvas = this._canvas;

    var shape = context.shape,
        oldParent = context.oldParent,
        oldParentIndex = context.oldParentIndex;

    // restore containment
    add$1(oldParent.children, shape, oldParentIndex);

    canvas.addShape(shape, oldParent);

    return shape;
  };

  function isConnection$2(element) {
    return element.waypoints;
  }

  /**
   * Calculates the absolute point relative to the new element's position
   *
   * @param {point} point [absolute]
   * @param {bounds} oldBounds
   * @param {bounds} newBounds
   *
   * @return {point} point [absolute]
   */
  function getNewAttachPoint(point, oldBounds, newBounds) {
    var oldCenter = center(oldBounds),
        newCenter = center(newBounds),
        oldDelta = delta(point, oldCenter);

    var newDelta = {
      x: oldDelta.x * (newBounds.width / oldBounds.width),
      y: oldDelta.y * (newBounds.height / oldBounds.height)
    };

    return roundPoint({
      x: newCenter.x + newDelta.x,
      y: newCenter.y + newDelta.y
    });
  }

  function getResizedSourceAnchor(connection, shape, oldBounds) {

    var waypoints = safeGetWaypoints(connection),
        oldAnchor = waypoints[0];

    return getNewAttachPoint(oldAnchor.original || oldAnchor, oldBounds, shape);
  }


  function getResizedTargetAnchor(connection, shape, oldBounds) {

    var waypoints = safeGetWaypoints(connection),
        oldAnchor = waypoints[waypoints.length - 1];

    return getNewAttachPoint(oldAnchor.original || oldAnchor, oldBounds, shape);
  }


  function getMovedSourceAnchor(connection, source, moveDelta) {
    return getResizedSourceAnchor(connection, source, substractPosition(source, moveDelta));
  }


  function getMovedTargetAnchor(connection, target, moveDelta) {
    return getResizedTargetAnchor(connection, target, substractPosition(target, moveDelta));
  }


  // helpers //////////////////////

  function substractPosition(bounds, delta) {
    return {
      x: bounds.x - delta.x,
      y: bounds.y - delta.y,
      width: bounds.width,
      height: bounds.height
    };
  }


  /**
   * Return waypoints of given connection; throw if non exists (should not happen!!).
   *
   * @param {Connection} connection
   *
   * @return {Array<Point>}
   */
  function safeGetWaypoints(connection) {

    var waypoints = connection.waypoints;

    if (!waypoints.length) {
      throw new Error('connection#' + connection.id + ': no waypoints');
    }

    return waypoints;
  }

  function MoveClosure() {

    this.allShapes = {};
    this.allConnections = {};

    this.enclosedElements = {};
    this.enclosedConnections = {};

    this.topLevel = {};
  }


  MoveClosure.prototype.add = function(element, isTopLevel) {
    return this.addAll([ element ], isTopLevel);
  };


  MoveClosure.prototype.addAll = function(elements, isTopLevel) {

    var newClosure = getClosure(elements, !!isTopLevel, this);

    assign(this, newClosure);

    return this;
  };

  /**
   * A helper that is able to carry out serialized move
   * operations on multiple elements.
   *
   * @param {Modeling} modeling
   */
  function MoveHelper(modeling) {
    this._modeling = modeling;
  }

  /**
   * Move the specified elements and all children by the given delta.
   *
   * This moves all enclosed connections, too and layouts all affected
   * external connections.
   *
   * @param  {Array<djs.model.Base>} elements
   * @param  {Point} delta
   * @param  {djs.model.Base} newParent applied to the first level of shapes
   *
   * @return {Array<djs.model.Base>} list of touched elements
   */
  MoveHelper.prototype.moveRecursive = function(elements, delta, newParent) {
    if (!elements) {
      return [];
    } else {
      return this.moveClosure(this.getClosure(elements), delta, newParent);
    }
  };

  /**
   * Move the given closure of elmements.
   *
   * @param {Object} closure
   * @param {Point} delta
   * @param {djs.model.Base} [newParent]
   * @param {djs.model.Base} [newHost]
   */
  MoveHelper.prototype.moveClosure = function(closure, delta, newParent, newHost, primaryShape) {
    var modeling = this._modeling;

    var allShapes = closure.allShapes,
        allConnections = closure.allConnections,
        enclosedConnections = closure.enclosedConnections,
        topLevel = closure.topLevel,
        keepParent = false;

    if (primaryShape && primaryShape.parent === newParent) {
      keepParent = true;
    }

    // move all shapes
    forEach(allShapes, function(shape) {

      // move the element according to the given delta
      modeling.moveShape(shape, delta, topLevel[shape.id] && !keepParent && newParent, {
        recurse: false,
        layout: false
      });
    });

    // move all child connections / layout external connections
    forEach(allConnections, function(c) {

      var sourceMoved = !!allShapes[c.source.id],
          targetMoved = !!allShapes[c.target.id];

      if (enclosedConnections[c.id] && sourceMoved && targetMoved) {
        modeling.moveConnection(c, delta, topLevel[c.id] && !keepParent && newParent);
      } else {
        modeling.layoutConnection(c, {
          connectionStart: sourceMoved && getMovedSourceAnchor(c, c.source, delta),
          connectionEnd: targetMoved && getMovedTargetAnchor(c, c.target, delta)
        });
      }
    });
  };

  /**
   * Returns the closure for the selected elements
   *
   * @param  {Array<djs.model.Base>} elements
   * @return {MoveClosure} closure
   */
  MoveHelper.prototype.getClosure = function(elements) {
    return new MoveClosure().addAll(elements, true);
  };

  /**
   * A handler that implements reversible moving of shapes.
   */
  function MoveShapeHandler(modeling) {
    this._modeling = modeling;

    this._helper = new MoveHelper(modeling);
  }

  MoveShapeHandler.$inject = [ 'modeling' ];


  MoveShapeHandler.prototype.execute = function(context) {

    var shape = context.shape,
        delta = context.delta,
        newParent = context.newParent || shape.parent,
        newParentIndex = context.newParentIndex,
        oldParent = shape.parent;

    context.oldBounds = pick(shape, [ 'x', 'y', 'width', 'height']);

    // save old parent in context
    context.oldParent = oldParent;
    context.oldParentIndex = remove$2(oldParent.children, shape);

    // add to new parent at position
    add$1(newParent.children, shape, newParentIndex);

    // update shape parent + position
    assign(shape, {
      parent: newParent,
      x: shape.x + delta.x,
      y: shape.y + delta.y
    });

    return shape;
  };

  MoveShapeHandler.prototype.postExecute = function(context) {

    var shape = context.shape,
        delta = context.delta,
        hints = context.hints;

    var modeling = this._modeling;

    if (hints.layout !== false) {

      forEach(shape.incoming, function(c) {
        modeling.layoutConnection(c, {
          connectionEnd: getMovedTargetAnchor(c, shape, delta)
        });
      });

      forEach(shape.outgoing, function(c) {
        modeling.layoutConnection(c, {
          connectionStart: getMovedSourceAnchor(c, shape, delta)
        });
      });
    }

    if (hints.recurse !== false) {
      this.moveChildren(context);
    }
  };

  MoveShapeHandler.prototype.revert = function(context) {

    var shape = context.shape,
        oldParent = context.oldParent,
        oldParentIndex = context.oldParentIndex,
        delta = context.delta;

    // restore previous location in old parent
    add$1(oldParent.children, shape, oldParentIndex);

    // revert to old position and parent
    assign(shape, {
      parent: oldParent,
      x: shape.x - delta.x,
      y: shape.y - delta.y
    });

    return shape;
  };

  MoveShapeHandler.prototype.moveChildren = function(context) {

    var delta = context.delta,
        shape = context.shape;

    this._helper.moveRecursive(shape.children, delta, null);
  };

  MoveShapeHandler.prototype.getNewParent = function(context) {
    return context.newParent || context.shape.parent;
  };

  /**
   * A handler that implements reversible resizing of shapes.
   *
   * @param {Modeling} modeling
   */
  function ResizeShapeHandler(modeling) {
    this._modeling = modeling;
  }

  ResizeShapeHandler.$inject = [ 'modeling' ];

  /**
   * {
   *   shape: {....}
   *   newBounds: {
   *     width:  20,
   *     height: 40,
   *     x:       5,
   *     y:      10
   *   }
   *
   * }
   */
  ResizeShapeHandler.prototype.execute = function(context) {
    var shape = context.shape,
        newBounds = context.newBounds,
        minBounds = context.minBounds;

    if (newBounds.x === undefined || newBounds.y === undefined ||
        newBounds.width === undefined || newBounds.height === undefined) {
      throw new Error('newBounds must have {x, y, width, height} properties');
    }

    if (minBounds && (newBounds.width < minBounds.width
      || newBounds.height < minBounds.height)) {
      throw new Error('width and height cannot be less than minimum height and width');
    } else if (!minBounds
      && newBounds.width < 10 || newBounds.height < 10) {
      throw new Error('width and height cannot be less than 10px');
    }

    // save old bbox in context
    context.oldBounds = {
      width:  shape.width,
      height: shape.height,
      x:      shape.x,
      y:      shape.y
    };

    // update shape
    assign(shape, {
      width:  newBounds.width,
      height: newBounds.height,
      x:      newBounds.x,
      y:      newBounds.y
    });

    return shape;
  };

  ResizeShapeHandler.prototype.postExecute = function(context) {

    var shape = context.shape,
        oldBounds = context.oldBounds;

    var modeling = this._modeling;

    forEach(shape.incoming, function(c) {
      modeling.layoutConnection(c, {
        connectionEnd: getResizedTargetAnchor(c, shape, oldBounds)
      });
    });

    forEach(shape.outgoing, function(c) {
      modeling.layoutConnection(c, {
        connectionStart: getResizedSourceAnchor(c, shape, oldBounds)
      });
    });

  };

  ResizeShapeHandler.prototype.revert = function(context) {

    var shape = context.shape,
        oldBounds = context.oldBounds;

    // restore previous bbox
    assign(shape, {
      width:  oldBounds.width,
      height: oldBounds.height,
      x:      oldBounds.x,
      y:      oldBounds.y
    });

    return shape;
  };

  /**
   * A handler that implements reversible replacing of shapes.
   * Internally the old shape will be removed and the new shape will be added.
   *
   *
   * @class
   * @constructor
   *
   * @param {canvas} Canvas
   */
  function ReplaceShapeHandler(modeling, rules) {
    this._modeling = modeling;
    this._rules = rules;
  }

  ReplaceShapeHandler.$inject = [ 'modeling', 'rules' ];


  // api //////////////////////


  /**
   * Replaces a shape with an replacement Element.
   *
   * The newData object should contain type, x, y.
   *
   * If possible also the incoming/outgoing connection
   * will be restored.
   *
   * @param {Object} context
   */
  ReplaceShapeHandler.prototype.preExecute = function(context) {

    var self = this,
        modeling = this._modeling,
        rules = this._rules;

    var oldShape = context.oldShape,
        newData = context.newData,
        hints = context.hints,
        newShape;

    function canReconnect(type, source, target, connection) {
      return rules.allowed(type, {
        source: source,
        target: target,
        connection: connection
      });
    }


    // (1) place a new shape at the given position

    var position = {
      x: newData.x,
      y: newData.y
    };

    newShape = context.newShape =
      context.newShape ||
      self.createShape(newData, position, oldShape.parent, hints);


    // (2) update the host

    if (oldShape.host) {
      modeling.updateAttachment(newShape, oldShape.host);
    }


    // (3) adopt all children from the old shape

    var children;

    if (hints.moveChildren !== false) {
      children = oldShape.children.slice();

      modeling.moveElements(children, { x: 0, y: 0 }, newShape);
    }

    // (4) reconnect connections to the new shape (where allowed)

    var incoming = oldShape.incoming.slice(),
        outgoing = oldShape.outgoing.slice();

    forEach(incoming, function(connection) {
      var waypoints = connection.waypoints,
          docking = waypoints[waypoints.length - 1],
          source = connection.source,
          allowed = canReconnect('connection.reconnectEnd', source, newShape, connection);

      if (allowed) {
        self.reconnectEnd(connection, newShape, docking);
      }
    });

    forEach(outgoing, function(connection) {
      var waypoints = connection.waypoints,
          docking = waypoints[0],
          target = connection.target,
          allowed = canReconnect('connection.reconnectStart', newShape, target, connection);

      if (allowed) {
        self.reconnectStart(connection, newShape, docking);
      }

    });
  };


  ReplaceShapeHandler.prototype.postExecute = function(context) {
    var modeling = this._modeling;

    var oldShape = context.oldShape,
        newShape = context.newShape;

    // if an element gets resized on replace, layout the connection again
    forEach(newShape.incoming, function(c) {
      modeling.layoutConnection(c, { endChanged: true });
    });

    forEach(newShape.outgoing, function(c) {
      modeling.layoutConnection(c, { startChanged: true });
    });

    modeling.removeShape(oldShape);
  };


  ReplaceShapeHandler.prototype.execute = function(context) {};

  ReplaceShapeHandler.prototype.revert = function(context) {};


  ReplaceShapeHandler.prototype.createShape = function(shape, position, target, hints) {
    var modeling = this._modeling;
    return modeling.createShape(shape, position, target, hints);
  };


  ReplaceShapeHandler.prototype.reconnectStart = function(connection, newSource, dockingPoint) {
    var modeling = this._modeling;
    modeling.reconnectStart(connection, newSource, dockingPoint);
  };


  ReplaceShapeHandler.prototype.reconnectEnd = function(connection, newTarget, dockingPoint) {
    var modeling = this._modeling;
    modeling.reconnectEnd(connection, newTarget, dockingPoint);
  };

  /**
   * A handler that toggles the collapsed state of an element
   * and the visibility of all its children.
   *
   * @param {Modeling} modeling
   */
  function ToggleShapeCollapseHandler(modeling) {
    this._modeling = modeling;
  }

  ToggleShapeCollapseHandler.$inject = [ 'modeling' ];


  ToggleShapeCollapseHandler.prototype.execute = function(context) {

    var shape = context.shape,
        children = shape.children;

    // remember previous visibility of children
    context.oldChildrenVisibility = getElementsVisibility(children);

    // toggle state
    shape.collapsed = !shape.collapsed;

    // hide/show children
    setHidden(children, shape.collapsed);

    return [shape].concat(children);
  };


  ToggleShapeCollapseHandler.prototype.revert = function(context) {

    var shape = context.shape,
        oldChildrenVisibility = context.oldChildrenVisibility;

    var children = shape.children;

    // set old visability of children
    restoreVisibility(children, oldChildrenVisibility);

    // retoggle state
    shape.collapsed = !shape.collapsed;

    return [shape].concat(children);
  };


  // helpers //////////////////////

  /**
   * Return a map { elementId -> hiddenState}.
   *
   * @param {Array<djs.model.Shape>} elements
   *
   * @return {Object}
   */
  function getElementsVisibility(elements) {

    var result = {};

    elements.forEach(function(e) {
      result[e.id] = e.hidden;
    });

    return result;
  }


  function setHidden(elements, newHidden) {
    elements.forEach(function(element) {
      element.hidden = newHidden;
    });
  }

  function restoreVisibility(elements, lastState) {
    elements.forEach(function(e) {
      e.hidden = lastState[e.id];
    });
  }

  /**
   * Get Resize direction given axis + offset
   *
   * @param {String} axis (x|y)
   * @param {Number} offset
   *
   * @return {String} (e|w|n|s)
   */


  /**
   * Resize the given bounds by the specified delta from a given anchor point.
   *
   * @param {Bounds} bounds the bounding box that should be resized
   * @param {String} direction in which the element is resized (n, s, e, w)
   * @param {Point} delta of the resize operation
   *
   * @return {Bounds} resized bounding box
   */
  function resizeBounds$1(bounds, direction, delta) {

    var dx = delta.x,
        dy = delta.y;

    switch (direction) {

    case 'n':
      return {
        x: bounds.x,
        y: bounds.y + dy,
        width: bounds.width,
        height: bounds.height - dy
      };

    case 's':
      return {
        x: bounds.x,
        y: bounds.y,
        width: bounds.width,
        height: bounds.height + dy
      };

    case 'w':
      return {
        x: bounds.x + dx,
        y: bounds.y,
        width: bounds.width - dx,
        height: bounds.height
      };

    case 'e':
      return {
        x: bounds.x,
        y: bounds.y,
        width: bounds.width + dx,
        height: bounds.height
      };

    default:
      throw new Error('unrecognized direction: ' + direction);
    }
  }

  /**
   * A handler that implements reversible creating and removing of space.
   *
   * It executes in two phases:
   *
   *  (1) resize all affected resizeShapes
   *  (2) move all affected moveElements
   */
  function SpaceToolHandler(modeling) {
    this._modeling = modeling;
  }

  SpaceToolHandler.$inject = [ 'modeling' ];


  SpaceToolHandler.prototype.preExecute = function(context) {

    // resize
    var modeling = this._modeling,
        resizingShapes = context.resizingShapes,
        delta = context.delta,
        direction = context.direction;

    forEach(resizingShapes, function(shape) {
      var newBounds = resizeBounds$1(shape, direction, delta);

      modeling.resizeShape(shape, newBounds);
    });
  };

  SpaceToolHandler.prototype.postExecute = function(context) {
    // move
    var modeling = this._modeling,
        movingShapes = context.movingShapes,
        delta = context.delta;

    modeling.moveElements(movingShapes, delta, undefined, { autoResize: false, attach: false });
  };

  SpaceToolHandler.prototype.execute = function(context) {};
  SpaceToolHandler.prototype.revert = function(context) {};

  /**
   * A handler that attaches a label to a given target shape.
   *
   * @param {Canvas} canvas
   */
  function CreateLabelHandler(canvas) {
    CreateShapeHandler.call(this, canvas);
  }

  inherits_browser(CreateLabelHandler, CreateShapeHandler);

  CreateLabelHandler.$inject = [ 'canvas' ];


  // api //////////////////////


  var originalExecute = CreateShapeHandler.prototype.execute;

  /**
   * Appends a label to a target shape.
   *
   * @method CreateLabelHandler#execute
   *
   * @param {Object} context
   * @param {ElementDescriptor} context.target the element the label is attached to
   * @param {ElementDescriptor} context.parent the parent object
   * @param {Point} context.position position of the new element
   */
  CreateLabelHandler.prototype.execute = function(context) {

    var label = context.shape;

    ensureValidDimensions(label);

    label.labelTarget = context.labelTarget;

    return originalExecute.call(this, context);
  };

  var originalRevert = CreateShapeHandler.prototype.revert;

  /**
   * Undo append by removing the shape
   */
  CreateLabelHandler.prototype.revert = function(context) {
    context.shape.labelTarget = null;

    return originalRevert.call(this, context);
  };


  // helpers //////////////////////

  function ensureValidDimensions(label) {
    // make sure a label has valid { width, height } dimensions
    [ 'width', 'height' ].forEach(function(prop) {
      if (typeof label[prop] === 'undefined') {
        label[prop] = 0;
      }
    });
  }

  function CreateConnectionHandler(canvas, layouter) {
    this._canvas = canvas;
    this._layouter = layouter;
  }

  CreateConnectionHandler.$inject = [ 'canvas', 'layouter' ];


  // api //////////////////////


  /**
   * Appends a shape to a target shape
   *
   * @param {Object} context
   * @param {djs.element.Base} context.source the source object
   * @param {djs.element.Base} context.target the parent object
   * @param {Point} context.position position of the new element
   */
  CreateConnectionHandler.prototype.execute = function(context) {

    var connection = context.connection,
        source = context.source,
        target = context.target,
        parent = context.parent,
        parentIndex = context.parentIndex,
        hints = context.hints;

    if (!source || !target) {
      throw new Error('source and target required');
    }

    if (!parent) {
      throw new Error('parent required');
    }

    connection.source = source;
    connection.target = target;

    if (!connection.waypoints) {
      connection.waypoints = this._layouter.layoutConnection(connection, hints);
    }

    // add connection
    this._canvas.addConnection(connection, parent, parentIndex);

    return connection;
  };

  CreateConnectionHandler.prototype.revert = function(context) {
    var connection = context.connection;

    this._canvas.removeConnection(connection);

    connection.source = null;
    connection.target = null;
  };

  /**
   * A handler that implements reversible deletion of Connections.
   */
  function DeleteConnectionHandler(canvas, modeling) {
    this._canvas = canvas;
    this._modeling = modeling;
  }

  DeleteConnectionHandler.$inject = [
    'canvas',
    'modeling'
  ];


  DeleteConnectionHandler.prototype.execute = function(context) {

    var connection = context.connection,
        parent = connection.parent;

    context.parent = parent;

    // remember containment
    context.parentIndex = indexOf$1(parent.children, connection);

    context.source = connection.source;
    context.target = connection.target;

    this._canvas.removeConnection(connection);

    connection.source = null;
    connection.target = null;

    return connection;
  };

  /**
   * Command revert implementation.
   */
  DeleteConnectionHandler.prototype.revert = function(context) {

    var connection = context.connection,
        parent = context.parent,
        parentIndex = context.parentIndex;

    connection.source = context.source;
    connection.target = context.target;

    // restore containment
    add$1(parent.children, connection, parentIndex);

    this._canvas.addConnection(connection, parent);

    return connection;
  };

  /**
   * A handler that implements reversible moving of connections.
   *
   * The handler differs from the layout connection handler in a sense
   * that it preserves the connection layout.
   */
  function MoveConnectionHandler() { }


  MoveConnectionHandler.prototype.execute = function(context) {

    var connection = context.connection,
        delta = context.delta;

    var newParent = context.newParent || connection.parent,
        newParentIndex = context.newParentIndex,
        oldParent = connection.parent;

    // save old parent in context
    context.oldParent = oldParent;
    context.oldParentIndex = remove$2(oldParent.children, connection);

    // add to new parent at position
    add$1(newParent.children, connection, newParentIndex);

    // update parent
    connection.parent = newParent;

    // update waypoint positions
    forEach(connection.waypoints, function(p) {
      p.x += delta.x;
      p.y += delta.y;

      if (p.original) {
        p.original.x += delta.x;
        p.original.y += delta.y;
      }
    });

    return connection;
  };

  MoveConnectionHandler.prototype.revert = function(context) {

    var connection = context.connection,
        newParent = connection.parent,
        oldParent = context.oldParent,
        oldParentIndex = context.oldParentIndex,
        delta = context.delta;

    // remove from newParent
    remove$2(newParent.children, connection);

    // restore previous location in old parent
    add$1(oldParent.children, connection, oldParentIndex);

    // restore parent
    connection.parent = oldParent;

    // revert to old waypoint positions
    forEach(connection.waypoints, function(p) {
      p.x -= delta.x;
      p.y -= delta.y;

      if (p.original) {
        p.original.x -= delta.x;
        p.original.y -= delta.y;
      }
    });

    return connection;
  };

  /**
   * A handler that implements reversible moving of shapes.
   */
  function LayoutConnectionHandler(layouter, canvas) {
    this._layouter = layouter;
    this._canvas = canvas;
  }

  LayoutConnectionHandler.$inject = [ 'layouter', 'canvas' ];

  LayoutConnectionHandler.prototype.execute = function(context) {

    var connection = context.connection;

    var oldWaypoints = connection.waypoints;

    assign(context, {
      oldWaypoints: oldWaypoints
    });

    connection.waypoints = this._layouter.layoutConnection(connection, context.hints);

    return connection;
  };

  LayoutConnectionHandler.prototype.revert = function(context) {

    var connection = context.connection;

    connection.waypoints = context.oldWaypoints;

    return connection;
  };

  function UpdateWaypointsHandler() { }

  UpdateWaypointsHandler.prototype.execute = function(context) {

    var connection = context.connection,
        newWaypoints = context.newWaypoints;

    context.oldWaypoints = connection.waypoints;

    connection.waypoints = newWaypoints;

    return connection;
  };

  UpdateWaypointsHandler.prototype.revert = function(context) {

    var connection = context.connection,
        oldWaypoints = context.oldWaypoints;

    connection.waypoints = oldWaypoints;

    return connection;
  };

  /**
   * Reconnect connection handler
   */
  function ReconnectConnectionHandler() { }

  ReconnectConnectionHandler.$inject = [ ];

  ReconnectConnectionHandler.prototype.execute = function(context) {

    var newSource = context.newSource,
        newTarget = context.newTarget,
        connection = context.connection,
        dockingOrPoints = context.dockingOrPoints,
        oldWaypoints = connection.waypoints,
        newWaypoints;

    if (!newSource && !newTarget) {
      throw new Error('newSource or newTarget are required');
    }

    if (newSource && newTarget) {
      throw new Error('must specify either newSource or newTarget');
    }

    context.oldWaypoints = oldWaypoints;

    if (isArray(dockingOrPoints)) {
      newWaypoints = dockingOrPoints;
    } else {
      newWaypoints = oldWaypoints.slice();

      newWaypoints.splice(newSource ? 0 : -1, 1, dockingOrPoints);
    }

    if (newSource) {
      context.oldSource = connection.source;
      connection.source = newSource;
    }

    if (newTarget) {
      context.oldTarget = connection.target;
      connection.target = newTarget;
    }

    connection.waypoints = newWaypoints;

    return connection;
  };

  ReconnectConnectionHandler.prototype.revert = function(context) {

    var newSource = context.newSource,
        newTarget = context.newTarget,
        connection = context.connection;

    if (newSource) {
      connection.source = context.oldSource;
    }

    if (newTarget) {
      connection.target = context.oldTarget;
    }

    connection.waypoints = context.oldWaypoints;

    return connection;
  };

  /**
   * A handler that implements reversible moving of shapes.
   */
  function MoveElementsHandler(modeling) {
    this._helper = new MoveHelper(modeling);
  }

  MoveElementsHandler.$inject = [ 'modeling' ];

  MoveElementsHandler.prototype.preExecute = function(context) {
    context.closure = this._helper.getClosure(context.shapes);
  };

  MoveElementsHandler.prototype.postExecute = function(context) {

    var hints = context.hints,
        primaryShape;

    if (hints && hints.primaryShape) {
      primaryShape = hints.primaryShape;
      hints.oldParent = primaryShape.parent;
    }

    this._helper.moveClosure(
      context.closure,
      context.delta,
      context.newParent,
      context.newHost,
      primaryShape
    );
  };

  function DeleteElementsHandler(modeling, elementRegistry) {
    this._modeling = modeling;
    this._elementRegistry = elementRegistry;
  }

  DeleteElementsHandler.$inject = [
    'modeling',
    'elementRegistry'
  ];


  DeleteElementsHandler.prototype.postExecute = function(context) {

    var modeling = this._modeling,
        elementRegistry = this._elementRegistry,
        elements = context.elements;

    forEach(elements, function(element) {

      // element may have been removed with previous
      // remove operations already (e.g. in case of nesting)
      if (!elementRegistry.get(element.id)) {
        return;
      }

      if (element.waypoints) {
        modeling.removeConnection(element);
      } else {
        modeling.removeShape(element);
      }
    });
  };

  /**
   * A handler that distributes elements evenly.
   */
  function DistributeElements(modeling) {
    this._modeling = modeling;
  }

  DistributeElements.$inject = [ 'modeling' ];

  var OFF_AXIS = {
    x: 'y',
    y: 'x'
  };

  DistributeElements.prototype.preExecute = function(context) {
    var modeling = this._modeling;

    var groups = context.groups,
        axis = context.axis,
        dimension = context.dimension;

    function updateRange(group, element) {
      group.range.min = Math.min(element[axis], group.range.min);
      group.range.max = Math.max(element[axis] + element[dimension], group.range.max);
    }

    function center(element) {
      return element[axis] + element[dimension] / 2;
    }

    function lastIdx(arr) {
      return arr.length - 1;
    }

    function rangeDiff(range) {
      return range.max - range.min;
    }

    function centerElement(refCenter, element) {
      var delta = { y: 0 };

      delta[axis] = refCenter - center(element);

      if (delta[axis]) {

        delta[OFF_AXIS[axis]] = 0;

        modeling.moveElements([ element ], delta, element.parent);
      }
    }

    var firstGroup = groups[0],
        lastGroupIdx = lastIdx(groups),
        lastGroup = groups[ lastGroupIdx ];

    var margin,
        spaceInBetween,
        groupsSize = 0; // the size of each range

    forEach(groups, function(group, idx) {
      var sortedElements,
          refElem,
          refCenter;

      if (group.elements.length < 2) {
        if (idx && idx !== groups.length - 1) {
          updateRange(group, group.elements[0]);

          groupsSize += rangeDiff(group.range);
        }
        return;
      }

      sortedElements = sortBy(group.elements, axis);

      refElem = sortedElements[0];

      if (idx === lastGroupIdx) {
        refElem = sortedElements[lastIdx(sortedElements)];
      }

      refCenter = center(refElem);

      // wanna update the ranges after the shapes have been centered
      group.range = null;

      forEach(sortedElements, function(element) {

        centerElement(refCenter, element);

        if (group.range === null) {
          group.range = {
            min: element[axis],
            max: element[axis] + element[dimension]
          };

          return;
        }

        // update group's range after centering the range elements
        updateRange(group, element);
      });

      if (idx && idx !== groups.length - 1) {
        groupsSize += rangeDiff(group.range);
      }
    });

    spaceInBetween = Math.abs(lastGroup.range.min - firstGroup.range.max);

    margin = Math.round((spaceInBetween - groupsSize) / (groups.length - 1));

    if (margin < groups.length - 1) {
      return;
    }

    forEach(groups, function(group, groupIdx) {
      var delta = {},
          prevGroup;

      if (group === firstGroup || group === lastGroup) {
        return;
      }

      prevGroup = groups[groupIdx - 1];

      group.range.max = 0;

      forEach(group.elements, function(element, idx) {
        delta[OFF_AXIS[axis]] = 0;
        delta[axis] = (prevGroup.range.max - element[axis]) + margin;

        if (group.range.min !== element[axis]) {
          delta[axis] += element[axis] - group.range.min;
        }

        if (delta[axis]) {
          modeling.moveElements([ element ], delta, element.parent);
        }

        group.range.max = Math.max(element[axis] + element[dimension], idx ? group.range.max : 0);
      });
    });
  };

  DistributeElements.prototype.postExecute = function(context) {

  };

  /**
   * A handler that align elements in a certain way.
   *
   */
  function AlignElements(modeling, canvas) {
    this._modeling = modeling;
    this._canvas = canvas;
  }

  AlignElements.$inject = [ 'modeling', 'canvas' ];


  AlignElements.prototype.preExecute = function(context) {
    var modeling = this._modeling;

    var elements = context.elements,
        alignment = context.alignment;


    forEach(elements, function(element) {
      var delta = {
        x: 0,
        y: 0
      };

      if (alignment.left) {
        delta.x = alignment.left - element.x;

      } else if (alignment.right) {
        delta.x = (alignment.right - element.width) - element.x;

      } else if (alignment.center) {
        delta.x = (alignment.center - Math.round(element.width / 2)) - element.x;

      } else if (alignment.top) {
        delta.y = alignment.top - element.y;

      } else if (alignment.bottom) {
        delta.y = (alignment.bottom - element.height) - element.y;

      } else if (alignment.middle) {
        delta.y = (alignment.middle - Math.round(element.height / 2)) - element.y;
      }

      modeling.moveElements([ element ], delta, element.parent);
    });
  };

  AlignElements.prototype.postExecute = function(context) {

  };

  /**
   * A handler that implements reversible attaching/detaching of shapes.
   */
  function UpdateAttachmentHandler(modeling) {
    this._modeling = modeling;
  }

  UpdateAttachmentHandler.$inject = [ 'modeling' ];


  UpdateAttachmentHandler.prototype.execute = function(context) {
    var shape = context.shape,
        newHost = context.newHost,
        oldHost = shape.host;

    // (0) detach from old host
    context.oldHost = oldHost;
    context.attacherIdx = removeAttacher(oldHost, shape);

    // (1) attach to new host
    addAttacher(newHost, shape);

    // (2) update host
    shape.host = newHost;

    return shape;
  };

  UpdateAttachmentHandler.prototype.revert = function(context) {
    var shape = context.shape,
        newHost = context.newHost,
        oldHost = context.oldHost,
        attacherIdx = context.attacherIdx;

    // (2) update host
    shape.host = oldHost;

    // (1) attach to new host
    removeAttacher(newHost, shape);

    // (0) detach from old host
    addAttacher(oldHost, shape, attacherIdx);

    return shape;
  };


  function removeAttacher(host, attacher) {
    // remove attacher from host
    return remove$2(host && host.attachers, attacher);
  }

  function addAttacher(host, attacher, idx) {

    if (!host) {
      return;
    }

    var attachers = host.attachers;

    if (!attachers) {
      host.attachers = attachers = [];
    }

    add$1(attachers, attacher, idx);
  }

  function removeProperties(element, properties) {
    forEach(properties, function(prop) {
      if (element[prop]) {
        delete element[prop];
      }
    });
  }

  /**
   * A handler that implements pasting of elements onto the diagram.
   *
   * @param {eventBus} EventBus
   * @param {canvas} Canvas
   * @param {selection} Selection
   * @param {elementFactory} ElementFactory
   * @param {modeling} Modeling
   * @param {rules} Rules
   */
  function PasteHandler(
      eventBus, canvas, selection,
      elementFactory, modeling, rules) {

    this._eventBus = eventBus;
    this._canvas = canvas;
    this._selection = selection;
    this._elementFactory = elementFactory;
    this._modeling = modeling;
    this._rules = rules;
  }


  PasteHandler.$inject = [
    'eventBus',
    'canvas',
    'selection',
    'elementFactory',
    'modeling',
    'rules'
  ];


  // api //////////////////////

  /**
   * Creates a new shape
   *
   * @param {Object} context
   * @param {Object} context.tree the new shape
   * @param {Element} context.topParent the paste target
   */
  PasteHandler.prototype.preExecute = function(context) {
    var eventBus = this._eventBus,
        self = this;

    var tree = context.tree,
        topParent = context.topParent,
        position = context.position;

    tree.createdElements = {};

    tree.labels = [];

    forEach(tree, function(elements, depthStr) {
      var depth = parseInt(depthStr, 10);

      if (isNaN(depth)) {
        return;
      }

      // set the parent on the top level elements
      if (!depth) {
        elements = map(elements, function(descriptor) {
          descriptor.parent = topParent;

          return descriptor;
        });
      }

      // Order by priority for element creation
      elements = sortBy(elements, 'priority');

      forEach(elements, function(descriptor) {
        var id = descriptor.id,
            parent = descriptor.parent,
            hints = {},
            newPosition;

        var element = assign({}, descriptor);

        if (depth) {
          element.parent = self._getCreatedElement(parent, tree);
        }

        // this happens when shapes have not been created due to rules
        if (!parent) {
          return;
        }

        eventBus.fire('element.paste', {
          createdElements: tree.createdElements,
          descriptor: element
        });

        // in case the parent changed during 'element.paste'
        parent = element.parent;

        if (element.waypoints) {
          element = self._createConnection(element, parent, position, tree);

          if (element) {
            tree.createdElements[id] = {
              element: element,
              descriptor: descriptor
            };
          }

          return;
        }


        // supply not-root information as hint
        if (element.parent !== topParent) {
          hints.root = false;
        }

        // set host
        if (element.host) {
          hints.attach = true;

          parent = self._getCreatedElement(element.host, tree);
        }

        // handle labels
        if (element.labelTarget) {
          return tree.labels.push(element);
        }

        newPosition = {
          x: Math.round(position.x + element.delta.x + (element.width / 2)),
          y: Math.round(position.y + element.delta.y + (element.height / 2))
        };

        removeProperties(element, [
          'id',
          'parent',
          'delta',
          'host',
          'priority'
        ]);

        element = self._createShape(element, parent, newPosition, hints);

        if (element) {
          tree.createdElements[id] = {
            element: element,
            descriptor: descriptor
          };
        }
      });
    });
  };

  // move label's to their relative position
  PasteHandler.prototype.postExecute = function(context) {
    var modeling = this._modeling,
        selection = this._selection,
        self = this;

    var tree = context.tree,
        labels = tree.labels,
        topLevelElements = [];

    forEach(labels, function(labelDescriptor) {
      var labelTarget = self._getCreatedElement(labelDescriptor.labelTarget, tree),
          labels, labelTargetPos, newPosition;

      if (!labelTarget) {
        return;
      }

      labels = labelTarget.labels;

      if (!labels || !labels.length) {
        return;
      }

      labelTargetPos = {
        x: labelTarget.x,
        y: labelTarget.y
      };

      if (labelTarget.waypoints) {
        labelTargetPos = labelTarget.waypoints[0];
      }

      forEach(labels, function(label) {
        newPosition = {
          x: Math.round((labelTargetPos.x - label.x) + labelDescriptor.delta.x),
          y: Math.round((labelTargetPos.y - label.y) + labelDescriptor.delta.y)
        };

        modeling.moveShape(label, newPosition, labelTarget.parent);
      });
    });

    forEach(tree[0], function(descriptor) {
      var id = descriptor.id,
          toplevel = tree.createdElements[id];

      if (toplevel) {
        topLevelElements.push(toplevel.element);
      }
    });

    selection.select(topLevelElements);
  };


  PasteHandler.prototype._createConnection = function(element, parent, parentCenter, tree) {
    var modeling = this._modeling,
        rules = this._rules;

    var connection, source, target, canPaste;

    element.waypoints = map(element.waypoints, function(waypoint, idx) {
      return {
        x: Math.round(parentCenter.x + element.delta[idx].x),
        y: Math.round(parentCenter.y + element.delta[idx].y)
      };
    });

    source = this._getCreatedElement(element.source, tree);
    target = this._getCreatedElement(element.target, tree);

    if (!source || !target) {
      return null;
    }

    canPaste = rules.allowed('element.paste', {
      source: source,
      target: target
    });

    if (!canPaste) {
      return null;
    }

    removeProperties(element, [
      'id',
      'parent',
      'delta',
      'source',
      'target',
      'width',
      'height',
      'priority'
    ]);

    connection = modeling.createConnection(source, target, element, parent);

    return connection;
  };


  PasteHandler.prototype._createShape = function(element, parent, position, isAttach, hints) {
    var modeling = this._modeling,
        elementFactory = this._elementFactory,
        rules = this._rules;

    var canPaste = rules.allowed('element.paste', {
      element: element,
      position: position,
      parent: parent
    });

    if (!canPaste) {
      return null;
    }

    var shape = elementFactory.createShape(element);

    modeling.createShape(shape, position, parent, isAttach, hints);

    return shape;
  };


  PasteHandler.prototype._getCreatedElement = function(id, tree) {
    return tree.createdElements[id] && tree.createdElements[id].element;
  };

  /**
   * The basic modeling entry point.
   *
   * @param {EventBus} eventBus
   * @param {ElementFactory} elementFactory
   * @param {CommandStack} commandStack
   */
  function Modeling(eventBus, elementFactory, commandStack) {
    this._eventBus = eventBus;
    this._elementFactory = elementFactory;
    this._commandStack = commandStack;

    var self = this;

    eventBus.on('diagram.init', function() {
      // register modeling handlers
      self.registerHandlers(commandStack);
    });
  }

  Modeling.$inject = [ 'eventBus', 'elementFactory', 'commandStack' ];


  Modeling.prototype.getHandlers = function() {
    return {
      'shape.append': AppendShapeHandler,
      'shape.create': CreateShapeHandler,
      'shape.delete': DeleteShapeHandler,
      'shape.move': MoveShapeHandler,
      'shape.resize': ResizeShapeHandler,
      'shape.replace': ReplaceShapeHandler,
      'shape.toggleCollapse': ToggleShapeCollapseHandler,

      'spaceTool': SpaceToolHandler,

      'label.create': CreateLabelHandler,

      'connection.create': CreateConnectionHandler,
      'connection.delete': DeleteConnectionHandler,
      'connection.move': MoveConnectionHandler,
      'connection.layout': LayoutConnectionHandler,

      'connection.updateWaypoints': UpdateWaypointsHandler,

      'connection.reconnectStart': ReconnectConnectionHandler,
      'connection.reconnectEnd': ReconnectConnectionHandler,

      'elements.move': MoveElementsHandler,
      'elements.delete': DeleteElementsHandler,

      'elements.distribute': DistributeElements,
      'elements.align': AlignElements,

      'element.updateAttachment': UpdateAttachmentHandler,

      'elements.paste': PasteHandler
    };
  };

  /**
   * Register handlers with the command stack
   *
   * @param {CommandStack} commandStack
   */
  Modeling.prototype.registerHandlers = function(commandStack) {
    forEach(this.getHandlers(), function(handler, id) {
      commandStack.registerHandler(id, handler);
    });
  };


  // modeling helpers //////////////////////

  Modeling.prototype.moveShape = function(shape, delta, newParent, newParentIndex, hints) {

    if (typeof newParentIndex === 'object') {
      hints = newParentIndex;
      newParentIndex = null;
    }

    var context = {
      shape: shape,
      delta:  delta,
      newParent: newParent,
      newParentIndex: newParentIndex,
      hints: hints || {}
    };

    this._commandStack.execute('shape.move', context);
  };


  /**
   * Update the attachment of the given shape.
   *
   * @param {djs.mode.Base} shape
   * @param {djs.model.Base} [newHost]
   */
  Modeling.prototype.updateAttachment = function(shape, newHost) {
    var context = {
      shape: shape,
      newHost: newHost
    };

    this._commandStack.execute('element.updateAttachment', context);
  };


  /**
   * Move a number of shapes to a new target, either setting it as
   * the new parent or attaching it.
   *
   * @param {Array<djs.mode.Base>} shapes
   * @param {Point} delta
   * @param {djs.model.Base} [target]
   * @param {Object} [hints]
   * @param {Boolean} [hints.attach=false]
   */
  Modeling.prototype.moveElements = function(shapes, delta, target, hints) {

    hints = hints || {};

    var attach = hints.attach;

    var newParent = target,
        newHost;

    if (attach === true) {
      newHost = target;
      newParent = target.parent;
    } else

    if (attach === false) {
      newHost = null;
    }

    var context = {
      shapes: shapes,
      delta: delta,
      newParent: newParent,
      newHost: newHost,
      hints: hints
    };

    this._commandStack.execute('elements.move', context);
  };


  Modeling.prototype.moveConnection = function(connection, delta, newParent, newParentIndex, hints) {

    if (typeof newParentIndex === 'object') {
      hints = newParentIndex;
      newParentIndex = undefined;
    }

    var context = {
      connection: connection,
      delta: delta,
      newParent: newParent,
      newParentIndex: newParentIndex,
      hints: hints || {}
    };

    this._commandStack.execute('connection.move', context);
  };


  Modeling.prototype.layoutConnection = function(connection, hints) {
    var context = {
      connection: connection,
      hints: hints || {}
    };

    this._commandStack.execute('connection.layout', context);
  };


  /**
   * Create connection.
   *
   * @param {djs.model.Base} source
   * @param {djs.model.Base} target
   * @param {Number} [targetIndex]
   * @param {Object|djs.model.Connection} connection
   * @param {djs.model.Base} parent
   * @param {Object} hints
   *
   * @return {djs.model.Connection} the created connection.
   */
  Modeling.prototype.createConnection = function(source, target, parentIndex, connection, parent, hints) {

    if (typeof parentIndex === 'object') {
      hints = parent;
      parent = connection;
      connection = parentIndex;
      parentIndex = undefined;
    }

    connection = this._create('connection', connection);

    var context = {
      source: source,
      target: target,
      parent: parent,
      parentIndex: parentIndex,
      connection: connection,
      hints: hints
    };

    this._commandStack.execute('connection.create', context);

    return context.connection;
  };


  /**
   * Create a shape at the specified position.
   *
   * @param {djs.model.Shape|Object} shape
   * @param {Point} position
   * @param {djs.model.Shape|djs.model.Root} target
   * @param {Number} [parentIndex] position in parents children list
   * @param {Object} [hints]
   * @param {Boolean} [hints.attach] whether to attach to target or become a child
   *
   * @return {djs.model.Shape} the created shape
   */
  Modeling.prototype.createShape = function(shape, position, target, parentIndex, hints) {

    if (typeof parentIndex !== 'number') {
      hints = parentIndex;
      parentIndex = undefined;
    }

    hints = hints || {};

    var attach = hints.attach,
        parent,
        host;

    shape = this._create('shape', shape);

    if (attach) {
      parent = target.parent;
      host = target;
    } else {
      parent = target;
    }

    var context = {
      position: position,
      shape: shape,
      parent: parent,
      parentIndex: parentIndex,
      host: host,
      hints: hints
    };

    this._commandStack.execute('shape.create', context);

    return context.shape;
  };


  Modeling.prototype.createLabel = function(labelTarget, position, label, parent) {

    label = this._create('label', label);

    var context = {
      labelTarget: labelTarget,
      position: position,
      parent: parent || labelTarget.parent,
      shape: label
    };

    this._commandStack.execute('label.create', context);

    return context.shape;
  };


  /**
   * Append shape to given source, drawing a connection
   * between source and the newly created shape.
   *
   * @param {djs.model.Shape} source
   * @param {djs.model.Shape|Object} shape
   * @param {Point} position
   * @param {djs.model.Shape} target
   * @param {Object} [hints]
   * @param {Boolean} [hints.attach]
   * @param {djs.model.Connection|Object} [hints.connection]
   * @param {djs.model.Base} [hints.connectionParent]
   *
   * @return {djs.model.Shape} the newly created shape
   */
  Modeling.prototype.appendShape = function(source, shape, position, target, hints) {

    hints = hints || {};

    shape = this._create('shape', shape);

    var context = {
      source: source,
      position: position,
      target: target,
      shape: shape,
      connection: hints.connection,
      connectionParent: hints.connectionParent,
      attach: hints.attach
    };

    this._commandStack.execute('shape.append', context);

    return context.shape;
  };


  Modeling.prototype.removeElements = function(elements) {
    var context = {
      elements: elements
    };

    this._commandStack.execute('elements.delete', context);
  };


  Modeling.prototype.distributeElements = function(groups, axis, dimension) {
    var context = {
      groups: groups,
      axis: axis,
      dimension: dimension
    };

    this._commandStack.execute('elements.distribute', context);
  };


  Modeling.prototype.removeShape = function(shape, hints) {
    var context = {
      shape: shape,
      hints: hints || {}
    };

    this._commandStack.execute('shape.delete', context);
  };


  Modeling.prototype.removeConnection = function(connection, hints) {
    var context = {
      connection: connection,
      hints: hints || {}
    };

    this._commandStack.execute('connection.delete', context);
  };

  Modeling.prototype.replaceShape = function(oldShape, newShape, hints) {
    var context = {
      oldShape: oldShape,
      newData: newShape,
      hints: hints || {}
    };

    this._commandStack.execute('shape.replace', context);

    return context.newShape;
  };

  Modeling.prototype.pasteElements = function(tree, topParent, position) {
    var context = {
      tree: tree,
      topParent: topParent,
      position: position
    };

    this._commandStack.execute('elements.paste', context);
  };

  Modeling.prototype.alignElements = function(elements, alignment) {
    var context = {
      elements: elements,
      alignment: alignment
    };

    this._commandStack.execute('elements.align', context);
  };

  Modeling.prototype.resizeShape = function(shape, newBounds, minBounds) {
    var context = {
      shape: shape,
      newBounds: newBounds,
      minBounds: minBounds
    };

    this._commandStack.execute('shape.resize', context);
  };

  Modeling.prototype.createSpace = function(movingShapes, resizingShapes, delta, direction) {
    var context = {
      movingShapes: movingShapes,
      resizingShapes: resizingShapes,
      delta: delta,
      direction: direction
    };

    this._commandStack.execute('spaceTool', context);
  };

  Modeling.prototype.updateWaypoints = function(connection, newWaypoints, hints) {
    var context = {
      connection: connection,
      newWaypoints: newWaypoints,
      hints: hints || {}
    };

    this._commandStack.execute('connection.updateWaypoints', context);
  };

  Modeling.prototype.reconnectStart = function(connection, newSource, dockingOrPoints) {
    var context = {
      connection: connection,
      newSource: newSource,
      dockingOrPoints: dockingOrPoints
    };

    this._commandStack.execute('connection.reconnectStart', context);
  };

  Modeling.prototype.reconnectEnd = function(connection, newTarget, dockingOrPoints) {
    var context = {
      connection: connection,
      newTarget: newTarget,
      dockingOrPoints: dockingOrPoints
    };

    this._commandStack.execute('connection.reconnectEnd', context);
  };

  Modeling.prototype.connect = function(source, target, attrs, hints) {
    return this.createConnection(source, target, attrs || {}, source.parent, hints);
  };

  Modeling.prototype._create = function(type, attrs) {
    if (attrs instanceof Base) {
      return attrs;
    } else {
      return this._elementFactory.create(type, attrs);
    }
  };

  Modeling.prototype.toggleCollapse = function(shape, hints) {
    var context = {
      shape: shape,
      hints: hints || {}
    };

    this._commandStack.execute('shape.toggleCollapse', context);
  };

  inherits_browser(Modeling$1, Modeling);

  Modeling$1.$inject = [
    'eventBus',
    'elementFactory',
    'commandStack',
    'textRenderer',
    'canvas'
  ];

  function Modeling$1(eventBus, elementFactory, commandStack, textRenderer, canvas) {
    this.canvas = canvas;
    this._textRenderer = textRenderer;

    Modeling.call(this, eventBus, elementFactory, commandStack);
  }

  Modeling$1.prototype.getHandlers = function() {
    var handlers = Modeling.prototype.getHandlers.call(this);

    handlers['element.updateLabel'] = UpdateLabelHandler;
    handlers['layout.connection.labels'] = LayoutConnectionLabelsHandler;
    handlers['element.hide'] = HideElementHandler;

    return handlers;
  };

  Modeling$1.prototype.updateLabel = function(element, newLabel) {
    this._commandStack.execute('element.updateLabel', {
      element: element,
      newLabel: newLabel
    });
  };

  Modeling$1.prototype.connect = function(source, target, connectionType, attrs, hints) {
    return this.createConnection(source, target, assign({
      connectionType: connectionType
    }, attrs), source.parent);
  };

  Modeling$1.prototype.hide = function(element) {
    var context = {
      element: element
    };

    this._commandStack.execute('element.hide', context);
  };

  var UmlModelingModule = {
    __init__: [
      'umlUpdater',
      'modeling'
    ],
    __depends__: [
      CommandModule
    ],
    connectionDocking: [ 'type', CroppingConnectionDocking ],
    elementFactory: [ 'type', ElementFactory$1 ],
    modeling: [ 'type', Modeling$1 ],
    umlUpdater: [ 'type', UmlUpdater ],
    layouter: [ 'type', UmlLayouter ]
  };

  var LOW_PRIORITY$9 = 750;

  var MARKER_OK$4 = 'drop-ok',
      MARKER_NOT_OK$4 = 'drop-not-ok',
      MARKER_ATTACH$2 = 'attach-ok',
      MARKER_NEW_PARENT$2 = 'new-parent';

  function Create$1(
      eventBus, dragging, modeling,
      canvas, styles, graphicsFactory) {

    /** set drop marker on an element */
    function setMarker(element, marker) {

      [ MARKER_ATTACH$2, MARKER_OK$4, MARKER_NOT_OK$4, MARKER_NEW_PARENT$2 ].forEach(function(m) {

        if (m === marker) {
          canvas.addMarker(element, m);
        } else {
          canvas.removeMarker(element, m);
        }
      });
    }

    function createVisual(shape) {
      var group, preview, visual;

      group = create('g');
      attr$1(group, styles.cls('djs-drag-group', [ 'no-events' ]));

      append(canvas.getDefaultLayer(), group);

      preview = create('g');
      classes$1(preview).add('djs-dragger');

      append(group, preview);

      translate(preview, shape.width / -2, shape.height / -2);

      var visualGroup = create('g');
      classes$1(visualGroup).add('djs-visual');

      append(preview, visualGroup);

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
          setMarker(hover, MARKER_ATTACH$2);
        } else {
          setMarker(hover, canExecute ? MARKER_NEW_PARENT$2 : MARKER_NOT_OK$4);
        }
      }
    });

    eventBus.on('create.move', LOW_PRIORITY$9, function(event) {

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
        remove$1(context.visual);
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

  Create$1.$inject = [
    'eventBus',
    'dragging',
    'modeling',
    'canvas',
    'styles',
    'graphicsFactory'
  ];

  var CreateModule$1 = {
    __depends__: [
      DraggingModule
    ],
    create: [ 'type', Create$1 ]
  };

  PaletteProvider.$inject = [
    'create',
    'elementFactory',
    'lassoTool',
    'palette',
    'textRenderer',
    'eventBus'
  ];

  function PaletteProvider(create, elementFactory, lassoTool, palette, textRenderer, eventBus) {
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
      }
      return {
        group: group,
        className: className,
        title: title,
        action: {
          dragstart: createListener,
          click: createListener
        }
      };
    }
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

  var PaletteProviderModule = {
    __depends__: [
      CreateModule$1,
      PaletteModule,
      LassoToolModule
    ],
    __init__: [ 'paletteProvider' ],
    paletteProvider: [ 'type', PaletteProvider ]
  };

  var ModelingModule = {
    __depends__: [
      CommandModule,
      ChangeSupportModule,
      SelectionModule,
      RulesModule
    ],
    __init__: [ 'modeling' ],
    modeling: [ 'type', Modeling ],
    layouter: [ 'type', BaseLayouter ]
  };

  ContextPadProvider.$inject = [
    'connect',
    'contextPad',
    'modeling',
    'eventBus'
  ];

  function ContextPadProvider(connect, contextPad, modeling, eventBus) {
    this._connect = connect;
    this._modeling = modeling;
    this._eventBus = eventBus;
    this._contextPad = contextPad;

    contextPad.registerProvider(this);
  }

  ContextPadProvider.prototype.getContextPadEntries = function(element) {
    var modeling = this._modeling,
        connect = this._connect,
        eventBus = this._eventBus,
        contextPad = this._contextPad;

    function removeElement(event, element) {
      eventBus.fire('delete.element', {
        context: {
          element: element
        }
      });
    }

    function startConnect(event, element, type) {
      connect.start(event, element, type);
    }

    function addProperty(event, element) {
      assign(event, {
        context: {
          shape: element
        }
      });

      eventBus.fire('create.class.property', event);
    }

    function hide(event, element) {
      modeling.hide(element);

      contextPad.close();
    }

    function getConnectionPadEntry(type, title) {
      return  {
        group: 'add',
        className: 'context-pad-icon-' + type,
        title: title,
        action: {
          click: function(event, element) {
            startConnect(event, element, type);
          },
          dragstart: function(event, element) {
            startConnect(event, element, type);
          }
        }
      }
    }

    function getRemoveShapePadEntry() {
      return {
        group: 'admin',
        className: 'context-pad-icon-delete',
        title: 'Remove',
        action: {
          click: removeElement,
          dragstart: removeElement
        }
      };
    }

    function getCreatePropertyPadEntry() {
      return {
        group: 'add',
        className: 'context-pad-icon-create-property',
        title: 'Create Property',
        action: {
          click: addProperty
        }
      };
    }

    function getHidePadEntry() {
      return {
        group: 'admin',
        className: 'context-pad-icon-hide',
        title: 'Hide Part',
        action: {
          click: hide
        }
      };
    }

    function getGoToPadEntry() {
      return {
        group: 'admin',
        className: 'context-pad-icon-goto',
        title: 'Go to',
        action: {
          click: function(event, element) {
            eventBus.fire('element.goto', {
              context: {
                element: element
              }
            });
          }
        }
      };
    }

    var contextPadEntries = {
      'delete': getRemoveShapePadEntry(),
      'hide': getHidePadEntry()
    };

    if(element instanceof Shape && !(element instanceof Label)) {
      if(!('stereotypes' in element && element.stereotypes.indexOf('enumeration') != -1)) {
        assign(contextPadEntries, {
          'inheritance-connection': getConnectionPadEntry('inheritance', 'Inheritance Connection'),
          'composition-connection': getConnectionPadEntry('composition', 'Composition Connection'),
          'association-connection': getConnectionPadEntry('association', 'Association Connection'),
          'create-property': getCreatePropertyPadEntry()
        });
      }

      if(element.isImported) {
        assign(contextPadEntries, {
          'goto': getGoToPadEntry()
        });
      }
    }

    return contextPadEntries;
  };

  var ContextPadProviderModule = {
    __depends__: [
      ContextPadModule,
      ConnectModule,
      ModelingModule
    ],
    __init__: [ 'contextPadProvider' ],
    contextPadProvider: [ 'type', ContextPadProvider ]
  };

  /**
   * A basic provider that may be extended to implement modeling rules.
   *
   * Extensions should implement the init method to actually add their custom
   * modeling checks. Checks may be added via the #addRule(action, fn) method.
   *
   * @param {EventBus} eventBus
   */
  function RuleProvider(eventBus) {
    CommandInterceptor.call(this, eventBus);

    this.init();
  }

  RuleProvider.$inject = [ 'eventBus' ];

  inherits_browser(RuleProvider, CommandInterceptor);


  /**
   * Adds a modeling rule for the given action, implemented through
   * a callback function.
   *
   * The function will receive the modeling specific action context
   * to perform its check. It must return `false` to disallow the
   * action from happening or `true` to allow the action.
   *
   * A rule provider may pass over the evaluation to lower priority
   * rules by returning return nothing (or <code>undefined</code>).
   *
   * @example
   *
   * ResizableRules.prototype.init = function() {
   *
   *   \/**
   *    * Return `true`, `false` or nothing to denote
   *    * _allowed_, _not allowed_ and _continue evaluating_.
   *    *\/
   *   this.addRule('shape.resize', function(context) {
   *
   *     var shape = context.shape;
   *
   *     if (!context.newBounds) {
   *       // check general resizability
   *       if (!shape.resizable) {
   *         return false;
   *       }
   *
   *       // not returning anything (read: undefined)
   *       // will continue the evaluation of other rules
   *       // (with lower priority)
   *       return;
   *     } else {
   *       // element must have minimum size of 10*10 points
   *       return context.newBounds.width > 10 && context.newBounds.height > 10;
   *     }
   *   });
   * };
   *
   * @param {String|Array<String>} actions the identifier for the modeling action to check
   * @param {Number} [priority] the priority at which this rule is being applied
   * @param {Function} fn the callback function that performs the actual check
   */
  RuleProvider.prototype.addRule = function(actions, priority, fn) {

    var self = this;

    if (typeof actions === 'string') {
      actions = [ actions ];
    }

    actions.forEach(function(action) {

      self.canExecute(action, priority, function(context, action, event) {
        return fn(context);
      }, true);
    });
  };

  /**
   * Implement this method to add new rules during provider initialization.
   */
  RuleProvider.prototype.init = function() {};

  inherits_browser(UmlRulesProvider, RuleProvider);

  UmlRulesProvider.$inject = [ 'eventBus' ];

  function UmlRulesProvider(eventBus) {
    RuleProvider.call(this, eventBus);
  }

  UmlRulesProvider.prototype.init = function() {
    this.addRule('shape.resize', function(context) {
      var shape = context.shape;

      return (shape instanceof Shape && !(shape instanceof Label));
    });

    this.addRule('elements.move', function(context) {
      var shapes = context.shapes;

      if(shapes.some(function(shape) {
        return shape instanceof Label;
      })) {
        return false;
      }

      if(context.target instanceof Label || (context.target instanceof Shape && !(context.target instanceof Root))) {
        return false;
      }

      return true;
    });
  };

  var UmlRulesProviderModule = {
    __depends__: [
      RulesModule
    ],
    __init__: [ 'umlRulesProvider' ],
    umlRulesProvider: [ 'type', UmlRulesProvider ]
  };

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

        set$1('grab');
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

  function round$7(number, resolution) {
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
      this._viewboxChanged = debounce(bind(this._viewboxChanged, this), 300);
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
    add$1(parent.children, element, parentIndex);
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
      scale = round$7(matrix.a, 1000);

      x = round$7(-matrix.e || 0, 1000);
      y = round$7(-matrix.f || 0, 1000);

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

    return round$7(matrix.a, 1000);
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

  var FN_REF = '__fn';

  var DEFAULT_PRIORITY$1 = 1000;

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
      priority = DEFAULT_PRIORITY$1;
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
      priority = DEFAULT_PRIORITY$1;
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

    parents = reduce(elements, function(map$$1, e) {

      if (e.parent) {
        map$$1[e.parent.id] = e.parent;
      }

      return map$$1;
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

  inherits_browser(Modeler, Viewer);

  function Modeler(options) {
    Viewer.call(this, options);
  }

  Modeler.prototype._modelingModules = [
    SelectionModule,
    MoveModule,
    OutlineModule,
    LassoToolModule,
    PaletteModule,
    CreateModule,
    ContextPadModule,
    ConnectModule,
    RulesModule,
    PaletteProviderModule,
    ContextPadProviderModule,
    UmlRulesProviderModule,
    UmlModelingModule,
    LabelSupportModule,
    ChangeSupportModule,
    ResizeModule,
    DirectEditingModule,
    BendpointsModule
  ];

  Modeler.prototype._modules = [].concat(
    Modeler.prototype._modules,
    Modeler.prototype._modelingModules
  );

  return Modeler;

})));
