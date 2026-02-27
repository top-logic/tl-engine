const xy = /* @__PURE__ */ new Map();
function Pd(v, A) {
  xy.set(v, A);
}
function Vy(v) {
  return xy.get(v);
}
function Ky(v) {
  return v && v.__esModule && Object.prototype.hasOwnProperty.call(v, "default") ? v.default : v;
}
var mi = { exports: {} }, G = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var Uy;
function pd() {
  if (Uy) return G;
  Uy = 1;
  var v = Symbol.for("react.transitional.element"), A = Symbol.for("react.portal"), O = Symbol.for("react.fragment"), m = Symbol.for("react.strict_mode"), q = Symbol.for("react.profiler"), V = Symbol.for("react.consumer"), P = Symbol.for("react.context"), Al = Symbol.for("react.forward_ref"), R = Symbol.for("react.suspense"), _ = Symbol.for("react.memo"), $ = Symbol.for("react.lazy"), B = Symbol.for("react.activity"), ll = Symbol.iterator;
  function wl(o) {
    return o === null || typeof o != "object" ? null : (o = ll && o[ll] || o["@@iterator"], typeof o == "function" ? o : null);
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
  function Rl(o, z, p) {
    this.props = o, this.context = z, this.refs = Dt, this.updater = p || Yl;
  }
  var ft = Rl.prototype = new Wt();
  ft.constructor = Rl, Cl(ft, Wl.prototype), ft.isPureReactComponent = !0;
  var Et = Array.isArray;
  function Gl() {
  }
  var w = { H: null, A: null, T: null, S: null }, jl = Object.prototype.hasOwnProperty;
  function zt(o, z, p) {
    var U = p.ref;
    return {
      $$typeof: v,
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
    return typeof o == "object" && o !== null && o.$$typeof === v;
  }
  function Xl(o) {
    var z = { "=": "=0", ":": "=2" };
    return "$" + o.replace(/[=:]/g, function(p) {
      return z[p];
    });
  }
  var Eu = /\/+/g;
  function Ut(o, z) {
    return typeof o == "object" && o !== null && o.key != null ? Xl("" + o.key) : z.toString(36);
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
  function b(o, z, p, U, j) {
    var Z = typeof o;
    (Z === "undefined" || Z === "boolean") && (o = null);
    var tl = !1;
    if (o === null) tl = !0;
    else
      switch (Z) {
        case "bigint":
        case "string":
        case "number":
          tl = !0;
          break;
        case "object":
          switch (o.$$typeof) {
            case v:
            case A:
              tl = !0;
              break;
            case $:
              return tl = o._init, b(
                tl(o._payload),
                z,
                p,
                U,
                j
              );
          }
      }
    if (tl)
      return j = j(o), tl = U === "" ? "." + Ut(o, 0) : U, Et(j) ? (p = "", tl != null && (p = tl.replace(Eu, "$&/") + "/"), b(j, z, p, "", function(Da) {
        return Da;
      })) : j != null && (At(j) && (j = Lu(
        j,
        p + (j.key == null || o && o.key === j.key ? "" : ("" + j.key).replace(
          Eu,
          "$&/"
        ) + "/") + tl
      )), z.push(j)), 1;
    tl = 0;
    var ql = U === "" ? "." : U + ":";
    if (Et(o))
      for (var hl = 0; hl < o.length; hl++)
        U = o[hl], Z = ql + Ut(U, hl), tl += b(
          U,
          z,
          p,
          Z,
          j
        );
    else if (hl = wl(o), typeof hl == "function")
      for (o = hl.call(o), hl = 0; !(U = o.next()).done; )
        U = U.value, Z = ql + Ut(U, hl++), tl += b(
          U,
          z,
          p,
          Z,
          j
        );
    else if (Z === "object") {
      if (typeof o.then == "function")
        return b(
          gt(o),
          z,
          p,
          U,
          j
        );
      throw z = String(o), Error(
        "Objects are not valid as a React child (found: " + (z === "[object Object]" ? "object with keys {" + Object.keys(o).join(", ") + "}" : z) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return tl;
  }
  function M(o, z, p) {
    if (o == null) return o;
    var U = [], j = 0;
    return b(o, U, "", "", function(Z) {
      return z.call(p, Z, j++);
    }), U;
  }
  function Y(o) {
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
  return G.Activity = B, G.Children = il, G.Component = Wl, G.Fragment = O, G.Profiler = q, G.PureComponent = Rl, G.StrictMode = m, G.Suspense = R, G.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = w, G.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(o) {
      return w.H.useMemoCache(o);
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
    var U = Cl({}, o.props), j = o.key;
    if (z != null)
      for (Z in z.key !== void 0 && (j = "" + z.key), z)
        !jl.call(z, Z) || Z === "key" || Z === "__self" || Z === "__source" || Z === "ref" && z.ref === void 0 || (U[Z] = z[Z]);
    var Z = arguments.length - 2;
    if (Z === 1) U.children = p;
    else if (1 < Z) {
      for (var tl = Array(Z), ql = 0; ql < Z; ql++)
        tl[ql] = arguments[ql + 2];
      U.children = tl;
    }
    return zt(o.type, j, U);
  }, G.createContext = function(o) {
    return o = {
      $$typeof: P,
      _currentValue: o,
      _currentValue2: o,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, o.Provider = o, o.Consumer = {
      $$typeof: V,
      _context: o
    }, o;
  }, G.createElement = function(o, z, p) {
    var U, j = {}, Z = null;
    if (z != null)
      for (U in z.key !== void 0 && (Z = "" + z.key), z)
        jl.call(z, U) && U !== "key" && U !== "__self" && U !== "__source" && (j[U] = z[U]);
    var tl = arguments.length - 2;
    if (tl === 1) j.children = p;
    else if (1 < tl) {
      for (var ql = Array(tl), hl = 0; hl < tl; hl++)
        ql[hl] = arguments[hl + 2];
      j.children = ql;
    }
    if (o && o.defaultProps)
      for (U in tl = o.defaultProps, tl)
        j[U] === void 0 && (j[U] = tl[U]);
    return zt(o, Z, j);
  }, G.createRef = function() {
    return { current: null };
  }, G.forwardRef = function(o) {
    return { $$typeof: Al, render: o };
  }, G.isValidElement = At, G.lazy = function(o) {
    return {
      $$typeof: $,
      _payload: { _status: -1, _result: o },
      _init: Y
    };
  }, G.memo = function(o, z) {
    return {
      $$typeof: _,
      type: o,
      compare: z === void 0 ? null : z
    };
  }, G.startTransition = function(o) {
    var z = w.T, p = {};
    w.T = p;
    try {
      var U = o(), j = w.S;
      j !== null && j(p, U), typeof U == "object" && U !== null && typeof U.then == "function" && U.then(Gl, el);
    } catch (Z) {
      el(Z);
    } finally {
      z !== null && p.types !== null && (z.types = p.types), w.T = z;
    }
  }, G.unstable_useCacheRefresh = function() {
    return w.H.useCacheRefresh();
  }, G.use = function(o) {
    return w.H.use(o);
  }, G.useActionState = function(o, z, p) {
    return w.H.useActionState(o, z, p);
  }, G.useCallback = function(o, z) {
    return w.H.useCallback(o, z);
  }, G.useContext = function(o) {
    return w.H.useContext(o);
  }, G.useDebugValue = function() {
  }, G.useDeferredValue = function(o, z) {
    return w.H.useDeferredValue(o, z);
  }, G.useEffect = function(o, z) {
    return w.H.useEffect(o, z);
  }, G.useEffectEvent = function(o) {
    return w.H.useEffectEvent(o);
  }, G.useId = function() {
    return w.H.useId();
  }, G.useImperativeHandle = function(o, z, p) {
    return w.H.useImperativeHandle(o, z, p);
  }, G.useInsertionEffect = function(o, z) {
    return w.H.useInsertionEffect(o, z);
  }, G.useLayoutEffect = function(o, z) {
    return w.H.useLayoutEffect(o, z);
  }, G.useMemo = function(o, z) {
    return w.H.useMemo(o, z);
  }, G.useOptimistic = function(o, z) {
    return w.H.useOptimistic(o, z);
  }, G.useReducer = function(o, z, p) {
    return w.H.useReducer(o, z, p);
  }, G.useRef = function(o) {
    return w.H.useRef(o);
  }, G.useState = function(o) {
    return w.H.useState(o);
  }, G.useSyncExternalStore = function(o, z, p) {
    return w.H.useSyncExternalStore(
      o,
      z,
      p
    );
  }, G.useTransition = function() {
    return w.H.useTransition();
  }, G.version = "19.2.4", G;
}
var Ny;
function _i() {
  return Ny || (Ny = 1, mi.exports = pd()), mi.exports;
}
var nt = _i();
const Kn = /* @__PURE__ */ Ky(nt), Oi = /* @__PURE__ */ new Map(), Jn = /* @__PURE__ */ new Set();
let Ei = !1, Mi = 0;
const zi = /* @__PURE__ */ new Set();
let Jy = "", wy = "";
function Dd(v) {
  Jy = v;
}
function Ud(v) {
  wy = v;
}
function Wy() {
  for (const v of zi)
    v();
}
function Nd(v) {
  return zi.add(v), () => zi.delete(v);
}
function Rd() {
  return Mi;
}
function Hd(v) {
  Jn.add(v), Ei || (Ei = !0, queueMicrotask(Cd));
}
async function Cd() {
  if (Ei = !1, Jn.size === 0)
    return;
  const v = Array.from(Jn);
  Jn.clear();
  try {
    const A = Jy + "react-api/i18n?keys=" + encodeURIComponent(v.join(",")) + "&windowName=" + encodeURIComponent(wy), O = await fetch(A);
    if (!O.ok) {
      console.error("[TLReact] i18n fetch failed:", O.status);
      return;
    }
    const m = await O.json();
    for (const [q, V] of Object.entries(m))
      Oi.set(q, V);
    Mi++, Wy();
  } catch (A) {
    console.error("[TLReact] i18n fetch error:", A);
  }
}
function l1(v) {
  Kn.useSyncExternalStore(Nd, Rd);
  const A = {};
  for (const [O, m] of Object.entries(v)) {
    const q = Oi.get(O);
    q !== void 0 ? A[O] = q : (A[O] = m, Hd(O));
  }
  return A;
}
function $y() {
  Oi.clear(), Mi++, Wy();
}
const _e = /* @__PURE__ */ new Map();
let Ee = null, Ry = null, wn = 0, di = null;
const qd = 45e3, Bd = 15e3;
function Yd(v) {
  Ry = v, di && clearInterval(di), Hy(v), di = setInterval(() => {
    wn > 0 && Date.now() - wn > qd && (console.warn("[TLReact] No heartbeat received, reconnecting SSE."), Hy(Ry));
  }, Bd);
}
function Hy(v) {
  Ee && Ee.close(), Ee = new EventSource(v), wn = Date.now(), $y(), Ee.onmessage = (A) => {
    wn = Date.now();
    try {
      const O = JSON.parse(A.data);
      jd(O);
    } catch (O) {
      console.error("[TLReact] Failed to parse SSE event:", O);
    }
  }, Ee.onerror = () => {
    console.warn("[TLReact] SSE connection error, will reconnect automatically.");
  };
}
function Fy(v, A) {
  let O = _e.get(v);
  O || (O = /* @__PURE__ */ new Set(), _e.set(v, O)), O.add(A);
}
function Gd(v, A) {
  const O = _e.get(v);
  O && (O.delete(A), O.size === 0 && _e.delete(v));
}
function ky(v, A) {
  const O = _e.get(v);
  if (O)
    for (const m of O)
      m(A);
}
function jd(v) {
  if (!Array.isArray(v) || v.length < 2) {
    console.warn("[TLReact] Unexpected SSE event format:", v);
    return;
  }
  const A = v[0], O = v[1];
  switch (A) {
    case "Heartbeat":
      break;
    case "StateEvent":
      Xd(O);
      break;
    case "PatchEvent":
      Qd(O);
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
    case "I18NCacheInvalidation":
      $y();
      break;
    default:
      console.warn("[TLReact] Unknown SSE event type:", A);
  }
}
function Xd(v) {
  const A = JSON.parse(v.state);
  ky(v.controlId, A);
}
function Qd(v) {
  const A = JSON.parse(v.patch);
  ky(v.controlId, A);
}
const Tu = {
  contentReplacement(v) {
    const A = document.getElementById(v.elementId);
    A && (A.innerHTML = v.html);
  },
  elementReplacement(v) {
    const A = document.getElementById(v.elementId);
    A && (A.outerHTML = v.html);
  },
  propertyUpdate(v) {
    const A = document.getElementById(v.elementId);
    if (A)
      for (const O of v.properties)
        A.setAttribute(O.name, O.value);
  },
  cssClassUpdate(v) {
    const A = document.getElementById(v.elementId);
    A && (A.className = v.cssClass);
  },
  fragmentInsertion(v) {
    const A = document.getElementById(v.elementId);
    A && A.insertAdjacentHTML(v.position, v.html);
  },
  rangeReplacement(v) {
    const A = document.getElementById(v.startId), O = document.getElementById(v.stopId);
    if (A && O && A.parentNode) {
      const m = A.parentNode, q = document.createRange();
      q.setStartBefore(A), q.setEndAfter(O), q.deleteContents();
      const V = document.createElement("template");
      V.innerHTML = v.html, m.insertBefore(V.content, q.startContainer.childNodes[q.startOffset] || null);
    }
  },
  jsSnipplet(v) {
    try {
      (0, eval)(v.code);
    } catch (A) {
      console.error("[TLReact] Error executing JS snippet:", A);
    }
  },
  functionCall(v) {
    try {
      const A = JSON.parse(v.arguments), O = v.functionRef ? window[v.functionRef] : window, m = O == null ? void 0 : O[v.functionName];
      typeof m == "function" ? m.apply(O, A) : console.warn("[TLReact] Function not found:", v.functionRef + "." + v.functionName);
    } catch (A) {
      console.error("[TLReact] Error executing function call:", A);
    }
  }
};
var hi = { exports: {} }, ze = {}, Si = { exports: {} }, gi = {};
/**
 * @license React
 * scheduler.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var Cy;
function Zd() {
  return Cy || (Cy = 1, (function(v) {
    function A(b, M) {
      var Y = b.length;
      b.push(M);
      l: for (; 0 < Y; ) {
        var el = Y - 1 >>> 1, il = b[el];
        if (0 < q(il, M))
          b[el] = M, b[Y] = il, Y = el;
        else break l;
      }
    }
    function O(b) {
      return b.length === 0 ? null : b[0];
    }
    function m(b) {
      if (b.length === 0) return null;
      var M = b[0], Y = b.pop();
      if (Y !== M) {
        b[0] = Y;
        l: for (var el = 0, il = b.length, o = il >>> 1; el < o; ) {
          var z = 2 * (el + 1) - 1, p = b[z], U = z + 1, j = b[U];
          if (0 > q(p, Y))
            U < il && 0 > q(j, p) ? (b[el] = j, b[U] = Y, el = U) : (b[el] = p, b[z] = Y, el = z);
          else if (U < il && 0 > q(j, Y))
            b[el] = j, b[U] = Y, el = U;
          else break l;
        }
      }
      return M;
    }
    function q(b, M) {
      var Y = b.sortIndex - M.sortIndex;
      return Y !== 0 ? Y : b.id - M.id;
    }
    if (v.unstable_now = void 0, typeof performance == "object" && typeof performance.now == "function") {
      var V = performance;
      v.unstable_now = function() {
        return V.now();
      };
    } else {
      var P = Date, Al = P.now();
      v.unstable_now = function() {
        return P.now() - Al;
      };
    }
    var R = [], _ = [], $ = 1, B = null, ll = 3, wl = !1, Yl = !1, Cl = !1, Dt = !1, Wl = typeof setTimeout == "function" ? setTimeout : null, Wt = typeof clearTimeout == "function" ? clearTimeout : null, Rl = typeof setImmediate < "u" ? setImmediate : null;
    function ft(b) {
      for (var M = O(_); M !== null; ) {
        if (M.callback === null) m(_);
        else if (M.startTime <= b)
          m(_), M.sortIndex = M.expirationTime, A(R, M);
        else break;
        M = O(_);
      }
    }
    function Et(b) {
      if (Cl = !1, ft(b), !Yl)
        if (O(R) !== null)
          Yl = !0, Gl || (Gl = !0, Xl());
        else {
          var M = O(_);
          M !== null && gt(Et, M.startTime - b);
        }
    }
    var Gl = !1, w = -1, jl = 5, zt = -1;
    function Lu() {
      return Dt ? !0 : !(v.unstable_now() - zt < jl);
    }
    function At() {
      if (Dt = !1, Gl) {
        var b = v.unstable_now();
        zt = b;
        var M = !0;
        try {
          l: {
            Yl = !1, Cl && (Cl = !1, Wt(w), w = -1), wl = !0;
            var Y = ll;
            try {
              t: {
                for (ft(b), B = O(R); B !== null && !(B.expirationTime > b && Lu()); ) {
                  var el = B.callback;
                  if (typeof el == "function") {
                    B.callback = null, ll = B.priorityLevel;
                    var il = el(
                      B.expirationTime <= b
                    );
                    if (b = v.unstable_now(), typeof il == "function") {
                      B.callback = il, ft(b), M = !0;
                      break t;
                    }
                    B === O(R) && m(R), ft(b);
                  } else m(R);
                  B = O(R);
                }
                if (B !== null) M = !0;
                else {
                  var o = O(_);
                  o !== null && gt(
                    Et,
                    o.startTime - b
                  ), M = !1;
                }
              }
              break l;
            } finally {
              B = null, ll = Y, wl = !1;
            }
            M = void 0;
          }
        } finally {
          M ? Xl() : Gl = !1;
        }
      }
    }
    var Xl;
    if (typeof Rl == "function")
      Xl = function() {
        Rl(At);
      };
    else if (typeof MessageChannel < "u") {
      var Eu = new MessageChannel(), Ut = Eu.port2;
      Eu.port1.onmessage = At, Xl = function() {
        Ut.postMessage(null);
      };
    } else
      Xl = function() {
        Wl(At, 0);
      };
    function gt(b, M) {
      w = Wl(function() {
        b(v.unstable_now());
      }, M);
    }
    v.unstable_IdlePriority = 5, v.unstable_ImmediatePriority = 1, v.unstable_LowPriority = 4, v.unstable_NormalPriority = 3, v.unstable_Profiling = null, v.unstable_UserBlockingPriority = 2, v.unstable_cancelCallback = function(b) {
      b.callback = null;
    }, v.unstable_forceFrameRate = function(b) {
      0 > b || 125 < b ? console.error(
        "forceFrameRate takes a positive int between 0 and 125, forcing frame rates higher than 125 fps is not supported"
      ) : jl = 0 < b ? Math.floor(1e3 / b) : 5;
    }, v.unstable_getCurrentPriorityLevel = function() {
      return ll;
    }, v.unstable_next = function(b) {
      switch (ll) {
        case 1:
        case 2:
        case 3:
          var M = 3;
          break;
        default:
          M = ll;
      }
      var Y = ll;
      ll = M;
      try {
        return b();
      } finally {
        ll = Y;
      }
    }, v.unstable_requestPaint = function() {
      Dt = !0;
    }, v.unstable_runWithPriority = function(b, M) {
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
      var Y = ll;
      ll = b;
      try {
        return M();
      } finally {
        ll = Y;
      }
    }, v.unstable_scheduleCallback = function(b, M, Y) {
      var el = v.unstable_now();
      switch (typeof Y == "object" && Y !== null ? (Y = Y.delay, Y = typeof Y == "number" && 0 < Y ? el + Y : el) : Y = el, b) {
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
      return il = Y + il, b = {
        id: $++,
        callback: M,
        priorityLevel: b,
        startTime: Y,
        expirationTime: il,
        sortIndex: -1
      }, Y > el ? (b.sortIndex = Y, A(_, b), O(R) === null && b === O(_) && (Cl ? (Wt(w), w = -1) : Cl = !0, gt(Et, Y - el))) : (b.sortIndex = il, A(R, b), Yl || wl || (Yl = !0, Gl || (Gl = !0, Xl()))), b;
    }, v.unstable_shouldYield = Lu, v.unstable_wrapCallback = function(b) {
      var M = ll;
      return function() {
        var Y = ll;
        ll = M;
        try {
          return b.apply(this, arguments);
        } finally {
          ll = Y;
        }
      };
    };
  })(gi)), gi;
}
var qy;
function Ld() {
  return qy || (qy = 1, Si.exports = Zd()), Si.exports;
}
var ri = { exports: {} }, Hl = {};
/**
 * @license React
 * react-dom.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var By;
function xd() {
  if (By) return Hl;
  By = 1;
  var v = _i();
  function A(R) {
    var _ = "https://react.dev/errors/" + R;
    if (1 < arguments.length) {
      _ += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var $ = 2; $ < arguments.length; $++)
        _ += "&args[]=" + encodeURIComponent(arguments[$]);
    }
    return "Minified React error #" + R + "; visit " + _ + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function O() {
  }
  var m = {
    d: {
      f: O,
      r: function() {
        throw Error(A(522));
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
  }, q = Symbol.for("react.portal");
  function V(R, _, $) {
    var B = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: q,
      key: B == null ? null : "" + B,
      children: R,
      containerInfo: _,
      implementation: $
    };
  }
  var P = v.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function Al(R, _) {
    if (R === "font") return "";
    if (typeof _ == "string")
      return _ === "use-credentials" ? _ : "";
  }
  return Hl.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = m, Hl.createPortal = function(R, _) {
    var $ = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!_ || _.nodeType !== 1 && _.nodeType !== 9 && _.nodeType !== 11)
      throw Error(A(299));
    return V(R, _, null, $);
  }, Hl.flushSync = function(R) {
    var _ = P.T, $ = m.p;
    try {
      if (P.T = null, m.p = 2, R) return R();
    } finally {
      P.T = _, m.p = $, m.d.f();
    }
  }, Hl.preconnect = function(R, _) {
    typeof R == "string" && (_ ? (_ = _.crossOrigin, _ = typeof _ == "string" ? _ === "use-credentials" ? _ : "" : void 0) : _ = null, m.d.C(R, _));
  }, Hl.prefetchDNS = function(R) {
    typeof R == "string" && m.d.D(R);
  }, Hl.preinit = function(R, _) {
    if (typeof R == "string" && _ && typeof _.as == "string") {
      var $ = _.as, B = Al($, _.crossOrigin), ll = typeof _.integrity == "string" ? _.integrity : void 0, wl = typeof _.fetchPriority == "string" ? _.fetchPriority : void 0;
      $ === "style" ? m.d.S(
        R,
        typeof _.precedence == "string" ? _.precedence : void 0,
        {
          crossOrigin: B,
          integrity: ll,
          fetchPriority: wl
        }
      ) : $ === "script" && m.d.X(R, {
        crossOrigin: B,
        integrity: ll,
        fetchPriority: wl,
        nonce: typeof _.nonce == "string" ? _.nonce : void 0
      });
    }
  }, Hl.preinitModule = function(R, _) {
    if (typeof R == "string")
      if (typeof _ == "object" && _ !== null) {
        if (_.as == null || _.as === "script") {
          var $ = Al(
            _.as,
            _.crossOrigin
          );
          m.d.M(R, {
            crossOrigin: $,
            integrity: typeof _.integrity == "string" ? _.integrity : void 0,
            nonce: typeof _.nonce == "string" ? _.nonce : void 0
          });
        }
      } else _ == null && m.d.M(R);
  }, Hl.preload = function(R, _) {
    if (typeof R == "string" && typeof _ == "object" && _ !== null && typeof _.as == "string") {
      var $ = _.as, B = Al($, _.crossOrigin);
      m.d.L(R, $, {
        crossOrigin: B,
        integrity: typeof _.integrity == "string" ? _.integrity : void 0,
        nonce: typeof _.nonce == "string" ? _.nonce : void 0,
        type: typeof _.type == "string" ? _.type : void 0,
        fetchPriority: typeof _.fetchPriority == "string" ? _.fetchPriority : void 0,
        referrerPolicy: typeof _.referrerPolicy == "string" ? _.referrerPolicy : void 0,
        imageSrcSet: typeof _.imageSrcSet == "string" ? _.imageSrcSet : void 0,
        imageSizes: typeof _.imageSizes == "string" ? _.imageSizes : void 0,
        media: typeof _.media == "string" ? _.media : void 0
      });
    }
  }, Hl.preloadModule = function(R, _) {
    if (typeof R == "string")
      if (_) {
        var $ = Al(_.as, _.crossOrigin);
        m.d.m(R, {
          as: typeof _.as == "string" && _.as !== "script" ? _.as : void 0,
          crossOrigin: $,
          integrity: typeof _.integrity == "string" ? _.integrity : void 0
        });
      } else m.d.m(R);
  }, Hl.requestFormReset = function(R) {
    m.d.r(R);
  }, Hl.unstable_batchedUpdates = function(R, _) {
    return R(_);
  }, Hl.useFormState = function(R, _, $) {
    return P.H.useFormState(R, _, $);
  }, Hl.useFormStatus = function() {
    return P.H.useHostTransitionStatus();
  }, Hl.version = "19.2.4", Hl;
}
var Yy;
function Iy() {
  if (Yy) return ri.exports;
  Yy = 1;
  function v() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(v);
      } catch (A) {
        console.error(A);
      }
  }
  return v(), ri.exports = xd(), ri.exports;
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
var Gy;
function Vd() {
  if (Gy) return ze;
  Gy = 1;
  var v = Ld(), A = _i(), O = Iy();
  function m(l) {
    var t = "https://react.dev/errors/" + l;
    if (1 < arguments.length) {
      t += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var u = 2; u < arguments.length; u++)
        t += "&args[]=" + encodeURIComponent(arguments[u]);
    }
    return "Minified React error #" + l + "; visit " + t + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function q(l) {
    return !(!l || l.nodeType !== 1 && l.nodeType !== 9 && l.nodeType !== 11);
  }
  function V(l) {
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
  function P(l) {
    if (l.tag === 13) {
      var t = l.memoizedState;
      if (t === null && (l = l.alternate, l !== null && (t = l.memoizedState)), t !== null) return t.dehydrated;
    }
    return null;
  }
  function Al(l) {
    if (l.tag === 31) {
      var t = l.memoizedState;
      if (t === null && (l = l.alternate, l !== null && (t = l.memoizedState)), t !== null) return t.dehydrated;
    }
    return null;
  }
  function R(l) {
    if (V(l) !== l)
      throw Error(m(188));
  }
  function _(l) {
    var t = l.alternate;
    if (!t) {
      if (t = V(l), t === null) throw Error(m(188));
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
          if (n === u) return R(e), l;
          if (n === a) return R(e), t;
          n = n.sibling;
        }
        throw Error(m(188));
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
          if (!f) throw Error(m(189));
        }
      }
      if (u.alternate !== a) throw Error(m(190));
    }
    if (u.tag !== 3) throw Error(m(188));
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
  var B = Object.assign, ll = Symbol.for("react.element"), wl = Symbol.for("react.transitional.element"), Yl = Symbol.for("react.portal"), Cl = Symbol.for("react.fragment"), Dt = Symbol.for("react.strict_mode"), Wl = Symbol.for("react.profiler"), Wt = Symbol.for("react.consumer"), Rl = Symbol.for("react.context"), ft = Symbol.for("react.forward_ref"), Et = Symbol.for("react.suspense"), Gl = Symbol.for("react.suspense_list"), w = Symbol.for("react.memo"), jl = Symbol.for("react.lazy"), zt = Symbol.for("react.activity"), Lu = Symbol.for("react.memo_cache_sentinel"), At = Symbol.iterator;
  function Xl(l) {
    return l === null || typeof l != "object" ? null : (l = At && l[At] || l["@@iterator"], typeof l == "function" ? l : null);
  }
  var Eu = Symbol.for("react.client.reference");
  function Ut(l) {
    if (l == null) return null;
    if (typeof l == "function")
      return l.$$typeof === Eu ? null : l.displayName || l.name || null;
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
        case Rl:
          return l.displayName || "Context";
        case Wt:
          return (l._context.displayName || "Context") + ".Consumer";
        case ft:
          var t = l.render;
          return l = l.displayName, l || (l = t.displayName || t.name || "", l = l !== "" ? "ForwardRef(" + l + ")" : "ForwardRef"), l;
        case w:
          return t = l.displayName || null, t !== null ? t : Ut(l.type) || "Memo";
        case jl:
          t = l._payload, l = l._init;
          try {
            return Ut(l(t));
          } catch {
          }
      }
    return null;
  }
  var gt = Array.isArray, b = A.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE, M = O.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE, Y = {
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
  var U = o(null), j = o(null), Z = o(null), tl = o(null);
  function ql(l, t) {
    switch (p(Z, t), p(j, l), p(U, null), t.nodeType) {
      case 9:
      case 11:
        l = (l = t.documentElement) && (l = l.namespaceURI) ? I0(l) : 0;
        break;
      default:
        if (l = t.tagName, t = t.namespaceURI)
          t = I0(t), l = P0(t, l);
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
  function hl() {
    z(U), z(j), z(Z);
  }
  function Da(l) {
    l.memoizedState !== null && p(tl, l);
    var t = U.current, u = P0(t, l.type);
    t !== u && (p(j, l), p(U, u));
  }
  function Me(l) {
    j.current === l && (z(U), z(j)), tl.current === l && (z(tl), ge._currentValue = Y);
  }
  var Wn, pi;
  function zu(l) {
    if (Wn === void 0)
      try {
        throw Error();
      } catch (u) {
        var t = u.stack.trim().match(/\n( *(at )?)/);
        Wn = t && t[1] || "", pi = -1 < u.stack.indexOf(`
    at`) ? " (<anonymous>)" : -1 < u.stack.indexOf("@") ? "@unknown:0:0" : "";
      }
    return `
` + Wn + l + pi;
  }
  var $n = !1;
  function Fn(l, t) {
    if (!l || $n) return "";
    $n = !0;
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
      $n = !1, Error.prepareStackTrace = u;
    }
    return (u = l ? l.displayName || l.name : "") ? zu(u) : "";
  }
  function ev(l, t) {
    switch (l.tag) {
      case 26:
      case 27:
      case 5:
        return zu(l.type);
      case 16:
        return zu("Lazy");
      case 13:
        return l.child !== t && t !== null ? zu("Suspense Fallback") : zu("Suspense");
      case 19:
        return zu("SuspenseList");
      case 0:
      case 15:
        return Fn(l.type, !1);
      case 11:
        return Fn(l.type.render, !1);
      case 1:
        return Fn(l.type, !0);
      case 31:
        return zu("Activity");
      default:
        return "";
    }
  }
  function Di(l) {
    try {
      var t = "", u = null;
      do
        t += ev(l, u), u = l, l = l.return;
      while (l);
      return t;
    } catch (a) {
      return `
Error generating stack: ` + a.message + `
` + a.stack;
    }
  }
  var kn = Object.prototype.hasOwnProperty, In = v.unstable_scheduleCallback, Pn = v.unstable_cancelCallback, nv = v.unstable_shouldYield, fv = v.unstable_requestPaint, $l = v.unstable_now, cv = v.unstable_getCurrentPriorityLevel, Ui = v.unstable_ImmediatePriority, Ni = v.unstable_UserBlockingPriority, pe = v.unstable_NormalPriority, iv = v.unstable_LowPriority, Ri = v.unstable_IdlePriority, sv = v.log, ov = v.unstable_setDisableYieldValue, Ua = null, Fl = null;
  function $t(l) {
    if (typeof sv == "function" && ov(l), Fl && typeof Fl.setStrictMode == "function")
      try {
        Fl.setStrictMode(Ua, l);
      } catch {
      }
  }
  var kl = Math.clz32 ? Math.clz32 : mv, yv = Math.log, vv = Math.LN2;
  function mv(l) {
    return l >>>= 0, l === 0 ? 32 : 31 - (yv(l) / vv | 0) | 0;
  }
  var De = 256, Ue = 262144, Ne = 4194304;
  function Au(l) {
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
    return c !== 0 ? (a = c & ~n, a !== 0 ? e = Au(a) : (f &= c, f !== 0 ? e = Au(f) : u || (u = c & ~l, u !== 0 && (e = Au(u))))) : (c = a & ~n, c !== 0 ? e = Au(c) : f !== 0 ? e = Au(f) : u || (u = a & ~l, u !== 0 && (e = Au(u)))), e === 0 ? 0 : t !== 0 && t !== e && (t & n) === 0 && (n = e & -e, u = t & -t, n >= u || n === 32 && (u & 4194048) !== 0) ? t : e;
  }
  function Na(l, t) {
    return (l.pendingLanes & ~(l.suspendedLanes & ~l.pingedLanes) & t) === 0;
  }
  function dv(l, t) {
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
  function Hi() {
    var l = Ne;
    return Ne <<= 1, (Ne & 62914560) === 0 && (Ne = 4194304), l;
  }
  function lf(l) {
    for (var t = [], u = 0; 31 > u; u++) t.push(l);
    return t;
  }
  function Ra(l, t) {
    l.pendingLanes |= t, t !== 268435456 && (l.suspendedLanes = 0, l.pingedLanes = 0, l.warmLanes = 0);
  }
  function hv(l, t, u, a, e, n) {
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
    a !== 0 && Ci(l, a, 0), n !== 0 && e === 0 && l.tag !== 0 && (l.suspendedLanes |= n & ~(f & ~t));
  }
  function Ci(l, t, u) {
    l.pendingLanes |= t, l.suspendedLanes &= ~t;
    var a = 31 - kl(t);
    l.entangledLanes |= t, l.entanglements[a] = l.entanglements[a] | 1073741824 | u & 261930;
  }
  function qi(l, t) {
    var u = l.entangledLanes |= t;
    for (l = l.entanglements; u; ) {
      var a = 31 - kl(u), e = 1 << a;
      e & t | l[a] & t && (l[a] |= t), u &= ~e;
    }
  }
  function Bi(l, t) {
    var u = t & -t;
    return u = (u & 42) !== 0 ? 1 : tf(u), (u & (l.suspendedLanes | t)) !== 0 ? 0 : u;
  }
  function tf(l) {
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
  function uf(l) {
    return l &= -l, 2 < l ? 8 < l ? (l & 134217727) !== 0 ? 32 : 268435456 : 8 : 2;
  }
  function Yi() {
    var l = M.p;
    return l !== 0 ? l : (l = window.event, l === void 0 ? 32 : zy(l.type));
  }
  function Gi(l, t) {
    var u = M.p;
    try {
      return M.p = l, t();
    } finally {
      M.p = u;
    }
  }
  var Ft = Math.random().toString(36).slice(2), Ml = "__reactFiber$" + Ft, Ql = "__reactProps$" + Ft, xu = "__reactContainer$" + Ft, af = "__reactEvents$" + Ft, Sv = "__reactListeners$" + Ft, gv = "__reactHandles$" + Ft, ji = "__reactResources$" + Ft, Ha = "__reactMarker$" + Ft;
  function ef(l) {
    delete l[Ml], delete l[Ql], delete l[af], delete l[Sv], delete l[gv];
  }
  function Vu(l) {
    var t = l[Ml];
    if (t) return t;
    for (var u = l.parentNode; u; ) {
      if (t = u[xu] || u[Ml]) {
        if (u = t.alternate, t.child !== null || u !== null && u.child !== null)
          for (l = fy(l); l !== null; ) {
            if (u = l[Ml]) return u;
            l = fy(l);
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
    throw Error(m(33));
  }
  function Ju(l) {
    var t = l[ji];
    return t || (t = l[ji] = { hoistableStyles: /* @__PURE__ */ new Map(), hoistableScripts: /* @__PURE__ */ new Map() }), t;
  }
  function _l(l) {
    l[Ha] = !0;
  }
  var Xi = /* @__PURE__ */ new Set(), Qi = {};
  function _u(l, t) {
    wu(l, t), wu(l + "Capture", t);
  }
  function wu(l, t) {
    for (Qi[l] = t, l = 0; l < t.length; l++)
      Xi.add(t[l]);
  }
  var rv = RegExp(
    "^[:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD][:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD\\-.0-9\\u00B7\\u0300-\\u036F\\u203F-\\u2040]*$"
  ), Zi = {}, Li = {};
  function bv(l) {
    return kn.call(Li, l) ? !0 : kn.call(Zi, l) ? !1 : rv.test(l) ? Li[l] = !0 : (Zi[l] = !0, !1);
  }
  function He(l, t, u) {
    if (bv(t))
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
  function Ce(l, t, u) {
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
  function Nt(l, t, u, a) {
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
  function xi(l) {
    var t = l.type;
    return (l = l.nodeName) && l.toLowerCase() === "input" && (t === "checkbox" || t === "radio");
  }
  function Tv(l, t, u) {
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
  function nf(l) {
    if (!l._valueTracker) {
      var t = xi(l) ? "checked" : "value";
      l._valueTracker = Tv(
        l,
        t,
        "" + l[t]
      );
    }
  }
  function Vi(l) {
    if (!l) return !1;
    var t = l._valueTracker;
    if (!t) return !0;
    var u = t.getValue(), a = "";
    return l && (a = xi(l) ? l.checked ? "true" : "false" : l.value), l = a, l !== u ? (t.setValue(l), !0) : !1;
  }
  function qe(l) {
    if (l = l || (typeof document < "u" ? document : void 0), typeof l > "u") return null;
    try {
      return l.activeElement || l.body;
    } catch {
      return l.body;
    }
  }
  var Ev = /[\n"\\]/g;
  function it(l) {
    return l.replace(
      Ev,
      function(t) {
        return "\\" + t.charCodeAt(0).toString(16) + " ";
      }
    );
  }
  function ff(l, t, u, a, e, n, f, c) {
    l.name = "", f != null && typeof f != "function" && typeof f != "symbol" && typeof f != "boolean" ? l.type = f : l.removeAttribute("type"), t != null ? f === "number" ? (t === 0 && l.value === "" || l.value != t) && (l.value = "" + ct(t)) : l.value !== "" + ct(t) && (l.value = "" + ct(t)) : f !== "submit" && f !== "reset" || l.removeAttribute("value"), t != null ? cf(l, f, ct(t)) : u != null ? cf(l, f, ct(u)) : a != null && l.removeAttribute("value"), e == null && n != null && (l.defaultChecked = !!n), e != null && (l.checked = e && typeof e != "function" && typeof e != "symbol"), c != null && typeof c != "function" && typeof c != "symbol" && typeof c != "boolean" ? l.name = "" + ct(c) : l.removeAttribute("name");
  }
  function Ki(l, t, u, a, e, n, f, c) {
    if (n != null && typeof n != "function" && typeof n != "symbol" && typeof n != "boolean" && (l.type = n), t != null || u != null) {
      if (!(n !== "submit" && n !== "reset" || t != null)) {
        nf(l);
        return;
      }
      u = u != null ? "" + ct(u) : "", t = t != null ? "" + ct(t) : u, c || t === l.value || (l.value = t), l.defaultValue = t;
    }
    a = a ?? e, a = typeof a != "function" && typeof a != "symbol" && !!a, l.checked = c ? l.checked : !!a, l.defaultChecked = !!a, f != null && typeof f != "function" && typeof f != "symbol" && typeof f != "boolean" && (l.name = f), nf(l);
  }
  function cf(l, t, u) {
    t === "number" && qe(l.ownerDocument) === l || l.defaultValue === "" + u || (l.defaultValue = "" + u);
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
  function Ji(l, t, u) {
    if (t != null && (t = "" + ct(t), t !== l.value && (l.value = t), u == null)) {
      l.defaultValue !== t && (l.defaultValue = t);
      return;
    }
    l.defaultValue = u != null ? "" + ct(u) : "";
  }
  function wi(l, t, u, a) {
    if (t == null) {
      if (a != null) {
        if (u != null) throw Error(m(92));
        if (gt(a)) {
          if (1 < a.length) throw Error(m(93));
          a = a[0];
        }
        u = a;
      }
      u == null && (u = ""), t = u;
    }
    u = ct(t), l.defaultValue = u, a = l.textContent, a === u && a !== "" && a !== null && (l.value = a), nf(l);
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
  var zv = new Set(
    "animationIterationCount aspectRatio borderImageOutset borderImageSlice borderImageWidth boxFlex boxFlexGroup boxOrdinalGroup columnCount columns flex flexGrow flexPositive flexShrink flexNegative flexOrder gridArea gridRow gridRowEnd gridRowSpan gridRowStart gridColumn gridColumnEnd gridColumnSpan gridColumnStart fontWeight lineClamp lineHeight opacity order orphans scale tabSize widows zIndex zoom fillOpacity floodOpacity stopOpacity strokeDasharray strokeDashoffset strokeMiterlimit strokeOpacity strokeWidth MozAnimationIterationCount MozBoxFlex MozBoxFlexGroup MozLineClamp msAnimationIterationCount msFlex msZoom msFlexGrow msFlexNegative msFlexOrder msFlexPositive msFlexShrink msGridColumn msGridColumnSpan msGridRow msGridRowSpan WebkitAnimationIterationCount WebkitBoxFlex WebKitBoxFlexGroup WebkitBoxOrdinalGroup WebkitColumnCount WebkitColumns WebkitFlex WebkitFlexGrow WebkitFlexPositive WebkitFlexShrink WebkitLineClamp".split(
      " "
    )
  );
  function Wi(l, t, u) {
    var a = t.indexOf("--") === 0;
    u == null || typeof u == "boolean" || u === "" ? a ? l.setProperty(t, "") : t === "float" ? l.cssFloat = "" : l[t] = "" : a ? l.setProperty(t, u) : typeof u != "number" || u === 0 || zv.has(t) ? t === "float" ? l.cssFloat = u : l[t] = ("" + u).trim() : l[t] = u + "px";
  }
  function $i(l, t, u) {
    if (t != null && typeof t != "object")
      throw Error(m(62));
    if (l = l.style, u != null) {
      for (var a in u)
        !u.hasOwnProperty(a) || t != null && t.hasOwnProperty(a) || (a.indexOf("--") === 0 ? l.setProperty(a, "") : a === "float" ? l.cssFloat = "" : l[a] = "");
      for (var e in t)
        a = t[e], t.hasOwnProperty(e) && u[e] !== a && Wi(l, e, a);
    } else
      for (var n in t)
        t.hasOwnProperty(n) && Wi(l, n, t[n]);
  }
  function sf(l) {
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
  var Av = /* @__PURE__ */ new Map([
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
  ]), _v = /^[\u0000-\u001F ]*j[\r\n\t]*a[\r\n\t]*v[\r\n\t]*a[\r\n\t]*s[\r\n\t]*c[\r\n\t]*r[\r\n\t]*i[\r\n\t]*p[\r\n\t]*t[\r\n\t]*:/i;
  function Be(l) {
    return _v.test("" + l) ? "javascript:throw new Error('React has blocked a javascript: URL as a security precaution.')" : l;
  }
  function Rt() {
  }
  var of = null;
  function yf(l) {
    return l = l.target || l.srcElement || window, l.correspondingUseElement && (l = l.correspondingUseElement), l.nodeType === 3 ? l.parentNode : l;
  }
  var Fu = null, ku = null;
  function Fi(l) {
    var t = Ku(l);
    if (t && (l = t.stateNode)) {
      var u = l[Ql] || null;
      l: switch (l = t.stateNode, t.type) {
        case "input":
          if (ff(
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
                var e = a[Ql] || null;
                if (!e) throw Error(m(90));
                ff(
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
              a = u[t], a.form === l.form && Vi(a);
          }
          break l;
        case "textarea":
          Ji(l, u.value, u.defaultValue);
          break l;
        case "select":
          t = u.value, t != null && Wu(l, !!u.multiple, t, !1);
      }
    }
  }
  var vf = !1;
  function ki(l, t, u) {
    if (vf) return l(t, u);
    vf = !0;
    try {
      var a = l(t);
      return a;
    } finally {
      if (vf = !1, (Fu !== null || ku !== null) && (An(), Fu && (t = Fu, l = ku, ku = Fu = null, Fi(t), l)))
        for (t = 0; t < l.length; t++) Fi(l[t]);
    }
  }
  function qa(l, t) {
    var u = l.stateNode;
    if (u === null) return null;
    var a = u[Ql] || null;
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
        m(231, t, typeof u)
      );
    return u;
  }
  var Ht = !(typeof window > "u" || typeof window.document > "u" || typeof window.document.createElement > "u"), mf = !1;
  if (Ht)
    try {
      var Ba = {};
      Object.defineProperty(Ba, "passive", {
        get: function() {
          mf = !0;
        }
      }), window.addEventListener("test", Ba, Ba), window.removeEventListener("test", Ba, Ba);
    } catch {
      mf = !1;
    }
  var kt = null, df = null, Ye = null;
  function Ii() {
    if (Ye) return Ye;
    var l, t = df, u = t.length, a, e = "value" in kt ? kt.value : kt.textContent, n = e.length;
    for (l = 0; l < u && t[l] === e[l]; l++) ;
    var f = u - l;
    for (a = 1; a <= f && t[u - a] === e[n - a]; a++) ;
    return Ye = e.slice(l, 1 < a ? 1 - a : void 0);
  }
  function Ge(l) {
    var t = l.keyCode;
    return "charCode" in l ? (l = l.charCode, l === 0 && t === 13 && (l = 13)) : l = t, l === 10 && (l = 13), 32 <= l || l === 13 ? l : 0;
  }
  function je() {
    return !0;
  }
  function Pi() {
    return !1;
  }
  function Zl(l) {
    function t(u, a, e, n, f) {
      this._reactName = u, this._targetInst = e, this.type = a, this.nativeEvent = n, this.target = f, this.currentTarget = null;
      for (var c in l)
        l.hasOwnProperty(c) && (u = l[c], this[c] = u ? u(n) : n[c]);
      return this.isDefaultPrevented = (n.defaultPrevented != null ? n.defaultPrevented : n.returnValue === !1) ? je : Pi, this.isPropagationStopped = Pi, this;
    }
    return B(t.prototype, {
      preventDefault: function() {
        this.defaultPrevented = !0;
        var u = this.nativeEvent;
        u && (u.preventDefault ? u.preventDefault() : typeof u.returnValue != "unknown" && (u.returnValue = !1), this.isDefaultPrevented = je);
      },
      stopPropagation: function() {
        var u = this.nativeEvent;
        u && (u.stopPropagation ? u.stopPropagation() : typeof u.cancelBubble != "unknown" && (u.cancelBubble = !0), this.isPropagationStopped = je);
      },
      persist: function() {
      },
      isPersistent: je
    }), t;
  }
  var Ou = {
    eventPhase: 0,
    bubbles: 0,
    cancelable: 0,
    timeStamp: function(l) {
      return l.timeStamp || Date.now();
    },
    defaultPrevented: 0,
    isTrusted: 0
  }, Xe = Zl(Ou), Ya = B({}, Ou, { view: 0, detail: 0 }), Ov = Zl(Ya), hf, Sf, Ga, Qe = B({}, Ya, {
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
    getModifierState: rf,
    button: 0,
    buttons: 0,
    relatedTarget: function(l) {
      return l.relatedTarget === void 0 ? l.fromElement === l.srcElement ? l.toElement : l.fromElement : l.relatedTarget;
    },
    movementX: function(l) {
      return "movementX" in l ? l.movementX : (l !== Ga && (Ga && l.type === "mousemove" ? (hf = l.screenX - Ga.screenX, Sf = l.screenY - Ga.screenY) : Sf = hf = 0, Ga = l), hf);
    },
    movementY: function(l) {
      return "movementY" in l ? l.movementY : Sf;
    }
  }), ls = Zl(Qe), Mv = B({}, Qe, { dataTransfer: 0 }), pv = Zl(Mv), Dv = B({}, Ya, { relatedTarget: 0 }), gf = Zl(Dv), Uv = B({}, Ou, {
    animationName: 0,
    elapsedTime: 0,
    pseudoElement: 0
  }), Nv = Zl(Uv), Rv = B({}, Ou, {
    clipboardData: function(l) {
      return "clipboardData" in l ? l.clipboardData : window.clipboardData;
    }
  }), Hv = Zl(Rv), Cv = B({}, Ou, { data: 0 }), ts = Zl(Cv), qv = {
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
  }, Bv = {
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
  }, Yv = {
    Alt: "altKey",
    Control: "ctrlKey",
    Meta: "metaKey",
    Shift: "shiftKey"
  };
  function Gv(l) {
    var t = this.nativeEvent;
    return t.getModifierState ? t.getModifierState(l) : (l = Yv[l]) ? !!t[l] : !1;
  }
  function rf() {
    return Gv;
  }
  var jv = B({}, Ya, {
    key: function(l) {
      if (l.key) {
        var t = qv[l.key] || l.key;
        if (t !== "Unidentified") return t;
      }
      return l.type === "keypress" ? (l = Ge(l), l === 13 ? "Enter" : String.fromCharCode(l)) : l.type === "keydown" || l.type === "keyup" ? Bv[l.keyCode] || "Unidentified" : "";
    },
    code: 0,
    location: 0,
    ctrlKey: 0,
    shiftKey: 0,
    altKey: 0,
    metaKey: 0,
    repeat: 0,
    locale: 0,
    getModifierState: rf,
    charCode: function(l) {
      return l.type === "keypress" ? Ge(l) : 0;
    },
    keyCode: function(l) {
      return l.type === "keydown" || l.type === "keyup" ? l.keyCode : 0;
    },
    which: function(l) {
      return l.type === "keypress" ? Ge(l) : l.type === "keydown" || l.type === "keyup" ? l.keyCode : 0;
    }
  }), Xv = Zl(jv), Qv = B({}, Qe, {
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
  }), us = Zl(Qv), Zv = B({}, Ya, {
    touches: 0,
    targetTouches: 0,
    changedTouches: 0,
    altKey: 0,
    metaKey: 0,
    ctrlKey: 0,
    shiftKey: 0,
    getModifierState: rf
  }), Lv = Zl(Zv), xv = B({}, Ou, {
    propertyName: 0,
    elapsedTime: 0,
    pseudoElement: 0
  }), Vv = Zl(xv), Kv = B({}, Qe, {
    deltaX: function(l) {
      return "deltaX" in l ? l.deltaX : "wheelDeltaX" in l ? -l.wheelDeltaX : 0;
    },
    deltaY: function(l) {
      return "deltaY" in l ? l.deltaY : "wheelDeltaY" in l ? -l.wheelDeltaY : "wheelDelta" in l ? -l.wheelDelta : 0;
    },
    deltaZ: 0,
    deltaMode: 0
  }), Jv = Zl(Kv), wv = B({}, Ou, {
    newState: 0,
    oldState: 0
  }), Wv = Zl(wv), $v = [9, 13, 27, 32], bf = Ht && "CompositionEvent" in window, ja = null;
  Ht && "documentMode" in document && (ja = document.documentMode);
  var Fv = Ht && "TextEvent" in window && !ja, as = Ht && (!bf || ja && 8 < ja && 11 >= ja), es = " ", ns = !1;
  function fs(l, t) {
    switch (l) {
      case "keyup":
        return $v.indexOf(t.keyCode) !== -1;
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
  function cs(l) {
    return l = l.detail, typeof l == "object" && "data" in l ? l.data : null;
  }
  var Iu = !1;
  function kv(l, t) {
    switch (l) {
      case "compositionend":
        return cs(t);
      case "keypress":
        return t.which !== 32 ? null : (ns = !0, es);
      case "textInput":
        return l = t.data, l === es && ns ? null : l;
      default:
        return null;
    }
  }
  function Iv(l, t) {
    if (Iu)
      return l === "compositionend" || !bf && fs(l, t) ? (l = Ii(), Ye = df = kt = null, Iu = !1, l) : null;
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
        return as && t.locale !== "ko" ? null : t.data;
      default:
        return null;
    }
  }
  var Pv = {
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
  function is(l) {
    var t = l && l.nodeName && l.nodeName.toLowerCase();
    return t === "input" ? !!Pv[l.type] : t === "textarea";
  }
  function ss(l, t, u, a) {
    Fu ? ku ? ku.push(a) : ku = [a] : Fu = a, t = Nn(t, "onChange"), 0 < t.length && (u = new Xe(
      "onChange",
      "change",
      null,
      u,
      a
    ), l.push({ event: u, listeners: t }));
  }
  var Xa = null, Qa = null;
  function lm(l) {
    J0(l, 0);
  }
  function Ze(l) {
    var t = Ca(l);
    if (Vi(t)) return l;
  }
  function os(l, t) {
    if (l === "change") return t;
  }
  var ys = !1;
  if (Ht) {
    var Tf;
    if (Ht) {
      var Ef = "oninput" in document;
      if (!Ef) {
        var vs = document.createElement("div");
        vs.setAttribute("oninput", "return;"), Ef = typeof vs.oninput == "function";
      }
      Tf = Ef;
    } else Tf = !1;
    ys = Tf && (!document.documentMode || 9 < document.documentMode);
  }
  function ms() {
    Xa && (Xa.detachEvent("onpropertychange", ds), Qa = Xa = null);
  }
  function ds(l) {
    if (l.propertyName === "value" && Ze(Qa)) {
      var t = [];
      ss(
        t,
        Qa,
        l,
        yf(l)
      ), ki(lm, t);
    }
  }
  function tm(l, t, u) {
    l === "focusin" ? (ms(), Xa = t, Qa = u, Xa.attachEvent("onpropertychange", ds)) : l === "focusout" && ms();
  }
  function um(l) {
    if (l === "selectionchange" || l === "keyup" || l === "keydown")
      return Ze(Qa);
  }
  function am(l, t) {
    if (l === "click") return Ze(t);
  }
  function em(l, t) {
    if (l === "input" || l === "change")
      return Ze(t);
  }
  function nm(l, t) {
    return l === t && (l !== 0 || 1 / l === 1 / t) || l !== l && t !== t;
  }
  var Il = typeof Object.is == "function" ? Object.is : nm;
  function Za(l, t) {
    if (Il(l, t)) return !0;
    if (typeof l != "object" || l === null || typeof t != "object" || t === null)
      return !1;
    var u = Object.keys(l), a = Object.keys(t);
    if (u.length !== a.length) return !1;
    for (a = 0; a < u.length; a++) {
      var e = u[a];
      if (!kn.call(t, e) || !Il(l[e], t[e]))
        return !1;
    }
    return !0;
  }
  function hs(l) {
    for (; l && l.firstChild; ) l = l.firstChild;
    return l;
  }
  function Ss(l, t) {
    var u = hs(l);
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
      u = hs(u);
    }
  }
  function gs(l, t) {
    return l && t ? l === t ? !0 : l && l.nodeType === 3 ? !1 : t && t.nodeType === 3 ? gs(l, t.parentNode) : "contains" in l ? l.contains(t) : l.compareDocumentPosition ? !!(l.compareDocumentPosition(t) & 16) : !1 : !1;
  }
  function rs(l) {
    l = l != null && l.ownerDocument != null && l.ownerDocument.defaultView != null ? l.ownerDocument.defaultView : window;
    for (var t = qe(l.document); t instanceof l.HTMLIFrameElement; ) {
      try {
        var u = typeof t.contentWindow.location.href == "string";
      } catch {
        u = !1;
      }
      if (u) l = t.contentWindow;
      else break;
      t = qe(l.document);
    }
    return t;
  }
  function zf(l) {
    var t = l && l.nodeName && l.nodeName.toLowerCase();
    return t && (t === "input" && (l.type === "text" || l.type === "search" || l.type === "tel" || l.type === "url" || l.type === "password") || t === "textarea" || l.contentEditable === "true");
  }
  var fm = Ht && "documentMode" in document && 11 >= document.documentMode, Pu = null, Af = null, La = null, _f = !1;
  function bs(l, t, u) {
    var a = u.window === u ? u.document : u.nodeType === 9 ? u : u.ownerDocument;
    _f || Pu == null || Pu !== qe(a) || (a = Pu, "selectionStart" in a && zf(a) ? a = { start: a.selectionStart, end: a.selectionEnd } : (a = (a.ownerDocument && a.ownerDocument.defaultView || window).getSelection(), a = {
      anchorNode: a.anchorNode,
      anchorOffset: a.anchorOffset,
      focusNode: a.focusNode,
      focusOffset: a.focusOffset
    }), La && Za(La, a) || (La = a, a = Nn(Af, "onSelect"), 0 < a.length && (t = new Xe(
      "onSelect",
      "select",
      null,
      t,
      u
    ), l.push({ event: t, listeners: a }), t.target = Pu)));
  }
  function Mu(l, t) {
    var u = {};
    return u[l.toLowerCase()] = t.toLowerCase(), u["Webkit" + l] = "webkit" + t, u["Moz" + l] = "moz" + t, u;
  }
  var la = {
    animationend: Mu("Animation", "AnimationEnd"),
    animationiteration: Mu("Animation", "AnimationIteration"),
    animationstart: Mu("Animation", "AnimationStart"),
    transitionrun: Mu("Transition", "TransitionRun"),
    transitionstart: Mu("Transition", "TransitionStart"),
    transitioncancel: Mu("Transition", "TransitionCancel"),
    transitionend: Mu("Transition", "TransitionEnd")
  }, Of = {}, Ts = {};
  Ht && (Ts = document.createElement("div").style, "AnimationEvent" in window || (delete la.animationend.animation, delete la.animationiteration.animation, delete la.animationstart.animation), "TransitionEvent" in window || delete la.transitionend.transition);
  function pu(l) {
    if (Of[l]) return Of[l];
    if (!la[l]) return l;
    var t = la[l], u;
    for (u in t)
      if (t.hasOwnProperty(u) && u in Ts)
        return Of[l] = t[u];
    return l;
  }
  var Es = pu("animationend"), zs = pu("animationiteration"), As = pu("animationstart"), cm = pu("transitionrun"), im = pu("transitionstart"), sm = pu("transitioncancel"), _s = pu("transitionend"), Os = /* @__PURE__ */ new Map(), Mf = "abort auxClick beforeToggle cancel canPlay canPlayThrough click close contextMenu copy cut drag dragEnd dragEnter dragExit dragLeave dragOver dragStart drop durationChange emptied encrypted ended error gotPointerCapture input invalid keyDown keyPress keyUp load loadedData loadedMetadata loadStart lostPointerCapture mouseDown mouseMove mouseOut mouseOver mouseUp paste pause play playing pointerCancel pointerDown pointerMove pointerOut pointerOver pointerUp progress rateChange reset resize seeked seeking stalled submit suspend timeUpdate touchCancel touchEnd touchStart volumeChange scroll toggle touchMove waiting wheel".split(
    " "
  );
  Mf.push("scrollEnd");
  function rt(l, t) {
    Os.set(l, t), _u(t, [l]);
  }
  var Le = typeof reportError == "function" ? reportError : function(l) {
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
  }, st = [], ta = 0, pf = 0;
  function xe() {
    for (var l = ta, t = pf = ta = 0; t < l; ) {
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
      n !== 0 && Ms(u, e, n);
    }
  }
  function Ve(l, t, u, a) {
    st[ta++] = l, st[ta++] = t, st[ta++] = u, st[ta++] = a, pf |= a, l.lanes |= a, l = l.alternate, l !== null && (l.lanes |= a);
  }
  function Df(l, t, u, a) {
    return Ve(l, t, u, a), Ke(l);
  }
  function Du(l, t) {
    return Ve(l, null, null, t), Ke(l);
  }
  function Ms(l, t, u) {
    l.lanes |= u;
    var a = l.alternate;
    a !== null && (a.lanes |= u);
    for (var e = !1, n = l.return; n !== null; )
      n.childLanes |= u, a = n.alternate, a !== null && (a.childLanes |= u), n.tag === 22 && (l = n.stateNode, l === null || l._visibility & 1 || (e = !0)), l = n, n = n.return;
    return l.tag === 3 ? (n = l.stateNode, e && t !== null && (e = 31 - kl(u), l = n.hiddenUpdates, a = l[e], a === null ? l[e] = [t] : a.push(t), t.lane = u | 536870912), n) : null;
  }
  function Ke(l) {
    if (50 < oe)
      throw oe = 0, Gc = null, Error(m(185));
    for (var t = l.return; t !== null; )
      l = t, t = l.return;
    return l.tag === 3 ? l.stateNode : null;
  }
  var ua = {};
  function om(l, t, u, a) {
    this.tag = l, this.key = u, this.sibling = this.child = this.return = this.stateNode = this.type = this.elementType = null, this.index = 0, this.refCleanup = this.ref = null, this.pendingProps = t, this.dependencies = this.memoizedState = this.updateQueue = this.memoizedProps = null, this.mode = a, this.subtreeFlags = this.flags = 0, this.deletions = null, this.childLanes = this.lanes = 0, this.alternate = null;
  }
  function Pl(l, t, u, a) {
    return new om(l, t, u, a);
  }
  function Uf(l) {
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
  function ps(l, t) {
    l.flags &= 65011714;
    var u = l.alternate;
    return u === null ? (l.childLanes = 0, l.lanes = t, l.child = null, l.subtreeFlags = 0, l.memoizedProps = null, l.memoizedState = null, l.updateQueue = null, l.dependencies = null, l.stateNode = null) : (l.childLanes = u.childLanes, l.lanes = u.lanes, l.child = u.child, l.subtreeFlags = 0, l.deletions = null, l.memoizedProps = u.memoizedProps, l.memoizedState = u.memoizedState, l.updateQueue = u.updateQueue, l.type = u.type, t = u.dependencies, l.dependencies = t === null ? null : {
      lanes: t.lanes,
      firstContext: t.firstContext
    }), l;
  }
  function Je(l, t, u, a, e, n) {
    var f = 0;
    if (a = l, typeof l == "function") Uf(l) && (f = 1);
    else if (typeof l == "string")
      f = hd(
        l,
        u,
        U.current
      ) ? 26 : l === "html" || l === "head" || l === "body" ? 27 : 5;
    else
      l: switch (l) {
        case zt:
          return l = Pl(31, u, t, e), l.elementType = zt, l.lanes = n, l;
        case Cl:
          return Uu(u.children, e, n, t);
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
              case Rl:
                f = 10;
                break l;
              case Wt:
                f = 9;
                break l;
              case ft:
                f = 11;
                break l;
              case w:
                f = 14;
                break l;
              case jl:
                f = 16, a = null;
                break l;
            }
          f = 29, u = Error(
            m(130, l === null ? "null" : typeof l, "")
          ), a = null;
      }
    return t = Pl(f, u, t, e), t.elementType = l, t.type = a, t.lanes = n, t;
  }
  function Uu(l, t, u, a) {
    return l = Pl(7, l, a, t), l.lanes = u, l;
  }
  function Nf(l, t, u) {
    return l = Pl(6, l, null, t), l.lanes = u, l;
  }
  function Ds(l) {
    var t = Pl(18, null, null, 0);
    return t.stateNode = l, t;
  }
  function Rf(l, t, u) {
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
  var Us = /* @__PURE__ */ new WeakMap();
  function ot(l, t) {
    if (typeof l == "object" && l !== null) {
      var u = Us.get(l);
      return u !== void 0 ? u : (t = {
        value: l,
        source: t,
        stack: Di(t)
      }, Us.set(l, t), t);
    }
    return {
      value: l,
      source: t,
      stack: Di(t)
    };
  }
  var aa = [], ea = 0, we = null, xa = 0, yt = [], vt = 0, It = null, _t = 1, Ot = "";
  function qt(l, t) {
    aa[ea++] = xa, aa[ea++] = we, we = l, xa = t;
  }
  function Ns(l, t, u) {
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
  function Hf(l) {
    l.return !== null && (qt(l, 1), Ns(l, 1, 0));
  }
  function Cf(l) {
    for (; l === we; )
      we = aa[--ea], aa[ea] = null, xa = aa[--ea], aa[ea] = null;
    for (; l === It; )
      It = yt[--vt], yt[vt] = null, Ot = yt[--vt], yt[vt] = null, _t = yt[--vt], yt[vt] = null;
  }
  function Rs(l, t) {
    yt[vt++] = _t, yt[vt++] = Ot, yt[vt++] = It, _t = t.id, Ot = t.overflow, It = l;
  }
  var pl = null, ol = null, W = !1, Pt = null, mt = !1, qf = Error(m(519));
  function lu(l) {
    var t = Error(
      m(
        418,
        1 < arguments.length && arguments[1] !== void 0 && arguments[1] ? "text" : "HTML",
        ""
      )
    );
    throw Va(ot(t, l)), qf;
  }
  function Hs(l) {
    var t = l.stateNode, u = l.type, a = l.memoizedProps;
    switch (t[Ml] = l, t[Ql] = a, u) {
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
        x("invalid", t), Ki(
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
        x("invalid", t), wi(t, a.value, a.defaultValue, a.children);
    }
    u = a.children, typeof u != "string" && typeof u != "number" && typeof u != "bigint" || t.textContent === "" + u || a.suppressHydrationWarning === !0 || F0(t.textContent, u) ? (a.popover != null && (x("beforetoggle", t), x("toggle", t)), a.onScroll != null && x("scroll", t), a.onScrollEnd != null && x("scrollend", t), a.onClick != null && (t.onclick = Rt), t = !0) : t = !1, t || lu(l, !0);
  }
  function Cs(l) {
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
    if (!W) return Cs(l), W = !0, !1;
    var t = l.tag, u;
    if ((u = t !== 3 && t !== 27) && ((u = t === 5) && (u = l.type, u = !(u !== "form" && u !== "button") || Ic(l.type, l.memoizedProps)), u = !u), u && ol && lu(l), Cs(l), t === 13) {
      if (l = l.memoizedState, l = l !== null ? l.dehydrated : null, !l) throw Error(m(317));
      ol = ny(l);
    } else if (t === 31) {
      if (l = l.memoizedState, l = l !== null ? l.dehydrated : null, !l) throw Error(m(317));
      ol = ny(l);
    } else
      t === 27 ? (t = ol, du(l.type) ? (l = ai, ai = null, ol = l) : ol = t) : ol = pl ? ht(l.stateNode.nextSibling) : null;
    return !0;
  }
  function Nu() {
    ol = pl = null, W = !1;
  }
  function Bf() {
    var l = Pt;
    return l !== null && (Kl === null ? Kl = l : Kl.push.apply(
      Kl,
      l
    ), Pt = null), l;
  }
  function Va(l) {
    Pt === null ? Pt = [l] : Pt.push(l);
  }
  var Yf = o(null), Ru = null, Bt = null;
  function tu(l, t, u) {
    p(Yf, t._currentValue), t._currentValue = u;
  }
  function Yt(l) {
    l._currentValue = Yf.current, z(Yf);
  }
  function Gf(l, t, u) {
    for (; l !== null; ) {
      var a = l.alternate;
      if ((l.childLanes & t) !== t ? (l.childLanes |= t, a !== null && (a.childLanes |= t)) : a !== null && (a.childLanes & t) !== t && (a.childLanes |= t), l === u) break;
      l = l.return;
    }
  }
  function jf(l, t, u, a) {
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
              n.lanes |= u, c = n.alternate, c !== null && (c.lanes |= u), Gf(
                n.return,
                u,
                l
              ), a || (f = null);
              break l;
            }
          n = c.next;
        }
      } else if (e.tag === 18) {
        if (f = e.return, f === null) throw Error(m(341));
        f.lanes |= u, n = f.alternate, n !== null && (n.lanes |= u), Gf(f, u, l), f = null;
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
        if (f === null) throw Error(m(387));
        if (f = f.memoizedProps, f !== null) {
          var c = e.type;
          Il(e.pendingProps.value, f.value) || (l !== null ? l.push(c) : l = [c]);
        }
      } else if (e === tl.current) {
        if (f = e.alternate, f === null) throw Error(m(387));
        f.memoizedState.memoizedState !== e.memoizedState.memoizedState && (l !== null ? l.push(ge) : l = [ge]);
      }
      e = e.return;
    }
    l !== null && jf(
      t,
      l,
      u,
      a
    ), t.flags |= 262144;
  }
  function We(l) {
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
  function Hu(l) {
    Ru = l, Bt = null, l = l.dependencies, l !== null && (l.firstContext = null);
  }
  function Dl(l) {
    return qs(Ru, l);
  }
  function $e(l, t) {
    return Ru === null && Hu(l), qs(l, t);
  }
  function qs(l, t) {
    var u = t._currentValue;
    if (t = { context: t, memoizedValue: u, next: null }, Bt === null) {
      if (l === null) throw Error(m(308));
      Bt = t, l.dependencies = { lanes: 0, firstContext: t }, l.flags |= 524288;
    } else Bt = Bt.next = t;
    return u;
  }
  var ym = typeof AbortController < "u" ? AbortController : function() {
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
  }, vm = v.unstable_scheduleCallback, mm = v.unstable_NormalPriority, rl = {
    $$typeof: Rl,
    Consumer: null,
    Provider: null,
    _currentValue: null,
    _currentValue2: null,
    _threadCount: 0
  };
  function Xf() {
    return {
      controller: new ym(),
      data: /* @__PURE__ */ new Map(),
      refCount: 0
    };
  }
  function Ka(l) {
    l.refCount--, l.refCount === 0 && vm(mm, function() {
      l.controller.abort();
    });
  }
  var Ja = null, Qf = 0, ca = 0, ia = null;
  function dm(l, t) {
    if (Ja === null) {
      var u = Ja = [];
      Qf = 0, ca = xc(), ia = {
        status: "pending",
        value: void 0,
        then: function(a) {
          u.push(a);
        }
      };
    }
    return Qf++, t.then(Bs, Bs), t;
  }
  function Bs() {
    if (--Qf === 0 && Ja !== null) {
      ia !== null && (ia.status = "fulfilled");
      var l = Ja;
      Ja = null, ca = 0, ia = null;
      for (var t = 0; t < l.length; t++) (0, l[t])();
    }
  }
  function hm(l, t) {
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
  var Ys = b.S;
  b.S = function(l, t) {
    T0 = $l(), typeof t == "object" && t !== null && typeof t.then == "function" && dm(l, t), Ys !== null && Ys(l, t);
  };
  var Cu = o(null);
  function Zf() {
    var l = Cu.current;
    return l !== null ? l : sl.pooledCache;
  }
  function Fe(l, t) {
    t === null ? p(Cu, Cu.current) : p(Cu, t.pool);
  }
  function Gs() {
    var l = Zf();
    return l === null ? null : { parent: rl._currentValue, pool: l };
  }
  var sa = Error(m(460)), Lf = Error(m(474)), ke = Error(m(542)), Ie = { then: function() {
  } };
  function js(l) {
    return l = l.status, l === "fulfilled" || l === "rejected";
  }
  function Xs(l, t, u) {
    switch (u = l[u], u === void 0 ? l.push(t) : u !== t && (t.then(Rt, Rt), t = u), t.status) {
      case "fulfilled":
        return t.value;
      case "rejected":
        throw l = t.reason, Zs(l), l;
      default:
        if (typeof t.status == "string") t.then(Rt, Rt);
        else {
          if (l = sl, l !== null && 100 < l.shellSuspendCounter)
            throw Error(m(482));
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
            throw l = t.reason, Zs(l), l;
        }
        throw Bu = t, sa;
    }
  }
  function qu(l) {
    try {
      var t = l._init;
      return t(l._payload);
    } catch (u) {
      throw u !== null && typeof u == "object" && typeof u.then == "function" ? (Bu = u, sa) : u;
    }
  }
  var Bu = null;
  function Qs() {
    if (Bu === null) throw Error(m(459));
    var l = Bu;
    return Bu = null, l;
  }
  function Zs(l) {
    if (l === sa || l === ke)
      throw Error(m(483));
  }
  var oa = null, wa = 0;
  function Pe(l) {
    var t = wa;
    return wa += 1, oa === null && (oa = []), Xs(oa, l, t);
  }
  function Wa(l, t) {
    t = t.props.ref, l.ref = t !== void 0 ? t : null;
  }
  function ln(l, t) {
    throw t.$$typeof === ll ? Error(m(525)) : (l = Object.prototype.toString.call(t), Error(
      m(
        31,
        l === "[object Object]" ? "object with keys {" + Object.keys(t).join(", ") + "}" : l
      )
    ));
  }
  function Ls(l) {
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
      return s === null || s.tag !== 6 ? (s = Nf(d, y.mode, T), s.return = y, s) : (s = e(s, d), s.return = y, s);
    }
    function i(y, s, d, T) {
      var H = d.type;
      return H === Cl ? r(
        y,
        s,
        d.props.children,
        T,
        d.key
      ) : s !== null && (s.elementType === H || typeof H == "object" && H !== null && H.$$typeof === jl && qu(H) === s.type) ? (s = e(s, d.props), Wa(s, d), s.return = y, s) : (s = Je(
        d.type,
        d.key,
        d.props,
        null,
        y.mode,
        T
      ), Wa(s, d), s.return = y, s);
    }
    function h(y, s, d, T) {
      return s === null || s.tag !== 4 || s.stateNode.containerInfo !== d.containerInfo || s.stateNode.implementation !== d.implementation ? (s = Rf(d, y.mode, T), s.return = y, s) : (s = e(s, d.children || []), s.return = y, s);
    }
    function r(y, s, d, T, H) {
      return s === null || s.tag !== 7 ? (s = Uu(
        d,
        y.mode,
        T,
        H
      ), s.return = y, s) : (s = e(s, d), s.return = y, s);
    }
    function E(y, s, d) {
      if (typeof s == "string" && s !== "" || typeof s == "number" || typeof s == "bigint")
        return s = Nf(
          "" + s,
          y.mode,
          d
        ), s.return = y, s;
      if (typeof s == "object" && s !== null) {
        switch (s.$$typeof) {
          case wl:
            return d = Je(
              s.type,
              s.key,
              s.props,
              null,
              y.mode,
              d
            ), Wa(d, s), d.return = y, d;
          case Yl:
            return s = Rf(
              s,
              y.mode,
              d
            ), s.return = y, s;
          case jl:
            return s = qu(s), E(y, s, d);
        }
        if (gt(s) || Xl(s))
          return s = Uu(
            s,
            y.mode,
            d,
            null
          ), s.return = y, s;
        if (typeof s.then == "function")
          return E(y, Pe(s), d);
        if (s.$$typeof === Rl)
          return E(
            y,
            $e(y, s),
            d
          );
        ln(y, s);
      }
      return null;
    }
    function S(y, s, d, T) {
      var H = s !== null ? s.key : null;
      if (typeof d == "string" && d !== "" || typeof d == "number" || typeof d == "bigint")
        return H !== null ? null : c(y, s, "" + d, T);
      if (typeof d == "object" && d !== null) {
        switch (d.$$typeof) {
          case wl:
            return d.key === H ? i(y, s, d, T) : null;
          case Yl:
            return d.key === H ? h(y, s, d, T) : null;
          case jl:
            return d = qu(d), S(y, s, d, T);
        }
        if (gt(d) || Xl(d))
          return H !== null ? null : r(y, s, d, T, null);
        if (typeof d.then == "function")
          return S(
            y,
            s,
            Pe(d),
            T
          );
        if (d.$$typeof === Rl)
          return S(
            y,
            s,
            $e(y, d),
            T
          );
        ln(y, d);
      }
      return null;
    }
    function g(y, s, d, T, H) {
      if (typeof T == "string" && T !== "" || typeof T == "number" || typeof T == "bigint")
        return y = y.get(d) || null, c(s, y, "" + T, H);
      if (typeof T == "object" && T !== null) {
        switch (T.$$typeof) {
          case wl:
            return y = y.get(
              T.key === null ? d : T.key
            ) || null, i(s, y, T, H);
          case Yl:
            return y = y.get(
              T.key === null ? d : T.key
            ) || null, h(s, y, T, H);
          case jl:
            return T = qu(T), g(
              y,
              s,
              d,
              T,
              H
            );
        }
        if (gt(T) || Xl(T))
          return y = y.get(d) || null, r(s, y, T, H, null);
        if (typeof T.then == "function")
          return g(
            y,
            s,
            d,
            Pe(T),
            H
          );
        if (T.$$typeof === Rl)
          return g(
            y,
            s,
            d,
            $e(s, T),
            H
          );
        ln(s, T);
      }
      return null;
    }
    function D(y, s, d, T) {
      for (var H = null, F = null, N = s, Q = s = 0, J = null; N !== null && Q < d.length; Q++) {
        N.index > Q ? (J = N, N = null) : J = N.sibling;
        var k = S(
          y,
          N,
          d[Q],
          T
        );
        if (k === null) {
          N === null && (N = J);
          break;
        }
        l && N && k.alternate === null && t(y, N), s = n(k, s, Q), F === null ? H = k : F.sibling = k, F = k, N = J;
      }
      if (Q === d.length)
        return u(y, N), W && qt(y, Q), H;
      if (N === null) {
        for (; Q < d.length; Q++)
          N = E(y, d[Q], T), N !== null && (s = n(
            N,
            s,
            Q
          ), F === null ? H = N : F.sibling = N, F = N);
        return W && qt(y, Q), H;
      }
      for (N = a(N); Q < d.length; Q++)
        J = g(
          N,
          y,
          Q,
          d[Q],
          T
        ), J !== null && (l && J.alternate !== null && N.delete(
          J.key === null ? Q : J.key
        ), s = n(
          J,
          s,
          Q
        ), F === null ? H = J : F.sibling = J, F = J);
      return l && N.forEach(function(bu) {
        return t(y, bu);
      }), W && qt(y, Q), H;
    }
    function C(y, s, d, T) {
      if (d == null) throw Error(m(151));
      for (var H = null, F = null, N = s, Q = s = 0, J = null, k = d.next(); N !== null && !k.done; Q++, k = d.next()) {
        N.index > Q ? (J = N, N = null) : J = N.sibling;
        var bu = S(y, N, k.value, T);
        if (bu === null) {
          N === null && (N = J);
          break;
        }
        l && N && bu.alternate === null && t(y, N), s = n(bu, s, Q), F === null ? H = bu : F.sibling = bu, F = bu, N = J;
      }
      if (k.done)
        return u(y, N), W && qt(y, Q), H;
      if (N === null) {
        for (; !k.done; Q++, k = d.next())
          k = E(y, k.value, T), k !== null && (s = n(k, s, Q), F === null ? H = k : F.sibling = k, F = k);
        return W && qt(y, Q), H;
      }
      for (N = a(N); !k.done; Q++, k = d.next())
        k = g(N, y, Q, k.value, T), k !== null && (l && k.alternate !== null && N.delete(k.key === null ? Q : k.key), s = n(k, s, Q), F === null ? H = k : F.sibling = k, F = k);
      return l && N.forEach(function(Md) {
        return t(y, Md);
      }), W && qt(y, Q), H;
    }
    function cl(y, s, d, T) {
      if (typeof d == "object" && d !== null && d.type === Cl && d.key === null && (d = d.props.children), typeof d == "object" && d !== null) {
        switch (d.$$typeof) {
          case wl:
            l: {
              for (var H = d.key; s !== null; ) {
                if (s.key === H) {
                  if (H = d.type, H === Cl) {
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
                  } else if (s.elementType === H || typeof H == "object" && H !== null && H.$$typeof === jl && qu(H) === s.type) {
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
              d.type === Cl ? (T = Uu(
                d.props.children,
                y.mode,
                T,
                d.key
              ), T.return = y, y = T) : (T = Je(
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
              for (H = d.key; s !== null; ) {
                if (s.key === H)
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
              T = Rf(d, y.mode, T), T.return = y, y = T;
            }
            return f(y);
          case jl:
            return d = qu(d), cl(
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
        if (Xl(d)) {
          if (H = Xl(d), typeof H != "function") throw Error(m(150));
          return d = H.call(d), C(
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
            Pe(d),
            T
          );
        if (d.$$typeof === Rl)
          return cl(
            y,
            s,
            $e(y, d),
            T
          );
        ln(y, d);
      }
      return typeof d == "string" && d !== "" || typeof d == "number" || typeof d == "bigint" ? (d = "" + d, s !== null && s.tag === 6 ? (u(y, s.sibling), T = e(s, d), T.return = y, y = T) : (u(y, s), T = Nf(d, y.mode, T), T.return = y, y = T), f(y)) : u(y, s);
    }
    return function(y, s, d, T) {
      try {
        wa = 0;
        var H = cl(
          y,
          s,
          d,
          T
        );
        return oa = null, H;
      } catch (N) {
        if (N === sa || N === ke) throw N;
        var F = Pl(29, N, null, y.mode);
        return F.lanes = T, F.return = y, F;
      } finally {
      }
    };
  }
  var Yu = Ls(!0), xs = Ls(!1), uu = !1;
  function xf(l) {
    l.updateQueue = {
      baseState: l.memoizedState,
      firstBaseUpdate: null,
      lastBaseUpdate: null,
      shared: { pending: null, lanes: 0, hiddenCallbacks: null },
      callbacks: null
    };
  }
  function Vf(l, t) {
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
      return e === null ? t.next = t : (t.next = e.next, e.next = t), a.pending = t, t = Ke(l), Ms(l, null, u), t;
    }
    return Ve(l, a, t, u), Ke(l);
  }
  function $a(l, t, u) {
    if (t = t.updateQueue, t !== null && (t = t.shared, (u & 4194048) !== 0)) {
      var a = t.lanes;
      a &= l.pendingLanes, u |= a, t.lanes = u, qi(l, u);
    }
  }
  function Kf(l, t) {
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
  var Jf = !1;
  function Fa() {
    if (Jf) {
      var l = ia;
      if (l !== null) throw l;
    }
  }
  function ka(l, t, u, a) {
    Jf = !1;
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
        if (g ? (K & S) === S : (a & S) === S) {
          S !== 0 && S === ca && (Jf = !0), r !== null && (r = r.next = {
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
                E = B({}, E, S);
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
  function Vs(l, t) {
    if (typeof l != "function")
      throw Error(m(191, l));
    l.call(t);
  }
  function Ks(l, t) {
    var u = l.callbacks;
    if (u !== null)
      for (l.callbacks = null, l = 0; l < u.length; l++)
        Vs(u[l], t);
  }
  var ya = o(null), tn = o(0);
  function Js(l, t) {
    l = Kt, p(tn, l), p(ya, t), Kt = l | t.baseLanes;
  }
  function wf() {
    p(tn, Kt), p(ya, ya.current);
  }
  function Wf() {
    Kt = tn.current, z(ya), z(tn);
  }
  var lt = o(null), dt = null;
  function nu(l) {
    var t = l.alternate;
    p(Sl, Sl.current & 1), p(lt, l), dt === null && (t === null || ya.current !== null || t.memoizedState !== null) && (dt = l);
  }
  function $f(l) {
    p(Sl, Sl.current), p(lt, l), dt === null && (dt = l);
  }
  function ws(l) {
    l.tag === 22 ? (p(Sl, Sl.current), p(lt, l), dt === null && (dt = l)) : fu();
  }
  function fu() {
    p(Sl, Sl.current), p(lt, lt.current);
  }
  function tt(l) {
    z(lt), dt === l && (dt = null), z(Sl);
  }
  var Sl = o(0);
  function un(l) {
    for (var t = l; t !== null; ) {
      if (t.tag === 13) {
        var u = t.memoizedState;
        if (u !== null && (u = u.dehydrated, u === null || ti(u) || ui(u)))
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
  var Gt = 0, X = null, nl = null, bl = null, an = !1, va = !1, Gu = !1, en = 0, Ia = 0, ma = null, Sm = 0;
  function ml() {
    throw Error(m(321));
  }
  function Ff(l, t) {
    if (t === null) return !1;
    for (var u = 0; u < t.length && u < l.length; u++)
      if (!Il(l[u], t[u])) return !1;
    return !0;
  }
  function kf(l, t, u, a, e, n) {
    return Gt = n, X = t, t.memoizedState = null, t.updateQueue = null, t.lanes = 0, b.H = l === null || l.memoizedState === null ? Ro : vc, Gu = !1, n = u(a, e), Gu = !1, va && (n = $s(
      t,
      u,
      a,
      e
    )), Ws(l), n;
  }
  function Ws(l) {
    b.H = te;
    var t = nl !== null && nl.next !== null;
    if (Gt = 0, bl = nl = X = null, an = !1, Ia = 0, ma = null, t) throw Error(m(300));
    l === null || Tl || (l = l.dependencies, l !== null && We(l) && (Tl = !0));
  }
  function $s(l, t, u, a) {
    X = l;
    var e = 0;
    do {
      if (va && (ma = null), Ia = 0, va = !1, 25 <= e) throw Error(m(301));
      if (e += 1, bl = nl = null, l.updateQueue != null) {
        var n = l.updateQueue;
        n.lastEffect = null, n.events = null, n.stores = null, n.memoCache != null && (n.memoCache.index = 0);
      }
      b.H = Ho, n = t(u, a);
    } while (va);
    return n;
  }
  function gm() {
    var l = b.H, t = l.useState()[0];
    return t = typeof t.then == "function" ? Pa(t) : t, l = l.useState()[0], (nl !== null ? nl.memoizedState : null) !== l && (X.flags |= 1024), t;
  }
  function If() {
    var l = en !== 0;
    return en = 0, l;
  }
  function Pf(l, t, u) {
    t.updateQueue = l.updateQueue, t.flags &= -2053, l.lanes &= ~u;
  }
  function lc(l) {
    if (an) {
      for (l = l.memoizedState; l !== null; ) {
        var t = l.queue;
        t !== null && (t.pending = null), l = l.next;
      }
      an = !1;
    }
    Gt = 0, bl = nl = X = null, va = !1, Ia = en = 0, ma = null;
  }
  function Bl() {
    var l = {
      memoizedState: null,
      baseState: null,
      baseQueue: null,
      queue: null,
      next: null
    };
    return bl === null ? X.memoizedState = bl = l : bl = bl.next = l, bl;
  }
  function gl() {
    if (nl === null) {
      var l = X.alternate;
      l = l !== null ? l.memoizedState : null;
    } else l = nl.next;
    var t = bl === null ? X.memoizedState : bl.next;
    if (t !== null)
      bl = t, nl = l;
    else {
      if (l === null)
        throw X.alternate === null ? Error(m(467)) : Error(m(310));
      nl = l, l = {
        memoizedState: nl.memoizedState,
        baseState: nl.baseState,
        baseQueue: nl.baseQueue,
        queue: nl.queue,
        next: null
      }, bl === null ? X.memoizedState = bl = l : bl = bl.next = l;
    }
    return bl;
  }
  function nn() {
    return { lastEffect: null, events: null, stores: null, memoCache: null };
  }
  function Pa(l) {
    var t = Ia;
    return Ia += 1, ma === null && (ma = []), l = Xs(ma, l, t), t = X, (bl === null ? t.memoizedState : bl.next) === null && (t = t.alternate, b.H = t === null || t.memoizedState === null ? Ro : vc), l;
  }
  function fn(l) {
    if (l !== null && typeof l == "object") {
      if (typeof l.then == "function") return Pa(l);
      if (l.$$typeof === Rl) return Dl(l);
    }
    throw Error(m(438, String(l)));
  }
  function tc(l) {
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
    if (t == null && (t = { data: [], index: 0 }), u === null && (u = nn(), X.updateQueue = u), u.memoCache = t, u = t.data[t.index], u === void 0)
      for (u = t.data[t.index] = Array(l), a = 0; a < l; a++)
        u[a] = Lu;
    return t.index++, u;
  }
  function jt(l, t) {
    return typeof t == "function" ? t(l) : t;
  }
  function cn(l) {
    var t = gl();
    return uc(t, nl, l);
  }
  function uc(l, t, u) {
    var a = l.queue;
    if (a === null) throw Error(m(311));
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
        if (E !== h.lane ? (K & E) === E : (Gt & E) === E) {
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
            }, i === null ? (c = i = E, f = n) : i = i.next = E, X.lanes |= S, su |= S;
          E = h.action, Gu && u(n, E), n = h.hasEagerState ? h.eagerState : u(n, E);
        } else
          S = {
            lane: E,
            revertLane: h.revertLane,
            gesture: h.gesture,
            action: h.action,
            hasEagerState: h.hasEagerState,
            eagerState: h.eagerState,
            next: null
          }, i === null ? (c = i = S, f = n) : i = i.next = S, X.lanes |= E, su |= E;
        h = h.next;
      } while (h !== null && h !== t);
      if (i === null ? f = n : i.next = c, !Il(n, l.memoizedState) && (Tl = !0, r && (u = ia, u !== null)))
        throw u;
      l.memoizedState = n, l.baseState = f, l.baseQueue = i, a.lastRenderedState = n;
    }
    return e === null && (a.lanes = 0), [l.memoizedState, a.dispatch];
  }
  function ac(l) {
    var t = gl(), u = t.queue;
    if (u === null) throw Error(m(311));
    u.lastRenderedReducer = l;
    var a = u.dispatch, e = u.pending, n = t.memoizedState;
    if (e !== null) {
      u.pending = null;
      var f = e = e.next;
      do
        n = l(n, f.action), f = f.next;
      while (f !== e);
      Il(n, t.memoizedState) || (Tl = !0), t.memoizedState = n, t.baseQueue === null && (t.baseState = n), u.lastRenderedState = n;
    }
    return [n, a];
  }
  function Fs(l, t, u) {
    var a = X, e = gl(), n = W;
    if (n) {
      if (u === void 0) throw Error(m(407));
      u = u();
    } else u = t();
    var f = !Il(
      (nl || e).memoizedState,
      u
    );
    if (f && (e.memoizedState = u, Tl = !0), e = e.queue, fc(Ps.bind(null, a, e, l), [
      l
    ]), e.getSnapshot !== t || f || bl !== null && bl.memoizedState.tag & 1) {
      if (a.flags |= 2048, da(
        9,
        { destroy: void 0 },
        Is.bind(
          null,
          a,
          e,
          u,
          t
        ),
        null
      ), sl === null) throw Error(m(349));
      n || (Gt & 127) !== 0 || ks(a, t, u);
    }
    return u;
  }
  function ks(l, t, u) {
    l.flags |= 16384, l = { getSnapshot: t, value: u }, t = X.updateQueue, t === null ? (t = nn(), X.updateQueue = t, t.stores = [l]) : (u = t.stores, u === null ? t.stores = [l] : u.push(l));
  }
  function Is(l, t, u, a) {
    t.value = u, t.getSnapshot = a, lo(t) && to(l);
  }
  function Ps(l, t, u) {
    return u(function() {
      lo(t) && to(l);
    });
  }
  function lo(l) {
    var t = l.getSnapshot;
    l = l.value;
    try {
      var u = t();
      return !Il(l, u);
    } catch {
      return !0;
    }
  }
  function to(l) {
    var t = Du(l, 2);
    t !== null && Jl(t, l, 2);
  }
  function ec(l) {
    var t = Bl();
    if (typeof l == "function") {
      var u = l;
      if (l = u(), Gu) {
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
      lastRenderedReducer: jt,
      lastRenderedState: l
    }, t;
  }
  function uo(l, t, u, a) {
    return l.baseState = u, uc(
      l,
      nl,
      typeof a == "function" ? a : jt
    );
  }
  function rm(l, t, u, a, e) {
    if (yn(l)) throw Error(m(485));
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
      b.T !== null ? u(!0) : n.isTransition = !1, a(n), u = t.pending, u === null ? (n.next = t.pending = n, ao(t, n)) : (n.next = u.next, t.pending = u.next = n);
    }
  }
  function ao(l, t) {
    var u = t.action, a = t.payload, e = l.state;
    if (t.isTransition) {
      var n = b.T, f = {};
      b.T = f;
      try {
        var c = u(e, a), i = b.S;
        i !== null && i(f, c), eo(l, t, c);
      } catch (h) {
        nc(l, t, h);
      } finally {
        n !== null && f.types !== null && (n.types = f.types), b.T = n;
      }
    } else
      try {
        n = u(e, a), eo(l, t, n);
      } catch (h) {
        nc(l, t, h);
      }
  }
  function eo(l, t, u) {
    u !== null && typeof u == "object" && typeof u.then == "function" ? u.then(
      function(a) {
        no(l, t, a);
      },
      function(a) {
        return nc(l, t, a);
      }
    ) : no(l, t, u);
  }
  function no(l, t, u) {
    t.status = "fulfilled", t.value = u, fo(t), l.state = u, t = l.pending, t !== null && (u = t.next, u === t ? l.pending = null : (u = u.next, t.next = u, ao(l, u)));
  }
  function nc(l, t, u) {
    var a = l.pending;
    if (l.pending = null, a !== null) {
      a = a.next;
      do
        t.status = "rejected", t.reason = u, fo(t), t = t.next;
      while (t !== a);
    }
    l.action = null;
  }
  function fo(l) {
    l = l.listeners;
    for (var t = 0; t < l.length; t++) (0, l[t])();
  }
  function co(l, t) {
    return t;
  }
  function io(l, t) {
    if (W) {
      var u = sl.formState;
      if (u !== null) {
        l: {
          var a = X;
          if (W) {
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
      lastRenderedReducer: co,
      lastRenderedState: t
    }, u.queue = a, u = Do.bind(
      null,
      X,
      a
    ), a.dispatch = u, a = ec(!1), n = yc.bind(
      null,
      X,
      !1,
      a.queue
    ), a = Bl(), e = {
      state: t,
      dispatch: null,
      action: l,
      pending: null
    }, a.queue = e, u = rm.bind(
      null,
      X,
      e,
      n,
      u
    ), e.dispatch = u, a.memoizedState = l, [t, u, !1];
  }
  function so(l) {
    var t = gl();
    return oo(t, nl, l);
  }
  function oo(l, t, u) {
    if (t = uc(
      l,
      t,
      co
    )[0], l = cn(jt)[0], typeof t == "object" && t !== null && typeof t.then == "function")
      try {
        var a = Pa(t);
      } catch (f) {
        throw f === sa ? ke : f;
      }
    else a = t;
    t = gl();
    var e = t.queue, n = e.dispatch;
    return u !== t.memoizedState && (X.flags |= 2048, da(
      9,
      { destroy: void 0 },
      bm.bind(null, e, u),
      null
    )), [a, n, l];
  }
  function bm(l, t) {
    l.action = t;
  }
  function yo(l) {
    var t = gl(), u = nl;
    if (u !== null)
      return oo(t, u, l);
    gl(), t = t.memoizedState, u = gl();
    var a = u.queue.dispatch;
    return u.memoizedState = l, [t, a, !1];
  }
  function da(l, t, u, a) {
    return l = { tag: l, create: u, deps: a, inst: t, next: null }, t = X.updateQueue, t === null && (t = nn(), X.updateQueue = t), u = t.lastEffect, u === null ? t.lastEffect = l.next = l : (a = u.next, u.next = l, l.next = a, t.lastEffect = l), l;
  }
  function vo() {
    return gl().memoizedState;
  }
  function sn(l, t, u, a) {
    var e = Bl();
    X.flags |= l, e.memoizedState = da(
      1 | t,
      { destroy: void 0 },
      u,
      a === void 0 ? null : a
    );
  }
  function on(l, t, u, a) {
    var e = gl();
    a = a === void 0 ? null : a;
    var n = e.memoizedState.inst;
    nl !== null && a !== null && Ff(a, nl.memoizedState.deps) ? e.memoizedState = da(t, n, u, a) : (X.flags |= l, e.memoizedState = da(
      1 | t,
      n,
      u,
      a
    ));
  }
  function mo(l, t) {
    sn(8390656, 8, l, t);
  }
  function fc(l, t) {
    on(2048, 8, l, t);
  }
  function Tm(l) {
    X.flags |= 4;
    var t = X.updateQueue;
    if (t === null)
      t = nn(), X.updateQueue = t, t.events = [l];
    else {
      var u = t.events;
      u === null ? t.events = [l] : u.push(l);
    }
  }
  function ho(l) {
    var t = gl().memoizedState;
    return Tm({ ref: t, nextImpl: l }), function() {
      if ((I & 2) !== 0) throw Error(m(440));
      return t.impl.apply(void 0, arguments);
    };
  }
  function So(l, t) {
    return on(4, 2, l, t);
  }
  function go(l, t) {
    return on(4, 4, l, t);
  }
  function ro(l, t) {
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
  function bo(l, t, u) {
    u = u != null ? u.concat([l]) : null, on(4, 4, ro.bind(null, t, l), u);
  }
  function cc() {
  }
  function To(l, t) {
    var u = gl();
    t = t === void 0 ? null : t;
    var a = u.memoizedState;
    return t !== null && Ff(t, a[1]) ? a[0] : (u.memoizedState = [l, t], l);
  }
  function Eo(l, t) {
    var u = gl();
    t = t === void 0 ? null : t;
    var a = u.memoizedState;
    if (t !== null && Ff(t, a[1]))
      return a[0];
    if (a = l(), Gu) {
      $t(!0);
      try {
        l();
      } finally {
        $t(!1);
      }
    }
    return u.memoizedState = [a, t], a;
  }
  function ic(l, t, u) {
    return u === void 0 || (Gt & 1073741824) !== 0 && (K & 261930) === 0 ? l.memoizedState = t : (l.memoizedState = u, l = z0(), X.lanes |= l, su |= l, u);
  }
  function zo(l, t, u, a) {
    return Il(u, t) ? u : ya.current !== null ? (l = ic(l, u, a), Il(l, t) || (Tl = !0), l) : (Gt & 42) === 0 || (Gt & 1073741824) !== 0 && (K & 261930) === 0 ? (Tl = !0, l.memoizedState = u) : (l = z0(), X.lanes |= l, su |= l, t);
  }
  function Ao(l, t, u, a, e) {
    var n = M.p;
    M.p = n !== 0 && 8 > n ? n : 8;
    var f = b.T, c = {};
    b.T = c, yc(l, !1, t, u);
    try {
      var i = e(), h = b.S;
      if (h !== null && h(c, i), i !== null && typeof i == "object" && typeof i.then == "function") {
        var r = hm(
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
  function Em() {
  }
  function sc(l, t, u, a) {
    if (l.tag !== 5) throw Error(m(476));
    var e = _o(l).queue;
    Ao(
      l,
      e,
      t,
      Y,
      u === null ? Em : function() {
        return Oo(l), u(a);
      }
    );
  }
  function _o(l) {
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
        lastRenderedReducer: jt,
        lastRenderedState: Y
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
        lastRenderedReducer: jt,
        lastRenderedState: u
      },
      next: null
    }, l.memoizedState = t, l = l.alternate, l !== null && (l.memoizedState = t), t;
  }
  function Oo(l) {
    var t = _o(l);
    t.next === null && (t = l.alternate.memoizedState), le(
      l,
      t.next.queue,
      {},
      et()
    );
  }
  function oc() {
    return Dl(ge);
  }
  function Mo() {
    return gl().memoizedState;
  }
  function po() {
    return gl().memoizedState;
  }
  function zm(l) {
    for (var t = l.return; t !== null; ) {
      switch (t.tag) {
        case 24:
        case 3:
          var u = et();
          l = au(u);
          var a = eu(t, l, u);
          a !== null && (Jl(a, t, u), $a(a, t, u)), t = { cache: Xf() }, l.payload = t;
          return;
      }
      t = t.return;
    }
  }
  function Am(l, t, u) {
    var a = et();
    u = {
      lane: a,
      revertLane: 0,
      gesture: null,
      action: u,
      hasEagerState: !1,
      eagerState: null,
      next: null
    }, yn(l) ? Uo(t, u) : (u = Df(l, t, u, a), u !== null && (Jl(u, l, a), No(u, t, a)));
  }
  function Do(l, t, u) {
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
    if (yn(l)) Uo(t, e);
    else {
      var n = l.alternate;
      if (l.lanes === 0 && (n === null || n.lanes === 0) && (n = t.lastRenderedReducer, n !== null))
        try {
          var f = t.lastRenderedState, c = n(f, u);
          if (e.hasEagerState = !0, e.eagerState = c, Il(c, f))
            return Ve(l, t, e, 0), sl === null && xe(), !1;
        } catch {
        } finally {
        }
      if (u = Df(l, t, e, a), u !== null)
        return Jl(u, l, a), No(u, t, a), !0;
    }
    return !1;
  }
  function yc(l, t, u, a) {
    if (a = {
      lane: 2,
      revertLane: xc(),
      gesture: null,
      action: a,
      hasEagerState: !1,
      eagerState: null,
      next: null
    }, yn(l)) {
      if (t) throw Error(m(479));
    } else
      t = Df(
        l,
        u,
        a,
        2
      ), t !== null && Jl(t, l, 2);
  }
  function yn(l) {
    var t = l.alternate;
    return l === X || t !== null && t === X;
  }
  function Uo(l, t) {
    va = an = !0;
    var u = l.pending;
    u === null ? t.next = t : (t.next = u.next, u.next = t), l.pending = t;
  }
  function No(l, t, u) {
    if ((u & 4194048) !== 0) {
      var a = t.lanes;
      a &= l.pendingLanes, u |= a, t.lanes = u, qi(l, u);
    }
  }
  var te = {
    readContext: Dl,
    use: fn,
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
  var Ro = {
    readContext: Dl,
    use: fn,
    useCallback: function(l, t) {
      return Bl().memoizedState = [
        l,
        t === void 0 ? null : t
      ], l;
    },
    useContext: Dl,
    useEffect: mo,
    useImperativeHandle: function(l, t, u) {
      u = u != null ? u.concat([l]) : null, sn(
        4194308,
        4,
        ro.bind(null, t, l),
        u
      );
    },
    useLayoutEffect: function(l, t) {
      return sn(4194308, 4, l, t);
    },
    useInsertionEffect: function(l, t) {
      sn(4, 2, l, t);
    },
    useMemo: function(l, t) {
      var u = Bl();
      t = t === void 0 ? null : t;
      var a = l();
      if (Gu) {
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
        if (Gu) {
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
      }, a.queue = l, l = l.dispatch = Am.bind(
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
      l = ec(l);
      var t = l.queue, u = Do.bind(null, X, t);
      return t.dispatch = u, [l.memoizedState, u];
    },
    useDebugValue: cc,
    useDeferredValue: function(l, t) {
      var u = Bl();
      return ic(u, l, t);
    },
    useTransition: function() {
      var l = ec(!1);
      return l = Ao.bind(
        null,
        X,
        l.queue,
        !0,
        !1
      ), Bl().memoizedState = l, [!1, l];
    },
    useSyncExternalStore: function(l, t, u) {
      var a = X, e = Bl();
      if (W) {
        if (u === void 0)
          throw Error(m(407));
        u = u();
      } else {
        if (u = t(), sl === null)
          throw Error(m(349));
        (K & 127) !== 0 || ks(a, t, u);
      }
      e.memoizedState = u;
      var n = { value: u, getSnapshot: t };
      return e.queue = n, mo(Ps.bind(null, a, n, l), [
        l
      ]), a.flags |= 2048, da(
        9,
        { destroy: void 0 },
        Is.bind(
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
      if (W) {
        var u = Ot, a = _t;
        u = (a & ~(1 << 32 - kl(a) - 1)).toString(32) + u, t = "_" + t + "R_" + u, u = en++, 0 < u && (t += "H" + u.toString(32)), t += "_";
      } else
        u = Sm++, t = "_" + t + "r_" + u.toString(32) + "_";
      return l.memoizedState = t;
    },
    useHostTransitionStatus: oc,
    useFormState: io,
    useActionState: io,
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
      return t.queue = u, t = yc.bind(
        null,
        X,
        !0,
        u
      ), u.dispatch = t, [l, t];
    },
    useMemoCache: tc,
    useCacheRefresh: function() {
      return Bl().memoizedState = zm.bind(
        null,
        X
      );
    },
    useEffectEvent: function(l) {
      var t = Bl(), u = { impl: l };
      return t.memoizedState = u, function() {
        if ((I & 2) !== 0)
          throw Error(m(440));
        return u.impl.apply(void 0, arguments);
      };
    }
  }, vc = {
    readContext: Dl,
    use: fn,
    useCallback: To,
    useContext: Dl,
    useEffect: fc,
    useImperativeHandle: bo,
    useInsertionEffect: So,
    useLayoutEffect: go,
    useMemo: Eo,
    useReducer: cn,
    useRef: vo,
    useState: function() {
      return cn(jt);
    },
    useDebugValue: cc,
    useDeferredValue: function(l, t) {
      var u = gl();
      return zo(
        u,
        nl.memoizedState,
        l,
        t
      );
    },
    useTransition: function() {
      var l = cn(jt)[0], t = gl().memoizedState;
      return [
        typeof l == "boolean" ? l : Pa(l),
        t
      ];
    },
    useSyncExternalStore: Fs,
    useId: Mo,
    useHostTransitionStatus: oc,
    useFormState: so,
    useActionState: so,
    useOptimistic: function(l, t) {
      var u = gl();
      return uo(u, nl, l, t);
    },
    useMemoCache: tc,
    useCacheRefresh: po
  };
  vc.useEffectEvent = ho;
  var Ho = {
    readContext: Dl,
    use: fn,
    useCallback: To,
    useContext: Dl,
    useEffect: fc,
    useImperativeHandle: bo,
    useInsertionEffect: So,
    useLayoutEffect: go,
    useMemo: Eo,
    useReducer: ac,
    useRef: vo,
    useState: function() {
      return ac(jt);
    },
    useDebugValue: cc,
    useDeferredValue: function(l, t) {
      var u = gl();
      return nl === null ? ic(u, l, t) : zo(
        u,
        nl.memoizedState,
        l,
        t
      );
    },
    useTransition: function() {
      var l = ac(jt)[0], t = gl().memoizedState;
      return [
        typeof l == "boolean" ? l : Pa(l),
        t
      ];
    },
    useSyncExternalStore: Fs,
    useId: Mo,
    useHostTransitionStatus: oc,
    useFormState: yo,
    useActionState: yo,
    useOptimistic: function(l, t) {
      var u = gl();
      return nl !== null ? uo(u, nl, l, t) : (u.baseState = l, [l, u.queue.dispatch]);
    },
    useMemoCache: tc,
    useCacheRefresh: po
  };
  Ho.useEffectEvent = ho;
  function mc(l, t, u, a) {
    t = l.memoizedState, u = u(a, t), u = u == null ? t : B({}, t, u), l.memoizedState = u, l.lanes === 0 && (l.updateQueue.baseState = u);
  }
  var dc = {
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
  function Co(l, t, u, a, e, n, f) {
    return l = l.stateNode, typeof l.shouldComponentUpdate == "function" ? l.shouldComponentUpdate(a, n, f) : t.prototype && t.prototype.isPureReactComponent ? !Za(u, a) || !Za(e, n) : !0;
  }
  function qo(l, t, u, a) {
    l = t.state, typeof t.componentWillReceiveProps == "function" && t.componentWillReceiveProps(u, a), typeof t.UNSAFE_componentWillReceiveProps == "function" && t.UNSAFE_componentWillReceiveProps(u, a), t.state !== l && dc.enqueueReplaceState(t, t.state, null);
  }
  function ju(l, t) {
    var u = t;
    if ("ref" in t) {
      u = {};
      for (var a in t)
        a !== "ref" && (u[a] = t[a]);
    }
    if (l = l.defaultProps) {
      u === t && (u = B({}, u));
      for (var e in l)
        u[e] === void 0 && (u[e] = l[e]);
    }
    return u;
  }
  function Bo(l) {
    Le(l);
  }
  function Yo(l) {
    console.error(l);
  }
  function Go(l) {
    Le(l);
  }
  function vn(l, t) {
    try {
      var u = l.onUncaughtError;
      u(t.value, { componentStack: t.stack });
    } catch (a) {
      setTimeout(function() {
        throw a;
      });
    }
  }
  function jo(l, t, u) {
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
  function hc(l, t, u) {
    return u = au(u), u.tag = 3, u.payload = { element: null }, u.callback = function() {
      vn(l, t);
    }, u;
  }
  function Xo(l) {
    return l = au(l), l.tag = 3, l;
  }
  function Qo(l, t, u, a) {
    var e = u.type.getDerivedStateFromError;
    if (typeof e == "function") {
      var n = a.value;
      l.payload = function() {
        return e(n);
      }, l.callback = function() {
        jo(t, u, a);
      };
    }
    var f = u.stateNode;
    f !== null && typeof f.componentDidCatch == "function" && (l.callback = function() {
      jo(t, u, a), typeof e != "function" && (ou === null ? ou = /* @__PURE__ */ new Set([this]) : ou.add(this));
      var c = a.stack;
      this.componentDidCatch(a.value, {
        componentStack: c !== null ? c : ""
      });
    });
  }
  function _m(l, t, u, a, e) {
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
            return dt === null ? _n() : u.alternate === null && dl === 0 && (dl = 3), u.flags &= -257, u.flags |= 65536, u.lanes = e, a === Ie ? u.flags |= 16384 : (t = u.updateQueue, t === null ? u.updateQueue = /* @__PURE__ */ new Set([a]) : t.add(a), Qc(l, a, e)), !1;
          case 22:
            return u.flags |= 65536, a === Ie ? u.flags |= 16384 : (t = u.updateQueue, t === null ? (t = {
              transitions: null,
              markerInstances: null,
              retryQueue: /* @__PURE__ */ new Set([a])
            }, u.updateQueue = t) : (u = t.retryQueue, u === null ? t.retryQueue = /* @__PURE__ */ new Set([a]) : u.add(a)), Qc(l, a, e)), !1;
        }
        throw Error(m(435, u.tag));
      }
      return Qc(l, a, e), _n(), !1;
    }
    if (W)
      return t = lt.current, t !== null ? ((t.flags & 65536) === 0 && (t.flags |= 256), t.flags |= 65536, t.lanes = e, a !== qf && (l = Error(m(422), { cause: a }), Va(ot(l, u)))) : (a !== qf && (t = Error(m(423), {
        cause: a
      }), Va(
        ot(t, u)
      )), l = l.current.alternate, l.flags |= 65536, e &= -e, l.lanes |= e, a = ot(a, u), e = hc(
        l.stateNode,
        a,
        e
      ), Kf(l, e), dl !== 4 && (dl = 2)), !1;
    var n = Error(m(520), { cause: a });
    if (n = ot(n, u), se === null ? se = [n] : se.push(n), dl !== 4 && (dl = 2), t === null) return !0;
    a = ot(a, u), u = t;
    do {
      switch (u.tag) {
        case 3:
          return u.flags |= 65536, l = e & -e, u.lanes |= l, l = hc(u.stateNode, a, l), Kf(u, l), !1;
        case 1:
          if (t = u.type, n = u.stateNode, (u.flags & 128) === 0 && (typeof t.getDerivedStateFromError == "function" || n !== null && typeof n.componentDidCatch == "function" && (ou === null || !ou.has(n))))
            return u.flags |= 65536, e &= -e, u.lanes |= e, e = Xo(e), Qo(
              e,
              l,
              u,
              a
            ), Kf(u, e), !1;
      }
      u = u.return;
    } while (u !== null);
    return !1;
  }
  var Sc = Error(m(461)), Tl = !1;
  function Ul(l, t, u, a) {
    t.child = l === null ? xs(t, null, u, a) : Yu(
      t,
      l.child,
      u,
      a
    );
  }
  function Zo(l, t, u, a, e) {
    u = u.render;
    var n = t.ref;
    if ("ref" in a) {
      var f = {};
      for (var c in a)
        c !== "ref" && (f[c] = a[c]);
    } else f = a;
    return Hu(t), a = kf(
      l,
      t,
      u,
      f,
      n,
      e
    ), c = If(), l !== null && !Tl ? (Pf(l, t, e), Xt(l, t, e)) : (W && c && Hf(t), t.flags |= 1, Ul(l, t, a, e), t.child);
  }
  function Lo(l, t, u, a, e) {
    if (l === null) {
      var n = u.type;
      return typeof n == "function" && !Uf(n) && n.defaultProps === void 0 && u.compare === null ? (t.tag = 15, t.type = n, xo(
        l,
        t,
        n,
        a,
        e
      )) : (l = Je(
        u.type,
        null,
        a,
        t,
        t.mode,
        e
      ), l.ref = t.ref, l.return = t, t.child = l);
    }
    if (n = l.child, !_c(l, e)) {
      var f = n.memoizedProps;
      if (u = u.compare, u = u !== null ? u : Za, u(f, a) && l.ref === t.ref)
        return Xt(l, t, e);
    }
    return t.flags |= 1, l = Ct(n, a), l.ref = t.ref, l.return = t, t.child = l;
  }
  function xo(l, t, u, a, e) {
    if (l !== null) {
      var n = l.memoizedProps;
      if (Za(n, a) && l.ref === t.ref)
        if (Tl = !1, t.pendingProps = a = n, _c(l, e))
          (l.flags & 131072) !== 0 && (Tl = !0);
        else
          return t.lanes = l.lanes, Xt(l, t, e);
    }
    return gc(
      l,
      t,
      u,
      a,
      e
    );
  }
  function Vo(l, t, u, a) {
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
        return Ko(
          l,
          t,
          n,
          u,
          a
        );
      }
      if ((u & 536870912) !== 0)
        t.memoizedState = { baseLanes: 0, cachePool: null }, l !== null && Fe(
          t,
          n !== null ? n.cachePool : null
        ), n !== null ? Js(t, n) : wf(), ws(t);
      else
        return a = t.lanes = 536870912, Ko(
          l,
          t,
          n !== null ? n.baseLanes | u : u,
          u,
          a
        );
    } else
      n !== null ? (Fe(t, n.cachePool), Js(t, n), fu(), t.memoizedState = null) : (l !== null && Fe(t, null), wf(), fu());
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
  function Ko(l, t, u, a, e) {
    var n = Zf();
    return n = n === null ? null : { parent: rl._currentValue, pool: n }, t.memoizedState = {
      baseLanes: u,
      cachePool: n
    }, l !== null && Fe(t, null), wf(), ws(t), l !== null && fa(l, t, a, !0), t.childLanes = e, null;
  }
  function mn(l, t) {
    return t = hn(
      { mode: t.mode, children: t.children },
      l.mode
    ), t.ref = l.ref, l.child = t, t.return = l, t;
  }
  function Jo(l, t, u) {
    return Yu(t, l.child, null, u), l = mn(t, t.pendingProps), l.flags |= 2, tt(t), t.memoizedState = null, l;
  }
  function Om(l, t, u) {
    var a = t.pendingProps, e = (t.flags & 128) !== 0;
    if (t.flags &= -129, l === null) {
      if (W) {
        if (a.mode === "hidden")
          return l = mn(t, a), t.lanes = 536870912, ue(null, l);
        if ($f(t), (l = ol) ? (l = ey(
          l,
          mt
        ), l = l !== null && l.data === "&" ? l : null, l !== null && (t.memoizedState = {
          dehydrated: l,
          treeContext: It !== null ? { id: _t, overflow: Ot } : null,
          retryLane: 536870912,
          hydrationErrors: null
        }, u = Ds(l), u.return = t, t.child = u, pl = t, ol = null)) : l = null, l === null) throw lu(t);
        return t.lanes = 536870912, null;
      }
      return mn(t, a);
    }
    var n = l.memoizedState;
    if (n !== null) {
      var f = n.dehydrated;
      if ($f(t), e)
        if (t.flags & 256)
          t.flags &= -257, t = Jo(
            l,
            t,
            u
          );
        else if (t.memoizedState !== null)
          t.child = l.child, t.flags |= 128, t = null;
        else throw Error(m(558));
      else if (Tl || fa(l, t, u, !1), e = (u & l.childLanes) !== 0, Tl || e) {
        if (a = sl, a !== null && (f = Bi(a, u), f !== 0 && f !== n.retryLane))
          throw n.retryLane = f, Du(l, f), Jl(a, l, f), Sc;
        _n(), t = Jo(
          l,
          t,
          u
        );
      } else
        l = n.treeContext, ol = ht(f.nextSibling), pl = t, W = !0, Pt = null, mt = !1, l !== null && Rs(t, l), t = mn(t, a), t.flags |= 4096;
      return t;
    }
    return l = Ct(l.child, {
      mode: a.mode,
      children: a.children
    }), l.ref = t.ref, t.child = l, l.return = t, l;
  }
  function dn(l, t) {
    var u = t.ref;
    if (u === null)
      l !== null && l.ref !== null && (t.flags |= 4194816);
    else {
      if (typeof u != "function" && typeof u != "object")
        throw Error(m(284));
      (l === null || l.ref !== u) && (t.flags |= 4194816);
    }
  }
  function gc(l, t, u, a, e) {
    return Hu(t), u = kf(
      l,
      t,
      u,
      a,
      void 0,
      e
    ), a = If(), l !== null && !Tl ? (Pf(l, t, e), Xt(l, t, e)) : (W && a && Hf(t), t.flags |= 1, Ul(l, t, u, e), t.child);
  }
  function wo(l, t, u, a, e, n) {
    return Hu(t), t.updateQueue = null, u = $s(
      t,
      a,
      u,
      e
    ), Ws(l), a = If(), l !== null && !Tl ? (Pf(l, t, n), Xt(l, t, n)) : (W && a && Hf(t), t.flags |= 1, Ul(l, t, u, n), t.child);
  }
  function Wo(l, t, u, a, e) {
    if (Hu(t), t.stateNode === null) {
      var n = ua, f = u.contextType;
      typeof f == "object" && f !== null && (n = Dl(f)), n = new u(a, n), t.memoizedState = n.state !== null && n.state !== void 0 ? n.state : null, n.updater = dc, t.stateNode = n, n._reactInternals = t, n = t.stateNode, n.props = a, n.state = t.memoizedState, n.refs = {}, xf(t), f = u.contextType, n.context = typeof f == "object" && f !== null ? Dl(f) : ua, n.state = t.memoizedState, f = u.getDerivedStateFromProps, typeof f == "function" && (mc(
        t,
        u,
        f,
        a
      ), n.state = t.memoizedState), typeof u.getDerivedStateFromProps == "function" || typeof n.getSnapshotBeforeUpdate == "function" || typeof n.UNSAFE_componentWillMount != "function" && typeof n.componentWillMount != "function" || (f = n.state, typeof n.componentWillMount == "function" && n.componentWillMount(), typeof n.UNSAFE_componentWillMount == "function" && n.UNSAFE_componentWillMount(), f !== n.state && dc.enqueueReplaceState(n, n.state, null), ka(t, a, n, e), Fa(), n.state = t.memoizedState), typeof n.componentDidMount == "function" && (t.flags |= 4194308), a = !0;
    } else if (l === null) {
      n = t.stateNode;
      var c = t.memoizedProps, i = ju(u, c);
      n.props = i;
      var h = n.context, r = u.contextType;
      f = ua, typeof r == "object" && r !== null && (f = Dl(r));
      var E = u.getDerivedStateFromProps;
      r = typeof E == "function" || typeof n.getSnapshotBeforeUpdate == "function", c = t.pendingProps !== c, r || typeof n.UNSAFE_componentWillReceiveProps != "function" && typeof n.componentWillReceiveProps != "function" || (c || h !== f) && qo(
        t,
        n,
        a,
        f
      ), uu = !1;
      var S = t.memoizedState;
      n.state = S, ka(t, a, n, e), Fa(), h = t.memoizedState, c || S !== h || uu ? (typeof E == "function" && (mc(
        t,
        u,
        E,
        a
      ), h = t.memoizedState), (i = uu || Co(
        t,
        u,
        i,
        a,
        S,
        h,
        f
      )) ? (r || typeof n.UNSAFE_componentWillMount != "function" && typeof n.componentWillMount != "function" || (typeof n.componentWillMount == "function" && n.componentWillMount(), typeof n.UNSAFE_componentWillMount == "function" && n.UNSAFE_componentWillMount()), typeof n.componentDidMount == "function" && (t.flags |= 4194308)) : (typeof n.componentDidMount == "function" && (t.flags |= 4194308), t.memoizedProps = a, t.memoizedState = h), n.props = a, n.state = h, n.context = f, a = i) : (typeof n.componentDidMount == "function" && (t.flags |= 4194308), a = !1);
    } else {
      n = t.stateNode, Vf(l, t), f = t.memoizedProps, r = ju(u, f), n.props = r, E = t.pendingProps, S = n.context, h = u.contextType, i = ua, typeof h == "object" && h !== null && (i = Dl(h)), c = u.getDerivedStateFromProps, (h = typeof c == "function" || typeof n.getSnapshotBeforeUpdate == "function") || typeof n.UNSAFE_componentWillReceiveProps != "function" && typeof n.componentWillReceiveProps != "function" || (f !== E || S !== i) && qo(
        t,
        n,
        a,
        i
      ), uu = !1, S = t.memoizedState, n.state = S, ka(t, a, n, e), Fa();
      var g = t.memoizedState;
      f !== E || S !== g || uu || l !== null && l.dependencies !== null && We(l.dependencies) ? (typeof c == "function" && (mc(
        t,
        u,
        c,
        a
      ), g = t.memoizedState), (r = uu || Co(
        t,
        u,
        r,
        a,
        S,
        g,
        i
      ) || l !== null && l.dependencies !== null && We(l.dependencies)) ? (h || typeof n.UNSAFE_componentWillUpdate != "function" && typeof n.componentWillUpdate != "function" || (typeof n.componentWillUpdate == "function" && n.componentWillUpdate(a, g, i), typeof n.UNSAFE_componentWillUpdate == "function" && n.UNSAFE_componentWillUpdate(
        a,
        g,
        i
      )), typeof n.componentDidUpdate == "function" && (t.flags |= 4), typeof n.getSnapshotBeforeUpdate == "function" && (t.flags |= 1024)) : (typeof n.componentDidUpdate != "function" || f === l.memoizedProps && S === l.memoizedState || (t.flags |= 4), typeof n.getSnapshotBeforeUpdate != "function" || f === l.memoizedProps && S === l.memoizedState || (t.flags |= 1024), t.memoizedProps = a, t.memoizedState = g), n.props = a, n.state = g, n.context = i, a = r) : (typeof n.componentDidUpdate != "function" || f === l.memoizedProps && S === l.memoizedState || (t.flags |= 4), typeof n.getSnapshotBeforeUpdate != "function" || f === l.memoizedProps && S === l.memoizedState || (t.flags |= 1024), a = !1);
    }
    return n = a, dn(l, t), a = (t.flags & 128) !== 0, n || a ? (n = t.stateNode, u = a && typeof u.getDerivedStateFromError != "function" ? null : n.render(), t.flags |= 1, l !== null && a ? (t.child = Yu(
      t,
      l.child,
      null,
      e
    ), t.child = Yu(
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
  function $o(l, t, u, a) {
    return Nu(), t.flags |= 256, Ul(l, t, u, a), t.child;
  }
  var rc = {
    dehydrated: null,
    treeContext: null,
    retryLane: 0,
    hydrationErrors: null
  };
  function bc(l) {
    return { baseLanes: l, cachePool: Gs() };
  }
  function Tc(l, t, u) {
    return l = l !== null ? l.childLanes & ~u : 0, t && (l |= at), l;
  }
  function Fo(l, t, u) {
    var a = t.pendingProps, e = !1, n = (t.flags & 128) !== 0, f;
    if ((f = n) || (f = l !== null && l.memoizedState === null ? !1 : (Sl.current & 2) !== 0), f && (e = !0, t.flags &= -129), f = (t.flags & 32) !== 0, t.flags &= -33, l === null) {
      if (W) {
        if (e ? nu(t) : fu(), (l = ol) ? (l = ey(
          l,
          mt
        ), l = l !== null && l.data !== "&" ? l : null, l !== null && (t.memoizedState = {
          dehydrated: l,
          treeContext: It !== null ? { id: _t, overflow: Ot } : null,
          retryLane: 536870912,
          hydrationErrors: null
        }, u = Ds(l), u.return = t, t.child = u, pl = t, ol = null)) : l = null, l === null) throw lu(t);
        return ui(l) ? t.lanes = 32 : t.lanes = 536870912, null;
      }
      var c = a.children;
      return a = a.fallback, e ? (fu(), e = t.mode, c = hn(
        { mode: "hidden", children: c },
        e
      ), a = Uu(
        a,
        e,
        u,
        null
      ), c.return = t, a.return = t, c.sibling = a, t.child = c, a = t.child, a.memoizedState = bc(u), a.childLanes = Tc(
        l,
        f,
        u
      ), t.memoizedState = rc, ue(null, a)) : (nu(t), Ec(t, c));
    }
    var i = l.memoizedState;
    if (i !== null && (c = i.dehydrated, c !== null)) {
      if (n)
        t.flags & 256 ? (nu(t), t.flags &= -257, t = zc(
          l,
          t,
          u
        )) : t.memoizedState !== null ? (fu(), t.child = l.child, t.flags |= 128, t = null) : (fu(), c = a.fallback, e = t.mode, a = hn(
          { mode: "visible", children: a.children },
          e
        ), c = Uu(
          c,
          e,
          u,
          null
        ), c.flags |= 2, a.return = t, c.return = t, a.sibling = c, t.child = a, Yu(
          t,
          l.child,
          null,
          u
        ), a = t.child, a.memoizedState = bc(u), a.childLanes = Tc(
          l,
          f,
          u
        ), t.memoizedState = rc, t = ue(null, a));
      else if (nu(t), ui(c)) {
        if (f = c.nextSibling && c.nextSibling.dataset, f) var h = f.dgst;
        f = h, a = Error(m(419)), a.stack = "", a.digest = f, Va({ value: a, source: null, stack: null }), t = zc(
          l,
          t,
          u
        );
      } else if (Tl || fa(l, t, u, !1), f = (u & l.childLanes) !== 0, Tl || f) {
        if (f = sl, f !== null && (a = Bi(f, u), a !== 0 && a !== i.retryLane))
          throw i.retryLane = a, Du(l, a), Jl(f, l, a), Sc;
        ti(c) || _n(), t = zc(
          l,
          t,
          u
        );
      } else
        ti(c) ? (t.flags |= 192, t.child = l.child, t = null) : (l = i.treeContext, ol = ht(
          c.nextSibling
        ), pl = t, W = !0, Pt = null, mt = !1, l !== null && Rs(t, l), t = Ec(
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
    ) : (c = Uu(
      c,
      e,
      u,
      null
    ), c.flags |= 2), c.return = t, a.return = t, a.sibling = c, t.child = a, ue(null, a), a = t.child, c = l.child.memoizedState, c === null ? c = bc(u) : (e = c.cachePool, e !== null ? (i = rl._currentValue, e = e.parent !== i ? { parent: i, pool: i } : e) : e = Gs(), c = {
      baseLanes: c.baseLanes | u,
      cachePool: e
    }), a.memoizedState = c, a.childLanes = Tc(
      l,
      f,
      u
    ), t.memoizedState = rc, ue(l.child, a)) : (nu(t), u = l.child, l = u.sibling, u = Ct(u, {
      mode: "visible",
      children: a.children
    }), u.return = t, u.sibling = null, l !== null && (f = t.deletions, f === null ? (t.deletions = [l], t.flags |= 16) : f.push(l)), t.child = u, t.memoizedState = null, u);
  }
  function Ec(l, t) {
    return t = hn(
      { mode: "visible", children: t },
      l.mode
    ), t.return = l, l.child = t;
  }
  function hn(l, t) {
    return l = Pl(22, l, null, t), l.lanes = 0, l;
  }
  function zc(l, t, u) {
    return Yu(t, l.child, null, u), l = Ec(
      t,
      t.pendingProps.children
    ), l.flags |= 2, t.memoizedState = null, l;
  }
  function ko(l, t, u) {
    l.lanes |= t;
    var a = l.alternate;
    a !== null && (a.lanes |= t), Gf(l.return, t, u);
  }
  function Ac(l, t, u, a, e, n) {
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
  function Io(l, t, u) {
    var a = t.pendingProps, e = a.revealOrder, n = a.tail;
    a = a.children;
    var f = Sl.current, c = (f & 2) !== 0;
    if (c ? (f = f & 1 | 2, t.flags |= 128) : f &= 1, p(Sl, f), Ul(l, t, a, u), a = W ? xa : 0, !c && l !== null && (l.flags & 128) !== 0)
      l: for (l = t.child; l !== null; ) {
        if (l.tag === 13)
          l.memoizedState !== null && ko(l, u, t);
        else if (l.tag === 19)
          ko(l, u, t);
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
          l = u.alternate, l !== null && un(l) === null && (e = u), u = u.sibling;
        u = e, u === null ? (e = t.child, t.child = null) : (e = u.sibling, u.sibling = null), Ac(
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
          if (l = e.alternate, l !== null && un(l) === null) {
            t.child = e;
            break;
          }
          l = e.sibling, e.sibling = u, u = e, e = l;
        }
        Ac(
          t,
          !0,
          u,
          null,
          n,
          a
        );
        break;
      case "together":
        Ac(
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
      throw Error(m(153));
    if (t.child !== null) {
      for (l = t.child, u = Ct(l, l.pendingProps), t.child = u, u.return = t; l.sibling !== null; )
        l = l.sibling, u = u.sibling = Ct(l, l.pendingProps), u.return = t;
      u.sibling = null;
    }
    return t.child;
  }
  function _c(l, t) {
    return (l.lanes & t) !== 0 ? !0 : (l = l.dependencies, !!(l !== null && We(l)));
  }
  function Mm(l, t, u) {
    switch (t.tag) {
      case 3:
        ql(t, t.stateNode.containerInfo), tu(t, rl, l.memoizedState.cache), Nu();
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
          return t.flags |= 128, $f(t), null;
        break;
      case 13:
        var a = t.memoizedState;
        if (a !== null)
          return a.dehydrated !== null ? (nu(t), t.flags |= 128, null) : (u & t.child.childLanes) !== 0 ? Fo(l, t, u) : (nu(t), l = Xt(
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
            return Io(
              l,
              t,
              u
            );
          t.flags |= 128;
        }
        if (e = t.memoizedState, e !== null && (e.rendering = null, e.tail = null, e.lastEffect = null), p(Sl, Sl.current), a) break;
        return null;
      case 22:
        return t.lanes = 0, Vo(
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
  function Po(l, t, u) {
    if (l !== null)
      if (l.memoizedProps !== t.pendingProps)
        Tl = !0;
      else {
        if (!_c(l, u) && (t.flags & 128) === 0)
          return Tl = !1, Mm(
            l,
            t,
            u
          );
        Tl = (l.flags & 131072) !== 0;
      }
    else
      Tl = !1, W && (t.flags & 1048576) !== 0 && Ns(t, xa, t.index);
    switch (t.lanes = 0, t.tag) {
      case 16:
        l: {
          var a = t.pendingProps;
          if (l = qu(t.elementType), t.type = l, typeof l == "function")
            Uf(l) ? (a = ju(l, a), t.tag = 1, t = Wo(
              null,
              t,
              l,
              a,
              u
            )) : (t.tag = 0, t = gc(
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
                t.tag = 11, t = Zo(
                  null,
                  t,
                  l,
                  a,
                  u
                );
                break l;
              } else if (e === w) {
                t.tag = 14, t = Lo(
                  null,
                  t,
                  l,
                  a,
                  u
                );
                break l;
              }
            }
            throw t = Ut(l) || l, Error(m(306, t, ""));
          }
        }
        return t;
      case 0:
        return gc(
          l,
          t,
          t.type,
          t.pendingProps,
          u
        );
      case 1:
        return a = t.type, e = ju(
          a,
          t.pendingProps
        ), Wo(
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
          ), l === null) throw Error(m(387));
          a = t.pendingProps;
          var n = t.memoizedState;
          e = n.element, Vf(l, t), ka(t, a, null, u);
          var f = t.memoizedState;
          if (a = f.cache, tu(t, rl, a), a !== n.cache && jf(
            t,
            [rl],
            u,
            !0
          ), Fa(), a = f.element, n.isDehydrated)
            if (n = {
              element: a,
              isDehydrated: !1,
              cache: f.cache
            }, t.updateQueue.baseState = n, t.memoizedState = n, t.flags & 256) {
              t = $o(
                l,
                t,
                a,
                u
              );
              break l;
            } else if (a !== e) {
              e = ot(
                Error(m(424)),
                t
              ), Va(e), t = $o(
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
              for (ol = ht(l.firstChild), pl = t, W = !0, Pt = null, mt = !0, u = xs(
                t,
                null,
                a,
                u
              ), t.child = u; u; )
                u.flags = u.flags & -3 | 4096, u = u.sibling;
            }
          else {
            if (Nu(), a === e) {
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
        return dn(l, t), l === null ? (u = oy(
          t.type,
          null,
          t.pendingProps,
          null
        )) ? t.memoizedState = u : W || (u = t.type, l = t.pendingProps, a = Rn(
          Z.current
        ).createElement(u), a[Ml] = t, a[Ql] = l, Nl(a, u, l), _l(a), t.stateNode = a) : t.memoizedState = oy(
          t.type,
          l.memoizedProps,
          t.pendingProps,
          l.memoizedState
        ), null;
      case 27:
        return Da(t), l === null && W && (a = t.stateNode = cy(
          t.type,
          t.pendingProps,
          Z.current
        ), pl = t, mt = !0, e = ol, du(t.type) ? (ai = e, ol = ht(a.firstChild)) : ol = e), Ul(
          l,
          t,
          t.pendingProps.children,
          u
        ), dn(l, t), l === null && (t.flags |= 4194304), t.child;
      case 5:
        return l === null && W && ((e = a = ol) && (a = ud(
          a,
          t.type,
          t.pendingProps,
          mt
        ), a !== null ? (t.stateNode = a, pl = t, ol = ht(a.firstChild), mt = !1, e = !0) : e = !1), e || lu(t)), Da(t), e = t.type, n = t.pendingProps, f = l !== null ? l.memoizedProps : null, a = n.children, Ic(e, n) ? a = null : f !== null && Ic(e, f) && (t.flags |= 32), t.memoizedState !== null && (e = kf(
          l,
          t,
          gm,
          null,
          null,
          u
        ), ge._currentValue = e), dn(l, t), Ul(l, t, a, u), t.child;
      case 6:
        return l === null && W && ((l = u = ol) && (u = ad(
          u,
          t.pendingProps,
          mt
        ), u !== null ? (t.stateNode = u, pl = t, ol = null, l = !0) : l = !1), l || lu(t)), null;
      case 13:
        return Fo(l, t, u);
      case 4:
        return ql(
          t,
          t.stateNode.containerInfo
        ), a = t.pendingProps, l === null ? t.child = Yu(
          t,
          null,
          a,
          u
        ) : Ul(l, t, a, u), t.child;
      case 11:
        return Zo(
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
        return e = t.type._context, a = t.pendingProps.children, Hu(t), e = Dl(e), a = a(e), t.flags |= 1, Ul(l, t, a, u), t.child;
      case 14:
        return Lo(
          l,
          t,
          t.type,
          t.pendingProps,
          u
        );
      case 15:
        return xo(
          l,
          t,
          t.type,
          t.pendingProps,
          u
        );
      case 19:
        return Io(l, t, u);
      case 31:
        return Om(l, t, u);
      case 22:
        return Vo(
          l,
          t,
          u,
          t.pendingProps
        );
      case 24:
        return Hu(t), a = Dl(rl), l === null ? (e = Zf(), e === null && (e = sl, n = Xf(), e.pooledCache = n, n.refCount++, n !== null && (e.pooledCacheLanes |= u), e = n), t.memoizedState = { parent: a, cache: e }, xf(t), tu(t, rl, e)) : ((l.lanes & u) !== 0 && (Vf(l, t), ka(t, null, null, u), Fa()), e = l.memoizedState, n = t.memoizedState, e.parent !== a ? (e = { parent: a, cache: a }, t.memoizedState = e, t.lanes === 0 && (t.memoizedState = t.updateQueue.baseState = e), tu(t, rl, a)) : (a = n.cache, tu(t, rl, a), a !== e.cache && jf(
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
    throw Error(m(156, t.tag));
  }
  function Qt(l) {
    l.flags |= 4;
  }
  function Oc(l, t, u, a, e) {
    if ((t = (l.mode & 32) !== 0) && (t = !1), t) {
      if (l.flags |= 16777216, (e & 335544128) === e)
        if (l.stateNode.complete) l.flags |= 8192;
        else if (M0()) l.flags |= 8192;
        else
          throw Bu = Ie, Lf;
    } else l.flags &= -16777217;
  }
  function l0(l, t) {
    if (t.type !== "stylesheet" || (t.state.loading & 4) !== 0)
      l.flags &= -16777217;
    else if (l.flags |= 16777216, !hy(t))
      if (M0()) l.flags |= 8192;
      else
        throw Bu = Ie, Lf;
  }
  function Sn(l, t) {
    t !== null && (l.flags |= 4), l.flags & 16384 && (t = l.tag !== 22 ? Hi() : 536870912, l.lanes |= t, ra |= t);
  }
  function ae(l, t) {
    if (!W)
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
  function pm(l, t, u) {
    var a = t.pendingProps;
    switch (Cf(t), t.tag) {
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
        return u = t.stateNode, a = null, l !== null && (a = l.memoizedState.cache), t.memoizedState.cache !== a && (t.flags |= 2048), Yt(rl), hl(), u.pendingContext && (u.context = u.pendingContext, u.pendingContext = null), (l === null || l.child === null) && (na(t) ? Qt(t) : l === null || l.memoizedState.isDehydrated && (t.flags & 256) === 0 || (t.flags |= 1024, Bf())), yl(t), null;
      case 26:
        var e = t.type, n = t.memoizedState;
        return l === null ? (Qt(t), n !== null ? (yl(t), l0(t, n)) : (yl(t), Oc(
          t,
          e,
          null,
          a,
          u
        ))) : n ? n !== l.memoizedState ? (Qt(t), yl(t), l0(t, n)) : (yl(t), t.flags &= -16777217) : (l = l.memoizedProps, l !== a && Qt(t), yl(t), Oc(
          t,
          e,
          l,
          a,
          u
        )), null;
      case 27:
        if (Me(t), u = Z.current, e = t.type, l !== null && t.stateNode != null)
          l.memoizedProps !== a && Qt(t);
        else {
          if (!a) {
            if (t.stateNode === null)
              throw Error(m(166));
            return yl(t), null;
          }
          l = U.current, na(t) ? Hs(t) : (l = cy(e, a, u), t.stateNode = l, Qt(t));
        }
        return yl(t), null;
      case 5:
        if (Me(t), e = t.type, l !== null && t.stateNode != null)
          l.memoizedProps !== a && Qt(t);
        else {
          if (!a) {
            if (t.stateNode === null)
              throw Error(m(166));
            return yl(t), null;
          }
          if (n = U.current, na(t))
            Hs(t);
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
            n[Ml] = t, n[Ql] = a;
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
            l: switch (Nl(n, e, a), e) {
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
        return yl(t), Oc(
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
            throw Error(m(166));
          if (l = Z.current, na(t)) {
            if (l = t.stateNode, u = t.memoizedProps, a = null, e = pl, e !== null)
              switch (e.tag) {
                case 27:
                case 5:
                  a = e.memoizedProps;
              }
            l[Ml] = t, l = !!(l.nodeValue === u || a !== null && a.suppressHydrationWarning === !0 || F0(l.nodeValue, u)), l || lu(t, !0);
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
              if (!a) throw Error(m(318));
              if (l = t.memoizedState, l = l !== null ? l.dehydrated : null, !l) throw Error(m(557));
              l[Ml] = t;
            } else
              Nu(), (t.flags & 128) === 0 && (t.memoizedState = null), t.flags |= 4;
            yl(t), l = !1;
          } else
            u = Bf(), l !== null && l.memoizedState !== null && (l.memoizedState.hydrationErrors = u), l = !0;
          if (!l)
            return t.flags & 256 ? (tt(t), t) : (tt(t), null);
          if ((t.flags & 128) !== 0)
            throw Error(m(558));
        }
        return yl(t), null;
      case 13:
        if (a = t.memoizedState, l === null || l.memoizedState !== null && l.memoizedState.dehydrated !== null) {
          if (e = na(t), a !== null && a.dehydrated !== null) {
            if (l === null) {
              if (!e) throw Error(m(318));
              if (e = t.memoizedState, e = e !== null ? e.dehydrated : null, !e) throw Error(m(317));
              e[Ml] = t;
            } else
              Nu(), (t.flags & 128) === 0 && (t.memoizedState = null), t.flags |= 4;
            yl(t), e = !1;
          } else
            e = Bf(), l !== null && l.memoizedState !== null && (l.memoizedState.hydrationErrors = e), e = !0;
          if (!e)
            return t.flags & 256 ? (tt(t), t) : (tt(t), null);
        }
        return tt(t), (t.flags & 128) !== 0 ? (t.lanes = u, t) : (u = a !== null, l = l !== null && l.memoizedState !== null, u && (a = t.child, e = null, a.alternate !== null && a.alternate.memoizedState !== null && a.alternate.memoizedState.cachePool !== null && (e = a.alternate.memoizedState.cachePool.pool), n = null, a.memoizedState !== null && a.memoizedState.cachePool !== null && (n = a.memoizedState.cachePool.pool), n !== e && (a.flags |= 2048)), u !== l && u && (t.child.flags |= 8192), Sn(t, t.updateQueue), yl(t), null);
      case 4:
        return hl(), l === null && wc(t.stateNode.containerInfo), yl(t), null;
      case 10:
        return Yt(t.type), yl(t), null;
      case 19:
        if (z(Sl), a = t.memoizedState, a === null) return yl(t), null;
        if (e = (t.flags & 128) !== 0, n = a.rendering, n === null)
          if (e) ae(a, !1);
          else {
            if (dl !== 0 || l !== null && (l.flags & 128) !== 0)
              for (l = t.child; l !== null; ) {
                if (n = un(l), n !== null) {
                  for (t.flags |= 128, ae(a, !1), l = n.updateQueue, t.updateQueue = l, Sn(t, l), t.subtreeFlags = 0, l = u, u = t.child; u !== null; )
                    ps(u, l), u = u.sibling;
                  return p(
                    Sl,
                    Sl.current & 1 | 2
                  ), W && qt(t, a.treeForkCount), t.child;
                }
                l = l.sibling;
              }
            a.tail !== null && $l() > En && (t.flags |= 128, e = !0, ae(a, !1), t.lanes = 4194304);
          }
        else {
          if (!e)
            if (l = un(n), l !== null) {
              if (t.flags |= 128, e = !0, l = l.updateQueue, t.updateQueue = l, Sn(t, l), ae(a, !0), a.tail === null && a.tailMode === "hidden" && !n.alternate && !W)
                return yl(t), null;
            } else
              2 * $l() - a.renderingStartTime > En && u !== 536870912 && (t.flags |= 128, e = !0, ae(a, !1), t.lanes = 4194304);
          a.isBackwards ? (n.sibling = t.child, t.child = n) : (l = a.last, l !== null ? l.sibling = n : t.child = n, a.last = n);
        }
        return a.tail !== null ? (l = a.tail, a.rendering = l, a.tail = l.sibling, a.renderingStartTime = $l(), l.sibling = null, u = Sl.current, p(
          Sl,
          e ? u & 1 | 2 : u & 1
        ), W && qt(t, a.treeForkCount), l) : (yl(t), null);
      case 22:
      case 23:
        return tt(t), Wf(), a = t.memoizedState !== null, l !== null ? l.memoizedState !== null !== a && (t.flags |= 8192) : a && (t.flags |= 8192), a ? (u & 536870912) !== 0 && (t.flags & 128) === 0 && (yl(t), t.subtreeFlags & 6 && (t.flags |= 8192)) : yl(t), u = t.updateQueue, u !== null && Sn(t, u.retryQueue), u = null, l !== null && l.memoizedState !== null && l.memoizedState.cachePool !== null && (u = l.memoizedState.cachePool.pool), a = null, t.memoizedState !== null && t.memoizedState.cachePool !== null && (a = t.memoizedState.cachePool.pool), a !== u && (t.flags |= 2048), l !== null && z(Cu), null;
      case 24:
        return u = null, l !== null && (u = l.memoizedState.cache), t.memoizedState.cache !== u && (t.flags |= 2048), Yt(rl), yl(t), null;
      case 25:
        return null;
      case 30:
        return null;
    }
    throw Error(m(156, t.tag));
  }
  function Dm(l, t) {
    switch (Cf(t), t.tag) {
      case 1:
        return l = t.flags, l & 65536 ? (t.flags = l & -65537 | 128, t) : null;
      case 3:
        return Yt(rl), hl(), l = t.flags, (l & 65536) !== 0 && (l & 128) === 0 ? (t.flags = l & -65537 | 128, t) : null;
      case 26:
      case 27:
      case 5:
        return Me(t), null;
      case 31:
        if (t.memoizedState !== null) {
          if (tt(t), t.alternate === null)
            throw Error(m(340));
          Nu();
        }
        return l = t.flags, l & 65536 ? (t.flags = l & -65537 | 128, t) : null;
      case 13:
        if (tt(t), l = t.memoizedState, l !== null && l.dehydrated !== null) {
          if (t.alternate === null)
            throw Error(m(340));
          Nu();
        }
        return l = t.flags, l & 65536 ? (t.flags = l & -65537 | 128, t) : null;
      case 19:
        return z(Sl), null;
      case 4:
        return hl(), null;
      case 10:
        return Yt(t.type), null;
      case 22:
      case 23:
        return tt(t), Wf(), l !== null && z(Cu), l = t.flags, l & 65536 ? (t.flags = l & -65537 | 128, t) : null;
      case 24:
        return Yt(rl), null;
      case 25:
        return null;
      default:
        return null;
    }
  }
  function t0(l, t) {
    switch (Cf(t), t.tag) {
      case 3:
        Yt(rl), hl();
        break;
      case 26:
      case 27:
      case 5:
        Me(t);
        break;
      case 4:
        hl();
        break;
      case 31:
        t.memoizedState !== null && tt(t);
        break;
      case 13:
        tt(t);
        break;
      case 19:
        z(Sl);
        break;
      case 10:
        Yt(t.type);
        break;
      case 22:
      case 23:
        tt(t), Wf(), l !== null && z(Cu);
        break;
      case 24:
        Yt(rl);
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
      al(t, t.return, c);
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
                al(
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
      al(t, t.return, r);
    }
  }
  function u0(l) {
    var t = l.updateQueue;
    if (t !== null) {
      var u = l.stateNode;
      try {
        Ks(t, u);
      } catch (a) {
        al(l, l.return, a);
      }
    }
  }
  function a0(l, t, u) {
    u.props = ju(
      l.type,
      l.memoizedProps
    ), u.state = l.memoizedState;
    try {
      u.componentWillUnmount();
    } catch (a) {
      al(l, t, a);
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
      al(l, t, e);
    }
  }
  function Mt(l, t) {
    var u = l.ref, a = l.refCleanup;
    if (u !== null)
      if (typeof a == "function")
        try {
          a();
        } catch (e) {
          al(l, t, e);
        } finally {
          l.refCleanup = null, l = l.alternate, l != null && (l.refCleanup = null);
        }
      else if (typeof u == "function")
        try {
          u(null);
        } catch (e) {
          al(l, t, e);
        }
      else u.current = null;
  }
  function e0(l) {
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
      al(l, l.return, e);
    }
  }
  function Mc(l, t, u) {
    try {
      var a = l.stateNode;
      Fm(a, l.type, u, t), a[Ql] = t;
    } catch (e) {
      al(l, l.return, e);
    }
  }
  function n0(l) {
    return l.tag === 5 || l.tag === 3 || l.tag === 26 || l.tag === 27 && du(l.type) || l.tag === 4;
  }
  function pc(l) {
    l: for (; ; ) {
      for (; l.sibling === null; ) {
        if (l.return === null || n0(l.return)) return null;
        l = l.return;
      }
      for (l.sibling.return = l.return, l = l.sibling; l.tag !== 5 && l.tag !== 6 && l.tag !== 18; ) {
        if (l.tag === 27 && du(l.type) || l.flags & 2 || l.child === null || l.tag === 4) continue l;
        l.child.return = l, l = l.child;
      }
      if (!(l.flags & 2)) return l.stateNode;
    }
  }
  function Dc(l, t, u) {
    var a = l.tag;
    if (a === 5 || a === 6)
      l = l.stateNode, t ? (u.nodeType === 9 ? u.body : u.nodeName === "HTML" ? u.ownerDocument.body : u).insertBefore(l, t) : (t = u.nodeType === 9 ? u.body : u.nodeName === "HTML" ? u.ownerDocument.body : u, t.appendChild(l), u = u._reactRootContainer, u != null || t.onclick !== null || (t.onclick = Rt));
    else if (a !== 4 && (a === 27 && du(l.type) && (u = l.stateNode, t = null), l = l.child, l !== null))
      for (Dc(l, t, u), l = l.sibling; l !== null; )
        Dc(l, t, u), l = l.sibling;
  }
  function gn(l, t, u) {
    var a = l.tag;
    if (a === 5 || a === 6)
      l = l.stateNode, t ? u.insertBefore(l, t) : u.appendChild(l);
    else if (a !== 4 && (a === 27 && du(l.type) && (u = l.stateNode), l = l.child, l !== null))
      for (gn(l, t, u), l = l.sibling; l !== null; )
        gn(l, t, u), l = l.sibling;
  }
  function f0(l) {
    var t = l.stateNode, u = l.memoizedProps;
    try {
      for (var a = l.type, e = t.attributes; e.length; )
        t.removeAttributeNode(e[0]);
      Nl(t, a, u), t[Ml] = l, t[Ql] = u;
    } catch (n) {
      al(l, l.return, n);
    }
  }
  var Zt = !1, El = !1, Uc = !1, c0 = typeof WeakSet == "function" ? WeakSet : Set, Ol = null;
  function Um(l, t) {
    if (l = l.containerInfo, Fc = jn, l = rs(l), zf(l)) {
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
    for (kc = { focusedElem: l, selectionRange: u }, jn = !1, Ol = t; Ol !== null; )
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
                  var D = ju(
                    u.type,
                    e
                  );
                  l = a.getSnapshotBeforeUpdate(
                    D,
                    n
                  ), a.__reactInternalSnapshotBeforeUpdate = l;
                } catch (C) {
                  al(
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
                  li(l);
                else if (u === 1)
                  switch (l.nodeName) {
                    case "HEAD":
                    case "HTML":
                    case "BODY":
                      li(l);
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
            l.return = t.return, Ol = l;
            break;
          }
          Ol = t.return;
        }
  }
  function i0(l, t, u) {
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
              al(u, u.return, f);
            }
          else {
            var e = ju(
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
              al(
                u,
                u.return,
                f
              );
            }
          }
        a & 64 && u0(u), a & 512 && ne(u, u.return);
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
            Ks(l, t);
          } catch (f) {
            al(u, u.return, f);
          }
        }
        break;
      case 27:
        t === null && a & 4 && f0(u);
      case 26:
      case 5:
        xt(l, u), t === null && a & 4 && e0(u), a & 512 && ne(u, u.return);
        break;
      case 12:
        xt(l, u);
        break;
      case 31:
        xt(l, u), a & 4 && y0(l, u);
        break;
      case 13:
        xt(l, u), a & 4 && v0(l, u), a & 64 && (l = u.memoizedState, l !== null && (l = l.dehydrated, l !== null && (u = jm.bind(
          null,
          u
        ), ed(l, u))));
        break;
      case 22:
        if (a = u.memoizedState !== null || Zt, !a) {
          t = t !== null && t.memoizedState !== null || El, e = Zt;
          var n = El;
          Zt = a, (El = t) && !n ? Vt(
            l,
            u,
            (u.subtreeFlags & 8772) !== 0
          ) : xt(l, u), Zt = e, El = n;
        }
        break;
      case 30:
        break;
      default:
        xt(l, u);
    }
  }
  function s0(l) {
    var t = l.alternate;
    t !== null && (l.alternate = null, s0(t)), l.child = null, l.deletions = null, l.sibling = null, l.tag === 5 && (t = l.stateNode, t !== null && ef(t)), l.stateNode = null, l.return = null, l.dependencies = null, l.memoizedProps = null, l.memoizedState = null, l.pendingProps = null, l.stateNode = null, l.updateQueue = null;
  }
  var vl = null, Ll = !1;
  function Lt(l, t, u) {
    for (u = u.child; u !== null; )
      o0(l, t, u), u = u.sibling;
  }
  function o0(l, t, u) {
    if (Fl && typeof Fl.onCommitFiberUnmount == "function")
      try {
        Fl.onCommitFiberUnmount(Ua, u);
      } catch {
      }
    switch (u.tag) {
      case 26:
        El || Mt(u, t), Lt(
          l,
          t,
          u
        ), u.memoizedState ? u.memoizedState.count-- : u.stateNode && (u = u.stateNode, u.parentNode.removeChild(u));
        break;
      case 27:
        El || Mt(u, t);
        var a = vl, e = Ll;
        du(u.type) && (vl = u.stateNode, Ll = !1), Lt(
          l,
          t,
          u
        ), de(u.stateNode), vl = a, Ll = e;
        break;
      case 5:
        El || Mt(u, t);
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
              al(
                u,
                t,
                n
              );
            }
          else
            try {
              vl.removeChild(u.stateNode);
            } catch (n) {
              al(
                u,
                t,
                n
              );
            }
        break;
      case 18:
        vl !== null && (Ll ? (l = vl, uy(
          l.nodeType === 9 ? l.body : l.nodeName === "HTML" ? l.ownerDocument.body : l,
          u.stateNode
        ), Ma(l)) : uy(vl, u.stateNode));
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
        cu(2, u, t), El || cu(4, u, t), Lt(
          l,
          t,
          u
        );
        break;
      case 1:
        El || (Mt(u, t), a = u.stateNode, typeof a.componentWillUnmount == "function" && a0(
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
        El = (a = El) || u.memoizedState !== null, Lt(
          l,
          t,
          u
        ), El = a;
        break;
      default:
        Lt(
          l,
          t,
          u
        );
    }
  }
  function y0(l, t) {
    if (t.memoizedState === null && (l = t.alternate, l !== null && (l = l.memoizedState, l !== null))) {
      l = l.dehydrated;
      try {
        Ma(l);
      } catch (u) {
        al(t, t.return, u);
      }
    }
  }
  function v0(l, t) {
    if (t.memoizedState === null && (l = t.alternate, l !== null && (l = l.memoizedState, l !== null && (l = l.dehydrated, l !== null))))
      try {
        Ma(l);
      } catch (u) {
        al(t, t.return, u);
      }
  }
  function Nm(l) {
    switch (l.tag) {
      case 31:
      case 13:
      case 19:
        var t = l.stateNode;
        return t === null && (t = l.stateNode = new c0()), t;
      case 22:
        return l = l.stateNode, t = l._retryCache, t === null && (t = l._retryCache = new c0()), t;
      default:
        throw Error(m(435, l.tag));
    }
  }
  function rn(l, t) {
    var u = Nm(l);
    t.forEach(function(a) {
      if (!u.has(a)) {
        u.add(a);
        var e = Xm.bind(null, l, a);
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
        if (vl === null) throw Error(m(160));
        o0(n, f, e), vl = null, Ll = !1, n = e.alternate, n !== null && (n.return = null), e.return = null;
      }
    if (t.subtreeFlags & 13886)
      for (t = t.child; t !== null; )
        m0(t, l), t = t.sibling;
  }
  var bt = null;
  function m0(l, t) {
    var u = l.alternate, a = l.flags;
    switch (l.tag) {
      case 0:
      case 11:
      case 14:
      case 15:
        xl(t, l), Vl(l), a & 4 && (cu(3, l, l.return), ee(3, l), cu(5, l, l.return));
        break;
      case 1:
        xl(t, l), Vl(l), a & 512 && (El || u === null || Mt(u, u.return)), a & 64 && Zt && (l = l.updateQueue, l !== null && (a = l.callbacks, a !== null && (u = l.shared.hiddenCallbacks, l.shared.hiddenCallbacks = u === null ? a : u.concat(a))));
        break;
      case 26:
        var e = bt;
        if (xl(t, l), Vl(l), a & 512 && (El || u === null || Mt(u, u.return)), a & 4) {
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
                      )), Nl(n, a, u), n[Ml] = l, _l(n), a = n;
                      break l;
                    case "link":
                      var f = my(
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
                      n = e.createElement(a), Nl(n, a, u), e.head.appendChild(n);
                      break;
                    case "meta":
                      if (f = my(
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
                      n = e.createElement(a), Nl(n, a, u), e.head.appendChild(n);
                      break;
                    default:
                      throw Error(m(468, a));
                  }
                  n[Ml] = l, _l(n), a = n;
                }
                l.stateNode = a;
              } else
                dy(
                  e,
                  l.type,
                  l.stateNode
                );
            else
              l.stateNode = vy(
                e,
                a,
                l.memoizedProps
              );
          else
            n !== a ? (n === null ? u.stateNode !== null && (u = u.stateNode, u.parentNode.removeChild(u)) : n.count--, a === null ? dy(
              e,
              l.type,
              l.stateNode
            ) : vy(
              e,
              a,
              l.memoizedProps
            )) : a === null && l.stateNode !== null && Mc(
              l,
              l.memoizedProps,
              u.memoizedProps
            );
        }
        break;
      case 27:
        xl(t, l), Vl(l), a & 512 && (El || u === null || Mt(u, u.return)), u !== null && a & 4 && Mc(
          l,
          l.memoizedProps,
          u.memoizedProps
        );
        break;
      case 5:
        if (xl(t, l), Vl(l), a & 512 && (El || u === null || Mt(u, u.return)), l.flags & 32) {
          e = l.stateNode;
          try {
            $u(e, "");
          } catch (D) {
            al(l, l.return, D);
          }
        }
        a & 4 && l.stateNode != null && (e = l.memoizedProps, Mc(
          l,
          e,
          u !== null ? u.memoizedProps : e
        )), a & 1024 && (Uc = !0);
        break;
      case 6:
        if (xl(t, l), Vl(l), a & 4) {
          if (l.stateNode === null)
            throw Error(m(162));
          a = l.memoizedProps, u = l.stateNode;
          try {
            u.nodeValue = a;
          } catch (D) {
            al(l, l.return, D);
          }
        }
        break;
      case 3:
        if (qn = null, e = bt, bt = Hn(t.containerInfo), xl(t, l), bt = e, Vl(l), a & 4 && u !== null && u.memoizedState.isDehydrated)
          try {
            Ma(t.containerInfo);
          } catch (D) {
            al(l, l.return, D);
          }
        Uc && (Uc = !1, d0(l));
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
        xl(t, l), Vl(l), a & 4 && (a = l.updateQueue, a !== null && (l.updateQueue = null, rn(l, a)));
        break;
      case 13:
        xl(t, l), Vl(l), l.child.flags & 8192 && l.memoizedState !== null != (u !== null && u.memoizedState !== null) && (Tn = $l()), a & 4 && (a = l.updateQueue, a !== null && (l.updateQueue = null, rn(l, a)));
        break;
      case 22:
        e = l.memoizedState !== null;
        var i = u !== null && u.memoizedState !== null, h = Zt, r = El;
        if (Zt = h || e, El = r || i, xl(t, l), El = r, Zt = h, Vl(l), a & 8192)
          l: for (t = l.stateNode, t._visibility = e ? t._visibility & -2 : t._visibility | 1, e && (u === null || i || Zt || El || Xu(l)), u = null, t = l; ; ) {
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
                  al(i, i.return, D);
                }
              }
            } else if (t.tag === 6) {
              if (u === null) {
                i = t;
                try {
                  i.stateNode.nodeValue = e ? "" : i.memoizedProps;
                } catch (D) {
                  al(i, i.return, D);
                }
              }
            } else if (t.tag === 18) {
              if (u === null) {
                i = t;
                try {
                  var g = i.stateNode;
                  e ? ay(g, !0) : ay(i.stateNode, !1);
                } catch (D) {
                  al(i, i.return, D);
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
        a & 4 && (a = l.updateQueue, a !== null && (u = a.retryQueue, u !== null && (a.retryQueue = null, rn(l, u))));
        break;
      case 19:
        xl(t, l), Vl(l), a & 4 && (a = l.updateQueue, a !== null && (l.updateQueue = null, rn(l, a)));
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
          if (n0(a)) {
            u = a;
            break;
          }
          a = a.return;
        }
        if (u == null) throw Error(m(160));
        switch (u.tag) {
          case 27:
            var e = u.stateNode, n = pc(l);
            gn(l, n, e);
            break;
          case 5:
            var f = u.stateNode;
            u.flags & 32 && ($u(f, ""), u.flags &= -33);
            var c = pc(l);
            gn(l, c, f);
            break;
          case 3:
          case 4:
            var i = u.stateNode.containerInfo, h = pc(l);
            Dc(
              l,
              h,
              i
            );
            break;
          default:
            throw Error(m(161));
        }
      } catch (r) {
        al(l, l.return, r);
      }
      l.flags &= -3;
    }
    t & 4096 && (l.flags &= -4097);
  }
  function d0(l) {
    if (l.subtreeFlags & 1024)
      for (l = l.child; l !== null; ) {
        var t = l;
        d0(t), t.tag === 5 && t.flags & 1024 && t.stateNode.reset(), l = l.sibling;
      }
  }
  function xt(l, t) {
    if (t.subtreeFlags & 8772)
      for (t = t.child; t !== null; )
        i0(l, t.alternate, t), t = t.sibling;
  }
  function Xu(l) {
    for (l = l.child; l !== null; ) {
      var t = l;
      switch (t.tag) {
        case 0:
        case 11:
        case 14:
        case 15:
          cu(4, t, t.return), Xu(t);
          break;
        case 1:
          Mt(t, t.return);
          var u = t.stateNode;
          typeof u.componentWillUnmount == "function" && a0(
            t,
            t.return,
            u
          ), Xu(t);
          break;
        case 27:
          de(t.stateNode);
        case 26:
        case 5:
          Mt(t, t.return), Xu(t);
          break;
        case 22:
          t.memoizedState === null && Xu(t);
          break;
        case 30:
          Xu(t);
          break;
        default:
          Xu(t);
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
              al(a, a.return, h);
            }
          if (a = n, e = a.updateQueue, e !== null) {
            var c = a.stateNode;
            try {
              var i = e.shared.hiddenCallbacks;
              if (i !== null)
                for (e.shared.hiddenCallbacks = null, e = 0; e < i.length; e++)
                  Vs(i[e], c);
            } catch (h) {
              al(a, a.return, h);
            }
          }
          u && f & 64 && u0(n), ne(n, n.return);
          break;
        case 27:
          f0(n);
        case 26:
        case 5:
          Vt(
            e,
            n,
            u
          ), u && a === null && f & 4 && e0(n), ne(n, n.return);
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
          ), u && f & 4 && y0(e, n);
          break;
        case 13:
          Vt(
            e,
            n,
            u
          ), u && f & 4 && v0(e, n);
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
  function Nc(l, t) {
    var u = null;
    l !== null && l.memoizedState !== null && l.memoizedState.cachePool !== null && (u = l.memoizedState.cachePool.pool), l = null, t.memoizedState !== null && t.memoizedState.cachePool !== null && (l = t.memoizedState.cachePool.pool), l !== u && (l != null && l.refCount++, u != null && Ka(u));
  }
  function Rc(l, t) {
    l = null, t.alternate !== null && (l = t.alternate.memoizedState.cache), t = t.memoizedState.cache, t !== l && (t.refCount++, l != null && Ka(l));
  }
  function Tt(l, t, u, a) {
    if (t.subtreeFlags & 10256)
      for (t = t.child; t !== null; )
        h0(
          l,
          t,
          u,
          a
        ), t = t.sibling;
  }
  function h0(l, t, u, a) {
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
            al(t, t.return, i);
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
        )), e & 2048 && Nc(f, t);
        break;
      case 24:
        Tt(
          l,
          t,
          u,
          a
        ), e & 2048 && Rc(t.alternate, t);
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
          )), e && h & 2048 && Nc(
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
          ), e && h & 2048 && Rc(f.alternate, f);
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
            fe(u, a), e & 2048 && Nc(
              a.alternate,
              a
            );
            break;
          case 24:
            fe(u, a), e & 2048 && Rc(a.alternate, a);
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
        S0(
          l,
          t,
          u
        ), l = l.sibling;
  }
  function S0(l, t, u) {
    switch (l.tag) {
      case 26:
        Sa(
          l,
          t,
          u
        ), l.flags & ce && l.memoizedState !== null && Sd(
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
  function g0(l) {
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
          Ol = a, b0(
            a,
            l
          );
        }
      g0(l);
    }
    if (l.subtreeFlags & 10256)
      for (l = l.child; l !== null; )
        r0(l), l = l.sibling;
  }
  function r0(l) {
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
        l.memoizedState !== null && t._visibility & 2 && (l.return === null || l.return.tag !== 13) ? (t._visibility &= -3, bn(l)) : ie(l);
        break;
      default:
        ie(l);
    }
  }
  function bn(l) {
    var t = l.deletions;
    if ((l.flags & 16) !== 0) {
      if (t !== null)
        for (var u = 0; u < t.length; u++) {
          var a = t[u];
          Ol = a, b0(
            a,
            l
          );
        }
      g0(l);
    }
    for (l = l.child; l !== null; ) {
      switch (t = l, t.tag) {
        case 0:
        case 11:
        case 15:
          cu(8, t, t.return), bn(t);
          break;
        case 22:
          u = t.stateNode, u._visibility & 2 && (u._visibility &= -3, bn(t));
          break;
        default:
          bn(t);
      }
      l = l.sibling;
    }
  }
  function b0(l, t) {
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
          if (s0(a), a === u) {
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
  var Rm = {
    getCacheForType: function(l) {
      var t = Dl(rl), u = t.data.get(l);
      return u === void 0 && (u = l(), t.data.set(l, u)), u;
    },
    cacheSignal: function() {
      return Dl(rl).controller.signal;
    }
  }, Hm = typeof WeakMap == "function" ? WeakMap : Map, I = 0, sl = null, L = null, K = 0, ul = 0, ut = null, iu = !1, ga = !1, Hc = !1, Kt = 0, dl = 0, su = 0, Qu = 0, Cc = 0, at = 0, ra = 0, se = null, Kl = null, qc = !1, Tn = 0, T0 = 0, En = 1 / 0, zn = null, ou = null, zl = 0, yu = null, ba = null, Jt = 0, Bc = 0, Yc = null, E0 = null, oe = 0, Gc = null;
  function et() {
    return (I & 2) !== 0 && K !== 0 ? K & -K : b.T !== null ? xc() : Yi();
  }
  function z0() {
    if (at === 0)
      if ((K & 536870912) === 0 || W) {
        var l = Ue;
        Ue <<= 1, (Ue & 3932160) === 0 && (Ue = 262144), at = l;
      } else at = 536870912;
    return l = lt.current, l !== null && (l.flags |= 32), at;
  }
  function Jl(l, t, u) {
    (l === sl && (ul === 2 || ul === 9) || l.cancelPendingCommit !== null) && (Ta(l, 0), vu(
      l,
      K,
      at,
      !1
    )), Ra(l, u), ((I & 2) === 0 || l !== sl) && (l === sl && ((I & 2) === 0 && (Qu |= u), dl === 4 && vu(
      l,
      K,
      at,
      !1
    )), pt(l));
  }
  function A0(l, t, u) {
    if ((I & 6) !== 0) throw Error(m(327));
    var a = !u && (t & 127) === 0 && (t & l.expiredLanes) === 0 || Na(l, t), e = a ? Bm(l, t) : Xc(l, t, !0), n = a;
    do {
      if (e === 0) {
        ga && !a && vu(l, t, 0, !1);
        break;
      } else {
        if (u = l.current.alternate, n && !Cm(u)) {
          e = Xc(l, t, !1), n = !1;
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
              if (i && (Ta(c, f).flags |= 256), f = Xc(
                c,
                f,
                !1
              ), f !== 2) {
                if (Hc && !i) {
                  c.errorRecoveryDisabledLanes |= n, Qu |= n, e = 4;
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
              throw Error(m(345));
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
              throw Error(m(329));
          }
          if ((t & 62914560) === t && (e = Tn + 300 - $l(), 10 < e)) {
            if (vu(
              a,
              t,
              at,
              !iu
            ), Re(a, 0, !0) !== 0) break l;
            Jt = t, a.timeoutHandle = ly(
              _0.bind(
                null,
                a,
                u,
                Kl,
                zn,
                qc,
                t,
                at,
                Qu,
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
          _0(
            a,
            u,
            Kl,
            zn,
            qc,
            t,
            at,
            Qu,
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
  function _0(l, t, u, a, e, n, f, c, i, h, r, E, S, g) {
    if (l.timeoutHandle = -1, E = t.subtreeFlags, E & 8192 || (E & 16785408) === 16785408) {
      E = {
        stylesheets: null,
        count: 0,
        imgCount: 0,
        imgBytes: 0,
        suspenseyImages: [],
        waitingForImages: !0,
        waitingForViewTransition: !1,
        unsuspend: Rt
      }, S0(
        t,
        n,
        E
      );
      var D = (n & 62914560) === n ? Tn - $l() : (n & 4194048) === n ? T0 - $l() : 0;
      if (D = gd(
        E,
        D
      ), D !== null) {
        Jt = n, l.cancelPendingCommit = D(
          H0.bind(
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
    H0(
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
  function Cm(l) {
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
    t &= ~Cc, t &= ~Qu, l.suspendedLanes |= t, l.pingedLanes &= ~t, a && (l.warmLanes |= t), a = l.expirationTimes;
    for (var e = t; 0 < e; ) {
      var n = 31 - kl(e), f = 1 << n;
      a[n] = -1, e &= ~f;
    }
    u !== 0 && Ci(l, u, t);
  }
  function An() {
    return (I & 6) === 0 ? (ye(0), !1) : !0;
  }
  function jc() {
    if (L !== null) {
      if (ul === 0)
        var l = L.return;
      else
        l = L, Bt = Ru = null, lc(l), oa = null, wa = 0, l = L;
      for (; l !== null; )
        t0(l.alternate, l), l = l.return;
      L = null;
    }
  }
  function Ta(l, t) {
    var u = l.timeoutHandle;
    u !== -1 && (l.timeoutHandle = -1, Pm(u)), u = l.cancelPendingCommit, u !== null && (l.cancelPendingCommit = null, u()), Jt = 0, jc(), sl = l, L = u = Ct(l.current, null), K = t, ul = 0, ut = null, iu = !1, ga = Na(l, t), Hc = !1, ra = at = Cc = Qu = su = dl = 0, Kl = se = null, qc = !1, (t & 8) !== 0 && (t |= t & 32);
    var a = l.entangledLanes;
    if (a !== 0)
      for (l = l.entanglements, a &= t; 0 < a; ) {
        var e = 31 - kl(a), n = 1 << e;
        t |= l[e], a &= ~n;
      }
    return Kt = t, xe(), u;
  }
  function O0(l, t) {
    X = null, b.H = te, t === sa || t === ke ? (t = Qs(), ul = 3) : t === Lf ? (t = Qs(), ul = 4) : ul = t === Sc ? 8 : t !== null && typeof t == "object" && typeof t.then == "function" ? 6 : 1, ut = t, L === null && (dl = 1, vn(
      l,
      ot(t, l.current)
    ));
  }
  function M0() {
    var l = lt.current;
    return l === null ? !0 : (K & 4194048) === K ? dt === null : (K & 62914560) === K || (K & 536870912) !== 0 ? l === dt : !1;
  }
  function p0() {
    var l = b.H;
    return b.H = te, l === null ? te : l;
  }
  function D0() {
    var l = b.A;
    return b.A = Rm, l;
  }
  function _n() {
    dl = 4, iu || (K & 4194048) !== K && lt.current !== null || (ga = !0), (su & 134217727) === 0 && (Qu & 134217727) === 0 || sl === null || vu(
      sl,
      K,
      at,
      !1
    );
  }
  function Xc(l, t, u) {
    var a = I;
    I |= 2;
    var e = p0(), n = D0();
    (sl !== l || K !== t) && (zn = null, Ta(l, t)), t = !1;
    var f = dl;
    l: do
      try {
        if (ul !== 0 && L !== null) {
          var c = L, i = ut;
          switch (ul) {
            case 8:
              jc(), f = 6;
              break l;
            case 3:
            case 2:
            case 9:
            case 6:
              lt.current === null && (t = !0);
              var h = ul;
              if (ul = 0, ut = null, Ea(l, c, i, h), u && ga) {
                f = 0;
                break l;
              }
              break;
            default:
              h = ul, ul = 0, ut = null, Ea(l, c, i, h);
          }
        }
        qm(), f = dl;
        break;
      } catch (r) {
        O0(l, r);
      }
    while (!0);
    return t && l.shellSuspendCounter++, Bt = Ru = null, I = a, b.H = e, b.A = n, L === null && (sl = null, K = 0, xe()), f;
  }
  function qm() {
    for (; L !== null; ) U0(L);
  }
  function Bm(l, t) {
    var u = I;
    I |= 2;
    var a = p0(), e = D0();
    sl !== l || K !== t ? (zn = null, En = $l() + 500, Ta(l, t)) : ga = Na(
      l,
      t
    );
    l: do
      try {
        if (ul !== 0 && L !== null) {
          t = L;
          var n = ut;
          t: switch (ul) {
            case 1:
              ul = 0, ut = null, Ea(l, t, n, 1);
              break;
            case 2:
            case 9:
              if (js(n)) {
                ul = 0, ut = null, N0(t);
                break;
              }
              t = function() {
                ul !== 2 && ul !== 9 || sl !== l || (ul = 7), pt(l);
              }, n.then(t, t);
              break l;
            case 3:
              ul = 7;
              break l;
            case 4:
              ul = 5;
              break l;
            case 7:
              js(n) ? (ul = 0, ut = null, N0(t)) : (ul = 0, ut = null, Ea(l, t, n, 7));
              break;
            case 5:
              var f = null;
              switch (L.tag) {
                case 26:
                  f = L.memoizedState;
                case 5:
                case 27:
                  var c = L;
                  if (f ? hy(f) : c.stateNode.complete) {
                    ul = 0, ut = null;
                    var i = c.sibling;
                    if (i !== null) L = i;
                    else {
                      var h = c.return;
                      h !== null ? (L = h, On(h)) : L = null;
                    }
                    break t;
                  }
              }
              ul = 0, ut = null, Ea(l, t, n, 5);
              break;
            case 6:
              ul = 0, ut = null, Ea(l, t, n, 6);
              break;
            case 8:
              jc(), dl = 6;
              break l;
            default:
              throw Error(m(462));
          }
        }
        Ym();
        break;
      } catch (r) {
        O0(l, r);
      }
    while (!0);
    return Bt = Ru = null, b.H = a, b.A = e, I = u, L !== null ? 0 : (sl = null, K = 0, xe(), dl);
  }
  function Ym() {
    for (; L !== null && !nv(); )
      U0(L);
  }
  function U0(l) {
    var t = Po(l.alternate, l, Kt);
    l.memoizedProps = l.pendingProps, t === null ? On(l) : L = t;
  }
  function N0(l) {
    var t = l, u = t.alternate;
    switch (t.tag) {
      case 15:
      case 0:
        t = wo(
          u,
          t,
          t.pendingProps,
          t.type,
          void 0,
          K
        );
        break;
      case 11:
        t = wo(
          u,
          t,
          t.pendingProps,
          t.type.render,
          t.ref,
          K
        );
        break;
      case 5:
        lc(t);
      default:
        t0(u, t), t = L = ps(t, Kt), t = Po(u, t, Kt);
    }
    l.memoizedProps = l.pendingProps, t === null ? On(l) : L = t;
  }
  function Ea(l, t, u, a) {
    Bt = Ru = null, lc(t), oa = null, wa = 0;
    var e = t.return;
    try {
      if (_m(
        l,
        e,
        t,
        u,
        K
      )) {
        dl = 1, vn(
          l,
          ot(u, l.current)
        ), L = null;
        return;
      }
    } catch (n) {
      if (e !== null) throw L = e, n;
      dl = 1, vn(
        l,
        ot(u, l.current)
      ), L = null;
      return;
    }
    t.flags & 32768 ? (W || a === 1 ? l = !0 : ga || (K & 536870912) !== 0 ? l = !1 : (iu = l = !0, (a === 2 || a === 9 || a === 3 || a === 6) && (a = lt.current, a !== null && a.tag === 13 && (a.flags |= 16384))), R0(t, l)) : On(t);
  }
  function On(l) {
    var t = l;
    do {
      if ((t.flags & 32768) !== 0) {
        R0(
          t,
          iu
        );
        return;
      }
      l = t.return;
      var u = pm(
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
  function R0(l, t) {
    do {
      var u = Dm(l.alternate, l);
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
  function H0(l, t, u, a, e, n, f, c, i) {
    l.cancelPendingCommit = null;
    do
      Mn();
    while (zl !== 0);
    if ((I & 6) !== 0) throw Error(m(327));
    if (t !== null) {
      if (t === l.current) throw Error(m(177));
      if (n = t.lanes | t.childLanes, n |= pf, hv(
        l,
        u,
        n,
        f,
        c,
        i
      ), l === sl && (L = sl = null, K = 0), ba = t, yu = l, Jt = u, Bc = n, Yc = e, E0 = a, (t.subtreeFlags & 10256) !== 0 || (t.flags & 10256) !== 0 ? (l.callbackNode = null, l.callbackPriority = 0, Qm(pe, function() {
        return G0(), null;
      })) : (l.callbackNode = null, l.callbackPriority = 0), a = (t.flags & 13878) !== 0, (t.subtreeFlags & 13878) !== 0 || a) {
        a = b.T, b.T = null, e = M.p, M.p = 2, f = I, I |= 4;
        try {
          Um(l, t, u);
        } finally {
          I = f, M.p = e, b.T = a;
        }
      }
      zl = 1, C0(), q0(), B0();
    }
  }
  function C0() {
    if (zl === 1) {
      zl = 0;
      var l = yu, t = ba, u = (t.flags & 13878) !== 0;
      if ((t.subtreeFlags & 13878) !== 0 || u) {
        u = b.T, b.T = null;
        var a = M.p;
        M.p = 2;
        var e = I;
        I |= 4;
        try {
          m0(t, l);
          var n = kc, f = rs(l.containerInfo), c = n.focusedElem, i = n.selectionRange;
          if (f !== c && c && c.ownerDocument && gs(
            c.ownerDocument.documentElement,
            c
          )) {
            if (i !== null && zf(c)) {
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
                  var y = Ss(
                    c,
                    C
                  ), s = Ss(
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
          jn = !!Fc, kc = Fc = null;
        } finally {
          I = e, M.p = a, b.T = u;
        }
      }
      l.current = t, zl = 2;
    }
  }
  function q0() {
    if (zl === 2) {
      zl = 0;
      var l = yu, t = ba, u = (t.flags & 8772) !== 0;
      if ((t.subtreeFlags & 8772) !== 0 || u) {
        u = b.T, b.T = null;
        var a = M.p;
        M.p = 2;
        var e = I;
        I |= 4;
        try {
          i0(l, t.alternate, t);
        } finally {
          I = e, M.p = a, b.T = u;
        }
      }
      zl = 3;
    }
  }
  function B0() {
    if (zl === 4 || zl === 3) {
      zl = 0, fv();
      var l = yu, t = ba, u = Jt, a = E0;
      (t.subtreeFlags & 10256) !== 0 || (t.flags & 10256) !== 0 ? zl = 5 : (zl = 0, ba = yu = null, Y0(l, l.pendingLanes));
      var e = l.pendingLanes;
      if (e === 0 && (ou = null), uf(u), t = t.stateNode, Fl && typeof Fl.onCommitFiberRoot == "function")
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
      (Jt & 3) !== 0 && Mn(), pt(l), e = l.pendingLanes, (u & 261930) !== 0 && (e & 42) !== 0 ? l === Gc ? oe++ : (oe = 0, Gc = l) : oe = 0, ye(0);
    }
  }
  function Y0(l, t) {
    (l.pooledCacheLanes &= t) === 0 && (t = l.pooledCache, t != null && (l.pooledCache = null, Ka(t)));
  }
  function Mn() {
    return C0(), q0(), B0(), G0();
  }
  function G0() {
    if (zl !== 5) return !1;
    var l = yu, t = Bc;
    Bc = 0;
    var u = uf(Jt), a = b.T, e = M.p;
    try {
      M.p = 32 > u ? 32 : u, b.T = null, u = Yc, Yc = null;
      var n = yu, f = Jt;
      if (zl = 0, ba = yu = null, Jt = 0, (I & 6) !== 0) throw Error(m(331));
      var c = I;
      if (I |= 4, r0(n.current), h0(
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
      M.p = e, b.T = a, Y0(l, t);
    }
  }
  function j0(l, t, u) {
    t = ot(u, t), t = hc(l.stateNode, t, 2), l = eu(l, t, 2), l !== null && (Ra(l, 2), pt(l));
  }
  function al(l, t, u) {
    if (l.tag === 3)
      j0(l, l, u);
    else
      for (; t !== null; ) {
        if (t.tag === 3) {
          j0(
            t,
            l,
            u
          );
          break;
        } else if (t.tag === 1) {
          var a = t.stateNode;
          if (typeof t.type.getDerivedStateFromError == "function" || typeof a.componentDidCatch == "function" && (ou === null || !ou.has(a))) {
            l = ot(u, l), u = Xo(2), a = eu(t, u, 2), a !== null && (Qo(
              u,
              a,
              t,
              l
            ), Ra(a, 2), pt(a));
            break;
          }
        }
        t = t.return;
      }
  }
  function Qc(l, t, u) {
    var a = l.pingCache;
    if (a === null) {
      a = l.pingCache = new Hm();
      var e = /* @__PURE__ */ new Set();
      a.set(t, e);
    } else
      e = a.get(t), e === void 0 && (e = /* @__PURE__ */ new Set(), a.set(t, e));
    e.has(u) || (Hc = !0, e.add(u), l = Gm.bind(null, l, t, u), t.then(l, l));
  }
  function Gm(l, t, u) {
    var a = l.pingCache;
    a !== null && a.delete(t), l.pingedLanes |= l.suspendedLanes & u, l.warmLanes &= ~u, sl === l && (K & u) === u && (dl === 4 || dl === 3 && (K & 62914560) === K && 300 > $l() - Tn ? (I & 2) === 0 && Ta(l, 0) : Cc |= u, ra === K && (ra = 0)), pt(l);
  }
  function X0(l, t) {
    t === 0 && (t = Hi()), l = Du(l, t), l !== null && (Ra(l, t), pt(l));
  }
  function jm(l) {
    var t = l.memoizedState, u = 0;
    t !== null && (u = t.retryLane), X0(l, u);
  }
  function Xm(l, t) {
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
        throw Error(m(314));
    }
    a !== null && a.delete(t), X0(l, u);
  }
  function Qm(l, t) {
    return In(l, t);
  }
  var pn = null, za = null, Zc = !1, Dn = !1, Lc = !1, mu = 0;
  function pt(l) {
    l !== za && l.next === null && (za === null ? pn = za = l : za = za.next = l), Dn = !0, Zc || (Zc = !0, Lm());
  }
  function ye(l, t) {
    if (!Lc && Dn) {
      Lc = !0;
      do
        for (var u = !1, a = pn; a !== null; ) {
          if (l !== 0) {
            var e = a.pendingLanes;
            if (e === 0) var n = 0;
            else {
              var f = a.suspendedLanes, c = a.pingedLanes;
              n = (1 << 31 - kl(42 | l) + 1) - 1, n &= e & ~(f & ~c), n = n & 201326741 ? n & 201326741 | 1 : n ? n | 2 : 0;
            }
            n !== 0 && (u = !0, x0(a, n));
          } else
            n = K, n = Re(
              a,
              a === sl ? n : 0,
              a.cancelPendingCommit !== null || a.timeoutHandle !== -1
            ), (n & 3) === 0 || Na(a, n) || (u = !0, x0(a, n));
          a = a.next;
        }
      while (u);
      Lc = !1;
    }
  }
  function Zm() {
    Q0();
  }
  function Q0() {
    Dn = Zc = !1;
    var l = 0;
    mu !== 0 && Im() && (l = mu);
    for (var t = $l(), u = null, a = pn; a !== null; ) {
      var e = a.next, n = Z0(a, t);
      n === 0 ? (a.next = null, u === null ? pn = e : u.next = e, e === null && (za = u)) : (u = a, (l !== 0 || (n & 3) !== 0) && (Dn = !0)), a = e;
    }
    zl !== 0 && zl !== 5 || ye(l), mu !== 0 && (mu = 0);
  }
  function Z0(l, t) {
    for (var u = l.suspendedLanes, a = l.pingedLanes, e = l.expirationTimes, n = l.pendingLanes & -62914561; 0 < n; ) {
      var f = 31 - kl(n), c = 1 << f, i = e[f];
      i === -1 ? ((c & u) === 0 || (c & a) !== 0) && (e[f] = dv(c, t)) : i <= t && (l.expiredLanes |= c), n &= ~c;
    }
    if (t = sl, u = K, u = Re(
      l,
      l === t ? u : 0,
      l.cancelPendingCommit !== null || l.timeoutHandle !== -1
    ), a = l.callbackNode, u === 0 || l === t && (ul === 2 || ul === 9) || l.cancelPendingCommit !== null)
      return a !== null && a !== null && Pn(a), l.callbackNode = null, l.callbackPriority = 0;
    if ((u & 3) === 0 || Na(l, u)) {
      if (t = u & -u, t === l.callbackPriority) return t;
      switch (a !== null && Pn(a), uf(u)) {
        case 2:
        case 8:
          u = Ni;
          break;
        case 32:
          u = pe;
          break;
        case 268435456:
          u = Ri;
          break;
        default:
          u = pe;
      }
      return a = L0.bind(null, l), u = In(u, a), l.callbackPriority = t, l.callbackNode = u, t;
    }
    return a !== null && a !== null && Pn(a), l.callbackPriority = 2, l.callbackNode = null, 2;
  }
  function L0(l, t) {
    if (zl !== 0 && zl !== 5)
      return l.callbackNode = null, l.callbackPriority = 0, null;
    var u = l.callbackNode;
    if (Mn() && l.callbackNode !== u)
      return null;
    var a = K;
    return a = Re(
      l,
      l === sl ? a : 0,
      l.cancelPendingCommit !== null || l.timeoutHandle !== -1
    ), a === 0 ? null : (A0(l, a, t), Z0(l, $l()), l.callbackNode != null && l.callbackNode === u ? L0.bind(null, l) : null);
  }
  function x0(l, t) {
    if (Mn()) return null;
    A0(l, t, !0);
  }
  function Lm() {
    ld(function() {
      (I & 6) !== 0 ? In(
        Ui,
        Zm
      ) : Q0();
    });
  }
  function xc() {
    if (mu === 0) {
      var l = ca;
      l === 0 && (l = De, De <<= 1, (De & 261888) === 0 && (De = 256)), mu = l;
    }
    return mu;
  }
  function V0(l) {
    return l == null || typeof l == "symbol" || typeof l == "boolean" ? null : typeof l == "function" ? l : Be("" + l);
  }
  function K0(l, t) {
    var u = t.ownerDocument.createElement("input");
    return u.name = t.name, u.value = t.value, l.id && u.setAttribute("form", l.id), t.parentNode.insertBefore(u, t), l = new FormData(l), u.parentNode.removeChild(u), l;
  }
  function xm(l, t, u, a, e) {
    if (t === "submit" && u && u.stateNode === e) {
      var n = V0(
        (e[Ql] || null).action
      ), f = a.submitter;
      f && (t = (t = f[Ql] || null) ? V0(t.formAction) : f.getAttribute("formAction"), t !== null && (n = t, f = null));
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
                  var i = f ? K0(e, f) : new FormData(e);
                  sc(
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
                typeof n == "function" && (c.preventDefault(), i = f ? K0(e, f) : new FormData(e), sc(
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
  for (var Vc = 0; Vc < Mf.length; Vc++) {
    var Kc = Mf[Vc], Vm = Kc.toLowerCase(), Km = Kc[0].toUpperCase() + Kc.slice(1);
    rt(
      Vm,
      "on" + Km
    );
  }
  rt(Es, "onAnimationEnd"), rt(zs, "onAnimationIteration"), rt(As, "onAnimationStart"), rt("dblclick", "onDoubleClick"), rt("focusin", "onFocus"), rt("focusout", "onBlur"), rt(cm, "onTransitionRun"), rt(im, "onTransitionStart"), rt(sm, "onTransitionCancel"), rt(_s, "onTransitionEnd"), wu("onMouseEnter", ["mouseout", "mouseover"]), wu("onMouseLeave", ["mouseout", "mouseover"]), wu("onPointerEnter", ["pointerout", "pointerover"]), wu("onPointerLeave", ["pointerout", "pointerover"]), _u(
    "onChange",
    "change click focusin focusout input keydown keyup selectionchange".split(" ")
  ), _u(
    "onSelect",
    "focusout contextmenu dragend focusin keydown keyup mousedown mouseup selectionchange".split(
      " "
    )
  ), _u("onBeforeInput", [
    "compositionend",
    "keypress",
    "textInput",
    "paste"
  ]), _u(
    "onCompositionEnd",
    "compositionend focusout keydown keypress keyup mousedown".split(" ")
  ), _u(
    "onCompositionStart",
    "compositionstart focusout keydown keypress keyup mousedown".split(" ")
  ), _u(
    "onCompositionUpdate",
    "compositionupdate focusout keydown keypress keyup mousedown".split(" ")
  );
  var ve = "abort canplay canplaythrough durationchange emptied encrypted ended error loadeddata loadedmetadata loadstart pause play playing progress ratechange resize seeked seeking stalled suspend timeupdate volumechange waiting".split(
    " "
  ), Jm = new Set(
    "beforetoggle cancel close invalid load scroll scrollend toggle".split(" ").concat(ve)
  );
  function J0(l, t) {
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
              Le(r);
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
              Le(r);
            }
            e.currentTarget = null, n = i;
          }
      }
    }
  }
  function x(l, t) {
    var u = t[af];
    u === void 0 && (u = t[af] = /* @__PURE__ */ new Set());
    var a = l + "__bubble";
    u.has(a) || (w0(t, l, 2, !1), u.add(a));
  }
  function Jc(l, t, u) {
    var a = 0;
    t && (a |= 4), w0(
      u,
      l,
      a,
      t
    );
  }
  var Un = "_reactListening" + Math.random().toString(36).slice(2);
  function wc(l) {
    if (!l[Un]) {
      l[Un] = !0, Xi.forEach(function(u) {
        u !== "selectionchange" && (Jm.has(u) || Jc(u, !1, l), Jc(u, !0, l));
      });
      var t = l.nodeType === 9 ? l : l.ownerDocument;
      t === null || t[Un] || (t[Un] = !0, Jc("selectionchange", !1, t));
    }
  }
  function w0(l, t, u, a) {
    switch (zy(t)) {
      case 2:
        var e = Td;
        break;
      case 8:
        e = Ed;
        break;
      default:
        e = ii;
    }
    u = e.bind(
      null,
      t,
      u,
      l
    ), e = void 0, !mf || t !== "touchstart" && t !== "touchmove" && t !== "wheel" || (e = !0), a ? e !== void 0 ? l.addEventListener(t, u, {
      capture: !0,
      passive: e
    }) : l.addEventListener(t, u, !0) : e !== void 0 ? l.addEventListener(t, u, {
      passive: e
    }) : l.addEventListener(t, u, !1);
  }
  function Wc(l, t, u, a, e) {
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
    ki(function() {
      var h = n, r = yf(u), E = [];
      l: {
        var S = Os.get(l);
        if (S !== void 0) {
          var g = Xe, D = l;
          switch (l) {
            case "keypress":
              if (Ge(u) === 0) break l;
            case "keydown":
            case "keyup":
              g = Xv;
              break;
            case "focusin":
              D = "focus", g = gf;
              break;
            case "focusout":
              D = "blur", g = gf;
              break;
            case "beforeblur":
            case "afterblur":
              g = gf;
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
              g = ls;
              break;
            case "drag":
            case "dragend":
            case "dragenter":
            case "dragexit":
            case "dragleave":
            case "dragover":
            case "dragstart":
            case "drop":
              g = pv;
              break;
            case "touchcancel":
            case "touchend":
            case "touchmove":
            case "touchstart":
              g = Lv;
              break;
            case Es:
            case zs:
            case As:
              g = Nv;
              break;
            case _s:
              g = Vv;
              break;
            case "scroll":
            case "scrollend":
              g = Ov;
              break;
            case "wheel":
              g = Jv;
              break;
            case "copy":
            case "cut":
            case "paste":
              g = Hv;
              break;
            case "gotpointercapture":
            case "lostpointercapture":
            case "pointercancel":
            case "pointerdown":
            case "pointermove":
            case "pointerout":
            case "pointerover":
            case "pointerup":
              g = us;
              break;
            case "toggle":
            case "beforetoggle":
              g = Wv;
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
          if (S = l === "mouseover" || l === "pointerover", g = l === "mouseout" || l === "pointerout", S && u !== of && (D = u.relatedTarget || u.fromElement) && (Vu(D) || D[xu]))
            break l;
          if ((g || S) && (S = r.window === r ? r : (S = r.ownerDocument) ? S.defaultView || S.parentWindow : window, g ? (D = u.relatedTarget || u.toElement, g = h, D = D ? Vu(D) : null, D !== null && (cl = V(D), C = D.tag, D !== cl || C !== 5 && C !== 27 && C !== 6) && (D = null)) : (g = null, D = h), g !== D)) {
            if (C = ls, T = "onMouseLeave", y = "onMouseEnter", s = "mouse", (l === "pointerout" || l === "pointerover") && (C = us, T = "onPointerLeave", y = "onPointerEnter", s = "pointer"), cl = g == null ? S : Ca(g), d = D == null ? S : Ca(D), S = new C(
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
                for (C = wm, y = g, s = D, d = 0, T = y; T; T = C(T))
                  d++;
                T = 0;
                for (var H = s; H; H = C(H))
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
            g !== null && W0(
              E,
              S,
              g,
              C,
              !1
            ), D !== null && cl !== null && W0(
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
            var F = os;
          else if (is(S))
            if (ys)
              F = em;
            else {
              F = um;
              var N = tm;
            }
          else
            g = S.nodeName, !g || g.toLowerCase() !== "input" || S.type !== "checkbox" && S.type !== "radio" ? h && sf(h.elementType) && (F = os) : F = am;
          if (F && (F = F(l, h))) {
            ss(
              E,
              F,
              u,
              r
            );
            break l;
          }
          N && N(l, S, h), l === "focusout" && h && S.type === "number" && h.memoizedProps.value != null && cf(S, "number", S.value);
        }
        switch (N = h ? Ca(h) : window, l) {
          case "focusin":
            (is(N) || N.contentEditable === "true") && (Pu = N, Af = h, La = null);
            break;
          case "focusout":
            La = Af = Pu = null;
            break;
          case "mousedown":
            _f = !0;
            break;
          case "contextmenu":
          case "mouseup":
          case "dragend":
            _f = !1, bs(E, u, r);
            break;
          case "selectionchange":
            if (fm) break;
          case "keydown":
          case "keyup":
            bs(E, u, r);
        }
        var Q;
        if (bf)
          l: {
            switch (l) {
              case "compositionstart":
                var J = "onCompositionStart";
                break l;
              case "compositionend":
                J = "onCompositionEnd";
                break l;
              case "compositionupdate":
                J = "onCompositionUpdate";
                break l;
            }
            J = void 0;
          }
        else
          Iu ? fs(l, u) && (J = "onCompositionEnd") : l === "keydown" && u.keyCode === 229 && (J = "onCompositionStart");
        J && (as && u.locale !== "ko" && (Iu || J !== "onCompositionStart" ? J === "onCompositionEnd" && Iu && (Q = Ii()) : (kt = r, df = "value" in kt ? kt.value : kt.textContent, Iu = !0)), N = Nn(h, J), 0 < N.length && (J = new ts(
          J,
          l,
          null,
          u,
          r
        ), E.push({ event: J, listeners: N }), Q ? J.data = Q : (Q = cs(u), Q !== null && (J.data = Q)))), (Q = Fv ? kv(l, u) : Iv(l, u)) && (J = Nn(h, "onBeforeInput"), 0 < J.length && (N = new ts(
          "onBeforeInput",
          "beforeinput",
          null,
          u,
          r
        ), E.push({
          event: N,
          listeners: J
        }), N.data = Q)), xm(
          E,
          l,
          h,
          u,
          r
        );
      }
      J0(E, t);
    });
  }
  function me(l, t, u) {
    return {
      instance: l,
      listener: t,
      currentTarget: u
    };
  }
  function Nn(l, t) {
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
  function wm(l) {
    if (l === null) return null;
    do
      l = l.return;
    while (l && l.tag !== 5 && l.tag !== 27);
    return l || null;
  }
  function W0(l, t, u, a, e) {
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
  var Wm = /\r\n?/g, $m = /\u0000|\uFFFD/g;
  function $0(l) {
    return (typeof l == "string" ? l : "" + l).replace(Wm, `
`).replace($m, "");
  }
  function F0(l, t) {
    return t = $0(t), $0(l) === t;
  }
  function fl(l, t, u, a, e, n) {
    switch (u) {
      case "children":
        typeof a == "string" ? t === "body" || t === "textarea" && a === "" || $u(l, a) : (typeof a == "number" || typeof a == "bigint") && t !== "body" && $u(l, "" + a);
        break;
      case "className":
        Ce(l, "class", a);
        break;
      case "tabIndex":
        Ce(l, "tabindex", a);
        break;
      case "dir":
      case "role":
      case "viewBox":
      case "width":
      case "height":
        Ce(l, u, a);
        break;
      case "style":
        $i(l, a, n);
        break;
      case "data":
        if (t !== "object") {
          Ce(l, "data", a);
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
        a = Be("" + a), l.setAttribute(u, a);
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
        a = Be("" + a), l.setAttribute(u, a);
        break;
      case "onClick":
        a != null && (l.onclick = Rt);
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
            throw Error(m(61));
          if (u = a.__html, u != null) {
            if (e.children != null) throw Error(m(60));
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
        u = Be("" + a), l.setAttributeNS(
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
        Nt(
          l,
          "http://www.w3.org/1999/xlink",
          "xlink:actuate",
          a
        );
        break;
      case "xlinkArcrole":
        Nt(
          l,
          "http://www.w3.org/1999/xlink",
          "xlink:arcrole",
          a
        );
        break;
      case "xlinkRole":
        Nt(
          l,
          "http://www.w3.org/1999/xlink",
          "xlink:role",
          a
        );
        break;
      case "xlinkShow":
        Nt(
          l,
          "http://www.w3.org/1999/xlink",
          "xlink:show",
          a
        );
        break;
      case "xlinkTitle":
        Nt(
          l,
          "http://www.w3.org/1999/xlink",
          "xlink:title",
          a
        );
        break;
      case "xlinkType":
        Nt(
          l,
          "http://www.w3.org/1999/xlink",
          "xlink:type",
          a
        );
        break;
      case "xmlBase":
        Nt(
          l,
          "http://www.w3.org/XML/1998/namespace",
          "xml:base",
          a
        );
        break;
      case "xmlLang":
        Nt(
          l,
          "http://www.w3.org/XML/1998/namespace",
          "xml:lang",
          a
        );
        break;
      case "xmlSpace":
        Nt(
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
        (!(2 < u.length) || u[0] !== "o" && u[0] !== "O" || u[1] !== "n" && u[1] !== "N") && (u = Av.get(u) || u, He(l, u, a));
    }
  }
  function $c(l, t, u, a, e, n) {
    switch (u) {
      case "style":
        $i(l, a, n);
        break;
      case "dangerouslySetInnerHTML":
        if (a != null) {
          if (typeof a != "object" || !("__html" in a))
            throw Error(m(61));
          if (u = a.__html, u != null) {
            if (e.children != null) throw Error(m(60));
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
        a != null && (l.onclick = Rt);
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
        if (!Qi.hasOwnProperty(u))
          l: {
            if (u[0] === "o" && u[1] === "n" && (e = u.endsWith("Capture"), t = u.slice(2, e ? u.length - 7 : void 0), n = l[Ql] || null, n = n != null ? n[u] : null, typeof n == "function" && l.removeEventListener(t, n, e), typeof a == "function")) {
              typeof n != "function" && n !== null && (u in l ? l[u] = null : l.hasAttribute(u) && l.removeAttribute(u)), l.addEventListener(t, a, e);
              break l;
            }
            u in l ? l[u] = a : a === !0 ? l.setAttribute(u, "") : He(l, u, a);
          }
    }
  }
  function Nl(l, t, u) {
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
                  throw Error(m(137, t));
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
                    throw Error(m(137, t));
                  break;
                default:
                  fl(l, t, a, r, u, null);
              }
          }
        Ki(
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
                if (c != null) throw Error(m(91));
                break;
              default:
                fl(l, t, f, c, u, null);
            }
        wi(l, a, e, n);
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
                throw Error(m(137, t));
              default:
                fl(l, t, h, a, u, null);
            }
        return;
      default:
        if (sf(t)) {
          for (r in u)
            u.hasOwnProperty(r) && (a = u[r], a !== void 0 && $c(
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
  function Fm(l, t, u, a) {
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
                  throw Error(m(137, t));
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
        ff(
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
                if (e != null) throw Error(m(91));
                break;
              default:
                e !== n && fl(l, t, f, e, a, n);
            }
        Ji(l, S, g);
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
                  throw Error(m(137, t));
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
        if (sf(t)) {
          for (var cl in u)
            S = u[cl], u.hasOwnProperty(cl) && S !== void 0 && !a.hasOwnProperty(cl) && $c(
              l,
              t,
              cl,
              void 0,
              a,
              S
            );
          for (r in a)
            S = a[r], g = u[r], !a.hasOwnProperty(r) || S === g || S === void 0 && g === void 0 || $c(
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
  function k0(l) {
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
  function km() {
    if (typeof performance.getEntriesByType == "function") {
      for (var l = 0, t = 0, u = performance.getEntriesByType("resource"), a = 0; a < u.length; a++) {
        var e = u[a], n = e.transferSize, f = e.initiatorType, c = e.duration;
        if (n && c && k0(f)) {
          for (f = 0, c = e.responseEnd, a += 1; a < u.length; a++) {
            var i = u[a], h = i.startTime;
            if (h > c) break;
            var r = i.transferSize, E = i.initiatorType;
            r && k0(E) && (i = i.responseEnd, f += r * (i < c ? 1 : (c - h) / (i - h)));
          }
          if (--a, t += 8 * (n + f) / (e.duration / 1e3), l++, 10 < l) break;
        }
      }
      if (0 < l) return t / l / 1e6;
    }
    return navigator.connection && (l = navigator.connection.downlink, typeof l == "number") ? l : 5;
  }
  var Fc = null, kc = null;
  function Rn(l) {
    return l.nodeType === 9 ? l : l.ownerDocument;
  }
  function I0(l) {
    switch (l) {
      case "http://www.w3.org/2000/svg":
        return 1;
      case "http://www.w3.org/1998/Math/MathML":
        return 2;
      default:
        return 0;
    }
  }
  function P0(l, t) {
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
  function Ic(l, t) {
    return l === "textarea" || l === "noscript" || typeof t.children == "string" || typeof t.children == "number" || typeof t.children == "bigint" || typeof t.dangerouslySetInnerHTML == "object" && t.dangerouslySetInnerHTML !== null && t.dangerouslySetInnerHTML.__html != null;
  }
  var Pc = null;
  function Im() {
    var l = window.event;
    return l && l.type === "popstate" ? l === Pc ? !1 : (Pc = l, !0) : (Pc = null, !1);
  }
  var ly = typeof setTimeout == "function" ? setTimeout : void 0, Pm = typeof clearTimeout == "function" ? clearTimeout : void 0, ty = typeof Promise == "function" ? Promise : void 0, ld = typeof queueMicrotask == "function" ? queueMicrotask : typeof ty < "u" ? function(l) {
    return ty.resolve(null).then(l).catch(td);
  } : ly;
  function td(l) {
    setTimeout(function() {
      throw l;
    });
  }
  function du(l) {
    return l === "head";
  }
  function uy(l, t) {
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
  function ay(l, t) {
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
  function li(l) {
    var t = l.firstChild;
    for (t && t.nodeType === 10 && (t = t.nextSibling); t; ) {
      var u = t;
      switch (t = t.nextSibling, u.nodeName) {
        case "HTML":
        case "HEAD":
        case "BODY":
          li(u), ef(u);
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
  function ud(l, t, u, a) {
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
  function ad(l, t, u) {
    if (t === "") return null;
    for (; l.nodeType !== 3; )
      if ((l.nodeType !== 1 || l.nodeName !== "INPUT" || l.type !== "hidden") && !u || (l = ht(l.nextSibling), l === null)) return null;
    return l;
  }
  function ey(l, t) {
    for (; l.nodeType !== 8; )
      if ((l.nodeType !== 1 || l.nodeName !== "INPUT" || l.type !== "hidden") && !t || (l = ht(l.nextSibling), l === null)) return null;
    return l;
  }
  function ti(l) {
    return l.data === "$?" || l.data === "$~";
  }
  function ui(l) {
    return l.data === "$!" || l.data === "$?" && l.ownerDocument.readyState !== "loading";
  }
  function ed(l, t) {
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
  var ai = null;
  function ny(l) {
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
  function fy(l) {
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
  function cy(l, t, u) {
    switch (t = Rn(u), l) {
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
  function de(l) {
    for (var t = l.attributes; t.length; )
      l.removeAttributeNode(t[0]);
    ef(l);
  }
  var St = /* @__PURE__ */ new Map(), iy = /* @__PURE__ */ new Set();
  function Hn(l) {
    return typeof l.getRootNode == "function" ? l.getRootNode() : l.nodeType === 9 ? l : l.ownerDocument;
  }
  var wt = M.d;
  M.d = {
    f: nd,
    r: fd,
    D: cd,
    C: id,
    L: sd,
    m: od,
    X: vd,
    S: yd,
    M: md
  };
  function nd() {
    var l = wt.f(), t = An();
    return l || t;
  }
  function fd(l) {
    var t = Ku(l);
    t !== null && t.tag === 5 && t.type === "form" ? Oo(t) : wt.r(l);
  }
  var Aa = typeof document > "u" ? null : document;
  function sy(l, t, u) {
    var a = Aa;
    if (a && typeof t == "string" && t) {
      var e = it(t);
      e = 'link[rel="' + l + '"][href="' + e + '"]', typeof u == "string" && (e += '[crossorigin="' + u + '"]'), iy.has(e) || (iy.add(e), l = { rel: l, crossOrigin: u, href: t }, a.querySelector(e) === null && (t = a.createElement("link"), Nl(t, "link", l), _l(t), a.head.appendChild(t)));
    }
  }
  function cd(l) {
    wt.D(l), sy("dns-prefetch", l, null);
  }
  function id(l, t) {
    wt.C(l, t), sy("preconnect", l, t);
  }
  function sd(l, t, u) {
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
      St.has(n) || (l = B(
        {
          rel: "preload",
          href: t === "image" && u && u.imageSrcSet ? void 0 : l,
          as: t
        },
        u
      ), St.set(n, l), a.querySelector(e) !== null || t === "style" && a.querySelector(he(n)) || t === "script" && a.querySelector(Se(n)) || (t = a.createElement("link"), Nl(t, "link", l), _l(t), a.head.appendChild(t)));
    }
  }
  function od(l, t) {
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
      if (!St.has(n) && (l = B({ rel: "modulepreload", href: l }, t), St.set(n, l), u.querySelector(e) === null)) {
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
        a = u.createElement("link"), Nl(a, "link", l), _l(a), u.head.appendChild(a);
      }
    }
  }
  function yd(l, t, u) {
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
          l = B(
            { rel: "stylesheet", href: l, "data-precedence": t },
            u
          ), (u = St.get(n)) && ei(l, u);
          var i = f = a.createElement("link");
          _l(i), Nl(i, "link", l), i._p = new Promise(function(h, r) {
            i.onload = h, i.onerror = r;
          }), i.addEventListener("load", function() {
            c.loading |= 1;
          }), i.addEventListener("error", function() {
            c.loading |= 2;
          }), c.loading |= 4, Cn(f, t, a);
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
  function vd(l, t) {
    wt.X(l, t);
    var u = Aa;
    if (u && l) {
      var a = Ju(u).hoistableScripts, e = Oa(l), n = a.get(e);
      n || (n = u.querySelector(Se(e)), n || (l = B({ src: l, async: !0 }, t), (t = St.get(e)) && ni(l, t), n = u.createElement("script"), _l(n), Nl(n, "link", l), u.head.appendChild(n)), n = {
        type: "script",
        instance: n,
        count: 1,
        state: null
      }, a.set(e, n));
    }
  }
  function md(l, t) {
    wt.M(l, t);
    var u = Aa;
    if (u && l) {
      var a = Ju(u).hoistableScripts, e = Oa(l), n = a.get(e);
      n || (n = u.querySelector(Se(e)), n || (l = B({ src: l, async: !0, type: "module" }, t), (t = St.get(e)) && ni(l, t), n = u.createElement("script"), _l(n), Nl(n, "link", l), u.head.appendChild(n)), n = {
        type: "script",
        instance: n,
        count: 1,
        state: null
      }, a.set(e, n));
    }
  }
  function oy(l, t, u, a) {
    var e = (e = Z.current) ? Hn(e) : null;
    if (!e) throw Error(m(446));
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
          }, St.set(l, u), n || dd(
            e,
            l,
            u,
            f.state
          ))), t && a === null)
            throw Error(m(528, ""));
          return f;
        }
        if (t && a !== null)
          throw Error(m(529, ""));
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
        throw Error(m(444, l));
    }
  }
  function _a(l) {
    return 'href="' + it(l) + '"';
  }
  function he(l) {
    return 'link[rel="stylesheet"][' + l + "]";
  }
  function yy(l) {
    return B({}, l, {
      "data-precedence": l.precedence,
      precedence: null
    });
  }
  function dd(l, t, u, a) {
    l.querySelector('link[rel="preload"][as="style"][' + t + "]") ? a.loading = 1 : (t = l.createElement("link"), a.preload = t, t.addEventListener("load", function() {
      return a.loading |= 1;
    }), t.addEventListener("error", function() {
      return a.loading |= 2;
    }), Nl(t, "link", u), _l(t), l.head.appendChild(t));
  }
  function Oa(l) {
    return '[src="' + it(l) + '"]';
  }
  function Se(l) {
    return "script[async]" + l;
  }
  function vy(l, t, u) {
    if (t.count++, t.instance === null)
      switch (t.type) {
        case "style":
          var a = l.querySelector(
            'style[data-href~="' + it(u.href) + '"]'
          );
          if (a)
            return t.instance = a, _l(a), a;
          var e = B({}, u, {
            "data-href": u.href,
            "data-precedence": u.precedence,
            href: null,
            precedence: null
          });
          return a = (l.ownerDocument || l).createElement(
            "style"
          ), _l(a), Nl(a, "style", e), Cn(a, u.precedence, l), t.instance = a;
        case "stylesheet":
          e = _a(u.href);
          var n = l.querySelector(
            he(e)
          );
          if (n)
            return t.state.loading |= 4, t.instance = n, _l(n), n;
          a = yy(u), (e = St.get(e)) && ei(a, e), n = (l.ownerDocument || l).createElement("link"), _l(n);
          var f = n;
          return f._p = new Promise(function(c, i) {
            f.onload = c, f.onerror = i;
          }), Nl(n, "link", a), t.state.loading |= 4, Cn(n, u.precedence, l), t.instance = n;
        case "script":
          return n = Oa(u.src), (e = l.querySelector(
            Se(n)
          )) ? (t.instance = e, _l(e), e) : (a = u, (e = St.get(n)) && (a = B({}, u), ni(a, e)), l = l.ownerDocument || l, e = l.createElement("script"), _l(e), Nl(e, "link", a), l.head.appendChild(e), t.instance = e);
        case "void":
          return null;
        default:
          throw Error(m(443, t.type));
      }
    else
      t.type === "stylesheet" && (t.state.loading & 4) === 0 && (a = t.instance, t.state.loading |= 4, Cn(a, u.precedence, l));
    return t.instance;
  }
  function Cn(l, t, u) {
    for (var a = u.querySelectorAll(
      'link[rel="stylesheet"][data-precedence],style[data-precedence]'
    ), e = a.length ? a[a.length - 1] : null, n = e, f = 0; f < a.length; f++) {
      var c = a[f];
      if (c.dataset.precedence === t) n = c;
      else if (n !== e) break;
    }
    n ? n.parentNode.insertBefore(l, n.nextSibling) : (t = u.nodeType === 9 ? u.head : u, t.insertBefore(l, t.firstChild));
  }
  function ei(l, t) {
    l.crossOrigin == null && (l.crossOrigin = t.crossOrigin), l.referrerPolicy == null && (l.referrerPolicy = t.referrerPolicy), l.title == null && (l.title = t.title);
  }
  function ni(l, t) {
    l.crossOrigin == null && (l.crossOrigin = t.crossOrigin), l.referrerPolicy == null && (l.referrerPolicy = t.referrerPolicy), l.integrity == null && (l.integrity = t.integrity);
  }
  var qn = null;
  function my(l, t, u) {
    if (qn === null) {
      var a = /* @__PURE__ */ new Map(), e = qn = /* @__PURE__ */ new Map();
      e.set(u, a);
    } else
      e = qn, a = e.get(u), a || (a = /* @__PURE__ */ new Map(), e.set(u, a));
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
  function dy(l, t, u) {
    l = l.ownerDocument || l, l.head.insertBefore(
      u,
      t === "title" ? l.querySelector("head > title") : null
    );
  }
  function hd(l, t, u) {
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
  function hy(l) {
    return !(l.type === "stylesheet" && (l.state.loading & 3) === 0);
  }
  function Sd(l, t, u, a) {
    if (u.type === "stylesheet" && (typeof a.media != "string" || matchMedia(a.media).matches !== !1) && (u.state.loading & 4) === 0) {
      if (u.instance === null) {
        var e = _a(a.href), n = t.querySelector(
          he(e)
        );
        if (n) {
          t = n._p, t !== null && typeof t == "object" && typeof t.then == "function" && (l.count++, l = Bn.bind(l), t.then(l, l)), u.state.loading |= 4, u.instance = n, _l(n);
          return;
        }
        n = t.ownerDocument || t, a = yy(a), (e = St.get(e)) && ei(a, e), n = n.createElement("link"), _l(n);
        var f = n;
        f._p = new Promise(function(c, i) {
          f.onload = c, f.onerror = i;
        }), Nl(n, "link", a), u.instance = n;
      }
      l.stylesheets === null && (l.stylesheets = /* @__PURE__ */ new Map()), l.stylesheets.set(u, t), (t = u.state.preload) && (u.state.loading & 3) === 0 && (l.count++, u = Bn.bind(l), t.addEventListener("load", u), t.addEventListener("error", u));
    }
  }
  var fi = 0;
  function gd(l, t) {
    return l.stylesheets && l.count === 0 && Gn(l, l.stylesheets), 0 < l.count || 0 < l.imgCount ? function(u) {
      var a = setTimeout(function() {
        if (l.stylesheets && Gn(l, l.stylesheets), l.unsuspend) {
          var n = l.unsuspend;
          l.unsuspend = null, n();
        }
      }, 6e4 + t);
      0 < l.imgBytes && fi === 0 && (fi = 62500 * km());
      var e = setTimeout(
        function() {
          if (l.waitingForImages = !1, l.count === 0 && (l.stylesheets && Gn(l, l.stylesheets), l.unsuspend)) {
            var n = l.unsuspend;
            l.unsuspend = null, n();
          }
        },
        (l.imgBytes > fi ? 50 : 800) + t
      );
      return l.unsuspend = u, function() {
        l.unsuspend = null, clearTimeout(a), clearTimeout(e);
      };
    } : null;
  }
  function Bn() {
    if (this.count--, this.count === 0 && (this.imgCount === 0 || !this.waitingForImages)) {
      if (this.stylesheets) Gn(this, this.stylesheets);
      else if (this.unsuspend) {
        var l = this.unsuspend;
        this.unsuspend = null, l();
      }
    }
  }
  var Yn = null;
  function Gn(l, t) {
    l.stylesheets = null, l.unsuspend !== null && (l.count++, Yn = /* @__PURE__ */ new Map(), t.forEach(rd, l), Yn = null, Bn.call(l));
  }
  function rd(l, t) {
    if (!(t.state.loading & 4)) {
      var u = Yn.get(l);
      if (u) var a = u.get(null);
      else {
        u = /* @__PURE__ */ new Map(), Yn.set(l, u);
        for (var e = l.querySelectorAll(
          "link[data-precedence],style[data-precedence]"
        ), n = 0; n < e.length; n++) {
          var f = e[n];
          (f.nodeName === "LINK" || f.getAttribute("media") !== "not all") && (u.set(f.dataset.precedence, f), a = f);
        }
        a && u.set(null, a);
      }
      e = t.instance, f = e.getAttribute("data-precedence"), n = u.get(f) || a, n === a && u.set(null, e), u.set(f, e), this.count++, a = Bn.bind(this), e.addEventListener("load", a), e.addEventListener("error", a), n ? n.parentNode.insertBefore(e, n.nextSibling) : (l = l.nodeType === 9 ? l.head : l, l.insertBefore(e, l.firstChild)), t.state.loading |= 4;
    }
  }
  var ge = {
    $$typeof: Rl,
    Provider: null,
    Consumer: null,
    _currentValue: Y,
    _currentValue2: Y,
    _threadCount: 0
  };
  function bd(l, t, u, a, e, n, f, c, i) {
    this.tag = 1, this.containerInfo = l, this.pingCache = this.current = this.pendingChildren = null, this.timeoutHandle = -1, this.callbackNode = this.next = this.pendingContext = this.context = this.cancelPendingCommit = null, this.callbackPriority = 0, this.expirationTimes = lf(-1), this.entangledLanes = this.shellSuspendCounter = this.errorRecoveryDisabledLanes = this.expiredLanes = this.warmLanes = this.pingedLanes = this.suspendedLanes = this.pendingLanes = 0, this.entanglements = lf(0), this.hiddenUpdates = lf(null), this.identifierPrefix = a, this.onUncaughtError = e, this.onCaughtError = n, this.onRecoverableError = f, this.pooledCache = null, this.pooledCacheLanes = 0, this.formState = i, this.incompleteTransitions = /* @__PURE__ */ new Map();
  }
  function Sy(l, t, u, a, e, n, f, c, i, h, r, E) {
    return l = new bd(
      l,
      t,
      u,
      f,
      i,
      h,
      r,
      E,
      c
    ), t = 1, n === !0 && (t |= 24), n = Pl(3, null, null, t), l.current = n, n.stateNode = l, t = Xf(), t.refCount++, l.pooledCache = t, t.refCount++, n.memoizedState = {
      element: a,
      isDehydrated: u,
      cache: t
    }, xf(n), l;
  }
  function gy(l) {
    return l ? (l = ua, l) : ua;
  }
  function ry(l, t, u, a, e, n) {
    e = gy(e), a.context === null ? a.context = e : a.pendingContext = e, a = au(t), a.payload = { element: u }, n = n === void 0 ? null : n, n !== null && (a.callback = n), u = eu(l, a, t), u !== null && (Jl(u, l, t), $a(u, l, t));
  }
  function by(l, t) {
    if (l = l.memoizedState, l !== null && l.dehydrated !== null) {
      var u = l.retryLane;
      l.retryLane = u !== 0 && u < t ? u : t;
    }
  }
  function ci(l, t) {
    by(l, t), (l = l.alternate) && by(l, t);
  }
  function Ty(l) {
    if (l.tag === 13 || l.tag === 31) {
      var t = Du(l, 67108864);
      t !== null && Jl(t, l, 67108864), ci(l, 67108864);
    }
  }
  function Ey(l) {
    if (l.tag === 13 || l.tag === 31) {
      var t = et();
      t = tf(t);
      var u = Du(l, t);
      u !== null && Jl(u, l, t), ci(l, t);
    }
  }
  var jn = !0;
  function Td(l, t, u, a) {
    var e = b.T;
    b.T = null;
    var n = M.p;
    try {
      M.p = 2, ii(l, t, u, a);
    } finally {
      M.p = n, b.T = e;
    }
  }
  function Ed(l, t, u, a) {
    var e = b.T;
    b.T = null;
    var n = M.p;
    try {
      M.p = 8, ii(l, t, u, a);
    } finally {
      M.p = n, b.T = e;
    }
  }
  function ii(l, t, u, a) {
    if (jn) {
      var e = si(a);
      if (e === null)
        Wc(
          l,
          t,
          a,
          Xn,
          u
        ), Ay(l, a);
      else if (Ad(
        e,
        l,
        t,
        u,
        a
      ))
        a.stopPropagation();
      else if (Ay(l, a), t & 4 && -1 < zd.indexOf(l)) {
        for (; e !== null; ) {
          var n = Ku(e);
          if (n !== null)
            switch (n.tag) {
              case 3:
                if (n = n.stateNode, n.current.memoizedState.isDehydrated) {
                  var f = Au(n.pendingLanes);
                  if (f !== 0) {
                    var c = n;
                    for (c.pendingLanes |= 2, c.entangledLanes |= 2; f; ) {
                      var i = 1 << 31 - kl(f);
                      c.entanglements[1] |= i, f &= ~i;
                    }
                    pt(n), (I & 6) === 0 && (En = $l() + 500, ye(0));
                  }
                }
                break;
              case 31:
              case 13:
                c = Du(n, 2), c !== null && Jl(c, n, 2), An(), ci(n, 2);
            }
          if (n = si(a), n === null && Wc(
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
        Wc(
          l,
          t,
          a,
          null,
          u
        );
    }
  }
  function si(l) {
    return l = yf(l), oi(l);
  }
  var Xn = null;
  function oi(l) {
    if (Xn = null, l = Vu(l), l !== null) {
      var t = V(l);
      if (t === null) l = null;
      else {
        var u = t.tag;
        if (u === 13) {
          if (l = P(t), l !== null) return l;
          l = null;
        } else if (u === 31) {
          if (l = Al(t), l !== null) return l;
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
  function zy(l) {
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
        switch (cv()) {
          case Ui:
            return 2;
          case Ni:
            return 8;
          case pe:
          case iv:
            return 32;
          case Ri:
            return 268435456;
          default:
            return 32;
        }
      default:
        return 32;
    }
  }
  var yi = !1, hu = null, Su = null, gu = null, re = /* @__PURE__ */ new Map(), be = /* @__PURE__ */ new Map(), ru = [], zd = "mousedown mouseup touchcancel touchend touchstart auxclick dblclick pointercancel pointerdown pointerup dragend dragstart drop compositionend compositionstart keydown keypress keyup input textInput copy cut paste click change contextmenu reset".split(
    " "
  );
  function Ay(l, t) {
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
    }, t !== null && (t = Ku(t), t !== null && Ty(t)), l) : (l.eventSystemFlags |= a, t = l.targetContainers, e !== null && t.indexOf(e) === -1 && t.push(e), l);
  }
  function Ad(l, t, u, a, e) {
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
  function _y(l) {
    var t = Vu(l.target);
    if (t !== null) {
      var u = V(t);
      if (u !== null) {
        if (t = u.tag, t === 13) {
          if (t = P(u), t !== null) {
            l.blockedOn = t, Gi(l.priority, function() {
              Ey(u);
            });
            return;
          }
        } else if (t === 31) {
          if (t = Al(u), t !== null) {
            l.blockedOn = t, Gi(l.priority, function() {
              Ey(u);
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
      var u = si(l.nativeEvent);
      if (u === null) {
        u = l.nativeEvent;
        var a = new u.constructor(
          u.type,
          u
        );
        of = a, u.target.dispatchEvent(a), of = null;
      } else
        return t = Ku(u), t !== null && Ty(t), l.blockedOn = u, !1;
      t.shift();
    }
    return !0;
  }
  function Oy(l, t, u) {
    Qn(l) && u.delete(t);
  }
  function _d() {
    yi = !1, hu !== null && Qn(hu) && (hu = null), Su !== null && Qn(Su) && (Su = null), gu !== null && Qn(gu) && (gu = null), re.forEach(Oy), be.forEach(Oy);
  }
  function Zn(l, t) {
    l.blockedOn === t && (l.blockedOn = null, yi || (yi = !0, v.unstable_scheduleCallback(
      v.unstable_NormalPriority,
      _d
    )));
  }
  var Ln = null;
  function My(l) {
    Ln !== l && (Ln = l, v.unstable_scheduleCallback(
      v.unstable_NormalPriority,
      function() {
        Ln === l && (Ln = null);
        for (var t = 0; t < l.length; t += 3) {
          var u = l[t], a = l[t + 1], e = l[t + 2];
          if (typeof a != "function") {
            if (oi(a || u) === null)
              continue;
            break;
          }
          var n = Ku(u);
          n !== null && (l.splice(t, 3), t -= 3, sc(
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
      return Zn(i, l);
    }
    hu !== null && Zn(hu, l), Su !== null && Zn(Su, l), gu !== null && Zn(gu, l), re.forEach(t), be.forEach(t);
    for (var u = 0; u < ru.length; u++) {
      var a = ru[u];
      a.blockedOn === l && (a.blockedOn = null);
    }
    for (; 0 < ru.length && (u = ru[0], u.blockedOn === null); )
      _y(u), u.blockedOn === null && ru.shift();
    if (u = (l.ownerDocument || l).$$reactFormReplay, u != null)
      for (a = 0; a < u.length; a += 3) {
        var e = u[a], n = u[a + 1], f = e[Ql] || null;
        if (typeof n == "function")
          f || My(u);
        else if (f) {
          var c = null;
          if (n && n.hasAttribute("formAction")) {
            if (e = n, f = n[Ql] || null)
              c = f.formAction;
            else if (oi(e) !== null) continue;
          } else c = f.action;
          typeof c == "function" ? u[a + 1] = c : (u.splice(a, 3), a -= 3), My(u);
        }
      }
  }
  function py() {
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
  function vi(l) {
    this._internalRoot = l;
  }
  xn.prototype.render = vi.prototype.render = function(l) {
    var t = this._internalRoot;
    if (t === null) throw Error(m(409));
    var u = t.current, a = et();
    ry(u, a, l, t, null, null);
  }, xn.prototype.unmount = vi.prototype.unmount = function() {
    var l = this._internalRoot;
    if (l !== null) {
      this._internalRoot = null;
      var t = l.containerInfo;
      ry(l.current, 2, null, l, null, null), An(), t[xu] = null;
    }
  };
  function xn(l) {
    this._internalRoot = l;
  }
  xn.prototype.unstable_scheduleHydration = function(l) {
    if (l) {
      var t = Yi();
      l = { blockedOn: null, target: l, priority: t };
      for (var u = 0; u < ru.length && t !== 0 && t < ru[u].priority; u++) ;
      ru.splice(u, 0, l), u === 0 && _y(l);
    }
  };
  var Dy = A.version;
  if (Dy !== "19.2.4")
    throw Error(
      m(
        527,
        Dy,
        "19.2.4"
      )
    );
  M.findDOMNode = function(l) {
    var t = l._reactInternals;
    if (t === void 0)
      throw typeof l.render == "function" ? Error(m(188)) : (l = Object.keys(l).join(","), Error(m(268, l)));
    return l = _(t), l = l !== null ? $(l) : null, l = l === null ? null : l.stateNode, l;
  };
  var Od = {
    bundleType: 0,
    version: "19.2.4",
    rendererPackageName: "react-dom",
    currentDispatcherRef: b,
    reconcilerVersion: "19.2.4"
  };
  if (typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ < "u") {
    var Vn = __REACT_DEVTOOLS_GLOBAL_HOOK__;
    if (!Vn.isDisabled && Vn.supportsFiber)
      try {
        Ua = Vn.inject(
          Od
        ), Fl = Vn;
      } catch {
      }
  }
  return ze.createRoot = function(l, t) {
    if (!q(l)) throw Error(m(299));
    var u = !1, a = "", e = Bo, n = Yo, f = Go;
    return t != null && (t.unstable_strictMode === !0 && (u = !0), t.identifierPrefix !== void 0 && (a = t.identifierPrefix), t.onUncaughtError !== void 0 && (e = t.onUncaughtError), t.onCaughtError !== void 0 && (n = t.onCaughtError), t.onRecoverableError !== void 0 && (f = t.onRecoverableError)), t = Sy(
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
      py
    ), l[xu] = t.current, wc(l), new vi(t);
  }, ze.hydrateRoot = function(l, t, u) {
    if (!q(l)) throw Error(m(299));
    var a = !1, e = "", n = Bo, f = Yo, c = Go, i = null;
    return u != null && (u.unstable_strictMode === !0 && (a = !0), u.identifierPrefix !== void 0 && (e = u.identifierPrefix), u.onUncaughtError !== void 0 && (n = u.onUncaughtError), u.onCaughtError !== void 0 && (f = u.onCaughtError), u.onRecoverableError !== void 0 && (c = u.onRecoverableError), u.formState !== void 0 && (i = u.formState)), t = Sy(
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
      py
    ), t.context = gy(null), u = t.current, a = et(), a = tf(a), e = au(a), e.callback = null, eu(u, e, a), u = a, t.current.lanes = u, Ra(t, u), pt(t), l[xu] = t.current, wc(l), new xn(t);
  }, ze.version = "19.2.4", ze;
}
var jy;
function Kd() {
  if (jy) return hi.exports;
  jy = 1;
  function v() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(v);
      } catch (A) {
        console.error(A);
      }
  }
  return v(), hi.exports = Vd(), hi.exports;
}
var Jd = Kd(), Py = Iy();
const t1 = /* @__PURE__ */ Ky(Py);
class lv {
  constructor(A) {
    this._subscribers = /* @__PURE__ */ new Set(), this.getSnapshot = () => this._state, this.subscribeStore = (O) => (this._subscribers.add(O), () => this._subscribers.delete(O)), this._state = { ...A };
  }
  replaceState(A) {
    this._state = { ...A }, this._notify();
  }
  applyPatch(A) {
    this._state = { ...this._state, ...A }, this._notify();
  }
  _notify() {
    for (const A of this._subscribers)
      A();
  }
}
const pa = nt.createContext(null), Zu = /* @__PURE__ */ new Map();
let tv = "", Xy = !1;
function Oe() {
  return tv + "/";
}
function uv(v, A, O, m, q) {
  q !== void 0 && (tv = q), Xy || (Xy = !0, Dd(Oe()), Yd(Oe() + "react-api/events"));
  const V = m ?? "";
  Ud(V);
  const P = document.getElementById(v);
  if (!P) {
    console.error("[TLReact] Mount point not found:", v);
    return;
  }
  const Al = Vy(A);
  if (!Al) {
    console.error("[TLReact] Component not registered:", A);
    return;
  }
  Ai(v);
  const R = new lv(O);
  O.hidden === !0 && (P.style.display = "none");
  const _ = (ll) => {
    R.applyPatch(ll);
  };
  Fy(v, _);
  const $ = Jd.createRoot(P);
  Zu.set(v, { root: $, store: R, sseListener: _ }), av = V;
  const B = () => {
    const ll = nt.useSyncExternalStore(R.subscribeStore, R.getSnapshot);
    return nt.useLayoutEffect(() => {
      P.style.display = ll.hidden === !0 ? "none" : "";
    }, [ll.hidden]), Kn.createElement(
      pa.Provider,
      { value: { controlId: v, windowName: V, store: R } },
      Kn.createElement(Al, { controlId: v, state: ll })
    );
  };
  Py.flushSync(() => {
    $.render(Kn.createElement(B));
  });
}
function wd(v, A, O) {
  uv(v, A, O);
}
function Ai(v) {
  const A = Zu.get(v);
  A && (Gd(v, A.sseListener), A.root && A.root.unmount(), Zu.delete(v));
}
function Wd(v, A) {
  let O = Zu.get(v);
  if (!O) {
    const q = new lv(A), V = (P) => {
      q.applyPatch(P);
    };
    Fy(v, V), O = { root: null, store: q, sseListener: V }, Zu.set(v, O);
  }
  return { controlId: v, windowName: av, store: O.store };
}
let av = "";
function $d() {
  const v = nt.useContext(pa);
  if (!v)
    throw new Error("useTLState must be used inside a TLReact-mounted component.");
  return nt.useSyncExternalStore(v.store.subscribeStore, v.store.getSnapshot);
}
function Fd() {
  const v = nt.useContext(pa);
  if (!v)
    throw new Error("useTLCommand must be used inside a TLReact-mounted component.");
  const A = v.controlId, O = v.windowName;
  return nt.useCallback(
    async (m, q) => {
      const V = JSON.stringify({
        controlId: A,
        command: m,
        windowName: O,
        arguments: q ?? {}
      });
      try {
        const P = await fetch(Oe() + "react-api/command", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: V
        });
        P.ok || console.error("[TLReact] Command failed:", P.status, await P.text());
      } catch (P) {
        console.error("[TLReact] Command error:", P);
      }
    },
    [A, O]
  );
}
function u1() {
  const v = nt.useContext(pa);
  if (!v)
    throw new Error("useTLUpload must be used inside a TLReact-mounted component.");
  const A = v.controlId, O = v.windowName;
  return nt.useCallback(
    async (m) => {
      m.append("controlId", A), m.append("windowName", O);
      try {
        const q = await fetch(Oe() + "react-api/upload", {
          method: "POST",
          body: m
        });
        q.ok || console.error("[TLReact] Upload failed:", q.status, await q.text());
      } catch (q) {
        console.error("[TLReact] Upload error:", q);
      }
    },
    [A, O]
  );
}
function a1() {
  const v = nt.useContext(pa);
  if (!v)
    throw new Error("useTLDataUrl must be used inside a TLReact-mounted component.");
  return Oe() + "react-api/data?controlId=" + encodeURIComponent(v.controlId);
}
function e1() {
  const v = $d(), A = Fd(), O = nt.useCallback(
    (m) => {
      A("valueChanged", { value: m });
    },
    [A]
  );
  return [v.value, O];
}
function Qy() {
  new MutationObserver((A) => {
    for (const O of A)
      for (const m of O.removedNodes)
        if (m instanceof HTMLElement) {
          const q = m.id;
          q && Zu.has(q) && Ai(q);
          for (const [V] of Zu)
            m.querySelector("#" + CSS.escape(V)) && Ai(V);
        }
  }).observe(document.body, { childList: !0, subtree: !0 });
}
document.readyState === "loading" ? document.addEventListener("DOMContentLoaded", () => {
  Qy();
}) : Qy();
var bi = { exports: {} }, Ae = {};
/**
 * @license React
 * react-jsx-runtime.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var Zy;
function kd() {
  if (Zy) return Ae;
  Zy = 1;
  var v = Symbol.for("react.transitional.element"), A = Symbol.for("react.fragment");
  function O(m, q, V) {
    var P = null;
    if (V !== void 0 && (P = "" + V), q.key !== void 0 && (P = "" + q.key), "key" in q) {
      V = {};
      for (var Al in q)
        Al !== "key" && (V[Al] = q[Al]);
    } else V = q;
    return q = V.ref, {
      $$typeof: v,
      type: m,
      key: P,
      ref: q !== void 0 ? q : null,
      props: V
    };
  }
  return Ae.Fragment = A, Ae.jsx = O, Ae.jsxs = O, Ae;
}
var Ly;
function Id() {
  return Ly || (Ly = 1, bi.exports = kd()), bi.exports;
}
var Ti = Id();
const n1 = ({ control: v }) => {
  const A = v, O = Vy(A.module), m = nt.useMemo(
    () => Wd(A.controlId, A.state),
    [A.controlId]
  );
  return O ? /* @__PURE__ */ Ti.jsx(pa.Provider, { value: m, children: /* @__PURE__ */ Ti.jsx(O, { controlId: A.controlId, state: A.state }) }) : /* @__PURE__ */ Ti.jsxs("span", { children: [
    "[Component not registered: ",
    A.module,
    "]"
  ] });
};
window.TLReact = { mount: uv, mountField: wd };
export {
  Kn as React,
  t1 as ReactDOM,
  n1 as TLChild,
  pa as TLControlContext,
  Yd as connect,
  Wd as createChildContext,
  Vy as getComponent,
  uv as mount,
  wd as mountField,
  Pd as register,
  Fy as subscribe,
  Ai as unmount,
  Gd as unsubscribe,
  l1 as useI18N,
  Fd as useTLCommand,
  a1 as useTLDataUrl,
  e1 as useTLFieldValue,
  $d as useTLState,
  u1 as useTLUpload
};
