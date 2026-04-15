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
function Eb(i, c) {
  Dv.set(i, c);
}
function Nv(i) {
  return Dv.get(i);
}
function Uv(i) {
  return i && i.__esModule && Object.prototype.hasOwnProperty.call(i, "default") ? i.default : i;
}
var yo = { exports: {} }, ut = {};
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
  var i = Symbol.for("react.transitional.element"), c = Symbol.for("react.portal"), s = Symbol.for("react.fragment"), o = Symbol.for("react.strict_mode"), d = Symbol.for("react.profiler"), m = Symbol.for("react.consumer"), v = Symbol.for("react.context"), p = Symbol.for("react.forward_ref"), S = Symbol.for("react.suspense"), y = Symbol.for("react.memo"), M = Symbol.for("react.lazy"), z = Symbol.for("react.activity"), N = Symbol.iterator;
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
  }, L = Object.assign, V = {};
  function X(g, w, j) {
    this.props = g, this.context = w, this.refs = V, this.updater = j || Y;
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
    this.props = g, this.context = w, this.refs = V, this.updater = j || Y;
  }
  var $ = G.prototype = new W();
  $.constructor = G, L($, X.prototype), $.isPureReactComponent = !0;
  var at = Array.isArray;
  function lt() {
  }
  var Z = { H: null, A: null, T: null, S: null }, it = Object.prototype.hasOwnProperty;
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
      return I = I(g), mt = K === "" ? "." + Q(g, 0) : K, at(I) ? (j = "", mt != null && (j = mt.replace(Ct, "$&/") + "/"), R(I, w, j, "", function(Ll) {
        return Ll;
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
  return ut.Activity = z, ut.Children = et, ut.Component = X, ut.Fragment = s, ut.Profiler = d, ut.PureComponent = G, ut.StrictMode = o, ut.Suspense = S, ut.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = Z, ut.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(g) {
      return Z.H.useMemoCache(g);
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
      $$typeof: y,
      type: g,
      compare: w === void 0 ? null : w
    };
  }, ut.startTransition = function(g) {
    var w = Z.T, j = {};
    Z.T = j;
    try {
      var K = g(), I = Z.S;
      I !== null && I(j, K), typeof K == "object" && K !== null && typeof K.then == "function" && K.then(lt, F);
    } catch (nt) {
      F(nt);
    } finally {
      w !== null && j.types !== null && (w.types = j.types), Z.T = w;
    }
  }, ut.unstable_useCacheRefresh = function() {
    return Z.H.useCacheRefresh();
  }, ut.use = function(g) {
    return Z.H.use(g);
  }, ut.useActionState = function(g, w, j) {
    return Z.H.useActionState(g, w, j);
  }, ut.useCallback = function(g, w) {
    return Z.H.useCallback(g, w);
  }, ut.useContext = function(g) {
    return Z.H.useContext(g);
  }, ut.useDebugValue = function() {
  }, ut.useDeferredValue = function(g, w) {
    return Z.H.useDeferredValue(g, w);
  }, ut.useEffect = function(g, w) {
    return Z.H.useEffect(g, w);
  }, ut.useEffectEvent = function(g) {
    return Z.H.useEffectEvent(g);
  }, ut.useId = function() {
    return Z.H.useId();
  }, ut.useImperativeHandle = function(g, w, j) {
    return Z.H.useImperativeHandle(g, w, j);
  }, ut.useInsertionEffect = function(g, w) {
    return Z.H.useInsertionEffect(g, w);
  }, ut.useLayoutEffect = function(g, w) {
    return Z.H.useLayoutEffect(g, w);
  }, ut.useMemo = function(g, w) {
    return Z.H.useMemo(g, w);
  }, ut.useOptimistic = function(g, w) {
    return Z.H.useOptimistic(g, w);
  }, ut.useReducer = function(g, w, j) {
    return Z.H.useReducer(g, w, j);
  }, ut.useRef = function(g) {
    return Z.H.useRef(g);
  }, ut.useState = function(g) {
    return Z.H.useState(g);
  }, ut.useSyncExternalStore = function(g, w, j) {
    return Z.H.useSyncExternalStore(
      g,
      w,
      j
    );
  }, ut.useTransition = function() {
    return Z.H.useTransition();
  }, ut.version = "19.2.4", ut;
}
var Fm;
function qo() {
  return Fm || (Fm = 1, yo.exports = G0()), yo.exports;
}
var q = qo();
const un = /* @__PURE__ */ Uv(q), wv = /* @__PURE__ */ X0({
  __proto__: null,
  default: un
}, [q]), Lo = /* @__PURE__ */ new Map(), Mi = /* @__PURE__ */ new Set();
let Mo = !1, Yo = 0;
const Co = /* @__PURE__ */ new Set();
let Hv = "", Bv = "";
function Q0(i) {
  Hv = i;
}
function V0(i) {
  Bv = i;
}
function qv() {
  for (const i of Co)
    i();
}
function Z0(i) {
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
function Tb(i) {
  un.useSyncExternalStore(Z0, K0);
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
const cn = /* @__PURE__ */ new Map();
let ka = null;
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
  ka === null && (ka = setInterval(() => {
    for (const [i, c] of cn)
      c.closed && (cn.delete(i), $0(i));
    cn.size === 0 && ka !== null && (clearInterval(ka), ka = null);
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
  o ? (cn.set(i.windowId, o), F0()) : eg(i.windowId);
}
function P0(i) {
  const c = cn.get(i.windowId);
  c && (c.close(), cn.delete(i.windowId));
}
function tg(i) {
  const c = cn.get(i.windowId);
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
let Fa = null, $m = null, Ni = 0, ho = null;
const ng = 45e3, ag = 15e3;
function Im(i) {
  $m = i, ho && clearInterval(ho), Pm(i), ho = setInterval(() => {
    Ni > 0 && Date.now() - Ni > ng && (console.warn("[TLReact] No heartbeat received, reconnecting SSE."), Pm($m));
  }, ag);
}
function Pm(i) {
  Fa && Fa.close(), Fa = new EventSource(i), Ni = Date.now(), Lv(), Fa.onmessage = (c) => {
    Ni = Date.now();
    try {
      const s = JSON.parse(c.data);
      ug(s);
    } catch (s) {
      console.error("[TLReact] Failed to parse SSE event:", s);
    }
  }, Fa.onerror = () => {
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
var go = { exports: {} }, $a = {}, bo = { exports: {} }, So = {};
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
    var S = [], y = [], M = 1, z = null, N = 3, D = !1, Y = !1, L = !1, V = !1, X = typeof setTimeout == "function" ? setTimeout : null, W = typeof clearTimeout == "function" ? clearTimeout : null, G = typeof setImmediate < "u" ? setImmediate : null;
    function $(R) {
      for (var B = s(y); B !== null; ) {
        if (B.callback === null) o(y);
        else if (B.startTime <= R)
          o(y), B.sortIndex = B.expirationTime, c(S, B);
        else break;
        B = s(y);
      }
    }
    function at(R) {
      if (L = !1, $(R), !Y)
        if (s(S) !== null)
          Y = !0, lt || (lt = !0, dt());
        else {
          var B = s(y);
          B !== null && ft(at, B.startTime - R);
        }
    }
    var lt = !1, Z = -1, it = 5, zt = -1;
    function Tt() {
      return V ? !0 : !(i.unstable_now() - zt < it);
    }
    function _t() {
      if (V = !1, lt) {
        var R = i.unstable_now();
        zt = R;
        var B = !0;
        try {
          t: {
            Y = !1, L && (L = !1, W(Z), Z = -1), D = !0;
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
                  var g = s(y);
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
      Z = X(function() {
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
      V = !0;
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
      }, H > F ? (R.sortIndex = H, c(y, R), s(S) === null && R === s(y) && (L ? (W(Z), Z = -1) : L = !0, ft(at, H - F))) : (R.sortIndex = et, c(S, R), Y || D || (Y = !0, lt || (lt = !0, dt()))), R;
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
    var y = "https://react.dev/errors/" + S;
    if (1 < arguments.length) {
      y += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var M = 2; M < arguments.length; M++)
        y += "&args[]=" + encodeURIComponent(arguments[M]);
    }
    return "Minified React error #" + S + "; visit " + y + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
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
  function m(S, y, M) {
    var z = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: d,
      key: z == null ? null : "" + z,
      children: S,
      containerInfo: y,
      implementation: M
    };
  }
  var v = i.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function p(S, y) {
    if (S === "font") return "";
    if (typeof y == "string")
      return y === "use-credentials" ? y : "";
  }
  return Pt.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = o, Pt.createPortal = function(S, y) {
    var M = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!y || y.nodeType !== 1 && y.nodeType !== 9 && y.nodeType !== 11)
      throw Error(c(299));
    return m(S, y, null, M);
  }, Pt.flushSync = function(S) {
    var y = v.T, M = o.p;
    try {
      if (v.T = null, o.p = 2, S) return S();
    } finally {
      v.T = y, o.p = M, o.d.f();
    }
  }, Pt.preconnect = function(S, y) {
    typeof S == "string" && (y ? (y = y.crossOrigin, y = typeof y == "string" ? y === "use-credentials" ? y : "" : void 0) : y = null, o.d.C(S, y));
  }, Pt.prefetchDNS = function(S) {
    typeof S == "string" && o.d.D(S);
  }, Pt.preinit = function(S, y) {
    if (typeof S == "string" && y && typeof y.as == "string") {
      var M = y.as, z = p(M, y.crossOrigin), N = typeof y.integrity == "string" ? y.integrity : void 0, D = typeof y.fetchPriority == "string" ? y.fetchPriority : void 0;
      M === "style" ? o.d.S(
        S,
        typeof y.precedence == "string" ? y.precedence : void 0,
        {
          crossOrigin: z,
          integrity: N,
          fetchPriority: D
        }
      ) : M === "script" && o.d.X(S, {
        crossOrigin: z,
        integrity: N,
        fetchPriority: D,
        nonce: typeof y.nonce == "string" ? y.nonce : void 0
      });
    }
  }, Pt.preinitModule = function(S, y) {
    if (typeof S == "string")
      if (typeof y == "object" && y !== null) {
        if (y.as == null || y.as === "script") {
          var M = p(
            y.as,
            y.crossOrigin
          );
          o.d.M(S, {
            crossOrigin: M,
            integrity: typeof y.integrity == "string" ? y.integrity : void 0,
            nonce: typeof y.nonce == "string" ? y.nonce : void 0
          });
        }
      } else y == null && o.d.M(S);
  }, Pt.preload = function(S, y) {
    if (typeof S == "string" && typeof y == "object" && y !== null && typeof y.as == "string") {
      var M = y.as, z = p(M, y.crossOrigin);
      o.d.L(S, M, {
        crossOrigin: z,
        integrity: typeof y.integrity == "string" ? y.integrity : void 0,
        nonce: typeof y.nonce == "string" ? y.nonce : void 0,
        type: typeof y.type == "string" ? y.type : void 0,
        fetchPriority: typeof y.fetchPriority == "string" ? y.fetchPriority : void 0,
        referrerPolicy: typeof y.referrerPolicy == "string" ? y.referrerPolicy : void 0,
        imageSrcSet: typeof y.imageSrcSet == "string" ? y.imageSrcSet : void 0,
        imageSizes: typeof y.imageSizes == "string" ? y.imageSizes : void 0,
        media: typeof y.media == "string" ? y.media : void 0
      });
    }
  }, Pt.preloadModule = function(S, y) {
    if (typeof S == "string")
      if (y) {
        var M = p(y.as, y.crossOrigin);
        o.d.m(S, {
          as: typeof y.as == "string" && y.as !== "script" ? y.as : void 0,
          crossOrigin: M,
          integrity: typeof y.integrity == "string" ? y.integrity : void 0
        });
      } else o.d.m(S);
  }, Pt.requestFormReset = function(S) {
    o.d.r(S);
  }, Pt.unstable_batchedUpdates = function(S, y) {
    return S(y);
  }, Pt.useFormState = function(S, y, M) {
    return v.H.useFormState(S, y, M);
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
  if (av) return $a;
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
  function y(t) {
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
  var z = Object.assign, N = Symbol.for("react.element"), D = Symbol.for("react.transitional.element"), Y = Symbol.for("react.portal"), L = Symbol.for("react.fragment"), V = Symbol.for("react.strict_mode"), X = Symbol.for("react.profiler"), W = Symbol.for("react.consumer"), G = Symbol.for("react.context"), $ = Symbol.for("react.forward_ref"), at = Symbol.for("react.suspense"), lt = Symbol.for("react.suspense_list"), Z = Symbol.for("react.memo"), it = Symbol.for("react.lazy"), zt = Symbol.for("react.activity"), Tt = Symbol.for("react.memo_cache_sentinel"), _t = Symbol.iterator;
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
      case V:
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
        case Z:
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
  function Ll(t) {
    t.memoizedState !== null && j(mt, t);
    var e = K.current, l = Sm(e, t.type);
    e !== l && (j(I, t), j(K, l));
  }
  function mn(t) {
    I.current === t && (w(K), w(I)), mt.current === t && (w(mt), Za._currentValue = H);
  }
  var la, fu;
  function Xe(t) {
    if (la === void 0)
      try {
        throw Error();
      } catch (l) {
        var e = l.stack.trim().match(/\n( *(at )?)/);
        la = e && e[1] || "", fu = -1 < l.stack.indexOf(`
    at`) ? " (<anonymous>)" : -1 < l.stack.indexOf("@") ? "@unknown:0:0" : "";
      }
    return `
` + la + t + fu;
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
        var h = f.split(`
`), A = r.split(`
`);
        for (a = n = 0; n < h.length && !h[n].includes("DetermineComponentFrameRoot"); )
          n++;
        for (; a < A.length && !A[a].includes(
          "DetermineComponentFrameRoot"
        ); )
          a++;
        if (n === h.length || a === A.length)
          for (n = h.length - 1, a = A.length - 1; 1 <= n && 0 <= a && h[n] !== A[a]; )
            a--;
        for (; 1 <= n && 0 <= a; n--, a--)
          if (h[n] !== A[a]) {
            if (n !== 1 || a !== 1)
              do
                if (n--, a--, 0 > a || h[n] !== A[a]) {
                  var C = `
` + h[n].replace(" at new ", " at ");
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
  function by(t, e) {
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
        e += by(t, l), l = t, t = t.return;
      while (t);
      return e;
    } catch (n) {
      return `
Error generating stack: ` + n.message + `
` + n.stack;
    }
  }
  var Ii = Object.prototype.hasOwnProperty, Pi = i.unstable_scheduleCallback, tc = i.unstable_cancelCallback, Sy = i.unstable_shouldYield, py = i.unstable_requestPaint, fe = i.unstable_now, Ey = i.unstable_getCurrentPriorityLevel, Fo = i.unstable_ImmediatePriority, $o = i.unstable_UserBlockingPriority, ou = i.unstable_NormalPriority, Ty = i.unstable_LowPriority, Io = i.unstable_IdlePriority, Ay = i.log, Ry = i.unstable_setDisableYieldValue, na = null, oe = null;
  function fl(t) {
    if (typeof Ay == "function" && Ry(t), oe && typeof oe.setStrictMode == "function")
      try {
        oe.setStrictMode(na, t);
      } catch {
      }
  }
  var se = Math.clz32 ? Math.clz32 : _y, Oy = Math.log, zy = Math.LN2;
  function _y(t) {
    return t >>>= 0, t === 0 ? 32 : 31 - (Oy(t) / zy | 0) | 0;
  }
  var su = 256, ru = 262144, du = 4194304;
  function Yl(t) {
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
    return r !== 0 ? (n = r & ~u, n !== 0 ? a = Yl(n) : (f &= r, f !== 0 ? a = Yl(f) : l || (l = r & ~t, l !== 0 && (a = Yl(l))))) : (r = n & ~u, r !== 0 ? a = Yl(r) : f !== 0 ? a = Yl(f) : l || (l = n & ~t, l !== 0 && (a = Yl(l)))), a === 0 ? 0 : e !== 0 && e !== a && (e & u) === 0 && (u = a & -a, l = e & -e, u >= l || u === 32 && (l & 4194048) !== 0) ? e : a;
  }
  function aa(t, e) {
    return (t.pendingLanes & ~(t.suspendedLanes & ~t.pingedLanes) & e) === 0;
  }
  function My(t, e) {
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
  function ua(t, e) {
    t.pendingLanes |= e, e !== 268435456 && (t.suspendedLanes = 0, t.pingedLanes = 0, t.warmLanes = 0);
  }
  function Cy(t, e, l, n, a, u) {
    var f = t.pendingLanes;
    t.pendingLanes = l, t.suspendedLanes = 0, t.pingedLanes = 0, t.warmLanes = 0, t.expiredLanes &= l, t.entangledLanes &= l, t.errorRecoveryDisabledLanes &= l, t.shellSuspendCounter = 0;
    var r = t.entanglements, h = t.expirationTimes, A = t.hiddenUpdates;
    for (l = f & ~l; 0 < l; ) {
      var C = 31 - se(l), U = 1 << C;
      r[C] = 0, h[C] = -1;
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
  var ol = Math.random().toString(36).slice(2), Wt = "__reactFiber$" + ol, ee = "__reactProps$" + ol, vn = "__reactContainer$" + ol, ac = "__reactEvents$" + ol, xy = "__reactListeners$" + ol, Dy = "__reactHandles$" + ol, us = "__reactResources$" + ol, ia = "__reactMarker$" + ol;
  function uc(t) {
    delete t[Wt], delete t[ee], delete t[ac], delete t[xy], delete t[Dy];
  }
  function yn(t) {
    var e = t[Wt];
    if (e) return e;
    for (var l = t.parentNode; l; ) {
      if (e = l[vn] || l[Wt]) {
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
    if (t = t[Wt] || t[vn]) {
      var e = t.tag;
      if (e === 5 || e === 6 || e === 13 || e === 31 || e === 26 || e === 27 || e === 3)
        return t;
    }
    return null;
  }
  function ca(t) {
    var e = t.tag;
    if (e === 5 || e === 26 || e === 27 || e === 6) return t.stateNode;
    throw Error(o(33));
  }
  function gn(t) {
    var e = t[us];
    return e || (e = t[us] = { hoistableStyles: /* @__PURE__ */ new Map(), hoistableScripts: /* @__PURE__ */ new Map() }), e;
  }
  function Zt(t) {
    t[ia] = !0;
  }
  var is = /* @__PURE__ */ new Set(), cs = {};
  function jl(t, e) {
    bn(t, e), bn(t + "Capture", e);
  }
  function bn(t, e) {
    for (cs[t] = e, t = 0; t < e.length; t++)
      is.add(e[t]);
  }
  var Ny = RegExp(
    "^[:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD][:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD\\-.0-9\\u00B7\\u0300-\\u036F\\u203F-\\u2040]*$"
  ), fs = {}, os = {};
  function Uy(t) {
    return Ii.call(os, t) ? !0 : Ii.call(fs, t) ? !1 : Ny.test(t) ? os[t] = !0 : (fs[t] = !0, !1);
  }
  function vu(t, e, l) {
    if (Uy(e))
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
  function yu(t, e, l) {
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
  function wy(t, e, l) {
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
      t._valueTracker = wy(
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
  function hu(t) {
    if (t = t || (typeof document < "u" ? document : void 0), typeof t > "u") return null;
    try {
      return t.activeElement || t.body;
    } catch {
      return t.body;
    }
  }
  var Hy = /[\n"\\]/g;
  function Ee(t) {
    return t.replace(
      Hy,
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
    e === "number" && hu(t.ownerDocument) === t || t.defaultValue === "" + l || (t.defaultValue = "" + l);
  }
  function Sn(t, e, l, n) {
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
  function pn(t, e) {
    if (e) {
      var l = t.firstChild;
      if (l && l === t.lastChild && l.nodeType === 3) {
        l.nodeValue = e;
        return;
      }
    }
    t.textContent = e;
  }
  var By = new Set(
    "animationIterationCount aspectRatio borderImageOutset borderImageSlice borderImageWidth boxFlex boxFlexGroup boxOrdinalGroup columnCount columns flex flexGrow flexPositive flexShrink flexNegative flexOrder gridArea gridRow gridRowEnd gridRowSpan gridRowStart gridColumn gridColumnEnd gridColumnSpan gridColumnStart fontWeight lineClamp lineHeight opacity order orphans scale tabSize widows zIndex zoom fillOpacity floodOpacity stopOpacity strokeDasharray strokeDashoffset strokeMiterlimit strokeOpacity strokeWidth MozAnimationIterationCount MozBoxFlex MozBoxFlexGroup MozLineClamp msAnimationIterationCount msFlex msZoom msFlexGrow msFlexNegative msFlexOrder msFlexPositive msFlexShrink msGridColumn msGridColumnSpan msGridRow msGridRowSpan WebkitAnimationIterationCount WebkitBoxFlex WebKitBoxFlexGroup WebkitBoxOrdinalGroup WebkitColumnCount WebkitColumns WebkitFlex WebkitFlexGrow WebkitFlexPositive WebkitFlexShrink WebkitLineClamp".split(
      " "
    )
  );
  function ys(t, e, l) {
    var n = e.indexOf("--") === 0;
    l == null || typeof l == "boolean" || l === "" ? n ? t.setProperty(e, "") : e === "float" ? t.cssFloat = "" : t[e] = "" : n ? t.setProperty(e, l) : typeof l != "number" || l === 0 || By.has(e) ? e === "float" ? t.cssFloat = l : t[e] = ("" + l).trim() : t[e] = l + "px";
  }
  function hs(t, e, l) {
    if (e != null && typeof e != "object")
      throw Error(o(62));
    if (t = t.style, l != null) {
      for (var n in l)
        !l.hasOwnProperty(n) || e != null && e.hasOwnProperty(n) || (n.indexOf("--") === 0 ? t.setProperty(n, "") : n === "float" ? t.cssFloat = "" : t[n] = "");
      for (var a in e)
        n = e[a], e.hasOwnProperty(a) && l[a] !== n && ys(t, a, n);
    } else
      for (var u in e)
        e.hasOwnProperty(u) && ys(t, u, e[u]);
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
  var qy = /* @__PURE__ */ new Map([
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
  ]), Ly = /^[\u0000-\u001F ]*j[\r\n\t]*a[\r\n\t]*v[\r\n\t]*a[\r\n\t]*s[\r\n\t]*c[\r\n\t]*r[\r\n\t]*i[\r\n\t]*p[\r\n\t]*t[\r\n\t]*:/i;
  function gu(t) {
    return Ly.test("" + t) ? "javascript:throw new Error('React has blocked a javascript: URL as a security precaution.')" : t;
  }
  function Qe() {
  }
  var sc = null;
  function rc(t) {
    return t = t.target || t.srcElement || window, t.correspondingUseElement && (t = t.correspondingUseElement), t.nodeType === 3 ? t.parentNode : t;
  }
  var En = null, Tn = null;
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
          e = l.value, e != null && Sn(t, !!l.multiple, e, !1);
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
      if (dc = !1, (En !== null || Tn !== null) && (ai(), En && (e = En, t = Tn, Tn = En = null, gs(e), t)))
        for (e = 0; e < t.length; e++) gs(t[e]);
    }
  }
  function fa(t, e) {
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
  var Ve = !(typeof window > "u" || typeof window.document > "u" || typeof window.document.createElement > "u"), mc = !1;
  if (Ve)
    try {
      var oa = {};
      Object.defineProperty(oa, "passive", {
        get: function() {
          mc = !0;
        }
      }), window.addEventListener("test", oa, oa), window.removeEventListener("test", oa, oa);
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
  var Xl = {
    eventPhase: 0,
    bubbles: 0,
    cancelable: 0,
    timeStamp: function(t) {
      return t.timeStamp || Date.now();
    },
    defaultPrevented: 0,
    isTrusted: 0
  }, Eu = le(Xl), sa = z({}, Xl, { view: 0, detail: 0 }), Yy = le(sa), yc, hc, ra, Tu = z({}, sa, {
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
      return "movementX" in t ? t.movementX : (t !== ra && (ra && t.type === "mousemove" ? (yc = t.screenX - ra.screenX, hc = t.screenY - ra.screenY) : hc = yc = 0, ra = t), yc);
    },
    movementY: function(t) {
      return "movementY" in t ? t.movementY : hc;
    }
  }), Es = le(Tu), jy = z({}, Tu, { dataTransfer: 0 }), Xy = le(jy), Gy = z({}, sa, { relatedTarget: 0 }), gc = le(Gy), Qy = z({}, Xl, {
    animationName: 0,
    elapsedTime: 0,
    pseudoElement: 0
  }), Vy = le(Qy), Zy = z({}, Xl, {
    clipboardData: function(t) {
      return "clipboardData" in t ? t.clipboardData : window.clipboardData;
    }
  }), Ky = le(Zy), Jy = z({}, Xl, { data: 0 }), Ts = le(Jy), Wy = {
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
  }, ky = {
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
  }, Fy = {
    Alt: "altKey",
    Control: "ctrlKey",
    Meta: "metaKey",
    Shift: "shiftKey"
  };
  function $y(t) {
    var e = this.nativeEvent;
    return e.getModifierState ? e.getModifierState(t) : (t = Fy[t]) ? !!e[t] : !1;
  }
  function bc() {
    return $y;
  }
  var Iy = z({}, sa, {
    key: function(t) {
      if (t.key) {
        var e = Wy[t.key] || t.key;
        if (e !== "Unidentified") return e;
      }
      return t.type === "keypress" ? (t = Su(t), t === 13 ? "Enter" : String.fromCharCode(t)) : t.type === "keydown" || t.type === "keyup" ? ky[t.keyCode] || "Unidentified" : "";
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
  }), Py = le(Iy), th = z({}, Tu, {
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
  }), As = le(th), eh = z({}, sa, {
    touches: 0,
    targetTouches: 0,
    changedTouches: 0,
    altKey: 0,
    metaKey: 0,
    ctrlKey: 0,
    shiftKey: 0,
    getModifierState: bc
  }), lh = le(eh), nh = z({}, Xl, {
    propertyName: 0,
    elapsedTime: 0,
    pseudoElement: 0
  }), ah = le(nh), uh = z({}, Tu, {
    deltaX: function(t) {
      return "deltaX" in t ? t.deltaX : "wheelDeltaX" in t ? -t.wheelDeltaX : 0;
    },
    deltaY: function(t) {
      return "deltaY" in t ? t.deltaY : "wheelDeltaY" in t ? -t.wheelDeltaY : "wheelDelta" in t ? -t.wheelDelta : 0;
    },
    deltaZ: 0,
    deltaMode: 0
  }), ih = le(uh), ch = z({}, Xl, {
    newState: 0,
    oldState: 0
  }), fh = le(ch), oh = [9, 13, 27, 32], Sc = Ve && "CompositionEvent" in window, da = null;
  Ve && "documentMode" in document && (da = document.documentMode);
  var sh = Ve && "TextEvent" in window && !da, Rs = Ve && (!Sc || da && 8 < da && 11 >= da), Os = " ", zs = !1;
  function _s(t, e) {
    switch (t) {
      case "keyup":
        return oh.indexOf(e.keyCode) !== -1;
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
  var An = !1;
  function rh(t, e) {
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
  function dh(t, e) {
    if (An)
      return t === "compositionend" || !Sc && _s(t, e) ? (t = Ss(), bu = vc = sl = null, An = !1, t) : null;
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
  var mh = {
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
    return e === "input" ? !!mh[t.type] : e === "textarea";
  }
  function xs(t, e, l, n) {
    En ? Tn ? Tn.push(n) : Tn = [n] : En = n, e = ri(e, "onChange"), 0 < e.length && (l = new Eu(
      "onChange",
      "change",
      null,
      l,
      n
    ), t.push({ event: l, listeners: e }));
  }
  var ma = null, va = null;
  function vh(t) {
    dm(t, 0);
  }
  function Au(t) {
    var e = ca(t);
    if (rs(e)) return t;
  }
  function Ds(t, e) {
    if (t === "change") return e;
  }
  var Ns = !1;
  if (Ve) {
    var pc;
    if (Ve) {
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
    ma && (ma.detachEvent("onpropertychange", Hs), va = ma = null);
  }
  function Hs(t) {
    if (t.propertyName === "value" && Au(va)) {
      var e = [];
      xs(
        e,
        va,
        t,
        rc(t)
      ), bs(vh, e);
    }
  }
  function yh(t, e, l) {
    t === "focusin" ? (ws(), ma = e, va = l, ma.attachEvent("onpropertychange", Hs)) : t === "focusout" && ws();
  }
  function hh(t) {
    if (t === "selectionchange" || t === "keyup" || t === "keydown")
      return Au(va);
  }
  function gh(t, e) {
    if (t === "click") return Au(e);
  }
  function bh(t, e) {
    if (t === "input" || t === "change")
      return Au(e);
  }
  function Sh(t, e) {
    return t === e && (t !== 0 || 1 / t === 1 / e) || t !== t && e !== e;
  }
  var re = typeof Object.is == "function" ? Object.is : Sh;
  function ya(t, e) {
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
    for (var e = hu(t.document); e instanceof t.HTMLIFrameElement; ) {
      try {
        var l = typeof e.contentWindow.location.href == "string";
      } catch {
        l = !1;
      }
      if (l) t = e.contentWindow;
      else break;
      e = hu(t.document);
    }
    return e;
  }
  function Tc(t) {
    var e = t && t.nodeName && t.nodeName.toLowerCase();
    return e && (e === "input" && (t.type === "text" || t.type === "search" || t.type === "tel" || t.type === "url" || t.type === "password") || e === "textarea" || t.contentEditable === "true");
  }
  var ph = Ve && "documentMode" in document && 11 >= document.documentMode, Rn = null, Ac = null, ha = null, Rc = !1;
  function js(t, e, l) {
    var n = l.window === l ? l.document : l.nodeType === 9 ? l : l.ownerDocument;
    Rc || Rn == null || Rn !== hu(n) || (n = Rn, "selectionStart" in n && Tc(n) ? n = { start: n.selectionStart, end: n.selectionEnd } : (n = (n.ownerDocument && n.ownerDocument.defaultView || window).getSelection(), n = {
      anchorNode: n.anchorNode,
      anchorOffset: n.anchorOffset,
      focusNode: n.focusNode,
      focusOffset: n.focusOffset
    }), ha && ya(ha, n) || (ha = n, n = ri(Ac, "onSelect"), 0 < n.length && (e = new Eu(
      "onSelect",
      "select",
      null,
      e,
      l
    ), t.push({ event: e, listeners: n }), e.target = Rn)));
  }
  function Gl(t, e) {
    var l = {};
    return l[t.toLowerCase()] = e.toLowerCase(), l["Webkit" + t] = "webkit" + e, l["Moz" + t] = "moz" + e, l;
  }
  var On = {
    animationend: Gl("Animation", "AnimationEnd"),
    animationiteration: Gl("Animation", "AnimationIteration"),
    animationstart: Gl("Animation", "AnimationStart"),
    transitionrun: Gl("Transition", "TransitionRun"),
    transitionstart: Gl("Transition", "TransitionStart"),
    transitioncancel: Gl("Transition", "TransitionCancel"),
    transitionend: Gl("Transition", "TransitionEnd")
  }, Oc = {}, Xs = {};
  Ve && (Xs = document.createElement("div").style, "AnimationEvent" in window || (delete On.animationend.animation, delete On.animationiteration.animation, delete On.animationstart.animation), "TransitionEvent" in window || delete On.transitionend.transition);
  function Ql(t) {
    if (Oc[t]) return Oc[t];
    if (!On[t]) return t;
    var e = On[t], l;
    for (l in e)
      if (e.hasOwnProperty(l) && l in Xs)
        return Oc[t] = e[l];
    return t;
  }
  var Gs = Ql("animationend"), Qs = Ql("animationiteration"), Vs = Ql("animationstart"), Eh = Ql("transitionrun"), Th = Ql("transitionstart"), Ah = Ql("transitioncancel"), Zs = Ql("transitionend"), Ks = /* @__PURE__ */ new Map(), zc = "abort auxClick beforeToggle cancel canPlay canPlayThrough click close contextMenu copy cut drag dragEnd dragEnter dragExit dragLeave dragOver dragStart drop durationChange emptied encrypted ended error gotPointerCapture input invalid keyDown keyPress keyUp load loadedData loadedMetadata loadStart lostPointerCapture mouseDown mouseMove mouseOut mouseOver mouseUp paste pause play playing pointerCancel pointerDown pointerMove pointerOut pointerOver pointerUp progress rateChange reset resize seeked seeking stalled submit suspend timeUpdate touchCancel touchEnd touchStart volumeChange scroll toggle touchMove waiting wheel".split(
    " "
  );
  zc.push("scrollEnd");
  function xe(t, e) {
    Ks.set(t, e), jl(e, [t]);
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
  }, Te = [], zn = 0, _c = 0;
  function Ou() {
    for (var t = zn, e = _c = zn = 0; e < t; ) {
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
    Te[zn++] = t, Te[zn++] = e, Te[zn++] = l, Te[zn++] = n, _c |= n, t.lanes |= n, t = t.alternate, t !== null && (t.lanes |= n);
  }
  function Mc(t, e, l, n) {
    return zu(t, e, l, n), _u(t);
  }
  function Vl(t, e) {
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
    if (50 < La)
      throw La = 0, Lf = null, Error(o(185));
    for (var e = t.return; e !== null; )
      t = e, e = t.return;
    return t.tag === 3 ? t.stateNode : null;
  }
  var _n = {};
  function Rh(t, e, l, n) {
    this.tag = t, this.key = l, this.sibling = this.child = this.return = this.stateNode = this.type = this.elementType = null, this.index = 0, this.refCleanup = this.ref = null, this.pendingProps = e, this.dependencies = this.memoizedState = this.updateQueue = this.memoizedProps = null, this.mode = n, this.subtreeFlags = this.flags = 0, this.deletions = null, this.childLanes = this.lanes = 0, this.alternate = null;
  }
  function de(t, e, l, n) {
    return new Rh(t, e, l, n);
  }
  function Cc(t) {
    return t = t.prototype, !(!t || !t.isReactComponent);
  }
  function Ze(t, e) {
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
        case V:
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
              case Z:
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
  var Mn = [], Cn = 0, Cu = null, ga = 0, Re = [], Oe = 0, rl = null, Ue = 1, we = "";
  function Ke(t, e) {
    Mn[Cn++] = ga, Mn[Cn++] = Cu, Cu = t, ga = e;
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
      Cu = Mn[--Cn], Mn[Cn] = null, ga = Mn[--Cn], Mn[Cn] = null;
    for (; t === rl; )
      rl = Re[--Oe], Re[Oe] = null, we = Re[--Oe], Re[Oe] = null, Ue = Re[--Oe], Re[Oe] = null;
  }
  function Is(t, e) {
    Re[Oe++] = Ue, Re[Oe++] = we, Re[Oe++] = rl, Ue = e.id, we = e.overflow, rl = t;
  }
  var kt = null, xt = null, ht = !1, dl = null, ze = !1, wc = Error(o(519));
  function ml(t) {
    var e = Error(
      o(
        418,
        1 < arguments.length && arguments[1] !== void 0 && arguments[1] ? "text" : "HTML",
        ""
      )
    );
    throw ba(Ae(e, t)), wc;
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
        for (l = 0; l < ja.length; l++)
          rt(ja[l], e);
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
    l = n.children, typeof l != "string" && typeof l != "number" && typeof l != "bigint" || e.textContent === "" + l || n.suppressHydrationWarning === !0 || hm(e.textContent, l) ? (n.popover != null && (rt("beforetoggle", e), rt("toggle", e)), n.onScroll != null && rt("scroll", e), n.onScrollEnd != null && rt("scrollend", e), n.onClick != null && (e.onclick = Qe), e = !0) : e = !1, e || ml(t, !0);
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
  function xn(t) {
    if (t !== kt) return !1;
    if (!ht) return tr(t), ht = !0, !1;
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
  function Kl() {
    xt = kt = null, ht = !1;
  }
  function Hc() {
    var t = dl;
    return t !== null && (ie === null ? ie = t : ie.push.apply(
      ie,
      t
    ), dl = null), t;
  }
  function ba(t) {
    dl === null ? dl = [t] : dl.push(t);
  }
  var Bc = g(null), Jl = null, Je = null;
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
          for (var h = 0; h < e.length; h++)
            if (r.context === e[h]) {
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
  function Dn(t, e, l, n) {
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
  function Wl(t) {
    Jl = t, Je = null, t = t.dependencies, t !== null && (t.firstContext = null);
  }
  function Ft(t) {
    return er(Jl, t);
  }
  function Du(t, e) {
    return Jl === null && Wl(t), er(t, e);
  }
  function er(t, e) {
    var l = e._currentValue;
    if (e = { context: e, memoizedValue: l, next: null }, Je === null) {
      if (t === null) throw Error(o(308));
      Je = e, t.dependencies = { lanes: 0, firstContext: e }, t.flags |= 524288;
    } else Je = Je.next = e;
    return l;
  }
  var Oh = typeof AbortController < "u" ? AbortController : function() {
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
  }, zh = i.unstable_scheduleCallback, _h = i.unstable_NormalPriority, Yt = {
    $$typeof: G,
    Consumer: null,
    Provider: null,
    _currentValue: null,
    _currentValue2: null,
    _threadCount: 0
  };
  function Yc() {
    return {
      controller: new Oh(),
      data: /* @__PURE__ */ new Map(),
      refCount: 0
    };
  }
  function Sa(t) {
    t.refCount--, t.refCount === 0 && zh(_h, function() {
      t.controller.abort();
    });
  }
  var pa = null, jc = 0, Nn = 0, Un = null;
  function Mh(t, e) {
    if (pa === null) {
      var l = pa = [];
      jc = 0, Nn = Vf(), Un = {
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
    if (--jc === 0 && pa !== null) {
      Un !== null && (Un.status = "fulfilled");
      var t = pa;
      pa = null, Nn = 0, Un = null;
      for (var e = 0; e < t.length; e++) (0, t[e])();
    }
  }
  function Ch(t, e) {
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
    jd = fe(), typeof e == "object" && e !== null && typeof e.then == "function" && Mh(t, e), nr !== null && nr(t, e);
  };
  var kl = g(null);
  function Xc() {
    var t = kl.current;
    return t !== null ? t : Mt.pooledCache;
  }
  function Nu(t, e) {
    e === null ? j(kl, kl.current) : j(kl, e.pool);
  }
  function ar() {
    var t = Xc();
    return t === null ? null : { parent: Yt._currentValue, pool: t };
  }
  var wn = Error(o(460)), Gc = Error(o(474)), Uu = Error(o(542)), wu = { then: function() {
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
        throw $l = e, wn;
    }
  }
  function Fl(t) {
    try {
      var e = t._init;
      return e(t._payload);
    } catch (l) {
      throw l !== null && typeof l == "object" && typeof l.then == "function" ? ($l = l, wn) : l;
    }
  }
  var $l = null;
  function cr() {
    if ($l === null) throw Error(o(459));
    var t = $l;
    return $l = null, t;
  }
  function fr(t) {
    if (t === wn || t === Uu)
      throw Error(o(483));
  }
  var Hn = null, Ea = 0;
  function Hu(t) {
    var e = Ea;
    return Ea += 1, Hn === null && (Hn = []), ir(Hn, t, e);
  }
  function Ta(t, e) {
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
      return E = Ze(E, b), E.index = 0, E.sibling = null, E;
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
    function h(E, b, T, x) {
      var P = T.type;
      return P === L ? C(
        E,
        b,
        T.props.children,
        x,
        T.key
      ) : b !== null && (b.elementType === P || typeof P == "object" && P !== null && P.$$typeof === it && Fl(P) === b.type) ? (b = a(b, T.props), Ta(b, T), b.return = E, b) : (b = Mu(
        T.type,
        T.key,
        T.props,
        null,
        E.mode,
        x
      ), Ta(b, T), b.return = E, b);
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
            ), Ta(T, b), T.return = E, T;
          case Y:
            return b = Dc(
              b,
              E.mode,
              T
            ), b.return = E, b;
          case it:
            return b = Fl(b), U(E, b, T);
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
            return T.key === P ? h(E, b, T, x) : null;
          case Y:
            return T.key === P ? A(E, b, T, x) : null;
          case it:
            return T = Fl(T), O(E, b, T, x);
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
            ) || null, h(b, E, x, P);
          case Y:
            return E = E.get(
              x.key === null ? T : x.key
            ) || null, A(b, E, x, P);
          case it:
            return x = Fl(x), _(
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
      for (var P = null, gt = null, k = b, ot = b = 0, yt = null; k !== null && ot < T.length; ot++) {
        k.index > ot ? (yt = k, k = null) : yt = k.sibling;
        var bt = O(
          E,
          k,
          T[ot],
          x
        );
        if (bt === null) {
          k === null && (k = yt);
          break;
        }
        t && k && bt.alternate === null && e(E, k), b = u(bt, b, ot), gt === null ? P = bt : gt.sibling = bt, gt = bt, k = yt;
      }
      if (ot === T.length)
        return l(E, k), ht && Ke(E, ot), P;
      if (k === null) {
        for (; ot < T.length; ot++)
          k = U(E, T[ot], x), k !== null && (b = u(
            k,
            b,
            ot
          ), gt === null ? P = k : gt.sibling = k, gt = k);
        return ht && Ke(E, ot), P;
      }
      for (k = n(k); ot < T.length; ot++)
        yt = _(
          k,
          E,
          ot,
          T[ot],
          x
        ), yt !== null && (t && yt.alternate !== null && k.delete(
          yt.key === null ? ot : yt.key
        ), b = u(
          yt,
          b,
          ot
        ), gt === null ? P = yt : gt.sibling = yt, gt = yt);
      return t && k.forEach(function(Nl) {
        return e(E, Nl);
      }), ht && Ke(E, ot), P;
    }
    function tt(E, b, T, x) {
      if (T == null) throw Error(o(151));
      for (var P = null, gt = null, k = b, ot = b = 0, yt = null, bt = T.next(); k !== null && !bt.done; ot++, bt = T.next()) {
        k.index > ot ? (yt = k, k = null) : yt = k.sibling;
        var Nl = O(E, k, bt.value, x);
        if (Nl === null) {
          k === null && (k = yt);
          break;
        }
        t && k && Nl.alternate === null && e(E, k), b = u(Nl, b, ot), gt === null ? P = Nl : gt.sibling = Nl, gt = Nl, k = yt;
      }
      if (bt.done)
        return l(E, k), ht && Ke(E, ot), P;
      if (k === null) {
        for (; !bt.done; ot++, bt = T.next())
          bt = U(E, bt.value, x), bt !== null && (b = u(bt, b, ot), gt === null ? P = bt : gt.sibling = bt, gt = bt);
        return ht && Ke(E, ot), P;
      }
      for (k = n(k); !bt.done; ot++, bt = T.next())
        bt = _(k, E, ot, bt.value, x), bt !== null && (t && bt.alternate !== null && k.delete(bt.key === null ? ot : bt.key), b = u(bt, b, ot), gt === null ? P = bt : gt.sibling = bt, gt = bt);
      return t && k.forEach(function(j0) {
        return e(E, j0);
      }), ht && Ke(E, ot), P;
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
                  } else if (b.elementType === P || typeof P == "object" && P !== null && P.$$typeof === it && Fl(P) === b.type) {
                    l(
                      E,
                      b.sibling
                    ), x = a(b, T.props), Ta(x, T), x.return = E, E = x;
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
              ), Ta(x, T), x.return = E, E = x);
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
            return T = Fl(T), Ot(
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
        Ea = 0;
        var P = Ot(
          E,
          b,
          T,
          x
        );
        return Hn = null, P;
      } catch (k) {
        if (k === wn || k === Uu) throw k;
        var gt = de(29, k, null, E.mode);
        return gt.lanes = x, gt.return = E, gt;
      } finally {
      }
    };
  }
  var Il = or(!0), sr = or(!1), yl = !1;
  function Qc(t) {
    t.updateQueue = {
      baseState: t.memoizedState,
      firstBaseUpdate: null,
      lastBaseUpdate: null,
      shared: { pending: null, lanes: 0, hiddenCallbacks: null },
      callbacks: null
    };
  }
  function Vc(t, e) {
    t = t.updateQueue, e.updateQueue === t && (e.updateQueue = {
      baseState: t.baseState,
      firstBaseUpdate: t.firstBaseUpdate,
      lastBaseUpdate: t.lastBaseUpdate,
      shared: t.shared,
      callbacks: null
    });
  }
  function hl(t) {
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
  function Aa(t, e, l) {
    if (e = e.updateQueue, e !== null && (e = e.shared, (l & 4194048) !== 0)) {
      var n = e.lanes;
      n &= t.pendingLanes, l |= n, e.lanes = l, es(t, l);
    }
  }
  function Zc(t, e) {
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
  function Ra() {
    if (Kc) {
      var t = Un;
      if (t !== null) throw t;
    }
  }
  function Oa(t, e, l, n) {
    Kc = !1;
    var a = t.updateQueue;
    yl = !1;
    var u = a.firstBaseUpdate, f = a.lastBaseUpdate, r = a.shared.pending;
    if (r !== null) {
      a.shared.pending = null;
      var h = r, A = h.next;
      h.next = null, f === null ? u = A : f.next = A, f = h;
      var C = t.alternate;
      C !== null && (C = C.updateQueue, r = C.lastBaseUpdate, r !== f && (r === null ? C.firstBaseUpdate = A : r.next = A, C.lastBaseUpdate = h));
    }
    if (u !== null) {
      var U = a.baseState;
      f = 0, C = A = h = null, r = u;
      do {
        var O = r.lane & -536870913, _ = O !== r.lane;
        if (_ ? (vt & O) === O : (n & O) === O) {
          O !== 0 && O === Nn && (Kc = !0), C !== null && (C = C.next = {
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
                yl = !0;
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
          }, C === null ? (A = C = _, h = U) : C = C.next = _, f |= O;
        if (r = r.next, r === null) {
          if (r = a.shared.pending, r === null)
            break;
          _ = r, r = _.next, _.next = null, a.lastBaseUpdate = _, a.shared.pending = null;
        }
      } while (!0);
      C === null && (h = U), a.baseState = h, a.firstBaseUpdate = A, a.lastBaseUpdate = C, u === null && (a.shared.lanes = 0), Tl |= f, t.lanes = f, t.memoizedState = U;
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
  var Bn = g(null), qu = g(0);
  function mr(t, e) {
    t = nl, j(qu, t), j(Bn, e), nl = t | e.baseLanes;
  }
  function Jc() {
    j(qu, nl), j(Bn, Bn.current);
  }
  function Wc() {
    nl = qu.current, w(Bn), w(qu);
  }
  var me = g(null), _e = null;
  function bl(t) {
    var e = t.alternate;
    j(Bt, Bt.current & 1), j(me, t), _e === null && (e === null || Bn.current !== null || e.memoizedState !== null) && (_e = t);
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
  var ke = 0, ct = null, At = null, jt = null, Yu = !1, qn = !1, Pl = !1, ju = 0, za = 0, Ln = null, xh = 0;
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
    return ke = u, ct = e, e.memoizedState = null, e.updateQueue = null, e.lanes = 0, R.H = t === null || t.memoizedState === null ? $r : mf, Pl = !1, u = l(n, a), Pl = !1, qn && (u = hr(
      e,
      l,
      n,
      a
    )), yr(t), u;
  }
  function yr(t) {
    R.H = Ca;
    var e = At !== null && At.next !== null;
    if (ke = 0, jt = At = ct = null, Yu = !1, za = 0, Ln = null, e) throw Error(o(300));
    t === null || Xt || (t = t.dependencies, t !== null && xu(t) && (Xt = !0));
  }
  function hr(t, e, l, n) {
    ct = t;
    var a = 0;
    do {
      if (qn && (Ln = null), za = 0, qn = !1, 25 <= a) throw Error(o(301));
      if (a += 1, jt = At = null, t.updateQueue != null) {
        var u = t.updateQueue;
        u.lastEffect = null, u.events = null, u.stores = null, u.memoCache != null && (u.memoCache.index = 0);
      }
      R.H = Ir, u = e(l, n);
    } while (qn);
    return u;
  }
  function Dh() {
    var t = R.H, e = t.useState()[0];
    return e = typeof e.then == "function" ? _a(e) : e, t = t.useState()[0], (At !== null ? At.memoizedState : null) !== t && (ct.flags |= 1024), e;
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
    ke = 0, jt = At = ct = null, qn = !1, za = ju = 0, Ln = null;
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
  function _a(t) {
    var e = za;
    return za += 1, Ln === null && (Ln = []), t = ir(Ln, t, e), e = ct, (jt === null ? e.memoizedState : jt.next) === null && (e = e.alternate, R.H = e === null || e.memoizedState === null ? $r : mf), t;
  }
  function Gu(t) {
    if (t !== null && typeof t == "object") {
      if (typeof t.then == "function") return _a(t);
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
      var r = f = null, h = null, A = e, C = !1;
      do {
        var U = A.lane & -536870913;
        if (U !== A.lane ? (vt & U) === U : (ke & U) === U) {
          var O = A.revertLane;
          if (O === 0)
            h !== null && (h = h.next = {
              lane: 0,
              revertLane: 0,
              gesture: null,
              action: A.action,
              hasEagerState: A.hasEagerState,
              eagerState: A.eagerState,
              next: null
            }), U === Nn && (C = !0);
          else if ((ke & O) === O) {
            A = A.next, O === Nn && (C = !0);
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
            }, h === null ? (r = h = U, f = u) : h = h.next = U, ct.lanes |= O, Tl |= O;
          U = A.action, Pl && l(u, U), u = A.hasEagerState ? A.eagerState : l(u, U);
        } else
          O = {
            lane: U,
            revertLane: A.revertLane,
            gesture: A.gesture,
            action: A.action,
            hasEagerState: A.hasEagerState,
            eagerState: A.eagerState,
            next: null
          }, h === null ? (r = h = O, f = u) : h = h.next = O, ct.lanes |= U, Tl |= U;
        A = A.next;
      } while (A !== null && A !== e);
      if (h === null ? f = u : h.next = r, !re(u, t.memoizedState) && (Xt = !0, C && (l = Un, l !== null)))
        throw l;
      t.memoizedState = u, t.baseState = f, t.baseQueue = h, n.lastRenderedState = u;
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
    var n = ct, a = qt(), u = ht;
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
      if (n.flags |= 2048, Yn(
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
    var e = Vl(t, 2);
    e !== null && ce(e, t, 2);
  }
  function af(t) {
    var e = te();
    if (typeof t == "function") {
      var l = t;
      if (t = l(), Pl) {
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
  function Nh(t, e, l, n, a) {
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
        var r = l(a, n), h = R.S;
        h !== null && h(f, r), Or(t, e, r);
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
    if (ht) {
      var l = Mt.formState;
      if (l !== null) {
        t: {
          var n = ct;
          if (ht) {
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
    }, n.queue = a, l = Nh.bind(
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
        var n = _a(e);
      } catch (f) {
        throw f === wn ? Uu : f;
      }
    else n = e;
    e = qt();
    var a = e.queue, u = a.dispatch;
    return l !== e.memoizedState && (ct.flags |= 2048, Yn(
      9,
      { destroy: void 0 },
      Uh.bind(null, a, l),
      null
    )), [n, u, t];
  }
  function Uh(t, e) {
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
  function Yn(t, e, l, n) {
    return t = { tag: t, create: l, deps: n, inst: e, next: null }, e = ct.updateQueue, e === null && (e = Xu(), ct.updateQueue = e), l = e.lastEffect, l === null ? e.lastEffect = t.next = t : (n = l.next, l.next = t, t.next = n, e.lastEffect = t), t;
  }
  function Ur() {
    return qt().memoizedState;
  }
  function Vu(t, e, l, n) {
    var a = te();
    ct.flags |= t, a.memoizedState = Yn(
      1 | e,
      { destroy: void 0 },
      l,
      n === void 0 ? null : n
    );
  }
  function Zu(t, e, l, n) {
    var a = qt();
    n = n === void 0 ? null : n;
    var u = a.memoizedState.inst;
    At !== null && n !== null && Fc(n, At.memoizedState.deps) ? a.memoizedState = Yn(e, u, l, n) : (ct.flags |= t, a.memoizedState = Yn(
      1 | e,
      u,
      l,
      n
    ));
  }
  function wr(t, e) {
    Vu(8390656, 8, t, e);
  }
  function cf(t, e) {
    Zu(2048, 8, t, e);
  }
  function wh(t) {
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
    return wh({ ref: e, nextImpl: t }), function() {
      if ((St & 2) !== 0) throw Error(o(440));
      return e.impl.apply(void 0, arguments);
    };
  }
  function Br(t, e) {
    return Zu(4, 2, t, e);
  }
  function qr(t, e) {
    return Zu(4, 4, t, e);
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
    l = l != null ? l.concat([t]) : null, Zu(4, 4, Lr.bind(null, e, t), l);
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
    if (n = t(), Pl) {
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
    return re(l, e) ? l : Bn.current !== null ? (t = of(t, l, n), re(t, e) || (Xt = !0), t) : (ke & 42) === 0 || (ke & 1073741824) !== 0 && (vt & 261930) === 0 ? (Xt = !0, t.memoizedState = l) : (t = Gd(), ct.lanes |= t, Tl |= t, e);
  }
  function Qr(t, e, l, n, a) {
    var u = B.p;
    B.p = u !== 0 && 8 > u ? u : 8;
    var f = R.T, r = {};
    R.T = r, df(t, !1, e, l);
    try {
      var h = a(), A = R.S;
      if (A !== null && A(r, h), h !== null && typeof h == "object" && typeof h.then == "function") {
        var C = Ch(
          h,
          n
        );
        Ma(
          t,
          e,
          C,
          ge(t)
        );
      } else
        Ma(
          t,
          e,
          n,
          ge(t)
        );
    } catch (U) {
      Ma(
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
  function Hh() {
  }
  function sf(t, e, l, n) {
    if (t.tag !== 5) throw Error(o(476));
    var a = Vr(t).queue;
    Qr(
      t,
      a,
      e,
      H,
      l === null ? Hh : function() {
        return Zr(t), l(n);
      }
    );
  }
  function Vr(t) {
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
  function Zr(t) {
    var e = Vr(t);
    e.next === null && (e = t.alternate.memoizedState), Ma(
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
  function Bh(t) {
    for (var e = t.return; e !== null; ) {
      switch (e.tag) {
        case 24:
        case 3:
          var l = ge();
          t = hl(l);
          var n = gl(e, t, l);
          n !== null && (ce(n, e, l), Aa(n, e, l)), e = { cache: Yc() }, t.payload = e;
          return;
      }
      e = e.return;
    }
  }
  function qh(t, e, l) {
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
    Ma(t, e, l, n);
  }
  function Ma(t, e, l, n) {
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
      revertLane: Vf(),
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
    qn = Yu = !0;
    var l = t.pending;
    l === null ? e.next = e : (e.next = l.next, l.next = e), t.pending = e;
  }
  function Fr(t, e, l) {
    if ((l & 4194048) !== 0) {
      var n = e.lanes;
      n &= t.pendingLanes, l |= n, e.lanes = l, es(t, l);
    }
  }
  var Ca = {
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
  Ca.useEffectEvent = wt;
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
      l = l != null ? l.concat([t]) : null, Vu(
        4194308,
        4,
        Lr.bind(null, e, t),
        l
      );
    },
    useLayoutEffect: function(t, e) {
      return Vu(4194308, 4, t, e);
    },
    useInsertionEffect: function(t, e) {
      Vu(4, 2, t, e);
    },
    useMemo: function(t, e) {
      var l = te();
      e = e === void 0 ? null : e;
      var n = t();
      if (Pl) {
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
        if (Pl) {
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
      }, n.queue = t, t = t.dispatch = qh.bind(
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
      if (ht) {
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
      ]), n.flags |= 2048, Yn(
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
      if (ht) {
        var l = we, n = Ue;
        l = (n & ~(1 << 32 - se(n) - 1)).toString(32) + l, e = "_" + e + "R_" + l, l = ju++, 0 < l && (e += "H" + l.toString(32)), e += "_";
      } else
        l = xh++, e = "_" + e + "r_" + l.toString(32) + "_";
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
      return te().memoizedState = Bh.bind(
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
        typeof t == "boolean" ? t : _a(t),
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
        typeof t == "boolean" ? t : _a(t),
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
  var yf = {
    enqueueSetState: function(t, e, l) {
      t = t._reactInternals;
      var n = ge(), a = hl(n);
      a.payload = e, l != null && (a.callback = l), e = gl(t, a, n), e !== null && (ce(e, t, n), Aa(e, t, n));
    },
    enqueueReplaceState: function(t, e, l) {
      t = t._reactInternals;
      var n = ge(), a = hl(n);
      a.tag = 1, a.payload = e, l != null && (a.callback = l), e = gl(t, a, n), e !== null && (ce(e, t, n), Aa(e, t, n));
    },
    enqueueForceUpdate: function(t, e) {
      t = t._reactInternals;
      var l = ge(), n = hl(l);
      n.tag = 2, e != null && (n.callback = e), e = gl(t, n, l), e !== null && (ce(e, t, l), Aa(e, t, l));
    }
  };
  function Pr(t, e, l, n, a, u, f) {
    return t = t.stateNode, typeof t.shouldComponentUpdate == "function" ? t.shouldComponentUpdate(n, u, f) : e.prototype && e.prototype.isPureReactComponent ? !ya(l, n) || !ya(a, u) : !0;
  }
  function td(t, e, l, n) {
    t = e.state, typeof e.componentWillReceiveProps == "function" && e.componentWillReceiveProps(l, n), typeof e.UNSAFE_componentWillReceiveProps == "function" && e.UNSAFE_componentWillReceiveProps(l, n), e.state !== t && yf.enqueueReplaceState(e, e.state, null);
  }
  function tn(t, e) {
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
  function hf(t, e, l) {
    return l = hl(l), l.tag = 3, l.payload = { element: null }, l.callback = function() {
      Ju(t, e);
    }, l;
  }
  function ud(t) {
    return t = hl(t), t.tag = 3, t;
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
  function Lh(t, e, l, n, a) {
    if (l.flags |= 32768, n !== null && typeof n == "object" && typeof n.then == "function") {
      if (e = l.alternate, e !== null && Dn(
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
    if (ht)
      return e = me.current, e !== null ? ((e.flags & 65536) === 0 && (e.flags |= 256), e.flags |= 65536, e.lanes = a, n !== wc && (t = Error(o(422), { cause: n }), ba(Ae(t, l)))) : (n !== wc && (e = Error(o(423), {
        cause: n
      }), ba(
        Ae(e, l)
      )), t = t.current.alternate, t.flags |= 65536, a &= -a, t.lanes |= a, n = Ae(n, l), a = hf(
        t.stateNode,
        n,
        a
      ), Zc(t, a), Ht !== 4 && (Ht = 2)), !1;
    var u = Error(o(520), { cause: n });
    if (u = Ae(u, l), qa === null ? qa = [u] : qa.push(u), Ht !== 4 && (Ht = 2), e === null) return !0;
    n = Ae(n, l), l = e;
    do {
      switch (l.tag) {
        case 3:
          return l.flags |= 65536, t = a & -a, l.lanes |= t, t = hf(l.stateNode, n, t), Zc(l, t), !1;
        case 1:
          if (e = l.type, u = l.stateNode, (l.flags & 128) === 0 && (typeof e.getDerivedStateFromError == "function" || u !== null && typeof u.componentDidCatch == "function" && (Al === null || !Al.has(u))))
            return l.flags |= 65536, a &= -a, l.lanes |= a, a = ud(a), id(
              a,
              t,
              l,
              n
            ), Zc(l, a), !1;
      }
      l = l.return;
    } while (l !== null);
    return !1;
  }
  var gf = Error(o(461)), Xt = !1;
  function $t(t, e, l, n) {
    e.child = t === null ? sr(e, null, l, n) : Il(
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
    return Wl(e), n = $c(
      t,
      e,
      l,
      f,
      u,
      a
    ), r = Ic(), t !== null && !Xt ? (Pc(t, e, a), $e(t, e, a)) : (ht && r && Nc(e), e.flags |= 1, $t(t, e, n, a), e.child);
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
      if (l = l.compare, l = l !== null ? l : ya, l(f, n) && t.ref === e.ref)
        return $e(t, e, a);
    }
    return e.flags |= 1, t = Ze(u, n), t.ref = e.ref, t.return = e, e.child = t;
  }
  function od(t, e, l, n, a) {
    if (t !== null) {
      var u = t.memoizedProps;
      if (ya(u, n) && t.ref === e.ref)
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
  function xa(t, e) {
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
    }, t !== null && Nu(e, null), Jc(), vr(e), t !== null && Dn(t, e, n, !0), e.childLanes = a, null;
  }
  function Wu(t, e) {
    return e = Fu(
      { mode: e.mode, children: e.children },
      t.mode
    ), e.ref = t.ref, t.child = e, e.return = t, e;
  }
  function dd(t, e, l) {
    return Il(e, t.child, null, l), t = Wu(e, e.pendingProps), t.flags |= 2, ve(e), e.memoizedState = null, t;
  }
  function Yh(t, e, l) {
    var n = e.pendingProps, a = (e.flags & 128) !== 0;
    if (e.flags &= -129, t === null) {
      if (ht) {
        if (n.mode === "hidden")
          return t = Wu(e, n), e.lanes = 536870912, xa(null, t);
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
      else if (Xt || Dn(t, e, l, !1), a = (l & t.childLanes) !== 0, Xt || a) {
        if (n = Mt, n !== null && (f = ls(n, l), f !== 0 && f !== u.retryLane))
          throw u.retryLane = f, Vl(t, f), ce(n, t, f), gf;
        ui(), e = dd(
          t,
          e,
          l
        );
      } else
        t = u.treeContext, xt = Me(f.nextSibling), kt = e, ht = !0, dl = null, ze = !1, t !== null && Is(e, t), e = Wu(e, n), e.flags |= 4096;
      return e;
    }
    return t = Ze(t.child, {
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
    return Wl(e), l = $c(
      t,
      e,
      l,
      n,
      void 0,
      a
    ), n = Ic(), t !== null && !Xt ? (Pc(t, e, a), $e(t, e, a)) : (ht && n && Nc(e), e.flags |= 1, $t(t, e, l, a), e.child);
  }
  function md(t, e, l, n, a, u) {
    return Wl(e), e.updateQueue = null, l = hr(
      e,
      n,
      l,
      a
    ), yr(t), n = Ic(), t !== null && !Xt ? (Pc(t, e, u), $e(t, e, u)) : (ht && n && Nc(e), e.flags |= 1, $t(t, e, l, u), e.child);
  }
  function vd(t, e, l, n, a) {
    if (Wl(e), e.stateNode === null) {
      var u = _n, f = l.contextType;
      typeof f == "object" && f !== null && (u = Ft(f)), u = new l(n, u), e.memoizedState = u.state !== null && u.state !== void 0 ? u.state : null, u.updater = yf, e.stateNode = u, u._reactInternals = e, u = e.stateNode, u.props = n, u.state = e.memoizedState, u.refs = {}, Qc(e), f = l.contextType, u.context = typeof f == "object" && f !== null ? Ft(f) : _n, u.state = e.memoizedState, f = l.getDerivedStateFromProps, typeof f == "function" && (vf(
        e,
        l,
        f,
        n
      ), u.state = e.memoizedState), typeof l.getDerivedStateFromProps == "function" || typeof u.getSnapshotBeforeUpdate == "function" || typeof u.UNSAFE_componentWillMount != "function" && typeof u.componentWillMount != "function" || (f = u.state, typeof u.componentWillMount == "function" && u.componentWillMount(), typeof u.UNSAFE_componentWillMount == "function" && u.UNSAFE_componentWillMount(), f !== u.state && yf.enqueueReplaceState(u, u.state, null), Oa(e, n, u, a), Ra(), u.state = e.memoizedState), typeof u.componentDidMount == "function" && (e.flags |= 4194308), n = !0;
    } else if (t === null) {
      u = e.stateNode;
      var r = e.memoizedProps, h = tn(l, r);
      u.props = h;
      var A = u.context, C = l.contextType;
      f = _n, typeof C == "object" && C !== null && (f = Ft(C));
      var U = l.getDerivedStateFromProps;
      C = typeof U == "function" || typeof u.getSnapshotBeforeUpdate == "function", r = e.pendingProps !== r, C || typeof u.UNSAFE_componentWillReceiveProps != "function" && typeof u.componentWillReceiveProps != "function" || (r || A !== f) && td(
        e,
        u,
        n,
        f
      ), yl = !1;
      var O = e.memoizedState;
      u.state = O, Oa(e, n, u, a), Ra(), A = e.memoizedState, r || O !== A || yl ? (typeof U == "function" && (vf(
        e,
        l,
        U,
        n
      ), A = e.memoizedState), (h = yl || Pr(
        e,
        l,
        h,
        n,
        O,
        A,
        f
      )) ? (C || typeof u.UNSAFE_componentWillMount != "function" && typeof u.componentWillMount != "function" || (typeof u.componentWillMount == "function" && u.componentWillMount(), typeof u.UNSAFE_componentWillMount == "function" && u.UNSAFE_componentWillMount()), typeof u.componentDidMount == "function" && (e.flags |= 4194308)) : (typeof u.componentDidMount == "function" && (e.flags |= 4194308), e.memoizedProps = n, e.memoizedState = A), u.props = n, u.state = A, u.context = f, n = h) : (typeof u.componentDidMount == "function" && (e.flags |= 4194308), n = !1);
    } else {
      u = e.stateNode, Vc(t, e), f = e.memoizedProps, C = tn(l, f), u.props = C, U = e.pendingProps, O = u.context, A = l.contextType, h = _n, typeof A == "object" && A !== null && (h = Ft(A)), r = l.getDerivedStateFromProps, (A = typeof r == "function" || typeof u.getSnapshotBeforeUpdate == "function") || typeof u.UNSAFE_componentWillReceiveProps != "function" && typeof u.componentWillReceiveProps != "function" || (f !== U || O !== h) && td(
        e,
        u,
        n,
        h
      ), yl = !1, O = e.memoizedState, u.state = O, Oa(e, n, u, a), Ra();
      var _ = e.memoizedState;
      f !== U || O !== _ || yl || t !== null && t.dependencies !== null && xu(t.dependencies) ? (typeof r == "function" && (vf(
        e,
        l,
        r,
        n
      ), _ = e.memoizedState), (C = yl || Pr(
        e,
        l,
        C,
        n,
        O,
        _,
        h
      ) || t !== null && t.dependencies !== null && xu(t.dependencies)) ? (A || typeof u.UNSAFE_componentWillUpdate != "function" && typeof u.componentWillUpdate != "function" || (typeof u.componentWillUpdate == "function" && u.componentWillUpdate(n, _, h), typeof u.UNSAFE_componentWillUpdate == "function" && u.UNSAFE_componentWillUpdate(
        n,
        _,
        h
      )), typeof u.componentDidUpdate == "function" && (e.flags |= 4), typeof u.getSnapshotBeforeUpdate == "function" && (e.flags |= 1024)) : (typeof u.componentDidUpdate != "function" || f === t.memoizedProps && O === t.memoizedState || (e.flags |= 4), typeof u.getSnapshotBeforeUpdate != "function" || f === t.memoizedProps && O === t.memoizedState || (e.flags |= 1024), e.memoizedProps = n, e.memoizedState = _), u.props = n, u.state = _, u.context = h, n = C) : (typeof u.componentDidUpdate != "function" || f === t.memoizedProps && O === t.memoizedState || (e.flags |= 4), typeof u.getSnapshotBeforeUpdate != "function" || f === t.memoizedProps && O === t.memoizedState || (e.flags |= 1024), n = !1);
    }
    return u = n, ku(t, e), n = (e.flags & 128) !== 0, u || n ? (u = e.stateNode, l = n && typeof l.getDerivedStateFromError != "function" ? null : u.render(), e.flags |= 1, t !== null && n ? (e.child = Il(
      e,
      t.child,
      null,
      a
    ), e.child = Il(
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
  function yd(t, e, l, n) {
    return Kl(), e.flags |= 256, $t(t, e, l, n), e.child;
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
    return t = t !== null ? t.childLanes & ~l : 0, e && (t |= he), t;
  }
  function hd(t, e, l) {
    var n = e.pendingProps, a = !1, u = (e.flags & 128) !== 0, f;
    if ((f = u) || (f = t !== null && t.memoizedState === null ? !1 : (Bt.current & 2) !== 0), f && (a = !0, e.flags &= -129), f = (e.flags & 32) !== 0, e.flags &= -33, t === null) {
      if (ht) {
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
      ), e.memoizedState = Sf, xa(null, n)) : (bl(e), Tf(e, r));
    }
    var h = t.memoizedState;
    if (h !== null && (r = h.dehydrated, r !== null)) {
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
        ), r.flags |= 2, n.return = e, r.return = e, n.sibling = r, e.child = n, Il(
          e,
          t.child,
          null,
          l
        ), n = e.child, n.memoizedState = pf(l), n.childLanes = Ef(
          t,
          f,
          l
        ), e.memoizedState = Sf, e = xa(null, n));
      else if (bl(e), no(r)) {
        if (f = r.nextSibling && r.nextSibling.dataset, f) var A = f.dgst;
        f = A, n = Error(o(419)), n.stack = "", n.digest = f, ba({ value: n, source: null, stack: null }), e = Af(
          t,
          e,
          l
        );
      } else if (Xt || Dn(t, e, l, !1), f = (l & t.childLanes) !== 0, Xt || f) {
        if (f = Mt, f !== null && (n = ls(f, l), n !== 0 && n !== h.retryLane))
          throw h.retryLane = n, Vl(t, n), ce(f, t, n), gf;
        lo(r) || ui(), e = Af(
          t,
          e,
          l
        );
      } else
        lo(r) ? (e.flags |= 192, e.child = t.child, e = null) : (t = h.treeContext, xt = Me(
          r.nextSibling
        ), kt = e, ht = !0, dl = null, ze = !1, t !== null && Is(e, t), e = Tf(
          e,
          n.children
        ), e.flags |= 4096);
      return e;
    }
    return a ? (Sl(), r = n.fallback, a = e.mode, h = t.child, A = h.sibling, n = Ze(h, {
      mode: "hidden",
      children: n.children
    }), n.subtreeFlags = h.subtreeFlags & 65011712, A !== null ? r = Ze(
      A,
      r
    ) : (r = Zl(
      r,
      a,
      l,
      null
    ), r.flags |= 2), r.return = e, n.return = e, n.sibling = r, e.child = n, xa(null, n), n = e.child, r = t.child.memoizedState, r === null ? r = pf(l) : (a = r.cachePool, a !== null ? (h = Yt._currentValue, a = a.parent !== h ? { parent: h, pool: h } : a) : a = ar(), r = {
      baseLanes: r.baseLanes | l,
      cachePool: a
    }), n.memoizedState = r, n.childLanes = Ef(
      t,
      f,
      l
    ), e.memoizedState = Sf, xa(t.child, n)) : (bl(e), l = t.child, t = l.sibling, l = Ze(l, {
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
    return Il(e, t.child, null, l), t = Tf(
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
    if (r ? (f = f & 1 | 2, e.flags |= 128) : f &= 1, j(Bt, f), $t(t, e, n, l), n = ht ? ga : 0, !r && t !== null && (t.flags & 128) !== 0)
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
        if (Dn(
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
      for (t = e.child, l = Ze(t, t.pendingProps), e.child = l, l.return = e; t.sibling !== null; )
        t = t.sibling, l = l.sibling = Ze(t, t.pendingProps), l.return = e;
      l.sibling = null;
    }
    return e.child;
  }
  function Of(t, e) {
    return (t.lanes & e) !== 0 ? !0 : (t = t.dependencies, !!(t !== null && xu(t)));
  }
  function jh(t, e, l) {
    switch (e.tag) {
      case 3:
        Jt(e, e.stateNode.containerInfo), vl(e, Yt, t.memoizedState.cache), Kl();
        break;
      case 27:
      case 5:
        Ll(e);
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
          return n.dehydrated !== null ? (bl(e), e.flags |= 128, null) : (l & e.child.childLanes) !== 0 ? hd(t, e, l) : (bl(e), t = $e(
            t,
            e,
            l
          ), t !== null ? t.sibling : null);
        bl(e);
        break;
      case 19:
        var a = (t.flags & 128) !== 0;
        if (n = (l & e.childLanes) !== 0, n || (Dn(
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
          return Xt = !1, jh(
            t,
            e,
            l
          );
        Xt = (t.flags & 131072) !== 0;
      }
    else
      Xt = !1, ht && (e.flags & 1048576) !== 0 && $s(e, ga, e.index);
    switch (e.lanes = 0, e.tag) {
      case 16:
        t: {
          var n = e.pendingProps;
          if (t = Fl(e.elementType), e.type = t, typeof t == "function")
            Cc(t) ? (n = tn(t, n), e.tag = 1, e = vd(
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
              } else if (a === Z) {
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
        return n = e.type, a = tn(
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
          a = u.element, Vc(t, e), Oa(e, n, null, l);
          var f = e.memoizedState;
          if (n = f.cache, vl(e, Yt, n), n !== u.cache && Lc(
            e,
            [Yt],
            l,
            !0
          ), Ra(), n = f.element, u.isDehydrated)
            if (u = {
              element: n,
              isDehydrated: !1,
              cache: f.cache
            }, e.updateQueue.baseState = u, e.memoizedState = u, e.flags & 256) {
              e = yd(
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
              ), ba(a), e = yd(
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
              for (xt = Me(t.firstChild), kt = e, ht = !0, dl = null, ze = !0, l = sr(
                e,
                null,
                n,
                l
              ), e.child = l; l; )
                l.flags = l.flags & -3 | 4096, l = l.sibling;
            }
          else {
            if (Kl(), n === a) {
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
        )) ? e.memoizedState = l : ht || (l = e.type, t = e.pendingProps, n = di(
          nt.current
        ).createElement(l), n[Wt] = e, n[ee] = t, It(n, l, t), Zt(n), e.stateNode = n) : e.memoizedState = xm(
          e.type,
          t.memoizedProps,
          e.pendingProps,
          t.memoizedState
        ), null;
      case 27:
        return Ll(e), t === null && ht && (n = e.stateNode = _m(
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
        return t === null && ht && ((a = n = xt) && (n = h0(
          n,
          e.type,
          e.pendingProps,
          ze
        ), n !== null ? (e.stateNode = n, kt = e, xt = Me(n.firstChild), ze = !1, a = !0) : a = !1), a || ml(e)), Ll(e), a = e.type, u = e.pendingProps, f = t !== null ? t.memoizedProps : null, n = u.children, Pf(a, u) ? n = null : f !== null && Pf(a, f) && (e.flags |= 32), e.memoizedState !== null && (a = $c(
          t,
          e,
          Dh,
          null,
          null,
          l
        ), Za._currentValue = a), ku(t, e), $t(t, e, n, l), e.child;
      case 6:
        return t === null && ht && ((t = l = xt) && (l = g0(
          l,
          e.pendingProps,
          ze
        ), l !== null ? (e.stateNode = l, kt = e, xt = null, t = !0) : t = !1), t || ml(e)), null;
      case 13:
        return hd(t, e, l);
      case 4:
        return Jt(
          e,
          e.stateNode.containerInfo
        ), n = e.pendingProps, t === null ? e.child = Il(
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
        return a = e.type._context, n = e.pendingProps.children, Wl(e), a = Ft(a), n = n(a), e.flags |= 1, $t(t, e, n, l), e.child;
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
        return Yh(t, e, l);
      case 22:
        return sd(
          t,
          e,
          l,
          e.pendingProps
        );
      case 24:
        return Wl(e), n = Ft(Yt), t === null ? (a = Xc(), a === null && (a = Mt, u = Yc(), a.pooledCache = u, u.refCount++, u !== null && (a.pooledCacheLanes |= l), a = u), e.memoizedState = { parent: n, cache: a }, Qc(e), vl(e, Yt, a)) : ((t.lanes & l) !== 0 && (Vc(t, e), Oa(e, null, null, l), Ra()), a = t.memoizedState, u = e.memoizedState, a.parent !== n ? (a = { parent: n, cache: n }, e.memoizedState = a, e.lanes === 0 && (e.memoizedState = e.updateQueue.baseState = a), vl(e, Yt, n)) : (n = u.cache, vl(e, Yt, n), n !== a.cache && Lc(
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
          throw $l = wu, Gc;
    } else t.flags &= -16777217;
  }
  function pd(t, e) {
    if (e.type !== "stylesheet" || (e.state.loading & 4) !== 0)
      t.flags &= -16777217;
    else if (t.flags |= 16777216, !Hm(e))
      if (Kd()) t.flags |= 8192;
      else
        throw $l = wu, Gc;
  }
  function $u(t, e) {
    e !== null && (t.flags |= 4), t.flags & 16384 && (e = t.tag !== 22 ? Po() : 536870912, t.lanes |= e, Qn |= e);
  }
  function Da(t, e) {
    if (!ht)
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
  function Xh(t, e, l) {
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
        return l = e.stateNode, n = null, t !== null && (n = t.memoizedState.cache), e.memoizedState.cache !== n && (e.flags |= 2048), We(Yt), Ut(), l.pendingContext && (l.context = l.pendingContext, l.pendingContext = null), (t === null || t.child === null) && (xn(e) ? Ie(e) : t === null || t.memoizedState.isDehydrated && (e.flags & 256) === 0 || (e.flags |= 1024, Hc())), Dt(e), null;
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
        if (mn(e), l = nt.current, a = e.type, t !== null && e.stateNode != null)
          t.memoizedProps !== n && Ie(e);
        else {
          if (!n) {
            if (e.stateNode === null)
              throw Error(o(166));
            return Dt(e), null;
          }
          t = K.current, xn(e) ? Ps(e) : (t = _m(a, n, l), e.stateNode = t, Ie(e));
        }
        return Dt(e), null;
      case 5:
        if (mn(e), a = e.type, t !== null && e.stateNode != null)
          t.memoizedProps !== n && Ie(e);
        else {
          if (!n) {
            if (e.stateNode === null)
              throw Error(o(166));
            return Dt(e), null;
          }
          if (u = K.current, xn(e))
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
          if (t = nt.current, xn(e)) {
            if (t = e.stateNode, l = e.memoizedProps, n = null, a = kt, a !== null)
              switch (a.tag) {
                case 27:
                case 5:
                  n = a.memoizedProps;
              }
            t[Wt] = e, t = !!(t.nodeValue === l || n !== null && n.suppressHydrationWarning === !0 || hm(t.nodeValue, l)), t || ml(e, !0);
          } else
            t = di(t).createTextNode(
              n
            ), t[Wt] = e, e.stateNode = t;
        }
        return Dt(e), null;
      case 31:
        if (l = e.memoizedState, t === null || t.memoizedState !== null) {
          if (n = xn(e), l !== null) {
            if (t === null) {
              if (!n) throw Error(o(318));
              if (t = e.memoizedState, t = t !== null ? t.dehydrated : null, !t) throw Error(o(557));
              t[Wt] = e;
            } else
              Kl(), (e.flags & 128) === 0 && (e.memoizedState = null), e.flags |= 4;
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
          if (a = xn(e), n !== null && n.dehydrated !== null) {
            if (t === null) {
              if (!a) throw Error(o(318));
              if (a = e.memoizedState, a = a !== null ? a.dehydrated : null, !a) throw Error(o(317));
              a[Wt] = e;
            } else
              Kl(), (e.flags & 128) === 0 && (e.memoizedState = null), e.flags |= 4;
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
          if (a) Da(n, !1);
          else {
            if (Ht !== 0 || t !== null && (t.flags & 128) !== 0)
              for (t = e.child; t !== null; ) {
                if (u = Lu(t), u !== null) {
                  for (e.flags |= 128, Da(n, !1), t = u.updateQueue, e.updateQueue = t, $u(e, t), e.subtreeFlags = 0, t = l, l = e.child; l !== null; )
                    Ws(l, t), l = l.sibling;
                  return j(
                    Bt,
                    Bt.current & 1 | 2
                  ), ht && Ke(e, n.treeForkCount), e.child;
                }
                t = t.sibling;
              }
            n.tail !== null && fe() > li && (e.flags |= 128, a = !0, Da(n, !1), e.lanes = 4194304);
          }
        else {
          if (!a)
            if (t = Lu(u), t !== null) {
              if (e.flags |= 128, a = !0, t = t.updateQueue, e.updateQueue = t, $u(e, t), Da(n, !0), n.tail === null && n.tailMode === "hidden" && !u.alternate && !ht)
                return Dt(e), null;
            } else
              2 * fe() - n.renderingStartTime > li && l !== 536870912 && (e.flags |= 128, a = !0, Da(n, !1), e.lanes = 4194304);
          n.isBackwards ? (u.sibling = e.child, e.child = u) : (t = n.last, t !== null ? t.sibling = u : e.child = u, n.last = u);
        }
        return n.tail !== null ? (t = n.tail, n.rendering = t, n.tail = t.sibling, n.renderingStartTime = fe(), t.sibling = null, l = Bt.current, j(
          Bt,
          a ? l & 1 | 2 : l & 1
        ), ht && Ke(e, n.treeForkCount), t) : (Dt(e), null);
      case 22:
      case 23:
        return ve(e), Wc(), n = e.memoizedState !== null, t !== null ? t.memoizedState !== null !== n && (e.flags |= 8192) : n && (e.flags |= 8192), n ? (l & 536870912) !== 0 && (e.flags & 128) === 0 && (Dt(e), e.subtreeFlags & 6 && (e.flags |= 8192)) : Dt(e), l = e.updateQueue, l !== null && $u(e, l.retryQueue), l = null, t !== null && t.memoizedState !== null && t.memoizedState.cachePool !== null && (l = t.memoizedState.cachePool.pool), n = null, e.memoizedState !== null && e.memoizedState.cachePool !== null && (n = e.memoizedState.cachePool.pool), n !== l && (e.flags |= 2048), t !== null && w(kl), null;
      case 24:
        return l = null, t !== null && (l = t.memoizedState.cache), e.memoizedState.cache !== l && (e.flags |= 2048), We(Yt), Dt(e), null;
      case 25:
        return null;
      case 30:
        return null;
    }
    throw Error(o(156, e.tag));
  }
  function Gh(t, e) {
    switch (Uc(e), e.tag) {
      case 1:
        return t = e.flags, t & 65536 ? (e.flags = t & -65537 | 128, e) : null;
      case 3:
        return We(Yt), Ut(), t = e.flags, (t & 65536) !== 0 && (t & 128) === 0 ? (e.flags = t & -65537 | 128, e) : null;
      case 26:
      case 27:
      case 5:
        return mn(e), null;
      case 31:
        if (e.memoizedState !== null) {
          if (ve(e), e.alternate === null)
            throw Error(o(340));
          Kl();
        }
        return t = e.flags, t & 65536 ? (e.flags = t & -65537 | 128, e) : null;
      case 13:
        if (ve(e), t = e.memoizedState, t !== null && t.dehydrated !== null) {
          if (e.alternate === null)
            throw Error(o(340));
          Kl();
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
        return ve(e), Wc(), t !== null && w(kl), t = e.flags, t & 65536 ? (e.flags = t & -65537 | 128, e) : null;
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
        mn(e);
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
        ve(e), Wc(), t !== null && w(kl);
        break;
      case 24:
        We(Yt);
    }
  }
  function Na(t, e) {
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
              var h = l, A = r;
              try {
                A();
              } catch (C) {
                Et(
                  a,
                  h,
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
    l.props = tn(
      t.type,
      t.memoizedProps
    ), l.state = t.memoizedState;
    try {
      l.componentWillUnmount();
    } catch (n) {
      Et(t, e, n);
    }
  }
  function Ua(t, e) {
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
  function Qh(t, e) {
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
            var f = 0, r = -1, h = -1, A = 0, C = 0, U = t, O = null;
            e: for (; ; ) {
              for (var _; U !== l || a !== 0 && U.nodeType !== 3 || (r = f + a), U !== u || n !== 0 && U.nodeType !== 3 || (h = f + n), U.nodeType === 3 && (f += U.nodeValue.length), (_ = U.firstChild) !== null; )
                O = U, U = _;
              for (; ; ) {
                if (U === t) break e;
                if (O === l && ++A === a && (r = f), O === u && ++C === n && (h = f), (_ = U.nextSibling) !== null) break;
                U = O, O = U.parentNode;
              }
              U = _;
            }
            l = r === -1 || h === -1 ? null : { start: r, end: h };
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
                  var J = tn(
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
        el(t, l), n & 4 && Na(5, l);
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
            var a = tn(
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
        n & 64 && Td(l), n & 512 && Ua(l, l.return);
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
        el(t, l), e === null && n & 4 && Rd(l), n & 512 && Ua(l, l.return);
        break;
      case 12:
        el(t, l);
        break;
      case 31:
        el(t, l), n & 4 && Dd(t, l);
        break;
      case 13:
        el(t, l), n & 4 && Nd(t, l), n & 64 && (t = l.memoizedState, t !== null && (t = t.dehydrated, t !== null && (l = Ih.bind(
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
        oe.onCommitFiberUnmount(na, l);
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
        ), Ga(l.stateNode), Nt = n, ne = a;
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
        ), $n(t)) : Tm(Nt, l.stateNode));
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
        $n(t);
      } catch (l) {
        Et(e, e.return, l);
      }
    }
  }
  function Nd(t, e) {
    if (e.memoizedState === null && (t = e.alternate, t !== null && (t = t.memoizedState, t !== null && (t = t.dehydrated, t !== null))))
      try {
        $n(t);
      } catch (l) {
        Et(e, e.return, l);
      }
  }
  function Vh(t) {
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
    var l = Vh(t);
    e.forEach(function(n) {
      if (!l.has(n)) {
        l.add(n);
        var a = Ph.bind(null, t, n);
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
        ae(e, t), ue(t), n & 4 && (pl(3, t, t.return), Na(3, t), pl(5, t, t.return));
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
                      u = a.getElementsByTagName("title")[0], (!u || u[ia] || u[Wt] || u.namespaceURI === "http://www.w3.org/2000/svg" || u.hasAttribute("itemprop")) && (u = a.createElement(n), a.head.insertBefore(
                        u,
                        a.querySelector("head > title")
                      )), It(u, n, l), u[Wt] = t, Zt(u), n = u;
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
                  u[Wt] = t, Zt(u), n = u;
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
            pn(a, "");
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
        if (yi = null, a = De, De = mi(e.containerInfo), ae(e, t), De = a, ue(t), n & 4 && l !== null && l.memoizedState.isDehydrated)
          try {
            $n(e.containerInfo);
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
        var h = l !== null && l.memoizedState !== null, A = Pe, C = Gt;
        if (Pe = A || a, Gt = C || h, ae(e, t), Gt = C, Pe = A, ue(t), n & 8192)
          t: for (e = t.stateNode, e._visibility = a ? e._visibility & -2 : e._visibility | 1, a && (l === null || h || Pe || Gt || en(t)), l = null, e = t; ; ) {
            if (e.tag === 5 || e.tag === 26) {
              if (l === null) {
                h = l = e;
                try {
                  if (u = h.stateNode, a)
                    f = u.style, typeof f.setProperty == "function" ? f.setProperty("display", "none", "important") : f.display = "none";
                  else {
                    r = h.stateNode;
                    var U = h.memoizedProps.style, O = U != null && U.hasOwnProperty("display") ? U.display : null;
                    r.style.display = O == null || typeof O == "boolean" ? "" : ("" + O).trim();
                  }
                } catch (J) {
                  Et(h, h.return, J);
                }
              }
            } else if (e.tag === 6) {
              if (l === null) {
                h = e;
                try {
                  h.stateNode.nodeValue = a ? "" : h.memoizedProps;
                } catch (J) {
                  Et(h, h.return, J);
                }
              }
            } else if (e.tag === 18) {
              if (l === null) {
                h = e;
                try {
                  var _ = h.stateNode;
                  a ? Am(_, !0) : Am(h.stateNode, !1);
                } catch (J) {
                  Et(h, h.return, J);
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
            l.flags & 32 && (pn(f, ""), l.flags &= -33);
            var r = Mf(t);
            Iu(t, r, f);
            break;
          case 3:
          case 4:
            var h = l.stateNode.containerInfo, A = Mf(t);
            Cf(
              t,
              A,
              h
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
  function en(t) {
    for (t = t.child; t !== null; ) {
      var e = t;
      switch (e.tag) {
        case 0:
        case 11:
        case 14:
        case 15:
          pl(4, e, e.return), en(e);
          break;
        case 1:
          He(e, e.return);
          var l = e.stateNode;
          typeof l.componentWillUnmount == "function" && Ad(
            e,
            e.return,
            l
          ), en(e);
          break;
        case 27:
          Ga(e.stateNode);
        case 26:
        case 5:
          He(e, e.return), en(e);
          break;
        case 22:
          e.memoizedState === null && en(e);
          break;
        case 30:
          en(e);
          break;
        default:
          en(e);
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
          ), Na(4, u);
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
              var h = a.shared.hiddenCallbacks;
              if (h !== null)
                for (a.shared.hiddenCallbacks = null, a = 0; a < h.length; a++)
                  rr(h[a], r);
            } catch (A) {
              Et(n, n.return, A);
            }
          }
          l && f & 64 && Td(u), Ua(u, u.return);
          break;
        case 27:
          zd(u);
        case 26:
        case 5:
          ll(
            a,
            u,
            l
          ), l && n === null && f & 4 && Rd(u), Ua(u, u.return);
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
          ), Ua(u, u.return);
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
    t !== null && t.memoizedState !== null && t.memoizedState.cachePool !== null && (l = t.memoizedState.cachePool.pool), t = null, e.memoizedState !== null && e.memoizedState.cachePool !== null && (t = e.memoizedState.cachePool.pool), t !== l && (t != null && t.refCount++, l != null && Sa(l));
  }
  function Nf(t, e) {
    t = null, e.alternate !== null && (t = e.alternate.memoizedState.cache), e = e.memoizedState.cache, e !== t && (e.refCount++, t != null && Sa(t));
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
        ), a & 2048 && Na(9, e);
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
        ), a & 2048 && (t = null, e.alternate !== null && (t = e.alternate.memoizedState.cache), e = e.memoizedState.cache, e !== t && (e.refCount++, t != null && Sa(t)));
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
          } catch (h) {
            Et(e, e.return, h);
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
        ) : wa(t, e) : u._visibility & 2 ? Ne(
          t,
          e,
          l,
          n
        ) : (u._visibility |= 2, jn(
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
  function jn(t, e, l, n, a) {
    for (a = a && ((e.subtreeFlags & 10256) !== 0 || !1), e = e.child; e !== null; ) {
      var u = t, f = e, r = l, h = n, A = f.flags;
      switch (f.tag) {
        case 0:
        case 11:
        case 15:
          jn(
            u,
            f,
            r,
            h,
            a
          ), Na(8, f);
          break;
        case 23:
          break;
        case 22:
          var C = f.stateNode;
          f.memoizedState !== null ? C._visibility & 2 ? jn(
            u,
            f,
            r,
            h,
            a
          ) : wa(
            u,
            f
          ) : (C._visibility |= 2, jn(
            u,
            f,
            r,
            h,
            a
          )), a && A & 2048 && Df(
            f.alternate,
            f
          );
          break;
        case 24:
          jn(
            u,
            f,
            r,
            h,
            a
          ), a && A & 2048 && Nf(f.alternate, f);
          break;
        default:
          jn(
            u,
            f,
            r,
            h,
            a
          );
      }
      e = e.sibling;
    }
  }
  function wa(t, e) {
    if (e.subtreeFlags & 10256)
      for (e = e.child; e !== null; ) {
        var l = t, n = e, a = n.flags;
        switch (n.tag) {
          case 22:
            wa(l, n), a & 2048 && Df(
              n.alternate,
              n
            );
            break;
          case 24:
            wa(l, n), a & 2048 && Nf(n.alternate, n);
            break;
          default:
            wa(l, n);
        }
        e = e.sibling;
      }
  }
  var Ha = 8192;
  function Xn(t, e, l) {
    if (t.subtreeFlags & Ha)
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
        Xn(
          t,
          e,
          l
        ), t.flags & Ha && t.memoizedState !== null && x0(
          l,
          De,
          t.memoizedState,
          t.memoizedProps
        );
        break;
      case 5:
        Xn(
          t,
          e,
          l
        );
        break;
      case 3:
      case 4:
        var n = De;
        De = mi(t.stateNode.containerInfo), Xn(
          t,
          e,
          l
        ), De = n;
        break;
      case 22:
        t.memoizedState === null && (n = t.alternate, n !== null && n.memoizedState !== null ? (n = Ha, Ha = 16777216, Xn(
          t,
          e,
          l
        ), Ha = n) : Xn(
          t,
          e,
          l
        ));
        break;
      default:
        Xn(
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
  function Ba(t) {
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
        Ba(t), t.flags & 2048 && pl(9, t, t.return);
        break;
      case 3:
        Ba(t);
        break;
      case 12:
        Ba(t);
        break;
      case 22:
        var e = t.stateNode;
        t.memoizedState !== null && e._visibility & 2 && (t.return === null || t.return.tag !== 13) ? (e._visibility &= -3, ti(t)) : Ba(t);
        break;
      default:
        Ba(t);
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
          Sa(l.memoizedState.cache);
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
  var Zh = {
    getCacheForType: function(t) {
      var e = Ft(Yt), l = e.data.get(t);
      return l === void 0 && (l = t(), e.data.set(t, l)), l;
    },
    cacheSignal: function() {
      return Ft(Yt).controller.signal;
    }
  }, Kh = typeof WeakMap == "function" ? WeakMap : Map, St = 0, Mt = null, st = null, vt = 0, pt = 0, ye = null, El = !1, Gn = !1, Uf = !1, nl = 0, Ht = 0, Tl = 0, ln = 0, wf = 0, he = 0, Qn = 0, qa = null, ie = null, Hf = !1, ei = 0, jd = 0, li = 1 / 0, ni = null, Al = null, Qt = 0, Rl = null, Vn = null, al = 0, Bf = 0, qf = null, Xd = null, La = 0, Lf = null;
  function ge() {
    return (St & 2) !== 0 && vt !== 0 ? vt & -vt : R.T !== null ? Vf() : ns();
  }
  function Gd() {
    if (he === 0)
      if ((vt & 536870912) === 0 || ht) {
        var t = ru;
        ru <<= 1, (ru & 3932160) === 0 && (ru = 262144), he = t;
      } else he = 536870912;
    return t = me.current, t !== null && (t.flags |= 32), he;
  }
  function ce(t, e, l) {
    (t === Mt && (pt === 2 || pt === 9) || t.cancelPendingCommit !== null) && (Zn(t, 0), Ol(
      t,
      vt,
      he,
      !1
    )), ua(t, l), ((St & 2) === 0 || t !== Mt) && (t === Mt && ((St & 2) === 0 && (ln |= l), Ht === 4 && Ol(
      t,
      vt,
      he,
      !1
    )), Be(t));
  }
  function Qd(t, e, l) {
    if ((St & 6) !== 0) throw Error(o(327));
    var n = !l && (e & 127) === 0 && (e & t.expiredLanes) === 0 || aa(t, e), a = n ? kh(t, e) : jf(t, e, !0), u = n;
    do {
      if (a === 0) {
        Gn && !n && Ol(t, e, 0, !1);
        break;
      } else {
        if (l = t.current.alternate, u && !Jh(l)) {
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
              a = qa;
              var h = r.current.memoizedState.isDehydrated;
              if (h && (Zn(r, f).flags |= 256), f = jf(
                r,
                f,
                !1
              ), f !== 2) {
                if (Uf && !h) {
                  r.errorRecoveryDisabledLanes |= u, ln |= u, a = 4;
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
                he,
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
              he,
              !El
            ), mu(n, 0, !0) !== 0) break t;
            al = e, n.timeoutHandle = pm(
              Vd.bind(
                null,
                n,
                l,
                ie,
                ni,
                Hf,
                e,
                he,
                ln,
                Qn,
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
          Vd(
            n,
            l,
            ie,
            ni,
            Hf,
            e,
            he,
            ln,
            Qn,
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
  function Vd(t, e, l, n, a, u, f, r, h, A, C, U, O, _) {
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
            h,
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
      h
    );
  }
  function Jh(t) {
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
    e &= ~wf, e &= ~ln, t.suspendedLanes |= e, t.pingedLanes &= ~e, n && (t.warmLanes |= e), n = t.expirationTimes;
    for (var a = e; 0 < a; ) {
      var u = 31 - se(a), f = 1 << u;
      n[u] = -1, a &= ~f;
    }
    l !== 0 && ts(t, l, e);
  }
  function ai() {
    return (St & 6) === 0 ? (Ya(0), !1) : !0;
  }
  function Yf() {
    if (st !== null) {
      if (pt === 0)
        var t = st.return;
      else
        t = st, Je = Jl = null, tf(t), Hn = null, Ea = 0, t = st;
      for (; t !== null; )
        Ed(t.alternate, t), t = t.return;
      st = null;
    }
  }
  function Zn(t, e) {
    var l = t.timeoutHandle;
    l !== -1 && (t.timeoutHandle = -1, m0(l)), l = t.cancelPendingCommit, l !== null && (t.cancelPendingCommit = null, l()), al = 0, Yf(), Mt = t, st = l = Ze(t.current, null), vt = e, pt = 0, ye = null, El = !1, Gn = aa(t, e), Uf = !1, Qn = he = wf = ln = Tl = Ht = 0, ie = qa = null, Hf = !1, (e & 8) !== 0 && (e |= e & 32);
    var n = t.entangledLanes;
    if (n !== 0)
      for (t = t.entanglements, n &= e; 0 < n; ) {
        var a = 31 - se(n), u = 1 << a;
        e |= t[a], n &= ~u;
      }
    return nl = e, Ou(), l;
  }
  function Zd(t, e) {
    ct = null, R.H = Ca, e === wn || e === Uu ? (e = cr(), pt = 3) : e === Gc ? (e = cr(), pt = 4) : pt = e === gf ? 8 : e !== null && typeof e == "object" && typeof e.then == "function" ? 6 : 1, ye = e, st === null && (Ht = 1, Ju(
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
    return R.H = Ca, t === null ? Ca : t;
  }
  function Wd() {
    var t = R.A;
    return R.A = Zh, t;
  }
  function ui() {
    Ht = 4, El || (vt & 4194048) !== vt && me.current !== null || (Gn = !0), (Tl & 134217727) === 0 && (ln & 134217727) === 0 || Mt === null || Ol(
      Mt,
      vt,
      he,
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
          var r = st, h = ye;
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
              if (pt = 0, ye = null, Kn(t, r, h, A), l && Gn) {
                f = 0;
                break t;
              }
              break;
            default:
              A = pt, pt = 0, ye = null, Kn(t, r, h, A);
          }
        }
        Wh(), f = Ht;
        break;
      } catch (C) {
        Zd(t, C);
      }
    while (!0);
    return e && t.shellSuspendCounter++, Je = Jl = null, St = n, R.H = a, R.A = u, st === null && (Mt = null, vt = 0, Ou()), f;
  }
  function Wh() {
    for (; st !== null; ) kd(st);
  }
  function kh(t, e) {
    var l = St;
    St |= 2;
    var n = Jd(), a = Wd();
    Mt !== t || vt !== e ? (ni = null, li = fe() + 500, Zn(t, e)) : Gn = aa(
      t,
      e
    );
    t: do
      try {
        if (pt !== 0 && st !== null) {
          e = st;
          var u = ye;
          e: switch (pt) {
            case 1:
              pt = 0, ye = null, Kn(t, e, u, 1);
              break;
            case 2:
            case 9:
              if (ur(u)) {
                pt = 0, ye = null, Fd(e);
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
              ur(u) ? (pt = 0, ye = null, Fd(e)) : (pt = 0, ye = null, Kn(t, e, u, 7));
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
                    pt = 0, ye = null;
                    var h = r.sibling;
                    if (h !== null) st = h;
                    else {
                      var A = r.return;
                      A !== null ? (st = A, ii(A)) : st = null;
                    }
                    break e;
                  }
              }
              pt = 0, ye = null, Kn(t, e, u, 5);
              break;
            case 6:
              pt = 0, ye = null, Kn(t, e, u, 6);
              break;
            case 8:
              Yf(), Ht = 6;
              break t;
            default:
              throw Error(o(462));
          }
        }
        Fh();
        break;
      } catch (C) {
        Zd(t, C);
      }
    while (!0);
    return Je = Jl = null, R.H = n, R.A = a, St = l, st !== null ? 0 : (Mt = null, vt = 0, Ou(), Ht);
  }
  function Fh() {
    for (; st !== null && !Sy(); )
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
  function Kn(t, e, l, n) {
    Je = Jl = null, tf(e), Hn = null, Ea = 0;
    var a = e.return;
    try {
      if (Lh(
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
    e.flags & 32768 ? (ht || n === 1 ? t = !0 : Gn || (vt & 536870912) !== 0 ? t = !1 : (El = t = !0, (n === 2 || n === 9 || n === 3 || n === 6) && (n = me.current, n !== null && n.tag === 13 && (n.flags |= 16384))), $d(e, t)) : ii(e);
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
      var l = Xh(
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
      var l = Gh(t.alternate, t);
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
  function Id(t, e, l, n, a, u, f, r, h) {
    t.cancelPendingCommit = null;
    do
      ci();
    while (Qt !== 0);
    if ((St & 6) !== 0) throw Error(o(327));
    if (e !== null) {
      if (e === t.current) throw Error(o(177));
      if (u = e.lanes | e.childLanes, u |= _c, Cy(
        t,
        l,
        u,
        f,
        r,
        h
      ), t === Mt && (st = Mt = null, vt = 0), Vn = e, Rl = t, al = l, Bf = u, qf = a, Xd = n, (e.subtreeFlags & 10256) !== 0 || (e.flags & 10256) !== 0 ? (t.callbackNode = null, t.callbackPriority = 0, t0(ou, function() {
        return nm(), null;
      })) : (t.callbackNode = null, t.callbackPriority = 0), n = (e.flags & 13878) !== 0, (e.subtreeFlags & 13878) !== 0 || n) {
        n = R.T, R.T = null, a = B.p, B.p = 2, f = St, St |= 4;
        try {
          Qh(t, e, l);
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
      var t = Rl, e = Vn, l = (e.flags & 13878) !== 0;
      if ((e.subtreeFlags & 13878) !== 0 || l) {
        l = R.T, R.T = null;
        var n = B.p;
        B.p = 2;
        var a = St;
        St |= 4;
        try {
          Ud(e, t);
          var u = If, f = Ys(t.containerInfo), r = u.focusedElem, h = u.selectionRange;
          if (f !== r && r && r.ownerDocument && Ls(
            r.ownerDocument.documentElement,
            r
          )) {
            if (h !== null && Tc(r)) {
              var A = h.start, C = h.end;
              if (C === void 0 && (C = A), "selectionStart" in r)
                r.selectionStart = A, r.selectionEnd = Math.min(
                  C,
                  r.value.length
                );
              else {
                var U = r.ownerDocument || document, O = U && U.defaultView || window;
                if (O.getSelection) {
                  var _ = O.getSelection(), J = r.textContent.length, tt = Math.min(h.start, J), Ot = h.end === void 0 ? tt : Math.min(h.end, J);
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
      var t = Rl, e = Vn, l = (e.flags & 8772) !== 0;
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
      Qt = 0, py();
      var t = Rl, e = Vn, l = al, n = Xd;
      (e.subtreeFlags & 10256) !== 0 || (e.flags & 10256) !== 0 ? Qt = 5 : (Qt = 0, Vn = Rl = null, lm(t, t.pendingLanes));
      var a = t.pendingLanes;
      if (a === 0 && (Al = null), nc(l), e = e.stateNode, oe && typeof oe.onCommitFiberRoot == "function")
        try {
          oe.onCommitFiberRoot(
            na,
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
      (al & 3) !== 0 && ci(), Be(t), a = t.pendingLanes, (l & 261930) !== 0 && (a & 42) !== 0 ? t === Lf ? La++ : (La = 0, Lf = t) : La = 0, Ya(0);
    }
  }
  function lm(t, e) {
    (t.pooledCacheLanes &= e) === 0 && (e = t.pooledCache, e != null && (t.pooledCache = null, Sa(e)));
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
      if (Qt = 0, Vn = Rl = null, al = 0, (St & 6) !== 0) throw Error(o(331));
      var r = St;
      if (St |= 4, Ld(u.current), Hd(
        u,
        u.current,
        f,
        l
      ), St = r, Ya(0, !1), oe && typeof oe.onPostCommitFiberRoot == "function")
        try {
          oe.onPostCommitFiberRoot(na, u);
        } catch {
        }
      return !0;
    } finally {
      B.p = a, R.T = n, lm(t, e);
    }
  }
  function am(t, e, l) {
    e = Ae(l, e), e = hf(t.stateNode, e, 2), t = gl(t, e, 2), t !== null && (ua(t, 2), Be(t));
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
            ), ua(n, 2), Be(n));
            break;
          }
        }
        e = e.return;
      }
  }
  function Xf(t, e, l) {
    var n = t.pingCache;
    if (n === null) {
      n = t.pingCache = new Kh();
      var a = /* @__PURE__ */ new Set();
      n.set(e, a);
    } else
      a = n.get(e), a === void 0 && (a = /* @__PURE__ */ new Set(), n.set(e, a));
    a.has(l) || (Uf = !0, a.add(l), t = $h.bind(null, t, e, l), e.then(t, t));
  }
  function $h(t, e, l) {
    var n = t.pingCache;
    n !== null && n.delete(e), t.pingedLanes |= t.suspendedLanes & l, t.warmLanes &= ~l, Mt === t && (vt & l) === l && (Ht === 4 || Ht === 3 && (vt & 62914560) === vt && 300 > fe() - ei ? (St & 2) === 0 && Zn(t, 0) : wf |= l, Qn === vt && (Qn = 0)), Be(t);
  }
  function um(t, e) {
    e === 0 && (e = Po()), t = Vl(t, e), t !== null && (ua(t, e), Be(t));
  }
  function Ih(t) {
    var e = t.memoizedState, l = 0;
    e !== null && (l = e.retryLane), um(t, l);
  }
  function Ph(t, e) {
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
  var fi = null, Jn = null, Gf = !1, oi = !1, Qf = !1, zl = 0;
  function Be(t) {
    t !== Jn && t.next === null && (Jn === null ? fi = Jn = t : Jn = Jn.next = t), oi = !0, Gf || (Gf = !0, l0());
  }
  function Ya(t, e) {
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
            ), (u & 3) === 0 || aa(n, u) || (l = !0, om(n, u));
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
      u === 0 ? (n.next = null, l === null ? fi = a : l.next = a, a === null && (Jn = l)) : (l = n, (t !== 0 || (u & 3) !== 0) && (oi = !0)), n = a;
    }
    Qt !== 0 && Qt !== 5 || Ya(t), zl !== 0 && (zl = 0);
  }
  function cm(t, e) {
    for (var l = t.suspendedLanes, n = t.pingedLanes, a = t.expirationTimes, u = t.pendingLanes & -62914561; 0 < u; ) {
      var f = 31 - se(u), r = 1 << f, h = a[f];
      h === -1 ? ((r & l) === 0 || (r & n) !== 0) && (a[f] = My(r, e)) : h <= e && (t.expiredLanes |= r), u &= ~r;
    }
    if (e = Mt, l = vt, l = mu(
      t,
      t === e ? l : 0,
      t.cancelPendingCommit !== null || t.timeoutHandle !== -1
    ), n = t.callbackNode, l === 0 || t === e && (pt === 2 || pt === 9) || t.cancelPendingCommit !== null)
      return n !== null && n !== null && tc(n), t.callbackNode = null, t.callbackPriority = 0;
    if ((l & 3) === 0 || aa(t, l)) {
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
  function Vf() {
    if (zl === 0) {
      var t = Nn;
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
                  var h = f ? rm(a, f) : new FormData(a);
                  sf(
                    l,
                    {
                      pending: !0,
                      data: h,
                      method: a.method,
                      action: u
                    },
                    null,
                    h
                  );
                }
              } else
                typeof u == "function" && (r.preventDefault(), h = f ? rm(a, f) : new FormData(a), sf(
                  l,
                  {
                    pending: !0,
                    data: h,
                    method: a.method,
                    action: u
                  },
                  u,
                  h
                ));
            },
            currentTarget: a
          }
        ]
      });
    }
  }
  for (var Zf = 0; Zf < zc.length; Zf++) {
    var Kf = zc[Zf], a0 = Kf.toLowerCase(), u0 = Kf[0].toUpperCase() + Kf.slice(1);
    xe(
      a0,
      "on" + u0
    );
  }
  xe(Gs, "onAnimationEnd"), xe(Qs, "onAnimationIteration"), xe(Vs, "onAnimationStart"), xe("dblclick", "onDoubleClick"), xe("focusin", "onFocus"), xe("focusout", "onBlur"), xe(Eh, "onTransitionRun"), xe(Th, "onTransitionStart"), xe(Ah, "onTransitionCancel"), xe(Zs, "onTransitionEnd"), bn("onMouseEnter", ["mouseout", "mouseover"]), bn("onMouseLeave", ["mouseout", "mouseover"]), bn("onPointerEnter", ["pointerout", "pointerover"]), bn("onPointerLeave", ["pointerout", "pointerover"]), jl(
    "onChange",
    "change click focusin focusout input keydown keyup selectionchange".split(" ")
  ), jl(
    "onSelect",
    "focusout contextmenu dragend focusin keydown keyup mousedown mouseup selectionchange".split(
      " "
    )
  ), jl("onBeforeInput", [
    "compositionend",
    "keypress",
    "textInput",
    "paste"
  ]), jl(
    "onCompositionEnd",
    "compositionend focusout keydown keypress keyup mousedown".split(" ")
  ), jl(
    "onCompositionStart",
    "compositionstart focusout keydown keypress keyup mousedown".split(" ")
  ), jl(
    "onCompositionUpdate",
    "compositionupdate focusout keydown keypress keyup mousedown".split(" ")
  );
  var ja = "abort canplay canplaythrough durationchange emptied encrypted ended error loadeddata loadedmetadata loadstart pause play playing progress ratechange resize seeked seeking stalled suspend timeupdate volumechange waiting".split(
    " "
  ), i0 = new Set(
    "beforetoggle cancel close invalid load scroll scrollend toggle".split(" ").concat(ja)
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
            var r = n[f], h = r.instance, A = r.currentTarget;
            if (r = r.listener, h !== u && a.isPropagationStopped())
              break t;
            u = r, a.currentTarget = A;
            try {
              u(a);
            } catch (C) {
              Ru(C);
            }
            a.currentTarget = null, u = h;
          }
        else
          for (f = 0; f < n.length; f++) {
            if (r = n[f], h = r.instance, A = r.currentTarget, r = r.listener, h !== u && a.isPropagationStopped())
              break t;
            u = r, a.currentTarget = A;
            try {
              u(a);
            } catch (C) {
              Ru(C);
            }
            a.currentTarget = null, u = h;
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
              var h = f.tag;
              if ((h === 3 || h === 4) && f.stateNode.containerInfo === a)
                return;
              f = f.return;
            }
          for (; r !== null; ) {
            if (f = yn(r), f === null) return;
            if (h = f.tag, h === 5 || h === 6 || h === 26 || h === 27) {
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
              _ = Py;
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
              _ = Xy;
              break;
            case "touchcancel":
            case "touchend":
            case "touchmove":
            case "touchstart":
              _ = lh;
              break;
            case Gs:
            case Qs:
            case Vs:
              _ = Vy;
              break;
            case Zs:
              _ = ah;
              break;
            case "scroll":
            case "scrollend":
              _ = Yy;
              break;
            case "wheel":
              _ = ih;
              break;
            case "copy":
            case "cut":
            case "paste":
              _ = Ky;
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
              _ = fh;
          }
          var tt = (e & 4) !== 0, Ot = !tt && (t === "scroll" || t === "scrollend"), E = tt ? O !== null ? O + "Capture" : null : O;
          tt = [];
          for (var b = A, T; b !== null; ) {
            var x = b;
            if (T = x.stateNode, x = x.tag, x !== 5 && x !== 26 && x !== 27 || T === null || E === null || (x = fa(b, E), x != null && tt.push(
              Xa(b, x, T)
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
          if (O = t === "mouseover" || t === "pointerover", _ = t === "mouseout" || t === "pointerout", O && l !== sc && (J = l.relatedTarget || l.fromElement) && (yn(J) || J[vn]))
            break t;
          if ((_ || O) && (O = C.window === C ? C : (O = C.ownerDocument) ? O.defaultView || O.parentWindow : window, _ ? (J = l.relatedTarget || l.toElement, _ = A, J = J ? yn(J) : null, J !== null && (Ot = m(J), tt = J.tag, J !== Ot || tt !== 5 && tt !== 27 && tt !== 6) && (J = null)) : (_ = null, J = A), _ !== J)) {
            if (tt = Es, x = "onMouseLeave", E = "onMouseEnter", b = "mouse", (t === "pointerout" || t === "pointerover") && (tt = As, x = "onPointerLeave", E = "onPointerEnter", b = "pointer"), Ot = _ == null ? O : ca(_), T = J == null ? O : ca(J), O = new tt(
              x,
              b + "leave",
              _,
              l,
              C
            ), O.target = Ot, O.relatedTarget = T, x = null, yn(C) === A && (tt = new tt(
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
          if (O = A ? ca(A) : window, _ = O.nodeName && O.nodeName.toLowerCase(), _ === "select" || _ === "input" && O.type === "file")
            var gt = Ds;
          else if (Cs(O))
            if (Ns)
              gt = bh;
            else {
              gt = hh;
              var k = yh;
            }
          else
            _ = O.nodeName, !_ || _.toLowerCase() !== "input" || O.type !== "checkbox" && O.type !== "radio" ? A && oc(A.elementType) && (gt = Ds) : gt = gh;
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
        switch (k = A ? ca(A) : window, t) {
          case "focusin":
            (Cs(k) || k.contentEditable === "true") && (Rn = k, Ac = A, ha = null);
            break;
          case "focusout":
            ha = Ac = Rn = null;
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
            if (ph) break;
          case "keydown":
          case "keyup":
            js(U, l, C);
        }
        var ot;
        if (Sc)
          t: {
            switch (t) {
              case "compositionstart":
                var yt = "onCompositionStart";
                break t;
              case "compositionend":
                yt = "onCompositionEnd";
                break t;
              case "compositionupdate":
                yt = "onCompositionUpdate";
                break t;
            }
            yt = void 0;
          }
        else
          An ? _s(t, l) && (yt = "onCompositionEnd") : t === "keydown" && l.keyCode === 229 && (yt = "onCompositionStart");
        yt && (Rs && l.locale !== "ko" && (An || yt !== "onCompositionStart" ? yt === "onCompositionEnd" && An && (ot = Ss()) : (sl = C, vc = "value" in sl ? sl.value : sl.textContent, An = !0)), k = ri(A, yt), 0 < k.length && (yt = new Ts(
          yt,
          t,
          null,
          l,
          C
        ), U.push({ event: yt, listeners: k }), ot ? yt.data = ot : (ot = Ms(l), ot !== null && (yt.data = ot)))), (ot = sh ? rh(t, l) : dh(t, l)) && (yt = ri(A, "onBeforeInput"), 0 < yt.length && (k = new Ts(
          "onBeforeInput",
          "beforeinput",
          null,
          l,
          C
        ), U.push({
          event: k,
          listeners: yt
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
  function Xa(t, e, l) {
    return {
      instance: t,
      listener: e,
      currentTarget: l
    };
  }
  function ri(t, e) {
    for (var l = e + "Capture", n = []; t !== null; ) {
      var a = t, u = a.stateNode;
      if (a = a.tag, a !== 5 && a !== 26 && a !== 27 || u === null || (a = fa(t, l), a != null && n.unshift(
        Xa(t, a, u)
      ), a = fa(t, e), a != null && n.push(
        Xa(t, a, u)
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
      var r = l, h = r.alternate, A = r.stateNode;
      if (r = r.tag, h !== null && h === n) break;
      r !== 5 && r !== 26 && r !== 27 || A === null || (h = A, a ? (A = fa(l, u), A != null && f.unshift(
        Xa(l, A, h)
      )) : a || (A = fa(l, u), A != null && f.push(
        Xa(l, A, h)
      ))), l = l.return;
    }
    f.length !== 0 && t.push({ event: e, listeners: f });
  }
  var f0 = /\r\n?/g, o0 = /\u0000|\uFFFD/g;
  function ym(t) {
    return (typeof t == "string" ? t : "" + t).replace(f0, `
`).replace(o0, "");
  }
  function hm(t, e) {
    return e = ym(e), ym(t) === e;
  }
  function Rt(t, e, l, n, a, u) {
    switch (l) {
      case "children":
        typeof n == "string" ? e === "body" || e === "textarea" && n === "" || pn(t, n) : (typeof n == "number" || typeof n == "bigint") && e !== "body" && pn(t, "" + n);
        break;
      case "className":
        yu(t, "class", n);
        break;
      case "tabIndex":
        yu(t, "tabindex", n);
        break;
      case "dir":
      case "role":
      case "viewBox":
      case "width":
      case "height":
        yu(t, l, n);
        break;
      case "style":
        hs(t, n, u);
        break;
      case "data":
        if (e !== "object") {
          yu(t, "data", n);
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
        (!(2 < l.length) || l[0] !== "o" && l[0] !== "O" || l[1] !== "n" && l[1] !== "N") && (l = qy.get(l) || l, vu(t, l, n));
    }
  }
  function Ff(t, e, l, n, a, u) {
    switch (l) {
      case "style":
        hs(t, n, u);
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
        typeof n == "string" ? pn(t, n) : (typeof n == "number" || typeof n == "bigint") && pn(t, "" + n);
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
        var r = u = f = a = null, h = null, A = null;
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
                  h = C;
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
          h,
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
        e = u, l = f, t.multiple = !!n, e != null ? Sn(t, !!n, e, !1) : l != null && Sn(t, !!n, l, !0);
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
        for (h in l)
          if (l.hasOwnProperty(h) && (n = l[h], n != null))
            switch (h) {
              case "selected":
                t.selected = n && typeof n != "function" && typeof n != "symbol";
                break;
              default:
                Rt(t, e, h, n, l, null);
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
        for (n = 0; n < ja.length; n++)
          rt(ja[n], t);
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
        var a = null, u = null, f = null, r = null, h = null, A = null, C = null;
        for (_ in l) {
          var U = l[_];
          if (l.hasOwnProperty(_) && U != null)
            switch (_) {
              case "checked":
                break;
              case "value":
                break;
              case "defaultValue":
                h = U;
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
          h,
          A,
          C,
          u,
          a
        );
        return;
      case "select":
        _ = f = r = O = null;
        for (u in l)
          if (h = l[u], l.hasOwnProperty(u) && h != null)
            switch (u) {
              case "value":
                break;
              case "multiple":
                _ = h;
              default:
                n.hasOwnProperty(u) || Rt(
                  t,
                  e,
                  u,
                  null,
                  n,
                  h
                );
            }
        for (a in n)
          if (u = n[a], h = l[a], n.hasOwnProperty(a) && (u != null || h != null))
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
                u !== h && Rt(
                  t,
                  e,
                  a,
                  u,
                  n,
                  h
                );
            }
        e = r, l = f, n = _, O != null ? Sn(t, !!l, O, !1) : !!n != !!l && (e != null ? Sn(t, !!l, e, !0) : Sn(t, !!l, l ? [] : "", !1));
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
        for (h in n)
          if (O = n[h], _ = l[h], n.hasOwnProperty(h) && O !== _ && (O != null || _ != null))
            switch (h) {
              case "selected":
                t.selected = O && typeof O != "function" && typeof O != "symbol";
                break;
              default:
                Rt(
                  t,
                  e,
                  h,
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
            var h = l[n], A = h.startTime;
            if (A > r) break;
            var C = h.transferSize, U = h.initiatorType;
            C && gm(U) && (h = h.responseEnd, f += C * (h < r ? 1 : (r - A) / (h - A)));
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
    return Em.resolve(null).then(t).catch(y0);
  } : pm;
  function y0(t) {
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
            t.removeChild(a), $n(e);
            return;
          }
          n--;
        } else if (l === "$" || l === "$?" || l === "$~" || l === "$!" || l === "&")
          n++;
        else if (l === "html")
          Ga(t.ownerDocument.documentElement);
        else if (l === "head") {
          l = t.ownerDocument.head, Ga(l);
          for (var u = l.firstChild; u; ) {
            var f = u.nextSibling, r = u.nodeName;
            u[ia] || r === "SCRIPT" || r === "STYLE" || r === "LINK" && u.rel.toLowerCase() === "stylesheet" || l.removeChild(u), u = f;
          }
        } else
          l === "body" && Ga(t.ownerDocument.body);
      l = a;
    } while (l);
    $n(e);
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
  function h0(t, e, l, n) {
    for (; t.nodeType === 1; ) {
      var a = l;
      if (t.nodeName.toLowerCase() !== e.toLowerCase()) {
        if (!n && (t.nodeName !== "INPUT" || t.type !== "hidden"))
          break;
      } else if (n) {
        if (!t[ia])
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
  function Ga(t) {
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
    e !== null && e.tag === 5 && e.type === "form" ? Zr(e) : ul.r(t);
  }
  var Wn = typeof document > "u" ? null : document;
  function Cm(t, e, l) {
    var n = Wn;
    if (n && typeof e == "string" && e) {
      var a = Ee(e);
      a = 'link[rel="' + t + '"][href="' + a + '"]', typeof l == "string" && (a += '[crossorigin="' + l + '"]'), Mm.has(a) || (Mm.add(a), t = { rel: t, crossOrigin: l, href: e }, n.querySelector(a) === null && (e = n.createElement("link"), It(e, "link", t), Zt(e), n.head.appendChild(e)));
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
    var n = Wn;
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
          u = kn(t);
          break;
        case "script":
          u = Fn(t);
      }
      Ce.has(u) || (t = z(
        {
          rel: "preload",
          href: e === "image" && l && l.imageSrcSet ? void 0 : t,
          as: e
        },
        l
      ), Ce.set(u, t), n.querySelector(a) !== null || e === "style" && n.querySelector(Qa(u)) || e === "script" && n.querySelector(Va(u)) || (e = n.createElement("link"), It(e, "link", t), Zt(e), n.head.appendChild(e)));
    }
  }
  function R0(t, e) {
    ul.m(t, e);
    var l = Wn;
    if (l && t) {
      var n = e && typeof e.as == "string" ? e.as : "script", a = 'link[rel="modulepreload"][as="' + Ee(n) + '"][href="' + Ee(t) + '"]', u = a;
      switch (n) {
        case "audioworklet":
        case "paintworklet":
        case "serviceworker":
        case "sharedworker":
        case "worker":
        case "script":
          u = Fn(t);
      }
      if (!Ce.has(u) && (t = z({ rel: "modulepreload", href: t }, e), Ce.set(u, t), l.querySelector(a) === null)) {
        switch (n) {
          case "audioworklet":
          case "paintworklet":
          case "serviceworker":
          case "sharedworker":
          case "worker":
          case "script":
            if (l.querySelector(Va(u)))
              return;
        }
        n = l.createElement("link"), It(n, "link", t), Zt(n), l.head.appendChild(n);
      }
    }
  }
  function O0(t, e, l) {
    ul.S(t, e, l);
    var n = Wn;
    if (n && t) {
      var a = gn(n).hoistableStyles, u = kn(t);
      e = e || "default";
      var f = a.get(u);
      if (!f) {
        var r = { loading: 0, preload: null };
        if (f = n.querySelector(
          Qa(u)
        ))
          r.loading = 5;
        else {
          t = z(
            { rel: "stylesheet", href: t, "data-precedence": e },
            l
          ), (l = Ce.get(u)) && uo(t, l);
          var h = f = n.createElement("link");
          Zt(h), It(h, "link", t), h._p = new Promise(function(A, C) {
            h.onload = A, h.onerror = C;
          }), h.addEventListener("load", function() {
            r.loading |= 1;
          }), h.addEventListener("error", function() {
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
    var l = Wn;
    if (l && t) {
      var n = gn(l).hoistableScripts, a = Fn(t), u = n.get(a);
      u || (u = l.querySelector(Va(a)), u || (t = z({ src: t, async: !0 }, e), (e = Ce.get(a)) && io(t, e), u = l.createElement("script"), Zt(u), It(u, "link", t), l.head.appendChild(u)), u = {
        type: "script",
        instance: u,
        count: 1,
        state: null
      }, n.set(a, u));
    }
  }
  function _0(t, e) {
    ul.M(t, e);
    var l = Wn;
    if (l && t) {
      var n = gn(l).hoistableScripts, a = Fn(t), u = n.get(a);
      u || (u = l.querySelector(Va(a)), u || (t = z({ src: t, async: !0, type: "module" }, e), (e = Ce.get(a)) && io(t, e), u = l.createElement("script"), Zt(u), It(u, "link", t), l.head.appendChild(u)), u = {
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
        return typeof l.precedence == "string" && typeof l.href == "string" ? (e = kn(l.href), l = gn(
          a
        ).hoistableStyles, n = l.get(e), n || (n = {
          type: "style",
          instance: null,
          count: 0,
          state: null
        }, l.set(e, n)), n) : { type: "void", instance: null, count: 0, state: null };
      case "link":
        if (l.rel === "stylesheet" && typeof l.href == "string" && typeof l.precedence == "string") {
          t = kn(l.href);
          var u = gn(
            a
          ).hoistableStyles, f = u.get(t);
          if (f || (a = a.ownerDocument || a, f = {
            type: "stylesheet",
            instance: null,
            count: 0,
            state: { loading: 0, preload: null }
          }, u.set(t, f), (u = a.querySelector(
            Qa(t)
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
        return e = l.async, l = l.src, typeof l == "string" && e && typeof e != "function" && typeof e != "symbol" ? (e = Fn(l), l = gn(
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
  function kn(t) {
    return 'href="' + Ee(t) + '"';
  }
  function Qa(t) {
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
    }), It(e, "link", l), Zt(e), t.head.appendChild(e));
  }
  function Fn(t) {
    return '[src="' + Ee(t) + '"]';
  }
  function Va(t) {
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
            return e.instance = n, Zt(n), n;
          var a = z({}, l, {
            "data-href": l.href,
            "data-precedence": l.precedence,
            href: null,
            precedence: null
          });
          return n = (t.ownerDocument || t).createElement(
            "style"
          ), Zt(n), It(n, "style", a), vi(n, l.precedence, t), e.instance = n;
        case "stylesheet":
          a = kn(l.href);
          var u = t.querySelector(
            Qa(a)
          );
          if (u)
            return e.state.loading |= 4, e.instance = u, Zt(u), u;
          n = Dm(l), (a = Ce.get(a)) && uo(n, a), u = (t.ownerDocument || t).createElement("link"), Zt(u);
          var f = u;
          return f._p = new Promise(function(r, h) {
            f.onload = r, f.onerror = h;
          }), It(u, "link", n), e.state.loading |= 4, vi(u, l.precedence, t), e.instance = u;
        case "script":
          return u = Fn(l.src), (a = t.querySelector(
            Va(u)
          )) ? (e.instance = a, Zt(a), a) : (n = l, (a = Ce.get(u)) && (n = z({}, l), io(n, a)), t = t.ownerDocument || t, a = t.createElement("script"), Zt(a), It(a, "link", n), t.head.appendChild(a), e.instance = a);
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
  var yi = null;
  function Um(t, e, l) {
    if (yi === null) {
      var n = /* @__PURE__ */ new Map(), a = yi = /* @__PURE__ */ new Map();
      a.set(l, n);
    } else
      a = yi, n = a.get(l), n || (n = /* @__PURE__ */ new Map(), a.set(l, n));
    if (n.has(t)) return n;
    for (n.set(t, null), l = l.getElementsByTagName(t), a = 0; a < l.length; a++) {
      var u = l[a];
      if (!(u[ia] || u[Wt] || t === "link" && u.getAttribute("rel") === "stylesheet") && u.namespaceURI !== "http://www.w3.org/2000/svg") {
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
        var a = kn(n.href), u = e.querySelector(
          Qa(a)
        );
        if (u) {
          e = u._p, e !== null && typeof e == "object" && typeof e.then == "function" && (t.count++, t = hi.bind(t), e.then(t, t)), l.state.loading |= 4, l.instance = u, Zt(u);
          return;
        }
        u = e.ownerDocument || e, n = Dm(n), (a = Ce.get(a)) && uo(n, a), u = u.createElement("link"), Zt(u);
        var f = u;
        f._p = new Promise(function(r, h) {
          f.onload = r, f.onerror = h;
        }), It(u, "link", n), l.instance = u;
      }
      t.stylesheets === null && (t.stylesheets = /* @__PURE__ */ new Map()), t.stylesheets.set(l, e), (e = l.state.preload) && (l.state.loading & 3) === 0 && (t.count++, l = hi.bind(t), e.addEventListener("load", l), e.addEventListener("error", l));
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
  function hi() {
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
    t.stylesheets = null, t.unsuspend !== null && (t.count++, gi = /* @__PURE__ */ new Map(), e.forEach(N0, t), gi = null, hi.call(t));
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
      a = e.instance, f = a.getAttribute("data-precedence"), u = l.get(f) || n, u === n && l.set(null, a), l.set(f, a), this.count++, n = hi.bind(this), a.addEventListener("load", n), a.addEventListener("error", n), u ? u.parentNode.insertBefore(a, u.nextSibling) : (t = t.nodeType === 9 ? t.head : t, t.insertBefore(a, t.firstChild)), e.state.loading |= 4;
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
  function U0(t, e, l, n, a, u, f, r, h) {
    this.tag = 1, this.containerInfo = t, this.pingCache = this.current = this.pendingChildren = null, this.timeoutHandle = -1, this.callbackNode = this.next = this.pendingContext = this.context = this.cancelPendingCommit = null, this.callbackPriority = 0, this.expirationTimes = ec(-1), this.entangledLanes = this.shellSuspendCounter = this.errorRecoveryDisabledLanes = this.expiredLanes = this.warmLanes = this.pingedLanes = this.suspendedLanes = this.pendingLanes = 0, this.entanglements = ec(0), this.hiddenUpdates = ec(null), this.identifierPrefix = n, this.onUncaughtError = a, this.onCaughtError = u, this.onRecoverableError = f, this.pooledCache = null, this.pooledCacheLanes = 0, this.formState = h, this.incompleteTransitions = /* @__PURE__ */ new Map();
  }
  function Bm(t, e, l, n, a, u, f, r, h, A, C, U) {
    return t = new U0(
      t,
      e,
      l,
      f,
      h,
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
    return t ? (t = _n, t) : _n;
  }
  function Lm(t, e, l, n, a, u) {
    a = qm(a), n.context === null ? n.context = a : n.pendingContext = a, n = hl(e), n.payload = { element: l }, u = u === void 0 ? null : u, u !== null && (n.callback = u), l = gl(t, n, e), l !== null && (ce(l, t, e), Aa(l, t, e));
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
      var e = Vl(t, 67108864);
      e !== null && ce(e, t, 67108864), fo(t, 67108864);
    }
  }
  function Xm(t) {
    if (t.tag === 13 || t.tag === 31) {
      var e = ge();
      e = lc(e);
      var l = Vl(t, e);
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
                  var f = Yl(u.pendingLanes);
                  if (f !== 0) {
                    var r = u;
                    for (r.pendingLanes |= 2, r.entangledLanes |= 2; f; ) {
                      var h = 1 << 31 - se(f);
                      r.entanglements[1] |= h, f &= ~h;
                    }
                    Be(u), (St & 6) === 0 && (li = fe() + 500, Ya(0));
                  }
                }
                break;
              case 31:
              case 13:
                r = Vl(u, 2), r !== null && ce(r, u, 2), ai(), fo(u, 2);
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
    if (pi = null, t = yn(t), t !== null) {
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
        switch (Ey()) {
          case Fo:
            return 2;
          case $o:
            return 8;
          case ou:
          case Ty:
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
  var mo = !1, Ml = null, Cl = null, xl = null, Ka = /* @__PURE__ */ new Map(), Ja = /* @__PURE__ */ new Map(), Dl = [], B0 = "mousedown mouseup touchcancel touchend touchstart auxclick dblclick pointercancel pointerdown pointerup dragend dragstart drop compositionend compositionstart keydown keypress keyup input textInput copy cut paste click change contextmenu reset".split(
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
        Ka.delete(e.pointerId);
        break;
      case "gotpointercapture":
      case "lostpointercapture":
        Ja.delete(e.pointerId);
    }
  }
  function Wa(t, e, l, n, a, u) {
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
        return Ml = Wa(
          Ml,
          t,
          e,
          l,
          n,
          a
        ), !0;
      case "dragenter":
        return Cl = Wa(
          Cl,
          t,
          e,
          l,
          n,
          a
        ), !0;
      case "mouseover":
        return xl = Wa(
          xl,
          t,
          e,
          l,
          n,
          a
        ), !0;
      case "pointerover":
        var u = a.pointerId;
        return Ka.set(
          u,
          Wa(
            Ka.get(u) || null,
            t,
            e,
            l,
            n,
            a
          )
        ), !0;
      case "gotpointercapture":
        return u = a.pointerId, Ja.set(
          u,
          Wa(
            Ja.get(u) || null,
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
  function Vm(t) {
    var e = yn(t.target);
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
  function Zm(t, e, l) {
    Ei(t) && l.delete(e);
  }
  function L0() {
    mo = !1, Ml !== null && Ei(Ml) && (Ml = null), Cl !== null && Ei(Cl) && (Cl = null), xl !== null && Ei(xl) && (xl = null), Ka.forEach(Zm), Ja.forEach(Zm);
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
  function $n(t) {
    function e(h) {
      return Ti(h, t);
    }
    Ml !== null && Ti(Ml, t), Cl !== null && Ti(Cl, t), xl !== null && Ti(xl, t), Ka.forEach(e), Ja.forEach(e);
    for (var l = 0; l < Dl.length; l++) {
      var n = Dl[l];
      n.blockedOn === t && (n.blockedOn = null);
    }
    for (; 0 < Dl.length && (l = Dl[0], l.blockedOn === null); )
      Vm(l), l.blockedOn === null && Dl.shift();
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
      Lm(t.current, 2, null, t, null, null), ai(), e[vn] = null;
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
      Dl.splice(l, 0, t), l === 0 && Vm(t);
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
    return t = y(e), t = t !== null ? M(t) : null, t = t === null ? null : t.stateNode, t;
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
        na = Oi.inject(
          Y0
        ), oe = Oi;
      } catch {
      }
  }
  return $a.createRoot = function(t, e) {
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
    ), t[vn] = e.current, Wf(t), new vo(e);
  }, $a.hydrateRoot = function(t, e, l) {
    if (!d(t)) throw Error(o(299));
    var n = !1, a = "", u = ed, f = ld, r = nd, h = null;
    return l != null && (l.unstable_strictMode === !0 && (n = !0), l.identifierPrefix !== void 0 && (a = l.identifierPrefix), l.onUncaughtError !== void 0 && (u = l.onUncaughtError), l.onCaughtError !== void 0 && (f = l.onCaughtError), l.onRecoverableError !== void 0 && (r = l.onRecoverableError), l.formState !== void 0 && (h = l.formState)), e = Bm(
      t,
      1,
      !0,
      e,
      l ?? null,
      n,
      a,
      h,
      u,
      f,
      r,
      Jm
    ), e.context = qm(null), l = e.current, n = ge(), n = lc(n), a = hl(n), a.callback = null, gl(l, a, n), l = n, e.current.lanes = l, ua(e, l), Be(e), t[vn] = e.current, Wf(t), new Ri(e);
  }, $a.version = "19.2.4", $a;
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
const Ab = /* @__PURE__ */ Uv(Gi);
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
const dn = q.createContext(null), qe = /* @__PURE__ */ new Map();
let Vv = "", iv = !1;
function fn() {
  return Vv + "/";
}
function Go(i, c, s, o, d) {
  d !== void 0 && (Vv = d);
  const m = o ?? "";
  iv ? d !== void 0 && Im(fn() + "react-api/events?windowName=" + encodeURIComponent(m)) : (iv = !0, Q0(fn()), Im(fn() + "react-api/events?windowName=" + encodeURIComponent(m))), V0(m);
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
  const y = (N) => {
    S.applyPatch(N);
  };
  Xo(i, y);
  const M = Gv.createRoot(v);
  qe.set(i, { root: M, store: S, sseListener: y }), Zv = m;
  const z = () => {
    const N = q.useSyncExternalStore(S.subscribeStore, S.getSnapshot);
    return q.useLayoutEffect(() => {
      v.style.display = N.hidden === !0 ? "none" : "";
    }, [N.hidden]), un.createElement(
      dn.Provider,
      { value: { controlId: i, windowName: m, store: S } },
      un.createElement(p, { controlId: i, state: N })
    );
  };
  Gi.flushSync(() => {
    M.render(un.createElement(z));
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
function yg(i, c) {
  let s = qe.get(i);
  if (!s) {
    const d = new Qv(c), m = (v) => {
      d.applyPatch(v);
    };
    Xo(i, m), s = { root: null, store: d, sseListener: m }, qe.set(i, s);
  }
  return { controlId: i, windowName: Zv, store: s.store };
}
let Zv = "";
function hg() {
  const i = q.useContext(dn);
  if (!i)
    throw new Error("useTLState must be used inside a TLReact-mounted component.");
  return q.useSyncExternalStore(i.store.subscribeStore, i.store.getSnapshot);
}
function gg() {
  const i = q.useContext(dn);
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
        const v = await fetch(fn() + "react-api/command", {
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
function Rb() {
  const i = q.useContext(dn);
  if (!i)
    throw new Error("useTLUpload must be used inside a TLReact-mounted component.");
  const c = i.controlId, s = i.windowName;
  return q.useCallback(
    async (o) => {
      o.append("controlId", c), o.append("windowName", s);
      try {
        const d = await fetch(fn() + "react-api/upload", {
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
function Ob() {
  const i = q.useContext(dn);
  if (!i)
    throw new Error("useTLDataUrl must be used inside a TLReact-mounted component.");
  return fn() + "react-api/data?controlId=" + encodeURIComponent(i.controlId) + "&windowName=" + encodeURIComponent(i.windowName);
}
function zb() {
  const i = hg(), c = gg(), s = q.useContext(dn), o = q.useCallback(
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
    const S = d.dataset.reactState, y = S ? JSON.parse(S) : {};
    Go(d.id, m, y, v, p);
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
var Eo = { exports: {} }, Ia = {};
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
  if (fv) return Ia;
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
  return Ia.Fragment = c, Ia.jsx = s, Ia.jsxs = s, Ia;
}
var ov;
function Sg() {
  return ov || (ov = 1, Eo.exports = bg()), Eo.exports;
}
var Vt = Sg();
const _b = ({ control: i }) => {
  const c = i, s = Nv(c.module), o = q.useMemo(
    () => yg(c.controlId, c.state),
    [c.controlId]
  );
  q.useEffect(() => () => Ui(c.controlId), [c.controlId]);
  const d = q.useSyncExternalStore(o.store.subscribeStore, o.store.getSnapshot);
  return s ? /* @__PURE__ */ Vt.jsx(dn.Provider, { value: o, children: /* @__PURE__ */ Vt.jsx(s, { controlId: c.controlId, state: d }) }) : /* @__PURE__ */ Vt.jsxs("span", { children: [
    "[Component not registered: ",
    c.module,
    "]"
  ] });
};
function Qi() {
  return typeof window < "u";
}
function ea(i) {
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
  return /^(table|td|th)$/.test(ea(i));
}
function Vi(i) {
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
const Eg = /transform|translate|scale|rotate|perspective|filter/, Tg = /paint|layout|strict|content/, nn = (i) => !!i && i !== "none";
let To;
function Vo(i) {
  const c = Lt(i) ? Se(i) : i;
  return nn(c.transform) || nn(c.translate) || nn(c.scale) || nn(c.rotate) || nn(c.perspective) || !Zi() && (nn(c.backdropFilter) || nn(c.filter)) || Eg.test(c.willChange || "") || Tg.test(c.contain || "");
}
function Ag(i) {
  let c = cl(i);
  for (; je(c) && !il(c); ) {
    if (Vo(c))
      return c;
    if (Vi(c))
      return null;
    c = cl(c);
  }
  return null;
}
function Zi() {
  return To == null && (To = typeof CSS < "u" && CSS.supports && CSS.supports("-webkit-backdrop-filter", "none")), To;
}
function il(i) {
  return /^(html|body|#document)$/.test(ea(i));
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
  if (ea(i) === "html")
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
function Bl(i, c, s) {
  var o;
  c === void 0 && (c = []), s === void 0 && (s = !0);
  const d = Kv(i), m = d === ((o = i.ownerDocument) == null ? void 0 : o.body), v = be(d);
  if (m) {
    const p = Do(v);
    return c.concat(v, v.visualViewport || [], uu(d) ? d : [], p && s ? Bl(p) : []);
  } else
    return c.concat(d, Bl(d, [], s));
}
function Do(i) {
  return i.parent && Object.getPrototypeOf(i.parent) ? i.frameElement : null;
}
const ta = Math.min, on = Math.max, Hi = Math.round, zi = Math.floor, Le = (i) => ({
  x: i,
  y: i
}), Rg = {
  left: "right",
  right: "left",
  bottom: "top",
  top: "bottom"
};
function No(i, c, s) {
  return on(i, ta(c, s));
}
function iu(i, c) {
  return typeof i == "function" ? i(c) : i;
}
function sn(i) {
  return i.split("-")[0];
}
function cu(i) {
  return i.split("-")[1];
}
function Jv(i) {
  return i === "x" ? "y" : "x";
}
function Zo(i) {
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
  const o = cu(i), d = Ko(i), m = Zo(d);
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
  let m = Cg(sn(i), s === "start", o);
  return d && (m = m.map((v) => v + "-" + d), c && (m = m.concat(m.map(Uo)))), m;
}
function Bi(i) {
  const c = sn(i);
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
        var p = v.assignedElements(), S = p.length ? p : v.children, y = Ho(S, !0, o);
        o.flatten ? d.push.apply(d, y) : d.push({
          scopeParent: v,
          candidates: y
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
      var y, M, z;
      o = Li(d), d = (y = o) === null || y === void 0 ? void 0 : y.host, m = !!((M = d) !== null && M !== void 0 && (z = M.ownerDocument) !== null && z !== void 0 && z.contains(d));
    }
  }
  return m;
}, dv = function(c) {
  var s = c.getBoundingClientRect(), o = s.width, d = s.height;
  return o === 0 && d === 0;
}, Vg = function(c, s) {
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
        var y = c.parentElement, M = Li(c);
        if (y && !y.shadowRoot && d(y) === !0)
          return dv(c);
        c.assignedSlot ? c = c.assignedSlot : !y && M !== c.ownerDocument ? c = M.host : c = y;
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
}, Zg = function(c) {
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
  return !(s.disabled || qg(s) || Vg(s, c) || // For a details element with a summary, the summary element gets the focus
  Lg(s) || Zg(s));
}, mv = function(c, s) {
  return !(Gg(s) || $v(s) < 0 || !Kg(c, s));
}, Jg = function(c) {
  var s = parseInt(c.getAttribute("tabindex"), 10);
  return !!(isNaN(s) || s >= 0);
}, Pv = function(c) {
  var s = [], o = [];
  return c.forEach(function(d, m) {
    var v = !!d.scopeParent, p = v ? d.scopeParent : d, S = Hg(p, v), y = v ? Pv(d.candidates) : p;
    S === 0 ? v ? s.push.apply(s, y) : s.push(p) : o.push({
      documentOrder: m,
      tabIndex: S,
      item: d,
      isScope: v,
      content: y
    });
  }), o.sort(Bg).reduce(function(d, m) {
    return m.isScope ? d.push.apply(d, m.content) : d.push(m.content), d;
  }, []).concat(s);
}, ty = function(c, s) {
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
function Pa(i) {
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
}, ql = Pg ? q.useLayoutEffect : t1;
const e1 = {
  ...wv
}, l1 = e1.useInsertionEffect, n1 = l1 || ((i) => i());
function In(i) {
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
const ey = () => ({
  getShadowRoot: !0,
  displayCheck: (
    // JSDOM does not support the `tabbable` library. To solve this we can
    // check if `ResizeObserver` is a real function (not polyfilled), which
    // determines if the current environment is JSDOM-like.
    typeof ResizeObserver == "function" && ResizeObserver.toString().includes("[native code]") ? "full" : "none"
  )
});
function ly(i, c) {
  const s = ty(i, ey()), o = s.length;
  if (o === 0) return;
  const d = kg(au(i)), m = s.indexOf(d), v = m === -1 ? c === 1 ? 0 : o - 1 : m + c;
  return s[v];
}
function a1(i) {
  return ly(au(i).body, 1) || i;
}
function u1(i) {
  return ly(au(i).body, -1) || i;
}
function Ro(i, c) {
  const s = c || i.currentTarget, o = i.relatedTarget;
  return !o || !Bo(s, o);
}
function i1(i) {
  ty(i, ey()).forEach((s) => {
    s.dataset.tabindex = s.getAttribute("tabindex") || "", s.setAttribute("tabindex", "-1");
  });
}
function yv(i) {
  i.querySelectorAll("[data-tabindex]").forEach((s) => {
    const o = s.dataset.tabindex;
    delete s.dataset.tabindex, o ? s.setAttribute("tabindex", o) : s.removeAttribute("tabindex");
  });
}
function hv(i, c, s) {
  let {
    reference: o,
    floating: d
  } = i;
  const m = wl(c), v = Ko(c), p = Zo(v), S = sn(c), y = m === "y", M = o.x + o.width / 2 - d.width / 2, z = o.y + o.height / 2 - d.height / 2, N = o[p] / 2 - d[p] / 2;
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
      D[v] -= N * (s && y ? -1 : 1);
      break;
    case "end":
      D[v] += N * (s && y ? -1 : 1);
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
    boundary: y = "clippingAncestors",
    rootBoundary: M = "viewport",
    elementContext: z = "floating",
    altBoundary: N = !1,
    padding: D = 0
  } = iu(c, i), Y = Wv(D), V = p[N ? z === "floating" ? "reference" : "floating" : z], X = qi(await m.getClippingRect({
    element: (s = await (m.isElement == null ? void 0 : m.isElement(V))) == null || s ? V : V.contextElement || await (m.getDocumentElement == null ? void 0 : m.getDocumentElement(p.floating)),
    boundary: y,
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
  let y = await v.getElementRects({
    reference: i,
    floating: c,
    strategy: d
  }), {
    x: M,
    y: z
  } = hv(y, o, S), N = o, D = 0;
  const Y = {};
  for (let L = 0; L < m.length; L++) {
    const V = m[L];
    if (!V)
      continue;
    const {
      name: X,
      fn: W
    } = V, {
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
      rects: y,
      platform: p,
      elements: {
        reference: i,
        floating: c
      }
    });
    M = G ?? M, z = $ ?? z, Y[X] = {
      ...Y[X],
      ...at
    }, lt && D < f1 && (D++, typeof lt == "object" && (lt.placement && (N = lt.placement), lt.rects && (y = lt.rects === !0 ? await v.getElementRects({
      reference: i,
      floating: c,
      strategy: d
    }) : lt.rects), {
      x: M,
      y: z
    } = hv(y, N, S)), L = -1);
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
      element: y,
      padding: M = 0
    } = iu(i, c) || {};
    if (y == null)
      return {};
    const z = Wv(M), N = {
      x: s,
      y: o
    }, D = Ko(d), Y = Zo(D), L = await v.getDimensions(y), V = D === "y", X = V ? "top" : "left", W = V ? "bottom" : "right", G = V ? "clientHeight" : "clientWidth", $ = m.reference[Y] + m.reference[D] - N[D] - m.floating[Y], at = N[D] - m.reference[D], lt = await (v.getOffsetParent == null ? void 0 : v.getOffsetParent(y));
    let Z = lt ? lt[G] : 0;
    (!Z || !await (v.isElement == null ? void 0 : v.isElement(lt))) && (Z = p.floating[G] || m.floating[Y]);
    const it = $ / 2 - at / 2, zt = Z / 2 - L[Y] / 2 - 1, Tt = ta(z[X], zt), _t = ta(z[W], zt), dt = Tt, Ct = Z - L[Y] - _t, Q = Z / 2 - L[Y] / 2 + it, ft = No(dt, Q, Ct), R = !S.arrow && cu(d) != null && Q !== ft && m.reference[Y] / 2 - (Q < dt ? Tt : _t) - L[Y] / 2 < 0, B = R ? Q < dt ? Q - dt : Q - Ct : 0;
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
        elements: y
      } = c, {
        mainAxis: M = !0,
        crossAxis: z = !0,
        fallbackPlacements: N,
        fallbackStrategy: D = "bestFit",
        fallbackAxisSideDirection: Y = "none",
        flipAlignment: L = !0,
        ...V
      } = iu(i, c);
      if ((s = m.arrow) != null && s.alignmentOffset)
        return {};
      const X = sn(d), W = wl(p), G = sn(p) === p, $ = await (S.isRTL == null ? void 0 : S.isRTL(y.floating)), at = N || (G || !L ? [Bi(p)] : zg(p)), lt = Y !== "none";
      !N && lt && at.push(...xg(p, L, Y, $));
      const Z = [p, ...at], it = await S.detectOverflow(c, V), zt = [];
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
        const Q = (((_t = m.flip) == null ? void 0 : _t.index) || 0) + 1, ft = Z[Q];
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
  } = i, m = await (o.isRTL == null ? void 0 : o.isRTL(d.floating)), v = sn(s), p = cu(s), S = wl(s) === "y", y = d1.has(v) ? -1 : 1, M = m && S ? -1 : 1, z = iu(c, i);
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
    y: N * y
  } : {
    x: N * y,
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
}, y1 = function(i) {
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
        ...y
      } = iu(i, c), M = {
        x: s,
        y: o
      }, z = await m.detectOverflow(c, y), N = wl(sn(d)), D = Jv(N);
      let Y = M[D], L = M[N];
      if (v) {
        const X = D === "y" ? "top" : "left", W = D === "y" ? "bottom" : "right", G = Y + z[X], $ = Y - z[W];
        Y = No(G, Y, $);
      }
      if (p) {
        const X = N === "y" ? "top" : "left", W = N === "y" ? "bottom" : "right", G = L + z[X], $ = L - z[W];
        L = No(G, L, $);
      }
      const V = S.fn({
        ...c,
        [D]: Y,
        [N]: L
      });
      return {
        ...V,
        data: {
          x: V.x - s,
          y: V.y - o,
          enabled: {
            [D]: v,
            [N]: p
          }
        }
      };
    }
  };
};
function ny(i) {
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
function Pn(i) {
  const c = Jo(i);
  if (!je(c))
    return Le(1);
  const s = c.getBoundingClientRect(), {
    width: o,
    height: d,
    $: m
  } = ny(c);
  let v = (m ? Hi(s.width) : s.width) / o, p = (m ? Hi(s.height) : s.height) / d;
  return (!v || !Number.isFinite(v)) && (v = 1), (!p || !Number.isFinite(p)) && (p = 1), {
    x: v,
    y: p
  };
}
const h1 = /* @__PURE__ */ Le(0);
function ay(i) {
  const c = be(i);
  return !Zi() || !c.visualViewport ? h1 : {
    x: c.visualViewport.offsetLeft,
    y: c.visualViewport.offsetTop
  };
}
function g1(i, c, s) {
  return c === void 0 && (c = !1), !s || c && s !== be(i) ? !1 : c;
}
function rn(i, c, s, o) {
  c === void 0 && (c = !1), s === void 0 && (s = !1);
  const d = i.getBoundingClientRect(), m = Jo(i);
  let v = Le(1);
  c && (o ? Lt(o) && (v = Pn(o)) : v = Pn(i));
  const p = g1(m, s, o) ? ay(m) : Le(0);
  let S = (d.left + p.x) / v.x, y = (d.top + p.y) / v.y, M = d.width / v.x, z = d.height / v.y;
  if (m) {
    const N = be(m), D = o && Lt(o) ? be(o) : o;
    let Y = N, L = Do(Y);
    for (; L && o && D !== Y; ) {
      const V = Pn(L), X = L.getBoundingClientRect(), W = Se(L), G = X.left + (L.clientLeft + parseFloat(W.paddingLeft)) * V.x, $ = X.top + (L.clientTop + parseFloat(W.paddingTop)) * V.y;
      S *= V.x, y *= V.y, M *= V.x, z *= V.y, S += G, y += $, Y = be(L), L = Do(Y);
    }
  }
  return qi({
    width: M,
    height: z,
    x: S,
    y
  });
}
function Ji(i, c) {
  const s = Ki(i).scrollLeft;
  return c ? c.left + s : rn(Ye(i)).left + s;
}
function uy(i, c) {
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
  const m = d === "fixed", v = Ye(o), p = c ? Vi(c.floating) : !1;
  if (o === v || p && m)
    return s;
  let S = {
    scrollLeft: 0,
    scrollTop: 0
  }, y = Le(1);
  const M = Le(0), z = je(o);
  if ((z || !z && !m) && ((ea(o) !== "body" || uu(v)) && (S = Ki(o)), z)) {
    const D = rn(o);
    y = Pn(o), M.x = D.x + o.clientLeft, M.y = D.y + o.clientTop;
  }
  const N = v && !z && !m ? uy(v, S) : Le(0);
  return {
    width: s.width * y.x,
    height: s.height * y.y,
    x: s.x * y.x - S.scrollLeft * y.x + M.x + N.x,
    y: s.y * y.y - S.scrollTop * y.y + M.y + N.y
  };
}
function S1(i) {
  return Array.from(i.getClientRects());
}
function p1(i) {
  const c = Ye(i), s = Ki(i), o = i.ownerDocument.body, d = on(c.scrollWidth, c.clientWidth, o.scrollWidth, o.clientWidth), m = on(c.scrollHeight, c.clientHeight, o.scrollHeight, o.clientHeight);
  let v = -s.scrollLeft + Ji(i);
  const p = -s.scrollTop;
  return Se(o).direction === "rtl" && (v += on(c.clientWidth, o.clientWidth) - d), {
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
    const M = Zi();
    (!M || M && c === "fixed") && (p = d.offsetLeft, S = d.offsetTop);
  }
  const y = Ji(o);
  if (y <= 0) {
    const M = o.ownerDocument, z = M.body, N = getComputedStyle(z), D = M.compatMode === "CSS1Compat" && parseFloat(N.marginLeft) + parseFloat(N.marginRight) || 0, Y = Math.abs(o.clientWidth - z.clientWidth - D);
    Y <= gv && (m -= Y);
  } else y <= gv && (m += y);
  return {
    width: m,
    height: v,
    x: p,
    y: S
  };
}
function T1(i, c) {
  const s = rn(i, !0, c === "fixed"), o = s.top + i.clientTop, d = s.left + i.clientLeft, m = je(i) ? Pn(i) : Le(1), v = i.clientWidth * m.x, p = i.clientHeight * m.y, S = d * m.x, y = o * m.y;
  return {
    width: v,
    height: p,
    x: S,
    y
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
    const d = ay(i);
    o = {
      x: c.x - d.x,
      y: c.y - d.y,
      width: c.width,
      height: c.height
    };
  }
  return qi(o);
}
function iy(i, c) {
  const s = cl(i);
  return s === c || !Lt(s) || il(s) ? !1 : Se(s).position === "fixed" || iy(s, c);
}
function A1(i, c) {
  const s = c.get(i);
  if (s)
    return s;
  let o = Bl(i, [], !1).filter((p) => Lt(p) && ea(p) !== "body"), d = null;
  const m = Se(i).position === "fixed";
  let v = m ? cl(i) : i;
  for (; Lt(v) && !il(v); ) {
    const p = Se(v), S = Vo(v);
    !S && p.position === "fixed" && (d = null), (m ? !S && !d : !S && p.position === "static" && !!d && (d.position === "absolute" || d.position === "fixed") || uu(v) && !S && iy(i, v)) ? o = o.filter((M) => M !== v) : d = p, v = cl(v);
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
  const v = [...s === "clippingAncestors" ? Vi(c) ? [] : A1(c, this._c) : [].concat(s), o], p = bv(c, v[0], d);
  let S = p.top, y = p.right, M = p.bottom, z = p.left;
  for (let N = 1; N < v.length; N++) {
    const D = bv(c, v[N], d);
    S = on(D.top, S), y = ta(D.right, y), M = ta(D.bottom, M), z = on(D.left, z);
  }
  return {
    width: y - z,
    height: M - S,
    x: z,
    y: S
  };
}
function O1(i) {
  const {
    width: c,
    height: s
  } = ny(i);
  return {
    width: c,
    height: s
  };
}
function z1(i, c, s) {
  const o = je(c), d = Ye(c), m = s === "fixed", v = rn(i, !0, m, c);
  let p = {
    scrollLeft: 0,
    scrollTop: 0
  };
  const S = Le(0);
  function y() {
    S.x = Ji(d);
  }
  if (o || !o && !m)
    if ((ea(c) !== "body" || uu(d)) && (p = Ki(c)), o) {
      const D = rn(c, !0, m, c);
      S.x = D.x + c.clientLeft, S.y = D.y + c.clientTop;
    } else d && y();
  m && !o && d && y();
  const M = d && !o && !m ? uy(d, p) : Le(0), z = v.left + p.scrollLeft - S.x - M.x, N = v.top + p.scrollTop - S.y - M.y;
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
function cy(i, c) {
  const s = be(i);
  if (Vi(i))
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
  return o && il(o) && Oo(o) && !Vo(o) ? s : o || Ag(i) || s;
}
const _1 = async function(i) {
  const c = this.getOffsetParent || cy, s = this.getDimensions, o = await s(i.floating);
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
  getOffsetParent: cy,
  getElementRects: _1,
  getClientRects: S1,
  getDimensions: O1,
  getScale: Pn,
  isElement: Lt,
  isRTL: M1
};
function fy(i, c) {
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
    const y = i.getBoundingClientRect(), {
      left: M,
      top: z,
      width: N,
      height: D
    } = y;
    if (p || c(), !N || !D)
      return;
    const Y = zi(z), L = zi(d.clientWidth - (M + N)), V = zi(d.clientHeight - (z + D)), X = zi(M), G = {
      rootMargin: -Y + "px " + -L + "px " + -V + "px " + -X + "px",
      threshold: on(0, ta(1, S)) || 1
    };
    let $ = !0;
    function at(lt) {
      const Z = lt[0].intersectionRatio;
      if (Z !== S) {
        if (!$)
          return v();
        Z ? v(!1, Z) : o = setTimeout(() => {
          v(!1, 1e-7);
        }, 1e3);
      }
      Z === 1 && !fy(y, i.getBoundingClientRect()) && v(), $ = !1;
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
  } = o, y = Jo(i), M = d || m ? [...y ? Bl(y) : [], ...c ? Bl(c) : []] : [];
  M.forEach((X) => {
    d && X.addEventListener("scroll", s, {
      passive: !0
    }), m && X.addEventListener("resize", s);
  });
  const z = y && p ? x1(y, s) : null;
  let N = -1, D = null;
  v && (D = new ResizeObserver((X) => {
    let [W] = X;
    W && W.target === y && D && c && (D.unobserve(c), cancelAnimationFrame(N), N = requestAnimationFrame(() => {
      var G;
      (G = D) == null || G.observe(c);
    })), s();
  }), y && !S && D.observe(y), c && D.observe(c));
  let Y, L = S ? rn(i) : null;
  S && V();
  function V() {
    const X = rn(i);
    L && !fy(L, X) && s(), L = X, Y = requestAnimationFrame(V);
  }
  return s(), () => {
    var X;
    M.forEach((W) => {
      d && W.removeEventListener("scroll", s), m && W.removeEventListener("resize", s);
    }), z == null || z(), (X = D) == null || X.disconnect(), D = null, S && cancelAnimationFrame(Y);
  };
}
const N1 = v1, U1 = y1, w1 = r1, pv = s1, H1 = (i, c, s) => {
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
function oy(i) {
  return typeof window > "u" ? 1 : (i.ownerDocument.defaultView || window).devicePixelRatio || 1;
}
function Ev(i, c) {
  const s = oy(i);
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
    open: y
  } = i, [M, z] = q.useState({
    x: 0,
    y: 0,
    strategy: s,
    placement: c,
    middlewareData: {},
    isPositioned: !1
  }), [N, D] = q.useState(o);
  ji(N, o) || D(o);
  const [Y, L] = q.useState(null), [V, X] = q.useState(null), W = q.useCallback((H) => {
    H !== lt.current && (lt.current = H, L(H));
  }, []), G = q.useCallback((H) => {
    H !== Z.current && (Z.current = H, X(H));
  }, []), $ = m || Y, at = v || V, lt = q.useRef(null), Z = q.useRef(null), it = q.useRef(M), zt = S != null, Tt = zo(S), _t = zo(d), dt = zo(y), Ct = q.useCallback(() => {
    if (!lt.current || !Z.current)
      return;
    const H = {
      placement: c,
      strategy: s,
      middleware: N
    };
    _t.current && (H.platform = _t.current), H1(lt.current, Z.current, H).then((F) => {
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
    y === !1 && it.current.isPositioned && (it.current.isPositioned = !1, z((H) => ({
      ...H,
      isPositioned: !1
    })));
  }, [y]);
  const Q = q.useRef(!1);
  xi(() => (Q.current = !0, () => {
    Q.current = !1;
  }), []), xi(() => {
    if ($ && (lt.current = $), at && (Z.current = at), $ && at) {
      if (Tt.current)
        return Tt.current($, at, Ct);
      Ct();
    }
  }, [$, at, Ct, Tt, zt]);
  const ft = q.useMemo(() => ({
    reference: lt,
    floating: Z,
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
      ...oy(R.floating) >= 1.5 && {
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
}, V1 = "data-floating-ui-focusable", Tv = "active", Av = "selected", Z1 = {
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
  return ql(() => {
    i == null && c(Ov());
  }, []), q.useEffect(() => {
    Rv = !0;
  }, []), i;
}
const W1 = Z1.useId, Wi = W1 || J1, k1 = /* @__PURE__ */ q.forwardRef(function(c, s) {
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
    tipRadius: y = 0,
    strokeWidth: M = 0,
    staticOffset: z,
    stroke: N,
    d: D,
    style: {
      transform: Y,
      ...L
    } = {},
    ...V
  } = c, X = Wi(), [W, G] = q.useState(!1);
  if (ql(() => {
    if (!d) return;
    Se(d).direction === "rtl" && G(!0);
  }, [d]), !d)
    return null;
  const [$, at] = o.split("-"), lt = $ === "top" || $ === "bottom";
  let Z = z;
  (lt && v != null && v.x || !lt && v != null && v.y) && (Z = null);
  const it = M * 2, zt = it / 2, Tt = p / 2 * (y / -8 + 1), _t = S / 2 * y / 4, dt = !!D, Ct = Z && at === "end" ? "bottom" : "top";
  let Q = Z && at === "end" ? "right" : "left";
  Z && W && (Q = at === "end" ? "left" : "right");
  const ft = (m == null ? void 0 : m.x) != null ? Z || m.x : "", R = (m == null ? void 0 : m.y) != null ? Z || m.y : "", B = D || "M0,0" + (" H" + p) + (" L" + (p - Tt) + "," + (S - _t)) + (" Q" + p / 2 + "," + S + " " + Tt + "," + (S - _t)) + " Z", H = {
    top: dt ? "rotate(180deg)" : "",
    left: dt ? "rotate(90deg)" : "rotate(-90deg)",
    bottom: dt ? "" : "rotate(180deg)",
    right: dt ? "rotate(-90deg)" : "rotate(90deg)"
  }[$];
  return /* @__PURE__ */ Vt.jsxs("svg", {
    ...V,
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
    children: [it > 0 && /* @__PURE__ */ Vt.jsx("path", {
      clipPath: "url(#" + X + ")",
      fill: "none",
      stroke: N,
      strokeWidth: it + (D ? 0 : 1),
      d: B
    }), /* @__PURE__ */ Vt.jsx("path", {
      stroke: it && !D ? V.fill : "none",
      d: B
    }), /* @__PURE__ */ Vt.jsx("clipPath", {
      id: X,
      children: /* @__PURE__ */ Vt.jsx("rect", {
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
const $1 = /* @__PURE__ */ q.createContext(null), I1 = /* @__PURE__ */ q.createContext(null), sy = () => {
  var i;
  return ((i = q.useContext($1)) == null ? void 0 : i.id) || null;
}, ry = () => q.useContext(I1);
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
  ql(() => {
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
  return /* @__PURE__ */ Vt.jsx("span", {
    ...c,
    ...m
  });
}), tb = {
  clipPath: "inset(50%)",
  position: "fixed",
  top: 0,
  left: 0
}, dy = /* @__PURE__ */ q.createContext(null), _v = /* @__PURE__ */ Wo("portal");
function eb(i) {
  i === void 0 && (i = {});
  const {
    id: c,
    root: s
  } = i, o = Wi(), d = nb(), [m, v] = q.useState(null), p = q.useRef(null);
  return ql(() => () => {
    m == null || m.remove(), queueMicrotask(() => {
      p.current = null;
    });
  }, [m]), ql(() => {
    if (!o || p.current) return;
    const S = c ? document.getElementById(c) : null;
    if (!S) return;
    const y = document.createElement("div");
    y.id = o, y.setAttribute(_v, ""), S.appendChild(y), p.current = y, v(y);
  }, [c, o]), ql(() => {
    if (s === null || !o || p.current) return;
    let S = s || (d == null ? void 0 : d.portalNode);
    S && !Qo(S) && (S = S.current), S = S || document.body;
    let y = null;
    c && (y = document.createElement("div"), y.id = c, S.appendChild(y));
    const M = document.createElement("div");
    M.id = o, M.setAttribute(_v, ""), S = y || S, S.appendChild(M), p.current = M, v(M);
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
  }), [v, p] = q.useState(null), S = q.useRef(null), y = q.useRef(null), M = q.useRef(null), z = q.useRef(null), N = v == null ? void 0 : v.modal, D = v == null ? void 0 : v.open, Y = (
    // The FocusManager and therefore floating element are currently open/
    // rendered.
    !!v && // Guards are only for non-modal focus management.
    !v.modal && // Don't render if unmount is transitioning.
    v.open && d && !!(o || m)
  );
  return q.useEffect(() => {
    if (!m || !d || N)
      return;
    function L(V) {
      m && Ro(V) && (V.type === "focusin" ? yv : i1)(m);
    }
    return m.addEventListener("focusin", L, !0), m.addEventListener("focusout", L, !0), () => {
      m.removeEventListener("focusin", L, !0), m.removeEventListener("focusout", L, !0);
    };
  }, [m, d, N]), q.useEffect(() => {
    m && (D || yv(m));
  }, [D, m]), /* @__PURE__ */ Vt.jsxs(dy.Provider, {
    value: q.useMemo(() => ({
      preserveTabOrder: d,
      beforeOutsideRef: S,
      afterOutsideRef: y,
      beforeInsideRef: M,
      afterInsideRef: z,
      portalNode: m,
      setFocusManagerState: p
    }), [d, m]),
    children: [Y && m && /* @__PURE__ */ Vt.jsx(zv, {
      "data-type": "outside",
      ref: S,
      onFocus: (L) => {
        if (Ro(L, m)) {
          var V;
          (V = M.current) == null || V.focus();
        } else {
          const X = v ? v.domReference : null, W = u1(X);
          W == null || W.focus();
        }
      }
    }), Y && m && /* @__PURE__ */ Vt.jsx("span", {
      "aria-owns": m.id,
      style: tb
    }), m && /* @__PURE__ */ Gi.createPortal(c, m), Y && m && /* @__PURE__ */ Vt.jsx(zv, {
      "data-type": "outside",
      ref: y,
      onFocus: (L) => {
        if (Ro(L, m)) {
          var V;
          (V = z.current) == null || V.focus();
        } else {
          const X = v ? v.domReference : null, W = a1(X);
          W == null || W.focus(), v != null && v.closeOnFocusOut && (v == null || v.onOpenChange(!1, L.nativeEvent, "focus-out"));
        }
      }
    })]
  });
}
const nb = () => q.useContext(dy), ab = {
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
    outsidePressEvent: y = "pointerdown",
    referencePress: M = !1,
    referencePressEvent: z = "pointerdown",
    ancestorScroll: N = !1,
    bubbles: D,
    capture: Y
  } = c, L = ry(), V = In(typeof S == "function" ? S : () => !1), X = typeof S == "function" ? V : S, W = q.useRef(!1), {
    escapeKey: G,
    outsidePress: $
  } = Mv(D), {
    escapeKey: at,
    outsidePress: lt
  } = Mv(Y), Z = q.useRef(!1), it = In((Q) => {
    var ft;
    if (!s || !v || !p || Q.key !== "Escape" || Z.current)
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
  }), zt = In((Q) => {
    var ft;
    const R = () => {
      var B;
      it(Q), (B = Pa(Q)) == null || B.removeEventListener("keydown", R);
    };
    (ft = Pa(Q)) == null || ft.addEventListener("keydown", R);
  }), Tt = In((Q) => {
    var ft;
    const R = m.current.insideReactTree;
    m.current.insideReactTree = !1;
    const B = W.current;
    if (W.current = !1, y === "click" && B || R || typeof X == "function" && !X(Q))
      return;
    const H = Pa(Q), F = "[" + Wo("inert") + "]", et = au(d.floating).querySelectorAll(F);
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
      const I = il(H), nt = Se(H), mt = /auto|scroll/, Jt = I || mt.test(nt.overflowX), Ut = I || mt.test(nt.overflowY), Ll = Jt && H.clientWidth > 0 && H.scrollWidth > H.clientWidth, mn = Ut && H.clientHeight > 0 && H.scrollHeight > H.clientHeight, la = nt.direction === "rtl", fu = mn && (la ? Q.offsetX <= H.offsetWidth - H.clientWidth : Q.offsetX > H.clientWidth), Xe = Ll && Q.offsetY > H.clientHeight;
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
  }), _t = In((Q) => {
    var ft;
    const R = () => {
      var B;
      Tt(Q), (B = Pa(Q)) == null || B.removeEventListener(y, R);
    };
    (ft = Pa(Q)) == null || ft.addEventListener(y, R);
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
      window.clearTimeout(Q), Z.current = !0;
    }
    function B() {
      Q = window.setTimeout(
        () => {
          Z.current = !1;
        },
        // 0ms or 1ms don't work in Safari. 5ms appears to consistently work.
        // Only apply to WebKit for the test to remain 0ms.
        Zi() ? 5 : 0
      );
    }
    const H = au(d.floating);
    p && (H.addEventListener("keydown", at ? zt : it, at), H.addEventListener("compositionstart", R), H.addEventListener("compositionend", B)), X && H.addEventListener(y, lt ? _t : Tt, lt);
    let F = [];
    return N && (Lt(d.domReference) && (F = Bl(d.domReference)), Lt(d.floating) && (F = F.concat(Bl(d.floating))), !Lt(d.reference) && d.reference && d.reference.contextElement && (F = F.concat(Bl(d.reference.contextElement)))), F = F.filter((et) => {
      var g;
      return et !== ((g = H.defaultView) == null ? void 0 : g.visualViewport);
    }), F.forEach((et) => {
      et.addEventListener("scroll", ft, {
        passive: !0
      });
    }), () => {
      p && (H.removeEventListener("keydown", at ? zt : it, at), H.removeEventListener("compositionstart", R), H.removeEventListener("compositionend", B)), X && H.removeEventListener(y, lt ? _t : Tt, lt), F.forEach((et) => {
        et.removeEventListener("scroll", ft);
      }), window.clearTimeout(Q);
    };
  }, [m, d, p, X, y, s, o, N, v, G, $, it, at, zt, Tt, lt, _t]), q.useEffect(() => {
    m.current.insideReactTree = !1;
  }, [m, X, y]);
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
      [ub[y]]: () => {
        m.current.insideReactTree = !0;
      }
    };
  }, [it, y, m]);
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
  } = i, d = Wi(), m = q.useRef({}), [v] = q.useState(() => F1()), p = sy() != null, [S, y] = q.useState(o.reference), M = In((D, Y, L) => {
    m.current.openEvent = D ? Y : void 0, v.emit("openchange", {
      open: D,
      event: Y,
      reason: L,
      nested: p
    }), s == null || s(D, Y, L);
  }), z = q.useMemo(() => ({
    setPositionReference: y
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
  }), o = i.rootContext || s, d = o.elements, [m, v] = q.useState(null), [p, S] = q.useState(null), M = (d == null ? void 0 : d.domReference) || m, z = q.useRef(null), N = ry();
  ql(() => {
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
  }, [D.refs]), V = q.useMemo(() => ({
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
    refs: V,
    elements: X,
    nodeId: c
  }), [D, V, X, c, o]);
  return ql(() => {
    o.dataRef.current.floatingContext = W;
    const G = N == null ? void 0 : N.nodesRef.current.find(($) => $.id === c);
    G && (G.context = W);
  }), q.useMemo(() => ({
    ...D,
    context: W,
    refs: V,
    elements: X
  }), [D, V, X, W]);
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
      [V1]: ""
    },
    ...m,
    ...c.map((v) => {
      const p = v ? v[s] : null;
      return typeof p == "function" ? i ? p(i) : null : p;
    }).concat(i).reduce((v, p) => (p && Object.entries(p).forEach((S) => {
      let [y, M] = S;
      if (!(d && [Tv, Av].includes(y)))
        if (y.indexOf("on") === 0) {
          if (o.has(y) || o.set(y, []), typeof M == "function") {
            var z;
            (z = o.get(y)) == null || z.push(M), v[y] = function() {
              for (var N, D = arguments.length, Y = new Array(D), L = 0; L < D; L++)
                Y[L] = arguments[L];
              return (N = o.get(y)) == null ? void 0 : N.map((V) => V(...Y)).find((V) => V !== void 0);
            };
          }
        } else
          v[y] = M;
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
  } = c, y = Wi(), M = ((s = m.domReference) == null ? void 0 : s.id) || y, z = q.useMemo(() => {
    var W;
    return ((W = $g(m.floating)) == null ? void 0 : W.id) || v;
  }, [m.floating, v]), N = (o = sb.get(S)) != null ? o : S, Y = sy() != null, L = q.useMemo(() => N === "tooltip" || S === "label" ? {
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
  }, [N, z, Y, d, M, S]), V = q.useMemo(() => {
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
    floating: V,
    item: X
  } : {}, [p, L, V, X]);
}
function db(i) {
  const { anchor: c, data: s, onClose: o, onEnter: d, onLeave: m } = i, v = un.useRef(null), { refs: p, floatingStyles: S, context: y } = fb({
    open: !0,
    onOpenChange: (D) => {
      D || o();
    },
    placement: "top",
    elements: { reference: c },
    middleware: [j1(10), G1(), X1({ padding: 8 }), Q1({ element: v })],
    whileElementsMounted: D1
  }), M = ib(y, { outsidePress: !0, escapeKey: !0 }), z = rb(y, { role: "tooltip" }), { getFloatingProps: N } = ob([M, z]);
  return /* @__PURE__ */ Vt.jsx(lb, { children: /* @__PURE__ */ Vt.jsxs(
    "div",
    {
      ref: p.setFloating,
      style: s.interactive ? S : { ...S, pointerEvents: "none" },
      className: "tl-tooltip-popover" + (s.interactive ? " tl-tooltip-popover--interactive" : ""),
      onPointerEnter: d,
      onPointerLeave: m,
      ...N(),
      children: [
        s.caption ? /* @__PURE__ */ Vt.jsx("div", { className: "tl-tooltip-caption", children: s.caption }) : null,
        s.html != null ? /* @__PURE__ */ Vt.jsx(
          "div",
          {
            className: "tl-tooltip-body",
            dangerouslySetInnerHTML: { __html: s.html }
          }
        ) : /* @__PURE__ */ Vt.jsx("div", { className: "tl-tooltip-body", children: s.text ?? "" }),
        /* @__PURE__ */ Vt.jsx(
          k1,
          {
            ref: v,
            context: y,
            className: "tl-tooltip-arrow",
            width: 12,
            height: 6
          }
        )
      ]
    }
  ) });
}
const mb = 400, vb = 150, yb = 400, _i = /* @__PURE__ */ new Map();
let an = null, Di = null, tu = null, eu = null, Hl = null;
function Cv() {
  an || (an = document.createElement("div"), an.id = "tl-tooltip-host", document.body.appendChild(an), Di = Gv.createRoot(an), ki(), document.addEventListener("pointerover", hb, !0), document.addEventListener("pointerout", gb, !0));
}
function hb(i) {
  const c = i.target;
  if (!c) return;
  const s = my(c);
  if (!s) return;
  gy(), hy();
  const o = Sb(s, c);
  if (!o) return;
  const d = s.kind === "dynamic" ? c : s.el;
  pb(d, o);
}
function gb(i) {
  const c = i.relatedTarget;
  c && an && an.contains(c) || c && my(c) || (hy(), yy());
}
function my(i) {
  var s;
  let c = i;
  for (; c; ) {
    const o = (s = c.getAttribute) == null ? void 0 : s.call(c, "data-tooltip");
    if (o != null) {
      const d = bb(o, c);
      if (d) return d;
    }
    c = c.parentElement;
  }
  return null;
}
function bb(i, c) {
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
      const m = vy(c);
      return m ? { kind: "key", controlId: m, key: d, el: c } : null;
    }
    default:
      return null;
  }
}
function vy(i) {
  let c = i;
  for (; c; ) {
    const s = c.id;
    if (s && vg(s)) return s;
    c = c.parentElement;
  }
  return null;
}
function Sb(i, c) {
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
      const d = vy(i.host);
      if (!d) return null;
      const m = d + "\0" + o.key;
      let v = _i.get(m);
      return v || (v = xv(d, o.key), _i.set(m, v)), v;
    }
  }
}
function pb(i, c) {
  tu = window.setTimeout(async () => {
    tu = null;
    const s = await c;
    s && document.contains(i) && (Hl = { anchor: i, data: s }, ki());
  }, mb);
}
function yy() {
  const i = Hl != null && Hl.data.interactive ? yb : vb;
  eu = window.setTimeout(() => {
    eu = null, Hl = null, ki();
  }, i);
}
function hy() {
  tu != null && (window.clearTimeout(tu), tu = null);
}
function gy() {
  eu != null && (window.clearTimeout(eu), eu = null);
}
function ki() {
  if (!Di) return;
  if (!Hl) {
    Di.render(null);
    return;
  }
  const { anchor: i, data: c } = Hl;
  Di.render(
    un.createElement(db, {
      anchor: i,
      data: c,
      onClose: () => {
        Hl = null, ki();
      },
      onEnter: gy,
      onLeave: yy
    })
  );
}
async function xv(i, c) {
  const s = document.body.dataset.windowName ?? "", o = fn() + `react-api/tooltip?controlId=${encodeURIComponent(i)}&key=${encodeURIComponent(c)}&windowName=${encodeURIComponent(s)}`, d = await fetch(o, { credentials: "same-origin" });
  return d.ok ? await d.json() : null;
}
window.TLReact = { mount: Go, mountField: mg, discoverAndMount: wi, subscribe: Xo, unsubscribe: Yv };
lg();
document.readyState === "loading" ? window.addEventListener("DOMContentLoaded", () => Cv(), { once: !0 }) : Cv();
export {
  un as React,
  Ab as ReactDOM,
  _b as TLChild,
  dn as TLControlContext,
  Im as connect,
  yg as createChildContext,
  wi as discoverAndMount,
  Nv as getComponent,
  vg as isMountedControl,
  Go as mount,
  mg as mountField,
  Eb as register,
  Xo as subscribe,
  Ui as unmount,
  Yv as unsubscribe,
  Tb as useI18N,
  gg as useTLCommand,
  Ob as useTLDataUrl,
  zb as useTLFieldValue,
  hg as useTLState,
  Rb as useTLUpload
};
