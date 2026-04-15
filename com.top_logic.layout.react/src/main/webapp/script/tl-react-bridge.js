function X0(i, c) {
  for (var s = 0; s < c.length; s++) {
    const o = c[s];
    if (typeof o != "string" && !Array.isArray(o)) {
      for (const d in o)
        if (d !== "default" && !(d in i)) {
          const m = Object.getOwnPropertyDescriptor(o, d);
          m && Object.defineProperty(i, d, m.get ? m : {
            enumerable: !0,
            get: () => o[d]
          });
        }
    }
  }
  return Object.freeze(Object.defineProperty(i, Symbol.toStringTag, { value: "Module" }));
}
const Dv = /* @__PURE__ */ new Map();
function pb(i, c) {
  Dv.set(i, c);
}
function Nv(i) {
  return Dv.get(i);
}
function Uv(i) {
  return i && i.__esModule && Object.prototype.hasOwnProperty.call(i, "default") ? i.default : i;
}
var ho = { exports: {} }, ut = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var km;
function G0() {
  if (km) return ut;
  km = 1;
  var i = Symbol.for("react.transitional.element"), c = Symbol.for("react.portal"), s = Symbol.for("react.fragment"), o = Symbol.for("react.strict_mode"), d = Symbol.for("react.profiler"), m = Symbol.for("react.consumer"), v = Symbol.for("react.context"), p = Symbol.for("react.forward_ref"), S = Symbol.for("react.suspense"), h = Symbol.for("react.memo"), M = Symbol.for("react.lazy"), z = Symbol.for("react.activity"), N = Symbol.iterator;
  function D(g) {
    return g === null || typeof g != "object" ? null : (g = N && g[N] || g["@@iterator"], typeof g == "function" ? g : null);
  }
  var Y = {
    isMounted: function() {
      return !1;
    },
    enqueueForceUpdate: function() {
    },
    enqueueReplaceState: function() {
    },
    enqueueSetState: function() {
    }
  }, L = Object.assign, Z = {};
  function X(g, w, j) {
    this.props = g, this.context = w, this.refs = Z, this.updater = j || Y;
  }
  X.prototype.isReactComponent = {}, X.prototype.setState = function(g, w) {
    if (typeof g != "object" && typeof g != "function" && g != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, g, w, "setState");
  }, X.prototype.forceUpdate = function(g) {
    this.updater.enqueueForceUpdate(this, g, "forceUpdate");
  };
  function W() {
  }
  W.prototype = X.prototype;
  function G(g, w, j) {
    this.props = g, this.context = w, this.refs = Z, this.updater = j || Y;
  }
  var $ = G.prototype = new W();
  $.constructor = G, L($, X.prototype), $.isPureReactComponent = !0;
  var at = Array.isArray;
  function lt() {
  }
  var V = { H: null, A: null, T: null, S: null }, it = Object.prototype.hasOwnProperty;
  function zt(g, w, j) {
    var K = j.ref;
    return {
      $$typeof: i,
      type: g,
      key: w,
      ref: K !== void 0 ? K : null,
      props: j
    };
  }
  function Tt(g, w) {
    return zt(g.type, w, g.props);
  }
  function _t(g) {
    return typeof g == "object" && g !== null && g.$$typeof === i;
  }
  function dt(g) {
    var w = { "=": "=0", ":": "=2" };
    return "$" + g.replace(/[=:]/g, function(j) {
      return w[j];
    });
  }
  var Ct = /\/+/g;
  function Q(g, w) {
    return typeof g == "object" && g !== null && g.key != null ? dt("" + g.key) : w.toString(36);
  }
  function ft(g) {
    switch (g.status) {
      case "fulfilled":
        return g.value;
      case "rejected":
        throw g.reason;
      default:
        switch (typeof g.status == "string" ? g.then(lt, lt) : (g.status = "pending", g.then(
          function(w) {
            g.status === "pending" && (g.status = "fulfilled", g.value = w);
          },
          function(w) {
            g.status === "pending" && (g.status = "rejected", g.reason = w);
          }
        )), g.status) {
          case "fulfilled":
            return g.value;
          case "rejected":
            throw g.reason;
        }
    }
    throw g;
  }
  function R(g, w, j, K, I) {
    var nt = typeof g;
    (nt === "undefined" || nt === "boolean") && (g = null);
    var mt = !1;
    if (g === null) mt = !0;
    else
      switch (nt) {
        case "bigint":
        case "string":
        case "number":
          mt = !0;
          break;
        case "object":
          switch (g.$$typeof) {
            case i:
            case c:
              mt = !0;
              break;
            case M:
              return mt = g._init, R(
                mt(g._payload),
                w,
                j,
                K,
                I
              );
          }
      }
    if (mt)
      return I = I(g), mt = K === "" ? "." + Q(g, 0) : K, at(I) ? (j = "", mt != null && (j = mt.replace(Ct, "$&/") + "/"), R(I, w, j, "", function(ql) {
        return ql;
      })) : I != null && (_t(I) && (I = Tt(
        I,
        j + (I.key == null || g && g.key === I.key ? "" : ("" + I.key).replace(
          Ct,
          "$&/"
        ) + "/") + mt
      )), w.push(I)), 1;
    mt = 0;
    var Jt = K === "" ? "." : K + ":";
    if (at(g))
      for (var Ut = 0; Ut < g.length; Ut++)
        K = g[Ut], nt = Jt + Q(K, Ut), mt += R(
          K,
          w,
          j,
          nt,
          I
        );
    else if (Ut = D(g), typeof Ut == "function")
      for (g = Ut.call(g), Ut = 0; !(K = g.next()).done; )
        K = K.value, nt = Jt + Q(K, Ut++), mt += R(
          K,
          w,
          j,
          nt,
          I
        );
    else if (nt === "object") {
      if (typeof g.then == "function")
        return R(
          ft(g),
          w,
          j,
          K,
          I
        );
      throw w = String(g), Error(
        "Objects are not valid as a React child (found: " + (w === "[object Object]" ? "object with keys {" + Object.keys(g).join(", ") + "}" : w) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return mt;
  }
  function B(g, w, j) {
    if (g == null) return g;
    var K = [], I = 0;
    return R(g, K, "", "", function(nt) {
      return w.call(j, nt, I++);
    }), K;
  }
  function H(g) {
    if (g._status === -1) {
      var w = g._result;
      w = w(), w.then(
        function(j) {
          (g._status === 0 || g._status === -1) && (g._status = 1, g._result = j);
        },
        function(j) {
          (g._status === 0 || g._status === -1) && (g._status = 2, g._result = j);
        }
      ), g._status === -1 && (g._status = 0, g._result = w);
    }
    if (g._status === 1) return g._result.default;
    throw g._result;
  }
  var F = typeof reportError == "function" ? reportError : function(g) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var w = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof g == "object" && g !== null && typeof g.message == "string" ? String(g.message) : String(g),
        error: g
      });
      if (!window.dispatchEvent(w)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", g);
      return;
    }
    console.error(g);
  }, et = {
    map: B,
    forEach: function(g, w, j) {
      B(
        g,
        function() {
          w.apply(this, arguments);
        },
        j
      );
    },
    count: function(g) {
      var w = 0;
      return B(g, function() {
        w++;
      }), w;
    },
    toArray: function(g) {
      return B(g, function(w) {
        return w;
      }) || [];
    },
    only: function(g) {
      if (!_t(g))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return g;
    }
  };
  return ut.Activity = z, ut.Children = et, ut.Component = X, ut.Fragment = s, ut.Profiler = d, ut.PureComponent = G, ut.StrictMode = o, ut.Suspense = S, ut.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = V, ut.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(g) {
      return V.H.useMemoCache(g);
    }
  }, ut.cache = function(g) {
    return function() {
      return g.apply(null, arguments);
    };
  }, ut.cacheSignal = function() {
    return null;
  }, ut.cloneElement = function(g, w, j) {
    if (g == null)
      throw Error(
        "The argument must be a React element, but you passed " + g + "."
      );
    var K = L({}, g.props), I = g.key;
    if (w != null)
      for (nt in w.key !== void 0 && (I = "" + w.key), w)
        !it.call(w, nt) || nt === "key" || nt === "__self" || nt === "__source" || nt === "ref" && w.ref === void 0 || (K[nt] = w[nt]);
    var nt = arguments.length - 2;
    if (nt === 1) K.children = j;
    else if (1 < nt) {
      for (var mt = Array(nt), Jt = 0; Jt < nt; Jt++)
        mt[Jt] = arguments[Jt + 2];
      K.children = mt;
    }
    return zt(g.type, I, K);
  }, ut.createContext = function(g) {
    return g = {
      $$typeof: v,
      _currentValue: g,
      _currentValue2: g,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, g.Provider = g, g.Consumer = {
      $$typeof: m,
      _context: g
    }, g;
  }, ut.createElement = function(g, w, j) {
    var K, I = {}, nt = null;
    if (w != null)
      for (K in w.key !== void 0 && (nt = "" + w.key), w)
        it.call(w, K) && K !== "key" && K !== "__self" && K !== "__source" && (I[K] = w[K]);
    var mt = arguments.length - 2;
    if (mt === 1) I.children = j;
    else if (1 < mt) {
      for (var Jt = Array(mt), Ut = 0; Ut < mt; Ut++)
        Jt[Ut] = arguments[Ut + 2];
      I.children = Jt;
    }
    if (g && g.defaultProps)
      for (K in mt = g.defaultProps, mt)
        I[K] === void 0 && (I[K] = mt[K]);
    return zt(g, nt, I);
  }, ut.createRef = function() {
    return { current: null };
  }, ut.forwardRef = function(g) {
    return { $$typeof: p, render: g };
  }, ut.isValidElement = _t, ut.lazy = function(g) {
    return {
      $$typeof: M,
      _payload: { _status: -1, _result: g },
      _init: H
    };
  }, ut.memo = function(g, w) {
    return {
      $$typeof: h,
      type: g,
      compare: w === void 0 ? null : w
    };
  }, ut.startTransition = function(g) {
    var w = V.T, j = {};
    V.T = j;
    try {
      var K = g(), I = V.S;
      I !== null && I(j, K), typeof K == "object" && K !== null && typeof K.then == "function" && K.then(lt, F);
    } catch (nt) {
      F(nt);
    } finally {
      w !== null && j.types !== null && (w.types = j.types), V.T = w;
    }
  }, ut.unstable_useCacheRefresh = function() {
    return V.H.useCacheRefresh();
  }, ut.use = function(g) {
    return V.H.use(g);
  }, ut.useActionState = function(g, w, j) {
    return V.H.useActionState(g, w, j);
  }, ut.useCallback = function(g, w) {
    return V.H.useCallback(g, w);
  }, ut.useContext = function(g) {
    return V.H.useContext(g);
  }, ut.useDebugValue = function() {
  }, ut.useDeferredValue = function(g, w) {
    return V.H.useDeferredValue(g, w);
  }, ut.useEffect = function(g, w) {
    return V.H.useEffect(g, w);
  }, ut.useEffectEvent = function(g) {
    return V.H.useEffectEvent(g);
  }, ut.useId = function() {
    return V.H.useId();
  }, ut.useImperativeHandle = function(g, w, j) {
    return V.H.useImperativeHandle(g, w, j);
  }, ut.useInsertionEffect = function(g, w) {
    return V.H.useInsertionEffect(g, w);
  }, ut.useLayoutEffect = function(g, w) {
    return V.H.useLayoutEffect(g, w);
  }, ut.useMemo = function(g, w) {
    return V.H.useMemo(g, w);
  }, ut.useOptimistic = function(g, w) {
    return V.H.useOptimistic(g, w);
  }, ut.useReducer = function(g, w, j) {
    return V.H.useReducer(g, w, j);
  }, ut.useRef = function(g) {
    return V.H.useRef(g);
  }, ut.useState = function(g) {
    return V.H.useState(g);
  }, ut.useSyncExternalStore = function(g, w, j) {
    return V.H.useSyncExternalStore(
      g,
      w,
      j
    );
  }, ut.useTransition = function() {
    return V.H.useTransition();
  }, ut.version = "19.2.4", ut;
}
var Fm;
function qo() {
  return Fm || (Fm = 1, ho.exports = G0()), ho.exports;
}
var q = qo();
const an = /* @__PURE__ */ Uv(q), wv = /* @__PURE__ */ X0({
  __proto__: null,
  default: an
}, [q]), Lo = /* @__PURE__ */ new Map(), Mi = /* @__PURE__ */ new Set();
let Mo = !1, Yo = 0;
const Co = /* @__PURE__ */ new Set();
let Hv = "", Bv = "";
function Q0(i) {
  Hv = i;
}
function Z0(i) {
  Bv = i;
}
function qv() {
  for (const i of Co)
    i();
}
function V0(i) {
  return Co.add(i), () => Co.delete(i);
}
function K0() {
  return Yo;
}
function J0(i) {
  Mi.add(i), Mo || (Mo = !0, queueMicrotask(W0));
}
async function W0() {
  if (Mo = !1, Mi.size === 0)
    return;
  const i = Array.from(Mi);
  Mi.clear();
  try {
    const c = Hv + "react-api/i18n?keys=" + encodeURIComponent(i.join(",")) + "&windowName=" + encodeURIComponent(Bv), s = await fetch(c);
    if (!s.ok) {
      console.error("[TLReact] i18n fetch failed:", s.status);
      return;
    }
    const o = await s.json();
    for (const [d, m] of Object.entries(o))
      Lo.set(d, m);
    Yo++, qv();
  } catch (c) {
    console.error("[TLReact] i18n fetch error:", c);
  }
}
function Eb(i) {
  an.useSyncExternalStore(V0, K0);
  const c = {};
  for (const [s, o] of Object.entries(i)) {
    const d = Lo.get(s);
    d !== void 0 ? c[s] = d : (c[s] = o, J0(s));
  }
  return c;
}
function Lv() {
  Lo.clear(), Yo++, qv();
}
const un = /* @__PURE__ */ new Map();
let Wa = null;
function jo() {
  return document.body.dataset.windowName ?? "";
}
function Xi() {
  return document.body.dataset.contextPath ?? "";
}
function k0(i) {
  const c = [];
  return i.width > 0 && c.push(`width=${i.width}`), i.height > 0 && c.push(`height=${i.height}`), c.push(`resizable=${i.resizable ? "yes" : "no"}`), c.join(",");
}
function F0() {
  Wa === null && (Wa = setInterval(() => {
    for (const [i, c] of un)
      c.closed && (un.delete(i), $0(i));
    un.size === 0 && Wa !== null && (clearInterval(Wa), Wa = null);
  }, 2e3));
}
function $0(i) {
  const c = Xi(), s = jo();
  fetch(`${c}/react-api/command`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      controlId: "",
      command: "windowClosed",
      windowName: s,
      arguments: { windowId: i }
    })
  }).catch(() => {
  });
}
function I0(i) {
  const s = `${Xi()}/view/${i.windowId}/`, o = window.open(s, i.windowId, k0(i));
  o ? (un.set(i.windowId, o), F0()) : eg(i.windowId);
}
function P0(i) {
  const c = un.get(i.windowId);
  c && (c.close(), un.delete(i.windowId));
}
function tg(i) {
  const c = un.get(i.windowId);
  c && !c.closed && c.focus();
}
function eg(i) {
  const c = Xi(), s = jo();
  fetch(`${c}/react-api/command`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      controlId: "",
      command: "windowBlocked",
      windowName: s,
      arguments: { windowId: i }
    })
  }).catch(() => {
  });
}
function lg() {
  window.addEventListener("beforeunload", () => {
    const i = Xi(), c = jo();
    if (!c) return;
    const s = JSON.stringify({
      controlId: "",
      command: "windowClosed",
      windowName: c,
      arguments: { windowId: c }
    }), o = new Blob([s], { type: "application/json" });
    navigator.sendBeacon(`${i}/react-api/command`, o);
  });
}
const lu = /* @__PURE__ */ new Map();
let ka = null, $m = null, Ni = 0, yo = null;
const ng = 45e3, ag = 15e3;
function Im(i) {
  $m = i, yo && clearInterval(yo), Pm(i), yo = setInterval(() => {
    Ni > 0 && Date.now() - Ni > ng && (console.warn("[TLReact] No heartbeat received, reconnecting SSE."), Pm($m));
  }, ag);
}
function Pm(i) {
  ka && ka.close(), ka = new EventSource(i), Ni = Date.now(), Lv(), ka.onmessage = (c) => {
    Ni = Date.now();
    try {
      const s = JSON.parse(c.data);
      ug(s);
    } catch (s) {
      console.error("[TLReact] Failed to parse SSE event:", s);
    }
  }, ka.onerror = () => {
    console.warn("[TLReact] SSE connection error, will reconnect automatically.");
  };
}
function Xo(i, c) {
  let s = lu.get(i);
  s || (s = /* @__PURE__ */ new Set(), lu.set(i, s)), s.add(c);
}
function Yv(i, c) {
  const s = lu.get(i);
  s && (s.delete(c), s.size === 0 && lu.delete(i));
}
function jv(i, c) {
  const s = lu.get(i);
  if (s)
    for (const o of s)
      o(c);
}
function ug(i) {
  if (!Array.isArray(i) || i.length < 2) {
    console.warn("[TLReact] Unexpected SSE event format:", i);
    return;
  }
  const c = i[0], s = i[1];
  switch (c !== "Heartbeat" && console.log("[SSE] dispatch", c, s), c) {
    case "Heartbeat":
      break;
    case "StateEvent":
      ig(s);
      break;
    case "PatchEvent":
      cg(s);
      break;
    case "ContentReplacement":
      Ul.contentReplacement(s);
      break;
    case "ElementReplacement":
      Ul.elementReplacement(s);
      break;
    case "PropertyUpdate":
      Ul.propertyUpdate(s);
      break;
    case "CssClassUpdate":
      Ul.cssClassUpdate(s);
      break;
    case "FragmentInsertion":
      Ul.fragmentInsertion(s);
      break;
    case "RangeReplacement":
      Ul.rangeReplacement(s);
      break;
    case "JSSnipplet":
      Ul.jsSnipplet(s);
      break;
    case "FunctionCall":
      Ul.functionCall(s);
      break;
    case "I18NCacheInvalidation":
      Lv();
      break;
    case "WindowOpenEvent":
      I0(s);
      break;
    case "WindowCloseEvent":
      P0(s);
      break;
    case "WindowFocusEvent":
      tg(s);
      break;
    default:
      console.warn("[TLReact] Unknown SSE event type:", c);
  }
}
function ig(i) {
  const c = JSON.parse(i.state);
  jv(i.controlId, c);
}
function cg(i) {
  const c = JSON.parse(i.patch);
  jv(i.controlId, c);
}
const Ul = {
  contentReplacement(i) {
    const c = document.getElementById(i.elementId);
    c && (c.innerHTML = i.html);
  },
  elementReplacement(i) {
    const c = document.getElementById(i.elementId);
    c && (c.outerHTML = i.html);
  },
  propertyUpdate(i) {
    const c = document.getElementById(i.elementId);
    if (c)
      for (const s of i.properties)
        c.setAttribute(s.name, s.value);
  },
  cssClassUpdate(i) {
    const c = document.getElementById(i.elementId);
    c && (c.className = i.cssClass);
  },
  fragmentInsertion(i) {
    const c = document.getElementById(i.elementId);
    c && c.insertAdjacentHTML(i.position, i.html);
  },
  rangeReplacement(i) {
    const c = document.getElementById(i.startId), s = document.getElementById(i.stopId);
    if (c && s && c.parentNode) {
      const o = c.parentNode, d = document.createRange();
      d.setStartBefore(c), d.setEndAfter(s), d.deleteContents();
      const m = document.createElement("template");
      m.innerHTML = i.html, o.insertBefore(m.content, d.startContainer.childNodes[d.startOffset] || null);
    }
  },
  jsSnipplet(i) {
    try {
      (0, eval)(i.code);
    } catch (c) {
      console.error("[TLReact] Error executing JS snippet:", c);
    }
  },
  functionCall(i) {
    try {
      const c = JSON.parse(i.arguments), s = i.functionRef ? window[i.functionRef] : window, o = s == null ? void 0 : s[i.functionName];
      typeof o == "function" ? o.apply(s, c) : console.warn("[TLReact] Function not found:", i.functionRef + "." + i.functionName);
    } catch (c) {
      console.error("[TLReact] Error executing function call:", c);
    }
  }
};
var go = { exports: {} }, Fa = {}, bo = { exports: {} }, So = {};
/**
 * @license React
 * scheduler.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var tv;
function fg() {
  return tv || (tv = 1, (function(i) {
    function c(R, B) {
      var H = R.length;
      R.push(B);
      t: for (; 0 < H; ) {
        var F = H - 1 >>> 1, et = R[F];
        if (0 < d(et, B))
          R[F] = B, R[H] = et, H = F;
        else break t;
      }
    }
    function s(R) {
      return R.length === 0 ? null : R[0];
    }
    function o(R) {
      if (R.length === 0) return null;
      var B = R[0], H = R.pop();
      if (H !== B) {
        R[0] = H;
        t: for (var F = 0, et = R.length, g = et >>> 1; F < g; ) {
          var w = 2 * (F + 1) - 1, j = R[w], K = w + 1, I = R[K];
          if (0 > d(j, H))
            K < et && 0 > d(I, j) ? (R[F] = I, R[K] = H, F = K) : (R[F] = j, R[w] = H, F = w);
          else if (K < et && 0 > d(I, H))
            R[F] = I, R[K] = H, F = K;
          else break t;
        }
      }
      return B;
    }
    function d(R, B) {
      var H = R.sortIndex - B.sortIndex;
      return H !== 0 ? H : R.id - B.id;
    }
    if (i.unstable_now = void 0, typeof performance == "object" && typeof performance.now == "function") {
      var m = performance;
      i.unstable_now = function() {
        return m.now();
      };
    } else {
      var v = Date, p = v.now();
      i.unstable_now = function() {
        return v.now() - p;
      };
    }
    var S = [], h = [], M = 1, z = null, N = 3, D = !1, Y = !1, L = !1, Z = !1, X = typeof setTimeout == "function" ? setTimeout : null, W = typeof clearTimeout == "function" ? clearTimeout : null, G = typeof setImmediate < "u" ? setImmediate : null;
    function $(R) {
      for (var B = s(h); B !== null; ) {
        if (B.callback === null) o(h);
        else if (B.startTime <= R)
          o(h), B.sortIndex = B.expirationTime, c(S, B);
        else break;
        B = s(h);
      }
    }
    function at(R) {
      if (L = !1, $(R), !Y)
        if (s(S) !== null)
          Y = !0, lt || (lt = !0, dt());
        else {
          var B = s(h);
          B !== null && ft(at, B.startTime - R);
        }
    }
    var lt = !1, V = -1, it = 5, zt = -1;
    function Tt() {
      return Z ? !0 : !(i.unstable_now() - zt < it);
    }
    function _t() {
      if (Z = !1, lt) {
        var R = i.unstable_now();
        zt = R;
        var B = !0;
        try {
          t: {
            Y = !1, L && (L = !1, W(V), V = -1), D = !0;
            var H = N;
            try {
              e: {
                for ($(R), z = s(S); z !== null && !(z.expirationTime > R && Tt()); ) {
                  var F = z.callback;
                  if (typeof F == "function") {
                    z.callback = null, N = z.priorityLevel;
                    var et = F(
                      z.expirationTime <= R
                    );
                    if (R = i.unstable_now(), typeof et == "function") {
                      z.callback = et, $(R), B = !0;
                      break e;
                    }
                    z === s(S) && o(S), $(R);
                  } else o(S);
                  z = s(S);
                }
                if (z !== null) B = !0;
                else {
                  var g = s(h);
                  g !== null && ft(
                    at,
                    g.startTime - R
                  ), B = !1;
                }
              }
              break t;
            } finally {
              z = null, N = H, D = !1;
            }
            B = void 0;
          }
        } finally {
          B ? dt() : lt = !1;
        }
      }
    }
    var dt;
    if (typeof G == "function")
      dt = function() {
        G(_t);
      };
    else if (typeof MessageChannel < "u") {
      var Ct = new MessageChannel(), Q = Ct.port2;
      Ct.port1.onmessage = _t, dt = function() {
        Q.postMessage(null);
      };
    } else
      dt = function() {
        X(_t, 0);
      };
    function ft(R, B) {
      V = X(function() {
        R(i.unstable_now());
      }, B);
    }
    i.unstable_IdlePriority = 5, i.unstable_ImmediatePriority = 1, i.unstable_LowPriority = 4, i.unstable_NormalPriority = 3, i.unstable_Profiling = null, i.unstable_UserBlockingPriority = 2, i.unstable_cancelCallback = function(R) {
      R.callback = null;
    }, i.unstable_forceFrameRate = function(R) {
      0 > R || 125 < R ? console.error(
        "forceFrameRate takes a positive int between 0 and 125, forcing frame rates higher than 125 fps is not supported"
      ) : it = 0 < R ? Math.floor(1e3 / R) : 5;
    }, i.unstable_getCurrentPriorityLevel = function() {
      return N;
    }, i.unstable_next = function(R) {
      switch (N) {
        case 1:
        case 2:
        case 3:
          var B = 3;
          break;
        default:
          B = N;
      }
      var H = N;
      N = B;
      try {
        return R();
      } finally {
        N = H;
      }
    }, i.unstable_requestPaint = function() {
      Z = !0;
    }, i.unstable_runWithPriority = function(R, B) {
      switch (R) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
          break;
        default:
          R = 3;
      }
      var H = N;
      N = R;
      try {
        return B();
      } finally {
        N = H;
      }
    }, i.unstable_scheduleCallback = function(R, B, H) {
      var F = i.unstable_now();
      switch (typeof H == "object" && H !== null ? (H = H.delay, H = typeof H == "number" && 0 < H ? F + H : F) : H = F, R) {
        case 1:
          var et = -1;
          break;
        case 2:
          et = 250;
          break;
        case 5:
          et = 1073741823;
          break;
        case 4:
          et = 1e4;
          break;
        default:
          et = 5e3;
      }
      return et = H + et, R = {
        id: M++,
        callback: B,
        priorityLevel: R,
        startTime: H,
        expirationTime: et,
        sortIndex: -1
      }, H > F ? (R.sortIndex = H, c(h, R), s(S) === null && R === s(h) && (L ? (W(V), V = -1) : L = !0, ft(at, H - F))) : (R.sortIndex = et, c(S, R), Y || D || (Y = !0, lt || (lt = !0, dt()))), R;
    }, i.unstable_shouldYield = Tt, i.unstable_wrapCallback = function(R) {
      var B = N;
      return function() {
        var H = N;
        N = B;
        try {
          return R.apply(this, arguments);
        } finally {
          N = H;
        }
      };
    };
  })(So)), So;
}
var ev;
function og() {
  return ev || (ev = 1, bo.exports = fg()), bo.exports;
}
var po = { exports: {} }, Pt = {};
/**
 * @license React
 * react-dom.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var lv;
function sg() {
  if (lv) return Pt;
  lv = 1;
  var i = qo();
  function c(S) {
    var h = "https://react.dev/errors/" + S;
    if (1 < arguments.length) {
      h += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var M = 2; M < arguments.length; M++)
        h += "&args[]=" + encodeURIComponent(arguments[M]);
    }
    return "Minified React error #" + S + "; visit " + h + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function s() {
  }
  var o = {
    d: {
      f: s,
      r: function() {
        throw Error(c(522));
      },
      D: s,
      C: s,
      L: s,
      m: s,
      X: s,
      S: s,
      M: s
    },
    p: 0,
    findDOMNode: null
  }, d = Symbol.for("react.portal");
  function m(S, h, M) {
    var z = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: d,
      key: z == null ? null : "" + z,
      children: S,
      containerInfo: h,
      implementation: M
    };
  }
  var v = i.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function p(S, h) {
    if (S === "font") return "";
    if (typeof h == "string")
      return h === "use-credentials" ? h : "";
  }
  return Pt.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = o, Pt.createPortal = function(S, h) {
    var M = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!h || h.nodeType !== 1 && h.nodeType !== 9 && h.nodeType !== 11)
      throw Error(c(299));
    return m(S, h, null, M);
  }, Pt.flushSync = function(S) {
    var h = v.T, M = o.p;
    try {
      if (v.T = null, o.p = 2, S) return S();
    } finally {
      v.T = h, o.p = M, o.d.f();
    }
  }, Pt.preconnect = function(S, h) {
    typeof S == "string" && (h ? (h = h.crossOrigin, h = typeof h == "string" ? h === "use-credentials" ? h : "" : void 0) : h = null, o.d.C(S, h));
  }, Pt.prefetchDNS = function(S) {
    typeof S == "string" && o.d.D(S);
  }, Pt.preinit = function(S, h) {
    if (typeof S == "string" && h && typeof h.as == "string") {
      var M = h.as, z = p(M, h.crossOrigin), N = typeof h.integrity == "string" ? h.integrity : void 0, D = typeof h.fetchPriority == "string" ? h.fetchPriority : void 0;
      M === "style" ? o.d.S(
        S,
        typeof h.precedence == "string" ? h.precedence : void 0,
        {
          crossOrigin: z,
          integrity: N,
          fetchPriority: D
        }
      ) : M === "script" && o.d.X(S, {
        crossOrigin: z,
        integrity: N,
        fetchPriority: D,
        nonce: typeof h.nonce == "string" ? h.nonce : void 0
      });
    }
  }, Pt.preinitModule = function(S, h) {
    if (typeof S == "string")
      if (typeof h == "object" && h !== null) {
        if (h.as == null || h.as === "script") {
          var M = p(
            h.as,
            h.crossOrigin
          );
          o.d.M(S, {
            crossOrigin: M,
            integrity: typeof h.integrity == "string" ? h.integrity : void 0,
            nonce: typeof h.nonce == "string" ? h.nonce : void 0
          });
        }
      } else h == null && o.d.M(S);
  }, Pt.preload = function(S, h) {
    if (typeof S == "string" && typeof h == "object" && h !== null && typeof h.as == "string") {
      var M = h.as, z = p(M, h.crossOrigin);
      o.d.L(S, M, {
        crossOrigin: z,
        integrity: typeof h.integrity == "string" ? h.integrity : void 0,
        nonce: typeof h.nonce == "string" ? h.nonce : void 0,
        type: typeof h.type == "string" ? h.type : void 0,
        fetchPriority: typeof h.fetchPriority == "string" ? h.fetchPriority : void 0,
        referrerPolicy: typeof h.referrerPolicy == "string" ? h.referrerPolicy : void 0,
        imageSrcSet: typeof h.imageSrcSet == "string" ? h.imageSrcSet : void 0,
        imageSizes: typeof h.imageSizes == "string" ? h.imageSizes : void 0,
        media: typeof h.media == "string" ? h.media : void 0
      });
    }
  }, Pt.preloadModule = function(S, h) {
    if (typeof S == "string")
      if (h) {
        var M = p(h.as, h.crossOrigin);
        o.d.m(S, {
          as: typeof h.as == "string" && h.as !== "script" ? h.as : void 0,
          crossOrigin: M,
          integrity: typeof h.integrity == "string" ? h.integrity : void 0
        });
      } else o.d.m(S);
  }, Pt.requestFormReset = function(S) {
    o.d.r(S);
  }, Pt.unstable_batchedUpdates = function(S, h) {
    return S(h);
  }, Pt.useFormState = function(S, h, M) {
    return v.H.useFormState(S, h, M);
  }, Pt.useFormStatus = function() {
    return v.H.useHostTransitionStatus();
  }, Pt.version = "19.2.4", Pt;
}
var nv;
function Xv() {
  if (nv) return po.exports;
  nv = 1;
  function i() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(i);
      } catch (c) {
        console.error(c);
      }
  }
  return i(), po.exports = sg(), po.exports;
}
/**
 * @license React
 * react-dom-client.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var av;
function rg() {
  if (av) return Fa;
  av = 1;
  var i = og(), c = qo(), s = Xv();
  function o(t) {
    var e = "https://react.dev/errors/" + t;
    if (1 < arguments.length) {
      e += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var l = 2; l < arguments.length; l++)
        e += "&args[]=" + encodeURIComponent(arguments[l]);
    }
    return "Minified React error #" + t + "; visit " + e + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function d(t) {
    return !(!t || t.nodeType !== 1 && t.nodeType !== 9 && t.nodeType !== 11);
  }
  function m(t) {
    var e = t, l = t;
    if (t.alternate) for (; e.return; ) e = e.return;
    else {
      t = e;
      do
        e = t, (e.flags & 4098) !== 0 && (l = e.return), t = e.return;
      while (t);
    }
    return e.tag === 3 ? l : null;
  }
  function v(t) {
    if (t.tag === 13) {
      var e = t.memoizedState;
      if (e === null && (t = t.alternate, t !== null && (e = t.memoizedState)), e !== null) return e.dehydrated;
    }
    return null;
  }
  function p(t) {
    if (t.tag === 31) {
      var e = t.memoizedState;
      if (e === null && (t = t.alternate, t !== null && (e = t.memoizedState)), e !== null) return e.dehydrated;
    }
    return null;
  }
  function S(t) {
    if (m(t) !== t)
      throw Error(o(188));
  }
  function h(t) {
    var e = t.alternate;
    if (!e) {
      if (e = m(t), e === null) throw Error(o(188));
      return e !== t ? null : t;
    }
    for (var l = t, n = e; ; ) {
      var a = l.return;
      if (a === null) break;
      var u = a.alternate;
      if (u === null) {
        if (n = a.return, n !== null) {
          l = n;
          continue;
        }
        break;
      }
      if (a.child === u.child) {
        for (u = a.child; u; ) {
          if (u === l) return S(a), t;
          if (u === n) return S(a), e;
          u = u.sibling;
        }
        throw Error(o(188));
      }
      if (l.return !== n.return) l = a, n = u;
      else {
        for (var f = !1, r = a.child; r; ) {
          if (r === l) {
            f = !0, l = a, n = u;
            break;
          }
          if (r === n) {
            f = !0, n = a, l = u;
            break;
          }
          r = r.sibling;
        }
        if (!f) {
          for (r = u.child; r; ) {
            if (r === l) {
              f = !0, l = u, n = a;
              break;
            }
            if (r === n) {
              f = !0, n = u, l = a;
              break;
            }
            r = r.sibling;
          }
          if (!f) throw Error(o(189));
        }
      }
      if (l.alternate !== n) throw Error(o(190));
    }
    if (l.tag !== 3) throw Error(o(188));
    return l.stateNode.current === l ? t : e;
  }
  function M(t) {
    var e = t.tag;
    if (e === 5 || e === 26 || e === 27 || e === 6) return t;
    for (t = t.child; t !== null; ) {
      if (e = M(t), e !== null) return e;
      t = t.sibling;
    }
    return null;
  }
  var z = Object.assign, N = Symbol.for("react.element"), D = Symbol.for("react.transitional.element"), Y = Symbol.for("react.portal"), L = Symbol.for("react.fragment"), Z = Symbol.for("react.strict_mode"), X = Symbol.for("react.profiler"), W = Symbol.for("react.consumer"), G = Symbol.for("react.context"), $ = Symbol.for("react.forward_ref"), at = Symbol.for("react.suspense"), lt = Symbol.for("react.suspense_list"), V = Symbol.for("react.memo"), it = Symbol.for("react.lazy"), zt = Symbol.for("react.activity"), Tt = Symbol.for("react.memo_cache_sentinel"), _t = Symbol.iterator;
  function dt(t) {
    return t === null || typeof t != "object" ? null : (t = _t && t[_t] || t["@@iterator"], typeof t == "function" ? t : null);
  }
  var Ct = Symbol.for("react.client.reference");
  function Q(t) {
    if (t == null) return null;
    if (typeof t == "function")
      return t.$$typeof === Ct ? null : t.displayName || t.name || null;
    if (typeof t == "string") return t;
    switch (t) {
      case L:
        return "Fragment";
      case X:
        return "Profiler";
      case Z:
        return "StrictMode";
      case at:
        return "Suspense";
      case lt:
        return "SuspenseList";
      case zt:
        return "Activity";
    }
    if (typeof t == "object")
      switch (t.$$typeof) {
        case Y:
          return "Portal";
        case G:
          return t.displayName || "Context";
        case W:
          return (t._context.displayName || "Context") + ".Consumer";
        case $:
          var e = t.render;
          return t = t.displayName, t || (t = e.displayName || e.name || "", t = t !== "" ? "ForwardRef(" + t + ")" : "ForwardRef"), t;
        case V:
          return e = t.displayName || null, e !== null ? e : Q(t.type) || "Memo";
        case it:
          e = t._payload, t = t._init;
          try {
            return Q(t(e));
          } catch {
          }
      }
    return null;
  }
  var ft = Array.isArray, R = c.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE, B = s.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE, H = {
    pending: !1,
    data: null,
    method: null,
    action: null
  }, F = [], et = -1;
  function g(t) {
    return { current: t };
  }
  function w(t) {
    0 > et || (t.current = F[et], F[et] = null, et--);
  }
  function j(t, e) {
    et++, F[et] = t.current, t.current = e;
  }
  var K = g(null), I = g(null), nt = g(null), mt = g(null);
  function Jt(t, e) {
    switch (j(nt, e), j(I, t), j(K, null), e.nodeType) {
      case 9:
      case 11:
        t = (t = e.documentElement) && (t = t.namespaceURI) ? bm(t) : 0;
        break;
      default:
        if (t = e.tagName, e = e.namespaceURI)
          e = bm(e), t = Sm(e, t);
        else
          switch (t) {
            case "svg":
              t = 1;
              break;
            case "math":
              t = 2;
              break;
            default:
              t = 0;
          }
    }
    w(K), j(K, t);
  }
  function Ut() {
    w(K), w(I), w(nt);
  }
  function ql(t) {
    t.memoizedState !== null && j(mt, t);
    var e = K.current, l = Sm(e, t.type);
    e !== l && (j(I, t), j(K, l));
  }
  function dn(t) {
    I.current === t && (w(K), w(I)), mt.current === t && (w(mt), Za._currentValue = H);
  }
  var ea, fu;
  function Xe(t) {
    if (ea === void 0)
      try {
        throw Error();
      } catch (l) {
        var e = l.stack.trim().match(/\n( *(at )?)/);
        ea = e && e[1] || "", fu = -1 < l.stack.indexOf(`
    at`) ? " (<anonymous>)" : -1 < l.stack.indexOf("@") ? "@unknown:0:0" : "";
      }
    return `
` + ea + t + fu;
  }
  var Fi = !1;
  function $i(t, e) {
    if (!t || Fi) return "";
    Fi = !0;
    var l = Error.prepareStackTrace;
    Error.prepareStackTrace = void 0;
    try {
      var n = {
        DetermineComponentFrameRoot: function() {
          try {
            if (e) {
              var U = function() {
                throw Error();
              };
              if (Object.defineProperty(U.prototype, "props", {
                set: function() {
                  throw Error();
                }
              }), typeof Reflect == "object" && Reflect.construct) {
                try {
                  Reflect.construct(U, []);
                } catch (_) {
                  var O = _;
                }
                Reflect.construct(t, [], U);
              } else {
                try {
                  U.call();
                } catch (_) {
                  O = _;
                }
                t.call(U.prototype);
              }
            } else {
              try {
                throw Error();
              } catch (_) {
                O = _;
              }
              (U = t()) && typeof U.catch == "function" && U.catch(function() {
              });
            }
          } catch (_) {
            if (_ && O && typeof _.stack == "string")
              return [_.stack, O.stack];
          }
          return [null, null];
        }
      };
      n.DetermineComponentFrameRoot.displayName = "DetermineComponentFrameRoot";
      var a = Object.getOwnPropertyDescriptor(
        n.DetermineComponentFrameRoot,
        "name"
      );
      a && a.configurable && Object.defineProperty(
        n.DetermineComponentFrameRoot,
        "name",
        { value: "DetermineComponentFrameRoot" }
      );
      var u = n.DetermineComponentFrameRoot(), f = u[0], r = u[1];
      if (f && r) {
        var y = f.split(`
`), A = r.split(`
`);
        for (a = n = 0; n < y.length && !y[n].includes("DetermineComponentFrameRoot"); )
          n++;
        for (; a < A.length && !A[a].includes(
          "DetermineComponentFrameRoot"
        ); )
          a++;
        if (n === y.length || a === A.length)
          for (n = y.length - 1, a = A.length - 1; 1 <= n && 0 <= a && y[n] !== A[a]; )
            a--;
        for (; 1 <= n && 0 <= a; n--, a--)
          if (y[n] !== A[a]) {
            if (n !== 1 || a !== 1)
              do
                if (n--, a--, 0 > a || y[n] !== A[a]) {
                  var C = `
` + y[n].replace(" at new ", " at ");
                  return t.displayName && C.includes("<anonymous>") && (C = C.replace("<anonymous>", t.displayName)), C;
                }
              while (1 <= n && 0 <= a);
            break;
          }
      }
    } finally {
      Fi = !1, Error.prepareStackTrace = l;
    }
    return (l = t ? t.displayName || t.name : "") ? Xe(l) : "";
  }
  function bh(t, e) {
    switch (t.tag) {
      case 26:
      case 27:
      case 5:
        return Xe(t.type);
      case 16:
        return Xe("Lazy");
      case 13:
        return t.child !== e && e !== null ? Xe("Suspense Fallback") : Xe("Suspense");
      case 19:
        return Xe("SuspenseList");
      case 0:
      case 15:
        return $i(t.type, !1);
      case 11:
        return $i(t.type.render, !1);
      case 1:
        return $i(t.type, !0);
      case 31:
        return Xe("Activity");
      default:
        return "";
    }
  }
  function ko(t) {
    try {
      var e = "", l = null;
      do
        e += bh(t, l), l = t, t = t.return;
      while (t);
      return e;
    } catch (n) {
      return `
Error generating stack: ` + n.message + `
` + n.stack;
    }
  }
  var Ii = Object.prototype.hasOwnProperty, Pi = i.unstable_scheduleCallback, tc = i.unstable_cancelCallback, Sh = i.unstable_shouldYield, ph = i.unstable_requestPaint, fe = i.unstable_now, Eh = i.unstable_getCurrentPriorityLevel, Fo = i.unstable_ImmediatePriority, $o = i.unstable_UserBlockingPriority, ou = i.unstable_NormalPriority, Th = i.unstable_LowPriority, Io = i.unstable_IdlePriority, Ah = i.log, Rh = i.unstable_setDisableYieldValue, la = null, oe = null;
  function fl(t) {
    if (typeof Ah == "function" && Rh(t), oe && typeof oe.setStrictMode == "function")
      try {
        oe.setStrictMode(la, t);
      } catch {
      }
  }
  var se = Math.clz32 ? Math.clz32 : _h, Oh = Math.log, zh = Math.LN2;
  function _h(t) {
    return t >>>= 0, t === 0 ? 32 : 31 - (Oh(t) / zh | 0) | 0;
  }
  var su = 256, ru = 262144, du = 4194304;
  function Ll(t) {
    var e = t & 42;
    if (e !== 0) return e;
    switch (t & -t) {
      case 1:
        return 1;
      case 2:
        return 2;
      case 4:
        return 4;
      case 8:
        return 8;
      case 16:
        return 16;
      case 32:
        return 32;
      case 64:
        return 64;
      case 128:
        return 128;
      case 256:
      case 512:
      case 1024:
      case 2048:
      case 4096:
      case 8192:
      case 16384:
      case 32768:
      case 65536:
      case 131072:
        return t & 261888;
      case 262144:
      case 524288:
      case 1048576:
      case 2097152:
        return t & 3932160;
      case 4194304:
      case 8388608:
      case 16777216:
      case 33554432:
        return t & 62914560;
      case 67108864:
        return 67108864;
      case 134217728:
        return 134217728;
      case 268435456:
        return 268435456;
      case 536870912:
        return 536870912;
      case 1073741824:
        return 0;
      default:
        return t;
    }
  }
  function mu(t, e, l) {
    var n = t.pendingLanes;
    if (n === 0) return 0;
    var a = 0, u = t.suspendedLanes, f = t.pingedLanes;
    t = t.warmLanes;
    var r = n & 134217727;
    return r !== 0 ? (n = r & ~u, n !== 0 ? a = Ll(n) : (f &= r, f !== 0 ? a = Ll(f) : l || (l = r & ~t, l !== 0 && (a = Ll(l))))) : (r = n & ~u, r !== 0 ? a = Ll(r) : f !== 0 ? a = Ll(f) : l || (l = n & ~t, l !== 0 && (a = Ll(l)))), a === 0 ? 0 : e !== 0 && e !== a && (e & u) === 0 && (u = a & -a, l = e & -e, u >= l || u === 32 && (l & 4194048) !== 0) ? e : a;
  }
  function na(t, e) {
    return (t.pendingLanes & ~(t.suspendedLanes & ~t.pingedLanes) & e) === 0;
  }
  function Mh(t, e) {
    switch (t) {
      case 1:
      case 2:
      case 4:
      case 8:
      case 64:
        return e + 250;
      case 16:
      case 32:
      case 128:
      case 256:
      case 512:
      case 1024:
      case 2048:
      case 4096:
      case 8192:
      case 16384:
      case 32768:
      case 65536:
      case 131072:
      case 262144:
      case 524288:
      case 1048576:
      case 2097152:
        return e + 5e3;
      case 4194304:
      case 8388608:
      case 16777216:
      case 33554432:
        return -1;
      case 67108864:
      case 134217728:
      case 268435456:
      case 536870912:
      case 1073741824:
        return -1;
      default:
        return -1;
    }
  }
  function Po() {
    var t = du;
    return du <<= 1, (du & 62914560) === 0 && (du = 4194304), t;
  }
  function ec(t) {
    for (var e = [], l = 0; 31 > l; l++) e.push(t);
    return e;
  }
  function aa(t, e) {
    t.pendingLanes |= e, e !== 268435456 && (t.suspendedLanes = 0, t.pingedLanes = 0, t.warmLanes = 0);
  }
  function Ch(t, e, l, n, a, u) {
    var f = t.pendingLanes;
    t.pendingLanes = l, t.suspendedLanes = 0, t.pingedLanes = 0, t.warmLanes = 0, t.expiredLanes &= l, t.entangledLanes &= l, t.errorRecoveryDisabledLanes &= l, t.shellSuspendCounter = 0;
    var r = t.entanglements, y = t.expirationTimes, A = t.hiddenUpdates;
    for (l = f & ~l; 0 < l; ) {
      var C = 31 - se(l), U = 1 << C;
      r[C] = 0, y[C] = -1;
      var O = A[C];
      if (O !== null)
        for (A[C] = null, C = 0; C < O.length; C++) {
          var _ = O[C];
          _ !== null && (_.lane &= -536870913);
        }
      l &= ~U;
    }
    n !== 0 && ts(t, n, 0), u !== 0 && a === 0 && t.tag !== 0 && (t.suspendedLanes |= u & ~(f & ~e));
  }
  function ts(t, e, l) {
    t.pendingLanes |= e, t.suspendedLanes &= ~e;
    var n = 31 - se(e);
    t.entangledLanes |= e, t.entanglements[n] = t.entanglements[n] | 1073741824 | l & 261930;
  }
  function es(t, e) {
    var l = t.entangledLanes |= e;
    for (t = t.entanglements; l; ) {
      var n = 31 - se(l), a = 1 << n;
      a & e | t[n] & e && (t[n] |= e), l &= ~a;
    }
  }
  function ls(t, e) {
    var l = e & -e;
    return l = (l & 42) !== 0 ? 1 : lc(l), (l & (t.suspendedLanes | e)) !== 0 ? 0 : l;
  }
  function lc(t) {
    switch (t) {
      case 2:
        t = 1;
        break;
      case 8:
        t = 4;
        break;
      case 32:
        t = 16;
        break;
      case 256:
      case 512:
      case 1024:
      case 2048:
      case 4096:
      case 8192:
      case 16384:
      case 32768:
      case 65536:
      case 131072:
      case 262144:
      case 524288:
      case 1048576:
      case 2097152:
      case 4194304:
      case 8388608:
      case 16777216:
      case 33554432:
        t = 128;
        break;
      case 268435456:
        t = 134217728;
        break;
      default:
        t = 0;
    }
    return t;
  }
  function nc(t) {
    return t &= -t, 2 < t ? 8 < t ? (t & 134217727) !== 0 ? 32 : 268435456 : 8 : 2;
  }
  function ns() {
    var t = B.p;
    return t !== 0 ? t : (t = window.event, t === void 0 ? 32 : Gm(t.type));
  }
  function as(t, e) {
    var l = B.p;
    try {
      return B.p = t, e();
    } finally {
      B.p = l;
    }
  }
  var ol = Math.random().toString(36).slice(2), Wt = "__reactFiber$" + ol, ee = "__reactProps$" + ol, mn = "__reactContainer$" + ol, ac = "__reactEvents$" + ol, xh = "__reactListeners$" + ol, Dh = "__reactHandles$" + ol, us = "__reactResources$" + ol, ua = "__reactMarker$" + ol;
  function uc(t) {
    delete t[Wt], delete t[ee], delete t[ac], delete t[xh], delete t[Dh];
  }
  function vn(t) {
    var e = t[Wt];
    if (e) return e;
    for (var l = t.parentNode; l; ) {
      if (e = l[mn] || l[Wt]) {
        if (l = e.alternate, e.child !== null || l !== null && l.child !== null)
          for (t = zm(t); t !== null; ) {
            if (l = t[Wt]) return l;
            t = zm(t);
          }
        return e;
      }
      t = l, l = t.parentNode;
    }
    return null;
  }
  function hn(t) {
    if (t = t[Wt] || t[mn]) {
      var e = t.tag;
      if (e === 5 || e === 6 || e === 13 || e === 31 || e === 26 || e === 27 || e === 3)
        return t;
    }
    return null;
  }
  function ia(t) {
    var e = t.tag;
    if (e === 5 || e === 26 || e === 27 || e === 6) return t.stateNode;
    throw Error(o(33));
  }
  function yn(t) {
    var e = t[us];
    return e || (e = t[us] = { hoistableStyles: /* @__PURE__ */ new Map(), hoistableScripts: /* @__PURE__ */ new Map() }), e;
  }
  function Vt(t) {
    t[ua] = !0;
  }
  var is = /* @__PURE__ */ new Set(), cs = {};
  function Yl(t, e) {
    gn(t, e), gn(t + "Capture", e);
  }
  function gn(t, e) {
    for (cs[t] = e, t = 0; t < e.length; t++)
      is.add(e[t]);
  }
  var Nh = RegExp(
    "^[:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD][:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD\\-.0-9\\u00B7\\u0300-\\u036F\\u203F-\\u2040]*$"
  ), fs = {}, os = {};
  function Uh(t) {
    return Ii.call(os, t) ? !0 : Ii.call(fs, t) ? !1 : Nh.test(t) ? os[t] = !0 : (fs[t] = !0, !1);
  }
  function vu(t, e, l) {
    if (Uh(e))
      if (l === null) t.removeAttribute(e);
      else {
        switch (typeof l) {
          case "undefined":
          case "function":
          case "symbol":
            t.removeAttribute(e);
            return;
          case "boolean":
            var n = e.toLowerCase().slice(0, 5);
            if (n !== "data-" && n !== "aria-") {
              t.removeAttribute(e);
              return;
            }
        }
        t.setAttribute(e, "" + l);
      }
  }
  function hu(t, e, l) {
    if (l === null) t.removeAttribute(e);
    else {
      switch (typeof l) {
        case "undefined":
        case "function":
        case "symbol":
        case "boolean":
          t.removeAttribute(e);
          return;
      }
      t.setAttribute(e, "" + l);
    }
  }
  function Ge(t, e, l, n) {
    if (n === null) t.removeAttribute(l);
    else {
      switch (typeof n) {
        case "undefined":
        case "function":
        case "symbol":
        case "boolean":
          t.removeAttribute(l);
          return;
      }
      t.setAttributeNS(e, l, "" + n);
    }
  }
  function pe(t) {
    switch (typeof t) {
      case "bigint":
      case "boolean":
      case "number":
      case "string":
      case "undefined":
        return t;
      case "object":
        return t;
      default:
        return "";
    }
  }
  function ss(t) {
    var e = t.type;
    return (t = t.nodeName) && t.toLowerCase() === "input" && (e === "checkbox" || e === "radio");
  }
  function wh(t, e, l) {
    var n = Object.getOwnPropertyDescriptor(
      t.constructor.prototype,
      e
    );
    if (!t.hasOwnProperty(e) && typeof n < "u" && typeof n.get == "function" && typeof n.set == "function") {
      var a = n.get, u = n.set;
      return Object.defineProperty(t, e, {
        configurable: !0,
        get: function() {
          return a.call(this);
        },
        set: function(f) {
          l = "" + f, u.call(this, f);
        }
      }), Object.defineProperty(t, e, {
        enumerable: n.enumerable
      }), {
        getValue: function() {
          return l;
        },
        setValue: function(f) {
          l = "" + f;
        },
        stopTracking: function() {
          t._valueTracker = null, delete t[e];
        }
      };
    }
  }
  function ic(t) {
    if (!t._valueTracker) {
      var e = ss(t) ? "checked" : "value";
      t._valueTracker = wh(
        t,
        e,
        "" + t[e]
      );
    }
  }
  function rs(t) {
    if (!t) return !1;
    var e = t._valueTracker;
    if (!e) return !0;
    var l = e.getValue(), n = "";
    return t && (n = ss(t) ? t.checked ? "true" : "false" : t.value), t = n, t !== l ? (e.setValue(t), !0) : !1;
  }
  function yu(t) {
    if (t = t || (typeof document < "u" ? document : void 0), typeof t > "u") return null;
    try {
      return t.activeElement || t.body;
    } catch {
      return t.body;
    }
  }
  var Hh = /[\n"\\]/g;
  function Ee(t) {
    return t.replace(
      Hh,
      function(e) {
        return "\\" + e.charCodeAt(0).toString(16) + " ";
      }
    );
  }
  function cc(t, e, l, n, a, u, f, r) {
    t.name = "", f != null && typeof f != "function" && typeof f != "symbol" && typeof f != "boolean" ? t.type = f : t.removeAttribute("type"), e != null ? f === "number" ? (e === 0 && t.value === "" || t.value != e) && (t.value = "" + pe(e)) : t.value !== "" + pe(e) && (t.value = "" + pe(e)) : f !== "submit" && f !== "reset" || t.removeAttribute("value"), e != null ? fc(t, f, pe(e)) : l != null ? fc(t, f, pe(l)) : n != null && t.removeAttribute("value"), a == null && u != null && (t.defaultChecked = !!u), a != null && (t.checked = a && typeof a != "function" && typeof a != "symbol"), r != null && typeof r != "function" && typeof r != "symbol" && typeof r != "boolean" ? t.name = "" + pe(r) : t.removeAttribute("name");
  }
  function ds(t, e, l, n, a, u, f, r) {
    if (u != null && typeof u != "function" && typeof u != "symbol" && typeof u != "boolean" && (t.type = u), e != null || l != null) {
      if (!(u !== "submit" && u !== "reset" || e != null)) {
        ic(t);
        return;
      }
      l = l != null ? "" + pe(l) : "", e = e != null ? "" + pe(e) : l, r || e === t.value || (t.value = e), t.defaultValue = e;
    }
    n = n ?? a, n = typeof n != "function" && typeof n != "symbol" && !!n, t.checked = r ? t.checked : !!n, t.defaultChecked = !!n, f != null && typeof f != "function" && typeof f != "symbol" && typeof f != "boolean" && (t.name = f), ic(t);
  }
  function fc(t, e, l) {
    e === "number" && yu(t.ownerDocument) === t || t.defaultValue === "" + l || (t.defaultValue = "" + l);
  }
  function bn(t, e, l, n) {
    if (t = t.options, e) {
      e = {};
      for (var a = 0; a < l.length; a++)
        e["$" + l[a]] = !0;
      for (l = 0; l < t.length; l++)
        a = e.hasOwnProperty("$" + t[l].value), t[l].selected !== a && (t[l].selected = a), a && n && (t[l].defaultSelected = !0);
    } else {
      for (l = "" + pe(l), e = null, a = 0; a < t.length; a++) {
        if (t[a].value === l) {
          t[a].selected = !0, n && (t[a].defaultSelected = !0);
          return;
        }
        e !== null || t[a].disabled || (e = t[a]);
      }
      e !== null && (e.selected = !0);
    }
  }
  function ms(t, e, l) {
    if (e != null && (e = "" + pe(e), e !== t.value && (t.value = e), l == null)) {
      t.defaultValue !== e && (t.defaultValue = e);
      return;
    }
    t.defaultValue = l != null ? "" + pe(l) : "";
  }
  function vs(t, e, l, n) {
    if (e == null) {
      if (n != null) {
        if (l != null) throw Error(o(92));
        if (ft(n)) {
          if (1 < n.length) throw Error(o(93));
          n = n[0];
        }
        l = n;
      }
      l == null && (l = ""), e = l;
    }
    l = pe(e), t.defaultValue = l, n = t.textContent, n === l && n !== "" && n !== null && (t.value = n), ic(t);
  }
  function Sn(t, e) {
    if (e) {
      var l = t.firstChild;
      if (l && l === t.lastChild && l.nodeType === 3) {
        l.nodeValue = e;
        return;
      }
    }
    t.textContent = e;
  }
  var Bh = new Set(
    "animationIterationCount aspectRatio borderImageOutset borderImageSlice borderImageWidth boxFlex boxFlexGroup boxOrdinalGroup columnCount columns flex flexGrow flexPositive flexShrink flexNegative flexOrder gridArea gridRow gridRowEnd gridRowSpan gridRowStart gridColumn gridColumnEnd gridColumnSpan gridColumnStart fontWeight lineClamp lineHeight opacity order orphans scale tabSize widows zIndex zoom fillOpacity floodOpacity stopOpacity strokeDasharray strokeDashoffset strokeMiterlimit strokeOpacity strokeWidth MozAnimationIterationCount MozBoxFlex MozBoxFlexGroup MozLineClamp msAnimationIterationCount msFlex msZoom msFlexGrow msFlexNegative msFlexOrder msFlexPositive msFlexShrink msGridColumn msGridColumnSpan msGridRow msGridRowSpan WebkitAnimationIterationCount WebkitBoxFlex WebKitBoxFlexGroup WebkitBoxOrdinalGroup WebkitColumnCount WebkitColumns WebkitFlex WebkitFlexGrow WebkitFlexPositive WebkitFlexShrink WebkitLineClamp".split(
      " "
    )
  );
  function hs(t, e, l) {
    var n = e.indexOf("--") === 0;
    l == null || typeof l == "boolean" || l === "" ? n ? t.setProperty(e, "") : e === "float" ? t.cssFloat = "" : t[e] = "" : n ? t.setProperty(e, l) : typeof l != "number" || l === 0 || Bh.has(e) ? e === "float" ? t.cssFloat = l : t[e] = ("" + l).trim() : t[e] = l + "px";
  }
  function ys(t, e, l) {
    if (e != null && typeof e != "object")
      throw Error(o(62));
    if (t = t.style, l != null) {
      for (var n in l)
        !l.hasOwnProperty(n) || e != null && e.hasOwnProperty(n) || (n.indexOf("--") === 0 ? t.setProperty(n, "") : n === "float" ? t.cssFloat = "" : t[n] = "");
      for (var a in e)
        n = e[a], e.hasOwnProperty(a) && l[a] !== n && hs(t, a, n);
    } else
      for (var u in e)
        e.hasOwnProperty(u) && hs(t, u, e[u]);
  }
  function oc(t) {
    if (t.indexOf("-") === -1) return !1;
    switch (t) {
      case "annotation-xml":
      case "color-profile":
      case "font-face":
      case "font-face-src":
      case "font-face-uri":
      case "font-face-format":
      case "font-face-name":
      case "missing-glyph":
        return !1;
      default:
        return !0;
    }
  }
  var qh = /* @__PURE__ */ new Map([
    ["acceptCharset", "accept-charset"],
    ["htmlFor", "for"],
    ["httpEquiv", "http-equiv"],
    ["crossOrigin", "crossorigin"],
    ["accentHeight", "accent-height"],
    ["alignmentBaseline", "alignment-baseline"],
    ["arabicForm", "arabic-form"],
    ["baselineShift", "baseline-shift"],
    ["capHeight", "cap-height"],
    ["clipPath", "clip-path"],
    ["clipRule", "clip-rule"],
    ["colorInterpolation", "color-interpolation"],
    ["colorInterpolationFilters", "color-interpolation-filters"],
    ["colorProfile", "color-profile"],
    ["colorRendering", "color-rendering"],
    ["dominantBaseline", "dominant-baseline"],
    ["enableBackground", "enable-background"],
    ["fillOpacity", "fill-opacity"],
    ["fillRule", "fill-rule"],
    ["floodColor", "flood-color"],
    ["floodOpacity", "flood-opacity"],
    ["fontFamily", "font-family"],
    ["fontSize", "font-size"],
    ["fontSizeAdjust", "font-size-adjust"],
    ["fontStretch", "font-stretch"],
    ["fontStyle", "font-style"],
    ["fontVariant", "font-variant"],
    ["fontWeight", "font-weight"],
    ["glyphName", "glyph-name"],
    ["glyphOrientationHorizontal", "glyph-orientation-horizontal"],
    ["glyphOrientationVertical", "glyph-orientation-vertical"],
    ["horizAdvX", "horiz-adv-x"],
    ["horizOriginX", "horiz-origin-x"],
    ["imageRendering", "image-rendering"],
    ["letterSpacing", "letter-spacing"],
    ["lightingColor", "lighting-color"],
    ["markerEnd", "marker-end"],
    ["markerMid", "marker-mid"],
    ["markerStart", "marker-start"],
    ["overlinePosition", "overline-position"],
    ["overlineThickness", "overline-thickness"],
    ["paintOrder", "paint-order"],
    ["panose-1", "panose-1"],
    ["pointerEvents", "pointer-events"],
    ["renderingIntent", "rendering-intent"],
    ["shapeRendering", "shape-rendering"],
    ["stopColor", "stop-color"],
    ["stopOpacity", "stop-opacity"],
    ["strikethroughPosition", "strikethrough-position"],
    ["strikethroughThickness", "strikethrough-thickness"],
    ["strokeDasharray", "stroke-dasharray"],
    ["strokeDashoffset", "stroke-dashoffset"],
    ["strokeLinecap", "stroke-linecap"],
    ["strokeLinejoin", "stroke-linejoin"],
    ["strokeMiterlimit", "stroke-miterlimit"],
    ["strokeOpacity", "stroke-opacity"],
    ["strokeWidth", "stroke-width"],
    ["textAnchor", "text-anchor"],
    ["textDecoration", "text-decoration"],
    ["textRendering", "text-rendering"],
    ["transformOrigin", "transform-origin"],
    ["underlinePosition", "underline-position"],
    ["underlineThickness", "underline-thickness"],
    ["unicodeBidi", "unicode-bidi"],
    ["unicodeRange", "unicode-range"],
    ["unitsPerEm", "units-per-em"],
    ["vAlphabetic", "v-alphabetic"],
    ["vHanging", "v-hanging"],
    ["vIdeographic", "v-ideographic"],
    ["vMathematical", "v-mathematical"],
    ["vectorEffect", "vector-effect"],
    ["vertAdvY", "vert-adv-y"],
    ["vertOriginX", "vert-origin-x"],
    ["vertOriginY", "vert-origin-y"],
    ["wordSpacing", "word-spacing"],
    ["writingMode", "writing-mode"],
    ["xmlnsXlink", "xmlns:xlink"],
    ["xHeight", "x-height"]
  ]), Lh = /^[\u0000-\u001F ]*j[\r\n\t]*a[\r\n\t]*v[\r\n\t]*a[\r\n\t]*s[\r\n\t]*c[\r\n\t]*r[\r\n\t]*i[\r\n\t]*p[\r\n\t]*t[\r\n\t]*:/i;
  function gu(t) {
    return Lh.test("" + t) ? "javascript:throw new Error('React has blocked a javascript: URL as a security precaution.')" : t;
  }
  function Qe() {
  }
  var sc = null;
  function rc(t) {
    return t = t.target || t.srcElement || window, t.correspondingUseElement && (t = t.correspondingUseElement), t.nodeType === 3 ? t.parentNode : t;
  }
  var pn = null, En = null;
  function gs(t) {
    var e = hn(t);
    if (e && (t = e.stateNode)) {
      var l = t[ee] || null;
      t: switch (t = e.stateNode, e.type) {
        case "input":
          if (cc(
            t,
            l.value,
            l.defaultValue,
            l.defaultValue,
            l.checked,
            l.defaultChecked,
            l.type,
            l.name
          ), e = l.name, l.type === "radio" && e != null) {
            for (l = t; l.parentNode; ) l = l.parentNode;
            for (l = l.querySelectorAll(
              'input[name="' + Ee(
                "" + e
              ) + '"][type="radio"]'
            ), e = 0; e < l.length; e++) {
              var n = l[e];
              if (n !== t && n.form === t.form) {
                var a = n[ee] || null;
                if (!a) throw Error(o(90));
                cc(
                  n,
                  a.value,
                  a.defaultValue,
                  a.defaultValue,
                  a.checked,
                  a.defaultChecked,
                  a.type,
                  a.name
                );
              }
            }
            for (e = 0; e < l.length; e++)
              n = l[e], n.form === t.form && rs(n);
          }
          break t;
        case "textarea":
          ms(t, l.value, l.defaultValue);
          break t;
        case "select":
          e = l.value, e != null && bn(t, !!l.multiple, e, !1);
      }
    }
  }
  var dc = !1;
  function bs(t, e, l) {
    if (dc) return t(e, l);
    dc = !0;
    try {
      var n = t(e);
      return n;
    } finally {
      if (dc = !1, (pn !== null || En !== null) && (ai(), pn && (e = pn, t = En, En = pn = null, gs(e), t)))
        for (e = 0; e < t.length; e++) gs(t[e]);
    }
  }
  function ca(t, e) {
    var l = t.stateNode;
    if (l === null) return null;
    var n = l[ee] || null;
    if (n === null) return null;
    l = n[e];
    t: switch (e) {
      case "onClick":
      case "onClickCapture":
      case "onDoubleClick":
      case "onDoubleClickCapture":
      case "onMouseDown":
      case "onMouseDownCapture":
      case "onMouseMove":
      case "onMouseMoveCapture":
      case "onMouseUp":
      case "onMouseUpCapture":
      case "onMouseEnter":
        (n = !n.disabled) || (t = t.type, n = !(t === "button" || t === "input" || t === "select" || t === "textarea")), t = !n;
        break t;
      default:
        t = !1;
    }
    if (t) return null;
    if (l && typeof l != "function")
      throw Error(
        o(231, e, typeof l)
      );
    return l;
  }
  var Ze = !(typeof window > "u" || typeof window.document > "u" || typeof window.document.createElement > "u"), mc = !1;
  if (Ze)
    try {
      var fa = {};
      Object.defineProperty(fa, "passive", {
        get: function() {
          mc = !0;
        }
      }), window.addEventListener("test", fa, fa), window.removeEventListener("test", fa, fa);
    } catch {
      mc = !1;
    }
  var sl = null, vc = null, bu = null;
  function Ss() {
    if (bu) return bu;
    var t, e = vc, l = e.length, n, a = "value" in sl ? sl.value : sl.textContent, u = a.length;
    for (t = 0; t < l && e[t] === a[t]; t++) ;
    var f = l - t;
    for (n = 1; n <= f && e[l - n] === a[u - n]; n++) ;
    return bu = a.slice(t, 1 < n ? 1 - n : void 0);
  }
  function Su(t) {
    var e = t.keyCode;
    return "charCode" in t ? (t = t.charCode, t === 0 && e === 13 && (t = 13)) : t = e, t === 10 && (t = 13), 32 <= t || t === 13 ? t : 0;
  }
  function pu() {
    return !0;
  }
  function ps() {
    return !1;
  }
  function le(t) {
    function e(l, n, a, u, f) {
      this._reactName = l, this._targetInst = a, this.type = n, this.nativeEvent = u, this.target = f, this.currentTarget = null;
      for (var r in t)
        t.hasOwnProperty(r) && (l = t[r], this[r] = l ? l(u) : u[r]);
      return this.isDefaultPrevented = (u.defaultPrevented != null ? u.defaultPrevented : u.returnValue === !1) ? pu : ps, this.isPropagationStopped = ps, this;
    }
    return z(e.prototype, {
      preventDefault: function() {
        this.defaultPrevented = !0;
        var l = this.nativeEvent;
        l && (l.preventDefault ? l.preventDefault() : typeof l.returnValue != "unknown" && (l.returnValue = !1), this.isDefaultPrevented = pu);
      },
      stopPropagation: function() {
        var l = this.nativeEvent;
        l && (l.stopPropagation ? l.stopPropagation() : typeof l.cancelBubble != "unknown" && (l.cancelBubble = !0), this.isPropagationStopped = pu);
      },
      persist: function() {
      },
      isPersistent: pu
    }), e;
  }
  var jl = {
    eventPhase: 0,
    bubbles: 0,
    cancelable: 0,
    timeStamp: function(t) {
      return t.timeStamp || Date.now();
    },
    defaultPrevented: 0,
    isTrusted: 0
  }, Eu = le(jl), oa = z({}, jl, { view: 0, detail: 0 }), Yh = le(oa), hc, yc, sa, Tu = z({}, oa, {
    screenX: 0,
    screenY: 0,
    clientX: 0,
    clientY: 0,
    pageX: 0,
    pageY: 0,
    ctrlKey: 0,
    shiftKey: 0,
    altKey: 0,
    metaKey: 0,
    getModifierState: bc,
    button: 0,
    buttons: 0,
    relatedTarget: function(t) {
      return t.relatedTarget === void 0 ? t.fromElement === t.srcElement ? t.toElement : t.fromElement : t.relatedTarget;
    },
    movementX: function(t) {
      return "movementX" in t ? t.movementX : (t !== sa && (sa && t.type === "mousemove" ? (hc = t.screenX - sa.screenX, yc = t.screenY - sa.screenY) : yc = hc = 0, sa = t), hc);
    },
    movementY: function(t) {
      return "movementY" in t ? t.movementY : yc;
    }
  }), Es = le(Tu), jh = z({}, Tu, { dataTransfer: 0 }), Xh = le(jh), Gh = z({}, oa, { relatedTarget: 0 }), gc = le(Gh), Qh = z({}, jl, {
    animationName: 0,
    elapsedTime: 0,
    pseudoElement: 0
  }), Zh = le(Qh), Vh = z({}, jl, {
    clipboardData: function(t) {
      return "clipboardData" in t ? t.clipboardData : window.clipboardData;
    }
  }), Kh = le(Vh), Jh = z({}, jl, { data: 0 }), Ts = le(Jh), Wh = {
    Esc: "Escape",
    Spacebar: " ",
    Left: "ArrowLeft",
    Up: "ArrowUp",
    Right: "ArrowRight",
    Down: "ArrowDown",
    Del: "Delete",
    Win: "OS",
    Menu: "ContextMenu",
    Apps: "ContextMenu",
    Scroll: "ScrollLock",
    MozPrintableKey: "Unidentified"
  }, kh = {
    8: "Backspace",
    9: "Tab",
    12: "Clear",
    13: "Enter",
    16: "Shift",
    17: "Control",
    18: "Alt",
    19: "Pause",
    20: "CapsLock",
    27: "Escape",
    32: " ",
    33: "PageUp",
    34: "PageDown",
    35: "End",
    36: "Home",
    37: "ArrowLeft",
    38: "ArrowUp",
    39: "ArrowRight",
    40: "ArrowDown",
    45: "Insert",
    46: "Delete",
    112: "F1",
    113: "F2",
    114: "F3",
    115: "F4",
    116: "F5",
    117: "F6",
    118: "F7",
    119: "F8",
    120: "F9",
    121: "F10",
    122: "F11",
    123: "F12",
    144: "NumLock",
    145: "ScrollLock",
    224: "Meta"
  }, Fh = {
    Alt: "altKey",
    Control: "ctrlKey",
    Meta: "metaKey",
    Shift: "shiftKey"
  };
  function $h(t) {
    var e = this.nativeEvent;
    return e.getModifierState ? e.getModifierState(t) : (t = Fh[t]) ? !!e[t] : !1;
  }
  function bc() {
    return $h;
  }
  var Ih = z({}, oa, {
    key: function(t) {
      if (t.key) {
        var e = Wh[t.key] || t.key;
        if (e !== "Unidentified") return e;
      }
      return t.type === "keypress" ? (t = Su(t), t === 13 ? "Enter" : String.fromCharCode(t)) : t.type === "keydown" || t.type === "keyup" ? kh[t.keyCode] || "Unidentified" : "";
    },
    code: 0,
    location: 0,
    ctrlKey: 0,
    shiftKey: 0,
    altKey: 0,
    metaKey: 0,
    repeat: 0,
    locale: 0,
    getModifierState: bc,
    charCode: function(t) {
      return t.type === "keypress" ? Su(t) : 0;
    },
    keyCode: function(t) {
      return t.type === "keydown" || t.type === "keyup" ? t.keyCode : 0;
    },
    which: function(t) {
      return t.type === "keypress" ? Su(t) : t.type === "keydown" || t.type === "keyup" ? t.keyCode : 0;
    }
  }), Ph = le(Ih), ty = z({}, Tu, {
    pointerId: 0,
    width: 0,
    height: 0,
    pressure: 0,
    tangentialPressure: 0,
    tiltX: 0,
    tiltY: 0,
    twist: 0,
    pointerType: 0,
    isPrimary: 0
  }), As = le(ty), ey = z({}, oa, {
    touches: 0,
    targetTouches: 0,
    changedTouches: 0,
    altKey: 0,
    metaKey: 0,
    ctrlKey: 0,
    shiftKey: 0,
    getModifierState: bc
  }), ly = le(ey), ny = z({}, jl, {
    propertyName: 0,
    elapsedTime: 0,
    pseudoElement: 0
  }), ay = le(ny), uy = z({}, Tu, {
    deltaX: function(t) {
      return "deltaX" in t ? t.deltaX : "wheelDeltaX" in t ? -t.wheelDeltaX : 0;
    },
    deltaY: function(t) {
      return "deltaY" in t ? t.deltaY : "wheelDeltaY" in t ? -t.wheelDeltaY : "wheelDelta" in t ? -t.wheelDelta : 0;
    },
    deltaZ: 0,
    deltaMode: 0
  }), iy = le(uy), cy = z({}, jl, {
    newState: 0,
    oldState: 0
  }), fy = le(cy), oy = [9, 13, 27, 32], Sc = Ze && "CompositionEvent" in window, ra = null;
  Ze && "documentMode" in document && (ra = document.documentMode);
  var sy = Ze && "TextEvent" in window && !ra, Rs = Ze && (!Sc || ra && 8 < ra && 11 >= ra), Os = " ", zs = !1;
  function _s(t, e) {
    switch (t) {
      case "keyup":
        return oy.indexOf(e.keyCode) !== -1;
      case "keydown":
        return e.keyCode !== 229;
      case "keypress":
      case "mousedown":
      case "focusout":
        return !0;
      default:
        return !1;
    }
  }
  function Ms(t) {
    return t = t.detail, typeof t == "object" && "data" in t ? t.data : null;
  }
  var Tn = !1;
  function ry(t, e) {
    switch (t) {
      case "compositionend":
        return Ms(e);
      case "keypress":
        return e.which !== 32 ? null : (zs = !0, Os);
      case "textInput":
        return t = e.data, t === Os && zs ? null : t;
      default:
        return null;
    }
  }
  function dy(t, e) {
    if (Tn)
      return t === "compositionend" || !Sc && _s(t, e) ? (t = Ss(), bu = vc = sl = null, Tn = !1, t) : null;
    switch (t) {
      case "paste":
        return null;
      case "keypress":
        if (!(e.ctrlKey || e.altKey || e.metaKey) || e.ctrlKey && e.altKey) {
          if (e.char && 1 < e.char.length)
            return e.char;
          if (e.which) return String.fromCharCode(e.which);
        }
        return null;
      case "compositionend":
        return Rs && e.locale !== "ko" ? null : e.data;
      default:
        return null;
    }
  }
  var my = {
    color: !0,
    date: !0,
    datetime: !0,
    "datetime-local": !0,
    email: !0,
    month: !0,
    number: !0,
    password: !0,
    range: !0,
    search: !0,
    tel: !0,
    text: !0,
    time: !0,
    url: !0,
    week: !0
  };
  function Cs(t) {
    var e = t && t.nodeName && t.nodeName.toLowerCase();
    return e === "input" ? !!my[t.type] : e === "textarea";
  }
  function xs(t, e, l, n) {
    pn ? En ? En.push(n) : En = [n] : pn = n, e = ri(e, "onChange"), 0 < e.length && (l = new Eu(
      "onChange",
      "change",
      null,
      l,
      n
    ), t.push({ event: l, listeners: e }));
  }
  var da = null, ma = null;
  function vy(t) {
    dm(t, 0);
  }
  function Au(t) {
    var e = ia(t);
    if (rs(e)) return t;
  }
  function Ds(t, e) {
    if (t === "change") return e;
  }
  var Ns = !1;
  if (Ze) {
    var pc;
    if (Ze) {
      var Ec = "oninput" in document;
      if (!Ec) {
        var Us = document.createElement("div");
        Us.setAttribute("oninput", "return;"), Ec = typeof Us.oninput == "function";
      }
      pc = Ec;
    } else pc = !1;
    Ns = pc && (!document.documentMode || 9 < document.documentMode);
  }
  function ws() {
    da && (da.detachEvent("onpropertychange", Hs), ma = da = null);
  }
  function Hs(t) {
    if (t.propertyName === "value" && Au(ma)) {
      var e = [];
      xs(
        e,
        ma,
        t,
        rc(t)
      ), bs(vy, e);
    }
  }
  function hy(t, e, l) {
    t === "focusin" ? (ws(), da = e, ma = l, da.attachEvent("onpropertychange", Hs)) : t === "focusout" && ws();
  }
  function yy(t) {
    if (t === "selectionchange" || t === "keyup" || t === "keydown")
      return Au(ma);
  }
  function gy(t, e) {
    if (t === "click") return Au(e);
  }
  function by(t, e) {
    if (t === "input" || t === "change")
      return Au(e);
  }
  function Sy(t, e) {
    return t === e && (t !== 0 || 1 / t === 1 / e) || t !== t && e !== e;
  }
  var re = typeof Object.is == "function" ? Object.is : Sy;
  function va(t, e) {
    if (re(t, e)) return !0;
    if (typeof t != "object" || t === null || typeof e != "object" || e === null)
      return !1;
    var l = Object.keys(t), n = Object.keys(e);
    if (l.length !== n.length) return !1;
    for (n = 0; n < l.length; n++) {
      var a = l[n];
      if (!Ii.call(e, a) || !re(t[a], e[a]))
        return !1;
    }
    return !0;
  }
  function Bs(t) {
    for (; t && t.firstChild; ) t = t.firstChild;
    return t;
  }
  function qs(t, e) {
    var l = Bs(t);
    t = 0;
    for (var n; l; ) {
      if (l.nodeType === 3) {
        if (n = t + l.textContent.length, t <= e && n >= e)
          return { node: l, offset: e - t };
        t = n;
      }
      t: {
        for (; l; ) {
          if (l.nextSibling) {
            l = l.nextSibling;
            break t;
          }
          l = l.parentNode;
        }
        l = void 0;
      }
      l = Bs(l);
    }
  }
  function Ls(t, e) {
    return t && e ? t === e ? !0 : t && t.nodeType === 3 ? !1 : e && e.nodeType === 3 ? Ls(t, e.parentNode) : "contains" in t ? t.contains(e) : t.compareDocumentPosition ? !!(t.compareDocumentPosition(e) & 16) : !1 : !1;
  }
  function Ys(t) {
    t = t != null && t.ownerDocument != null && t.ownerDocument.defaultView != null ? t.ownerDocument.defaultView : window;
    for (var e = yu(t.document); e instanceof t.HTMLIFrameElement; ) {
      try {
        var l = typeof e.contentWindow.location.href == "string";
      } catch {
        l = !1;
      }
      if (l) t = e.contentWindow;
      else break;
      e = yu(t.document);
    }
    return e;
  }
  function Tc(t) {
    var e = t && t.nodeName && t.nodeName.toLowerCase();
    return e && (e === "input" && (t.type === "text" || t.type === "search" || t.type === "tel" || t.type === "url" || t.type === "password") || e === "textarea" || t.contentEditable === "true");
  }
  var py = Ze && "documentMode" in document && 11 >= document.documentMode, An = null, Ac = null, ha = null, Rc = !1;
  function js(t, e, l) {
    var n = l.window === l ? l.document : l.nodeType === 9 ? l : l.ownerDocument;
    Rc || An == null || An !== yu(n) || (n = An, "selectionStart" in n && Tc(n) ? n = { start: n.selectionStart, end: n.selectionEnd } : (n = (n.ownerDocument && n.ownerDocument.defaultView || window).getSelection(), n = {
      anchorNode: n.anchorNode,
      anchorOffset: n.anchorOffset,
      focusNode: n.focusNode,
      focusOffset: n.focusOffset
    }), ha && va(ha, n) || (ha = n, n = ri(Ac, "onSelect"), 0 < n.length && (e = new Eu(
      "onSelect",
      "select",
      null,
      e,
      l
    ), t.push({ event: e, listeners: n }), e.target = An)));
  }
  function Xl(t, e) {
    var l = {};
    return l[t.toLowerCase()] = e.toLowerCase(), l["Webkit" + t] = "webkit" + e, l["Moz" + t] = "moz" + e, l;
  }
  var Rn = {
    animationend: Xl("Animation", "AnimationEnd"),
    animationiteration: Xl("Animation", "AnimationIteration"),
    animationstart: Xl("Animation", "AnimationStart"),
    transitionrun: Xl("Transition", "TransitionRun"),
    transitionstart: Xl("Transition", "TransitionStart"),
    transitioncancel: Xl("Transition", "TransitionCancel"),
    transitionend: Xl("Transition", "TransitionEnd")
  }, Oc = {}, Xs = {};
  Ze && (Xs = document.createElement("div").style, "AnimationEvent" in window || (delete Rn.animationend.animation, delete Rn.animationiteration.animation, delete Rn.animationstart.animation), "TransitionEvent" in window || delete Rn.transitionend.transition);
  function Gl(t) {
    if (Oc[t]) return Oc[t];
    if (!Rn[t]) return t;
    var e = Rn[t], l;
    for (l in e)
      if (e.hasOwnProperty(l) && l in Xs)
        return Oc[t] = e[l];
    return t;
  }
  var Gs = Gl("animationend"), Qs = Gl("animationiteration"), Zs = Gl("animationstart"), Ey = Gl("transitionrun"), Ty = Gl("transitionstart"), Ay = Gl("transitioncancel"), Vs = Gl("transitionend"), Ks = /* @__PURE__ */ new Map(), zc = "abort auxClick beforeToggle cancel canPlay canPlayThrough click close contextMenu copy cut drag dragEnd dragEnter dragExit dragLeave dragOver dragStart drop durationChange emptied encrypted ended error gotPointerCapture input invalid keyDown keyPress keyUp load loadedData loadedMetadata loadStart lostPointerCapture mouseDown mouseMove mouseOut mouseOver mouseUp paste pause play playing pointerCancel pointerDown pointerMove pointerOut pointerOver pointerUp progress rateChange reset resize seeked seeking stalled submit suspend timeUpdate touchCancel touchEnd touchStart volumeChange scroll toggle touchMove waiting wheel".split(
    " "
  );
  zc.push("scrollEnd");
  function xe(t, e) {
    Ks.set(t, e), Yl(e, [t]);
  }
  var Ru = typeof reportError == "function" ? reportError : function(t) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var e = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof t == "object" && t !== null && typeof t.message == "string" ? String(t.message) : String(t),
        error: t
      });
      if (!window.dispatchEvent(e)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", t);
      return;
    }
    console.error(t);
  }, Te = [], On = 0, _c = 0;
  function Ou() {
    for (var t = On, e = _c = On = 0; e < t; ) {
      var l = Te[e];
      Te[e++] = null;
      var n = Te[e];
      Te[e++] = null;
      var a = Te[e];
      Te[e++] = null;
      var u = Te[e];
      if (Te[e++] = null, n !== null && a !== null) {
        var f = n.pending;
        f === null ? a.next = a : (a.next = f.next, f.next = a), n.pending = a;
      }
      u !== 0 && Js(l, a, u);
    }
  }
  function zu(t, e, l, n) {
    Te[On++] = t, Te[On++] = e, Te[On++] = l, Te[On++] = n, _c |= n, t.lanes |= n, t = t.alternate, t !== null && (t.lanes |= n);
  }
  function Mc(t, e, l, n) {
    return zu(t, e, l, n), _u(t);
  }
  function Ql(t, e) {
    return zu(t, null, null, e), _u(t);
  }
  function Js(t, e, l) {
    t.lanes |= l;
    var n = t.alternate;
    n !== null && (n.lanes |= l);
    for (var a = !1, u = t.return; u !== null; )
      u.childLanes |= l, n = u.alternate, n !== null && (n.childLanes |= l), u.tag === 22 && (t = u.stateNode, t === null || t._visibility & 1 || (a = !0)), t = u, u = u.return;
    return t.tag === 3 ? (u = t.stateNode, a && e !== null && (a = 31 - se(l), t = u.hiddenUpdates, n = t[a], n === null ? t[a] = [e] : n.push(e), e.lane = l | 536870912), u) : null;
  }
  function _u(t) {
    if (50 < qa)
      throw qa = 0, Lf = null, Error(o(185));
    for (var e = t.return; e !== null; )
      t = e, e = t.return;
    return t.tag === 3 ? t.stateNode : null;
  }
  var zn = {};
  function Ry(t, e, l, n) {
    this.tag = t, this.key = l, this.sibling = this.child = this.return = this.stateNode = this.type = this.elementType = null, this.index = 0, this.refCleanup = this.ref = null, this.pendingProps = e, this.dependencies = this.memoizedState = this.updateQueue = this.memoizedProps = null, this.mode = n, this.subtreeFlags = this.flags = 0, this.deletions = null, this.childLanes = this.lanes = 0, this.alternate = null;
  }
  function de(t, e, l, n) {
    return new Ry(t, e, l, n);
  }
  function Cc(t) {
    return t = t.prototype, !(!t || !t.isReactComponent);
  }
  function Ve(t, e) {
    var l = t.alternate;
    return l === null ? (l = de(
      t.tag,
      e,
      t.key,
      t.mode
    ), l.elementType = t.elementType, l.type = t.type, l.stateNode = t.stateNode, l.alternate = t, t.alternate = l) : (l.pendingProps = e, l.type = t.type, l.flags = 0, l.subtreeFlags = 0, l.deletions = null), l.flags = t.flags & 65011712, l.childLanes = t.childLanes, l.lanes = t.lanes, l.child = t.child, l.memoizedProps = t.memoizedProps, l.memoizedState = t.memoizedState, l.updateQueue = t.updateQueue, e = t.dependencies, l.dependencies = e === null ? null : { lanes: e.lanes, firstContext: e.firstContext }, l.sibling = t.sibling, l.index = t.index, l.ref = t.ref, l.refCleanup = t.refCleanup, l;
  }
  function Ws(t, e) {
    t.flags &= 65011714;
    var l = t.alternate;
    return l === null ? (t.childLanes = 0, t.lanes = e, t.child = null, t.subtreeFlags = 0, t.memoizedProps = null, t.memoizedState = null, t.updateQueue = null, t.dependencies = null, t.stateNode = null) : (t.childLanes = l.childLanes, t.lanes = l.lanes, t.child = l.child, t.subtreeFlags = 0, t.deletions = null, t.memoizedProps = l.memoizedProps, t.memoizedState = l.memoizedState, t.updateQueue = l.updateQueue, t.type = l.type, e = l.dependencies, t.dependencies = e === null ? null : {
      lanes: e.lanes,
      firstContext: e.firstContext
    }), t;
  }
  function Mu(t, e, l, n, a, u) {
    var f = 0;
    if (n = t, typeof t == "function") Cc(t) && (f = 1);
    else if (typeof t == "string")
      f = C0(
        t,
        l,
        K.current
      ) ? 26 : t === "html" || t === "head" || t === "body" ? 27 : 5;
    else
      t: switch (t) {
        case zt:
          return t = de(31, l, e, a), t.elementType = zt, t.lanes = u, t;
        case L:
          return Zl(l.children, a, u, e);
        case Z:
          f = 8, a |= 24;
          break;
        case X:
          return t = de(12, l, e, a | 2), t.elementType = X, t.lanes = u, t;
        case at:
          return t = de(13, l, e, a), t.elementType = at, t.lanes = u, t;
        case lt:
          return t = de(19, l, e, a), t.elementType = lt, t.lanes = u, t;
        default:
          if (typeof t == "object" && t !== null)
            switch (t.$$typeof) {
              case G:
                f = 10;
                break t;
              case W:
                f = 9;
                break t;
              case $:
                f = 11;
                break t;
              case V:
                f = 14;
                break t;
              case it:
                f = 16, n = null;
                break t;
            }
          f = 29, l = Error(
            o(130, t === null ? "null" : typeof t, "")
          ), n = null;
      }
    return e = de(f, l, e, a), e.elementType = t, e.type = n, e.lanes = u, e;
  }
  function Zl(t, e, l, n) {
    return t = de(7, t, n, e), t.lanes = l, t;
  }
  function xc(t, e, l) {
    return t = de(6, t, null, e), t.lanes = l, t;
  }
  function ks(t) {
    var e = de(18, null, null, 0);
    return e.stateNode = t, e;
  }
  function Dc(t, e, l) {
    return e = de(
      4,
      t.children !== null ? t.children : [],
      t.key,
      e
    ), e.lanes = l, e.stateNode = {
      containerInfo: t.containerInfo,
      pendingChildren: null,
      implementation: t.implementation
    }, e;
  }
  var Fs = /* @__PURE__ */ new WeakMap();
  function Ae(t, e) {
    if (typeof t == "object" && t !== null) {
      var l = Fs.get(t);
      return l !== void 0 ? l : (e = {
        value: t,
        source: e,
        stack: ko(e)
      }, Fs.set(t, e), e);
    }
    return {
      value: t,
      source: e,
      stack: ko(e)
    };
  }
  var _n = [], Mn = 0, Cu = null, ya = 0, Re = [], Oe = 0, rl = null, Ue = 1, we = "";
  function Ke(t, e) {
    _n[Mn++] = ya, _n[Mn++] = Cu, Cu = t, ya = e;
  }
  function $s(t, e, l) {
    Re[Oe++] = Ue, Re[Oe++] = we, Re[Oe++] = rl, rl = t;
    var n = Ue;
    t = we;
    var a = 32 - se(n) - 1;
    n &= ~(1 << a), l += 1;
    var u = 32 - se(e) + a;
    if (30 < u) {
      var f = a - a % 5;
      u = (n & (1 << f) - 1).toString(32), n >>= f, a -= f, Ue = 1 << 32 - se(e) + a | l << a | n, we = u + t;
    } else
      Ue = 1 << u | l << a | n, we = t;
  }
  function Nc(t) {
    t.return !== null && (Ke(t, 1), $s(t, 1, 0));
  }
  function Uc(t) {
    for (; t === Cu; )
      Cu = _n[--Mn], _n[Mn] = null, ya = _n[--Mn], _n[Mn] = null;
    for (; t === rl; )
      rl = Re[--Oe], Re[Oe] = null, we = Re[--Oe], Re[Oe] = null, Ue = Re[--Oe], Re[Oe] = null;
  }
  function Is(t, e) {
    Re[Oe++] = Ue, Re[Oe++] = we, Re[Oe++] = rl, Ue = e.id, we = e.overflow, rl = t;
  }
  var kt = null, xt = null, yt = !1, dl = null, ze = !1, wc = Error(o(519));
  function ml(t) {
    var e = Error(
      o(
        418,
        1 < arguments.length && arguments[1] !== void 0 && arguments[1] ? "text" : "HTML",
        ""
      )
    );
    throw ga(Ae(e, t)), wc;
  }
  function Ps(t) {
    var e = t.stateNode, l = t.type, n = t.memoizedProps;
    switch (e[Wt] = t, e[ee] = n, l) {
      case "dialog":
        rt("cancel", e), rt("close", e);
        break;
      case "iframe":
      case "object":
      case "embed":
        rt("load", e);
        break;
      case "video":
      case "audio":
        for (l = 0; l < Ya.length; l++)
          rt(Ya[l], e);
        break;
      case "source":
        rt("error", e);
        break;
      case "img":
      case "image":
      case "link":
        rt("error", e), rt("load", e);
        break;
      case "details":
        rt("toggle", e);
        break;
      case "input":
        rt("invalid", e), ds(
          e,
          n.value,
          n.defaultValue,
          n.checked,
          n.defaultChecked,
          n.type,
          n.name,
          !0
        );
        break;
      case "select":
        rt("invalid", e);
        break;
      case "textarea":
        rt("invalid", e), vs(e, n.value, n.defaultValue, n.children);
    }
    l = n.children, typeof l != "string" && typeof l != "number" && typeof l != "bigint" || e.textContent === "" + l || n.suppressHydrationWarning === !0 || ym(e.textContent, l) ? (n.popover != null && (rt("beforetoggle", e), rt("toggle", e)), n.onScroll != null && rt("scroll", e), n.onScrollEnd != null && rt("scrollend", e), n.onClick != null && (e.onclick = Qe), e = !0) : e = !1, e || ml(t, !0);
  }
  function tr(t) {
    for (kt = t.return; kt; )
      switch (kt.tag) {
        case 5:
        case 31:
        case 13:
          ze = !1;
          return;
        case 27:
        case 3:
          ze = !0;
          return;
        default:
          kt = kt.return;
      }
  }
  function Cn(t) {
    if (t !== kt) return !1;
    if (!yt) return tr(t), yt = !0, !1;
    var e = t.tag, l;
    if ((l = e !== 3 && e !== 27) && ((l = e === 5) && (l = t.type, l = !(l !== "form" && l !== "button") || Pf(t.type, t.memoizedProps)), l = !l), l && xt && ml(t), tr(t), e === 13) {
      if (t = t.memoizedState, t = t !== null ? t.dehydrated : null, !t) throw Error(o(317));
      xt = Om(t);
    } else if (e === 31) {
      if (t = t.memoizedState, t = t !== null ? t.dehydrated : null, !t) throw Error(o(317));
      xt = Om(t);
    } else
      e === 27 ? (e = xt, _l(t.type) ? (t = ao, ao = null, xt = t) : xt = e) : xt = kt ? Me(t.stateNode.nextSibling) : null;
    return !0;
  }
  function Vl() {
    xt = kt = null, yt = !1;
  }
  function Hc() {
    var t = dl;
    return t !== null && (ie === null ? ie = t : ie.push.apply(
      ie,
      t
    ), dl = null), t;
  }
  function ga(t) {
    dl === null ? dl = [t] : dl.push(t);
  }
  var Bc = g(null), Kl = null, Je = null;
  function vl(t, e, l) {
    j(Bc, e._currentValue), e._currentValue = l;
  }
  function We(t) {
    t._currentValue = Bc.current, w(Bc);
  }
  function qc(t, e, l) {
    for (; t !== null; ) {
      var n = t.alternate;
      if ((t.childLanes & e) !== e ? (t.childLanes |= e, n !== null && (n.childLanes |= e)) : n !== null && (n.childLanes & e) !== e && (n.childLanes |= e), t === l) break;
      t = t.return;
    }
  }
  function Lc(t, e, l, n) {
    var a = t.child;
    for (a !== null && (a.return = t); a !== null; ) {
      var u = a.dependencies;
      if (u !== null) {
        var f = a.child;
        u = u.firstContext;
        t: for (; u !== null; ) {
          var r = u;
          u = a;
          for (var y = 0; y < e.length; y++)
            if (r.context === e[y]) {
              u.lanes |= l, r = u.alternate, r !== null && (r.lanes |= l), qc(
                u.return,
                l,
                t
              ), n || (f = null);
              break t;
            }
          u = r.next;
        }
      } else if (a.tag === 18) {
        if (f = a.return, f === null) throw Error(o(341));
        f.lanes |= l, u = f.alternate, u !== null && (u.lanes |= l), qc(f, l, t), f = null;
      } else f = a.child;
      if (f !== null) f.return = a;
      else
        for (f = a; f !== null; ) {
          if (f === t) {
            f = null;
            break;
          }
          if (a = f.sibling, a !== null) {
            a.return = f.return, f = a;
            break;
          }
          f = f.return;
        }
      a = f;
    }
  }
  function xn(t, e, l, n) {
    t = null;
    for (var a = e, u = !1; a !== null; ) {
      if (!u) {
        if ((a.flags & 524288) !== 0) u = !0;
        else if ((a.flags & 262144) !== 0) break;
      }
      if (a.tag === 10) {
        var f = a.alternate;
        if (f === null) throw Error(o(387));
        if (f = f.memoizedProps, f !== null) {
          var r = a.type;
          re(a.pendingProps.value, f.value) || (t !== null ? t.push(r) : t = [r]);
        }
      } else if (a === mt.current) {
        if (f = a.alternate, f === null) throw Error(o(387));
        f.memoizedState.memoizedState !== a.memoizedState.memoizedState && (t !== null ? t.push(Za) : t = [Za]);
      }
      a = a.return;
    }
    t !== null && Lc(
      e,
      t,
      l,
      n
    ), e.flags |= 262144;
  }
  function xu(t) {
    for (t = t.firstContext; t !== null; ) {
      if (!re(
        t.context._currentValue,
        t.memoizedValue
      ))
        return !0;
      t = t.next;
    }
    return !1;
  }
  function Jl(t) {
    Kl = t, Je = null, t = t.dependencies, t !== null && (t.firstContext = null);
  }
  function Ft(t) {
    return er(Kl, t);
  }
  function Du(t, e) {
    return Kl === null && Jl(t), er(t, e);
  }
  function er(t, e) {
    var l = e._currentValue;
    if (e = { context: e, memoizedValue: l, next: null }, Je === null) {
      if (t === null) throw Error(o(308));
      Je = e, t.dependencies = { lanes: 0, firstContext: e }, t.flags |= 524288;
    } else Je = Je.next = e;
    return l;
  }
  var Oy = typeof AbortController < "u" ? AbortController : function() {
    var t = [], e = this.signal = {
      aborted: !1,
      addEventListener: function(l, n) {
        t.push(n);
      }
    };
    this.abort = function() {
      e.aborted = !0, t.forEach(function(l) {
        return l();
      });
    };
  }, zy = i.unstable_scheduleCallback, _y = i.unstable_NormalPriority, Yt = {
    $$typeof: G,
    Consumer: null,
    Provider: null,
    _currentValue: null,
    _currentValue2: null,
    _threadCount: 0
  };
  function Yc() {
    return {
      controller: new Oy(),
      data: /* @__PURE__ */ new Map(),
      refCount: 0
    };
  }
  function ba(t) {
    t.refCount--, t.refCount === 0 && zy(_y, function() {
      t.controller.abort();
    });
  }
  var Sa = null, jc = 0, Dn = 0, Nn = null;
  function My(t, e) {
    if (Sa === null) {
      var l = Sa = [];
      jc = 0, Dn = Zf(), Nn = {
        status: "pending",
        value: void 0,
        then: function(n) {
          l.push(n);
        }
      };
    }
    return jc++, e.then(lr, lr), e;
  }
  function lr() {
    if (--jc === 0 && Sa !== null) {
      Nn !== null && (Nn.status = "fulfilled");
      var t = Sa;
      Sa = null, Dn = 0, Nn = null;
      for (var e = 0; e < t.length; e++) (0, t[e])();
    }
  }
  function Cy(t, e) {
    var l = [], n = {
      status: "pending",
      value: null,
      reason: null,
      then: function(a) {
        l.push(a);
      }
    };
    return t.then(
      function() {
        n.status = "fulfilled", n.value = e;
        for (var a = 0; a < l.length; a++) (0, l[a])(e);
      },
      function(a) {
        for (n.status = "rejected", n.reason = a, a = 0; a < l.length; a++)
          (0, l[a])(void 0);
      }
    ), n;
  }
  var nr = R.S;
  R.S = function(t, e) {
    jd = fe(), typeof e == "object" && e !== null && typeof e.then == "function" && My(t, e), nr !== null && nr(t, e);
  };
  var Wl = g(null);
  function Xc() {
    var t = Wl.current;
    return t !== null ? t : Mt.pooledCache;
  }
  function Nu(t, e) {
    e === null ? j(Wl, Wl.current) : j(Wl, e.pool);
  }
  function ar() {
    var t = Xc();
    return t === null ? null : { parent: Yt._currentValue, pool: t };
  }
  var Un = Error(o(460)), Gc = Error(o(474)), Uu = Error(o(542)), wu = { then: function() {
  } };
  function ur(t) {
    return t = t.status, t === "fulfilled" || t === "rejected";
  }
  function ir(t, e, l) {
    switch (l = t[l], l === void 0 ? t.push(e) : l !== e && (e.then(Qe, Qe), e = l), e.status) {
      case "fulfilled":
        return e.value;
      case "rejected":
        throw t = e.reason, fr(t), t;
      default:
        if (typeof e.status == "string") e.then(Qe, Qe);
        else {
          if (t = Mt, t !== null && 100 < t.shellSuspendCounter)
            throw Error(o(482));
          t = e, t.status = "pending", t.then(
            function(n) {
              if (e.status === "pending") {
                var a = e;
                a.status = "fulfilled", a.value = n;
              }
            },
            function(n) {
              if (e.status === "pending") {
                var a = e;
                a.status = "rejected", a.reason = n;
              }
            }
          );
        }
        switch (e.status) {
          case "fulfilled":
            return e.value;
          case "rejected":
            throw t = e.reason, fr(t), t;
        }
        throw Fl = e, Un;
    }
  }
  function kl(t) {
    try {
      var e = t._init;
      return e(t._payload);
    } catch (l) {
      throw l !== null && typeof l == "object" && typeof l.then == "function" ? (Fl = l, Un) : l;
    }
  }
  var Fl = null;
  function cr() {
    if (Fl === null) throw Error(o(459));
    var t = Fl;
    return Fl = null, t;
  }
  function fr(t) {
    if (t === Un || t === Uu)
      throw Error(o(483));
  }
  var wn = null, pa = 0;
  function Hu(t) {
    var e = pa;
    return pa += 1, wn === null && (wn = []), ir(wn, t, e);
  }
  function Ea(t, e) {
    e = e.props.ref, t.ref = e !== void 0 ? e : null;
  }
  function Bu(t, e) {
    throw e.$$typeof === N ? Error(o(525)) : (t = Object.prototype.toString.call(e), Error(
      o(
        31,
        t === "[object Object]" ? "object with keys {" + Object.keys(e).join(", ") + "}" : t
      )
    ));
  }
  function or(t) {
    function e(E, b) {
      if (t) {
        var T = E.deletions;
        T === null ? (E.deletions = [b], E.flags |= 16) : T.push(b);
      }
    }
    function l(E, b) {
      if (!t) return null;
      for (; b !== null; )
        e(E, b), b = b.sibling;
      return null;
    }
    function n(E) {
      for (var b = /* @__PURE__ */ new Map(); E !== null; )
        E.key !== null ? b.set(E.key, E) : b.set(E.index, E), E = E.sibling;
      return b;
    }
    function a(E, b) {
      return E = Ve(E, b), E.index = 0, E.sibling = null, E;
    }
    function u(E, b, T) {
      return E.index = T, t ? (T = E.alternate, T !== null ? (T = T.index, T < b ? (E.flags |= 67108866, b) : T) : (E.flags |= 67108866, b)) : (E.flags |= 1048576, b);
    }
    function f(E) {
      return t && E.alternate === null && (E.flags |= 67108866), E;
    }
    function r(E, b, T, x) {
      return b === null || b.tag !== 6 ? (b = xc(T, E.mode, x), b.return = E, b) : (b = a(b, T), b.return = E, b);
    }
    function y(E, b, T, x) {
      var P = T.type;
      return P === L ? C(
        E,
        b,
        T.props.children,
        x,
        T.key
      ) : b !== null && (b.elementType === P || typeof P == "object" && P !== null && P.$$typeof === it && kl(P) === b.type) ? (b = a(b, T.props), Ea(b, T), b.return = E, b) : (b = Mu(
        T.type,
        T.key,
        T.props,
        null,
        E.mode,
        x
      ), Ea(b, T), b.return = E, b);
    }
    function A(E, b, T, x) {
      return b === null || b.tag !== 4 || b.stateNode.containerInfo !== T.containerInfo || b.stateNode.implementation !== T.implementation ? (b = Dc(T, E.mode, x), b.return = E, b) : (b = a(b, T.children || []), b.return = E, b);
    }
    function C(E, b, T, x, P) {
      return b === null || b.tag !== 7 ? (b = Zl(
        T,
        E.mode,
        x,
        P
      ), b.return = E, b) : (b = a(b, T), b.return = E, b);
    }
    function U(E, b, T) {
      if (typeof b == "string" && b !== "" || typeof b == "number" || typeof b == "bigint")
        return b = xc(
          "" + b,
          E.mode,
          T
        ), b.return = E, b;
      if (typeof b == "object" && b !== null) {
        switch (b.$$typeof) {
          case D:
            return T = Mu(
              b.type,
              b.key,
              b.props,
              null,
              E.mode,
              T
            ), Ea(T, b), T.return = E, T;
          case Y:
            return b = Dc(
              b,
              E.mode,
              T
            ), b.return = E, b;
          case it:
            return b = kl(b), U(E, b, T);
        }
        if (ft(b) || dt(b))
          return b = Zl(
            b,
            E.mode,
            T,
            null
          ), b.return = E, b;
        if (typeof b.then == "function")
          return U(E, Hu(b), T);
        if (b.$$typeof === G)
          return U(
            E,
            Du(E, b),
            T
          );
        Bu(E, b);
      }
      return null;
    }
    function O(E, b, T, x) {
      var P = b !== null ? b.key : null;
      if (typeof T == "string" && T !== "" || typeof T == "number" || typeof T == "bigint")
        return P !== null ? null : r(E, b, "" + T, x);
      if (typeof T == "object" && T !== null) {
        switch (T.$$typeof) {
          case D:
            return T.key === P ? y(E, b, T, x) : null;
          case Y:
            return T.key === P ? A(E, b, T, x) : null;
          case it:
            return T = kl(T), O(E, b, T, x);
        }
        if (ft(T) || dt(T))
          return P !== null ? null : C(E, b, T, x, null);
        if (typeof T.then == "function")
          return O(
            E,
            b,
            Hu(T),
            x
          );
        if (T.$$typeof === G)
          return O(
            E,
            b,
            Du(E, T),
            x
          );
        Bu(E, T);
      }
      return null;
    }
    function _(E, b, T, x, P) {
      if (typeof x == "string" && x !== "" || typeof x == "number" || typeof x == "bigint")
        return E = E.get(T) || null, r(b, E, "" + x, P);
      if (typeof x == "object" && x !== null) {
        switch (x.$$typeof) {
          case D:
            return E = E.get(
              x.key === null ? T : x.key
            ) || null, y(b, E, x, P);
          case Y:
            return E = E.get(
              x.key === null ? T : x.key
            ) || null, A(b, E, x, P);
          case it:
            return x = kl(x), _(
              E,
              b,
              T,
              x,
              P
            );
        }
        if (ft(x) || dt(x))
          return E = E.get(T) || null, C(b, E, x, P, null);
        if (typeof x.then == "function")
          return _(
            E,
            b,
            T,
            Hu(x),
            P
          );
        if (x.$$typeof === G)
          return _(
            E,
            b,
            T,
            Du(b, x),
            P
          );
        Bu(b, x);
      }
      return null;
    }
    function J(E, b, T, x) {
      for (var P = null, gt = null, k = b, ot = b = 0, ht = null; k !== null && ot < T.length; ot++) {
        k.index > ot ? (ht = k, k = null) : ht = k.sibling;
        var bt = O(
          E,
          k,
          T[ot],
          x
        );
        if (bt === null) {
          k === null && (k = ht);
          break;
        }
        t && k && bt.alternate === null && e(E, k), b = u(bt, b, ot), gt === null ? P = bt : gt.sibling = bt, gt = bt, k = ht;
      }
      if (ot === T.length)
        return l(E, k), yt && Ke(E, ot), P;
      if (k === null) {
        for (; ot < T.length; ot++)
          k = U(E, T[ot], x), k !== null && (b = u(
            k,
            b,
            ot
          ), gt === null ? P = k : gt.sibling = k, gt = k);
        return yt && Ke(E, ot), P;
      }
      for (k = n(k); ot < T.length; ot++)
        ht = _(
          k,
          E,
          ot,
          T[ot],
          x
        ), ht !== null && (t && ht.alternate !== null && k.delete(
          ht.key === null ? ot : ht.key
        ), b = u(
          ht,
          b,
          ot
        ), gt === null ? P = ht : gt.sibling = ht, gt = ht);
      return t && k.forEach(function(Nl) {
        return e(E, Nl);
      }), yt && Ke(E, ot), P;
    }
    function tt(E, b, T, x) {
      if (T == null) throw Error(o(151));
      for (var P = null, gt = null, k = b, ot = b = 0, ht = null, bt = T.next(); k !== null && !bt.done; ot++, bt = T.next()) {
        k.index > ot ? (ht = k, k = null) : ht = k.sibling;
        var Nl = O(E, k, bt.value, x);
        if (Nl === null) {
          k === null && (k = ht);
          break;
        }
        t && k && Nl.alternate === null && e(E, k), b = u(Nl, b, ot), gt === null ? P = Nl : gt.sibling = Nl, gt = Nl, k = ht;
      }
      if (bt.done)
        return l(E, k), yt && Ke(E, ot), P;
      if (k === null) {
        for (; !bt.done; ot++, bt = T.next())
          bt = U(E, bt.value, x), bt !== null && (b = u(bt, b, ot), gt === null ? P = bt : gt.sibling = bt, gt = bt);
        return yt && Ke(E, ot), P;
      }
      for (k = n(k); !bt.done; ot++, bt = T.next())
        bt = _(k, E, ot, bt.value, x), bt !== null && (t && bt.alternate !== null && k.delete(bt.key === null ? ot : bt.key), b = u(bt, b, ot), gt === null ? P = bt : gt.sibling = bt, gt = bt);
      return t && k.forEach(function(j0) {
        return e(E, j0);
      }), yt && Ke(E, ot), P;
    }
    function Ot(E, b, T, x) {
      if (typeof T == "object" && T !== null && T.type === L && T.key === null && (T = T.props.children), typeof T == "object" && T !== null) {
        switch (T.$$typeof) {
          case D:
            t: {
              for (var P = T.key; b !== null; ) {
                if (b.key === P) {
                  if (P = T.type, P === L) {
                    if (b.tag === 7) {
                      l(
                        E,
                        b.sibling
                      ), x = a(
                        b,
                        T.props.children
                      ), x.return = E, E = x;
                      break t;
                    }
                  } else if (b.elementType === P || typeof P == "object" && P !== null && P.$$typeof === it && kl(P) === b.type) {
                    l(
                      E,
                      b.sibling
                    ), x = a(b, T.props), Ea(x, T), x.return = E, E = x;
                    break t;
                  }
                  l(E, b);
                  break;
                } else e(E, b);
                b = b.sibling;
              }
              T.type === L ? (x = Zl(
                T.props.children,
                E.mode,
                x,
                T.key
              ), x.return = E, E = x) : (x = Mu(
                T.type,
                T.key,
                T.props,
                null,
                E.mode,
                x
              ), Ea(x, T), x.return = E, E = x);
            }
            return f(E);
          case Y:
            t: {
              for (P = T.key; b !== null; ) {
                if (b.key === P)
                  if (b.tag === 4 && b.stateNode.containerInfo === T.containerInfo && b.stateNode.implementation === T.implementation) {
                    l(
                      E,
                      b.sibling
                    ), x = a(b, T.children || []), x.return = E, E = x;
                    break t;
                  } else {
                    l(E, b);
                    break;
                  }
                else e(E, b);
                b = b.sibling;
              }
              x = Dc(T, E.mode, x), x.return = E, E = x;
            }
            return f(E);
          case it:
            return T = kl(T), Ot(
              E,
              b,
              T,
              x
            );
        }
        if (ft(T))
          return J(
            E,
            b,
            T,
            x
          );
        if (dt(T)) {
          if (P = dt(T), typeof P != "function") throw Error(o(150));
          return T = P.call(T), tt(
            E,
            b,
            T,
            x
          );
        }
        if (typeof T.then == "function")
          return Ot(
            E,
            b,
            Hu(T),
            x
          );
        if (T.$$typeof === G)
          return Ot(
            E,
            b,
            Du(E, T),
            x
          );
        Bu(E, T);
      }
      return typeof T == "string" && T !== "" || typeof T == "number" || typeof T == "bigint" ? (T = "" + T, b !== null && b.tag === 6 ? (l(E, b.sibling), x = a(b, T), x.return = E, E = x) : (l(E, b), x = xc(T, E.mode, x), x.return = E, E = x), f(E)) : l(E, b);
    }
    return function(E, b, T, x) {
      try {
        pa = 0;
        var P = Ot(
          E,
          b,
          T,
          x
        );
        return wn = null, P;
      } catch (k) {
        if (k === Un || k === Uu) throw k;
        var gt = de(29, k, null, E.mode);
        return gt.lanes = x, gt.return = E, gt;
      } finally {
      }
    };
  }
  var $l = or(!0), sr = or(!1), hl = !1;
  function Qc(t) {
    t.updateQueue = {
      baseState: t.memoizedState,
      firstBaseUpdate: null,
      lastBaseUpdate: null,
      shared: { pending: null, lanes: 0, hiddenCallbacks: null },
      callbacks: null
    };
  }
  function Zc(t, e) {
    t = t.updateQueue, e.updateQueue === t && (e.updateQueue = {
      baseState: t.baseState,
      firstBaseUpdate: t.firstBaseUpdate,
      lastBaseUpdate: t.lastBaseUpdate,
      shared: t.shared,
      callbacks: null
    });
  }
  function yl(t) {
    return { lane: t, tag: 0, payload: null, callback: null, next: null };
  }
  function gl(t, e, l) {
    var n = t.updateQueue;
    if (n === null) return null;
    if (n = n.shared, (St & 2) !== 0) {
      var a = n.pending;
      return a === null ? e.next = e : (e.next = a.next, a.next = e), n.pending = e, e = _u(t), Js(t, null, l), e;
    }
    return zu(t, n, e, l), _u(t);
  }
  function Ta(t, e, l) {
    if (e = e.updateQueue, e !== null && (e = e.shared, (l & 4194048) !== 0)) {
      var n = e.lanes;
      n &= t.pendingLanes, l |= n, e.lanes = l, es(t, l);
    }
  }
  function Vc(t, e) {
    var l = t.updateQueue, n = t.alternate;
    if (n !== null && (n = n.updateQueue, l === n)) {
      var a = null, u = null;
      if (l = l.firstBaseUpdate, l !== null) {
        do {
          var f = {
            lane: l.lane,
            tag: l.tag,
            payload: l.payload,
            callback: null,
            next: null
          };
          u === null ? a = u = f : u = u.next = f, l = l.next;
        } while (l !== null);
        u === null ? a = u = e : u = u.next = e;
      } else a = u = e;
      l = {
        baseState: n.baseState,
        firstBaseUpdate: a,
        lastBaseUpdate: u,
        shared: n.shared,
        callbacks: n.callbacks
      }, t.updateQueue = l;
      return;
    }
    t = l.lastBaseUpdate, t === null ? l.firstBaseUpdate = e : t.next = e, l.lastBaseUpdate = e;
  }
  var Kc = !1;
  function Aa() {
    if (Kc) {
      var t = Nn;
      if (t !== null) throw t;
    }
  }
  function Ra(t, e, l, n) {
    Kc = !1;
    var a = t.updateQueue;
    hl = !1;
    var u = a.firstBaseUpdate, f = a.lastBaseUpdate, r = a.shared.pending;
    if (r !== null) {
      a.shared.pending = null;
      var y = r, A = y.next;
      y.next = null, f === null ? u = A : f.next = A, f = y;
      var C = t.alternate;
      C !== null && (C = C.updateQueue, r = C.lastBaseUpdate, r !== f && (r === null ? C.firstBaseUpdate = A : r.next = A, C.lastBaseUpdate = y));
    }
    if (u !== null) {
      var U = a.baseState;
      f = 0, C = A = y = null, r = u;
      do {
        var O = r.lane & -536870913, _ = O !== r.lane;
        if (_ ? (vt & O) === O : (n & O) === O) {
          O !== 0 && O === Dn && (Kc = !0), C !== null && (C = C.next = {
            lane: 0,
            tag: r.tag,
            payload: r.payload,
            callback: null,
            next: null
          });
          t: {
            var J = t, tt = r;
            O = e;
            var Ot = l;
            switch (tt.tag) {
              case 1:
                if (J = tt.payload, typeof J == "function") {
                  U = J.call(Ot, U, O);
                  break t;
                }
                U = J;
                break t;
              case 3:
                J.flags = J.flags & -65537 | 128;
              case 0:
                if (J = tt.payload, O = typeof J == "function" ? J.call(Ot, U, O) : J, O == null) break t;
                U = z({}, U, O);
                break t;
              case 2:
                hl = !0;
            }
          }
          O = r.callback, O !== null && (t.flags |= 64, _ && (t.flags |= 8192), _ = a.callbacks, _ === null ? a.callbacks = [O] : _.push(O));
        } else
          _ = {
            lane: O,
            tag: r.tag,
            payload: r.payload,
            callback: r.callback,
            next: null
          }, C === null ? (A = C = _, y = U) : C = C.next = _, f |= O;
        if (r = r.next, r === null) {
          if (r = a.shared.pending, r === null)
            break;
          _ = r, r = _.next, _.next = null, a.lastBaseUpdate = _, a.shared.pending = null;
        }
      } while (!0);
      C === null && (y = U), a.baseState = y, a.firstBaseUpdate = A, a.lastBaseUpdate = C, u === null && (a.shared.lanes = 0), Tl |= f, t.lanes = f, t.memoizedState = U;
    }
  }
  function rr(t, e) {
    if (typeof t != "function")
      throw Error(o(191, t));
    t.call(e);
  }
  function dr(t, e) {
    var l = t.callbacks;
    if (l !== null)
      for (t.callbacks = null, t = 0; t < l.length; t++)
        rr(l[t], e);
  }
  var Hn = g(null), qu = g(0);
  function mr(t, e) {
    t = nl, j(qu, t), j(Hn, e), nl = t | e.baseLanes;
  }
  function Jc() {
    j(qu, nl), j(Hn, Hn.current);
  }
  function Wc() {
    nl = qu.current, w(Hn), w(qu);
  }
  var me = g(null), _e = null;
  function bl(t) {
    var e = t.alternate;
    j(Bt, Bt.current & 1), j(me, t), _e === null && (e === null || Hn.current !== null || e.memoizedState !== null) && (_e = t);
  }
  function kc(t) {
    j(Bt, Bt.current), j(me, t), _e === null && (_e = t);
  }
  function vr(t) {
    t.tag === 22 ? (j(Bt, Bt.current), j(me, t), _e === null && (_e = t)) : Sl();
  }
  function Sl() {
    j(Bt, Bt.current), j(me, me.current);
  }
  function ve(t) {
    w(me), _e === t && (_e = null), w(Bt);
  }
  var Bt = g(0);
  function Lu(t) {
    for (var e = t; e !== null; ) {
      if (e.tag === 13) {
        var l = e.memoizedState;
        if (l !== null && (l = l.dehydrated, l === null || lo(l) || no(l)))
          return e;
      } else if (e.tag === 19 && (e.memoizedProps.revealOrder === "forwards" || e.memoizedProps.revealOrder === "backwards" || e.memoizedProps.revealOrder === "unstable_legacy-backwards" || e.memoizedProps.revealOrder === "together")) {
        if ((e.flags & 128) !== 0) return e;
      } else if (e.child !== null) {
        e.child.return = e, e = e.child;
        continue;
      }
      if (e === t) break;
      for (; e.sibling === null; ) {
        if (e.return === null || e.return === t) return null;
        e = e.return;
      }
      e.sibling.return = e.return, e = e.sibling;
    }
    return null;
  }
  var ke = 0, ct = null, At = null, jt = null, Yu = !1, Bn = !1, Il = !1, ju = 0, Oa = 0, qn = null, xy = 0;
  function wt() {
    throw Error(o(321));
  }
  function Fc(t, e) {
    if (e === null) return !1;
    for (var l = 0; l < e.length && l < t.length; l++)
      if (!re(t[l], e[l])) return !1;
    return !0;
  }
  function $c(t, e, l, n, a, u) {
    return ke = u, ct = e, e.memoizedState = null, e.updateQueue = null, e.lanes = 0, R.H = t === null || t.memoizedState === null ? $r : mf, Il = !1, u = l(n, a), Il = !1, Bn && (u = yr(
      e,
      l,
      n,
      a
    )), hr(t), u;
  }
  function hr(t) {
    R.H = Ma;
    var e = At !== null && At.next !== null;
    if (ke = 0, jt = At = ct = null, Yu = !1, Oa = 0, qn = null, e) throw Error(o(300));
    t === null || Xt || (t = t.dependencies, t !== null && xu(t) && (Xt = !0));
  }
  function yr(t, e, l, n) {
    ct = t;
    var a = 0;
    do {
      if (Bn && (qn = null), Oa = 0, Bn = !1, 25 <= a) throw Error(o(301));
      if (a += 1, jt = At = null, t.updateQueue != null) {
        var u = t.updateQueue;
        u.lastEffect = null, u.events = null, u.stores = null, u.memoCache != null && (u.memoCache.index = 0);
      }
      R.H = Ir, u = e(l, n);
    } while (Bn);
    return u;
  }
  function Dy() {
    var t = R.H, e = t.useState()[0];
    return e = typeof e.then == "function" ? za(e) : e, t = t.useState()[0], (At !== null ? At.memoizedState : null) !== t && (ct.flags |= 1024), e;
  }
  function Ic() {
    var t = ju !== 0;
    return ju = 0, t;
  }
  function Pc(t, e, l) {
    e.updateQueue = t.updateQueue, e.flags &= -2053, t.lanes &= ~l;
  }
  function tf(t) {
    if (Yu) {
      for (t = t.memoizedState; t !== null; ) {
        var e = t.queue;
        e !== null && (e.pending = null), t = t.next;
      }
      Yu = !1;
    }
    ke = 0, jt = At = ct = null, Bn = !1, Oa = ju = 0, qn = null;
  }
  function te() {
    var t = {
      memoizedState: null,
      baseState: null,
      baseQueue: null,
      queue: null,
      next: null
    };
    return jt === null ? ct.memoizedState = jt = t : jt = jt.next = t, jt;
  }
  function qt() {
    if (At === null) {
      var t = ct.alternate;
      t = t !== null ? t.memoizedState : null;
    } else t = At.next;
    var e = jt === null ? ct.memoizedState : jt.next;
    if (e !== null)
      jt = e, At = t;
    else {
      if (t === null)
        throw ct.alternate === null ? Error(o(467)) : Error(o(310));
      At = t, t = {
        memoizedState: At.memoizedState,
        baseState: At.baseState,
        baseQueue: At.baseQueue,
        queue: At.queue,
        next: null
      }, jt === null ? ct.memoizedState = jt = t : jt = jt.next = t;
    }
    return jt;
  }
  function Xu() {
    return { lastEffect: null, events: null, stores: null, memoCache: null };
  }
  function za(t) {
    var e = Oa;
    return Oa += 1, qn === null && (qn = []), t = ir(qn, t, e), e = ct, (jt === null ? e.memoizedState : jt.next) === null && (e = e.alternate, R.H = e === null || e.memoizedState === null ? $r : mf), t;
  }
  function Gu(t) {
    if (t !== null && typeof t == "object") {
      if (typeof t.then == "function") return za(t);
      if (t.$$typeof === G) return Ft(t);
    }
    throw Error(o(438, String(t)));
  }
  function ef(t) {
    var e = null, l = ct.updateQueue;
    if (l !== null && (e = l.memoCache), e == null) {
      var n = ct.alternate;
      n !== null && (n = n.updateQueue, n !== null && (n = n.memoCache, n != null && (e = {
        data: n.data.map(function(a) {
          return a.slice();
        }),
        index: 0
      })));
    }
    if (e == null && (e = { data: [], index: 0 }), l === null && (l = Xu(), ct.updateQueue = l), l.memoCache = e, l = e.data[e.index], l === void 0)
      for (l = e.data[e.index] = Array(t), n = 0; n < t; n++)
        l[n] = Tt;
    return e.index++, l;
  }
  function Fe(t, e) {
    return typeof e == "function" ? e(t) : e;
  }
  function Qu(t) {
    var e = qt();
    return lf(e, At, t);
  }
  function lf(t, e, l) {
    var n = t.queue;
    if (n === null) throw Error(o(311));
    n.lastRenderedReducer = l;
    var a = t.baseQueue, u = n.pending;
    if (u !== null) {
      if (a !== null) {
        var f = a.next;
        a.next = u.next, u.next = f;
      }
      e.baseQueue = a = u, n.pending = null;
    }
    if (u = t.baseState, a === null) t.memoizedState = u;
    else {
      e = a.next;
      var r = f = null, y = null, A = e, C = !1;
      do {
        var U = A.lane & -536870913;
        if (U !== A.lane ? (vt & U) === U : (ke & U) === U) {
          var O = A.revertLane;
          if (O === 0)
            y !== null && (y = y.next = {
              lane: 0,
              revertLane: 0,
              gesture: null,
              action: A.action,
              hasEagerState: A.hasEagerState,
              eagerState: A.eagerState,
              next: null
            }), U === Dn && (C = !0);
          else if ((ke & O) === O) {
            A = A.next, O === Dn && (C = !0);
            continue;
          } else
            U = {
              lane: 0,
              revertLane: A.revertLane,
              gesture: null,
              action: A.action,
              hasEagerState: A.hasEagerState,
              eagerState: A.eagerState,
              next: null
            }, y === null ? (r = y = U, f = u) : y = y.next = U, ct.lanes |= O, Tl |= O;
          U = A.action, Il && l(u, U), u = A.hasEagerState ? A.eagerState : l(u, U);
        } else
          O = {
            lane: U,
            revertLane: A.revertLane,
            gesture: A.gesture,
            action: A.action,
            hasEagerState: A.hasEagerState,
            eagerState: A.eagerState,
            next: null
          }, y === null ? (r = y = O, f = u) : y = y.next = O, ct.lanes |= U, Tl |= U;
        A = A.next;
      } while (A !== null && A !== e);
      if (y === null ? f = u : y.next = r, !re(u, t.memoizedState) && (Xt = !0, C && (l = Nn, l !== null)))
        throw l;
      t.memoizedState = u, t.baseState = f, t.baseQueue = y, n.lastRenderedState = u;
    }
    return a === null && (n.lanes = 0), [t.memoizedState, n.dispatch];
  }
  function nf(t) {
    var e = qt(), l = e.queue;
    if (l === null) throw Error(o(311));
    l.lastRenderedReducer = t;
    var n = l.dispatch, a = l.pending, u = e.memoizedState;
    if (a !== null) {
      l.pending = null;
      var f = a = a.next;
      do
        u = t(u, f.action), f = f.next;
      while (f !== a);
      re(u, e.memoizedState) || (Xt = !0), e.memoizedState = u, e.baseQueue === null && (e.baseState = u), l.lastRenderedState = u;
    }
    return [u, n];
  }
  function gr(t, e, l) {
    var n = ct, a = qt(), u = yt;
    if (u) {
      if (l === void 0) throw Error(o(407));
      l = l();
    } else l = e();
    var f = !re(
      (At || a).memoizedState,
      l
    );
    if (f && (a.memoizedState = l, Xt = !0), a = a.queue, cf(pr.bind(null, n, a, t), [
      t
    ]), a.getSnapshot !== e || f || jt !== null && jt.memoizedState.tag & 1) {
      if (n.flags |= 2048, Ln(
        9,
        { destroy: void 0 },
        Sr.bind(
          null,
          n,
          a,
          l,
          e
        ),
        null
      ), Mt === null) throw Error(o(349));
      u || (ke & 127) !== 0 || br(n, e, l);
    }
    return l;
  }
  function br(t, e, l) {
    t.flags |= 16384, t = { getSnapshot: e, value: l }, e = ct.updateQueue, e === null ? (e = Xu(), ct.updateQueue = e, e.stores = [t]) : (l = e.stores, l === null ? e.stores = [t] : l.push(t));
  }
  function Sr(t, e, l, n) {
    e.value = l, e.getSnapshot = n, Er(e) && Tr(t);
  }
  function pr(t, e, l) {
    return l(function() {
      Er(e) && Tr(t);
    });
  }
  function Er(t) {
    var e = t.getSnapshot;
    t = t.value;
    try {
      var l = e();
      return !re(t, l);
    } catch {
      return !0;
    }
  }
  function Tr(t) {
    var e = Ql(t, 2);
    e !== null && ce(e, t, 2);
  }
  function af(t) {
    var e = te();
    if (typeof t == "function") {
      var l = t;
      if (t = l(), Il) {
        fl(!0);
        try {
          l();
        } finally {
          fl(!1);
        }
      }
    }
    return e.memoizedState = e.baseState = t, e.queue = {
      pending: null,
      lanes: 0,
      dispatch: null,
      lastRenderedReducer: Fe,
      lastRenderedState: t
    }, e;
  }
  function Ar(t, e, l, n) {
    return t.baseState = l, lf(
      t,
      At,
      typeof n == "function" ? n : Fe
    );
  }
  function Ny(t, e, l, n, a) {
    if (Ku(t)) throw Error(o(485));
    if (t = e.action, t !== null) {
      var u = {
        payload: a,
        action: t,
        next: null,
        isTransition: !0,
        status: "pending",
        value: null,
        reason: null,
        listeners: [],
        then: function(f) {
          u.listeners.push(f);
        }
      };
      R.T !== null ? l(!0) : u.isTransition = !1, n(u), l = e.pending, l === null ? (u.next = e.pending = u, Rr(e, u)) : (u.next = l.next, e.pending = l.next = u);
    }
  }
  function Rr(t, e) {
    var l = e.action, n = e.payload, a = t.state;
    if (e.isTransition) {
      var u = R.T, f = {};
      R.T = f;
      try {
        var r = l(a, n), y = R.S;
        y !== null && y(f, r), Or(t, e, r);
      } catch (A) {
        uf(t, e, A);
      } finally {
        u !== null && f.types !== null && (u.types = f.types), R.T = u;
      }
    } else
      try {
        u = l(a, n), Or(t, e, u);
      } catch (A) {
        uf(t, e, A);
      }
  }
  function Or(t, e, l) {
    l !== null && typeof l == "object" && typeof l.then == "function" ? l.then(
      function(n) {
        zr(t, e, n);
      },
      function(n) {
        return uf(t, e, n);
      }
    ) : zr(t, e, l);
  }
  function zr(t, e, l) {
    e.status = "fulfilled", e.value = l, _r(e), t.state = l, e = t.pending, e !== null && (l = e.next, l === e ? t.pending = null : (l = l.next, e.next = l, Rr(t, l)));
  }
  function uf(t, e, l) {
    var n = t.pending;
    if (t.pending = null, n !== null) {
      n = n.next;
      do
        e.status = "rejected", e.reason = l, _r(e), e = e.next;
      while (e !== n);
    }
    t.action = null;
  }
  function _r(t) {
    t = t.listeners;
    for (var e = 0; e < t.length; e++) (0, t[e])();
  }
  function Mr(t, e) {
    return e;
  }
  function Cr(t, e) {
    if (yt) {
      var l = Mt.formState;
      if (l !== null) {
        t: {
          var n = ct;
          if (yt) {
            if (xt) {
              e: {
                for (var a = xt, u = ze; a.nodeType !== 8; ) {
                  if (!u) {
                    a = null;
                    break e;
                  }
                  if (a = Me(
                    a.nextSibling
                  ), a === null) {
                    a = null;
                    break e;
                  }
                }
                u = a.data, a = u === "F!" || u === "F" ? a : null;
              }
              if (a) {
                xt = Me(
                  a.nextSibling
                ), n = a.data === "F!";
                break t;
              }
            }
            ml(n);
          }
          n = !1;
        }
        n && (e = l[0]);
      }
    }
    return l = te(), l.memoizedState = l.baseState = e, n = {
      pending: null,
      lanes: 0,
      dispatch: null,
      lastRenderedReducer: Mr,
      lastRenderedState: e
    }, l.queue = n, l = Wr.bind(
      null,
      ct,
      n
    ), n.dispatch = l, n = af(!1), u = df.bind(
      null,
      ct,
      !1,
      n.queue
    ), n = te(), a = {
      state: e,
      dispatch: null,
      action: t,
      pending: null
    }, n.queue = a, l = Ny.bind(
      null,
      ct,
      a,
      u,
      l
    ), a.dispatch = l, n.memoizedState = t, [e, l, !1];
  }
  function xr(t) {
    var e = qt();
    return Dr(e, At, t);
  }
  function Dr(t, e, l) {
    if (e = lf(
      t,
      e,
      Mr
    )[0], t = Qu(Fe)[0], typeof e == "object" && e !== null && typeof e.then == "function")
      try {
        var n = za(e);
      } catch (f) {
        throw f === Un ? Uu : f;
      }
    else n = e;
    e = qt();
    var a = e.queue, u = a.dispatch;
    return l !== e.memoizedState && (ct.flags |= 2048, Ln(
      9,
      { destroy: void 0 },
      Uy.bind(null, a, l),
      null
    )), [n, u, t];
  }
  function Uy(t, e) {
    t.action = e;
  }
  function Nr(t) {
    var e = qt(), l = At;
    if (l !== null)
      return Dr(e, l, t);
    qt(), e = e.memoizedState, l = qt();
    var n = l.queue.dispatch;
    return l.memoizedState = t, [e, n, !1];
  }
  function Ln(t, e, l, n) {
    return t = { tag: t, create: l, deps: n, inst: e, next: null }, e = ct.updateQueue, e === null && (e = Xu(), ct.updateQueue = e), l = e.lastEffect, l === null ? e.lastEffect = t.next = t : (n = l.next, l.next = t, t.next = n, e.lastEffect = t), t;
  }
  function Ur() {
    return qt().memoizedState;
  }
  function Zu(t, e, l, n) {
    var a = te();
    ct.flags |= t, a.memoizedState = Ln(
      1 | e,
      { destroy: void 0 },
      l,
      n === void 0 ? null : n
    );
  }
  function Vu(t, e, l, n) {
    var a = qt();
    n = n === void 0 ? null : n;
    var u = a.memoizedState.inst;
    At !== null && n !== null && Fc(n, At.memoizedState.deps) ? a.memoizedState = Ln(e, u, l, n) : (ct.flags |= t, a.memoizedState = Ln(
      1 | e,
      u,
      l,
      n
    ));
  }
  function wr(t, e) {
    Zu(8390656, 8, t, e);
  }
  function cf(t, e) {
    Vu(2048, 8, t, e);
  }
  function wy(t) {
    ct.flags |= 4;
    var e = ct.updateQueue;
    if (e === null)
      e = Xu(), ct.updateQueue = e, e.events = [t];
    else {
      var l = e.events;
      l === null ? e.events = [t] : l.push(t);
    }
  }
  function Hr(t) {
    var e = qt().memoizedState;
    return wy({ ref: e, nextImpl: t }), function() {
      if ((St & 2) !== 0) throw Error(o(440));
      return e.impl.apply(void 0, arguments);
    };
  }
  function Br(t, e) {
    return Vu(4, 2, t, e);
  }
  function qr(t, e) {
    return Vu(4, 4, t, e);
  }
  function Lr(t, e) {
    if (typeof e == "function") {
      t = t();
      var l = e(t);
      return function() {
        typeof l == "function" ? l() : e(null);
      };
    }
    if (e != null)
      return t = t(), e.current = t, function() {
        e.current = null;
      };
  }
  function Yr(t, e, l) {
    l = l != null ? l.concat([t]) : null, Vu(4, 4, Lr.bind(null, e, t), l);
  }
  function ff() {
  }
  function jr(t, e) {
    var l = qt();
    e = e === void 0 ? null : e;
    var n = l.memoizedState;
    return e !== null && Fc(e, n[1]) ? n[0] : (l.memoizedState = [t, e], t);
  }
  function Xr(t, e) {
    var l = qt();
    e = e === void 0 ? null : e;
    var n = l.memoizedState;
    if (e !== null && Fc(e, n[1]))
      return n[0];
    if (n = t(), Il) {
      fl(!0);
      try {
        t();
      } finally {
        fl(!1);
      }
    }
    return l.memoizedState = [n, e], n;
  }
  function of(t, e, l) {
    return l === void 0 || (ke & 1073741824) !== 0 && (vt & 261930) === 0 ? t.memoizedState = e : (t.memoizedState = l, t = Gd(), ct.lanes |= t, Tl |= t, l);
  }
  function Gr(t, e, l, n) {
    return re(l, e) ? l : Hn.current !== null ? (t = of(t, l, n), re(t, e) || (Xt = !0), t) : (ke & 42) === 0 || (ke & 1073741824) !== 0 && (vt & 261930) === 0 ? (Xt = !0, t.memoizedState = l) : (t = Gd(), ct.lanes |= t, Tl |= t, e);
  }
  function Qr(t, e, l, n, a) {
    var u = B.p;
    B.p = u !== 0 && 8 > u ? u : 8;
    var f = R.T, r = {};
    R.T = r, df(t, !1, e, l);
    try {
      var y = a(), A = R.S;
      if (A !== null && A(r, y), y !== null && typeof y == "object" && typeof y.then == "function") {
        var C = Cy(
          y,
          n
        );
        _a(
          t,
          e,
          C,
          ge(t)
        );
      } else
        _a(
          t,
          e,
          n,
          ge(t)
        );
    } catch (U) {
      _a(
        t,
        e,
        { then: function() {
        }, status: "rejected", reason: U },
        ge()
      );
    } finally {
      B.p = u, f !== null && r.types !== null && (f.types = r.types), R.T = f;
    }
  }
  function Hy() {
  }
  function sf(t, e, l, n) {
    if (t.tag !== 5) throw Error(o(476));
    var a = Zr(t).queue;
    Qr(
      t,
      a,
      e,
      H,
      l === null ? Hy : function() {
        return Vr(t), l(n);
      }
    );
  }
  function Zr(t) {
    var e = t.memoizedState;
    if (e !== null) return e;
    e = {
      memoizedState: H,
      baseState: H,
      baseQueue: null,
      queue: {
        pending: null,
        lanes: 0,
        dispatch: null,
        lastRenderedReducer: Fe,
        lastRenderedState: H
      },
      next: null
    };
    var l = {};
    return e.next = {
      memoizedState: l,
      baseState: l,
      baseQueue: null,
      queue: {
        pending: null,
        lanes: 0,
        dispatch: null,
        lastRenderedReducer: Fe,
        lastRenderedState: l
      },
      next: null
    }, t.memoizedState = e, t = t.alternate, t !== null && (t.memoizedState = e), e;
  }
  function Vr(t) {
    var e = Zr(t);
    e.next === null && (e = t.alternate.memoizedState), _a(
      t,
      e.next.queue,
      {},
      ge()
    );
  }
  function rf() {
    return Ft(Za);
  }
  function Kr() {
    return qt().memoizedState;
  }
  function Jr() {
    return qt().memoizedState;
  }
  function By(t) {
    for (var e = t.return; e !== null; ) {
      switch (e.tag) {
        case 24:
        case 3:
          var l = ge();
          t = yl(l);
          var n = gl(e, t, l);
          n !== null && (ce(n, e, l), Ta(n, e, l)), e = { cache: Yc() }, t.payload = e;
          return;
      }
      e = e.return;
    }
  }
  function qy(t, e, l) {
    var n = ge();
    l = {
      lane: n,
      revertLane: 0,
      gesture: null,
      action: l,
      hasEagerState: !1,
      eagerState: null,
      next: null
    }, Ku(t) ? kr(e, l) : (l = Mc(t, e, l, n), l !== null && (ce(l, t, n), Fr(l, e, n)));
  }
  function Wr(t, e, l) {
    var n = ge();
    _a(t, e, l, n);
  }
  function _a(t, e, l, n) {
    var a = {
      lane: n,
      revertLane: 0,
      gesture: null,
      action: l,
      hasEagerState: !1,
      eagerState: null,
      next: null
    };
    if (Ku(t)) kr(e, a);
    else {
      var u = t.alternate;
      if (t.lanes === 0 && (u === null || u.lanes === 0) && (u = e.lastRenderedReducer, u !== null))
        try {
          var f = e.lastRenderedState, r = u(f, l);
          if (a.hasEagerState = !0, a.eagerState = r, re(r, f))
            return zu(t, e, a, 0), Mt === null && Ou(), !1;
        } catch {
        } finally {
        }
      if (l = Mc(t, e, a, n), l !== null)
        return ce(l, t, n), Fr(l, e, n), !0;
    }
    return !1;
  }
  function df(t, e, l, n) {
    if (n = {
      lane: 2,
      revertLane: Zf(),
      gesture: null,
      action: n,
      hasEagerState: !1,
      eagerState: null,
      next: null
    }, Ku(t)) {
      if (e) throw Error(o(479));
    } else
      e = Mc(
        t,
        l,
        n,
        2
      ), e !== null && ce(e, t, 2);
  }
  function Ku(t) {
    var e = t.alternate;
    return t === ct || e !== null && e === ct;
  }
  function kr(t, e) {
    Bn = Yu = !0;
    var l = t.pending;
    l === null ? e.next = e : (e.next = l.next, l.next = e), t.pending = e;
  }
  function Fr(t, e, l) {
    if ((l & 4194048) !== 0) {
      var n = e.lanes;
      n &= t.pendingLanes, l |= n, e.lanes = l, es(t, l);
    }
  }
  var Ma = {
    readContext: Ft,
    use: Gu,
    useCallback: wt,
    useContext: wt,
    useEffect: wt,
    useImperativeHandle: wt,
    useLayoutEffect: wt,
    useInsertionEffect: wt,
    useMemo: wt,
    useReducer: wt,
    useRef: wt,
    useState: wt,
    useDebugValue: wt,
    useDeferredValue: wt,
    useTransition: wt,
    useSyncExternalStore: wt,
    useId: wt,
    useHostTransitionStatus: wt,
    useFormState: wt,
    useActionState: wt,
    useOptimistic: wt,
    useMemoCache: wt,
    useCacheRefresh: wt
  };
  Ma.useEffectEvent = wt;
  var $r = {
    readContext: Ft,
    use: Gu,
    useCallback: function(t, e) {
      return te().memoizedState = [
        t,
        e === void 0 ? null : e
      ], t;
    },
    useContext: Ft,
    useEffect: wr,
    useImperativeHandle: function(t, e, l) {
      l = l != null ? l.concat([t]) : null, Zu(
        4194308,
        4,
        Lr.bind(null, e, t),
        l
      );
    },
    useLayoutEffect: function(t, e) {
      return Zu(4194308, 4, t, e);
    },
    useInsertionEffect: function(t, e) {
      Zu(4, 2, t, e);
    },
    useMemo: function(t, e) {
      var l = te();
      e = e === void 0 ? null : e;
      var n = t();
      if (Il) {
        fl(!0);
        try {
          t();
        } finally {
          fl(!1);
        }
      }
      return l.memoizedState = [n, e], n;
    },
    useReducer: function(t, e, l) {
      var n = te();
      if (l !== void 0) {
        var a = l(e);
        if (Il) {
          fl(!0);
          try {
            l(e);
          } finally {
            fl(!1);
          }
        }
      } else a = e;
      return n.memoizedState = n.baseState = a, t = {
        pending: null,
        lanes: 0,
        dispatch: null,
        lastRenderedReducer: t,
        lastRenderedState: a
      }, n.queue = t, t = t.dispatch = qy.bind(
        null,
        ct,
        t
      ), [n.memoizedState, t];
    },
    useRef: function(t) {
      var e = te();
      return t = { current: t }, e.memoizedState = t;
    },
    useState: function(t) {
      t = af(t);
      var e = t.queue, l = Wr.bind(null, ct, e);
      return e.dispatch = l, [t.memoizedState, l];
    },
    useDebugValue: ff,
    useDeferredValue: function(t, e) {
      var l = te();
      return of(l, t, e);
    },
    useTransition: function() {
      var t = af(!1);
      return t = Qr.bind(
        null,
        ct,
        t.queue,
        !0,
        !1
      ), te().memoizedState = t, [!1, t];
    },
    useSyncExternalStore: function(t, e, l) {
      var n = ct, a = te();
      if (yt) {
        if (l === void 0)
          throw Error(o(407));
        l = l();
      } else {
        if (l = e(), Mt === null)
          throw Error(o(349));
        (vt & 127) !== 0 || br(n, e, l);
      }
      a.memoizedState = l;
      var u = { value: l, getSnapshot: e };
      return a.queue = u, wr(pr.bind(null, n, u, t), [
        t
      ]), n.flags |= 2048, Ln(
        9,
        { destroy: void 0 },
        Sr.bind(
          null,
          n,
          u,
          l,
          e
        ),
        null
      ), l;
    },
    useId: function() {
      var t = te(), e = Mt.identifierPrefix;
      if (yt) {
        var l = we, n = Ue;
        l = (n & ~(1 << 32 - se(n) - 1)).toString(32) + l, e = "_" + e + "R_" + l, l = ju++, 0 < l && (e += "H" + l.toString(32)), e += "_";
      } else
        l = xy++, e = "_" + e + "r_" + l.toString(32) + "_";
      return t.memoizedState = e;
    },
    useHostTransitionStatus: rf,
    useFormState: Cr,
    useActionState: Cr,
    useOptimistic: function(t) {
      var e = te();
      e.memoizedState = e.baseState = t;
      var l = {
        pending: null,
        lanes: 0,
        dispatch: null,
        lastRenderedReducer: null,
        lastRenderedState: null
      };
      return e.queue = l, e = df.bind(
        null,
        ct,
        !0,
        l
      ), l.dispatch = e, [t, e];
    },
    useMemoCache: ef,
    useCacheRefresh: function() {
      return te().memoizedState = By.bind(
        null,
        ct
      );
    },
    useEffectEvent: function(t) {
      var e = te(), l = { impl: t };
      return e.memoizedState = l, function() {
        if ((St & 2) !== 0)
          throw Error(o(440));
        return l.impl.apply(void 0, arguments);
      };
    }
  }, mf = {
    readContext: Ft,
    use: Gu,
    useCallback: jr,
    useContext: Ft,
    useEffect: cf,
    useImperativeHandle: Yr,
    useInsertionEffect: Br,
    useLayoutEffect: qr,
    useMemo: Xr,
    useReducer: Qu,
    useRef: Ur,
    useState: function() {
      return Qu(Fe);
    },
    useDebugValue: ff,
    useDeferredValue: function(t, e) {
      var l = qt();
      return Gr(
        l,
        At.memoizedState,
        t,
        e
      );
    },
    useTransition: function() {
      var t = Qu(Fe)[0], e = qt().memoizedState;
      return [
        typeof t == "boolean" ? t : za(t),
        e
      ];
    },
    useSyncExternalStore: gr,
    useId: Kr,
    useHostTransitionStatus: rf,
    useFormState: xr,
    useActionState: xr,
    useOptimistic: function(t, e) {
      var l = qt();
      return Ar(l, At, t, e);
    },
    useMemoCache: ef,
    useCacheRefresh: Jr
  };
  mf.useEffectEvent = Hr;
  var Ir = {
    readContext: Ft,
    use: Gu,
    useCallback: jr,
    useContext: Ft,
    useEffect: cf,
    useImperativeHandle: Yr,
    useInsertionEffect: Br,
    useLayoutEffect: qr,
    useMemo: Xr,
    useReducer: nf,
    useRef: Ur,
    useState: function() {
      return nf(Fe);
    },
    useDebugValue: ff,
    useDeferredValue: function(t, e) {
      var l = qt();
      return At === null ? of(l, t, e) : Gr(
        l,
        At.memoizedState,
        t,
        e
      );
    },
    useTransition: function() {
      var t = nf(Fe)[0], e = qt().memoizedState;
      return [
        typeof t == "boolean" ? t : za(t),
        e
      ];
    },
    useSyncExternalStore: gr,
    useId: Kr,
    useHostTransitionStatus: rf,
    useFormState: Nr,
    useActionState: Nr,
    useOptimistic: function(t, e) {
      var l = qt();
      return At !== null ? Ar(l, At, t, e) : (l.baseState = t, [t, l.queue.dispatch]);
    },
    useMemoCache: ef,
    useCacheRefresh: Jr
  };
  Ir.useEffectEvent = Hr;
  function vf(t, e, l, n) {
    e = t.memoizedState, l = l(n, e), l = l == null ? e : z({}, e, l), t.memoizedState = l, t.lanes === 0 && (t.updateQueue.baseState = l);
  }
  var hf = {
    enqueueSetState: function(t, e, l) {
      t = t._reactInternals;
      var n = ge(), a = yl(n);
      a.payload = e, l != null && (a.callback = l), e = gl(t, a, n), e !== null && (ce(e, t, n), Ta(e, t, n));
    },
    enqueueReplaceState: function(t, e, l) {
      t = t._reactInternals;
      var n = ge(), a = yl(n);
      a.tag = 1, a.payload = e, l != null && (a.callback = l), e = gl(t, a, n), e !== null && (ce(e, t, n), Ta(e, t, n));
    },
    enqueueForceUpdate: function(t, e) {
      t = t._reactInternals;
      var l = ge(), n = yl(l);
      n.tag = 2, e != null && (n.callback = e), e = gl(t, n, l), e !== null && (ce(e, t, l), Ta(e, t, l));
    }
  };
  function Pr(t, e, l, n, a, u, f) {
    return t = t.stateNode, typeof t.shouldComponentUpdate == "function" ? t.shouldComponentUpdate(n, u, f) : e.prototype && e.prototype.isPureReactComponent ? !va(l, n) || !va(a, u) : !0;
  }
  function td(t, e, l, n) {
    t = e.state, typeof e.componentWillReceiveProps == "function" && e.componentWillReceiveProps(l, n), typeof e.UNSAFE_componentWillReceiveProps == "function" && e.UNSAFE_componentWillReceiveProps(l, n), e.state !== t && hf.enqueueReplaceState(e, e.state, null);
  }
  function Pl(t, e) {
    var l = e;
    if ("ref" in e) {
      l = {};
      for (var n in e)
        n !== "ref" && (l[n] = e[n]);
    }
    if (t = t.defaultProps) {
      l === e && (l = z({}, l));
      for (var a in t)
        l[a] === void 0 && (l[a] = t[a]);
    }
    return l;
  }
  function ed(t) {
    Ru(t);
  }
  function ld(t) {
    console.error(t);
  }
  function nd(t) {
    Ru(t);
  }
  function Ju(t, e) {
    try {
      var l = t.onUncaughtError;
      l(e.value, { componentStack: e.stack });
    } catch (n) {
      setTimeout(function() {
        throw n;
      });
    }
  }
  function ad(t, e, l) {
    try {
      var n = t.onCaughtError;
      n(l.value, {
        componentStack: l.stack,
        errorBoundary: e.tag === 1 ? e.stateNode : null
      });
    } catch (a) {
      setTimeout(function() {
        throw a;
      });
    }
  }
  function yf(t, e, l) {
    return l = yl(l), l.tag = 3, l.payload = { element: null }, l.callback = function() {
      Ju(t, e);
    }, l;
  }
  function ud(t) {
    return t = yl(t), t.tag = 3, t;
  }
  function id(t, e, l, n) {
    var a = l.type.getDerivedStateFromError;
    if (typeof a == "function") {
      var u = n.value;
      t.payload = function() {
        return a(u);
      }, t.callback = function() {
        ad(e, l, n);
      };
    }
    var f = l.stateNode;
    f !== null && typeof f.componentDidCatch == "function" && (t.callback = function() {
      ad(e, l, n), typeof a != "function" && (Al === null ? Al = /* @__PURE__ */ new Set([this]) : Al.add(this));
      var r = n.stack;
      this.componentDidCatch(n.value, {
        componentStack: r !== null ? r : ""
      });
    });
  }
  function Ly(t, e, l, n, a) {
    if (l.flags |= 32768, n !== null && typeof n == "object" && typeof n.then == "function") {
      if (e = l.alternate, e !== null && xn(
        e,
        l,
        a,
        !0
      ), l = me.current, l !== null) {
        switch (l.tag) {
          case 31:
          case 13:
            return _e === null ? ui() : l.alternate === null && Ht === 0 && (Ht = 3), l.flags &= -257, l.flags |= 65536, l.lanes = a, n === wu ? l.flags |= 16384 : (e = l.updateQueue, e === null ? l.updateQueue = /* @__PURE__ */ new Set([n]) : e.add(n), Xf(t, n, a)), !1;
          case 22:
            return l.flags |= 65536, n === wu ? l.flags |= 16384 : (e = l.updateQueue, e === null ? (e = {
              transitions: null,
              markerInstances: null,
              retryQueue: /* @__PURE__ */ new Set([n])
            }, l.updateQueue = e) : (l = e.retryQueue, l === null ? e.retryQueue = /* @__PURE__ */ new Set([n]) : l.add(n)), Xf(t, n, a)), !1;
        }
        throw Error(o(435, l.tag));
      }
      return Xf(t, n, a), ui(), !1;
    }
    if (yt)
      return e = me.current, e !== null ? ((e.flags & 65536) === 0 && (e.flags |= 256), e.flags |= 65536, e.lanes = a, n !== wc && (t = Error(o(422), { cause: n }), ga(Ae(t, l)))) : (n !== wc && (e = Error(o(423), {
        cause: n
      }), ga(
        Ae(e, l)
      )), t = t.current.alternate, t.flags |= 65536, a &= -a, t.lanes |= a, n = Ae(n, l), a = yf(
        t.stateNode,
        n,
        a
      ), Vc(t, a), Ht !== 4 && (Ht = 2)), !1;
    var u = Error(o(520), { cause: n });
    if (u = Ae(u, l), Ba === null ? Ba = [u] : Ba.push(u), Ht !== 4 && (Ht = 2), e === null) return !0;
    n = Ae(n, l), l = e;
    do {
      switch (l.tag) {
        case 3:
          return l.flags |= 65536, t = a & -a, l.lanes |= t, t = yf(l.stateNode, n, t), Vc(l, t), !1;
        case 1:
          if (e = l.type, u = l.stateNode, (l.flags & 128) === 0 && (typeof e.getDerivedStateFromError == "function" || u !== null && typeof u.componentDidCatch == "function" && (Al === null || !Al.has(u))))
            return l.flags |= 65536, a &= -a, l.lanes |= a, a = ud(a), id(
              a,
              t,
              l,
              n
            ), Vc(l, a), !1;
      }
      l = l.return;
    } while (l !== null);
    return !1;
  }
  var gf = Error(o(461)), Xt = !1;
  function $t(t, e, l, n) {
    e.child = t === null ? sr(e, null, l, n) : $l(
      e,
      t.child,
      l,
      n
    );
  }
  function cd(t, e, l, n, a) {
    l = l.render;
    var u = e.ref;
    if ("ref" in n) {
      var f = {};
      for (var r in n)
        r !== "ref" && (f[r] = n[r]);
    } else f = n;
    return Jl(e), n = $c(
      t,
      e,
      l,
      f,
      u,
      a
    ), r = Ic(), t !== null && !Xt ? (Pc(t, e, a), $e(t, e, a)) : (yt && r && Nc(e), e.flags |= 1, $t(t, e, n, a), e.child);
  }
  function fd(t, e, l, n, a) {
    if (t === null) {
      var u = l.type;
      return typeof u == "function" && !Cc(u) && u.defaultProps === void 0 && l.compare === null ? (e.tag = 15, e.type = u, od(
        t,
        e,
        u,
        n,
        a
      )) : (t = Mu(
        l.type,
        null,
        n,
        e,
        e.mode,
        a
      ), t.ref = e.ref, t.return = e, e.child = t);
    }
    if (u = t.child, !Of(t, a)) {
      var f = u.memoizedProps;
      if (l = l.compare, l = l !== null ? l : va, l(f, n) && t.ref === e.ref)
        return $e(t, e, a);
    }
    return e.flags |= 1, t = Ve(u, n), t.ref = e.ref, t.return = e, e.child = t;
  }
  function od(t, e, l, n, a) {
    if (t !== null) {
      var u = t.memoizedProps;
      if (va(u, n) && t.ref === e.ref)
        if (Xt = !1, e.pendingProps = n = u, Of(t, a))
          (t.flags & 131072) !== 0 && (Xt = !0);
        else
          return e.lanes = t.lanes, $e(t, e, a);
    }
    return bf(
      t,
      e,
      l,
      n,
      a
    );
  }
  function sd(t, e, l, n) {
    var a = n.children, u = t !== null ? t.memoizedState : null;
    if (t === null && e.stateNode === null && (e.stateNode = {
      _visibility: 1,
      _pendingMarkers: null,
      _retryCache: null,
      _transitions: null
    }), n.mode === "hidden") {
      if ((e.flags & 128) !== 0) {
        if (u = u !== null ? u.baseLanes | l : l, t !== null) {
          for (n = e.child = t.child, a = 0; n !== null; )
            a = a | n.lanes | n.childLanes, n = n.sibling;
          n = a & ~u;
        } else n = 0, e.child = null;
        return rd(
          t,
          e,
          u,
          l,
          n
        );
      }
      if ((l & 536870912) !== 0)
        e.memoizedState = { baseLanes: 0, cachePool: null }, t !== null && Nu(
          e,
          u !== null ? u.cachePool : null
        ), u !== null ? mr(e, u) : Jc(), vr(e);
      else
        return n = e.lanes = 536870912, rd(
          t,
          e,
          u !== null ? u.baseLanes | l : l,
          l,
          n
        );
    } else
      u !== null ? (Nu(e, u.cachePool), mr(e, u), Sl(), e.memoizedState = null) : (t !== null && Nu(e, null), Jc(), Sl());
    return $t(t, e, a, l), e.child;
  }
  function Ca(t, e) {
    return t !== null && t.tag === 22 || e.stateNode !== null || (e.stateNode = {
      _visibility: 1,
      _pendingMarkers: null,
      _retryCache: null,
      _transitions: null
    }), e.sibling;
  }
  function rd(t, e, l, n, a) {
    var u = Xc();
    return u = u === null ? null : { parent: Yt._currentValue, pool: u }, e.memoizedState = {
      baseLanes: l,
      cachePool: u
    }, t !== null && Nu(e, null), Jc(), vr(e), t !== null && xn(t, e, n, !0), e.childLanes = a, null;
  }
  function Wu(t, e) {
    return e = Fu(
      { mode: e.mode, children: e.children },
      t.mode
    ), e.ref = t.ref, t.child = e, e.return = t, e;
  }
  function dd(t, e, l) {
    return $l(e, t.child, null, l), t = Wu(e, e.pendingProps), t.flags |= 2, ve(e), e.memoizedState = null, t;
  }
  function Yy(t, e, l) {
    var n = e.pendingProps, a = (e.flags & 128) !== 0;
    if (e.flags &= -129, t === null) {
      if (yt) {
        if (n.mode === "hidden")
          return t = Wu(e, n), e.lanes = 536870912, Ca(null, t);
        if (kc(e), (t = xt) ? (t = Rm(
          t,
          ze
        ), t = t !== null && t.data === "&" ? t : null, t !== null && (e.memoizedState = {
          dehydrated: t,
          treeContext: rl !== null ? { id: Ue, overflow: we } : null,
          retryLane: 536870912,
          hydrationErrors: null
        }, l = ks(t), l.return = e, e.child = l, kt = e, xt = null)) : t = null, t === null) throw ml(e);
        return e.lanes = 536870912, null;
      }
      return Wu(e, n);
    }
    var u = t.memoizedState;
    if (u !== null) {
      var f = u.dehydrated;
      if (kc(e), a)
        if (e.flags & 256)
          e.flags &= -257, e = dd(
            t,
            e,
            l
          );
        else if (e.memoizedState !== null)
          e.child = t.child, e.flags |= 128, e = null;
        else throw Error(o(558));
      else if (Xt || xn(t, e, l, !1), a = (l & t.childLanes) !== 0, Xt || a) {
        if (n = Mt, n !== null && (f = ls(n, l), f !== 0 && f !== u.retryLane))
          throw u.retryLane = f, Ql(t, f), ce(n, t, f), gf;
        ui(), e = dd(
          t,
          e,
          l
        );
      } else
        t = u.treeContext, xt = Me(f.nextSibling), kt = e, yt = !0, dl = null, ze = !1, t !== null && Is(e, t), e = Wu(e, n), e.flags |= 4096;
      return e;
    }
    return t = Ve(t.child, {
      mode: n.mode,
      children: n.children
    }), t.ref = e.ref, e.child = t, t.return = e, t;
  }
  function ku(t, e) {
    var l = e.ref;
    if (l === null)
      t !== null && t.ref !== null && (e.flags |= 4194816);
    else {
      if (typeof l != "function" && typeof l != "object")
        throw Error(o(284));
      (t === null || t.ref !== l) && (e.flags |= 4194816);
    }
  }
  function bf(t, e, l, n, a) {
    return Jl(e), l = $c(
      t,
      e,
      l,
      n,
      void 0,
      a
    ), n = Ic(), t !== null && !Xt ? (Pc(t, e, a), $e(t, e, a)) : (yt && n && Nc(e), e.flags |= 1, $t(t, e, l, a), e.child);
  }
  function md(t, e, l, n, a, u) {
    return Jl(e), e.updateQueue = null, l = yr(
      e,
      n,
      l,
      a
    ), hr(t), n = Ic(), t !== null && !Xt ? (Pc(t, e, u), $e(t, e, u)) : (yt && n && Nc(e), e.flags |= 1, $t(t, e, l, u), e.child);
  }
  function vd(t, e, l, n, a) {
    if (Jl(e), e.stateNode === null) {
      var u = zn, f = l.contextType;
      typeof f == "object" && f !== null && (u = Ft(f)), u = new l(n, u), e.memoizedState = u.state !== null && u.state !== void 0 ? u.state : null, u.updater = hf, e.stateNode = u, u._reactInternals = e, u = e.stateNode, u.props = n, u.state = e.memoizedState, u.refs = {}, Qc(e), f = l.contextType, u.context = typeof f == "object" && f !== null ? Ft(f) : zn, u.state = e.memoizedState, f = l.getDerivedStateFromProps, typeof f == "function" && (vf(
        e,
        l,
        f,
        n
      ), u.state = e.memoizedState), typeof l.getDerivedStateFromProps == "function" || typeof u.getSnapshotBeforeUpdate == "function" || typeof u.UNSAFE_componentWillMount != "function" && typeof u.componentWillMount != "function" || (f = u.state, typeof u.componentWillMount == "function" && u.componentWillMount(), typeof u.UNSAFE_componentWillMount == "function" && u.UNSAFE_componentWillMount(), f !== u.state && hf.enqueueReplaceState(u, u.state, null), Ra(e, n, u, a), Aa(), u.state = e.memoizedState), typeof u.componentDidMount == "function" && (e.flags |= 4194308), n = !0;
    } else if (t === null) {
      u = e.stateNode;
      var r = e.memoizedProps, y = Pl(l, r);
      u.props = y;
      var A = u.context, C = l.contextType;
      f = zn, typeof C == "object" && C !== null && (f = Ft(C));
      var U = l.getDerivedStateFromProps;
      C = typeof U == "function" || typeof u.getSnapshotBeforeUpdate == "function", r = e.pendingProps !== r, C || typeof u.UNSAFE_componentWillReceiveProps != "function" && typeof u.componentWillReceiveProps != "function" || (r || A !== f) && td(
        e,
        u,
        n,
        f
      ), hl = !1;
      var O = e.memoizedState;
      u.state = O, Ra(e, n, u, a), Aa(), A = e.memoizedState, r || O !== A || hl ? (typeof U == "function" && (vf(
        e,
        l,
        U,
        n
      ), A = e.memoizedState), (y = hl || Pr(
        e,
        l,
        y,
        n,
        O,
        A,
        f
      )) ? (C || typeof u.UNSAFE_componentWillMount != "function" && typeof u.componentWillMount != "function" || (typeof u.componentWillMount == "function" && u.componentWillMount(), typeof u.UNSAFE_componentWillMount == "function" && u.UNSAFE_componentWillMount()), typeof u.componentDidMount == "function" && (e.flags |= 4194308)) : (typeof u.componentDidMount == "function" && (e.flags |= 4194308), e.memoizedProps = n, e.memoizedState = A), u.props = n, u.state = A, u.context = f, n = y) : (typeof u.componentDidMount == "function" && (e.flags |= 4194308), n = !1);
    } else {
      u = e.stateNode, Zc(t, e), f = e.memoizedProps, C = Pl(l, f), u.props = C, U = e.pendingProps, O = u.context, A = l.contextType, y = zn, typeof A == "object" && A !== null && (y = Ft(A)), r = l.getDerivedStateFromProps, (A = typeof r == "function" || typeof u.getSnapshotBeforeUpdate == "function") || typeof u.UNSAFE_componentWillReceiveProps != "function" && typeof u.componentWillReceiveProps != "function" || (f !== U || O !== y) && td(
        e,
        u,
        n,
        y
      ), hl = !1, O = e.memoizedState, u.state = O, Ra(e, n, u, a), Aa();
      var _ = e.memoizedState;
      f !== U || O !== _ || hl || t !== null && t.dependencies !== null && xu(t.dependencies) ? (typeof r == "function" && (vf(
        e,
        l,
        r,
        n
      ), _ = e.memoizedState), (C = hl || Pr(
        e,
        l,
        C,
        n,
        O,
        _,
        y
      ) || t !== null && t.dependencies !== null && xu(t.dependencies)) ? (A || typeof u.UNSAFE_componentWillUpdate != "function" && typeof u.componentWillUpdate != "function" || (typeof u.componentWillUpdate == "function" && u.componentWillUpdate(n, _, y), typeof u.UNSAFE_componentWillUpdate == "function" && u.UNSAFE_componentWillUpdate(
        n,
        _,
        y
      )), typeof u.componentDidUpdate == "function" && (e.flags |= 4), typeof u.getSnapshotBeforeUpdate == "function" && (e.flags |= 1024)) : (typeof u.componentDidUpdate != "function" || f === t.memoizedProps && O === t.memoizedState || (e.flags |= 4), typeof u.getSnapshotBeforeUpdate != "function" || f === t.memoizedProps && O === t.memoizedState || (e.flags |= 1024), e.memoizedProps = n, e.memoizedState = _), u.props = n, u.state = _, u.context = y, n = C) : (typeof u.componentDidUpdate != "function" || f === t.memoizedProps && O === t.memoizedState || (e.flags |= 4), typeof u.getSnapshotBeforeUpdate != "function" || f === t.memoizedProps && O === t.memoizedState || (e.flags |= 1024), n = !1);
    }
    return u = n, ku(t, e), n = (e.flags & 128) !== 0, u || n ? (u = e.stateNode, l = n && typeof l.getDerivedStateFromError != "function" ? null : u.render(), e.flags |= 1, t !== null && n ? (e.child = $l(
      e,
      t.child,
      null,
      a
    ), e.child = $l(
      e,
      null,
      l,
      a
    )) : $t(t, e, l, a), e.memoizedState = u.state, t = e.child) : t = $e(
      t,
      e,
      a
    ), t;
  }
  function hd(t, e, l, n) {
    return Vl(), e.flags |= 256, $t(t, e, l, n), e.child;
  }
  var Sf = {
    dehydrated: null,
    treeContext: null,
    retryLane: 0,
    hydrationErrors: null
  };
  function pf(t) {
    return { baseLanes: t, cachePool: ar() };
  }
  function Ef(t, e, l) {
    return t = t !== null ? t.childLanes & ~l : 0, e && (t |= ye), t;
  }
  function yd(t, e, l) {
    var n = e.pendingProps, a = !1, u = (e.flags & 128) !== 0, f;
    if ((f = u) || (f = t !== null && t.memoizedState === null ? !1 : (Bt.current & 2) !== 0), f && (a = !0, e.flags &= -129), f = (e.flags & 32) !== 0, e.flags &= -33, t === null) {
      if (yt) {
        if (a ? bl(e) : Sl(), (t = xt) ? (t = Rm(
          t,
          ze
        ), t = t !== null && t.data !== "&" ? t : null, t !== null && (e.memoizedState = {
          dehydrated: t,
          treeContext: rl !== null ? { id: Ue, overflow: we } : null,
          retryLane: 536870912,
          hydrationErrors: null
        }, l = ks(t), l.return = e, e.child = l, kt = e, xt = null)) : t = null, t === null) throw ml(e);
        return no(t) ? e.lanes = 32 : e.lanes = 536870912, null;
      }
      var r = n.children;
      return n = n.fallback, a ? (Sl(), a = e.mode, r = Fu(
        { mode: "hidden", children: r },
        a
      ), n = Zl(
        n,
        a,
        l,
        null
      ), r.return = e, n.return = e, r.sibling = n, e.child = r, n = e.child, n.memoizedState = pf(l), n.childLanes = Ef(
        t,
        f,
        l
      ), e.memoizedState = Sf, Ca(null, n)) : (bl(e), Tf(e, r));
    }
    var y = t.memoizedState;
    if (y !== null && (r = y.dehydrated, r !== null)) {
      if (u)
        e.flags & 256 ? (bl(e), e.flags &= -257, e = Af(
          t,
          e,
          l
        )) : e.memoizedState !== null ? (Sl(), e.child = t.child, e.flags |= 128, e = null) : (Sl(), r = n.fallback, a = e.mode, n = Fu(
          { mode: "visible", children: n.children },
          a
        ), r = Zl(
          r,
          a,
          l,
          null
        ), r.flags |= 2, n.return = e, r.return = e, n.sibling = r, e.child = n, $l(
          e,
          t.child,
          null,
          l
        ), n = e.child, n.memoizedState = pf(l), n.childLanes = Ef(
          t,
          f,
          l
        ), e.memoizedState = Sf, e = Ca(null, n));
      else if (bl(e), no(r)) {
        if (f = r.nextSibling && r.nextSibling.dataset, f) var A = f.dgst;
        f = A, n = Error(o(419)), n.stack = "", n.digest = f, ga({ value: n, source: null, stack: null }), e = Af(
          t,
          e,
          l
        );
      } else if (Xt || xn(t, e, l, !1), f = (l & t.childLanes) !== 0, Xt || f) {
        if (f = Mt, f !== null && (n = ls(f, l), n !== 0 && n !== y.retryLane))
          throw y.retryLane = n, Ql(t, n), ce(f, t, n), gf;
        lo(r) || ui(), e = Af(
          t,
          e,
          l
        );
      } else
        lo(r) ? (e.flags |= 192, e.child = t.child, e = null) : (t = y.treeContext, xt = Me(
          r.nextSibling
        ), kt = e, yt = !0, dl = null, ze = !1, t !== null && Is(e, t), e = Tf(
          e,
          n.children
        ), e.flags |= 4096);
      return e;
    }
    return a ? (Sl(), r = n.fallback, a = e.mode, y = t.child, A = y.sibling, n = Ve(y, {
      mode: "hidden",
      children: n.children
    }), n.subtreeFlags = y.subtreeFlags & 65011712, A !== null ? r = Ve(
      A,
      r
    ) : (r = Zl(
      r,
      a,
      l,
      null
    ), r.flags |= 2), r.return = e, n.return = e, n.sibling = r, e.child = n, Ca(null, n), n = e.child, r = t.child.memoizedState, r === null ? r = pf(l) : (a = r.cachePool, a !== null ? (y = Yt._currentValue, a = a.parent !== y ? { parent: y, pool: y } : a) : a = ar(), r = {
      baseLanes: r.baseLanes | l,
      cachePool: a
    }), n.memoizedState = r, n.childLanes = Ef(
      t,
      f,
      l
    ), e.memoizedState = Sf, Ca(t.child, n)) : (bl(e), l = t.child, t = l.sibling, l = Ve(l, {
      mode: "visible",
      children: n.children
    }), l.return = e, l.sibling = null, t !== null && (f = e.deletions, f === null ? (e.deletions = [t], e.flags |= 16) : f.push(t)), e.child = l, e.memoizedState = null, l);
  }
  function Tf(t, e) {
    return e = Fu(
      { mode: "visible", children: e },
      t.mode
    ), e.return = t, t.child = e;
  }
  function Fu(t, e) {
    return t = de(22, t, null, e), t.lanes = 0, t;
  }
  function Af(t, e, l) {
    return $l(e, t.child, null, l), t = Tf(
      e,
      e.pendingProps.children
    ), t.flags |= 2, e.memoizedState = null, t;
  }
  function gd(t, e, l) {
    t.lanes |= e;
    var n = t.alternate;
    n !== null && (n.lanes |= e), qc(t.return, e, l);
  }
  function Rf(t, e, l, n, a, u) {
    var f = t.memoizedState;
    f === null ? t.memoizedState = {
      isBackwards: e,
      rendering: null,
      renderingStartTime: 0,
      last: n,
      tail: l,
      tailMode: a,
      treeForkCount: u
    } : (f.isBackwards = e, f.rendering = null, f.renderingStartTime = 0, f.last = n, f.tail = l, f.tailMode = a, f.treeForkCount = u);
  }
  function bd(t, e, l) {
    var n = e.pendingProps, a = n.revealOrder, u = n.tail;
    n = n.children;
    var f = Bt.current, r = (f & 2) !== 0;
    if (r ? (f = f & 1 | 2, e.flags |= 128) : f &= 1, j(Bt, f), $t(t, e, n, l), n = yt ? ya : 0, !r && t !== null && (t.flags & 128) !== 0)
      t: for (t = e.child; t !== null; ) {
        if (t.tag === 13)
          t.memoizedState !== null && gd(t, l, e);
        else if (t.tag === 19)
          gd(t, l, e);
        else if (t.child !== null) {
          t.child.return = t, t = t.child;
          continue;
        }
        if (t === e) break t;
        for (; t.sibling === null; ) {
          if (t.return === null || t.return === e)
            break t;
          t = t.return;
        }
        t.sibling.return = t.return, t = t.sibling;
      }
    switch (a) {
      case "forwards":
        for (l = e.child, a = null; l !== null; )
          t = l.alternate, t !== null && Lu(t) === null && (a = l), l = l.sibling;
        l = a, l === null ? (a = e.child, e.child = null) : (a = l.sibling, l.sibling = null), Rf(
          e,
          !1,
          a,
          l,
          u,
          n
        );
        break;
      case "backwards":
      case "unstable_legacy-backwards":
        for (l = null, a = e.child, e.child = null; a !== null; ) {
          if (t = a.alternate, t !== null && Lu(t) === null) {
            e.child = a;
            break;
          }
          t = a.sibling, a.sibling = l, l = a, a = t;
        }
        Rf(
          e,
          !0,
          l,
          null,
          u,
          n
        );
        break;
      case "together":
        Rf(
          e,
          !1,
          null,
          null,
          void 0,
          n
        );
        break;
      default:
        e.memoizedState = null;
    }
    return e.child;
  }
  function $e(t, e, l) {
    if (t !== null && (e.dependencies = t.dependencies), Tl |= e.lanes, (l & e.childLanes) === 0)
      if (t !== null) {
        if (xn(
          t,
          e,
          l,
          !1
        ), (l & e.childLanes) === 0)
          return null;
      } else return null;
    if (t !== null && e.child !== t.child)
      throw Error(o(153));
    if (e.child !== null) {
      for (t = e.child, l = Ve(t, t.pendingProps), e.child = l, l.return = e; t.sibling !== null; )
        t = t.sibling, l = l.sibling = Ve(t, t.pendingProps), l.return = e;
      l.sibling = null;
    }
    return e.child;
  }
  function Of(t, e) {
    return (t.lanes & e) !== 0 ? !0 : (t = t.dependencies, !!(t !== null && xu(t)));
  }
  function jy(t, e, l) {
    switch (e.tag) {
      case 3:
        Jt(e, e.stateNode.containerInfo), vl(e, Yt, t.memoizedState.cache), Vl();
        break;
      case 27:
      case 5:
        ql(e);
        break;
      case 4:
        Jt(e, e.stateNode.containerInfo);
        break;
      case 10:
        vl(
          e,
          e.type,
          e.memoizedProps.value
        );
        break;
      case 31:
        if (e.memoizedState !== null)
          return e.flags |= 128, kc(e), null;
        break;
      case 13:
        var n = e.memoizedState;
        if (n !== null)
          return n.dehydrated !== null ? (bl(e), e.flags |= 128, null) : (l & e.child.childLanes) !== 0 ? yd(t, e, l) : (bl(e), t = $e(
            t,
            e,
            l
          ), t !== null ? t.sibling : null);
        bl(e);
        break;
      case 19:
        var a = (t.flags & 128) !== 0;
        if (n = (l & e.childLanes) !== 0, n || (xn(
          t,
          e,
          l,
          !1
        ), n = (l & e.childLanes) !== 0), a) {
          if (n)
            return bd(
              t,
              e,
              l
            );
          e.flags |= 128;
        }
        if (a = e.memoizedState, a !== null && (a.rendering = null, a.tail = null, a.lastEffect = null), j(Bt, Bt.current), n) break;
        return null;
      case 22:
        return e.lanes = 0, sd(
          t,
          e,
          l,
          e.pendingProps
        );
      case 24:
        vl(e, Yt, t.memoizedState.cache);
    }
    return $e(t, e, l);
  }
  function Sd(t, e, l) {
    if (t !== null)
      if (t.memoizedProps !== e.pendingProps)
        Xt = !0;
      else {
        if (!Of(t, l) && (e.flags & 128) === 0)
          return Xt = !1, jy(
            t,
            e,
            l
          );
        Xt = (t.flags & 131072) !== 0;
      }
    else
      Xt = !1, yt && (e.flags & 1048576) !== 0 && $s(e, ya, e.index);
    switch (e.lanes = 0, e.tag) {
      case 16:
        t: {
          var n = e.pendingProps;
          if (t = kl(e.elementType), e.type = t, typeof t == "function")
            Cc(t) ? (n = Pl(t, n), e.tag = 1, e = vd(
              null,
              e,
              t,
              n,
              l
            )) : (e.tag = 0, e = bf(
              null,
              e,
              t,
              n,
              l
            ));
          else {
            if (t != null) {
              var a = t.$$typeof;
              if (a === $) {
                e.tag = 11, e = cd(
                  null,
                  e,
                  t,
                  n,
                  l
                );
                break t;
              } else if (a === V) {
                e.tag = 14, e = fd(
                  null,
                  e,
                  t,
                  n,
                  l
                );
                break t;
              }
            }
            throw e = Q(t) || t, Error(o(306, e, ""));
          }
        }
        return e;
      case 0:
        return bf(
          t,
          e,
          e.type,
          e.pendingProps,
          l
        );
      case 1:
        return n = e.type, a = Pl(
          n,
          e.pendingProps
        ), vd(
          t,
          e,
          n,
          a,
          l
        );
      case 3:
        t: {
          if (Jt(
            e,
            e.stateNode.containerInfo
          ), t === null) throw Error(o(387));
          n = e.pendingProps;
          var u = e.memoizedState;
          a = u.element, Zc(t, e), Ra(e, n, null, l);
          var f = e.memoizedState;
          if (n = f.cache, vl(e, Yt, n), n !== u.cache && Lc(
            e,
            [Yt],
            l,
            !0
          ), Aa(), n = f.element, u.isDehydrated)
            if (u = {
              element: n,
              isDehydrated: !1,
              cache: f.cache
            }, e.updateQueue.baseState = u, e.memoizedState = u, e.flags & 256) {
              e = hd(
                t,
                e,
                n,
                l
              );
              break t;
            } else if (n !== a) {
              a = Ae(
                Error(o(424)),
                e
              ), ga(a), e = hd(
                t,
                e,
                n,
                l
              );
              break t;
            } else {
              switch (t = e.stateNode.containerInfo, t.nodeType) {
                case 9:
                  t = t.body;
                  break;
                default:
                  t = t.nodeName === "HTML" ? t.ownerDocument.body : t;
              }
              for (xt = Me(t.firstChild), kt = e, yt = !0, dl = null, ze = !0, l = sr(
                e,
                null,
                n,
                l
              ), e.child = l; l; )
                l.flags = l.flags & -3 | 4096, l = l.sibling;
            }
          else {
            if (Vl(), n === a) {
              e = $e(
                t,
                e,
                l
              );
              break t;
            }
            $t(t, e, n, l);
          }
          e = e.child;
        }
        return e;
      case 26:
        return ku(t, e), t === null ? (l = xm(
          e.type,
          null,
          e.pendingProps,
          null
        )) ? e.memoizedState = l : yt || (l = e.type, t = e.pendingProps, n = di(
          nt.current
        ).createElement(l), n[Wt] = e, n[ee] = t, It(n, l, t), Vt(n), e.stateNode = n) : e.memoizedState = xm(
          e.type,
          t.memoizedProps,
          e.pendingProps,
          t.memoizedState
        ), null;
      case 27:
        return ql(e), t === null && yt && (n = e.stateNode = _m(
          e.type,
          e.pendingProps,
          nt.current
        ), kt = e, ze = !0, a = xt, _l(e.type) ? (ao = a, xt = Me(n.firstChild)) : xt = a), $t(
          t,
          e,
          e.pendingProps.children,
          l
        ), ku(t, e), t === null && (e.flags |= 4194304), e.child;
      case 5:
        return t === null && yt && ((a = n = xt) && (n = y0(
          n,
          e.type,
          e.pendingProps,
          ze
        ), n !== null ? (e.stateNode = n, kt = e, xt = Me(n.firstChild), ze = !1, a = !0) : a = !1), a || ml(e)), ql(e), a = e.type, u = e.pendingProps, f = t !== null ? t.memoizedProps : null, n = u.children, Pf(a, u) ? n = null : f !== null && Pf(a, f) && (e.flags |= 32), e.memoizedState !== null && (a = $c(
          t,
          e,
          Dy,
          null,
          null,
          l
        ), Za._currentValue = a), ku(t, e), $t(t, e, n, l), e.child;
      case 6:
        return t === null && yt && ((t = l = xt) && (l = g0(
          l,
          e.pendingProps,
          ze
        ), l !== null ? (e.stateNode = l, kt = e, xt = null, t = !0) : t = !1), t || ml(e)), null;
      case 13:
        return yd(t, e, l);
      case 4:
        return Jt(
          e,
          e.stateNode.containerInfo
        ), n = e.pendingProps, t === null ? e.child = $l(
          e,
          null,
          n,
          l
        ) : $t(t, e, n, l), e.child;
      case 11:
        return cd(
          t,
          e,
          e.type,
          e.pendingProps,
          l
        );
      case 7:
        return $t(
          t,
          e,
          e.pendingProps,
          l
        ), e.child;
      case 8:
        return $t(
          t,
          e,
          e.pendingProps.children,
          l
        ), e.child;
      case 12:
        return $t(
          t,
          e,
          e.pendingProps.children,
          l
        ), e.child;
      case 10:
        return n = e.pendingProps, vl(e, e.type, n.value), $t(t, e, n.children, l), e.child;
      case 9:
        return a = e.type._context, n = e.pendingProps.children, Jl(e), a = Ft(a), n = n(a), e.flags |= 1, $t(t, e, n, l), e.child;
      case 14:
        return fd(
          t,
          e,
          e.type,
          e.pendingProps,
          l
        );
      case 15:
        return od(
          t,
          e,
          e.type,
          e.pendingProps,
          l
        );
      case 19:
        return bd(t, e, l);
      case 31:
        return Yy(t, e, l);
      case 22:
        return sd(
          t,
          e,
          l,
          e.pendingProps
        );
      case 24:
        return Jl(e), n = Ft(Yt), t === null ? (a = Xc(), a === null && (a = Mt, u = Yc(), a.pooledCache = u, u.refCount++, u !== null && (a.pooledCacheLanes |= l), a = u), e.memoizedState = { parent: n, cache: a }, Qc(e), vl(e, Yt, a)) : ((t.lanes & l) !== 0 && (Zc(t, e), Ra(e, null, null, l), Aa()), a = t.memoizedState, u = e.memoizedState, a.parent !== n ? (a = { parent: n, cache: n }, e.memoizedState = a, e.lanes === 0 && (e.memoizedState = e.updateQueue.baseState = a), vl(e, Yt, n)) : (n = u.cache, vl(e, Yt, n), n !== a.cache && Lc(
          e,
          [Yt],
          l,
          !0
        ))), $t(
          t,
          e,
          e.pendingProps.children,
          l
        ), e.child;
      case 29:
        throw e.pendingProps;
    }
    throw Error(o(156, e.tag));
  }
  function Ie(t) {
    t.flags |= 4;
  }
  function zf(t, e, l, n, a) {
    if ((e = (t.mode & 32) !== 0) && (e = !1), e) {
      if (t.flags |= 16777216, (a & 335544128) === a)
        if (t.stateNode.complete) t.flags |= 8192;
        else if (Kd()) t.flags |= 8192;
        else
          throw Fl = wu, Gc;
    } else t.flags &= -16777217;
  }
  function pd(t, e) {
    if (e.type !== "stylesheet" || (e.state.loading & 4) !== 0)
      t.flags &= -16777217;
    else if (t.flags |= 16777216, !Hm(e))
      if (Kd()) t.flags |= 8192;
      else
        throw Fl = wu, Gc;
  }
  function $u(t, e) {
    e !== null && (t.flags |= 4), t.flags & 16384 && (e = t.tag !== 22 ? Po() : 536870912, t.lanes |= e, Gn |= e);
  }
  function xa(t, e) {
    if (!yt)
      switch (t.tailMode) {
        case "hidden":
          e = t.tail;
          for (var l = null; e !== null; )
            e.alternate !== null && (l = e), e = e.sibling;
          l === null ? t.tail = null : l.sibling = null;
          break;
        case "collapsed":
          l = t.tail;
          for (var n = null; l !== null; )
            l.alternate !== null && (n = l), l = l.sibling;
          n === null ? e || t.tail === null ? t.tail = null : t.tail.sibling = null : n.sibling = null;
      }
  }
  function Dt(t) {
    var e = t.alternate !== null && t.alternate.child === t.child, l = 0, n = 0;
    if (e)
      for (var a = t.child; a !== null; )
        l |= a.lanes | a.childLanes, n |= a.subtreeFlags & 65011712, n |= a.flags & 65011712, a.return = t, a = a.sibling;
    else
      for (a = t.child; a !== null; )
        l |= a.lanes | a.childLanes, n |= a.subtreeFlags, n |= a.flags, a.return = t, a = a.sibling;
    return t.subtreeFlags |= n, t.childLanes = l, e;
  }
  function Xy(t, e, l) {
    var n = e.pendingProps;
    switch (Uc(e), e.tag) {
      case 16:
      case 15:
      case 0:
      case 11:
      case 7:
      case 8:
      case 12:
      case 9:
      case 14:
        return Dt(e), null;
      case 1:
        return Dt(e), null;
      case 3:
        return l = e.stateNode, n = null, t !== null && (n = t.memoizedState.cache), e.memoizedState.cache !== n && (e.flags |= 2048), We(Yt), Ut(), l.pendingContext && (l.context = l.pendingContext, l.pendingContext = null), (t === null || t.child === null) && (Cn(e) ? Ie(e) : t === null || t.memoizedState.isDehydrated && (e.flags & 256) === 0 || (e.flags |= 1024, Hc())), Dt(e), null;
      case 26:
        var a = e.type, u = e.memoizedState;
        return t === null ? (Ie(e), u !== null ? (Dt(e), pd(e, u)) : (Dt(e), zf(
          e,
          a,
          null,
          n,
          l
        ))) : u ? u !== t.memoizedState ? (Ie(e), Dt(e), pd(e, u)) : (Dt(e), e.flags &= -16777217) : (t = t.memoizedProps, t !== n && Ie(e), Dt(e), zf(
          e,
          a,
          t,
          n,
          l
        )), null;
      case 27:
        if (dn(e), l = nt.current, a = e.type, t !== null && e.stateNode != null)
          t.memoizedProps !== n && Ie(e);
        else {
          if (!n) {
            if (e.stateNode === null)
              throw Error(o(166));
            return Dt(e), null;
          }
          t = K.current, Cn(e) ? Ps(e) : (t = _m(a, n, l), e.stateNode = t, Ie(e));
        }
        return Dt(e), null;
      case 5:
        if (dn(e), a = e.type, t !== null && e.stateNode != null)
          t.memoizedProps !== n && Ie(e);
        else {
          if (!n) {
            if (e.stateNode === null)
              throw Error(o(166));
            return Dt(e), null;
          }
          if (u = K.current, Cn(e))
            Ps(e);
          else {
            var f = di(
              nt.current
            );
            switch (u) {
              case 1:
                u = f.createElementNS(
                  "http://www.w3.org/2000/svg",
                  a
                );
                break;
              case 2:
                u = f.createElementNS(
                  "http://www.w3.org/1998/Math/MathML",
                  a
                );
                break;
              default:
                switch (a) {
                  case "svg":
                    u = f.createElementNS(
                      "http://www.w3.org/2000/svg",
                      a
                    );
                    break;
                  case "math":
                    u = f.createElementNS(
                      "http://www.w3.org/1998/Math/MathML",
                      a
                    );
                    break;
                  case "script":
                    u = f.createElement("div"), u.innerHTML = "<script><\/script>", u = u.removeChild(
                      u.firstChild
                    );
                    break;
                  case "select":
                    u = typeof n.is == "string" ? f.createElement("select", {
                      is: n.is
                    }) : f.createElement("select"), n.multiple ? u.multiple = !0 : n.size && (u.size = n.size);
                    break;
                  default:
                    u = typeof n.is == "string" ? f.createElement(a, { is: n.is }) : f.createElement(a);
                }
            }
            u[Wt] = e, u[ee] = n;
            t: for (f = e.child; f !== null; ) {
              if (f.tag === 5 || f.tag === 6)
                u.appendChild(f.stateNode);
              else if (f.tag !== 4 && f.tag !== 27 && f.child !== null) {
                f.child.return = f, f = f.child;
                continue;
              }
              if (f === e) break t;
              for (; f.sibling === null; ) {
                if (f.return === null || f.return === e)
                  break t;
                f = f.return;
              }
              f.sibling.return = f.return, f = f.sibling;
            }
            e.stateNode = u;
            t: switch (It(u, a, n), a) {
              case "button":
              case "input":
              case "select":
              case "textarea":
                n = !!n.autoFocus;
                break t;
              case "img":
                n = !0;
                break t;
              default:
                n = !1;
            }
            n && Ie(e);
          }
        }
        return Dt(e), zf(
          e,
          e.type,
          t === null ? null : t.memoizedProps,
          e.pendingProps,
          l
        ), null;
      case 6:
        if (t && e.stateNode != null)
          t.memoizedProps !== n && Ie(e);
        else {
          if (typeof n != "string" && e.stateNode === null)
            throw Error(o(166));
          if (t = nt.current, Cn(e)) {
            if (t = e.stateNode, l = e.memoizedProps, n = null, a = kt, a !== null)
              switch (a.tag) {
                case 27:
                case 5:
                  n = a.memoizedProps;
              }
            t[Wt] = e, t = !!(t.nodeValue === l || n !== null && n.suppressHydrationWarning === !0 || ym(t.nodeValue, l)), t || ml(e, !0);
          } else
            t = di(t).createTextNode(
              n
            ), t[Wt] = e, e.stateNode = t;
        }
        return Dt(e), null;
      case 31:
        if (l = e.memoizedState, t === null || t.memoizedState !== null) {
          if (n = Cn(e), l !== null) {
            if (t === null) {
              if (!n) throw Error(o(318));
              if (t = e.memoizedState, t = t !== null ? t.dehydrated : null, !t) throw Error(o(557));
              t[Wt] = e;
            } else
              Vl(), (e.flags & 128) === 0 && (e.memoizedState = null), e.flags |= 4;
            Dt(e), t = !1;
          } else
            l = Hc(), t !== null && t.memoizedState !== null && (t.memoizedState.hydrationErrors = l), t = !0;
          if (!t)
            return e.flags & 256 ? (ve(e), e) : (ve(e), null);
          if ((e.flags & 128) !== 0)
            throw Error(o(558));
        }
        return Dt(e), null;
      case 13:
        if (n = e.memoizedState, t === null || t.memoizedState !== null && t.memoizedState.dehydrated !== null) {
          if (a = Cn(e), n !== null && n.dehydrated !== null) {
            if (t === null) {
              if (!a) throw Error(o(318));
              if (a = e.memoizedState, a = a !== null ? a.dehydrated : null, !a) throw Error(o(317));
              a[Wt] = e;
            } else
              Vl(), (e.flags & 128) === 0 && (e.memoizedState = null), e.flags |= 4;
            Dt(e), a = !1;
          } else
            a = Hc(), t !== null && t.memoizedState !== null && (t.memoizedState.hydrationErrors = a), a = !0;
          if (!a)
            return e.flags & 256 ? (ve(e), e) : (ve(e), null);
        }
        return ve(e), (e.flags & 128) !== 0 ? (e.lanes = l, e) : (l = n !== null, t = t !== null && t.memoizedState !== null, l && (n = e.child, a = null, n.alternate !== null && n.alternate.memoizedState !== null && n.alternate.memoizedState.cachePool !== null && (a = n.alternate.memoizedState.cachePool.pool), u = null, n.memoizedState !== null && n.memoizedState.cachePool !== null && (u = n.memoizedState.cachePool.pool), u !== a && (n.flags |= 2048)), l !== t && l && (e.child.flags |= 8192), $u(e, e.updateQueue), Dt(e), null);
      case 4:
        return Ut(), t === null && Wf(e.stateNode.containerInfo), Dt(e), null;
      case 10:
        return We(e.type), Dt(e), null;
      case 19:
        if (w(Bt), n = e.memoizedState, n === null) return Dt(e), null;
        if (a = (e.flags & 128) !== 0, u = n.rendering, u === null)
          if (a) xa(n, !1);
          else {
            if (Ht !== 0 || t !== null && (t.flags & 128) !== 0)
              for (t = e.child; t !== null; ) {
                if (u = Lu(t), u !== null) {
                  for (e.flags |= 128, xa(n, !1), t = u.updateQueue, e.updateQueue = t, $u(e, t), e.subtreeFlags = 0, t = l, l = e.child; l !== null; )
                    Ws(l, t), l = l.sibling;
                  return j(
                    Bt,
                    Bt.current & 1 | 2
                  ), yt && Ke(e, n.treeForkCount), e.child;
                }
                t = t.sibling;
              }
            n.tail !== null && fe() > li && (e.flags |= 128, a = !0, xa(n, !1), e.lanes = 4194304);
          }
        else {
          if (!a)
            if (t = Lu(u), t !== null) {
              if (e.flags |= 128, a = !0, t = t.updateQueue, e.updateQueue = t, $u(e, t), xa(n, !0), n.tail === null && n.tailMode === "hidden" && !u.alternate && !yt)
                return Dt(e), null;
            } else
              2 * fe() - n.renderingStartTime > li && l !== 536870912 && (e.flags |= 128, a = !0, xa(n, !1), e.lanes = 4194304);
          n.isBackwards ? (u.sibling = e.child, e.child = u) : (t = n.last, t !== null ? t.sibling = u : e.child = u, n.last = u);
        }
        return n.tail !== null ? (t = n.tail, n.rendering = t, n.tail = t.sibling, n.renderingStartTime = fe(), t.sibling = null, l = Bt.current, j(
          Bt,
          a ? l & 1 | 2 : l & 1
        ), yt && Ke(e, n.treeForkCount), t) : (Dt(e), null);
      case 22:
      case 23:
        return ve(e), Wc(), n = e.memoizedState !== null, t !== null ? t.memoizedState !== null !== n && (e.flags |= 8192) : n && (e.flags |= 8192), n ? (l & 536870912) !== 0 && (e.flags & 128) === 0 && (Dt(e), e.subtreeFlags & 6 && (e.flags |= 8192)) : Dt(e), l = e.updateQueue, l !== null && $u(e, l.retryQueue), l = null, t !== null && t.memoizedState !== null && t.memoizedState.cachePool !== null && (l = t.memoizedState.cachePool.pool), n = null, e.memoizedState !== null && e.memoizedState.cachePool !== null && (n = e.memoizedState.cachePool.pool), n !== l && (e.flags |= 2048), t !== null && w(Wl), null;
      case 24:
        return l = null, t !== null && (l = t.memoizedState.cache), e.memoizedState.cache !== l && (e.flags |= 2048), We(Yt), Dt(e), null;
      case 25:
        return null;
      case 30:
        return null;
    }
    throw Error(o(156, e.tag));
  }
  function Gy(t, e) {
    switch (Uc(e), e.tag) {
      case 1:
        return t = e.flags, t & 65536 ? (e.flags = t & -65537 | 128, e) : null;
      case 3:
        return We(Yt), Ut(), t = e.flags, (t & 65536) !== 0 && (t & 128) === 0 ? (e.flags = t & -65537 | 128, e) : null;
      case 26:
      case 27:
      case 5:
        return dn(e), null;
      case 31:
        if (e.memoizedState !== null) {
          if (ve(e), e.alternate === null)
            throw Error(o(340));
          Vl();
        }
        return t = e.flags, t & 65536 ? (e.flags = t & -65537 | 128, e) : null;
      case 13:
        if (ve(e), t = e.memoizedState, t !== null && t.dehydrated !== null) {
          if (e.alternate === null)
            throw Error(o(340));
          Vl();
        }
        return t = e.flags, t & 65536 ? (e.flags = t & -65537 | 128, e) : null;
      case 19:
        return w(Bt), null;
      case 4:
        return Ut(), null;
      case 10:
        return We(e.type), null;
      case 22:
      case 23:
        return ve(e), Wc(), t !== null && w(Wl), t = e.flags, t & 65536 ? (e.flags = t & -65537 | 128, e) : null;
      case 24:
        return We(Yt), null;
      case 25:
        return null;
      default:
        return null;
    }
  }
  function Ed(t, e) {
    switch (Uc(e), e.tag) {
      case 3:
        We(Yt), Ut();
        break;
      case 26:
      case 27:
      case 5:
        dn(e);
        break;
      case 4:
        Ut();
        break;
      case 31:
        e.memoizedState !== null && ve(e);
        break;
      case 13:
        ve(e);
        break;
      case 19:
        w(Bt);
        break;
      case 10:
        We(e.type);
        break;
      case 22:
      case 23:
        ve(e), Wc(), t !== null && w(Wl);
        break;
      case 24:
        We(Yt);
    }
  }
  function Da(t, e) {
    try {
      var l = e.updateQueue, n = l !== null ? l.lastEffect : null;
      if (n !== null) {
        var a = n.next;
        l = a;
        do {
          if ((l.tag & t) === t) {
            n = void 0;
            var u = l.create, f = l.inst;
            n = u(), f.destroy = n;
          }
          l = l.next;
        } while (l !== a);
      }
    } catch (r) {
      Et(e, e.return, r);
    }
  }
  function pl(t, e, l) {
    try {
      var n = e.updateQueue, a = n !== null ? n.lastEffect : null;
      if (a !== null) {
        var u = a.next;
        n = u;
        do {
          if ((n.tag & t) === t) {
            var f = n.inst, r = f.destroy;
            if (r !== void 0) {
              f.destroy = void 0, a = e;
              var y = l, A = r;
              try {
                A();
              } catch (C) {
                Et(
                  a,
                  y,
                  C
                );
              }
            }
          }
          n = n.next;
        } while (n !== u);
      }
    } catch (C) {
      Et(e, e.return, C);
    }
  }
  function Td(t) {
    var e = t.updateQueue;
    if (e !== null) {
      var l = t.stateNode;
      try {
        dr(e, l);
      } catch (n) {
        Et(t, t.return, n);
      }
    }
  }
  function Ad(t, e, l) {
    l.props = Pl(
      t.type,
      t.memoizedProps
    ), l.state = t.memoizedState;
    try {
      l.componentWillUnmount();
    } catch (n) {
      Et(t, e, n);
    }
  }
  function Na(t, e) {
    try {
      var l = t.ref;
      if (l !== null) {
        switch (t.tag) {
          case 26:
          case 27:
          case 5:
            var n = t.stateNode;
            break;
          case 30:
            n = t.stateNode;
            break;
          default:
            n = t.stateNode;
        }
        typeof l == "function" ? t.refCleanup = l(n) : l.current = n;
      }
    } catch (a) {
      Et(t, e, a);
    }
  }
  function He(t, e) {
    var l = t.ref, n = t.refCleanup;
    if (l !== null)
      if (typeof n == "function")
        try {
          n();
        } catch (a) {
          Et(t, e, a);
        } finally {
          t.refCleanup = null, t = t.alternate, t != null && (t.refCleanup = null);
        }
      else if (typeof l == "function")
        try {
          l(null);
        } catch (a) {
          Et(t, e, a);
        }
      else l.current = null;
  }
  function Rd(t) {
    var e = t.type, l = t.memoizedProps, n = t.stateNode;
    try {
      t: switch (e) {
        case "button":
        case "input":
        case "select":
        case "textarea":
          l.autoFocus && n.focus();
          break t;
        case "img":
          l.src ? n.src = l.src : l.srcSet && (n.srcset = l.srcSet);
      }
    } catch (a) {
      Et(t, t.return, a);
    }
  }
  function _f(t, e, l) {
    try {
      var n = t.stateNode;
      s0(n, t.type, l, e), n[ee] = e;
    } catch (a) {
      Et(t, t.return, a);
    }
  }
  function Od(t) {
    return t.tag === 5 || t.tag === 3 || t.tag === 26 || t.tag === 27 && _l(t.type) || t.tag === 4;
  }
  function Mf(t) {
    t: for (; ; ) {
      for (; t.sibling === null; ) {
        if (t.return === null || Od(t.return)) return null;
        t = t.return;
      }
      for (t.sibling.return = t.return, t = t.sibling; t.tag !== 5 && t.tag !== 6 && t.tag !== 18; ) {
        if (t.tag === 27 && _l(t.type) || t.flags & 2 || t.child === null || t.tag === 4) continue t;
        t.child.return = t, t = t.child;
      }
      if (!(t.flags & 2)) return t.stateNode;
    }
  }
  function Cf(t, e, l) {
    var n = t.tag;
    if (n === 5 || n === 6)
      t = t.stateNode, e ? (l.nodeType === 9 ? l.body : l.nodeName === "HTML" ? l.ownerDocument.body : l).insertBefore(t, e) : (e = l.nodeType === 9 ? l.body : l.nodeName === "HTML" ? l.ownerDocument.body : l, e.appendChild(t), l = l._reactRootContainer, l != null || e.onclick !== null || (e.onclick = Qe));
    else if (n !== 4 && (n === 27 && _l(t.type) && (l = t.stateNode, e = null), t = t.child, t !== null))
      for (Cf(t, e, l), t = t.sibling; t !== null; )
        Cf(t, e, l), t = t.sibling;
  }
  function Iu(t, e, l) {
    var n = t.tag;
    if (n === 5 || n === 6)
      t = t.stateNode, e ? l.insertBefore(t, e) : l.appendChild(t);
    else if (n !== 4 && (n === 27 && _l(t.type) && (l = t.stateNode), t = t.child, t !== null))
      for (Iu(t, e, l), t = t.sibling; t !== null; )
        Iu(t, e, l), t = t.sibling;
  }
  function zd(t) {
    var e = t.stateNode, l = t.memoizedProps;
    try {
      for (var n = t.type, a = e.attributes; a.length; )
        e.removeAttributeNode(a[0]);
      It(e, n, l), e[Wt] = t, e[ee] = l;
    } catch (u) {
      Et(t, t.return, u);
    }
  }
  var Pe = !1, Gt = !1, xf = !1, _d = typeof WeakSet == "function" ? WeakSet : Set, Kt = null;
  function Qy(t, e) {
    if (t = t.containerInfo, $f = Si, t = Ys(t), Tc(t)) {
      if ("selectionStart" in t)
        var l = {
          start: t.selectionStart,
          end: t.selectionEnd
        };
      else
        t: {
          l = (l = t.ownerDocument) && l.defaultView || window;
          var n = l.getSelection && l.getSelection();
          if (n && n.rangeCount !== 0) {
            l = n.anchorNode;
            var a = n.anchorOffset, u = n.focusNode;
            n = n.focusOffset;
            try {
              l.nodeType, u.nodeType;
            } catch {
              l = null;
              break t;
            }
            var f = 0, r = -1, y = -1, A = 0, C = 0, U = t, O = null;
            e: for (; ; ) {
              for (var _; U !== l || a !== 0 && U.nodeType !== 3 || (r = f + a), U !== u || n !== 0 && U.nodeType !== 3 || (y = f + n), U.nodeType === 3 && (f += U.nodeValue.length), (_ = U.firstChild) !== null; )
                O = U, U = _;
              for (; ; ) {
                if (U === t) break e;
                if (O === l && ++A === a && (r = f), O === u && ++C === n && (y = f), (_ = U.nextSibling) !== null) break;
                U = O, O = U.parentNode;
              }
              U = _;
            }
            l = r === -1 || y === -1 ? null : { start: r, end: y };
          } else l = null;
        }
      l = l || { start: 0, end: 0 };
    } else l = null;
    for (If = { focusedElem: t, selectionRange: l }, Si = !1, Kt = e; Kt !== null; )
      if (e = Kt, t = e.child, (e.subtreeFlags & 1028) !== 0 && t !== null)
        t.return = e, Kt = t;
      else
        for (; Kt !== null; ) {
          switch (e = Kt, u = e.alternate, t = e.flags, e.tag) {
            case 0:
              if ((t & 4) !== 0 && (t = e.updateQueue, t = t !== null ? t.events : null, t !== null))
                for (l = 0; l < t.length; l++)
                  a = t[l], a.ref.impl = a.nextImpl;
              break;
            case 11:
            case 15:
              break;
            case 1:
              if ((t & 1024) !== 0 && u !== null) {
                t = void 0, l = e, a = u.memoizedProps, u = u.memoizedState, n = l.stateNode;
                try {
                  var J = Pl(
                    l.type,
                    a
                  );
                  t = n.getSnapshotBeforeUpdate(
                    J,
                    u
                  ), n.__reactInternalSnapshotBeforeUpdate = t;
                } catch (tt) {
                  Et(
                    l,
                    l.return,
                    tt
                  );
                }
              }
              break;
            case 3:
              if ((t & 1024) !== 0) {
                if (t = e.stateNode.containerInfo, l = t.nodeType, l === 9)
                  eo(t);
                else if (l === 1)
                  switch (t.nodeName) {
                    case "HEAD":
                    case "HTML":
                    case "BODY":
                      eo(t);
                      break;
                    default:
                      t.textContent = "";
                  }
              }
              break;
            case 5:
            case 26:
            case 27:
            case 6:
            case 4:
            case 17:
              break;
            default:
              if ((t & 1024) !== 0) throw Error(o(163));
          }
          if (t = e.sibling, t !== null) {
            t.return = e.return, Kt = t;
            break;
          }
          Kt = e.return;
        }
  }
  function Md(t, e, l) {
    var n = l.flags;
    switch (l.tag) {
      case 0:
      case 11:
      case 15:
        el(t, l), n & 4 && Da(5, l);
        break;
      case 1:
        if (el(t, l), n & 4)
          if (t = l.stateNode, e === null)
            try {
              t.componentDidMount();
            } catch (f) {
              Et(l, l.return, f);
            }
          else {
            var a = Pl(
              l.type,
              e.memoizedProps
            );
            e = e.memoizedState;
            try {
              t.componentDidUpdate(
                a,
                e,
                t.__reactInternalSnapshotBeforeUpdate
              );
            } catch (f) {
              Et(
                l,
                l.return,
                f
              );
            }
          }
        n & 64 && Td(l), n & 512 && Na(l, l.return);
        break;
      case 3:
        if (el(t, l), n & 64 && (t = l.updateQueue, t !== null)) {
          if (e = null, l.child !== null)
            switch (l.child.tag) {
              case 27:
              case 5:
                e = l.child.stateNode;
                break;
              case 1:
                e = l.child.stateNode;
            }
          try {
            dr(t, e);
          } catch (f) {
            Et(l, l.return, f);
          }
        }
        break;
      case 27:
        e === null && n & 4 && zd(l);
      case 26:
      case 5:
        el(t, l), e === null && n & 4 && Rd(l), n & 512 && Na(l, l.return);
        break;
      case 12:
        el(t, l);
        break;
      case 31:
        el(t, l), n & 4 && Dd(t, l);
        break;
      case 13:
        el(t, l), n & 4 && Nd(t, l), n & 64 && (t = l.memoizedState, t !== null && (t = t.dehydrated, t !== null && (l = Iy.bind(
          null,
          l
        ), b0(t, l))));
        break;
      case 22:
        if (n = l.memoizedState !== null || Pe, !n) {
          e = e !== null && e.memoizedState !== null || Gt, a = Pe;
          var u = Gt;
          Pe = n, (Gt = e) && !u ? ll(
            t,
            l,
            (l.subtreeFlags & 8772) !== 0
          ) : el(t, l), Pe = a, Gt = u;
        }
        break;
      case 30:
        break;
      default:
        el(t, l);
    }
  }
  function Cd(t) {
    var e = t.alternate;
    e !== null && (t.alternate = null, Cd(e)), t.child = null, t.deletions = null, t.sibling = null, t.tag === 5 && (e = t.stateNode, e !== null && uc(e)), t.stateNode = null, t.return = null, t.dependencies = null, t.memoizedProps = null, t.memoizedState = null, t.pendingProps = null, t.stateNode = null, t.updateQueue = null;
  }
  var Nt = null, ne = !1;
  function tl(t, e, l) {
    for (l = l.child; l !== null; )
      xd(t, e, l), l = l.sibling;
  }
  function xd(t, e, l) {
    if (oe && typeof oe.onCommitFiberUnmount == "function")
      try {
        oe.onCommitFiberUnmount(la, l);
      } catch {
      }
    switch (l.tag) {
      case 26:
        Gt || He(l, e), tl(
          t,
          e,
          l
        ), l.memoizedState ? l.memoizedState.count-- : l.stateNode && (l = l.stateNode, l.parentNode.removeChild(l));
        break;
      case 27:
        Gt || He(l, e);
        var n = Nt, a = ne;
        _l(l.type) && (Nt = l.stateNode, ne = !1), tl(
          t,
          e,
          l
        ), Xa(l.stateNode), Nt = n, ne = a;
        break;
      case 5:
        Gt || He(l, e);
      case 6:
        if (n = Nt, a = ne, Nt = null, tl(
          t,
          e,
          l
        ), Nt = n, ne = a, Nt !== null)
          if (ne)
            try {
              (Nt.nodeType === 9 ? Nt.body : Nt.nodeName === "HTML" ? Nt.ownerDocument.body : Nt).removeChild(l.stateNode);
            } catch (u) {
              Et(
                l,
                e,
                u
              );
            }
          else
            try {
              Nt.removeChild(l.stateNode);
            } catch (u) {
              Et(
                l,
                e,
                u
              );
            }
        break;
      case 18:
        Nt !== null && (ne ? (t = Nt, Tm(
          t.nodeType === 9 ? t.body : t.nodeName === "HTML" ? t.ownerDocument.body : t,
          l.stateNode
        ), Fn(t)) : Tm(Nt, l.stateNode));
        break;
      case 4:
        n = Nt, a = ne, Nt = l.stateNode.containerInfo, ne = !0, tl(
          t,
          e,
          l
        ), Nt = n, ne = a;
        break;
      case 0:
      case 11:
      case 14:
      case 15:
        pl(2, l, e), Gt || pl(4, l, e), tl(
          t,
          e,
          l
        );
        break;
      case 1:
        Gt || (He(l, e), n = l.stateNode, typeof n.componentWillUnmount == "function" && Ad(
          l,
          e,
          n
        )), tl(
          t,
          e,
          l
        );
        break;
      case 21:
        tl(
          t,
          e,
          l
        );
        break;
      case 22:
        Gt = (n = Gt) || l.memoizedState !== null, tl(
          t,
          e,
          l
        ), Gt = n;
        break;
      default:
        tl(
          t,
          e,
          l
        );
    }
  }
  function Dd(t, e) {
    if (e.memoizedState === null && (t = e.alternate, t !== null && (t = t.memoizedState, t !== null))) {
      t = t.dehydrated;
      try {
        Fn(t);
      } catch (l) {
        Et(e, e.return, l);
      }
    }
  }
  function Nd(t, e) {
    if (e.memoizedState === null && (t = e.alternate, t !== null && (t = t.memoizedState, t !== null && (t = t.dehydrated, t !== null))))
      try {
        Fn(t);
      } catch (l) {
        Et(e, e.return, l);
      }
  }
  function Zy(t) {
    switch (t.tag) {
      case 31:
      case 13:
      case 19:
        var e = t.stateNode;
        return e === null && (e = t.stateNode = new _d()), e;
      case 22:
        return t = t.stateNode, e = t._retryCache, e === null && (e = t._retryCache = new _d()), e;
      default:
        throw Error(o(435, t.tag));
    }
  }
  function Pu(t, e) {
    var l = Zy(t);
    e.forEach(function(n) {
      if (!l.has(n)) {
        l.add(n);
        var a = Py.bind(null, t, n);
        n.then(a, a);
      }
    });
  }
  function ae(t, e) {
    var l = e.deletions;
    if (l !== null)
      for (var n = 0; n < l.length; n++) {
        var a = l[n], u = t, f = e, r = f;
        t: for (; r !== null; ) {
          switch (r.tag) {
            case 27:
              if (_l(r.type)) {
                Nt = r.stateNode, ne = !1;
                break t;
              }
              break;
            case 5:
              Nt = r.stateNode, ne = !1;
              break t;
            case 3:
            case 4:
              Nt = r.stateNode.containerInfo, ne = !0;
              break t;
          }
          r = r.return;
        }
        if (Nt === null) throw Error(o(160));
        xd(u, f, a), Nt = null, ne = !1, u = a.alternate, u !== null && (u.return = null), a.return = null;
      }
    if (e.subtreeFlags & 13886)
      for (e = e.child; e !== null; )
        Ud(e, t), e = e.sibling;
  }
  var De = null;
  function Ud(t, e) {
    var l = t.alternate, n = t.flags;
    switch (t.tag) {
      case 0:
      case 11:
      case 14:
      case 15:
        ae(e, t), ue(t), n & 4 && (pl(3, t, t.return), Da(3, t), pl(5, t, t.return));
        break;
      case 1:
        ae(e, t), ue(t), n & 512 && (Gt || l === null || He(l, l.return)), n & 64 && Pe && (t = t.updateQueue, t !== null && (n = t.callbacks, n !== null && (l = t.shared.hiddenCallbacks, t.shared.hiddenCallbacks = l === null ? n : l.concat(n))));
        break;
      case 26:
        var a = De;
        if (ae(e, t), ue(t), n & 512 && (Gt || l === null || He(l, l.return)), n & 4) {
          var u = l !== null ? l.memoizedState : null;
          if (n = t.memoizedState, l === null)
            if (n === null)
              if (t.stateNode === null) {
                t: {
                  n = t.type, l = t.memoizedProps, a = a.ownerDocument || a;
                  e: switch (n) {
                    case "title":
                      u = a.getElementsByTagName("title")[0], (!u || u[ua] || u[Wt] || u.namespaceURI === "http://www.w3.org/2000/svg" || u.hasAttribute("itemprop")) && (u = a.createElement(n), a.head.insertBefore(
                        u,
                        a.querySelector("head > title")
                      )), It(u, n, l), u[Wt] = t, Vt(u), n = u;
                      break t;
                    case "link":
                      var f = Um(
                        "link",
                        "href",
                        a
                      ).get(n + (l.href || ""));
                      if (f) {
                        for (var r = 0; r < f.length; r++)
                          if (u = f[r], u.getAttribute("href") === (l.href == null || l.href === "" ? null : l.href) && u.getAttribute("rel") === (l.rel == null ? null : l.rel) && u.getAttribute("title") === (l.title == null ? null : l.title) && u.getAttribute("crossorigin") === (l.crossOrigin == null ? null : l.crossOrigin)) {
                            f.splice(r, 1);
                            break e;
                          }
                      }
                      u = a.createElement(n), It(u, n, l), a.head.appendChild(u);
                      break;
                    case "meta":
                      if (f = Um(
                        "meta",
                        "content",
                        a
                      ).get(n + (l.content || ""))) {
                        for (r = 0; r < f.length; r++)
                          if (u = f[r], u.getAttribute("content") === (l.content == null ? null : "" + l.content) && u.getAttribute("name") === (l.name == null ? null : l.name) && u.getAttribute("property") === (l.property == null ? null : l.property) && u.getAttribute("http-equiv") === (l.httpEquiv == null ? null : l.httpEquiv) && u.getAttribute("charset") === (l.charSet == null ? null : l.charSet)) {
                            f.splice(r, 1);
                            break e;
                          }
                      }
                      u = a.createElement(n), It(u, n, l), a.head.appendChild(u);
                      break;
                    default:
                      throw Error(o(468, n));
                  }
                  u[Wt] = t, Vt(u), n = u;
                }
                t.stateNode = n;
              } else
                wm(
                  a,
                  t.type,
                  t.stateNode
                );
            else
              t.stateNode = Nm(
                a,
                n,
                t.memoizedProps
              );
          else
            u !== n ? (u === null ? l.stateNode !== null && (l = l.stateNode, l.parentNode.removeChild(l)) : u.count--, n === null ? wm(
              a,
              t.type,
              t.stateNode
            ) : Nm(
              a,
              n,
              t.memoizedProps
            )) : n === null && t.stateNode !== null && _f(
              t,
              t.memoizedProps,
              l.memoizedProps
            );
        }
        break;
      case 27:
        ae(e, t), ue(t), n & 512 && (Gt || l === null || He(l, l.return)), l !== null && n & 4 && _f(
          t,
          t.memoizedProps,
          l.memoizedProps
        );
        break;
      case 5:
        if (ae(e, t), ue(t), n & 512 && (Gt || l === null || He(l, l.return)), t.flags & 32) {
          a = t.stateNode;
          try {
            Sn(a, "");
          } catch (J) {
            Et(t, t.return, J);
          }
        }
        n & 4 && t.stateNode != null && (a = t.memoizedProps, _f(
          t,
          a,
          l !== null ? l.memoizedProps : a
        )), n & 1024 && (xf = !0);
        break;
      case 6:
        if (ae(e, t), ue(t), n & 4) {
          if (t.stateNode === null)
            throw Error(o(162));
          n = t.memoizedProps, l = t.stateNode;
          try {
            l.nodeValue = n;
          } catch (J) {
            Et(t, t.return, J);
          }
        }
        break;
      case 3:
        if (hi = null, a = De, De = mi(e.containerInfo), ae(e, t), De = a, ue(t), n & 4 && l !== null && l.memoizedState.isDehydrated)
          try {
            Fn(e.containerInfo);
          } catch (J) {
            Et(t, t.return, J);
          }
        xf && (xf = !1, wd(t));
        break;
      case 4:
        n = De, De = mi(
          t.stateNode.containerInfo
        ), ae(e, t), ue(t), De = n;
        break;
      case 12:
        ae(e, t), ue(t);
        break;
      case 31:
        ae(e, t), ue(t), n & 4 && (n = t.updateQueue, n !== null && (t.updateQueue = null, Pu(t, n)));
        break;
      case 13:
        ae(e, t), ue(t), t.child.flags & 8192 && t.memoizedState !== null != (l !== null && l.memoizedState !== null) && (ei = fe()), n & 4 && (n = t.updateQueue, n !== null && (t.updateQueue = null, Pu(t, n)));
        break;
      case 22:
        a = t.memoizedState !== null;
        var y = l !== null && l.memoizedState !== null, A = Pe, C = Gt;
        if (Pe = A || a, Gt = C || y, ae(e, t), Gt = C, Pe = A, ue(t), n & 8192)
          t: for (e = t.stateNode, e._visibility = a ? e._visibility & -2 : e._visibility | 1, a && (l === null || y || Pe || Gt || tn(t)), l = null, e = t; ; ) {
            if (e.tag === 5 || e.tag === 26) {
              if (l === null) {
                y = l = e;
                try {
                  if (u = y.stateNode, a)
                    f = u.style, typeof f.setProperty == "function" ? f.setProperty("display", "none", "important") : f.display = "none";
                  else {
                    r = y.stateNode;
                    var U = y.memoizedProps.style, O = U != null && U.hasOwnProperty("display") ? U.display : null;
                    r.style.display = O == null || typeof O == "boolean" ? "" : ("" + O).trim();
                  }
                } catch (J) {
                  Et(y, y.return, J);
                }
              }
            } else if (e.tag === 6) {
              if (l === null) {
                y = e;
                try {
                  y.stateNode.nodeValue = a ? "" : y.memoizedProps;
                } catch (J) {
                  Et(y, y.return, J);
                }
              }
            } else if (e.tag === 18) {
              if (l === null) {
                y = e;
                try {
                  var _ = y.stateNode;
                  a ? Am(_, !0) : Am(y.stateNode, !1);
                } catch (J) {
                  Et(y, y.return, J);
                }
              }
            } else if ((e.tag !== 22 && e.tag !== 23 || e.memoizedState === null || e === t) && e.child !== null) {
              e.child.return = e, e = e.child;
              continue;
            }
            if (e === t) break t;
            for (; e.sibling === null; ) {
              if (e.return === null || e.return === t) break t;
              l === e && (l = null), e = e.return;
            }
            l === e && (l = null), e.sibling.return = e.return, e = e.sibling;
          }
        n & 4 && (n = t.updateQueue, n !== null && (l = n.retryQueue, l !== null && (n.retryQueue = null, Pu(t, l))));
        break;
      case 19:
        ae(e, t), ue(t), n & 4 && (n = t.updateQueue, n !== null && (t.updateQueue = null, Pu(t, n)));
        break;
      case 30:
        break;
      case 21:
        break;
      default:
        ae(e, t), ue(t);
    }
  }
  function ue(t) {
    var e = t.flags;
    if (e & 2) {
      try {
        for (var l, n = t.return; n !== null; ) {
          if (Od(n)) {
            l = n;
            break;
          }
          n = n.return;
        }
        if (l == null) throw Error(o(160));
        switch (l.tag) {
          case 27:
            var a = l.stateNode, u = Mf(t);
            Iu(t, u, a);
            break;
          case 5:
            var f = l.stateNode;
            l.flags & 32 && (Sn(f, ""), l.flags &= -33);
            var r = Mf(t);
            Iu(t, r, f);
            break;
          case 3:
          case 4:
            var y = l.stateNode.containerInfo, A = Mf(t);
            Cf(
              t,
              A,
              y
            );
            break;
          default:
            throw Error(o(161));
        }
      } catch (C) {
        Et(t, t.return, C);
      }
      t.flags &= -3;
    }
    e & 4096 && (t.flags &= -4097);
  }
  function wd(t) {
    if (t.subtreeFlags & 1024)
      for (t = t.child; t !== null; ) {
        var e = t;
        wd(e), e.tag === 5 && e.flags & 1024 && e.stateNode.reset(), t = t.sibling;
      }
  }
  function el(t, e) {
    if (e.subtreeFlags & 8772)
      for (e = e.child; e !== null; )
        Md(t, e.alternate, e), e = e.sibling;
  }
  function tn(t) {
    for (t = t.child; t !== null; ) {
      var e = t;
      switch (e.tag) {
        case 0:
        case 11:
        case 14:
        case 15:
          pl(4, e, e.return), tn(e);
          break;
        case 1:
          He(e, e.return);
          var l = e.stateNode;
          typeof l.componentWillUnmount == "function" && Ad(
            e,
            e.return,
            l
          ), tn(e);
          break;
        case 27:
          Xa(e.stateNode);
        case 26:
        case 5:
          He(e, e.return), tn(e);
          break;
        case 22:
          e.memoizedState === null && tn(e);
          break;
        case 30:
          tn(e);
          break;
        default:
          tn(e);
      }
      t = t.sibling;
    }
  }
  function ll(t, e, l) {
    for (l = l && (e.subtreeFlags & 8772) !== 0, e = e.child; e !== null; ) {
      var n = e.alternate, a = t, u = e, f = u.flags;
      switch (u.tag) {
        case 0:
        case 11:
        case 15:
          ll(
            a,
            u,
            l
          ), Da(4, u);
          break;
        case 1:
          if (ll(
            a,
            u,
            l
          ), n = u, a = n.stateNode, typeof a.componentDidMount == "function")
            try {
              a.componentDidMount();
            } catch (A) {
              Et(n, n.return, A);
            }
          if (n = u, a = n.updateQueue, a !== null) {
            var r = n.stateNode;
            try {
              var y = a.shared.hiddenCallbacks;
              if (y !== null)
                for (a.shared.hiddenCallbacks = null, a = 0; a < y.length; a++)
                  rr(y[a], r);
            } catch (A) {
              Et(n, n.return, A);
            }
          }
          l && f & 64 && Td(u), Na(u, u.return);
          break;
        case 27:
          zd(u);
        case 26:
        case 5:
          ll(
            a,
            u,
            l
          ), l && n === null && f & 4 && Rd(u), Na(u, u.return);
          break;
        case 12:
          ll(
            a,
            u,
            l
          );
          break;
        case 31:
          ll(
            a,
            u,
            l
          ), l && f & 4 && Dd(a, u);
          break;
        case 13:
          ll(
            a,
            u,
            l
          ), l && f & 4 && Nd(a, u);
          break;
        case 22:
          u.memoizedState === null && ll(
            a,
            u,
            l
          ), Na(u, u.return);
          break;
        case 30:
          break;
        default:
          ll(
            a,
            u,
            l
          );
      }
      e = e.sibling;
    }
  }
  function Df(t, e) {
    var l = null;
    t !== null && t.memoizedState !== null && t.memoizedState.cachePool !== null && (l = t.memoizedState.cachePool.pool), t = null, e.memoizedState !== null && e.memoizedState.cachePool !== null && (t = e.memoizedState.cachePool.pool), t !== l && (t != null && t.refCount++, l != null && ba(l));
  }
  function Nf(t, e) {
    t = null, e.alternate !== null && (t = e.alternate.memoizedState.cache), e = e.memoizedState.cache, e !== t && (e.refCount++, t != null && ba(t));
  }
  function Ne(t, e, l, n) {
    if (e.subtreeFlags & 10256)
      for (e = e.child; e !== null; )
        Hd(
          t,
          e,
          l,
          n
        ), e = e.sibling;
  }
  function Hd(t, e, l, n) {
    var a = e.flags;
    switch (e.tag) {
      case 0:
      case 11:
      case 15:
        Ne(
          t,
          e,
          l,
          n
        ), a & 2048 && Da(9, e);
        break;
      case 1:
        Ne(
          t,
          e,
          l,
          n
        );
        break;
      case 3:
        Ne(
          t,
          e,
          l,
          n
        ), a & 2048 && (t = null, e.alternate !== null && (t = e.alternate.memoizedState.cache), e = e.memoizedState.cache, e !== t && (e.refCount++, t != null && ba(t)));
        break;
      case 12:
        if (a & 2048) {
          Ne(
            t,
            e,
            l,
            n
          ), t = e.stateNode;
          try {
            var u = e.memoizedProps, f = u.id, r = u.onPostCommit;
            typeof r == "function" && r(
              f,
              e.alternate === null ? "mount" : "update",
              t.passiveEffectDuration,
              -0
            );
          } catch (y) {
            Et(e, e.return, y);
          }
        } else
          Ne(
            t,
            e,
            l,
            n
          );
        break;
      case 31:
        Ne(
          t,
          e,
          l,
          n
        );
        break;
      case 13:
        Ne(
          t,
          e,
          l,
          n
        );
        break;
      case 23:
        break;
      case 22:
        u = e.stateNode, f = e.alternate, e.memoizedState !== null ? u._visibility & 2 ? Ne(
          t,
          e,
          l,
          n
        ) : Ua(t, e) : u._visibility & 2 ? Ne(
          t,
          e,
          l,
          n
        ) : (u._visibility |= 2, Yn(
          t,
          e,
          l,
          n,
          (e.subtreeFlags & 10256) !== 0 || !1
        )), a & 2048 && Df(f, e);
        break;
      case 24:
        Ne(
          t,
          e,
          l,
          n
        ), a & 2048 && Nf(e.alternate, e);
        break;
      default:
        Ne(
          t,
          e,
          l,
          n
        );
    }
  }
  function Yn(t, e, l, n, a) {
    for (a = a && ((e.subtreeFlags & 10256) !== 0 || !1), e = e.child; e !== null; ) {
      var u = t, f = e, r = l, y = n, A = f.flags;
      switch (f.tag) {
        case 0:
        case 11:
        case 15:
          Yn(
            u,
            f,
            r,
            y,
            a
          ), Da(8, f);
          break;
        case 23:
          break;
        case 22:
          var C = f.stateNode;
          f.memoizedState !== null ? C._visibility & 2 ? Yn(
            u,
            f,
            r,
            y,
            a
          ) : Ua(
            u,
            f
          ) : (C._visibility |= 2, Yn(
            u,
            f,
            r,
            y,
            a
          )), a && A & 2048 && Df(
            f.alternate,
            f
          );
          break;
        case 24:
          Yn(
            u,
            f,
            r,
            y,
            a
          ), a && A & 2048 && Nf(f.alternate, f);
          break;
        default:
          Yn(
            u,
            f,
            r,
            y,
            a
          );
      }
      e = e.sibling;
    }
  }
  function Ua(t, e) {
    if (e.subtreeFlags & 10256)
      for (e = e.child; e !== null; ) {
        var l = t, n = e, a = n.flags;
        switch (n.tag) {
          case 22:
            Ua(l, n), a & 2048 && Df(
              n.alternate,
              n
            );
            break;
          case 24:
            Ua(l, n), a & 2048 && Nf(n.alternate, n);
            break;
          default:
            Ua(l, n);
        }
        e = e.sibling;
      }
  }
  var wa = 8192;
  function jn(t, e, l) {
    if (t.subtreeFlags & wa)
      for (t = t.child; t !== null; )
        Bd(
          t,
          e,
          l
        ), t = t.sibling;
  }
  function Bd(t, e, l) {
    switch (t.tag) {
      case 26:
        jn(
          t,
          e,
          l
        ), t.flags & wa && t.memoizedState !== null && x0(
          l,
          De,
          t.memoizedState,
          t.memoizedProps
        );
        break;
      case 5:
        jn(
          t,
          e,
          l
        );
        break;
      case 3:
      case 4:
        var n = De;
        De = mi(t.stateNode.containerInfo), jn(
          t,
          e,
          l
        ), De = n;
        break;
      case 22:
        t.memoizedState === null && (n = t.alternate, n !== null && n.memoizedState !== null ? (n = wa, wa = 16777216, jn(
          t,
          e,
          l
        ), wa = n) : jn(
          t,
          e,
          l
        ));
        break;
      default:
        jn(
          t,
          e,
          l
        );
    }
  }
  function qd(t) {
    var e = t.alternate;
    if (e !== null && (t = e.child, t !== null)) {
      e.child = null;
      do
        e = t.sibling, t.sibling = null, t = e;
      while (t !== null);
    }
  }
  function Ha(t) {
    var e = t.deletions;
    if ((t.flags & 16) !== 0) {
      if (e !== null)
        for (var l = 0; l < e.length; l++) {
          var n = e[l];
          Kt = n, Yd(
            n,
            t
          );
        }
      qd(t);
    }
    if (t.subtreeFlags & 10256)
      for (t = t.child; t !== null; )
        Ld(t), t = t.sibling;
  }
  function Ld(t) {
    switch (t.tag) {
      case 0:
      case 11:
      case 15:
        Ha(t), t.flags & 2048 && pl(9, t, t.return);
        break;
      case 3:
        Ha(t);
        break;
      case 12:
        Ha(t);
        break;
      case 22:
        var e = t.stateNode;
        t.memoizedState !== null && e._visibility & 2 && (t.return === null || t.return.tag !== 13) ? (e._visibility &= -3, ti(t)) : Ha(t);
        break;
      default:
        Ha(t);
    }
  }
  function ti(t) {
    var e = t.deletions;
    if ((t.flags & 16) !== 0) {
      if (e !== null)
        for (var l = 0; l < e.length; l++) {
          var n = e[l];
          Kt = n, Yd(
            n,
            t
          );
        }
      qd(t);
    }
    for (t = t.child; t !== null; ) {
      switch (e = t, e.tag) {
        case 0:
        case 11:
        case 15:
          pl(8, e, e.return), ti(e);
          break;
        case 22:
          l = e.stateNode, l._visibility & 2 && (l._visibility &= -3, ti(e));
          break;
        default:
          ti(e);
      }
      t = t.sibling;
    }
  }
  function Yd(t, e) {
    for (; Kt !== null; ) {
      var l = Kt;
      switch (l.tag) {
        case 0:
        case 11:
        case 15:
          pl(8, l, e);
          break;
        case 23:
        case 22:
          if (l.memoizedState !== null && l.memoizedState.cachePool !== null) {
            var n = l.memoizedState.cachePool.pool;
            n != null && n.refCount++;
          }
          break;
        case 24:
          ba(l.memoizedState.cache);
      }
      if (n = l.child, n !== null) n.return = l, Kt = n;
      else
        t: for (l = t; Kt !== null; ) {
          n = Kt;
          var a = n.sibling, u = n.return;
          if (Cd(n), n === l) {
            Kt = null;
            break t;
          }
          if (a !== null) {
            a.return = u, Kt = a;
            break t;
          }
          Kt = u;
        }
    }
  }
  var Vy = {
    getCacheForType: function(t) {
      var e = Ft(Yt), l = e.data.get(t);
      return l === void 0 && (l = t(), e.data.set(t, l)), l;
    },
    cacheSignal: function() {
      return Ft(Yt).controller.signal;
    }
  }, Ky = typeof WeakMap == "function" ? WeakMap : Map, St = 0, Mt = null, st = null, vt = 0, pt = 0, he = null, El = !1, Xn = !1, Uf = !1, nl = 0, Ht = 0, Tl = 0, en = 0, wf = 0, ye = 0, Gn = 0, Ba = null, ie = null, Hf = !1, ei = 0, jd = 0, li = 1 / 0, ni = null, Al = null, Qt = 0, Rl = null, Qn = null, al = 0, Bf = 0, qf = null, Xd = null, qa = 0, Lf = null;
  function ge() {
    return (St & 2) !== 0 && vt !== 0 ? vt & -vt : R.T !== null ? Zf() : ns();
  }
  function Gd() {
    if (ye === 0)
      if ((vt & 536870912) === 0 || yt) {
        var t = ru;
        ru <<= 1, (ru & 3932160) === 0 && (ru = 262144), ye = t;
      } else ye = 536870912;
    return t = me.current, t !== null && (t.flags |= 32), ye;
  }
  function ce(t, e, l) {
    (t === Mt && (pt === 2 || pt === 9) || t.cancelPendingCommit !== null) && (Zn(t, 0), Ol(
      t,
      vt,
      ye,
      !1
    )), aa(t, l), ((St & 2) === 0 || t !== Mt) && (t === Mt && ((St & 2) === 0 && (en |= l), Ht === 4 && Ol(
      t,
      vt,
      ye,
      !1
    )), Be(t));
  }
  function Qd(t, e, l) {
    if ((St & 6) !== 0) throw Error(o(327));
    var n = !l && (e & 127) === 0 && (e & t.expiredLanes) === 0 || na(t, e), a = n ? ky(t, e) : jf(t, e, !0), u = n;
    do {
      if (a === 0) {
        Xn && !n && Ol(t, e, 0, !1);
        break;
      } else {
        if (l = t.current.alternate, u && !Jy(l)) {
          a = jf(t, e, !1), u = !1;
          continue;
        }
        if (a === 2) {
          if (u = e, t.errorRecoveryDisabledLanes & u)
            var f = 0;
          else
            f = t.pendingLanes & -536870913, f = f !== 0 ? f : f & 536870912 ? 536870912 : 0;
          if (f !== 0) {
            e = f;
            t: {
              var r = t;
              a = Ba;
              var y = r.current.memoizedState.isDehydrated;
              if (y && (Zn(r, f).flags |= 256), f = jf(
                r,
                f,
                !1
              ), f !== 2) {
                if (Uf && !y) {
                  r.errorRecoveryDisabledLanes |= u, en |= u, a = 4;
                  break t;
                }
                u = ie, ie = a, u !== null && (ie === null ? ie = u : ie.push.apply(
                  ie,
                  u
                ));
              }
              a = f;
            }
            if (u = !1, a !== 2) continue;
          }
        }
        if (a === 1) {
          Zn(t, 0), Ol(t, e, 0, !0);
          break;
        }
        t: {
          switch (n = t, u = a, u) {
            case 0:
            case 1:
              throw Error(o(345));
            case 4:
              if ((e & 4194048) !== e) break;
            case 6:
              Ol(
                n,
                e,
                ye,
                !El
              );
              break t;
            case 2:
              ie = null;
              break;
            case 3:
            case 5:
              break;
            default:
              throw Error(o(329));
          }
          if ((e & 62914560) === e && (a = ei + 300 - fe(), 10 < a)) {
            if (Ol(
              n,
              e,
              ye,
              !El
            ), mu(n, 0, !0) !== 0) break t;
            al = e, n.timeoutHandle = pm(
              Zd.bind(
                null,
                n,
                l,
                ie,
                ni,
                Hf,
                e,
                ye,
                en,
                Gn,
                El,
                u,
                "Throttled",
                -0,
                0
              ),
              a
            );
            break t;
          }
          Zd(
            n,
            l,
            ie,
            ni,
            Hf,
            e,
            ye,
            en,
            Gn,
            El,
            u,
            null,
            -0,
            0
          );
        }
      }
      break;
    } while (!0);
    Be(t);
  }
  function Zd(t, e, l, n, a, u, f, r, y, A, C, U, O, _) {
    if (t.timeoutHandle = -1, U = e.subtreeFlags, U & 8192 || (U & 16785408) === 16785408) {
      U = {
        stylesheets: null,
        count: 0,
        imgCount: 0,
        imgBytes: 0,
        suspenseyImages: [],
        waitingForImages: !0,
        waitingForViewTransition: !1,
        unsuspend: Qe
      }, Bd(
        e,
        u,
        U
      );
      var J = (u & 62914560) === u ? ei - fe() : (u & 4194048) === u ? jd - fe() : 0;
      if (J = D0(
        U,
        J
      ), J !== null) {
        al = u, t.cancelPendingCommit = J(
          Id.bind(
            null,
            t,
            e,
            u,
            l,
            n,
            a,
            f,
            r,
            y,
            C,
            U,
            null,
            O,
            _
          )
        ), Ol(t, u, f, !A);
        return;
      }
    }
    Id(
      t,
      e,
      u,
      l,
      n,
      a,
      f,
      r,
      y
    );
  }
  function Jy(t) {
    for (var e = t; ; ) {
      var l = e.tag;
      if ((l === 0 || l === 11 || l === 15) && e.flags & 16384 && (l = e.updateQueue, l !== null && (l = l.stores, l !== null)))
        for (var n = 0; n < l.length; n++) {
          var a = l[n], u = a.getSnapshot;
          a = a.value;
          try {
            if (!re(u(), a)) return !1;
          } catch {
            return !1;
          }
        }
      if (l = e.child, e.subtreeFlags & 16384 && l !== null)
        l.return = e, e = l;
      else {
        if (e === t) break;
        for (; e.sibling === null; ) {
          if (e.return === null || e.return === t) return !0;
          e = e.return;
        }
        e.sibling.return = e.return, e = e.sibling;
      }
    }
    return !0;
  }
  function Ol(t, e, l, n) {
    e &= ~wf, e &= ~en, t.suspendedLanes |= e, t.pingedLanes &= ~e, n && (t.warmLanes |= e), n = t.expirationTimes;
    for (var a = e; 0 < a; ) {
      var u = 31 - se(a), f = 1 << u;
      n[u] = -1, a &= ~f;
    }
    l !== 0 && ts(t, l, e);
  }
  function ai() {
    return (St & 6) === 0 ? (La(0), !1) : !0;
  }
  function Yf() {
    if (st !== null) {
      if (pt === 0)
        var t = st.return;
      else
        t = st, Je = Kl = null, tf(t), wn = null, pa = 0, t = st;
      for (; t !== null; )
        Ed(t.alternate, t), t = t.return;
      st = null;
    }
  }
  function Zn(t, e) {
    var l = t.timeoutHandle;
    l !== -1 && (t.timeoutHandle = -1, m0(l)), l = t.cancelPendingCommit, l !== null && (t.cancelPendingCommit = null, l()), al = 0, Yf(), Mt = t, st = l = Ve(t.current, null), vt = e, pt = 0, he = null, El = !1, Xn = na(t, e), Uf = !1, Gn = ye = wf = en = Tl = Ht = 0, ie = Ba = null, Hf = !1, (e & 8) !== 0 && (e |= e & 32);
    var n = t.entangledLanes;
    if (n !== 0)
      for (t = t.entanglements, n &= e; 0 < n; ) {
        var a = 31 - se(n), u = 1 << a;
        e |= t[a], n &= ~u;
      }
    return nl = e, Ou(), l;
  }
  function Vd(t, e) {
    ct = null, R.H = Ma, e === Un || e === Uu ? (e = cr(), pt = 3) : e === Gc ? (e = cr(), pt = 4) : pt = e === gf ? 8 : e !== null && typeof e == "object" && typeof e.then == "function" ? 6 : 1, he = e, st === null && (Ht = 1, Ju(
      t,
      Ae(e, t.current)
    ));
  }
  function Kd() {
    var t = me.current;
    return t === null ? !0 : (vt & 4194048) === vt ? _e === null : (vt & 62914560) === vt || (vt & 536870912) !== 0 ? t === _e : !1;
  }
  function Jd() {
    var t = R.H;
    return R.H = Ma, t === null ? Ma : t;
  }
  function Wd() {
    var t = R.A;
    return R.A = Vy, t;
  }
  function ui() {
    Ht = 4, El || (vt & 4194048) !== vt && me.current !== null || (Xn = !0), (Tl & 134217727) === 0 && (en & 134217727) === 0 || Mt === null || Ol(
      Mt,
      vt,
      ye,
      !1
    );
  }
  function jf(t, e, l) {
    var n = St;
    St |= 2;
    var a = Jd(), u = Wd();
    (Mt !== t || vt !== e) && (ni = null, Zn(t, e)), e = !1;
    var f = Ht;
    t: do
      try {
        if (pt !== 0 && st !== null) {
          var r = st, y = he;
          switch (pt) {
            case 8:
              Yf(), f = 6;
              break t;
            case 3:
            case 2:
            case 9:
            case 6:
              me.current === null && (e = !0);
              var A = pt;
              if (pt = 0, he = null, Vn(t, r, y, A), l && Xn) {
                f = 0;
                break t;
              }
              break;
            default:
              A = pt, pt = 0, he = null, Vn(t, r, y, A);
          }
        }
        Wy(), f = Ht;
        break;
      } catch (C) {
        Vd(t, C);
      }
    while (!0);
    return e && t.shellSuspendCounter++, Je = Kl = null, St = n, R.H = a, R.A = u, st === null && (Mt = null, vt = 0, Ou()), f;
  }
  function Wy() {
    for (; st !== null; ) kd(st);
  }
  function ky(t, e) {
    var l = St;
    St |= 2;
    var n = Jd(), a = Wd();
    Mt !== t || vt !== e ? (ni = null, li = fe() + 500, Zn(t, e)) : Xn = na(
      t,
      e
    );
    t: do
      try {
        if (pt !== 0 && st !== null) {
          e = st;
          var u = he;
          e: switch (pt) {
            case 1:
              pt = 0, he = null, Vn(t, e, u, 1);
              break;
            case 2:
            case 9:
              if (ur(u)) {
                pt = 0, he = null, Fd(e);
                break;
              }
              e = function() {
                pt !== 2 && pt !== 9 || Mt !== t || (pt = 7), Be(t);
              }, u.then(e, e);
              break t;
            case 3:
              pt = 7;
              break t;
            case 4:
              pt = 5;
              break t;
            case 7:
              ur(u) ? (pt = 0, he = null, Fd(e)) : (pt = 0, he = null, Vn(t, e, u, 7));
              break;
            case 5:
              var f = null;
              switch (st.tag) {
                case 26:
                  f = st.memoizedState;
                case 5:
                case 27:
                  var r = st;
                  if (f ? Hm(f) : r.stateNode.complete) {
                    pt = 0, he = null;
                    var y = r.sibling;
                    if (y !== null) st = y;
                    else {
                      var A = r.return;
                      A !== null ? (st = A, ii(A)) : st = null;
                    }
                    break e;
                  }
              }
              pt = 0, he = null, Vn(t, e, u, 5);
              break;
            case 6:
              pt = 0, he = null, Vn(t, e, u, 6);
              break;
            case 8:
              Yf(), Ht = 6;
              break t;
            default:
              throw Error(o(462));
          }
        }
        Fy();
        break;
      } catch (C) {
        Vd(t, C);
      }
    while (!0);
    return Je = Kl = null, R.H = n, R.A = a, St = l, st !== null ? 0 : (Mt = null, vt = 0, Ou(), Ht);
  }
  function Fy() {
    for (; st !== null && !Sh(); )
      kd(st);
  }
  function kd(t) {
    var e = Sd(t.alternate, t, nl);
    t.memoizedProps = t.pendingProps, e === null ? ii(t) : st = e;
  }
  function Fd(t) {
    var e = t, l = e.alternate;
    switch (e.tag) {
      case 15:
      case 0:
        e = md(
          l,
          e,
          e.pendingProps,
          e.type,
          void 0,
          vt
        );
        break;
      case 11:
        e = md(
          l,
          e,
          e.pendingProps,
          e.type.render,
          e.ref,
          vt
        );
        break;
      case 5:
        tf(e);
      default:
        Ed(l, e), e = st = Ws(e, nl), e = Sd(l, e, nl);
    }
    t.memoizedProps = t.pendingProps, e === null ? ii(t) : st = e;
  }
  function Vn(t, e, l, n) {
    Je = Kl = null, tf(e), wn = null, pa = 0;
    var a = e.return;
    try {
      if (Ly(
        t,
        a,
        e,
        l,
        vt
      )) {
        Ht = 1, Ju(
          t,
          Ae(l, t.current)
        ), st = null;
        return;
      }
    } catch (u) {
      if (a !== null) throw st = a, u;
      Ht = 1, Ju(
        t,
        Ae(l, t.current)
      ), st = null;
      return;
    }
    e.flags & 32768 ? (yt || n === 1 ? t = !0 : Xn || (vt & 536870912) !== 0 ? t = !1 : (El = t = !0, (n === 2 || n === 9 || n === 3 || n === 6) && (n = me.current, n !== null && n.tag === 13 && (n.flags |= 16384))), $d(e, t)) : ii(e);
  }
  function ii(t) {
    var e = t;
    do {
      if ((e.flags & 32768) !== 0) {
        $d(
          e,
          El
        );
        return;
      }
      t = e.return;
      var l = Xy(
        e.alternate,
        e,
        nl
      );
      if (l !== null) {
        st = l;
        return;
      }
      if (e = e.sibling, e !== null) {
        st = e;
        return;
      }
      st = e = t;
    } while (e !== null);
    Ht === 0 && (Ht = 5);
  }
  function $d(t, e) {
    do {
      var l = Gy(t.alternate, t);
      if (l !== null) {
        l.flags &= 32767, st = l;
        return;
      }
      if (l = t.return, l !== null && (l.flags |= 32768, l.subtreeFlags = 0, l.deletions = null), !e && (t = t.sibling, t !== null)) {
        st = t;
        return;
      }
      st = t = l;
    } while (t !== null);
    Ht = 6, st = null;
  }
  function Id(t, e, l, n, a, u, f, r, y) {
    t.cancelPendingCommit = null;
    do
      ci();
    while (Qt !== 0);
    if ((St & 6) !== 0) throw Error(o(327));
    if (e !== null) {
      if (e === t.current) throw Error(o(177));
      if (u = e.lanes | e.childLanes, u |= _c, Ch(
        t,
        l,
        u,
        f,
        r,
        y
      ), t === Mt && (st = Mt = null, vt = 0), Qn = e, Rl = t, al = l, Bf = u, qf = a, Xd = n, (e.subtreeFlags & 10256) !== 0 || (e.flags & 10256) !== 0 ? (t.callbackNode = null, t.callbackPriority = 0, t0(ou, function() {
        return nm(), null;
      })) : (t.callbackNode = null, t.callbackPriority = 0), n = (e.flags & 13878) !== 0, (e.subtreeFlags & 13878) !== 0 || n) {
        n = R.T, R.T = null, a = B.p, B.p = 2, f = St, St |= 4;
        try {
          Qy(t, e, l);
        } finally {
          St = f, B.p = a, R.T = n;
        }
      }
      Qt = 1, Pd(), tm(), em();
    }
  }
  function Pd() {
    if (Qt === 1) {
      Qt = 0;
      var t = Rl, e = Qn, l = (e.flags & 13878) !== 0;
      if ((e.subtreeFlags & 13878) !== 0 || l) {
        l = R.T, R.T = null;
        var n = B.p;
        B.p = 2;
        var a = St;
        St |= 4;
        try {
          Ud(e, t);
          var u = If, f = Ys(t.containerInfo), r = u.focusedElem, y = u.selectionRange;
          if (f !== r && r && r.ownerDocument && Ls(
            r.ownerDocument.documentElement,
            r
          )) {
            if (y !== null && Tc(r)) {
              var A = y.start, C = y.end;
              if (C === void 0 && (C = A), "selectionStart" in r)
                r.selectionStart = A, r.selectionEnd = Math.min(
                  C,
                  r.value.length
                );
              else {
                var U = r.ownerDocument || document, O = U && U.defaultView || window;
                if (O.getSelection) {
                  var _ = O.getSelection(), J = r.textContent.length, tt = Math.min(y.start, J), Ot = y.end === void 0 ? tt : Math.min(y.end, J);
                  !_.extend && tt > Ot && (f = Ot, Ot = tt, tt = f);
                  var E = qs(
                    r,
                    tt
                  ), b = qs(
                    r,
                    Ot
                  );
                  if (E && b && (_.rangeCount !== 1 || _.anchorNode !== E.node || _.anchorOffset !== E.offset || _.focusNode !== b.node || _.focusOffset !== b.offset)) {
                    var T = U.createRange();
                    T.setStart(E.node, E.offset), _.removeAllRanges(), tt > Ot ? (_.addRange(T), _.extend(b.node, b.offset)) : (T.setEnd(b.node, b.offset), _.addRange(T));
                  }
                }
              }
            }
            for (U = [], _ = r; _ = _.parentNode; )
              _.nodeType === 1 && U.push({
                element: _,
                left: _.scrollLeft,
                top: _.scrollTop
              });
            for (typeof r.focus == "function" && r.focus(), r = 0; r < U.length; r++) {
              var x = U[r];
              x.element.scrollLeft = x.left, x.element.scrollTop = x.top;
            }
          }
          Si = !!$f, If = $f = null;
        } finally {
          St = a, B.p = n, R.T = l;
        }
      }
      t.current = e, Qt = 2;
    }
  }
  function tm() {
    if (Qt === 2) {
      Qt = 0;
      var t = Rl, e = Qn, l = (e.flags & 8772) !== 0;
      if ((e.subtreeFlags & 8772) !== 0 || l) {
        l = R.T, R.T = null;
        var n = B.p;
        B.p = 2;
        var a = St;
        St |= 4;
        try {
          Md(t, e.alternate, e);
        } finally {
          St = a, B.p = n, R.T = l;
        }
      }
      Qt = 3;
    }
  }
  function em() {
    if (Qt === 4 || Qt === 3) {
      Qt = 0, ph();
      var t = Rl, e = Qn, l = al, n = Xd;
      (e.subtreeFlags & 10256) !== 0 || (e.flags & 10256) !== 0 ? Qt = 5 : (Qt = 0, Qn = Rl = null, lm(t, t.pendingLanes));
      var a = t.pendingLanes;
      if (a === 0 && (Al = null), nc(l), e = e.stateNode, oe && typeof oe.onCommitFiberRoot == "function")
        try {
          oe.onCommitFiberRoot(
            la,
            e,
            void 0,
            (e.current.flags & 128) === 128
          );
        } catch {
        }
      if (n !== null) {
        e = R.T, a = B.p, B.p = 2, R.T = null;
        try {
          for (var u = t.onRecoverableError, f = 0; f < n.length; f++) {
            var r = n[f];
            u(r.value, {
              componentStack: r.stack
            });
          }
        } finally {
          R.T = e, B.p = a;
        }
      }
      (al & 3) !== 0 && ci(), Be(t), a = t.pendingLanes, (l & 261930) !== 0 && (a & 42) !== 0 ? t === Lf ? qa++ : (qa = 0, Lf = t) : qa = 0, La(0);
    }
  }
  function lm(t, e) {
    (t.pooledCacheLanes &= e) === 0 && (e = t.pooledCache, e != null && (t.pooledCache = null, ba(e)));
  }
  function ci() {
    return Pd(), tm(), em(), nm();
  }
  function nm() {
    if (Qt !== 5) return !1;
    var t = Rl, e = Bf;
    Bf = 0;
    var l = nc(al), n = R.T, a = B.p;
    try {
      B.p = 32 > l ? 32 : l, R.T = null, l = qf, qf = null;
      var u = Rl, f = al;
      if (Qt = 0, Qn = Rl = null, al = 0, (St & 6) !== 0) throw Error(o(331));
      var r = St;
      if (St |= 4, Ld(u.current), Hd(
        u,
        u.current,
        f,
        l
      ), St = r, La(0, !1), oe && typeof oe.onPostCommitFiberRoot == "function")
        try {
          oe.onPostCommitFiberRoot(la, u);
        } catch {
        }
      return !0;
    } finally {
      B.p = a, R.T = n, lm(t, e);
    }
  }
  function am(t, e, l) {
    e = Ae(l, e), e = yf(t.stateNode, e, 2), t = gl(t, e, 2), t !== null && (aa(t, 2), Be(t));
  }
  function Et(t, e, l) {
    if (t.tag === 3)
      am(t, t, l);
    else
      for (; e !== null; ) {
        if (e.tag === 3) {
          am(
            e,
            t,
            l
          );
          break;
        } else if (e.tag === 1) {
          var n = e.stateNode;
          if (typeof e.type.getDerivedStateFromError == "function" || typeof n.componentDidCatch == "function" && (Al === null || !Al.has(n))) {
            t = Ae(l, t), l = ud(2), n = gl(e, l, 2), n !== null && (id(
              l,
              n,
              e,
              t
            ), aa(n, 2), Be(n));
            break;
          }
        }
        e = e.return;
      }
  }
  function Xf(t, e, l) {
    var n = t.pingCache;
    if (n === null) {
      n = t.pingCache = new Ky();
      var a = /* @__PURE__ */ new Set();
      n.set(e, a);
    } else
      a = n.get(e), a === void 0 && (a = /* @__PURE__ */ new Set(), n.set(e, a));
    a.has(l) || (Uf = !0, a.add(l), t = $y.bind(null, t, e, l), e.then(t, t));
  }
  function $y(t, e, l) {
    var n = t.pingCache;
    n !== null && n.delete(e), t.pingedLanes |= t.suspendedLanes & l, t.warmLanes &= ~l, Mt === t && (vt & l) === l && (Ht === 4 || Ht === 3 && (vt & 62914560) === vt && 300 > fe() - ei ? (St & 2) === 0 && Zn(t, 0) : wf |= l, Gn === vt && (Gn = 0)), Be(t);
  }
  function um(t, e) {
    e === 0 && (e = Po()), t = Ql(t, e), t !== null && (aa(t, e), Be(t));
  }
  function Iy(t) {
    var e = t.memoizedState, l = 0;
    e !== null && (l = e.retryLane), um(t, l);
  }
  function Py(t, e) {
    var l = 0;
    switch (t.tag) {
      case 31:
      case 13:
        var n = t.stateNode, a = t.memoizedState;
        a !== null && (l = a.retryLane);
        break;
      case 19:
        n = t.stateNode;
        break;
      case 22:
        n = t.stateNode._retryCache;
        break;
      default:
        throw Error(o(314));
    }
    n !== null && n.delete(e), um(t, l);
  }
  function t0(t, e) {
    return Pi(t, e);
  }
  var fi = null, Kn = null, Gf = !1, oi = !1, Qf = !1, zl = 0;
  function Be(t) {
    t !== Kn && t.next === null && (Kn === null ? fi = Kn = t : Kn = Kn.next = t), oi = !0, Gf || (Gf = !0, l0());
  }
  function La(t, e) {
    if (!Qf && oi) {
      Qf = !0;
      do
        for (var l = !1, n = fi; n !== null; ) {
          if (t !== 0) {
            var a = n.pendingLanes;
            if (a === 0) var u = 0;
            else {
              var f = n.suspendedLanes, r = n.pingedLanes;
              u = (1 << 31 - se(42 | t) + 1) - 1, u &= a & ~(f & ~r), u = u & 201326741 ? u & 201326741 | 1 : u ? u | 2 : 0;
            }
            u !== 0 && (l = !0, om(n, u));
          } else
            u = vt, u = mu(
              n,
              n === Mt ? u : 0,
              n.cancelPendingCommit !== null || n.timeoutHandle !== -1
            ), (u & 3) === 0 || na(n, u) || (l = !0, om(n, u));
          n = n.next;
        }
      while (l);
      Qf = !1;
    }
  }
  function e0() {
    im();
  }
  function im() {
    oi = Gf = !1;
    var t = 0;
    zl !== 0 && d0() && (t = zl);
    for (var e = fe(), l = null, n = fi; n !== null; ) {
      var a = n.next, u = cm(n, e);
      u === 0 ? (n.next = null, l === null ? fi = a : l.next = a, a === null && (Kn = l)) : (l = n, (t !== 0 || (u & 3) !== 0) && (oi = !0)), n = a;
    }
    Qt !== 0 && Qt !== 5 || La(t), zl !== 0 && (zl = 0);
  }
  function cm(t, e) {
    for (var l = t.suspendedLanes, n = t.pingedLanes, a = t.expirationTimes, u = t.pendingLanes & -62914561; 0 < u; ) {
      var f = 31 - se(u), r = 1 << f, y = a[f];
      y === -1 ? ((r & l) === 0 || (r & n) !== 0) && (a[f] = Mh(r, e)) : y <= e && (t.expiredLanes |= r), u &= ~r;
    }
    if (e = Mt, l = vt, l = mu(
      t,
      t === e ? l : 0,
      t.cancelPendingCommit !== null || t.timeoutHandle !== -1
    ), n = t.callbackNode, l === 0 || t === e && (pt === 2 || pt === 9) || t.cancelPendingCommit !== null)
      return n !== null && n !== null && tc(n), t.callbackNode = null, t.callbackPriority = 0;
    if ((l & 3) === 0 || na(t, l)) {
      if (e = l & -l, e === t.callbackPriority) return e;
      switch (n !== null && tc(n), nc(l)) {
        case 2:
        case 8:
          l = $o;
          break;
        case 32:
          l = ou;
          break;
        case 268435456:
          l = Io;
          break;
        default:
          l = ou;
      }
      return n = fm.bind(null, t), l = Pi(l, n), t.callbackPriority = e, t.callbackNode = l, e;
    }
    return n !== null && n !== null && tc(n), t.callbackPriority = 2, t.callbackNode = null, 2;
  }
  function fm(t, e) {
    if (Qt !== 0 && Qt !== 5)
      return t.callbackNode = null, t.callbackPriority = 0, null;
    var l = t.callbackNode;
    if (ci() && t.callbackNode !== l)
      return null;
    var n = vt;
    return n = mu(
      t,
      t === Mt ? n : 0,
      t.cancelPendingCommit !== null || t.timeoutHandle !== -1
    ), n === 0 ? null : (Qd(t, n, e), cm(t, fe()), t.callbackNode != null && t.callbackNode === l ? fm.bind(null, t) : null);
  }
  function om(t, e) {
    if (ci()) return null;
    Qd(t, e, !0);
  }
  function l0() {
    v0(function() {
      (St & 6) !== 0 ? Pi(
        Fo,
        e0
      ) : im();
    });
  }
  function Zf() {
    if (zl === 0) {
      var t = Dn;
      t === 0 && (t = su, su <<= 1, (su & 261888) === 0 && (su = 256)), zl = t;
    }
    return zl;
  }
  function sm(t) {
    return t == null || typeof t == "symbol" || typeof t == "boolean" ? null : typeof t == "function" ? t : gu("" + t);
  }
  function rm(t, e) {
    var l = e.ownerDocument.createElement("input");
    return l.name = e.name, l.value = e.value, t.id && l.setAttribute("form", t.id), e.parentNode.insertBefore(l, e), t = new FormData(t), l.parentNode.removeChild(l), t;
  }
  function n0(t, e, l, n, a) {
    if (e === "submit" && l && l.stateNode === a) {
      var u = sm(
        (a[ee] || null).action
      ), f = n.submitter;
      f && (e = (e = f[ee] || null) ? sm(e.formAction) : f.getAttribute("formAction"), e !== null && (u = e, f = null));
      var r = new Eu(
        "action",
        "action",
        null,
        n,
        a
      );
      t.push({
        event: r,
        listeners: [
          {
            instance: null,
            listener: function() {
              if (n.defaultPrevented) {
                if (zl !== 0) {
                  var y = f ? rm(a, f) : new FormData(a);
                  sf(
                    l,
                    {
                      pending: !0,
                      data: y,
                      method: a.method,
                      action: u
                    },
                    null,
                    y
                  );
                }
              } else
                typeof u == "function" && (r.preventDefault(), y = f ? rm(a, f) : new FormData(a), sf(
                  l,
                  {
                    pending: !0,
                    data: y,
                    method: a.method,
                    action: u
                  },
                  u,
                  y
                ));
            },
            currentTarget: a
          }
        ]
      });
    }
  }
  for (var Vf = 0; Vf < zc.length; Vf++) {
    var Kf = zc[Vf], a0 = Kf.toLowerCase(), u0 = Kf[0].toUpperCase() + Kf.slice(1);
    xe(
      a0,
      "on" + u0
    );
  }
  xe(Gs, "onAnimationEnd"), xe(Qs, "onAnimationIteration"), xe(Zs, "onAnimationStart"), xe("dblclick", "onDoubleClick"), xe("focusin", "onFocus"), xe("focusout", "onBlur"), xe(Ey, "onTransitionRun"), xe(Ty, "onTransitionStart"), xe(Ay, "onTransitionCancel"), xe(Vs, "onTransitionEnd"), gn("onMouseEnter", ["mouseout", "mouseover"]), gn("onMouseLeave", ["mouseout", "mouseover"]), gn("onPointerEnter", ["pointerout", "pointerover"]), gn("onPointerLeave", ["pointerout", "pointerover"]), Yl(
    "onChange",
    "change click focusin focusout input keydown keyup selectionchange".split(" ")
  ), Yl(
    "onSelect",
    "focusout contextmenu dragend focusin keydown keyup mousedown mouseup selectionchange".split(
      " "
    )
  ), Yl("onBeforeInput", [
    "compositionend",
    "keypress",
    "textInput",
    "paste"
  ]), Yl(
    "onCompositionEnd",
    "compositionend focusout keydown keypress keyup mousedown".split(" ")
  ), Yl(
    "onCompositionStart",
    "compositionstart focusout keydown keypress keyup mousedown".split(" ")
  ), Yl(
    "onCompositionUpdate",
    "compositionupdate focusout keydown keypress keyup mousedown".split(" ")
  );
  var Ya = "abort canplay canplaythrough durationchange emptied encrypted ended error loadeddata loadedmetadata loadstart pause play playing progress ratechange resize seeked seeking stalled suspend timeupdate volumechange waiting".split(
    " "
  ), i0 = new Set(
    "beforetoggle cancel close invalid load scroll scrollend toggle".split(" ").concat(Ya)
  );
  function dm(t, e) {
    e = (e & 4) !== 0;
    for (var l = 0; l < t.length; l++) {
      var n = t[l], a = n.event;
      n = n.listeners;
      t: {
        var u = void 0;
        if (e)
          for (var f = n.length - 1; 0 <= f; f--) {
            var r = n[f], y = r.instance, A = r.currentTarget;
            if (r = r.listener, y !== u && a.isPropagationStopped())
              break t;
            u = r, a.currentTarget = A;
            try {
              u(a);
            } catch (C) {
              Ru(C);
            }
            a.currentTarget = null, u = y;
          }
        else
          for (f = 0; f < n.length; f++) {
            if (r = n[f], y = r.instance, A = r.currentTarget, r = r.listener, y !== u && a.isPropagationStopped())
              break t;
            u = r, a.currentTarget = A;
            try {
              u(a);
            } catch (C) {
              Ru(C);
            }
            a.currentTarget = null, u = y;
          }
      }
    }
  }
  function rt(t, e) {
    var l = e[ac];
    l === void 0 && (l = e[ac] = /* @__PURE__ */ new Set());
    var n = t + "__bubble";
    l.has(n) || (mm(e, t, 2, !1), l.add(n));
  }
  function Jf(t, e, l) {
    var n = 0;
    e && (n |= 4), mm(
      l,
      t,
      n,
      e
    );
  }
  var si = "_reactListening" + Math.random().toString(36).slice(2);
  function Wf(t) {
    if (!t[si]) {
      t[si] = !0, is.forEach(function(l) {
        l !== "selectionchange" && (i0.has(l) || Jf(l, !1, t), Jf(l, !0, t));
      });
      var e = t.nodeType === 9 ? t : t.ownerDocument;
      e === null || e[si] || (e[si] = !0, Jf("selectionchange", !1, e));
    }
  }
  function mm(t, e, l, n) {
    switch (Gm(e)) {
      case 2:
        var a = w0;
        break;
      case 8:
        a = H0;
        break;
      default:
        a = oo;
    }
    l = a.bind(
      null,
      e,
      l,
      t
    ), a = void 0, !mc || e !== "touchstart" && e !== "touchmove" && e !== "wheel" || (a = !0), n ? a !== void 0 ? t.addEventListener(e, l, {
      capture: !0,
      passive: a
    }) : t.addEventListener(e, l, !0) : a !== void 0 ? t.addEventListener(e, l, {
      passive: a
    }) : t.addEventListener(e, l, !1);
  }
  function kf(t, e, l, n, a) {
    var u = n;
    if ((e & 1) === 0 && (e & 2) === 0 && n !== null)
      t: for (; ; ) {
        if (n === null) return;
        var f = n.tag;
        if (f === 3 || f === 4) {
          var r = n.stateNode.containerInfo;
          if (r === a) break;
          if (f === 4)
            for (f = n.return; f !== null; ) {
              var y = f.tag;
              if ((y === 3 || y === 4) && f.stateNode.containerInfo === a)
                return;
              f = f.return;
            }
          for (; r !== null; ) {
            if (f = vn(r), f === null) return;
            if (y = f.tag, y === 5 || y === 6 || y === 26 || y === 27) {
              n = u = f;
              continue t;
            }
            r = r.parentNode;
          }
        }
        n = n.return;
      }
    bs(function() {
      var A = u, C = rc(l), U = [];
      t: {
        var O = Ks.get(t);
        if (O !== void 0) {
          var _ = Eu, J = t;
          switch (t) {
            case "keypress":
              if (Su(l) === 0) break t;
            case "keydown":
            case "keyup":
              _ = Ph;
              break;
            case "focusin":
              J = "focus", _ = gc;
              break;
            case "focusout":
              J = "blur", _ = gc;
              break;
            case "beforeblur":
            case "afterblur":
              _ = gc;
              break;
            case "click":
              if (l.button === 2) break t;
            case "auxclick":
            case "dblclick":
            case "mousedown":
            case "mousemove":
            case "mouseup":
            case "mouseout":
            case "mouseover":
            case "contextmenu":
              _ = Es;
              break;
            case "drag":
            case "dragend":
            case "dragenter":
            case "dragexit":
            case "dragleave":
            case "dragover":
            case "dragstart":
            case "drop":
              _ = Xh;
              break;
            case "touchcancel":
            case "touchend":
            case "touchmove":
            case "touchstart":
              _ = ly;
              break;
            case Gs:
            case Qs:
            case Zs:
              _ = Zh;
              break;
            case Vs:
              _ = ay;
              break;
            case "scroll":
            case "scrollend":
              _ = Yh;
              break;
            case "wheel":
              _ = iy;
              break;
            case "copy":
            case "cut":
            case "paste":
              _ = Kh;
              break;
            case "gotpointercapture":
            case "lostpointercapture":
            case "pointercancel":
            case "pointerdown":
            case "pointermove":
            case "pointerout":
            case "pointerover":
            case "pointerup":
              _ = As;
              break;
            case "toggle":
            case "beforetoggle":
              _ = fy;
          }
          var tt = (e & 4) !== 0, Ot = !tt && (t === "scroll" || t === "scrollend"), E = tt ? O !== null ? O + "Capture" : null : O;
          tt = [];
          for (var b = A, T; b !== null; ) {
            var x = b;
            if (T = x.stateNode, x = x.tag, x !== 5 && x !== 26 && x !== 27 || T === null || E === null || (x = ca(b, E), x != null && tt.push(
              ja(b, x, T)
            )), Ot) break;
            b = b.return;
          }
          0 < tt.length && (O = new _(
            O,
            J,
            null,
            l,
            C
          ), U.push({ event: O, listeners: tt }));
        }
      }
      if ((e & 7) === 0) {
        t: {
          if (O = t === "mouseover" || t === "pointerover", _ = t === "mouseout" || t === "pointerout", O && l !== sc && (J = l.relatedTarget || l.fromElement) && (vn(J) || J[mn]))
            break t;
          if ((_ || O) && (O = C.window === C ? C : (O = C.ownerDocument) ? O.defaultView || O.parentWindow : window, _ ? (J = l.relatedTarget || l.toElement, _ = A, J = J ? vn(J) : null, J !== null && (Ot = m(J), tt = J.tag, J !== Ot || tt !== 5 && tt !== 27 && tt !== 6) && (J = null)) : (_ = null, J = A), _ !== J)) {
            if (tt = Es, x = "onMouseLeave", E = "onMouseEnter", b = "mouse", (t === "pointerout" || t === "pointerover") && (tt = As, x = "onPointerLeave", E = "onPointerEnter", b = "pointer"), Ot = _ == null ? O : ia(_), T = J == null ? O : ia(J), O = new tt(
              x,
              b + "leave",
              _,
              l,
              C
            ), O.target = Ot, O.relatedTarget = T, x = null, vn(C) === A && (tt = new tt(
              E,
              b + "enter",
              J,
              l,
              C
            ), tt.target = T, tt.relatedTarget = Ot, x = tt), Ot = x, _ && J)
              e: {
                for (tt = c0, E = _, b = J, T = 0, x = E; x; x = tt(x))
                  T++;
                x = 0;
                for (var P = b; P; P = tt(P))
                  x++;
                for (; 0 < T - x; )
                  E = tt(E), T--;
                for (; 0 < x - T; )
                  b = tt(b), x--;
                for (; T--; ) {
                  if (E === b || b !== null && E === b.alternate) {
                    tt = E;
                    break e;
                  }
                  E = tt(E), b = tt(b);
                }
                tt = null;
              }
            else tt = null;
            _ !== null && vm(
              U,
              O,
              _,
              tt,
              !1
            ), J !== null && Ot !== null && vm(
              U,
              Ot,
              J,
              tt,
              !0
            );
          }
        }
        t: {
          if (O = A ? ia(A) : window, _ = O.nodeName && O.nodeName.toLowerCase(), _ === "select" || _ === "input" && O.type === "file")
            var gt = Ds;
          else if (Cs(O))
            if (Ns)
              gt = by;
            else {
              gt = yy;
              var k = hy;
            }
          else
            _ = O.nodeName, !_ || _.toLowerCase() !== "input" || O.type !== "checkbox" && O.type !== "radio" ? A && oc(A.elementType) && (gt = Ds) : gt = gy;
          if (gt && (gt = gt(t, A))) {
            xs(
              U,
              gt,
              l,
              C
            );
            break t;
          }
          k && k(t, O, A), t === "focusout" && A && O.type === "number" && A.memoizedProps.value != null && fc(O, "number", O.value);
        }
        switch (k = A ? ia(A) : window, t) {
          case "focusin":
            (Cs(k) || k.contentEditable === "true") && (An = k, Ac = A, ha = null);
            break;
          case "focusout":
            ha = Ac = An = null;
            break;
          case "mousedown":
            Rc = !0;
            break;
          case "contextmenu":
          case "mouseup":
          case "dragend":
            Rc = !1, js(U, l, C);
            break;
          case "selectionchange":
            if (py) break;
          case "keydown":
          case "keyup":
            js(U, l, C);
        }
        var ot;
        if (Sc)
          t: {
            switch (t) {
              case "compositionstart":
                var ht = "onCompositionStart";
                break t;
              case "compositionend":
                ht = "onCompositionEnd";
                break t;
              case "compositionupdate":
                ht = "onCompositionUpdate";
                break t;
            }
            ht = void 0;
          }
        else
          Tn ? _s(t, l) && (ht = "onCompositionEnd") : t === "keydown" && l.keyCode === 229 && (ht = "onCompositionStart");
        ht && (Rs && l.locale !== "ko" && (Tn || ht !== "onCompositionStart" ? ht === "onCompositionEnd" && Tn && (ot = Ss()) : (sl = C, vc = "value" in sl ? sl.value : sl.textContent, Tn = !0)), k = ri(A, ht), 0 < k.length && (ht = new Ts(
          ht,
          t,
          null,
          l,
          C
        ), U.push({ event: ht, listeners: k }), ot ? ht.data = ot : (ot = Ms(l), ot !== null && (ht.data = ot)))), (ot = sy ? ry(t, l) : dy(t, l)) && (ht = ri(A, "onBeforeInput"), 0 < ht.length && (k = new Ts(
          "onBeforeInput",
          "beforeinput",
          null,
          l,
          C
        ), U.push({
          event: k,
          listeners: ht
        }), k.data = ot)), n0(
          U,
          t,
          A,
          l,
          C
        );
      }
      dm(U, e);
    });
  }
  function ja(t, e, l) {
    return {
      instance: t,
      listener: e,
      currentTarget: l
    };
  }
  function ri(t, e) {
    for (var l = e + "Capture", n = []; t !== null; ) {
      var a = t, u = a.stateNode;
      if (a = a.tag, a !== 5 && a !== 26 && a !== 27 || u === null || (a = ca(t, l), a != null && n.unshift(
        ja(t, a, u)
      ), a = ca(t, e), a != null && n.push(
        ja(t, a, u)
      )), t.tag === 3) return n;
      t = t.return;
    }
    return [];
  }
  function c0(t) {
    if (t === null) return null;
    do
      t = t.return;
    while (t && t.tag !== 5 && t.tag !== 27);
    return t || null;
  }
  function vm(t, e, l, n, a) {
    for (var u = e._reactName, f = []; l !== null && l !== n; ) {
      var r = l, y = r.alternate, A = r.stateNode;
      if (r = r.tag, y !== null && y === n) break;
      r !== 5 && r !== 26 && r !== 27 || A === null || (y = A, a ? (A = ca(l, u), A != null && f.unshift(
        ja(l, A, y)
      )) : a || (A = ca(l, u), A != null && f.push(
        ja(l, A, y)
      ))), l = l.return;
    }
    f.length !== 0 && t.push({ event: e, listeners: f });
  }
  var f0 = /\r\n?/g, o0 = /\u0000|\uFFFD/g;
  function hm(t) {
    return (typeof t == "string" ? t : "" + t).replace(f0, `
`).replace(o0, "");
  }
  function ym(t, e) {
    return e = hm(e), hm(t) === e;
  }
  function Rt(t, e, l, n, a, u) {
    switch (l) {
      case "children":
        typeof n == "string" ? e === "body" || e === "textarea" && n === "" || Sn(t, n) : (typeof n == "number" || typeof n == "bigint") && e !== "body" && Sn(t, "" + n);
        break;
      case "className":
        hu(t, "class", n);
        break;
      case "tabIndex":
        hu(t, "tabindex", n);
        break;
      case "dir":
      case "role":
      case "viewBox":
      case "width":
      case "height":
        hu(t, l, n);
        break;
      case "style":
        ys(t, n, u);
        break;
      case "data":
        if (e !== "object") {
          hu(t, "data", n);
          break;
        }
      case "src":
      case "href":
        if (n === "" && (e !== "a" || l !== "href")) {
          t.removeAttribute(l);
          break;
        }
        if (n == null || typeof n == "function" || typeof n == "symbol" || typeof n == "boolean") {
          t.removeAttribute(l);
          break;
        }
        n = gu("" + n), t.setAttribute(l, n);
        break;
      case "action":
      case "formAction":
        if (typeof n == "function") {
          t.setAttribute(
            l,
            "javascript:throw new Error('A React form was unexpectedly submitted. If you called form.submit() manually, consider using form.requestSubmit() instead. If you\\'re trying to use event.stopPropagation() in a submit event handler, consider also calling event.preventDefault().')"
          );
          break;
        } else
          typeof u == "function" && (l === "formAction" ? (e !== "input" && Rt(t, e, "name", a.name, a, null), Rt(
            t,
            e,
            "formEncType",
            a.formEncType,
            a,
            null
          ), Rt(
            t,
            e,
            "formMethod",
            a.formMethod,
            a,
            null
          ), Rt(
            t,
            e,
            "formTarget",
            a.formTarget,
            a,
            null
          )) : (Rt(t, e, "encType", a.encType, a, null), Rt(t, e, "method", a.method, a, null), Rt(t, e, "target", a.target, a, null)));
        if (n == null || typeof n == "symbol" || typeof n == "boolean") {
          t.removeAttribute(l);
          break;
        }
        n = gu("" + n), t.setAttribute(l, n);
        break;
      case "onClick":
        n != null && (t.onclick = Qe);
        break;
      case "onScroll":
        n != null && rt("scroll", t);
        break;
      case "onScrollEnd":
        n != null && rt("scrollend", t);
        break;
      case "dangerouslySetInnerHTML":
        if (n != null) {
          if (typeof n != "object" || !("__html" in n))
            throw Error(o(61));
          if (l = n.__html, l != null) {
            if (a.children != null) throw Error(o(60));
            t.innerHTML = l;
          }
        }
        break;
      case "multiple":
        t.multiple = n && typeof n != "function" && typeof n != "symbol";
        break;
      case "muted":
        t.muted = n && typeof n != "function" && typeof n != "symbol";
        break;
      case "suppressContentEditableWarning":
      case "suppressHydrationWarning":
      case "defaultValue":
      case "defaultChecked":
      case "innerHTML":
      case "ref":
        break;
      case "autoFocus":
        break;
      case "xlinkHref":
        if (n == null || typeof n == "function" || typeof n == "boolean" || typeof n == "symbol") {
          t.removeAttribute("xlink:href");
          break;
        }
        l = gu("" + n), t.setAttributeNS(
          "http://www.w3.org/1999/xlink",
          "xlink:href",
          l
        );
        break;
      case "contentEditable":
      case "spellCheck":
      case "draggable":
      case "value":
      case "autoReverse":
      case "externalResourcesRequired":
      case "focusable":
      case "preserveAlpha":
        n != null && typeof n != "function" && typeof n != "symbol" ? t.setAttribute(l, "" + n) : t.removeAttribute(l);
        break;
      case "inert":
      case "allowFullScreen":
      case "async":
      case "autoPlay":
      case "controls":
      case "default":
      case "defer":
      case "disabled":
      case "disablePictureInPicture":
      case "disableRemotePlayback":
      case "formNoValidate":
      case "hidden":
      case "loop":
      case "noModule":
      case "noValidate":
      case "open":
      case "playsInline":
      case "readOnly":
      case "required":
      case "reversed":
      case "scoped":
      case "seamless":
      case "itemScope":
        n && typeof n != "function" && typeof n != "symbol" ? t.setAttribute(l, "") : t.removeAttribute(l);
        break;
      case "capture":
      case "download":
        n === !0 ? t.setAttribute(l, "") : n !== !1 && n != null && typeof n != "function" && typeof n != "symbol" ? t.setAttribute(l, n) : t.removeAttribute(l);
        break;
      case "cols":
      case "rows":
      case "size":
      case "span":
        n != null && typeof n != "function" && typeof n != "symbol" && !isNaN(n) && 1 <= n ? t.setAttribute(l, n) : t.removeAttribute(l);
        break;
      case "rowSpan":
      case "start":
        n == null || typeof n == "function" || typeof n == "symbol" || isNaN(n) ? t.removeAttribute(l) : t.setAttribute(l, n);
        break;
      case "popover":
        rt("beforetoggle", t), rt("toggle", t), vu(t, "popover", n);
        break;
      case "xlinkActuate":
        Ge(
          t,
          "http://www.w3.org/1999/xlink",
          "xlink:actuate",
          n
        );
        break;
      case "xlinkArcrole":
        Ge(
          t,
          "http://www.w3.org/1999/xlink",
          "xlink:arcrole",
          n
        );
        break;
      case "xlinkRole":
        Ge(
          t,
          "http://www.w3.org/1999/xlink",
          "xlink:role",
          n
        );
        break;
      case "xlinkShow":
        Ge(
          t,
          "http://www.w3.org/1999/xlink",
          "xlink:show",
          n
        );
        break;
      case "xlinkTitle":
        Ge(
          t,
          "http://www.w3.org/1999/xlink",
          "xlink:title",
          n
        );
        break;
      case "xlinkType":
        Ge(
          t,
          "http://www.w3.org/1999/xlink",
          "xlink:type",
          n
        );
        break;
      case "xmlBase":
        Ge(
          t,
          "http://www.w3.org/XML/1998/namespace",
          "xml:base",
          n
        );
        break;
      case "xmlLang":
        Ge(
          t,
          "http://www.w3.org/XML/1998/namespace",
          "xml:lang",
          n
        );
        break;
      case "xmlSpace":
        Ge(
          t,
          "http://www.w3.org/XML/1998/namespace",
          "xml:space",
          n
        );
        break;
      case "is":
        vu(t, "is", n);
        break;
      case "innerText":
      case "textContent":
        break;
      default:
        (!(2 < l.length) || l[0] !== "o" && l[0] !== "O" || l[1] !== "n" && l[1] !== "N") && (l = qh.get(l) || l, vu(t, l, n));
    }
  }
  function Ff(t, e, l, n, a, u) {
    switch (l) {
      case "style":
        ys(t, n, u);
        break;
      case "dangerouslySetInnerHTML":
        if (n != null) {
          if (typeof n != "object" || !("__html" in n))
            throw Error(o(61));
          if (l = n.__html, l != null) {
            if (a.children != null) throw Error(o(60));
            t.innerHTML = l;
          }
        }
        break;
      case "children":
        typeof n == "string" ? Sn(t, n) : (typeof n == "number" || typeof n == "bigint") && Sn(t, "" + n);
        break;
      case "onScroll":
        n != null && rt("scroll", t);
        break;
      case "onScrollEnd":
        n != null && rt("scrollend", t);
        break;
      case "onClick":
        n != null && (t.onclick = Qe);
        break;
      case "suppressContentEditableWarning":
      case "suppressHydrationWarning":
      case "innerHTML":
      case "ref":
        break;
      case "innerText":
      case "textContent":
        break;
      default:
        if (!cs.hasOwnProperty(l))
          t: {
            if (l[0] === "o" && l[1] === "n" && (a = l.endsWith("Capture"), e = l.slice(2, a ? l.length - 7 : void 0), u = t[ee] || null, u = u != null ? u[l] : null, typeof u == "function" && t.removeEventListener(e, u, a), typeof n == "function")) {
              typeof u != "function" && u !== null && (l in t ? t[l] = null : t.hasAttribute(l) && t.removeAttribute(l)), t.addEventListener(e, n, a);
              break t;
            }
            l in t ? t[l] = n : n === !0 ? t.setAttribute(l, "") : vu(t, l, n);
          }
    }
  }
  function It(t, e, l) {
    switch (e) {
      case "div":
      case "span":
      case "svg":
      case "path":
      case "a":
      case "g":
      case "p":
      case "li":
        break;
      case "img":
        rt("error", t), rt("load", t);
        var n = !1, a = !1, u;
        for (u in l)
          if (l.hasOwnProperty(u)) {
            var f = l[u];
            if (f != null)
              switch (u) {
                case "src":
                  n = !0;
                  break;
                case "srcSet":
                  a = !0;
                  break;
                case "children":
                case "dangerouslySetInnerHTML":
                  throw Error(o(137, e));
                default:
                  Rt(t, e, u, f, l, null);
              }
          }
        a && Rt(t, e, "srcSet", l.srcSet, l, null), n && Rt(t, e, "src", l.src, l, null);
        return;
      case "input":
        rt("invalid", t);
        var r = u = f = a = null, y = null, A = null;
        for (n in l)
          if (l.hasOwnProperty(n)) {
            var C = l[n];
            if (C != null)
              switch (n) {
                case "name":
                  a = C;
                  break;
                case "type":
                  f = C;
                  break;
                case "checked":
                  y = C;
                  break;
                case "defaultChecked":
                  A = C;
                  break;
                case "value":
                  u = C;
                  break;
                case "defaultValue":
                  r = C;
                  break;
                case "children":
                case "dangerouslySetInnerHTML":
                  if (C != null)
                    throw Error(o(137, e));
                  break;
                default:
                  Rt(t, e, n, C, l, null);
              }
          }
        ds(
          t,
          u,
          r,
          y,
          A,
          f,
          a,
          !1
        );
        return;
      case "select":
        rt("invalid", t), n = f = u = null;
        for (a in l)
          if (l.hasOwnProperty(a) && (r = l[a], r != null))
            switch (a) {
              case "value":
                u = r;
                break;
              case "defaultValue":
                f = r;
                break;
              case "multiple":
                n = r;
              default:
                Rt(t, e, a, r, l, null);
            }
        e = u, l = f, t.multiple = !!n, e != null ? bn(t, !!n, e, !1) : l != null && bn(t, !!n, l, !0);
        return;
      case "textarea":
        rt("invalid", t), u = a = n = null;
        for (f in l)
          if (l.hasOwnProperty(f) && (r = l[f], r != null))
            switch (f) {
              case "value":
                n = r;
                break;
              case "defaultValue":
                a = r;
                break;
              case "children":
                u = r;
                break;
              case "dangerouslySetInnerHTML":
                if (r != null) throw Error(o(91));
                break;
              default:
                Rt(t, e, f, r, l, null);
            }
        vs(t, n, a, u);
        return;
      case "option":
        for (y in l)
          if (l.hasOwnProperty(y) && (n = l[y], n != null))
            switch (y) {
              case "selected":
                t.selected = n && typeof n != "function" && typeof n != "symbol";
                break;
              default:
                Rt(t, e, y, n, l, null);
            }
        return;
      case "dialog":
        rt("beforetoggle", t), rt("toggle", t), rt("cancel", t), rt("close", t);
        break;
      case "iframe":
      case "object":
        rt("load", t);
        break;
      case "video":
      case "audio":
        for (n = 0; n < Ya.length; n++)
          rt(Ya[n], t);
        break;
      case "image":
        rt("error", t), rt("load", t);
        break;
      case "details":
        rt("toggle", t);
        break;
      case "embed":
      case "source":
      case "link":
        rt("error", t), rt("load", t);
      case "area":
      case "base":
      case "br":
      case "col":
      case "hr":
      case "keygen":
      case "meta":
      case "param":
      case "track":
      case "wbr":
      case "menuitem":
        for (A in l)
          if (l.hasOwnProperty(A) && (n = l[A], n != null))
            switch (A) {
              case "children":
              case "dangerouslySetInnerHTML":
                throw Error(o(137, e));
              default:
                Rt(t, e, A, n, l, null);
            }
        return;
      default:
        if (oc(e)) {
          for (C in l)
            l.hasOwnProperty(C) && (n = l[C], n !== void 0 && Ff(
              t,
              e,
              C,
              n,
              l,
              void 0
            ));
          return;
        }
    }
    for (r in l)
      l.hasOwnProperty(r) && (n = l[r], n != null && Rt(t, e, r, n, l, null));
  }
  function s0(t, e, l, n) {
    switch (e) {
      case "div":
      case "span":
      case "svg":
      case "path":
      case "a":
      case "g":
      case "p":
      case "li":
        break;
      case "input":
        var a = null, u = null, f = null, r = null, y = null, A = null, C = null;
        for (_ in l) {
          var U = l[_];
          if (l.hasOwnProperty(_) && U != null)
            switch (_) {
              case "checked":
                break;
              case "value":
                break;
              case "defaultValue":
                y = U;
              default:
                n.hasOwnProperty(_) || Rt(t, e, _, null, n, U);
            }
        }
        for (var O in n) {
          var _ = n[O];
          if (U = l[O], n.hasOwnProperty(O) && (_ != null || U != null))
            switch (O) {
              case "type":
                u = _;
                break;
              case "name":
                a = _;
                break;
              case "checked":
                A = _;
                break;
              case "defaultChecked":
                C = _;
                break;
              case "value":
                f = _;
                break;
              case "defaultValue":
                r = _;
                break;
              case "children":
              case "dangerouslySetInnerHTML":
                if (_ != null)
                  throw Error(o(137, e));
                break;
              default:
                _ !== U && Rt(
                  t,
                  e,
                  O,
                  _,
                  n,
                  U
                );
            }
        }
        cc(
          t,
          f,
          r,
          y,
          A,
          C,
          u,
          a
        );
        return;
      case "select":
        _ = f = r = O = null;
        for (u in l)
          if (y = l[u], l.hasOwnProperty(u) && y != null)
            switch (u) {
              case "value":
                break;
              case "multiple":
                _ = y;
              default:
                n.hasOwnProperty(u) || Rt(
                  t,
                  e,
                  u,
                  null,
                  n,
                  y
                );
            }
        for (a in n)
          if (u = n[a], y = l[a], n.hasOwnProperty(a) && (u != null || y != null))
            switch (a) {
              case "value":
                O = u;
                break;
              case "defaultValue":
                r = u;
                break;
              case "multiple":
                f = u;
              default:
                u !== y && Rt(
                  t,
                  e,
                  a,
                  u,
                  n,
                  y
                );
            }
        e = r, l = f, n = _, O != null ? bn(t, !!l, O, !1) : !!n != !!l && (e != null ? bn(t, !!l, e, !0) : bn(t, !!l, l ? [] : "", !1));
        return;
      case "textarea":
        _ = O = null;
        for (r in l)
          if (a = l[r], l.hasOwnProperty(r) && a != null && !n.hasOwnProperty(r))
            switch (r) {
              case "value":
                break;
              case "children":
                break;
              default:
                Rt(t, e, r, null, n, a);
            }
        for (f in n)
          if (a = n[f], u = l[f], n.hasOwnProperty(f) && (a != null || u != null))
            switch (f) {
              case "value":
                O = a;
                break;
              case "defaultValue":
                _ = a;
                break;
              case "children":
                break;
              case "dangerouslySetInnerHTML":
                if (a != null) throw Error(o(91));
                break;
              default:
                a !== u && Rt(t, e, f, a, n, u);
            }
        ms(t, O, _);
        return;
      case "option":
        for (var J in l)
          if (O = l[J], l.hasOwnProperty(J) && O != null && !n.hasOwnProperty(J))
            switch (J) {
              case "selected":
                t.selected = !1;
                break;
              default:
                Rt(
                  t,
                  e,
                  J,
                  null,
                  n,
                  O
                );
            }
        for (y in n)
          if (O = n[y], _ = l[y], n.hasOwnProperty(y) && O !== _ && (O != null || _ != null))
            switch (y) {
              case "selected":
                t.selected = O && typeof O != "function" && typeof O != "symbol";
                break;
              default:
                Rt(
                  t,
                  e,
                  y,
                  O,
                  n,
                  _
                );
            }
        return;
      case "img":
      case "link":
      case "area":
      case "base":
      case "br":
      case "col":
      case "embed":
      case "hr":
      case "keygen":
      case "meta":
      case "param":
      case "source":
      case "track":
      case "wbr":
      case "menuitem":
        for (var tt in l)
          O = l[tt], l.hasOwnProperty(tt) && O != null && !n.hasOwnProperty(tt) && Rt(t, e, tt, null, n, O);
        for (A in n)
          if (O = n[A], _ = l[A], n.hasOwnProperty(A) && O !== _ && (O != null || _ != null))
            switch (A) {
              case "children":
              case "dangerouslySetInnerHTML":
                if (O != null)
                  throw Error(o(137, e));
                break;
              default:
                Rt(
                  t,
                  e,
                  A,
                  O,
                  n,
                  _
                );
            }
        return;
      default:
        if (oc(e)) {
          for (var Ot in l)
            O = l[Ot], l.hasOwnProperty(Ot) && O !== void 0 && !n.hasOwnProperty(Ot) && Ff(
              t,
              e,
              Ot,
              void 0,
              n,
              O
            );
          for (C in n)
            O = n[C], _ = l[C], !n.hasOwnProperty(C) || O === _ || O === void 0 && _ === void 0 || Ff(
              t,
              e,
              C,
              O,
              n,
              _
            );
          return;
        }
    }
    for (var E in l)
      O = l[E], l.hasOwnProperty(E) && O != null && !n.hasOwnProperty(E) && Rt(t, e, E, null, n, O);
    for (U in n)
      O = n[U], _ = l[U], !n.hasOwnProperty(U) || O === _ || O == null && _ == null || Rt(t, e, U, O, n, _);
  }
  function gm(t) {
    switch (t) {
      case "css":
      case "script":
      case "font":
      case "img":
      case "image":
      case "input":
      case "link":
        return !0;
      default:
        return !1;
    }
  }
  function r0() {
    if (typeof performance.getEntriesByType == "function") {
      for (var t = 0, e = 0, l = performance.getEntriesByType("resource"), n = 0; n < l.length; n++) {
        var a = l[n], u = a.transferSize, f = a.initiatorType, r = a.duration;
        if (u && r && gm(f)) {
          for (f = 0, r = a.responseEnd, n += 1; n < l.length; n++) {
            var y = l[n], A = y.startTime;
            if (A > r) break;
            var C = y.transferSize, U = y.initiatorType;
            C && gm(U) && (y = y.responseEnd, f += C * (y < r ? 1 : (r - A) / (y - A)));
          }
          if (--n, e += 8 * (u + f) / (a.duration / 1e3), t++, 10 < t) break;
        }
      }
      if (0 < t) return e / t / 1e6;
    }
    return navigator.connection && (t = navigator.connection.downlink, typeof t == "number") ? t : 5;
  }
  var $f = null, If = null;
  function di(t) {
    return t.nodeType === 9 ? t : t.ownerDocument;
  }
  function bm(t) {
    switch (t) {
      case "http://www.w3.org/2000/svg":
        return 1;
      case "http://www.w3.org/1998/Math/MathML":
        return 2;
      default:
        return 0;
    }
  }
  function Sm(t, e) {
    if (t === 0)
      switch (e) {
        case "svg":
          return 1;
        case "math":
          return 2;
        default:
          return 0;
      }
    return t === 1 && e === "foreignObject" ? 0 : t;
  }
  function Pf(t, e) {
    return t === "textarea" || t === "noscript" || typeof e.children == "string" || typeof e.children == "number" || typeof e.children == "bigint" || typeof e.dangerouslySetInnerHTML == "object" && e.dangerouslySetInnerHTML !== null && e.dangerouslySetInnerHTML.__html != null;
  }
  var to = null;
  function d0() {
    var t = window.event;
    return t && t.type === "popstate" ? t === to ? !1 : (to = t, !0) : (to = null, !1);
  }
  var pm = typeof setTimeout == "function" ? setTimeout : void 0, m0 = typeof clearTimeout == "function" ? clearTimeout : void 0, Em = typeof Promise == "function" ? Promise : void 0, v0 = typeof queueMicrotask == "function" ? queueMicrotask : typeof Em < "u" ? function(t) {
    return Em.resolve(null).then(t).catch(h0);
  } : pm;
  function h0(t) {
    setTimeout(function() {
      throw t;
    });
  }
  function _l(t) {
    return t === "head";
  }
  function Tm(t, e) {
    var l = e, n = 0;
    do {
      var a = l.nextSibling;
      if (t.removeChild(l), a && a.nodeType === 8)
        if (l = a.data, l === "/$" || l === "/&") {
          if (n === 0) {
            t.removeChild(a), Fn(e);
            return;
          }
          n--;
        } else if (l === "$" || l === "$?" || l === "$~" || l === "$!" || l === "&")
          n++;
        else if (l === "html")
          Xa(t.ownerDocument.documentElement);
        else if (l === "head") {
          l = t.ownerDocument.head, Xa(l);
          for (var u = l.firstChild; u; ) {
            var f = u.nextSibling, r = u.nodeName;
            u[ua] || r === "SCRIPT" || r === "STYLE" || r === "LINK" && u.rel.toLowerCase() === "stylesheet" || l.removeChild(u), u = f;
          }
        } else
          l === "body" && Xa(t.ownerDocument.body);
      l = a;
    } while (l);
    Fn(e);
  }
  function Am(t, e) {
    var l = t;
    t = 0;
    do {
      var n = l.nextSibling;
      if (l.nodeType === 1 ? e ? (l._stashedDisplay = l.style.display, l.style.display = "none") : (l.style.display = l._stashedDisplay || "", l.getAttribute("style") === "" && l.removeAttribute("style")) : l.nodeType === 3 && (e ? (l._stashedText = l.nodeValue, l.nodeValue = "") : l.nodeValue = l._stashedText || ""), n && n.nodeType === 8)
        if (l = n.data, l === "/$") {
          if (t === 0) break;
          t--;
        } else
          l !== "$" && l !== "$?" && l !== "$~" && l !== "$!" || t++;
      l = n;
    } while (l);
  }
  function eo(t) {
    var e = t.firstChild;
    for (e && e.nodeType === 10 && (e = e.nextSibling); e; ) {
      var l = e;
      switch (e = e.nextSibling, l.nodeName) {
        case "HTML":
        case "HEAD":
        case "BODY":
          eo(l), uc(l);
          continue;
        case "SCRIPT":
        case "STYLE":
          continue;
        case "LINK":
          if (l.rel.toLowerCase() === "stylesheet") continue;
      }
      t.removeChild(l);
    }
  }
  function y0(t, e, l, n) {
    for (; t.nodeType === 1; ) {
      var a = l;
      if (t.nodeName.toLowerCase() !== e.toLowerCase()) {
        if (!n && (t.nodeName !== "INPUT" || t.type !== "hidden"))
          break;
      } else if (n) {
        if (!t[ua])
          switch (e) {
            case "meta":
              if (!t.hasAttribute("itemprop")) break;
              return t;
            case "link":
              if (u = t.getAttribute("rel"), u === "stylesheet" && t.hasAttribute("data-precedence"))
                break;
              if (u !== a.rel || t.getAttribute("href") !== (a.href == null || a.href === "" ? null : a.href) || t.getAttribute("crossorigin") !== (a.crossOrigin == null ? null : a.crossOrigin) || t.getAttribute("title") !== (a.title == null ? null : a.title))
                break;
              return t;
            case "style":
              if (t.hasAttribute("data-precedence")) break;
              return t;
            case "script":
              if (u = t.getAttribute("src"), (u !== (a.src == null ? null : a.src) || t.getAttribute("type") !== (a.type == null ? null : a.type) || t.getAttribute("crossorigin") !== (a.crossOrigin == null ? null : a.crossOrigin)) && u && t.hasAttribute("async") && !t.hasAttribute("itemprop"))
                break;
              return t;
            default:
              return t;
          }
      } else if (e === "input" && t.type === "hidden") {
        var u = a.name == null ? null : "" + a.name;
        if (a.type === "hidden" && t.getAttribute("name") === u)
          return t;
      } else return t;
      if (t = Me(t.nextSibling), t === null) break;
    }
    return null;
  }
  function g0(t, e, l) {
    if (e === "") return null;
    for (; t.nodeType !== 3; )
      if ((t.nodeType !== 1 || t.nodeName !== "INPUT" || t.type !== "hidden") && !l || (t = Me(t.nextSibling), t === null)) return null;
    return t;
  }
  function Rm(t, e) {
    for (; t.nodeType !== 8; )
      if ((t.nodeType !== 1 || t.nodeName !== "INPUT" || t.type !== "hidden") && !e || (t = Me(t.nextSibling), t === null)) return null;
    return t;
  }
  function lo(t) {
    return t.data === "$?" || t.data === "$~";
  }
  function no(t) {
    return t.data === "$!" || t.data === "$?" && t.ownerDocument.readyState !== "loading";
  }
  function b0(t, e) {
    var l = t.ownerDocument;
    if (t.data === "$~") t._reactRetry = e;
    else if (t.data !== "$?" || l.readyState !== "loading")
      e();
    else {
      var n = function() {
        e(), l.removeEventListener("DOMContentLoaded", n);
      };
      l.addEventListener("DOMContentLoaded", n), t._reactRetry = n;
    }
  }
  function Me(t) {
    for (; t != null; t = t.nextSibling) {
      var e = t.nodeType;
      if (e === 1 || e === 3) break;
      if (e === 8) {
        if (e = t.data, e === "$" || e === "$!" || e === "$?" || e === "$~" || e === "&" || e === "F!" || e === "F")
          break;
        if (e === "/$" || e === "/&") return null;
      }
    }
    return t;
  }
  var ao = null;
  function Om(t) {
    t = t.nextSibling;
    for (var e = 0; t; ) {
      if (t.nodeType === 8) {
        var l = t.data;
        if (l === "/$" || l === "/&") {
          if (e === 0)
            return Me(t.nextSibling);
          e--;
        } else
          l !== "$" && l !== "$!" && l !== "$?" && l !== "$~" && l !== "&" || e++;
      }
      t = t.nextSibling;
    }
    return null;
  }
  function zm(t) {
    t = t.previousSibling;
    for (var e = 0; t; ) {
      if (t.nodeType === 8) {
        var l = t.data;
        if (l === "$" || l === "$!" || l === "$?" || l === "$~" || l === "&") {
          if (e === 0) return t;
          e--;
        } else l !== "/$" && l !== "/&" || e++;
      }
      t = t.previousSibling;
    }
    return null;
  }
  function _m(t, e, l) {
    switch (e = di(l), t) {
      case "html":
        if (t = e.documentElement, !t) throw Error(o(452));
        return t;
      case "head":
        if (t = e.head, !t) throw Error(o(453));
        return t;
      case "body":
        if (t = e.body, !t) throw Error(o(454));
        return t;
      default:
        throw Error(o(451));
    }
  }
  function Xa(t) {
    for (var e = t.attributes; e.length; )
      t.removeAttributeNode(e[0]);
    uc(t);
  }
  var Ce = /* @__PURE__ */ new Map(), Mm = /* @__PURE__ */ new Set();
  function mi(t) {
    return typeof t.getRootNode == "function" ? t.getRootNode() : t.nodeType === 9 ? t : t.ownerDocument;
  }
  var ul = B.d;
  B.d = {
    f: S0,
    r: p0,
    D: E0,
    C: T0,
    L: A0,
    m: R0,
    X: z0,
    S: O0,
    M: _0
  };
  function S0() {
    var t = ul.f(), e = ai();
    return t || e;
  }
  function p0(t) {
    var e = hn(t);
    e !== null && e.tag === 5 && e.type === "form" ? Vr(e) : ul.r(t);
  }
  var Jn = typeof document > "u" ? null : document;
  function Cm(t, e, l) {
    var n = Jn;
    if (n && typeof e == "string" && e) {
      var a = Ee(e);
      a = 'link[rel="' + t + '"][href="' + a + '"]', typeof l == "string" && (a += '[crossorigin="' + l + '"]'), Mm.has(a) || (Mm.add(a), t = { rel: t, crossOrigin: l, href: e }, n.querySelector(a) === null && (e = n.createElement("link"), It(e, "link", t), Vt(e), n.head.appendChild(e)));
    }
  }
  function E0(t) {
    ul.D(t), Cm("dns-prefetch", t, null);
  }
  function T0(t, e) {
    ul.C(t, e), Cm("preconnect", t, e);
  }
  function A0(t, e, l) {
    ul.L(t, e, l);
    var n = Jn;
    if (n && t && e) {
      var a = 'link[rel="preload"][as="' + Ee(e) + '"]';
      e === "image" && l && l.imageSrcSet ? (a += '[imagesrcset="' + Ee(
        l.imageSrcSet
      ) + '"]', typeof l.imageSizes == "string" && (a += '[imagesizes="' + Ee(
        l.imageSizes
      ) + '"]')) : a += '[href="' + Ee(t) + '"]';
      var u = a;
      switch (e) {
        case "style":
          u = Wn(t);
          break;
        case "script":
          u = kn(t);
      }
      Ce.has(u) || (t = z(
        {
          rel: "preload",
          href: e === "image" && l && l.imageSrcSet ? void 0 : t,
          as: e
        },
        l
      ), Ce.set(u, t), n.querySelector(a) !== null || e === "style" && n.querySelector(Ga(u)) || e === "script" && n.querySelector(Qa(u)) || (e = n.createElement("link"), It(e, "link", t), Vt(e), n.head.appendChild(e)));
    }
  }
  function R0(t, e) {
    ul.m(t, e);
    var l = Jn;
    if (l && t) {
      var n = e && typeof e.as == "string" ? e.as : "script", a = 'link[rel="modulepreload"][as="' + Ee(n) + '"][href="' + Ee(t) + '"]', u = a;
      switch (n) {
        case "audioworklet":
        case "paintworklet":
        case "serviceworker":
        case "sharedworker":
        case "worker":
        case "script":
          u = kn(t);
      }
      if (!Ce.has(u) && (t = z({ rel: "modulepreload", href: t }, e), Ce.set(u, t), l.querySelector(a) === null)) {
        switch (n) {
          case "audioworklet":
          case "paintworklet":
          case "serviceworker":
          case "sharedworker":
          case "worker":
          case "script":
            if (l.querySelector(Qa(u)))
              return;
        }
        n = l.createElement("link"), It(n, "link", t), Vt(n), l.head.appendChild(n);
      }
    }
  }
  function O0(t, e, l) {
    ul.S(t, e, l);
    var n = Jn;
    if (n && t) {
      var a = yn(n).hoistableStyles, u = Wn(t);
      e = e || "default";
      var f = a.get(u);
      if (!f) {
        var r = { loading: 0, preload: null };
        if (f = n.querySelector(
          Ga(u)
        ))
          r.loading = 5;
        else {
          t = z(
            { rel: "stylesheet", href: t, "data-precedence": e },
            l
          ), (l = Ce.get(u)) && uo(t, l);
          var y = f = n.createElement("link");
          Vt(y), It(y, "link", t), y._p = new Promise(function(A, C) {
            y.onload = A, y.onerror = C;
          }), y.addEventListener("load", function() {
            r.loading |= 1;
          }), y.addEventListener("error", function() {
            r.loading |= 2;
          }), r.loading |= 4, vi(f, e, n);
        }
        f = {
          type: "stylesheet",
          instance: f,
          count: 1,
          state: r
        }, a.set(u, f);
      }
    }
  }
  function z0(t, e) {
    ul.X(t, e);
    var l = Jn;
    if (l && t) {
      var n = yn(l).hoistableScripts, a = kn(t), u = n.get(a);
      u || (u = l.querySelector(Qa(a)), u || (t = z({ src: t, async: !0 }, e), (e = Ce.get(a)) && io(t, e), u = l.createElement("script"), Vt(u), It(u, "link", t), l.head.appendChild(u)), u = {
        type: "script",
        instance: u,
        count: 1,
        state: null
      }, n.set(a, u));
    }
  }
  function _0(t, e) {
    ul.M(t, e);
    var l = Jn;
    if (l && t) {
      var n = yn(l).hoistableScripts, a = kn(t), u = n.get(a);
      u || (u = l.querySelector(Qa(a)), u || (t = z({ src: t, async: !0, type: "module" }, e), (e = Ce.get(a)) && io(t, e), u = l.createElement("script"), Vt(u), It(u, "link", t), l.head.appendChild(u)), u = {
        type: "script",
        instance: u,
        count: 1,
        state: null
      }, n.set(a, u));
    }
  }
  function xm(t, e, l, n) {
    var a = (a = nt.current) ? mi(a) : null;
    if (!a) throw Error(o(446));
    switch (t) {
      case "meta":
      case "title":
        return null;
      case "style":
        return typeof l.precedence == "string" && typeof l.href == "string" ? (e = Wn(l.href), l = yn(
          a
        ).hoistableStyles, n = l.get(e), n || (n = {
          type: "style",
          instance: null,
          count: 0,
          state: null
        }, l.set(e, n)), n) : { type: "void", instance: null, count: 0, state: null };
      case "link":
        if (l.rel === "stylesheet" && typeof l.href == "string" && typeof l.precedence == "string") {
          t = Wn(l.href);
          var u = yn(
            a
          ).hoistableStyles, f = u.get(t);
          if (f || (a = a.ownerDocument || a, f = {
            type: "stylesheet",
            instance: null,
            count: 0,
            state: { loading: 0, preload: null }
          }, u.set(t, f), (u = a.querySelector(
            Ga(t)
          )) && !u._p && (f.instance = u, f.state.loading = 5), Ce.has(t) || (l = {
            rel: "preload",
            as: "style",
            href: l.href,
            crossOrigin: l.crossOrigin,
            integrity: l.integrity,
            media: l.media,
            hrefLang: l.hrefLang,
            referrerPolicy: l.referrerPolicy
          }, Ce.set(t, l), u || M0(
            a,
            t,
            l,
            f.state
          ))), e && n === null)
            throw Error(o(528, ""));
          return f;
        }
        if (e && n !== null)
          throw Error(o(529, ""));
        return null;
      case "script":
        return e = l.async, l = l.src, typeof l == "string" && e && typeof e != "function" && typeof e != "symbol" ? (e = kn(l), l = yn(
          a
        ).hoistableScripts, n = l.get(e), n || (n = {
          type: "script",
          instance: null,
          count: 0,
          state: null
        }, l.set(e, n)), n) : { type: "void", instance: null, count: 0, state: null };
      default:
        throw Error(o(444, t));
    }
  }
  function Wn(t) {
    return 'href="' + Ee(t) + '"';
  }
  function Ga(t) {
    return 'link[rel="stylesheet"][' + t + "]";
  }
  function Dm(t) {
    return z({}, t, {
      "data-precedence": t.precedence,
      precedence: null
    });
  }
  function M0(t, e, l, n) {
    t.querySelector('link[rel="preload"][as="style"][' + e + "]") ? n.loading = 1 : (e = t.createElement("link"), n.preload = e, e.addEventListener("load", function() {
      return n.loading |= 1;
    }), e.addEventListener("error", function() {
      return n.loading |= 2;
    }), It(e, "link", l), Vt(e), t.head.appendChild(e));
  }
  function kn(t) {
    return '[src="' + Ee(t) + '"]';
  }
  function Qa(t) {
    return "script[async]" + t;
  }
  function Nm(t, e, l) {
    if (e.count++, e.instance === null)
      switch (e.type) {
        case "style":
          var n = t.querySelector(
            'style[data-href~="' + Ee(l.href) + '"]'
          );
          if (n)
            return e.instance = n, Vt(n), n;
          var a = z({}, l, {
            "data-href": l.href,
            "data-precedence": l.precedence,
            href: null,
            precedence: null
          });
          return n = (t.ownerDocument || t).createElement(
            "style"
          ), Vt(n), It(n, "style", a), vi(n, l.precedence, t), e.instance = n;
        case "stylesheet":
          a = Wn(l.href);
          var u = t.querySelector(
            Ga(a)
          );
          if (u)
            return e.state.loading |= 4, e.instance = u, Vt(u), u;
          n = Dm(l), (a = Ce.get(a)) && uo(n, a), u = (t.ownerDocument || t).createElement("link"), Vt(u);
          var f = u;
          return f._p = new Promise(function(r, y) {
            f.onload = r, f.onerror = y;
          }), It(u, "link", n), e.state.loading |= 4, vi(u, l.precedence, t), e.instance = u;
        case "script":
          return u = kn(l.src), (a = t.querySelector(
            Qa(u)
          )) ? (e.instance = a, Vt(a), a) : (n = l, (a = Ce.get(u)) && (n = z({}, l), io(n, a)), t = t.ownerDocument || t, a = t.createElement("script"), Vt(a), It(a, "link", n), t.head.appendChild(a), e.instance = a);
        case "void":
          return null;
        default:
          throw Error(o(443, e.type));
      }
    else
      e.type === "stylesheet" && (e.state.loading & 4) === 0 && (n = e.instance, e.state.loading |= 4, vi(n, l.precedence, t));
    return e.instance;
  }
  function vi(t, e, l) {
    for (var n = l.querySelectorAll(
      'link[rel="stylesheet"][data-precedence],style[data-precedence]'
    ), a = n.length ? n[n.length - 1] : null, u = a, f = 0; f < n.length; f++) {
      var r = n[f];
      if (r.dataset.precedence === e) u = r;
      else if (u !== a) break;
    }
    u ? u.parentNode.insertBefore(t, u.nextSibling) : (e = l.nodeType === 9 ? l.head : l, e.insertBefore(t, e.firstChild));
  }
  function uo(t, e) {
    t.crossOrigin == null && (t.crossOrigin = e.crossOrigin), t.referrerPolicy == null && (t.referrerPolicy = e.referrerPolicy), t.title == null && (t.title = e.title);
  }
  function io(t, e) {
    t.crossOrigin == null && (t.crossOrigin = e.crossOrigin), t.referrerPolicy == null && (t.referrerPolicy = e.referrerPolicy), t.integrity == null && (t.integrity = e.integrity);
  }
  var hi = null;
  function Um(t, e, l) {
    if (hi === null) {
      var n = /* @__PURE__ */ new Map(), a = hi = /* @__PURE__ */ new Map();
      a.set(l, n);
    } else
      a = hi, n = a.get(l), n || (n = /* @__PURE__ */ new Map(), a.set(l, n));
    if (n.has(t)) return n;
    for (n.set(t, null), l = l.getElementsByTagName(t), a = 0; a < l.length; a++) {
      var u = l[a];
      if (!(u[ua] || u[Wt] || t === "link" && u.getAttribute("rel") === "stylesheet") && u.namespaceURI !== "http://www.w3.org/2000/svg") {
        var f = u.getAttribute(e) || "";
        f = t + f;
        var r = n.get(f);
        r ? r.push(u) : n.set(f, [u]);
      }
    }
    return n;
  }
  function wm(t, e, l) {
    t = t.ownerDocument || t, t.head.insertBefore(
      l,
      e === "title" ? t.querySelector("head > title") : null
    );
  }
  function C0(t, e, l) {
    if (l === 1 || e.itemProp != null) return !1;
    switch (t) {
      case "meta":
      case "title":
        return !0;
      case "style":
        if (typeof e.precedence != "string" || typeof e.href != "string" || e.href === "")
          break;
        return !0;
      case "link":
        if (typeof e.rel != "string" || typeof e.href != "string" || e.href === "" || e.onLoad || e.onError)
          break;
        switch (e.rel) {
          case "stylesheet":
            return t = e.disabled, typeof e.precedence == "string" && t == null;
          default:
            return !0;
        }
      case "script":
        if (e.async && typeof e.async != "function" && typeof e.async != "symbol" && !e.onLoad && !e.onError && e.src && typeof e.src == "string")
          return !0;
    }
    return !1;
  }
  function Hm(t) {
    return !(t.type === "stylesheet" && (t.state.loading & 3) === 0);
  }
  function x0(t, e, l, n) {
    if (l.type === "stylesheet" && (typeof n.media != "string" || matchMedia(n.media).matches !== !1) && (l.state.loading & 4) === 0) {
      if (l.instance === null) {
        var a = Wn(n.href), u = e.querySelector(
          Ga(a)
        );
        if (u) {
          e = u._p, e !== null && typeof e == "object" && typeof e.then == "function" && (t.count++, t = yi.bind(t), e.then(t, t)), l.state.loading |= 4, l.instance = u, Vt(u);
          return;
        }
        u = e.ownerDocument || e, n = Dm(n), (a = Ce.get(a)) && uo(n, a), u = u.createElement("link"), Vt(u);
        var f = u;
        f._p = new Promise(function(r, y) {
          f.onload = r, f.onerror = y;
        }), It(u, "link", n), l.instance = u;
      }
      t.stylesheets === null && (t.stylesheets = /* @__PURE__ */ new Map()), t.stylesheets.set(l, e), (e = l.state.preload) && (l.state.loading & 3) === 0 && (t.count++, l = yi.bind(t), e.addEventListener("load", l), e.addEventListener("error", l));
    }
  }
  var co = 0;
  function D0(t, e) {
    return t.stylesheets && t.count === 0 && bi(t, t.stylesheets), 0 < t.count || 0 < t.imgCount ? function(l) {
      var n = setTimeout(function() {
        if (t.stylesheets && bi(t, t.stylesheets), t.unsuspend) {
          var u = t.unsuspend;
          t.unsuspend = null, u();
        }
      }, 6e4 + e);
      0 < t.imgBytes && co === 0 && (co = 62500 * r0());
      var a = setTimeout(
        function() {
          if (t.waitingForImages = !1, t.count === 0 && (t.stylesheets && bi(t, t.stylesheets), t.unsuspend)) {
            var u = t.unsuspend;
            t.unsuspend = null, u();
          }
        },
        (t.imgBytes > co ? 50 : 800) + e
      );
      return t.unsuspend = l, function() {
        t.unsuspend = null, clearTimeout(n), clearTimeout(a);
      };
    } : null;
  }
  function yi() {
    if (this.count--, this.count === 0 && (this.imgCount === 0 || !this.waitingForImages)) {
      if (this.stylesheets) bi(this, this.stylesheets);
      else if (this.unsuspend) {
        var t = this.unsuspend;
        this.unsuspend = null, t();
      }
    }
  }
  var gi = null;
  function bi(t, e) {
    t.stylesheets = null, t.unsuspend !== null && (t.count++, gi = /* @__PURE__ */ new Map(), e.forEach(N0, t), gi = null, yi.call(t));
  }
  function N0(t, e) {
    if (!(e.state.loading & 4)) {
      var l = gi.get(t);
      if (l) var n = l.get(null);
      else {
        l = /* @__PURE__ */ new Map(), gi.set(t, l);
        for (var a = t.querySelectorAll(
          "link[data-precedence],style[data-precedence]"
        ), u = 0; u < a.length; u++) {
          var f = a[u];
          (f.nodeName === "LINK" || f.getAttribute("media") !== "not all") && (l.set(f.dataset.precedence, f), n = f);
        }
        n && l.set(null, n);
      }
      a = e.instance, f = a.getAttribute("data-precedence"), u = l.get(f) || n, u === n && l.set(null, a), l.set(f, a), this.count++, n = yi.bind(this), a.addEventListener("load", n), a.addEventListener("error", n), u ? u.parentNode.insertBefore(a, u.nextSibling) : (t = t.nodeType === 9 ? t.head : t, t.insertBefore(a, t.firstChild)), e.state.loading |= 4;
    }
  }
  var Za = {
    $$typeof: G,
    Provider: null,
    Consumer: null,
    _currentValue: H,
    _currentValue2: H,
    _threadCount: 0
  };
  function U0(t, e, l, n, a, u, f, r, y) {
    this.tag = 1, this.containerInfo = t, this.pingCache = this.current = this.pendingChildren = null, this.timeoutHandle = -1, this.callbackNode = this.next = this.pendingContext = this.context = this.cancelPendingCommit = null, this.callbackPriority = 0, this.expirationTimes = ec(-1), this.entangledLanes = this.shellSuspendCounter = this.errorRecoveryDisabledLanes = this.expiredLanes = this.warmLanes = this.pingedLanes = this.suspendedLanes = this.pendingLanes = 0, this.entanglements = ec(0), this.hiddenUpdates = ec(null), this.identifierPrefix = n, this.onUncaughtError = a, this.onCaughtError = u, this.onRecoverableError = f, this.pooledCache = null, this.pooledCacheLanes = 0, this.formState = y, this.incompleteTransitions = /* @__PURE__ */ new Map();
  }
  function Bm(t, e, l, n, a, u, f, r, y, A, C, U) {
    return t = new U0(
      t,
      e,
      l,
      f,
      y,
      A,
      C,
      U,
      r
    ), e = 1, u === !0 && (e |= 24), u = de(3, null, null, e), t.current = u, u.stateNode = t, e = Yc(), e.refCount++, t.pooledCache = e, e.refCount++, u.memoizedState = {
      element: n,
      isDehydrated: l,
      cache: e
    }, Qc(u), t;
  }
  function qm(t) {
    return t ? (t = zn, t) : zn;
  }
  function Lm(t, e, l, n, a, u) {
    a = qm(a), n.context === null ? n.context = a : n.pendingContext = a, n = yl(e), n.payload = { element: l }, u = u === void 0 ? null : u, u !== null && (n.callback = u), l = gl(t, n, e), l !== null && (ce(l, t, e), Ta(l, t, e));
  }
  function Ym(t, e) {
    if (t = t.memoizedState, t !== null && t.dehydrated !== null) {
      var l = t.retryLane;
      t.retryLane = l !== 0 && l < e ? l : e;
    }
  }
  function fo(t, e) {
    Ym(t, e), (t = t.alternate) && Ym(t, e);
  }
  function jm(t) {
    if (t.tag === 13 || t.tag === 31) {
      var e = Ql(t, 67108864);
      e !== null && ce(e, t, 67108864), fo(t, 67108864);
    }
  }
  function Xm(t) {
    if (t.tag === 13 || t.tag === 31) {
      var e = ge();
      e = lc(e);
      var l = Ql(t, e);
      l !== null && ce(l, t, e), fo(t, e);
    }
  }
  var Si = !0;
  function w0(t, e, l, n) {
    var a = R.T;
    R.T = null;
    var u = B.p;
    try {
      B.p = 2, oo(t, e, l, n);
    } finally {
      B.p = u, R.T = a;
    }
  }
  function H0(t, e, l, n) {
    var a = R.T;
    R.T = null;
    var u = B.p;
    try {
      B.p = 8, oo(t, e, l, n);
    } finally {
      B.p = u, R.T = a;
    }
  }
  function oo(t, e, l, n) {
    if (Si) {
      var a = so(n);
      if (a === null)
        kf(
          t,
          e,
          n,
          pi,
          l
        ), Qm(t, n);
      else if (q0(
        a,
        t,
        e,
        l,
        n
      ))
        n.stopPropagation();
      else if (Qm(t, n), e & 4 && -1 < B0.indexOf(t)) {
        for (; a !== null; ) {
          var u = hn(a);
          if (u !== null)
            switch (u.tag) {
              case 3:
                if (u = u.stateNode, u.current.memoizedState.isDehydrated) {
                  var f = Ll(u.pendingLanes);
                  if (f !== 0) {
                    var r = u;
                    for (r.pendingLanes |= 2, r.entangledLanes |= 2; f; ) {
                      var y = 1 << 31 - se(f);
                      r.entanglements[1] |= y, f &= ~y;
                    }
                    Be(u), (St & 6) === 0 && (li = fe() + 500, La(0));
                  }
                }
                break;
              case 31:
              case 13:
                r = Ql(u, 2), r !== null && ce(r, u, 2), ai(), fo(u, 2);
            }
          if (u = so(n), u === null && kf(
            t,
            e,
            n,
            pi,
            l
          ), u === a) break;
          a = u;
        }
        a !== null && n.stopPropagation();
      } else
        kf(
          t,
          e,
          n,
          null,
          l
        );
    }
  }
  function so(t) {
    return t = rc(t), ro(t);
  }
  var pi = null;
  function ro(t) {
    if (pi = null, t = vn(t), t !== null) {
      var e = m(t);
      if (e === null) t = null;
      else {
        var l = e.tag;
        if (l === 13) {
          if (t = v(e), t !== null) return t;
          t = null;
        } else if (l === 31) {
          if (t = p(e), t !== null) return t;
          t = null;
        } else if (l === 3) {
          if (e.stateNode.current.memoizedState.isDehydrated)
            return e.tag === 3 ? e.stateNode.containerInfo : null;
          t = null;
        } else e !== t && (t = null);
      }
    }
    return pi = t, null;
  }
  function Gm(t) {
    switch (t) {
      case "beforetoggle":
      case "cancel":
      case "click":
      case "close":
      case "contextmenu":
      case "copy":
      case "cut":
      case "auxclick":
      case "dblclick":
      case "dragend":
      case "dragstart":
      case "drop":
      case "focusin":
      case "focusout":
      case "input":
      case "invalid":
      case "keydown":
      case "keypress":
      case "keyup":
      case "mousedown":
      case "mouseup":
      case "paste":
      case "pause":
      case "play":
      case "pointercancel":
      case "pointerdown":
      case "pointerup":
      case "ratechange":
      case "reset":
      case "resize":
      case "seeked":
      case "submit":
      case "toggle":
      case "touchcancel":
      case "touchend":
      case "touchstart":
      case "volumechange":
      case "change":
      case "selectionchange":
      case "textInput":
      case "compositionstart":
      case "compositionend":
      case "compositionupdate":
      case "beforeblur":
      case "afterblur":
      case "beforeinput":
      case "blur":
      case "fullscreenchange":
      case "focus":
      case "hashchange":
      case "popstate":
      case "select":
      case "selectstart":
        return 2;
      case "drag":
      case "dragenter":
      case "dragexit":
      case "dragleave":
      case "dragover":
      case "mousemove":
      case "mouseout":
      case "mouseover":
      case "pointermove":
      case "pointerout":
      case "pointerover":
      case "scroll":
      case "touchmove":
      case "wheel":
      case "mouseenter":
      case "mouseleave":
      case "pointerenter":
      case "pointerleave":
        return 8;
      case "message":
        switch (Eh()) {
          case Fo:
            return 2;
          case $o:
            return 8;
          case ou:
          case Th:
            return 32;
          case Io:
            return 268435456;
          default:
            return 32;
        }
      default:
        return 32;
    }
  }
  var mo = !1, Ml = null, Cl = null, xl = null, Va = /* @__PURE__ */ new Map(), Ka = /* @__PURE__ */ new Map(), Dl = [], B0 = "mousedown mouseup touchcancel touchend touchstart auxclick dblclick pointercancel pointerdown pointerup dragend dragstart drop compositionend compositionstart keydown keypress keyup input textInput copy cut paste click change contextmenu reset".split(
    " "
  );
  function Qm(t, e) {
    switch (t) {
      case "focusin":
      case "focusout":
        Ml = null;
        break;
      case "dragenter":
      case "dragleave":
        Cl = null;
        break;
      case "mouseover":
      case "mouseout":
        xl = null;
        break;
      case "pointerover":
      case "pointerout":
        Va.delete(e.pointerId);
        break;
      case "gotpointercapture":
      case "lostpointercapture":
        Ka.delete(e.pointerId);
    }
  }
  function Ja(t, e, l, n, a, u) {
    return t === null || t.nativeEvent !== u ? (t = {
      blockedOn: e,
      domEventName: l,
      eventSystemFlags: n,
      nativeEvent: u,
      targetContainers: [a]
    }, e !== null && (e = hn(e), e !== null && jm(e)), t) : (t.eventSystemFlags |= n, e = t.targetContainers, a !== null && e.indexOf(a) === -1 && e.push(a), t);
  }
  function q0(t, e, l, n, a) {
    switch (e) {
      case "focusin":
        return Ml = Ja(
          Ml,
          t,
          e,
          l,
          n,
          a
        ), !0;
      case "dragenter":
        return Cl = Ja(
          Cl,
          t,
          e,
          l,
          n,
          a
        ), !0;
      case "mouseover":
        return xl = Ja(
          xl,
          t,
          e,
          l,
          n,
          a
        ), !0;
      case "pointerover":
        var u = a.pointerId;
        return Va.set(
          u,
          Ja(
            Va.get(u) || null,
            t,
            e,
            l,
            n,
            a
          )
        ), !0;
      case "gotpointercapture":
        return u = a.pointerId, Ka.set(
          u,
          Ja(
            Ka.get(u) || null,
            t,
            e,
            l,
            n,
            a
          )
        ), !0;
    }
    return !1;
  }
  function Zm(t) {
    var e = vn(t.target);
    if (e !== null) {
      var l = m(e);
      if (l !== null) {
        if (e = l.tag, e === 13) {
          if (e = v(l), e !== null) {
            t.blockedOn = e, as(t.priority, function() {
              Xm(l);
            });
            return;
          }
        } else if (e === 31) {
          if (e = p(l), e !== null) {
            t.blockedOn = e, as(t.priority, function() {
              Xm(l);
            });
            return;
          }
        } else if (e === 3 && l.stateNode.current.memoizedState.isDehydrated) {
          t.blockedOn = l.tag === 3 ? l.stateNode.containerInfo : null;
          return;
        }
      }
    }
    t.blockedOn = null;
  }
  function Ei(t) {
    if (t.blockedOn !== null) return !1;
    for (var e = t.targetContainers; 0 < e.length; ) {
      var l = so(t.nativeEvent);
      if (l === null) {
        l = t.nativeEvent;
        var n = new l.constructor(
          l.type,
          l
        );
        sc = n, l.target.dispatchEvent(n), sc = null;
      } else
        return e = hn(l), e !== null && jm(e), t.blockedOn = l, !1;
      e.shift();
    }
    return !0;
  }
  function Vm(t, e, l) {
    Ei(t) && l.delete(e);
  }
  function L0() {
    mo = !1, Ml !== null && Ei(Ml) && (Ml = null), Cl !== null && Ei(Cl) && (Cl = null), xl !== null && Ei(xl) && (xl = null), Va.forEach(Vm), Ka.forEach(Vm);
  }
  function Ti(t, e) {
    t.blockedOn === e && (t.blockedOn = null, mo || (mo = !0, i.unstable_scheduleCallback(
      i.unstable_NormalPriority,
      L0
    )));
  }
  var Ai = null;
  function Km(t) {
    Ai !== t && (Ai = t, i.unstable_scheduleCallback(
      i.unstable_NormalPriority,
      function() {
        Ai === t && (Ai = null);
        for (var e = 0; e < t.length; e += 3) {
          var l = t[e], n = t[e + 1], a = t[e + 2];
          if (typeof n != "function") {
            if (ro(n || l) === null)
              continue;
            break;
          }
          var u = hn(l);
          u !== null && (t.splice(e, 3), e -= 3, sf(
            u,
            {
              pending: !0,
              data: a,
              method: l.method,
              action: n
            },
            n,
            a
          ));
        }
      }
    ));
  }
  function Fn(t) {
    function e(y) {
      return Ti(y, t);
    }
    Ml !== null && Ti(Ml, t), Cl !== null && Ti(Cl, t), xl !== null && Ti(xl, t), Va.forEach(e), Ka.forEach(e);
    for (var l = 0; l < Dl.length; l++) {
      var n = Dl[l];
      n.blockedOn === t && (n.blockedOn = null);
    }
    for (; 0 < Dl.length && (l = Dl[0], l.blockedOn === null); )
      Zm(l), l.blockedOn === null && Dl.shift();
    if (l = (t.ownerDocument || t).$$reactFormReplay, l != null)
      for (n = 0; n < l.length; n += 3) {
        var a = l[n], u = l[n + 1], f = a[ee] || null;
        if (typeof u == "function")
          f || Km(l);
        else if (f) {
          var r = null;
          if (u && u.hasAttribute("formAction")) {
            if (a = u, f = u[ee] || null)
              r = f.formAction;
            else if (ro(a) !== null) continue;
          } else r = f.action;
          typeof r == "function" ? l[n + 1] = r : (l.splice(n, 3), n -= 3), Km(l);
        }
      }
  }
  function Jm() {
    function t(u) {
      u.canIntercept && u.info === "react-transition" && u.intercept({
        handler: function() {
          return new Promise(function(f) {
            return a = f;
          });
        },
        focusReset: "manual",
        scroll: "manual"
      });
    }
    function e() {
      a !== null && (a(), a = null), n || setTimeout(l, 20);
    }
    function l() {
      if (!n && !navigation.transition) {
        var u = navigation.currentEntry;
        u && u.url != null && navigation.navigate(u.url, {
          state: u.getState(),
          info: "react-transition",
          history: "replace"
        });
      }
    }
    if (typeof navigation == "object") {
      var n = !1, a = null;
      return navigation.addEventListener("navigate", t), navigation.addEventListener("navigatesuccess", e), navigation.addEventListener("navigateerror", e), setTimeout(l, 100), function() {
        n = !0, navigation.removeEventListener("navigate", t), navigation.removeEventListener("navigatesuccess", e), navigation.removeEventListener("navigateerror", e), a !== null && (a(), a = null);
      };
    }
  }
  function vo(t) {
    this._internalRoot = t;
  }
  Ri.prototype.render = vo.prototype.render = function(t) {
    var e = this._internalRoot;
    if (e === null) throw Error(o(409));
    var l = e.current, n = ge();
    Lm(l, n, t, e, null, null);
  }, Ri.prototype.unmount = vo.prototype.unmount = function() {
    var t = this._internalRoot;
    if (t !== null) {
      this._internalRoot = null;
      var e = t.containerInfo;
      Lm(t.current, 2, null, t, null, null), ai(), e[mn] = null;
    }
  };
  function Ri(t) {
    this._internalRoot = t;
  }
  Ri.prototype.unstable_scheduleHydration = function(t) {
    if (t) {
      var e = ns();
      t = { blockedOn: null, target: t, priority: e };
      for (var l = 0; l < Dl.length && e !== 0 && e < Dl[l].priority; l++) ;
      Dl.splice(l, 0, t), l === 0 && Zm(t);
    }
  };
  var Wm = c.version;
  if (Wm !== "19.2.4")
    throw Error(
      o(
        527,
        Wm,
        "19.2.4"
      )
    );
  B.findDOMNode = function(t) {
    var e = t._reactInternals;
    if (e === void 0)
      throw typeof t.render == "function" ? Error(o(188)) : (t = Object.keys(t).join(","), Error(o(268, t)));
    return t = h(e), t = t !== null ? M(t) : null, t = t === null ? null : t.stateNode, t;
  };
  var Y0 = {
    bundleType: 0,
    version: "19.2.4",
    rendererPackageName: "react-dom",
    currentDispatcherRef: R,
    reconcilerVersion: "19.2.4"
  };
  if (typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ < "u") {
    var Oi = __REACT_DEVTOOLS_GLOBAL_HOOK__;
    if (!Oi.isDisabled && Oi.supportsFiber)
      try {
        la = Oi.inject(
          Y0
        ), oe = Oi;
      } catch {
      }
  }
  return Fa.createRoot = function(t, e) {
    if (!d(t)) throw Error(o(299));
    var l = !1, n = "", a = ed, u = ld, f = nd;
    return e != null && (e.unstable_strictMode === !0 && (l = !0), e.identifierPrefix !== void 0 && (n = e.identifierPrefix), e.onUncaughtError !== void 0 && (a = e.onUncaughtError), e.onCaughtError !== void 0 && (u = e.onCaughtError), e.onRecoverableError !== void 0 && (f = e.onRecoverableError)), e = Bm(
      t,
      1,
      !1,
      null,
      null,
      l,
      n,
      null,
      a,
      u,
      f,
      Jm
    ), t[mn] = e.current, Wf(t), new vo(e);
  }, Fa.hydrateRoot = function(t, e, l) {
    if (!d(t)) throw Error(o(299));
    var n = !1, a = "", u = ed, f = ld, r = nd, y = null;
    return l != null && (l.unstable_strictMode === !0 && (n = !0), l.identifierPrefix !== void 0 && (a = l.identifierPrefix), l.onUncaughtError !== void 0 && (u = l.onUncaughtError), l.onCaughtError !== void 0 && (f = l.onCaughtError), l.onRecoverableError !== void 0 && (r = l.onRecoverableError), l.formState !== void 0 && (y = l.formState)), e = Bm(
      t,
      1,
      !0,
      e,
      l ?? null,
      n,
      a,
      y,
      u,
      f,
      r,
      Jm
    ), e.context = qm(null), l = e.current, n = ge(), n = lc(n), a = yl(n), a.callback = null, gl(l, a, n), l = n, e.current.lanes = l, aa(e, l), Be(e), t[mn] = e.current, Wf(t), new Ri(e);
  }, Fa.version = "19.2.4", Fa;
}
var uv;
function dg() {
  if (uv) return go.exports;
  uv = 1;
  function i() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(i);
      } catch (c) {
        console.error(c);
      }
  }
  return i(), go.exports = rg(), go.exports;
}
var Gv = dg(), Gi = Xv();
const Tb = /* @__PURE__ */ Uv(Gi);
class Qv {
  constructor(c) {
    this._subscribers = /* @__PURE__ */ new Set(), this.getSnapshot = () => this._state, this.subscribeStore = (s) => (this._subscribers.add(s), () => this._subscribers.delete(s)), this._state = { ...c };
  }
  replaceState(c) {
    this._state = { ...c }, this._notify();
  }
  applyPatch(c) {
    this._state = { ...this._state, ...c }, this._notify();
  }
  _notify() {
    for (const c of this._subscribers)
      c();
  }
}
const rn = q.createContext(null), qe = /* @__PURE__ */ new Map();
let Zv = "", iv = !1;
function cn() {
  return Zv + "/";
}
function Go(i, c, s, o, d) {
  d !== void 0 && (Zv = d);
  const m = o ?? "";
  iv ? d !== void 0 && Im(cn() + "react-api/events?windowName=" + encodeURIComponent(m)) : (iv = !0, Q0(cn()), Im(cn() + "react-api/events?windowName=" + encodeURIComponent(m))), Z0(m);
  const v = document.getElementById(i);
  if (!v) {
    console.error("[TLReact] Mount point not found:", i);
    return;
  }
  const p = Nv(c);
  if (!p) {
    console.error("[TLReact] Component not registered:", c);
    return;
  }
  Ui(i);
  const S = new Qv(s);
  s.hidden === !0 && (v.style.display = "none");
  const h = (N) => {
    S.applyPatch(N);
  };
  Xo(i, h);
  const M = Gv.createRoot(v);
  qe.set(i, { root: M, store: S, sseListener: h }), Vv = m;
  const z = () => {
    const N = q.useSyncExternalStore(S.subscribeStore, S.getSnapshot);
    return q.useLayoutEffect(() => {
      v.style.display = N.hidden === !0 ? "none" : "";
    }, [N.hidden]), an.createElement(
      rn.Provider,
      { value: { controlId: i, windowName: m, store: S } },
      an.createElement(p, { controlId: i, state: N })
    );
  };
  Gi.flushSync(() => {
    M.render(an.createElement(z));
  });
}
function mg(i, c, s) {
  Go(i, c, s);
}
function Ui(i) {
  const c = qe.get(i);
  c && (Yv(i, c.sseListener), c.root && c.root.unmount(), qe.delete(i));
}
function vg(i) {
  return qe.has(i);
}
function hg(i, c) {
  let s = qe.get(i);
  if (!s) {
    const d = new Qv(c), m = (v) => {
      d.applyPatch(v);
    };
    Xo(i, m), s = { root: null, store: d, sseListener: m }, qe.set(i, s);
  }
  return { controlId: i, windowName: Vv, store: s.store };
}
let Vv = "";
function yg() {
  const i = q.useContext(rn);
  if (!i)
    throw new Error("useTLState must be used inside a TLReact-mounted component.");
  return q.useSyncExternalStore(i.store.subscribeStore, i.store.getSnapshot);
}
function gg() {
  const i = q.useContext(rn);
  if (!i)
    throw new Error("useTLCommand must be used inside a TLReact-mounted component.");
  const c = i.controlId, s = i.windowName;
  return q.useCallback(
    async (o, d) => {
      const m = JSON.stringify({
        controlId: c,
        command: o,
        windowName: s,
        arguments: d ?? {}
      });
      try {
        const v = await fetch(cn() + "react-api/command", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: m
        });
        v.ok || console.error("[TLReact] Command failed:", v.status, await v.text());
      } catch (v) {
        console.error("[TLReact] Command error:", v);
      }
    },
    [c, s]
  );
}
function Ab() {
  const i = q.useContext(rn);
  if (!i)
    throw new Error("useTLUpload must be used inside a TLReact-mounted component.");
  const c = i.controlId, s = i.windowName;
  return q.useCallback(
    async (o) => {
      o.append("controlId", c), o.append("windowName", s);
      try {
        const d = await fetch(cn() + "react-api/upload", {
          method: "POST",
          body: o
        });
        d.ok || console.error("[TLReact] Upload failed:", d.status, await d.text());
      } catch (d) {
        console.error("[TLReact] Upload error:", d);
      }
    },
    [c, s]
  );
}
function Rb() {
  const i = q.useContext(rn);
  if (!i)
    throw new Error("useTLDataUrl must be used inside a TLReact-mounted component.");
  return cn() + "react-api/data?controlId=" + encodeURIComponent(i.controlId) + "&windowName=" + encodeURIComponent(i.windowName);
}
function Ob() {
  const i = yg(), c = gg(), s = q.useContext(rn), o = q.useCallback(
    (d) => {
      s == null || s.store.applyPatch({ value: d }), c("valueChanged", { value: d });
    },
    [c, s]
  );
  return [i.value, o];
}
function wi(i = document.body) {
  const c = document.body.dataset.windowName, s = document.body.dataset.contextPath, o = i.querySelectorAll("[data-react-module]");
  for (const d of o) {
    if (!d.id || qe.has(d.id))
      continue;
    const m = d.dataset.reactModule, v = c ?? d.dataset.windowName, p = s ?? d.dataset.contextPath;
    if (!m || v === void 0 || p === void 0)
      continue;
    const S = d.dataset.reactState, h = S ? JSON.parse(S) : {};
    Go(d.id, m, h, v, p);
  }
}
function cv() {
  new MutationObserver((c) => {
    var s;
    for (const o of c)
      for (const d of o.removedNodes)
        if (d instanceof HTMLElement) {
          const m = d.id;
          m && qe.has(m) && qe.get(m).root !== null && Ui(m);
          for (const [v, p] of qe.entries())
            p.root !== null && d.querySelector("#" + CSS.escape(v)) && Ui(v);
        }
    for (const o of c)
      for (const d of o.addedNodes)
        d instanceof HTMLElement && ((s = d.dataset) != null && s.reactModule ? wi(d.parentElement ?? document.body) : d.querySelector("[data-react-module]") && wi(d));
  }).observe(document.body, { childList: !0, subtree: !0 });
}
document.readyState === "loading" ? document.addEventListener("DOMContentLoaded", cv) : cv();
window.addEventListener("load", () => wi());
var Eo = { exports: {} }, $a = {};
/**
 * @license React
 * react-jsx-runtime.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var fv;
function bg() {
  if (fv) return $a;
  fv = 1;
  var i = Symbol.for("react.transitional.element"), c = Symbol.for("react.fragment");
  function s(o, d, m) {
    var v = null;
    if (m !== void 0 && (v = "" + m), d.key !== void 0 && (v = "" + d.key), "key" in d) {
      m = {};
      for (var p in d)
        p !== "key" && (m[p] = d[p]);
    } else m = d;
    return d = m.ref, {
      $$typeof: i,
      type: o,
      key: v,
      ref: d !== void 0 ? d : null,
      props: m
    };
  }
  return $a.Fragment = c, $a.jsx = s, $a.jsxs = s, $a;
}
var ov;
function Sg() {
  return ov || (ov = 1, Eo.exports = bg()), Eo.exports;
}
var Zt = Sg();
const zb = ({ control: i }) => {
  const c = i, s = Nv(c.module), o = q.useMemo(
    () => hg(c.controlId, c.state),
    [c.controlId]
  );
  q.useEffect(() => () => Ui(c.controlId), [c.controlId]);
  const d = q.useSyncExternalStore(o.store.subscribeStore, o.store.getSnapshot);
  return s ? /* @__PURE__ */ Zt.jsx(rn.Provider, { value: o, children: /* @__PURE__ */ Zt.jsx(s, { controlId: c.controlId, state: d }) }) : /* @__PURE__ */ Zt.jsxs("span", { children: [
    "[Component not registered: ",
    c.module,
    "]"
  ] });
};
function Qi() {
  return typeof window < "u";
}
function ta(i) {
  return Qo(i) ? (i.nodeName || "").toLowerCase() : "#document";
}
function be(i) {
  var c;
  return (i == null || (c = i.ownerDocument) == null ? void 0 : c.defaultView) || window;
}
function Ye(i) {
  var c;
  return (c = (Qo(i) ? i.ownerDocument : i.document) || window.document) == null ? void 0 : c.documentElement;
}
function Qo(i) {
  return Qi() ? i instanceof Node || i instanceof be(i).Node : !1;
}
function Lt(i) {
  return Qi() ? i instanceof Element || i instanceof be(i).Element : !1;
}
function je(i) {
  return Qi() ? i instanceof HTMLElement || i instanceof be(i).HTMLElement : !1;
}
function xo(i) {
  return !Qi() || typeof ShadowRoot > "u" ? !1 : i instanceof ShadowRoot || i instanceof be(i).ShadowRoot;
}
function uu(i) {
  const {
    overflow: c,
    overflowX: s,
    overflowY: o,
    display: d
  } = Se(i);
  return /auto|scroll|overlay|hidden|clip/.test(c + o + s) && d !== "inline" && d !== "contents";
}
function pg(i) {
  return /^(table|td|th)$/.test(ta(i));
}
function Zi(i) {
  try {
    if (i.matches(":popover-open"))
      return !0;
  } catch {
  }
  try {
    return i.matches(":modal");
  } catch {
    return !1;
  }
}
const Eg = /transform|translate|scale|rotate|perspective|filter/, Tg = /paint|layout|strict|content/, ln = (i) => !!i && i !== "none";
let To;
function Zo(i) {
  const c = Lt(i) ? Se(i) : i;
  return ln(c.transform) || ln(c.translate) || ln(c.scale) || ln(c.rotate) || ln(c.perspective) || !Vi() && (ln(c.backdropFilter) || ln(c.filter)) || Eg.test(c.willChange || "") || Tg.test(c.contain || "");
}
function Ag(i) {
  let c = cl(i);
  for (; je(c) && !il(c); ) {
    if (Zo(c))
      return c;
    if (Zi(c))
      return null;
    c = cl(c);
  }
  return null;
}
function Vi() {
  return To == null && (To = typeof CSS < "u" && CSS.supports && CSS.supports("-webkit-backdrop-filter", "none")), To;
}
function il(i) {
  return /^(html|body|#document)$/.test(ta(i));
}
function Se(i) {
  return be(i).getComputedStyle(i);
}
function Ki(i) {
  return Lt(i) ? {
    scrollLeft: i.scrollLeft,
    scrollTop: i.scrollTop
  } : {
    scrollLeft: i.scrollX,
    scrollTop: i.scrollY
  };
}
function cl(i) {
  if (ta(i) === "html")
    return i;
  const c = (
    // Step into the shadow DOM of the parent of a slotted node.
    i.assignedSlot || // DOM Element detected.
    i.parentNode || // ShadowRoot detected.
    xo(i) && i.host || // Fallback.
    Ye(i)
  );
  return xo(c) ? c.host : c;
}
function Kv(i) {
  const c = cl(i);
  return il(c) ? i.ownerDocument ? i.ownerDocument.body : i.body : je(c) && uu(c) ? c : Kv(c);
}
function Hl(i, c, s) {
  var o;
  c === void 0 && (c = []), s === void 0 && (s = !0);
  const d = Kv(i), m = d === ((o = i.ownerDocument) == null ? void 0 : o.body), v = be(d);
  if (m) {
    const p = Do(v);
    return c.concat(v, v.visualViewport || [], uu(d) ? d : [], p && s ? Hl(p) : []);
  } else
    return c.concat(d, Hl(d, [], s));
}
function Do(i) {
  return i.parent && Object.getPrototypeOf(i.parent) ? i.frameElement : null;
}
const Pn = Math.min, fn = Math.max, Hi = Math.round, zi = Math.floor, Le = (i) => ({
  x: i,
  y: i
}), Rg = {
  left: "right",
  right: "left",
  bottom: "top",
  top: "bottom"
};
function No(i, c, s) {
  return fn(i, Pn(c, s));
}
function iu(i, c) {
  return typeof i == "function" ? i(c) : i;
}
function on(i) {
  return i.split("-")[0];
}
function cu(i) {
  return i.split("-")[1];
}
function Jv(i) {
  return i === "x" ? "y" : "x";
}
function Vo(i) {
  return i === "y" ? "height" : "width";
}
function wl(i) {
  const c = i[0];
  return c === "t" || c === "b" ? "y" : "x";
}
function Ko(i) {
  return Jv(wl(i));
}
function Og(i, c, s) {
  s === void 0 && (s = !1);
  const o = cu(i), d = Ko(i), m = Vo(d);
  let v = d === "x" ? o === (s ? "end" : "start") ? "right" : "left" : o === "start" ? "bottom" : "top";
  return c.reference[m] > c.floating[m] && (v = Bi(v)), [v, Bi(v)];
}
function zg(i) {
  const c = Bi(i);
  return [Uo(i), c, Uo(c)];
}
function Uo(i) {
  return i.includes("start") ? i.replace("start", "end") : i.replace("end", "start");
}
const sv = ["left", "right"], rv = ["right", "left"], _g = ["top", "bottom"], Mg = ["bottom", "top"];
function Cg(i, c, s) {
  switch (i) {
    case "top":
    case "bottom":
      return s ? c ? rv : sv : c ? sv : rv;
    case "left":
    case "right":
      return c ? _g : Mg;
    default:
      return [];
  }
}
function xg(i, c, s, o) {
  const d = cu(i);
  let m = Cg(on(i), s === "start", o);
  return d && (m = m.map((v) => v + "-" + d), c && (m = m.concat(m.map(Uo)))), m;
}
function Bi(i) {
  const c = on(i);
  return Rg[c] + i.slice(c.length);
}
function Dg(i) {
  return {
    top: 0,
    right: 0,
    bottom: 0,
    left: 0,
    ...i
  };
}
function Wv(i) {
  return typeof i != "number" ? Dg(i) : {
    top: i,
    right: i,
    bottom: i,
    left: i
  };
}
function qi(i) {
  const {
    x: c,
    y: s,
    width: o,
    height: d
  } = i;
  return {
    width: o,
    height: d,
    top: s,
    left: c,
    right: c + o,
    bottom: s + d,
    x: c,
    y: s
  };
}
/*!
* tabbable 6.4.0
* @license MIT, https://github.com/focus-trap/tabbable/blob/master/LICENSE
*/
var Ng = ["input:not([inert]):not([inert] *)", "select:not([inert]):not([inert] *)", "textarea:not([inert]):not([inert] *)", "a[href]:not([inert]):not([inert] *)", "button:not([inert]):not([inert] *)", "[tabindex]:not(slot):not([inert]):not([inert] *)", "audio[controls]:not([inert]):not([inert] *)", "video[controls]:not([inert]):not([inert] *)", '[contenteditable]:not([contenteditable="false"]):not([inert]):not([inert] *)', "details>summary:first-of-type:not([inert]):not([inert] *)", "details:not([inert]):not([inert] *)"], wo = /* @__PURE__ */ Ng.join(","), kv = typeof Element > "u", nu = kv ? function() {
} : Element.prototype.matches || Element.prototype.msMatchesSelector || Element.prototype.webkitMatchesSelector, Li = !kv && Element.prototype.getRootNode ? function(i) {
  var c;
  return i == null || (c = i.getRootNode) === null || c === void 0 ? void 0 : c.call(i);
} : function(i) {
  return i == null ? void 0 : i.ownerDocument;
}, Yi = function(c, s) {
  var o;
  s === void 0 && (s = !0);
  var d = c == null || (o = c.getAttribute) === null || o === void 0 ? void 0 : o.call(c, "inert"), m = d === "" || d === "true", v = m || s && c && // closest does not exist on shadow roots, so we fall back to a manual
  // lookup upward, in case it is not defined.
  (typeof c.closest == "function" ? c.closest("[inert]") : Yi(c.parentNode));
  return v;
}, Ug = function(c) {
  var s, o = c == null || (s = c.getAttribute) === null || s === void 0 ? void 0 : s.call(c, "contenteditable");
  return o === "" || o === "true";
}, wg = function(c, s, o) {
  if (Yi(c))
    return [];
  var d = Array.prototype.slice.apply(c.querySelectorAll(wo));
  return s && nu.call(c, wo) && d.unshift(c), d = d.filter(o), d;
}, Ho = function(c, s, o) {
  for (var d = [], m = Array.from(c); m.length; ) {
    var v = m.shift();
    if (!Yi(v, !1))
      if (v.tagName === "SLOT") {
        var p = v.assignedElements(), S = p.length ? p : v.children, h = Ho(S, !0, o);
        o.flatten ? d.push.apply(d, h) : d.push({
          scopeParent: v,
          candidates: h
        });
      } else {
        var M = nu.call(v, wo);
        M && o.filter(v) && (s || !c.includes(v)) && d.push(v);
        var z = v.shadowRoot || // check for an undisclosed shadow
        typeof o.getShadowRoot == "function" && o.getShadowRoot(v), N = !Yi(z, !1) && (!o.shadowRootFilter || o.shadowRootFilter(v));
        if (z && N) {
          var D = Ho(z === !0 ? v.children : z.children, !0, o);
          o.flatten ? d.push.apply(d, D) : d.push({
            scopeParent: v,
            candidates: D
          });
        } else
          m.unshift.apply(m, v.children);
      }
  }
  return d;
}, Fv = function(c) {
  return !isNaN(parseInt(c.getAttribute("tabindex"), 10));
}, $v = function(c) {
  if (!c)
    throw new Error("No node provided");
  return c.tabIndex < 0 && (/^(AUDIO|VIDEO|DETAILS)$/.test(c.tagName) || Ug(c)) && !Fv(c) ? 0 : c.tabIndex;
}, Hg = function(c, s) {
  var o = $v(c);
  return o < 0 && s && !Fv(c) ? 0 : o;
}, Bg = function(c, s) {
  return c.tabIndex === s.tabIndex ? c.documentOrder - s.documentOrder : c.tabIndex - s.tabIndex;
}, Iv = function(c) {
  return c.tagName === "INPUT";
}, qg = function(c) {
  return Iv(c) && c.type === "hidden";
}, Lg = function(c) {
  var s = c.tagName === "DETAILS" && Array.prototype.slice.apply(c.children).some(function(o) {
    return o.tagName === "SUMMARY";
  });
  return s;
}, Yg = function(c, s) {
  for (var o = 0; o < c.length; o++)
    if (c[o].checked && c[o].form === s)
      return c[o];
}, jg = function(c) {
  if (!c.name)
    return !0;
  var s = c.form || Li(c), o = function(p) {
    return s.querySelectorAll('input[type="radio"][name="' + p + '"]');
  }, d;
  if (typeof window < "u" && typeof window.CSS < "u" && typeof window.CSS.escape == "function")
    d = o(window.CSS.escape(c.name));
  else
    try {
      d = o(c.name);
    } catch (v) {
      return console.error("Looks like you have a radio button with a name attribute containing invalid CSS selector characters and need the CSS.escape polyfill: %s", v.message), !1;
    }
  var m = Yg(d, c.form);
  return !m || m === c;
}, Xg = function(c) {
  return Iv(c) && c.type === "radio";
}, Gg = function(c) {
  return Xg(c) && !jg(c);
}, Qg = function(c) {
  var s, o = c && Li(c), d = (s = o) === null || s === void 0 ? void 0 : s.host, m = !1;
  if (o && o !== c) {
    var v, p, S;
    for (m = !!((v = d) !== null && v !== void 0 && (p = v.ownerDocument) !== null && p !== void 0 && p.contains(d) || c != null && (S = c.ownerDocument) !== null && S !== void 0 && S.contains(c)); !m && d; ) {
      var h, M, z;
      o = Li(d), d = (h = o) === null || h === void 0 ? void 0 : h.host, m = !!((M = d) !== null && M !== void 0 && (z = M.ownerDocument) !== null && z !== void 0 && z.contains(d));
    }
  }
  return m;
}, dv = function(c) {
  var s = c.getBoundingClientRect(), o = s.width, d = s.height;
  return o === 0 && d === 0;
}, Zg = function(c, s) {
  var o = s.displayCheck, d = s.getShadowRoot;
  if (o === "full-native" && "checkVisibility" in c) {
    var m = c.checkVisibility({
      // Checking opacity might be desirable for some use cases, but natively,
      // opacity zero elements _are_ focusable and tabbable.
      checkOpacity: !1,
      opacityProperty: !1,
      contentVisibilityAuto: !0,
      visibilityProperty: !0,
      // This is an alias for `visibilityProperty`. Contemporary browsers
      // support both. However, this alias has wider browser support (Chrome
      // >= 105 and Firefox >= 106, vs. Chrome >= 121 and Firefox >= 122), so
      // we include it anyway.
      checkVisibilityCSS: !0
    });
    return !m;
  }
  if (getComputedStyle(c).visibility === "hidden")
    return !0;
  var v = nu.call(c, "details>summary:first-of-type"), p = v ? c.parentElement : c;
  if (nu.call(p, "details:not([open]) *"))
    return !0;
  if (!o || o === "full" || // full-native can run this branch when it falls through in case
  // Element#checkVisibility is unsupported
  o === "full-native" || o === "legacy-full") {
    if (typeof d == "function") {
      for (var S = c; c; ) {
        var h = c.parentElement, M = Li(c);
        if (h && !h.shadowRoot && d(h) === !0)
          return dv(c);
        c.assignedSlot ? c = c.assignedSlot : !h && M !== c.ownerDocument ? c = M.host : c = h;
      }
      c = S;
    }
    if (Qg(c))
      return !c.getClientRects().length;
    if (o !== "legacy-full")
      return !0;
  } else if (o === "non-zero-area")
    return dv(c);
  return !1;
}, Vg = function(c) {
  if (/^(INPUT|BUTTON|SELECT|TEXTAREA)$/.test(c.tagName))
    for (var s = c.parentElement; s; ) {
      if (s.tagName === "FIELDSET" && s.disabled) {
        for (var o = 0; o < s.children.length; o++) {
          var d = s.children.item(o);
          if (d.tagName === "LEGEND")
            return nu.call(s, "fieldset[disabled] *") ? !0 : !d.contains(c);
        }
        return !0;
      }
      s = s.parentElement;
    }
  return !1;
}, Kg = function(c, s) {
  return !(s.disabled || qg(s) || Zg(s, c) || // For a details element with a summary, the summary element gets the focus
  Lg(s) || Vg(s));
}, mv = function(c, s) {
  return !(Gg(s) || $v(s) < 0 || !Kg(c, s));
}, Jg = function(c) {
  var s = parseInt(c.getAttribute("tabindex"), 10);
  return !!(isNaN(s) || s >= 0);
}, Pv = function(c) {
  var s = [], o = [];
  return c.forEach(function(d, m) {
    var v = !!d.scopeParent, p = v ? d.scopeParent : d, S = Hg(p, v), h = v ? Pv(d.candidates) : p;
    S === 0 ? v ? s.push.apply(s, h) : s.push(p) : o.push({
      documentOrder: m,
      tabIndex: S,
      item: d,
      isScope: v,
      content: h
    });
  }), o.sort(Bg).reduce(function(d, m) {
    return m.isScope ? d.push.apply(d, m.content) : d.push(m.content), d;
  }, []).concat(s);
}, th = function(c, s) {
  s = s || {};
  var o;
  return s.getShadowRoot ? o = Ho([c], s.includeContainer, {
    filter: mv.bind(null, s),
    flatten: !1,
    getShadowRoot: s.getShadowRoot,
    shadowRootFilter: Jg
  }) : o = wg(c, s.includeContainer, mv.bind(null, s)), Pv(o);
};
function Wg() {
  return /apple/i.test(navigator.vendor);
}
const vv = "data-floating-ui-focusable";
function kg(i) {
  let c = i.activeElement;
  for (; ((s = c) == null || (s = s.shadowRoot) == null ? void 0 : s.activeElement) != null; ) {
    var s;
    c = c.shadowRoot.activeElement;
  }
  return c;
}
function Bo(i, c) {
  if (!i || !c)
    return !1;
  const s = c.getRootNode == null ? void 0 : c.getRootNode();
  if (i.contains(c))
    return !0;
  if (s && xo(s)) {
    let o = c;
    for (; o; ) {
      if (i === o)
        return !0;
      o = o.parentNode || o.host;
    }
  }
  return !1;
}
function Ia(i) {
  return "composedPath" in i ? i.composedPath()[0] : i.target;
}
function Ao(i, c) {
  if (c == null)
    return !1;
  if ("composedPath" in i)
    return i.composedPath().includes(c);
  const s = i;
  return s.target != null && c.contains(s.target);
}
function Fg(i) {
  return i.matches("html,body");
}
function au(i) {
  return (i == null ? void 0 : i.ownerDocument) || document;
}
function $g(i) {
  return i ? i.hasAttribute(vv) ? i : i.querySelector("[" + vv + "]") || i : null;
}
function Ci(i, c, s) {
  return s === void 0 && (s = !0), i.filter((d) => {
    var m;
    return d.parentId === c && (!s || ((m = d.context) == null ? void 0 : m.open));
  }).flatMap((d) => [d, ...Ci(i, d.id, s)]);
}
function Ig(i) {
  return "nativeEvent" in i;
}
var Pg = typeof document < "u", t1 = function() {
}, Bl = Pg ? q.useLayoutEffect : t1;
const e1 = {
  ...wv
}, l1 = e1.useInsertionEffect, n1 = l1 || ((i) => i());
function $n(i) {
  const c = q.useRef(() => {
  });
  return n1(() => {
    c.current = i;
  }), q.useCallback(function() {
    for (var s = arguments.length, o = new Array(s), d = 0; d < s; d++)
      o[d] = arguments[d];
    return c.current == null ? void 0 : c.current(...o);
  }, []);
}
const eh = () => ({
  getShadowRoot: !0,
  displayCheck: (
    // JSDOM does not support the `tabbable` library. To solve this we can
    // check if `ResizeObserver` is a real function (not polyfilled), which
    // determines if the current environment is JSDOM-like.
    typeof ResizeObserver == "function" && ResizeObserver.toString().includes("[native code]") ? "full" : "none"
  )
});
function lh(i, c) {
  const s = th(i, eh()), o = s.length;
  if (o === 0) return;
  const d = kg(au(i)), m = s.indexOf(d), v = m === -1 ? c === 1 ? 0 : o - 1 : m + c;
  return s[v];
}
function a1(i) {
  return lh(au(i).body, 1) || i;
}
function u1(i) {
  return lh(au(i).body, -1) || i;
}
function Ro(i, c) {
  const s = c || i.currentTarget, o = i.relatedTarget;
  return !o || !Bo(s, o);
}
function i1(i) {
  th(i, eh()).forEach((s) => {
    s.dataset.tabindex = s.getAttribute("tabindex") || "", s.setAttribute("tabindex", "-1");
  });
}
function hv(i) {
  i.querySelectorAll("[data-tabindex]").forEach((s) => {
    const o = s.dataset.tabindex;
    delete s.dataset.tabindex, o ? s.setAttribute("tabindex", o) : s.removeAttribute("tabindex");
  });
}
function yv(i, c, s) {
  let {
    reference: o,
    floating: d
  } = i;
  const m = wl(c), v = Ko(c), p = Vo(v), S = on(c), h = m === "y", M = o.x + o.width / 2 - d.width / 2, z = o.y + o.height / 2 - d.height / 2, N = o[p] / 2 - d[p] / 2;
  let D;
  switch (S) {
    case "top":
      D = {
        x: M,
        y: o.y - d.height
      };
      break;
    case "bottom":
      D = {
        x: M,
        y: o.y + o.height
      };
      break;
    case "right":
      D = {
        x: o.x + o.width,
        y: z
      };
      break;
    case "left":
      D = {
        x: o.x - d.width,
        y: z
      };
      break;
    default:
      D = {
        x: o.x,
        y: o.y
      };
  }
  switch (cu(c)) {
    case "start":
      D[v] -= N * (s && h ? -1 : 1);
      break;
    case "end":
      D[v] += N * (s && h ? -1 : 1);
      break;
  }
  return D;
}
async function c1(i, c) {
  var s;
  c === void 0 && (c = {});
  const {
    x: o,
    y: d,
    platform: m,
    rects: v,
    elements: p,
    strategy: S
  } = i, {
    boundary: h = "clippingAncestors",
    rootBoundary: M = "viewport",
    elementContext: z = "floating",
    altBoundary: N = !1,
    padding: D = 0
  } = iu(c, i), Y = Wv(D), Z = p[N ? z === "floating" ? "reference" : "floating" : z], X = qi(await m.getClippingRect({
    element: (s = await (m.isElement == null ? void 0 : m.isElement(Z))) == null || s ? Z : Z.contextElement || await (m.getDocumentElement == null ? void 0 : m.getDocumentElement(p.floating)),
    boundary: h,
    rootBoundary: M,
    strategy: S
  })), W = z === "floating" ? {
    x: o,
    y: d,
    width: v.floating.width,
    height: v.floating.height
  } : v.reference, G = await (m.getOffsetParent == null ? void 0 : m.getOffsetParent(p.floating)), $ = await (m.isElement == null ? void 0 : m.isElement(G)) ? await (m.getScale == null ? void 0 : m.getScale(G)) || {
    x: 1,
    y: 1
  } : {
    x: 1,
    y: 1
  }, at = qi(m.convertOffsetParentRelativeRectToViewportRelativeRect ? await m.convertOffsetParentRelativeRectToViewportRelativeRect({
    elements: p,
    rect: W,
    offsetParent: G,
    strategy: S
  }) : W);
  return {
    top: (X.top - at.top + Y.top) / $.y,
    bottom: (at.bottom - X.bottom + Y.bottom) / $.y,
    left: (X.left - at.left + Y.left) / $.x,
    right: (at.right - X.right + Y.right) / $.x
  };
}
const f1 = 50, o1 = async (i, c, s) => {
  const {
    placement: o = "bottom",
    strategy: d = "absolute",
    middleware: m = [],
    platform: v
  } = s, p = v.detectOverflow ? v : {
    ...v,
    detectOverflow: c1
  }, S = await (v.isRTL == null ? void 0 : v.isRTL(c));
  let h = await v.getElementRects({
    reference: i,
    floating: c,
    strategy: d
  }), {
    x: M,
    y: z
  } = yv(h, o, S), N = o, D = 0;
  const Y = {};
  for (let L = 0; L < m.length; L++) {
    const Z = m[L];
    if (!Z)
      continue;
    const {
      name: X,
      fn: W
    } = Z, {
      x: G,
      y: $,
      data: at,
      reset: lt
    } = await W({
      x: M,
      y: z,
      initialPlacement: o,
      placement: N,
      strategy: d,
      middlewareData: Y,
      rects: h,
      platform: p,
      elements: {
        reference: i,
        floating: c
      }
    });
    M = G ?? M, z = $ ?? z, Y[X] = {
      ...Y[X],
      ...at
    }, lt && D < f1 && (D++, typeof lt == "object" && (lt.placement && (N = lt.placement), lt.rects && (h = lt.rects === !0 ? await v.getElementRects({
      reference: i,
      floating: c,
      strategy: d
    }) : lt.rects), {
      x: M,
      y: z
    } = yv(h, N, S)), L = -1);
  }
  return {
    x: M,
    y: z,
    placement: N,
    strategy: d,
    middlewareData: Y
  };
}, s1 = (i) => ({
  name: "arrow",
  options: i,
  async fn(c) {
    const {
      x: s,
      y: o,
      placement: d,
      rects: m,
      platform: v,
      elements: p,
      middlewareData: S
    } = c, {
      element: h,
      padding: M = 0
    } = iu(i, c) || {};
    if (h == null)
      return {};
    const z = Wv(M), N = {
      x: s,
      y: o
    }, D = Ko(d), Y = Vo(D), L = await v.getDimensions(h), Z = D === "y", X = Z ? "top" : "left", W = Z ? "bottom" : "right", G = Z ? "clientHeight" : "clientWidth", $ = m.reference[Y] + m.reference[D] - N[D] - m.floating[Y], at = N[D] - m.reference[D], lt = await (v.getOffsetParent == null ? void 0 : v.getOffsetParent(h));
    let V = lt ? lt[G] : 0;
    (!V || !await (v.isElement == null ? void 0 : v.isElement(lt))) && (V = p.floating[G] || m.floating[Y]);
    const it = $ / 2 - at / 2, zt = V / 2 - L[Y] / 2 - 1, Tt = Pn(z[X], zt), _t = Pn(z[W], zt), dt = Tt, Ct = V - L[Y] - _t, Q = V / 2 - L[Y] / 2 + it, ft = No(dt, Q, Ct), R = !S.arrow && cu(d) != null && Q !== ft && m.reference[Y] / 2 - (Q < dt ? Tt : _t) - L[Y] / 2 < 0, B = R ? Q < dt ? Q - dt : Q - Ct : 0;
    return {
      [D]: N[D] + B,
      data: {
        [D]: ft,
        centerOffset: Q - ft - B,
        ...R && {
          alignmentOffset: B
        }
      },
      reset: R
    };
  }
}), r1 = function(i) {
  return i === void 0 && (i = {}), {
    name: "flip",
    options: i,
    async fn(c) {
      var s, o;
      const {
        placement: d,
        middlewareData: m,
        rects: v,
        initialPlacement: p,
        platform: S,
        elements: h
      } = c, {
        mainAxis: M = !0,
        crossAxis: z = !0,
        fallbackPlacements: N,
        fallbackStrategy: D = "bestFit",
        fallbackAxisSideDirection: Y = "none",
        flipAlignment: L = !0,
        ...Z
      } = iu(i, c);
      if ((s = m.arrow) != null && s.alignmentOffset)
        return {};
      const X = on(d), W = wl(p), G = on(p) === p, $ = await (S.isRTL == null ? void 0 : S.isRTL(h.floating)), at = N || (G || !L ? [Bi(p)] : zg(p)), lt = Y !== "none";
      !N && lt && at.push(...xg(p, L, Y, $));
      const V = [p, ...at], it = await S.detectOverflow(c, Z), zt = [];
      let Tt = ((o = m.flip) == null ? void 0 : o.overflows) || [];
      if (M && zt.push(it[X]), z) {
        const Q = Og(d, v, $);
        zt.push(it[Q[0]], it[Q[1]]);
      }
      if (Tt = [...Tt, {
        placement: d,
        overflows: zt
      }], !zt.every((Q) => Q <= 0)) {
        var _t, dt;
        const Q = (((_t = m.flip) == null ? void 0 : _t.index) || 0) + 1, ft = V[Q];
        if (ft && (!(z === "alignment" ? W !== wl(ft) : !1) || // We leave the current main axis only if every placement on that axis
        // overflows the main axis.
        Tt.every((H) => wl(H.placement) === W ? H.overflows[0] > 0 : !0)))
          return {
            data: {
              index: Q,
              overflows: Tt
            },
            reset: {
              placement: ft
            }
          };
        let R = (dt = Tt.filter((B) => B.overflows[0] <= 0).sort((B, H) => B.overflows[1] - H.overflows[1])[0]) == null ? void 0 : dt.placement;
        if (!R)
          switch (D) {
            case "bestFit": {
              var Ct;
              const B = (Ct = Tt.filter((H) => {
                if (lt) {
                  const F = wl(H.placement);
                  return F === W || // Create a bias to the `y` side axis due to horizontal
                  // reading directions favoring greater width.
                  F === "y";
                }
                return !0;
              }).map((H) => [H.placement, H.overflows.filter((F) => F > 0).reduce((F, et) => F + et, 0)]).sort((H, F) => H[1] - F[1])[0]) == null ? void 0 : Ct[0];
              B && (R = B);
              break;
            }
            case "initialPlacement":
              R = p;
              break;
          }
        if (d !== R)
          return {
            reset: {
              placement: R
            }
          };
      }
      return {};
    }
  };
}, d1 = /* @__PURE__ */ new Set(["left", "top"]);
async function m1(i, c) {
  const {
    placement: s,
    platform: o,
    elements: d
  } = i, m = await (o.isRTL == null ? void 0 : o.isRTL(d.floating)), v = on(s), p = cu(s), S = wl(s) === "y", h = d1.has(v) ? -1 : 1, M = m && S ? -1 : 1, z = iu(c, i);
  let {
    mainAxis: N,
    crossAxis: D,
    alignmentAxis: Y
  } = typeof z == "number" ? {
    mainAxis: z,
    crossAxis: 0,
    alignmentAxis: null
  } : {
    mainAxis: z.mainAxis || 0,
    crossAxis: z.crossAxis || 0,
    alignmentAxis: z.alignmentAxis
  };
  return p && typeof Y == "number" && (D = p === "end" ? Y * -1 : Y), S ? {
    x: D * M,
    y: N * h
  } : {
    x: N * h,
    y: D * M
  };
}
const v1 = function(i) {
  return i === void 0 && (i = 0), {
    name: "offset",
    options: i,
    async fn(c) {
      var s, o;
      const {
        x: d,
        y: m,
        placement: v,
        middlewareData: p
      } = c, S = await m1(c, i);
      return v === ((s = p.offset) == null ? void 0 : s.placement) && (o = p.arrow) != null && o.alignmentOffset ? {} : {
        x: d + S.x,
        y: m + S.y,
        data: {
          ...S,
          placement: v
        }
      };
    }
  };
}, h1 = function(i) {
  return i === void 0 && (i = {}), {
    name: "shift",
    options: i,
    async fn(c) {
      const {
        x: s,
        y: o,
        placement: d,
        platform: m
      } = c, {
        mainAxis: v = !0,
        crossAxis: p = !1,
        limiter: S = {
          fn: (X) => {
            let {
              x: W,
              y: G
            } = X;
            return {
              x: W,
              y: G
            };
          }
        },
        ...h
      } = iu(i, c), M = {
        x: s,
        y: o
      }, z = await m.detectOverflow(c, h), N = wl(on(d)), D = Jv(N);
      let Y = M[D], L = M[N];
      if (v) {
        const X = D === "y" ? "top" : "left", W = D === "y" ? "bottom" : "right", G = Y + z[X], $ = Y - z[W];
        Y = No(G, Y, $);
      }
      if (p) {
        const X = N === "y" ? "top" : "left", W = N === "y" ? "bottom" : "right", G = L + z[X], $ = L - z[W];
        L = No(G, L, $);
      }
      const Z = S.fn({
        ...c,
        [D]: Y,
        [N]: L
      });
      return {
        ...Z,
        data: {
          x: Z.x - s,
          y: Z.y - o,
          enabled: {
            [D]: v,
            [N]: p
          }
        }
      };
    }
  };
};
function nh(i) {
  const c = Se(i);
  let s = parseFloat(c.width) || 0, o = parseFloat(c.height) || 0;
  const d = je(i), m = d ? i.offsetWidth : s, v = d ? i.offsetHeight : o, p = Hi(s) !== m || Hi(o) !== v;
  return p && (s = m, o = v), {
    width: s,
    height: o,
    $: p
  };
}
function Jo(i) {
  return Lt(i) ? i : i.contextElement;
}
function In(i) {
  const c = Jo(i);
  if (!je(c))
    return Le(1);
  const s = c.getBoundingClientRect(), {
    width: o,
    height: d,
    $: m
  } = nh(c);
  let v = (m ? Hi(s.width) : s.width) / o, p = (m ? Hi(s.height) : s.height) / d;
  return (!v || !Number.isFinite(v)) && (v = 1), (!p || !Number.isFinite(p)) && (p = 1), {
    x: v,
    y: p
  };
}
const y1 = /* @__PURE__ */ Le(0);
function ah(i) {
  const c = be(i);
  return !Vi() || !c.visualViewport ? y1 : {
    x: c.visualViewport.offsetLeft,
    y: c.visualViewport.offsetTop
  };
}
function g1(i, c, s) {
  return c === void 0 && (c = !1), !s || c && s !== be(i) ? !1 : c;
}
function sn(i, c, s, o) {
  c === void 0 && (c = !1), s === void 0 && (s = !1);
  const d = i.getBoundingClientRect(), m = Jo(i);
  let v = Le(1);
  c && (o ? Lt(o) && (v = In(o)) : v = In(i));
  const p = g1(m, s, o) ? ah(m) : Le(0);
  let S = (d.left + p.x) / v.x, h = (d.top + p.y) / v.y, M = d.width / v.x, z = d.height / v.y;
  if (m) {
    const N = be(m), D = o && Lt(o) ? be(o) : o;
    let Y = N, L = Do(Y);
    for (; L && o && D !== Y; ) {
      const Z = In(L), X = L.getBoundingClientRect(), W = Se(L), G = X.left + (L.clientLeft + parseFloat(W.paddingLeft)) * Z.x, $ = X.top + (L.clientTop + parseFloat(W.paddingTop)) * Z.y;
      S *= Z.x, h *= Z.y, M *= Z.x, z *= Z.y, S += G, h += $, Y = be(L), L = Do(Y);
    }
  }
  return qi({
    width: M,
    height: z,
    x: S,
    y: h
  });
}
function Ji(i, c) {
  const s = Ki(i).scrollLeft;
  return c ? c.left + s : sn(Ye(i)).left + s;
}
function uh(i, c) {
  const s = i.getBoundingClientRect(), o = s.left + c.scrollLeft - Ji(i, s), d = s.top + c.scrollTop;
  return {
    x: o,
    y: d
  };
}
function b1(i) {
  let {
    elements: c,
    rect: s,
    offsetParent: o,
    strategy: d
  } = i;
  const m = d === "fixed", v = Ye(o), p = c ? Zi(c.floating) : !1;
  if (o === v || p && m)
    return s;
  let S = {
    scrollLeft: 0,
    scrollTop: 0
  }, h = Le(1);
  const M = Le(0), z = je(o);
  if ((z || !z && !m) && ((ta(o) !== "body" || uu(v)) && (S = Ki(o)), z)) {
    const D = sn(o);
    h = In(o), M.x = D.x + o.clientLeft, M.y = D.y + o.clientTop;
  }
  const N = v && !z && !m ? uh(v, S) : Le(0);
  return {
    width: s.width * h.x,
    height: s.height * h.y,
    x: s.x * h.x - S.scrollLeft * h.x + M.x + N.x,
    y: s.y * h.y - S.scrollTop * h.y + M.y + N.y
  };
}
function S1(i) {
  return Array.from(i.getClientRects());
}
function p1(i) {
  const c = Ye(i), s = Ki(i), o = i.ownerDocument.body, d = fn(c.scrollWidth, c.clientWidth, o.scrollWidth, o.clientWidth), m = fn(c.scrollHeight, c.clientHeight, o.scrollHeight, o.clientHeight);
  let v = -s.scrollLeft + Ji(i);
  const p = -s.scrollTop;
  return Se(o).direction === "rtl" && (v += fn(c.clientWidth, o.clientWidth) - d), {
    width: d,
    height: m,
    x: v,
    y: p
  };
}
const gv = 25;
function E1(i, c) {
  const s = be(i), o = Ye(i), d = s.visualViewport;
  let m = o.clientWidth, v = o.clientHeight, p = 0, S = 0;
  if (d) {
    m = d.width, v = d.height;
    const M = Vi();
    (!M || M && c === "fixed") && (p = d.offsetLeft, S = d.offsetTop);
  }
  const h = Ji(o);
  if (h <= 0) {
    const M = o.ownerDocument, z = M.body, N = getComputedStyle(z), D = M.compatMode === "CSS1Compat" && parseFloat(N.marginLeft) + parseFloat(N.marginRight) || 0, Y = Math.abs(o.clientWidth - z.clientWidth - D);
    Y <= gv && (m -= Y);
  } else h <= gv && (m += h);
  return {
    width: m,
    height: v,
    x: p,
    y: S
  };
}
function T1(i, c) {
  const s = sn(i, !0, c === "fixed"), o = s.top + i.clientTop, d = s.left + i.clientLeft, m = je(i) ? In(i) : Le(1), v = i.clientWidth * m.x, p = i.clientHeight * m.y, S = d * m.x, h = o * m.y;
  return {
    width: v,
    height: p,
    x: S,
    y: h
  };
}
function bv(i, c, s) {
  let o;
  if (c === "viewport")
    o = E1(i, s);
  else if (c === "document")
    o = p1(Ye(i));
  else if (Lt(c))
    o = T1(c, s);
  else {
    const d = ah(i);
    o = {
      x: c.x - d.x,
      y: c.y - d.y,
      width: c.width,
      height: c.height
    };
  }
  return qi(o);
}
function ih(i, c) {
  const s = cl(i);
  return s === c || !Lt(s) || il(s) ? !1 : Se(s).position === "fixed" || ih(s, c);
}
function A1(i, c) {
  const s = c.get(i);
  if (s)
    return s;
  let o = Hl(i, [], !1).filter((p) => Lt(p) && ta(p) !== "body"), d = null;
  const m = Se(i).position === "fixed";
  let v = m ? cl(i) : i;
  for (; Lt(v) && !il(v); ) {
    const p = Se(v), S = Zo(v);
    !S && p.position === "fixed" && (d = null), (m ? !S && !d : !S && p.position === "static" && !!d && (d.position === "absolute" || d.position === "fixed") || uu(v) && !S && ih(i, v)) ? o = o.filter((M) => M !== v) : d = p, v = cl(v);
  }
  return c.set(i, o), o;
}
function R1(i) {
  let {
    element: c,
    boundary: s,
    rootBoundary: o,
    strategy: d
  } = i;
  const v = [...s === "clippingAncestors" ? Zi(c) ? [] : A1(c, this._c) : [].concat(s), o], p = bv(c, v[0], d);
  let S = p.top, h = p.right, M = p.bottom, z = p.left;
  for (let N = 1; N < v.length; N++) {
    const D = bv(c, v[N], d);
    S = fn(D.top, S), h = Pn(D.right, h), M = Pn(D.bottom, M), z = fn(D.left, z);
  }
  return {
    width: h - z,
    height: M - S,
    x: z,
    y: S
  };
}
function O1(i) {
  const {
    width: c,
    height: s
  } = nh(i);
  return {
    width: c,
    height: s
  };
}
function z1(i, c, s) {
  const o = je(c), d = Ye(c), m = s === "fixed", v = sn(i, !0, m, c);
  let p = {
    scrollLeft: 0,
    scrollTop: 0
  };
  const S = Le(0);
  function h() {
    S.x = Ji(d);
  }
  if (o || !o && !m)
    if ((ta(c) !== "body" || uu(d)) && (p = Ki(c)), o) {
      const D = sn(c, !0, m, c);
      S.x = D.x + c.clientLeft, S.y = D.y + c.clientTop;
    } else d && h();
  m && !o && d && h();
  const M = d && !o && !m ? uh(d, p) : Le(0), z = v.left + p.scrollLeft - S.x - M.x, N = v.top + p.scrollTop - S.y - M.y;
  return {
    x: z,
    y: N,
    width: v.width,
    height: v.height
  };
}
function Oo(i) {
  return Se(i).position === "static";
}
function Sv(i, c) {
  if (!je(i) || Se(i).position === "fixed")
    return null;
  if (c)
    return c(i);
  let s = i.offsetParent;
  return Ye(i) === s && (s = s.ownerDocument.body), s;
}
function ch(i, c) {
  const s = be(i);
  if (Zi(i))
    return s;
  if (!je(i)) {
    let d = cl(i);
    for (; d && !il(d); ) {
      if (Lt(d) && !Oo(d))
        return d;
      d = cl(d);
    }
    return s;
  }
  let o = Sv(i, c);
  for (; o && pg(o) && Oo(o); )
    o = Sv(o, c);
  return o && il(o) && Oo(o) && !Zo(o) ? s : o || Ag(i) || s;
}
const _1 = async function(i) {
  const c = this.getOffsetParent || ch, s = this.getDimensions, o = await s(i.floating);
  return {
    reference: z1(i.reference, await c(i.floating), i.strategy),
    floating: {
      x: 0,
      y: 0,
      width: o.width,
      height: o.height
    }
  };
};
function M1(i) {
  return Se(i).direction === "rtl";
}
const C1 = {
  convertOffsetParentRelativeRectToViewportRelativeRect: b1,
  getDocumentElement: Ye,
  getClippingRect: R1,
  getOffsetParent: ch,
  getElementRects: _1,
  getClientRects: S1,
  getDimensions: O1,
  getScale: In,
  isElement: Lt,
  isRTL: M1
};
function fh(i, c) {
  return i.x === c.x && i.y === c.y && i.width === c.width && i.height === c.height;
}
function x1(i, c) {
  let s = null, o;
  const d = Ye(i);
  function m() {
    var p;
    clearTimeout(o), (p = s) == null || p.disconnect(), s = null;
  }
  function v(p, S) {
    p === void 0 && (p = !1), S === void 0 && (S = 1), m();
    const h = i.getBoundingClientRect(), {
      left: M,
      top: z,
      width: N,
      height: D
    } = h;
    if (p || c(), !N || !D)
      return;
    const Y = zi(z), L = zi(d.clientWidth - (M + N)), Z = zi(d.clientHeight - (z + D)), X = zi(M), G = {
      rootMargin: -Y + "px " + -L + "px " + -Z + "px " + -X + "px",
      threshold: fn(0, Pn(1, S)) || 1
    };
    let $ = !0;
    function at(lt) {
      const V = lt[0].intersectionRatio;
      if (V !== S) {
        if (!$)
          return v();
        V ? v(!1, V) : o = setTimeout(() => {
          v(!1, 1e-7);
        }, 1e3);
      }
      V === 1 && !fh(h, i.getBoundingClientRect()) && v(), $ = !1;
    }
    try {
      s = new IntersectionObserver(at, {
        ...G,
        // Handle <iframe>s
        root: d.ownerDocument
      });
    } catch {
      s = new IntersectionObserver(at, G);
    }
    s.observe(i);
  }
  return v(!0), m;
}
function D1(i, c, s, o) {
  o === void 0 && (o = {});
  const {
    ancestorScroll: d = !0,
    ancestorResize: m = !0,
    elementResize: v = typeof ResizeObserver == "function",
    layoutShift: p = typeof IntersectionObserver == "function",
    animationFrame: S = !1
  } = o, h = Jo(i), M = d || m ? [...h ? Hl(h) : [], ...c ? Hl(c) : []] : [];
  M.forEach((X) => {
    d && X.addEventListener("scroll", s, {
      passive: !0
    }), m && X.addEventListener("resize", s);
  });
  const z = h && p ? x1(h, s) : null;
  let N = -1, D = null;
  v && (D = new ResizeObserver((X) => {
    let [W] = X;
    W && W.target === h && D && c && (D.unobserve(c), cancelAnimationFrame(N), N = requestAnimationFrame(() => {
      var G;
      (G = D) == null || G.observe(c);
    })), s();
  }), h && !S && D.observe(h), c && D.observe(c));
  let Y, L = S ? sn(i) : null;
  S && Z();
  function Z() {
    const X = sn(i);
    L && !fh(L, X) && s(), L = X, Y = requestAnimationFrame(Z);
  }
  return s(), () => {
    var X;
    M.forEach((W) => {
      d && W.removeEventListener("scroll", s), m && W.removeEventListener("resize", s);
    }), z == null || z(), (X = D) == null || X.disconnect(), D = null, S && cancelAnimationFrame(Y);
  };
}
const N1 = v1, U1 = h1, w1 = r1, pv = s1, H1 = (i, c, s) => {
  const o = /* @__PURE__ */ new Map(), d = {
    platform: C1,
    ...s
  }, m = {
    ...d.platform,
    _c: o
  };
  return o1(i, c, {
    ...d,
    platform: m
  });
};
var B1 = typeof document < "u", q1 = function() {
}, xi = B1 ? q.useLayoutEffect : q1;
function ji(i, c) {
  if (i === c)
    return !0;
  if (typeof i != typeof c)
    return !1;
  if (typeof i == "function" && i.toString() === c.toString())
    return !0;
  let s, o, d;
  if (i && c && typeof i == "object") {
    if (Array.isArray(i)) {
      if (s = i.length, s !== c.length) return !1;
      for (o = s; o-- !== 0; )
        if (!ji(i[o], c[o]))
          return !1;
      return !0;
    }
    if (d = Object.keys(i), s = d.length, s !== Object.keys(c).length)
      return !1;
    for (o = s; o-- !== 0; )
      if (!{}.hasOwnProperty.call(c, d[o]))
        return !1;
    for (o = s; o-- !== 0; ) {
      const m = d[o];
      if (!(m === "_owner" && i.$$typeof) && !ji(i[m], c[m]))
        return !1;
    }
    return !0;
  }
  return i !== i && c !== c;
}
function oh(i) {
  return typeof window > "u" ? 1 : (i.ownerDocument.defaultView || window).devicePixelRatio || 1;
}
function Ev(i, c) {
  const s = oh(i);
  return Math.round(c * s) / s;
}
function zo(i) {
  const c = q.useRef(i);
  return xi(() => {
    c.current = i;
  }), c;
}
function L1(i) {
  i === void 0 && (i = {});
  const {
    placement: c = "bottom",
    strategy: s = "absolute",
    middleware: o = [],
    platform: d,
    elements: {
      reference: m,
      floating: v
    } = {},
    transform: p = !0,
    whileElementsMounted: S,
    open: h
  } = i, [M, z] = q.useState({
    x: 0,
    y: 0,
    strategy: s,
    placement: c,
    middlewareData: {},
    isPositioned: !1
  }), [N, D] = q.useState(o);
  ji(N, o) || D(o);
  const [Y, L] = q.useState(null), [Z, X] = q.useState(null), W = q.useCallback((H) => {
    H !== lt.current && (lt.current = H, L(H));
  }, []), G = q.useCallback((H) => {
    H !== V.current && (V.current = H, X(H));
  }, []), $ = m || Y, at = v || Z, lt = q.useRef(null), V = q.useRef(null), it = q.useRef(M), zt = S != null, Tt = zo(S), _t = zo(d), dt = zo(h), Ct = q.useCallback(() => {
    if (!lt.current || !V.current)
      return;
    const H = {
      placement: c,
      strategy: s,
      middleware: N
    };
    _t.current && (H.platform = _t.current), H1(lt.current, V.current, H).then((F) => {
      const et = {
        ...F,
        // The floating element's position may be recomputed while it's closed
        // but still mounted (such as when transitioning out). To ensure
        // `isPositioned` will be `false` initially on the next open, avoid
        // setting it to `true` when `open === false` (must be specified).
        isPositioned: dt.current !== !1
      };
      Q.current && !ji(it.current, et) && (it.current = et, Gi.flushSync(() => {
        z(et);
      }));
    });
  }, [N, c, s, _t, dt]);
  xi(() => {
    h === !1 && it.current.isPositioned && (it.current.isPositioned = !1, z((H) => ({
      ...H,
      isPositioned: !1
    })));
  }, [h]);
  const Q = q.useRef(!1);
  xi(() => (Q.current = !0, () => {
    Q.current = !1;
  }), []), xi(() => {
    if ($ && (lt.current = $), at && (V.current = at), $ && at) {
      if (Tt.current)
        return Tt.current($, at, Ct);
      Ct();
    }
  }, [$, at, Ct, Tt, zt]);
  const ft = q.useMemo(() => ({
    reference: lt,
    floating: V,
    setReference: W,
    setFloating: G
  }), [W, G]), R = q.useMemo(() => ({
    reference: $,
    floating: at
  }), [$, at]), B = q.useMemo(() => {
    const H = {
      position: s,
      left: 0,
      top: 0
    };
    if (!R.floating)
      return H;
    const F = Ev(R.floating, M.x), et = Ev(R.floating, M.y);
    return p ? {
      ...H,
      transform: "translate(" + F + "px, " + et + "px)",
      ...oh(R.floating) >= 1.5 && {
        willChange: "transform"
      }
    } : {
      position: s,
      left: F,
      top: et
    };
  }, [s, p, R.floating, M.x, M.y]);
  return q.useMemo(() => ({
    ...M,
    update: Ct,
    refs: ft,
    elements: R,
    floatingStyles: B
  }), [M, Ct, ft, R, B]);
}
const Y1 = (i) => {
  function c(s) {
    return {}.hasOwnProperty.call(s, "current");
  }
  return {
    name: "arrow",
    options: i,
    fn(s) {
      const {
        element: o,
        padding: d
      } = typeof i == "function" ? i(s) : i;
      return o && c(o) ? o.current != null ? pv({
        element: o.current,
        padding: d
      }).fn(s) : {} : o ? pv({
        element: o,
        padding: d
      }).fn(s) : {};
    }
  };
}, j1 = (i, c) => {
  const s = N1(i);
  return {
    name: s.name,
    fn: s.fn,
    options: [i, c]
  };
}, X1 = (i, c) => {
  const s = U1(i);
  return {
    name: s.name,
    fn: s.fn,
    options: [i, c]
  };
}, G1 = (i, c) => {
  const s = w1(i);
  return {
    name: s.name,
    fn: s.fn,
    options: [i, c]
  };
}, Q1 = (i, c) => {
  const s = Y1(i);
  return {
    name: s.name,
    fn: s.fn,
    options: [i, c]
  };
}, Z1 = "data-floating-ui-focusable", Tv = "active", Av = "selected", V1 = {
  ...wv
};
let Rv = !1, K1 = 0;
const Ov = () => (
  // Ensure the id is unique with multiple independent versions of Floating UI
  // on <React 18
  "floating-ui-" + Math.random().toString(36).slice(2, 6) + K1++
);
function J1() {
  const [i, c] = q.useState(() => Rv ? Ov() : void 0);
  return Bl(() => {
    i == null && c(Ov());
  }, []), q.useEffect(() => {
    Rv = !0;
  }, []), i;
}
const W1 = V1.useId, Wi = W1 || J1, k1 = /* @__PURE__ */ q.forwardRef(function(c, s) {
  const {
    context: {
      placement: o,
      elements: {
        floating: d
      },
      middlewareData: {
        arrow: m,
        shift: v
      }
    },
    width: p = 14,
    height: S = 7,
    tipRadius: h = 0,
    strokeWidth: M = 0,
    staticOffset: z,
    stroke: N,
    d: D,
    style: {
      transform: Y,
      ...L
    } = {},
    ...Z
  } = c, X = Wi(), [W, G] = q.useState(!1);
  if (Bl(() => {
    if (!d) return;
    Se(d).direction === "rtl" && G(!0);
  }, [d]), !d)
    return null;
  const [$, at] = o.split("-"), lt = $ === "top" || $ === "bottom";
  let V = z;
  (lt && v != null && v.x || !lt && v != null && v.y) && (V = null);
  const it = M * 2, zt = it / 2, Tt = p / 2 * (h / -8 + 1), _t = S / 2 * h / 4, dt = !!D, Ct = V && at === "end" ? "bottom" : "top";
  let Q = V && at === "end" ? "right" : "left";
  V && W && (Q = at === "end" ? "left" : "right");
  const ft = (m == null ? void 0 : m.x) != null ? V || m.x : "", R = (m == null ? void 0 : m.y) != null ? V || m.y : "", B = D || "M0,0" + (" H" + p) + (" L" + (p - Tt) + "," + (S - _t)) + (" Q" + p / 2 + "," + S + " " + Tt + "," + (S - _t)) + " Z", H = {
    top: dt ? "rotate(180deg)" : "",
    left: dt ? "rotate(90deg)" : "rotate(-90deg)",
    bottom: dt ? "" : "rotate(180deg)",
    right: dt ? "rotate(-90deg)" : "rotate(90deg)"
  }[$];
  return /* @__PURE__ */ Zt.jsxs("svg", {
    ...Z,
    "aria-hidden": !0,
    ref: s,
    width: dt ? p : p + it,
    height: p,
    viewBox: "0 0 " + p + " " + (S > p ? S : p),
    style: {
      position: "absolute",
      pointerEvents: "none",
      [Q]: ft,
      [Ct]: R,
      [$]: lt || dt ? "100%" : "calc(100% - " + it / 2 + "px)",
      transform: [H, Y].filter((F) => !!F).join(" "),
      ...L
    },
    children: [it > 0 && /* @__PURE__ */ Zt.jsx("path", {
      clipPath: "url(#" + X + ")",
      fill: "none",
      stroke: N,
      strokeWidth: it + (D ? 0 : 1),
      d: B
    }), /* @__PURE__ */ Zt.jsx("path", {
      stroke: it && !D ? Z.fill : "none",
      d: B
    }), /* @__PURE__ */ Zt.jsx("clipPath", {
      id: X,
      children: /* @__PURE__ */ Zt.jsx("rect", {
        x: -zt,
        y: zt * (dt ? -1 : 1),
        width: p + it,
        height: p
      })
    })]
  });
});
function F1() {
  const i = /* @__PURE__ */ new Map();
  return {
    emit(c, s) {
      var o;
      (o = i.get(c)) == null || o.forEach((d) => d(s));
    },
    on(c, s) {
      i.has(c) || i.set(c, /* @__PURE__ */ new Set()), i.get(c).add(s);
    },
    off(c, s) {
      var o;
      (o = i.get(c)) == null || o.delete(s);
    }
  };
}
const $1 = /* @__PURE__ */ q.createContext(null), I1 = /* @__PURE__ */ q.createContext(null), sh = () => {
  var i;
  return ((i = q.useContext($1)) == null ? void 0 : i.id) || null;
}, rh = () => q.useContext(I1);
function Wo(i) {
  return "data-floating-ui-" + i;
}
const P1 = {
  border: 0,
  clip: "rect(0 0 0 0)",
  height: "1px",
  margin: "-1px",
  overflow: "hidden",
  padding: 0,
  position: "fixed",
  whiteSpace: "nowrap",
  width: "1px",
  top: 0,
  left: 0
}, zv = /* @__PURE__ */ q.forwardRef(function(c, s) {
  const [o, d] = q.useState();
  Bl(() => {
    Wg() && d("button");
  }, []);
  const m = {
    ref: s,
    tabIndex: 0,
    // Role is only for VoiceOver
    role: o,
    "aria-hidden": o ? void 0 : !0,
    [Wo("focus-guard")]: "",
    style: P1
  };
  return /* @__PURE__ */ Zt.jsx("span", {
    ...c,
    ...m
  });
}), tb = {
  clipPath: "inset(50%)",
  position: "fixed",
  top: 0,
  left: 0
}, dh = /* @__PURE__ */ q.createContext(null), _v = /* @__PURE__ */ Wo("portal");
function eb(i) {
  i === void 0 && (i = {});
  const {
    id: c,
    root: s
  } = i, o = Wi(), d = nb(), [m, v] = q.useState(null), p = q.useRef(null);
  return Bl(() => () => {
    m == null || m.remove(), queueMicrotask(() => {
      p.current = null;
    });
  }, [m]), Bl(() => {
    if (!o || p.current) return;
    const S = c ? document.getElementById(c) : null;
    if (!S) return;
    const h = document.createElement("div");
    h.id = o, h.setAttribute(_v, ""), S.appendChild(h), p.current = h, v(h);
  }, [c, o]), Bl(() => {
    if (s === null || !o || p.current) return;
    let S = s || (d == null ? void 0 : d.portalNode);
    S && !Qo(S) && (S = S.current), S = S || document.body;
    let h = null;
    c && (h = document.createElement("div"), h.id = c, S.appendChild(h));
    const M = document.createElement("div");
    M.id = o, M.setAttribute(_v, ""), S = h || S, S.appendChild(M), p.current = M, v(M);
  }, [c, s, o, d]), m;
}
function lb(i) {
  const {
    children: c,
    id: s,
    root: o,
    preserveTabOrder: d = !0
  } = i, m = eb({
    id: s,
    root: o
  }), [v, p] = q.useState(null), S = q.useRef(null), h = q.useRef(null), M = q.useRef(null), z = q.useRef(null), N = v == null ? void 0 : v.modal, D = v == null ? void 0 : v.open, Y = (
    // The FocusManager and therefore floating element are currently open/
    // rendered.
    !!v && // Guards are only for non-modal focus management.
    !v.modal && // Don't render if unmount is transitioning.
    v.open && d && !!(o || m)
  );
  return q.useEffect(() => {
    if (!m || !d || N)
      return;
    function L(Z) {
      m && Ro(Z) && (Z.type === "focusin" ? hv : i1)(m);
    }
    return m.addEventListener("focusin", L, !0), m.addEventListener("focusout", L, !0), () => {
      m.removeEventListener("focusin", L, !0), m.removeEventListener("focusout", L, !0);
    };
  }, [m, d, N]), q.useEffect(() => {
    m && (D || hv(m));
  }, [D, m]), /* @__PURE__ */ Zt.jsxs(dh.Provider, {
    value: q.useMemo(() => ({
      preserveTabOrder: d,
      beforeOutsideRef: S,
      afterOutsideRef: h,
      beforeInsideRef: M,
      afterInsideRef: z,
      portalNode: m,
      setFocusManagerState: p
    }), [d, m]),
    children: [Y && m && /* @__PURE__ */ Zt.jsx(zv, {
      "data-type": "outside",
      ref: S,
      onFocus: (L) => {
        if (Ro(L, m)) {
          var Z;
          (Z = M.current) == null || Z.focus();
        } else {
          const X = v ? v.domReference : null, W = u1(X);
          W == null || W.focus();
        }
      }
    }), Y && m && /* @__PURE__ */ Zt.jsx("span", {
      "aria-owns": m.id,
      style: tb
    }), m && /* @__PURE__ */ Gi.createPortal(c, m), Y && m && /* @__PURE__ */ Zt.jsx(zv, {
      "data-type": "outside",
      ref: h,
      onFocus: (L) => {
        if (Ro(L, m)) {
          var Z;
          (Z = z.current) == null || Z.focus();
        } else {
          const X = v ? v.domReference : null, W = a1(X);
          W == null || W.focus(), v != null && v.closeOnFocusOut && (v == null || v.onOpenChange(!1, L.nativeEvent, "focus-out"));
        }
      }
    })]
  });
}
const nb = () => q.useContext(dh), ab = {
  pointerdown: "onPointerDown",
  mousedown: "onMouseDown",
  click: "onClick"
}, ub = {
  pointerdown: "onPointerDownCapture",
  mousedown: "onMouseDownCapture",
  click: "onClickCapture"
}, Mv = (i) => {
  var c, s;
  return {
    escapeKey: typeof i == "boolean" ? i : (c = i == null ? void 0 : i.escapeKey) != null ? c : !1,
    outsidePress: typeof i == "boolean" ? i : (s = i == null ? void 0 : i.outsidePress) != null ? s : !0
  };
};
function ib(i, c) {
  c === void 0 && (c = {});
  const {
    open: s,
    onOpenChange: o,
    elements: d,
    dataRef: m
  } = i, {
    enabled: v = !0,
    escapeKey: p = !0,
    outsidePress: S = !0,
    outsidePressEvent: h = "pointerdown",
    referencePress: M = !1,
    referencePressEvent: z = "pointerdown",
    ancestorScroll: N = !1,
    bubbles: D,
    capture: Y
  } = c, L = rh(), Z = $n(typeof S == "function" ? S : () => !1), X = typeof S == "function" ? Z : S, W = q.useRef(!1), {
    escapeKey: G,
    outsidePress: $
  } = Mv(D), {
    escapeKey: at,
    outsidePress: lt
  } = Mv(Y), V = q.useRef(!1), it = $n((Q) => {
    var ft;
    if (!s || !v || !p || Q.key !== "Escape" || V.current)
      return;
    const R = (ft = m.current.floatingContext) == null ? void 0 : ft.nodeId, B = L ? Ci(L.nodesRef.current, R) : [];
    if (!G && (Q.stopPropagation(), B.length > 0)) {
      let H = !0;
      if (B.forEach((F) => {
        var et;
        if ((et = F.context) != null && et.open && !F.context.dataRef.current.__escapeKeyBubbles) {
          H = !1;
          return;
        }
      }), !H)
        return;
    }
    o(!1, Ig(Q) ? Q.nativeEvent : Q, "escape-key");
  }), zt = $n((Q) => {
    var ft;
    const R = () => {
      var B;
      it(Q), (B = Ia(Q)) == null || B.removeEventListener("keydown", R);
    };
    (ft = Ia(Q)) == null || ft.addEventListener("keydown", R);
  }), Tt = $n((Q) => {
    var ft;
    const R = m.current.insideReactTree;
    m.current.insideReactTree = !1;
    const B = W.current;
    if (W.current = !1, h === "click" && B || R || typeof X == "function" && !X(Q))
      return;
    const H = Ia(Q), F = "[" + Wo("inert") + "]", et = au(d.floating).querySelectorAll(F);
    let g = Lt(H) ? H : null;
    for (; g && !il(g); ) {
      const I = cl(g);
      if (il(I) || !Lt(I))
        break;
      g = I;
    }
    if (et.length && Lt(H) && !Fg(H) && // Clicked on a direct ancestor (e.g. FloatingOverlay).
    !Bo(H, d.floating) && // If the target root element contains none of the markers, then the
    // element was injected after the floating element rendered.
    Array.from(et).every((I) => !Bo(g, I)))
      return;
    if (je(H) && Ct) {
      const I = il(H), nt = Se(H), mt = /auto|scroll/, Jt = I || mt.test(nt.overflowX), Ut = I || mt.test(nt.overflowY), ql = Jt && H.clientWidth > 0 && H.scrollWidth > H.clientWidth, dn = Ut && H.clientHeight > 0 && H.scrollHeight > H.clientHeight, ea = nt.direction === "rtl", fu = dn && (ea ? Q.offsetX <= H.offsetWidth - H.clientWidth : Q.offsetX > H.clientWidth), Xe = ql && Q.offsetY > H.clientHeight;
      if (fu || Xe)
        return;
    }
    const w = (ft = m.current.floatingContext) == null ? void 0 : ft.nodeId, j = L && Ci(L.nodesRef.current, w).some((I) => {
      var nt;
      return Ao(Q, (nt = I.context) == null ? void 0 : nt.elements.floating);
    });
    if (Ao(Q, d.floating) || Ao(Q, d.domReference) || j)
      return;
    const K = L ? Ci(L.nodesRef.current, w) : [];
    if (K.length > 0) {
      let I = !0;
      if (K.forEach((nt) => {
        var mt;
        if ((mt = nt.context) != null && mt.open && !nt.context.dataRef.current.__outsidePressBubbles) {
          I = !1;
          return;
        }
      }), !I)
        return;
    }
    o(!1, Q, "outside-press");
  }), _t = $n((Q) => {
    var ft;
    const R = () => {
      var B;
      Tt(Q), (B = Ia(Q)) == null || B.removeEventListener(h, R);
    };
    (ft = Ia(Q)) == null || ft.addEventListener(h, R);
  });
  q.useEffect(() => {
    if (!s || !v)
      return;
    m.current.__escapeKeyBubbles = G, m.current.__outsidePressBubbles = $;
    let Q = -1;
    function ft(et) {
      o(!1, et, "ancestor-scroll");
    }
    function R() {
      window.clearTimeout(Q), V.current = !0;
    }
    function B() {
      Q = window.setTimeout(
        () => {
          V.current = !1;
        },
        // 0ms or 1ms don't work in Safari. 5ms appears to consistently work.
        // Only apply to WebKit for the test to remain 0ms.
        Vi() ? 5 : 0
      );
    }
    const H = au(d.floating);
    p && (H.addEventListener("keydown", at ? zt : it, at), H.addEventListener("compositionstart", R), H.addEventListener("compositionend", B)), X && H.addEventListener(h, lt ? _t : Tt, lt);
    let F = [];
    return N && (Lt(d.domReference) && (F = Hl(d.domReference)), Lt(d.floating) && (F = F.concat(Hl(d.floating))), !Lt(d.reference) && d.reference && d.reference.contextElement && (F = F.concat(Hl(d.reference.contextElement)))), F = F.filter((et) => {
      var g;
      return et !== ((g = H.defaultView) == null ? void 0 : g.visualViewport);
    }), F.forEach((et) => {
      et.addEventListener("scroll", ft, {
        passive: !0
      });
    }), () => {
      p && (H.removeEventListener("keydown", at ? zt : it, at), H.removeEventListener("compositionstart", R), H.removeEventListener("compositionend", B)), X && H.removeEventListener(h, lt ? _t : Tt, lt), F.forEach((et) => {
        et.removeEventListener("scroll", ft);
      }), window.clearTimeout(Q);
    };
  }, [m, d, p, X, h, s, o, N, v, G, $, it, at, zt, Tt, lt, _t]), q.useEffect(() => {
    m.current.insideReactTree = !1;
  }, [m, X, h]);
  const dt = q.useMemo(() => ({
    onKeyDown: it,
    ...M && {
      [ab[z]]: (Q) => {
        o(!1, Q.nativeEvent, "reference-press");
      },
      ...z !== "click" && {
        onClick(Q) {
          o(!1, Q.nativeEvent, "reference-press");
        }
      }
    }
  }), [it, o, M, z]), Ct = q.useMemo(() => {
    function Q(ft) {
      ft.button === 0 && (W.current = !0);
    }
    return {
      onKeyDown: it,
      onMouseDown: Q,
      onMouseUp: Q,
      [ub[h]]: () => {
        m.current.insideReactTree = !0;
      }
    };
  }, [it, h, m]);
  return q.useMemo(() => v ? {
    reference: dt,
    floating: Ct
  } : {}, [v, dt, Ct]);
}
function cb(i) {
  const {
    open: c = !1,
    onOpenChange: s,
    elements: o
  } = i, d = Wi(), m = q.useRef({}), [v] = q.useState(() => F1()), p = sh() != null, [S, h] = q.useState(o.reference), M = $n((D, Y, L) => {
    m.current.openEvent = D ? Y : void 0, v.emit("openchange", {
      open: D,
      event: Y,
      reason: L,
      nested: p
    }), s == null || s(D, Y, L);
  }), z = q.useMemo(() => ({
    setPositionReference: h
  }), []), N = q.useMemo(() => ({
    reference: S || o.reference || null,
    floating: o.floating || null,
    domReference: o.reference
  }), [S, o.reference, o.floating]);
  return q.useMemo(() => ({
    dataRef: m,
    open: c,
    onOpenChange: M,
    elements: N,
    events: v,
    floatingId: d,
    refs: z
  }), [c, M, N, v, d, z]);
}
function fb(i) {
  i === void 0 && (i = {});
  const {
    nodeId: c
  } = i, s = cb({
    ...i,
    elements: {
      reference: null,
      floating: null,
      ...i.elements
    }
  }), o = i.rootContext || s, d = o.elements, [m, v] = q.useState(null), [p, S] = q.useState(null), M = (d == null ? void 0 : d.domReference) || m, z = q.useRef(null), N = rh();
  Bl(() => {
    M && (z.current = M);
  }, [M]);
  const D = L1({
    ...i,
    elements: {
      ...d,
      ...p && {
        reference: p
      }
    }
  }), Y = q.useCallback((G) => {
    const $ = Lt(G) ? {
      getBoundingClientRect: () => G.getBoundingClientRect(),
      getClientRects: () => G.getClientRects(),
      contextElement: G
    } : G;
    S($), D.refs.setReference($);
  }, [D.refs]), L = q.useCallback((G) => {
    (Lt(G) || G === null) && (z.current = G, v(G)), (Lt(D.refs.reference.current) || D.refs.reference.current === null || // Don't allow setting virtual elements using the old technique back to
    // `null` to support `positionReference` + an unstable `reference`
    // callback ref.
    G !== null && !Lt(G)) && D.refs.setReference(G);
  }, [D.refs]), Z = q.useMemo(() => ({
    ...D.refs,
    setReference: L,
    setPositionReference: Y,
    domReference: z
  }), [D.refs, L, Y]), X = q.useMemo(() => ({
    ...D.elements,
    domReference: M
  }), [D.elements, M]), W = q.useMemo(() => ({
    ...D,
    ...o,
    refs: Z,
    elements: X,
    nodeId: c
  }), [D, Z, X, c, o]);
  return Bl(() => {
    o.dataRef.current.floatingContext = W;
    const G = N == null ? void 0 : N.nodesRef.current.find(($) => $.id === c);
    G && (G.context = W);
  }), q.useMemo(() => ({
    ...D,
    context: W,
    refs: Z,
    elements: X
  }), [D, Z, X, W]);
}
function _o(i, c, s) {
  const o = /* @__PURE__ */ new Map(), d = s === "item";
  let m = i;
  if (d && i) {
    const {
      [Tv]: v,
      [Av]: p,
      ...S
    } = i;
    m = S;
  }
  return {
    ...s === "floating" && {
      tabIndex: -1,
      [Z1]: ""
    },
    ...m,
    ...c.map((v) => {
      const p = v ? v[s] : null;
      return typeof p == "function" ? i ? p(i) : null : p;
    }).concat(i).reduce((v, p) => (p && Object.entries(p).forEach((S) => {
      let [h, M] = S;
      if (!(d && [Tv, Av].includes(h)))
        if (h.indexOf("on") === 0) {
          if (o.has(h) || o.set(h, []), typeof M == "function") {
            var z;
            (z = o.get(h)) == null || z.push(M), v[h] = function() {
              for (var N, D = arguments.length, Y = new Array(D), L = 0; L < D; L++)
                Y[L] = arguments[L];
              return (N = o.get(h)) == null ? void 0 : N.map((Z) => Z(...Y)).find((Z) => Z !== void 0);
            };
          }
        } else
          v[h] = M;
    }), v), {})
  };
}
function ob(i) {
  i === void 0 && (i = []);
  const c = i.map((p) => p == null ? void 0 : p.reference), s = i.map((p) => p == null ? void 0 : p.floating), o = i.map((p) => p == null ? void 0 : p.item), d = q.useCallback(
    (p) => _o(p, i, "reference"),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    c
  ), m = q.useCallback(
    (p) => _o(p, i, "floating"),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    s
  ), v = q.useCallback(
    (p) => _o(p, i, "item"),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    o
  );
  return q.useMemo(() => ({
    getReferenceProps: d,
    getFloatingProps: m,
    getItemProps: v
  }), [d, m, v]);
}
const sb = /* @__PURE__ */ new Map([["select", "listbox"], ["combobox", "listbox"], ["label", !1]]);
function rb(i, c) {
  var s, o;
  c === void 0 && (c = {});
  const {
    open: d,
    elements: m,
    floatingId: v
  } = i, {
    enabled: p = !0,
    role: S = "dialog"
  } = c, h = Wi(), M = ((s = m.domReference) == null ? void 0 : s.id) || h, z = q.useMemo(() => {
    var W;
    return ((W = $g(m.floating)) == null ? void 0 : W.id) || v;
  }, [m.floating, v]), N = (o = sb.get(S)) != null ? o : S, Y = sh() != null, L = q.useMemo(() => N === "tooltip" || S === "label" ? {
    ["aria-" + (S === "label" ? "labelledby" : "describedby")]: d ? z : void 0
  } : {
    "aria-expanded": d ? "true" : "false",
    "aria-haspopup": N === "alertdialog" ? "dialog" : N,
    "aria-controls": d ? z : void 0,
    ...N === "listbox" && {
      role: "combobox"
    },
    ...N === "menu" && {
      id: M
    },
    ...N === "menu" && Y && {
      role: "menuitem"
    },
    ...S === "select" && {
      "aria-autocomplete": "none"
    },
    ...S === "combobox" && {
      "aria-autocomplete": "list"
    }
  }, [N, z, Y, d, M, S]), Z = q.useMemo(() => {
    const W = {
      id: z,
      ...N && {
        role: N
      }
    };
    return N === "tooltip" || S === "label" ? W : {
      ...W,
      ...N === "menu" && {
        "aria-labelledby": M
      }
    };
  }, [N, z, M, S]), X = q.useCallback((W) => {
    let {
      active: G,
      selected: $
    } = W;
    const at = {
      role: "option",
      ...G && {
        id: z + "-fui-option"
      }
    };
    switch (S) {
      case "select":
      case "combobox":
        return {
          ...at,
          "aria-selected": $
        };
    }
    return {};
  }, [z, S]);
  return q.useMemo(() => p ? {
    reference: L,
    floating: Z,
    item: X
  } : {}, [p, L, Z, X]);
}
function db(i) {
  const { anchor: c, data: s, onClose: o, onEnter: d, onLeave: m } = i, v = an.useRef(null), { refs: p, floatingStyles: S, context: h, isPositioned: M } = fb({
    open: !0,
    onOpenChange: (Y) => {
      Y || o();
    },
    placement: "top",
    elements: { reference: c },
    middleware: [j1(10), G1(), X1({ padding: 8 }), Q1({ element: v })],
    whileElementsMounted: D1
  }), z = ib(h, { outsidePress: !0, escapeKey: !0 }), N = rb(h, { role: "tooltip" }), { getFloatingProps: D } = ob([z, N]);
  return /* @__PURE__ */ Zt.jsx(lb, { children: /* @__PURE__ */ Zt.jsxs(
    "div",
    {
      ref: p.setFloating,
      style: { ...S, visibility: M ? "visible" : "hidden" },
      className: "tl-tooltip-popover",
      onPointerEnter: d,
      onPointerLeave: m,
      ...D(),
      children: [
        s.caption ? /* @__PURE__ */ Zt.jsx("div", { className: "tl-tooltip-caption", children: s.caption }) : null,
        s.html != null ? /* @__PURE__ */ Zt.jsx(
          "div",
          {
            className: "tl-tooltip-body",
            dangerouslySetInnerHTML: { __html: s.html }
          }
        ) : /* @__PURE__ */ Zt.jsx("div", { className: "tl-tooltip-body", children: s.text ?? "" }),
        /* @__PURE__ */ Zt.jsx(
          k1,
          {
            ref: v,
            context: h,
            className: "tl-tooltip-arrow",
            width: 12,
            height: 6
          }
        )
      ]
    }
  ) });
}
const mb = 400, vb = 150, _i = /* @__PURE__ */ new Map();
let nn = null, Di = null, Pa = null, tu = null, eu = null;
function Cv() {
  nn || (nn = document.createElement("div"), nn.id = "tl-tooltip-host", document.body.appendChild(nn), Di = Gv.createRoot(nn), ki(), document.addEventListener("pointerover", hb, !0), document.addEventListener("pointerout", yb, !0));
}
function hb(i) {
  const c = i.target;
  if (!c) return;
  const s = mh(c);
  if (!s) return;
  gh(), yh();
  const o = bb(s, c);
  if (!o) return;
  const d = s.kind === "dynamic" ? c : s.el;
  Sb(d, o);
}
function yb(i) {
  const c = i.relatedTarget;
  c && nn && nn.contains(c) || c && mh(c) || (yh(), hh());
}
function mh(i) {
  var s;
  let c = i;
  for (; c; ) {
    const o = (s = c.getAttribute) == null ? void 0 : s.call(c, "data-tooltip");
    if (o != null) {
      const d = gb(o, c);
      if (d) return d;
    }
    c = c.parentElement;
  }
  return null;
}
function gb(i, c) {
  if (i === "dynamic") return { kind: "dynamic", host: c };
  const s = i.indexOf(":");
  if (s < 0) return null;
  const o = i.substring(0, s), d = i.substring(s + 1);
  switch (o) {
    case "text":
      return { kind: "text", text: d, el: c };
    case "html":
      return { kind: "html", html: d, el: c };
    case "key": {
      const m = vh(c);
      return m ? { kind: "key", controlId: m, key: d, el: c } : null;
    }
    default:
      return null;
  }
}
function vh(i) {
  let c = i;
  for (; c; ) {
    const s = c.id;
    if (s && vg(s)) return s;
    c = c.parentElement;
  }
  return null;
}
function bb(i, c) {
  switch (i.kind) {
    case "text":
      return Promise.resolve({ text: i.text });
    case "html":
      return Promise.resolve({ html: i.html });
    case "key": {
      const s = i.controlId + "\0" + i.key;
      let o = _i.get(s);
      return o || (o = xv(i.controlId, i.key), _i.set(s, o)), o;
    }
    case "dynamic": {
      const s = { target: c, resolved: null };
      i.host.dispatchEvent(new CustomEvent("tl-tooltip-resolve", { detail: s, bubbles: !1 }));
      const o = s.resolved;
      if (!o) return null;
      if ("inline" in o) return Promise.resolve(o.inline);
      const d = vh(i.host);
      if (!d) return null;
      const m = d + "\0" + o.key;
      let v = _i.get(m);
      return v || (v = xv(d, o.key), _i.set(m, v)), v;
    }
  }
}
function Sb(i, c) {
  Pa = window.setTimeout(async () => {
    Pa = null;
    const s = await c;
    s && document.contains(i) && (eu = { anchor: i, data: s }, ki());
  }, mb);
}
function hh() {
  tu = window.setTimeout(() => {
    tu = null, eu = null, ki();
  }, vb);
}
function yh() {
  Pa != null && (window.clearTimeout(Pa), Pa = null);
}
function gh() {
  tu != null && (window.clearTimeout(tu), tu = null);
}
function ki() {
  if (!Di) return;
  if (!eu) {
    Di.render(null);
    return;
  }
  const { anchor: i, data: c } = eu;
  Di.render(
    an.createElement(db, {
      anchor: i,
      data: c,
      onClose: () => {
        eu = null, ki();
      },
      onEnter: gh,
      onLeave: hh
    })
  );
}
async function xv(i, c) {
  const s = document.body.dataset.windowName ?? "", o = cn() + `react-api/tooltip?controlId=${encodeURIComponent(i)}&key=${encodeURIComponent(c)}&windowName=${encodeURIComponent(s)}`, d = await fetch(o, { credentials: "same-origin" });
  return d.ok ? await d.json() : null;
}
window.TLReact = { mount: Go, mountField: mg, discoverAndMount: wi, subscribe: Xo, unsubscribe: Yv };
lg();
document.readyState === "loading" ? window.addEventListener("DOMContentLoaded", () => Cv(), { once: !0 }) : Cv();
export {
  an as React,
  Tb as ReactDOM,
  zb as TLChild,
  rn as TLControlContext,
  Im as connect,
  hg as createChildContext,
  wi as discoverAndMount,
  Nv as getComponent,
  vg as isMountedControl,
  Go as mount,
  mg as mountField,
  pb as register,
  Xo as subscribe,
  Ui as unmount,
  Yv as unsubscribe,
  Eb as useI18N,
  gg as useTLCommand,
  Rb as useTLDataUrl,
  Ob as useTLFieldValue,
  yg as useTLState,
  Ab as useTLUpload
};
