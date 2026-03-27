const k0 = /* @__PURE__ */ new Map();
function yh(o, g) {
  k0.set(o, g);
}
function I0(o) {
  return k0.get(o);
}
function P0(o) {
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
var Y0;
function Bv() {
  if (Y0) return j;
  Y0 = 1;
  var o = Symbol.for("react.transitional.element"), g = Symbol.for("react.portal"), z = Symbol.for("react.fragment"), m = Symbol.for("react.strict_mode"), D = Symbol.for("react.profiler"), G = Symbol.for("react.consumer"), K = Symbol.for("react.context"), ml = Symbol.for("react.forward_ref"), N = Symbol.for("react.suspense"), p = Symbol.for("react.memo"), F = Symbol.for("react.lazy"), q = Symbol.for("react.activity"), ll = Symbol.iterator;
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
  }, Bl = Object.assign, Dt = {};
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
  function $t() {
  }
  $t.prototype = $l.prototype;
  function Rl(d, _, M) {
    this.props = d, this.context = _, this.refs = Dt, this.updater = M || jl;
  }
  var ct = Rl.prototype = new $t();
  ct.constructor = Rl, Bl(ct, $l.prototype), ct.isPureReactComponent = !0;
  var Et = Array.isArray;
  function Gl() {
  }
  var W = { H: null, A: null, T: null, S: null }, Xl = Object.prototype.hasOwnProperty;
  function zt(d, _, M) {
    var R = M.ref;
    return {
      $$typeof: o,
      type: d,
      key: _,
      ref: R !== void 0 ? R : null,
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
  function T(d, _, M, R, X) {
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
              return tl = d._init, T(
                tl(d._payload),
                _,
                M,
                R,
                X
              );
          }
      }
    if (tl)
      return X = X(d), tl = R === "" ? "." + Ut(d, 0) : R, Et(X) ? (M = "", tl != null && (M = tl.replace(za, "$&/") + "/"), T(X, _, M, "", function(Nu) {
        return Nu;
      })) : X != null && (At(X) && (X = Va(
        X,
        M + (X.key == null || d && d.key === X.key ? "" : ("" + X.key).replace(
          za,
          "$&/"
        ) + "/") + tl
      )), _.push(X)), 1;
    tl = 0;
    var ql = R === "" ? "." : R + ":";
    if (Et(d))
      for (var Sl = 0; Sl < d.length; Sl++)
        R = d[Sl], L = ql + Ut(R, Sl), tl += T(
          R,
          _,
          M,
          L,
          X
        );
    else if (Sl = Wl(d), typeof Sl == "function")
      for (d = Sl.call(d), Sl = 0; !(R = d.next()).done; )
        R = R.value, L = ql + Ut(R, Sl++), tl += T(
          R,
          _,
          M,
          L,
          X
        );
    else if (L === "object") {
      if (typeof d.then == "function")
        return T(
          rt(d),
          _,
          M,
          R,
          X
        );
      throw _ = String(d), Error(
        "Objects are not valid as a React child (found: " + (_ === "[object Object]" ? "object with keys {" + Object.keys(d).join(", ") + "}" : _) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return tl;
  }
  function O(d, _, M) {
    if (d == null) return d;
    var R = [], X = 0;
    return T(d, R, "", "", function(L) {
      return _.call(M, L, X++);
    }), R;
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
  return j.Activity = q, j.Children = il, j.Component = $l, j.Fragment = z, j.Profiler = D, j.PureComponent = Rl, j.StrictMode = m, j.Suspense = N, j.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = W, j.__COMPILER_RUNTIME = {
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
    var R = Bl({}, d.props), X = d.key;
    if (_ != null)
      for (L in _.key !== void 0 && (X = "" + _.key), _)
        !Xl.call(_, L) || L === "key" || L === "__self" || L === "__source" || L === "ref" && _.ref === void 0 || (R[L] = _[L]);
    var L = arguments.length - 2;
    if (L === 1) R.children = M;
    else if (1 < L) {
      for (var tl = Array(L), ql = 0; ql < L; ql++)
        tl[ql] = arguments[ql + 2];
      R.children = tl;
    }
    return zt(d.type, X, R);
  }, j.createContext = function(d) {
    return d = {
      $$typeof: K,
      _currentValue: d,
      _currentValue2: d,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, d.Provider = d, d.Consumer = {
      $$typeof: G,
      _context: d
    }, d;
  }, j.createElement = function(d, _, M) {
    var R, X = {}, L = null;
    if (_ != null)
      for (R in _.key !== void 0 && (L = "" + _.key), _)
        Xl.call(_, R) && R !== "key" && R !== "__self" && R !== "__source" && (X[R] = _[R]);
    var tl = arguments.length - 2;
    if (tl === 1) X.children = M;
    else if (1 < tl) {
      for (var ql = Array(tl), Sl = 0; Sl < tl; Sl++)
        ql[Sl] = arguments[Sl + 2];
      X.children = ql;
    }
    if (d && d.defaultProps)
      for (R in tl = d.defaultProps, tl)
        X[R] === void 0 && (X[R] = tl[R]);
    return zt(d, L, X);
  }, j.createRef = function() {
    return { current: null };
  }, j.forwardRef = function(d) {
    return { $$typeof: ml, render: d };
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
      var R = d(), X = W.S;
      X !== null && X(M, R), typeof R == "object" && R !== null && typeof R.then == "function" && R.then(Gl, el);
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
var j0;
function Di() {
  return j0 || (j0 = 1, gi.exports = Bv()), gi.exports;
}
var Hl = Di();
const wn = /* @__PURE__ */ P0(Hl), Ui = /* @__PURE__ */ new Map(), Wn = /* @__PURE__ */ new Set();
let Oi = !1, Ni = 0;
const Mi = /* @__PURE__ */ new Set();
let ly = "", ty = "";
function qv(o) {
  ly = o;
}
function Yv(o) {
  ty = o;
}
function ay() {
  for (const o of Mi)
    o();
}
function jv(o) {
  return Mi.add(o), () => Mi.delete(o);
}
function Gv() {
  return Ni;
}
function Xv(o) {
  Wn.add(o), Oi || (Oi = !0, queueMicrotask(Qv));
}
async function Qv() {
  if (Oi = !1, Wn.size === 0)
    return;
  const o = Array.from(Wn);
  Wn.clear();
  try {
    const g = ly + "react-api/i18n?keys=" + encodeURIComponent(o.join(",")) + "&windowName=" + encodeURIComponent(ty), z = await fetch(g);
    if (!z.ok) {
      console.error("[TLReact] i18n fetch failed:", z.status);
      return;
    }
    const m = await z.json();
    for (const [D, G] of Object.entries(m))
      Ui.set(D, G);
    Ni++, ay();
  } catch (g) {
    console.error("[TLReact] i18n fetch error:", g);
  }
}
function mh(o) {
  wn.useSyncExternalStore(jv, Gv);
  const g = {};
  for (const [z, m] of Object.entries(o)) {
    const D = Ui.get(z);
    D !== void 0 ? g[z] = D : (g[z] = m, Xv(z));
  }
  return g;
}
function uy() {
  Ui.clear(), Ni++, ay();
}
const La = /* @__PURE__ */ new Map();
let Ae = null;
function Ri() {
  return document.body.dataset.windowName ?? "";
}
function In() {
  return document.body.dataset.contextPath ?? "";
}
function Zv(o) {
  const g = [];
  return o.width > 0 && g.push(`width=${o.width}`), o.height > 0 && g.push(`height=${o.height}`), g.push(`resizable=${o.resizable ? "yes" : "no"}`), g.join(",");
}
function Lv() {
  Ae === null && (Ae = setInterval(() => {
    for (const [o, g] of La)
      g.closed && (La.delete(o), xv(o));
    La.size === 0 && Ae !== null && (clearInterval(Ae), Ae = null);
  }, 2e3));
}
function xv(o) {
  const g = In(), z = Ri();
  fetch(`${g}/react-api/command`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      controlId: "",
      command: "windowClosed",
      windowName: z,
      arguments: { windowId: o }
    })
  }).catch(() => {
  });
}
function Vv(o) {
  const z = `${In()}/view/${o.windowId}/`, m = window.open(z, o.windowId, Zv(o));
  m ? (La.set(o.windowId, m), Lv()) : wv(o.windowId);
}
function Kv(o) {
  const g = La.get(o.windowId);
  g && (g.close(), La.delete(o.windowId));
}
function Jv(o) {
  const g = La.get(o.windowId);
  g && !g.closed && g.focus();
}
function wv(o) {
  const g = In(), z = Ri();
  fetch(`${g}/react-api/command`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      controlId: "",
      command: "windowBlocked",
      windowName: z,
      arguments: { windowId: o }
    })
  }).catch(() => {
  });
}
function Wv() {
  window.addEventListener("beforeunload", () => {
    const o = In(), g = Ri();
    if (!g) return;
    const z = JSON.stringify({
      controlId: "",
      command: "windowClosed",
      windowName: g,
      arguments: { windowId: g }
    }), m = new Blob([z], { type: "application/json" });
    navigator.sendBeacon(`${o}/react-api/command`, m);
  });
}
const Me = /* @__PURE__ */ new Map();
let _e = null, G0 = null, $n = 0, bi = null;
const $v = 45e3, Fv = 15e3;
function X0(o) {
  G0 = o, bi && clearInterval(bi), Q0(o), bi = setInterval(() => {
    $n > 0 && Date.now() - $n > $v && (console.warn("[TLReact] No heartbeat received, reconnecting SSE."), Q0(G0));
  }, Fv);
}
function Q0(o) {
  _e && _e.close(), _e = new EventSource(o), $n = Date.now(), uy(), _e.onmessage = (g) => {
    $n = Date.now();
    try {
      const z = JSON.parse(g.data);
      kv(z);
    } catch (z) {
      console.error("[TLReact] Failed to parse SSE event:", z);
    }
  }, _e.onerror = () => {
    console.warn("[TLReact] SSE connection error, will reconnect automatically.");
  };
}
function Ci(o, g) {
  let z = Me.get(o);
  z || (z = /* @__PURE__ */ new Set(), Me.set(o, z)), z.add(g);
}
function ey(o, g) {
  const z = Me.get(o);
  z && (z.delete(g), z.size === 0 && Me.delete(o));
}
function ny(o, g) {
  const z = Me.get(o);
  if (z)
    for (const m of z)
      m(g);
}
function kv(o) {
  if (!Array.isArray(o) || o.length < 2) {
    console.warn("[TLReact] Unexpected SSE event format:", o);
    return;
  }
  const g = o[0], z = o[1];
  switch (g !== "Heartbeat" && console.log("[SSE] dispatch", g, z), g) {
    case "Heartbeat":
      break;
    case "StateEvent":
      Iv(z);
      break;
    case "PatchEvent":
      Pv(z);
      break;
    case "ContentReplacement":
      Ea.contentReplacement(z);
      break;
    case "ElementReplacement":
      Ea.elementReplacement(z);
      break;
    case "PropertyUpdate":
      Ea.propertyUpdate(z);
      break;
    case "CssClassUpdate":
      Ea.cssClassUpdate(z);
      break;
    case "FragmentInsertion":
      Ea.fragmentInsertion(z);
      break;
    case "RangeReplacement":
      Ea.rangeReplacement(z);
      break;
    case "JSSnipplet":
      Ea.jsSnipplet(z);
      break;
    case "FunctionCall":
      Ea.functionCall(z);
      break;
    case "I18NCacheInvalidation":
      uy();
      break;
    case "WindowOpenEvent":
      Vv(z);
      break;
    case "WindowCloseEvent":
      Kv(z);
      break;
    case "WindowFocusEvent":
      Jv(z);
      break;
    default:
      console.warn("[TLReact] Unknown SSE event type:", g);
  }
}
function Iv(o) {
  const g = JSON.parse(o.state);
  ny(o.controlId, g);
}
function Pv(o) {
  const g = JSON.parse(o.patch);
  ny(o.controlId, g);
}
const Ea = {
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
      for (const z of o.properties)
        g.setAttribute(z.name, z.value);
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
    const g = document.getElementById(o.startId), z = document.getElementById(o.stopId);
    if (g && z && g.parentNode) {
      const m = g.parentNode, D = document.createRange();
      D.setStartBefore(g), D.setEndAfter(z), D.deleteContents();
      const G = document.createElement("template");
      G.innerHTML = o.html, m.insertBefore(G.content, D.startContainer.childNodes[D.startOffset] || null);
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
      const g = JSON.parse(o.arguments), z = o.functionRef ? window[o.functionRef] : window, m = z == null ? void 0 : z[o.functionName];
      typeof m == "function" ? m.apply(z, g) : console.warn("[TLReact] Function not found:", o.functionRef + "." + o.functionName);
    } catch (g) {
      console.error("[TLReact] Error executing function call:", g);
    }
  }
};
var Ti = { exports: {} }, pe = {}, Ei = { exports: {} }, zi = {};
/**
 * @license React
 * scheduler.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var Z0;
function lh() {
  return Z0 || (Z0 = 1, (function(o) {
    function g(T, O) {
      var Y = T.length;
      T.push(O);
      l: for (; 0 < Y; ) {
        var el = Y - 1 >>> 1, il = T[el];
        if (0 < D(il, O))
          T[el] = O, T[Y] = il, Y = el;
        else break l;
      }
    }
    function z(T) {
      return T.length === 0 ? null : T[0];
    }
    function m(T) {
      if (T.length === 0) return null;
      var O = T[0], Y = T.pop();
      if (Y !== O) {
        T[0] = Y;
        l: for (var el = 0, il = T.length, d = il >>> 1; el < d; ) {
          var _ = 2 * (el + 1) - 1, M = T[_], R = _ + 1, X = T[R];
          if (0 > D(M, Y))
            R < il && 0 > D(X, M) ? (T[el] = X, T[R] = Y, el = R) : (T[el] = M, T[_] = Y, el = _);
          else if (R < il && 0 > D(X, Y))
            T[el] = X, T[R] = Y, el = R;
          else break l;
        }
      }
      return O;
    }
    function D(T, O) {
      var Y = T.sortIndex - O.sortIndex;
      return Y !== 0 ? Y : T.id - O.id;
    }
    if (o.unstable_now = void 0, typeof performance == "object" && typeof performance.now == "function") {
      var G = performance;
      o.unstable_now = function() {
        return G.now();
      };
    } else {
      var K = Date, ml = K.now();
      o.unstable_now = function() {
        return K.now() - ml;
      };
    }
    var N = [], p = [], F = 1, q = null, ll = 3, Wl = !1, jl = !1, Bl = !1, Dt = !1, $l = typeof setTimeout == "function" ? setTimeout : null, $t = typeof clearTimeout == "function" ? clearTimeout : null, Rl = typeof setImmediate < "u" ? setImmediate : null;
    function ct(T) {
      for (var O = z(p); O !== null; ) {
        if (O.callback === null) m(p);
        else if (O.startTime <= T)
          m(p), O.sortIndex = O.expirationTime, g(N, O);
        else break;
        O = z(p);
      }
    }
    function Et(T) {
      if (Bl = !1, ct(T), !jl)
        if (z(N) !== null)
          jl = !0, Gl || (Gl = !0, Ql());
        else {
          var O = z(p);
          O !== null && rt(Et, O.startTime - T);
        }
    }
    var Gl = !1, W = -1, Xl = 5, zt = -1;
    function Va() {
      return Dt ? !0 : !(o.unstable_now() - zt < Xl);
    }
    function At() {
      if (Dt = !1, Gl) {
        var T = o.unstable_now();
        zt = T;
        var O = !0;
        try {
          l: {
            jl = !1, Bl && (Bl = !1, $t(W), W = -1), Wl = !0;
            var Y = ll;
            try {
              t: {
                for (ct(T), q = z(N); q !== null && !(q.expirationTime > T && Va()); ) {
                  var el = q.callback;
                  if (typeof el == "function") {
                    q.callback = null, ll = q.priorityLevel;
                    var il = el(
                      q.expirationTime <= T
                    );
                    if (T = o.unstable_now(), typeof il == "function") {
                      q.callback = il, ct(T), O = !0;
                      break t;
                    }
                    q === z(N) && m(N), ct(T);
                  } else m(N);
                  q = z(N);
                }
                if (q !== null) O = !0;
                else {
                  var d = z(p);
                  d !== null && rt(
                    Et,
                    d.startTime - T
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
    function rt(T, O) {
      W = $l(function() {
        T(o.unstable_now());
      }, O);
    }
    o.unstable_IdlePriority = 5, o.unstable_ImmediatePriority = 1, o.unstable_LowPriority = 4, o.unstable_NormalPriority = 3, o.unstable_Profiling = null, o.unstable_UserBlockingPriority = 2, o.unstable_cancelCallback = function(T) {
      T.callback = null;
    }, o.unstable_forceFrameRate = function(T) {
      0 > T || 125 < T ? console.error(
        "forceFrameRate takes a positive int between 0 and 125, forcing frame rates higher than 125 fps is not supported"
      ) : Xl = 0 < T ? Math.floor(1e3 / T) : 5;
    }, o.unstable_getCurrentPriorityLevel = function() {
      return ll;
    }, o.unstable_next = function(T) {
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
        return T();
      } finally {
        ll = Y;
      }
    }, o.unstable_requestPaint = function() {
      Dt = !0;
    }, o.unstable_runWithPriority = function(T, O) {
      switch (T) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
          break;
        default:
          T = 3;
      }
      var Y = ll;
      ll = T;
      try {
        return O();
      } finally {
        ll = Y;
      }
    }, o.unstable_scheduleCallback = function(T, O, Y) {
      var el = o.unstable_now();
      switch (typeof Y == "object" && Y !== null ? (Y = Y.delay, Y = typeof Y == "number" && 0 < Y ? el + Y : el) : Y = el, T) {
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
      return il = Y + il, T = {
        id: F++,
        callback: O,
        priorityLevel: T,
        startTime: Y,
        expirationTime: il,
        sortIndex: -1
      }, Y > el ? (T.sortIndex = Y, g(p, T), z(N) === null && T === z(p) && (Bl ? ($t(W), W = -1) : Bl = !0, rt(Et, Y - el))) : (T.sortIndex = il, g(N, T), jl || Wl || (jl = !0, Gl || (Gl = !0, Ql()))), T;
    }, o.unstable_shouldYield = Va, o.unstable_wrapCallback = function(T) {
      var O = ll;
      return function() {
        var Y = ll;
        ll = O;
        try {
          return T.apply(this, arguments);
        } finally {
          ll = Y;
        }
      };
    };
  })(zi)), zi;
}
var L0;
function th() {
  return L0 || (L0 = 1, Ei.exports = lh()), Ei.exports;
}
var Ai = { exports: {} }, Cl = {};
/**
 * @license React
 * react-dom.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var x0;
function ah() {
  if (x0) return Cl;
  x0 = 1;
  var o = Di();
  function g(N) {
    var p = "https://react.dev/errors/" + N;
    if (1 < arguments.length) {
      p += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var F = 2; F < arguments.length; F++)
        p += "&args[]=" + encodeURIComponent(arguments[F]);
    }
    return "Minified React error #" + N + "; visit " + p + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function z() {
  }
  var m = {
    d: {
      f: z,
      r: function() {
        throw Error(g(522));
      },
      D: z,
      C: z,
      L: z,
      m: z,
      X: z,
      S: z,
      M: z
    },
    p: 0,
    findDOMNode: null
  }, D = Symbol.for("react.portal");
  function G(N, p, F) {
    var q = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: D,
      key: q == null ? null : "" + q,
      children: N,
      containerInfo: p,
      implementation: F
    };
  }
  var K = o.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function ml(N, p) {
    if (N === "font") return "";
    if (typeof p == "string")
      return p === "use-credentials" ? p : "";
  }
  return Cl.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = m, Cl.createPortal = function(N, p) {
    var F = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!p || p.nodeType !== 1 && p.nodeType !== 9 && p.nodeType !== 11)
      throw Error(g(299));
    return G(N, p, null, F);
  }, Cl.flushSync = function(N) {
    var p = K.T, F = m.p;
    try {
      if (K.T = null, m.p = 2, N) return N();
    } finally {
      K.T = p, m.p = F, m.d.f();
    }
  }, Cl.preconnect = function(N, p) {
    typeof N == "string" && (p ? (p = p.crossOrigin, p = typeof p == "string" ? p === "use-credentials" ? p : "" : void 0) : p = null, m.d.C(N, p));
  }, Cl.prefetchDNS = function(N) {
    typeof N == "string" && m.d.D(N);
  }, Cl.preinit = function(N, p) {
    if (typeof N == "string" && p && typeof p.as == "string") {
      var F = p.as, q = ml(F, p.crossOrigin), ll = typeof p.integrity == "string" ? p.integrity : void 0, Wl = typeof p.fetchPriority == "string" ? p.fetchPriority : void 0;
      F === "style" ? m.d.S(
        N,
        typeof p.precedence == "string" ? p.precedence : void 0,
        {
          crossOrigin: q,
          integrity: ll,
          fetchPriority: Wl
        }
      ) : F === "script" && m.d.X(N, {
        crossOrigin: q,
        integrity: ll,
        fetchPriority: Wl,
        nonce: typeof p.nonce == "string" ? p.nonce : void 0
      });
    }
  }, Cl.preinitModule = function(N, p) {
    if (typeof N == "string")
      if (typeof p == "object" && p !== null) {
        if (p.as == null || p.as === "script") {
          var F = ml(
            p.as,
            p.crossOrigin
          );
          m.d.M(N, {
            crossOrigin: F,
            integrity: typeof p.integrity == "string" ? p.integrity : void 0,
            nonce: typeof p.nonce == "string" ? p.nonce : void 0
          });
        }
      } else p == null && m.d.M(N);
  }, Cl.preload = function(N, p) {
    if (typeof N == "string" && typeof p == "object" && p !== null && typeof p.as == "string") {
      var F = p.as, q = ml(F, p.crossOrigin);
      m.d.L(N, F, {
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
  }, Cl.preloadModule = function(N, p) {
    if (typeof N == "string")
      if (p) {
        var F = ml(p.as, p.crossOrigin);
        m.d.m(N, {
          as: typeof p.as == "string" && p.as !== "script" ? p.as : void 0,
          crossOrigin: F,
          integrity: typeof p.integrity == "string" ? p.integrity : void 0
        });
      } else m.d.m(N);
  }, Cl.requestFormReset = function(N) {
    m.d.r(N);
  }, Cl.unstable_batchedUpdates = function(N, p) {
    return N(p);
  }, Cl.useFormState = function(N, p, F) {
    return K.H.useFormState(N, p, F);
  }, Cl.useFormStatus = function() {
    return K.H.useHostTransitionStatus();
  }, Cl.version = "19.2.4", Cl;
}
var V0;
function cy() {
  if (V0) return Ai.exports;
  V0 = 1;
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
var K0;
function uh() {
  if (K0) return pe;
  K0 = 1;
  var o = th(), g = Di(), z = cy();
  function m(l) {
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
  function G(l) {
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
  function ml(l) {
    if (l.tag === 31) {
      var t = l.memoizedState;
      if (t === null && (l = l.alternate, l !== null && (t = l.memoizedState)), t !== null) return t.dehydrated;
    }
    return null;
  }
  function N(l) {
    if (G(l) !== l)
      throw Error(m(188));
  }
  function p(l) {
    var t = l.alternate;
    if (!t) {
      if (t = G(l), t === null) throw Error(m(188));
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
          if (n === a) return N(e), l;
          if (n === u) return N(e), t;
          n = n.sibling;
        }
        throw Error(m(188));
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
          if (!c) throw Error(m(189));
        }
      }
      if (a.alternate !== u) throw Error(m(190));
    }
    if (a.tag !== 3) throw Error(m(188));
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
  var q = Object.assign, ll = Symbol.for("react.element"), Wl = Symbol.for("react.transitional.element"), jl = Symbol.for("react.portal"), Bl = Symbol.for("react.fragment"), Dt = Symbol.for("react.strict_mode"), $l = Symbol.for("react.profiler"), $t = Symbol.for("react.consumer"), Rl = Symbol.for("react.context"), ct = Symbol.for("react.forward_ref"), Et = Symbol.for("react.suspense"), Gl = Symbol.for("react.suspense_list"), W = Symbol.for("react.memo"), Xl = Symbol.for("react.lazy"), zt = Symbol.for("react.activity"), Va = Symbol.for("react.memo_cache_sentinel"), At = Symbol.iterator;
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
      case Bl:
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
        case $t:
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
  var rt = Array.isArray, T = g.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE, O = z.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE, Y = {
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
  var R = d(null), X = d(null), L = d(null), tl = d(null);
  function ql(l, t) {
    switch (M(L, t), M(X, l), M(R, null), t.nodeType) {
      case 9:
      case 11:
        l = (l = t.documentElement) && (l = l.namespaceURI) ? n0(l) : 0;
        break;
      default:
        if (l = t.tagName, t = t.namespaceURI)
          t = n0(t), l = c0(t, l);
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
    _(R), M(R, l);
  }
  function Sl() {
    _(R), _(X), _(L);
  }
  function Nu(l) {
    l.memoizedState !== null && M(tl, l);
    var t = R.current, a = c0(t, l.type);
    t !== a && (M(X, l), M(R, a));
  }
  function De(l) {
    X.current === l && (_(R), _(X)), tl.current === l && (_(tl), be._currentValue = Y);
  }
  var Pn, Bi;
  function Aa(l) {
    if (Pn === void 0)
      try {
        throw Error();
      } catch (a) {
        var t = a.stack.trim().match(/\n( *(at )?)/);
        Pn = t && t[1] || "", Bi = -1 < a.stack.indexOf(`
    at`) ? " (<anonymous>)" : -1 < a.stack.indexOf("@") ? "@unknown:0:0" : "";
      }
    return `
` + Pn + l + Bi;
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
  function dy(l, t) {
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
  function qi(l) {
    try {
      var t = "", a = null;
      do
        t += dy(l, a), a = l, l = l.return;
      while (l);
      return t;
    } catch (u) {
      return `
Error generating stack: ` + u.message + `
` + u.stack;
    }
  }
  var ac = Object.prototype.hasOwnProperty, uc = o.unstable_scheduleCallback, ec = o.unstable_cancelCallback, yy = o.unstable_shouldYield, my = o.unstable_requestPaint, Fl = o.unstable_now, vy = o.unstable_getCurrentPriorityLevel, Yi = o.unstable_ImmediatePriority, ji = o.unstable_UserBlockingPriority, Ue = o.unstable_NormalPriority, hy = o.unstable_LowPriority, Gi = o.unstable_IdlePriority, Sy = o.log, ry = o.unstable_setDisableYieldValue, Ru = null, kl = null;
  function Ft(l) {
    if (typeof Sy == "function" && ry(l), kl && typeof kl.setStrictMode == "function")
      try {
        kl.setStrictMode(Ru, l);
      } catch {
      }
  }
  var Il = Math.clz32 ? Math.clz32 : Ty, gy = Math.log, by = Math.LN2;
  function Ty(l) {
    return l >>>= 0, l === 0 ? 32 : 31 - (gy(l) / by | 0) | 0;
  }
  var Ne = 256, Re = 262144, Ce = 4194304;
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
  function He(l, t, a) {
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
  function Ey(l, t) {
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
  function Xi() {
    var l = Ce;
    return Ce <<= 1, (Ce & 62914560) === 0 && (Ce = 4194304), l;
  }
  function nc(l) {
    for (var t = [], a = 0; 31 > a; a++) t.push(l);
    return t;
  }
  function Hu(l, t) {
    l.pendingLanes |= t, t !== 268435456 && (l.suspendedLanes = 0, l.pingedLanes = 0, l.warmLanes = 0);
  }
  function zy(l, t, a, u, e, n) {
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
    u !== 0 && Qi(l, u, 0), n !== 0 && e === 0 && l.tag !== 0 && (l.suspendedLanes |= n & ~(c & ~t));
  }
  function Qi(l, t, a) {
    l.pendingLanes |= t, l.suspendedLanes &= ~t;
    var u = 31 - Il(t);
    l.entangledLanes |= t, l.entanglements[u] = l.entanglements[u] | 1073741824 | a & 261930;
  }
  function Zi(l, t) {
    var a = l.entangledLanes |= t;
    for (l = l.entanglements; a; ) {
      var u = 31 - Il(a), e = 1 << u;
      e & t | l[u] & t && (l[u] |= t), a &= ~e;
    }
  }
  function Li(l, t) {
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
  function xi() {
    var l = O.p;
    return l !== 0 ? l : (l = window.event, l === void 0 ? 32 : U0(l.type));
  }
  function Vi(l, t) {
    var a = O.p;
    try {
      return O.p = l, t();
    } finally {
      O.p = a;
    }
  }
  var kt = Math.random().toString(36).slice(2), Ol = "__reactFiber$" + kt, Zl = "__reactProps$" + kt, Ka = "__reactContainer$" + kt, ic = "__reactEvents$" + kt, Ay = "__reactListeners$" + kt, _y = "__reactHandles$" + kt, Ki = "__reactResources$" + kt, Bu = "__reactMarker$" + kt;
  function oc(l) {
    delete l[Ol], delete l[Zl], delete l[ic], delete l[Ay], delete l[_y];
  }
  function Ja(l) {
    var t = l[Ol];
    if (t) return t;
    for (var a = l.parentNode; a; ) {
      if (t = a[Ka] || a[Ol]) {
        if (a = t.alternate, t.child !== null || a !== null && a.child !== null)
          for (l = m0(l); l !== null; ) {
            if (a = l[Ol]) return a;
            l = m0(l);
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
  function qu(l) {
    var t = l.tag;
    if (t === 5 || t === 26 || t === 27 || t === 6) return l.stateNode;
    throw Error(m(33));
  }
  function Wa(l) {
    var t = l[Ki];
    return t || (t = l[Ki] = { hoistableStyles: /* @__PURE__ */ new Map(), hoistableScripts: /* @__PURE__ */ new Map() }), t;
  }
  function _l(l) {
    l[Bu] = !0;
  }
  var Ji = /* @__PURE__ */ new Set(), wi = {};
  function pa(l, t) {
    $a(l, t), $a(l + "Capture", t);
  }
  function $a(l, t) {
    for (wi[l] = t, l = 0; l < t.length; l++)
      Ji.add(t[l]);
  }
  var py = RegExp(
    "^[:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD][:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD\\-.0-9\\u00B7\\u0300-\\u036F\\u203F-\\u2040]*$"
  ), Wi = {}, $i = {};
  function Oy(l) {
    return ac.call($i, l) ? !0 : ac.call(Wi, l) ? !1 : py.test(l) ? $i[l] = !0 : (Wi[l] = !0, !1);
  }
  function Be(l, t, a) {
    if (Oy(t))
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
  function qe(l, t, a) {
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
  function Fi(l) {
    var t = l.type;
    return (l = l.nodeName) && l.toLowerCase() === "input" && (t === "checkbox" || t === "radio");
  }
  function My(l, t, a) {
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
      var t = Fi(l) ? "checked" : "value";
      l._valueTracker = My(
        l,
        t,
        "" + l[t]
      );
    }
  }
  function ki(l) {
    if (!l) return !1;
    var t = l._valueTracker;
    if (!t) return !0;
    var a = t.getValue(), u = "";
    return l && (u = Fi(l) ? l.checked ? "true" : "false" : l.value), l = u, l !== a ? (t.setValue(l), !0) : !1;
  }
  function Ye(l) {
    if (l = l || (typeof document < "u" ? document : void 0), typeof l > "u") return null;
    try {
      return l.activeElement || l.body;
    } catch {
      return l.body;
    }
  }
  var Dy = /[\n"\\]/g;
  function it(l) {
    return l.replace(
      Dy,
      function(t) {
        return "\\" + t.charCodeAt(0).toString(16) + " ";
      }
    );
  }
  function dc(l, t, a, u, e, n, c, f) {
    l.name = "", c != null && typeof c != "function" && typeof c != "symbol" && typeof c != "boolean" ? l.type = c : l.removeAttribute("type"), t != null ? c === "number" ? (t === 0 && l.value === "" || l.value != t) && (l.value = "" + ft(t)) : l.value !== "" + ft(t) && (l.value = "" + ft(t)) : c !== "submit" && c !== "reset" || l.removeAttribute("value"), t != null ? yc(l, c, ft(t)) : a != null ? yc(l, c, ft(a)) : u != null && l.removeAttribute("value"), e == null && n != null && (l.defaultChecked = !!n), e != null && (l.checked = e && typeof e != "function" && typeof e != "symbol"), f != null && typeof f != "function" && typeof f != "symbol" && typeof f != "boolean" ? l.name = "" + ft(f) : l.removeAttribute("name");
  }
  function Ii(l, t, a, u, e, n, c, f) {
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
    t === "number" && Ye(l.ownerDocument) === l || l.defaultValue === "" + a || (l.defaultValue = "" + a);
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
  function Pi(l, t, a) {
    if (t != null && (t = "" + ft(t), t !== l.value && (l.value = t), a == null)) {
      l.defaultValue !== t && (l.defaultValue = t);
      return;
    }
    l.defaultValue = a != null ? "" + ft(a) : "";
  }
  function lo(l, t, a, u) {
    if (t == null) {
      if (u != null) {
        if (a != null) throw Error(m(92));
        if (rt(u)) {
          if (1 < u.length) throw Error(m(93));
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
  var Uy = new Set(
    "animationIterationCount aspectRatio borderImageOutset borderImageSlice borderImageWidth boxFlex boxFlexGroup boxOrdinalGroup columnCount columns flex flexGrow flexPositive flexShrink flexNegative flexOrder gridArea gridRow gridRowEnd gridRowSpan gridRowStart gridColumn gridColumnEnd gridColumnSpan gridColumnStart fontWeight lineClamp lineHeight opacity order orphans scale tabSize widows zIndex zoom fillOpacity floodOpacity stopOpacity strokeDasharray strokeDashoffset strokeMiterlimit strokeOpacity strokeWidth MozAnimationIterationCount MozBoxFlex MozBoxFlexGroup MozLineClamp msAnimationIterationCount msFlex msZoom msFlexGrow msFlexNegative msFlexOrder msFlexPositive msFlexShrink msGridColumn msGridColumnSpan msGridRow msGridRowSpan WebkitAnimationIterationCount WebkitBoxFlex WebKitBoxFlexGroup WebkitBoxOrdinalGroup WebkitColumnCount WebkitColumns WebkitFlex WebkitFlexGrow WebkitFlexPositive WebkitFlexShrink WebkitLineClamp".split(
      " "
    )
  );
  function to(l, t, a) {
    var u = t.indexOf("--") === 0;
    a == null || typeof a == "boolean" || a === "" ? u ? l.setProperty(t, "") : t === "float" ? l.cssFloat = "" : l[t] = "" : u ? l.setProperty(t, a) : typeof a != "number" || a === 0 || Uy.has(t) ? t === "float" ? l.cssFloat = a : l[t] = ("" + a).trim() : l[t] = a + "px";
  }
  function ao(l, t, a) {
    if (t != null && typeof t != "object")
      throw Error(m(62));
    if (l = l.style, a != null) {
      for (var u in a)
        !a.hasOwnProperty(u) || t != null && t.hasOwnProperty(u) || (u.indexOf("--") === 0 ? l.setProperty(u, "") : u === "float" ? l.cssFloat = "" : l[u] = "");
      for (var e in t)
        u = t[e], t.hasOwnProperty(e) && a[e] !== u && to(l, e, u);
    } else
      for (var n in t)
        t.hasOwnProperty(n) && to(l, n, t[n]);
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
  var Ny = /* @__PURE__ */ new Map([
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
  ]), Ry = /^[\u0000-\u001F ]*j[\r\n\t]*a[\r\n\t]*v[\r\n\t]*a[\r\n\t]*s[\r\n\t]*c[\r\n\t]*r[\r\n\t]*i[\r\n\t]*p[\r\n\t]*t[\r\n\t]*:/i;
  function je(l) {
    return Ry.test("" + l) ? "javascript:throw new Error('React has blocked a javascript: URL as a security precaution.')" : l;
  }
  function Rt() {
  }
  var vc = null;
  function hc(l) {
    return l = l.target || l.srcElement || window, l.correspondingUseElement && (l = l.correspondingUseElement), l.nodeType === 3 ? l.parentNode : l;
  }
  var Ia = null, Pa = null;
  function uo(l) {
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
                if (!e) throw Error(m(90));
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
              u = a[t], u.form === l.form && ki(u);
          }
          break l;
        case "textarea":
          Pi(l, a.value, a.defaultValue);
          break l;
        case "select":
          t = a.value, t != null && Fa(l, !!a.multiple, t, !1);
      }
    }
  }
  var Sc = !1;
  function eo(l, t, a) {
    if (Sc) return l(t, a);
    Sc = !0;
    try {
      var u = l(t);
      return u;
    } finally {
      if (Sc = !1, (Ia !== null || Pa !== null) && (pn(), Ia && (t = Ia, l = Pa, Pa = Ia = null, uo(t), l)))
        for (t = 0; t < l.length; t++) uo(l[t]);
    }
  }
  function Yu(l, t) {
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
        m(231, t, typeof a)
      );
    return a;
  }
  var Ct = !(typeof window > "u" || typeof window.document > "u" || typeof window.document.createElement > "u"), rc = !1;
  if (Ct)
    try {
      var ju = {};
      Object.defineProperty(ju, "passive", {
        get: function() {
          rc = !0;
        }
      }), window.addEventListener("test", ju, ju), window.removeEventListener("test", ju, ju);
    } catch {
      rc = !1;
    }
  var It = null, gc = null, Ge = null;
  function no() {
    if (Ge) return Ge;
    var l, t = gc, a = t.length, u, e = "value" in It ? It.value : It.textContent, n = e.length;
    for (l = 0; l < a && t[l] === e[l]; l++) ;
    var c = a - l;
    for (u = 1; u <= c && t[a - u] === e[n - u]; u++) ;
    return Ge = e.slice(l, 1 < u ? 1 - u : void 0);
  }
  function Xe(l) {
    var t = l.keyCode;
    return "charCode" in l ? (l = l.charCode, l === 0 && t === 13 && (l = 13)) : l = t, l === 10 && (l = 13), 32 <= l || l === 13 ? l : 0;
  }
  function Qe() {
    return !0;
  }
  function co() {
    return !1;
  }
  function Ll(l) {
    function t(a, u, e, n, c) {
      this._reactName = a, this._targetInst = e, this.type = u, this.nativeEvent = n, this.target = c, this.currentTarget = null;
      for (var f in l)
        l.hasOwnProperty(f) && (a = l[f], this[f] = a ? a(n) : n[f]);
      return this.isDefaultPrevented = (n.defaultPrevented != null ? n.defaultPrevented : n.returnValue === !1) ? Qe : co, this.isPropagationStopped = co, this;
    }
    return q(t.prototype, {
      preventDefault: function() {
        this.defaultPrevented = !0;
        var a = this.nativeEvent;
        a && (a.preventDefault ? a.preventDefault() : typeof a.returnValue != "unknown" && (a.returnValue = !1), this.isDefaultPrevented = Qe);
      },
      stopPropagation: function() {
        var a = this.nativeEvent;
        a && (a.stopPropagation ? a.stopPropagation() : typeof a.cancelBubble != "unknown" && (a.cancelBubble = !0), this.isPropagationStopped = Qe);
      },
      persist: function() {
      },
      isPersistent: Qe
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
  }, Ze = Ll(Oa), Gu = q({}, Oa, { view: 0, detail: 0 }), Cy = Ll(Gu), bc, Tc, Xu, Le = q({}, Gu, {
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
      return "movementX" in l ? l.movementX : (l !== Xu && (Xu && l.type === "mousemove" ? (bc = l.screenX - Xu.screenX, Tc = l.screenY - Xu.screenY) : Tc = bc = 0, Xu = l), bc);
    },
    movementY: function(l) {
      return "movementY" in l ? l.movementY : Tc;
    }
  }), fo = Ll(Le), Hy = q({}, Le, { dataTransfer: 0 }), By = Ll(Hy), qy = q({}, Gu, { relatedTarget: 0 }), Ec = Ll(qy), Yy = q({}, Oa, {
    animationName: 0,
    elapsedTime: 0,
    pseudoElement: 0
  }), jy = Ll(Yy), Gy = q({}, Oa, {
    clipboardData: function(l) {
      return "clipboardData" in l ? l.clipboardData : window.clipboardData;
    }
  }), Xy = Ll(Gy), Qy = q({}, Oa, { data: 0 }), io = Ll(Qy), Zy = {
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
  }, Ly = {
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
  }, xy = {
    Alt: "altKey",
    Control: "ctrlKey",
    Meta: "metaKey",
    Shift: "shiftKey"
  };
  function Vy(l) {
    var t = this.nativeEvent;
    return t.getModifierState ? t.getModifierState(l) : (l = xy[l]) ? !!t[l] : !1;
  }
  function zc() {
    return Vy;
  }
  var Ky = q({}, Gu, {
    key: function(l) {
      if (l.key) {
        var t = Zy[l.key] || l.key;
        if (t !== "Unidentified") return t;
      }
      return l.type === "keypress" ? (l = Xe(l), l === 13 ? "Enter" : String.fromCharCode(l)) : l.type === "keydown" || l.type === "keyup" ? Ly[l.keyCode] || "Unidentified" : "";
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
      return l.type === "keypress" ? Xe(l) : 0;
    },
    keyCode: function(l) {
      return l.type === "keydown" || l.type === "keyup" ? l.keyCode : 0;
    },
    which: function(l) {
      return l.type === "keypress" ? Xe(l) : l.type === "keydown" || l.type === "keyup" ? l.keyCode : 0;
    }
  }), Jy = Ll(Ky), wy = q({}, Le, {
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
  }), oo = Ll(wy), Wy = q({}, Gu, {
    touches: 0,
    targetTouches: 0,
    changedTouches: 0,
    altKey: 0,
    metaKey: 0,
    ctrlKey: 0,
    shiftKey: 0,
    getModifierState: zc
  }), $y = Ll(Wy), Fy = q({}, Oa, {
    propertyName: 0,
    elapsedTime: 0,
    pseudoElement: 0
  }), ky = Ll(Fy), Iy = q({}, Le, {
    deltaX: function(l) {
      return "deltaX" in l ? l.deltaX : "wheelDeltaX" in l ? -l.wheelDeltaX : 0;
    },
    deltaY: function(l) {
      return "deltaY" in l ? l.deltaY : "wheelDeltaY" in l ? -l.wheelDeltaY : "wheelDelta" in l ? -l.wheelDelta : 0;
    },
    deltaZ: 0,
    deltaMode: 0
  }), Py = Ll(Iy), lm = q({}, Oa, {
    newState: 0,
    oldState: 0
  }), tm = Ll(lm), am = [9, 13, 27, 32], Ac = Ct && "CompositionEvent" in window, Qu = null;
  Ct && "documentMode" in document && (Qu = document.documentMode);
  var um = Ct && "TextEvent" in window && !Qu, so = Ct && (!Ac || Qu && 8 < Qu && 11 >= Qu), yo = " ", mo = !1;
  function vo(l, t) {
    switch (l) {
      case "keyup":
        return am.indexOf(t.keyCode) !== -1;
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
  function ho(l) {
    return l = l.detail, typeof l == "object" && "data" in l ? l.data : null;
  }
  var lu = !1;
  function em(l, t) {
    switch (l) {
      case "compositionend":
        return ho(t);
      case "keypress":
        return t.which !== 32 ? null : (mo = !0, yo);
      case "textInput":
        return l = t.data, l === yo && mo ? null : l;
      default:
        return null;
    }
  }
  function nm(l, t) {
    if (lu)
      return l === "compositionend" || !Ac && vo(l, t) ? (l = no(), Ge = gc = It = null, lu = !1, l) : null;
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
        return so && t.locale !== "ko" ? null : t.data;
      default:
        return null;
    }
  }
  var cm = {
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
  function So(l) {
    var t = l && l.nodeName && l.nodeName.toLowerCase();
    return t === "input" ? !!cm[l.type] : t === "textarea";
  }
  function ro(l, t, a, u) {
    Ia ? Pa ? Pa.push(u) : Pa = [u] : Ia = u, t = Cn(t, "onChange"), 0 < t.length && (a = new Ze(
      "onChange",
      "change",
      null,
      a,
      u
    ), l.push({ event: a, listeners: t }));
  }
  var Zu = null, Lu = null;
  function fm(l) {
    Pd(l, 0);
  }
  function xe(l) {
    var t = qu(l);
    if (ki(t)) return l;
  }
  function go(l, t) {
    if (l === "change") return t;
  }
  var bo = !1;
  if (Ct) {
    var _c;
    if (Ct) {
      var pc = "oninput" in document;
      if (!pc) {
        var To = document.createElement("div");
        To.setAttribute("oninput", "return;"), pc = typeof To.oninput == "function";
      }
      _c = pc;
    } else _c = !1;
    bo = _c && (!document.documentMode || 9 < document.documentMode);
  }
  function Eo() {
    Zu && (Zu.detachEvent("onpropertychange", zo), Lu = Zu = null);
  }
  function zo(l) {
    if (l.propertyName === "value" && xe(Lu)) {
      var t = [];
      ro(
        t,
        Lu,
        l,
        hc(l)
      ), eo(fm, t);
    }
  }
  function im(l, t, a) {
    l === "focusin" ? (Eo(), Zu = t, Lu = a, Zu.attachEvent("onpropertychange", zo)) : l === "focusout" && Eo();
  }
  function om(l) {
    if (l === "selectionchange" || l === "keyup" || l === "keydown")
      return xe(Lu);
  }
  function sm(l, t) {
    if (l === "click") return xe(t);
  }
  function dm(l, t) {
    if (l === "input" || l === "change")
      return xe(t);
  }
  function ym(l, t) {
    return l === t && (l !== 0 || 1 / l === 1 / t) || l !== l && t !== t;
  }
  var Pl = typeof Object.is == "function" ? Object.is : ym;
  function xu(l, t) {
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
  function Ao(l) {
    for (; l && l.firstChild; ) l = l.firstChild;
    return l;
  }
  function _o(l, t) {
    var a = Ao(l);
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
      a = Ao(a);
    }
  }
  function po(l, t) {
    return l && t ? l === t ? !0 : l && l.nodeType === 3 ? !1 : t && t.nodeType === 3 ? po(l, t.parentNode) : "contains" in l ? l.contains(t) : l.compareDocumentPosition ? !!(l.compareDocumentPosition(t) & 16) : !1 : !1;
  }
  function Oo(l) {
    l = l != null && l.ownerDocument != null && l.ownerDocument.defaultView != null ? l.ownerDocument.defaultView : window;
    for (var t = Ye(l.document); t instanceof l.HTMLIFrameElement; ) {
      try {
        var a = typeof t.contentWindow.location.href == "string";
      } catch {
        a = !1;
      }
      if (a) l = t.contentWindow;
      else break;
      t = Ye(l.document);
    }
    return t;
  }
  function Oc(l) {
    var t = l && l.nodeName && l.nodeName.toLowerCase();
    return t && (t === "input" && (l.type === "text" || l.type === "search" || l.type === "tel" || l.type === "url" || l.type === "password") || t === "textarea" || l.contentEditable === "true");
  }
  var mm = Ct && "documentMode" in document && 11 >= document.documentMode, tu = null, Mc = null, Vu = null, Dc = !1;
  function Mo(l, t, a) {
    var u = a.window === a ? a.document : a.nodeType === 9 ? a : a.ownerDocument;
    Dc || tu == null || tu !== Ye(u) || (u = tu, "selectionStart" in u && Oc(u) ? u = { start: u.selectionStart, end: u.selectionEnd } : (u = (u.ownerDocument && u.ownerDocument.defaultView || window).getSelection(), u = {
      anchorNode: u.anchorNode,
      anchorOffset: u.anchorOffset,
      focusNode: u.focusNode,
      focusOffset: u.focusOffset
    }), Vu && xu(Vu, u) || (Vu = u, u = Cn(Mc, "onSelect"), 0 < u.length && (t = new Ze(
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
  }, Uc = {}, Do = {};
  Ct && (Do = document.createElement("div").style, "AnimationEvent" in window || (delete au.animationend.animation, delete au.animationiteration.animation, delete au.animationstart.animation), "TransitionEvent" in window || delete au.transitionend.transition);
  function Da(l) {
    if (Uc[l]) return Uc[l];
    if (!au[l]) return l;
    var t = au[l], a;
    for (a in t)
      if (t.hasOwnProperty(a) && a in Do)
        return Uc[l] = t[a];
    return l;
  }
  var Uo = Da("animationend"), No = Da("animationiteration"), Ro = Da("animationstart"), vm = Da("transitionrun"), hm = Da("transitionstart"), Sm = Da("transitioncancel"), Co = Da("transitionend"), Ho = /* @__PURE__ */ new Map(), Nc = "abort auxClick beforeToggle cancel canPlay canPlayThrough click close contextMenu copy cut drag dragEnd dragEnter dragExit dragLeave dragOver dragStart drop durationChange emptied encrypted ended error gotPointerCapture input invalid keyDown keyPress keyUp load loadedData loadedMetadata loadStart lostPointerCapture mouseDown mouseMove mouseOut mouseOver mouseUp paste pause play playing pointerCancel pointerDown pointerMove pointerOut pointerOver pointerUp progress rateChange reset resize seeked seeking stalled submit suspend timeUpdate touchCancel touchEnd touchStart volumeChange scroll toggle touchMove waiting wheel".split(
    " "
  );
  Nc.push("scrollEnd");
  function gt(l, t) {
    Ho.set(l, t), pa(t, [l]);
  }
  var Ve = typeof reportError == "function" ? reportError : function(l) {
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
  function Ke() {
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
      n !== 0 && Bo(a, e, n);
    }
  }
  function Je(l, t, a, u) {
    ot[uu++] = l, ot[uu++] = t, ot[uu++] = a, ot[uu++] = u, Rc |= u, l.lanes |= u, l = l.alternate, l !== null && (l.lanes |= u);
  }
  function Cc(l, t, a, u) {
    return Je(l, t, a, u), we(l);
  }
  function Ua(l, t) {
    return Je(l, null, null, t), we(l);
  }
  function Bo(l, t, a) {
    l.lanes |= a;
    var u = l.alternate;
    u !== null && (u.lanes |= a);
    for (var e = !1, n = l.return; n !== null; )
      n.childLanes |= a, u = n.alternate, u !== null && (u.childLanes |= a), n.tag === 22 && (l = n.stateNode, l === null || l._visibility & 1 || (e = !0)), l = n, n = n.return;
    return l.tag === 3 ? (n = l.stateNode, e && t !== null && (e = 31 - Il(a), l = n.hiddenUpdates, u = l[e], u === null ? l[e] = [t] : u.push(t), t.lane = a | 536870912), n) : null;
  }
  function we(l) {
    if (50 < ye)
      throw ye = 0, Lf = null, Error(m(185));
    for (var t = l.return; t !== null; )
      l = t, t = l.return;
    return l.tag === 3 ? l.stateNode : null;
  }
  var eu = {};
  function rm(l, t, a, u) {
    this.tag = l, this.key = a, this.sibling = this.child = this.return = this.stateNode = this.type = this.elementType = null, this.index = 0, this.refCleanup = this.ref = null, this.pendingProps = t, this.dependencies = this.memoizedState = this.updateQueue = this.memoizedProps = null, this.mode = u, this.subtreeFlags = this.flags = 0, this.deletions = null, this.childLanes = this.lanes = 0, this.alternate = null;
  }
  function lt(l, t, a, u) {
    return new rm(l, t, a, u);
  }
  function Hc(l) {
    return l = l.prototype, !(!l || !l.isReactComponent);
  }
  function Ht(l, t) {
    var a = l.alternate;
    return a === null ? (a = lt(
      l.tag,
      t,
      l.key,
      l.mode
    ), a.elementType = l.elementType, a.type = l.type, a.stateNode = l.stateNode, a.alternate = l, l.alternate = a) : (a.pendingProps = t, a.type = l.type, a.flags = 0, a.subtreeFlags = 0, a.deletions = null), a.flags = l.flags & 65011712, a.childLanes = l.childLanes, a.lanes = l.lanes, a.child = l.child, a.memoizedProps = l.memoizedProps, a.memoizedState = l.memoizedState, a.updateQueue = l.updateQueue, t = l.dependencies, a.dependencies = t === null ? null : { lanes: t.lanes, firstContext: t.firstContext }, a.sibling = l.sibling, a.index = l.index, a.ref = l.ref, a.refCleanup = l.refCleanup, a;
  }
  function qo(l, t) {
    l.flags &= 65011714;
    var a = l.alternate;
    return a === null ? (l.childLanes = 0, l.lanes = t, l.child = null, l.subtreeFlags = 0, l.memoizedProps = null, l.memoizedState = null, l.updateQueue = null, l.dependencies = null, l.stateNode = null) : (l.childLanes = a.childLanes, l.lanes = a.lanes, l.child = a.child, l.subtreeFlags = 0, l.deletions = null, l.memoizedProps = a.memoizedProps, l.memoizedState = a.memoizedState, l.updateQueue = a.updateQueue, l.type = a.type, t = a.dependencies, l.dependencies = t === null ? null : {
      lanes: t.lanes,
      firstContext: t.firstContext
    }), l;
  }
  function We(l, t, a, u, e, n) {
    var c = 0;
    if (u = l, typeof l == "function") Hc(l) && (c = 1);
    else if (typeof l == "string")
      c = zv(
        l,
        a,
        R.current
      ) ? 26 : l === "html" || l === "head" || l === "body" ? 27 : 5;
    else
      l: switch (l) {
        case zt:
          return l = lt(31, a, t, e), l.elementType = zt, l.lanes = n, l;
        case Bl:
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
              case $t:
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
            m(130, l === null ? "null" : typeof l, "")
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
  function Yo(l) {
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
  var jo = /* @__PURE__ */ new WeakMap();
  function st(l, t) {
    if (typeof l == "object" && l !== null) {
      var a = jo.get(l);
      return a !== void 0 ? a : (t = {
        value: l,
        source: t,
        stack: qi(t)
      }, jo.set(l, t), t);
    }
    return {
      value: l,
      source: t,
      stack: qi(t)
    };
  }
  var nu = [], cu = 0, $e = null, Ku = 0, dt = [], yt = 0, Pt = null, _t = 1, pt = "";
  function Bt(l, t) {
    nu[cu++] = Ku, nu[cu++] = $e, $e = l, Ku = t;
  }
  function Go(l, t, a) {
    dt[yt++] = _t, dt[yt++] = pt, dt[yt++] = Pt, Pt = l;
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
    l.return !== null && (Bt(l, 1), Go(l, 1, 0));
  }
  function jc(l) {
    for (; l === $e; )
      $e = nu[--cu], nu[cu] = null, Ku = nu[--cu], nu[cu] = null;
    for (; l === Pt; )
      Pt = dt[--yt], dt[yt] = null, pt = dt[--yt], dt[yt] = null, _t = dt[--yt], dt[yt] = null;
  }
  function Xo(l, t) {
    dt[yt++] = _t, dt[yt++] = pt, dt[yt++] = Pt, _t = t.id, pt = t.overflow, Pt = l;
  }
  var Ml = null, sl = null, $ = !1, la = null, mt = !1, Gc = Error(m(519));
  function ta(l) {
    var t = Error(
      m(
        418,
        1 < arguments.length && arguments[1] !== void 0 && arguments[1] ? "text" : "HTML",
        ""
      )
    );
    throw Ju(st(t, l)), Gc;
  }
  function Qo(l) {
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
        for (a = 0; a < ve.length; a++)
          V(ve[a], t);
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
        V("invalid", t), Ii(
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
        V("invalid", t), lo(t, u.value, u.defaultValue, u.children);
    }
    a = u.children, typeof a != "string" && typeof a != "number" && typeof a != "bigint" || t.textContent === "" + a || u.suppressHydrationWarning === !0 || u0(t.textContent, a) ? (u.popover != null && (V("beforetoggle", t), V("toggle", t)), u.onScroll != null && V("scroll", t), u.onScrollEnd != null && V("scrollend", t), u.onClick != null && (t.onclick = Rt), t = !0) : t = !1, t || ta(l, !0);
  }
  function Zo(l) {
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
    if (!$) return Zo(l), $ = !0, !1;
    var t = l.tag, a;
    if ((a = t !== 3 && t !== 27) && ((a = t === 5) && (a = l.type, a = !(a !== "form" && a !== "button") || ui(l.type, l.memoizedProps)), a = !a), a && sl && ta(l), Zo(l), t === 13) {
      if (l = l.memoizedState, l = l !== null ? l.dehydrated : null, !l) throw Error(m(317));
      sl = y0(l);
    } else if (t === 31) {
      if (l = l.memoizedState, l = l !== null ? l.dehydrated : null, !l) throw Error(m(317));
      sl = y0(l);
    } else
      t === 27 ? (t = sl, ha(l.type) ? (l = ii, ii = null, sl = l) : sl = t) : sl = Ml ? ht(l.stateNode.nextSibling) : null;
    return !0;
  }
  function Ra() {
    sl = Ml = null, $ = !1;
  }
  function Xc() {
    var l = la;
    return l !== null && (Jl === null ? Jl = l : Jl.push.apply(
      Jl,
      l
    ), la = null), l;
  }
  function Ju(l) {
    la === null ? la = [l] : la.push(l);
  }
  var Qc = d(null), Ca = null, qt = null;
  function aa(l, t, a) {
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
        if (c = e.return, c === null) throw Error(m(341));
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
        if (c === null) throw Error(m(387));
        if (c = c.memoizedProps, c !== null) {
          var f = e.type;
          Pl(e.pendingProps.value, c.value) || (l !== null ? l.push(f) : l = [f]);
        }
      } else if (e === tl.current) {
        if (c = e.alternate, c === null) throw Error(m(387));
        c.memoizedState.memoizedState !== e.memoizedState.memoizedState && (l !== null ? l.push(be) : l = [be]);
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
  function Fe(l) {
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
  function Ha(l) {
    Ca = l, qt = null, l = l.dependencies, l !== null && (l.firstContext = null);
  }
  function Dl(l) {
    return Lo(Ca, l);
  }
  function ke(l, t) {
    return Ca === null && Ha(l), Lo(l, t);
  }
  function Lo(l, t) {
    var a = t._currentValue;
    if (t = { context: t, memoizedValue: a, next: null }, qt === null) {
      if (l === null) throw Error(m(308));
      qt = t, l.dependencies = { lanes: 0, firstContext: t }, l.flags |= 524288;
    } else qt = qt.next = t;
    return a;
  }
  var gm = typeof AbortController < "u" ? AbortController : function() {
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
  }, bm = o.unstable_scheduleCallback, Tm = o.unstable_NormalPriority, bl = {
    $$typeof: Rl,
    Consumer: null,
    Provider: null,
    _currentValue: null,
    _currentValue2: null,
    _threadCount: 0
  };
  function xc() {
    return {
      controller: new gm(),
      data: /* @__PURE__ */ new Map(),
      refCount: 0
    };
  }
  function wu(l) {
    l.refCount--, l.refCount === 0 && bm(Tm, function() {
      l.controller.abort();
    });
  }
  var Wu = null, Vc = 0, ou = 0, su = null;
  function Em(l, t) {
    if (Wu === null) {
      var a = Wu = [];
      Vc = 0, ou = Wf(), su = {
        status: "pending",
        value: void 0,
        then: function(u) {
          a.push(u);
        }
      };
    }
    return Vc++, t.then(xo, xo), t;
  }
  function xo() {
    if (--Vc === 0 && Wu !== null) {
      su !== null && (su.status = "fulfilled");
      var l = Wu;
      Wu = null, ou = 0, su = null;
      for (var t = 0; t < l.length; t++) (0, l[t])();
    }
  }
  function zm(l, t) {
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
  var Vo = T.S;
  T.S = function(l, t) {
    Md = Fl(), typeof t == "object" && t !== null && typeof t.then == "function" && Em(l, t), Vo !== null && Vo(l, t);
  };
  var Ba = d(null);
  function Kc() {
    var l = Ba.current;
    return l !== null ? l : ol.pooledCache;
  }
  function Ie(l, t) {
    t === null ? M(Ba, Ba.current) : M(Ba, t.pool);
  }
  function Ko() {
    var l = Kc();
    return l === null ? null : { parent: bl._currentValue, pool: l };
  }
  var du = Error(m(460)), Jc = Error(m(474)), Pe = Error(m(542)), ln = { then: function() {
  } };
  function Jo(l) {
    return l = l.status, l === "fulfilled" || l === "rejected";
  }
  function wo(l, t, a) {
    switch (a = l[a], a === void 0 ? l.push(t) : a !== t && (t.then(Rt, Rt), t = a), t.status) {
      case "fulfilled":
        return t.value;
      case "rejected":
        throw l = t.reason, $o(l), l;
      default:
        if (typeof t.status == "string") t.then(Rt, Rt);
        else {
          if (l = ol, l !== null && 100 < l.shellSuspendCounter)
            throw Error(m(482));
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
            throw l = t.reason, $o(l), l;
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
  function Wo() {
    if (Ya === null) throw Error(m(459));
    var l = Ya;
    return Ya = null, l;
  }
  function $o(l) {
    if (l === du || l === Pe)
      throw Error(m(483));
  }
  var yu = null, $u = 0;
  function tn(l) {
    var t = $u;
    return $u += 1, yu === null && (yu = []), wo(yu, l, t);
  }
  function Fu(l, t) {
    t = t.props.ref, l.ref = t !== void 0 ? t : null;
  }
  function an(l, t) {
    throw t.$$typeof === ll ? Error(m(525)) : (l = Object.prototype.toString.call(t), Error(
      m(
        31,
        l === "[object Object]" ? "object with keys {" + Object.keys(t).join(", ") + "}" : l
      )
    ));
  }
  function Fo(l) {
    function t(y, s) {
      if (l) {
        var v = y.deletions;
        v === null ? (y.deletions = [s], y.flags |= 16) : v.push(s);
      }
    }
    function a(y, s) {
      if (!l) return null;
      for (; s !== null; )
        t(y, s), s = s.sibling;
      return null;
    }
    function u(y) {
      for (var s = /* @__PURE__ */ new Map(); y !== null; )
        y.key !== null ? s.set(y.key, y) : s.set(y.index, y), y = y.sibling;
      return s;
    }
    function e(y, s) {
      return y = Ht(y, s), y.index = 0, y.sibling = null, y;
    }
    function n(y, s, v) {
      return y.index = v, l ? (v = y.alternate, v !== null ? (v = v.index, v < s ? (y.flags |= 67108866, s) : v) : (y.flags |= 67108866, s)) : (y.flags |= 1048576, s);
    }
    function c(y) {
      return l && y.alternate === null && (y.flags |= 67108866), y;
    }
    function f(y, s, v, E) {
      return s === null || s.tag !== 6 ? (s = Bc(v, y.mode, E), s.return = y, s) : (s = e(s, v), s.return = y, s);
    }
    function i(y, s, v, E) {
      var H = v.type;
      return H === Bl ? b(
        y,
        s,
        v.props.children,
        E,
        v.key
      ) : s !== null && (s.elementType === H || typeof H == "object" && H !== null && H.$$typeof === Xl && qa(H) === s.type) ? (s = e(s, v.props), Fu(s, v), s.return = y, s) : (s = We(
        v.type,
        v.key,
        v.props,
        null,
        y.mode,
        E
      ), Fu(s, v), s.return = y, s);
    }
    function h(y, s, v, E) {
      return s === null || s.tag !== 4 || s.stateNode.containerInfo !== v.containerInfo || s.stateNode.implementation !== v.implementation ? (s = qc(v, y.mode, E), s.return = y, s) : (s = e(s, v.children || []), s.return = y, s);
    }
    function b(y, s, v, E, H) {
      return s === null || s.tag !== 7 ? (s = Na(
        v,
        y.mode,
        E,
        H
      ), s.return = y, s) : (s = e(s, v), s.return = y, s);
    }
    function A(y, s, v) {
      if (typeof s == "string" && s !== "" || typeof s == "number" || typeof s == "bigint")
        return s = Bc(
          "" + s,
          y.mode,
          v
        ), s.return = y, s;
      if (typeof s == "object" && s !== null) {
        switch (s.$$typeof) {
          case Wl:
            return v = We(
              s.type,
              s.key,
              s.props,
              null,
              y.mode,
              v
            ), Fu(v, s), v.return = y, v;
          case jl:
            return s = qc(
              s,
              y.mode,
              v
            ), s.return = y, s;
          case Xl:
            return s = qa(s), A(y, s, v);
        }
        if (rt(s) || Ql(s))
          return s = Na(
            s,
            y.mode,
            v,
            null
          ), s.return = y, s;
        if (typeof s.then == "function")
          return A(y, tn(s), v);
        if (s.$$typeof === Rl)
          return A(
            y,
            ke(y, s),
            v
          );
        an(y, s);
      }
      return null;
    }
    function S(y, s, v, E) {
      var H = s !== null ? s.key : null;
      if (typeof v == "string" && v !== "" || typeof v == "number" || typeof v == "bigint")
        return H !== null ? null : f(y, s, "" + v, E);
      if (typeof v == "object" && v !== null) {
        switch (v.$$typeof) {
          case Wl:
            return v.key === H ? i(y, s, v, E) : null;
          case jl:
            return v.key === H ? h(y, s, v, E) : null;
          case Xl:
            return v = qa(v), S(y, s, v, E);
        }
        if (rt(v) || Ql(v))
          return H !== null ? null : b(y, s, v, E, null);
        if (typeof v.then == "function")
          return S(
            y,
            s,
            tn(v),
            E
          );
        if (v.$$typeof === Rl)
          return S(
            y,
            s,
            ke(y, v),
            E
          );
        an(y, v);
      }
      return null;
    }
    function r(y, s, v, E, H) {
      if (typeof E == "string" && E !== "" || typeof E == "number" || typeof E == "bigint")
        return y = y.get(v) || null, f(s, y, "" + E, H);
      if (typeof E == "object" && E !== null) {
        switch (E.$$typeof) {
          case Wl:
            return y = y.get(
              E.key === null ? v : E.key
            ) || null, i(s, y, E, H);
          case jl:
            return y = y.get(
              E.key === null ? v : E.key
            ) || null, h(s, y, E, H);
          case Xl:
            return E = qa(E), r(
              y,
              s,
              v,
              E,
              H
            );
        }
        if (rt(E) || Ql(E))
          return y = y.get(v) || null, b(s, y, E, H, null);
        if (typeof E.then == "function")
          return r(
            y,
            s,
            v,
            tn(E),
            H
          );
        if (E.$$typeof === Rl)
          return r(
            y,
            s,
            v,
            ke(s, E),
            H
          );
        an(s, E);
      }
      return null;
    }
    function U(y, s, v, E) {
      for (var H = null, k = null, C = s, Z = s = 0, w = null; C !== null && Z < v.length; Z++) {
        C.index > Z ? (w = C, C = null) : w = C.sibling;
        var I = S(
          y,
          C,
          v[Z],
          E
        );
        if (I === null) {
          C === null && (C = w);
          break;
        }
        l && C && I.alternate === null && t(y, C), s = n(I, s, Z), k === null ? H = I : k.sibling = I, k = I, C = w;
      }
      if (Z === v.length)
        return a(y, C), $ && Bt(y, Z), H;
      if (C === null) {
        for (; Z < v.length; Z++)
          C = A(y, v[Z], E), C !== null && (s = n(
            C,
            s,
            Z
          ), k === null ? H = C : k.sibling = C, k = C);
        return $ && Bt(y, Z), H;
      }
      for (C = u(C); Z < v.length; Z++)
        w = r(
          C,
          y,
          Z,
          v[Z],
          E
        ), w !== null && (l && w.alternate !== null && C.delete(
          w.key === null ? Z : w.key
        ), s = n(
          w,
          s,
          Z
        ), k === null ? H = w : k.sibling = w, k = w);
      return l && C.forEach(function(Ta) {
        return t(y, Ta);
      }), $ && Bt(y, Z), H;
    }
    function B(y, s, v, E) {
      if (v == null) throw Error(m(151));
      for (var H = null, k = null, C = s, Z = s = 0, w = null, I = v.next(); C !== null && !I.done; Z++, I = v.next()) {
        C.index > Z ? (w = C, C = null) : w = C.sibling;
        var Ta = S(y, C, I.value, E);
        if (Ta === null) {
          C === null && (C = w);
          break;
        }
        l && C && Ta.alternate === null && t(y, C), s = n(Ta, s, Z), k === null ? H = Ta : k.sibling = Ta, k = Ta, C = w;
      }
      if (I.done)
        return a(y, C), $ && Bt(y, Z), H;
      if (C === null) {
        for (; !I.done; Z++, I = v.next())
          I = A(y, I.value, E), I !== null && (s = n(I, s, Z), k === null ? H = I : k.sibling = I, k = I);
        return $ && Bt(y, Z), H;
      }
      for (C = u(C); !I.done; Z++, I = v.next())
        I = r(C, y, Z, I.value, E), I !== null && (l && I.alternate !== null && C.delete(I.key === null ? Z : I.key), s = n(I, s, Z), k === null ? H = I : k.sibling = I, k = I);
      return l && C.forEach(function(Hv) {
        return t(y, Hv);
      }), $ && Bt(y, Z), H;
    }
    function fl(y, s, v, E) {
      if (typeof v == "object" && v !== null && v.type === Bl && v.key === null && (v = v.props.children), typeof v == "object" && v !== null) {
        switch (v.$$typeof) {
          case Wl:
            l: {
              for (var H = v.key; s !== null; ) {
                if (s.key === H) {
                  if (H = v.type, H === Bl) {
                    if (s.tag === 7) {
                      a(
                        y,
                        s.sibling
                      ), E = e(
                        s,
                        v.props.children
                      ), E.return = y, y = E;
                      break l;
                    }
                  } else if (s.elementType === H || typeof H == "object" && H !== null && H.$$typeof === Xl && qa(H) === s.type) {
                    a(
                      y,
                      s.sibling
                    ), E = e(s, v.props), Fu(E, v), E.return = y, y = E;
                    break l;
                  }
                  a(y, s);
                  break;
                } else t(y, s);
                s = s.sibling;
              }
              v.type === Bl ? (E = Na(
                v.props.children,
                y.mode,
                E,
                v.key
              ), E.return = y, y = E) : (E = We(
                v.type,
                v.key,
                v.props,
                null,
                y.mode,
                E
              ), Fu(E, v), E.return = y, y = E);
            }
            return c(y);
          case jl:
            l: {
              for (H = v.key; s !== null; ) {
                if (s.key === H)
                  if (s.tag === 4 && s.stateNode.containerInfo === v.containerInfo && s.stateNode.implementation === v.implementation) {
                    a(
                      y,
                      s.sibling
                    ), E = e(s, v.children || []), E.return = y, y = E;
                    break l;
                  } else {
                    a(y, s);
                    break;
                  }
                else t(y, s);
                s = s.sibling;
              }
              E = qc(v, y.mode, E), E.return = y, y = E;
            }
            return c(y);
          case Xl:
            return v = qa(v), fl(
              y,
              s,
              v,
              E
            );
        }
        if (rt(v))
          return U(
            y,
            s,
            v,
            E
          );
        if (Ql(v)) {
          if (H = Ql(v), typeof H != "function") throw Error(m(150));
          return v = H.call(v), B(
            y,
            s,
            v,
            E
          );
        }
        if (typeof v.then == "function")
          return fl(
            y,
            s,
            tn(v),
            E
          );
        if (v.$$typeof === Rl)
          return fl(
            y,
            s,
            ke(y, v),
            E
          );
        an(y, v);
      }
      return typeof v == "string" && v !== "" || typeof v == "number" || typeof v == "bigint" ? (v = "" + v, s !== null && s.tag === 6 ? (a(y, s.sibling), E = e(s, v), E.return = y, y = E) : (a(y, s), E = Bc(v, y.mode, E), E.return = y, y = E), c(y)) : a(y, s);
    }
    return function(y, s, v, E) {
      try {
        $u = 0;
        var H = fl(
          y,
          s,
          v,
          E
        );
        return yu = null, H;
      } catch (C) {
        if (C === du || C === Pe) throw C;
        var k = lt(29, C, null, y.mode);
        return k.lanes = E, k.return = y, k;
      } finally {
      }
    };
  }
  var ja = Fo(!0), ko = Fo(!1), ua = !1;
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
  function ea(l) {
    return { lane: l, tag: 0, payload: null, callback: null, next: null };
  }
  function na(l, t, a) {
    var u = l.updateQueue;
    if (u === null) return null;
    if (u = u.shared, (P & 2) !== 0) {
      var e = u.pending;
      return e === null ? t.next = t : (t.next = e.next, e.next = t), u.pending = t, t = we(l), Bo(l, null, a), t;
    }
    return Je(l, u, t, a), we(l);
  }
  function ku(l, t, a) {
    if (t = t.updateQueue, t !== null && (t = t.shared, (a & 4194048) !== 0)) {
      var u = t.lanes;
      u &= l.pendingLanes, a |= u, t.lanes = a, Zi(l, a);
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
  function Iu() {
    if (Fc) {
      var l = su;
      if (l !== null) throw l;
    }
  }
  function Pu(l, t, a, u) {
    Fc = !1;
    var e = l.updateQueue;
    ua = !1;
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
                ua = !0;
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
      b === null && (i = A), e.baseState = i, e.firstBaseUpdate = h, e.lastBaseUpdate = b, n === null && (e.shared.lanes = 0), sa |= c, l.lanes = c, l.memoizedState = A;
    }
  }
  function Io(l, t) {
    if (typeof l != "function")
      throw Error(m(191, l));
    l.call(t);
  }
  function Po(l, t) {
    var a = l.callbacks;
    if (a !== null)
      for (l.callbacks = null, l = 0; l < a.length; l++)
        Io(a[l], t);
  }
  var mu = d(null), un = d(0);
  function ls(l, t) {
    l = Kt, M(un, l), M(mu, t), Kt = l | t.baseLanes;
  }
  function kc() {
    M(un, Kt), M(mu, mu.current);
  }
  function Ic() {
    Kt = un.current, _(mu), _(un);
  }
  var tt = d(null), vt = null;
  function ca(l) {
    var t = l.alternate;
    M(rl, rl.current & 1), M(tt, l), vt === null && (t === null || mu.current !== null || t.memoizedState !== null) && (vt = l);
  }
  function Pc(l) {
    M(rl, rl.current), M(tt, l), vt === null && (vt = l);
  }
  function ts(l) {
    l.tag === 22 ? (M(rl, rl.current), M(tt, l), vt === null && (vt = l)) : fa();
  }
  function fa() {
    M(rl, rl.current), M(tt, tt.current);
  }
  function at(l) {
    _(tt), vt === l && (vt = null), _(rl);
  }
  var rl = d(0);
  function en(l) {
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
  var jt = 0, Q = null, nl = null, Tl = null, nn = !1, vu = !1, Ga = !1, cn = 0, le = 0, hu = null, Am = 0;
  function vl() {
    throw Error(m(321));
  }
  function lf(l, t) {
    if (t === null) return !1;
    for (var a = 0; a < t.length && a < l.length; a++)
      if (!Pl(l[a], t[a])) return !1;
    return !0;
  }
  function tf(l, t, a, u, e, n) {
    return jt = n, Q = t, t.memoizedState = null, t.updateQueue = null, t.lanes = 0, T.H = l === null || l.memoizedState === null ? Gs : rf, Ga = !1, n = a(u, e), Ga = !1, vu && (n = us(
      t,
      a,
      u,
      e
    )), as(l), n;
  }
  function as(l) {
    T.H = ue;
    var t = nl !== null && nl.next !== null;
    if (jt = 0, Tl = nl = Q = null, nn = !1, le = 0, hu = null, t) throw Error(m(300));
    l === null || El || (l = l.dependencies, l !== null && Fe(l) && (El = !0));
  }
  function us(l, t, a, u) {
    Q = l;
    var e = 0;
    do {
      if (vu && (hu = null), le = 0, vu = !1, 25 <= e) throw Error(m(301));
      if (e += 1, Tl = nl = null, l.updateQueue != null) {
        var n = l.updateQueue;
        n.lastEffect = null, n.events = null, n.stores = null, n.memoCache != null && (n.memoCache.index = 0);
      }
      T.H = Xs, n = t(a, u);
    } while (vu);
    return n;
  }
  function _m() {
    var l = T.H, t = l.useState()[0];
    return t = typeof t.then == "function" ? te(t) : t, l = l.useState()[0], (nl !== null ? nl.memoizedState : null) !== l && (Q.flags |= 1024), t;
  }
  function af() {
    var l = cn !== 0;
    return cn = 0, l;
  }
  function uf(l, t, a) {
    t.updateQueue = l.updateQueue, t.flags &= -2053, l.lanes &= ~a;
  }
  function ef(l) {
    if (nn) {
      for (l = l.memoizedState; l !== null; ) {
        var t = l.queue;
        t !== null && (t.pending = null), l = l.next;
      }
      nn = !1;
    }
    jt = 0, Tl = nl = Q = null, vu = !1, le = cn = 0, hu = null;
  }
  function Yl() {
    var l = {
      memoizedState: null,
      baseState: null,
      baseQueue: null,
      queue: null,
      next: null
    };
    return Tl === null ? Q.memoizedState = Tl = l : Tl = Tl.next = l, Tl;
  }
  function gl() {
    if (nl === null) {
      var l = Q.alternate;
      l = l !== null ? l.memoizedState : null;
    } else l = nl.next;
    var t = Tl === null ? Q.memoizedState : Tl.next;
    if (t !== null)
      Tl = t, nl = l;
    else {
      if (l === null)
        throw Q.alternate === null ? Error(m(467)) : Error(m(310));
      nl = l, l = {
        memoizedState: nl.memoizedState,
        baseState: nl.baseState,
        baseQueue: nl.baseQueue,
        queue: nl.queue,
        next: null
      }, Tl === null ? Q.memoizedState = Tl = l : Tl = Tl.next = l;
    }
    return Tl;
  }
  function fn() {
    return { lastEffect: null, events: null, stores: null, memoCache: null };
  }
  function te(l) {
    var t = le;
    return le += 1, hu === null && (hu = []), l = wo(hu, l, t), t = Q, (Tl === null ? t.memoizedState : Tl.next) === null && (t = t.alternate, T.H = t === null || t.memoizedState === null ? Gs : rf), l;
  }
  function on(l) {
    if (l !== null && typeof l == "object") {
      if (typeof l.then == "function") return te(l);
      if (l.$$typeof === Rl) return Dl(l);
    }
    throw Error(m(438, String(l)));
  }
  function nf(l) {
    var t = null, a = Q.updateQueue;
    if (a !== null && (t = a.memoCache), t == null) {
      var u = Q.alternate;
      u !== null && (u = u.updateQueue, u !== null && (u = u.memoCache, u != null && (t = {
        data: u.data.map(function(e) {
          return e.slice();
        }),
        index: 0
      })));
    }
    if (t == null && (t = { data: [], index: 0 }), a === null && (a = fn(), Q.updateQueue = a), a.memoCache = t, a = t.data[t.index], a === void 0)
      for (a = t.data[t.index] = Array(l), u = 0; u < l; u++)
        a[u] = Va;
    return t.index++, a;
  }
  function Gt(l, t) {
    return typeof t == "function" ? t(l) : t;
  }
  function sn(l) {
    var t = gl();
    return cf(t, nl, l);
  }
  function cf(l, t, a) {
    var u = l.queue;
    if (u === null) throw Error(m(311));
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
            }, i === null ? (f = i = A, c = n) : i = i.next = A, Q.lanes |= S, sa |= S;
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
          }, i === null ? (f = i = S, c = n) : i = i.next = S, Q.lanes |= A, sa |= A;
        h = h.next;
      } while (h !== null && h !== t);
      if (i === null ? c = n : i.next = f, !Pl(n, l.memoizedState) && (El = !0, b && (a = su, a !== null)))
        throw a;
      l.memoizedState = n, l.baseState = c, l.baseQueue = i, u.lastRenderedState = n;
    }
    return e === null && (u.lanes = 0), [l.memoizedState, u.dispatch];
  }
  function ff(l) {
    var t = gl(), a = t.queue;
    if (a === null) throw Error(m(311));
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
  function es(l, t, a) {
    var u = Q, e = gl(), n = $;
    if (n) {
      if (a === void 0) throw Error(m(407));
      a = a();
    } else a = t();
    var c = !Pl(
      (nl || e).memoizedState,
      a
    );
    if (c && (e.memoizedState = a, El = !0), e = e.queue, df(fs.bind(null, u, e, l), [
      l
    ]), e.getSnapshot !== t || c || Tl !== null && Tl.memoizedState.tag & 1) {
      if (u.flags |= 2048, Su(
        9,
        { destroy: void 0 },
        cs.bind(
          null,
          u,
          e,
          a,
          t
        ),
        null
      ), ol === null) throw Error(m(349));
      n || (jt & 127) !== 0 || ns(u, t, a);
    }
    return a;
  }
  function ns(l, t, a) {
    l.flags |= 16384, l = { getSnapshot: t, value: a }, t = Q.updateQueue, t === null ? (t = fn(), Q.updateQueue = t, t.stores = [l]) : (a = t.stores, a === null ? t.stores = [l] : a.push(l));
  }
  function cs(l, t, a, u) {
    t.value = a, t.getSnapshot = u, is(t) && os(l);
  }
  function fs(l, t, a) {
    return a(function() {
      is(t) && os(l);
    });
  }
  function is(l) {
    var t = l.getSnapshot;
    l = l.value;
    try {
      var a = t();
      return !Pl(l, a);
    } catch {
      return !0;
    }
  }
  function os(l) {
    var t = Ua(l, 2);
    t !== null && wl(t, l, 2);
  }
  function of(l) {
    var t = Yl();
    if (typeof l == "function") {
      var a = l;
      if (l = a(), Ga) {
        Ft(!0);
        try {
          a();
        } finally {
          Ft(!1);
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
  function ss(l, t, a, u) {
    return l.baseState = a, cf(
      l,
      nl,
      typeof u == "function" ? u : Gt
    );
  }
  function pm(l, t, a, u, e) {
    if (mn(l)) throw Error(m(485));
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
      T.T !== null ? a(!0) : n.isTransition = !1, u(n), a = t.pending, a === null ? (n.next = t.pending = n, ds(t, n)) : (n.next = a.next, t.pending = a.next = n);
    }
  }
  function ds(l, t) {
    var a = t.action, u = t.payload, e = l.state;
    if (t.isTransition) {
      var n = T.T, c = {};
      T.T = c;
      try {
        var f = a(e, u), i = T.S;
        i !== null && i(c, f), ys(l, t, f);
      } catch (h) {
        sf(l, t, h);
      } finally {
        n !== null && c.types !== null && (n.types = c.types), T.T = n;
      }
    } else
      try {
        n = a(e, u), ys(l, t, n);
      } catch (h) {
        sf(l, t, h);
      }
  }
  function ys(l, t, a) {
    a !== null && typeof a == "object" && typeof a.then == "function" ? a.then(
      function(u) {
        ms(l, t, u);
      },
      function(u) {
        return sf(l, t, u);
      }
    ) : ms(l, t, a);
  }
  function ms(l, t, a) {
    t.status = "fulfilled", t.value = a, vs(t), l.state = a, t = l.pending, t !== null && (a = t.next, a === t ? l.pending = null : (a = a.next, t.next = a, ds(l, a)));
  }
  function sf(l, t, a) {
    var u = l.pending;
    if (l.pending = null, u !== null) {
      u = u.next;
      do
        t.status = "rejected", t.reason = a, vs(t), t = t.next;
      while (t !== u);
    }
    l.action = null;
  }
  function vs(l) {
    l = l.listeners;
    for (var t = 0; t < l.length; t++) (0, l[t])();
  }
  function hs(l, t) {
    return t;
  }
  function Ss(l, t) {
    if ($) {
      var a = ol.formState;
      if (a !== null) {
        l: {
          var u = Q;
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
            ta(u);
          }
          u = !1;
        }
        u && (t = a[0]);
      }
    }
    return a = Yl(), a.memoizedState = a.baseState = t, u = {
      pending: null,
      lanes: 0,
      dispatch: null,
      lastRenderedReducer: hs,
      lastRenderedState: t
    }, a.queue = u, a = qs.bind(
      null,
      Q,
      u
    ), u.dispatch = a, u = of(!1), n = Sf.bind(
      null,
      Q,
      !1,
      u.queue
    ), u = Yl(), e = {
      state: t,
      dispatch: null,
      action: l,
      pending: null
    }, u.queue = e, a = pm.bind(
      null,
      Q,
      e,
      n,
      a
    ), e.dispatch = a, u.memoizedState = l, [t, a, !1];
  }
  function rs(l) {
    var t = gl();
    return gs(t, nl, l);
  }
  function gs(l, t, a) {
    if (t = cf(
      l,
      t,
      hs
    )[0], l = sn(Gt)[0], typeof t == "object" && t !== null && typeof t.then == "function")
      try {
        var u = te(t);
      } catch (c) {
        throw c === du ? Pe : c;
      }
    else u = t;
    t = gl();
    var e = t.queue, n = e.dispatch;
    return a !== t.memoizedState && (Q.flags |= 2048, Su(
      9,
      { destroy: void 0 },
      Om.bind(null, e, a),
      null
    )), [u, n, l];
  }
  function Om(l, t) {
    l.action = t;
  }
  function bs(l) {
    var t = gl(), a = nl;
    if (a !== null)
      return gs(t, a, l);
    gl(), t = t.memoizedState, a = gl();
    var u = a.queue.dispatch;
    return a.memoizedState = l, [t, u, !1];
  }
  function Su(l, t, a, u) {
    return l = { tag: l, create: a, deps: u, inst: t, next: null }, t = Q.updateQueue, t === null && (t = fn(), Q.updateQueue = t), a = t.lastEffect, a === null ? t.lastEffect = l.next = l : (u = a.next, a.next = l, l.next = u, t.lastEffect = l), l;
  }
  function Ts() {
    return gl().memoizedState;
  }
  function dn(l, t, a, u) {
    var e = Yl();
    Q.flags |= l, e.memoizedState = Su(
      1 | t,
      { destroy: void 0 },
      a,
      u === void 0 ? null : u
    );
  }
  function yn(l, t, a, u) {
    var e = gl();
    u = u === void 0 ? null : u;
    var n = e.memoizedState.inst;
    nl !== null && u !== null && lf(u, nl.memoizedState.deps) ? e.memoizedState = Su(t, n, a, u) : (Q.flags |= l, e.memoizedState = Su(
      1 | t,
      n,
      a,
      u
    ));
  }
  function Es(l, t) {
    dn(8390656, 8, l, t);
  }
  function df(l, t) {
    yn(2048, 8, l, t);
  }
  function Mm(l) {
    Q.flags |= 4;
    var t = Q.updateQueue;
    if (t === null)
      t = fn(), Q.updateQueue = t, t.events = [l];
    else {
      var a = t.events;
      a === null ? t.events = [l] : a.push(l);
    }
  }
  function zs(l) {
    var t = gl().memoizedState;
    return Mm({ ref: t, nextImpl: l }), function() {
      if ((P & 2) !== 0) throw Error(m(440));
      return t.impl.apply(void 0, arguments);
    };
  }
  function As(l, t) {
    return yn(4, 2, l, t);
  }
  function _s(l, t) {
    return yn(4, 4, l, t);
  }
  function ps(l, t) {
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
  function Os(l, t, a) {
    a = a != null ? a.concat([l]) : null, yn(4, 4, ps.bind(null, t, l), a);
  }
  function yf() {
  }
  function Ms(l, t) {
    var a = gl();
    t = t === void 0 ? null : t;
    var u = a.memoizedState;
    return t !== null && lf(t, u[1]) ? u[0] : (a.memoizedState = [l, t], l);
  }
  function Ds(l, t) {
    var a = gl();
    t = t === void 0 ? null : t;
    var u = a.memoizedState;
    if (t !== null && lf(t, u[1]))
      return u[0];
    if (u = l(), Ga) {
      Ft(!0);
      try {
        l();
      } finally {
        Ft(!1);
      }
    }
    return a.memoizedState = [u, t], u;
  }
  function mf(l, t, a) {
    return a === void 0 || (jt & 1073741824) !== 0 && (J & 261930) === 0 ? l.memoizedState = t : (l.memoizedState = a, l = Ud(), Q.lanes |= l, sa |= l, a);
  }
  function Us(l, t, a, u) {
    return Pl(a, t) ? a : mu.current !== null ? (l = mf(l, a, u), Pl(l, t) || (El = !0), l) : (jt & 42) === 0 || (jt & 1073741824) !== 0 && (J & 261930) === 0 ? (El = !0, l.memoizedState = a) : (l = Ud(), Q.lanes |= l, sa |= l, t);
  }
  function Ns(l, t, a, u, e) {
    var n = O.p;
    O.p = n !== 0 && 8 > n ? n : 8;
    var c = T.T, f = {};
    T.T = f, Sf(l, !1, t, a);
    try {
      var i = e(), h = T.S;
      if (h !== null && h(f, i), i !== null && typeof i == "object" && typeof i.then == "function") {
        var b = zm(
          i,
          u
        );
        ae(
          l,
          t,
          b,
          nt(l)
        );
      } else
        ae(
          l,
          t,
          u,
          nt(l)
        );
    } catch (A) {
      ae(
        l,
        t,
        { then: function() {
        }, status: "rejected", reason: A },
        nt()
      );
    } finally {
      O.p = n, c !== null && f.types !== null && (c.types = f.types), T.T = c;
    }
  }
  function Dm() {
  }
  function vf(l, t, a, u) {
    if (l.tag !== 5) throw Error(m(476));
    var e = Rs(l).queue;
    Ns(
      l,
      e,
      t,
      Y,
      a === null ? Dm : function() {
        return Cs(l), a(u);
      }
    );
  }
  function Rs(l) {
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
  function Cs(l) {
    var t = Rs(l);
    t.next === null && (t = l.alternate.memoizedState), ae(
      l,
      t.next.queue,
      {},
      nt()
    );
  }
  function hf() {
    return Dl(be);
  }
  function Hs() {
    return gl().memoizedState;
  }
  function Bs() {
    return gl().memoizedState;
  }
  function Um(l) {
    for (var t = l.return; t !== null; ) {
      switch (t.tag) {
        case 24:
        case 3:
          var a = nt();
          l = ea(a);
          var u = na(t, l, a);
          u !== null && (wl(u, t, a), ku(u, t, a)), t = { cache: xc() }, l.payload = t;
          return;
      }
      t = t.return;
    }
  }
  function Nm(l, t, a) {
    var u = nt();
    a = {
      lane: u,
      revertLane: 0,
      gesture: null,
      action: a,
      hasEagerState: !1,
      eagerState: null,
      next: null
    }, mn(l) ? Ys(t, a) : (a = Cc(l, t, a, u), a !== null && (wl(a, l, u), js(a, t, u)));
  }
  function qs(l, t, a) {
    var u = nt();
    ae(l, t, a, u);
  }
  function ae(l, t, a, u) {
    var e = {
      lane: u,
      revertLane: 0,
      gesture: null,
      action: a,
      hasEagerState: !1,
      eagerState: null,
      next: null
    };
    if (mn(l)) Ys(t, e);
    else {
      var n = l.alternate;
      if (l.lanes === 0 && (n === null || n.lanes === 0) && (n = t.lastRenderedReducer, n !== null))
        try {
          var c = t.lastRenderedState, f = n(c, a);
          if (e.hasEagerState = !0, e.eagerState = f, Pl(f, c))
            return Je(l, t, e, 0), ol === null && Ke(), !1;
        } catch {
        } finally {
        }
      if (a = Cc(l, t, e, u), a !== null)
        return wl(a, l, u), js(a, t, u), !0;
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
    }, mn(l)) {
      if (t) throw Error(m(479));
    } else
      t = Cc(
        l,
        a,
        u,
        2
      ), t !== null && wl(t, l, 2);
  }
  function mn(l) {
    var t = l.alternate;
    return l === Q || t !== null && t === Q;
  }
  function Ys(l, t) {
    vu = nn = !0;
    var a = l.pending;
    a === null ? t.next = t : (t.next = a.next, a.next = t), l.pending = t;
  }
  function js(l, t, a) {
    if ((a & 4194048) !== 0) {
      var u = t.lanes;
      u &= l.pendingLanes, a |= u, t.lanes = a, Zi(l, a);
    }
  }
  var ue = {
    readContext: Dl,
    use: on,
    useCallback: vl,
    useContext: vl,
    useEffect: vl,
    useImperativeHandle: vl,
    useLayoutEffect: vl,
    useInsertionEffect: vl,
    useMemo: vl,
    useReducer: vl,
    useRef: vl,
    useState: vl,
    useDebugValue: vl,
    useDeferredValue: vl,
    useTransition: vl,
    useSyncExternalStore: vl,
    useId: vl,
    useHostTransitionStatus: vl,
    useFormState: vl,
    useActionState: vl,
    useOptimistic: vl,
    useMemoCache: vl,
    useCacheRefresh: vl
  };
  ue.useEffectEvent = vl;
  var Gs = {
    readContext: Dl,
    use: on,
    useCallback: function(l, t) {
      return Yl().memoizedState = [
        l,
        t === void 0 ? null : t
      ], l;
    },
    useContext: Dl,
    useEffect: Es,
    useImperativeHandle: function(l, t, a) {
      a = a != null ? a.concat([l]) : null, dn(
        4194308,
        4,
        ps.bind(null, t, l),
        a
      );
    },
    useLayoutEffect: function(l, t) {
      return dn(4194308, 4, l, t);
    },
    useInsertionEffect: function(l, t) {
      dn(4, 2, l, t);
    },
    useMemo: function(l, t) {
      var a = Yl();
      t = t === void 0 ? null : t;
      var u = l();
      if (Ga) {
        Ft(!0);
        try {
          l();
        } finally {
          Ft(!1);
        }
      }
      return a.memoizedState = [u, t], u;
    },
    useReducer: function(l, t, a) {
      var u = Yl();
      if (a !== void 0) {
        var e = a(t);
        if (Ga) {
          Ft(!0);
          try {
            a(t);
          } finally {
            Ft(!1);
          }
        }
      } else e = t;
      return u.memoizedState = u.baseState = e, l = {
        pending: null,
        lanes: 0,
        dispatch: null,
        lastRenderedReducer: l,
        lastRenderedState: e
      }, u.queue = l, l = l.dispatch = Nm.bind(
        null,
        Q,
        l
      ), [u.memoizedState, l];
    },
    useRef: function(l) {
      var t = Yl();
      return l = { current: l }, t.memoizedState = l;
    },
    useState: function(l) {
      l = of(l);
      var t = l.queue, a = qs.bind(null, Q, t);
      return t.dispatch = a, [l.memoizedState, a];
    },
    useDebugValue: yf,
    useDeferredValue: function(l, t) {
      var a = Yl();
      return mf(a, l, t);
    },
    useTransition: function() {
      var l = of(!1);
      return l = Ns.bind(
        null,
        Q,
        l.queue,
        !0,
        !1
      ), Yl().memoizedState = l, [!1, l];
    },
    useSyncExternalStore: function(l, t, a) {
      var u = Q, e = Yl();
      if ($) {
        if (a === void 0)
          throw Error(m(407));
        a = a();
      } else {
        if (a = t(), ol === null)
          throw Error(m(349));
        (J & 127) !== 0 || ns(u, t, a);
      }
      e.memoizedState = a;
      var n = { value: a, getSnapshot: t };
      return e.queue = n, Es(fs.bind(null, u, n, l), [
        l
      ]), u.flags |= 2048, Su(
        9,
        { destroy: void 0 },
        cs.bind(
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
      var l = Yl(), t = ol.identifierPrefix;
      if ($) {
        var a = pt, u = _t;
        a = (u & ~(1 << 32 - Il(u) - 1)).toString(32) + a, t = "_" + t + "R_" + a, a = cn++, 0 < a && (t += "H" + a.toString(32)), t += "_";
      } else
        a = Am++, t = "_" + t + "r_" + a.toString(32) + "_";
      return l.memoizedState = t;
    },
    useHostTransitionStatus: hf,
    useFormState: Ss,
    useActionState: Ss,
    useOptimistic: function(l) {
      var t = Yl();
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
        Q,
        !0,
        a
      ), a.dispatch = t, [l, t];
    },
    useMemoCache: nf,
    useCacheRefresh: function() {
      return Yl().memoizedState = Um.bind(
        null,
        Q
      );
    },
    useEffectEvent: function(l) {
      var t = Yl(), a = { impl: l };
      return t.memoizedState = a, function() {
        if ((P & 2) !== 0)
          throw Error(m(440));
        return a.impl.apply(void 0, arguments);
      };
    }
  }, rf = {
    readContext: Dl,
    use: on,
    useCallback: Ms,
    useContext: Dl,
    useEffect: df,
    useImperativeHandle: Os,
    useInsertionEffect: As,
    useLayoutEffect: _s,
    useMemo: Ds,
    useReducer: sn,
    useRef: Ts,
    useState: function() {
      return sn(Gt);
    },
    useDebugValue: yf,
    useDeferredValue: function(l, t) {
      var a = gl();
      return Us(
        a,
        nl.memoizedState,
        l,
        t
      );
    },
    useTransition: function() {
      var l = sn(Gt)[0], t = gl().memoizedState;
      return [
        typeof l == "boolean" ? l : te(l),
        t
      ];
    },
    useSyncExternalStore: es,
    useId: Hs,
    useHostTransitionStatus: hf,
    useFormState: rs,
    useActionState: rs,
    useOptimistic: function(l, t) {
      var a = gl();
      return ss(a, nl, l, t);
    },
    useMemoCache: nf,
    useCacheRefresh: Bs
  };
  rf.useEffectEvent = zs;
  var Xs = {
    readContext: Dl,
    use: on,
    useCallback: Ms,
    useContext: Dl,
    useEffect: df,
    useImperativeHandle: Os,
    useInsertionEffect: As,
    useLayoutEffect: _s,
    useMemo: Ds,
    useReducer: ff,
    useRef: Ts,
    useState: function() {
      return ff(Gt);
    },
    useDebugValue: yf,
    useDeferredValue: function(l, t) {
      var a = gl();
      return nl === null ? mf(a, l, t) : Us(
        a,
        nl.memoizedState,
        l,
        t
      );
    },
    useTransition: function() {
      var l = ff(Gt)[0], t = gl().memoizedState;
      return [
        typeof l == "boolean" ? l : te(l),
        t
      ];
    },
    useSyncExternalStore: es,
    useId: Hs,
    useHostTransitionStatus: hf,
    useFormState: bs,
    useActionState: bs,
    useOptimistic: function(l, t) {
      var a = gl();
      return nl !== null ? ss(a, nl, l, t) : (a.baseState = l, [l, a.queue.dispatch]);
    },
    useMemoCache: nf,
    useCacheRefresh: Bs
  };
  Xs.useEffectEvent = zs;
  function gf(l, t, a, u) {
    t = l.memoizedState, a = a(u, t), a = a == null ? t : q({}, t, a), l.memoizedState = a, l.lanes === 0 && (l.updateQueue.baseState = a);
  }
  var bf = {
    enqueueSetState: function(l, t, a) {
      l = l._reactInternals;
      var u = nt(), e = ea(u);
      e.payload = t, a != null && (e.callback = a), t = na(l, e, u), t !== null && (wl(t, l, u), ku(t, l, u));
    },
    enqueueReplaceState: function(l, t, a) {
      l = l._reactInternals;
      var u = nt(), e = ea(u);
      e.tag = 1, e.payload = t, a != null && (e.callback = a), t = na(l, e, u), t !== null && (wl(t, l, u), ku(t, l, u));
    },
    enqueueForceUpdate: function(l, t) {
      l = l._reactInternals;
      var a = nt(), u = ea(a);
      u.tag = 2, t != null && (u.callback = t), t = na(l, u, a), t !== null && (wl(t, l, a), ku(t, l, a));
    }
  };
  function Qs(l, t, a, u, e, n, c) {
    return l = l.stateNode, typeof l.shouldComponentUpdate == "function" ? l.shouldComponentUpdate(u, n, c) : t.prototype && t.prototype.isPureReactComponent ? !xu(a, u) || !xu(e, n) : !0;
  }
  function Zs(l, t, a, u) {
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
  function Ls(l) {
    Ve(l);
  }
  function xs(l) {
    console.error(l);
  }
  function Vs(l) {
    Ve(l);
  }
  function vn(l, t) {
    try {
      var a = l.onUncaughtError;
      a(t.value, { componentStack: t.stack });
    } catch (u) {
      setTimeout(function() {
        throw u;
      });
    }
  }
  function Ks(l, t, a) {
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
    return a = ea(a), a.tag = 3, a.payload = { element: null }, a.callback = function() {
      vn(l, t);
    }, a;
  }
  function Js(l) {
    return l = ea(l), l.tag = 3, l;
  }
  function ws(l, t, a, u) {
    var e = a.type.getDerivedStateFromError;
    if (typeof e == "function") {
      var n = u.value;
      l.payload = function() {
        return e(n);
      }, l.callback = function() {
        Ks(t, a, u);
      };
    }
    var c = a.stateNode;
    c !== null && typeof c.componentDidCatch == "function" && (l.callback = function() {
      Ks(t, a, u), typeof e != "function" && (da === null ? da = /* @__PURE__ */ new Set([this]) : da.add(this));
      var f = u.stack;
      this.componentDidCatch(u.value, {
        componentStack: f !== null ? f : ""
      });
    });
  }
  function Rm(l, t, a, u, e) {
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
            return vt === null ? On() : a.alternate === null && hl === 0 && (hl = 3), a.flags &= -257, a.flags |= 65536, a.lanes = e, u === ln ? a.flags |= 16384 : (t = a.updateQueue, t === null ? a.updateQueue = /* @__PURE__ */ new Set([u]) : t.add(u), Kf(l, u, e)), !1;
          case 22:
            return a.flags |= 65536, u === ln ? a.flags |= 16384 : (t = a.updateQueue, t === null ? (t = {
              transitions: null,
              markerInstances: null,
              retryQueue: /* @__PURE__ */ new Set([u])
            }, a.updateQueue = t) : (a = t.retryQueue, a === null ? t.retryQueue = /* @__PURE__ */ new Set([u]) : a.add(u)), Kf(l, u, e)), !1;
        }
        throw Error(m(435, a.tag));
      }
      return Kf(l, u, e), On(), !1;
    }
    if ($)
      return t = tt.current, t !== null ? ((t.flags & 65536) === 0 && (t.flags |= 256), t.flags |= 65536, t.lanes = e, u !== Gc && (l = Error(m(422), { cause: u }), Ju(st(l, a)))) : (u !== Gc && (t = Error(m(423), {
        cause: u
      }), Ju(
        st(t, a)
      )), l = l.current.alternate, l.flags |= 65536, e &= -e, l.lanes |= e, u = st(u, a), e = Tf(
        l.stateNode,
        u,
        e
      ), $c(l, e), hl !== 4 && (hl = 2)), !1;
    var n = Error(m(520), { cause: u });
    if (n = st(n, a), de === null ? de = [n] : de.push(n), hl !== 4 && (hl = 2), t === null) return !0;
    u = st(u, a), a = t;
    do {
      switch (a.tag) {
        case 3:
          return a.flags |= 65536, l = e & -e, a.lanes |= l, l = Tf(a.stateNode, u, l), $c(a, l), !1;
        case 1:
          if (t = a.type, n = a.stateNode, (a.flags & 128) === 0 && (typeof t.getDerivedStateFromError == "function" || n !== null && typeof n.componentDidCatch == "function" && (da === null || !da.has(n))))
            return a.flags |= 65536, e &= -e, a.lanes |= e, e = Js(e), ws(
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
  var Ef = Error(m(461)), El = !1;
  function Ul(l, t, a, u) {
    t.child = l === null ? ko(t, null, a, u) : ja(
      t,
      l.child,
      a,
      u
    );
  }
  function Ws(l, t, a, u, e) {
    a = a.render;
    var n = t.ref;
    if ("ref" in u) {
      var c = {};
      for (var f in u)
        f !== "ref" && (c[f] = u[f]);
    } else c = u;
    return Ha(t), u = tf(
      l,
      t,
      a,
      c,
      n,
      e
    ), f = af(), l !== null && !El ? (uf(l, t, e), Xt(l, t, e)) : ($ && f && Yc(t), t.flags |= 1, Ul(l, t, u, e), t.child);
  }
  function $s(l, t, a, u, e) {
    if (l === null) {
      var n = a.type;
      return typeof n == "function" && !Hc(n) && n.defaultProps === void 0 && a.compare === null ? (t.tag = 15, t.type = n, Fs(
        l,
        t,
        n,
        u,
        e
      )) : (l = We(
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
      if (a = a.compare, a = a !== null ? a : xu, a(c, u) && l.ref === t.ref)
        return Xt(l, t, e);
    }
    return t.flags |= 1, l = Ht(n, u), l.ref = t.ref, l.return = t, t.child = l;
  }
  function Fs(l, t, a, u, e) {
    if (l !== null) {
      var n = l.memoizedProps;
      if (xu(n, u) && l.ref === t.ref)
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
  function ks(l, t, a, u) {
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
        return Is(
          l,
          t,
          n,
          a,
          u
        );
      }
      if ((a & 536870912) !== 0)
        t.memoizedState = { baseLanes: 0, cachePool: null }, l !== null && Ie(
          t,
          n !== null ? n.cachePool : null
        ), n !== null ? ls(t, n) : kc(), ts(t);
      else
        return u = t.lanes = 536870912, Is(
          l,
          t,
          n !== null ? n.baseLanes | a : a,
          a,
          u
        );
    } else
      n !== null ? (Ie(t, n.cachePool), ls(t, n), fa(), t.memoizedState = null) : (l !== null && Ie(t, null), kc(), fa());
    return Ul(l, t, e, a), t.child;
  }
  function ee(l, t) {
    return l !== null && l.tag === 22 || t.stateNode !== null || (t.stateNode = {
      _visibility: 1,
      _pendingMarkers: null,
      _retryCache: null,
      _transitions: null
    }), t.sibling;
  }
  function Is(l, t, a, u, e) {
    var n = Kc();
    return n = n === null ? null : { parent: bl._currentValue, pool: n }, t.memoizedState = {
      baseLanes: a,
      cachePool: n
    }, l !== null && Ie(t, null), kc(), ts(t), l !== null && iu(l, t, u, !0), t.childLanes = e, null;
  }
  function hn(l, t) {
    return t = rn(
      { mode: t.mode, children: t.children },
      l.mode
    ), t.ref = l.ref, l.child = t, t.return = l, t;
  }
  function Ps(l, t, a) {
    return ja(t, l.child, null, a), l = hn(t, t.pendingProps), l.flags |= 2, at(t), t.memoizedState = null, l;
  }
  function Cm(l, t, a) {
    var u = t.pendingProps, e = (t.flags & 128) !== 0;
    if (t.flags &= -129, l === null) {
      if ($) {
        if (u.mode === "hidden")
          return l = hn(t, u), t.lanes = 536870912, ee(null, l);
        if (Pc(t), (l = sl) ? (l = d0(
          l,
          mt
        ), l = l !== null && l.data === "&" ? l : null, l !== null && (t.memoizedState = {
          dehydrated: l,
          treeContext: Pt !== null ? { id: _t, overflow: pt } : null,
          retryLane: 536870912,
          hydrationErrors: null
        }, a = Yo(l), a.return = t, t.child = a, Ml = t, sl = null)) : l = null, l === null) throw ta(t);
        return t.lanes = 536870912, null;
      }
      return hn(t, u);
    }
    var n = l.memoizedState;
    if (n !== null) {
      var c = n.dehydrated;
      if (Pc(t), e)
        if (t.flags & 256)
          t.flags &= -257, t = Ps(
            l,
            t,
            a
          );
        else if (t.memoizedState !== null)
          t.child = l.child, t.flags |= 128, t = null;
        else throw Error(m(558));
      else if (El || iu(l, t, a, !1), e = (a & l.childLanes) !== 0, El || e) {
        if (u = ol, u !== null && (c = Li(u, a), c !== 0 && c !== n.retryLane))
          throw n.retryLane = c, Ua(l, c), wl(u, l, c), Ef;
        On(), t = Ps(
          l,
          t,
          a
        );
      } else
        l = n.treeContext, sl = ht(c.nextSibling), Ml = t, $ = !0, la = null, mt = !1, l !== null && Xo(t, l), t = hn(t, u), t.flags |= 4096;
      return t;
    }
    return l = Ht(l.child, {
      mode: u.mode,
      children: u.children
    }), l.ref = t.ref, t.child = l, l.return = t, l;
  }
  function Sn(l, t) {
    var a = t.ref;
    if (a === null)
      l !== null && l.ref !== null && (t.flags |= 4194816);
    else {
      if (typeof a != "function" && typeof a != "object")
        throw Error(m(284));
      (l === null || l.ref !== a) && (t.flags |= 4194816);
    }
  }
  function zf(l, t, a, u, e) {
    return Ha(t), a = tf(
      l,
      t,
      a,
      u,
      void 0,
      e
    ), u = af(), l !== null && !El ? (uf(l, t, e), Xt(l, t, e)) : ($ && u && Yc(t), t.flags |= 1, Ul(l, t, a, e), t.child);
  }
  function ld(l, t, a, u, e, n) {
    return Ha(t), t.updateQueue = null, a = us(
      t,
      u,
      a,
      e
    ), as(l), u = af(), l !== null && !El ? (uf(l, t, n), Xt(l, t, n)) : ($ && u && Yc(t), t.flags |= 1, Ul(l, t, a, n), t.child);
  }
  function td(l, t, a, u, e) {
    if (Ha(t), t.stateNode === null) {
      var n = eu, c = a.contextType;
      typeof c == "object" && c !== null && (n = Dl(c)), n = new a(u, n), t.memoizedState = n.state !== null && n.state !== void 0 ? n.state : null, n.updater = bf, t.stateNode = n, n._reactInternals = t, n = t.stateNode, n.props = u, n.state = t.memoizedState, n.refs = {}, wc(t), c = a.contextType, n.context = typeof c == "object" && c !== null ? Dl(c) : eu, n.state = t.memoizedState, c = a.getDerivedStateFromProps, typeof c == "function" && (gf(
        t,
        a,
        c,
        u
      ), n.state = t.memoizedState), typeof a.getDerivedStateFromProps == "function" || typeof n.getSnapshotBeforeUpdate == "function" || typeof n.UNSAFE_componentWillMount != "function" && typeof n.componentWillMount != "function" || (c = n.state, typeof n.componentWillMount == "function" && n.componentWillMount(), typeof n.UNSAFE_componentWillMount == "function" && n.UNSAFE_componentWillMount(), c !== n.state && bf.enqueueReplaceState(n, n.state, null), Pu(t, u, n, e), Iu(), n.state = t.memoizedState), typeof n.componentDidMount == "function" && (t.flags |= 4194308), u = !0;
    } else if (l === null) {
      n = t.stateNode;
      var f = t.memoizedProps, i = Xa(a, f);
      n.props = i;
      var h = n.context, b = a.contextType;
      c = eu, typeof b == "object" && b !== null && (c = Dl(b));
      var A = a.getDerivedStateFromProps;
      b = typeof A == "function" || typeof n.getSnapshotBeforeUpdate == "function", f = t.pendingProps !== f, b || typeof n.UNSAFE_componentWillReceiveProps != "function" && typeof n.componentWillReceiveProps != "function" || (f || h !== c) && Zs(
        t,
        n,
        u,
        c
      ), ua = !1;
      var S = t.memoizedState;
      n.state = S, Pu(t, u, n, e), Iu(), h = t.memoizedState, f || S !== h || ua ? (typeof A == "function" && (gf(
        t,
        a,
        A,
        u
      ), h = t.memoizedState), (i = ua || Qs(
        t,
        a,
        i,
        u,
        S,
        h,
        c
      )) ? (b || typeof n.UNSAFE_componentWillMount != "function" && typeof n.componentWillMount != "function" || (typeof n.componentWillMount == "function" && n.componentWillMount(), typeof n.UNSAFE_componentWillMount == "function" && n.UNSAFE_componentWillMount()), typeof n.componentDidMount == "function" && (t.flags |= 4194308)) : (typeof n.componentDidMount == "function" && (t.flags |= 4194308), t.memoizedProps = u, t.memoizedState = h), n.props = u, n.state = h, n.context = c, u = i) : (typeof n.componentDidMount == "function" && (t.flags |= 4194308), u = !1);
    } else {
      n = t.stateNode, Wc(l, t), c = t.memoizedProps, b = Xa(a, c), n.props = b, A = t.pendingProps, S = n.context, h = a.contextType, i = eu, typeof h == "object" && h !== null && (i = Dl(h)), f = a.getDerivedStateFromProps, (h = typeof f == "function" || typeof n.getSnapshotBeforeUpdate == "function") || typeof n.UNSAFE_componentWillReceiveProps != "function" && typeof n.componentWillReceiveProps != "function" || (c !== A || S !== i) && Zs(
        t,
        n,
        u,
        i
      ), ua = !1, S = t.memoizedState, n.state = S, Pu(t, u, n, e), Iu();
      var r = t.memoizedState;
      c !== A || S !== r || ua || l !== null && l.dependencies !== null && Fe(l.dependencies) ? (typeof f == "function" && (gf(
        t,
        a,
        f,
        u
      ), r = t.memoizedState), (b = ua || Qs(
        t,
        a,
        b,
        u,
        S,
        r,
        i
      ) || l !== null && l.dependencies !== null && Fe(l.dependencies)) ? (h || typeof n.UNSAFE_componentWillUpdate != "function" && typeof n.componentWillUpdate != "function" || (typeof n.componentWillUpdate == "function" && n.componentWillUpdate(u, r, i), typeof n.UNSAFE_componentWillUpdate == "function" && n.UNSAFE_componentWillUpdate(
        u,
        r,
        i
      )), typeof n.componentDidUpdate == "function" && (t.flags |= 4), typeof n.getSnapshotBeforeUpdate == "function" && (t.flags |= 1024)) : (typeof n.componentDidUpdate != "function" || c === l.memoizedProps && S === l.memoizedState || (t.flags |= 4), typeof n.getSnapshotBeforeUpdate != "function" || c === l.memoizedProps && S === l.memoizedState || (t.flags |= 1024), t.memoizedProps = u, t.memoizedState = r), n.props = u, n.state = r, n.context = i, u = b) : (typeof n.componentDidUpdate != "function" || c === l.memoizedProps && S === l.memoizedState || (t.flags |= 4), typeof n.getSnapshotBeforeUpdate != "function" || c === l.memoizedProps && S === l.memoizedState || (t.flags |= 1024), u = !1);
    }
    return n = u, Sn(l, t), u = (t.flags & 128) !== 0, n || u ? (n = t.stateNode, a = u && typeof a.getDerivedStateFromError != "function" ? null : n.render(), t.flags |= 1, l !== null && u ? (t.child = ja(
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
  function ad(l, t, a, u) {
    return Ra(), t.flags |= 256, Ul(l, t, a, u), t.child;
  }
  var Af = {
    dehydrated: null,
    treeContext: null,
    retryLane: 0,
    hydrationErrors: null
  };
  function _f(l) {
    return { baseLanes: l, cachePool: Ko() };
  }
  function pf(l, t, a) {
    return l = l !== null ? l.childLanes & ~a : 0, t && (l |= et), l;
  }
  function ud(l, t, a) {
    var u = t.pendingProps, e = !1, n = (t.flags & 128) !== 0, c;
    if ((c = n) || (c = l !== null && l.memoizedState === null ? !1 : (rl.current & 2) !== 0), c && (e = !0, t.flags &= -129), c = (t.flags & 32) !== 0, t.flags &= -33, l === null) {
      if ($) {
        if (e ? ca(t) : fa(), (l = sl) ? (l = d0(
          l,
          mt
        ), l = l !== null && l.data !== "&" ? l : null, l !== null && (t.memoizedState = {
          dehydrated: l,
          treeContext: Pt !== null ? { id: _t, overflow: pt } : null,
          retryLane: 536870912,
          hydrationErrors: null
        }, a = Yo(l), a.return = t, t.child = a, Ml = t, sl = null)) : l = null, l === null) throw ta(t);
        return fi(l) ? t.lanes = 32 : t.lanes = 536870912, null;
      }
      var f = u.children;
      return u = u.fallback, e ? (fa(), e = t.mode, f = rn(
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
      ), t.memoizedState = Af, ee(null, u)) : (ca(t), Of(t, f));
    }
    var i = l.memoizedState;
    if (i !== null && (f = i.dehydrated, f !== null)) {
      if (n)
        t.flags & 256 ? (ca(t), t.flags &= -257, t = Mf(
          l,
          t,
          a
        )) : t.memoizedState !== null ? (fa(), t.child = l.child, t.flags |= 128, t = null) : (fa(), f = u.fallback, e = t.mode, u = rn(
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
        ), t.memoizedState = Af, t = ee(null, u));
      else if (ca(t), fi(f)) {
        if (c = f.nextSibling && f.nextSibling.dataset, c) var h = c.dgst;
        c = h, u = Error(m(419)), u.stack = "", u.digest = c, Ju({ value: u, source: null, stack: null }), t = Mf(
          l,
          t,
          a
        );
      } else if (El || iu(l, t, a, !1), c = (a & l.childLanes) !== 0, El || c) {
        if (c = ol, c !== null && (u = Li(c, a), u !== 0 && u !== i.retryLane))
          throw i.retryLane = u, Ua(l, u), wl(c, l, u), Ef;
        ci(f) || On(), t = Mf(
          l,
          t,
          a
        );
      } else
        ci(f) ? (t.flags |= 192, t.child = l.child, t = null) : (l = i.treeContext, sl = ht(
          f.nextSibling
        ), Ml = t, $ = !0, la = null, mt = !1, l !== null && Xo(t, l), t = Of(
          t,
          u.children
        ), t.flags |= 4096);
      return t;
    }
    return e ? (fa(), f = u.fallback, e = t.mode, i = l.child, h = i.sibling, u = Ht(i, {
      mode: "hidden",
      children: u.children
    }), u.subtreeFlags = i.subtreeFlags & 65011712, h !== null ? f = Ht(
      h,
      f
    ) : (f = Na(
      f,
      e,
      a,
      null
    ), f.flags |= 2), f.return = t, u.return = t, u.sibling = f, t.child = u, ee(null, u), u = t.child, f = l.child.memoizedState, f === null ? f = _f(a) : (e = f.cachePool, e !== null ? (i = bl._currentValue, e = e.parent !== i ? { parent: i, pool: i } : e) : e = Ko(), f = {
      baseLanes: f.baseLanes | a,
      cachePool: e
    }), u.memoizedState = f, u.childLanes = pf(
      l,
      c,
      a
    ), t.memoizedState = Af, ee(l.child, u)) : (ca(t), a = l.child, l = a.sibling, a = Ht(a, {
      mode: "visible",
      children: u.children
    }), a.return = t, a.sibling = null, l !== null && (c = t.deletions, c === null ? (t.deletions = [l], t.flags |= 16) : c.push(l)), t.child = a, t.memoizedState = null, a);
  }
  function Of(l, t) {
    return t = rn(
      { mode: "visible", children: t },
      l.mode
    ), t.return = l, l.child = t;
  }
  function rn(l, t) {
    return l = lt(22, l, null, t), l.lanes = 0, l;
  }
  function Mf(l, t, a) {
    return ja(t, l.child, null, a), l = Of(
      t,
      t.pendingProps.children
    ), l.flags |= 2, t.memoizedState = null, l;
  }
  function ed(l, t, a) {
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
  function nd(l, t, a) {
    var u = t.pendingProps, e = u.revealOrder, n = u.tail;
    u = u.children;
    var c = rl.current, f = (c & 2) !== 0;
    if (f ? (c = c & 1 | 2, t.flags |= 128) : c &= 1, M(rl, c), Ul(l, t, u, a), u = $ ? Ku : 0, !f && l !== null && (l.flags & 128) !== 0)
      l: for (l = t.child; l !== null; ) {
        if (l.tag === 13)
          l.memoizedState !== null && ed(l, a, t);
        else if (l.tag === 19)
          ed(l, a, t);
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
          l = a.alternate, l !== null && en(l) === null && (e = a), a = a.sibling;
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
          if (l = e.alternate, l !== null && en(l) === null) {
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
    if (l !== null && (t.dependencies = l.dependencies), sa |= t.lanes, (a & t.childLanes) === 0)
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
      throw Error(m(153));
    if (t.child !== null) {
      for (l = t.child, a = Ht(l, l.pendingProps), t.child = a, a.return = t; l.sibling !== null; )
        l = l.sibling, a = a.sibling = Ht(l, l.pendingProps), a.return = t;
      a.sibling = null;
    }
    return t.child;
  }
  function Uf(l, t) {
    return (l.lanes & t) !== 0 ? !0 : (l = l.dependencies, !!(l !== null && Fe(l)));
  }
  function Hm(l, t, a) {
    switch (t.tag) {
      case 3:
        ql(t, t.stateNode.containerInfo), aa(t, bl, l.memoizedState.cache), Ra();
        break;
      case 27:
      case 5:
        Nu(t);
        break;
      case 4:
        ql(t, t.stateNode.containerInfo);
        break;
      case 10:
        aa(
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
          return u.dehydrated !== null ? (ca(t), t.flags |= 128, null) : (a & t.child.childLanes) !== 0 ? ud(l, t, a) : (ca(t), l = Xt(
            l,
            t,
            a
          ), l !== null ? l.sibling : null);
        ca(t);
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
            return nd(
              l,
              t,
              a
            );
          t.flags |= 128;
        }
        if (e = t.memoizedState, e !== null && (e.rendering = null, e.tail = null, e.lastEffect = null), M(rl, rl.current), u) break;
        return null;
      case 22:
        return t.lanes = 0, ks(
          l,
          t,
          a,
          t.pendingProps
        );
      case 24:
        aa(t, bl, l.memoizedState.cache);
    }
    return Xt(l, t, a);
  }
  function cd(l, t, a) {
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
      El = !1, $ && (t.flags & 1048576) !== 0 && Go(t, Ku, t.index);
    switch (t.lanes = 0, t.tag) {
      case 16:
        l: {
          var u = t.pendingProps;
          if (l = qa(t.elementType), t.type = l, typeof l == "function")
            Hc(l) ? (u = Xa(l, u), t.tag = 1, t = td(
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
                t.tag = 11, t = Ws(
                  null,
                  t,
                  l,
                  u,
                  a
                );
                break l;
              } else if (e === W) {
                t.tag = 14, t = $s(
                  null,
                  t,
                  l,
                  u,
                  a
                );
                break l;
              }
            }
            throw t = Ut(l) || l, Error(m(306, t, ""));
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
        ), td(
          l,
          t,
          u,
          e,
          a
        );
      case 3:
        l: {
          if (ql(
            t,
            t.stateNode.containerInfo
          ), l === null) throw Error(m(387));
          u = t.pendingProps;
          var n = t.memoizedState;
          e = n.element, Wc(l, t), Pu(t, u, null, a);
          var c = t.memoizedState;
          if (u = c.cache, aa(t, bl, u), u !== n.cache && Lc(
            t,
            [bl],
            a,
            !0
          ), Iu(), u = c.element, n.isDehydrated)
            if (n = {
              element: u,
              isDehydrated: !1,
              cache: c.cache
            }, t.updateQueue.baseState = n, t.memoizedState = n, t.flags & 256) {
              t = ad(
                l,
                t,
                u,
                a
              );
              break l;
            } else if (u !== e) {
              e = st(
                Error(m(424)),
                t
              ), Ju(e), t = ad(
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
              for (sl = ht(l.firstChild), Ml = t, $ = !0, la = null, mt = !0, a = ko(
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
        return Sn(l, t), l === null ? (a = r0(
          t.type,
          null,
          t.pendingProps,
          null
        )) ? t.memoizedState = a : $ || (a = t.type, l = t.pendingProps, u = Hn(
          L.current
        ).createElement(a), u[Ol] = t, u[Zl] = l, Nl(u, a, l), _l(u), t.stateNode = u) : t.memoizedState = r0(
          t.type,
          l.memoizedProps,
          t.pendingProps,
          l.memoizedState
        ), null;
      case 27:
        return Nu(t), l === null && $ && (u = t.stateNode = v0(
          t.type,
          t.pendingProps,
          L.current
        ), Ml = t, mt = !0, e = sl, ha(t.type) ? (ii = e, sl = ht(u.firstChild)) : sl = e), Ul(
          l,
          t,
          t.pendingProps.children,
          a
        ), Sn(l, t), l === null && (t.flags |= 4194304), t.child;
      case 5:
        return l === null && $ && ((e = u = sl) && (u = ov(
          u,
          t.type,
          t.pendingProps,
          mt
        ), u !== null ? (t.stateNode = u, Ml = t, sl = ht(u.firstChild), mt = !1, e = !0) : e = !1), e || ta(t)), Nu(t), e = t.type, n = t.pendingProps, c = l !== null ? l.memoizedProps : null, u = n.children, ui(e, n) ? u = null : c !== null && ui(e, c) && (t.flags |= 32), t.memoizedState !== null && (e = tf(
          l,
          t,
          _m,
          null,
          null,
          a
        ), be._currentValue = e), Sn(l, t), Ul(l, t, u, a), t.child;
      case 6:
        return l === null && $ && ((l = a = sl) && (a = sv(
          a,
          t.pendingProps,
          mt
        ), a !== null ? (t.stateNode = a, Ml = t, sl = null, l = !0) : l = !1), l || ta(t)), null;
      case 13:
        return ud(l, t, a);
      case 4:
        return ql(
          t,
          t.stateNode.containerInfo
        ), u = t.pendingProps, l === null ? t.child = ja(
          t,
          null,
          u,
          a
        ) : Ul(l, t, u, a), t.child;
      case 11:
        return Ws(
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
        return u = t.pendingProps, aa(t, t.type, u.value), Ul(l, t, u.children, a), t.child;
      case 9:
        return e = t.type._context, u = t.pendingProps.children, Ha(t), e = Dl(e), u = u(e), t.flags |= 1, Ul(l, t, u, a), t.child;
      case 14:
        return $s(
          l,
          t,
          t.type,
          t.pendingProps,
          a
        );
      case 15:
        return Fs(
          l,
          t,
          t.type,
          t.pendingProps,
          a
        );
      case 19:
        return nd(l, t, a);
      case 31:
        return Cm(l, t, a);
      case 22:
        return ks(
          l,
          t,
          a,
          t.pendingProps
        );
      case 24:
        return Ha(t), u = Dl(bl), l === null ? (e = Kc(), e === null && (e = ol, n = xc(), e.pooledCache = n, n.refCount++, n !== null && (e.pooledCacheLanes |= a), e = n), t.memoizedState = { parent: u, cache: e }, wc(t), aa(t, bl, e)) : ((l.lanes & a) !== 0 && (Wc(l, t), Pu(t, null, null, a), Iu()), e = l.memoizedState, n = t.memoizedState, e.parent !== u ? (e = { parent: u, cache: u }, t.memoizedState = e, t.lanes === 0 && (t.memoizedState = t.updateQueue.baseState = e), aa(t, bl, u)) : (u = n.cache, aa(t, bl, u), u !== e.cache && Lc(
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
    throw Error(m(156, t.tag));
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
          throw Ya = ln, Jc;
    } else l.flags &= -16777217;
  }
  function fd(l, t) {
    if (t.type !== "stylesheet" || (t.state.loading & 4) !== 0)
      l.flags &= -16777217;
    else if (l.flags |= 16777216, !z0(t))
      if (Hd()) l.flags |= 8192;
      else
        throw Ya = ln, Jc;
  }
  function gn(l, t) {
    t !== null && (l.flags |= 4), l.flags & 16384 && (t = l.tag !== 22 ? Xi() : 536870912, l.lanes |= t, Tu |= t);
  }
  function ne(l, t) {
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
  function Bm(l, t, a) {
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
        return a = t.stateNode, u = null, l !== null && (u = l.memoizedState.cache), t.memoizedState.cache !== u && (t.flags |= 2048), Yt(bl), Sl(), a.pendingContext && (a.context = a.pendingContext, a.pendingContext = null), (l === null || l.child === null) && (fu(t) ? Qt(t) : l === null || l.memoizedState.isDehydrated && (t.flags & 256) === 0 || (t.flags |= 1024, Xc())), dl(t), null;
      case 26:
        var e = t.type, n = t.memoizedState;
        return l === null ? (Qt(t), n !== null ? (dl(t), fd(t, n)) : (dl(t), Nf(
          t,
          e,
          null,
          u,
          a
        ))) : n ? n !== l.memoizedState ? (Qt(t), dl(t), fd(t, n)) : (dl(t), t.flags &= -16777217) : (l = l.memoizedProps, l !== u && Qt(t), dl(t), Nf(
          t,
          e,
          l,
          u,
          a
        )), null;
      case 27:
        if (De(t), a = L.current, e = t.type, l !== null && t.stateNode != null)
          l.memoizedProps !== u && Qt(t);
        else {
          if (!u) {
            if (t.stateNode === null)
              throw Error(m(166));
            return dl(t), null;
          }
          l = R.current, fu(t) ? Qo(t) : (l = v0(e, u, a), t.stateNode = l, Qt(t));
        }
        return dl(t), null;
      case 5:
        if (De(t), e = t.type, l !== null && t.stateNode != null)
          l.memoizedProps !== u && Qt(t);
        else {
          if (!u) {
            if (t.stateNode === null)
              throw Error(m(166));
            return dl(t), null;
          }
          if (n = R.current, fu(t))
            Qo(t);
          else {
            var c = Hn(
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
            throw Error(m(166));
          if (l = L.current, fu(t)) {
            if (l = t.stateNode, a = t.memoizedProps, u = null, e = Ml, e !== null)
              switch (e.tag) {
                case 27:
                case 5:
                  u = e.memoizedProps;
              }
            l[Ol] = t, l = !!(l.nodeValue === a || u !== null && u.suppressHydrationWarning === !0 || u0(l.nodeValue, a)), l || ta(t, !0);
          } else
            l = Hn(l).createTextNode(
              u
            ), l[Ol] = t, t.stateNode = l;
        }
        return dl(t), null;
      case 31:
        if (a = t.memoizedState, l === null || l.memoizedState !== null) {
          if (u = fu(t), a !== null) {
            if (l === null) {
              if (!u) throw Error(m(318));
              if (l = t.memoizedState, l = l !== null ? l.dehydrated : null, !l) throw Error(m(557));
              l[Ol] = t;
            } else
              Ra(), (t.flags & 128) === 0 && (t.memoizedState = null), t.flags |= 4;
            dl(t), l = !1;
          } else
            a = Xc(), l !== null && l.memoizedState !== null && (l.memoizedState.hydrationErrors = a), l = !0;
          if (!l)
            return t.flags & 256 ? (at(t), t) : (at(t), null);
          if ((t.flags & 128) !== 0)
            throw Error(m(558));
        }
        return dl(t), null;
      case 13:
        if (u = t.memoizedState, l === null || l.memoizedState !== null && l.memoizedState.dehydrated !== null) {
          if (e = fu(t), u !== null && u.dehydrated !== null) {
            if (l === null) {
              if (!e) throw Error(m(318));
              if (e = t.memoizedState, e = e !== null ? e.dehydrated : null, !e) throw Error(m(317));
              e[Ol] = t;
            } else
              Ra(), (t.flags & 128) === 0 && (t.memoizedState = null), t.flags |= 4;
            dl(t), e = !1;
          } else
            e = Xc(), l !== null && l.memoizedState !== null && (l.memoizedState.hydrationErrors = e), e = !0;
          if (!e)
            return t.flags & 256 ? (at(t), t) : (at(t), null);
        }
        return at(t), (t.flags & 128) !== 0 ? (t.lanes = a, t) : (a = u !== null, l = l !== null && l.memoizedState !== null, a && (u = t.child, e = null, u.alternate !== null && u.alternate.memoizedState !== null && u.alternate.memoizedState.cachePool !== null && (e = u.alternate.memoizedState.cachePool.pool), n = null, u.memoizedState !== null && u.memoizedState.cachePool !== null && (n = u.memoizedState.cachePool.pool), n !== e && (u.flags |= 2048)), a !== l && a && (t.child.flags |= 8192), gn(t, t.updateQueue), dl(t), null);
      case 4:
        return Sl(), l === null && If(t.stateNode.containerInfo), dl(t), null;
      case 10:
        return Yt(t.type), dl(t), null;
      case 19:
        if (_(rl), u = t.memoizedState, u === null) return dl(t), null;
        if (e = (t.flags & 128) !== 0, n = u.rendering, n === null)
          if (e) ne(u, !1);
          else {
            if (hl !== 0 || l !== null && (l.flags & 128) !== 0)
              for (l = t.child; l !== null; ) {
                if (n = en(l), n !== null) {
                  for (t.flags |= 128, ne(u, !1), l = n.updateQueue, t.updateQueue = l, gn(t, l), t.subtreeFlags = 0, l = a, a = t.child; a !== null; )
                    qo(a, l), a = a.sibling;
                  return M(
                    rl,
                    rl.current & 1 | 2
                  ), $ && Bt(t, u.treeForkCount), t.child;
                }
                l = l.sibling;
              }
            u.tail !== null && Fl() > An && (t.flags |= 128, e = !0, ne(u, !1), t.lanes = 4194304);
          }
        else {
          if (!e)
            if (l = en(n), l !== null) {
              if (t.flags |= 128, e = !0, l = l.updateQueue, t.updateQueue = l, gn(t, l), ne(u, !0), u.tail === null && u.tailMode === "hidden" && !n.alternate && !$)
                return dl(t), null;
            } else
              2 * Fl() - u.renderingStartTime > An && a !== 536870912 && (t.flags |= 128, e = !0, ne(u, !1), t.lanes = 4194304);
          u.isBackwards ? (n.sibling = t.child, t.child = n) : (l = u.last, l !== null ? l.sibling = n : t.child = n, u.last = n);
        }
        return u.tail !== null ? (l = u.tail, u.rendering = l, u.tail = l.sibling, u.renderingStartTime = Fl(), l.sibling = null, a = rl.current, M(
          rl,
          e ? a & 1 | 2 : a & 1
        ), $ && Bt(t, u.treeForkCount), l) : (dl(t), null);
      case 22:
      case 23:
        return at(t), Ic(), u = t.memoizedState !== null, l !== null ? l.memoizedState !== null !== u && (t.flags |= 8192) : u && (t.flags |= 8192), u ? (a & 536870912) !== 0 && (t.flags & 128) === 0 && (dl(t), t.subtreeFlags & 6 && (t.flags |= 8192)) : dl(t), a = t.updateQueue, a !== null && gn(t, a.retryQueue), a = null, l !== null && l.memoizedState !== null && l.memoizedState.cachePool !== null && (a = l.memoizedState.cachePool.pool), u = null, t.memoizedState !== null && t.memoizedState.cachePool !== null && (u = t.memoizedState.cachePool.pool), u !== a && (t.flags |= 2048), l !== null && _(Ba), null;
      case 24:
        return a = null, l !== null && (a = l.memoizedState.cache), t.memoizedState.cache !== a && (t.flags |= 2048), Yt(bl), dl(t), null;
      case 25:
        return null;
      case 30:
        return null;
    }
    throw Error(m(156, t.tag));
  }
  function qm(l, t) {
    switch (jc(t), t.tag) {
      case 1:
        return l = t.flags, l & 65536 ? (t.flags = l & -65537 | 128, t) : null;
      case 3:
        return Yt(bl), Sl(), l = t.flags, (l & 65536) !== 0 && (l & 128) === 0 ? (t.flags = l & -65537 | 128, t) : null;
      case 26:
      case 27:
      case 5:
        return De(t), null;
      case 31:
        if (t.memoizedState !== null) {
          if (at(t), t.alternate === null)
            throw Error(m(340));
          Ra();
        }
        return l = t.flags, l & 65536 ? (t.flags = l & -65537 | 128, t) : null;
      case 13:
        if (at(t), l = t.memoizedState, l !== null && l.dehydrated !== null) {
          if (t.alternate === null)
            throw Error(m(340));
          Ra();
        }
        return l = t.flags, l & 65536 ? (t.flags = l & -65537 | 128, t) : null;
      case 19:
        return _(rl), null;
      case 4:
        return Sl(), null;
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
  function id(l, t) {
    switch (jc(t), t.tag) {
      case 3:
        Yt(bl), Sl();
        break;
      case 26:
      case 27:
      case 5:
        De(t);
        break;
      case 4:
        Sl();
        break;
      case 31:
        t.memoizedState !== null && at(t);
        break;
      case 13:
        at(t);
        break;
      case 19:
        _(rl);
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
  function ce(l, t) {
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
  function ia(l, t, a) {
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
  function od(l) {
    var t = l.updateQueue;
    if (t !== null) {
      var a = l.stateNode;
      try {
        Po(t, a);
      } catch (u) {
        ul(l, l.return, u);
      }
    }
  }
  function sd(l, t, a) {
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
  function fe(l, t) {
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
  function dd(l) {
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
      uv(u, l.type, a, t), u[Zl] = t;
    } catch (e) {
      ul(l, l.return, e);
    }
  }
  function yd(l) {
    return l.tag === 5 || l.tag === 3 || l.tag === 26 || l.tag === 27 && ha(l.type) || l.tag === 4;
  }
  function Cf(l) {
    l: for (; ; ) {
      for (; l.sibling === null; ) {
        if (l.return === null || yd(l.return)) return null;
        l = l.return;
      }
      for (l.sibling.return = l.return, l = l.sibling; l.tag !== 5 && l.tag !== 6 && l.tag !== 18; ) {
        if (l.tag === 27 && ha(l.type) || l.flags & 2 || l.child === null || l.tag === 4) continue l;
        l.child.return = l, l = l.child;
      }
      if (!(l.flags & 2)) return l.stateNode;
    }
  }
  function Hf(l, t, a) {
    var u = l.tag;
    if (u === 5 || u === 6)
      l = l.stateNode, t ? (a.nodeType === 9 ? a.body : a.nodeName === "HTML" ? a.ownerDocument.body : a).insertBefore(l, t) : (t = a.nodeType === 9 ? a.body : a.nodeName === "HTML" ? a.ownerDocument.body : a, t.appendChild(l), a = a._reactRootContainer, a != null || t.onclick !== null || (t.onclick = Rt));
    else if (u !== 4 && (u === 27 && ha(l.type) && (a = l.stateNode, t = null), l = l.child, l !== null))
      for (Hf(l, t, a), l = l.sibling; l !== null; )
        Hf(l, t, a), l = l.sibling;
  }
  function bn(l, t, a) {
    var u = l.tag;
    if (u === 5 || u === 6)
      l = l.stateNode, t ? a.insertBefore(l, t) : a.appendChild(l);
    else if (u !== 4 && (u === 27 && ha(l.type) && (a = l.stateNode), l = l.child, l !== null))
      for (bn(l, t, a), l = l.sibling; l !== null; )
        bn(l, t, a), l = l.sibling;
  }
  function md(l) {
    var t = l.stateNode, a = l.memoizedProps;
    try {
      for (var u = l.type, e = t.attributes; e.length; )
        t.removeAttributeNode(e[0]);
      Nl(t, u, a), t[Ol] = l, t[Zl] = a;
    } catch (n) {
      ul(l, l.return, n);
    }
  }
  var Zt = !1, zl = !1, Bf = !1, vd = typeof WeakSet == "function" ? WeakSet : Set, pl = null;
  function Ym(l, t) {
    if (l = l.containerInfo, ti = Qn, l = Oo(l), Oc(l)) {
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
    for (ai = { focusedElem: l, selectionRange: a }, Qn = !1, pl = t; pl !== null; )
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
              if ((l & 1024) !== 0) throw Error(m(163));
          }
          if (l = t.sibling, l !== null) {
            l.return = t.return, pl = l;
            break;
          }
          pl = t.return;
        }
  }
  function hd(l, t, a) {
    var u = a.flags;
    switch (a.tag) {
      case 0:
      case 11:
      case 15:
        xt(l, a), u & 4 && ce(5, a);
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
        u & 64 && od(a), u & 512 && fe(a, a.return);
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
            Po(l, t);
          } catch (c) {
            ul(a, a.return, c);
          }
        }
        break;
      case 27:
        t === null && u & 4 && md(a);
      case 26:
      case 5:
        xt(l, a), t === null && u & 4 && dd(a), u & 512 && fe(a, a.return);
        break;
      case 12:
        xt(l, a);
        break;
      case 31:
        xt(l, a), u & 4 && gd(l, a);
        break;
      case 13:
        xt(l, a), u & 4 && bd(l, a), u & 64 && (l = a.memoizedState, l !== null && (l = l.dehydrated, l !== null && (a = Km.bind(
          null,
          a
        ), dv(l, a))));
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
  function Sd(l) {
    var t = l.alternate;
    t !== null && (l.alternate = null, Sd(t)), l.child = null, l.deletions = null, l.sibling = null, l.tag === 5 && (t = l.stateNode, t !== null && oc(t)), l.stateNode = null, l.return = null, l.dependencies = null, l.memoizedProps = null, l.memoizedState = null, l.pendingProps = null, l.stateNode = null, l.updateQueue = null;
  }
  var yl = null, xl = !1;
  function Lt(l, t, a) {
    for (a = a.child; a !== null; )
      rd(l, t, a), a = a.sibling;
  }
  function rd(l, t, a) {
    if (kl && typeof kl.onCommitFiberUnmount == "function")
      try {
        kl.onCommitFiberUnmount(Ru, a);
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
        ha(a.type) && (yl = a.stateNode, xl = !1), Lt(
          l,
          t,
          a
        ), Se(a.stateNode), yl = u, xl = e;
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
        yl !== null && (xl ? (l = yl, o0(
          l.nodeType === 9 ? l.body : l.nodeName === "HTML" ? l.ownerDocument.body : l,
          a.stateNode
        ), Du(l)) : o0(yl, a.stateNode));
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
        ia(2, a, t), zl || ia(4, a, t), Lt(
          l,
          t,
          a
        );
        break;
      case 1:
        zl || (Ot(a, t), u = a.stateNode, typeof u.componentWillUnmount == "function" && sd(
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
  function gd(l, t) {
    if (t.memoizedState === null && (l = t.alternate, l !== null && (l = l.memoizedState, l !== null))) {
      l = l.dehydrated;
      try {
        Du(l);
      } catch (a) {
        ul(t, t.return, a);
      }
    }
  }
  function bd(l, t) {
    if (t.memoizedState === null && (l = t.alternate, l !== null && (l = l.memoizedState, l !== null && (l = l.dehydrated, l !== null))))
      try {
        Du(l);
      } catch (a) {
        ul(t, t.return, a);
      }
  }
  function jm(l) {
    switch (l.tag) {
      case 31:
      case 13:
      case 19:
        var t = l.stateNode;
        return t === null && (t = l.stateNode = new vd()), t;
      case 22:
        return l = l.stateNode, t = l._retryCache, t === null && (t = l._retryCache = new vd()), t;
      default:
        throw Error(m(435, l.tag));
    }
  }
  function Tn(l, t) {
    var a = jm(l);
    t.forEach(function(u) {
      if (!a.has(u)) {
        a.add(u);
        var e = Jm.bind(null, l, u);
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
              if (ha(f.type)) {
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
        if (yl === null) throw Error(m(160));
        rd(n, c, e), yl = null, xl = !1, n = e.alternate, n !== null && (n.return = null), e.return = null;
      }
    if (t.subtreeFlags & 13886)
      for (t = t.child; t !== null; )
        Td(t, l), t = t.sibling;
  }
  var bt = null;
  function Td(l, t) {
    var a = l.alternate, u = l.flags;
    switch (l.tag) {
      case 0:
      case 11:
      case 14:
      case 15:
        Vl(t, l), Kl(l), u & 4 && (ia(3, l, l.return), ce(3, l), ia(5, l, l.return));
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
                      n = e.getElementsByTagName("title")[0], (!n || n[Bu] || n[Ol] || n.namespaceURI === "http://www.w3.org/2000/svg" || n.hasAttribute("itemprop")) && (n = e.createElement(u), e.head.insertBefore(
                        n,
                        e.querySelector("head > title")
                      )), Nl(n, u, a), n[Ol] = l, _l(n), u = n;
                      break l;
                    case "link":
                      var c = T0(
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
                      if (c = T0(
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
                      throw Error(m(468, u));
                  }
                  n[Ol] = l, _l(n), u = n;
                }
                l.stateNode = u;
              } else
                E0(
                  e,
                  l.type,
                  l.stateNode
                );
            else
              l.stateNode = b0(
                e,
                u,
                l.memoizedProps
              );
          else
            n !== u ? (n === null ? a.stateNode !== null && (a = a.stateNode, a.parentNode.removeChild(a)) : n.count--, u === null ? E0(
              e,
              l.type,
              l.stateNode
            ) : b0(
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
            throw Error(m(162));
          u = l.memoizedProps, a = l.stateNode;
          try {
            a.nodeValue = u;
          } catch (U) {
            ul(l, l.return, U);
          }
        }
        break;
      case 3:
        if (Yn = null, e = bt, bt = Bn(t.containerInfo), Vl(t, l), bt = e, Kl(l), u & 4 && a !== null && a.memoizedState.isDehydrated)
          try {
            Du(t.containerInfo);
          } catch (U) {
            ul(l, l.return, U);
          }
        Bf && (Bf = !1, Ed(l));
        break;
      case 4:
        u = bt, bt = Bn(
          l.stateNode.containerInfo
        ), Vl(t, l), Kl(l), bt = u;
        break;
      case 12:
        Vl(t, l), Kl(l);
        break;
      case 31:
        Vl(t, l), Kl(l), u & 4 && (u = l.updateQueue, u !== null && (l.updateQueue = null, Tn(l, u)));
        break;
      case 13:
        Vl(t, l), Kl(l), l.child.flags & 8192 && l.memoizedState !== null != (a !== null && a.memoizedState !== null) && (zn = Fl()), u & 4 && (u = l.updateQueue, u !== null && (l.updateQueue = null, Tn(l, u)));
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
                  e ? s0(r, !0) : s0(i.stateNode, !1);
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
        u & 4 && (u = l.updateQueue, u !== null && (a = u.retryQueue, a !== null && (u.retryQueue = null, Tn(l, a))));
        break;
      case 19:
        Vl(t, l), Kl(l), u & 4 && (u = l.updateQueue, u !== null && (l.updateQueue = null, Tn(l, u)));
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
          if (yd(u)) {
            a = u;
            break;
          }
          u = u.return;
        }
        if (a == null) throw Error(m(160));
        switch (a.tag) {
          case 27:
            var e = a.stateNode, n = Cf(l);
            bn(l, n, e);
            break;
          case 5:
            var c = a.stateNode;
            a.flags & 32 && (ka(c, ""), a.flags &= -33);
            var f = Cf(l);
            bn(l, f, c);
            break;
          case 3:
          case 4:
            var i = a.stateNode.containerInfo, h = Cf(l);
            Hf(
              l,
              h,
              i
            );
            break;
          default:
            throw Error(m(161));
        }
      } catch (b) {
        ul(l, l.return, b);
      }
      l.flags &= -3;
    }
    t & 4096 && (l.flags &= -4097);
  }
  function Ed(l) {
    if (l.subtreeFlags & 1024)
      for (l = l.child; l !== null; ) {
        var t = l;
        Ed(t), t.tag === 5 && t.flags & 1024 && t.stateNode.reset(), l = l.sibling;
      }
  }
  function xt(l, t) {
    if (t.subtreeFlags & 8772)
      for (t = t.child; t !== null; )
        hd(l, t.alternate, t), t = t.sibling;
  }
  function Qa(l) {
    for (l = l.child; l !== null; ) {
      var t = l;
      switch (t.tag) {
        case 0:
        case 11:
        case 14:
        case 15:
          ia(4, t, t.return), Qa(t);
          break;
        case 1:
          Ot(t, t.return);
          var a = t.stateNode;
          typeof a.componentWillUnmount == "function" && sd(
            t,
            t.return,
            a
          ), Qa(t);
          break;
        case 27:
          Se(t.stateNode);
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
          ), ce(4, n);
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
                  Io(i[e], f);
            } catch (h) {
              ul(u, u.return, h);
            }
          }
          a && c & 64 && od(n), fe(n, n.return);
          break;
        case 27:
          md(n);
        case 26:
        case 5:
          Vt(
            e,
            n,
            a
          ), a && u === null && c & 4 && dd(n), fe(n, n.return);
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
          ), a && c & 4 && gd(e, n);
          break;
        case 13:
          Vt(
            e,
            n,
            a
          ), a && c & 4 && bd(e, n);
          break;
        case 22:
          n.memoizedState === null && Vt(
            e,
            n,
            a
          ), fe(n, n.return);
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
    l !== null && l.memoizedState !== null && l.memoizedState.cachePool !== null && (a = l.memoizedState.cachePool.pool), l = null, t.memoizedState !== null && t.memoizedState.cachePool !== null && (l = t.memoizedState.cachePool.pool), l !== a && (l != null && l.refCount++, a != null && wu(a));
  }
  function Yf(l, t) {
    l = null, t.alternate !== null && (l = t.alternate.memoizedState.cache), t = t.memoizedState.cache, t !== l && (t.refCount++, l != null && wu(l));
  }
  function Tt(l, t, a, u) {
    if (t.subtreeFlags & 10256)
      for (t = t.child; t !== null; )
        zd(
          l,
          t,
          a,
          u
        ), t = t.sibling;
  }
  function zd(l, t, a, u) {
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
        ), e & 2048 && ce(9, t);
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
        ), e & 2048 && (l = null, t.alternate !== null && (l = t.alternate.memoizedState.cache), t = t.memoizedState.cache, t !== l && (t.refCount++, l != null && wu(l)));
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
        ) : ie(l, t) : n._visibility & 2 ? Tt(
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
          ), ce(8, c);
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
          ) : ie(
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
  function ie(l, t) {
    if (t.subtreeFlags & 10256)
      for (t = t.child; t !== null; ) {
        var a = l, u = t, e = u.flags;
        switch (u.tag) {
          case 22:
            ie(a, u), e & 2048 && qf(
              u.alternate,
              u
            );
            break;
          case 24:
            ie(a, u), e & 2048 && Yf(u.alternate, u);
            break;
          default:
            ie(a, u);
        }
        t = t.sibling;
      }
  }
  var oe = 8192;
  function gu(l, t, a) {
    if (l.subtreeFlags & oe)
      for (l = l.child; l !== null; )
        Ad(
          l,
          t,
          a
        ), l = l.sibling;
  }
  function Ad(l, t, a) {
    switch (l.tag) {
      case 26:
        gu(
          l,
          t,
          a
        ), l.flags & oe && l.memoizedState !== null && Av(
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
        bt = Bn(l.stateNode.containerInfo), gu(
          l,
          t,
          a
        ), bt = u;
        break;
      case 22:
        l.memoizedState === null && (u = l.alternate, u !== null && u.memoizedState !== null ? (u = oe, oe = 16777216, gu(
          l,
          t,
          a
        ), oe = u) : gu(
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
  function _d(l) {
    var t = l.alternate;
    if (t !== null && (l = t.child, l !== null)) {
      t.child = null;
      do
        t = l.sibling, l.sibling = null, l = t;
      while (l !== null);
    }
  }
  function se(l) {
    var t = l.deletions;
    if ((l.flags & 16) !== 0) {
      if (t !== null)
        for (var a = 0; a < t.length; a++) {
          var u = t[a];
          pl = u, Od(
            u,
            l
          );
        }
      _d(l);
    }
    if (l.subtreeFlags & 10256)
      for (l = l.child; l !== null; )
        pd(l), l = l.sibling;
  }
  function pd(l) {
    switch (l.tag) {
      case 0:
      case 11:
      case 15:
        se(l), l.flags & 2048 && ia(9, l, l.return);
        break;
      case 3:
        se(l);
        break;
      case 12:
        se(l);
        break;
      case 22:
        var t = l.stateNode;
        l.memoizedState !== null && t._visibility & 2 && (l.return === null || l.return.tag !== 13) ? (t._visibility &= -3, En(l)) : se(l);
        break;
      default:
        se(l);
    }
  }
  function En(l) {
    var t = l.deletions;
    if ((l.flags & 16) !== 0) {
      if (t !== null)
        for (var a = 0; a < t.length; a++) {
          var u = t[a];
          pl = u, Od(
            u,
            l
          );
        }
      _d(l);
    }
    for (l = l.child; l !== null; ) {
      switch (t = l, t.tag) {
        case 0:
        case 11:
        case 15:
          ia(8, t, t.return), En(t);
          break;
        case 22:
          a = t.stateNode, a._visibility & 2 && (a._visibility &= -3, En(t));
          break;
        default:
          En(t);
      }
      l = l.sibling;
    }
  }
  function Od(l, t) {
    for (; pl !== null; ) {
      var a = pl;
      switch (a.tag) {
        case 0:
        case 11:
        case 15:
          ia(8, a, t);
          break;
        case 23:
        case 22:
          if (a.memoizedState !== null && a.memoizedState.cachePool !== null) {
            var u = a.memoizedState.cachePool.pool;
            u != null && u.refCount++;
          }
          break;
        case 24:
          wu(a.memoizedState.cache);
      }
      if (u = a.child, u !== null) u.return = a, pl = u;
      else
        l: for (a = l; pl !== null; ) {
          u = pl;
          var e = u.sibling, n = u.return;
          if (Sd(u), u === a) {
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
  var Gm = {
    getCacheForType: function(l) {
      var t = Dl(bl), a = t.data.get(l);
      return a === void 0 && (a = l(), t.data.set(l, a)), a;
    },
    cacheSignal: function() {
      return Dl(bl).controller.signal;
    }
  }, Xm = typeof WeakMap == "function" ? WeakMap : Map, P = 0, ol = null, x = null, J = 0, al = 0, ut = null, oa = !1, bu = !1, jf = !1, Kt = 0, hl = 0, sa = 0, Za = 0, Gf = 0, et = 0, Tu = 0, de = null, Jl = null, Xf = !1, zn = 0, Md = 0, An = 1 / 0, _n = null, da = null, Al = 0, ya = null, Eu = null, Jt = 0, Qf = 0, Zf = null, Dd = null, ye = 0, Lf = null;
  function nt() {
    return (P & 2) !== 0 && J !== 0 ? J & -J : T.T !== null ? Wf() : xi();
  }
  function Ud() {
    if (et === 0)
      if ((J & 536870912) === 0 || $) {
        var l = Re;
        Re <<= 1, (Re & 3932160) === 0 && (Re = 262144), et = l;
      } else et = 536870912;
    return l = tt.current, l !== null && (l.flags |= 32), et;
  }
  function wl(l, t, a) {
    (l === ol && (al === 2 || al === 9) || l.cancelPendingCommit !== null) && (zu(l, 0), ma(
      l,
      J,
      et,
      !1
    )), Hu(l, a), ((P & 2) === 0 || l !== ol) && (l === ol && ((P & 2) === 0 && (Za |= a), hl === 4 && ma(
      l,
      J,
      et,
      !1
    )), Mt(l));
  }
  function Nd(l, t, a) {
    if ((P & 6) !== 0) throw Error(m(327));
    var u = !a && (t & 127) === 0 && (t & l.expiredLanes) === 0 || Cu(l, t), e = u ? Lm(l, t) : Vf(l, t, !0), n = u;
    do {
      if (e === 0) {
        bu && !u && ma(l, t, 0, !1);
        break;
      } else {
        if (a = l.current.alternate, n && !Qm(a)) {
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
              e = de;
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
          zu(l, 0), ma(l, t, 0, !0);
          break;
        }
        l: {
          switch (u = l, n = e, n) {
            case 0:
            case 1:
              throw Error(m(345));
            case 4:
              if ((t & 4194048) !== t) break;
            case 6:
              ma(
                u,
                t,
                et,
                !oa
              );
              break l;
            case 2:
              Jl = null;
              break;
            case 3:
            case 5:
              break;
            default:
              throw Error(m(329));
          }
          if ((t & 62914560) === t && (e = zn + 300 - Fl(), 10 < e)) {
            if (ma(
              u,
              t,
              et,
              !oa
            ), He(u, 0, !0) !== 0) break l;
            Jt = t, u.timeoutHandle = f0(
              Rd.bind(
                null,
                u,
                a,
                Jl,
                _n,
                Xf,
                t,
                et,
                Za,
                Tu,
                oa,
                n,
                "Throttled",
                -0,
                0
              ),
              e
            );
            break l;
          }
          Rd(
            u,
            a,
            Jl,
            _n,
            Xf,
            t,
            et,
            Za,
            Tu,
            oa,
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
  function Rd(l, t, a, u, e, n, c, f, i, h, b, A, S, r) {
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
      }, Ad(
        t,
        n,
        A
      );
      var U = (n & 62914560) === n ? zn - Fl() : (n & 4194048) === n ? Md - Fl() : 0;
      if (U = _v(
        A,
        U
      ), U !== null) {
        Jt = n, l.cancelPendingCommit = U(
          Xd.bind(
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
        ), ma(l, n, c, !h);
        return;
      }
    }
    Xd(
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
  function Qm(l) {
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
  function ma(l, t, a, u) {
    t &= ~Gf, t &= ~Za, l.suspendedLanes |= t, l.pingedLanes &= ~t, u && (l.warmLanes |= t), u = l.expirationTimes;
    for (var e = t; 0 < e; ) {
      var n = 31 - Il(e), c = 1 << n;
      u[n] = -1, e &= ~c;
    }
    a !== 0 && Qi(l, a, t);
  }
  function pn() {
    return (P & 6) === 0 ? (me(0), !1) : !0;
  }
  function xf() {
    if (x !== null) {
      if (al === 0)
        var l = x.return;
      else
        l = x, qt = Ca = null, ef(l), yu = null, $u = 0, l = x;
      for (; l !== null; )
        id(l.alternate, l), l = l.return;
      x = null;
    }
  }
  function zu(l, t) {
    var a = l.timeoutHandle;
    a !== -1 && (l.timeoutHandle = -1, cv(a)), a = l.cancelPendingCommit, a !== null && (l.cancelPendingCommit = null, a()), Jt = 0, xf(), ol = l, x = a = Ht(l.current, null), J = t, al = 0, ut = null, oa = !1, bu = Cu(l, t), jf = !1, Tu = et = Gf = Za = sa = hl = 0, Jl = de = null, Xf = !1, (t & 8) !== 0 && (t |= t & 32);
    var u = l.entangledLanes;
    if (u !== 0)
      for (l = l.entanglements, u &= t; 0 < u; ) {
        var e = 31 - Il(u), n = 1 << e;
        t |= l[e], u &= ~n;
      }
    return Kt = t, Ke(), a;
  }
  function Cd(l, t) {
    Q = null, T.H = ue, t === du || t === Pe ? (t = Wo(), al = 3) : t === Jc ? (t = Wo(), al = 4) : al = t === Ef ? 8 : t !== null && typeof t == "object" && typeof t.then == "function" ? 6 : 1, ut = t, x === null && (hl = 1, vn(
      l,
      st(t, l.current)
    ));
  }
  function Hd() {
    var l = tt.current;
    return l === null ? !0 : (J & 4194048) === J ? vt === null : (J & 62914560) === J || (J & 536870912) !== 0 ? l === vt : !1;
  }
  function Bd() {
    var l = T.H;
    return T.H = ue, l === null ? ue : l;
  }
  function qd() {
    var l = T.A;
    return T.A = Gm, l;
  }
  function On() {
    hl = 4, oa || (J & 4194048) !== J && tt.current !== null || (bu = !0), (sa & 134217727) === 0 && (Za & 134217727) === 0 || ol === null || ma(
      ol,
      J,
      et,
      !1
    );
  }
  function Vf(l, t, a) {
    var u = P;
    P |= 2;
    var e = Bd(), n = qd();
    (ol !== l || J !== t) && (_n = null, zu(l, t)), t = !1;
    var c = hl;
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
        Zm(), c = hl;
        break;
      } catch (b) {
        Cd(l, b);
      }
    while (!0);
    return t && l.shellSuspendCounter++, qt = Ca = null, P = u, T.H = e, T.A = n, x === null && (ol = null, J = 0, Ke()), c;
  }
  function Zm() {
    for (; x !== null; ) Yd(x);
  }
  function Lm(l, t) {
    var a = P;
    P |= 2;
    var u = Bd(), e = qd();
    ol !== l || J !== t ? (_n = null, An = Fl() + 500, zu(l, t)) : bu = Cu(
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
              if (Jo(n)) {
                al = 0, ut = null, jd(t);
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
              Jo(n) ? (al = 0, ut = null, jd(t)) : (al = 0, ut = null, Au(l, t, n, 7));
              break;
            case 5:
              var c = null;
              switch (x.tag) {
                case 26:
                  c = x.memoizedState;
                case 5:
                case 27:
                  var f = x;
                  if (c ? z0(c) : f.stateNode.complete) {
                    al = 0, ut = null;
                    var i = f.sibling;
                    if (i !== null) x = i;
                    else {
                      var h = f.return;
                      h !== null ? (x = h, Mn(h)) : x = null;
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
              xf(), hl = 6;
              break l;
            default:
              throw Error(m(462));
          }
        }
        xm();
        break;
      } catch (b) {
        Cd(l, b);
      }
    while (!0);
    return qt = Ca = null, T.H = u, T.A = e, P = a, x !== null ? 0 : (ol = null, J = 0, Ke(), hl);
  }
  function xm() {
    for (; x !== null && !yy(); )
      Yd(x);
  }
  function Yd(l) {
    var t = cd(l.alternate, l, Kt);
    l.memoizedProps = l.pendingProps, t === null ? Mn(l) : x = t;
  }
  function jd(l) {
    var t = l, a = t.alternate;
    switch (t.tag) {
      case 15:
      case 0:
        t = ld(
          a,
          t,
          t.pendingProps,
          t.type,
          void 0,
          J
        );
        break;
      case 11:
        t = ld(
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
        id(a, t), t = x = qo(t, Kt), t = cd(a, t, Kt);
    }
    l.memoizedProps = l.pendingProps, t === null ? Mn(l) : x = t;
  }
  function Au(l, t, a, u) {
    qt = Ca = null, ef(t), yu = null, $u = 0;
    var e = t.return;
    try {
      if (Rm(
        l,
        e,
        t,
        a,
        J
      )) {
        hl = 1, vn(
          l,
          st(a, l.current)
        ), x = null;
        return;
      }
    } catch (n) {
      if (e !== null) throw x = e, n;
      hl = 1, vn(
        l,
        st(a, l.current)
      ), x = null;
      return;
    }
    t.flags & 32768 ? ($ || u === 1 ? l = !0 : bu || (J & 536870912) !== 0 ? l = !1 : (oa = l = !0, (u === 2 || u === 9 || u === 3 || u === 6) && (u = tt.current, u !== null && u.tag === 13 && (u.flags |= 16384))), Gd(t, l)) : Mn(t);
  }
  function Mn(l) {
    var t = l;
    do {
      if ((t.flags & 32768) !== 0) {
        Gd(
          t,
          oa
        );
        return;
      }
      l = t.return;
      var a = Bm(
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
    hl === 0 && (hl = 5);
  }
  function Gd(l, t) {
    do {
      var a = qm(l.alternate, l);
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
    hl = 6, x = null;
  }
  function Xd(l, t, a, u, e, n, c, f, i) {
    l.cancelPendingCommit = null;
    do
      Dn();
    while (Al !== 0);
    if ((P & 6) !== 0) throw Error(m(327));
    if (t !== null) {
      if (t === l.current) throw Error(m(177));
      if (n = t.lanes | t.childLanes, n |= Rc, zy(
        l,
        a,
        n,
        c,
        f,
        i
      ), l === ol && (x = ol = null, J = 0), Eu = t, ya = l, Jt = a, Qf = n, Zf = e, Dd = u, (t.subtreeFlags & 10256) !== 0 || (t.flags & 10256) !== 0 ? (l.callbackNode = null, l.callbackPriority = 0, wm(Ue, function() {
        return Vd(), null;
      })) : (l.callbackNode = null, l.callbackPriority = 0), u = (t.flags & 13878) !== 0, (t.subtreeFlags & 13878) !== 0 || u) {
        u = T.T, T.T = null, e = O.p, O.p = 2, c = P, P |= 4;
        try {
          Ym(l, t, a);
        } finally {
          P = c, O.p = e, T.T = u;
        }
      }
      Al = 1, Qd(), Zd(), Ld();
    }
  }
  function Qd() {
    if (Al === 1) {
      Al = 0;
      var l = ya, t = Eu, a = (t.flags & 13878) !== 0;
      if ((t.subtreeFlags & 13878) !== 0 || a) {
        a = T.T, T.T = null;
        var u = O.p;
        O.p = 2;
        var e = P;
        P |= 4;
        try {
          Td(t, l);
          var n = ai, c = Oo(l.containerInfo), f = n.focusedElem, i = n.selectionRange;
          if (c !== f && f && f.ownerDocument && po(
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
                  var y = _o(
                    f,
                    B
                  ), s = _o(
                    f,
                    fl
                  );
                  if (y && s && (r.rangeCount !== 1 || r.anchorNode !== y.node || r.anchorOffset !== y.offset || r.focusNode !== s.node || r.focusOffset !== s.offset)) {
                    var v = A.createRange();
                    v.setStart(y.node, y.offset), r.removeAllRanges(), B > fl ? (r.addRange(v), r.extend(s.node, s.offset)) : (v.setEnd(s.node, s.offset), r.addRange(v));
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
              var E = A[f];
              E.element.scrollLeft = E.left, E.element.scrollTop = E.top;
            }
          }
          Qn = !!ti, ai = ti = null;
        } finally {
          P = e, O.p = u, T.T = a;
        }
      }
      l.current = t, Al = 2;
    }
  }
  function Zd() {
    if (Al === 2) {
      Al = 0;
      var l = ya, t = Eu, a = (t.flags & 8772) !== 0;
      if ((t.subtreeFlags & 8772) !== 0 || a) {
        a = T.T, T.T = null;
        var u = O.p;
        O.p = 2;
        var e = P;
        P |= 4;
        try {
          hd(l, t.alternate, t);
        } finally {
          P = e, O.p = u, T.T = a;
        }
      }
      Al = 3;
    }
  }
  function Ld() {
    if (Al === 4 || Al === 3) {
      Al = 0, my();
      var l = ya, t = Eu, a = Jt, u = Dd;
      (t.subtreeFlags & 10256) !== 0 || (t.flags & 10256) !== 0 ? Al = 5 : (Al = 0, Eu = ya = null, xd(l, l.pendingLanes));
      var e = l.pendingLanes;
      if (e === 0 && (da = null), fc(a), t = t.stateNode, kl && typeof kl.onCommitFiberRoot == "function")
        try {
          kl.onCommitFiberRoot(
            Ru,
            t,
            void 0,
            (t.current.flags & 128) === 128
          );
        } catch {
        }
      if (u !== null) {
        t = T.T, e = O.p, O.p = 2, T.T = null;
        try {
          for (var n = l.onRecoverableError, c = 0; c < u.length; c++) {
            var f = u[c];
            n(f.value, {
              componentStack: f.stack
            });
          }
        } finally {
          T.T = t, O.p = e;
        }
      }
      (Jt & 3) !== 0 && Dn(), Mt(l), e = l.pendingLanes, (a & 261930) !== 0 && (e & 42) !== 0 ? l === Lf ? ye++ : (ye = 0, Lf = l) : ye = 0, me(0);
    }
  }
  function xd(l, t) {
    (l.pooledCacheLanes &= t) === 0 && (t = l.pooledCache, t != null && (l.pooledCache = null, wu(t)));
  }
  function Dn() {
    return Qd(), Zd(), Ld(), Vd();
  }
  function Vd() {
    if (Al !== 5) return !1;
    var l = ya, t = Qf;
    Qf = 0;
    var a = fc(Jt), u = T.T, e = O.p;
    try {
      O.p = 32 > a ? 32 : a, T.T = null, a = Zf, Zf = null;
      var n = ya, c = Jt;
      if (Al = 0, Eu = ya = null, Jt = 0, (P & 6) !== 0) throw Error(m(331));
      var f = P;
      if (P |= 4, pd(n.current), zd(
        n,
        n.current,
        c,
        a
      ), P = f, me(0, !1), kl && typeof kl.onPostCommitFiberRoot == "function")
        try {
          kl.onPostCommitFiberRoot(Ru, n);
        } catch {
        }
      return !0;
    } finally {
      O.p = e, T.T = u, xd(l, t);
    }
  }
  function Kd(l, t, a) {
    t = st(a, t), t = Tf(l.stateNode, t, 2), l = na(l, t, 2), l !== null && (Hu(l, 2), Mt(l));
  }
  function ul(l, t, a) {
    if (l.tag === 3)
      Kd(l, l, a);
    else
      for (; t !== null; ) {
        if (t.tag === 3) {
          Kd(
            t,
            l,
            a
          );
          break;
        } else if (t.tag === 1) {
          var u = t.stateNode;
          if (typeof t.type.getDerivedStateFromError == "function" || typeof u.componentDidCatch == "function" && (da === null || !da.has(u))) {
            l = st(a, l), a = Js(2), u = na(t, a, 2), u !== null && (ws(
              a,
              u,
              t,
              l
            ), Hu(u, 2), Mt(u));
            break;
          }
        }
        t = t.return;
      }
  }
  function Kf(l, t, a) {
    var u = l.pingCache;
    if (u === null) {
      u = l.pingCache = new Xm();
      var e = /* @__PURE__ */ new Set();
      u.set(t, e);
    } else
      e = u.get(t), e === void 0 && (e = /* @__PURE__ */ new Set(), u.set(t, e));
    e.has(a) || (jf = !0, e.add(a), l = Vm.bind(null, l, t, a), t.then(l, l));
  }
  function Vm(l, t, a) {
    var u = l.pingCache;
    u !== null && u.delete(t), l.pingedLanes |= l.suspendedLanes & a, l.warmLanes &= ~a, ol === l && (J & a) === a && (hl === 4 || hl === 3 && (J & 62914560) === J && 300 > Fl() - zn ? (P & 2) === 0 && zu(l, 0) : Gf |= a, Tu === J && (Tu = 0)), Mt(l);
  }
  function Jd(l, t) {
    t === 0 && (t = Xi()), l = Ua(l, t), l !== null && (Hu(l, t), Mt(l));
  }
  function Km(l) {
    var t = l.memoizedState, a = 0;
    t !== null && (a = t.retryLane), Jd(l, a);
  }
  function Jm(l, t) {
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
        throw Error(m(314));
    }
    u !== null && u.delete(t), Jd(l, a);
  }
  function wm(l, t) {
    return uc(l, t);
  }
  var Un = null, _u = null, Jf = !1, Nn = !1, wf = !1, va = 0;
  function Mt(l) {
    l !== _u && l.next === null && (_u === null ? Un = _u = l : _u = _u.next = l), Nn = !0, Jf || (Jf = !0, $m());
  }
  function me(l, t) {
    if (!wf && Nn) {
      wf = !0;
      do
        for (var a = !1, u = Un; u !== null; ) {
          if (l !== 0) {
            var e = u.pendingLanes;
            if (e === 0) var n = 0;
            else {
              var c = u.suspendedLanes, f = u.pingedLanes;
              n = (1 << 31 - Il(42 | l) + 1) - 1, n &= e & ~(c & ~f), n = n & 201326741 ? n & 201326741 | 1 : n ? n | 2 : 0;
            }
            n !== 0 && (a = !0, Fd(u, n));
          } else
            n = J, n = He(
              u,
              u === ol ? n : 0,
              u.cancelPendingCommit !== null || u.timeoutHandle !== -1
            ), (n & 3) === 0 || Cu(u, n) || (a = !0, Fd(u, n));
          u = u.next;
        }
      while (a);
      wf = !1;
    }
  }
  function Wm() {
    wd();
  }
  function wd() {
    Nn = Jf = !1;
    var l = 0;
    va !== 0 && nv() && (l = va);
    for (var t = Fl(), a = null, u = Un; u !== null; ) {
      var e = u.next, n = Wd(u, t);
      n === 0 ? (u.next = null, a === null ? Un = e : a.next = e, e === null && (_u = a)) : (a = u, (l !== 0 || (n & 3) !== 0) && (Nn = !0)), u = e;
    }
    Al !== 0 && Al !== 5 || me(l), va !== 0 && (va = 0);
  }
  function Wd(l, t) {
    for (var a = l.suspendedLanes, u = l.pingedLanes, e = l.expirationTimes, n = l.pendingLanes & -62914561; 0 < n; ) {
      var c = 31 - Il(n), f = 1 << c, i = e[c];
      i === -1 ? ((f & a) === 0 || (f & u) !== 0) && (e[c] = Ey(f, t)) : i <= t && (l.expiredLanes |= f), n &= ~f;
    }
    if (t = ol, a = J, a = He(
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
          a = ji;
          break;
        case 32:
          a = Ue;
          break;
        case 268435456:
          a = Gi;
          break;
        default:
          a = Ue;
      }
      return u = $d.bind(null, l), a = uc(a, u), l.callbackPriority = t, l.callbackNode = a, t;
    }
    return u !== null && u !== null && ec(u), l.callbackPriority = 2, l.callbackNode = null, 2;
  }
  function $d(l, t) {
    if (Al !== 0 && Al !== 5)
      return l.callbackNode = null, l.callbackPriority = 0, null;
    var a = l.callbackNode;
    if (Dn() && l.callbackNode !== a)
      return null;
    var u = J;
    return u = He(
      l,
      l === ol ? u : 0,
      l.cancelPendingCommit !== null || l.timeoutHandle !== -1
    ), u === 0 ? null : (Nd(l, u, t), Wd(l, Fl()), l.callbackNode != null && l.callbackNode === a ? $d.bind(null, l) : null);
  }
  function Fd(l, t) {
    if (Dn()) return null;
    Nd(l, t, !0);
  }
  function $m() {
    fv(function() {
      (P & 6) !== 0 ? uc(
        Yi,
        Wm
      ) : wd();
    });
  }
  function Wf() {
    if (va === 0) {
      var l = ou;
      l === 0 && (l = Ne, Ne <<= 1, (Ne & 261888) === 0 && (Ne = 256)), va = l;
    }
    return va;
  }
  function kd(l) {
    return l == null || typeof l == "symbol" || typeof l == "boolean" ? null : typeof l == "function" ? l : je("" + l);
  }
  function Id(l, t) {
    var a = t.ownerDocument.createElement("input");
    return a.name = t.name, a.value = t.value, l.id && a.setAttribute("form", l.id), t.parentNode.insertBefore(a, t), l = new FormData(l), a.parentNode.removeChild(a), l;
  }
  function Fm(l, t, a, u, e) {
    if (t === "submit" && a && a.stateNode === e) {
      var n = kd(
        (e[Zl] || null).action
      ), c = u.submitter;
      c && (t = (t = c[Zl] || null) ? kd(t.formAction) : c.getAttribute("formAction"), t !== null && (n = t, c = null));
      var f = new Ze(
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
                if (va !== 0) {
                  var i = c ? Id(e, c) : new FormData(e);
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
                typeof n == "function" && (f.preventDefault(), i = c ? Id(e, c) : new FormData(e), vf(
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
    var Ff = Nc[$f], km = Ff.toLowerCase(), Im = Ff[0].toUpperCase() + Ff.slice(1);
    gt(
      km,
      "on" + Im
    );
  }
  gt(Uo, "onAnimationEnd"), gt(No, "onAnimationIteration"), gt(Ro, "onAnimationStart"), gt("dblclick", "onDoubleClick"), gt("focusin", "onFocus"), gt("focusout", "onBlur"), gt(vm, "onTransitionRun"), gt(hm, "onTransitionStart"), gt(Sm, "onTransitionCancel"), gt(Co, "onTransitionEnd"), $a("onMouseEnter", ["mouseout", "mouseover"]), $a("onMouseLeave", ["mouseout", "mouseover"]), $a("onPointerEnter", ["pointerout", "pointerover"]), $a("onPointerLeave", ["pointerout", "pointerover"]), pa(
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
  var ve = "abort canplay canplaythrough durationchange emptied encrypted ended error loadeddata loadedmetadata loadstart pause play playing progress ratechange resize seeked seeking stalled suspend timeupdate volumechange waiting".split(
    " "
  ), Pm = new Set(
    "beforetoggle cancel close invalid load scroll scrollend toggle".split(" ").concat(ve)
  );
  function Pd(l, t) {
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
              Ve(b);
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
              Ve(b);
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
    a.has(u) || (l0(t, l, 2, !1), a.add(u));
  }
  function kf(l, t, a) {
    var u = 0;
    t && (u |= 4), l0(
      a,
      l,
      u,
      t
    );
  }
  var Rn = "_reactListening" + Math.random().toString(36).slice(2);
  function If(l) {
    if (!l[Rn]) {
      l[Rn] = !0, Ji.forEach(function(a) {
        a !== "selectionchange" && (Pm.has(a) || kf(a, !1, l), kf(a, !0, l));
      });
      var t = l.nodeType === 9 ? l : l.ownerDocument;
      t === null || t[Rn] || (t[Rn] = !0, kf("selectionchange", !1, t));
    }
  }
  function l0(l, t, a, u) {
    switch (U0(t)) {
      case 2:
        var e = Mv;
        break;
      case 8:
        e = Dv;
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
    eo(function() {
      var h = n, b = hc(a), A = [];
      l: {
        var S = Ho.get(l);
        if (S !== void 0) {
          var r = Ze, U = l;
          switch (l) {
            case "keypress":
              if (Xe(a) === 0) break l;
            case "keydown":
            case "keyup":
              r = Jy;
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
              r = fo;
              break;
            case "drag":
            case "dragend":
            case "dragenter":
            case "dragexit":
            case "dragleave":
            case "dragover":
            case "dragstart":
            case "drop":
              r = By;
              break;
            case "touchcancel":
            case "touchend":
            case "touchmove":
            case "touchstart":
              r = $y;
              break;
            case Uo:
            case No:
            case Ro:
              r = jy;
              break;
            case Co:
              r = ky;
              break;
            case "scroll":
            case "scrollend":
              r = Cy;
              break;
            case "wheel":
              r = Py;
              break;
            case "copy":
            case "cut":
            case "paste":
              r = Xy;
              break;
            case "gotpointercapture":
            case "lostpointercapture":
            case "pointercancel":
            case "pointerdown":
            case "pointermove":
            case "pointerout":
            case "pointerover":
            case "pointerup":
              r = oo;
              break;
            case "toggle":
            case "beforetoggle":
              r = tm;
          }
          var B = (t & 4) !== 0, fl = !B && (l === "scroll" || l === "scrollend"), y = B ? S !== null ? S + "Capture" : null : S;
          B = [];
          for (var s = h, v; s !== null; ) {
            var E = s;
            if (v = E.stateNode, E = E.tag, E !== 5 && E !== 26 && E !== 27 || v === null || y === null || (E = Yu(s, y), E != null && B.push(
              he(s, E, v)
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
          if ((r || S) && (S = b.window === b ? b : (S = b.ownerDocument) ? S.defaultView || S.parentWindow : window, r ? (U = a.relatedTarget || a.toElement, r = h, U = U ? Ja(U) : null, U !== null && (fl = G(U), B = U.tag, U !== fl || B !== 5 && B !== 27 && B !== 6) && (U = null)) : (r = null, U = h), r !== U)) {
            if (B = fo, E = "onMouseLeave", y = "onMouseEnter", s = "mouse", (l === "pointerout" || l === "pointerover") && (B = oo, E = "onPointerLeave", y = "onPointerEnter", s = "pointer"), fl = r == null ? S : qu(r), v = U == null ? S : qu(U), S = new B(
              E,
              s + "leave",
              r,
              a,
              b
            ), S.target = fl, S.relatedTarget = v, E = null, Ja(b) === h && (B = new B(
              y,
              s + "enter",
              U,
              a,
              b
            ), B.target = v, B.relatedTarget = fl, E = B), fl = E, r && U)
              t: {
                for (B = lv, y = r, s = U, v = 0, E = y; E; E = B(E))
                  v++;
                E = 0;
                for (var H = s; H; H = B(H))
                  E++;
                for (; 0 < v - E; )
                  y = B(y), v--;
                for (; 0 < E - v; )
                  s = B(s), E--;
                for (; v--; ) {
                  if (y === s || s !== null && y === s.alternate) {
                    B = y;
                    break t;
                  }
                  y = B(y), s = B(s);
                }
                B = null;
              }
            else B = null;
            r !== null && t0(
              A,
              S,
              r,
              B,
              !1
            ), U !== null && fl !== null && t0(
              A,
              fl,
              U,
              B,
              !0
            );
          }
        }
        l: {
          if (S = h ? qu(h) : window, r = S.nodeName && S.nodeName.toLowerCase(), r === "select" || r === "input" && S.type === "file")
            var k = go;
          else if (So(S))
            if (bo)
              k = dm;
            else {
              k = om;
              var C = im;
            }
          else
            r = S.nodeName, !r || r.toLowerCase() !== "input" || S.type !== "checkbox" && S.type !== "radio" ? h && mc(h.elementType) && (k = go) : k = sm;
          if (k && (k = k(l, h))) {
            ro(
              A,
              k,
              a,
              b
            );
            break l;
          }
          C && C(l, S, h), l === "focusout" && h && S.type === "number" && h.memoizedProps.value != null && yc(S, "number", S.value);
        }
        switch (C = h ? qu(h) : window, l) {
          case "focusin":
            (So(C) || C.contentEditable === "true") && (tu = C, Mc = h, Vu = null);
            break;
          case "focusout":
            Vu = Mc = tu = null;
            break;
          case "mousedown":
            Dc = !0;
            break;
          case "contextmenu":
          case "mouseup":
          case "dragend":
            Dc = !1, Mo(A, a, b);
            break;
          case "selectionchange":
            if (mm) break;
          case "keydown":
          case "keyup":
            Mo(A, a, b);
        }
        var Z;
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
          lu ? vo(l, a) && (w = "onCompositionEnd") : l === "keydown" && a.keyCode === 229 && (w = "onCompositionStart");
        w && (so && a.locale !== "ko" && (lu || w !== "onCompositionStart" ? w === "onCompositionEnd" && lu && (Z = no()) : (It = b, gc = "value" in It ? It.value : It.textContent, lu = !0)), C = Cn(h, w), 0 < C.length && (w = new io(
          w,
          l,
          null,
          a,
          b
        ), A.push({ event: w, listeners: C }), Z ? w.data = Z : (Z = ho(a), Z !== null && (w.data = Z)))), (Z = um ? em(l, a) : nm(l, a)) && (w = Cn(h, "onBeforeInput"), 0 < w.length && (C = new io(
          "onBeforeInput",
          "beforeinput",
          null,
          a,
          b
        ), A.push({
          event: C,
          listeners: w
        }), C.data = Z)), Fm(
          A,
          l,
          h,
          a,
          b
        );
      }
      Pd(A, t);
    });
  }
  function he(l, t, a) {
    return {
      instance: l,
      listener: t,
      currentTarget: a
    };
  }
  function Cn(l, t) {
    for (var a = t + "Capture", u = []; l !== null; ) {
      var e = l, n = e.stateNode;
      if (e = e.tag, e !== 5 && e !== 26 && e !== 27 || n === null || (e = Yu(l, a), e != null && u.unshift(
        he(l, e, n)
      ), e = Yu(l, t), e != null && u.push(
        he(l, e, n)
      )), l.tag === 3) return u;
      l = l.return;
    }
    return [];
  }
  function lv(l) {
    if (l === null) return null;
    do
      l = l.return;
    while (l && l.tag !== 5 && l.tag !== 27);
    return l || null;
  }
  function t0(l, t, a, u, e) {
    for (var n = t._reactName, c = []; a !== null && a !== u; ) {
      var f = a, i = f.alternate, h = f.stateNode;
      if (f = f.tag, i !== null && i === u) break;
      f !== 5 && f !== 26 && f !== 27 || h === null || (i = h, e ? (h = Yu(a, n), h != null && c.unshift(
        he(a, h, i)
      )) : e || (h = Yu(a, n), h != null && c.push(
        he(a, h, i)
      ))), a = a.return;
    }
    c.length !== 0 && l.push({ event: t, listeners: c });
  }
  var tv = /\r\n?/g, av = /\u0000|\uFFFD/g;
  function a0(l) {
    return (typeof l == "string" ? l : "" + l).replace(tv, `
`).replace(av, "");
  }
  function u0(l, t) {
    return t = a0(t), a0(l) === t;
  }
  function cl(l, t, a, u, e, n) {
    switch (a) {
      case "children":
        typeof u == "string" ? t === "body" || t === "textarea" && u === "" || ka(l, u) : (typeof u == "number" || typeof u == "bigint") && t !== "body" && ka(l, "" + u);
        break;
      case "className":
        qe(l, "class", u);
        break;
      case "tabIndex":
        qe(l, "tabindex", u);
        break;
      case "dir":
      case "role":
      case "viewBox":
      case "width":
      case "height":
        qe(l, a, u);
        break;
      case "style":
        ao(l, u, n);
        break;
      case "data":
        if (t !== "object") {
          qe(l, "data", u);
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
        u = je("" + u), l.setAttribute(a, u);
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
        u = je("" + u), l.setAttribute(a, u);
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
            throw Error(m(61));
          if (a = u.__html, a != null) {
            if (e.children != null) throw Error(m(60));
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
        a = je("" + u), l.setAttributeNS(
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
        V("beforetoggle", l), V("toggle", l), Be(l, "popover", u);
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
        Be(l, "is", u);
        break;
      case "innerText":
      case "textContent":
        break;
      default:
        (!(2 < a.length) || a[0] !== "o" && a[0] !== "O" || a[1] !== "n" && a[1] !== "N") && (a = Ny.get(a) || a, Be(l, a, u));
    }
  }
  function li(l, t, a, u, e, n) {
    switch (a) {
      case "style":
        ao(l, u, n);
        break;
      case "dangerouslySetInnerHTML":
        if (u != null) {
          if (typeof u != "object" || !("__html" in u))
            throw Error(m(61));
          if (a = u.__html, a != null) {
            if (e.children != null) throw Error(m(60));
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
        if (!wi.hasOwnProperty(a))
          l: {
            if (a[0] === "o" && a[1] === "n" && (e = a.endsWith("Capture"), t = a.slice(2, e ? a.length - 7 : void 0), n = l[Zl] || null, n = n != null ? n[a] : null, typeof n == "function" && l.removeEventListener(t, n, e), typeof u == "function")) {
              typeof n != "function" && n !== null && (a in l ? l[a] = null : l.hasAttribute(a) && l.removeAttribute(a)), l.addEventListener(t, u, e);
              break l;
            }
            a in l ? l[a] = u : u === !0 ? l.setAttribute(a, "") : Be(l, a, u);
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
                  throw Error(m(137, t));
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
                    throw Error(m(137, t));
                  break;
                default:
                  cl(l, t, u, b, a, null);
              }
          }
        Ii(
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
                if (f != null) throw Error(m(91));
                break;
              default:
                cl(l, t, c, f, a, null);
            }
        lo(l, u, e, n);
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
        for (u = 0; u < ve.length; u++)
          V(ve[u], l);
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
                throw Error(m(137, t));
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
  function uv(l, t, a, u) {
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
                  throw Error(m(137, t));
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
                if (e != null) throw Error(m(91));
                break;
              default:
                e !== n && cl(l, t, c, e, u, n);
            }
        Pi(l, S, r);
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
                  throw Error(m(137, t));
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
    for (var y in a)
      S = a[y], a.hasOwnProperty(y) && S != null && !u.hasOwnProperty(y) && cl(l, t, y, null, u, S);
    for (A in u)
      S = u[A], r = a[A], !u.hasOwnProperty(A) || S === r || S == null && r == null || cl(l, t, A, S, u, r);
  }
  function e0(l) {
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
  function ev() {
    if (typeof performance.getEntriesByType == "function") {
      for (var l = 0, t = 0, a = performance.getEntriesByType("resource"), u = 0; u < a.length; u++) {
        var e = a[u], n = e.transferSize, c = e.initiatorType, f = e.duration;
        if (n && f && e0(c)) {
          for (c = 0, f = e.responseEnd, u += 1; u < a.length; u++) {
            var i = a[u], h = i.startTime;
            if (h > f) break;
            var b = i.transferSize, A = i.initiatorType;
            b && e0(A) && (i = i.responseEnd, c += b * (i < f ? 1 : (f - h) / (i - h)));
          }
          if (--u, t += 8 * (n + c) / (e.duration / 1e3), l++, 10 < l) break;
        }
      }
      if (0 < l) return t / l / 1e6;
    }
    return navigator.connection && (l = navigator.connection.downlink, typeof l == "number") ? l : 5;
  }
  var ti = null, ai = null;
  function Hn(l) {
    return l.nodeType === 9 ? l : l.ownerDocument;
  }
  function n0(l) {
    switch (l) {
      case "http://www.w3.org/2000/svg":
        return 1;
      case "http://www.w3.org/1998/Math/MathML":
        return 2;
      default:
        return 0;
    }
  }
  function c0(l, t) {
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
  function nv() {
    var l = window.event;
    return l && l.type === "popstate" ? l === ei ? !1 : (ei = l, !0) : (ei = null, !1);
  }
  var f0 = typeof setTimeout == "function" ? setTimeout : void 0, cv = typeof clearTimeout == "function" ? clearTimeout : void 0, i0 = typeof Promise == "function" ? Promise : void 0, fv = typeof queueMicrotask == "function" ? queueMicrotask : typeof i0 < "u" ? function(l) {
    return i0.resolve(null).then(l).catch(iv);
  } : f0;
  function iv(l) {
    setTimeout(function() {
      throw l;
    });
  }
  function ha(l) {
    return l === "head";
  }
  function o0(l, t) {
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
          Se(l.ownerDocument.documentElement);
        else if (a === "head") {
          a = l.ownerDocument.head, Se(a);
          for (var n = a.firstChild; n; ) {
            var c = n.nextSibling, f = n.nodeName;
            n[Bu] || f === "SCRIPT" || f === "STYLE" || f === "LINK" && n.rel.toLowerCase() === "stylesheet" || a.removeChild(n), n = c;
          }
        } else
          a === "body" && Se(l.ownerDocument.body);
      a = e;
    } while (a);
    Du(t);
  }
  function s0(l, t) {
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
  function ov(l, t, a, u) {
    for (; l.nodeType === 1; ) {
      var e = a;
      if (l.nodeName.toLowerCase() !== t.toLowerCase()) {
        if (!u && (l.nodeName !== "INPUT" || l.type !== "hidden"))
          break;
      } else if (u) {
        if (!l[Bu])
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
  function sv(l, t, a) {
    if (t === "") return null;
    for (; l.nodeType !== 3; )
      if ((l.nodeType !== 1 || l.nodeName !== "INPUT" || l.type !== "hidden") && !a || (l = ht(l.nextSibling), l === null)) return null;
    return l;
  }
  function d0(l, t) {
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
  function dv(l, t) {
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
  function y0(l) {
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
  function m0(l) {
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
  function v0(l, t, a) {
    switch (t = Hn(a), l) {
      case "html":
        if (l = t.documentElement, !l) throw Error(m(452));
        return l;
      case "head":
        if (l = t.head, !l) throw Error(m(453));
        return l;
      case "body":
        if (l = t.body, !l) throw Error(m(454));
        return l;
      default:
        throw Error(m(451));
    }
  }
  function Se(l) {
    for (var t = l.attributes; t.length; )
      l.removeAttributeNode(t[0]);
    oc(l);
  }
  var St = /* @__PURE__ */ new Map(), h0 = /* @__PURE__ */ new Set();
  function Bn(l) {
    return typeof l.getRootNode == "function" ? l.getRootNode() : l.nodeType === 9 ? l : l.ownerDocument;
  }
  var wt = O.d;
  O.d = {
    f: yv,
    r: mv,
    D: vv,
    C: hv,
    L: Sv,
    m: rv,
    X: bv,
    S: gv,
    M: Tv
  };
  function yv() {
    var l = wt.f(), t = pn();
    return l || t;
  }
  function mv(l) {
    var t = wa(l);
    t !== null && t.tag === 5 && t.type === "form" ? Cs(t) : wt.r(l);
  }
  var pu = typeof document > "u" ? null : document;
  function S0(l, t, a) {
    var u = pu;
    if (u && typeof t == "string" && t) {
      var e = it(t);
      e = 'link[rel="' + l + '"][href="' + e + '"]', typeof a == "string" && (e += '[crossorigin="' + a + '"]'), h0.has(e) || (h0.add(e), l = { rel: l, crossOrigin: a, href: t }, u.querySelector(e) === null && (t = u.createElement("link"), Nl(t, "link", l), _l(t), u.head.appendChild(t)));
    }
  }
  function vv(l) {
    wt.D(l), S0("dns-prefetch", l, null);
  }
  function hv(l, t) {
    wt.C(l, t), S0("preconnect", l, t);
  }
  function Sv(l, t, a) {
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
      ), St.set(n, l), u.querySelector(e) !== null || t === "style" && u.querySelector(re(n)) || t === "script" && u.querySelector(ge(n)) || (t = u.createElement("link"), Nl(t, "link", l), _l(t), u.head.appendChild(t)));
    }
  }
  function rv(l, t) {
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
            if (a.querySelector(ge(n)))
              return;
        }
        u = a.createElement("link"), Nl(u, "link", l), _l(u), a.head.appendChild(u);
      }
    }
  }
  function gv(l, t, a) {
    wt.S(l, t, a);
    var u = pu;
    if (u && l) {
      var e = Wa(u).hoistableStyles, n = Ou(l);
      t = t || "default";
      var c = e.get(n);
      if (!c) {
        var f = { loading: 0, preload: null };
        if (c = u.querySelector(
          re(n)
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
          }), f.loading |= 4, qn(c, t, u);
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
  function bv(l, t) {
    wt.X(l, t);
    var a = pu;
    if (a && l) {
      var u = Wa(a).hoistableScripts, e = Mu(l), n = u.get(e);
      n || (n = a.querySelector(ge(e)), n || (l = q({ src: l, async: !0 }, t), (t = St.get(e)) && si(l, t), n = a.createElement("script"), _l(n), Nl(n, "link", l), a.head.appendChild(n)), n = {
        type: "script",
        instance: n,
        count: 1,
        state: null
      }, u.set(e, n));
    }
  }
  function Tv(l, t) {
    wt.M(l, t);
    var a = pu;
    if (a && l) {
      var u = Wa(a).hoistableScripts, e = Mu(l), n = u.get(e);
      n || (n = a.querySelector(ge(e)), n || (l = q({ src: l, async: !0, type: "module" }, t), (t = St.get(e)) && si(l, t), n = a.createElement("script"), _l(n), Nl(n, "link", l), a.head.appendChild(n)), n = {
        type: "script",
        instance: n,
        count: 1,
        state: null
      }, u.set(e, n));
    }
  }
  function r0(l, t, a, u) {
    var e = (e = L.current) ? Bn(e) : null;
    if (!e) throw Error(m(446));
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
            re(l)
          )) && !n._p && (c.instance = n, c.state.loading = 5), St.has(l) || (a = {
            rel: "preload",
            as: "style",
            href: a.href,
            crossOrigin: a.crossOrigin,
            integrity: a.integrity,
            media: a.media,
            hrefLang: a.hrefLang,
            referrerPolicy: a.referrerPolicy
          }, St.set(l, a), n || Ev(
            e,
            l,
            a,
            c.state
          ))), t && u === null)
            throw Error(m(528, ""));
          return c;
        }
        if (t && u !== null)
          throw Error(m(529, ""));
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
        throw Error(m(444, l));
    }
  }
  function Ou(l) {
    return 'href="' + it(l) + '"';
  }
  function re(l) {
    return 'link[rel="stylesheet"][' + l + "]";
  }
  function g0(l) {
    return q({}, l, {
      "data-precedence": l.precedence,
      precedence: null
    });
  }
  function Ev(l, t, a, u) {
    l.querySelector('link[rel="preload"][as="style"][' + t + "]") ? u.loading = 1 : (t = l.createElement("link"), u.preload = t, t.addEventListener("load", function() {
      return u.loading |= 1;
    }), t.addEventListener("error", function() {
      return u.loading |= 2;
    }), Nl(t, "link", a), _l(t), l.head.appendChild(t));
  }
  function Mu(l) {
    return '[src="' + it(l) + '"]';
  }
  function ge(l) {
    return "script[async]" + l;
  }
  function b0(l, t, a) {
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
          ), _l(u), Nl(u, "style", e), qn(u, a.precedence, l), t.instance = u;
        case "stylesheet":
          e = Ou(a.href);
          var n = l.querySelector(
            re(e)
          );
          if (n)
            return t.state.loading |= 4, t.instance = n, _l(n), n;
          u = g0(a), (e = St.get(e)) && oi(u, e), n = (l.ownerDocument || l).createElement("link"), _l(n);
          var c = n;
          return c._p = new Promise(function(f, i) {
            c.onload = f, c.onerror = i;
          }), Nl(n, "link", u), t.state.loading |= 4, qn(n, a.precedence, l), t.instance = n;
        case "script":
          return n = Mu(a.src), (e = l.querySelector(
            ge(n)
          )) ? (t.instance = e, _l(e), e) : (u = a, (e = St.get(n)) && (u = q({}, a), si(u, e)), l = l.ownerDocument || l, e = l.createElement("script"), _l(e), Nl(e, "link", u), l.head.appendChild(e), t.instance = e);
        case "void":
          return null;
        default:
          throw Error(m(443, t.type));
      }
    else
      t.type === "stylesheet" && (t.state.loading & 4) === 0 && (u = t.instance, t.state.loading |= 4, qn(u, a.precedence, l));
    return t.instance;
  }
  function qn(l, t, a) {
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
  var Yn = null;
  function T0(l, t, a) {
    if (Yn === null) {
      var u = /* @__PURE__ */ new Map(), e = Yn = /* @__PURE__ */ new Map();
      e.set(a, u);
    } else
      e = Yn, u = e.get(a), u || (u = /* @__PURE__ */ new Map(), e.set(a, u));
    if (u.has(l)) return u;
    for (u.set(l, null), a = a.getElementsByTagName(l), e = 0; e < a.length; e++) {
      var n = a[e];
      if (!(n[Bu] || n[Ol] || l === "link" && n.getAttribute("rel") === "stylesheet") && n.namespaceURI !== "http://www.w3.org/2000/svg") {
        var c = n.getAttribute(t) || "";
        c = l + c;
        var f = u.get(c);
        f ? f.push(n) : u.set(c, [n]);
      }
    }
    return u;
  }
  function E0(l, t, a) {
    l = l.ownerDocument || l, l.head.insertBefore(
      a,
      t === "title" ? l.querySelector("head > title") : null
    );
  }
  function zv(l, t, a) {
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
  function z0(l) {
    return !(l.type === "stylesheet" && (l.state.loading & 3) === 0);
  }
  function Av(l, t, a, u) {
    if (a.type === "stylesheet" && (typeof u.media != "string" || matchMedia(u.media).matches !== !1) && (a.state.loading & 4) === 0) {
      if (a.instance === null) {
        var e = Ou(u.href), n = t.querySelector(
          re(e)
        );
        if (n) {
          t = n._p, t !== null && typeof t == "object" && typeof t.then == "function" && (l.count++, l = jn.bind(l), t.then(l, l)), a.state.loading |= 4, a.instance = n, _l(n);
          return;
        }
        n = t.ownerDocument || t, u = g0(u), (e = St.get(e)) && oi(u, e), n = n.createElement("link"), _l(n);
        var c = n;
        c._p = new Promise(function(f, i) {
          c.onload = f, c.onerror = i;
        }), Nl(n, "link", u), a.instance = n;
      }
      l.stylesheets === null && (l.stylesheets = /* @__PURE__ */ new Map()), l.stylesheets.set(a, t), (t = a.state.preload) && (a.state.loading & 3) === 0 && (l.count++, a = jn.bind(l), t.addEventListener("load", a), t.addEventListener("error", a));
    }
  }
  var di = 0;
  function _v(l, t) {
    return l.stylesheets && l.count === 0 && Xn(l, l.stylesheets), 0 < l.count || 0 < l.imgCount ? function(a) {
      var u = setTimeout(function() {
        if (l.stylesheets && Xn(l, l.stylesheets), l.unsuspend) {
          var n = l.unsuspend;
          l.unsuspend = null, n();
        }
      }, 6e4 + t);
      0 < l.imgBytes && di === 0 && (di = 62500 * ev());
      var e = setTimeout(
        function() {
          if (l.waitingForImages = !1, l.count === 0 && (l.stylesheets && Xn(l, l.stylesheets), l.unsuspend)) {
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
  function jn() {
    if (this.count--, this.count === 0 && (this.imgCount === 0 || !this.waitingForImages)) {
      if (this.stylesheets) Xn(this, this.stylesheets);
      else if (this.unsuspend) {
        var l = this.unsuspend;
        this.unsuspend = null, l();
      }
    }
  }
  var Gn = null;
  function Xn(l, t) {
    l.stylesheets = null, l.unsuspend !== null && (l.count++, Gn = /* @__PURE__ */ new Map(), t.forEach(pv, l), Gn = null, jn.call(l));
  }
  function pv(l, t) {
    if (!(t.state.loading & 4)) {
      var a = Gn.get(l);
      if (a) var u = a.get(null);
      else {
        a = /* @__PURE__ */ new Map(), Gn.set(l, a);
        for (var e = l.querySelectorAll(
          "link[data-precedence],style[data-precedence]"
        ), n = 0; n < e.length; n++) {
          var c = e[n];
          (c.nodeName === "LINK" || c.getAttribute("media") !== "not all") && (a.set(c.dataset.precedence, c), u = c);
        }
        u && a.set(null, u);
      }
      e = t.instance, c = e.getAttribute("data-precedence"), n = a.get(c) || u, n === u && a.set(null, e), a.set(c, e), this.count++, u = jn.bind(this), e.addEventListener("load", u), e.addEventListener("error", u), n ? n.parentNode.insertBefore(e, n.nextSibling) : (l = l.nodeType === 9 ? l.head : l, l.insertBefore(e, l.firstChild)), t.state.loading |= 4;
    }
  }
  var be = {
    $$typeof: Rl,
    Provider: null,
    Consumer: null,
    _currentValue: Y,
    _currentValue2: Y,
    _threadCount: 0
  };
  function Ov(l, t, a, u, e, n, c, f, i) {
    this.tag = 1, this.containerInfo = l, this.pingCache = this.current = this.pendingChildren = null, this.timeoutHandle = -1, this.callbackNode = this.next = this.pendingContext = this.context = this.cancelPendingCommit = null, this.callbackPriority = 0, this.expirationTimes = nc(-1), this.entangledLanes = this.shellSuspendCounter = this.errorRecoveryDisabledLanes = this.expiredLanes = this.warmLanes = this.pingedLanes = this.suspendedLanes = this.pendingLanes = 0, this.entanglements = nc(0), this.hiddenUpdates = nc(null), this.identifierPrefix = u, this.onUncaughtError = e, this.onCaughtError = n, this.onRecoverableError = c, this.pooledCache = null, this.pooledCacheLanes = 0, this.formState = i, this.incompleteTransitions = /* @__PURE__ */ new Map();
  }
  function A0(l, t, a, u, e, n, c, f, i, h, b, A) {
    return l = new Ov(
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
  function _0(l) {
    return l ? (l = eu, l) : eu;
  }
  function p0(l, t, a, u, e, n) {
    e = _0(e), u.context === null ? u.context = e : u.pendingContext = e, u = ea(t), u.payload = { element: a }, n = n === void 0 ? null : n, n !== null && (u.callback = n), a = na(l, u, t), a !== null && (wl(a, l, t), ku(a, l, t));
  }
  function O0(l, t) {
    if (l = l.memoizedState, l !== null && l.dehydrated !== null) {
      var a = l.retryLane;
      l.retryLane = a !== 0 && a < t ? a : t;
    }
  }
  function yi(l, t) {
    O0(l, t), (l = l.alternate) && O0(l, t);
  }
  function M0(l) {
    if (l.tag === 13 || l.tag === 31) {
      var t = Ua(l, 67108864);
      t !== null && wl(t, l, 67108864), yi(l, 67108864);
    }
  }
  function D0(l) {
    if (l.tag === 13 || l.tag === 31) {
      var t = nt();
      t = cc(t);
      var a = Ua(l, t);
      a !== null && wl(a, l, t), yi(l, t);
    }
  }
  var Qn = !0;
  function Mv(l, t, a, u) {
    var e = T.T;
    T.T = null;
    var n = O.p;
    try {
      O.p = 2, mi(l, t, a, u);
    } finally {
      O.p = n, T.T = e;
    }
  }
  function Dv(l, t, a, u) {
    var e = T.T;
    T.T = null;
    var n = O.p;
    try {
      O.p = 8, mi(l, t, a, u);
    } finally {
      O.p = n, T.T = e;
    }
  }
  function mi(l, t, a, u) {
    if (Qn) {
      var e = vi(u);
      if (e === null)
        Pf(
          l,
          t,
          u,
          Zn,
          a
        ), N0(l, u);
      else if (Nv(
        e,
        l,
        t,
        a,
        u
      ))
        u.stopPropagation();
      else if (N0(l, u), t & 4 && -1 < Uv.indexOf(l)) {
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
                    Mt(n), (P & 6) === 0 && (An = Fl() + 500, me(0));
                  }
                }
                break;
              case 31:
              case 13:
                f = Ua(n, 2), f !== null && wl(f, n, 2), pn(), yi(n, 2);
            }
          if (n = vi(u), n === null && Pf(
            l,
            t,
            u,
            Zn,
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
  var Zn = null;
  function hi(l) {
    if (Zn = null, l = Ja(l), l !== null) {
      var t = G(l);
      if (t === null) l = null;
      else {
        var a = t.tag;
        if (a === 13) {
          if (l = K(t), l !== null) return l;
          l = null;
        } else if (a === 31) {
          if (l = ml(t), l !== null) return l;
          l = null;
        } else if (a === 3) {
          if (t.stateNode.current.memoizedState.isDehydrated)
            return t.tag === 3 ? t.stateNode.containerInfo : null;
          l = null;
        } else t !== l && (l = null);
      }
    }
    return Zn = l, null;
  }
  function U0(l) {
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
        switch (vy()) {
          case Yi:
            return 2;
          case ji:
            return 8;
          case Ue:
          case hy:
            return 32;
          case Gi:
            return 268435456;
          default:
            return 32;
        }
      default:
        return 32;
    }
  }
  var Si = !1, Sa = null, ra = null, ga = null, Te = /* @__PURE__ */ new Map(), Ee = /* @__PURE__ */ new Map(), ba = [], Uv = "mousedown mouseup touchcancel touchend touchstart auxclick dblclick pointercancel pointerdown pointerup dragend dragstart drop compositionend compositionstart keydown keypress keyup input textInput copy cut paste click change contextmenu reset".split(
    " "
  );
  function N0(l, t) {
    switch (l) {
      case "focusin":
      case "focusout":
        Sa = null;
        break;
      case "dragenter":
      case "dragleave":
        ra = null;
        break;
      case "mouseover":
      case "mouseout":
        ga = null;
        break;
      case "pointerover":
      case "pointerout":
        Te.delete(t.pointerId);
        break;
      case "gotpointercapture":
      case "lostpointercapture":
        Ee.delete(t.pointerId);
    }
  }
  function ze(l, t, a, u, e, n) {
    return l === null || l.nativeEvent !== n ? (l = {
      blockedOn: t,
      domEventName: a,
      eventSystemFlags: u,
      nativeEvent: n,
      targetContainers: [e]
    }, t !== null && (t = wa(t), t !== null && M0(t)), l) : (l.eventSystemFlags |= u, t = l.targetContainers, e !== null && t.indexOf(e) === -1 && t.push(e), l);
  }
  function Nv(l, t, a, u, e) {
    switch (t) {
      case "focusin":
        return Sa = ze(
          Sa,
          l,
          t,
          a,
          u,
          e
        ), !0;
      case "dragenter":
        return ra = ze(
          ra,
          l,
          t,
          a,
          u,
          e
        ), !0;
      case "mouseover":
        return ga = ze(
          ga,
          l,
          t,
          a,
          u,
          e
        ), !0;
      case "pointerover":
        var n = e.pointerId;
        return Te.set(
          n,
          ze(
            Te.get(n) || null,
            l,
            t,
            a,
            u,
            e
          )
        ), !0;
      case "gotpointercapture":
        return n = e.pointerId, Ee.set(
          n,
          ze(
            Ee.get(n) || null,
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
  function R0(l) {
    var t = Ja(l.target);
    if (t !== null) {
      var a = G(t);
      if (a !== null) {
        if (t = a.tag, t === 13) {
          if (t = K(a), t !== null) {
            l.blockedOn = t, Vi(l.priority, function() {
              D0(a);
            });
            return;
          }
        } else if (t === 31) {
          if (t = ml(a), t !== null) {
            l.blockedOn = t, Vi(l.priority, function() {
              D0(a);
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
  function Ln(l) {
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
        return t = wa(a), t !== null && M0(t), l.blockedOn = a, !1;
      t.shift();
    }
    return !0;
  }
  function C0(l, t, a) {
    Ln(l) && a.delete(t);
  }
  function Rv() {
    Si = !1, Sa !== null && Ln(Sa) && (Sa = null), ra !== null && Ln(ra) && (ra = null), ga !== null && Ln(ga) && (ga = null), Te.forEach(C0), Ee.forEach(C0);
  }
  function xn(l, t) {
    l.blockedOn === t && (l.blockedOn = null, Si || (Si = !0, o.unstable_scheduleCallback(
      o.unstable_NormalPriority,
      Rv
    )));
  }
  var Vn = null;
  function H0(l) {
    Vn !== l && (Vn = l, o.unstable_scheduleCallback(
      o.unstable_NormalPriority,
      function() {
        Vn === l && (Vn = null);
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
      return xn(i, l);
    }
    Sa !== null && xn(Sa, l), ra !== null && xn(ra, l), ga !== null && xn(ga, l), Te.forEach(t), Ee.forEach(t);
    for (var a = 0; a < ba.length; a++) {
      var u = ba[a];
      u.blockedOn === l && (u.blockedOn = null);
    }
    for (; 0 < ba.length && (a = ba[0], a.blockedOn === null); )
      R0(a), a.blockedOn === null && ba.shift();
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
  function B0() {
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
  Kn.prototype.render = ri.prototype.render = function(l) {
    var t = this._internalRoot;
    if (t === null) throw Error(m(409));
    var a = t.current, u = nt();
    p0(a, u, l, t, null, null);
  }, Kn.prototype.unmount = ri.prototype.unmount = function() {
    var l = this._internalRoot;
    if (l !== null) {
      this._internalRoot = null;
      var t = l.containerInfo;
      p0(l.current, 2, null, l, null, null), pn(), t[Ka] = null;
    }
  };
  function Kn(l) {
    this._internalRoot = l;
  }
  Kn.prototype.unstable_scheduleHydration = function(l) {
    if (l) {
      var t = xi();
      l = { blockedOn: null, target: l, priority: t };
      for (var a = 0; a < ba.length && t !== 0 && t < ba[a].priority; a++) ;
      ba.splice(a, 0, l), a === 0 && R0(l);
    }
  };
  var q0 = g.version;
  if (q0 !== "19.2.4")
    throw Error(
      m(
        527,
        q0,
        "19.2.4"
      )
    );
  O.findDOMNode = function(l) {
    var t = l._reactInternals;
    if (t === void 0)
      throw typeof l.render == "function" ? Error(m(188)) : (l = Object.keys(l).join(","), Error(m(268, l)));
    return l = p(t), l = l !== null ? F(l) : null, l = l === null ? null : l.stateNode, l;
  };
  var Cv = {
    bundleType: 0,
    version: "19.2.4",
    rendererPackageName: "react-dom",
    currentDispatcherRef: T,
    reconcilerVersion: "19.2.4"
  };
  if (typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ < "u") {
    var Jn = __REACT_DEVTOOLS_GLOBAL_HOOK__;
    if (!Jn.isDisabled && Jn.supportsFiber)
      try {
        Ru = Jn.inject(
          Cv
        ), kl = Jn;
      } catch {
      }
  }
  return pe.createRoot = function(l, t) {
    if (!D(l)) throw Error(m(299));
    var a = !1, u = "", e = Ls, n = xs, c = Vs;
    return t != null && (t.unstable_strictMode === !0 && (a = !0), t.identifierPrefix !== void 0 && (u = t.identifierPrefix), t.onUncaughtError !== void 0 && (e = t.onUncaughtError), t.onCaughtError !== void 0 && (n = t.onCaughtError), t.onRecoverableError !== void 0 && (c = t.onRecoverableError)), t = A0(
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
      B0
    ), l[Ka] = t.current, If(l), new ri(t);
  }, pe.hydrateRoot = function(l, t, a) {
    if (!D(l)) throw Error(m(299));
    var u = !1, e = "", n = Ls, c = xs, f = Vs, i = null;
    return a != null && (a.unstable_strictMode === !0 && (u = !0), a.identifierPrefix !== void 0 && (e = a.identifierPrefix), a.onUncaughtError !== void 0 && (n = a.onUncaughtError), a.onCaughtError !== void 0 && (c = a.onCaughtError), a.onRecoverableError !== void 0 && (f = a.onRecoverableError), a.formState !== void 0 && (i = a.formState)), t = A0(
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
      B0
    ), t.context = _0(null), a = t.current, u = nt(), u = cc(u), e = ea(u), e.callback = null, na(a, e, u), a = u, t.current.lanes = a, Hu(t, a), Mt(t), l[Ka] = t.current, If(l), new Kn(t);
  }, pe.version = "19.2.4", pe;
}
var J0;
function eh() {
  if (J0) return Ti.exports;
  J0 = 1;
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
var nh = eh(), fy = cy();
const vh = /* @__PURE__ */ P0(fy);
class iy {
  constructor(g) {
    this._subscribers = /* @__PURE__ */ new Set(), this.getSnapshot = () => this._state, this.subscribeStore = (z) => (this._subscribers.add(z), () => this._subscribers.delete(z)), this._state = { ...g };
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
const xa = Hl.createContext(null), Wt = /* @__PURE__ */ new Map();
let oy = "", w0 = !1;
function Uu() {
  return oy + "/";
}
function Hi(o, g, z, m, D) {
  D !== void 0 && (oy = D);
  const G = m ?? "";
  w0 ? D !== void 0 && X0(Uu() + "react-api/events?windowName=" + encodeURIComponent(G)) : (w0 = !0, qv(Uu()), X0(Uu() + "react-api/events?windowName=" + encodeURIComponent(G))), Yv(G);
  const K = document.getElementById(o);
  if (!K) {
    console.error("[TLReact] Mount point not found:", o);
    return;
  }
  const ml = I0(g);
  if (!ml) {
    console.error("[TLReact] Component not registered:", g);
    return;
  }
  Fn(o);
  const N = new iy(z);
  z.hidden === !0 && (K.style.display = "none");
  const p = (ll) => {
    N.applyPatch(ll);
  };
  Ci(o, p);
  const F = nh.createRoot(K);
  Wt.set(o, { root: F, store: N, sseListener: p }), sy = G;
  const q = () => {
    const ll = Hl.useSyncExternalStore(N.subscribeStore, N.getSnapshot);
    return Hl.useLayoutEffect(() => {
      K.style.display = ll.hidden === !0 ? "none" : "";
    }, [ll.hidden]), wn.createElement(
      xa.Provider,
      { value: { controlId: o, windowName: G, store: N } },
      wn.createElement(ml, { controlId: o, state: ll })
    );
  };
  fy.flushSync(() => {
    F.render(wn.createElement(q));
  });
}
function ch(o, g, z) {
  Hi(o, g, z);
}
function Fn(o) {
  const g = Wt.get(o);
  g && (ey(o, g.sseListener), g.root && g.root.unmount(), Wt.delete(o));
}
function fh(o, g) {
  let z = Wt.get(o);
  if (!z) {
    const D = new iy(g), G = (K) => {
      D.applyPatch(K);
    };
    Ci(o, G), z = { root: null, store: D, sseListener: G }, Wt.set(o, z);
  }
  return { controlId: o, windowName: sy, store: z.store };
}
let sy = "";
function ih() {
  const o = Hl.useContext(xa);
  if (!o)
    throw new Error("useTLState must be used inside a TLReact-mounted component.");
  return Hl.useSyncExternalStore(o.store.subscribeStore, o.store.getSnapshot);
}
function oh() {
  const o = Hl.useContext(xa);
  if (!o)
    throw new Error("useTLCommand must be used inside a TLReact-mounted component.");
  const g = o.controlId, z = o.windowName;
  return Hl.useCallback(
    async (m, D) => {
      const G = JSON.stringify({
        controlId: g,
        command: m,
        windowName: z,
        arguments: D ?? {}
      });
      try {
        const K = await fetch(Uu() + "react-api/command", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: G
        });
        K.ok || console.error("[TLReact] Command failed:", K.status, await K.text());
      } catch (K) {
        console.error("[TLReact] Command error:", K);
      }
    },
    [g, z]
  );
}
function hh() {
  const o = Hl.useContext(xa);
  if (!o)
    throw new Error("useTLUpload must be used inside a TLReact-mounted component.");
  const g = o.controlId, z = o.windowName;
  return Hl.useCallback(
    async (m) => {
      m.append("controlId", g), m.append("windowName", z);
      try {
        const D = await fetch(Uu() + "react-api/upload", {
          method: "POST",
          body: m
        });
        D.ok || console.error("[TLReact] Upload failed:", D.status, await D.text());
      } catch (D) {
        console.error("[TLReact] Upload error:", D);
      }
    },
    [g, z]
  );
}
function Sh() {
  const o = Hl.useContext(xa);
  if (!o)
    throw new Error("useTLDataUrl must be used inside a TLReact-mounted component.");
  return Uu() + "react-api/data?controlId=" + encodeURIComponent(o.controlId) + "&windowName=" + encodeURIComponent(o.windowName);
}
function rh() {
  const o = ih(), g = oh(), z = Hl.useContext(xa), m = Hl.useCallback(
    (D) => {
      z == null || z.store.applyPatch({ value: D }), g("valueChanged", { value: D });
    },
    [g, z]
  );
  return [o.value, m];
}
function kn(o = document.body) {
  const g = document.body.dataset.windowName, z = document.body.dataset.contextPath, m = o.querySelectorAll("[data-react-module]");
  for (const D of m) {
    if (!D.id || Wt.has(D.id))
      continue;
    const G = D.dataset.reactModule, K = g ?? D.dataset.windowName, ml = z ?? D.dataset.contextPath;
    if (!G || K === void 0 || ml === void 0)
      continue;
    const N = D.dataset.reactState, p = N ? JSON.parse(N) : {};
    Hi(D.id, G, p, K, ml);
  }
}
function W0() {
  new MutationObserver((g) => {
    var z;
    for (const m of g)
      for (const D of m.removedNodes)
        if (D instanceof HTMLElement) {
          const G = D.id;
          G && Wt.has(G) && Wt.get(G).root !== null && Fn(G);
          for (const [K, ml] of Wt.entries())
            ml.root !== null && D.querySelector("#" + CSS.escape(K)) && Fn(K);
        }
    for (const m of g)
      for (const D of m.addedNodes)
        D instanceof HTMLElement && ((z = D.dataset) != null && z.reactModule ? kn(D.parentElement ?? document.body) : D.querySelector("[data-react-module]") && kn(D));
  }).observe(document.body, { childList: !0, subtree: !0 });
}
document.readyState === "loading" ? document.addEventListener("DOMContentLoaded", W0) : W0();
window.addEventListener("load", () => kn());
var _i = { exports: {} }, Oe = {};
/**
 * @license React
 * react-jsx-runtime.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var $0;
function sh() {
  if ($0) return Oe;
  $0 = 1;
  var o = Symbol.for("react.transitional.element"), g = Symbol.for("react.fragment");
  function z(m, D, G) {
    var K = null;
    if (G !== void 0 && (K = "" + G), D.key !== void 0 && (K = "" + D.key), "key" in D) {
      G = {};
      for (var ml in D)
        ml !== "key" && (G[ml] = D[ml]);
    } else G = D;
    return D = G.ref, {
      $$typeof: o,
      type: m,
      key: K,
      ref: D !== void 0 ? D : null,
      props: G
    };
  }
  return Oe.Fragment = g, Oe.jsx = z, Oe.jsxs = z, Oe;
}
var F0;
function dh() {
  return F0 || (F0 = 1, _i.exports = sh()), _i.exports;
}
var pi = dh();
const gh = ({ control: o }) => {
  const g = o, z = I0(g.module), m = Hl.useMemo(
    () => fh(g.controlId, g.state),
    [g.controlId]
  );
  Hl.useEffect(() => () => Fn(g.controlId), [g.controlId]);
  const D = Hl.useSyncExternalStore(m.store.subscribeStore, m.store.getSnapshot);
  return z ? /* @__PURE__ */ pi.jsx(xa.Provider, { value: m, children: /* @__PURE__ */ pi.jsx(z, { controlId: g.controlId, state: D }) }) : /* @__PURE__ */ pi.jsxs("span", { children: [
    "[Component not registered: ",
    g.module,
    "]"
  ] });
};
window.TLReact = { mount: Hi, mountField: ch, discoverAndMount: kn, subscribe: Ci, unsubscribe: ey };
Wv();
export {
  wn as React,
  vh as ReactDOM,
  gh as TLChild,
  xa as TLControlContext,
  X0 as connect,
  fh as createChildContext,
  kn as discoverAndMount,
  I0 as getComponent,
  Hi as mount,
  ch as mountField,
  yh as register,
  Ci as subscribe,
  Fn as unmount,
  ey as unsubscribe,
  mh as useI18N,
  oh as useTLCommand,
  Sh as useTLDataUrl,
  rh as useTLFieldValue,
  ih as useTLState,
  hh as useTLUpload
};
