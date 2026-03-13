const F0 = /* @__PURE__ */ new Map();
function yh(o, g) {
  F0.set(o, g);
}
function k0(o) {
  return F0.get(o);
}
function I0(o) {
  return o && o.__esModule && Object.prototype.hasOwnProperty.call(o, "default") ? o.default : o;
}
var gi = { exports: {} }, j = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var q0;
function Cv() {
  if (q0) return j;
  q0 = 1;
  var o = Symbol.for("react.transitional.element"), g = Symbol.for("react.portal"), T = Symbol.for("react.fragment"), y = Symbol.for("react.strict_mode"), D = Symbol.for("react.profiler"), Z = Symbol.for("react.consumer"), K = Symbol.for("react.context"), gl = Symbol.for("react.forward_ref"), H = Symbol.for("react.suspense"), p = Symbol.for("react.memo"), F = Symbol.for("react.lazy"), q = Symbol.for("react.activity"), ll = Symbol.iterator;
  function Wl(d) {
    return d === null || typeof d != "object" ? null : (d = ll && d[ll] || d["@@iterator"], typeof d == "function" ? d : null);
  }
  var jl = {
    isMounted: function() {
      return !1;
    },
    enqueueForceUpdate: function() {
    },
    enqueueReplaceState: function() {
    },
    enqueueSetState: function() {
    }
  }, Cl = Object.assign, Dt = {};
  function $l(d, _, M) {
    this.props = d, this.context = _, this.refs = Dt, this.updater = M || jl;
  }
  $l.prototype.isReactComponent = {}, $l.prototype.setState = function(d, _) {
    if (typeof d != "object" && typeof d != "function" && d != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, d, _, "setState");
  }, $l.prototype.forceUpdate = function(d) {
    this.updater.enqueueForceUpdate(this, d, "forceUpdate");
  };
  function Wt() {
  }
  Wt.prototype = $l.prototype;
  function Rl(d, _, M) {
    this.props = d, this.context = _, this.refs = Dt, this.updater = M || jl;
  }
  var ct = Rl.prototype = new Wt();
  ct.constructor = Rl, Cl(ct, $l.prototype), ct.isPureReactComponent = !0;
  var Et = Array.isArray;
  function Gl() {
  }
  var W = { H: null, A: null, T: null, S: null }, Xl = Object.prototype.hasOwnProperty;
  function zt(d, _, M) {
    var N = M.ref;
    return {
      $$typeof: o,
      type: d,
      key: _,
      ref: N !== void 0 ? N : null,
      props: M
    };
  }
  function Va(d, _) {
    return zt(d.type, _, d.props);
  }
  function At(d) {
    return typeof d == "object" && d !== null && d.$$typeof === o;
  }
  function Ql(d) {
    var _ = { "=": "=0", ":": "=2" };
    return "$" + d.replace(/[=:]/g, function(M) {
      return _[M];
    });
  }
  var za = /\/+/g;
  function Ut(d, _) {
    return typeof d == "object" && d !== null && d.key != null ? Ql("" + d.key) : _.toString(36);
  }
  function rt(d) {
    switch (d.status) {
      case "fulfilled":
        return d.value;
      case "rejected":
        throw d.reason;
      default:
        switch (typeof d.status == "string" ? d.then(Gl, Gl) : (d.status = "pending", d.then(
          function(_) {
            d.status === "pending" && (d.status = "fulfilled", d.value = _);
          },
          function(_) {
            d.status === "pending" && (d.status = "rejected", d.reason = _);
          }
        )), d.status) {
          case "fulfilled":
            return d.value;
          case "rejected":
            throw d.reason;
        }
    }
    throw d;
  }
  function E(d, _, M, N, G) {
    var L = typeof d;
    (L === "undefined" || L === "boolean") && (d = null);
    var tl = !1;
    if (d === null) tl = !0;
    else
      switch (L) {
        case "bigint":
        case "string":
        case "number":
          tl = !0;
          break;
        case "object":
          switch (d.$$typeof) {
            case o:
            case g:
              tl = !0;
              break;
            case F:
              return tl = d._init, E(
                tl(d._payload),
                _,
                M,
                N,
                G
              );
          }
      }
    if (tl)
      return G = G(d), tl = N === "" ? "." + Ut(d, 0) : N, Et(G) ? (M = "", tl != null && (M = tl.replace(za, "$&/") + "/"), E(G, _, M, "", function(Ru) {
        return Ru;
      })) : G != null && (At(G) && (G = Va(
        G,
        M + (G.key == null || d && d.key === G.key ? "" : ("" + G.key).replace(
          za,
          "$&/"
        ) + "/") + tl
      )), _.push(G)), 1;
    tl = 0;
    var Bl = N === "" ? "." : N + ":";
    if (Et(d))
      for (var hl = 0; hl < d.length; hl++)
        N = d[hl], L = Bl + Ut(N, hl), tl += E(
          N,
          _,
          M,
          L,
          G
        );
    else if (hl = Wl(d), typeof hl == "function")
      for (d = hl.call(d), hl = 0; !(N = d.next()).done; )
        N = N.value, L = Bl + Ut(N, hl++), tl += E(
          N,
          _,
          M,
          L,
          G
        );
    else if (L === "object") {
      if (typeof d.then == "function")
        return E(
          rt(d),
          _,
          M,
          N,
          G
        );
      throw _ = String(d), Error(
        "Objects are not valid as a React child (found: " + (_ === "[object Object]" ? "object with keys {" + Object.keys(d).join(", ") + "}" : _) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return tl;
  }
  function O(d, _, M) {
    if (d == null) return d;
    var N = [], G = 0;
    return E(d, N, "", "", function(L) {
      return _.call(M, L, G++);
    }), N;
  }
  function Y(d) {
    if (d._status === -1) {
      var _ = d._result;
      _ = _(), _.then(
        function(M) {
          (d._status === 0 || d._status === -1) && (d._status = 1, d._result = M);
        },
        function(M) {
          (d._status === 0 || d._status === -1) && (d._status = 2, d._result = M);
        }
      ), d._status === -1 && (d._status = 0, d._result = _);
    }
    if (d._status === 1) return d._result.default;
    throw d._result;
  }
  var el = typeof reportError == "function" ? reportError : function(d) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var _ = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof d == "object" && d !== null && typeof d.message == "string" ? String(d.message) : String(d),
        error: d
      });
      if (!window.dispatchEvent(_)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", d);
      return;
    }
    console.error(d);
  }, il = {
    map: O,
    forEach: function(d, _, M) {
      O(
        d,
        function() {
          _.apply(this, arguments);
        },
        M
      );
    },
    count: function(d) {
      var _ = 0;
      return O(d, function() {
        _++;
      }), _;
    },
    toArray: function(d) {
      return O(d, function(_) {
        return _;
      }) || [];
    },
    only: function(d) {
      if (!At(d))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return d;
    }
  };
  return j.Activity = q, j.Children = il, j.Component = $l, j.Fragment = T, j.Profiler = D, j.PureComponent = Rl, j.StrictMode = y, j.Suspense = H, j.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = W, j.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(d) {
      return W.H.useMemoCache(d);
    }
  }, j.cache = function(d) {
    return function() {
      return d.apply(null, arguments);
    };
  }, j.cacheSignal = function() {
    return null;
  }, j.cloneElement = function(d, _, M) {
    if (d == null)
      throw Error(
        "The argument must be a React element, but you passed " + d + "."
      );
    var N = Cl({}, d.props), G = d.key;
    if (_ != null)
      for (L in _.key !== void 0 && (G = "" + _.key), _)
        !Xl.call(_, L) || L === "key" || L === "__self" || L === "__source" || L === "ref" && _.ref === void 0 || (N[L] = _[L]);
    var L = arguments.length - 2;
    if (L === 1) N.children = M;
    else if (1 < L) {
      for (var tl = Array(L), Bl = 0; Bl < L; Bl++)
        tl[Bl] = arguments[Bl + 2];
      N.children = tl;
    }
    return zt(d.type, G, N);
  }, j.createContext = function(d) {
    return d = {
      $$typeof: K,
      _currentValue: d,
      _currentValue2: d,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, d.Provider = d, d.Consumer = {
      $$typeof: Z,
      _context: d
    }, d;
  }, j.createElement = function(d, _, M) {
    var N, G = {}, L = null;
    if (_ != null)
      for (N in _.key !== void 0 && (L = "" + _.key), _)
        Xl.call(_, N) && N !== "key" && N !== "__self" && N !== "__source" && (G[N] = _[N]);
    var tl = arguments.length - 2;
    if (tl === 1) G.children = M;
    else if (1 < tl) {
      for (var Bl = Array(tl), hl = 0; hl < tl; hl++)
        Bl[hl] = arguments[hl + 2];
      G.children = Bl;
    }
    if (d && d.defaultProps)
      for (N in tl = d.defaultProps, tl)
        G[N] === void 0 && (G[N] = tl[N]);
    return zt(d, L, G);
  }, j.createRef = function() {
    return { current: null };
  }, j.forwardRef = function(d) {
    return { $$typeof: gl, render: d };
  }, j.isValidElement = At, j.lazy = function(d) {
    return {
      $$typeof: F,
      _payload: { _status: -1, _result: d },
      _init: Y
    };
  }, j.memo = function(d, _) {
    return {
      $$typeof: p,
      type: d,
      compare: _ === void 0 ? null : _
    };
  }, j.startTransition = function(d) {
    var _ = W.T, M = {};
    W.T = M;
    try {
      var N = d(), G = W.S;
      G !== null && G(M, N), typeof N == "object" && N !== null && typeof N.then == "function" && N.then(Gl, el);
    } catch (L) {
      el(L);
    } finally {
      _ !== null && M.types !== null && (_.types = M.types), W.T = _;
    }
  }, j.unstable_useCacheRefresh = function() {
    return W.H.useCacheRefresh();
  }, j.use = function(d) {
    return W.H.use(d);
  }, j.useActionState = function(d, _, M) {
    return W.H.useActionState(d, _, M);
  }, j.useCallback = function(d, _) {
    return W.H.useCallback(d, _);
  }, j.useContext = function(d) {
    return W.H.useContext(d);
  }, j.useDebugValue = function() {
  }, j.useDeferredValue = function(d, _) {
    return W.H.useDeferredValue(d, _);
  }, j.useEffect = function(d, _) {
    return W.H.useEffect(d, _);
  }, j.useEffectEvent = function(d) {
    return W.H.useEffectEvent(d);
  }, j.useId = function() {
    return W.H.useId();
  }, j.useImperativeHandle = function(d, _, M) {
    return W.H.useImperativeHandle(d, _, M);
  }, j.useInsertionEffect = function(d, _) {
    return W.H.useInsertionEffect(d, _);
  }, j.useLayoutEffect = function(d, _) {
    return W.H.useLayoutEffect(d, _);
  }, j.useMemo = function(d, _) {
    return W.H.useMemo(d, _);
  }, j.useOptimistic = function(d, _) {
    return W.H.useOptimistic(d, _);
  }, j.useReducer = function(d, _, M) {
    return W.H.useReducer(d, _, M);
  }, j.useRef = function(d) {
    return W.H.useRef(d);
  }, j.useState = function(d) {
    return W.H.useState(d);
  }, j.useSyncExternalStore = function(d, _, M) {
    return W.H.useSyncExternalStore(
      d,
      _,
      M
    );
  }, j.useTransition = function() {
    return W.H.useTransition();
  }, j.version = "19.2.4", j;
}
var Y0;
function Ui() {
  return Y0 || (Y0 = 1, gi.exports = Cv()), gi.exports;
}
var Yl = Ui();
const Wn = /* @__PURE__ */ I0(Yl), Ni = /* @__PURE__ */ new Map(), $n = /* @__PURE__ */ new Set();
let Oi = !1, Ri = 0;
const Mi = /* @__PURE__ */ new Set();
let P0 = "", ly = "";
function Bv(o) {
  P0 = o;
}
function qv(o) {
  ly = o;
}
function ty() {
  for (const o of Mi)
    o();
}
function Yv(o) {
  return Mi.add(o), () => Mi.delete(o);
}
function jv() {
  return Ri;
}
function Gv(o) {
  $n.add(o), Oi || (Oi = !0, queueMicrotask(Xv));
}
async function Xv() {
  if (Oi = !1, $n.size === 0)
    return;
  const o = Array.from($n);
  $n.clear();
  try {
    const g = P0 + "react-api/i18n?keys=" + encodeURIComponent(o.join(",")) + "&windowName=" + encodeURIComponent(ly), T = await fetch(g);
    if (!T.ok) {
      console.error("[TLReact] i18n fetch failed:", T.status);
      return;
    }
    const y = await T.json();
    for (const [D, Z] of Object.entries(y))
      Ni.set(D, Z);
    Ri++, ty();
  } catch (g) {
    console.error("[TLReact] i18n fetch error:", g);
  }
}
function mh(o) {
  Wn.useSyncExternalStore(Yv, jv);
  const g = {};
  for (const [T, y] of Object.entries(o)) {
    const D = Ni.get(T);
    D !== void 0 ? g[T] = D : (g[T] = y, Gv(T));
  }
  return g;
}
function ay() {
  Ni.clear(), Ri++, ty();
}
const La = /* @__PURE__ */ new Map();
let _e = null;
function Nu() {
  return document.body.dataset.windowName ?? "";
}
function In() {
  return document.body.dataset.contextPath ?? "";
}
function Qv(o) {
  const g = [];
  return o.width > 0 && g.push(`width=${o.width}`), o.height > 0 && g.push(`height=${o.height}`), g.push(`resizable=${o.resizable ? "yes" : "no"}`), g.join(",");
}
function Zv() {
  _e === null && (_e = setInterval(() => {
    for (const [o, g] of La)
      g.closed && (La.delete(o), Lv(o));
    La.size === 0 && _e !== null && (clearInterval(_e), _e = null);
  }, 2e3));
}
function Lv(o) {
  const g = In(), T = Nu();
  fetch(`${g}/react-api/command`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      controlId: "",
      command: "windowClosed",
      windowName: T,
      arguments: { windowId: o }
    })
  }).catch(() => {
  });
}
function xv(o) {
  if (o.targetWindowId !== Nu()) return;
  const T = `${In()}/view/${o.windowId}/`, y = window.open(T, o.windowId, Qv(o));
  y ? (La.set(o.windowId, y), Zv()) : Jv(o.windowId);
}
function Vv(o) {
  if (o.targetWindowId !== Nu()) return;
  const g = La.get(o.windowId);
  g && (g.close(), La.delete(o.windowId));
}
function Kv(o) {
  if (o.targetWindowId !== Nu()) return;
  const g = La.get(o.windowId);
  g && !g.closed && g.focus();
}
function Jv(o) {
  const g = In(), T = Nu();
  fetch(`${g}/react-api/command`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      controlId: "",
      command: "windowBlocked",
      windowName: T,
      arguments: { windowId: o }
    })
  }).catch(() => {
  });
}
function wv() {
  window.addEventListener("beforeunload", () => {
    const o = In(), g = Nu();
    if (!g) return;
    const T = JSON.stringify({
      controlId: "",
      command: "windowClosed",
      windowName: g,
      arguments: { windowId: g }
    }), y = new Blob([T], { type: "application/json" });
    navigator.sendBeacon(`${o}/react-api/command`, y);
  });
}
const De = /* @__PURE__ */ new Map();
let pe = null, j0 = null, Fn = 0, bi = null;
const Wv = 45e3, $v = 15e3;
function G0(o) {
  j0 = o, bi && clearInterval(bi), X0(o), bi = setInterval(() => {
    Fn > 0 && Date.now() - Fn > Wv && (console.warn("[TLReact] No heartbeat received, reconnecting SSE."), X0(j0));
  }, $v);
}
function X0(o) {
  pe && pe.close(), pe = new EventSource(o), Fn = Date.now(), ay(), pe.onmessage = (g) => {
    Fn = Date.now();
    try {
      const T = JSON.parse(g.data);
      kv(T);
    } catch (T) {
      console.error("[TLReact] Failed to parse SSE event:", T);
    }
  }, pe.onerror = () => {
    console.warn("[TLReact] SSE connection error, will reconnect automatically.");
  };
}
function uy(o, g) {
  let T = De.get(o);
  T || (T = /* @__PURE__ */ new Set(), De.set(o, T)), T.add(g);
}
function Fv(o, g) {
  const T = De.get(o);
  T && (T.delete(g), T.size === 0 && De.delete(o));
}
function ey(o, g) {
  const T = De.get(o);
  if (T)
    for (const y of T)
      y(g);
}
function kv(o) {
  if (!Array.isArray(o) || o.length < 2) {
    console.warn("[TLReact] Unexpected SSE event format:", o);
    return;
  }
  const g = o[0], T = o[1];
  switch (g) {
    case "Heartbeat":
      break;
    case "StateEvent":
      Iv(T);
      break;
    case "PatchEvent":
      Pv(T);
      break;
    case "ContentReplacement":
      Ta.contentReplacement(T);
      break;
    case "ElementReplacement":
      Ta.elementReplacement(T);
      break;
    case "PropertyUpdate":
      Ta.propertyUpdate(T);
      break;
    case "CssClassUpdate":
      Ta.cssClassUpdate(T);
      break;
    case "FragmentInsertion":
      Ta.fragmentInsertion(T);
      break;
    case "RangeReplacement":
      Ta.rangeReplacement(T);
      break;
    case "JSSnipplet":
      Ta.jsSnipplet(T);
      break;
    case "FunctionCall":
      Ta.functionCall(T);
      break;
    case "I18NCacheInvalidation":
      ay();
      break;
    case "WindowOpenEvent":
      xv(T);
      break;
    case "WindowCloseEvent":
      Vv(T);
      break;
    case "WindowFocusEvent":
      Kv(T);
      break;
    default:
      console.warn("[TLReact] Unknown SSE event type:", g);
  }
}
function Iv(o) {
  const g = JSON.parse(o.state);
  ey(o.controlId, g);
}
function Pv(o) {
  const g = JSON.parse(o.patch);
  ey(o.controlId, g);
}
const Ta = {
  contentReplacement(o) {
    const g = document.getElementById(o.elementId);
    g && (g.innerHTML = o.html);
  },
  elementReplacement(o) {
    const g = document.getElementById(o.elementId);
    g && (g.outerHTML = o.html);
  },
  propertyUpdate(o) {
    const g = document.getElementById(o.elementId);
    if (g)
      for (const T of o.properties)
        g.setAttribute(T.name, T.value);
  },
  cssClassUpdate(o) {
    const g = document.getElementById(o.elementId);
    g && (g.className = o.cssClass);
  },
  fragmentInsertion(o) {
    const g = document.getElementById(o.elementId);
    g && g.insertAdjacentHTML(o.position, o.html);
  },
  rangeReplacement(o) {
    const g = document.getElementById(o.startId), T = document.getElementById(o.stopId);
    if (g && T && g.parentNode) {
      const y = g.parentNode, D = document.createRange();
      D.setStartBefore(g), D.setEndAfter(T), D.deleteContents();
      const Z = document.createElement("template");
      Z.innerHTML = o.html, y.insertBefore(Z.content, D.startContainer.childNodes[D.startOffset] || null);
    }
  },
  jsSnipplet(o) {
    try {
      (0, eval)(o.code);
    } catch (g) {
      console.error("[TLReact] Error executing JS snippet:", g);
    }
  },
  functionCall(o) {
    try {
      const g = JSON.parse(o.arguments), T = o.functionRef ? window[o.functionRef] : window, y = T == null ? void 0 : T[o.functionName];
      typeof y == "function" ? y.apply(T, g) : console.warn("[TLReact] Function not found:", o.functionRef + "." + o.functionName);
    } catch (g) {
      console.error("[TLReact] Error executing function call:", g);
    }
  }
};
var Ti = { exports: {} }, Oe = {}, Ei = { exports: {} }, zi = {};
/**
 * @license React
 * scheduler.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var Q0;
function lh() {
  return Q0 || (Q0 = 1, (function(o) {
    function g(E, O) {
      var Y = E.length;
      E.push(O);
      l: for (; 0 < Y; ) {
        var el = Y - 1 >>> 1, il = E[el];
        if (0 < D(il, O))
          E[el] = O, E[Y] = il, Y = el;
        else break l;
      }
    }
    function T(E) {
      return E.length === 0 ? null : E[0];
    }
    function y(E) {
      if (E.length === 0) return null;
      var O = E[0], Y = E.pop();
      if (Y !== O) {
        E[0] = Y;
        l: for (var el = 0, il = E.length, d = il >>> 1; el < d; ) {
          var _ = 2 * (el + 1) - 1, M = E[_], N = _ + 1, G = E[N];
          if (0 > D(M, Y))
            N < il && 0 > D(G, M) ? (E[el] = G, E[N] = Y, el = N) : (E[el] = M, E[_] = Y, el = _);
          else if (N < il && 0 > D(G, Y))
            E[el] = G, E[N] = Y, el = N;
          else break l;
        }
      }
      return O;
    }
    function D(E, O) {
      var Y = E.sortIndex - O.sortIndex;
      return Y !== 0 ? Y : E.id - O.id;
    }
    if (o.unstable_now = void 0, typeof performance == "object" && typeof performance.now == "function") {
      var Z = performance;
      o.unstable_now = function() {
        return Z.now();
      };
    } else {
      var K = Date, gl = K.now();
      o.unstable_now = function() {
        return K.now() - gl;
      };
    }
    var H = [], p = [], F = 1, q = null, ll = 3, Wl = !1, jl = !1, Cl = !1, Dt = !1, $l = typeof setTimeout == "function" ? setTimeout : null, Wt = typeof clearTimeout == "function" ? clearTimeout : null, Rl = typeof setImmediate < "u" ? setImmediate : null;
    function ct(E) {
      for (var O = T(p); O !== null; ) {
        if (O.callback === null) y(p);
        else if (O.startTime <= E)
          y(p), O.sortIndex = O.expirationTime, g(H, O);
        else break;
        O = T(p);
      }
    }
    function Et(E) {
      if (Cl = !1, ct(E), !jl)
        if (T(H) !== null)
          jl = !0, Gl || (Gl = !0, Ql());
        else {
          var O = T(p);
          O !== null && rt(Et, O.startTime - E);
        }
    }
    var Gl = !1, W = -1, Xl = 5, zt = -1;
    function Va() {
      return Dt ? !0 : !(o.unstable_now() - zt < Xl);
    }
    function At() {
      if (Dt = !1, Gl) {
        var E = o.unstable_now();
        zt = E;
        var O = !0;
        try {
          l: {
            jl = !1, Cl && (Cl = !1, Wt(W), W = -1), Wl = !0;
            var Y = ll;
            try {
              t: {
                for (ct(E), q = T(H); q !== null && !(q.expirationTime > E && Va()); ) {
                  var el = q.callback;
                  if (typeof el == "function") {
                    q.callback = null, ll = q.priorityLevel;
                    var il = el(
                      q.expirationTime <= E
                    );
                    if (E = o.unstable_now(), typeof il == "function") {
                      q.callback = il, ct(E), O = !0;
                      break t;
                    }
                    q === T(H) && y(H), ct(E);
                  } else y(H);
                  q = T(H);
                }
                if (q !== null) O = !0;
                else {
                  var d = T(p);
                  d !== null && rt(
                    Et,
                    d.startTime - E
                  ), O = !1;
                }
              }
              break l;
            } finally {
              q = null, ll = Y, Wl = !1;
            }
            O = void 0;
          }
        } finally {
          O ? Ql() : Gl = !1;
        }
      }
    }
    var Ql;
    if (typeof Rl == "function")
      Ql = function() {
        Rl(At);
      };
    else if (typeof MessageChannel < "u") {
      var za = new MessageChannel(), Ut = za.port2;
      za.port1.onmessage = At, Ql = function() {
        Ut.postMessage(null);
      };
    } else
      Ql = function() {
        $l(At, 0);
      };
    function rt(E, O) {
      W = $l(function() {
        E(o.unstable_now());
      }, O);
    }
    o.unstable_IdlePriority = 5, o.unstable_ImmediatePriority = 1, o.unstable_LowPriority = 4, o.unstable_NormalPriority = 3, o.unstable_Profiling = null, o.unstable_UserBlockingPriority = 2, o.unstable_cancelCallback = function(E) {
      E.callback = null;
    }, o.unstable_forceFrameRate = function(E) {
      0 > E || 125 < E ? console.error(
        "forceFrameRate takes a positive int between 0 and 125, forcing frame rates higher than 125 fps is not supported"
      ) : Xl = 0 < E ? Math.floor(1e3 / E) : 5;
    }, o.unstable_getCurrentPriorityLevel = function() {
      return ll;
    }, o.unstable_next = function(E) {
      switch (ll) {
        case 1:
        case 2:
        case 3:
          var O = 3;
          break;
        default:
          O = ll;
      }
      var Y = ll;
      ll = O;
      try {
        return E();
      } finally {
        ll = Y;
      }
    }, o.unstable_requestPaint = function() {
      Dt = !0;
    }, o.unstable_runWithPriority = function(E, O) {
      switch (E) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
          break;
        default:
          E = 3;
      }
      var Y = ll;
      ll = E;
      try {
        return O();
      } finally {
        ll = Y;
      }
    }, o.unstable_scheduleCallback = function(E, O, Y) {
      var el = o.unstable_now();
      switch (typeof Y == "object" && Y !== null ? (Y = Y.delay, Y = typeof Y == "number" && 0 < Y ? el + Y : el) : Y = el, E) {
        case 1:
          var il = -1;
          break;
        case 2:
          il = 250;
          break;
        case 5:
          il = 1073741823;
          break;
        case 4:
          il = 1e4;
          break;
        default:
          il = 5e3;
      }
      return il = Y + il, E = {
        id: F++,
        callback: O,
        priorityLevel: E,
        startTime: Y,
        expirationTime: il,
        sortIndex: -1
      }, Y > el ? (E.sortIndex = Y, g(p, E), T(H) === null && E === T(p) && (Cl ? (Wt(W), W = -1) : Cl = !0, rt(Et, Y - el))) : (E.sortIndex = il, g(H, E), jl || Wl || (jl = !0, Gl || (Gl = !0, Ql()))), E;
    }, o.unstable_shouldYield = Va, o.unstable_wrapCallback = function(E) {
      var O = ll;
      return function() {
        var Y = ll;
        ll = O;
        try {
          return E.apply(this, arguments);
        } finally {
          ll = Y;
        }
      };
    };
  })(zi)), zi;
}
var Z0;
function th() {
  return Z0 || (Z0 = 1, Ei.exports = lh()), Ei.exports;
}
var Ai = { exports: {} }, Hl = {};
/**
 * @license React
 * react-dom.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var L0;
function ah() {
  if (L0) return Hl;
  L0 = 1;
  var o = Ui();
  function g(H) {
    var p = "https://react.dev/errors/" + H;
    if (1 < arguments.length) {
      p += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var F = 2; F < arguments.length; F++)
        p += "&args[]=" + encodeURIComponent(arguments[F]);
    }
    return "Minified React error #" + H + "; visit " + p + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function T() {
  }
  var y = {
    d: {
      f: T,
      r: function() {
        throw Error(g(522));
      },
      D: T,
      C: T,
      L: T,
      m: T,
      X: T,
      S: T,
      M: T
    },
    p: 0,
    findDOMNode: null
  }, D = Symbol.for("react.portal");
  function Z(H, p, F) {
    var q = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: D,
      key: q == null ? null : "" + q,
      children: H,
      containerInfo: p,
      implementation: F
    };
  }
  var K = o.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function gl(H, p) {
    if (H === "font") return "";
    if (typeof p == "string")
      return p === "use-credentials" ? p : "";
  }
  return Hl.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = y, Hl.createPortal = function(H, p) {
    var F = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!p || p.nodeType !== 1 && p.nodeType !== 9 && p.nodeType !== 11)
      throw Error(g(299));
    return Z(H, p, null, F);
  }, Hl.flushSync = function(H) {
    var p = K.T, F = y.p;
    try {
      if (K.T = null, y.p = 2, H) return H();
    } finally {
      K.T = p, y.p = F, y.d.f();
    }
  }, Hl.preconnect = function(H, p) {
    typeof H == "string" && (p ? (p = p.crossOrigin, p = typeof p == "string" ? p === "use-credentials" ? p : "" : void 0) : p = null, y.d.C(H, p));
  }, Hl.prefetchDNS = function(H) {
    typeof H == "string" && y.d.D(H);
  }, Hl.preinit = function(H, p) {
    if (typeof H == "string" && p && typeof p.as == "string") {
      var F = p.as, q = gl(F, p.crossOrigin), ll = typeof p.integrity == "string" ? p.integrity : void 0, Wl = typeof p.fetchPriority == "string" ? p.fetchPriority : void 0;
      F === "style" ? y.d.S(
        H,
        typeof p.precedence == "string" ? p.precedence : void 0,
        {
          crossOrigin: q,
          integrity: ll,
          fetchPriority: Wl
        }
      ) : F === "script" && y.d.X(H, {
        crossOrigin: q,
        integrity: ll,
        fetchPriority: Wl,
        nonce: typeof p.nonce == "string" ? p.nonce : void 0
      });
    }
  }, Hl.preinitModule = function(H, p) {
    if (typeof H == "string")
      if (typeof p == "object" && p !== null) {
        if (p.as == null || p.as === "script") {
          var F = gl(
            p.as,
            p.crossOrigin
          );
          y.d.M(H, {
            crossOrigin: F,
            integrity: typeof p.integrity == "string" ? p.integrity : void 0,
            nonce: typeof p.nonce == "string" ? p.nonce : void 0
          });
        }
      } else p == null && y.d.M(H);
  }, Hl.preload = function(H, p) {
    if (typeof H == "string" && typeof p == "object" && p !== null && typeof p.as == "string") {
      var F = p.as, q = gl(F, p.crossOrigin);
      y.d.L(H, F, {
        crossOrigin: q,
        integrity: typeof p.integrity == "string" ? p.integrity : void 0,
        nonce: typeof p.nonce == "string" ? p.nonce : void 0,
        type: typeof p.type == "string" ? p.type : void 0,
        fetchPriority: typeof p.fetchPriority == "string" ? p.fetchPriority : void 0,
        referrerPolicy: typeof p.referrerPolicy == "string" ? p.referrerPolicy : void 0,
        imageSrcSet: typeof p.imageSrcSet == "string" ? p.imageSrcSet : void 0,
        imageSizes: typeof p.imageSizes == "string" ? p.imageSizes : void 0,
        media: typeof p.media == "string" ? p.media : void 0
      });
    }
  }, Hl.preloadModule = function(H, p) {
    if (typeof H == "string")
      if (p) {
        var F = gl(p.as, p.crossOrigin);
        y.d.m(H, {
          as: typeof p.as == "string" && p.as !== "script" ? p.as : void 0,
          crossOrigin: F,
          integrity: typeof p.integrity == "string" ? p.integrity : void 0
        });
      } else y.d.m(H);
  }, Hl.requestFormReset = function(H) {
    y.d.r(H);
  }, Hl.unstable_batchedUpdates = function(H, p) {
    return H(p);
  }, Hl.useFormState = function(H, p, F) {
    return K.H.useFormState(H, p, F);
  }, Hl.useFormStatus = function() {
    return K.H.useHostTransitionStatus();
  }, Hl.version = "19.2.4", Hl;
}
var x0;
function ny() {
  if (x0) return Ai.exports;
  x0 = 1;
  function o() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(o);
      } catch (g) {
        console.error(g);
      }
  }
  return o(), Ai.exports = ah(), Ai.exports;
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
var V0;
function uh() {
  if (V0) return Oe;
  V0 = 1;
  var o = th(), g = Ui(), T = ny();
  function y(l) {
    var t = "https://react.dev/errors/" + l;
    if (1 < arguments.length) {
      t += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var a = 2; a < arguments.length; a++)
        t += "&args[]=" + encodeURIComponent(arguments[a]);
    }
    return "Minified React error #" + l + "; visit " + t + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function D(l) {
    return !(!l || l.nodeType !== 1 && l.nodeType !== 9 && l.nodeType !== 11);
  }
  function Z(l) {
    var t = l, a = l;
    if (l.alternate) for (; t.return; ) t = t.return;
    else {
      l = t;
      do
        t = l, (t.flags & 4098) !== 0 && (a = t.return), l = t.return;
      while (l);
    }
    return t.tag === 3 ? a : null;
  }
  function K(l) {
    if (l.tag === 13) {
      var t = l.memoizedState;
      if (t === null && (l = l.alternate, l !== null && (t = l.memoizedState)), t !== null) return t.dehydrated;
    }
    return null;
  }
  function gl(l) {
    if (l.tag === 31) {
      var t = l.memoizedState;
      if (t === null && (l = l.alternate, l !== null && (t = l.memoizedState)), t !== null) return t.dehydrated;
    }
    return null;
  }
  function H(l) {
    if (Z(l) !== l)
      throw Error(y(188));
  }
  function p(l) {
    var t = l.alternate;
    if (!t) {
      if (t = Z(l), t === null) throw Error(y(188));
      return t !== l ? null : l;
    }
    for (var a = l, u = t; ; ) {
      var e = a.return;
      if (e === null) break;
      var n = e.alternate;
      if (n === null) {
        if (u = e.return, u !== null) {
          a = u;
          continue;
        }
        break;
      }
      if (e.child === n.child) {
        for (n = e.child; n; ) {
          if (n === a) return H(e), l;
          if (n === u) return H(e), t;
          n = n.sibling;
        }
        throw Error(y(188));
      }
      if (a.return !== u.return) a = e, u = n;
      else {
        for (var c = !1, f = e.child; f; ) {
          if (f === a) {
            c = !0, a = e, u = n;
            break;
          }
          if (f === u) {
            c = !0, u = e, a = n;
            break;
          }
          f = f.sibling;
        }
        if (!c) {
          for (f = n.child; f; ) {
            if (f === a) {
              c = !0, a = n, u = e;
              break;
            }
            if (f === u) {
              c = !0, u = n, a = e;
              break;
            }
            f = f.sibling;
          }
          if (!c) throw Error(y(189));
        }
      }
      if (a.alternate !== u) throw Error(y(190));
    }
    if (a.tag !== 3) throw Error(y(188));
    return a.stateNode.current === a ? l : t;
  }
  function F(l) {
    var t = l.tag;
    if (t === 5 || t === 26 || t === 27 || t === 6) return l;
    for (l = l.child; l !== null; ) {
      if (t = F(l), t !== null) return t;
      l = l.sibling;
    }
    return null;
  }
  var q = Object.assign, ll = Symbol.for("react.element"), Wl = Symbol.for("react.transitional.element"), jl = Symbol.for("react.portal"), Cl = Symbol.for("react.fragment"), Dt = Symbol.for("react.strict_mode"), $l = Symbol.for("react.profiler"), Wt = Symbol.for("react.consumer"), Rl = Symbol.for("react.context"), ct = Symbol.for("react.forward_ref"), Et = Symbol.for("react.suspense"), Gl = Symbol.for("react.suspense_list"), W = Symbol.for("react.memo"), Xl = Symbol.for("react.lazy"), zt = Symbol.for("react.activity"), Va = Symbol.for("react.memo_cache_sentinel"), At = Symbol.iterator;
  function Ql(l) {
    return l === null || typeof l != "object" ? null : (l = At && l[At] || l["@@iterator"], typeof l == "function" ? l : null);
  }
  var za = Symbol.for("react.client.reference");
  function Ut(l) {
    if (l == null) return null;
    if (typeof l == "function")
      return l.$$typeof === za ? null : l.displayName || l.name || null;
    if (typeof l == "string") return l;
    switch (l) {
      case Cl:
        return "Fragment";
      case $l:
        return "Profiler";
      case Dt:
        return "StrictMode";
      case Et:
        return "Suspense";
      case Gl:
        return "SuspenseList";
      case zt:
        return "Activity";
    }
    if (typeof l == "object")
      switch (l.$$typeof) {
        case jl:
          return "Portal";
        case Rl:
          return l.displayName || "Context";
        case Wt:
          return (l._context.displayName || "Context") + ".Consumer";
        case ct:
          var t = l.render;
          return l = l.displayName, l || (l = t.displayName || t.name || "", l = l !== "" ? "ForwardRef(" + l + ")" : "ForwardRef"), l;
        case W:
          return t = l.displayName || null, t !== null ? t : Ut(l.type) || "Memo";
        case Xl:
          t = l._payload, l = l._init;
          try {
            return Ut(l(t));
          } catch {
          }
      }
    return null;
  }
  var rt = Array.isArray, E = g.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE, O = T.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE, Y = {
    pending: !1,
    data: null,
    method: null,
    action: null
  }, el = [], il = -1;
  function d(l) {
    return { current: l };
  }
  function _(l) {
    0 > il || (l.current = el[il], el[il] = null, il--);
  }
  function M(l, t) {
    il++, el[il] = l.current, l.current = t;
  }
  var N = d(null), G = d(null), L = d(null), tl = d(null);
  function Bl(l, t) {
    switch (M(L, t), M(G, l), M(N, null), t.nodeType) {
      case 9:
      case 11:
        l = (l = t.documentElement) && (l = l.namespaceURI) ? e0(l) : 0;
        break;
      default:
        if (l = t.tagName, t = t.namespaceURI)
          t = e0(t), l = n0(t, l);
        else
          switch (l) {
            case "svg":
              l = 1;
              break;
            case "math":
              l = 2;
              break;
            default:
              l = 0;
          }
    }
    _(N), M(N, l);
  }
  function hl() {
    _(N), _(G), _(L);
  }
  function Ru(l) {
    l.memoizedState !== null && M(tl, l);
    var t = N.current, a = n0(t, l.type);
    t !== a && (M(G, l), M(N, a));
  }
  function Ue(l) {
    G.current === l && (_(N), _(G)), tl.current === l && (_(tl), Te._currentValue = Y);
  }
  var Pn, Ci;
  function Aa(l) {
    if (Pn === void 0)
      try {
        throw Error();
      } catch (a) {
        var t = a.stack.trim().match(/\n( *(at )?)/);
        Pn = t && t[1] || "", Ci = -1 < a.stack.indexOf(`
    at`) ? " (<anonymous>)" : -1 < a.stack.indexOf("@") ? "@unknown:0:0" : "";
      }
    return `
` + Pn + l + Ci;
  }
  var lc = !1;
  function tc(l, t) {
    if (!l || lc) return "";
    lc = !0;
    var a = Error.prepareStackTrace;
    Error.prepareStackTrace = void 0;
    try {
      var u = {
        DetermineComponentFrameRoot: function() {
          try {
            if (t) {
              var A = function() {
                throw Error();
              };
              if (Object.defineProperty(A.prototype, "props", {
                set: function() {
                  throw Error();
                }
              }), typeof Reflect == "object" && Reflect.construct) {
                try {
                  Reflect.construct(A, []);
                } catch (r) {
                  var S = r;
                }
                Reflect.construct(l, [], A);
              } else {
                try {
                  A.call();
                } catch (r) {
                  S = r;
                }
                l.call(A.prototype);
              }
            } else {
              try {
                throw Error();
              } catch (r) {
                S = r;
              }
              (A = l()) && typeof A.catch == "function" && A.catch(function() {
              });
            }
          } catch (r) {
            if (r && S && typeof r.stack == "string")
              return [r.stack, S.stack];
          }
          return [null, null];
        }
      };
      u.DetermineComponentFrameRoot.displayName = "DetermineComponentFrameRoot";
      var e = Object.getOwnPropertyDescriptor(
        u.DetermineComponentFrameRoot,
        "name"
      );
      e && e.configurable && Object.defineProperty(
        u.DetermineComponentFrameRoot,
        "name",
        { value: "DetermineComponentFrameRoot" }
      );
      var n = u.DetermineComponentFrameRoot(), c = n[0], f = n[1];
      if (c && f) {
        var i = c.split(`
`), h = f.split(`
`);
        for (e = u = 0; u < i.length && !i[u].includes("DetermineComponentFrameRoot"); )
          u++;
        for (; e < h.length && !h[e].includes(
          "DetermineComponentFrameRoot"
        ); )
          e++;
        if (u === i.length || e === h.length)
          for (u = i.length - 1, e = h.length - 1; 1 <= u && 0 <= e && i[u] !== h[e]; )
            e--;
        for (; 1 <= u && 0 <= e; u--, e--)
          if (i[u] !== h[e]) {
            if (u !== 1 || e !== 1)
              do
                if (u--, e--, 0 > e || i[u] !== h[e]) {
                  var b = `
` + i[u].replace(" at new ", " at ");
                  return l.displayName && b.includes("<anonymous>") && (b = b.replace("<anonymous>", l.displayName)), b;
                }
              while (1 <= u && 0 <= e);
            break;
          }
      }
    } finally {
      lc = !1, Error.prepareStackTrace = a;
    }
    return (a = l ? l.displayName || l.name : "") ? Aa(a) : "";
  }
  function sy(l, t) {
    switch (l.tag) {
      case 26:
      case 27:
      case 5:
        return Aa(l.type);
      case 16:
        return Aa("Lazy");
      case 13:
        return l.child !== t && t !== null ? Aa("Suspense Fallback") : Aa("Suspense");
      case 19:
        return Aa("SuspenseList");
      case 0:
      case 15:
        return tc(l.type, !1);
      case 11:
        return tc(l.type.render, !1);
      case 1:
        return tc(l.type, !0);
      case 31:
        return Aa("Activity");
      default:
        return "";
    }
  }
  function Bi(l) {
    try {
      var t = "", a = null;
      do
        t += sy(l, a), a = l, l = l.return;
      while (l);
      return t;
    } catch (u) {
      return `
Error generating stack: ` + u.message + `
` + u.stack;
    }
  }
  var ac = Object.prototype.hasOwnProperty, uc = o.unstable_scheduleCallback, ec = o.unstable_cancelCallback, dy = o.unstable_shouldYield, yy = o.unstable_requestPaint, Fl = o.unstable_now, my = o.unstable_getCurrentPriorityLevel, qi = o.unstable_ImmediatePriority, Yi = o.unstable_UserBlockingPriority, Ne = o.unstable_NormalPriority, vy = o.unstable_LowPriority, ji = o.unstable_IdlePriority, hy = o.log, Sy = o.unstable_setDisableYieldValue, Hu = null, kl = null;
  function $t(l) {
    if (typeof hy == "function" && Sy(l), kl && typeof kl.setStrictMode == "function")
      try {
        kl.setStrictMode(Hu, l);
      } catch {
      }
  }
  var Il = Math.clz32 ? Math.clz32 : by, ry = Math.log, gy = Math.LN2;
  function by(l) {
    return l >>>= 0, l === 0 ? 32 : 31 - (ry(l) / gy | 0) | 0;
  }
  var Re = 256, He = 262144, Ce = 4194304;
  function _a(l) {
    var t = l & 42;
    if (t !== 0) return t;
    switch (l & -l) {
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
        return l & 261888;
      case 262144:
      case 524288:
      case 1048576:
      case 2097152:
        return l & 3932160;
      case 4194304:
      case 8388608:
      case 16777216:
      case 33554432:
        return l & 62914560;
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
        return l;
    }
  }
  function Be(l, t, a) {
    var u = l.pendingLanes;
    if (u === 0) return 0;
    var e = 0, n = l.suspendedLanes, c = l.pingedLanes;
    l = l.warmLanes;
    var f = u & 134217727;
    return f !== 0 ? (u = f & ~n, u !== 0 ? e = _a(u) : (c &= f, c !== 0 ? e = _a(c) : a || (a = f & ~l, a !== 0 && (e = _a(a))))) : (f = u & ~n, f !== 0 ? e = _a(f) : c !== 0 ? e = _a(c) : a || (a = u & ~l, a !== 0 && (e = _a(a)))), e === 0 ? 0 : t !== 0 && t !== e && (t & n) === 0 && (n = e & -e, a = t & -t, n >= a || n === 32 && (a & 4194048) !== 0) ? t : e;
  }
  function Cu(l, t) {
    return (l.pendingLanes & ~(l.suspendedLanes & ~l.pingedLanes) & t) === 0;
  }
  function Ty(l, t) {
    switch (l) {
      case 1:
      case 2:
      case 4:
      case 8:
      case 64:
        return t + 250;
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
        return t + 5e3;
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
  function Gi() {
    var l = Ce;
    return Ce <<= 1, (Ce & 62914560) === 0 && (Ce = 4194304), l;
  }
  function nc(l) {
    for (var t = [], a = 0; 31 > a; a++) t.push(l);
    return t;
  }
  function Bu(l, t) {
    l.pendingLanes |= t, t !== 268435456 && (l.suspendedLanes = 0, l.pingedLanes = 0, l.warmLanes = 0);
  }
  function Ey(l, t, a, u, e, n) {
    var c = l.pendingLanes;
    l.pendingLanes = a, l.suspendedLanes = 0, l.pingedLanes = 0, l.warmLanes = 0, l.expiredLanes &= a, l.entangledLanes &= a, l.errorRecoveryDisabledLanes &= a, l.shellSuspendCounter = 0;
    var f = l.entanglements, i = l.expirationTimes, h = l.hiddenUpdates;
    for (a = c & ~a; 0 < a; ) {
      var b = 31 - Il(a), A = 1 << b;
      f[b] = 0, i[b] = -1;
      var S = h[b];
      if (S !== null)
        for (h[b] = null, b = 0; b < S.length; b++) {
          var r = S[b];
          r !== null && (r.lane &= -536870913);
        }
      a &= ~A;
    }
    u !== 0 && Xi(l, u, 0), n !== 0 && e === 0 && l.tag !== 0 && (l.suspendedLanes |= n & ~(c & ~t));
  }
  function Xi(l, t, a) {
    l.pendingLanes |= t, l.suspendedLanes &= ~t;
    var u = 31 - Il(t);
    l.entangledLanes |= t, l.entanglements[u] = l.entanglements[u] | 1073741824 | a & 261930;
  }
  function Qi(l, t) {
    var a = l.entangledLanes |= t;
    for (l = l.entanglements; a; ) {
      var u = 31 - Il(a), e = 1 << u;
      e & t | l[u] & t && (l[u] |= t), a &= ~e;
    }
  }
  function Zi(l, t) {
    var a = t & -t;
    return a = (a & 42) !== 0 ? 1 : cc(a), (a & (l.suspendedLanes | t)) !== 0 ? 0 : a;
  }
  function cc(l) {
    switch (l) {
      case 2:
        l = 1;
        break;
      case 8:
        l = 4;
        break;
      case 32:
        l = 16;
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
        l = 128;
        break;
      case 268435456:
        l = 134217728;
        break;
      default:
        l = 0;
    }
    return l;
  }
  function fc(l) {
    return l &= -l, 2 < l ? 8 < l ? (l & 134217727) !== 0 ? 32 : 268435456 : 8 : 2;
  }
  function Li() {
    var l = O.p;
    return l !== 0 ? l : (l = window.event, l === void 0 ? 32 : D0(l.type));
  }
  function xi(l, t) {
    var a = O.p;
    try {
      return O.p = l, t();
    } finally {
      O.p = a;
    }
  }
  var Ft = Math.random().toString(36).slice(2), Ol = "__reactFiber$" + Ft, Zl = "__reactProps$" + Ft, Ka = "__reactContainer$" + Ft, ic = "__reactEvents$" + Ft, zy = "__reactListeners$" + Ft, Ay = "__reactHandles$" + Ft, Vi = "__reactResources$" + Ft, qu = "__reactMarker$" + Ft;
  function oc(l) {
    delete l[Ol], delete l[Zl], delete l[ic], delete l[zy], delete l[Ay];
  }
  function Ja(l) {
    var t = l[Ol];
    if (t) return t;
    for (var a = l.parentNode; a; ) {
      if (t = a[Ka] || a[Ol]) {
        if (a = t.alternate, t.child !== null || a !== null && a.child !== null)
          for (l = y0(l); l !== null; ) {
            if (a = l[Ol]) return a;
            l = y0(l);
          }
        return t;
      }
      l = a, a = l.parentNode;
    }
    return null;
  }
  function wa(l) {
    if (l = l[Ol] || l[Ka]) {
      var t = l.tag;
      if (t === 5 || t === 6 || t === 13 || t === 31 || t === 26 || t === 27 || t === 3)
        return l;
    }
    return null;
  }
  function Yu(l) {
    var t = l.tag;
    if (t === 5 || t === 26 || t === 27 || t === 6) return l.stateNode;
    throw Error(y(33));
  }
  function Wa(l) {
    var t = l[Vi];
    return t || (t = l[Vi] = { hoistableStyles: /* @__PURE__ */ new Map(), hoistableScripts: /* @__PURE__ */ new Map() }), t;
  }
  function _l(l) {
    l[qu] = !0;
  }
  var Ki = /* @__PURE__ */ new Set(), Ji = {};
  function pa(l, t) {
    $a(l, t), $a(l + "Capture", t);
  }
  function $a(l, t) {
    for (Ji[l] = t, l = 0; l < t.length; l++)
      Ki.add(t[l]);
  }
  var _y = RegExp(
    "^[:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD][:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD\\-.0-9\\u00B7\\u0300-\\u036F\\u203F-\\u2040]*$"
  ), wi = {}, Wi = {};
  function py(l) {
    return ac.call(Wi, l) ? !0 : ac.call(wi, l) ? !1 : _y.test(l) ? Wi[l] = !0 : (wi[l] = !0, !1);
  }
  function qe(l, t, a) {
    if (py(t))
      if (a === null) l.removeAttribute(t);
      else {
        switch (typeof a) {
          case "undefined":
          case "function":
          case "symbol":
            l.removeAttribute(t);
            return;
          case "boolean":
            var u = t.toLowerCase().slice(0, 5);
            if (u !== "data-" && u !== "aria-") {
              l.removeAttribute(t);
              return;
            }
        }
        l.setAttribute(t, "" + a);
      }
  }
  function Ye(l, t, a) {
    if (a === null) l.removeAttribute(t);
    else {
      switch (typeof a) {
        case "undefined":
        case "function":
        case "symbol":
        case "boolean":
          l.removeAttribute(t);
          return;
      }
      l.setAttribute(t, "" + a);
    }
  }
  function Nt(l, t, a, u) {
    if (u === null) l.removeAttribute(a);
    else {
      switch (typeof u) {
        case "undefined":
        case "function":
        case "symbol":
        case "boolean":
          l.removeAttribute(a);
          return;
      }
      l.setAttributeNS(t, a, "" + u);
    }
  }
  function ft(l) {
    switch (typeof l) {
      case "bigint":
      case "boolean":
      case "number":
      case "string":
      case "undefined":
        return l;
      case "object":
        return l;
      default:
        return "";
    }
  }
  function $i(l) {
    var t = l.type;
    return (l = l.nodeName) && l.toLowerCase() === "input" && (t === "checkbox" || t === "radio");
  }
  function Oy(l, t, a) {
    var u = Object.getOwnPropertyDescriptor(
      l.constructor.prototype,
      t
    );
    if (!l.hasOwnProperty(t) && typeof u < "u" && typeof u.get == "function" && typeof u.set == "function") {
      var e = u.get, n = u.set;
      return Object.defineProperty(l, t, {
        configurable: !0,
        get: function() {
          return e.call(this);
        },
        set: function(c) {
          a = "" + c, n.call(this, c);
        }
      }), Object.defineProperty(l, t, {
        enumerable: u.enumerable
      }), {
        getValue: function() {
          return a;
        },
        setValue: function(c) {
          a = "" + c;
        },
        stopTracking: function() {
          l._valueTracker = null, delete l[t];
        }
      };
    }
  }
  function sc(l) {
    if (!l._valueTracker) {
      var t = $i(l) ? "checked" : "value";
      l._valueTracker = Oy(
        l,
        t,
        "" + l[t]
      );
    }
  }
  function Fi(l) {
    if (!l) return !1;
    var t = l._valueTracker;
    if (!t) return !0;
    var a = t.getValue(), u = "";
    return l && (u = $i(l) ? l.checked ? "true" : "false" : l.value), l = u, l !== a ? (t.setValue(l), !0) : !1;
  }
  function je(l) {
    if (l = l || (typeof document < "u" ? document : void 0), typeof l > "u") return null;
    try {
      return l.activeElement || l.body;
    } catch {
      return l.body;
    }
  }
  var My = /[\n"\\]/g;
  function it(l) {
    return l.replace(
      My,
      function(t) {
        return "\\" + t.charCodeAt(0).toString(16) + " ";
      }
    );
  }
  function dc(l, t, a, u, e, n, c, f) {
    l.name = "", c != null && typeof c != "function" && typeof c != "symbol" && typeof c != "boolean" ? l.type = c : l.removeAttribute("type"), t != null ? c === "number" ? (t === 0 && l.value === "" || l.value != t) && (l.value = "" + ft(t)) : l.value !== "" + ft(t) && (l.value = "" + ft(t)) : c !== "submit" && c !== "reset" || l.removeAttribute("value"), t != null ? yc(l, c, ft(t)) : a != null ? yc(l, c, ft(a)) : u != null && l.removeAttribute("value"), e == null && n != null && (l.defaultChecked = !!n), e != null && (l.checked = e && typeof e != "function" && typeof e != "symbol"), f != null && typeof f != "function" && typeof f != "symbol" && typeof f != "boolean" ? l.name = "" + ft(f) : l.removeAttribute("name");
  }
  function ki(l, t, a, u, e, n, c, f) {
    if (n != null && typeof n != "function" && typeof n != "symbol" && typeof n != "boolean" && (l.type = n), t != null || a != null) {
      if (!(n !== "submit" && n !== "reset" || t != null)) {
        sc(l);
        return;
      }
      a = a != null ? "" + ft(a) : "", t = t != null ? "" + ft(t) : a, f || t === l.value || (l.value = t), l.defaultValue = t;
    }
    u = u ?? e, u = typeof u != "function" && typeof u != "symbol" && !!u, l.checked = f ? l.checked : !!u, l.defaultChecked = !!u, c != null && typeof c != "function" && typeof c != "symbol" && typeof c != "boolean" && (l.name = c), sc(l);
  }
  function yc(l, t, a) {
    t === "number" && je(l.ownerDocument) === l || l.defaultValue === "" + a || (l.defaultValue = "" + a);
  }
  function Fa(l, t, a, u) {
    if (l = l.options, t) {
      t = {};
      for (var e = 0; e < a.length; e++)
        t["$" + a[e]] = !0;
      for (a = 0; a < l.length; a++)
        e = t.hasOwnProperty("$" + l[a].value), l[a].selected !== e && (l[a].selected = e), e && u && (l[a].defaultSelected = !0);
    } else {
      for (a = "" + ft(a), t = null, e = 0; e < l.length; e++) {
        if (l[e].value === a) {
          l[e].selected = !0, u && (l[e].defaultSelected = !0);
          return;
        }
        t !== null || l[e].disabled || (t = l[e]);
      }
      t !== null && (t.selected = !0);
    }
  }
  function Ii(l, t, a) {
    if (t != null && (t = "" + ft(t), t !== l.value && (l.value = t), a == null)) {
      l.defaultValue !== t && (l.defaultValue = t);
      return;
    }
    l.defaultValue = a != null ? "" + ft(a) : "";
  }
  function Pi(l, t, a, u) {
    if (t == null) {
      if (u != null) {
        if (a != null) throw Error(y(92));
        if (rt(u)) {
          if (1 < u.length) throw Error(y(93));
          u = u[0];
        }
        a = u;
      }
      a == null && (a = ""), t = a;
    }
    a = ft(t), l.defaultValue = a, u = l.textContent, u === a && u !== "" && u !== null && (l.value = u), sc(l);
  }
  function ka(l, t) {
    if (t) {
      var a = l.firstChild;
      if (a && a === l.lastChild && a.nodeType === 3) {
        a.nodeValue = t;
        return;
      }
    }
    l.textContent = t;
  }
  var Dy = new Set(
    "animationIterationCount aspectRatio borderImageOutset borderImageSlice borderImageWidth boxFlex boxFlexGroup boxOrdinalGroup columnCount columns flex flexGrow flexPositive flexShrink flexNegative flexOrder gridArea gridRow gridRowEnd gridRowSpan gridRowStart gridColumn gridColumnEnd gridColumnSpan gridColumnStart fontWeight lineClamp lineHeight opacity order orphans scale tabSize widows zIndex zoom fillOpacity floodOpacity stopOpacity strokeDasharray strokeDashoffset strokeMiterlimit strokeOpacity strokeWidth MozAnimationIterationCount MozBoxFlex MozBoxFlexGroup MozLineClamp msAnimationIterationCount msFlex msZoom msFlexGrow msFlexNegative msFlexOrder msFlexPositive msFlexShrink msGridColumn msGridColumnSpan msGridRow msGridRowSpan WebkitAnimationIterationCount WebkitBoxFlex WebKitBoxFlexGroup WebkitBoxOrdinalGroup WebkitColumnCount WebkitColumns WebkitFlex WebkitFlexGrow WebkitFlexPositive WebkitFlexShrink WebkitLineClamp".split(
      " "
    )
  );
  function lo(l, t, a) {
    var u = t.indexOf("--") === 0;
    a == null || typeof a == "boolean" || a === "" ? u ? l.setProperty(t, "") : t === "float" ? l.cssFloat = "" : l[t] = "" : u ? l.setProperty(t, a) : typeof a != "number" || a === 0 || Dy.has(t) ? t === "float" ? l.cssFloat = a : l[t] = ("" + a).trim() : l[t] = a + "px";
  }
  function to(l, t, a) {
    if (t != null && typeof t != "object")
      throw Error(y(62));
    if (l = l.style, a != null) {
      for (var u in a)
        !a.hasOwnProperty(u) || t != null && t.hasOwnProperty(u) || (u.indexOf("--") === 0 ? l.setProperty(u, "") : u === "float" ? l.cssFloat = "" : l[u] = "");
      for (var e in t)
        u = t[e], t.hasOwnProperty(e) && a[e] !== u && lo(l, e, u);
    } else
      for (var n in t)
        t.hasOwnProperty(n) && lo(l, n, t[n]);
  }
  function mc(l) {
    if (l.indexOf("-") === -1) return !1;
    switch (l) {
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
  var Uy = /* @__PURE__ */ new Map([
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
  ]), Ny = /^[\u0000-\u001F ]*j[\r\n\t]*a[\r\n\t]*v[\r\n\t]*a[\r\n\t]*s[\r\n\t]*c[\r\n\t]*r[\r\n\t]*i[\r\n\t]*p[\r\n\t]*t[\r\n\t]*:/i;
  function Ge(l) {
    return Ny.test("" + l) ? "javascript:throw new Error('React has blocked a javascript: URL as a security precaution.')" : l;
  }
  function Rt() {
  }
  var vc = null;
  function hc(l) {
    return l = l.target || l.srcElement || window, l.correspondingUseElement && (l = l.correspondingUseElement), l.nodeType === 3 ? l.parentNode : l;
  }
  var Ia = null, Pa = null;
  function ao(l) {
    var t = wa(l);
    if (t && (l = t.stateNode)) {
      var a = l[Zl] || null;
      l: switch (l = t.stateNode, t.type) {
        case "input":
          if (dc(
            l,
            a.value,
            a.defaultValue,
            a.defaultValue,
            a.checked,
            a.defaultChecked,
            a.type,
            a.name
          ), t = a.name, a.type === "radio" && t != null) {
            for (a = l; a.parentNode; ) a = a.parentNode;
            for (a = a.querySelectorAll(
              'input[name="' + it(
                "" + t
              ) + '"][type="radio"]'
            ), t = 0; t < a.length; t++) {
              var u = a[t];
              if (u !== l && u.form === l.form) {
                var e = u[Zl] || null;
                if (!e) throw Error(y(90));
                dc(
                  u,
                  e.value,
                  e.defaultValue,
                  e.defaultValue,
                  e.checked,
                  e.defaultChecked,
                  e.type,
                  e.name
                );
              }
            }
            for (t = 0; t < a.length; t++)
              u = a[t], u.form === l.form && Fi(u);
          }
          break l;
        case "textarea":
          Ii(l, a.value, a.defaultValue);
          break l;
        case "select":
          t = a.value, t != null && Fa(l, !!a.multiple, t, !1);
      }
    }
  }
  var Sc = !1;
  function uo(l, t, a) {
    if (Sc) return l(t, a);
    Sc = !0;
    try {
      var u = l(t);
      return u;
    } finally {
      if (Sc = !1, (Ia !== null || Pa !== null) && (On(), Ia && (t = Ia, l = Pa, Pa = Ia = null, ao(t), l)))
        for (t = 0; t < l.length; t++) ao(l[t]);
    }
  }
  function ju(l, t) {
    var a = l.stateNode;
    if (a === null) return null;
    var u = a[Zl] || null;
    if (u === null) return null;
    a = u[t];
    l: switch (t) {
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
        (u = !u.disabled) || (l = l.type, u = !(l === "button" || l === "input" || l === "select" || l === "textarea")), l = !u;
        break l;
      default:
        l = !1;
    }
    if (l) return null;
    if (a && typeof a != "function")
      throw Error(
        y(231, t, typeof a)
      );
    return a;
  }
  var Ht = !(typeof window > "u" || typeof window.document > "u" || typeof window.document.createElement > "u"), rc = !1;
  if (Ht)
    try {
      var Gu = {};
      Object.defineProperty(Gu, "passive", {
        get: function() {
          rc = !0;
        }
      }), window.addEventListener("test", Gu, Gu), window.removeEventListener("test", Gu, Gu);
    } catch {
      rc = !1;
    }
  var kt = null, gc = null, Xe = null;
  function eo() {
    if (Xe) return Xe;
    var l, t = gc, a = t.length, u, e = "value" in kt ? kt.value : kt.textContent, n = e.length;
    for (l = 0; l < a && t[l] === e[l]; l++) ;
    var c = a - l;
    for (u = 1; u <= c && t[a - u] === e[n - u]; u++) ;
    return Xe = e.slice(l, 1 < u ? 1 - u : void 0);
  }
  function Qe(l) {
    var t = l.keyCode;
    return "charCode" in l ? (l = l.charCode, l === 0 && t === 13 && (l = 13)) : l = t, l === 10 && (l = 13), 32 <= l || l === 13 ? l : 0;
  }
  function Ze() {
    return !0;
  }
  function no() {
    return !1;
  }
  function Ll(l) {
    function t(a, u, e, n, c) {
      this._reactName = a, this._targetInst = e, this.type = u, this.nativeEvent = n, this.target = c, this.currentTarget = null;
      for (var f in l)
        l.hasOwnProperty(f) && (a = l[f], this[f] = a ? a(n) : n[f]);
      return this.isDefaultPrevented = (n.defaultPrevented != null ? n.defaultPrevented : n.returnValue === !1) ? Ze : no, this.isPropagationStopped = no, this;
    }
    return q(t.prototype, {
      preventDefault: function() {
        this.defaultPrevented = !0;
        var a = this.nativeEvent;
        a && (a.preventDefault ? a.preventDefault() : typeof a.returnValue != "unknown" && (a.returnValue = !1), this.isDefaultPrevented = Ze);
      },
      stopPropagation: function() {
        var a = this.nativeEvent;
        a && (a.stopPropagation ? a.stopPropagation() : typeof a.cancelBubble != "unknown" && (a.cancelBubble = !0), this.isPropagationStopped = Ze);
      },
      persist: function() {
      },
      isPersistent: Ze
    }), t;
  }
  var Oa = {
    eventPhase: 0,
    bubbles: 0,
    cancelable: 0,
    timeStamp: function(l) {
      return l.timeStamp || Date.now();
    },
    defaultPrevented: 0,
    isTrusted: 0
  }, Le = Ll(Oa), Xu = q({}, Oa, { view: 0, detail: 0 }), Ry = Ll(Xu), bc, Tc, Qu, xe = q({}, Xu, {
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
    getModifierState: zc,
    button: 0,
    buttons: 0,
    relatedTarget: function(l) {
      return l.relatedTarget === void 0 ? l.fromElement === l.srcElement ? l.toElement : l.fromElement : l.relatedTarget;
    },
    movementX: function(l) {
      return "movementX" in l ? l.movementX : (l !== Qu && (Qu && l.type === "mousemove" ? (bc = l.screenX - Qu.screenX, Tc = l.screenY - Qu.screenY) : Tc = bc = 0, Qu = l), bc);
    },
    movementY: function(l) {
      return "movementY" in l ? l.movementY : Tc;
    }
  }), co = Ll(xe), Hy = q({}, xe, { dataTransfer: 0 }), Cy = Ll(Hy), By = q({}, Xu, { relatedTarget: 0 }), Ec = Ll(By), qy = q({}, Oa, {
    animationName: 0,
    elapsedTime: 0,
    pseudoElement: 0
  }), Yy = Ll(qy), jy = q({}, Oa, {
    clipboardData: function(l) {
      return "clipboardData" in l ? l.clipboardData : window.clipboardData;
    }
  }), Gy = Ll(jy), Xy = q({}, Oa, { data: 0 }), fo = Ll(Xy), Qy = {
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
  }, Zy = {
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
  }, Ly = {
    Alt: "altKey",
    Control: "ctrlKey",
    Meta: "metaKey",
    Shift: "shiftKey"
  };
  function xy(l) {
    var t = this.nativeEvent;
    return t.getModifierState ? t.getModifierState(l) : (l = Ly[l]) ? !!t[l] : !1;
  }
  function zc() {
    return xy;
  }
  var Vy = q({}, Xu, {
    key: function(l) {
      if (l.key) {
        var t = Qy[l.key] || l.key;
        if (t !== "Unidentified") return t;
      }
      return l.type === "keypress" ? (l = Qe(l), l === 13 ? "Enter" : String.fromCharCode(l)) : l.type === "keydown" || l.type === "keyup" ? Zy[l.keyCode] || "Unidentified" : "";
    },
    code: 0,
    location: 0,
    ctrlKey: 0,
    shiftKey: 0,
    altKey: 0,
    metaKey: 0,
    repeat: 0,
    locale: 0,
    getModifierState: zc,
    charCode: function(l) {
      return l.type === "keypress" ? Qe(l) : 0;
    },
    keyCode: function(l) {
      return l.type === "keydown" || l.type === "keyup" ? l.keyCode : 0;
    },
    which: function(l) {
      return l.type === "keypress" ? Qe(l) : l.type === "keydown" || l.type === "keyup" ? l.keyCode : 0;
    }
  }), Ky = Ll(Vy), Jy = q({}, xe, {
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
  }), io = Ll(Jy), wy = q({}, Xu, {
    touches: 0,
    targetTouches: 0,
    changedTouches: 0,
    altKey: 0,
    metaKey: 0,
    ctrlKey: 0,
    shiftKey: 0,
    getModifierState: zc
  }), Wy = Ll(wy), $y = q({}, Oa, {
    propertyName: 0,
    elapsedTime: 0,
    pseudoElement: 0
  }), Fy = Ll($y), ky = q({}, xe, {
    deltaX: function(l) {
      return "deltaX" in l ? l.deltaX : "wheelDeltaX" in l ? -l.wheelDeltaX : 0;
    },
    deltaY: function(l) {
      return "deltaY" in l ? l.deltaY : "wheelDeltaY" in l ? -l.wheelDeltaY : "wheelDelta" in l ? -l.wheelDelta : 0;
    },
    deltaZ: 0,
    deltaMode: 0
  }), Iy = Ll(ky), Py = q({}, Oa, {
    newState: 0,
    oldState: 0
  }), lm = Ll(Py), tm = [9, 13, 27, 32], Ac = Ht && "CompositionEvent" in window, Zu = null;
  Ht && "documentMode" in document && (Zu = document.documentMode);
  var am = Ht && "TextEvent" in window && !Zu, oo = Ht && (!Ac || Zu && 8 < Zu && 11 >= Zu), so = " ", yo = !1;
  function mo(l, t) {
    switch (l) {
      case "keyup":
        return tm.indexOf(t.keyCode) !== -1;
      case "keydown":
        return t.keyCode !== 229;
      case "keypress":
      case "mousedown":
      case "focusout":
        return !0;
      default:
        return !1;
    }
  }
  function vo(l) {
    return l = l.detail, typeof l == "object" && "data" in l ? l.data : null;
  }
  var lu = !1;
  function um(l, t) {
    switch (l) {
      case "compositionend":
        return vo(t);
      case "keypress":
        return t.which !== 32 ? null : (yo = !0, so);
      case "textInput":
        return l = t.data, l === so && yo ? null : l;
      default:
        return null;
    }
  }
  function em(l, t) {
    if (lu)
      return l === "compositionend" || !Ac && mo(l, t) ? (l = eo(), Xe = gc = kt = null, lu = !1, l) : null;
    switch (l) {
      case "paste":
        return null;
      case "keypress":
        if (!(t.ctrlKey || t.altKey || t.metaKey) || t.ctrlKey && t.altKey) {
          if (t.char && 1 < t.char.length)
            return t.char;
          if (t.which) return String.fromCharCode(t.which);
        }
        return null;
      case "compositionend":
        return oo && t.locale !== "ko" ? null : t.data;
      default:
        return null;
    }
  }
  var nm = {
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
  function ho(l) {
    var t = l && l.nodeName && l.nodeName.toLowerCase();
    return t === "input" ? !!nm[l.type] : t === "textarea";
  }
  function So(l, t, a, u) {
    Ia ? Pa ? Pa.push(u) : Pa = [u] : Ia = u, t = Cn(t, "onChange"), 0 < t.length && (a = new Le(
      "onChange",
      "change",
      null,
      a,
      u
    ), l.push({ event: a, listeners: t }));
  }
  var Lu = null, xu = null;
  function cm(l) {
    Id(l, 0);
  }
  function Ve(l) {
    var t = Yu(l);
    if (Fi(t)) return l;
  }
  function ro(l, t) {
    if (l === "change") return t;
  }
  var go = !1;
  if (Ht) {
    var _c;
    if (Ht) {
      var pc = "oninput" in document;
      if (!pc) {
        var bo = document.createElement("div");
        bo.setAttribute("oninput", "return;"), pc = typeof bo.oninput == "function";
      }
      _c = pc;
    } else _c = !1;
    go = _c && (!document.documentMode || 9 < document.documentMode);
  }
  function To() {
    Lu && (Lu.detachEvent("onpropertychange", Eo), xu = Lu = null);
  }
  function Eo(l) {
    if (l.propertyName === "value" && Ve(xu)) {
      var t = [];
      So(
        t,
        xu,
        l,
        hc(l)
      ), uo(cm, t);
    }
  }
  function fm(l, t, a) {
    l === "focusin" ? (To(), Lu = t, xu = a, Lu.attachEvent("onpropertychange", Eo)) : l === "focusout" && To();
  }
  function im(l) {
    if (l === "selectionchange" || l === "keyup" || l === "keydown")
      return Ve(xu);
  }
  function om(l, t) {
    if (l === "click") return Ve(t);
  }
  function sm(l, t) {
    if (l === "input" || l === "change")
      return Ve(t);
  }
  function dm(l, t) {
    return l === t && (l !== 0 || 1 / l === 1 / t) || l !== l && t !== t;
  }
  var Pl = typeof Object.is == "function" ? Object.is : dm;
  function Vu(l, t) {
    if (Pl(l, t)) return !0;
    if (typeof l != "object" || l === null || typeof t != "object" || t === null)
      return !1;
    var a = Object.keys(l), u = Object.keys(t);
    if (a.length !== u.length) return !1;
    for (u = 0; u < a.length; u++) {
      var e = a[u];
      if (!ac.call(t, e) || !Pl(l[e], t[e]))
        return !1;
    }
    return !0;
  }
  function zo(l) {
    for (; l && l.firstChild; ) l = l.firstChild;
    return l;
  }
  function Ao(l, t) {
    var a = zo(l);
    l = 0;
    for (var u; a; ) {
      if (a.nodeType === 3) {
        if (u = l + a.textContent.length, l <= t && u >= t)
          return { node: a, offset: t - l };
        l = u;
      }
      l: {
        for (; a; ) {
          if (a.nextSibling) {
            a = a.nextSibling;
            break l;
          }
          a = a.parentNode;
        }
        a = void 0;
      }
      a = zo(a);
    }
  }
  function _o(l, t) {
    return l && t ? l === t ? !0 : l && l.nodeType === 3 ? !1 : t && t.nodeType === 3 ? _o(l, t.parentNode) : "contains" in l ? l.contains(t) : l.compareDocumentPosition ? !!(l.compareDocumentPosition(t) & 16) : !1 : !1;
  }
  function po(l) {
    l = l != null && l.ownerDocument != null && l.ownerDocument.defaultView != null ? l.ownerDocument.defaultView : window;
    for (var t = je(l.document); t instanceof l.HTMLIFrameElement; ) {
      try {
        var a = typeof t.contentWindow.location.href == "string";
      } catch {
        a = !1;
      }
      if (a) l = t.contentWindow;
      else break;
      t = je(l.document);
    }
    return t;
  }
  function Oc(l) {
    var t = l && l.nodeName && l.nodeName.toLowerCase();
    return t && (t === "input" && (l.type === "text" || l.type === "search" || l.type === "tel" || l.type === "url" || l.type === "password") || t === "textarea" || l.contentEditable === "true");
  }
  var ym = Ht && "documentMode" in document && 11 >= document.documentMode, tu = null, Mc = null, Ku = null, Dc = !1;
  function Oo(l, t, a) {
    var u = a.window === a ? a.document : a.nodeType === 9 ? a : a.ownerDocument;
    Dc || tu == null || tu !== je(u) || (u = tu, "selectionStart" in u && Oc(u) ? u = { start: u.selectionStart, end: u.selectionEnd } : (u = (u.ownerDocument && u.ownerDocument.defaultView || window).getSelection(), u = {
      anchorNode: u.anchorNode,
      anchorOffset: u.anchorOffset,
      focusNode: u.focusNode,
      focusOffset: u.focusOffset
    }), Ku && Vu(Ku, u) || (Ku = u, u = Cn(Mc, "onSelect"), 0 < u.length && (t = new Le(
      "onSelect",
      "select",
      null,
      t,
      a
    ), l.push({ event: t, listeners: u }), t.target = tu)));
  }
  function Ma(l, t) {
    var a = {};
    return a[l.toLowerCase()] = t.toLowerCase(), a["Webkit" + l] = "webkit" + t, a["Moz" + l] = "moz" + t, a;
  }
  var au = {
    animationend: Ma("Animation", "AnimationEnd"),
    animationiteration: Ma("Animation", "AnimationIteration"),
    animationstart: Ma("Animation", "AnimationStart"),
    transitionrun: Ma("Transition", "TransitionRun"),
    transitionstart: Ma("Transition", "TransitionStart"),
    transitioncancel: Ma("Transition", "TransitionCancel"),
    transitionend: Ma("Transition", "TransitionEnd")
  }, Uc = {}, Mo = {};
  Ht && (Mo = document.createElement("div").style, "AnimationEvent" in window || (delete au.animationend.animation, delete au.animationiteration.animation, delete au.animationstart.animation), "TransitionEvent" in window || delete au.transitionend.transition);
  function Da(l) {
    if (Uc[l]) return Uc[l];
    if (!au[l]) return l;
    var t = au[l], a;
    for (a in t)
      if (t.hasOwnProperty(a) && a in Mo)
        return Uc[l] = t[a];
    return l;
  }
  var Do = Da("animationend"), Uo = Da("animationiteration"), No = Da("animationstart"), mm = Da("transitionrun"), vm = Da("transitionstart"), hm = Da("transitioncancel"), Ro = Da("transitionend"), Ho = /* @__PURE__ */ new Map(), Nc = "abort auxClick beforeToggle cancel canPlay canPlayThrough click close contextMenu copy cut drag dragEnd dragEnter dragExit dragLeave dragOver dragStart drop durationChange emptied encrypted ended error gotPointerCapture input invalid keyDown keyPress keyUp load loadedData loadedMetadata loadStart lostPointerCapture mouseDown mouseMove mouseOut mouseOver mouseUp paste pause play playing pointerCancel pointerDown pointerMove pointerOut pointerOver pointerUp progress rateChange reset resize seeked seeking stalled submit suspend timeUpdate touchCancel touchEnd touchStart volumeChange scroll toggle touchMove waiting wheel".split(
    " "
  );
  Nc.push("scrollEnd");
  function gt(l, t) {
    Ho.set(l, t), pa(t, [l]);
  }
  var Ke = typeof reportError == "function" ? reportError : function(l) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var t = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof l == "object" && l !== null && typeof l.message == "string" ? String(l.message) : String(l),
        error: l
      });
      if (!window.dispatchEvent(t)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", l);
      return;
    }
    console.error(l);
  }, ot = [], uu = 0, Rc = 0;
  function Je() {
    for (var l = uu, t = Rc = uu = 0; t < l; ) {
      var a = ot[t];
      ot[t++] = null;
      var u = ot[t];
      ot[t++] = null;
      var e = ot[t];
      ot[t++] = null;
      var n = ot[t];
      if (ot[t++] = null, u !== null && e !== null) {
        var c = u.pending;
        c === null ? e.next = e : (e.next = c.next, c.next = e), u.pending = e;
      }
      n !== 0 && Co(a, e, n);
    }
  }
  function we(l, t, a, u) {
    ot[uu++] = l, ot[uu++] = t, ot[uu++] = a, ot[uu++] = u, Rc |= u, l.lanes |= u, l = l.alternate, l !== null && (l.lanes |= u);
  }
  function Hc(l, t, a, u) {
    return we(l, t, a, u), We(l);
  }
  function Ua(l, t) {
    return we(l, null, null, t), We(l);
  }
  function Co(l, t, a) {
    l.lanes |= a;
    var u = l.alternate;
    u !== null && (u.lanes |= a);
    for (var e = !1, n = l.return; n !== null; )
      n.childLanes |= a, u = n.alternate, u !== null && (u.childLanes |= a), n.tag === 22 && (l = n.stateNode, l === null || l._visibility & 1 || (e = !0)), l = n, n = n.return;
    return l.tag === 3 ? (n = l.stateNode, e && t !== null && (e = 31 - Il(a), l = n.hiddenUpdates, u = l[e], u === null ? l[e] = [t] : u.push(t), t.lane = a | 536870912), n) : null;
  }
  function We(l) {
    if (50 < me)
      throw me = 0, Lf = null, Error(y(185));
    for (var t = l.return; t !== null; )
      l = t, t = l.return;
    return l.tag === 3 ? l.stateNode : null;
  }
  var eu = {};
  function Sm(l, t, a, u) {
    this.tag = l, this.key = a, this.sibling = this.child = this.return = this.stateNode = this.type = this.elementType = null, this.index = 0, this.refCleanup = this.ref = null, this.pendingProps = t, this.dependencies = this.memoizedState = this.updateQueue = this.memoizedProps = null, this.mode = u, this.subtreeFlags = this.flags = 0, this.deletions = null, this.childLanes = this.lanes = 0, this.alternate = null;
  }
  function lt(l, t, a, u) {
    return new Sm(l, t, a, u);
  }
  function Cc(l) {
    return l = l.prototype, !(!l || !l.isReactComponent);
  }
  function Ct(l, t) {
    var a = l.alternate;
    return a === null ? (a = lt(
      l.tag,
      t,
      l.key,
      l.mode
    ), a.elementType = l.elementType, a.type = l.type, a.stateNode = l.stateNode, a.alternate = l, l.alternate = a) : (a.pendingProps = t, a.type = l.type, a.flags = 0, a.subtreeFlags = 0, a.deletions = null), a.flags = l.flags & 65011712, a.childLanes = l.childLanes, a.lanes = l.lanes, a.child = l.child, a.memoizedProps = l.memoizedProps, a.memoizedState = l.memoizedState, a.updateQueue = l.updateQueue, t = l.dependencies, a.dependencies = t === null ? null : { lanes: t.lanes, firstContext: t.firstContext }, a.sibling = l.sibling, a.index = l.index, a.ref = l.ref, a.refCleanup = l.refCleanup, a;
  }
  function Bo(l, t) {
    l.flags &= 65011714;
    var a = l.alternate;
    return a === null ? (l.childLanes = 0, l.lanes = t, l.child = null, l.subtreeFlags = 0, l.memoizedProps = null, l.memoizedState = null, l.updateQueue = null, l.dependencies = null, l.stateNode = null) : (l.childLanes = a.childLanes, l.lanes = a.lanes, l.child = a.child, l.subtreeFlags = 0, l.deletions = null, l.memoizedProps = a.memoizedProps, l.memoizedState = a.memoizedState, l.updateQueue = a.updateQueue, l.type = a.type, t = a.dependencies, l.dependencies = t === null ? null : {
      lanes: t.lanes,
      firstContext: t.firstContext
    }), l;
  }
  function $e(l, t, a, u, e, n) {
    var c = 0;
    if (u = l, typeof l == "function") Cc(l) && (c = 1);
    else if (typeof l == "string")
      c = Ev(
        l,
        a,
        N.current
      ) ? 26 : l === "html" || l === "head" || l === "body" ? 27 : 5;
    else
      l: switch (l) {
        case zt:
          return l = lt(31, a, t, e), l.elementType = zt, l.lanes = n, l;
        case Cl:
          return Na(a.children, e, n, t);
        case Dt:
          c = 8, e |= 24;
          break;
        case $l:
          return l = lt(12, a, t, e | 2), l.elementType = $l, l.lanes = n, l;
        case Et:
          return l = lt(13, a, t, e), l.elementType = Et, l.lanes = n, l;
        case Gl:
          return l = lt(19, a, t, e), l.elementType = Gl, l.lanes = n, l;
        default:
          if (typeof l == "object" && l !== null)
            switch (l.$$typeof) {
              case Rl:
                c = 10;
                break l;
              case Wt:
                c = 9;
                break l;
              case ct:
                c = 11;
                break l;
              case W:
                c = 14;
                break l;
              case Xl:
                c = 16, u = null;
                break l;
            }
          c = 29, a = Error(
            y(130, l === null ? "null" : typeof l, "")
          ), u = null;
      }
    return t = lt(c, a, t, e), t.elementType = l, t.type = u, t.lanes = n, t;
  }
  function Na(l, t, a, u) {
    return l = lt(7, l, u, t), l.lanes = a, l;
  }
  function Bc(l, t, a) {
    return l = lt(6, l, null, t), l.lanes = a, l;
  }
  function qo(l) {
    var t = lt(18, null, null, 0);
    return t.stateNode = l, t;
  }
  function qc(l, t, a) {
    return t = lt(
      4,
      l.children !== null ? l.children : [],
      l.key,
      t
    ), t.lanes = a, t.stateNode = {
      containerInfo: l.containerInfo,
      pendingChildren: null,
      implementation: l.implementation
    }, t;
  }
  var Yo = /* @__PURE__ */ new WeakMap();
  function st(l, t) {
    if (typeof l == "object" && l !== null) {
      var a = Yo.get(l);
      return a !== void 0 ? a : (t = {
        value: l,
        source: t,
        stack: Bi(t)
      }, Yo.set(l, t), t);
    }
    return {
      value: l,
      source: t,
      stack: Bi(t)
    };
  }
  var nu = [], cu = 0, Fe = null, Ju = 0, dt = [], yt = 0, It = null, _t = 1, pt = "";
  function Bt(l, t) {
    nu[cu++] = Ju, nu[cu++] = Fe, Fe = l, Ju = t;
  }
  function jo(l, t, a) {
    dt[yt++] = _t, dt[yt++] = pt, dt[yt++] = It, It = l;
    var u = _t;
    l = pt;
    var e = 32 - Il(u) - 1;
    u &= ~(1 << e), a += 1;
    var n = 32 - Il(t) + e;
    if (30 < n) {
      var c = e - e % 5;
      n = (u & (1 << c) - 1).toString(32), u >>= c, e -= c, _t = 1 << 32 - Il(t) + e | a << e | u, pt = n + l;
    } else
      _t = 1 << n | a << e | u, pt = l;
  }
  function Yc(l) {
    l.return !== null && (Bt(l, 1), jo(l, 1, 0));
  }
  function jc(l) {
    for (; l === Fe; )
      Fe = nu[--cu], nu[cu] = null, Ju = nu[--cu], nu[cu] = null;
    for (; l === It; )
      It = dt[--yt], dt[yt] = null, pt = dt[--yt], dt[yt] = null, _t = dt[--yt], dt[yt] = null;
  }
  function Go(l, t) {
    dt[yt++] = _t, dt[yt++] = pt, dt[yt++] = It, _t = t.id, pt = t.overflow, It = l;
  }
  var Ml = null, sl = null, $ = !1, Pt = null, mt = !1, Gc = Error(y(519));
  function la(l) {
    var t = Error(
      y(
        418,
        1 < arguments.length && arguments[1] !== void 0 && arguments[1] ? "text" : "HTML",
        ""
      )
    );
    throw wu(st(t, l)), Gc;
  }
  function Xo(l) {
    var t = l.stateNode, a = l.type, u = l.memoizedProps;
    switch (t[Ol] = l, t[Zl] = u, a) {
      case "dialog":
        V("cancel", t), V("close", t);
        break;
      case "iframe":
      case "object":
      case "embed":
        V("load", t);
        break;
      case "video":
      case "audio":
        for (a = 0; a < he.length; a++)
          V(he[a], t);
        break;
      case "source":
        V("error", t);
        break;
      case "img":
      case "image":
      case "link":
        V("error", t), V("load", t);
        break;
      case "details":
        V("toggle", t);
        break;
      case "input":
        V("invalid", t), ki(
          t,
          u.value,
          u.defaultValue,
          u.checked,
          u.defaultChecked,
          u.type,
          u.name,
          !0
        );
        break;
      case "select":
        V("invalid", t);
        break;
      case "textarea":
        V("invalid", t), Pi(t, u.value, u.defaultValue, u.children);
    }
    a = u.children, typeof a != "string" && typeof a != "number" && typeof a != "bigint" || t.textContent === "" + a || u.suppressHydrationWarning === !0 || a0(t.textContent, a) ? (u.popover != null && (V("beforetoggle", t), V("toggle", t)), u.onScroll != null && V("scroll", t), u.onScrollEnd != null && V("scrollend", t), u.onClick != null && (t.onclick = Rt), t = !0) : t = !1, t || la(l, !0);
  }
  function Qo(l) {
    for (Ml = l.return; Ml; )
      switch (Ml.tag) {
        case 5:
        case 31:
        case 13:
          mt = !1;
          return;
        case 27:
        case 3:
          mt = !0;
          return;
        default:
          Ml = Ml.return;
      }
  }
  function fu(l) {
    if (l !== Ml) return !1;
    if (!$) return Qo(l), $ = !0, !1;
    var t = l.tag, a;
    if ((a = t !== 3 && t !== 27) && ((a = t === 5) && (a = l.type, a = !(a !== "form" && a !== "button") || ui(l.type, l.memoizedProps)), a = !a), a && sl && la(l), Qo(l), t === 13) {
      if (l = l.memoizedState, l = l !== null ? l.dehydrated : null, !l) throw Error(y(317));
      sl = d0(l);
    } else if (t === 31) {
      if (l = l.memoizedState, l = l !== null ? l.dehydrated : null, !l) throw Error(y(317));
      sl = d0(l);
    } else
      t === 27 ? (t = sl, va(l.type) ? (l = ii, ii = null, sl = l) : sl = t) : sl = Ml ? ht(l.stateNode.nextSibling) : null;
    return !0;
  }
  function Ra() {
    sl = Ml = null, $ = !1;
  }
  function Xc() {
    var l = Pt;
    return l !== null && (Jl === null ? Jl = l : Jl.push.apply(
      Jl,
      l
    ), Pt = null), l;
  }
  function wu(l) {
    Pt === null ? Pt = [l] : Pt.push(l);
  }
  var Qc = d(null), Ha = null, qt = null;
  function ta(l, t, a) {
    M(Qc, t._currentValue), t._currentValue = a;
  }
  function Yt(l) {
    l._currentValue = Qc.current, _(Qc);
  }
  function Zc(l, t, a) {
    for (; l !== null; ) {
      var u = l.alternate;
      if ((l.childLanes & t) !== t ? (l.childLanes |= t, u !== null && (u.childLanes |= t)) : u !== null && (u.childLanes & t) !== t && (u.childLanes |= t), l === a) break;
      l = l.return;
    }
  }
  function Lc(l, t, a, u) {
    var e = l.child;
    for (e !== null && (e.return = l); e !== null; ) {
      var n = e.dependencies;
      if (n !== null) {
        var c = e.child;
        n = n.firstContext;
        l: for (; n !== null; ) {
          var f = n;
          n = e;
          for (var i = 0; i < t.length; i++)
            if (f.context === t[i]) {
              n.lanes |= a, f = n.alternate, f !== null && (f.lanes |= a), Zc(
                n.return,
                a,
                l
              ), u || (c = null);
              break l;
            }
          n = f.next;
        }
      } else if (e.tag === 18) {
        if (c = e.return, c === null) throw Error(y(341));
        c.lanes |= a, n = c.alternate, n !== null && (n.lanes |= a), Zc(c, a, l), c = null;
      } else c = e.child;
      if (c !== null) c.return = e;
      else
        for (c = e; c !== null; ) {
          if (c === l) {
            c = null;
            break;
          }
          if (e = c.sibling, e !== null) {
            e.return = c.return, c = e;
            break;
          }
          c = c.return;
        }
      e = c;
    }
  }
  function iu(l, t, a, u) {
    l = null;
    for (var e = t, n = !1; e !== null; ) {
      if (!n) {
        if ((e.flags & 524288) !== 0) n = !0;
        else if ((e.flags & 262144) !== 0) break;
      }
      if (e.tag === 10) {
        var c = e.alternate;
        if (c === null) throw Error(y(387));
        if (c = c.memoizedProps, c !== null) {
          var f = e.type;
          Pl(e.pendingProps.value, c.value) || (l !== null ? l.push(f) : l = [f]);
        }
      } else if (e === tl.current) {
        if (c = e.alternate, c === null) throw Error(y(387));
        c.memoizedState.memoizedState !== e.memoizedState.memoizedState && (l !== null ? l.push(Te) : l = [Te]);
      }
      e = e.return;
    }
    l !== null && Lc(
      t,
      l,
      a,
      u
    ), t.flags |= 262144;
  }
  function ke(l) {
    for (l = l.firstContext; l !== null; ) {
      if (!Pl(
        l.context._currentValue,
        l.memoizedValue
      ))
        return !0;
      l = l.next;
    }
    return !1;
  }
  function Ca(l) {
    Ha = l, qt = null, l = l.dependencies, l !== null && (l.firstContext = null);
  }
  function Dl(l) {
    return Zo(Ha, l);
  }
  function Ie(l, t) {
    return Ha === null && Ca(l), Zo(l, t);
  }
  function Zo(l, t) {
    var a = t._currentValue;
    if (t = { context: t, memoizedValue: a, next: null }, qt === null) {
      if (l === null) throw Error(y(308));
      qt = t, l.dependencies = { lanes: 0, firstContext: t }, l.flags |= 524288;
    } else qt = qt.next = t;
    return a;
  }
  var rm = typeof AbortController < "u" ? AbortController : function() {
    var l = [], t = this.signal = {
      aborted: !1,
      addEventListener: function(a, u) {
        l.push(u);
      }
    };
    this.abort = function() {
      t.aborted = !0, l.forEach(function(a) {
        return a();
      });
    };
  }, gm = o.unstable_scheduleCallback, bm = o.unstable_NormalPriority, bl = {
    $$typeof: Rl,
    Consumer: null,
    Provider: null,
    _currentValue: null,
    _currentValue2: null,
    _threadCount: 0
  };
  function xc() {
    return {
      controller: new rm(),
      data: /* @__PURE__ */ new Map(),
      refCount: 0
    };
  }
  function Wu(l) {
    l.refCount--, l.refCount === 0 && gm(bm, function() {
      l.controller.abort();
    });
  }
  var $u = null, Vc = 0, ou = 0, su = null;
  function Tm(l, t) {
    if ($u === null) {
      var a = $u = [];
      Vc = 0, ou = Wf(), su = {
        status: "pending",
        value: void 0,
        then: function(u) {
          a.push(u);
        }
      };
    }
    return Vc++, t.then(Lo, Lo), t;
  }
  function Lo() {
    if (--Vc === 0 && $u !== null) {
      su !== null && (su.status = "fulfilled");
      var l = $u;
      $u = null, ou = 0, su = null;
      for (var t = 0; t < l.length; t++) (0, l[t])();
    }
  }
  function Em(l, t) {
    var a = [], u = {
      status: "pending",
      value: null,
      reason: null,
      then: function(e) {
        a.push(e);
      }
    };
    return l.then(
      function() {
        u.status = "fulfilled", u.value = t;
        for (var e = 0; e < a.length; e++) (0, a[e])(t);
      },
      function(e) {
        for (u.status = "rejected", u.reason = e, e = 0; e < a.length; e++)
          (0, a[e])(void 0);
      }
    ), u;
  }
  var xo = E.S;
  E.S = function(l, t) {
    Od = Fl(), typeof t == "object" && t !== null && typeof t.then == "function" && Tm(l, t), xo !== null && xo(l, t);
  };
  var Ba = d(null);
  function Kc() {
    var l = Ba.current;
    return l !== null ? l : ol.pooledCache;
  }
  function Pe(l, t) {
    t === null ? M(Ba, Ba.current) : M(Ba, t.pool);
  }
  function Vo() {
    var l = Kc();
    return l === null ? null : { parent: bl._currentValue, pool: l };
  }
  var du = Error(y(460)), Jc = Error(y(474)), ln = Error(y(542)), tn = { then: function() {
  } };
  function Ko(l) {
    return l = l.status, l === "fulfilled" || l === "rejected";
  }
  function Jo(l, t, a) {
    switch (a = l[a], a === void 0 ? l.push(t) : a !== t && (t.then(Rt, Rt), t = a), t.status) {
      case "fulfilled":
        return t.value;
      case "rejected":
        throw l = t.reason, Wo(l), l;
      default:
        if (typeof t.status == "string") t.then(Rt, Rt);
        else {
          if (l = ol, l !== null && 100 < l.shellSuspendCounter)
            throw Error(y(482));
          l = t, l.status = "pending", l.then(
            function(u) {
              if (t.status === "pending") {
                var e = t;
                e.status = "fulfilled", e.value = u;
              }
            },
            function(u) {
              if (t.status === "pending") {
                var e = t;
                e.status = "rejected", e.reason = u;
              }
            }
          );
        }
        switch (t.status) {
          case "fulfilled":
            return t.value;
          case "rejected":
            throw l = t.reason, Wo(l), l;
        }
        throw Ya = t, du;
    }
  }
  function qa(l) {
    try {
      var t = l._init;
      return t(l._payload);
    } catch (a) {
      throw a !== null && typeof a == "object" && typeof a.then == "function" ? (Ya = a, du) : a;
    }
  }
  var Ya = null;
  function wo() {
    if (Ya === null) throw Error(y(459));
    var l = Ya;
    return Ya = null, l;
  }
  function Wo(l) {
    if (l === du || l === ln)
      throw Error(y(483));
  }
  var yu = null, Fu = 0;
  function an(l) {
    var t = Fu;
    return Fu += 1, yu === null && (yu = []), Jo(yu, l, t);
  }
  function ku(l, t) {
    t = t.props.ref, l.ref = t !== void 0 ? t : null;
  }
  function un(l, t) {
    throw t.$$typeof === ll ? Error(y(525)) : (l = Object.prototype.toString.call(t), Error(
      y(
        31,
        l === "[object Object]" ? "object with keys {" + Object.keys(t).join(", ") + "}" : l
      )
    ));
  }
  function $o(l) {
    function t(m, s) {
      if (l) {
        var v = m.deletions;
        v === null ? (m.deletions = [s], m.flags |= 16) : v.push(s);
      }
    }
    function a(m, s) {
      if (!l) return null;
      for (; s !== null; )
        t(m, s), s = s.sibling;
      return null;
    }
    function u(m) {
      for (var s = /* @__PURE__ */ new Map(); m !== null; )
        m.key !== null ? s.set(m.key, m) : s.set(m.index, m), m = m.sibling;
      return s;
    }
    function e(m, s) {
      return m = Ct(m, s), m.index = 0, m.sibling = null, m;
    }
    function n(m, s, v) {
      return m.index = v, l ? (v = m.alternate, v !== null ? (v = v.index, v < s ? (m.flags |= 67108866, s) : v) : (m.flags |= 67108866, s)) : (m.flags |= 1048576, s);
    }
    function c(m) {
      return l && m.alternate === null && (m.flags |= 67108866), m;
    }
    function f(m, s, v, z) {
      return s === null || s.tag !== 6 ? (s = Bc(v, m.mode, z), s.return = m, s) : (s = e(s, v), s.return = m, s);
    }
    function i(m, s, v, z) {
      var C = v.type;
      return C === Cl ? b(
        m,
        s,
        v.props.children,
        z,
        v.key
      ) : s !== null && (s.elementType === C || typeof C == "object" && C !== null && C.$$typeof === Xl && qa(C) === s.type) ? (s = e(s, v.props), ku(s, v), s.return = m, s) : (s = $e(
        v.type,
        v.key,
        v.props,
        null,
        m.mode,
        z
      ), ku(s, v), s.return = m, s);
    }
    function h(m, s, v, z) {
      return s === null || s.tag !== 4 || s.stateNode.containerInfo !== v.containerInfo || s.stateNode.implementation !== v.implementation ? (s = qc(v, m.mode, z), s.return = m, s) : (s = e(s, v.children || []), s.return = m, s);
    }
    function b(m, s, v, z, C) {
      return s === null || s.tag !== 7 ? (s = Na(
        v,
        m.mode,
        z,
        C
      ), s.return = m, s) : (s = e(s, v), s.return = m, s);
    }
    function A(m, s, v) {
      if (typeof s == "string" && s !== "" || typeof s == "number" || typeof s == "bigint")
        return s = Bc(
          "" + s,
          m.mode,
          v
        ), s.return = m, s;
      if (typeof s == "object" && s !== null) {
        switch (s.$$typeof) {
          case Wl:
            return v = $e(
              s.type,
              s.key,
              s.props,
              null,
              m.mode,
              v
            ), ku(v, s), v.return = m, v;
          case jl:
            return s = qc(
              s,
              m.mode,
              v
            ), s.return = m, s;
          case Xl:
            return s = qa(s), A(m, s, v);
        }
        if (rt(s) || Ql(s))
          return s = Na(
            s,
            m.mode,
            v,
            null
          ), s.return = m, s;
        if (typeof s.then == "function")
          return A(m, an(s), v);
        if (s.$$typeof === Rl)
          return A(
            m,
            Ie(m, s),
            v
          );
        un(m, s);
      }
      return null;
    }
    function S(m, s, v, z) {
      var C = s !== null ? s.key : null;
      if (typeof v == "string" && v !== "" || typeof v == "number" || typeof v == "bigint")
        return C !== null ? null : f(m, s, "" + v, z);
      if (typeof v == "object" && v !== null) {
        switch (v.$$typeof) {
          case Wl:
            return v.key === C ? i(m, s, v, z) : null;
          case jl:
            return v.key === C ? h(m, s, v, z) : null;
          case Xl:
            return v = qa(v), S(m, s, v, z);
        }
        if (rt(v) || Ql(v))
          return C !== null ? null : b(m, s, v, z, null);
        if (typeof v.then == "function")
          return S(
            m,
            s,
            an(v),
            z
          );
        if (v.$$typeof === Rl)
          return S(
            m,
            s,
            Ie(m, v),
            z
          );
        un(m, v);
      }
      return null;
    }
    function r(m, s, v, z, C) {
      if (typeof z == "string" && z !== "" || typeof z == "number" || typeof z == "bigint")
        return m = m.get(v) || null, f(s, m, "" + z, C);
      if (typeof z == "object" && z !== null) {
        switch (z.$$typeof) {
          case Wl:
            return m = m.get(
              z.key === null ? v : z.key
            ) || null, i(s, m, z, C);
          case jl:
            return m = m.get(
              z.key === null ? v : z.key
            ) || null, h(s, m, z, C);
          case Xl:
            return z = qa(z), r(
              m,
              s,
              v,
              z,
              C
            );
        }
        if (rt(z) || Ql(z))
          return m = m.get(v) || null, b(s, m, z, C, null);
        if (typeof z.then == "function")
          return r(
            m,
            s,
            v,
            an(z),
            C
          );
        if (z.$$typeof === Rl)
          return r(
            m,
            s,
            v,
            Ie(s, z),
            C
          );
        un(s, z);
      }
      return null;
    }
    function U(m, s, v, z) {
      for (var C = null, k = null, R = s, Q = s = 0, w = null; R !== null && Q < v.length; Q++) {
        R.index > Q ? (w = R, R = null) : w = R.sibling;
        var I = S(
          m,
          R,
          v[Q],
          z
        );
        if (I === null) {
          R === null && (R = w);
          break;
        }
        l && R && I.alternate === null && t(m, R), s = n(I, s, Q), k === null ? C = I : k.sibling = I, k = I, R = w;
      }
      if (Q === v.length)
        return a(m, R), $ && Bt(m, Q), C;
      if (R === null) {
        for (; Q < v.length; Q++)
          R = A(m, v[Q], z), R !== null && (s = n(
            R,
            s,
            Q
          ), k === null ? C = R : k.sibling = R, k = R);
        return $ && Bt(m, Q), C;
      }
      for (R = u(R); Q < v.length; Q++)
        w = r(
          R,
          m,
          Q,
          v[Q],
          z
        ), w !== null && (l && w.alternate !== null && R.delete(
          w.key === null ? Q : w.key
        ), s = n(
          w,
          s,
          Q
        ), k === null ? C = w : k.sibling = w, k = w);
      return l && R.forEach(function(ba) {
        return t(m, ba);
      }), $ && Bt(m, Q), C;
    }
    function B(m, s, v, z) {
      if (v == null) throw Error(y(151));
      for (var C = null, k = null, R = s, Q = s = 0, w = null, I = v.next(); R !== null && !I.done; Q++, I = v.next()) {
        R.index > Q ? (w = R, R = null) : w = R.sibling;
        var ba = S(m, R, I.value, z);
        if (ba === null) {
          R === null && (R = w);
          break;
        }
        l && R && ba.alternate === null && t(m, R), s = n(ba, s, Q), k === null ? C = ba : k.sibling = ba, k = ba, R = w;
      }
      if (I.done)
        return a(m, R), $ && Bt(m, Q), C;
      if (R === null) {
        for (; !I.done; Q++, I = v.next())
          I = A(m, I.value, z), I !== null && (s = n(I, s, Q), k === null ? C = I : k.sibling = I, k = I);
        return $ && Bt(m, Q), C;
      }
      for (R = u(R); !I.done; Q++, I = v.next())
        I = r(R, m, Q, I.value, z), I !== null && (l && I.alternate !== null && R.delete(I.key === null ? Q : I.key), s = n(I, s, Q), k === null ? C = I : k.sibling = I, k = I);
      return l && R.forEach(function(Hv) {
        return t(m, Hv);
      }), $ && Bt(m, Q), C;
    }
    function fl(m, s, v, z) {
      if (typeof v == "object" && v !== null && v.type === Cl && v.key === null && (v = v.props.children), typeof v == "object" && v !== null) {
        switch (v.$$typeof) {
          case Wl:
            l: {
              for (var C = v.key; s !== null; ) {
                if (s.key === C) {
                  if (C = v.type, C === Cl) {
                    if (s.tag === 7) {
                      a(
                        m,
                        s.sibling
                      ), z = e(
                        s,
                        v.props.children
                      ), z.return = m, m = z;
                      break l;
                    }
                  } else if (s.elementType === C || typeof C == "object" && C !== null && C.$$typeof === Xl && qa(C) === s.type) {
                    a(
                      m,
                      s.sibling
                    ), z = e(s, v.props), ku(z, v), z.return = m, m = z;
                    break l;
                  }
                  a(m, s);
                  break;
                } else t(m, s);
                s = s.sibling;
              }
              v.type === Cl ? (z = Na(
                v.props.children,
                m.mode,
                z,
                v.key
              ), z.return = m, m = z) : (z = $e(
                v.type,
                v.key,
                v.props,
                null,
                m.mode,
                z
              ), ku(z, v), z.return = m, m = z);
            }
            return c(m);
          case jl:
            l: {
              for (C = v.key; s !== null; ) {
                if (s.key === C)
                  if (s.tag === 4 && s.stateNode.containerInfo === v.containerInfo && s.stateNode.implementation === v.implementation) {
                    a(
                      m,
                      s.sibling
                    ), z = e(s, v.children || []), z.return = m, m = z;
                    break l;
                  } else {
                    a(m, s);
                    break;
                  }
                else t(m, s);
                s = s.sibling;
              }
              z = qc(v, m.mode, z), z.return = m, m = z;
            }
            return c(m);
          case Xl:
            return v = qa(v), fl(
              m,
              s,
              v,
              z
            );
        }
        if (rt(v))
          return U(
            m,
            s,
            v,
            z
          );
        if (Ql(v)) {
          if (C = Ql(v), typeof C != "function") throw Error(y(150));
          return v = C.call(v), B(
            m,
            s,
            v,
            z
          );
        }
        if (typeof v.then == "function")
          return fl(
            m,
            s,
            an(v),
            z
          );
        if (v.$$typeof === Rl)
          return fl(
            m,
            s,
            Ie(m, v),
            z
          );
        un(m, v);
      }
      return typeof v == "string" && v !== "" || typeof v == "number" || typeof v == "bigint" ? (v = "" + v, s !== null && s.tag === 6 ? (a(m, s.sibling), z = e(s, v), z.return = m, m = z) : (a(m, s), z = Bc(v, m.mode, z), z.return = m, m = z), c(m)) : a(m, s);
    }
    return function(m, s, v, z) {
      try {
        Fu = 0;
        var C = fl(
          m,
          s,
          v,
          z
        );
        return yu = null, C;
      } catch (R) {
        if (R === du || R === ln) throw R;
        var k = lt(29, R, null, m.mode);
        return k.lanes = z, k.return = m, k;
      } finally {
      }
    };
  }
  var ja = $o(!0), Fo = $o(!1), aa = !1;
  function wc(l) {
    l.updateQueue = {
      baseState: l.memoizedState,
      firstBaseUpdate: null,
      lastBaseUpdate: null,
      shared: { pending: null, lanes: 0, hiddenCallbacks: null },
      callbacks: null
    };
  }
  function Wc(l, t) {
    l = l.updateQueue, t.updateQueue === l && (t.updateQueue = {
      baseState: l.baseState,
      firstBaseUpdate: l.firstBaseUpdate,
      lastBaseUpdate: l.lastBaseUpdate,
      shared: l.shared,
      callbacks: null
    });
  }
  function ua(l) {
    return { lane: l, tag: 0, payload: null, callback: null, next: null };
  }
  function ea(l, t, a) {
    var u = l.updateQueue;
    if (u === null) return null;
    if (u = u.shared, (P & 2) !== 0) {
      var e = u.pending;
      return e === null ? t.next = t : (t.next = e.next, e.next = t), u.pending = t, t = We(l), Co(l, null, a), t;
    }
    return we(l, u, t, a), We(l);
  }
  function Iu(l, t, a) {
    if (t = t.updateQueue, t !== null && (t = t.shared, (a & 4194048) !== 0)) {
      var u = t.lanes;
      u &= l.pendingLanes, a |= u, t.lanes = a, Qi(l, a);
    }
  }
  function $c(l, t) {
    var a = l.updateQueue, u = l.alternate;
    if (u !== null && (u = u.updateQueue, a === u)) {
      var e = null, n = null;
      if (a = a.firstBaseUpdate, a !== null) {
        do {
          var c = {
            lane: a.lane,
            tag: a.tag,
            payload: a.payload,
            callback: null,
            next: null
          };
          n === null ? e = n = c : n = n.next = c, a = a.next;
        } while (a !== null);
        n === null ? e = n = t : n = n.next = t;
      } else e = n = t;
      a = {
        baseState: u.baseState,
        firstBaseUpdate: e,
        lastBaseUpdate: n,
        shared: u.shared,
        callbacks: u.callbacks
      }, l.updateQueue = a;
      return;
    }
    l = a.lastBaseUpdate, l === null ? a.firstBaseUpdate = t : l.next = t, a.lastBaseUpdate = t;
  }
  var Fc = !1;
  function Pu() {
    if (Fc) {
      var l = su;
      if (l !== null) throw l;
    }
  }
  function le(l, t, a, u) {
    Fc = !1;
    var e = l.updateQueue;
    aa = !1;
    var n = e.firstBaseUpdate, c = e.lastBaseUpdate, f = e.shared.pending;
    if (f !== null) {
      e.shared.pending = null;
      var i = f, h = i.next;
      i.next = null, c === null ? n = h : c.next = h, c = i;
      var b = l.alternate;
      b !== null && (b = b.updateQueue, f = b.lastBaseUpdate, f !== c && (f === null ? b.firstBaseUpdate = h : f.next = h, b.lastBaseUpdate = i));
    }
    if (n !== null) {
      var A = e.baseState;
      c = 0, b = h = i = null, f = n;
      do {
        var S = f.lane & -536870913, r = S !== f.lane;
        if (r ? (J & S) === S : (u & S) === S) {
          S !== 0 && S === ou && (Fc = !0), b !== null && (b = b.next = {
            lane: 0,
            tag: f.tag,
            payload: f.payload,
            callback: null,
            next: null
          });
          l: {
            var U = l, B = f;
            S = t;
            var fl = a;
            switch (B.tag) {
              case 1:
                if (U = B.payload, typeof U == "function") {
                  A = U.call(fl, A, S);
                  break l;
                }
                A = U;
                break l;
              case 3:
                U.flags = U.flags & -65537 | 128;
              case 0:
                if (U = B.payload, S = typeof U == "function" ? U.call(fl, A, S) : U, S == null) break l;
                A = q({}, A, S);
                break l;
              case 2:
                aa = !0;
            }
          }
          S = f.callback, S !== null && (l.flags |= 64, r && (l.flags |= 8192), r = e.callbacks, r === null ? e.callbacks = [S] : r.push(S));
        } else
          r = {
            lane: S,
            tag: f.tag,
            payload: f.payload,
            callback: f.callback,
            next: null
          }, b === null ? (h = b = r, i = A) : b = b.next = r, c |= S;
        if (f = f.next, f === null) {
          if (f = e.shared.pending, f === null)
            break;
          r = f, f = r.next, r.next = null, e.lastBaseUpdate = r, e.shared.pending = null;
        }
      } while (!0);
      b === null && (i = A), e.baseState = i, e.firstBaseUpdate = h, e.lastBaseUpdate = b, n === null && (e.shared.lanes = 0), oa |= c, l.lanes = c, l.memoizedState = A;
    }
  }
  function ko(l, t) {
    if (typeof l != "function")
      throw Error(y(191, l));
    l.call(t);
  }
  function Io(l, t) {
    var a = l.callbacks;
    if (a !== null)
      for (l.callbacks = null, l = 0; l < a.length; l++)
        ko(a[l], t);
  }
  var mu = d(null), en = d(0);
  function Po(l, t) {
    l = Kt, M(en, l), M(mu, t), Kt = l | t.baseLanes;
  }
  function kc() {
    M(en, Kt), M(mu, mu.current);
  }
  function Ic() {
    Kt = en.current, _(mu), _(en);
  }
  var tt = d(null), vt = null;
  function na(l) {
    var t = l.alternate;
    M(Sl, Sl.current & 1), M(tt, l), vt === null && (t === null || mu.current !== null || t.memoizedState !== null) && (vt = l);
  }
  function Pc(l) {
    M(Sl, Sl.current), M(tt, l), vt === null && (vt = l);
  }
  function ls(l) {
    l.tag === 22 ? (M(Sl, Sl.current), M(tt, l), vt === null && (vt = l)) : ca();
  }
  function ca() {
    M(Sl, Sl.current), M(tt, tt.current);
  }
  function at(l) {
    _(tt), vt === l && (vt = null), _(Sl);
  }
  var Sl = d(0);
  function nn(l) {
    for (var t = l; t !== null; ) {
      if (t.tag === 13) {
        var a = t.memoizedState;
        if (a !== null && (a = a.dehydrated, a === null || ci(a) || fi(a)))
          return t;
      } else if (t.tag === 19 && (t.memoizedProps.revealOrder === "forwards" || t.memoizedProps.revealOrder === "backwards" || t.memoizedProps.revealOrder === "unstable_legacy-backwards" || t.memoizedProps.revealOrder === "together")) {
        if ((t.flags & 128) !== 0) return t;
      } else if (t.child !== null) {
        t.child.return = t, t = t.child;
        continue;
      }
      if (t === l) break;
      for (; t.sibling === null; ) {
        if (t.return === null || t.return === l) return null;
        t = t.return;
      }
      t.sibling.return = t.return, t = t.sibling;
    }
    return null;
  }
  var jt = 0, X = null, nl = null, Tl = null, cn = !1, vu = !1, Ga = !1, fn = 0, te = 0, hu = null, zm = 0;
  function ml() {
    throw Error(y(321));
  }
  function lf(l, t) {
    if (t === null) return !1;
    for (var a = 0; a < t.length && a < l.length; a++)
      if (!Pl(l[a], t[a])) return !1;
    return !0;
  }
  function tf(l, t, a, u, e, n) {
    return jt = n, X = t, t.memoizedState = null, t.updateQueue = null, t.lanes = 0, E.H = l === null || l.memoizedState === null ? js : rf, Ga = !1, n = a(u, e), Ga = !1, vu && (n = as(
      t,
      a,
      u,
      e
    )), ts(l), n;
  }
  function ts(l) {
    E.H = ee;
    var t = nl !== null && nl.next !== null;
    if (jt = 0, Tl = nl = X = null, cn = !1, te = 0, hu = null, t) throw Error(y(300));
    l === null || El || (l = l.dependencies, l !== null && ke(l) && (El = !0));
  }
  function as(l, t, a, u) {
    X = l;
    var e = 0;
    do {
      if (vu && (hu = null), te = 0, vu = !1, 25 <= e) throw Error(y(301));
      if (e += 1, Tl = nl = null, l.updateQueue != null) {
        var n = l.updateQueue;
        n.lastEffect = null, n.events = null, n.stores = null, n.memoCache != null && (n.memoCache.index = 0);
      }
      E.H = Gs, n = t(a, u);
    } while (vu);
    return n;
  }
  function Am() {
    var l = E.H, t = l.useState()[0];
    return t = typeof t.then == "function" ? ae(t) : t, l = l.useState()[0], (nl !== null ? nl.memoizedState : null) !== l && (X.flags |= 1024), t;
  }
  function af() {
    var l = fn !== 0;
    return fn = 0, l;
  }
  function uf(l, t, a) {
    t.updateQueue = l.updateQueue, t.flags &= -2053, l.lanes &= ~a;
  }
  function ef(l) {
    if (cn) {
      for (l = l.memoizedState; l !== null; ) {
        var t = l.queue;
        t !== null && (t.pending = null), l = l.next;
      }
      cn = !1;
    }
    jt = 0, Tl = nl = X = null, vu = !1, te = fn = 0, hu = null;
  }
  function ql() {
    var l = {
      memoizedState: null,
      baseState: null,
      baseQueue: null,
      queue: null,
      next: null
    };
    return Tl === null ? X.memoizedState = Tl = l : Tl = Tl.next = l, Tl;
  }
  function rl() {
    if (nl === null) {
      var l = X.alternate;
      l = l !== null ? l.memoizedState : null;
    } else l = nl.next;
    var t = Tl === null ? X.memoizedState : Tl.next;
    if (t !== null)
      Tl = t, nl = l;
    else {
      if (l === null)
        throw X.alternate === null ? Error(y(467)) : Error(y(310));
      nl = l, l = {
        memoizedState: nl.memoizedState,
        baseState: nl.baseState,
        baseQueue: nl.baseQueue,
        queue: nl.queue,
        next: null
      }, Tl === null ? X.memoizedState = Tl = l : Tl = Tl.next = l;
    }
    return Tl;
  }
  function on() {
    return { lastEffect: null, events: null, stores: null, memoCache: null };
  }
  function ae(l) {
    var t = te;
    return te += 1, hu === null && (hu = []), l = Jo(hu, l, t), t = X, (Tl === null ? t.memoizedState : Tl.next) === null && (t = t.alternate, E.H = t === null || t.memoizedState === null ? js : rf), l;
  }
  function sn(l) {
    if (l !== null && typeof l == "object") {
      if (typeof l.then == "function") return ae(l);
      if (l.$$typeof === Rl) return Dl(l);
    }
    throw Error(y(438, String(l)));
  }
  function nf(l) {
    var t = null, a = X.updateQueue;
    if (a !== null && (t = a.memoCache), t == null) {
      var u = X.alternate;
      u !== null && (u = u.updateQueue, u !== null && (u = u.memoCache, u != null && (t = {
        data: u.data.map(function(e) {
          return e.slice();
        }),
        index: 0
      })));
    }
    if (t == null && (t = { data: [], index: 0 }), a === null && (a = on(), X.updateQueue = a), a.memoCache = t, a = t.data[t.index], a === void 0)
      for (a = t.data[t.index] = Array(l), u = 0; u < l; u++)
        a[u] = Va;
    return t.index++, a;
  }
  function Gt(l, t) {
    return typeof t == "function" ? t(l) : t;
  }
  function dn(l) {
    var t = rl();
    return cf(t, nl, l);
  }
  function cf(l, t, a) {
    var u = l.queue;
    if (u === null) throw Error(y(311));
    u.lastRenderedReducer = a;
    var e = l.baseQueue, n = u.pending;
    if (n !== null) {
      if (e !== null) {
        var c = e.next;
        e.next = n.next, n.next = c;
      }
      t.baseQueue = e = n, u.pending = null;
    }
    if (n = l.baseState, e === null) l.memoizedState = n;
    else {
      t = e.next;
      var f = c = null, i = null, h = t, b = !1;
      do {
        var A = h.lane & -536870913;
        if (A !== h.lane ? (J & A) === A : (jt & A) === A) {
          var S = h.revertLane;
          if (S === 0)
            i !== null && (i = i.next = {
              lane: 0,
              revertLane: 0,
              gesture: null,
              action: h.action,
              hasEagerState: h.hasEagerState,
              eagerState: h.eagerState,
              next: null
            }), A === ou && (b = !0);
          else if ((jt & S) === S) {
            h = h.next, S === ou && (b = !0);
            continue;
          } else
            A = {
              lane: 0,
              revertLane: h.revertLane,
              gesture: null,
              action: h.action,
              hasEagerState: h.hasEagerState,
              eagerState: h.eagerState,
              next: null
            }, i === null ? (f = i = A, c = n) : i = i.next = A, X.lanes |= S, oa |= S;
          A = h.action, Ga && a(n, A), n = h.hasEagerState ? h.eagerState : a(n, A);
        } else
          S = {
            lane: A,
            revertLane: h.revertLane,
            gesture: h.gesture,
            action: h.action,
            hasEagerState: h.hasEagerState,
            eagerState: h.eagerState,
            next: null
          }, i === null ? (f = i = S, c = n) : i = i.next = S, X.lanes |= A, oa |= A;
        h = h.next;
      } while (h !== null && h !== t);
      if (i === null ? c = n : i.next = f, !Pl(n, l.memoizedState) && (El = !0, b && (a = su, a !== null)))
        throw a;
      l.memoizedState = n, l.baseState = c, l.baseQueue = i, u.lastRenderedState = n;
    }
    return e === null && (u.lanes = 0), [l.memoizedState, u.dispatch];
  }
  function ff(l) {
    var t = rl(), a = t.queue;
    if (a === null) throw Error(y(311));
    a.lastRenderedReducer = l;
    var u = a.dispatch, e = a.pending, n = t.memoizedState;
    if (e !== null) {
      a.pending = null;
      var c = e = e.next;
      do
        n = l(n, c.action), c = c.next;
      while (c !== e);
      Pl(n, t.memoizedState) || (El = !0), t.memoizedState = n, t.baseQueue === null && (t.baseState = n), a.lastRenderedState = n;
    }
    return [n, u];
  }
  function us(l, t, a) {
    var u = X, e = rl(), n = $;
    if (n) {
      if (a === void 0) throw Error(y(407));
      a = a();
    } else a = t();
    var c = !Pl(
      (nl || e).memoizedState,
      a
    );
    if (c && (e.memoizedState = a, El = !0), e = e.queue, df(cs.bind(null, u, e, l), [
      l
    ]), e.getSnapshot !== t || c || Tl !== null && Tl.memoizedState.tag & 1) {
      if (u.flags |= 2048, Su(
        9,
        { destroy: void 0 },
        ns.bind(
          null,
          u,
          e,
          a,
          t
        ),
        null
      ), ol === null) throw Error(y(349));
      n || (jt & 127) !== 0 || es(u, t, a);
    }
    return a;
  }
  function es(l, t, a) {
    l.flags |= 16384, l = { getSnapshot: t, value: a }, t = X.updateQueue, t === null ? (t = on(), X.updateQueue = t, t.stores = [l]) : (a = t.stores, a === null ? t.stores = [l] : a.push(l));
  }
  function ns(l, t, a, u) {
    t.value = a, t.getSnapshot = u, fs(t) && is(l);
  }
  function cs(l, t, a) {
    return a(function() {
      fs(t) && is(l);
    });
  }
  function fs(l) {
    var t = l.getSnapshot;
    l = l.value;
    try {
      var a = t();
      return !Pl(l, a);
    } catch {
      return !0;
    }
  }
  function is(l) {
    var t = Ua(l, 2);
    t !== null && wl(t, l, 2);
  }
  function of(l) {
    var t = ql();
    if (typeof l == "function") {
      var a = l;
      if (l = a(), Ga) {
        $t(!0);
        try {
          a();
        } finally {
          $t(!1);
        }
      }
    }
    return t.memoizedState = t.baseState = l, t.queue = {
      pending: null,
      lanes: 0,
      dispatch: null,
      lastRenderedReducer: Gt,
      lastRenderedState: l
    }, t;
  }
  function os(l, t, a, u) {
    return l.baseState = a, cf(
      l,
      nl,
      typeof u == "function" ? u : Gt
    );
  }
  function _m(l, t, a, u, e) {
    if (vn(l)) throw Error(y(485));
    if (l = t.action, l !== null) {
      var n = {
        payload: e,
        action: l,
        next: null,
        isTransition: !0,
        status: "pending",
        value: null,
        reason: null,
        listeners: [],
        then: function(c) {
          n.listeners.push(c);
        }
      };
      E.T !== null ? a(!0) : n.isTransition = !1, u(n), a = t.pending, a === null ? (n.next = t.pending = n, ss(t, n)) : (n.next = a.next, t.pending = a.next = n);
    }
  }
  function ss(l, t) {
    var a = t.action, u = t.payload, e = l.state;
    if (t.isTransition) {
      var n = E.T, c = {};
      E.T = c;
      try {
        var f = a(e, u), i = E.S;
        i !== null && i(c, f), ds(l, t, f);
      } catch (h) {
        sf(l, t, h);
      } finally {
        n !== null && c.types !== null && (n.types = c.types), E.T = n;
      }
    } else
      try {
        n = a(e, u), ds(l, t, n);
      } catch (h) {
        sf(l, t, h);
      }
  }
  function ds(l, t, a) {
    a !== null && typeof a == "object" && typeof a.then == "function" ? a.then(
      function(u) {
        ys(l, t, u);
      },
      function(u) {
        return sf(l, t, u);
      }
    ) : ys(l, t, a);
  }
  function ys(l, t, a) {
    t.status = "fulfilled", t.value = a, ms(t), l.state = a, t = l.pending, t !== null && (a = t.next, a === t ? l.pending = null : (a = a.next, t.next = a, ss(l, a)));
  }
  function sf(l, t, a) {
    var u = l.pending;
    if (l.pending = null, u !== null) {
      u = u.next;
      do
        t.status = "rejected", t.reason = a, ms(t), t = t.next;
      while (t !== u);
    }
    l.action = null;
  }
  function ms(l) {
    l = l.listeners;
    for (var t = 0; t < l.length; t++) (0, l[t])();
  }
  function vs(l, t) {
    return t;
  }
  function hs(l, t) {
    if ($) {
      var a = ol.formState;
      if (a !== null) {
        l: {
          var u = X;
          if ($) {
            if (sl) {
              t: {
                for (var e = sl, n = mt; e.nodeType !== 8; ) {
                  if (!n) {
                    e = null;
                    break t;
                  }
                  if (e = ht(
                    e.nextSibling
                  ), e === null) {
                    e = null;
                    break t;
                  }
                }
                n = e.data, e = n === "F!" || n === "F" ? e : null;
              }
              if (e) {
                sl = ht(
                  e.nextSibling
                ), u = e.data === "F!";
                break l;
              }
            }
            la(u);
          }
          u = !1;
        }
        u && (t = a[0]);
      }
    }
    return a = ql(), a.memoizedState = a.baseState = t, u = {
      pending: null,
      lanes: 0,
      dispatch: null,
      lastRenderedReducer: vs,
      lastRenderedState: t
    }, a.queue = u, a = Bs.bind(
      null,
      X,
      u
    ), u.dispatch = a, u = of(!1), n = Sf.bind(
      null,
      X,
      !1,
      u.queue
    ), u = ql(), e = {
      state: t,
      dispatch: null,
      action: l,
      pending: null
    }, u.queue = e, a = _m.bind(
      null,
      X,
      e,
      n,
      a
    ), e.dispatch = a, u.memoizedState = l, [t, a, !1];
  }
  function Ss(l) {
    var t = rl();
    return rs(t, nl, l);
  }
  function rs(l, t, a) {
    if (t = cf(
      l,
      t,
      vs
    )[0], l = dn(Gt)[0], typeof t == "object" && t !== null && typeof t.then == "function")
      try {
        var u = ae(t);
      } catch (c) {
        throw c === du ? ln : c;
      }
    else u = t;
    t = rl();
    var e = t.queue, n = e.dispatch;
    return a !== t.memoizedState && (X.flags |= 2048, Su(
      9,
      { destroy: void 0 },
      pm.bind(null, e, a),
      null
    )), [u, n, l];
  }
  function pm(l, t) {
    l.action = t;
  }
  function gs(l) {
    var t = rl(), a = nl;
    if (a !== null)
      return rs(t, a, l);
    rl(), t = t.memoizedState, a = rl();
    var u = a.queue.dispatch;
    return a.memoizedState = l, [t, u, !1];
  }
  function Su(l, t, a, u) {
    return l = { tag: l, create: a, deps: u, inst: t, next: null }, t = X.updateQueue, t === null && (t = on(), X.updateQueue = t), a = t.lastEffect, a === null ? t.lastEffect = l.next = l : (u = a.next, a.next = l, l.next = u, t.lastEffect = l), l;
  }
  function bs() {
    return rl().memoizedState;
  }
  function yn(l, t, a, u) {
    var e = ql();
    X.flags |= l, e.memoizedState = Su(
      1 | t,
      { destroy: void 0 },
      a,
      u === void 0 ? null : u
    );
  }
  function mn(l, t, a, u) {
    var e = rl();
    u = u === void 0 ? null : u;
    var n = e.memoizedState.inst;
    nl !== null && u !== null && lf(u, nl.memoizedState.deps) ? e.memoizedState = Su(t, n, a, u) : (X.flags |= l, e.memoizedState = Su(
      1 | t,
      n,
      a,
      u
    ));
  }
  function Ts(l, t) {
    yn(8390656, 8, l, t);
  }
  function df(l, t) {
    mn(2048, 8, l, t);
  }
  function Om(l) {
    X.flags |= 4;
    var t = X.updateQueue;
    if (t === null)
      t = on(), X.updateQueue = t, t.events = [l];
    else {
      var a = t.events;
      a === null ? t.events = [l] : a.push(l);
    }
  }
  function Es(l) {
    var t = rl().memoizedState;
    return Om({ ref: t, nextImpl: l }), function() {
      if ((P & 2) !== 0) throw Error(y(440));
      return t.impl.apply(void 0, arguments);
    };
  }
  function zs(l, t) {
    return mn(4, 2, l, t);
  }
  function As(l, t) {
    return mn(4, 4, l, t);
  }
  function _s(l, t) {
    if (typeof t == "function") {
      l = l();
      var a = t(l);
      return function() {
        typeof a == "function" ? a() : t(null);
      };
    }
    if (t != null)
      return l = l(), t.current = l, function() {
        t.current = null;
      };
  }
  function ps(l, t, a) {
    a = a != null ? a.concat([l]) : null, mn(4, 4, _s.bind(null, t, l), a);
  }
  function yf() {
  }
  function Os(l, t) {
    var a = rl();
    t = t === void 0 ? null : t;
    var u = a.memoizedState;
    return t !== null && lf(t, u[1]) ? u[0] : (a.memoizedState = [l, t], l);
  }
  function Ms(l, t) {
    var a = rl();
    t = t === void 0 ? null : t;
    var u = a.memoizedState;
    if (t !== null && lf(t, u[1]))
      return u[0];
    if (u = l(), Ga) {
      $t(!0);
      try {
        l();
      } finally {
        $t(!1);
      }
    }
    return a.memoizedState = [u, t], u;
  }
  function mf(l, t, a) {
    return a === void 0 || (jt & 1073741824) !== 0 && (J & 261930) === 0 ? l.memoizedState = t : (l.memoizedState = a, l = Dd(), X.lanes |= l, oa |= l, a);
  }
  function Ds(l, t, a, u) {
    return Pl(a, t) ? a : mu.current !== null ? (l = mf(l, a, u), Pl(l, t) || (El = !0), l) : (jt & 42) === 0 || (jt & 1073741824) !== 0 && (J & 261930) === 0 ? (El = !0, l.memoizedState = a) : (l = Dd(), X.lanes |= l, oa |= l, t);
  }
  function Us(l, t, a, u, e) {
    var n = O.p;
    O.p = n !== 0 && 8 > n ? n : 8;
    var c = E.T, f = {};
    E.T = f, Sf(l, !1, t, a);
    try {
      var i = e(), h = E.S;
      if (h !== null && h(f, i), i !== null && typeof i == "object" && typeof i.then == "function") {
        var b = Em(
          i,
          u
        );
        ue(
          l,
          t,
          b,
          nt(l)
        );
      } else
        ue(
          l,
          t,
          u,
          nt(l)
        );
    } catch (A) {
      ue(
        l,
        t,
        { then: function() {
        }, status: "rejected", reason: A },
        nt()
      );
    } finally {
      O.p = n, c !== null && f.types !== null && (c.types = f.types), E.T = c;
    }
  }
  function Mm() {
  }
  function vf(l, t, a, u) {
    if (l.tag !== 5) throw Error(y(476));
    var e = Ns(l).queue;
    Us(
      l,
      e,
      t,
      Y,
      a === null ? Mm : function() {
        return Rs(l), a(u);
      }
    );
  }
  function Ns(l) {
    var t = l.memoizedState;
    if (t !== null) return t;
    t = {
      memoizedState: Y,
      baseState: Y,
      baseQueue: null,
      queue: {
        pending: null,
        lanes: 0,
        dispatch: null,
        lastRenderedReducer: Gt,
        lastRenderedState: Y
      },
      next: null
    };
    var a = {};
    return t.next = {
      memoizedState: a,
      baseState: a,
      baseQueue: null,
      queue: {
        pending: null,
        lanes: 0,
        dispatch: null,
        lastRenderedReducer: Gt,
        lastRenderedState: a
      },
      next: null
    }, l.memoizedState = t, l = l.alternate, l !== null && (l.memoizedState = t), t;
  }
  function Rs(l) {
    var t = Ns(l);
    t.next === null && (t = l.alternate.memoizedState), ue(
      l,
      t.next.queue,
      {},
      nt()
    );
  }
  function hf() {
    return Dl(Te);
  }
  function Hs() {
    return rl().memoizedState;
  }
  function Cs() {
    return rl().memoizedState;
  }
  function Dm(l) {
    for (var t = l.return; t !== null; ) {
      switch (t.tag) {
        case 24:
        case 3:
          var a = nt();
          l = ua(a);
          var u = ea(t, l, a);
          u !== null && (wl(u, t, a), Iu(u, t, a)), t = { cache: xc() }, l.payload = t;
          return;
      }
      t = t.return;
    }
  }
  function Um(l, t, a) {
    var u = nt();
    a = {
      lane: u,
      revertLane: 0,
      gesture: null,
      action: a,
      hasEagerState: !1,
      eagerState: null,
      next: null
    }, vn(l) ? qs(t, a) : (a = Hc(l, t, a, u), a !== null && (wl(a, l, u), Ys(a, t, u)));
  }
  function Bs(l, t, a) {
    var u = nt();
    ue(l, t, a, u);
  }
  function ue(l, t, a, u) {
    var e = {
      lane: u,
      revertLane: 0,
      gesture: null,
      action: a,
      hasEagerState: !1,
      eagerState: null,
      next: null
    };
    if (vn(l)) qs(t, e);
    else {
      var n = l.alternate;
      if (l.lanes === 0 && (n === null || n.lanes === 0) && (n = t.lastRenderedReducer, n !== null))
        try {
          var c = t.lastRenderedState, f = n(c, a);
          if (e.hasEagerState = !0, e.eagerState = f, Pl(f, c))
            return we(l, t, e, 0), ol === null && Je(), !1;
        } catch {
        } finally {
        }
      if (a = Hc(l, t, e, u), a !== null)
        return wl(a, l, u), Ys(a, t, u), !0;
    }
    return !1;
  }
  function Sf(l, t, a, u) {
    if (u = {
      lane: 2,
      revertLane: Wf(),
      gesture: null,
      action: u,
      hasEagerState: !1,
      eagerState: null,
      next: null
    }, vn(l)) {
      if (t) throw Error(y(479));
    } else
      t = Hc(
        l,
        a,
        u,
        2
      ), t !== null && wl(t, l, 2);
  }
  function vn(l) {
    var t = l.alternate;
    return l === X || t !== null && t === X;
  }
  function qs(l, t) {
    vu = cn = !0;
    var a = l.pending;
    a === null ? t.next = t : (t.next = a.next, a.next = t), l.pending = t;
  }
  function Ys(l, t, a) {
    if ((a & 4194048) !== 0) {
      var u = t.lanes;
      u &= l.pendingLanes, a |= u, t.lanes = a, Qi(l, a);
    }
  }
  var ee = {
    readContext: Dl,
    use: sn,
    useCallback: ml,
    useContext: ml,
    useEffect: ml,
    useImperativeHandle: ml,
    useLayoutEffect: ml,
    useInsertionEffect: ml,
    useMemo: ml,
    useReducer: ml,
    useRef: ml,
    useState: ml,
    useDebugValue: ml,
    useDeferredValue: ml,
    useTransition: ml,
    useSyncExternalStore: ml,
    useId: ml,
    useHostTransitionStatus: ml,
    useFormState: ml,
    useActionState: ml,
    useOptimistic: ml,
    useMemoCache: ml,
    useCacheRefresh: ml
  };
  ee.useEffectEvent = ml;
  var js = {
    readContext: Dl,
    use: sn,
    useCallback: function(l, t) {
      return ql().memoizedState = [
        l,
        t === void 0 ? null : t
      ], l;
    },
    useContext: Dl,
    useEffect: Ts,
    useImperativeHandle: function(l, t, a) {
      a = a != null ? a.concat([l]) : null, yn(
        4194308,
        4,
        _s.bind(null, t, l),
        a
      );
    },
    useLayoutEffect: function(l, t) {
      return yn(4194308, 4, l, t);
    },
    useInsertionEffect: function(l, t) {
      yn(4, 2, l, t);
    },
    useMemo: function(l, t) {
      var a = ql();
      t = t === void 0 ? null : t;
      var u = l();
      if (Ga) {
        $t(!0);
        try {
          l();
        } finally {
          $t(!1);
        }
      }
      return a.memoizedState = [u, t], u;
    },
    useReducer: function(l, t, a) {
      var u = ql();
      if (a !== void 0) {
        var e = a(t);
        if (Ga) {
          $t(!0);
          try {
            a(t);
          } finally {
            $t(!1);
          }
        }
      } else e = t;
      return u.memoizedState = u.baseState = e, l = {
        pending: null,
        lanes: 0,
        dispatch: null,
        lastRenderedReducer: l,
        lastRenderedState: e
      }, u.queue = l, l = l.dispatch = Um.bind(
        null,
        X,
        l
      ), [u.memoizedState, l];
    },
    useRef: function(l) {
      var t = ql();
      return l = { current: l }, t.memoizedState = l;
    },
    useState: function(l) {
      l = of(l);
      var t = l.queue, a = Bs.bind(null, X, t);
      return t.dispatch = a, [l.memoizedState, a];
    },
    useDebugValue: yf,
    useDeferredValue: function(l, t) {
      var a = ql();
      return mf(a, l, t);
    },
    useTransition: function() {
      var l = of(!1);
      return l = Us.bind(
        null,
        X,
        l.queue,
        !0,
        !1
      ), ql().memoizedState = l, [!1, l];
    },
    useSyncExternalStore: function(l, t, a) {
      var u = X, e = ql();
      if ($) {
        if (a === void 0)
          throw Error(y(407));
        a = a();
      } else {
        if (a = t(), ol === null)
          throw Error(y(349));
        (J & 127) !== 0 || es(u, t, a);
      }
      e.memoizedState = a;
      var n = { value: a, getSnapshot: t };
      return e.queue = n, Ts(cs.bind(null, u, n, l), [
        l
      ]), u.flags |= 2048, Su(
        9,
        { destroy: void 0 },
        ns.bind(
          null,
          u,
          n,
          a,
          t
        ),
        null
      ), a;
    },
    useId: function() {
      var l = ql(), t = ol.identifierPrefix;
      if ($) {
        var a = pt, u = _t;
        a = (u & ~(1 << 32 - Il(u) - 1)).toString(32) + a, t = "_" + t + "R_" + a, a = fn++, 0 < a && (t += "H" + a.toString(32)), t += "_";
      } else
        a = zm++, t = "_" + t + "r_" + a.toString(32) + "_";
      return l.memoizedState = t;
    },
    useHostTransitionStatus: hf,
    useFormState: hs,
    useActionState: hs,
    useOptimistic: function(l) {
      var t = ql();
      t.memoizedState = t.baseState = l;
      var a = {
        pending: null,
        lanes: 0,
        dispatch: null,
        lastRenderedReducer: null,
        lastRenderedState: null
      };
      return t.queue = a, t = Sf.bind(
        null,
        X,
        !0,
        a
      ), a.dispatch = t, [l, t];
    },
    useMemoCache: nf,
    useCacheRefresh: function() {
      return ql().memoizedState = Dm.bind(
        null,
        X
      );
    },
    useEffectEvent: function(l) {
      var t = ql(), a = { impl: l };
      return t.memoizedState = a, function() {
        if ((P & 2) !== 0)
          throw Error(y(440));
        return a.impl.apply(void 0, arguments);
      };
    }
  }, rf = {
    readContext: Dl,
    use: sn,
    useCallback: Os,
    useContext: Dl,
    useEffect: df,
    useImperativeHandle: ps,
    useInsertionEffect: zs,
    useLayoutEffect: As,
    useMemo: Ms,
    useReducer: dn,
    useRef: bs,
    useState: function() {
      return dn(Gt);
    },
    useDebugValue: yf,
    useDeferredValue: function(l, t) {
      var a = rl();
      return Ds(
        a,
        nl.memoizedState,
        l,
        t
      );
    },
    useTransition: function() {
      var l = dn(Gt)[0], t = rl().memoizedState;
      return [
        typeof l == "boolean" ? l : ae(l),
        t
      ];
    },
    useSyncExternalStore: us,
    useId: Hs,
    useHostTransitionStatus: hf,
    useFormState: Ss,
    useActionState: Ss,
    useOptimistic: function(l, t) {
      var a = rl();
      return os(a, nl, l, t);
    },
    useMemoCache: nf,
    useCacheRefresh: Cs
  };
  rf.useEffectEvent = Es;
  var Gs = {
    readContext: Dl,
    use: sn,
    useCallback: Os,
    useContext: Dl,
    useEffect: df,
    useImperativeHandle: ps,
    useInsertionEffect: zs,
    useLayoutEffect: As,
    useMemo: Ms,
    useReducer: ff,
    useRef: bs,
    useState: function() {
      return ff(Gt);
    },
    useDebugValue: yf,
    useDeferredValue: function(l, t) {
      var a = rl();
      return nl === null ? mf(a, l, t) : Ds(
        a,
        nl.memoizedState,
        l,
        t
      );
    },
    useTransition: function() {
      var l = ff(Gt)[0], t = rl().memoizedState;
      return [
        typeof l == "boolean" ? l : ae(l),
        t
      ];
    },
    useSyncExternalStore: us,
    useId: Hs,
    useHostTransitionStatus: hf,
    useFormState: gs,
    useActionState: gs,
    useOptimistic: function(l, t) {
      var a = rl();
      return nl !== null ? os(a, nl, l, t) : (a.baseState = l, [l, a.queue.dispatch]);
    },
    useMemoCache: nf,
    useCacheRefresh: Cs
  };
  Gs.useEffectEvent = Es;
  function gf(l, t, a, u) {
    t = l.memoizedState, a = a(u, t), a = a == null ? t : q({}, t, a), l.memoizedState = a, l.lanes === 0 && (l.updateQueue.baseState = a);
  }
  var bf = {
    enqueueSetState: function(l, t, a) {
      l = l._reactInternals;
      var u = nt(), e = ua(u);
      e.payload = t, a != null && (e.callback = a), t = ea(l, e, u), t !== null && (wl(t, l, u), Iu(t, l, u));
    },
    enqueueReplaceState: function(l, t, a) {
      l = l._reactInternals;
      var u = nt(), e = ua(u);
      e.tag = 1, e.payload = t, a != null && (e.callback = a), t = ea(l, e, u), t !== null && (wl(t, l, u), Iu(t, l, u));
    },
    enqueueForceUpdate: function(l, t) {
      l = l._reactInternals;
      var a = nt(), u = ua(a);
      u.tag = 2, t != null && (u.callback = t), t = ea(l, u, a), t !== null && (wl(t, l, a), Iu(t, l, a));
    }
  };
  function Xs(l, t, a, u, e, n, c) {
    return l = l.stateNode, typeof l.shouldComponentUpdate == "function" ? l.shouldComponentUpdate(u, n, c) : t.prototype && t.prototype.isPureReactComponent ? !Vu(a, u) || !Vu(e, n) : !0;
  }
  function Qs(l, t, a, u) {
    l = t.state, typeof t.componentWillReceiveProps == "function" && t.componentWillReceiveProps(a, u), typeof t.UNSAFE_componentWillReceiveProps == "function" && t.UNSAFE_componentWillReceiveProps(a, u), t.state !== l && bf.enqueueReplaceState(t, t.state, null);
  }
  function Xa(l, t) {
    var a = t;
    if ("ref" in t) {
      a = {};
      for (var u in t)
        u !== "ref" && (a[u] = t[u]);
    }
    if (l = l.defaultProps) {
      a === t && (a = q({}, a));
      for (var e in l)
        a[e] === void 0 && (a[e] = l[e]);
    }
    return a;
  }
  function Zs(l) {
    Ke(l);
  }
  function Ls(l) {
    console.error(l);
  }
  function xs(l) {
    Ke(l);
  }
  function hn(l, t) {
    try {
      var a = l.onUncaughtError;
      a(t.value, { componentStack: t.stack });
    } catch (u) {
      setTimeout(function() {
        throw u;
      });
    }
  }
  function Vs(l, t, a) {
    try {
      var u = l.onCaughtError;
      u(a.value, {
        componentStack: a.stack,
        errorBoundary: t.tag === 1 ? t.stateNode : null
      });
    } catch (e) {
      setTimeout(function() {
        throw e;
      });
    }
  }
  function Tf(l, t, a) {
    return a = ua(a), a.tag = 3, a.payload = { element: null }, a.callback = function() {
      hn(l, t);
    }, a;
  }
  function Ks(l) {
    return l = ua(l), l.tag = 3, l;
  }
  function Js(l, t, a, u) {
    var e = a.type.getDerivedStateFromError;
    if (typeof e == "function") {
      var n = u.value;
      l.payload = function() {
        return e(n);
      }, l.callback = function() {
        Vs(t, a, u);
      };
    }
    var c = a.stateNode;
    c !== null && typeof c.componentDidCatch == "function" && (l.callback = function() {
      Vs(t, a, u), typeof e != "function" && (sa === null ? sa = /* @__PURE__ */ new Set([this]) : sa.add(this));
      var f = u.stack;
      this.componentDidCatch(u.value, {
        componentStack: f !== null ? f : ""
      });
    });
  }
  function Nm(l, t, a, u, e) {
    if (a.flags |= 32768, u !== null && typeof u == "object" && typeof u.then == "function") {
      if (t = a.alternate, t !== null && iu(
        t,
        a,
        e,
        !0
      ), a = tt.current, a !== null) {
        switch (a.tag) {
          case 31:
          case 13:
            return vt === null ? Mn() : a.alternate === null && vl === 0 && (vl = 3), a.flags &= -257, a.flags |= 65536, a.lanes = e, u === tn ? a.flags |= 16384 : (t = a.updateQueue, t === null ? a.updateQueue = /* @__PURE__ */ new Set([u]) : t.add(u), Kf(l, u, e)), !1;
          case 22:
            return a.flags |= 65536, u === tn ? a.flags |= 16384 : (t = a.updateQueue, t === null ? (t = {
              transitions: null,
              markerInstances: null,
              retryQueue: /* @__PURE__ */ new Set([u])
            }, a.updateQueue = t) : (a = t.retryQueue, a === null ? t.retryQueue = /* @__PURE__ */ new Set([u]) : a.add(u)), Kf(l, u, e)), !1;
        }
        throw Error(y(435, a.tag));
      }
      return Kf(l, u, e), Mn(), !1;
    }
    if ($)
      return t = tt.current, t !== null ? ((t.flags & 65536) === 0 && (t.flags |= 256), t.flags |= 65536, t.lanes = e, u !== Gc && (l = Error(y(422), { cause: u }), wu(st(l, a)))) : (u !== Gc && (t = Error(y(423), {
        cause: u
      }), wu(
        st(t, a)
      )), l = l.current.alternate, l.flags |= 65536, e &= -e, l.lanes |= e, u = st(u, a), e = Tf(
        l.stateNode,
        u,
        e
      ), $c(l, e), vl !== 4 && (vl = 2)), !1;
    var n = Error(y(520), { cause: u });
    if (n = st(n, a), ye === null ? ye = [n] : ye.push(n), vl !== 4 && (vl = 2), t === null) return !0;
    u = st(u, a), a = t;
    do {
      switch (a.tag) {
        case 3:
          return a.flags |= 65536, l = e & -e, a.lanes |= l, l = Tf(a.stateNode, u, l), $c(a, l), !1;
        case 1:
          if (t = a.type, n = a.stateNode, (a.flags & 128) === 0 && (typeof t.getDerivedStateFromError == "function" || n !== null && typeof n.componentDidCatch == "function" && (sa === null || !sa.has(n))))
            return a.flags |= 65536, e &= -e, a.lanes |= e, e = Ks(e), Js(
              e,
              l,
              a,
              u
            ), $c(a, e), !1;
      }
      a = a.return;
    } while (a !== null);
    return !1;
  }
  var Ef = Error(y(461)), El = !1;
  function Ul(l, t, a, u) {
    t.child = l === null ? Fo(t, null, a, u) : ja(
      t,
      l.child,
      a,
      u
    );
  }
  function ws(l, t, a, u, e) {
    a = a.render;
    var n = t.ref;
    if ("ref" in u) {
      var c = {};
      for (var f in u)
        f !== "ref" && (c[f] = u[f]);
    } else c = u;
    return Ca(t), u = tf(
      l,
      t,
      a,
      c,
      n,
      e
    ), f = af(), l !== null && !El ? (uf(l, t, e), Xt(l, t, e)) : ($ && f && Yc(t), t.flags |= 1, Ul(l, t, u, e), t.child);
  }
  function Ws(l, t, a, u, e) {
    if (l === null) {
      var n = a.type;
      return typeof n == "function" && !Cc(n) && n.defaultProps === void 0 && a.compare === null ? (t.tag = 15, t.type = n, $s(
        l,
        t,
        n,
        u,
        e
      )) : (l = $e(
        a.type,
        null,
        u,
        t,
        t.mode,
        e
      ), l.ref = t.ref, l.return = t, t.child = l);
    }
    if (n = l.child, !Uf(l, e)) {
      var c = n.memoizedProps;
      if (a = a.compare, a = a !== null ? a : Vu, a(c, u) && l.ref === t.ref)
        return Xt(l, t, e);
    }
    return t.flags |= 1, l = Ct(n, u), l.ref = t.ref, l.return = t, t.child = l;
  }
  function $s(l, t, a, u, e) {
    if (l !== null) {
      var n = l.memoizedProps;
      if (Vu(n, u) && l.ref === t.ref)
        if (El = !1, t.pendingProps = u = n, Uf(l, e))
          (l.flags & 131072) !== 0 && (El = !0);
        else
          return t.lanes = l.lanes, Xt(l, t, e);
    }
    return zf(
      l,
      t,
      a,
      u,
      e
    );
  }
  function Fs(l, t, a, u) {
    var e = u.children, n = l !== null ? l.memoizedState : null;
    if (l === null && t.stateNode === null && (t.stateNode = {
      _visibility: 1,
      _pendingMarkers: null,
      _retryCache: null,
      _transitions: null
    }), u.mode === "hidden") {
      if ((t.flags & 128) !== 0) {
        if (n = n !== null ? n.baseLanes | a : a, l !== null) {
          for (u = t.child = l.child, e = 0; u !== null; )
            e = e | u.lanes | u.childLanes, u = u.sibling;
          u = e & ~n;
        } else u = 0, t.child = null;
        return ks(
          l,
          t,
          n,
          a,
          u
        );
      }
      if ((a & 536870912) !== 0)
        t.memoizedState = { baseLanes: 0, cachePool: null }, l !== null && Pe(
          t,
          n !== null ? n.cachePool : null
        ), n !== null ? Po(t, n) : kc(), ls(t);
      else
        return u = t.lanes = 536870912, ks(
          l,
          t,
          n !== null ? n.baseLanes | a : a,
          a,
          u
        );
    } else
      n !== null ? (Pe(t, n.cachePool), Po(t, n), ca(), t.memoizedState = null) : (l !== null && Pe(t, null), kc(), ca());
    return Ul(l, t, e, a), t.child;
  }
  function ne(l, t) {
    return l !== null && l.tag === 22 || t.stateNode !== null || (t.stateNode = {
      _visibility: 1,
      _pendingMarkers: null,
      _retryCache: null,
      _transitions: null
    }), t.sibling;
  }
  function ks(l, t, a, u, e) {
    var n = Kc();
    return n = n === null ? null : { parent: bl._currentValue, pool: n }, t.memoizedState = {
      baseLanes: a,
      cachePool: n
    }, l !== null && Pe(t, null), kc(), ls(t), l !== null && iu(l, t, u, !0), t.childLanes = e, null;
  }
  function Sn(l, t) {
    return t = gn(
      { mode: t.mode, children: t.children },
      l.mode
    ), t.ref = l.ref, l.child = t, t.return = l, t;
  }
  function Is(l, t, a) {
    return ja(t, l.child, null, a), l = Sn(t, t.pendingProps), l.flags |= 2, at(t), t.memoizedState = null, l;
  }
  function Rm(l, t, a) {
    var u = t.pendingProps, e = (t.flags & 128) !== 0;
    if (t.flags &= -129, l === null) {
      if ($) {
        if (u.mode === "hidden")
          return l = Sn(t, u), t.lanes = 536870912, ne(null, l);
        if (Pc(t), (l = sl) ? (l = s0(
          l,
          mt
        ), l = l !== null && l.data === "&" ? l : null, l !== null && (t.memoizedState = {
          dehydrated: l,
          treeContext: It !== null ? { id: _t, overflow: pt } : null,
          retryLane: 536870912,
          hydrationErrors: null
        }, a = qo(l), a.return = t, t.child = a, Ml = t, sl = null)) : l = null, l === null) throw la(t);
        return t.lanes = 536870912, null;
      }
      return Sn(t, u);
    }
    var n = l.memoizedState;
    if (n !== null) {
      var c = n.dehydrated;
      if (Pc(t), e)
        if (t.flags & 256)
          t.flags &= -257, t = Is(
            l,
            t,
            a
          );
        else if (t.memoizedState !== null)
          t.child = l.child, t.flags |= 128, t = null;
        else throw Error(y(558));
      else if (El || iu(l, t, a, !1), e = (a & l.childLanes) !== 0, El || e) {
        if (u = ol, u !== null && (c = Zi(u, a), c !== 0 && c !== n.retryLane))
          throw n.retryLane = c, Ua(l, c), wl(u, l, c), Ef;
        Mn(), t = Is(
          l,
          t,
          a
        );
      } else
        l = n.treeContext, sl = ht(c.nextSibling), Ml = t, $ = !0, Pt = null, mt = !1, l !== null && Go(t, l), t = Sn(t, u), t.flags |= 4096;
      return t;
    }
    return l = Ct(l.child, {
      mode: u.mode,
      children: u.children
    }), l.ref = t.ref, t.child = l, l.return = t, l;
  }
  function rn(l, t) {
    var a = t.ref;
    if (a === null)
      l !== null && l.ref !== null && (t.flags |= 4194816);
    else {
      if (typeof a != "function" && typeof a != "object")
        throw Error(y(284));
      (l === null || l.ref !== a) && (t.flags |= 4194816);
    }
  }
  function zf(l, t, a, u, e) {
    return Ca(t), a = tf(
      l,
      t,
      a,
      u,
      void 0,
      e
    ), u = af(), l !== null && !El ? (uf(l, t, e), Xt(l, t, e)) : ($ && u && Yc(t), t.flags |= 1, Ul(l, t, a, e), t.child);
  }
  function Ps(l, t, a, u, e, n) {
    return Ca(t), t.updateQueue = null, a = as(
      t,
      u,
      a,
      e
    ), ts(l), u = af(), l !== null && !El ? (uf(l, t, n), Xt(l, t, n)) : ($ && u && Yc(t), t.flags |= 1, Ul(l, t, a, n), t.child);
  }
  function ld(l, t, a, u, e) {
    if (Ca(t), t.stateNode === null) {
      var n = eu, c = a.contextType;
      typeof c == "object" && c !== null && (n = Dl(c)), n = new a(u, n), t.memoizedState = n.state !== null && n.state !== void 0 ? n.state : null, n.updater = bf, t.stateNode = n, n._reactInternals = t, n = t.stateNode, n.props = u, n.state = t.memoizedState, n.refs = {}, wc(t), c = a.contextType, n.context = typeof c == "object" && c !== null ? Dl(c) : eu, n.state = t.memoizedState, c = a.getDerivedStateFromProps, typeof c == "function" && (gf(
        t,
        a,
        c,
        u
      ), n.state = t.memoizedState), typeof a.getDerivedStateFromProps == "function" || typeof n.getSnapshotBeforeUpdate == "function" || typeof n.UNSAFE_componentWillMount != "function" && typeof n.componentWillMount != "function" || (c = n.state, typeof n.componentWillMount == "function" && n.componentWillMount(), typeof n.UNSAFE_componentWillMount == "function" && n.UNSAFE_componentWillMount(), c !== n.state && bf.enqueueReplaceState(n, n.state, null), le(t, u, n, e), Pu(), n.state = t.memoizedState), typeof n.componentDidMount == "function" && (t.flags |= 4194308), u = !0;
    } else if (l === null) {
      n = t.stateNode;
      var f = t.memoizedProps, i = Xa(a, f);
      n.props = i;
      var h = n.context, b = a.contextType;
      c = eu, typeof b == "object" && b !== null && (c = Dl(b));
      var A = a.getDerivedStateFromProps;
      b = typeof A == "function" || typeof n.getSnapshotBeforeUpdate == "function", f = t.pendingProps !== f, b || typeof n.UNSAFE_componentWillReceiveProps != "function" && typeof n.componentWillReceiveProps != "function" || (f || h !== c) && Qs(
        t,
        n,
        u,
        c
      ), aa = !1;
      var S = t.memoizedState;
      n.state = S, le(t, u, n, e), Pu(), h = t.memoizedState, f || S !== h || aa ? (typeof A == "function" && (gf(
        t,
        a,
        A,
        u
      ), h = t.memoizedState), (i = aa || Xs(
        t,
        a,
        i,
        u,
        S,
        h,
        c
      )) ? (b || typeof n.UNSAFE_componentWillMount != "function" && typeof n.componentWillMount != "function" || (typeof n.componentWillMount == "function" && n.componentWillMount(), typeof n.UNSAFE_componentWillMount == "function" && n.UNSAFE_componentWillMount()), typeof n.componentDidMount == "function" && (t.flags |= 4194308)) : (typeof n.componentDidMount == "function" && (t.flags |= 4194308), t.memoizedProps = u, t.memoizedState = h), n.props = u, n.state = h, n.context = c, u = i) : (typeof n.componentDidMount == "function" && (t.flags |= 4194308), u = !1);
    } else {
      n = t.stateNode, Wc(l, t), c = t.memoizedProps, b = Xa(a, c), n.props = b, A = t.pendingProps, S = n.context, h = a.contextType, i = eu, typeof h == "object" && h !== null && (i = Dl(h)), f = a.getDerivedStateFromProps, (h = typeof f == "function" || typeof n.getSnapshotBeforeUpdate == "function") || typeof n.UNSAFE_componentWillReceiveProps != "function" && typeof n.componentWillReceiveProps != "function" || (c !== A || S !== i) && Qs(
        t,
        n,
        u,
        i
      ), aa = !1, S = t.memoizedState, n.state = S, le(t, u, n, e), Pu();
      var r = t.memoizedState;
      c !== A || S !== r || aa || l !== null && l.dependencies !== null && ke(l.dependencies) ? (typeof f == "function" && (gf(
        t,
        a,
        f,
        u
      ), r = t.memoizedState), (b = aa || Xs(
        t,
        a,
        b,
        u,
        S,
        r,
        i
      ) || l !== null && l.dependencies !== null && ke(l.dependencies)) ? (h || typeof n.UNSAFE_componentWillUpdate != "function" && typeof n.componentWillUpdate != "function" || (typeof n.componentWillUpdate == "function" && n.componentWillUpdate(u, r, i), typeof n.UNSAFE_componentWillUpdate == "function" && n.UNSAFE_componentWillUpdate(
        u,
        r,
        i
      )), typeof n.componentDidUpdate == "function" && (t.flags |= 4), typeof n.getSnapshotBeforeUpdate == "function" && (t.flags |= 1024)) : (typeof n.componentDidUpdate != "function" || c === l.memoizedProps && S === l.memoizedState || (t.flags |= 4), typeof n.getSnapshotBeforeUpdate != "function" || c === l.memoizedProps && S === l.memoizedState || (t.flags |= 1024), t.memoizedProps = u, t.memoizedState = r), n.props = u, n.state = r, n.context = i, u = b) : (typeof n.componentDidUpdate != "function" || c === l.memoizedProps && S === l.memoizedState || (t.flags |= 4), typeof n.getSnapshotBeforeUpdate != "function" || c === l.memoizedProps && S === l.memoizedState || (t.flags |= 1024), u = !1);
    }
    return n = u, rn(l, t), u = (t.flags & 128) !== 0, n || u ? (n = t.stateNode, a = u && typeof a.getDerivedStateFromError != "function" ? null : n.render(), t.flags |= 1, l !== null && u ? (t.child = ja(
      t,
      l.child,
      null,
      e
    ), t.child = ja(
      t,
      null,
      a,
      e
    )) : Ul(l, t, a, e), t.memoizedState = n.state, l = t.child) : l = Xt(
      l,
      t,
      e
    ), l;
  }
  function td(l, t, a, u) {
    return Ra(), t.flags |= 256, Ul(l, t, a, u), t.child;
  }
  var Af = {
    dehydrated: null,
    treeContext: null,
    retryLane: 0,
    hydrationErrors: null
  };
  function _f(l) {
    return { baseLanes: l, cachePool: Vo() };
  }
  function pf(l, t, a) {
    return l = l !== null ? l.childLanes & ~a : 0, t && (l |= et), l;
  }
  function ad(l, t, a) {
    var u = t.pendingProps, e = !1, n = (t.flags & 128) !== 0, c;
    if ((c = n) || (c = l !== null && l.memoizedState === null ? !1 : (Sl.current & 2) !== 0), c && (e = !0, t.flags &= -129), c = (t.flags & 32) !== 0, t.flags &= -33, l === null) {
      if ($) {
        if (e ? na(t) : ca(), (l = sl) ? (l = s0(
          l,
          mt
        ), l = l !== null && l.data !== "&" ? l : null, l !== null && (t.memoizedState = {
          dehydrated: l,
          treeContext: It !== null ? { id: _t, overflow: pt } : null,
          retryLane: 536870912,
          hydrationErrors: null
        }, a = qo(l), a.return = t, t.child = a, Ml = t, sl = null)) : l = null, l === null) throw la(t);
        return fi(l) ? t.lanes = 32 : t.lanes = 536870912, null;
      }
      var f = u.children;
      return u = u.fallback, e ? (ca(), e = t.mode, f = gn(
        { mode: "hidden", children: f },
        e
      ), u = Na(
        u,
        e,
        a,
        null
      ), f.return = t, u.return = t, f.sibling = u, t.child = f, u = t.child, u.memoizedState = _f(a), u.childLanes = pf(
        l,
        c,
        a
      ), t.memoizedState = Af, ne(null, u)) : (na(t), Of(t, f));
    }
    var i = l.memoizedState;
    if (i !== null && (f = i.dehydrated, f !== null)) {
      if (n)
        t.flags & 256 ? (na(t), t.flags &= -257, t = Mf(
          l,
          t,
          a
        )) : t.memoizedState !== null ? (ca(), t.child = l.child, t.flags |= 128, t = null) : (ca(), f = u.fallback, e = t.mode, u = gn(
          { mode: "visible", children: u.children },
          e
        ), f = Na(
          f,
          e,
          a,
          null
        ), f.flags |= 2, u.return = t, f.return = t, u.sibling = f, t.child = u, ja(
          t,
          l.child,
          null,
          a
        ), u = t.child, u.memoizedState = _f(a), u.childLanes = pf(
          l,
          c,
          a
        ), t.memoizedState = Af, t = ne(null, u));
      else if (na(t), fi(f)) {
        if (c = f.nextSibling && f.nextSibling.dataset, c) var h = c.dgst;
        c = h, u = Error(y(419)), u.stack = "", u.digest = c, wu({ value: u, source: null, stack: null }), t = Mf(
          l,
          t,
          a
        );
      } else if (El || iu(l, t, a, !1), c = (a & l.childLanes) !== 0, El || c) {
        if (c = ol, c !== null && (u = Zi(c, a), u !== 0 && u !== i.retryLane))
          throw i.retryLane = u, Ua(l, u), wl(c, l, u), Ef;
        ci(f) || Mn(), t = Mf(
          l,
          t,
          a
        );
      } else
        ci(f) ? (t.flags |= 192, t.child = l.child, t = null) : (l = i.treeContext, sl = ht(
          f.nextSibling
        ), Ml = t, $ = !0, Pt = null, mt = !1, l !== null && Go(t, l), t = Of(
          t,
          u.children
        ), t.flags |= 4096);
      return t;
    }
    return e ? (ca(), f = u.fallback, e = t.mode, i = l.child, h = i.sibling, u = Ct(i, {
      mode: "hidden",
      children: u.children
    }), u.subtreeFlags = i.subtreeFlags & 65011712, h !== null ? f = Ct(
      h,
      f
    ) : (f = Na(
      f,
      e,
      a,
      null
    ), f.flags |= 2), f.return = t, u.return = t, u.sibling = f, t.child = u, ne(null, u), u = t.child, f = l.child.memoizedState, f === null ? f = _f(a) : (e = f.cachePool, e !== null ? (i = bl._currentValue, e = e.parent !== i ? { parent: i, pool: i } : e) : e = Vo(), f = {
      baseLanes: f.baseLanes | a,
      cachePool: e
    }), u.memoizedState = f, u.childLanes = pf(
      l,
      c,
      a
    ), t.memoizedState = Af, ne(l.child, u)) : (na(t), a = l.child, l = a.sibling, a = Ct(a, {
      mode: "visible",
      children: u.children
    }), a.return = t, a.sibling = null, l !== null && (c = t.deletions, c === null ? (t.deletions = [l], t.flags |= 16) : c.push(l)), t.child = a, t.memoizedState = null, a);
  }
  function Of(l, t) {
    return t = gn(
      { mode: "visible", children: t },
      l.mode
    ), t.return = l, l.child = t;
  }
  function gn(l, t) {
    return l = lt(22, l, null, t), l.lanes = 0, l;
  }
  function Mf(l, t, a) {
    return ja(t, l.child, null, a), l = Of(
      t,
      t.pendingProps.children
    ), l.flags |= 2, t.memoizedState = null, l;
  }
  function ud(l, t, a) {
    l.lanes |= t;
    var u = l.alternate;
    u !== null && (u.lanes |= t), Zc(l.return, t, a);
  }
  function Df(l, t, a, u, e, n) {
    var c = l.memoizedState;
    c === null ? l.memoizedState = {
      isBackwards: t,
      rendering: null,
      renderingStartTime: 0,
      last: u,
      tail: a,
      tailMode: e,
      treeForkCount: n
    } : (c.isBackwards = t, c.rendering = null, c.renderingStartTime = 0, c.last = u, c.tail = a, c.tailMode = e, c.treeForkCount = n);
  }
  function ed(l, t, a) {
    var u = t.pendingProps, e = u.revealOrder, n = u.tail;
    u = u.children;
    var c = Sl.current, f = (c & 2) !== 0;
    if (f ? (c = c & 1 | 2, t.flags |= 128) : c &= 1, M(Sl, c), Ul(l, t, u, a), u = $ ? Ju : 0, !f && l !== null && (l.flags & 128) !== 0)
      l: for (l = t.child; l !== null; ) {
        if (l.tag === 13)
          l.memoizedState !== null && ud(l, a, t);
        else if (l.tag === 19)
          ud(l, a, t);
        else if (l.child !== null) {
          l.child.return = l, l = l.child;
          continue;
        }
        if (l === t) break l;
        for (; l.sibling === null; ) {
          if (l.return === null || l.return === t)
            break l;
          l = l.return;
        }
        l.sibling.return = l.return, l = l.sibling;
      }
    switch (e) {
      case "forwards":
        for (a = t.child, e = null; a !== null; )
          l = a.alternate, l !== null && nn(l) === null && (e = a), a = a.sibling;
        a = e, a === null ? (e = t.child, t.child = null) : (e = a.sibling, a.sibling = null), Df(
          t,
          !1,
          e,
          a,
          n,
          u
        );
        break;
      case "backwards":
      case "unstable_legacy-backwards":
        for (a = null, e = t.child, t.child = null; e !== null; ) {
          if (l = e.alternate, l !== null && nn(l) === null) {
            t.child = e;
            break;
          }
          l = e.sibling, e.sibling = a, a = e, e = l;
        }
        Df(
          t,
          !0,
          a,
          null,
          n,
          u
        );
        break;
      case "together":
        Df(
          t,
          !1,
          null,
          null,
          void 0,
          u
        );
        break;
      default:
        t.memoizedState = null;
    }
    return t.child;
  }
  function Xt(l, t, a) {
    if (l !== null && (t.dependencies = l.dependencies), oa |= t.lanes, (a & t.childLanes) === 0)
      if (l !== null) {
        if (iu(
          l,
          t,
          a,
          !1
        ), (a & t.childLanes) === 0)
          return null;
      } else return null;
    if (l !== null && t.child !== l.child)
      throw Error(y(153));
    if (t.child !== null) {
      for (l = t.child, a = Ct(l, l.pendingProps), t.child = a, a.return = t; l.sibling !== null; )
        l = l.sibling, a = a.sibling = Ct(l, l.pendingProps), a.return = t;
      a.sibling = null;
    }
    return t.child;
  }
  function Uf(l, t) {
    return (l.lanes & t) !== 0 ? !0 : (l = l.dependencies, !!(l !== null && ke(l)));
  }
  function Hm(l, t, a) {
    switch (t.tag) {
      case 3:
        Bl(t, t.stateNode.containerInfo), ta(t, bl, l.memoizedState.cache), Ra();
        break;
      case 27:
      case 5:
        Ru(t);
        break;
      case 4:
        Bl(t, t.stateNode.containerInfo);
        break;
      case 10:
        ta(
          t,
          t.type,
          t.memoizedProps.value
        );
        break;
      case 31:
        if (t.memoizedState !== null)
          return t.flags |= 128, Pc(t), null;
        break;
      case 13:
        var u = t.memoizedState;
        if (u !== null)
          return u.dehydrated !== null ? (na(t), t.flags |= 128, null) : (a & t.child.childLanes) !== 0 ? ad(l, t, a) : (na(t), l = Xt(
            l,
            t,
            a
          ), l !== null ? l.sibling : null);
        na(t);
        break;
      case 19:
        var e = (l.flags & 128) !== 0;
        if (u = (a & t.childLanes) !== 0, u || (iu(
          l,
          t,
          a,
          !1
        ), u = (a & t.childLanes) !== 0), e) {
          if (u)
            return ed(
              l,
              t,
              a
            );
          t.flags |= 128;
        }
        if (e = t.memoizedState, e !== null && (e.rendering = null, e.tail = null, e.lastEffect = null), M(Sl, Sl.current), u) break;
        return null;
      case 22:
        return t.lanes = 0, Fs(
          l,
          t,
          a,
          t.pendingProps
        );
      case 24:
        ta(t, bl, l.memoizedState.cache);
    }
    return Xt(l, t, a);
  }
  function nd(l, t, a) {
    if (l !== null)
      if (l.memoizedProps !== t.pendingProps)
        El = !0;
      else {
        if (!Uf(l, a) && (t.flags & 128) === 0)
          return El = !1, Hm(
            l,
            t,
            a
          );
        El = (l.flags & 131072) !== 0;
      }
    else
      El = !1, $ && (t.flags & 1048576) !== 0 && jo(t, Ju, t.index);
    switch (t.lanes = 0, t.tag) {
      case 16:
        l: {
          var u = t.pendingProps;
          if (l = qa(t.elementType), t.type = l, typeof l == "function")
            Cc(l) ? (u = Xa(l, u), t.tag = 1, t = ld(
              null,
              t,
              l,
              u,
              a
            )) : (t.tag = 0, t = zf(
              null,
              t,
              l,
              u,
              a
            ));
          else {
            if (l != null) {
              var e = l.$$typeof;
              if (e === ct) {
                t.tag = 11, t = ws(
                  null,
                  t,
                  l,
                  u,
                  a
                );
                break l;
              } else if (e === W) {
                t.tag = 14, t = Ws(
                  null,
                  t,
                  l,
                  u,
                  a
                );
                break l;
              }
            }
            throw t = Ut(l) || l, Error(y(306, t, ""));
          }
        }
        return t;
      case 0:
        return zf(
          l,
          t,
          t.type,
          t.pendingProps,
          a
        );
      case 1:
        return u = t.type, e = Xa(
          u,
          t.pendingProps
        ), ld(
          l,
          t,
          u,
          e,
          a
        );
      case 3:
        l: {
          if (Bl(
            t,
            t.stateNode.containerInfo
          ), l === null) throw Error(y(387));
          u = t.pendingProps;
          var n = t.memoizedState;
          e = n.element, Wc(l, t), le(t, u, null, a);
          var c = t.memoizedState;
          if (u = c.cache, ta(t, bl, u), u !== n.cache && Lc(
            t,
            [bl],
            a,
            !0
          ), Pu(), u = c.element, n.isDehydrated)
            if (n = {
              element: u,
              isDehydrated: !1,
              cache: c.cache
            }, t.updateQueue.baseState = n, t.memoizedState = n, t.flags & 256) {
              t = td(
                l,
                t,
                u,
                a
              );
              break l;
            } else if (u !== e) {
              e = st(
                Error(y(424)),
                t
              ), wu(e), t = td(
                l,
                t,
                u,
                a
              );
              break l;
            } else {
              switch (l = t.stateNode.containerInfo, l.nodeType) {
                case 9:
                  l = l.body;
                  break;
                default:
                  l = l.nodeName === "HTML" ? l.ownerDocument.body : l;
              }
              for (sl = ht(l.firstChild), Ml = t, $ = !0, Pt = null, mt = !0, a = Fo(
                t,
                null,
                u,
                a
              ), t.child = a; a; )
                a.flags = a.flags & -3 | 4096, a = a.sibling;
            }
          else {
            if (Ra(), u === e) {
              t = Xt(
                l,
                t,
                a
              );
              break l;
            }
            Ul(l, t, u, a);
          }
          t = t.child;
        }
        return t;
      case 26:
        return rn(l, t), l === null ? (a = S0(
          t.type,
          null,
          t.pendingProps,
          null
        )) ? t.memoizedState = a : $ || (a = t.type, l = t.pendingProps, u = Bn(
          L.current
        ).createElement(a), u[Ol] = t, u[Zl] = l, Nl(u, a, l), _l(u), t.stateNode = u) : t.memoizedState = S0(
          t.type,
          l.memoizedProps,
          t.pendingProps,
          l.memoizedState
        ), null;
      case 27:
        return Ru(t), l === null && $ && (u = t.stateNode = m0(
          t.type,
          t.pendingProps,
          L.current
        ), Ml = t, mt = !0, e = sl, va(t.type) ? (ii = e, sl = ht(u.firstChild)) : sl = e), Ul(
          l,
          t,
          t.pendingProps.children,
          a
        ), rn(l, t), l === null && (t.flags |= 4194304), t.child;
      case 5:
        return l === null && $ && ((e = u = sl) && (u = iv(
          u,
          t.type,
          t.pendingProps,
          mt
        ), u !== null ? (t.stateNode = u, Ml = t, sl = ht(u.firstChild), mt = !1, e = !0) : e = !1), e || la(t)), Ru(t), e = t.type, n = t.pendingProps, c = l !== null ? l.memoizedProps : null, u = n.children, ui(e, n) ? u = null : c !== null && ui(e, c) && (t.flags |= 32), t.memoizedState !== null && (e = tf(
          l,
          t,
          Am,
          null,
          null,
          a
        ), Te._currentValue = e), rn(l, t), Ul(l, t, u, a), t.child;
      case 6:
        return l === null && $ && ((l = a = sl) && (a = ov(
          a,
          t.pendingProps,
          mt
        ), a !== null ? (t.stateNode = a, Ml = t, sl = null, l = !0) : l = !1), l || la(t)), null;
      case 13:
        return ad(l, t, a);
      case 4:
        return Bl(
          t,
          t.stateNode.containerInfo
        ), u = t.pendingProps, l === null ? t.child = ja(
          t,
          null,
          u,
          a
        ) : Ul(l, t, u, a), t.child;
      case 11:
        return ws(
          l,
          t,
          t.type,
          t.pendingProps,
          a
        );
      case 7:
        return Ul(
          l,
          t,
          t.pendingProps,
          a
        ), t.child;
      case 8:
        return Ul(
          l,
          t,
          t.pendingProps.children,
          a
        ), t.child;
      case 12:
        return Ul(
          l,
          t,
          t.pendingProps.children,
          a
        ), t.child;
      case 10:
        return u = t.pendingProps, ta(t, t.type, u.value), Ul(l, t, u.children, a), t.child;
      case 9:
        return e = t.type._context, u = t.pendingProps.children, Ca(t), e = Dl(e), u = u(e), t.flags |= 1, Ul(l, t, u, a), t.child;
      case 14:
        return Ws(
          l,
          t,
          t.type,
          t.pendingProps,
          a
        );
      case 15:
        return $s(
          l,
          t,
          t.type,
          t.pendingProps,
          a
        );
      case 19:
        return ed(l, t, a);
      case 31:
        return Rm(l, t, a);
      case 22:
        return Fs(
          l,
          t,
          a,
          t.pendingProps
        );
      case 24:
        return Ca(t), u = Dl(bl), l === null ? (e = Kc(), e === null && (e = ol, n = xc(), e.pooledCache = n, n.refCount++, n !== null && (e.pooledCacheLanes |= a), e = n), t.memoizedState = { parent: u, cache: e }, wc(t), ta(t, bl, e)) : ((l.lanes & a) !== 0 && (Wc(l, t), le(t, null, null, a), Pu()), e = l.memoizedState, n = t.memoizedState, e.parent !== u ? (e = { parent: u, cache: u }, t.memoizedState = e, t.lanes === 0 && (t.memoizedState = t.updateQueue.baseState = e), ta(t, bl, u)) : (u = n.cache, ta(t, bl, u), u !== e.cache && Lc(
          t,
          [bl],
          a,
          !0
        ))), Ul(
          l,
          t,
          t.pendingProps.children,
          a
        ), t.child;
      case 29:
        throw t.pendingProps;
    }
    throw Error(y(156, t.tag));
  }
  function Qt(l) {
    l.flags |= 4;
  }
  function Nf(l, t, a, u, e) {
    if ((t = (l.mode & 32) !== 0) && (t = !1), t) {
      if (l.flags |= 16777216, (e & 335544128) === e)
        if (l.stateNode.complete) l.flags |= 8192;
        else if (Hd()) l.flags |= 8192;
        else
          throw Ya = tn, Jc;
    } else l.flags &= -16777217;
  }
  function cd(l, t) {
    if (t.type !== "stylesheet" || (t.state.loading & 4) !== 0)
      l.flags &= -16777217;
    else if (l.flags |= 16777216, !E0(t))
      if (Hd()) l.flags |= 8192;
      else
        throw Ya = tn, Jc;
  }
  function bn(l, t) {
    t !== null && (l.flags |= 4), l.flags & 16384 && (t = l.tag !== 22 ? Gi() : 536870912, l.lanes |= t, Tu |= t);
  }
  function ce(l, t) {
    if (!$)
      switch (l.tailMode) {
        case "hidden":
          t = l.tail;
          for (var a = null; t !== null; )
            t.alternate !== null && (a = t), t = t.sibling;
          a === null ? l.tail = null : a.sibling = null;
          break;
        case "collapsed":
          a = l.tail;
          for (var u = null; a !== null; )
            a.alternate !== null && (u = a), a = a.sibling;
          u === null ? t || l.tail === null ? l.tail = null : l.tail.sibling = null : u.sibling = null;
      }
  }
  function dl(l) {
    var t = l.alternate !== null && l.alternate.child === l.child, a = 0, u = 0;
    if (t)
      for (var e = l.child; e !== null; )
        a |= e.lanes | e.childLanes, u |= e.subtreeFlags & 65011712, u |= e.flags & 65011712, e.return = l, e = e.sibling;
    else
      for (e = l.child; e !== null; )
        a |= e.lanes | e.childLanes, u |= e.subtreeFlags, u |= e.flags, e.return = l, e = e.sibling;
    return l.subtreeFlags |= u, l.childLanes = a, t;
  }
  function Cm(l, t, a) {
    var u = t.pendingProps;
    switch (jc(t), t.tag) {
      case 16:
      case 15:
      case 0:
      case 11:
      case 7:
      case 8:
      case 12:
      case 9:
      case 14:
        return dl(t), null;
      case 1:
        return dl(t), null;
      case 3:
        return a = t.stateNode, u = null, l !== null && (u = l.memoizedState.cache), t.memoizedState.cache !== u && (t.flags |= 2048), Yt(bl), hl(), a.pendingContext && (a.context = a.pendingContext, a.pendingContext = null), (l === null || l.child === null) && (fu(t) ? Qt(t) : l === null || l.memoizedState.isDehydrated && (t.flags & 256) === 0 || (t.flags |= 1024, Xc())), dl(t), null;
      case 26:
        var e = t.type, n = t.memoizedState;
        return l === null ? (Qt(t), n !== null ? (dl(t), cd(t, n)) : (dl(t), Nf(
          t,
          e,
          null,
          u,
          a
        ))) : n ? n !== l.memoizedState ? (Qt(t), dl(t), cd(t, n)) : (dl(t), t.flags &= -16777217) : (l = l.memoizedProps, l !== u && Qt(t), dl(t), Nf(
          t,
          e,
          l,
          u,
          a
        )), null;
      case 27:
        if (Ue(t), a = L.current, e = t.type, l !== null && t.stateNode != null)
          l.memoizedProps !== u && Qt(t);
        else {
          if (!u) {
            if (t.stateNode === null)
              throw Error(y(166));
            return dl(t), null;
          }
          l = N.current, fu(t) ? Xo(t) : (l = m0(e, u, a), t.stateNode = l, Qt(t));
        }
        return dl(t), null;
      case 5:
        if (Ue(t), e = t.type, l !== null && t.stateNode != null)
          l.memoizedProps !== u && Qt(t);
        else {
          if (!u) {
            if (t.stateNode === null)
              throw Error(y(166));
            return dl(t), null;
          }
          if (n = N.current, fu(t))
            Xo(t);
          else {
            var c = Bn(
              L.current
            );
            switch (n) {
              case 1:
                n = c.createElementNS(
                  "http://www.w3.org/2000/svg",
                  e
                );
                break;
              case 2:
                n = c.createElementNS(
                  "http://www.w3.org/1998/Math/MathML",
                  e
                );
                break;
              default:
                switch (e) {
                  case "svg":
                    n = c.createElementNS(
                      "http://www.w3.org/2000/svg",
                      e
                    );
                    break;
                  case "math":
                    n = c.createElementNS(
                      "http://www.w3.org/1998/Math/MathML",
                      e
                    );
                    break;
                  case "script":
                    n = c.createElement("div"), n.innerHTML = "<script><\/script>", n = n.removeChild(
                      n.firstChild
                    );
                    break;
                  case "select":
                    n = typeof u.is == "string" ? c.createElement("select", {
                      is: u.is
                    }) : c.createElement("select"), u.multiple ? n.multiple = !0 : u.size && (n.size = u.size);
                    break;
                  default:
                    n = typeof u.is == "string" ? c.createElement(e, { is: u.is }) : c.createElement(e);
                }
            }
            n[Ol] = t, n[Zl] = u;
            l: for (c = t.child; c !== null; ) {
              if (c.tag === 5 || c.tag === 6)
                n.appendChild(c.stateNode);
              else if (c.tag !== 4 && c.tag !== 27 && c.child !== null) {
                c.child.return = c, c = c.child;
                continue;
              }
              if (c === t) break l;
              for (; c.sibling === null; ) {
                if (c.return === null || c.return === t)
                  break l;
                c = c.return;
              }
              c.sibling.return = c.return, c = c.sibling;
            }
            t.stateNode = n;
            l: switch (Nl(n, e, u), e) {
              case "button":
              case "input":
              case "select":
              case "textarea":
                u = !!u.autoFocus;
                break l;
              case "img":
                u = !0;
                break l;
              default:
                u = !1;
            }
            u && Qt(t);
          }
        }
        return dl(t), Nf(
          t,
          t.type,
          l === null ? null : l.memoizedProps,
          t.pendingProps,
          a
        ), null;
      case 6:
        if (l && t.stateNode != null)
          l.memoizedProps !== u && Qt(t);
        else {
          if (typeof u != "string" && t.stateNode === null)
            throw Error(y(166));
          if (l = L.current, fu(t)) {
            if (l = t.stateNode, a = t.memoizedProps, u = null, e = Ml, e !== null)
              switch (e.tag) {
                case 27:
                case 5:
                  u = e.memoizedProps;
              }
            l[Ol] = t, l = !!(l.nodeValue === a || u !== null && u.suppressHydrationWarning === !0 || a0(l.nodeValue, a)), l || la(t, !0);
          } else
            l = Bn(l).createTextNode(
              u
            ), l[Ol] = t, t.stateNode = l;
        }
        return dl(t), null;
      case 31:
        if (a = t.memoizedState, l === null || l.memoizedState !== null) {
          if (u = fu(t), a !== null) {
            if (l === null) {
              if (!u) throw Error(y(318));
              if (l = t.memoizedState, l = l !== null ? l.dehydrated : null, !l) throw Error(y(557));
              l[Ol] = t;
            } else
              Ra(), (t.flags & 128) === 0 && (t.memoizedState = null), t.flags |= 4;
            dl(t), l = !1;
          } else
            a = Xc(), l !== null && l.memoizedState !== null && (l.memoizedState.hydrationErrors = a), l = !0;
          if (!l)
            return t.flags & 256 ? (at(t), t) : (at(t), null);
          if ((t.flags & 128) !== 0)
            throw Error(y(558));
        }
        return dl(t), null;
      case 13:
        if (u = t.memoizedState, l === null || l.memoizedState !== null && l.memoizedState.dehydrated !== null) {
          if (e = fu(t), u !== null && u.dehydrated !== null) {
            if (l === null) {
              if (!e) throw Error(y(318));
              if (e = t.memoizedState, e = e !== null ? e.dehydrated : null, !e) throw Error(y(317));
              e[Ol] = t;
            } else
              Ra(), (t.flags & 128) === 0 && (t.memoizedState = null), t.flags |= 4;
            dl(t), e = !1;
          } else
            e = Xc(), l !== null && l.memoizedState !== null && (l.memoizedState.hydrationErrors = e), e = !0;
          if (!e)
            return t.flags & 256 ? (at(t), t) : (at(t), null);
        }
        return at(t), (t.flags & 128) !== 0 ? (t.lanes = a, t) : (a = u !== null, l = l !== null && l.memoizedState !== null, a && (u = t.child, e = null, u.alternate !== null && u.alternate.memoizedState !== null && u.alternate.memoizedState.cachePool !== null && (e = u.alternate.memoizedState.cachePool.pool), n = null, u.memoizedState !== null && u.memoizedState.cachePool !== null && (n = u.memoizedState.cachePool.pool), n !== e && (u.flags |= 2048)), a !== l && a && (t.child.flags |= 8192), bn(t, t.updateQueue), dl(t), null);
      case 4:
        return hl(), l === null && If(t.stateNode.containerInfo), dl(t), null;
      case 10:
        return Yt(t.type), dl(t), null;
      case 19:
        if (_(Sl), u = t.memoizedState, u === null) return dl(t), null;
        if (e = (t.flags & 128) !== 0, n = u.rendering, n === null)
          if (e) ce(u, !1);
          else {
            if (vl !== 0 || l !== null && (l.flags & 128) !== 0)
              for (l = t.child; l !== null; ) {
                if (n = nn(l), n !== null) {
                  for (t.flags |= 128, ce(u, !1), l = n.updateQueue, t.updateQueue = l, bn(t, l), t.subtreeFlags = 0, l = a, a = t.child; a !== null; )
                    Bo(a, l), a = a.sibling;
                  return M(
                    Sl,
                    Sl.current & 1 | 2
                  ), $ && Bt(t, u.treeForkCount), t.child;
                }
                l = l.sibling;
              }
            u.tail !== null && Fl() > _n && (t.flags |= 128, e = !0, ce(u, !1), t.lanes = 4194304);
          }
        else {
          if (!e)
            if (l = nn(n), l !== null) {
              if (t.flags |= 128, e = !0, l = l.updateQueue, t.updateQueue = l, bn(t, l), ce(u, !0), u.tail === null && u.tailMode === "hidden" && !n.alternate && !$)
                return dl(t), null;
            } else
              2 * Fl() - u.renderingStartTime > _n && a !== 536870912 && (t.flags |= 128, e = !0, ce(u, !1), t.lanes = 4194304);
          u.isBackwards ? (n.sibling = t.child, t.child = n) : (l = u.last, l !== null ? l.sibling = n : t.child = n, u.last = n);
        }
        return u.tail !== null ? (l = u.tail, u.rendering = l, u.tail = l.sibling, u.renderingStartTime = Fl(), l.sibling = null, a = Sl.current, M(
          Sl,
          e ? a & 1 | 2 : a & 1
        ), $ && Bt(t, u.treeForkCount), l) : (dl(t), null);
      case 22:
      case 23:
        return at(t), Ic(), u = t.memoizedState !== null, l !== null ? l.memoizedState !== null !== u && (t.flags |= 8192) : u && (t.flags |= 8192), u ? (a & 536870912) !== 0 && (t.flags & 128) === 0 && (dl(t), t.subtreeFlags & 6 && (t.flags |= 8192)) : dl(t), a = t.updateQueue, a !== null && bn(t, a.retryQueue), a = null, l !== null && l.memoizedState !== null && l.memoizedState.cachePool !== null && (a = l.memoizedState.cachePool.pool), u = null, t.memoizedState !== null && t.memoizedState.cachePool !== null && (u = t.memoizedState.cachePool.pool), u !== a && (t.flags |= 2048), l !== null && _(Ba), null;
      case 24:
        return a = null, l !== null && (a = l.memoizedState.cache), t.memoizedState.cache !== a && (t.flags |= 2048), Yt(bl), dl(t), null;
      case 25:
        return null;
      case 30:
        return null;
    }
    throw Error(y(156, t.tag));
  }
  function Bm(l, t) {
    switch (jc(t), t.tag) {
      case 1:
        return l = t.flags, l & 65536 ? (t.flags = l & -65537 | 128, t) : null;
      case 3:
        return Yt(bl), hl(), l = t.flags, (l & 65536) !== 0 && (l & 128) === 0 ? (t.flags = l & -65537 | 128, t) : null;
      case 26:
      case 27:
      case 5:
        return Ue(t), null;
      case 31:
        if (t.memoizedState !== null) {
          if (at(t), t.alternate === null)
            throw Error(y(340));
          Ra();
        }
        return l = t.flags, l & 65536 ? (t.flags = l & -65537 | 128, t) : null;
      case 13:
        if (at(t), l = t.memoizedState, l !== null && l.dehydrated !== null) {
          if (t.alternate === null)
            throw Error(y(340));
          Ra();
        }
        return l = t.flags, l & 65536 ? (t.flags = l & -65537 | 128, t) : null;
      case 19:
        return _(Sl), null;
      case 4:
        return hl(), null;
      case 10:
        return Yt(t.type), null;
      case 22:
      case 23:
        return at(t), Ic(), l !== null && _(Ba), l = t.flags, l & 65536 ? (t.flags = l & -65537 | 128, t) : null;
      case 24:
        return Yt(bl), null;
      case 25:
        return null;
      default:
        return null;
    }
  }
  function fd(l, t) {
    switch (jc(t), t.tag) {
      case 3:
        Yt(bl), hl();
        break;
      case 26:
      case 27:
      case 5:
        Ue(t);
        break;
      case 4:
        hl();
        break;
      case 31:
        t.memoizedState !== null && at(t);
        break;
      case 13:
        at(t);
        break;
      case 19:
        _(Sl);
        break;
      case 10:
        Yt(t.type);
        break;
      case 22:
      case 23:
        at(t), Ic(), l !== null && _(Ba);
        break;
      case 24:
        Yt(bl);
    }
  }
  function fe(l, t) {
    try {
      var a = t.updateQueue, u = a !== null ? a.lastEffect : null;
      if (u !== null) {
        var e = u.next;
        a = e;
        do {
          if ((a.tag & l) === l) {
            u = void 0;
            var n = a.create, c = a.inst;
            u = n(), c.destroy = u;
          }
          a = a.next;
        } while (a !== e);
      }
    } catch (f) {
      ul(t, t.return, f);
    }
  }
  function fa(l, t, a) {
    try {
      var u = t.updateQueue, e = u !== null ? u.lastEffect : null;
      if (e !== null) {
        var n = e.next;
        u = n;
        do {
          if ((u.tag & l) === l) {
            var c = u.inst, f = c.destroy;
            if (f !== void 0) {
              c.destroy = void 0, e = t;
              var i = a, h = f;
              try {
                h();
              } catch (b) {
                ul(
                  e,
                  i,
                  b
                );
              }
            }
          }
          u = u.next;
        } while (u !== n);
      }
    } catch (b) {
      ul(t, t.return, b);
    }
  }
  function id(l) {
    var t = l.updateQueue;
    if (t !== null) {
      var a = l.stateNode;
      try {
        Io(t, a);
      } catch (u) {
        ul(l, l.return, u);
      }
    }
  }
  function od(l, t, a) {
    a.props = Xa(
      l.type,
      l.memoizedProps
    ), a.state = l.memoizedState;
    try {
      a.componentWillUnmount();
    } catch (u) {
      ul(l, t, u);
    }
  }
  function ie(l, t) {
    try {
      var a = l.ref;
      if (a !== null) {
        switch (l.tag) {
          case 26:
          case 27:
          case 5:
            var u = l.stateNode;
            break;
          case 30:
            u = l.stateNode;
            break;
          default:
            u = l.stateNode;
        }
        typeof a == "function" ? l.refCleanup = a(u) : a.current = u;
      }
    } catch (e) {
      ul(l, t, e);
    }
  }
  function Ot(l, t) {
    var a = l.ref, u = l.refCleanup;
    if (a !== null)
      if (typeof u == "function")
        try {
          u();
        } catch (e) {
          ul(l, t, e);
        } finally {
          l.refCleanup = null, l = l.alternate, l != null && (l.refCleanup = null);
        }
      else if (typeof a == "function")
        try {
          a(null);
        } catch (e) {
          ul(l, t, e);
        }
      else a.current = null;
  }
  function sd(l) {
    var t = l.type, a = l.memoizedProps, u = l.stateNode;
    try {
      l: switch (t) {
        case "button":
        case "input":
        case "select":
        case "textarea":
          a.autoFocus && u.focus();
          break l;
        case "img":
          a.src ? u.src = a.src : a.srcSet && (u.srcset = a.srcSet);
      }
    } catch (e) {
      ul(l, l.return, e);
    }
  }
  function Rf(l, t, a) {
    try {
      var u = l.stateNode;
      av(u, l.type, a, t), u[Zl] = t;
    } catch (e) {
      ul(l, l.return, e);
    }
  }
  function dd(l) {
    return l.tag === 5 || l.tag === 3 || l.tag === 26 || l.tag === 27 && va(l.type) || l.tag === 4;
  }
  function Hf(l) {
    l: for (; ; ) {
      for (; l.sibling === null; ) {
        if (l.return === null || dd(l.return)) return null;
        l = l.return;
      }
      for (l.sibling.return = l.return, l = l.sibling; l.tag !== 5 && l.tag !== 6 && l.tag !== 18; ) {
        if (l.tag === 27 && va(l.type) || l.flags & 2 || l.child === null || l.tag === 4) continue l;
        l.child.return = l, l = l.child;
      }
      if (!(l.flags & 2)) return l.stateNode;
    }
  }
  function Cf(l, t, a) {
    var u = l.tag;
    if (u === 5 || u === 6)
      l = l.stateNode, t ? (a.nodeType === 9 ? a.body : a.nodeName === "HTML" ? a.ownerDocument.body : a).insertBefore(l, t) : (t = a.nodeType === 9 ? a.body : a.nodeName === "HTML" ? a.ownerDocument.body : a, t.appendChild(l), a = a._reactRootContainer, a != null || t.onclick !== null || (t.onclick = Rt));
    else if (u !== 4 && (u === 27 && va(l.type) && (a = l.stateNode, t = null), l = l.child, l !== null))
      for (Cf(l, t, a), l = l.sibling; l !== null; )
        Cf(l, t, a), l = l.sibling;
  }
  function Tn(l, t, a) {
    var u = l.tag;
    if (u === 5 || u === 6)
      l = l.stateNode, t ? a.insertBefore(l, t) : a.appendChild(l);
    else if (u !== 4 && (u === 27 && va(l.type) && (a = l.stateNode), l = l.child, l !== null))
      for (Tn(l, t, a), l = l.sibling; l !== null; )
        Tn(l, t, a), l = l.sibling;
  }
  function yd(l) {
    var t = l.stateNode, a = l.memoizedProps;
    try {
      for (var u = l.type, e = t.attributes; e.length; )
        t.removeAttributeNode(e[0]);
      Nl(t, u, a), t[Ol] = l, t[Zl] = a;
    } catch (n) {
      ul(l, l.return, n);
    }
  }
  var Zt = !1, zl = !1, Bf = !1, md = typeof WeakSet == "function" ? WeakSet : Set, pl = null;
  function qm(l, t) {
    if (l = l.containerInfo, ti = Zn, l = po(l), Oc(l)) {
      if ("selectionStart" in l)
        var a = {
          start: l.selectionStart,
          end: l.selectionEnd
        };
      else
        l: {
          a = (a = l.ownerDocument) && a.defaultView || window;
          var u = a.getSelection && a.getSelection();
          if (u && u.rangeCount !== 0) {
            a = u.anchorNode;
            var e = u.anchorOffset, n = u.focusNode;
            u = u.focusOffset;
            try {
              a.nodeType, n.nodeType;
            } catch {
              a = null;
              break l;
            }
            var c = 0, f = -1, i = -1, h = 0, b = 0, A = l, S = null;
            t: for (; ; ) {
              for (var r; A !== a || e !== 0 && A.nodeType !== 3 || (f = c + e), A !== n || u !== 0 && A.nodeType !== 3 || (i = c + u), A.nodeType === 3 && (c += A.nodeValue.length), (r = A.firstChild) !== null; )
                S = A, A = r;
              for (; ; ) {
                if (A === l) break t;
                if (S === a && ++h === e && (f = c), S === n && ++b === u && (i = c), (r = A.nextSibling) !== null) break;
                A = S, S = A.parentNode;
              }
              A = r;
            }
            a = f === -1 || i === -1 ? null : { start: f, end: i };
          } else a = null;
        }
      a = a || { start: 0, end: 0 };
    } else a = null;
    for (ai = { focusedElem: l, selectionRange: a }, Zn = !1, pl = t; pl !== null; )
      if (t = pl, l = t.child, (t.subtreeFlags & 1028) !== 0 && l !== null)
        l.return = t, pl = l;
      else
        for (; pl !== null; ) {
          switch (t = pl, n = t.alternate, l = t.flags, t.tag) {
            case 0:
              if ((l & 4) !== 0 && (l = t.updateQueue, l = l !== null ? l.events : null, l !== null))
                for (a = 0; a < l.length; a++)
                  e = l[a], e.ref.impl = e.nextImpl;
              break;
            case 11:
            case 15:
              break;
            case 1:
              if ((l & 1024) !== 0 && n !== null) {
                l = void 0, a = t, e = n.memoizedProps, n = n.memoizedState, u = a.stateNode;
                try {
                  var U = Xa(
                    a.type,
                    e
                  );
                  l = u.getSnapshotBeforeUpdate(
                    U,
                    n
                  ), u.__reactInternalSnapshotBeforeUpdate = l;
                } catch (B) {
                  ul(
                    a,
                    a.return,
                    B
                  );
                }
              }
              break;
            case 3:
              if ((l & 1024) !== 0) {
                if (l = t.stateNode.containerInfo, a = l.nodeType, a === 9)
                  ni(l);
                else if (a === 1)
                  switch (l.nodeName) {
                    case "HEAD":
                    case "HTML":
                    case "BODY":
                      ni(l);
                      break;
                    default:
                      l.textContent = "";
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
              if ((l & 1024) !== 0) throw Error(y(163));
          }
          if (l = t.sibling, l !== null) {
            l.return = t.return, pl = l;
            break;
          }
          pl = t.return;
        }
  }
  function vd(l, t, a) {
    var u = a.flags;
    switch (a.tag) {
      case 0:
      case 11:
      case 15:
        xt(l, a), u & 4 && fe(5, a);
        break;
      case 1:
        if (xt(l, a), u & 4)
          if (l = a.stateNode, t === null)
            try {
              l.componentDidMount();
            } catch (c) {
              ul(a, a.return, c);
            }
          else {
            var e = Xa(
              a.type,
              t.memoizedProps
            );
            t = t.memoizedState;
            try {
              l.componentDidUpdate(
                e,
                t,
                l.__reactInternalSnapshotBeforeUpdate
              );
            } catch (c) {
              ul(
                a,
                a.return,
                c
              );
            }
          }
        u & 64 && id(a), u & 512 && ie(a, a.return);
        break;
      case 3:
        if (xt(l, a), u & 64 && (l = a.updateQueue, l !== null)) {
          if (t = null, a.child !== null)
            switch (a.child.tag) {
              case 27:
              case 5:
                t = a.child.stateNode;
                break;
              case 1:
                t = a.child.stateNode;
            }
          try {
            Io(l, t);
          } catch (c) {
            ul(a, a.return, c);
          }
        }
        break;
      case 27:
        t === null && u & 4 && yd(a);
      case 26:
      case 5:
        xt(l, a), t === null && u & 4 && sd(a), u & 512 && ie(a, a.return);
        break;
      case 12:
        xt(l, a);
        break;
      case 31:
        xt(l, a), u & 4 && rd(l, a);
        break;
      case 13:
        xt(l, a), u & 4 && gd(l, a), u & 64 && (l = a.memoizedState, l !== null && (l = l.dehydrated, l !== null && (a = Vm.bind(
          null,
          a
        ), sv(l, a))));
        break;
      case 22:
        if (u = a.memoizedState !== null || Zt, !u) {
          t = t !== null && t.memoizedState !== null || zl, e = Zt;
          var n = zl;
          Zt = u, (zl = t) && !n ? Vt(
            l,
            a,
            (a.subtreeFlags & 8772) !== 0
          ) : xt(l, a), Zt = e, zl = n;
        }
        break;
      case 30:
        break;
      default:
        xt(l, a);
    }
  }
  function hd(l) {
    var t = l.alternate;
    t !== null && (l.alternate = null, hd(t)), l.child = null, l.deletions = null, l.sibling = null, l.tag === 5 && (t = l.stateNode, t !== null && oc(t)), l.stateNode = null, l.return = null, l.dependencies = null, l.memoizedProps = null, l.memoizedState = null, l.pendingProps = null, l.stateNode = null, l.updateQueue = null;
  }
  var yl = null, xl = !1;
  function Lt(l, t, a) {
    for (a = a.child; a !== null; )
      Sd(l, t, a), a = a.sibling;
  }
  function Sd(l, t, a) {
    if (kl && typeof kl.onCommitFiberUnmount == "function")
      try {
        kl.onCommitFiberUnmount(Hu, a);
      } catch {
      }
    switch (a.tag) {
      case 26:
        zl || Ot(a, t), Lt(
          l,
          t,
          a
        ), a.memoizedState ? a.memoizedState.count-- : a.stateNode && (a = a.stateNode, a.parentNode.removeChild(a));
        break;
      case 27:
        zl || Ot(a, t);
        var u = yl, e = xl;
        va(a.type) && (yl = a.stateNode, xl = !1), Lt(
          l,
          t,
          a
        ), re(a.stateNode), yl = u, xl = e;
        break;
      case 5:
        zl || Ot(a, t);
      case 6:
        if (u = yl, e = xl, yl = null, Lt(
          l,
          t,
          a
        ), yl = u, xl = e, yl !== null)
          if (xl)
            try {
              (yl.nodeType === 9 ? yl.body : yl.nodeName === "HTML" ? yl.ownerDocument.body : yl).removeChild(a.stateNode);
            } catch (n) {
              ul(
                a,
                t,
                n
              );
            }
          else
            try {
              yl.removeChild(a.stateNode);
            } catch (n) {
              ul(
                a,
                t,
                n
              );
            }
        break;
      case 18:
        yl !== null && (xl ? (l = yl, i0(
          l.nodeType === 9 ? l.body : l.nodeName === "HTML" ? l.ownerDocument.body : l,
          a.stateNode
        ), Du(l)) : i0(yl, a.stateNode));
        break;
      case 4:
        u = yl, e = xl, yl = a.stateNode.containerInfo, xl = !0, Lt(
          l,
          t,
          a
        ), yl = u, xl = e;
        break;
      case 0:
      case 11:
      case 14:
      case 15:
        fa(2, a, t), zl || fa(4, a, t), Lt(
          l,
          t,
          a
        );
        break;
      case 1:
        zl || (Ot(a, t), u = a.stateNode, typeof u.componentWillUnmount == "function" && od(
          a,
          t,
          u
        )), Lt(
          l,
          t,
          a
        );
        break;
      case 21:
        Lt(
          l,
          t,
          a
        );
        break;
      case 22:
        zl = (u = zl) || a.memoizedState !== null, Lt(
          l,
          t,
          a
        ), zl = u;
        break;
      default:
        Lt(
          l,
          t,
          a
        );
    }
  }
  function rd(l, t) {
    if (t.memoizedState === null && (l = t.alternate, l !== null && (l = l.memoizedState, l !== null))) {
      l = l.dehydrated;
      try {
        Du(l);
      } catch (a) {
        ul(t, t.return, a);
      }
    }
  }
  function gd(l, t) {
    if (t.memoizedState === null && (l = t.alternate, l !== null && (l = l.memoizedState, l !== null && (l = l.dehydrated, l !== null))))
      try {
        Du(l);
      } catch (a) {
        ul(t, t.return, a);
      }
  }
  function Ym(l) {
    switch (l.tag) {
      case 31:
      case 13:
      case 19:
        var t = l.stateNode;
        return t === null && (t = l.stateNode = new md()), t;
      case 22:
        return l = l.stateNode, t = l._retryCache, t === null && (t = l._retryCache = new md()), t;
      default:
        throw Error(y(435, l.tag));
    }
  }
  function En(l, t) {
    var a = Ym(l);
    t.forEach(function(u) {
      if (!a.has(u)) {
        a.add(u);
        var e = Km.bind(null, l, u);
        u.then(e, e);
      }
    });
  }
  function Vl(l, t) {
    var a = t.deletions;
    if (a !== null)
      for (var u = 0; u < a.length; u++) {
        var e = a[u], n = l, c = t, f = c;
        l: for (; f !== null; ) {
          switch (f.tag) {
            case 27:
              if (va(f.type)) {
                yl = f.stateNode, xl = !1;
                break l;
              }
              break;
            case 5:
              yl = f.stateNode, xl = !1;
              break l;
            case 3:
            case 4:
              yl = f.stateNode.containerInfo, xl = !0;
              break l;
          }
          f = f.return;
        }
        if (yl === null) throw Error(y(160));
        Sd(n, c, e), yl = null, xl = !1, n = e.alternate, n !== null && (n.return = null), e.return = null;
      }
    if (t.subtreeFlags & 13886)
      for (t = t.child; t !== null; )
        bd(t, l), t = t.sibling;
  }
  var bt = null;
  function bd(l, t) {
    var a = l.alternate, u = l.flags;
    switch (l.tag) {
      case 0:
      case 11:
      case 14:
      case 15:
        Vl(t, l), Kl(l), u & 4 && (fa(3, l, l.return), fe(3, l), fa(5, l, l.return));
        break;
      case 1:
        Vl(t, l), Kl(l), u & 512 && (zl || a === null || Ot(a, a.return)), u & 64 && Zt && (l = l.updateQueue, l !== null && (u = l.callbacks, u !== null && (a = l.shared.hiddenCallbacks, l.shared.hiddenCallbacks = a === null ? u : a.concat(u))));
        break;
      case 26:
        var e = bt;
        if (Vl(t, l), Kl(l), u & 512 && (zl || a === null || Ot(a, a.return)), u & 4) {
          var n = a !== null ? a.memoizedState : null;
          if (u = l.memoizedState, a === null)
            if (u === null)
              if (l.stateNode === null) {
                l: {
                  u = l.type, a = l.memoizedProps, e = e.ownerDocument || e;
                  t: switch (u) {
                    case "title":
                      n = e.getElementsByTagName("title")[0], (!n || n[qu] || n[Ol] || n.namespaceURI === "http://www.w3.org/2000/svg" || n.hasAttribute("itemprop")) && (n = e.createElement(u), e.head.insertBefore(
                        n,
                        e.querySelector("head > title")
                      )), Nl(n, u, a), n[Ol] = l, _l(n), u = n;
                      break l;
                    case "link":
                      var c = b0(
                        "link",
                        "href",
                        e
                      ).get(u + (a.href || ""));
                      if (c) {
                        for (var f = 0; f < c.length; f++)
                          if (n = c[f], n.getAttribute("href") === (a.href == null || a.href === "" ? null : a.href) && n.getAttribute("rel") === (a.rel == null ? null : a.rel) && n.getAttribute("title") === (a.title == null ? null : a.title) && n.getAttribute("crossorigin") === (a.crossOrigin == null ? null : a.crossOrigin)) {
                            c.splice(f, 1);
                            break t;
                          }
                      }
                      n = e.createElement(u), Nl(n, u, a), e.head.appendChild(n);
                      break;
                    case "meta":
                      if (c = b0(
                        "meta",
                        "content",
                        e
                      ).get(u + (a.content || ""))) {
                        for (f = 0; f < c.length; f++)
                          if (n = c[f], n.getAttribute("content") === (a.content == null ? null : "" + a.content) && n.getAttribute("name") === (a.name == null ? null : a.name) && n.getAttribute("property") === (a.property == null ? null : a.property) && n.getAttribute("http-equiv") === (a.httpEquiv == null ? null : a.httpEquiv) && n.getAttribute("charset") === (a.charSet == null ? null : a.charSet)) {
                            c.splice(f, 1);
                            break t;
                          }
                      }
                      n = e.createElement(u), Nl(n, u, a), e.head.appendChild(n);
                      break;
                    default:
                      throw Error(y(468, u));
                  }
                  n[Ol] = l, _l(n), u = n;
                }
                l.stateNode = u;
              } else
                T0(
                  e,
                  l.type,
                  l.stateNode
                );
            else
              l.stateNode = g0(
                e,
                u,
                l.memoizedProps
              );
          else
            n !== u ? (n === null ? a.stateNode !== null && (a = a.stateNode, a.parentNode.removeChild(a)) : n.count--, u === null ? T0(
              e,
              l.type,
              l.stateNode
            ) : g0(
              e,
              u,
              l.memoizedProps
            )) : u === null && l.stateNode !== null && Rf(
              l,
              l.memoizedProps,
              a.memoizedProps
            );
        }
        break;
      case 27:
        Vl(t, l), Kl(l), u & 512 && (zl || a === null || Ot(a, a.return)), a !== null && u & 4 && Rf(
          l,
          l.memoizedProps,
          a.memoizedProps
        );
        break;
      case 5:
        if (Vl(t, l), Kl(l), u & 512 && (zl || a === null || Ot(a, a.return)), l.flags & 32) {
          e = l.stateNode;
          try {
            ka(e, "");
          } catch (U) {
            ul(l, l.return, U);
          }
        }
        u & 4 && l.stateNode != null && (e = l.memoizedProps, Rf(
          l,
          e,
          a !== null ? a.memoizedProps : e
        )), u & 1024 && (Bf = !0);
        break;
      case 6:
        if (Vl(t, l), Kl(l), u & 4) {
          if (l.stateNode === null)
            throw Error(y(162));
          u = l.memoizedProps, a = l.stateNode;
          try {
            a.nodeValue = u;
          } catch (U) {
            ul(l, l.return, U);
          }
        }
        break;
      case 3:
        if (jn = null, e = bt, bt = qn(t.containerInfo), Vl(t, l), bt = e, Kl(l), u & 4 && a !== null && a.memoizedState.isDehydrated)
          try {
            Du(t.containerInfo);
          } catch (U) {
            ul(l, l.return, U);
          }
        Bf && (Bf = !1, Td(l));
        break;
      case 4:
        u = bt, bt = qn(
          l.stateNode.containerInfo
        ), Vl(t, l), Kl(l), bt = u;
        break;
      case 12:
        Vl(t, l), Kl(l);
        break;
      case 31:
        Vl(t, l), Kl(l), u & 4 && (u = l.updateQueue, u !== null && (l.updateQueue = null, En(l, u)));
        break;
      case 13:
        Vl(t, l), Kl(l), l.child.flags & 8192 && l.memoizedState !== null != (a !== null && a.memoizedState !== null) && (An = Fl()), u & 4 && (u = l.updateQueue, u !== null && (l.updateQueue = null, En(l, u)));
        break;
      case 22:
        e = l.memoizedState !== null;
        var i = a !== null && a.memoizedState !== null, h = Zt, b = zl;
        if (Zt = h || e, zl = b || i, Vl(t, l), zl = b, Zt = h, Kl(l), u & 8192)
          l: for (t = l.stateNode, t._visibility = e ? t._visibility & -2 : t._visibility | 1, e && (a === null || i || Zt || zl || Qa(l)), a = null, t = l; ; ) {
            if (t.tag === 5 || t.tag === 26) {
              if (a === null) {
                i = a = t;
                try {
                  if (n = i.stateNode, e)
                    c = n.style, typeof c.setProperty == "function" ? c.setProperty("display", "none", "important") : c.display = "none";
                  else {
                    f = i.stateNode;
                    var A = i.memoizedProps.style, S = A != null && A.hasOwnProperty("display") ? A.display : null;
                    f.style.display = S == null || typeof S == "boolean" ? "" : ("" + S).trim();
                  }
                } catch (U) {
                  ul(i, i.return, U);
                }
              }
            } else if (t.tag === 6) {
              if (a === null) {
                i = t;
                try {
                  i.stateNode.nodeValue = e ? "" : i.memoizedProps;
                } catch (U) {
                  ul(i, i.return, U);
                }
              }
            } else if (t.tag === 18) {
              if (a === null) {
                i = t;
                try {
                  var r = i.stateNode;
                  e ? o0(r, !0) : o0(i.stateNode, !1);
                } catch (U) {
                  ul(i, i.return, U);
                }
              }
            } else if ((t.tag !== 22 && t.tag !== 23 || t.memoizedState === null || t === l) && t.child !== null) {
              t.child.return = t, t = t.child;
              continue;
            }
            if (t === l) break l;
            for (; t.sibling === null; ) {
              if (t.return === null || t.return === l) break l;
              a === t && (a = null), t = t.return;
            }
            a === t && (a = null), t.sibling.return = t.return, t = t.sibling;
          }
        u & 4 && (u = l.updateQueue, u !== null && (a = u.retryQueue, a !== null && (u.retryQueue = null, En(l, a))));
        break;
      case 19:
        Vl(t, l), Kl(l), u & 4 && (u = l.updateQueue, u !== null && (l.updateQueue = null, En(l, u)));
        break;
      case 30:
        break;
      case 21:
        break;
      default:
        Vl(t, l), Kl(l);
    }
  }
  function Kl(l) {
    var t = l.flags;
    if (t & 2) {
      try {
        for (var a, u = l.return; u !== null; ) {
          if (dd(u)) {
            a = u;
            break;
          }
          u = u.return;
        }
        if (a == null) throw Error(y(160));
        switch (a.tag) {
          case 27:
            var e = a.stateNode, n = Hf(l);
            Tn(l, n, e);
            break;
          case 5:
            var c = a.stateNode;
            a.flags & 32 && (ka(c, ""), a.flags &= -33);
            var f = Hf(l);
            Tn(l, f, c);
            break;
          case 3:
          case 4:
            var i = a.stateNode.containerInfo, h = Hf(l);
            Cf(
              l,
              h,
              i
            );
            break;
          default:
            throw Error(y(161));
        }
      } catch (b) {
        ul(l, l.return, b);
      }
      l.flags &= -3;
    }
    t & 4096 && (l.flags &= -4097);
  }
  function Td(l) {
    if (l.subtreeFlags & 1024)
      for (l = l.child; l !== null; ) {
        var t = l;
        Td(t), t.tag === 5 && t.flags & 1024 && t.stateNode.reset(), l = l.sibling;
      }
  }
  function xt(l, t) {
    if (t.subtreeFlags & 8772)
      for (t = t.child; t !== null; )
        vd(l, t.alternate, t), t = t.sibling;
  }
  function Qa(l) {
    for (l = l.child; l !== null; ) {
      var t = l;
      switch (t.tag) {
        case 0:
        case 11:
        case 14:
        case 15:
          fa(4, t, t.return), Qa(t);
          break;
        case 1:
          Ot(t, t.return);
          var a = t.stateNode;
          typeof a.componentWillUnmount == "function" && od(
            t,
            t.return,
            a
          ), Qa(t);
          break;
        case 27:
          re(t.stateNode);
        case 26:
        case 5:
          Ot(t, t.return), Qa(t);
          break;
        case 22:
          t.memoizedState === null && Qa(t);
          break;
        case 30:
          Qa(t);
          break;
        default:
          Qa(t);
      }
      l = l.sibling;
    }
  }
  function Vt(l, t, a) {
    for (a = a && (t.subtreeFlags & 8772) !== 0, t = t.child; t !== null; ) {
      var u = t.alternate, e = l, n = t, c = n.flags;
      switch (n.tag) {
        case 0:
        case 11:
        case 15:
          Vt(
            e,
            n,
            a
          ), fe(4, n);
          break;
        case 1:
          if (Vt(
            e,
            n,
            a
          ), u = n, e = u.stateNode, typeof e.componentDidMount == "function")
            try {
              e.componentDidMount();
            } catch (h) {
              ul(u, u.return, h);
            }
          if (u = n, e = u.updateQueue, e !== null) {
            var f = u.stateNode;
            try {
              var i = e.shared.hiddenCallbacks;
              if (i !== null)
                for (e.shared.hiddenCallbacks = null, e = 0; e < i.length; e++)
                  ko(i[e], f);
            } catch (h) {
              ul(u, u.return, h);
            }
          }
          a && c & 64 && id(n), ie(n, n.return);
          break;
        case 27:
          yd(n);
        case 26:
        case 5:
          Vt(
            e,
            n,
            a
          ), a && u === null && c & 4 && sd(n), ie(n, n.return);
          break;
        case 12:
          Vt(
            e,
            n,
            a
          );
          break;
        case 31:
          Vt(
            e,
            n,
            a
          ), a && c & 4 && rd(e, n);
          break;
        case 13:
          Vt(
            e,
            n,
            a
          ), a && c & 4 && gd(e, n);
          break;
        case 22:
          n.memoizedState === null && Vt(
            e,
            n,
            a
          ), ie(n, n.return);
          break;
        case 30:
          break;
        default:
          Vt(
            e,
            n,
            a
          );
      }
      t = t.sibling;
    }
  }
  function qf(l, t) {
    var a = null;
    l !== null && l.memoizedState !== null && l.memoizedState.cachePool !== null && (a = l.memoizedState.cachePool.pool), l = null, t.memoizedState !== null && t.memoizedState.cachePool !== null && (l = t.memoizedState.cachePool.pool), l !== a && (l != null && l.refCount++, a != null && Wu(a));
  }
  function Yf(l, t) {
    l = null, t.alternate !== null && (l = t.alternate.memoizedState.cache), t = t.memoizedState.cache, t !== l && (t.refCount++, l != null && Wu(l));
  }
  function Tt(l, t, a, u) {
    if (t.subtreeFlags & 10256)
      for (t = t.child; t !== null; )
        Ed(
          l,
          t,
          a,
          u
        ), t = t.sibling;
  }
  function Ed(l, t, a, u) {
    var e = t.flags;
    switch (t.tag) {
      case 0:
      case 11:
      case 15:
        Tt(
          l,
          t,
          a,
          u
        ), e & 2048 && fe(9, t);
        break;
      case 1:
        Tt(
          l,
          t,
          a,
          u
        );
        break;
      case 3:
        Tt(
          l,
          t,
          a,
          u
        ), e & 2048 && (l = null, t.alternate !== null && (l = t.alternate.memoizedState.cache), t = t.memoizedState.cache, t !== l && (t.refCount++, l != null && Wu(l)));
        break;
      case 12:
        if (e & 2048) {
          Tt(
            l,
            t,
            a,
            u
          ), l = t.stateNode;
          try {
            var n = t.memoizedProps, c = n.id, f = n.onPostCommit;
            typeof f == "function" && f(
              c,
              t.alternate === null ? "mount" : "update",
              l.passiveEffectDuration,
              -0
            );
          } catch (i) {
            ul(t, t.return, i);
          }
        } else
          Tt(
            l,
            t,
            a,
            u
          );
        break;
      case 31:
        Tt(
          l,
          t,
          a,
          u
        );
        break;
      case 13:
        Tt(
          l,
          t,
          a,
          u
        );
        break;
      case 23:
        break;
      case 22:
        n = t.stateNode, c = t.alternate, t.memoizedState !== null ? n._visibility & 2 ? Tt(
          l,
          t,
          a,
          u
        ) : oe(l, t) : n._visibility & 2 ? Tt(
          l,
          t,
          a,
          u
        ) : (n._visibility |= 2, ru(
          l,
          t,
          a,
          u,
          (t.subtreeFlags & 10256) !== 0 || !1
        )), e & 2048 && qf(c, t);
        break;
      case 24:
        Tt(
          l,
          t,
          a,
          u
        ), e & 2048 && Yf(t.alternate, t);
        break;
      default:
        Tt(
          l,
          t,
          a,
          u
        );
    }
  }
  function ru(l, t, a, u, e) {
    for (e = e && ((t.subtreeFlags & 10256) !== 0 || !1), t = t.child; t !== null; ) {
      var n = l, c = t, f = a, i = u, h = c.flags;
      switch (c.tag) {
        case 0:
        case 11:
        case 15:
          ru(
            n,
            c,
            f,
            i,
            e
          ), fe(8, c);
          break;
        case 23:
          break;
        case 22:
          var b = c.stateNode;
          c.memoizedState !== null ? b._visibility & 2 ? ru(
            n,
            c,
            f,
            i,
            e
          ) : oe(
            n,
            c
          ) : (b._visibility |= 2, ru(
            n,
            c,
            f,
            i,
            e
          )), e && h & 2048 && qf(
            c.alternate,
            c
          );
          break;
        case 24:
          ru(
            n,
            c,
            f,
            i,
            e
          ), e && h & 2048 && Yf(c.alternate, c);
          break;
        default:
          ru(
            n,
            c,
            f,
            i,
            e
          );
      }
      t = t.sibling;
    }
  }
  function oe(l, t) {
    if (t.subtreeFlags & 10256)
      for (t = t.child; t !== null; ) {
        var a = l, u = t, e = u.flags;
        switch (u.tag) {
          case 22:
            oe(a, u), e & 2048 && qf(
              u.alternate,
              u
            );
            break;
          case 24:
            oe(a, u), e & 2048 && Yf(u.alternate, u);
            break;
          default:
            oe(a, u);
        }
        t = t.sibling;
      }
  }
  var se = 8192;
  function gu(l, t, a) {
    if (l.subtreeFlags & se)
      for (l = l.child; l !== null; )
        zd(
          l,
          t,
          a
        ), l = l.sibling;
  }
  function zd(l, t, a) {
    switch (l.tag) {
      case 26:
        gu(
          l,
          t,
          a
        ), l.flags & se && l.memoizedState !== null && zv(
          a,
          bt,
          l.memoizedState,
          l.memoizedProps
        );
        break;
      case 5:
        gu(
          l,
          t,
          a
        );
        break;
      case 3:
      case 4:
        var u = bt;
        bt = qn(l.stateNode.containerInfo), gu(
          l,
          t,
          a
        ), bt = u;
        break;
      case 22:
        l.memoizedState === null && (u = l.alternate, u !== null && u.memoizedState !== null ? (u = se, se = 16777216, gu(
          l,
          t,
          a
        ), se = u) : gu(
          l,
          t,
          a
        ));
        break;
      default:
        gu(
          l,
          t,
          a
        );
    }
  }
  function Ad(l) {
    var t = l.alternate;
    if (t !== null && (l = t.child, l !== null)) {
      t.child = null;
      do
        t = l.sibling, l.sibling = null, l = t;
      while (l !== null);
    }
  }
  function de(l) {
    var t = l.deletions;
    if ((l.flags & 16) !== 0) {
      if (t !== null)
        for (var a = 0; a < t.length; a++) {
          var u = t[a];
          pl = u, pd(
            u,
            l
          );
        }
      Ad(l);
    }
    if (l.subtreeFlags & 10256)
      for (l = l.child; l !== null; )
        _d(l), l = l.sibling;
  }
  function _d(l) {
    switch (l.tag) {
      case 0:
      case 11:
      case 15:
        de(l), l.flags & 2048 && fa(9, l, l.return);
        break;
      case 3:
        de(l);
        break;
      case 12:
        de(l);
        break;
      case 22:
        var t = l.stateNode;
        l.memoizedState !== null && t._visibility & 2 && (l.return === null || l.return.tag !== 13) ? (t._visibility &= -3, zn(l)) : de(l);
        break;
      default:
        de(l);
    }
  }
  function zn(l) {
    var t = l.deletions;
    if ((l.flags & 16) !== 0) {
      if (t !== null)
        for (var a = 0; a < t.length; a++) {
          var u = t[a];
          pl = u, pd(
            u,
            l
          );
        }
      Ad(l);
    }
    for (l = l.child; l !== null; ) {
      switch (t = l, t.tag) {
        case 0:
        case 11:
        case 15:
          fa(8, t, t.return), zn(t);
          break;
        case 22:
          a = t.stateNode, a._visibility & 2 && (a._visibility &= -3, zn(t));
          break;
        default:
          zn(t);
      }
      l = l.sibling;
    }
  }
  function pd(l, t) {
    for (; pl !== null; ) {
      var a = pl;
      switch (a.tag) {
        case 0:
        case 11:
        case 15:
          fa(8, a, t);
          break;
        case 23:
        case 22:
          if (a.memoizedState !== null && a.memoizedState.cachePool !== null) {
            var u = a.memoizedState.cachePool.pool;
            u != null && u.refCount++;
          }
          break;
        case 24:
          Wu(a.memoizedState.cache);
      }
      if (u = a.child, u !== null) u.return = a, pl = u;
      else
        l: for (a = l; pl !== null; ) {
          u = pl;
          var e = u.sibling, n = u.return;
          if (hd(u), u === a) {
            pl = null;
            break l;
          }
          if (e !== null) {
            e.return = n, pl = e;
            break l;
          }
          pl = n;
        }
    }
  }
  var jm = {
    getCacheForType: function(l) {
      var t = Dl(bl), a = t.data.get(l);
      return a === void 0 && (a = l(), t.data.set(l, a)), a;
    },
    cacheSignal: function() {
      return Dl(bl).controller.signal;
    }
  }, Gm = typeof WeakMap == "function" ? WeakMap : Map, P = 0, ol = null, x = null, J = 0, al = 0, ut = null, ia = !1, bu = !1, jf = !1, Kt = 0, vl = 0, oa = 0, Za = 0, Gf = 0, et = 0, Tu = 0, ye = null, Jl = null, Xf = !1, An = 0, Od = 0, _n = 1 / 0, pn = null, sa = null, Al = 0, da = null, Eu = null, Jt = 0, Qf = 0, Zf = null, Md = null, me = 0, Lf = null;
  function nt() {
    return (P & 2) !== 0 && J !== 0 ? J & -J : E.T !== null ? Wf() : Li();
  }
  function Dd() {
    if (et === 0)
      if ((J & 536870912) === 0 || $) {
        var l = He;
        He <<= 1, (He & 3932160) === 0 && (He = 262144), et = l;
      } else et = 536870912;
    return l = tt.current, l !== null && (l.flags |= 32), et;
  }
  function wl(l, t, a) {
    (l === ol && (al === 2 || al === 9) || l.cancelPendingCommit !== null) && (zu(l, 0), ya(
      l,
      J,
      et,
      !1
    )), Bu(l, a), ((P & 2) === 0 || l !== ol) && (l === ol && ((P & 2) === 0 && (Za |= a), vl === 4 && ya(
      l,
      J,
      et,
      !1
    )), Mt(l));
  }
  function Ud(l, t, a) {
    if ((P & 6) !== 0) throw Error(y(327));
    var u = !a && (t & 127) === 0 && (t & l.expiredLanes) === 0 || Cu(l, t), e = u ? Zm(l, t) : Vf(l, t, !0), n = u;
    do {
      if (e === 0) {
        bu && !u && ya(l, t, 0, !1);
        break;
      } else {
        if (a = l.current.alternate, n && !Xm(a)) {
          e = Vf(l, t, !1), n = !1;
          continue;
        }
        if (e === 2) {
          if (n = t, l.errorRecoveryDisabledLanes & n)
            var c = 0;
          else
            c = l.pendingLanes & -536870913, c = c !== 0 ? c : c & 536870912 ? 536870912 : 0;
          if (c !== 0) {
            t = c;
            l: {
              var f = l;
              e = ye;
              var i = f.current.memoizedState.isDehydrated;
              if (i && (zu(f, c).flags |= 256), c = Vf(
                f,
                c,
                !1
              ), c !== 2) {
                if (jf && !i) {
                  f.errorRecoveryDisabledLanes |= n, Za |= n, e = 4;
                  break l;
                }
                n = Jl, Jl = e, n !== null && (Jl === null ? Jl = n : Jl.push.apply(
                  Jl,
                  n
                ));
              }
              e = c;
            }
            if (n = !1, e !== 2) continue;
          }
        }
        if (e === 1) {
          zu(l, 0), ya(l, t, 0, !0);
          break;
        }
        l: {
          switch (u = l, n = e, n) {
            case 0:
            case 1:
              throw Error(y(345));
            case 4:
              if ((t & 4194048) !== t) break;
            case 6:
              ya(
                u,
                t,
                et,
                !ia
              );
              break l;
            case 2:
              Jl = null;
              break;
            case 3:
            case 5:
              break;
            default:
              throw Error(y(329));
          }
          if ((t & 62914560) === t && (e = An + 300 - Fl(), 10 < e)) {
            if (ya(
              u,
              t,
              et,
              !ia
            ), Be(u, 0, !0) !== 0) break l;
            Jt = t, u.timeoutHandle = c0(
              Nd.bind(
                null,
                u,
                a,
                Jl,
                pn,
                Xf,
                t,
                et,
                Za,
                Tu,
                ia,
                n,
                "Throttled",
                -0,
                0
              ),
              e
            );
            break l;
          }
          Nd(
            u,
            a,
            Jl,
            pn,
            Xf,
            t,
            et,
            Za,
            Tu,
            ia,
            n,
            null,
            -0,
            0
          );
        }
      }
      break;
    } while (!0);
    Mt(l);
  }
  function Nd(l, t, a, u, e, n, c, f, i, h, b, A, S, r) {
    if (l.timeoutHandle = -1, A = t.subtreeFlags, A & 8192 || (A & 16785408) === 16785408) {
      A = {
        stylesheets: null,
        count: 0,
        imgCount: 0,
        imgBytes: 0,
        suspenseyImages: [],
        waitingForImages: !0,
        waitingForViewTransition: !1,
        unsuspend: Rt
      }, zd(
        t,
        n,
        A
      );
      var U = (n & 62914560) === n ? An - Fl() : (n & 4194048) === n ? Od - Fl() : 0;
      if (U = Av(
        A,
        U
      ), U !== null) {
        Jt = n, l.cancelPendingCommit = U(
          Gd.bind(
            null,
            l,
            t,
            n,
            a,
            u,
            e,
            c,
            f,
            i,
            b,
            A,
            null,
            S,
            r
          )
        ), ya(l, n, c, !h);
        return;
      }
    }
    Gd(
      l,
      t,
      n,
      a,
      u,
      e,
      c,
      f,
      i
    );
  }
  function Xm(l) {
    for (var t = l; ; ) {
      var a = t.tag;
      if ((a === 0 || a === 11 || a === 15) && t.flags & 16384 && (a = t.updateQueue, a !== null && (a = a.stores, a !== null)))
        for (var u = 0; u < a.length; u++) {
          var e = a[u], n = e.getSnapshot;
          e = e.value;
          try {
            if (!Pl(n(), e)) return !1;
          } catch {
            return !1;
          }
        }
      if (a = t.child, t.subtreeFlags & 16384 && a !== null)
        a.return = t, t = a;
      else {
        if (t === l) break;
        for (; t.sibling === null; ) {
          if (t.return === null || t.return === l) return !0;
          t = t.return;
        }
        t.sibling.return = t.return, t = t.sibling;
      }
    }
    return !0;
  }
  function ya(l, t, a, u) {
    t &= ~Gf, t &= ~Za, l.suspendedLanes |= t, l.pingedLanes &= ~t, u && (l.warmLanes |= t), u = l.expirationTimes;
    for (var e = t; 0 < e; ) {
      var n = 31 - Il(e), c = 1 << n;
      u[n] = -1, e &= ~c;
    }
    a !== 0 && Xi(l, a, t);
  }
  function On() {
    return (P & 6) === 0 ? (ve(0), !1) : !0;
  }
  function xf() {
    if (x !== null) {
      if (al === 0)
        var l = x.return;
      else
        l = x, qt = Ha = null, ef(l), yu = null, Fu = 0, l = x;
      for (; l !== null; )
        fd(l.alternate, l), l = l.return;
      x = null;
    }
  }
  function zu(l, t) {
    var a = l.timeoutHandle;
    a !== -1 && (l.timeoutHandle = -1, nv(a)), a = l.cancelPendingCommit, a !== null && (l.cancelPendingCommit = null, a()), Jt = 0, xf(), ol = l, x = a = Ct(l.current, null), J = t, al = 0, ut = null, ia = !1, bu = Cu(l, t), jf = !1, Tu = et = Gf = Za = oa = vl = 0, Jl = ye = null, Xf = !1, (t & 8) !== 0 && (t |= t & 32);
    var u = l.entangledLanes;
    if (u !== 0)
      for (l = l.entanglements, u &= t; 0 < u; ) {
        var e = 31 - Il(u), n = 1 << e;
        t |= l[e], u &= ~n;
      }
    return Kt = t, Je(), a;
  }
  function Rd(l, t) {
    X = null, E.H = ee, t === du || t === ln ? (t = wo(), al = 3) : t === Jc ? (t = wo(), al = 4) : al = t === Ef ? 8 : t !== null && typeof t == "object" && typeof t.then == "function" ? 6 : 1, ut = t, x === null && (vl = 1, hn(
      l,
      st(t, l.current)
    ));
  }
  function Hd() {
    var l = tt.current;
    return l === null ? !0 : (J & 4194048) === J ? vt === null : (J & 62914560) === J || (J & 536870912) !== 0 ? l === vt : !1;
  }
  function Cd() {
    var l = E.H;
    return E.H = ee, l === null ? ee : l;
  }
  function Bd() {
    var l = E.A;
    return E.A = jm, l;
  }
  function Mn() {
    vl = 4, ia || (J & 4194048) !== J && tt.current !== null || (bu = !0), (oa & 134217727) === 0 && (Za & 134217727) === 0 || ol === null || ya(
      ol,
      J,
      et,
      !1
    );
  }
  function Vf(l, t, a) {
    var u = P;
    P |= 2;
    var e = Cd(), n = Bd();
    (ol !== l || J !== t) && (pn = null, zu(l, t)), t = !1;
    var c = vl;
    l: do
      try {
        if (al !== 0 && x !== null) {
          var f = x, i = ut;
          switch (al) {
            case 8:
              xf(), c = 6;
              break l;
            case 3:
            case 2:
            case 9:
            case 6:
              tt.current === null && (t = !0);
              var h = al;
              if (al = 0, ut = null, Au(l, f, i, h), a && bu) {
                c = 0;
                break l;
              }
              break;
            default:
              h = al, al = 0, ut = null, Au(l, f, i, h);
          }
        }
        Qm(), c = vl;
        break;
      } catch (b) {
        Rd(l, b);
      }
    while (!0);
    return t && l.shellSuspendCounter++, qt = Ha = null, P = u, E.H = e, E.A = n, x === null && (ol = null, J = 0, Je()), c;
  }
  function Qm() {
    for (; x !== null; ) qd(x);
  }
  function Zm(l, t) {
    var a = P;
    P |= 2;
    var u = Cd(), e = Bd();
    ol !== l || J !== t ? (pn = null, _n = Fl() + 500, zu(l, t)) : bu = Cu(
      l,
      t
    );
    l: do
      try {
        if (al !== 0 && x !== null) {
          t = x;
          var n = ut;
          t: switch (al) {
            case 1:
              al = 0, ut = null, Au(l, t, n, 1);
              break;
            case 2:
            case 9:
              if (Ko(n)) {
                al = 0, ut = null, Yd(t);
                break;
              }
              t = function() {
                al !== 2 && al !== 9 || ol !== l || (al = 7), Mt(l);
              }, n.then(t, t);
              break l;
            case 3:
              al = 7;
              break l;
            case 4:
              al = 5;
              break l;
            case 7:
              Ko(n) ? (al = 0, ut = null, Yd(t)) : (al = 0, ut = null, Au(l, t, n, 7));
              break;
            case 5:
              var c = null;
              switch (x.tag) {
                case 26:
                  c = x.memoizedState;
                case 5:
                case 27:
                  var f = x;
                  if (c ? E0(c) : f.stateNode.complete) {
                    al = 0, ut = null;
                    var i = f.sibling;
                    if (i !== null) x = i;
                    else {
                      var h = f.return;
                      h !== null ? (x = h, Dn(h)) : x = null;
                    }
                    break t;
                  }
              }
              al = 0, ut = null, Au(l, t, n, 5);
              break;
            case 6:
              al = 0, ut = null, Au(l, t, n, 6);
              break;
            case 8:
              xf(), vl = 6;
              break l;
            default:
              throw Error(y(462));
          }
        }
        Lm();
        break;
      } catch (b) {
        Rd(l, b);
      }
    while (!0);
    return qt = Ha = null, E.H = u, E.A = e, P = a, x !== null ? 0 : (ol = null, J = 0, Je(), vl);
  }
  function Lm() {
    for (; x !== null && !dy(); )
      qd(x);
  }
  function qd(l) {
    var t = nd(l.alternate, l, Kt);
    l.memoizedProps = l.pendingProps, t === null ? Dn(l) : x = t;
  }
  function Yd(l) {
    var t = l, a = t.alternate;
    switch (t.tag) {
      case 15:
      case 0:
        t = Ps(
          a,
          t,
          t.pendingProps,
          t.type,
          void 0,
          J
        );
        break;
      case 11:
        t = Ps(
          a,
          t,
          t.pendingProps,
          t.type.render,
          t.ref,
          J
        );
        break;
      case 5:
        ef(t);
      default:
        fd(a, t), t = x = Bo(t, Kt), t = nd(a, t, Kt);
    }
    l.memoizedProps = l.pendingProps, t === null ? Dn(l) : x = t;
  }
  function Au(l, t, a, u) {
    qt = Ha = null, ef(t), yu = null, Fu = 0;
    var e = t.return;
    try {
      if (Nm(
        l,
        e,
        t,
        a,
        J
      )) {
        vl = 1, hn(
          l,
          st(a, l.current)
        ), x = null;
        return;
      }
    } catch (n) {
      if (e !== null) throw x = e, n;
      vl = 1, hn(
        l,
        st(a, l.current)
      ), x = null;
      return;
    }
    t.flags & 32768 ? ($ || u === 1 ? l = !0 : bu || (J & 536870912) !== 0 ? l = !1 : (ia = l = !0, (u === 2 || u === 9 || u === 3 || u === 6) && (u = tt.current, u !== null && u.tag === 13 && (u.flags |= 16384))), jd(t, l)) : Dn(t);
  }
  function Dn(l) {
    var t = l;
    do {
      if ((t.flags & 32768) !== 0) {
        jd(
          t,
          ia
        );
        return;
      }
      l = t.return;
      var a = Cm(
        t.alternate,
        t,
        Kt
      );
      if (a !== null) {
        x = a;
        return;
      }
      if (t = t.sibling, t !== null) {
        x = t;
        return;
      }
      x = t = l;
    } while (t !== null);
    vl === 0 && (vl = 5);
  }
  function jd(l, t) {
    do {
      var a = Bm(l.alternate, l);
      if (a !== null) {
        a.flags &= 32767, x = a;
        return;
      }
      if (a = l.return, a !== null && (a.flags |= 32768, a.subtreeFlags = 0, a.deletions = null), !t && (l = l.sibling, l !== null)) {
        x = l;
        return;
      }
      x = l = a;
    } while (l !== null);
    vl = 6, x = null;
  }
  function Gd(l, t, a, u, e, n, c, f, i) {
    l.cancelPendingCommit = null;
    do
      Un();
    while (Al !== 0);
    if ((P & 6) !== 0) throw Error(y(327));
    if (t !== null) {
      if (t === l.current) throw Error(y(177));
      if (n = t.lanes | t.childLanes, n |= Rc, Ey(
        l,
        a,
        n,
        c,
        f,
        i
      ), l === ol && (x = ol = null, J = 0), Eu = t, da = l, Jt = a, Qf = n, Zf = e, Md = u, (t.subtreeFlags & 10256) !== 0 || (t.flags & 10256) !== 0 ? (l.callbackNode = null, l.callbackPriority = 0, Jm(Ne, function() {
        return xd(), null;
      })) : (l.callbackNode = null, l.callbackPriority = 0), u = (t.flags & 13878) !== 0, (t.subtreeFlags & 13878) !== 0 || u) {
        u = E.T, E.T = null, e = O.p, O.p = 2, c = P, P |= 4;
        try {
          qm(l, t, a);
        } finally {
          P = c, O.p = e, E.T = u;
        }
      }
      Al = 1, Xd(), Qd(), Zd();
    }
  }
  function Xd() {
    if (Al === 1) {
      Al = 0;
      var l = da, t = Eu, a = (t.flags & 13878) !== 0;
      if ((t.subtreeFlags & 13878) !== 0 || a) {
        a = E.T, E.T = null;
        var u = O.p;
        O.p = 2;
        var e = P;
        P |= 4;
        try {
          bd(t, l);
          var n = ai, c = po(l.containerInfo), f = n.focusedElem, i = n.selectionRange;
          if (c !== f && f && f.ownerDocument && _o(
            f.ownerDocument.documentElement,
            f
          )) {
            if (i !== null && Oc(f)) {
              var h = i.start, b = i.end;
              if (b === void 0 && (b = h), "selectionStart" in f)
                f.selectionStart = h, f.selectionEnd = Math.min(
                  b,
                  f.value.length
                );
              else {
                var A = f.ownerDocument || document, S = A && A.defaultView || window;
                if (S.getSelection) {
                  var r = S.getSelection(), U = f.textContent.length, B = Math.min(i.start, U), fl = i.end === void 0 ? B : Math.min(i.end, U);
                  !r.extend && B > fl && (c = fl, fl = B, B = c);
                  var m = Ao(
                    f,
                    B
                  ), s = Ao(
                    f,
                    fl
                  );
                  if (m && s && (r.rangeCount !== 1 || r.anchorNode !== m.node || r.anchorOffset !== m.offset || r.focusNode !== s.node || r.focusOffset !== s.offset)) {
                    var v = A.createRange();
                    v.setStart(m.node, m.offset), r.removeAllRanges(), B > fl ? (r.addRange(v), r.extend(s.node, s.offset)) : (v.setEnd(s.node, s.offset), r.addRange(v));
                  }
                }
              }
            }
            for (A = [], r = f; r = r.parentNode; )
              r.nodeType === 1 && A.push({
                element: r,
                left: r.scrollLeft,
                top: r.scrollTop
              });
            for (typeof f.focus == "function" && f.focus(), f = 0; f < A.length; f++) {
              var z = A[f];
              z.element.scrollLeft = z.left, z.element.scrollTop = z.top;
            }
          }
          Zn = !!ti, ai = ti = null;
        } finally {
          P = e, O.p = u, E.T = a;
        }
      }
      l.current = t, Al = 2;
    }
  }
  function Qd() {
    if (Al === 2) {
      Al = 0;
      var l = da, t = Eu, a = (t.flags & 8772) !== 0;
      if ((t.subtreeFlags & 8772) !== 0 || a) {
        a = E.T, E.T = null;
        var u = O.p;
        O.p = 2;
        var e = P;
        P |= 4;
        try {
          vd(l, t.alternate, t);
        } finally {
          P = e, O.p = u, E.T = a;
        }
      }
      Al = 3;
    }
  }
  function Zd() {
    if (Al === 4 || Al === 3) {
      Al = 0, yy();
      var l = da, t = Eu, a = Jt, u = Md;
      (t.subtreeFlags & 10256) !== 0 || (t.flags & 10256) !== 0 ? Al = 5 : (Al = 0, Eu = da = null, Ld(l, l.pendingLanes));
      var e = l.pendingLanes;
      if (e === 0 && (sa = null), fc(a), t = t.stateNode, kl && typeof kl.onCommitFiberRoot == "function")
        try {
          kl.onCommitFiberRoot(
            Hu,
            t,
            void 0,
            (t.current.flags & 128) === 128
          );
        } catch {
        }
      if (u !== null) {
        t = E.T, e = O.p, O.p = 2, E.T = null;
        try {
          for (var n = l.onRecoverableError, c = 0; c < u.length; c++) {
            var f = u[c];
            n(f.value, {
              componentStack: f.stack
            });
          }
        } finally {
          E.T = t, O.p = e;
        }
      }
      (Jt & 3) !== 0 && Un(), Mt(l), e = l.pendingLanes, (a & 261930) !== 0 && (e & 42) !== 0 ? l === Lf ? me++ : (me = 0, Lf = l) : me = 0, ve(0);
    }
  }
  function Ld(l, t) {
    (l.pooledCacheLanes &= t) === 0 && (t = l.pooledCache, t != null && (l.pooledCache = null, Wu(t)));
  }
  function Un() {
    return Xd(), Qd(), Zd(), xd();
  }
  function xd() {
    if (Al !== 5) return !1;
    var l = da, t = Qf;
    Qf = 0;
    var a = fc(Jt), u = E.T, e = O.p;
    try {
      O.p = 32 > a ? 32 : a, E.T = null, a = Zf, Zf = null;
      var n = da, c = Jt;
      if (Al = 0, Eu = da = null, Jt = 0, (P & 6) !== 0) throw Error(y(331));
      var f = P;
      if (P |= 4, _d(n.current), Ed(
        n,
        n.current,
        c,
        a
      ), P = f, ve(0, !1), kl && typeof kl.onPostCommitFiberRoot == "function")
        try {
          kl.onPostCommitFiberRoot(Hu, n);
        } catch {
        }
      return !0;
    } finally {
      O.p = e, E.T = u, Ld(l, t);
    }
  }
  function Vd(l, t, a) {
    t = st(a, t), t = Tf(l.stateNode, t, 2), l = ea(l, t, 2), l !== null && (Bu(l, 2), Mt(l));
  }
  function ul(l, t, a) {
    if (l.tag === 3)
      Vd(l, l, a);
    else
      for (; t !== null; ) {
        if (t.tag === 3) {
          Vd(
            t,
            l,
            a
          );
          break;
        } else if (t.tag === 1) {
          var u = t.stateNode;
          if (typeof t.type.getDerivedStateFromError == "function" || typeof u.componentDidCatch == "function" && (sa === null || !sa.has(u))) {
            l = st(a, l), a = Ks(2), u = ea(t, a, 2), u !== null && (Js(
              a,
              u,
              t,
              l
            ), Bu(u, 2), Mt(u));
            break;
          }
        }
        t = t.return;
      }
  }
  function Kf(l, t, a) {
    var u = l.pingCache;
    if (u === null) {
      u = l.pingCache = new Gm();
      var e = /* @__PURE__ */ new Set();
      u.set(t, e);
    } else
      e = u.get(t), e === void 0 && (e = /* @__PURE__ */ new Set(), u.set(t, e));
    e.has(a) || (jf = !0, e.add(a), l = xm.bind(null, l, t, a), t.then(l, l));
  }
  function xm(l, t, a) {
    var u = l.pingCache;
    u !== null && u.delete(t), l.pingedLanes |= l.suspendedLanes & a, l.warmLanes &= ~a, ol === l && (J & a) === a && (vl === 4 || vl === 3 && (J & 62914560) === J && 300 > Fl() - An ? (P & 2) === 0 && zu(l, 0) : Gf |= a, Tu === J && (Tu = 0)), Mt(l);
  }
  function Kd(l, t) {
    t === 0 && (t = Gi()), l = Ua(l, t), l !== null && (Bu(l, t), Mt(l));
  }
  function Vm(l) {
    var t = l.memoizedState, a = 0;
    t !== null && (a = t.retryLane), Kd(l, a);
  }
  function Km(l, t) {
    var a = 0;
    switch (l.tag) {
      case 31:
      case 13:
        var u = l.stateNode, e = l.memoizedState;
        e !== null && (a = e.retryLane);
        break;
      case 19:
        u = l.stateNode;
        break;
      case 22:
        u = l.stateNode._retryCache;
        break;
      default:
        throw Error(y(314));
    }
    u !== null && u.delete(t), Kd(l, a);
  }
  function Jm(l, t) {
    return uc(l, t);
  }
  var Nn = null, _u = null, Jf = !1, Rn = !1, wf = !1, ma = 0;
  function Mt(l) {
    l !== _u && l.next === null && (_u === null ? Nn = _u = l : _u = _u.next = l), Rn = !0, Jf || (Jf = !0, Wm());
  }
  function ve(l, t) {
    if (!wf && Rn) {
      wf = !0;
      do
        for (var a = !1, u = Nn; u !== null; ) {
          if (l !== 0) {
            var e = u.pendingLanes;
            if (e === 0) var n = 0;
            else {
              var c = u.suspendedLanes, f = u.pingedLanes;
              n = (1 << 31 - Il(42 | l) + 1) - 1, n &= e & ~(c & ~f), n = n & 201326741 ? n & 201326741 | 1 : n ? n | 2 : 0;
            }
            n !== 0 && (a = !0, $d(u, n));
          } else
            n = J, n = Be(
              u,
              u === ol ? n : 0,
              u.cancelPendingCommit !== null || u.timeoutHandle !== -1
            ), (n & 3) === 0 || Cu(u, n) || (a = !0, $d(u, n));
          u = u.next;
        }
      while (a);
      wf = !1;
    }
  }
  function wm() {
    Jd();
  }
  function Jd() {
    Rn = Jf = !1;
    var l = 0;
    ma !== 0 && ev() && (l = ma);
    for (var t = Fl(), a = null, u = Nn; u !== null; ) {
      var e = u.next, n = wd(u, t);
      n === 0 ? (u.next = null, a === null ? Nn = e : a.next = e, e === null && (_u = a)) : (a = u, (l !== 0 || (n & 3) !== 0) && (Rn = !0)), u = e;
    }
    Al !== 0 && Al !== 5 || ve(l), ma !== 0 && (ma = 0);
  }
  function wd(l, t) {
    for (var a = l.suspendedLanes, u = l.pingedLanes, e = l.expirationTimes, n = l.pendingLanes & -62914561; 0 < n; ) {
      var c = 31 - Il(n), f = 1 << c, i = e[c];
      i === -1 ? ((f & a) === 0 || (f & u) !== 0) && (e[c] = Ty(f, t)) : i <= t && (l.expiredLanes |= f), n &= ~f;
    }
    if (t = ol, a = J, a = Be(
      l,
      l === t ? a : 0,
      l.cancelPendingCommit !== null || l.timeoutHandle !== -1
    ), u = l.callbackNode, a === 0 || l === t && (al === 2 || al === 9) || l.cancelPendingCommit !== null)
      return u !== null && u !== null && ec(u), l.callbackNode = null, l.callbackPriority = 0;
    if ((a & 3) === 0 || Cu(l, a)) {
      if (t = a & -a, t === l.callbackPriority) return t;
      switch (u !== null && ec(u), fc(a)) {
        case 2:
        case 8:
          a = Yi;
          break;
        case 32:
          a = Ne;
          break;
        case 268435456:
          a = ji;
          break;
        default:
          a = Ne;
      }
      return u = Wd.bind(null, l), a = uc(a, u), l.callbackPriority = t, l.callbackNode = a, t;
    }
    return u !== null && u !== null && ec(u), l.callbackPriority = 2, l.callbackNode = null, 2;
  }
  function Wd(l, t) {
    if (Al !== 0 && Al !== 5)
      return l.callbackNode = null, l.callbackPriority = 0, null;
    var a = l.callbackNode;
    if (Un() && l.callbackNode !== a)
      return null;
    var u = J;
    return u = Be(
      l,
      l === ol ? u : 0,
      l.cancelPendingCommit !== null || l.timeoutHandle !== -1
    ), u === 0 ? null : (Ud(l, u, t), wd(l, Fl()), l.callbackNode != null && l.callbackNode === a ? Wd.bind(null, l) : null);
  }
  function $d(l, t) {
    if (Un()) return null;
    Ud(l, t, !0);
  }
  function Wm() {
    cv(function() {
      (P & 6) !== 0 ? uc(
        qi,
        wm
      ) : Jd();
    });
  }
  function Wf() {
    if (ma === 0) {
      var l = ou;
      l === 0 && (l = Re, Re <<= 1, (Re & 261888) === 0 && (Re = 256)), ma = l;
    }
    return ma;
  }
  function Fd(l) {
    return l == null || typeof l == "symbol" || typeof l == "boolean" ? null : typeof l == "function" ? l : Ge("" + l);
  }
  function kd(l, t) {
    var a = t.ownerDocument.createElement("input");
    return a.name = t.name, a.value = t.value, l.id && a.setAttribute("form", l.id), t.parentNode.insertBefore(a, t), l = new FormData(l), a.parentNode.removeChild(a), l;
  }
  function $m(l, t, a, u, e) {
    if (t === "submit" && a && a.stateNode === e) {
      var n = Fd(
        (e[Zl] || null).action
      ), c = u.submitter;
      c && (t = (t = c[Zl] || null) ? Fd(t.formAction) : c.getAttribute("formAction"), t !== null && (n = t, c = null));
      var f = new Le(
        "action",
        "action",
        null,
        u,
        e
      );
      l.push({
        event: f,
        listeners: [
          {
            instance: null,
            listener: function() {
              if (u.defaultPrevented) {
                if (ma !== 0) {
                  var i = c ? kd(e, c) : new FormData(e);
                  vf(
                    a,
                    {
                      pending: !0,
                      data: i,
                      method: e.method,
                      action: n
                    },
                    null,
                    i
                  );
                }
              } else
                typeof n == "function" && (f.preventDefault(), i = c ? kd(e, c) : new FormData(e), vf(
                  a,
                  {
                    pending: !0,
                    data: i,
                    method: e.method,
                    action: n
                  },
                  n,
                  i
                ));
            },
            currentTarget: e
          }
        ]
      });
    }
  }
  for (var $f = 0; $f < Nc.length; $f++) {
    var Ff = Nc[$f], Fm = Ff.toLowerCase(), km = Ff[0].toUpperCase() + Ff.slice(1);
    gt(
      Fm,
      "on" + km
    );
  }
  gt(Do, "onAnimationEnd"), gt(Uo, "onAnimationIteration"), gt(No, "onAnimationStart"), gt("dblclick", "onDoubleClick"), gt("focusin", "onFocus"), gt("focusout", "onBlur"), gt(mm, "onTransitionRun"), gt(vm, "onTransitionStart"), gt(hm, "onTransitionCancel"), gt(Ro, "onTransitionEnd"), $a("onMouseEnter", ["mouseout", "mouseover"]), $a("onMouseLeave", ["mouseout", "mouseover"]), $a("onPointerEnter", ["pointerout", "pointerover"]), $a("onPointerLeave", ["pointerout", "pointerover"]), pa(
    "onChange",
    "change click focusin focusout input keydown keyup selectionchange".split(" ")
  ), pa(
    "onSelect",
    "focusout contextmenu dragend focusin keydown keyup mousedown mouseup selectionchange".split(
      " "
    )
  ), pa("onBeforeInput", [
    "compositionend",
    "keypress",
    "textInput",
    "paste"
  ]), pa(
    "onCompositionEnd",
    "compositionend focusout keydown keypress keyup mousedown".split(" ")
  ), pa(
    "onCompositionStart",
    "compositionstart focusout keydown keypress keyup mousedown".split(" ")
  ), pa(
    "onCompositionUpdate",
    "compositionupdate focusout keydown keypress keyup mousedown".split(" ")
  );
  var he = "abort canplay canplaythrough durationchange emptied encrypted ended error loadeddata loadedmetadata loadstart pause play playing progress ratechange resize seeked seeking stalled suspend timeupdate volumechange waiting".split(
    " "
  ), Im = new Set(
    "beforetoggle cancel close invalid load scroll scrollend toggle".split(" ").concat(he)
  );
  function Id(l, t) {
    t = (t & 4) !== 0;
    for (var a = 0; a < l.length; a++) {
      var u = l[a], e = u.event;
      u = u.listeners;
      l: {
        var n = void 0;
        if (t)
          for (var c = u.length - 1; 0 <= c; c--) {
            var f = u[c], i = f.instance, h = f.currentTarget;
            if (f = f.listener, i !== n && e.isPropagationStopped())
              break l;
            n = f, e.currentTarget = h;
            try {
              n(e);
            } catch (b) {
              Ke(b);
            }
            e.currentTarget = null, n = i;
          }
        else
          for (c = 0; c < u.length; c++) {
            if (f = u[c], i = f.instance, h = f.currentTarget, f = f.listener, i !== n && e.isPropagationStopped())
              break l;
            n = f, e.currentTarget = h;
            try {
              n(e);
            } catch (b) {
              Ke(b);
            }
            e.currentTarget = null, n = i;
          }
      }
    }
  }
  function V(l, t) {
    var a = t[ic];
    a === void 0 && (a = t[ic] = /* @__PURE__ */ new Set());
    var u = l + "__bubble";
    a.has(u) || (Pd(t, l, 2, !1), a.add(u));
  }
  function kf(l, t, a) {
    var u = 0;
    t && (u |= 4), Pd(
      a,
      l,
      u,
      t
    );
  }
  var Hn = "_reactListening" + Math.random().toString(36).slice(2);
  function If(l) {
    if (!l[Hn]) {
      l[Hn] = !0, Ki.forEach(function(a) {
        a !== "selectionchange" && (Im.has(a) || kf(a, !1, l), kf(a, !0, l));
      });
      var t = l.nodeType === 9 ? l : l.ownerDocument;
      t === null || t[Hn] || (t[Hn] = !0, kf("selectionchange", !1, t));
    }
  }
  function Pd(l, t, a, u) {
    switch (D0(t)) {
      case 2:
        var e = Ov;
        break;
      case 8:
        e = Mv;
        break;
      default:
        e = mi;
    }
    a = e.bind(
      null,
      t,
      a,
      l
    ), e = void 0, !rc || t !== "touchstart" && t !== "touchmove" && t !== "wheel" || (e = !0), u ? e !== void 0 ? l.addEventListener(t, a, {
      capture: !0,
      passive: e
    }) : l.addEventListener(t, a, !0) : e !== void 0 ? l.addEventListener(t, a, {
      passive: e
    }) : l.addEventListener(t, a, !1);
  }
  function Pf(l, t, a, u, e) {
    var n = u;
    if ((t & 1) === 0 && (t & 2) === 0 && u !== null)
      l: for (; ; ) {
        if (u === null) return;
        var c = u.tag;
        if (c === 3 || c === 4) {
          var f = u.stateNode.containerInfo;
          if (f === e) break;
          if (c === 4)
            for (c = u.return; c !== null; ) {
              var i = c.tag;
              if ((i === 3 || i === 4) && c.stateNode.containerInfo === e)
                return;
              c = c.return;
            }
          for (; f !== null; ) {
            if (c = Ja(f), c === null) return;
            if (i = c.tag, i === 5 || i === 6 || i === 26 || i === 27) {
              u = n = c;
              continue l;
            }
            f = f.parentNode;
          }
        }
        u = u.return;
      }
    uo(function() {
      var h = n, b = hc(a), A = [];
      l: {
        var S = Ho.get(l);
        if (S !== void 0) {
          var r = Le, U = l;
          switch (l) {
            case "keypress":
              if (Qe(a) === 0) break l;
            case "keydown":
            case "keyup":
              r = Ky;
              break;
            case "focusin":
              U = "focus", r = Ec;
              break;
            case "focusout":
              U = "blur", r = Ec;
              break;
            case "beforeblur":
            case "afterblur":
              r = Ec;
              break;
            case "click":
              if (a.button === 2) break l;
            case "auxclick":
            case "dblclick":
            case "mousedown":
            case "mousemove":
            case "mouseup":
            case "mouseout":
            case "mouseover":
            case "contextmenu":
              r = co;
              break;
            case "drag":
            case "dragend":
            case "dragenter":
            case "dragexit":
            case "dragleave":
            case "dragover":
            case "dragstart":
            case "drop":
              r = Cy;
              break;
            case "touchcancel":
            case "touchend":
            case "touchmove":
            case "touchstart":
              r = Wy;
              break;
            case Do:
            case Uo:
            case No:
              r = Yy;
              break;
            case Ro:
              r = Fy;
              break;
            case "scroll":
            case "scrollend":
              r = Ry;
              break;
            case "wheel":
              r = Iy;
              break;
            case "copy":
            case "cut":
            case "paste":
              r = Gy;
              break;
            case "gotpointercapture":
            case "lostpointercapture":
            case "pointercancel":
            case "pointerdown":
            case "pointermove":
            case "pointerout":
            case "pointerover":
            case "pointerup":
              r = io;
              break;
            case "toggle":
            case "beforetoggle":
              r = lm;
          }
          var B = (t & 4) !== 0, fl = !B && (l === "scroll" || l === "scrollend"), m = B ? S !== null ? S + "Capture" : null : S;
          B = [];
          for (var s = h, v; s !== null; ) {
            var z = s;
            if (v = z.stateNode, z = z.tag, z !== 5 && z !== 26 && z !== 27 || v === null || m === null || (z = ju(s, m), z != null && B.push(
              Se(s, z, v)
            )), fl) break;
            s = s.return;
          }
          0 < B.length && (S = new r(
            S,
            U,
            null,
            a,
            b
          ), A.push({ event: S, listeners: B }));
        }
      }
      if ((t & 7) === 0) {
        l: {
          if (S = l === "mouseover" || l === "pointerover", r = l === "mouseout" || l === "pointerout", S && a !== vc && (U = a.relatedTarget || a.fromElement) && (Ja(U) || U[Ka]))
            break l;
          if ((r || S) && (S = b.window === b ? b : (S = b.ownerDocument) ? S.defaultView || S.parentWindow : window, r ? (U = a.relatedTarget || a.toElement, r = h, U = U ? Ja(U) : null, U !== null && (fl = Z(U), B = U.tag, U !== fl || B !== 5 && B !== 27 && B !== 6) && (U = null)) : (r = null, U = h), r !== U)) {
            if (B = co, z = "onMouseLeave", m = "onMouseEnter", s = "mouse", (l === "pointerout" || l === "pointerover") && (B = io, z = "onPointerLeave", m = "onPointerEnter", s = "pointer"), fl = r == null ? S : Yu(r), v = U == null ? S : Yu(U), S = new B(
              z,
              s + "leave",
              r,
              a,
              b
            ), S.target = fl, S.relatedTarget = v, z = null, Ja(b) === h && (B = new B(
              m,
              s + "enter",
              U,
              a,
              b
            ), B.target = v, B.relatedTarget = fl, z = B), fl = z, r && U)
              t: {
                for (B = Pm, m = r, s = U, v = 0, z = m; z; z = B(z))
                  v++;
                z = 0;
                for (var C = s; C; C = B(C))
                  z++;
                for (; 0 < v - z; )
                  m = B(m), v--;
                for (; 0 < z - v; )
                  s = B(s), z--;
                for (; v--; ) {
                  if (m === s || s !== null && m === s.alternate) {
                    B = m;
                    break t;
                  }
                  m = B(m), s = B(s);
                }
                B = null;
              }
            else B = null;
            r !== null && l0(
              A,
              S,
              r,
              B,
              !1
            ), U !== null && fl !== null && l0(
              A,
              fl,
              U,
              B,
              !0
            );
          }
        }
        l: {
          if (S = h ? Yu(h) : window, r = S.nodeName && S.nodeName.toLowerCase(), r === "select" || r === "input" && S.type === "file")
            var k = ro;
          else if (ho(S))
            if (go)
              k = sm;
            else {
              k = im;
              var R = fm;
            }
          else
            r = S.nodeName, !r || r.toLowerCase() !== "input" || S.type !== "checkbox" && S.type !== "radio" ? h && mc(h.elementType) && (k = ro) : k = om;
          if (k && (k = k(l, h))) {
            So(
              A,
              k,
              a,
              b
            );
            break l;
          }
          R && R(l, S, h), l === "focusout" && h && S.type === "number" && h.memoizedProps.value != null && yc(S, "number", S.value);
        }
        switch (R = h ? Yu(h) : window, l) {
          case "focusin":
            (ho(R) || R.contentEditable === "true") && (tu = R, Mc = h, Ku = null);
            break;
          case "focusout":
            Ku = Mc = tu = null;
            break;
          case "mousedown":
            Dc = !0;
            break;
          case "contextmenu":
          case "mouseup":
          case "dragend":
            Dc = !1, Oo(A, a, b);
            break;
          case "selectionchange":
            if (ym) break;
          case "keydown":
          case "keyup":
            Oo(A, a, b);
        }
        var Q;
        if (Ac)
          l: {
            switch (l) {
              case "compositionstart":
                var w = "onCompositionStart";
                break l;
              case "compositionend":
                w = "onCompositionEnd";
                break l;
              case "compositionupdate":
                w = "onCompositionUpdate";
                break l;
            }
            w = void 0;
          }
        else
          lu ? mo(l, a) && (w = "onCompositionEnd") : l === "keydown" && a.keyCode === 229 && (w = "onCompositionStart");
        w && (oo && a.locale !== "ko" && (lu || w !== "onCompositionStart" ? w === "onCompositionEnd" && lu && (Q = eo()) : (kt = b, gc = "value" in kt ? kt.value : kt.textContent, lu = !0)), R = Cn(h, w), 0 < R.length && (w = new fo(
          w,
          l,
          null,
          a,
          b
        ), A.push({ event: w, listeners: R }), Q ? w.data = Q : (Q = vo(a), Q !== null && (w.data = Q)))), (Q = am ? um(l, a) : em(l, a)) && (w = Cn(h, "onBeforeInput"), 0 < w.length && (R = new fo(
          "onBeforeInput",
          "beforeinput",
          null,
          a,
          b
        ), A.push({
          event: R,
          listeners: w
        }), R.data = Q)), $m(
          A,
          l,
          h,
          a,
          b
        );
      }
      Id(A, t);
    });
  }
  function Se(l, t, a) {
    return {
      instance: l,
      listener: t,
      currentTarget: a
    };
  }
  function Cn(l, t) {
    for (var a = t + "Capture", u = []; l !== null; ) {
      var e = l, n = e.stateNode;
      if (e = e.tag, e !== 5 && e !== 26 && e !== 27 || n === null || (e = ju(l, a), e != null && u.unshift(
        Se(l, e, n)
      ), e = ju(l, t), e != null && u.push(
        Se(l, e, n)
      )), l.tag === 3) return u;
      l = l.return;
    }
    return [];
  }
  function Pm(l) {
    if (l === null) return null;
    do
      l = l.return;
    while (l && l.tag !== 5 && l.tag !== 27);
    return l || null;
  }
  function l0(l, t, a, u, e) {
    for (var n = t._reactName, c = []; a !== null && a !== u; ) {
      var f = a, i = f.alternate, h = f.stateNode;
      if (f = f.tag, i !== null && i === u) break;
      f !== 5 && f !== 26 && f !== 27 || h === null || (i = h, e ? (h = ju(a, n), h != null && c.unshift(
        Se(a, h, i)
      )) : e || (h = ju(a, n), h != null && c.push(
        Se(a, h, i)
      ))), a = a.return;
    }
    c.length !== 0 && l.push({ event: t, listeners: c });
  }
  var lv = /\r\n?/g, tv = /\u0000|\uFFFD/g;
  function t0(l) {
    return (typeof l == "string" ? l : "" + l).replace(lv, `
`).replace(tv, "");
  }
  function a0(l, t) {
    return t = t0(t), t0(l) === t;
  }
  function cl(l, t, a, u, e, n) {
    switch (a) {
      case "children":
        typeof u == "string" ? t === "body" || t === "textarea" && u === "" || ka(l, u) : (typeof u == "number" || typeof u == "bigint") && t !== "body" && ka(l, "" + u);
        break;
      case "className":
        Ye(l, "class", u);
        break;
      case "tabIndex":
        Ye(l, "tabindex", u);
        break;
      case "dir":
      case "role":
      case "viewBox":
      case "width":
      case "height":
        Ye(l, a, u);
        break;
      case "style":
        to(l, u, n);
        break;
      case "data":
        if (t !== "object") {
          Ye(l, "data", u);
          break;
        }
      case "src":
      case "href":
        if (u === "" && (t !== "a" || a !== "href")) {
          l.removeAttribute(a);
          break;
        }
        if (u == null || typeof u == "function" || typeof u == "symbol" || typeof u == "boolean") {
          l.removeAttribute(a);
          break;
        }
        u = Ge("" + u), l.setAttribute(a, u);
        break;
      case "action":
      case "formAction":
        if (typeof u == "function") {
          l.setAttribute(
            a,
            "javascript:throw new Error('A React form was unexpectedly submitted. If you called form.submit() manually, consider using form.requestSubmit() instead. If you\\'re trying to use event.stopPropagation() in a submit event handler, consider also calling event.preventDefault().')"
          );
          break;
        } else
          typeof n == "function" && (a === "formAction" ? (t !== "input" && cl(l, t, "name", e.name, e, null), cl(
            l,
            t,
            "formEncType",
            e.formEncType,
            e,
            null
          ), cl(
            l,
            t,
            "formMethod",
            e.formMethod,
            e,
            null
          ), cl(
            l,
            t,
            "formTarget",
            e.formTarget,
            e,
            null
          )) : (cl(l, t, "encType", e.encType, e, null), cl(l, t, "method", e.method, e, null), cl(l, t, "target", e.target, e, null)));
        if (u == null || typeof u == "symbol" || typeof u == "boolean") {
          l.removeAttribute(a);
          break;
        }
        u = Ge("" + u), l.setAttribute(a, u);
        break;
      case "onClick":
        u != null && (l.onclick = Rt);
        break;
      case "onScroll":
        u != null && V("scroll", l);
        break;
      case "onScrollEnd":
        u != null && V("scrollend", l);
        break;
      case "dangerouslySetInnerHTML":
        if (u != null) {
          if (typeof u != "object" || !("__html" in u))
            throw Error(y(61));
          if (a = u.__html, a != null) {
            if (e.children != null) throw Error(y(60));
            l.innerHTML = a;
          }
        }
        break;
      case "multiple":
        l.multiple = u && typeof u != "function" && typeof u != "symbol";
        break;
      case "muted":
        l.muted = u && typeof u != "function" && typeof u != "symbol";
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
        if (u == null || typeof u == "function" || typeof u == "boolean" || typeof u == "symbol") {
          l.removeAttribute("xlink:href");
          break;
        }
        a = Ge("" + u), l.setAttributeNS(
          "http://www.w3.org/1999/xlink",
          "xlink:href",
          a
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
        u != null && typeof u != "function" && typeof u != "symbol" ? l.setAttribute(a, "" + u) : l.removeAttribute(a);
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
        u && typeof u != "function" && typeof u != "symbol" ? l.setAttribute(a, "") : l.removeAttribute(a);
        break;
      case "capture":
      case "download":
        u === !0 ? l.setAttribute(a, "") : u !== !1 && u != null && typeof u != "function" && typeof u != "symbol" ? l.setAttribute(a, u) : l.removeAttribute(a);
        break;
      case "cols":
      case "rows":
      case "size":
      case "span":
        u != null && typeof u != "function" && typeof u != "symbol" && !isNaN(u) && 1 <= u ? l.setAttribute(a, u) : l.removeAttribute(a);
        break;
      case "rowSpan":
      case "start":
        u == null || typeof u == "function" || typeof u == "symbol" || isNaN(u) ? l.removeAttribute(a) : l.setAttribute(a, u);
        break;
      case "popover":
        V("beforetoggle", l), V("toggle", l), qe(l, "popover", u);
        break;
      case "xlinkActuate":
        Nt(
          l,
          "http://www.w3.org/1999/xlink",
          "xlink:actuate",
          u
        );
        break;
      case "xlinkArcrole":
        Nt(
          l,
          "http://www.w3.org/1999/xlink",
          "xlink:arcrole",
          u
        );
        break;
      case "xlinkRole":
        Nt(
          l,
          "http://www.w3.org/1999/xlink",
          "xlink:role",
          u
        );
        break;
      case "xlinkShow":
        Nt(
          l,
          "http://www.w3.org/1999/xlink",
          "xlink:show",
          u
        );
        break;
      case "xlinkTitle":
        Nt(
          l,
          "http://www.w3.org/1999/xlink",
          "xlink:title",
          u
        );
        break;
      case "xlinkType":
        Nt(
          l,
          "http://www.w3.org/1999/xlink",
          "xlink:type",
          u
        );
        break;
      case "xmlBase":
        Nt(
          l,
          "http://www.w3.org/XML/1998/namespace",
          "xml:base",
          u
        );
        break;
      case "xmlLang":
        Nt(
          l,
          "http://www.w3.org/XML/1998/namespace",
          "xml:lang",
          u
        );
        break;
      case "xmlSpace":
        Nt(
          l,
          "http://www.w3.org/XML/1998/namespace",
          "xml:space",
          u
        );
        break;
      case "is":
        qe(l, "is", u);
        break;
      case "innerText":
      case "textContent":
        break;
      default:
        (!(2 < a.length) || a[0] !== "o" && a[0] !== "O" || a[1] !== "n" && a[1] !== "N") && (a = Uy.get(a) || a, qe(l, a, u));
    }
  }
  function li(l, t, a, u, e, n) {
    switch (a) {
      case "style":
        to(l, u, n);
        break;
      case "dangerouslySetInnerHTML":
        if (u != null) {
          if (typeof u != "object" || !("__html" in u))
            throw Error(y(61));
          if (a = u.__html, a != null) {
            if (e.children != null) throw Error(y(60));
            l.innerHTML = a;
          }
        }
        break;
      case "children":
        typeof u == "string" ? ka(l, u) : (typeof u == "number" || typeof u == "bigint") && ka(l, "" + u);
        break;
      case "onScroll":
        u != null && V("scroll", l);
        break;
      case "onScrollEnd":
        u != null && V("scrollend", l);
        break;
      case "onClick":
        u != null && (l.onclick = Rt);
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
        if (!Ji.hasOwnProperty(a))
          l: {
            if (a[0] === "o" && a[1] === "n" && (e = a.endsWith("Capture"), t = a.slice(2, e ? a.length - 7 : void 0), n = l[Zl] || null, n = n != null ? n[a] : null, typeof n == "function" && l.removeEventListener(t, n, e), typeof u == "function")) {
              typeof n != "function" && n !== null && (a in l ? l[a] = null : l.hasAttribute(a) && l.removeAttribute(a)), l.addEventListener(t, u, e);
              break l;
            }
            a in l ? l[a] = u : u === !0 ? l.setAttribute(a, "") : qe(l, a, u);
          }
    }
  }
  function Nl(l, t, a) {
    switch (t) {
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
        V("error", l), V("load", l);
        var u = !1, e = !1, n;
        for (n in a)
          if (a.hasOwnProperty(n)) {
            var c = a[n];
            if (c != null)
              switch (n) {
                case "src":
                  u = !0;
                  break;
                case "srcSet":
                  e = !0;
                  break;
                case "children":
                case "dangerouslySetInnerHTML":
                  throw Error(y(137, t));
                default:
                  cl(l, t, n, c, a, null);
              }
          }
        e && cl(l, t, "srcSet", a.srcSet, a, null), u && cl(l, t, "src", a.src, a, null);
        return;
      case "input":
        V("invalid", l);
        var f = n = c = e = null, i = null, h = null;
        for (u in a)
          if (a.hasOwnProperty(u)) {
            var b = a[u];
            if (b != null)
              switch (u) {
                case "name":
                  e = b;
                  break;
                case "type":
                  c = b;
                  break;
                case "checked":
                  i = b;
                  break;
                case "defaultChecked":
                  h = b;
                  break;
                case "value":
                  n = b;
                  break;
                case "defaultValue":
                  f = b;
                  break;
                case "children":
                case "dangerouslySetInnerHTML":
                  if (b != null)
                    throw Error(y(137, t));
                  break;
                default:
                  cl(l, t, u, b, a, null);
              }
          }
        ki(
          l,
          n,
          f,
          i,
          h,
          c,
          e,
          !1
        );
        return;
      case "select":
        V("invalid", l), u = c = n = null;
        for (e in a)
          if (a.hasOwnProperty(e) && (f = a[e], f != null))
            switch (e) {
              case "value":
                n = f;
                break;
              case "defaultValue":
                c = f;
                break;
              case "multiple":
                u = f;
              default:
                cl(l, t, e, f, a, null);
            }
        t = n, a = c, l.multiple = !!u, t != null ? Fa(l, !!u, t, !1) : a != null && Fa(l, !!u, a, !0);
        return;
      case "textarea":
        V("invalid", l), n = e = u = null;
        for (c in a)
          if (a.hasOwnProperty(c) && (f = a[c], f != null))
            switch (c) {
              case "value":
                u = f;
                break;
              case "defaultValue":
                e = f;
                break;
              case "children":
                n = f;
                break;
              case "dangerouslySetInnerHTML":
                if (f != null) throw Error(y(91));
                break;
              default:
                cl(l, t, c, f, a, null);
            }
        Pi(l, u, e, n);
        return;
      case "option":
        for (i in a)
          if (a.hasOwnProperty(i) && (u = a[i], u != null))
            switch (i) {
              case "selected":
                l.selected = u && typeof u != "function" && typeof u != "symbol";
                break;
              default:
                cl(l, t, i, u, a, null);
            }
        return;
      case "dialog":
        V("beforetoggle", l), V("toggle", l), V("cancel", l), V("close", l);
        break;
      case "iframe":
      case "object":
        V("load", l);
        break;
      case "video":
      case "audio":
        for (u = 0; u < he.length; u++)
          V(he[u], l);
        break;
      case "image":
        V("error", l), V("load", l);
        break;
      case "details":
        V("toggle", l);
        break;
      case "embed":
      case "source":
      case "link":
        V("error", l), V("load", l);
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
        for (h in a)
          if (a.hasOwnProperty(h) && (u = a[h], u != null))
            switch (h) {
              case "children":
              case "dangerouslySetInnerHTML":
                throw Error(y(137, t));
              default:
                cl(l, t, h, u, a, null);
            }
        return;
      default:
        if (mc(t)) {
          for (b in a)
            a.hasOwnProperty(b) && (u = a[b], u !== void 0 && li(
              l,
              t,
              b,
              u,
              a,
              void 0
            ));
          return;
        }
    }
    for (f in a)
      a.hasOwnProperty(f) && (u = a[f], u != null && cl(l, t, f, u, a, null));
  }
  function av(l, t, a, u) {
    switch (t) {
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
        var e = null, n = null, c = null, f = null, i = null, h = null, b = null;
        for (r in a) {
          var A = a[r];
          if (a.hasOwnProperty(r) && A != null)
            switch (r) {
              case "checked":
                break;
              case "value":
                break;
              case "defaultValue":
                i = A;
              default:
                u.hasOwnProperty(r) || cl(l, t, r, null, u, A);
            }
        }
        for (var S in u) {
          var r = u[S];
          if (A = a[S], u.hasOwnProperty(S) && (r != null || A != null))
            switch (S) {
              case "type":
                n = r;
                break;
              case "name":
                e = r;
                break;
              case "checked":
                h = r;
                break;
              case "defaultChecked":
                b = r;
                break;
              case "value":
                c = r;
                break;
              case "defaultValue":
                f = r;
                break;
              case "children":
              case "dangerouslySetInnerHTML":
                if (r != null)
                  throw Error(y(137, t));
                break;
              default:
                r !== A && cl(
                  l,
                  t,
                  S,
                  r,
                  u,
                  A
                );
            }
        }
        dc(
          l,
          c,
          f,
          i,
          h,
          b,
          n,
          e
        );
        return;
      case "select":
        r = c = f = S = null;
        for (n in a)
          if (i = a[n], a.hasOwnProperty(n) && i != null)
            switch (n) {
              case "value":
                break;
              case "multiple":
                r = i;
              default:
                u.hasOwnProperty(n) || cl(
                  l,
                  t,
                  n,
                  null,
                  u,
                  i
                );
            }
        for (e in u)
          if (n = u[e], i = a[e], u.hasOwnProperty(e) && (n != null || i != null))
            switch (e) {
              case "value":
                S = n;
                break;
              case "defaultValue":
                f = n;
                break;
              case "multiple":
                c = n;
              default:
                n !== i && cl(
                  l,
                  t,
                  e,
                  n,
                  u,
                  i
                );
            }
        t = f, a = c, u = r, S != null ? Fa(l, !!a, S, !1) : !!u != !!a && (t != null ? Fa(l, !!a, t, !0) : Fa(l, !!a, a ? [] : "", !1));
        return;
      case "textarea":
        r = S = null;
        for (f in a)
          if (e = a[f], a.hasOwnProperty(f) && e != null && !u.hasOwnProperty(f))
            switch (f) {
              case "value":
                break;
              case "children":
                break;
              default:
                cl(l, t, f, null, u, e);
            }
        for (c in u)
          if (e = u[c], n = a[c], u.hasOwnProperty(c) && (e != null || n != null))
            switch (c) {
              case "value":
                S = e;
                break;
              case "defaultValue":
                r = e;
                break;
              case "children":
                break;
              case "dangerouslySetInnerHTML":
                if (e != null) throw Error(y(91));
                break;
              default:
                e !== n && cl(l, t, c, e, u, n);
            }
        Ii(l, S, r);
        return;
      case "option":
        for (var U in a)
          if (S = a[U], a.hasOwnProperty(U) && S != null && !u.hasOwnProperty(U))
            switch (U) {
              case "selected":
                l.selected = !1;
                break;
              default:
                cl(
                  l,
                  t,
                  U,
                  null,
                  u,
                  S
                );
            }
        for (i in u)
          if (S = u[i], r = a[i], u.hasOwnProperty(i) && S !== r && (S != null || r != null))
            switch (i) {
              case "selected":
                l.selected = S && typeof S != "function" && typeof S != "symbol";
                break;
              default:
                cl(
                  l,
                  t,
                  i,
                  S,
                  u,
                  r
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
        for (var B in a)
          S = a[B], a.hasOwnProperty(B) && S != null && !u.hasOwnProperty(B) && cl(l, t, B, null, u, S);
        for (h in u)
          if (S = u[h], r = a[h], u.hasOwnProperty(h) && S !== r && (S != null || r != null))
            switch (h) {
              case "children":
              case "dangerouslySetInnerHTML":
                if (S != null)
                  throw Error(y(137, t));
                break;
              default:
                cl(
                  l,
                  t,
                  h,
                  S,
                  u,
                  r
                );
            }
        return;
      default:
        if (mc(t)) {
          for (var fl in a)
            S = a[fl], a.hasOwnProperty(fl) && S !== void 0 && !u.hasOwnProperty(fl) && li(
              l,
              t,
              fl,
              void 0,
              u,
              S
            );
          for (b in u)
            S = u[b], r = a[b], !u.hasOwnProperty(b) || S === r || S === void 0 && r === void 0 || li(
              l,
              t,
              b,
              S,
              u,
              r
            );
          return;
        }
    }
    for (var m in a)
      S = a[m], a.hasOwnProperty(m) && S != null && !u.hasOwnProperty(m) && cl(l, t, m, null, u, S);
    for (A in u)
      S = u[A], r = a[A], !u.hasOwnProperty(A) || S === r || S == null && r == null || cl(l, t, A, S, u, r);
  }
  function u0(l) {
    switch (l) {
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
  function uv() {
    if (typeof performance.getEntriesByType == "function") {
      for (var l = 0, t = 0, a = performance.getEntriesByType("resource"), u = 0; u < a.length; u++) {
        var e = a[u], n = e.transferSize, c = e.initiatorType, f = e.duration;
        if (n && f && u0(c)) {
          for (c = 0, f = e.responseEnd, u += 1; u < a.length; u++) {
            var i = a[u], h = i.startTime;
            if (h > f) break;
            var b = i.transferSize, A = i.initiatorType;
            b && u0(A) && (i = i.responseEnd, c += b * (i < f ? 1 : (f - h) / (i - h)));
          }
          if (--u, t += 8 * (n + c) / (e.duration / 1e3), l++, 10 < l) break;
        }
      }
      if (0 < l) return t / l / 1e6;
    }
    return navigator.connection && (l = navigator.connection.downlink, typeof l == "number") ? l : 5;
  }
  var ti = null, ai = null;
  function Bn(l) {
    return l.nodeType === 9 ? l : l.ownerDocument;
  }
  function e0(l) {
    switch (l) {
      case "http://www.w3.org/2000/svg":
        return 1;
      case "http://www.w3.org/1998/Math/MathML":
        return 2;
      default:
        return 0;
    }
  }
  function n0(l, t) {
    if (l === 0)
      switch (t) {
        case "svg":
          return 1;
        case "math":
          return 2;
        default:
          return 0;
      }
    return l === 1 && t === "foreignObject" ? 0 : l;
  }
  function ui(l, t) {
    return l === "textarea" || l === "noscript" || typeof t.children == "string" || typeof t.children == "number" || typeof t.children == "bigint" || typeof t.dangerouslySetInnerHTML == "object" && t.dangerouslySetInnerHTML !== null && t.dangerouslySetInnerHTML.__html != null;
  }
  var ei = null;
  function ev() {
    var l = window.event;
    return l && l.type === "popstate" ? l === ei ? !1 : (ei = l, !0) : (ei = null, !1);
  }
  var c0 = typeof setTimeout == "function" ? setTimeout : void 0, nv = typeof clearTimeout == "function" ? clearTimeout : void 0, f0 = typeof Promise == "function" ? Promise : void 0, cv = typeof queueMicrotask == "function" ? queueMicrotask : typeof f0 < "u" ? function(l) {
    return f0.resolve(null).then(l).catch(fv);
  } : c0;
  function fv(l) {
    setTimeout(function() {
      throw l;
    });
  }
  function va(l) {
    return l === "head";
  }
  function i0(l, t) {
    var a = t, u = 0;
    do {
      var e = a.nextSibling;
      if (l.removeChild(a), e && e.nodeType === 8)
        if (a = e.data, a === "/$" || a === "/&") {
          if (u === 0) {
            l.removeChild(e), Du(t);
            return;
          }
          u--;
        } else if (a === "$" || a === "$?" || a === "$~" || a === "$!" || a === "&")
          u++;
        else if (a === "html")
          re(l.ownerDocument.documentElement);
        else if (a === "head") {
          a = l.ownerDocument.head, re(a);
          for (var n = a.firstChild; n; ) {
            var c = n.nextSibling, f = n.nodeName;
            n[qu] || f === "SCRIPT" || f === "STYLE" || f === "LINK" && n.rel.toLowerCase() === "stylesheet" || a.removeChild(n), n = c;
          }
        } else
          a === "body" && re(l.ownerDocument.body);
      a = e;
    } while (a);
    Du(t);
  }
  function o0(l, t) {
    var a = l;
    l = 0;
    do {
      var u = a.nextSibling;
      if (a.nodeType === 1 ? t ? (a._stashedDisplay = a.style.display, a.style.display = "none") : (a.style.display = a._stashedDisplay || "", a.getAttribute("style") === "" && a.removeAttribute("style")) : a.nodeType === 3 && (t ? (a._stashedText = a.nodeValue, a.nodeValue = "") : a.nodeValue = a._stashedText || ""), u && u.nodeType === 8)
        if (a = u.data, a === "/$") {
          if (l === 0) break;
          l--;
        } else
          a !== "$" && a !== "$?" && a !== "$~" && a !== "$!" || l++;
      a = u;
    } while (a);
  }
  function ni(l) {
    var t = l.firstChild;
    for (t && t.nodeType === 10 && (t = t.nextSibling); t; ) {
      var a = t;
      switch (t = t.nextSibling, a.nodeName) {
        case "HTML":
        case "HEAD":
        case "BODY":
          ni(a), oc(a);
          continue;
        case "SCRIPT":
        case "STYLE":
          continue;
        case "LINK":
          if (a.rel.toLowerCase() === "stylesheet") continue;
      }
      l.removeChild(a);
    }
  }
  function iv(l, t, a, u) {
    for (; l.nodeType === 1; ) {
      var e = a;
      if (l.nodeName.toLowerCase() !== t.toLowerCase()) {
        if (!u && (l.nodeName !== "INPUT" || l.type !== "hidden"))
          break;
      } else if (u) {
        if (!l[qu])
          switch (t) {
            case "meta":
              if (!l.hasAttribute("itemprop")) break;
              return l;
            case "link":
              if (n = l.getAttribute("rel"), n === "stylesheet" && l.hasAttribute("data-precedence"))
                break;
              if (n !== e.rel || l.getAttribute("href") !== (e.href == null || e.href === "" ? null : e.href) || l.getAttribute("crossorigin") !== (e.crossOrigin == null ? null : e.crossOrigin) || l.getAttribute("title") !== (e.title == null ? null : e.title))
                break;
              return l;
            case "style":
              if (l.hasAttribute("data-precedence")) break;
              return l;
            case "script":
              if (n = l.getAttribute("src"), (n !== (e.src == null ? null : e.src) || l.getAttribute("type") !== (e.type == null ? null : e.type) || l.getAttribute("crossorigin") !== (e.crossOrigin == null ? null : e.crossOrigin)) && n && l.hasAttribute("async") && !l.hasAttribute("itemprop"))
                break;
              return l;
            default:
              return l;
          }
      } else if (t === "input" && l.type === "hidden") {
        var n = e.name == null ? null : "" + e.name;
        if (e.type === "hidden" && l.getAttribute("name") === n)
          return l;
      } else return l;
      if (l = ht(l.nextSibling), l === null) break;
    }
    return null;
  }
  function ov(l, t, a) {
    if (t === "") return null;
    for (; l.nodeType !== 3; )
      if ((l.nodeType !== 1 || l.nodeName !== "INPUT" || l.type !== "hidden") && !a || (l = ht(l.nextSibling), l === null)) return null;
    return l;
  }
  function s0(l, t) {
    for (; l.nodeType !== 8; )
      if ((l.nodeType !== 1 || l.nodeName !== "INPUT" || l.type !== "hidden") && !t || (l = ht(l.nextSibling), l === null)) return null;
    return l;
  }
  function ci(l) {
    return l.data === "$?" || l.data === "$~";
  }
  function fi(l) {
    return l.data === "$!" || l.data === "$?" && l.ownerDocument.readyState !== "loading";
  }
  function sv(l, t) {
    var a = l.ownerDocument;
    if (l.data === "$~") l._reactRetry = t;
    else if (l.data !== "$?" || a.readyState !== "loading")
      t();
    else {
      var u = function() {
        t(), a.removeEventListener("DOMContentLoaded", u);
      };
      a.addEventListener("DOMContentLoaded", u), l._reactRetry = u;
    }
  }
  function ht(l) {
    for (; l != null; l = l.nextSibling) {
      var t = l.nodeType;
      if (t === 1 || t === 3) break;
      if (t === 8) {
        if (t = l.data, t === "$" || t === "$!" || t === "$?" || t === "$~" || t === "&" || t === "F!" || t === "F")
          break;
        if (t === "/$" || t === "/&") return null;
      }
    }
    return l;
  }
  var ii = null;
  function d0(l) {
    l = l.nextSibling;
    for (var t = 0; l; ) {
      if (l.nodeType === 8) {
        var a = l.data;
        if (a === "/$" || a === "/&") {
          if (t === 0)
            return ht(l.nextSibling);
          t--;
        } else
          a !== "$" && a !== "$!" && a !== "$?" && a !== "$~" && a !== "&" || t++;
      }
      l = l.nextSibling;
    }
    return null;
  }
  function y0(l) {
    l = l.previousSibling;
    for (var t = 0; l; ) {
      if (l.nodeType === 8) {
        var a = l.data;
        if (a === "$" || a === "$!" || a === "$?" || a === "$~" || a === "&") {
          if (t === 0) return l;
          t--;
        } else a !== "/$" && a !== "/&" || t++;
      }
      l = l.previousSibling;
    }
    return null;
  }
  function m0(l, t, a) {
    switch (t = Bn(a), l) {
      case "html":
        if (l = t.documentElement, !l) throw Error(y(452));
        return l;
      case "head":
        if (l = t.head, !l) throw Error(y(453));
        return l;
      case "body":
        if (l = t.body, !l) throw Error(y(454));
        return l;
      default:
        throw Error(y(451));
    }
  }
  function re(l) {
    for (var t = l.attributes; t.length; )
      l.removeAttributeNode(t[0]);
    oc(l);
  }
  var St = /* @__PURE__ */ new Map(), v0 = /* @__PURE__ */ new Set();
  function qn(l) {
    return typeof l.getRootNode == "function" ? l.getRootNode() : l.nodeType === 9 ? l : l.ownerDocument;
  }
  var wt = O.d;
  O.d = {
    f: dv,
    r: yv,
    D: mv,
    C: vv,
    L: hv,
    m: Sv,
    X: gv,
    S: rv,
    M: bv
  };
  function dv() {
    var l = wt.f(), t = On();
    return l || t;
  }
  function yv(l) {
    var t = wa(l);
    t !== null && t.tag === 5 && t.type === "form" ? Rs(t) : wt.r(l);
  }
  var pu = typeof document > "u" ? null : document;
  function h0(l, t, a) {
    var u = pu;
    if (u && typeof t == "string" && t) {
      var e = it(t);
      e = 'link[rel="' + l + '"][href="' + e + '"]', typeof a == "string" && (e += '[crossorigin="' + a + '"]'), v0.has(e) || (v0.add(e), l = { rel: l, crossOrigin: a, href: t }, u.querySelector(e) === null && (t = u.createElement("link"), Nl(t, "link", l), _l(t), u.head.appendChild(t)));
    }
  }
  function mv(l) {
    wt.D(l), h0("dns-prefetch", l, null);
  }
  function vv(l, t) {
    wt.C(l, t), h0("preconnect", l, t);
  }
  function hv(l, t, a) {
    wt.L(l, t, a);
    var u = pu;
    if (u && l && t) {
      var e = 'link[rel="preload"][as="' + it(t) + '"]';
      t === "image" && a && a.imageSrcSet ? (e += '[imagesrcset="' + it(
        a.imageSrcSet
      ) + '"]', typeof a.imageSizes == "string" && (e += '[imagesizes="' + it(
        a.imageSizes
      ) + '"]')) : e += '[href="' + it(l) + '"]';
      var n = e;
      switch (t) {
        case "style":
          n = Ou(l);
          break;
        case "script":
          n = Mu(l);
      }
      St.has(n) || (l = q(
        {
          rel: "preload",
          href: t === "image" && a && a.imageSrcSet ? void 0 : l,
          as: t
        },
        a
      ), St.set(n, l), u.querySelector(e) !== null || t === "style" && u.querySelector(ge(n)) || t === "script" && u.querySelector(be(n)) || (t = u.createElement("link"), Nl(t, "link", l), _l(t), u.head.appendChild(t)));
    }
  }
  function Sv(l, t) {
    wt.m(l, t);
    var a = pu;
    if (a && l) {
      var u = t && typeof t.as == "string" ? t.as : "script", e = 'link[rel="modulepreload"][as="' + it(u) + '"][href="' + it(l) + '"]', n = e;
      switch (u) {
        case "audioworklet":
        case "paintworklet":
        case "serviceworker":
        case "sharedworker":
        case "worker":
        case "script":
          n = Mu(l);
      }
      if (!St.has(n) && (l = q({ rel: "modulepreload", href: l }, t), St.set(n, l), a.querySelector(e) === null)) {
        switch (u) {
          case "audioworklet":
          case "paintworklet":
          case "serviceworker":
          case "sharedworker":
          case "worker":
          case "script":
            if (a.querySelector(be(n)))
              return;
        }
        u = a.createElement("link"), Nl(u, "link", l), _l(u), a.head.appendChild(u);
      }
    }
  }
  function rv(l, t, a) {
    wt.S(l, t, a);
    var u = pu;
    if (u && l) {
      var e = Wa(u).hoistableStyles, n = Ou(l);
      t = t || "default";
      var c = e.get(n);
      if (!c) {
        var f = { loading: 0, preload: null };
        if (c = u.querySelector(
          ge(n)
        ))
          f.loading = 5;
        else {
          l = q(
            { rel: "stylesheet", href: l, "data-precedence": t },
            a
          ), (a = St.get(n)) && oi(l, a);
          var i = c = u.createElement("link");
          _l(i), Nl(i, "link", l), i._p = new Promise(function(h, b) {
            i.onload = h, i.onerror = b;
          }), i.addEventListener("load", function() {
            f.loading |= 1;
          }), i.addEventListener("error", function() {
            f.loading |= 2;
          }), f.loading |= 4, Yn(c, t, u);
        }
        c = {
          type: "stylesheet",
          instance: c,
          count: 1,
          state: f
        }, e.set(n, c);
      }
    }
  }
  function gv(l, t) {
    wt.X(l, t);
    var a = pu;
    if (a && l) {
      var u = Wa(a).hoistableScripts, e = Mu(l), n = u.get(e);
      n || (n = a.querySelector(be(e)), n || (l = q({ src: l, async: !0 }, t), (t = St.get(e)) && si(l, t), n = a.createElement("script"), _l(n), Nl(n, "link", l), a.head.appendChild(n)), n = {
        type: "script",
        instance: n,
        count: 1,
        state: null
      }, u.set(e, n));
    }
  }
  function bv(l, t) {
    wt.M(l, t);
    var a = pu;
    if (a && l) {
      var u = Wa(a).hoistableScripts, e = Mu(l), n = u.get(e);
      n || (n = a.querySelector(be(e)), n || (l = q({ src: l, async: !0, type: "module" }, t), (t = St.get(e)) && si(l, t), n = a.createElement("script"), _l(n), Nl(n, "link", l), a.head.appendChild(n)), n = {
        type: "script",
        instance: n,
        count: 1,
        state: null
      }, u.set(e, n));
    }
  }
  function S0(l, t, a, u) {
    var e = (e = L.current) ? qn(e) : null;
    if (!e) throw Error(y(446));
    switch (l) {
      case "meta":
      case "title":
        return null;
      case "style":
        return typeof a.precedence == "string" && typeof a.href == "string" ? (t = Ou(a.href), a = Wa(
          e
        ).hoistableStyles, u = a.get(t), u || (u = {
          type: "style",
          instance: null,
          count: 0,
          state: null
        }, a.set(t, u)), u) : { type: "void", instance: null, count: 0, state: null };
      case "link":
        if (a.rel === "stylesheet" && typeof a.href == "string" && typeof a.precedence == "string") {
          l = Ou(a.href);
          var n = Wa(
            e
          ).hoistableStyles, c = n.get(l);
          if (c || (e = e.ownerDocument || e, c = {
            type: "stylesheet",
            instance: null,
            count: 0,
            state: { loading: 0, preload: null }
          }, n.set(l, c), (n = e.querySelector(
            ge(l)
          )) && !n._p && (c.instance = n, c.state.loading = 5), St.has(l) || (a = {
            rel: "preload",
            as: "style",
            href: a.href,
            crossOrigin: a.crossOrigin,
            integrity: a.integrity,
            media: a.media,
            hrefLang: a.hrefLang,
            referrerPolicy: a.referrerPolicy
          }, St.set(l, a), n || Tv(
            e,
            l,
            a,
            c.state
          ))), t && u === null)
            throw Error(y(528, ""));
          return c;
        }
        if (t && u !== null)
          throw Error(y(529, ""));
        return null;
      case "script":
        return t = a.async, a = a.src, typeof a == "string" && t && typeof t != "function" && typeof t != "symbol" ? (t = Mu(a), a = Wa(
          e
        ).hoistableScripts, u = a.get(t), u || (u = {
          type: "script",
          instance: null,
          count: 0,
          state: null
        }, a.set(t, u)), u) : { type: "void", instance: null, count: 0, state: null };
      default:
        throw Error(y(444, l));
    }
  }
  function Ou(l) {
    return 'href="' + it(l) + '"';
  }
  function ge(l) {
    return 'link[rel="stylesheet"][' + l + "]";
  }
  function r0(l) {
    return q({}, l, {
      "data-precedence": l.precedence,
      precedence: null
    });
  }
  function Tv(l, t, a, u) {
    l.querySelector('link[rel="preload"][as="style"][' + t + "]") ? u.loading = 1 : (t = l.createElement("link"), u.preload = t, t.addEventListener("load", function() {
      return u.loading |= 1;
    }), t.addEventListener("error", function() {
      return u.loading |= 2;
    }), Nl(t, "link", a), _l(t), l.head.appendChild(t));
  }
  function Mu(l) {
    return '[src="' + it(l) + '"]';
  }
  function be(l) {
    return "script[async]" + l;
  }
  function g0(l, t, a) {
    if (t.count++, t.instance === null)
      switch (t.type) {
        case "style":
          var u = l.querySelector(
            'style[data-href~="' + it(a.href) + '"]'
          );
          if (u)
            return t.instance = u, _l(u), u;
          var e = q({}, a, {
            "data-href": a.href,
            "data-precedence": a.precedence,
            href: null,
            precedence: null
          });
          return u = (l.ownerDocument || l).createElement(
            "style"
          ), _l(u), Nl(u, "style", e), Yn(u, a.precedence, l), t.instance = u;
        case "stylesheet":
          e = Ou(a.href);
          var n = l.querySelector(
            ge(e)
          );
          if (n)
            return t.state.loading |= 4, t.instance = n, _l(n), n;
          u = r0(a), (e = St.get(e)) && oi(u, e), n = (l.ownerDocument || l).createElement("link"), _l(n);
          var c = n;
          return c._p = new Promise(function(f, i) {
            c.onload = f, c.onerror = i;
          }), Nl(n, "link", u), t.state.loading |= 4, Yn(n, a.precedence, l), t.instance = n;
        case "script":
          return n = Mu(a.src), (e = l.querySelector(
            be(n)
          )) ? (t.instance = e, _l(e), e) : (u = a, (e = St.get(n)) && (u = q({}, a), si(u, e)), l = l.ownerDocument || l, e = l.createElement("script"), _l(e), Nl(e, "link", u), l.head.appendChild(e), t.instance = e);
        case "void":
          return null;
        default:
          throw Error(y(443, t.type));
      }
    else
      t.type === "stylesheet" && (t.state.loading & 4) === 0 && (u = t.instance, t.state.loading |= 4, Yn(u, a.precedence, l));
    return t.instance;
  }
  function Yn(l, t, a) {
    for (var u = a.querySelectorAll(
      'link[rel="stylesheet"][data-precedence],style[data-precedence]'
    ), e = u.length ? u[u.length - 1] : null, n = e, c = 0; c < u.length; c++) {
      var f = u[c];
      if (f.dataset.precedence === t) n = f;
      else if (n !== e) break;
    }
    n ? n.parentNode.insertBefore(l, n.nextSibling) : (t = a.nodeType === 9 ? a.head : a, t.insertBefore(l, t.firstChild));
  }
  function oi(l, t) {
    l.crossOrigin == null && (l.crossOrigin = t.crossOrigin), l.referrerPolicy == null && (l.referrerPolicy = t.referrerPolicy), l.title == null && (l.title = t.title);
  }
  function si(l, t) {
    l.crossOrigin == null && (l.crossOrigin = t.crossOrigin), l.referrerPolicy == null && (l.referrerPolicy = t.referrerPolicy), l.integrity == null && (l.integrity = t.integrity);
  }
  var jn = null;
  function b0(l, t, a) {
    if (jn === null) {
      var u = /* @__PURE__ */ new Map(), e = jn = /* @__PURE__ */ new Map();
      e.set(a, u);
    } else
      e = jn, u = e.get(a), u || (u = /* @__PURE__ */ new Map(), e.set(a, u));
    if (u.has(l)) return u;
    for (u.set(l, null), a = a.getElementsByTagName(l), e = 0; e < a.length; e++) {
      var n = a[e];
      if (!(n[qu] || n[Ol] || l === "link" && n.getAttribute("rel") === "stylesheet") && n.namespaceURI !== "http://www.w3.org/2000/svg") {
        var c = n.getAttribute(t) || "";
        c = l + c;
        var f = u.get(c);
        f ? f.push(n) : u.set(c, [n]);
      }
    }
    return u;
  }
  function T0(l, t, a) {
    l = l.ownerDocument || l, l.head.insertBefore(
      a,
      t === "title" ? l.querySelector("head > title") : null
    );
  }
  function Ev(l, t, a) {
    if (a === 1 || t.itemProp != null) return !1;
    switch (l) {
      case "meta":
      case "title":
        return !0;
      case "style":
        if (typeof t.precedence != "string" || typeof t.href != "string" || t.href === "")
          break;
        return !0;
      case "link":
        if (typeof t.rel != "string" || typeof t.href != "string" || t.href === "" || t.onLoad || t.onError)
          break;
        switch (t.rel) {
          case "stylesheet":
            return l = t.disabled, typeof t.precedence == "string" && l == null;
          default:
            return !0;
        }
      case "script":
        if (t.async && typeof t.async != "function" && typeof t.async != "symbol" && !t.onLoad && !t.onError && t.src && typeof t.src == "string")
          return !0;
    }
    return !1;
  }
  function E0(l) {
    return !(l.type === "stylesheet" && (l.state.loading & 3) === 0);
  }
  function zv(l, t, a, u) {
    if (a.type === "stylesheet" && (typeof u.media != "string" || matchMedia(u.media).matches !== !1) && (a.state.loading & 4) === 0) {
      if (a.instance === null) {
        var e = Ou(u.href), n = t.querySelector(
          ge(e)
        );
        if (n) {
          t = n._p, t !== null && typeof t == "object" && typeof t.then == "function" && (l.count++, l = Gn.bind(l), t.then(l, l)), a.state.loading |= 4, a.instance = n, _l(n);
          return;
        }
        n = t.ownerDocument || t, u = r0(u), (e = St.get(e)) && oi(u, e), n = n.createElement("link"), _l(n);
        var c = n;
        c._p = new Promise(function(f, i) {
          c.onload = f, c.onerror = i;
        }), Nl(n, "link", u), a.instance = n;
      }
      l.stylesheets === null && (l.stylesheets = /* @__PURE__ */ new Map()), l.stylesheets.set(a, t), (t = a.state.preload) && (a.state.loading & 3) === 0 && (l.count++, a = Gn.bind(l), t.addEventListener("load", a), t.addEventListener("error", a));
    }
  }
  var di = 0;
  function Av(l, t) {
    return l.stylesheets && l.count === 0 && Qn(l, l.stylesheets), 0 < l.count || 0 < l.imgCount ? function(a) {
      var u = setTimeout(function() {
        if (l.stylesheets && Qn(l, l.stylesheets), l.unsuspend) {
          var n = l.unsuspend;
          l.unsuspend = null, n();
        }
      }, 6e4 + t);
      0 < l.imgBytes && di === 0 && (di = 62500 * uv());
      var e = setTimeout(
        function() {
          if (l.waitingForImages = !1, l.count === 0 && (l.stylesheets && Qn(l, l.stylesheets), l.unsuspend)) {
            var n = l.unsuspend;
            l.unsuspend = null, n();
          }
        },
        (l.imgBytes > di ? 50 : 800) + t
      );
      return l.unsuspend = a, function() {
        l.unsuspend = null, clearTimeout(u), clearTimeout(e);
      };
    } : null;
  }
  function Gn() {
    if (this.count--, this.count === 0 && (this.imgCount === 0 || !this.waitingForImages)) {
      if (this.stylesheets) Qn(this, this.stylesheets);
      else if (this.unsuspend) {
        var l = this.unsuspend;
        this.unsuspend = null, l();
      }
    }
  }
  var Xn = null;
  function Qn(l, t) {
    l.stylesheets = null, l.unsuspend !== null && (l.count++, Xn = /* @__PURE__ */ new Map(), t.forEach(_v, l), Xn = null, Gn.call(l));
  }
  function _v(l, t) {
    if (!(t.state.loading & 4)) {
      var a = Xn.get(l);
      if (a) var u = a.get(null);
      else {
        a = /* @__PURE__ */ new Map(), Xn.set(l, a);
        for (var e = l.querySelectorAll(
          "link[data-precedence],style[data-precedence]"
        ), n = 0; n < e.length; n++) {
          var c = e[n];
          (c.nodeName === "LINK" || c.getAttribute("media") !== "not all") && (a.set(c.dataset.precedence, c), u = c);
        }
        u && a.set(null, u);
      }
      e = t.instance, c = e.getAttribute("data-precedence"), n = a.get(c) || u, n === u && a.set(null, e), a.set(c, e), this.count++, u = Gn.bind(this), e.addEventListener("load", u), e.addEventListener("error", u), n ? n.parentNode.insertBefore(e, n.nextSibling) : (l = l.nodeType === 9 ? l.head : l, l.insertBefore(e, l.firstChild)), t.state.loading |= 4;
    }
  }
  var Te = {
    $$typeof: Rl,
    Provider: null,
    Consumer: null,
    _currentValue: Y,
    _currentValue2: Y,
    _threadCount: 0
  };
  function pv(l, t, a, u, e, n, c, f, i) {
    this.tag = 1, this.containerInfo = l, this.pingCache = this.current = this.pendingChildren = null, this.timeoutHandle = -1, this.callbackNode = this.next = this.pendingContext = this.context = this.cancelPendingCommit = null, this.callbackPriority = 0, this.expirationTimes = nc(-1), this.entangledLanes = this.shellSuspendCounter = this.errorRecoveryDisabledLanes = this.expiredLanes = this.warmLanes = this.pingedLanes = this.suspendedLanes = this.pendingLanes = 0, this.entanglements = nc(0), this.hiddenUpdates = nc(null), this.identifierPrefix = u, this.onUncaughtError = e, this.onCaughtError = n, this.onRecoverableError = c, this.pooledCache = null, this.pooledCacheLanes = 0, this.formState = i, this.incompleteTransitions = /* @__PURE__ */ new Map();
  }
  function z0(l, t, a, u, e, n, c, f, i, h, b, A) {
    return l = new pv(
      l,
      t,
      a,
      c,
      i,
      h,
      b,
      A,
      f
    ), t = 1, n === !0 && (t |= 24), n = lt(3, null, null, t), l.current = n, n.stateNode = l, t = xc(), t.refCount++, l.pooledCache = t, t.refCount++, n.memoizedState = {
      element: u,
      isDehydrated: a,
      cache: t
    }, wc(n), l;
  }
  function A0(l) {
    return l ? (l = eu, l) : eu;
  }
  function _0(l, t, a, u, e, n) {
    e = A0(e), u.context === null ? u.context = e : u.pendingContext = e, u = ua(t), u.payload = { element: a }, n = n === void 0 ? null : n, n !== null && (u.callback = n), a = ea(l, u, t), a !== null && (wl(a, l, t), Iu(a, l, t));
  }
  function p0(l, t) {
    if (l = l.memoizedState, l !== null && l.dehydrated !== null) {
      var a = l.retryLane;
      l.retryLane = a !== 0 && a < t ? a : t;
    }
  }
  function yi(l, t) {
    p0(l, t), (l = l.alternate) && p0(l, t);
  }
  function O0(l) {
    if (l.tag === 13 || l.tag === 31) {
      var t = Ua(l, 67108864);
      t !== null && wl(t, l, 67108864), yi(l, 67108864);
    }
  }
  function M0(l) {
    if (l.tag === 13 || l.tag === 31) {
      var t = nt();
      t = cc(t);
      var a = Ua(l, t);
      a !== null && wl(a, l, t), yi(l, t);
    }
  }
  var Zn = !0;
  function Ov(l, t, a, u) {
    var e = E.T;
    E.T = null;
    var n = O.p;
    try {
      O.p = 2, mi(l, t, a, u);
    } finally {
      O.p = n, E.T = e;
    }
  }
  function Mv(l, t, a, u) {
    var e = E.T;
    E.T = null;
    var n = O.p;
    try {
      O.p = 8, mi(l, t, a, u);
    } finally {
      O.p = n, E.T = e;
    }
  }
  function mi(l, t, a, u) {
    if (Zn) {
      var e = vi(u);
      if (e === null)
        Pf(
          l,
          t,
          u,
          Ln,
          a
        ), U0(l, u);
      else if (Uv(
        e,
        l,
        t,
        a,
        u
      ))
        u.stopPropagation();
      else if (U0(l, u), t & 4 && -1 < Dv.indexOf(l)) {
        for (; e !== null; ) {
          var n = wa(e);
          if (n !== null)
            switch (n.tag) {
              case 3:
                if (n = n.stateNode, n.current.memoizedState.isDehydrated) {
                  var c = _a(n.pendingLanes);
                  if (c !== 0) {
                    var f = n;
                    for (f.pendingLanes |= 2, f.entangledLanes |= 2; c; ) {
                      var i = 1 << 31 - Il(c);
                      f.entanglements[1] |= i, c &= ~i;
                    }
                    Mt(n), (P & 6) === 0 && (_n = Fl() + 500, ve(0));
                  }
                }
                break;
              case 31:
              case 13:
                f = Ua(n, 2), f !== null && wl(f, n, 2), On(), yi(n, 2);
            }
          if (n = vi(u), n === null && Pf(
            l,
            t,
            u,
            Ln,
            a
          ), n === e) break;
          e = n;
        }
        e !== null && u.stopPropagation();
      } else
        Pf(
          l,
          t,
          u,
          null,
          a
        );
    }
  }
  function vi(l) {
    return l = hc(l), hi(l);
  }
  var Ln = null;
  function hi(l) {
    if (Ln = null, l = Ja(l), l !== null) {
      var t = Z(l);
      if (t === null) l = null;
      else {
        var a = t.tag;
        if (a === 13) {
          if (l = K(t), l !== null) return l;
          l = null;
        } else if (a === 31) {
          if (l = gl(t), l !== null) return l;
          l = null;
        } else if (a === 3) {
          if (t.stateNode.current.memoizedState.isDehydrated)
            return t.tag === 3 ? t.stateNode.containerInfo : null;
          l = null;
        } else t !== l && (l = null);
      }
    }
    return Ln = l, null;
  }
  function D0(l) {
    switch (l) {
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
        switch (my()) {
          case qi:
            return 2;
          case Yi:
            return 8;
          case Ne:
          case vy:
            return 32;
          case ji:
            return 268435456;
          default:
            return 32;
        }
      default:
        return 32;
    }
  }
  var Si = !1, ha = null, Sa = null, ra = null, Ee = /* @__PURE__ */ new Map(), ze = /* @__PURE__ */ new Map(), ga = [], Dv = "mousedown mouseup touchcancel touchend touchstart auxclick dblclick pointercancel pointerdown pointerup dragend dragstart drop compositionend compositionstart keydown keypress keyup input textInput copy cut paste click change contextmenu reset".split(
    " "
  );
  function U0(l, t) {
    switch (l) {
      case "focusin":
      case "focusout":
        ha = null;
        break;
      case "dragenter":
      case "dragleave":
        Sa = null;
        break;
      case "mouseover":
      case "mouseout":
        ra = null;
        break;
      case "pointerover":
      case "pointerout":
        Ee.delete(t.pointerId);
        break;
      case "gotpointercapture":
      case "lostpointercapture":
        ze.delete(t.pointerId);
    }
  }
  function Ae(l, t, a, u, e, n) {
    return l === null || l.nativeEvent !== n ? (l = {
      blockedOn: t,
      domEventName: a,
      eventSystemFlags: u,
      nativeEvent: n,
      targetContainers: [e]
    }, t !== null && (t = wa(t), t !== null && O0(t)), l) : (l.eventSystemFlags |= u, t = l.targetContainers, e !== null && t.indexOf(e) === -1 && t.push(e), l);
  }
  function Uv(l, t, a, u, e) {
    switch (t) {
      case "focusin":
        return ha = Ae(
          ha,
          l,
          t,
          a,
          u,
          e
        ), !0;
      case "dragenter":
        return Sa = Ae(
          Sa,
          l,
          t,
          a,
          u,
          e
        ), !0;
      case "mouseover":
        return ra = Ae(
          ra,
          l,
          t,
          a,
          u,
          e
        ), !0;
      case "pointerover":
        var n = e.pointerId;
        return Ee.set(
          n,
          Ae(
            Ee.get(n) || null,
            l,
            t,
            a,
            u,
            e
          )
        ), !0;
      case "gotpointercapture":
        return n = e.pointerId, ze.set(
          n,
          Ae(
            ze.get(n) || null,
            l,
            t,
            a,
            u,
            e
          )
        ), !0;
    }
    return !1;
  }
  function N0(l) {
    var t = Ja(l.target);
    if (t !== null) {
      var a = Z(t);
      if (a !== null) {
        if (t = a.tag, t === 13) {
          if (t = K(a), t !== null) {
            l.blockedOn = t, xi(l.priority, function() {
              M0(a);
            });
            return;
          }
        } else if (t === 31) {
          if (t = gl(a), t !== null) {
            l.blockedOn = t, xi(l.priority, function() {
              M0(a);
            });
            return;
          }
        } else if (t === 3 && a.stateNode.current.memoizedState.isDehydrated) {
          l.blockedOn = a.tag === 3 ? a.stateNode.containerInfo : null;
          return;
        }
      }
    }
    l.blockedOn = null;
  }
  function xn(l) {
    if (l.blockedOn !== null) return !1;
    for (var t = l.targetContainers; 0 < t.length; ) {
      var a = vi(l.nativeEvent);
      if (a === null) {
        a = l.nativeEvent;
        var u = new a.constructor(
          a.type,
          a
        );
        vc = u, a.target.dispatchEvent(u), vc = null;
      } else
        return t = wa(a), t !== null && O0(t), l.blockedOn = a, !1;
      t.shift();
    }
    return !0;
  }
  function R0(l, t, a) {
    xn(l) && a.delete(t);
  }
  function Nv() {
    Si = !1, ha !== null && xn(ha) && (ha = null), Sa !== null && xn(Sa) && (Sa = null), ra !== null && xn(ra) && (ra = null), Ee.forEach(R0), ze.forEach(R0);
  }
  function Vn(l, t) {
    l.blockedOn === t && (l.blockedOn = null, Si || (Si = !0, o.unstable_scheduleCallback(
      o.unstable_NormalPriority,
      Nv
    )));
  }
  var Kn = null;
  function H0(l) {
    Kn !== l && (Kn = l, o.unstable_scheduleCallback(
      o.unstable_NormalPriority,
      function() {
        Kn === l && (Kn = null);
        for (var t = 0; t < l.length; t += 3) {
          var a = l[t], u = l[t + 1], e = l[t + 2];
          if (typeof u != "function") {
            if (hi(u || a) === null)
              continue;
            break;
          }
          var n = wa(a);
          n !== null && (l.splice(t, 3), t -= 3, vf(
            n,
            {
              pending: !0,
              data: e,
              method: a.method,
              action: u
            },
            u,
            e
          ));
        }
      }
    ));
  }
  function Du(l) {
    function t(i) {
      return Vn(i, l);
    }
    ha !== null && Vn(ha, l), Sa !== null && Vn(Sa, l), ra !== null && Vn(ra, l), Ee.forEach(t), ze.forEach(t);
    for (var a = 0; a < ga.length; a++) {
      var u = ga[a];
      u.blockedOn === l && (u.blockedOn = null);
    }
    for (; 0 < ga.length && (a = ga[0], a.blockedOn === null); )
      N0(a), a.blockedOn === null && ga.shift();
    if (a = (l.ownerDocument || l).$$reactFormReplay, a != null)
      for (u = 0; u < a.length; u += 3) {
        var e = a[u], n = a[u + 1], c = e[Zl] || null;
        if (typeof n == "function")
          c || H0(a);
        else if (c) {
          var f = null;
          if (n && n.hasAttribute("formAction")) {
            if (e = n, c = n[Zl] || null)
              f = c.formAction;
            else if (hi(e) !== null) continue;
          } else f = c.action;
          typeof f == "function" ? a[u + 1] = f : (a.splice(u, 3), u -= 3), H0(a);
        }
      }
  }
  function C0() {
    function l(n) {
      n.canIntercept && n.info === "react-transition" && n.intercept({
        handler: function() {
          return new Promise(function(c) {
            return e = c;
          });
        },
        focusReset: "manual",
        scroll: "manual"
      });
    }
    function t() {
      e !== null && (e(), e = null), u || setTimeout(a, 20);
    }
    function a() {
      if (!u && !navigation.transition) {
        var n = navigation.currentEntry;
        n && n.url != null && navigation.navigate(n.url, {
          state: n.getState(),
          info: "react-transition",
          history: "replace"
        });
      }
    }
    if (typeof navigation == "object") {
      var u = !1, e = null;
      return navigation.addEventListener("navigate", l), navigation.addEventListener("navigatesuccess", t), navigation.addEventListener("navigateerror", t), setTimeout(a, 100), function() {
        u = !0, navigation.removeEventListener("navigate", l), navigation.removeEventListener("navigatesuccess", t), navigation.removeEventListener("navigateerror", t), e !== null && (e(), e = null);
      };
    }
  }
  function ri(l) {
    this._internalRoot = l;
  }
  Jn.prototype.render = ri.prototype.render = function(l) {
    var t = this._internalRoot;
    if (t === null) throw Error(y(409));
    var a = t.current, u = nt();
    _0(a, u, l, t, null, null);
  }, Jn.prototype.unmount = ri.prototype.unmount = function() {
    var l = this._internalRoot;
    if (l !== null) {
      this._internalRoot = null;
      var t = l.containerInfo;
      _0(l.current, 2, null, l, null, null), On(), t[Ka] = null;
    }
  };
  function Jn(l) {
    this._internalRoot = l;
  }
  Jn.prototype.unstable_scheduleHydration = function(l) {
    if (l) {
      var t = Li();
      l = { blockedOn: null, target: l, priority: t };
      for (var a = 0; a < ga.length && t !== 0 && t < ga[a].priority; a++) ;
      ga.splice(a, 0, l), a === 0 && N0(l);
    }
  };
  var B0 = g.version;
  if (B0 !== "19.2.4")
    throw Error(
      y(
        527,
        B0,
        "19.2.4"
      )
    );
  O.findDOMNode = function(l) {
    var t = l._reactInternals;
    if (t === void 0)
      throw typeof l.render == "function" ? Error(y(188)) : (l = Object.keys(l).join(","), Error(y(268, l)));
    return l = p(t), l = l !== null ? F(l) : null, l = l === null ? null : l.stateNode, l;
  };
  var Rv = {
    bundleType: 0,
    version: "19.2.4",
    rendererPackageName: "react-dom",
    currentDispatcherRef: E,
    reconcilerVersion: "19.2.4"
  };
  if (typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ < "u") {
    var wn = __REACT_DEVTOOLS_GLOBAL_HOOK__;
    if (!wn.isDisabled && wn.supportsFiber)
      try {
        Hu = wn.inject(
          Rv
        ), kl = wn;
      } catch {
      }
  }
  return Oe.createRoot = function(l, t) {
    if (!D(l)) throw Error(y(299));
    var a = !1, u = "", e = Zs, n = Ls, c = xs;
    return t != null && (t.unstable_strictMode === !0 && (a = !0), t.identifierPrefix !== void 0 && (u = t.identifierPrefix), t.onUncaughtError !== void 0 && (e = t.onUncaughtError), t.onCaughtError !== void 0 && (n = t.onCaughtError), t.onRecoverableError !== void 0 && (c = t.onRecoverableError)), t = z0(
      l,
      1,
      !1,
      null,
      null,
      a,
      u,
      null,
      e,
      n,
      c,
      C0
    ), l[Ka] = t.current, If(l), new ri(t);
  }, Oe.hydrateRoot = function(l, t, a) {
    if (!D(l)) throw Error(y(299));
    var u = !1, e = "", n = Zs, c = Ls, f = xs, i = null;
    return a != null && (a.unstable_strictMode === !0 && (u = !0), a.identifierPrefix !== void 0 && (e = a.identifierPrefix), a.onUncaughtError !== void 0 && (n = a.onUncaughtError), a.onCaughtError !== void 0 && (c = a.onCaughtError), a.onRecoverableError !== void 0 && (f = a.onRecoverableError), a.formState !== void 0 && (i = a.formState)), t = z0(
      l,
      1,
      !0,
      t,
      a ?? null,
      u,
      e,
      i,
      n,
      c,
      f,
      C0
    ), t.context = A0(null), a = t.current, u = nt(), u = cc(u), e = ua(u), e.callback = null, ea(a, e, u), a = u, t.current.lanes = a, Bu(t, a), Mt(t), l[Ka] = t.current, If(l), new Jn(t);
  }, Oe.version = "19.2.4", Oe;
}
var K0;
function eh() {
  if (K0) return Ti.exports;
  K0 = 1;
  function o() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(o);
      } catch (g) {
        console.error(g);
      }
  }
  return o(), Ti.exports = uh(), Ti.exports;
}
var nh = eh(), cy = ny();
const vh = /* @__PURE__ */ I0(cy);
class fy {
  constructor(g) {
    this._subscribers = /* @__PURE__ */ new Set(), this.getSnapshot = () => this._state, this.subscribeStore = (T) => (this._subscribers.add(T), () => this._subscribers.delete(T)), this._state = { ...g };
  }
  replaceState(g) {
    this._state = { ...g }, this._notify();
  }
  applyPatch(g) {
    this._state = { ...this._state, ...g }, this._notify();
  }
  _notify() {
    for (const g of this._subscribers)
      g();
  }
}
const xa = Yl.createContext(null), Ea = /* @__PURE__ */ new Map();
let iy = "", J0 = !1;
function Uu() {
  return iy + "/";
}
function Hi(o, g, T, y, D) {
  D !== void 0 && (iy = D), J0 ? D !== void 0 && G0(Uu() + "react-api/events") : (J0 = !0, Bv(Uu()), G0(Uu() + "react-api/events"));
  const Z = y ?? "";
  qv(Z);
  const K = document.getElementById(o);
  if (!K) {
    console.error("[TLReact] Mount point not found:", o);
    return;
  }
  const gl = k0(g);
  if (!gl) {
    console.error("[TLReact] Component not registered:", g);
    return;
  }
  Di(o);
  const H = new fy(T);
  T.hidden === !0 && (K.style.display = "none");
  const p = (ll) => {
    H.applyPatch(ll);
  };
  uy(o, p);
  const F = nh.createRoot(K);
  Ea.set(o, { root: F, store: H, sseListener: p }), oy = Z;
  const q = () => {
    const ll = Yl.useSyncExternalStore(H.subscribeStore, H.getSnapshot);
    return Yl.useLayoutEffect(() => {
      K.style.display = ll.hidden === !0 ? "none" : "";
    }, [ll.hidden]), Wn.createElement(
      xa.Provider,
      { value: { controlId: o, windowName: Z, store: H } },
      Wn.createElement(gl, { controlId: o, state: ll })
    );
  };
  cy.flushSync(() => {
    F.render(Wn.createElement(q));
  });
}
function ch(o, g, T) {
  Hi(o, g, T);
}
function Di(o) {
  const g = Ea.get(o);
  g && (Fv(o, g.sseListener), g.root && g.root.unmount(), Ea.delete(o));
}
function fh(o, g) {
  let T = Ea.get(o);
  if (!T) {
    const D = new fy(g), Z = (K) => {
      D.applyPatch(K);
    };
    uy(o, Z), T = { root: null, store: D, sseListener: Z }, Ea.set(o, T);
  }
  return { controlId: o, windowName: oy, store: T.store };
}
let oy = "";
function ih() {
  const o = Yl.useContext(xa);
  if (!o)
    throw new Error("useTLState must be used inside a TLReact-mounted component.");
  return Yl.useSyncExternalStore(o.store.subscribeStore, o.store.getSnapshot);
}
function oh() {
  const o = Yl.useContext(xa);
  if (!o)
    throw new Error("useTLCommand must be used inside a TLReact-mounted component.");
  const g = o.controlId, T = o.windowName;
  return Yl.useCallback(
    async (y, D) => {
      const Z = JSON.stringify({
        controlId: g,
        command: y,
        windowName: T,
        arguments: D ?? {}
      });
      try {
        const K = await fetch(Uu() + "react-api/command", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: Z
        });
        K.ok || console.error("[TLReact] Command failed:", K.status, await K.text());
      } catch (K) {
        console.error("[TLReact] Command error:", K);
      }
    },
    [g, T]
  );
}
function hh() {
  const o = Yl.useContext(xa);
  if (!o)
    throw new Error("useTLUpload must be used inside a TLReact-mounted component.");
  const g = o.controlId, T = o.windowName;
  return Yl.useCallback(
    async (y) => {
      y.append("controlId", g), y.append("windowName", T);
      try {
        const D = await fetch(Uu() + "react-api/upload", {
          method: "POST",
          body: y
        });
        D.ok || console.error("[TLReact] Upload failed:", D.status, await D.text());
      } catch (D) {
        console.error("[TLReact] Upload error:", D);
      }
    },
    [g, T]
  );
}
function Sh() {
  const o = Yl.useContext(xa);
  if (!o)
    throw new Error("useTLDataUrl must be used inside a TLReact-mounted component.");
  return Uu() + "react-api/data?controlId=" + encodeURIComponent(o.controlId);
}
function rh() {
  const o = ih(), g = oh(), T = Yl.useContext(xa), y = Yl.useCallback(
    (D) => {
      T == null || T.store.applyPatch({ value: D }), g("valueChanged", { value: D });
    },
    [g, T]
  );
  return [o.value, y];
}
function kn(o = document.body) {
  const g = o.querySelectorAll("[data-react-module]");
  for (const T of g) {
    if (!T.id || Ea.has(T.id))
      continue;
    const y = T.dataset.reactModule, D = T.dataset.windowName, Z = T.dataset.contextPath;
    if (!y || D === void 0 || Z === void 0)
      continue;
    const K = T.dataset.reactState, gl = K ? JSON.parse(K) : {};
    Hi(T.id, y, gl, D, Z);
  }
}
function w0() {
  new MutationObserver((g) => {
    var T;
    for (const y of g)
      for (const D of y.removedNodes)
        if (D instanceof HTMLElement) {
          const Z = D.id;
          Z && Ea.has(Z) && Di(Z);
          for (const K of Ea.keys())
            D.querySelector("#" + CSS.escape(K)) && Di(K);
        }
    for (const y of g)
      for (const D of y.addedNodes)
        D instanceof HTMLElement && ((T = D.dataset) != null && T.reactModule ? kn(D.parentElement ?? document.body) : D.querySelector("[data-react-module]") && kn(D));
  }).observe(document.body, { childList: !0, subtree: !0 });
}
document.readyState === "loading" ? document.addEventListener("DOMContentLoaded", w0) : w0();
window.addEventListener("load", () => kn());
var _i = { exports: {} }, Me = {};
/**
 * @license React
 * react-jsx-runtime.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var W0;
function sh() {
  if (W0) return Me;
  W0 = 1;
  var o = Symbol.for("react.transitional.element"), g = Symbol.for("react.fragment");
  function T(y, D, Z) {
    var K = null;
    if (Z !== void 0 && (K = "" + Z), D.key !== void 0 && (K = "" + D.key), "key" in D) {
      Z = {};
      for (var gl in D)
        gl !== "key" && (Z[gl] = D[gl]);
    } else Z = D;
    return D = Z.ref, {
      $$typeof: o,
      type: y,
      key: K,
      ref: D !== void 0 ? D : null,
      props: Z
    };
  }
  return Me.Fragment = g, Me.jsx = T, Me.jsxs = T, Me;
}
var $0;
function dh() {
  return $0 || ($0 = 1, _i.exports = sh()), _i.exports;
}
var pi = dh();
const gh = ({ control: o }) => {
  const g = o, T = k0(g.module), y = Yl.useMemo(
    () => fh(g.controlId, g.state),
    [g.controlId]
  ), D = Yl.useSyncExternalStore(y.store.subscribeStore, y.store.getSnapshot);
  return T ? /* @__PURE__ */ pi.jsx(xa.Provider, { value: y, children: /* @__PURE__ */ pi.jsx(T, { controlId: g.controlId, state: D }) }) : /* @__PURE__ */ pi.jsxs("span", { children: [
    "[Component not registered: ",
    g.module,
    "]"
  ] });
};
window.TLReact = { mount: Hi, mountField: ch, discoverAndMount: kn };
wv();
export {
  Wn as React,
  vh as ReactDOM,
  gh as TLChild,
  xa as TLControlContext,
  G0 as connect,
  fh as createChildContext,
  kn as discoverAndMount,
  k0 as getComponent,
  Hi as mount,
  ch as mountField,
  yh as register,
  uy as subscribe,
  Di as unmount,
  Fv as unsubscribe,
  mh as useI18N,
  oh as useTLCommand,
  Sh as useTLDataUrl,
  rh as useTLFieldValue,
  ih as useTLState,
  hh as useTLUpload
};
