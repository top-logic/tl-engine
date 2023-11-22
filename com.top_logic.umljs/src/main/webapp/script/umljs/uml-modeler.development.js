/*!
 * uml-js - uml-modeler v1.0.0
 *
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 *
 * Date: 2023-11-22
 */

(function (global, factory) {
  typeof exports === 'object' && typeof module !== 'undefined' ? module.exports = factory() :
  typeof define === 'function' && define.amd ? define(factory) :
  (global = typeof globalThis !== 'undefined' ? globalThis : global || self, global.UmlJS = factory());
})(this, (function () { 'use strict';

  /**
   * Flatten array, one level deep.
   *
   * @template T
   *
   * @param {T[][]} arr
   *
   * @return {T[]}
   */
  function flatten(arr) {
    return Array.prototype.concat.apply([], arr);
  }

  const nativeToString$1 = Object.prototype.toString;
  const nativeHasOwnProperty$1 = Object.prototype.hasOwnProperty;

  function isUndefined$1(obj) {
    return obj === undefined;
  }

  function isDefined(obj) {
    return obj !== undefined;
  }

  function isNil(obj) {
    return obj == null;
  }

  function isArray$3(obj) {
    return nativeToString$1.call(obj) === '[object Array]';
  }

  function isObject(obj) {
    return nativeToString$1.call(obj) === '[object Object]';
  }

  function isNumber(obj) {
    return nativeToString$1.call(obj) === '[object Number]';
  }

  /**
   * @param {any} obj
   *
   * @return {boolean}
   */
  function isFunction(obj) {
    const tag = nativeToString$1.call(obj);

    return (
      tag === '[object Function]' ||
      tag === '[object AsyncFunction]' ||
      tag === '[object GeneratorFunction]' ||
      tag === '[object AsyncGeneratorFunction]' ||
      tag === '[object Proxy]'
    );
  }

  function isString(obj) {
    return nativeToString$1.call(obj) === '[object String]';
  }


  /**
   * Ensure collection is an array.
   *
   * @param {Object} obj
   */
  function ensureArray(obj) {

    if (isArray$3(obj)) {
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
  function has$1(target, key) {
    return nativeHasOwnProperty$1.call(target, key);
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

    forEach$1(collection, function(val, key) {
      if (matchFn(val, key)) {
        match = val;

        return false;
      }
    });

    return match;

  }


  /**
   * Filter elements in collection.
   *
   * @template T
   * @param {Collection<T>} collection
   * @param {Matcher<T>} matcher
   *
   * @return {T[]} result
   */
  function filter(collection, matcher) {

    const matchFn = toMatcher(matcher);

    let result = [];

    forEach$1(collection, function(val, key) {
      if (matchFn(val, key)) {
        result.push(val);
      }
    });

    return result;
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
  function forEach$1(collection, iterator) {

    let val,
        result;

    if (isUndefined$1(collection)) {
      return;
    }

    const convertKey = isArray$3(collection) ? toNum$1 : identity$1;

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

  /**
   * Return collection without element.
   *
   * @template T
   * @param {ArrayCollection<T>} arr
   * @param {Matcher<T>} matcher
   *
   * @return {T[]}
   */
  function without(arr, matcher) {

    if (isUndefined$1(arr)) {
      return [];
    }

    ensureArray(arr);

    const matchFn = toMatcher(matcher);

    return arr.filter(function(el, idx) {
      return !matchFn(el, idx);
    });

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

    forEach$1(collection, function(value, idx) {
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
  function map$1(collection, fn) {

    let result = [];

    forEach$1(collection, function(val, key) {
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
    return map$1(collection, (val) => val);
  }


  /**
   * Group collection members by attribute.
   *
   * @param {Object|Array} collection
   * @param {Extractor} extractor
   *
   * @return {Object} map with { attrValue => [ a, b, c ] }
   */
  function groupBy(collection, extractor, grouped = {}) {

    extractor = toExtractor(extractor);

    forEach$1(collection, function(val) {
      let discriminator = extractor(val) || '_';

      let group = grouped[discriminator];

      if (!group) {
        group = grouped[discriminator] = [];
      }

      group.push(val);
    });

    return grouped;
  }


  function uniqueBy(extractor, ...collections) {

    extractor = toExtractor(extractor);

    let grouped = {};

    forEach$1(collections, (c) => groupBy(c, extractor, grouped));

    let result = map$1(grouped, function(val, key) {
      return val[0];
    });

    return result;
  }



  /**
   * Sort collection by criteria.
   *
   * @template T
   *
   * @param {Collection<T>} collection
   * @param {Extractor<T, number | string>} extractor
   *
   * @return {Array}
   */
  function sortBy(collection, extractor) {

    extractor = toExtractor(extractor);

    let sorted = [];

    forEach$1(collection, function(value, key) {
      let disc = extractor(value, key);

      let entry = {
        d: disc,
        v: value
      };

      for (var idx = 0; idx < sorted.length; idx++) {
        let { d } = sorted[idx];

        if (disc < d) {
          sorted.splice(idx, 0, entry);
          return;
        }
      }

      // not inserted, append (!)
      sorted.push(entry);
    });

    return map$1(sorted, (e) => e.v);
  }


  /**
   * Create an object pattern matcher.
   *
   * @example
   *
   * ```javascript
   * const matcher = matchPattern({ id: 1 });
   *
   * let element = find(elements, matcher);
   * ```
   *
   * @template T
   *
   * @param {T} pattern
   *
   * @return { (el: any) =>  boolean } matcherFn
   */
  function matchPattern(pattern) {

    return function(el) {

      return every(pattern, function(val, key) {
        return el[key] === val;
      });

    };
  }


  /**
   * @param {string | ((e: any) => any) } extractor
   *
   * @return { (e: any) => any }
   */
  function toExtractor(extractor) {

    /**
     * @satisfies { (e: any) => any }
     */
    return isFunction(extractor) ? extractor : (e) => {

      // @ts-ignore: just works
      return e[extractor];
    };
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


  function identity$1(arg) {
    return arg;
  }

  function toNum$1(arg) {
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
  function bind$2(fn, target) {
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
  function assign$1(target, ...others) {
    return Object.assign(target, ...others);
  }

  /**
   * Pick properties from the given target.
   *
   * @template T
   * @template {any[]} V
   *
   * @param {T} target
   * @param {V} properties
   *
   * @return Pick<T, V>
   */
  function pick(target, properties) {

    let result = {};

    let obj = Object(target);

    forEach$1(properties, function(prop) {

      if (prop in obj) {
        result[prop] = target[prop];
      }
    });

    return result;
  }

  /**
   * Pick all target properties, excluding the given ones.
   *
   * @template T
   * @template {any[]} V
   *
   * @param {T} target
   * @param {V} properties
   *
   * @return {Omit<T, V>} target
   */
  function omit(target, properties) {

    let result = {};

    let obj = Object(target);

    forEach$1(obj, function(prop, key) {

      if (properties.indexOf(key) === -1) {
        result[key] = prop;
      }
    });

    return result;
  }

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

  const nativeToString = Object.prototype.toString;
  const nativeHasOwnProperty = Object.prototype.hasOwnProperty;

  function isUndefined(obj) {
    return obj === undefined;
  }

  function isArray$2(obj) {
    return nativeToString.call(obj) === '[object Array]';
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

    let val,
        result;

    if (isUndefined(collection)) {
      return;
    }

    const convertKey = isArray$2(collection) ? toNum : identity;

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


  function identity(arg) {
    return arg;
  }

  function toNum(arg) {
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
  function assign(element, ...styleSources) {
    const target = element.style;

    forEach(styleSources, function(style) {
      if (!style) {
        return;
      }

      forEach(style, function(value, key) {
        target[key] = value;
      });
    });

    return element;
  }

  /**
   * Set attribute `name` to `val`, or get attr `name`.
   *
   * @param {Element} el
   * @param {String} name
   * @param {String} [val]
   * @api public
   */
  function attr$1(el, name, val) {

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
  var forceCaptureEvents = [ 'focus', 'blur' ];

  function bind(el, selector, type, fn, capture) {
    if (forceCaptureEvents.indexOf(type) !== -1) {
      capture = true;
    }

    return event.bind(el, type, function(e) {
      var target = e.target || e.srcElement;
      e.delegateTarget = closest(target, selector, true);
      if (e.delegateTarget) {
        fn.call(el, e);
      }
    }, capture);
  }

  /**
   * Unbind event `type`'s callback `fn`.
   *
   * @param {Element} el
   * @param {String} type
   * @param {Function} fn
   * @param {Boolean} capture
   * @api public
   */
  function unbind(el, type, fn, capture) {
    if (forceCaptureEvents.indexOf(type) !== -1) {
      capture = true;
    }

    return event.unbind(el, type, fn, capture);
  }

  var delegate = {
    bind,
    unbind
  };

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

  function all(selector, el) {
    el = el || document;

    return el.querySelectorAll(selector);
  }

  function remove$2(el) {
    el.parentNode && el.parentNode.removeChild(el);
  }

  /**
   * @typedef {import('../util/Types').Point} Point
   */

  function __stopPropagation(event) {
    if (!event || typeof event.stopPropagation !== 'function') {
      return;
    }

    event.stopPropagation();
  }

  /**
   * @param {import('../core/EventBus').Event} event
   *
   * @return {Event}
   */
  function getOriginal$1(event) {
    return event.originalEvent || event.srcEvent;
  }

  /**
   * @param {Event|import('../core/EventBus').Event} event
   */
  function stopPropagation$1(event) {
    __stopPropagation(event);
    __stopPropagation(getOriginal$1(event));
  }

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

  function isMac() {
    return (/mac/i).test(navigator.platform);
  }

  /**
   * @param {MouseEvent} event
   * @param {string} button
   *
   * @return {boolean}
   */
  function isButton(event, button) {
    return (getOriginal$1(event) || event).button === button;
  }

  /**
   * @param {MouseEvent} event
   *
   * @return {boolean}
   */
  function isPrimaryButton(event) {

    // button === 0 -> left áka primary mouse button
    return isButton(event, 0);
  }

  /**
   * @param {MouseEvent} event
   *
   * @return {boolean}
   */
  function isAuxiliaryButton(event) {

    // button === 1 -> auxiliary áka wheel button
    return isButton(event, 1);
  }

  /**
   * @param {MouseEvent} event
   *
   * @return {boolean}
   */
  function hasPrimaryModifier(event) {
    var originalEvent = getOriginal$1(event) || event;

    if (!isPrimaryButton(event)) {
      return false;
    }

    // Use cmd as primary modifier key for mac OS
    if (isMac()) {
      return originalEvent.metaKey;
    } else {
      return originalEvent.ctrlKey;
    }
  }

  /**
   * @param {MouseEvent} event
   *
   * @return {boolean}
   */
  function hasSecondaryModifier(event) {
    var originalEvent = getOriginal$1(event) || event;

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

  function clone$1(element) {
    return element.cloneNode(true);
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


  function set$1(element, svg) {

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
        set$1(element, svg);
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
  function transform$1(node, transforms) {
    var transformList = node.transform.baseVal;

    if (transforms) {

      if (!Array.isArray(transforms)) {
        transforms = [ transforms ];
      }

      setTransforms(transformList, transforms);
    }

    return transformList.consolidate();
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
        vectorLength$1(point.x - pointBefore.x, point.y - pointBefore.y),
        vectorLength$1(pointAfter.x - point.x, pointAfter.y - point.y)
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

    const totalLength = vectorLength$1(deltaX, deltaY);

    const percent = length / totalLength;

    return {
      x: start.x + deltaX * percent,
      y: start.y + deltaY * percent
    };
  }

  function vectorLength$1(x, y) {
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

  /**
   * @typedef {import('../../model/Types').Element} Element
   *
   * @typedef {import('../../core/ElementRegistry').default} ElementRegistry
   * @typedef {import('../../core/EventBus').default} EventBus
   * @typedef {import('../../draw/Styles').default} Styles
   *
   * @typedef {import('../../util/Types').Point} Point
   */

  function allowAll(event) { return true; }

  function allowPrimaryAndAuxiliary(event) {
    return isPrimaryButton(event) || isAuxiliaryButton(event);
  }

  var LOW_PRIORITY$8 = 500;


  /**
   * A plugin that provides interaction events for diagram elements.
   *
   * It emits the following events:
   *
   *   * element.click
   *   * element.contextmenu
   *   * element.dblclick
   *   * element.hover
   *   * element.mousedown
   *   * element.mousemove
   *   * element.mouseup
   *   * element.out
   *
   * Each event is a tuple { element, gfx, originalEvent }.
   *
   * Canceling the event via Event#preventDefault()
   * prevents the original DOM operation.
   *
   * @param {EventBus} eventBus
   * @param {ElementRegistry} elementRegistry
   * @param {Styles} styles
   */
  function InteractionEvents(eventBus, elementRegistry, styles) {

    var self = this;

    /**
     * Fire an interaction event.
     *
     * @param {string} type local event name, e.g. element.click.
     * @param {MouseEvent|TouchEvent} event native event
     * @param {Element} [element] the diagram element to emit the event on;
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

    // TODO(nikku): document this
    var handlers = {};

    function mouseHandler(localEventName) {
      return handlers[localEventName];
    }

    function isIgnored(localEventName, event) {

      var filter = ignoredFilters[localEventName] || isPrimaryButton;

      // only react on left mouse button interactions
      // except for interaction events that are enabled
      // for secundary mouse button
      return !filter(event);
    }

    var bindings = {
      click: 'element.click',
      contextmenu: 'element.contextmenu',
      dblclick: 'element.dblclick',
      mousedown: 'element.mousedown',
      mousemove: 'element.mousemove',
      mouseover: 'element.hover',
      mouseout: 'element.out',
      mouseup: 'element.mouseup',
    };

    var ignoredFilters = {
      'element.contextmenu': allowAll,
      'element.mousedown': allowPrimaryAndAuxiliary,
      'element.mouseup': allowPrimaryAndAuxiliary,
      'element.click': allowPrimaryAndAuxiliary,
      'element.dblclick': allowPrimaryAndAuxiliary
    };


    // manual event trigger //////////

    /**
     * Trigger an interaction event (based on a native dom event)
     * on the target shape or connection.
     *
     * @param {string} eventName the name of the triggered DOM event
     * @param {MouseEvent|TouchEvent} event
     * @param {Element} targetElement
     */
    function triggerMouseEvent(eventName, event, targetElement) {

      // i.e. element.mousedown...
      var localEventName = bindings[eventName];

      if (!localEventName) {
        throw new Error('unmapped DOM event name <' + eventName + '>');
      }

      return fire(localEventName, event, targetElement);
    }


    var ELEMENT_SELECTOR = 'svg, .djs-element';

    // event handling ///////

    function registerEvent(node, event, localEvent, ignoredFilter) {

      var handler = handlers[localEvent] = function(event) {
        fire(localEvent, event);
      };

      if (ignoredFilter) {
        ignoredFilters[localEvent] = ignoredFilter;
      }

      handler.$delegate = delegate.bind(node, ELEMENT_SELECTOR, event, handler);
    }

    function unregisterEvent(node, event, localEvent) {

      var handler = mouseHandler(localEvent);

      if (!handler) {
        return;
      }

      delegate.unbind(node, event, handler.$delegate);
    }

    function registerEvents(svg) {
      forEach$1(bindings, function(val, key) {
        registerEvent(svg, key, val);
      });
    }

    function unregisterEvents(svg) {
      forEach$1(bindings, function(val, key) {
        unregisterEvent(svg, key, val);
      });
    }

    eventBus.on('canvas.destroy', function(event) {
      unregisterEvents(event.svg);
    });

    eventBus.on('canvas.init', function(event) {
      registerEvents(event.svg);
    });


    // hit box updating ////////////////

    eventBus.on([ 'shape.added', 'connection.added' ], function(event) {
      var element = event.element,
          gfx = event.gfx;

      eventBus.fire('interactionEvents.createHit', { element: element, gfx: gfx });
    });

    // Update djs-hit on change.
    // A low priortity is necessary, because djs-hit of labels has to be updated
    // after the label bounds have been updated in the renderer.
    eventBus.on([
      'shape.changed',
      'connection.changed'
    ], LOW_PRIORITY$8, function(event) {

      var element = event.element,
          gfx = event.gfx;

      eventBus.fire('interactionEvents.updateHit', { element: element, gfx: gfx });
    });

    eventBus.on('interactionEvents.createHit', LOW_PRIORITY$8, function(event) {
      var element = event.element,
          gfx = event.gfx;

      self.createDefaultHit(element, gfx);
    });

    eventBus.on('interactionEvents.updateHit', function(event) {
      var element = event.element,
          gfx = event.gfx;

      self.updateDefaultHit(element, gfx);
    });


    // hit styles ////////////

    var STROKE_HIT_STYLE = createHitStyle('djs-hit djs-hit-stroke');

    var CLICK_STROKE_HIT_STYLE = createHitStyle('djs-hit djs-hit-click-stroke');

    var ALL_HIT_STYLE = createHitStyle('djs-hit djs-hit-all');

    var NO_MOVE_HIT_STYLE = createHitStyle('djs-hit djs-hit-no-move');

    var HIT_TYPES = {
      'all': ALL_HIT_STYLE,
      'click-stroke': CLICK_STROKE_HIT_STYLE,
      'stroke': STROKE_HIT_STYLE,
      'no-move': NO_MOVE_HIT_STYLE
    };

    function createHitStyle(classNames, attrs) {

      attrs = assign$1({
        stroke: 'white',
        strokeWidth: 15
      }, attrs || {});

      return styles.cls(classNames, [ 'no-fill', 'no-border' ], attrs);
    }


    // style helpers ///////////////

    function applyStyle(hit, type) {

      var attrs = HIT_TYPES[type];

      if (!attrs) {
        throw new Error('invalid hit type <' + type + '>');
      }

      attr(hit, attrs);

      return hit;
    }

    function appendHit(gfx, hit) {
      append(gfx, hit);
    }


    // API

    /**
     * Remove hints on the given graphics.
     *
     * @param {SVGElement} gfx
     */
    this.removeHits = function(gfx) {
      var hits = all('.djs-hit', gfx);

      forEach$1(hits, remove$1);
    };

    /**
     * Create default hit for the given element.
     *
     * @param {Element} element
     * @param {SVGElement} gfx
     *
     * @return {SVGElement} created hit
     */
    this.createDefaultHit = function(element, gfx) {
      var waypoints = element.waypoints,
          isFrame = element.isFrame,
          boxType;

      if (waypoints) {
        return this.createWaypointsHit(gfx, waypoints);
      } else {

        boxType = isFrame ? 'stroke' : 'all';

        return this.createBoxHit(gfx, boxType, {
          width: element.width,
          height: element.height
        });
      }
    };

    /**
     * Create hits for the given waypoints.
     *
     * @param {SVGElement} gfx
     * @param {Point[]} waypoints
     *
     * @return {SVGElement}
     */
    this.createWaypointsHit = function(gfx, waypoints) {

      var hit = createLine(waypoints);

      applyStyle(hit, 'stroke');

      appendHit(gfx, hit);

      return hit;
    };

    /**
     * Create hits for a box.
     *
     * @param {SVGElement} gfx
     * @param {string} type
     * @param {Object} attrs
     *
     * @return {SVGElement}
     */
    this.createBoxHit = function(gfx, type, attrs) {

      attrs = assign$1({
        x: 0,
        y: 0
      }, attrs);

      var hit = create$1('rect');

      applyStyle(hit, type);

      attr(hit, attrs);

      appendHit(gfx, hit);

      return hit;
    };

    /**
     * Update default hit of the element.
     *
     * @param {Element} element
     * @param {SVGElement} gfx
     *
     * @return {SVGElement} updated hit
     */
    this.updateDefaultHit = function(element, gfx) {

      var hit = query('.djs-hit', gfx);

      if (!hit) {
        return;
      }

      if (element.waypoints) {
        updateLine(hit, element.waypoints);
      } else {
        attr(hit, {
          width: element.width,
          height: element.height
        });
      }

      return hit;
    };

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
   * @property {Element} element
   * @property {SVGElement} gfx
   * @property {Event} originalEvent
   */

  /**
   * An event indicating that the mouse has left an element
   *
   * @event element.out
   *
   * @type {Object}
   * @property {Element} element
   * @property {SVGElement} gfx
   * @property {Event} originalEvent
   */

  /**
   * An event indicating that the mouse has clicked an element
   *
   * @event element.click
   *
   * @type {Object}
   * @property {Element} element
   * @property {SVGElement} gfx
   * @property {Event} originalEvent
   */

  /**
   * An event indicating that the mouse has double clicked an element
   *
   * @event element.dblclick
   *
   * @type {Object}
   * @property {Element} element
   * @property {SVGElement} gfx
   * @property {Event} originalEvent
   */

  /**
   * An event indicating that the mouse has gone down on an element.
   *
   * @event element.mousedown
   *
   * @type {Object}
   * @property {Element} element
   * @property {SVGElement} gfx
   * @property {Event} originalEvent
   */

  /**
   * An event indicating that the mouse has gone up on an element.
   *
   * @event element.mouseup
   *
   * @type {Object}
   * @property {Element} element
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
   * @property {Element} element
   * @property {SVGElement} gfx
   * @property {Event} originalEvent
   */

  /**
   * @type { import('didi').ModuleDeclaration }
   */
  var InteractionEventsModule = {
    __init__: [ 'interactionEvents' ],
    interactionEvents: [ 'type', InteractionEvents ]
  };

  /**
   * @typedef {import('../model/Types').Connection} Connection
   * @typedef {import('../model/Types').Element} Element
   * @typedef {import('../model/Types').Shape} Shape
   *
   * @typedef {import('../../type/Types').Rect} Rect
   *
   * @typedef { {
   *   allShapes: Record<string, Shape>,
   *   allConnections: Record<string, Connection>,
   *   topLevel: Record<string, Element>,
   *   enclosedConnections: Record<string, Connection>,
   *   enclosedElements: Record<string, Element>
   * } } Closure
   */

  /**
   * Get parent elements.
   *
   * @param {Element[]} elements
   *
   * @return {Element[]}
   */
  function getParents(elements) {

    // find elements that are not children of any other elements
    return filter(elements, function(element) {
      return !find(elements, function(e) {
        return e !== element && getParent(element, e);
      });
    });
  }


  function getParent(element, parent) {
    if (!parent) {
      return;
    }

    if (element === parent) {
      return parent;
    }

    if (!element.parent) {
      return;
    }

    return getParent(element.parent, parent);
  }


  /**
   * Adds an element to a collection and returns true if the
   * element was added.
   *
   * @param {Object[]} elements
   * @param {Object} element
   * @param {boolean} [unique]
   */
  function add$1(elements, element, unique) {
    var canAdd = !unique || elements.indexOf(element) === -1;

    if (canAdd) {
      elements.push(element);
    }

    return canAdd;
  }


  /**
   * Iterate over each element in a collection, calling the iterator function `fn`
   * with (element, index, recursionDepth).
   *
   * Recurse into all elements that are returned by `fn`.
   *
   * @param {Element|Element[]} elements
   * @param {(element: Element, index: number, depth: number) => Element[] | boolean | undefined} fn
   * @param {number} [depth] maximum recursion depth
   */
  function eachElement(elements, fn, depth) {

    depth = depth || 0;

    if (!isArray$3(elements)) {
      elements = [ elements ];
    }

    forEach$1(elements, function(s, i) {
      var filter = fn(s, i, depth);

      if (isArray$3(filter) && filter.length) {
        eachElement(filter, fn, depth + 1);
      }
    });
  }


  /**
   * Collects self + child elements up to a given depth from a list of elements.
   *
   * @param {Element|Element[]} elements the elements to select the children from
   * @param {boolean} unique whether to return a unique result set (no duplicates)
   * @param {number} maxDepth the depth to search through or -1 for infinite
   *
   * @return {Element[]} found elements
   */
  function selfAndChildren(elements, unique, maxDepth) {
    var result = [],
        processedChildren = [];

    eachElement(elements, function(element, i, depth) {
      add$1(result, element, unique);

      var children = element.children;

      // max traversal depth not reached yet
      if (maxDepth === -1 || depth < maxDepth) {

        // children exist && children not yet processed
        if (children && add$1(processedChildren, children, unique)) {
          return children;
        }
      }
    });

    return result;
  }


  /**
   * Return self + ALL children for a number of elements
   *
   * @param {Element[]} elements to query
   * @param {boolean} [allowDuplicates] to allow duplicates in the result set
   *
   * @return {Element[]} the collected elements
   */
  function selfAndAllChildren(elements, allowDuplicates) {
    return selfAndChildren(elements, !allowDuplicates, -1);
  }


  /**
   * Gets the the closure for all selected elements,
   * their enclosed children and connections.
   *
   * @param {Element[]} elements
   * @param {boolean} [isTopLevel=true]
   * @param {Closure} [closure]
   *
   * @return {Closure} newClosure
   */
  function getClosure(elements, isTopLevel, closure) {

    if (isUndefined$1(isTopLevel)) {
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
        forEach$1(element.incoming, handleConnection);

        forEach$1(element.outgoing, handleConnection);

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
   * @param {Element|Element[]} elements
   * @param {boolean} [stopRecursion=false]
   *
   * @return {Rect}
   */
  function getBBox(elements, stopRecursion) {

    stopRecursion = !!stopRecursion;
    if (!isArray$3(elements)) {
      elements = [ elements ];
    }

    var minX,
        minY,
        maxX,
        maxY;

    forEach$1(elements, function(element) {

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
   * @param {Element[]} elements List of Elements to search through
   * @param {Rect} bbox the enclosing bbox.
   *
   * @return {Element[]} enclosed elements
   */
  function getEnclosedElements(elements, bbox) {

    var filteredElements = {};

    forEach$1(elements, function(element) {

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

  // helpers ///////////////////////////////

  function copyObject(src1, src2) {
    return assign$1({}, src1 || {}, src2 || {});
  }

  var LOW_PRIORITY$7 = 500;

  /**
   * @typedef {import('../../model/Types').Element} Element
   *
   * @typedef {import('../../core/EventBus').default} EventBus
   * @typedef {import('../../draw/Styles').default} Styles
   */

  /**
   * @class
   *
   * A plugin that adds an outline to shapes and connections that may be activated and styled
   * via CSS classes.
   *
   * @param {EventBus} eventBus
   * @param {Styles} styles
   */
  function Outline(eventBus, styles) {

    this.offset = 6;

    var OUTLINE_STYLE = styles.cls('djs-outline', [ 'no-fill' ]);

    var self = this;

    function createOutline(gfx, bounds) {
      var outline = create$1('rect');

      attr(outline, assign$1({
        x: 10,
        y: 10,
        rx: 4,
        width: 100,
        height: 100
      }, OUTLINE_STYLE));

      append(gfx, outline);

      return outline;
    }

    // A low priortity is necessary, because outlines of labels have to be updated
    // after the label bounds have been updated in the renderer.
    eventBus.on([ 'shape.added', 'shape.changed' ], LOW_PRIORITY$7, function(event) {
      var element = event.element,
          gfx = event.gfx;

      var outline = query('.djs-outline', gfx);

      if (!outline) {
        outline = createOutline(gfx);
      }

      self.updateShapeOutline(outline, element);
    });

    eventBus.on([ 'connection.added', 'connection.changed' ], function(event) {
      var element = event.element,
          gfx = event.gfx;

      var outline = query('.djs-outline', gfx);

      if (!outline) {
        outline = createOutline(gfx);
      }

      self.updateConnectionOutline(outline, element);
    });
  }


  /**
   * Updates the outline of a shape respecting the dimension of the
   * element and an outline offset.
   *
   * @param {SVGElement} outline
   * @param {Element} element
   */
  Outline.prototype.updateShapeOutline = function(outline, element) {

    attr(outline, {
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
   * @param {SVGElement} outline
   * @param {Element} connection
   */
  Outline.prototype.updateConnectionOutline = function(outline, connection) {

    var bbox = getBBox(connection);

    attr(outline, {
      x: bbox.x - this.offset,
      y: bbox.y - this.offset,
      width: bbox.width + this.offset * 2,
      height: bbox.height + this.offset * 2
    });

  };


  Outline.$inject = [ 'eventBus', 'styles', 'elementRegistry' ];

  /**
   * @type { import('didi').ModuleDeclaration }
   */
  var OutlineModule = {
    __init__: [ 'outline' ],
    outline: [ 'type', Outline ]
  };

  /**
   * @typedef {import('../../core/Canvas').default} Canvas
   * @typedef {import('../../core/EventBus').default} EventBus
   */

  /**
   * A service that offers the current selection in a diagram.
   * Offers the api to control the selection, too.
   *
   * @param {EventBus} eventBus
   * @param {Canvas} canvas
   */
  function Selection(eventBus, canvas) {

    this._eventBus = eventBus;
    this._canvas = canvas;

    /**
     * @type {Object[]}
     */
    this._selectedElements = [];

    var self = this;

    eventBus.on([ 'shape.remove', 'connection.remove' ], function(e) {
      var element = e.element;
      self.deselect(element);
    });

    eventBus.on([ 'diagram.clear', 'root.set' ], function(e) {
      self.select(null);
    });
  }

  Selection.$inject = [ 'eventBus', 'canvas' ];

  /**
   * Deselect an element.
   *
   * @param {Object} element The element to deselect.
   */
  Selection.prototype.deselect = function(element) {
    var selectedElements = this._selectedElements;

    var idx = selectedElements.indexOf(element);

    if (idx !== -1) {
      var oldSelection = selectedElements.slice();

      selectedElements.splice(idx, 1);

      this._eventBus.fire('selection.changed', { oldSelection: oldSelection, newSelection: selectedElements });
    }
  };

  /**
   * Get the selected elements.
   *
   * @return {Object[]} The selected elements.
   */
  Selection.prototype.get = function() {
    return this._selectedElements;
  };

  /**
   * Check whether an element is selected.
   *
   * @param {Object} element The element.
   *
   * @return {boolean} Whether the element is selected.
   */
  Selection.prototype.isSelected = function(element) {
    return this._selectedElements.indexOf(element) !== -1;
  };


  /**
   * Select one or many elements.
   *
   * @param {Object|Object[]} elements The element(s) to select.
   * @param {boolean} [add] Whether to add the element(s) to the selected elements.
   * Defaults to `false`.
   */
  Selection.prototype.select = function(elements, add) {
    var selectedElements = this._selectedElements,
        oldSelection = selectedElements.slice();

    if (!isArray$3(elements)) {
      elements = elements ? [ elements ] : [];
    }

    var canvas = this._canvas;

    var rootElement = canvas.getRootElement();

    elements = elements.filter(function(element) {
      var elementRoot = canvas.findRoot(element);

      return rootElement === elementRoot;
    });

    // selection may be cleared by passing an empty array or null
    // to the method
    if (add) {
      forEach$1(elements, function(element) {
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

  /**
   * @typedef {import('../../core/Canvas').default} Canvas
   * @typedef {import('../../core/EventBus').default} EventBus
   * @typedef {import('./Selection').default} Selection
   */

  var MARKER_HOVER = 'hover',
      MARKER_SELECTED = 'selected';

  var SELECTION_OUTLINE_PADDING = 6;


  /**
   * A plugin that adds a visible selection UI to shapes and connections
   * by appending the <code>hover</code> and <code>selected</code> classes to them.
   *
   * @class
   *
   * Makes elements selectable, too.
   *
   * @param {Canvas} canvas
   * @param {EventBus} eventBus
   * @param {Selection} selection
   */
  function SelectionVisuals(canvas, eventBus, selection) {
    this._canvas = canvas;

    var self = this;

    this._multiSelectionBox = null;

    function addMarker(e, cls) {
      canvas.addMarker(e, cls);
    }

    function removeMarker(e, cls) {
      canvas.removeMarker(e, cls);
    }

    eventBus.on('element.hover', function(event) {
      addMarker(event.element, MARKER_HOVER);
    });

    eventBus.on('element.out', function(event) {
      removeMarker(event.element, MARKER_HOVER);
    });

    eventBus.on('selection.changed', function(event) {

      function deselect(s) {
        removeMarker(s, MARKER_SELECTED);
      }

      function select(s) {
        addMarker(s, MARKER_SELECTED);
      }

      var oldSelection = event.oldSelection,
          newSelection = event.newSelection;

      forEach$1(oldSelection, function(e) {
        if (newSelection.indexOf(e) === -1) {
          deselect(e);
        }
      });

      forEach$1(newSelection, function(e) {
        if (oldSelection.indexOf(e) === -1) {
          select(e);
        }
      });

      self._updateSelectionOutline(newSelection);
    });


    eventBus.on('element.changed', function(event) {
      if (selection.isSelected(event.element)) {
        self._updateSelectionOutline(selection.get());
      }
    });
  }

  SelectionVisuals.$inject = [
    'canvas',
    'eventBus',
    'selection'
  ];

  SelectionVisuals.prototype._updateSelectionOutline = function(selection) {
    var layer = this._canvas.getLayer('selectionOutline');

    clear(layer);

    var enabled = selection.length > 1;

    var container = this._canvas.getContainer();

    classes(container)[enabled ? 'add' : 'remove']('djs-multi-select');

    if (!enabled) {
      return;
    }

    var bBox = addSelectionOutlinePadding(getBBox(selection));

    var rect = create$1('rect');

    attr(rect, assign$1({
      rx: 3
    }, bBox));

    classes(rect).add('djs-selection-outline');

    append(layer, rect);
  };

  // helpers //////////

  function addSelectionOutlinePadding(bBox) {
    return {
      x: bBox.x - SELECTION_OUTLINE_PADDING,
      y: bBox.y - SELECTION_OUTLINE_PADDING,
      width: bBox.width + SELECTION_OUTLINE_PADDING * 2,
      height: bBox.height + SELECTION_OUTLINE_PADDING * 2
    };
  }

  /**
   * @typedef {import('../../core/Canvas').default} Canvas
   * @typedef {import('../../core/ElementRegistry').default} ElementRegistry
   * @typedef {import('../../core/EventBus').default} EventBus
   * @typedef {import('./Selection').default} Selection
   */

  /**
   * @param {EventBus} eventBus
   * @param {Selection} selection
   * @param {Canvas} canvas
   * @param {ElementRegistry} elementRegistry
   */
  function SelectionBehavior(eventBus, selection, canvas, elementRegistry) {

    // Select elements on create
    eventBus.on('create.end', 500, function(event) {
      var context = event.context,
          canExecute = context.canExecute,
          elements = context.elements,
          hints = context.hints || {},
          autoSelect = hints.autoSelect;

      if (canExecute) {
        if (autoSelect === false) {

          // Select no elements
          return;
        }

        if (isArray$3(autoSelect)) {
          selection.select(autoSelect);
        } else {

          // Select all elements by default
          selection.select(elements.filter(isShown));
        }
      }
    });

    // Select connection targets on connect
    eventBus.on('connect.end', 500, function(event) {
      var context = event.context,
          connection = context.connection;

      if (connection) {
        selection.select(connection);
      }
    });

    // Select shapes on move
    eventBus.on('shape.move.end', 500, function(event) {
      var previousSelection = event.previousSelection || [];

      var shape = elementRegistry.get(event.context.shape.id);

      // Always select main shape on move
      var isSelected = find(previousSelection, function(selectedShape) {
        return shape.id === selectedShape.id;
      });

      if (!isSelected) {
        selection.select(shape);
      }
    });

    // Select elements on click
    eventBus.on('element.click', function(event) {

      if (!isPrimaryButton(event)) {
        return;
      }

      var element = event.element;

      if (element === canvas.getRootElement()) {
        element = null;
      }

      var isSelected = selection.isSelected(element),
          isMultiSelect = selection.get().length > 1;

      // Add to selection if CTRL or SHIFT pressed
      var add = hasPrimaryModifier(event) || hasSecondaryModifier(event);

      if (isSelected && isMultiSelect) {
        if (add) {

          // Deselect element
          return selection.deselect(element);
        } else {

          // Select element only
          return selection.select(element);
        }
      } else if (!isSelected) {

        // Select element
        selection.select(element, add);
      } else {

        // Deselect element
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


  function isShown(element) {
    return !element.hidden;
  }

  /**
   * @type { import('didi').ModuleDeclaration }
   */
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
   * @typedef {import('didi').Injector} Injector
   */

  /**
   * A service that provides rules for certain diagram actions.
   *
   * The default implementation will hook into the {@link CommandStack}
   * to perform the actual rule evaluation. Make sure to provide the
   * `commandStack` service with this module if you plan to use it.
   *
   * Together with this implementation you may use the {@link import('./RuleProvider').default}
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
   * @param {string} action The action to be allowed or disallowed.
   * @param {Object} [context] The context for allowing or disallowing the action.
   *
   * @return {boolean|null} Wether the action is allowed. Returns `null` if the action
   * is to be ignored.
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

  /**
   * @type { import('didi').ModuleDeclaration }
   */
  var RulesModule = {
    __init__: [ 'rules' ],
    rules: [ 'type', Rules ]
  };

  /**
   * @typedef {import('didi').Injector} Injector
   *
   * @typedef {import('../../core/ElementRegistry').default} ElementRegistry
   * @typedef {import('../../core/EventBus').default} EventBus
   */

  var HIGH_PRIORITY$5 = 1500;


  /**
   * Browsers may swallow certain events (hover, out ...) if users are to
   * fast with the mouse.
   *
   * @see http://stackoverflow.com/questions/7448468/why-cant-i-reliably-capture-a-mouseout-event
   *
   * The fix implemented in this component ensure that we
   *
   * 1) have a hover state after a successful drag.move event
   * 2) have an out event when dragging leaves an element
   *
   * @param {ElementRegistry} elementRegistry
   * @param {EventBus} eventBus
   * @param {Injector} injector
   */
  function HoverFix(elementRegistry, eventBus, injector) {

    var self = this;

    var dragging = injector.get('dragging', false);

    /**
     * Make sure we are god damn hovering!
     *
     * @param {Event} dragging event
     */
    function ensureHover(event) {

      if (event.hover) {
        return;
      }

      var originalEvent = event.originalEvent;

      var gfx = self._findTargetGfx(originalEvent);

      var element = gfx && elementRegistry.get(gfx);

      if (gfx && element) {

        // 1) cancel current mousemove
        event.stopPropagation();

        // 2) emit fake hover for new target
        dragging.hover({ element: element, gfx: gfx });

        // 3) re-trigger move event
        dragging.move(originalEvent);
      }
    }


    if (dragging) {

      /**
       * We wait for a specific sequence of events before
       * emitting a fake drag.hover event.
       *
       * Event Sequence:
       *
       * drag.start
       * drag.move >> ensure we are hovering
       */
      eventBus.on('drag.start', function(event) {

        eventBus.once('drag.move', HIGH_PRIORITY$5, function(event) {

          ensureHover(event);

        });

      });
    }


    /**
     * We make sure that element.out is always fired, even if the
     * browser swallows an element.out event.
     *
     * Event sequence:
     *
     * element.hover
     * (element.out >> sometimes swallowed)
     * element.hover >> ensure we fired element.out
     */
    (function() {
      var hoverGfx;
      var hover;

      eventBus.on('element.hover', function(event) {

        // (1) remember current hover element
        hoverGfx = event.gfx;
        hover = event.element;
      });

      eventBus.on('element.hover', HIGH_PRIORITY$5, function(event) {

        // (3) am I on an element still?
        if (hover) {

          // (4) that is a problem, gotta "simulate the out"
          eventBus.fire('element.out', {
            element: hover,
            gfx: hoverGfx
          });
        }

      });

      eventBus.on('element.out', function() {

        // (2) unset hover state if we correctly outed us *GG*
        hoverGfx = null;
        hover = null;
      });

    })();

    this._findTargetGfx = function(event) {
      var position,
          target;

      if (!(event instanceof MouseEvent)) {
        return;
      }

      position = toPoint(event);

      // damn expensive operation, ouch!
      target = document.elementFromPoint(position.x, position.y);

      return getGfx(target);
    };

  }

  HoverFix.$inject = [
    'elementRegistry',
    'eventBus',
    'injector'
  ];


  // helpers /////////////////////

  function getGfx(target) {
    return closest(target, 'svg, .djs-element', true);
  }

  /**
   * @type { import('didi').ModuleDeclaration }
   */
  var HoverFixModule = {
    __init__: [
      'hoverFix'
    ],
    hoverFix: [ 'type', HoverFix ],
  };

  var CURSOR_CLS_PATTERN = /^djs-cursor-.*$/;

  /**
   * @param {string} mode
   */
  function set(mode) {
    var classes = classes$1(document.body);

    classes.removeMatching(CURSOR_CLS_PATTERN);

    if (mode) {
      classes.add('djs-cursor-' + mode);
    }
  }

  function unset() {
    set(null);
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
   * @typedef {import('../util/Types').Rect} Rect
   */

  /**
   * @param {Rect} bounds
   * @return {Point}
   */
  function center(bounds) {
    return {
      x: bounds.x + (bounds.width / 2),
      y: bounds.y + (bounds.height / 2)
    };
  }


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
   * Checks if key pressed is one of provided keys.
   *
   * @param {string|string[]} keys
   * @param {KeyboardEvent} event
   * @return {boolean}
   */
  function isKey(keys, event) {
    keys = isArray$3(keys) ? keys : [ keys ];

    return keys.indexOf(event.key) !== -1 || keys.indexOf(event.code) !== -1;
  }

  var round$8 = Math.round;

  /**
   * @typedef {import('../../util/Types').Point} Point
   *
   * @typedef {import('../../core/Canvas').default} Canvas
   * @typedef {import('../../core/ElementRegistry').default} ElementRegistry
   * @typedef {import('../../core/EventBus').default} EventBus
   * @typedef {import('../selection/Selection').default} Selection
   */

  var DRAG_ACTIVE_CLS = 'djs-drag-active';


  function preventDefault$1(event) {
    event.preventDefault();
  }

  function isTouchEvent(event) {

    // check for TouchEvent being available first
    // (i.e. not available on desktop Firefox)
    return typeof TouchEvent !== 'undefined' && event instanceof TouchEvent;
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
   *   * sets and restores the selection if elements still exist
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
   * ```javascript
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
   * ```
   *
   * @param {EventBus} eventBus
   * @param {Canvas} canvas
   * @param {Selection} selection
   * @param {ElementRegistry} elementRegistry
   */
  function Dragging(eventBus, canvas, selection, elementRegistry) {

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

      var event = eventBus.createEvent(
        assign$1(
          {},
          dragContext.payload,
          dragContext.data,
          { isTouch: dragContext.isTouch }
        )
      );

      // default integration
      if (eventBus.fire('drag.' + type, event) === false) {
        return false;
      }

      return eventBus.fire(dragContext.prefix + '.' + type, event);
    }

    function restoreSelection(previousSelection) {
      var existingSelection = previousSelection.filter(function(element) {
        return elementRegistry.get(element.id);
      });

      existingSelection.length && selection.select(existingSelection);
    }

    // event listeners

    function move(event, activate) {
      var payload = context.payload,
          displacement = context.displacement;

      var globalStart = context.globalStart,
          globalCurrent = toPoint(event),
          globalDelta = delta(globalCurrent, globalStart);

      var localStart = context.localStart,
          localCurrent = toLocalPoint(globalCurrent),
          localDelta = delta(localCurrent, localStart);


      // activate context explicitly or once threshold is reached
      if (!context.active && (activate || getLength(globalDelta) > context.threshold)) {

        // fire start event with original
        // starting coordinates

        assign$1(payload, {
          x: round$8(localStart.x + displacement.x),
          y: round$8(localStart.y + displacement.y),
          dx: 0,
          dy: 0
        }, { originalEvent: event });

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
          set(context.cursor);
        }

        // indicate dragging via marker on root element
        canvas.addMarker(canvas.getRootElement(), DRAG_ACTIVE_CLS);
      }

      stopPropagation$1(event);

      if (context.active) {

        // update payload with actual coordinates
        assign$1(payload, {
          x: round$8(localCurrent.x + displacement.x),
          y: round$8(localCurrent.y + displacement.y),
          dx: round$8(localDelta.x),
          dy: round$8(localDelta.y)
        }, { originalEvent: event });

        // emit move event
        fire('move');
      }
    }

    function end(event) {
      var previousContext,
          returnValue = true;

      if (context.active) {

        if (event) {
          context.payload.originalEvent = event;

          // suppress original event (click, ...)
          // because we just ended a drag operation
          stopPropagation$1(event);
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

    function checkCancel(event) {

      if (isKey('Escape', event)) {
        preventDefault$1(event);

        cancel();
      }
    }


    // prevent ghost click that might occur after a finished
    // drag and drop session

    function trapClickAndEnd(event) {

      var untrap;

      // trap the click in case we are part of an active
      // drag operation. This will effectively prevent
      // the ghost click that cannot be canceled otherwise.
      if (context.active) {

        untrap = install(eventBus);

        // remove trap after minimal delay
        setTimeout(untrap, 400);

        // prevent default action (click)
        preventDefault$1(event);
      }

      end(event);
    }

    function trapTouch(event) {
      move(event);
    }

    // update the drag events model element (`hover`) and graphical element (`hoverGfx`)
    // properties during hover and out and fire {prefix}.hover and {prefix}.out properties
    // respectively

    function hover(event) {
      var payload = context.payload;

      payload.hoverGfx = event.gfx;
      payload.hover = event.element;

      fire('hover');
    }

    function out(event) {
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
      event.unbind(document, 'mousemove', move);

      event.unbind(document, 'dragstart', preventDefault$1);
      event.unbind(document, 'selectstart', preventDefault$1);

      event.unbind(document, 'mousedown', endDrag, true);
      event.unbind(document, 'mouseup', endDrag, true);

      event.unbind(document, 'keyup', checkCancel);

      event.unbind(document, 'touchstart', trapTouch, true);
      event.unbind(document, 'touchcancel', cancel, true);
      event.unbind(document, 'touchmove', move, true);
      event.unbind(document, 'touchend', end, true);

      eventBus.off('element.hover', hover);
      eventBus.off('element.out', out);

      // remove drag marker on root element
      canvas.removeMarker(canvas.getRootElement(), DRAG_ACTIVE_CLS);

      // restore selection, unless it has changed
      var previousSelection = context.payload.previousSelection;

      if (restore !== false && previousSelection && !selection.get().length) {
        restoreSelection(previousSelection);
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
     * @param {Point} [relativeTo] actual diagram local position this drag operation should start at
     * @param {string} prefix
     * @param {Object} [options]
     */
    function init(event$1, relativeTo, prefix, options) {

      // only one drag operation may be active, at a time
      if (context) {
        cancel(false);
      }

      if (typeof relativeTo === 'string') {
        options = prefix;
        prefix = relativeTo;
        relativeTo = null;
      }

      options = assign$1({}, defaultOptions, options || {});

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

      if (event$1) {
        originalEvent = getOriginal$1(event$1) || event$1;
        globalStart = toPoint(event$1);

        stopPropagation$1(event$1);

        // prevent default browser dragging behavior
        if (originalEvent.type === 'dragstart') {
          preventDefault$1(originalEvent);
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

      context = assign$1({
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
          event.bind(document, 'touchstart', trapTouch, true);
          event.bind(document, 'touchcancel', cancel, true);
          event.bind(document, 'touchmove', move, true);
          event.bind(document, 'touchend', end, true);
        } else {

          // assume we use the mouse to interact per default
          event.bind(document, 'mousemove', move);

          // prevent default browser drag and text selection behavior
          event.bind(document, 'dragstart', preventDefault$1);
          event.bind(document, 'selectstart', preventDefault$1);

          event.bind(document, 'mousedown', endDrag, true);
          event.bind(document, 'mouseup', endDrag, true);
        }

        event.bind(document, 'keyup', checkCancel);

        eventBus.on('element.hover', hover);
        eventBus.on('element.out', out);
      }

      fire('init');

      if (options.autoActivate) {
        move(event$1, true);
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
      assign$1(defaultOptions, options);
    };
  }

  Dragging.$inject = [
    'eventBus',
    'canvas',
    'selection',
    'elementRegistry'
  ];

  /**
   * @type { import('didi').ModuleDeclaration }
   */
  var DraggingModule = {
    __depends__: [
      HoverFixModule,
      SelectionModule,
    ],
    dragging: [ 'type', Dragging ],
  };

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
   * @typedef {import('../../core/Types').ElementLike} Element
   * @typedef {import('../../core/Types').ShapeLike} Shape
   *
   * @typedef {import('../../core/Canvas').default} Canvas
   * @typedef {import('../../core/ElementRegistry').default} ElementRegistry
   * @typedef {import('../../core/EventBus').default} EventBus
   * @typedef {import('../../draw/Styles').default} Styles
   */

  var MARKER_TYPES = [
    'marker-start',
    'marker-mid',
    'marker-end'
  ];

  var NODES_CAN_HAVE_MARKER = [
    'circle',
    'ellipse',
    'line',
    'path',
    'polygon',
    'polyline',
    'path',
    'rect'
  ];


  /**
   * Adds support for previews of moving/resizing elements.
   *
   * @param {ElementRegistry} elementRegistry
   * @param {EventBus} eventBus
   * @param {Canvas} canvas
   * @param {Styles} styles
   */
  function PreviewSupport(elementRegistry, eventBus, canvas, styles) {
    this._elementRegistry = elementRegistry;
    this._canvas = canvas;
    this._styles = styles;

    this._clonedMarkers = {};

    var self = this;

    eventBus.on('drag.cleanup', function() {
      forEach$1(self._clonedMarkers, function(clonedMarker) {
        remove$1(clonedMarker);
      });

      self._clonedMarkers = {};
    });
  }

  PreviewSupport.$inject = [
    'elementRegistry',
    'eventBus',
    'canvas',
    'styles'
  ];


  /**
   * Returns graphics of an element.
   *
   * @param {Element} element
   *
   * @return {SVGElement}
   */
  PreviewSupport.prototype.getGfx = function(element) {
    return this._elementRegistry.getGraphics(element);
  };

  /**
   * Adds a move preview of a given shape to a given SVG group.
   *
   * @param {Element} element The element to be moved.
   * @param {SVGElement} group The SVG group to add the preview to.
   * @param {SVGElement} [gfx] The optional graphical element of the element.
   *
   * @return {SVGElement} The preview.
   */
  PreviewSupport.prototype.addDragger = function(element, group, gfx) {
    gfx = gfx || this.getGfx(element);

    var dragger = clone$1(gfx);
    var bbox = gfx.getBoundingClientRect();

    this._cloneMarkers(getVisual(dragger));

    attr(dragger, this._styles.cls('djs-dragger', [], {
      x: bbox.top,
      y: bbox.left
    }));

    append(group, dragger);

    return dragger;
  };

  /**
   * Adds a resize preview of a given shape to a given SVG group.
   *
   * @param {Shape} shape The element to be resized.
   * @param {SVGElement} group The SVG group to add the preview to.
   *
   * @return {SVGElement} The preview.
   */
  PreviewSupport.prototype.addFrame = function(shape, group) {

    var frame = create$1('rect', {
      class: 'djs-resize-overlay',
      width:  shape.width,
      height: shape.height,
      x: shape.x,
      y: shape.y
    });

    append(group, frame);

    return frame;
  };

  /**
   * Clone all markers referenced by a node and its child nodes.
   *
   * @param {SVGElement} gfx
   */
  PreviewSupport.prototype._cloneMarkers = function(gfx) {
    var self = this;

    if (gfx.childNodes) {

      // TODO: use forEach once we drop PhantomJS
      for (var i = 0; i < gfx.childNodes.length; i++) {

        // recursively clone markers of child nodes
        self._cloneMarkers(gfx.childNodes[ i ]);
      }
    }

    if (!canHaveMarker(gfx)) {
      return;
    }

    MARKER_TYPES.forEach(function(markerType) {
      if (attr(gfx, markerType)) {
        var marker = getMarker(gfx, markerType, self._canvas.getContainer());

        self._cloneMarker(gfx, marker, markerType);
      }
    });
  };

  /**
   * Clone marker referenced by an element.
   *
   * @param {SVGElement} gfx
   * @param {SVGElement} marker
   * @param {string} markerType
   */
  PreviewSupport.prototype._cloneMarker = function(gfx, marker, markerType) {
    var markerId = marker.id;

    var clonedMarker = this._clonedMarkers[ markerId ];

    if (!clonedMarker) {
      clonedMarker = clone$1(marker);

      var clonedMarkerId = markerId + '-clone';

      clonedMarker.id = clonedMarkerId;

      classes(clonedMarker)
        .add('djs-dragger')
        .add('djs-dragger-marker');

      this._clonedMarkers[ markerId ] = clonedMarker;

      var defs = query('defs', this._canvas._svg);

      if (!defs) {
        defs = create$1('defs');

        append(this._canvas._svg, defs);
      }

      append(defs, clonedMarker);
    }

    var reference = idToReference(this._clonedMarkers[ markerId ].id);

    attr(gfx, markerType, reference);
  };

  // helpers //////////

  /**
   * Get marker of given type referenced by node.
   *
   * @param {HTMLElement} node
   * @param {string} markerType
   * @param {HTMLElement} [parentNode]
   *
   * @param {HTMLElement}
   */
  function getMarker(node, markerType, parentNode) {
    var id = referenceToId(attr(node, markerType));

    return query('marker#' + id, parentNode || document);
  }

  /**
   * Get ID of fragment within current document from its functional IRI reference.
   * References may use single or double quotes.
   *
   * @param {string} reference
   *
   * @return {string}
   */
  function referenceToId(reference) {
    return reference.match(/url\(['"]?#([^'"]*)['"]?\)/)[1];
  }

  /**
   * Get functional IRI reference for given ID of fragment within current document.
   *
   * @param {string} id
   *
   * @return {string}
   */
  function idToReference(id) {
    return 'url(#' + id + ')';
  }

  /**
   * Check wether node type can have marker attributes.
   *
   * @param {HTMLElement} node
   *
   * @return {boolean}
   */
  function canHaveMarker(node) {
    return NODES_CAN_HAVE_MARKER.indexOf(node.nodeName) !== -1;
  }

  /**
   * @type { import('didi').ModuleDeclaration }
   */
  var PreviewSupportModule = {
    __init__: [ 'previewSupport' ],
    previewSupport: [ 'type', PreviewSupport ]
  };

  /**
   * @typedef {import('../../core/Types').ElementLike} Element
   * @typedef {import('../../core/Types').ShapeLike} Shape
   *
   * @typedef {import('../dragging/Dragging').default} Dragging
   * @typedef {import('../../core/EventBus').default} EventBus
   * @typedef {import('../modeling/Modeling').default} Modeling
   * @typedef {import('../rules/Rules').default} Rules
   * @typedef {import('../selection/Selection').default} Selection
   */

  var LOW_PRIORITY$6 = 500,
      MEDIUM_PRIORITY = 1250,
      HIGH_PRIORITY$4 = 1500;

  var round$7 = Math.round;

  function mid(element) {
    return {
      x: element.x + round$7(element.width / 2),
      y: element.y + round$7(element.height / 2)
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
    eventBus.on('shape.move.start', HIGH_PRIORITY$4, function(event) {

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
      assign$1(context, {
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
    eventBus.on('shape.move.move', LOW_PRIORITY$6, function(event) {

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

      if (canExecute === false) {
        return false;
      }

      // ensure we have actual pixel values deltas
      // (important when zoom level was > 1 during move)
      delta.x = round$7(delta.x);
      delta.y = round$7(delta.y);

      if (delta.x === 0 && delta.y === 0) {

        // didn't move
        return;
      }

      modeling.moveElements(shapes, delta, context.target, {
        primaryShape: context.shape,
        attach: isAttach
      });
    });


    // move activation

    eventBus.on('element.mousedown', function(event) {

      if (!isPrimaryButton(event)) {
        return;
      }

      var originalEvent = getOriginal$1(event);

      if (!originalEvent) {
        throw new Error('must supply DOM mousedown event');
      }

      return start(originalEvent, event.element);
    });

    /**
     * Start move.
     *
     * @param {MouseEvent|TouchEvent} event
     * @param {Shape} element
     * @param {boolean} [activate]
     * @param {Object} [context]
     */
    function start(event, element, activate, context) {
      if (isObject(activate)) {
        context = activate;
        activate = false;
      }

      // do not move connections or the root element
      if (element.waypoints || !element.parent) {
        return;
      }

      // ignore non-draggable hits
      if (classes(event.target).has('djs-hit-no-move')) {
        return;
      }

      var referencePoint = mid(element);

      dragging.init(event, referencePoint, 'shape.move', {
        cursor: 'grabbing',
        autoActivate: activate,
        data: {
          shape: element,
          context: context || {}
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
   * @param {Element[]} elements
   *
   * @return {Element[]} filtered
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
   * @param {SVGElement} gfx
   * @param {number} x
   * @param {number} y
   * @param {number} [angle]
   * @param {number} [amount]
   */
  function transform(gfx, x, y, angle, amount) {
    var translate = createTransform();
    translate.setTranslate(x, y);

    var rotate = createTransform();
    rotate.setRotate(angle || 0, 0, 0);

    var scale = createTransform();
    scale.setScale(amount || 1, amount || 1);

    transform$1(gfx, [ translate, rotate, scale ]);
  }


  /**
   * @param {SVGElement} gfx
   * @param {number} x
   * @param {number} y
   */
  function translate(gfx, x, y) {
    var translate = createTransform();
    translate.setTranslate(x, y);

    transform$1(gfx, translate);
  }


  /**
   * @param {SVGElement} gfx
   * @param {number} angle
   */
  function rotate(gfx, angle) {
    var rotate = createTransform();
    rotate.setRotate(angle, 0, 0);

    transform$1(gfx, rotate);
  }

  /**
   * Checks whether a value is an instance of Connection.
   *
   * @param {any} value
   *
   * @return {boolean}
   */
  function isConnection(value) {
    return isObject(value) && has$1(value, 'waypoints');
  }

  /**
   * Checks whether a value is an instance of Label.
   *
   * @param {any} value
   *
   * @return {boolean}
   */
  function isLabel(value) {
    return isObject(value) && has$1(value, 'labelTarget');
  }

  /**
   * Checks whether a value is an instance of Root.
   *
   * @param {any} value
   *
   * @return {boolean}
   */
  function isRoot(value) {
    return isObject(value) && isNil(value.parent);
  }

  /**
   * @typedef {import('../../model/Types').Element} Element
   *
   * @typedef {import('../../core/Canvas').default} Canvas
   * @typedef {import('../../core/EventBus').default} EventBus
   * @typedef {import('../preview-support/PreviewSupport').default} PreviewSupport
   * @typedef {import('../../draw/Styles').default} Styles
   */

  var LOW_PRIORITY$5 = 499;

  var MARKER_DRAGGING = 'djs-dragging',
      MARKER_OK$4 = 'drop-ok',
      MARKER_NOT_OK$4 = 'drop-not-ok',
      MARKER_NEW_PARENT$2 = 'new-parent',
      MARKER_ATTACH$2 = 'attach-ok';


  /**
   * Provides previews for moving shapes when moving.
   *
   * @param {EventBus} eventBus
   * @param {Canvas} canvas
   * @param {Styles} styles
   * @param {PreviewSupport} previewSupport
   */
  function MovePreview(
      eventBus, canvas, styles, previewSupport) {

    function getVisualDragShapes(shapes) {
      var elements = getAllDraggedElements(shapes);

      var filteredElements = removeEdges(elements);

      return filteredElements;
    }

    function getAllDraggedElements(shapes) {
      var allShapes = selfAndAllChildren(shapes, true);

      var allConnections = map$1(allShapes, function(shape) {
        return (shape.incoming || []).concat(shape.outgoing || []);
      });

      return flatten(allShapes.concat(allConnections));
    }

    /**
     * Sets drop marker on an element.
     */
    function setMarker(element, marker) {

      [ MARKER_ATTACH$2, MARKER_OK$4, MARKER_NOT_OK$4, MARKER_NEW_PARENT$2 ].forEach(function(m) {

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
     * @param {Element} element
     * @param {boolean} addMarker
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
    eventBus.on('shape.move.start', LOW_PRIORITY$5, function(event) {
      var context = event.context,
          dragShapes = context.shapes,
          allDraggedElements = context.allDraggedElements;

      var visuallyDraggedShapes = getVisualDragShapes(dragShapes);

      if (!context.dragGroup) {
        var dragGroup = create$1('g');

        attr(dragGroup, styles.cls('djs-drag-group', [ 'no-events' ]));

        var activeLayer = canvas.getActiveLayer();

        append(activeLayer, dragGroup);

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
      forEach$1(allDraggedElements, function(e) {
        canvas.addMarker(e, MARKER_DRAGGING);
      });

      context.allDraggedElements = allDraggedElements;

      // determine, if any of the dragged elements have different parents
      context.differentParents = haveDifferentParents(dragShapes);
    });

    // update previews
    eventBus.on('shape.move.move', LOW_PRIORITY$5, function(event) {

      var context = event.context,
          dragGroup = context.dragGroup,
          target = context.target,
          parent = context.shape.parent,
          canExecute = context.canExecute;

      if (target) {
        if (canExecute === 'attach') {
          setMarker(target, MARKER_ATTACH$2);
        } else if (context.canExecute && target && target.id !== parent.id) {
          setMarker(target, MARKER_NEW_PARENT$2);
        } else {
          setMarker(target, context.canExecute ? MARKER_OK$4 : MARKER_NOT_OK$4);
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
      forEach$1(allDraggedElements, function(e) {
        canvas.removeMarker(e, MARKER_DRAGGING);
      });

      if (dragGroup) {
        remove$1(dragGroup);
      }
    });


    // API //////////////////////

    /**
     * Make an element draggable.
     *
     * @param {Object} context
     * @param {Element} element
     * @param {boolean} addMarker
     */
    this.makeDraggable = makeDraggable;
  }

  MovePreview.$inject = [
    'eventBus',
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

      if (!isConnection(element)) {
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
   * @type { import('didi').ModuleDeclaration }
   */
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

  /**
   * @typedef {import('../dragging/Dragging').default} Dragging
   * @typedef {import('../../core/EventBus').default} EventBus
   *
   * @typedef {import('../../core/EventBus').Event} Event
   */

  var LOW_PRIORITY$4 = 250;

  /**
   * The tool manager acts as middle-man between the available tool's and the Palette,
   * it takes care of making sure that the correct active state is set.
   *
   * @param {EventBus} eventBus
   * @param {Dragging} dragging
   */
  function ToolManager(eventBus, dragging) {
    this._eventBus = eventBus;
    this._dragging = dragging;

    this._tools = [];
    this._active = null;
  }

  ToolManager.$inject = [ 'eventBus', 'dragging' ];

  /**
   * Register a tool.
   *
   * @param {string} name
   * @param { {
   *   dragging: string;
   *   tool: string;
   * } } events
   */
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

    // TODO: add test cases
    forEach$1(events, function(event) {
      eventsToRegister.push(event + '.ended');
      eventsToRegister.push(event + '.canceled');
    });

    eventBus.on(eventsToRegister, LOW_PRIORITY$4, function(event) {

      // We defer the de-activation of the tool to the .activate phase,
      // so we're able to check if we want to toggle off the current
      // active tool or switch to a new one
      if (!this._active) {
        return;
      }

      if (isPaletteClick(event)) {
        return;
      }

      this.setActive(null);
    }, this);

  };


  // helpers ///////////////

  /**
   * Check if a given event is a palette click event.
   *
   * @param {Event} event
   *
   * @return {boolean}
   */
  function isPaletteClick(event) {
    var target = event.originalEvent && event.originalEvent.target;

    return target && closest(target, '.group[data-group="tools"]');
  }

  /**
   * @type { import('didi').ModuleDeclaration }
   */
  var ToolManagerModule = {
    __depends__: [
      DraggingModule
    ],
    __init__: [ 'toolManager' ],
    toolManager: [ 'type', ToolManager ]
  };

  /**
   * @typedef {import('../../core/EventBus').default} EventBus
   */

  /**
   * @param {EventBus} eventBus
   */
  function Mouse(eventBus) {
    var self = this;

    this._lastMoveEvent = null;

    function setLastMoveEvent(mousemoveEvent) {
      self._lastMoveEvent = mousemoveEvent;
    }

    eventBus.on('canvas.init', function(context) {
      var svg = self._svg = context.svg;

      svg.addEventListener('mousemove', setLastMoveEvent);
    });

    eventBus.on('canvas.destroy', function() {
      self._lastMouseEvent = null;

      self._svg.removeEventListener('mousemove', setLastMoveEvent);
    });
  }

  Mouse.$inject = [ 'eventBus' ];

  Mouse.prototype.getLastMoveEvent = function() {
    return this._lastMoveEvent || createMoveEvent(0, 0);
  };

  // helpers //////////

  function createMoveEvent(x, y) {
    var event = document.createEvent('MouseEvent');

    var screenX = x,
        screenY = y,
        clientX = x,
        clientY = y;

    if (event.initMouseEvent) {
      event.initMouseEvent(
        'mousemove',
        true,
        true,
        window,
        0,
        screenX,
        screenY,
        clientX,
        clientY,
        false,
        false,
        false,
        false,
        0,
        null
      );
    }

    return event;
  }

  /**
   * @type { import('didi').ModuleDeclaration }
   */
  var MouseModule = {
    __init__: [ 'mouse' ],
    mouse: [ 'type', Mouse ]
  };

  /**
   * @typedef {import('../../core/Canvas').default} Canvas
   * @typedef {import('../dragging/Dragging').default} Dragging
   * @typedef {import('../../core/ElementRegistry').default} ElementRegistry
   * @typedef {import('../../core/EventBus').default} EventBus
   * @typedef {import('../mouse/Mouse').default} Mouse
   * @typedef {import('../selection/Selection').default} Selection
   * @typedef {import('../tool-manager/ToolManager').default} ToolManager
   */

  var LASSO_TOOL_CURSOR = 'crosshair';

  /**
   * @param {EventBus} eventBus
   * @param {Canvas} canvas
   * @param {Dragging} dragging
   * @param {ElementRegistry} elementRegistry
   * @param {Selection} selection
   * @param {ToolManager} toolManager
   * @param {Mouse} mouse
   */
  function LassoTool(
      eventBus, canvas, dragging,
      elementRegistry, selection, toolManager,
      mouse) {

    this._selection = selection;
    this._dragging = dragging;
    this._mouse = mouse;

    var self = this;

    // lasso visuals implementation

    /**
    * A helper that realizes the selection box visual
    */
    var visuals = {

      create: function(context) {
        var container = canvas.getActiveLayer(),
            frame;

        frame = context.frame = create$1('rect');
        attr(frame, {
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

        attr(frame, {
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

      if (!hasSecondaryModifier(event)) {
        return;
      }

      self.activateLasso(event.originalEvent);

      // we've handled the event
      return true;
    });
  }

  LassoTool.$inject = [
    'eventBus',
    'canvas',
    'dragging',
    'elementRegistry',
    'selection',
    'toolManager',
    'mouse'
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

  LassoTool.prototype.activateSelection = function(event, autoActivate) {

    this._dragging.init(event, 'lasso.selection', {
      trapClick: false,
      autoActivate: autoActivate,
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
      return this._dragging.cancel();
    }

    var mouseEvent = this._mouse.getLastMoveEvent();

    this.activateSelection(mouseEvent, !!mouseEvent);
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

  /**
   * @type { import('didi').ModuleDeclaration }
   */
  var LassoToolModule = {
    __depends__: [
      ToolManagerModule,
      MouseModule
    ],
    __init__: [ 'lassoTool' ],
    lassoTool: [ 'type', LassoTool ]
  };

  /**
   * @param {string} str
   *
   * @return {string}
   */
  function escapeCSS(str) {
    return CSS.escape(str);
  }

  /**
   * @typedef {import('../../core/Canvas').default} Canvas
   * @typedef {import('../../core/EventBus').default} EventBus
   *
   * @typedef {import('./PaletteProvider').PaletteEntries} PaletteEntries
   * @typedef {import('./PaletteProvider').default} PaletteProvider
   */

  var TOGGLE_SELECTOR = '.djs-palette-toggle',
      ENTRY_SELECTOR = '.entry',
      ELEMENT_SELECTOR = TOGGLE_SELECTOR + ', ' + ENTRY_SELECTOR;

  var PALETTE_PREFIX = 'djs-palette-',
      PALETTE_SHOWN_CLS = 'shown',
      PALETTE_OPEN_CLS = 'open',
      PALETTE_TWO_COLUMN_CLS = 'two-column';

  var DEFAULT_PRIORITY$3 = 1000;


  /**
   * A palette containing modeling elements.
   *
   * @param {EventBus} eventBus
   * @param {Canvas} canvas
   */
  function Palette(eventBus, canvas) {

    this._eventBus = eventBus;
    this._canvas = canvas;

    var self = this;

    eventBus.on('tool-manager.update', function(event) {
      var tool = event.tool;

      self.updateToolHighlight(tool);
    });

    eventBus.on('i18n.changed', function() {
      self._update();
    });

    eventBus.on('diagram.init', function() {

      self._diagramInitialized = true;

      self._rebuild();
    });
  }

  Palette.$inject = [ 'eventBus', 'canvas' ];

  /**
   * @overlord
   *
   * Register a palette provider with default priority. See
   * {@link PaletteProvider} for examples.
   *
   * @param {PaletteProvider} provider
   */

  /**
   * Register a palette provider with the given priority. See
   * {@link PaletteProvider} for examples.
   *
   * @param {number} priority
   * @param {PaletteProvider} provider
   */
  Palette.prototype.registerProvider = function(priority, provider) {
    if (!provider) {
      provider = priority;
      priority = DEFAULT_PRIORITY$3;
    }

    this._eventBus.on('palette.getProviders', priority, function(event) {
      event.providers.push(provider);
    });

    this._rebuild();
  };


  /**
   * Returns the palette entries.
   *
   * @return {PaletteEntries}
   */
  Palette.prototype.getEntries = function() {
    var providers = this._getProviders();

    return providers.reduce(addPaletteEntries, {});
  };

  Palette.prototype._rebuild = function() {

    if (!this._diagramInitialized) {
      return;
    }

    var providers = this._getProviders();

    if (!providers.length) {
      return;
    }

    if (!this._container) {
      this._init();
    }

    this._update();
  };

  /**
   * Initialize palette.
   */
  Palette.prototype._init = function() {

    var self = this;

    var eventBus = this._eventBus;

    var parentContainer = this._getParentContainer();

    var container = this._container = domify$1(Palette.HTML_MARKUP);

    parentContainer.appendChild(container);
    classes$1(parentContainer).add(PALETTE_PREFIX + PALETTE_SHOWN_CLS);

    delegate.bind(container, ELEMENT_SELECTOR, 'click', function(event) {

      var target = event.delegateTarget;

      if (matches(target, TOGGLE_SELECTOR)) {
        return self.toggle();
      }

      self.trigger('click', event);
    });

    // prevent drag propagation
    event.bind(container, 'mousedown', function(event) {
      event.stopPropagation();
    });

    // prevent drag propagation
    delegate.bind(container, ENTRY_SELECTOR, 'dragstart', function(event) {
      self.trigger('dragstart', event);
    });

    eventBus.on('canvas.resized', this._layoutChanged, this);

    eventBus.fire('palette.create', {
      container: container
    });
  };

  Palette.prototype._getProviders = function(id) {

    var event = this._eventBus.createEvent({
      type: 'palette.getProviders',
      providers: []
    });

    this._eventBus.fire(event);

    return event.providers;
  };

  /**
   * Update palette state.
   *
   * @param { {
   *   open?: boolean;
   *   twoColumn?: boolean;
   * } } [state]
   */
  Palette.prototype._toggleState = function(state) {

    state = state || {};

    var parent = this._getParentContainer(),
        container = this._container;

    var eventBus = this._eventBus;

    var twoColumn;

    var cls = classes$1(container),
        parentCls = classes$1(parent);

    if ('twoColumn' in state) {
      twoColumn = state.twoColumn;
    } else {
      twoColumn = this._needsCollapse(parent.clientHeight, this._entries || {});
    }

    // always update two column
    cls.toggle(PALETTE_TWO_COLUMN_CLS, twoColumn);
    parentCls.toggle(PALETTE_PREFIX + PALETTE_TWO_COLUMN_CLS, twoColumn);

    if ('open' in state) {
      cls.toggle(PALETTE_OPEN_CLS, state.open);
      parentCls.toggle(PALETTE_PREFIX + PALETTE_OPEN_CLS, state.open);
    }

    eventBus.fire('palette.changed', {
      twoColumn: twoColumn,
      open: this.isOpen()
    });
  };

  Palette.prototype._update = function() {

    var entriesContainer = query('.djs-palette-entries', this._container),
        entries = this._entries = this.getEntries();

    clear$1(entriesContainer);

    forEach$1(entries, function(entry, id) {

      var grouping = entry.group || 'default';

      var container = query('[data-group=' + escapeCSS(grouping) + ']', entriesContainer);
      if (!container) {
        container = domify$1('<div class="group"></div>');
        attr$1(container, 'data-group', grouping);

        entriesContainer.appendChild(container);
      }

      var html = entry.html || (
        entry.separator ?
          '<hr class="separator" />' :
          '<div class="entry" draggable="true"></div>');


      var control = domify$1(html);
      container.appendChild(control);

      if (!entry.separator) {
        attr$1(control, 'data-action', id);

        if (entry.title) {
          attr$1(control, 'title', entry.title);
        }

        if (entry.className) {
          addClasses$1(control, entry.className);
        }

        if (entry.imageUrl) {
          var image = domify$1('<img>');
          attr$1(image, 'src', entry.imageUrl);

          control.appendChild(image);
        }
      }
    });

    // open after update
    this.open();
  };


  /**
   * Trigger an action available on the palette
   *
   * @param {string} action
   * @param {Event} event
   * @param {boolean} [autoActivate=false]
   */
  Palette.prototype.trigger = function(action, event, autoActivate) {
    var entry,
        originalEvent,
        button = event.delegateTarget || event.target;

    if (!button) {
      return event.preventDefault();
    }

    entry = attr$1(button, 'data-action');
    originalEvent = event.originalEvent || event;

    return this.triggerEntry(entry, action, originalEvent, autoActivate);
  };

  /**
   * @param {string} entryId
   * @param {string} action
   * @param {Event} event
   * @param {boolean} [autoActivate=false]
   */
  Palette.prototype.triggerEntry = function(entryId, action, event, autoActivate) {
    var entries = this._entries,
        entry,
        handler;

    entry = entries[entryId];

    // when user clicks on the palette and not on an action
    if (!entry) {
      return;
    }

    handler = entry.action;

    if (this._eventBus.fire('palette.trigger', { entry, event }) === false) {
      return;
    }

    // simple action (via callback function)
    if (isFunction(handler)) {
      if (action === 'click') {
        return handler(event, autoActivate);
      }
    } else {
      if (handler[action]) {
        return handler[action](event, autoActivate);
      }
    }

    // silence other actions
    event.preventDefault();
  };

  Palette.prototype._layoutChanged = function() {
    this._toggleState({});
  };

  /**
   * Do we need to collapse to two columns?
   *
   * @param {number} availableHeight
   * @param {PaletteEntries} entries
   *
   * @return {boolean}
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
   * Close the palette.
   */
  Palette.prototype.close = function() {
    this._toggleState({
      open: false,
      twoColumn: false
    });
  };

  /**
   * Open the palette.
   */
  Palette.prototype.open = function() {
    this._toggleState({ open: true });
  };

  /**
   * Toggle the palette.
   */
  Palette.prototype.toggle = function() {
    if (this.isOpen()) {
      this.close();
    } else {
      this.open();
    }
  };

  /**
   * @param {string} tool
   *
   * @return {boolean}
   */
  Palette.prototype.isActiveTool = function(tool) {
    return tool && this._activeTool === tool;
  };

  /**
   * @param {string} name
   */
  Palette.prototype.updateToolHighlight = function(name) {
    var entriesContainer,
        toolsContainer;

    if (!this._toolsContainer) {
      entriesContainer = query('.djs-palette-entries', this._container);

      this._toolsContainer = query('[data-group=tools]', entriesContainer);
    }

    toolsContainer = this._toolsContainer;

    forEach$1(toolsContainer.children, function(tool) {
      var actionName = tool.getAttribute('data-action');

      if (!actionName) {
        return;
      }

      var toolClasses = classes$1(tool);

      actionName = actionName.replace('-tool', '');

      if (toolClasses.contains('entry') && actionName === name) {
        toolClasses.add('highlighted-entry');
      } else {
        toolClasses.remove('highlighted-entry');
      }
    });
  };


  /**
   * Return `true` if the palette is opened.
   *
   * @example
   *
   * ```javascript
   * palette.open();
   *
   * if (palette.isOpen()) {
   *   // yes, we are open
   * }
   * ```
   *
   * @return {boolean}
   */
  Palette.prototype.isOpen = function() {
    return classes$1(this._container).has(PALETTE_OPEN_CLS);
  };

  /**
   * Get parent element of palette.
   *
   * @return {HTMLElement}
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

  function addClasses$1(element, classNames) {

    var classes = classes$1(element);

    var actualClassNames = isArray$3(classNames) ? classNames : classNames.split(/\s+/g);
    actualClassNames.forEach(function(cls) {
      classes.add(cls);
    });
  }

  function addPaletteEntries(entries, provider) {

    var entriesOrUpdater = provider.getPaletteEntries();

    if (isFunction(entriesOrUpdater)) {
      return entriesOrUpdater(entries);
    }

    forEach$1(entriesOrUpdater, function(entry, id) {
      entries[id] = entry;
    });

    return entries;
  }

  /**
   * @type { import('didi').ModuleDeclaration }
   */
  var PaletteModule = {
    __init__: [ 'palette' ],
    palette: [ 'type', Palette ]
  };

  var MARKER_OK$3 = 'drop-ok',
      MARKER_NOT_OK$3 = 'drop-not-ok',
      MARKER_ATTACH$1 = 'attach-ok',
      MARKER_NEW_PARENT$1 = 'new-parent';

  /**
   * @typedef {import('../../core/Types').ElementLike} Element
   * @typedef {import('../../core/Types').ShapeLike} Shape
   *
   * @typedef {import('../../util/Types').Point} Point
   *
   * @typedef {import('../../core/Canvas').default} Canvas
   * @typedef {import('../dragging/Dragging').default} Dragging
   * @typedef {import('../../core/EventBus').default} EventBus
   * @typedef {import('../modeling/Modeling').default} Modeling
   * @typedef {import('../rules/Rules').default} Rules
   */

  var PREFIX$1 = 'create';

  var HIGH_PRIORITY$3 = 2000;


  /**
   * Create new elements through drag and drop.
   *
   * @param {Canvas} canvas
   * @param {Dragging} dragging
   * @param {EventBus} eventBus
   * @param {Modeling} modeling
   * @param {Rules} rules
   */
  function Create$1(
      canvas,
      dragging,
      eventBus,
      modeling,
      rules
  ) {

    // rules //////////

    /**
     * Check wether elements can be created.
     *
     * @param {Element[]} elements
     * @param {Shape} target
     * @param {Point} position
     * @param {Element} [source]
     *
     * @return {boolean|null|Object}
     */
    function canCreate(elements, target, position, source, hints) {
      if (!target) {
        return false;
      }

      // ignore child elements and external labels
      elements = filter(elements, function(element) {
        var labelTarget = element.labelTarget;

        return !element.parent && !(isLabel(element) && elements.indexOf(labelTarget) !== -1);
      });

      var shape = find(elements, function(element) {
        return !isConnection(element);
      });

      var attach = false,
          connect = false,
          create = false;

      // (1) attaching single shapes
      if (isSingleShape(elements)) {
        attach = rules.allowed('shape.attach', {
          position: position,
          shape: shape,
          target: target
        });
      }

      if (!attach) {

        // (2) creating elements
        if (isSingleShape(elements)) {
          create = rules.allowed('shape.create', {
            position: position,
            shape: shape,
            source: source,
            target: target
          });
        } else {
          create = rules.allowed('elements.create', {
            elements: elements,
            position: position,
            target: target
          });
        }

      }

      var connectionTarget = hints.connectionTarget;

      // (3) appending single shapes
      if (create || attach) {
        if (shape && source) {
          connect = rules.allowed('connection.create', {
            source: connectionTarget === source ? shape : source,
            target: connectionTarget === source ? source : shape,
            hints: {
              targetParent: target,
              targetAttach: attach
            }
          });
        }

        return {
          attach: attach,
          connect: connect
        };
      }

      // ignore wether or not elements can be created
      if (create === null || attach === null) {
        return null;
      }

      return false;
    }

    function setMarker(element, marker) {
      [ MARKER_ATTACH$1, MARKER_OK$3, MARKER_NOT_OK$3, MARKER_NEW_PARENT$1 ].forEach(function(m) {

        if (m === marker) {
          canvas.addMarker(element, m);
        } else {
          canvas.removeMarker(element, m);
        }
      });
    }

    // event handling //////////

    eventBus.on([ 'create.move', 'create.hover' ], function(event) {
      var context = event.context,
          elements = context.elements,
          hover = event.hover,
          source = context.source,
          hints = context.hints || {};

      if (!hover) {
        context.canExecute = false;
        context.target = null;

        return;
      }

      ensureConstraints$2(event);

      var position = {
        x: event.x,
        y: event.y
      };

      var canExecute = context.canExecute = hover && canCreate(elements, hover, position, source, hints);

      if (hover && canExecute !== null) {
        context.target = hover;

        if (canExecute && canExecute.attach) {
          setMarker(hover, MARKER_ATTACH$1);
        } else {
          setMarker(hover, canExecute ? MARKER_NEW_PARENT$1 : MARKER_NOT_OK$3);
        }
      }
    });

    eventBus.on([ 'create.end', 'create.out', 'create.cleanup' ], function(event) {
      var hover = event.hover;

      if (hover) {
        setMarker(hover, null);
      }
    });

    eventBus.on('create.end', function(event) {
      var context = event.context,
          source = context.source,
          shape = context.shape,
          elements = context.elements,
          target = context.target,
          canExecute = context.canExecute,
          attach = canExecute && canExecute.attach,
          connect = canExecute && canExecute.connect,
          hints = context.hints || {};

      if (canExecute === false || !target) {
        return false;
      }

      ensureConstraints$2(event);

      var position = {
        x: event.x,
        y: event.y
      };

      if (connect) {
        shape = modeling.appendShape(source, shape, position, target, {
          attach: attach,
          connection: connect === true ? {} : connect,
          connectionTarget: hints.connectionTarget
        });
      } else {
        elements = modeling.createElements(elements, position, target, assign$1({}, hints, {
          attach: attach
        }));

        // update shape
        shape = find(elements, function(element) {
          return !isConnection(element);
        });
      }

      // update elements and shape
      assign$1(context, {
        elements: elements,
        shape: shape
      });

      assign$1(event, {
        elements: elements,
        shape: shape
      });
    });

    function cancel() {
      var context = dragging.context();

      if (context && context.prefix === PREFIX$1) {
        dragging.cancel();
      }
    }

    // cancel on <elements.changed> that is not result of <drag.end>
    eventBus.on('create.init', function() {
      eventBus.on('elements.changed', cancel);

      eventBus.once([ 'create.cancel', 'create.end' ], HIGH_PRIORITY$3, function() {
        eventBus.off('elements.changed', cancel);
      });
    });

    // API //////////

    this.start = function(event, elements, context) {
      if (!isArray$3(elements)) {
        elements = [ elements ];
      }

      var shape = find(elements, function(element) {
        return !isConnection(element);
      });

      if (!shape) {

        // at least one shape is required
        return;
      }

      context = assign$1({
        elements: elements,
        hints: {},
        shape: shape
      }, context || {});

      // make sure each element has x and y
      forEach$1(elements, function(element) {
        if (!isNumber(element.x)) {
          element.x = 0;
        }

        if (!isNumber(element.y)) {
          element.y = 0;
        }
      });

      var visibleElements = filter(elements, function(element) {
        return !element.hidden;
      });

      var bbox = getBBox(visibleElements);

      // center elements around cursor
      forEach$1(elements, function(element) {
        if (isConnection(element)) {
          element.waypoints = map$1(element.waypoints, function(waypoint) {
            return {
              x: waypoint.x - bbox.x - bbox.width / 2,
              y: waypoint.y - bbox.y - bbox.height / 2
            };
          });
        }

        assign$1(element, {
          x: element.x - bbox.x - bbox.width / 2,
          y: element.y - bbox.y - bbox.height / 2
        });
      });

      dragging.init(event, PREFIX$1, {
        cursor: 'grabbing',
        autoActivate: true,
        data: {
          shape: shape,
          elements: elements,
          context: context
        }
      });
    };
  }

  Create$1.$inject = [
    'canvas',
    'dragging',
    'eventBus',
    'modeling',
    'rules'
  ];

  // helpers //////////

  function ensureConstraints$2(event) {
    var context = event.context,
        createConstraints = context.createConstraints;

    if (!createConstraints) {
      return;
    }

    if (createConstraints.left) {
      event.x = Math.max(event.x, createConstraints.left);
    }

    if (createConstraints.right) {
      event.x = Math.min(event.x, createConstraints.right);
    }

    if (createConstraints.top) {
      event.y = Math.max(event.y, createConstraints.top);
    }

    if (createConstraints.bottom) {
      event.y = Math.min(event.y, createConstraints.bottom);
    }
  }

  function isSingleShape(elements) {
    return elements && elements.length === 1 && !isConnection(elements[ 0 ]);
  }

  /**
   * @typedef {import('../../core/Canvas').default} Canvas
   * @typedef {import('../../core/EventBus').default} EventBus
   * @typedef {import('../../core/GraphicsFactory').default} GraphicsFactory
   * @typedef {import('../preview-support/PreviewSupport').default} PreviewSupport
   * @typedef {import('../../draw/Styles').default} Styles
   */

  var LOW_PRIORITY$3 = 750;

  /**
   * @param {Canvas} canvas
   * @param {EventBus} eventBus
   * @param {GraphicsFactory} graphicsFactory
   * @param {PreviewSupport} previewSupport
   * @param {Styles} styles
   */
  function CreatePreview(
      canvas,
      eventBus,
      graphicsFactory,
      previewSupport,
      styles
  ) {
    function createDragGroup(elements) {
      var dragGroup = create$1('g');

      attr(dragGroup, styles.cls('djs-drag-group', [ 'no-events' ]));

      var childrenGfx = create$1('g');

      elements.forEach(function(element) {

        // create graphics
        var gfx;

        if (element.hidden) {
          return;
        }

        if (element.waypoints) {
          gfx = graphicsFactory._createContainer('connection', childrenGfx);

          graphicsFactory.drawConnection(getVisual(gfx), element);
        } else {
          gfx = graphicsFactory._createContainer('shape', childrenGfx);

          graphicsFactory.drawShape(getVisual(gfx), element);

          translate(gfx, element.x, element.y);
        }

        // add preview
        previewSupport.addDragger(element, dragGroup, gfx);
      });

      return dragGroup;
    }

    eventBus.on('create.move', LOW_PRIORITY$3, function(event) {

      var hover = event.hover,
          context = event.context,
          elements = context.elements,
          dragGroup = context.dragGroup;

      // lazily create previews
      if (!dragGroup) {
        dragGroup = context.dragGroup = createDragGroup(elements);
      }

      var activeLayer;

      if (hover) {
        if (!dragGroup.parentNode) {
          activeLayer = canvas.getActiveLayer();

          append(activeLayer, dragGroup);
        }

        translate(dragGroup, event.x, event.y);
      } else {
        remove$1(dragGroup);
      }
    });

    eventBus.on('create.cleanup', function(event) {
      var context = event.context,
          dragGroup = context.dragGroup;

      if (dragGroup) {
        remove$1(dragGroup);
      }
    });
  }

  CreatePreview.$inject = [
    'canvas',
    'eventBus',
    'graphicsFactory',
    'previewSupport',
    'styles'
  ];

  /**
   * @type { import('didi').ModuleDeclaration }
   */
  var CreateModule$1 = {
    __depends__: [
      DraggingModule,
      PreviewSupportModule,
      RulesModule,
      SelectionModule
    ],
    __init__: [
      'create',
      'createPreview'
    ],
    create: [ 'type', Create$1 ],
    createPreview: [ 'type', CreatePreview ]
  };

  /**
   * Util that provides unique IDs.
   *
   * @class
   * @constructor
   *
   * The ids can be customized via a given prefix and contain a random value to avoid collisions.
   *
   * @param {string} [prefix] a prefix to prepend to generated ids (for better readability)
   */
  function IdGenerator(prefix) {

    this._counter = 0;
    this._prefix = (prefix ? prefix + '-' : '') + Math.floor(Math.random() * 1000000000) + '-';
  }

  /**
   * Returns a next unique ID.
   *
   * @return {string} the id
   */
  IdGenerator.prototype.next = function() {
    return this._prefix + (++this._counter);
  };

  // document wide unique overlay ids
  var ids = new IdGenerator('ov');

  var LOW_PRIORITY$2 = 500;

  /**
   * @typedef {import('../../core/Canvas').default} Canvas
   * @typedef {import('../../core/ElementRegistry').default} ElementRegistry
   * @typedef {import('../../core/EventBus').default} EventBus
   *
   * @typedef {import('../../model/Types').Element} Element
   *
   * @typedef { {
   *   minZoom?: number,
   *   maxZoom?: number
   * } } OverlaysConfigShow
   *
   * @typedef { {
   *   min?: number,
   *   max?: number
   * } } OverlaysConfigScale
   *
   * @typedef { {
  *   id: string,
  *   type: string | null,
  *   element: Element | string
  * } & OverlayAttrs } Overlay
  *
   * @typedef { {
   *   html: HTMLElement | string,
   *   position: {
   *     top?: number,
   *     right?: number,
   *     bottom?: number,
   *     left?: number
   *   }
   * } & OverlaysConfigDefault } OverlayAttrs
   *
   * @typedef { {
   *   html: HTMLElement,
   *   element: Element,
   *   overlays: Overlay[]
   * } } OverlayContainer
   *
   * @typedef {{
   *   defaults?: OverlaysConfigDefault
   * }} OverlaysConfig
   *
   * @typedef { {
   *  show?: OverlaysConfigShow,
   *  scale?: OverlaysConfigScale | boolean
   * } } OverlaysConfigDefault
   *
   * @typedef { {
   *   id?: string;
   *   element?: Element | string;
   *   type?: string;
   * } | string } OverlaysFilter
   */

  /**
   * A service that allows users to attach overlays to diagram elements.
   *
   * The overlay service will take care of overlay positioning during updates.
   *
   * @example
   *
   * ```javascript
   * // add a pink badge on the top left of the shape
   *
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
   * ```
   *
   * ```javascript
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
   * ```
   *
   * @param {OverlaysConfig} config
   * @param {EventBus} eventBus
   * @param {Canvas} canvas
   * @param {ElementRegistry} elementRegistry
   */
  function Overlays(config, eventBus, canvas, elementRegistry) {
    this._eventBus = eventBus;
    this._canvas = canvas;
    this._elementRegistry = elementRegistry;

    this._ids = ids;

    /**
     * @type {OverlaysConfigDefault}
     */
    this._overlayDefaults = assign$1({

      // no show constraints
      show: null,

      // always scale
      scale: true
    }, config && config.defaults);

    /**
     * @type {Map<string, Overlay>}
     */
    this._overlays = {};

    /**
     * @type {OverlayContainer[]}
     */
    this._overlayContainers = [];

    /**
     * @type {HTMLElement}
     */
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
   * Returns the overlay with the specified ID or a list of overlays
   * for an element with a given type.
   *
   * @example
   *
   * ```javascript
   * // return the single overlay with the given ID
   * overlays.get('some-id');
   *
   * // return all overlays for the shape
   * overlays.get({ element: someShape });
   *
   * // return all overlays on shape with type 'badge'
   * overlays.get({ element: someShape, type: 'badge' });
   *
   * // shape can also be specified as ID
   * overlays.get({ element: 'element-id', type: 'badge' });
   * ```
   *
   * @param {OverlaysFilter} search The filter to be used to find the overlay(s).
   *
   * @return {Overlay|Overlay[]} The overlay(s).
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
   * Adds an HTML overlay to an element.
   *
   * @param {Element|string} element The element to add the overlay to.
   * @param {string} [type] An optional type that can be used to filter.
   * @param {OverlayAttrs} overlay The overlay.
   *
   * @return {string} The overlay's ID that can be used to get or remove it.
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

    overlay = assign$1({}, this._overlayDefaults, overlay, {
      id: id,
      type: type,
      element: element,
      html: overlay.html
    });

    this._addOverlay(overlay);

    return id;
  };


  /**
   * Remove an overlay with the given ID or all overlays matching the given filter.
   *
   * @see Overlays#get for filter options.
   *
   * @param {OverlaysFilter} filter The filter to be used to find the overlay.
   */
  Overlays.prototype.remove = function(filter) {

    var overlays = this.get(filter) || [];

    if (!isArray$3(overlays)) {
      overlays = [ overlays ];
    }

    var self = this;

    forEach$1(overlays, function(overlay) {

      var container = self._getOverlayContainer(overlay.element, true);

      if (overlay) {
        remove$2(overlay.html);
        remove$2(overlay.htmlContainer);

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

  /**
   * Checks whether overlays are shown.
   *
   * @return {boolean} Whether overlays are shown.
   */
  Overlays.prototype.isShown = function() {
    return this._overlayRoot.style.display !== 'none';
  };

  /**
   * Show all overlays.
   */
  Overlays.prototype.show = function() {
    setVisible(this._overlayRoot);
  };

  /**
   * Hide all overlays.
   */
  Overlays.prototype.hide = function() {
    setVisible(this._overlayRoot, false);
  };

  /**
   * Remove all overlays and their container.
   */
  Overlays.prototype.clear = function() {
    this._overlays = {};

    this._overlayContainers = [];

    clear$1(this._overlayRoot);
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

    attr$1(container.html, 'data-container-id', element.id);
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
    this._updateOverlayVisibilty(overlay, this._canvas.viewbox());
  };


  Overlays.prototype._createOverlayContainer = function(element) {
    var html = domify$1('<div class="djs-overlays" />');
    assign(html, { position: 'absolute' });

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
      html = domify$1(html);
    }

    overlayContainer = this._getOverlayContainer(element);

    htmlContainer = domify$1('<div class="djs-overlay" data-overlay-id="' + id + '">');
    assign(htmlContainer, { position: 'absolute' });

    htmlContainer.appendChild(html);

    if (overlay.type) {
      classes$1(htmlContainer).add('djs-overlay-' + overlay.type);
    }

    var elementRoot = this._canvas.findRoot(element);
    var activeRoot = this._canvas.getRootElement();

    setVisible(htmlContainer, elementRoot === activeRoot);

    overlay.htmlContainer = htmlContainer;

    overlayContainer.overlays.push(overlay);
    overlayContainer.html.appendChild(htmlContainer);

    this._overlays[id] = overlay;

    this._updateOverlay(overlay);
    this._updateOverlayVisibilty(overlay, this._canvas.viewbox());
  };


  Overlays.prototype._updateOverlayVisibilty = function(overlay, viewbox) {
    var show = overlay.show,
        rootElement = this._canvas.findRoot(overlay.element),
        minZoom = show && show.minZoom,
        maxZoom = show && show.maxZoom,
        htmlContainer = overlay.htmlContainer,
        activeRootElement = this._canvas.getRootElement(),
        visible = true;

    if (rootElement !== activeRootElement) {
      visible = false;
    } else if (show) {
      if (
        (isDefined(minZoom) && minZoom > viewbox.scale) ||
        (isDefined(maxZoom) && maxZoom < viewbox.scale)
      ) {
        visible = false;
      }
    }

    setVisible(htmlContainer, visible);

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

    forEach$1(this._overlays, function(overlay) {
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

      forEach$1(overlays, function(o) {
        self.remove(o.id);
      });

      var container = self._getOverlayContainer(element);

      if (container) {
        remove$2(container.html);
        var i = self._overlayContainers.indexOf(container);
        if (i !== -1) {
          self._overlayContainers.splice(i, 1);
        }
      }
    });


    // move integration

    eventBus.on('element.changed', LOW_PRIORITY$2, function(e) {
      var element = e.element;

      var container = self._getOverlayContainer(element, true);

      if (container) {
        forEach$1(container.overlays, function(overlay) {
          self._updateOverlay(overlay);
        });

        self._updateOverlayContainer(container);
      }
    });


    // marker integration, simply add them on the overlays as classes, too.

    eventBus.on('element.marker.update', function(e) {
      var container = self._getOverlayContainer(e.element, true);
      if (container) {
        classes$1(container.html)[e.add ? 'add' : 'remove'](e.marker);
      }
    });


    eventBus.on('root.set', function() {
      self._updateOverlaysVisibilty(self._canvas.viewbox());
    });

    // clear overlays with diagram

    eventBus.on('diagram.clear', this.clear, this);
  };



  // helpers /////////////////////////////

  function createRoot(parentNode) {
    var root = domify$1(
      '<div class="djs-overlay-container" />'
    );

    assign(root, {
      position: 'absolute',
      width: 0,
      height: 0
    });

    parentNode.insertBefore(root, parentNode.firstChild);

    return root;
  }

  function setPosition(el, x, y) {
    assign(el, { left: x + 'px', top: y + 'px' });
  }

  /**
   * Set element visible
   *
   * @param {DOMElement} el
   * @param {boolean} [visible=true]
   */
  function setVisible(el, visible) {
    el.style.display = visible === false ? 'none' : '';
  }

  function setTransform(el, transform) {

    el.style['transform-origin'] = 'top left';

    [ '', '-ms-', '-webkit-' ].forEach(function(prefix) {
      el.style[prefix + 'transform'] = transform;
    });
  }

  /**
   * @type { import('didi').ModuleDeclaration }
   */
  var OverlaysModule = {
    __init__: [ 'overlays' ],
    overlays: [ 'type', Overlays ]
  };

  /**
   * @typedef {import('../../model/Types').Element} Element
   *
   * @typedef {import('../../util/Types').Rect} Rect
   *
   * @typedef {import('../../core/Canvas').default} Canvas
   * @typedef {import('../../core/EventBus').default} EventBus
   * @typedef {import('../overlays/Overlays').default} Overlays
   *
   * @typedef {import('../overlays/Overlays').Overlay} Overlay
   *
   * @typedef {import('./ContextPadProvider').default} ContextPadProvider
   * @typedef {import('./ContextPadProvider').ContextPadEntries} ContextPadEntries
   *
   * @typedef { {
   *   scale?: {
   *     min?: number;
   *     max?: number;
   *   };
   * } } ContextPadConfig
   */

  /**
   * @template {Element} [ElementType=Element]
   *
   * @typedef {ElementType|ElementType[]} ContextPadTarget
   */

  var entrySelector = '.entry';

  var DEFAULT_PRIORITY$2 = 1000;
  var CONTEXT_PAD_PADDING = 12;

  /**
   * A context pad that displays element specific, contextual actions next
   * to a diagram element.
   *
   * @param {Canvas} canvas
   * @param {ContextPadConfig} config
   * @param {EventBus} eventBus
   * @param {Overlays} overlays
   */
  function ContextPad(canvas, config, eventBus, overlays) {

    this._canvas = canvas;
    this._eventBus = eventBus;
    this._overlays = overlays;

    var scale = isDefined(config && config.scale) ? config.scale : {
      min: 1,
      max: 1.5
    };

    this._overlaysConfig = {
      scale: scale
    };

    this._current = null;

    this._init();
  }

  ContextPad.$inject = [
    'canvas',
    'config.contextPad',
    'eventBus',
    'overlays'
  ];


  /**
   * Registers events needed for interaction with other components.
   */
  ContextPad.prototype._init = function() {
    var self = this;

    this._eventBus.on('selection.changed', function(event) {

      var selection = event.newSelection;

      var target = selection.length
        ? selection.length === 1
          ? selection[0]
          : selection
        : null;

      if (target) {
        self.open(target, true);
      } else {
        self.close();
      }
    });

    this._eventBus.on('elements.changed', function(event) {
      var elements = event.elements,
          current = self._current;

      if (!current) {
        return;
      }

      var currentTarget = current.target;

      var currentChanged = some(
        isArray$3(currentTarget) ? currentTarget : [ currentTarget ],
        function(element) {
          return includes$2(elements, element);
        }
      );

      // re-open if elements in current selection changed
      if (currentChanged) {
        self.open(currentTarget, true);
      }
    });
  };

  /**
   * @overlord
   *
   * Register a context pad provider with the default priority. See
   * {@link ContextPadProvider} for examples.
   *
   * @param {ContextPadProvider} provider
   */

  /**
   * Register a context pad provider with the given priority. See
   * {@link ContextPadProvider} for examples.
   *
   * @param {number} priority
   * @param {ContextPadProvider} provider
   */
  ContextPad.prototype.registerProvider = function(priority, provider) {
    if (!provider) {
      provider = priority;
      priority = DEFAULT_PRIORITY$2;
    }

    this._eventBus.on('contextPad.getProviders', priority, function(event) {
      event.providers.push(provider);
    });
  };


  /**
   * Get context pad entries for given elements.
   *
   * @param {ContextPadTarget} target
   *
   * @return {ContextPadEntries} list of entries
   */
  ContextPad.prototype.getEntries = function(target) {
    var providers = this._getProviders();

    var provideFn = isArray$3(target)
      ? 'getMultiElementContextPadEntries'
      : 'getContextPadEntries';

    var entries = {};

    // loop through all providers and their entries.
    // group entries by id so that overriding an entry is possible
    forEach$1(providers, function(provider) {

      if (!isFunction(provider[provideFn])) {
        return;
      }

      var entriesOrUpdater = provider[provideFn](target);

      if (isFunction(entriesOrUpdater)) {
        entries = entriesOrUpdater(entries);
      } else {
        forEach$1(entriesOrUpdater, function(entry, id) {
          entries[id] = entry;
        });
      }
    });

    return entries;
  };


  /**
   * Trigger context pad via DOM event.
   *
   * The entry to trigger is determined by the target element.
   *
   * @param {string} action
   * @param {Event} event
   * @param {boolean} [autoActivate=false]
   */
  ContextPad.prototype.trigger = function(action, event, autoActivate) {

    var entry,
        originalEvent,
        button = event.delegateTarget || event.target;

    if (!button) {
      return event.preventDefault();
    }

    entry = attr$1(button, 'data-action');
    originalEvent = event.originalEvent || event;

    return this.triggerEntry(entry, action, originalEvent, autoActivate);
  };

  /**
   * Trigger context pad entry entry.
   *
   * @param {string} entryId
   * @param {string} action
   * @param {Event} event
   * @param {boolean} [autoActivate=false]
   */
  ContextPad.prototype.triggerEntry = function(entryId, action, event, autoActivate) {

    if (!this.isShown()) {
      return;
    }

    var target = this._current.target,
        entries = this._current.entries;

    var entry = entries[entryId];

    if (!entry) {
      return;
    }

    var handler = entry.action;

    if (this._eventBus.fire('contextPad.trigger', { entry, event }) === false) {
      return;
    }

    // simple action (via callback function)
    if (isFunction(handler)) {
      if (action === 'click') {
        return handler(event, target, autoActivate);
      }
    } else {
      if (handler[action]) {
        return handler[action](event, target, autoActivate);
      }
    }

    // silence other actions
    event.preventDefault();
  };


  /**
   * Open the context pad for given elements.
   *
   * @param {ContextPadTarget} target
   * @param {boolean} [force=false] - Force re-opening context pad.
   */
  ContextPad.prototype.open = function(target, force) {
    if (!force && this.isOpen(target)) {
      return;
    }

    this.close();

    this._updateAndOpen(target);
  };

  ContextPad.prototype._getProviders = function() {

    var event = this._eventBus.createEvent({
      type: 'contextPad.getProviders',
      providers: []
    });

    this._eventBus.fire(event);

    return event.providers;
  };


  /**
   * @param {ContextPadTarget} target
   */
  ContextPad.prototype._updateAndOpen = function(target) {
    var entries = this.getEntries(target),
        pad = this.getPad(target),
        html = pad.html,
        image;

    forEach$1(entries, function(entry, id) {
      var grouping = entry.group || 'default',
          control = domify$1(entry.html || '<div class="entry" draggable="true"></div>'),
          container;

      attr$1(control, 'data-action', id);

      container = query('[data-group=' + escapeCSS(grouping) + ']', html);
      if (!container) {
        container = domify$1('<div class="group"></div>');
        attr$1(container, 'data-group', grouping);

        html.appendChild(container);
      }

      container.appendChild(control);

      if (entry.className) {
        addClasses(control, entry.className);
      }

      if (entry.title) {
        attr$1(control, 'title', entry.title);
      }

      if (entry.imageUrl) {
        image = domify$1('<img>');
        attr$1(image, 'src', entry.imageUrl);
        image.style.width = '100%';
        image.style.height = '100%';

        control.appendChild(image);
      }
    });

    classes$1(html).add('open');

    this._current = {
      target: target,
      entries: entries,
      pad: pad
    };

    this._eventBus.fire('contextPad.open', { current: this._current });
  };

  /**
   * @param {ContextPadTarget} target
   *
   * @return {Overlay}
   */
  ContextPad.prototype.getPad = function(target) {
    if (this.isOpen()) {
      return this._current.pad;
    }

    var self = this;

    var overlays = this._overlays;

    var html = domify$1('<div class="djs-context-pad"></div>');

    var position = this._getPosition(target);

    var overlaysConfig = assign$1({
      html: html
    }, this._overlaysConfig, position);

    delegate.bind(html, entrySelector, 'click', function(event) {
      self.trigger('click', event);
    });

    delegate.bind(html, entrySelector, 'dragstart', function(event) {
      self.trigger('dragstart', event);
    });

    // stop propagation of mouse events
    event.bind(html, 'mousedown', function(event) {
      event.stopPropagation();
    });

    var activeRootElement = this._canvas.getRootElement();

    this._overlayId = overlays.add(activeRootElement, 'context-pad', overlaysConfig);

    var pad = overlays.get(this._overlayId);

    this._eventBus.fire('contextPad.create', {
      target: target,
      pad: pad
    });

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
   * Check if pad is open.
   *
   * If target is provided, check if it is opened
   * for the given target (single or multiple elements).
   *
   * @param {ContextPadTarget} [target]
   * @return {boolean}
   */
  ContextPad.prototype.isOpen = function(target) {
    var current = this._current;

    if (!current) {
      return false;
    }

    // basic no-args is open check
    if (!target) {
      return true;
    }

    var currentTarget = current.target;

    // strict handling of single vs. multi-selection
    if (isArray$3(target) !== isArray$3(currentTarget)) {
      return false;
    }

    if (isArray$3(target)) {
      return (
        target.length === currentTarget.length &&
        every(target, function(element) {
          return includes$2(currentTarget, element);
        })
      );
    } else {
      return currentTarget === target;
    }
  };


  /**
   * Check if pad is open and not hidden.
   *
   * @return {boolean}
   */
  ContextPad.prototype.isShown = function() {
    return this.isOpen() && this._overlays.isShown();
  };


  /**
   * Get contex pad position.
   *
   * @param {ContextPadTarget} target
   *
   * @return {Rect}
   */
  ContextPad.prototype._getPosition = function(target) {

    var elements = isArray$3(target) ? target : [ target ];
    var bBox = getBBox(elements);

    return {
      position: {
        left: bBox.x + bBox.width + CONTEXT_PAD_PADDING,
        top: bBox.y - CONTEXT_PAD_PADDING / 2
      }
    };
  };


  // helpers //////////

  function addClasses(element, classNames) {
    var classes = classes$1(element);

    classNames = isArray$3(classNames) ? classNames : classNames.split(/\s+/g);

    classNames.forEach(function(cls) {
      classes.add(cls);
    });
  }

  /**
   * @param {any[]} array
   * @param {any} item
   *
   * @return {boolean}
   */
  function includes$2(array, item) {
    return array.indexOf(item) !== -1;
  }

  /**
   * @type { import('didi').ModuleDeclaration }
   */
  var ContextPadModule = {
    __depends__: [
      InteractionEventsModule,
      OverlaysModule
    ],
    contextPad: [ 'type', ContextPad ]
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


  var ALIGNED_THRESHOLD = 2;

  /**
   * Check whether two points are horizontally or vertically aligned.
   *
   * @param {Point[]|Point} a
   * @param {Point} [b]
   *
   * @return {string|boolean} If and how the two points are aligned ('h', 'v' or `false`).
   */
  function pointsAligned(a, b) {
    var points = Array.from(arguments).flat();

    const axisMap = {
      'x': 'v',
      'y': 'h'
    };

    for (const [ axis, orientation ] of Object.entries(axisMap)) {
      if (pointsAlignedOnAxis(axis, points)) {
        return orientation;
      }
    }

    return false;
  }

  /**
   * @param {Axis} axis
   * @param {Point[]} points
   *
   * @return {boolean}
   */
  function pointsAlignedOnAxis(axis, points) {
    const referencePoint = points[0];

    return every(points, function(point) {
      return Math.abs(referencePoint[axis] - point[axis]) <= ALIGNED_THRESHOLD;
    });
  }

  /**
   * Returns true if the point p is inside the rectangle rect
   *
   * @param {Point} p
   * @param {Rect} rect
   * @param {number} tolerance
   *
   * @return {boolean}
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
   * @param {Point} p
   * @param {Point} q
   *
   * @return {Point} The mid point between the two points.
   */
  function getMidPoint(p, q) {
    return {
      x: Math.round(p.x + ((q.x - p.x) / 2.0)),
      y: Math.round(p.y + ((q.y - p.y) / 2.0))
    };
  }

  function getDefaultExportFromCjs (x) {
  	return x && x.__esModule && Object.prototype.hasOwnProperty.call(x, 'default') ? x['default'] : x;
  }

  /**
   * This file contains source code adapted from Snap.svg (licensed Apache-2.0).
   *
   * @see https://github.com/adobe-webplatform/Snap.svg/blob/master/src/path.js
   */

  /* eslint no-fallthrough: "off" */

  var p2s = /,?([a-z]),?/gi,
      toFloat = parseFloat,
      math = Math,
      PI = math.PI,
      mmin = math.min,
      mmax = math.max,
      pow = math.pow,
      abs$1 = math.abs,
      pathCommand = /([a-z])[\s,]*((-?\d*\.?\d*(?:e[-+]?\d+)?[\s]*,?[\s]*)+)/ig,
      pathValues = /(-?\d*\.?\d*(?:e[-+]?\d+)?)[\s]*,?[\s]*/ig;

  var isArray$1 = Array.isArray || function(o) { return o instanceof Array; };

  function hasProperty(obj, property) {
    return Object.prototype.hasOwnProperty.call(obj, property);
  }

  function clone(obj) {

    if (typeof obj == 'function' || Object(obj) !== obj) {
      return obj;
    }

    var res = new obj.constructor;

    for (var key in obj) {
      if (hasProperty(obj, key)) {
        res[key] = clone(obj[key]);
      }
    }

    return res;
  }

  function repush(array, item) {
    for (var i = 0, ii = array.length; i < ii; i++) if (array[i] === item) {
      return array.push(array.splice(i, 1)[0]);
    }
  }

  function cacher(f) {

    function newf() {

      var arg = Array.prototype.slice.call(arguments, 0),
          args = arg.join('\u2400'),
          cache = newf.cache = newf.cache || {},
          count = newf.count = newf.count || [];

      if (hasProperty(cache, args)) {
        repush(count, args);
        return cache[args];
      }

      count.length >= 1e3 && delete cache[count.shift()];
      count.push(args);
      cache[args] = f.apply(0, arg);

      return cache[args];
    }
    return newf;
  }

  function parsePathString(pathString) {

    if (!pathString) {
      return null;
    }

    var pth = paths(pathString);

    if (pth.arr) {
      return clone(pth.arr);
    }

    var paramCounts = { a: 7, c: 6, h: 1, l: 2, m: 2, q: 4, s: 4, t: 2, v: 1, z: 0 },
        data = [];

    if (isArray$1(pathString) && isArray$1(pathString[0])) { // rough assumption
      data = clone(pathString);
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

        while (params.length >= paramCounts[name]) {
          data.push([b].concat(params.splice(0, paramCounts[name])));
          if (!paramCounts[name]) {
            break;
          }
        }
      });
    }

    data.toString = paths.toString;
    pth.arr = clone(data);

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
      for (var key in p) {
        if (hasProperty(p, key) && key != ps) {
          p[key].sleep--;
          !p[key].sleep && delete p[key];
        }
      }
    });

    return p[ps];
  }

  function rectBBox(x, y, width, height) {

    if (arguments.length === 1) {
      y = x.y;
      width = x.width;
      height = x.height;
      x = x.x;
    }

    return {
      x: x,
      y: y,
      width: width,
      height: height,
      x2: x + width,
      y2: y + height
    };
  }

  function pathToString() {
    return this.join(',').replace(p2s, '$1');
  }

  function pathClone(pathArray) {
    var res = clone(pathArray);
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
        y = t13 * p1y + t12 * 3 * t * c1y + t1 * 3 * t * t * c2y + t3 * p2y;

    return {
      x: fixError(x),
      y: fixError(y)
    };
  }

  function bezierBBox(points) {

    var bbox = curveBBox.apply(null, points);

    return rectBBox(
      bbox.x0,
      bbox.y0,
      bbox.x1 - bbox.x0,
      bbox.y1 - bbox.y0
    );
  }

  function isPointInsideBBox(bbox, x, y) {
    return x >= bbox.x &&
      x <= bbox.x + bbox.width &&
      y >= bbox.y &&
      y <= bbox.y + bbox.height;
  }

  function isBBoxIntersect(bbox1, bbox2) {
    bbox1 = rectBBox(bbox1);
    bbox2 = rectBBox(bbox2);
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

    var px = fixError(nx / denominator),
        py = fixError(ny / denominator),
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

  function fixError(number) {
    return Math.round(number * 100000000000) / 100000000000;
  }

  function findBezierIntersections(bez1, bez2, justCount) {
    var bbox1 = bezierBBox(bez1),
        bbox2 = bezierBBox(bez2);

    if (!isBBoxIntersect(bbox1, bbox2)) {
      return justCount ? 0 : [];
    }

    // As an optimization, lines will have only 1 segment

    var l1 = bezlen.apply(0, bez1),
        l2 = bezlen.apply(0, bez2),
        n1 = isLine(bez1) ? 1 : ~~(l1 / 5) || 1,
        n2 = isLine(bez2) ? 1 : ~~(l2 / 5) || 1,
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
            ci = abs$1(di1.x - di.x) < .01 ? 'y' : 'x',
            cj = abs$1(dj1.x - dj.x) < .01 ? 'y' : 'x',
            is = intersectLines(di.x, di.y, di1.x, di1.y, dj.x, dj.y, dj1.x, dj1.y),
            key;

        if (is) {
          key = is.x.toFixed(9) + '#' + is.y.toFixed(9);

          if (xy[key]) {
            continue;
          }

          xy[key] = true;

          var t1 = di.t + abs$1((is[ci] - di[ci]) / (di1[ci] - di[ci])) * (di1.t - di.t),
              t2 = dj.t + abs$1((is[cj] - dj[cj]) / (dj1[cj] - dj[cj])) * (dj1.t - dj.t);

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
   * // ]
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


  function pathToAbsolute(pathArray) {
    var pth = paths(pathArray);

    if (pth.abs) {
      return pathClone(pth.abs);
    }

    if (!isArray$1(pathArray) || !isArray$1(pathArray && pathArray[0])) { // rough assumption
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
        case 'M':
          mx = +pa[1] + x;
          my = +pa[2] + y;
        default:
          for (var j = 1, jj = pa.length; j < jj; j++) {
            r[j] = +pa[j] + ((j % 2) ? x : y);
          }
        }
      } else {
        for (var k = 0, kk = pa.length; k < kk; k++) {
          r[k] = pa[k];
        }
      }
      pa0 = pa0.toUpperCase();

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

    res.toString = pathToString;
    pth.abs = pathClone(res);

    return res;
  }

  function isLine(bez) {
    return (
      bez[0] === bez[2] &&
      bez[1] === bez[3] &&
      bez[4] === bez[6] &&
      bez[5] === bez[7]
    );
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
              math.sqrt(abs$1((rx2 * ry2 - rx2 * y * y - ry2 * x * x) / (rx2 * y * y + ry2 * x * x))),
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

    if (abs$1(df) > _120) {
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

      if (abs$1(a) < 1e-12) {

        if (abs$1(b) < 1e-12) {
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
      x0: mmin.apply(0, bounds[0]),
      y0: mmin.apply(0, bounds[1]),
      x1: mmax.apply(0, bounds[0]),
      y1: mmax.apply(0, bounds[1])
    };
  }

  function pathToCurve(path) {

    var pth = paths(path);

    // return cached curve, if existing
    if (pth.curve) {
      return pathClone(pth.curve);
    }

    var curvedPath = pathToAbsolute(path),
        attrs = { x: 0, y: 0, bx: 0, by: 0, X: 0, Y: 0, qx: null, qy: null },
        processPath = function(path, d, pathCommand) {
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
            if (pathCommand == 'C' || pathCommand == 'S') {

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
            if (pathCommand == 'Q' || pathCommand == 'T') {

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
              pathCommands[i] = 'A'; // if created multiple C:s, their original seg is saved
              pp.splice(i++, 0, ['C'].concat(pi.splice(0, 6)));
            }

            pp.splice(i, 1);
            ii = curvedPath.length;
          }
        },

        pathCommands = [], // path commands of original path p
        pfirst = '', // temporary holder for original path command
        pathCommand = ''; // holder for previous path command of original path

    for (var i = 0, ii = curvedPath.length; i < ii; i++) {
      curvedPath[i] && (pfirst = curvedPath[i][0]); // save current path command

      if (pfirst != 'C') // C is not saved yet, because it may be result of conversion
      {
        pathCommands[i] = pfirst; // Save current path command
        i && (pathCommand = pathCommands[i - 1]); // Get previous path command pathCommand
      }
      curvedPath[i] = processPath(curvedPath[i], attrs, pathCommand); // Previous path command is inputted to processPath

      if (pathCommands[i] != 'A' && pfirst == 'C') pathCommands[i] = 'C'; // A is the only command
      // which may produce multiple C:s
      // so we have to make sure that C is also C in original path

      fixArc(curvedPath, i); // fixArc adds also the right amount of A:s to pathCommands

      var seg = curvedPath[i],
          seglen = seg.length;

      attrs.x = seg[seglen - 2];
      attrs.y = seg[seglen - 1];
      attrs.bx = toFloat(seg[seglen - 4]) || attrs.x;
      attrs.by = toFloat(seg[seglen - 3]) || attrs.y;
    }

    // cache curve
    pth.curve = pathClone(curvedPath);

    return curvedPath;
  }

  var intersect = findPathIntersections;

  var intersectPaths = /*@__PURE__*/getDefaultExportFromCjs(intersect);

  /**
   * @typedef {import('../util/Types').Point} Point
   *
   * @typedef { {
   *   bendpoint?: boolean;
   *   index: number;
   *   point: Point;
   * } } Intersection
   */

  var round$6 = Math.round,
      max$2 = Math.max;


  function circlePath(center, r) {
    var x = center.x,
        y = center.y;

    return [
      [ 'M', x, y ],
      [ 'm', 0, -r ],
      [ 'a', r, r, 0, 1, 1, 0, 2 * r ],
      [ 'a', r, r, 0, 1, 1, 0, -2 * r ],
      [ 'z' ]
    ];
  }

  function linePath(points) {
    var segments = [];

    points.forEach(function(p, idx) {
      segments.push([ idx === 0 ? 'M' : 'L', p.x, p.y ]);
    });

    return segments;
  }


  var INTERSECTION_THRESHOLD$1 = 10;

  /**
   * @param {Point[]} waypoints
   * @param {Point} reference
   *
   * @return {Intersection|null}
   */
  function getBendpointIntersection(waypoints, reference) {

    var i, w;

    for (i = 0; (w = waypoints[i]); i++) {

      if (pointDistance(w, reference) <= INTERSECTION_THRESHOLD$1) {
        return {
          point: waypoints[i],
          bendpoint: true,
          index: i
        };
      }
    }

    return null;
  }

  /**
   * @param {Point[]} waypoints
   * @param {Point} reference
   *
   * @return {Intersection|null}
   */
  function getPathIntersection(waypoints, reference) {

    var intersections = intersectPaths(circlePath(reference, INTERSECTION_THRESHOLD$1), linePath(waypoints));

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

        idx = max$2(a.segment2, b.segment2) - 1;

        return {
          point: waypoints[idx],
          bendpoint: true,
          index: idx
        };
      }

      return {
        point: {
          x: (round$6(a.x + b.x) / 2),
          y: (round$6(a.y + b.y) / 2)
        },
        index: a.segment2
      };
    }

    return {
      point: {
        x: round$6(a.x),
        y: round$6(a.y)
      },
      index: a.segment2
    };
  }

  /**
   * Returns the closest point on the connection towards a given reference point.
   *
   * @param {Point[]} waypoints
   * @param {Point} reference
   *
   * @return {Intersection|null}
   */
  function getApproxIntersection(waypoints, reference) {
    return getBendpointIntersection(waypoints, reference) || getPathIntersection(waypoints, reference);
  }

  /**
   * @typedef {import('../../util/Types').Point} Point
   * @typedef {import('../../util/Types').Vector} Vector
   */

  /**
   * Returns the length of a vector.
   *
   * @param {Vector} vector
   *
   * @return {number}
   */
  function vectorLength(vector) {
    return Math.sqrt(Math.pow(vector.x, 2) + Math.pow(vector.y, 2));
  }


  /**
   * Solves a 2D equation system
   * a + r*b = c, where a,b,c are 2D vectors
   *
   * @param {Vector} a
   * @param {Vector} b
   * @param {Vector} c
   *
   * @return {number}
   */
  function solveLambaSystem(a, b, c) {

    // the 2d system
    var system = [
      { n: a[0] - c[0], lambda: b[0] },
      { n: a[1] - c[1], lambda: b[1] }
    ];

    // solve
    var n = system[0].n * b[0] + system[1].n * b[1],
        l = system[0].lambda * b[0] + system[1].lambda * b[1];

    return -n / l;
  }


  /**
   * Calculates the position of the perpendicular foot.
   *
   * @param {Point} point
   * @param {Point[]} line
   *
   * @return {Point}
   */
  function perpendicularFoot(point, line) {

    var a = line[0], b = line[1];

    // relative position of b from a
    var bd = { x: b.x - a.x, y: b.y - a.y };

    // solve equation system to the parametrized vectors param real value
    var r = solveLambaSystem([ a.x, a.y ], [ bd.x, bd.y ], [ point.x, point.y ]);

    return { x: a.x + r * bd.x, y: a.y + r * bd.y };
  }


  /**
   * Calculates the distance between a point and a line.
   *
   * @param {Point} point
   * @param {Point[]} line
   *
   * @return {number}
   */
  function getDistancePointLine(point, line) {

    var pfPoint = perpendicularFoot(point, line);

    // distance vector
    var connectionVector = {
      x: pfPoint.x - point.x,
      y: pfPoint.y - point.y
    };

    return vectorLength(connectionVector);
  }

  /**
   * @typedef {import('../../core/Types').ConnectionLike} Connection
   *
   * @typedef {import('../../util/Types').Point} Point
   */

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

  function getConnectionIntersection(canvas, waypoints, event) {
    var localPosition = toCanvasCoordinates(canvas, event),
        intersection = getApproxIntersection(waypoints, localPosition);

    return intersection;
  }

  function addBendpoint(parentGfx, cls) {
    var groupGfx = create$1('g');
    classes(groupGfx).add(BENDPOINT_CLS);

    append(parentGfx, groupGfx);

    var visual = create$1('circle');
    attr(visual, {
      cx: 0,
      cy: 0,
      r: 4
    });
    classes(visual).add('djs-visual');

    append(groupGfx, visual);

    var hit = create$1('circle');
    attr(hit, {
      cx: 0,
      cy: 0,
      r: 10
    });
    classes(hit).add('djs-hit');

    append(groupGfx, hit);

    if (cls) {
      classes(groupGfx).add(cls);
    }

    return groupGfx;
  }

  function createParallelDragger(parentGfx, segmentStart, segmentEnd, alignment) {
    var draggerGfx = create$1('g');

    append(parentGfx, draggerGfx);

    var width = 18,
        height = 6,
        padding = 11,
        hitWidth = calculateHitWidth(segmentStart, segmentEnd, alignment),
        hitHeight = height + padding;

    var visual = create$1('rect');
    attr(visual, {
      x: -width / 2,
      y: -height / 2,
      width: width,
      height: height
    });
    classes(visual).add('djs-visual');

    append(draggerGfx, visual);

    var hit = create$1('rect');
    attr(hit, {
      x: -hitWidth / 2,
      y: -hitHeight / 2,
      width: hitWidth,
      height: hitHeight
    });
    classes(hit).add('djs-hit');

    append(draggerGfx, hit);

    rotate(draggerGfx, alignment === 'v' ? 90 : 0);

    return draggerGfx;
  }


  function addSegmentDragger(parentGfx, segmentStart, segmentEnd) {

    var groupGfx = create$1('g'),
        mid = getMidPoint(segmentStart, segmentEnd),
        alignment = pointsAligned(segmentStart, segmentEnd);

    append(parentGfx, groupGfx);

    createParallelDragger(groupGfx, segmentStart, segmentEnd, alignment);

    classes(groupGfx).add(SEGMENT_DRAGGER_CLS);
    classes(groupGfx).add(alignment === 'h' ? 'horizontal' : 'vertical');

    translate(groupGfx, mid.x, mid.y);

    return groupGfx;
  }

  /**
   * Calculates region for segment move which is 2/3 of the full segment length
   * @param {number} segmentLength
   *
   * @return {number}
   */
  function calculateSegmentMoveRegion(segmentLength) {
    return Math.abs(Math.round(segmentLength * 2 / 3));
  }

  /**
   * Returns the point with the closest distance that is on the connection path.
   *
   * @param {Point} position
   * @param {Connection} connection
   * @return {Point}
   */
  function getClosestPointOnConnection(position, connection) {
    var segment = getClosestSegment(position, connection);

    return perpendicularFoot(position, segment);
  }


  // helper //////////

  function calculateHitWidth(segmentStart, segmentEnd, alignment) {
    var segmentLengthXAxis = segmentEnd.x - segmentStart.x,
        segmentLengthYAxis = segmentEnd.y - segmentStart.y;

    return alignment === 'h' ?
      calculateSegmentMoveRegion(segmentLengthXAxis) :
      calculateSegmentMoveRegion(segmentLengthYAxis);
  }

  function getClosestSegment(position, connection) {
    var waypoints = connection.waypoints;

    var minDistance = Infinity,
        segmentIndex;

    for (var i = 0; i < waypoints.length - 1; i++) {
      var start = waypoints[i],
          end = waypoints[i + 1],
          distance = getDistancePointLine(position, [ start, end ]);

      if (distance < minDistance) {
        minDistance = distance;
        segmentIndex = i;
      }
    }

    return [ waypoints[segmentIndex], waypoints[segmentIndex + 1] ];
  }

  /**
   * @typedef {import('../bendpoints/BendpointMove').default} BendpointMove
   * @typedef {import('../../core/Canvas').default} Canvas
   * @typedef {import('../bendpoints/ConnectionSegmentMove').default} ConnectionSegmentMove
   * @typedef {import('../../core/EventBus').default} EventBus
   * @typedef {import('../interaction-events/InteractionEvents').default} InteractionEvents
   */

  /**
   * A service that adds editable bendpoints to connections.
   *
   * @param {EventBus} eventBus
   * @param {Canvas} canvas
   * @param {InteractionEvents} interactionEvents
   * @param {BendpointMove} bendpointMove
   * @param {ConnectionSegmentMove} connectionSegmentMove
   */
  function Bendpoints(
      eventBus, canvas, interactionEvents,
      bendpointMove, connectionSegmentMove) {

    /**
     * Returns true if intersection point is inside middle region of segment, adjusted by
     * optional threshold
     */
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

    /**
     * Calculates the threshold from a connection's middle which fits the two-third-region
     */
    function calculateIntersectionThreshold(connection, intersection) {
      var waypoints = connection.waypoints,
          relevantSegment, alignment, segmentLength, threshold;

      if (intersection.index <= 0 || intersection.bendpoint) {
        return null;
      }

      // segment relative to connection intersection
      relevantSegment = {
        start: waypoints[intersection.index - 1],
        end: waypoints[intersection.index]
      };

      alignment = pointsAligned(relevantSegment.start, relevantSegment.end);

      if (!alignment) {
        return null;
      }

      if (alignment === 'h') {
        segmentLength = relevantSegment.end.x - relevantSegment.start.x;
      } else {
        segmentLength = relevantSegment.end.y - relevantSegment.start.y;
      }

      // calculate threshold relative to 2/3 of segment length
      threshold = calculateSegmentMoveRegion(segmentLength) / 2;

      return threshold;
    }

    function activateBendpointMove(event, connection) {
      var waypoints = connection.waypoints,
          intersection = getConnectionIntersection(canvas, waypoints, event),
          threshold;

      if (!intersection) {
        return;
      }

      threshold = calculateIntersectionThreshold(connection, intersection);

      if (isIntersectionMiddle(intersection, waypoints, threshold)) {
        connectionSegmentMove.start(event, connection, intersection.index);
      } else {
        bendpointMove.start(event, connection, intersection.index, !intersection.bendpoint);
      }

      // we've handled the event
      return true;
    }

    function bindInteractionEvents(node, eventName, element) {

      event.bind(node, eventName, function(event) {
        interactionEvents.triggerMouseEvent(eventName, event, element);
        event.stopPropagation();
      });
    }

    function getBendpointsContainer(element, create) {

      var layer = canvas.getLayer('overlays'),
          gfx = query('.djs-bendpoints[data-element-id="' + escapeCSS(element.id) + '"]', layer);

      if (!gfx && create) {
        gfx = create$1('g');
        attr(gfx, { 'data-element-id': element.id });
        classes(gfx).add('djs-bendpoints');

        append(layer, gfx);

        bindInteractionEvents(gfx, 'mousedown', element);
        bindInteractionEvents(gfx, 'click', element);
        bindInteractionEvents(gfx, 'dblclick', element);
      }

      return gfx;
    }

    function getSegmentDragger(idx, parentGfx) {
      return query(
        '.djs-segment-dragger[data-segment-idx="' + idx + '"]',
        parentGfx
      );
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
          segmentEnd,
          segmentDraggerGfx;

      for (var i = 1; i < waypoints.length; i++) {

        segmentStart = waypoints[i - 1];
        segmentEnd = waypoints[i];

        if (pointsAligned(segmentStart, segmentEnd)) {
          segmentDraggerGfx = addSegmentDragger(gfx, segmentStart, segmentEnd);

          attr(segmentDraggerGfx, { 'data-segment-idx': i });

          bindInteractionEvents(segmentDraggerGfx, 'mousemove', connection);
        }
      }
    }

    function clearBendpoints(gfx) {
      forEach$1(all('.' + BENDPOINT_CLS, gfx), function(node) {
        remove$1(node);
      });
    }

    function clearSegmentDraggers(gfx) {
      forEach$1(all('.' + SEGMENT_DRAGGER_CLS, gfx), function(node) {
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

    function updateFloatingBendpointPosition(parentGfx, intersection) {
      var floating = query('.floating', parentGfx),
          point = intersection.point;

      if (!floating) {
        return;
      }

      translate(floating, point.x, point.y);

    }

    function updateSegmentDraggerPosition(parentGfx, intersection, waypoints) {

      var draggerGfx = getSegmentDragger(intersection.index, parentGfx),
          segmentStart = waypoints[intersection.index - 1],
          segmentEnd = waypoints[intersection.index],
          point = intersection.point,
          mid = getMidPoint(segmentStart, segmentEnd),
          alignment = pointsAligned(segmentStart, segmentEnd),
          draggerVisual, relativePosition;

      if (!draggerGfx) {
        return;
      }

      draggerVisual = getDraggerVisual(draggerGfx);

      relativePosition = {
        x: point.x - mid.x,
        y: point.y - mid.y
      };

      if (alignment === 'v') {

        // rotate position
        relativePosition = {
          x: relativePosition.y,
          y: relativePosition.x
        };
      }

      translate(draggerVisual, relativePosition.x, relativePosition.y);
    }

    eventBus.on('connection.changed', function(event) {
      updateHandles(event.element);
    });

    eventBus.on('connection.remove', function(event) {
      var gfx = getBendpointsContainer(event.element);

      if (gfx) {
        remove$1(gfx);
      }
    });

    eventBus.on('element.marker.update', function(event) {

      var element = event.element,
          bendpointsGfx;

      if (!element.waypoints) {
        return;
      }

      bendpointsGfx = addHandles(element);

      if (event.add) {
        classes(bendpointsGfx).add(event.marker);
      } else {
        classes(bendpointsGfx).remove(event.marker);
      }
    });

    eventBus.on('element.mousemove', function(event) {

      var element = event.element,
          waypoints = element.waypoints,
          bendpointsGfx,
          intersection;

      if (waypoints) {
        bendpointsGfx = getBendpointsContainer(element, true);

        intersection = getConnectionIntersection(canvas, waypoints, event.originalEvent);

        if (!intersection) {
          return;
        }

        updateFloatingBendpointPosition(bendpointsGfx, intersection);

        if (!intersection.bendpoint) {
          updateSegmentDraggerPosition(bendpointsGfx, intersection, waypoints);
        }

      }
    });

    eventBus.on('element.mousedown', function(event) {

      if (!isPrimaryButton(event)) {
        return;
      }

      var originalEvent = event.originalEvent,
          element = event.element;

      if (!element.waypoints) {
        return;
      }

      return activateBendpointMove(originalEvent, element);
    });

    eventBus.on('selection.changed', function(event) {
      var newSelection = event.newSelection,
          primary = newSelection[0];

      if (primary && primary.waypoints) {
        addHandles(primary);
      }
    });

    eventBus.on('element.hover', function(event) {
      var element = event.element;

      if (element.waypoints) {
        addHandles(element);
        interactionEvents.registerEvent(event.gfx, 'mousemove', 'element.mousemove');
      }
    });

    eventBus.on('element.out', function(event) {
      interactionEvents.unregisterEvent(event.gfx, 'mousemove', 'element.mousemove');
    });

    // update bendpoint container data attribute on element ID change
    eventBus.on('element.updateId', function(context) {
      var element = context.element,
          newId = context.newId;

      if (element.waypoints) {
        var bendpointContainer = getBendpointsContainer(element);

        if (bendpointContainer) {
          attr(bendpointContainer, { 'data-element-id': newId });
        }
      }
    });

    // API

    this.addHandles = addHandles;
    this.updateHandles = updateHandles;
    this.getBendpointsContainer = getBendpointsContainer;
    this.getSegmentDragger = getSegmentDragger;
  }

  Bendpoints.$inject = [
    'eventBus',
    'canvas',
    'interactionEvents',
    'bendpointMove',
    'connectionSegmentMove'
  ];



  // helper /////////////

  function getDraggerVisual(draggerGfx) {
    return query('.djs-visual', draggerGfx);
  }

  /**
   * @typedef {import('../core/Types').ElementLike} Element
   * @typedef {import('../core/Types').ConnectionLike} Connection
   *
   * @typedef {import('../util/Types').DirectionTRBL} DirectionTRBL
   * @typedef {import('../util/Types').Point} Point
   * @typedef {import('../util/Types').Rect} Rect
   * @typedef {import('../util/Types').RectTRBL} RectTRBL
   */

  /**
   * @param {Rect} bounds
   *
   * @returns {Rect}
   */
  function roundBounds(bounds) {
    return {
      x: Math.round(bounds.x),
      y: Math.round(bounds.y),
      width: Math.round(bounds.width),
      height: Math.round(bounds.height)
    };
  }

  /**
   * @param {Point} point
   *
   * @returns {Point}
   */
  function roundPoint(point) {

    return {
      x: Math.round(point.x),
      y: Math.round(point.y)
    };
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


  /**
   * Convert a { top, left, bottom, right } to an objects bounds.
   *
   * @param {RectTRBL} trbl
   *
   * @return {Rect}
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
   * @param {Point|Rect} bounds
   *
   * @return {Point}
   */
  function getBoundsMid(bounds) {
    return roundPoint({
      x: bounds.x + (bounds.width || 0) / 2,
      y: bounds.y + (bounds.height || 0) / 2
    });
  }


  /**
   * Get the mid of the given Connection.
   *
   * @param {Connection} connection
   *
   * @return {Point}
   */
  function getConnectionMid(connection) {
    var waypoints = connection.waypoints;

    // calculate total length and length of each segment
    var parts = waypoints.reduce(function(parts, point, index) {

      var lastPoint = waypoints[index - 1];

      if (lastPoint) {
        var lastPart = parts[parts.length - 1];

        var startLength = lastPart && lastPart.endLength || 0;
        var length = distance(lastPoint, point);

        parts.push({
          start: lastPoint,
          end: point,
          startLength: startLength,
          endLength: startLength + length,
          length: length
        });
      }

      return parts;
    }, []);

    var totalLength = parts.reduce(function(length, part) {
      return length + part.length;
    }, 0);

    // find which segement contains middle point
    var midLength = totalLength / 2;

    var i = 0;
    var midSegment = parts[i];

    while (midSegment.endLength < midLength) {
      midSegment = parts[++i];
    }

    // calculate relative position on mid segment
    var segmentProgress = (midLength - midSegment.startLength) / midSegment.length;

    var midPoint = {
      x: midSegment.start.x + (midSegment.end.x - midSegment.start.x) * segmentProgress,
      y: midSegment.start.y + (midSegment.end.y - midSegment.start.y) * segmentProgress
    };

    return midPoint;
  }


  /**
   * Get the mid of the given Element.
   *
   * @param {Element} element
   *
   * @return {Point}
   */
  function getMid(element) {
    if (isConnection(element)) {
      return getConnectionMid(element);
    }

    return getBoundsMid(element);
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


  // intersection utils //////////////////////

  /**
   * Get intersection between an element and a line path.
   *
   * @param {string} elementPath
   * @param {string} linePath
   * @param {boolean} cropStart Whether to crop start or end.
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
    return intersectPaths(a, b);
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

  // helpers //////////////////////

  function distance(a, b) {
    return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
  }

  /**
   * @typedef {import('didi').Injector} Injector
   *
   * @typedef {import('../../core/Canvas').default} Canvas
   * @typedef {import('../dragging/Dragging').default} Dragging
   * @typedef {import('../../core/EventBus').default} EventBus
   * @typedef {import('../modeling/Modeling').default} Modeling
   * @typedef {import('../rules/Rules').default} Rules
   */

  var round$5 = Math.round;

  var RECONNECT_START$1 = 'reconnectStart',
      RECONNECT_END$1 = 'reconnectEnd',
      UPDATE_WAYPOINTS$1 = 'updateWaypoints';


  /**
   * Move bendpoints through drag and drop to add/remove bendpoints or reconnect connection.
   *
   * @param {Injector} injector
   * @param {EventBus} eventBus
   * @param {Canvas} canvas
   * @param {Dragging} dragging
   * @param {Rules} rules
   * @param {Modeling} modeling
   */
  function BendpointMove(injector, eventBus, canvas, dragging, rules, modeling) {
    this._injector = injector;

    this.start = function(event, connection, bendpointIndex, insert) {
      var gfx = canvas.getGraphics(connection),
          source = connection.source,
          target = connection.target,
          waypoints = connection.waypoints,
          type;

      if (!insert && bendpointIndex === 0) {
        type = RECONNECT_START$1;
      } else
      if (!insert && bendpointIndex === waypoints.length - 1) {
        type = RECONNECT_END$1;
      } else {
        type = UPDATE_WAYPOINTS$1;
      }

      var command = type === UPDATE_WAYPOINTS$1 ? 'connection.updateWaypoints' : 'connection.reconnect';

      var allowed = rules.allowed(command, {
        connection: connection,
        source: source,
        target: target
      });

      if (allowed === false) {
        allowed = rules.allowed(command, {
          connection: connection,
          source: target,
          target: source
        });
      }

      if (allowed === false) {
        return;
      }

      dragging.init(event, 'bendpoint.move', {
        data: {
          connection: connection,
          connectionGfx: gfx,
          context: {
            allowed: allowed,
            bendpointIndex: bendpointIndex,
            connection: connection,
            source: source,
            target: target,
            insert: insert,
            type: type
          }
        }
      });
    };

    eventBus.on('bendpoint.move.hover', function(event) {
      var context = event.context,
          connection = context.connection,
          source = connection.source,
          target = connection.target,
          hover = event.hover,
          type = context.type;

      // cache hover state
      context.hover = hover;

      var allowed;

      if (!hover) {
        return;
      }

      var command = type === UPDATE_WAYPOINTS$1 ? 'connection.updateWaypoints' : 'connection.reconnect';

      allowed = context.allowed = rules.allowed(command, {
        connection: connection,
        source: type === RECONNECT_START$1 ? hover : source,
        target: type === RECONNECT_END$1 ? hover : target
      });

      if (allowed) {
        context.source = type === RECONNECT_START$1 ? hover : source;
        context.target = type === RECONNECT_END$1 ? hover : target;

        return;
      }

      if (allowed === false) {
        allowed = context.allowed = rules.allowed(command, {
          connection: connection,
          source: type === RECONNECT_END$1 ? hover : target,
          target: type === RECONNECT_START$1 ? hover : source
        });
      }

      if (allowed) {
        context.source = type === RECONNECT_END$1 ? hover : target;
        context.target = type === RECONNECT_START$1 ? hover : source;
      }
    });

    eventBus.on([ 'bendpoint.move.out', 'bendpoint.move.cleanup' ], function(event) {
      var context = event.context,
          type = context.type;

      context.hover = null;
      context.source = null;
      context.target = null;

      if (type !== UPDATE_WAYPOINTS$1) {
        context.allowed = false;
      }
    });

    eventBus.on('bendpoint.move.end', function(event) {
      var context = event.context,
          allowed = context.allowed,
          bendpointIndex = context.bendpointIndex,
          connection = context.connection,
          insert = context.insert,
          newWaypoints = connection.waypoints.slice(),
          source = context.source,
          target = context.target,
          type = context.type,
          hints = context.hints || {};

      // ensure integer values (important if zoom level was > 1 during move)
      var docking = {
        x: round$5(event.x),
        y: round$5(event.y)
      };

      if (!allowed) {
        return false;
      }

      if (type === UPDATE_WAYPOINTS$1) {
        if (insert) {

          // insert new bendpoint
          newWaypoints.splice(bendpointIndex, 0, docking);
        } else {

          // swap previous waypoint with moved one
          newWaypoints[bendpointIndex] = docking;
        }

        // pass hints about actual moved bendpoint
        // useful for connection/label layout
        hints.bendpointMove = {
          insert: insert,
          bendpointIndex: bendpointIndex
        };

        newWaypoints = this.cropWaypoints(connection, newWaypoints);

        modeling.updateWaypoints(connection, filterRedundantWaypoints(newWaypoints), hints);
      } else {
        if (type === RECONNECT_START$1) {
          hints.docking = 'source';

          if (isReverse(context)) {
            hints.docking = 'target';

            hints.newWaypoints = newWaypoints.reverse();
          }
        } else if (type === RECONNECT_END$1) {
          hints.docking = 'target';

          if (isReverse(context)) {
            hints.docking = 'source';

            hints.newWaypoints = newWaypoints.reverse();
          }
        }

        modeling.reconnect(connection, source, target, docking, hints);
      }
    }, this);
  }

  BendpointMove.$inject = [
    'injector',
    'eventBus',
    'canvas',
    'dragging',
    'rules',
    'modeling'
  ];

  BendpointMove.prototype.cropWaypoints = function(connection, newWaypoints) {
    var connectionDocking = this._injector.get('connectionDocking', false);

    if (!connectionDocking) {
      return newWaypoints;
    }

    var waypoints = connection.waypoints;

    connection.waypoints = newWaypoints;

    connection.waypoints = connectionDocking.getCroppedWaypoints(connection);

    newWaypoints = connection.waypoints;

    connection.waypoints = waypoints;

    return newWaypoints;
  };


  // helpers //////////

  function isReverse(context) {
    var hover = context.hover,
        source = context.source,
        target = context.target,
        type = context.type;

    if (type === RECONNECT_START$1) {
      return hover && target && hover === target && source !== target;
    }

    if (type === RECONNECT_END$1) {
      return hover && source && hover === source && source !== target;
    }
  }

  /**
   * @typedef {import('didi').Injector} Injector
   *
   * @typedef {import('../bendpoints/BendpointMove').default} BendpointMove
   * @typedef {import('../../core/Canvas').default} Canvas
   * @typedef {import('../../core/EventBus').default} EventBus
   */

  var RECONNECT_START = 'reconnectStart',
      RECONNECT_END = 'reconnectEnd',
      UPDATE_WAYPOINTS = 'updateWaypoints';

  var MARKER_OK$2 = 'connect-ok',
      MARKER_NOT_OK$2 = 'connect-not-ok',
      MARKER_CONNECT_HOVER$1 = 'connect-hover',
      MARKER_CONNECT_UPDATING$1 = 'djs-updating',
      MARKER_ELEMENT_HIDDEN = 'djs-element-hidden';

  var HIGH_PRIORITY$2 = 1100;

  /**
   * Preview connection while moving bendpoints.
   *
   * @param {BendpointMove} bendpointMove
   * @param {Injector} injector
   * @param {EventBus} eventBus
   * @param {Canvas} canvas
   */
  function BendpointMovePreview(bendpointMove, injector, eventBus, canvas) {
    this._injector = injector;

    var connectionPreview = injector.get('connectionPreview', false);

    eventBus.on('bendpoint.move.start', function(event) {
      var context = event.context,
          bendpointIndex = context.bendpointIndex,
          connection = context.connection,
          insert = context.insert,
          waypoints = connection.waypoints,
          newWaypoints = waypoints.slice();

      context.waypoints = waypoints;

      if (insert) {

        // insert placeholder for new bendpoint
        newWaypoints.splice(bendpointIndex, 0, { x: event.x, y: event.y });
      }

      connection.waypoints = newWaypoints;

      // add dragger gfx
      var draggerGfx = context.draggerGfx = addBendpoint(canvas.getLayer('overlays'));

      classes(draggerGfx).add('djs-dragging');

      canvas.addMarker(connection, MARKER_ELEMENT_HIDDEN);
      canvas.addMarker(connection, MARKER_CONNECT_UPDATING$1);
    });

    eventBus.on('bendpoint.move.hover', function(event) {
      var context = event.context,
          allowed = context.allowed,
          hover = context.hover,
          type = context.type;

      if (hover) {
        canvas.addMarker(hover, MARKER_CONNECT_HOVER$1);

        if (type === UPDATE_WAYPOINTS) {
          return;
        }

        if (allowed) {
          canvas.removeMarker(hover, MARKER_NOT_OK$2);
          canvas.addMarker(hover, MARKER_OK$2);
        } else if (allowed === false) {
          canvas.removeMarker(hover, MARKER_OK$2);
          canvas.addMarker(hover, MARKER_NOT_OK$2);
        }
      }
    });

    eventBus.on([
      'bendpoint.move.out',
      'bendpoint.move.cleanup'
    ], HIGH_PRIORITY$2, function(event) {
      var context = event.context,
          hover = context.hover,
          target = context.target;

      if (hover) {
        canvas.removeMarker(hover, MARKER_CONNECT_HOVER$1);
        canvas.removeMarker(hover, target ? MARKER_OK$2 : MARKER_NOT_OK$2);
      }
    });

    eventBus.on('bendpoint.move.move', function(event) {
      var context = event.context,
          allowed = context.allowed,
          bendpointIndex = context.bendpointIndex,
          draggerGfx = context.draggerGfx,
          hover = context.hover,
          type = context.type,
          connection = context.connection,
          source = connection.source,
          target = connection.target,
          newWaypoints = connection.waypoints.slice(),
          bendpoint = { x: event.x, y: event.y },
          hints = context.hints || {},
          drawPreviewHints = {};

      if (connectionPreview) {
        if (hints.connectionStart) {
          drawPreviewHints.connectionStart = hints.connectionStart;
        }

        if (hints.connectionEnd) {
          drawPreviewHints.connectionEnd = hints.connectionEnd;
        }


        if (type === RECONNECT_START) {
          if (isReverse(context)) {
            drawPreviewHints.connectionEnd = drawPreviewHints.connectionEnd || bendpoint;

            drawPreviewHints.source = target;
            drawPreviewHints.target = hover || source;

            newWaypoints = newWaypoints.reverse();
          } else {
            drawPreviewHints.connectionStart = drawPreviewHints.connectionStart || bendpoint;

            drawPreviewHints.source = hover || source;
            drawPreviewHints.target = target;
          }
        } else if (type === RECONNECT_END) {
          if (isReverse(context)) {
            drawPreviewHints.connectionStart = drawPreviewHints.connectionStart || bendpoint;

            drawPreviewHints.source = hover || target;
            drawPreviewHints.target = source;

            newWaypoints = newWaypoints.reverse();
          } else {
            drawPreviewHints.connectionEnd = drawPreviewHints.connectionEnd || bendpoint;

            drawPreviewHints.source = source;
            drawPreviewHints.target = hover || target;
          }

        } else {
          drawPreviewHints.noCropping = true;
          drawPreviewHints.noLayout = true;
          newWaypoints[ bendpointIndex ] = bendpoint;
        }

        if (type === UPDATE_WAYPOINTS) {
          newWaypoints = bendpointMove.cropWaypoints(connection, newWaypoints);
        }

        drawPreviewHints.waypoints = newWaypoints;

        connectionPreview.drawPreview(context, allowed, drawPreviewHints);
      }

      translate(draggerGfx, event.x, event.y);
    }, this);

    eventBus.on([
      'bendpoint.move.end',
      'bendpoint.move.cancel'
    ], HIGH_PRIORITY$2, function(event) {
      var context = event.context,
          connection = context.connection,
          draggerGfx = context.draggerGfx,
          hover = context.hover,
          target = context.target,
          waypoints = context.waypoints;

      connection.waypoints = waypoints;

      // remove dragger gfx
      remove$1(draggerGfx);

      canvas.removeMarker(connection, MARKER_CONNECT_UPDATING$1);
      canvas.removeMarker(connection, MARKER_ELEMENT_HIDDEN);

      if (hover) {
        canvas.removeMarker(hover, MARKER_OK$2);
        canvas.removeMarker(hover, target ? MARKER_OK$2 : MARKER_NOT_OK$2);
      }

      if (connectionPreview) {
        connectionPreview.cleanUp(context);
      }
    });
  }

  BendpointMovePreview.$inject = [
    'bendpointMove',
    'injector',
    'eventBus',
    'canvas'
  ];

  var MARKER_CONNECT_HOVER = 'connect-hover',
      MARKER_CONNECT_UPDATING = 'djs-updating';

  /**
   * @typedef {import('../../model/Types').Shape} Shape
   *
   * @typedef {import('../../util/Types').Axis} Axis
   * @typedef {import('../../util/Types').Point} Point
   *
   * @typedef {import('didi').Injector} Injector
   *
   * @typedef {import('../../core/Canvas').default} Canvas
   * @typedef {import('../dragging/Dragging').default} Dragging
   * @typedef {import('../../core/EventBus').default} EventBus
   * @typedef {import('../../core/GraphicsFactory').default} GraphicsFactory
   * @typedef {import('../modeling/Modeling').default} Modeling
   */

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
   * @param {Point} point
   * @param {Shape} referenceElement
   * @param {Axis} moveAxis
   *
   * @return {Point}
   */
  function getDocking$1(point, referenceElement, moveAxis) {

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
   * A component that implements moving of bendpoints.
   *
   * @param {Injector} injector
   * @param {EventBus} eventBus
   * @param {Canvas} canvas
   * @param {Canvas} dragging
   * @param {GraphicsFactory} graphicsFactory
   * @param {Modeling} modeling
   */
  function ConnectionSegmentMove(
      injector, eventBus, canvas,
      dragging, graphicsFactory, modeling) {

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
          intersection = getConnectionIntersection(canvas, waypoints, event),
          direction, axis, dragPosition;

      direction = pointsAligned(segmentStart, segmentEnd);

      // do not move diagonal connection
      if (!direction) {
        return;
      }

      // the axis where we are going to move things
      axis = direction === 'v' ? 'x' : 'y';

      if (segmentStartIndex === 0) {
        segmentStart = getDocking$1(segmentStart, connection.source, axis);
      }

      if (segmentEndIndex === waypoints.length - 1) {
        segmentEnd = getDocking$1(segmentEnd, connection.target, axis);
      }

      if (intersection) {
        dragPosition = intersection.point;
      } else {

        // set to segment center as default
        dragPosition = {
          x: (segmentStart.x + segmentEnd.x) / 2,
          y: (segmentStart.y + segmentEnd.y) / 2
        };
      }

      context = {
        connection: connection,
        segmentStartIndex: segmentStartIndex,
        segmentEndIndex: segmentEndIndex,
        segmentStart: segmentStart,
        segmentEnd: segmentEnd,
        axis: axis,
        dragPosition: dragPosition
      };

      dragging.init(event, dragPosition, 'connectionSegment.move', {
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
     * @param {Point[]} newWaypoints
     *
     * @return {Point[]} cropped connection waypoints
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
     * @param {Point[]} waypoints
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

    eventBus.on('connectionSegment.move.start', function(event) {

      var context = event.context,
          connection = event.connection,
          layer = canvas.getLayer('overlays');

      context.originalWaypoints = connection.waypoints.slice();

      // add dragger gfx
      context.draggerGfx = addSegmentDragger(layer, context.segmentStart, context.segmentEnd);
      classes(context.draggerGfx).add('djs-dragging');

      canvas.addMarker(connection, MARKER_CONNECT_UPDATING);
    });

    eventBus.on('connectionSegment.move.move', function(event) {

      var context = event.context,
          connection = context.connection,
          segmentStartIndex = context.segmentStartIndex,
          segmentEndIndex = context.segmentEndIndex,
          segmentStart = context.segmentStart,
          segmentEnd = context.segmentEnd,
          axis = context.axis;

      var newWaypoints = context.originalWaypoints.slice(),
          newSegmentStart = axisAdd(segmentStart, axis, event['d' + axis]),
          newSegmentEnd = axisAdd(segmentEnd, axis, event['d' + axis]);

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
      updateDragger(context, segmentOffset, event);

      // save segmentOffset in context
      context.newSegmentStartIndex = segmentStartIndex + segmentOffset;

      // redraw connection
      redrawConnection(event);
    });

    eventBus.on('connectionSegment.move.hover', function(event) {

      event.context.hover = event.hover;
      canvas.addMarker(event.hover, MARKER_CONNECT_HOVER);
    });

    eventBus.on([
      'connectionSegment.move.out',
      'connectionSegment.move.cleanup'
    ], function(event) {

      // remove connect marker
      // if it was added
      var hover = event.context.hover;

      if (hover) {
        canvas.removeMarker(hover, MARKER_CONNECT_HOVER);
      }
    });

    eventBus.on('connectionSegment.move.cleanup', function(event) {

      var context = event.context,
          connection = context.connection;

      // remove dragger gfx
      if (context.draggerGfx) {
        remove$1(context.draggerGfx);
      }

      canvas.removeMarker(connection, MARKER_CONNECT_UPDATING);
    });

    eventBus.on([
      'connectionSegment.move.cancel',
      'connectionSegment.move.end'
    ], function(event) {
      var context = event.context,
          connection = context.connection;

      connection.waypoints = context.originalWaypoints;

      redrawConnection(event);
    });

    eventBus.on('connectionSegment.move.end', function(event) {

      var context = event.context,
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
    'modeling'
  ];

  /**
   * @typedef {import('../../core/Types').ConnectionLike} Connection
   * @typedef {import('../../core/Types').ShapeLike} Shape
   *
   * @typedef {import('../../core/EventBus').Event} Event
   *
   * @typedef {import('../../util/Types').Axis} Axis
   */



  /**
   * Set the given event as snapped.
   *
   * This method may change the x and/or y position of the shape
   * from the given event!
   *
   * @param {Event} event
   * @param {Axis} axis
   * @param {number|boolean} value
   *
   * @return {number} old value
   */
  function setSnapped(event, axis, value) {
    if (typeof axis !== 'string') {
      throw new Error('axis must be in [x, y]');
    }

    if (typeof value !== 'number' && value !== false) {
      throw new Error('value must be Number or false');
    }

    var delta,
        previousValue = event[axis];

    var snapped = event.snapped = (event.snapped || {});


    if (value === false) {
      snapped[axis] = false;
    } else {
      snapped[axis] = true;

      delta = value - previousValue;

      event[axis] += delta;
      event['d' + axis] += delta;
    }

    return previousValue;
  }

  /**
   * @typedef {import('../../core/EventBus').default} EventBus
   */
  var abs = Math.abs,
      round$4 = Math.round;

  var TOLERANCE = 10;

  /**
   * @param {EventBus} eventBus
   */
  function BendpointSnapping(eventBus) {

    function snapTo(values, value) {

      if (isArray$3(values)) {
        var i = values.length;

        while (i--) if (abs(values[i] - value) <= TOLERANCE) {
          return values[i];
        }
      } else {
        values = +values;
        var rem = value % values;

        if (rem < TOLERANCE) {
          return value - rem;
        }

        if (rem > values - TOLERANCE) {
          return value - rem + values;
        }
      }

      return value;
    }

    function getSnapPoint(element, event) {

      if (element.waypoints) {
        return getClosestPointOnConnection(event, element);
      }

      if (element.width) {
        return {
          x: round$4(element.width / 2 + element.x),
          y: round$4(element.height / 2 + element.y)
        };
      }
    }

    // connection segment snapping //////////////////////

    function getConnectionSegmentSnaps(event) {

      var context = event.context,
          snapPoints = context.snapPoints,
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
        referenceWaypoints.unshift(getSnapPoint(connection.source, event));
      }

      if (segmentEndIndex > waypoints.length - 3) {
        referenceWaypoints.unshift(getSnapPoint(connection.target, event));
      }

      context.snapPoints = snapPoints = { horizontal: [] , vertical: [] };

      forEach$1(referenceWaypoints, function(p) {

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
      var snapPoints = getConnectionSegmentSnaps(event),
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
      assign$1(event, {
        dx: event.dx - cx,
        dy: event.dy - cy,
        x: sx,
        y: sy
      });

      // only set snapped if actually snapped
      if (cx || snapPoints.vertical.indexOf(x) !== -1) {
        setSnapped(event, 'x', sx);
      }

      if (cy || snapPoints.horizontal.indexOf(y) !== -1) {
        setSnapped(event, 'y', sy);
      }
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

      forEach$1(referenceWaypoints, function(p) {

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

    // Snap Endpoint of new connection
    eventBus.on([
      'connect.hover',
      'connect.move',
      'connect.end'
    ], 1500, function(event) {
      var context = event.context,
          hover = context.hover,
          hoverMid = hover && getSnapPoint(hover, event);

      // only snap on connections, elements can have multiple connect endpoints
      if (!isConnection(hover) || !hoverMid || !hoverMid.x || !hoverMid.y) {
        return;
      }

      setSnapped(event, 'x', hoverMid.x);
      setSnapped(event, 'y', hoverMid.y);
    });

    eventBus.on([ 'bendpoint.move.move', 'bendpoint.move.end' ], 1500, function(event) {

      var context = event.context,
          snapPoints = getBendpointSnaps(context),
          hover = context.hover,
          hoverMid = hover && getSnapPoint(hover, event),
          x = event.x,
          y = event.y,
          sx, sy;

      if (!snapPoints) {
        return;
      }

      // snap to hover mid
      sx = snapTo(hoverMid ? snapPoints.vertical.concat([ hoverMid.x ]) : snapPoints.vertical, x);
      sy = snapTo(hoverMid ? snapPoints.horizontal.concat([ hoverMid.y ]) : snapPoints.horizontal, y);

      // correction x/y
      var cx = (x - sx),
          cy = (y - sy);

      // update delta
      assign$1(event, {
        dx: event.dx - cx,
        dy: event.dy - cy,
        x: event.x - cx,
        y: event.y - cy
      });

      // only set snapped if actually snapped
      if (cx || snapPoints.vertical.indexOf(x) !== -1) {
        setSnapped(event, 'x', sx);
      }

      if (cy || snapPoints.horizontal.indexOf(y) !== -1) {
        setSnapped(event, 'y', sy);
      }
    });
  }


  BendpointSnapping.$inject = [ 'eventBus' ];

  /**
   * @type { import('didi').ModuleDeclaration }
   */
  var BendpointsModule = {
    __depends__: [
      DraggingModule,
      RulesModule
    ],
    __init__: [ 'bendpoints', 'bendpointSnapping', 'bendpointMovePreview' ],
    bendpoints: [ 'type', Bendpoints ],
    bendpointMove: [ 'type', BendpointMove ],
    bendpointMovePreview: [ 'type', BendpointMovePreview ],
    connectionSegmentMove: [ 'type', ConnectionSegmentMove ],
    bendpointSnapping: [ 'type', BendpointSnapping ]
  };

  function e(e,t){t&&(e.super_=t,e.prototype=Object.create(t.prototype,{constructor:{value:e,enumerable:!1,writable:!0,configurable:!0}}));}

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
   * Fail save get the index of an element in a collection.
   *
   * @param {Array<Object>} collection
   * @param {Object} element
   *
   * @return {number} the index or -1 if collection or element do
   *                  not exist or the element is not contained.
   */
  function indexOf(collection, element) {

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
   * @param {Object[]} [collection]
   * @param {(element: Object) => void} removeFn
   *
   * @return {Object[]} the cleared collection
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

  /**
   * @typedef {import('../core/Types').ElementLike} ElementLike
   * @typedef {import('../core/EventBus').default} EventBus
   * @typedef {import('./CommandStack').CommandContext} CommandContext
   *
   * @typedef {string|string[]} Events
   * @typedef { (context: CommandContext) => ElementLike[] | void } HandlerFunction
   * @typedef { (context: CommandContext) => void } ComposeHandlerFunction
   */

  var DEFAULT_PRIORITY$1 = 1000;

  /**
   * A utility that can be used to plug into the command execution for
   * extension and/or validation.
   *
   * @class
   * @constructor
   *
   * @example
   *
   * ```javascript
   * import CommandInterceptor from 'diagram-js/lib/command/CommandInterceptor';
   *
   * class CommandLogger extends CommandInterceptor {
   *   constructor(eventBus) {
   *     super(eventBus);
   *
   *   this.preExecute('shape.create', (event) => {
   *     console.log('commandStack.shape-create.preExecute', event);
   *   });
   * }
   * ```
   *
   * @param {EventBus} eventBus
   */
  function CommandInterceptor(eventBus) {

    /**
     * @type {EventBus}
     */
    this._eventBus = eventBus;
  }

  CommandInterceptor.$inject = [ 'eventBus' ];

  function unwrapEvent(fn, that) {
    return function(event) {
      return fn.call(that || null, event.context, event.command, event);
    };
  }


  /**
   * Intercept a command during one of the phases.
   *
   * @param {Events} [events] command(s) to intercept
   * @param {string} [hook] phase to intercept
   * @param {number} [priority]
   * @param {ComposeHandlerFunction|HandlerFunction} handlerFn
   * @param {boolean} [unwrap] whether the event should be unwrapped
   * @param {any} [that]
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
      priority = DEFAULT_PRIORITY$1;
    }

    if (isObject(unwrap)) {
      that = unwrap;
      unwrap = false;
    }

    if (!isFunction(handlerFn)) {
      throw new Error('handlerFn must be a function');
    }

    if (!isArray$3(events)) {
      events = [ events ];
    }

    var eventBus = this._eventBus;

    forEach$1(events, function(event) {

      // concat commandStack(.event)?(.hook)?
      var fullEvent = [ 'commandStack', event, hook ].filter(function(e) { return e; }).join('.');

      eventBus.on(fullEvent, priority, unwrap ? unwrapEvent(handlerFn, that) : handlerFn, that);
    });
  };

  /**
   * Add a <canExecute> phase of command interceptor.
   *
   * @param {Events} [events] command(s) to intercept
   * @param {number} [priority]
   * @param {ComposeHandlerFunction|HandlerFunction} handlerFn
   * @param {boolean} [unwrap] whether the event should be unwrapped
   * @param {any} [that]
   */
  CommandInterceptor.prototype.canExecute = createHook('canExecute');

  /**
   * Add a <preExecute> phase of command interceptor.
   *
   * @param {Events} [events] command(s) to intercept
   * @param {number} [priority]
   * @param {ComposeHandlerFunction|HandlerFunction} handlerFn
   * @param {boolean} [unwrap] whether the event should be unwrapped
   * @param {any} [that]
   */
  CommandInterceptor.prototype.preExecute = createHook('preExecute');

  /**
   * Add a <preExecuted> phase of command interceptor.
   *
   * @param {Events} [events] command(s) to intercept
   * @param {number} [priority]
   * @param {ComposeHandlerFunction|HandlerFunction} handlerFn
   * @param {boolean} [unwrap] whether the event should be unwrapped
   * @param {any} [that]
   */
  CommandInterceptor.prototype.preExecuted = createHook('preExecuted');

  /**
   * Add a <execute> phase of command interceptor.
   *
   * @param {Events} [events] command(s) to intercept
   * @param {number} [priority]
   * @param {ComposeHandlerFunction|HandlerFunction} handlerFn
   * @param {boolean} [unwrap] whether the event should be unwrapped
   * @param {any} [that]
   */
  CommandInterceptor.prototype.execute = createHook('execute');

  /**
   * Add a <executed> phase of command interceptor.
   *
   * @param {Events} [events] command(s) to intercept
   * @param {number} [priority]
   * @param {ComposeHandlerFunction|HandlerFunction} handlerFn
   * @param {boolean} [unwrap] whether the event should be unwrapped
   * @param {any} [that]
   */
  CommandInterceptor.prototype.executed = createHook('executed');

  /**
   * Add a <postExecute> phase of command interceptor.
   *
   * @param {Events} [events] command(s) to intercept
   * @param {number} [priority]
   * @param {ComposeHandlerFunction|HandlerFunction} handlerFn
   * @param {boolean} [unwrap] whether the event should be unwrapped
   * @param {any} [that]
   */
  CommandInterceptor.prototype.postExecute = createHook('postExecute');

  /**
   * Add a <postExecuted> phase of command interceptor.
   *
   * @param {Events} [events] command(s) to intercept
   * @param {number} [priority]
   * @param {ComposeHandlerFunction|HandlerFunction} handlerFn
   * @param {boolean} [unwrap] whether the event should be unwrapped
   * @param {any} [that]
   */
  CommandInterceptor.prototype.postExecuted = createHook('postExecuted');

  /**
   * Add a <revert> phase of command interceptor.
   *
   * @param {Events} [events] command(s) to intercept
   * @param {number} [priority]
   * @param {ComposeHandlerFunction|HandlerFunction} handlerFn
   * @param {boolean} [unwrap] whether the event should be unwrapped
   * @param {any} [that]
   */
  CommandInterceptor.prototype.revert = createHook('revert');

  /**
   * Add a <reverted> phase of command interceptor.
   *
   * @param {Events} [events] command(s) to intercept
   * @param {number} [priority]
   * @param {ComposeHandlerFunction|HandlerFunction} handlerFn
   * @param {boolean} [unwrap] whether the event should be unwrapped
   * @param {any} [that]
   */
  CommandInterceptor.prototype.reverted = createHook('reverted');

  /*
   * Add prototype methods for each phase of command execution (e.g. execute,
   * revert).
   *
   * @param {string} hook
   *
   * @return { (
   *   events?: Events,
   *   priority?: number,
   *   handlerFn: ComposeHandlerFunction|HandlerFunction,
   *   unwrap?: boolean
   * ) => any }
   */
  function createHook(hook) {

    /**
     * @this {CommandInterceptor}
     *
     * @param {Events} [events]
     * @param {number} [priority]
     * @param {ComposeHandlerFunction|HandlerFunction} handlerFn
     * @param {boolean} [unwrap]
     * @param {any} [that]
     */
    const hookFn = function(events, priority, handlerFn, unwrap, that) {

      if (isFunction(events) || isNumber(events)) {
        that = unwrap;
        unwrap = handlerFn;
        handlerFn = priority;
        priority = events;
        events = null;
      }

      this.on(events, hook, priority, handlerFn, unwrap, that);
    };

    return hookFn;
  }

  var LOW_PRIORITY$1 = 250,
      HIGH_PRIORITY$1 = 1400;

  /**
   * @typedef {import('../../model/Types').Element} Element
   *
   * @typedef {import('didi').Injector} Injector
   *
   * @typedef {import('../../core/EventBus').default} EventBus
   * @typedef {import('../modeling/Modeling').default} Modeling
   */

  /**
   * A handler that makes sure labels are properly moved with
   * their label targets.
   *
   * @param {Injector} injector
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
    movePreview && eventBus.on('shape.move.start', LOW_PRIORITY$1, function(e) {

      var context = e.context,
          shapes = context.shapes;

      var labels = [];

      forEach$1(shapes, function(element) {

        forEach$1(element.labels, function(label) {

          if (!label.hidden && context.shapes.indexOf(label) === -1) {
            labels.push(label);
          }

          if (element.labelTarget) {
            labels.push(element);
          }
        });
      });

      forEach$1(labels, function(label) {
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
      forEach$1(enclosedElements, function(element) {
        forEach$1(element.labels, function(label) {

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
        context.labelTargetIndex = indexOf(labelTarget.labels, shape);
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
        add(labelTarget.labels, shape, labelTargetIndex);

        shape.labelTarget = labelTarget;
      }
    });

  }

  e(LabelSupport, CommandInterceptor);

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
   * @param {Element[]} elements
   *
   * @return {Element[]} filtered
   */
  function removeLabels(elements) {

    return filter(elements, function(element) {

      // filter out labels that are move together
      // with their label targets
      return elements.indexOf(element.labelTarget) === -1;
    });
  }

  /**
   * @type { import('didi').ModuleDeclaration }
   */
  var LabelSupportModule = {
    __init__: [ 'labelSupport' ],
    labelSupport: [ 'type', LabelSupport ]
  };

  /**
   * @typedef {import('../../core/Canvas').default} Canvas
   * @typedef {import('../../core/ElementRegistry').default} ElementRegistry
   * @typedef {import('../../core/EventBus').default} EventBus
   * @typedef {import('../../core/GraphicsFactory').default} GraphicsFactory
   */

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

  /**
   * @type { import('didi').ModuleDeclaration }
   */
  var ChangeSupportModule = {
    __init__: [ 'changeSupport' ],
    changeSupport: [ 'type', ChangeSupport ]
  };

  var max$1 = Math.max,
      min$1 = Math.min;

  var DEFAULT_CHILD_BOX_PADDING = 20;

  /**
   * Resize the given bounds by the specified delta from a given anchor point.
   *
   * @param {Rect} bounds the bounding box that should be resized
   * @param {Direction} direction in which the element is resized (nw, ne, se, sw)
   * @param {Point} delta of the resize operation
   *
   * @return {Rect} resized bounding box
   */
  function resizeBounds$1(bounds, direction, delta) {
    var dx = delta.x,
        dy = delta.y;

    var newBounds = {
      x: bounds.x,
      y: bounds.y,
      width: bounds.width,
      height: bounds.height
    };

    if (direction.indexOf('n') !== -1) {
      newBounds.y = bounds.y + dy;
      newBounds.height = bounds.height - dy;
    } else if (direction.indexOf('s') !== -1) {
      newBounds.height = bounds.height + dy;
    }

    if (direction.indexOf('e') !== -1) {
      newBounds.width = bounds.width + dx;
    } else if (direction.indexOf('w') !== -1) {
      newBounds.x = bounds.x + dx;
      newBounds.width = bounds.width - dx;
    }

    return newBounds;
  }


  function applyConstraints(attr, trbl, resizeConstraints) {

    var value = trbl[attr],
        minValue = resizeConstraints.min && resizeConstraints.min[attr],
        maxValue = resizeConstraints.max && resizeConstraints.max[attr];

    if (isNumber(minValue)) {
      value = (/top|left/.test(attr) ? min$1 : max$1)(value, minValue);
    }

    if (isNumber(maxValue)) {
      value = (/top|left/.test(attr) ? max$1 : min$1)(value, maxValue);
    }

    return value;
  }

  function ensureConstraints$1(currentBounds, resizeConstraints) {

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
      top: min$1(minBox.top, childrenBox.top),
      left: min$1(minBox.left, childrenBox.left),
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
   * @param {Element} element
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
   * @param {Shape|Shape[]} shapeOrChildren
   * @param {RectTRBL|number} padding
   *
   * @return {Rect}
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

  /**
   * @typedef {import('../../core/Types').ShapeLike} Shape
   *
   * @typedef {import('../../util/Types').Direction} Direction
   * @typedef {import('../../util/Types').Point} Point
   *
   * @typedef {import('../../core/EventBus').default} EventBus
   * @typedef {import('../dragging/Dragging').default} Dragging
   * @typedef {import('../modeling/Modeling').default} Modeling
   * @typedef {import('../rules/Rules').default} Rules
   */

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
   *
   * @param {EventBus} eventBus
   * @param {Rules} rules
   * @param {Modeling} modeling
   * @param {Dragging} dragging
   */
  function Resize(eventBus, rules, modeling, dragging) {

    this._dragging = dragging;
    this._rules = rules;

    var self = this;


    /**
     * Handle resize move by specified delta.
     *
     * @param {Object} context
     * @param {Point} delta
     */
    function handleMove(context, delta) {

      var shape = context.shape,
          direction = context.direction,
          resizeConstraints = context.resizeConstraints,
          newBounds;

      context.delta = delta;

      newBounds = resizeBounds$1(shape, direction, delta);

      // ensure constraints during resize
      context.newBounds = ensureConstraints$1(newBounds, resizeConstraints);

      // update + cache executable state
      context.canExecute = self.canResize(context);
    }

    /**
     * Handle resize start.
     *
     * @param {Object} context
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
     * @param {Object} context
     */
    function handleEnd(context) {
      var shape = context.shape,
          canExecute = context.canExecute,
          newBounds = context.newBounds;

      if (canExecute) {

        // ensure we have actual pixel values for new bounds
        // (important when zoom level was > 1 during move)
        newBounds = roundBounds(newBounds);

        if (!boundsChanged(shape, newBounds)) {

          // no resize necessary
          return;
        }

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
   * @param {MouseEvent|TouchEvent} event
   * @param {Shape} shape
   * @param {Object|Direction} contextOrDirection
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

    context = assign$1({ shape: shape }, contextOrDirection);

    direction = context.direction;

    if (!direction) {
      throw new Error('must provide a direction (n|w|s|e|nw|se|ne|sw)');
    }

    dragging.init(event, getReferencePoint(shape, direction), 'resize', {
      autoActivate: true,
      cursor: getCursor(direction),
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

  // helpers //////////

  function boundsChanged(shape, newBounds) {
    return shape.x !== newBounds.x ||
      shape.y !== newBounds.y ||
      shape.width !== newBounds.width ||
      shape.height !== newBounds.height;
  }

  function getReferencePoint(shape, direction) {
    var mid = getMid(shape),
        trbl = asTRBL(shape);

    var referencePoint = {
      x: mid.x,
      y: mid.y
    };

    if (direction.indexOf('n') !== -1) {
      referencePoint.y = trbl.top;
    } else if (direction.indexOf('s') !== -1) {
      referencePoint.y = trbl.bottom;
    }

    if (direction.indexOf('e') !== -1) {
      referencePoint.x = trbl.right;
    } else if (direction.indexOf('w') !== -1) {
      referencePoint.x = trbl.left;
    }

    return referencePoint;
  }

  function getCursor(direction) {
    var prefix = 'resize-';

    if (direction === 'n' || direction === 's') {
      return prefix + 'ns';
    } else if (direction === 'e' || direction === 'w') {
      return prefix + 'ew';
    } else if (direction === 'nw' || direction === 'se') {
      return prefix + 'nwse';
    } else {
      return prefix + 'nesw';
    }
  }

  var MARKER_RESIZING = 'djs-resizing',
      MARKER_RESIZE_NOT_OK = 'resize-not-ok';

  var LOW_PRIORITY = 500;

  /**
   * @typedef {import('../../core/Canvas').default} Canvas
   * @typedef {import('../../core/EventBus').default} EventBus
   * @typedef {import('../preview-support/PreviewSupport').default} PreviewSupport
   */

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
        frame = context.frame = previewSupport.addFrame(shape, canvas.getActiveLayer());

        canvas.addMarker(shape, MARKER_RESIZING);
      }

      if (bounds.width > 5) {
        attr(frame, { x: bounds.x, width: bounds.width });
      }

      if (bounds.height > 5) {
        attr(frame, { y: bounds.y, height: bounds.height });
      }

      if (context.canExecute) {
        classes(frame).remove(MARKER_RESIZE_NOT_OK);
      } else {
        classes(frame).add(MARKER_RESIZE_NOT_OK);
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
    eventBus.on('resize.move', LOW_PRIORITY, function(event) {
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

  /**
   * @typedef {import('../../model/Types').Element} Element
   *
   * @typedef {import('../../core/Canvas').default} Canvas
   * @typedef {import('../../core/EventBus').default} EventBus
   * @typedef {import('../resize/Resize').default} Resize
   * @typedef {import('../selection/Selection').default} Selection
   */

  var HANDLE_OFFSET = -6,
      HANDLE_SIZE = 8,
      HANDLE_HIT_SIZE = 20;

  var CLS_RESIZER = 'djs-resizer';

  var directions = [ 'n', 'w', 's', 'e', 'nw', 'ne', 'se', 'sw' ];


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
        forEach$1(newSelection, bind$2(self.addResizer, self));
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

    function startResize(event) {

      // only trigger on left mouse button
      if (isPrimaryButton(event)) {
        resize.activate(event, element, direction);
      }
    }

    event.bind(gfx, 'mousedown', startResize);
    event.bind(gfx, 'touchstart', startResize);
  };


  ResizeHandles.prototype._createResizer = function(element, x, y, direction) {
    var resizersParent = this._getResizersParent();

    var offset = getHandleOffset(direction);

    var group = create$1('g');

    classes(group).add(CLS_RESIZER);
    classes(group).add(CLS_RESIZER + '-' + element.id);
    classes(group).add(CLS_RESIZER + '-' + direction);

    append(resizersParent, group);

    var visual = create$1('rect');

    attr(visual, {
      x: -HANDLE_SIZE / 2 + offset.x,
      y: -HANDLE_SIZE / 2 + offset.y,
      width: HANDLE_SIZE,
      height: HANDLE_SIZE
    });

    classes(visual).add(CLS_RESIZER + '-visual');

    append(group, visual);

    var hit = create$1('rect');

    attr(hit, {
      x: -HANDLE_HIT_SIZE / 2 + offset.x,
      y: -HANDLE_HIT_SIZE / 2 + offset.y,
      width: HANDLE_HIT_SIZE,
      height: HANDLE_HIT_SIZE
    });

    classes(hit).add(CLS_RESIZER + '-hit');

    append(group, hit);

    transform(group, x, y);

    return group;
  };

  ResizeHandles.prototype.createResizer = function(element, direction) {
    var point = getReferencePoint(element, direction);

    var resizer = this._createResizer(element, point.x, point.y, direction);

    this.makeDraggable(element, resizer, direction);
  };

  // resize handles implementation ///////////////////////////////

  /**
   * Add resizers for a given element.
   *
   * @param {Element} element
   */
  ResizeHandles.prototype.addResizer = function(element) {
    var self = this;

    if (isConnection(element) || !this._resize.canResize({ shape: element })) {
      return;
    }

    forEach$1(directions, function(direction) {
      self.createResizer(element, direction);
    });
  };

  /**
   * Remove all resizers
   */
  ResizeHandles.prototype.removeResizers = function() {
    var resizersParent = this._getResizersParent();

    clear(resizersParent);
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

  // helpers //////////

  function getHandleOffset(direction) {
    var offset = {
      x: 0,
      y: 0
    };

    if (direction.indexOf('e') !== -1) {
      offset.x = -HANDLE_OFFSET;
    } else if (direction.indexOf('w') !== -1) {
      offset.x = HANDLE_OFFSET;
    }

    if (direction.indexOf('s') !== -1) {
      offset.y = -HANDLE_OFFSET;
    } else if (direction.indexOf('n') !== -1) {
      offset.y = HANDLE_OFFSET;
    }

    return offset;
  }

  /**
   * @type { import('didi').ModuleDeclaration }
   */
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

  var min = Math.min,
      max = Math.max;

  function preventDefault(e) {
    e.preventDefault();
  }

  function stopPropagation(e) {
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

    this.parent = domify$1(
      '<div class="djs-direct-editing-parent">' +
        '<div class="djs-direct-editing-content" contenteditable="true"></div>' +
      '</div>'
    );

    this.content = query('[contenteditable]', this.parent);

    this.keyHandler = options.keyHandler || function() {};
    this.resizeHandler = options.resizeHandler || function() {};

    this.autoResize = bind$2(this.autoResize, this);
    this.handlePaste = bind$2(this.handlePaste, this);
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

    assign$1(parent.style, {
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

    assign$1(content.style, {
      boxSizing: 'border-box',
      width: '100%',
      outline: 'none',
      wordWrap: 'break-word'
    }, contentStyle);

    if (options.centerVertically) {
      assign$1(content.style, {
        position: 'absolute',
        top: '50%',
        transform: 'translate(0, -50%)'
      }, contentStyle);
    }

    content.innerText = value;

    event.bind(content, 'keydown', this.keyHandler);
    event.bind(content, 'mousedown', stopPropagation);
    event.bind(content, 'paste', self.handlePaste);

    if (options.autoResize) {
      event.bind(content, 'input', this.autoResize);
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

    this.insertText(text);

    if (options.autoResize) {
      var hasResized = this.autoResize(style);

      if (hasResized) {
        this.resizeHandler(hasResized);
      }
    }
  };

  TextBox.prototype.insertText = function(text) {
    text = normalizeEndOfLineSequences(text);

    // insertText command not supported by Internet Explorer
    var success = document.execCommand('insertText', false, text);

    if (success) {
      return;
    }

    this._insertTextIE(text);
  };

  TextBox.prototype._insertTextIE = function(text) {

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
          remove$2(childNode);
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
      resizeHandle = this.resizeHandle = domify$1(
        '<div class="djs-direct-editing-resize-handle"></div>'
      );

      var startX, startY, startWidth, startHeight;

      var onMouseDown = function(e) {
        preventDefault(e);
        stopPropagation(e);

        startX = e.clientX;
        startY = e.clientY;

        var bounds = parent.getBoundingClientRect();

        startWidth = bounds.width;
        startHeight = bounds.height;

        event.bind(document, 'mousemove', onMouseMove);
        event.bind(document, 'mouseup', onMouseUp);
      };

      var onMouseMove = function(e) {
        preventDefault(e);
        stopPropagation(e);

        var newWidth = min(max(startWidth + e.clientX - startX, minWidth), maxWidth);
        var newHeight = min(max(startHeight + e.clientY - startY, minHeight), maxHeight);

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
        preventDefault(e);
        stopPropagation(e);

        event.unbind(document,'mousemove', onMouseMove, false);
        event.unbind(document, 'mouseup', onMouseUp, false);
      };

      event.bind(resizeHandle, 'mousedown', onMouseDown);
    }

    assign$1(resizeHandle.style, {
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

    event.unbind(content, 'keydown', this.keyHandler);
    event.unbind(content, 'mousedown', stopPropagation);
    event.unbind(content, 'input', this.autoResize);
    event.unbind(content, 'paste', this.handlePaste);

    if (resizeHandle) {
      resizeHandle.removeAttribute('style');

      remove$2(resizeHandle);
    }

    remove$2(parent);
  };


  TextBox.prototype.getValue = function() {
    return this.content.innerText.trim();
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

  // helpers //////////

  function normalizeEndOfLineSequences(string) {
    return string.replace(/\r\n|\r|\n/g, '\n');
  }

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
      keyHandler: bind$2(this._handleKey, this),
      resizeHandler: bind$2(this._handleResize, this)
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
   * @param {djs.model.Base} [element]
   *
   * @return {boolean}
   */
  DirectEditing.prototype.isActive = function(element) {
    return !!(this._active && (!element || this._active.element === element));
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

    var containerBounds,
        previousBounds = active.context.bounds,
        newBounds = this.$textbox.getBoundingClientRect(),
        newText = this.getValue(),
        previousText = active.context.text;

    if (
      newText !== previousText ||
      newBounds.height !== previousBounds.height ||
      newBounds.width !== previousBounds.width
    ) {
      containerBounds = this._textbox.container.getBoundingClientRect();

      active.provider.update(active.element, newText, active.context.text, {
        x: newBounds.left - containerBounds.left,
        y: newBounds.top - containerBounds.top,
        width: newBounds.width,
        height: newBounds.height
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
      return ((context = p.activate(element))) ? p : null;
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

  var MARKER_OK$1 = 'connect-ok',
      MARKER_NOT_OK$1 = 'connect-not-ok';


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

      attr(visual, { 'points': [ waypoints[0].x, waypoints[0].y, waypoints[1].x, waypoints[1].y ] });
    });

    eventBus.on('connect.hover', function(event) {
      var context = event.context;
          context.source;
          var hover = event.hover,
          canExecute;

      canExecute = context.canExecute = canConnect();

      // simply ignore hover
      if (canExecute === null) {
        return;
      }

      context.target = hover;

      canvas.addMarker(hover, canExecute ? MARKER_OK$1 : MARKER_NOT_OK$1);
    });

    eventBus.on([ 'connect.out', 'connect.cleanup' ], function(event) {
      var context = event.context;

      if (context.target) {
        canvas.removeMarker(context.target, context.canExecute ? MARKER_OK$1 : MARKER_NOT_OK$1);
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

      visual = create$1('polyline');
      attr(visual, {
        'stroke': '#333',
        'strokeDasharray': [ 1 ],
        'strokeWidth': 2,
        'pointer-events': 'none'
      });

      append(canvas.getDefaultLayer(), visual);

      context.visual = visual;
    });

    eventBus.on('connect.end', function(event) {

      var context = event.context;
          context.source;
          context.type;
          context.sourcePosition;
          context.target;
          ({
            x: event.x,
            y: event.y
          });
          var canExecute = context.canExecute || canConnect();

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
   * @typedef {import('../../model/Types').Element} Element
   * @typedef {import('../../model/Types').Connection} Connection
   * @typedef {import('../../model/Types').Shape} Shape
   *
   * @typedef {import('../../util/Types').Point} Point
   *
   * @typedef {import('didi').Injector} Injector
   *
   * @typedef {import('../../core/Canvas').default} Canvas
   * @typedef {import('../../core/ElementFactory').default} ElementFactory
   * @typedef {import('../../core/GraphicsFactory').default} GraphicsFactory
   */

  var MARKER_CONNECTION_PREVIEW = 'djs-connection-preview';

  /**
   * Draws connection preview. Optionally, this can use layouter and connection docking to draw
   * better looking previews.
   *
   * @param {Injector} injector
   * @param {Canvas} canvas
   * @param {GraphicsFactory} graphicsFactory
   * @param {ElementFactory} elementFactory
   */
  function ConnectionPreview(
      injector,
      canvas,
      graphicsFactory,
      elementFactory
  ) {
    this._canvas = canvas;
    this._graphicsFactory = graphicsFactory;
    this._elementFactory = elementFactory;

    // optional components
    this._connectionDocking = injector.get('connectionDocking', false);
    this._layouter = injector.get('layouter', false);
  }

  ConnectionPreview.$inject = [
    'injector',
    'canvas',
    'graphicsFactory',
    'elementFactory'
  ];

  /**
   * Draw connection preview.
   *
   * Provide at least one of <source, connectionStart> and <target, connectionEnd> to create a preview.
   * In the clean up stage, call `connectionPreview#cleanUp` with the context to remove preview.
   *
   * @param {Object} context
   * @param {Object|boolean} canConnect
   * @param {Object} hints
   * @param {Element} [hints.source] source element
   * @param {Element} [hints.target] target element
   * @param {Point} [hints.connectionStart] connection preview start
   * @param {Point} [hints.connectionEnd] connection preview end
   * @param {Point[]} [hints.waypoints] provided waypoints for preview
   * @param {boolean} [hints.noLayout] true if preview should not be laid out
   * @param {boolean} [hints.noCropping] true if preview should not be cropped
   * @param {boolean} [hints.noNoop] true if simple connection should not be drawn
   */
  ConnectionPreview.prototype.drawPreview = function(context, canConnect, hints) {

    hints = hints || {};

    var connectionPreviewGfx = context.connectionPreviewGfx,
        getConnection = context.getConnection,
        source = hints.source,
        target = hints.target,
        waypoints = hints.waypoints,
        connectionStart = hints.connectionStart,
        connectionEnd = hints.connectionEnd,
        noLayout = hints.noLayout,
        noCropping = hints.noCropping,
        noNoop = hints.noNoop,
        connection;

    var self = this;

    if (!connectionPreviewGfx) {
      connectionPreviewGfx = context.connectionPreviewGfx = this.createConnectionPreviewGfx();
    }

    clear(connectionPreviewGfx);

    if (!getConnection) {
      getConnection = context.getConnection = cacheReturnValues(function(canConnect, source, target) {
        return self.getConnection(canConnect, source, target);
      });
    }

    if (canConnect) {
      connection = getConnection(canConnect, source, target);
    }

    if (!connection) {
      !noNoop && this.drawNoopPreview(connectionPreviewGfx, hints);
      return;
    }
    
    if(context.connection) {
    	connection.connectionType = context.connection.connectionType;
    }

    connection.waypoints = waypoints || [];

    // optional layout
    if (this._layouter && !noLayout) {
      connection.waypoints = this._layouter.layoutConnection(connection, {
        source: source,
        target: target,
        connectionStart: connectionStart,
        connectionEnd: connectionEnd,
        waypoints: hints.waypoints || connection.waypoints
      });
    }

    // fallback if no waypoints were provided nor created with layouter
    if (!connection.waypoints || !connection.waypoints.length) {
      connection.waypoints = [
        source ? getMid(source) : connectionStart,
        target ? getMid(target) : connectionEnd
      ];
    }

    // optional cropping
    if (this._connectionDocking && (source || target) && !noCropping) {
      connection.waypoints = this._connectionDocking.getCroppedWaypoints(connection, source, target);
    }

    this._graphicsFactory.drawConnection(connectionPreviewGfx, connection);
  };

  /**
   * Draw simple connection between source and target or provided points.
   *
   * @param {SVGElement} connectionPreviewGfx container for the connection
   * @param {Object} hints
   * @param {Element} [hints.source] source element
   * @param {Element} [hints.target] target element
   * @param {Point} [hints.connectionStart] required if source is not provided
   * @param {Point} [hints.connectionEnd] required if target is not provided
   */
  ConnectionPreview.prototype.drawNoopPreview = function(connectionPreviewGfx, hints) {
    var source = hints.source,
        target = hints.target,
        start = hints.connectionStart || getMid(source),
        end = hints.connectionEnd || getMid(target);

    var waypoints = this.cropWaypoints(start, end, source, target);

    var connection = this.createNoopConnection(waypoints[0], waypoints[1]);

    append(connectionPreviewGfx, connection);
  };

  /**
   * Return cropped waypoints.
   *
   * @param {Point} start
   * @param {Point} end
   * @param {Element} source
   * @param {Element} target
   *
   * @return {Point[]}
   */
  ConnectionPreview.prototype.cropWaypoints = function(start, end, source, target) {
    var graphicsFactory = this._graphicsFactory,
        sourcePath = source && graphicsFactory.getShapePath(source),
        targetPath = target && graphicsFactory.getShapePath(target),
        connectionPath = graphicsFactory.getConnectionPath({ waypoints: [ start, end ] });

    start = (source && getElementLineIntersection(sourcePath, connectionPath, true)) || start;
    end = (target && getElementLineIntersection(targetPath, connectionPath, false)) || end;

    return [ start, end ];
  };

  /**
   * Remove connection preview container if it exists.
   *
   * @param {Object} [context]
   * @param {SVGElement} [context.connectionPreviewGfx] preview container
   */
  ConnectionPreview.prototype.cleanUp = function(context) {
    if (context && context.connectionPreviewGfx) {
      remove$1(context.connectionPreviewGfx);
    }
  };

  /**
   * Get connection that connects source and target.
   *
   * @param {Object|boolean} canConnect
   *
   * @return {Connection}
   */
  ConnectionPreview.prototype.getConnection = function(canConnect) {
    var attrs = ensureConnectionAttrs(canConnect);

    return this._elementFactory.createConnection(attrs);
  };


  /**
   * Add and return preview graphics.
   *
   * @return {SVGElement}
   */
  ConnectionPreview.prototype.createConnectionPreviewGfx = function() {
    var gfx = create$1('g');

    attr(gfx, {
      pointerEvents: 'none'
    });

    classes(gfx).add('djs-connection').add(MARKER_CONNECTION_PREVIEW);

    append(this._canvas.getActiveLayer(), gfx);

    return gfx;
  };

  /**
   * Create and return simple connection.
   *
   * @param {Point} start
   * @param {Point} end
   *
   * @return {SVGElement}
   */
  ConnectionPreview.prototype.createNoopConnection = function(start, end) {
    return createLine([ start, end ], {
      'stroke': '#333',
      'strokeDasharray': [ 1 ],
      'strokeWidth': 2,
      'pointer-events': 'none'
    });
  };

  // helpers //////////

  /**
   * Returns function that returns cached return values referenced by stringified first argument.
   *
   * @param {Function} fn
   *
   * @return {Function}
   */
  function cacheReturnValues(fn) {
    var returnValues = {};

    /**
     * Return cached return value referenced by stringified first argument.
     *
     * @return {*}
     */
    return function(firstArgument) {
      var key = JSON.stringify(firstArgument);

      var returnValue = returnValues[key];

      if (!returnValue) {
        returnValue = returnValues[key] = fn.apply(null, arguments);
      }

      return returnValue;
    };
  }

  /**
   * Ensure connection attributes is object.
   *
   * @param {Object|boolean} canConnect
   *
   * @return {Object}
   */
  function ensureConnectionAttrs(canConnect) {
    if (isObject(canConnect)) {
      return canConnect;
    } else {
      return {};
    }
  }

  /**
   * @type { import('didi').ModuleDeclaration }
   */
  var ConnectionPreviewModule = {
    __init__: [ 'connectionPreview' ],
    connectionPreview: [ 'type', ConnectionPreview ]
  };

  /**
   * @typedef {import('didi').Injector} Injector
   *
   * @typedef {import('../core/Types').ElementLike} ElementLike
   *
   * @typedef {import('../core/EventBus').default} EventBus
   * @typedef {import('./CommandHandler').default} CommandHandler
   *
   * @typedef { any } CommandContext
   * @typedef { {
   *   new (...args: any[]) : CommandHandler
   * } } CommandHandlerConstructor
   * @typedef { {
   *   [key: string]: CommandHandler;
   * } } CommandHandlerMap
   * @typedef { {
   *   command: string;
   *   context: any;
   *   id?: any;
   * } } CommandStackAction
   * @typedef { {
   *   actions: CommandStackAction[];
   *   dirty: ElementLike[];
   *   trigger: 'execute' | 'undo' | 'redo' | 'clear' | null;
   *   atomic?: boolean;
   * } } CurrentExecution
   */

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
   * commands) in their `preExecute(d)` and `postExecute(d)` phases. The command
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
     * @type {CommandHandlerMap}
     */
    this._handlerMap = {};

    /**
     * A stack containing all re/undoable actions on the diagram
     *
     * @type {CommandStackAction[]}
     */
    this._stack = [];

    /**
     * The current index on the stack
     *
     * @type {number}
     */
    this._stackIdx = -1;

    /**
     * Current active commandStack execution
     *
     * @type {CurrentExecution}
     */
    this._currentExecution = {
      actions: [],
      dirty: [],
      trigger: null
    };

    /**
     * @type {Injector}
     */
    this._injector = injector;

    /**
     * @type EventBus
     */
    this._eventBus = eventBus;

    /**
     * @type { number }
     */
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
   * Execute a command.
   *
   * @param {string} command The command to execute.
   * @param {CommandContext} context The context with which to execute the command.
   */
  CommandStack.prototype.execute = function(command, context) {
    if (!command) {
      throw new Error('command required');
    }

    this._currentExecution.trigger = 'execute';

    const action = { command: command, context: context };

    this._pushAction(action);
    this._internalExecute(action);
    this._popAction();
  };


  /**
   * Check whether a command can be executed.
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
   * @param {string} command The command to execute.
   * @param {CommandContext} context The context with which to execute the command.
   *
   * @return {boolean} Whether the command can be executed with the given context.
   */
  CommandStack.prototype.canExecute = function(command, context) {

    const action = { command: command, context: context };

    const handler = this._getHandler(command);

    let result = this._fire(command, 'canExecute', action);

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
   * Clear the command stack, erasing all undo / redo history.
   *
   * @param {boolean} [emit=true] Whether to fire an event. Defaults to `true`.
   */
  CommandStack.prototype.clear = function(emit) {
    this._stack.length = 0;
    this._stackIdx = -1;

    if (emit !== false) {
      this._fire('changed', { trigger: 'clear' });
    }
  };


  /**
   * Undo last command(s)
   */
  CommandStack.prototype.undo = function() {
    let action = this._getUndoAction(),
        next;

    if (action) {
      this._currentExecution.trigger = 'undo';

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
    let action = this._getRedoAction(),
        next;

    if (action) {
      this._currentExecution.trigger = 'redo';

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
   * Register a handler instance with the command stack.
   *
   * @param {string} command Command to be executed.
   * @param {CommandHandler} handler Handler to execute the command.
   */
  CommandStack.prototype.register = function(command, handler) {
    this._setHandler(command, handler);
  };


  /**
   * Register a handler type with the command stack  by instantiating it and
   * injecting its dependencies.
   *
   * @param {string} command Command to be executed.
   * @param {CommandHandlerConstructor} handlerCls Constructor to instantiate a {@link CommandHandler}.
   */
  CommandStack.prototype.registerHandler = function(command, handlerCls) {

    if (!command || !handlerCls) {
      throw new Error('command and handlerCls must be defined');
    }

    const handler = this._injector.instantiate(handlerCls);
    this.register(command, handler);
  };

  /**
   * @return {boolean}
   */
  CommandStack.prototype.canUndo = function() {
    return !!this._getUndoAction();
  };

  /**
   * @return {boolean}
   */
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
    const command = action.command,
          context = action.context;

    const handler = this._getHandler(command);

    // guard against illegal nested command stack invocations
    this._atomicDo(() => {
      this._fire(command, 'revert', action);

      if (handler.revert) {
        this._markDirty(handler.revert(context));
      }

      this._revertedAction(action);

      this._fire(command, 'reverted', action);
    });
  };


  CommandStack.prototype._fire = function(command, qualifier, event) {
    if (arguments.length < 3) {
      event = qualifier;
      qualifier = null;
    }

    const names = qualifier ? [ command + '.' + qualifier, qualifier ] : [ command ];
    let result;

    event = this._eventBus.createEvent(event);

    for (const name of names) {
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

    const execution = this._currentExecution;

    execution.atomic = true;

    try {
      fn();
    } finally {
      execution.atomic = false;
    }
  };

  CommandStack.prototype._internalExecute = function(action, redo) {
    const command = action.command,
          context = action.context;

    const handler = this._getHandler(command);

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
    this._atomicDo(() => {

      this._fire(command, 'execute', action);

      if (handler.execute) {

        // actual execute + mark return results as dirty
        this._markDirty(handler.execute(context));
      }

      // log to stack
      this._executedAction(action, redo);

      this._fire(command, 'executed', action);
    });

    if (!redo) {
      this._fire(command, 'postExecute', action);

      if (handler.postExecute) {
        handler.postExecute(context);
      }

      this._fire(command, 'postExecuted', action);
    }

    this._popAction();
  };


  CommandStack.prototype._pushAction = function(action) {

    const execution = this._currentExecution,
          actions = execution.actions;

    const baseAction = actions[0];

    if (execution.atomic) {
      throw new Error('illegal invocation in <execute> or <revert> phase (action: ' + action.command + ')');
    }

    if (!action.id) {
      action.id = (baseAction && baseAction.id) || this._createId();
    }

    actions.push(action);
  };


  CommandStack.prototype._popAction = function() {
    const execution = this._currentExecution,
          trigger = execution.trigger,
          actions = execution.actions,
          dirty = execution.dirty;

    actions.pop();

    if (!actions.length) {
      this._eventBus.fire('elements.changed', { elements: uniqueBy('id', dirty.reverse()) });

      dirty.length = 0;

      this._fire('changed', { trigger: trigger });

      execution.trigger = null;
    }
  };


  CommandStack.prototype._markDirty = function(elements) {
    const execution = this._currentExecution;

    if (!elements) {
      return;
    }

    elements = isArray$3(elements) ? elements : [ elements ];

    execution.dirty = execution.dirty.concat(elements);
  };


  CommandStack.prototype._executedAction = function(action, redo) {
    const stackIdx = ++this._stackIdx;

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

  /**
   * @type { import('didi').ModuleDeclaration }
   */
  var CommandModule = {
    commandStack: [ 'type', CommandStack ]
  };

  /**
   * @typedef {import('../core/ElementRegistry').default} ElementRegistry
   * @typedef {import('../core/GraphicsFactory').default} GraphicsFactory
   */

  function dockingToPoint(docking) {

    // use the dockings actual point and
    // retain the original docking
    return assign$1({ original: docking.point.original || docking.point }, docking.actual);
  }


  /**
   * A {@link ConnectionDocking} that crops connection waypoints based on
   * the path(s) of the connection source and target.
   *
   * @param {ElementRegistry} elementRegistry
   * @param {GraphicsFactory} graphicsFactory
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
    return assign$1(new Type(), attrs);
  }

  /**
   * Checks whether an object is a model instance.
   *
   * @param {any} obj
   *
   * @return {boolean}
   */
  function isModelElement(obj) {
    return obj instanceof ElementImpl;
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
  function ElementFactory$1() {
    this._uid = 12;
  }

  /**
   * Create a root element.
   *
   * @param {Partial<Root>} [attrs]
   *
   * @return {V} The created root element.
   */
  ElementFactory$1.prototype.createRoot = function(attrs) {
    return this.create('root', attrs);
  };

  /**
   * Create a label.
   *
   * @param {Partial<Label>} [attrs]
   *
   * @return {U} The created label.
   */
  ElementFactory$1.prototype.createLabel = function(attrs) {
    return this.create('label', attrs);
  };

  /**
   * Create a shape.
   *
   * @param {Partial<Shape>} [attrs]
   *
   * @return {W} The created shape.
   */
  ElementFactory$1.prototype.createShape = function(attrs) {
    return this.create('shape', attrs);
  };

  /**
   * Create a connection.
   *
   * @param {Partial<Connection>} [attrs]
   *
   * @return {T} The created connection.
   */
  ElementFactory$1.prototype.createConnection = function(attrs) {
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
  ElementFactory$1.prototype.create = function(type, attrs) {

    attrs = assign$1({}, attrs || {});

    if (!attrs.id) {
      attrs.id = type + '_' + (this._uid++);
    }

    return create(type, attrs);
  };

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

  inherits(ElementFactory, ElementFactory$1);

  function ElementFactory() {
    ElementFactory$1.call(this);
  }

  ElementFactory.prototype.createEnumeration = function(attrs) {
    var newAttrs = {
      shapeType: 'enumeration',
      businessObject: {}
    };

    assign$1(newAttrs, attrs);

    return this.createShape(newAttrs);
  };

  ElementFactory.prototype.createClass = function(attrs) {
    var newAttrs = {
      shapeType: 'class',
      businessObject: {}
    };

    assign$1(newAttrs, attrs);

    return this.createShape(newAttrs);
  };

  ElementFactory.prototype.createUmlLabel = function(attrs) {
    var newAttrs = {
      shapeType: 'label'
    };

    assign$1(newAttrs, attrs);

    return this.createLabel(newAttrs);
  };

  ElementFactory.prototype.createInheritance = function(attrs) {
    var newAttrs = {
      connectionType: 'inheritance'
    };

    assign$1(newAttrs, attrs);

    return this.createConnection(newAttrs);
  };

  ElementFactory.prototype.createAssociation = function(attrs) {
    var newAttrs = {
      connectionType: 'association'
    };

    assign$1(newAttrs, attrs);

    return this.createConnection(newAttrs);
  };

  ElementFactory.prototype.createAggregation = function(attrs) {
    var newAttrs = {
      connectionType: 'aggregation'
    };

    assign$1(newAttrs, attrs);

    return this.createConnection(newAttrs);
  };

  ElementFactory.prototype.createComposition = function(attrs) {
    var newAttrs = {
      connectionType: 'composition'
    };

    assign$1(newAttrs, attrs);

    return this.createConnection(newAttrs);
  };

  ElementFactory.prototype.createDependency = function(attrs) {
    var newAttrs = {
      connectionType: 'dependency'
    };

    assign$1(newAttrs, attrs);

    return this.createConnection(newAttrs);
  };

  ElementFactory.prototype.createRealization = function(attrs) {
    var newAttrs = {
      connectionType: 'realization'
    };

    assign$1(newAttrs, attrs);

    return this.createConnection(newAttrs);
  };

  /**
   * @typedef {import('../core/Types').ElementLike} Element
   * @typedef {import('../core/Types').ConnectionLike} Connection
   *
   * @typedef {import('../util').Point} Point
   *
   * @typedef { {
   *   connectionStart?: Point;
   *   connectionEnd?: Point;
   *   source?: Element;
   *   target?: Element;
   * } } LayoutConnectionHints
   */



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
   * @param {Connection} connection
   * @param {LayoutConnectionHints} [hints]
   *
   * @return {Point[]} The waypoints of the laid out connection.
   */
  BaseLayouter.prototype.layoutConnection = function(connection, hints) {

    hints = hints || {};

    return [
      hints.connectionStart || getMid(hints.source || connection.source),
      hints.connectionEnd || getMid(hints.target || connection.target)
    ];
  };

  /**
   * @typedef {import('../util/Types').Point} Point
   * @typedef {import('../util/Types').Rect} Rect
   */

  var MIN_SEGMENT_LENGTH = 20,
      POINT_ORIENTATION_PADDING = 5;

  var round$3 = Math.round;

  var INTERSECTION_THRESHOLD = 20,
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

    var xmid = round$3((b.x - a.x) / 2 + a.x),
        ymid = round$3((b.y - a.y) / 2 + a.y);

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

    var xmid = round$3((b.x - a.x) / 2 + a.x),
        ymid = round$3((b.y - a.y) / 2 + a.y);

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


  /**
   * Connect two rectangles using a manhattan layouted connection.
   *
   * @param {Rect} source source rectangle
   * @param {Rect} target target rectangle
   * @param {Point} [start] source docking
   * @param {Point} [end] target docking
   * @param {Object} [hints]
   * @param {string} [hints.preserveDocking=source] preserve docking on selected side
   * @param {string[]} [hints.preferredLayouts]
   * @param {Point|boolean} [hints.connectionStart] whether the start changed
   * @param {Point|boolean} [hints.connectionEnd] whether the end changed
   *
   * @return {Point[]} connection points
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
   * @param {Rect} source
   * @param {Rect} target
   * @param {Point} [start]
   * @param {Point} [end]
   * @param {Point[]} [waypoints]
   * @param {Object} [hints]
   * @param {string[]} [hints.preferredLayouts] The list of preferred layouts.
   * @param {boolean} [hints.connectionStart]
   * @param {boolean} [hints.connectionEnd]
   *
   * @return {Point[]} The waypoints of the repaired connection.
   */
  function repairConnection(source, target, start, end, waypoints, hints) {

    if (isArray$3(start)) {
      waypoints = start;
      hints = end;

      start = getMid(source);
      end = getMid(target);
    }

    hints = assign$1({ preferredLayouts: [] }, hints);
    waypoints = waypoints || [];

    var preferredLayouts = hints.preferredLayouts,
        preferStraight = preferredLayouts.indexOf('straight') !== -1,
        repairedWaypoints;

    // just layout non-existing or simple connections
    // attempt to render straight lines, if required

    // attempt to layout a straight line
    repairedWaypoints = preferStraight && tryLayoutStraight(source, target, start, end, hints);

    if (repairedWaypoints) {
      return repairedWaypoints;
    }

    // try to layout from end
    repairedWaypoints = hints.connectionEnd && tryRepairConnectionEnd(target, source, end, waypoints);

    if (repairedWaypoints) {
      return repairedWaypoints;
    }

    // try to layout from start
    repairedWaypoints = hints.connectionStart && tryRepairConnectionStart(source, target, start, waypoints);

    if (repairedWaypoints) {
      return repairedWaypoints;
    }

    // or whether nothing seems to have changed
    if (!hints.connectionStart && !hints.connectionEnd && waypoints && waypoints.length) {
      return waypoints;
    }

    // simply reconnect if nothing else worked
    return connectRectangles(source, target, start, end, hints);
  }


  function inRange(a, start, end) {
    return a >= start && a <= end;
  }

  function isInRange(axis, a, b) {
    var size = {
      x: 'width',
      y: 'height'
    };

    return inRange(a[axis], b[axis], b[axis] + b[size[axis]]);
  }

  /**
   * Lay out a straight connection.
   *
   * @param {Rect} source
   * @param {Rect} target
   * @param {Point} start
   * @param {Point} end
   * @param {Object} [hints]
   * @param {string} [hints.preserveDocking]
   *
   * @return {Point[]|null} The waypoints or null if layout isn't possible.
   */
  function tryLayoutStraight(source, target, start, end, hints) {
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
   * Repair a connection from start.
   *
   * @param {Rect} moved
   * @param {Rect} other
   * @param {Point} newDocking
   * @param {Point[]} points originalPoints from moved to other
   *
   * @return {Point[]|null} The waypoints of the repaired connection.
   */
  function tryRepairConnectionStart(moved, other, newDocking, points) {
    return _tryRepairConnectionSide(moved, other, newDocking, points);
  }

  /**
   * Repair a connection from end.
   *
   * @param {Rect} moved
   * @param {Rect} other
   * @param {Point} newDocking
   * @param {Point[]} points originalPoints from moved to other
   *
   * @return {Point[]|null} The waypoints of the repaired connection.
   */
  function tryRepairConnectionEnd(moved, other, newDocking, points) {
    var waypoints = points.slice().reverse();

    waypoints = _tryRepairConnectionSide(moved, other, newDocking, waypoints);

    return waypoints ? waypoints.reverse() : null;
  }

  /**
   * Repair a connection from one side that moved.
   *
   * @param {Rect} moved
   * @param {Rect} other
   * @param {Point} newDocking
   * @param {Point[]} points originalPoints from moved to other
   *
   * @return {Point[]} The waypoints of the repaired connection.
   */
  function _tryRepairConnectionSide(moved, other, newDocking, points) {

    function needsRelayout(points) {
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

        // repair horizontal alignment
        return { x: newPeer.x, y: candidate.y };
      case 'h':

        // repair vertical alignment
        return { x: candidate.x, y: newPeer.y };
      }

      return { x: candidate.x, y: candidate. y };
    }

    function removeOverlapping(points, a, b) {
      var i;

      for (i = points.length - 2; i !== 0; i--) {

        // intersects (?) break, remove all bendpoints up to this one and relayout
        if (pointInRect(points[i], a, INTERSECTION_THRESHOLD) ||
            pointInRect(points[i], b, INTERSECTION_THRESHOLD)) {

          // return sliced old connection
          return points.slice(i);
        }
      }

      return points;
    }

    // (0) only repair what has layoutable bendpoints

    // (1) if only one bendpoint and on shape moved onto other shapes axis
    //     (horizontally / vertically), relayout

    if (needsRelayout(points)) {
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
      newPoints = _tryRepairConnectionSide(moved, other, newDocking, slicedPoints);
    }

    // (4) do NOT repair if repaired bendpoints are aligned
    if (newPoints && pointsAligned(newPoints)) {
      return null;
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
   * ```javascript
   * getDirections('top'); // -> 'v:v'
   * getDirections('intersect'); // -> 't:t'
   *
   * getDirections('top-right', 'v:h'); // -> 'v:h'
   * getDirections('top-right', 'h:h'); // -> 'h:h'
   * ```
   *
   * @param {string} orientation
   * @param {string} defaultLayout
   *
   * @return {string}
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

  inherits(UmlLayouter, BaseLayouter);

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

    assign$1(manhattanOptions, {
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

  /**
   * Checks whether a value is an instance of Shape.
   *
   * @param {any} value
   *
   * @return {boolean}
   */
  function isShape(value) {
  	// TODO SFO.. use of !isConnection is not the cleanest way to implement this..
    return isObject(value) && has$1(value, 'children') && !isConnection(value);
  }

  inherits(UmlUpdater, CommandInterceptor);

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
      if(isLabel(event.element) && event.element.labelType === 'classifier') {
        return false;
      }
    });

    var delegatedClassifierEvents = ['element.click', 'element.mousedown', 'element.mouseup', 'element.contextmenu', 'element.dblclick' ];

    eventBus.on(delegatedClassifierEvents, 1500, function(event) {
      if(isLabel(event.element) && event.element.labelType === 'classifier') {
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

  function VisibilityHandler() {

  }
  VisibilityHandler.prototype.execute = function(context) {
    
  };

  /**
   * @typedef {import('../../../core/Canvas').default} Canvas
   * @typedef {import('../Modeling').default} Modeling
   */

  /**
   * A handler that align elements in a certain way.
   *
   * @param {Modeling} modeling
   * @param {Canvas} canvas
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


    forEach$1(elements, function(element) {
      var delta = {
        x: 0,
        y: 0
      };

      if (isDefined(alignment.left)) {
        delta.x = alignment.left - element.x;

      } else if (isDefined(alignment.right)) {
        delta.x = (alignment.right - element.width) - element.x;

      } else if (isDefined(alignment.center)) {
        delta.x = (alignment.center - Math.round(element.width / 2)) - element.x;

      } else if (isDefined(alignment.top)) {
        delta.y = alignment.top - element.y;

      } else if (isDefined(alignment.bottom)) {
        delta.y = (alignment.bottom - element.height) - element.y;

      } else if (isDefined(alignment.middle)) {
        delta.y = (alignment.middle - Math.round(element.height / 2)) - element.y;
      }

      modeling.moveElements([ element ], delta, element.parent);
    });
  };

  AlignElements.prototype.postExecute = function(context) {

  };

  /**
   * @typedef {import('../../../model/Types').Element} Element
   * @typedef {import('../../../model/Types').Parent} Parent
   * @typedef {import('../../../model/Types').Shape} Shape
   *
   * @typedef {import('../../../util/Types').Point} Point
   *
   * @typedef {import('../Modeling').default} Modeling
   */

  /**
   * A handler that implements reversible appending of shapes
   * to a source shape.
   *
   * @param {Modeling} modeling
   */
  function AppendShapeHandler(modeling) {
    this._modeling = modeling;
  }

  AppendShapeHandler.$inject = [ 'modeling' ];


  // api //////////////////////


  /**
   * Creates a new shape.
   *
   * @param {Object} context
   * @param {Partial<Shape>} context.shape The new shape.
   * @param {Element} context.source The element to which to append the new shape to.
   * @param {Parent} context.parent The parent.
   * @param {Point} context.position The position at which to create the new shape.
   */
  AppendShapeHandler.prototype.preExecute = function(context) {

    var source = context.source;

    if (!source) {
      throw new Error('source required');
    }

    var target = context.target || source.parent,
        shape = context.shape,
        hints = context.hints || {};

    shape = context.shape =
      this._modeling.createShape(
        shape,
        context.position,
        target, { attach: hints.attach });

    context.shape = shape;
  };

  AppendShapeHandler.prototype.postExecute = function(context) {
    var hints = context.hints || {};

    if (!existsConnection(context.source, context.shape)) {

      // create connection
      if (hints.connectionTarget === context.source) {
        this._modeling.connect(context.shape, context.source, context.connection);
      } else {
        this._modeling.connect(context.source, context.shape, context.connection);
      }
    }
  };


  function existsConnection(source, target) {
    return some(source.outgoing, function(c) {
      return c.target === target;
    });
  }

  /**
   * @typedef {import('../../../model/Types').Element} Element
   * @typedef {import('../../../model/Types').Shape} Shape
   *
   * @typedef {import('../../../util/Types').Point} Point
   *
   * @typedef {import('../Modeling').ModelingHints} ModelingHints
   *
   * @typedef {import('../../../core/Canvas').default} Canvas
   * @typedef {import('../../../layout/BaseLayouter').default} Layouter
   */

  /**
   * @param {Canvas} canvas
   * @param {Layouter} layouter
   */
  function CreateConnectionHandler(canvas, layouter) {
    this._canvas = canvas;
    this._layouter = layouter;
  }

  CreateConnectionHandler.$inject = [ 'canvas', 'layouter' ];


  // api //////////////////////


  /**
   * Creates a new connection between two elements.
   *
   * @param {Object} context
   * @param {Element} context.source The source.
   * @param {Element} context.target The target.
   * @param {Shape} context.parent The parent.
   * @param {number} [context.parentIndex] The optional index at which to add the
   * connection to the parent's children.
   * @param {ModelingHints} [context.hints] The optional hints.
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

    return connection;
  };

  /**
   * @typedef {import('../Modeling').default} Modeling
   */

  var round$2 = Math.round;

  /**
   * @param {Modeling} modeling
   */
  function CreateElementsHandler(modeling) {
    this._modeling = modeling;
  }

  CreateElementsHandler.$inject = [
    'modeling'
  ];

  CreateElementsHandler.prototype.preExecute = function(context) {
    var elements = context.elements,
        parent = context.parent,
        parentIndex = context.parentIndex,
        position = context.position,
        hints = context.hints;

    var modeling = this._modeling;

    // make sure each element has x and y
    forEach$1(elements, function(element) {
      if (!isNumber(element.x)) {
        element.x = 0;
      }

      if (!isNumber(element.y)) {
        element.y = 0;
      }
    });

    var visibleElements = filter(elements, function(element) {
      return !element.hidden;
    });

    var bbox = getBBox(visibleElements);

    // center elements around position
    forEach$1(elements, function(element) {
      if (isConnection(element)) {
        element.waypoints = map$1(element.waypoints, function(waypoint) {
          return {
            x: round$2(waypoint.x - bbox.x - bbox.width / 2 + position.x),
            y: round$2(waypoint.y - bbox.y - bbox.height / 2 + position.y)
          };
        });
      }

      assign$1(element, {
        x: round$2(element.x - bbox.x - bbox.width / 2 + position.x),
        y: round$2(element.y - bbox.y - bbox.height / 2 + position.y)
      });
    });

    var parents = getParents(elements);

    var cache = {};

    forEach$1(elements, function(element) {
      if (isConnection(element)) {
        cache[ element.id ] = isNumber(parentIndex) ?
          modeling.createConnection(
            cache[ element.source.id ],
            cache[ element.target.id ],
            parentIndex,
            element,
            element.parent || parent,
            hints
          ) :
          modeling.createConnection(
            cache[ element.source.id ],
            cache[ element.target.id ],
            element,
            element.parent || parent,
            hints
          );

        return;
      }

      var createShapeHints = assign$1({}, hints);

      if (parents.indexOf(element) === -1) {
        createShapeHints.autoResize = false;
      }

      if (isLabel(element)) {
        createShapeHints = omit(createShapeHints, [ 'attach' ]);
      }

      cache[ element.id ] = isNumber(parentIndex) ?
        modeling.createShape(
          element,
          pick(element, [ 'x', 'y', 'width', 'height' ]),
          element.parent || parent,
          parentIndex,
          createShapeHints
        ) :
        modeling.createShape(
          element,
          pick(element, [ 'x', 'y', 'width', 'height' ]),
          element.parent || parent,
          createShapeHints
        );
    });

    context.elements = values(cache);
  };

  /**
   * @typedef {import('../../../model/Types').Element} Element
   * @typedef {import('../../../util/Types').Point} Point
   *
   * @typedef {import('../../../core/Canvas').default} Canvas
   */

  var round$1 = Math.round;


  /**
   * A handler that implements reversible addition of shapes.
   *
   * @param {Canvas} canvas
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
   * @param {Element} context.parent The parent.
   * @param {Point} context.position The position at which to create the new shape.
   * @param {number} [context.parentIndex] The optional index at which to add the
   * shape to the parent's children.
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
      assign$1(shape, positionOrBounds);
    } else {
      assign$1(shape, {
        x: positionOrBounds.x - round$1(shape.width / 2),
        y: positionOrBounds.y - round$1(shape.height / 2)
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

    var shape = context.shape;

    // (3) remove form canvas
    this._canvas.removeShape(shape);

    return shape;
  };

  /**
   * @typedef {import('../../../core/Canvas').default} Canvas
   *
   * @typedef {import('../../../model/Types').Element} Element
   * @typedef {import('../../../model/Types').Parent} Parent
   * @typedef {import('../../../model/Types').Shape} Shape
   * @typedef {import('../../../util/Types').Point} Point
   */

  /**
   * A handler that attaches a label to a given target shape.
   *
   * @param {Canvas} canvas
   */
  function CreateLabelHandler(canvas) {
    CreateShapeHandler.call(this, canvas);
  }

  e(CreateLabelHandler, CreateShapeHandler);

  CreateLabelHandler.$inject = [ 'canvas' ];


  // api //////////////////////


  var originalExecute = CreateShapeHandler.prototype.execute;

  /**
   * Append label to element.
   *
   * @param { {
   *   parent: Parent;
   *   position: Point;
   *   shape: Shape;
   *   target: Element;
   * } } context
   */
  CreateLabelHandler.prototype.execute = function(context) {

    var label = context.shape;

    ensureValidDimensions(label);

    label.labelTarget = context.labelTarget;

    return originalExecute.call(this, context);
  };

  var originalRevert = CreateShapeHandler.prototype.revert;

  /**
   * Revert appending by removing label.
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

  /**
   * @typedef {import('../../../core/Canvas').default} Canvas
   * @typedef {import('../Modeling').default} Modeling
   */

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


  /**
   * - Remove connections
   */
  DeleteConnectionHandler.prototype.preExecute = function(context) {

    var modeling = this._modeling;

    var connection = context.connection;

    // remove connections
    saveClear(connection.incoming, function(connection) {

      // To make sure that the connection isn't removed twice
      // For example if a container is removed
      modeling.removeConnection(connection, { nested: true });
    });

    saveClear(connection.outgoing, function(connection) {
      modeling.removeConnection(connection, { nested: true });
    });

  };


  DeleteConnectionHandler.prototype.execute = function(context) {

    var connection = context.connection,
        parent = connection.parent;

    context.parent = parent;

    // remember containment
    context.parentIndex = indexOf(parent.children, connection);

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
    add(parent.children, connection, parentIndex);

    this._canvas.addConnection(connection, parent);

    return connection;
  };

  /**
   * @typedef {import('../../../core/ElementRegistry').default} ElementRegistry
   * @typedef {import('../Modeling').default} Modeling
   */

  /**
   * @param {Modeling} modeling
   * @param {ElementRegistry} elementRegistry
   */
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

    forEach$1(elements, function(element) {

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
   * @typedef {import('../../../core/Canvas').default} Canvas
   * @typedef {import('../Modeling').default} Modeling
   */

  /**
   * A handler that implements reversible deletion of shapes.
   *
   * @param {Canvas} canvas
   * @param {Modeling} modeling
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
      if (isConnection(child)) {
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
    context.oldParentIndex = indexOf(oldParent.children, shape);

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
    add(oldParent.children, shape, oldParentIndex);

    canvas.addShape(shape, oldParent);

    return shape;
  };

  /**
   * @typedef {import('../Modeling').default} Modeling
   */

  /**
   * A handler that distributes elements evenly.
   *
   * @param {Modeling} modeling
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

    forEach$1(groups, function(group, idx) {
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

      forEach$1(sortedElements, function(element) {

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

    forEach$1(groups, function(group, groupIdx) {
      var delta = {},
          prevGroup;

      if (group === firstGroup || group === lastGroup) {
        return;
      }

      prevGroup = groups[groupIdx - 1];

      group.range.max = 0;

      forEach$1(group.elements, function(element, idx) {
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
   * @typedef {import('../../../core/Canvas').default} Canvas
   * @typedef {import('../../../layout/BaseLayouter').default} Layouter
   */

  /**
   * A handler that implements reversible moving of shapes.
   *
   * @param {Layouter} layouter
   * @param {Canvas} canvas
   */
  function LayoutConnectionHandler(layouter, canvas) {
    this._layouter = layouter;
    this._canvas = canvas;
  }

  LayoutConnectionHandler.$inject = [ 'layouter', 'canvas' ];

  LayoutConnectionHandler.prototype.execute = function(context) {

    var connection = context.connection;

    var oldWaypoints = connection.waypoints;

    assign$1(context, {
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
    context.oldParentIndex = remove(oldParent.children, connection);

    // add to new parent at position
    add(newParent.children, connection, newParentIndex);

    // update parent
    connection.parent = newParent;

    // update waypoint positions
    forEach$1(connection.waypoints, function(p) {
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
    remove(newParent.children, connection);

    // restore previous location in old parent
    add(oldParent.children, connection, oldParentIndex);

    // restore parent
    connection.parent = oldParent;

    // revert to old waypoint positions
    forEach$1(connection.waypoints, function(p) {
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
   * @typedef {import('../model/Types').Shape} Shape
   *
   * @typedef {import('../util/Types').Point} Point
   * @typedef {import('../util/Types').Rect} Rect
   */

  /**
   * Calculates the absolute point relative to the new element's position.
   *
   * @param {Point} point [absolute]
   * @param {Rect} oldBounds
   * @param {Rect} newBounds
   *
   * @return {Point} point [absolute]
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

  /**
   * @typedef {import('../../../../core/Types').ConnectionLike} Connection
   * @typedef {import('../../../../core/Types').ShapeLike} Shape
   *
   * @typedef {import('../../../../util/Types').Point} Point
   * @typedef {import('../../../../util/Types').Rect} Rect
   */

  /**
   * @param {Connection} connection
   * @param {Shape} shape
   * @param {Rect} oldBounds
   * @return {Point}
   */
  function getResizedSourceAnchor(connection, shape, oldBounds) {

    var waypoints = safeGetWaypoints(connection),
        waypointsInsideNewBounds = getWaypointsInsideBounds(waypoints, shape),
        oldAnchor = waypoints[0];

    // new anchor is the last waypoint enclosed be resized source
    if (waypointsInsideNewBounds.length) {
      return waypointsInsideNewBounds[ waypointsInsideNewBounds.length - 1 ];
    }

    return getNewAttachPoint(oldAnchor.original || oldAnchor, oldBounds, shape);
  }


  function getResizedTargetAnchor(connection, shape, oldBounds) {

    var waypoints = safeGetWaypoints(connection),
        waypointsInsideNewBounds = getWaypointsInsideBounds(waypoints, shape),
        oldAnchor = waypoints[waypoints.length - 1];

    // new anchor is the first waypoint enclosed be resized target
    if (waypointsInsideNewBounds.length) {
      return waypointsInsideNewBounds[ 0 ];
    }

    return getNewAttachPoint(oldAnchor.original || oldAnchor, oldBounds, shape);
  }


  function getMovedSourceAnchor(connection, source, moveDelta) {

    var waypoints = safeGetWaypoints(connection),
        oldBounds = subtract(source, moveDelta),
        oldAnchor = waypoints[ 0 ];

    return getNewAttachPoint(oldAnchor.original || oldAnchor, oldBounds, source);
  }


  function getMovedTargetAnchor(connection, target, moveDelta) {

    var waypoints = safeGetWaypoints(connection),
        oldBounds = subtract(target, moveDelta),
        oldAnchor = waypoints[ waypoints.length - 1 ];

    return getNewAttachPoint(oldAnchor.original || oldAnchor, oldBounds, target);
  }


  // helpers //////////////////////

  function subtract(bounds, delta) {
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
   * @return {Point[]}
   */
  function safeGetWaypoints(connection) {

    var waypoints = connection.waypoints;

    if (!waypoints.length) {
      throw new Error('connection#' + connection.id + ': no waypoints');
    }

    return waypoints;
  }

  function getWaypointsInsideBounds(waypoints, bounds) {
    var originalWaypoints = map$1(waypoints, getOriginal);

    return filter(originalWaypoints, function(waypoint) {
      return isInsideBounds(waypoint, bounds);
    });
  }

  /**
   * Checks if point is inside bounds, incl. edges.
   *
   * @param {Point} point
   * @param {Rect} bounds
   */
  function isInsideBounds(point, bounds) {
    return getOrientation(bounds, point, 1) === 'intersect';
  }

  function getOriginal(point) {
    return point.original || point;
  }

  /**
   * @typedef {import('../../../../model/Types').Connection} Connection
   * @typedef {import('../../../../model/Types').Element} Element
   * @typedef {import('../../../../model/Types').Shape} Shape
   */

  function MoveClosure() {

    /**
     * @type {Record<string, Shape>}
     */
    this.allShapes = {};

    /**
     * @type {Record<string, Connection>}
     */
    this.allConnections = {};

    /**
     * @type {Record<string, Element>}
     */
    this.enclosedElements = {};

    /**
     * @type {Record<string, Connection>}
     */
    this.enclosedConnections = {};

    /**
     * @type {Record<string, Element>}
     */
    this.topLevel = {};
  }

  /**
   * @param {Element} element
   * @param {boolean} [isTopLevel]
   *
   * @return {MoveClosure}
   */
  MoveClosure.prototype.add = function(element, isTopLevel) {
    return this.addAll([ element ], isTopLevel);
  };

  /**
   * @param {Element[]} elements
   * @param {boolean} [isTopLevel]
   *
   * @return {MoveClosure}
   */
  MoveClosure.prototype.addAll = function(elements, isTopLevel) {

    var newClosure = getClosure(elements, !!isTopLevel, this);

    assign$1(this, newClosure);

    return this;
  };

  /**
   * @typedef {import('../../../../core/Types').ElementLike} Element
   * @typedef {import('../../../../core/Types').ShapeLike} Shape
   *
   * @typedef {import('../../../../util/Types').Point} Point
   *
   * @typedef {import('../../Modeling').default} Modeling
   */

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
   * @template {Element} T
   *
   * @param {T[]} elements
   * @param {Point} delta
   * @param {Shape} newParent The new parent of all elements that are not nested.
   *
   * @return {T[]}
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
   * @param {Shape} [newParent]
   * @param {Shape} [newHost]
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
    forEach$1(allShapes, function(shape) {

      // move the element according to the given delta
      modeling.moveShape(shape, delta, topLevel[shape.id] && !keepParent && newParent, {
        recurse: false,
        layout: false
      });
    });

    // move all child connections / layout external connections
    forEach$1(allConnections, function(c) {

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
   * @param {Element[]} elements
   *
   * @return {MoveClosure}
   */
  MoveHelper.prototype.getClosure = function(elements) {
    return new MoveClosure().addAll(elements, true);
  };

  /**
   * @typedef {import('../Modeling').default} Modeling
   */

  /**
   * A handler that implements reversible moving of shapes.
   *
   * @param {Modeling} modeling
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

  /**
   * @typedef {import('../Modeling').default} Modeling
   */

  /**
   * A handler that implements reversible moving of shapes.
   *
   * @param {Modeling} modeling
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

    context.oldBounds = pick(shape, [ 'x', 'y', 'width', 'height' ]);

    // save old parent in context
    context.oldParent = oldParent;
    context.oldParentIndex = remove(oldParent.children, shape);

    // add to new parent at position
    add(newParent.children, shape, newParentIndex);

    // update shape parent + position
    assign$1(shape, {
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

      forEach$1(shape.incoming, function(c) {
        modeling.layoutConnection(c, {
          connectionEnd: getMovedTargetAnchor(c, shape, delta)
        });
      });

      forEach$1(shape.outgoing, function(c) {
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
    add(oldParent.children, shape, oldParentIndex);

    // revert to old position and parent
    assign$1(shape, {
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
   * @typedef {import('../Modeling').default} Modeling
   */

  /**
   * Reconnect connection handler.
   *
   * @param {Modeling} modeling
   */
  function ReconnectConnectionHandler(modeling) {
    this._modeling = modeling;
  }

  ReconnectConnectionHandler.$inject = [ 'modeling' ];

  ReconnectConnectionHandler.prototype.execute = function(context) {
    var newSource = context.newSource,
        newTarget = context.newTarget,
        connection = context.connection,
        dockingOrPoints = context.dockingOrPoints;

    if (!newSource && !newTarget) {
      throw new Error('newSource or newTarget required');
    }

    if (isArray$3(dockingOrPoints)) {
      context.oldWaypoints = connection.waypoints;
      connection.waypoints = dockingOrPoints;
    }

    if (newSource) {
      context.oldSource = connection.source;
      connection.source = newSource;
    }

    if (newTarget) {
      context.oldTarget = connection.target;
      connection.target = newTarget;
    }

    return connection;
  };

  ReconnectConnectionHandler.prototype.postExecute = function(context) {
    var connection = context.connection,
        newSource = context.newSource,
        newTarget = context.newTarget,
        dockingOrPoints = context.dockingOrPoints,
        hints = context.hints || {};

    var layoutConnectionHints = {};

    if (hints.connectionStart) {
      layoutConnectionHints.connectionStart = hints.connectionStart;
    }

    if (hints.connectionEnd) {
      layoutConnectionHints.connectionEnd = hints.connectionEnd;
    }

    if (hints.layoutConnection === false) {
      return;
    }

    if (newSource && (!newTarget || hints.docking === 'source')) {
      layoutConnectionHints.connectionStart = layoutConnectionHints.connectionStart
        || getDocking(isArray$3(dockingOrPoints) ? dockingOrPoints[ 0 ] : dockingOrPoints);
    }

    if (newTarget && (!newSource || hints.docking === 'target')) {
      layoutConnectionHints.connectionEnd = layoutConnectionHints.connectionEnd
        || getDocking(isArray$3(dockingOrPoints) ? dockingOrPoints[ dockingOrPoints.length - 1 ] : dockingOrPoints);
    }

    if (hints.newWaypoints) {
      layoutConnectionHints.waypoints = hints.newWaypoints;
    }

    this._modeling.layoutConnection(connection, layoutConnectionHints);
  };

  ReconnectConnectionHandler.prototype.revert = function(context) {
    var oldSource = context.oldSource,
        oldTarget = context.oldTarget,
        oldWaypoints = context.oldWaypoints,
        connection = context.connection;

    if (oldSource) {
      connection.source = oldSource;
    }

    if (oldTarget) {
      connection.target = oldTarget;
    }

    if (oldWaypoints) {
      connection.waypoints = oldWaypoints;
    }

    return connection;
  };



  // helpers //////////

  function getDocking(point) {
    return point.original || point;
  }

  /**
   * @typedef {import('../../model/Types').Shape} Shape
   *
   * @typedef {import('../Modeling').default} Modeling
   * @typedef {import('../../rules/Rules').default} Rules
   */

  /**
   * Replace shape by adding new shape and removing old shape. Incoming and outgoing connections will
   * be kept if possible.
   *
   * @class
   * @constructor
   *
   * @param {Modeling} modeling
   * @param {Rules} rules
   */
  function ReplaceShapeHandler(modeling, rules) {
    this._modeling = modeling;
    this._rules = rules;
  }

  ReplaceShapeHandler.$inject = [ 'modeling', 'rules' ];


  /**
   * Add new shape.
   *
   * @param {Object} context
   * @param {Shape} context.oldShape
   * @param {Object} context.newData
   * @param {string} context.newData.type
   * @param {number} context.newData.x
   * @param {number} context.newData.y
   * @param {Object} [context.hints]
   */
  ReplaceShapeHandler.prototype.preExecute = function(context) {
    var self = this,
        modeling = this._modeling,
        rules = this._rules;

    var oldShape = context.oldShape,
        newData = context.newData,
        hints = context.hints || {},
        newShape;

    function canReconnect(source, target, connection) {
      return rules.allowed('connection.reconnect', {
        connection: connection,
        source: source,
        target: target
      });
    }

    // (1) add new shape at given position
    var position = {
      x: newData.x,
      y: newData.y
    };

    var oldBounds = {
      x: oldShape.x,
      y: oldShape.y,
      width: oldShape.width,
      height: oldShape.height
    };

    newShape = context.newShape =
      context.newShape ||
      self.createShape(newData, position, oldShape.parent, hints);

    // (2) update host
    if (oldShape.host) {
      modeling.updateAttachment(newShape, oldShape.host);
    }

    // (3) adopt all children from old shape
    var children;

    if (hints.moveChildren !== false) {
      children = oldShape.children.slice();

      modeling.moveElements(children, { x: 0, y: 0 }, newShape, hints);
    }

    // (4) reconnect connections to new shape if possible
    var incoming = oldShape.incoming.slice(),
        outgoing = oldShape.outgoing.slice();

    forEach$1(incoming, function(connection) {
      var source = connection.source,
          allowed = canReconnect(source, newShape, connection);

      if (allowed) {
        self.reconnectEnd(
          connection, newShape,
          getResizedTargetAnchor(connection, newShape, oldBounds),
          hints
        );
      }
    });

    forEach$1(outgoing, function(connection) {
      var target = connection.target,
          allowed = canReconnect(newShape, target, connection);

      if (allowed) {
        self.reconnectStart(
          connection, newShape,
          getResizedSourceAnchor(connection, newShape, oldBounds),
          hints
        );
      }
    });
  };


  /**
   * Remove old shape.
   */
  ReplaceShapeHandler.prototype.postExecute = function(context) {
    var oldShape = context.oldShape;

    this._modeling.removeShape(oldShape);
  };


  ReplaceShapeHandler.prototype.execute = function(context) {};


  ReplaceShapeHandler.prototype.revert = function(context) {};


  ReplaceShapeHandler.prototype.createShape = function(shape, position, target, hints) {
    return this._modeling.createShape(shape, position, target, hints);
  };


  ReplaceShapeHandler.prototype.reconnectStart = function(connection, newSource, dockingPoint, hints) {
    this._modeling.reconnectStart(connection, newSource, dockingPoint, hints);
  };


  ReplaceShapeHandler.prototype.reconnectEnd = function(connection, newTarget, dockingPoint, hints) {
    this._modeling.reconnectEnd(connection, newTarget, dockingPoint, hints);
  };

  /**
   * @typedef {import('../Modeling').default} Modeling
   */

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
    assign$1(shape, {
      width:  newBounds.width,
      height: newBounds.height,
      x:      newBounds.x,
      y:      newBounds.y
    });

    return shape;
  };

  ResizeShapeHandler.prototype.postExecute = function(context) {
    var modeling = this._modeling;

    var shape = context.shape,
        oldBounds = context.oldBounds,
        hints = context.hints || {};

    if (hints.layout === false) {
      return;
    }

    forEach$1(shape.incoming, function(c) {
      modeling.layoutConnection(c, {
        connectionEnd: getResizedTargetAnchor(c, shape, oldBounds)
      });
    });

    forEach$1(shape.outgoing, function(c) {
      modeling.layoutConnection(c, {
        connectionStart: getResizedSourceAnchor(c, shape, oldBounds)
      });
    });

  };

  ResizeShapeHandler.prototype.revert = function(context) {

    var shape = context.shape,
        oldBounds = context.oldBounds;

    // restore previous bbox
    assign$1(shape, {
      width:  oldBounds.width,
      height: oldBounds.height,
      x:      oldBounds.x,
      y:      oldBounds.y
    });

    return shape;
  };

  /**
   * @typedef {import('../../core/Types').ConnectionLike} Connection
   * @typedef {import('../../core/Types').ShapeLike} Shape
   *
   * @typedef {import('../../util/Types').Axis} Axis
   * @typedef {import('../../util/Types').Direction} Direction
   * @typedef {import('../../util/Types').Point} Point
   * @typedef {import('../../util/Types').Rect} Rect
   */


  /**
   * Returns connections whose waypoints are to be updated. Waypoints are to be updated if start
   * or end is to be moved or resized.
   *
   * @param {Array<Shape>} movingShapes
   * @param {Array<Shape>} resizingShapes
   *
   * @return {Array<Connection>}
   */
  function getWaypointsUpdatingConnections(movingShapes, resizingShapes) {
    var waypointsUpdatingConnections = [];

    forEach$1(movingShapes.concat(resizingShapes), function(shape) {
      var incoming = shape.incoming,
          outgoing = shape.outgoing;

      forEach$1(incoming.concat(outgoing), function(connection) {
        var source = connection.source,
            target = connection.target;

        if (includes$1(movingShapes, source) ||
          includes$1(movingShapes, target) ||
          includes$1(resizingShapes, source) ||
          includes$1(resizingShapes, target)) {

          if (!includes$1(waypointsUpdatingConnections, connection)) {
            waypointsUpdatingConnections.push(connection);
          }
        }
      });
    });

    return waypointsUpdatingConnections;
  }

  function includes$1(array, item) {
    return array.indexOf(item) !== -1;
  }

  /**
   * Resize bounds.
   *
   * @param {Rect} bounds
   * @param {Direction} direction
   * @param {Point} delta
   *
   * @return {Rect}
   */
  function resizeBounds(bounds, direction, delta) {
    var x = bounds.x,
        y = bounds.y,
        width = bounds.width,
        height = bounds.height,
        dx = delta.x,
        dy = delta.y;

    switch (direction) {
    case 'n':
      return {
        x: x,
        y: y + dy,
        width: width,
        height: height - dy
      };
    case 's':
      return {
        x: x,
        y: y,
        width: width,
        height: height + dy
      };
    case 'w':
      return {
        x: x + dx,
        y: y,
        width: width - dx,
        height: height
      };
    case 'e':
      return {
        x: x,
        y: y,
        width: width + dx,
        height: height
      };
    default:
      throw new Error('unknown direction: ' + direction);
    }
  }

  /**
   * @typedef {import('../Modeling').default} Modeling
   */

  /**
   * Add or remove space by moving and resizing shapes and updating connection waypoints.
   *
   * @param {Modeling} modeling
   */
  function SpaceToolHandler(modeling) {
    this._modeling = modeling;
  }

  SpaceToolHandler.$inject = [ 'modeling' ];

  SpaceToolHandler.prototype.preExecute = function(context) {
    var delta = context.delta,
        direction = context.direction,
        movingShapes = context.movingShapes,
        resizingShapes = context.resizingShapes,
        start = context.start,
        oldBounds = {};

    // (1) move shapes
    this.moveShapes(movingShapes, delta);

    // (2a) save old bounds of resized shapes
    forEach$1(resizingShapes, function(shape) {
      oldBounds[shape.id] = getBounds(shape);
    });

    // (2b) resize shapes
    this.resizeShapes(resizingShapes, delta, direction);

    // (3) update connection waypoints
    this.updateConnectionWaypoints(
      getWaypointsUpdatingConnections(movingShapes, resizingShapes),
      delta,
      direction,
      start,
      movingShapes,
      resizingShapes,
      oldBounds
    );
  };

  SpaceToolHandler.prototype.execute = function() {};
  SpaceToolHandler.prototype.revert = function() {};

  SpaceToolHandler.prototype.moveShapes = function(shapes, delta) {
    var self = this;

    forEach$1(shapes, function(element) {
      self._modeling.moveShape(element, delta, null, {
        autoResize: false,
        layout: false,
        recurse: false
      });
    });
  };

  SpaceToolHandler.prototype.resizeShapes = function(shapes, delta, direction) {
    var self = this;

    forEach$1(shapes, function(shape) {
      var newBounds = resizeBounds(shape, direction, delta);

      self._modeling.resizeShape(shape, newBounds, null, {
        attachSupport: false,
        autoResize: false,
        layout: false
      });
    });
  };

  /**
   * Update connections waypoints according to the rules:
   *   1. Both source and target are moved/resized => move waypoints by the delta
   *   2. Only one of source and target is moved/resized => re-layout connection with moved start/end
   */
  SpaceToolHandler.prototype.updateConnectionWaypoints = function(
      connections,
      delta,
      direction,
      start,
      movingShapes,
      resizingShapes,
      oldBounds
  ) {
    var self = this,
        affectedShapes = movingShapes.concat(resizingShapes);

    forEach$1(connections, function(connection) {
      var source = connection.source,
          target = connection.target,
          waypoints = copyWaypoints(connection),
          axis = getAxisFromDirection(direction),
          layoutHints = {};

      if (includes(affectedShapes, source) && includes(affectedShapes, target)) {

        // move waypoints
        waypoints = map$1(waypoints, function(waypoint) {
          if (shouldMoveWaypoint(waypoint, start, direction)) {

            // move waypoint
            waypoint[ axis ] = waypoint[ axis ] + delta[ axis ];
          }

          if (waypoint.original && shouldMoveWaypoint(waypoint.original, start, direction)) {

            // move waypoint original
            waypoint.original[ axis ] = waypoint.original[ axis ] + delta[ axis ];
          }

          return waypoint;
        });

        self._modeling.updateWaypoints(connection, waypoints, {
          labelBehavior: false
        });
      } else if (includes(affectedShapes, source) || includes(affectedShapes, target)) {

        // re-layout connection with moved start/end
        if (includes(movingShapes, source)) {
          layoutHints.connectionStart = getMovedSourceAnchor(connection, source, delta);
        } else if (includes(movingShapes, target)) {
          layoutHints.connectionEnd = getMovedTargetAnchor(connection, target, delta);
        } else if (includes(resizingShapes, source)) {
          layoutHints.connectionStart = getResizedSourceAnchor(
            connection, source, oldBounds[source.id]
          );
        } else if (includes(resizingShapes, target)) {
          layoutHints.connectionEnd = getResizedTargetAnchor(
            connection, target, oldBounds[target.id]
          );
        }

        self._modeling.layoutConnection(connection, layoutHints);
      }
    });
  };


  // helpers //////////

  function copyWaypoint(waypoint) {
    return assign$1({}, waypoint);
  }

  function copyWaypoints(connection) {
    return map$1(connection.waypoints, function(waypoint) {

      waypoint = copyWaypoint(waypoint);

      if (waypoint.original) {
        waypoint.original = copyWaypoint(waypoint.original);
      }

      return waypoint;
    });
  }

  function getAxisFromDirection(direction) {
    switch (direction) {
    case 'n':
      return 'y';
    case 'w':
      return 'x';
    case 's':
      return 'y';
    case 'e':
      return 'x';
    }
  }

  function shouldMoveWaypoint(waypoint, start, direction) {
    var relevantAxis = getAxisFromDirection(direction);

    if (/e|s/.test(direction)) {
      return waypoint[ relevantAxis ] > start;
    } else if (/n|w/.test(direction)) {
      return waypoint[ relevantAxis ] < start;
    }
  }

  function includes(array, item) {
    return array.indexOf(item) !== -1;
  }

  function getBounds(shape) {
    return {
      x: shape.x,
      y: shape.y,
      height: shape.height,
      width: shape.width
    };
  }

  /**
   * @typedef {import('../../model/Types').Shape} Shape
   *
   * @typedef {import('../Modeling').default} Modeling
   */

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

    // recursively remember previous visibility of children
    context.oldChildrenVisibility = getElementsVisibilityRecursive(children);

    // toggle state
    shape.collapsed = !shape.collapsed;

    // recursively hide/show children
    var result = setHiddenRecursive(children, shape.collapsed);

    return [ shape ].concat(result);
  };


  ToggleShapeCollapseHandler.prototype.revert = function(context) {

    var shape = context.shape,
        oldChildrenVisibility = context.oldChildrenVisibility;

    var children = shape.children;

    // recursively set old visability of children
    var result = restoreVisibilityRecursive(children, oldChildrenVisibility);

    // retoggle state
    shape.collapsed = !shape.collapsed;

    return [ shape ].concat(result);
  };


  // helpers //////////////////////

  /**
   * Return a map { elementId -> hiddenState}.
   *
   * @param {Shape[]} elements
   *
   * @return {Object}
   */
  function getElementsVisibilityRecursive(elements) {

    var result = {};

    forEach$1(elements, function(element) {
      result[element.id] = element.hidden;

      if (element.children) {
        result = assign$1({}, result, getElementsVisibilityRecursive(element.children));
      }
    });

    return result;
  }


  function setHiddenRecursive(elements, newHidden) {
    var result = [];
    forEach$1(elements, function(element) {
      element.hidden = newHidden;

      result = result.concat(element);

      if (element.children) {
        result = result.concat(setHiddenRecursive(element.children, element.collapsed || newHidden));
      }
    });

    return result;
  }

  function restoreVisibilityRecursive(elements, lastState) {
    var result = [];
    forEach$1(elements, function(element) {
      element.hidden = lastState[element.id];

      result = result.concat(element);

      if (element.children) {
        result = result.concat(restoreVisibilityRecursive(element.children, lastState));
      }
    });

    return result;
  }

  /**
   * @typedef {import('../Modeling').default} Modeling
   */

  /**
   * A handler that implements reversible attaching/detaching of shapes.
   *
   * @param {Modeling} modeling
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
    return remove(host && host.attachers, attacher);
  }

  function addAttacher(host, attacher, idx) {

    if (!host) {
      return;
    }

    var attachers = host.attachers;

    if (!attachers) {
      host.attachers = attachers = [];
    }

    add(attachers, attacher, idx);
  }

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
   * @typedef {import('../../model/Types').Element} Element
   * @typedef {import('../../model/Types').Connection} Connection
   * @typedef {import('../../model/Types').Parent} Parent
   * @typedef {import('../../model/Types').Shape} Shape
   * @typedef {import('../../model/Types').Label} Label
   *
   * @typedef {import('../../command/CommandStack').default} CommandStack
   * @typedef {import('../../core/ElementFactory').default} ElementFactory
   * @typedef {import('../../core/EventBus').default} EventBus
   *
   * @typedef {import('../../command/CommandStack').CommandHandlerConstructor} CommandHandlerConstructor
   *
   * @typedef {import('../../util/Types').Dimensions} Dimensions
   * @typedef {import('../../util/Types').Direction} Direction
   * @typedef {import('../../util/Types').Point} Point
   * @typedef {import('../../util/Types').Rect} Rect
   *
   * @typedef { 'x' | 'y' } ModelingDistributeAxis
   *
   * @typedef { 'width' | 'height' } ModelingDistributeDimension
   *
   * @typedef { {
   *   bottom?: number;
   *   center?: number;
   *   left?: number;
   *   middle?: number;
   *   right?: number;
   *   top?: number;
   * } } ModelingAlignAlignment
   *
   * @typedef { {
   *   [key: string]: any;
   * } } ModelingHints
   *
   * @typedef { {
   *   attach?: boolean;
   * } & ModelingHints } ModelingMoveElementsHints
   *
   * @typedef { {
   *   attach?: boolean;
   * } & ModelingHints } ModelingCreateShapeHints
   */

  /**
   * @template {Element} U
   *
   * @typedef { {
   *   elements: U[],
   *   range: {
   *     min: number;
   *     max: number;
   *   } }
   * } ModelingDistributeGroup
   */

  /**
   * The basic modeling entry point.
   *
   * @template {Connection} [T=Connection]
   * @template {Element} [U=Element]
   * @template {Label} [V=Label]
   * @template {Parent} [W=Parent]
   * @template {Shape} [X=Shape]
   *
   * @param {EventBus} eventBus
   * @param {ElementFactory} elementFactory
   * @param {CommandStack} commandStack
   */
  function Modeling$1(eventBus, elementFactory, commandStack) {
    this._eventBus = eventBus;
    this._elementFactory = elementFactory;
    this._commandStack = commandStack;

    var self = this;

    eventBus.on('diagram.init', function() {

      // register modeling handlers
      self.registerHandlers(commandStack);
    });
  }

  Modeling$1.$inject = [ 'eventBus', 'elementFactory', 'commandStack' ];

  /**
   * Get a map of all command handlers.
   *
   * @return {Map<string, CommandHandlerConstructor>}
   */
  Modeling$1.prototype.getHandlers = function() {
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

      'connection.reconnect': ReconnectConnectionHandler,

      'elements.create': CreateElementsHandler,
      'elements.move': MoveElementsHandler,
      'elements.delete': DeleteElementsHandler,

      'elements.distribute': DistributeElements,
      'elements.align': AlignElements,

      'element.updateAttachment': UpdateAttachmentHandler
    };
  };

  /**
   * Register handlers with the command stack
   *
   * @param {CommandStack} commandStack
   */
  Modeling$1.prototype.registerHandlers = function(commandStack) {
    forEach$1(this.getHandlers(), function(handler, id) {
      commandStack.registerHandler(id, handler);
    });
  };


  /**
   * Move a shape by the given delta and optionally to a new parent.
   *
   * @param {X} shape
   * @param {Point} delta
   * @param {W} [newParent]
   * @param {number} [newParentIndex]
   * @param {ModelingHints} [hints]
   */
  Modeling$1.prototype.moveShape = function(shape, delta, newParent, newParentIndex, hints) {

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
   * Update the attachment of a shape.
   *
   * @param {X} shape
   * @param {X} [newHost=undefined]
   */
  Modeling$1.prototype.updateAttachment = function(shape, newHost) {
    var context = {
      shape: shape,
      newHost: newHost
    };

    this._commandStack.execute('element.updateAttachment', context);
  };


  /**
   * Move elements by a given delta and optionally to a new parent.
   *
   * @param {U[]} shapes
   * @param {Point} delta
   * @param {W} [target]
   * @param {ModelingMoveElementsHints} [hints]
   */
  Modeling$1.prototype.moveElements = function(shapes, delta, target, hints) {

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

  /**
   * Move a shape by the given delta and optionally to a new parent.
   *
   * @param {T} connection
   * @param {Point} delta
   * @param {W} [newParent]
   * @param {number} [newParentIndex]
   * @param {ModelingHints} [hints]
   */
  Modeling$1.prototype.moveConnection = function(connection, delta, newParent, newParentIndex, hints) {

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

  /**
   * Layout a connection.
   *
   * @param {T} connection
   * @param {ModelingHints} [hints]
   */
  Modeling$1.prototype.layoutConnection = function(connection, hints) {
    var context = {
      connection: connection,
      hints: hints || {}
    };

    this._commandStack.execute('connection.layout', context);
  };

  /**
   * Create a connection.
   *
   * @overlord
   *
   * @param {U} source
   * @param {U} target
   * @param {Partial<T>} connection
   * @param {W} parent
   * @param {ModelingHints} [hints]
   *
   * @return {T}
   */

  /**
   * Create a connection.
   *
   * @param {U} source
   * @param {U} target
   * @param {number} parentIndex
   * @param {Partial<T>} connection
   * @param {W} parent
   * @param {ModelingHints} [hints]
   *
   * @return {T}
   */
  Modeling$1.prototype.createConnection = function(source, target, parentIndex, connection, parent, hints) {

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
   * Create a shape.
   *
   * @overlord
   *
   * @param {Partial<X>} shape
   * @param {Point} position
   * @param {W} target
   * @param {ModelingCreateShapeHints} [hints]
   *
   * @return {X}
   */

  /**
   * Create a shape.
   *
   * @param {Partial<X>} shape
   * @param {Point} position
   * @param {W} target
   * @param {number} parentIndex
   * @param {ModelingCreateShapeHints} [hints]
   *
   * @return {X}
   */
  Modeling$1.prototype.createShape = function(shape, position, target, parentIndex, hints) {

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

  /**
   * Create elements.
   *
   * @param {Partial<U>[]} elements
   * @param {Point} position
   * @param {W} parent
   * @param {number} [parentIndex]
   * @param {ModelingHints} [hints]
   *
   * @return {U[]}
   */
  Modeling$1.prototype.createElements = function(elements, position, parent, parentIndex, hints) {
    if (!isArray$3(elements)) {
      elements = [ elements ];
    }

    if (typeof parentIndex !== 'number') {
      hints = parentIndex;
      parentIndex = undefined;
    }

    hints = hints || {};

    var context = {
      position: position,
      elements: elements,
      parent: parent,
      parentIndex: parentIndex,
      hints: hints
    };

    this._commandStack.execute('elements.create', context);

    return context.elements;
  };

  /**
   * Create a label.
   *
   * @param {U} labelTarget
   * @param {Point} position
   * @param {Partial<V>} label
   * @param {W} [parent]
   *
   * @return {V}
   */
  Modeling$1.prototype.createLabel = function(labelTarget, position, label, parent) {

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
   * Create and connect a shape to a source.
   *
   * @param {U} source
   * @param {Partial<X>} shape
   * @param {Point} position
   * @param {W} target
   * @param {ModelingHints} [hints]
   *
   * @return {X}
   */
  Modeling$1.prototype.appendShape = function(source, shape, position, target, hints) {

    hints = hints || {};

    shape = this._create('shape', shape);

    var context = {
      source: source,
      position: position,
      target: target,
      shape: shape,
      connection: hints.connection,
      connectionParent: hints.connectionParent,
      hints: hints
    };

    this._commandStack.execute('shape.append', context);

    return context.shape;
  };

  /**
   * Remove elements.
   *
   * @param {U[]} elements
   */
  Modeling$1.prototype.removeElements = function(elements) {
    var context = {
      elements: elements
    };

    this._commandStack.execute('elements.delete', context);
  };

  /**
   * Distribute elements along a given axis.
   *
   * @param {ModelingDistributeGroup<U>[]} groups
   * @param {ModelingDistributeAxis} axis
   * @param {ModelingDistributeDimension} dimension
   */
  Modeling$1.prototype.distributeElements = function(groups, axis, dimension) {
    var context = {
      groups: groups,
      axis: axis,
      dimension: dimension
    };

    this._commandStack.execute('elements.distribute', context);
  };

  /**
   * Remove a shape.
   *
   * @param {X} shape
   * @param {ModelingHints} [hints]
   */
  Modeling$1.prototype.removeShape = function(shape, hints) {
    var context = {
      shape: shape,
      hints: hints || {}
    };

    this._commandStack.execute('shape.delete', context);
  };

  /**
   * Remove a connection.
   *
   * @param {T} connection
   * @param {ModelingHints} [hints]
   */
  Modeling$1.prototype.removeConnection = function(connection, hints) {
    var context = {
      connection: connection,
      hints: hints || {}
    };

    this._commandStack.execute('connection.delete', context);
  };

  /**
   * Replace a shape.
   *
   * @param {X} oldShape
   * @param {Partial<X>} newShape
   * @param {ModelingHints} [hints]
   *
   * @return {X}
   */
  Modeling$1.prototype.replaceShape = function(oldShape, newShape, hints) {
    var context = {
      oldShape: oldShape,
      newData: newShape,
      hints: hints || {}
    };

    this._commandStack.execute('shape.replace', context);

    return context.newShape;
  };

  /**
   * Align elements.
   *
   * @param {U[]} elements
   * @param {ModelingAlignAlignment} alignment
   */
  Modeling$1.prototype.alignElements = function(elements, alignment) {
    var context = {
      elements: elements,
      alignment: alignment
    };

    this._commandStack.execute('elements.align', context);
  };

  /**
   * Resize a shape.
   *
   * @param {X} shape
   * @param {Rect} newBounds
   * @param {Dimensions} [minBounds]
   * @param {ModelingHints} [hints]
   */
  Modeling$1.prototype.resizeShape = function(shape, newBounds, minBounds, hints) {
    var context = {
      shape: shape,
      newBounds: newBounds,
      minBounds: minBounds,
      hints: hints
    };

    this._commandStack.execute('shape.resize', context);
  };

  /**
   * Create space along an horizontally or vertically.
   *
   * @param {X[]} movingShapes
   * @param {X[]} resizingShapes
   * @param {Point} delta
   * @param {Direction} direction
   * @param {number} start
   */
  Modeling$1.prototype.createSpace = function(movingShapes, resizingShapes, delta, direction, start) {
    var context = {
      delta: delta,
      direction: direction,
      movingShapes: movingShapes,
      resizingShapes: resizingShapes,
      start: start
    };

    this._commandStack.execute('spaceTool', context);
  };

  /**
   * Update a connetions waypoints.
   *
   * @param {T} connection
   * @param {Point[]} newWaypoints
   * @param {ModelingHints} [hints]
   */
  Modeling$1.prototype.updateWaypoints = function(connection, newWaypoints, hints) {
    var context = {
      connection: connection,
      newWaypoints: newWaypoints,
      hints: hints || {}
    };

    this._commandStack.execute('connection.updateWaypoints', context);
  };

  /**
   * Reconnect a connections source and/or target.
   *
   * @param {T} connection
   * @param {U} source
   * @param {U} target
   * @param {Point|Point[]} dockingOrPoints
   * @param {ModelingHints} [hints]
   */
  Modeling$1.prototype.reconnect = function(connection, source, target, dockingOrPoints, hints) {
    var context = {
      connection: connection,
      newSource: source,
      newTarget: target,
      dockingOrPoints: dockingOrPoints,
      hints: hints || {}
    };

    this._commandStack.execute('connection.reconnect', context);
  };

  /**
   * Reconnect a connections source.
   *
   * @param {T} connection
   * @param {U} newSource
   * @param {Point|Point[]} dockingOrPoints
   * @param {ModelingHints} [hints]
   */
  Modeling$1.prototype.reconnectStart = function(connection, newSource, dockingOrPoints, hints) {
    if (!hints) {
      hints = {};
    }

    this.reconnect(connection, newSource, connection.target, dockingOrPoints, assign$1(hints, {
      docking: 'source'
    }));
  };

  /**
   * Reconnect a connections target.
   *
   * @param {T} connection
   * @param {U} newTarget
   * @param {Point|Point[]} dockingOrPoints
   * @param {ModelingHints} [hints]
   */
  Modeling$1.prototype.reconnectEnd = function(connection, newTarget, dockingOrPoints, hints) {
    if (!hints) {
      hints = {};
    }

    this.reconnect(connection, connection.source, newTarget, dockingOrPoints, assign$1(hints, {
      docking: 'target'
    }));
  };

  /**
   * Connect two elements.
   *
   * @param {U} source
   * @param {U} target
   * @param {Partial<T>} [attrs]
   * @param {ModelingHints} [hints]
   *
   * @return {T}
   */
  Modeling$1.prototype.connect = function(source, target, attrs, hints) {
    return this.createConnection(source, target, attrs || {}, source.parent, hints);
  };

  Modeling$1.prototype._create = function(type, attrs) {
    if (isModelElement(attrs)) {
      return attrs;
    } else {
      return this._elementFactory.create(type, attrs);
    }
  };

  /**
   * Collapse or expand a shape.
   *
   * @param {X} shape
   * @param {ModelingHints} [hints]
   */
  Modeling$1.prototype.toggleCollapse = function(shape, hints) {
    var context = {
      shape: shape,
      hints: hints || {}
    };

    this._commandStack.execute('shape.toggleCollapse', context);
  };

  inherits(Modeling, Modeling$1);

  Modeling.$inject = [
    'eventBus',
    'elementFactory',
    'elementRegistry',
    'commandStack',
    'textRenderer',
    'canvas',
    'selection',
    'graphicsFactory',
    'layouter'
  ];

  function Modeling(eventBus, elementFactory, elementRegistry, commandStack, textRenderer, canvas, selection, graphicsFactory, layouter) {
    this.canvas = canvas;
    this._textRenderer = textRenderer;
    this._graphicsFactory = graphicsFactory;
    this._elementRegistry = elementRegistry;
    this._selection = selection;
    this._layouter = layouter;

    Modeling$1.call(this, eventBus, elementFactory, commandStack);
  }

  Modeling.prototype.getHandlers = function() {
    var handlers = Modeling$1.prototype.getHandlers.call(this);

    handlers['element.updateLabel'] = UpdateLabelHandler;
    handlers['layout.connection.labels'] = LayoutConnectionLabelsHandler;
    handlers['elements.visibility'] = VisibilityHandler;

    return handlers;
  };

  Modeling.prototype.updateLabel = function(element, newLabel) {
    this._commandStack.execute('element.updateLabel', {
      element: element,
      newLabel: newLabel
    });
  };

  Modeling.prototype.connect = function(source, target, connectionType, attrs, hints) {
    return this.createConnection(source, target, assign$1({
      connectionType: connectionType
    }, attrs), source.parent);
  };

  Modeling.prototype.toggleVisibility = function(element, isVisible) {
    this.handleVisibilityContext = {
      elements: [],
      visibility: isVisible
    };
    
    if(isVisible) {
      this.showHiddenParents(element);
    }

    this.setVisibility(element, isVisible);
    
    if(isConnection(element)) {
      if(isVisible) {
        this._setVisibility(element.source, true);
        this.updateGraphics(element.source, true);
        this._setVisibility(element.target, true);
        this.updateGraphics(element.target, true);
      }
    }
    
    this.handleVisibilityContext.elements.forEach(element => {
      if(element.hidden) {
        this._selection.deselect(element);
      }
    });

    this._commandStack.execute('elements.visibility', this.handleVisibilityContext);
  };

  Modeling.prototype.showHiddenParents = function(element) {
      var parent = element.parent;
      
      if(!parent.isVisible) {
        if(isConnection(parent)) {
          this._setVisibility( parent.source, true);
          this.updateGraphics(parent.source, true);
          this._setVisibility(parent.target, true);
          this.updateGraphics(parent.target, true);
        }
        
        this._setVisibility( parent, true);
        this.updateGraphics(parent, true);
      }
  };

  Modeling.prototype.setVisibility = function(element, isVisible) {
    this._setVisibility(element, isVisible);
    
    element.hidden = !isVisible && !this._layouter.showHiddenElements;
    
    if(isShape(element)) {
      if(isLabel(element)) {
        this.setLabelVisibility(element, isVisible);
      } else {
        this.setShapeVisibility(element, isVisible);
      }
    } else {
      this.setConnectionVisibility(element, isVisible);
    }
  };

  Modeling.prototype._setVisibility = function(element, isVisible) {
    if(element.isVisible !== isVisible) {
      if(!this.handleVisibilityContext.elements.includes(element)) {
        this.handleVisibilityContext.elements.push(element);
      }
      
      element.isVisible = isVisible;
    }
  };

  Modeling.prototype.setLabelVisibility = function(label, visibility) {
    this.setChildrenVisibility(label, visibility);
    
    this.updateGraphics(label, false);
  };

  Modeling.prototype.setConnectionVisibility = function(connection, isVisible) {
    this.setChildrenVisibility(connection, isVisible);

    this.updateGraphics(connection, false);
  };

  Modeling.prototype.setShapeVisibility = function(shape, visibility) {
    this.setAttachedEdgesVisibility(shape, visibility);
    this.setChildrenVisibility(shape, visibility);

    this.updateGraphics(shape, false);
  };

  Modeling.prototype.setAttachedEdgesVisibility = function(shape, visibility) {
    this.setIncomingAttachedEdgesVisibility(shape, visibility);
    this.setOutgoingAttachedEdgesVisibility(shape, visibility);
  };

  Modeling.prototype.setIncomingAttachedEdgesVisibility = function(shape, isVisible) {
    var modeling = this;

    if('incoming' in shape) {
      shape.incoming.forEach(function(edge) {
        if(isVisible) {
          if(edge.source.isVisible) {
            modeling.setVisibility(edge, isVisible);
          }
        } else {
          modeling.setVisibility(edge, isVisible);
        }
      });
    }
  };

  Modeling.prototype.setOutgoingAttachedEdgesVisibility = function(shape, isVisible) {
    var modeling = this;

    if('outgoing' in shape) {
      shape.outgoing.forEach(function(edge) {
        if(isVisible) {
          if(edge.target.isVisible) {
            modeling.setVisibility(edge, isVisible);
          }
        } else {
          modeling.setVisibility(edge, isVisible);
        }
      });
    }
  };

  Modeling.prototype.setChildrenVisibility = function(element, visibility) {
    var modeling = this;

    if('children' in element) {
      element.children.forEach(function(child) {
        modeling.setVisibility(child, visibility);
      });
    }
  };

  Modeling.prototype.updateGraphics = function(element, updateChildren) {
    if(isConnection(element)) {
      this.updateGfx(element, 'connection');
    } else {
      this.updateGfx(element, 'shape');
    }
    
    if(updateChildren) {
      this.updateGraphicChildren(element);
    }
  };

  Modeling.prototype.updateGraphicChildren = function(element) {
    var modeling = this;
    
    if('children' in element) {
  	element.children.forEach(function(child) {
  	  modeling.updateGraphics(child, false);
  	});
    }
  };

  Modeling.prototype.updateGfx = function(element, type) {
    var gfx = this._elementRegistry.getGraphics(element, false);

    this._graphicsFactory.update(type, element, gfx);
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
    elementFactory: [ 'type', ElementFactory ],
    modeling: [ 'type', Modeling ],
    umlUpdater: [ 'type', UmlUpdater ],
    layouter: [ 'type', UmlLayouter ]
  };

  var MARKER_OK = 'drop-ok',
      MARKER_NOT_OK = 'drop-not-ok',
      MARKER_ATTACH = 'attach-ok',
      MARKER_NEW_PARENT = 'new-parent';

  /**
   * @typedef {import('../../core/Types').ElementLike} Element
   * @typedef {import('../../core/Types').ShapeLike} Shape
   *
   * @typedef {import('../../util/Types').Point} Point
   *
   * @typedef {import('../../core/Canvas').default} Canvas
   * @typedef {import('../dragging/Dragging').default} Dragging
   * @typedef {import('../../core/EventBus').default} EventBus
   * @typedef {import('../modeling/Modeling').default} Modeling
   * @typedef {import('../rules/Rules').default} Rules
   */

  var PREFIX = 'create';

  var HIGH_PRIORITY = 2000;


  /**
   * Create new elements through drag and drop.
   *
   * @param {Canvas} canvas
   * @param {Dragging} dragging
   * @param {EventBus} eventBus
   * @param {Modeling} modeling
   * @param {Rules} rules
   */
  function Create(
      canvas,
      dragging,
      eventBus,
      modeling,
      rules
  ) {
    function setMarker(element, marker) {
      [ MARKER_ATTACH, MARKER_OK, MARKER_NOT_OK, MARKER_NEW_PARENT ].forEach(function(m) {

        if (m === marker) {
          canvas.addMarker(element, m);
        } else {
          canvas.removeMarker(element, m);
        }
      });
    }

    // event handling //////////

    eventBus.on([ 'create.move', 'create.hover' ], function(event) {
      var context = event.context;
          context.elements;
          var hover = event.hover;
          context.source;
          context.hints || {};

      if (!hover) {
        context.canExecute = false;
        context.target = null;

        return;
      }

      ensureConstraints(event);

      ({
        x: event.x,
        y: event.y
      });

      var canExecute = context.canExecute = hover;

      if (hover && canExecute !== null) {
        context.target = hover;

        if (canExecute && canExecute.attach) {
          setMarker(hover, MARKER_ATTACH);
        } else {
          setMarker(hover, canExecute ? MARKER_NEW_PARENT : MARKER_NOT_OK);
        }
      }
    });

    eventBus.on([ 'create.end', 'create.out', 'create.cleanup' ], function(event) {
      var hover = event.hover;

      if (hover) {
        setMarker(hover, null);
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

    function cancel() {
      var context = dragging.context();

      if (context && context.prefix === PREFIX) {
        dragging.cancel();
      }
    }

    // cancel on <elements.changed> that is not result of <drag.end>
    eventBus.on('create.init', function() {
      eventBus.on('elements.changed', cancel);

      eventBus.once([ 'create.cancel', 'create.end' ], HIGH_PRIORITY, function() {
        eventBus.off('elements.changed', cancel);
      });
    });

    // API //////////

    this.start = function(event, elements, context) {
      if (!isArray$3(elements)) {
        elements = [ elements ];
      }

      var shape = find(elements, function(element) {
        return !isConnection(element);
      });

      if (!shape) {

        // at least one shape is required
        return;
      }

      context = assign$1({
        elements: elements,
        hints: {},
        shape: shape
      }, context || {});

      // make sure each element has x and y
      forEach$1(elements, function(element) {
        if (!isNumber(element.x)) {
          element.x = 0;
        }

        if (!isNumber(element.y)) {
          element.y = 0;
        }
      });

      var visibleElements = filter(elements, function(element) {
        return !element.hidden;
      });

      var bbox = getBBox(visibleElements);

      // center elements around cursor
      forEach$1(elements, function(element) {
        if (isConnection(element)) {
          element.waypoints = map$1(element.waypoints, function(waypoint) {
            return {
              x: waypoint.x - bbox.x - bbox.width / 2,
              y: waypoint.y - bbox.y - bbox.height / 2
            };
          });
        }

        assign$1(element, {
          x: element.x - bbox.x - bbox.width / 2,
          y: element.y - bbox.y - bbox.height / 2
        });
      });

      dragging.init(event, PREFIX, {
        cursor: 'grabbing',
        autoActivate: true,
        data: {
          shape: shape,
          elements: elements,
          context: context
        }
      });
    };
  }

  Create.$inject = [
    'canvas',
    'dragging',
    'eventBus',
    'modeling',
    'rules'
  ];

  // helpers //////////

  function ensureConstraints(event) {
    var context = event.context,
        createConstraints = context.createConstraints;

    if (!createConstraints) {
      return;
    }

    if (createConstraints.left) {
      event.x = Math.max(event.x, createConstraints.left);
    }

    if (createConstraints.right) {
      event.x = Math.min(event.x, createConstraints.right);
    }

    if (createConstraints.top) {
      event.y = Math.max(event.y, createConstraints.top);
    }

    if (createConstraints.bottom) {
      event.y = Math.min(event.y, createConstraints.bottom);
    }
  }

  var CreateModule = {
    __depends__: [
      DraggingModule
    ],
    create: [ 'type', Create ]
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
        lassoTool = this._lassoTool;
        this._textRenderer;
        this._eventBus;

    function createShapeAction(type, group, className, title) {
      function createListener(event) {
        var shape = elementFactory.createClass({
          width: 200,
          height: 100,
          name: "Name"
        });
        
        var context = {
  		  type: type
  	  };

        create.start(event, shape, context);
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
      CreateModule,
      PaletteModule,
      LassoToolModule
    ],
    __init__: [ 'paletteProvider' ],
    paletteProvider: [ 'type', PaletteProvider ]
  };

  /**
   * @type { import('didi').ModuleDeclaration }
   */
  var ModelingModule = {
    __depends__: [
      CommandModule,
      ChangeSupportModule,
      SelectionModule,
      RulesModule
    ],
    __init__: [ 'modeling' ],
    modeling: [ 'type', Modeling$1 ],
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
      assign$1(event, {
        context: {
          shape: element
        }
      });

      eventBus.fire('create.class.property', event);
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

    function getToggleVisibilityPadEntry(element) {
      if(element.isVisible) {
  	  return {
  	    group: 'admin',
  	    className: 'context-pad-icon-hide',
  	    title: 'Hide Part',
  	    action: {
  	      click: function(event, element) {
      		  contextPad.close();
  	          modeling.toggleVisibility(element, false);
            }
  	    }
  	  };
      } else {
        return {
          group: 'admin',
          className: 'context-pad-icon-show',
          title: 'Show Part',
          action: {
            click: function(event, element) {
  			contextPad.close();
              modeling.toggleVisibility(element, true);
     		  }
          }
        };
      }
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
      'toggleVisibility': getToggleVisibilityPadEntry(element)
    };
    
    if(isShape(element) && !isLabel(element)) {
      if(!('stereotypes' in element && element.stereotypes.indexOf('enumeration') != -1)) {
        assign$1(contextPadEntries, {
          'inheritance-connection': getConnectionPadEntry('inheritance', 'Inheritance Connection'),
          'composition-connection': getConnectionPadEntry('composition', 'Composition Connection'),
          'association-connection': getConnectionPadEntry('association', 'Association Connection'),
          'create-property': getCreatePropertyPadEntry()
        });
      }

      if(element.isImported) {
        assign$1(contextPadEntries, {
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
   * @typedef {import('../../core/EventBus').default} EventBus
   */

  /**
   * A basic provider that may be extended to implement modeling rules.
   *
   * Extensions should implement the init method to actually add their custom
   * modeling checks. Checks may be added via the #addRule(action, fn) method.
   *
   * @class
   *
   * @param {EventBus} eventBus
   */
  function RuleProvider(eventBus) {
    CommandInterceptor.call(this, eventBus);

    this.init();
  }

  RuleProvider.$inject = [ 'eventBus' ];

  e(RuleProvider, CommandInterceptor);


  /**
   * Adds a modeling rule for the given action, implemented through
   * a callback function.
   *
   * The callback receives a modeling specific action context
   * to perform its check. It must return `false` to disallow the
   * action from happening or `true` to allow the action. Usually returing
   * `null` denotes that a particular interaction shall be ignored.
   * By returning nothing or `undefined` you pass evaluation to lower
   * priority rules.
   *
   * @example
   *
   * ```javascript
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
   * ```
   *
   * @param {string|string[]} actions the identifier for the modeling action to check
   * @param {number} [priority] the priority at which this rule is being applied
   * @param {(any) => any} fn the callback function that performs the actual check
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

  inherits(UmlRulesProvider, RuleProvider);

  UmlRulesProvider.$inject = [ 'eventBus' ];

  function UmlRulesProvider(eventBus) {
    RuleProvider.call(this, eventBus);
  }

  UmlRulesProvider.prototype.init = function() {
    this.addRule('shape.resize', function(context) {
      var shape = context.shape;

      return (isShape(shape) && !isLabel(shape));
    });

    this.addRule('elements.move', function(context) {
      var shapes = context.shapes;

      if(shapes.some(function(shape) {
        return isLabel(shape);
      })) {
        return false;
      }

      if(isLabel(context.target) || isShape(context.target) && !isRoot(context.target)) {
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

        set('grab');
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

  function updateVisibility(parentGfx, element) {
    var groupElement = parentGfx.closest('.djs-group');
    
    if(element.parent && element.parent.isVisible) {
  	  if(element.isVisible) {
  		groupElement.style.removeProperty('opacity');
  	  } else {
  		groupElement.style.setProperty('opacity', 0.3);
  	  }
    } else {
        groupElement.style.removeProperty('opacity');
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
    
    updateVisibility(parentGfx, element);

    return rectangle;
  }
  function drawLabel(parentGfx, element, textRenderer) {
    var text = drawText(parentGfx, element.businessObject + '', getGeneralLabelStyle(), textRenderer);
    
    updateVisibility(parentGfx, element);
    
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
    return assign$1(getGeneralLabelStyle(), {
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
    
    updateVisibility(parentGfx, element);
    
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

    var options = assign$1(options, this._style);

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

      assign$1(attrs, getEdgeLabels(edge));

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
      attr(rect, assign$1({}, this.FRAME_STYLE, attrs || {}));
    } else {
      attr(rect, assign$1({}, this.SHAPE_STYLE, attrs || {}));
    }

    append(visuals, rect);

    return rect;
  };

  /**
   * @private
   */
  DefaultRenderer.prototype.drawConnection = function drawConnection(visuals, connection, attrs) {

    var line = createLine(connection.waypoints, assign$1({}, this.CONNECTION_STYLE, attrs || {}));
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

      return assign$1(attrs, { 'class': className });
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

      if (!isArray$3(traits) && !additionalAttrs) {
        additionalAttrs = traits;
        traits = [];
      }

      var attrs = reduce(traits, function(attrs, t) {
        return assign$1(attrs, defaultTraits[t] || {});
      }, {});

      return additionalAttrs ? assign$1(attrs, additionalAttrs) : attrs;
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
      if (!isArray$3(traits)) {
        defaultStyles = traits;
        traits = [];
      }

      return self.style(traits || [], assign$1({}, defaultStyles, custom || {}));
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

    options = assign$1({}, { width: '100%', height: '100%' }, options);

    const container = options.container || document.body;

    // create a <div> around the svg element with the respective size
    // this way we can always get the correct container size
    // (this is impossible for <svg> elements at the moment)
    const parent = document.createElement('div');
    parent.setAttribute('class', 'djs-container djs-parent');

    assign(parent, {
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
      this._viewboxChanged = debounce(bind$2(this._viewboxChanged, this), 300);
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

    forEach$1([ container.gfx, container.secondaryGfx ], function(gfx) {
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
        transform,
        scale,
        x, y;

    if (!box) {

      // compute the inner box based on the
      // diagrams active layer. This allows us to exclude
      // external components, such as overlays

      activeLayer = this._rootElement ? this.getActiveLayer() : null;
      innerBox = activeLayer && activeLayer.getBBox() || {};

      transform = transform$1(viewport);
      matrix = transform ? transform.matrix : createMatrix();
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

        transform$1(viewport, matrix);
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
        delta = assign$1({ dx: 0, dy: 0 }, delta || {});

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
      centerPoint = assign$1(point, center);

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

    events = isArray$3(events) ? events : [ events ];

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

    events = isArray$3(events) ? events : [ events ];

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
    assign$1(this, data || {});
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
    forEach$1(parents, function(parent) {

      var children = parent.children;

      if (!children) {
        return;
      }

      var childrenGfx = self._getChildrenContainer(parent);

      forEach$1(children.slice().reverse(), function(child) {
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
    elementFactory: [ 'type', ElementFactory$1 ],
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

    assign$1(container.style, {
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

  inherits(Modeler, Viewer);

  function Modeler(options) {
    Viewer.call(this, options);
  }

  Modeler.prototype._modelingModules = [
    SelectionModule,
    MoveModule,
    OutlineModule,
    LassoToolModule,
    PaletteModule,
    CreateModule$1,
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
    ConnectionPreviewModule,
    BendpointsModule
  ];

  Modeler.prototype._modules = [].concat(
    Modeler.prototype._modules,
    Modeler.prototype._modelingModules
  );

  return Modeler;

}));
