const Xy = /* @__PURE__ */ new Map();
function Gd(m, _) {
  Xy.set(m, _);
}
function Qy(m) {
  return Xy.get(m);
}
const _e = /* @__PURE__ */ new Map();
let Ee = null, Ay = null, Vn = 0, yi = null;
const rd = 45e3, bd = 15e3;
function Td(m) {
  Ay = m, yi && clearInterval(yi), _y(m), yi = setInterval(() => {
    Vn > 0 && Date.now() - Vn > rd && (console.warn("[TLReact] No heartbeat received, reconnecting SSE."), _y(Ay));
  }, bd);
}
function _y(m) {
  Ee && Ee.close(), Ee = new EventSource(m), Vn = Date.now(), Ee.onmessage = (_) => {
    Vn = Date.now();
    try {
      const O = JSON.parse(_.data);
      Ed(O);
    } catch (O) {
      console.error("[TLReact] Failed to parse SSE event:", O);
    }
  }, Ee.onerror = () => {
    console.warn("[TLReact] SSE connection error, will reconnect automatically.");
  };
}
function jy(m, _) {
  let O = _e.get(m);
  O || (O = /* @__PURE__ */ new Set(), _e.set(m, O)), O.add(_);
}
function Xd(m, _) {
  const O = _e.get(m);
  O && (O.delete(_), O.size === 0 && _e.delete(m));
}
function Zy(m, _) {
  const O = _e.get(m);
  if (O)
    for (const v of O)
      v(_);
}
function Ed(m) {
  if (!Array.isArray(m) || m.length < 2) {
    console.warn("[TLReact] Unexpected SSE event format:", m);
    return;
  }
  const _ = m[0], O = m[1];
  switch (_) {
    case "Heartbeat":
      break;
    case "StateEvent":
      zd(O);
      break;
    case "PatchEvent":
      Ad(O);
      break;
    case "ContentReplacement":
      Tu.contentReplacement(O);
      break;
    case "ElementReplacement":
      Tu.elementReplacement(O);
      break;
    case "PropertyUpdate":
      Tu.propertyUpdate(O);
      break;
    case "CssClassUpdate":
      Tu.cssClassUpdate(O);
      break;
    case "FragmentInsertion":
      Tu.fragmentInsertion(O);
      break;
    case "RangeReplacement":
      Tu.rangeReplacement(O);
      break;
    case "JSSnipplet":
      Tu.jsSnipplet(O);
      break;
    case "FunctionCall":
      Tu.functionCall(O);
      break;
    default:
      console.warn("[TLReact] Unknown SSE event type:", _);
  }
}
function zd(m) {
  const _ = JSON.parse(m.state);
  Zy(m.controlId, _);
}
function Ad(m) {
  const _ = JSON.parse(m.patch);
  Zy(m.controlId, _);
}
const Tu = {
  contentReplacement(m) {
    const _ = document.getElementById(m.elementId);
    _ && (_.innerHTML = m.html);
  },
  elementReplacement(m) {
    const _ = document.getElementById(m.elementId);
    _ && (_.outerHTML = m.html);
  },
  propertyUpdate(m) {
    const _ = document.getElementById(m.elementId);
    if (_)
      for (const O of m.properties)
        _.setAttribute(O.name, O.value);
  },
  cssClassUpdate(m) {
    const _ = document.getElementById(m.elementId);
    _ && (_.className = m.cssClass);
  },
  fragmentInsertion(m) {
    const _ = document.getElementById(m.elementId);
    _ && _.insertAdjacentHTML(m.position, m.html);
  },
  rangeReplacement(m) {
    const _ = document.getElementById(m.startId), O = document.getElementById(m.stopId);
    if (_ && O && _.parentNode) {
      const v = _.parentNode, Y = document.createRange();
      Y.setStartBefore(_), Y.setEndAfter(O), Y.deleteContents();
      const W = document.createElement("template");
      W.innerHTML = m.html, v.insertBefore(W.content, Y.startContainer.childNodes[Y.startOffset] || null);
    }
  },
  jsSnipplet(m) {
    try {
      (0, eval)(m.code);
    } catch (_) {
      console.error("[TLReact] Error executing JS snippet:", _);
    }
  },
  functionCall(m) {
    try {
      const _ = JSON.parse(m.arguments), O = m.functionRef ? window[m.functionRef] : window, v = O == null ? void 0 : O[m.functionName];
      typeof v == "function" ? v.apply(O, _) : console.warn("[TLReact] Function not found:", m.functionRef + "." + m.functionName);
    } catch (_) {
      console.error("[TLReact] Error executing function call:", _);
    }
  }
};
function Ly(m) {
  return m && m.__esModule && Object.prototype.hasOwnProperty.call(m, "default") ? m.default : m;
}
var vi = { exports: {} }, G = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var Oy;
function _d() {
  if (Oy) return G;
  Oy = 1;
  var m = Symbol.for("react.transitional.element"), _ = Symbol.for("react.portal"), O = Symbol.for("react.fragment"), v = Symbol.for("react.strict_mode"), Y = Symbol.for("react.profiler"), W = Symbol.for("react.consumer"), al = Symbol.for("react.context"), hl = Symbol.for("react.forward_ref"), H = Symbol.for("react.suspense"), A = Symbol.for("react.memo"), $ = Symbol.for("react.lazy"), q = Symbol.for("react.activity"), P = Symbol.iterator;
  function wl(o) {
    return o === null || typeof o != "object" ? null : (o = P && o[P] || o["@@iterator"], typeof o == "function" ? o : null);
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
  function Wl(o, z, p) {
    this.props = o, this.context = z, this.refs = Dt, this.updater = p || Yl;
  }
  Wl.prototype.isReactComponent = {}, Wl.prototype.setState = function(o, z) {
    if (typeof o != "object" && typeof o != "function" && o != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, o, z, "setState");
  }, Wl.prototype.forceUpdate = function(o) {
    this.updater.enqueueForceUpdate(this, o, "forceUpdate");
  };
  function Wt() {
  }
  Wt.prototype = Wl.prototype;
  function Nl(o, z, p) {
    this.props = o, this.context = z, this.refs = Dt, this.updater = p || Yl;
  }
  var ft = Nl.prototype = new Wt();
  ft.constructor = Nl, Cl(ft, Wl.prototype), ft.isPureReactComponent = !0;
  var Et = Array.isArray;
  function Gl() {
  }
  var J = { H: null, A: null, T: null, S: null }, Xl = Object.prototype.hasOwnProperty;
  function zt(o, z, p) {
    var U = p.ref;
    return {
      $$typeof: m,
      type: o,
      key: z,
      ref: U !== void 0 ? U : null,
      props: p
    };
  }
  function Lu(o, z) {
    return zt(o.type, z, o.props);
  }
  function At(o) {
    return typeof o == "object" && o !== null && o.$$typeof === m;
  }
  function Ql(o) {
    var z = { "=": "=0", ":": "=2" };
    return "$" + o.replace(/[=:]/g, function(p) {
      return z[p];
    });
  }
  var zu = /\/+/g;
  function Ut(o, z) {
    return typeof o == "object" && o !== null && o.key != null ? Ql("" + o.key) : z.toString(36);
  }
  function gt(o) {
    switch (o.status) {
      case "fulfilled":
        return o.value;
      case "rejected":
        throw o.reason;
      default:
        switch (typeof o.status == "string" ? o.then(Gl, Gl) : (o.status = "pending", o.then(
          function(z) {
            o.status === "pending" && (o.status = "fulfilled", o.value = z);
          },
          function(z) {
            o.status === "pending" && (o.status = "rejected", o.reason = z);
          }
        )), o.status) {
          case "fulfilled":
            return o.value;
          case "rejected":
            throw o.reason;
        }
    }
    throw o;
  }
  function b(o, z, p, U, X) {
    var Z = typeof o;
    (Z === "undefined" || Z === "boolean") && (o = null);
    var ll = !1;
    if (o === null) ll = !0;
    else
      switch (Z) {
        case "bigint":
        case "string":
        case "number":
          ll = !0;
          break;
        case "object":
          switch (o.$$typeof) {
            case m:
            case _:
              ll = !0;
              break;
            case $:
              return ll = o._init, b(
                ll(o._payload),
                z,
                p,
                U,
                X
              );
          }
      }
    if (ll)
      return X = X(o), ll = U === "" ? "." + Ut(o, 0) : U, Et(X) ? (p = "", ll != null && (p = ll.replace(zu, "$&/") + "/"), b(X, z, p, "", function(Da) {
        return Da;
      })) : X != null && (At(X) && (X = Lu(
        X,
        p + (X.key == null || o && o.key === X.key ? "" : ("" + X.key).replace(
          zu,
          "$&/"
        ) + "/") + ll
      )), z.push(X)), 1;
    ll = 0;
    var ql = U === "" ? "." : U + ":";
    if (Et(o))
      for (var Sl = 0; Sl < o.length; Sl++)
        U = o[Sl], Z = ql + Ut(U, Sl), ll += b(
          U,
          z,
          p,
          Z,
          X
        );
    else if (Sl = wl(o), typeof Sl == "function")
      for (o = Sl.call(o), Sl = 0; !(U = o.next()).done; )
        U = U.value, Z = ql + Ut(U, Sl++), ll += b(
          U,
          z,
          p,
          Z,
          X
        );
    else if (Z === "object") {
      if (typeof o.then == "function")
        return b(
          gt(o),
          z,
          p,
          U,
          X
        );
      throw z = String(o), Error(
        "Objects are not valid as a React child (found: " + (z === "[object Object]" ? "object with keys {" + Object.keys(o).join(", ") + "}" : z) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return ll;
  }
  function M(o, z, p) {
    if (o == null) return o;
    var U = [], X = 0;
    return b(o, U, "", "", function(Z) {
      return z.call(p, Z, X++);
    }), U;
  }
  function B(o) {
    if (o._status === -1) {
      var z = o._result;
      z = z(), z.then(
        function(p) {
          (o._status === 0 || o._status === -1) && (o._status = 1, o._result = p);
        },
        function(p) {
          (o._status === 0 || o._status === -1) && (o._status = 2, o._result = p);
        }
      ), o._status === -1 && (o._status = 0, o._result = z);
    }
    if (o._status === 1) return o._result.default;
    throw o._result;
  }
  var el = typeof reportError == "function" ? reportError : function(o) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var z = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof o == "object" && o !== null && typeof o.message == "string" ? String(o.message) : String(o),
        error: o
      });
      if (!window.dispatchEvent(z)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", o);
      return;
    }
    console.error(o);
  }, il = {
    map: M,
    forEach: function(o, z, p) {
      M(
        o,
        function() {
          z.apply(this, arguments);
        },
        p
      );
    },
    count: function(o) {
      var z = 0;
      return M(o, function() {
        z++;
      }), z;
    },
    toArray: function(o) {
      return M(o, function(z) {
        return z;
      }) || [];
    },
    only: function(o) {
      if (!At(o))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return o;
    }
  };
  return G.Activity = q, G.Children = il, G.Component = Wl, G.Fragment = O, G.Profiler = Y, G.PureComponent = Nl, G.StrictMode = v, G.Suspense = H, G.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = J, G.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(o) {
      return J.H.useMemoCache(o);
    }
  }, G.cache = function(o) {
    return function() {
      return o.apply(null, arguments);
    };
  }, G.cacheSignal = function() {
    return null;
  }, G.cloneElement = function(o, z, p) {
    if (o == null)
      throw Error(
        "The argument must be a React element, but you passed " + o + "."
      );
    var U = Cl({}, o.props), X = o.key;
    if (z != null)
      for (Z in z.key !== void 0 && (X = "" + z.key), z)
        !Xl.call(z, Z) || Z === "key" || Z === "__self" || Z === "__source" || Z === "ref" && z.ref === void 0 || (U[Z] = z[Z]);
    var Z = arguments.length - 2;
    if (Z === 1) U.children = p;
    else if (1 < Z) {
      for (var ll = Array(Z), ql = 0; ql < Z; ql++)
        ll[ql] = arguments[ql + 2];
      U.children = ll;
    }
    return zt(o.type, X, U);
  }, G.createContext = function(o) {
    return o = {
      $$typeof: al,
      _currentValue: o,
      _currentValue2: o,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, o.Provider = o, o.Consumer = {
      $$typeof: W,
      _context: o
    }, o;
  }, G.createElement = function(o, z, p) {
    var U, X = {}, Z = null;
    if (z != null)
      for (U in z.key !== void 0 && (Z = "" + z.key), z)
        Xl.call(z, U) && U !== "key" && U !== "__self" && U !== "__source" && (X[U] = z[U]);
    var ll = arguments.length - 2;
    if (ll === 1) X.children = p;
    else if (1 < ll) {
      for (var ql = Array(ll), Sl = 0; Sl < ll; Sl++)
        ql[Sl] = arguments[Sl + 2];
      X.children = ql;
    }
    if (o && o.defaultProps)
      for (U in ll = o.defaultProps, ll)
        X[U] === void 0 && (X[U] = ll[U]);
    return zt(o, Z, X);
  }, G.createRef = function() {
    return { current: null };
  }, G.forwardRef = function(o) {
    return { $$typeof: hl, render: o };
  }, G.isValidElement = At, G.lazy = function(o) {
    return {
      $$typeof: $,
      _payload: { _status: -1, _result: o },
      _init: B
    };
  }, G.memo = function(o, z) {
    return {
      $$typeof: A,
      type: o,
      compare: z === void 0 ? null : z
    };
  }, G.startTransition = function(o) {
    var z = J.T, p = {};
    J.T = p;
    try {
      var U = o(), X = J.S;
      X !== null && X(p, U), typeof U == "object" && U !== null && typeof U.then == "function" && U.then(Gl, el);
    } catch (Z) {
      el(Z);
    } finally {
      z !== null && p.types !== null && (z.types = p.types), J.T = z;
    }
  }, G.unstable_useCacheRefresh = function() {
    return J.H.useCacheRefresh();
  }, G.use = function(o) {
    return J.H.use(o);
  }, G.useActionState = function(o, z, p) {
    return J.H.useActionState(o, z, p);
  }, G.useCallback = function(o, z) {
    return J.H.useCallback(o, z);
  }, G.useContext = function(o) {
    return J.H.useContext(o);
  }, G.useDebugValue = function() {
  }, G.useDeferredValue = function(o, z) {
    return J.H.useDeferredValue(o, z);
  }, G.useEffect = function(o, z) {
    return J.H.useEffect(o, z);
  }, G.useEffectEvent = function(o) {
    return J.H.useEffectEvent(o);
  }, G.useId = function() {
    return J.H.useId();
  }, G.useImperativeHandle = function(o, z, p) {
    return J.H.useImperativeHandle(o, z, p);
  }, G.useInsertionEffect = function(o, z) {
    return J.H.useInsertionEffect(o, z);
  }, G.useLayoutEffect = function(o, z) {
    return J.H.useLayoutEffect(o, z);
  }, G.useMemo = function(o, z) {
    return J.H.useMemo(o, z);
  }, G.useOptimistic = function(o, z) {
    return J.H.useOptimistic(o, z);
  }, G.useReducer = function(o, z, p) {
    return J.H.useReducer(o, z, p);
  }, G.useRef = function(o) {
    return J.H.useRef(o);
  }, G.useState = function(o) {
    return J.H.useState(o);
  }, G.useSyncExternalStore = function(o, z, p) {
    return J.H.useSyncExternalStore(
      o,
      z,
      p
    );
  }, G.useTransition = function() {
    return J.H.useTransition();
  }, G.version = "19.2.4", G;
}
var My;
function Ti() {
  return My || (My = 1, vi.exports = _d()), vi.exports;
}
var nt = Ti();
const mi = /* @__PURE__ */ Ly(nt);
var di = { exports: {} }, ze = {}, hi = { exports: {} }, Si = {};
/**
 * @license React
 * scheduler.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var py;
function Od() {
  return py || (py = 1, (function(m) {
    function _(b, M) {
      var B = b.length;
      b.push(M);
      l: for (; 0 < B; ) {
        var el = B - 1 >>> 1, il = b[el];
        if (0 < Y(il, M))
          b[el] = M, b[B] = il, B = el;
        else break l;
      }
    }
    function O(b) {
      return b.length === 0 ? null : b[0];
    }
    function v(b) {
      if (b.length === 0) return null;
      var M = b[0], B = b.pop();
      if (B !== M) {
        b[0] = B;
        l: for (var el = 0, il = b.length, o = il >>> 1; el < o; ) {
          var z = 2 * (el + 1) - 1, p = b[z], U = z + 1, X = b[U];
          if (0 > Y(p, B))
            U < il && 0 > Y(X, p) ? (b[el] = X, b[U] = B, el = U) : (b[el] = p, b[z] = B, el = z);
          else if (U < il && 0 > Y(X, B))
            b[el] = X, b[U] = B, el = U;
          else break l;
        }
      }
      return M;
    }
    function Y(b, M) {
      var B = b.sortIndex - M.sortIndex;
      return B !== 0 ? B : b.id - M.id;
    }
    if (m.unstable_now = void 0, typeof performance == "object" && typeof performance.now == "function") {
      var W = performance;
      m.unstable_now = function() {
        return W.now();
      };
    } else {
      var al = Date, hl = al.now();
      m.unstable_now = function() {
        return al.now() - hl;
      };
    }
    var H = [], A = [], $ = 1, q = null, P = 3, wl = !1, Yl = !1, Cl = !1, Dt = !1, Wl = typeof setTimeout == "function" ? setTimeout : null, Wt = typeof clearTimeout == "function" ? clearTimeout : null, Nl = typeof setImmediate < "u" ? setImmediate : null;
    function ft(b) {
      for (var M = O(A); M !== null; ) {
        if (M.callback === null) v(A);
        else if (M.startTime <= b)
          v(A), M.sortIndex = M.expirationTime, _(H, M);
        else break;
        M = O(A);
      }
    }
    function Et(b) {
      if (Cl = !1, ft(b), !Yl)
        if (O(H) !== null)
          Yl = !0, Gl || (Gl = !0, Ql());
        else {
          var M = O(A);
          M !== null && gt(Et, M.startTime - b);
        }
    }
    var Gl = !1, J = -1, Xl = 5, zt = -1;
    function Lu() {
      return Dt ? !0 : !(m.unstable_now() - zt < Xl);
    }
    function At() {
      if (Dt = !1, Gl) {
        var b = m.unstable_now();
        zt = b;
        var M = !0;
        try {
          l: {
            Yl = !1, Cl && (Cl = !1, Wt(J), J = -1), wl = !0;
            var B = P;
            try {
              t: {
                for (ft(b), q = O(H); q !== null && !(q.expirationTime > b && Lu()); ) {
                  var el = q.callback;
                  if (typeof el == "function") {
                    q.callback = null, P = q.priorityLevel;
                    var il = el(
                      q.expirationTime <= b
                    );
                    if (b = m.unstable_now(), typeof il == "function") {
                      q.callback = il, ft(b), M = !0;
                      break t;
                    }
                    q === O(H) && v(H), ft(b);
                  } else v(H);
                  q = O(H);
                }
                if (q !== null) M = !0;
                else {
                  var o = O(A);
                  o !== null && gt(
                    Et,
                    o.startTime - b
                  ), M = !1;
                }
              }
              break l;
            } finally {
              q = null, P = B, wl = !1;
            }
            M = void 0;
          }
        } finally {
          M ? Ql() : Gl = !1;
        }
      }
    }
    var Ql;
    if (typeof Nl == "function")
      Ql = function() {
        Nl(At);
      };
    else if (typeof MessageChannel < "u") {
      var zu = new MessageChannel(), Ut = zu.port2;
      zu.port1.onmessage = At, Ql = function() {
        Ut.postMessage(null);
      };
    } else
      Ql = function() {
        Wl(At, 0);
      };
    function gt(b, M) {
      J = Wl(function() {
        b(m.unstable_now());
      }, M);
    }
    m.unstable_IdlePriority = 5, m.unstable_ImmediatePriority = 1, m.unstable_LowPriority = 4, m.unstable_NormalPriority = 3, m.unstable_Profiling = null, m.unstable_UserBlockingPriority = 2, m.unstable_cancelCallback = function(b) {
      b.callback = null;
    }, m.unstable_forceFrameRate = function(b) {
      0 > b || 125 < b ? console.error(
        "forceFrameRate takes a positive int between 0 and 125, forcing frame rates higher than 125 fps is not supported"
      ) : Xl = 0 < b ? Math.floor(1e3 / b) : 5;
    }, m.unstable_getCurrentPriorityLevel = function() {
      return P;
    }, m.unstable_next = function(b) {
      switch (P) {
        case 1:
        case 2:
        case 3:
          var M = 3;
          break;
        default:
          M = P;
      }
      var B = P;
      P = M;
      try {
        return b();
      } finally {
        P = B;
      }
    }, m.unstable_requestPaint = function() {
      Dt = !0;
    }, m.unstable_runWithPriority = function(b, M) {
      switch (b) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
          break;
        default:
          b = 3;
      }
      var B = P;
      P = b;
      try {
        return M();
      } finally {
        P = B;
      }
    }, m.unstable_scheduleCallback = function(b, M, B) {
      var el = m.unstable_now();
      switch (typeof B == "object" && B !== null ? (B = B.delay, B = typeof B == "number" && 0 < B ? el + B : el) : B = el, b) {
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
      return il = B + il, b = {
        id: $++,
        callback: M,
        priorityLevel: b,
        startTime: B,
        expirationTime: il,
        sortIndex: -1
      }, B > el ? (b.sortIndex = B, _(A, b), O(H) === null && b === O(A) && (Cl ? (Wt(J), J = -1) : Cl = !0, gt(Et, B - el))) : (b.sortIndex = il, _(H, b), Yl || wl || (Yl = !0, Gl || (Gl = !0, Ql()))), b;
    }, m.unstable_shouldYield = Lu, m.unstable_wrapCallback = function(b) {
      var M = P;
      return function() {
        var B = P;
        P = M;
        try {
          return b.apply(this, arguments);
        } finally {
          P = B;
        }
      };
    };
  })(Si)), Si;
}
var Dy;
function Md() {
  return Dy || (Dy = 1, hi.exports = Od()), hi.exports;
}
var gi = { exports: {} }, Hl = {};
/**
 * @license React
 * react-dom.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var Uy;
function pd() {
  if (Uy) return Hl;
  Uy = 1;
  var m = Ti();
  function _(H) {
    var A = "https://react.dev/errors/" + H;
    if (1 < arguments.length) {
      A += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var $ = 2; $ < arguments.length; $++)
        A += "&args[]=" + encodeURIComponent(arguments[$]);
    }
    return "Minified React error #" + H + "; visit " + A + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function O() {
  }
  var v = {
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
  }, Y = Symbol.for("react.portal");
  function W(H, A, $) {
    var q = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: Y,
      key: q == null ? null : "" + q,
      children: H,
      containerInfo: A,
      implementation: $
    };
  }
  var al = m.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function hl(H, A) {
    if (H === "font") return "";
    if (typeof A == "string")
      return A === "use-credentials" ? A : "";
  }
  return Hl.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = v, Hl.createPortal = function(H, A) {
    var $ = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!A || A.nodeType !== 1 && A.nodeType !== 9 && A.nodeType !== 11)
      throw Error(_(299));
    return W(H, A, null, $);
  }, Hl.flushSync = function(H) {
    var A = al.T, $ = v.p;
    try {
      if (al.T = null, v.p = 2, H) return H();
    } finally {
      al.T = A, v.p = $, v.d.f();
    }
  }, Hl.preconnect = function(H, A) {
    typeof H == "string" && (A ? (A = A.crossOrigin, A = typeof A == "string" ? A === "use-credentials" ? A : "" : void 0) : A = null, v.d.C(H, A));
  }, Hl.prefetchDNS = function(H) {
    typeof H == "string" && v.d.D(H);
  }, Hl.preinit = function(H, A) {
    if (typeof H == "string" && A && typeof A.as == "string") {
      var $ = A.as, q = hl($, A.crossOrigin), P = typeof A.integrity == "string" ? A.integrity : void 0, wl = typeof A.fetchPriority == "string" ? A.fetchPriority : void 0;
      $ === "style" ? v.d.S(
        H,
        typeof A.precedence == "string" ? A.precedence : void 0,
        {
          crossOrigin: q,
          integrity: P,
          fetchPriority: wl
        }
      ) : $ === "script" && v.d.X(H, {
        crossOrigin: q,
        integrity: P,
        fetchPriority: wl,
        nonce: typeof A.nonce == "string" ? A.nonce : void 0
      });
    }
  }, Hl.preinitModule = function(H, A) {
    if (typeof H == "string")
      if (typeof A == "object" && A !== null) {
        if (A.as == null || A.as === "script") {
          var $ = hl(
            A.as,
            A.crossOrigin
          );
          v.d.M(H, {
            crossOrigin: $,
            integrity: typeof A.integrity == "string" ? A.integrity : void 0,
            nonce: typeof A.nonce == "string" ? A.nonce : void 0
          });
        }
      } else A == null && v.d.M(H);
  }, Hl.preload = function(H, A) {
    if (typeof H == "string" && typeof A == "object" && A !== null && typeof A.as == "string") {
      var $ = A.as, q = hl($, A.crossOrigin);
      v.d.L(H, $, {
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
  }, Hl.preloadModule = function(H, A) {
    if (typeof H == "string")
      if (A) {
        var $ = hl(A.as, A.crossOrigin);
        v.d.m(H, {
          as: typeof A.as == "string" && A.as !== "script" ? A.as : void 0,
          crossOrigin: $,
          integrity: typeof A.integrity == "string" ? A.integrity : void 0
        });
      } else v.d.m(H);
  }, Hl.requestFormReset = function(H) {
    v.d.r(H);
  }, Hl.unstable_batchedUpdates = function(H, A) {
    return H(A);
  }, Hl.useFormState = function(H, A, $) {
    return al.H.useFormState(H, A, $);
  }, Hl.useFormStatus = function() {
    return al.H.useHostTransitionStatus();
  }, Hl.version = "19.2.4", Hl;
}
var Ry;
function xy() {
  if (Ry) return gi.exports;
  Ry = 1;
  function m() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(m);
      } catch (_) {
        console.error(_);
      }
  }
  return m(), gi.exports = pd(), gi.exports;
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
var Ny;
function Dd() {
  if (Ny) return ze;
  Ny = 1;
  var m = Md(), _ = Ti(), O = xy();
  function v(l) {
    var t = "https://react.dev/errors/" + l;
    if (1 < arguments.length) {
      t += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var u = 2; u < arguments.length; u++)
        t += "&args[]=" + encodeURIComponent(arguments[u]);
    }
    return "Minified React error #" + l + "; visit " + t + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function Y(l) {
    return !(!l || l.nodeType !== 1 && l.nodeType !== 9 && l.nodeType !== 11);
  }
  function W(l) {
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
  function al(l) {
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
  function H(l) {
    if (W(l) !== l)
      throw Error(v(188));
  }
  function A(l) {
    var t = l.alternate;
    if (!t) {
      if (t = W(l), t === null) throw Error(v(188));
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
          if (n === u) return H(e), l;
          if (n === a) return H(e), t;
          n = n.sibling;
        }
        throw Error(v(188));
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
          if (!f) throw Error(v(189));
        }
      }
      if (u.alternate !== a) throw Error(v(190));
    }
    if (u.tag !== 3) throw Error(v(188));
    return u.stateNode.current === u ? l : t;
  }
  function $(l) {
    var t = l.tag;
    if (t === 5 || t === 26 || t === 27 || t === 6) return l;
    for (l = l.child; l !== null; ) {
      if (t = $(l), t !== null) return t;
      l = l.sibling;
    }
    return null;
  }
  var q = Object.assign, P = Symbol.for("react.element"), wl = Symbol.for("react.transitional.element"), Yl = Symbol.for("react.portal"), Cl = Symbol.for("react.fragment"), Dt = Symbol.for("react.strict_mode"), Wl = Symbol.for("react.profiler"), Wt = Symbol.for("react.consumer"), Nl = Symbol.for("react.context"), ft = Symbol.for("react.forward_ref"), Et = Symbol.for("react.suspense"), Gl = Symbol.for("react.suspense_list"), J = Symbol.for("react.memo"), Xl = Symbol.for("react.lazy"), zt = Symbol.for("react.activity"), Lu = Symbol.for("react.memo_cache_sentinel"), At = Symbol.iterator;
  function Ql(l) {
    return l === null || typeof l != "object" ? null : (l = At && l[At] || l["@@iterator"], typeof l == "function" ? l : null);
  }
  var zu = Symbol.for("react.client.reference");
  function Ut(l) {
    if (l == null) return null;
    if (typeof l == "function")
      return l.$$typeof === zu ? null : l.displayName || l.name || null;
    if (typeof l == "string") return l;
    switch (l) {
      case Cl:
        return "Fragment";
      case Wl:
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
        case Yl:
          return "Portal";
        case Nl:
          return l.displayName || "Context";
        case Wt:
          return (l._context.displayName || "Context") + ".Consumer";
        case ft:
          var t = l.render;
          return l = l.displayName, l || (l = t.displayName || t.name || "", l = l !== "" ? "ForwardRef(" + l + ")" : "ForwardRef"), l;
        case J:
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
  var gt = Array.isArray, b = _.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE, M = O.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE, B = {
    pending: !1,
    data: null,
    method: null,
    action: null
  }, el = [], il = -1;
  function o(l) {
    return { current: l };
  }
  function z(l) {
    0 > il || (l.current = el[il], el[il] = null, il--);
  }
  function p(l, t) {
    il++, el[il] = l.current, l.current = t;
  }
  var U = o(null), X = o(null), Z = o(null), ll = o(null);
  function ql(l, t) {
    switch (p(Z, t), p(X, l), p(U, null), t.nodeType) {
      case 9:
      case 11:
        l = (l = t.documentElement) && (l = l.namespaceURI) ? Jo(l) : 0;
        break;
      default:
        if (l = t.tagName, t = t.namespaceURI)
          t = Jo(t), l = wo(t, l);
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
    z(U), p(U, l);
  }
  function Sl() {
    z(U), z(X), z(Z);
  }
  function Da(l) {
    l.memoizedState !== null && p(ll, l);
    var t = U.current, u = wo(t, l.type);
    t !== u && (p(X, l), p(U, u));
  }
  function Oe(l) {
    X.current === l && (z(U), z(X)), ll.current === l && (z(ll), ge._currentValue = B);
  }
  var Jn, Ei;
  function Au(l) {
    if (Jn === void 0)
      try {
        throw Error();
      } catch (u) {
        var t = u.stack.trim().match(/\n( *(at )?)/);
        Jn = t && t[1] || "", Ei = -1 < u.stack.indexOf(`
    at`) ? " (<anonymous>)" : -1 < u.stack.indexOf("@") ? "@unknown:0:0" : "";
      }
    return `
` + Jn + l + Ei;
  }
  var wn = !1;
  function Wn(l, t) {
    if (!l || wn) return "";
    wn = !0;
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
`), h = c.split(`
`);
        for (e = a = 0; a < i.length && !i[a].includes("DetermineComponentFrameRoot"); )
          a++;
        for (; e < h.length && !h[e].includes(
          "DetermineComponentFrameRoot"
        ); )
          e++;
        if (a === i.length || e === h.length)
          for (a = i.length - 1, e = h.length - 1; 1 <= a && 0 <= e && i[a] !== h[e]; )
            e--;
        for (; 1 <= a && 0 <= e; a--, e--)
          if (i[a] !== h[e]) {
            if (a !== 1 || e !== 1)
              do
                if (a--, e--, 0 > e || i[a] !== h[e]) {
                  var r = `
` + i[a].replace(" at new ", " at ");
                  return l.displayName && r.includes("<anonymous>") && (r = r.replace("<anonymous>", l.displayName)), r;
                }
              while (1 <= a && 0 <= e);
            break;
          }
      }
    } finally {
      wn = !1, Error.prepareStackTrace = u;
    }
    return (u = l ? l.displayName || l.name : "") ? Au(u) : "";
  }
  function $y(l, t) {
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
        return Wn(l.type, !1);
      case 11:
        return Wn(l.type.render, !1);
      case 1:
        return Wn(l.type, !0);
      case 31:
        return Au("Activity");
      default:
        return "";
    }
  }
  function zi(l) {
    try {
      var t = "", u = null;
      do
        t += $y(l, u), u = l, l = l.return;
      while (l);
      return t;
    } catch (a) {
      return `
Error generating stack: ` + a.message + `
` + a.stack;
    }
  }
  var $n = Object.prototype.hasOwnProperty, Fn = m.unstable_scheduleCallback, kn = m.unstable_cancelCallback, Fy = m.unstable_shouldYield, ky = m.unstable_requestPaint, $l = m.unstable_now, Iy = m.unstable_getCurrentPriorityLevel, Ai = m.unstable_ImmediatePriority, _i = m.unstable_UserBlockingPriority, Me = m.unstable_NormalPriority, Py = m.unstable_LowPriority, Oi = m.unstable_IdlePriority, lv = m.log, tv = m.unstable_setDisableYieldValue, Ua = null, Fl = null;
  function $t(l) {
    if (typeof lv == "function" && tv(l), Fl && typeof Fl.setStrictMode == "function")
      try {
        Fl.setStrictMode(Ua, l);
      } catch {
      }
  }
  var kl = Math.clz32 ? Math.clz32 : ev, uv = Math.log, av = Math.LN2;
  function ev(l) {
    return l >>>= 0, l === 0 ? 32 : 31 - (uv(l) / av | 0) | 0;
  }
  var pe = 256, De = 262144, Ue = 4194304;
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
  function Re(l, t, u) {
    var a = l.pendingLanes;
    if (a === 0) return 0;
    var e = 0, n = l.suspendedLanes, f = l.pingedLanes;
    l = l.warmLanes;
    var c = a & 134217727;
    return c !== 0 ? (a = c & ~n, a !== 0 ? e = _u(a) : (f &= c, f !== 0 ? e = _u(f) : u || (u = c & ~l, u !== 0 && (e = _u(u))))) : (c = a & ~n, c !== 0 ? e = _u(c) : f !== 0 ? e = _u(f) : u || (u = a & ~l, u !== 0 && (e = _u(u)))), e === 0 ? 0 : t !== 0 && t !== e && (t & n) === 0 && (n = e & -e, u = t & -t, n >= u || n === 32 && (u & 4194048) !== 0) ? t : e;
  }
  function Ra(l, t) {
    return (l.pendingLanes & ~(l.suspendedLanes & ~l.pingedLanes) & t) === 0;
  }
  function nv(l, t) {
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
  function Mi() {
    var l = Ue;
    return Ue <<= 1, (Ue & 62914560) === 0 && (Ue = 4194304), l;
  }
  function In(l) {
    for (var t = [], u = 0; 31 > u; u++) t.push(l);
    return t;
  }
  function Na(l, t) {
    l.pendingLanes |= t, t !== 268435456 && (l.suspendedLanes = 0, l.pingedLanes = 0, l.warmLanes = 0);
  }
  function fv(l, t, u, a, e, n) {
    var f = l.pendingLanes;
    l.pendingLanes = u, l.suspendedLanes = 0, l.pingedLanes = 0, l.warmLanes = 0, l.expiredLanes &= u, l.entangledLanes &= u, l.errorRecoveryDisabledLanes &= u, l.shellSuspendCounter = 0;
    var c = l.entanglements, i = l.expirationTimes, h = l.hiddenUpdates;
    for (u = f & ~u; 0 < u; ) {
      var r = 31 - kl(u), E = 1 << r;
      c[r] = 0, i[r] = -1;
      var S = h[r];
      if (S !== null)
        for (h[r] = null, r = 0; r < S.length; r++) {
          var g = S[r];
          g !== null && (g.lane &= -536870913);
        }
      u &= ~E;
    }
    a !== 0 && pi(l, a, 0), n !== 0 && e === 0 && l.tag !== 0 && (l.suspendedLanes |= n & ~(f & ~t));
  }
  function pi(l, t, u) {
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
  function Ui(l, t) {
    var u = t & -t;
    return u = (u & 42) !== 0 ? 1 : Pn(u), (u & (l.suspendedLanes | t)) !== 0 ? 0 : u;
  }
  function Pn(l) {
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
  function lf(l) {
    return l &= -l, 2 < l ? 8 < l ? (l & 134217727) !== 0 ? 32 : 268435456 : 8 : 2;
  }
  function Ri() {
    var l = M.p;
    return l !== 0 ? l : (l = window.event, l === void 0 ? 32 : Sy(l.type));
  }
  function Ni(l, t) {
    var u = M.p;
    try {
      return M.p = l, t();
    } finally {
      M.p = u;
    }
  }
  var Ft = Math.random().toString(36).slice(2), Ml = "__reactFiber$" + Ft, jl = "__reactProps$" + Ft, xu = "__reactContainer$" + Ft, tf = "__reactEvents$" + Ft, cv = "__reactListeners$" + Ft, iv = "__reactHandles$" + Ft, Hi = "__reactResources$" + Ft, Ha = "__reactMarker$" + Ft;
  function uf(l) {
    delete l[Ml], delete l[jl], delete l[tf], delete l[cv], delete l[iv];
  }
  function Vu(l) {
    var t = l[Ml];
    if (t) return t;
    for (var u = l.parentNode; u; ) {
      if (t = u[xu] || u[Ml]) {
        if (u = t.alternate, t.child !== null || u !== null && u.child !== null)
          for (l = ly(l); l !== null; ) {
            if (u = l[Ml]) return u;
            l = ly(l);
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
  function Ca(l) {
    var t = l.tag;
    if (t === 5 || t === 26 || t === 27 || t === 6) return l.stateNode;
    throw Error(v(33));
  }
  function Ju(l) {
    var t = l[Hi];
    return t || (t = l[Hi] = { hoistableStyles: /* @__PURE__ */ new Map(), hoistableScripts: /* @__PURE__ */ new Map() }), t;
  }
  function _l(l) {
    l[Ha] = !0;
  }
  var Ci = /* @__PURE__ */ new Set(), qi = {};
  function Ou(l, t) {
    wu(l, t), wu(l + "Capture", t);
  }
  function wu(l, t) {
    for (qi[l] = t, l = 0; l < t.length; l++)
      Ci.add(t[l]);
  }
  var sv = RegExp(
    "^[:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD][:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD\\-.0-9\\u00B7\\u0300-\\u036F\\u203F-\\u2040]*$"
  ), Bi = {}, Yi = {};
  function ov(l) {
    return $n.call(Yi, l) ? !0 : $n.call(Bi, l) ? !1 : sv.test(l) ? Yi[l] = !0 : (Bi[l] = !0, !1);
  }
  function Ne(l, t, u) {
    if (ov(t))
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
  function He(l, t, u) {
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
  function Rt(l, t, u, a) {
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
  function ct(l) {
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
  function Gi(l) {
    var t = l.type;
    return (l = l.nodeName) && l.toLowerCase() === "input" && (t === "checkbox" || t === "radio");
  }
  function yv(l, t, u) {
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
  function af(l) {
    if (!l._valueTracker) {
      var t = Gi(l) ? "checked" : "value";
      l._valueTracker = yv(
        l,
        t,
        "" + l[t]
      );
    }
  }
  function Xi(l) {
    if (!l) return !1;
    var t = l._valueTracker;
    if (!t) return !0;
    var u = t.getValue(), a = "";
    return l && (a = Gi(l) ? l.checked ? "true" : "false" : l.value), l = a, l !== u ? (t.setValue(l), !0) : !1;
  }
  function Ce(l) {
    if (l = l || (typeof document < "u" ? document : void 0), typeof l > "u") return null;
    try {
      return l.activeElement || l.body;
    } catch {
      return l.body;
    }
  }
  var vv = /[\n"\\]/g;
  function it(l) {
    return l.replace(
      vv,
      function(t) {
        return "\\" + t.charCodeAt(0).toString(16) + " ";
      }
    );
  }
  function ef(l, t, u, a, e, n, f, c) {
    l.name = "", f != null && typeof f != "function" && typeof f != "symbol" && typeof f != "boolean" ? l.type = f : l.removeAttribute("type"), t != null ? f === "number" ? (t === 0 && l.value === "" || l.value != t) && (l.value = "" + ct(t)) : l.value !== "" + ct(t) && (l.value = "" + ct(t)) : f !== "submit" && f !== "reset" || l.removeAttribute("value"), t != null ? nf(l, f, ct(t)) : u != null ? nf(l, f, ct(u)) : a != null && l.removeAttribute("value"), e == null && n != null && (l.defaultChecked = !!n), e != null && (l.checked = e && typeof e != "function" && typeof e != "symbol"), c != null && typeof c != "function" && typeof c != "symbol" && typeof c != "boolean" ? l.name = "" + ct(c) : l.removeAttribute("name");
  }
  function Qi(l, t, u, a, e, n, f, c) {
    if (n != null && typeof n != "function" && typeof n != "symbol" && typeof n != "boolean" && (l.type = n), t != null || u != null) {
      if (!(n !== "submit" && n !== "reset" || t != null)) {
        af(l);
        return;
      }
      u = u != null ? "" + ct(u) : "", t = t != null ? "" + ct(t) : u, c || t === l.value || (l.value = t), l.defaultValue = t;
    }
    a = a ?? e, a = typeof a != "function" && typeof a != "symbol" && !!a, l.checked = c ? l.checked : !!a, l.defaultChecked = !!a, f != null && typeof f != "function" && typeof f != "symbol" && typeof f != "boolean" && (l.name = f), af(l);
  }
  function nf(l, t, u) {
    t === "number" && Ce(l.ownerDocument) === l || l.defaultValue === "" + u || (l.defaultValue = "" + u);
  }
  function Wu(l, t, u, a) {
    if (l = l.options, t) {
      t = {};
      for (var e = 0; e < u.length; e++)
        t["$" + u[e]] = !0;
      for (u = 0; u < l.length; u++)
        e = t.hasOwnProperty("$" + l[u].value), l[u].selected !== e && (l[u].selected = e), e && a && (l[u].defaultSelected = !0);
    } else {
      for (u = "" + ct(u), t = null, e = 0; e < l.length; e++) {
        if (l[e].value === u) {
          l[e].selected = !0, a && (l[e].defaultSelected = !0);
          return;
        }
        t !== null || l[e].disabled || (t = l[e]);
      }
      t !== null && (t.selected = !0);
    }
  }
  function ji(l, t, u) {
    if (t != null && (t = "" + ct(t), t !== l.value && (l.value = t), u == null)) {
      l.defaultValue !== t && (l.defaultValue = t);
      return;
    }
    l.defaultValue = u != null ? "" + ct(u) : "";
  }
  function Zi(l, t, u, a) {
    if (t == null) {
      if (a != null) {
        if (u != null) throw Error(v(92));
        if (gt(a)) {
          if (1 < a.length) throw Error(v(93));
          a = a[0];
        }
        u = a;
      }
      u == null && (u = ""), t = u;
    }
    u = ct(t), l.defaultValue = u, a = l.textContent, a === u && a !== "" && a !== null && (l.value = a), af(l);
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
  var mv = new Set(
    "animationIterationCount aspectRatio borderImageOutset borderImageSlice borderImageWidth boxFlex boxFlexGroup boxOrdinalGroup columnCount columns flex flexGrow flexPositive flexShrink flexNegative flexOrder gridArea gridRow gridRowEnd gridRowSpan gridRowStart gridColumn gridColumnEnd gridColumnSpan gridColumnStart fontWeight lineClamp lineHeight opacity order orphans scale tabSize widows zIndex zoom fillOpacity floodOpacity stopOpacity strokeDasharray strokeDashoffset strokeMiterlimit strokeOpacity strokeWidth MozAnimationIterationCount MozBoxFlex MozBoxFlexGroup MozLineClamp msAnimationIterationCount msFlex msZoom msFlexGrow msFlexNegative msFlexOrder msFlexPositive msFlexShrink msGridColumn msGridColumnSpan msGridRow msGridRowSpan WebkitAnimationIterationCount WebkitBoxFlex WebKitBoxFlexGroup WebkitBoxOrdinalGroup WebkitColumnCount WebkitColumns WebkitFlex WebkitFlexGrow WebkitFlexPositive WebkitFlexShrink WebkitLineClamp".split(
      " "
    )
  );
  function Li(l, t, u) {
    var a = t.indexOf("--") === 0;
    u == null || typeof u == "boolean" || u === "" ? a ? l.setProperty(t, "") : t === "float" ? l.cssFloat = "" : l[t] = "" : a ? l.setProperty(t, u) : typeof u != "number" || u === 0 || mv.has(t) ? t === "float" ? l.cssFloat = u : l[t] = ("" + u).trim() : l[t] = u + "px";
  }
  function xi(l, t, u) {
    if (t != null && typeof t != "object")
      throw Error(v(62));
    if (l = l.style, u != null) {
      for (var a in u)
        !u.hasOwnProperty(a) || t != null && t.hasOwnProperty(a) || (a.indexOf("--") === 0 ? l.setProperty(a, "") : a === "float" ? l.cssFloat = "" : l[a] = "");
      for (var e in t)
        a = t[e], t.hasOwnProperty(e) && u[e] !== a && Li(l, e, a);
    } else
      for (var n in t)
        t.hasOwnProperty(n) && Li(l, n, t[n]);
  }
  function ff(l) {
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
  var dv = /* @__PURE__ */ new Map([
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
  ]), hv = /^[\u0000-\u001F ]*j[\r\n\t]*a[\r\n\t]*v[\r\n\t]*a[\r\n\t]*s[\r\n\t]*c[\r\n\t]*r[\r\n\t]*i[\r\n\t]*p[\r\n\t]*t[\r\n\t]*:/i;
  function qe(l) {
    return hv.test("" + l) ? "javascript:throw new Error('React has blocked a javascript: URL as a security precaution.')" : l;
  }
  function Nt() {
  }
  var cf = null;
  function sf(l) {
    return l = l.target || l.srcElement || window, l.correspondingUseElement && (l = l.correspondingUseElement), l.nodeType === 3 ? l.parentNode : l;
  }
  var Fu = null, ku = null;
  function Vi(l) {
    var t = Ku(l);
    if (t && (l = t.stateNode)) {
      var u = l[jl] || null;
      l: switch (l = t.stateNode, t.type) {
        case "input":
          if (ef(
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
              'input[name="' + it(
                "" + t
              ) + '"][type="radio"]'
            ), t = 0; t < u.length; t++) {
              var a = u[t];
              if (a !== l && a.form === l.form) {
                var e = a[jl] || null;
                if (!e) throw Error(v(90));
                ef(
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
              a = u[t], a.form === l.form && Xi(a);
          }
          break l;
        case "textarea":
          ji(l, u.value, u.defaultValue);
          break l;
        case "select":
          t = u.value, t != null && Wu(l, !!u.multiple, t, !1);
      }
    }
  }
  var of = !1;
  function Ki(l, t, u) {
    if (of) return l(t, u);
    of = !0;
    try {
      var a = l(t);
      return a;
    } finally {
      if (of = !1, (Fu !== null || ku !== null) && (zn(), Fu && (t = Fu, l = ku, ku = Fu = null, Vi(t), l)))
        for (t = 0; t < l.length; t++) Vi(l[t]);
    }
  }
  function qa(l, t) {
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
        v(231, t, typeof u)
      );
    return u;
  }
  var Ht = !(typeof window > "u" || typeof window.document > "u" || typeof window.document.createElement > "u"), yf = !1;
  if (Ht)
    try {
      var Ba = {};
      Object.defineProperty(Ba, "passive", {
        get: function() {
          yf = !0;
        }
      }), window.addEventListener("test", Ba, Ba), window.removeEventListener("test", Ba, Ba);
    } catch {
      yf = !1;
    }
  var kt = null, vf = null, Be = null;
  function Ji() {
    if (Be) return Be;
    var l, t = vf, u = t.length, a, e = "value" in kt ? kt.value : kt.textContent, n = e.length;
    for (l = 0; l < u && t[l] === e[l]; l++) ;
    var f = u - l;
    for (a = 1; a <= f && t[u - a] === e[n - a]; a++) ;
    return Be = e.slice(l, 1 < a ? 1 - a : void 0);
  }
  function Ye(l) {
    var t = l.keyCode;
    return "charCode" in l ? (l = l.charCode, l === 0 && t === 13 && (l = 13)) : l = t, l === 10 && (l = 13), 32 <= l || l === 13 ? l : 0;
  }
  function Ge() {
    return !0;
  }
  function wi() {
    return !1;
  }
  function Zl(l) {
    function t(u, a, e, n, f) {
      this._reactName = u, this._targetInst = e, this.type = a, this.nativeEvent = n, this.target = f, this.currentTarget = null;
      for (var c in l)
        l.hasOwnProperty(c) && (u = l[c], this[c] = u ? u(n) : n[c]);
      return this.isDefaultPrevented = (n.defaultPrevented != null ? n.defaultPrevented : n.returnValue === !1) ? Ge : wi, this.isPropagationStopped = wi, this;
    }
    return q(t.prototype, {
      preventDefault: function() {
        this.defaultPrevented = !0;
        var u = this.nativeEvent;
        u && (u.preventDefault ? u.preventDefault() : typeof u.returnValue != "unknown" && (u.returnValue = !1), this.isDefaultPrevented = Ge);
      },
      stopPropagation: function() {
        var u = this.nativeEvent;
        u && (u.stopPropagation ? u.stopPropagation() : typeof u.cancelBubble != "unknown" && (u.cancelBubble = !0), this.isPropagationStopped = Ge);
      },
      persist: function() {
      },
      isPersistent: Ge
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
  }, Xe = Zl(Mu), Ya = q({}, Mu, { view: 0, detail: 0 }), Sv = Zl(Ya), mf, df, Ga, Qe = q({}, Ya, {
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
    getModifierState: Sf,
    button: 0,
    buttons: 0,
    relatedTarget: function(l) {
      return l.relatedTarget === void 0 ? l.fromElement === l.srcElement ? l.toElement : l.fromElement : l.relatedTarget;
    },
    movementX: function(l) {
      return "movementX" in l ? l.movementX : (l !== Ga && (Ga && l.type === "mousemove" ? (mf = l.screenX - Ga.screenX, df = l.screenY - Ga.screenY) : df = mf = 0, Ga = l), mf);
    },
    movementY: function(l) {
      return "movementY" in l ? l.movementY : df;
    }
  }), Wi = Zl(Qe), gv = q({}, Qe, { dataTransfer: 0 }), rv = Zl(gv), bv = q({}, Ya, { relatedTarget: 0 }), hf = Zl(bv), Tv = q({}, Mu, {
    animationName: 0,
    elapsedTime: 0,
    pseudoElement: 0
  }), Ev = Zl(Tv), zv = q({}, Mu, {
    clipboardData: function(l) {
      return "clipboardData" in l ? l.clipboardData : window.clipboardData;
    }
  }), Av = Zl(zv), _v = q({}, Mu, { data: 0 }), $i = Zl(_v), Ov = {
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
  }, Mv = {
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
  }, pv = {
    Alt: "altKey",
    Control: "ctrlKey",
    Meta: "metaKey",
    Shift: "shiftKey"
  };
  function Dv(l) {
    var t = this.nativeEvent;
    return t.getModifierState ? t.getModifierState(l) : (l = pv[l]) ? !!t[l] : !1;
  }
  function Sf() {
    return Dv;
  }
  var Uv = q({}, Ya, {
    key: function(l) {
      if (l.key) {
        var t = Ov[l.key] || l.key;
        if (t !== "Unidentified") return t;
      }
      return l.type === "keypress" ? (l = Ye(l), l === 13 ? "Enter" : String.fromCharCode(l)) : l.type === "keydown" || l.type === "keyup" ? Mv[l.keyCode] || "Unidentified" : "";
    },
    code: 0,
    location: 0,
    ctrlKey: 0,
    shiftKey: 0,
    altKey: 0,
    metaKey: 0,
    repeat: 0,
    locale: 0,
    getModifierState: Sf,
    charCode: function(l) {
      return l.type === "keypress" ? Ye(l) : 0;
    },
    keyCode: function(l) {
      return l.type === "keydown" || l.type === "keyup" ? l.keyCode : 0;
    },
    which: function(l) {
      return l.type === "keypress" ? Ye(l) : l.type === "keydown" || l.type === "keyup" ? l.keyCode : 0;
    }
  }), Rv = Zl(Uv), Nv = q({}, Qe, {
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
  }), Fi = Zl(Nv), Hv = q({}, Ya, {
    touches: 0,
    targetTouches: 0,
    changedTouches: 0,
    altKey: 0,
    metaKey: 0,
    ctrlKey: 0,
    shiftKey: 0,
    getModifierState: Sf
  }), Cv = Zl(Hv), qv = q({}, Mu, {
    propertyName: 0,
    elapsedTime: 0,
    pseudoElement: 0
  }), Bv = Zl(qv), Yv = q({}, Qe, {
    deltaX: function(l) {
      return "deltaX" in l ? l.deltaX : "wheelDeltaX" in l ? -l.wheelDeltaX : 0;
    },
    deltaY: function(l) {
      return "deltaY" in l ? l.deltaY : "wheelDeltaY" in l ? -l.wheelDeltaY : "wheelDelta" in l ? -l.wheelDelta : 0;
    },
    deltaZ: 0,
    deltaMode: 0
  }), Gv = Zl(Yv), Xv = q({}, Mu, {
    newState: 0,
    oldState: 0
  }), Qv = Zl(Xv), jv = [9, 13, 27, 32], gf = Ht && "CompositionEvent" in window, Xa = null;
  Ht && "documentMode" in document && (Xa = document.documentMode);
  var Zv = Ht && "TextEvent" in window && !Xa, ki = Ht && (!gf || Xa && 8 < Xa && 11 >= Xa), Ii = " ", Pi = !1;
  function ls(l, t) {
    switch (l) {
      case "keyup":
        return jv.indexOf(t.keyCode) !== -1;
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
  function ts(l) {
    return l = l.detail, typeof l == "object" && "data" in l ? l.data : null;
  }
  var Iu = !1;
  function Lv(l, t) {
    switch (l) {
      case "compositionend":
        return ts(t);
      case "keypress":
        return t.which !== 32 ? null : (Pi = !0, Ii);
      case "textInput":
        return l = t.data, l === Ii && Pi ? null : l;
      default:
        return null;
    }
  }
  function xv(l, t) {
    if (Iu)
      return l === "compositionend" || !gf && ls(l, t) ? (l = Ji(), Be = vf = kt = null, Iu = !1, l) : null;
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
        return ki && t.locale !== "ko" ? null : t.data;
      default:
        return null;
    }
  }
  var Vv = {
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
  function us(l) {
    var t = l && l.nodeName && l.nodeName.toLowerCase();
    return t === "input" ? !!Vv[l.type] : t === "textarea";
  }
  function as(l, t, u, a) {
    Fu ? ku ? ku.push(a) : ku = [a] : Fu = a, t = Un(t, "onChange"), 0 < t.length && (u = new Xe(
      "onChange",
      "change",
      null,
      u,
      a
    ), l.push({ event: u, listeners: t }));
  }
  var Qa = null, ja = null;
  function Kv(l) {
    jo(l, 0);
  }
  function je(l) {
    var t = Ca(l);
    if (Xi(t)) return l;
  }
  function es(l, t) {
    if (l === "change") return t;
  }
  var ns = !1;
  if (Ht) {
    var rf;
    if (Ht) {
      var bf = "oninput" in document;
      if (!bf) {
        var fs = document.createElement("div");
        fs.setAttribute("oninput", "return;"), bf = typeof fs.oninput == "function";
      }
      rf = bf;
    } else rf = !1;
    ns = rf && (!document.documentMode || 9 < document.documentMode);
  }
  function cs() {
    Qa && (Qa.detachEvent("onpropertychange", is), ja = Qa = null);
  }
  function is(l) {
    if (l.propertyName === "value" && je(ja)) {
      var t = [];
      as(
        t,
        ja,
        l,
        sf(l)
      ), Ki(Kv, t);
    }
  }
  function Jv(l, t, u) {
    l === "focusin" ? (cs(), Qa = t, ja = u, Qa.attachEvent("onpropertychange", is)) : l === "focusout" && cs();
  }
  function wv(l) {
    if (l === "selectionchange" || l === "keyup" || l === "keydown")
      return je(ja);
  }
  function Wv(l, t) {
    if (l === "click") return je(t);
  }
  function $v(l, t) {
    if (l === "input" || l === "change")
      return je(t);
  }
  function Fv(l, t) {
    return l === t && (l !== 0 || 1 / l === 1 / t) || l !== l && t !== t;
  }
  var Il = typeof Object.is == "function" ? Object.is : Fv;
  function Za(l, t) {
    if (Il(l, t)) return !0;
    if (typeof l != "object" || l === null || typeof t != "object" || t === null)
      return !1;
    var u = Object.keys(l), a = Object.keys(t);
    if (u.length !== a.length) return !1;
    for (a = 0; a < u.length; a++) {
      var e = u[a];
      if (!$n.call(t, e) || !Il(l[e], t[e]))
        return !1;
    }
    return !0;
  }
  function ss(l) {
    for (; l && l.firstChild; ) l = l.firstChild;
    return l;
  }
  function os(l, t) {
    var u = ss(l);
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
      u = ss(u);
    }
  }
  function ys(l, t) {
    return l && t ? l === t ? !0 : l && l.nodeType === 3 ? !1 : t && t.nodeType === 3 ? ys(l, t.parentNode) : "contains" in l ? l.contains(t) : l.compareDocumentPosition ? !!(l.compareDocumentPosition(t) & 16) : !1 : !1;
  }
  function vs(l) {
    l = l != null && l.ownerDocument != null && l.ownerDocument.defaultView != null ? l.ownerDocument.defaultView : window;
    for (var t = Ce(l.document); t instanceof l.HTMLIFrameElement; ) {
      try {
        var u = typeof t.contentWindow.location.href == "string";
      } catch {
        u = !1;
      }
      if (u) l = t.contentWindow;
      else break;
      t = Ce(l.document);
    }
    return t;
  }
  function Tf(l) {
    var t = l && l.nodeName && l.nodeName.toLowerCase();
    return t && (t === "input" && (l.type === "text" || l.type === "search" || l.type === "tel" || l.type === "url" || l.type === "password") || t === "textarea" || l.contentEditable === "true");
  }
  var kv = Ht && "documentMode" in document && 11 >= document.documentMode, Pu = null, Ef = null, La = null, zf = !1;
  function ms(l, t, u) {
    var a = u.window === u ? u.document : u.nodeType === 9 ? u : u.ownerDocument;
    zf || Pu == null || Pu !== Ce(a) || (a = Pu, "selectionStart" in a && Tf(a) ? a = { start: a.selectionStart, end: a.selectionEnd } : (a = (a.ownerDocument && a.ownerDocument.defaultView || window).getSelection(), a = {
      anchorNode: a.anchorNode,
      anchorOffset: a.anchorOffset,
      focusNode: a.focusNode,
      focusOffset: a.focusOffset
    }), La && Za(La, a) || (La = a, a = Un(Ef, "onSelect"), 0 < a.length && (t = new Xe(
      "onSelect",
      "select",
      null,
      t,
      u
    ), l.push({ event: t, listeners: a }), t.target = Pu)));
  }
  function pu(l, t) {
    var u = {};
    return u[l.toLowerCase()] = t.toLowerCase(), u["Webkit" + l] = "webkit" + t, u["Moz" + l] = "moz" + t, u;
  }
  var la = {
    animationend: pu("Animation", "AnimationEnd"),
    animationiteration: pu("Animation", "AnimationIteration"),
    animationstart: pu("Animation", "AnimationStart"),
    transitionrun: pu("Transition", "TransitionRun"),
    transitionstart: pu("Transition", "TransitionStart"),
    transitioncancel: pu("Transition", "TransitionCancel"),
    transitionend: pu("Transition", "TransitionEnd")
  }, Af = {}, ds = {};
  Ht && (ds = document.createElement("div").style, "AnimationEvent" in window || (delete la.animationend.animation, delete la.animationiteration.animation, delete la.animationstart.animation), "TransitionEvent" in window || delete la.transitionend.transition);
  function Du(l) {
    if (Af[l]) return Af[l];
    if (!la[l]) return l;
    var t = la[l], u;
    for (u in t)
      if (t.hasOwnProperty(u) && u in ds)
        return Af[l] = t[u];
    return l;
  }
  var hs = Du("animationend"), Ss = Du("animationiteration"), gs = Du("animationstart"), Iv = Du("transitionrun"), Pv = Du("transitionstart"), lm = Du("transitioncancel"), rs = Du("transitionend"), bs = /* @__PURE__ */ new Map(), _f = "abort auxClick beforeToggle cancel canPlay canPlayThrough click close contextMenu copy cut drag dragEnd dragEnter dragExit dragLeave dragOver dragStart drop durationChange emptied encrypted ended error gotPointerCapture input invalid keyDown keyPress keyUp load loadedData loadedMetadata loadStart lostPointerCapture mouseDown mouseMove mouseOut mouseOver mouseUp paste pause play playing pointerCancel pointerDown pointerMove pointerOut pointerOver pointerUp progress rateChange reset resize seeked seeking stalled submit suspend timeUpdate touchCancel touchEnd touchStart volumeChange scroll toggle touchMove waiting wheel".split(
    " "
  );
  _f.push("scrollEnd");
  function rt(l, t) {
    bs.set(l, t), Ou(t, [l]);
  }
  var Ze = typeof reportError == "function" ? reportError : function(l) {
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
  }, st = [], ta = 0, Of = 0;
  function Le() {
    for (var l = ta, t = Of = ta = 0; t < l; ) {
      var u = st[t];
      st[t++] = null;
      var a = st[t];
      st[t++] = null;
      var e = st[t];
      st[t++] = null;
      var n = st[t];
      if (st[t++] = null, a !== null && e !== null) {
        var f = a.pending;
        f === null ? e.next = e : (e.next = f.next, f.next = e), a.pending = e;
      }
      n !== 0 && Ts(u, e, n);
    }
  }
  function xe(l, t, u, a) {
    st[ta++] = l, st[ta++] = t, st[ta++] = u, st[ta++] = a, Of |= a, l.lanes |= a, l = l.alternate, l !== null && (l.lanes |= a);
  }
  function Mf(l, t, u, a) {
    return xe(l, t, u, a), Ve(l);
  }
  function Uu(l, t) {
    return xe(l, null, null, t), Ve(l);
  }
  function Ts(l, t, u) {
    l.lanes |= u;
    var a = l.alternate;
    a !== null && (a.lanes |= u);
    for (var e = !1, n = l.return; n !== null; )
      n.childLanes |= u, a = n.alternate, a !== null && (a.childLanes |= u), n.tag === 22 && (l = n.stateNode, l === null || l._visibility & 1 || (e = !0)), l = n, n = n.return;
    return l.tag === 3 ? (n = l.stateNode, e && t !== null && (e = 31 - kl(u), l = n.hiddenUpdates, a = l[e], a === null ? l[e] = [t] : a.push(t), t.lane = u | 536870912), n) : null;
  }
  function Ve(l) {
    if (50 < oe)
      throw oe = 0, Bc = null, Error(v(185));
    for (var t = l.return; t !== null; )
      l = t, t = l.return;
    return l.tag === 3 ? l.stateNode : null;
  }
  var ua = {};
  function tm(l, t, u, a) {
    this.tag = l, this.key = u, this.sibling = this.child = this.return = this.stateNode = this.type = this.elementType = null, this.index = 0, this.refCleanup = this.ref = null, this.pendingProps = t, this.dependencies = this.memoizedState = this.updateQueue = this.memoizedProps = null, this.mode = a, this.subtreeFlags = this.flags = 0, this.deletions = null, this.childLanes = this.lanes = 0, this.alternate = null;
  }
  function Pl(l, t, u, a) {
    return new tm(l, t, u, a);
  }
  function pf(l) {
    return l = l.prototype, !(!l || !l.isReactComponent);
  }
  function Ct(l, t) {
    var u = l.alternate;
    return u === null ? (u = Pl(
      l.tag,
      t,
      l.key,
      l.mode
    ), u.elementType = l.elementType, u.type = l.type, u.stateNode = l.stateNode, u.alternate = l, l.alternate = u) : (u.pendingProps = t, u.type = l.type, u.flags = 0, u.subtreeFlags = 0, u.deletions = null), u.flags = l.flags & 65011712, u.childLanes = l.childLanes, u.lanes = l.lanes, u.child = l.child, u.memoizedProps = l.memoizedProps, u.memoizedState = l.memoizedState, u.updateQueue = l.updateQueue, t = l.dependencies, u.dependencies = t === null ? null : { lanes: t.lanes, firstContext: t.firstContext }, u.sibling = l.sibling, u.index = l.index, u.ref = l.ref, u.refCleanup = l.refCleanup, u;
  }
  function Es(l, t) {
    l.flags &= 65011714;
    var u = l.alternate;
    return u === null ? (l.childLanes = 0, l.lanes = t, l.child = null, l.subtreeFlags = 0, l.memoizedProps = null, l.memoizedState = null, l.updateQueue = null, l.dependencies = null, l.stateNode = null) : (l.childLanes = u.childLanes, l.lanes = u.lanes, l.child = u.child, l.subtreeFlags = 0, l.deletions = null, l.memoizedProps = u.memoizedProps, l.memoizedState = u.memoizedState, l.updateQueue = u.updateQueue, l.type = u.type, t = u.dependencies, l.dependencies = t === null ? null : {
      lanes: t.lanes,
      firstContext: t.firstContext
    }), l;
  }
  function Ke(l, t, u, a, e, n) {
    var f = 0;
    if (a = l, typeof l == "function") pf(l) && (f = 1);
    else if (typeof l == "string")
      f = fd(
        l,
        u,
        U.current
      ) ? 26 : l === "html" || l === "head" || l === "body" ? 27 : 5;
    else
      l: switch (l) {
        case zt:
          return l = Pl(31, u, t, e), l.elementType = zt, l.lanes = n, l;
        case Cl:
          return Ru(u.children, e, n, t);
        case Dt:
          f = 8, e |= 24;
          break;
        case Wl:
          return l = Pl(12, u, t, e | 2), l.elementType = Wl, l.lanes = n, l;
        case Et:
          return l = Pl(13, u, t, e), l.elementType = Et, l.lanes = n, l;
        case Gl:
          return l = Pl(19, u, t, e), l.elementType = Gl, l.lanes = n, l;
        default:
          if (typeof l == "object" && l !== null)
            switch (l.$$typeof) {
              case Nl:
                f = 10;
                break l;
              case Wt:
                f = 9;
                break l;
              case ft:
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
            v(130, l === null ? "null" : typeof l, "")
          ), a = null;
      }
    return t = Pl(f, u, t, e), t.elementType = l, t.type = a, t.lanes = n, t;
  }
  function Ru(l, t, u, a) {
    return l = Pl(7, l, a, t), l.lanes = u, l;
  }
  function Df(l, t, u) {
    return l = Pl(6, l, null, t), l.lanes = u, l;
  }
  function zs(l) {
    var t = Pl(18, null, null, 0);
    return t.stateNode = l, t;
  }
  function Uf(l, t, u) {
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
  var As = /* @__PURE__ */ new WeakMap();
  function ot(l, t) {
    if (typeof l == "object" && l !== null) {
      var u = As.get(l);
      return u !== void 0 ? u : (t = {
        value: l,
        source: t,
        stack: zi(t)
      }, As.set(l, t), t);
    }
    return {
      value: l,
      source: t,
      stack: zi(t)
    };
  }
  var aa = [], ea = 0, Je = null, xa = 0, yt = [], vt = 0, It = null, _t = 1, Ot = "";
  function qt(l, t) {
    aa[ea++] = xa, aa[ea++] = Je, Je = l, xa = t;
  }
  function _s(l, t, u) {
    yt[vt++] = _t, yt[vt++] = Ot, yt[vt++] = It, It = l;
    var a = _t;
    l = Ot;
    var e = 32 - kl(a) - 1;
    a &= ~(1 << e), u += 1;
    var n = 32 - kl(t) + e;
    if (30 < n) {
      var f = e - e % 5;
      n = (a & (1 << f) - 1).toString(32), a >>= f, e -= f, _t = 1 << 32 - kl(t) + e | u << e | a, Ot = n + l;
    } else
      _t = 1 << n | u << e | a, Ot = l;
  }
  function Rf(l) {
    l.return !== null && (qt(l, 1), _s(l, 1, 0));
  }
  function Nf(l) {
    for (; l === Je; )
      Je = aa[--ea], aa[ea] = null, xa = aa[--ea], aa[ea] = null;
    for (; l === It; )
      It = yt[--vt], yt[vt] = null, Ot = yt[--vt], yt[vt] = null, _t = yt[--vt], yt[vt] = null;
  }
  function Os(l, t) {
    yt[vt++] = _t, yt[vt++] = Ot, yt[vt++] = It, _t = t.id, Ot = t.overflow, It = l;
  }
  var pl = null, ol = null, w = !1, Pt = null, mt = !1, Hf = Error(v(519));
  function lu(l) {
    var t = Error(
      v(
        418,
        1 < arguments.length && arguments[1] !== void 0 && arguments[1] ? "text" : "HTML",
        ""
      )
    );
    throw Va(ot(t, l)), Hf;
  }
  function Ms(l) {
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
        x("invalid", t), Qi(
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
        x("invalid", t), Zi(t, a.value, a.defaultValue, a.children);
    }
    u = a.children, typeof u != "string" && typeof u != "number" && typeof u != "bigint" || t.textContent === "" + u || a.suppressHydrationWarning === !0 || Vo(t.textContent, u) ? (a.popover != null && (x("beforetoggle", t), x("toggle", t)), a.onScroll != null && x("scroll", t), a.onScrollEnd != null && x("scrollend", t), a.onClick != null && (t.onclick = Nt), t = !0) : t = !1, t || lu(l, !0);
  }
  function ps(l) {
    for (pl = l.return; pl; )
      switch (pl.tag) {
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
          pl = pl.return;
      }
  }
  function na(l) {
    if (l !== pl) return !1;
    if (!w) return ps(l), w = !0, !1;
    var t = l.tag, u;
    if ((u = t !== 3 && t !== 27) && ((u = t === 5) && (u = l.type, u = !(u !== "form" && u !== "button") || Fc(l.type, l.memoizedProps)), u = !u), u && ol && lu(l), ps(l), t === 13) {
      if (l = l.memoizedState, l = l !== null ? l.dehydrated : null, !l) throw Error(v(317));
      ol = Po(l);
    } else if (t === 31) {
      if (l = l.memoizedState, l = l !== null ? l.dehydrated : null, !l) throw Error(v(317));
      ol = Po(l);
    } else
      t === 27 ? (t = ol, du(l.type) ? (l = ti, ti = null, ol = l) : ol = t) : ol = pl ? ht(l.stateNode.nextSibling) : null;
    return !0;
  }
  function Nu() {
    ol = pl = null, w = !1;
  }
  function Cf() {
    var l = Pt;
    return l !== null && (Kl === null ? Kl = l : Kl.push.apply(
      Kl,
      l
    ), Pt = null), l;
  }
  function Va(l) {
    Pt === null ? Pt = [l] : Pt.push(l);
  }
  var qf = o(null), Hu = null, Bt = null;
  function tu(l, t, u) {
    p(qf, t._currentValue), t._currentValue = u;
  }
  function Yt(l) {
    l._currentValue = qf.current, z(qf);
  }
  function Bf(l, t, u) {
    for (; l !== null; ) {
      var a = l.alternate;
      if ((l.childLanes & t) !== t ? (l.childLanes |= t, a !== null && (a.childLanes |= t)) : a !== null && (a.childLanes & t) !== t && (a.childLanes |= t), l === u) break;
      l = l.return;
    }
  }
  function Yf(l, t, u, a) {
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
              n.lanes |= u, c = n.alternate, c !== null && (c.lanes |= u), Bf(
                n.return,
                u,
                l
              ), a || (f = null);
              break l;
            }
          n = c.next;
        }
      } else if (e.tag === 18) {
        if (f = e.return, f === null) throw Error(v(341));
        f.lanes |= u, n = f.alternate, n !== null && (n.lanes |= u), Bf(f, u, l), f = null;
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
        if (f === null) throw Error(v(387));
        if (f = f.memoizedProps, f !== null) {
          var c = e.type;
          Il(e.pendingProps.value, f.value) || (l !== null ? l.push(c) : l = [c]);
        }
      } else if (e === ll.current) {
        if (f = e.alternate, f === null) throw Error(v(387));
        f.memoizedState.memoizedState !== e.memoizedState.memoizedState && (l !== null ? l.push(ge) : l = [ge]);
      }
      e = e.return;
    }
    l !== null && Yf(
      t,
      l,
      u,
      a
    ), t.flags |= 262144;
  }
  function we(l) {
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
    Hu = l, Bt = null, l = l.dependencies, l !== null && (l.firstContext = null);
  }
  function Dl(l) {
    return Ds(Hu, l);
  }
  function We(l, t) {
    return Hu === null && Cu(l), Ds(l, t);
  }
  function Ds(l, t) {
    var u = t._currentValue;
    if (t = { context: t, memoizedValue: u, next: null }, Bt === null) {
      if (l === null) throw Error(v(308));
      Bt = t, l.dependencies = { lanes: 0, firstContext: t }, l.flags |= 524288;
    } else Bt = Bt.next = t;
    return u;
  }
  var um = typeof AbortController < "u" ? AbortController : function() {
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
  }, am = m.unstable_scheduleCallback, em = m.unstable_NormalPriority, bl = {
    $$typeof: Nl,
    Consumer: null,
    Provider: null,
    _currentValue: null,
    _currentValue2: null,
    _threadCount: 0
  };
  function Gf() {
    return {
      controller: new um(),
      data: /* @__PURE__ */ new Map(),
      refCount: 0
    };
  }
  function Ka(l) {
    l.refCount--, l.refCount === 0 && am(em, function() {
      l.controller.abort();
    });
  }
  var Ja = null, Xf = 0, ca = 0, ia = null;
  function nm(l, t) {
    if (Ja === null) {
      var u = Ja = [];
      Xf = 0, ca = Zc(), ia = {
        status: "pending",
        value: void 0,
        then: function(a) {
          u.push(a);
        }
      };
    }
    return Xf++, t.then(Us, Us), t;
  }
  function Us() {
    if (--Xf === 0 && Ja !== null) {
      ia !== null && (ia.status = "fulfilled");
      var l = Ja;
      Ja = null, ca = 0, ia = null;
      for (var t = 0; t < l.length; t++) (0, l[t])();
    }
  }
  function fm(l, t) {
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
  var Rs = b.S;
  b.S = function(l, t) {
    mo = $l(), typeof t == "object" && t !== null && typeof t.then == "function" && nm(l, t), Rs !== null && Rs(l, t);
  };
  var qu = o(null);
  function Qf() {
    var l = qu.current;
    return l !== null ? l : sl.pooledCache;
  }
  function $e(l, t) {
    t === null ? p(qu, qu.current) : p(qu, t.pool);
  }
  function Ns() {
    var l = Qf();
    return l === null ? null : { parent: bl._currentValue, pool: l };
  }
  var sa = Error(v(460)), jf = Error(v(474)), Fe = Error(v(542)), ke = { then: function() {
  } };
  function Hs(l) {
    return l = l.status, l === "fulfilled" || l === "rejected";
  }
  function Cs(l, t, u) {
    switch (u = l[u], u === void 0 ? l.push(t) : u !== t && (t.then(Nt, Nt), t = u), t.status) {
      case "fulfilled":
        return t.value;
      case "rejected":
        throw l = t.reason, Bs(l), l;
      default:
        if (typeof t.status == "string") t.then(Nt, Nt);
        else {
          if (l = sl, l !== null && 100 < l.shellSuspendCounter)
            throw Error(v(482));
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
            throw l = t.reason, Bs(l), l;
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
  function qs() {
    if (Yu === null) throw Error(v(459));
    var l = Yu;
    return Yu = null, l;
  }
  function Bs(l) {
    if (l === sa || l === Fe)
      throw Error(v(483));
  }
  var oa = null, wa = 0;
  function Ie(l) {
    var t = wa;
    return wa += 1, oa === null && (oa = []), Cs(oa, l, t);
  }
  function Wa(l, t) {
    t = t.props.ref, l.ref = t !== void 0 ? t : null;
  }
  function Pe(l, t) {
    throw t.$$typeof === P ? Error(v(525)) : (l = Object.prototype.toString.call(t), Error(
      v(
        31,
        l === "[object Object]" ? "object with keys {" + Object.keys(t).join(", ") + "}" : l
      )
    ));
  }
  function Ys(l) {
    function t(y, s) {
      if (l) {
        var d = y.deletions;
        d === null ? (y.deletions = [s], y.flags |= 16) : d.push(s);
      }
    }
    function u(y, s) {
      if (!l) return null;
      for (; s !== null; )
        t(y, s), s = s.sibling;
      return null;
    }
    function a(y) {
      for (var s = /* @__PURE__ */ new Map(); y !== null; )
        y.key !== null ? s.set(y.key, y) : s.set(y.index, y), y = y.sibling;
      return s;
    }
    function e(y, s) {
      return y = Ct(y, s), y.index = 0, y.sibling = null, y;
    }
    function n(y, s, d) {
      return y.index = d, l ? (d = y.alternate, d !== null ? (d = d.index, d < s ? (y.flags |= 67108866, s) : d) : (y.flags |= 67108866, s)) : (y.flags |= 1048576, s);
    }
    function f(y) {
      return l && y.alternate === null && (y.flags |= 67108866), y;
    }
    function c(y, s, d, T) {
      return s === null || s.tag !== 6 ? (s = Df(d, y.mode, T), s.return = y, s) : (s = e(s, d), s.return = y, s);
    }
    function i(y, s, d, T) {
      var N = d.type;
      return N === Cl ? r(
        y,
        s,
        d.props.children,
        T,
        d.key
      ) : s !== null && (s.elementType === N || typeof N == "object" && N !== null && N.$$typeof === Xl && Bu(N) === s.type) ? (s = e(s, d.props), Wa(s, d), s.return = y, s) : (s = Ke(
        d.type,
        d.key,
        d.props,
        null,
        y.mode,
        T
      ), Wa(s, d), s.return = y, s);
    }
    function h(y, s, d, T) {
      return s === null || s.tag !== 4 || s.stateNode.containerInfo !== d.containerInfo || s.stateNode.implementation !== d.implementation ? (s = Uf(d, y.mode, T), s.return = y, s) : (s = e(s, d.children || []), s.return = y, s);
    }
    function r(y, s, d, T, N) {
      return s === null || s.tag !== 7 ? (s = Ru(
        d,
        y.mode,
        T,
        N
      ), s.return = y, s) : (s = e(s, d), s.return = y, s);
    }
    function E(y, s, d) {
      if (typeof s == "string" && s !== "" || typeof s == "number" || typeof s == "bigint")
        return s = Df(
          "" + s,
          y.mode,
          d
        ), s.return = y, s;
      if (typeof s == "object" && s !== null) {
        switch (s.$$typeof) {
          case wl:
            return d = Ke(
              s.type,
              s.key,
              s.props,
              null,
              y.mode,
              d
            ), Wa(d, s), d.return = y, d;
          case Yl:
            return s = Uf(
              s,
              y.mode,
              d
            ), s.return = y, s;
          case Xl:
            return s = Bu(s), E(y, s, d);
        }
        if (gt(s) || Ql(s))
          return s = Ru(
            s,
            y.mode,
            d,
            null
          ), s.return = y, s;
        if (typeof s.then == "function")
          return E(y, Ie(s), d);
        if (s.$$typeof === Nl)
          return E(
            y,
            We(y, s),
            d
          );
        Pe(y, s);
      }
      return null;
    }
    function S(y, s, d, T) {
      var N = s !== null ? s.key : null;
      if (typeof d == "string" && d !== "" || typeof d == "number" || typeof d == "bigint")
        return N !== null ? null : c(y, s, "" + d, T);
      if (typeof d == "object" && d !== null) {
        switch (d.$$typeof) {
          case wl:
            return d.key === N ? i(y, s, d, T) : null;
          case Yl:
            return d.key === N ? h(y, s, d, T) : null;
          case Xl:
            return d = Bu(d), S(y, s, d, T);
        }
        if (gt(d) || Ql(d))
          return N !== null ? null : r(y, s, d, T, null);
        if (typeof d.then == "function")
          return S(
            y,
            s,
            Ie(d),
            T
          );
        if (d.$$typeof === Nl)
          return S(
            y,
            s,
            We(y, d),
            T
          );
        Pe(y, d);
      }
      return null;
    }
    function g(y, s, d, T, N) {
      if (typeof T == "string" && T !== "" || typeof T == "number" || typeof T == "bigint")
        return y = y.get(d) || null, c(s, y, "" + T, N);
      if (typeof T == "object" && T !== null) {
        switch (T.$$typeof) {
          case wl:
            return y = y.get(
              T.key === null ? d : T.key
            ) || null, i(s, y, T, N);
          case Yl:
            return y = y.get(
              T.key === null ? d : T.key
            ) || null, h(s, y, T, N);
          case Xl:
            return T = Bu(T), g(
              y,
              s,
              d,
              T,
              N
            );
        }
        if (gt(T) || Ql(T))
          return y = y.get(d) || null, r(s, y, T, N, null);
        if (typeof T.then == "function")
          return g(
            y,
            s,
            d,
            Ie(T),
            N
          );
        if (T.$$typeof === Nl)
          return g(
            y,
            s,
            d,
            We(s, T),
            N
          );
        Pe(s, T);
      }
      return null;
    }
    function D(y, s, d, T) {
      for (var N = null, F = null, R = s, j = s = 0, K = null; R !== null && j < d.length; j++) {
        R.index > j ? (K = R, R = null) : K = R.sibling;
        var k = S(
          y,
          R,
          d[j],
          T
        );
        if (k === null) {
          R === null && (R = K);
          break;
        }
        l && R && k.alternate === null && t(y, R), s = n(k, s, j), F === null ? N = k : F.sibling = k, F = k, R = K;
      }
      if (j === d.length)
        return u(y, R), w && qt(y, j), N;
      if (R === null) {
        for (; j < d.length; j++)
          R = E(y, d[j], T), R !== null && (s = n(
            R,
            s,
            j
          ), F === null ? N = R : F.sibling = R, F = R);
        return w && qt(y, j), N;
      }
      for (R = a(R); j < d.length; j++)
        K = g(
          R,
          y,
          j,
          d[j],
          T
        ), K !== null && (l && K.alternate !== null && R.delete(
          K.key === null ? j : K.key
        ), s = n(
          K,
          s,
          j
        ), F === null ? N = K : F.sibling = K, F = K);
      return l && R.forEach(function(bu) {
        return t(y, bu);
      }), w && qt(y, j), N;
    }
    function C(y, s, d, T) {
      if (d == null) throw Error(v(151));
      for (var N = null, F = null, R = s, j = s = 0, K = null, k = d.next(); R !== null && !k.done; j++, k = d.next()) {
        R.index > j ? (K = R, R = null) : K = R.sibling;
        var bu = S(y, R, k.value, T);
        if (bu === null) {
          R === null && (R = K);
          break;
        }
        l && R && bu.alternate === null && t(y, R), s = n(bu, s, j), F === null ? N = bu : F.sibling = bu, F = bu, R = K;
      }
      if (k.done)
        return u(y, R), w && qt(y, j), N;
      if (R === null) {
        for (; !k.done; j++, k = d.next())
          k = E(y, k.value, T), k !== null && (s = n(k, s, j), F === null ? N = k : F.sibling = k, F = k);
        return w && qt(y, j), N;
      }
      for (R = a(R); !k.done; j++, k = d.next())
        k = g(R, y, j, k.value, T), k !== null && (l && k.alternate !== null && R.delete(k.key === null ? j : k.key), s = n(k, s, j), F === null ? N = k : F.sibling = k, F = k);
      return l && R.forEach(function(gd) {
        return t(y, gd);
      }), w && qt(y, j), N;
    }
    function cl(y, s, d, T) {
      if (typeof d == "object" && d !== null && d.type === Cl && d.key === null && (d = d.props.children), typeof d == "object" && d !== null) {
        switch (d.$$typeof) {
          case wl:
            l: {
              for (var N = d.key; s !== null; ) {
                if (s.key === N) {
                  if (N = d.type, N === Cl) {
                    if (s.tag === 7) {
                      u(
                        y,
                        s.sibling
                      ), T = e(
                        s,
                        d.props.children
                      ), T.return = y, y = T;
                      break l;
                    }
                  } else if (s.elementType === N || typeof N == "object" && N !== null && N.$$typeof === Xl && Bu(N) === s.type) {
                    u(
                      y,
                      s.sibling
                    ), T = e(s, d.props), Wa(T, d), T.return = y, y = T;
                    break l;
                  }
                  u(y, s);
                  break;
                } else t(y, s);
                s = s.sibling;
              }
              d.type === Cl ? (T = Ru(
                d.props.children,
                y.mode,
                T,
                d.key
              ), T.return = y, y = T) : (T = Ke(
                d.type,
                d.key,
                d.props,
                null,
                y.mode,
                T
              ), Wa(T, d), T.return = y, y = T);
            }
            return f(y);
          case Yl:
            l: {
              for (N = d.key; s !== null; ) {
                if (s.key === N)
                  if (s.tag === 4 && s.stateNode.containerInfo === d.containerInfo && s.stateNode.implementation === d.implementation) {
                    u(
                      y,
                      s.sibling
                    ), T = e(s, d.children || []), T.return = y, y = T;
                    break l;
                  } else {
                    u(y, s);
                    break;
                  }
                else t(y, s);
                s = s.sibling;
              }
              T = Uf(d, y.mode, T), T.return = y, y = T;
            }
            return f(y);
          case Xl:
            return d = Bu(d), cl(
              y,
              s,
              d,
              T
            );
        }
        if (gt(d))
          return D(
            y,
            s,
            d,
            T
          );
        if (Ql(d)) {
          if (N = Ql(d), typeof N != "function") throw Error(v(150));
          return d = N.call(d), C(
            y,
            s,
            d,
            T
          );
        }
        if (typeof d.then == "function")
          return cl(
            y,
            s,
            Ie(d),
            T
          );
        if (d.$$typeof === Nl)
          return cl(
            y,
            s,
            We(y, d),
            T
          );
        Pe(y, d);
      }
      return typeof d == "string" && d !== "" || typeof d == "number" || typeof d == "bigint" ? (d = "" + d, s !== null && s.tag === 6 ? (u(y, s.sibling), T = e(s, d), T.return = y, y = T) : (u(y, s), T = Df(d, y.mode, T), T.return = y, y = T), f(y)) : u(y, s);
    }
    return function(y, s, d, T) {
      try {
        wa = 0;
        var N = cl(
          y,
          s,
          d,
          T
        );
        return oa = null, N;
      } catch (R) {
        if (R === sa || R === Fe) throw R;
        var F = Pl(29, R, null, y.mode);
        return F.lanes = T, F.return = y, F;
      } finally {
      }
    };
  }
  var Gu = Ys(!0), Gs = Ys(!1), uu = !1;
  function Zf(l) {
    l.updateQueue = {
      baseState: l.memoizedState,
      firstBaseUpdate: null,
      lastBaseUpdate: null,
      shared: { pending: null, lanes: 0, hiddenCallbacks: null },
      callbacks: null
    };
  }
  function Lf(l, t) {
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
      return e === null ? t.next = t : (t.next = e.next, e.next = t), a.pending = t, t = Ve(l), Ts(l, null, u), t;
    }
    return xe(l, a, t, u), Ve(l);
  }
  function $a(l, t, u) {
    if (t = t.updateQueue, t !== null && (t = t.shared, (u & 4194048) !== 0)) {
      var a = t.lanes;
      a &= l.pendingLanes, u |= a, t.lanes = u, Di(l, u);
    }
  }
  function xf(l, t) {
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
  var Vf = !1;
  function Fa() {
    if (Vf) {
      var l = ia;
      if (l !== null) throw l;
    }
  }
  function ka(l, t, u, a) {
    Vf = !1;
    var e = l.updateQueue;
    uu = !1;
    var n = e.firstBaseUpdate, f = e.lastBaseUpdate, c = e.shared.pending;
    if (c !== null) {
      e.shared.pending = null;
      var i = c, h = i.next;
      i.next = null, f === null ? n = h : f.next = h, f = i;
      var r = l.alternate;
      r !== null && (r = r.updateQueue, c = r.lastBaseUpdate, c !== f && (c === null ? r.firstBaseUpdate = h : c.next = h, r.lastBaseUpdate = i));
    }
    if (n !== null) {
      var E = e.baseState;
      f = 0, r = h = i = null, c = n;
      do {
        var S = c.lane & -536870913, g = S !== c.lane;
        if (g ? (V & S) === S : (a & S) === S) {
          S !== 0 && S === ca && (Vf = !0), r !== null && (r = r.next = {
            lane: 0,
            tag: c.tag,
            payload: c.payload,
            callback: null,
            next: null
          });
          l: {
            var D = l, C = c;
            S = t;
            var cl = u;
            switch (C.tag) {
              case 1:
                if (D = C.payload, typeof D == "function") {
                  E = D.call(cl, E, S);
                  break l;
                }
                E = D;
                break l;
              case 3:
                D.flags = D.flags & -65537 | 128;
              case 0:
                if (D = C.payload, S = typeof D == "function" ? D.call(cl, E, S) : D, S == null) break l;
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
          }, r === null ? (h = r = g, i = E) : r = r.next = g, f |= S;
        if (c = c.next, c === null) {
          if (c = e.shared.pending, c === null)
            break;
          g = c, c = g.next, g.next = null, e.lastBaseUpdate = g, e.shared.pending = null;
        }
      } while (!0);
      r === null && (i = E), e.baseState = i, e.firstBaseUpdate = h, e.lastBaseUpdate = r, n === null && (e.shared.lanes = 0), su |= f, l.lanes = f, l.memoizedState = E;
    }
  }
  function Xs(l, t) {
    if (typeof l != "function")
      throw Error(v(191, l));
    l.call(t);
  }
  function Qs(l, t) {
    var u = l.callbacks;
    if (u !== null)
      for (l.callbacks = null, l = 0; l < u.length; l++)
        Xs(u[l], t);
  }
  var ya = o(null), ln = o(0);
  function js(l, t) {
    l = Kt, p(ln, l), p(ya, t), Kt = l | t.baseLanes;
  }
  function Kf() {
    p(ln, Kt), p(ya, ya.current);
  }
  function Jf() {
    Kt = ln.current, z(ya), z(ln);
  }
  var lt = o(null), dt = null;
  function nu(l) {
    var t = l.alternate;
    p(gl, gl.current & 1), p(lt, l), dt === null && (t === null || ya.current !== null || t.memoizedState !== null) && (dt = l);
  }
  function wf(l) {
    p(gl, gl.current), p(lt, l), dt === null && (dt = l);
  }
  function Zs(l) {
    l.tag === 22 ? (p(gl, gl.current), p(lt, l), dt === null && (dt = l)) : fu();
  }
  function fu() {
    p(gl, gl.current), p(lt, lt.current);
  }
  function tt(l) {
    z(lt), dt === l && (dt = null), z(gl);
  }
  var gl = o(0);
  function tn(l) {
    for (var t = l; t !== null; ) {
      if (t.tag === 13) {
        var u = t.memoizedState;
        if (u !== null && (u = u.dehydrated, u === null || Pc(u) || li(u)))
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
  var Gt = 0, Q = null, nl = null, Tl = null, un = !1, va = !1, Xu = !1, an = 0, Ia = 0, ma = null, cm = 0;
  function ml() {
    throw Error(v(321));
  }
  function Wf(l, t) {
    if (t === null) return !1;
    for (var u = 0; u < t.length && u < l.length; u++)
      if (!Il(l[u], t[u])) return !1;
    return !0;
  }
  function $f(l, t, u, a, e, n) {
    return Gt = n, Q = t, t.memoizedState = null, t.updateQueue = null, t.lanes = 0, b.H = l === null || l.memoizedState === null ? _0 : oc, Xu = !1, n = u(a, e), Xu = !1, va && (n = xs(
      t,
      u,
      a,
      e
    )), Ls(l), n;
  }
  function Ls(l) {
    b.H = te;
    var t = nl !== null && nl.next !== null;
    if (Gt = 0, Tl = nl = Q = null, un = !1, Ia = 0, ma = null, t) throw Error(v(300));
    l === null || El || (l = l.dependencies, l !== null && we(l) && (El = !0));
  }
  function xs(l, t, u, a) {
    Q = l;
    var e = 0;
    do {
      if (va && (ma = null), Ia = 0, va = !1, 25 <= e) throw Error(v(301));
      if (e += 1, Tl = nl = null, l.updateQueue != null) {
        var n = l.updateQueue;
        n.lastEffect = null, n.events = null, n.stores = null, n.memoCache != null && (n.memoCache.index = 0);
      }
      b.H = O0, n = t(u, a);
    } while (va);
    return n;
  }
  function im() {
    var l = b.H, t = l.useState()[0];
    return t = typeof t.then == "function" ? Pa(t) : t, l = l.useState()[0], (nl !== null ? nl.memoizedState : null) !== l && (Q.flags |= 1024), t;
  }
  function Ff() {
    var l = an !== 0;
    return an = 0, l;
  }
  function kf(l, t, u) {
    t.updateQueue = l.updateQueue, t.flags &= -2053, l.lanes &= ~u;
  }
  function If(l) {
    if (un) {
      for (l = l.memoizedState; l !== null; ) {
        var t = l.queue;
        t !== null && (t.pending = null), l = l.next;
      }
      un = !1;
    }
    Gt = 0, Tl = nl = Q = null, va = !1, Ia = an = 0, ma = null;
  }
  function Bl() {
    var l = {
      memoizedState: null,
      baseState: null,
      baseQueue: null,
      queue: null,
      next: null
    };
    return Tl === null ? Q.memoizedState = Tl = l : Tl = Tl.next = l, Tl;
  }
  function rl() {
    if (nl === null) {
      var l = Q.alternate;
      l = l !== null ? l.memoizedState : null;
    } else l = nl.next;
    var t = Tl === null ? Q.memoizedState : Tl.next;
    if (t !== null)
      Tl = t, nl = l;
    else {
      if (l === null)
        throw Q.alternate === null ? Error(v(467)) : Error(v(310));
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
  function en() {
    return { lastEffect: null, events: null, stores: null, memoCache: null };
  }
  function Pa(l) {
    var t = Ia;
    return Ia += 1, ma === null && (ma = []), l = Cs(ma, l, t), t = Q, (Tl === null ? t.memoizedState : Tl.next) === null && (t = t.alternate, b.H = t === null || t.memoizedState === null ? _0 : oc), l;
  }
  function nn(l) {
    if (l !== null && typeof l == "object") {
      if (typeof l.then == "function") return Pa(l);
      if (l.$$typeof === Nl) return Dl(l);
    }
    throw Error(v(438, String(l)));
  }
  function Pf(l) {
    var t = null, u = Q.updateQueue;
    if (u !== null && (t = u.memoCache), t == null) {
      var a = Q.alternate;
      a !== null && (a = a.updateQueue, a !== null && (a = a.memoCache, a != null && (t = {
        data: a.data.map(function(e) {
          return e.slice();
        }),
        index: 0
      })));
    }
    if (t == null && (t = { data: [], index: 0 }), u === null && (u = en(), Q.updateQueue = u), u.memoCache = t, u = t.data[t.index], u === void 0)
      for (u = t.data[t.index] = Array(l), a = 0; a < l; a++)
        u[a] = Lu;
    return t.index++, u;
  }
  function Xt(l, t) {
    return typeof t == "function" ? t(l) : t;
  }
  function fn(l) {
    var t = rl();
    return lc(t, nl, l);
  }
  function lc(l, t, u) {
    var a = l.queue;
    if (a === null) throw Error(v(311));
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
      var c = f = null, i = null, h = t, r = !1;
      do {
        var E = h.lane & -536870913;
        if (E !== h.lane ? (V & E) === E : (Gt & E) === E) {
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
            }), E === ca && (r = !0);
          else if ((Gt & S) === S) {
            h = h.next, S === ca && (r = !0);
            continue;
          } else
            E = {
              lane: 0,
              revertLane: h.revertLane,
              gesture: null,
              action: h.action,
              hasEagerState: h.hasEagerState,
              eagerState: h.eagerState,
              next: null
            }, i === null ? (c = i = E, f = n) : i = i.next = E, Q.lanes |= S, su |= S;
          E = h.action, Xu && u(n, E), n = h.hasEagerState ? h.eagerState : u(n, E);
        } else
          S = {
            lane: E,
            revertLane: h.revertLane,
            gesture: h.gesture,
            action: h.action,
            hasEagerState: h.hasEagerState,
            eagerState: h.eagerState,
            next: null
          }, i === null ? (c = i = S, f = n) : i = i.next = S, Q.lanes |= E, su |= E;
        h = h.next;
      } while (h !== null && h !== t);
      if (i === null ? f = n : i.next = c, !Il(n, l.memoizedState) && (El = !0, r && (u = ia, u !== null)))
        throw u;
      l.memoizedState = n, l.baseState = f, l.baseQueue = i, a.lastRenderedState = n;
    }
    return e === null && (a.lanes = 0), [l.memoizedState, a.dispatch];
  }
  function tc(l) {
    var t = rl(), u = t.queue;
    if (u === null) throw Error(v(311));
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
  function Vs(l, t, u) {
    var a = Q, e = rl(), n = w;
    if (n) {
      if (u === void 0) throw Error(v(407));
      u = u();
    } else u = t();
    var f = !Il(
      (nl || e).memoizedState,
      u
    );
    if (f && (e.memoizedState = u, El = !0), e = e.queue, ec(ws.bind(null, a, e, l), [
      l
    ]), e.getSnapshot !== t || f || Tl !== null && Tl.memoizedState.tag & 1) {
      if (a.flags |= 2048, da(
        9,
        { destroy: void 0 },
        Js.bind(
          null,
          a,
          e,
          u,
          t
        ),
        null
      ), sl === null) throw Error(v(349));
      n || (Gt & 127) !== 0 || Ks(a, t, u);
    }
    return u;
  }
  function Ks(l, t, u) {
    l.flags |= 16384, l = { getSnapshot: t, value: u }, t = Q.updateQueue, t === null ? (t = en(), Q.updateQueue = t, t.stores = [l]) : (u = t.stores, u === null ? t.stores = [l] : u.push(l));
  }
  function Js(l, t, u, a) {
    t.value = u, t.getSnapshot = a, Ws(t) && $s(l);
  }
  function ws(l, t, u) {
    return u(function() {
      Ws(t) && $s(l);
    });
  }
  function Ws(l) {
    var t = l.getSnapshot;
    l = l.value;
    try {
      var u = t();
      return !Il(l, u);
    } catch {
      return !0;
    }
  }
  function $s(l) {
    var t = Uu(l, 2);
    t !== null && Jl(t, l, 2);
  }
  function uc(l) {
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
      lastRenderedReducer: Xt,
      lastRenderedState: l
    }, t;
  }
  function Fs(l, t, u, a) {
    return l.baseState = u, lc(
      l,
      nl,
      typeof a == "function" ? a : Xt
    );
  }
  function sm(l, t, u, a, e) {
    if (on(l)) throw Error(v(485));
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
      b.T !== null ? u(!0) : n.isTransition = !1, a(n), u = t.pending, u === null ? (n.next = t.pending = n, ks(t, n)) : (n.next = u.next, t.pending = u.next = n);
    }
  }
  function ks(l, t) {
    var u = t.action, a = t.payload, e = l.state;
    if (t.isTransition) {
      var n = b.T, f = {};
      b.T = f;
      try {
        var c = u(e, a), i = b.S;
        i !== null && i(f, c), Is(l, t, c);
      } catch (h) {
        ac(l, t, h);
      } finally {
        n !== null && f.types !== null && (n.types = f.types), b.T = n;
      }
    } else
      try {
        n = u(e, a), Is(l, t, n);
      } catch (h) {
        ac(l, t, h);
      }
  }
  function Is(l, t, u) {
    u !== null && typeof u == "object" && typeof u.then == "function" ? u.then(
      function(a) {
        Ps(l, t, a);
      },
      function(a) {
        return ac(l, t, a);
      }
    ) : Ps(l, t, u);
  }
  function Ps(l, t, u) {
    t.status = "fulfilled", t.value = u, l0(t), l.state = u, t = l.pending, t !== null && (u = t.next, u === t ? l.pending = null : (u = u.next, t.next = u, ks(l, u)));
  }
  function ac(l, t, u) {
    var a = l.pending;
    if (l.pending = null, a !== null) {
      a = a.next;
      do
        t.status = "rejected", t.reason = u, l0(t), t = t.next;
      while (t !== a);
    }
    l.action = null;
  }
  function l0(l) {
    l = l.listeners;
    for (var t = 0; t < l.length; t++) (0, l[t])();
  }
  function t0(l, t) {
    return t;
  }
  function u0(l, t) {
    if (w) {
      var u = sl.formState;
      if (u !== null) {
        l: {
          var a = Q;
          if (w) {
            if (ol) {
              t: {
                for (var e = ol, n = mt; e.nodeType !== 8; ) {
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
                ol = ht(
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
      lastRenderedReducer: t0,
      lastRenderedState: t
    }, u.queue = a, u = E0.bind(
      null,
      Q,
      a
    ), a.dispatch = u, a = uc(!1), n = sc.bind(
      null,
      Q,
      !1,
      a.queue
    ), a = Bl(), e = {
      state: t,
      dispatch: null,
      action: l,
      pending: null
    }, a.queue = e, u = sm.bind(
      null,
      Q,
      e,
      n,
      u
    ), e.dispatch = u, a.memoizedState = l, [t, u, !1];
  }
  function a0(l) {
    var t = rl();
    return e0(t, nl, l);
  }
  function e0(l, t, u) {
    if (t = lc(
      l,
      t,
      t0
    )[0], l = fn(Xt)[0], typeof t == "object" && t !== null && typeof t.then == "function")
      try {
        var a = Pa(t);
      } catch (f) {
        throw f === sa ? Fe : f;
      }
    else a = t;
    t = rl();
    var e = t.queue, n = e.dispatch;
    return u !== t.memoizedState && (Q.flags |= 2048, da(
      9,
      { destroy: void 0 },
      om.bind(null, e, u),
      null
    )), [a, n, l];
  }
  function om(l, t) {
    l.action = t;
  }
  function n0(l) {
    var t = rl(), u = nl;
    if (u !== null)
      return e0(t, u, l);
    rl(), t = t.memoizedState, u = rl();
    var a = u.queue.dispatch;
    return u.memoizedState = l, [t, a, !1];
  }
  function da(l, t, u, a) {
    return l = { tag: l, create: u, deps: a, inst: t, next: null }, t = Q.updateQueue, t === null && (t = en(), Q.updateQueue = t), u = t.lastEffect, u === null ? t.lastEffect = l.next = l : (a = u.next, u.next = l, l.next = a, t.lastEffect = l), l;
  }
  function f0() {
    return rl().memoizedState;
  }
  function cn(l, t, u, a) {
    var e = Bl();
    Q.flags |= l, e.memoizedState = da(
      1 | t,
      { destroy: void 0 },
      u,
      a === void 0 ? null : a
    );
  }
  function sn(l, t, u, a) {
    var e = rl();
    a = a === void 0 ? null : a;
    var n = e.memoizedState.inst;
    nl !== null && a !== null && Wf(a, nl.memoizedState.deps) ? e.memoizedState = da(t, n, u, a) : (Q.flags |= l, e.memoizedState = da(
      1 | t,
      n,
      u,
      a
    ));
  }
  function c0(l, t) {
    cn(8390656, 8, l, t);
  }
  function ec(l, t) {
    sn(2048, 8, l, t);
  }
  function ym(l) {
    Q.flags |= 4;
    var t = Q.updateQueue;
    if (t === null)
      t = en(), Q.updateQueue = t, t.events = [l];
    else {
      var u = t.events;
      u === null ? t.events = [l] : u.push(l);
    }
  }
  function i0(l) {
    var t = rl().memoizedState;
    return ym({ ref: t, nextImpl: l }), function() {
      if ((I & 2) !== 0) throw Error(v(440));
      return t.impl.apply(void 0, arguments);
    };
  }
  function s0(l, t) {
    return sn(4, 2, l, t);
  }
  function o0(l, t) {
    return sn(4, 4, l, t);
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
    u = u != null ? u.concat([l]) : null, sn(4, 4, y0.bind(null, t, l), u);
  }
  function nc() {
  }
  function m0(l, t) {
    var u = rl();
    t = t === void 0 ? null : t;
    var a = u.memoizedState;
    return t !== null && Wf(t, a[1]) ? a[0] : (u.memoizedState = [l, t], l);
  }
  function d0(l, t) {
    var u = rl();
    t = t === void 0 ? null : t;
    var a = u.memoizedState;
    if (t !== null && Wf(t, a[1]))
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
  function fc(l, t, u) {
    return u === void 0 || (Gt & 1073741824) !== 0 && (V & 261930) === 0 ? l.memoizedState = t : (l.memoizedState = u, l = So(), Q.lanes |= l, su |= l, u);
  }
  function h0(l, t, u, a) {
    return Il(u, t) ? u : ya.current !== null ? (l = fc(l, u, a), Il(l, t) || (El = !0), l) : (Gt & 42) === 0 || (Gt & 1073741824) !== 0 && (V & 261930) === 0 ? (El = !0, l.memoizedState = u) : (l = So(), Q.lanes |= l, su |= l, t);
  }
  function S0(l, t, u, a, e) {
    var n = M.p;
    M.p = n !== 0 && 8 > n ? n : 8;
    var f = b.T, c = {};
    b.T = c, sc(l, !1, t, u);
    try {
      var i = e(), h = b.S;
      if (h !== null && h(c, i), i !== null && typeof i == "object" && typeof i.then == "function") {
        var r = fm(
          i,
          a
        );
        le(
          l,
          t,
          r,
          et(l)
        );
      } else
        le(
          l,
          t,
          a,
          et(l)
        );
    } catch (E) {
      le(
        l,
        t,
        { then: function() {
        }, status: "rejected", reason: E },
        et()
      );
    } finally {
      M.p = n, f !== null && c.types !== null && (f.types = c.types), b.T = f;
    }
  }
  function vm() {
  }
  function cc(l, t, u, a) {
    if (l.tag !== 5) throw Error(v(476));
    var e = g0(l).queue;
    S0(
      l,
      e,
      t,
      B,
      u === null ? vm : function() {
        return r0(l), u(a);
      }
    );
  }
  function g0(l) {
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
        lastRenderedReducer: Xt,
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
        lastRenderedReducer: Xt,
        lastRenderedState: u
      },
      next: null
    }, l.memoizedState = t, l = l.alternate, l !== null && (l.memoizedState = t), t;
  }
  function r0(l) {
    var t = g0(l);
    t.next === null && (t = l.alternate.memoizedState), le(
      l,
      t.next.queue,
      {},
      et()
    );
  }
  function ic() {
    return Dl(ge);
  }
  function b0() {
    return rl().memoizedState;
  }
  function T0() {
    return rl().memoizedState;
  }
  function mm(l) {
    for (var t = l.return; t !== null; ) {
      switch (t.tag) {
        case 24:
        case 3:
          var u = et();
          l = au(u);
          var a = eu(t, l, u);
          a !== null && (Jl(a, t, u), $a(a, t, u)), t = { cache: Gf() }, l.payload = t;
          return;
      }
      t = t.return;
    }
  }
  function dm(l, t, u) {
    var a = et();
    u = {
      lane: a,
      revertLane: 0,
      gesture: null,
      action: u,
      hasEagerState: !1,
      eagerState: null,
      next: null
    }, on(l) ? z0(t, u) : (u = Mf(l, t, u, a), u !== null && (Jl(u, l, a), A0(u, t, a)));
  }
  function E0(l, t, u) {
    var a = et();
    le(l, t, u, a);
  }
  function le(l, t, u, a) {
    var e = {
      lane: a,
      revertLane: 0,
      gesture: null,
      action: u,
      hasEagerState: !1,
      eagerState: null,
      next: null
    };
    if (on(l)) z0(t, e);
    else {
      var n = l.alternate;
      if (l.lanes === 0 && (n === null || n.lanes === 0) && (n = t.lastRenderedReducer, n !== null))
        try {
          var f = t.lastRenderedState, c = n(f, u);
          if (e.hasEagerState = !0, e.eagerState = c, Il(c, f))
            return xe(l, t, e, 0), sl === null && Le(), !1;
        } catch {
        } finally {
        }
      if (u = Mf(l, t, e, a), u !== null)
        return Jl(u, l, a), A0(u, t, a), !0;
    }
    return !1;
  }
  function sc(l, t, u, a) {
    if (a = {
      lane: 2,
      revertLane: Zc(),
      gesture: null,
      action: a,
      hasEagerState: !1,
      eagerState: null,
      next: null
    }, on(l)) {
      if (t) throw Error(v(479));
    } else
      t = Mf(
        l,
        u,
        a,
        2
      ), t !== null && Jl(t, l, 2);
  }
  function on(l) {
    var t = l.alternate;
    return l === Q || t !== null && t === Q;
  }
  function z0(l, t) {
    va = un = !0;
    var u = l.pending;
    u === null ? t.next = t : (t.next = u.next, u.next = t), l.pending = t;
  }
  function A0(l, t, u) {
    if ((u & 4194048) !== 0) {
      var a = t.lanes;
      a &= l.pendingLanes, u |= a, t.lanes = u, Di(l, u);
    }
  }
  var te = {
    readContext: Dl,
    use: nn,
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
  te.useEffectEvent = ml;
  var _0 = {
    readContext: Dl,
    use: nn,
    useCallback: function(l, t) {
      return Bl().memoizedState = [
        l,
        t === void 0 ? null : t
      ], l;
    },
    useContext: Dl,
    useEffect: c0,
    useImperativeHandle: function(l, t, u) {
      u = u != null ? u.concat([l]) : null, cn(
        4194308,
        4,
        y0.bind(null, t, l),
        u
      );
    },
    useLayoutEffect: function(l, t) {
      return cn(4194308, 4, l, t);
    },
    useInsertionEffect: function(l, t) {
      cn(4, 2, l, t);
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
      }, a.queue = l, l = l.dispatch = dm.bind(
        null,
        Q,
        l
      ), [a.memoizedState, l];
    },
    useRef: function(l) {
      var t = Bl();
      return l = { current: l }, t.memoizedState = l;
    },
    useState: function(l) {
      l = uc(l);
      var t = l.queue, u = E0.bind(null, Q, t);
      return t.dispatch = u, [l.memoizedState, u];
    },
    useDebugValue: nc,
    useDeferredValue: function(l, t) {
      var u = Bl();
      return fc(u, l, t);
    },
    useTransition: function() {
      var l = uc(!1);
      return l = S0.bind(
        null,
        Q,
        l.queue,
        !0,
        !1
      ), Bl().memoizedState = l, [!1, l];
    },
    useSyncExternalStore: function(l, t, u) {
      var a = Q, e = Bl();
      if (w) {
        if (u === void 0)
          throw Error(v(407));
        u = u();
      } else {
        if (u = t(), sl === null)
          throw Error(v(349));
        (V & 127) !== 0 || Ks(a, t, u);
      }
      e.memoizedState = u;
      var n = { value: u, getSnapshot: t };
      return e.queue = n, c0(ws.bind(null, a, n, l), [
        l
      ]), a.flags |= 2048, da(
        9,
        { destroy: void 0 },
        Js.bind(
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
        var u = Ot, a = _t;
        u = (a & ~(1 << 32 - kl(a) - 1)).toString(32) + u, t = "_" + t + "R_" + u, u = an++, 0 < u && (t += "H" + u.toString(32)), t += "_";
      } else
        u = cm++, t = "_" + t + "r_" + u.toString(32) + "_";
      return l.memoizedState = t;
    },
    useHostTransitionStatus: ic,
    useFormState: u0,
    useActionState: u0,
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
      return t.queue = u, t = sc.bind(
        null,
        Q,
        !0,
        u
      ), u.dispatch = t, [l, t];
    },
    useMemoCache: Pf,
    useCacheRefresh: function() {
      return Bl().memoizedState = mm.bind(
        null,
        Q
      );
    },
    useEffectEvent: function(l) {
      var t = Bl(), u = { impl: l };
      return t.memoizedState = u, function() {
        if ((I & 2) !== 0)
          throw Error(v(440));
        return u.impl.apply(void 0, arguments);
      };
    }
  }, oc = {
    readContext: Dl,
    use: nn,
    useCallback: m0,
    useContext: Dl,
    useEffect: ec,
    useImperativeHandle: v0,
    useInsertionEffect: s0,
    useLayoutEffect: o0,
    useMemo: d0,
    useReducer: fn,
    useRef: f0,
    useState: function() {
      return fn(Xt);
    },
    useDebugValue: nc,
    useDeferredValue: function(l, t) {
      var u = rl();
      return h0(
        u,
        nl.memoizedState,
        l,
        t
      );
    },
    useTransition: function() {
      var l = fn(Xt)[0], t = rl().memoizedState;
      return [
        typeof l == "boolean" ? l : Pa(l),
        t
      ];
    },
    useSyncExternalStore: Vs,
    useId: b0,
    useHostTransitionStatus: ic,
    useFormState: a0,
    useActionState: a0,
    useOptimistic: function(l, t) {
      var u = rl();
      return Fs(u, nl, l, t);
    },
    useMemoCache: Pf,
    useCacheRefresh: T0
  };
  oc.useEffectEvent = i0;
  var O0 = {
    readContext: Dl,
    use: nn,
    useCallback: m0,
    useContext: Dl,
    useEffect: ec,
    useImperativeHandle: v0,
    useInsertionEffect: s0,
    useLayoutEffect: o0,
    useMemo: d0,
    useReducer: tc,
    useRef: f0,
    useState: function() {
      return tc(Xt);
    },
    useDebugValue: nc,
    useDeferredValue: function(l, t) {
      var u = rl();
      return nl === null ? fc(u, l, t) : h0(
        u,
        nl.memoizedState,
        l,
        t
      );
    },
    useTransition: function() {
      var l = tc(Xt)[0], t = rl().memoizedState;
      return [
        typeof l == "boolean" ? l : Pa(l),
        t
      ];
    },
    useSyncExternalStore: Vs,
    useId: b0,
    useHostTransitionStatus: ic,
    useFormState: n0,
    useActionState: n0,
    useOptimistic: function(l, t) {
      var u = rl();
      return nl !== null ? Fs(u, nl, l, t) : (u.baseState = l, [l, u.queue.dispatch]);
    },
    useMemoCache: Pf,
    useCacheRefresh: T0
  };
  O0.useEffectEvent = i0;
  function yc(l, t, u, a) {
    t = l.memoizedState, u = u(a, t), u = u == null ? t : q({}, t, u), l.memoizedState = u, l.lanes === 0 && (l.updateQueue.baseState = u);
  }
  var vc = {
    enqueueSetState: function(l, t, u) {
      l = l._reactInternals;
      var a = et(), e = au(a);
      e.payload = t, u != null && (e.callback = u), t = eu(l, e, a), t !== null && (Jl(t, l, a), $a(t, l, a));
    },
    enqueueReplaceState: function(l, t, u) {
      l = l._reactInternals;
      var a = et(), e = au(a);
      e.tag = 1, e.payload = t, u != null && (e.callback = u), t = eu(l, e, a), t !== null && (Jl(t, l, a), $a(t, l, a));
    },
    enqueueForceUpdate: function(l, t) {
      l = l._reactInternals;
      var u = et(), a = au(u);
      a.tag = 2, t != null && (a.callback = t), t = eu(l, a, u), t !== null && (Jl(t, l, u), $a(t, l, u));
    }
  };
  function M0(l, t, u, a, e, n, f) {
    return l = l.stateNode, typeof l.shouldComponentUpdate == "function" ? l.shouldComponentUpdate(a, n, f) : t.prototype && t.prototype.isPureReactComponent ? !Za(u, a) || !Za(e, n) : !0;
  }
  function p0(l, t, u, a) {
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
    Ze(l);
  }
  function U0(l) {
    console.error(l);
  }
  function R0(l) {
    Ze(l);
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
  function N0(l, t, u) {
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
  function mc(l, t, u) {
    return u = au(u), u.tag = 3, u.payload = { element: null }, u.callback = function() {
      yn(l, t);
    }, u;
  }
  function H0(l) {
    return l = au(l), l.tag = 3, l;
  }
  function C0(l, t, u, a) {
    var e = u.type.getDerivedStateFromError;
    if (typeof e == "function") {
      var n = a.value;
      l.payload = function() {
        return e(n);
      }, l.callback = function() {
        N0(t, u, a);
      };
    }
    var f = u.stateNode;
    f !== null && typeof f.componentDidCatch == "function" && (l.callback = function() {
      N0(t, u, a), typeof e != "function" && (ou === null ? ou = /* @__PURE__ */ new Set([this]) : ou.add(this));
      var c = a.stack;
      this.componentDidCatch(a.value, {
        componentStack: c !== null ? c : ""
      });
    });
  }
  function hm(l, t, u, a, e) {
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
            return dt === null ? An() : u.alternate === null && dl === 0 && (dl = 3), u.flags &= -257, u.flags |= 65536, u.lanes = e, a === ke ? u.flags |= 16384 : (t = u.updateQueue, t === null ? u.updateQueue = /* @__PURE__ */ new Set([a]) : t.add(a), Xc(l, a, e)), !1;
          case 22:
            return u.flags |= 65536, a === ke ? u.flags |= 16384 : (t = u.updateQueue, t === null ? (t = {
              transitions: null,
              markerInstances: null,
              retryQueue: /* @__PURE__ */ new Set([a])
            }, u.updateQueue = t) : (u = t.retryQueue, u === null ? t.retryQueue = /* @__PURE__ */ new Set([a]) : u.add(a)), Xc(l, a, e)), !1;
        }
        throw Error(v(435, u.tag));
      }
      return Xc(l, a, e), An(), !1;
    }
    if (w)
      return t = lt.current, t !== null ? ((t.flags & 65536) === 0 && (t.flags |= 256), t.flags |= 65536, t.lanes = e, a !== Hf && (l = Error(v(422), { cause: a }), Va(ot(l, u)))) : (a !== Hf && (t = Error(v(423), {
        cause: a
      }), Va(
        ot(t, u)
      )), l = l.current.alternate, l.flags |= 65536, e &= -e, l.lanes |= e, a = ot(a, u), e = mc(
        l.stateNode,
        a,
        e
      ), xf(l, e), dl !== 4 && (dl = 2)), !1;
    var n = Error(v(520), { cause: a });
    if (n = ot(n, u), se === null ? se = [n] : se.push(n), dl !== 4 && (dl = 2), t === null) return !0;
    a = ot(a, u), u = t;
    do {
      switch (u.tag) {
        case 3:
          return u.flags |= 65536, l = e & -e, u.lanes |= l, l = mc(u.stateNode, a, l), xf(u, l), !1;
        case 1:
          if (t = u.type, n = u.stateNode, (u.flags & 128) === 0 && (typeof t.getDerivedStateFromError == "function" || n !== null && typeof n.componentDidCatch == "function" && (ou === null || !ou.has(n))))
            return u.flags |= 65536, e &= -e, u.lanes |= e, e = H0(e), C0(
              e,
              l,
              u,
              a
            ), xf(u, e), !1;
      }
      u = u.return;
    } while (u !== null);
    return !1;
  }
  var dc = Error(v(461)), El = !1;
  function Ul(l, t, u, a) {
    t.child = l === null ? Gs(t, null, u, a) : Gu(
      t,
      l.child,
      u,
      a
    );
  }
  function q0(l, t, u, a, e) {
    u = u.render;
    var n = t.ref;
    if ("ref" in a) {
      var f = {};
      for (var c in a)
        c !== "ref" && (f[c] = a[c]);
    } else f = a;
    return Cu(t), a = $f(
      l,
      t,
      u,
      f,
      n,
      e
    ), c = Ff(), l !== null && !El ? (kf(l, t, e), Qt(l, t, e)) : (w && c && Rf(t), t.flags |= 1, Ul(l, t, a, e), t.child);
  }
  function B0(l, t, u, a, e) {
    if (l === null) {
      var n = u.type;
      return typeof n == "function" && !pf(n) && n.defaultProps === void 0 && u.compare === null ? (t.tag = 15, t.type = n, Y0(
        l,
        t,
        n,
        a,
        e
      )) : (l = Ke(
        u.type,
        null,
        a,
        t,
        t.mode,
        e
      ), l.ref = t.ref, l.return = t, t.child = l);
    }
    if (n = l.child, !zc(l, e)) {
      var f = n.memoizedProps;
      if (u = u.compare, u = u !== null ? u : Za, u(f, a) && l.ref === t.ref)
        return Qt(l, t, e);
    }
    return t.flags |= 1, l = Ct(n, a), l.ref = t.ref, l.return = t, t.child = l;
  }
  function Y0(l, t, u, a, e) {
    if (l !== null) {
      var n = l.memoizedProps;
      if (Za(n, a) && l.ref === t.ref)
        if (El = !1, t.pendingProps = a = n, zc(l, e))
          (l.flags & 131072) !== 0 && (El = !0);
        else
          return t.lanes = l.lanes, Qt(l, t, e);
    }
    return hc(
      l,
      t,
      u,
      a,
      e
    );
  }
  function G0(l, t, u, a) {
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
        return X0(
          l,
          t,
          n,
          u,
          a
        );
      }
      if ((u & 536870912) !== 0)
        t.memoizedState = { baseLanes: 0, cachePool: null }, l !== null && $e(
          t,
          n !== null ? n.cachePool : null
        ), n !== null ? js(t, n) : Kf(), Zs(t);
      else
        return a = t.lanes = 536870912, X0(
          l,
          t,
          n !== null ? n.baseLanes | u : u,
          u,
          a
        );
    } else
      n !== null ? ($e(t, n.cachePool), js(t, n), fu(), t.memoizedState = null) : (l !== null && $e(t, null), Kf(), fu());
    return Ul(l, t, e, u), t.child;
  }
  function ue(l, t) {
    return l !== null && l.tag === 22 || t.stateNode !== null || (t.stateNode = {
      _visibility: 1,
      _pendingMarkers: null,
      _retryCache: null,
      _transitions: null
    }), t.sibling;
  }
  function X0(l, t, u, a, e) {
    var n = Qf();
    return n = n === null ? null : { parent: bl._currentValue, pool: n }, t.memoizedState = {
      baseLanes: u,
      cachePool: n
    }, l !== null && $e(t, null), Kf(), Zs(t), l !== null && fa(l, t, a, !0), t.childLanes = e, null;
  }
  function vn(l, t) {
    return t = dn(
      { mode: t.mode, children: t.children },
      l.mode
    ), t.ref = l.ref, l.child = t, t.return = l, t;
  }
  function Q0(l, t, u) {
    return Gu(t, l.child, null, u), l = vn(t, t.pendingProps), l.flags |= 2, tt(t), t.memoizedState = null, l;
  }
  function Sm(l, t, u) {
    var a = t.pendingProps, e = (t.flags & 128) !== 0;
    if (t.flags &= -129, l === null) {
      if (w) {
        if (a.mode === "hidden")
          return l = vn(t, a), t.lanes = 536870912, ue(null, l);
        if (wf(t), (l = ol) ? (l = Io(
          l,
          mt
        ), l = l !== null && l.data === "&" ? l : null, l !== null && (t.memoizedState = {
          dehydrated: l,
          treeContext: It !== null ? { id: _t, overflow: Ot } : null,
          retryLane: 536870912,
          hydrationErrors: null
        }, u = zs(l), u.return = t, t.child = u, pl = t, ol = null)) : l = null, l === null) throw lu(t);
        return t.lanes = 536870912, null;
      }
      return vn(t, a);
    }
    var n = l.memoizedState;
    if (n !== null) {
      var f = n.dehydrated;
      if (wf(t), e)
        if (t.flags & 256)
          t.flags &= -257, t = Q0(
            l,
            t,
            u
          );
        else if (t.memoizedState !== null)
          t.child = l.child, t.flags |= 128, t = null;
        else throw Error(v(558));
      else if (El || fa(l, t, u, !1), e = (u & l.childLanes) !== 0, El || e) {
        if (a = sl, a !== null && (f = Ui(a, u), f !== 0 && f !== n.retryLane))
          throw n.retryLane = f, Uu(l, f), Jl(a, l, f), dc;
        An(), t = Q0(
          l,
          t,
          u
        );
      } else
        l = n.treeContext, ol = ht(f.nextSibling), pl = t, w = !0, Pt = null, mt = !1, l !== null && Os(t, l), t = vn(t, a), t.flags |= 4096;
      return t;
    }
    return l = Ct(l.child, {
      mode: a.mode,
      children: a.children
    }), l.ref = t.ref, t.child = l, l.return = t, l;
  }
  function mn(l, t) {
    var u = t.ref;
    if (u === null)
      l !== null && l.ref !== null && (t.flags |= 4194816);
    else {
      if (typeof u != "function" && typeof u != "object")
        throw Error(v(284));
      (l === null || l.ref !== u) && (t.flags |= 4194816);
    }
  }
  function hc(l, t, u, a, e) {
    return Cu(t), u = $f(
      l,
      t,
      u,
      a,
      void 0,
      e
    ), a = Ff(), l !== null && !El ? (kf(l, t, e), Qt(l, t, e)) : (w && a && Rf(t), t.flags |= 1, Ul(l, t, u, e), t.child);
  }
  function j0(l, t, u, a, e, n) {
    return Cu(t), t.updateQueue = null, u = xs(
      t,
      a,
      u,
      e
    ), Ls(l), a = Ff(), l !== null && !El ? (kf(l, t, n), Qt(l, t, n)) : (w && a && Rf(t), t.flags |= 1, Ul(l, t, u, n), t.child);
  }
  function Z0(l, t, u, a, e) {
    if (Cu(t), t.stateNode === null) {
      var n = ua, f = u.contextType;
      typeof f == "object" && f !== null && (n = Dl(f)), n = new u(a, n), t.memoizedState = n.state !== null && n.state !== void 0 ? n.state : null, n.updater = vc, t.stateNode = n, n._reactInternals = t, n = t.stateNode, n.props = a, n.state = t.memoizedState, n.refs = {}, Zf(t), f = u.contextType, n.context = typeof f == "object" && f !== null ? Dl(f) : ua, n.state = t.memoizedState, f = u.getDerivedStateFromProps, typeof f == "function" && (yc(
        t,
        u,
        f,
        a
      ), n.state = t.memoizedState), typeof u.getDerivedStateFromProps == "function" || typeof n.getSnapshotBeforeUpdate == "function" || typeof n.UNSAFE_componentWillMount != "function" && typeof n.componentWillMount != "function" || (f = n.state, typeof n.componentWillMount == "function" && n.componentWillMount(), typeof n.UNSAFE_componentWillMount == "function" && n.UNSAFE_componentWillMount(), f !== n.state && vc.enqueueReplaceState(n, n.state, null), ka(t, a, n, e), Fa(), n.state = t.memoizedState), typeof n.componentDidMount == "function" && (t.flags |= 4194308), a = !0;
    } else if (l === null) {
      n = t.stateNode;
      var c = t.memoizedProps, i = Qu(u, c);
      n.props = i;
      var h = n.context, r = u.contextType;
      f = ua, typeof r == "object" && r !== null && (f = Dl(r));
      var E = u.getDerivedStateFromProps;
      r = typeof E == "function" || typeof n.getSnapshotBeforeUpdate == "function", c = t.pendingProps !== c, r || typeof n.UNSAFE_componentWillReceiveProps != "function" && typeof n.componentWillReceiveProps != "function" || (c || h !== f) && p0(
        t,
        n,
        a,
        f
      ), uu = !1;
      var S = t.memoizedState;
      n.state = S, ka(t, a, n, e), Fa(), h = t.memoizedState, c || S !== h || uu ? (typeof E == "function" && (yc(
        t,
        u,
        E,
        a
      ), h = t.memoizedState), (i = uu || M0(
        t,
        u,
        i,
        a,
        S,
        h,
        f
      )) ? (r || typeof n.UNSAFE_componentWillMount != "function" && typeof n.componentWillMount != "function" || (typeof n.componentWillMount == "function" && n.componentWillMount(), typeof n.UNSAFE_componentWillMount == "function" && n.UNSAFE_componentWillMount()), typeof n.componentDidMount == "function" && (t.flags |= 4194308)) : (typeof n.componentDidMount == "function" && (t.flags |= 4194308), t.memoizedProps = a, t.memoizedState = h), n.props = a, n.state = h, n.context = f, a = i) : (typeof n.componentDidMount == "function" && (t.flags |= 4194308), a = !1);
    } else {
      n = t.stateNode, Lf(l, t), f = t.memoizedProps, r = Qu(u, f), n.props = r, E = t.pendingProps, S = n.context, h = u.contextType, i = ua, typeof h == "object" && h !== null && (i = Dl(h)), c = u.getDerivedStateFromProps, (h = typeof c == "function" || typeof n.getSnapshotBeforeUpdate == "function") || typeof n.UNSAFE_componentWillReceiveProps != "function" && typeof n.componentWillReceiveProps != "function" || (f !== E || S !== i) && p0(
        t,
        n,
        a,
        i
      ), uu = !1, S = t.memoizedState, n.state = S, ka(t, a, n, e), Fa();
      var g = t.memoizedState;
      f !== E || S !== g || uu || l !== null && l.dependencies !== null && we(l.dependencies) ? (typeof c == "function" && (yc(
        t,
        u,
        c,
        a
      ), g = t.memoizedState), (r = uu || M0(
        t,
        u,
        r,
        a,
        S,
        g,
        i
      ) || l !== null && l.dependencies !== null && we(l.dependencies)) ? (h || typeof n.UNSAFE_componentWillUpdate != "function" && typeof n.componentWillUpdate != "function" || (typeof n.componentWillUpdate == "function" && n.componentWillUpdate(a, g, i), typeof n.UNSAFE_componentWillUpdate == "function" && n.UNSAFE_componentWillUpdate(
        a,
        g,
        i
      )), typeof n.componentDidUpdate == "function" && (t.flags |= 4), typeof n.getSnapshotBeforeUpdate == "function" && (t.flags |= 1024)) : (typeof n.componentDidUpdate != "function" || f === l.memoizedProps && S === l.memoizedState || (t.flags |= 4), typeof n.getSnapshotBeforeUpdate != "function" || f === l.memoizedProps && S === l.memoizedState || (t.flags |= 1024), t.memoizedProps = a, t.memoizedState = g), n.props = a, n.state = g, n.context = i, a = r) : (typeof n.componentDidUpdate != "function" || f === l.memoizedProps && S === l.memoizedState || (t.flags |= 4), typeof n.getSnapshotBeforeUpdate != "function" || f === l.memoizedProps && S === l.memoizedState || (t.flags |= 1024), a = !1);
    }
    return n = a, mn(l, t), a = (t.flags & 128) !== 0, n || a ? (n = t.stateNode, u = a && typeof u.getDerivedStateFromError != "function" ? null : n.render(), t.flags |= 1, l !== null && a ? (t.child = Gu(
      t,
      l.child,
      null,
      e
    ), t.child = Gu(
      t,
      null,
      u,
      e
    )) : Ul(l, t, u, e), t.memoizedState = n.state, l = t.child) : l = Qt(
      l,
      t,
      e
    ), l;
  }
  function L0(l, t, u, a) {
    return Nu(), t.flags |= 256, Ul(l, t, u, a), t.child;
  }
  var Sc = {
    dehydrated: null,
    treeContext: null,
    retryLane: 0,
    hydrationErrors: null
  };
  function gc(l) {
    return { baseLanes: l, cachePool: Ns() };
  }
  function rc(l, t, u) {
    return l = l !== null ? l.childLanes & ~u : 0, t && (l |= at), l;
  }
  function x0(l, t, u) {
    var a = t.pendingProps, e = !1, n = (t.flags & 128) !== 0, f;
    if ((f = n) || (f = l !== null && l.memoizedState === null ? !1 : (gl.current & 2) !== 0), f && (e = !0, t.flags &= -129), f = (t.flags & 32) !== 0, t.flags &= -33, l === null) {
      if (w) {
        if (e ? nu(t) : fu(), (l = ol) ? (l = Io(
          l,
          mt
        ), l = l !== null && l.data !== "&" ? l : null, l !== null && (t.memoizedState = {
          dehydrated: l,
          treeContext: It !== null ? { id: _t, overflow: Ot } : null,
          retryLane: 536870912,
          hydrationErrors: null
        }, u = zs(l), u.return = t, t.child = u, pl = t, ol = null)) : l = null, l === null) throw lu(t);
        return li(l) ? t.lanes = 32 : t.lanes = 536870912, null;
      }
      var c = a.children;
      return a = a.fallback, e ? (fu(), e = t.mode, c = dn(
        { mode: "hidden", children: c },
        e
      ), a = Ru(
        a,
        e,
        u,
        null
      ), c.return = t, a.return = t, c.sibling = a, t.child = c, a = t.child, a.memoizedState = gc(u), a.childLanes = rc(
        l,
        f,
        u
      ), t.memoizedState = Sc, ue(null, a)) : (nu(t), bc(t, c));
    }
    var i = l.memoizedState;
    if (i !== null && (c = i.dehydrated, c !== null)) {
      if (n)
        t.flags & 256 ? (nu(t), t.flags &= -257, t = Tc(
          l,
          t,
          u
        )) : t.memoizedState !== null ? (fu(), t.child = l.child, t.flags |= 128, t = null) : (fu(), c = a.fallback, e = t.mode, a = dn(
          { mode: "visible", children: a.children },
          e
        ), c = Ru(
          c,
          e,
          u,
          null
        ), c.flags |= 2, a.return = t, c.return = t, a.sibling = c, t.child = a, Gu(
          t,
          l.child,
          null,
          u
        ), a = t.child, a.memoizedState = gc(u), a.childLanes = rc(
          l,
          f,
          u
        ), t.memoizedState = Sc, t = ue(null, a));
      else if (nu(t), li(c)) {
        if (f = c.nextSibling && c.nextSibling.dataset, f) var h = f.dgst;
        f = h, a = Error(v(419)), a.stack = "", a.digest = f, Va({ value: a, source: null, stack: null }), t = Tc(
          l,
          t,
          u
        );
      } else if (El || fa(l, t, u, !1), f = (u & l.childLanes) !== 0, El || f) {
        if (f = sl, f !== null && (a = Ui(f, u), a !== 0 && a !== i.retryLane))
          throw i.retryLane = a, Uu(l, a), Jl(f, l, a), dc;
        Pc(c) || An(), t = Tc(
          l,
          t,
          u
        );
      } else
        Pc(c) ? (t.flags |= 192, t.child = l.child, t = null) : (l = i.treeContext, ol = ht(
          c.nextSibling
        ), pl = t, w = !0, Pt = null, mt = !1, l !== null && Os(t, l), t = bc(
          t,
          a.children
        ), t.flags |= 4096);
      return t;
    }
    return e ? (fu(), c = a.fallback, e = t.mode, i = l.child, h = i.sibling, a = Ct(i, {
      mode: "hidden",
      children: a.children
    }), a.subtreeFlags = i.subtreeFlags & 65011712, h !== null ? c = Ct(
      h,
      c
    ) : (c = Ru(
      c,
      e,
      u,
      null
    ), c.flags |= 2), c.return = t, a.return = t, a.sibling = c, t.child = a, ue(null, a), a = t.child, c = l.child.memoizedState, c === null ? c = gc(u) : (e = c.cachePool, e !== null ? (i = bl._currentValue, e = e.parent !== i ? { parent: i, pool: i } : e) : e = Ns(), c = {
      baseLanes: c.baseLanes | u,
      cachePool: e
    }), a.memoizedState = c, a.childLanes = rc(
      l,
      f,
      u
    ), t.memoizedState = Sc, ue(l.child, a)) : (nu(t), u = l.child, l = u.sibling, u = Ct(u, {
      mode: "visible",
      children: a.children
    }), u.return = t, u.sibling = null, l !== null && (f = t.deletions, f === null ? (t.deletions = [l], t.flags |= 16) : f.push(l)), t.child = u, t.memoizedState = null, u);
  }
  function bc(l, t) {
    return t = dn(
      { mode: "visible", children: t },
      l.mode
    ), t.return = l, l.child = t;
  }
  function dn(l, t) {
    return l = Pl(22, l, null, t), l.lanes = 0, l;
  }
  function Tc(l, t, u) {
    return Gu(t, l.child, null, u), l = bc(
      t,
      t.pendingProps.children
    ), l.flags |= 2, t.memoizedState = null, l;
  }
  function V0(l, t, u) {
    l.lanes |= t;
    var a = l.alternate;
    a !== null && (a.lanes |= t), Bf(l.return, t, u);
  }
  function Ec(l, t, u, a, e, n) {
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
  function K0(l, t, u) {
    var a = t.pendingProps, e = a.revealOrder, n = a.tail;
    a = a.children;
    var f = gl.current, c = (f & 2) !== 0;
    if (c ? (f = f & 1 | 2, t.flags |= 128) : f &= 1, p(gl, f), Ul(l, t, a, u), a = w ? xa : 0, !c && l !== null && (l.flags & 128) !== 0)
      l: for (l = t.child; l !== null; ) {
        if (l.tag === 13)
          l.memoizedState !== null && V0(l, u, t);
        else if (l.tag === 19)
          V0(l, u, t);
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
          l = u.alternate, l !== null && tn(l) === null && (e = u), u = u.sibling;
        u = e, u === null ? (e = t.child, t.child = null) : (e = u.sibling, u.sibling = null), Ec(
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
          if (l = e.alternate, l !== null && tn(l) === null) {
            t.child = e;
            break;
          }
          l = e.sibling, e.sibling = u, u = e, e = l;
        }
        Ec(
          t,
          !0,
          u,
          null,
          n,
          a
        );
        break;
      case "together":
        Ec(
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
  function Qt(l, t, u) {
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
      throw Error(v(153));
    if (t.child !== null) {
      for (l = t.child, u = Ct(l, l.pendingProps), t.child = u, u.return = t; l.sibling !== null; )
        l = l.sibling, u = u.sibling = Ct(l, l.pendingProps), u.return = t;
      u.sibling = null;
    }
    return t.child;
  }
  function zc(l, t) {
    return (l.lanes & t) !== 0 ? !0 : (l = l.dependencies, !!(l !== null && we(l)));
  }
  function gm(l, t, u) {
    switch (t.tag) {
      case 3:
        ql(t, t.stateNode.containerInfo), tu(t, bl, l.memoizedState.cache), Nu();
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
          return t.flags |= 128, wf(t), null;
        break;
      case 13:
        var a = t.memoizedState;
        if (a !== null)
          return a.dehydrated !== null ? (nu(t), t.flags |= 128, null) : (u & t.child.childLanes) !== 0 ? x0(l, t, u) : (nu(t), l = Qt(
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
            return K0(
              l,
              t,
              u
            );
          t.flags |= 128;
        }
        if (e = t.memoizedState, e !== null && (e.rendering = null, e.tail = null, e.lastEffect = null), p(gl, gl.current), a) break;
        return null;
      case 22:
        return t.lanes = 0, G0(
          l,
          t,
          u,
          t.pendingProps
        );
      case 24:
        tu(t, bl, l.memoizedState.cache);
    }
    return Qt(l, t, u);
  }
  function J0(l, t, u) {
    if (l !== null)
      if (l.memoizedProps !== t.pendingProps)
        El = !0;
      else {
        if (!zc(l, u) && (t.flags & 128) === 0)
          return El = !1, gm(
            l,
            t,
            u
          );
        El = (l.flags & 131072) !== 0;
      }
    else
      El = !1, w && (t.flags & 1048576) !== 0 && _s(t, xa, t.index);
    switch (t.lanes = 0, t.tag) {
      case 16:
        l: {
          var a = t.pendingProps;
          if (l = Bu(t.elementType), t.type = l, typeof l == "function")
            pf(l) ? (a = Qu(l, a), t.tag = 1, t = Z0(
              null,
              t,
              l,
              a,
              u
            )) : (t.tag = 0, t = hc(
              null,
              t,
              l,
              a,
              u
            ));
          else {
            if (l != null) {
              var e = l.$$typeof;
              if (e === ft) {
                t.tag = 11, t = q0(
                  null,
                  t,
                  l,
                  a,
                  u
                );
                break l;
              } else if (e === J) {
                t.tag = 14, t = B0(
                  null,
                  t,
                  l,
                  a,
                  u
                );
                break l;
              }
            }
            throw t = Ut(l) || l, Error(v(306, t, ""));
          }
        }
        return t;
      case 0:
        return hc(
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
        ), Z0(
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
          ), l === null) throw Error(v(387));
          a = t.pendingProps;
          var n = t.memoizedState;
          e = n.element, Lf(l, t), ka(t, a, null, u);
          var f = t.memoizedState;
          if (a = f.cache, tu(t, bl, a), a !== n.cache && Yf(
            t,
            [bl],
            u,
            !0
          ), Fa(), a = f.element, n.isDehydrated)
            if (n = {
              element: a,
              isDehydrated: !1,
              cache: f.cache
            }, t.updateQueue.baseState = n, t.memoizedState = n, t.flags & 256) {
              t = L0(
                l,
                t,
                a,
                u
              );
              break l;
            } else if (a !== e) {
              e = ot(
                Error(v(424)),
                t
              ), Va(e), t = L0(
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
              for (ol = ht(l.firstChild), pl = t, w = !0, Pt = null, mt = !0, u = Gs(
                t,
                null,
                a,
                u
              ), t.child = u; u; )
                u.flags = u.flags & -3 | 4096, u = u.sibling;
            }
          else {
            if (Nu(), a === e) {
              t = Qt(
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
        return mn(l, t), l === null ? (u = ey(
          t.type,
          null,
          t.pendingProps,
          null
        )) ? t.memoizedState = u : w || (u = t.type, l = t.pendingProps, a = Rn(
          Z.current
        ).createElement(u), a[Ml] = t, a[jl] = l, Rl(a, u, l), _l(a), t.stateNode = a) : t.memoizedState = ey(
          t.type,
          l.memoizedProps,
          t.pendingProps,
          l.memoizedState
        ), null;
      case 27:
        return Da(t), l === null && w && (a = t.stateNode = ty(
          t.type,
          t.pendingProps,
          Z.current
        ), pl = t, mt = !0, e = ol, du(t.type) ? (ti = e, ol = ht(a.firstChild)) : ol = e), Ul(
          l,
          t,
          t.pendingProps.children,
          u
        ), mn(l, t), l === null && (t.flags |= 4194304), t.child;
      case 5:
        return l === null && w && ((e = a = ol) && (a = wm(
          a,
          t.type,
          t.pendingProps,
          mt
        ), a !== null ? (t.stateNode = a, pl = t, ol = ht(a.firstChild), mt = !1, e = !0) : e = !1), e || lu(t)), Da(t), e = t.type, n = t.pendingProps, f = l !== null ? l.memoizedProps : null, a = n.children, Fc(e, n) ? a = null : f !== null && Fc(e, f) && (t.flags |= 32), t.memoizedState !== null && (e = $f(
          l,
          t,
          im,
          null,
          null,
          u
        ), ge._currentValue = e), mn(l, t), Ul(l, t, a, u), t.child;
      case 6:
        return l === null && w && ((l = u = ol) && (u = Wm(
          u,
          t.pendingProps,
          mt
        ), u !== null ? (t.stateNode = u, pl = t, ol = null, l = !0) : l = !1), l || lu(t)), null;
      case 13:
        return x0(l, t, u);
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
        return q0(
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
        return e = t.type._context, a = t.pendingProps.children, Cu(t), e = Dl(e), a = a(e), t.flags |= 1, Ul(l, t, a, u), t.child;
      case 14:
        return B0(
          l,
          t,
          t.type,
          t.pendingProps,
          u
        );
      case 15:
        return Y0(
          l,
          t,
          t.type,
          t.pendingProps,
          u
        );
      case 19:
        return K0(l, t, u);
      case 31:
        return Sm(l, t, u);
      case 22:
        return G0(
          l,
          t,
          u,
          t.pendingProps
        );
      case 24:
        return Cu(t), a = Dl(bl), l === null ? (e = Qf(), e === null && (e = sl, n = Gf(), e.pooledCache = n, n.refCount++, n !== null && (e.pooledCacheLanes |= u), e = n), t.memoizedState = { parent: a, cache: e }, Zf(t), tu(t, bl, e)) : ((l.lanes & u) !== 0 && (Lf(l, t), ka(t, null, null, u), Fa()), e = l.memoizedState, n = t.memoizedState, e.parent !== a ? (e = { parent: a, cache: a }, t.memoizedState = e, t.lanes === 0 && (t.memoizedState = t.updateQueue.baseState = e), tu(t, bl, a)) : (a = n.cache, tu(t, bl, a), a !== e.cache && Yf(
          t,
          [bl],
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
    throw Error(v(156, t.tag));
  }
  function jt(l) {
    l.flags |= 4;
  }
  function Ac(l, t, u, a, e) {
    if ((t = (l.mode & 32) !== 0) && (t = !1), t) {
      if (l.flags |= 16777216, (e & 335544128) === e)
        if (l.stateNode.complete) l.flags |= 8192;
        else if (To()) l.flags |= 8192;
        else
          throw Yu = ke, jf;
    } else l.flags &= -16777217;
  }
  function w0(l, t) {
    if (t.type !== "stylesheet" || (t.state.loading & 4) !== 0)
      l.flags &= -16777217;
    else if (l.flags |= 16777216, !sy(t))
      if (To()) l.flags |= 8192;
      else
        throw Yu = ke, jf;
  }
  function hn(l, t) {
    t !== null && (l.flags |= 4), l.flags & 16384 && (t = l.tag !== 22 ? Mi() : 536870912, l.lanes |= t, ra |= t);
  }
  function ae(l, t) {
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
  function yl(l) {
    var t = l.alternate !== null && l.alternate.child === l.child, u = 0, a = 0;
    if (t)
      for (var e = l.child; e !== null; )
        u |= e.lanes | e.childLanes, a |= e.subtreeFlags & 65011712, a |= e.flags & 65011712, e.return = l, e = e.sibling;
    else
      for (e = l.child; e !== null; )
        u |= e.lanes | e.childLanes, a |= e.subtreeFlags, a |= e.flags, e.return = l, e = e.sibling;
    return l.subtreeFlags |= a, l.childLanes = u, t;
  }
  function rm(l, t, u) {
    var a = t.pendingProps;
    switch (Nf(t), t.tag) {
      case 16:
      case 15:
      case 0:
      case 11:
      case 7:
      case 8:
      case 12:
      case 9:
      case 14:
        return yl(t), null;
      case 1:
        return yl(t), null;
      case 3:
        return u = t.stateNode, a = null, l !== null && (a = l.memoizedState.cache), t.memoizedState.cache !== a && (t.flags |= 2048), Yt(bl), Sl(), u.pendingContext && (u.context = u.pendingContext, u.pendingContext = null), (l === null || l.child === null) && (na(t) ? jt(t) : l === null || l.memoizedState.isDehydrated && (t.flags & 256) === 0 || (t.flags |= 1024, Cf())), yl(t), null;
      case 26:
        var e = t.type, n = t.memoizedState;
        return l === null ? (jt(t), n !== null ? (yl(t), w0(t, n)) : (yl(t), Ac(
          t,
          e,
          null,
          a,
          u
        ))) : n ? n !== l.memoizedState ? (jt(t), yl(t), w0(t, n)) : (yl(t), t.flags &= -16777217) : (l = l.memoizedProps, l !== a && jt(t), yl(t), Ac(
          t,
          e,
          l,
          a,
          u
        )), null;
      case 27:
        if (Oe(t), u = Z.current, e = t.type, l !== null && t.stateNode != null)
          l.memoizedProps !== a && jt(t);
        else {
          if (!a) {
            if (t.stateNode === null)
              throw Error(v(166));
            return yl(t), null;
          }
          l = U.current, na(t) ? Ms(t) : (l = ty(e, a, u), t.stateNode = l, jt(t));
        }
        return yl(t), null;
      case 5:
        if (Oe(t), e = t.type, l !== null && t.stateNode != null)
          l.memoizedProps !== a && jt(t);
        else {
          if (!a) {
            if (t.stateNode === null)
              throw Error(v(166));
            return yl(t), null;
          }
          if (n = U.current, na(t))
            Ms(t);
          else {
            var f = Rn(
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
            l: switch (Rl(n, e, a), e) {
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
            a && jt(t);
          }
        }
        return yl(t), Ac(
          t,
          t.type,
          l === null ? null : l.memoizedProps,
          t.pendingProps,
          u
        ), null;
      case 6:
        if (l && t.stateNode != null)
          l.memoizedProps !== a && jt(t);
        else {
          if (typeof a != "string" && t.stateNode === null)
            throw Error(v(166));
          if (l = Z.current, na(t)) {
            if (l = t.stateNode, u = t.memoizedProps, a = null, e = pl, e !== null)
              switch (e.tag) {
                case 27:
                case 5:
                  a = e.memoizedProps;
              }
            l[Ml] = t, l = !!(l.nodeValue === u || a !== null && a.suppressHydrationWarning === !0 || Vo(l.nodeValue, u)), l || lu(t, !0);
          } else
            l = Rn(l).createTextNode(
              a
            ), l[Ml] = t, t.stateNode = l;
        }
        return yl(t), null;
      case 31:
        if (u = t.memoizedState, l === null || l.memoizedState !== null) {
          if (a = na(t), u !== null) {
            if (l === null) {
              if (!a) throw Error(v(318));
              if (l = t.memoizedState, l = l !== null ? l.dehydrated : null, !l) throw Error(v(557));
              l[Ml] = t;
            } else
              Nu(), (t.flags & 128) === 0 && (t.memoizedState = null), t.flags |= 4;
            yl(t), l = !1;
          } else
            u = Cf(), l !== null && l.memoizedState !== null && (l.memoizedState.hydrationErrors = u), l = !0;
          if (!l)
            return t.flags & 256 ? (tt(t), t) : (tt(t), null);
          if ((t.flags & 128) !== 0)
            throw Error(v(558));
        }
        return yl(t), null;
      case 13:
        if (a = t.memoizedState, l === null || l.memoizedState !== null && l.memoizedState.dehydrated !== null) {
          if (e = na(t), a !== null && a.dehydrated !== null) {
            if (l === null) {
              if (!e) throw Error(v(318));
              if (e = t.memoizedState, e = e !== null ? e.dehydrated : null, !e) throw Error(v(317));
              e[Ml] = t;
            } else
              Nu(), (t.flags & 128) === 0 && (t.memoizedState = null), t.flags |= 4;
            yl(t), e = !1;
          } else
            e = Cf(), l !== null && l.memoizedState !== null && (l.memoizedState.hydrationErrors = e), e = !0;
          if (!e)
            return t.flags & 256 ? (tt(t), t) : (tt(t), null);
        }
        return tt(t), (t.flags & 128) !== 0 ? (t.lanes = u, t) : (u = a !== null, l = l !== null && l.memoizedState !== null, u && (a = t.child, e = null, a.alternate !== null && a.alternate.memoizedState !== null && a.alternate.memoizedState.cachePool !== null && (e = a.alternate.memoizedState.cachePool.pool), n = null, a.memoizedState !== null && a.memoizedState.cachePool !== null && (n = a.memoizedState.cachePool.pool), n !== e && (a.flags |= 2048)), u !== l && u && (t.child.flags |= 8192), hn(t, t.updateQueue), yl(t), null);
      case 4:
        return Sl(), l === null && Kc(t.stateNode.containerInfo), yl(t), null;
      case 10:
        return Yt(t.type), yl(t), null;
      case 19:
        if (z(gl), a = t.memoizedState, a === null) return yl(t), null;
        if (e = (t.flags & 128) !== 0, n = a.rendering, n === null)
          if (e) ae(a, !1);
          else {
            if (dl !== 0 || l !== null && (l.flags & 128) !== 0)
              for (l = t.child; l !== null; ) {
                if (n = tn(l), n !== null) {
                  for (t.flags |= 128, ae(a, !1), l = n.updateQueue, t.updateQueue = l, hn(t, l), t.subtreeFlags = 0, l = u, u = t.child; u !== null; )
                    Es(u, l), u = u.sibling;
                  return p(
                    gl,
                    gl.current & 1 | 2
                  ), w && qt(t, a.treeForkCount), t.child;
                }
                l = l.sibling;
              }
            a.tail !== null && $l() > Tn && (t.flags |= 128, e = !0, ae(a, !1), t.lanes = 4194304);
          }
        else {
          if (!e)
            if (l = tn(n), l !== null) {
              if (t.flags |= 128, e = !0, l = l.updateQueue, t.updateQueue = l, hn(t, l), ae(a, !0), a.tail === null && a.tailMode === "hidden" && !n.alternate && !w)
                return yl(t), null;
            } else
              2 * $l() - a.renderingStartTime > Tn && u !== 536870912 && (t.flags |= 128, e = !0, ae(a, !1), t.lanes = 4194304);
          a.isBackwards ? (n.sibling = t.child, t.child = n) : (l = a.last, l !== null ? l.sibling = n : t.child = n, a.last = n);
        }
        return a.tail !== null ? (l = a.tail, a.rendering = l, a.tail = l.sibling, a.renderingStartTime = $l(), l.sibling = null, u = gl.current, p(
          gl,
          e ? u & 1 | 2 : u & 1
        ), w && qt(t, a.treeForkCount), l) : (yl(t), null);
      case 22:
      case 23:
        return tt(t), Jf(), a = t.memoizedState !== null, l !== null ? l.memoizedState !== null !== a && (t.flags |= 8192) : a && (t.flags |= 8192), a ? (u & 536870912) !== 0 && (t.flags & 128) === 0 && (yl(t), t.subtreeFlags & 6 && (t.flags |= 8192)) : yl(t), u = t.updateQueue, u !== null && hn(t, u.retryQueue), u = null, l !== null && l.memoizedState !== null && l.memoizedState.cachePool !== null && (u = l.memoizedState.cachePool.pool), a = null, t.memoizedState !== null && t.memoizedState.cachePool !== null && (a = t.memoizedState.cachePool.pool), a !== u && (t.flags |= 2048), l !== null && z(qu), null;
      case 24:
        return u = null, l !== null && (u = l.memoizedState.cache), t.memoizedState.cache !== u && (t.flags |= 2048), Yt(bl), yl(t), null;
      case 25:
        return null;
      case 30:
        return null;
    }
    throw Error(v(156, t.tag));
  }
  function bm(l, t) {
    switch (Nf(t), t.tag) {
      case 1:
        return l = t.flags, l & 65536 ? (t.flags = l & -65537 | 128, t) : null;
      case 3:
        return Yt(bl), Sl(), l = t.flags, (l & 65536) !== 0 && (l & 128) === 0 ? (t.flags = l & -65537 | 128, t) : null;
      case 26:
      case 27:
      case 5:
        return Oe(t), null;
      case 31:
        if (t.memoizedState !== null) {
          if (tt(t), t.alternate === null)
            throw Error(v(340));
          Nu();
        }
        return l = t.flags, l & 65536 ? (t.flags = l & -65537 | 128, t) : null;
      case 13:
        if (tt(t), l = t.memoizedState, l !== null && l.dehydrated !== null) {
          if (t.alternate === null)
            throw Error(v(340));
          Nu();
        }
        return l = t.flags, l & 65536 ? (t.flags = l & -65537 | 128, t) : null;
      case 19:
        return z(gl), null;
      case 4:
        return Sl(), null;
      case 10:
        return Yt(t.type), null;
      case 22:
      case 23:
        return tt(t), Jf(), l !== null && z(qu), l = t.flags, l & 65536 ? (t.flags = l & -65537 | 128, t) : null;
      case 24:
        return Yt(bl), null;
      case 25:
        return null;
      default:
        return null;
    }
  }
  function W0(l, t) {
    switch (Nf(t), t.tag) {
      case 3:
        Yt(bl), Sl();
        break;
      case 26:
      case 27:
      case 5:
        Oe(t);
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
        z(gl);
        break;
      case 10:
        Yt(t.type);
        break;
      case 22:
      case 23:
        tt(t), Jf(), l !== null && z(qu);
        break;
      case 24:
        Yt(bl);
    }
  }
  function ee(l, t) {
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
      ul(t, t.return, c);
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
              var i = u, h = c;
              try {
                h();
              } catch (r) {
                ul(
                  e,
                  i,
                  r
                );
              }
            }
          }
          a = a.next;
        } while (a !== n);
      }
    } catch (r) {
      ul(t, t.return, r);
    }
  }
  function $0(l) {
    var t = l.updateQueue;
    if (t !== null) {
      var u = l.stateNode;
      try {
        Qs(t, u);
      } catch (a) {
        ul(l, l.return, a);
      }
    }
  }
  function F0(l, t, u) {
    u.props = Qu(
      l.type,
      l.memoizedProps
    ), u.state = l.memoizedState;
    try {
      u.componentWillUnmount();
    } catch (a) {
      ul(l, t, a);
    }
  }
  function ne(l, t) {
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
      ul(l, t, e);
    }
  }
  function Mt(l, t) {
    var u = l.ref, a = l.refCleanup;
    if (u !== null)
      if (typeof a == "function")
        try {
          a();
        } catch (e) {
          ul(l, t, e);
        } finally {
          l.refCleanup = null, l = l.alternate, l != null && (l.refCleanup = null);
        }
      else if (typeof u == "function")
        try {
          u(null);
        } catch (e) {
          ul(l, t, e);
        }
      else u.current = null;
  }
  function k0(l) {
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
      ul(l, l.return, e);
    }
  }
  function _c(l, t, u) {
    try {
      var a = l.stateNode;
      Zm(a, l.type, u, t), a[jl] = t;
    } catch (e) {
      ul(l, l.return, e);
    }
  }
  function I0(l) {
    return l.tag === 5 || l.tag === 3 || l.tag === 26 || l.tag === 27 && du(l.type) || l.tag === 4;
  }
  function Oc(l) {
    l: for (; ; ) {
      for (; l.sibling === null; ) {
        if (l.return === null || I0(l.return)) return null;
        l = l.return;
      }
      for (l.sibling.return = l.return, l = l.sibling; l.tag !== 5 && l.tag !== 6 && l.tag !== 18; ) {
        if (l.tag === 27 && du(l.type) || l.flags & 2 || l.child === null || l.tag === 4) continue l;
        l.child.return = l, l = l.child;
      }
      if (!(l.flags & 2)) return l.stateNode;
    }
  }
  function Mc(l, t, u) {
    var a = l.tag;
    if (a === 5 || a === 6)
      l = l.stateNode, t ? (u.nodeType === 9 ? u.body : u.nodeName === "HTML" ? u.ownerDocument.body : u).insertBefore(l, t) : (t = u.nodeType === 9 ? u.body : u.nodeName === "HTML" ? u.ownerDocument.body : u, t.appendChild(l), u = u._reactRootContainer, u != null || t.onclick !== null || (t.onclick = Nt));
    else if (a !== 4 && (a === 27 && du(l.type) && (u = l.stateNode, t = null), l = l.child, l !== null))
      for (Mc(l, t, u), l = l.sibling; l !== null; )
        Mc(l, t, u), l = l.sibling;
  }
  function Sn(l, t, u) {
    var a = l.tag;
    if (a === 5 || a === 6)
      l = l.stateNode, t ? u.insertBefore(l, t) : u.appendChild(l);
    else if (a !== 4 && (a === 27 && du(l.type) && (u = l.stateNode), l = l.child, l !== null))
      for (Sn(l, t, u), l = l.sibling; l !== null; )
        Sn(l, t, u), l = l.sibling;
  }
  function P0(l) {
    var t = l.stateNode, u = l.memoizedProps;
    try {
      for (var a = l.type, e = t.attributes; e.length; )
        t.removeAttributeNode(e[0]);
      Rl(t, a, u), t[Ml] = l, t[jl] = u;
    } catch (n) {
      ul(l, l.return, n);
    }
  }
  var Zt = !1, zl = !1, pc = !1, lo = typeof WeakSet == "function" ? WeakSet : Set, Ol = null;
  function Tm(l, t) {
    if (l = l.containerInfo, Wc = Gn, l = vs(l), Tf(l)) {
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
            var f = 0, c = -1, i = -1, h = 0, r = 0, E = l, S = null;
            t: for (; ; ) {
              for (var g; E !== u || e !== 0 && E.nodeType !== 3 || (c = f + e), E !== n || a !== 0 && E.nodeType !== 3 || (i = f + a), E.nodeType === 3 && (f += E.nodeValue.length), (g = E.firstChild) !== null; )
                S = E, E = g;
              for (; ; ) {
                if (E === l) break t;
                if (S === u && ++h === e && (c = f), S === n && ++r === a && (i = f), (g = E.nextSibling) !== null) break;
                E = S, S = E.parentNode;
              }
              E = g;
            }
            u = c === -1 || i === -1 ? null : { start: c, end: i };
          } else u = null;
        }
      u = u || { start: 0, end: 0 };
    } else u = null;
    for ($c = { focusedElem: l, selectionRange: u }, Gn = !1, Ol = t; Ol !== null; )
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
                  var D = Qu(
                    u.type,
                    e
                  );
                  l = a.getSnapshotBeforeUpdate(
                    D,
                    n
                  ), a.__reactInternalSnapshotBeforeUpdate = l;
                } catch (C) {
                  ul(
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
                  Ic(l);
                else if (u === 1)
                  switch (l.nodeName) {
                    case "HEAD":
                    case "HTML":
                    case "BODY":
                      Ic(l);
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
              if ((l & 1024) !== 0) throw Error(v(163));
          }
          if (l = t.sibling, l !== null) {
            l.return = t.return, Ol = l;
            break;
          }
          Ol = t.return;
        }
  }
  function to(l, t, u) {
    var a = u.flags;
    switch (u.tag) {
      case 0:
      case 11:
      case 15:
        xt(l, u), a & 4 && ee(5, u);
        break;
      case 1:
        if (xt(l, u), a & 4)
          if (l = u.stateNode, t === null)
            try {
              l.componentDidMount();
            } catch (f) {
              ul(u, u.return, f);
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
              ul(
                u,
                u.return,
                f
              );
            }
          }
        a & 64 && $0(u), a & 512 && ne(u, u.return);
        break;
      case 3:
        if (xt(l, u), a & 64 && (l = u.updateQueue, l !== null)) {
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
            Qs(l, t);
          } catch (f) {
            ul(u, u.return, f);
          }
        }
        break;
      case 27:
        t === null && a & 4 && P0(u);
      case 26:
      case 5:
        xt(l, u), t === null && a & 4 && k0(u), a & 512 && ne(u, u.return);
        break;
      case 12:
        xt(l, u);
        break;
      case 31:
        xt(l, u), a & 4 && eo(l, u);
        break;
      case 13:
        xt(l, u), a & 4 && no(l, u), a & 64 && (l = u.memoizedState, l !== null && (l = l.dehydrated, l !== null && (u = Um.bind(
          null,
          u
        ), $m(l, u))));
        break;
      case 22:
        if (a = u.memoizedState !== null || Zt, !a) {
          t = t !== null && t.memoizedState !== null || zl, e = Zt;
          var n = zl;
          Zt = a, (zl = t) && !n ? Vt(
            l,
            u,
            (u.subtreeFlags & 8772) !== 0
          ) : xt(l, u), Zt = e, zl = n;
        }
        break;
      case 30:
        break;
      default:
        xt(l, u);
    }
  }
  function uo(l) {
    var t = l.alternate;
    t !== null && (l.alternate = null, uo(t)), l.child = null, l.deletions = null, l.sibling = null, l.tag === 5 && (t = l.stateNode, t !== null && uf(t)), l.stateNode = null, l.return = null, l.dependencies = null, l.memoizedProps = null, l.memoizedState = null, l.pendingProps = null, l.stateNode = null, l.updateQueue = null;
  }
  var vl = null, Ll = !1;
  function Lt(l, t, u) {
    for (u = u.child; u !== null; )
      ao(l, t, u), u = u.sibling;
  }
  function ao(l, t, u) {
    if (Fl && typeof Fl.onCommitFiberUnmount == "function")
      try {
        Fl.onCommitFiberUnmount(Ua, u);
      } catch {
      }
    switch (u.tag) {
      case 26:
        zl || Mt(u, t), Lt(
          l,
          t,
          u
        ), u.memoizedState ? u.memoizedState.count-- : u.stateNode && (u = u.stateNode, u.parentNode.removeChild(u));
        break;
      case 27:
        zl || Mt(u, t);
        var a = vl, e = Ll;
        du(u.type) && (vl = u.stateNode, Ll = !1), Lt(
          l,
          t,
          u
        ), de(u.stateNode), vl = a, Ll = e;
        break;
      case 5:
        zl || Mt(u, t);
      case 6:
        if (a = vl, e = Ll, vl = null, Lt(
          l,
          t,
          u
        ), vl = a, Ll = e, vl !== null)
          if (Ll)
            try {
              (vl.nodeType === 9 ? vl.body : vl.nodeName === "HTML" ? vl.ownerDocument.body : vl).removeChild(u.stateNode);
            } catch (n) {
              ul(
                u,
                t,
                n
              );
            }
          else
            try {
              vl.removeChild(u.stateNode);
            } catch (n) {
              ul(
                u,
                t,
                n
              );
            }
        break;
      case 18:
        vl !== null && (Ll ? (l = vl, Fo(
          l.nodeType === 9 ? l.body : l.nodeName === "HTML" ? l.ownerDocument.body : l,
          u.stateNode
        ), Ma(l)) : Fo(vl, u.stateNode));
        break;
      case 4:
        a = vl, e = Ll, vl = u.stateNode.containerInfo, Ll = !0, Lt(
          l,
          t,
          u
        ), vl = a, Ll = e;
        break;
      case 0:
      case 11:
      case 14:
      case 15:
        cu(2, u, t), zl || cu(4, u, t), Lt(
          l,
          t,
          u
        );
        break;
      case 1:
        zl || (Mt(u, t), a = u.stateNode, typeof a.componentWillUnmount == "function" && F0(
          u,
          t,
          a
        )), Lt(
          l,
          t,
          u
        );
        break;
      case 21:
        Lt(
          l,
          t,
          u
        );
        break;
      case 22:
        zl = (a = zl) || u.memoizedState !== null, Lt(
          l,
          t,
          u
        ), zl = a;
        break;
      default:
        Lt(
          l,
          t,
          u
        );
    }
  }
  function eo(l, t) {
    if (t.memoizedState === null && (l = t.alternate, l !== null && (l = l.memoizedState, l !== null))) {
      l = l.dehydrated;
      try {
        Ma(l);
      } catch (u) {
        ul(t, t.return, u);
      }
    }
  }
  function no(l, t) {
    if (t.memoizedState === null && (l = t.alternate, l !== null && (l = l.memoizedState, l !== null && (l = l.dehydrated, l !== null))))
      try {
        Ma(l);
      } catch (u) {
        ul(t, t.return, u);
      }
  }
  function Em(l) {
    switch (l.tag) {
      case 31:
      case 13:
      case 19:
        var t = l.stateNode;
        return t === null && (t = l.stateNode = new lo()), t;
      case 22:
        return l = l.stateNode, t = l._retryCache, t === null && (t = l._retryCache = new lo()), t;
      default:
        throw Error(v(435, l.tag));
    }
  }
  function gn(l, t) {
    var u = Em(l);
    t.forEach(function(a) {
      if (!u.has(a)) {
        u.add(a);
        var e = Rm.bind(null, l, a);
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
                vl = c.stateNode, Ll = !1;
                break l;
              }
              break;
            case 5:
              vl = c.stateNode, Ll = !1;
              break l;
            case 3:
            case 4:
              vl = c.stateNode.containerInfo, Ll = !0;
              break l;
          }
          c = c.return;
        }
        if (vl === null) throw Error(v(160));
        ao(n, f, e), vl = null, Ll = !1, n = e.alternate, n !== null && (n.return = null), e.return = null;
      }
    if (t.subtreeFlags & 13886)
      for (t = t.child; t !== null; )
        fo(t, l), t = t.sibling;
  }
  var bt = null;
  function fo(l, t) {
    var u = l.alternate, a = l.flags;
    switch (l.tag) {
      case 0:
      case 11:
      case 14:
      case 15:
        xl(t, l), Vl(l), a & 4 && (cu(3, l, l.return), ee(3, l), cu(5, l, l.return));
        break;
      case 1:
        xl(t, l), Vl(l), a & 512 && (zl || u === null || Mt(u, u.return)), a & 64 && Zt && (l = l.updateQueue, l !== null && (a = l.callbacks, a !== null && (u = l.shared.hiddenCallbacks, l.shared.hiddenCallbacks = u === null ? a : u.concat(a))));
        break;
      case 26:
        var e = bt;
        if (xl(t, l), Vl(l), a & 512 && (zl || u === null || Mt(u, u.return)), a & 4) {
          var n = u !== null ? u.memoizedState : null;
          if (a = l.memoizedState, u === null)
            if (a === null)
              if (l.stateNode === null) {
                l: {
                  a = l.type, u = l.memoizedProps, e = e.ownerDocument || e;
                  t: switch (a) {
                    case "title":
                      n = e.getElementsByTagName("title")[0], (!n || n[Ha] || n[Ml] || n.namespaceURI === "http://www.w3.org/2000/svg" || n.hasAttribute("itemprop")) && (n = e.createElement(a), e.head.insertBefore(
                        n,
                        e.querySelector("head > title")
                      )), Rl(n, a, u), n[Ml] = l, _l(n), a = n;
                      break l;
                    case "link":
                      var f = cy(
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
                      n = e.createElement(a), Rl(n, a, u), e.head.appendChild(n);
                      break;
                    case "meta":
                      if (f = cy(
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
                      n = e.createElement(a), Rl(n, a, u), e.head.appendChild(n);
                      break;
                    default:
                      throw Error(v(468, a));
                  }
                  n[Ml] = l, _l(n), a = n;
                }
                l.stateNode = a;
              } else
                iy(
                  e,
                  l.type,
                  l.stateNode
                );
            else
              l.stateNode = fy(
                e,
                a,
                l.memoizedProps
              );
          else
            n !== a ? (n === null ? u.stateNode !== null && (u = u.stateNode, u.parentNode.removeChild(u)) : n.count--, a === null ? iy(
              e,
              l.type,
              l.stateNode
            ) : fy(
              e,
              a,
              l.memoizedProps
            )) : a === null && l.stateNode !== null && _c(
              l,
              l.memoizedProps,
              u.memoizedProps
            );
        }
        break;
      case 27:
        xl(t, l), Vl(l), a & 512 && (zl || u === null || Mt(u, u.return)), u !== null && a & 4 && _c(
          l,
          l.memoizedProps,
          u.memoizedProps
        );
        break;
      case 5:
        if (xl(t, l), Vl(l), a & 512 && (zl || u === null || Mt(u, u.return)), l.flags & 32) {
          e = l.stateNode;
          try {
            $u(e, "");
          } catch (D) {
            ul(l, l.return, D);
          }
        }
        a & 4 && l.stateNode != null && (e = l.memoizedProps, _c(
          l,
          e,
          u !== null ? u.memoizedProps : e
        )), a & 1024 && (pc = !0);
        break;
      case 6:
        if (xl(t, l), Vl(l), a & 4) {
          if (l.stateNode === null)
            throw Error(v(162));
          a = l.memoizedProps, u = l.stateNode;
          try {
            u.nodeValue = a;
          } catch (D) {
            ul(l, l.return, D);
          }
        }
        break;
      case 3:
        if (Cn = null, e = bt, bt = Nn(t.containerInfo), xl(t, l), bt = e, Vl(l), a & 4 && u !== null && u.memoizedState.isDehydrated)
          try {
            Ma(t.containerInfo);
          } catch (D) {
            ul(l, l.return, D);
          }
        pc && (pc = !1, co(l));
        break;
      case 4:
        a = bt, bt = Nn(
          l.stateNode.containerInfo
        ), xl(t, l), Vl(l), bt = a;
        break;
      case 12:
        xl(t, l), Vl(l);
        break;
      case 31:
        xl(t, l), Vl(l), a & 4 && (a = l.updateQueue, a !== null && (l.updateQueue = null, gn(l, a)));
        break;
      case 13:
        xl(t, l), Vl(l), l.child.flags & 8192 && l.memoizedState !== null != (u !== null && u.memoizedState !== null) && (bn = $l()), a & 4 && (a = l.updateQueue, a !== null && (l.updateQueue = null, gn(l, a)));
        break;
      case 22:
        e = l.memoizedState !== null;
        var i = u !== null && u.memoizedState !== null, h = Zt, r = zl;
        if (Zt = h || e, zl = r || i, xl(t, l), zl = r, Zt = h, Vl(l), a & 8192)
          l: for (t = l.stateNode, t._visibility = e ? t._visibility & -2 : t._visibility | 1, e && (u === null || i || Zt || zl || ju(l)), u = null, t = l; ; ) {
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
                } catch (D) {
                  ul(i, i.return, D);
                }
              }
            } else if (t.tag === 6) {
              if (u === null) {
                i = t;
                try {
                  i.stateNode.nodeValue = e ? "" : i.memoizedProps;
                } catch (D) {
                  ul(i, i.return, D);
                }
              }
            } else if (t.tag === 18) {
              if (u === null) {
                i = t;
                try {
                  var g = i.stateNode;
                  e ? ko(g, !0) : ko(i.stateNode, !1);
                } catch (D) {
                  ul(i, i.return, D);
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
        a & 4 && (a = l.updateQueue, a !== null && (u = a.retryQueue, u !== null && (a.retryQueue = null, gn(l, u))));
        break;
      case 19:
        xl(t, l), Vl(l), a & 4 && (a = l.updateQueue, a !== null && (l.updateQueue = null, gn(l, a)));
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
          if (I0(a)) {
            u = a;
            break;
          }
          a = a.return;
        }
        if (u == null) throw Error(v(160));
        switch (u.tag) {
          case 27:
            var e = u.stateNode, n = Oc(l);
            Sn(l, n, e);
            break;
          case 5:
            var f = u.stateNode;
            u.flags & 32 && ($u(f, ""), u.flags &= -33);
            var c = Oc(l);
            Sn(l, c, f);
            break;
          case 3:
          case 4:
            var i = u.stateNode.containerInfo, h = Oc(l);
            Mc(
              l,
              h,
              i
            );
            break;
          default:
            throw Error(v(161));
        }
      } catch (r) {
        ul(l, l.return, r);
      }
      l.flags &= -3;
    }
    t & 4096 && (l.flags &= -4097);
  }
  function co(l) {
    if (l.subtreeFlags & 1024)
      for (l = l.child; l !== null; ) {
        var t = l;
        co(t), t.tag === 5 && t.flags & 1024 && t.stateNode.reset(), l = l.sibling;
      }
  }
  function xt(l, t) {
    if (t.subtreeFlags & 8772)
      for (t = t.child; t !== null; )
        to(l, t.alternate, t), t = t.sibling;
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
          Mt(t, t.return);
          var u = t.stateNode;
          typeof u.componentWillUnmount == "function" && F0(
            t,
            t.return,
            u
          ), ju(t);
          break;
        case 27:
          de(t.stateNode);
        case 26:
        case 5:
          Mt(t, t.return), ju(t);
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
  function Vt(l, t, u) {
    for (u = u && (t.subtreeFlags & 8772) !== 0, t = t.child; t !== null; ) {
      var a = t.alternate, e = l, n = t, f = n.flags;
      switch (n.tag) {
        case 0:
        case 11:
        case 15:
          Vt(
            e,
            n,
            u
          ), ee(4, n);
          break;
        case 1:
          if (Vt(
            e,
            n,
            u
          ), a = n, e = a.stateNode, typeof e.componentDidMount == "function")
            try {
              e.componentDidMount();
            } catch (h) {
              ul(a, a.return, h);
            }
          if (a = n, e = a.updateQueue, e !== null) {
            var c = a.stateNode;
            try {
              var i = e.shared.hiddenCallbacks;
              if (i !== null)
                for (e.shared.hiddenCallbacks = null, e = 0; e < i.length; e++)
                  Xs(i[e], c);
            } catch (h) {
              ul(a, a.return, h);
            }
          }
          u && f & 64 && $0(n), ne(n, n.return);
          break;
        case 27:
          P0(n);
        case 26:
        case 5:
          Vt(
            e,
            n,
            u
          ), u && a === null && f & 4 && k0(n), ne(n, n.return);
          break;
        case 12:
          Vt(
            e,
            n,
            u
          );
          break;
        case 31:
          Vt(
            e,
            n,
            u
          ), u && f & 4 && eo(e, n);
          break;
        case 13:
          Vt(
            e,
            n,
            u
          ), u && f & 4 && no(e, n);
          break;
        case 22:
          n.memoizedState === null && Vt(
            e,
            n,
            u
          ), ne(n, n.return);
          break;
        case 30:
          break;
        default:
          Vt(
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
    l !== null && l.memoizedState !== null && l.memoizedState.cachePool !== null && (u = l.memoizedState.cachePool.pool), l = null, t.memoizedState !== null && t.memoizedState.cachePool !== null && (l = t.memoizedState.cachePool.pool), l !== u && (l != null && l.refCount++, u != null && Ka(u));
  }
  function Uc(l, t) {
    l = null, t.alternate !== null && (l = t.alternate.memoizedState.cache), t = t.memoizedState.cache, t !== l && (t.refCount++, l != null && Ka(l));
  }
  function Tt(l, t, u, a) {
    if (t.subtreeFlags & 10256)
      for (t = t.child; t !== null; )
        io(
          l,
          t,
          u,
          a
        ), t = t.sibling;
  }
  function io(l, t, u, a) {
    var e = t.flags;
    switch (t.tag) {
      case 0:
      case 11:
      case 15:
        Tt(
          l,
          t,
          u,
          a
        ), e & 2048 && ee(9, t);
        break;
      case 1:
        Tt(
          l,
          t,
          u,
          a
        );
        break;
      case 3:
        Tt(
          l,
          t,
          u,
          a
        ), e & 2048 && (l = null, t.alternate !== null && (l = t.alternate.memoizedState.cache), t = t.memoizedState.cache, t !== l && (t.refCount++, l != null && Ka(l)));
        break;
      case 12:
        if (e & 2048) {
          Tt(
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
            ul(t, t.return, i);
          }
        } else
          Tt(
            l,
            t,
            u,
            a
          );
        break;
      case 31:
        Tt(
          l,
          t,
          u,
          a
        );
        break;
      case 13:
        Tt(
          l,
          t,
          u,
          a
        );
        break;
      case 23:
        break;
      case 22:
        n = t.stateNode, f = t.alternate, t.memoizedState !== null ? n._visibility & 2 ? Tt(
          l,
          t,
          u,
          a
        ) : fe(l, t) : n._visibility & 2 ? Tt(
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
        Tt(
          l,
          t,
          u,
          a
        ), e & 2048 && Uc(t.alternate, t);
        break;
      default:
        Tt(
          l,
          t,
          u,
          a
        );
    }
  }
  function ha(l, t, u, a, e) {
    for (e = e && ((t.subtreeFlags & 10256) !== 0 || !1), t = t.child; t !== null; ) {
      var n = l, f = t, c = u, i = a, h = f.flags;
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
          ), ee(8, f);
          break;
        case 23:
          break;
        case 22:
          var r = f.stateNode;
          f.memoizedState !== null ? r._visibility & 2 ? ha(
            n,
            f,
            c,
            i,
            e
          ) : fe(
            n,
            f
          ) : (r._visibility |= 2, ha(
            n,
            f,
            c,
            i,
            e
          )), e && h & 2048 && Dc(
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
          ), e && h & 2048 && Uc(f.alternate, f);
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
  function fe(l, t) {
    if (t.subtreeFlags & 10256)
      for (t = t.child; t !== null; ) {
        var u = l, a = t, e = a.flags;
        switch (a.tag) {
          case 22:
            fe(u, a), e & 2048 && Dc(
              a.alternate,
              a
            );
            break;
          case 24:
            fe(u, a), e & 2048 && Uc(a.alternate, a);
            break;
          default:
            fe(u, a);
        }
        t = t.sibling;
      }
  }
  var ce = 8192;
  function Sa(l, t, u) {
    if (l.subtreeFlags & ce)
      for (l = l.child; l !== null; )
        so(
          l,
          t,
          u
        ), l = l.sibling;
  }
  function so(l, t, u) {
    switch (l.tag) {
      case 26:
        Sa(
          l,
          t,
          u
        ), l.flags & ce && l.memoizedState !== null && cd(
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
        bt = Nn(l.stateNode.containerInfo), Sa(
          l,
          t,
          u
        ), bt = a;
        break;
      case 22:
        l.memoizedState === null && (a = l.alternate, a !== null && a.memoizedState !== null ? (a = ce, ce = 16777216, Sa(
          l,
          t,
          u
        ), ce = a) : Sa(
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
  function oo(l) {
    var t = l.alternate;
    if (t !== null && (l = t.child, l !== null)) {
      t.child = null;
      do
        t = l.sibling, l.sibling = null, l = t;
      while (l !== null);
    }
  }
  function ie(l) {
    var t = l.deletions;
    if ((l.flags & 16) !== 0) {
      if (t !== null)
        for (var u = 0; u < t.length; u++) {
          var a = t[u];
          Ol = a, vo(
            a,
            l
          );
        }
      oo(l);
    }
    if (l.subtreeFlags & 10256)
      for (l = l.child; l !== null; )
        yo(l), l = l.sibling;
  }
  function yo(l) {
    switch (l.tag) {
      case 0:
      case 11:
      case 15:
        ie(l), l.flags & 2048 && cu(9, l, l.return);
        break;
      case 3:
        ie(l);
        break;
      case 12:
        ie(l);
        break;
      case 22:
        var t = l.stateNode;
        l.memoizedState !== null && t._visibility & 2 && (l.return === null || l.return.tag !== 13) ? (t._visibility &= -3, rn(l)) : ie(l);
        break;
      default:
        ie(l);
    }
  }
  function rn(l) {
    var t = l.deletions;
    if ((l.flags & 16) !== 0) {
      if (t !== null)
        for (var u = 0; u < t.length; u++) {
          var a = t[u];
          Ol = a, vo(
            a,
            l
          );
        }
      oo(l);
    }
    for (l = l.child; l !== null; ) {
      switch (t = l, t.tag) {
        case 0:
        case 11:
        case 15:
          cu(8, t, t.return), rn(t);
          break;
        case 22:
          u = t.stateNode, u._visibility & 2 && (u._visibility &= -3, rn(t));
          break;
        default:
          rn(t);
      }
      l = l.sibling;
    }
  }
  function vo(l, t) {
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
          Ka(u.memoizedState.cache);
      }
      if (a = u.child, a !== null) a.return = u, Ol = a;
      else
        l: for (u = l; Ol !== null; ) {
          a = Ol;
          var e = a.sibling, n = a.return;
          if (uo(a), a === u) {
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
  var zm = {
    getCacheForType: function(l) {
      var t = Dl(bl), u = t.data.get(l);
      return u === void 0 && (u = l(), t.data.set(l, u)), u;
    },
    cacheSignal: function() {
      return Dl(bl).controller.signal;
    }
  }, Am = typeof WeakMap == "function" ? WeakMap : Map, I = 0, sl = null, L = null, V = 0, tl = 0, ut = null, iu = !1, ga = !1, Rc = !1, Kt = 0, dl = 0, su = 0, Zu = 0, Nc = 0, at = 0, ra = 0, se = null, Kl = null, Hc = !1, bn = 0, mo = 0, Tn = 1 / 0, En = null, ou = null, Al = 0, yu = null, ba = null, Jt = 0, Cc = 0, qc = null, ho = null, oe = 0, Bc = null;
  function et() {
    return (I & 2) !== 0 && V !== 0 ? V & -V : b.T !== null ? Zc() : Ri();
  }
  function So() {
    if (at === 0)
      if ((V & 536870912) === 0 || w) {
        var l = De;
        De <<= 1, (De & 3932160) === 0 && (De = 262144), at = l;
      } else at = 536870912;
    return l = lt.current, l !== null && (l.flags |= 32), at;
  }
  function Jl(l, t, u) {
    (l === sl && (tl === 2 || tl === 9) || l.cancelPendingCommit !== null) && (Ta(l, 0), vu(
      l,
      V,
      at,
      !1
    )), Na(l, u), ((I & 2) === 0 || l !== sl) && (l === sl && ((I & 2) === 0 && (Zu |= u), dl === 4 && vu(
      l,
      V,
      at,
      !1
    )), pt(l));
  }
  function go(l, t, u) {
    if ((I & 6) !== 0) throw Error(v(327));
    var a = !u && (t & 127) === 0 && (t & l.expiredLanes) === 0 || Ra(l, t), e = a ? Mm(l, t) : Gc(l, t, !0), n = a;
    do {
      if (e === 0) {
        ga && !a && vu(l, t, 0, !1);
        break;
      } else {
        if (u = l.current.alternate, n && !_m(u)) {
          e = Gc(l, t, !1), n = !1;
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
              e = se;
              var i = c.current.memoizedState.isDehydrated;
              if (i && (Ta(c, f).flags |= 256), f = Gc(
                c,
                f,
                !1
              ), f !== 2) {
                if (Rc && !i) {
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
          Ta(l, 0), vu(l, t, 0, !0);
          break;
        }
        l: {
          switch (a = l, n = e, n) {
            case 0:
            case 1:
              throw Error(v(345));
            case 4:
              if ((t & 4194048) !== t) break;
            case 6:
              vu(
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
              throw Error(v(329));
          }
          if ((t & 62914560) === t && (e = bn + 300 - $l(), 10 < e)) {
            if (vu(
              a,
              t,
              at,
              !iu
            ), Re(a, 0, !0) !== 0) break l;
            Jt = t, a.timeoutHandle = Wo(
              ro.bind(
                null,
                a,
                u,
                Kl,
                En,
                Hc,
                t,
                at,
                Zu,
                ra,
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
          ro(
            a,
            u,
            Kl,
            En,
            Hc,
            t,
            at,
            Zu,
            ra,
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
    pt(l);
  }
  function ro(l, t, u, a, e, n, f, c, i, h, r, E, S, g) {
    if (l.timeoutHandle = -1, E = t.subtreeFlags, E & 8192 || (E & 16785408) === 16785408) {
      E = {
        stylesheets: null,
        count: 0,
        imgCount: 0,
        imgBytes: 0,
        suspenseyImages: [],
        waitingForImages: !0,
        waitingForViewTransition: !1,
        unsuspend: Nt
      }, so(
        t,
        n,
        E
      );
      var D = (n & 62914560) === n ? bn - $l() : (n & 4194048) === n ? mo - $l() : 0;
      if (D = id(
        E,
        D
      ), D !== null) {
        Jt = n, l.cancelPendingCommit = D(
          Mo.bind(
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
            r,
            E,
            null,
            S,
            g
          )
        ), vu(l, n, f, !h);
        return;
      }
    }
    Mo(
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
  function _m(l) {
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
  function vu(l, t, u, a) {
    t &= ~Nc, t &= ~Zu, l.suspendedLanes |= t, l.pingedLanes &= ~t, a && (l.warmLanes |= t), a = l.expirationTimes;
    for (var e = t; 0 < e; ) {
      var n = 31 - kl(e), f = 1 << n;
      a[n] = -1, e &= ~f;
    }
    u !== 0 && pi(l, u, t);
  }
  function zn() {
    return (I & 6) === 0 ? (ye(0), !1) : !0;
  }
  function Yc() {
    if (L !== null) {
      if (tl === 0)
        var l = L.return;
      else
        l = L, Bt = Hu = null, If(l), oa = null, wa = 0, l = L;
      for (; l !== null; )
        W0(l.alternate, l), l = l.return;
      L = null;
    }
  }
  function Ta(l, t) {
    var u = l.timeoutHandle;
    u !== -1 && (l.timeoutHandle = -1, Vm(u)), u = l.cancelPendingCommit, u !== null && (l.cancelPendingCommit = null, u()), Jt = 0, Yc(), sl = l, L = u = Ct(l.current, null), V = t, tl = 0, ut = null, iu = !1, ga = Ra(l, t), Rc = !1, ra = at = Nc = Zu = su = dl = 0, Kl = se = null, Hc = !1, (t & 8) !== 0 && (t |= t & 32);
    var a = l.entangledLanes;
    if (a !== 0)
      for (l = l.entanglements, a &= t; 0 < a; ) {
        var e = 31 - kl(a), n = 1 << e;
        t |= l[e], a &= ~n;
      }
    return Kt = t, Le(), u;
  }
  function bo(l, t) {
    Q = null, b.H = te, t === sa || t === Fe ? (t = qs(), tl = 3) : t === jf ? (t = qs(), tl = 4) : tl = t === dc ? 8 : t !== null && typeof t == "object" && typeof t.then == "function" ? 6 : 1, ut = t, L === null && (dl = 1, yn(
      l,
      ot(t, l.current)
    ));
  }
  function To() {
    var l = lt.current;
    return l === null ? !0 : (V & 4194048) === V ? dt === null : (V & 62914560) === V || (V & 536870912) !== 0 ? l === dt : !1;
  }
  function Eo() {
    var l = b.H;
    return b.H = te, l === null ? te : l;
  }
  function zo() {
    var l = b.A;
    return b.A = zm, l;
  }
  function An() {
    dl = 4, iu || (V & 4194048) !== V && lt.current !== null || (ga = !0), (su & 134217727) === 0 && (Zu & 134217727) === 0 || sl === null || vu(
      sl,
      V,
      at,
      !1
    );
  }
  function Gc(l, t, u) {
    var a = I;
    I |= 2;
    var e = Eo(), n = zo();
    (sl !== l || V !== t) && (En = null, Ta(l, t)), t = !1;
    var f = dl;
    l: do
      try {
        if (tl !== 0 && L !== null) {
          var c = L, i = ut;
          switch (tl) {
            case 8:
              Yc(), f = 6;
              break l;
            case 3:
            case 2:
            case 9:
            case 6:
              lt.current === null && (t = !0);
              var h = tl;
              if (tl = 0, ut = null, Ea(l, c, i, h), u && ga) {
                f = 0;
                break l;
              }
              break;
            default:
              h = tl, tl = 0, ut = null, Ea(l, c, i, h);
          }
        }
        Om(), f = dl;
        break;
      } catch (r) {
        bo(l, r);
      }
    while (!0);
    return t && l.shellSuspendCounter++, Bt = Hu = null, I = a, b.H = e, b.A = n, L === null && (sl = null, V = 0, Le()), f;
  }
  function Om() {
    for (; L !== null; ) Ao(L);
  }
  function Mm(l, t) {
    var u = I;
    I |= 2;
    var a = Eo(), e = zo();
    sl !== l || V !== t ? (En = null, Tn = $l() + 500, Ta(l, t)) : ga = Ra(
      l,
      t
    );
    l: do
      try {
        if (tl !== 0 && L !== null) {
          t = L;
          var n = ut;
          t: switch (tl) {
            case 1:
              tl = 0, ut = null, Ea(l, t, n, 1);
              break;
            case 2:
            case 9:
              if (Hs(n)) {
                tl = 0, ut = null, _o(t);
                break;
              }
              t = function() {
                tl !== 2 && tl !== 9 || sl !== l || (tl = 7), pt(l);
              }, n.then(t, t);
              break l;
            case 3:
              tl = 7;
              break l;
            case 4:
              tl = 5;
              break l;
            case 7:
              Hs(n) ? (tl = 0, ut = null, _o(t)) : (tl = 0, ut = null, Ea(l, t, n, 7));
              break;
            case 5:
              var f = null;
              switch (L.tag) {
                case 26:
                  f = L.memoizedState;
                case 5:
                case 27:
                  var c = L;
                  if (f ? sy(f) : c.stateNode.complete) {
                    tl = 0, ut = null;
                    var i = c.sibling;
                    if (i !== null) L = i;
                    else {
                      var h = c.return;
                      h !== null ? (L = h, _n(h)) : L = null;
                    }
                    break t;
                  }
              }
              tl = 0, ut = null, Ea(l, t, n, 5);
              break;
            case 6:
              tl = 0, ut = null, Ea(l, t, n, 6);
              break;
            case 8:
              Yc(), dl = 6;
              break l;
            default:
              throw Error(v(462));
          }
        }
        pm();
        break;
      } catch (r) {
        bo(l, r);
      }
    while (!0);
    return Bt = Hu = null, b.H = a, b.A = e, I = u, L !== null ? 0 : (sl = null, V = 0, Le(), dl);
  }
  function pm() {
    for (; L !== null && !Fy(); )
      Ao(L);
  }
  function Ao(l) {
    var t = J0(l.alternate, l, Kt);
    l.memoizedProps = l.pendingProps, t === null ? _n(l) : L = t;
  }
  function _o(l) {
    var t = l, u = t.alternate;
    switch (t.tag) {
      case 15:
      case 0:
        t = j0(
          u,
          t,
          t.pendingProps,
          t.type,
          void 0,
          V
        );
        break;
      case 11:
        t = j0(
          u,
          t,
          t.pendingProps,
          t.type.render,
          t.ref,
          V
        );
        break;
      case 5:
        If(t);
      default:
        W0(u, t), t = L = Es(t, Kt), t = J0(u, t, Kt);
    }
    l.memoizedProps = l.pendingProps, t === null ? _n(l) : L = t;
  }
  function Ea(l, t, u, a) {
    Bt = Hu = null, If(t), oa = null, wa = 0;
    var e = t.return;
    try {
      if (hm(
        l,
        e,
        t,
        u,
        V
      )) {
        dl = 1, yn(
          l,
          ot(u, l.current)
        ), L = null;
        return;
      }
    } catch (n) {
      if (e !== null) throw L = e, n;
      dl = 1, yn(
        l,
        ot(u, l.current)
      ), L = null;
      return;
    }
    t.flags & 32768 ? (w || a === 1 ? l = !0 : ga || (V & 536870912) !== 0 ? l = !1 : (iu = l = !0, (a === 2 || a === 9 || a === 3 || a === 6) && (a = lt.current, a !== null && a.tag === 13 && (a.flags |= 16384))), Oo(t, l)) : _n(t);
  }
  function _n(l) {
    var t = l;
    do {
      if ((t.flags & 32768) !== 0) {
        Oo(
          t,
          iu
        );
        return;
      }
      l = t.return;
      var u = rm(
        t.alternate,
        t,
        Kt
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
  function Oo(l, t) {
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
  function Mo(l, t, u, a, e, n, f, c, i) {
    l.cancelPendingCommit = null;
    do
      On();
    while (Al !== 0);
    if ((I & 6) !== 0) throw Error(v(327));
    if (t !== null) {
      if (t === l.current) throw Error(v(177));
      if (n = t.lanes | t.childLanes, n |= Of, fv(
        l,
        u,
        n,
        f,
        c,
        i
      ), l === sl && (L = sl = null, V = 0), ba = t, yu = l, Jt = u, Cc = n, qc = e, ho = a, (t.subtreeFlags & 10256) !== 0 || (t.flags & 10256) !== 0 ? (l.callbackNode = null, l.callbackPriority = 0, Nm(Me, function() {
        return No(), null;
      })) : (l.callbackNode = null, l.callbackPriority = 0), a = (t.flags & 13878) !== 0, (t.subtreeFlags & 13878) !== 0 || a) {
        a = b.T, b.T = null, e = M.p, M.p = 2, f = I, I |= 4;
        try {
          Tm(l, t, u);
        } finally {
          I = f, M.p = e, b.T = a;
        }
      }
      Al = 1, po(), Do(), Uo();
    }
  }
  function po() {
    if (Al === 1) {
      Al = 0;
      var l = yu, t = ba, u = (t.flags & 13878) !== 0;
      if ((t.subtreeFlags & 13878) !== 0 || u) {
        u = b.T, b.T = null;
        var a = M.p;
        M.p = 2;
        var e = I;
        I |= 4;
        try {
          fo(t, l);
          var n = $c, f = vs(l.containerInfo), c = n.focusedElem, i = n.selectionRange;
          if (f !== c && c && c.ownerDocument && ys(
            c.ownerDocument.documentElement,
            c
          )) {
            if (i !== null && Tf(c)) {
              var h = i.start, r = i.end;
              if (r === void 0 && (r = h), "selectionStart" in c)
                c.selectionStart = h, c.selectionEnd = Math.min(
                  r,
                  c.value.length
                );
              else {
                var E = c.ownerDocument || document, S = E && E.defaultView || window;
                if (S.getSelection) {
                  var g = S.getSelection(), D = c.textContent.length, C = Math.min(i.start, D), cl = i.end === void 0 ? C : Math.min(i.end, D);
                  !g.extend && C > cl && (f = cl, cl = C, C = f);
                  var y = os(
                    c,
                    C
                  ), s = os(
                    c,
                    cl
                  );
                  if (y && s && (g.rangeCount !== 1 || g.anchorNode !== y.node || g.anchorOffset !== y.offset || g.focusNode !== s.node || g.focusOffset !== s.offset)) {
                    var d = E.createRange();
                    d.setStart(y.node, y.offset), g.removeAllRanges(), C > cl ? (g.addRange(d), g.extend(s.node, s.offset)) : (d.setEnd(s.node, s.offset), g.addRange(d));
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
              var T = E[c];
              T.element.scrollLeft = T.left, T.element.scrollTop = T.top;
            }
          }
          Gn = !!Wc, $c = Wc = null;
        } finally {
          I = e, M.p = a, b.T = u;
        }
      }
      l.current = t, Al = 2;
    }
  }
  function Do() {
    if (Al === 2) {
      Al = 0;
      var l = yu, t = ba, u = (t.flags & 8772) !== 0;
      if ((t.subtreeFlags & 8772) !== 0 || u) {
        u = b.T, b.T = null;
        var a = M.p;
        M.p = 2;
        var e = I;
        I |= 4;
        try {
          to(l, t.alternate, t);
        } finally {
          I = e, M.p = a, b.T = u;
        }
      }
      Al = 3;
    }
  }
  function Uo() {
    if (Al === 4 || Al === 3) {
      Al = 0, ky();
      var l = yu, t = ba, u = Jt, a = ho;
      (t.subtreeFlags & 10256) !== 0 || (t.flags & 10256) !== 0 ? Al = 5 : (Al = 0, ba = yu = null, Ro(l, l.pendingLanes));
      var e = l.pendingLanes;
      if (e === 0 && (ou = null), lf(u), t = t.stateNode, Fl && typeof Fl.onCommitFiberRoot == "function")
        try {
          Fl.onCommitFiberRoot(
            Ua,
            t,
            void 0,
            (t.current.flags & 128) === 128
          );
        } catch {
        }
      if (a !== null) {
        t = b.T, e = M.p, M.p = 2, b.T = null;
        try {
          for (var n = l.onRecoverableError, f = 0; f < a.length; f++) {
            var c = a[f];
            n(c.value, {
              componentStack: c.stack
            });
          }
        } finally {
          b.T = t, M.p = e;
        }
      }
      (Jt & 3) !== 0 && On(), pt(l), e = l.pendingLanes, (u & 261930) !== 0 && (e & 42) !== 0 ? l === Bc ? oe++ : (oe = 0, Bc = l) : oe = 0, ye(0);
    }
  }
  function Ro(l, t) {
    (l.pooledCacheLanes &= t) === 0 && (t = l.pooledCache, t != null && (l.pooledCache = null, Ka(t)));
  }
  function On() {
    return po(), Do(), Uo(), No();
  }
  function No() {
    if (Al !== 5) return !1;
    var l = yu, t = Cc;
    Cc = 0;
    var u = lf(Jt), a = b.T, e = M.p;
    try {
      M.p = 32 > u ? 32 : u, b.T = null, u = qc, qc = null;
      var n = yu, f = Jt;
      if (Al = 0, ba = yu = null, Jt = 0, (I & 6) !== 0) throw Error(v(331));
      var c = I;
      if (I |= 4, yo(n.current), io(
        n,
        n.current,
        f,
        u
      ), I = c, ye(0, !1), Fl && typeof Fl.onPostCommitFiberRoot == "function")
        try {
          Fl.onPostCommitFiberRoot(Ua, n);
        } catch {
        }
      return !0;
    } finally {
      M.p = e, b.T = a, Ro(l, t);
    }
  }
  function Ho(l, t, u) {
    t = ot(u, t), t = mc(l.stateNode, t, 2), l = eu(l, t, 2), l !== null && (Na(l, 2), pt(l));
  }
  function ul(l, t, u) {
    if (l.tag === 3)
      Ho(l, l, u);
    else
      for (; t !== null; ) {
        if (t.tag === 3) {
          Ho(
            t,
            l,
            u
          );
          break;
        } else if (t.tag === 1) {
          var a = t.stateNode;
          if (typeof t.type.getDerivedStateFromError == "function" || typeof a.componentDidCatch == "function" && (ou === null || !ou.has(a))) {
            l = ot(u, l), u = H0(2), a = eu(t, u, 2), a !== null && (C0(
              u,
              a,
              t,
              l
            ), Na(a, 2), pt(a));
            break;
          }
        }
        t = t.return;
      }
  }
  function Xc(l, t, u) {
    var a = l.pingCache;
    if (a === null) {
      a = l.pingCache = new Am();
      var e = /* @__PURE__ */ new Set();
      a.set(t, e);
    } else
      e = a.get(t), e === void 0 && (e = /* @__PURE__ */ new Set(), a.set(t, e));
    e.has(u) || (Rc = !0, e.add(u), l = Dm.bind(null, l, t, u), t.then(l, l));
  }
  function Dm(l, t, u) {
    var a = l.pingCache;
    a !== null && a.delete(t), l.pingedLanes |= l.suspendedLanes & u, l.warmLanes &= ~u, sl === l && (V & u) === u && (dl === 4 || dl === 3 && (V & 62914560) === V && 300 > $l() - bn ? (I & 2) === 0 && Ta(l, 0) : Nc |= u, ra === V && (ra = 0)), pt(l);
  }
  function Co(l, t) {
    t === 0 && (t = Mi()), l = Uu(l, t), l !== null && (Na(l, t), pt(l));
  }
  function Um(l) {
    var t = l.memoizedState, u = 0;
    t !== null && (u = t.retryLane), Co(l, u);
  }
  function Rm(l, t) {
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
        throw Error(v(314));
    }
    a !== null && a.delete(t), Co(l, u);
  }
  function Nm(l, t) {
    return Fn(l, t);
  }
  var Mn = null, za = null, Qc = !1, pn = !1, jc = !1, mu = 0;
  function pt(l) {
    l !== za && l.next === null && (za === null ? Mn = za = l : za = za.next = l), pn = !0, Qc || (Qc = !0, Cm());
  }
  function ye(l, t) {
    if (!jc && pn) {
      jc = !0;
      do
        for (var u = !1, a = Mn; a !== null; ) {
          if (l !== 0) {
            var e = a.pendingLanes;
            if (e === 0) var n = 0;
            else {
              var f = a.suspendedLanes, c = a.pingedLanes;
              n = (1 << 31 - kl(42 | l) + 1) - 1, n &= e & ~(f & ~c), n = n & 201326741 ? n & 201326741 | 1 : n ? n | 2 : 0;
            }
            n !== 0 && (u = !0, Go(a, n));
          } else
            n = V, n = Re(
              a,
              a === sl ? n : 0,
              a.cancelPendingCommit !== null || a.timeoutHandle !== -1
            ), (n & 3) === 0 || Ra(a, n) || (u = !0, Go(a, n));
          a = a.next;
        }
      while (u);
      jc = !1;
    }
  }
  function Hm() {
    qo();
  }
  function qo() {
    pn = Qc = !1;
    var l = 0;
    mu !== 0 && xm() && (l = mu);
    for (var t = $l(), u = null, a = Mn; a !== null; ) {
      var e = a.next, n = Bo(a, t);
      n === 0 ? (a.next = null, u === null ? Mn = e : u.next = e, e === null && (za = u)) : (u = a, (l !== 0 || (n & 3) !== 0) && (pn = !0)), a = e;
    }
    Al !== 0 && Al !== 5 || ye(l), mu !== 0 && (mu = 0);
  }
  function Bo(l, t) {
    for (var u = l.suspendedLanes, a = l.pingedLanes, e = l.expirationTimes, n = l.pendingLanes & -62914561; 0 < n; ) {
      var f = 31 - kl(n), c = 1 << f, i = e[f];
      i === -1 ? ((c & u) === 0 || (c & a) !== 0) && (e[f] = nv(c, t)) : i <= t && (l.expiredLanes |= c), n &= ~c;
    }
    if (t = sl, u = V, u = Re(
      l,
      l === t ? u : 0,
      l.cancelPendingCommit !== null || l.timeoutHandle !== -1
    ), a = l.callbackNode, u === 0 || l === t && (tl === 2 || tl === 9) || l.cancelPendingCommit !== null)
      return a !== null && a !== null && kn(a), l.callbackNode = null, l.callbackPriority = 0;
    if ((u & 3) === 0 || Ra(l, u)) {
      if (t = u & -u, t === l.callbackPriority) return t;
      switch (a !== null && kn(a), lf(u)) {
        case 2:
        case 8:
          u = _i;
          break;
        case 32:
          u = Me;
          break;
        case 268435456:
          u = Oi;
          break;
        default:
          u = Me;
      }
      return a = Yo.bind(null, l), u = Fn(u, a), l.callbackPriority = t, l.callbackNode = u, t;
    }
    return a !== null && a !== null && kn(a), l.callbackPriority = 2, l.callbackNode = null, 2;
  }
  function Yo(l, t) {
    if (Al !== 0 && Al !== 5)
      return l.callbackNode = null, l.callbackPriority = 0, null;
    var u = l.callbackNode;
    if (On() && l.callbackNode !== u)
      return null;
    var a = V;
    return a = Re(
      l,
      l === sl ? a : 0,
      l.cancelPendingCommit !== null || l.timeoutHandle !== -1
    ), a === 0 ? null : (go(l, a, t), Bo(l, $l()), l.callbackNode != null && l.callbackNode === u ? Yo.bind(null, l) : null);
  }
  function Go(l, t) {
    if (On()) return null;
    go(l, t, !0);
  }
  function Cm() {
    Km(function() {
      (I & 6) !== 0 ? Fn(
        Ai,
        Hm
      ) : qo();
    });
  }
  function Zc() {
    if (mu === 0) {
      var l = ca;
      l === 0 && (l = pe, pe <<= 1, (pe & 261888) === 0 && (pe = 256)), mu = l;
    }
    return mu;
  }
  function Xo(l) {
    return l == null || typeof l == "symbol" || typeof l == "boolean" ? null : typeof l == "function" ? l : qe("" + l);
  }
  function Qo(l, t) {
    var u = t.ownerDocument.createElement("input");
    return u.name = t.name, u.value = t.value, l.id && u.setAttribute("form", l.id), t.parentNode.insertBefore(u, t), l = new FormData(l), u.parentNode.removeChild(u), l;
  }
  function qm(l, t, u, a, e) {
    if (t === "submit" && u && u.stateNode === e) {
      var n = Xo(
        (e[jl] || null).action
      ), f = a.submitter;
      f && (t = (t = f[jl] || null) ? Xo(t.formAction) : f.getAttribute("formAction"), t !== null && (n = t, f = null));
      var c = new Xe(
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
                  var i = f ? Qo(e, f) : new FormData(e);
                  cc(
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
                typeof n == "function" && (c.preventDefault(), i = f ? Qo(e, f) : new FormData(e), cc(
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
  for (var Lc = 0; Lc < _f.length; Lc++) {
    var xc = _f[Lc], Bm = xc.toLowerCase(), Ym = xc[0].toUpperCase() + xc.slice(1);
    rt(
      Bm,
      "on" + Ym
    );
  }
  rt(hs, "onAnimationEnd"), rt(Ss, "onAnimationIteration"), rt(gs, "onAnimationStart"), rt("dblclick", "onDoubleClick"), rt("focusin", "onFocus"), rt("focusout", "onBlur"), rt(Iv, "onTransitionRun"), rt(Pv, "onTransitionStart"), rt(lm, "onTransitionCancel"), rt(rs, "onTransitionEnd"), wu("onMouseEnter", ["mouseout", "mouseover"]), wu("onMouseLeave", ["mouseout", "mouseover"]), wu("onPointerEnter", ["pointerout", "pointerover"]), wu("onPointerLeave", ["pointerout", "pointerover"]), Ou(
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
  ), Gm = new Set(
    "beforetoggle cancel close invalid load scroll scrollend toggle".split(" ").concat(ve)
  );
  function jo(l, t) {
    t = (t & 4) !== 0;
    for (var u = 0; u < l.length; u++) {
      var a = l[u], e = a.event;
      a = a.listeners;
      l: {
        var n = void 0;
        if (t)
          for (var f = a.length - 1; 0 <= f; f--) {
            var c = a[f], i = c.instance, h = c.currentTarget;
            if (c = c.listener, i !== n && e.isPropagationStopped())
              break l;
            n = c, e.currentTarget = h;
            try {
              n(e);
            } catch (r) {
              Ze(r);
            }
            e.currentTarget = null, n = i;
          }
        else
          for (f = 0; f < a.length; f++) {
            if (c = a[f], i = c.instance, h = c.currentTarget, c = c.listener, i !== n && e.isPropagationStopped())
              break l;
            n = c, e.currentTarget = h;
            try {
              n(e);
            } catch (r) {
              Ze(r);
            }
            e.currentTarget = null, n = i;
          }
      }
    }
  }
  function x(l, t) {
    var u = t[tf];
    u === void 0 && (u = t[tf] = /* @__PURE__ */ new Set());
    var a = l + "__bubble";
    u.has(a) || (Zo(t, l, 2, !1), u.add(a));
  }
  function Vc(l, t, u) {
    var a = 0;
    t && (a |= 4), Zo(
      u,
      l,
      a,
      t
    );
  }
  var Dn = "_reactListening" + Math.random().toString(36).slice(2);
  function Kc(l) {
    if (!l[Dn]) {
      l[Dn] = !0, Ci.forEach(function(u) {
        u !== "selectionchange" && (Gm.has(u) || Vc(u, !1, l), Vc(u, !0, l));
      });
      var t = l.nodeType === 9 ? l : l.ownerDocument;
      t === null || t[Dn] || (t[Dn] = !0, Vc("selectionchange", !1, t));
    }
  }
  function Zo(l, t, u, a) {
    switch (Sy(t)) {
      case 2:
        var e = yd;
        break;
      case 8:
        e = vd;
        break;
      default:
        e = fi;
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
  function Jc(l, t, u, a, e) {
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
    Ki(function() {
      var h = n, r = sf(u), E = [];
      l: {
        var S = bs.get(l);
        if (S !== void 0) {
          var g = Xe, D = l;
          switch (l) {
            case "keypress":
              if (Ye(u) === 0) break l;
            case "keydown":
            case "keyup":
              g = Rv;
              break;
            case "focusin":
              D = "focus", g = hf;
              break;
            case "focusout":
              D = "blur", g = hf;
              break;
            case "beforeblur":
            case "afterblur":
              g = hf;
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
              g = Wi;
              break;
            case "drag":
            case "dragend":
            case "dragenter":
            case "dragexit":
            case "dragleave":
            case "dragover":
            case "dragstart":
            case "drop":
              g = rv;
              break;
            case "touchcancel":
            case "touchend":
            case "touchmove":
            case "touchstart":
              g = Cv;
              break;
            case hs:
            case Ss:
            case gs:
              g = Ev;
              break;
            case rs:
              g = Bv;
              break;
            case "scroll":
            case "scrollend":
              g = Sv;
              break;
            case "wheel":
              g = Gv;
              break;
            case "copy":
            case "cut":
            case "paste":
              g = Av;
              break;
            case "gotpointercapture":
            case "lostpointercapture":
            case "pointercancel":
            case "pointerdown":
            case "pointermove":
            case "pointerout":
            case "pointerover":
            case "pointerup":
              g = Fi;
              break;
            case "toggle":
            case "beforetoggle":
              g = Qv;
          }
          var C = (t & 4) !== 0, cl = !C && (l === "scroll" || l === "scrollend"), y = C ? S !== null ? S + "Capture" : null : S;
          C = [];
          for (var s = h, d; s !== null; ) {
            var T = s;
            if (d = T.stateNode, T = T.tag, T !== 5 && T !== 26 && T !== 27 || d === null || y === null || (T = qa(s, y), T != null && C.push(
              me(s, T, d)
            )), cl) break;
            s = s.return;
          }
          0 < C.length && (S = new g(
            S,
            D,
            null,
            u,
            r
          ), E.push({ event: S, listeners: C }));
        }
      }
      if ((t & 7) === 0) {
        l: {
          if (S = l === "mouseover" || l === "pointerover", g = l === "mouseout" || l === "pointerout", S && u !== cf && (D = u.relatedTarget || u.fromElement) && (Vu(D) || D[xu]))
            break l;
          if ((g || S) && (S = r.window === r ? r : (S = r.ownerDocument) ? S.defaultView || S.parentWindow : window, g ? (D = u.relatedTarget || u.toElement, g = h, D = D ? Vu(D) : null, D !== null && (cl = W(D), C = D.tag, D !== cl || C !== 5 && C !== 27 && C !== 6) && (D = null)) : (g = null, D = h), g !== D)) {
            if (C = Wi, T = "onMouseLeave", y = "onMouseEnter", s = "mouse", (l === "pointerout" || l === "pointerover") && (C = Fi, T = "onPointerLeave", y = "onPointerEnter", s = "pointer"), cl = g == null ? S : Ca(g), d = D == null ? S : Ca(D), S = new C(
              T,
              s + "leave",
              g,
              u,
              r
            ), S.target = cl, S.relatedTarget = d, T = null, Vu(r) === h && (C = new C(
              y,
              s + "enter",
              D,
              u,
              r
            ), C.target = d, C.relatedTarget = cl, T = C), cl = T, g && D)
              t: {
                for (C = Xm, y = g, s = D, d = 0, T = y; T; T = C(T))
                  d++;
                T = 0;
                for (var N = s; N; N = C(N))
                  T++;
                for (; 0 < d - T; )
                  y = C(y), d--;
                for (; 0 < T - d; )
                  s = C(s), T--;
                for (; d--; ) {
                  if (y === s || s !== null && y === s.alternate) {
                    C = y;
                    break t;
                  }
                  y = C(y), s = C(s);
                }
                C = null;
              }
            else C = null;
            g !== null && Lo(
              E,
              S,
              g,
              C,
              !1
            ), D !== null && cl !== null && Lo(
              E,
              cl,
              D,
              C,
              !0
            );
          }
        }
        l: {
          if (S = h ? Ca(h) : window, g = S.nodeName && S.nodeName.toLowerCase(), g === "select" || g === "input" && S.type === "file")
            var F = es;
          else if (us(S))
            if (ns)
              F = $v;
            else {
              F = wv;
              var R = Jv;
            }
          else
            g = S.nodeName, !g || g.toLowerCase() !== "input" || S.type !== "checkbox" && S.type !== "radio" ? h && ff(h.elementType) && (F = es) : F = Wv;
          if (F && (F = F(l, h))) {
            as(
              E,
              F,
              u,
              r
            );
            break l;
          }
          R && R(l, S, h), l === "focusout" && h && S.type === "number" && h.memoizedProps.value != null && nf(S, "number", S.value);
        }
        switch (R = h ? Ca(h) : window, l) {
          case "focusin":
            (us(R) || R.contentEditable === "true") && (Pu = R, Ef = h, La = null);
            break;
          case "focusout":
            La = Ef = Pu = null;
            break;
          case "mousedown":
            zf = !0;
            break;
          case "contextmenu":
          case "mouseup":
          case "dragend":
            zf = !1, ms(E, u, r);
            break;
          case "selectionchange":
            if (kv) break;
          case "keydown":
          case "keyup":
            ms(E, u, r);
        }
        var j;
        if (gf)
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
          Iu ? ls(l, u) && (K = "onCompositionEnd") : l === "keydown" && u.keyCode === 229 && (K = "onCompositionStart");
        K && (ki && u.locale !== "ko" && (Iu || K !== "onCompositionStart" ? K === "onCompositionEnd" && Iu && (j = Ji()) : (kt = r, vf = "value" in kt ? kt.value : kt.textContent, Iu = !0)), R = Un(h, K), 0 < R.length && (K = new $i(
          K,
          l,
          null,
          u,
          r
        ), E.push({ event: K, listeners: R }), j ? K.data = j : (j = ts(u), j !== null && (K.data = j)))), (j = Zv ? Lv(l, u) : xv(l, u)) && (K = Un(h, "onBeforeInput"), 0 < K.length && (R = new $i(
          "onBeforeInput",
          "beforeinput",
          null,
          u,
          r
        ), E.push({
          event: R,
          listeners: K
        }), R.data = j)), qm(
          E,
          l,
          h,
          u,
          r
        );
      }
      jo(E, t);
    });
  }
  function me(l, t, u) {
    return {
      instance: l,
      listener: t,
      currentTarget: u
    };
  }
  function Un(l, t) {
    for (var u = t + "Capture", a = []; l !== null; ) {
      var e = l, n = e.stateNode;
      if (e = e.tag, e !== 5 && e !== 26 && e !== 27 || n === null || (e = qa(l, u), e != null && a.unshift(
        me(l, e, n)
      ), e = qa(l, t), e != null && a.push(
        me(l, e, n)
      )), l.tag === 3) return a;
      l = l.return;
    }
    return [];
  }
  function Xm(l) {
    if (l === null) return null;
    do
      l = l.return;
    while (l && l.tag !== 5 && l.tag !== 27);
    return l || null;
  }
  function Lo(l, t, u, a, e) {
    for (var n = t._reactName, f = []; u !== null && u !== a; ) {
      var c = u, i = c.alternate, h = c.stateNode;
      if (c = c.tag, i !== null && i === a) break;
      c !== 5 && c !== 26 && c !== 27 || h === null || (i = h, e ? (h = qa(u, n), h != null && f.unshift(
        me(u, h, i)
      )) : e || (h = qa(u, n), h != null && f.push(
        me(u, h, i)
      ))), u = u.return;
    }
    f.length !== 0 && l.push({ event: t, listeners: f });
  }
  var Qm = /\r\n?/g, jm = /\u0000|\uFFFD/g;
  function xo(l) {
    return (typeof l == "string" ? l : "" + l).replace(Qm, `
`).replace(jm, "");
  }
  function Vo(l, t) {
    return t = xo(t), xo(l) === t;
  }
  function fl(l, t, u, a, e, n) {
    switch (u) {
      case "children":
        typeof a == "string" ? t === "body" || t === "textarea" && a === "" || $u(l, a) : (typeof a == "number" || typeof a == "bigint") && t !== "body" && $u(l, "" + a);
        break;
      case "className":
        He(l, "class", a);
        break;
      case "tabIndex":
        He(l, "tabindex", a);
        break;
      case "dir":
      case "role":
      case "viewBox":
      case "width":
      case "height":
        He(l, u, a);
        break;
      case "style":
        xi(l, a, n);
        break;
      case "data":
        if (t !== "object") {
          He(l, "data", a);
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
        a = qe("" + a), l.setAttribute(u, a);
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
        a = qe("" + a), l.setAttribute(u, a);
        break;
      case "onClick":
        a != null && (l.onclick = Nt);
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
            throw Error(v(61));
          if (u = a.__html, u != null) {
            if (e.children != null) throw Error(v(60));
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
        u = qe("" + a), l.setAttributeNS(
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
        x("beforetoggle", l), x("toggle", l), Ne(l, "popover", a);
        break;
      case "xlinkActuate":
        Rt(
          l,
          "http://www.w3.org/1999/xlink",
          "xlink:actuate",
          a
        );
        break;
      case "xlinkArcrole":
        Rt(
          l,
          "http://www.w3.org/1999/xlink",
          "xlink:arcrole",
          a
        );
        break;
      case "xlinkRole":
        Rt(
          l,
          "http://www.w3.org/1999/xlink",
          "xlink:role",
          a
        );
        break;
      case "xlinkShow":
        Rt(
          l,
          "http://www.w3.org/1999/xlink",
          "xlink:show",
          a
        );
        break;
      case "xlinkTitle":
        Rt(
          l,
          "http://www.w3.org/1999/xlink",
          "xlink:title",
          a
        );
        break;
      case "xlinkType":
        Rt(
          l,
          "http://www.w3.org/1999/xlink",
          "xlink:type",
          a
        );
        break;
      case "xmlBase":
        Rt(
          l,
          "http://www.w3.org/XML/1998/namespace",
          "xml:base",
          a
        );
        break;
      case "xmlLang":
        Rt(
          l,
          "http://www.w3.org/XML/1998/namespace",
          "xml:lang",
          a
        );
        break;
      case "xmlSpace":
        Rt(
          l,
          "http://www.w3.org/XML/1998/namespace",
          "xml:space",
          a
        );
        break;
      case "is":
        Ne(l, "is", a);
        break;
      case "innerText":
      case "textContent":
        break;
      default:
        (!(2 < u.length) || u[0] !== "o" && u[0] !== "O" || u[1] !== "n" && u[1] !== "N") && (u = dv.get(u) || u, Ne(l, u, a));
    }
  }
  function wc(l, t, u, a, e, n) {
    switch (u) {
      case "style":
        xi(l, a, n);
        break;
      case "dangerouslySetInnerHTML":
        if (a != null) {
          if (typeof a != "object" || !("__html" in a))
            throw Error(v(61));
          if (u = a.__html, u != null) {
            if (e.children != null) throw Error(v(60));
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
        a != null && (l.onclick = Nt);
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
        if (!qi.hasOwnProperty(u))
          l: {
            if (u[0] === "o" && u[1] === "n" && (e = u.endsWith("Capture"), t = u.slice(2, e ? u.length - 7 : void 0), n = l[jl] || null, n = n != null ? n[u] : null, typeof n == "function" && l.removeEventListener(t, n, e), typeof a == "function")) {
              typeof n != "function" && n !== null && (u in l ? l[u] = null : l.hasAttribute(u) && l.removeAttribute(u)), l.addEventListener(t, a, e);
              break l;
            }
            u in l ? l[u] = a : a === !0 ? l.setAttribute(u, "") : Ne(l, u, a);
          }
    }
  }
  function Rl(l, t, u) {
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
                  throw Error(v(137, t));
                default:
                  fl(l, t, n, f, u, null);
              }
          }
        e && fl(l, t, "srcSet", u.srcSet, u, null), a && fl(l, t, "src", u.src, u, null);
        return;
      case "input":
        x("invalid", l);
        var c = n = f = e = null, i = null, h = null;
        for (a in u)
          if (u.hasOwnProperty(a)) {
            var r = u[a];
            if (r != null)
              switch (a) {
                case "name":
                  e = r;
                  break;
                case "type":
                  f = r;
                  break;
                case "checked":
                  i = r;
                  break;
                case "defaultChecked":
                  h = r;
                  break;
                case "value":
                  n = r;
                  break;
                case "defaultValue":
                  c = r;
                  break;
                case "children":
                case "dangerouslySetInnerHTML":
                  if (r != null)
                    throw Error(v(137, t));
                  break;
                default:
                  fl(l, t, a, r, u, null);
              }
          }
        Qi(
          l,
          n,
          c,
          i,
          h,
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
                if (c != null) throw Error(v(91));
                break;
              default:
                fl(l, t, f, c, u, null);
            }
        Zi(l, a, e, n);
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
        for (h in u)
          if (u.hasOwnProperty(h) && (a = u[h], a != null))
            switch (h) {
              case "children":
              case "dangerouslySetInnerHTML":
                throw Error(v(137, t));
              default:
                fl(l, t, h, a, u, null);
            }
        return;
      default:
        if (ff(t)) {
          for (r in u)
            u.hasOwnProperty(r) && (a = u[r], a !== void 0 && wc(
              l,
              t,
              r,
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
  function Zm(l, t, u, a) {
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
        var e = null, n = null, f = null, c = null, i = null, h = null, r = null;
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
                h = g;
                break;
              case "defaultChecked":
                r = g;
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
                  throw Error(v(137, t));
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
        ef(
          l,
          f,
          c,
          i,
          h,
          r,
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
                if (e != null) throw Error(v(91));
                break;
              default:
                e !== n && fl(l, t, f, e, a, n);
            }
        ji(l, S, g);
        return;
      case "option":
        for (var D in u)
          if (S = u[D], u.hasOwnProperty(D) && S != null && !a.hasOwnProperty(D))
            switch (D) {
              case "selected":
                l.selected = !1;
                break;
              default:
                fl(
                  l,
                  t,
                  D,
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
        for (h in a)
          if (S = a[h], g = u[h], a.hasOwnProperty(h) && S !== g && (S != null || g != null))
            switch (h) {
              case "children":
              case "dangerouslySetInnerHTML":
                if (S != null)
                  throw Error(v(137, t));
                break;
              default:
                fl(
                  l,
                  t,
                  h,
                  S,
                  a,
                  g
                );
            }
        return;
      default:
        if (ff(t)) {
          for (var cl in u)
            S = u[cl], u.hasOwnProperty(cl) && S !== void 0 && !a.hasOwnProperty(cl) && wc(
              l,
              t,
              cl,
              void 0,
              a,
              S
            );
          for (r in a)
            S = a[r], g = u[r], !a.hasOwnProperty(r) || S === g || S === void 0 && g === void 0 || wc(
              l,
              t,
              r,
              S,
              a,
              g
            );
          return;
        }
    }
    for (var y in u)
      S = u[y], u.hasOwnProperty(y) && S != null && !a.hasOwnProperty(y) && fl(l, t, y, null, a, S);
    for (E in a)
      S = a[E], g = u[E], !a.hasOwnProperty(E) || S === g || S == null && g == null || fl(l, t, E, S, a, g);
  }
  function Ko(l) {
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
  function Lm() {
    if (typeof performance.getEntriesByType == "function") {
      for (var l = 0, t = 0, u = performance.getEntriesByType("resource"), a = 0; a < u.length; a++) {
        var e = u[a], n = e.transferSize, f = e.initiatorType, c = e.duration;
        if (n && c && Ko(f)) {
          for (f = 0, c = e.responseEnd, a += 1; a < u.length; a++) {
            var i = u[a], h = i.startTime;
            if (h > c) break;
            var r = i.transferSize, E = i.initiatorType;
            r && Ko(E) && (i = i.responseEnd, f += r * (i < c ? 1 : (c - h) / (i - h)));
          }
          if (--a, t += 8 * (n + f) / (e.duration / 1e3), l++, 10 < l) break;
        }
      }
      if (0 < l) return t / l / 1e6;
    }
    return navigator.connection && (l = navigator.connection.downlink, typeof l == "number") ? l : 5;
  }
  var Wc = null, $c = null;
  function Rn(l) {
    return l.nodeType === 9 ? l : l.ownerDocument;
  }
  function Jo(l) {
    switch (l) {
      case "http://www.w3.org/2000/svg":
        return 1;
      case "http://www.w3.org/1998/Math/MathML":
        return 2;
      default:
        return 0;
    }
  }
  function wo(l, t) {
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
  function Fc(l, t) {
    return l === "textarea" || l === "noscript" || typeof t.children == "string" || typeof t.children == "number" || typeof t.children == "bigint" || typeof t.dangerouslySetInnerHTML == "object" && t.dangerouslySetInnerHTML !== null && t.dangerouslySetInnerHTML.__html != null;
  }
  var kc = null;
  function xm() {
    var l = window.event;
    return l && l.type === "popstate" ? l === kc ? !1 : (kc = l, !0) : (kc = null, !1);
  }
  var Wo = typeof setTimeout == "function" ? setTimeout : void 0, Vm = typeof clearTimeout == "function" ? clearTimeout : void 0, $o = typeof Promise == "function" ? Promise : void 0, Km = typeof queueMicrotask == "function" ? queueMicrotask : typeof $o < "u" ? function(l) {
    return $o.resolve(null).then(l).catch(Jm);
  } : Wo;
  function Jm(l) {
    setTimeout(function() {
      throw l;
    });
  }
  function du(l) {
    return l === "head";
  }
  function Fo(l, t) {
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
          de(l.ownerDocument.documentElement);
        else if (u === "head") {
          u = l.ownerDocument.head, de(u);
          for (var n = u.firstChild; n; ) {
            var f = n.nextSibling, c = n.nodeName;
            n[Ha] || c === "SCRIPT" || c === "STYLE" || c === "LINK" && n.rel.toLowerCase() === "stylesheet" || u.removeChild(n), n = f;
          }
        } else
          u === "body" && de(l.ownerDocument.body);
      u = e;
    } while (u);
    Ma(t);
  }
  function ko(l, t) {
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
  function Ic(l) {
    var t = l.firstChild;
    for (t && t.nodeType === 10 && (t = t.nextSibling); t; ) {
      var u = t;
      switch (t = t.nextSibling, u.nodeName) {
        case "HTML":
        case "HEAD":
        case "BODY":
          Ic(u), uf(u);
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
  function wm(l, t, u, a) {
    for (; l.nodeType === 1; ) {
      var e = u;
      if (l.nodeName.toLowerCase() !== t.toLowerCase()) {
        if (!a && (l.nodeName !== "INPUT" || l.type !== "hidden"))
          break;
      } else if (a) {
        if (!l[Ha])
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
  function Wm(l, t, u) {
    if (t === "") return null;
    for (; l.nodeType !== 3; )
      if ((l.nodeType !== 1 || l.nodeName !== "INPUT" || l.type !== "hidden") && !u || (l = ht(l.nextSibling), l === null)) return null;
    return l;
  }
  function Io(l, t) {
    for (; l.nodeType !== 8; )
      if ((l.nodeType !== 1 || l.nodeName !== "INPUT" || l.type !== "hidden") && !t || (l = ht(l.nextSibling), l === null)) return null;
    return l;
  }
  function Pc(l) {
    return l.data === "$?" || l.data === "$~";
  }
  function li(l) {
    return l.data === "$!" || l.data === "$?" && l.ownerDocument.readyState !== "loading";
  }
  function $m(l, t) {
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
  var ti = null;
  function Po(l) {
    l = l.nextSibling;
    for (var t = 0; l; ) {
      if (l.nodeType === 8) {
        var u = l.data;
        if (u === "/$" || u === "/&") {
          if (t === 0)
            return ht(l.nextSibling);
          t--;
        } else
          u !== "$" && u !== "$!" && u !== "$?" && u !== "$~" && u !== "&" || t++;
      }
      l = l.nextSibling;
    }
    return null;
  }
  function ly(l) {
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
  function ty(l, t, u) {
    switch (t = Rn(u), l) {
      case "html":
        if (l = t.documentElement, !l) throw Error(v(452));
        return l;
      case "head":
        if (l = t.head, !l) throw Error(v(453));
        return l;
      case "body":
        if (l = t.body, !l) throw Error(v(454));
        return l;
      default:
        throw Error(v(451));
    }
  }
  function de(l) {
    for (var t = l.attributes; t.length; )
      l.removeAttributeNode(t[0]);
    uf(l);
  }
  var St = /* @__PURE__ */ new Map(), uy = /* @__PURE__ */ new Set();
  function Nn(l) {
    return typeof l.getRootNode == "function" ? l.getRootNode() : l.nodeType === 9 ? l : l.ownerDocument;
  }
  var wt = M.d;
  M.d = {
    f: Fm,
    r: km,
    D: Im,
    C: Pm,
    L: ld,
    m: td,
    X: ad,
    S: ud,
    M: ed
  };
  function Fm() {
    var l = wt.f(), t = zn();
    return l || t;
  }
  function km(l) {
    var t = Ku(l);
    t !== null && t.tag === 5 && t.type === "form" ? r0(t) : wt.r(l);
  }
  var Aa = typeof document > "u" ? null : document;
  function ay(l, t, u) {
    var a = Aa;
    if (a && typeof t == "string" && t) {
      var e = it(t);
      e = 'link[rel="' + l + '"][href="' + e + '"]', typeof u == "string" && (e += '[crossorigin="' + u + '"]'), uy.has(e) || (uy.add(e), l = { rel: l, crossOrigin: u, href: t }, a.querySelector(e) === null && (t = a.createElement("link"), Rl(t, "link", l), _l(t), a.head.appendChild(t)));
    }
  }
  function Im(l) {
    wt.D(l), ay("dns-prefetch", l, null);
  }
  function Pm(l, t) {
    wt.C(l, t), ay("preconnect", l, t);
  }
  function ld(l, t, u) {
    wt.L(l, t, u);
    var a = Aa;
    if (a && l && t) {
      var e = 'link[rel="preload"][as="' + it(t) + '"]';
      t === "image" && u && u.imageSrcSet ? (e += '[imagesrcset="' + it(
        u.imageSrcSet
      ) + '"]', typeof u.imageSizes == "string" && (e += '[imagesizes="' + it(
        u.imageSizes
      ) + '"]')) : e += '[href="' + it(l) + '"]';
      var n = e;
      switch (t) {
        case "style":
          n = _a(l);
          break;
        case "script":
          n = Oa(l);
      }
      St.has(n) || (l = q(
        {
          rel: "preload",
          href: t === "image" && u && u.imageSrcSet ? void 0 : l,
          as: t
        },
        u
      ), St.set(n, l), a.querySelector(e) !== null || t === "style" && a.querySelector(he(n)) || t === "script" && a.querySelector(Se(n)) || (t = a.createElement("link"), Rl(t, "link", l), _l(t), a.head.appendChild(t)));
    }
  }
  function td(l, t) {
    wt.m(l, t);
    var u = Aa;
    if (u && l) {
      var a = t && typeof t.as == "string" ? t.as : "script", e = 'link[rel="modulepreload"][as="' + it(a) + '"][href="' + it(l) + '"]', n = e;
      switch (a) {
        case "audioworklet":
        case "paintworklet":
        case "serviceworker":
        case "sharedworker":
        case "worker":
        case "script":
          n = Oa(l);
      }
      if (!St.has(n) && (l = q({ rel: "modulepreload", href: l }, t), St.set(n, l), u.querySelector(e) === null)) {
        switch (a) {
          case "audioworklet":
          case "paintworklet":
          case "serviceworker":
          case "sharedworker":
          case "worker":
          case "script":
            if (u.querySelector(Se(n)))
              return;
        }
        a = u.createElement("link"), Rl(a, "link", l), _l(a), u.head.appendChild(a);
      }
    }
  }
  function ud(l, t, u) {
    wt.S(l, t, u);
    var a = Aa;
    if (a && l) {
      var e = Ju(a).hoistableStyles, n = _a(l);
      t = t || "default";
      var f = e.get(n);
      if (!f) {
        var c = { loading: 0, preload: null };
        if (f = a.querySelector(
          he(n)
        ))
          c.loading = 5;
        else {
          l = q(
            { rel: "stylesheet", href: l, "data-precedence": t },
            u
          ), (u = St.get(n)) && ui(l, u);
          var i = f = a.createElement("link");
          _l(i), Rl(i, "link", l), i._p = new Promise(function(h, r) {
            i.onload = h, i.onerror = r;
          }), i.addEventListener("load", function() {
            c.loading |= 1;
          }), i.addEventListener("error", function() {
            c.loading |= 2;
          }), c.loading |= 4, Hn(f, t, a);
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
  function ad(l, t) {
    wt.X(l, t);
    var u = Aa;
    if (u && l) {
      var a = Ju(u).hoistableScripts, e = Oa(l), n = a.get(e);
      n || (n = u.querySelector(Se(e)), n || (l = q({ src: l, async: !0 }, t), (t = St.get(e)) && ai(l, t), n = u.createElement("script"), _l(n), Rl(n, "link", l), u.head.appendChild(n)), n = {
        type: "script",
        instance: n,
        count: 1,
        state: null
      }, a.set(e, n));
    }
  }
  function ed(l, t) {
    wt.M(l, t);
    var u = Aa;
    if (u && l) {
      var a = Ju(u).hoistableScripts, e = Oa(l), n = a.get(e);
      n || (n = u.querySelector(Se(e)), n || (l = q({ src: l, async: !0, type: "module" }, t), (t = St.get(e)) && ai(l, t), n = u.createElement("script"), _l(n), Rl(n, "link", l), u.head.appendChild(n)), n = {
        type: "script",
        instance: n,
        count: 1,
        state: null
      }, a.set(e, n));
    }
  }
  function ey(l, t, u, a) {
    var e = (e = Z.current) ? Nn(e) : null;
    if (!e) throw Error(v(446));
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
            he(l)
          )) && !n._p && (f.instance = n, f.state.loading = 5), St.has(l) || (u = {
            rel: "preload",
            as: "style",
            href: u.href,
            crossOrigin: u.crossOrigin,
            integrity: u.integrity,
            media: u.media,
            hrefLang: u.hrefLang,
            referrerPolicy: u.referrerPolicy
          }, St.set(l, u), n || nd(
            e,
            l,
            u,
            f.state
          ))), t && a === null)
            throw Error(v(528, ""));
          return f;
        }
        if (t && a !== null)
          throw Error(v(529, ""));
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
        throw Error(v(444, l));
    }
  }
  function _a(l) {
    return 'href="' + it(l) + '"';
  }
  function he(l) {
    return 'link[rel="stylesheet"][' + l + "]";
  }
  function ny(l) {
    return q({}, l, {
      "data-precedence": l.precedence,
      precedence: null
    });
  }
  function nd(l, t, u, a) {
    l.querySelector('link[rel="preload"][as="style"][' + t + "]") ? a.loading = 1 : (t = l.createElement("link"), a.preload = t, t.addEventListener("load", function() {
      return a.loading |= 1;
    }), t.addEventListener("error", function() {
      return a.loading |= 2;
    }), Rl(t, "link", u), _l(t), l.head.appendChild(t));
  }
  function Oa(l) {
    return '[src="' + it(l) + '"]';
  }
  function Se(l) {
    return "script[async]" + l;
  }
  function fy(l, t, u) {
    if (t.count++, t.instance === null)
      switch (t.type) {
        case "style":
          var a = l.querySelector(
            'style[data-href~="' + it(u.href) + '"]'
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
          ), _l(a), Rl(a, "style", e), Hn(a, u.precedence, l), t.instance = a;
        case "stylesheet":
          e = _a(u.href);
          var n = l.querySelector(
            he(e)
          );
          if (n)
            return t.state.loading |= 4, t.instance = n, _l(n), n;
          a = ny(u), (e = St.get(e)) && ui(a, e), n = (l.ownerDocument || l).createElement("link"), _l(n);
          var f = n;
          return f._p = new Promise(function(c, i) {
            f.onload = c, f.onerror = i;
          }), Rl(n, "link", a), t.state.loading |= 4, Hn(n, u.precedence, l), t.instance = n;
        case "script":
          return n = Oa(u.src), (e = l.querySelector(
            Se(n)
          )) ? (t.instance = e, _l(e), e) : (a = u, (e = St.get(n)) && (a = q({}, u), ai(a, e)), l = l.ownerDocument || l, e = l.createElement("script"), _l(e), Rl(e, "link", a), l.head.appendChild(e), t.instance = e);
        case "void":
          return null;
        default:
          throw Error(v(443, t.type));
      }
    else
      t.type === "stylesheet" && (t.state.loading & 4) === 0 && (a = t.instance, t.state.loading |= 4, Hn(a, u.precedence, l));
    return t.instance;
  }
  function Hn(l, t, u) {
    for (var a = u.querySelectorAll(
      'link[rel="stylesheet"][data-precedence],style[data-precedence]'
    ), e = a.length ? a[a.length - 1] : null, n = e, f = 0; f < a.length; f++) {
      var c = a[f];
      if (c.dataset.precedence === t) n = c;
      else if (n !== e) break;
    }
    n ? n.parentNode.insertBefore(l, n.nextSibling) : (t = u.nodeType === 9 ? u.head : u, t.insertBefore(l, t.firstChild));
  }
  function ui(l, t) {
    l.crossOrigin == null && (l.crossOrigin = t.crossOrigin), l.referrerPolicy == null && (l.referrerPolicy = t.referrerPolicy), l.title == null && (l.title = t.title);
  }
  function ai(l, t) {
    l.crossOrigin == null && (l.crossOrigin = t.crossOrigin), l.referrerPolicy == null && (l.referrerPolicy = t.referrerPolicy), l.integrity == null && (l.integrity = t.integrity);
  }
  var Cn = null;
  function cy(l, t, u) {
    if (Cn === null) {
      var a = /* @__PURE__ */ new Map(), e = Cn = /* @__PURE__ */ new Map();
      e.set(u, a);
    } else
      e = Cn, a = e.get(u), a || (a = /* @__PURE__ */ new Map(), e.set(u, a));
    if (a.has(l)) return a;
    for (a.set(l, null), u = u.getElementsByTagName(l), e = 0; e < u.length; e++) {
      var n = u[e];
      if (!(n[Ha] || n[Ml] || l === "link" && n.getAttribute("rel") === "stylesheet") && n.namespaceURI !== "http://www.w3.org/2000/svg") {
        var f = n.getAttribute(t) || "";
        f = l + f;
        var c = a.get(f);
        c ? c.push(n) : a.set(f, [n]);
      }
    }
    return a;
  }
  function iy(l, t, u) {
    l = l.ownerDocument || l, l.head.insertBefore(
      u,
      t === "title" ? l.querySelector("head > title") : null
    );
  }
  function fd(l, t, u) {
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
  function sy(l) {
    return !(l.type === "stylesheet" && (l.state.loading & 3) === 0);
  }
  function cd(l, t, u, a) {
    if (u.type === "stylesheet" && (typeof a.media != "string" || matchMedia(a.media).matches !== !1) && (u.state.loading & 4) === 0) {
      if (u.instance === null) {
        var e = _a(a.href), n = t.querySelector(
          he(e)
        );
        if (n) {
          t = n._p, t !== null && typeof t == "object" && typeof t.then == "function" && (l.count++, l = qn.bind(l), t.then(l, l)), u.state.loading |= 4, u.instance = n, _l(n);
          return;
        }
        n = t.ownerDocument || t, a = ny(a), (e = St.get(e)) && ui(a, e), n = n.createElement("link"), _l(n);
        var f = n;
        f._p = new Promise(function(c, i) {
          f.onload = c, f.onerror = i;
        }), Rl(n, "link", a), u.instance = n;
      }
      l.stylesheets === null && (l.stylesheets = /* @__PURE__ */ new Map()), l.stylesheets.set(u, t), (t = u.state.preload) && (u.state.loading & 3) === 0 && (l.count++, u = qn.bind(l), t.addEventListener("load", u), t.addEventListener("error", u));
    }
  }
  var ei = 0;
  function id(l, t) {
    return l.stylesheets && l.count === 0 && Yn(l, l.stylesheets), 0 < l.count || 0 < l.imgCount ? function(u) {
      var a = setTimeout(function() {
        if (l.stylesheets && Yn(l, l.stylesheets), l.unsuspend) {
          var n = l.unsuspend;
          l.unsuspend = null, n();
        }
      }, 6e4 + t);
      0 < l.imgBytes && ei === 0 && (ei = 62500 * Lm());
      var e = setTimeout(
        function() {
          if (l.waitingForImages = !1, l.count === 0 && (l.stylesheets && Yn(l, l.stylesheets), l.unsuspend)) {
            var n = l.unsuspend;
            l.unsuspend = null, n();
          }
        },
        (l.imgBytes > ei ? 50 : 800) + t
      );
      return l.unsuspend = u, function() {
        l.unsuspend = null, clearTimeout(a), clearTimeout(e);
      };
    } : null;
  }
  function qn() {
    if (this.count--, this.count === 0 && (this.imgCount === 0 || !this.waitingForImages)) {
      if (this.stylesheets) Yn(this, this.stylesheets);
      else if (this.unsuspend) {
        var l = this.unsuspend;
        this.unsuspend = null, l();
      }
    }
  }
  var Bn = null;
  function Yn(l, t) {
    l.stylesheets = null, l.unsuspend !== null && (l.count++, Bn = /* @__PURE__ */ new Map(), t.forEach(sd, l), Bn = null, qn.call(l));
  }
  function sd(l, t) {
    if (!(t.state.loading & 4)) {
      var u = Bn.get(l);
      if (u) var a = u.get(null);
      else {
        u = /* @__PURE__ */ new Map(), Bn.set(l, u);
        for (var e = l.querySelectorAll(
          "link[data-precedence],style[data-precedence]"
        ), n = 0; n < e.length; n++) {
          var f = e[n];
          (f.nodeName === "LINK" || f.getAttribute("media") !== "not all") && (u.set(f.dataset.precedence, f), a = f);
        }
        a && u.set(null, a);
      }
      e = t.instance, f = e.getAttribute("data-precedence"), n = u.get(f) || a, n === a && u.set(null, e), u.set(f, e), this.count++, a = qn.bind(this), e.addEventListener("load", a), e.addEventListener("error", a), n ? n.parentNode.insertBefore(e, n.nextSibling) : (l = l.nodeType === 9 ? l.head : l, l.insertBefore(e, l.firstChild)), t.state.loading |= 4;
    }
  }
  var ge = {
    $$typeof: Nl,
    Provider: null,
    Consumer: null,
    _currentValue: B,
    _currentValue2: B,
    _threadCount: 0
  };
  function od(l, t, u, a, e, n, f, c, i) {
    this.tag = 1, this.containerInfo = l, this.pingCache = this.current = this.pendingChildren = null, this.timeoutHandle = -1, this.callbackNode = this.next = this.pendingContext = this.context = this.cancelPendingCommit = null, this.callbackPriority = 0, this.expirationTimes = In(-1), this.entangledLanes = this.shellSuspendCounter = this.errorRecoveryDisabledLanes = this.expiredLanes = this.warmLanes = this.pingedLanes = this.suspendedLanes = this.pendingLanes = 0, this.entanglements = In(0), this.hiddenUpdates = In(null), this.identifierPrefix = a, this.onUncaughtError = e, this.onCaughtError = n, this.onRecoverableError = f, this.pooledCache = null, this.pooledCacheLanes = 0, this.formState = i, this.incompleteTransitions = /* @__PURE__ */ new Map();
  }
  function oy(l, t, u, a, e, n, f, c, i, h, r, E) {
    return l = new od(
      l,
      t,
      u,
      f,
      i,
      h,
      r,
      E,
      c
    ), t = 1, n === !0 && (t |= 24), n = Pl(3, null, null, t), l.current = n, n.stateNode = l, t = Gf(), t.refCount++, l.pooledCache = t, t.refCount++, n.memoizedState = {
      element: a,
      isDehydrated: u,
      cache: t
    }, Zf(n), l;
  }
  function yy(l) {
    return l ? (l = ua, l) : ua;
  }
  function vy(l, t, u, a, e, n) {
    e = yy(e), a.context === null ? a.context = e : a.pendingContext = e, a = au(t), a.payload = { element: u }, n = n === void 0 ? null : n, n !== null && (a.callback = n), u = eu(l, a, t), u !== null && (Jl(u, l, t), $a(u, l, t));
  }
  function my(l, t) {
    if (l = l.memoizedState, l !== null && l.dehydrated !== null) {
      var u = l.retryLane;
      l.retryLane = u !== 0 && u < t ? u : t;
    }
  }
  function ni(l, t) {
    my(l, t), (l = l.alternate) && my(l, t);
  }
  function dy(l) {
    if (l.tag === 13 || l.tag === 31) {
      var t = Uu(l, 67108864);
      t !== null && Jl(t, l, 67108864), ni(l, 67108864);
    }
  }
  function hy(l) {
    if (l.tag === 13 || l.tag === 31) {
      var t = et();
      t = Pn(t);
      var u = Uu(l, t);
      u !== null && Jl(u, l, t), ni(l, t);
    }
  }
  var Gn = !0;
  function yd(l, t, u, a) {
    var e = b.T;
    b.T = null;
    var n = M.p;
    try {
      M.p = 2, fi(l, t, u, a);
    } finally {
      M.p = n, b.T = e;
    }
  }
  function vd(l, t, u, a) {
    var e = b.T;
    b.T = null;
    var n = M.p;
    try {
      M.p = 8, fi(l, t, u, a);
    } finally {
      M.p = n, b.T = e;
    }
  }
  function fi(l, t, u, a) {
    if (Gn) {
      var e = ci(a);
      if (e === null)
        Jc(
          l,
          t,
          a,
          Xn,
          u
        ), gy(l, a);
      else if (dd(
        e,
        l,
        t,
        u,
        a
      ))
        a.stopPropagation();
      else if (gy(l, a), t & 4 && -1 < md.indexOf(l)) {
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
                    pt(n), (I & 6) === 0 && (Tn = $l() + 500, ye(0));
                  }
                }
                break;
              case 31:
              case 13:
                c = Uu(n, 2), c !== null && Jl(c, n, 2), zn(), ni(n, 2);
            }
          if (n = ci(a), n === null && Jc(
            l,
            t,
            a,
            Xn,
            u
          ), n === e) break;
          e = n;
        }
        e !== null && a.stopPropagation();
      } else
        Jc(
          l,
          t,
          a,
          null,
          u
        );
    }
  }
  function ci(l) {
    return l = sf(l), ii(l);
  }
  var Xn = null;
  function ii(l) {
    if (Xn = null, l = Vu(l), l !== null) {
      var t = W(l);
      if (t === null) l = null;
      else {
        var u = t.tag;
        if (u === 13) {
          if (l = al(t), l !== null) return l;
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
    return Xn = l, null;
  }
  function Sy(l) {
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
        switch (Iy()) {
          case Ai:
            return 2;
          case _i:
            return 8;
          case Me:
          case Py:
            return 32;
          case Oi:
            return 268435456;
          default:
            return 32;
        }
      default:
        return 32;
    }
  }
  var si = !1, hu = null, Su = null, gu = null, re = /* @__PURE__ */ new Map(), be = /* @__PURE__ */ new Map(), ru = [], md = "mousedown mouseup touchcancel touchend touchstart auxclick dblclick pointercancel pointerdown pointerup dragend dragstart drop compositionend compositionstart keydown keypress keyup input textInput copy cut paste click change contextmenu reset".split(
    " "
  );
  function gy(l, t) {
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
        re.delete(t.pointerId);
        break;
      case "gotpointercapture":
      case "lostpointercapture":
        be.delete(t.pointerId);
    }
  }
  function Te(l, t, u, a, e, n) {
    return l === null || l.nativeEvent !== n ? (l = {
      blockedOn: t,
      domEventName: u,
      eventSystemFlags: a,
      nativeEvent: n,
      targetContainers: [e]
    }, t !== null && (t = Ku(t), t !== null && dy(t)), l) : (l.eventSystemFlags |= a, t = l.targetContainers, e !== null && t.indexOf(e) === -1 && t.push(e), l);
  }
  function dd(l, t, u, a, e) {
    switch (t) {
      case "focusin":
        return hu = Te(
          hu,
          l,
          t,
          u,
          a,
          e
        ), !0;
      case "dragenter":
        return Su = Te(
          Su,
          l,
          t,
          u,
          a,
          e
        ), !0;
      case "mouseover":
        return gu = Te(
          gu,
          l,
          t,
          u,
          a,
          e
        ), !0;
      case "pointerover":
        var n = e.pointerId;
        return re.set(
          n,
          Te(
            re.get(n) || null,
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
          Te(
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
  function ry(l) {
    var t = Vu(l.target);
    if (t !== null) {
      var u = W(t);
      if (u !== null) {
        if (t = u.tag, t === 13) {
          if (t = al(u), t !== null) {
            l.blockedOn = t, Ni(l.priority, function() {
              hy(u);
            });
            return;
          }
        } else if (t === 31) {
          if (t = hl(u), t !== null) {
            l.blockedOn = t, Ni(l.priority, function() {
              hy(u);
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
  function Qn(l) {
    if (l.blockedOn !== null) return !1;
    for (var t = l.targetContainers; 0 < t.length; ) {
      var u = ci(l.nativeEvent);
      if (u === null) {
        u = l.nativeEvent;
        var a = new u.constructor(
          u.type,
          u
        );
        cf = a, u.target.dispatchEvent(a), cf = null;
      } else
        return t = Ku(u), t !== null && dy(t), l.blockedOn = u, !1;
      t.shift();
    }
    return !0;
  }
  function by(l, t, u) {
    Qn(l) && u.delete(t);
  }
  function hd() {
    si = !1, hu !== null && Qn(hu) && (hu = null), Su !== null && Qn(Su) && (Su = null), gu !== null && Qn(gu) && (gu = null), re.forEach(by), be.forEach(by);
  }
  function jn(l, t) {
    l.blockedOn === t && (l.blockedOn = null, si || (si = !0, m.unstable_scheduleCallback(
      m.unstable_NormalPriority,
      hd
    )));
  }
  var Zn = null;
  function Ty(l) {
    Zn !== l && (Zn = l, m.unstable_scheduleCallback(
      m.unstable_NormalPriority,
      function() {
        Zn === l && (Zn = null);
        for (var t = 0; t < l.length; t += 3) {
          var u = l[t], a = l[t + 1], e = l[t + 2];
          if (typeof a != "function") {
            if (ii(a || u) === null)
              continue;
            break;
          }
          var n = Ku(u);
          n !== null && (l.splice(t, 3), t -= 3, cc(
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
      return jn(i, l);
    }
    hu !== null && jn(hu, l), Su !== null && jn(Su, l), gu !== null && jn(gu, l), re.forEach(t), be.forEach(t);
    for (var u = 0; u < ru.length; u++) {
      var a = ru[u];
      a.blockedOn === l && (a.blockedOn = null);
    }
    for (; 0 < ru.length && (u = ru[0], u.blockedOn === null); )
      ry(u), u.blockedOn === null && ru.shift();
    if (u = (l.ownerDocument || l).$$reactFormReplay, u != null)
      for (a = 0; a < u.length; a += 3) {
        var e = u[a], n = u[a + 1], f = e[jl] || null;
        if (typeof n == "function")
          f || Ty(u);
        else if (f) {
          var c = null;
          if (n && n.hasAttribute("formAction")) {
            if (e = n, f = n[jl] || null)
              c = f.formAction;
            else if (ii(e) !== null) continue;
          } else c = f.action;
          typeof c == "function" ? u[a + 1] = c : (u.splice(a, 3), a -= 3), Ty(u);
        }
      }
  }
  function Ey() {
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
  function oi(l) {
    this._internalRoot = l;
  }
  Ln.prototype.render = oi.prototype.render = function(l) {
    var t = this._internalRoot;
    if (t === null) throw Error(v(409));
    var u = t.current, a = et();
    vy(u, a, l, t, null, null);
  }, Ln.prototype.unmount = oi.prototype.unmount = function() {
    var l = this._internalRoot;
    if (l !== null) {
      this._internalRoot = null;
      var t = l.containerInfo;
      vy(l.current, 2, null, l, null, null), zn(), t[xu] = null;
    }
  };
  function Ln(l) {
    this._internalRoot = l;
  }
  Ln.prototype.unstable_scheduleHydration = function(l) {
    if (l) {
      var t = Ri();
      l = { blockedOn: null, target: l, priority: t };
      for (var u = 0; u < ru.length && t !== 0 && t < ru[u].priority; u++) ;
      ru.splice(u, 0, l), u === 0 && ry(l);
    }
  };
  var zy = _.version;
  if (zy !== "19.2.4")
    throw Error(
      v(
        527,
        zy,
        "19.2.4"
      )
    );
  M.findDOMNode = function(l) {
    var t = l._reactInternals;
    if (t === void 0)
      throw typeof l.render == "function" ? Error(v(188)) : (l = Object.keys(l).join(","), Error(v(268, l)));
    return l = A(t), l = l !== null ? $(l) : null, l = l === null ? null : l.stateNode, l;
  };
  var Sd = {
    bundleType: 0,
    version: "19.2.4",
    rendererPackageName: "react-dom",
    currentDispatcherRef: b,
    reconcilerVersion: "19.2.4"
  };
  if (typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ < "u") {
    var xn = __REACT_DEVTOOLS_GLOBAL_HOOK__;
    if (!xn.isDisabled && xn.supportsFiber)
      try {
        Ua = xn.inject(
          Sd
        ), Fl = xn;
      } catch {
      }
  }
  return ze.createRoot = function(l, t) {
    if (!Y(l)) throw Error(v(299));
    var u = !1, a = "", e = D0, n = U0, f = R0;
    return t != null && (t.unstable_strictMode === !0 && (u = !0), t.identifierPrefix !== void 0 && (a = t.identifierPrefix), t.onUncaughtError !== void 0 && (e = t.onUncaughtError), t.onCaughtError !== void 0 && (n = t.onCaughtError), t.onRecoverableError !== void 0 && (f = t.onRecoverableError)), t = oy(
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
      Ey
    ), l[xu] = t.current, Kc(l), new oi(t);
  }, ze.hydrateRoot = function(l, t, u) {
    if (!Y(l)) throw Error(v(299));
    var a = !1, e = "", n = D0, f = U0, c = R0, i = null;
    return u != null && (u.unstable_strictMode === !0 && (a = !0), u.identifierPrefix !== void 0 && (e = u.identifierPrefix), u.onUncaughtError !== void 0 && (n = u.onUncaughtError), u.onCaughtError !== void 0 && (f = u.onCaughtError), u.onRecoverableError !== void 0 && (c = u.onRecoverableError), u.formState !== void 0 && (i = u.formState)), t = oy(
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
      Ey
    ), t.context = yy(null), u = t.current, a = et(), a = Pn(a), e = au(a), e.callback = null, eu(u, e, a), u = a, t.current.lanes = u, Na(t, u), pt(t), l[xu] = t.current, Kc(l), new Ln(t);
  }, ze.version = "19.2.4", ze;
}
var Hy;
function Ud() {
  if (Hy) return di.exports;
  Hy = 1;
  function m() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(m);
      } catch (_) {
        console.error(_);
      }
  }
  return m(), di.exports = Dd(), di.exports;
}
var Rd = Ud(), Vy = xy();
const Qd = /* @__PURE__ */ Ly(Vy);
class Ky {
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
const pa = nt.createContext(null), Eu = /* @__PURE__ */ new Map();
let Jy = "", Cy = !1;
function Kn() {
  return Jy + "/";
}
function wy(m, _, O, v, Y) {
  Y !== void 0 && (Jy = Y), Cy || (Cy = !0, Td(Kn() + "react-api/events"));
  const W = document.getElementById(m);
  if (!W) {
    console.error("[TLReact] Mount point not found:", m);
    return;
  }
  const al = Qy(_);
  if (!al) {
    console.error("[TLReact] Component not registered:", _);
    return;
  }
  const hl = new Ky(O);
  O.hidden === !0 && (W.style.display = "none"), jy(m, (P) => {
    hl.applyPatch(P);
  });
  const A = Rd.createRoot(W);
  Eu.set(m, { root: A, store: hl });
  const $ = v ?? "";
  Wy = $;
  const q = () => {
    const P = nt.useSyncExternalStore(hl.subscribeStore, hl.getSnapshot);
    return nt.useLayoutEffect(() => {
      W.style.display = P.hidden === !0 ? "none" : "";
    }, [P.hidden]), mi.createElement(
      pa.Provider,
      { value: { controlId: m, windowName: $, store: hl } },
      mi.createElement(al, { controlId: m, state: P })
    );
  };
  Vy.flushSync(() => {
    A.render(mi.createElement(q));
  });
}
function Nd(m, _, O) {
  wy(m, _, O);
}
function qy(m) {
  const _ = Eu.get(m);
  _ && (_.root.unmount(), Eu.delete(m));
}
function Hd(m, _) {
  let O = Eu.get(m);
  if (!O) {
    const Y = new Ky(_);
    jy(m, (al) => {
      Y.applyPatch(al);
    }), Eu.set(m, { root: null, store: Y }), O = Eu.get(m);
  }
  return { controlId: m, windowName: Wy, store: O.store };
}
let Wy = "";
function Cd() {
  const m = nt.useContext(pa);
  if (!m)
    throw new Error("useTLState must be used inside a TLReact-mounted component.");
  return nt.useSyncExternalStore(m.store.subscribeStore, m.store.getSnapshot);
}
function qd() {
  const m = nt.useContext(pa);
  if (!m)
    throw new Error("useTLCommand must be used inside a TLReact-mounted component.");
  const _ = m.controlId, O = m.windowName;
  return nt.useCallback(
    async (v, Y) => {
      const W = JSON.stringify({
        controlId: _,
        command: v,
        windowName: O,
        arguments: Y ?? {}
      });
      try {
        const al = await fetch(Kn() + "react-api/command", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: W
        });
        al.ok || console.error("[TLReact] Command failed:", al.status, await al.text());
      } catch (al) {
        console.error("[TLReact] Command error:", al);
      }
    },
    [_, O]
  );
}
function jd() {
  const m = nt.useContext(pa);
  if (!m)
    throw new Error("useTLUpload must be used inside a TLReact-mounted component.");
  const _ = m.controlId, O = m.windowName;
  return nt.useCallback(
    async (v) => {
      v.append("controlId", _), v.append("windowName", O);
      try {
        const Y = await fetch(Kn() + "react-api/upload", {
          method: "POST",
          body: v
        });
        Y.ok || console.error("[TLReact] Upload failed:", Y.status, await Y.text());
      } catch (Y) {
        console.error("[TLReact] Upload error:", Y);
      }
    },
    [_, O]
  );
}
function Zd() {
  const m = nt.useContext(pa);
  if (!m)
    throw new Error("useTLDataUrl must be used inside a TLReact-mounted component.");
  return Kn() + "react-api/data?controlId=" + encodeURIComponent(m.controlId);
}
function Ld() {
  const m = Cd(), _ = qd(), O = nt.useCallback(
    (v) => {
      _("valueChanged", { value: v });
    },
    [_]
  );
  return [m.value, O];
}
function By() {
  new MutationObserver((_) => {
    for (const O of _)
      for (const v of O.removedNodes)
        if (v instanceof HTMLElement) {
          const Y = v.id;
          Y && Eu.has(Y) && qy(Y);
          for (const [W] of Eu)
            v.querySelector("#" + CSS.escape(W)) && qy(W);
        }
  }).observe(document.body, { childList: !0, subtree: !0 });
}
document.readyState === "loading" ? document.addEventListener("DOMContentLoaded", () => {
  By();
}) : By();
var ri = { exports: {} }, Ae = {};
/**
 * @license React
 * react-jsx-runtime.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var Yy;
function Bd() {
  if (Yy) return Ae;
  Yy = 1;
  var m = Symbol.for("react.transitional.element"), _ = Symbol.for("react.fragment");
  function O(v, Y, W) {
    var al = null;
    if (W !== void 0 && (al = "" + W), Y.key !== void 0 && (al = "" + Y.key), "key" in Y) {
      W = {};
      for (var hl in Y)
        hl !== "key" && (W[hl] = Y[hl]);
    } else W = Y;
    return Y = W.ref, {
      $$typeof: m,
      type: v,
      key: al,
      ref: Y !== void 0 ? Y : null,
      props: W
    };
  }
  return Ae.Fragment = _, Ae.jsx = O, Ae.jsxs = O, Ae;
}
var Gy;
function Yd() {
  return Gy || (Gy = 1, ri.exports = Bd()), ri.exports;
}
var bi = Yd();
const xd = ({ control: m }) => {
  const _ = m, O = Qy(_.module), v = nt.useMemo(
    () => Hd(_.controlId, _.state),
    [_.controlId]
  );
  return O ? /* @__PURE__ */ bi.jsx(pa.Provider, { value: v, children: /* @__PURE__ */ bi.jsx(O, { controlId: _.controlId, state: _.state }) }) : /* @__PURE__ */ bi.jsxs("span", { children: [
    "[Component not registered: ",
    _.module,
    "]"
  ] });
};
window.TLReact = { mount: wy, mountField: Nd };
export {
  mi as React,
  Qd as ReactDOM,
  xd as TLChild,
  pa as TLControlContext,
  Td as connect,
  Hd as createChildContext,
  Qy as getComponent,
  wy as mount,
  Nd as mountField,
  Gd as register,
  jy as subscribe,
  qy as unmount,
  Xd as unsubscribe,
  qd as useTLCommand,
  Zd as useTLDataUrl,
  Ld as useTLFieldValue,
  Cd as useTLState,
  jd as useTLUpload
};
