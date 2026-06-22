function lg(i, c) {
  for (var o = 0; o < c.length; o++) {
    const f = c[o];
    if (typeof f != "string" && !Array.isArray(f)) {
      for (const r in f)
        if (r !== "default" && !(r in i)) {
          const m = Object.getOwnPropertyDescriptor(f, r);
          m && Object.defineProperty(i, r, m.get ? m : {
            enumerable: !0,
            get: () => f[r]
          });
        }
    }
  }
  return Object.freeze(Object.defineProperty(i, Symbol.toStringTag, { value: "Module" }));
}
const Yh = /* @__PURE__ */ new Map();
function fS(i, c) {
  Yh.set(i, c);
}
function jh(i) {
  return Yh.get(i);
}
function Xh(i) {
  return i && i.__esModule && Object.prototype.hasOwnProperty.call(i, "default") ? i.default : i;
}
var bo = { exports: {} }, ut = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var Pm;
function ng() {
  if (Pm) return ut;
  Pm = 1;
  var i = Symbol.for("react.transitional.element"), c = Symbol.for("react.portal"), o = Symbol.for("react.fragment"), f = Symbol.for("react.strict_mode"), r = Symbol.for("react.profiler"), m = Symbol.for("react.consumer"), h = Symbol.for("react.context"), p = Symbol.for("react.forward_ref"), b = Symbol.for("react.suspense"), v = Symbol.for("react.memo"), M = Symbol.for("react.lazy"), z = Symbol.for("react.activity"), N = Symbol.iterator;
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
  }, q = Object.assign, V = {};
  function X(g, U, j) {
    this.props = g, this.context = U, this.refs = V, this.updater = j || Y;
  }
  X.prototype.isReactComponent = {}, X.prototype.setState = function(g, U) {
    if (typeof g != "object" && typeof g != "function" && g != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, g, U, "setState");
  }, X.prototype.forceUpdate = function(g) {
    this.updater.enqueueForceUpdate(this, g, "forceUpdate");
  };
  function W() {
  }
  W.prototype = X.prototype;
  function G(g, U, j) {
    this.props = g, this.context = U, this.refs = V, this.updater = j || Y;
  }
  var $ = G.prototype = new W();
  $.constructor = G, q($, X.prototype), $.isPureReactComponent = !0;
  var at = Array.isArray;
  function lt() {
  }
  var Z = { H: null, A: null, T: null, S: null }, it = Object.prototype.hasOwnProperty;
  function zt(g, U, j) {
    var K = j.ref;
    return {
      $$typeof: i,
      type: g,
      key: U,
      ref: K !== void 0 ? K : null,
      props: j
    };
  }
  function Tt(g, U) {
    return zt(g.type, U, g.props);
  }
  function _t(g) {
    return typeof g == "object" && g !== null && g.$$typeof === i;
  }
  function dt(g) {
    var U = { "=": "=0", ":": "=2" };
    return "$" + g.replace(/[=:]/g, function(j) {
      return U[j];
    });
  }
  var Ct = /\/+/g;
  function Q(g, U) {
    return typeof g == "object" && g !== null && g.key != null ? dt("" + g.key) : U.toString(36);
  }
  function ft(g) {
    switch (g.status) {
      case "fulfilled":
        return g.value;
      case "rejected":
        throw g.reason;
      default:
        switch (typeof g.status == "string" ? g.then(lt, lt) : (g.status = "pending", g.then(
          function(U) {
            g.status === "pending" && (g.status = "fulfilled", g.value = U);
          },
          function(U) {
            g.status === "pending" && (g.status = "rejected", g.reason = U);
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
  function R(g, U, j, K, I) {
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
                U,
                j,
                K,
                I
              );
          }
      }
    if (mt)
      return I = I(g), mt = K === "" ? "." + Q(g, 0) : K, at(I) ? (j = "", mt != null && (j = mt.replace(Ct, "$&/") + "/"), R(I, U, j, "", function(jl) {
        return jl;
      })) : I != null && (_t(I) && (I = Tt(
        I,
        j + (I.key == null || g && g.key === I.key ? "" : ("" + I.key).replace(
          Ct,
          "$&/"
        ) + "/") + mt
      )), U.push(I)), 1;
    mt = 0;
    var Jt = K === "" ? "." : K + ":";
    if (at(g))
      for (var wt = 0; wt < g.length; wt++)
        K = g[wt], nt = Jt + Q(K, wt), mt += R(
          K,
          U,
          j,
          nt,
          I
        );
    else if (wt = D(g), typeof wt == "function")
      for (g = wt.call(g), wt = 0; !(K = g.next()).done; )
        K = K.value, nt = Jt + Q(K, wt++), mt += R(
          K,
          U,
          j,
          nt,
          I
        );
    else if (nt === "object") {
      if (typeof g.then == "function")
        return R(
          ft(g),
          U,
          j,
          K,
          I
        );
      throw U = String(g), Error(
        "Objects are not valid as a React child (found: " + (U === "[object Object]" ? "object with keys {" + Object.keys(g).join(", ") + "}" : U) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return mt;
  }
  function B(g, U, j) {
    if (g == null) return g;
    var K = [], I = 0;
    return R(g, K, "", "", function(nt) {
      return U.call(j, nt, I++);
    }), K;
  }
  function H(g) {
    if (g._status === -1) {
      var U = g._result;
      U = U(), U.then(
        function(j) {
          (g._status === 0 || g._status === -1) && (g._status = 1, g._result = j);
        },
        function(j) {
          (g._status === 0 || g._status === -1) && (g._status = 2, g._result = j);
        }
      ), g._status === -1 && (g._status = 0, g._result = U);
    }
    if (g._status === 1) return g._result.default;
    throw g._result;
  }
  var F = typeof reportError == "function" ? reportError : function(g) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var U = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof g == "object" && g !== null && typeof g.message == "string" ? String(g.message) : String(g),
        error: g
      });
      if (!window.dispatchEvent(U)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", g);
      return;
    }
    console.error(g);
  }, et = {
    map: B,
    forEach: function(g, U, j) {
      B(
        g,
        function() {
          U.apply(this, arguments);
        },
        j
      );
    },
    count: function(g) {
      var U = 0;
      return B(g, function() {
        U++;
      }), U;
    },
    toArray: function(g) {
      return B(g, function(U) {
        return U;
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
  return ut.Activity = z, ut.Children = et, ut.Component = X, ut.Fragment = o, ut.Profiler = r, ut.PureComponent = G, ut.StrictMode = f, ut.Suspense = b, ut.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = Z, ut.__COMPILER_RUNTIME = {
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
  }, ut.cloneElement = function(g, U, j) {
    if (g == null)
      throw Error(
        "The argument must be a React element, but you passed " + g + "."
      );
    var K = q({}, g.props), I = g.key;
    if (U != null)
      for (nt in U.key !== void 0 && (I = "" + U.key), U)
        !it.call(U, nt) || nt === "key" || nt === "__self" || nt === "__source" || nt === "ref" && U.ref === void 0 || (K[nt] = U[nt]);
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
      $$typeof: h,
      _currentValue: g,
      _currentValue2: g,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, g.Provider = g, g.Consumer = {
      $$typeof: m,
      _context: g
    }, g;
  }, ut.createElement = function(g, U, j) {
    var K, I = {}, nt = null;
    if (U != null)
      for (K in U.key !== void 0 && (nt = "" + U.key), U)
        it.call(U, K) && K !== "key" && K !== "__self" && K !== "__source" && (I[K] = U[K]);
    var mt = arguments.length - 2;
    if (mt === 1) I.children = j;
    else if (1 < mt) {
      for (var Jt = Array(mt), wt = 0; wt < mt; wt++)
        Jt[wt] = arguments[wt + 2];
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
  }, ut.memo = function(g, U) {
    return {
      $$typeof: v,
      type: g,
      compare: U === void 0 ? null : U
    };
  }, ut.startTransition = function(g) {
    var U = Z.T, j = {};
    Z.T = j;
    try {
      var K = g(), I = Z.S;
      I !== null && I(j, K), typeof K == "object" && K !== null && typeof K.then == "function" && K.then(lt, F);
    } catch (nt) {
      F(nt);
    } finally {
      U !== null && j.types !== null && (U.types = j.types), Z.T = U;
    }
  }, ut.unstable_useCacheRefresh = function() {
    return Z.H.useCacheRefresh();
  }, ut.use = function(g) {
    return Z.H.use(g);
  }, ut.useActionState = function(g, U, j) {
    return Z.H.useActionState(g, U, j);
  }, ut.useCallback = function(g, U) {
    return Z.H.useCallback(g, U);
  }, ut.useContext = function(g) {
    return Z.H.useContext(g);
  }, ut.useDebugValue = function() {
  }, ut.useDeferredValue = function(g, U) {
    return Z.H.useDeferredValue(g, U);
  }, ut.useEffect = function(g, U) {
    return Z.H.useEffect(g, U);
  }, ut.useEffectEvent = function(g) {
    return Z.H.useEffectEvent(g);
  }, ut.useId = function() {
    return Z.H.useId();
  }, ut.useImperativeHandle = function(g, U, j) {
    return Z.H.useImperativeHandle(g, U, j);
  }, ut.useInsertionEffect = function(g, U) {
    return Z.H.useInsertionEffect(g, U);
  }, ut.useLayoutEffect = function(g, U) {
    return Z.H.useLayoutEffect(g, U);
  }, ut.useMemo = function(g, U) {
    return Z.H.useMemo(g, U);
  }, ut.useOptimistic = function(g, U) {
    return Z.H.useOptimistic(g, U);
  }, ut.useReducer = function(g, U, j) {
    return Z.H.useReducer(g, U, j);
  }, ut.useRef = function(g) {
    return Z.H.useRef(g);
  }, ut.useState = function(g) {
    return Z.H.useState(g);
  }, ut.useSyncExternalStore = function(g, U, j) {
    return Z.H.useSyncExternalStore(
      g,
      U,
      j
    );
  }, ut.useTransition = function() {
    return Z.H.useTransition();
  }, ut.version = "19.2.4", ut;
}
var th;
function jo() {
  return th || (th = 1, bo.exports = ng()), bo.exports;
}
var L = jo();
const te = /* @__PURE__ */ Xh(L), Gh = /* @__PURE__ */ lg({
  __proto__: null,
  default: te
}, [L]), Xo = /* @__PURE__ */ new Map(), Ci = /* @__PURE__ */ new Set();
let xo = !1, Go = 0;
const Do = /* @__PURE__ */ new Set();
let Qh = "", Vh = "";
function ag(i) {
  Qh = i;
}
function ug(i) {
  Vh = i;
}
function Zh() {
  for (const i of Do)
    i();
}
function ig(i) {
  return Do.add(i), () => Do.delete(i);
}
function cg() {
  return Go;
}
function fg(i) {
  Ci.add(i), xo || (xo = !0, queueMicrotask(og));
}
async function og() {
  if (xo = !1, Ci.size === 0)
    return;
  const i = Array.from(Ci);
  Ci.clear();
  try {
    const c = Qh + "react-api/i18n?keys=" + encodeURIComponent(i.join(",")) + "&windowName=" + encodeURIComponent(Vh), o = await fetch(c);
    if (!o.ok) {
      console.error("[TLReact] i18n fetch failed:", o.status);
      return;
    }
    const f = await o.json();
    for (const [r, m] of Object.entries(f))
      Xo.set(r, m);
    Go++, Zh();
  } catch (c) {
    console.error("[TLReact] i18n fetch error:", c);
  }
}
function oS(i) {
  te.useSyncExternalStore(ig, cg);
  const c = {};
  for (const [o, f] of Object.entries(i)) {
    const r = Xo.get(o);
    r !== void 0 ? c[o] = r : (c[o] = f, fg(o));
  }
  return c;
}
function Kh() {
  Xo.clear(), Go++, Zh();
}
const fn = /* @__PURE__ */ new Map();
let Fa = null;
function Qo() {
  return document.body.dataset.windowName ?? "";
}
function Vi() {
  return document.body.dataset.contextPath ?? "";
}
function sg(i) {
  const c = [];
  return i.width > 0 && c.push(`width=${i.width}`), i.height > 0 && c.push(`height=${i.height}`), c.push(`resizable=${i.resizable ? "yes" : "no"}`), c.join(",");
}
function rg() {
  Fa === null && (Fa = setInterval(() => {
    for (const [i, c] of fn)
      c.closed && (fn.delete(i), dg(i));
    fn.size === 0 && Fa !== null && (clearInterval(Fa), Fa = null);
  }, 2e3));
}
function dg(i) {
  const c = Vi(), o = Qo();
  fetch(`${c}/react-api/command`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      controlId: "",
      command: "windowClosed",
      windowName: o,
      arguments: { windowId: i }
    })
  }).catch(() => {
  });
}
function mg(i) {
  const o = `${Vi()}/view/${i.windowId}/`, f = window.open(o, i.windowId, sg(i));
  f ? (fn.set(i.windowId, f), rg()) : yg(i.windowId);
}
function hg(i) {
  const c = fn.get(i.windowId);
  c && (c.close(), fn.delete(i.windowId));
}
function vg(i) {
  const c = fn.get(i.windowId);
  c && !c.closed && c.focus();
}
function yg(i) {
  const c = Vi(), o = Qo();
  fetch(`${c}/react-api/command`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      controlId: "",
      command: "windowBlocked",
      windowName: o,
      arguments: { windowId: i }
    })
  }).catch(() => {
  });
}
function gg() {
  window.addEventListener("beforeunload", () => {
    const i = Vi(), c = Qo();
    if (!c) return;
    const o = JSON.stringify({
      controlId: "",
      command: "windowClosed",
      windowName: c,
      arguments: { windowId: c }
    }), f = new Blob([o], { type: "application/json" });
    navigator.sendBeacon(`${i}/react-api/command`, f);
  });
}
let eh = !1;
function bg() {
  return document.body.dataset.windowName ?? "";
}
function Sg() {
  return document.body.dataset.contextPath ?? "";
}
function pg() {
  eh || (eh = !0, window.addEventListener("popstate", () => {
    const i = Og(), c = window.location.search;
    Ag(i + c);
  }), Rg());
}
function Eg(i) {
  const o = Vo() + i.url;
  i.replace ? history.replaceState(null, "", o) : history.pushState(null, "", o);
}
function Tg(i) {
  const c = Vo();
  history.replaceState(null, "", c + i.currentUrl);
}
function Ag(i) {
  const c = Sg(), o = bg();
  fetch(`${c}/react-api/command`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      controlId: "",
      command: "navigateToRoute",
      windowName: o,
      arguments: { url: i }
    })
  }).catch((f) => {
    console.error("[TLReact] navigateToRoute error:", f);
  });
}
function Rg() {
  const i = window.location.pathname, c = i.indexOf("/view/");
  if (c < 0) return;
  const o = i.substring(c + 6), f = o.indexOf("/");
  if (f > 0 && o.substring(0, f).match(/^v[0-9a-f]+$/i)) {
    const m = i.substring(0, c + 6) + o.substring(f + 1);
    history.replaceState(null, "", m + window.location.search);
  }
}
function Og() {
  const i = window.location.pathname, c = Vo();
  return i.substring(c.length);
}
function Vo() {
  const i = window.location.pathname, c = i.indexOf("/view/");
  return c >= 0 ? i.substring(0, c + 6) : "/";
}
const nu = /* @__PURE__ */ new Map();
let un = null, lh = null, Hi = 0, tu = null, Jh = !1, nh = !1;
const zg = 45e3, _g = 15e3;
function ah(i) {
  if (lh = i, !nh) {
    nh = !0;
    const c = () => {
      Jh = !0;
    };
    window.addEventListener("beforeunload", c), window.addEventListener("pagehide", c);
  }
  tu && clearInterval(tu), uh(i), tu = setInterval(() => {
    Hi > 0 && Date.now() - Hi > zg && (console.warn("[TLReact] No heartbeat received, reconnecting SSE."), uh(lh));
  }, _g);
}
function uh(i) {
  un && un.close(), un = new EventSource(i), Hi = Date.now(), Kh(), un.onmessage = (c) => {
    Hi = Date.now();
    try {
      const o = JSON.parse(c.data);
      Mg(o);
    } catch (o) {
      console.error("[TLReact] Failed to parse SSE event:", o);
    }
  }, un.onerror = () => {
    if (!Jh) {
      if (un && un.readyState === EventSource.CLOSED) {
        console.warn("[TLReact] SSE connection permanently closed (session lost). Reloading page."), tu && clearInterval(tu), window.location.reload();
        return;
      }
      console.warn("[TLReact] SSE connection error, will reconnect automatically.");
    }
  };
}
function Zo(i, c) {
  let o = nu.get(i);
  o || (o = /* @__PURE__ */ new Set(), nu.set(i, o)), o.add(c);
}
function Wh(i, c) {
  const o = nu.get(i);
  o && (o.delete(c), o.size === 0 && nu.delete(i));
}
function kh(i, c) {
  const o = nu.get(i);
  if (o)
    for (const f of o)
      f(c);
}
function Mg(i) {
  if (!Array.isArray(i) || i.length < 2) {
    console.warn("[TLReact] Unexpected SSE event format:", i);
    return;
  }
  const c = i[0], o = i[1];
  switch (c !== "Heartbeat" && console.log("[SSE] dispatch", c, o), c) {
    case "Heartbeat":
      break;
    case "StateEvent":
      Cg(o);
      break;
    case "PatchEvent":
      xg(o);
      break;
    case "ContentReplacement":
      Hl.contentReplacement(o);
      break;
    case "ElementReplacement":
      Hl.elementReplacement(o);
      break;
    case "PropertyUpdate":
      Hl.propertyUpdate(o);
      break;
    case "CssClassUpdate":
      Hl.cssClassUpdate(o);
      break;
    case "FragmentInsertion":
      Hl.fragmentInsertion(o);
      break;
    case "RangeReplacement":
      Hl.rangeReplacement(o);
      break;
    case "JSSnipplet":
      Hl.jsSnipplet(o);
      break;
    case "FunctionCall":
      Hl.functionCall(o);
      break;
    case "I18NCacheInvalidation":
      Kh();
      break;
    case "WindowOpenEvent":
      mg(o);
      break;
    case "WindowCloseEvent":
      hg(o);
      break;
    case "WindowFocusEvent":
      vg(o);
      break;
    case "RouteChangeEvent":
      Eg(o);
      break;
    case "RouteVetoEvent":
      Tg(o);
      break;
    default:
      console.warn("[TLReact] Unknown SSE event type:", c);
  }
}
function Cg(i) {
  const c = JSON.parse(i.state);
  kh(i.controlId, c);
}
function xg(i) {
  const c = JSON.parse(i.patch);
  kh(i.controlId, c);
}
const Hl = {
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
      for (const o of i.properties)
        c.setAttribute(o.name, o.value);
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
    const c = document.getElementById(i.startId), o = document.getElementById(i.stopId);
    if (c && o && c.parentNode) {
      const f = c.parentNode, r = document.createRange();
      r.setStartBefore(c), r.setEndAfter(o), r.deleteContents();
      const m = document.createElement("template");
      m.innerHTML = i.html, f.insertBefore(m.content, r.startContainer.childNodes[r.startOffset] || null);
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
      const c = JSON.parse(i.arguments), o = i.functionRef ? window[i.functionRef] : window, f = o == null ? void 0 : o[i.functionName];
      typeof f == "function" ? f.apply(o, c) : console.warn("[TLReact] Function not found:", i.functionRef + "." + i.functionName);
    } catch (c) {
      console.error("[TLReact] Error executing function call:", c);
    }
  }
};
var So = { exports: {} }, $a = {}, po = { exports: {} }, Eo = {};
/**
 * @license React
 * scheduler.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var ih;
function Dg() {
  return ih || (ih = 1, (function(i) {
    function c(R, B) {
      var H = R.length;
      R.push(B);
      t: for (; 0 < H; ) {
        var F = H - 1 >>> 1, et = R[F];
        if (0 < r(et, B))
          R[F] = B, R[H] = et, H = F;
        else break t;
      }
    }
    function o(R) {
      return R.length === 0 ? null : R[0];
    }
    function f(R) {
      if (R.length === 0) return null;
      var B = R[0], H = R.pop();
      if (H !== B) {
        R[0] = H;
        t: for (var F = 0, et = R.length, g = et >>> 1; F < g; ) {
          var U = 2 * (F + 1) - 1, j = R[U], K = U + 1, I = R[K];
          if (0 > r(j, H))
            K < et && 0 > r(I, j) ? (R[F] = I, R[K] = H, F = K) : (R[F] = j, R[U] = H, F = U);
          else if (K < et && 0 > r(I, H))
            R[F] = I, R[K] = H, F = K;
          else break t;
        }
      }
      return B;
    }
    function r(R, B) {
      var H = R.sortIndex - B.sortIndex;
      return H !== 0 ? H : R.id - B.id;
    }
    if (i.unstable_now = void 0, typeof performance == "object" && typeof performance.now == "function") {
      var m = performance;
      i.unstable_now = function() {
        return m.now();
      };
    } else {
      var h = Date, p = h.now();
      i.unstable_now = function() {
        return h.now() - p;
      };
    }
    var b = [], v = [], M = 1, z = null, N = 3, D = !1, Y = !1, q = !1, V = !1, X = typeof setTimeout == "function" ? setTimeout : null, W = typeof clearTimeout == "function" ? clearTimeout : null, G = typeof setImmediate < "u" ? setImmediate : null;
    function $(R) {
      for (var B = o(v); B !== null; ) {
        if (B.callback === null) f(v);
        else if (B.startTime <= R)
          f(v), B.sortIndex = B.expirationTime, c(b, B);
        else break;
        B = o(v);
      }
    }
    function at(R) {
      if (q = !1, $(R), !Y)
        if (o(b) !== null)
          Y = !0, lt || (lt = !0, dt());
        else {
          var B = o(v);
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
            Y = !1, q && (q = !1, W(Z), Z = -1), D = !0;
            var H = N;
            try {
              e: {
                for ($(R), z = o(b); z !== null && !(z.expirationTime > R && Tt()); ) {
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
                    z === o(b) && f(b), $(R);
                  } else f(b);
                  z = o(b);
                }
                if (z !== null) B = !0;
                else {
                  var g = o(v);
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
      }, H > F ? (R.sortIndex = H, c(v, R), o(b) === null && R === o(v) && (q ? (W(Z), Z = -1) : q = !0, ft(at, H - F))) : (R.sortIndex = et, c(b, R), Y || D || (Y = !0, lt || (lt = !0, dt()))), R;
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
  })(Eo)), Eo;
}
var ch;
function Ng() {
  return ch || (ch = 1, po.exports = Dg()), po.exports;
}
var To = { exports: {} }, Pt = {};
/**
 * @license React
 * react-dom.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var fh;
function wg() {
  if (fh) return Pt;
  fh = 1;
  var i = jo();
  function c(b) {
    var v = "https://react.dev/errors/" + b;
    if (1 < arguments.length) {
      v += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var M = 2; M < arguments.length; M++)
        v += "&args[]=" + encodeURIComponent(arguments[M]);
    }
    return "Minified React error #" + b + "; visit " + v + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function o() {
  }
  var f = {
    d: {
      f: o,
      r: function() {
        throw Error(c(522));
      },
      D: o,
      C: o,
      L: o,
      m: o,
      X: o,
      S: o,
      M: o
    },
    p: 0,
    findDOMNode: null
  }, r = Symbol.for("react.portal");
  function m(b, v, M) {
    var z = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: r,
      key: z == null ? null : "" + z,
      children: b,
      containerInfo: v,
      implementation: M
    };
  }
  var h = i.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function p(b, v) {
    if (b === "font") return "";
    if (typeof v == "string")
      return v === "use-credentials" ? v : "";
  }
  return Pt.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = f, Pt.createPortal = function(b, v) {
    var M = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!v || v.nodeType !== 1 && v.nodeType !== 9 && v.nodeType !== 11)
      throw Error(c(299));
    return m(b, v, null, M);
  }, Pt.flushSync = function(b) {
    var v = h.T, M = f.p;
    try {
      if (h.T = null, f.p = 2, b) return b();
    } finally {
      h.T = v, f.p = M, f.d.f();
    }
  }, Pt.preconnect = function(b, v) {
    typeof b == "string" && (v ? (v = v.crossOrigin, v = typeof v == "string" ? v === "use-credentials" ? v : "" : void 0) : v = null, f.d.C(b, v));
  }, Pt.prefetchDNS = function(b) {
    typeof b == "string" && f.d.D(b);
  }, Pt.preinit = function(b, v) {
    if (typeof b == "string" && v && typeof v.as == "string") {
      var M = v.as, z = p(M, v.crossOrigin), N = typeof v.integrity == "string" ? v.integrity : void 0, D = typeof v.fetchPriority == "string" ? v.fetchPriority : void 0;
      M === "style" ? f.d.S(
        b,
        typeof v.precedence == "string" ? v.precedence : void 0,
        {
          crossOrigin: z,
          integrity: N,
          fetchPriority: D
        }
      ) : M === "script" && f.d.X(b, {
        crossOrigin: z,
        integrity: N,
        fetchPriority: D,
        nonce: typeof v.nonce == "string" ? v.nonce : void 0
      });
    }
  }, Pt.preinitModule = function(b, v) {
    if (typeof b == "string")
      if (typeof v == "object" && v !== null) {
        if (v.as == null || v.as === "script") {
          var M = p(
            v.as,
            v.crossOrigin
          );
          f.d.M(b, {
            crossOrigin: M,
            integrity: typeof v.integrity == "string" ? v.integrity : void 0,
            nonce: typeof v.nonce == "string" ? v.nonce : void 0
          });
        }
      } else v == null && f.d.M(b);
  }, Pt.preload = function(b, v) {
    if (typeof b == "string" && typeof v == "object" && v !== null && typeof v.as == "string") {
      var M = v.as, z = p(M, v.crossOrigin);
      f.d.L(b, M, {
        crossOrigin: z,
        integrity: typeof v.integrity == "string" ? v.integrity : void 0,
        nonce: typeof v.nonce == "string" ? v.nonce : void 0,
        type: typeof v.type == "string" ? v.type : void 0,
        fetchPriority: typeof v.fetchPriority == "string" ? v.fetchPriority : void 0,
        referrerPolicy: typeof v.referrerPolicy == "string" ? v.referrerPolicy : void 0,
        imageSrcSet: typeof v.imageSrcSet == "string" ? v.imageSrcSet : void 0,
        imageSizes: typeof v.imageSizes == "string" ? v.imageSizes : void 0,
        media: typeof v.media == "string" ? v.media : void 0
      });
    }
  }, Pt.preloadModule = function(b, v) {
    if (typeof b == "string")
      if (v) {
        var M = p(v.as, v.crossOrigin);
        f.d.m(b, {
          as: typeof v.as == "string" && v.as !== "script" ? v.as : void 0,
          crossOrigin: M,
          integrity: typeof v.integrity == "string" ? v.integrity : void 0
        });
      } else f.d.m(b);
  }, Pt.requestFormReset = function(b) {
    f.d.r(b);
  }, Pt.unstable_batchedUpdates = function(b, v) {
    return b(v);
  }, Pt.useFormState = function(b, v, M) {
    return h.H.useFormState(b, v, M);
  }, Pt.useFormStatus = function() {
    return h.H.useHostTransitionStatus();
  }, Pt.version = "19.2.4", Pt;
}
var oh;
function Fh() {
  if (oh) return To.exports;
  oh = 1;
  function i() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(i);
      } catch (c) {
        console.error(c);
      }
  }
  return i(), To.exports = wg(), To.exports;
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
var sh;
function Ug() {
  if (sh) return $a;
  sh = 1;
  var i = Ng(), c = jo(), o = Fh();
  function f(t) {
    var e = "https://react.dev/errors/" + t;
    if (1 < arguments.length) {
      e += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var l = 2; l < arguments.length; l++)
        e += "&args[]=" + encodeURIComponent(arguments[l]);
    }
    return "Minified React error #" + t + "; visit " + e + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function r(t) {
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
  function h(t) {
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
  function b(t) {
    if (m(t) !== t)
      throw Error(f(188));
  }
  function v(t) {
    var e = t.alternate;
    if (!e) {
      if (e = m(t), e === null) throw Error(f(188));
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
          if (u === l) return b(a), t;
          if (u === n) return b(a), e;
          u = u.sibling;
        }
        throw Error(f(188));
      }
      if (l.return !== n.return) l = a, n = u;
      else {
        for (var s = !1, d = a.child; d; ) {
          if (d === l) {
            s = !0, l = a, n = u;
            break;
          }
          if (d === n) {
            s = !0, n = a, l = u;
            break;
          }
          d = d.sibling;
        }
        if (!s) {
          for (d = u.child; d; ) {
            if (d === l) {
              s = !0, l = u, n = a;
              break;
            }
            if (d === n) {
              s = !0, n = u, l = a;
              break;
            }
            d = d.sibling;
          }
          if (!s) throw Error(f(189));
        }
      }
      if (l.alternate !== n) throw Error(f(190));
    }
    if (l.tag !== 3) throw Error(f(188));
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
  var z = Object.assign, N = Symbol.for("react.element"), D = Symbol.for("react.transitional.element"), Y = Symbol.for("react.portal"), q = Symbol.for("react.fragment"), V = Symbol.for("react.strict_mode"), X = Symbol.for("react.profiler"), W = Symbol.for("react.consumer"), G = Symbol.for("react.context"), $ = Symbol.for("react.forward_ref"), at = Symbol.for("react.suspense"), lt = Symbol.for("react.suspense_list"), Z = Symbol.for("react.memo"), it = Symbol.for("react.lazy"), zt = Symbol.for("react.activity"), Tt = Symbol.for("react.memo_cache_sentinel"), _t = Symbol.iterator;
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
      case q:
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
  var ft = Array.isArray, R = c.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE, B = o.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE, H = {
    pending: !1,
    data: null,
    method: null,
    action: null
  }, F = [], et = -1;
  function g(t) {
    return { current: t };
  }
  function U(t) {
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
        t = (t = e.documentElement) && (t = t.namespaceURI) ? Tm(t) : 0;
        break;
      default:
        if (t = e.tagName, e = e.namespaceURI)
          e = Tm(e), t = Am(e, t);
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
    U(K), j(K, t);
  }
  function wt() {
    U(K), U(I), U(nt);
  }
  function jl(t) {
    t.memoizedState !== null && j(mt, t);
    var e = K.current, l = Am(e, t.type);
    e !== l && (j(I, t), j(K, l));
  }
  function hn(t) {
    I.current === t && (U(K), U(I)), mt.current === t && (U(mt), Ka._currentValue = H);
  }
  var na, ou;
  function Ge(t) {
    if (na === void 0)
      try {
        throw Error();
      } catch (l) {
        var e = l.stack.trim().match(/\n( *(at )?)/);
        na = e && e[1] || "", ou = -1 < l.stack.indexOf(`
    at`) ? " (<anonymous>)" : -1 < l.stack.indexOf("@") ? "@unknown:0:0" : "";
      }
    return `
` + na + t + ou;
  }
  var Pi = !1;
  function tc(t, e) {
    if (!t || Pi) return "";
    Pi = !0;
    var l = Error.prepareStackTrace;
    Error.prepareStackTrace = void 0;
    try {
      var n = {
        DetermineComponentFrameRoot: function() {
          try {
            if (e) {
              var w = function() {
                throw Error();
              };
              if (Object.defineProperty(w.prototype, "props", {
                set: function() {
                  throw Error();
                }
              }), typeof Reflect == "object" && Reflect.construct) {
                try {
                  Reflect.construct(w, []);
                } catch (_) {
                  var O = _;
                }
                Reflect.construct(t, [], w);
              } else {
                try {
                  w.call();
                } catch (_) {
                  O = _;
                }
                t.call(w.prototype);
              }
            } else {
              try {
                throw Error();
              } catch (_) {
                O = _;
              }
              (w = t()) && typeof w.catch == "function" && w.catch(function() {
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
      var u = n.DetermineComponentFrameRoot(), s = u[0], d = u[1];
      if (s && d) {
        var y = s.split(`
`), A = d.split(`
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
      Pi = !1, Error.prepareStackTrace = l;
    }
    return (l = t ? t.displayName || t.name : "") ? Ge(l) : "";
  }
  function wv(t, e) {
    switch (t.tag) {
      case 26:
      case 27:
      case 5:
        return Ge(t.type);
      case 16:
        return Ge("Lazy");
      case 13:
        return t.child !== e && e !== null ? Ge("Suspense Fallback") : Ge("Suspense");
      case 19:
        return Ge("SuspenseList");
      case 0:
      case 15:
        return tc(t.type, !1);
      case 11:
        return tc(t.type.render, !1);
      case 1:
        return tc(t.type, !0);
      case 31:
        return Ge("Activity");
      default:
        return "";
    }
  }
  function Po(t) {
    try {
      var e = "", l = null;
      do
        e += wv(t, l), l = t, t = t.return;
      while (t);
      return e;
    } catch (n) {
      return `
Error generating stack: ` + n.message + `
` + n.stack;
    }
  }
  var ec = Object.prototype.hasOwnProperty, lc = i.unstable_scheduleCallback, nc = i.unstable_cancelCallback, Uv = i.unstable_shouldYield, Hv = i.unstable_requestPaint, oe = i.unstable_now, Bv = i.unstable_getCurrentPriorityLevel, ts = i.unstable_ImmediatePriority, es = i.unstable_UserBlockingPriority, su = i.unstable_NormalPriority, Lv = i.unstable_LowPriority, ls = i.unstable_IdlePriority, qv = i.log, Yv = i.unstable_setDisableYieldValue, aa = null, se = null;
  function sl(t) {
    if (typeof qv == "function" && Yv(t), se && typeof se.setStrictMode == "function")
      try {
        se.setStrictMode(aa, t);
      } catch {
      }
  }
  var re = Math.clz32 ? Math.clz32 : Gv, jv = Math.log, Xv = Math.LN2;
  function Gv(t) {
    return t >>>= 0, t === 0 ? 32 : 31 - (jv(t) / Xv | 0) | 0;
  }
  var ru = 256, du = 262144, mu = 4194304;
  function Xl(t) {
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
  function hu(t, e, l) {
    var n = t.pendingLanes;
    if (n === 0) return 0;
    var a = 0, u = t.suspendedLanes, s = t.pingedLanes;
    t = t.warmLanes;
    var d = n & 134217727;
    return d !== 0 ? (n = d & ~u, n !== 0 ? a = Xl(n) : (s &= d, s !== 0 ? a = Xl(s) : l || (l = d & ~t, l !== 0 && (a = Xl(l))))) : (d = n & ~u, d !== 0 ? a = Xl(d) : s !== 0 ? a = Xl(s) : l || (l = n & ~t, l !== 0 && (a = Xl(l)))), a === 0 ? 0 : e !== 0 && e !== a && (e & u) === 0 && (u = a & -a, l = e & -e, u >= l || u === 32 && (l & 4194048) !== 0) ? e : a;
  }
  function ua(t, e) {
    return (t.pendingLanes & ~(t.suspendedLanes & ~t.pingedLanes) & e) === 0;
  }
  function Qv(t, e) {
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
  function ns() {
    var t = mu;
    return mu <<= 1, (mu & 62914560) === 0 && (mu = 4194304), t;
  }
  function ac(t) {
    for (var e = [], l = 0; 31 > l; l++) e.push(t);
    return e;
  }
  function ia(t, e) {
    t.pendingLanes |= e, e !== 268435456 && (t.suspendedLanes = 0, t.pingedLanes = 0, t.warmLanes = 0);
  }
  function Vv(t, e, l, n, a, u) {
    var s = t.pendingLanes;
    t.pendingLanes = l, t.suspendedLanes = 0, t.pingedLanes = 0, t.warmLanes = 0, t.expiredLanes &= l, t.entangledLanes &= l, t.errorRecoveryDisabledLanes &= l, t.shellSuspendCounter = 0;
    var d = t.entanglements, y = t.expirationTimes, A = t.hiddenUpdates;
    for (l = s & ~l; 0 < l; ) {
      var C = 31 - re(l), w = 1 << C;
      d[C] = 0, y[C] = -1;
      var O = A[C];
      if (O !== null)
        for (A[C] = null, C = 0; C < O.length; C++) {
          var _ = O[C];
          _ !== null && (_.lane &= -536870913);
        }
      l &= ~w;
    }
    n !== 0 && as(t, n, 0), u !== 0 && a === 0 && t.tag !== 0 && (t.suspendedLanes |= u & ~(s & ~e));
  }
  function as(t, e, l) {
    t.pendingLanes |= e, t.suspendedLanes &= ~e;
    var n = 31 - re(e);
    t.entangledLanes |= e, t.entanglements[n] = t.entanglements[n] | 1073741824 | l & 261930;
  }
  function us(t, e) {
    var l = t.entangledLanes |= e;
    for (t = t.entanglements; l; ) {
      var n = 31 - re(l), a = 1 << n;
      a & e | t[n] & e && (t[n] |= e), l &= ~a;
    }
  }
  function is(t, e) {
    var l = e & -e;
    return l = (l & 42) !== 0 ? 1 : uc(l), (l & (t.suspendedLanes | e)) !== 0 ? 0 : l;
  }
  function uc(t) {
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
  function ic(t) {
    return t &= -t, 2 < t ? 8 < t ? (t & 134217727) !== 0 ? 32 : 268435456 : 8 : 2;
  }
  function cs() {
    var t = B.p;
    return t !== 0 ? t : (t = window.event, t === void 0 ? 32 : Km(t.type));
  }
  function fs(t, e) {
    var l = B.p;
    try {
      return B.p = t, e();
    } finally {
      B.p = l;
    }
  }
  var rl = Math.random().toString(36).slice(2), Wt = "__reactFiber$" + rl, le = "__reactProps$" + rl, vn = "__reactContainer$" + rl, cc = "__reactEvents$" + rl, Zv = "__reactListeners$" + rl, Kv = "__reactHandles$" + rl, os = "__reactResources$" + rl, ca = "__reactMarker$" + rl;
  function fc(t) {
    delete t[Wt], delete t[le], delete t[cc], delete t[Zv], delete t[Kv];
  }
  function yn(t) {
    var e = t[Wt];
    if (e) return e;
    for (var l = t.parentNode; l; ) {
      if (e = l[vn] || l[Wt]) {
        if (l = e.alternate, e.child !== null || l !== null && l.child !== null)
          for (t = xm(t); t !== null; ) {
            if (l = t[Wt]) return l;
            t = xm(t);
          }
        return e;
      }
      t = l, l = t.parentNode;
    }
    return null;
  }
  function gn(t) {
    if (t = t[Wt] || t[vn]) {
      var e = t.tag;
      if (e === 5 || e === 6 || e === 13 || e === 31 || e === 26 || e === 27 || e === 3)
        return t;
    }
    return null;
  }
  function fa(t) {
    var e = t.tag;
    if (e === 5 || e === 26 || e === 27 || e === 6) return t.stateNode;
    throw Error(f(33));
  }
  function bn(t) {
    var e = t[os];
    return e || (e = t[os] = { hoistableStyles: /* @__PURE__ */ new Map(), hoistableScripts: /* @__PURE__ */ new Map() }), e;
  }
  function Zt(t) {
    t[ca] = !0;
  }
  var ss = /* @__PURE__ */ new Set(), rs = {};
  function Gl(t, e) {
    Sn(t, e), Sn(t + "Capture", e);
  }
  function Sn(t, e) {
    for (rs[t] = e, t = 0; t < e.length; t++)
      ss.add(e[t]);
  }
  var Jv = RegExp(
    "^[:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD][:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD\\-.0-9\\u00B7\\u0300-\\u036F\\u203F-\\u2040]*$"
  ), ds = {}, ms = {};
  function Wv(t) {
    return ec.call(ms, t) ? !0 : ec.call(ds, t) ? !1 : Jv.test(t) ? ms[t] = !0 : (ds[t] = !0, !1);
  }
  function vu(t, e, l) {
    if (Wv(e))
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
  function Qe(t, e, l, n) {
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
  function Ee(t) {
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
  function hs(t) {
    var e = t.type;
    return (t = t.nodeName) && t.toLowerCase() === "input" && (e === "checkbox" || e === "radio");
  }
  function kv(t, e, l) {
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
        set: function(s) {
          l = "" + s, u.call(this, s);
        }
      }), Object.defineProperty(t, e, {
        enumerable: n.enumerable
      }), {
        getValue: function() {
          return l;
        },
        setValue: function(s) {
          l = "" + s;
        },
        stopTracking: function() {
          t._valueTracker = null, delete t[e];
        }
      };
    }
  }
  function oc(t) {
    if (!t._valueTracker) {
      var e = hs(t) ? "checked" : "value";
      t._valueTracker = kv(
        t,
        e,
        "" + t[e]
      );
    }
  }
  function vs(t) {
    if (!t) return !1;
    var e = t._valueTracker;
    if (!e) return !0;
    var l = e.getValue(), n = "";
    return t && (n = hs(t) ? t.checked ? "true" : "false" : t.value), t = n, t !== l ? (e.setValue(t), !0) : !1;
  }
  function gu(t) {
    if (t = t || (typeof document < "u" ? document : void 0), typeof t > "u") return null;
    try {
      return t.activeElement || t.body;
    } catch {
      return t.body;
    }
  }
  var Fv = /[\n"\\]/g;
  function Te(t) {
    return t.replace(
      Fv,
      function(e) {
        return "\\" + e.charCodeAt(0).toString(16) + " ";
      }
    );
  }
  function sc(t, e, l, n, a, u, s, d) {
    t.name = "", s != null && typeof s != "function" && typeof s != "symbol" && typeof s != "boolean" ? t.type = s : t.removeAttribute("type"), e != null ? s === "number" ? (e === 0 && t.value === "" || t.value != e) && (t.value = "" + Ee(e)) : t.value !== "" + Ee(e) && (t.value = "" + Ee(e)) : s !== "submit" && s !== "reset" || t.removeAttribute("value"), e != null ? rc(t, s, Ee(e)) : l != null ? rc(t, s, Ee(l)) : n != null && t.removeAttribute("value"), a == null && u != null && (t.defaultChecked = !!u), a != null && (t.checked = a && typeof a != "function" && typeof a != "symbol"), d != null && typeof d != "function" && typeof d != "symbol" && typeof d != "boolean" ? t.name = "" + Ee(d) : t.removeAttribute("name");
  }
  function ys(t, e, l, n, a, u, s, d) {
    if (u != null && typeof u != "function" && typeof u != "symbol" && typeof u != "boolean" && (t.type = u), e != null || l != null) {
      if (!(u !== "submit" && u !== "reset" || e != null)) {
        oc(t);
        return;
      }
      l = l != null ? "" + Ee(l) : "", e = e != null ? "" + Ee(e) : l, d || e === t.value || (t.value = e), t.defaultValue = e;
    }
    n = n ?? a, n = typeof n != "function" && typeof n != "symbol" && !!n, t.checked = d ? t.checked : !!n, t.defaultChecked = !!n, s != null && typeof s != "function" && typeof s != "symbol" && typeof s != "boolean" && (t.name = s), oc(t);
  }
  function rc(t, e, l) {
    e === "number" && gu(t.ownerDocument) === t || t.defaultValue === "" + l || (t.defaultValue = "" + l);
  }
  function pn(t, e, l, n) {
    if (t = t.options, e) {
      e = {};
      for (var a = 0; a < l.length; a++)
        e["$" + l[a]] = !0;
      for (l = 0; l < t.length; l++)
        a = e.hasOwnProperty("$" + t[l].value), t[l].selected !== a && (t[l].selected = a), a && n && (t[l].defaultSelected = !0);
    } else {
      for (l = "" + Ee(l), e = null, a = 0; a < t.length; a++) {
        if (t[a].value === l) {
          t[a].selected = !0, n && (t[a].defaultSelected = !0);
          return;
        }
        e !== null || t[a].disabled || (e = t[a]);
      }
      e !== null && (e.selected = !0);
    }
  }
  function gs(t, e, l) {
    if (e != null && (e = "" + Ee(e), e !== t.value && (t.value = e), l == null)) {
      t.defaultValue !== e && (t.defaultValue = e);
      return;
    }
    t.defaultValue = l != null ? "" + Ee(l) : "";
  }
  function bs(t, e, l, n) {
    if (e == null) {
      if (n != null) {
        if (l != null) throw Error(f(92));
        if (ft(n)) {
          if (1 < n.length) throw Error(f(93));
          n = n[0];
        }
        l = n;
      }
      l == null && (l = ""), e = l;
    }
    l = Ee(e), t.defaultValue = l, n = t.textContent, n === l && n !== "" && n !== null && (t.value = n), oc(t);
  }
  function En(t, e) {
    if (e) {
      var l = t.firstChild;
      if (l && l === t.lastChild && l.nodeType === 3) {
        l.nodeValue = e;
        return;
      }
    }
    t.textContent = e;
  }
  var $v = new Set(
    "animationIterationCount aspectRatio borderImageOutset borderImageSlice borderImageWidth boxFlex boxFlexGroup boxOrdinalGroup columnCount columns flex flexGrow flexPositive flexShrink flexNegative flexOrder gridArea gridRow gridRowEnd gridRowSpan gridRowStart gridColumn gridColumnEnd gridColumnSpan gridColumnStart fontWeight lineClamp lineHeight opacity order orphans scale tabSize widows zIndex zoom fillOpacity floodOpacity stopOpacity strokeDasharray strokeDashoffset strokeMiterlimit strokeOpacity strokeWidth MozAnimationIterationCount MozBoxFlex MozBoxFlexGroup MozLineClamp msAnimationIterationCount msFlex msZoom msFlexGrow msFlexNegative msFlexOrder msFlexPositive msFlexShrink msGridColumn msGridColumnSpan msGridRow msGridRowSpan WebkitAnimationIterationCount WebkitBoxFlex WebKitBoxFlexGroup WebkitBoxOrdinalGroup WebkitColumnCount WebkitColumns WebkitFlex WebkitFlexGrow WebkitFlexPositive WebkitFlexShrink WebkitLineClamp".split(
      " "
    )
  );
  function Ss(t, e, l) {
    var n = e.indexOf("--") === 0;
    l == null || typeof l == "boolean" || l === "" ? n ? t.setProperty(e, "") : e === "float" ? t.cssFloat = "" : t[e] = "" : n ? t.setProperty(e, l) : typeof l != "number" || l === 0 || $v.has(e) ? e === "float" ? t.cssFloat = l : t[e] = ("" + l).trim() : t[e] = l + "px";
  }
  function ps(t, e, l) {
    if (e != null && typeof e != "object")
      throw Error(f(62));
    if (t = t.style, l != null) {
      for (var n in l)
        !l.hasOwnProperty(n) || e != null && e.hasOwnProperty(n) || (n.indexOf("--") === 0 ? t.setProperty(n, "") : n === "float" ? t.cssFloat = "" : t[n] = "");
      for (var a in e)
        n = e[a], e.hasOwnProperty(a) && l[a] !== n && Ss(t, a, n);
    } else
      for (var u in e)
        e.hasOwnProperty(u) && Ss(t, u, e[u]);
  }
  function dc(t) {
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
  var Iv = /* @__PURE__ */ new Map([
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
  ]), Pv = /^[\u0000-\u001F ]*j[\r\n\t]*a[\r\n\t]*v[\r\n\t]*a[\r\n\t]*s[\r\n\t]*c[\r\n\t]*r[\r\n\t]*i[\r\n\t]*p[\r\n\t]*t[\r\n\t]*:/i;
  function bu(t) {
    return Pv.test("" + t) ? "javascript:throw new Error('React has blocked a javascript: URL as a security precaution.')" : t;
  }
  function Ve() {
  }
  var mc = null;
  function hc(t) {
    return t = t.target || t.srcElement || window, t.correspondingUseElement && (t = t.correspondingUseElement), t.nodeType === 3 ? t.parentNode : t;
  }
  var Tn = null, An = null;
  function Es(t) {
    var e = gn(t);
    if (e && (t = e.stateNode)) {
      var l = t[le] || null;
      t: switch (t = e.stateNode, e.type) {
        case "input":
          if (sc(
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
              'input[name="' + Te(
                "" + e
              ) + '"][type="radio"]'
            ), e = 0; e < l.length; e++) {
              var n = l[e];
              if (n !== t && n.form === t.form) {
                var a = n[le] || null;
                if (!a) throw Error(f(90));
                sc(
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
              n = l[e], n.form === t.form && vs(n);
          }
          break t;
        case "textarea":
          gs(t, l.value, l.defaultValue);
          break t;
        case "select":
          e = l.value, e != null && pn(t, !!l.multiple, e, !1);
      }
    }
  }
  var vc = !1;
  function Ts(t, e, l) {
    if (vc) return t(e, l);
    vc = !0;
    try {
      var n = t(e);
      return n;
    } finally {
      if (vc = !1, (Tn !== null || An !== null) && (ui(), Tn && (e = Tn, t = An, An = Tn = null, Es(e), t)))
        for (e = 0; e < t.length; e++) Es(t[e]);
    }
  }
  function oa(t, e) {
    var l = t.stateNode;
    if (l === null) return null;
    var n = l[le] || null;
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
        f(231, e, typeof l)
      );
    return l;
  }
  var Ze = !(typeof window > "u" || typeof window.document > "u" || typeof window.document.createElement > "u"), yc = !1;
  if (Ze)
    try {
      var sa = {};
      Object.defineProperty(sa, "passive", {
        get: function() {
          yc = !0;
        }
      }), window.addEventListener("test", sa, sa), window.removeEventListener("test", sa, sa);
    } catch {
      yc = !1;
    }
  var dl = null, gc = null, Su = null;
  function As() {
    if (Su) return Su;
    var t, e = gc, l = e.length, n, a = "value" in dl ? dl.value : dl.textContent, u = a.length;
    for (t = 0; t < l && e[t] === a[t]; t++) ;
    var s = l - t;
    for (n = 1; n <= s && e[l - n] === a[u - n]; n++) ;
    return Su = a.slice(t, 1 < n ? 1 - n : void 0);
  }
  function pu(t) {
    var e = t.keyCode;
    return "charCode" in t ? (t = t.charCode, t === 0 && e === 13 && (t = 13)) : t = e, t === 10 && (t = 13), 32 <= t || t === 13 ? t : 0;
  }
  function Eu() {
    return !0;
  }
  function Rs() {
    return !1;
  }
  function ne(t) {
    function e(l, n, a, u, s) {
      this._reactName = l, this._targetInst = a, this.type = n, this.nativeEvent = u, this.target = s, this.currentTarget = null;
      for (var d in t)
        t.hasOwnProperty(d) && (l = t[d], this[d] = l ? l(u) : u[d]);
      return this.isDefaultPrevented = (u.defaultPrevented != null ? u.defaultPrevented : u.returnValue === !1) ? Eu : Rs, this.isPropagationStopped = Rs, this;
    }
    return z(e.prototype, {
      preventDefault: function() {
        this.defaultPrevented = !0;
        var l = this.nativeEvent;
        l && (l.preventDefault ? l.preventDefault() : typeof l.returnValue != "unknown" && (l.returnValue = !1), this.isDefaultPrevented = Eu);
      },
      stopPropagation: function() {
        var l = this.nativeEvent;
        l && (l.stopPropagation ? l.stopPropagation() : typeof l.cancelBubble != "unknown" && (l.cancelBubble = !0), this.isPropagationStopped = Eu);
      },
      persist: function() {
      },
      isPersistent: Eu
    }), e;
  }
  var Ql = {
    eventPhase: 0,
    bubbles: 0,
    cancelable: 0,
    timeStamp: function(t) {
      return t.timeStamp || Date.now();
    },
    defaultPrevented: 0,
    isTrusted: 0
  }, Tu = ne(Ql), ra = z({}, Ql, { view: 0, detail: 0 }), ty = ne(ra), bc, Sc, da, Au = z({}, ra, {
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
    getModifierState: Ec,
    button: 0,
    buttons: 0,
    relatedTarget: function(t) {
      return t.relatedTarget === void 0 ? t.fromElement === t.srcElement ? t.toElement : t.fromElement : t.relatedTarget;
    },
    movementX: function(t) {
      return "movementX" in t ? t.movementX : (t !== da && (da && t.type === "mousemove" ? (bc = t.screenX - da.screenX, Sc = t.screenY - da.screenY) : Sc = bc = 0, da = t), bc);
    },
    movementY: function(t) {
      return "movementY" in t ? t.movementY : Sc;
    }
  }), Os = ne(Au), ey = z({}, Au, { dataTransfer: 0 }), ly = ne(ey), ny = z({}, ra, { relatedTarget: 0 }), pc = ne(ny), ay = z({}, Ql, {
    animationName: 0,
    elapsedTime: 0,
    pseudoElement: 0
  }), uy = ne(ay), iy = z({}, Ql, {
    clipboardData: function(t) {
      return "clipboardData" in t ? t.clipboardData : window.clipboardData;
    }
  }), cy = ne(iy), fy = z({}, Ql, { data: 0 }), zs = ne(fy), oy = {
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
  }, sy = {
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
  }, ry = {
    Alt: "altKey",
    Control: "ctrlKey",
    Meta: "metaKey",
    Shift: "shiftKey"
  };
  function dy(t) {
    var e = this.nativeEvent;
    return e.getModifierState ? e.getModifierState(t) : (t = ry[t]) ? !!e[t] : !1;
  }
  function Ec() {
    return dy;
  }
  var my = z({}, ra, {
    key: function(t) {
      if (t.key) {
        var e = oy[t.key] || t.key;
        if (e !== "Unidentified") return e;
      }
      return t.type === "keypress" ? (t = pu(t), t === 13 ? "Enter" : String.fromCharCode(t)) : t.type === "keydown" || t.type === "keyup" ? sy[t.keyCode] || "Unidentified" : "";
    },
    code: 0,
    location: 0,
    ctrlKey: 0,
    shiftKey: 0,
    altKey: 0,
    metaKey: 0,
    repeat: 0,
    locale: 0,
    getModifierState: Ec,
    charCode: function(t) {
      return t.type === "keypress" ? pu(t) : 0;
    },
    keyCode: function(t) {
      return t.type === "keydown" || t.type === "keyup" ? t.keyCode : 0;
    },
    which: function(t) {
      return t.type === "keypress" ? pu(t) : t.type === "keydown" || t.type === "keyup" ? t.keyCode : 0;
    }
  }), hy = ne(my), vy = z({}, Au, {
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
  }), _s = ne(vy), yy = z({}, ra, {
    touches: 0,
    targetTouches: 0,
    changedTouches: 0,
    altKey: 0,
    metaKey: 0,
    ctrlKey: 0,
    shiftKey: 0,
    getModifierState: Ec
  }), gy = ne(yy), by = z({}, Ql, {
    propertyName: 0,
    elapsedTime: 0,
    pseudoElement: 0
  }), Sy = ne(by), py = z({}, Au, {
    deltaX: function(t) {
      return "deltaX" in t ? t.deltaX : "wheelDeltaX" in t ? -t.wheelDeltaX : 0;
    },
    deltaY: function(t) {
      return "deltaY" in t ? t.deltaY : "wheelDeltaY" in t ? -t.wheelDeltaY : "wheelDelta" in t ? -t.wheelDelta : 0;
    },
    deltaZ: 0,
    deltaMode: 0
  }), Ey = ne(py), Ty = z({}, Ql, {
    newState: 0,
    oldState: 0
  }), Ay = ne(Ty), Ry = [9, 13, 27, 32], Tc = Ze && "CompositionEvent" in window, ma = null;
  Ze && "documentMode" in document && (ma = document.documentMode);
  var Oy = Ze && "TextEvent" in window && !ma, Ms = Ze && (!Tc || ma && 8 < ma && 11 >= ma), Cs = " ", xs = !1;
  function Ds(t, e) {
    switch (t) {
      case "keyup":
        return Ry.indexOf(e.keyCode) !== -1;
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
  function Ns(t) {
    return t = t.detail, typeof t == "object" && "data" in t ? t.data : null;
  }
  var Rn = !1;
  function zy(t, e) {
    switch (t) {
      case "compositionend":
        return Ns(e);
      case "keypress":
        return e.which !== 32 ? null : (xs = !0, Cs);
      case "textInput":
        return t = e.data, t === Cs && xs ? null : t;
      default:
        return null;
    }
  }
  function _y(t, e) {
    if (Rn)
      return t === "compositionend" || !Tc && Ds(t, e) ? (t = As(), Su = gc = dl = null, Rn = !1, t) : null;
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
        return Ms && e.locale !== "ko" ? null : e.data;
      default:
        return null;
    }
  }
  var My = {
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
  function ws(t) {
    var e = t && t.nodeName && t.nodeName.toLowerCase();
    return e === "input" ? !!My[t.type] : e === "textarea";
  }
  function Us(t, e, l, n) {
    Tn ? An ? An.push(n) : An = [n] : Tn = n, e = di(e, "onChange"), 0 < e.length && (l = new Tu(
      "onChange",
      "change",
      null,
      l,
      n
    ), t.push({ event: l, listeners: e }));
  }
  var ha = null, va = null;
  function Cy(t) {
    ym(t, 0);
  }
  function Ru(t) {
    var e = fa(t);
    if (vs(e)) return t;
  }
  function Hs(t, e) {
    if (t === "change") return e;
  }
  var Bs = !1;
  if (Ze) {
    var Ac;
    if (Ze) {
      var Rc = "oninput" in document;
      if (!Rc) {
        var Ls = document.createElement("div");
        Ls.setAttribute("oninput", "return;"), Rc = typeof Ls.oninput == "function";
      }
      Ac = Rc;
    } else Ac = !1;
    Bs = Ac && (!document.documentMode || 9 < document.documentMode);
  }
  function qs() {
    ha && (ha.detachEvent("onpropertychange", Ys), va = ha = null);
  }
  function Ys(t) {
    if (t.propertyName === "value" && Ru(va)) {
      var e = [];
      Us(
        e,
        va,
        t,
        hc(t)
      ), Ts(Cy, e);
    }
  }
  function xy(t, e, l) {
    t === "focusin" ? (qs(), ha = e, va = l, ha.attachEvent("onpropertychange", Ys)) : t === "focusout" && qs();
  }
  function Dy(t) {
    if (t === "selectionchange" || t === "keyup" || t === "keydown")
      return Ru(va);
  }
  function Ny(t, e) {
    if (t === "click") return Ru(e);
  }
  function wy(t, e) {
    if (t === "input" || t === "change")
      return Ru(e);
  }
  function Uy(t, e) {
    return t === e && (t !== 0 || 1 / t === 1 / e) || t !== t && e !== e;
  }
  var de = typeof Object.is == "function" ? Object.is : Uy;
  function ya(t, e) {
    if (de(t, e)) return !0;
    if (typeof t != "object" || t === null || typeof e != "object" || e === null)
      return !1;
    var l = Object.keys(t), n = Object.keys(e);
    if (l.length !== n.length) return !1;
    for (n = 0; n < l.length; n++) {
      var a = l[n];
      if (!ec.call(e, a) || !de(t[a], e[a]))
        return !1;
    }
    return !0;
  }
  function js(t) {
    for (; t && t.firstChild; ) t = t.firstChild;
    return t;
  }
  function Xs(t, e) {
    var l = js(t);
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
      l = js(l);
    }
  }
  function Gs(t, e) {
    return t && e ? t === e ? !0 : t && t.nodeType === 3 ? !1 : e && e.nodeType === 3 ? Gs(t, e.parentNode) : "contains" in t ? t.contains(e) : t.compareDocumentPosition ? !!(t.compareDocumentPosition(e) & 16) : !1 : !1;
  }
  function Qs(t) {
    t = t != null && t.ownerDocument != null && t.ownerDocument.defaultView != null ? t.ownerDocument.defaultView : window;
    for (var e = gu(t.document); e instanceof t.HTMLIFrameElement; ) {
      try {
        var l = typeof e.contentWindow.location.href == "string";
      } catch {
        l = !1;
      }
      if (l) t = e.contentWindow;
      else break;
      e = gu(t.document);
    }
    return e;
  }
  function Oc(t) {
    var e = t && t.nodeName && t.nodeName.toLowerCase();
    return e && (e === "input" && (t.type === "text" || t.type === "search" || t.type === "tel" || t.type === "url" || t.type === "password") || e === "textarea" || t.contentEditable === "true");
  }
  var Hy = Ze && "documentMode" in document && 11 >= document.documentMode, On = null, zc = null, ga = null, _c = !1;
  function Vs(t, e, l) {
    var n = l.window === l ? l.document : l.nodeType === 9 ? l : l.ownerDocument;
    _c || On == null || On !== gu(n) || (n = On, "selectionStart" in n && Oc(n) ? n = { start: n.selectionStart, end: n.selectionEnd } : (n = (n.ownerDocument && n.ownerDocument.defaultView || window).getSelection(), n = {
      anchorNode: n.anchorNode,
      anchorOffset: n.anchorOffset,
      focusNode: n.focusNode,
      focusOffset: n.focusOffset
    }), ga && ya(ga, n) || (ga = n, n = di(zc, "onSelect"), 0 < n.length && (e = new Tu(
      "onSelect",
      "select",
      null,
      e,
      l
    ), t.push({ event: e, listeners: n }), e.target = On)));
  }
  function Vl(t, e) {
    var l = {};
    return l[t.toLowerCase()] = e.toLowerCase(), l["Webkit" + t] = "webkit" + e, l["Moz" + t] = "moz" + e, l;
  }
  var zn = {
    animationend: Vl("Animation", "AnimationEnd"),
    animationiteration: Vl("Animation", "AnimationIteration"),
    animationstart: Vl("Animation", "AnimationStart"),
    transitionrun: Vl("Transition", "TransitionRun"),
    transitionstart: Vl("Transition", "TransitionStart"),
    transitioncancel: Vl("Transition", "TransitionCancel"),
    transitionend: Vl("Transition", "TransitionEnd")
  }, Mc = {}, Zs = {};
  Ze && (Zs = document.createElement("div").style, "AnimationEvent" in window || (delete zn.animationend.animation, delete zn.animationiteration.animation, delete zn.animationstart.animation), "TransitionEvent" in window || delete zn.transitionend.transition);
  function Zl(t) {
    if (Mc[t]) return Mc[t];
    if (!zn[t]) return t;
    var e = zn[t], l;
    for (l in e)
      if (e.hasOwnProperty(l) && l in Zs)
        return Mc[t] = e[l];
    return t;
  }
  var Ks = Zl("animationend"), Js = Zl("animationiteration"), Ws = Zl("animationstart"), By = Zl("transitionrun"), Ly = Zl("transitionstart"), qy = Zl("transitioncancel"), ks = Zl("transitionend"), Fs = /* @__PURE__ */ new Map(), Cc = "abort auxClick beforeToggle cancel canPlay canPlayThrough click close contextMenu copy cut drag dragEnd dragEnter dragExit dragLeave dragOver dragStart drop durationChange emptied encrypted ended error gotPointerCapture input invalid keyDown keyPress keyUp load loadedData loadedMetadata loadStart lostPointerCapture mouseDown mouseMove mouseOut mouseOver mouseUp paste pause play playing pointerCancel pointerDown pointerMove pointerOut pointerOver pointerUp progress rateChange reset resize seeked seeking stalled submit suspend timeUpdate touchCancel touchEnd touchStart volumeChange scroll toggle touchMove waiting wheel".split(
    " "
  );
  Cc.push("scrollEnd");
  function De(t, e) {
    Fs.set(t, e), Gl(e, [t]);
  }
  var Ou = typeof reportError == "function" ? reportError : function(t) {
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
  }, Ae = [], _n = 0, xc = 0;
  function zu() {
    for (var t = _n, e = xc = _n = 0; e < t; ) {
      var l = Ae[e];
      Ae[e++] = null;
      var n = Ae[e];
      Ae[e++] = null;
      var a = Ae[e];
      Ae[e++] = null;
      var u = Ae[e];
      if (Ae[e++] = null, n !== null && a !== null) {
        var s = n.pending;
        s === null ? a.next = a : (a.next = s.next, s.next = a), n.pending = a;
      }
      u !== 0 && $s(l, a, u);
    }
  }
  function _u(t, e, l, n) {
    Ae[_n++] = t, Ae[_n++] = e, Ae[_n++] = l, Ae[_n++] = n, xc |= n, t.lanes |= n, t = t.alternate, t !== null && (t.lanes |= n);
  }
  function Dc(t, e, l, n) {
    return _u(t, e, l, n), Mu(t);
  }
  function Kl(t, e) {
    return _u(t, null, null, e), Mu(t);
  }
  function $s(t, e, l) {
    t.lanes |= l;
    var n = t.alternate;
    n !== null && (n.lanes |= l);
    for (var a = !1, u = t.return; u !== null; )
      u.childLanes |= l, n = u.alternate, n !== null && (n.childLanes |= l), u.tag === 22 && (t = u.stateNode, t === null || t._visibility & 1 || (a = !0)), t = u, u = u.return;
    return t.tag === 3 ? (u = t.stateNode, a && e !== null && (a = 31 - re(l), t = u.hiddenUpdates, n = t[a], n === null ? t[a] = [e] : n.push(e), e.lane = l | 536870912), u) : null;
  }
  function Mu(t) {
    if (50 < Ya)
      throw Ya = 0, Xf = null, Error(f(185));
    for (var e = t.return; e !== null; )
      t = e, e = t.return;
    return t.tag === 3 ? t.stateNode : null;
  }
  var Mn = {};
  function Yy(t, e, l, n) {
    this.tag = t, this.key = l, this.sibling = this.child = this.return = this.stateNode = this.type = this.elementType = null, this.index = 0, this.refCleanup = this.ref = null, this.pendingProps = e, this.dependencies = this.memoizedState = this.updateQueue = this.memoizedProps = null, this.mode = n, this.subtreeFlags = this.flags = 0, this.deletions = null, this.childLanes = this.lanes = 0, this.alternate = null;
  }
  function me(t, e, l, n) {
    return new Yy(t, e, l, n);
  }
  function Nc(t) {
    return t = t.prototype, !(!t || !t.isReactComponent);
  }
  function Ke(t, e) {
    var l = t.alternate;
    return l === null ? (l = me(
      t.tag,
      e,
      t.key,
      t.mode
    ), l.elementType = t.elementType, l.type = t.type, l.stateNode = t.stateNode, l.alternate = t, t.alternate = l) : (l.pendingProps = e, l.type = t.type, l.flags = 0, l.subtreeFlags = 0, l.deletions = null), l.flags = t.flags & 65011712, l.childLanes = t.childLanes, l.lanes = t.lanes, l.child = t.child, l.memoizedProps = t.memoizedProps, l.memoizedState = t.memoizedState, l.updateQueue = t.updateQueue, e = t.dependencies, l.dependencies = e === null ? null : { lanes: e.lanes, firstContext: e.firstContext }, l.sibling = t.sibling, l.index = t.index, l.ref = t.ref, l.refCleanup = t.refCleanup, l;
  }
  function Is(t, e) {
    t.flags &= 65011714;
    var l = t.alternate;
    return l === null ? (t.childLanes = 0, t.lanes = e, t.child = null, t.subtreeFlags = 0, t.memoizedProps = null, t.memoizedState = null, t.updateQueue = null, t.dependencies = null, t.stateNode = null) : (t.childLanes = l.childLanes, t.lanes = l.lanes, t.child = l.child, t.subtreeFlags = 0, t.deletions = null, t.memoizedProps = l.memoizedProps, t.memoizedState = l.memoizedState, t.updateQueue = l.updateQueue, t.type = l.type, e = l.dependencies, t.dependencies = e === null ? null : {
      lanes: e.lanes,
      firstContext: e.firstContext
    }), t;
  }
  function Cu(t, e, l, n, a, u) {
    var s = 0;
    if (n = t, typeof t == "function") Nc(t) && (s = 1);
    else if (typeof t == "string")
      s = V0(
        t,
        l,
        K.current
      ) ? 26 : t === "html" || t === "head" || t === "body" ? 27 : 5;
    else
      t: switch (t) {
        case zt:
          return t = me(31, l, e, a), t.elementType = zt, t.lanes = u, t;
        case q:
          return Jl(l.children, a, u, e);
        case V:
          s = 8, a |= 24;
          break;
        case X:
          return t = me(12, l, e, a | 2), t.elementType = X, t.lanes = u, t;
        case at:
          return t = me(13, l, e, a), t.elementType = at, t.lanes = u, t;
        case lt:
          return t = me(19, l, e, a), t.elementType = lt, t.lanes = u, t;
        default:
          if (typeof t == "object" && t !== null)
            switch (t.$$typeof) {
              case G:
                s = 10;
                break t;
              case W:
                s = 9;
                break t;
              case $:
                s = 11;
                break t;
              case Z:
                s = 14;
                break t;
              case it:
                s = 16, n = null;
                break t;
            }
          s = 29, l = Error(
            f(130, t === null ? "null" : typeof t, "")
          ), n = null;
      }
    return e = me(s, l, e, a), e.elementType = t, e.type = n, e.lanes = u, e;
  }
  function Jl(t, e, l, n) {
    return t = me(7, t, n, e), t.lanes = l, t;
  }
  function wc(t, e, l) {
    return t = me(6, t, null, e), t.lanes = l, t;
  }
  function Ps(t) {
    var e = me(18, null, null, 0);
    return e.stateNode = t, e;
  }
  function Uc(t, e, l) {
    return e = me(
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
  var tr = /* @__PURE__ */ new WeakMap();
  function Re(t, e) {
    if (typeof t == "object" && t !== null) {
      var l = tr.get(t);
      return l !== void 0 ? l : (e = {
        value: t,
        source: e,
        stack: Po(e)
      }, tr.set(t, e), e);
    }
    return {
      value: t,
      source: e,
      stack: Po(e)
    };
  }
  var Cn = [], xn = 0, xu = null, ba = 0, Oe = [], ze = 0, ml = null, Ue = 1, He = "";
  function Je(t, e) {
    Cn[xn++] = ba, Cn[xn++] = xu, xu = t, ba = e;
  }
  function er(t, e, l) {
    Oe[ze++] = Ue, Oe[ze++] = He, Oe[ze++] = ml, ml = t;
    var n = Ue;
    t = He;
    var a = 32 - re(n) - 1;
    n &= ~(1 << a), l += 1;
    var u = 32 - re(e) + a;
    if (30 < u) {
      var s = a - a % 5;
      u = (n & (1 << s) - 1).toString(32), n >>= s, a -= s, Ue = 1 << 32 - re(e) + a | l << a | n, He = u + t;
    } else
      Ue = 1 << u | l << a | n, He = t;
  }
  function Hc(t) {
    t.return !== null && (Je(t, 1), er(t, 1, 0));
  }
  function Bc(t) {
    for (; t === xu; )
      xu = Cn[--xn], Cn[xn] = null, ba = Cn[--xn], Cn[xn] = null;
    for (; t === ml; )
      ml = Oe[--ze], Oe[ze] = null, He = Oe[--ze], Oe[ze] = null, Ue = Oe[--ze], Oe[ze] = null;
  }
  function lr(t, e) {
    Oe[ze++] = Ue, Oe[ze++] = He, Oe[ze++] = ml, Ue = e.id, He = e.overflow, ml = t;
  }
  var kt = null, xt = null, yt = !1, hl = null, _e = !1, Lc = Error(f(519));
  function vl(t) {
    var e = Error(
      f(
        418,
        1 < arguments.length && arguments[1] !== void 0 && arguments[1] ? "text" : "HTML",
        ""
      )
    );
    throw Sa(Re(e, t)), Lc;
  }
  function nr(t) {
    var e = t.stateNode, l = t.type, n = t.memoizedProps;
    switch (e[Wt] = t, e[le] = n, l) {
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
        for (l = 0; l < Xa.length; l++)
          rt(Xa[l], e);
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
        rt("invalid", e), ys(
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
        rt("invalid", e), bs(e, n.value, n.defaultValue, n.children);
    }
    l = n.children, typeof l != "string" && typeof l != "number" && typeof l != "bigint" || e.textContent === "" + l || n.suppressHydrationWarning === !0 || pm(e.textContent, l) ? (n.popover != null && (rt("beforetoggle", e), rt("toggle", e)), n.onScroll != null && rt("scroll", e), n.onScrollEnd != null && rt("scrollend", e), n.onClick != null && (e.onclick = Ve), e = !0) : e = !1, e || vl(t, !0);
  }
  function ar(t) {
    for (kt = t.return; kt; )
      switch (kt.tag) {
        case 5:
        case 31:
        case 13:
          _e = !1;
          return;
        case 27:
        case 3:
          _e = !0;
          return;
        default:
          kt = kt.return;
      }
  }
  function Dn(t) {
    if (t !== kt) return !1;
    if (!yt) return ar(t), yt = !0, !1;
    var e = t.tag, l;
    if ((l = e !== 3 && e !== 27) && ((l = e === 5) && (l = t.type, l = !(l !== "form" && l !== "button") || lo(t.type, t.memoizedProps)), l = !l), l && xt && vl(t), ar(t), e === 13) {
      if (t = t.memoizedState, t = t !== null ? t.dehydrated : null, !t) throw Error(f(317));
      xt = Cm(t);
    } else if (e === 31) {
      if (t = t.memoizedState, t = t !== null ? t.dehydrated : null, !t) throw Error(f(317));
      xt = Cm(t);
    } else
      e === 27 ? (e = xt, Cl(t.type) ? (t = co, co = null, xt = t) : xt = e) : xt = kt ? Ce(t.stateNode.nextSibling) : null;
    return !0;
  }
  function Wl() {
    xt = kt = null, yt = !1;
  }
  function qc() {
    var t = hl;
    return t !== null && (ce === null ? ce = t : ce.push.apply(
      ce,
      t
    ), hl = null), t;
  }
  function Sa(t) {
    hl === null ? hl = [t] : hl.push(t);
  }
  var Yc = g(null), kl = null, We = null;
  function yl(t, e, l) {
    j(Yc, e._currentValue), e._currentValue = l;
  }
  function ke(t) {
    t._currentValue = Yc.current, U(Yc);
  }
  function jc(t, e, l) {
    for (; t !== null; ) {
      var n = t.alternate;
      if ((t.childLanes & e) !== e ? (t.childLanes |= e, n !== null && (n.childLanes |= e)) : n !== null && (n.childLanes & e) !== e && (n.childLanes |= e), t === l) break;
      t = t.return;
    }
  }
  function Xc(t, e, l, n) {
    var a = t.child;
    for (a !== null && (a.return = t); a !== null; ) {
      var u = a.dependencies;
      if (u !== null) {
        var s = a.child;
        u = u.firstContext;
        t: for (; u !== null; ) {
          var d = u;
          u = a;
          for (var y = 0; y < e.length; y++)
            if (d.context === e[y]) {
              u.lanes |= l, d = u.alternate, d !== null && (d.lanes |= l), jc(
                u.return,
                l,
                t
              ), n || (s = null);
              break t;
            }
          u = d.next;
        }
      } else if (a.tag === 18) {
        if (s = a.return, s === null) throw Error(f(341));
        s.lanes |= l, u = s.alternate, u !== null && (u.lanes |= l), jc(s, l, t), s = null;
      } else s = a.child;
      if (s !== null) s.return = a;
      else
        for (s = a; s !== null; ) {
          if (s === t) {
            s = null;
            break;
          }
          if (a = s.sibling, a !== null) {
            a.return = s.return, s = a;
            break;
          }
          s = s.return;
        }
      a = s;
    }
  }
  function Nn(t, e, l, n) {
    t = null;
    for (var a = e, u = !1; a !== null; ) {
      if (!u) {
        if ((a.flags & 524288) !== 0) u = !0;
        else if ((a.flags & 262144) !== 0) break;
      }
      if (a.tag === 10) {
        var s = a.alternate;
        if (s === null) throw Error(f(387));
        if (s = s.memoizedProps, s !== null) {
          var d = a.type;
          de(a.pendingProps.value, s.value) || (t !== null ? t.push(d) : t = [d]);
        }
      } else if (a === mt.current) {
        if (s = a.alternate, s === null) throw Error(f(387));
        s.memoizedState.memoizedState !== a.memoizedState.memoizedState && (t !== null ? t.push(Ka) : t = [Ka]);
      }
      a = a.return;
    }
    t !== null && Xc(
      e,
      t,
      l,
      n
    ), e.flags |= 262144;
  }
  function Du(t) {
    for (t = t.firstContext; t !== null; ) {
      if (!de(
        t.context._currentValue,
        t.memoizedValue
      ))
        return !0;
      t = t.next;
    }
    return !1;
  }
  function Fl(t) {
    kl = t, We = null, t = t.dependencies, t !== null && (t.firstContext = null);
  }
  function Ft(t) {
    return ur(kl, t);
  }
  function Nu(t, e) {
    return kl === null && Fl(t), ur(t, e);
  }
  function ur(t, e) {
    var l = e._currentValue;
    if (e = { context: e, memoizedValue: l, next: null }, We === null) {
      if (t === null) throw Error(f(308));
      We = e, t.dependencies = { lanes: 0, firstContext: e }, t.flags |= 524288;
    } else We = We.next = e;
    return l;
  }
  var jy = typeof AbortController < "u" ? AbortController : function() {
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
  }, Xy = i.unstable_scheduleCallback, Gy = i.unstable_NormalPriority, Yt = {
    $$typeof: G,
    Consumer: null,
    Provider: null,
    _currentValue: null,
    _currentValue2: null,
    _threadCount: 0
  };
  function Gc() {
    return {
      controller: new jy(),
      data: /* @__PURE__ */ new Map(),
      refCount: 0
    };
  }
  function pa(t) {
    t.refCount--, t.refCount === 0 && Xy(Gy, function() {
      t.controller.abort();
    });
  }
  var Ea = null, Qc = 0, wn = 0, Un = null;
  function Qy(t, e) {
    if (Ea === null) {
      var l = Ea = [];
      Qc = 0, wn = Jf(), Un = {
        status: "pending",
        value: void 0,
        then: function(n) {
          l.push(n);
        }
      };
    }
    return Qc++, e.then(ir, ir), e;
  }
  function ir() {
    if (--Qc === 0 && Ea !== null) {
      Un !== null && (Un.status = "fulfilled");
      var t = Ea;
      Ea = null, wn = 0, Un = null;
      for (var e = 0; e < t.length; e++) (0, t[e])();
    }
  }
  function Vy(t, e) {
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
  var cr = R.S;
  R.S = function(t, e) {
    Vd = oe(), typeof e == "object" && e !== null && typeof e.then == "function" && Qy(t, e), cr !== null && cr(t, e);
  };
  var $l = g(null);
  function Vc() {
    var t = $l.current;
    return t !== null ? t : Mt.pooledCache;
  }
  function wu(t, e) {
    e === null ? j($l, $l.current) : j($l, e.pool);
  }
  function fr() {
    var t = Vc();
    return t === null ? null : { parent: Yt._currentValue, pool: t };
  }
  var Hn = Error(f(460)), Zc = Error(f(474)), Uu = Error(f(542)), Hu = { then: function() {
  } };
  function or(t) {
    return t = t.status, t === "fulfilled" || t === "rejected";
  }
  function sr(t, e, l) {
    switch (l = t[l], l === void 0 ? t.push(e) : l !== e && (e.then(Ve, Ve), e = l), e.status) {
      case "fulfilled":
        return e.value;
      case "rejected":
        throw t = e.reason, dr(t), t;
      default:
        if (typeof e.status == "string") e.then(Ve, Ve);
        else {
          if (t = Mt, t !== null && 100 < t.shellSuspendCounter)
            throw Error(f(482));
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
            throw t = e.reason, dr(t), t;
        }
        throw Pl = e, Hn;
    }
  }
  function Il(t) {
    try {
      var e = t._init;
      return e(t._payload);
    } catch (l) {
      throw l !== null && typeof l == "object" && typeof l.then == "function" ? (Pl = l, Hn) : l;
    }
  }
  var Pl = null;
  function rr() {
    if (Pl === null) throw Error(f(459));
    var t = Pl;
    return Pl = null, t;
  }
  function dr(t) {
    if (t === Hn || t === Uu)
      throw Error(f(483));
  }
  var Bn = null, Ta = 0;
  function Bu(t) {
    var e = Ta;
    return Ta += 1, Bn === null && (Bn = []), sr(Bn, t, e);
  }
  function Aa(t, e) {
    e = e.props.ref, t.ref = e !== void 0 ? e : null;
  }
  function Lu(t, e) {
    throw e.$$typeof === N ? Error(f(525)) : (t = Object.prototype.toString.call(e), Error(
      f(
        31,
        t === "[object Object]" ? "object with keys {" + Object.keys(e).join(", ") + "}" : t
      )
    ));
  }
  function mr(t) {
    function e(E, S) {
      if (t) {
        var T = E.deletions;
        T === null ? (E.deletions = [S], E.flags |= 16) : T.push(S);
      }
    }
    function l(E, S) {
      if (!t) return null;
      for (; S !== null; )
        e(E, S), S = S.sibling;
      return null;
    }
    function n(E) {
      for (var S = /* @__PURE__ */ new Map(); E !== null; )
        E.key !== null ? S.set(E.key, E) : S.set(E.index, E), E = E.sibling;
      return S;
    }
    function a(E, S) {
      return E = Ke(E, S), E.index = 0, E.sibling = null, E;
    }
    function u(E, S, T) {
      return E.index = T, t ? (T = E.alternate, T !== null ? (T = T.index, T < S ? (E.flags |= 67108866, S) : T) : (E.flags |= 67108866, S)) : (E.flags |= 1048576, S);
    }
    function s(E) {
      return t && E.alternate === null && (E.flags |= 67108866), E;
    }
    function d(E, S, T, x) {
      return S === null || S.tag !== 6 ? (S = wc(T, E.mode, x), S.return = E, S) : (S = a(S, T), S.return = E, S);
    }
    function y(E, S, T, x) {
      var P = T.type;
      return P === q ? C(
        E,
        S,
        T.props.children,
        x,
        T.key
      ) : S !== null && (S.elementType === P || typeof P == "object" && P !== null && P.$$typeof === it && Il(P) === S.type) ? (S = a(S, T.props), Aa(S, T), S.return = E, S) : (S = Cu(
        T.type,
        T.key,
        T.props,
        null,
        E.mode,
        x
      ), Aa(S, T), S.return = E, S);
    }
    function A(E, S, T, x) {
      return S === null || S.tag !== 4 || S.stateNode.containerInfo !== T.containerInfo || S.stateNode.implementation !== T.implementation ? (S = Uc(T, E.mode, x), S.return = E, S) : (S = a(S, T.children || []), S.return = E, S);
    }
    function C(E, S, T, x, P) {
      return S === null || S.tag !== 7 ? (S = Jl(
        T,
        E.mode,
        x,
        P
      ), S.return = E, S) : (S = a(S, T), S.return = E, S);
    }
    function w(E, S, T) {
      if (typeof S == "string" && S !== "" || typeof S == "number" || typeof S == "bigint")
        return S = wc(
          "" + S,
          E.mode,
          T
        ), S.return = E, S;
      if (typeof S == "object" && S !== null) {
        switch (S.$$typeof) {
          case D:
            return T = Cu(
              S.type,
              S.key,
              S.props,
              null,
              E.mode,
              T
            ), Aa(T, S), T.return = E, T;
          case Y:
            return S = Uc(
              S,
              E.mode,
              T
            ), S.return = E, S;
          case it:
            return S = Il(S), w(E, S, T);
        }
        if (ft(S) || dt(S))
          return S = Jl(
            S,
            E.mode,
            T,
            null
          ), S.return = E, S;
        if (typeof S.then == "function")
          return w(E, Bu(S), T);
        if (S.$$typeof === G)
          return w(
            E,
            Nu(E, S),
            T
          );
        Lu(E, S);
      }
      return null;
    }
    function O(E, S, T, x) {
      var P = S !== null ? S.key : null;
      if (typeof T == "string" && T !== "" || typeof T == "number" || typeof T == "bigint")
        return P !== null ? null : d(E, S, "" + T, x);
      if (typeof T == "object" && T !== null) {
        switch (T.$$typeof) {
          case D:
            return T.key === P ? y(E, S, T, x) : null;
          case Y:
            return T.key === P ? A(E, S, T, x) : null;
          case it:
            return T = Il(T), O(E, S, T, x);
        }
        if (ft(T) || dt(T))
          return P !== null ? null : C(E, S, T, x, null);
        if (typeof T.then == "function")
          return O(
            E,
            S,
            Bu(T),
            x
          );
        if (T.$$typeof === G)
          return O(
            E,
            S,
            Nu(E, T),
            x
          );
        Lu(E, T);
      }
      return null;
    }
    function _(E, S, T, x, P) {
      if (typeof x == "string" && x !== "" || typeof x == "number" || typeof x == "bigint")
        return E = E.get(T) || null, d(S, E, "" + x, P);
      if (typeof x == "object" && x !== null) {
        switch (x.$$typeof) {
          case D:
            return E = E.get(
              x.key === null ? T : x.key
            ) || null, y(S, E, x, P);
          case Y:
            return E = E.get(
              x.key === null ? T : x.key
            ) || null, A(S, E, x, P);
          case it:
            return x = Il(x), _(
              E,
              S,
              T,
              x,
              P
            );
        }
        if (ft(x) || dt(x))
          return E = E.get(T) || null, C(S, E, x, P, null);
        if (typeof x.then == "function")
          return _(
            E,
            S,
            T,
            Bu(x),
            P
          );
        if (x.$$typeof === G)
          return _(
            E,
            S,
            T,
            Nu(S, x),
            P
          );
        Lu(S, x);
      }
      return null;
    }
    function J(E, S, T, x) {
      for (var P = null, gt = null, k = S, ot = S = 0, vt = null; k !== null && ot < T.length; ot++) {
        k.index > ot ? (vt = k, k = null) : vt = k.sibling;
        var bt = O(
          E,
          k,
          T[ot],
          x
        );
        if (bt === null) {
          k === null && (k = vt);
          break;
        }
        t && k && bt.alternate === null && e(E, k), S = u(bt, S, ot), gt === null ? P = bt : gt.sibling = bt, gt = bt, k = vt;
      }
      if (ot === T.length)
        return l(E, k), yt && Je(E, ot), P;
      if (k === null) {
        for (; ot < T.length; ot++)
          k = w(E, T[ot], x), k !== null && (S = u(
            k,
            S,
            ot
          ), gt === null ? P = k : gt.sibling = k, gt = k);
        return yt && Je(E, ot), P;
      }
      for (k = n(k); ot < T.length; ot++)
        vt = _(
          k,
          E,
          ot,
          T[ot],
          x
        ), vt !== null && (t && vt.alternate !== null && k.delete(
          vt.key === null ? ot : vt.key
        ), S = u(
          vt,
          S,
          ot
        ), gt === null ? P = vt : gt.sibling = vt, gt = vt);
      return t && k.forEach(function(Ul) {
        return e(E, Ul);
      }), yt && Je(E, ot), P;
    }
    function tt(E, S, T, x) {
      if (T == null) throw Error(f(151));
      for (var P = null, gt = null, k = S, ot = S = 0, vt = null, bt = T.next(); k !== null && !bt.done; ot++, bt = T.next()) {
        k.index > ot ? (vt = k, k = null) : vt = k.sibling;
        var Ul = O(E, k, bt.value, x);
        if (Ul === null) {
          k === null && (k = vt);
          break;
        }
        t && k && Ul.alternate === null && e(E, k), S = u(Ul, S, ot), gt === null ? P = Ul : gt.sibling = Ul, gt = Ul, k = vt;
      }
      if (bt.done)
        return l(E, k), yt && Je(E, ot), P;
      if (k === null) {
        for (; !bt.done; ot++, bt = T.next())
          bt = w(E, bt.value, x), bt !== null && (S = u(bt, S, ot), gt === null ? P = bt : gt.sibling = bt, gt = bt);
        return yt && Je(E, ot), P;
      }
      for (k = n(k); !bt.done; ot++, bt = T.next())
        bt = _(k, E, ot, bt.value, x), bt !== null && (t && bt.alternate !== null && k.delete(bt.key === null ? ot : bt.key), S = u(bt, S, ot), gt === null ? P = bt : gt.sibling = bt, gt = bt);
      return t && k.forEach(function(eg) {
        return e(E, eg);
      }), yt && Je(E, ot), P;
    }
    function Ot(E, S, T, x) {
      if (typeof T == "object" && T !== null && T.type === q && T.key === null && (T = T.props.children), typeof T == "object" && T !== null) {
        switch (T.$$typeof) {
          case D:
            t: {
              for (var P = T.key; S !== null; ) {
                if (S.key === P) {
                  if (P = T.type, P === q) {
                    if (S.tag === 7) {
                      l(
                        E,
                        S.sibling
                      ), x = a(
                        S,
                        T.props.children
                      ), x.return = E, E = x;
                      break t;
                    }
                  } else if (S.elementType === P || typeof P == "object" && P !== null && P.$$typeof === it && Il(P) === S.type) {
                    l(
                      E,
                      S.sibling
                    ), x = a(S, T.props), Aa(x, T), x.return = E, E = x;
                    break t;
                  }
                  l(E, S);
                  break;
                } else e(E, S);
                S = S.sibling;
              }
              T.type === q ? (x = Jl(
                T.props.children,
                E.mode,
                x,
                T.key
              ), x.return = E, E = x) : (x = Cu(
                T.type,
                T.key,
                T.props,
                null,
                E.mode,
                x
              ), Aa(x, T), x.return = E, E = x);
            }
            return s(E);
          case Y:
            t: {
              for (P = T.key; S !== null; ) {
                if (S.key === P)
                  if (S.tag === 4 && S.stateNode.containerInfo === T.containerInfo && S.stateNode.implementation === T.implementation) {
                    l(
                      E,
                      S.sibling
                    ), x = a(S, T.children || []), x.return = E, E = x;
                    break t;
                  } else {
                    l(E, S);
                    break;
                  }
                else e(E, S);
                S = S.sibling;
              }
              x = Uc(T, E.mode, x), x.return = E, E = x;
            }
            return s(E);
          case it:
            return T = Il(T), Ot(
              E,
              S,
              T,
              x
            );
        }
        if (ft(T))
          return J(
            E,
            S,
            T,
            x
          );
        if (dt(T)) {
          if (P = dt(T), typeof P != "function") throw Error(f(150));
          return T = P.call(T), tt(
            E,
            S,
            T,
            x
          );
        }
        if (typeof T.then == "function")
          return Ot(
            E,
            S,
            Bu(T),
            x
          );
        if (T.$$typeof === G)
          return Ot(
            E,
            S,
            Nu(E, T),
            x
          );
        Lu(E, T);
      }
      return typeof T == "string" && T !== "" || typeof T == "number" || typeof T == "bigint" ? (T = "" + T, S !== null && S.tag === 6 ? (l(E, S.sibling), x = a(S, T), x.return = E, E = x) : (l(E, S), x = wc(T, E.mode, x), x.return = E, E = x), s(E)) : l(E, S);
    }
    return function(E, S, T, x) {
      try {
        Ta = 0;
        var P = Ot(
          E,
          S,
          T,
          x
        );
        return Bn = null, P;
      } catch (k) {
        if (k === Hn || k === Uu) throw k;
        var gt = me(29, k, null, E.mode);
        return gt.lanes = x, gt.return = E, gt;
      } finally {
      }
    };
  }
  var tn = mr(!0), hr = mr(!1), gl = !1;
  function Kc(t) {
    t.updateQueue = {
      baseState: t.memoizedState,
      firstBaseUpdate: null,
      lastBaseUpdate: null,
      shared: { pending: null, lanes: 0, hiddenCallbacks: null },
      callbacks: null
    };
  }
  function Jc(t, e) {
    t = t.updateQueue, e.updateQueue === t && (e.updateQueue = {
      baseState: t.baseState,
      firstBaseUpdate: t.firstBaseUpdate,
      lastBaseUpdate: t.lastBaseUpdate,
      shared: t.shared,
      callbacks: null
    });
  }
  function bl(t) {
    return { lane: t, tag: 0, payload: null, callback: null, next: null };
  }
  function Sl(t, e, l) {
    var n = t.updateQueue;
    if (n === null) return null;
    if (n = n.shared, (St & 2) !== 0) {
      var a = n.pending;
      return a === null ? e.next = e : (e.next = a.next, a.next = e), n.pending = e, e = Mu(t), $s(t, null, l), e;
    }
    return _u(t, n, e, l), Mu(t);
  }
  function Ra(t, e, l) {
    if (e = e.updateQueue, e !== null && (e = e.shared, (l & 4194048) !== 0)) {
      var n = e.lanes;
      n &= t.pendingLanes, l |= n, e.lanes = l, us(t, l);
    }
  }
  function Wc(t, e) {
    var l = t.updateQueue, n = t.alternate;
    if (n !== null && (n = n.updateQueue, l === n)) {
      var a = null, u = null;
      if (l = l.firstBaseUpdate, l !== null) {
        do {
          var s = {
            lane: l.lane,
            tag: l.tag,
            payload: l.payload,
            callback: null,
            next: null
          };
          u === null ? a = u = s : u = u.next = s, l = l.next;
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
  var kc = !1;
  function Oa() {
    if (kc) {
      var t = Un;
      if (t !== null) throw t;
    }
  }
  function za(t, e, l, n) {
    kc = !1;
    var a = t.updateQueue;
    gl = !1;
    var u = a.firstBaseUpdate, s = a.lastBaseUpdate, d = a.shared.pending;
    if (d !== null) {
      a.shared.pending = null;
      var y = d, A = y.next;
      y.next = null, s === null ? u = A : s.next = A, s = y;
      var C = t.alternate;
      C !== null && (C = C.updateQueue, d = C.lastBaseUpdate, d !== s && (d === null ? C.firstBaseUpdate = A : d.next = A, C.lastBaseUpdate = y));
    }
    if (u !== null) {
      var w = a.baseState;
      s = 0, C = A = y = null, d = u;
      do {
        var O = d.lane & -536870913, _ = O !== d.lane;
        if (_ ? (ht & O) === O : (n & O) === O) {
          O !== 0 && O === wn && (kc = !0), C !== null && (C = C.next = {
            lane: 0,
            tag: d.tag,
            payload: d.payload,
            callback: null,
            next: null
          });
          t: {
            var J = t, tt = d;
            O = e;
            var Ot = l;
            switch (tt.tag) {
              case 1:
                if (J = tt.payload, typeof J == "function") {
                  w = J.call(Ot, w, O);
                  break t;
                }
                w = J;
                break t;
              case 3:
                J.flags = J.flags & -65537 | 128;
              case 0:
                if (J = tt.payload, O = typeof J == "function" ? J.call(Ot, w, O) : J, O == null) break t;
                w = z({}, w, O);
                break t;
              case 2:
                gl = !0;
            }
          }
          O = d.callback, O !== null && (t.flags |= 64, _ && (t.flags |= 8192), _ = a.callbacks, _ === null ? a.callbacks = [O] : _.push(O));
        } else
          _ = {
            lane: O,
            tag: d.tag,
            payload: d.payload,
            callback: d.callback,
            next: null
          }, C === null ? (A = C = _, y = w) : C = C.next = _, s |= O;
        if (d = d.next, d === null) {
          if (d = a.shared.pending, d === null)
            break;
          _ = d, d = _.next, _.next = null, a.lastBaseUpdate = _, a.shared.pending = null;
        }
      } while (!0);
      C === null && (y = w), a.baseState = y, a.firstBaseUpdate = A, a.lastBaseUpdate = C, u === null && (a.shared.lanes = 0), Rl |= s, t.lanes = s, t.memoizedState = w;
    }
  }
  function vr(t, e) {
    if (typeof t != "function")
      throw Error(f(191, t));
    t.call(e);
  }
  function yr(t, e) {
    var l = t.callbacks;
    if (l !== null)
      for (t.callbacks = null, t = 0; t < l.length; t++)
        vr(l[t], e);
  }
  var Ln = g(null), qu = g(0);
  function gr(t, e) {
    t = al, j(qu, t), j(Ln, e), al = t | e.baseLanes;
  }
  function Fc() {
    j(qu, al), j(Ln, Ln.current);
  }
  function $c() {
    al = qu.current, U(Ln), U(qu);
  }
  var he = g(null), Me = null;
  function pl(t) {
    var e = t.alternate;
    j(Bt, Bt.current & 1), j(he, t), Me === null && (e === null || Ln.current !== null || e.memoizedState !== null) && (Me = t);
  }
  function Ic(t) {
    j(Bt, Bt.current), j(he, t), Me === null && (Me = t);
  }
  function br(t) {
    t.tag === 22 ? (j(Bt, Bt.current), j(he, t), Me === null && (Me = t)) : El();
  }
  function El() {
    j(Bt, Bt.current), j(he, he.current);
  }
  function ve(t) {
    U(he), Me === t && (Me = null), U(Bt);
  }
  var Bt = g(0);
  function Yu(t) {
    for (var e = t; e !== null; ) {
      if (e.tag === 13) {
        var l = e.memoizedState;
        if (l !== null && (l = l.dehydrated, l === null || uo(l) || io(l)))
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
  var Fe = 0, ct = null, At = null, jt = null, ju = !1, qn = !1, en = !1, Xu = 0, _a = 0, Yn = null, Zy = 0;
  function Ut() {
    throw Error(f(321));
  }
  function Pc(t, e) {
    if (e === null) return !1;
    for (var l = 0; l < e.length && l < t.length; l++)
      if (!de(t[l], e[l])) return !1;
    return !0;
  }
  function tf(t, e, l, n, a, u) {
    return Fe = u, ct = e, e.memoizedState = null, e.updateQueue = null, e.lanes = 0, R.H = t === null || t.memoizedState === null ? ed : yf, en = !1, u = l(n, a), en = !1, qn && (u = pr(
      e,
      l,
      n,
      a
    )), Sr(t), u;
  }
  function Sr(t) {
    R.H = xa;
    var e = At !== null && At.next !== null;
    if (Fe = 0, jt = At = ct = null, ju = !1, _a = 0, Yn = null, e) throw Error(f(300));
    t === null || Xt || (t = t.dependencies, t !== null && Du(t) && (Xt = !0));
  }
  function pr(t, e, l, n) {
    ct = t;
    var a = 0;
    do {
      if (qn && (Yn = null), _a = 0, qn = !1, 25 <= a) throw Error(f(301));
      if (a += 1, jt = At = null, t.updateQueue != null) {
        var u = t.updateQueue;
        u.lastEffect = null, u.events = null, u.stores = null, u.memoCache != null && (u.memoCache.index = 0);
      }
      R.H = ld, u = e(l, n);
    } while (qn);
    return u;
  }
  function Ky() {
    var t = R.H, e = t.useState()[0];
    return e = typeof e.then == "function" ? Ma(e) : e, t = t.useState()[0], (At !== null ? At.memoizedState : null) !== t && (ct.flags |= 1024), e;
  }
  function ef() {
    var t = Xu !== 0;
    return Xu = 0, t;
  }
  function lf(t, e, l) {
    e.updateQueue = t.updateQueue, e.flags &= -2053, t.lanes &= ~l;
  }
  function nf(t) {
    if (ju) {
      for (t = t.memoizedState; t !== null; ) {
        var e = t.queue;
        e !== null && (e.pending = null), t = t.next;
      }
      ju = !1;
    }
    Fe = 0, jt = At = ct = null, qn = !1, _a = Xu = 0, Yn = null;
  }
  function ee() {
    var t = {
      memoizedState: null,
      baseState: null,
      baseQueue: null,
      queue: null,
      next: null
    };
    return jt === null ? ct.memoizedState = jt = t : jt = jt.next = t, jt;
  }
  function Lt() {
    if (At === null) {
      var t = ct.alternate;
      t = t !== null ? t.memoizedState : null;
    } else t = At.next;
    var e = jt === null ? ct.memoizedState : jt.next;
    if (e !== null)
      jt = e, At = t;
    else {
      if (t === null)
        throw ct.alternate === null ? Error(f(467)) : Error(f(310));
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
  function Gu() {
    return { lastEffect: null, events: null, stores: null, memoCache: null };
  }
  function Ma(t) {
    var e = _a;
    return _a += 1, Yn === null && (Yn = []), t = sr(Yn, t, e), e = ct, (jt === null ? e.memoizedState : jt.next) === null && (e = e.alternate, R.H = e === null || e.memoizedState === null ? ed : yf), t;
  }
  function Qu(t) {
    if (t !== null && typeof t == "object") {
      if (typeof t.then == "function") return Ma(t);
      if (t.$$typeof === G) return Ft(t);
    }
    throw Error(f(438, String(t)));
  }
  function af(t) {
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
    if (e == null && (e = { data: [], index: 0 }), l === null && (l = Gu(), ct.updateQueue = l), l.memoCache = e, l = e.data[e.index], l === void 0)
      for (l = e.data[e.index] = Array(t), n = 0; n < t; n++)
        l[n] = Tt;
    return e.index++, l;
  }
  function $e(t, e) {
    return typeof e == "function" ? e(t) : e;
  }
  function Vu(t) {
    var e = Lt();
    return uf(e, At, t);
  }
  function uf(t, e, l) {
    var n = t.queue;
    if (n === null) throw Error(f(311));
    n.lastRenderedReducer = l;
    var a = t.baseQueue, u = n.pending;
    if (u !== null) {
      if (a !== null) {
        var s = a.next;
        a.next = u.next, u.next = s;
      }
      e.baseQueue = a = u, n.pending = null;
    }
    if (u = t.baseState, a === null) t.memoizedState = u;
    else {
      e = a.next;
      var d = s = null, y = null, A = e, C = !1;
      do {
        var w = A.lane & -536870913;
        if (w !== A.lane ? (ht & w) === w : (Fe & w) === w) {
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
            }), w === wn && (C = !0);
          else if ((Fe & O) === O) {
            A = A.next, O === wn && (C = !0);
            continue;
          } else
            w = {
              lane: 0,
              revertLane: A.revertLane,
              gesture: null,
              action: A.action,
              hasEagerState: A.hasEagerState,
              eagerState: A.eagerState,
              next: null
            }, y === null ? (d = y = w, s = u) : y = y.next = w, ct.lanes |= O, Rl |= O;
          w = A.action, en && l(u, w), u = A.hasEagerState ? A.eagerState : l(u, w);
        } else
          O = {
            lane: w,
            revertLane: A.revertLane,
            gesture: A.gesture,
            action: A.action,
            hasEagerState: A.hasEagerState,
            eagerState: A.eagerState,
            next: null
          }, y === null ? (d = y = O, s = u) : y = y.next = O, ct.lanes |= w, Rl |= w;
        A = A.next;
      } while (A !== null && A !== e);
      if (y === null ? s = u : y.next = d, !de(u, t.memoizedState) && (Xt = !0, C && (l = Un, l !== null)))
        throw l;
      t.memoizedState = u, t.baseState = s, t.baseQueue = y, n.lastRenderedState = u;
    }
    return a === null && (n.lanes = 0), [t.memoizedState, n.dispatch];
  }
  function cf(t) {
    var e = Lt(), l = e.queue;
    if (l === null) throw Error(f(311));
    l.lastRenderedReducer = t;
    var n = l.dispatch, a = l.pending, u = e.memoizedState;
    if (a !== null) {
      l.pending = null;
      var s = a = a.next;
      do
        u = t(u, s.action), s = s.next;
      while (s !== a);
      de(u, e.memoizedState) || (Xt = !0), e.memoizedState = u, e.baseQueue === null && (e.baseState = u), l.lastRenderedState = u;
    }
    return [u, n];
  }
  function Er(t, e, l) {
    var n = ct, a = Lt(), u = yt;
    if (u) {
      if (l === void 0) throw Error(f(407));
      l = l();
    } else l = e();
    var s = !de(
      (At || a).memoizedState,
      l
    );
    if (s && (a.memoizedState = l, Xt = !0), a = a.queue, sf(Rr.bind(null, n, a, t), [
      t
    ]), a.getSnapshot !== e || s || jt !== null && jt.memoizedState.tag & 1) {
      if (n.flags |= 2048, jn(
        9,
        { destroy: void 0 },
        Ar.bind(
          null,
          n,
          a,
          l,
          e
        ),
        null
      ), Mt === null) throw Error(f(349));
      u || (Fe & 127) !== 0 || Tr(n, e, l);
    }
    return l;
  }
  function Tr(t, e, l) {
    t.flags |= 16384, t = { getSnapshot: e, value: l }, e = ct.updateQueue, e === null ? (e = Gu(), ct.updateQueue = e, e.stores = [t]) : (l = e.stores, l === null ? e.stores = [t] : l.push(t));
  }
  function Ar(t, e, l, n) {
    e.value = l, e.getSnapshot = n, Or(e) && zr(t);
  }
  function Rr(t, e, l) {
    return l(function() {
      Or(e) && zr(t);
    });
  }
  function Or(t) {
    var e = t.getSnapshot;
    t = t.value;
    try {
      var l = e();
      return !de(t, l);
    } catch {
      return !0;
    }
  }
  function zr(t) {
    var e = Kl(t, 2);
    e !== null && fe(e, t, 2);
  }
  function ff(t) {
    var e = ee();
    if (typeof t == "function") {
      var l = t;
      if (t = l(), en) {
        sl(!0);
        try {
          l();
        } finally {
          sl(!1);
        }
      }
    }
    return e.memoizedState = e.baseState = t, e.queue = {
      pending: null,
      lanes: 0,
      dispatch: null,
      lastRenderedReducer: $e,
      lastRenderedState: t
    }, e;
  }
  function _r(t, e, l, n) {
    return t.baseState = l, uf(
      t,
      At,
      typeof n == "function" ? n : $e
    );
  }
  function Jy(t, e, l, n, a) {
    if (Ju(t)) throw Error(f(485));
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
        then: function(s) {
          u.listeners.push(s);
        }
      };
      R.T !== null ? l(!0) : u.isTransition = !1, n(u), l = e.pending, l === null ? (u.next = e.pending = u, Mr(e, u)) : (u.next = l.next, e.pending = l.next = u);
    }
  }
  function Mr(t, e) {
    var l = e.action, n = e.payload, a = t.state;
    if (e.isTransition) {
      var u = R.T, s = {};
      R.T = s;
      try {
        var d = l(a, n), y = R.S;
        y !== null && y(s, d), Cr(t, e, d);
      } catch (A) {
        of(t, e, A);
      } finally {
        u !== null && s.types !== null && (u.types = s.types), R.T = u;
      }
    } else
      try {
        u = l(a, n), Cr(t, e, u);
      } catch (A) {
        of(t, e, A);
      }
  }
  function Cr(t, e, l) {
    l !== null && typeof l == "object" && typeof l.then == "function" ? l.then(
      function(n) {
        xr(t, e, n);
      },
      function(n) {
        return of(t, e, n);
      }
    ) : xr(t, e, l);
  }
  function xr(t, e, l) {
    e.status = "fulfilled", e.value = l, Dr(e), t.state = l, e = t.pending, e !== null && (l = e.next, l === e ? t.pending = null : (l = l.next, e.next = l, Mr(t, l)));
  }
  function of(t, e, l) {
    var n = t.pending;
    if (t.pending = null, n !== null) {
      n = n.next;
      do
        e.status = "rejected", e.reason = l, Dr(e), e = e.next;
      while (e !== n);
    }
    t.action = null;
  }
  function Dr(t) {
    t = t.listeners;
    for (var e = 0; e < t.length; e++) (0, t[e])();
  }
  function Nr(t, e) {
    return e;
  }
  function wr(t, e) {
    if (yt) {
      var l = Mt.formState;
      if (l !== null) {
        t: {
          var n = ct;
          if (yt) {
            if (xt) {
              e: {
                for (var a = xt, u = _e; a.nodeType !== 8; ) {
                  if (!u) {
                    a = null;
                    break e;
                  }
                  if (a = Ce(
                    a.nextSibling
                  ), a === null) {
                    a = null;
                    break e;
                  }
                }
                u = a.data, a = u === "F!" || u === "F" ? a : null;
              }
              if (a) {
                xt = Ce(
                  a.nextSibling
                ), n = a.data === "F!";
                break t;
              }
            }
            vl(n);
          }
          n = !1;
        }
        n && (e = l[0]);
      }
    }
    return l = ee(), l.memoizedState = l.baseState = e, n = {
      pending: null,
      lanes: 0,
      dispatch: null,
      lastRenderedReducer: Nr,
      lastRenderedState: e
    }, l.queue = n, l = Ir.bind(
      null,
      ct,
      n
    ), n.dispatch = l, n = ff(!1), u = vf.bind(
      null,
      ct,
      !1,
      n.queue
    ), n = ee(), a = {
      state: e,
      dispatch: null,
      action: t,
      pending: null
    }, n.queue = a, l = Jy.bind(
      null,
      ct,
      a,
      u,
      l
    ), a.dispatch = l, n.memoizedState = t, [e, l, !1];
  }
  function Ur(t) {
    var e = Lt();
    return Hr(e, At, t);
  }
  function Hr(t, e, l) {
    if (e = uf(
      t,
      e,
      Nr
    )[0], t = Vu($e)[0], typeof e == "object" && e !== null && typeof e.then == "function")
      try {
        var n = Ma(e);
      } catch (s) {
        throw s === Hn ? Uu : s;
      }
    else n = e;
    e = Lt();
    var a = e.queue, u = a.dispatch;
    return l !== e.memoizedState && (ct.flags |= 2048, jn(
      9,
      { destroy: void 0 },
      Wy.bind(null, a, l),
      null
    )), [n, u, t];
  }
  function Wy(t, e) {
    t.action = e;
  }
  function Br(t) {
    var e = Lt(), l = At;
    if (l !== null)
      return Hr(e, l, t);
    Lt(), e = e.memoizedState, l = Lt();
    var n = l.queue.dispatch;
    return l.memoizedState = t, [e, n, !1];
  }
  function jn(t, e, l, n) {
    return t = { tag: t, create: l, deps: n, inst: e, next: null }, e = ct.updateQueue, e === null && (e = Gu(), ct.updateQueue = e), l = e.lastEffect, l === null ? e.lastEffect = t.next = t : (n = l.next, l.next = t, t.next = n, e.lastEffect = t), t;
  }
  function Lr() {
    return Lt().memoizedState;
  }
  function Zu(t, e, l, n) {
    var a = ee();
    ct.flags |= t, a.memoizedState = jn(
      1 | e,
      { destroy: void 0 },
      l,
      n === void 0 ? null : n
    );
  }
  function Ku(t, e, l, n) {
    var a = Lt();
    n = n === void 0 ? null : n;
    var u = a.memoizedState.inst;
    At !== null && n !== null && Pc(n, At.memoizedState.deps) ? a.memoizedState = jn(e, u, l, n) : (ct.flags |= t, a.memoizedState = jn(
      1 | e,
      u,
      l,
      n
    ));
  }
  function qr(t, e) {
    Zu(8390656, 8, t, e);
  }
  function sf(t, e) {
    Ku(2048, 8, t, e);
  }
  function ky(t) {
    ct.flags |= 4;
    var e = ct.updateQueue;
    if (e === null)
      e = Gu(), ct.updateQueue = e, e.events = [t];
    else {
      var l = e.events;
      l === null ? e.events = [t] : l.push(t);
    }
  }
  function Yr(t) {
    var e = Lt().memoizedState;
    return ky({ ref: e, nextImpl: t }), function() {
      if ((St & 2) !== 0) throw Error(f(440));
      return e.impl.apply(void 0, arguments);
    };
  }
  function jr(t, e) {
    return Ku(4, 2, t, e);
  }
  function Xr(t, e) {
    return Ku(4, 4, t, e);
  }
  function Gr(t, e) {
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
  function Qr(t, e, l) {
    l = l != null ? l.concat([t]) : null, Ku(4, 4, Gr.bind(null, e, t), l);
  }
  function rf() {
  }
  function Vr(t, e) {
    var l = Lt();
    e = e === void 0 ? null : e;
    var n = l.memoizedState;
    return e !== null && Pc(e, n[1]) ? n[0] : (l.memoizedState = [t, e], t);
  }
  function Zr(t, e) {
    var l = Lt();
    e = e === void 0 ? null : e;
    var n = l.memoizedState;
    if (e !== null && Pc(e, n[1]))
      return n[0];
    if (n = t(), en) {
      sl(!0);
      try {
        t();
      } finally {
        sl(!1);
      }
    }
    return l.memoizedState = [n, e], n;
  }
  function df(t, e, l) {
    return l === void 0 || (Fe & 1073741824) !== 0 && (ht & 261930) === 0 ? t.memoizedState = e : (t.memoizedState = l, t = Kd(), ct.lanes |= t, Rl |= t, l);
  }
  function Kr(t, e, l, n) {
    return de(l, e) ? l : Ln.current !== null ? (t = df(t, l, n), de(t, e) || (Xt = !0), t) : (Fe & 42) === 0 || (Fe & 1073741824) !== 0 && (ht & 261930) === 0 ? (Xt = !0, t.memoizedState = l) : (t = Kd(), ct.lanes |= t, Rl |= t, e);
  }
  function Jr(t, e, l, n, a) {
    var u = B.p;
    B.p = u !== 0 && 8 > u ? u : 8;
    var s = R.T, d = {};
    R.T = d, vf(t, !1, e, l);
    try {
      var y = a(), A = R.S;
      if (A !== null && A(d, y), y !== null && typeof y == "object" && typeof y.then == "function") {
        var C = Vy(
          y,
          n
        );
        Ca(
          t,
          e,
          C,
          be(t)
        );
      } else
        Ca(
          t,
          e,
          n,
          be(t)
        );
    } catch (w) {
      Ca(
        t,
        e,
        { then: function() {
        }, status: "rejected", reason: w },
        be()
      );
    } finally {
      B.p = u, s !== null && d.types !== null && (s.types = d.types), R.T = s;
    }
  }
  function Fy() {
  }
  function mf(t, e, l, n) {
    if (t.tag !== 5) throw Error(f(476));
    var a = Wr(t).queue;
    Jr(
      t,
      a,
      e,
      H,
      l === null ? Fy : function() {
        return kr(t), l(n);
      }
    );
  }
  function Wr(t) {
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
        lastRenderedReducer: $e,
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
        lastRenderedReducer: $e,
        lastRenderedState: l
      },
      next: null
    }, t.memoizedState = e, t = t.alternate, t !== null && (t.memoizedState = e), e;
  }
  function kr(t) {
    var e = Wr(t);
    e.next === null && (e = t.alternate.memoizedState), Ca(
      t,
      e.next.queue,
      {},
      be()
    );
  }
  function hf() {
    return Ft(Ka);
  }
  function Fr() {
    return Lt().memoizedState;
  }
  function $r() {
    return Lt().memoizedState;
  }
  function $y(t) {
    for (var e = t.return; e !== null; ) {
      switch (e.tag) {
        case 24:
        case 3:
          var l = be();
          t = bl(l);
          var n = Sl(e, t, l);
          n !== null && (fe(n, e, l), Ra(n, e, l)), e = { cache: Gc() }, t.payload = e;
          return;
      }
      e = e.return;
    }
  }
  function Iy(t, e, l) {
    var n = be();
    l = {
      lane: n,
      revertLane: 0,
      gesture: null,
      action: l,
      hasEagerState: !1,
      eagerState: null,
      next: null
    }, Ju(t) ? Pr(e, l) : (l = Dc(t, e, l, n), l !== null && (fe(l, t, n), td(l, e, n)));
  }
  function Ir(t, e, l) {
    var n = be();
    Ca(t, e, l, n);
  }
  function Ca(t, e, l, n) {
    var a = {
      lane: n,
      revertLane: 0,
      gesture: null,
      action: l,
      hasEagerState: !1,
      eagerState: null,
      next: null
    };
    if (Ju(t)) Pr(e, a);
    else {
      var u = t.alternate;
      if (t.lanes === 0 && (u === null || u.lanes === 0) && (u = e.lastRenderedReducer, u !== null))
        try {
          var s = e.lastRenderedState, d = u(s, l);
          if (a.hasEagerState = !0, a.eagerState = d, de(d, s))
            return _u(t, e, a, 0), Mt === null && zu(), !1;
        } catch {
        } finally {
        }
      if (l = Dc(t, e, a, n), l !== null)
        return fe(l, t, n), td(l, e, n), !0;
    }
    return !1;
  }
  function vf(t, e, l, n) {
    if (n = {
      lane: 2,
      revertLane: Jf(),
      gesture: null,
      action: n,
      hasEagerState: !1,
      eagerState: null,
      next: null
    }, Ju(t)) {
      if (e) throw Error(f(479));
    } else
      e = Dc(
        t,
        l,
        n,
        2
      ), e !== null && fe(e, t, 2);
  }
  function Ju(t) {
    var e = t.alternate;
    return t === ct || e !== null && e === ct;
  }
  function Pr(t, e) {
    qn = ju = !0;
    var l = t.pending;
    l === null ? e.next = e : (e.next = l.next, l.next = e), t.pending = e;
  }
  function td(t, e, l) {
    if ((l & 4194048) !== 0) {
      var n = e.lanes;
      n &= t.pendingLanes, l |= n, e.lanes = l, us(t, l);
    }
  }
  var xa = {
    readContext: Ft,
    use: Qu,
    useCallback: Ut,
    useContext: Ut,
    useEffect: Ut,
    useImperativeHandle: Ut,
    useLayoutEffect: Ut,
    useInsertionEffect: Ut,
    useMemo: Ut,
    useReducer: Ut,
    useRef: Ut,
    useState: Ut,
    useDebugValue: Ut,
    useDeferredValue: Ut,
    useTransition: Ut,
    useSyncExternalStore: Ut,
    useId: Ut,
    useHostTransitionStatus: Ut,
    useFormState: Ut,
    useActionState: Ut,
    useOptimistic: Ut,
    useMemoCache: Ut,
    useCacheRefresh: Ut
  };
  xa.useEffectEvent = Ut;
  var ed = {
    readContext: Ft,
    use: Qu,
    useCallback: function(t, e) {
      return ee().memoizedState = [
        t,
        e === void 0 ? null : e
      ], t;
    },
    useContext: Ft,
    useEffect: qr,
    useImperativeHandle: function(t, e, l) {
      l = l != null ? l.concat([t]) : null, Zu(
        4194308,
        4,
        Gr.bind(null, e, t),
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
      var l = ee();
      e = e === void 0 ? null : e;
      var n = t();
      if (en) {
        sl(!0);
        try {
          t();
        } finally {
          sl(!1);
        }
      }
      return l.memoizedState = [n, e], n;
    },
    useReducer: function(t, e, l) {
      var n = ee();
      if (l !== void 0) {
        var a = l(e);
        if (en) {
          sl(!0);
          try {
            l(e);
          } finally {
            sl(!1);
          }
        }
      } else a = e;
      return n.memoizedState = n.baseState = a, t = {
        pending: null,
        lanes: 0,
        dispatch: null,
        lastRenderedReducer: t,
        lastRenderedState: a
      }, n.queue = t, t = t.dispatch = Iy.bind(
        null,
        ct,
        t
      ), [n.memoizedState, t];
    },
    useRef: function(t) {
      var e = ee();
      return t = { current: t }, e.memoizedState = t;
    },
    useState: function(t) {
      t = ff(t);
      var e = t.queue, l = Ir.bind(null, ct, e);
      return e.dispatch = l, [t.memoizedState, l];
    },
    useDebugValue: rf,
    useDeferredValue: function(t, e) {
      var l = ee();
      return df(l, t, e);
    },
    useTransition: function() {
      var t = ff(!1);
      return t = Jr.bind(
        null,
        ct,
        t.queue,
        !0,
        !1
      ), ee().memoizedState = t, [!1, t];
    },
    useSyncExternalStore: function(t, e, l) {
      var n = ct, a = ee();
      if (yt) {
        if (l === void 0)
          throw Error(f(407));
        l = l();
      } else {
        if (l = e(), Mt === null)
          throw Error(f(349));
        (ht & 127) !== 0 || Tr(n, e, l);
      }
      a.memoizedState = l;
      var u = { value: l, getSnapshot: e };
      return a.queue = u, qr(Rr.bind(null, n, u, t), [
        t
      ]), n.flags |= 2048, jn(
        9,
        { destroy: void 0 },
        Ar.bind(
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
      var t = ee(), e = Mt.identifierPrefix;
      if (yt) {
        var l = He, n = Ue;
        l = (n & ~(1 << 32 - re(n) - 1)).toString(32) + l, e = "_" + e + "R_" + l, l = Xu++, 0 < l && (e += "H" + l.toString(32)), e += "_";
      } else
        l = Zy++, e = "_" + e + "r_" + l.toString(32) + "_";
      return t.memoizedState = e;
    },
    useHostTransitionStatus: hf,
    useFormState: wr,
    useActionState: wr,
    useOptimistic: function(t) {
      var e = ee();
      e.memoizedState = e.baseState = t;
      var l = {
        pending: null,
        lanes: 0,
        dispatch: null,
        lastRenderedReducer: null,
        lastRenderedState: null
      };
      return e.queue = l, e = vf.bind(
        null,
        ct,
        !0,
        l
      ), l.dispatch = e, [t, e];
    },
    useMemoCache: af,
    useCacheRefresh: function() {
      return ee().memoizedState = $y.bind(
        null,
        ct
      );
    },
    useEffectEvent: function(t) {
      var e = ee(), l = { impl: t };
      return e.memoizedState = l, function() {
        if ((St & 2) !== 0)
          throw Error(f(440));
        return l.impl.apply(void 0, arguments);
      };
    }
  }, yf = {
    readContext: Ft,
    use: Qu,
    useCallback: Vr,
    useContext: Ft,
    useEffect: sf,
    useImperativeHandle: Qr,
    useInsertionEffect: jr,
    useLayoutEffect: Xr,
    useMemo: Zr,
    useReducer: Vu,
    useRef: Lr,
    useState: function() {
      return Vu($e);
    },
    useDebugValue: rf,
    useDeferredValue: function(t, e) {
      var l = Lt();
      return Kr(
        l,
        At.memoizedState,
        t,
        e
      );
    },
    useTransition: function() {
      var t = Vu($e)[0], e = Lt().memoizedState;
      return [
        typeof t == "boolean" ? t : Ma(t),
        e
      ];
    },
    useSyncExternalStore: Er,
    useId: Fr,
    useHostTransitionStatus: hf,
    useFormState: Ur,
    useActionState: Ur,
    useOptimistic: function(t, e) {
      var l = Lt();
      return _r(l, At, t, e);
    },
    useMemoCache: af,
    useCacheRefresh: $r
  };
  yf.useEffectEvent = Yr;
  var ld = {
    readContext: Ft,
    use: Qu,
    useCallback: Vr,
    useContext: Ft,
    useEffect: sf,
    useImperativeHandle: Qr,
    useInsertionEffect: jr,
    useLayoutEffect: Xr,
    useMemo: Zr,
    useReducer: cf,
    useRef: Lr,
    useState: function() {
      return cf($e);
    },
    useDebugValue: rf,
    useDeferredValue: function(t, e) {
      var l = Lt();
      return At === null ? df(l, t, e) : Kr(
        l,
        At.memoizedState,
        t,
        e
      );
    },
    useTransition: function() {
      var t = cf($e)[0], e = Lt().memoizedState;
      return [
        typeof t == "boolean" ? t : Ma(t),
        e
      ];
    },
    useSyncExternalStore: Er,
    useId: Fr,
    useHostTransitionStatus: hf,
    useFormState: Br,
    useActionState: Br,
    useOptimistic: function(t, e) {
      var l = Lt();
      return At !== null ? _r(l, At, t, e) : (l.baseState = t, [t, l.queue.dispatch]);
    },
    useMemoCache: af,
    useCacheRefresh: $r
  };
  ld.useEffectEvent = Yr;
  function gf(t, e, l, n) {
    e = t.memoizedState, l = l(n, e), l = l == null ? e : z({}, e, l), t.memoizedState = l, t.lanes === 0 && (t.updateQueue.baseState = l);
  }
  var bf = {
    enqueueSetState: function(t, e, l) {
      t = t._reactInternals;
      var n = be(), a = bl(n);
      a.payload = e, l != null && (a.callback = l), e = Sl(t, a, n), e !== null && (fe(e, t, n), Ra(e, t, n));
    },
    enqueueReplaceState: function(t, e, l) {
      t = t._reactInternals;
      var n = be(), a = bl(n);
      a.tag = 1, a.payload = e, l != null && (a.callback = l), e = Sl(t, a, n), e !== null && (fe(e, t, n), Ra(e, t, n));
    },
    enqueueForceUpdate: function(t, e) {
      t = t._reactInternals;
      var l = be(), n = bl(l);
      n.tag = 2, e != null && (n.callback = e), e = Sl(t, n, l), e !== null && (fe(e, t, l), Ra(e, t, l));
    }
  };
  function nd(t, e, l, n, a, u, s) {
    return t = t.stateNode, typeof t.shouldComponentUpdate == "function" ? t.shouldComponentUpdate(n, u, s) : e.prototype && e.prototype.isPureReactComponent ? !ya(l, n) || !ya(a, u) : !0;
  }
  function ad(t, e, l, n) {
    t = e.state, typeof e.componentWillReceiveProps == "function" && e.componentWillReceiveProps(l, n), typeof e.UNSAFE_componentWillReceiveProps == "function" && e.UNSAFE_componentWillReceiveProps(l, n), e.state !== t && bf.enqueueReplaceState(e, e.state, null);
  }
  function ln(t, e) {
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
  function ud(t) {
    Ou(t);
  }
  function id(t) {
    console.error(t);
  }
  function cd(t) {
    Ou(t);
  }
  function Wu(t, e) {
    try {
      var l = t.onUncaughtError;
      l(e.value, { componentStack: e.stack });
    } catch (n) {
      setTimeout(function() {
        throw n;
      });
    }
  }
  function fd(t, e, l) {
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
  function Sf(t, e, l) {
    return l = bl(l), l.tag = 3, l.payload = { element: null }, l.callback = function() {
      Wu(t, e);
    }, l;
  }
  function od(t) {
    return t = bl(t), t.tag = 3, t;
  }
  function sd(t, e, l, n) {
    var a = l.type.getDerivedStateFromError;
    if (typeof a == "function") {
      var u = n.value;
      t.payload = function() {
        return a(u);
      }, t.callback = function() {
        fd(e, l, n);
      };
    }
    var s = l.stateNode;
    s !== null && typeof s.componentDidCatch == "function" && (t.callback = function() {
      fd(e, l, n), typeof a != "function" && (Ol === null ? Ol = /* @__PURE__ */ new Set([this]) : Ol.add(this));
      var d = n.stack;
      this.componentDidCatch(n.value, {
        componentStack: d !== null ? d : ""
      });
    });
  }
  function Py(t, e, l, n, a) {
    if (l.flags |= 32768, n !== null && typeof n == "object" && typeof n.then == "function") {
      if (e = l.alternate, e !== null && Nn(
        e,
        l,
        a,
        !0
      ), l = he.current, l !== null) {
        switch (l.tag) {
          case 31:
          case 13:
            return Me === null ? ii() : l.alternate === null && Ht === 0 && (Ht = 3), l.flags &= -257, l.flags |= 65536, l.lanes = a, n === Hu ? l.flags |= 16384 : (e = l.updateQueue, e === null ? l.updateQueue = /* @__PURE__ */ new Set([n]) : e.add(n), Vf(t, n, a)), !1;
          case 22:
            return l.flags |= 65536, n === Hu ? l.flags |= 16384 : (e = l.updateQueue, e === null ? (e = {
              transitions: null,
              markerInstances: null,
              retryQueue: /* @__PURE__ */ new Set([n])
            }, l.updateQueue = e) : (l = e.retryQueue, l === null ? e.retryQueue = /* @__PURE__ */ new Set([n]) : l.add(n)), Vf(t, n, a)), !1;
        }
        throw Error(f(435, l.tag));
      }
      return Vf(t, n, a), ii(), !1;
    }
    if (yt)
      return e = he.current, e !== null ? ((e.flags & 65536) === 0 && (e.flags |= 256), e.flags |= 65536, e.lanes = a, n !== Lc && (t = Error(f(422), { cause: n }), Sa(Re(t, l)))) : (n !== Lc && (e = Error(f(423), {
        cause: n
      }), Sa(
        Re(e, l)
      )), t = t.current.alternate, t.flags |= 65536, a &= -a, t.lanes |= a, n = Re(n, l), a = Sf(
        t.stateNode,
        n,
        a
      ), Wc(t, a), Ht !== 4 && (Ht = 2)), !1;
    var u = Error(f(520), { cause: n });
    if (u = Re(u, l), qa === null ? qa = [u] : qa.push(u), Ht !== 4 && (Ht = 2), e === null) return !0;
    n = Re(n, l), l = e;
    do {
      switch (l.tag) {
        case 3:
          return l.flags |= 65536, t = a & -a, l.lanes |= t, t = Sf(l.stateNode, n, t), Wc(l, t), !1;
        case 1:
          if (e = l.type, u = l.stateNode, (l.flags & 128) === 0 && (typeof e.getDerivedStateFromError == "function" || u !== null && typeof u.componentDidCatch == "function" && (Ol === null || !Ol.has(u))))
            return l.flags |= 65536, a &= -a, l.lanes |= a, a = od(a), sd(
              a,
              t,
              l,
              n
            ), Wc(l, a), !1;
      }
      l = l.return;
    } while (l !== null);
    return !1;
  }
  var pf = Error(f(461)), Xt = !1;
  function $t(t, e, l, n) {
    e.child = t === null ? hr(e, null, l, n) : tn(
      e,
      t.child,
      l,
      n
    );
  }
  function rd(t, e, l, n, a) {
    l = l.render;
    var u = e.ref;
    if ("ref" in n) {
      var s = {};
      for (var d in n)
        d !== "ref" && (s[d] = n[d]);
    } else s = n;
    return Fl(e), n = tf(
      t,
      e,
      l,
      s,
      u,
      a
    ), d = ef(), t !== null && !Xt ? (lf(t, e, a), Ie(t, e, a)) : (yt && d && Hc(e), e.flags |= 1, $t(t, e, n, a), e.child);
  }
  function dd(t, e, l, n, a) {
    if (t === null) {
      var u = l.type;
      return typeof u == "function" && !Nc(u) && u.defaultProps === void 0 && l.compare === null ? (e.tag = 15, e.type = u, md(
        t,
        e,
        u,
        n,
        a
      )) : (t = Cu(
        l.type,
        null,
        n,
        e,
        e.mode,
        a
      ), t.ref = e.ref, t.return = e, e.child = t);
    }
    if (u = t.child, !Mf(t, a)) {
      var s = u.memoizedProps;
      if (l = l.compare, l = l !== null ? l : ya, l(s, n) && t.ref === e.ref)
        return Ie(t, e, a);
    }
    return e.flags |= 1, t = Ke(u, n), t.ref = e.ref, t.return = e, e.child = t;
  }
  function md(t, e, l, n, a) {
    if (t !== null) {
      var u = t.memoizedProps;
      if (ya(u, n) && t.ref === e.ref)
        if (Xt = !1, e.pendingProps = n = u, Mf(t, a))
          (t.flags & 131072) !== 0 && (Xt = !0);
        else
          return e.lanes = t.lanes, Ie(t, e, a);
    }
    return Ef(
      t,
      e,
      l,
      n,
      a
    );
  }
  function hd(t, e, l, n) {
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
        return vd(
          t,
          e,
          u,
          l,
          n
        );
      }
      if ((l & 536870912) !== 0)
        e.memoizedState = { baseLanes: 0, cachePool: null }, t !== null && wu(
          e,
          u !== null ? u.cachePool : null
        ), u !== null ? gr(e, u) : Fc(), br(e);
      else
        return n = e.lanes = 536870912, vd(
          t,
          e,
          u !== null ? u.baseLanes | l : l,
          l,
          n
        );
    } else
      u !== null ? (wu(e, u.cachePool), gr(e, u), El(), e.memoizedState = null) : (t !== null && wu(e, null), Fc(), El());
    return $t(t, e, a, l), e.child;
  }
  function Da(t, e) {
    return t !== null && t.tag === 22 || e.stateNode !== null || (e.stateNode = {
      _visibility: 1,
      _pendingMarkers: null,
      _retryCache: null,
      _transitions: null
    }), e.sibling;
  }
  function vd(t, e, l, n, a) {
    var u = Vc();
    return u = u === null ? null : { parent: Yt._currentValue, pool: u }, e.memoizedState = {
      baseLanes: l,
      cachePool: u
    }, t !== null && wu(e, null), Fc(), br(e), t !== null && Nn(t, e, n, !0), e.childLanes = a, null;
  }
  function ku(t, e) {
    return e = $u(
      { mode: e.mode, children: e.children },
      t.mode
    ), e.ref = t.ref, t.child = e, e.return = t, e;
  }
  function yd(t, e, l) {
    return tn(e, t.child, null, l), t = ku(e, e.pendingProps), t.flags |= 2, ve(e), e.memoizedState = null, t;
  }
  function t0(t, e, l) {
    var n = e.pendingProps, a = (e.flags & 128) !== 0;
    if (e.flags &= -129, t === null) {
      if (yt) {
        if (n.mode === "hidden")
          return t = ku(e, n), e.lanes = 536870912, Da(null, t);
        if (Ic(e), (t = xt) ? (t = Mm(
          t,
          _e
        ), t = t !== null && t.data === "&" ? t : null, t !== null && (e.memoizedState = {
          dehydrated: t,
          treeContext: ml !== null ? { id: Ue, overflow: He } : null,
          retryLane: 536870912,
          hydrationErrors: null
        }, l = Ps(t), l.return = e, e.child = l, kt = e, xt = null)) : t = null, t === null) throw vl(e);
        return e.lanes = 536870912, null;
      }
      return ku(e, n);
    }
    var u = t.memoizedState;
    if (u !== null) {
      var s = u.dehydrated;
      if (Ic(e), a)
        if (e.flags & 256)
          e.flags &= -257, e = yd(
            t,
            e,
            l
          );
        else if (e.memoizedState !== null)
          e.child = t.child, e.flags |= 128, e = null;
        else throw Error(f(558));
      else if (Xt || Nn(t, e, l, !1), a = (l & t.childLanes) !== 0, Xt || a) {
        if (n = Mt, n !== null && (s = is(n, l), s !== 0 && s !== u.retryLane))
          throw u.retryLane = s, Kl(t, s), fe(n, t, s), pf;
        ii(), e = yd(
          t,
          e,
          l
        );
      } else
        t = u.treeContext, xt = Ce(s.nextSibling), kt = e, yt = !0, hl = null, _e = !1, t !== null && lr(e, t), e = ku(e, n), e.flags |= 4096;
      return e;
    }
    return t = Ke(t.child, {
      mode: n.mode,
      children: n.children
    }), t.ref = e.ref, e.child = t, t.return = e, t;
  }
  function Fu(t, e) {
    var l = e.ref;
    if (l === null)
      t !== null && t.ref !== null && (e.flags |= 4194816);
    else {
      if (typeof l != "function" && typeof l != "object")
        throw Error(f(284));
      (t === null || t.ref !== l) && (e.flags |= 4194816);
    }
  }
  function Ef(t, e, l, n, a) {
    return Fl(e), l = tf(
      t,
      e,
      l,
      n,
      void 0,
      a
    ), n = ef(), t !== null && !Xt ? (lf(t, e, a), Ie(t, e, a)) : (yt && n && Hc(e), e.flags |= 1, $t(t, e, l, a), e.child);
  }
  function gd(t, e, l, n, a, u) {
    return Fl(e), e.updateQueue = null, l = pr(
      e,
      n,
      l,
      a
    ), Sr(t), n = ef(), t !== null && !Xt ? (lf(t, e, u), Ie(t, e, u)) : (yt && n && Hc(e), e.flags |= 1, $t(t, e, l, u), e.child);
  }
  function bd(t, e, l, n, a) {
    if (Fl(e), e.stateNode === null) {
      var u = Mn, s = l.contextType;
      typeof s == "object" && s !== null && (u = Ft(s)), u = new l(n, u), e.memoizedState = u.state !== null && u.state !== void 0 ? u.state : null, u.updater = bf, e.stateNode = u, u._reactInternals = e, u = e.stateNode, u.props = n, u.state = e.memoizedState, u.refs = {}, Kc(e), s = l.contextType, u.context = typeof s == "object" && s !== null ? Ft(s) : Mn, u.state = e.memoizedState, s = l.getDerivedStateFromProps, typeof s == "function" && (gf(
        e,
        l,
        s,
        n
      ), u.state = e.memoizedState), typeof l.getDerivedStateFromProps == "function" || typeof u.getSnapshotBeforeUpdate == "function" || typeof u.UNSAFE_componentWillMount != "function" && typeof u.componentWillMount != "function" || (s = u.state, typeof u.componentWillMount == "function" && u.componentWillMount(), typeof u.UNSAFE_componentWillMount == "function" && u.UNSAFE_componentWillMount(), s !== u.state && bf.enqueueReplaceState(u, u.state, null), za(e, n, u, a), Oa(), u.state = e.memoizedState), typeof u.componentDidMount == "function" && (e.flags |= 4194308), n = !0;
    } else if (t === null) {
      u = e.stateNode;
      var d = e.memoizedProps, y = ln(l, d);
      u.props = y;
      var A = u.context, C = l.contextType;
      s = Mn, typeof C == "object" && C !== null && (s = Ft(C));
      var w = l.getDerivedStateFromProps;
      C = typeof w == "function" || typeof u.getSnapshotBeforeUpdate == "function", d = e.pendingProps !== d, C || typeof u.UNSAFE_componentWillReceiveProps != "function" && typeof u.componentWillReceiveProps != "function" || (d || A !== s) && ad(
        e,
        u,
        n,
        s
      ), gl = !1;
      var O = e.memoizedState;
      u.state = O, za(e, n, u, a), Oa(), A = e.memoizedState, d || O !== A || gl ? (typeof w == "function" && (gf(
        e,
        l,
        w,
        n
      ), A = e.memoizedState), (y = gl || nd(
        e,
        l,
        y,
        n,
        O,
        A,
        s
      )) ? (C || typeof u.UNSAFE_componentWillMount != "function" && typeof u.componentWillMount != "function" || (typeof u.componentWillMount == "function" && u.componentWillMount(), typeof u.UNSAFE_componentWillMount == "function" && u.UNSAFE_componentWillMount()), typeof u.componentDidMount == "function" && (e.flags |= 4194308)) : (typeof u.componentDidMount == "function" && (e.flags |= 4194308), e.memoizedProps = n, e.memoizedState = A), u.props = n, u.state = A, u.context = s, n = y) : (typeof u.componentDidMount == "function" && (e.flags |= 4194308), n = !1);
    } else {
      u = e.stateNode, Jc(t, e), s = e.memoizedProps, C = ln(l, s), u.props = C, w = e.pendingProps, O = u.context, A = l.contextType, y = Mn, typeof A == "object" && A !== null && (y = Ft(A)), d = l.getDerivedStateFromProps, (A = typeof d == "function" || typeof u.getSnapshotBeforeUpdate == "function") || typeof u.UNSAFE_componentWillReceiveProps != "function" && typeof u.componentWillReceiveProps != "function" || (s !== w || O !== y) && ad(
        e,
        u,
        n,
        y
      ), gl = !1, O = e.memoizedState, u.state = O, za(e, n, u, a), Oa();
      var _ = e.memoizedState;
      s !== w || O !== _ || gl || t !== null && t.dependencies !== null && Du(t.dependencies) ? (typeof d == "function" && (gf(
        e,
        l,
        d,
        n
      ), _ = e.memoizedState), (C = gl || nd(
        e,
        l,
        C,
        n,
        O,
        _,
        y
      ) || t !== null && t.dependencies !== null && Du(t.dependencies)) ? (A || typeof u.UNSAFE_componentWillUpdate != "function" && typeof u.componentWillUpdate != "function" || (typeof u.componentWillUpdate == "function" && u.componentWillUpdate(n, _, y), typeof u.UNSAFE_componentWillUpdate == "function" && u.UNSAFE_componentWillUpdate(
        n,
        _,
        y
      )), typeof u.componentDidUpdate == "function" && (e.flags |= 4), typeof u.getSnapshotBeforeUpdate == "function" && (e.flags |= 1024)) : (typeof u.componentDidUpdate != "function" || s === t.memoizedProps && O === t.memoizedState || (e.flags |= 4), typeof u.getSnapshotBeforeUpdate != "function" || s === t.memoizedProps && O === t.memoizedState || (e.flags |= 1024), e.memoizedProps = n, e.memoizedState = _), u.props = n, u.state = _, u.context = y, n = C) : (typeof u.componentDidUpdate != "function" || s === t.memoizedProps && O === t.memoizedState || (e.flags |= 4), typeof u.getSnapshotBeforeUpdate != "function" || s === t.memoizedProps && O === t.memoizedState || (e.flags |= 1024), n = !1);
    }
    return u = n, Fu(t, e), n = (e.flags & 128) !== 0, u || n ? (u = e.stateNode, l = n && typeof l.getDerivedStateFromError != "function" ? null : u.render(), e.flags |= 1, t !== null && n ? (e.child = tn(
      e,
      t.child,
      null,
      a
    ), e.child = tn(
      e,
      null,
      l,
      a
    )) : $t(t, e, l, a), e.memoizedState = u.state, t = e.child) : t = Ie(
      t,
      e,
      a
    ), t;
  }
  function Sd(t, e, l, n) {
    return Wl(), e.flags |= 256, $t(t, e, l, n), e.child;
  }
  var Tf = {
    dehydrated: null,
    treeContext: null,
    retryLane: 0,
    hydrationErrors: null
  };
  function Af(t) {
    return { baseLanes: t, cachePool: fr() };
  }
  function Rf(t, e, l) {
    return t = t !== null ? t.childLanes & ~l : 0, e && (t |= ge), t;
  }
  function pd(t, e, l) {
    var n = e.pendingProps, a = !1, u = (e.flags & 128) !== 0, s;
    if ((s = u) || (s = t !== null && t.memoizedState === null ? !1 : (Bt.current & 2) !== 0), s && (a = !0, e.flags &= -129), s = (e.flags & 32) !== 0, e.flags &= -33, t === null) {
      if (yt) {
        if (a ? pl(e) : El(), (t = xt) ? (t = Mm(
          t,
          _e
        ), t = t !== null && t.data !== "&" ? t : null, t !== null && (e.memoizedState = {
          dehydrated: t,
          treeContext: ml !== null ? { id: Ue, overflow: He } : null,
          retryLane: 536870912,
          hydrationErrors: null
        }, l = Ps(t), l.return = e, e.child = l, kt = e, xt = null)) : t = null, t === null) throw vl(e);
        return io(t) ? e.lanes = 32 : e.lanes = 536870912, null;
      }
      var d = n.children;
      return n = n.fallback, a ? (El(), a = e.mode, d = $u(
        { mode: "hidden", children: d },
        a
      ), n = Jl(
        n,
        a,
        l,
        null
      ), d.return = e, n.return = e, d.sibling = n, e.child = d, n = e.child, n.memoizedState = Af(l), n.childLanes = Rf(
        t,
        s,
        l
      ), e.memoizedState = Tf, Da(null, n)) : (pl(e), Of(e, d));
    }
    var y = t.memoizedState;
    if (y !== null && (d = y.dehydrated, d !== null)) {
      if (u)
        e.flags & 256 ? (pl(e), e.flags &= -257, e = zf(
          t,
          e,
          l
        )) : e.memoizedState !== null ? (El(), e.child = t.child, e.flags |= 128, e = null) : (El(), d = n.fallback, a = e.mode, n = $u(
          { mode: "visible", children: n.children },
          a
        ), d = Jl(
          d,
          a,
          l,
          null
        ), d.flags |= 2, n.return = e, d.return = e, n.sibling = d, e.child = n, tn(
          e,
          t.child,
          null,
          l
        ), n = e.child, n.memoizedState = Af(l), n.childLanes = Rf(
          t,
          s,
          l
        ), e.memoizedState = Tf, e = Da(null, n));
      else if (pl(e), io(d)) {
        if (s = d.nextSibling && d.nextSibling.dataset, s) var A = s.dgst;
        s = A, n = Error(f(419)), n.stack = "", n.digest = s, Sa({ value: n, source: null, stack: null }), e = zf(
          t,
          e,
          l
        );
      } else if (Xt || Nn(t, e, l, !1), s = (l & t.childLanes) !== 0, Xt || s) {
        if (s = Mt, s !== null && (n = is(s, l), n !== 0 && n !== y.retryLane))
          throw y.retryLane = n, Kl(t, n), fe(s, t, n), pf;
        uo(d) || ii(), e = zf(
          t,
          e,
          l
        );
      } else
        uo(d) ? (e.flags |= 192, e.child = t.child, e = null) : (t = y.treeContext, xt = Ce(
          d.nextSibling
        ), kt = e, yt = !0, hl = null, _e = !1, t !== null && lr(e, t), e = Of(
          e,
          n.children
        ), e.flags |= 4096);
      return e;
    }
    return a ? (El(), d = n.fallback, a = e.mode, y = t.child, A = y.sibling, n = Ke(y, {
      mode: "hidden",
      children: n.children
    }), n.subtreeFlags = y.subtreeFlags & 65011712, A !== null ? d = Ke(
      A,
      d
    ) : (d = Jl(
      d,
      a,
      l,
      null
    ), d.flags |= 2), d.return = e, n.return = e, n.sibling = d, e.child = n, Da(null, n), n = e.child, d = t.child.memoizedState, d === null ? d = Af(l) : (a = d.cachePool, a !== null ? (y = Yt._currentValue, a = a.parent !== y ? { parent: y, pool: y } : a) : a = fr(), d = {
      baseLanes: d.baseLanes | l,
      cachePool: a
    }), n.memoizedState = d, n.childLanes = Rf(
      t,
      s,
      l
    ), e.memoizedState = Tf, Da(t.child, n)) : (pl(e), l = t.child, t = l.sibling, l = Ke(l, {
      mode: "visible",
      children: n.children
    }), l.return = e, l.sibling = null, t !== null && (s = e.deletions, s === null ? (e.deletions = [t], e.flags |= 16) : s.push(t)), e.child = l, e.memoizedState = null, l);
  }
  function Of(t, e) {
    return e = $u(
      { mode: "visible", children: e },
      t.mode
    ), e.return = t, t.child = e;
  }
  function $u(t, e) {
    return t = me(22, t, null, e), t.lanes = 0, t;
  }
  function zf(t, e, l) {
    return tn(e, t.child, null, l), t = Of(
      e,
      e.pendingProps.children
    ), t.flags |= 2, e.memoizedState = null, t;
  }
  function Ed(t, e, l) {
    t.lanes |= e;
    var n = t.alternate;
    n !== null && (n.lanes |= e), jc(t.return, e, l);
  }
  function _f(t, e, l, n, a, u) {
    var s = t.memoizedState;
    s === null ? t.memoizedState = {
      isBackwards: e,
      rendering: null,
      renderingStartTime: 0,
      last: n,
      tail: l,
      tailMode: a,
      treeForkCount: u
    } : (s.isBackwards = e, s.rendering = null, s.renderingStartTime = 0, s.last = n, s.tail = l, s.tailMode = a, s.treeForkCount = u);
  }
  function Td(t, e, l) {
    var n = e.pendingProps, a = n.revealOrder, u = n.tail;
    n = n.children;
    var s = Bt.current, d = (s & 2) !== 0;
    if (d ? (s = s & 1 | 2, e.flags |= 128) : s &= 1, j(Bt, s), $t(t, e, n, l), n = yt ? ba : 0, !d && t !== null && (t.flags & 128) !== 0)
      t: for (t = e.child; t !== null; ) {
        if (t.tag === 13)
          t.memoizedState !== null && Ed(t, l, e);
        else if (t.tag === 19)
          Ed(t, l, e);
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
          t = l.alternate, t !== null && Yu(t) === null && (a = l), l = l.sibling;
        l = a, l === null ? (a = e.child, e.child = null) : (a = l.sibling, l.sibling = null), _f(
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
          if (t = a.alternate, t !== null && Yu(t) === null) {
            e.child = a;
            break;
          }
          t = a.sibling, a.sibling = l, l = a, a = t;
        }
        _f(
          e,
          !0,
          l,
          null,
          u,
          n
        );
        break;
      case "together":
        _f(
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
  function Ie(t, e, l) {
    if (t !== null && (e.dependencies = t.dependencies), Rl |= e.lanes, (l & e.childLanes) === 0)
      if (t !== null) {
        if (Nn(
          t,
          e,
          l,
          !1
        ), (l & e.childLanes) === 0)
          return null;
      } else return null;
    if (t !== null && e.child !== t.child)
      throw Error(f(153));
    if (e.child !== null) {
      for (t = e.child, l = Ke(t, t.pendingProps), e.child = l, l.return = e; t.sibling !== null; )
        t = t.sibling, l = l.sibling = Ke(t, t.pendingProps), l.return = e;
      l.sibling = null;
    }
    return e.child;
  }
  function Mf(t, e) {
    return (t.lanes & e) !== 0 ? !0 : (t = t.dependencies, !!(t !== null && Du(t)));
  }
  function e0(t, e, l) {
    switch (e.tag) {
      case 3:
        Jt(e, e.stateNode.containerInfo), yl(e, Yt, t.memoizedState.cache), Wl();
        break;
      case 27:
      case 5:
        jl(e);
        break;
      case 4:
        Jt(e, e.stateNode.containerInfo);
        break;
      case 10:
        yl(
          e,
          e.type,
          e.memoizedProps.value
        );
        break;
      case 31:
        if (e.memoizedState !== null)
          return e.flags |= 128, Ic(e), null;
        break;
      case 13:
        var n = e.memoizedState;
        if (n !== null)
          return n.dehydrated !== null ? (pl(e), e.flags |= 128, null) : (l & e.child.childLanes) !== 0 ? pd(t, e, l) : (pl(e), t = Ie(
            t,
            e,
            l
          ), t !== null ? t.sibling : null);
        pl(e);
        break;
      case 19:
        var a = (t.flags & 128) !== 0;
        if (n = (l & e.childLanes) !== 0, n || (Nn(
          t,
          e,
          l,
          !1
        ), n = (l & e.childLanes) !== 0), a) {
          if (n)
            return Td(
              t,
              e,
              l
            );
          e.flags |= 128;
        }
        if (a = e.memoizedState, a !== null && (a.rendering = null, a.tail = null, a.lastEffect = null), j(Bt, Bt.current), n) break;
        return null;
      case 22:
        return e.lanes = 0, hd(
          t,
          e,
          l,
          e.pendingProps
        );
      case 24:
        yl(e, Yt, t.memoizedState.cache);
    }
    return Ie(t, e, l);
  }
  function Ad(t, e, l) {
    if (t !== null)
      if (t.memoizedProps !== e.pendingProps)
        Xt = !0;
      else {
        if (!Mf(t, l) && (e.flags & 128) === 0)
          return Xt = !1, e0(
            t,
            e,
            l
          );
        Xt = (t.flags & 131072) !== 0;
      }
    else
      Xt = !1, yt && (e.flags & 1048576) !== 0 && er(e, ba, e.index);
    switch (e.lanes = 0, e.tag) {
      case 16:
        t: {
          var n = e.pendingProps;
          if (t = Il(e.elementType), e.type = t, typeof t == "function")
            Nc(t) ? (n = ln(t, n), e.tag = 1, e = bd(
              null,
              e,
              t,
              n,
              l
            )) : (e.tag = 0, e = Ef(
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
                e.tag = 11, e = rd(
                  null,
                  e,
                  t,
                  n,
                  l
                );
                break t;
              } else if (a === Z) {
                e.tag = 14, e = dd(
                  null,
                  e,
                  t,
                  n,
                  l
                );
                break t;
              }
            }
            throw e = Q(t) || t, Error(f(306, e, ""));
          }
        }
        return e;
      case 0:
        return Ef(
          t,
          e,
          e.type,
          e.pendingProps,
          l
        );
      case 1:
        return n = e.type, a = ln(
          n,
          e.pendingProps
        ), bd(
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
          ), t === null) throw Error(f(387));
          n = e.pendingProps;
          var u = e.memoizedState;
          a = u.element, Jc(t, e), za(e, n, null, l);
          var s = e.memoizedState;
          if (n = s.cache, yl(e, Yt, n), n !== u.cache && Xc(
            e,
            [Yt],
            l,
            !0
          ), Oa(), n = s.element, u.isDehydrated)
            if (u = {
              element: n,
              isDehydrated: !1,
              cache: s.cache
            }, e.updateQueue.baseState = u, e.memoizedState = u, e.flags & 256) {
              e = Sd(
                t,
                e,
                n,
                l
              );
              break t;
            } else if (n !== a) {
              a = Re(
                Error(f(424)),
                e
              ), Sa(a), e = Sd(
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
              for (xt = Ce(t.firstChild), kt = e, yt = !0, hl = null, _e = !0, l = hr(
                e,
                null,
                n,
                l
              ), e.child = l; l; )
                l.flags = l.flags & -3 | 4096, l = l.sibling;
            }
          else {
            if (Wl(), n === a) {
              e = Ie(
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
        return Fu(t, e), t === null ? (l = Um(
          e.type,
          null,
          e.pendingProps,
          null
        )) ? e.memoizedState = l : yt || (l = e.type, t = e.pendingProps, n = mi(
          nt.current
        ).createElement(l), n[Wt] = e, n[le] = t, It(n, l, t), Zt(n), e.stateNode = n) : e.memoizedState = Um(
          e.type,
          t.memoizedProps,
          e.pendingProps,
          t.memoizedState
        ), null;
      case 27:
        return jl(e), t === null && yt && (n = e.stateNode = Dm(
          e.type,
          e.pendingProps,
          nt.current
        ), kt = e, _e = !0, a = xt, Cl(e.type) ? (co = a, xt = Ce(n.firstChild)) : xt = a), $t(
          t,
          e,
          e.pendingProps.children,
          l
        ), Fu(t, e), t === null && (e.flags |= 4194304), e.child;
      case 5:
        return t === null && yt && ((a = n = xt) && (n = D0(
          n,
          e.type,
          e.pendingProps,
          _e
        ), n !== null ? (e.stateNode = n, kt = e, xt = Ce(n.firstChild), _e = !1, a = !0) : a = !1), a || vl(e)), jl(e), a = e.type, u = e.pendingProps, s = t !== null ? t.memoizedProps : null, n = u.children, lo(a, u) ? n = null : s !== null && lo(a, s) && (e.flags |= 32), e.memoizedState !== null && (a = tf(
          t,
          e,
          Ky,
          null,
          null,
          l
        ), Ka._currentValue = a), Fu(t, e), $t(t, e, n, l), e.child;
      case 6:
        return t === null && yt && ((t = l = xt) && (l = N0(
          l,
          e.pendingProps,
          _e
        ), l !== null ? (e.stateNode = l, kt = e, xt = null, t = !0) : t = !1), t || vl(e)), null;
      case 13:
        return pd(t, e, l);
      case 4:
        return Jt(
          e,
          e.stateNode.containerInfo
        ), n = e.pendingProps, t === null ? e.child = tn(
          e,
          null,
          n,
          l
        ) : $t(t, e, n, l), e.child;
      case 11:
        return rd(
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
        return n = e.pendingProps, yl(e, e.type, n.value), $t(t, e, n.children, l), e.child;
      case 9:
        return a = e.type._context, n = e.pendingProps.children, Fl(e), a = Ft(a), n = n(a), e.flags |= 1, $t(t, e, n, l), e.child;
      case 14:
        return dd(
          t,
          e,
          e.type,
          e.pendingProps,
          l
        );
      case 15:
        return md(
          t,
          e,
          e.type,
          e.pendingProps,
          l
        );
      case 19:
        return Td(t, e, l);
      case 31:
        return t0(t, e, l);
      case 22:
        return hd(
          t,
          e,
          l,
          e.pendingProps
        );
      case 24:
        return Fl(e), n = Ft(Yt), t === null ? (a = Vc(), a === null && (a = Mt, u = Gc(), a.pooledCache = u, u.refCount++, u !== null && (a.pooledCacheLanes |= l), a = u), e.memoizedState = { parent: n, cache: a }, Kc(e), yl(e, Yt, a)) : ((t.lanes & l) !== 0 && (Jc(t, e), za(e, null, null, l), Oa()), a = t.memoizedState, u = e.memoizedState, a.parent !== n ? (a = { parent: n, cache: n }, e.memoizedState = a, e.lanes === 0 && (e.memoizedState = e.updateQueue.baseState = a), yl(e, Yt, n)) : (n = u.cache, yl(e, Yt, n), n !== a.cache && Xc(
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
    throw Error(f(156, e.tag));
  }
  function Pe(t) {
    t.flags |= 4;
  }
  function Cf(t, e, l, n, a) {
    if ((e = (t.mode & 32) !== 0) && (e = !1), e) {
      if (t.flags |= 16777216, (a & 335544128) === a)
        if (t.stateNode.complete) t.flags |= 8192;
        else if (Fd()) t.flags |= 8192;
        else
          throw Pl = Hu, Zc;
    } else t.flags &= -16777217;
  }
  function Rd(t, e) {
    if (e.type !== "stylesheet" || (e.state.loading & 4) !== 0)
      t.flags &= -16777217;
    else if (t.flags |= 16777216, !Ym(e))
      if (Fd()) t.flags |= 8192;
      else
        throw Pl = Hu, Zc;
  }
  function Iu(t, e) {
    e !== null && (t.flags |= 4), t.flags & 16384 && (e = t.tag !== 22 ? ns() : 536870912, t.lanes |= e, Vn |= e);
  }
  function Na(t, e) {
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
  function l0(t, e, l) {
    var n = e.pendingProps;
    switch (Bc(e), e.tag) {
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
        return l = e.stateNode, n = null, t !== null && (n = t.memoizedState.cache), e.memoizedState.cache !== n && (e.flags |= 2048), ke(Yt), wt(), l.pendingContext && (l.context = l.pendingContext, l.pendingContext = null), (t === null || t.child === null) && (Dn(e) ? Pe(e) : t === null || t.memoizedState.isDehydrated && (e.flags & 256) === 0 || (e.flags |= 1024, qc())), Dt(e), null;
      case 26:
        var a = e.type, u = e.memoizedState;
        return t === null ? (Pe(e), u !== null ? (Dt(e), Rd(e, u)) : (Dt(e), Cf(
          e,
          a,
          null,
          n,
          l
        ))) : u ? u !== t.memoizedState ? (Pe(e), Dt(e), Rd(e, u)) : (Dt(e), e.flags &= -16777217) : (t = t.memoizedProps, t !== n && Pe(e), Dt(e), Cf(
          e,
          a,
          t,
          n,
          l
        )), null;
      case 27:
        if (hn(e), l = nt.current, a = e.type, t !== null && e.stateNode != null)
          t.memoizedProps !== n && Pe(e);
        else {
          if (!n) {
            if (e.stateNode === null)
              throw Error(f(166));
            return Dt(e), null;
          }
          t = K.current, Dn(e) ? nr(e) : (t = Dm(a, n, l), e.stateNode = t, Pe(e));
        }
        return Dt(e), null;
      case 5:
        if (hn(e), a = e.type, t !== null && e.stateNode != null)
          t.memoizedProps !== n && Pe(e);
        else {
          if (!n) {
            if (e.stateNode === null)
              throw Error(f(166));
            return Dt(e), null;
          }
          if (u = K.current, Dn(e))
            nr(e);
          else {
            var s = mi(
              nt.current
            );
            switch (u) {
              case 1:
                u = s.createElementNS(
                  "http://www.w3.org/2000/svg",
                  a
                );
                break;
              case 2:
                u = s.createElementNS(
                  "http://www.w3.org/1998/Math/MathML",
                  a
                );
                break;
              default:
                switch (a) {
                  case "svg":
                    u = s.createElementNS(
                      "http://www.w3.org/2000/svg",
                      a
                    );
                    break;
                  case "math":
                    u = s.createElementNS(
                      "http://www.w3.org/1998/Math/MathML",
                      a
                    );
                    break;
                  case "script":
                    u = s.createElement("div"), u.innerHTML = "<script><\/script>", u = u.removeChild(
                      u.firstChild
                    );
                    break;
                  case "select":
                    u = typeof n.is == "string" ? s.createElement("select", {
                      is: n.is
                    }) : s.createElement("select"), n.multiple ? u.multiple = !0 : n.size && (u.size = n.size);
                    break;
                  default:
                    u = typeof n.is == "string" ? s.createElement(a, { is: n.is }) : s.createElement(a);
                }
            }
            u[Wt] = e, u[le] = n;
            t: for (s = e.child; s !== null; ) {
              if (s.tag === 5 || s.tag === 6)
                u.appendChild(s.stateNode);
              else if (s.tag !== 4 && s.tag !== 27 && s.child !== null) {
                s.child.return = s, s = s.child;
                continue;
              }
              if (s === e) break t;
              for (; s.sibling === null; ) {
                if (s.return === null || s.return === e)
                  break t;
                s = s.return;
              }
              s.sibling.return = s.return, s = s.sibling;
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
            n && Pe(e);
          }
        }
        return Dt(e), Cf(
          e,
          e.type,
          t === null ? null : t.memoizedProps,
          e.pendingProps,
          l
        ), null;
      case 6:
        if (t && e.stateNode != null)
          t.memoizedProps !== n && Pe(e);
        else {
          if (typeof n != "string" && e.stateNode === null)
            throw Error(f(166));
          if (t = nt.current, Dn(e)) {
            if (t = e.stateNode, l = e.memoizedProps, n = null, a = kt, a !== null)
              switch (a.tag) {
                case 27:
                case 5:
                  n = a.memoizedProps;
              }
            t[Wt] = e, t = !!(t.nodeValue === l || n !== null && n.suppressHydrationWarning === !0 || pm(t.nodeValue, l)), t || vl(e, !0);
          } else
            t = mi(t).createTextNode(
              n
            ), t[Wt] = e, e.stateNode = t;
        }
        return Dt(e), null;
      case 31:
        if (l = e.memoizedState, t === null || t.memoizedState !== null) {
          if (n = Dn(e), l !== null) {
            if (t === null) {
              if (!n) throw Error(f(318));
              if (t = e.memoizedState, t = t !== null ? t.dehydrated : null, !t) throw Error(f(557));
              t[Wt] = e;
            } else
              Wl(), (e.flags & 128) === 0 && (e.memoizedState = null), e.flags |= 4;
            Dt(e), t = !1;
          } else
            l = qc(), t !== null && t.memoizedState !== null && (t.memoizedState.hydrationErrors = l), t = !0;
          if (!t)
            return e.flags & 256 ? (ve(e), e) : (ve(e), null);
          if ((e.flags & 128) !== 0)
            throw Error(f(558));
        }
        return Dt(e), null;
      case 13:
        if (n = e.memoizedState, t === null || t.memoizedState !== null && t.memoizedState.dehydrated !== null) {
          if (a = Dn(e), n !== null && n.dehydrated !== null) {
            if (t === null) {
              if (!a) throw Error(f(318));
              if (a = e.memoizedState, a = a !== null ? a.dehydrated : null, !a) throw Error(f(317));
              a[Wt] = e;
            } else
              Wl(), (e.flags & 128) === 0 && (e.memoizedState = null), e.flags |= 4;
            Dt(e), a = !1;
          } else
            a = qc(), t !== null && t.memoizedState !== null && (t.memoizedState.hydrationErrors = a), a = !0;
          if (!a)
            return e.flags & 256 ? (ve(e), e) : (ve(e), null);
        }
        return ve(e), (e.flags & 128) !== 0 ? (e.lanes = l, e) : (l = n !== null, t = t !== null && t.memoizedState !== null, l && (n = e.child, a = null, n.alternate !== null && n.alternate.memoizedState !== null && n.alternate.memoizedState.cachePool !== null && (a = n.alternate.memoizedState.cachePool.pool), u = null, n.memoizedState !== null && n.memoizedState.cachePool !== null && (u = n.memoizedState.cachePool.pool), u !== a && (n.flags |= 2048)), l !== t && l && (e.child.flags |= 8192), Iu(e, e.updateQueue), Dt(e), null);
      case 4:
        return wt(), t === null && $f(e.stateNode.containerInfo), Dt(e), null;
      case 10:
        return ke(e.type), Dt(e), null;
      case 19:
        if (U(Bt), n = e.memoizedState, n === null) return Dt(e), null;
        if (a = (e.flags & 128) !== 0, u = n.rendering, u === null)
          if (a) Na(n, !1);
          else {
            if (Ht !== 0 || t !== null && (t.flags & 128) !== 0)
              for (t = e.child; t !== null; ) {
                if (u = Yu(t), u !== null) {
                  for (e.flags |= 128, Na(n, !1), t = u.updateQueue, e.updateQueue = t, Iu(e, t), e.subtreeFlags = 0, t = l, l = e.child; l !== null; )
                    Is(l, t), l = l.sibling;
                  return j(
                    Bt,
                    Bt.current & 1 | 2
                  ), yt && Je(e, n.treeForkCount), e.child;
                }
                t = t.sibling;
              }
            n.tail !== null && oe() > ni && (e.flags |= 128, a = !0, Na(n, !1), e.lanes = 4194304);
          }
        else {
          if (!a)
            if (t = Yu(u), t !== null) {
              if (e.flags |= 128, a = !0, t = t.updateQueue, e.updateQueue = t, Iu(e, t), Na(n, !0), n.tail === null && n.tailMode === "hidden" && !u.alternate && !yt)
                return Dt(e), null;
            } else
              2 * oe() - n.renderingStartTime > ni && l !== 536870912 && (e.flags |= 128, a = !0, Na(n, !1), e.lanes = 4194304);
          n.isBackwards ? (u.sibling = e.child, e.child = u) : (t = n.last, t !== null ? t.sibling = u : e.child = u, n.last = u);
        }
        return n.tail !== null ? (t = n.tail, n.rendering = t, n.tail = t.sibling, n.renderingStartTime = oe(), t.sibling = null, l = Bt.current, j(
          Bt,
          a ? l & 1 | 2 : l & 1
        ), yt && Je(e, n.treeForkCount), t) : (Dt(e), null);
      case 22:
      case 23:
        return ve(e), $c(), n = e.memoizedState !== null, t !== null ? t.memoizedState !== null !== n && (e.flags |= 8192) : n && (e.flags |= 8192), n ? (l & 536870912) !== 0 && (e.flags & 128) === 0 && (Dt(e), e.subtreeFlags & 6 && (e.flags |= 8192)) : Dt(e), l = e.updateQueue, l !== null && Iu(e, l.retryQueue), l = null, t !== null && t.memoizedState !== null && t.memoizedState.cachePool !== null && (l = t.memoizedState.cachePool.pool), n = null, e.memoizedState !== null && e.memoizedState.cachePool !== null && (n = e.memoizedState.cachePool.pool), n !== l && (e.flags |= 2048), t !== null && U($l), null;
      case 24:
        return l = null, t !== null && (l = t.memoizedState.cache), e.memoizedState.cache !== l && (e.flags |= 2048), ke(Yt), Dt(e), null;
      case 25:
        return null;
      case 30:
        return null;
    }
    throw Error(f(156, e.tag));
  }
  function n0(t, e) {
    switch (Bc(e), e.tag) {
      case 1:
        return t = e.flags, t & 65536 ? (e.flags = t & -65537 | 128, e) : null;
      case 3:
        return ke(Yt), wt(), t = e.flags, (t & 65536) !== 0 && (t & 128) === 0 ? (e.flags = t & -65537 | 128, e) : null;
      case 26:
      case 27:
      case 5:
        return hn(e), null;
      case 31:
        if (e.memoizedState !== null) {
          if (ve(e), e.alternate === null)
            throw Error(f(340));
          Wl();
        }
        return t = e.flags, t & 65536 ? (e.flags = t & -65537 | 128, e) : null;
      case 13:
        if (ve(e), t = e.memoizedState, t !== null && t.dehydrated !== null) {
          if (e.alternate === null)
            throw Error(f(340));
          Wl();
        }
        return t = e.flags, t & 65536 ? (e.flags = t & -65537 | 128, e) : null;
      case 19:
        return U(Bt), null;
      case 4:
        return wt(), null;
      case 10:
        return ke(e.type), null;
      case 22:
      case 23:
        return ve(e), $c(), t !== null && U($l), t = e.flags, t & 65536 ? (e.flags = t & -65537 | 128, e) : null;
      case 24:
        return ke(Yt), null;
      case 25:
        return null;
      default:
        return null;
    }
  }
  function Od(t, e) {
    switch (Bc(e), e.tag) {
      case 3:
        ke(Yt), wt();
        break;
      case 26:
      case 27:
      case 5:
        hn(e);
        break;
      case 4:
        wt();
        break;
      case 31:
        e.memoizedState !== null && ve(e);
        break;
      case 13:
        ve(e);
        break;
      case 19:
        U(Bt);
        break;
      case 10:
        ke(e.type);
        break;
      case 22:
      case 23:
        ve(e), $c(), t !== null && U($l);
        break;
      case 24:
        ke(Yt);
    }
  }
  function wa(t, e) {
    try {
      var l = e.updateQueue, n = l !== null ? l.lastEffect : null;
      if (n !== null) {
        var a = n.next;
        l = a;
        do {
          if ((l.tag & t) === t) {
            n = void 0;
            var u = l.create, s = l.inst;
            n = u(), s.destroy = n;
          }
          l = l.next;
        } while (l !== a);
      }
    } catch (d) {
      Et(e, e.return, d);
    }
  }
  function Tl(t, e, l) {
    try {
      var n = e.updateQueue, a = n !== null ? n.lastEffect : null;
      if (a !== null) {
        var u = a.next;
        n = u;
        do {
          if ((n.tag & t) === t) {
            var s = n.inst, d = s.destroy;
            if (d !== void 0) {
              s.destroy = void 0, a = e;
              var y = l, A = d;
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
  function zd(t) {
    var e = t.updateQueue;
    if (e !== null) {
      var l = t.stateNode;
      try {
        yr(e, l);
      } catch (n) {
        Et(t, t.return, n);
      }
    }
  }
  function _d(t, e, l) {
    l.props = ln(
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
  function Be(t, e) {
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
  function Md(t) {
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
  function xf(t, e, l) {
    try {
      var n = t.stateNode;
      O0(n, t.type, l, e), n[le] = e;
    } catch (a) {
      Et(t, t.return, a);
    }
  }
  function Cd(t) {
    return t.tag === 5 || t.tag === 3 || t.tag === 26 || t.tag === 27 && Cl(t.type) || t.tag === 4;
  }
  function Df(t) {
    t: for (; ; ) {
      for (; t.sibling === null; ) {
        if (t.return === null || Cd(t.return)) return null;
        t = t.return;
      }
      for (t.sibling.return = t.return, t = t.sibling; t.tag !== 5 && t.tag !== 6 && t.tag !== 18; ) {
        if (t.tag === 27 && Cl(t.type) || t.flags & 2 || t.child === null || t.tag === 4) continue t;
        t.child.return = t, t = t.child;
      }
      if (!(t.flags & 2)) return t.stateNode;
    }
  }
  function Nf(t, e, l) {
    var n = t.tag;
    if (n === 5 || n === 6)
      t = t.stateNode, e ? (l.nodeType === 9 ? l.body : l.nodeName === "HTML" ? l.ownerDocument.body : l).insertBefore(t, e) : (e = l.nodeType === 9 ? l.body : l.nodeName === "HTML" ? l.ownerDocument.body : l, e.appendChild(t), l = l._reactRootContainer, l != null || e.onclick !== null || (e.onclick = Ve));
    else if (n !== 4 && (n === 27 && Cl(t.type) && (l = t.stateNode, e = null), t = t.child, t !== null))
      for (Nf(t, e, l), t = t.sibling; t !== null; )
        Nf(t, e, l), t = t.sibling;
  }
  function Pu(t, e, l) {
    var n = t.tag;
    if (n === 5 || n === 6)
      t = t.stateNode, e ? l.insertBefore(t, e) : l.appendChild(t);
    else if (n !== 4 && (n === 27 && Cl(t.type) && (l = t.stateNode), t = t.child, t !== null))
      for (Pu(t, e, l), t = t.sibling; t !== null; )
        Pu(t, e, l), t = t.sibling;
  }
  function xd(t) {
    var e = t.stateNode, l = t.memoizedProps;
    try {
      for (var n = t.type, a = e.attributes; a.length; )
        e.removeAttributeNode(a[0]);
      It(e, n, l), e[Wt] = t, e[le] = l;
    } catch (u) {
      Et(t, t.return, u);
    }
  }
  var tl = !1, Gt = !1, wf = !1, Dd = typeof WeakSet == "function" ? WeakSet : Set, Kt = null;
  function a0(t, e) {
    if (t = t.containerInfo, to = pi, t = Qs(t), Oc(t)) {
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
            var s = 0, d = -1, y = -1, A = 0, C = 0, w = t, O = null;
            e: for (; ; ) {
              for (var _; w !== l || a !== 0 && w.nodeType !== 3 || (d = s + a), w !== u || n !== 0 && w.nodeType !== 3 || (y = s + n), w.nodeType === 3 && (s += w.nodeValue.length), (_ = w.firstChild) !== null; )
                O = w, w = _;
              for (; ; ) {
                if (w === t) break e;
                if (O === l && ++A === a && (d = s), O === u && ++C === n && (y = s), (_ = w.nextSibling) !== null) break;
                w = O, O = w.parentNode;
              }
              w = _;
            }
            l = d === -1 || y === -1 ? null : { start: d, end: y };
          } else l = null;
        }
      l = l || { start: 0, end: 0 };
    } else l = null;
    for (eo = { focusedElem: t, selectionRange: l }, pi = !1, Kt = e; Kt !== null; )
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
                  var J = ln(
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
                  ao(t);
                else if (l === 1)
                  switch (t.nodeName) {
                    case "HEAD":
                    case "HTML":
                    case "BODY":
                      ao(t);
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
              if ((t & 1024) !== 0) throw Error(f(163));
          }
          if (t = e.sibling, t !== null) {
            t.return = e.return, Kt = t;
            break;
          }
          Kt = e.return;
        }
  }
  function Nd(t, e, l) {
    var n = l.flags;
    switch (l.tag) {
      case 0:
      case 11:
      case 15:
        ll(t, l), n & 4 && wa(5, l);
        break;
      case 1:
        if (ll(t, l), n & 4)
          if (t = l.stateNode, e === null)
            try {
              t.componentDidMount();
            } catch (s) {
              Et(l, l.return, s);
            }
          else {
            var a = ln(
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
            } catch (s) {
              Et(
                l,
                l.return,
                s
              );
            }
          }
        n & 64 && zd(l), n & 512 && Ua(l, l.return);
        break;
      case 3:
        if (ll(t, l), n & 64 && (t = l.updateQueue, t !== null)) {
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
            yr(t, e);
          } catch (s) {
            Et(l, l.return, s);
          }
        }
        break;
      case 27:
        e === null && n & 4 && xd(l);
      case 26:
      case 5:
        ll(t, l), e === null && n & 4 && Md(l), n & 512 && Ua(l, l.return);
        break;
      case 12:
        ll(t, l);
        break;
      case 31:
        ll(t, l), n & 4 && Hd(t, l);
        break;
      case 13:
        ll(t, l), n & 4 && Bd(t, l), n & 64 && (t = l.memoizedState, t !== null && (t = t.dehydrated, t !== null && (l = m0.bind(
          null,
          l
        ), w0(t, l))));
        break;
      case 22:
        if (n = l.memoizedState !== null || tl, !n) {
          e = e !== null && e.memoizedState !== null || Gt, a = tl;
          var u = Gt;
          tl = n, (Gt = e) && !u ? nl(
            t,
            l,
            (l.subtreeFlags & 8772) !== 0
          ) : ll(t, l), tl = a, Gt = u;
        }
        break;
      case 30:
        break;
      default:
        ll(t, l);
    }
  }
  function wd(t) {
    var e = t.alternate;
    e !== null && (t.alternate = null, wd(e)), t.child = null, t.deletions = null, t.sibling = null, t.tag === 5 && (e = t.stateNode, e !== null && fc(e)), t.stateNode = null, t.return = null, t.dependencies = null, t.memoizedProps = null, t.memoizedState = null, t.pendingProps = null, t.stateNode = null, t.updateQueue = null;
  }
  var Nt = null, ae = !1;
  function el(t, e, l) {
    for (l = l.child; l !== null; )
      Ud(t, e, l), l = l.sibling;
  }
  function Ud(t, e, l) {
    if (se && typeof se.onCommitFiberUnmount == "function")
      try {
        se.onCommitFiberUnmount(aa, l);
      } catch {
      }
    switch (l.tag) {
      case 26:
        Gt || Be(l, e), el(
          t,
          e,
          l
        ), l.memoizedState ? l.memoizedState.count-- : l.stateNode && (l = l.stateNode, l.parentNode.removeChild(l));
        break;
      case 27:
        Gt || Be(l, e);
        var n = Nt, a = ae;
        Cl(l.type) && (Nt = l.stateNode, ae = !1), el(
          t,
          e,
          l
        ), Qa(l.stateNode), Nt = n, ae = a;
        break;
      case 5:
        Gt || Be(l, e);
      case 6:
        if (n = Nt, a = ae, Nt = null, el(
          t,
          e,
          l
        ), Nt = n, ae = a, Nt !== null)
          if (ae)
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
        Nt !== null && (ae ? (t = Nt, zm(
          t.nodeType === 9 ? t.body : t.nodeName === "HTML" ? t.ownerDocument.body : t,
          l.stateNode
        ), In(t)) : zm(Nt, l.stateNode));
        break;
      case 4:
        n = Nt, a = ae, Nt = l.stateNode.containerInfo, ae = !0, el(
          t,
          e,
          l
        ), Nt = n, ae = a;
        break;
      case 0:
      case 11:
      case 14:
      case 15:
        Tl(2, l, e), Gt || Tl(4, l, e), el(
          t,
          e,
          l
        );
        break;
      case 1:
        Gt || (Be(l, e), n = l.stateNode, typeof n.componentWillUnmount == "function" && _d(
          l,
          e,
          n
        )), el(
          t,
          e,
          l
        );
        break;
      case 21:
        el(
          t,
          e,
          l
        );
        break;
      case 22:
        Gt = (n = Gt) || l.memoizedState !== null, el(
          t,
          e,
          l
        ), Gt = n;
        break;
      default:
        el(
          t,
          e,
          l
        );
    }
  }
  function Hd(t, e) {
    if (e.memoizedState === null && (t = e.alternate, t !== null && (t = t.memoizedState, t !== null))) {
      t = t.dehydrated;
      try {
        In(t);
      } catch (l) {
        Et(e, e.return, l);
      }
    }
  }
  function Bd(t, e) {
    if (e.memoizedState === null && (t = e.alternate, t !== null && (t = t.memoizedState, t !== null && (t = t.dehydrated, t !== null))))
      try {
        In(t);
      } catch (l) {
        Et(e, e.return, l);
      }
  }
  function u0(t) {
    switch (t.tag) {
      case 31:
      case 13:
      case 19:
        var e = t.stateNode;
        return e === null && (e = t.stateNode = new Dd()), e;
      case 22:
        return t = t.stateNode, e = t._retryCache, e === null && (e = t._retryCache = new Dd()), e;
      default:
        throw Error(f(435, t.tag));
    }
  }
  function ti(t, e) {
    var l = u0(t);
    e.forEach(function(n) {
      if (!l.has(n)) {
        l.add(n);
        var a = h0.bind(null, t, n);
        n.then(a, a);
      }
    });
  }
  function ue(t, e) {
    var l = e.deletions;
    if (l !== null)
      for (var n = 0; n < l.length; n++) {
        var a = l[n], u = t, s = e, d = s;
        t: for (; d !== null; ) {
          switch (d.tag) {
            case 27:
              if (Cl(d.type)) {
                Nt = d.stateNode, ae = !1;
                break t;
              }
              break;
            case 5:
              Nt = d.stateNode, ae = !1;
              break t;
            case 3:
            case 4:
              Nt = d.stateNode.containerInfo, ae = !0;
              break t;
          }
          d = d.return;
        }
        if (Nt === null) throw Error(f(160));
        Ud(u, s, a), Nt = null, ae = !1, u = a.alternate, u !== null && (u.return = null), a.return = null;
      }
    if (e.subtreeFlags & 13886)
      for (e = e.child; e !== null; )
        Ld(e, t), e = e.sibling;
  }
  var Ne = null;
  function Ld(t, e) {
    var l = t.alternate, n = t.flags;
    switch (t.tag) {
      case 0:
      case 11:
      case 14:
      case 15:
        ue(e, t), ie(t), n & 4 && (Tl(3, t, t.return), wa(3, t), Tl(5, t, t.return));
        break;
      case 1:
        ue(e, t), ie(t), n & 512 && (Gt || l === null || Be(l, l.return)), n & 64 && tl && (t = t.updateQueue, t !== null && (n = t.callbacks, n !== null && (l = t.shared.hiddenCallbacks, t.shared.hiddenCallbacks = l === null ? n : l.concat(n))));
        break;
      case 26:
        var a = Ne;
        if (ue(e, t), ie(t), n & 512 && (Gt || l === null || Be(l, l.return)), n & 4) {
          var u = l !== null ? l.memoizedState : null;
          if (n = t.memoizedState, l === null)
            if (n === null)
              if (t.stateNode === null) {
                t: {
                  n = t.type, l = t.memoizedProps, a = a.ownerDocument || a;
                  e: switch (n) {
                    case "title":
                      u = a.getElementsByTagName("title")[0], (!u || u[ca] || u[Wt] || u.namespaceURI === "http://www.w3.org/2000/svg" || u.hasAttribute("itemprop")) && (u = a.createElement(n), a.head.insertBefore(
                        u,
                        a.querySelector("head > title")
                      )), It(u, n, l), u[Wt] = t, Zt(u), n = u;
                      break t;
                    case "link":
                      var s = Lm(
                        "link",
                        "href",
                        a
                      ).get(n + (l.href || ""));
                      if (s) {
                        for (var d = 0; d < s.length; d++)
                          if (u = s[d], u.getAttribute("href") === (l.href == null || l.href === "" ? null : l.href) && u.getAttribute("rel") === (l.rel == null ? null : l.rel) && u.getAttribute("title") === (l.title == null ? null : l.title) && u.getAttribute("crossorigin") === (l.crossOrigin == null ? null : l.crossOrigin)) {
                            s.splice(d, 1);
                            break e;
                          }
                      }
                      u = a.createElement(n), It(u, n, l), a.head.appendChild(u);
                      break;
                    case "meta":
                      if (s = Lm(
                        "meta",
                        "content",
                        a
                      ).get(n + (l.content || ""))) {
                        for (d = 0; d < s.length; d++)
                          if (u = s[d], u.getAttribute("content") === (l.content == null ? null : "" + l.content) && u.getAttribute("name") === (l.name == null ? null : l.name) && u.getAttribute("property") === (l.property == null ? null : l.property) && u.getAttribute("http-equiv") === (l.httpEquiv == null ? null : l.httpEquiv) && u.getAttribute("charset") === (l.charSet == null ? null : l.charSet)) {
                            s.splice(d, 1);
                            break e;
                          }
                      }
                      u = a.createElement(n), It(u, n, l), a.head.appendChild(u);
                      break;
                    default:
                      throw Error(f(468, n));
                  }
                  u[Wt] = t, Zt(u), n = u;
                }
                t.stateNode = n;
              } else
                qm(
                  a,
                  t.type,
                  t.stateNode
                );
            else
              t.stateNode = Bm(
                a,
                n,
                t.memoizedProps
              );
          else
            u !== n ? (u === null ? l.stateNode !== null && (l = l.stateNode, l.parentNode.removeChild(l)) : u.count--, n === null ? qm(
              a,
              t.type,
              t.stateNode
            ) : Bm(
              a,
              n,
              t.memoizedProps
            )) : n === null && t.stateNode !== null && xf(
              t,
              t.memoizedProps,
              l.memoizedProps
            );
        }
        break;
      case 27:
        ue(e, t), ie(t), n & 512 && (Gt || l === null || Be(l, l.return)), l !== null && n & 4 && xf(
          t,
          t.memoizedProps,
          l.memoizedProps
        );
        break;
      case 5:
        if (ue(e, t), ie(t), n & 512 && (Gt || l === null || Be(l, l.return)), t.flags & 32) {
          a = t.stateNode;
          try {
            En(a, "");
          } catch (J) {
            Et(t, t.return, J);
          }
        }
        n & 4 && t.stateNode != null && (a = t.memoizedProps, xf(
          t,
          a,
          l !== null ? l.memoizedProps : a
        )), n & 1024 && (wf = !0);
        break;
      case 6:
        if (ue(e, t), ie(t), n & 4) {
          if (t.stateNode === null)
            throw Error(f(162));
          n = t.memoizedProps, l = t.stateNode;
          try {
            l.nodeValue = n;
          } catch (J) {
            Et(t, t.return, J);
          }
        }
        break;
      case 3:
        if (yi = null, a = Ne, Ne = hi(e.containerInfo), ue(e, t), Ne = a, ie(t), n & 4 && l !== null && l.memoizedState.isDehydrated)
          try {
            In(e.containerInfo);
          } catch (J) {
            Et(t, t.return, J);
          }
        wf && (wf = !1, qd(t));
        break;
      case 4:
        n = Ne, Ne = hi(
          t.stateNode.containerInfo
        ), ue(e, t), ie(t), Ne = n;
        break;
      case 12:
        ue(e, t), ie(t);
        break;
      case 31:
        ue(e, t), ie(t), n & 4 && (n = t.updateQueue, n !== null && (t.updateQueue = null, ti(t, n)));
        break;
      case 13:
        ue(e, t), ie(t), t.child.flags & 8192 && t.memoizedState !== null != (l !== null && l.memoizedState !== null) && (li = oe()), n & 4 && (n = t.updateQueue, n !== null && (t.updateQueue = null, ti(t, n)));
        break;
      case 22:
        a = t.memoizedState !== null;
        var y = l !== null && l.memoizedState !== null, A = tl, C = Gt;
        if (tl = A || a, Gt = C || y, ue(e, t), Gt = C, tl = A, ie(t), n & 8192)
          t: for (e = t.stateNode, e._visibility = a ? e._visibility & -2 : e._visibility | 1, a && (l === null || y || tl || Gt || nn(t)), l = null, e = t; ; ) {
            if (e.tag === 5 || e.tag === 26) {
              if (l === null) {
                y = l = e;
                try {
                  if (u = y.stateNode, a)
                    s = u.style, typeof s.setProperty == "function" ? s.setProperty("display", "none", "important") : s.display = "none";
                  else {
                    d = y.stateNode;
                    var w = y.memoizedProps.style, O = w != null && w.hasOwnProperty("display") ? w.display : null;
                    d.style.display = O == null || typeof O == "boolean" ? "" : ("" + O).trim();
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
                  a ? _m(_, !0) : _m(y.stateNode, !1);
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
        n & 4 && (n = t.updateQueue, n !== null && (l = n.retryQueue, l !== null && (n.retryQueue = null, ti(t, l))));
        break;
      case 19:
        ue(e, t), ie(t), n & 4 && (n = t.updateQueue, n !== null && (t.updateQueue = null, ti(t, n)));
        break;
      case 30:
        break;
      case 21:
        break;
      default:
        ue(e, t), ie(t);
    }
  }
  function ie(t) {
    var e = t.flags;
    if (e & 2) {
      try {
        for (var l, n = t.return; n !== null; ) {
          if (Cd(n)) {
            l = n;
            break;
          }
          n = n.return;
        }
        if (l == null) throw Error(f(160));
        switch (l.tag) {
          case 27:
            var a = l.stateNode, u = Df(t);
            Pu(t, u, a);
            break;
          case 5:
            var s = l.stateNode;
            l.flags & 32 && (En(s, ""), l.flags &= -33);
            var d = Df(t);
            Pu(t, d, s);
            break;
          case 3:
          case 4:
            var y = l.stateNode.containerInfo, A = Df(t);
            Nf(
              t,
              A,
              y
            );
            break;
          default:
            throw Error(f(161));
        }
      } catch (C) {
        Et(t, t.return, C);
      }
      t.flags &= -3;
    }
    e & 4096 && (t.flags &= -4097);
  }
  function qd(t) {
    if (t.subtreeFlags & 1024)
      for (t = t.child; t !== null; ) {
        var e = t;
        qd(e), e.tag === 5 && e.flags & 1024 && e.stateNode.reset(), t = t.sibling;
      }
  }
  function ll(t, e) {
    if (e.subtreeFlags & 8772)
      for (e = e.child; e !== null; )
        Nd(t, e.alternate, e), e = e.sibling;
  }
  function nn(t) {
    for (t = t.child; t !== null; ) {
      var e = t;
      switch (e.tag) {
        case 0:
        case 11:
        case 14:
        case 15:
          Tl(4, e, e.return), nn(e);
          break;
        case 1:
          Be(e, e.return);
          var l = e.stateNode;
          typeof l.componentWillUnmount == "function" && _d(
            e,
            e.return,
            l
          ), nn(e);
          break;
        case 27:
          Qa(e.stateNode);
        case 26:
        case 5:
          Be(e, e.return), nn(e);
          break;
        case 22:
          e.memoizedState === null && nn(e);
          break;
        case 30:
          nn(e);
          break;
        default:
          nn(e);
      }
      t = t.sibling;
    }
  }
  function nl(t, e, l) {
    for (l = l && (e.subtreeFlags & 8772) !== 0, e = e.child; e !== null; ) {
      var n = e.alternate, a = t, u = e, s = u.flags;
      switch (u.tag) {
        case 0:
        case 11:
        case 15:
          nl(
            a,
            u,
            l
          ), wa(4, u);
          break;
        case 1:
          if (nl(
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
            var d = n.stateNode;
            try {
              var y = a.shared.hiddenCallbacks;
              if (y !== null)
                for (a.shared.hiddenCallbacks = null, a = 0; a < y.length; a++)
                  vr(y[a], d);
            } catch (A) {
              Et(n, n.return, A);
            }
          }
          l && s & 64 && zd(u), Ua(u, u.return);
          break;
        case 27:
          xd(u);
        case 26:
        case 5:
          nl(
            a,
            u,
            l
          ), l && n === null && s & 4 && Md(u), Ua(u, u.return);
          break;
        case 12:
          nl(
            a,
            u,
            l
          );
          break;
        case 31:
          nl(
            a,
            u,
            l
          ), l && s & 4 && Hd(a, u);
          break;
        case 13:
          nl(
            a,
            u,
            l
          ), l && s & 4 && Bd(a, u);
          break;
        case 22:
          u.memoizedState === null && nl(
            a,
            u,
            l
          ), Ua(u, u.return);
          break;
        case 30:
          break;
        default:
          nl(
            a,
            u,
            l
          );
      }
      e = e.sibling;
    }
  }
  function Uf(t, e) {
    var l = null;
    t !== null && t.memoizedState !== null && t.memoizedState.cachePool !== null && (l = t.memoizedState.cachePool.pool), t = null, e.memoizedState !== null && e.memoizedState.cachePool !== null && (t = e.memoizedState.cachePool.pool), t !== l && (t != null && t.refCount++, l != null && pa(l));
  }
  function Hf(t, e) {
    t = null, e.alternate !== null && (t = e.alternate.memoizedState.cache), e = e.memoizedState.cache, e !== t && (e.refCount++, t != null && pa(t));
  }
  function we(t, e, l, n) {
    if (e.subtreeFlags & 10256)
      for (e = e.child; e !== null; )
        Yd(
          t,
          e,
          l,
          n
        ), e = e.sibling;
  }
  function Yd(t, e, l, n) {
    var a = e.flags;
    switch (e.tag) {
      case 0:
      case 11:
      case 15:
        we(
          t,
          e,
          l,
          n
        ), a & 2048 && wa(9, e);
        break;
      case 1:
        we(
          t,
          e,
          l,
          n
        );
        break;
      case 3:
        we(
          t,
          e,
          l,
          n
        ), a & 2048 && (t = null, e.alternate !== null && (t = e.alternate.memoizedState.cache), e = e.memoizedState.cache, e !== t && (e.refCount++, t != null && pa(t)));
        break;
      case 12:
        if (a & 2048) {
          we(
            t,
            e,
            l,
            n
          ), t = e.stateNode;
          try {
            var u = e.memoizedProps, s = u.id, d = u.onPostCommit;
            typeof d == "function" && d(
              s,
              e.alternate === null ? "mount" : "update",
              t.passiveEffectDuration,
              -0
            );
          } catch (y) {
            Et(e, e.return, y);
          }
        } else
          we(
            t,
            e,
            l,
            n
          );
        break;
      case 31:
        we(
          t,
          e,
          l,
          n
        );
        break;
      case 13:
        we(
          t,
          e,
          l,
          n
        );
        break;
      case 23:
        break;
      case 22:
        u = e.stateNode, s = e.alternate, e.memoizedState !== null ? u._visibility & 2 ? we(
          t,
          e,
          l,
          n
        ) : Ha(t, e) : u._visibility & 2 ? we(
          t,
          e,
          l,
          n
        ) : (u._visibility |= 2, Xn(
          t,
          e,
          l,
          n,
          (e.subtreeFlags & 10256) !== 0 || !1
        )), a & 2048 && Uf(s, e);
        break;
      case 24:
        we(
          t,
          e,
          l,
          n
        ), a & 2048 && Hf(e.alternate, e);
        break;
      default:
        we(
          t,
          e,
          l,
          n
        );
    }
  }
  function Xn(t, e, l, n, a) {
    for (a = a && ((e.subtreeFlags & 10256) !== 0 || !1), e = e.child; e !== null; ) {
      var u = t, s = e, d = l, y = n, A = s.flags;
      switch (s.tag) {
        case 0:
        case 11:
        case 15:
          Xn(
            u,
            s,
            d,
            y,
            a
          ), wa(8, s);
          break;
        case 23:
          break;
        case 22:
          var C = s.stateNode;
          s.memoizedState !== null ? C._visibility & 2 ? Xn(
            u,
            s,
            d,
            y,
            a
          ) : Ha(
            u,
            s
          ) : (C._visibility |= 2, Xn(
            u,
            s,
            d,
            y,
            a
          )), a && A & 2048 && Uf(
            s.alternate,
            s
          );
          break;
        case 24:
          Xn(
            u,
            s,
            d,
            y,
            a
          ), a && A & 2048 && Hf(s.alternate, s);
          break;
        default:
          Xn(
            u,
            s,
            d,
            y,
            a
          );
      }
      e = e.sibling;
    }
  }
  function Ha(t, e) {
    if (e.subtreeFlags & 10256)
      for (e = e.child; e !== null; ) {
        var l = t, n = e, a = n.flags;
        switch (n.tag) {
          case 22:
            Ha(l, n), a & 2048 && Uf(
              n.alternate,
              n
            );
            break;
          case 24:
            Ha(l, n), a & 2048 && Hf(n.alternate, n);
            break;
          default:
            Ha(l, n);
        }
        e = e.sibling;
      }
  }
  var Ba = 8192;
  function Gn(t, e, l) {
    if (t.subtreeFlags & Ba)
      for (t = t.child; t !== null; )
        jd(
          t,
          e,
          l
        ), t = t.sibling;
  }
  function jd(t, e, l) {
    switch (t.tag) {
      case 26:
        Gn(
          t,
          e,
          l
        ), t.flags & Ba && t.memoizedState !== null && Z0(
          l,
          Ne,
          t.memoizedState,
          t.memoizedProps
        );
        break;
      case 5:
        Gn(
          t,
          e,
          l
        );
        break;
      case 3:
      case 4:
        var n = Ne;
        Ne = hi(t.stateNode.containerInfo), Gn(
          t,
          e,
          l
        ), Ne = n;
        break;
      case 22:
        t.memoizedState === null && (n = t.alternate, n !== null && n.memoizedState !== null ? (n = Ba, Ba = 16777216, Gn(
          t,
          e,
          l
        ), Ba = n) : Gn(
          t,
          e,
          l
        ));
        break;
      default:
        Gn(
          t,
          e,
          l
        );
    }
  }
  function Xd(t) {
    var e = t.alternate;
    if (e !== null && (t = e.child, t !== null)) {
      e.child = null;
      do
        e = t.sibling, t.sibling = null, t = e;
      while (t !== null);
    }
  }
  function La(t) {
    var e = t.deletions;
    if ((t.flags & 16) !== 0) {
      if (e !== null)
        for (var l = 0; l < e.length; l++) {
          var n = e[l];
          Kt = n, Qd(
            n,
            t
          );
        }
      Xd(t);
    }
    if (t.subtreeFlags & 10256)
      for (t = t.child; t !== null; )
        Gd(t), t = t.sibling;
  }
  function Gd(t) {
    switch (t.tag) {
      case 0:
      case 11:
      case 15:
        La(t), t.flags & 2048 && Tl(9, t, t.return);
        break;
      case 3:
        La(t);
        break;
      case 12:
        La(t);
        break;
      case 22:
        var e = t.stateNode;
        t.memoizedState !== null && e._visibility & 2 && (t.return === null || t.return.tag !== 13) ? (e._visibility &= -3, ei(t)) : La(t);
        break;
      default:
        La(t);
    }
  }
  function ei(t) {
    var e = t.deletions;
    if ((t.flags & 16) !== 0) {
      if (e !== null)
        for (var l = 0; l < e.length; l++) {
          var n = e[l];
          Kt = n, Qd(
            n,
            t
          );
        }
      Xd(t);
    }
    for (t = t.child; t !== null; ) {
      switch (e = t, e.tag) {
        case 0:
        case 11:
        case 15:
          Tl(8, e, e.return), ei(e);
          break;
        case 22:
          l = e.stateNode, l._visibility & 2 && (l._visibility &= -3, ei(e));
          break;
        default:
          ei(e);
      }
      t = t.sibling;
    }
  }
  function Qd(t, e) {
    for (; Kt !== null; ) {
      var l = Kt;
      switch (l.tag) {
        case 0:
        case 11:
        case 15:
          Tl(8, l, e);
          break;
        case 23:
        case 22:
          if (l.memoizedState !== null && l.memoizedState.cachePool !== null) {
            var n = l.memoizedState.cachePool.pool;
            n != null && n.refCount++;
          }
          break;
        case 24:
          pa(l.memoizedState.cache);
      }
      if (n = l.child, n !== null) n.return = l, Kt = n;
      else
        t: for (l = t; Kt !== null; ) {
          n = Kt;
          var a = n.sibling, u = n.return;
          if (wd(n), n === l) {
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
  var i0 = {
    getCacheForType: function(t) {
      var e = Ft(Yt), l = e.data.get(t);
      return l === void 0 && (l = t(), e.data.set(t, l)), l;
    },
    cacheSignal: function() {
      return Ft(Yt).controller.signal;
    }
  }, c0 = typeof WeakMap == "function" ? WeakMap : Map, St = 0, Mt = null, st = null, ht = 0, pt = 0, ye = null, Al = !1, Qn = !1, Bf = !1, al = 0, Ht = 0, Rl = 0, an = 0, Lf = 0, ge = 0, Vn = 0, qa = null, ce = null, qf = !1, li = 0, Vd = 0, ni = 1 / 0, ai = null, Ol = null, Qt = 0, zl = null, Zn = null, ul = 0, Yf = 0, jf = null, Zd = null, Ya = 0, Xf = null;
  function be() {
    return (St & 2) !== 0 && ht !== 0 ? ht & -ht : R.T !== null ? Jf() : cs();
  }
  function Kd() {
    if (ge === 0)
      if ((ht & 536870912) === 0 || yt) {
        var t = du;
        du <<= 1, (du & 3932160) === 0 && (du = 262144), ge = t;
      } else ge = 536870912;
    return t = he.current, t !== null && (t.flags |= 32), ge;
  }
  function fe(t, e, l) {
    (t === Mt && (pt === 2 || pt === 9) || t.cancelPendingCommit !== null) && (Kn(t, 0), _l(
      t,
      ht,
      ge,
      !1
    )), ia(t, l), ((St & 2) === 0 || t !== Mt) && (t === Mt && ((St & 2) === 0 && (an |= l), Ht === 4 && _l(
      t,
      ht,
      ge,
      !1
    )), Le(t));
  }
  function Jd(t, e, l) {
    if ((St & 6) !== 0) throw Error(f(327));
    var n = !l && (e & 127) === 0 && (e & t.expiredLanes) === 0 || ua(t, e), a = n ? s0(t, e) : Qf(t, e, !0), u = n;
    do {
      if (a === 0) {
        Qn && !n && _l(t, e, 0, !1);
        break;
      } else {
        if (l = t.current.alternate, u && !f0(l)) {
          a = Qf(t, e, !1), u = !1;
          continue;
        }
        if (a === 2) {
          if (u = e, t.errorRecoveryDisabledLanes & u)
            var s = 0;
          else
            s = t.pendingLanes & -536870913, s = s !== 0 ? s : s & 536870912 ? 536870912 : 0;
          if (s !== 0) {
            e = s;
            t: {
              var d = t;
              a = qa;
              var y = d.current.memoizedState.isDehydrated;
              if (y && (Kn(d, s).flags |= 256), s = Qf(
                d,
                s,
                !1
              ), s !== 2) {
                if (Bf && !y) {
                  d.errorRecoveryDisabledLanes |= u, an |= u, a = 4;
                  break t;
                }
                u = ce, ce = a, u !== null && (ce === null ? ce = u : ce.push.apply(
                  ce,
                  u
                ));
              }
              a = s;
            }
            if (u = !1, a !== 2) continue;
          }
        }
        if (a === 1) {
          Kn(t, 0), _l(t, e, 0, !0);
          break;
        }
        t: {
          switch (n = t, u = a, u) {
            case 0:
            case 1:
              throw Error(f(345));
            case 4:
              if ((e & 4194048) !== e) break;
            case 6:
              _l(
                n,
                e,
                ge,
                !Al
              );
              break t;
            case 2:
              ce = null;
              break;
            case 3:
            case 5:
              break;
            default:
              throw Error(f(329));
          }
          if ((e & 62914560) === e && (a = li + 300 - oe(), 10 < a)) {
            if (_l(
              n,
              e,
              ge,
              !Al
            ), hu(n, 0, !0) !== 0) break t;
            ul = e, n.timeoutHandle = Rm(
              Wd.bind(
                null,
                n,
                l,
                ce,
                ai,
                qf,
                e,
                ge,
                an,
                Vn,
                Al,
                u,
                "Throttled",
                -0,
                0
              ),
              a
            );
            break t;
          }
          Wd(
            n,
            l,
            ce,
            ai,
            qf,
            e,
            ge,
            an,
            Vn,
            Al,
            u,
            null,
            -0,
            0
          );
        }
      }
      break;
    } while (!0);
    Le(t);
  }
  function Wd(t, e, l, n, a, u, s, d, y, A, C, w, O, _) {
    if (t.timeoutHandle = -1, w = e.subtreeFlags, w & 8192 || (w & 16785408) === 16785408) {
      w = {
        stylesheets: null,
        count: 0,
        imgCount: 0,
        imgBytes: 0,
        suspenseyImages: [],
        waitingForImages: !0,
        waitingForViewTransition: !1,
        unsuspend: Ve
      }, jd(
        e,
        u,
        w
      );
      var J = (u & 62914560) === u ? li - oe() : (u & 4194048) === u ? Vd - oe() : 0;
      if (J = K0(
        w,
        J
      ), J !== null) {
        ul = u, t.cancelPendingCommit = J(
          lm.bind(
            null,
            t,
            e,
            u,
            l,
            n,
            a,
            s,
            d,
            y,
            C,
            w,
            null,
            O,
            _
          )
        ), _l(t, u, s, !A);
        return;
      }
    }
    lm(
      t,
      e,
      u,
      l,
      n,
      a,
      s,
      d,
      y
    );
  }
  function f0(t) {
    for (var e = t; ; ) {
      var l = e.tag;
      if ((l === 0 || l === 11 || l === 15) && e.flags & 16384 && (l = e.updateQueue, l !== null && (l = l.stores, l !== null)))
        for (var n = 0; n < l.length; n++) {
          var a = l[n], u = a.getSnapshot;
          a = a.value;
          try {
            if (!de(u(), a)) return !1;
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
  function _l(t, e, l, n) {
    e &= ~Lf, e &= ~an, t.suspendedLanes |= e, t.pingedLanes &= ~e, n && (t.warmLanes |= e), n = t.expirationTimes;
    for (var a = e; 0 < a; ) {
      var u = 31 - re(a), s = 1 << u;
      n[u] = -1, a &= ~s;
    }
    l !== 0 && as(t, l, e);
  }
  function ui() {
    return (St & 6) === 0 ? (ja(0), !1) : !0;
  }
  function Gf() {
    if (st !== null) {
      if (pt === 0)
        var t = st.return;
      else
        t = st, We = kl = null, nf(t), Bn = null, Ta = 0, t = st;
      for (; t !== null; )
        Od(t.alternate, t), t = t.return;
      st = null;
    }
  }
  function Kn(t, e) {
    var l = t.timeoutHandle;
    l !== -1 && (t.timeoutHandle = -1, M0(l)), l = t.cancelPendingCommit, l !== null && (t.cancelPendingCommit = null, l()), ul = 0, Gf(), Mt = t, st = l = Ke(t.current, null), ht = e, pt = 0, ye = null, Al = !1, Qn = ua(t, e), Bf = !1, Vn = ge = Lf = an = Rl = Ht = 0, ce = qa = null, qf = !1, (e & 8) !== 0 && (e |= e & 32);
    var n = t.entangledLanes;
    if (n !== 0)
      for (t = t.entanglements, n &= e; 0 < n; ) {
        var a = 31 - re(n), u = 1 << a;
        e |= t[a], n &= ~u;
      }
    return al = e, zu(), l;
  }
  function kd(t, e) {
    ct = null, R.H = xa, e === Hn || e === Uu ? (e = rr(), pt = 3) : e === Zc ? (e = rr(), pt = 4) : pt = e === pf ? 8 : e !== null && typeof e == "object" && typeof e.then == "function" ? 6 : 1, ye = e, st === null && (Ht = 1, Wu(
      t,
      Re(e, t.current)
    ));
  }
  function Fd() {
    var t = he.current;
    return t === null ? !0 : (ht & 4194048) === ht ? Me === null : (ht & 62914560) === ht || (ht & 536870912) !== 0 ? t === Me : !1;
  }
  function $d() {
    var t = R.H;
    return R.H = xa, t === null ? xa : t;
  }
  function Id() {
    var t = R.A;
    return R.A = i0, t;
  }
  function ii() {
    Ht = 4, Al || (ht & 4194048) !== ht && he.current !== null || (Qn = !0), (Rl & 134217727) === 0 && (an & 134217727) === 0 || Mt === null || _l(
      Mt,
      ht,
      ge,
      !1
    );
  }
  function Qf(t, e, l) {
    var n = St;
    St |= 2;
    var a = $d(), u = Id();
    (Mt !== t || ht !== e) && (ai = null, Kn(t, e)), e = !1;
    var s = Ht;
    t: do
      try {
        if (pt !== 0 && st !== null) {
          var d = st, y = ye;
          switch (pt) {
            case 8:
              Gf(), s = 6;
              break t;
            case 3:
            case 2:
            case 9:
            case 6:
              he.current === null && (e = !0);
              var A = pt;
              if (pt = 0, ye = null, Jn(t, d, y, A), l && Qn) {
                s = 0;
                break t;
              }
              break;
            default:
              A = pt, pt = 0, ye = null, Jn(t, d, y, A);
          }
        }
        o0(), s = Ht;
        break;
      } catch (C) {
        kd(t, C);
      }
    while (!0);
    return e && t.shellSuspendCounter++, We = kl = null, St = n, R.H = a, R.A = u, st === null && (Mt = null, ht = 0, zu()), s;
  }
  function o0() {
    for (; st !== null; ) Pd(st);
  }
  function s0(t, e) {
    var l = St;
    St |= 2;
    var n = $d(), a = Id();
    Mt !== t || ht !== e ? (ai = null, ni = oe() + 500, Kn(t, e)) : Qn = ua(
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
              pt = 0, ye = null, Jn(t, e, u, 1);
              break;
            case 2:
            case 9:
              if (or(u)) {
                pt = 0, ye = null, tm(e);
                break;
              }
              e = function() {
                pt !== 2 && pt !== 9 || Mt !== t || (pt = 7), Le(t);
              }, u.then(e, e);
              break t;
            case 3:
              pt = 7;
              break t;
            case 4:
              pt = 5;
              break t;
            case 7:
              or(u) ? (pt = 0, ye = null, tm(e)) : (pt = 0, ye = null, Jn(t, e, u, 7));
              break;
            case 5:
              var s = null;
              switch (st.tag) {
                case 26:
                  s = st.memoizedState;
                case 5:
                case 27:
                  var d = st;
                  if (s ? Ym(s) : d.stateNode.complete) {
                    pt = 0, ye = null;
                    var y = d.sibling;
                    if (y !== null) st = y;
                    else {
                      var A = d.return;
                      A !== null ? (st = A, ci(A)) : st = null;
                    }
                    break e;
                  }
              }
              pt = 0, ye = null, Jn(t, e, u, 5);
              break;
            case 6:
              pt = 0, ye = null, Jn(t, e, u, 6);
              break;
            case 8:
              Gf(), Ht = 6;
              break t;
            default:
              throw Error(f(462));
          }
        }
        r0();
        break;
      } catch (C) {
        kd(t, C);
      }
    while (!0);
    return We = kl = null, R.H = n, R.A = a, St = l, st !== null ? 0 : (Mt = null, ht = 0, zu(), Ht);
  }
  function r0() {
    for (; st !== null && !Uv(); )
      Pd(st);
  }
  function Pd(t) {
    var e = Ad(t.alternate, t, al);
    t.memoizedProps = t.pendingProps, e === null ? ci(t) : st = e;
  }
  function tm(t) {
    var e = t, l = e.alternate;
    switch (e.tag) {
      case 15:
      case 0:
        e = gd(
          l,
          e,
          e.pendingProps,
          e.type,
          void 0,
          ht
        );
        break;
      case 11:
        e = gd(
          l,
          e,
          e.pendingProps,
          e.type.render,
          e.ref,
          ht
        );
        break;
      case 5:
        nf(e);
      default:
        Od(l, e), e = st = Is(e, al), e = Ad(l, e, al);
    }
    t.memoizedProps = t.pendingProps, e === null ? ci(t) : st = e;
  }
  function Jn(t, e, l, n) {
    We = kl = null, nf(e), Bn = null, Ta = 0;
    var a = e.return;
    try {
      if (Py(
        t,
        a,
        e,
        l,
        ht
      )) {
        Ht = 1, Wu(
          t,
          Re(l, t.current)
        ), st = null;
        return;
      }
    } catch (u) {
      if (a !== null) throw st = a, u;
      Ht = 1, Wu(
        t,
        Re(l, t.current)
      ), st = null;
      return;
    }
    e.flags & 32768 ? (yt || n === 1 ? t = !0 : Qn || (ht & 536870912) !== 0 ? t = !1 : (Al = t = !0, (n === 2 || n === 9 || n === 3 || n === 6) && (n = he.current, n !== null && n.tag === 13 && (n.flags |= 16384))), em(e, t)) : ci(e);
  }
  function ci(t) {
    var e = t;
    do {
      if ((e.flags & 32768) !== 0) {
        em(
          e,
          Al
        );
        return;
      }
      t = e.return;
      var l = l0(
        e.alternate,
        e,
        al
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
  function em(t, e) {
    do {
      var l = n0(t.alternate, t);
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
  function lm(t, e, l, n, a, u, s, d, y) {
    t.cancelPendingCommit = null;
    do
      fi();
    while (Qt !== 0);
    if ((St & 6) !== 0) throw Error(f(327));
    if (e !== null) {
      if (e === t.current) throw Error(f(177));
      if (u = e.lanes | e.childLanes, u |= xc, Vv(
        t,
        l,
        u,
        s,
        d,
        y
      ), t === Mt && (st = Mt = null, ht = 0), Zn = e, zl = t, ul = l, Yf = u, jf = a, Zd = n, (e.subtreeFlags & 10256) !== 0 || (e.flags & 10256) !== 0 ? (t.callbackNode = null, t.callbackPriority = 0, v0(su, function() {
        return cm(), null;
      })) : (t.callbackNode = null, t.callbackPriority = 0), n = (e.flags & 13878) !== 0, (e.subtreeFlags & 13878) !== 0 || n) {
        n = R.T, R.T = null, a = B.p, B.p = 2, s = St, St |= 4;
        try {
          a0(t, e, l);
        } finally {
          St = s, B.p = a, R.T = n;
        }
      }
      Qt = 1, nm(), am(), um();
    }
  }
  function nm() {
    if (Qt === 1) {
      Qt = 0;
      var t = zl, e = Zn, l = (e.flags & 13878) !== 0;
      if ((e.subtreeFlags & 13878) !== 0 || l) {
        l = R.T, R.T = null;
        var n = B.p;
        B.p = 2;
        var a = St;
        St |= 4;
        try {
          Ld(e, t);
          var u = eo, s = Qs(t.containerInfo), d = u.focusedElem, y = u.selectionRange;
          if (s !== d && d && d.ownerDocument && Gs(
            d.ownerDocument.documentElement,
            d
          )) {
            if (y !== null && Oc(d)) {
              var A = y.start, C = y.end;
              if (C === void 0 && (C = A), "selectionStart" in d)
                d.selectionStart = A, d.selectionEnd = Math.min(
                  C,
                  d.value.length
                );
              else {
                var w = d.ownerDocument || document, O = w && w.defaultView || window;
                if (O.getSelection) {
                  var _ = O.getSelection(), J = d.textContent.length, tt = Math.min(y.start, J), Ot = y.end === void 0 ? tt : Math.min(y.end, J);
                  !_.extend && tt > Ot && (s = Ot, Ot = tt, tt = s);
                  var E = Xs(
                    d,
                    tt
                  ), S = Xs(
                    d,
                    Ot
                  );
                  if (E && S && (_.rangeCount !== 1 || _.anchorNode !== E.node || _.anchorOffset !== E.offset || _.focusNode !== S.node || _.focusOffset !== S.offset)) {
                    var T = w.createRange();
                    T.setStart(E.node, E.offset), _.removeAllRanges(), tt > Ot ? (_.addRange(T), _.extend(S.node, S.offset)) : (T.setEnd(S.node, S.offset), _.addRange(T));
                  }
                }
              }
            }
            for (w = [], _ = d; _ = _.parentNode; )
              _.nodeType === 1 && w.push({
                element: _,
                left: _.scrollLeft,
                top: _.scrollTop
              });
            for (typeof d.focus == "function" && d.focus(), d = 0; d < w.length; d++) {
              var x = w[d];
              x.element.scrollLeft = x.left, x.element.scrollTop = x.top;
            }
          }
          pi = !!to, eo = to = null;
        } finally {
          St = a, B.p = n, R.T = l;
        }
      }
      t.current = e, Qt = 2;
    }
  }
  function am() {
    if (Qt === 2) {
      Qt = 0;
      var t = zl, e = Zn, l = (e.flags & 8772) !== 0;
      if ((e.subtreeFlags & 8772) !== 0 || l) {
        l = R.T, R.T = null;
        var n = B.p;
        B.p = 2;
        var a = St;
        St |= 4;
        try {
          Nd(t, e.alternate, e);
        } finally {
          St = a, B.p = n, R.T = l;
        }
      }
      Qt = 3;
    }
  }
  function um() {
    if (Qt === 4 || Qt === 3) {
      Qt = 0, Hv();
      var t = zl, e = Zn, l = ul, n = Zd;
      (e.subtreeFlags & 10256) !== 0 || (e.flags & 10256) !== 0 ? Qt = 5 : (Qt = 0, Zn = zl = null, im(t, t.pendingLanes));
      var a = t.pendingLanes;
      if (a === 0 && (Ol = null), ic(l), e = e.stateNode, se && typeof se.onCommitFiberRoot == "function")
        try {
          se.onCommitFiberRoot(
            aa,
            e,
            void 0,
            (e.current.flags & 128) === 128
          );
        } catch {
        }
      if (n !== null) {
        e = R.T, a = B.p, B.p = 2, R.T = null;
        try {
          for (var u = t.onRecoverableError, s = 0; s < n.length; s++) {
            var d = n[s];
            u(d.value, {
              componentStack: d.stack
            });
          }
        } finally {
          R.T = e, B.p = a;
        }
      }
      (ul & 3) !== 0 && fi(), Le(t), a = t.pendingLanes, (l & 261930) !== 0 && (a & 42) !== 0 ? t === Xf ? Ya++ : (Ya = 0, Xf = t) : Ya = 0, ja(0);
    }
  }
  function im(t, e) {
    (t.pooledCacheLanes &= e) === 0 && (e = t.pooledCache, e != null && (t.pooledCache = null, pa(e)));
  }
  function fi() {
    return nm(), am(), um(), cm();
  }
  function cm() {
    if (Qt !== 5) return !1;
    var t = zl, e = Yf;
    Yf = 0;
    var l = ic(ul), n = R.T, a = B.p;
    try {
      B.p = 32 > l ? 32 : l, R.T = null, l = jf, jf = null;
      var u = zl, s = ul;
      if (Qt = 0, Zn = zl = null, ul = 0, (St & 6) !== 0) throw Error(f(331));
      var d = St;
      if (St |= 4, Gd(u.current), Yd(
        u,
        u.current,
        s,
        l
      ), St = d, ja(0, !1), se && typeof se.onPostCommitFiberRoot == "function")
        try {
          se.onPostCommitFiberRoot(aa, u);
        } catch {
        }
      return !0;
    } finally {
      B.p = a, R.T = n, im(t, e);
    }
  }
  function fm(t, e, l) {
    e = Re(l, e), e = Sf(t.stateNode, e, 2), t = Sl(t, e, 2), t !== null && (ia(t, 2), Le(t));
  }
  function Et(t, e, l) {
    if (t.tag === 3)
      fm(t, t, l);
    else
      for (; e !== null; ) {
        if (e.tag === 3) {
          fm(
            e,
            t,
            l
          );
          break;
        } else if (e.tag === 1) {
          var n = e.stateNode;
          if (typeof e.type.getDerivedStateFromError == "function" || typeof n.componentDidCatch == "function" && (Ol === null || !Ol.has(n))) {
            t = Re(l, t), l = od(2), n = Sl(e, l, 2), n !== null && (sd(
              l,
              n,
              e,
              t
            ), ia(n, 2), Le(n));
            break;
          }
        }
        e = e.return;
      }
  }
  function Vf(t, e, l) {
    var n = t.pingCache;
    if (n === null) {
      n = t.pingCache = new c0();
      var a = /* @__PURE__ */ new Set();
      n.set(e, a);
    } else
      a = n.get(e), a === void 0 && (a = /* @__PURE__ */ new Set(), n.set(e, a));
    a.has(l) || (Bf = !0, a.add(l), t = d0.bind(null, t, e, l), e.then(t, t));
  }
  function d0(t, e, l) {
    var n = t.pingCache;
    n !== null && n.delete(e), t.pingedLanes |= t.suspendedLanes & l, t.warmLanes &= ~l, Mt === t && (ht & l) === l && (Ht === 4 || Ht === 3 && (ht & 62914560) === ht && 300 > oe() - li ? (St & 2) === 0 && Kn(t, 0) : Lf |= l, Vn === ht && (Vn = 0)), Le(t);
  }
  function om(t, e) {
    e === 0 && (e = ns()), t = Kl(t, e), t !== null && (ia(t, e), Le(t));
  }
  function m0(t) {
    var e = t.memoizedState, l = 0;
    e !== null && (l = e.retryLane), om(t, l);
  }
  function h0(t, e) {
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
        throw Error(f(314));
    }
    n !== null && n.delete(e), om(t, l);
  }
  function v0(t, e) {
    return lc(t, e);
  }
  var oi = null, Wn = null, Zf = !1, si = !1, Kf = !1, Ml = 0;
  function Le(t) {
    t !== Wn && t.next === null && (Wn === null ? oi = Wn = t : Wn = Wn.next = t), si = !0, Zf || (Zf = !0, g0());
  }
  function ja(t, e) {
    if (!Kf && si) {
      Kf = !0;
      do
        for (var l = !1, n = oi; n !== null; ) {
          if (t !== 0) {
            var a = n.pendingLanes;
            if (a === 0) var u = 0;
            else {
              var s = n.suspendedLanes, d = n.pingedLanes;
              u = (1 << 31 - re(42 | t) + 1) - 1, u &= a & ~(s & ~d), u = u & 201326741 ? u & 201326741 | 1 : u ? u | 2 : 0;
            }
            u !== 0 && (l = !0, mm(n, u));
          } else
            u = ht, u = hu(
              n,
              n === Mt ? u : 0,
              n.cancelPendingCommit !== null || n.timeoutHandle !== -1
            ), (u & 3) === 0 || ua(n, u) || (l = !0, mm(n, u));
          n = n.next;
        }
      while (l);
      Kf = !1;
    }
  }
  function y0() {
    sm();
  }
  function sm() {
    si = Zf = !1;
    var t = 0;
    Ml !== 0 && _0() && (t = Ml);
    for (var e = oe(), l = null, n = oi; n !== null; ) {
      var a = n.next, u = rm(n, e);
      u === 0 ? (n.next = null, l === null ? oi = a : l.next = a, a === null && (Wn = l)) : (l = n, (t !== 0 || (u & 3) !== 0) && (si = !0)), n = a;
    }
    Qt !== 0 && Qt !== 5 || ja(t), Ml !== 0 && (Ml = 0);
  }
  function rm(t, e) {
    for (var l = t.suspendedLanes, n = t.pingedLanes, a = t.expirationTimes, u = t.pendingLanes & -62914561; 0 < u; ) {
      var s = 31 - re(u), d = 1 << s, y = a[s];
      y === -1 ? ((d & l) === 0 || (d & n) !== 0) && (a[s] = Qv(d, e)) : y <= e && (t.expiredLanes |= d), u &= ~d;
    }
    if (e = Mt, l = ht, l = hu(
      t,
      t === e ? l : 0,
      t.cancelPendingCommit !== null || t.timeoutHandle !== -1
    ), n = t.callbackNode, l === 0 || t === e && (pt === 2 || pt === 9) || t.cancelPendingCommit !== null)
      return n !== null && n !== null && nc(n), t.callbackNode = null, t.callbackPriority = 0;
    if ((l & 3) === 0 || ua(t, l)) {
      if (e = l & -l, e === t.callbackPriority) return e;
      switch (n !== null && nc(n), ic(l)) {
        case 2:
        case 8:
          l = es;
          break;
        case 32:
          l = su;
          break;
        case 268435456:
          l = ls;
          break;
        default:
          l = su;
      }
      return n = dm.bind(null, t), l = lc(l, n), t.callbackPriority = e, t.callbackNode = l, e;
    }
    return n !== null && n !== null && nc(n), t.callbackPriority = 2, t.callbackNode = null, 2;
  }
  function dm(t, e) {
    if (Qt !== 0 && Qt !== 5)
      return t.callbackNode = null, t.callbackPriority = 0, null;
    var l = t.callbackNode;
    if (fi() && t.callbackNode !== l)
      return null;
    var n = ht;
    return n = hu(
      t,
      t === Mt ? n : 0,
      t.cancelPendingCommit !== null || t.timeoutHandle !== -1
    ), n === 0 ? null : (Jd(t, n, e), rm(t, oe()), t.callbackNode != null && t.callbackNode === l ? dm.bind(null, t) : null);
  }
  function mm(t, e) {
    if (fi()) return null;
    Jd(t, e, !0);
  }
  function g0() {
    C0(function() {
      (St & 6) !== 0 ? lc(
        ts,
        y0
      ) : sm();
    });
  }
  function Jf() {
    if (Ml === 0) {
      var t = wn;
      t === 0 && (t = ru, ru <<= 1, (ru & 261888) === 0 && (ru = 256)), Ml = t;
    }
    return Ml;
  }
  function hm(t) {
    return t == null || typeof t == "symbol" || typeof t == "boolean" ? null : typeof t == "function" ? t : bu("" + t);
  }
  function vm(t, e) {
    var l = e.ownerDocument.createElement("input");
    return l.name = e.name, l.value = e.value, t.id && l.setAttribute("form", t.id), e.parentNode.insertBefore(l, e), t = new FormData(t), l.parentNode.removeChild(l), t;
  }
  function b0(t, e, l, n, a) {
    if (e === "submit" && l && l.stateNode === a) {
      var u = hm(
        (a[le] || null).action
      ), s = n.submitter;
      s && (e = (e = s[le] || null) ? hm(e.formAction) : s.getAttribute("formAction"), e !== null && (u = e, s = null));
      var d = new Tu(
        "action",
        "action",
        null,
        n,
        a
      );
      t.push({
        event: d,
        listeners: [
          {
            instance: null,
            listener: function() {
              if (n.defaultPrevented) {
                if (Ml !== 0) {
                  var y = s ? vm(a, s) : new FormData(a);
                  mf(
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
                typeof u == "function" && (d.preventDefault(), y = s ? vm(a, s) : new FormData(a), mf(
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
  for (var Wf = 0; Wf < Cc.length; Wf++) {
    var kf = Cc[Wf], S0 = kf.toLowerCase(), p0 = kf[0].toUpperCase() + kf.slice(1);
    De(
      S0,
      "on" + p0
    );
  }
  De(Ks, "onAnimationEnd"), De(Js, "onAnimationIteration"), De(Ws, "onAnimationStart"), De("dblclick", "onDoubleClick"), De("focusin", "onFocus"), De("focusout", "onBlur"), De(By, "onTransitionRun"), De(Ly, "onTransitionStart"), De(qy, "onTransitionCancel"), De(ks, "onTransitionEnd"), Sn("onMouseEnter", ["mouseout", "mouseover"]), Sn("onMouseLeave", ["mouseout", "mouseover"]), Sn("onPointerEnter", ["pointerout", "pointerover"]), Sn("onPointerLeave", ["pointerout", "pointerover"]), Gl(
    "onChange",
    "change click focusin focusout input keydown keyup selectionchange".split(" ")
  ), Gl(
    "onSelect",
    "focusout contextmenu dragend focusin keydown keyup mousedown mouseup selectionchange".split(
      " "
    )
  ), Gl("onBeforeInput", [
    "compositionend",
    "keypress",
    "textInput",
    "paste"
  ]), Gl(
    "onCompositionEnd",
    "compositionend focusout keydown keypress keyup mousedown".split(" ")
  ), Gl(
    "onCompositionStart",
    "compositionstart focusout keydown keypress keyup mousedown".split(" ")
  ), Gl(
    "onCompositionUpdate",
    "compositionupdate focusout keydown keypress keyup mousedown".split(" ")
  );
  var Xa = "abort canplay canplaythrough durationchange emptied encrypted ended error loadeddata loadedmetadata loadstart pause play playing progress ratechange resize seeked seeking stalled suspend timeupdate volumechange waiting".split(
    " "
  ), E0 = new Set(
    "beforetoggle cancel close invalid load scroll scrollend toggle".split(" ").concat(Xa)
  );
  function ym(t, e) {
    e = (e & 4) !== 0;
    for (var l = 0; l < t.length; l++) {
      var n = t[l], a = n.event;
      n = n.listeners;
      t: {
        var u = void 0;
        if (e)
          for (var s = n.length - 1; 0 <= s; s--) {
            var d = n[s], y = d.instance, A = d.currentTarget;
            if (d = d.listener, y !== u && a.isPropagationStopped())
              break t;
            u = d, a.currentTarget = A;
            try {
              u(a);
            } catch (C) {
              Ou(C);
            }
            a.currentTarget = null, u = y;
          }
        else
          for (s = 0; s < n.length; s++) {
            if (d = n[s], y = d.instance, A = d.currentTarget, d = d.listener, y !== u && a.isPropagationStopped())
              break t;
            u = d, a.currentTarget = A;
            try {
              u(a);
            } catch (C) {
              Ou(C);
            }
            a.currentTarget = null, u = y;
          }
      }
    }
  }
  function rt(t, e) {
    var l = e[cc];
    l === void 0 && (l = e[cc] = /* @__PURE__ */ new Set());
    var n = t + "__bubble";
    l.has(n) || (gm(e, t, 2, !1), l.add(n));
  }
  function Ff(t, e, l) {
    var n = 0;
    e && (n |= 4), gm(
      l,
      t,
      n,
      e
    );
  }
  var ri = "_reactListening" + Math.random().toString(36).slice(2);
  function $f(t) {
    if (!t[ri]) {
      t[ri] = !0, ss.forEach(function(l) {
        l !== "selectionchange" && (E0.has(l) || Ff(l, !1, t), Ff(l, !0, t));
      });
      var e = t.nodeType === 9 ? t : t.ownerDocument;
      e === null || e[ri] || (e[ri] = !0, Ff("selectionchange", !1, e));
    }
  }
  function gm(t, e, l, n) {
    switch (Km(e)) {
      case 2:
        var a = k0;
        break;
      case 8:
        a = F0;
        break;
      default:
        a = mo;
    }
    l = a.bind(
      null,
      e,
      l,
      t
    ), a = void 0, !yc || e !== "touchstart" && e !== "touchmove" && e !== "wheel" || (a = !0), n ? a !== void 0 ? t.addEventListener(e, l, {
      capture: !0,
      passive: a
    }) : t.addEventListener(e, l, !0) : a !== void 0 ? t.addEventListener(e, l, {
      passive: a
    }) : t.addEventListener(e, l, !1);
  }
  function If(t, e, l, n, a) {
    var u = n;
    if ((e & 1) === 0 && (e & 2) === 0 && n !== null)
      t: for (; ; ) {
        if (n === null) return;
        var s = n.tag;
        if (s === 3 || s === 4) {
          var d = n.stateNode.containerInfo;
          if (d === a) break;
          if (s === 4)
            for (s = n.return; s !== null; ) {
              var y = s.tag;
              if ((y === 3 || y === 4) && s.stateNode.containerInfo === a)
                return;
              s = s.return;
            }
          for (; d !== null; ) {
            if (s = yn(d), s === null) return;
            if (y = s.tag, y === 5 || y === 6 || y === 26 || y === 27) {
              n = u = s;
              continue t;
            }
            d = d.parentNode;
          }
        }
        n = n.return;
      }
    Ts(function() {
      var A = u, C = hc(l), w = [];
      t: {
        var O = Fs.get(t);
        if (O !== void 0) {
          var _ = Tu, J = t;
          switch (t) {
            case "keypress":
              if (pu(l) === 0) break t;
            case "keydown":
            case "keyup":
              _ = hy;
              break;
            case "focusin":
              J = "focus", _ = pc;
              break;
            case "focusout":
              J = "blur", _ = pc;
              break;
            case "beforeblur":
            case "afterblur":
              _ = pc;
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
              _ = Os;
              break;
            case "drag":
            case "dragend":
            case "dragenter":
            case "dragexit":
            case "dragleave":
            case "dragover":
            case "dragstart":
            case "drop":
              _ = ly;
              break;
            case "touchcancel":
            case "touchend":
            case "touchmove":
            case "touchstart":
              _ = gy;
              break;
            case Ks:
            case Js:
            case Ws:
              _ = uy;
              break;
            case ks:
              _ = Sy;
              break;
            case "scroll":
            case "scrollend":
              _ = ty;
              break;
            case "wheel":
              _ = Ey;
              break;
            case "copy":
            case "cut":
            case "paste":
              _ = cy;
              break;
            case "gotpointercapture":
            case "lostpointercapture":
            case "pointercancel":
            case "pointerdown":
            case "pointermove":
            case "pointerout":
            case "pointerover":
            case "pointerup":
              _ = _s;
              break;
            case "toggle":
            case "beforetoggle":
              _ = Ay;
          }
          var tt = (e & 4) !== 0, Ot = !tt && (t === "scroll" || t === "scrollend"), E = tt ? O !== null ? O + "Capture" : null : O;
          tt = [];
          for (var S = A, T; S !== null; ) {
            var x = S;
            if (T = x.stateNode, x = x.tag, x !== 5 && x !== 26 && x !== 27 || T === null || E === null || (x = oa(S, E), x != null && tt.push(
              Ga(S, x, T)
            )), Ot) break;
            S = S.return;
          }
          0 < tt.length && (O = new _(
            O,
            J,
            null,
            l,
            C
          ), w.push({ event: O, listeners: tt }));
        }
      }
      if ((e & 7) === 0) {
        t: {
          if (O = t === "mouseover" || t === "pointerover", _ = t === "mouseout" || t === "pointerout", O && l !== mc && (J = l.relatedTarget || l.fromElement) && (yn(J) || J[vn]))
            break t;
          if ((_ || O) && (O = C.window === C ? C : (O = C.ownerDocument) ? O.defaultView || O.parentWindow : window, _ ? (J = l.relatedTarget || l.toElement, _ = A, J = J ? yn(J) : null, J !== null && (Ot = m(J), tt = J.tag, J !== Ot || tt !== 5 && tt !== 27 && tt !== 6) && (J = null)) : (_ = null, J = A), _ !== J)) {
            if (tt = Os, x = "onMouseLeave", E = "onMouseEnter", S = "mouse", (t === "pointerout" || t === "pointerover") && (tt = _s, x = "onPointerLeave", E = "onPointerEnter", S = "pointer"), Ot = _ == null ? O : fa(_), T = J == null ? O : fa(J), O = new tt(
              x,
              S + "leave",
              _,
              l,
              C
            ), O.target = Ot, O.relatedTarget = T, x = null, yn(C) === A && (tt = new tt(
              E,
              S + "enter",
              J,
              l,
              C
            ), tt.target = T, tt.relatedTarget = Ot, x = tt), Ot = x, _ && J)
              e: {
                for (tt = T0, E = _, S = J, T = 0, x = E; x; x = tt(x))
                  T++;
                x = 0;
                for (var P = S; P; P = tt(P))
                  x++;
                for (; 0 < T - x; )
                  E = tt(E), T--;
                for (; 0 < x - T; )
                  S = tt(S), x--;
                for (; T--; ) {
                  if (E === S || S !== null && E === S.alternate) {
                    tt = E;
                    break e;
                  }
                  E = tt(E), S = tt(S);
                }
                tt = null;
              }
            else tt = null;
            _ !== null && bm(
              w,
              O,
              _,
              tt,
              !1
            ), J !== null && Ot !== null && bm(
              w,
              Ot,
              J,
              tt,
              !0
            );
          }
        }
        t: {
          if (O = A ? fa(A) : window, _ = O.nodeName && O.nodeName.toLowerCase(), _ === "select" || _ === "input" && O.type === "file")
            var gt = Hs;
          else if (ws(O))
            if (Bs)
              gt = wy;
            else {
              gt = Dy;
              var k = xy;
            }
          else
            _ = O.nodeName, !_ || _.toLowerCase() !== "input" || O.type !== "checkbox" && O.type !== "radio" ? A && dc(A.elementType) && (gt = Hs) : gt = Ny;
          if (gt && (gt = gt(t, A))) {
            Us(
              w,
              gt,
              l,
              C
            );
            break t;
          }
          k && k(t, O, A), t === "focusout" && A && O.type === "number" && A.memoizedProps.value != null && rc(O, "number", O.value);
        }
        switch (k = A ? fa(A) : window, t) {
          case "focusin":
            (ws(k) || k.contentEditable === "true") && (On = k, zc = A, ga = null);
            break;
          case "focusout":
            ga = zc = On = null;
            break;
          case "mousedown":
            _c = !0;
            break;
          case "contextmenu":
          case "mouseup":
          case "dragend":
            _c = !1, Vs(w, l, C);
            break;
          case "selectionchange":
            if (Hy) break;
          case "keydown":
          case "keyup":
            Vs(w, l, C);
        }
        var ot;
        if (Tc)
          t: {
            switch (t) {
              case "compositionstart":
                var vt = "onCompositionStart";
                break t;
              case "compositionend":
                vt = "onCompositionEnd";
                break t;
              case "compositionupdate":
                vt = "onCompositionUpdate";
                break t;
            }
            vt = void 0;
          }
        else
          Rn ? Ds(t, l) && (vt = "onCompositionEnd") : t === "keydown" && l.keyCode === 229 && (vt = "onCompositionStart");
        vt && (Ms && l.locale !== "ko" && (Rn || vt !== "onCompositionStart" ? vt === "onCompositionEnd" && Rn && (ot = As()) : (dl = C, gc = "value" in dl ? dl.value : dl.textContent, Rn = !0)), k = di(A, vt), 0 < k.length && (vt = new zs(
          vt,
          t,
          null,
          l,
          C
        ), w.push({ event: vt, listeners: k }), ot ? vt.data = ot : (ot = Ns(l), ot !== null && (vt.data = ot)))), (ot = Oy ? zy(t, l) : _y(t, l)) && (vt = di(A, "onBeforeInput"), 0 < vt.length && (k = new zs(
          "onBeforeInput",
          "beforeinput",
          null,
          l,
          C
        ), w.push({
          event: k,
          listeners: vt
        }), k.data = ot)), b0(
          w,
          t,
          A,
          l,
          C
        );
      }
      ym(w, e);
    });
  }
  function Ga(t, e, l) {
    return {
      instance: t,
      listener: e,
      currentTarget: l
    };
  }
  function di(t, e) {
    for (var l = e + "Capture", n = []; t !== null; ) {
      var a = t, u = a.stateNode;
      if (a = a.tag, a !== 5 && a !== 26 && a !== 27 || u === null || (a = oa(t, l), a != null && n.unshift(
        Ga(t, a, u)
      ), a = oa(t, e), a != null && n.push(
        Ga(t, a, u)
      )), t.tag === 3) return n;
      t = t.return;
    }
    return [];
  }
  function T0(t) {
    if (t === null) return null;
    do
      t = t.return;
    while (t && t.tag !== 5 && t.tag !== 27);
    return t || null;
  }
  function bm(t, e, l, n, a) {
    for (var u = e._reactName, s = []; l !== null && l !== n; ) {
      var d = l, y = d.alternate, A = d.stateNode;
      if (d = d.tag, y !== null && y === n) break;
      d !== 5 && d !== 26 && d !== 27 || A === null || (y = A, a ? (A = oa(l, u), A != null && s.unshift(
        Ga(l, A, y)
      )) : a || (A = oa(l, u), A != null && s.push(
        Ga(l, A, y)
      ))), l = l.return;
    }
    s.length !== 0 && t.push({ event: e, listeners: s });
  }
  var A0 = /\r\n?/g, R0 = /\u0000|\uFFFD/g;
  function Sm(t) {
    return (typeof t == "string" ? t : "" + t).replace(A0, `
`).replace(R0, "");
  }
  function pm(t, e) {
    return e = Sm(e), Sm(t) === e;
  }
  function Rt(t, e, l, n, a, u) {
    switch (l) {
      case "children":
        typeof n == "string" ? e === "body" || e === "textarea" && n === "" || En(t, n) : (typeof n == "number" || typeof n == "bigint") && e !== "body" && En(t, "" + n);
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
        ps(t, n, u);
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
        n = bu("" + n), t.setAttribute(l, n);
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
        n = bu("" + n), t.setAttribute(l, n);
        break;
      case "onClick":
        n != null && (t.onclick = Ve);
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
            throw Error(f(61));
          if (l = n.__html, l != null) {
            if (a.children != null) throw Error(f(60));
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
        l = bu("" + n), t.setAttributeNS(
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
        Qe(
          t,
          "http://www.w3.org/1999/xlink",
          "xlink:actuate",
          n
        );
        break;
      case "xlinkArcrole":
        Qe(
          t,
          "http://www.w3.org/1999/xlink",
          "xlink:arcrole",
          n
        );
        break;
      case "xlinkRole":
        Qe(
          t,
          "http://www.w3.org/1999/xlink",
          "xlink:role",
          n
        );
        break;
      case "xlinkShow":
        Qe(
          t,
          "http://www.w3.org/1999/xlink",
          "xlink:show",
          n
        );
        break;
      case "xlinkTitle":
        Qe(
          t,
          "http://www.w3.org/1999/xlink",
          "xlink:title",
          n
        );
        break;
      case "xlinkType":
        Qe(
          t,
          "http://www.w3.org/1999/xlink",
          "xlink:type",
          n
        );
        break;
      case "xmlBase":
        Qe(
          t,
          "http://www.w3.org/XML/1998/namespace",
          "xml:base",
          n
        );
        break;
      case "xmlLang":
        Qe(
          t,
          "http://www.w3.org/XML/1998/namespace",
          "xml:lang",
          n
        );
        break;
      case "xmlSpace":
        Qe(
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
        (!(2 < l.length) || l[0] !== "o" && l[0] !== "O" || l[1] !== "n" && l[1] !== "N") && (l = Iv.get(l) || l, vu(t, l, n));
    }
  }
  function Pf(t, e, l, n, a, u) {
    switch (l) {
      case "style":
        ps(t, n, u);
        break;
      case "dangerouslySetInnerHTML":
        if (n != null) {
          if (typeof n != "object" || !("__html" in n))
            throw Error(f(61));
          if (l = n.__html, l != null) {
            if (a.children != null) throw Error(f(60));
            t.innerHTML = l;
          }
        }
        break;
      case "children":
        typeof n == "string" ? En(t, n) : (typeof n == "number" || typeof n == "bigint") && En(t, "" + n);
        break;
      case "onScroll":
        n != null && rt("scroll", t);
        break;
      case "onScrollEnd":
        n != null && rt("scrollend", t);
        break;
      case "onClick":
        n != null && (t.onclick = Ve);
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
        if (!rs.hasOwnProperty(l))
          t: {
            if (l[0] === "o" && l[1] === "n" && (a = l.endsWith("Capture"), e = l.slice(2, a ? l.length - 7 : void 0), u = t[le] || null, u = u != null ? u[l] : null, typeof u == "function" && t.removeEventListener(e, u, a), typeof n == "function")) {
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
            var s = l[u];
            if (s != null)
              switch (u) {
                case "src":
                  n = !0;
                  break;
                case "srcSet":
                  a = !0;
                  break;
                case "children":
                case "dangerouslySetInnerHTML":
                  throw Error(f(137, e));
                default:
                  Rt(t, e, u, s, l, null);
              }
          }
        a && Rt(t, e, "srcSet", l.srcSet, l, null), n && Rt(t, e, "src", l.src, l, null);
        return;
      case "input":
        rt("invalid", t);
        var d = u = s = a = null, y = null, A = null;
        for (n in l)
          if (l.hasOwnProperty(n)) {
            var C = l[n];
            if (C != null)
              switch (n) {
                case "name":
                  a = C;
                  break;
                case "type":
                  s = C;
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
                  d = C;
                  break;
                case "children":
                case "dangerouslySetInnerHTML":
                  if (C != null)
                    throw Error(f(137, e));
                  break;
                default:
                  Rt(t, e, n, C, l, null);
              }
          }
        ys(
          t,
          u,
          d,
          y,
          A,
          s,
          a,
          !1
        );
        return;
      case "select":
        rt("invalid", t), n = s = u = null;
        for (a in l)
          if (l.hasOwnProperty(a) && (d = l[a], d != null))
            switch (a) {
              case "value":
                u = d;
                break;
              case "defaultValue":
                s = d;
                break;
              case "multiple":
                n = d;
              default:
                Rt(t, e, a, d, l, null);
            }
        e = u, l = s, t.multiple = !!n, e != null ? pn(t, !!n, e, !1) : l != null && pn(t, !!n, l, !0);
        return;
      case "textarea":
        rt("invalid", t), u = a = n = null;
        for (s in l)
          if (l.hasOwnProperty(s) && (d = l[s], d != null))
            switch (s) {
              case "value":
                n = d;
                break;
              case "defaultValue":
                a = d;
                break;
              case "children":
                u = d;
                break;
              case "dangerouslySetInnerHTML":
                if (d != null) throw Error(f(91));
                break;
              default:
                Rt(t, e, s, d, l, null);
            }
        bs(t, n, a, u);
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
        for (n = 0; n < Xa.length; n++)
          rt(Xa[n], t);
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
                throw Error(f(137, e));
              default:
                Rt(t, e, A, n, l, null);
            }
        return;
      default:
        if (dc(e)) {
          for (C in l)
            l.hasOwnProperty(C) && (n = l[C], n !== void 0 && Pf(
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
    for (d in l)
      l.hasOwnProperty(d) && (n = l[d], n != null && Rt(t, e, d, n, l, null));
  }
  function O0(t, e, l, n) {
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
        var a = null, u = null, s = null, d = null, y = null, A = null, C = null;
        for (_ in l) {
          var w = l[_];
          if (l.hasOwnProperty(_) && w != null)
            switch (_) {
              case "checked":
                break;
              case "value":
                break;
              case "defaultValue":
                y = w;
              default:
                n.hasOwnProperty(_) || Rt(t, e, _, null, n, w);
            }
        }
        for (var O in n) {
          var _ = n[O];
          if (w = l[O], n.hasOwnProperty(O) && (_ != null || w != null))
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
                s = _;
                break;
              case "defaultValue":
                d = _;
                break;
              case "children":
              case "dangerouslySetInnerHTML":
                if (_ != null)
                  throw Error(f(137, e));
                break;
              default:
                _ !== w && Rt(
                  t,
                  e,
                  O,
                  _,
                  n,
                  w
                );
            }
        }
        sc(
          t,
          s,
          d,
          y,
          A,
          C,
          u,
          a
        );
        return;
      case "select":
        _ = s = d = O = null;
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
                d = u;
                break;
              case "multiple":
                s = u;
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
        e = d, l = s, n = _, O != null ? pn(t, !!l, O, !1) : !!n != !!l && (e != null ? pn(t, !!l, e, !0) : pn(t, !!l, l ? [] : "", !1));
        return;
      case "textarea":
        _ = O = null;
        for (d in l)
          if (a = l[d], l.hasOwnProperty(d) && a != null && !n.hasOwnProperty(d))
            switch (d) {
              case "value":
                break;
              case "children":
                break;
              default:
                Rt(t, e, d, null, n, a);
            }
        for (s in n)
          if (a = n[s], u = l[s], n.hasOwnProperty(s) && (a != null || u != null))
            switch (s) {
              case "value":
                O = a;
                break;
              case "defaultValue":
                _ = a;
                break;
              case "children":
                break;
              case "dangerouslySetInnerHTML":
                if (a != null) throw Error(f(91));
                break;
              default:
                a !== u && Rt(t, e, s, a, n, u);
            }
        gs(t, O, _);
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
                  throw Error(f(137, e));
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
        if (dc(e)) {
          for (var Ot in l)
            O = l[Ot], l.hasOwnProperty(Ot) && O !== void 0 && !n.hasOwnProperty(Ot) && Pf(
              t,
              e,
              Ot,
              void 0,
              n,
              O
            );
          for (C in n)
            O = n[C], _ = l[C], !n.hasOwnProperty(C) || O === _ || O === void 0 && _ === void 0 || Pf(
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
    for (w in n)
      O = n[w], _ = l[w], !n.hasOwnProperty(w) || O === _ || O == null && _ == null || Rt(t, e, w, O, n, _);
  }
  function Em(t) {
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
  function z0() {
    if (typeof performance.getEntriesByType == "function") {
      for (var t = 0, e = 0, l = performance.getEntriesByType("resource"), n = 0; n < l.length; n++) {
        var a = l[n], u = a.transferSize, s = a.initiatorType, d = a.duration;
        if (u && d && Em(s)) {
          for (s = 0, d = a.responseEnd, n += 1; n < l.length; n++) {
            var y = l[n], A = y.startTime;
            if (A > d) break;
            var C = y.transferSize, w = y.initiatorType;
            C && Em(w) && (y = y.responseEnd, s += C * (y < d ? 1 : (d - A) / (y - A)));
          }
          if (--n, e += 8 * (u + s) / (a.duration / 1e3), t++, 10 < t) break;
        }
      }
      if (0 < t) return e / t / 1e6;
    }
    return navigator.connection && (t = navigator.connection.downlink, typeof t == "number") ? t : 5;
  }
  var to = null, eo = null;
  function mi(t) {
    return t.nodeType === 9 ? t : t.ownerDocument;
  }
  function Tm(t) {
    switch (t) {
      case "http://www.w3.org/2000/svg":
        return 1;
      case "http://www.w3.org/1998/Math/MathML":
        return 2;
      default:
        return 0;
    }
  }
  function Am(t, e) {
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
  function lo(t, e) {
    return t === "textarea" || t === "noscript" || typeof e.children == "string" || typeof e.children == "number" || typeof e.children == "bigint" || typeof e.dangerouslySetInnerHTML == "object" && e.dangerouslySetInnerHTML !== null && e.dangerouslySetInnerHTML.__html != null;
  }
  var no = null;
  function _0() {
    var t = window.event;
    return t && t.type === "popstate" ? t === no ? !1 : (no = t, !0) : (no = null, !1);
  }
  var Rm = typeof setTimeout == "function" ? setTimeout : void 0, M0 = typeof clearTimeout == "function" ? clearTimeout : void 0, Om = typeof Promise == "function" ? Promise : void 0, C0 = typeof queueMicrotask == "function" ? queueMicrotask : typeof Om < "u" ? function(t) {
    return Om.resolve(null).then(t).catch(x0);
  } : Rm;
  function x0(t) {
    setTimeout(function() {
      throw t;
    });
  }
  function Cl(t) {
    return t === "head";
  }
  function zm(t, e) {
    var l = e, n = 0;
    do {
      var a = l.nextSibling;
      if (t.removeChild(l), a && a.nodeType === 8)
        if (l = a.data, l === "/$" || l === "/&") {
          if (n === 0) {
            t.removeChild(a), In(e);
            return;
          }
          n--;
        } else if (l === "$" || l === "$?" || l === "$~" || l === "$!" || l === "&")
          n++;
        else if (l === "html")
          Qa(t.ownerDocument.documentElement);
        else if (l === "head") {
          l = t.ownerDocument.head, Qa(l);
          for (var u = l.firstChild; u; ) {
            var s = u.nextSibling, d = u.nodeName;
            u[ca] || d === "SCRIPT" || d === "STYLE" || d === "LINK" && u.rel.toLowerCase() === "stylesheet" || l.removeChild(u), u = s;
          }
        } else
          l === "body" && Qa(t.ownerDocument.body);
      l = a;
    } while (l);
    In(e);
  }
  function _m(t, e) {
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
  function ao(t) {
    var e = t.firstChild;
    for (e && e.nodeType === 10 && (e = e.nextSibling); e; ) {
      var l = e;
      switch (e = e.nextSibling, l.nodeName) {
        case "HTML":
        case "HEAD":
        case "BODY":
          ao(l), fc(l);
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
  function D0(t, e, l, n) {
    for (; t.nodeType === 1; ) {
      var a = l;
      if (t.nodeName.toLowerCase() !== e.toLowerCase()) {
        if (!n && (t.nodeName !== "INPUT" || t.type !== "hidden"))
          break;
      } else if (n) {
        if (!t[ca])
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
      if (t = Ce(t.nextSibling), t === null) break;
    }
    return null;
  }
  function N0(t, e, l) {
    if (e === "") return null;
    for (; t.nodeType !== 3; )
      if ((t.nodeType !== 1 || t.nodeName !== "INPUT" || t.type !== "hidden") && !l || (t = Ce(t.nextSibling), t === null)) return null;
    return t;
  }
  function Mm(t, e) {
    for (; t.nodeType !== 8; )
      if ((t.nodeType !== 1 || t.nodeName !== "INPUT" || t.type !== "hidden") && !e || (t = Ce(t.nextSibling), t === null)) return null;
    return t;
  }
  function uo(t) {
    return t.data === "$?" || t.data === "$~";
  }
  function io(t) {
    return t.data === "$!" || t.data === "$?" && t.ownerDocument.readyState !== "loading";
  }
  function w0(t, e) {
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
  function Ce(t) {
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
  var co = null;
  function Cm(t) {
    t = t.nextSibling;
    for (var e = 0; t; ) {
      if (t.nodeType === 8) {
        var l = t.data;
        if (l === "/$" || l === "/&") {
          if (e === 0)
            return Ce(t.nextSibling);
          e--;
        } else
          l !== "$" && l !== "$!" && l !== "$?" && l !== "$~" && l !== "&" || e++;
      }
      t = t.nextSibling;
    }
    return null;
  }
  function xm(t) {
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
  function Dm(t, e, l) {
    switch (e = mi(l), t) {
      case "html":
        if (t = e.documentElement, !t) throw Error(f(452));
        return t;
      case "head":
        if (t = e.head, !t) throw Error(f(453));
        return t;
      case "body":
        if (t = e.body, !t) throw Error(f(454));
        return t;
      default:
        throw Error(f(451));
    }
  }
  function Qa(t) {
    for (var e = t.attributes; e.length; )
      t.removeAttributeNode(e[0]);
    fc(t);
  }
  var xe = /* @__PURE__ */ new Map(), Nm = /* @__PURE__ */ new Set();
  function hi(t) {
    return typeof t.getRootNode == "function" ? t.getRootNode() : t.nodeType === 9 ? t : t.ownerDocument;
  }
  var il = B.d;
  B.d = {
    f: U0,
    r: H0,
    D: B0,
    C: L0,
    L: q0,
    m: Y0,
    X: X0,
    S: j0,
    M: G0
  };
  function U0() {
    var t = il.f(), e = ui();
    return t || e;
  }
  function H0(t) {
    var e = gn(t);
    e !== null && e.tag === 5 && e.type === "form" ? kr(e) : il.r(t);
  }
  var kn = typeof document > "u" ? null : document;
  function wm(t, e, l) {
    var n = kn;
    if (n && typeof e == "string" && e) {
      var a = Te(e);
      a = 'link[rel="' + t + '"][href="' + a + '"]', typeof l == "string" && (a += '[crossorigin="' + l + '"]'), Nm.has(a) || (Nm.add(a), t = { rel: t, crossOrigin: l, href: e }, n.querySelector(a) === null && (e = n.createElement("link"), It(e, "link", t), Zt(e), n.head.appendChild(e)));
    }
  }
  function B0(t) {
    il.D(t), wm("dns-prefetch", t, null);
  }
  function L0(t, e) {
    il.C(t, e), wm("preconnect", t, e);
  }
  function q0(t, e, l) {
    il.L(t, e, l);
    var n = kn;
    if (n && t && e) {
      var a = 'link[rel="preload"][as="' + Te(e) + '"]';
      e === "image" && l && l.imageSrcSet ? (a += '[imagesrcset="' + Te(
        l.imageSrcSet
      ) + '"]', typeof l.imageSizes == "string" && (a += '[imagesizes="' + Te(
        l.imageSizes
      ) + '"]')) : a += '[href="' + Te(t) + '"]';
      var u = a;
      switch (e) {
        case "style":
          u = Fn(t);
          break;
        case "script":
          u = $n(t);
      }
      xe.has(u) || (t = z(
        {
          rel: "preload",
          href: e === "image" && l && l.imageSrcSet ? void 0 : t,
          as: e
        },
        l
      ), xe.set(u, t), n.querySelector(a) !== null || e === "style" && n.querySelector(Va(u)) || e === "script" && n.querySelector(Za(u)) || (e = n.createElement("link"), It(e, "link", t), Zt(e), n.head.appendChild(e)));
    }
  }
  function Y0(t, e) {
    il.m(t, e);
    var l = kn;
    if (l && t) {
      var n = e && typeof e.as == "string" ? e.as : "script", a = 'link[rel="modulepreload"][as="' + Te(n) + '"][href="' + Te(t) + '"]', u = a;
      switch (n) {
        case "audioworklet":
        case "paintworklet":
        case "serviceworker":
        case "sharedworker":
        case "worker":
        case "script":
          u = $n(t);
      }
      if (!xe.has(u) && (t = z({ rel: "modulepreload", href: t }, e), xe.set(u, t), l.querySelector(a) === null)) {
        switch (n) {
          case "audioworklet":
          case "paintworklet":
          case "serviceworker":
          case "sharedworker":
          case "worker":
          case "script":
            if (l.querySelector(Za(u)))
              return;
        }
        n = l.createElement("link"), It(n, "link", t), Zt(n), l.head.appendChild(n);
      }
    }
  }
  function j0(t, e, l) {
    il.S(t, e, l);
    var n = kn;
    if (n && t) {
      var a = bn(n).hoistableStyles, u = Fn(t);
      e = e || "default";
      var s = a.get(u);
      if (!s) {
        var d = { loading: 0, preload: null };
        if (s = n.querySelector(
          Va(u)
        ))
          d.loading = 5;
        else {
          t = z(
            { rel: "stylesheet", href: t, "data-precedence": e },
            l
          ), (l = xe.get(u)) && fo(t, l);
          var y = s = n.createElement("link");
          Zt(y), It(y, "link", t), y._p = new Promise(function(A, C) {
            y.onload = A, y.onerror = C;
          }), y.addEventListener("load", function() {
            d.loading |= 1;
          }), y.addEventListener("error", function() {
            d.loading |= 2;
          }), d.loading |= 4, vi(s, e, n);
        }
        s = {
          type: "stylesheet",
          instance: s,
          count: 1,
          state: d
        }, a.set(u, s);
      }
    }
  }
  function X0(t, e) {
    il.X(t, e);
    var l = kn;
    if (l && t) {
      var n = bn(l).hoistableScripts, a = $n(t), u = n.get(a);
      u || (u = l.querySelector(Za(a)), u || (t = z({ src: t, async: !0 }, e), (e = xe.get(a)) && oo(t, e), u = l.createElement("script"), Zt(u), It(u, "link", t), l.head.appendChild(u)), u = {
        type: "script",
        instance: u,
        count: 1,
        state: null
      }, n.set(a, u));
    }
  }
  function G0(t, e) {
    il.M(t, e);
    var l = kn;
    if (l && t) {
      var n = bn(l).hoistableScripts, a = $n(t), u = n.get(a);
      u || (u = l.querySelector(Za(a)), u || (t = z({ src: t, async: !0, type: "module" }, e), (e = xe.get(a)) && oo(t, e), u = l.createElement("script"), Zt(u), It(u, "link", t), l.head.appendChild(u)), u = {
        type: "script",
        instance: u,
        count: 1,
        state: null
      }, n.set(a, u));
    }
  }
  function Um(t, e, l, n) {
    var a = (a = nt.current) ? hi(a) : null;
    if (!a) throw Error(f(446));
    switch (t) {
      case "meta":
      case "title":
        return null;
      case "style":
        return typeof l.precedence == "string" && typeof l.href == "string" ? (e = Fn(l.href), l = bn(
          a
        ).hoistableStyles, n = l.get(e), n || (n = {
          type: "style",
          instance: null,
          count: 0,
          state: null
        }, l.set(e, n)), n) : { type: "void", instance: null, count: 0, state: null };
      case "link":
        if (l.rel === "stylesheet" && typeof l.href == "string" && typeof l.precedence == "string") {
          t = Fn(l.href);
          var u = bn(
            a
          ).hoistableStyles, s = u.get(t);
          if (s || (a = a.ownerDocument || a, s = {
            type: "stylesheet",
            instance: null,
            count: 0,
            state: { loading: 0, preload: null }
          }, u.set(t, s), (u = a.querySelector(
            Va(t)
          )) && !u._p && (s.instance = u, s.state.loading = 5), xe.has(t) || (l = {
            rel: "preload",
            as: "style",
            href: l.href,
            crossOrigin: l.crossOrigin,
            integrity: l.integrity,
            media: l.media,
            hrefLang: l.hrefLang,
            referrerPolicy: l.referrerPolicy
          }, xe.set(t, l), u || Q0(
            a,
            t,
            l,
            s.state
          ))), e && n === null)
            throw Error(f(528, ""));
          return s;
        }
        if (e && n !== null)
          throw Error(f(529, ""));
        return null;
      case "script":
        return e = l.async, l = l.src, typeof l == "string" && e && typeof e != "function" && typeof e != "symbol" ? (e = $n(l), l = bn(
          a
        ).hoistableScripts, n = l.get(e), n || (n = {
          type: "script",
          instance: null,
          count: 0,
          state: null
        }, l.set(e, n)), n) : { type: "void", instance: null, count: 0, state: null };
      default:
        throw Error(f(444, t));
    }
  }
  function Fn(t) {
    return 'href="' + Te(t) + '"';
  }
  function Va(t) {
    return 'link[rel="stylesheet"][' + t + "]";
  }
  function Hm(t) {
    return z({}, t, {
      "data-precedence": t.precedence,
      precedence: null
    });
  }
  function Q0(t, e, l, n) {
    t.querySelector('link[rel="preload"][as="style"][' + e + "]") ? n.loading = 1 : (e = t.createElement("link"), n.preload = e, e.addEventListener("load", function() {
      return n.loading |= 1;
    }), e.addEventListener("error", function() {
      return n.loading |= 2;
    }), It(e, "link", l), Zt(e), t.head.appendChild(e));
  }
  function $n(t) {
    return '[src="' + Te(t) + '"]';
  }
  function Za(t) {
    return "script[async]" + t;
  }
  function Bm(t, e, l) {
    if (e.count++, e.instance === null)
      switch (e.type) {
        case "style":
          var n = t.querySelector(
            'style[data-href~="' + Te(l.href) + '"]'
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
          a = Fn(l.href);
          var u = t.querySelector(
            Va(a)
          );
          if (u)
            return e.state.loading |= 4, e.instance = u, Zt(u), u;
          n = Hm(l), (a = xe.get(a)) && fo(n, a), u = (t.ownerDocument || t).createElement("link"), Zt(u);
          var s = u;
          return s._p = new Promise(function(d, y) {
            s.onload = d, s.onerror = y;
          }), It(u, "link", n), e.state.loading |= 4, vi(u, l.precedence, t), e.instance = u;
        case "script":
          return u = $n(l.src), (a = t.querySelector(
            Za(u)
          )) ? (e.instance = a, Zt(a), a) : (n = l, (a = xe.get(u)) && (n = z({}, l), oo(n, a)), t = t.ownerDocument || t, a = t.createElement("script"), Zt(a), It(a, "link", n), t.head.appendChild(a), e.instance = a);
        case "void":
          return null;
        default:
          throw Error(f(443, e.type));
      }
    else
      e.type === "stylesheet" && (e.state.loading & 4) === 0 && (n = e.instance, e.state.loading |= 4, vi(n, l.precedence, t));
    return e.instance;
  }
  function vi(t, e, l) {
    for (var n = l.querySelectorAll(
      'link[rel="stylesheet"][data-precedence],style[data-precedence]'
    ), a = n.length ? n[n.length - 1] : null, u = a, s = 0; s < n.length; s++) {
      var d = n[s];
      if (d.dataset.precedence === e) u = d;
      else if (u !== a) break;
    }
    u ? u.parentNode.insertBefore(t, u.nextSibling) : (e = l.nodeType === 9 ? l.head : l, e.insertBefore(t, e.firstChild));
  }
  function fo(t, e) {
    t.crossOrigin == null && (t.crossOrigin = e.crossOrigin), t.referrerPolicy == null && (t.referrerPolicy = e.referrerPolicy), t.title == null && (t.title = e.title);
  }
  function oo(t, e) {
    t.crossOrigin == null && (t.crossOrigin = e.crossOrigin), t.referrerPolicy == null && (t.referrerPolicy = e.referrerPolicy), t.integrity == null && (t.integrity = e.integrity);
  }
  var yi = null;
  function Lm(t, e, l) {
    if (yi === null) {
      var n = /* @__PURE__ */ new Map(), a = yi = /* @__PURE__ */ new Map();
      a.set(l, n);
    } else
      a = yi, n = a.get(l), n || (n = /* @__PURE__ */ new Map(), a.set(l, n));
    if (n.has(t)) return n;
    for (n.set(t, null), l = l.getElementsByTagName(t), a = 0; a < l.length; a++) {
      var u = l[a];
      if (!(u[ca] || u[Wt] || t === "link" && u.getAttribute("rel") === "stylesheet") && u.namespaceURI !== "http://www.w3.org/2000/svg") {
        var s = u.getAttribute(e) || "";
        s = t + s;
        var d = n.get(s);
        d ? d.push(u) : n.set(s, [u]);
      }
    }
    return n;
  }
  function qm(t, e, l) {
    t = t.ownerDocument || t, t.head.insertBefore(
      l,
      e === "title" ? t.querySelector("head > title") : null
    );
  }
  function V0(t, e, l) {
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
  function Ym(t) {
    return !(t.type === "stylesheet" && (t.state.loading & 3) === 0);
  }
  function Z0(t, e, l, n) {
    if (l.type === "stylesheet" && (typeof n.media != "string" || matchMedia(n.media).matches !== !1) && (l.state.loading & 4) === 0) {
      if (l.instance === null) {
        var a = Fn(n.href), u = e.querySelector(
          Va(a)
        );
        if (u) {
          e = u._p, e !== null && typeof e == "object" && typeof e.then == "function" && (t.count++, t = gi.bind(t), e.then(t, t)), l.state.loading |= 4, l.instance = u, Zt(u);
          return;
        }
        u = e.ownerDocument || e, n = Hm(n), (a = xe.get(a)) && fo(n, a), u = u.createElement("link"), Zt(u);
        var s = u;
        s._p = new Promise(function(d, y) {
          s.onload = d, s.onerror = y;
        }), It(u, "link", n), l.instance = u;
      }
      t.stylesheets === null && (t.stylesheets = /* @__PURE__ */ new Map()), t.stylesheets.set(l, e), (e = l.state.preload) && (l.state.loading & 3) === 0 && (t.count++, l = gi.bind(t), e.addEventListener("load", l), e.addEventListener("error", l));
    }
  }
  var so = 0;
  function K0(t, e) {
    return t.stylesheets && t.count === 0 && Si(t, t.stylesheets), 0 < t.count || 0 < t.imgCount ? function(l) {
      var n = setTimeout(function() {
        if (t.stylesheets && Si(t, t.stylesheets), t.unsuspend) {
          var u = t.unsuspend;
          t.unsuspend = null, u();
        }
      }, 6e4 + e);
      0 < t.imgBytes && so === 0 && (so = 62500 * z0());
      var a = setTimeout(
        function() {
          if (t.waitingForImages = !1, t.count === 0 && (t.stylesheets && Si(t, t.stylesheets), t.unsuspend)) {
            var u = t.unsuspend;
            t.unsuspend = null, u();
          }
        },
        (t.imgBytes > so ? 50 : 800) + e
      );
      return t.unsuspend = l, function() {
        t.unsuspend = null, clearTimeout(n), clearTimeout(a);
      };
    } : null;
  }
  function gi() {
    if (this.count--, this.count === 0 && (this.imgCount === 0 || !this.waitingForImages)) {
      if (this.stylesheets) Si(this, this.stylesheets);
      else if (this.unsuspend) {
        var t = this.unsuspend;
        this.unsuspend = null, t();
      }
    }
  }
  var bi = null;
  function Si(t, e) {
    t.stylesheets = null, t.unsuspend !== null && (t.count++, bi = /* @__PURE__ */ new Map(), e.forEach(J0, t), bi = null, gi.call(t));
  }
  function J0(t, e) {
    if (!(e.state.loading & 4)) {
      var l = bi.get(t);
      if (l) var n = l.get(null);
      else {
        l = /* @__PURE__ */ new Map(), bi.set(t, l);
        for (var a = t.querySelectorAll(
          "link[data-precedence],style[data-precedence]"
        ), u = 0; u < a.length; u++) {
          var s = a[u];
          (s.nodeName === "LINK" || s.getAttribute("media") !== "not all") && (l.set(s.dataset.precedence, s), n = s);
        }
        n && l.set(null, n);
      }
      a = e.instance, s = a.getAttribute("data-precedence"), u = l.get(s) || n, u === n && l.set(null, a), l.set(s, a), this.count++, n = gi.bind(this), a.addEventListener("load", n), a.addEventListener("error", n), u ? u.parentNode.insertBefore(a, u.nextSibling) : (t = t.nodeType === 9 ? t.head : t, t.insertBefore(a, t.firstChild)), e.state.loading |= 4;
    }
  }
  var Ka = {
    $$typeof: G,
    Provider: null,
    Consumer: null,
    _currentValue: H,
    _currentValue2: H,
    _threadCount: 0
  };
  function W0(t, e, l, n, a, u, s, d, y) {
    this.tag = 1, this.containerInfo = t, this.pingCache = this.current = this.pendingChildren = null, this.timeoutHandle = -1, this.callbackNode = this.next = this.pendingContext = this.context = this.cancelPendingCommit = null, this.callbackPriority = 0, this.expirationTimes = ac(-1), this.entangledLanes = this.shellSuspendCounter = this.errorRecoveryDisabledLanes = this.expiredLanes = this.warmLanes = this.pingedLanes = this.suspendedLanes = this.pendingLanes = 0, this.entanglements = ac(0), this.hiddenUpdates = ac(null), this.identifierPrefix = n, this.onUncaughtError = a, this.onCaughtError = u, this.onRecoverableError = s, this.pooledCache = null, this.pooledCacheLanes = 0, this.formState = y, this.incompleteTransitions = /* @__PURE__ */ new Map();
  }
  function jm(t, e, l, n, a, u, s, d, y, A, C, w) {
    return t = new W0(
      t,
      e,
      l,
      s,
      y,
      A,
      C,
      w,
      d
    ), e = 1, u === !0 && (e |= 24), u = me(3, null, null, e), t.current = u, u.stateNode = t, e = Gc(), e.refCount++, t.pooledCache = e, e.refCount++, u.memoizedState = {
      element: n,
      isDehydrated: l,
      cache: e
    }, Kc(u), t;
  }
  function Xm(t) {
    return t ? (t = Mn, t) : Mn;
  }
  function Gm(t, e, l, n, a, u) {
    a = Xm(a), n.context === null ? n.context = a : n.pendingContext = a, n = bl(e), n.payload = { element: l }, u = u === void 0 ? null : u, u !== null && (n.callback = u), l = Sl(t, n, e), l !== null && (fe(l, t, e), Ra(l, t, e));
  }
  function Qm(t, e) {
    if (t = t.memoizedState, t !== null && t.dehydrated !== null) {
      var l = t.retryLane;
      t.retryLane = l !== 0 && l < e ? l : e;
    }
  }
  function ro(t, e) {
    Qm(t, e), (t = t.alternate) && Qm(t, e);
  }
  function Vm(t) {
    if (t.tag === 13 || t.tag === 31) {
      var e = Kl(t, 67108864);
      e !== null && fe(e, t, 67108864), ro(t, 67108864);
    }
  }
  function Zm(t) {
    if (t.tag === 13 || t.tag === 31) {
      var e = be();
      e = uc(e);
      var l = Kl(t, e);
      l !== null && fe(l, t, e), ro(t, e);
    }
  }
  var pi = !0;
  function k0(t, e, l, n) {
    var a = R.T;
    R.T = null;
    var u = B.p;
    try {
      B.p = 2, mo(t, e, l, n);
    } finally {
      B.p = u, R.T = a;
    }
  }
  function F0(t, e, l, n) {
    var a = R.T;
    R.T = null;
    var u = B.p;
    try {
      B.p = 8, mo(t, e, l, n);
    } finally {
      B.p = u, R.T = a;
    }
  }
  function mo(t, e, l, n) {
    if (pi) {
      var a = ho(n);
      if (a === null)
        If(
          t,
          e,
          n,
          Ei,
          l
        ), Jm(t, n);
      else if (I0(
        a,
        t,
        e,
        l,
        n
      ))
        n.stopPropagation();
      else if (Jm(t, n), e & 4 && -1 < $0.indexOf(t)) {
        for (; a !== null; ) {
          var u = gn(a);
          if (u !== null)
            switch (u.tag) {
              case 3:
                if (u = u.stateNode, u.current.memoizedState.isDehydrated) {
                  var s = Xl(u.pendingLanes);
                  if (s !== 0) {
                    var d = u;
                    for (d.pendingLanes |= 2, d.entangledLanes |= 2; s; ) {
                      var y = 1 << 31 - re(s);
                      d.entanglements[1] |= y, s &= ~y;
                    }
                    Le(u), (St & 6) === 0 && (ni = oe() + 500, ja(0));
                  }
                }
                break;
              case 31:
              case 13:
                d = Kl(u, 2), d !== null && fe(d, u, 2), ui(), ro(u, 2);
            }
          if (u = ho(n), u === null && If(
            t,
            e,
            n,
            Ei,
            l
          ), u === a) break;
          a = u;
        }
        a !== null && n.stopPropagation();
      } else
        If(
          t,
          e,
          n,
          null,
          l
        );
    }
  }
  function ho(t) {
    return t = hc(t), vo(t);
  }
  var Ei = null;
  function vo(t) {
    if (Ei = null, t = yn(t), t !== null) {
      var e = m(t);
      if (e === null) t = null;
      else {
        var l = e.tag;
        if (l === 13) {
          if (t = h(e), t !== null) return t;
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
    return Ei = t, null;
  }
  function Km(t) {
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
        switch (Bv()) {
          case ts:
            return 2;
          case es:
            return 8;
          case su:
          case Lv:
            return 32;
          case ls:
            return 268435456;
          default:
            return 32;
        }
      default:
        return 32;
    }
  }
  var yo = !1, xl = null, Dl = null, Nl = null, Ja = /* @__PURE__ */ new Map(), Wa = /* @__PURE__ */ new Map(), wl = [], $0 = "mousedown mouseup touchcancel touchend touchstart auxclick dblclick pointercancel pointerdown pointerup dragend dragstart drop compositionend compositionstart keydown keypress keyup input textInput copy cut paste click change contextmenu reset".split(
    " "
  );
  function Jm(t, e) {
    switch (t) {
      case "focusin":
      case "focusout":
        xl = null;
        break;
      case "dragenter":
      case "dragleave":
        Dl = null;
        break;
      case "mouseover":
      case "mouseout":
        Nl = null;
        break;
      case "pointerover":
      case "pointerout":
        Ja.delete(e.pointerId);
        break;
      case "gotpointercapture":
      case "lostpointercapture":
        Wa.delete(e.pointerId);
    }
  }
  function ka(t, e, l, n, a, u) {
    return t === null || t.nativeEvent !== u ? (t = {
      blockedOn: e,
      domEventName: l,
      eventSystemFlags: n,
      nativeEvent: u,
      targetContainers: [a]
    }, e !== null && (e = gn(e), e !== null && Vm(e)), t) : (t.eventSystemFlags |= n, e = t.targetContainers, a !== null && e.indexOf(a) === -1 && e.push(a), t);
  }
  function I0(t, e, l, n, a) {
    switch (e) {
      case "focusin":
        return xl = ka(
          xl,
          t,
          e,
          l,
          n,
          a
        ), !0;
      case "dragenter":
        return Dl = ka(
          Dl,
          t,
          e,
          l,
          n,
          a
        ), !0;
      case "mouseover":
        return Nl = ka(
          Nl,
          t,
          e,
          l,
          n,
          a
        ), !0;
      case "pointerover":
        var u = a.pointerId;
        return Ja.set(
          u,
          ka(
            Ja.get(u) || null,
            t,
            e,
            l,
            n,
            a
          )
        ), !0;
      case "gotpointercapture":
        return u = a.pointerId, Wa.set(
          u,
          ka(
            Wa.get(u) || null,
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
  function Wm(t) {
    var e = yn(t.target);
    if (e !== null) {
      var l = m(e);
      if (l !== null) {
        if (e = l.tag, e === 13) {
          if (e = h(l), e !== null) {
            t.blockedOn = e, fs(t.priority, function() {
              Zm(l);
            });
            return;
          }
        } else if (e === 31) {
          if (e = p(l), e !== null) {
            t.blockedOn = e, fs(t.priority, function() {
              Zm(l);
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
  function Ti(t) {
    if (t.blockedOn !== null) return !1;
    for (var e = t.targetContainers; 0 < e.length; ) {
      var l = ho(t.nativeEvent);
      if (l === null) {
        l = t.nativeEvent;
        var n = new l.constructor(
          l.type,
          l
        );
        mc = n, l.target.dispatchEvent(n), mc = null;
      } else
        return e = gn(l), e !== null && Vm(e), t.blockedOn = l, !1;
      e.shift();
    }
    return !0;
  }
  function km(t, e, l) {
    Ti(t) && l.delete(e);
  }
  function P0() {
    yo = !1, xl !== null && Ti(xl) && (xl = null), Dl !== null && Ti(Dl) && (Dl = null), Nl !== null && Ti(Nl) && (Nl = null), Ja.forEach(km), Wa.forEach(km);
  }
  function Ai(t, e) {
    t.blockedOn === e && (t.blockedOn = null, yo || (yo = !0, i.unstable_scheduleCallback(
      i.unstable_NormalPriority,
      P0
    )));
  }
  var Ri = null;
  function Fm(t) {
    Ri !== t && (Ri = t, i.unstable_scheduleCallback(
      i.unstable_NormalPriority,
      function() {
        Ri === t && (Ri = null);
        for (var e = 0; e < t.length; e += 3) {
          var l = t[e], n = t[e + 1], a = t[e + 2];
          if (typeof n != "function") {
            if (vo(n || l) === null)
              continue;
            break;
          }
          var u = gn(l);
          u !== null && (t.splice(e, 3), e -= 3, mf(
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
  function In(t) {
    function e(y) {
      return Ai(y, t);
    }
    xl !== null && Ai(xl, t), Dl !== null && Ai(Dl, t), Nl !== null && Ai(Nl, t), Ja.forEach(e), Wa.forEach(e);
    for (var l = 0; l < wl.length; l++) {
      var n = wl[l];
      n.blockedOn === t && (n.blockedOn = null);
    }
    for (; 0 < wl.length && (l = wl[0], l.blockedOn === null); )
      Wm(l), l.blockedOn === null && wl.shift();
    if (l = (t.ownerDocument || t).$$reactFormReplay, l != null)
      for (n = 0; n < l.length; n += 3) {
        var a = l[n], u = l[n + 1], s = a[le] || null;
        if (typeof u == "function")
          s || Fm(l);
        else if (s) {
          var d = null;
          if (u && u.hasAttribute("formAction")) {
            if (a = u, s = u[le] || null)
              d = s.formAction;
            else if (vo(a) !== null) continue;
          } else d = s.action;
          typeof d == "function" ? l[n + 1] = d : (l.splice(n, 3), n -= 3), Fm(l);
        }
      }
  }
  function $m() {
    function t(u) {
      u.canIntercept && u.info === "react-transition" && u.intercept({
        handler: function() {
          return new Promise(function(s) {
            return a = s;
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
  function go(t) {
    this._internalRoot = t;
  }
  Oi.prototype.render = go.prototype.render = function(t) {
    var e = this._internalRoot;
    if (e === null) throw Error(f(409));
    var l = e.current, n = be();
    Gm(l, n, t, e, null, null);
  }, Oi.prototype.unmount = go.prototype.unmount = function() {
    var t = this._internalRoot;
    if (t !== null) {
      this._internalRoot = null;
      var e = t.containerInfo;
      Gm(t.current, 2, null, t, null, null), ui(), e[vn] = null;
    }
  };
  function Oi(t) {
    this._internalRoot = t;
  }
  Oi.prototype.unstable_scheduleHydration = function(t) {
    if (t) {
      var e = cs();
      t = { blockedOn: null, target: t, priority: e };
      for (var l = 0; l < wl.length && e !== 0 && e < wl[l].priority; l++) ;
      wl.splice(l, 0, t), l === 0 && Wm(t);
    }
  };
  var Im = c.version;
  if (Im !== "19.2.4")
    throw Error(
      f(
        527,
        Im,
        "19.2.4"
      )
    );
  B.findDOMNode = function(t) {
    var e = t._reactInternals;
    if (e === void 0)
      throw typeof t.render == "function" ? Error(f(188)) : (t = Object.keys(t).join(","), Error(f(268, t)));
    return t = v(e), t = t !== null ? M(t) : null, t = t === null ? null : t.stateNode, t;
  };
  var tg = {
    bundleType: 0,
    version: "19.2.4",
    rendererPackageName: "react-dom",
    currentDispatcherRef: R,
    reconcilerVersion: "19.2.4"
  };
  if (typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ < "u") {
    var zi = __REACT_DEVTOOLS_GLOBAL_HOOK__;
    if (!zi.isDisabled && zi.supportsFiber)
      try {
        aa = zi.inject(
          tg
        ), se = zi;
      } catch {
      }
  }
  return $a.createRoot = function(t, e) {
    if (!r(t)) throw Error(f(299));
    var l = !1, n = "", a = ud, u = id, s = cd;
    return e != null && (e.unstable_strictMode === !0 && (l = !0), e.identifierPrefix !== void 0 && (n = e.identifierPrefix), e.onUncaughtError !== void 0 && (a = e.onUncaughtError), e.onCaughtError !== void 0 && (u = e.onCaughtError), e.onRecoverableError !== void 0 && (s = e.onRecoverableError)), e = jm(
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
      s,
      $m
    ), t[vn] = e.current, $f(t), new go(e);
  }, $a.hydrateRoot = function(t, e, l) {
    if (!r(t)) throw Error(f(299));
    var n = !1, a = "", u = ud, s = id, d = cd, y = null;
    return l != null && (l.unstable_strictMode === !0 && (n = !0), l.identifierPrefix !== void 0 && (a = l.identifierPrefix), l.onUncaughtError !== void 0 && (u = l.onUncaughtError), l.onCaughtError !== void 0 && (s = l.onCaughtError), l.onRecoverableError !== void 0 && (d = l.onRecoverableError), l.formState !== void 0 && (y = l.formState)), e = jm(
      t,
      1,
      !0,
      e,
      l ?? null,
      n,
      a,
      y,
      u,
      s,
      d,
      $m
    ), e.context = Xm(null), l = e.current, n = be(), n = uc(n), a = bl(n), a.callback = null, Sl(l, a, n), l = n, e.current.lanes = l, ia(e, l), Le(e), t[vn] = e.current, $f(t), new Oi(e);
  }, $a.version = "19.2.4", $a;
}
var rh;
function Hg() {
  if (rh) return So.exports;
  rh = 1;
  function i() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(i);
      } catch (c) {
        console.error(c);
      }
  }
  return i(), So.exports = Ug(), So.exports;
}
var $h = Hg(), Zi = Fh();
const sS = /* @__PURE__ */ Xh(Zi), xi = [];
let Bg = 1, dh = !1;
function Ih(i, c = !1) {
  return { id: Bg++, isActive: i, bindings: /* @__PURE__ */ new Map(), modal: c };
}
function Ph(i) {
  return xi.push(i), () => {
    const c = xi.indexOf(i);
    c >= 0 && xi.splice(c, 1);
  };
}
function tv(i, c, o) {
  const f = Yg(c);
  let r = i.bindings.get(f);
  return r || (r = [], i.bindings.set(f, r)), r.push(o), () => {
    const m = i.bindings.get(f);
    if (!m)
      return;
    const h = m.lastIndexOf(o);
    h >= 0 && m.splice(h, 1), m.length === 0 && i.bindings.delete(f);
  };
}
const Lg = {
  enter: "Enter",
  return: "Enter",
  escape: "Escape",
  esc: "Escape",
  space: "Space",
  spacebar: "Space",
  " ": "Space",
  up: "ArrowUp",
  down: "ArrowDown",
  left: "ArrowLeft",
  right: "ArrowRight",
  arrowup: "ArrowUp",
  arrowdown: "ArrowDown",
  arrowleft: "ArrowLeft",
  arrowright: "ArrowRight",
  home: "Home",
  end: "End",
  pageup: "PageUp",
  pagedown: "PageDown",
  delete: "Delete",
  del: "Delete",
  tab: "Tab",
  backspace: "Backspace"
};
function ev(i) {
  const c = Lg[i.toLowerCase()];
  return c || (i.length === 1 ? i.toUpperCase() : i);
}
function lv(i, c, o, f, r) {
  return (c ? "Ctrl+" : "") + (o ? "Alt+" : "") + (f ? "Shift+" : "") + (r ? "Meta+" : "") + i;
}
function qg(i) {
  return lv(ev(i.key), i.ctrlKey, i.altKey, i.shiftKey, i.metaKey);
}
function Yg(i) {
  const c = i.split("+").map((p) => p.trim()).filter(Boolean);
  let o = !1, f = !1, r = !1, m = !1, h = "";
  for (const p of c)
    switch (p.toLowerCase()) {
      case "ctrl":
      case "control":
        o = !0;
        break;
      case "alt":
      case "option":
        f = !0;
        break;
      case "shift":
        r = !0;
        break;
      case "meta":
      case "cmd":
      case "command":
      case "win":
        m = !0;
        break;
      default:
        h = p;
    }
  return lv(ev(h), o, f, r, m);
}
const jg = /* @__PURE__ */ new Set([
  "text",
  "search",
  "url",
  "tel",
  "email",
  "password",
  "number",
  "date",
  "datetime-local",
  "month",
  "week",
  "time",
  ""
]);
function Xg(i) {
  if (!i)
    return !1;
  const c = i;
  return c.isContentEditable || c.tagName === "TEXTAREA" ? !0 : c.tagName === "INPUT" ? jg.has(c.type) : !1;
}
function Gg(i) {
  if (!i)
    return !1;
  const c = i;
  return c.isContentEditable || c.tagName === "TEXTAREA";
}
const Qg = /* @__PURE__ */ new Set(["ArrowUp", "ArrowDown", "ArrowLeft", "ArrowRight", "Home", "End", "PageUp", "PageDown"]);
function Vg(i) {
  if (!i)
    return !1;
  const c = i;
  return c.tagName === "BUTTON" || c.getAttribute("role") === "button";
}
function Zg(i) {
  if (i.defaultPrevented)
    return;
  const c = document.activeElement;
  if (i.key === "Enter" && Gg(c) || i.key === "Enter" && Vg(c) || (Qg.has(i.key) || i.key === " ") && Xg(c))
    return;
  const o = qg(i), f = xi.slice().sort((r, m) => m.id - r.id);
  for (const r of f) {
    if (!r.isActive())
      continue;
    const m = r.bindings.get(o);
    if (m && m.length > 0 && m[m.length - 1]() !== !1) {
      i.preventDefault(), i.stopPropagation();
      return;
    }
    if (r.modal)
      return;
  }
}
function Kg() {
  dh || (dh = !0, document.addEventListener("keydown", Zg, !1));
}
const Di = [];
let Jg = 1, mh = !1;
const Wg = [
  "a[href]",
  "button:not([disabled])",
  "input:not([disabled]):not([type=hidden])",
  "select:not([disabled])",
  "textarea:not([disabled])",
  '[tabindex]:not([tabindex="-1"])'
].join(","), kg = "input:not([disabled]):not([type=hidden]),select:not([disabled]),textarea:not([disabled])";
function No(i, c = !1) {
  const o = i.querySelectorAll(c ? kg : Wg);
  for (const f of o)
    if (f.getClientRects().length > 0 || f === document.activeElement)
      return f;
  return null;
}
function Fg() {
  let i = null;
  for (const c of Di)
    c.getElement() && (!i || c.id > i.id) && (i = c);
  return i;
}
function $g(i) {
  const c = Fg();
  if (!c)
    return;
  const o = c.getElement();
  if (!o)
    return;
  const f = i.target;
  if (f && o.contains(f))
    return;
  const r = (c.getFocusTarget && c.getFocusTarget()) ?? No(o) ?? o;
  r && r !== document.activeElement && r.focus();
}
function Ig(i, c) {
  const o = { id: Jg++, getElement: i, getFocusTarget: c };
  return Di.push(o), () => {
    const f = Di.indexOf(o);
    f >= 0 && Di.splice(f, 1);
  };
}
function Pg() {
  mh || (mh = !0, document.addEventListener("focusin", $g, !0));
}
class nv {
  constructor(c) {
    this._subscribers = /* @__PURE__ */ new Set(), this.getSnapshot = () => this._state, this.subscribeStore = (o) => (this._subscribers.add(o), () => this._subscribers.delete(o)), this._state = { ...c };
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
const mn = L.createContext(null), qe = /* @__PURE__ */ new Map();
let av = "", hh = !1;
function on() {
  return av + "/";
}
function Ko(i, c, o, f, r) {
  r !== void 0 && (av = r);
  const m = f ?? "";
  hh ? r !== void 0 && ah(on() + "react-api/events?windowName=" + encodeURIComponent(m)) : (hh = !0, ag(on()), ah(on() + "react-api/events?windowName=" + encodeURIComponent(m))), ug(m);
  const h = document.getElementById(i);
  if (!h) {
    console.error("[TLReact] Mount point not found:", i);
    return;
  }
  const p = jh(c);
  if (!p) {
    console.error("[TLReact] Component not registered:", c);
    return;
  }
  Bi(i);
  const b = new nv(o);
  o.hidden === !0 && (h.style.display = "none");
  const v = (N) => {
    b.applyPatch(N);
  };
  Zo(i, v);
  const M = $h.createRoot(h);
  qe.set(i, { root: M, store: b, sseListener: v }), uv = m;
  const z = () => {
    const N = L.useSyncExternalStore(b.subscribeStore, b.getSnapshot);
    return L.useLayoutEffect(() => {
      h.style.display = N.hidden === !0 ? "none" : "";
    }, [N.hidden]), te.createElement(
      mn.Provider,
      { value: { controlId: i, windowName: m, store: b } },
      te.createElement(p, { controlId: i, state: N })
    );
  };
  Zi.flushSync(() => {
    M.render(te.createElement(z));
  });
}
function tb(i, c, o) {
  Ko(i, c, o);
}
function Bi(i) {
  const c = qe.get(i);
  c && (Wh(i, c.sseListener), c.root && c.root.unmount(), qe.delete(i));
}
function eb(i) {
  return qe.has(i);
}
function lb(i, c) {
  let o = qe.get(i);
  if (!o) {
    const r = new nv(c), m = (h) => {
      r.applyPatch(h);
    };
    Zo(i, m), o = { root: null, store: r, sseListener: m }, qe.set(i, o);
  }
  return { controlId: i, windowName: uv, store: o.store };
}
let uv = "";
function nb() {
  const i = L.useContext(mn);
  if (!i)
    throw new Error("useTLState must be used inside a TLReact-mounted component.");
  return L.useSyncExternalStore(i.store.subscribeStore, i.store.getSnapshot);
}
function ab() {
  const i = L.useContext(mn);
  if (!i)
    throw new Error("useTLCommand must be used inside a TLReact-mounted component.");
  const c = i.controlId, o = i.windowName;
  return L.useCallback(
    async (f, r) => {
      const m = JSON.stringify({
        controlId: c,
        command: f,
        windowName: o,
        arguments: r ?? {}
      });
      try {
        const h = await fetch(on() + "react-api/command", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: m
        });
        h.ok || console.error("[TLReact] Command failed:", h.status, await h.text());
      } catch (h) {
        console.error("[TLReact] Command error:", h);
      }
    },
    [c, o]
  );
}
function rS() {
  const i = L.useContext(mn);
  if (!i)
    throw new Error("useTLUpload must be used inside a TLReact-mounted component.");
  const c = i.controlId, o = i.windowName;
  return L.useCallback(
    async (f) => {
      f.append("controlId", c), f.append("windowName", o);
      try {
        const r = await fetch(on() + "react-api/upload", {
          method: "POST",
          body: f
        });
        r.ok || console.error("[TLReact] Upload failed:", r.status, await r.text());
      } catch (r) {
        console.error("[TLReact] Upload error:", r);
      }
    },
    [c, o]
  );
}
function dS() {
  const i = L.useContext(mn);
  if (!i)
    throw new Error("useTLDataUrl must be used inside a TLReact-mounted component.");
  return on() + "react-api/data?controlId=" + encodeURIComponent(i.controlId) + "&windowName=" + encodeURIComponent(i.windowName);
}
function mS() {
  const i = nb(), c = ab(), o = L.useContext(mn), f = L.useCallback(
    (r) => {
      o == null || o.store.applyPatch({ value: r }), c("valueChanged", { value: r });
    },
    [c, o]
  );
  return [i.value, f];
}
const iv = L.createContext(null), hS = ({ active: i, modal: c, children: o }) => {
  const f = te.useRef(i);
  f.current = i;
  const r = te.useMemo(
    () => Ih(() => f.current ? f.current() : !0, c === !0),
    []
  );
  return te.useEffect(() => Ph(r), [r]), te.createElement(iv.Provider, { value: r }, o);
};
function vS(i, c) {
  const o = L.useContext(iv), f = te.useRef(c);
  f.current = c;
  const r = i == null ? [] : Array.isArray(i) ? i : [i], m = r.join("|");
  te.useEffect(() => {
    if (!o || r.length === 0)
      return;
    const h = () => f.current(), p = r.map((b) => tv(o, b, h));
    return () => p.forEach((b) => b());
  }, [o, m]);
}
function yS(i, c) {
  const o = te.useRef(c);
  o.current = c, te.useEffect(() => {
    if (!i)
      return;
    const f = Ih(() => !0);
    for (const r of Object.keys(o.current))
      tv(f, r, () => {
        var m, h;
        return (h = (m = o.current)[r]) == null ? void 0 : h.call(m);
      });
    return Ph(f);
  }, [i]);
}
function gS(i, c, o = "container") {
  te.useEffect(() => {
    if (!i || !c.current)
      return;
    const r = document.activeElement, m = () => {
      const b = c.current;
      if (!b)
        return null;
      const v = o === "field" ? No(b, !0) : o === "first" ? No(b, !1) : null;
      return v || (b.hasAttribute("tabindex") || b.setAttribute("tabindex", "-1"), b);
    }, h = Ig(() => c.current, m), p = m();
    return p && p !== document.activeElement && p.focus(), () => {
      h(), r && r.isConnected && typeof r.focus == "function" && r.focus();
    };
  }, [i]);
}
function Li(i = document.body) {
  const c = document.body.dataset.windowName, o = document.body.dataset.contextPath, f = i.querySelectorAll("[data-react-module]");
  for (const r of f) {
    if (!r.id || qe.has(r.id))
      continue;
    const m = r.dataset.reactModule, h = c ?? r.dataset.windowName, p = o ?? r.dataset.contextPath;
    if (!m || h === void 0 || p === void 0)
      continue;
    const b = r.dataset.reactState, v = b ? JSON.parse(b) : {};
    Ko(r.id, m, v, h, p);
  }
}
function vh() {
  new MutationObserver((c) => {
    var o;
    for (const f of c)
      for (const r of f.removedNodes)
        if (r instanceof HTMLElement) {
          const m = r.id;
          m && qe.has(m) && qe.get(m).root !== null && Bi(m);
          for (const [h, p] of qe.entries())
            p.root !== null && r.querySelector("#" + CSS.escape(h)) && Bi(h);
        }
    for (const f of c)
      for (const r of f.addedNodes)
        r instanceof HTMLElement && ((o = r.dataset) != null && o.reactModule ? Li(r.parentElement ?? document.body) : r.querySelector("[data-react-module]") && Li(r));
  }).observe(document.body, { childList: !0, subtree: !0 });
}
document.readyState === "loading" ? document.addEventListener("DOMContentLoaded", vh) : vh();
window.addEventListener("load", () => Li());
var Ao = { exports: {} }, Ia = {};
/**
 * @license React
 * react-jsx-runtime.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var yh;
function ub() {
  if (yh) return Ia;
  yh = 1;
  var i = Symbol.for("react.transitional.element"), c = Symbol.for("react.fragment");
  function o(f, r, m) {
    var h = null;
    if (m !== void 0 && (h = "" + m), r.key !== void 0 && (h = "" + r.key), "key" in r) {
      m = {};
      for (var p in r)
        p !== "key" && (m[p] = r[p]);
    } else m = r;
    return r = m.ref, {
      $$typeof: i,
      type: f,
      key: h,
      ref: r !== void 0 ? r : null,
      props: m
    };
  }
  return Ia.Fragment = c, Ia.jsx = o, Ia.jsxs = o, Ia;
}
var gh;
function ib() {
  return gh || (gh = 1, Ao.exports = ub()), Ao.exports;
}
var Vt = ib();
const bS = ({ control: i }) => {
  const c = i, o = jh(c.module), f = L.useMemo(
    () => lb(c.controlId, c.state),
    [c.controlId]
  );
  L.useEffect(() => () => Bi(c.controlId), [c.controlId]);
  const r = L.useSyncExternalStore(f.store.subscribeStore, f.store.getSnapshot), m = JSON.stringify(c.state);
  return L.useEffect(() => {
    f.store.applyPatch(c.state);
  }, [m]), o ? /* @__PURE__ */ Vt.jsx(mn.Provider, { value: f, children: /* @__PURE__ */ Vt.jsx(o, { controlId: c.controlId, state: r }) }) : /* @__PURE__ */ Vt.jsxs("span", { children: [
    "[Component not registered: ",
    c.module,
    "]"
  ] });
};
function Ki() {
  return typeof window < "u";
}
function la(i) {
  return Jo(i) ? (i.nodeName || "").toLowerCase() : "#document";
}
function Se(i) {
  var c;
  return (i == null || (c = i.ownerDocument) == null ? void 0 : c.defaultView) || window;
}
function je(i) {
  var c;
  return (c = (Jo(i) ? i.ownerDocument : i.document) || window.document) == null ? void 0 : c.documentElement;
}
function Jo(i) {
  return Ki() ? i instanceof Node || i instanceof Se(i).Node : !1;
}
function qt(i) {
  return Ki() ? i instanceof Element || i instanceof Se(i).Element : !1;
}
function Xe(i) {
  return Ki() ? i instanceof HTMLElement || i instanceof Se(i).HTMLElement : !1;
}
function wo(i) {
  return !Ki() || typeof ShadowRoot > "u" ? !1 : i instanceof ShadowRoot || i instanceof Se(i).ShadowRoot;
}
function iu(i) {
  const {
    overflow: c,
    overflowX: o,
    overflowY: f,
    display: r
  } = pe(i);
  return /auto|scroll|overlay|hidden|clip/.test(c + f + o) && r !== "inline" && r !== "contents";
}
function cb(i) {
  return /^(table|td|th)$/.test(la(i));
}
function Ji(i) {
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
const fb = /transform|translate|scale|rotate|perspective|filter/, ob = /paint|layout|strict|content/, cn = (i) => !!i && i !== "none";
let Ro;
function Wo(i) {
  const c = qt(i) ? pe(i) : i;
  return cn(c.transform) || cn(c.translate) || cn(c.scale) || cn(c.rotate) || cn(c.perspective) || !Wi() && (cn(c.backdropFilter) || cn(c.filter)) || fb.test(c.willChange || "") || ob.test(c.contain || "");
}
function sb(i) {
  let c = ol(i);
  for (; Xe(c) && !fl(c); ) {
    if (Wo(c))
      return c;
    if (Ji(c))
      return null;
    c = ol(c);
  }
  return null;
}
function Wi() {
  return Ro == null && (Ro = typeof CSS < "u" && CSS.supports && CSS.supports("-webkit-backdrop-filter", "none")), Ro;
}
function fl(i) {
  return /^(html|body|#document)$/.test(la(i));
}
function pe(i) {
  return Se(i).getComputedStyle(i);
}
function ki(i) {
  return qt(i) ? {
    scrollLeft: i.scrollLeft,
    scrollTop: i.scrollTop
  } : {
    scrollLeft: i.scrollX,
    scrollTop: i.scrollY
  };
}
function ol(i) {
  if (la(i) === "html")
    return i;
  const c = (
    // Step into the shadow DOM of the parent of a slotted node.
    i.assignedSlot || // DOM Element detected.
    i.parentNode || // ShadowRoot detected.
    wo(i) && i.host || // Fallback.
    je(i)
  );
  return wo(c) ? c.host : c;
}
function cv(i) {
  const c = ol(i);
  return fl(c) ? i.ownerDocument ? i.ownerDocument.body : i.body : Xe(c) && iu(c) ? c : cv(c);
}
function ql(i, c, o) {
  var f;
  c === void 0 && (c = []), o === void 0 && (o = !0);
  const r = cv(i), m = r === ((f = i.ownerDocument) == null ? void 0 : f.body), h = Se(r);
  if (m) {
    const p = Uo(h);
    return c.concat(h, h.visualViewport || [], iu(r) ? r : [], p && o ? ql(p) : []);
  } else
    return c.concat(r, ql(r, [], o));
}
function Uo(i) {
  return i.parent && Object.getPrototypeOf(i.parent) ? i.frameElement : null;
}
const ea = Math.min, sn = Math.max, qi = Math.round, _i = Math.floor, Ye = (i) => ({
  x: i,
  y: i
}), rb = {
  left: "right",
  right: "left",
  bottom: "top",
  top: "bottom"
};
function Ho(i, c, o) {
  return sn(i, ea(c, o));
}
function cu(i, c) {
  return typeof i == "function" ? i(c) : i;
}
function rn(i) {
  return i.split("-")[0];
}
function fu(i) {
  return i.split("-")[1];
}
function fv(i) {
  return i === "x" ? "y" : "x";
}
function ko(i) {
  return i === "y" ? "height" : "width";
}
function Bl(i) {
  const c = i[0];
  return c === "t" || c === "b" ? "y" : "x";
}
function Fo(i) {
  return fv(Bl(i));
}
function db(i, c, o) {
  o === void 0 && (o = !1);
  const f = fu(i), r = Fo(i), m = ko(r);
  let h = r === "x" ? f === (o ? "end" : "start") ? "right" : "left" : f === "start" ? "bottom" : "top";
  return c.reference[m] > c.floating[m] && (h = Yi(h)), [h, Yi(h)];
}
function mb(i) {
  const c = Yi(i);
  return [Bo(i), c, Bo(c)];
}
function Bo(i) {
  return i.includes("start") ? i.replace("start", "end") : i.replace("end", "start");
}
const bh = ["left", "right"], Sh = ["right", "left"], hb = ["top", "bottom"], vb = ["bottom", "top"];
function yb(i, c, o) {
  switch (i) {
    case "top":
    case "bottom":
      return o ? c ? Sh : bh : c ? bh : Sh;
    case "left":
    case "right":
      return c ? hb : vb;
    default:
      return [];
  }
}
function gb(i, c, o, f) {
  const r = fu(i);
  let m = yb(rn(i), o === "start", f);
  return r && (m = m.map((h) => h + "-" + r), c && (m = m.concat(m.map(Bo)))), m;
}
function Yi(i) {
  const c = rn(i);
  return rb[c] + i.slice(c.length);
}
function bb(i) {
  return {
    top: 0,
    right: 0,
    bottom: 0,
    left: 0,
    ...i
  };
}
function ov(i) {
  return typeof i != "number" ? bb(i) : {
    top: i,
    right: i,
    bottom: i,
    left: i
  };
}
function ji(i) {
  const {
    x: c,
    y: o,
    width: f,
    height: r
  } = i;
  return {
    width: f,
    height: r,
    top: o,
    left: c,
    right: c + f,
    bottom: o + r,
    x: c,
    y: o
  };
}
/*!
* tabbable 6.4.0
* @license MIT, https://github.com/focus-trap/tabbable/blob/master/LICENSE
*/
var Sb = ["input:not([inert]):not([inert] *)", "select:not([inert]):not([inert] *)", "textarea:not([inert]):not([inert] *)", "a[href]:not([inert]):not([inert] *)", "button:not([inert]):not([inert] *)", "[tabindex]:not(slot):not([inert]):not([inert] *)", "audio[controls]:not([inert]):not([inert] *)", "video[controls]:not([inert]):not([inert] *)", '[contenteditable]:not([contenteditable="false"]):not([inert]):not([inert] *)', "details>summary:first-of-type:not([inert]):not([inert] *)", "details:not([inert]):not([inert] *)"], Lo = /* @__PURE__ */ Sb.join(","), sv = typeof Element > "u", au = sv ? function() {
} : Element.prototype.matches || Element.prototype.msMatchesSelector || Element.prototype.webkitMatchesSelector, Xi = !sv && Element.prototype.getRootNode ? function(i) {
  var c;
  return i == null || (c = i.getRootNode) === null || c === void 0 ? void 0 : c.call(i);
} : function(i) {
  return i == null ? void 0 : i.ownerDocument;
}, Gi = function(c, o) {
  var f;
  o === void 0 && (o = !0);
  var r = c == null || (f = c.getAttribute) === null || f === void 0 ? void 0 : f.call(c, "inert"), m = r === "" || r === "true", h = m || o && c && // closest does not exist on shadow roots, so we fall back to a manual
  // lookup upward, in case it is not defined.
  (typeof c.closest == "function" ? c.closest("[inert]") : Gi(c.parentNode));
  return h;
}, pb = function(c) {
  var o, f = c == null || (o = c.getAttribute) === null || o === void 0 ? void 0 : o.call(c, "contenteditable");
  return f === "" || f === "true";
}, Eb = function(c, o, f) {
  if (Gi(c))
    return [];
  var r = Array.prototype.slice.apply(c.querySelectorAll(Lo));
  return o && au.call(c, Lo) && r.unshift(c), r = r.filter(f), r;
}, qo = function(c, o, f) {
  for (var r = [], m = Array.from(c); m.length; ) {
    var h = m.shift();
    if (!Gi(h, !1))
      if (h.tagName === "SLOT") {
        var p = h.assignedElements(), b = p.length ? p : h.children, v = qo(b, !0, f);
        f.flatten ? r.push.apply(r, v) : r.push({
          scopeParent: h,
          candidates: v
        });
      } else {
        var M = au.call(h, Lo);
        M && f.filter(h) && (o || !c.includes(h)) && r.push(h);
        var z = h.shadowRoot || // check for an undisclosed shadow
        typeof f.getShadowRoot == "function" && f.getShadowRoot(h), N = !Gi(z, !1) && (!f.shadowRootFilter || f.shadowRootFilter(h));
        if (z && N) {
          var D = qo(z === !0 ? h.children : z.children, !0, f);
          f.flatten ? r.push.apply(r, D) : r.push({
            scopeParent: h,
            candidates: D
          });
        } else
          m.unshift.apply(m, h.children);
      }
  }
  return r;
}, rv = function(c) {
  return !isNaN(parseInt(c.getAttribute("tabindex"), 10));
}, dv = function(c) {
  if (!c)
    throw new Error("No node provided");
  return c.tabIndex < 0 && (/^(AUDIO|VIDEO|DETAILS)$/.test(c.tagName) || pb(c)) && !rv(c) ? 0 : c.tabIndex;
}, Tb = function(c, o) {
  var f = dv(c);
  return f < 0 && o && !rv(c) ? 0 : f;
}, Ab = function(c, o) {
  return c.tabIndex === o.tabIndex ? c.documentOrder - o.documentOrder : c.tabIndex - o.tabIndex;
}, mv = function(c) {
  return c.tagName === "INPUT";
}, Rb = function(c) {
  return mv(c) && c.type === "hidden";
}, Ob = function(c) {
  var o = c.tagName === "DETAILS" && Array.prototype.slice.apply(c.children).some(function(f) {
    return f.tagName === "SUMMARY";
  });
  return o;
}, zb = function(c, o) {
  for (var f = 0; f < c.length; f++)
    if (c[f].checked && c[f].form === o)
      return c[f];
}, _b = function(c) {
  if (!c.name)
    return !0;
  var o = c.form || Xi(c), f = function(p) {
    return o.querySelectorAll('input[type="radio"][name="' + p + '"]');
  }, r;
  if (typeof window < "u" && typeof window.CSS < "u" && typeof window.CSS.escape == "function")
    r = f(window.CSS.escape(c.name));
  else
    try {
      r = f(c.name);
    } catch (h) {
      return console.error("Looks like you have a radio button with a name attribute containing invalid CSS selector characters and need the CSS.escape polyfill: %s", h.message), !1;
    }
  var m = zb(r, c.form);
  return !m || m === c;
}, Mb = function(c) {
  return mv(c) && c.type === "radio";
}, Cb = function(c) {
  return Mb(c) && !_b(c);
}, xb = function(c) {
  var o, f = c && Xi(c), r = (o = f) === null || o === void 0 ? void 0 : o.host, m = !1;
  if (f && f !== c) {
    var h, p, b;
    for (m = !!((h = r) !== null && h !== void 0 && (p = h.ownerDocument) !== null && p !== void 0 && p.contains(r) || c != null && (b = c.ownerDocument) !== null && b !== void 0 && b.contains(c)); !m && r; ) {
      var v, M, z;
      f = Xi(r), r = (v = f) === null || v === void 0 ? void 0 : v.host, m = !!((M = r) !== null && M !== void 0 && (z = M.ownerDocument) !== null && z !== void 0 && z.contains(r));
    }
  }
  return m;
}, ph = function(c) {
  var o = c.getBoundingClientRect(), f = o.width, r = o.height;
  return f === 0 && r === 0;
}, Db = function(c, o) {
  var f = o.displayCheck, r = o.getShadowRoot;
  if (f === "full-native" && "checkVisibility" in c) {
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
  var h = au.call(c, "details>summary:first-of-type"), p = h ? c.parentElement : c;
  if (au.call(p, "details:not([open]) *"))
    return !0;
  if (!f || f === "full" || // full-native can run this branch when it falls through in case
  // Element#checkVisibility is unsupported
  f === "full-native" || f === "legacy-full") {
    if (typeof r == "function") {
      for (var b = c; c; ) {
        var v = c.parentElement, M = Xi(c);
        if (v && !v.shadowRoot && r(v) === !0)
          return ph(c);
        c.assignedSlot ? c = c.assignedSlot : !v && M !== c.ownerDocument ? c = M.host : c = v;
      }
      c = b;
    }
    if (xb(c))
      return !c.getClientRects().length;
    if (f !== "legacy-full")
      return !0;
  } else if (f === "non-zero-area")
    return ph(c);
  return !1;
}, Nb = function(c) {
  if (/^(INPUT|BUTTON|SELECT|TEXTAREA)$/.test(c.tagName))
    for (var o = c.parentElement; o; ) {
      if (o.tagName === "FIELDSET" && o.disabled) {
        for (var f = 0; f < o.children.length; f++) {
          var r = o.children.item(f);
          if (r.tagName === "LEGEND")
            return au.call(o, "fieldset[disabled] *") ? !0 : !r.contains(c);
        }
        return !0;
      }
      o = o.parentElement;
    }
  return !1;
}, wb = function(c, o) {
  return !(o.disabled || Rb(o) || Db(o, c) || // For a details element with a summary, the summary element gets the focus
  Ob(o) || Nb(o));
}, Eh = function(c, o) {
  return !(Cb(o) || dv(o) < 0 || !wb(c, o));
}, Ub = function(c) {
  var o = parseInt(c.getAttribute("tabindex"), 10);
  return !!(isNaN(o) || o >= 0);
}, hv = function(c) {
  var o = [], f = [];
  return c.forEach(function(r, m) {
    var h = !!r.scopeParent, p = h ? r.scopeParent : r, b = Tb(p, h), v = h ? hv(r.candidates) : p;
    b === 0 ? h ? o.push.apply(o, v) : o.push(p) : f.push({
      documentOrder: m,
      tabIndex: b,
      item: r,
      isScope: h,
      content: v
    });
  }), f.sort(Ab).reduce(function(r, m) {
    return m.isScope ? r.push.apply(r, m.content) : r.push(m.content), r;
  }, []).concat(o);
}, vv = function(c, o) {
  o = o || {};
  var f;
  return o.getShadowRoot ? f = qo([c], o.includeContainer, {
    filter: Eh.bind(null, o),
    flatten: !1,
    getShadowRoot: o.getShadowRoot,
    shadowRootFilter: Ub
  }) : f = Eb(c, o.includeContainer, Eh.bind(null, o)), hv(f);
};
function Hb() {
  return /apple/i.test(navigator.vendor);
}
const Th = "data-floating-ui-focusable";
function Bb(i) {
  let c = i.activeElement;
  for (; ((o = c) == null || (o = o.shadowRoot) == null ? void 0 : o.activeElement) != null; ) {
    var o;
    c = c.shadowRoot.activeElement;
  }
  return c;
}
function Yo(i, c) {
  if (!i || !c)
    return !1;
  const o = c.getRootNode == null ? void 0 : c.getRootNode();
  if (i.contains(c))
    return !0;
  if (o && wo(o)) {
    let f = c;
    for (; f; ) {
      if (i === f)
        return !0;
      f = f.parentNode || f.host;
    }
  }
  return !1;
}
function Pa(i) {
  return "composedPath" in i ? i.composedPath()[0] : i.target;
}
function Oo(i, c) {
  if (c == null)
    return !1;
  if ("composedPath" in i)
    return i.composedPath().includes(c);
  const o = i;
  return o.target != null && c.contains(o.target);
}
function Lb(i) {
  return i.matches("html,body");
}
function uu(i) {
  return (i == null ? void 0 : i.ownerDocument) || document;
}
function qb(i) {
  return i ? i.hasAttribute(Th) ? i : i.querySelector("[" + Th + "]") || i : null;
}
function Ni(i, c, o) {
  return o === void 0 && (o = !0), i.filter((r) => {
    var m;
    return r.parentId === c && (!o || ((m = r.context) == null ? void 0 : m.open));
  }).flatMap((r) => [r, ...Ni(i, r.id, o)]);
}
function Yb(i) {
  return "nativeEvent" in i;
}
var jb = typeof document < "u", Xb = function() {
}, Yl = jb ? L.useLayoutEffect : Xb;
const Gb = {
  ...Gh
}, Qb = Gb.useInsertionEffect, Vb = Qb || ((i) => i());
function Pn(i) {
  const c = L.useRef(() => {
  });
  return Vb(() => {
    c.current = i;
  }), L.useCallback(function() {
    for (var o = arguments.length, f = new Array(o), r = 0; r < o; r++)
      f[r] = arguments[r];
    return c.current == null ? void 0 : c.current(...f);
  }, []);
}
const yv = () => ({
  getShadowRoot: !0,
  displayCheck: (
    // JSDOM does not support the `tabbable` library. To solve this we can
    // check if `ResizeObserver` is a real function (not polyfilled), which
    // determines if the current environment is JSDOM-like.
    typeof ResizeObserver == "function" && ResizeObserver.toString().includes("[native code]") ? "full" : "none"
  )
});
function gv(i, c) {
  const o = vv(i, yv()), f = o.length;
  if (f === 0) return;
  const r = Bb(uu(i)), m = o.indexOf(r), h = m === -1 ? c === 1 ? 0 : f - 1 : m + c;
  return o[h];
}
function Zb(i) {
  return gv(uu(i).body, 1) || i;
}
function Kb(i) {
  return gv(uu(i).body, -1) || i;
}
function zo(i, c) {
  const o = c || i.currentTarget, f = i.relatedTarget;
  return !f || !Yo(o, f);
}
function Jb(i) {
  vv(i, yv()).forEach((o) => {
    o.dataset.tabindex = o.getAttribute("tabindex") || "", o.setAttribute("tabindex", "-1");
  });
}
function Ah(i) {
  i.querySelectorAll("[data-tabindex]").forEach((o) => {
    const f = o.dataset.tabindex;
    delete o.dataset.tabindex, f ? o.setAttribute("tabindex", f) : o.removeAttribute("tabindex");
  });
}
function Rh(i, c, o) {
  let {
    reference: f,
    floating: r
  } = i;
  const m = Bl(c), h = Fo(c), p = ko(h), b = rn(c), v = m === "y", M = f.x + f.width / 2 - r.width / 2, z = f.y + f.height / 2 - r.height / 2, N = f[p] / 2 - r[p] / 2;
  let D;
  switch (b) {
    case "top":
      D = {
        x: M,
        y: f.y - r.height
      };
      break;
    case "bottom":
      D = {
        x: M,
        y: f.y + f.height
      };
      break;
    case "right":
      D = {
        x: f.x + f.width,
        y: z
      };
      break;
    case "left":
      D = {
        x: f.x - r.width,
        y: z
      };
      break;
    default:
      D = {
        x: f.x,
        y: f.y
      };
  }
  switch (fu(c)) {
    case "start":
      D[h] -= N * (o && v ? -1 : 1);
      break;
    case "end":
      D[h] += N * (o && v ? -1 : 1);
      break;
  }
  return D;
}
async function Wb(i, c) {
  var o;
  c === void 0 && (c = {});
  const {
    x: f,
    y: r,
    platform: m,
    rects: h,
    elements: p,
    strategy: b
  } = i, {
    boundary: v = "clippingAncestors",
    rootBoundary: M = "viewport",
    elementContext: z = "floating",
    altBoundary: N = !1,
    padding: D = 0
  } = cu(c, i), Y = ov(D), V = p[N ? z === "floating" ? "reference" : "floating" : z], X = ji(await m.getClippingRect({
    element: (o = await (m.isElement == null ? void 0 : m.isElement(V))) == null || o ? V : V.contextElement || await (m.getDocumentElement == null ? void 0 : m.getDocumentElement(p.floating)),
    boundary: v,
    rootBoundary: M,
    strategy: b
  })), W = z === "floating" ? {
    x: f,
    y: r,
    width: h.floating.width,
    height: h.floating.height
  } : h.reference, G = await (m.getOffsetParent == null ? void 0 : m.getOffsetParent(p.floating)), $ = await (m.isElement == null ? void 0 : m.isElement(G)) ? await (m.getScale == null ? void 0 : m.getScale(G)) || {
    x: 1,
    y: 1
  } : {
    x: 1,
    y: 1
  }, at = ji(m.convertOffsetParentRelativeRectToViewportRelativeRect ? await m.convertOffsetParentRelativeRectToViewportRelativeRect({
    elements: p,
    rect: W,
    offsetParent: G,
    strategy: b
  }) : W);
  return {
    top: (X.top - at.top + Y.top) / $.y,
    bottom: (at.bottom - X.bottom + Y.bottom) / $.y,
    left: (X.left - at.left + Y.left) / $.x,
    right: (at.right - X.right + Y.right) / $.x
  };
}
const kb = 50, Fb = async (i, c, o) => {
  const {
    placement: f = "bottom",
    strategy: r = "absolute",
    middleware: m = [],
    platform: h
  } = o, p = h.detectOverflow ? h : {
    ...h,
    detectOverflow: Wb
  }, b = await (h.isRTL == null ? void 0 : h.isRTL(c));
  let v = await h.getElementRects({
    reference: i,
    floating: c,
    strategy: r
  }), {
    x: M,
    y: z
  } = Rh(v, f, b), N = f, D = 0;
  const Y = {};
  for (let q = 0; q < m.length; q++) {
    const V = m[q];
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
      initialPlacement: f,
      placement: N,
      strategy: r,
      middlewareData: Y,
      rects: v,
      platform: p,
      elements: {
        reference: i,
        floating: c
      }
    });
    M = G ?? M, z = $ ?? z, Y[X] = {
      ...Y[X],
      ...at
    }, lt && D < kb && (D++, typeof lt == "object" && (lt.placement && (N = lt.placement), lt.rects && (v = lt.rects === !0 ? await h.getElementRects({
      reference: i,
      floating: c,
      strategy: r
    }) : lt.rects), {
      x: M,
      y: z
    } = Rh(v, N, b)), q = -1);
  }
  return {
    x: M,
    y: z,
    placement: N,
    strategy: r,
    middlewareData: Y
  };
}, $b = (i) => ({
  name: "arrow",
  options: i,
  async fn(c) {
    const {
      x: o,
      y: f,
      placement: r,
      rects: m,
      platform: h,
      elements: p,
      middlewareData: b
    } = c, {
      element: v,
      padding: M = 0
    } = cu(i, c) || {};
    if (v == null)
      return {};
    const z = ov(M), N = {
      x: o,
      y: f
    }, D = Fo(r), Y = ko(D), q = await h.getDimensions(v), V = D === "y", X = V ? "top" : "left", W = V ? "bottom" : "right", G = V ? "clientHeight" : "clientWidth", $ = m.reference[Y] + m.reference[D] - N[D] - m.floating[Y], at = N[D] - m.reference[D], lt = await (h.getOffsetParent == null ? void 0 : h.getOffsetParent(v));
    let Z = lt ? lt[G] : 0;
    (!Z || !await (h.isElement == null ? void 0 : h.isElement(lt))) && (Z = p.floating[G] || m.floating[Y]);
    const it = $ / 2 - at / 2, zt = Z / 2 - q[Y] / 2 - 1, Tt = ea(z[X], zt), _t = ea(z[W], zt), dt = Tt, Ct = Z - q[Y] - _t, Q = Z / 2 - q[Y] / 2 + it, ft = Ho(dt, Q, Ct), R = !b.arrow && fu(r) != null && Q !== ft && m.reference[Y] / 2 - (Q < dt ? Tt : _t) - q[Y] / 2 < 0, B = R ? Q < dt ? Q - dt : Q - Ct : 0;
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
}), Ib = function(i) {
  return i === void 0 && (i = {}), {
    name: "flip",
    options: i,
    async fn(c) {
      var o, f;
      const {
        placement: r,
        middlewareData: m,
        rects: h,
        initialPlacement: p,
        platform: b,
        elements: v
      } = c, {
        mainAxis: M = !0,
        crossAxis: z = !0,
        fallbackPlacements: N,
        fallbackStrategy: D = "bestFit",
        fallbackAxisSideDirection: Y = "none",
        flipAlignment: q = !0,
        ...V
      } = cu(i, c);
      if ((o = m.arrow) != null && o.alignmentOffset)
        return {};
      const X = rn(r), W = Bl(p), G = rn(p) === p, $ = await (b.isRTL == null ? void 0 : b.isRTL(v.floating)), at = N || (G || !q ? [Yi(p)] : mb(p)), lt = Y !== "none";
      !N && lt && at.push(...gb(p, q, Y, $));
      const Z = [p, ...at], it = await b.detectOverflow(c, V), zt = [];
      let Tt = ((f = m.flip) == null ? void 0 : f.overflows) || [];
      if (M && zt.push(it[X]), z) {
        const Q = db(r, h, $);
        zt.push(it[Q[0]], it[Q[1]]);
      }
      if (Tt = [...Tt, {
        placement: r,
        overflows: zt
      }], !zt.every((Q) => Q <= 0)) {
        var _t, dt;
        const Q = (((_t = m.flip) == null ? void 0 : _t.index) || 0) + 1, ft = Z[Q];
        if (ft && (!(z === "alignment" ? W !== Bl(ft) : !1) || // We leave the current main axis only if every placement on that axis
        // overflows the main axis.
        Tt.every((H) => Bl(H.placement) === W ? H.overflows[0] > 0 : !0)))
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
                  const F = Bl(H.placement);
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
        if (r !== R)
          return {
            reset: {
              placement: R
            }
          };
      }
      return {};
    }
  };
}, Pb = /* @__PURE__ */ new Set(["left", "top"]);
async function t1(i, c) {
  const {
    placement: o,
    platform: f,
    elements: r
  } = i, m = await (f.isRTL == null ? void 0 : f.isRTL(r.floating)), h = rn(o), p = fu(o), b = Bl(o) === "y", v = Pb.has(h) ? -1 : 1, M = m && b ? -1 : 1, z = cu(c, i);
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
  return p && typeof Y == "number" && (D = p === "end" ? Y * -1 : Y), b ? {
    x: D * M,
    y: N * v
  } : {
    x: N * v,
    y: D * M
  };
}
const e1 = function(i) {
  return i === void 0 && (i = 0), {
    name: "offset",
    options: i,
    async fn(c) {
      var o, f;
      const {
        x: r,
        y: m,
        placement: h,
        middlewareData: p
      } = c, b = await t1(c, i);
      return h === ((o = p.offset) == null ? void 0 : o.placement) && (f = p.arrow) != null && f.alignmentOffset ? {} : {
        x: r + b.x,
        y: m + b.y,
        data: {
          ...b,
          placement: h
        }
      };
    }
  };
}, l1 = function(i) {
  return i === void 0 && (i = {}), {
    name: "shift",
    options: i,
    async fn(c) {
      const {
        x: o,
        y: f,
        placement: r,
        platform: m
      } = c, {
        mainAxis: h = !0,
        crossAxis: p = !1,
        limiter: b = {
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
        ...v
      } = cu(i, c), M = {
        x: o,
        y: f
      }, z = await m.detectOverflow(c, v), N = Bl(rn(r)), D = fv(N);
      let Y = M[D], q = M[N];
      if (h) {
        const X = D === "y" ? "top" : "left", W = D === "y" ? "bottom" : "right", G = Y + z[X], $ = Y - z[W];
        Y = Ho(G, Y, $);
      }
      if (p) {
        const X = N === "y" ? "top" : "left", W = N === "y" ? "bottom" : "right", G = q + z[X], $ = q - z[W];
        q = Ho(G, q, $);
      }
      const V = b.fn({
        ...c,
        [D]: Y,
        [N]: q
      });
      return {
        ...V,
        data: {
          x: V.x - o,
          y: V.y - f,
          enabled: {
            [D]: h,
            [N]: p
          }
        }
      };
    }
  };
};
function bv(i) {
  const c = pe(i);
  let o = parseFloat(c.width) || 0, f = parseFloat(c.height) || 0;
  const r = Xe(i), m = r ? i.offsetWidth : o, h = r ? i.offsetHeight : f, p = qi(o) !== m || qi(f) !== h;
  return p && (o = m, f = h), {
    width: o,
    height: f,
    $: p
  };
}
function $o(i) {
  return qt(i) ? i : i.contextElement;
}
function ta(i) {
  const c = $o(i);
  if (!Xe(c))
    return Ye(1);
  const o = c.getBoundingClientRect(), {
    width: f,
    height: r,
    $: m
  } = bv(c);
  let h = (m ? qi(o.width) : o.width) / f, p = (m ? qi(o.height) : o.height) / r;
  return (!h || !Number.isFinite(h)) && (h = 1), (!p || !Number.isFinite(p)) && (p = 1), {
    x: h,
    y: p
  };
}
const n1 = /* @__PURE__ */ Ye(0);
function Sv(i) {
  const c = Se(i);
  return !Wi() || !c.visualViewport ? n1 : {
    x: c.visualViewport.offsetLeft,
    y: c.visualViewport.offsetTop
  };
}
function a1(i, c, o) {
  return c === void 0 && (c = !1), !o || c && o !== Se(i) ? !1 : c;
}
function dn(i, c, o, f) {
  c === void 0 && (c = !1), o === void 0 && (o = !1);
  const r = i.getBoundingClientRect(), m = $o(i);
  let h = Ye(1);
  c && (f ? qt(f) && (h = ta(f)) : h = ta(i));
  const p = a1(m, o, f) ? Sv(m) : Ye(0);
  let b = (r.left + p.x) / h.x, v = (r.top + p.y) / h.y, M = r.width / h.x, z = r.height / h.y;
  if (m) {
    const N = Se(m), D = f && qt(f) ? Se(f) : f;
    let Y = N, q = Uo(Y);
    for (; q && f && D !== Y; ) {
      const V = ta(q), X = q.getBoundingClientRect(), W = pe(q), G = X.left + (q.clientLeft + parseFloat(W.paddingLeft)) * V.x, $ = X.top + (q.clientTop + parseFloat(W.paddingTop)) * V.y;
      b *= V.x, v *= V.y, M *= V.x, z *= V.y, b += G, v += $, Y = Se(q), q = Uo(Y);
    }
  }
  return ji({
    width: M,
    height: z,
    x: b,
    y: v
  });
}
function Fi(i, c) {
  const o = ki(i).scrollLeft;
  return c ? c.left + o : dn(je(i)).left + o;
}
function pv(i, c) {
  const o = i.getBoundingClientRect(), f = o.left + c.scrollLeft - Fi(i, o), r = o.top + c.scrollTop;
  return {
    x: f,
    y: r
  };
}
function u1(i) {
  let {
    elements: c,
    rect: o,
    offsetParent: f,
    strategy: r
  } = i;
  const m = r === "fixed", h = je(f), p = c ? Ji(c.floating) : !1;
  if (f === h || p && m)
    return o;
  let b = {
    scrollLeft: 0,
    scrollTop: 0
  }, v = Ye(1);
  const M = Ye(0), z = Xe(f);
  if ((z || !z && !m) && ((la(f) !== "body" || iu(h)) && (b = ki(f)), z)) {
    const D = dn(f);
    v = ta(f), M.x = D.x + f.clientLeft, M.y = D.y + f.clientTop;
  }
  const N = h && !z && !m ? pv(h, b) : Ye(0);
  return {
    width: o.width * v.x,
    height: o.height * v.y,
    x: o.x * v.x - b.scrollLeft * v.x + M.x + N.x,
    y: o.y * v.y - b.scrollTop * v.y + M.y + N.y
  };
}
function i1(i) {
  return Array.from(i.getClientRects());
}
function c1(i) {
  const c = je(i), o = ki(i), f = i.ownerDocument.body, r = sn(c.scrollWidth, c.clientWidth, f.scrollWidth, f.clientWidth), m = sn(c.scrollHeight, c.clientHeight, f.scrollHeight, f.clientHeight);
  let h = -o.scrollLeft + Fi(i);
  const p = -o.scrollTop;
  return pe(f).direction === "rtl" && (h += sn(c.clientWidth, f.clientWidth) - r), {
    width: r,
    height: m,
    x: h,
    y: p
  };
}
const Oh = 25;
function f1(i, c) {
  const o = Se(i), f = je(i), r = o.visualViewport;
  let m = f.clientWidth, h = f.clientHeight, p = 0, b = 0;
  if (r) {
    m = r.width, h = r.height;
    const M = Wi();
    (!M || M && c === "fixed") && (p = r.offsetLeft, b = r.offsetTop);
  }
  const v = Fi(f);
  if (v <= 0) {
    const M = f.ownerDocument, z = M.body, N = getComputedStyle(z), D = M.compatMode === "CSS1Compat" && parseFloat(N.marginLeft) + parseFloat(N.marginRight) || 0, Y = Math.abs(f.clientWidth - z.clientWidth - D);
    Y <= Oh && (m -= Y);
  } else v <= Oh && (m += v);
  return {
    width: m,
    height: h,
    x: p,
    y: b
  };
}
function o1(i, c) {
  const o = dn(i, !0, c === "fixed"), f = o.top + i.clientTop, r = o.left + i.clientLeft, m = Xe(i) ? ta(i) : Ye(1), h = i.clientWidth * m.x, p = i.clientHeight * m.y, b = r * m.x, v = f * m.y;
  return {
    width: h,
    height: p,
    x: b,
    y: v
  };
}
function zh(i, c, o) {
  let f;
  if (c === "viewport")
    f = f1(i, o);
  else if (c === "document")
    f = c1(je(i));
  else if (qt(c))
    f = o1(c, o);
  else {
    const r = Sv(i);
    f = {
      x: c.x - r.x,
      y: c.y - r.y,
      width: c.width,
      height: c.height
    };
  }
  return ji(f);
}
function Ev(i, c) {
  const o = ol(i);
  return o === c || !qt(o) || fl(o) ? !1 : pe(o).position === "fixed" || Ev(o, c);
}
function s1(i, c) {
  const o = c.get(i);
  if (o)
    return o;
  let f = ql(i, [], !1).filter((p) => qt(p) && la(p) !== "body"), r = null;
  const m = pe(i).position === "fixed";
  let h = m ? ol(i) : i;
  for (; qt(h) && !fl(h); ) {
    const p = pe(h), b = Wo(h);
    !b && p.position === "fixed" && (r = null), (m ? !b && !r : !b && p.position === "static" && !!r && (r.position === "absolute" || r.position === "fixed") || iu(h) && !b && Ev(i, h)) ? f = f.filter((M) => M !== h) : r = p, h = ol(h);
  }
  return c.set(i, f), f;
}
function r1(i) {
  let {
    element: c,
    boundary: o,
    rootBoundary: f,
    strategy: r
  } = i;
  const h = [...o === "clippingAncestors" ? Ji(c) ? [] : s1(c, this._c) : [].concat(o), f], p = zh(c, h[0], r);
  let b = p.top, v = p.right, M = p.bottom, z = p.left;
  for (let N = 1; N < h.length; N++) {
    const D = zh(c, h[N], r);
    b = sn(D.top, b), v = ea(D.right, v), M = ea(D.bottom, M), z = sn(D.left, z);
  }
  return {
    width: v - z,
    height: M - b,
    x: z,
    y: b
  };
}
function d1(i) {
  const {
    width: c,
    height: o
  } = bv(i);
  return {
    width: c,
    height: o
  };
}
function m1(i, c, o) {
  const f = Xe(c), r = je(c), m = o === "fixed", h = dn(i, !0, m, c);
  let p = {
    scrollLeft: 0,
    scrollTop: 0
  };
  const b = Ye(0);
  function v() {
    b.x = Fi(r);
  }
  if (f || !f && !m)
    if ((la(c) !== "body" || iu(r)) && (p = ki(c)), f) {
      const D = dn(c, !0, m, c);
      b.x = D.x + c.clientLeft, b.y = D.y + c.clientTop;
    } else r && v();
  m && !f && r && v();
  const M = r && !f && !m ? pv(r, p) : Ye(0), z = h.left + p.scrollLeft - b.x - M.x, N = h.top + p.scrollTop - b.y - M.y;
  return {
    x: z,
    y: N,
    width: h.width,
    height: h.height
  };
}
function _o(i) {
  return pe(i).position === "static";
}
function _h(i, c) {
  if (!Xe(i) || pe(i).position === "fixed")
    return null;
  if (c)
    return c(i);
  let o = i.offsetParent;
  return je(i) === o && (o = o.ownerDocument.body), o;
}
function Tv(i, c) {
  const o = Se(i);
  if (Ji(i))
    return o;
  if (!Xe(i)) {
    let r = ol(i);
    for (; r && !fl(r); ) {
      if (qt(r) && !_o(r))
        return r;
      r = ol(r);
    }
    return o;
  }
  let f = _h(i, c);
  for (; f && cb(f) && _o(f); )
    f = _h(f, c);
  return f && fl(f) && _o(f) && !Wo(f) ? o : f || sb(i) || o;
}
const h1 = async function(i) {
  const c = this.getOffsetParent || Tv, o = this.getDimensions, f = await o(i.floating);
  return {
    reference: m1(i.reference, await c(i.floating), i.strategy),
    floating: {
      x: 0,
      y: 0,
      width: f.width,
      height: f.height
    }
  };
};
function v1(i) {
  return pe(i).direction === "rtl";
}
const y1 = {
  convertOffsetParentRelativeRectToViewportRelativeRect: u1,
  getDocumentElement: je,
  getClippingRect: r1,
  getOffsetParent: Tv,
  getElementRects: h1,
  getClientRects: i1,
  getDimensions: d1,
  getScale: ta,
  isElement: qt,
  isRTL: v1
};
function Av(i, c) {
  return i.x === c.x && i.y === c.y && i.width === c.width && i.height === c.height;
}
function g1(i, c) {
  let o = null, f;
  const r = je(i);
  function m() {
    var p;
    clearTimeout(f), (p = o) == null || p.disconnect(), o = null;
  }
  function h(p, b) {
    p === void 0 && (p = !1), b === void 0 && (b = 1), m();
    const v = i.getBoundingClientRect(), {
      left: M,
      top: z,
      width: N,
      height: D
    } = v;
    if (p || c(), !N || !D)
      return;
    const Y = _i(z), q = _i(r.clientWidth - (M + N)), V = _i(r.clientHeight - (z + D)), X = _i(M), G = {
      rootMargin: -Y + "px " + -q + "px " + -V + "px " + -X + "px",
      threshold: sn(0, ea(1, b)) || 1
    };
    let $ = !0;
    function at(lt) {
      const Z = lt[0].intersectionRatio;
      if (Z !== b) {
        if (!$)
          return h();
        Z ? h(!1, Z) : f = setTimeout(() => {
          h(!1, 1e-7);
        }, 1e3);
      }
      Z === 1 && !Av(v, i.getBoundingClientRect()) && h(), $ = !1;
    }
    try {
      o = new IntersectionObserver(at, {
        ...G,
        // Handle <iframe>s
        root: r.ownerDocument
      });
    } catch {
      o = new IntersectionObserver(at, G);
    }
    o.observe(i);
  }
  return h(!0), m;
}
function b1(i, c, o, f) {
  f === void 0 && (f = {});
  const {
    ancestorScroll: r = !0,
    ancestorResize: m = !0,
    elementResize: h = typeof ResizeObserver == "function",
    layoutShift: p = typeof IntersectionObserver == "function",
    animationFrame: b = !1
  } = f, v = $o(i), M = r || m ? [...v ? ql(v) : [], ...c ? ql(c) : []] : [];
  M.forEach((X) => {
    r && X.addEventListener("scroll", o, {
      passive: !0
    }), m && X.addEventListener("resize", o);
  });
  const z = v && p ? g1(v, o) : null;
  let N = -1, D = null;
  h && (D = new ResizeObserver((X) => {
    let [W] = X;
    W && W.target === v && D && c && (D.unobserve(c), cancelAnimationFrame(N), N = requestAnimationFrame(() => {
      var G;
      (G = D) == null || G.observe(c);
    })), o();
  }), v && !b && D.observe(v), c && D.observe(c));
  let Y, q = b ? dn(i) : null;
  b && V();
  function V() {
    const X = dn(i);
    q && !Av(q, X) && o(), q = X, Y = requestAnimationFrame(V);
  }
  return o(), () => {
    var X;
    M.forEach((W) => {
      r && W.removeEventListener("scroll", o), m && W.removeEventListener("resize", o);
    }), z == null || z(), (X = D) == null || X.disconnect(), D = null, b && cancelAnimationFrame(Y);
  };
}
const S1 = e1, p1 = l1, E1 = Ib, Mh = $b, T1 = (i, c, o) => {
  const f = /* @__PURE__ */ new Map(), r = {
    platform: y1,
    ...o
  }, m = {
    ...r.platform,
    _c: f
  };
  return Fb(i, c, {
    ...r,
    platform: m
  });
};
var A1 = typeof document < "u", R1 = function() {
}, wi = A1 ? L.useLayoutEffect : R1;
function Qi(i, c) {
  if (i === c)
    return !0;
  if (typeof i != typeof c)
    return !1;
  if (typeof i == "function" && i.toString() === c.toString())
    return !0;
  let o, f, r;
  if (i && c && typeof i == "object") {
    if (Array.isArray(i)) {
      if (o = i.length, o !== c.length) return !1;
      for (f = o; f-- !== 0; )
        if (!Qi(i[f], c[f]))
          return !1;
      return !0;
    }
    if (r = Object.keys(i), o = r.length, o !== Object.keys(c).length)
      return !1;
    for (f = o; f-- !== 0; )
      if (!{}.hasOwnProperty.call(c, r[f]))
        return !1;
    for (f = o; f-- !== 0; ) {
      const m = r[f];
      if (!(m === "_owner" && i.$$typeof) && !Qi(i[m], c[m]))
        return !1;
    }
    return !0;
  }
  return i !== i && c !== c;
}
function Rv(i) {
  return typeof window > "u" ? 1 : (i.ownerDocument.defaultView || window).devicePixelRatio || 1;
}
function Ch(i, c) {
  const o = Rv(i);
  return Math.round(c * o) / o;
}
function Mo(i) {
  const c = L.useRef(i);
  return wi(() => {
    c.current = i;
  }), c;
}
function O1(i) {
  i === void 0 && (i = {});
  const {
    placement: c = "bottom",
    strategy: o = "absolute",
    middleware: f = [],
    platform: r,
    elements: {
      reference: m,
      floating: h
    } = {},
    transform: p = !0,
    whileElementsMounted: b,
    open: v
  } = i, [M, z] = L.useState({
    x: 0,
    y: 0,
    strategy: o,
    placement: c,
    middlewareData: {},
    isPositioned: !1
  }), [N, D] = L.useState(f);
  Qi(N, f) || D(f);
  const [Y, q] = L.useState(null), [V, X] = L.useState(null), W = L.useCallback((H) => {
    H !== lt.current && (lt.current = H, q(H));
  }, []), G = L.useCallback((H) => {
    H !== Z.current && (Z.current = H, X(H));
  }, []), $ = m || Y, at = h || V, lt = L.useRef(null), Z = L.useRef(null), it = L.useRef(M), zt = b != null, Tt = Mo(b), _t = Mo(r), dt = Mo(v), Ct = L.useCallback(() => {
    if (!lt.current || !Z.current)
      return;
    const H = {
      placement: c,
      strategy: o,
      middleware: N
    };
    _t.current && (H.platform = _t.current), T1(lt.current, Z.current, H).then((F) => {
      const et = {
        ...F,
        // The floating element's position may be recomputed while it's closed
        // but still mounted (such as when transitioning out). To ensure
        // `isPositioned` will be `false` initially on the next open, avoid
        // setting it to `true` when `open === false` (must be specified).
        isPositioned: dt.current !== !1
      };
      Q.current && !Qi(it.current, et) && (it.current = et, Zi.flushSync(() => {
        z(et);
      }));
    });
  }, [N, c, o, _t, dt]);
  wi(() => {
    v === !1 && it.current.isPositioned && (it.current.isPositioned = !1, z((H) => ({
      ...H,
      isPositioned: !1
    })));
  }, [v]);
  const Q = L.useRef(!1);
  wi(() => (Q.current = !0, () => {
    Q.current = !1;
  }), []), wi(() => {
    if ($ && (lt.current = $), at && (Z.current = at), $ && at) {
      if (Tt.current)
        return Tt.current($, at, Ct);
      Ct();
    }
  }, [$, at, Ct, Tt, zt]);
  const ft = L.useMemo(() => ({
    reference: lt,
    floating: Z,
    setReference: W,
    setFloating: G
  }), [W, G]), R = L.useMemo(() => ({
    reference: $,
    floating: at
  }), [$, at]), B = L.useMemo(() => {
    const H = {
      position: o,
      left: 0,
      top: 0
    };
    if (!R.floating)
      return H;
    const F = Ch(R.floating, M.x), et = Ch(R.floating, M.y);
    return p ? {
      ...H,
      transform: "translate(" + F + "px, " + et + "px)",
      ...Rv(R.floating) >= 1.5 && {
        willChange: "transform"
      }
    } : {
      position: o,
      left: F,
      top: et
    };
  }, [o, p, R.floating, M.x, M.y]);
  return L.useMemo(() => ({
    ...M,
    update: Ct,
    refs: ft,
    elements: R,
    floatingStyles: B
  }), [M, Ct, ft, R, B]);
}
const z1 = (i) => {
  function c(o) {
    return {}.hasOwnProperty.call(o, "current");
  }
  return {
    name: "arrow",
    options: i,
    fn(o) {
      const {
        element: f,
        padding: r
      } = typeof i == "function" ? i(o) : i;
      return f && c(f) ? f.current != null ? Mh({
        element: f.current,
        padding: r
      }).fn(o) : {} : f ? Mh({
        element: f,
        padding: r
      }).fn(o) : {};
    }
  };
}, _1 = (i, c) => {
  const o = S1(i);
  return {
    name: o.name,
    fn: o.fn,
    options: [i, c]
  };
}, M1 = (i, c) => {
  const o = p1(i);
  return {
    name: o.name,
    fn: o.fn,
    options: [i, c]
  };
}, C1 = (i, c) => {
  const o = E1(i);
  return {
    name: o.name,
    fn: o.fn,
    options: [i, c]
  };
}, x1 = (i, c) => {
  const o = z1(i);
  return {
    name: o.name,
    fn: o.fn,
    options: [i, c]
  };
}, D1 = "data-floating-ui-focusable", xh = "active", Dh = "selected", N1 = {
  ...Gh
};
let Nh = !1, w1 = 0;
const wh = () => (
  // Ensure the id is unique with multiple independent versions of Floating UI
  // on <React 18
  "floating-ui-" + Math.random().toString(36).slice(2, 6) + w1++
);
function U1() {
  const [i, c] = L.useState(() => Nh ? wh() : void 0);
  return Yl(() => {
    i == null && c(wh());
  }, []), L.useEffect(() => {
    Nh = !0;
  }, []), i;
}
const H1 = N1.useId, $i = H1 || U1, B1 = /* @__PURE__ */ L.forwardRef(function(c, o) {
  const {
    context: {
      placement: f,
      elements: {
        floating: r
      },
      middlewareData: {
        arrow: m,
        shift: h
      }
    },
    width: p = 14,
    height: b = 7,
    tipRadius: v = 0,
    strokeWidth: M = 0,
    staticOffset: z,
    stroke: N,
    d: D,
    style: {
      transform: Y,
      ...q
    } = {},
    ...V
  } = c, X = $i(), [W, G] = L.useState(!1);
  if (Yl(() => {
    if (!r) return;
    pe(r).direction === "rtl" && G(!0);
  }, [r]), !r)
    return null;
  const [$, at] = f.split("-"), lt = $ === "top" || $ === "bottom";
  let Z = z;
  (lt && h != null && h.x || !lt && h != null && h.y) && (Z = null);
  const it = M * 2, zt = it / 2, Tt = p / 2 * (v / -8 + 1), _t = b / 2 * v / 4, dt = !!D, Ct = Z && at === "end" ? "bottom" : "top";
  let Q = Z && at === "end" ? "right" : "left";
  Z && W && (Q = at === "end" ? "left" : "right");
  const ft = (m == null ? void 0 : m.x) != null ? Z || m.x : "", R = (m == null ? void 0 : m.y) != null ? Z || m.y : "", B = D || "M0,0" + (" H" + p) + (" L" + (p - Tt) + "," + (b - _t)) + (" Q" + p / 2 + "," + b + " " + Tt + "," + (b - _t)) + " Z", H = {
    top: dt ? "rotate(180deg)" : "",
    left: dt ? "rotate(90deg)" : "rotate(-90deg)",
    bottom: dt ? "" : "rotate(180deg)",
    right: dt ? "rotate(-90deg)" : "rotate(90deg)"
  }[$];
  return /* @__PURE__ */ Vt.jsxs("svg", {
    ...V,
    "aria-hidden": !0,
    ref: o,
    width: dt ? p : p + it,
    height: p,
    viewBox: "0 0 " + p + " " + (b > p ? b : p),
    style: {
      position: "absolute",
      pointerEvents: "none",
      [Q]: ft,
      [Ct]: R,
      [$]: lt || dt ? "100%" : "calc(100% - " + it / 2 + "px)",
      transform: [H, Y].filter((F) => !!F).join(" "),
      ...q
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
function L1() {
  const i = /* @__PURE__ */ new Map();
  return {
    emit(c, o) {
      var f;
      (f = i.get(c)) == null || f.forEach((r) => r(o));
    },
    on(c, o) {
      i.has(c) || i.set(c, /* @__PURE__ */ new Set()), i.get(c).add(o);
    },
    off(c, o) {
      var f;
      (f = i.get(c)) == null || f.delete(o);
    }
  };
}
const q1 = /* @__PURE__ */ L.createContext(null), Y1 = /* @__PURE__ */ L.createContext(null), Ov = () => {
  var i;
  return ((i = L.useContext(q1)) == null ? void 0 : i.id) || null;
}, zv = () => L.useContext(Y1);
function Io(i) {
  return "data-floating-ui-" + i;
}
const j1 = {
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
}, Uh = /* @__PURE__ */ L.forwardRef(function(c, o) {
  const [f, r] = L.useState();
  Yl(() => {
    Hb() && r("button");
  }, []);
  const m = {
    ref: o,
    tabIndex: 0,
    // Role is only for VoiceOver
    role: f,
    "aria-hidden": f ? void 0 : !0,
    [Io("focus-guard")]: "",
    style: j1
  };
  return /* @__PURE__ */ Vt.jsx("span", {
    ...c,
    ...m
  });
}), X1 = {
  clipPath: "inset(50%)",
  position: "fixed",
  top: 0,
  left: 0
}, _v = /* @__PURE__ */ L.createContext(null), Hh = /* @__PURE__ */ Io("portal");
function G1(i) {
  i === void 0 && (i = {});
  const {
    id: c,
    root: o
  } = i, f = $i(), r = V1(), [m, h] = L.useState(null), p = L.useRef(null);
  return Yl(() => () => {
    m == null || m.remove(), queueMicrotask(() => {
      p.current = null;
    });
  }, [m]), Yl(() => {
    if (!f || p.current) return;
    const b = c ? document.getElementById(c) : null;
    if (!b) return;
    const v = document.createElement("div");
    v.id = f, v.setAttribute(Hh, ""), b.appendChild(v), p.current = v, h(v);
  }, [c, f]), Yl(() => {
    if (o === null || !f || p.current) return;
    let b = o || (r == null ? void 0 : r.portalNode);
    b && !Jo(b) && (b = b.current), b = b || document.body;
    let v = null;
    c && (v = document.createElement("div"), v.id = c, b.appendChild(v));
    const M = document.createElement("div");
    M.id = f, M.setAttribute(Hh, ""), b = v || b, b.appendChild(M), p.current = M, h(M);
  }, [c, o, f, r]), m;
}
function Q1(i) {
  const {
    children: c,
    id: o,
    root: f,
    preserveTabOrder: r = !0
  } = i, m = G1({
    id: o,
    root: f
  }), [h, p] = L.useState(null), b = L.useRef(null), v = L.useRef(null), M = L.useRef(null), z = L.useRef(null), N = h == null ? void 0 : h.modal, D = h == null ? void 0 : h.open, Y = (
    // The FocusManager and therefore floating element are currently open/
    // rendered.
    !!h && // Guards are only for non-modal focus management.
    !h.modal && // Don't render if unmount is transitioning.
    h.open && r && !!(f || m)
  );
  return L.useEffect(() => {
    if (!m || !r || N)
      return;
    function q(V) {
      m && zo(V) && (V.type === "focusin" ? Ah : Jb)(m);
    }
    return m.addEventListener("focusin", q, !0), m.addEventListener("focusout", q, !0), () => {
      m.removeEventListener("focusin", q, !0), m.removeEventListener("focusout", q, !0);
    };
  }, [m, r, N]), L.useEffect(() => {
    m && (D || Ah(m));
  }, [D, m]), /* @__PURE__ */ Vt.jsxs(_v.Provider, {
    value: L.useMemo(() => ({
      preserveTabOrder: r,
      beforeOutsideRef: b,
      afterOutsideRef: v,
      beforeInsideRef: M,
      afterInsideRef: z,
      portalNode: m,
      setFocusManagerState: p
    }), [r, m]),
    children: [Y && m && /* @__PURE__ */ Vt.jsx(Uh, {
      "data-type": "outside",
      ref: b,
      onFocus: (q) => {
        if (zo(q, m)) {
          var V;
          (V = M.current) == null || V.focus();
        } else {
          const X = h ? h.domReference : null, W = Kb(X);
          W == null || W.focus();
        }
      }
    }), Y && m && /* @__PURE__ */ Vt.jsx("span", {
      "aria-owns": m.id,
      style: X1
    }), m && /* @__PURE__ */ Zi.createPortal(c, m), Y && m && /* @__PURE__ */ Vt.jsx(Uh, {
      "data-type": "outside",
      ref: v,
      onFocus: (q) => {
        if (zo(q, m)) {
          var V;
          (V = z.current) == null || V.focus();
        } else {
          const X = h ? h.domReference : null, W = Zb(X);
          W == null || W.focus(), h != null && h.closeOnFocusOut && (h == null || h.onOpenChange(!1, q.nativeEvent, "focus-out"));
        }
      }
    })]
  });
}
const V1 = () => L.useContext(_v), Z1 = {
  pointerdown: "onPointerDown",
  mousedown: "onMouseDown",
  click: "onClick"
}, K1 = {
  pointerdown: "onPointerDownCapture",
  mousedown: "onMouseDownCapture",
  click: "onClickCapture"
}, Bh = (i) => {
  var c, o;
  return {
    escapeKey: typeof i == "boolean" ? i : (c = i == null ? void 0 : i.escapeKey) != null ? c : !1,
    outsidePress: typeof i == "boolean" ? i : (o = i == null ? void 0 : i.outsidePress) != null ? o : !0
  };
};
function J1(i, c) {
  c === void 0 && (c = {});
  const {
    open: o,
    onOpenChange: f,
    elements: r,
    dataRef: m
  } = i, {
    enabled: h = !0,
    escapeKey: p = !0,
    outsidePress: b = !0,
    outsidePressEvent: v = "pointerdown",
    referencePress: M = !1,
    referencePressEvent: z = "pointerdown",
    ancestorScroll: N = !1,
    bubbles: D,
    capture: Y
  } = c, q = zv(), V = Pn(typeof b == "function" ? b : () => !1), X = typeof b == "function" ? V : b, W = L.useRef(!1), {
    escapeKey: G,
    outsidePress: $
  } = Bh(D), {
    escapeKey: at,
    outsidePress: lt
  } = Bh(Y), Z = L.useRef(!1), it = Pn((Q) => {
    var ft;
    if (!o || !h || !p || Q.key !== "Escape" || Z.current)
      return;
    const R = (ft = m.current.floatingContext) == null ? void 0 : ft.nodeId, B = q ? Ni(q.nodesRef.current, R) : [];
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
    f(!1, Yb(Q) ? Q.nativeEvent : Q, "escape-key");
  }), zt = Pn((Q) => {
    var ft;
    const R = () => {
      var B;
      it(Q), (B = Pa(Q)) == null || B.removeEventListener("keydown", R);
    };
    (ft = Pa(Q)) == null || ft.addEventListener("keydown", R);
  }), Tt = Pn((Q) => {
    var ft;
    const R = m.current.insideReactTree;
    m.current.insideReactTree = !1;
    const B = W.current;
    if (W.current = !1, v === "click" && B || R || typeof X == "function" && !X(Q))
      return;
    const H = Pa(Q), F = "[" + Io("inert") + "]", et = uu(r.floating).querySelectorAll(F);
    let g = qt(H) ? H : null;
    for (; g && !fl(g); ) {
      const I = ol(g);
      if (fl(I) || !qt(I))
        break;
      g = I;
    }
    if (et.length && qt(H) && !Lb(H) && // Clicked on a direct ancestor (e.g. FloatingOverlay).
    !Yo(H, r.floating) && // If the target root element contains none of the markers, then the
    // element was injected after the floating element rendered.
    Array.from(et).every((I) => !Yo(g, I)))
      return;
    if (Xe(H) && Ct) {
      const I = fl(H), nt = pe(H), mt = /auto|scroll/, Jt = I || mt.test(nt.overflowX), wt = I || mt.test(nt.overflowY), jl = Jt && H.clientWidth > 0 && H.scrollWidth > H.clientWidth, hn = wt && H.clientHeight > 0 && H.scrollHeight > H.clientHeight, na = nt.direction === "rtl", ou = hn && (na ? Q.offsetX <= H.offsetWidth - H.clientWidth : Q.offsetX > H.clientWidth), Ge = jl && Q.offsetY > H.clientHeight;
      if (ou || Ge)
        return;
    }
    const U = (ft = m.current.floatingContext) == null ? void 0 : ft.nodeId, j = q && Ni(q.nodesRef.current, U).some((I) => {
      var nt;
      return Oo(Q, (nt = I.context) == null ? void 0 : nt.elements.floating);
    });
    if (Oo(Q, r.floating) || Oo(Q, r.domReference) || j)
      return;
    const K = q ? Ni(q.nodesRef.current, U) : [];
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
    f(!1, Q, "outside-press");
  }), _t = Pn((Q) => {
    var ft;
    const R = () => {
      var B;
      Tt(Q), (B = Pa(Q)) == null || B.removeEventListener(v, R);
    };
    (ft = Pa(Q)) == null || ft.addEventListener(v, R);
  });
  L.useEffect(() => {
    if (!o || !h)
      return;
    m.current.__escapeKeyBubbles = G, m.current.__outsidePressBubbles = $;
    let Q = -1;
    function ft(et) {
      f(!1, et, "ancestor-scroll");
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
        Wi() ? 5 : 0
      );
    }
    const H = uu(r.floating);
    p && (H.addEventListener("keydown", at ? zt : it, at), H.addEventListener("compositionstart", R), H.addEventListener("compositionend", B)), X && H.addEventListener(v, lt ? _t : Tt, lt);
    let F = [];
    return N && (qt(r.domReference) && (F = ql(r.domReference)), qt(r.floating) && (F = F.concat(ql(r.floating))), !qt(r.reference) && r.reference && r.reference.contextElement && (F = F.concat(ql(r.reference.contextElement)))), F = F.filter((et) => {
      var g;
      return et !== ((g = H.defaultView) == null ? void 0 : g.visualViewport);
    }), F.forEach((et) => {
      et.addEventListener("scroll", ft, {
        passive: !0
      });
    }), () => {
      p && (H.removeEventListener("keydown", at ? zt : it, at), H.removeEventListener("compositionstart", R), H.removeEventListener("compositionend", B)), X && H.removeEventListener(v, lt ? _t : Tt, lt), F.forEach((et) => {
        et.removeEventListener("scroll", ft);
      }), window.clearTimeout(Q);
    };
  }, [m, r, p, X, v, o, f, N, h, G, $, it, at, zt, Tt, lt, _t]), L.useEffect(() => {
    m.current.insideReactTree = !1;
  }, [m, X, v]);
  const dt = L.useMemo(() => ({
    onKeyDown: it,
    ...M && {
      [Z1[z]]: (Q) => {
        f(!1, Q.nativeEvent, "reference-press");
      },
      ...z !== "click" && {
        onClick(Q) {
          f(!1, Q.nativeEvent, "reference-press");
        }
      }
    }
  }), [it, f, M, z]), Ct = L.useMemo(() => {
    function Q(ft) {
      ft.button === 0 && (W.current = !0);
    }
    return {
      onKeyDown: it,
      onMouseDown: Q,
      onMouseUp: Q,
      [K1[v]]: () => {
        m.current.insideReactTree = !0;
      }
    };
  }, [it, v, m]);
  return L.useMemo(() => h ? {
    reference: dt,
    floating: Ct
  } : {}, [h, dt, Ct]);
}
function W1(i) {
  const {
    open: c = !1,
    onOpenChange: o,
    elements: f
  } = i, r = $i(), m = L.useRef({}), [h] = L.useState(() => L1()), p = Ov() != null, [b, v] = L.useState(f.reference), M = Pn((D, Y, q) => {
    m.current.openEvent = D ? Y : void 0, h.emit("openchange", {
      open: D,
      event: Y,
      reason: q,
      nested: p
    }), o == null || o(D, Y, q);
  }), z = L.useMemo(() => ({
    setPositionReference: v
  }), []), N = L.useMemo(() => ({
    reference: b || f.reference || null,
    floating: f.floating || null,
    domReference: f.reference
  }), [b, f.reference, f.floating]);
  return L.useMemo(() => ({
    dataRef: m,
    open: c,
    onOpenChange: M,
    elements: N,
    events: h,
    floatingId: r,
    refs: z
  }), [c, M, N, h, r, z]);
}
function k1(i) {
  i === void 0 && (i = {});
  const {
    nodeId: c
  } = i, o = W1({
    ...i,
    elements: {
      reference: null,
      floating: null,
      ...i.elements
    }
  }), f = i.rootContext || o, r = f.elements, [m, h] = L.useState(null), [p, b] = L.useState(null), M = (r == null ? void 0 : r.domReference) || m, z = L.useRef(null), N = zv();
  Yl(() => {
    M && (z.current = M);
  }, [M]);
  const D = O1({
    ...i,
    elements: {
      ...r,
      ...p && {
        reference: p
      }
    }
  }), Y = L.useCallback((G) => {
    const $ = qt(G) ? {
      getBoundingClientRect: () => G.getBoundingClientRect(),
      getClientRects: () => G.getClientRects(),
      contextElement: G
    } : G;
    b($), D.refs.setReference($);
  }, [D.refs]), q = L.useCallback((G) => {
    (qt(G) || G === null) && (z.current = G, h(G)), (qt(D.refs.reference.current) || D.refs.reference.current === null || // Don't allow setting virtual elements using the old technique back to
    // `null` to support `positionReference` + an unstable `reference`
    // callback ref.
    G !== null && !qt(G)) && D.refs.setReference(G);
  }, [D.refs]), V = L.useMemo(() => ({
    ...D.refs,
    setReference: q,
    setPositionReference: Y,
    domReference: z
  }), [D.refs, q, Y]), X = L.useMemo(() => ({
    ...D.elements,
    domReference: M
  }), [D.elements, M]), W = L.useMemo(() => ({
    ...D,
    ...f,
    refs: V,
    elements: X,
    nodeId: c
  }), [D, V, X, c, f]);
  return Yl(() => {
    f.dataRef.current.floatingContext = W;
    const G = N == null ? void 0 : N.nodesRef.current.find(($) => $.id === c);
    G && (G.context = W);
  }), L.useMemo(() => ({
    ...D,
    context: W,
    refs: V,
    elements: X
  }), [D, V, X, W]);
}
function Co(i, c, o) {
  const f = /* @__PURE__ */ new Map(), r = o === "item";
  let m = i;
  if (r && i) {
    const {
      [xh]: h,
      [Dh]: p,
      ...b
    } = i;
    m = b;
  }
  return {
    ...o === "floating" && {
      tabIndex: -1,
      [D1]: ""
    },
    ...m,
    ...c.map((h) => {
      const p = h ? h[o] : null;
      return typeof p == "function" ? i ? p(i) : null : p;
    }).concat(i).reduce((h, p) => (p && Object.entries(p).forEach((b) => {
      let [v, M] = b;
      if (!(r && [xh, Dh].includes(v)))
        if (v.indexOf("on") === 0) {
          if (f.has(v) || f.set(v, []), typeof M == "function") {
            var z;
            (z = f.get(v)) == null || z.push(M), h[v] = function() {
              for (var N, D = arguments.length, Y = new Array(D), q = 0; q < D; q++)
                Y[q] = arguments[q];
              return (N = f.get(v)) == null ? void 0 : N.map((V) => V(...Y)).find((V) => V !== void 0);
            };
          }
        } else
          h[v] = M;
    }), h), {})
  };
}
function F1(i) {
  i === void 0 && (i = []);
  const c = i.map((p) => p == null ? void 0 : p.reference), o = i.map((p) => p == null ? void 0 : p.floating), f = i.map((p) => p == null ? void 0 : p.item), r = L.useCallback(
    (p) => Co(p, i, "reference"),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    c
  ), m = L.useCallback(
    (p) => Co(p, i, "floating"),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    o
  ), h = L.useCallback(
    (p) => Co(p, i, "item"),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    f
  );
  return L.useMemo(() => ({
    getReferenceProps: r,
    getFloatingProps: m,
    getItemProps: h
  }), [r, m, h]);
}
const $1 = /* @__PURE__ */ new Map([["select", "listbox"], ["combobox", "listbox"], ["label", !1]]);
function I1(i, c) {
  var o, f;
  c === void 0 && (c = {});
  const {
    open: r,
    elements: m,
    floatingId: h
  } = i, {
    enabled: p = !0,
    role: b = "dialog"
  } = c, v = $i(), M = ((o = m.domReference) == null ? void 0 : o.id) || v, z = L.useMemo(() => {
    var W;
    return ((W = qb(m.floating)) == null ? void 0 : W.id) || h;
  }, [m.floating, h]), N = (f = $1.get(b)) != null ? f : b, Y = Ov() != null, q = L.useMemo(() => N === "tooltip" || b === "label" ? {
    ["aria-" + (b === "label" ? "labelledby" : "describedby")]: r ? z : void 0
  } : {
    "aria-expanded": r ? "true" : "false",
    "aria-haspopup": N === "alertdialog" ? "dialog" : N,
    "aria-controls": r ? z : void 0,
    ...N === "listbox" && {
      role: "combobox"
    },
    ...N === "menu" && {
      id: M
    },
    ...N === "menu" && Y && {
      role: "menuitem"
    },
    ...b === "select" && {
      "aria-autocomplete": "none"
    },
    ...b === "combobox" && {
      "aria-autocomplete": "list"
    }
  }, [N, z, Y, r, M, b]), V = L.useMemo(() => {
    const W = {
      id: z,
      ...N && {
        role: N
      }
    };
    return N === "tooltip" || b === "label" ? W : {
      ...W,
      ...N === "menu" && {
        "aria-labelledby": M
      }
    };
  }, [N, z, M, b]), X = L.useCallback((W) => {
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
    switch (b) {
      case "select":
      case "combobox":
        return {
          ...at,
          "aria-selected": $
        };
    }
    return {};
  }, [z, b]);
  return L.useMemo(() => p ? {
    reference: q,
    floating: V,
    item: X
  } : {}, [p, q, V, X]);
}
function P1(i) {
  const { anchor: c, data: o, portalRoot: f, onClose: r, onEnter: m, onLeave: h } = i, p = te.useRef(null), { refs: b, floatingStyles: v, context: M } = k1({
    open: !0,
    onOpenChange: (Y) => {
      Y || r();
    },
    placement: "top",
    elements: { reference: c },
    middleware: [_1(10), C1(), M1({ padding: 8 }), x1({ element: p })],
    whileElementsMounted: b1
  }), z = J1(M, { outsidePress: !0, escapeKey: !0 }), N = I1(M, { role: "tooltip" }), { getFloatingProps: D } = F1([z, N]);
  return /* @__PURE__ */ Vt.jsx(Q1, { root: f, children: /* @__PURE__ */ Vt.jsxs(
    "div",
    {
      ref: b.setFloating,
      style: o.interactive ? v : { ...v, pointerEvents: "none" },
      className: "tl-tooltip-popover" + (o.interactive ? " tl-tooltip-popover--interactive" : ""),
      ...D(),
      onPointerEnter: m,
      onPointerLeave: h,
      children: [
        o.caption ? /* @__PURE__ */ Vt.jsx("div", { className: "tl-tooltip-caption", children: o.caption }) : null,
        o.html != null ? /* @__PURE__ */ Vt.jsx(
          "div",
          {
            className: "tl-tooltip-body",
            dangerouslySetInnerHTML: { __html: o.html }
          }
        ) : /* @__PURE__ */ Vt.jsx("div", { className: "tl-tooltip-body", children: o.text ?? "" }),
        /* @__PURE__ */ Vt.jsx(
          B1,
          {
            ref: p,
            context: M,
            className: "tl-tooltip-arrow",
            width: 12,
            height: 6
          }
        )
      ]
    }
  ) });
}
const tS = 400, eS = 150, lS = 400, Mi = /* @__PURE__ */ new Map();
let cl = null, Ui = null, eu = null, lu = null, Ll = null;
function Lh() {
  cl || (cl = document.createElement("div"), cl.id = "tl-tooltip-host", document.body.appendChild(cl), Ui = $h.createRoot(cl), Ii(), document.addEventListener("pointerover", nS, !0), document.addEventListener("pointerout", aS, !0));
}
function nS(i) {
  const c = i.target;
  if (!c) return;
  const o = Mv(c);
  if (!o) return;
  Nv(), Dv();
  const f = iS(o, c);
  if (!f) return;
  const r = o.kind === "dynamic" ? c : o.el;
  cS(r, f);
}
function aS(i) {
  const c = i.relatedTarget;
  c && cl && cl.contains(c) || c && Mv(c) || (Dv(), xv());
}
function Mv(i) {
  var o;
  let c = i;
  for (; c; ) {
    const f = (o = c.getAttribute) == null ? void 0 : o.call(c, "data-tooltip");
    if (f != null) {
      const r = uS(f, c);
      if (r) return r;
    }
    c = c.parentElement;
  }
  return null;
}
function uS(i, c) {
  if (i === "dynamic") return { kind: "dynamic", host: c };
  const o = i.indexOf(":");
  if (o < 0) return null;
  const f = i.substring(0, o), r = i.substring(o + 1);
  switch (f) {
    case "text":
      return { kind: "text", text: r, el: c };
    case "html":
      return { kind: "html", html: r, el: c };
    case "key": {
      const m = Cv(c);
      return m ? { kind: "key", controlId: m, key: r, el: c } : null;
    }
    default:
      return null;
  }
}
function Cv(i) {
  let c = i;
  for (; c; ) {
    const o = c.id;
    if (o && eb(o)) return o;
    c = c.parentElement;
  }
  return null;
}
function iS(i, c) {
  switch (i.kind) {
    case "text":
      return Promise.resolve({ text: i.text });
    case "html":
      return Promise.resolve({ html: i.html });
    case "key": {
      const o = i.controlId + "\0" + i.key;
      let f = Mi.get(o);
      return f || (f = qh(i.controlId, i.key), Mi.set(o, f)), f;
    }
    case "dynamic": {
      const o = { target: c, resolved: null };
      i.host.dispatchEvent(new CustomEvent("tl-tooltip-resolve", { detail: o, bubbles: !1 }));
      const f = o.resolved;
      if (!f) return null;
      if ("inline" in f) return Promise.resolve(f.inline);
      const r = Cv(i.host);
      if (!r) return null;
      const m = r + "\0" + f.key;
      let h = Mi.get(m);
      return h || (h = qh(r, f.key), Mi.set(m, h)), h;
    }
  }
}
function cS(i, c) {
  eu = window.setTimeout(async () => {
    eu = null;
    const o = await c;
    o && document.contains(i) && (Ll = { anchor: i, data: o }, Ii());
  }, tS);
}
function xv() {
  const i = Ll != null && Ll.data.interactive ? lS : eS;
  lu = window.setTimeout(() => {
    lu = null, Ll = null, Ii();
  }, i);
}
function Dv() {
  eu != null && (window.clearTimeout(eu), eu = null);
}
function Nv() {
  lu != null && (window.clearTimeout(lu), lu = null);
}
function Ii() {
  if (!Ui || !cl) return;
  if (!Ll) {
    Ui.render(null);
    return;
  }
  const { anchor: i, data: c } = Ll;
  Ui.render(
    te.createElement(P1, {
      anchor: i,
      data: c,
      portalRoot: cl,
      onClose: () => {
        Ll = null, Ii();
      },
      onEnter: Nv,
      onLeave: xv
    })
  );
}
async function qh(i, c) {
  const o = document.body.dataset.windowName ?? "", f = on() + `react-api/tooltip?controlId=${encodeURIComponent(i)}&key=${encodeURIComponent(c)}&windowName=${encodeURIComponent(o)}`, r = await fetch(f, { credentials: "same-origin" });
  return r.ok ? await r.json() : null;
}
window.TLReact = { mount: Ko, mountField: tb, discoverAndMount: Li, subscribe: Zo, unsubscribe: Wh };
gg();
pg();
document.readyState === "loading" ? window.addEventListener("DOMContentLoaded", () => Lh(), { once: !0 }) : Lh();
Kg();
Pg();
export {
  hS as KeyboardScopeProvider,
  te as React,
  sS as ReactDOM,
  bS as TLChild,
  mn as TLControlContext,
  ah as connect,
  lb as createChildContext,
  Li as discoverAndMount,
  jh as getComponent,
  eb as isMountedControl,
  Ko as mount,
  tb as mountField,
  fS as register,
  Zo as subscribe,
  Bi as unmount,
  Wh as unsubscribe,
  gS as useFocusTrap,
  oS as useI18N,
  vS as useKeyboardBinding,
  yS as useStandaloneKeyboardScope,
  ab as useTLCommand,
  dS as useTLDataUrl,
  mS as useTLFieldValue,
  nb as useTLState,
  rS as useTLUpload
};
