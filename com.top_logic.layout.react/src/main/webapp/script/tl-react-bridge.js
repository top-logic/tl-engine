const Yv = /* @__PURE__ */ new Map();
function Gd(h, _) {
  Yv.set(h, _);
}
function Gv(h) {
  return Yv.get(h);
}
const Ae = /* @__PURE__ */ new Map();
let ze = null, Ev = null, xn = 0, yi = null;
const gd = 45e3, bd = 15e3;
function rd(h) {
  Ev = h, yi && clearInterval(yi), Tv(h), yi = setInterval(() => {
    xn > 0 && Date.now() - xn > gd && (console.warn("[TLReact] No heartbeat received, reconnecting SSE."), Tv(Ev));
  }, bd);
}
function Tv(h) {
  ze && ze.close(), ze = new EventSource(h), xn = Date.now(), ze.onmessage = (_) => {
    xn = Date.now();
    try {
      const O = JSON.parse(_.data);
      zd(O);
    } catch (O) {
      console.error("[TLReact] Failed to parse SSE event:", O);
    }
  }, ze.onerror = () => {
    console.warn("[TLReact] SSE connection error, will reconnect automatically.");
  };
}
function Xv(h, _) {
  let O = Ae.get(h);
  O || (O = /* @__PURE__ */ new Set(), Ae.set(h, O)), O.add(_);
}
function Xd(h, _) {
  const O = Ae.get(h);
  O && (O.delete(_), O.size === 0 && Ae.delete(h));
}
function Qv(h, _) {
  const O = Ae.get(h);
  if (O)
    for (const o of O)
      o(_);
}
function zd(h) {
  if (!Array.isArray(h) || h.length < 2) {
    console.warn("[TLReact] Unexpected SSE event format:", h);
    return;
  }
  const _ = h[0], O = h[1];
  switch (_) {
    case "Heartbeat":
      break;
    case "StateEvent":
      Ed(O);
      break;
    case "PatchEvent":
      Td(O);
      break;
    case "ContentReplacement":
      zu.contentReplacement(O);
      break;
    case "ElementReplacement":
      zu.elementReplacement(O);
      break;
    case "PropertyUpdate":
      zu.propertyUpdate(O);
      break;
    case "CssClassUpdate":
      zu.cssClassUpdate(O);
      break;
    case "FragmentInsertion":
      zu.fragmentInsertion(O);
      break;
    case "RangeReplacement":
      zu.rangeReplacement(O);
      break;
    case "JSSnipplet":
      zu.jsSnipplet(O);
      break;
    case "FunctionCall":
      zu.functionCall(O);
      break;
    default:
      console.warn("[TLReact] Unknown SSE event type:", _);
  }
}
function Ed(h) {
  const _ = JSON.parse(h.state);
  Qv(h.controlId, _);
}
function Td(h) {
  const _ = JSON.parse(h.patch);
  Qv(h.controlId, _);
}
const zu = {
  contentReplacement(h) {
    const _ = document.getElementById(h.elementId);
    _ && (_.innerHTML = h.html);
  },
  elementReplacement(h) {
    const _ = document.getElementById(h.elementId);
    _ && (_.outerHTML = h.html);
  },
  propertyUpdate(h) {
    const _ = document.getElementById(h.elementId);
    if (_)
      for (const O of h.properties)
        _.setAttribute(O.name, O.value);
  },
  cssClassUpdate(h) {
    const _ = document.getElementById(h.elementId);
    _ && (_.className = h.cssClass);
  },
  fragmentInsertion(h) {
    const _ = document.getElementById(h.elementId);
    _ && _.insertAdjacentHTML(h.position, h.html);
  },
  rangeReplacement(h) {
    const _ = document.getElementById(h.startId), O = document.getElementById(h.stopId);
    if (_ && O && _.parentNode) {
      const o = _.parentNode, j = document.createRange();
      j.setStartBefore(_), j.setEndAfter(O), j.deleteContents();
      const k = document.createElement("template");
      k.innerHTML = h.html, o.insertBefore(k.content, j.startContainer.childNodes[j.startOffset] || null);
    }
  },
  jsSnipplet(h) {
    try {
      (0, eval)(h.code);
    } catch (_) {
      console.error("[TLReact] Error executing JS snippet:", _);
    }
  },
  functionCall(h) {
    try {
      const _ = JSON.parse(h.arguments), O = h.functionRef ? window[h.functionRef] : window, o = O == null ? void 0 : O[h.functionName];
      typeof o == "function" ? o.apply(O, _) : console.warn("[TLReact] Function not found:", h.functionRef + "." + h.functionName);
    } catch (_) {
      console.error("[TLReact] Error executing function call:", _);
    }
  }
};
function jv(h) {
  return h && h.__esModule && Object.prototype.hasOwnProperty.call(h, "default") ? h.default : h;
}
var vi = { exports: {} }, Y = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var Av;
function Ad() {
  if (Av) return Y;
  Av = 1;
  var h = Symbol.for("react.transitional.element"), _ = Symbol.for("react.portal"), O = Symbol.for("react.fragment"), o = Symbol.for("react.strict_mode"), j = Symbol.for("react.profiler"), k = Symbol.for("react.consumer"), ul = Symbol.for("react.context"), hl = Symbol.for("react.forward_ref"), N = Symbol.for("react.suspense"), A = Symbol.for("react.memo"), W = Symbol.for("react.lazy"), q = Symbol.for("react.activity"), al = Symbol.iterator;
  function wl(y) {
    return y === null || typeof y != "object" ? null : (y = al && y[al] || y["@@iterator"], typeof y == "function" ? y : null);
  }
  var Yl = {
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
  function Wl(y, T, D) {
    this.props = y, this.context = T, this.refs = Dt, this.updater = D || Yl;
  }
  Wl.prototype.isReactComponent = {}, Wl.prototype.setState = function(y, T) {
    if (typeof y != "object" && typeof y != "function" && y != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, y, T, "setState");
  }, Wl.prototype.forceUpdate = function(y) {
    this.updater.enqueueForceUpdate(this, y, "forceUpdate");
  };
  function Wt() {
  }
  Wt.prototype = Wl.prototype;
  function Rl(y, T, D) {
    this.props = y, this.context = T, this.refs = Dt, this.updater = D || Yl;
  }
  var nt = Rl.prototype = new Wt();
  nt.constructor = Rl, Cl(nt, Wl.prototype), nt.isPureReactComponent = !0;
  var zt = Array.isArray;
  function Gl() {
  }
  var J = { H: null, A: null, T: null, S: null }, Xl = Object.prototype.hasOwnProperty;
  function Et(y, T, D) {
    var U = D.ref;
    return {
      $$typeof: h,
      type: y,
      key: T,
      ref: U !== void 0 ? U : null,
      props: D
    };
  }
  function Lu(y, T) {
    return Et(y.type, T, y.props);
  }
  function Tt(y) {
    return typeof y == "object" && y !== null && y.$$typeof === h;
  }
  function Ql(y) {
    var T = { "=": "=0", ":": "=2" };
    return "$" + y.replace(/[=:]/g, function(D) {
      return T[D];
    });
  }
  var Tu = /\/+/g;
  function pt(y, T) {
    return typeof y == "object" && y !== null && y.key != null ? Ql("" + y.key) : T.toString(36);
  }
  function St(y) {
    switch (y.status) {
      case "fulfilled":
        return y.value;
      case "rejected":
        throw y.reason;
      default:
        switch (typeof y.status == "string" ? y.then(Gl, Gl) : (y.status = "pending", y.then(
          function(T) {
            y.status === "pending" && (y.status = "fulfilled", y.value = T);
          },
          function(T) {
            y.status === "pending" && (y.status = "rejected", y.reason = T);
          }
        )), y.status) {
          case "fulfilled":
            return y.value;
          case "rejected":
            throw y.reason;
        }
    }
    throw y;
  }
  function r(y, T, D, U, G) {
    var Z = typeof y;
    (Z === "undefined" || Z === "boolean") && (y = null);
    var P = !1;
    if (y === null) P = !0;
    else
      switch (Z) {
        case "bigint":
        case "string":
        case "number":
          P = !0;
          break;
        case "object":
          switch (y.$$typeof) {
            case h:
            case _:
              P = !0;
              break;
            case W:
              return P = y._init, r(
                P(y._payload),
                T,
                D,
                U,
                G
              );
          }
      }
    if (P)
      return G = G(y), P = U === "" ? "." + pt(y, 0) : U, zt(G) ? (D = "", P != null && (D = P.replace(Tu, "$&/") + "/"), r(G, T, D, "", function(Da) {
        return Da;
      })) : G != null && (Tt(G) && (G = Lu(
        G,
        D + (G.key == null || y && y.key === G.key ? "" : ("" + G.key).replace(
          Tu,
          "$&/"
        ) + "/") + P
      )), T.push(G)), 1;
    P = 0;
    var ql = U === "" ? "." : U + ":";
    if (zt(y))
      for (var Sl = 0; Sl < y.length; Sl++)
        U = y[Sl], Z = ql + pt(U, Sl), P += r(
          U,
          T,
          D,
          Z,
          G
        );
    else if (Sl = wl(y), typeof Sl == "function")
      for (y = Sl.call(y), Sl = 0; !(U = y.next()).done; )
        U = U.value, Z = ql + pt(U, Sl++), P += r(
          U,
          T,
          D,
          Z,
          G
        );
    else if (Z === "object") {
      if (typeof y.then == "function")
        return r(
          St(y),
          T,
          D,
          U,
          G
        );
      throw T = String(y), Error(
        "Objects are not valid as a React child (found: " + (T === "[object Object]" ? "object with keys {" + Object.keys(y).join(", ") + "}" : T) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return P;
  }
  function M(y, T, D) {
    if (y == null) return y;
    var U = [], G = 0;
    return r(y, U, "", "", function(Z) {
      return T.call(D, Z, G++);
    }), U;
  }
  function B(y) {
    if (y._status === -1) {
      var T = y._result;
      T = T(), T.then(
        function(D) {
          (y._status === 0 || y._status === -1) && (y._status = 1, y._result = D);
        },
        function(D) {
          (y._status === 0 || y._status === -1) && (y._status = 2, y._result = D);
        }
      ), y._status === -1 && (y._status = 0, y._result = T);
    }
    if (y._status === 1) return y._result.default;
    throw y._result;
  }
  var el = typeof reportError == "function" ? reportError : function(y) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var T = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof y == "object" && y !== null && typeof y.message == "string" ? String(y.message) : String(y),
        error: y
      });
      if (!window.dispatchEvent(T)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", y);
      return;
    }
    console.error(y);
  }, il = {
    map: M,
    forEach: function(y, T, D) {
      M(
        y,
        function() {
          T.apply(this, arguments);
        },
        D
      );
    },
    count: function(y) {
      var T = 0;
      return M(y, function() {
        T++;
      }), T;
    },
    toArray: function(y) {
      return M(y, function(T) {
        return T;
      }) || [];
    },
    only: function(y) {
      if (!Tt(y))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return y;
    }
  };
  return Y.Activity = q, Y.Children = il, Y.Component = Wl, Y.Fragment = O, Y.Profiler = j, Y.PureComponent = Rl, Y.StrictMode = o, Y.Suspense = N, Y.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = J, Y.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(y) {
      return J.H.useMemoCache(y);
    }
  }, Y.cache = function(y) {
    return function() {
      return y.apply(null, arguments);
    };
  }, Y.cacheSignal = function() {
    return null;
  }, Y.cloneElement = function(y, T, D) {
    if (y == null)
      throw Error(
        "The argument must be a React element, but you passed " + y + "."
      );
    var U = Cl({}, y.props), G = y.key;
    if (T != null)
      for (Z in T.key !== void 0 && (G = "" + T.key), T)
        !Xl.call(T, Z) || Z === "key" || Z === "__self" || Z === "__source" || Z === "ref" && T.ref === void 0 || (U[Z] = T[Z]);
    var Z = arguments.length - 2;
    if (Z === 1) U.children = D;
    else if (1 < Z) {
      for (var P = Array(Z), ql = 0; ql < Z; ql++)
        P[ql] = arguments[ql + 2];
      U.children = P;
    }
    return Et(y.type, G, U);
  }, Y.createContext = function(y) {
    return y = {
      $$typeof: ul,
      _currentValue: y,
      _currentValue2: y,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, y.Provider = y, y.Consumer = {
      $$typeof: k,
      _context: y
    }, y;
  }, Y.createElement = function(y, T, D) {
    var U, G = {}, Z = null;
    if (T != null)
      for (U in T.key !== void 0 && (Z = "" + T.key), T)
        Xl.call(T, U) && U !== "key" && U !== "__self" && U !== "__source" && (G[U] = T[U]);
    var P = arguments.length - 2;
    if (P === 1) G.children = D;
    else if (1 < P) {
      for (var ql = Array(P), Sl = 0; Sl < P; Sl++)
        ql[Sl] = arguments[Sl + 2];
      G.children = ql;
    }
    if (y && y.defaultProps)
      for (U in P = y.defaultProps, P)
        G[U] === void 0 && (G[U] = P[U]);
    return Et(y, Z, G);
  }, Y.createRef = function() {
    return { current: null };
  }, Y.forwardRef = function(y) {
    return { $$typeof: hl, render: y };
  }, Y.isValidElement = Tt, Y.lazy = function(y) {
    return {
      $$typeof: W,
      _payload: { _status: -1, _result: y },
      _init: B
    };
  }, Y.memo = function(y, T) {
    return {
      $$typeof: A,
      type: y,
      compare: T === void 0 ? null : T
    };
  }, Y.startTransition = function(y) {
    var T = J.T, D = {};
    J.T = D;
    try {
      var U = y(), G = J.S;
      G !== null && G(D, U), typeof U == "object" && U !== null && typeof U.then == "function" && U.then(Gl, el);
    } catch (Z) {
      el(Z);
    } finally {
      T !== null && D.types !== null && (T.types = D.types), J.T = T;
    }
  }, Y.unstable_useCacheRefresh = function() {
    return J.H.useCacheRefresh();
  }, Y.use = function(y) {
    return J.H.use(y);
  }, Y.useActionState = function(y, T, D) {
    return J.H.useActionState(y, T, D);
  }, Y.useCallback = function(y, T) {
    return J.H.useCallback(y, T);
  }, Y.useContext = function(y) {
    return J.H.useContext(y);
  }, Y.useDebugValue = function() {
  }, Y.useDeferredValue = function(y, T) {
    return J.H.useDeferredValue(y, T);
  }, Y.useEffect = function(y, T) {
    return J.H.useEffect(y, T);
  }, Y.useEffectEvent = function(y) {
    return J.H.useEffectEvent(y);
  }, Y.useId = function() {
    return J.H.useId();
  }, Y.useImperativeHandle = function(y, T, D) {
    return J.H.useImperativeHandle(y, T, D);
  }, Y.useInsertionEffect = function(y, T) {
    return J.H.useInsertionEffect(y, T);
  }, Y.useLayoutEffect = function(y, T) {
    return J.H.useLayoutEffect(y, T);
  }, Y.useMemo = function(y, T) {
    return J.H.useMemo(y, T);
  }, Y.useOptimistic = function(y, T) {
    return J.H.useOptimistic(y, T);
  }, Y.useReducer = function(y, T, D) {
    return J.H.useReducer(y, T, D);
  }, Y.useRef = function(y) {
    return J.H.useRef(y);
  }, Y.useState = function(y) {
    return J.H.useState(y);
  }, Y.useSyncExternalStore = function(y, T, D) {
    return J.H.useSyncExternalStore(
      y,
      T,
      D
    );
  }, Y.useTransition = function() {
    return J.H.useTransition();
  }, Y.version = "19.2.4", Y;
}
var _v;
function ri() {
  return _v || (_v = 1, vi.exports = Ad()), vi.exports;
}
var wt = ri();
const oi = /* @__PURE__ */ jv(wt);
var mi = { exports: {} }, Ee = {}, di = { exports: {} }, hi = {};
/**
 * @license React
 * scheduler.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var Ov;
function _d() {
  return Ov || (Ov = 1, (function(h) {
    function _(r, M) {
      var B = r.length;
      r.push(M);
      l: for (; 0 < B; ) {
        var el = B - 1 >>> 1, il = r[el];
        if (0 < j(il, M))
          r[el] = M, r[B] = il, B = el;
        else break l;
      }
    }
    function O(r) {
      return r.length === 0 ? null : r[0];
    }
    function o(r) {
      if (r.length === 0) return null;
      var M = r[0], B = r.pop();
      if (B !== M) {
        r[0] = B;
        l: for (var el = 0, il = r.length, y = il >>> 1; el < y; ) {
          var T = 2 * (el + 1) - 1, D = r[T], U = T + 1, G = r[U];
          if (0 > j(D, B))
            U < il && 0 > j(G, D) ? (r[el] = G, r[U] = B, el = U) : (r[el] = D, r[T] = B, el = T);
          else if (U < il && 0 > j(G, B))
            r[el] = G, r[U] = B, el = U;
          else break l;
        }
      }
      return M;
    }
    function j(r, M) {
      var B = r.sortIndex - M.sortIndex;
      return B !== 0 ? B : r.id - M.id;
    }
    if (h.unstable_now = void 0, typeof performance == "object" && typeof performance.now == "function") {
      var k = performance;
      h.unstable_now = function() {
        return k.now();
      };
    } else {
      var ul = Date, hl = ul.now();
      h.unstable_now = function() {
        return ul.now() - hl;
      };
    }
    var N = [], A = [], W = 1, q = null, al = 3, wl = !1, Yl = !1, Cl = !1, Dt = !1, Wl = typeof setTimeout == "function" ? setTimeout : null, Wt = typeof clearTimeout == "function" ? clearTimeout : null, Rl = typeof setImmediate < "u" ? setImmediate : null;
    function nt(r) {
      for (var M = O(A); M !== null; ) {
        if (M.callback === null) o(A);
        else if (M.startTime <= r)
          o(A), M.sortIndex = M.expirationTime, _(N, M);
        else break;
        M = O(A);
      }
    }
    function zt(r) {
      if (Cl = !1, nt(r), !Yl)
        if (O(N) !== null)
          Yl = !0, Gl || (Gl = !0, Ql());
        else {
          var M = O(A);
          M !== null && St(zt, M.startTime - r);
        }
    }
    var Gl = !1, J = -1, Xl = 5, Et = -1;
    function Lu() {
      return Dt ? !0 : !(h.unstable_now() - Et < Xl);
    }
    function Tt() {
      if (Dt = !1, Gl) {
        var r = h.unstable_now();
        Et = r;
        var M = !0;
        try {
          l: {
            Yl = !1, Cl && (Cl = !1, Wt(J), J = -1), wl = !0;
            var B = al;
            try {
              t: {
                for (nt(r), q = O(N); q !== null && !(q.expirationTime > r && Lu()); ) {
                  var el = q.callback;
                  if (typeof el == "function") {
                    q.callback = null, al = q.priorityLevel;
                    var il = el(
                      q.expirationTime <= r
                    );
                    if (r = h.unstable_now(), typeof il == "function") {
                      q.callback = il, nt(r), M = !0;
                      break t;
                    }
                    q === O(N) && o(N), nt(r);
                  } else o(N);
                  q = O(N);
                }
                if (q !== null) M = !0;
                else {
                  var y = O(A);
                  y !== null && St(
                    zt,
                    y.startTime - r
                  ), M = !1;
                }
              }
              break l;
            } finally {
              q = null, al = B, wl = !1;
            }
            M = void 0;
          }
        } finally {
          M ? Ql() : Gl = !1;
        }
      }
    }
    var Ql;
    if (typeof Rl == "function")
      Ql = function() {
        Rl(Tt);
      };
    else if (typeof MessageChannel < "u") {
      var Tu = new MessageChannel(), pt = Tu.port2;
      Tu.port1.onmessage = Tt, Ql = function() {
        pt.postMessage(null);
      };
    } else
      Ql = function() {
        Wl(Tt, 0);
      };
    function St(r, M) {
      J = Wl(function() {
        r(h.unstable_now());
      }, M);
    }
    h.unstable_IdlePriority = 5, h.unstable_ImmediatePriority = 1, h.unstable_LowPriority = 4, h.unstable_NormalPriority = 3, h.unstable_Profiling = null, h.unstable_UserBlockingPriority = 2, h.unstable_cancelCallback = function(r) {
      r.callback = null;
    }, h.unstable_forceFrameRate = function(r) {
      0 > r || 125 < r ? console.error(
        "forceFrameRate takes a positive int between 0 and 125, forcing frame rates higher than 125 fps is not supported"
      ) : Xl = 0 < r ? Math.floor(1e3 / r) : 5;
    }, h.unstable_getCurrentPriorityLevel = function() {
      return al;
    }, h.unstable_next = function(r) {
      switch (al) {
        case 1:
        case 2:
        case 3:
          var M = 3;
          break;
        default:
          M = al;
      }
      var B = al;
      al = M;
      try {
        return r();
      } finally {
        al = B;
      }
    }, h.unstable_requestPaint = function() {
      Dt = !0;
    }, h.unstable_runWithPriority = function(r, M) {
      switch (r) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
          break;
        default:
          r = 3;
      }
      var B = al;
      al = r;
      try {
        return M();
      } finally {
        al = B;
      }
    }, h.unstable_scheduleCallback = function(r, M, B) {
      var el = h.unstable_now();
      switch (typeof B == "object" && B !== null ? (B = B.delay, B = typeof B == "number" && 0 < B ? el + B : el) : B = el, r) {
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
      return il = B + il, r = {
        id: W++,
        callback: M,
        priorityLevel: r,
        startTime: B,
        expirationTime: il,
        sortIndex: -1
      }, B > el ? (r.sortIndex = B, _(A, r), O(N) === null && r === O(A) && (Cl ? (Wt(J), J = -1) : Cl = !0, St(zt, B - el))) : (r.sortIndex = il, _(N, r), Yl || wl || (Yl = !0, Gl || (Gl = !0, Ql()))), r;
    }, h.unstable_shouldYield = Lu, h.unstable_wrapCallback = function(r) {
      var M = al;
      return function() {
        var B = al;
        al = M;
        try {
          return r.apply(this, arguments);
        } finally {
          al = B;
        }
      };
    };
  })(hi)), hi;
}
var Mv;
function Od() {
  return Mv || (Mv = 1, di.exports = _d()), di.exports;
}
var Si = { exports: {} }, Nl = {};
/**
 * @license React
 * react-dom.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var Dv;
function Md() {
  if (Dv) return Nl;
  Dv = 1;
  var h = ri();
  function _(N) {
    var A = "https://react.dev/errors/" + N;
    if (1 < arguments.length) {
      A += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var W = 2; W < arguments.length; W++)
        A += "&args[]=" + encodeURIComponent(arguments[W]);
    }
    return "Minified React error #" + N + "; visit " + A + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function O() {
  }
  var o = {
    d: {
      f: O,
      r: function() {
        throw Error(_(522));
      },
      D: O,
      C: O,
      L: O,
      m: O,
      X: O,
      S: O,
      M: O
    },
    p: 0,
    findDOMNode: null
  }, j = Symbol.for("react.portal");
  function k(N, A, W) {
    var q = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: j,
      key: q == null ? null : "" + q,
      children: N,
      containerInfo: A,
      implementation: W
    };
  }
  var ul = h.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function hl(N, A) {
    if (N === "font") return "";
    if (typeof A == "string")
      return A === "use-credentials" ? A : "";
  }
  return Nl.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = o, Nl.createPortal = function(N, A) {
    var W = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!A || A.nodeType !== 1 && A.nodeType !== 9 && A.nodeType !== 11)
      throw Error(_(299));
    return k(N, A, null, W);
  }, Nl.flushSync = function(N) {
    var A = ul.T, W = o.p;
    try {
      if (ul.T = null, o.p = 2, N) return N();
    } finally {
      ul.T = A, o.p = W, o.d.f();
    }
  }, Nl.preconnect = function(N, A) {
    typeof N == "string" && (A ? (A = A.crossOrigin, A = typeof A == "string" ? A === "use-credentials" ? A : "" : void 0) : A = null, o.d.C(N, A));
  }, Nl.prefetchDNS = function(N) {
    typeof N == "string" && o.d.D(N);
  }, Nl.preinit = function(N, A) {
    if (typeof N == "string" && A && typeof A.as == "string") {
      var W = A.as, q = hl(W, A.crossOrigin), al = typeof A.integrity == "string" ? A.integrity : void 0, wl = typeof A.fetchPriority == "string" ? A.fetchPriority : void 0;
      W === "style" ? o.d.S(
        N,
        typeof A.precedence == "string" ? A.precedence : void 0,
        {
          crossOrigin: q,
          integrity: al,
          fetchPriority: wl
        }
      ) : W === "script" && o.d.X(N, {
        crossOrigin: q,
        integrity: al,
        fetchPriority: wl,
        nonce: typeof A.nonce == "string" ? A.nonce : void 0
      });
    }
  }, Nl.preinitModule = function(N, A) {
    if (typeof N == "string")
      if (typeof A == "object" && A !== null) {
        if (A.as == null || A.as === "script") {
          var W = hl(
            A.as,
            A.crossOrigin
          );
          o.d.M(N, {
            crossOrigin: W,
            integrity: typeof A.integrity == "string" ? A.integrity : void 0,
            nonce: typeof A.nonce == "string" ? A.nonce : void 0
          });
        }
      } else A == null && o.d.M(N);
  }, Nl.preload = function(N, A) {
    if (typeof N == "string" && typeof A == "object" && A !== null && typeof A.as == "string") {
      var W = A.as, q = hl(W, A.crossOrigin);
      o.d.L(N, W, {
        crossOrigin: q,
        integrity: typeof A.integrity == "string" ? A.integrity : void 0,
        nonce: typeof A.nonce == "string" ? A.nonce : void 0,
        type: typeof A.type == "string" ? A.type : void 0,
        fetchPriority: typeof A.fetchPriority == "string" ? A.fetchPriority : void 0,
        referrerPolicy: typeof A.referrerPolicy == "string" ? A.referrerPolicy : void 0,
        imageSrcSet: typeof A.imageSrcSet == "string" ? A.imageSrcSet : void 0,
        imageSizes: typeof A.imageSizes == "string" ? A.imageSizes : void 0,
        media: typeof A.media == "string" ? A.media : void 0
      });
    }
  }, Nl.preloadModule = function(N, A) {
    if (typeof N == "string")
      if (A) {
        var W = hl(A.as, A.crossOrigin);
        o.d.m(N, {
          as: typeof A.as == "string" && A.as !== "script" ? A.as : void 0,
          crossOrigin: W,
          integrity: typeof A.integrity == "string" ? A.integrity : void 0
        });
      } else o.d.m(N);
  }, Nl.requestFormReset = function(N) {
    o.d.r(N);
  }, Nl.unstable_batchedUpdates = function(N, A) {
    return N(A);
  }, Nl.useFormState = function(N, A, W) {
    return ul.H.useFormState(N, A, W);
  }, Nl.useFormStatus = function() {
    return ul.H.useHostTransitionStatus();
  }, Nl.version = "19.2.4", Nl;
}
var pv;
function Zv() {
  if (pv) return Si.exports;
  pv = 1;
  function h() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(h);
      } catch (_) {
        console.error(_);
      }
  }
  return h(), Si.exports = Md(), Si.exports;
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
var Uv;
function Dd() {
  if (Uv) return Ee;
  Uv = 1;
  var h = Od(), _ = ri(), O = Zv();
  function o(l) {
    var t = "https://react.dev/errors/" + l;
    if (1 < arguments.length) {
      t += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var u = 2; u < arguments.length; u++)
        t += "&args[]=" + encodeURIComponent(arguments[u]);
    }
    return "Minified React error #" + l + "; visit " + t + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function j(l) {
    return !(!l || l.nodeType !== 1 && l.nodeType !== 9 && l.nodeType !== 11);
  }
  function k(l) {
    var t = l, u = l;
    if (l.alternate) for (; t.return; ) t = t.return;
    else {
      l = t;
      do
        t = l, (t.flags & 4098) !== 0 && (u = t.return), l = t.return;
      while (l);
    }
    return t.tag === 3 ? u : null;
  }
  function ul(l) {
    if (l.tag === 13) {
      var t = l.memoizedState;
      if (t === null && (l = l.alternate, l !== null && (t = l.memoizedState)), t !== null) return t.dehydrated;
    }
    return null;
  }
  function hl(l) {
    if (l.tag === 31) {
      var t = l.memoizedState;
      if (t === null && (l = l.alternate, l !== null && (t = l.memoizedState)), t !== null) return t.dehydrated;
    }
    return null;
  }
  function N(l) {
    if (k(l) !== l)
      throw Error(o(188));
  }
  function A(l) {
    var t = l.alternate;
    if (!t) {
      if (t = k(l), t === null) throw Error(o(188));
      return t !== l ? null : l;
    }
    for (var u = l, a = t; ; ) {
      var e = u.return;
      if (e === null) break;
      var n = e.alternate;
      if (n === null) {
        if (a = e.return, a !== null) {
          u = a;
          continue;
        }
        break;
      }
      if (e.child === n.child) {
        for (n = e.child; n; ) {
          if (n === u) return N(e), l;
          if (n === a) return N(e), t;
          n = n.sibling;
        }
        throw Error(o(188));
      }
      if (u.return !== a.return) u = e, a = n;
      else {
        for (var f = !1, c = e.child; c; ) {
          if (c === u) {
            f = !0, u = e, a = n;
            break;
          }
          if (c === a) {
            f = !0, a = e, u = n;
            break;
          }
          c = c.sibling;
        }
        if (!f) {
          for (c = n.child; c; ) {
            if (c === u) {
              f = !0, u = n, a = e;
              break;
            }
            if (c === a) {
              f = !0, a = n, u = e;
              break;
            }
            c = c.sibling;
          }
          if (!f) throw Error(o(189));
        }
      }
      if (u.alternate !== a) throw Error(o(190));
    }
    if (u.tag !== 3) throw Error(o(188));
    return u.stateNode.current === u ? l : t;
  }
  function W(l) {
    var t = l.tag;
    if (t === 5 || t === 26 || t === 27 || t === 6) return l;
    for (l = l.child; l !== null; ) {
      if (t = W(l), t !== null) return t;
      l = l.sibling;
    }
    return null;
  }
  var q = Object.assign, al = Symbol.for("react.element"), wl = Symbol.for("react.transitional.element"), Yl = Symbol.for("react.portal"), Cl = Symbol.for("react.fragment"), Dt = Symbol.for("react.strict_mode"), Wl = Symbol.for("react.profiler"), Wt = Symbol.for("react.consumer"), Rl = Symbol.for("react.context"), nt = Symbol.for("react.forward_ref"), zt = Symbol.for("react.suspense"), Gl = Symbol.for("react.suspense_list"), J = Symbol.for("react.memo"), Xl = Symbol.for("react.lazy"), Et = Symbol.for("react.activity"), Lu = Symbol.for("react.memo_cache_sentinel"), Tt = Symbol.iterator;
  function Ql(l) {
    return l === null || typeof l != "object" ? null : (l = Tt && l[Tt] || l["@@iterator"], typeof l == "function" ? l : null);
  }
  var Tu = Symbol.for("react.client.reference");
  function pt(l) {
    if (l == null) return null;
    if (typeof l == "function")
      return l.$$typeof === Tu ? null : l.displayName || l.name || null;
    if (typeof l == "string") return l;
    switch (l) {
      case Cl:
        return "Fragment";
      case Wl:
        return "Profiler";
      case Dt:
        return "StrictMode";
      case zt:
        return "Suspense";
      case Gl:
        return "SuspenseList";
      case Et:
        return "Activity";
    }
    if (typeof l == "object")
      switch (l.$$typeof) {
        case Yl:
          return "Portal";
        case Rl:
          return l.displayName || "Context";
        case Wt:
          return (l._context.displayName || "Context") + ".Consumer";
        case nt:
          var t = l.render;
          return l = l.displayName, l || (l = t.displayName || t.name || "", l = l !== "" ? "ForwardRef(" + l + ")" : "ForwardRef"), l;
        case J:
          return t = l.displayName || null, t !== null ? t : pt(l.type) || "Memo";
        case Xl:
          t = l._payload, l = l._init;
          try {
            return pt(l(t));
          } catch {
          }
      }
    return null;
  }
  var St = Array.isArray, r = _.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE, M = O.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE, B = {
    pending: !1,
    data: null,
    method: null,
    action: null
  }, el = [], il = -1;
  function y(l) {
    return { current: l };
  }
  function T(l) {
    0 > il || (l.current = el[il], el[il] = null, il--);
  }
  function D(l, t) {
    il++, el[il] = l.current, l.current = t;
  }
  var U = y(null), G = y(null), Z = y(null), P = y(null);
  function ql(l, t) {
    switch (D(Z, t), D(G, l), D(U, null), t.nodeType) {
      case 9:
      case 11:
        l = (l = t.documentElement) && (l = l.namespaceURI) ? Vy(l) : 0;
        break;
      default:
        if (l = t.tagName, t = t.namespaceURI)
          t = Vy(t), l = Ky(t, l);
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
    T(U), D(U, l);
  }
  function Sl() {
    T(U), T(G), T(Z);
  }
  function Da(l) {
    l.memoizedState !== null && D(P, l);
    var t = U.current, u = Ky(t, l.type);
    t !== u && (D(G, l), D(U, u));
  }
  function _e(l) {
    G.current === l && (T(U), T(G)), P.current === l && (T(P), Se._currentValue = B);
  }
  var Kn, zi;
  function Au(l) {
    if (Kn === void 0)
      try {
        throw Error();
      } catch (u) {
        var t = u.stack.trim().match(/\n( *(at )?)/);
        Kn = t && t[1] || "", zi = -1 < u.stack.indexOf(`
    at`) ? " (<anonymous>)" : -1 < u.stack.indexOf("@") ? "@unknown:0:0" : "";
      }
    return `
` + Kn + l + zi;
  }
  var Jn = !1;
  function wn(l, t) {
    if (!l || Jn) return "";
    Jn = !0;
    var u = Error.prepareStackTrace;
    Error.prepareStackTrace = void 0;
    try {
      var a = {
        DetermineComponentFrameRoot: function() {
          try {
            if (t) {
              var E = function() {
                throw Error();
              };
              if (Object.defineProperty(E.prototype, "props", {
                set: function() {
                  throw Error();
                }
              }), typeof Reflect == "object" && Reflect.construct) {
                try {
                  Reflect.construct(E, []);
                } catch (g) {
                  var S = g;
                }
                Reflect.construct(l, [], E);
              } else {
                try {
                  E.call();
                } catch (g) {
                  S = g;
                }
                l.call(E.prototype);
              }
            } else {
              try {
                throw Error();
              } catch (g) {
                S = g;
              }
              (E = l()) && typeof E.catch == "function" && E.catch(function() {
              });
            }
          } catch (g) {
            if (g && S && typeof g.stack == "string")
              return [g.stack, S.stack];
          }
          return [null, null];
        }
      };
      a.DetermineComponentFrameRoot.displayName = "DetermineComponentFrameRoot";
      var e = Object.getOwnPropertyDescriptor(
        a.DetermineComponentFrameRoot,
        "name"
      );
      e && e.configurable && Object.defineProperty(
        a.DetermineComponentFrameRoot,
        "name",
        { value: "DetermineComponentFrameRoot" }
      );
      var n = a.DetermineComponentFrameRoot(), f = n[0], c = n[1];
      if (f && c) {
        var i = f.split(`
`), d = c.split(`
`);
        for (e = a = 0; a < i.length && !i[a].includes("DetermineComponentFrameRoot"); )
          a++;
        for (; e < d.length && !d[e].includes(
          "DetermineComponentFrameRoot"
        ); )
          e++;
        if (a === i.length || e === d.length)
          for (a = i.length - 1, e = d.length - 1; 1 <= a && 0 <= e && i[a] !== d[e]; )
            e--;
        for (; 1 <= a && 0 <= e; a--, e--)
          if (i[a] !== d[e]) {
            if (a !== 1 || e !== 1)
              do
                if (a--, e--, 0 > e || i[a] !== d[e]) {
                  var b = `
` + i[a].replace(" at new ", " at ");
                  return l.displayName && b.includes("<anonymous>") && (b = b.replace("<anonymous>", l.displayName)), b;
                }
              while (1 <= a && 0 <= e);
            break;
          }
      }
    } finally {
      Jn = !1, Error.prepareStackTrace = u;
    }
    return (u = l ? l.displayName || l.name : "") ? Au(u) : "";
  }
  function wv(l, t) {
    switch (l.tag) {
      case 26:
      case 27:
      case 5:
        return Au(l.type);
      case 16:
        return Au("Lazy");
      case 13:
        return l.child !== t && t !== null ? Au("Suspense Fallback") : Au("Suspense");
      case 19:
        return Au("SuspenseList");
      case 0:
      case 15:
        return wn(l.type, !1);
      case 11:
        return wn(l.type.render, !1);
      case 1:
        return wn(l.type, !0);
      case 31:
        return Au("Activity");
      default:
        return "";
    }
  }
  function Ei(l) {
    try {
      var t = "", u = null;
      do
        t += wv(l, u), u = l, l = l.return;
      while (l);
      return t;
    } catch (a) {
      return `
Error generating stack: ` + a.message + `
` + a.stack;
    }
  }
  var Wn = Object.prototype.hasOwnProperty, $n = h.unstable_scheduleCallback, Fn = h.unstable_cancelCallback, Wv = h.unstable_shouldYield, $v = h.unstable_requestPaint, $l = h.unstable_now, Fv = h.unstable_getCurrentPriorityLevel, Ti = h.unstable_ImmediatePriority, Ai = h.unstable_UserBlockingPriority, Oe = h.unstable_NormalPriority, kv = h.unstable_LowPriority, _i = h.unstable_IdlePriority, Iv = h.log, Pv = h.unstable_setDisableYieldValue, pa = null, Fl = null;
  function $t(l) {
    if (typeof Iv == "function" && Pv(l), Fl && typeof Fl.setStrictMode == "function")
      try {
        Fl.setStrictMode(pa, l);
      } catch {
      }
  }
  var kl = Math.clz32 ? Math.clz32 : uo, lo = Math.log, to = Math.LN2;
  function uo(l) {
    return l >>>= 0, l === 0 ? 32 : 31 - (lo(l) / to | 0) | 0;
  }
  var Me = 256, De = 262144, pe = 4194304;
  function _u(l) {
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
  function Ue(l, t, u) {
    var a = l.pendingLanes;
    if (a === 0) return 0;
    var e = 0, n = l.suspendedLanes, f = l.pingedLanes;
    l = l.warmLanes;
    var c = a & 134217727;
    return c !== 0 ? (a = c & ~n, a !== 0 ? e = _u(a) : (f &= c, f !== 0 ? e = _u(f) : u || (u = c & ~l, u !== 0 && (e = _u(u))))) : (c = a & ~n, c !== 0 ? e = _u(c) : f !== 0 ? e = _u(f) : u || (u = a & ~l, u !== 0 && (e = _u(u)))), e === 0 ? 0 : t !== 0 && t !== e && (t & n) === 0 && (n = e & -e, u = t & -t, n >= u || n === 32 && (u & 4194048) !== 0) ? t : e;
  }
  function Ua(l, t) {
    return (l.pendingLanes & ~(l.suspendedLanes & ~l.pingedLanes) & t) === 0;
  }
  function ao(l, t) {
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
  function Oi() {
    var l = pe;
    return pe <<= 1, (pe & 62914560) === 0 && (pe = 4194304), l;
  }
  function kn(l) {
    for (var t = [], u = 0; 31 > u; u++) t.push(l);
    return t;
  }
  function Ha(l, t) {
    l.pendingLanes |= t, t !== 268435456 && (l.suspendedLanes = 0, l.pingedLanes = 0, l.warmLanes = 0);
  }
  function eo(l, t, u, a, e, n) {
    var f = l.pendingLanes;
    l.pendingLanes = u, l.suspendedLanes = 0, l.pingedLanes = 0, l.warmLanes = 0, l.expiredLanes &= u, l.entangledLanes &= u, l.errorRecoveryDisabledLanes &= u, l.shellSuspendCounter = 0;
    var c = l.entanglements, i = l.expirationTimes, d = l.hiddenUpdates;
    for (u = f & ~u; 0 < u; ) {
      var b = 31 - kl(u), E = 1 << b;
      c[b] = 0, i[b] = -1;
      var S = d[b];
      if (S !== null)
        for (d[b] = null, b = 0; b < S.length; b++) {
          var g = S[b];
          g !== null && (g.lane &= -536870913);
        }
      u &= ~E;
    }
    a !== 0 && Mi(l, a, 0), n !== 0 && e === 0 && l.tag !== 0 && (l.suspendedLanes |= n & ~(f & ~t));
  }
  function Mi(l, t, u) {
    l.pendingLanes |= t, l.suspendedLanes &= ~t;
    var a = 31 - kl(t);
    l.entangledLanes |= t, l.entanglements[a] = l.entanglements[a] | 1073741824 | u & 261930;
  }
  function Di(l, t) {
    var u = l.entangledLanes |= t;
    for (l = l.entanglements; u; ) {
      var a = 31 - kl(u), e = 1 << a;
      e & t | l[a] & t && (l[a] |= t), u &= ~e;
    }
  }
  function pi(l, t) {
    var u = t & -t;
    return u = (u & 42) !== 0 ? 1 : In(u), (u & (l.suspendedLanes | t)) !== 0 ? 0 : u;
  }
  function In(l) {
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
  function Pn(l) {
    return l &= -l, 2 < l ? 8 < l ? (l & 134217727) !== 0 ? 32 : 268435456 : 8 : 2;
  }
  function Ui() {
    var l = M.p;
    return l !== 0 ? l : (l = window.event, l === void 0 ? 32 : dv(l.type));
  }
  function Hi(l, t) {
    var u = M.p;
    try {
      return M.p = l, t();
    } finally {
      M.p = u;
    }
  }
  var Ft = Math.random().toString(36).slice(2), Ml = "__reactFiber$" + Ft, jl = "__reactProps$" + Ft, xu = "__reactContainer$" + Ft, lf = "__reactEvents$" + Ft, no = "__reactListeners$" + Ft, fo = "__reactHandles$" + Ft, Ri = "__reactResources$" + Ft, Ra = "__reactMarker$" + Ft;
  function tf(l) {
    delete l[Ml], delete l[jl], delete l[lf], delete l[no], delete l[fo];
  }
  function Vu(l) {
    var t = l[Ml];
    if (t) return t;
    for (var u = l.parentNode; u; ) {
      if (t = u[xu] || u[Ml]) {
        if (u = t.alternate, t.child !== null || u !== null && u.child !== null)
          for (l = Iy(l); l !== null; ) {
            if (u = l[Ml]) return u;
            l = Iy(l);
          }
        return t;
      }
      l = u, u = l.parentNode;
    }
    return null;
  }
  function Ku(l) {
    if (l = l[Ml] || l[xu]) {
      var t = l.tag;
      if (t === 5 || t === 6 || t === 13 || t === 31 || t === 26 || t === 27 || t === 3)
        return l;
    }
    return null;
  }
  function Na(l) {
    var t = l.tag;
    if (t === 5 || t === 26 || t === 27 || t === 6) return l.stateNode;
    throw Error(o(33));
  }
  function Ju(l) {
    var t = l[Ri];
    return t || (t = l[Ri] = { hoistableStyles: /* @__PURE__ */ new Map(), hoistableScripts: /* @__PURE__ */ new Map() }), t;
  }
  function _l(l) {
    l[Ra] = !0;
  }
  var Ni = /* @__PURE__ */ new Set(), Ci = {};
  function Ou(l, t) {
    wu(l, t), wu(l + "Capture", t);
  }
  function wu(l, t) {
    for (Ci[l] = t, l = 0; l < t.length; l++)
      Ni.add(t[l]);
  }
  var co = RegExp(
    "^[:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD][:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD\\-.0-9\\u00B7\\u0300-\\u036F\\u203F-\\u2040]*$"
  ), qi = {}, Bi = {};
  function io(l) {
    return Wn.call(Bi, l) ? !0 : Wn.call(qi, l) ? !1 : co.test(l) ? Bi[l] = !0 : (qi[l] = !0, !1);
  }
  function He(l, t, u) {
    if (io(t))
      if (u === null) l.removeAttribute(t);
      else {
        switch (typeof u) {
          case "undefined":
          case "function":
          case "symbol":
            l.removeAttribute(t);
            return;
          case "boolean":
            var a = t.toLowerCase().slice(0, 5);
            if (a !== "data-" && a !== "aria-") {
              l.removeAttribute(t);
              return;
            }
        }
        l.setAttribute(t, "" + u);
      }
  }
  function Re(l, t, u) {
    if (u === null) l.removeAttribute(t);
    else {
      switch (typeof u) {
        case "undefined":
        case "function":
        case "symbol":
        case "boolean":
          l.removeAttribute(t);
          return;
      }
      l.setAttribute(t, "" + u);
    }
  }
  function Ut(l, t, u, a) {
    if (a === null) l.removeAttribute(u);
    else {
      switch (typeof a) {
        case "undefined":
        case "function":
        case "symbol":
        case "boolean":
          l.removeAttribute(u);
          return;
      }
      l.setAttributeNS(t, u, "" + a);
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
  function Yi(l) {
    var t = l.type;
    return (l = l.nodeName) && l.toLowerCase() === "input" && (t === "checkbox" || t === "radio");
  }
  function so(l, t, u) {
    var a = Object.getOwnPropertyDescriptor(
      l.constructor.prototype,
      t
    );
    if (!l.hasOwnProperty(t) && typeof a < "u" && typeof a.get == "function" && typeof a.set == "function") {
      var e = a.get, n = a.set;
      return Object.defineProperty(l, t, {
        configurable: !0,
        get: function() {
          return e.call(this);
        },
        set: function(f) {
          u = "" + f, n.call(this, f);
        }
      }), Object.defineProperty(l, t, {
        enumerable: a.enumerable
      }), {
        getValue: function() {
          return u;
        },
        setValue: function(f) {
          u = "" + f;
        },
        stopTracking: function() {
          l._valueTracker = null, delete l[t];
        }
      };
    }
  }
  function uf(l) {
    if (!l._valueTracker) {
      var t = Yi(l) ? "checked" : "value";
      l._valueTracker = so(
        l,
        t,
        "" + l[t]
      );
    }
  }
  function Gi(l) {
    if (!l) return !1;
    var t = l._valueTracker;
    if (!t) return !0;
    var u = t.getValue(), a = "";
    return l && (a = Yi(l) ? l.checked ? "true" : "false" : l.value), l = a, l !== u ? (t.setValue(l), !0) : !1;
  }
  function Ne(l) {
    if (l = l || (typeof document < "u" ? document : void 0), typeof l > "u") return null;
    try {
      return l.activeElement || l.body;
    } catch {
      return l.body;
    }
  }
  var yo = /[\n"\\]/g;
  function ct(l) {
    return l.replace(
      yo,
      function(t) {
        return "\\" + t.charCodeAt(0).toString(16) + " ";
      }
    );
  }
  function af(l, t, u, a, e, n, f, c) {
    l.name = "", f != null && typeof f != "function" && typeof f != "symbol" && typeof f != "boolean" ? l.type = f : l.removeAttribute("type"), t != null ? f === "number" ? (t === 0 && l.value === "" || l.value != t) && (l.value = "" + ft(t)) : l.value !== "" + ft(t) && (l.value = "" + ft(t)) : f !== "submit" && f !== "reset" || l.removeAttribute("value"), t != null ? ef(l, f, ft(t)) : u != null ? ef(l, f, ft(u)) : a != null && l.removeAttribute("value"), e == null && n != null && (l.defaultChecked = !!n), e != null && (l.checked = e && typeof e != "function" && typeof e != "symbol"), c != null && typeof c != "function" && typeof c != "symbol" && typeof c != "boolean" ? l.name = "" + ft(c) : l.removeAttribute("name");
  }
  function Xi(l, t, u, a, e, n, f, c) {
    if (n != null && typeof n != "function" && typeof n != "symbol" && typeof n != "boolean" && (l.type = n), t != null || u != null) {
      if (!(n !== "submit" && n !== "reset" || t != null)) {
        uf(l);
        return;
      }
      u = u != null ? "" + ft(u) : "", t = t != null ? "" + ft(t) : u, c || t === l.value || (l.value = t), l.defaultValue = t;
    }
    a = a ?? e, a = typeof a != "function" && typeof a != "symbol" && !!a, l.checked = c ? l.checked : !!a, l.defaultChecked = !!a, f != null && typeof f != "function" && typeof f != "symbol" && typeof f != "boolean" && (l.name = f), uf(l);
  }
  function ef(l, t, u) {
    t === "number" && Ne(l.ownerDocument) === l || l.defaultValue === "" + u || (l.defaultValue = "" + u);
  }
  function Wu(l, t, u, a) {
    if (l = l.options, t) {
      t = {};
      for (var e = 0; e < u.length; e++)
        t["$" + u[e]] = !0;
      for (u = 0; u < l.length; u++)
        e = t.hasOwnProperty("$" + l[u].value), l[u].selected !== e && (l[u].selected = e), e && a && (l[u].defaultSelected = !0);
    } else {
      for (u = "" + ft(u), t = null, e = 0; e < l.length; e++) {
        if (l[e].value === u) {
          l[e].selected = !0, a && (l[e].defaultSelected = !0);
          return;
        }
        t !== null || l[e].disabled || (t = l[e]);
      }
      t !== null && (t.selected = !0);
    }
  }
  function Qi(l, t, u) {
    if (t != null && (t = "" + ft(t), t !== l.value && (l.value = t), u == null)) {
      l.defaultValue !== t && (l.defaultValue = t);
      return;
    }
    l.defaultValue = u != null ? "" + ft(u) : "";
  }
  function ji(l, t, u, a) {
    if (t == null) {
      if (a != null) {
        if (u != null) throw Error(o(92));
        if (St(a)) {
          if (1 < a.length) throw Error(o(93));
          a = a[0];
        }
        u = a;
      }
      u == null && (u = ""), t = u;
    }
    u = ft(t), l.defaultValue = u, a = l.textContent, a === u && a !== "" && a !== null && (l.value = a), uf(l);
  }
  function $u(l, t) {
    if (t) {
      var u = l.firstChild;
      if (u && u === l.lastChild && u.nodeType === 3) {
        u.nodeValue = t;
        return;
      }
    }
    l.textContent = t;
  }
  var vo = new Set(
    "animationIterationCount aspectRatio borderImageOutset borderImageSlice borderImageWidth boxFlex boxFlexGroup boxOrdinalGroup columnCount columns flex flexGrow flexPositive flexShrink flexNegative flexOrder gridArea gridRow gridRowEnd gridRowSpan gridRowStart gridColumn gridColumnEnd gridColumnSpan gridColumnStart fontWeight lineClamp lineHeight opacity order orphans scale tabSize widows zIndex zoom fillOpacity floodOpacity stopOpacity strokeDasharray strokeDashoffset strokeMiterlimit strokeOpacity strokeWidth MozAnimationIterationCount MozBoxFlex MozBoxFlexGroup MozLineClamp msAnimationIterationCount msFlex msZoom msFlexGrow msFlexNegative msFlexOrder msFlexPositive msFlexShrink msGridColumn msGridColumnSpan msGridRow msGridRowSpan WebkitAnimationIterationCount WebkitBoxFlex WebKitBoxFlexGroup WebkitBoxOrdinalGroup WebkitColumnCount WebkitColumns WebkitFlex WebkitFlexGrow WebkitFlexPositive WebkitFlexShrink WebkitLineClamp".split(
      " "
    )
  );
  function Zi(l, t, u) {
    var a = t.indexOf("--") === 0;
    u == null || typeof u == "boolean" || u === "" ? a ? l.setProperty(t, "") : t === "float" ? l.cssFloat = "" : l[t] = "" : a ? l.setProperty(t, u) : typeof u != "number" || u === 0 || vo.has(t) ? t === "float" ? l.cssFloat = u : l[t] = ("" + u).trim() : l[t] = u + "px";
  }
  function Li(l, t, u) {
    if (t != null && typeof t != "object")
      throw Error(o(62));
    if (l = l.style, u != null) {
      for (var a in u)
        !u.hasOwnProperty(a) || t != null && t.hasOwnProperty(a) || (a.indexOf("--") === 0 ? l.setProperty(a, "") : a === "float" ? l.cssFloat = "" : l[a] = "");
      for (var e in t)
        a = t[e], t.hasOwnProperty(e) && u[e] !== a && Zi(l, e, a);
    } else
      for (var n in t)
        t.hasOwnProperty(n) && Zi(l, n, t[n]);
  }
  function nf(l) {
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
  var oo = /* @__PURE__ */ new Map([
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
  ]), mo = /^[\u0000-\u001F ]*j[\r\n\t]*a[\r\n\t]*v[\r\n\t]*a[\r\n\t]*s[\r\n\t]*c[\r\n\t]*r[\r\n\t]*i[\r\n\t]*p[\r\n\t]*t[\r\n\t]*:/i;
  function Ce(l) {
    return mo.test("" + l) ? "javascript:throw new Error('React has blocked a javascript: URL as a security precaution.')" : l;
  }
  function Ht() {
  }
  var ff = null;
  function cf(l) {
    return l = l.target || l.srcElement || window, l.correspondingUseElement && (l = l.correspondingUseElement), l.nodeType === 3 ? l.parentNode : l;
  }
  var Fu = null, ku = null;
  function xi(l) {
    var t = Ku(l);
    if (t && (l = t.stateNode)) {
      var u = l[jl] || null;
      l: switch (l = t.stateNode, t.type) {
        case "input":
          if (af(
            l,
            u.value,
            u.defaultValue,
            u.defaultValue,
            u.checked,
            u.defaultChecked,
            u.type,
            u.name
          ), t = u.name, u.type === "radio" && t != null) {
            for (u = l; u.parentNode; ) u = u.parentNode;
            for (u = u.querySelectorAll(
              'input[name="' + ct(
                "" + t
              ) + '"][type="radio"]'
            ), t = 0; t < u.length; t++) {
              var a = u[t];
              if (a !== l && a.form === l.form) {
                var e = a[jl] || null;
                if (!e) throw Error(o(90));
                af(
                  a,
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
            for (t = 0; t < u.length; t++)
              a = u[t], a.form === l.form && Gi(a);
          }
          break l;
        case "textarea":
          Qi(l, u.value, u.defaultValue);
          break l;
        case "select":
          t = u.value, t != null && Wu(l, !!u.multiple, t, !1);
      }
    }
  }
  var sf = !1;
  function Vi(l, t, u) {
    if (sf) return l(t, u);
    sf = !0;
    try {
      var a = l(t);
      return a;
    } finally {
      if (sf = !1, (Fu !== null || ku !== null) && (En(), Fu && (t = Fu, l = ku, ku = Fu = null, xi(t), l)))
        for (t = 0; t < l.length; t++) xi(l[t]);
    }
  }
  function Ca(l, t) {
    var u = l.stateNode;
    if (u === null) return null;
    var a = u[jl] || null;
    if (a === null) return null;
    u = a[t];
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
        (a = !a.disabled) || (l = l.type, a = !(l === "button" || l === "input" || l === "select" || l === "textarea")), l = !a;
        break l;
      default:
        l = !1;
    }
    if (l) return null;
    if (u && typeof u != "function")
      throw Error(
        o(231, t, typeof u)
      );
    return u;
  }
  var Rt = !(typeof window > "u" || typeof window.document > "u" || typeof window.document.createElement > "u"), yf = !1;
  if (Rt)
    try {
      var qa = {};
      Object.defineProperty(qa, "passive", {
        get: function() {
          yf = !0;
        }
      }), window.addEventListener("test", qa, qa), window.removeEventListener("test", qa, qa);
    } catch {
      yf = !1;
    }
  var kt = null, vf = null, qe = null;
  function Ki() {
    if (qe) return qe;
    var l, t = vf, u = t.length, a, e = "value" in kt ? kt.value : kt.textContent, n = e.length;
    for (l = 0; l < u && t[l] === e[l]; l++) ;
    var f = u - l;
    for (a = 1; a <= f && t[u - a] === e[n - a]; a++) ;
    return qe = e.slice(l, 1 < a ? 1 - a : void 0);
  }
  function Be(l) {
    var t = l.keyCode;
    return "charCode" in l ? (l = l.charCode, l === 0 && t === 13 && (l = 13)) : l = t, l === 10 && (l = 13), 32 <= l || l === 13 ? l : 0;
  }
  function Ye() {
    return !0;
  }
  function Ji() {
    return !1;
  }
  function Zl(l) {
    function t(u, a, e, n, f) {
      this._reactName = u, this._targetInst = e, this.type = a, this.nativeEvent = n, this.target = f, this.currentTarget = null;
      for (var c in l)
        l.hasOwnProperty(c) && (u = l[c], this[c] = u ? u(n) : n[c]);
      return this.isDefaultPrevented = (n.defaultPrevented != null ? n.defaultPrevented : n.returnValue === !1) ? Ye : Ji, this.isPropagationStopped = Ji, this;
    }
    return q(t.prototype, {
      preventDefault: function() {
        this.defaultPrevented = !0;
        var u = this.nativeEvent;
        u && (u.preventDefault ? u.preventDefault() : typeof u.returnValue != "unknown" && (u.returnValue = !1), this.isDefaultPrevented = Ye);
      },
      stopPropagation: function() {
        var u = this.nativeEvent;
        u && (u.stopPropagation ? u.stopPropagation() : typeof u.cancelBubble != "unknown" && (u.cancelBubble = !0), this.isPropagationStopped = Ye);
      },
      persist: function() {
      },
      isPersistent: Ye
    }), t;
  }
  var Mu = {
    eventPhase: 0,
    bubbles: 0,
    cancelable: 0,
    timeStamp: function(l) {
      return l.timeStamp || Date.now();
    },
    defaultPrevented: 0,
    isTrusted: 0
  }, Ge = Zl(Mu), Ba = q({}, Mu, { view: 0, detail: 0 }), ho = Zl(Ba), of, mf, Ya, Xe = q({}, Ba, {
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
    getModifierState: hf,
    button: 0,
    buttons: 0,
    relatedTarget: function(l) {
      return l.relatedTarget === void 0 ? l.fromElement === l.srcElement ? l.toElement : l.fromElement : l.relatedTarget;
    },
    movementX: function(l) {
      return "movementX" in l ? l.movementX : (l !== Ya && (Ya && l.type === "mousemove" ? (of = l.screenX - Ya.screenX, mf = l.screenY - Ya.screenY) : mf = of = 0, Ya = l), of);
    },
    movementY: function(l) {
      return "movementY" in l ? l.movementY : mf;
    }
  }), wi = Zl(Xe), So = q({}, Xe, { dataTransfer: 0 }), go = Zl(So), bo = q({}, Ba, { relatedTarget: 0 }), df = Zl(bo), ro = q({}, Mu, {
    animationName: 0,
    elapsedTime: 0,
    pseudoElement: 0
  }), zo = Zl(ro), Eo = q({}, Mu, {
    clipboardData: function(l) {
      return "clipboardData" in l ? l.clipboardData : window.clipboardData;
    }
  }), To = Zl(Eo), Ao = q({}, Mu, { data: 0 }), Wi = Zl(Ao), _o = {
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
  }, Oo = {
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
  }, Mo = {
    Alt: "altKey",
    Control: "ctrlKey",
    Meta: "metaKey",
    Shift: "shiftKey"
  };
  function Do(l) {
    var t = this.nativeEvent;
    return t.getModifierState ? t.getModifierState(l) : (l = Mo[l]) ? !!t[l] : !1;
  }
  function hf() {
    return Do;
  }
  var po = q({}, Ba, {
    key: function(l) {
      if (l.key) {
        var t = _o[l.key] || l.key;
        if (t !== "Unidentified") return t;
      }
      return l.type === "keypress" ? (l = Be(l), l === 13 ? "Enter" : String.fromCharCode(l)) : l.type === "keydown" || l.type === "keyup" ? Oo[l.keyCode] || "Unidentified" : "";
    },
    code: 0,
    location: 0,
    ctrlKey: 0,
    shiftKey: 0,
    altKey: 0,
    metaKey: 0,
    repeat: 0,
    locale: 0,
    getModifierState: hf,
    charCode: function(l) {
      return l.type === "keypress" ? Be(l) : 0;
    },
    keyCode: function(l) {
      return l.type === "keydown" || l.type === "keyup" ? l.keyCode : 0;
    },
    which: function(l) {
      return l.type === "keypress" ? Be(l) : l.type === "keydown" || l.type === "keyup" ? l.keyCode : 0;
    }
  }), Uo = Zl(po), Ho = q({}, Xe, {
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
  }), $i = Zl(Ho), Ro = q({}, Ba, {
    touches: 0,
    targetTouches: 0,
    changedTouches: 0,
    altKey: 0,
    metaKey: 0,
    ctrlKey: 0,
    shiftKey: 0,
    getModifierState: hf
  }), No = Zl(Ro), Co = q({}, Mu, {
    propertyName: 0,
    elapsedTime: 0,
    pseudoElement: 0
  }), qo = Zl(Co), Bo = q({}, Xe, {
    deltaX: function(l) {
      return "deltaX" in l ? l.deltaX : "wheelDeltaX" in l ? -l.wheelDeltaX : 0;
    },
    deltaY: function(l) {
      return "deltaY" in l ? l.deltaY : "wheelDeltaY" in l ? -l.wheelDeltaY : "wheelDelta" in l ? -l.wheelDelta : 0;
    },
    deltaZ: 0,
    deltaMode: 0
  }), Yo = Zl(Bo), Go = q({}, Mu, {
    newState: 0,
    oldState: 0
  }), Xo = Zl(Go), Qo = [9, 13, 27, 32], Sf = Rt && "CompositionEvent" in window, Ga = null;
  Rt && "documentMode" in document && (Ga = document.documentMode);
  var jo = Rt && "TextEvent" in window && !Ga, Fi = Rt && (!Sf || Ga && 8 < Ga && 11 >= Ga), ki = " ", Ii = !1;
  function Pi(l, t) {
    switch (l) {
      case "keyup":
        return Qo.indexOf(t.keyCode) !== -1;
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
  function ls(l) {
    return l = l.detail, typeof l == "object" && "data" in l ? l.data : null;
  }
  var Iu = !1;
  function Zo(l, t) {
    switch (l) {
      case "compositionend":
        return ls(t);
      case "keypress":
        return t.which !== 32 ? null : (Ii = !0, ki);
      case "textInput":
        return l = t.data, l === ki && Ii ? null : l;
      default:
        return null;
    }
  }
  function Lo(l, t) {
    if (Iu)
      return l === "compositionend" || !Sf && Pi(l, t) ? (l = Ki(), qe = vf = kt = null, Iu = !1, l) : null;
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
        return Fi && t.locale !== "ko" ? null : t.data;
      default:
        return null;
    }
  }
  var xo = {
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
  function ts(l) {
    var t = l && l.nodeName && l.nodeName.toLowerCase();
    return t === "input" ? !!xo[l.type] : t === "textarea";
  }
  function us(l, t, u, a) {
    Fu ? ku ? ku.push(a) : ku = [a] : Fu = a, t = pn(t, "onChange"), 0 < t.length && (u = new Ge(
      "onChange",
      "change",
      null,
      u,
      a
    ), l.push({ event: u, listeners: t }));
  }
  var Xa = null, Qa = null;
  function Vo(l) {
    Xy(l, 0);
  }
  function Qe(l) {
    var t = Na(l);
    if (Gi(t)) return l;
  }
  function as(l, t) {
    if (l === "change") return t;
  }
  var es = !1;
  if (Rt) {
    var gf;
    if (Rt) {
      var bf = "oninput" in document;
      if (!bf) {
        var ns = document.createElement("div");
        ns.setAttribute("oninput", "return;"), bf = typeof ns.oninput == "function";
      }
      gf = bf;
    } else gf = !1;
    es = gf && (!document.documentMode || 9 < document.documentMode);
  }
  function fs() {
    Xa && (Xa.detachEvent("onpropertychange", cs), Qa = Xa = null);
  }
  function cs(l) {
    if (l.propertyName === "value" && Qe(Qa)) {
      var t = [];
      us(
        t,
        Qa,
        l,
        cf(l)
      ), Vi(Vo, t);
    }
  }
  function Ko(l, t, u) {
    l === "focusin" ? (fs(), Xa = t, Qa = u, Xa.attachEvent("onpropertychange", cs)) : l === "focusout" && fs();
  }
  function Jo(l) {
    if (l === "selectionchange" || l === "keyup" || l === "keydown")
      return Qe(Qa);
  }
  function wo(l, t) {
    if (l === "click") return Qe(t);
  }
  function Wo(l, t) {
    if (l === "input" || l === "change")
      return Qe(t);
  }
  function $o(l, t) {
    return l === t && (l !== 0 || 1 / l === 1 / t) || l !== l && t !== t;
  }
  var Il = typeof Object.is == "function" ? Object.is : $o;
  function ja(l, t) {
    if (Il(l, t)) return !0;
    if (typeof l != "object" || l === null || typeof t != "object" || t === null)
      return !1;
    var u = Object.keys(l), a = Object.keys(t);
    if (u.length !== a.length) return !1;
    for (a = 0; a < u.length; a++) {
      var e = u[a];
      if (!Wn.call(t, e) || !Il(l[e], t[e]))
        return !1;
    }
    return !0;
  }
  function is(l) {
    for (; l && l.firstChild; ) l = l.firstChild;
    return l;
  }
  function ss(l, t) {
    var u = is(l);
    l = 0;
    for (var a; u; ) {
      if (u.nodeType === 3) {
        if (a = l + u.textContent.length, l <= t && a >= t)
          return { node: u, offset: t - l };
        l = a;
      }
      l: {
        for (; u; ) {
          if (u.nextSibling) {
            u = u.nextSibling;
            break l;
          }
          u = u.parentNode;
        }
        u = void 0;
      }
      u = is(u);
    }
  }
  function ys(l, t) {
    return l && t ? l === t ? !0 : l && l.nodeType === 3 ? !1 : t && t.nodeType === 3 ? ys(l, t.parentNode) : "contains" in l ? l.contains(t) : l.compareDocumentPosition ? !!(l.compareDocumentPosition(t) & 16) : !1 : !1;
  }
  function vs(l) {
    l = l != null && l.ownerDocument != null && l.ownerDocument.defaultView != null ? l.ownerDocument.defaultView : window;
    for (var t = Ne(l.document); t instanceof l.HTMLIFrameElement; ) {
      try {
        var u = typeof t.contentWindow.location.href == "string";
      } catch {
        u = !1;
      }
      if (u) l = t.contentWindow;
      else break;
      t = Ne(l.document);
    }
    return t;
  }
  function rf(l) {
    var t = l && l.nodeName && l.nodeName.toLowerCase();
    return t && (t === "input" && (l.type === "text" || l.type === "search" || l.type === "tel" || l.type === "url" || l.type === "password") || t === "textarea" || l.contentEditable === "true");
  }
  var Fo = Rt && "documentMode" in document && 11 >= document.documentMode, Pu = null, zf = null, Za = null, Ef = !1;
  function os(l, t, u) {
    var a = u.window === u ? u.document : u.nodeType === 9 ? u : u.ownerDocument;
    Ef || Pu == null || Pu !== Ne(a) || (a = Pu, "selectionStart" in a && rf(a) ? a = { start: a.selectionStart, end: a.selectionEnd } : (a = (a.ownerDocument && a.ownerDocument.defaultView || window).getSelection(), a = {
      anchorNode: a.anchorNode,
      anchorOffset: a.anchorOffset,
      focusNode: a.focusNode,
      focusOffset: a.focusOffset
    }), Za && ja(Za, a) || (Za = a, a = pn(zf, "onSelect"), 0 < a.length && (t = new Ge(
      "onSelect",
      "select",
      null,
      t,
      u
    ), l.push({ event: t, listeners: a }), t.target = Pu)));
  }
  function Du(l, t) {
    var u = {};
    return u[l.toLowerCase()] = t.toLowerCase(), u["Webkit" + l] = "webkit" + t, u["Moz" + l] = "moz" + t, u;
  }
  var la = {
    animationend: Du("Animation", "AnimationEnd"),
    animationiteration: Du("Animation", "AnimationIteration"),
    animationstart: Du("Animation", "AnimationStart"),
    transitionrun: Du("Transition", "TransitionRun"),
    transitionstart: Du("Transition", "TransitionStart"),
    transitioncancel: Du("Transition", "TransitionCancel"),
    transitionend: Du("Transition", "TransitionEnd")
  }, Tf = {}, ms = {};
  Rt && (ms = document.createElement("div").style, "AnimationEvent" in window || (delete la.animationend.animation, delete la.animationiteration.animation, delete la.animationstart.animation), "TransitionEvent" in window || delete la.transitionend.transition);
  function pu(l) {
    if (Tf[l]) return Tf[l];
    if (!la[l]) return l;
    var t = la[l], u;
    for (u in t)
      if (t.hasOwnProperty(u) && u in ms)
        return Tf[l] = t[u];
    return l;
  }
  var ds = pu("animationend"), hs = pu("animationiteration"), Ss = pu("animationstart"), ko = pu("transitionrun"), Io = pu("transitionstart"), Po = pu("transitioncancel"), gs = pu("transitionend"), bs = /* @__PURE__ */ new Map(), Af = "abort auxClick beforeToggle cancel canPlay canPlayThrough click close contextMenu copy cut drag dragEnd dragEnter dragExit dragLeave dragOver dragStart drop durationChange emptied encrypted ended error gotPointerCapture input invalid keyDown keyPress keyUp load loadedData loadedMetadata loadStart lostPointerCapture mouseDown mouseMove mouseOut mouseOver mouseUp paste pause play playing pointerCancel pointerDown pointerMove pointerOut pointerOver pointerUp progress rateChange reset resize seeked seeking stalled submit suspend timeUpdate touchCancel touchEnd touchStart volumeChange scroll toggle touchMove waiting wheel".split(
    " "
  );
  Af.push("scrollEnd");
  function gt(l, t) {
    bs.set(l, t), Ou(t, [l]);
  }
  var je = typeof reportError == "function" ? reportError : function(l) {
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
  }, it = [], ta = 0, _f = 0;
  function Ze() {
    for (var l = ta, t = _f = ta = 0; t < l; ) {
      var u = it[t];
      it[t++] = null;
      var a = it[t];
      it[t++] = null;
      var e = it[t];
      it[t++] = null;
      var n = it[t];
      if (it[t++] = null, a !== null && e !== null) {
        var f = a.pending;
        f === null ? e.next = e : (e.next = f.next, f.next = e), a.pending = e;
      }
      n !== 0 && rs(u, e, n);
    }
  }
  function Le(l, t, u, a) {
    it[ta++] = l, it[ta++] = t, it[ta++] = u, it[ta++] = a, _f |= a, l.lanes |= a, l = l.alternate, l !== null && (l.lanes |= a);
  }
  function Of(l, t, u, a) {
    return Le(l, t, u, a), xe(l);
  }
  function Uu(l, t) {
    return Le(l, null, null, t), xe(l);
  }
  function rs(l, t, u) {
    l.lanes |= u;
    var a = l.alternate;
    a !== null && (a.lanes |= u);
    for (var e = !1, n = l.return; n !== null; )
      n.childLanes |= u, a = n.alternate, a !== null && (a.childLanes |= u), n.tag === 22 && (l = n.stateNode, l === null || l._visibility & 1 || (e = !0)), l = n, n = n.return;
    return l.tag === 3 ? (n = l.stateNode, e && t !== null && (e = 31 - kl(u), l = n.hiddenUpdates, a = l[e], a === null ? l[e] = [t] : a.push(t), t.lane = u | 536870912), n) : null;
  }
  function xe(l) {
    if (50 < se)
      throw se = 0, qc = null, Error(o(185));
    for (var t = l.return; t !== null; )
      l = t, t = l.return;
    return l.tag === 3 ? l.stateNode : null;
  }
  var ua = {};
  function lm(l, t, u, a) {
    this.tag = l, this.key = u, this.sibling = this.child = this.return = this.stateNode = this.type = this.elementType = null, this.index = 0, this.refCleanup = this.ref = null, this.pendingProps = t, this.dependencies = this.memoizedState = this.updateQueue = this.memoizedProps = null, this.mode = a, this.subtreeFlags = this.flags = 0, this.deletions = null, this.childLanes = this.lanes = 0, this.alternate = null;
  }
  function Pl(l, t, u, a) {
    return new lm(l, t, u, a);
  }
  function Mf(l) {
    return l = l.prototype, !(!l || !l.isReactComponent);
  }
  function Nt(l, t) {
    var u = l.alternate;
    return u === null ? (u = Pl(
      l.tag,
      t,
      l.key,
      l.mode
    ), u.elementType = l.elementType, u.type = l.type, u.stateNode = l.stateNode, u.alternate = l, l.alternate = u) : (u.pendingProps = t, u.type = l.type, u.flags = 0, u.subtreeFlags = 0, u.deletions = null), u.flags = l.flags & 65011712, u.childLanes = l.childLanes, u.lanes = l.lanes, u.child = l.child, u.memoizedProps = l.memoizedProps, u.memoizedState = l.memoizedState, u.updateQueue = l.updateQueue, t = l.dependencies, u.dependencies = t === null ? null : { lanes: t.lanes, firstContext: t.firstContext }, u.sibling = l.sibling, u.index = l.index, u.ref = l.ref, u.refCleanup = l.refCleanup, u;
  }
  function zs(l, t) {
    l.flags &= 65011714;
    var u = l.alternate;
    return u === null ? (l.childLanes = 0, l.lanes = t, l.child = null, l.subtreeFlags = 0, l.memoizedProps = null, l.memoizedState = null, l.updateQueue = null, l.dependencies = null, l.stateNode = null) : (l.childLanes = u.childLanes, l.lanes = u.lanes, l.child = u.child, l.subtreeFlags = 0, l.deletions = null, l.memoizedProps = u.memoizedProps, l.memoizedState = u.memoizedState, l.updateQueue = u.updateQueue, l.type = u.type, t = u.dependencies, l.dependencies = t === null ? null : {
      lanes: t.lanes,
      firstContext: t.firstContext
    }), l;
  }
  function Ve(l, t, u, a, e, n) {
    var f = 0;
    if (a = l, typeof l == "function") Mf(l) && (f = 1);
    else if (typeof l == "string")
      f = nd(
        l,
        u,
        U.current
      ) ? 26 : l === "html" || l === "head" || l === "body" ? 27 : 5;
    else
      l: switch (l) {
        case Et:
          return l = Pl(31, u, t, e), l.elementType = Et, l.lanes = n, l;
        case Cl:
          return Hu(u.children, e, n, t);
        case Dt:
          f = 8, e |= 24;
          break;
        case Wl:
          return l = Pl(12, u, t, e | 2), l.elementType = Wl, l.lanes = n, l;
        case zt:
          return l = Pl(13, u, t, e), l.elementType = zt, l.lanes = n, l;
        case Gl:
          return l = Pl(19, u, t, e), l.elementType = Gl, l.lanes = n, l;
        default:
          if (typeof l == "object" && l !== null)
            switch (l.$$typeof) {
              case Rl:
                f = 10;
                break l;
              case Wt:
                f = 9;
                break l;
              case nt:
                f = 11;
                break l;
              case J:
                f = 14;
                break l;
              case Xl:
                f = 16, a = null;
                break l;
            }
          f = 29, u = Error(
            o(130, l === null ? "null" : typeof l, "")
          ), a = null;
      }
    return t = Pl(f, u, t, e), t.elementType = l, t.type = a, t.lanes = n, t;
  }
  function Hu(l, t, u, a) {
    return l = Pl(7, l, a, t), l.lanes = u, l;
  }
  function Df(l, t, u) {
    return l = Pl(6, l, null, t), l.lanes = u, l;
  }
  function Es(l) {
    var t = Pl(18, null, null, 0);
    return t.stateNode = l, t;
  }
  function pf(l, t, u) {
    return t = Pl(
      4,
      l.children !== null ? l.children : [],
      l.key,
      t
    ), t.lanes = u, t.stateNode = {
      containerInfo: l.containerInfo,
      pendingChildren: null,
      implementation: l.implementation
    }, t;
  }
  var Ts = /* @__PURE__ */ new WeakMap();
  function st(l, t) {
    if (typeof l == "object" && l !== null) {
      var u = Ts.get(l);
      return u !== void 0 ? u : (t = {
        value: l,
        source: t,
        stack: Ei(t)
      }, Ts.set(l, t), t);
    }
    return {
      value: l,
      source: t,
      stack: Ei(t)
    };
  }
  var aa = [], ea = 0, Ke = null, La = 0, yt = [], vt = 0, It = null, At = 1, _t = "";
  function Ct(l, t) {
    aa[ea++] = La, aa[ea++] = Ke, Ke = l, La = t;
  }
  function As(l, t, u) {
    yt[vt++] = At, yt[vt++] = _t, yt[vt++] = It, It = l;
    var a = At;
    l = _t;
    var e = 32 - kl(a) - 1;
    a &= ~(1 << e), u += 1;
    var n = 32 - kl(t) + e;
    if (30 < n) {
      var f = e - e % 5;
      n = (a & (1 << f) - 1).toString(32), a >>= f, e -= f, At = 1 << 32 - kl(t) + e | u << e | a, _t = n + l;
    } else
      At = 1 << n | u << e | a, _t = l;
  }
  function Uf(l) {
    l.return !== null && (Ct(l, 1), As(l, 1, 0));
  }
  function Hf(l) {
    for (; l === Ke; )
      Ke = aa[--ea], aa[ea] = null, La = aa[--ea], aa[ea] = null;
    for (; l === It; )
      It = yt[--vt], yt[vt] = null, _t = yt[--vt], yt[vt] = null, At = yt[--vt], yt[vt] = null;
  }
  function _s(l, t) {
    yt[vt++] = At, yt[vt++] = _t, yt[vt++] = It, At = t.id, _t = t.overflow, It = l;
  }
  var Dl = null, yl = null, w = !1, Pt = null, ot = !1, Rf = Error(o(519));
  function lu(l) {
    var t = Error(
      o(
        418,
        1 < arguments.length && arguments[1] !== void 0 && arguments[1] ? "text" : "HTML",
        ""
      )
    );
    throw xa(st(t, l)), Rf;
  }
  function Os(l) {
    var t = l.stateNode, u = l.type, a = l.memoizedProps;
    switch (t[Ml] = l, t[jl] = a, u) {
      case "dialog":
        x("cancel", t), x("close", t);
        break;
      case "iframe":
      case "object":
      case "embed":
        x("load", t);
        break;
      case "video":
      case "audio":
        for (u = 0; u < ve.length; u++)
          x(ve[u], t);
        break;
      case "source":
        x("error", t);
        break;
      case "img":
      case "image":
      case "link":
        x("error", t), x("load", t);
        break;
      case "details":
        x("toggle", t);
        break;
      case "input":
        x("invalid", t), Xi(
          t,
          a.value,
          a.defaultValue,
          a.checked,
          a.defaultChecked,
          a.type,
          a.name,
          !0
        );
        break;
      case "select":
        x("invalid", t);
        break;
      case "textarea":
        x("invalid", t), ji(t, a.value, a.defaultValue, a.children);
    }
    u = a.children, typeof u != "string" && typeof u != "number" && typeof u != "bigint" || t.textContent === "" + u || a.suppressHydrationWarning === !0 || Ly(t.textContent, u) ? (a.popover != null && (x("beforetoggle", t), x("toggle", t)), a.onScroll != null && x("scroll", t), a.onScrollEnd != null && x("scrollend", t), a.onClick != null && (t.onclick = Ht), t = !0) : t = !1, t || lu(l, !0);
  }
  function Ms(l) {
    for (Dl = l.return; Dl; )
      switch (Dl.tag) {
        case 5:
        case 31:
        case 13:
          ot = !1;
          return;
        case 27:
        case 3:
          ot = !0;
          return;
        default:
          Dl = Dl.return;
      }
  }
  function na(l) {
    if (l !== Dl) return !1;
    if (!w) return Ms(l), w = !0, !1;
    var t = l.tag, u;
    if ((u = t !== 3 && t !== 27) && ((u = t === 5) && (u = l.type, u = !(u !== "form" && u !== "button") || $c(l.type, l.memoizedProps)), u = !u), u && yl && lu(l), Ms(l), t === 13) {
      if (l = l.memoizedState, l = l !== null ? l.dehydrated : null, !l) throw Error(o(317));
      yl = ky(l);
    } else if (t === 31) {
      if (l = l.memoizedState, l = l !== null ? l.dehydrated : null, !l) throw Error(o(317));
      yl = ky(l);
    } else
      t === 27 ? (t = yl, du(l.type) ? (l = li, li = null, yl = l) : yl = t) : yl = Dl ? dt(l.stateNode.nextSibling) : null;
    return !0;
  }
  function Ru() {
    yl = Dl = null, w = !1;
  }
  function Nf() {
    var l = Pt;
    return l !== null && (Kl === null ? Kl = l : Kl.push.apply(
      Kl,
      l
    ), Pt = null), l;
  }
  function xa(l) {
    Pt === null ? Pt = [l] : Pt.push(l);
  }
  var Cf = y(null), Nu = null, qt = null;
  function tu(l, t, u) {
    D(Cf, t._currentValue), t._currentValue = u;
  }
  function Bt(l) {
    l._currentValue = Cf.current, T(Cf);
  }
  function qf(l, t, u) {
    for (; l !== null; ) {
      var a = l.alternate;
      if ((l.childLanes & t) !== t ? (l.childLanes |= t, a !== null && (a.childLanes |= t)) : a !== null && (a.childLanes & t) !== t && (a.childLanes |= t), l === u) break;
      l = l.return;
    }
  }
  function Bf(l, t, u, a) {
    var e = l.child;
    for (e !== null && (e.return = l); e !== null; ) {
      var n = e.dependencies;
      if (n !== null) {
        var f = e.child;
        n = n.firstContext;
        l: for (; n !== null; ) {
          var c = n;
          n = e;
          for (var i = 0; i < t.length; i++)
            if (c.context === t[i]) {
              n.lanes |= u, c = n.alternate, c !== null && (c.lanes |= u), qf(
                n.return,
                u,
                l
              ), a || (f = null);
              break l;
            }
          n = c.next;
        }
      } else if (e.tag === 18) {
        if (f = e.return, f === null) throw Error(o(341));
        f.lanes |= u, n = f.alternate, n !== null && (n.lanes |= u), qf(f, u, l), f = null;
      } else f = e.child;
      if (f !== null) f.return = e;
      else
        for (f = e; f !== null; ) {
          if (f === l) {
            f = null;
            break;
          }
          if (e = f.sibling, e !== null) {
            e.return = f.return, f = e;
            break;
          }
          f = f.return;
        }
      e = f;
    }
  }
  function fa(l, t, u, a) {
    l = null;
    for (var e = t, n = !1; e !== null; ) {
      if (!n) {
        if ((e.flags & 524288) !== 0) n = !0;
        else if ((e.flags & 262144) !== 0) break;
      }
      if (e.tag === 10) {
        var f = e.alternate;
        if (f === null) throw Error(o(387));
        if (f = f.memoizedProps, f !== null) {
          var c = e.type;
          Il(e.pendingProps.value, f.value) || (l !== null ? l.push(c) : l = [c]);
        }
      } else if (e === P.current) {
        if (f = e.alternate, f === null) throw Error(o(387));
        f.memoizedState.memoizedState !== e.memoizedState.memoizedState && (l !== null ? l.push(Se) : l = [Se]);
      }
      e = e.return;
    }
    l !== null && Bf(
      t,
      l,
      u,
      a
    ), t.flags |= 262144;
  }
  function Je(l) {
    for (l = l.firstContext; l !== null; ) {
      if (!Il(
        l.context._currentValue,
        l.memoizedValue
      ))
        return !0;
      l = l.next;
    }
    return !1;
  }
  function Cu(l) {
    Nu = l, qt = null, l = l.dependencies, l !== null && (l.firstContext = null);
  }
  function pl(l) {
    return Ds(Nu, l);
  }
  function we(l, t) {
    return Nu === null && Cu(l), Ds(l, t);
  }
  function Ds(l, t) {
    var u = t._currentValue;
    if (t = { context: t, memoizedValue: u, next: null }, qt === null) {
      if (l === null) throw Error(o(308));
      qt = t, l.dependencies = { lanes: 0, firstContext: t }, l.flags |= 524288;
    } else qt = qt.next = t;
    return u;
  }
  var tm = typeof AbortController < "u" ? AbortController : function() {
    var l = [], t = this.signal = {
      aborted: !1,
      addEventListener: function(u, a) {
        l.push(a);
      }
    };
    this.abort = function() {
      t.aborted = !0, l.forEach(function(u) {
        return u();
      });
    };
  }, um = h.unstable_scheduleCallback, am = h.unstable_NormalPriority, rl = {
    $$typeof: Rl,
    Consumer: null,
    Provider: null,
    _currentValue: null,
    _currentValue2: null,
    _threadCount: 0
  };
  function Yf() {
    return {
      controller: new tm(),
      data: /* @__PURE__ */ new Map(),
      refCount: 0
    };
  }
  function Va(l) {
    l.refCount--, l.refCount === 0 && um(am, function() {
      l.controller.abort();
    });
  }
  var Ka = null, Gf = 0, ca = 0, ia = null;
  function em(l, t) {
    if (Ka === null) {
      var u = Ka = [];
      Gf = 0, ca = jc(), ia = {
        status: "pending",
        value: void 0,
        then: function(a) {
          u.push(a);
        }
      };
    }
    return Gf++, t.then(ps, ps), t;
  }
  function ps() {
    if (--Gf === 0 && Ka !== null) {
      ia !== null && (ia.status = "fulfilled");
      var l = Ka;
      Ka = null, ca = 0, ia = null;
      for (var t = 0; t < l.length; t++) (0, l[t])();
    }
  }
  function nm(l, t) {
    var u = [], a = {
      status: "pending",
      value: null,
      reason: null,
      then: function(e) {
        u.push(e);
      }
    };
    return l.then(
      function() {
        a.status = "fulfilled", a.value = t;
        for (var e = 0; e < u.length; e++) (0, u[e])(t);
      },
      function(e) {
        for (a.status = "rejected", a.reason = e, e = 0; e < u.length; e++)
          (0, u[e])(void 0);
      }
    ), a;
  }
  var Us = r.S;
  r.S = function(l, t) {
    oy = $l(), typeof t == "object" && t !== null && typeof t.then == "function" && em(l, t), Us !== null && Us(l, t);
  };
  var qu = y(null);
  function Xf() {
    var l = qu.current;
    return l !== null ? l : sl.pooledCache;
  }
  function We(l, t) {
    t === null ? D(qu, qu.current) : D(qu, t.pool);
  }
  function Hs() {
    var l = Xf();
    return l === null ? null : { parent: rl._currentValue, pool: l };
  }
  var sa = Error(o(460)), Qf = Error(o(474)), $e = Error(o(542)), Fe = { then: function() {
  } };
  function Rs(l) {
    return l = l.status, l === "fulfilled" || l === "rejected";
  }
  function Ns(l, t, u) {
    switch (u = l[u], u === void 0 ? l.push(t) : u !== t && (t.then(Ht, Ht), t = u), t.status) {
      case "fulfilled":
        return t.value;
      case "rejected":
        throw l = t.reason, qs(l), l;
      default:
        if (typeof t.status == "string") t.then(Ht, Ht);
        else {
          if (l = sl, l !== null && 100 < l.shellSuspendCounter)
            throw Error(o(482));
          l = t, l.status = "pending", l.then(
            function(a) {
              if (t.status === "pending") {
                var e = t;
                e.status = "fulfilled", e.value = a;
              }
            },
            function(a) {
              if (t.status === "pending") {
                var e = t;
                e.status = "rejected", e.reason = a;
              }
            }
          );
        }
        switch (t.status) {
          case "fulfilled":
            return t.value;
          case "rejected":
            throw l = t.reason, qs(l), l;
        }
        throw Yu = t, sa;
    }
  }
  function Bu(l) {
    try {
      var t = l._init;
      return t(l._payload);
    } catch (u) {
      throw u !== null && typeof u == "object" && typeof u.then == "function" ? (Yu = u, sa) : u;
    }
  }
  var Yu = null;
  function Cs() {
    if (Yu === null) throw Error(o(459));
    var l = Yu;
    return Yu = null, l;
  }
  function qs(l) {
    if (l === sa || l === $e)
      throw Error(o(483));
  }
  var ya = null, Ja = 0;
  function ke(l) {
    var t = Ja;
    return Ja += 1, ya === null && (ya = []), Ns(ya, l, t);
  }
  function wa(l, t) {
    t = t.props.ref, l.ref = t !== void 0 ? t : null;
  }
  function Ie(l, t) {
    throw t.$$typeof === al ? Error(o(525)) : (l = Object.prototype.toString.call(t), Error(
      o(
        31,
        l === "[object Object]" ? "object with keys {" + Object.keys(t).join(", ") + "}" : l
      )
    ));
  }
  function Bs(l) {
    function t(v, s) {
      if (l) {
        var m = v.deletions;
        m === null ? (v.deletions = [s], v.flags |= 16) : m.push(s);
      }
    }
    function u(v, s) {
      if (!l) return null;
      for (; s !== null; )
        t(v, s), s = s.sibling;
      return null;
    }
    function a(v) {
      for (var s = /* @__PURE__ */ new Map(); v !== null; )
        v.key !== null ? s.set(v.key, v) : s.set(v.index, v), v = v.sibling;
      return s;
    }
    function e(v, s) {
      return v = Nt(v, s), v.index = 0, v.sibling = null, v;
    }
    function n(v, s, m) {
      return v.index = m, l ? (m = v.alternate, m !== null ? (m = m.index, m < s ? (v.flags |= 67108866, s) : m) : (v.flags |= 67108866, s)) : (v.flags |= 1048576, s);
    }
    function f(v) {
      return l && v.alternate === null && (v.flags |= 67108866), v;
    }
    function c(v, s, m, z) {
      return s === null || s.tag !== 6 ? (s = Df(m, v.mode, z), s.return = v, s) : (s = e(s, m), s.return = v, s);
    }
    function i(v, s, m, z) {
      var R = m.type;
      return R === Cl ? b(
        v,
        s,
        m.props.children,
        z,
        m.key
      ) : s !== null && (s.elementType === R || typeof R == "object" && R !== null && R.$$typeof === Xl && Bu(R) === s.type) ? (s = e(s, m.props), wa(s, m), s.return = v, s) : (s = Ve(
        m.type,
        m.key,
        m.props,
        null,
        v.mode,
        z
      ), wa(s, m), s.return = v, s);
    }
    function d(v, s, m, z) {
      return s === null || s.tag !== 4 || s.stateNode.containerInfo !== m.containerInfo || s.stateNode.implementation !== m.implementation ? (s = pf(m, v.mode, z), s.return = v, s) : (s = e(s, m.children || []), s.return = v, s);
    }
    function b(v, s, m, z, R) {
      return s === null || s.tag !== 7 ? (s = Hu(
        m,
        v.mode,
        z,
        R
      ), s.return = v, s) : (s = e(s, m), s.return = v, s);
    }
    function E(v, s, m) {
      if (typeof s == "string" && s !== "" || typeof s == "number" || typeof s == "bigint")
        return s = Df(
          "" + s,
          v.mode,
          m
        ), s.return = v, s;
      if (typeof s == "object" && s !== null) {
        switch (s.$$typeof) {
          case wl:
            return m = Ve(
              s.type,
              s.key,
              s.props,
              null,
              v.mode,
              m
            ), wa(m, s), m.return = v, m;
          case Yl:
            return s = pf(
              s,
              v.mode,
              m
            ), s.return = v, s;
          case Xl:
            return s = Bu(s), E(v, s, m);
        }
        if (St(s) || Ql(s))
          return s = Hu(
            s,
            v.mode,
            m,
            null
          ), s.return = v, s;
        if (typeof s.then == "function")
          return E(v, ke(s), m);
        if (s.$$typeof === Rl)
          return E(
            v,
            we(v, s),
            m
          );
        Ie(v, s);
      }
      return null;
    }
    function S(v, s, m, z) {
      var R = s !== null ? s.key : null;
      if (typeof m == "string" && m !== "" || typeof m == "number" || typeof m == "bigint")
        return R !== null ? null : c(v, s, "" + m, z);
      if (typeof m == "object" && m !== null) {
        switch (m.$$typeof) {
          case wl:
            return m.key === R ? i(v, s, m, z) : null;
          case Yl:
            return m.key === R ? d(v, s, m, z) : null;
          case Xl:
            return m = Bu(m), S(v, s, m, z);
        }
        if (St(m) || Ql(m))
          return R !== null ? null : b(v, s, m, z, null);
        if (typeof m.then == "function")
          return S(
            v,
            s,
            ke(m),
            z
          );
        if (m.$$typeof === Rl)
          return S(
            v,
            s,
            we(v, m),
            z
          );
        Ie(v, m);
      }
      return null;
    }
    function g(v, s, m, z, R) {
      if (typeof z == "string" && z !== "" || typeof z == "number" || typeof z == "bigint")
        return v = v.get(m) || null, c(s, v, "" + z, R);
      if (typeof z == "object" && z !== null) {
        switch (z.$$typeof) {
          case wl:
            return v = v.get(
              z.key === null ? m : z.key
            ) || null, i(s, v, z, R);
          case Yl:
            return v = v.get(
              z.key === null ? m : z.key
            ) || null, d(s, v, z, R);
          case Xl:
            return z = Bu(z), g(
              v,
              s,
              m,
              z,
              R
            );
        }
        if (St(z) || Ql(z))
          return v = v.get(m) || null, b(s, v, z, R, null);
        if (typeof z.then == "function")
          return g(
            v,
            s,
            m,
            ke(z),
            R
          );
        if (z.$$typeof === Rl)
          return g(
            v,
            s,
            m,
            we(s, z),
            R
          );
        Ie(s, z);
      }
      return null;
    }
    function p(v, s, m, z) {
      for (var R = null, $ = null, H = s, Q = s = 0, K = null; H !== null && Q < m.length; Q++) {
        H.index > Q ? (K = H, H = null) : K = H.sibling;
        var F = S(
          v,
          H,
          m[Q],
          z
        );
        if (F === null) {
          H === null && (H = K);
          break;
        }
        l && H && F.alternate === null && t(v, H), s = n(F, s, Q), $ === null ? R = F : $.sibling = F, $ = F, H = K;
      }
      if (Q === m.length)
        return u(v, H), w && Ct(v, Q), R;
      if (H === null) {
        for (; Q < m.length; Q++)
          H = E(v, m[Q], z), H !== null && (s = n(
            H,
            s,
            Q
          ), $ === null ? R = H : $.sibling = H, $ = H);
        return w && Ct(v, Q), R;
      }
      for (H = a(H); Q < m.length; Q++)
        K = g(
          H,
          v,
          Q,
          m[Q],
          z
        ), K !== null && (l && K.alternate !== null && H.delete(
          K.key === null ? Q : K.key
        ), s = n(
          K,
          s,
          Q
        ), $ === null ? R = K : $.sibling = K, $ = K);
      return l && H.forEach(function(ru) {
        return t(v, ru);
      }), w && Ct(v, Q), R;
    }
    function C(v, s, m, z) {
      if (m == null) throw Error(o(151));
      for (var R = null, $ = null, H = s, Q = s = 0, K = null, F = m.next(); H !== null && !F.done; Q++, F = m.next()) {
        H.index > Q ? (K = H, H = null) : K = H.sibling;
        var ru = S(v, H, F.value, z);
        if (ru === null) {
          H === null && (H = K);
          break;
        }
        l && H && ru.alternate === null && t(v, H), s = n(ru, s, Q), $ === null ? R = ru : $.sibling = ru, $ = ru, H = K;
      }
      if (F.done)
        return u(v, H), w && Ct(v, Q), R;
      if (H === null) {
        for (; !F.done; Q++, F = m.next())
          F = E(v, F.value, z), F !== null && (s = n(F, s, Q), $ === null ? R = F : $.sibling = F, $ = F);
        return w && Ct(v, Q), R;
      }
      for (H = a(H); !F.done; Q++, F = m.next())
        F = g(H, v, Q, F.value, z), F !== null && (l && F.alternate !== null && H.delete(F.key === null ? Q : F.key), s = n(F, s, Q), $ === null ? R = F : $.sibling = F, $ = F);
      return l && H.forEach(function(Sd) {
        return t(v, Sd);
      }), w && Ct(v, Q), R;
    }
    function cl(v, s, m, z) {
      if (typeof m == "object" && m !== null && m.type === Cl && m.key === null && (m = m.props.children), typeof m == "object" && m !== null) {
        switch (m.$$typeof) {
          case wl:
            l: {
              for (var R = m.key; s !== null; ) {
                if (s.key === R) {
                  if (R = m.type, R === Cl) {
                    if (s.tag === 7) {
                      u(
                        v,
                        s.sibling
                      ), z = e(
                        s,
                        m.props.children
                      ), z.return = v, v = z;
                      break l;
                    }
                  } else if (s.elementType === R || typeof R == "object" && R !== null && R.$$typeof === Xl && Bu(R) === s.type) {
                    u(
                      v,
                      s.sibling
                    ), z = e(s, m.props), wa(z, m), z.return = v, v = z;
                    break l;
                  }
                  u(v, s);
                  break;
                } else t(v, s);
                s = s.sibling;
              }
              m.type === Cl ? (z = Hu(
                m.props.children,
                v.mode,
                z,
                m.key
              ), z.return = v, v = z) : (z = Ve(
                m.type,
                m.key,
                m.props,
                null,
                v.mode,
                z
              ), wa(z, m), z.return = v, v = z);
            }
            return f(v);
          case Yl:
            l: {
              for (R = m.key; s !== null; ) {
                if (s.key === R)
                  if (s.tag === 4 && s.stateNode.containerInfo === m.containerInfo && s.stateNode.implementation === m.implementation) {
                    u(
                      v,
                      s.sibling
                    ), z = e(s, m.children || []), z.return = v, v = z;
                    break l;
                  } else {
                    u(v, s);
                    break;
                  }
                else t(v, s);
                s = s.sibling;
              }
              z = pf(m, v.mode, z), z.return = v, v = z;
            }
            return f(v);
          case Xl:
            return m = Bu(m), cl(
              v,
              s,
              m,
              z
            );
        }
        if (St(m))
          return p(
            v,
            s,
            m,
            z
          );
        if (Ql(m)) {
          if (R = Ql(m), typeof R != "function") throw Error(o(150));
          return m = R.call(m), C(
            v,
            s,
            m,
            z
          );
        }
        if (typeof m.then == "function")
          return cl(
            v,
            s,
            ke(m),
            z
          );
        if (m.$$typeof === Rl)
          return cl(
            v,
            s,
            we(v, m),
            z
          );
        Ie(v, m);
      }
      return typeof m == "string" && m !== "" || typeof m == "number" || typeof m == "bigint" ? (m = "" + m, s !== null && s.tag === 6 ? (u(v, s.sibling), z = e(s, m), z.return = v, v = z) : (u(v, s), z = Df(m, v.mode, z), z.return = v, v = z), f(v)) : u(v, s);
    }
    return function(v, s, m, z) {
      try {
        Ja = 0;
        var R = cl(
          v,
          s,
          m,
          z
        );
        return ya = null, R;
      } catch (H) {
        if (H === sa || H === $e) throw H;
        var $ = Pl(29, H, null, v.mode);
        return $.lanes = z, $.return = v, $;
      } finally {
      }
    };
  }
  var Gu = Bs(!0), Ys = Bs(!1), uu = !1;
  function jf(l) {
    l.updateQueue = {
      baseState: l.memoizedState,
      firstBaseUpdate: null,
      lastBaseUpdate: null,
      shared: { pending: null, lanes: 0, hiddenCallbacks: null },
      callbacks: null
    };
  }
  function Zf(l, t) {
    l = l.updateQueue, t.updateQueue === l && (t.updateQueue = {
      baseState: l.baseState,
      firstBaseUpdate: l.firstBaseUpdate,
      lastBaseUpdate: l.lastBaseUpdate,
      shared: l.shared,
      callbacks: null
    });
  }
  function au(l) {
    return { lane: l, tag: 0, payload: null, callback: null, next: null };
  }
  function eu(l, t, u) {
    var a = l.updateQueue;
    if (a === null) return null;
    if (a = a.shared, (I & 2) !== 0) {
      var e = a.pending;
      return e === null ? t.next = t : (t.next = e.next, e.next = t), a.pending = t, t = xe(l), rs(l, null, u), t;
    }
    return Le(l, a, t, u), xe(l);
  }
  function Wa(l, t, u) {
    if (t = t.updateQueue, t !== null && (t = t.shared, (u & 4194048) !== 0)) {
      var a = t.lanes;
      a &= l.pendingLanes, u |= a, t.lanes = u, Di(l, u);
    }
  }
  function Lf(l, t) {
    var u = l.updateQueue, a = l.alternate;
    if (a !== null && (a = a.updateQueue, u === a)) {
      var e = null, n = null;
      if (u = u.firstBaseUpdate, u !== null) {
        do {
          var f = {
            lane: u.lane,
            tag: u.tag,
            payload: u.payload,
            callback: null,
            next: null
          };
          n === null ? e = n = f : n = n.next = f, u = u.next;
        } while (u !== null);
        n === null ? e = n = t : n = n.next = t;
      } else e = n = t;
      u = {
        baseState: a.baseState,
        firstBaseUpdate: e,
        lastBaseUpdate: n,
        shared: a.shared,
        callbacks: a.callbacks
      }, l.updateQueue = u;
      return;
    }
    l = u.lastBaseUpdate, l === null ? u.firstBaseUpdate = t : l.next = t, u.lastBaseUpdate = t;
  }
  var xf = !1;
  function $a() {
    if (xf) {
      var l = ia;
      if (l !== null) throw l;
    }
  }
  function Fa(l, t, u, a) {
    xf = !1;
    var e = l.updateQueue;
    uu = !1;
    var n = e.firstBaseUpdate, f = e.lastBaseUpdate, c = e.shared.pending;
    if (c !== null) {
      e.shared.pending = null;
      var i = c, d = i.next;
      i.next = null, f === null ? n = d : f.next = d, f = i;
      var b = l.alternate;
      b !== null && (b = b.updateQueue, c = b.lastBaseUpdate, c !== f && (c === null ? b.firstBaseUpdate = d : c.next = d, b.lastBaseUpdate = i));
    }
    if (n !== null) {
      var E = e.baseState;
      f = 0, b = d = i = null, c = n;
      do {
        var S = c.lane & -536870913, g = S !== c.lane;
        if (g ? (V & S) === S : (a & S) === S) {
          S !== 0 && S === ca && (xf = !0), b !== null && (b = b.next = {
            lane: 0,
            tag: c.tag,
            payload: c.payload,
            callback: null,
            next: null
          });
          l: {
            var p = l, C = c;
            S = t;
            var cl = u;
            switch (C.tag) {
              case 1:
                if (p = C.payload, typeof p == "function") {
                  E = p.call(cl, E, S);
                  break l;
                }
                E = p;
                break l;
              case 3:
                p.flags = p.flags & -65537 | 128;
              case 0:
                if (p = C.payload, S = typeof p == "function" ? p.call(cl, E, S) : p, S == null) break l;
                E = q({}, E, S);
                break l;
              case 2:
                uu = !0;
            }
          }
          S = c.callback, S !== null && (l.flags |= 64, g && (l.flags |= 8192), g = e.callbacks, g === null ? e.callbacks = [S] : g.push(S));
        } else
          g = {
            lane: S,
            tag: c.tag,
            payload: c.payload,
            callback: c.callback,
            next: null
          }, b === null ? (d = b = g, i = E) : b = b.next = g, f |= S;
        if (c = c.next, c === null) {
          if (c = e.shared.pending, c === null)
            break;
          g = c, c = g.next, g.next = null, e.lastBaseUpdate = g, e.shared.pending = null;
        }
      } while (!0);
      b === null && (i = E), e.baseState = i, e.firstBaseUpdate = d, e.lastBaseUpdate = b, n === null && (e.shared.lanes = 0), su |= f, l.lanes = f, l.memoizedState = E;
    }
  }
  function Gs(l, t) {
    if (typeof l != "function")
      throw Error(o(191, l));
    l.call(t);
  }
  function Xs(l, t) {
    var u = l.callbacks;
    if (u !== null)
      for (l.callbacks = null, l = 0; l < u.length; l++)
        Gs(u[l], t);
  }
  var va = y(null), Pe = y(0);
  function Qs(l, t) {
    l = Vt, D(Pe, l), D(va, t), Vt = l | t.baseLanes;
  }
  function Vf() {
    D(Pe, Vt), D(va, va.current);
  }
  function Kf() {
    Vt = Pe.current, T(va), T(Pe);
  }
  var lt = y(null), mt = null;
  function nu(l) {
    var t = l.alternate;
    D(gl, gl.current & 1), D(lt, l), mt === null && (t === null || va.current !== null || t.memoizedState !== null) && (mt = l);
  }
  function Jf(l) {
    D(gl, gl.current), D(lt, l), mt === null && (mt = l);
  }
  function js(l) {
    l.tag === 22 ? (D(gl, gl.current), D(lt, l), mt === null && (mt = l)) : fu();
  }
  function fu() {
    D(gl, gl.current), D(lt, lt.current);
  }
  function tt(l) {
    T(lt), mt === l && (mt = null), T(gl);
  }
  var gl = y(0);
  function ln(l) {
    for (var t = l; t !== null; ) {
      if (t.tag === 13) {
        var u = t.memoizedState;
        if (u !== null && (u = u.dehydrated, u === null || Ic(u) || Pc(u)))
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
  var Yt = 0, X = null, nl = null, zl = null, tn = !1, oa = !1, Xu = !1, un = 0, ka = 0, ma = null, fm = 0;
  function ml() {
    throw Error(o(321));
  }
  function wf(l, t) {
    if (t === null) return !1;
    for (var u = 0; u < t.length && u < l.length; u++)
      if (!Il(l[u], t[u])) return !1;
    return !0;
  }
  function Wf(l, t, u, a, e, n) {
    return Yt = n, X = t, t.memoizedState = null, t.updateQueue = null, t.lanes = 0, r.H = l === null || l.memoizedState === null ? A0 : sc, Xu = !1, n = u(a, e), Xu = !1, oa && (n = Ls(
      t,
      u,
      a,
      e
    )), Zs(l), n;
  }
  function Zs(l) {
    r.H = le;
    var t = nl !== null && nl.next !== null;
    if (Yt = 0, zl = nl = X = null, tn = !1, ka = 0, ma = null, t) throw Error(o(300));
    l === null || El || (l = l.dependencies, l !== null && Je(l) && (El = !0));
  }
  function Ls(l, t, u, a) {
    X = l;
    var e = 0;
    do {
      if (oa && (ma = null), ka = 0, oa = !1, 25 <= e) throw Error(o(301));
      if (e += 1, zl = nl = null, l.updateQueue != null) {
        var n = l.updateQueue;
        n.lastEffect = null, n.events = null, n.stores = null, n.memoCache != null && (n.memoCache.index = 0);
      }
      r.H = _0, n = t(u, a);
    } while (oa);
    return n;
  }
  function cm() {
    var l = r.H, t = l.useState()[0];
    return t = typeof t.then == "function" ? Ia(t) : t, l = l.useState()[0], (nl !== null ? nl.memoizedState : null) !== l && (X.flags |= 1024), t;
  }
  function $f() {
    var l = un !== 0;
    return un = 0, l;
  }
  function Ff(l, t, u) {
    t.updateQueue = l.updateQueue, t.flags &= -2053, l.lanes &= ~u;
  }
  function kf(l) {
    if (tn) {
      for (l = l.memoizedState; l !== null; ) {
        var t = l.queue;
        t !== null && (t.pending = null), l = l.next;
      }
      tn = !1;
    }
    Yt = 0, zl = nl = X = null, oa = !1, ka = un = 0, ma = null;
  }
  function Bl() {
    var l = {
      memoizedState: null,
      baseState: null,
      baseQueue: null,
      queue: null,
      next: null
    };
    return zl === null ? X.memoizedState = zl = l : zl = zl.next = l, zl;
  }
  function bl() {
    if (nl === null) {
      var l = X.alternate;
      l = l !== null ? l.memoizedState : null;
    } else l = nl.next;
    var t = zl === null ? X.memoizedState : zl.next;
    if (t !== null)
      zl = t, nl = l;
    else {
      if (l === null)
        throw X.alternate === null ? Error(o(467)) : Error(o(310));
      nl = l, l = {
        memoizedState: nl.memoizedState,
        baseState: nl.baseState,
        baseQueue: nl.baseQueue,
        queue: nl.queue,
        next: null
      }, zl === null ? X.memoizedState = zl = l : zl = zl.next = l;
    }
    return zl;
  }
  function an() {
    return { lastEffect: null, events: null, stores: null, memoCache: null };
  }
  function Ia(l) {
    var t = ka;
    return ka += 1, ma === null && (ma = []), l = Ns(ma, l, t), t = X, (zl === null ? t.memoizedState : zl.next) === null && (t = t.alternate, r.H = t === null || t.memoizedState === null ? A0 : sc), l;
  }
  function en(l) {
    if (l !== null && typeof l == "object") {
      if (typeof l.then == "function") return Ia(l);
      if (l.$$typeof === Rl) return pl(l);
    }
    throw Error(o(438, String(l)));
  }
  function If(l) {
    var t = null, u = X.updateQueue;
    if (u !== null && (t = u.memoCache), t == null) {
      var a = X.alternate;
      a !== null && (a = a.updateQueue, a !== null && (a = a.memoCache, a != null && (t = {
        data: a.data.map(function(e) {
          return e.slice();
        }),
        index: 0
      })));
    }
    if (t == null && (t = { data: [], index: 0 }), u === null && (u = an(), X.updateQueue = u), u.memoCache = t, u = t.data[t.index], u === void 0)
      for (u = t.data[t.index] = Array(l), a = 0; a < l; a++)
        u[a] = Lu;
    return t.index++, u;
  }
  function Gt(l, t) {
    return typeof t == "function" ? t(l) : t;
  }
  function nn(l) {
    var t = bl();
    return Pf(t, nl, l);
  }
  function Pf(l, t, u) {
    var a = l.queue;
    if (a === null) throw Error(o(311));
    a.lastRenderedReducer = u;
    var e = l.baseQueue, n = a.pending;
    if (n !== null) {
      if (e !== null) {
        var f = e.next;
        e.next = n.next, n.next = f;
      }
      t.baseQueue = e = n, a.pending = null;
    }
    if (n = l.baseState, e === null) l.memoizedState = n;
    else {
      t = e.next;
      var c = f = null, i = null, d = t, b = !1;
      do {
        var E = d.lane & -536870913;
        if (E !== d.lane ? (V & E) === E : (Yt & E) === E) {
          var S = d.revertLane;
          if (S === 0)
            i !== null && (i = i.next = {
              lane: 0,
              revertLane: 0,
              gesture: null,
              action: d.action,
              hasEagerState: d.hasEagerState,
              eagerState: d.eagerState,
              next: null
            }), E === ca && (b = !0);
          else if ((Yt & S) === S) {
            d = d.next, S === ca && (b = !0);
            continue;
          } else
            E = {
              lane: 0,
              revertLane: d.revertLane,
              gesture: null,
              action: d.action,
              hasEagerState: d.hasEagerState,
              eagerState: d.eagerState,
              next: null
            }, i === null ? (c = i = E, f = n) : i = i.next = E, X.lanes |= S, su |= S;
          E = d.action, Xu && u(n, E), n = d.hasEagerState ? d.eagerState : u(n, E);
        } else
          S = {
            lane: E,
            revertLane: d.revertLane,
            gesture: d.gesture,
            action: d.action,
            hasEagerState: d.hasEagerState,
            eagerState: d.eagerState,
            next: null
          }, i === null ? (c = i = S, f = n) : i = i.next = S, X.lanes |= E, su |= E;
        d = d.next;
      } while (d !== null && d !== t);
      if (i === null ? f = n : i.next = c, !Il(n, l.memoizedState) && (El = !0, b && (u = ia, u !== null)))
        throw u;
      l.memoizedState = n, l.baseState = f, l.baseQueue = i, a.lastRenderedState = n;
    }
    return e === null && (a.lanes = 0), [l.memoizedState, a.dispatch];
  }
  function lc(l) {
    var t = bl(), u = t.queue;
    if (u === null) throw Error(o(311));
    u.lastRenderedReducer = l;
    var a = u.dispatch, e = u.pending, n = t.memoizedState;
    if (e !== null) {
      u.pending = null;
      var f = e = e.next;
      do
        n = l(n, f.action), f = f.next;
      while (f !== e);
      Il(n, t.memoizedState) || (El = !0), t.memoizedState = n, t.baseQueue === null && (t.baseState = n), u.lastRenderedState = n;
    }
    return [n, a];
  }
  function xs(l, t, u) {
    var a = X, e = bl(), n = w;
    if (n) {
      if (u === void 0) throw Error(o(407));
      u = u();
    } else u = t();
    var f = !Il(
      (nl || e).memoizedState,
      u
    );
    if (f && (e.memoizedState = u, El = !0), e = e.queue, ac(Js.bind(null, a, e, l), [
      l
    ]), e.getSnapshot !== t || f || zl !== null && zl.memoizedState.tag & 1) {
      if (a.flags |= 2048, da(
        9,
        { destroy: void 0 },
        Ks.bind(
          null,
          a,
          e,
          u,
          t
        ),
        null
      ), sl === null) throw Error(o(349));
      n || (Yt & 127) !== 0 || Vs(a, t, u);
    }
    return u;
  }
  function Vs(l, t, u) {
    l.flags |= 16384, l = { getSnapshot: t, value: u }, t = X.updateQueue, t === null ? (t = an(), X.updateQueue = t, t.stores = [l]) : (u = t.stores, u === null ? t.stores = [l] : u.push(l));
  }
  function Ks(l, t, u, a) {
    t.value = u, t.getSnapshot = a, ws(t) && Ws(l);
  }
  function Js(l, t, u) {
    return u(function() {
      ws(t) && Ws(l);
    });
  }
  function ws(l) {
    var t = l.getSnapshot;
    l = l.value;
    try {
      var u = t();
      return !Il(l, u);
    } catch {
      return !0;
    }
  }
  function Ws(l) {
    var t = Uu(l, 2);
    t !== null && Jl(t, l, 2);
  }
  function tc(l) {
    var t = Bl();
    if (typeof l == "function") {
      var u = l;
      if (l = u(), Xu) {
        $t(!0);
        try {
          u();
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
  function $s(l, t, u, a) {
    return l.baseState = u, Pf(
      l,
      nl,
      typeof a == "function" ? a : Gt
    );
  }
  function im(l, t, u, a, e) {
    if (sn(l)) throw Error(o(485));
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
        then: function(f) {
          n.listeners.push(f);
        }
      };
      r.T !== null ? u(!0) : n.isTransition = !1, a(n), u = t.pending, u === null ? (n.next = t.pending = n, Fs(t, n)) : (n.next = u.next, t.pending = u.next = n);
    }
  }
  function Fs(l, t) {
    var u = t.action, a = t.payload, e = l.state;
    if (t.isTransition) {
      var n = r.T, f = {};
      r.T = f;
      try {
        var c = u(e, a), i = r.S;
        i !== null && i(f, c), ks(l, t, c);
      } catch (d) {
        uc(l, t, d);
      } finally {
        n !== null && f.types !== null && (n.types = f.types), r.T = n;
      }
    } else
      try {
        n = u(e, a), ks(l, t, n);
      } catch (d) {
        uc(l, t, d);
      }
  }
  function ks(l, t, u) {
    u !== null && typeof u == "object" && typeof u.then == "function" ? u.then(
      function(a) {
        Is(l, t, a);
      },
      function(a) {
        return uc(l, t, a);
      }
    ) : Is(l, t, u);
  }
  function Is(l, t, u) {
    t.status = "fulfilled", t.value = u, Ps(t), l.state = u, t = l.pending, t !== null && (u = t.next, u === t ? l.pending = null : (u = u.next, t.next = u, Fs(l, u)));
  }
  function uc(l, t, u) {
    var a = l.pending;
    if (l.pending = null, a !== null) {
      a = a.next;
      do
        t.status = "rejected", t.reason = u, Ps(t), t = t.next;
      while (t !== a);
    }
    l.action = null;
  }
  function Ps(l) {
    l = l.listeners;
    for (var t = 0; t < l.length; t++) (0, l[t])();
  }
  function l0(l, t) {
    return t;
  }
  function t0(l, t) {
    if (w) {
      var u = sl.formState;
      if (u !== null) {
        l: {
          var a = X;
          if (w) {
            if (yl) {
              t: {
                for (var e = yl, n = ot; e.nodeType !== 8; ) {
                  if (!n) {
                    e = null;
                    break t;
                  }
                  if (e = dt(
                    e.nextSibling
                  ), e === null) {
                    e = null;
                    break t;
                  }
                }
                n = e.data, e = n === "F!" || n === "F" ? e : null;
              }
              if (e) {
                yl = dt(
                  e.nextSibling
                ), a = e.data === "F!";
                break l;
              }
            }
            lu(a);
          }
          a = !1;
        }
        a && (t = u[0]);
      }
    }
    return u = Bl(), u.memoizedState = u.baseState = t, a = {
      pending: null,
      lanes: 0,
      dispatch: null,
      lastRenderedReducer: l0,
      lastRenderedState: t
    }, u.queue = a, u = z0.bind(
      null,
      X,
      a
    ), a.dispatch = u, a = tc(!1), n = ic.bind(
      null,
      X,
      !1,
      a.queue
    ), a = Bl(), e = {
      state: t,
      dispatch: null,
      action: l,
      pending: null
    }, a.queue = e, u = im.bind(
      null,
      X,
      e,
      n,
      u
    ), e.dispatch = u, a.memoizedState = l, [t, u, !1];
  }
  function u0(l) {
    var t = bl();
    return a0(t, nl, l);
  }
  function a0(l, t, u) {
    if (t = Pf(
      l,
      t,
      l0
    )[0], l = nn(Gt)[0], typeof t == "object" && t !== null && typeof t.then == "function")
      try {
        var a = Ia(t);
      } catch (f) {
        throw f === sa ? $e : f;
      }
    else a = t;
    t = bl();
    var e = t.queue, n = e.dispatch;
    return u !== t.memoizedState && (X.flags |= 2048, da(
      9,
      { destroy: void 0 },
      sm.bind(null, e, u),
      null
    )), [a, n, l];
  }
  function sm(l, t) {
    l.action = t;
  }
  function e0(l) {
    var t = bl(), u = nl;
    if (u !== null)
      return a0(t, u, l);
    bl(), t = t.memoizedState, u = bl();
    var a = u.queue.dispatch;
    return u.memoizedState = l, [t, a, !1];
  }
  function da(l, t, u, a) {
    return l = { tag: l, create: u, deps: a, inst: t, next: null }, t = X.updateQueue, t === null && (t = an(), X.updateQueue = t), u = t.lastEffect, u === null ? t.lastEffect = l.next = l : (a = u.next, u.next = l, l.next = a, t.lastEffect = l), l;
  }
  function n0() {
    return bl().memoizedState;
  }
  function fn(l, t, u, a) {
    var e = Bl();
    X.flags |= l, e.memoizedState = da(
      1 | t,
      { destroy: void 0 },
      u,
      a === void 0 ? null : a
    );
  }
  function cn(l, t, u, a) {
    var e = bl();
    a = a === void 0 ? null : a;
    var n = e.memoizedState.inst;
    nl !== null && a !== null && wf(a, nl.memoizedState.deps) ? e.memoizedState = da(t, n, u, a) : (X.flags |= l, e.memoizedState = da(
      1 | t,
      n,
      u,
      a
    ));
  }
  function f0(l, t) {
    fn(8390656, 8, l, t);
  }
  function ac(l, t) {
    cn(2048, 8, l, t);
  }
  function ym(l) {
    X.flags |= 4;
    var t = X.updateQueue;
    if (t === null)
      t = an(), X.updateQueue = t, t.events = [l];
    else {
      var u = t.events;
      u === null ? t.events = [l] : u.push(l);
    }
  }
  function c0(l) {
    var t = bl().memoizedState;
    return ym({ ref: t, nextImpl: l }), function() {
      if ((I & 2) !== 0) throw Error(o(440));
      return t.impl.apply(void 0, arguments);
    };
  }
  function i0(l, t) {
    return cn(4, 2, l, t);
  }
  function s0(l, t) {
    return cn(4, 4, l, t);
  }
  function y0(l, t) {
    if (typeof t == "function") {
      l = l();
      var u = t(l);
      return function() {
        typeof u == "function" ? u() : t(null);
      };
    }
    if (t != null)
      return l = l(), t.current = l, function() {
        t.current = null;
      };
  }
  function v0(l, t, u) {
    u = u != null ? u.concat([l]) : null, cn(4, 4, y0.bind(null, t, l), u);
  }
  function ec() {
  }
  function o0(l, t) {
    var u = bl();
    t = t === void 0 ? null : t;
    var a = u.memoizedState;
    return t !== null && wf(t, a[1]) ? a[0] : (u.memoizedState = [l, t], l);
  }
  function m0(l, t) {
    var u = bl();
    t = t === void 0 ? null : t;
    var a = u.memoizedState;
    if (t !== null && wf(t, a[1]))
      return a[0];
    if (a = l(), Xu) {
      $t(!0);
      try {
        l();
      } finally {
        $t(!1);
      }
    }
    return u.memoizedState = [a, t], a;
  }
  function nc(l, t, u) {
    return u === void 0 || (Yt & 1073741824) !== 0 && (V & 261930) === 0 ? l.memoizedState = t : (l.memoizedState = u, l = dy(), X.lanes |= l, su |= l, u);
  }
  function d0(l, t, u, a) {
    return Il(u, t) ? u : va.current !== null ? (l = nc(l, u, a), Il(l, t) || (El = !0), l) : (Yt & 42) === 0 || (Yt & 1073741824) !== 0 && (V & 261930) === 0 ? (El = !0, l.memoizedState = u) : (l = dy(), X.lanes |= l, su |= l, t);
  }
  function h0(l, t, u, a, e) {
    var n = M.p;
    M.p = n !== 0 && 8 > n ? n : 8;
    var f = r.T, c = {};
    r.T = c, ic(l, !1, t, u);
    try {
      var i = e(), d = r.S;
      if (d !== null && d(c, i), i !== null && typeof i == "object" && typeof i.then == "function") {
        var b = nm(
          i,
          a
        );
        Pa(
          l,
          t,
          b,
          et(l)
        );
      } else
        Pa(
          l,
          t,
          a,
          et(l)
        );
    } catch (E) {
      Pa(
        l,
        t,
        { then: function() {
        }, status: "rejected", reason: E },
        et()
      );
    } finally {
      M.p = n, f !== null && c.types !== null && (f.types = c.types), r.T = f;
    }
  }
  function vm() {
  }
  function fc(l, t, u, a) {
    if (l.tag !== 5) throw Error(o(476));
    var e = S0(l).queue;
    h0(
      l,
      e,
      t,
      B,
      u === null ? vm : function() {
        return g0(l), u(a);
      }
    );
  }
  function S0(l) {
    var t = l.memoizedState;
    if (t !== null) return t;
    t = {
      memoizedState: B,
      baseState: B,
      baseQueue: null,
      queue: {
        pending: null,
        lanes: 0,
        dispatch: null,
        lastRenderedReducer: Gt,
        lastRenderedState: B
      },
      next: null
    };
    var u = {};
    return t.next = {
      memoizedState: u,
      baseState: u,
      baseQueue: null,
      queue: {
        pending: null,
        lanes: 0,
        dispatch: null,
        lastRenderedReducer: Gt,
        lastRenderedState: u
      },
      next: null
    }, l.memoizedState = t, l = l.alternate, l !== null && (l.memoizedState = t), t;
  }
  function g0(l) {
    var t = S0(l);
    t.next === null && (t = l.alternate.memoizedState), Pa(
      l,
      t.next.queue,
      {},
      et()
    );
  }
  function cc() {
    return pl(Se);
  }
  function b0() {
    return bl().memoizedState;
  }
  function r0() {
    return bl().memoizedState;
  }
  function om(l) {
    for (var t = l.return; t !== null; ) {
      switch (t.tag) {
        case 24:
        case 3:
          var u = et();
          l = au(u);
          var a = eu(t, l, u);
          a !== null && (Jl(a, t, u), Wa(a, t, u)), t = { cache: Yf() }, l.payload = t;
          return;
      }
      t = t.return;
    }
  }
  function mm(l, t, u) {
    var a = et();
    u = {
      lane: a,
      revertLane: 0,
      gesture: null,
      action: u,
      hasEagerState: !1,
      eagerState: null,
      next: null
    }, sn(l) ? E0(t, u) : (u = Of(l, t, u, a), u !== null && (Jl(u, l, a), T0(u, t, a)));
  }
  function z0(l, t, u) {
    var a = et();
    Pa(l, t, u, a);
  }
  function Pa(l, t, u, a) {
    var e = {
      lane: a,
      revertLane: 0,
      gesture: null,
      action: u,
      hasEagerState: !1,
      eagerState: null,
      next: null
    };
    if (sn(l)) E0(t, e);
    else {
      var n = l.alternate;
      if (l.lanes === 0 && (n === null || n.lanes === 0) && (n = t.lastRenderedReducer, n !== null))
        try {
          var f = t.lastRenderedState, c = n(f, u);
          if (e.hasEagerState = !0, e.eagerState = c, Il(c, f))
            return Le(l, t, e, 0), sl === null && Ze(), !1;
        } catch {
        } finally {
        }
      if (u = Of(l, t, e, a), u !== null)
        return Jl(u, l, a), T0(u, t, a), !0;
    }
    return !1;
  }
  function ic(l, t, u, a) {
    if (a = {
      lane: 2,
      revertLane: jc(),
      gesture: null,
      action: a,
      hasEagerState: !1,
      eagerState: null,
      next: null
    }, sn(l)) {
      if (t) throw Error(o(479));
    } else
      t = Of(
        l,
        u,
        a,
        2
      ), t !== null && Jl(t, l, 2);
  }
  function sn(l) {
    var t = l.alternate;
    return l === X || t !== null && t === X;
  }
  function E0(l, t) {
    oa = tn = !0;
    var u = l.pending;
    u === null ? t.next = t : (t.next = u.next, u.next = t), l.pending = t;
  }
  function T0(l, t, u) {
    if ((u & 4194048) !== 0) {
      var a = t.lanes;
      a &= l.pendingLanes, u |= a, t.lanes = u, Di(l, u);
    }
  }
  var le = {
    readContext: pl,
    use: en,
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
  le.useEffectEvent = ml;
  var A0 = {
    readContext: pl,
    use: en,
    useCallback: function(l, t) {
      return Bl().memoizedState = [
        l,
        t === void 0 ? null : t
      ], l;
    },
    useContext: pl,
    useEffect: f0,
    useImperativeHandle: function(l, t, u) {
      u = u != null ? u.concat([l]) : null, fn(
        4194308,
        4,
        y0.bind(null, t, l),
        u
      );
    },
    useLayoutEffect: function(l, t) {
      return fn(4194308, 4, l, t);
    },
    useInsertionEffect: function(l, t) {
      fn(4, 2, l, t);
    },
    useMemo: function(l, t) {
      var u = Bl();
      t = t === void 0 ? null : t;
      var a = l();
      if (Xu) {
        $t(!0);
        try {
          l();
        } finally {
          $t(!1);
        }
      }
      return u.memoizedState = [a, t], a;
    },
    useReducer: function(l, t, u) {
      var a = Bl();
      if (u !== void 0) {
        var e = u(t);
        if (Xu) {
          $t(!0);
          try {
            u(t);
          } finally {
            $t(!1);
          }
        }
      } else e = t;
      return a.memoizedState = a.baseState = e, l = {
        pending: null,
        lanes: 0,
        dispatch: null,
        lastRenderedReducer: l,
        lastRenderedState: e
      }, a.queue = l, l = l.dispatch = mm.bind(
        null,
        X,
        l
      ), [a.memoizedState, l];
    },
    useRef: function(l) {
      var t = Bl();
      return l = { current: l }, t.memoizedState = l;
    },
    useState: function(l) {
      l = tc(l);
      var t = l.queue, u = z0.bind(null, X, t);
      return t.dispatch = u, [l.memoizedState, u];
    },
    useDebugValue: ec,
    useDeferredValue: function(l, t) {
      var u = Bl();
      return nc(u, l, t);
    },
    useTransition: function() {
      var l = tc(!1);
      return l = h0.bind(
        null,
        X,
        l.queue,
        !0,
        !1
      ), Bl().memoizedState = l, [!1, l];
    },
    useSyncExternalStore: function(l, t, u) {
      var a = X, e = Bl();
      if (w) {
        if (u === void 0)
          throw Error(o(407));
        u = u();
      } else {
        if (u = t(), sl === null)
          throw Error(o(349));
        (V & 127) !== 0 || Vs(a, t, u);
      }
      e.memoizedState = u;
      var n = { value: u, getSnapshot: t };
      return e.queue = n, f0(Js.bind(null, a, n, l), [
        l
      ]), a.flags |= 2048, da(
        9,
        { destroy: void 0 },
        Ks.bind(
          null,
          a,
          n,
          u,
          t
        ),
        null
      ), u;
    },
    useId: function() {
      var l = Bl(), t = sl.identifierPrefix;
      if (w) {
        var u = _t, a = At;
        u = (a & ~(1 << 32 - kl(a) - 1)).toString(32) + u, t = "_" + t + "R_" + u, u = un++, 0 < u && (t += "H" + u.toString(32)), t += "_";
      } else
        u = fm++, t = "_" + t + "r_" + u.toString(32) + "_";
      return l.memoizedState = t;
    },
    useHostTransitionStatus: cc,
    useFormState: t0,
    useActionState: t0,
    useOptimistic: function(l) {
      var t = Bl();
      t.memoizedState = t.baseState = l;
      var u = {
        pending: null,
        lanes: 0,
        dispatch: null,
        lastRenderedReducer: null,
        lastRenderedState: null
      };
      return t.queue = u, t = ic.bind(
        null,
        X,
        !0,
        u
      ), u.dispatch = t, [l, t];
    },
    useMemoCache: If,
    useCacheRefresh: function() {
      return Bl().memoizedState = om.bind(
        null,
        X
      );
    },
    useEffectEvent: function(l) {
      var t = Bl(), u = { impl: l };
      return t.memoizedState = u, function() {
        if ((I & 2) !== 0)
          throw Error(o(440));
        return u.impl.apply(void 0, arguments);
      };
    }
  }, sc = {
    readContext: pl,
    use: en,
    useCallback: o0,
    useContext: pl,
    useEffect: ac,
    useImperativeHandle: v0,
    useInsertionEffect: i0,
    useLayoutEffect: s0,
    useMemo: m0,
    useReducer: nn,
    useRef: n0,
    useState: function() {
      return nn(Gt);
    },
    useDebugValue: ec,
    useDeferredValue: function(l, t) {
      var u = bl();
      return d0(
        u,
        nl.memoizedState,
        l,
        t
      );
    },
    useTransition: function() {
      var l = nn(Gt)[0], t = bl().memoizedState;
      return [
        typeof l == "boolean" ? l : Ia(l),
        t
      ];
    },
    useSyncExternalStore: xs,
    useId: b0,
    useHostTransitionStatus: cc,
    useFormState: u0,
    useActionState: u0,
    useOptimistic: function(l, t) {
      var u = bl();
      return $s(u, nl, l, t);
    },
    useMemoCache: If,
    useCacheRefresh: r0
  };
  sc.useEffectEvent = c0;
  var _0 = {
    readContext: pl,
    use: en,
    useCallback: o0,
    useContext: pl,
    useEffect: ac,
    useImperativeHandle: v0,
    useInsertionEffect: i0,
    useLayoutEffect: s0,
    useMemo: m0,
    useReducer: lc,
    useRef: n0,
    useState: function() {
      return lc(Gt);
    },
    useDebugValue: ec,
    useDeferredValue: function(l, t) {
      var u = bl();
      return nl === null ? nc(u, l, t) : d0(
        u,
        nl.memoizedState,
        l,
        t
      );
    },
    useTransition: function() {
      var l = lc(Gt)[0], t = bl().memoizedState;
      return [
        typeof l == "boolean" ? l : Ia(l),
        t
      ];
    },
    useSyncExternalStore: xs,
    useId: b0,
    useHostTransitionStatus: cc,
    useFormState: e0,
    useActionState: e0,
    useOptimistic: function(l, t) {
      var u = bl();
      return nl !== null ? $s(u, nl, l, t) : (u.baseState = l, [l, u.queue.dispatch]);
    },
    useMemoCache: If,
    useCacheRefresh: r0
  };
  _0.useEffectEvent = c0;
  function yc(l, t, u, a) {
    t = l.memoizedState, u = u(a, t), u = u == null ? t : q({}, t, u), l.memoizedState = u, l.lanes === 0 && (l.updateQueue.baseState = u);
  }
  var vc = {
    enqueueSetState: function(l, t, u) {
      l = l._reactInternals;
      var a = et(), e = au(a);
      e.payload = t, u != null && (e.callback = u), t = eu(l, e, a), t !== null && (Jl(t, l, a), Wa(t, l, a));
    },
    enqueueReplaceState: function(l, t, u) {
      l = l._reactInternals;
      var a = et(), e = au(a);
      e.tag = 1, e.payload = t, u != null && (e.callback = u), t = eu(l, e, a), t !== null && (Jl(t, l, a), Wa(t, l, a));
    },
    enqueueForceUpdate: function(l, t) {
      l = l._reactInternals;
      var u = et(), a = au(u);
      a.tag = 2, t != null && (a.callback = t), t = eu(l, a, u), t !== null && (Jl(t, l, u), Wa(t, l, u));
    }
  };
  function O0(l, t, u, a, e, n, f) {
    return l = l.stateNode, typeof l.shouldComponentUpdate == "function" ? l.shouldComponentUpdate(a, n, f) : t.prototype && t.prototype.isPureReactComponent ? !ja(u, a) || !ja(e, n) : !0;
  }
  function M0(l, t, u, a) {
    l = t.state, typeof t.componentWillReceiveProps == "function" && t.componentWillReceiveProps(u, a), typeof t.UNSAFE_componentWillReceiveProps == "function" && t.UNSAFE_componentWillReceiveProps(u, a), t.state !== l && vc.enqueueReplaceState(t, t.state, null);
  }
  function Qu(l, t) {
    var u = t;
    if ("ref" in t) {
      u = {};
      for (var a in t)
        a !== "ref" && (u[a] = t[a]);
    }
    if (l = l.defaultProps) {
      u === t && (u = q({}, u));
      for (var e in l)
        u[e] === void 0 && (u[e] = l[e]);
    }
    return u;
  }
  function D0(l) {
    je(l);
  }
  function p0(l) {
    console.error(l);
  }
  function U0(l) {
    je(l);
  }
  function yn(l, t) {
    try {
      var u = l.onUncaughtError;
      u(t.value, { componentStack: t.stack });
    } catch (a) {
      setTimeout(function() {
        throw a;
      });
    }
  }
  function H0(l, t, u) {
    try {
      var a = l.onCaughtError;
      a(u.value, {
        componentStack: u.stack,
        errorBoundary: t.tag === 1 ? t.stateNode : null
      });
    } catch (e) {
      setTimeout(function() {
        throw e;
      });
    }
  }
  function oc(l, t, u) {
    return u = au(u), u.tag = 3, u.payload = { element: null }, u.callback = function() {
      yn(l, t);
    }, u;
  }
  function R0(l) {
    return l = au(l), l.tag = 3, l;
  }
  function N0(l, t, u, a) {
    var e = u.type.getDerivedStateFromError;
    if (typeof e == "function") {
      var n = a.value;
      l.payload = function() {
        return e(n);
      }, l.callback = function() {
        H0(t, u, a);
      };
    }
    var f = u.stateNode;
    f !== null && typeof f.componentDidCatch == "function" && (l.callback = function() {
      H0(t, u, a), typeof e != "function" && (yu === null ? yu = /* @__PURE__ */ new Set([this]) : yu.add(this));
      var c = a.stack;
      this.componentDidCatch(a.value, {
        componentStack: c !== null ? c : ""
      });
    });
  }
  function dm(l, t, u, a, e) {
    if (u.flags |= 32768, a !== null && typeof a == "object" && typeof a.then == "function") {
      if (t = u.alternate, t !== null && fa(
        t,
        u,
        e,
        !0
      ), u = lt.current, u !== null) {
        switch (u.tag) {
          case 31:
          case 13:
            return mt === null ? Tn() : u.alternate === null && dl === 0 && (dl = 3), u.flags &= -257, u.flags |= 65536, u.lanes = e, a === Fe ? u.flags |= 16384 : (t = u.updateQueue, t === null ? u.updateQueue = /* @__PURE__ */ new Set([a]) : t.add(a), Gc(l, a, e)), !1;
          case 22:
            return u.flags |= 65536, a === Fe ? u.flags |= 16384 : (t = u.updateQueue, t === null ? (t = {
              transitions: null,
              markerInstances: null,
              retryQueue: /* @__PURE__ */ new Set([a])
            }, u.updateQueue = t) : (u = t.retryQueue, u === null ? t.retryQueue = /* @__PURE__ */ new Set([a]) : u.add(a)), Gc(l, a, e)), !1;
        }
        throw Error(o(435, u.tag));
      }
      return Gc(l, a, e), Tn(), !1;
    }
    if (w)
      return t = lt.current, t !== null ? ((t.flags & 65536) === 0 && (t.flags |= 256), t.flags |= 65536, t.lanes = e, a !== Rf && (l = Error(o(422), { cause: a }), xa(st(l, u)))) : (a !== Rf && (t = Error(o(423), {
        cause: a
      }), xa(
        st(t, u)
      )), l = l.current.alternate, l.flags |= 65536, e &= -e, l.lanes |= e, a = st(a, u), e = oc(
        l.stateNode,
        a,
        e
      ), Lf(l, e), dl !== 4 && (dl = 2)), !1;
    var n = Error(o(520), { cause: a });
    if (n = st(n, u), ie === null ? ie = [n] : ie.push(n), dl !== 4 && (dl = 2), t === null) return !0;
    a = st(a, u), u = t;
    do {
      switch (u.tag) {
        case 3:
          return u.flags |= 65536, l = e & -e, u.lanes |= l, l = oc(u.stateNode, a, l), Lf(u, l), !1;
        case 1:
          if (t = u.type, n = u.stateNode, (u.flags & 128) === 0 && (typeof t.getDerivedStateFromError == "function" || n !== null && typeof n.componentDidCatch == "function" && (yu === null || !yu.has(n))))
            return u.flags |= 65536, e &= -e, u.lanes |= e, e = R0(e), N0(
              e,
              l,
              u,
              a
            ), Lf(u, e), !1;
      }
      u = u.return;
    } while (u !== null);
    return !1;
  }
  var mc = Error(o(461)), El = !1;
  function Ul(l, t, u, a) {
    t.child = l === null ? Ys(t, null, u, a) : Gu(
      t,
      l.child,
      u,
      a
    );
  }
  function C0(l, t, u, a, e) {
    u = u.render;
    var n = t.ref;
    if ("ref" in a) {
      var f = {};
      for (var c in a)
        c !== "ref" && (f[c] = a[c]);
    } else f = a;
    return Cu(t), a = Wf(
      l,
      t,
      u,
      f,
      n,
      e
    ), c = $f(), l !== null && !El ? (Ff(l, t, e), Xt(l, t, e)) : (w && c && Uf(t), t.flags |= 1, Ul(l, t, a, e), t.child);
  }
  function q0(l, t, u, a, e) {
    if (l === null) {
      var n = u.type;
      return typeof n == "function" && !Mf(n) && n.defaultProps === void 0 && u.compare === null ? (t.tag = 15, t.type = n, B0(
        l,
        t,
        n,
        a,
        e
      )) : (l = Ve(
        u.type,
        null,
        a,
        t,
        t.mode,
        e
      ), l.ref = t.ref, l.return = t, t.child = l);
    }
    if (n = l.child, !Ec(l, e)) {
      var f = n.memoizedProps;
      if (u = u.compare, u = u !== null ? u : ja, u(f, a) && l.ref === t.ref)
        return Xt(l, t, e);
    }
    return t.flags |= 1, l = Nt(n, a), l.ref = t.ref, l.return = t, t.child = l;
  }
  function B0(l, t, u, a, e) {
    if (l !== null) {
      var n = l.memoizedProps;
      if (ja(n, a) && l.ref === t.ref)
        if (El = !1, t.pendingProps = a = n, Ec(l, e))
          (l.flags & 131072) !== 0 && (El = !0);
        else
          return t.lanes = l.lanes, Xt(l, t, e);
    }
    return dc(
      l,
      t,
      u,
      a,
      e
    );
  }
  function Y0(l, t, u, a) {
    var e = a.children, n = l !== null ? l.memoizedState : null;
    if (l === null && t.stateNode === null && (t.stateNode = {
      _visibility: 1,
      _pendingMarkers: null,
      _retryCache: null,
      _transitions: null
    }), a.mode === "hidden") {
      if ((t.flags & 128) !== 0) {
        if (n = n !== null ? n.baseLanes | u : u, l !== null) {
          for (a = t.child = l.child, e = 0; a !== null; )
            e = e | a.lanes | a.childLanes, a = a.sibling;
          a = e & ~n;
        } else a = 0, t.child = null;
        return G0(
          l,
          t,
          n,
          u,
          a
        );
      }
      if ((u & 536870912) !== 0)
        t.memoizedState = { baseLanes: 0, cachePool: null }, l !== null && We(
          t,
          n !== null ? n.cachePool : null
        ), n !== null ? Qs(t, n) : Vf(), js(t);
      else
        return a = t.lanes = 536870912, G0(
          l,
          t,
          n !== null ? n.baseLanes | u : u,
          u,
          a
        );
    } else
      n !== null ? (We(t, n.cachePool), Qs(t, n), fu(), t.memoizedState = null) : (l !== null && We(t, null), Vf(), fu());
    return Ul(l, t, e, u), t.child;
  }
  function te(l, t) {
    return l !== null && l.tag === 22 || t.stateNode !== null || (t.stateNode = {
      _visibility: 1,
      _pendingMarkers: null,
      _retryCache: null,
      _transitions: null
    }), t.sibling;
  }
  function G0(l, t, u, a, e) {
    var n = Xf();
    return n = n === null ? null : { parent: rl._currentValue, pool: n }, t.memoizedState = {
      baseLanes: u,
      cachePool: n
    }, l !== null && We(t, null), Vf(), js(t), l !== null && fa(l, t, a, !0), t.childLanes = e, null;
  }
  function vn(l, t) {
    return t = mn(
      { mode: t.mode, children: t.children },
      l.mode
    ), t.ref = l.ref, l.child = t, t.return = l, t;
  }
  function X0(l, t, u) {
    return Gu(t, l.child, null, u), l = vn(t, t.pendingProps), l.flags |= 2, tt(t), t.memoizedState = null, l;
  }
  function hm(l, t, u) {
    var a = t.pendingProps, e = (t.flags & 128) !== 0;
    if (t.flags &= -129, l === null) {
      if (w) {
        if (a.mode === "hidden")
          return l = vn(t, a), t.lanes = 536870912, te(null, l);
        if (Jf(t), (l = yl) ? (l = Fy(
          l,
          ot
        ), l = l !== null && l.data === "&" ? l : null, l !== null && (t.memoizedState = {
          dehydrated: l,
          treeContext: It !== null ? { id: At, overflow: _t } : null,
          retryLane: 536870912,
          hydrationErrors: null
        }, u = Es(l), u.return = t, t.child = u, Dl = t, yl = null)) : l = null, l === null) throw lu(t);
        return t.lanes = 536870912, null;
      }
      return vn(t, a);
    }
    var n = l.memoizedState;
    if (n !== null) {
      var f = n.dehydrated;
      if (Jf(t), e)
        if (t.flags & 256)
          t.flags &= -257, t = X0(
            l,
            t,
            u
          );
        else if (t.memoizedState !== null)
          t.child = l.child, t.flags |= 128, t = null;
        else throw Error(o(558));
      else if (El || fa(l, t, u, !1), e = (u & l.childLanes) !== 0, El || e) {
        if (a = sl, a !== null && (f = pi(a, u), f !== 0 && f !== n.retryLane))
          throw n.retryLane = f, Uu(l, f), Jl(a, l, f), mc;
        Tn(), t = X0(
          l,
          t,
          u
        );
      } else
        l = n.treeContext, yl = dt(f.nextSibling), Dl = t, w = !0, Pt = null, ot = !1, l !== null && _s(t, l), t = vn(t, a), t.flags |= 4096;
      return t;
    }
    return l = Nt(l.child, {
      mode: a.mode,
      children: a.children
    }), l.ref = t.ref, t.child = l, l.return = t, l;
  }
  function on(l, t) {
    var u = t.ref;
    if (u === null)
      l !== null && l.ref !== null && (t.flags |= 4194816);
    else {
      if (typeof u != "function" && typeof u != "object")
        throw Error(o(284));
      (l === null || l.ref !== u) && (t.flags |= 4194816);
    }
  }
  function dc(l, t, u, a, e) {
    return Cu(t), u = Wf(
      l,
      t,
      u,
      a,
      void 0,
      e
    ), a = $f(), l !== null && !El ? (Ff(l, t, e), Xt(l, t, e)) : (w && a && Uf(t), t.flags |= 1, Ul(l, t, u, e), t.child);
  }
  function Q0(l, t, u, a, e, n) {
    return Cu(t), t.updateQueue = null, u = Ls(
      t,
      a,
      u,
      e
    ), Zs(l), a = $f(), l !== null && !El ? (Ff(l, t, n), Xt(l, t, n)) : (w && a && Uf(t), t.flags |= 1, Ul(l, t, u, n), t.child);
  }
  function j0(l, t, u, a, e) {
    if (Cu(t), t.stateNode === null) {
      var n = ua, f = u.contextType;
      typeof f == "object" && f !== null && (n = pl(f)), n = new u(a, n), t.memoizedState = n.state !== null && n.state !== void 0 ? n.state : null, n.updater = vc, t.stateNode = n, n._reactInternals = t, n = t.stateNode, n.props = a, n.state = t.memoizedState, n.refs = {}, jf(t), f = u.contextType, n.context = typeof f == "object" && f !== null ? pl(f) : ua, n.state = t.memoizedState, f = u.getDerivedStateFromProps, typeof f == "function" && (yc(
        t,
        u,
        f,
        a
      ), n.state = t.memoizedState), typeof u.getDerivedStateFromProps == "function" || typeof n.getSnapshotBeforeUpdate == "function" || typeof n.UNSAFE_componentWillMount != "function" && typeof n.componentWillMount != "function" || (f = n.state, typeof n.componentWillMount == "function" && n.componentWillMount(), typeof n.UNSAFE_componentWillMount == "function" && n.UNSAFE_componentWillMount(), f !== n.state && vc.enqueueReplaceState(n, n.state, null), Fa(t, a, n, e), $a(), n.state = t.memoizedState), typeof n.componentDidMount == "function" && (t.flags |= 4194308), a = !0;
    } else if (l === null) {
      n = t.stateNode;
      var c = t.memoizedProps, i = Qu(u, c);
      n.props = i;
      var d = n.context, b = u.contextType;
      f = ua, typeof b == "object" && b !== null && (f = pl(b));
      var E = u.getDerivedStateFromProps;
      b = typeof E == "function" || typeof n.getSnapshotBeforeUpdate == "function", c = t.pendingProps !== c, b || typeof n.UNSAFE_componentWillReceiveProps != "function" && typeof n.componentWillReceiveProps != "function" || (c || d !== f) && M0(
        t,
        n,
        a,
        f
      ), uu = !1;
      var S = t.memoizedState;
      n.state = S, Fa(t, a, n, e), $a(), d = t.memoizedState, c || S !== d || uu ? (typeof E == "function" && (yc(
        t,
        u,
        E,
        a
      ), d = t.memoizedState), (i = uu || O0(
        t,
        u,
        i,
        a,
        S,
        d,
        f
      )) ? (b || typeof n.UNSAFE_componentWillMount != "function" && typeof n.componentWillMount != "function" || (typeof n.componentWillMount == "function" && n.componentWillMount(), typeof n.UNSAFE_componentWillMount == "function" && n.UNSAFE_componentWillMount()), typeof n.componentDidMount == "function" && (t.flags |= 4194308)) : (typeof n.componentDidMount == "function" && (t.flags |= 4194308), t.memoizedProps = a, t.memoizedState = d), n.props = a, n.state = d, n.context = f, a = i) : (typeof n.componentDidMount == "function" && (t.flags |= 4194308), a = !1);
    } else {
      n = t.stateNode, Zf(l, t), f = t.memoizedProps, b = Qu(u, f), n.props = b, E = t.pendingProps, S = n.context, d = u.contextType, i = ua, typeof d == "object" && d !== null && (i = pl(d)), c = u.getDerivedStateFromProps, (d = typeof c == "function" || typeof n.getSnapshotBeforeUpdate == "function") || typeof n.UNSAFE_componentWillReceiveProps != "function" && typeof n.componentWillReceiveProps != "function" || (f !== E || S !== i) && M0(
        t,
        n,
        a,
        i
      ), uu = !1, S = t.memoizedState, n.state = S, Fa(t, a, n, e), $a();
      var g = t.memoizedState;
      f !== E || S !== g || uu || l !== null && l.dependencies !== null && Je(l.dependencies) ? (typeof c == "function" && (yc(
        t,
        u,
        c,
        a
      ), g = t.memoizedState), (b = uu || O0(
        t,
        u,
        b,
        a,
        S,
        g,
        i
      ) || l !== null && l.dependencies !== null && Je(l.dependencies)) ? (d || typeof n.UNSAFE_componentWillUpdate != "function" && typeof n.componentWillUpdate != "function" || (typeof n.componentWillUpdate == "function" && n.componentWillUpdate(a, g, i), typeof n.UNSAFE_componentWillUpdate == "function" && n.UNSAFE_componentWillUpdate(
        a,
        g,
        i
      )), typeof n.componentDidUpdate == "function" && (t.flags |= 4), typeof n.getSnapshotBeforeUpdate == "function" && (t.flags |= 1024)) : (typeof n.componentDidUpdate != "function" || f === l.memoizedProps && S === l.memoizedState || (t.flags |= 4), typeof n.getSnapshotBeforeUpdate != "function" || f === l.memoizedProps && S === l.memoizedState || (t.flags |= 1024), t.memoizedProps = a, t.memoizedState = g), n.props = a, n.state = g, n.context = i, a = b) : (typeof n.componentDidUpdate != "function" || f === l.memoizedProps && S === l.memoizedState || (t.flags |= 4), typeof n.getSnapshotBeforeUpdate != "function" || f === l.memoizedProps && S === l.memoizedState || (t.flags |= 1024), a = !1);
    }
    return n = a, on(l, t), a = (t.flags & 128) !== 0, n || a ? (n = t.stateNode, u = a && typeof u.getDerivedStateFromError != "function" ? null : n.render(), t.flags |= 1, l !== null && a ? (t.child = Gu(
      t,
      l.child,
      null,
      e
    ), t.child = Gu(
      t,
      null,
      u,
      e
    )) : Ul(l, t, u, e), t.memoizedState = n.state, l = t.child) : l = Xt(
      l,
      t,
      e
    ), l;
  }
  function Z0(l, t, u, a) {
    return Ru(), t.flags |= 256, Ul(l, t, u, a), t.child;
  }
  var hc = {
    dehydrated: null,
    treeContext: null,
    retryLane: 0,
    hydrationErrors: null
  };
  function Sc(l) {
    return { baseLanes: l, cachePool: Hs() };
  }
  function gc(l, t, u) {
    return l = l !== null ? l.childLanes & ~u : 0, t && (l |= at), l;
  }
  function L0(l, t, u) {
    var a = t.pendingProps, e = !1, n = (t.flags & 128) !== 0, f;
    if ((f = n) || (f = l !== null && l.memoizedState === null ? !1 : (gl.current & 2) !== 0), f && (e = !0, t.flags &= -129), f = (t.flags & 32) !== 0, t.flags &= -33, l === null) {
      if (w) {
        if (e ? nu(t) : fu(), (l = yl) ? (l = Fy(
          l,
          ot
        ), l = l !== null && l.data !== "&" ? l : null, l !== null && (t.memoizedState = {
          dehydrated: l,
          treeContext: It !== null ? { id: At, overflow: _t } : null,
          retryLane: 536870912,
          hydrationErrors: null
        }, u = Es(l), u.return = t, t.child = u, Dl = t, yl = null)) : l = null, l === null) throw lu(t);
        return Pc(l) ? t.lanes = 32 : t.lanes = 536870912, null;
      }
      var c = a.children;
      return a = a.fallback, e ? (fu(), e = t.mode, c = mn(
        { mode: "hidden", children: c },
        e
      ), a = Hu(
        a,
        e,
        u,
        null
      ), c.return = t, a.return = t, c.sibling = a, t.child = c, a = t.child, a.memoizedState = Sc(u), a.childLanes = gc(
        l,
        f,
        u
      ), t.memoizedState = hc, te(null, a)) : (nu(t), bc(t, c));
    }
    var i = l.memoizedState;
    if (i !== null && (c = i.dehydrated, c !== null)) {
      if (n)
        t.flags & 256 ? (nu(t), t.flags &= -257, t = rc(
          l,
          t,
          u
        )) : t.memoizedState !== null ? (fu(), t.child = l.child, t.flags |= 128, t = null) : (fu(), c = a.fallback, e = t.mode, a = mn(
          { mode: "visible", children: a.children },
          e
        ), c = Hu(
          c,
          e,
          u,
          null
        ), c.flags |= 2, a.return = t, c.return = t, a.sibling = c, t.child = a, Gu(
          t,
          l.child,
          null,
          u
        ), a = t.child, a.memoizedState = Sc(u), a.childLanes = gc(
          l,
          f,
          u
        ), t.memoizedState = hc, t = te(null, a));
      else if (nu(t), Pc(c)) {
        if (f = c.nextSibling && c.nextSibling.dataset, f) var d = f.dgst;
        f = d, a = Error(o(419)), a.stack = "", a.digest = f, xa({ value: a, source: null, stack: null }), t = rc(
          l,
          t,
          u
        );
      } else if (El || fa(l, t, u, !1), f = (u & l.childLanes) !== 0, El || f) {
        if (f = sl, f !== null && (a = pi(f, u), a !== 0 && a !== i.retryLane))
          throw i.retryLane = a, Uu(l, a), Jl(f, l, a), mc;
        Ic(c) || Tn(), t = rc(
          l,
          t,
          u
        );
      } else
        Ic(c) ? (t.flags |= 192, t.child = l.child, t = null) : (l = i.treeContext, yl = dt(
          c.nextSibling
        ), Dl = t, w = !0, Pt = null, ot = !1, l !== null && _s(t, l), t = bc(
          t,
          a.children
        ), t.flags |= 4096);
      return t;
    }
    return e ? (fu(), c = a.fallback, e = t.mode, i = l.child, d = i.sibling, a = Nt(i, {
      mode: "hidden",
      children: a.children
    }), a.subtreeFlags = i.subtreeFlags & 65011712, d !== null ? c = Nt(
      d,
      c
    ) : (c = Hu(
      c,
      e,
      u,
      null
    ), c.flags |= 2), c.return = t, a.return = t, a.sibling = c, t.child = a, te(null, a), a = t.child, c = l.child.memoizedState, c === null ? c = Sc(u) : (e = c.cachePool, e !== null ? (i = rl._currentValue, e = e.parent !== i ? { parent: i, pool: i } : e) : e = Hs(), c = {
      baseLanes: c.baseLanes | u,
      cachePool: e
    }), a.memoizedState = c, a.childLanes = gc(
      l,
      f,
      u
    ), t.memoizedState = hc, te(l.child, a)) : (nu(t), u = l.child, l = u.sibling, u = Nt(u, {
      mode: "visible",
      children: a.children
    }), u.return = t, u.sibling = null, l !== null && (f = t.deletions, f === null ? (t.deletions = [l], t.flags |= 16) : f.push(l)), t.child = u, t.memoizedState = null, u);
  }
  function bc(l, t) {
    return t = mn(
      { mode: "visible", children: t },
      l.mode
    ), t.return = l, l.child = t;
  }
  function mn(l, t) {
    return l = Pl(22, l, null, t), l.lanes = 0, l;
  }
  function rc(l, t, u) {
    return Gu(t, l.child, null, u), l = bc(
      t,
      t.pendingProps.children
    ), l.flags |= 2, t.memoizedState = null, l;
  }
  function x0(l, t, u) {
    l.lanes |= t;
    var a = l.alternate;
    a !== null && (a.lanes |= t), qf(l.return, t, u);
  }
  function zc(l, t, u, a, e, n) {
    var f = l.memoizedState;
    f === null ? l.memoizedState = {
      isBackwards: t,
      rendering: null,
      renderingStartTime: 0,
      last: a,
      tail: u,
      tailMode: e,
      treeForkCount: n
    } : (f.isBackwards = t, f.rendering = null, f.renderingStartTime = 0, f.last = a, f.tail = u, f.tailMode = e, f.treeForkCount = n);
  }
  function V0(l, t, u) {
    var a = t.pendingProps, e = a.revealOrder, n = a.tail;
    a = a.children;
    var f = gl.current, c = (f & 2) !== 0;
    if (c ? (f = f & 1 | 2, t.flags |= 128) : f &= 1, D(gl, f), Ul(l, t, a, u), a = w ? La : 0, !c && l !== null && (l.flags & 128) !== 0)
      l: for (l = t.child; l !== null; ) {
        if (l.tag === 13)
          l.memoizedState !== null && x0(l, u, t);
        else if (l.tag === 19)
          x0(l, u, t);
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
        for (u = t.child, e = null; u !== null; )
          l = u.alternate, l !== null && ln(l) === null && (e = u), u = u.sibling;
        u = e, u === null ? (e = t.child, t.child = null) : (e = u.sibling, u.sibling = null), zc(
          t,
          !1,
          e,
          u,
          n,
          a
        );
        break;
      case "backwards":
      case "unstable_legacy-backwards":
        for (u = null, e = t.child, t.child = null; e !== null; ) {
          if (l = e.alternate, l !== null && ln(l) === null) {
            t.child = e;
            break;
          }
          l = e.sibling, e.sibling = u, u = e, e = l;
        }
        zc(
          t,
          !0,
          u,
          null,
          n,
          a
        );
        break;
      case "together":
        zc(
          t,
          !1,
          null,
          null,
          void 0,
          a
        );
        break;
      default:
        t.memoizedState = null;
    }
    return t.child;
  }
  function Xt(l, t, u) {
    if (l !== null && (t.dependencies = l.dependencies), su |= t.lanes, (u & t.childLanes) === 0)
      if (l !== null) {
        if (fa(
          l,
          t,
          u,
          !1
        ), (u & t.childLanes) === 0)
          return null;
      } else return null;
    if (l !== null && t.child !== l.child)
      throw Error(o(153));
    if (t.child !== null) {
      for (l = t.child, u = Nt(l, l.pendingProps), t.child = u, u.return = t; l.sibling !== null; )
        l = l.sibling, u = u.sibling = Nt(l, l.pendingProps), u.return = t;
      u.sibling = null;
    }
    return t.child;
  }
  function Ec(l, t) {
    return (l.lanes & t) !== 0 ? !0 : (l = l.dependencies, !!(l !== null && Je(l)));
  }
  function Sm(l, t, u) {
    switch (t.tag) {
      case 3:
        ql(t, t.stateNode.containerInfo), tu(t, rl, l.memoizedState.cache), Ru();
        break;
      case 27:
      case 5:
        Da(t);
        break;
      case 4:
        ql(t, t.stateNode.containerInfo);
        break;
      case 10:
        tu(
          t,
          t.type,
          t.memoizedProps.value
        );
        break;
      case 31:
        if (t.memoizedState !== null)
          return t.flags |= 128, Jf(t), null;
        break;
      case 13:
        var a = t.memoizedState;
        if (a !== null)
          return a.dehydrated !== null ? (nu(t), t.flags |= 128, null) : (u & t.child.childLanes) !== 0 ? L0(l, t, u) : (nu(t), l = Xt(
            l,
            t,
            u
          ), l !== null ? l.sibling : null);
        nu(t);
        break;
      case 19:
        var e = (l.flags & 128) !== 0;
        if (a = (u & t.childLanes) !== 0, a || (fa(
          l,
          t,
          u,
          !1
        ), a = (u & t.childLanes) !== 0), e) {
          if (a)
            return V0(
              l,
              t,
              u
            );
          t.flags |= 128;
        }
        if (e = t.memoizedState, e !== null && (e.rendering = null, e.tail = null, e.lastEffect = null), D(gl, gl.current), a) break;
        return null;
      case 22:
        return t.lanes = 0, Y0(
          l,
          t,
          u,
          t.pendingProps
        );
      case 24:
        tu(t, rl, l.memoizedState.cache);
    }
    return Xt(l, t, u);
  }
  function K0(l, t, u) {
    if (l !== null)
      if (l.memoizedProps !== t.pendingProps)
        El = !0;
      else {
        if (!Ec(l, u) && (t.flags & 128) === 0)
          return El = !1, Sm(
            l,
            t,
            u
          );
        El = (l.flags & 131072) !== 0;
      }
    else
      El = !1, w && (t.flags & 1048576) !== 0 && As(t, La, t.index);
    switch (t.lanes = 0, t.tag) {
      case 16:
        l: {
          var a = t.pendingProps;
          if (l = Bu(t.elementType), t.type = l, typeof l == "function")
            Mf(l) ? (a = Qu(l, a), t.tag = 1, t = j0(
              null,
              t,
              l,
              a,
              u
            )) : (t.tag = 0, t = dc(
              null,
              t,
              l,
              a,
              u
            ));
          else {
            if (l != null) {
              var e = l.$$typeof;
              if (e === nt) {
                t.tag = 11, t = C0(
                  null,
                  t,
                  l,
                  a,
                  u
                );
                break l;
              } else if (e === J) {
                t.tag = 14, t = q0(
                  null,
                  t,
                  l,
                  a,
                  u
                );
                break l;
              }
            }
            throw t = pt(l) || l, Error(o(306, t, ""));
          }
        }
        return t;
      case 0:
        return dc(
          l,
          t,
          t.type,
          t.pendingProps,
          u
        );
      case 1:
        return a = t.type, e = Qu(
          a,
          t.pendingProps
        ), j0(
          l,
          t,
          a,
          e,
          u
        );
      case 3:
        l: {
          if (ql(
            t,
            t.stateNode.containerInfo
          ), l === null) throw Error(o(387));
          a = t.pendingProps;
          var n = t.memoizedState;
          e = n.element, Zf(l, t), Fa(t, a, null, u);
          var f = t.memoizedState;
          if (a = f.cache, tu(t, rl, a), a !== n.cache && Bf(
            t,
            [rl],
            u,
            !0
          ), $a(), a = f.element, n.isDehydrated)
            if (n = {
              element: a,
              isDehydrated: !1,
              cache: f.cache
            }, t.updateQueue.baseState = n, t.memoizedState = n, t.flags & 256) {
              t = Z0(
                l,
                t,
                a,
                u
              );
              break l;
            } else if (a !== e) {
              e = st(
                Error(o(424)),
                t
              ), xa(e), t = Z0(
                l,
                t,
                a,
                u
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
              for (yl = dt(l.firstChild), Dl = t, w = !0, Pt = null, ot = !0, u = Ys(
                t,
                null,
                a,
                u
              ), t.child = u; u; )
                u.flags = u.flags & -3 | 4096, u = u.sibling;
            }
          else {
            if (Ru(), a === e) {
              t = Xt(
                l,
                t,
                u
              );
              break l;
            }
            Ul(l, t, a, u);
          }
          t = t.child;
        }
        return t;
      case 26:
        return on(l, t), l === null ? (u = uv(
          t.type,
          null,
          t.pendingProps,
          null
        )) ? t.memoizedState = u : w || (u = t.type, l = t.pendingProps, a = Un(
          Z.current
        ).createElement(u), a[Ml] = t, a[jl] = l, Hl(a, u, l), _l(a), t.stateNode = a) : t.memoizedState = uv(
          t.type,
          l.memoizedProps,
          t.pendingProps,
          l.memoizedState
        ), null;
      case 27:
        return Da(t), l === null && w && (a = t.stateNode = Py(
          t.type,
          t.pendingProps,
          Z.current
        ), Dl = t, ot = !0, e = yl, du(t.type) ? (li = e, yl = dt(a.firstChild)) : yl = e), Ul(
          l,
          t,
          t.pendingProps.children,
          u
        ), on(l, t), l === null && (t.flags |= 4194304), t.child;
      case 5:
        return l === null && w && ((e = a = yl) && (a = Jm(
          a,
          t.type,
          t.pendingProps,
          ot
        ), a !== null ? (t.stateNode = a, Dl = t, yl = dt(a.firstChild), ot = !1, e = !0) : e = !1), e || lu(t)), Da(t), e = t.type, n = t.pendingProps, f = l !== null ? l.memoizedProps : null, a = n.children, $c(e, n) ? a = null : f !== null && $c(e, f) && (t.flags |= 32), t.memoizedState !== null && (e = Wf(
          l,
          t,
          cm,
          null,
          null,
          u
        ), Se._currentValue = e), on(l, t), Ul(l, t, a, u), t.child;
      case 6:
        return l === null && w && ((l = u = yl) && (u = wm(
          u,
          t.pendingProps,
          ot
        ), u !== null ? (t.stateNode = u, Dl = t, yl = null, l = !0) : l = !1), l || lu(t)), null;
      case 13:
        return L0(l, t, u);
      case 4:
        return ql(
          t,
          t.stateNode.containerInfo
        ), a = t.pendingProps, l === null ? t.child = Gu(
          t,
          null,
          a,
          u
        ) : Ul(l, t, a, u), t.child;
      case 11:
        return C0(
          l,
          t,
          t.type,
          t.pendingProps,
          u
        );
      case 7:
        return Ul(
          l,
          t,
          t.pendingProps,
          u
        ), t.child;
      case 8:
        return Ul(
          l,
          t,
          t.pendingProps.children,
          u
        ), t.child;
      case 12:
        return Ul(
          l,
          t,
          t.pendingProps.children,
          u
        ), t.child;
      case 10:
        return a = t.pendingProps, tu(t, t.type, a.value), Ul(l, t, a.children, u), t.child;
      case 9:
        return e = t.type._context, a = t.pendingProps.children, Cu(t), e = pl(e), a = a(e), t.flags |= 1, Ul(l, t, a, u), t.child;
      case 14:
        return q0(
          l,
          t,
          t.type,
          t.pendingProps,
          u
        );
      case 15:
        return B0(
          l,
          t,
          t.type,
          t.pendingProps,
          u
        );
      case 19:
        return V0(l, t, u);
      case 31:
        return hm(l, t, u);
      case 22:
        return Y0(
          l,
          t,
          u,
          t.pendingProps
        );
      case 24:
        return Cu(t), a = pl(rl), l === null ? (e = Xf(), e === null && (e = sl, n = Yf(), e.pooledCache = n, n.refCount++, n !== null && (e.pooledCacheLanes |= u), e = n), t.memoizedState = { parent: a, cache: e }, jf(t), tu(t, rl, e)) : ((l.lanes & u) !== 0 && (Zf(l, t), Fa(t, null, null, u), $a()), e = l.memoizedState, n = t.memoizedState, e.parent !== a ? (e = { parent: a, cache: a }, t.memoizedState = e, t.lanes === 0 && (t.memoizedState = t.updateQueue.baseState = e), tu(t, rl, a)) : (a = n.cache, tu(t, rl, a), a !== e.cache && Bf(
          t,
          [rl],
          u,
          !0
        ))), Ul(
          l,
          t,
          t.pendingProps.children,
          u
        ), t.child;
      case 29:
        throw t.pendingProps;
    }
    throw Error(o(156, t.tag));
  }
  function Qt(l) {
    l.flags |= 4;
  }
  function Tc(l, t, u, a, e) {
    if ((t = (l.mode & 32) !== 0) && (t = !1), t) {
      if (l.flags |= 16777216, (e & 335544128) === e)
        if (l.stateNode.complete) l.flags |= 8192;
        else if (by()) l.flags |= 8192;
        else
          throw Yu = Fe, Qf;
    } else l.flags &= -16777217;
  }
  function J0(l, t) {
    if (t.type !== "stylesheet" || (t.state.loading & 4) !== 0)
      l.flags &= -16777217;
    else if (l.flags |= 16777216, !cv(t))
      if (by()) l.flags |= 8192;
      else
        throw Yu = Fe, Qf;
  }
  function dn(l, t) {
    t !== null && (l.flags |= 4), l.flags & 16384 && (t = l.tag !== 22 ? Oi() : 536870912, l.lanes |= t, ba |= t);
  }
  function ue(l, t) {
    if (!w)
      switch (l.tailMode) {
        case "hidden":
          t = l.tail;
          for (var u = null; t !== null; )
            t.alternate !== null && (u = t), t = t.sibling;
          u === null ? l.tail = null : u.sibling = null;
          break;
        case "collapsed":
          u = l.tail;
          for (var a = null; u !== null; )
            u.alternate !== null && (a = u), u = u.sibling;
          a === null ? t || l.tail === null ? l.tail = null : l.tail.sibling = null : a.sibling = null;
      }
  }
  function vl(l) {
    var t = l.alternate !== null && l.alternate.child === l.child, u = 0, a = 0;
    if (t)
      for (var e = l.child; e !== null; )
        u |= e.lanes | e.childLanes, a |= e.subtreeFlags & 65011712, a |= e.flags & 65011712, e.return = l, e = e.sibling;
    else
      for (e = l.child; e !== null; )
        u |= e.lanes | e.childLanes, a |= e.subtreeFlags, a |= e.flags, e.return = l, e = e.sibling;
    return l.subtreeFlags |= a, l.childLanes = u, t;
  }
  function gm(l, t, u) {
    var a = t.pendingProps;
    switch (Hf(t), t.tag) {
      case 16:
      case 15:
      case 0:
      case 11:
      case 7:
      case 8:
      case 12:
      case 9:
      case 14:
        return vl(t), null;
      case 1:
        return vl(t), null;
      case 3:
        return u = t.stateNode, a = null, l !== null && (a = l.memoizedState.cache), t.memoizedState.cache !== a && (t.flags |= 2048), Bt(rl), Sl(), u.pendingContext && (u.context = u.pendingContext, u.pendingContext = null), (l === null || l.child === null) && (na(t) ? Qt(t) : l === null || l.memoizedState.isDehydrated && (t.flags & 256) === 0 || (t.flags |= 1024, Nf())), vl(t), null;
      case 26:
        var e = t.type, n = t.memoizedState;
        return l === null ? (Qt(t), n !== null ? (vl(t), J0(t, n)) : (vl(t), Tc(
          t,
          e,
          null,
          a,
          u
        ))) : n ? n !== l.memoizedState ? (Qt(t), vl(t), J0(t, n)) : (vl(t), t.flags &= -16777217) : (l = l.memoizedProps, l !== a && Qt(t), vl(t), Tc(
          t,
          e,
          l,
          a,
          u
        )), null;
      case 27:
        if (_e(t), u = Z.current, e = t.type, l !== null && t.stateNode != null)
          l.memoizedProps !== a && Qt(t);
        else {
          if (!a) {
            if (t.stateNode === null)
              throw Error(o(166));
            return vl(t), null;
          }
          l = U.current, na(t) ? Os(t) : (l = Py(e, a, u), t.stateNode = l, Qt(t));
        }
        return vl(t), null;
      case 5:
        if (_e(t), e = t.type, l !== null && t.stateNode != null)
          l.memoizedProps !== a && Qt(t);
        else {
          if (!a) {
            if (t.stateNode === null)
              throw Error(o(166));
            return vl(t), null;
          }
          if (n = U.current, na(t))
            Os(t);
          else {
            var f = Un(
              Z.current
            );
            switch (n) {
              case 1:
                n = f.createElementNS(
                  "http://www.w3.org/2000/svg",
                  e
                );
                break;
              case 2:
                n = f.createElementNS(
                  "http://www.w3.org/1998/Math/MathML",
                  e
                );
                break;
              default:
                switch (e) {
                  case "svg":
                    n = f.createElementNS(
                      "http://www.w3.org/2000/svg",
                      e
                    );
                    break;
                  case "math":
                    n = f.createElementNS(
                      "http://www.w3.org/1998/Math/MathML",
                      e
                    );
                    break;
                  case "script":
                    n = f.createElement("div"), n.innerHTML = "<script><\/script>", n = n.removeChild(
                      n.firstChild
                    );
                    break;
                  case "select":
                    n = typeof a.is == "string" ? f.createElement("select", {
                      is: a.is
                    }) : f.createElement("select"), a.multiple ? n.multiple = !0 : a.size && (n.size = a.size);
                    break;
                  default:
                    n = typeof a.is == "string" ? f.createElement(e, { is: a.is }) : f.createElement(e);
                }
            }
            n[Ml] = t, n[jl] = a;
            l: for (f = t.child; f !== null; ) {
              if (f.tag === 5 || f.tag === 6)
                n.appendChild(f.stateNode);
              else if (f.tag !== 4 && f.tag !== 27 && f.child !== null) {
                f.child.return = f, f = f.child;
                continue;
              }
              if (f === t) break l;
              for (; f.sibling === null; ) {
                if (f.return === null || f.return === t)
                  break l;
                f = f.return;
              }
              f.sibling.return = f.return, f = f.sibling;
            }
            t.stateNode = n;
            l: switch (Hl(n, e, a), e) {
              case "button":
              case "input":
              case "select":
              case "textarea":
                a = !!a.autoFocus;
                break l;
              case "img":
                a = !0;
                break l;
              default:
                a = !1;
            }
            a && Qt(t);
          }
        }
        return vl(t), Tc(
          t,
          t.type,
          l === null ? null : l.memoizedProps,
          t.pendingProps,
          u
        ), null;
      case 6:
        if (l && t.stateNode != null)
          l.memoizedProps !== a && Qt(t);
        else {
          if (typeof a != "string" && t.stateNode === null)
            throw Error(o(166));
          if (l = Z.current, na(t)) {
            if (l = t.stateNode, u = t.memoizedProps, a = null, e = Dl, e !== null)
              switch (e.tag) {
                case 27:
                case 5:
                  a = e.memoizedProps;
              }
            l[Ml] = t, l = !!(l.nodeValue === u || a !== null && a.suppressHydrationWarning === !0 || Ly(l.nodeValue, u)), l || lu(t, !0);
          } else
            l = Un(l).createTextNode(
              a
            ), l[Ml] = t, t.stateNode = l;
        }
        return vl(t), null;
      case 31:
        if (u = t.memoizedState, l === null || l.memoizedState !== null) {
          if (a = na(t), u !== null) {
            if (l === null) {
              if (!a) throw Error(o(318));
              if (l = t.memoizedState, l = l !== null ? l.dehydrated : null, !l) throw Error(o(557));
              l[Ml] = t;
            } else
              Ru(), (t.flags & 128) === 0 && (t.memoizedState = null), t.flags |= 4;
            vl(t), l = !1;
          } else
            u = Nf(), l !== null && l.memoizedState !== null && (l.memoizedState.hydrationErrors = u), l = !0;
          if (!l)
            return t.flags & 256 ? (tt(t), t) : (tt(t), null);
          if ((t.flags & 128) !== 0)
            throw Error(o(558));
        }
        return vl(t), null;
      case 13:
        if (a = t.memoizedState, l === null || l.memoizedState !== null && l.memoizedState.dehydrated !== null) {
          if (e = na(t), a !== null && a.dehydrated !== null) {
            if (l === null) {
              if (!e) throw Error(o(318));
              if (e = t.memoizedState, e = e !== null ? e.dehydrated : null, !e) throw Error(o(317));
              e[Ml] = t;
            } else
              Ru(), (t.flags & 128) === 0 && (t.memoizedState = null), t.flags |= 4;
            vl(t), e = !1;
          } else
            e = Nf(), l !== null && l.memoizedState !== null && (l.memoizedState.hydrationErrors = e), e = !0;
          if (!e)
            return t.flags & 256 ? (tt(t), t) : (tt(t), null);
        }
        return tt(t), (t.flags & 128) !== 0 ? (t.lanes = u, t) : (u = a !== null, l = l !== null && l.memoizedState !== null, u && (a = t.child, e = null, a.alternate !== null && a.alternate.memoizedState !== null && a.alternate.memoizedState.cachePool !== null && (e = a.alternate.memoizedState.cachePool.pool), n = null, a.memoizedState !== null && a.memoizedState.cachePool !== null && (n = a.memoizedState.cachePool.pool), n !== e && (a.flags |= 2048)), u !== l && u && (t.child.flags |= 8192), dn(t, t.updateQueue), vl(t), null);
      case 4:
        return Sl(), l === null && Vc(t.stateNode.containerInfo), vl(t), null;
      case 10:
        return Bt(t.type), vl(t), null;
      case 19:
        if (T(gl), a = t.memoizedState, a === null) return vl(t), null;
        if (e = (t.flags & 128) !== 0, n = a.rendering, n === null)
          if (e) ue(a, !1);
          else {
            if (dl !== 0 || l !== null && (l.flags & 128) !== 0)
              for (l = t.child; l !== null; ) {
                if (n = ln(l), n !== null) {
                  for (t.flags |= 128, ue(a, !1), l = n.updateQueue, t.updateQueue = l, dn(t, l), t.subtreeFlags = 0, l = u, u = t.child; u !== null; )
                    zs(u, l), u = u.sibling;
                  return D(
                    gl,
                    gl.current & 1 | 2
                  ), w && Ct(t, a.treeForkCount), t.child;
                }
                l = l.sibling;
              }
            a.tail !== null && $l() > rn && (t.flags |= 128, e = !0, ue(a, !1), t.lanes = 4194304);
          }
        else {
          if (!e)
            if (l = ln(n), l !== null) {
              if (t.flags |= 128, e = !0, l = l.updateQueue, t.updateQueue = l, dn(t, l), ue(a, !0), a.tail === null && a.tailMode === "hidden" && !n.alternate && !w)
                return vl(t), null;
            } else
              2 * $l() - a.renderingStartTime > rn && u !== 536870912 && (t.flags |= 128, e = !0, ue(a, !1), t.lanes = 4194304);
          a.isBackwards ? (n.sibling = t.child, t.child = n) : (l = a.last, l !== null ? l.sibling = n : t.child = n, a.last = n);
        }
        return a.tail !== null ? (l = a.tail, a.rendering = l, a.tail = l.sibling, a.renderingStartTime = $l(), l.sibling = null, u = gl.current, D(
          gl,
          e ? u & 1 | 2 : u & 1
        ), w && Ct(t, a.treeForkCount), l) : (vl(t), null);
      case 22:
      case 23:
        return tt(t), Kf(), a = t.memoizedState !== null, l !== null ? l.memoizedState !== null !== a && (t.flags |= 8192) : a && (t.flags |= 8192), a ? (u & 536870912) !== 0 && (t.flags & 128) === 0 && (vl(t), t.subtreeFlags & 6 && (t.flags |= 8192)) : vl(t), u = t.updateQueue, u !== null && dn(t, u.retryQueue), u = null, l !== null && l.memoizedState !== null && l.memoizedState.cachePool !== null && (u = l.memoizedState.cachePool.pool), a = null, t.memoizedState !== null && t.memoizedState.cachePool !== null && (a = t.memoizedState.cachePool.pool), a !== u && (t.flags |= 2048), l !== null && T(qu), null;
      case 24:
        return u = null, l !== null && (u = l.memoizedState.cache), t.memoizedState.cache !== u && (t.flags |= 2048), Bt(rl), vl(t), null;
      case 25:
        return null;
      case 30:
        return null;
    }
    throw Error(o(156, t.tag));
  }
  function bm(l, t) {
    switch (Hf(t), t.tag) {
      case 1:
        return l = t.flags, l & 65536 ? (t.flags = l & -65537 | 128, t) : null;
      case 3:
        return Bt(rl), Sl(), l = t.flags, (l & 65536) !== 0 && (l & 128) === 0 ? (t.flags = l & -65537 | 128, t) : null;
      case 26:
      case 27:
      case 5:
        return _e(t), null;
      case 31:
        if (t.memoizedState !== null) {
          if (tt(t), t.alternate === null)
            throw Error(o(340));
          Ru();
        }
        return l = t.flags, l & 65536 ? (t.flags = l & -65537 | 128, t) : null;
      case 13:
        if (tt(t), l = t.memoizedState, l !== null && l.dehydrated !== null) {
          if (t.alternate === null)
            throw Error(o(340));
          Ru();
        }
        return l = t.flags, l & 65536 ? (t.flags = l & -65537 | 128, t) : null;
      case 19:
        return T(gl), null;
      case 4:
        return Sl(), null;
      case 10:
        return Bt(t.type), null;
      case 22:
      case 23:
        return tt(t), Kf(), l !== null && T(qu), l = t.flags, l & 65536 ? (t.flags = l & -65537 | 128, t) : null;
      case 24:
        return Bt(rl), null;
      case 25:
        return null;
      default:
        return null;
    }
  }
  function w0(l, t) {
    switch (Hf(t), t.tag) {
      case 3:
        Bt(rl), Sl();
        break;
      case 26:
      case 27:
      case 5:
        _e(t);
        break;
      case 4:
        Sl();
        break;
      case 31:
        t.memoizedState !== null && tt(t);
        break;
      case 13:
        tt(t);
        break;
      case 19:
        T(gl);
        break;
      case 10:
        Bt(t.type);
        break;
      case 22:
      case 23:
        tt(t), Kf(), l !== null && T(qu);
        break;
      case 24:
        Bt(rl);
    }
  }
  function ae(l, t) {
    try {
      var u = t.updateQueue, a = u !== null ? u.lastEffect : null;
      if (a !== null) {
        var e = a.next;
        u = e;
        do {
          if ((u.tag & l) === l) {
            a = void 0;
            var n = u.create, f = u.inst;
            a = n(), f.destroy = a;
          }
          u = u.next;
        } while (u !== e);
      }
    } catch (c) {
      tl(t, t.return, c);
    }
  }
  function cu(l, t, u) {
    try {
      var a = t.updateQueue, e = a !== null ? a.lastEffect : null;
      if (e !== null) {
        var n = e.next;
        a = n;
        do {
          if ((a.tag & l) === l) {
            var f = a.inst, c = f.destroy;
            if (c !== void 0) {
              f.destroy = void 0, e = t;
              var i = u, d = c;
              try {
                d();
              } catch (b) {
                tl(
                  e,
                  i,
                  b
                );
              }
            }
          }
          a = a.next;
        } while (a !== n);
      }
    } catch (b) {
      tl(t, t.return, b);
    }
  }
  function W0(l) {
    var t = l.updateQueue;
    if (t !== null) {
      var u = l.stateNode;
      try {
        Xs(t, u);
      } catch (a) {
        tl(l, l.return, a);
      }
    }
  }
  function $0(l, t, u) {
    u.props = Qu(
      l.type,
      l.memoizedProps
    ), u.state = l.memoizedState;
    try {
      u.componentWillUnmount();
    } catch (a) {
      tl(l, t, a);
    }
  }
  function ee(l, t) {
    try {
      var u = l.ref;
      if (u !== null) {
        switch (l.tag) {
          case 26:
          case 27:
          case 5:
            var a = l.stateNode;
            break;
          case 30:
            a = l.stateNode;
            break;
          default:
            a = l.stateNode;
        }
        typeof u == "function" ? l.refCleanup = u(a) : u.current = a;
      }
    } catch (e) {
      tl(l, t, e);
    }
  }
  function Ot(l, t) {
    var u = l.ref, a = l.refCleanup;
    if (u !== null)
      if (typeof a == "function")
        try {
          a();
        } catch (e) {
          tl(l, t, e);
        } finally {
          l.refCleanup = null, l = l.alternate, l != null && (l.refCleanup = null);
        }
      else if (typeof u == "function")
        try {
          u(null);
        } catch (e) {
          tl(l, t, e);
        }
      else u.current = null;
  }
  function F0(l) {
    var t = l.type, u = l.memoizedProps, a = l.stateNode;
    try {
      l: switch (t) {
        case "button":
        case "input":
        case "select":
        case "textarea":
          u.autoFocus && a.focus();
          break l;
        case "img":
          u.src ? a.src = u.src : u.srcSet && (a.srcset = u.srcSet);
      }
    } catch (e) {
      tl(l, l.return, e);
    }
  }
  function Ac(l, t, u) {
    try {
      var a = l.stateNode;
      jm(a, l.type, u, t), a[jl] = t;
    } catch (e) {
      tl(l, l.return, e);
    }
  }
  function k0(l) {
    return l.tag === 5 || l.tag === 3 || l.tag === 26 || l.tag === 27 && du(l.type) || l.tag === 4;
  }
  function _c(l) {
    l: for (; ; ) {
      for (; l.sibling === null; ) {
        if (l.return === null || k0(l.return)) return null;
        l = l.return;
      }
      for (l.sibling.return = l.return, l = l.sibling; l.tag !== 5 && l.tag !== 6 && l.tag !== 18; ) {
        if (l.tag === 27 && du(l.type) || l.flags & 2 || l.child === null || l.tag === 4) continue l;
        l.child.return = l, l = l.child;
      }
      if (!(l.flags & 2)) return l.stateNode;
    }
  }
  function Oc(l, t, u) {
    var a = l.tag;
    if (a === 5 || a === 6)
      l = l.stateNode, t ? (u.nodeType === 9 ? u.body : u.nodeName === "HTML" ? u.ownerDocument.body : u).insertBefore(l, t) : (t = u.nodeType === 9 ? u.body : u.nodeName === "HTML" ? u.ownerDocument.body : u, t.appendChild(l), u = u._reactRootContainer, u != null || t.onclick !== null || (t.onclick = Ht));
    else if (a !== 4 && (a === 27 && du(l.type) && (u = l.stateNode, t = null), l = l.child, l !== null))
      for (Oc(l, t, u), l = l.sibling; l !== null; )
        Oc(l, t, u), l = l.sibling;
  }
  function hn(l, t, u) {
    var a = l.tag;
    if (a === 5 || a === 6)
      l = l.stateNode, t ? u.insertBefore(l, t) : u.appendChild(l);
    else if (a !== 4 && (a === 27 && du(l.type) && (u = l.stateNode), l = l.child, l !== null))
      for (hn(l, t, u), l = l.sibling; l !== null; )
        hn(l, t, u), l = l.sibling;
  }
  function I0(l) {
    var t = l.stateNode, u = l.memoizedProps;
    try {
      for (var a = l.type, e = t.attributes; e.length; )
        t.removeAttributeNode(e[0]);
      Hl(t, a, u), t[Ml] = l, t[jl] = u;
    } catch (n) {
      tl(l, l.return, n);
    }
  }
  var jt = !1, Tl = !1, Mc = !1, P0 = typeof WeakSet == "function" ? WeakSet : Set, Ol = null;
  function rm(l, t) {
    if (l = l.containerInfo, wc = Yn, l = vs(l), rf(l)) {
      if ("selectionStart" in l)
        var u = {
          start: l.selectionStart,
          end: l.selectionEnd
        };
      else
        l: {
          u = (u = l.ownerDocument) && u.defaultView || window;
          var a = u.getSelection && u.getSelection();
          if (a && a.rangeCount !== 0) {
            u = a.anchorNode;
            var e = a.anchorOffset, n = a.focusNode;
            a = a.focusOffset;
            try {
              u.nodeType, n.nodeType;
            } catch {
              u = null;
              break l;
            }
            var f = 0, c = -1, i = -1, d = 0, b = 0, E = l, S = null;
            t: for (; ; ) {
              for (var g; E !== u || e !== 0 && E.nodeType !== 3 || (c = f + e), E !== n || a !== 0 && E.nodeType !== 3 || (i = f + a), E.nodeType === 3 && (f += E.nodeValue.length), (g = E.firstChild) !== null; )
                S = E, E = g;
              for (; ; ) {
                if (E === l) break t;
                if (S === u && ++d === e && (c = f), S === n && ++b === a && (i = f), (g = E.nextSibling) !== null) break;
                E = S, S = E.parentNode;
              }
              E = g;
            }
            u = c === -1 || i === -1 ? null : { start: c, end: i };
          } else u = null;
        }
      u = u || { start: 0, end: 0 };
    } else u = null;
    for (Wc = { focusedElem: l, selectionRange: u }, Yn = !1, Ol = t; Ol !== null; )
      if (t = Ol, l = t.child, (t.subtreeFlags & 1028) !== 0 && l !== null)
        l.return = t, Ol = l;
      else
        for (; Ol !== null; ) {
          switch (t = Ol, n = t.alternate, l = t.flags, t.tag) {
            case 0:
              if ((l & 4) !== 0 && (l = t.updateQueue, l = l !== null ? l.events : null, l !== null))
                for (u = 0; u < l.length; u++)
                  e = l[u], e.ref.impl = e.nextImpl;
              break;
            case 11:
            case 15:
              break;
            case 1:
              if ((l & 1024) !== 0 && n !== null) {
                l = void 0, u = t, e = n.memoizedProps, n = n.memoizedState, a = u.stateNode;
                try {
                  var p = Qu(
                    u.type,
                    e
                  );
                  l = a.getSnapshotBeforeUpdate(
                    p,
                    n
                  ), a.__reactInternalSnapshotBeforeUpdate = l;
                } catch (C) {
                  tl(
                    u,
                    u.return,
                    C
                  );
                }
              }
              break;
            case 3:
              if ((l & 1024) !== 0) {
                if (l = t.stateNode.containerInfo, u = l.nodeType, u === 9)
                  kc(l);
                else if (u === 1)
                  switch (l.nodeName) {
                    case "HEAD":
                    case "HTML":
                    case "BODY":
                      kc(l);
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
              if ((l & 1024) !== 0) throw Error(o(163));
          }
          if (l = t.sibling, l !== null) {
            l.return = t.return, Ol = l;
            break;
          }
          Ol = t.return;
        }
  }
  function ly(l, t, u) {
    var a = u.flags;
    switch (u.tag) {
      case 0:
      case 11:
      case 15:
        Lt(l, u), a & 4 && ae(5, u);
        break;
      case 1:
        if (Lt(l, u), a & 4)
          if (l = u.stateNode, t === null)
            try {
              l.componentDidMount();
            } catch (f) {
              tl(u, u.return, f);
            }
          else {
            var e = Qu(
              u.type,
              t.memoizedProps
            );
            t = t.memoizedState;
            try {
              l.componentDidUpdate(
                e,
                t,
                l.__reactInternalSnapshotBeforeUpdate
              );
            } catch (f) {
              tl(
                u,
                u.return,
                f
              );
            }
          }
        a & 64 && W0(u), a & 512 && ee(u, u.return);
        break;
      case 3:
        if (Lt(l, u), a & 64 && (l = u.updateQueue, l !== null)) {
          if (t = null, u.child !== null)
            switch (u.child.tag) {
              case 27:
              case 5:
                t = u.child.stateNode;
                break;
              case 1:
                t = u.child.stateNode;
            }
          try {
            Xs(l, t);
          } catch (f) {
            tl(u, u.return, f);
          }
        }
        break;
      case 27:
        t === null && a & 4 && I0(u);
      case 26:
      case 5:
        Lt(l, u), t === null && a & 4 && F0(u), a & 512 && ee(u, u.return);
        break;
      case 12:
        Lt(l, u);
        break;
      case 31:
        Lt(l, u), a & 4 && ay(l, u);
        break;
      case 13:
        Lt(l, u), a & 4 && ey(l, u), a & 64 && (l = u.memoizedState, l !== null && (l = l.dehydrated, l !== null && (u = pm.bind(
          null,
          u
        ), Wm(l, u))));
        break;
      case 22:
        if (a = u.memoizedState !== null || jt, !a) {
          t = t !== null && t.memoizedState !== null || Tl, e = jt;
          var n = Tl;
          jt = a, (Tl = t) && !n ? xt(
            l,
            u,
            (u.subtreeFlags & 8772) !== 0
          ) : Lt(l, u), jt = e, Tl = n;
        }
        break;
      case 30:
        break;
      default:
        Lt(l, u);
    }
  }
  function ty(l) {
    var t = l.alternate;
    t !== null && (l.alternate = null, ty(t)), l.child = null, l.deletions = null, l.sibling = null, l.tag === 5 && (t = l.stateNode, t !== null && tf(t)), l.stateNode = null, l.return = null, l.dependencies = null, l.memoizedProps = null, l.memoizedState = null, l.pendingProps = null, l.stateNode = null, l.updateQueue = null;
  }
  var ol = null, Ll = !1;
  function Zt(l, t, u) {
    for (u = u.child; u !== null; )
      uy(l, t, u), u = u.sibling;
  }
  function uy(l, t, u) {
    if (Fl && typeof Fl.onCommitFiberUnmount == "function")
      try {
        Fl.onCommitFiberUnmount(pa, u);
      } catch {
      }
    switch (u.tag) {
      case 26:
        Tl || Ot(u, t), Zt(
          l,
          t,
          u
        ), u.memoizedState ? u.memoizedState.count-- : u.stateNode && (u = u.stateNode, u.parentNode.removeChild(u));
        break;
      case 27:
        Tl || Ot(u, t);
        var a = ol, e = Ll;
        du(u.type) && (ol = u.stateNode, Ll = !1), Zt(
          l,
          t,
          u
        ), me(u.stateNode), ol = a, Ll = e;
        break;
      case 5:
        Tl || Ot(u, t);
      case 6:
        if (a = ol, e = Ll, ol = null, Zt(
          l,
          t,
          u
        ), ol = a, Ll = e, ol !== null)
          if (Ll)
            try {
              (ol.nodeType === 9 ? ol.body : ol.nodeName === "HTML" ? ol.ownerDocument.body : ol).removeChild(u.stateNode);
            } catch (n) {
              tl(
                u,
                t,
                n
              );
            }
          else
            try {
              ol.removeChild(u.stateNode);
            } catch (n) {
              tl(
                u,
                t,
                n
              );
            }
        break;
      case 18:
        ol !== null && (Ll ? (l = ol, Wy(
          l.nodeType === 9 ? l.body : l.nodeName === "HTML" ? l.ownerDocument.body : l,
          u.stateNode
        ), Ma(l)) : Wy(ol, u.stateNode));
        break;
      case 4:
        a = ol, e = Ll, ol = u.stateNode.containerInfo, Ll = !0, Zt(
          l,
          t,
          u
        ), ol = a, Ll = e;
        break;
      case 0:
      case 11:
      case 14:
      case 15:
        cu(2, u, t), Tl || cu(4, u, t), Zt(
          l,
          t,
          u
        );
        break;
      case 1:
        Tl || (Ot(u, t), a = u.stateNode, typeof a.componentWillUnmount == "function" && $0(
          u,
          t,
          a
        )), Zt(
          l,
          t,
          u
        );
        break;
      case 21:
        Zt(
          l,
          t,
          u
        );
        break;
      case 22:
        Tl = (a = Tl) || u.memoizedState !== null, Zt(
          l,
          t,
          u
        ), Tl = a;
        break;
      default:
        Zt(
          l,
          t,
          u
        );
    }
  }
  function ay(l, t) {
    if (t.memoizedState === null && (l = t.alternate, l !== null && (l = l.memoizedState, l !== null))) {
      l = l.dehydrated;
      try {
        Ma(l);
      } catch (u) {
        tl(t, t.return, u);
      }
    }
  }
  function ey(l, t) {
    if (t.memoizedState === null && (l = t.alternate, l !== null && (l = l.memoizedState, l !== null && (l = l.dehydrated, l !== null))))
      try {
        Ma(l);
      } catch (u) {
        tl(t, t.return, u);
      }
  }
  function zm(l) {
    switch (l.tag) {
      case 31:
      case 13:
      case 19:
        var t = l.stateNode;
        return t === null && (t = l.stateNode = new P0()), t;
      case 22:
        return l = l.stateNode, t = l._retryCache, t === null && (t = l._retryCache = new P0()), t;
      default:
        throw Error(o(435, l.tag));
    }
  }
  function Sn(l, t) {
    var u = zm(l);
    t.forEach(function(a) {
      if (!u.has(a)) {
        u.add(a);
        var e = Um.bind(null, l, a);
        a.then(e, e);
      }
    });
  }
  function xl(l, t) {
    var u = t.deletions;
    if (u !== null)
      for (var a = 0; a < u.length; a++) {
        var e = u[a], n = l, f = t, c = f;
        l: for (; c !== null; ) {
          switch (c.tag) {
            case 27:
              if (du(c.type)) {
                ol = c.stateNode, Ll = !1;
                break l;
              }
              break;
            case 5:
              ol = c.stateNode, Ll = !1;
              break l;
            case 3:
            case 4:
              ol = c.stateNode.containerInfo, Ll = !0;
              break l;
          }
          c = c.return;
        }
        if (ol === null) throw Error(o(160));
        uy(n, f, e), ol = null, Ll = !1, n = e.alternate, n !== null && (n.return = null), e.return = null;
      }
    if (t.subtreeFlags & 13886)
      for (t = t.child; t !== null; )
        ny(t, l), t = t.sibling;
  }
  var bt = null;
  function ny(l, t) {
    var u = l.alternate, a = l.flags;
    switch (l.tag) {
      case 0:
      case 11:
      case 14:
      case 15:
        xl(t, l), Vl(l), a & 4 && (cu(3, l, l.return), ae(3, l), cu(5, l, l.return));
        break;
      case 1:
        xl(t, l), Vl(l), a & 512 && (Tl || u === null || Ot(u, u.return)), a & 64 && jt && (l = l.updateQueue, l !== null && (a = l.callbacks, a !== null && (u = l.shared.hiddenCallbacks, l.shared.hiddenCallbacks = u === null ? a : u.concat(a))));
        break;
      case 26:
        var e = bt;
        if (xl(t, l), Vl(l), a & 512 && (Tl || u === null || Ot(u, u.return)), a & 4) {
          var n = u !== null ? u.memoizedState : null;
          if (a = l.memoizedState, u === null)
            if (a === null)
              if (l.stateNode === null) {
                l: {
                  a = l.type, u = l.memoizedProps, e = e.ownerDocument || e;
                  t: switch (a) {
                    case "title":
                      n = e.getElementsByTagName("title")[0], (!n || n[Ra] || n[Ml] || n.namespaceURI === "http://www.w3.org/2000/svg" || n.hasAttribute("itemprop")) && (n = e.createElement(a), e.head.insertBefore(
                        n,
                        e.querySelector("head > title")
                      )), Hl(n, a, u), n[Ml] = l, _l(n), a = n;
                      break l;
                    case "link":
                      var f = nv(
                        "link",
                        "href",
                        e
                      ).get(a + (u.href || ""));
                      if (f) {
                        for (var c = 0; c < f.length; c++)
                          if (n = f[c], n.getAttribute("href") === (u.href == null || u.href === "" ? null : u.href) && n.getAttribute("rel") === (u.rel == null ? null : u.rel) && n.getAttribute("title") === (u.title == null ? null : u.title) && n.getAttribute("crossorigin") === (u.crossOrigin == null ? null : u.crossOrigin)) {
                            f.splice(c, 1);
                            break t;
                          }
                      }
                      n = e.createElement(a), Hl(n, a, u), e.head.appendChild(n);
                      break;
                    case "meta":
                      if (f = nv(
                        "meta",
                        "content",
                        e
                      ).get(a + (u.content || ""))) {
                        for (c = 0; c < f.length; c++)
                          if (n = f[c], n.getAttribute("content") === (u.content == null ? null : "" + u.content) && n.getAttribute("name") === (u.name == null ? null : u.name) && n.getAttribute("property") === (u.property == null ? null : u.property) && n.getAttribute("http-equiv") === (u.httpEquiv == null ? null : u.httpEquiv) && n.getAttribute("charset") === (u.charSet == null ? null : u.charSet)) {
                            f.splice(c, 1);
                            break t;
                          }
                      }
                      n = e.createElement(a), Hl(n, a, u), e.head.appendChild(n);
                      break;
                    default:
                      throw Error(o(468, a));
                  }
                  n[Ml] = l, _l(n), a = n;
                }
                l.stateNode = a;
              } else
                fv(
                  e,
                  l.type,
                  l.stateNode
                );
            else
              l.stateNode = ev(
                e,
                a,
                l.memoizedProps
              );
          else
            n !== a ? (n === null ? u.stateNode !== null && (u = u.stateNode, u.parentNode.removeChild(u)) : n.count--, a === null ? fv(
              e,
              l.type,
              l.stateNode
            ) : ev(
              e,
              a,
              l.memoizedProps
            )) : a === null && l.stateNode !== null && Ac(
              l,
              l.memoizedProps,
              u.memoizedProps
            );
        }
        break;
      case 27:
        xl(t, l), Vl(l), a & 512 && (Tl || u === null || Ot(u, u.return)), u !== null && a & 4 && Ac(
          l,
          l.memoizedProps,
          u.memoizedProps
        );
        break;
      case 5:
        if (xl(t, l), Vl(l), a & 512 && (Tl || u === null || Ot(u, u.return)), l.flags & 32) {
          e = l.stateNode;
          try {
            $u(e, "");
          } catch (p) {
            tl(l, l.return, p);
          }
        }
        a & 4 && l.stateNode != null && (e = l.memoizedProps, Ac(
          l,
          e,
          u !== null ? u.memoizedProps : e
        )), a & 1024 && (Mc = !0);
        break;
      case 6:
        if (xl(t, l), Vl(l), a & 4) {
          if (l.stateNode === null)
            throw Error(o(162));
          a = l.memoizedProps, u = l.stateNode;
          try {
            u.nodeValue = a;
          } catch (p) {
            tl(l, l.return, p);
          }
        }
        break;
      case 3:
        if (Nn = null, e = bt, bt = Hn(t.containerInfo), xl(t, l), bt = e, Vl(l), a & 4 && u !== null && u.memoizedState.isDehydrated)
          try {
            Ma(t.containerInfo);
          } catch (p) {
            tl(l, l.return, p);
          }
        Mc && (Mc = !1, fy(l));
        break;
      case 4:
        a = bt, bt = Hn(
          l.stateNode.containerInfo
        ), xl(t, l), Vl(l), bt = a;
        break;
      case 12:
        xl(t, l), Vl(l);
        break;
      case 31:
        xl(t, l), Vl(l), a & 4 && (a = l.updateQueue, a !== null && (l.updateQueue = null, Sn(l, a)));
        break;
      case 13:
        xl(t, l), Vl(l), l.child.flags & 8192 && l.memoizedState !== null != (u !== null && u.memoizedState !== null) && (bn = $l()), a & 4 && (a = l.updateQueue, a !== null && (l.updateQueue = null, Sn(l, a)));
        break;
      case 22:
        e = l.memoizedState !== null;
        var i = u !== null && u.memoizedState !== null, d = jt, b = Tl;
        if (jt = d || e, Tl = b || i, xl(t, l), Tl = b, jt = d, Vl(l), a & 8192)
          l: for (t = l.stateNode, t._visibility = e ? t._visibility & -2 : t._visibility | 1, e && (u === null || i || jt || Tl || ju(l)), u = null, t = l; ; ) {
            if (t.tag === 5 || t.tag === 26) {
              if (u === null) {
                i = u = t;
                try {
                  if (n = i.stateNode, e)
                    f = n.style, typeof f.setProperty == "function" ? f.setProperty("display", "none", "important") : f.display = "none";
                  else {
                    c = i.stateNode;
                    var E = i.memoizedProps.style, S = E != null && E.hasOwnProperty("display") ? E.display : null;
                    c.style.display = S == null || typeof S == "boolean" ? "" : ("" + S).trim();
                  }
                } catch (p) {
                  tl(i, i.return, p);
                }
              }
            } else if (t.tag === 6) {
              if (u === null) {
                i = t;
                try {
                  i.stateNode.nodeValue = e ? "" : i.memoizedProps;
                } catch (p) {
                  tl(i, i.return, p);
                }
              }
            } else if (t.tag === 18) {
              if (u === null) {
                i = t;
                try {
                  var g = i.stateNode;
                  e ? $y(g, !0) : $y(i.stateNode, !1);
                } catch (p) {
                  tl(i, i.return, p);
                }
              }
            } else if ((t.tag !== 22 && t.tag !== 23 || t.memoizedState === null || t === l) && t.child !== null) {
              t.child.return = t, t = t.child;
              continue;
            }
            if (t === l) break l;
            for (; t.sibling === null; ) {
              if (t.return === null || t.return === l) break l;
              u === t && (u = null), t = t.return;
            }
            u === t && (u = null), t.sibling.return = t.return, t = t.sibling;
          }
        a & 4 && (a = l.updateQueue, a !== null && (u = a.retryQueue, u !== null && (a.retryQueue = null, Sn(l, u))));
        break;
      case 19:
        xl(t, l), Vl(l), a & 4 && (a = l.updateQueue, a !== null && (l.updateQueue = null, Sn(l, a)));
        break;
      case 30:
        break;
      case 21:
        break;
      default:
        xl(t, l), Vl(l);
    }
  }
  function Vl(l) {
    var t = l.flags;
    if (t & 2) {
      try {
        for (var u, a = l.return; a !== null; ) {
          if (k0(a)) {
            u = a;
            break;
          }
          a = a.return;
        }
        if (u == null) throw Error(o(160));
        switch (u.tag) {
          case 27:
            var e = u.stateNode, n = _c(l);
            hn(l, n, e);
            break;
          case 5:
            var f = u.stateNode;
            u.flags & 32 && ($u(f, ""), u.flags &= -33);
            var c = _c(l);
            hn(l, c, f);
            break;
          case 3:
          case 4:
            var i = u.stateNode.containerInfo, d = _c(l);
            Oc(
              l,
              d,
              i
            );
            break;
          default:
            throw Error(o(161));
        }
      } catch (b) {
        tl(l, l.return, b);
      }
      l.flags &= -3;
    }
    t & 4096 && (l.flags &= -4097);
  }
  function fy(l) {
    if (l.subtreeFlags & 1024)
      for (l = l.child; l !== null; ) {
        var t = l;
        fy(t), t.tag === 5 && t.flags & 1024 && t.stateNode.reset(), l = l.sibling;
      }
  }
  function Lt(l, t) {
    if (t.subtreeFlags & 8772)
      for (t = t.child; t !== null; )
        ly(l, t.alternate, t), t = t.sibling;
  }
  function ju(l) {
    for (l = l.child; l !== null; ) {
      var t = l;
      switch (t.tag) {
        case 0:
        case 11:
        case 14:
        case 15:
          cu(4, t, t.return), ju(t);
          break;
        case 1:
          Ot(t, t.return);
          var u = t.stateNode;
          typeof u.componentWillUnmount == "function" && $0(
            t,
            t.return,
            u
          ), ju(t);
          break;
        case 27:
          me(t.stateNode);
        case 26:
        case 5:
          Ot(t, t.return), ju(t);
          break;
        case 22:
          t.memoizedState === null && ju(t);
          break;
        case 30:
          ju(t);
          break;
        default:
          ju(t);
      }
      l = l.sibling;
    }
  }
  function xt(l, t, u) {
    for (u = u && (t.subtreeFlags & 8772) !== 0, t = t.child; t !== null; ) {
      var a = t.alternate, e = l, n = t, f = n.flags;
      switch (n.tag) {
        case 0:
        case 11:
        case 15:
          xt(
            e,
            n,
            u
          ), ae(4, n);
          break;
        case 1:
          if (xt(
            e,
            n,
            u
          ), a = n, e = a.stateNode, typeof e.componentDidMount == "function")
            try {
              e.componentDidMount();
            } catch (d) {
              tl(a, a.return, d);
            }
          if (a = n, e = a.updateQueue, e !== null) {
            var c = a.stateNode;
            try {
              var i = e.shared.hiddenCallbacks;
              if (i !== null)
                for (e.shared.hiddenCallbacks = null, e = 0; e < i.length; e++)
                  Gs(i[e], c);
            } catch (d) {
              tl(a, a.return, d);
            }
          }
          u && f & 64 && W0(n), ee(n, n.return);
          break;
        case 27:
          I0(n);
        case 26:
        case 5:
          xt(
            e,
            n,
            u
          ), u && a === null && f & 4 && F0(n), ee(n, n.return);
          break;
        case 12:
          xt(
            e,
            n,
            u
          );
          break;
        case 31:
          xt(
            e,
            n,
            u
          ), u && f & 4 && ay(e, n);
          break;
        case 13:
          xt(
            e,
            n,
            u
          ), u && f & 4 && ey(e, n);
          break;
        case 22:
          n.memoizedState === null && xt(
            e,
            n,
            u
          ), ee(n, n.return);
          break;
        case 30:
          break;
        default:
          xt(
            e,
            n,
            u
          );
      }
      t = t.sibling;
    }
  }
  function Dc(l, t) {
    var u = null;
    l !== null && l.memoizedState !== null && l.memoizedState.cachePool !== null && (u = l.memoizedState.cachePool.pool), l = null, t.memoizedState !== null && t.memoizedState.cachePool !== null && (l = t.memoizedState.cachePool.pool), l !== u && (l != null && l.refCount++, u != null && Va(u));
  }
  function pc(l, t) {
    l = null, t.alternate !== null && (l = t.alternate.memoizedState.cache), t = t.memoizedState.cache, t !== l && (t.refCount++, l != null && Va(l));
  }
  function rt(l, t, u, a) {
    if (t.subtreeFlags & 10256)
      for (t = t.child; t !== null; )
        cy(
          l,
          t,
          u,
          a
        ), t = t.sibling;
  }
  function cy(l, t, u, a) {
    var e = t.flags;
    switch (t.tag) {
      case 0:
      case 11:
      case 15:
        rt(
          l,
          t,
          u,
          a
        ), e & 2048 && ae(9, t);
        break;
      case 1:
        rt(
          l,
          t,
          u,
          a
        );
        break;
      case 3:
        rt(
          l,
          t,
          u,
          a
        ), e & 2048 && (l = null, t.alternate !== null && (l = t.alternate.memoizedState.cache), t = t.memoizedState.cache, t !== l && (t.refCount++, l != null && Va(l)));
        break;
      case 12:
        if (e & 2048) {
          rt(
            l,
            t,
            u,
            a
          ), l = t.stateNode;
          try {
            var n = t.memoizedProps, f = n.id, c = n.onPostCommit;
            typeof c == "function" && c(
              f,
              t.alternate === null ? "mount" : "update",
              l.passiveEffectDuration,
              -0
            );
          } catch (i) {
            tl(t, t.return, i);
          }
        } else
          rt(
            l,
            t,
            u,
            a
          );
        break;
      case 31:
        rt(
          l,
          t,
          u,
          a
        );
        break;
      case 13:
        rt(
          l,
          t,
          u,
          a
        );
        break;
      case 23:
        break;
      case 22:
        n = t.stateNode, f = t.alternate, t.memoizedState !== null ? n._visibility & 2 ? rt(
          l,
          t,
          u,
          a
        ) : ne(l, t) : n._visibility & 2 ? rt(
          l,
          t,
          u,
          a
        ) : (n._visibility |= 2, ha(
          l,
          t,
          u,
          a,
          (t.subtreeFlags & 10256) !== 0 || !1
        )), e & 2048 && Dc(f, t);
        break;
      case 24:
        rt(
          l,
          t,
          u,
          a
        ), e & 2048 && pc(t.alternate, t);
        break;
      default:
        rt(
          l,
          t,
          u,
          a
        );
    }
  }
  function ha(l, t, u, a, e) {
    for (e = e && ((t.subtreeFlags & 10256) !== 0 || !1), t = t.child; t !== null; ) {
      var n = l, f = t, c = u, i = a, d = f.flags;
      switch (f.tag) {
        case 0:
        case 11:
        case 15:
          ha(
            n,
            f,
            c,
            i,
            e
          ), ae(8, f);
          break;
        case 23:
          break;
        case 22:
          var b = f.stateNode;
          f.memoizedState !== null ? b._visibility & 2 ? ha(
            n,
            f,
            c,
            i,
            e
          ) : ne(
            n,
            f
          ) : (b._visibility |= 2, ha(
            n,
            f,
            c,
            i,
            e
          )), e && d & 2048 && Dc(
            f.alternate,
            f
          );
          break;
        case 24:
          ha(
            n,
            f,
            c,
            i,
            e
          ), e && d & 2048 && pc(f.alternate, f);
          break;
        default:
          ha(
            n,
            f,
            c,
            i,
            e
          );
      }
      t = t.sibling;
    }
  }
  function ne(l, t) {
    if (t.subtreeFlags & 10256)
      for (t = t.child; t !== null; ) {
        var u = l, a = t, e = a.flags;
        switch (a.tag) {
          case 22:
            ne(u, a), e & 2048 && Dc(
              a.alternate,
              a
            );
            break;
          case 24:
            ne(u, a), e & 2048 && pc(a.alternate, a);
            break;
          default:
            ne(u, a);
        }
        t = t.sibling;
      }
  }
  var fe = 8192;
  function Sa(l, t, u) {
    if (l.subtreeFlags & fe)
      for (l = l.child; l !== null; )
        iy(
          l,
          t,
          u
        ), l = l.sibling;
  }
  function iy(l, t, u) {
    switch (l.tag) {
      case 26:
        Sa(
          l,
          t,
          u
        ), l.flags & fe && l.memoizedState !== null && fd(
          u,
          bt,
          l.memoizedState,
          l.memoizedProps
        );
        break;
      case 5:
        Sa(
          l,
          t,
          u
        );
        break;
      case 3:
      case 4:
        var a = bt;
        bt = Hn(l.stateNode.containerInfo), Sa(
          l,
          t,
          u
        ), bt = a;
        break;
      case 22:
        l.memoizedState === null && (a = l.alternate, a !== null && a.memoizedState !== null ? (a = fe, fe = 16777216, Sa(
          l,
          t,
          u
        ), fe = a) : Sa(
          l,
          t,
          u
        ));
        break;
      default:
        Sa(
          l,
          t,
          u
        );
    }
  }
  function sy(l) {
    var t = l.alternate;
    if (t !== null && (l = t.child, l !== null)) {
      t.child = null;
      do
        t = l.sibling, l.sibling = null, l = t;
      while (l !== null);
    }
  }
  function ce(l) {
    var t = l.deletions;
    if ((l.flags & 16) !== 0) {
      if (t !== null)
        for (var u = 0; u < t.length; u++) {
          var a = t[u];
          Ol = a, vy(
            a,
            l
          );
        }
      sy(l);
    }
    if (l.subtreeFlags & 10256)
      for (l = l.child; l !== null; )
        yy(l), l = l.sibling;
  }
  function yy(l) {
    switch (l.tag) {
      case 0:
      case 11:
      case 15:
        ce(l), l.flags & 2048 && cu(9, l, l.return);
        break;
      case 3:
        ce(l);
        break;
      case 12:
        ce(l);
        break;
      case 22:
        var t = l.stateNode;
        l.memoizedState !== null && t._visibility & 2 && (l.return === null || l.return.tag !== 13) ? (t._visibility &= -3, gn(l)) : ce(l);
        break;
      default:
        ce(l);
    }
  }
  function gn(l) {
    var t = l.deletions;
    if ((l.flags & 16) !== 0) {
      if (t !== null)
        for (var u = 0; u < t.length; u++) {
          var a = t[u];
          Ol = a, vy(
            a,
            l
          );
        }
      sy(l);
    }
    for (l = l.child; l !== null; ) {
      switch (t = l, t.tag) {
        case 0:
        case 11:
        case 15:
          cu(8, t, t.return), gn(t);
          break;
        case 22:
          u = t.stateNode, u._visibility & 2 && (u._visibility &= -3, gn(t));
          break;
        default:
          gn(t);
      }
      l = l.sibling;
    }
  }
  function vy(l, t) {
    for (; Ol !== null; ) {
      var u = Ol;
      switch (u.tag) {
        case 0:
        case 11:
        case 15:
          cu(8, u, t);
          break;
        case 23:
        case 22:
          if (u.memoizedState !== null && u.memoizedState.cachePool !== null) {
            var a = u.memoizedState.cachePool.pool;
            a != null && a.refCount++;
          }
          break;
        case 24:
          Va(u.memoizedState.cache);
      }
      if (a = u.child, a !== null) a.return = u, Ol = a;
      else
        l: for (u = l; Ol !== null; ) {
          a = Ol;
          var e = a.sibling, n = a.return;
          if (ty(a), a === u) {
            Ol = null;
            break l;
          }
          if (e !== null) {
            e.return = n, Ol = e;
            break l;
          }
          Ol = n;
        }
    }
  }
  var Em = {
    getCacheForType: function(l) {
      var t = pl(rl), u = t.data.get(l);
      return u === void 0 && (u = l(), t.data.set(l, u)), u;
    },
    cacheSignal: function() {
      return pl(rl).controller.signal;
    }
  }, Tm = typeof WeakMap == "function" ? WeakMap : Map, I = 0, sl = null, L = null, V = 0, ll = 0, ut = null, iu = !1, ga = !1, Uc = !1, Vt = 0, dl = 0, su = 0, Zu = 0, Hc = 0, at = 0, ba = 0, ie = null, Kl = null, Rc = !1, bn = 0, oy = 0, rn = 1 / 0, zn = null, yu = null, Al = 0, vu = null, ra = null, Kt = 0, Nc = 0, Cc = null, my = null, se = 0, qc = null;
  function et() {
    return (I & 2) !== 0 && V !== 0 ? V & -V : r.T !== null ? jc() : Ui();
  }
  function dy() {
    if (at === 0)
      if ((V & 536870912) === 0 || w) {
        var l = De;
        De <<= 1, (De & 3932160) === 0 && (De = 262144), at = l;
      } else at = 536870912;
    return l = lt.current, l !== null && (l.flags |= 32), at;
  }
  function Jl(l, t, u) {
    (l === sl && (ll === 2 || ll === 9) || l.cancelPendingCommit !== null) && (za(l, 0), ou(
      l,
      V,
      at,
      !1
    )), Ha(l, u), ((I & 2) === 0 || l !== sl) && (l === sl && ((I & 2) === 0 && (Zu |= u), dl === 4 && ou(
      l,
      V,
      at,
      !1
    )), Mt(l));
  }
  function hy(l, t, u) {
    if ((I & 6) !== 0) throw Error(o(327));
    var a = !u && (t & 127) === 0 && (t & l.expiredLanes) === 0 || Ua(l, t), e = a ? Om(l, t) : Yc(l, t, !0), n = a;
    do {
      if (e === 0) {
        ga && !a && ou(l, t, 0, !1);
        break;
      } else {
        if (u = l.current.alternate, n && !Am(u)) {
          e = Yc(l, t, !1), n = !1;
          continue;
        }
        if (e === 2) {
          if (n = t, l.errorRecoveryDisabledLanes & n)
            var f = 0;
          else
            f = l.pendingLanes & -536870913, f = f !== 0 ? f : f & 536870912 ? 536870912 : 0;
          if (f !== 0) {
            t = f;
            l: {
              var c = l;
              e = ie;
              var i = c.current.memoizedState.isDehydrated;
              if (i && (za(c, f).flags |= 256), f = Yc(
                c,
                f,
                !1
              ), f !== 2) {
                if (Uc && !i) {
                  c.errorRecoveryDisabledLanes |= n, Zu |= n, e = 4;
                  break l;
                }
                n = Kl, Kl = e, n !== null && (Kl === null ? Kl = n : Kl.push.apply(
                  Kl,
                  n
                ));
              }
              e = f;
            }
            if (n = !1, e !== 2) continue;
          }
        }
        if (e === 1) {
          za(l, 0), ou(l, t, 0, !0);
          break;
        }
        l: {
          switch (a = l, n = e, n) {
            case 0:
            case 1:
              throw Error(o(345));
            case 4:
              if ((t & 4194048) !== t) break;
            case 6:
              ou(
                a,
                t,
                at,
                !iu
              );
              break l;
            case 2:
              Kl = null;
              break;
            case 3:
            case 5:
              break;
            default:
              throw Error(o(329));
          }
          if ((t & 62914560) === t && (e = bn + 300 - $l(), 10 < e)) {
            if (ou(
              a,
              t,
              at,
              !iu
            ), Ue(a, 0, !0) !== 0) break l;
            Kt = t, a.timeoutHandle = Jy(
              Sy.bind(
                null,
                a,
                u,
                Kl,
                zn,
                Rc,
                t,
                at,
                Zu,
                ba,
                iu,
                n,
                "Throttled",
                -0,
                0
              ),
              e
            );
            break l;
          }
          Sy(
            a,
            u,
            Kl,
            zn,
            Rc,
            t,
            at,
            Zu,
            ba,
            iu,
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
  function Sy(l, t, u, a, e, n, f, c, i, d, b, E, S, g) {
    if (l.timeoutHandle = -1, E = t.subtreeFlags, E & 8192 || (E & 16785408) === 16785408) {
      E = {
        stylesheets: null,
        count: 0,
        imgCount: 0,
        imgBytes: 0,
        suspenseyImages: [],
        waitingForImages: !0,
        waitingForViewTransition: !1,
        unsuspend: Ht
      }, iy(
        t,
        n,
        E
      );
      var p = (n & 62914560) === n ? bn - $l() : (n & 4194048) === n ? oy - $l() : 0;
      if (p = cd(
        E,
        p
      ), p !== null) {
        Kt = n, l.cancelPendingCommit = p(
          _y.bind(
            null,
            l,
            t,
            n,
            u,
            a,
            e,
            f,
            c,
            i,
            b,
            E,
            null,
            S,
            g
          )
        ), ou(l, n, f, !d);
        return;
      }
    }
    _y(
      l,
      t,
      n,
      u,
      a,
      e,
      f,
      c,
      i
    );
  }
  function Am(l) {
    for (var t = l; ; ) {
      var u = t.tag;
      if ((u === 0 || u === 11 || u === 15) && t.flags & 16384 && (u = t.updateQueue, u !== null && (u = u.stores, u !== null)))
        for (var a = 0; a < u.length; a++) {
          var e = u[a], n = e.getSnapshot;
          e = e.value;
          try {
            if (!Il(n(), e)) return !1;
          } catch {
            return !1;
          }
        }
      if (u = t.child, t.subtreeFlags & 16384 && u !== null)
        u.return = t, t = u;
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
  function ou(l, t, u, a) {
    t &= ~Hc, t &= ~Zu, l.suspendedLanes |= t, l.pingedLanes &= ~t, a && (l.warmLanes |= t), a = l.expirationTimes;
    for (var e = t; 0 < e; ) {
      var n = 31 - kl(e), f = 1 << n;
      a[n] = -1, e &= ~f;
    }
    u !== 0 && Mi(l, u, t);
  }
  function En() {
    return (I & 6) === 0 ? (ye(0), !1) : !0;
  }
  function Bc() {
    if (L !== null) {
      if (ll === 0)
        var l = L.return;
      else
        l = L, qt = Nu = null, kf(l), ya = null, Ja = 0, l = L;
      for (; l !== null; )
        w0(l.alternate, l), l = l.return;
      L = null;
    }
  }
  function za(l, t) {
    var u = l.timeoutHandle;
    u !== -1 && (l.timeoutHandle = -1, xm(u)), u = l.cancelPendingCommit, u !== null && (l.cancelPendingCommit = null, u()), Kt = 0, Bc(), sl = l, L = u = Nt(l.current, null), V = t, ll = 0, ut = null, iu = !1, ga = Ua(l, t), Uc = !1, ba = at = Hc = Zu = su = dl = 0, Kl = ie = null, Rc = !1, (t & 8) !== 0 && (t |= t & 32);
    var a = l.entangledLanes;
    if (a !== 0)
      for (l = l.entanglements, a &= t; 0 < a; ) {
        var e = 31 - kl(a), n = 1 << e;
        t |= l[e], a &= ~n;
      }
    return Vt = t, Ze(), u;
  }
  function gy(l, t) {
    X = null, r.H = le, t === sa || t === $e ? (t = Cs(), ll = 3) : t === Qf ? (t = Cs(), ll = 4) : ll = t === mc ? 8 : t !== null && typeof t == "object" && typeof t.then == "function" ? 6 : 1, ut = t, L === null && (dl = 1, yn(
      l,
      st(t, l.current)
    ));
  }
  function by() {
    var l = lt.current;
    return l === null ? !0 : (V & 4194048) === V ? mt === null : (V & 62914560) === V || (V & 536870912) !== 0 ? l === mt : !1;
  }
  function ry() {
    var l = r.H;
    return r.H = le, l === null ? le : l;
  }
  function zy() {
    var l = r.A;
    return r.A = Em, l;
  }
  function Tn() {
    dl = 4, iu || (V & 4194048) !== V && lt.current !== null || (ga = !0), (su & 134217727) === 0 && (Zu & 134217727) === 0 || sl === null || ou(
      sl,
      V,
      at,
      !1
    );
  }
  function Yc(l, t, u) {
    var a = I;
    I |= 2;
    var e = ry(), n = zy();
    (sl !== l || V !== t) && (zn = null, za(l, t)), t = !1;
    var f = dl;
    l: do
      try {
        if (ll !== 0 && L !== null) {
          var c = L, i = ut;
          switch (ll) {
            case 8:
              Bc(), f = 6;
              break l;
            case 3:
            case 2:
            case 9:
            case 6:
              lt.current === null && (t = !0);
              var d = ll;
              if (ll = 0, ut = null, Ea(l, c, i, d), u && ga) {
                f = 0;
                break l;
              }
              break;
            default:
              d = ll, ll = 0, ut = null, Ea(l, c, i, d);
          }
        }
        _m(), f = dl;
        break;
      } catch (b) {
        gy(l, b);
      }
    while (!0);
    return t && l.shellSuspendCounter++, qt = Nu = null, I = a, r.H = e, r.A = n, L === null && (sl = null, V = 0, Ze()), f;
  }
  function _m() {
    for (; L !== null; ) Ey(L);
  }
  function Om(l, t) {
    var u = I;
    I |= 2;
    var a = ry(), e = zy();
    sl !== l || V !== t ? (zn = null, rn = $l() + 500, za(l, t)) : ga = Ua(
      l,
      t
    );
    l: do
      try {
        if (ll !== 0 && L !== null) {
          t = L;
          var n = ut;
          t: switch (ll) {
            case 1:
              ll = 0, ut = null, Ea(l, t, n, 1);
              break;
            case 2:
            case 9:
              if (Rs(n)) {
                ll = 0, ut = null, Ty(t);
                break;
              }
              t = function() {
                ll !== 2 && ll !== 9 || sl !== l || (ll = 7), Mt(l);
              }, n.then(t, t);
              break l;
            case 3:
              ll = 7;
              break l;
            case 4:
              ll = 5;
              break l;
            case 7:
              Rs(n) ? (ll = 0, ut = null, Ty(t)) : (ll = 0, ut = null, Ea(l, t, n, 7));
              break;
            case 5:
              var f = null;
              switch (L.tag) {
                case 26:
                  f = L.memoizedState;
                case 5:
                case 27:
                  var c = L;
                  if (f ? cv(f) : c.stateNode.complete) {
                    ll = 0, ut = null;
                    var i = c.sibling;
                    if (i !== null) L = i;
                    else {
                      var d = c.return;
                      d !== null ? (L = d, An(d)) : L = null;
                    }
                    break t;
                  }
              }
              ll = 0, ut = null, Ea(l, t, n, 5);
              break;
            case 6:
              ll = 0, ut = null, Ea(l, t, n, 6);
              break;
            case 8:
              Bc(), dl = 6;
              break l;
            default:
              throw Error(o(462));
          }
        }
        Mm();
        break;
      } catch (b) {
        gy(l, b);
      }
    while (!0);
    return qt = Nu = null, r.H = a, r.A = e, I = u, L !== null ? 0 : (sl = null, V = 0, Ze(), dl);
  }
  function Mm() {
    for (; L !== null && !Wv(); )
      Ey(L);
  }
  function Ey(l) {
    var t = K0(l.alternate, l, Vt);
    l.memoizedProps = l.pendingProps, t === null ? An(l) : L = t;
  }
  function Ty(l) {
    var t = l, u = t.alternate;
    switch (t.tag) {
      case 15:
      case 0:
        t = Q0(
          u,
          t,
          t.pendingProps,
          t.type,
          void 0,
          V
        );
        break;
      case 11:
        t = Q0(
          u,
          t,
          t.pendingProps,
          t.type.render,
          t.ref,
          V
        );
        break;
      case 5:
        kf(t);
      default:
        w0(u, t), t = L = zs(t, Vt), t = K0(u, t, Vt);
    }
    l.memoizedProps = l.pendingProps, t === null ? An(l) : L = t;
  }
  function Ea(l, t, u, a) {
    qt = Nu = null, kf(t), ya = null, Ja = 0;
    var e = t.return;
    try {
      if (dm(
        l,
        e,
        t,
        u,
        V
      )) {
        dl = 1, yn(
          l,
          st(u, l.current)
        ), L = null;
        return;
      }
    } catch (n) {
      if (e !== null) throw L = e, n;
      dl = 1, yn(
        l,
        st(u, l.current)
      ), L = null;
      return;
    }
    t.flags & 32768 ? (w || a === 1 ? l = !0 : ga || (V & 536870912) !== 0 ? l = !1 : (iu = l = !0, (a === 2 || a === 9 || a === 3 || a === 6) && (a = lt.current, a !== null && a.tag === 13 && (a.flags |= 16384))), Ay(t, l)) : An(t);
  }
  function An(l) {
    var t = l;
    do {
      if ((t.flags & 32768) !== 0) {
        Ay(
          t,
          iu
        );
        return;
      }
      l = t.return;
      var u = gm(
        t.alternate,
        t,
        Vt
      );
      if (u !== null) {
        L = u;
        return;
      }
      if (t = t.sibling, t !== null) {
        L = t;
        return;
      }
      L = t = l;
    } while (t !== null);
    dl === 0 && (dl = 5);
  }
  function Ay(l, t) {
    do {
      var u = bm(l.alternate, l);
      if (u !== null) {
        u.flags &= 32767, L = u;
        return;
      }
      if (u = l.return, u !== null && (u.flags |= 32768, u.subtreeFlags = 0, u.deletions = null), !t && (l = l.sibling, l !== null)) {
        L = l;
        return;
      }
      L = l = u;
    } while (l !== null);
    dl = 6, L = null;
  }
  function _y(l, t, u, a, e, n, f, c, i) {
    l.cancelPendingCommit = null;
    do
      _n();
    while (Al !== 0);
    if ((I & 6) !== 0) throw Error(o(327));
    if (t !== null) {
      if (t === l.current) throw Error(o(177));
      if (n = t.lanes | t.childLanes, n |= _f, eo(
        l,
        u,
        n,
        f,
        c,
        i
      ), l === sl && (L = sl = null, V = 0), ra = t, vu = l, Kt = u, Nc = n, Cc = e, my = a, (t.subtreeFlags & 10256) !== 0 || (t.flags & 10256) !== 0 ? (l.callbackNode = null, l.callbackPriority = 0, Hm(Oe, function() {
        return Uy(), null;
      })) : (l.callbackNode = null, l.callbackPriority = 0), a = (t.flags & 13878) !== 0, (t.subtreeFlags & 13878) !== 0 || a) {
        a = r.T, r.T = null, e = M.p, M.p = 2, f = I, I |= 4;
        try {
          rm(l, t, u);
        } finally {
          I = f, M.p = e, r.T = a;
        }
      }
      Al = 1, Oy(), My(), Dy();
    }
  }
  function Oy() {
    if (Al === 1) {
      Al = 0;
      var l = vu, t = ra, u = (t.flags & 13878) !== 0;
      if ((t.subtreeFlags & 13878) !== 0 || u) {
        u = r.T, r.T = null;
        var a = M.p;
        M.p = 2;
        var e = I;
        I |= 4;
        try {
          ny(t, l);
          var n = Wc, f = vs(l.containerInfo), c = n.focusedElem, i = n.selectionRange;
          if (f !== c && c && c.ownerDocument && ys(
            c.ownerDocument.documentElement,
            c
          )) {
            if (i !== null && rf(c)) {
              var d = i.start, b = i.end;
              if (b === void 0 && (b = d), "selectionStart" in c)
                c.selectionStart = d, c.selectionEnd = Math.min(
                  b,
                  c.value.length
                );
              else {
                var E = c.ownerDocument || document, S = E && E.defaultView || window;
                if (S.getSelection) {
                  var g = S.getSelection(), p = c.textContent.length, C = Math.min(i.start, p), cl = i.end === void 0 ? C : Math.min(i.end, p);
                  !g.extend && C > cl && (f = cl, cl = C, C = f);
                  var v = ss(
                    c,
                    C
                  ), s = ss(
                    c,
                    cl
                  );
                  if (v && s && (g.rangeCount !== 1 || g.anchorNode !== v.node || g.anchorOffset !== v.offset || g.focusNode !== s.node || g.focusOffset !== s.offset)) {
                    var m = E.createRange();
                    m.setStart(v.node, v.offset), g.removeAllRanges(), C > cl ? (g.addRange(m), g.extend(s.node, s.offset)) : (m.setEnd(s.node, s.offset), g.addRange(m));
                  }
                }
              }
            }
            for (E = [], g = c; g = g.parentNode; )
              g.nodeType === 1 && E.push({
                element: g,
                left: g.scrollLeft,
                top: g.scrollTop
              });
            for (typeof c.focus == "function" && c.focus(), c = 0; c < E.length; c++) {
              var z = E[c];
              z.element.scrollLeft = z.left, z.element.scrollTop = z.top;
            }
          }
          Yn = !!wc, Wc = wc = null;
        } finally {
          I = e, M.p = a, r.T = u;
        }
      }
      l.current = t, Al = 2;
    }
  }
  function My() {
    if (Al === 2) {
      Al = 0;
      var l = vu, t = ra, u = (t.flags & 8772) !== 0;
      if ((t.subtreeFlags & 8772) !== 0 || u) {
        u = r.T, r.T = null;
        var a = M.p;
        M.p = 2;
        var e = I;
        I |= 4;
        try {
          ly(l, t.alternate, t);
        } finally {
          I = e, M.p = a, r.T = u;
        }
      }
      Al = 3;
    }
  }
  function Dy() {
    if (Al === 4 || Al === 3) {
      Al = 0, $v();
      var l = vu, t = ra, u = Kt, a = my;
      (t.subtreeFlags & 10256) !== 0 || (t.flags & 10256) !== 0 ? Al = 5 : (Al = 0, ra = vu = null, py(l, l.pendingLanes));
      var e = l.pendingLanes;
      if (e === 0 && (yu = null), Pn(u), t = t.stateNode, Fl && typeof Fl.onCommitFiberRoot == "function")
        try {
          Fl.onCommitFiberRoot(
            pa,
            t,
            void 0,
            (t.current.flags & 128) === 128
          );
        } catch {
        }
      if (a !== null) {
        t = r.T, e = M.p, M.p = 2, r.T = null;
        try {
          for (var n = l.onRecoverableError, f = 0; f < a.length; f++) {
            var c = a[f];
            n(c.value, {
              componentStack: c.stack
            });
          }
        } finally {
          r.T = t, M.p = e;
        }
      }
      (Kt & 3) !== 0 && _n(), Mt(l), e = l.pendingLanes, (u & 261930) !== 0 && (e & 42) !== 0 ? l === qc ? se++ : (se = 0, qc = l) : se = 0, ye(0);
    }
  }
  function py(l, t) {
    (l.pooledCacheLanes &= t) === 0 && (t = l.pooledCache, t != null && (l.pooledCache = null, Va(t)));
  }
  function _n() {
    return Oy(), My(), Dy(), Uy();
  }
  function Uy() {
    if (Al !== 5) return !1;
    var l = vu, t = Nc;
    Nc = 0;
    var u = Pn(Kt), a = r.T, e = M.p;
    try {
      M.p = 32 > u ? 32 : u, r.T = null, u = Cc, Cc = null;
      var n = vu, f = Kt;
      if (Al = 0, ra = vu = null, Kt = 0, (I & 6) !== 0) throw Error(o(331));
      var c = I;
      if (I |= 4, yy(n.current), cy(
        n,
        n.current,
        f,
        u
      ), I = c, ye(0, !1), Fl && typeof Fl.onPostCommitFiberRoot == "function")
        try {
          Fl.onPostCommitFiberRoot(pa, n);
        } catch {
        }
      return !0;
    } finally {
      M.p = e, r.T = a, py(l, t);
    }
  }
  function Hy(l, t, u) {
    t = st(u, t), t = oc(l.stateNode, t, 2), l = eu(l, t, 2), l !== null && (Ha(l, 2), Mt(l));
  }
  function tl(l, t, u) {
    if (l.tag === 3)
      Hy(l, l, u);
    else
      for (; t !== null; ) {
        if (t.tag === 3) {
          Hy(
            t,
            l,
            u
          );
          break;
        } else if (t.tag === 1) {
          var a = t.stateNode;
          if (typeof t.type.getDerivedStateFromError == "function" || typeof a.componentDidCatch == "function" && (yu === null || !yu.has(a))) {
            l = st(u, l), u = R0(2), a = eu(t, u, 2), a !== null && (N0(
              u,
              a,
              t,
              l
            ), Ha(a, 2), Mt(a));
            break;
          }
        }
        t = t.return;
      }
  }
  function Gc(l, t, u) {
    var a = l.pingCache;
    if (a === null) {
      a = l.pingCache = new Tm();
      var e = /* @__PURE__ */ new Set();
      a.set(t, e);
    } else
      e = a.get(t), e === void 0 && (e = /* @__PURE__ */ new Set(), a.set(t, e));
    e.has(u) || (Uc = !0, e.add(u), l = Dm.bind(null, l, t, u), t.then(l, l));
  }
  function Dm(l, t, u) {
    var a = l.pingCache;
    a !== null && a.delete(t), l.pingedLanes |= l.suspendedLanes & u, l.warmLanes &= ~u, sl === l && (V & u) === u && (dl === 4 || dl === 3 && (V & 62914560) === V && 300 > $l() - bn ? (I & 2) === 0 && za(l, 0) : Hc |= u, ba === V && (ba = 0)), Mt(l);
  }
  function Ry(l, t) {
    t === 0 && (t = Oi()), l = Uu(l, t), l !== null && (Ha(l, t), Mt(l));
  }
  function pm(l) {
    var t = l.memoizedState, u = 0;
    t !== null && (u = t.retryLane), Ry(l, u);
  }
  function Um(l, t) {
    var u = 0;
    switch (l.tag) {
      case 31:
      case 13:
        var a = l.stateNode, e = l.memoizedState;
        e !== null && (u = e.retryLane);
        break;
      case 19:
        a = l.stateNode;
        break;
      case 22:
        a = l.stateNode._retryCache;
        break;
      default:
        throw Error(o(314));
    }
    a !== null && a.delete(t), Ry(l, u);
  }
  function Hm(l, t) {
    return $n(l, t);
  }
  var On = null, Ta = null, Xc = !1, Mn = !1, Qc = !1, mu = 0;
  function Mt(l) {
    l !== Ta && l.next === null && (Ta === null ? On = Ta = l : Ta = Ta.next = l), Mn = !0, Xc || (Xc = !0, Nm());
  }
  function ye(l, t) {
    if (!Qc && Mn) {
      Qc = !0;
      do
        for (var u = !1, a = On; a !== null; ) {
          if (l !== 0) {
            var e = a.pendingLanes;
            if (e === 0) var n = 0;
            else {
              var f = a.suspendedLanes, c = a.pingedLanes;
              n = (1 << 31 - kl(42 | l) + 1) - 1, n &= e & ~(f & ~c), n = n & 201326741 ? n & 201326741 | 1 : n ? n | 2 : 0;
            }
            n !== 0 && (u = !0, By(a, n));
          } else
            n = V, n = Ue(
              a,
              a === sl ? n : 0,
              a.cancelPendingCommit !== null || a.timeoutHandle !== -1
            ), (n & 3) === 0 || Ua(a, n) || (u = !0, By(a, n));
          a = a.next;
        }
      while (u);
      Qc = !1;
    }
  }
  function Rm() {
    Ny();
  }
  function Ny() {
    Mn = Xc = !1;
    var l = 0;
    mu !== 0 && Lm() && (l = mu);
    for (var t = $l(), u = null, a = On; a !== null; ) {
      var e = a.next, n = Cy(a, t);
      n === 0 ? (a.next = null, u === null ? On = e : u.next = e, e === null && (Ta = u)) : (u = a, (l !== 0 || (n & 3) !== 0) && (Mn = !0)), a = e;
    }
    Al !== 0 && Al !== 5 || ye(l), mu !== 0 && (mu = 0);
  }
  function Cy(l, t) {
    for (var u = l.suspendedLanes, a = l.pingedLanes, e = l.expirationTimes, n = l.pendingLanes & -62914561; 0 < n; ) {
      var f = 31 - kl(n), c = 1 << f, i = e[f];
      i === -1 ? ((c & u) === 0 || (c & a) !== 0) && (e[f] = ao(c, t)) : i <= t && (l.expiredLanes |= c), n &= ~c;
    }
    if (t = sl, u = V, u = Ue(
      l,
      l === t ? u : 0,
      l.cancelPendingCommit !== null || l.timeoutHandle !== -1
    ), a = l.callbackNode, u === 0 || l === t && (ll === 2 || ll === 9) || l.cancelPendingCommit !== null)
      return a !== null && a !== null && Fn(a), l.callbackNode = null, l.callbackPriority = 0;
    if ((u & 3) === 0 || Ua(l, u)) {
      if (t = u & -u, t === l.callbackPriority) return t;
      switch (a !== null && Fn(a), Pn(u)) {
        case 2:
        case 8:
          u = Ai;
          break;
        case 32:
          u = Oe;
          break;
        case 268435456:
          u = _i;
          break;
        default:
          u = Oe;
      }
      return a = qy.bind(null, l), u = $n(u, a), l.callbackPriority = t, l.callbackNode = u, t;
    }
    return a !== null && a !== null && Fn(a), l.callbackPriority = 2, l.callbackNode = null, 2;
  }
  function qy(l, t) {
    if (Al !== 0 && Al !== 5)
      return l.callbackNode = null, l.callbackPriority = 0, null;
    var u = l.callbackNode;
    if (_n() && l.callbackNode !== u)
      return null;
    var a = V;
    return a = Ue(
      l,
      l === sl ? a : 0,
      l.cancelPendingCommit !== null || l.timeoutHandle !== -1
    ), a === 0 ? null : (hy(l, a, t), Cy(l, $l()), l.callbackNode != null && l.callbackNode === u ? qy.bind(null, l) : null);
  }
  function By(l, t) {
    if (_n()) return null;
    hy(l, t, !0);
  }
  function Nm() {
    Vm(function() {
      (I & 6) !== 0 ? $n(
        Ti,
        Rm
      ) : Ny();
    });
  }
  function jc() {
    if (mu === 0) {
      var l = ca;
      l === 0 && (l = Me, Me <<= 1, (Me & 261888) === 0 && (Me = 256)), mu = l;
    }
    return mu;
  }
  function Yy(l) {
    return l == null || typeof l == "symbol" || typeof l == "boolean" ? null : typeof l == "function" ? l : Ce("" + l);
  }
  function Gy(l, t) {
    var u = t.ownerDocument.createElement("input");
    return u.name = t.name, u.value = t.value, l.id && u.setAttribute("form", l.id), t.parentNode.insertBefore(u, t), l = new FormData(l), u.parentNode.removeChild(u), l;
  }
  function Cm(l, t, u, a, e) {
    if (t === "submit" && u && u.stateNode === e) {
      var n = Yy(
        (e[jl] || null).action
      ), f = a.submitter;
      f && (t = (t = f[jl] || null) ? Yy(t.formAction) : f.getAttribute("formAction"), t !== null && (n = t, f = null));
      var c = new Ge(
        "action",
        "action",
        null,
        a,
        e
      );
      l.push({
        event: c,
        listeners: [
          {
            instance: null,
            listener: function() {
              if (a.defaultPrevented) {
                if (mu !== 0) {
                  var i = f ? Gy(e, f) : new FormData(e);
                  fc(
                    u,
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
                typeof n == "function" && (c.preventDefault(), i = f ? Gy(e, f) : new FormData(e), fc(
                  u,
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
  for (var Zc = 0; Zc < Af.length; Zc++) {
    var Lc = Af[Zc], qm = Lc.toLowerCase(), Bm = Lc[0].toUpperCase() + Lc.slice(1);
    gt(
      qm,
      "on" + Bm
    );
  }
  gt(ds, "onAnimationEnd"), gt(hs, "onAnimationIteration"), gt(Ss, "onAnimationStart"), gt("dblclick", "onDoubleClick"), gt("focusin", "onFocus"), gt("focusout", "onBlur"), gt(ko, "onTransitionRun"), gt(Io, "onTransitionStart"), gt(Po, "onTransitionCancel"), gt(gs, "onTransitionEnd"), wu("onMouseEnter", ["mouseout", "mouseover"]), wu("onMouseLeave", ["mouseout", "mouseover"]), wu("onPointerEnter", ["pointerout", "pointerover"]), wu("onPointerLeave", ["pointerout", "pointerover"]), Ou(
    "onChange",
    "change click focusin focusout input keydown keyup selectionchange".split(" ")
  ), Ou(
    "onSelect",
    "focusout contextmenu dragend focusin keydown keyup mousedown mouseup selectionchange".split(
      " "
    )
  ), Ou("onBeforeInput", [
    "compositionend",
    "keypress",
    "textInput",
    "paste"
  ]), Ou(
    "onCompositionEnd",
    "compositionend focusout keydown keypress keyup mousedown".split(" ")
  ), Ou(
    "onCompositionStart",
    "compositionstart focusout keydown keypress keyup mousedown".split(" ")
  ), Ou(
    "onCompositionUpdate",
    "compositionupdate focusout keydown keypress keyup mousedown".split(" ")
  );
  var ve = "abort canplay canplaythrough durationchange emptied encrypted ended error loadeddata loadedmetadata loadstart pause play playing progress ratechange resize seeked seeking stalled suspend timeupdate volumechange waiting".split(
    " "
  ), Ym = new Set(
    "beforetoggle cancel close invalid load scroll scrollend toggle".split(" ").concat(ve)
  );
  function Xy(l, t) {
    t = (t & 4) !== 0;
    for (var u = 0; u < l.length; u++) {
      var a = l[u], e = a.event;
      a = a.listeners;
      l: {
        var n = void 0;
        if (t)
          for (var f = a.length - 1; 0 <= f; f--) {
            var c = a[f], i = c.instance, d = c.currentTarget;
            if (c = c.listener, i !== n && e.isPropagationStopped())
              break l;
            n = c, e.currentTarget = d;
            try {
              n(e);
            } catch (b) {
              je(b);
            }
            e.currentTarget = null, n = i;
          }
        else
          for (f = 0; f < a.length; f++) {
            if (c = a[f], i = c.instance, d = c.currentTarget, c = c.listener, i !== n && e.isPropagationStopped())
              break l;
            n = c, e.currentTarget = d;
            try {
              n(e);
            } catch (b) {
              je(b);
            }
            e.currentTarget = null, n = i;
          }
      }
    }
  }
  function x(l, t) {
    var u = t[lf];
    u === void 0 && (u = t[lf] = /* @__PURE__ */ new Set());
    var a = l + "__bubble";
    u.has(a) || (Qy(t, l, 2, !1), u.add(a));
  }
  function xc(l, t, u) {
    var a = 0;
    t && (a |= 4), Qy(
      u,
      l,
      a,
      t
    );
  }
  var Dn = "_reactListening" + Math.random().toString(36).slice(2);
  function Vc(l) {
    if (!l[Dn]) {
      l[Dn] = !0, Ni.forEach(function(u) {
        u !== "selectionchange" && (Ym.has(u) || xc(u, !1, l), xc(u, !0, l));
      });
      var t = l.nodeType === 9 ? l : l.ownerDocument;
      t === null || t[Dn] || (t[Dn] = !0, xc("selectionchange", !1, t));
    }
  }
  function Qy(l, t, u, a) {
    switch (dv(t)) {
      case 2:
        var e = yd;
        break;
      case 8:
        e = vd;
        break;
      default:
        e = ni;
    }
    u = e.bind(
      null,
      t,
      u,
      l
    ), e = void 0, !yf || t !== "touchstart" && t !== "touchmove" && t !== "wheel" || (e = !0), a ? e !== void 0 ? l.addEventListener(t, u, {
      capture: !0,
      passive: e
    }) : l.addEventListener(t, u, !0) : e !== void 0 ? l.addEventListener(t, u, {
      passive: e
    }) : l.addEventListener(t, u, !1);
  }
  function Kc(l, t, u, a, e) {
    var n = a;
    if ((t & 1) === 0 && (t & 2) === 0 && a !== null)
      l: for (; ; ) {
        if (a === null) return;
        var f = a.tag;
        if (f === 3 || f === 4) {
          var c = a.stateNode.containerInfo;
          if (c === e) break;
          if (f === 4)
            for (f = a.return; f !== null; ) {
              var i = f.tag;
              if ((i === 3 || i === 4) && f.stateNode.containerInfo === e)
                return;
              f = f.return;
            }
          for (; c !== null; ) {
            if (f = Vu(c), f === null) return;
            if (i = f.tag, i === 5 || i === 6 || i === 26 || i === 27) {
              a = n = f;
              continue l;
            }
            c = c.parentNode;
          }
        }
        a = a.return;
      }
    Vi(function() {
      var d = n, b = cf(u), E = [];
      l: {
        var S = bs.get(l);
        if (S !== void 0) {
          var g = Ge, p = l;
          switch (l) {
            case "keypress":
              if (Be(u) === 0) break l;
            case "keydown":
            case "keyup":
              g = Uo;
              break;
            case "focusin":
              p = "focus", g = df;
              break;
            case "focusout":
              p = "blur", g = df;
              break;
            case "beforeblur":
            case "afterblur":
              g = df;
              break;
            case "click":
              if (u.button === 2) break l;
            case "auxclick":
            case "dblclick":
            case "mousedown":
            case "mousemove":
            case "mouseup":
            case "mouseout":
            case "mouseover":
            case "contextmenu":
              g = wi;
              break;
            case "drag":
            case "dragend":
            case "dragenter":
            case "dragexit":
            case "dragleave":
            case "dragover":
            case "dragstart":
            case "drop":
              g = go;
              break;
            case "touchcancel":
            case "touchend":
            case "touchmove":
            case "touchstart":
              g = No;
              break;
            case ds:
            case hs:
            case Ss:
              g = zo;
              break;
            case gs:
              g = qo;
              break;
            case "scroll":
            case "scrollend":
              g = ho;
              break;
            case "wheel":
              g = Yo;
              break;
            case "copy":
            case "cut":
            case "paste":
              g = To;
              break;
            case "gotpointercapture":
            case "lostpointercapture":
            case "pointercancel":
            case "pointerdown":
            case "pointermove":
            case "pointerout":
            case "pointerover":
            case "pointerup":
              g = $i;
              break;
            case "toggle":
            case "beforetoggle":
              g = Xo;
          }
          var C = (t & 4) !== 0, cl = !C && (l === "scroll" || l === "scrollend"), v = C ? S !== null ? S + "Capture" : null : S;
          C = [];
          for (var s = d, m; s !== null; ) {
            var z = s;
            if (m = z.stateNode, z = z.tag, z !== 5 && z !== 26 && z !== 27 || m === null || v === null || (z = Ca(s, v), z != null && C.push(
              oe(s, z, m)
            )), cl) break;
            s = s.return;
          }
          0 < C.length && (S = new g(
            S,
            p,
            null,
            u,
            b
          ), E.push({ event: S, listeners: C }));
        }
      }
      if ((t & 7) === 0) {
        l: {
          if (S = l === "mouseover" || l === "pointerover", g = l === "mouseout" || l === "pointerout", S && u !== ff && (p = u.relatedTarget || u.fromElement) && (Vu(p) || p[xu]))
            break l;
          if ((g || S) && (S = b.window === b ? b : (S = b.ownerDocument) ? S.defaultView || S.parentWindow : window, g ? (p = u.relatedTarget || u.toElement, g = d, p = p ? Vu(p) : null, p !== null && (cl = k(p), C = p.tag, p !== cl || C !== 5 && C !== 27 && C !== 6) && (p = null)) : (g = null, p = d), g !== p)) {
            if (C = wi, z = "onMouseLeave", v = "onMouseEnter", s = "mouse", (l === "pointerout" || l === "pointerover") && (C = $i, z = "onPointerLeave", v = "onPointerEnter", s = "pointer"), cl = g == null ? S : Na(g), m = p == null ? S : Na(p), S = new C(
              z,
              s + "leave",
              g,
              u,
              b
            ), S.target = cl, S.relatedTarget = m, z = null, Vu(b) === d && (C = new C(
              v,
              s + "enter",
              p,
              u,
              b
            ), C.target = m, C.relatedTarget = cl, z = C), cl = z, g && p)
              t: {
                for (C = Gm, v = g, s = p, m = 0, z = v; z; z = C(z))
                  m++;
                z = 0;
                for (var R = s; R; R = C(R))
                  z++;
                for (; 0 < m - z; )
                  v = C(v), m--;
                for (; 0 < z - m; )
                  s = C(s), z--;
                for (; m--; ) {
                  if (v === s || s !== null && v === s.alternate) {
                    C = v;
                    break t;
                  }
                  v = C(v), s = C(s);
                }
                C = null;
              }
            else C = null;
            g !== null && jy(
              E,
              S,
              g,
              C,
              !1
            ), p !== null && cl !== null && jy(
              E,
              cl,
              p,
              C,
              !0
            );
          }
        }
        l: {
          if (S = d ? Na(d) : window, g = S.nodeName && S.nodeName.toLowerCase(), g === "select" || g === "input" && S.type === "file")
            var $ = as;
          else if (ts(S))
            if (es)
              $ = Wo;
            else {
              $ = Jo;
              var H = Ko;
            }
          else
            g = S.nodeName, !g || g.toLowerCase() !== "input" || S.type !== "checkbox" && S.type !== "radio" ? d && nf(d.elementType) && ($ = as) : $ = wo;
          if ($ && ($ = $(l, d))) {
            us(
              E,
              $,
              u,
              b
            );
            break l;
          }
          H && H(l, S, d), l === "focusout" && d && S.type === "number" && d.memoizedProps.value != null && ef(S, "number", S.value);
        }
        switch (H = d ? Na(d) : window, l) {
          case "focusin":
            (ts(H) || H.contentEditable === "true") && (Pu = H, zf = d, Za = null);
            break;
          case "focusout":
            Za = zf = Pu = null;
            break;
          case "mousedown":
            Ef = !0;
            break;
          case "contextmenu":
          case "mouseup":
          case "dragend":
            Ef = !1, os(E, u, b);
            break;
          case "selectionchange":
            if (Fo) break;
          case "keydown":
          case "keyup":
            os(E, u, b);
        }
        var Q;
        if (Sf)
          l: {
            switch (l) {
              case "compositionstart":
                var K = "onCompositionStart";
                break l;
              case "compositionend":
                K = "onCompositionEnd";
                break l;
              case "compositionupdate":
                K = "onCompositionUpdate";
                break l;
            }
            K = void 0;
          }
        else
          Iu ? Pi(l, u) && (K = "onCompositionEnd") : l === "keydown" && u.keyCode === 229 && (K = "onCompositionStart");
        K && (Fi && u.locale !== "ko" && (Iu || K !== "onCompositionStart" ? K === "onCompositionEnd" && Iu && (Q = Ki()) : (kt = b, vf = "value" in kt ? kt.value : kt.textContent, Iu = !0)), H = pn(d, K), 0 < H.length && (K = new Wi(
          K,
          l,
          null,
          u,
          b
        ), E.push({ event: K, listeners: H }), Q ? K.data = Q : (Q = ls(u), Q !== null && (K.data = Q)))), (Q = jo ? Zo(l, u) : Lo(l, u)) && (K = pn(d, "onBeforeInput"), 0 < K.length && (H = new Wi(
          "onBeforeInput",
          "beforeinput",
          null,
          u,
          b
        ), E.push({
          event: H,
          listeners: K
        }), H.data = Q)), Cm(
          E,
          l,
          d,
          u,
          b
        );
      }
      Xy(E, t);
    });
  }
  function oe(l, t, u) {
    return {
      instance: l,
      listener: t,
      currentTarget: u
    };
  }
  function pn(l, t) {
    for (var u = t + "Capture", a = []; l !== null; ) {
      var e = l, n = e.stateNode;
      if (e = e.tag, e !== 5 && e !== 26 && e !== 27 || n === null || (e = Ca(l, u), e != null && a.unshift(
        oe(l, e, n)
      ), e = Ca(l, t), e != null && a.push(
        oe(l, e, n)
      )), l.tag === 3) return a;
      l = l.return;
    }
    return [];
  }
  function Gm(l) {
    if (l === null) return null;
    do
      l = l.return;
    while (l && l.tag !== 5 && l.tag !== 27);
    return l || null;
  }
  function jy(l, t, u, a, e) {
    for (var n = t._reactName, f = []; u !== null && u !== a; ) {
      var c = u, i = c.alternate, d = c.stateNode;
      if (c = c.tag, i !== null && i === a) break;
      c !== 5 && c !== 26 && c !== 27 || d === null || (i = d, e ? (d = Ca(u, n), d != null && f.unshift(
        oe(u, d, i)
      )) : e || (d = Ca(u, n), d != null && f.push(
        oe(u, d, i)
      ))), u = u.return;
    }
    f.length !== 0 && l.push({ event: t, listeners: f });
  }
  var Xm = /\r\n?/g, Qm = /\u0000|\uFFFD/g;
  function Zy(l) {
    return (typeof l == "string" ? l : "" + l).replace(Xm, `
`).replace(Qm, "");
  }
  function Ly(l, t) {
    return t = Zy(t), Zy(l) === t;
  }
  function fl(l, t, u, a, e, n) {
    switch (u) {
      case "children":
        typeof a == "string" ? t === "body" || t === "textarea" && a === "" || $u(l, a) : (typeof a == "number" || typeof a == "bigint") && t !== "body" && $u(l, "" + a);
        break;
      case "className":
        Re(l, "class", a);
        break;
      case "tabIndex":
        Re(l, "tabindex", a);
        break;
      case "dir":
      case "role":
      case "viewBox":
      case "width":
      case "height":
        Re(l, u, a);
        break;
      case "style":
        Li(l, a, n);
        break;
      case "data":
        if (t !== "object") {
          Re(l, "data", a);
          break;
        }
      case "src":
      case "href":
        if (a === "" && (t !== "a" || u !== "href")) {
          l.removeAttribute(u);
          break;
        }
        if (a == null || typeof a == "function" || typeof a == "symbol" || typeof a == "boolean") {
          l.removeAttribute(u);
          break;
        }
        a = Ce("" + a), l.setAttribute(u, a);
        break;
      case "action":
      case "formAction":
        if (typeof a == "function") {
          l.setAttribute(
            u,
            "javascript:throw new Error('A React form was unexpectedly submitted. If you called form.submit() manually, consider using form.requestSubmit() instead. If you\\'re trying to use event.stopPropagation() in a submit event handler, consider also calling event.preventDefault().')"
          );
          break;
        } else
          typeof n == "function" && (u === "formAction" ? (t !== "input" && fl(l, t, "name", e.name, e, null), fl(
            l,
            t,
            "formEncType",
            e.formEncType,
            e,
            null
          ), fl(
            l,
            t,
            "formMethod",
            e.formMethod,
            e,
            null
          ), fl(
            l,
            t,
            "formTarget",
            e.formTarget,
            e,
            null
          )) : (fl(l, t, "encType", e.encType, e, null), fl(l, t, "method", e.method, e, null), fl(l, t, "target", e.target, e, null)));
        if (a == null || typeof a == "symbol" || typeof a == "boolean") {
          l.removeAttribute(u);
          break;
        }
        a = Ce("" + a), l.setAttribute(u, a);
        break;
      case "onClick":
        a != null && (l.onclick = Ht);
        break;
      case "onScroll":
        a != null && x("scroll", l);
        break;
      case "onScrollEnd":
        a != null && x("scrollend", l);
        break;
      case "dangerouslySetInnerHTML":
        if (a != null) {
          if (typeof a != "object" || !("__html" in a))
            throw Error(o(61));
          if (u = a.__html, u != null) {
            if (e.children != null) throw Error(o(60));
            l.innerHTML = u;
          }
        }
        break;
      case "multiple":
        l.multiple = a && typeof a != "function" && typeof a != "symbol";
        break;
      case "muted":
        l.muted = a && typeof a != "function" && typeof a != "symbol";
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
        if (a == null || typeof a == "function" || typeof a == "boolean" || typeof a == "symbol") {
          l.removeAttribute("xlink:href");
          break;
        }
        u = Ce("" + a), l.setAttributeNS(
          "http://www.w3.org/1999/xlink",
          "xlink:href",
          u
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
        a != null && typeof a != "function" && typeof a != "symbol" ? l.setAttribute(u, "" + a) : l.removeAttribute(u);
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
        a && typeof a != "function" && typeof a != "symbol" ? l.setAttribute(u, "") : l.removeAttribute(u);
        break;
      case "capture":
      case "download":
        a === !0 ? l.setAttribute(u, "") : a !== !1 && a != null && typeof a != "function" && typeof a != "symbol" ? l.setAttribute(u, a) : l.removeAttribute(u);
        break;
      case "cols":
      case "rows":
      case "size":
      case "span":
        a != null && typeof a != "function" && typeof a != "symbol" && !isNaN(a) && 1 <= a ? l.setAttribute(u, a) : l.removeAttribute(u);
        break;
      case "rowSpan":
      case "start":
        a == null || typeof a == "function" || typeof a == "symbol" || isNaN(a) ? l.removeAttribute(u) : l.setAttribute(u, a);
        break;
      case "popover":
        x("beforetoggle", l), x("toggle", l), He(l, "popover", a);
        break;
      case "xlinkActuate":
        Ut(
          l,
          "http://www.w3.org/1999/xlink",
          "xlink:actuate",
          a
        );
        break;
      case "xlinkArcrole":
        Ut(
          l,
          "http://www.w3.org/1999/xlink",
          "xlink:arcrole",
          a
        );
        break;
      case "xlinkRole":
        Ut(
          l,
          "http://www.w3.org/1999/xlink",
          "xlink:role",
          a
        );
        break;
      case "xlinkShow":
        Ut(
          l,
          "http://www.w3.org/1999/xlink",
          "xlink:show",
          a
        );
        break;
      case "xlinkTitle":
        Ut(
          l,
          "http://www.w3.org/1999/xlink",
          "xlink:title",
          a
        );
        break;
      case "xlinkType":
        Ut(
          l,
          "http://www.w3.org/1999/xlink",
          "xlink:type",
          a
        );
        break;
      case "xmlBase":
        Ut(
          l,
          "http://www.w3.org/XML/1998/namespace",
          "xml:base",
          a
        );
        break;
      case "xmlLang":
        Ut(
          l,
          "http://www.w3.org/XML/1998/namespace",
          "xml:lang",
          a
        );
        break;
      case "xmlSpace":
        Ut(
          l,
          "http://www.w3.org/XML/1998/namespace",
          "xml:space",
          a
        );
        break;
      case "is":
        He(l, "is", a);
        break;
      case "innerText":
      case "textContent":
        break;
      default:
        (!(2 < u.length) || u[0] !== "o" && u[0] !== "O" || u[1] !== "n" && u[1] !== "N") && (u = oo.get(u) || u, He(l, u, a));
    }
  }
  function Jc(l, t, u, a, e, n) {
    switch (u) {
      case "style":
        Li(l, a, n);
        break;
      case "dangerouslySetInnerHTML":
        if (a != null) {
          if (typeof a != "object" || !("__html" in a))
            throw Error(o(61));
          if (u = a.__html, u != null) {
            if (e.children != null) throw Error(o(60));
            l.innerHTML = u;
          }
        }
        break;
      case "children":
        typeof a == "string" ? $u(l, a) : (typeof a == "number" || typeof a == "bigint") && $u(l, "" + a);
        break;
      case "onScroll":
        a != null && x("scroll", l);
        break;
      case "onScrollEnd":
        a != null && x("scrollend", l);
        break;
      case "onClick":
        a != null && (l.onclick = Ht);
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
        if (!Ci.hasOwnProperty(u))
          l: {
            if (u[0] === "o" && u[1] === "n" && (e = u.endsWith("Capture"), t = u.slice(2, e ? u.length - 7 : void 0), n = l[jl] || null, n = n != null ? n[u] : null, typeof n == "function" && l.removeEventListener(t, n, e), typeof a == "function")) {
              typeof n != "function" && n !== null && (u in l ? l[u] = null : l.hasAttribute(u) && l.removeAttribute(u)), l.addEventListener(t, a, e);
              break l;
            }
            u in l ? l[u] = a : a === !0 ? l.setAttribute(u, "") : He(l, u, a);
          }
    }
  }
  function Hl(l, t, u) {
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
        x("error", l), x("load", l);
        var a = !1, e = !1, n;
        for (n in u)
          if (u.hasOwnProperty(n)) {
            var f = u[n];
            if (f != null)
              switch (n) {
                case "src":
                  a = !0;
                  break;
                case "srcSet":
                  e = !0;
                  break;
                case "children":
                case "dangerouslySetInnerHTML":
                  throw Error(o(137, t));
                default:
                  fl(l, t, n, f, u, null);
              }
          }
        e && fl(l, t, "srcSet", u.srcSet, u, null), a && fl(l, t, "src", u.src, u, null);
        return;
      case "input":
        x("invalid", l);
        var c = n = f = e = null, i = null, d = null;
        for (a in u)
          if (u.hasOwnProperty(a)) {
            var b = u[a];
            if (b != null)
              switch (a) {
                case "name":
                  e = b;
                  break;
                case "type":
                  f = b;
                  break;
                case "checked":
                  i = b;
                  break;
                case "defaultChecked":
                  d = b;
                  break;
                case "value":
                  n = b;
                  break;
                case "defaultValue":
                  c = b;
                  break;
                case "children":
                case "dangerouslySetInnerHTML":
                  if (b != null)
                    throw Error(o(137, t));
                  break;
                default:
                  fl(l, t, a, b, u, null);
              }
          }
        Xi(
          l,
          n,
          c,
          i,
          d,
          f,
          e,
          !1
        );
        return;
      case "select":
        x("invalid", l), a = f = n = null;
        for (e in u)
          if (u.hasOwnProperty(e) && (c = u[e], c != null))
            switch (e) {
              case "value":
                n = c;
                break;
              case "defaultValue":
                f = c;
                break;
              case "multiple":
                a = c;
              default:
                fl(l, t, e, c, u, null);
            }
        t = n, u = f, l.multiple = !!a, t != null ? Wu(l, !!a, t, !1) : u != null && Wu(l, !!a, u, !0);
        return;
      case "textarea":
        x("invalid", l), n = e = a = null;
        for (f in u)
          if (u.hasOwnProperty(f) && (c = u[f], c != null))
            switch (f) {
              case "value":
                a = c;
                break;
              case "defaultValue":
                e = c;
                break;
              case "children":
                n = c;
                break;
              case "dangerouslySetInnerHTML":
                if (c != null) throw Error(o(91));
                break;
              default:
                fl(l, t, f, c, u, null);
            }
        ji(l, a, e, n);
        return;
      case "option":
        for (i in u)
          if (u.hasOwnProperty(i) && (a = u[i], a != null))
            switch (i) {
              case "selected":
                l.selected = a && typeof a != "function" && typeof a != "symbol";
                break;
              default:
                fl(l, t, i, a, u, null);
            }
        return;
      case "dialog":
        x("beforetoggle", l), x("toggle", l), x("cancel", l), x("close", l);
        break;
      case "iframe":
      case "object":
        x("load", l);
        break;
      case "video":
      case "audio":
        for (a = 0; a < ve.length; a++)
          x(ve[a], l);
        break;
      case "image":
        x("error", l), x("load", l);
        break;
      case "details":
        x("toggle", l);
        break;
      case "embed":
      case "source":
      case "link":
        x("error", l), x("load", l);
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
        for (d in u)
          if (u.hasOwnProperty(d) && (a = u[d], a != null))
            switch (d) {
              case "children":
              case "dangerouslySetInnerHTML":
                throw Error(o(137, t));
              default:
                fl(l, t, d, a, u, null);
            }
        return;
      default:
        if (nf(t)) {
          for (b in u)
            u.hasOwnProperty(b) && (a = u[b], a !== void 0 && Jc(
              l,
              t,
              b,
              a,
              u,
              void 0
            ));
          return;
        }
    }
    for (c in u)
      u.hasOwnProperty(c) && (a = u[c], a != null && fl(l, t, c, a, u, null));
  }
  function jm(l, t, u, a) {
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
        var e = null, n = null, f = null, c = null, i = null, d = null, b = null;
        for (g in u) {
          var E = u[g];
          if (u.hasOwnProperty(g) && E != null)
            switch (g) {
              case "checked":
                break;
              case "value":
                break;
              case "defaultValue":
                i = E;
              default:
                a.hasOwnProperty(g) || fl(l, t, g, null, a, E);
            }
        }
        for (var S in a) {
          var g = a[S];
          if (E = u[S], a.hasOwnProperty(S) && (g != null || E != null))
            switch (S) {
              case "type":
                n = g;
                break;
              case "name":
                e = g;
                break;
              case "checked":
                d = g;
                break;
              case "defaultChecked":
                b = g;
                break;
              case "value":
                f = g;
                break;
              case "defaultValue":
                c = g;
                break;
              case "children":
              case "dangerouslySetInnerHTML":
                if (g != null)
                  throw Error(o(137, t));
                break;
              default:
                g !== E && fl(
                  l,
                  t,
                  S,
                  g,
                  a,
                  E
                );
            }
        }
        af(
          l,
          f,
          c,
          i,
          d,
          b,
          n,
          e
        );
        return;
      case "select":
        g = f = c = S = null;
        for (n in u)
          if (i = u[n], u.hasOwnProperty(n) && i != null)
            switch (n) {
              case "value":
                break;
              case "multiple":
                g = i;
              default:
                a.hasOwnProperty(n) || fl(
                  l,
                  t,
                  n,
                  null,
                  a,
                  i
                );
            }
        for (e in a)
          if (n = a[e], i = u[e], a.hasOwnProperty(e) && (n != null || i != null))
            switch (e) {
              case "value":
                S = n;
                break;
              case "defaultValue":
                c = n;
                break;
              case "multiple":
                f = n;
              default:
                n !== i && fl(
                  l,
                  t,
                  e,
                  n,
                  a,
                  i
                );
            }
        t = c, u = f, a = g, S != null ? Wu(l, !!u, S, !1) : !!a != !!u && (t != null ? Wu(l, !!u, t, !0) : Wu(l, !!u, u ? [] : "", !1));
        return;
      case "textarea":
        g = S = null;
        for (c in u)
          if (e = u[c], u.hasOwnProperty(c) && e != null && !a.hasOwnProperty(c))
            switch (c) {
              case "value":
                break;
              case "children":
                break;
              default:
                fl(l, t, c, null, a, e);
            }
        for (f in a)
          if (e = a[f], n = u[f], a.hasOwnProperty(f) && (e != null || n != null))
            switch (f) {
              case "value":
                S = e;
                break;
              case "defaultValue":
                g = e;
                break;
              case "children":
                break;
              case "dangerouslySetInnerHTML":
                if (e != null) throw Error(o(91));
                break;
              default:
                e !== n && fl(l, t, f, e, a, n);
            }
        Qi(l, S, g);
        return;
      case "option":
        for (var p in u)
          if (S = u[p], u.hasOwnProperty(p) && S != null && !a.hasOwnProperty(p))
            switch (p) {
              case "selected":
                l.selected = !1;
                break;
              default:
                fl(
                  l,
                  t,
                  p,
                  null,
                  a,
                  S
                );
            }
        for (i in a)
          if (S = a[i], g = u[i], a.hasOwnProperty(i) && S !== g && (S != null || g != null))
            switch (i) {
              case "selected":
                l.selected = S && typeof S != "function" && typeof S != "symbol";
                break;
              default:
                fl(
                  l,
                  t,
                  i,
                  S,
                  a,
                  g
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
        for (var C in u)
          S = u[C], u.hasOwnProperty(C) && S != null && !a.hasOwnProperty(C) && fl(l, t, C, null, a, S);
        for (d in a)
          if (S = a[d], g = u[d], a.hasOwnProperty(d) && S !== g && (S != null || g != null))
            switch (d) {
              case "children":
              case "dangerouslySetInnerHTML":
                if (S != null)
                  throw Error(o(137, t));
                break;
              default:
                fl(
                  l,
                  t,
                  d,
                  S,
                  a,
                  g
                );
            }
        return;
      default:
        if (nf(t)) {
          for (var cl in u)
            S = u[cl], u.hasOwnProperty(cl) && S !== void 0 && !a.hasOwnProperty(cl) && Jc(
              l,
              t,
              cl,
              void 0,
              a,
              S
            );
          for (b in a)
            S = a[b], g = u[b], !a.hasOwnProperty(b) || S === g || S === void 0 && g === void 0 || Jc(
              l,
              t,
              b,
              S,
              a,
              g
            );
          return;
        }
    }
    for (var v in u)
      S = u[v], u.hasOwnProperty(v) && S != null && !a.hasOwnProperty(v) && fl(l, t, v, null, a, S);
    for (E in a)
      S = a[E], g = u[E], !a.hasOwnProperty(E) || S === g || S == null && g == null || fl(l, t, E, S, a, g);
  }
  function xy(l) {
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
  function Zm() {
    if (typeof performance.getEntriesByType == "function") {
      for (var l = 0, t = 0, u = performance.getEntriesByType("resource"), a = 0; a < u.length; a++) {
        var e = u[a], n = e.transferSize, f = e.initiatorType, c = e.duration;
        if (n && c && xy(f)) {
          for (f = 0, c = e.responseEnd, a += 1; a < u.length; a++) {
            var i = u[a], d = i.startTime;
            if (d > c) break;
            var b = i.transferSize, E = i.initiatorType;
            b && xy(E) && (i = i.responseEnd, f += b * (i < c ? 1 : (c - d) / (i - d)));
          }
          if (--a, t += 8 * (n + f) / (e.duration / 1e3), l++, 10 < l) break;
        }
      }
      if (0 < l) return t / l / 1e6;
    }
    return navigator.connection && (l = navigator.connection.downlink, typeof l == "number") ? l : 5;
  }
  var wc = null, Wc = null;
  function Un(l) {
    return l.nodeType === 9 ? l : l.ownerDocument;
  }
  function Vy(l) {
    switch (l) {
      case "http://www.w3.org/2000/svg":
        return 1;
      case "http://www.w3.org/1998/Math/MathML":
        return 2;
      default:
        return 0;
    }
  }
  function Ky(l, t) {
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
  function $c(l, t) {
    return l === "textarea" || l === "noscript" || typeof t.children == "string" || typeof t.children == "number" || typeof t.children == "bigint" || typeof t.dangerouslySetInnerHTML == "object" && t.dangerouslySetInnerHTML !== null && t.dangerouslySetInnerHTML.__html != null;
  }
  var Fc = null;
  function Lm() {
    var l = window.event;
    return l && l.type === "popstate" ? l === Fc ? !1 : (Fc = l, !0) : (Fc = null, !1);
  }
  var Jy = typeof setTimeout == "function" ? setTimeout : void 0, xm = typeof clearTimeout == "function" ? clearTimeout : void 0, wy = typeof Promise == "function" ? Promise : void 0, Vm = typeof queueMicrotask == "function" ? queueMicrotask : typeof wy < "u" ? function(l) {
    return wy.resolve(null).then(l).catch(Km);
  } : Jy;
  function Km(l) {
    setTimeout(function() {
      throw l;
    });
  }
  function du(l) {
    return l === "head";
  }
  function Wy(l, t) {
    var u = t, a = 0;
    do {
      var e = u.nextSibling;
      if (l.removeChild(u), e && e.nodeType === 8)
        if (u = e.data, u === "/$" || u === "/&") {
          if (a === 0) {
            l.removeChild(e), Ma(t);
            return;
          }
          a--;
        } else if (u === "$" || u === "$?" || u === "$~" || u === "$!" || u === "&")
          a++;
        else if (u === "html")
          me(l.ownerDocument.documentElement);
        else if (u === "head") {
          u = l.ownerDocument.head, me(u);
          for (var n = u.firstChild; n; ) {
            var f = n.nextSibling, c = n.nodeName;
            n[Ra] || c === "SCRIPT" || c === "STYLE" || c === "LINK" && n.rel.toLowerCase() === "stylesheet" || u.removeChild(n), n = f;
          }
        } else
          u === "body" && me(l.ownerDocument.body);
      u = e;
    } while (u);
    Ma(t);
  }
  function $y(l, t) {
    var u = l;
    l = 0;
    do {
      var a = u.nextSibling;
      if (u.nodeType === 1 ? t ? (u._stashedDisplay = u.style.display, u.style.display = "none") : (u.style.display = u._stashedDisplay || "", u.getAttribute("style") === "" && u.removeAttribute("style")) : u.nodeType === 3 && (t ? (u._stashedText = u.nodeValue, u.nodeValue = "") : u.nodeValue = u._stashedText || ""), a && a.nodeType === 8)
        if (u = a.data, u === "/$") {
          if (l === 0) break;
          l--;
        } else
          u !== "$" && u !== "$?" && u !== "$~" && u !== "$!" || l++;
      u = a;
    } while (u);
  }
  function kc(l) {
    var t = l.firstChild;
    for (t && t.nodeType === 10 && (t = t.nextSibling); t; ) {
      var u = t;
      switch (t = t.nextSibling, u.nodeName) {
        case "HTML":
        case "HEAD":
        case "BODY":
          kc(u), tf(u);
          continue;
        case "SCRIPT":
        case "STYLE":
          continue;
        case "LINK":
          if (u.rel.toLowerCase() === "stylesheet") continue;
      }
      l.removeChild(u);
    }
  }
  function Jm(l, t, u, a) {
    for (; l.nodeType === 1; ) {
      var e = u;
      if (l.nodeName.toLowerCase() !== t.toLowerCase()) {
        if (!a && (l.nodeName !== "INPUT" || l.type !== "hidden"))
          break;
      } else if (a) {
        if (!l[Ra])
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
      if (l = dt(l.nextSibling), l === null) break;
    }
    return null;
  }
  function wm(l, t, u) {
    if (t === "") return null;
    for (; l.nodeType !== 3; )
      if ((l.nodeType !== 1 || l.nodeName !== "INPUT" || l.type !== "hidden") && !u || (l = dt(l.nextSibling), l === null)) return null;
    return l;
  }
  function Fy(l, t) {
    for (; l.nodeType !== 8; )
      if ((l.nodeType !== 1 || l.nodeName !== "INPUT" || l.type !== "hidden") && !t || (l = dt(l.nextSibling), l === null)) return null;
    return l;
  }
  function Ic(l) {
    return l.data === "$?" || l.data === "$~";
  }
  function Pc(l) {
    return l.data === "$!" || l.data === "$?" && l.ownerDocument.readyState !== "loading";
  }
  function Wm(l, t) {
    var u = l.ownerDocument;
    if (l.data === "$~") l._reactRetry = t;
    else if (l.data !== "$?" || u.readyState !== "loading")
      t();
    else {
      var a = function() {
        t(), u.removeEventListener("DOMContentLoaded", a);
      };
      u.addEventListener("DOMContentLoaded", a), l._reactRetry = a;
    }
  }
  function dt(l) {
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
  var li = null;
  function ky(l) {
    l = l.nextSibling;
    for (var t = 0; l; ) {
      if (l.nodeType === 8) {
        var u = l.data;
        if (u === "/$" || u === "/&") {
          if (t === 0)
            return dt(l.nextSibling);
          t--;
        } else
          u !== "$" && u !== "$!" && u !== "$?" && u !== "$~" && u !== "&" || t++;
      }
      l = l.nextSibling;
    }
    return null;
  }
  function Iy(l) {
    l = l.previousSibling;
    for (var t = 0; l; ) {
      if (l.nodeType === 8) {
        var u = l.data;
        if (u === "$" || u === "$!" || u === "$?" || u === "$~" || u === "&") {
          if (t === 0) return l;
          t--;
        } else u !== "/$" && u !== "/&" || t++;
      }
      l = l.previousSibling;
    }
    return null;
  }
  function Py(l, t, u) {
    switch (t = Un(u), l) {
      case "html":
        if (l = t.documentElement, !l) throw Error(o(452));
        return l;
      case "head":
        if (l = t.head, !l) throw Error(o(453));
        return l;
      case "body":
        if (l = t.body, !l) throw Error(o(454));
        return l;
      default:
        throw Error(o(451));
    }
  }
  function me(l) {
    for (var t = l.attributes; t.length; )
      l.removeAttributeNode(t[0]);
    tf(l);
  }
  var ht = /* @__PURE__ */ new Map(), lv = /* @__PURE__ */ new Set();
  function Hn(l) {
    return typeof l.getRootNode == "function" ? l.getRootNode() : l.nodeType === 9 ? l : l.ownerDocument;
  }
  var Jt = M.d;
  M.d = {
    f: $m,
    r: Fm,
    D: km,
    C: Im,
    L: Pm,
    m: ld,
    X: ud,
    S: td,
    M: ad
  };
  function $m() {
    var l = Jt.f(), t = En();
    return l || t;
  }
  function Fm(l) {
    var t = Ku(l);
    t !== null && t.tag === 5 && t.type === "form" ? g0(t) : Jt.r(l);
  }
  var Aa = typeof document > "u" ? null : document;
  function tv(l, t, u) {
    var a = Aa;
    if (a && typeof t == "string" && t) {
      var e = ct(t);
      e = 'link[rel="' + l + '"][href="' + e + '"]', typeof u == "string" && (e += '[crossorigin="' + u + '"]'), lv.has(e) || (lv.add(e), l = { rel: l, crossOrigin: u, href: t }, a.querySelector(e) === null && (t = a.createElement("link"), Hl(t, "link", l), _l(t), a.head.appendChild(t)));
    }
  }
  function km(l) {
    Jt.D(l), tv("dns-prefetch", l, null);
  }
  function Im(l, t) {
    Jt.C(l, t), tv("preconnect", l, t);
  }
  function Pm(l, t, u) {
    Jt.L(l, t, u);
    var a = Aa;
    if (a && l && t) {
      var e = 'link[rel="preload"][as="' + ct(t) + '"]';
      t === "image" && u && u.imageSrcSet ? (e += '[imagesrcset="' + ct(
        u.imageSrcSet
      ) + '"]', typeof u.imageSizes == "string" && (e += '[imagesizes="' + ct(
        u.imageSizes
      ) + '"]')) : e += '[href="' + ct(l) + '"]';
      var n = e;
      switch (t) {
        case "style":
          n = _a(l);
          break;
        case "script":
          n = Oa(l);
      }
      ht.has(n) || (l = q(
        {
          rel: "preload",
          href: t === "image" && u && u.imageSrcSet ? void 0 : l,
          as: t
        },
        u
      ), ht.set(n, l), a.querySelector(e) !== null || t === "style" && a.querySelector(de(n)) || t === "script" && a.querySelector(he(n)) || (t = a.createElement("link"), Hl(t, "link", l), _l(t), a.head.appendChild(t)));
    }
  }
  function ld(l, t) {
    Jt.m(l, t);
    var u = Aa;
    if (u && l) {
      var a = t && typeof t.as == "string" ? t.as : "script", e = 'link[rel="modulepreload"][as="' + ct(a) + '"][href="' + ct(l) + '"]', n = e;
      switch (a) {
        case "audioworklet":
        case "paintworklet":
        case "serviceworker":
        case "sharedworker":
        case "worker":
        case "script":
          n = Oa(l);
      }
      if (!ht.has(n) && (l = q({ rel: "modulepreload", href: l }, t), ht.set(n, l), u.querySelector(e) === null)) {
        switch (a) {
          case "audioworklet":
          case "paintworklet":
          case "serviceworker":
          case "sharedworker":
          case "worker":
          case "script":
            if (u.querySelector(he(n)))
              return;
        }
        a = u.createElement("link"), Hl(a, "link", l), _l(a), u.head.appendChild(a);
      }
    }
  }
  function td(l, t, u) {
    Jt.S(l, t, u);
    var a = Aa;
    if (a && l) {
      var e = Ju(a).hoistableStyles, n = _a(l);
      t = t || "default";
      var f = e.get(n);
      if (!f) {
        var c = { loading: 0, preload: null };
        if (f = a.querySelector(
          de(n)
        ))
          c.loading = 5;
        else {
          l = q(
            { rel: "stylesheet", href: l, "data-precedence": t },
            u
          ), (u = ht.get(n)) && ti(l, u);
          var i = f = a.createElement("link");
          _l(i), Hl(i, "link", l), i._p = new Promise(function(d, b) {
            i.onload = d, i.onerror = b;
          }), i.addEventListener("load", function() {
            c.loading |= 1;
          }), i.addEventListener("error", function() {
            c.loading |= 2;
          }), c.loading |= 4, Rn(f, t, a);
        }
        f = {
          type: "stylesheet",
          instance: f,
          count: 1,
          state: c
        }, e.set(n, f);
      }
    }
  }
  function ud(l, t) {
    Jt.X(l, t);
    var u = Aa;
    if (u && l) {
      var a = Ju(u).hoistableScripts, e = Oa(l), n = a.get(e);
      n || (n = u.querySelector(he(e)), n || (l = q({ src: l, async: !0 }, t), (t = ht.get(e)) && ui(l, t), n = u.createElement("script"), _l(n), Hl(n, "link", l), u.head.appendChild(n)), n = {
        type: "script",
        instance: n,
        count: 1,
        state: null
      }, a.set(e, n));
    }
  }
  function ad(l, t) {
    Jt.M(l, t);
    var u = Aa;
    if (u && l) {
      var a = Ju(u).hoistableScripts, e = Oa(l), n = a.get(e);
      n || (n = u.querySelector(he(e)), n || (l = q({ src: l, async: !0, type: "module" }, t), (t = ht.get(e)) && ui(l, t), n = u.createElement("script"), _l(n), Hl(n, "link", l), u.head.appendChild(n)), n = {
        type: "script",
        instance: n,
        count: 1,
        state: null
      }, a.set(e, n));
    }
  }
  function uv(l, t, u, a) {
    var e = (e = Z.current) ? Hn(e) : null;
    if (!e) throw Error(o(446));
    switch (l) {
      case "meta":
      case "title":
        return null;
      case "style":
        return typeof u.precedence == "string" && typeof u.href == "string" ? (t = _a(u.href), u = Ju(
          e
        ).hoistableStyles, a = u.get(t), a || (a = {
          type: "style",
          instance: null,
          count: 0,
          state: null
        }, u.set(t, a)), a) : { type: "void", instance: null, count: 0, state: null };
      case "link":
        if (u.rel === "stylesheet" && typeof u.href == "string" && typeof u.precedence == "string") {
          l = _a(u.href);
          var n = Ju(
            e
          ).hoistableStyles, f = n.get(l);
          if (f || (e = e.ownerDocument || e, f = {
            type: "stylesheet",
            instance: null,
            count: 0,
            state: { loading: 0, preload: null }
          }, n.set(l, f), (n = e.querySelector(
            de(l)
          )) && !n._p && (f.instance = n, f.state.loading = 5), ht.has(l) || (u = {
            rel: "preload",
            as: "style",
            href: u.href,
            crossOrigin: u.crossOrigin,
            integrity: u.integrity,
            media: u.media,
            hrefLang: u.hrefLang,
            referrerPolicy: u.referrerPolicy
          }, ht.set(l, u), n || ed(
            e,
            l,
            u,
            f.state
          ))), t && a === null)
            throw Error(o(528, ""));
          return f;
        }
        if (t && a !== null)
          throw Error(o(529, ""));
        return null;
      case "script":
        return t = u.async, u = u.src, typeof u == "string" && t && typeof t != "function" && typeof t != "symbol" ? (t = Oa(u), u = Ju(
          e
        ).hoistableScripts, a = u.get(t), a || (a = {
          type: "script",
          instance: null,
          count: 0,
          state: null
        }, u.set(t, a)), a) : { type: "void", instance: null, count: 0, state: null };
      default:
        throw Error(o(444, l));
    }
  }
  function _a(l) {
    return 'href="' + ct(l) + '"';
  }
  function de(l) {
    return 'link[rel="stylesheet"][' + l + "]";
  }
  function av(l) {
    return q({}, l, {
      "data-precedence": l.precedence,
      precedence: null
    });
  }
  function ed(l, t, u, a) {
    l.querySelector('link[rel="preload"][as="style"][' + t + "]") ? a.loading = 1 : (t = l.createElement("link"), a.preload = t, t.addEventListener("load", function() {
      return a.loading |= 1;
    }), t.addEventListener("error", function() {
      return a.loading |= 2;
    }), Hl(t, "link", u), _l(t), l.head.appendChild(t));
  }
  function Oa(l) {
    return '[src="' + ct(l) + '"]';
  }
  function he(l) {
    return "script[async]" + l;
  }
  function ev(l, t, u) {
    if (t.count++, t.instance === null)
      switch (t.type) {
        case "style":
          var a = l.querySelector(
            'style[data-href~="' + ct(u.href) + '"]'
          );
          if (a)
            return t.instance = a, _l(a), a;
          var e = q({}, u, {
            "data-href": u.href,
            "data-precedence": u.precedence,
            href: null,
            precedence: null
          });
          return a = (l.ownerDocument || l).createElement(
            "style"
          ), _l(a), Hl(a, "style", e), Rn(a, u.precedence, l), t.instance = a;
        case "stylesheet":
          e = _a(u.href);
          var n = l.querySelector(
            de(e)
          );
          if (n)
            return t.state.loading |= 4, t.instance = n, _l(n), n;
          a = av(u), (e = ht.get(e)) && ti(a, e), n = (l.ownerDocument || l).createElement("link"), _l(n);
          var f = n;
          return f._p = new Promise(function(c, i) {
            f.onload = c, f.onerror = i;
          }), Hl(n, "link", a), t.state.loading |= 4, Rn(n, u.precedence, l), t.instance = n;
        case "script":
          return n = Oa(u.src), (e = l.querySelector(
            he(n)
          )) ? (t.instance = e, _l(e), e) : (a = u, (e = ht.get(n)) && (a = q({}, u), ui(a, e)), l = l.ownerDocument || l, e = l.createElement("script"), _l(e), Hl(e, "link", a), l.head.appendChild(e), t.instance = e);
        case "void":
          return null;
        default:
          throw Error(o(443, t.type));
      }
    else
      t.type === "stylesheet" && (t.state.loading & 4) === 0 && (a = t.instance, t.state.loading |= 4, Rn(a, u.precedence, l));
    return t.instance;
  }
  function Rn(l, t, u) {
    for (var a = u.querySelectorAll(
      'link[rel="stylesheet"][data-precedence],style[data-precedence]'
    ), e = a.length ? a[a.length - 1] : null, n = e, f = 0; f < a.length; f++) {
      var c = a[f];
      if (c.dataset.precedence === t) n = c;
      else if (n !== e) break;
    }
    n ? n.parentNode.insertBefore(l, n.nextSibling) : (t = u.nodeType === 9 ? u.head : u, t.insertBefore(l, t.firstChild));
  }
  function ti(l, t) {
    l.crossOrigin == null && (l.crossOrigin = t.crossOrigin), l.referrerPolicy == null && (l.referrerPolicy = t.referrerPolicy), l.title == null && (l.title = t.title);
  }
  function ui(l, t) {
    l.crossOrigin == null && (l.crossOrigin = t.crossOrigin), l.referrerPolicy == null && (l.referrerPolicy = t.referrerPolicy), l.integrity == null && (l.integrity = t.integrity);
  }
  var Nn = null;
  function nv(l, t, u) {
    if (Nn === null) {
      var a = /* @__PURE__ */ new Map(), e = Nn = /* @__PURE__ */ new Map();
      e.set(u, a);
    } else
      e = Nn, a = e.get(u), a || (a = /* @__PURE__ */ new Map(), e.set(u, a));
    if (a.has(l)) return a;
    for (a.set(l, null), u = u.getElementsByTagName(l), e = 0; e < u.length; e++) {
      var n = u[e];
      if (!(n[Ra] || n[Ml] || l === "link" && n.getAttribute("rel") === "stylesheet") && n.namespaceURI !== "http://www.w3.org/2000/svg") {
        var f = n.getAttribute(t) || "";
        f = l + f;
        var c = a.get(f);
        c ? c.push(n) : a.set(f, [n]);
      }
    }
    return a;
  }
  function fv(l, t, u) {
    l = l.ownerDocument || l, l.head.insertBefore(
      u,
      t === "title" ? l.querySelector("head > title") : null
    );
  }
  function nd(l, t, u) {
    if (u === 1 || t.itemProp != null) return !1;
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
  function cv(l) {
    return !(l.type === "stylesheet" && (l.state.loading & 3) === 0);
  }
  function fd(l, t, u, a) {
    if (u.type === "stylesheet" && (typeof a.media != "string" || matchMedia(a.media).matches !== !1) && (u.state.loading & 4) === 0) {
      if (u.instance === null) {
        var e = _a(a.href), n = t.querySelector(
          de(e)
        );
        if (n) {
          t = n._p, t !== null && typeof t == "object" && typeof t.then == "function" && (l.count++, l = Cn.bind(l), t.then(l, l)), u.state.loading |= 4, u.instance = n, _l(n);
          return;
        }
        n = t.ownerDocument || t, a = av(a), (e = ht.get(e)) && ti(a, e), n = n.createElement("link"), _l(n);
        var f = n;
        f._p = new Promise(function(c, i) {
          f.onload = c, f.onerror = i;
        }), Hl(n, "link", a), u.instance = n;
      }
      l.stylesheets === null && (l.stylesheets = /* @__PURE__ */ new Map()), l.stylesheets.set(u, t), (t = u.state.preload) && (u.state.loading & 3) === 0 && (l.count++, u = Cn.bind(l), t.addEventListener("load", u), t.addEventListener("error", u));
    }
  }
  var ai = 0;
  function cd(l, t) {
    return l.stylesheets && l.count === 0 && Bn(l, l.stylesheets), 0 < l.count || 0 < l.imgCount ? function(u) {
      var a = setTimeout(function() {
        if (l.stylesheets && Bn(l, l.stylesheets), l.unsuspend) {
          var n = l.unsuspend;
          l.unsuspend = null, n();
        }
      }, 6e4 + t);
      0 < l.imgBytes && ai === 0 && (ai = 62500 * Zm());
      var e = setTimeout(
        function() {
          if (l.waitingForImages = !1, l.count === 0 && (l.stylesheets && Bn(l, l.stylesheets), l.unsuspend)) {
            var n = l.unsuspend;
            l.unsuspend = null, n();
          }
        },
        (l.imgBytes > ai ? 50 : 800) + t
      );
      return l.unsuspend = u, function() {
        l.unsuspend = null, clearTimeout(a), clearTimeout(e);
      };
    } : null;
  }
  function Cn() {
    if (this.count--, this.count === 0 && (this.imgCount === 0 || !this.waitingForImages)) {
      if (this.stylesheets) Bn(this, this.stylesheets);
      else if (this.unsuspend) {
        var l = this.unsuspend;
        this.unsuspend = null, l();
      }
    }
  }
  var qn = null;
  function Bn(l, t) {
    l.stylesheets = null, l.unsuspend !== null && (l.count++, qn = /* @__PURE__ */ new Map(), t.forEach(id, l), qn = null, Cn.call(l));
  }
  function id(l, t) {
    if (!(t.state.loading & 4)) {
      var u = qn.get(l);
      if (u) var a = u.get(null);
      else {
        u = /* @__PURE__ */ new Map(), qn.set(l, u);
        for (var e = l.querySelectorAll(
          "link[data-precedence],style[data-precedence]"
        ), n = 0; n < e.length; n++) {
          var f = e[n];
          (f.nodeName === "LINK" || f.getAttribute("media") !== "not all") && (u.set(f.dataset.precedence, f), a = f);
        }
        a && u.set(null, a);
      }
      e = t.instance, f = e.getAttribute("data-precedence"), n = u.get(f) || a, n === a && u.set(null, e), u.set(f, e), this.count++, a = Cn.bind(this), e.addEventListener("load", a), e.addEventListener("error", a), n ? n.parentNode.insertBefore(e, n.nextSibling) : (l = l.nodeType === 9 ? l.head : l, l.insertBefore(e, l.firstChild)), t.state.loading |= 4;
    }
  }
  var Se = {
    $$typeof: Rl,
    Provider: null,
    Consumer: null,
    _currentValue: B,
    _currentValue2: B,
    _threadCount: 0
  };
  function sd(l, t, u, a, e, n, f, c, i) {
    this.tag = 1, this.containerInfo = l, this.pingCache = this.current = this.pendingChildren = null, this.timeoutHandle = -1, this.callbackNode = this.next = this.pendingContext = this.context = this.cancelPendingCommit = null, this.callbackPriority = 0, this.expirationTimes = kn(-1), this.entangledLanes = this.shellSuspendCounter = this.errorRecoveryDisabledLanes = this.expiredLanes = this.warmLanes = this.pingedLanes = this.suspendedLanes = this.pendingLanes = 0, this.entanglements = kn(0), this.hiddenUpdates = kn(null), this.identifierPrefix = a, this.onUncaughtError = e, this.onCaughtError = n, this.onRecoverableError = f, this.pooledCache = null, this.pooledCacheLanes = 0, this.formState = i, this.incompleteTransitions = /* @__PURE__ */ new Map();
  }
  function iv(l, t, u, a, e, n, f, c, i, d, b, E) {
    return l = new sd(
      l,
      t,
      u,
      f,
      i,
      d,
      b,
      E,
      c
    ), t = 1, n === !0 && (t |= 24), n = Pl(3, null, null, t), l.current = n, n.stateNode = l, t = Yf(), t.refCount++, l.pooledCache = t, t.refCount++, n.memoizedState = {
      element: a,
      isDehydrated: u,
      cache: t
    }, jf(n), l;
  }
  function sv(l) {
    return l ? (l = ua, l) : ua;
  }
  function yv(l, t, u, a, e, n) {
    e = sv(e), a.context === null ? a.context = e : a.pendingContext = e, a = au(t), a.payload = { element: u }, n = n === void 0 ? null : n, n !== null && (a.callback = n), u = eu(l, a, t), u !== null && (Jl(u, l, t), Wa(u, l, t));
  }
  function vv(l, t) {
    if (l = l.memoizedState, l !== null && l.dehydrated !== null) {
      var u = l.retryLane;
      l.retryLane = u !== 0 && u < t ? u : t;
    }
  }
  function ei(l, t) {
    vv(l, t), (l = l.alternate) && vv(l, t);
  }
  function ov(l) {
    if (l.tag === 13 || l.tag === 31) {
      var t = Uu(l, 67108864);
      t !== null && Jl(t, l, 67108864), ei(l, 67108864);
    }
  }
  function mv(l) {
    if (l.tag === 13 || l.tag === 31) {
      var t = et();
      t = In(t);
      var u = Uu(l, t);
      u !== null && Jl(u, l, t), ei(l, t);
    }
  }
  var Yn = !0;
  function yd(l, t, u, a) {
    var e = r.T;
    r.T = null;
    var n = M.p;
    try {
      M.p = 2, ni(l, t, u, a);
    } finally {
      M.p = n, r.T = e;
    }
  }
  function vd(l, t, u, a) {
    var e = r.T;
    r.T = null;
    var n = M.p;
    try {
      M.p = 8, ni(l, t, u, a);
    } finally {
      M.p = n, r.T = e;
    }
  }
  function ni(l, t, u, a) {
    if (Yn) {
      var e = fi(a);
      if (e === null)
        Kc(
          l,
          t,
          a,
          Gn,
          u
        ), hv(l, a);
      else if (md(
        e,
        l,
        t,
        u,
        a
      ))
        a.stopPropagation();
      else if (hv(l, a), t & 4 && -1 < od.indexOf(l)) {
        for (; e !== null; ) {
          var n = Ku(e);
          if (n !== null)
            switch (n.tag) {
              case 3:
                if (n = n.stateNode, n.current.memoizedState.isDehydrated) {
                  var f = _u(n.pendingLanes);
                  if (f !== 0) {
                    var c = n;
                    for (c.pendingLanes |= 2, c.entangledLanes |= 2; f; ) {
                      var i = 1 << 31 - kl(f);
                      c.entanglements[1] |= i, f &= ~i;
                    }
                    Mt(n), (I & 6) === 0 && (rn = $l() + 500, ye(0));
                  }
                }
                break;
              case 31:
              case 13:
                c = Uu(n, 2), c !== null && Jl(c, n, 2), En(), ei(n, 2);
            }
          if (n = fi(a), n === null && Kc(
            l,
            t,
            a,
            Gn,
            u
          ), n === e) break;
          e = n;
        }
        e !== null && a.stopPropagation();
      } else
        Kc(
          l,
          t,
          a,
          null,
          u
        );
    }
  }
  function fi(l) {
    return l = cf(l), ci(l);
  }
  var Gn = null;
  function ci(l) {
    if (Gn = null, l = Vu(l), l !== null) {
      var t = k(l);
      if (t === null) l = null;
      else {
        var u = t.tag;
        if (u === 13) {
          if (l = ul(t), l !== null) return l;
          l = null;
        } else if (u === 31) {
          if (l = hl(t), l !== null) return l;
          l = null;
        } else if (u === 3) {
          if (t.stateNode.current.memoizedState.isDehydrated)
            return t.tag === 3 ? t.stateNode.containerInfo : null;
          l = null;
        } else t !== l && (l = null);
      }
    }
    return Gn = l, null;
  }
  function dv(l) {
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
        switch (Fv()) {
          case Ti:
            return 2;
          case Ai:
            return 8;
          case Oe:
          case kv:
            return 32;
          case _i:
            return 268435456;
          default:
            return 32;
        }
      default:
        return 32;
    }
  }
  var ii = !1, hu = null, Su = null, gu = null, ge = /* @__PURE__ */ new Map(), be = /* @__PURE__ */ new Map(), bu = [], od = "mousedown mouseup touchcancel touchend touchstart auxclick dblclick pointercancel pointerdown pointerup dragend dragstart drop compositionend compositionstart keydown keypress keyup input textInput copy cut paste click change contextmenu reset".split(
    " "
  );
  function hv(l, t) {
    switch (l) {
      case "focusin":
      case "focusout":
        hu = null;
        break;
      case "dragenter":
      case "dragleave":
        Su = null;
        break;
      case "mouseover":
      case "mouseout":
        gu = null;
        break;
      case "pointerover":
      case "pointerout":
        ge.delete(t.pointerId);
        break;
      case "gotpointercapture":
      case "lostpointercapture":
        be.delete(t.pointerId);
    }
  }
  function re(l, t, u, a, e, n) {
    return l === null || l.nativeEvent !== n ? (l = {
      blockedOn: t,
      domEventName: u,
      eventSystemFlags: a,
      nativeEvent: n,
      targetContainers: [e]
    }, t !== null && (t = Ku(t), t !== null && ov(t)), l) : (l.eventSystemFlags |= a, t = l.targetContainers, e !== null && t.indexOf(e) === -1 && t.push(e), l);
  }
  function md(l, t, u, a, e) {
    switch (t) {
      case "focusin":
        return hu = re(
          hu,
          l,
          t,
          u,
          a,
          e
        ), !0;
      case "dragenter":
        return Su = re(
          Su,
          l,
          t,
          u,
          a,
          e
        ), !0;
      case "mouseover":
        return gu = re(
          gu,
          l,
          t,
          u,
          a,
          e
        ), !0;
      case "pointerover":
        var n = e.pointerId;
        return ge.set(
          n,
          re(
            ge.get(n) || null,
            l,
            t,
            u,
            a,
            e
          )
        ), !0;
      case "gotpointercapture":
        return n = e.pointerId, be.set(
          n,
          re(
            be.get(n) || null,
            l,
            t,
            u,
            a,
            e
          )
        ), !0;
    }
    return !1;
  }
  function Sv(l) {
    var t = Vu(l.target);
    if (t !== null) {
      var u = k(t);
      if (u !== null) {
        if (t = u.tag, t === 13) {
          if (t = ul(u), t !== null) {
            l.blockedOn = t, Hi(l.priority, function() {
              mv(u);
            });
            return;
          }
        } else if (t === 31) {
          if (t = hl(u), t !== null) {
            l.blockedOn = t, Hi(l.priority, function() {
              mv(u);
            });
            return;
          }
        } else if (t === 3 && u.stateNode.current.memoizedState.isDehydrated) {
          l.blockedOn = u.tag === 3 ? u.stateNode.containerInfo : null;
          return;
        }
      }
    }
    l.blockedOn = null;
  }
  function Xn(l) {
    if (l.blockedOn !== null) return !1;
    for (var t = l.targetContainers; 0 < t.length; ) {
      var u = fi(l.nativeEvent);
      if (u === null) {
        u = l.nativeEvent;
        var a = new u.constructor(
          u.type,
          u
        );
        ff = a, u.target.dispatchEvent(a), ff = null;
      } else
        return t = Ku(u), t !== null && ov(t), l.blockedOn = u, !1;
      t.shift();
    }
    return !0;
  }
  function gv(l, t, u) {
    Xn(l) && u.delete(t);
  }
  function dd() {
    ii = !1, hu !== null && Xn(hu) && (hu = null), Su !== null && Xn(Su) && (Su = null), gu !== null && Xn(gu) && (gu = null), ge.forEach(gv), be.forEach(gv);
  }
  function Qn(l, t) {
    l.blockedOn === t && (l.blockedOn = null, ii || (ii = !0, h.unstable_scheduleCallback(
      h.unstable_NormalPriority,
      dd
    )));
  }
  var jn = null;
  function bv(l) {
    jn !== l && (jn = l, h.unstable_scheduleCallback(
      h.unstable_NormalPriority,
      function() {
        jn === l && (jn = null);
        for (var t = 0; t < l.length; t += 3) {
          var u = l[t], a = l[t + 1], e = l[t + 2];
          if (typeof a != "function") {
            if (ci(a || u) === null)
              continue;
            break;
          }
          var n = Ku(u);
          n !== null && (l.splice(t, 3), t -= 3, fc(
            n,
            {
              pending: !0,
              data: e,
              method: u.method,
              action: a
            },
            a,
            e
          ));
        }
      }
    ));
  }
  function Ma(l) {
    function t(i) {
      return Qn(i, l);
    }
    hu !== null && Qn(hu, l), Su !== null && Qn(Su, l), gu !== null && Qn(gu, l), ge.forEach(t), be.forEach(t);
    for (var u = 0; u < bu.length; u++) {
      var a = bu[u];
      a.blockedOn === l && (a.blockedOn = null);
    }
    for (; 0 < bu.length && (u = bu[0], u.blockedOn === null); )
      Sv(u), u.blockedOn === null && bu.shift();
    if (u = (l.ownerDocument || l).$$reactFormReplay, u != null)
      for (a = 0; a < u.length; a += 3) {
        var e = u[a], n = u[a + 1], f = e[jl] || null;
        if (typeof n == "function")
          f || bv(u);
        else if (f) {
          var c = null;
          if (n && n.hasAttribute("formAction")) {
            if (e = n, f = n[jl] || null)
              c = f.formAction;
            else if (ci(e) !== null) continue;
          } else c = f.action;
          typeof c == "function" ? u[a + 1] = c : (u.splice(a, 3), a -= 3), bv(u);
        }
      }
  }
  function rv() {
    function l(n) {
      n.canIntercept && n.info === "react-transition" && n.intercept({
        handler: function() {
          return new Promise(function(f) {
            return e = f;
          });
        },
        focusReset: "manual",
        scroll: "manual"
      });
    }
    function t() {
      e !== null && (e(), e = null), a || setTimeout(u, 20);
    }
    function u() {
      if (!a && !navigation.transition) {
        var n = navigation.currentEntry;
        n && n.url != null && navigation.navigate(n.url, {
          state: n.getState(),
          info: "react-transition",
          history: "replace"
        });
      }
    }
    if (typeof navigation == "object") {
      var a = !1, e = null;
      return navigation.addEventListener("navigate", l), navigation.addEventListener("navigatesuccess", t), navigation.addEventListener("navigateerror", t), setTimeout(u, 100), function() {
        a = !0, navigation.removeEventListener("navigate", l), navigation.removeEventListener("navigatesuccess", t), navigation.removeEventListener("navigateerror", t), e !== null && (e(), e = null);
      };
    }
  }
  function si(l) {
    this._internalRoot = l;
  }
  Zn.prototype.render = si.prototype.render = function(l) {
    var t = this._internalRoot;
    if (t === null) throw Error(o(409));
    var u = t.current, a = et();
    yv(u, a, l, t, null, null);
  }, Zn.prototype.unmount = si.prototype.unmount = function() {
    var l = this._internalRoot;
    if (l !== null) {
      this._internalRoot = null;
      var t = l.containerInfo;
      yv(l.current, 2, null, l, null, null), En(), t[xu] = null;
    }
  };
  function Zn(l) {
    this._internalRoot = l;
  }
  Zn.prototype.unstable_scheduleHydration = function(l) {
    if (l) {
      var t = Ui();
      l = { blockedOn: null, target: l, priority: t };
      for (var u = 0; u < bu.length && t !== 0 && t < bu[u].priority; u++) ;
      bu.splice(u, 0, l), u === 0 && Sv(l);
    }
  };
  var zv = _.version;
  if (zv !== "19.2.4")
    throw Error(
      o(
        527,
        zv,
        "19.2.4"
      )
    );
  M.findDOMNode = function(l) {
    var t = l._reactInternals;
    if (t === void 0)
      throw typeof l.render == "function" ? Error(o(188)) : (l = Object.keys(l).join(","), Error(o(268, l)));
    return l = A(t), l = l !== null ? W(l) : null, l = l === null ? null : l.stateNode, l;
  };
  var hd = {
    bundleType: 0,
    version: "19.2.4",
    rendererPackageName: "react-dom",
    currentDispatcherRef: r,
    reconcilerVersion: "19.2.4"
  };
  if (typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ < "u") {
    var Ln = __REACT_DEVTOOLS_GLOBAL_HOOK__;
    if (!Ln.isDisabled && Ln.supportsFiber)
      try {
        pa = Ln.inject(
          hd
        ), Fl = Ln;
      } catch {
      }
  }
  return Ee.createRoot = function(l, t) {
    if (!j(l)) throw Error(o(299));
    var u = !1, a = "", e = D0, n = p0, f = U0;
    return t != null && (t.unstable_strictMode === !0 && (u = !0), t.identifierPrefix !== void 0 && (a = t.identifierPrefix), t.onUncaughtError !== void 0 && (e = t.onUncaughtError), t.onCaughtError !== void 0 && (n = t.onCaughtError), t.onRecoverableError !== void 0 && (f = t.onRecoverableError)), t = iv(
      l,
      1,
      !1,
      null,
      null,
      u,
      a,
      null,
      e,
      n,
      f,
      rv
    ), l[xu] = t.current, Vc(l), new si(t);
  }, Ee.hydrateRoot = function(l, t, u) {
    if (!j(l)) throw Error(o(299));
    var a = !1, e = "", n = D0, f = p0, c = U0, i = null;
    return u != null && (u.unstable_strictMode === !0 && (a = !0), u.identifierPrefix !== void 0 && (e = u.identifierPrefix), u.onUncaughtError !== void 0 && (n = u.onUncaughtError), u.onCaughtError !== void 0 && (f = u.onCaughtError), u.onRecoverableError !== void 0 && (c = u.onRecoverableError), u.formState !== void 0 && (i = u.formState)), t = iv(
      l,
      1,
      !0,
      t,
      u ?? null,
      a,
      e,
      i,
      n,
      f,
      c,
      rv
    ), t.context = sv(null), u = t.current, a = et(), a = In(a), e = au(a), e.callback = null, eu(u, e, a), u = a, t.current.lanes = u, Ha(t, u), Mt(t), l[xu] = t.current, Vc(l), new Zn(t);
  }, Ee.version = "19.2.4", Ee;
}
var Hv;
function pd() {
  if (Hv) return mi.exports;
  Hv = 1;
  function h() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(h);
      } catch (_) {
        console.error(_);
      }
  }
  return h(), mi.exports = Dd(), mi.exports;
}
var Ud = pd();
class Lv {
  constructor(_) {
    this._subscribers = /* @__PURE__ */ new Set(), this.getSnapshot = () => this._state, this.subscribeStore = (O) => (this._subscribers.add(O), () => this._subscribers.delete(O)), this._state = { ..._ };
  }
  replaceState(_) {
    this._state = { ..._ }, this._notify();
  }
  applyPatch(_) {
    this._state = { ...this._state, ..._ }, this._notify();
  }
  _notify() {
    for (const _ of this._subscribers)
      _();
  }
}
const Vn = wt.createContext(null), Eu = /* @__PURE__ */ new Map();
let xv = "", Rv = !1;
function Vv() {
  return xv + "/";
}
function Kv(h, _, O, o, j) {
  j !== void 0 && (xv = j), Rv || (Rv = !0, rd(Vv() + "react-api/events"));
  const k = document.getElementById(h);
  if (!k) {
    console.error("[TLReact] Mount point not found:", h);
    return;
  }
  const ul = Gv(_);
  if (!ul) {
    console.error("[TLReact] Component not registered:", _);
    return;
  }
  const hl = new Lv(O);
  Xv(h, (al) => {
    hl.applyPatch(al);
  });
  const A = Ud.createRoot(k);
  Eu.set(h, { root: A, store: hl });
  const W = o ?? "";
  Jv = W;
  const q = () => {
    const al = wt.useSyncExternalStore(hl.subscribeStore, hl.getSnapshot);
    return oi.createElement(
      Vn.Provider,
      { value: { controlId: h, windowName: W, store: hl } },
      oi.createElement(ul, { controlId: h, state: al })
    );
  };
  A.render(oi.createElement(q));
}
function Hd(h, _, O) {
  Kv(h, _, O);
}
function Nv(h) {
  const _ = Eu.get(h);
  _ && (_.root.unmount(), Eu.delete(h));
}
function Rd(h, _) {
  let O = Eu.get(h);
  if (!O) {
    const j = new Lv(_);
    Xv(h, (ul) => {
      j.applyPatch(ul);
    }), Eu.set(h, { root: null, store: j }), O = Eu.get(h);
  }
  return { controlId: h, windowName: Jv, store: O.store };
}
let Jv = "";
function Nd() {
  const h = wt.useContext(Vn);
  if (!h)
    throw new Error("useTLState must be used inside a TLReact-mounted component.");
  return wt.useSyncExternalStore(h.store.subscribeStore, h.store.getSnapshot);
}
function Cd() {
  const h = wt.useContext(Vn);
  if (!h)
    throw new Error("useTLCommand must be used inside a TLReact-mounted component.");
  const _ = h.controlId, O = h.windowName;
  return wt.useCallback(
    async (o, j) => {
      const k = JSON.stringify({
        controlId: _,
        command: o,
        windowName: O,
        arguments: j ?? {}
      });
      try {
        const ul = await fetch(Vv() + "react-api/command", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: k
        });
        ul.ok || console.error("[TLReact] Command failed:", ul.status, await ul.text());
      } catch (ul) {
        console.error("[TLReact] Command error:", ul);
      }
    },
    [_, O]
  );
}
function Qd() {
  const h = Nd(), _ = Cd(), O = wt.useCallback(
    (o) => {
      _("valueChanged", { value: o });
    },
    [_]
  );
  return [h.value, O];
}
function Cv() {
  new MutationObserver((_) => {
    for (const O of _)
      for (const o of O.removedNodes)
        if (o instanceof HTMLElement) {
          const j = o.id;
          j && Eu.has(j) && Nv(j);
          for (const [k] of Eu)
            o.querySelector("#" + CSS.escape(k)) && Nv(k);
        }
  }).observe(document.body, { childList: !0, subtree: !0 });
}
document.readyState === "loading" ? document.addEventListener("DOMContentLoaded", () => {
  Cv();
}) : Cv();
var gi = { exports: {} }, Te = {};
/**
 * @license React
 * react-jsx-runtime.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var qv;
function qd() {
  if (qv) return Te;
  qv = 1;
  var h = Symbol.for("react.transitional.element"), _ = Symbol.for("react.fragment");
  function O(o, j, k) {
    var ul = null;
    if (k !== void 0 && (ul = "" + k), j.key !== void 0 && (ul = "" + j.key), "key" in j) {
      k = {};
      for (var hl in j)
        hl !== "key" && (k[hl] = j[hl]);
    } else k = j;
    return j = k.ref, {
      $$typeof: h,
      type: o,
      key: ul,
      ref: j !== void 0 ? j : null,
      props: k
    };
  }
  return Te.Fragment = _, Te.jsx = O, Te.jsxs = O, Te;
}
var Bv;
function Bd() {
  return Bv || (Bv = 1, gi.exports = qd()), gi.exports;
}
var bi = Bd();
const jd = ({ control: h }) => {
  const _ = h, O = Gv(_.module), o = wt.useMemo(
    () => Rd(_.controlId, _.state),
    [_.controlId]
  );
  return O ? /* @__PURE__ */ bi.jsx(Vn.Provider, { value: o, children: /* @__PURE__ */ bi.jsx(O, { controlId: _.controlId, state: _.state }) }) : /* @__PURE__ */ bi.jsxs("span", { children: [
    "[Component not registered: ",
    _.module,
    "]"
  ] });
};
var Yd = Zv();
const Zd = /* @__PURE__ */ jv(Yd);
window.TLReact = { mount: Kv, mountField: Hd };
export {
  oi as React,
  Zd as ReactDOM,
  jd as TLChild,
  Vn as TLControlContext,
  rd as connect,
  Rd as createChildContext,
  Gv as getComponent,
  Kv as mount,
  Hd as mountField,
  Gd as register,
  Xv as subscribe,
  Nv as unmount,
  Xd as unsubscribe,
  Cd as useTLCommand,
  Qd as useTLFieldValue,
  Nd as useTLState
};
