const Jy = /* @__PURE__ */ new Map();
function l1(y, A) {
  Jy.set(y, A);
}
function wy(y) {
  return Jy.get(y);
}
function Wy(y) {
  return y && y.__esModule && Object.prototype.hasOwnProperty.call(y, "default") ? y.default : y;
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
var Ry;
function Um() {
  if (Ry) return G;
  Ry = 1;
  var y = Symbol.for("react.transitional.element"), A = Symbol.for("react.portal"), _ = Symbol.for("react.fragment"), d = Symbol.for("react.strict_mode"), U = Symbol.for("react.profiler"), Z = Symbol.for("react.consumer"), K = Symbol.for("react.context"), rl = Symbol.for("react.forward_ref"), H = Symbol.for("react.suspense"), O = Symbol.for("react.memo"), F = Symbol.for("react.lazy"), B = Symbol.for("react.activity"), ll = Symbol.iterator;
  function Wl(o) {
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
  function $l(o, z, p) {
    this.props = o, this.context = z, this.refs = Dt, this.updater = p || Yl;
  }
  $l.prototype.isReactComponent = {}, $l.prototype.setState = function(o, z) {
    if (typeof o != "object" && typeof o != "function" && o != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, o, z, "setState");
  }, $l.prototype.forceUpdate = function(o) {
    this.updater.enqueueForceUpdate(this, o, "forceUpdate");
  };
  function Wt() {
  }
  Wt.prototype = $l.prototype;
  function Rl(o, z, p) {
    this.props = o, this.context = z, this.refs = Dt, this.updater = p || Yl;
  }
  var ft = Rl.prototype = new Wt();
  ft.constructor = Rl, Cl(ft, $l.prototype), ft.isPureReactComponent = !0;
  var Et = Array.isArray;
  function Gl() {
  }
  var W = { H: null, A: null, T: null, S: null }, jl = Object.prototype.hasOwnProperty;
  function zt(o, z, p) {
    var N = p.ref;
    return {
      $$typeof: y,
      type: o,
      key: z,
      ref: N !== void 0 ? N : null,
      props: p
    };
  }
  function Lu(o, z) {
    return zt(o.type, z, o.props);
  }
  function At(o) {
    return typeof o == "object" && o !== null && o.$$typeof === y;
  }
  function Xl(o) {
    var z = { "=": "=0", ":": "=2" };
    return "$" + o.replace(/[=:]/g, function(p) {
      return z[p];
    });
  }
  var zu = /\/+/g;
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
  function b(o, z, p, N, j) {
    var L = typeof o;
    (L === "undefined" || L === "boolean") && (o = null);
    var tl = !1;
    if (o === null) tl = !0;
    else
      switch (L) {
        case "bigint":
        case "string":
        case "number":
          tl = !0;
          break;
        case "object":
          switch (o.$$typeof) {
            case y:
            case A:
              tl = !0;
              break;
            case F:
              return tl = o._init, b(
                tl(o._payload),
                z,
                p,
                N,
                j
              );
          }
      }
    if (tl)
      return j = j(o), tl = N === "" ? "." + Ut(o, 0) : N, Et(j) ? (p = "", tl != null && (p = tl.replace(zu, "$&/") + "/"), b(j, z, p, "", function(Ua) {
        return Ua;
      })) : j != null && (At(j) && (j = Lu(
        j,
        p + (j.key == null || o && o.key === j.key ? "" : ("" + j.key).replace(
          zu,
          "$&/"
        ) + "/") + tl
      )), z.push(j)), 1;
    tl = 0;
    var ql = N === "" ? "." : N + ":";
    if (Et(o))
      for (var hl = 0; hl < o.length; hl++)
        N = o[hl], L = ql + Ut(N, hl), tl += b(
          N,
          z,
          p,
          L,
          j
        );
    else if (hl = Wl(o), typeof hl == "function")
      for (o = hl.call(o), hl = 0; !(N = o.next()).done; )
        N = N.value, L = ql + Ut(N, hl++), tl += b(
          N,
          z,
          p,
          L,
          j
        );
    else if (L === "object") {
      if (typeof o.then == "function")
        return b(
          gt(o),
          z,
          p,
          N,
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
    var N = [], j = 0;
    return b(o, N, "", "", function(L) {
      return z.call(p, L, j++);
    }), N;
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
  return G.Activity = B, G.Children = il, G.Component = $l, G.Fragment = _, G.Profiler = U, G.PureComponent = Rl, G.StrictMode = d, G.Suspense = H, G.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = W, G.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(o) {
      return W.H.useMemoCache(o);
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
    var N = Cl({}, o.props), j = o.key;
    if (z != null)
      for (L in z.key !== void 0 && (j = "" + z.key), z)
        !jl.call(z, L) || L === "key" || L === "__self" || L === "__source" || L === "ref" && z.ref === void 0 || (N[L] = z[L]);
    var L = arguments.length - 2;
    if (L === 1) N.children = p;
    else if (1 < L) {
      for (var tl = Array(L), ql = 0; ql < L; ql++)
        tl[ql] = arguments[ql + 2];
      N.children = tl;
    }
    return zt(o.type, j, N);
  }, G.createContext = function(o) {
    return o = {
      $$typeof: K,
      _currentValue: o,
      _currentValue2: o,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, o.Provider = o, o.Consumer = {
      $$typeof: Z,
      _context: o
    }, o;
  }, G.createElement = function(o, z, p) {
    var N, j = {}, L = null;
    if (z != null)
      for (N in z.key !== void 0 && (L = "" + z.key), z)
        jl.call(z, N) && N !== "key" && N !== "__self" && N !== "__source" && (j[N] = z[N]);
    var tl = arguments.length - 2;
    if (tl === 1) j.children = p;
    else if (1 < tl) {
      for (var ql = Array(tl), hl = 0; hl < tl; hl++)
        ql[hl] = arguments[hl + 2];
      j.children = ql;
    }
    if (o && o.defaultProps)
      for (N in tl = o.defaultProps, tl)
        j[N] === void 0 && (j[N] = tl[N]);
    return zt(o, L, j);
  }, G.createRef = function() {
    return { current: null };
  }, G.forwardRef = function(o) {
    return { $$typeof: rl, render: o };
  }, G.isValidElement = At, G.lazy = function(o) {
    return {
      $$typeof: F,
      _payload: { _status: -1, _result: o },
      _init: Y
    };
  }, G.memo = function(o, z) {
    return {
      $$typeof: O,
      type: o,
      compare: z === void 0 ? null : z
    };
  }, G.startTransition = function(o) {
    var z = W.T, p = {};
    W.T = p;
    try {
      var N = o(), j = W.S;
      j !== null && j(p, N), typeof N == "object" && N !== null && typeof N.then == "function" && N.then(Gl, el);
    } catch (L) {
      el(L);
    } finally {
      z !== null && p.types !== null && (z.types = p.types), W.T = z;
    }
  }, G.unstable_useCacheRefresh = function() {
    return W.H.useCacheRefresh();
  }, G.use = function(o) {
    return W.H.use(o);
  }, G.useActionState = function(o, z, p) {
    return W.H.useActionState(o, z, p);
  }, G.useCallback = function(o, z) {
    return W.H.useCallback(o, z);
  }, G.useContext = function(o) {
    return W.H.useContext(o);
  }, G.useDebugValue = function() {
  }, G.useDeferredValue = function(o, z) {
    return W.H.useDeferredValue(o, z);
  }, G.useEffect = function(o, z) {
    return W.H.useEffect(o, z);
  }, G.useEffectEvent = function(o) {
    return W.H.useEffectEvent(o);
  }, G.useId = function() {
    return W.H.useId();
  }, G.useImperativeHandle = function(o, z, p) {
    return W.H.useImperativeHandle(o, z, p);
  }, G.useInsertionEffect = function(o, z) {
    return W.H.useInsertionEffect(o, z);
  }, G.useLayoutEffect = function(o, z) {
    return W.H.useLayoutEffect(o, z);
  }, G.useMemo = function(o, z) {
    return W.H.useMemo(o, z);
  }, G.useOptimistic = function(o, z) {
    return W.H.useOptimistic(o, z);
  }, G.useReducer = function(o, z, p) {
    return W.H.useReducer(o, z, p);
  }, G.useRef = function(o) {
    return W.H.useRef(o);
  }, G.useState = function(o) {
    return W.H.useState(o);
  }, G.useSyncExternalStore = function(o, z, p) {
    return W.H.useSyncExternalStore(
      o,
      z,
      p
    );
  }, G.useTransition = function() {
    return W.H.useTransition();
  }, G.version = "19.2.4", G;
}
var Hy;
function Oi() {
  return Hy || (Hy = 1, mi.exports = Um()), mi.exports;
}
var wl = Oi();
const Kn = /* @__PURE__ */ Wy(wl), Mi = /* @__PURE__ */ new Map(), Jn = /* @__PURE__ */ new Set();
let zi = !1, pi = 0;
const Ai = /* @__PURE__ */ new Set();
let $y = "", Fy = "";
function Nm(y) {
  $y = y;
}
function Rm(y) {
  Fy = y;
}
function ky() {
  for (const y of Ai)
    y();
}
function Hm(y) {
  return Ai.add(y), () => Ai.delete(y);
}
function Cm() {
  return pi;
}
function qm(y) {
  Jn.add(y), zi || (zi = !0, queueMicrotask(Bm));
}
async function Bm() {
  if (zi = !1, Jn.size === 0)
    return;
  const y = Array.from(Jn);
  Jn.clear();
  try {
    const A = $y + "react-api/i18n?keys=" + encodeURIComponent(y.join(",")) + "&windowName=" + encodeURIComponent(Fy), _ = await fetch(A);
    if (!_.ok) {
      console.error("[TLReact] i18n fetch failed:", _.status);
      return;
    }
    const d = await _.json();
    for (const [U, Z] of Object.entries(d))
      Mi.set(U, Z);
    pi++, ky();
  } catch (A) {
    console.error("[TLReact] i18n fetch error:", A);
  }
}
function t1(y) {
  Kn.useSyncExternalStore(Hm, Cm);
  const A = {};
  for (const [_, d] of Object.entries(y)) {
    const U = Mi.get(_);
    U !== void 0 ? A[_] = U : (A[_] = d, qm(_));
  }
  return A;
}
function Iy() {
  Mi.clear(), pi++, ky();
}
const Oe = /* @__PURE__ */ new Map();
let ze = null, Cy = null, wn = 0, hi = null;
const Ym = 45e3, Gm = 15e3;
function qy(y) {
  Cy = y, hi && clearInterval(hi), By(y), hi = setInterval(() => {
    wn > 0 && Date.now() - wn > Ym && (console.warn("[TLReact] No heartbeat received, reconnecting SSE."), By(Cy));
  }, Gm);
}
function By(y) {
  ze && ze.close(), ze = new EventSource(y), wn = Date.now(), Iy(), ze.onmessage = (A) => {
    wn = Date.now();
    try {
      const _ = JSON.parse(A.data);
      Xm(_);
    } catch (_) {
      console.error("[TLReact] Failed to parse SSE event:", _);
    }
  }, ze.onerror = () => {
    console.warn("[TLReact] SSE connection error, will reconnect automatically.");
  };
}
function Py(y, A) {
  let _ = Oe.get(y);
  _ || (_ = /* @__PURE__ */ new Set(), Oe.set(y, _)), _.add(A);
}
function jm(y, A) {
  const _ = Oe.get(y);
  _ && (_.delete(A), _.size === 0 && Oe.delete(y));
}
function lv(y, A) {
  const _ = Oe.get(y);
  if (_)
    for (const d of _)
      d(A);
}
function Xm(y) {
  if (!Array.isArray(y) || y.length < 2) {
    console.warn("[TLReact] Unexpected SSE event format:", y);
    return;
  }
  const A = y[0], _ = y[1];
  switch (A) {
    case "Heartbeat":
      break;
    case "StateEvent":
      Qm(_);
      break;
    case "PatchEvent":
      Zm(_);
      break;
    case "ContentReplacement":
      Tu.contentReplacement(_);
      break;
    case "ElementReplacement":
      Tu.elementReplacement(_);
      break;
    case "PropertyUpdate":
      Tu.propertyUpdate(_);
      break;
    case "CssClassUpdate":
      Tu.cssClassUpdate(_);
      break;
    case "FragmentInsertion":
      Tu.fragmentInsertion(_);
      break;
    case "RangeReplacement":
      Tu.rangeReplacement(_);
      break;
    case "JSSnipplet":
      Tu.jsSnipplet(_);
      break;
    case "FunctionCall":
      Tu.functionCall(_);
      break;
    case "I18NCacheInvalidation":
      Iy();
      break;
    default:
      console.warn("[TLReact] Unknown SSE event type:", A);
  }
}
function Qm(y) {
  const A = JSON.parse(y.state);
  lv(y.controlId, A);
}
function Zm(y) {
  const A = JSON.parse(y.patch);
  lv(y.controlId, A);
}
const Tu = {
  contentReplacement(y) {
    const A = document.getElementById(y.elementId);
    A && (A.innerHTML = y.html);
  },
  elementReplacement(y) {
    const A = document.getElementById(y.elementId);
    A && (A.outerHTML = y.html);
  },
  propertyUpdate(y) {
    const A = document.getElementById(y.elementId);
    if (A)
      for (const _ of y.properties)
        A.setAttribute(_.name, _.value);
  },
  cssClassUpdate(y) {
    const A = document.getElementById(y.elementId);
    A && (A.className = y.cssClass);
  },
  fragmentInsertion(y) {
    const A = document.getElementById(y.elementId);
    A && A.insertAdjacentHTML(y.position, y.html);
  },
  rangeReplacement(y) {
    const A = document.getElementById(y.startId), _ = document.getElementById(y.stopId);
    if (A && _ && A.parentNode) {
      const d = A.parentNode, U = document.createRange();
      U.setStartBefore(A), U.setEndAfter(_), U.deleteContents();
      const Z = document.createElement("template");
      Z.innerHTML = y.html, d.insertBefore(Z.content, U.startContainer.childNodes[U.startOffset] || null);
    }
  },
  jsSnipplet(y) {
    try {
      (0, eval)(y.code);
    } catch (A) {
      console.error("[TLReact] Error executing JS snippet:", A);
    }
  },
  functionCall(y) {
    try {
      const A = JSON.parse(y.arguments), _ = y.functionRef ? window[y.functionRef] : window, d = _ == null ? void 0 : _[y.functionName];
      typeof d == "function" ? d.apply(_, A) : console.warn("[TLReact] Function not found:", y.functionRef + "." + y.functionName);
    } catch (A) {
      console.error("[TLReact] Error executing function call:", A);
    }
  }
};
var Si = { exports: {} }, Ae = {}, gi = { exports: {} }, ri = {};
/**
 * @license React
 * scheduler.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var Yy;
function Lm() {
  return Yy || (Yy = 1, (function(y) {
    function A(b, M) {
      var Y = b.length;
      b.push(M);
      l: for (; 0 < Y; ) {
        var el = Y - 1 >>> 1, il = b[el];
        if (0 < U(il, M))
          b[el] = M, b[Y] = il, Y = el;
        else break l;
      }
    }
    function _(b) {
      return b.length === 0 ? null : b[0];
    }
    function d(b) {
      if (b.length === 0) return null;
      var M = b[0], Y = b.pop();
      if (Y !== M) {
        b[0] = Y;
        l: for (var el = 0, il = b.length, o = il >>> 1; el < o; ) {
          var z = 2 * (el + 1) - 1, p = b[z], N = z + 1, j = b[N];
          if (0 > U(p, Y))
            N < il && 0 > U(j, p) ? (b[el] = j, b[N] = Y, el = N) : (b[el] = p, b[z] = Y, el = z);
          else if (N < il && 0 > U(j, Y))
            b[el] = j, b[N] = Y, el = N;
          else break l;
        }
      }
      return M;
    }
    function U(b, M) {
      var Y = b.sortIndex - M.sortIndex;
      return Y !== 0 ? Y : b.id - M.id;
    }
    if (y.unstable_now = void 0, typeof performance == "object" && typeof performance.now == "function") {
      var Z = performance;
      y.unstable_now = function() {
        return Z.now();
      };
    } else {
      var K = Date, rl = K.now();
      y.unstable_now = function() {
        return K.now() - rl;
      };
    }
    var H = [], O = [], F = 1, B = null, ll = 3, Wl = !1, Yl = !1, Cl = !1, Dt = !1, $l = typeof setTimeout == "function" ? setTimeout : null, Wt = typeof clearTimeout == "function" ? clearTimeout : null, Rl = typeof setImmediate < "u" ? setImmediate : null;
    function ft(b) {
      for (var M = _(O); M !== null; ) {
        if (M.callback === null) d(O);
        else if (M.startTime <= b)
          d(O), M.sortIndex = M.expirationTime, A(H, M);
        else break;
        M = _(O);
      }
    }
    function Et(b) {
      if (Cl = !1, ft(b), !Yl)
        if (_(H) !== null)
          Yl = !0, Gl || (Gl = !0, Xl());
        else {
          var M = _(O);
          M !== null && gt(Et, M.startTime - b);
        }
    }
    var Gl = !1, W = -1, jl = 5, zt = -1;
    function Lu() {
      return Dt ? !0 : !(y.unstable_now() - zt < jl);
    }
    function At() {
      if (Dt = !1, Gl) {
        var b = y.unstable_now();
        zt = b;
        var M = !0;
        try {
          l: {
            Yl = !1, Cl && (Cl = !1, Wt(W), W = -1), Wl = !0;
            var Y = ll;
            try {
              t: {
                for (ft(b), B = _(H); B !== null && !(B.expirationTime > b && Lu()); ) {
                  var el = B.callback;
                  if (typeof el == "function") {
                    B.callback = null, ll = B.priorityLevel;
                    var il = el(
                      B.expirationTime <= b
                    );
                    if (b = y.unstable_now(), typeof il == "function") {
                      B.callback = il, ft(b), M = !0;
                      break t;
                    }
                    B === _(H) && d(H), ft(b);
                  } else d(H);
                  B = _(H);
                }
                if (B !== null) M = !0;
                else {
                  var o = _(O);
                  o !== null && gt(
                    Et,
                    o.startTime - b
                  ), M = !1;
                }
              }
              break l;
            } finally {
              B = null, ll = Y, Wl = !1;
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
      var zu = new MessageChannel(), Ut = zu.port2;
      zu.port1.onmessage = At, Xl = function() {
        Ut.postMessage(null);
      };
    } else
      Xl = function() {
        $l(At, 0);
      };
    function gt(b, M) {
      W = $l(function() {
        b(y.unstable_now());
      }, M);
    }
    y.unstable_IdlePriority = 5, y.unstable_ImmediatePriority = 1, y.unstable_LowPriority = 4, y.unstable_NormalPriority = 3, y.unstable_Profiling = null, y.unstable_UserBlockingPriority = 2, y.unstable_cancelCallback = function(b) {
      b.callback = null;
    }, y.unstable_forceFrameRate = function(b) {
      0 > b || 125 < b ? console.error(
        "forceFrameRate takes a positive int between 0 and 125, forcing frame rates higher than 125 fps is not supported"
      ) : jl = 0 < b ? Math.floor(1e3 / b) : 5;
    }, y.unstable_getCurrentPriorityLevel = function() {
      return ll;
    }, y.unstable_next = function(b) {
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
    }, y.unstable_requestPaint = function() {
      Dt = !0;
    }, y.unstable_runWithPriority = function(b, M) {
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
    }, y.unstable_scheduleCallback = function(b, M, Y) {
      var el = y.unstable_now();
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
        id: F++,
        callback: M,
        priorityLevel: b,
        startTime: Y,
        expirationTime: il,
        sortIndex: -1
      }, Y > el ? (b.sortIndex = Y, A(O, b), _(H) === null && b === _(O) && (Cl ? (Wt(W), W = -1) : Cl = !0, gt(Et, Y - el))) : (b.sortIndex = il, A(H, b), Yl || Wl || (Yl = !0, Gl || (Gl = !0, Xl()))), b;
    }, y.unstable_shouldYield = Lu, y.unstable_wrapCallback = function(b) {
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
  })(ri)), ri;
}
var Gy;
function xm() {
  return Gy || (Gy = 1, gi.exports = Lm()), gi.exports;
}
var bi = { exports: {} }, Hl = {};
/**
 * @license React
 * react-dom.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var jy;
function Vm() {
  if (jy) return Hl;
  jy = 1;
  var y = Oi();
  function A(H) {
    var O = "https://react.dev/errors/" + H;
    if (1 < arguments.length) {
      O += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var F = 2; F < arguments.length; F++)
        O += "&args[]=" + encodeURIComponent(arguments[F]);
    }
    return "Minified React error #" + H + "; visit " + O + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function _() {
  }
  var d = {
    d: {
      f: _,
      r: function() {
        throw Error(A(522));
      },
      D: _,
      C: _,
      L: _,
      m: _,
      X: _,
      S: _,
      M: _
    },
    p: 0,
    findDOMNode: null
  }, U = Symbol.for("react.portal");
  function Z(H, O, F) {
    var B = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: U,
      key: B == null ? null : "" + B,
      children: H,
      containerInfo: O,
      implementation: F
    };
  }
  var K = y.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function rl(H, O) {
    if (H === "font") return "";
    if (typeof O == "string")
      return O === "use-credentials" ? O : "";
  }
  return Hl.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = d, Hl.createPortal = function(H, O) {
    var F = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!O || O.nodeType !== 1 && O.nodeType !== 9 && O.nodeType !== 11)
      throw Error(A(299));
    return Z(H, O, null, F);
  }, Hl.flushSync = function(H) {
    var O = K.T, F = d.p;
    try {
      if (K.T = null, d.p = 2, H) return H();
    } finally {
      K.T = O, d.p = F, d.d.f();
    }
  }, Hl.preconnect = function(H, O) {
    typeof H == "string" && (O ? (O = O.crossOrigin, O = typeof O == "string" ? O === "use-credentials" ? O : "" : void 0) : O = null, d.d.C(H, O));
  }, Hl.prefetchDNS = function(H) {
    typeof H == "string" && d.d.D(H);
  }, Hl.preinit = function(H, O) {
    if (typeof H == "string" && O && typeof O.as == "string") {
      var F = O.as, B = rl(F, O.crossOrigin), ll = typeof O.integrity == "string" ? O.integrity : void 0, Wl = typeof O.fetchPriority == "string" ? O.fetchPriority : void 0;
      F === "style" ? d.d.S(
        H,
        typeof O.precedence == "string" ? O.precedence : void 0,
        {
          crossOrigin: B,
          integrity: ll,
          fetchPriority: Wl
        }
      ) : F === "script" && d.d.X(H, {
        crossOrigin: B,
        integrity: ll,
        fetchPriority: Wl,
        nonce: typeof O.nonce == "string" ? O.nonce : void 0
      });
    }
  }, Hl.preinitModule = function(H, O) {
    if (typeof H == "string")
      if (typeof O == "object" && O !== null) {
        if (O.as == null || O.as === "script") {
          var F = rl(
            O.as,
            O.crossOrigin
          );
          d.d.M(H, {
            crossOrigin: F,
            integrity: typeof O.integrity == "string" ? O.integrity : void 0,
            nonce: typeof O.nonce == "string" ? O.nonce : void 0
          });
        }
      } else O == null && d.d.M(H);
  }, Hl.preload = function(H, O) {
    if (typeof H == "string" && typeof O == "object" && O !== null && typeof O.as == "string") {
      var F = O.as, B = rl(F, O.crossOrigin);
      d.d.L(H, F, {
        crossOrigin: B,
        integrity: typeof O.integrity == "string" ? O.integrity : void 0,
        nonce: typeof O.nonce == "string" ? O.nonce : void 0,
        type: typeof O.type == "string" ? O.type : void 0,
        fetchPriority: typeof O.fetchPriority == "string" ? O.fetchPriority : void 0,
        referrerPolicy: typeof O.referrerPolicy == "string" ? O.referrerPolicy : void 0,
        imageSrcSet: typeof O.imageSrcSet == "string" ? O.imageSrcSet : void 0,
        imageSizes: typeof O.imageSizes == "string" ? O.imageSizes : void 0,
        media: typeof O.media == "string" ? O.media : void 0
      });
    }
  }, Hl.preloadModule = function(H, O) {
    if (typeof H == "string")
      if (O) {
        var F = rl(O.as, O.crossOrigin);
        d.d.m(H, {
          as: typeof O.as == "string" && O.as !== "script" ? O.as : void 0,
          crossOrigin: F,
          integrity: typeof O.integrity == "string" ? O.integrity : void 0
        });
      } else d.d.m(H);
  }, Hl.requestFormReset = function(H) {
    d.d.r(H);
  }, Hl.unstable_batchedUpdates = function(H, O) {
    return H(O);
  }, Hl.useFormState = function(H, O, F) {
    return K.H.useFormState(H, O, F);
  }, Hl.useFormStatus = function() {
    return K.H.useHostTransitionStatus();
  }, Hl.version = "19.2.4", Hl;
}
var Xy;
function tv() {
  if (Xy) return bi.exports;
  Xy = 1;
  function y() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(y);
      } catch (A) {
        console.error(A);
      }
  }
  return y(), bi.exports = Vm(), bi.exports;
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
var Qy;
function Km() {
  if (Qy) return Ae;
  Qy = 1;
  var y = xm(), A = Oi(), _ = tv();
  function d(l) {
    var t = "https://react.dev/errors/" + l;
    if (1 < arguments.length) {
      t += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var u = 2; u < arguments.length; u++)
        t += "&args[]=" + encodeURIComponent(arguments[u]);
    }
    return "Minified React error #" + l + "; visit " + t + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function U(l) {
    return !(!l || l.nodeType !== 1 && l.nodeType !== 9 && l.nodeType !== 11);
  }
  function Z(l) {
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
  function K(l) {
    if (l.tag === 13) {
      var t = l.memoizedState;
      if (t === null && (l = l.alternate, l !== null && (t = l.memoizedState)), t !== null) return t.dehydrated;
    }
    return null;
  }
  function rl(l) {
    if (l.tag === 31) {
      var t = l.memoizedState;
      if (t === null && (l = l.alternate, l !== null && (t = l.memoizedState)), t !== null) return t.dehydrated;
    }
    return null;
  }
  function H(l) {
    if (Z(l) !== l)
      throw Error(d(188));
  }
  function O(l) {
    var t = l.alternate;
    if (!t) {
      if (t = Z(l), t === null) throw Error(d(188));
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
        throw Error(d(188));
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
          if (!f) throw Error(d(189));
        }
      }
      if (u.alternate !== a) throw Error(d(190));
    }
    if (u.tag !== 3) throw Error(d(188));
    return u.stateNode.current === u ? l : t;
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
  var B = Object.assign, ll = Symbol.for("react.element"), Wl = Symbol.for("react.transitional.element"), Yl = Symbol.for("react.portal"), Cl = Symbol.for("react.fragment"), Dt = Symbol.for("react.strict_mode"), $l = Symbol.for("react.profiler"), Wt = Symbol.for("react.consumer"), Rl = Symbol.for("react.context"), ft = Symbol.for("react.forward_ref"), Et = Symbol.for("react.suspense"), Gl = Symbol.for("react.suspense_list"), W = Symbol.for("react.memo"), jl = Symbol.for("react.lazy"), zt = Symbol.for("react.activity"), Lu = Symbol.for("react.memo_cache_sentinel"), At = Symbol.iterator;
  function Xl(l) {
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
        case Yl:
          return "Portal";
        case Rl:
          return l.displayName || "Context";
        case Wt:
          return (l._context.displayName || "Context") + ".Consumer";
        case ft:
          var t = l.render;
          return l = l.displayName, l || (l = t.displayName || t.name || "", l = l !== "" ? "ForwardRef(" + l + ")" : "ForwardRef"), l;
        case W:
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
  var gt = Array.isArray, b = A.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE, M = _.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE, Y = {
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
  var N = o(null), j = o(null), L = o(null), tl = o(null);
  function ql(l, t) {
    switch (p(L, t), p(j, l), p(N, null), t.nodeType) {
      case 9:
      case 11:
        l = (l = t.documentElement) && (l = l.namespaceURI) ? ly(l) : 0;
        break;
      default:
        if (l = t.tagName, t = t.namespaceURI)
          t = ly(t), l = ty(t, l);
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
    z(N), p(N, l);
  }
  function hl() {
    z(N), z(j), z(L);
  }
  function Ua(l) {
    l.memoizedState !== null && p(tl, l);
    var t = N.current, u = ty(t, l.type);
    t !== u && (p(j, l), p(N, u));
  }
  function Me(l) {
    j.current === l && (z(N), z(j)), tl.current === l && (z(tl), re._currentValue = Y);
  }
  var $n, Ui;
  function Au(l) {
    if ($n === void 0)
      try {
        throw Error();
      } catch (u) {
        var t = u.stack.trim().match(/\n( *(at )?)/);
        $n = t && t[1] || "", Ui = -1 < u.stack.indexOf(`
    at`) ? " (<anonymous>)" : -1 < u.stack.indexOf("@") ? "@unknown:0:0" : "";
      }
    return `
` + $n + l + Ui;
  }
  var Fn = !1;
  function kn(l, t) {
    if (!l || Fn) return "";
    Fn = !0;
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
      Fn = !1, Error.prepareStackTrace = u;
    }
    return (u = l ? l.displayName || l.name : "") ? Au(u) : "";
  }
  function fv(l, t) {
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
        return kn(l.type, !1);
      case 11:
        return kn(l.type.render, !1);
      case 1:
        return kn(l.type, !0);
      case 31:
        return Au("Activity");
      default:
        return "";
    }
  }
  function Ni(l) {
    try {
      var t = "", u = null;
      do
        t += fv(l, u), u = l, l = l.return;
      while (l);
      return t;
    } catch (a) {
      return `
Error generating stack: ` + a.message + `
` + a.stack;
    }
  }
  var In = Object.prototype.hasOwnProperty, Pn = y.unstable_scheduleCallback, lf = y.unstable_cancelCallback, cv = y.unstable_shouldYield, iv = y.unstable_requestPaint, Fl = y.unstable_now, sv = y.unstable_getCurrentPriorityLevel, Ri = y.unstable_ImmediatePriority, Hi = y.unstable_UserBlockingPriority, pe = y.unstable_NormalPriority, ov = y.unstable_LowPriority, Ci = y.unstable_IdlePriority, yv = y.log, vv = y.unstable_setDisableYieldValue, Na = null, kl = null;
  function $t(l) {
    if (typeof yv == "function" && vv(l), kl && typeof kl.setStrictMode == "function")
      try {
        kl.setStrictMode(Na, l);
      } catch {
      }
  }
  var Il = Math.clz32 ? Math.clz32 : hv, dv = Math.log, mv = Math.LN2;
  function hv(l) {
    return l >>>= 0, l === 0 ? 32 : 31 - (dv(l) / mv | 0) | 0;
  }
  var De = 256, Ue = 262144, Ne = 4194304;
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
  function Sv(l, t) {
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
  function qi() {
    var l = Ne;
    return Ne <<= 1, (Ne & 62914560) === 0 && (Ne = 4194304), l;
  }
  function tf(l) {
    for (var t = [], u = 0; 31 > u; u++) t.push(l);
    return t;
  }
  function Ha(l, t) {
    l.pendingLanes |= t, t !== 268435456 && (l.suspendedLanes = 0, l.pingedLanes = 0, l.warmLanes = 0);
  }
  function gv(l, t, u, a, e, n) {
    var f = l.pendingLanes;
    l.pendingLanes = u, l.suspendedLanes = 0, l.pingedLanes = 0, l.warmLanes = 0, l.expiredLanes &= u, l.entangledLanes &= u, l.errorRecoveryDisabledLanes &= u, l.shellSuspendCounter = 0;
    var c = l.entanglements, i = l.expirationTimes, h = l.hiddenUpdates;
    for (u = f & ~u; 0 < u; ) {
      var r = 31 - Il(u), E = 1 << r;
      c[r] = 0, i[r] = -1;
      var S = h[r];
      if (S !== null)
        for (h[r] = null, r = 0; r < S.length; r++) {
          var g = S[r];
          g !== null && (g.lane &= -536870913);
        }
      u &= ~E;
    }
    a !== 0 && Bi(l, a, 0), n !== 0 && e === 0 && l.tag !== 0 && (l.suspendedLanes |= n & ~(f & ~t));
  }
  function Bi(l, t, u) {
    l.pendingLanes |= t, l.suspendedLanes &= ~t;
    var a = 31 - Il(t);
    l.entangledLanes |= t, l.entanglements[a] = l.entanglements[a] | 1073741824 | u & 261930;
  }
  function Yi(l, t) {
    var u = l.entangledLanes |= t;
    for (l = l.entanglements; u; ) {
      var a = 31 - Il(u), e = 1 << a;
      e & t | l[a] & t && (l[a] |= t), u &= ~e;
    }
  }
  function Gi(l, t) {
    var u = t & -t;
    return u = (u & 42) !== 0 ? 1 : uf(u), (u & (l.suspendedLanes | t)) !== 0 ? 0 : u;
  }
  function uf(l) {
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
  function af(l) {
    return l &= -l, 2 < l ? 8 < l ? (l & 134217727) !== 0 ? 32 : 268435456 : 8 : 2;
  }
  function ji() {
    var l = M.p;
    return l !== 0 ? l : (l = window.event, l === void 0 ? 32 : _y(l.type));
  }
  function Xi(l, t) {
    var u = M.p;
    try {
      return M.p = l, t();
    } finally {
      M.p = u;
    }
  }
  var Ft = Math.random().toString(36).slice(2), Ml = "__reactFiber$" + Ft, Ql = "__reactProps$" + Ft, xu = "__reactContainer$" + Ft, ef = "__reactEvents$" + Ft, rv = "__reactListeners$" + Ft, bv = "__reactHandles$" + Ft, Qi = "__reactResources$" + Ft, Ca = "__reactMarker$" + Ft;
  function nf(l) {
    delete l[Ml], delete l[Ql], delete l[ef], delete l[rv], delete l[bv];
  }
  function Vu(l) {
    var t = l[Ml];
    if (t) return t;
    for (var u = l.parentNode; u; ) {
      if (t = u[xu] || u[Ml]) {
        if (u = t.alternate, t.child !== null || u !== null && u.child !== null)
          for (l = iy(l); l !== null; ) {
            if (u = l[Ml]) return u;
            l = iy(l);
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
  function qa(l) {
    var t = l.tag;
    if (t === 5 || t === 26 || t === 27 || t === 6) return l.stateNode;
    throw Error(d(33));
  }
  function Ju(l) {
    var t = l[Qi];
    return t || (t = l[Qi] = { hoistableStyles: /* @__PURE__ */ new Map(), hoistableScripts: /* @__PURE__ */ new Map() }), t;
  }
  function _l(l) {
    l[Ca] = !0;
  }
  var Zi = /* @__PURE__ */ new Set(), Li = {};
  function Ou(l, t) {
    wu(l, t), wu(l + "Capture", t);
  }
  function wu(l, t) {
    for (Li[l] = t, l = 0; l < t.length; l++)
      Zi.add(t[l]);
  }
  var Tv = RegExp(
    "^[:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD][:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD\\-.0-9\\u00B7\\u0300-\\u036F\\u203F-\\u2040]*$"
  ), xi = {}, Vi = {};
  function Ev(l) {
    return In.call(Vi, l) ? !0 : In.call(xi, l) ? !1 : Tv.test(l) ? Vi[l] = !0 : (xi[l] = !0, !1);
  }
  function He(l, t, u) {
    if (Ev(t))
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
  function Ki(l) {
    var t = l.type;
    return (l = l.nodeName) && l.toLowerCase() === "input" && (t === "checkbox" || t === "radio");
  }
  function zv(l, t, u) {
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
  function ff(l) {
    if (!l._valueTracker) {
      var t = Ki(l) ? "checked" : "value";
      l._valueTracker = zv(
        l,
        t,
        "" + l[t]
      );
    }
  }
  function Ji(l) {
    if (!l) return !1;
    var t = l._valueTracker;
    if (!t) return !0;
    var u = t.getValue(), a = "";
    return l && (a = Ki(l) ? l.checked ? "true" : "false" : l.value), l = a, l !== u ? (t.setValue(l), !0) : !1;
  }
  function qe(l) {
    if (l = l || (typeof document < "u" ? document : void 0), typeof l > "u") return null;
    try {
      return l.activeElement || l.body;
    } catch {
      return l.body;
    }
  }
  var Av = /[\n"\\]/g;
  function it(l) {
    return l.replace(
      Av,
      function(t) {
        return "\\" + t.charCodeAt(0).toString(16) + " ";
      }
    );
  }
  function cf(l, t, u, a, e, n, f, c) {
    l.name = "", f != null && typeof f != "function" && typeof f != "symbol" && typeof f != "boolean" ? l.type = f : l.removeAttribute("type"), t != null ? f === "number" ? (t === 0 && l.value === "" || l.value != t) && (l.value = "" + ct(t)) : l.value !== "" + ct(t) && (l.value = "" + ct(t)) : f !== "submit" && f !== "reset" || l.removeAttribute("value"), t != null ? sf(l, f, ct(t)) : u != null ? sf(l, f, ct(u)) : a != null && l.removeAttribute("value"), e == null && n != null && (l.defaultChecked = !!n), e != null && (l.checked = e && typeof e != "function" && typeof e != "symbol"), c != null && typeof c != "function" && typeof c != "symbol" && typeof c != "boolean" ? l.name = "" + ct(c) : l.removeAttribute("name");
  }
  function wi(l, t, u, a, e, n, f, c) {
    if (n != null && typeof n != "function" && typeof n != "symbol" && typeof n != "boolean" && (l.type = n), t != null || u != null) {
      if (!(n !== "submit" && n !== "reset" || t != null)) {
        ff(l);
        return;
      }
      u = u != null ? "" + ct(u) : "", t = t != null ? "" + ct(t) : u, c || t === l.value || (l.value = t), l.defaultValue = t;
    }
    a = a ?? e, a = typeof a != "function" && typeof a != "symbol" && !!a, l.checked = c ? l.checked : !!a, l.defaultChecked = !!a, f != null && typeof f != "function" && typeof f != "symbol" && typeof f != "boolean" && (l.name = f), ff(l);
  }
  function sf(l, t, u) {
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
  function Wi(l, t, u) {
    if (t != null && (t = "" + ct(t), t !== l.value && (l.value = t), u == null)) {
      l.defaultValue !== t && (l.defaultValue = t);
      return;
    }
    l.defaultValue = u != null ? "" + ct(u) : "";
  }
  function $i(l, t, u, a) {
    if (t == null) {
      if (a != null) {
        if (u != null) throw Error(d(92));
        if (gt(a)) {
          if (1 < a.length) throw Error(d(93));
          a = a[0];
        }
        u = a;
      }
      u == null && (u = ""), t = u;
    }
    u = ct(t), l.defaultValue = u, a = l.textContent, a === u && a !== "" && a !== null && (l.value = a), ff(l);
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
  var _v = new Set(
    "animationIterationCount aspectRatio borderImageOutset borderImageSlice borderImageWidth boxFlex boxFlexGroup boxOrdinalGroup columnCount columns flex flexGrow flexPositive flexShrink flexNegative flexOrder gridArea gridRow gridRowEnd gridRowSpan gridRowStart gridColumn gridColumnEnd gridColumnSpan gridColumnStart fontWeight lineClamp lineHeight opacity order orphans scale tabSize widows zIndex zoom fillOpacity floodOpacity stopOpacity strokeDasharray strokeDashoffset strokeMiterlimit strokeOpacity strokeWidth MozAnimationIterationCount MozBoxFlex MozBoxFlexGroup MozLineClamp msAnimationIterationCount msFlex msZoom msFlexGrow msFlexNegative msFlexOrder msFlexPositive msFlexShrink msGridColumn msGridColumnSpan msGridRow msGridRowSpan WebkitAnimationIterationCount WebkitBoxFlex WebKitBoxFlexGroup WebkitBoxOrdinalGroup WebkitColumnCount WebkitColumns WebkitFlex WebkitFlexGrow WebkitFlexPositive WebkitFlexShrink WebkitLineClamp".split(
      " "
    )
  );
  function Fi(l, t, u) {
    var a = t.indexOf("--") === 0;
    u == null || typeof u == "boolean" || u === "" ? a ? l.setProperty(t, "") : t === "float" ? l.cssFloat = "" : l[t] = "" : a ? l.setProperty(t, u) : typeof u != "number" || u === 0 || _v.has(t) ? t === "float" ? l.cssFloat = u : l[t] = ("" + u).trim() : l[t] = u + "px";
  }
  function ki(l, t, u) {
    if (t != null && typeof t != "object")
      throw Error(d(62));
    if (l = l.style, u != null) {
      for (var a in u)
        !u.hasOwnProperty(a) || t != null && t.hasOwnProperty(a) || (a.indexOf("--") === 0 ? l.setProperty(a, "") : a === "float" ? l.cssFloat = "" : l[a] = "");
      for (var e in t)
        a = t[e], t.hasOwnProperty(e) && u[e] !== a && Fi(l, e, a);
    } else
      for (var n in t)
        t.hasOwnProperty(n) && Fi(l, n, t[n]);
  }
  function of(l) {
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
  var Ov = /* @__PURE__ */ new Map([
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
  ]), Mv = /^[\u0000-\u001F ]*j[\r\n\t]*a[\r\n\t]*v[\r\n\t]*a[\r\n\t]*s[\r\n\t]*c[\r\n\t]*r[\r\n\t]*i[\r\n\t]*p[\r\n\t]*t[\r\n\t]*:/i;
  function Be(l) {
    return Mv.test("" + l) ? "javascript:throw new Error('React has blocked a javascript: URL as a security precaution.')" : l;
  }
  function Rt() {
  }
  var yf = null;
  function vf(l) {
    return l = l.target || l.srcElement || window, l.correspondingUseElement && (l = l.correspondingUseElement), l.nodeType === 3 ? l.parentNode : l;
  }
  var Fu = null, ku = null;
  function Ii(l) {
    var t = Ku(l);
    if (t && (l = t.stateNode)) {
      var u = l[Ql] || null;
      l: switch (l = t.stateNode, t.type) {
        case "input":
          if (cf(
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
                if (!e) throw Error(d(90));
                cf(
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
              a = u[t], a.form === l.form && Ji(a);
          }
          break l;
        case "textarea":
          Wi(l, u.value, u.defaultValue);
          break l;
        case "select":
          t = u.value, t != null && Wu(l, !!u.multiple, t, !1);
      }
    }
  }
  var df = !1;
  function Pi(l, t, u) {
    if (df) return l(t, u);
    df = !0;
    try {
      var a = l(t);
      return a;
    } finally {
      if (df = !1, (Fu !== null || ku !== null) && (An(), Fu && (t = Fu, l = ku, ku = Fu = null, Ii(t), l)))
        for (t = 0; t < l.length; t++) Ii(l[t]);
    }
  }
  function Ba(l, t) {
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
        d(231, t, typeof u)
      );
    return u;
  }
  var Ht = !(typeof window > "u" || typeof window.document > "u" || typeof window.document.createElement > "u"), mf = !1;
  if (Ht)
    try {
      var Ya = {};
      Object.defineProperty(Ya, "passive", {
        get: function() {
          mf = !0;
        }
      }), window.addEventListener("test", Ya, Ya), window.removeEventListener("test", Ya, Ya);
    } catch {
      mf = !1;
    }
  var kt = null, hf = null, Ye = null;
  function ls() {
    if (Ye) return Ye;
    var l, t = hf, u = t.length, a, e = "value" in kt ? kt.value : kt.textContent, n = e.length;
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
  function ts() {
    return !1;
  }
  function Zl(l) {
    function t(u, a, e, n, f) {
      this._reactName = u, this._targetInst = e, this.type = a, this.nativeEvent = n, this.target = f, this.currentTarget = null;
      for (var c in l)
        l.hasOwnProperty(c) && (u = l[c], this[c] = u ? u(n) : n[c]);
      return this.isDefaultPrevented = (n.defaultPrevented != null ? n.defaultPrevented : n.returnValue === !1) ? je : ts, this.isPropagationStopped = ts, this;
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
  var Mu = {
    eventPhase: 0,
    bubbles: 0,
    cancelable: 0,
    timeStamp: function(l) {
      return l.timeStamp || Date.now();
    },
    defaultPrevented: 0,
    isTrusted: 0
  }, Xe = Zl(Mu), Ga = B({}, Mu, { view: 0, detail: 0 }), pv = Zl(Ga), Sf, gf, ja, Qe = B({}, Ga, {
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
    getModifierState: bf,
    button: 0,
    buttons: 0,
    relatedTarget: function(l) {
      return l.relatedTarget === void 0 ? l.fromElement === l.srcElement ? l.toElement : l.fromElement : l.relatedTarget;
    },
    movementX: function(l) {
      return "movementX" in l ? l.movementX : (l !== ja && (ja && l.type === "mousemove" ? (Sf = l.screenX - ja.screenX, gf = l.screenY - ja.screenY) : gf = Sf = 0, ja = l), Sf);
    },
    movementY: function(l) {
      return "movementY" in l ? l.movementY : gf;
    }
  }), us = Zl(Qe), Dv = B({}, Qe, { dataTransfer: 0 }), Uv = Zl(Dv), Nv = B({}, Ga, { relatedTarget: 0 }), rf = Zl(Nv), Rv = B({}, Mu, {
    animationName: 0,
    elapsedTime: 0,
    pseudoElement: 0
  }), Hv = Zl(Rv), Cv = B({}, Mu, {
    clipboardData: function(l) {
      return "clipboardData" in l ? l.clipboardData : window.clipboardData;
    }
  }), qv = Zl(Cv), Bv = B({}, Mu, { data: 0 }), as = Zl(Bv), Yv = {
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
  }, Gv = {
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
  }, jv = {
    Alt: "altKey",
    Control: "ctrlKey",
    Meta: "metaKey",
    Shift: "shiftKey"
  };
  function Xv(l) {
    var t = this.nativeEvent;
    return t.getModifierState ? t.getModifierState(l) : (l = jv[l]) ? !!t[l] : !1;
  }
  function bf() {
    return Xv;
  }
  var Qv = B({}, Ga, {
    key: function(l) {
      if (l.key) {
        var t = Yv[l.key] || l.key;
        if (t !== "Unidentified") return t;
      }
      return l.type === "keypress" ? (l = Ge(l), l === 13 ? "Enter" : String.fromCharCode(l)) : l.type === "keydown" || l.type === "keyup" ? Gv[l.keyCode] || "Unidentified" : "";
    },
    code: 0,
    location: 0,
    ctrlKey: 0,
    shiftKey: 0,
    altKey: 0,
    metaKey: 0,
    repeat: 0,
    locale: 0,
    getModifierState: bf,
    charCode: function(l) {
      return l.type === "keypress" ? Ge(l) : 0;
    },
    keyCode: function(l) {
      return l.type === "keydown" || l.type === "keyup" ? l.keyCode : 0;
    },
    which: function(l) {
      return l.type === "keypress" ? Ge(l) : l.type === "keydown" || l.type === "keyup" ? l.keyCode : 0;
    }
  }), Zv = Zl(Qv), Lv = B({}, Qe, {
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
  }), es = Zl(Lv), xv = B({}, Ga, {
    touches: 0,
    targetTouches: 0,
    changedTouches: 0,
    altKey: 0,
    metaKey: 0,
    ctrlKey: 0,
    shiftKey: 0,
    getModifierState: bf
  }), Vv = Zl(xv), Kv = B({}, Mu, {
    propertyName: 0,
    elapsedTime: 0,
    pseudoElement: 0
  }), Jv = Zl(Kv), wv = B({}, Qe, {
    deltaX: function(l) {
      return "deltaX" in l ? l.deltaX : "wheelDeltaX" in l ? -l.wheelDeltaX : 0;
    },
    deltaY: function(l) {
      return "deltaY" in l ? l.deltaY : "wheelDeltaY" in l ? -l.wheelDeltaY : "wheelDelta" in l ? -l.wheelDelta : 0;
    },
    deltaZ: 0,
    deltaMode: 0
  }), Wv = Zl(wv), $v = B({}, Mu, {
    newState: 0,
    oldState: 0
  }), Fv = Zl($v), kv = [9, 13, 27, 32], Tf = Ht && "CompositionEvent" in window, Xa = null;
  Ht && "documentMode" in document && (Xa = document.documentMode);
  var Iv = Ht && "TextEvent" in window && !Xa, ns = Ht && (!Tf || Xa && 8 < Xa && 11 >= Xa), fs = " ", cs = !1;
  function is(l, t) {
    switch (l) {
      case "keyup":
        return kv.indexOf(t.keyCode) !== -1;
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
  function ss(l) {
    return l = l.detail, typeof l == "object" && "data" in l ? l.data : null;
  }
  var Iu = !1;
  function Pv(l, t) {
    switch (l) {
      case "compositionend":
        return ss(t);
      case "keypress":
        return t.which !== 32 ? null : (cs = !0, fs);
      case "textInput":
        return l = t.data, l === fs && cs ? null : l;
      default:
        return null;
    }
  }
  function ld(l, t) {
    if (Iu)
      return l === "compositionend" || !Tf && is(l, t) ? (l = ls(), Ye = hf = kt = null, Iu = !1, l) : null;
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
        return ns && t.locale !== "ko" ? null : t.data;
      default:
        return null;
    }
  }
  var td = {
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
  function os(l) {
    var t = l && l.nodeName && l.nodeName.toLowerCase();
    return t === "input" ? !!td[l.type] : t === "textarea";
  }
  function ys(l, t, u, a) {
    Fu ? ku ? ku.push(a) : ku = [a] : Fu = a, t = Nn(t, "onChange"), 0 < t.length && (u = new Xe(
      "onChange",
      "change",
      null,
      u,
      a
    ), l.push({ event: u, listeners: t }));
  }
  var Qa = null, Za = null;
  function ud(l) {
    W0(l, 0);
  }
  function Ze(l) {
    var t = qa(l);
    if (Ji(t)) return l;
  }
  function vs(l, t) {
    if (l === "change") return t;
  }
  var ds = !1;
  if (Ht) {
    var Ef;
    if (Ht) {
      var zf = "oninput" in document;
      if (!zf) {
        var ms = document.createElement("div");
        ms.setAttribute("oninput", "return;"), zf = typeof ms.oninput == "function";
      }
      Ef = zf;
    } else Ef = !1;
    ds = Ef && (!document.documentMode || 9 < document.documentMode);
  }
  function hs() {
    Qa && (Qa.detachEvent("onpropertychange", Ss), Za = Qa = null);
  }
  function Ss(l) {
    if (l.propertyName === "value" && Ze(Za)) {
      var t = [];
      ys(
        t,
        Za,
        l,
        vf(l)
      ), Pi(ud, t);
    }
  }
  function ad(l, t, u) {
    l === "focusin" ? (hs(), Qa = t, Za = u, Qa.attachEvent("onpropertychange", Ss)) : l === "focusout" && hs();
  }
  function ed(l) {
    if (l === "selectionchange" || l === "keyup" || l === "keydown")
      return Ze(Za);
  }
  function nd(l, t) {
    if (l === "click") return Ze(t);
  }
  function fd(l, t) {
    if (l === "input" || l === "change")
      return Ze(t);
  }
  function cd(l, t) {
    return l === t && (l !== 0 || 1 / l === 1 / t) || l !== l && t !== t;
  }
  var Pl = typeof Object.is == "function" ? Object.is : cd;
  function La(l, t) {
    if (Pl(l, t)) return !0;
    if (typeof l != "object" || l === null || typeof t != "object" || t === null)
      return !1;
    var u = Object.keys(l), a = Object.keys(t);
    if (u.length !== a.length) return !1;
    for (a = 0; a < u.length; a++) {
      var e = u[a];
      if (!In.call(t, e) || !Pl(l[e], t[e]))
        return !1;
    }
    return !0;
  }
  function gs(l) {
    for (; l && l.firstChild; ) l = l.firstChild;
    return l;
  }
  function rs(l, t) {
    var u = gs(l);
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
      u = gs(u);
    }
  }
  function bs(l, t) {
    return l && t ? l === t ? !0 : l && l.nodeType === 3 ? !1 : t && t.nodeType === 3 ? bs(l, t.parentNode) : "contains" in l ? l.contains(t) : l.compareDocumentPosition ? !!(l.compareDocumentPosition(t) & 16) : !1 : !1;
  }
  function Ts(l) {
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
  function Af(l) {
    var t = l && l.nodeName && l.nodeName.toLowerCase();
    return t && (t === "input" && (l.type === "text" || l.type === "search" || l.type === "tel" || l.type === "url" || l.type === "password") || t === "textarea" || l.contentEditable === "true");
  }
  var id = Ht && "documentMode" in document && 11 >= document.documentMode, Pu = null, _f = null, xa = null, Of = !1;
  function Es(l, t, u) {
    var a = u.window === u ? u.document : u.nodeType === 9 ? u : u.ownerDocument;
    Of || Pu == null || Pu !== qe(a) || (a = Pu, "selectionStart" in a && Af(a) ? a = { start: a.selectionStart, end: a.selectionEnd } : (a = (a.ownerDocument && a.ownerDocument.defaultView || window).getSelection(), a = {
      anchorNode: a.anchorNode,
      anchorOffset: a.anchorOffset,
      focusNode: a.focusNode,
      focusOffset: a.focusOffset
    }), xa && La(xa, a) || (xa = a, a = Nn(_f, "onSelect"), 0 < a.length && (t = new Xe(
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
  }, Mf = {}, zs = {};
  Ht && (zs = document.createElement("div").style, "AnimationEvent" in window || (delete la.animationend.animation, delete la.animationiteration.animation, delete la.animationstart.animation), "TransitionEvent" in window || delete la.transitionend.transition);
  function Du(l) {
    if (Mf[l]) return Mf[l];
    if (!la[l]) return l;
    var t = la[l], u;
    for (u in t)
      if (t.hasOwnProperty(u) && u in zs)
        return Mf[l] = t[u];
    return l;
  }
  var As = Du("animationend"), _s = Du("animationiteration"), Os = Du("animationstart"), sd = Du("transitionrun"), od = Du("transitionstart"), yd = Du("transitioncancel"), Ms = Du("transitionend"), ps = /* @__PURE__ */ new Map(), pf = "abort auxClick beforeToggle cancel canPlay canPlayThrough click close contextMenu copy cut drag dragEnd dragEnter dragExit dragLeave dragOver dragStart drop durationChange emptied encrypted ended error gotPointerCapture input invalid keyDown keyPress keyUp load loadedData loadedMetadata loadStart lostPointerCapture mouseDown mouseMove mouseOut mouseOver mouseUp paste pause play playing pointerCancel pointerDown pointerMove pointerOut pointerOver pointerUp progress rateChange reset resize seeked seeking stalled submit suspend timeUpdate touchCancel touchEnd touchStart volumeChange scroll toggle touchMove waiting wheel".split(
    " "
  );
  pf.push("scrollEnd");
  function rt(l, t) {
    ps.set(l, t), Ou(t, [l]);
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
  }, st = [], ta = 0, Df = 0;
  function xe() {
    for (var l = ta, t = Df = ta = 0; t < l; ) {
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
      n !== 0 && Ds(u, e, n);
    }
  }
  function Ve(l, t, u, a) {
    st[ta++] = l, st[ta++] = t, st[ta++] = u, st[ta++] = a, Df |= a, l.lanes |= a, l = l.alternate, l !== null && (l.lanes |= a);
  }
  function Uf(l, t, u, a) {
    return Ve(l, t, u, a), Ke(l);
  }
  function Uu(l, t) {
    return Ve(l, null, null, t), Ke(l);
  }
  function Ds(l, t, u) {
    l.lanes |= u;
    var a = l.alternate;
    a !== null && (a.lanes |= u);
    for (var e = !1, n = l.return; n !== null; )
      n.childLanes |= u, a = n.alternate, a !== null && (a.childLanes |= u), n.tag === 22 && (l = n.stateNode, l === null || l._visibility & 1 || (e = !0)), l = n, n = n.return;
    return l.tag === 3 ? (n = l.stateNode, e && t !== null && (e = 31 - Il(u), l = n.hiddenUpdates, a = l[e], a === null ? l[e] = [t] : a.push(t), t.lane = u | 536870912), n) : null;
  }
  function Ke(l) {
    if (50 < ye)
      throw ye = 0, jc = null, Error(d(185));
    for (var t = l.return; t !== null; )
      l = t, t = l.return;
    return l.tag === 3 ? l.stateNode : null;
  }
  var ua = {};
  function vd(l, t, u, a) {
    this.tag = l, this.key = u, this.sibling = this.child = this.return = this.stateNode = this.type = this.elementType = null, this.index = 0, this.refCleanup = this.ref = null, this.pendingProps = t, this.dependencies = this.memoizedState = this.updateQueue = this.memoizedProps = null, this.mode = a, this.subtreeFlags = this.flags = 0, this.deletions = null, this.childLanes = this.lanes = 0, this.alternate = null;
  }
  function lt(l, t, u, a) {
    return new vd(l, t, u, a);
  }
  function Nf(l) {
    return l = l.prototype, !(!l || !l.isReactComponent);
  }
  function Ct(l, t) {
    var u = l.alternate;
    return u === null ? (u = lt(
      l.tag,
      t,
      l.key,
      l.mode
    ), u.elementType = l.elementType, u.type = l.type, u.stateNode = l.stateNode, u.alternate = l, l.alternate = u) : (u.pendingProps = t, u.type = l.type, u.flags = 0, u.subtreeFlags = 0, u.deletions = null), u.flags = l.flags & 65011712, u.childLanes = l.childLanes, u.lanes = l.lanes, u.child = l.child, u.memoizedProps = l.memoizedProps, u.memoizedState = l.memoizedState, u.updateQueue = l.updateQueue, t = l.dependencies, u.dependencies = t === null ? null : { lanes: t.lanes, firstContext: t.firstContext }, u.sibling = l.sibling, u.index = l.index, u.ref = l.ref, u.refCleanup = l.refCleanup, u;
  }
  function Us(l, t) {
    l.flags &= 65011714;
    var u = l.alternate;
    return u === null ? (l.childLanes = 0, l.lanes = t, l.child = null, l.subtreeFlags = 0, l.memoizedProps = null, l.memoizedState = null, l.updateQueue = null, l.dependencies = null, l.stateNode = null) : (l.childLanes = u.childLanes, l.lanes = u.lanes, l.child = u.child, l.subtreeFlags = 0, l.deletions = null, l.memoizedProps = u.memoizedProps, l.memoizedState = u.memoizedState, l.updateQueue = u.updateQueue, l.type = u.type, t = u.dependencies, l.dependencies = t === null ? null : {
      lanes: t.lanes,
      firstContext: t.firstContext
    }), l;
  }
  function Je(l, t, u, a, e, n) {
    var f = 0;
    if (a = l, typeof l == "function") Nf(l) && (f = 1);
    else if (typeof l == "string")
      f = gm(
        l,
        u,
        N.current
      ) ? 26 : l === "html" || l === "head" || l === "body" ? 27 : 5;
    else
      l: switch (l) {
        case zt:
          return l = lt(31, u, t, e), l.elementType = zt, l.lanes = n, l;
        case Cl:
          return Nu(u.children, e, n, t);
        case Dt:
          f = 8, e |= 24;
          break;
        case $l:
          return l = lt(12, u, t, e | 2), l.elementType = $l, l.lanes = n, l;
        case Et:
          return l = lt(13, u, t, e), l.elementType = Et, l.lanes = n, l;
        case Gl:
          return l = lt(19, u, t, e), l.elementType = Gl, l.lanes = n, l;
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
              case W:
                f = 14;
                break l;
              case jl:
                f = 16, a = null;
                break l;
            }
          f = 29, u = Error(
            d(130, l === null ? "null" : typeof l, "")
          ), a = null;
      }
    return t = lt(f, u, t, e), t.elementType = l, t.type = a, t.lanes = n, t;
  }
  function Nu(l, t, u, a) {
    return l = lt(7, l, a, t), l.lanes = u, l;
  }
  function Rf(l, t, u) {
    return l = lt(6, l, null, t), l.lanes = u, l;
  }
  function Ns(l) {
    var t = lt(18, null, null, 0);
    return t.stateNode = l, t;
  }
  function Hf(l, t, u) {
    return t = lt(
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
  var Rs = /* @__PURE__ */ new WeakMap();
  function ot(l, t) {
    if (typeof l == "object" && l !== null) {
      var u = Rs.get(l);
      return u !== void 0 ? u : (t = {
        value: l,
        source: t,
        stack: Ni(t)
      }, Rs.set(l, t), t);
    }
    return {
      value: l,
      source: t,
      stack: Ni(t)
    };
  }
  var aa = [], ea = 0, we = null, Va = 0, yt = [], vt = 0, It = null, _t = 1, Ot = "";
  function qt(l, t) {
    aa[ea++] = Va, aa[ea++] = we, we = l, Va = t;
  }
  function Hs(l, t, u) {
    yt[vt++] = _t, yt[vt++] = Ot, yt[vt++] = It, It = l;
    var a = _t;
    l = Ot;
    var e = 32 - Il(a) - 1;
    a &= ~(1 << e), u += 1;
    var n = 32 - Il(t) + e;
    if (30 < n) {
      var f = e - e % 5;
      n = (a & (1 << f) - 1).toString(32), a >>= f, e -= f, _t = 1 << 32 - Il(t) + e | u << e | a, Ot = n + l;
    } else
      _t = 1 << n | u << e | a, Ot = l;
  }
  function Cf(l) {
    l.return !== null && (qt(l, 1), Hs(l, 1, 0));
  }
  function qf(l) {
    for (; l === we; )
      we = aa[--ea], aa[ea] = null, Va = aa[--ea], aa[ea] = null;
    for (; l === It; )
      It = yt[--vt], yt[vt] = null, Ot = yt[--vt], yt[vt] = null, _t = yt[--vt], yt[vt] = null;
  }
  function Cs(l, t) {
    yt[vt++] = _t, yt[vt++] = Ot, yt[vt++] = It, _t = t.id, Ot = t.overflow, It = l;
  }
  var pl = null, ol = null, $ = !1, Pt = null, dt = !1, Bf = Error(d(519));
  function lu(l) {
    var t = Error(
      d(
        418,
        1 < arguments.length && arguments[1] !== void 0 && arguments[1] ? "text" : "HTML",
        ""
      )
    );
    throw Ka(ot(t, l)), Bf;
  }
  function qs(l) {
    var t = l.stateNode, u = l.type, a = l.memoizedProps;
    switch (t[Ml] = l, t[Ql] = a, u) {
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
        for (u = 0; u < de.length; u++)
          V(de[u], t);
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
        V("invalid", t), wi(
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
        V("invalid", t);
        break;
      case "textarea":
        V("invalid", t), $i(t, a.value, a.defaultValue, a.children);
    }
    u = a.children, typeof u != "string" && typeof u != "number" && typeof u != "bigint" || t.textContent === "" + u || a.suppressHydrationWarning === !0 || I0(t.textContent, u) ? (a.popover != null && (V("beforetoggle", t), V("toggle", t)), a.onScroll != null && V("scroll", t), a.onScrollEnd != null && V("scrollend", t), a.onClick != null && (t.onclick = Rt), t = !0) : t = !1, t || lu(l, !0);
  }
  function Bs(l) {
    for (pl = l.return; pl; )
      switch (pl.tag) {
        case 5:
        case 31:
        case 13:
          dt = !1;
          return;
        case 27:
        case 3:
          dt = !0;
          return;
        default:
          pl = pl.return;
      }
  }
  function na(l) {
    if (l !== pl) return !1;
    if (!$) return Bs(l), $ = !0, !1;
    var t = l.tag, u;
    if ((u = t !== 3 && t !== 27) && ((u = t === 5) && (u = l.type, u = !(u !== "form" && u !== "button") || Pc(l.type, l.memoizedProps)), u = !u), u && ol && lu(l), Bs(l), t === 13) {
      if (l = l.memoizedState, l = l !== null ? l.dehydrated : null, !l) throw Error(d(317));
      ol = cy(l);
    } else if (t === 31) {
      if (l = l.memoizedState, l = l !== null ? l.dehydrated : null, !l) throw Error(d(317));
      ol = cy(l);
    } else
      t === 27 ? (t = ol, mu(l.type) ? (l = ei, ei = null, ol = l) : ol = t) : ol = pl ? ht(l.stateNode.nextSibling) : null;
    return !0;
  }
  function Ru() {
    ol = pl = null, $ = !1;
  }
  function Yf() {
    var l = Pt;
    return l !== null && (Kl === null ? Kl = l : Kl.push.apply(
      Kl,
      l
    ), Pt = null), l;
  }
  function Ka(l) {
    Pt === null ? Pt = [l] : Pt.push(l);
  }
  var Gf = o(null), Hu = null, Bt = null;
  function tu(l, t, u) {
    p(Gf, t._currentValue), t._currentValue = u;
  }
  function Yt(l) {
    l._currentValue = Gf.current, z(Gf);
  }
  function jf(l, t, u) {
    for (; l !== null; ) {
      var a = l.alternate;
      if ((l.childLanes & t) !== t ? (l.childLanes |= t, a !== null && (a.childLanes |= t)) : a !== null && (a.childLanes & t) !== t && (a.childLanes |= t), l === u) break;
      l = l.return;
    }
  }
  function Xf(l, t, u, a) {
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
              n.lanes |= u, c = n.alternate, c !== null && (c.lanes |= u), jf(
                n.return,
                u,
                l
              ), a || (f = null);
              break l;
            }
          n = c.next;
        }
      } else if (e.tag === 18) {
        if (f = e.return, f === null) throw Error(d(341));
        f.lanes |= u, n = f.alternate, n !== null && (n.lanes |= u), jf(f, u, l), f = null;
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
        if (f === null) throw Error(d(387));
        if (f = f.memoizedProps, f !== null) {
          var c = e.type;
          Pl(e.pendingProps.value, f.value) || (l !== null ? l.push(c) : l = [c]);
        }
      } else if (e === tl.current) {
        if (f = e.alternate, f === null) throw Error(d(387));
        f.memoizedState.memoizedState !== e.memoizedState.memoizedState && (l !== null ? l.push(re) : l = [re]);
      }
      e = e.return;
    }
    l !== null && Xf(
      t,
      l,
      u,
      a
    ), t.flags |= 262144;
  }
  function We(l) {
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
  function Cu(l) {
    Hu = l, Bt = null, l = l.dependencies, l !== null && (l.firstContext = null);
  }
  function Dl(l) {
    return Ys(Hu, l);
  }
  function $e(l, t) {
    return Hu === null && Cu(l), Ys(l, t);
  }
  function Ys(l, t) {
    var u = t._currentValue;
    if (t = { context: t, memoizedValue: u, next: null }, Bt === null) {
      if (l === null) throw Error(d(308));
      Bt = t, l.dependencies = { lanes: 0, firstContext: t }, l.flags |= 524288;
    } else Bt = Bt.next = t;
    return u;
  }
  var dd = typeof AbortController < "u" ? AbortController : function() {
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
  }, md = y.unstable_scheduleCallback, hd = y.unstable_NormalPriority, bl = {
    $$typeof: Rl,
    Consumer: null,
    Provider: null,
    _currentValue: null,
    _currentValue2: null,
    _threadCount: 0
  };
  function Qf() {
    return {
      controller: new dd(),
      data: /* @__PURE__ */ new Map(),
      refCount: 0
    };
  }
  function Ja(l) {
    l.refCount--, l.refCount === 0 && md(hd, function() {
      l.controller.abort();
    });
  }
  var wa = null, Zf = 0, ca = 0, ia = null;
  function Sd(l, t) {
    if (wa === null) {
      var u = wa = [];
      Zf = 0, ca = Vc(), ia = {
        status: "pending",
        value: void 0,
        then: function(a) {
          u.push(a);
        }
      };
    }
    return Zf++, t.then(Gs, Gs), t;
  }
  function Gs() {
    if (--Zf === 0 && wa !== null) {
      ia !== null && (ia.status = "fulfilled");
      var l = wa;
      wa = null, ca = 0, ia = null;
      for (var t = 0; t < l.length; t++) (0, l[t])();
    }
  }
  function gd(l, t) {
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
  var js = b.S;
  b.S = function(l, t) {
    z0 = Fl(), typeof t == "object" && t !== null && typeof t.then == "function" && Sd(l, t), js !== null && js(l, t);
  };
  var qu = o(null);
  function Lf() {
    var l = qu.current;
    return l !== null ? l : sl.pooledCache;
  }
  function Fe(l, t) {
    t === null ? p(qu, qu.current) : p(qu, t.pool);
  }
  function Xs() {
    var l = Lf();
    return l === null ? null : { parent: bl._currentValue, pool: l };
  }
  var sa = Error(d(460)), xf = Error(d(474)), ke = Error(d(542)), Ie = { then: function() {
  } };
  function Qs(l) {
    return l = l.status, l === "fulfilled" || l === "rejected";
  }
  function Zs(l, t, u) {
    switch (u = l[u], u === void 0 ? l.push(t) : u !== t && (t.then(Rt, Rt), t = u), t.status) {
      case "fulfilled":
        return t.value;
      case "rejected":
        throw l = t.reason, xs(l), l;
      default:
        if (typeof t.status == "string") t.then(Rt, Rt);
        else {
          if (l = sl, l !== null && 100 < l.shellSuspendCounter)
            throw Error(d(482));
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
            throw l = t.reason, xs(l), l;
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
  function Ls() {
    if (Yu === null) throw Error(d(459));
    var l = Yu;
    return Yu = null, l;
  }
  function xs(l) {
    if (l === sa || l === ke)
      throw Error(d(483));
  }
  var oa = null, Wa = 0;
  function Pe(l) {
    var t = Wa;
    return Wa += 1, oa === null && (oa = []), Zs(oa, l, t);
  }
  function $a(l, t) {
    t = t.props.ref, l.ref = t !== void 0 ? t : null;
  }
  function ln(l, t) {
    throw t.$$typeof === ll ? Error(d(525)) : (l = Object.prototype.toString.call(t), Error(
      d(
        31,
        l === "[object Object]" ? "object with keys {" + Object.keys(t).join(", ") + "}" : l
      )
    ));
  }
  function Vs(l) {
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
      return v = Ct(v, s), v.index = 0, v.sibling = null, v;
    }
    function n(v, s, m) {
      return v.index = m, l ? (m = v.alternate, m !== null ? (m = m.index, m < s ? (v.flags |= 67108866, s) : m) : (v.flags |= 67108866, s)) : (v.flags |= 1048576, s);
    }
    function f(v) {
      return l && v.alternate === null && (v.flags |= 67108866), v;
    }
    function c(v, s, m, T) {
      return s === null || s.tag !== 6 ? (s = Rf(m, v.mode, T), s.return = v, s) : (s = e(s, m), s.return = v, s);
    }
    function i(v, s, m, T) {
      var C = m.type;
      return C === Cl ? r(
        v,
        s,
        m.props.children,
        T,
        m.key
      ) : s !== null && (s.elementType === C || typeof C == "object" && C !== null && C.$$typeof === jl && Bu(C) === s.type) ? (s = e(s, m.props), $a(s, m), s.return = v, s) : (s = Je(
        m.type,
        m.key,
        m.props,
        null,
        v.mode,
        T
      ), $a(s, m), s.return = v, s);
    }
    function h(v, s, m, T) {
      return s === null || s.tag !== 4 || s.stateNode.containerInfo !== m.containerInfo || s.stateNode.implementation !== m.implementation ? (s = Hf(m, v.mode, T), s.return = v, s) : (s = e(s, m.children || []), s.return = v, s);
    }
    function r(v, s, m, T, C) {
      return s === null || s.tag !== 7 ? (s = Nu(
        m,
        v.mode,
        T,
        C
      ), s.return = v, s) : (s = e(s, m), s.return = v, s);
    }
    function E(v, s, m) {
      if (typeof s == "string" && s !== "" || typeof s == "number" || typeof s == "bigint")
        return s = Rf(
          "" + s,
          v.mode,
          m
        ), s.return = v, s;
      if (typeof s == "object" && s !== null) {
        switch (s.$$typeof) {
          case Wl:
            return m = Je(
              s.type,
              s.key,
              s.props,
              null,
              v.mode,
              m
            ), $a(m, s), m.return = v, m;
          case Yl:
            return s = Hf(
              s,
              v.mode,
              m
            ), s.return = v, s;
          case jl:
            return s = Bu(s), E(v, s, m);
        }
        if (gt(s) || Xl(s))
          return s = Nu(
            s,
            v.mode,
            m,
            null
          ), s.return = v, s;
        if (typeof s.then == "function")
          return E(v, Pe(s), m);
        if (s.$$typeof === Rl)
          return E(
            v,
            $e(v, s),
            m
          );
        ln(v, s);
      }
      return null;
    }
    function S(v, s, m, T) {
      var C = s !== null ? s.key : null;
      if (typeof m == "string" && m !== "" || typeof m == "number" || typeof m == "bigint")
        return C !== null ? null : c(v, s, "" + m, T);
      if (typeof m == "object" && m !== null) {
        switch (m.$$typeof) {
          case Wl:
            return m.key === C ? i(v, s, m, T) : null;
          case Yl:
            return m.key === C ? h(v, s, m, T) : null;
          case jl:
            return m = Bu(m), S(v, s, m, T);
        }
        if (gt(m) || Xl(m))
          return C !== null ? null : r(v, s, m, T, null);
        if (typeof m.then == "function")
          return S(
            v,
            s,
            Pe(m),
            T
          );
        if (m.$$typeof === Rl)
          return S(
            v,
            s,
            $e(v, m),
            T
          );
        ln(v, m);
      }
      return null;
    }
    function g(v, s, m, T, C) {
      if (typeof T == "string" && T !== "" || typeof T == "number" || typeof T == "bigint")
        return v = v.get(m) || null, c(s, v, "" + T, C);
      if (typeof T == "object" && T !== null) {
        switch (T.$$typeof) {
          case Wl:
            return v = v.get(
              T.key === null ? m : T.key
            ) || null, i(s, v, T, C);
          case Yl:
            return v = v.get(
              T.key === null ? m : T.key
            ) || null, h(s, v, T, C);
          case jl:
            return T = Bu(T), g(
              v,
              s,
              m,
              T,
              C
            );
        }
        if (gt(T) || Xl(T))
          return v = v.get(m) || null, r(s, v, T, C, null);
        if (typeof T.then == "function")
          return g(
            v,
            s,
            m,
            Pe(T),
            C
          );
        if (T.$$typeof === Rl)
          return g(
            v,
            s,
            m,
            $e(s, T),
            C
          );
        ln(s, T);
      }
      return null;
    }
    function D(v, s, m, T) {
      for (var C = null, k = null, R = s, Q = s = 0, w = null; R !== null && Q < m.length; Q++) {
        R.index > Q ? (w = R, R = null) : w = R.sibling;
        var I = S(
          v,
          R,
          m[Q],
          T
        );
        if (I === null) {
          R === null && (R = w);
          break;
        }
        l && R && I.alternate === null && t(v, R), s = n(I, s, Q), k === null ? C = I : k.sibling = I, k = I, R = w;
      }
      if (Q === m.length)
        return u(v, R), $ && qt(v, Q), C;
      if (R === null) {
        for (; Q < m.length; Q++)
          R = E(v, m[Q], T), R !== null && (s = n(
            R,
            s,
            Q
          ), k === null ? C = R : k.sibling = R, k = R);
        return $ && qt(v, Q), C;
      }
      for (R = a(R); Q < m.length; Q++)
        w = g(
          R,
          v,
          Q,
          m[Q],
          T
        ), w !== null && (l && w.alternate !== null && R.delete(
          w.key === null ? Q : w.key
        ), s = n(
          w,
          s,
          Q
        ), k === null ? C = w : k.sibling = w, k = w);
      return l && R.forEach(function(bu) {
        return t(v, bu);
      }), $ && qt(v, Q), C;
    }
    function q(v, s, m, T) {
      if (m == null) throw Error(d(151));
      for (var C = null, k = null, R = s, Q = s = 0, w = null, I = m.next(); R !== null && !I.done; Q++, I = m.next()) {
        R.index > Q ? (w = R, R = null) : w = R.sibling;
        var bu = S(v, R, I.value, T);
        if (bu === null) {
          R === null && (R = w);
          break;
        }
        l && R && bu.alternate === null && t(v, R), s = n(bu, s, Q), k === null ? C = bu : k.sibling = bu, k = bu, R = w;
      }
      if (I.done)
        return u(v, R), $ && qt(v, Q), C;
      if (R === null) {
        for (; !I.done; Q++, I = m.next())
          I = E(v, I.value, T), I !== null && (s = n(I, s, Q), k === null ? C = I : k.sibling = I, k = I);
        return $ && qt(v, Q), C;
      }
      for (R = a(R); !I.done; Q++, I = m.next())
        I = g(R, v, Q, I.value, T), I !== null && (l && I.alternate !== null && R.delete(I.key === null ? Q : I.key), s = n(I, s, Q), k === null ? C = I : k.sibling = I, k = I);
      return l && R.forEach(function(Dm) {
        return t(v, Dm);
      }), $ && qt(v, Q), C;
    }
    function cl(v, s, m, T) {
      if (typeof m == "object" && m !== null && m.type === Cl && m.key === null && (m = m.props.children), typeof m == "object" && m !== null) {
        switch (m.$$typeof) {
          case Wl:
            l: {
              for (var C = m.key; s !== null; ) {
                if (s.key === C) {
                  if (C = m.type, C === Cl) {
                    if (s.tag === 7) {
                      u(
                        v,
                        s.sibling
                      ), T = e(
                        s,
                        m.props.children
                      ), T.return = v, v = T;
                      break l;
                    }
                  } else if (s.elementType === C || typeof C == "object" && C !== null && C.$$typeof === jl && Bu(C) === s.type) {
                    u(
                      v,
                      s.sibling
                    ), T = e(s, m.props), $a(T, m), T.return = v, v = T;
                    break l;
                  }
                  u(v, s);
                  break;
                } else t(v, s);
                s = s.sibling;
              }
              m.type === Cl ? (T = Nu(
                m.props.children,
                v.mode,
                T,
                m.key
              ), T.return = v, v = T) : (T = Je(
                m.type,
                m.key,
                m.props,
                null,
                v.mode,
                T
              ), $a(T, m), T.return = v, v = T);
            }
            return f(v);
          case Yl:
            l: {
              for (C = m.key; s !== null; ) {
                if (s.key === C)
                  if (s.tag === 4 && s.stateNode.containerInfo === m.containerInfo && s.stateNode.implementation === m.implementation) {
                    u(
                      v,
                      s.sibling
                    ), T = e(s, m.children || []), T.return = v, v = T;
                    break l;
                  } else {
                    u(v, s);
                    break;
                  }
                else t(v, s);
                s = s.sibling;
              }
              T = Hf(m, v.mode, T), T.return = v, v = T;
            }
            return f(v);
          case jl:
            return m = Bu(m), cl(
              v,
              s,
              m,
              T
            );
        }
        if (gt(m))
          return D(
            v,
            s,
            m,
            T
          );
        if (Xl(m)) {
          if (C = Xl(m), typeof C != "function") throw Error(d(150));
          return m = C.call(m), q(
            v,
            s,
            m,
            T
          );
        }
        if (typeof m.then == "function")
          return cl(
            v,
            s,
            Pe(m),
            T
          );
        if (m.$$typeof === Rl)
          return cl(
            v,
            s,
            $e(v, m),
            T
          );
        ln(v, m);
      }
      return typeof m == "string" && m !== "" || typeof m == "number" || typeof m == "bigint" ? (m = "" + m, s !== null && s.tag === 6 ? (u(v, s.sibling), T = e(s, m), T.return = v, v = T) : (u(v, s), T = Rf(m, v.mode, T), T.return = v, v = T), f(v)) : u(v, s);
    }
    return function(v, s, m, T) {
      try {
        Wa = 0;
        var C = cl(
          v,
          s,
          m,
          T
        );
        return oa = null, C;
      } catch (R) {
        if (R === sa || R === ke) throw R;
        var k = lt(29, R, null, v.mode);
        return k.lanes = T, k.return = v, k;
      } finally {
      }
    };
  }
  var Gu = Vs(!0), Ks = Vs(!1), uu = !1;
  function Vf(l) {
    l.updateQueue = {
      baseState: l.memoizedState,
      firstBaseUpdate: null,
      lastBaseUpdate: null,
      shared: { pending: null, lanes: 0, hiddenCallbacks: null },
      callbacks: null
    };
  }
  function Kf(l, t) {
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
    if (a = a.shared, (P & 2) !== 0) {
      var e = a.pending;
      return e === null ? t.next = t : (t.next = e.next, e.next = t), a.pending = t, t = Ke(l), Ds(l, null, u), t;
    }
    return Ve(l, a, t, u), Ke(l);
  }
  function Fa(l, t, u) {
    if (t = t.updateQueue, t !== null && (t = t.shared, (u & 4194048) !== 0)) {
      var a = t.lanes;
      a &= l.pendingLanes, u |= a, t.lanes = u, Yi(l, u);
    }
  }
  function Jf(l, t) {
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
  var wf = !1;
  function ka() {
    if (wf) {
      var l = ia;
      if (l !== null) throw l;
    }
  }
  function Ia(l, t, u, a) {
    wf = !1;
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
        if (g ? (J & S) === S : (a & S) === S) {
          S !== 0 && S === ca && (wf = !0), r !== null && (r = r.next = {
            lane: 0,
            tag: c.tag,
            payload: c.payload,
            callback: null,
            next: null
          });
          l: {
            var D = l, q = c;
            S = t;
            var cl = u;
            switch (q.tag) {
              case 1:
                if (D = q.payload, typeof D == "function") {
                  E = D.call(cl, E, S);
                  break l;
                }
                E = D;
                break l;
              case 3:
                D.flags = D.flags & -65537 | 128;
              case 0:
                if (D = q.payload, S = typeof D == "function" ? D.call(cl, E, S) : D, S == null) break l;
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
  function Js(l, t) {
    if (typeof l != "function")
      throw Error(d(191, l));
    l.call(t);
  }
  function ws(l, t) {
    var u = l.callbacks;
    if (u !== null)
      for (l.callbacks = null, l = 0; l < u.length; l++)
        Js(u[l], t);
  }
  var ya = o(null), tn = o(0);
  function Ws(l, t) {
    l = Kt, p(tn, l), p(ya, t), Kt = l | t.baseLanes;
  }
  function Wf() {
    p(tn, Kt), p(ya, ya.current);
  }
  function $f() {
    Kt = tn.current, z(ya), z(tn);
  }
  var tt = o(null), mt = null;
  function nu(l) {
    var t = l.alternate;
    p(Sl, Sl.current & 1), p(tt, l), mt === null && (t === null || ya.current !== null || t.memoizedState !== null) && (mt = l);
  }
  function Ff(l) {
    p(Sl, Sl.current), p(tt, l), mt === null && (mt = l);
  }
  function $s(l) {
    l.tag === 22 ? (p(Sl, Sl.current), p(tt, l), mt === null && (mt = l)) : fu();
  }
  function fu() {
    p(Sl, Sl.current), p(tt, tt.current);
  }
  function ut(l) {
    z(tt), mt === l && (mt = null), z(Sl);
  }
  var Sl = o(0);
  function un(l) {
    for (var t = l; t !== null; ) {
      if (t.tag === 13) {
        var u = t.memoizedState;
        if (u !== null && (u = u.dehydrated, u === null || ui(u) || ai(u)))
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
  var Gt = 0, X = null, nl = null, Tl = null, an = !1, va = !1, ju = !1, en = 0, Pa = 0, da = null, rd = 0;
  function dl() {
    throw Error(d(321));
  }
  function kf(l, t) {
    if (t === null) return !1;
    for (var u = 0; u < t.length && u < l.length; u++)
      if (!Pl(l[u], t[u])) return !1;
    return !0;
  }
  function If(l, t, u, a, e, n) {
    return Gt = n, X = t, t.memoizedState = null, t.updateQueue = null, t.lanes = 0, b.H = l === null || l.memoizedState === null ? Co : dc, ju = !1, n = u(a, e), ju = !1, va && (n = ks(
      t,
      u,
      a,
      e
    )), Fs(l), n;
  }
  function Fs(l) {
    b.H = ue;
    var t = nl !== null && nl.next !== null;
    if (Gt = 0, Tl = nl = X = null, an = !1, Pa = 0, da = null, t) throw Error(d(300));
    l === null || El || (l = l.dependencies, l !== null && We(l) && (El = !0));
  }
  function ks(l, t, u, a) {
    X = l;
    var e = 0;
    do {
      if (va && (da = null), Pa = 0, va = !1, 25 <= e) throw Error(d(301));
      if (e += 1, Tl = nl = null, l.updateQueue != null) {
        var n = l.updateQueue;
        n.lastEffect = null, n.events = null, n.stores = null, n.memoCache != null && (n.memoCache.index = 0);
      }
      b.H = qo, n = t(u, a);
    } while (va);
    return n;
  }
  function bd() {
    var l = b.H, t = l.useState()[0];
    return t = typeof t.then == "function" ? le(t) : t, l = l.useState()[0], (nl !== null ? nl.memoizedState : null) !== l && (X.flags |= 1024), t;
  }
  function Pf() {
    var l = en !== 0;
    return en = 0, l;
  }
  function lc(l, t, u) {
    t.updateQueue = l.updateQueue, t.flags &= -2053, l.lanes &= ~u;
  }
  function tc(l) {
    if (an) {
      for (l = l.memoizedState; l !== null; ) {
        var t = l.queue;
        t !== null && (t.pending = null), l = l.next;
      }
      an = !1;
    }
    Gt = 0, Tl = nl = X = null, va = !1, Pa = en = 0, da = null;
  }
  function Bl() {
    var l = {
      memoizedState: null,
      baseState: null,
      baseQueue: null,
      queue: null,
      next: null
    };
    return Tl === null ? X.memoizedState = Tl = l : Tl = Tl.next = l, Tl;
  }
  function gl() {
    if (nl === null) {
      var l = X.alternate;
      l = l !== null ? l.memoizedState : null;
    } else l = nl.next;
    var t = Tl === null ? X.memoizedState : Tl.next;
    if (t !== null)
      Tl = t, nl = l;
    else {
      if (l === null)
        throw X.alternate === null ? Error(d(467)) : Error(d(310));
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
  function nn() {
    return { lastEffect: null, events: null, stores: null, memoCache: null };
  }
  function le(l) {
    var t = Pa;
    return Pa += 1, da === null && (da = []), l = Zs(da, l, t), t = X, (Tl === null ? t.memoizedState : Tl.next) === null && (t = t.alternate, b.H = t === null || t.memoizedState === null ? Co : dc), l;
  }
  function fn(l) {
    if (l !== null && typeof l == "object") {
      if (typeof l.then == "function") return le(l);
      if (l.$$typeof === Rl) return Dl(l);
    }
    throw Error(d(438, String(l)));
  }
  function uc(l) {
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
    return ac(t, nl, l);
  }
  function ac(l, t, u) {
    var a = l.queue;
    if (a === null) throw Error(d(311));
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
        if (E !== h.lane ? (J & E) === E : (Gt & E) === E) {
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
          E = h.action, ju && u(n, E), n = h.hasEagerState ? h.eagerState : u(n, E);
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
      if (i === null ? f = n : i.next = c, !Pl(n, l.memoizedState) && (El = !0, r && (u = ia, u !== null)))
        throw u;
      l.memoizedState = n, l.baseState = f, l.baseQueue = i, a.lastRenderedState = n;
    }
    return e === null && (a.lanes = 0), [l.memoizedState, a.dispatch];
  }
  function ec(l) {
    var t = gl(), u = t.queue;
    if (u === null) throw Error(d(311));
    u.lastRenderedReducer = l;
    var a = u.dispatch, e = u.pending, n = t.memoizedState;
    if (e !== null) {
      u.pending = null;
      var f = e = e.next;
      do
        n = l(n, f.action), f = f.next;
      while (f !== e);
      Pl(n, t.memoizedState) || (El = !0), t.memoizedState = n, t.baseQueue === null && (t.baseState = n), u.lastRenderedState = n;
    }
    return [n, a];
  }
  function Is(l, t, u) {
    var a = X, e = gl(), n = $;
    if (n) {
      if (u === void 0) throw Error(d(407));
      u = u();
    } else u = t();
    var f = !Pl(
      (nl || e).memoizedState,
      u
    );
    if (f && (e.memoizedState = u, El = !0), e = e.queue, cc(to.bind(null, a, e, l), [
      l
    ]), e.getSnapshot !== t || f || Tl !== null && Tl.memoizedState.tag & 1) {
      if (a.flags |= 2048, ma(
        9,
        { destroy: void 0 },
        lo.bind(
          null,
          a,
          e,
          u,
          t
        ),
        null
      ), sl === null) throw Error(d(349));
      n || (Gt & 127) !== 0 || Ps(a, t, u);
    }
    return u;
  }
  function Ps(l, t, u) {
    l.flags |= 16384, l = { getSnapshot: t, value: u }, t = X.updateQueue, t === null ? (t = nn(), X.updateQueue = t, t.stores = [l]) : (u = t.stores, u === null ? t.stores = [l] : u.push(l));
  }
  function lo(l, t, u, a) {
    t.value = u, t.getSnapshot = a, uo(t) && ao(l);
  }
  function to(l, t, u) {
    return u(function() {
      uo(t) && ao(l);
    });
  }
  function uo(l) {
    var t = l.getSnapshot;
    l = l.value;
    try {
      var u = t();
      return !Pl(l, u);
    } catch {
      return !0;
    }
  }
  function ao(l) {
    var t = Uu(l, 2);
    t !== null && Jl(t, l, 2);
  }
  function nc(l) {
    var t = Bl();
    if (typeof l == "function") {
      var u = l;
      if (l = u(), ju) {
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
  function eo(l, t, u, a) {
    return l.baseState = u, ac(
      l,
      nl,
      typeof a == "function" ? a : jt
    );
  }
  function Td(l, t, u, a, e) {
    if (yn(l)) throw Error(d(485));
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
      b.T !== null ? u(!0) : n.isTransition = !1, a(n), u = t.pending, u === null ? (n.next = t.pending = n, no(t, n)) : (n.next = u.next, t.pending = u.next = n);
    }
  }
  function no(l, t) {
    var u = t.action, a = t.payload, e = l.state;
    if (t.isTransition) {
      var n = b.T, f = {};
      b.T = f;
      try {
        var c = u(e, a), i = b.S;
        i !== null && i(f, c), fo(l, t, c);
      } catch (h) {
        fc(l, t, h);
      } finally {
        n !== null && f.types !== null && (n.types = f.types), b.T = n;
      }
    } else
      try {
        n = u(e, a), fo(l, t, n);
      } catch (h) {
        fc(l, t, h);
      }
  }
  function fo(l, t, u) {
    u !== null && typeof u == "object" && typeof u.then == "function" ? u.then(
      function(a) {
        co(l, t, a);
      },
      function(a) {
        return fc(l, t, a);
      }
    ) : co(l, t, u);
  }
  function co(l, t, u) {
    t.status = "fulfilled", t.value = u, io(t), l.state = u, t = l.pending, t !== null && (u = t.next, u === t ? l.pending = null : (u = u.next, t.next = u, no(l, u)));
  }
  function fc(l, t, u) {
    var a = l.pending;
    if (l.pending = null, a !== null) {
      a = a.next;
      do
        t.status = "rejected", t.reason = u, io(t), t = t.next;
      while (t !== a);
    }
    l.action = null;
  }
  function io(l) {
    l = l.listeners;
    for (var t = 0; t < l.length; t++) (0, l[t])();
  }
  function so(l, t) {
    return t;
  }
  function oo(l, t) {
    if ($) {
      var u = sl.formState;
      if (u !== null) {
        l: {
          var a = X;
          if ($) {
            if (ol) {
              t: {
                for (var e = ol, n = dt; e.nodeType !== 8; ) {
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
      lastRenderedReducer: so,
      lastRenderedState: t
    }, u.queue = a, u = No.bind(
      null,
      X,
      a
    ), a.dispatch = u, a = nc(!1), n = vc.bind(
      null,
      X,
      !1,
      a.queue
    ), a = Bl(), e = {
      state: t,
      dispatch: null,
      action: l,
      pending: null
    }, a.queue = e, u = Td.bind(
      null,
      X,
      e,
      n,
      u
    ), e.dispatch = u, a.memoizedState = l, [t, u, !1];
  }
  function yo(l) {
    var t = gl();
    return vo(t, nl, l);
  }
  function vo(l, t, u) {
    if (t = ac(
      l,
      t,
      so
    )[0], l = cn(jt)[0], typeof t == "object" && t !== null && typeof t.then == "function")
      try {
        var a = le(t);
      } catch (f) {
        throw f === sa ? ke : f;
      }
    else a = t;
    t = gl();
    var e = t.queue, n = e.dispatch;
    return u !== t.memoizedState && (X.flags |= 2048, ma(
      9,
      { destroy: void 0 },
      Ed.bind(null, e, u),
      null
    )), [a, n, l];
  }
  function Ed(l, t) {
    l.action = t;
  }
  function mo(l) {
    var t = gl(), u = nl;
    if (u !== null)
      return vo(t, u, l);
    gl(), t = t.memoizedState, u = gl();
    var a = u.queue.dispatch;
    return u.memoizedState = l, [t, a, !1];
  }
  function ma(l, t, u, a) {
    return l = { tag: l, create: u, deps: a, inst: t, next: null }, t = X.updateQueue, t === null && (t = nn(), X.updateQueue = t), u = t.lastEffect, u === null ? t.lastEffect = l.next = l : (a = u.next, u.next = l, l.next = a, t.lastEffect = l), l;
  }
  function ho() {
    return gl().memoizedState;
  }
  function sn(l, t, u, a) {
    var e = Bl();
    X.flags |= l, e.memoizedState = ma(
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
    nl !== null && a !== null && kf(a, nl.memoizedState.deps) ? e.memoizedState = ma(t, n, u, a) : (X.flags |= l, e.memoizedState = ma(
      1 | t,
      n,
      u,
      a
    ));
  }
  function So(l, t) {
    sn(8390656, 8, l, t);
  }
  function cc(l, t) {
    on(2048, 8, l, t);
  }
  function zd(l) {
    X.flags |= 4;
    var t = X.updateQueue;
    if (t === null)
      t = nn(), X.updateQueue = t, t.events = [l];
    else {
      var u = t.events;
      u === null ? t.events = [l] : u.push(l);
    }
  }
  function go(l) {
    var t = gl().memoizedState;
    return zd({ ref: t, nextImpl: l }), function() {
      if ((P & 2) !== 0) throw Error(d(440));
      return t.impl.apply(void 0, arguments);
    };
  }
  function ro(l, t) {
    return on(4, 2, l, t);
  }
  function bo(l, t) {
    return on(4, 4, l, t);
  }
  function To(l, t) {
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
  function Eo(l, t, u) {
    u = u != null ? u.concat([l]) : null, on(4, 4, To.bind(null, t, l), u);
  }
  function ic() {
  }
  function zo(l, t) {
    var u = gl();
    t = t === void 0 ? null : t;
    var a = u.memoizedState;
    return t !== null && kf(t, a[1]) ? a[0] : (u.memoizedState = [l, t], l);
  }
  function Ao(l, t) {
    var u = gl();
    t = t === void 0 ? null : t;
    var a = u.memoizedState;
    if (t !== null && kf(t, a[1]))
      return a[0];
    if (a = l(), ju) {
      $t(!0);
      try {
        l();
      } finally {
        $t(!1);
      }
    }
    return u.memoizedState = [a, t], a;
  }
  function sc(l, t, u) {
    return u === void 0 || (Gt & 1073741824) !== 0 && (J & 261930) === 0 ? l.memoizedState = t : (l.memoizedState = u, l = _0(), X.lanes |= l, su |= l, u);
  }
  function _o(l, t, u, a) {
    return Pl(u, t) ? u : ya.current !== null ? (l = sc(l, u, a), Pl(l, t) || (El = !0), l) : (Gt & 42) === 0 || (Gt & 1073741824) !== 0 && (J & 261930) === 0 ? (El = !0, l.memoizedState = u) : (l = _0(), X.lanes |= l, su |= l, t);
  }
  function Oo(l, t, u, a, e) {
    var n = M.p;
    M.p = n !== 0 && 8 > n ? n : 8;
    var f = b.T, c = {};
    b.T = c, vc(l, !1, t, u);
    try {
      var i = e(), h = b.S;
      if (h !== null && h(c, i), i !== null && typeof i == "object" && typeof i.then == "function") {
        var r = gd(
          i,
          a
        );
        te(
          l,
          t,
          r,
          nt(l)
        );
      } else
        te(
          l,
          t,
          a,
          nt(l)
        );
    } catch (E) {
      te(
        l,
        t,
        { then: function() {
        }, status: "rejected", reason: E },
        nt()
      );
    } finally {
      M.p = n, f !== null && c.types !== null && (f.types = c.types), b.T = f;
    }
  }
  function Ad() {
  }
  function oc(l, t, u, a) {
    if (l.tag !== 5) throw Error(d(476));
    var e = Mo(l).queue;
    Oo(
      l,
      e,
      t,
      Y,
      u === null ? Ad : function() {
        return po(l), u(a);
      }
    );
  }
  function Mo(l) {
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
  function po(l) {
    var t = Mo(l);
    t.next === null && (t = l.alternate.memoizedState), te(
      l,
      t.next.queue,
      {},
      nt()
    );
  }
  function yc() {
    return Dl(re);
  }
  function Do() {
    return gl().memoizedState;
  }
  function Uo() {
    return gl().memoizedState;
  }
  function _d(l) {
    for (var t = l.return; t !== null; ) {
      switch (t.tag) {
        case 24:
        case 3:
          var u = nt();
          l = au(u);
          var a = eu(t, l, u);
          a !== null && (Jl(a, t, u), Fa(a, t, u)), t = { cache: Qf() }, l.payload = t;
          return;
      }
      t = t.return;
    }
  }
  function Od(l, t, u) {
    var a = nt();
    u = {
      lane: a,
      revertLane: 0,
      gesture: null,
      action: u,
      hasEagerState: !1,
      eagerState: null,
      next: null
    }, yn(l) ? Ro(t, u) : (u = Uf(l, t, u, a), u !== null && (Jl(u, l, a), Ho(u, t, a)));
  }
  function No(l, t, u) {
    var a = nt();
    te(l, t, u, a);
  }
  function te(l, t, u, a) {
    var e = {
      lane: a,
      revertLane: 0,
      gesture: null,
      action: u,
      hasEagerState: !1,
      eagerState: null,
      next: null
    };
    if (yn(l)) Ro(t, e);
    else {
      var n = l.alternate;
      if (l.lanes === 0 && (n === null || n.lanes === 0) && (n = t.lastRenderedReducer, n !== null))
        try {
          var f = t.lastRenderedState, c = n(f, u);
          if (e.hasEagerState = !0, e.eagerState = c, Pl(c, f))
            return Ve(l, t, e, 0), sl === null && xe(), !1;
        } catch {
        } finally {
        }
      if (u = Uf(l, t, e, a), u !== null)
        return Jl(u, l, a), Ho(u, t, a), !0;
    }
    return !1;
  }
  function vc(l, t, u, a) {
    if (a = {
      lane: 2,
      revertLane: Vc(),
      gesture: null,
      action: a,
      hasEagerState: !1,
      eagerState: null,
      next: null
    }, yn(l)) {
      if (t) throw Error(d(479));
    } else
      t = Uf(
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
  function Ro(l, t) {
    va = an = !0;
    var u = l.pending;
    u === null ? t.next = t : (t.next = u.next, u.next = t), l.pending = t;
  }
  function Ho(l, t, u) {
    if ((u & 4194048) !== 0) {
      var a = t.lanes;
      a &= l.pendingLanes, u |= a, t.lanes = u, Yi(l, u);
    }
  }
  var ue = {
    readContext: Dl,
    use: fn,
    useCallback: dl,
    useContext: dl,
    useEffect: dl,
    useImperativeHandle: dl,
    useLayoutEffect: dl,
    useInsertionEffect: dl,
    useMemo: dl,
    useReducer: dl,
    useRef: dl,
    useState: dl,
    useDebugValue: dl,
    useDeferredValue: dl,
    useTransition: dl,
    useSyncExternalStore: dl,
    useId: dl,
    useHostTransitionStatus: dl,
    useFormState: dl,
    useActionState: dl,
    useOptimistic: dl,
    useMemoCache: dl,
    useCacheRefresh: dl
  };
  ue.useEffectEvent = dl;
  var Co = {
    readContext: Dl,
    use: fn,
    useCallback: function(l, t) {
      return Bl().memoizedState = [
        l,
        t === void 0 ? null : t
      ], l;
    },
    useContext: Dl,
    useEffect: So,
    useImperativeHandle: function(l, t, u) {
      u = u != null ? u.concat([l]) : null, sn(
        4194308,
        4,
        To.bind(null, t, l),
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
      if (ju) {
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
        if (ju) {
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
      }, a.queue = l, l = l.dispatch = Od.bind(
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
      l = nc(l);
      var t = l.queue, u = No.bind(null, X, t);
      return t.dispatch = u, [l.memoizedState, u];
    },
    useDebugValue: ic,
    useDeferredValue: function(l, t) {
      var u = Bl();
      return sc(u, l, t);
    },
    useTransition: function() {
      var l = nc(!1);
      return l = Oo.bind(
        null,
        X,
        l.queue,
        !0,
        !1
      ), Bl().memoizedState = l, [!1, l];
    },
    useSyncExternalStore: function(l, t, u) {
      var a = X, e = Bl();
      if ($) {
        if (u === void 0)
          throw Error(d(407));
        u = u();
      } else {
        if (u = t(), sl === null)
          throw Error(d(349));
        (J & 127) !== 0 || Ps(a, t, u);
      }
      e.memoizedState = u;
      var n = { value: u, getSnapshot: t };
      return e.queue = n, So(to.bind(null, a, n, l), [
        l
      ]), a.flags |= 2048, ma(
        9,
        { destroy: void 0 },
        lo.bind(
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
      if ($) {
        var u = Ot, a = _t;
        u = (a & ~(1 << 32 - Il(a) - 1)).toString(32) + u, t = "_" + t + "R_" + u, u = en++, 0 < u && (t += "H" + u.toString(32)), t += "_";
      } else
        u = rd++, t = "_" + t + "r_" + u.toString(32) + "_";
      return l.memoizedState = t;
    },
    useHostTransitionStatus: yc,
    useFormState: oo,
    useActionState: oo,
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
      return t.queue = u, t = vc.bind(
        null,
        X,
        !0,
        u
      ), u.dispatch = t, [l, t];
    },
    useMemoCache: uc,
    useCacheRefresh: function() {
      return Bl().memoizedState = _d.bind(
        null,
        X
      );
    },
    useEffectEvent: function(l) {
      var t = Bl(), u = { impl: l };
      return t.memoizedState = u, function() {
        if ((P & 2) !== 0)
          throw Error(d(440));
        return u.impl.apply(void 0, arguments);
      };
    }
  }, dc = {
    readContext: Dl,
    use: fn,
    useCallback: zo,
    useContext: Dl,
    useEffect: cc,
    useImperativeHandle: Eo,
    useInsertionEffect: ro,
    useLayoutEffect: bo,
    useMemo: Ao,
    useReducer: cn,
    useRef: ho,
    useState: function() {
      return cn(jt);
    },
    useDebugValue: ic,
    useDeferredValue: function(l, t) {
      var u = gl();
      return _o(
        u,
        nl.memoizedState,
        l,
        t
      );
    },
    useTransition: function() {
      var l = cn(jt)[0], t = gl().memoizedState;
      return [
        typeof l == "boolean" ? l : le(l),
        t
      ];
    },
    useSyncExternalStore: Is,
    useId: Do,
    useHostTransitionStatus: yc,
    useFormState: yo,
    useActionState: yo,
    useOptimistic: function(l, t) {
      var u = gl();
      return eo(u, nl, l, t);
    },
    useMemoCache: uc,
    useCacheRefresh: Uo
  };
  dc.useEffectEvent = go;
  var qo = {
    readContext: Dl,
    use: fn,
    useCallback: zo,
    useContext: Dl,
    useEffect: cc,
    useImperativeHandle: Eo,
    useInsertionEffect: ro,
    useLayoutEffect: bo,
    useMemo: Ao,
    useReducer: ec,
    useRef: ho,
    useState: function() {
      return ec(jt);
    },
    useDebugValue: ic,
    useDeferredValue: function(l, t) {
      var u = gl();
      return nl === null ? sc(u, l, t) : _o(
        u,
        nl.memoizedState,
        l,
        t
      );
    },
    useTransition: function() {
      var l = ec(jt)[0], t = gl().memoizedState;
      return [
        typeof l == "boolean" ? l : le(l),
        t
      ];
    },
    useSyncExternalStore: Is,
    useId: Do,
    useHostTransitionStatus: yc,
    useFormState: mo,
    useActionState: mo,
    useOptimistic: function(l, t) {
      var u = gl();
      return nl !== null ? eo(u, nl, l, t) : (u.baseState = l, [l, u.queue.dispatch]);
    },
    useMemoCache: uc,
    useCacheRefresh: Uo
  };
  qo.useEffectEvent = go;
  function mc(l, t, u, a) {
    t = l.memoizedState, u = u(a, t), u = u == null ? t : B({}, t, u), l.memoizedState = u, l.lanes === 0 && (l.updateQueue.baseState = u);
  }
  var hc = {
    enqueueSetState: function(l, t, u) {
      l = l._reactInternals;
      var a = nt(), e = au(a);
      e.payload = t, u != null && (e.callback = u), t = eu(l, e, a), t !== null && (Jl(t, l, a), Fa(t, l, a));
    },
    enqueueReplaceState: function(l, t, u) {
      l = l._reactInternals;
      var a = nt(), e = au(a);
      e.tag = 1, e.payload = t, u != null && (e.callback = u), t = eu(l, e, a), t !== null && (Jl(t, l, a), Fa(t, l, a));
    },
    enqueueForceUpdate: function(l, t) {
      l = l._reactInternals;
      var u = nt(), a = au(u);
      a.tag = 2, t != null && (a.callback = t), t = eu(l, a, u), t !== null && (Jl(t, l, u), Fa(t, l, u));
    }
  };
  function Bo(l, t, u, a, e, n, f) {
    return l = l.stateNode, typeof l.shouldComponentUpdate == "function" ? l.shouldComponentUpdate(a, n, f) : t.prototype && t.prototype.isPureReactComponent ? !La(u, a) || !La(e, n) : !0;
  }
  function Yo(l, t, u, a) {
    l = t.state, typeof t.componentWillReceiveProps == "function" && t.componentWillReceiveProps(u, a), typeof t.UNSAFE_componentWillReceiveProps == "function" && t.UNSAFE_componentWillReceiveProps(u, a), t.state !== l && hc.enqueueReplaceState(t, t.state, null);
  }
  function Xu(l, t) {
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
  function Go(l) {
    Le(l);
  }
  function jo(l) {
    console.error(l);
  }
  function Xo(l) {
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
  function Qo(l, t, u) {
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
  function Sc(l, t, u) {
    return u = au(u), u.tag = 3, u.payload = { element: null }, u.callback = function() {
      vn(l, t);
    }, u;
  }
  function Zo(l) {
    return l = au(l), l.tag = 3, l;
  }
  function Lo(l, t, u, a) {
    var e = u.type.getDerivedStateFromError;
    if (typeof e == "function") {
      var n = a.value;
      l.payload = function() {
        return e(n);
      }, l.callback = function() {
        Qo(t, u, a);
      };
    }
    var f = u.stateNode;
    f !== null && typeof f.componentDidCatch == "function" && (l.callback = function() {
      Qo(t, u, a), typeof e != "function" && (ou === null ? ou = /* @__PURE__ */ new Set([this]) : ou.add(this));
      var c = a.stack;
      this.componentDidCatch(a.value, {
        componentStack: c !== null ? c : ""
      });
    });
  }
  function Md(l, t, u, a, e) {
    if (u.flags |= 32768, a !== null && typeof a == "object" && typeof a.then == "function") {
      if (t = u.alternate, t !== null && fa(
        t,
        u,
        e,
        !0
      ), u = tt.current, u !== null) {
        switch (u.tag) {
          case 31:
          case 13:
            return mt === null ? _n() : u.alternate === null && ml === 0 && (ml = 3), u.flags &= -257, u.flags |= 65536, u.lanes = e, a === Ie ? u.flags |= 16384 : (t = u.updateQueue, t === null ? u.updateQueue = /* @__PURE__ */ new Set([a]) : t.add(a), Zc(l, a, e)), !1;
          case 22:
            return u.flags |= 65536, a === Ie ? u.flags |= 16384 : (t = u.updateQueue, t === null ? (t = {
              transitions: null,
              markerInstances: null,
              retryQueue: /* @__PURE__ */ new Set([a])
            }, u.updateQueue = t) : (u = t.retryQueue, u === null ? t.retryQueue = /* @__PURE__ */ new Set([a]) : u.add(a)), Zc(l, a, e)), !1;
        }
        throw Error(d(435, u.tag));
      }
      return Zc(l, a, e), _n(), !1;
    }
    if ($)
      return t = tt.current, t !== null ? ((t.flags & 65536) === 0 && (t.flags |= 256), t.flags |= 65536, t.lanes = e, a !== Bf && (l = Error(d(422), { cause: a }), Ka(ot(l, u)))) : (a !== Bf && (t = Error(d(423), {
        cause: a
      }), Ka(
        ot(t, u)
      )), l = l.current.alternate, l.flags |= 65536, e &= -e, l.lanes |= e, a = ot(a, u), e = Sc(
        l.stateNode,
        a,
        e
      ), Jf(l, e), ml !== 4 && (ml = 2)), !1;
    var n = Error(d(520), { cause: a });
    if (n = ot(n, u), oe === null ? oe = [n] : oe.push(n), ml !== 4 && (ml = 2), t === null) return !0;
    a = ot(a, u), u = t;
    do {
      switch (u.tag) {
        case 3:
          return u.flags |= 65536, l = e & -e, u.lanes |= l, l = Sc(u.stateNode, a, l), Jf(u, l), !1;
        case 1:
          if (t = u.type, n = u.stateNode, (u.flags & 128) === 0 && (typeof t.getDerivedStateFromError == "function" || n !== null && typeof n.componentDidCatch == "function" && (ou === null || !ou.has(n))))
            return u.flags |= 65536, e &= -e, u.lanes |= e, e = Zo(e), Lo(
              e,
              l,
              u,
              a
            ), Jf(u, e), !1;
      }
      u = u.return;
    } while (u !== null);
    return !1;
  }
  var gc = Error(d(461)), El = !1;
  function Ul(l, t, u, a) {
    t.child = l === null ? Ks(t, null, u, a) : Gu(
      t,
      l.child,
      u,
      a
    );
  }
  function xo(l, t, u, a, e) {
    u = u.render;
    var n = t.ref;
    if ("ref" in a) {
      var f = {};
      for (var c in a)
        c !== "ref" && (f[c] = a[c]);
    } else f = a;
    return Cu(t), a = If(
      l,
      t,
      u,
      f,
      n,
      e
    ), c = Pf(), l !== null && !El ? (lc(l, t, e), Xt(l, t, e)) : ($ && c && Cf(t), t.flags |= 1, Ul(l, t, a, e), t.child);
  }
  function Vo(l, t, u, a, e) {
    if (l === null) {
      var n = u.type;
      return typeof n == "function" && !Nf(n) && n.defaultProps === void 0 && u.compare === null ? (t.tag = 15, t.type = n, Ko(
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
    if (n = l.child, !Oc(l, e)) {
      var f = n.memoizedProps;
      if (u = u.compare, u = u !== null ? u : La, u(f, a) && l.ref === t.ref)
        return Xt(l, t, e);
    }
    return t.flags |= 1, l = Ct(n, a), l.ref = t.ref, l.return = t, t.child = l;
  }
  function Ko(l, t, u, a, e) {
    if (l !== null) {
      var n = l.memoizedProps;
      if (La(n, a) && l.ref === t.ref)
        if (El = !1, t.pendingProps = a = n, Oc(l, e))
          (l.flags & 131072) !== 0 && (El = !0);
        else
          return t.lanes = l.lanes, Xt(l, t, e);
    }
    return rc(
      l,
      t,
      u,
      a,
      e
    );
  }
  function Jo(l, t, u, a) {
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
        return wo(
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
        ), n !== null ? Ws(t, n) : Wf(), $s(t);
      else
        return a = t.lanes = 536870912, wo(
          l,
          t,
          n !== null ? n.baseLanes | u : u,
          u,
          a
        );
    } else
      n !== null ? (Fe(t, n.cachePool), Ws(t, n), fu(), t.memoizedState = null) : (l !== null && Fe(t, null), Wf(), fu());
    return Ul(l, t, e, u), t.child;
  }
  function ae(l, t) {
    return l !== null && l.tag === 22 || t.stateNode !== null || (t.stateNode = {
      _visibility: 1,
      _pendingMarkers: null,
      _retryCache: null,
      _transitions: null
    }), t.sibling;
  }
  function wo(l, t, u, a, e) {
    var n = Lf();
    return n = n === null ? null : { parent: bl._currentValue, pool: n }, t.memoizedState = {
      baseLanes: u,
      cachePool: n
    }, l !== null && Fe(t, null), Wf(), $s(t), l !== null && fa(l, t, a, !0), t.childLanes = e, null;
  }
  function dn(l, t) {
    return t = hn(
      { mode: t.mode, children: t.children },
      l.mode
    ), t.ref = l.ref, l.child = t, t.return = l, t;
  }
  function Wo(l, t, u) {
    return Gu(t, l.child, null, u), l = dn(t, t.pendingProps), l.flags |= 2, ut(t), t.memoizedState = null, l;
  }
  function pd(l, t, u) {
    var a = t.pendingProps, e = (t.flags & 128) !== 0;
    if (t.flags &= -129, l === null) {
      if ($) {
        if (a.mode === "hidden")
          return l = dn(t, a), t.lanes = 536870912, ae(null, l);
        if (Ff(t), (l = ol) ? (l = fy(
          l,
          dt
        ), l = l !== null && l.data === "&" ? l : null, l !== null && (t.memoizedState = {
          dehydrated: l,
          treeContext: It !== null ? { id: _t, overflow: Ot } : null,
          retryLane: 536870912,
          hydrationErrors: null
        }, u = Ns(l), u.return = t, t.child = u, pl = t, ol = null)) : l = null, l === null) throw lu(t);
        return t.lanes = 536870912, null;
      }
      return dn(t, a);
    }
    var n = l.memoizedState;
    if (n !== null) {
      var f = n.dehydrated;
      if (Ff(t), e)
        if (t.flags & 256)
          t.flags &= -257, t = Wo(
            l,
            t,
            u
          );
        else if (t.memoizedState !== null)
          t.child = l.child, t.flags |= 128, t = null;
        else throw Error(d(558));
      else if (El || fa(l, t, u, !1), e = (u & l.childLanes) !== 0, El || e) {
        if (a = sl, a !== null && (f = Gi(a, u), f !== 0 && f !== n.retryLane))
          throw n.retryLane = f, Uu(l, f), Jl(a, l, f), gc;
        _n(), t = Wo(
          l,
          t,
          u
        );
      } else
        l = n.treeContext, ol = ht(f.nextSibling), pl = t, $ = !0, Pt = null, dt = !1, l !== null && Cs(t, l), t = dn(t, a), t.flags |= 4096;
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
        throw Error(d(284));
      (l === null || l.ref !== u) && (t.flags |= 4194816);
    }
  }
  function rc(l, t, u, a, e) {
    return Cu(t), u = If(
      l,
      t,
      u,
      a,
      void 0,
      e
    ), a = Pf(), l !== null && !El ? (lc(l, t, e), Xt(l, t, e)) : ($ && a && Cf(t), t.flags |= 1, Ul(l, t, u, e), t.child);
  }
  function $o(l, t, u, a, e, n) {
    return Cu(t), t.updateQueue = null, u = ks(
      t,
      a,
      u,
      e
    ), Fs(l), a = Pf(), l !== null && !El ? (lc(l, t, n), Xt(l, t, n)) : ($ && a && Cf(t), t.flags |= 1, Ul(l, t, u, n), t.child);
  }
  function Fo(l, t, u, a, e) {
    if (Cu(t), t.stateNode === null) {
      var n = ua, f = u.contextType;
      typeof f == "object" && f !== null && (n = Dl(f)), n = new u(a, n), t.memoizedState = n.state !== null && n.state !== void 0 ? n.state : null, n.updater = hc, t.stateNode = n, n._reactInternals = t, n = t.stateNode, n.props = a, n.state = t.memoizedState, n.refs = {}, Vf(t), f = u.contextType, n.context = typeof f == "object" && f !== null ? Dl(f) : ua, n.state = t.memoizedState, f = u.getDerivedStateFromProps, typeof f == "function" && (mc(
        t,
        u,
        f,
        a
      ), n.state = t.memoizedState), typeof u.getDerivedStateFromProps == "function" || typeof n.getSnapshotBeforeUpdate == "function" || typeof n.UNSAFE_componentWillMount != "function" && typeof n.componentWillMount != "function" || (f = n.state, typeof n.componentWillMount == "function" && n.componentWillMount(), typeof n.UNSAFE_componentWillMount == "function" && n.UNSAFE_componentWillMount(), f !== n.state && hc.enqueueReplaceState(n, n.state, null), Ia(t, a, n, e), ka(), n.state = t.memoizedState), typeof n.componentDidMount == "function" && (t.flags |= 4194308), a = !0;
    } else if (l === null) {
      n = t.stateNode;
      var c = t.memoizedProps, i = Xu(u, c);
      n.props = i;
      var h = n.context, r = u.contextType;
      f = ua, typeof r == "object" && r !== null && (f = Dl(r));
      var E = u.getDerivedStateFromProps;
      r = typeof E == "function" || typeof n.getSnapshotBeforeUpdate == "function", c = t.pendingProps !== c, r || typeof n.UNSAFE_componentWillReceiveProps != "function" && typeof n.componentWillReceiveProps != "function" || (c || h !== f) && Yo(
        t,
        n,
        a,
        f
      ), uu = !1;
      var S = t.memoizedState;
      n.state = S, Ia(t, a, n, e), ka(), h = t.memoizedState, c || S !== h || uu ? (typeof E == "function" && (mc(
        t,
        u,
        E,
        a
      ), h = t.memoizedState), (i = uu || Bo(
        t,
        u,
        i,
        a,
        S,
        h,
        f
      )) ? (r || typeof n.UNSAFE_componentWillMount != "function" && typeof n.componentWillMount != "function" || (typeof n.componentWillMount == "function" && n.componentWillMount(), typeof n.UNSAFE_componentWillMount == "function" && n.UNSAFE_componentWillMount()), typeof n.componentDidMount == "function" && (t.flags |= 4194308)) : (typeof n.componentDidMount == "function" && (t.flags |= 4194308), t.memoizedProps = a, t.memoizedState = h), n.props = a, n.state = h, n.context = f, a = i) : (typeof n.componentDidMount == "function" && (t.flags |= 4194308), a = !1);
    } else {
      n = t.stateNode, Kf(l, t), f = t.memoizedProps, r = Xu(u, f), n.props = r, E = t.pendingProps, S = n.context, h = u.contextType, i = ua, typeof h == "object" && h !== null && (i = Dl(h)), c = u.getDerivedStateFromProps, (h = typeof c == "function" || typeof n.getSnapshotBeforeUpdate == "function") || typeof n.UNSAFE_componentWillReceiveProps != "function" && typeof n.componentWillReceiveProps != "function" || (f !== E || S !== i) && Yo(
        t,
        n,
        a,
        i
      ), uu = !1, S = t.memoizedState, n.state = S, Ia(t, a, n, e), ka();
      var g = t.memoizedState;
      f !== E || S !== g || uu || l !== null && l.dependencies !== null && We(l.dependencies) ? (typeof c == "function" && (mc(
        t,
        u,
        c,
        a
      ), g = t.memoizedState), (r = uu || Bo(
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
    )) : Ul(l, t, u, e), t.memoizedState = n.state, l = t.child) : l = Xt(
      l,
      t,
      e
    ), l;
  }
  function ko(l, t, u, a) {
    return Ru(), t.flags |= 256, Ul(l, t, u, a), t.child;
  }
  var bc = {
    dehydrated: null,
    treeContext: null,
    retryLane: 0,
    hydrationErrors: null
  };
  function Tc(l) {
    return { baseLanes: l, cachePool: Xs() };
  }
  function Ec(l, t, u) {
    return l = l !== null ? l.childLanes & ~u : 0, t && (l |= et), l;
  }
  function Io(l, t, u) {
    var a = t.pendingProps, e = !1, n = (t.flags & 128) !== 0, f;
    if ((f = n) || (f = l !== null && l.memoizedState === null ? !1 : (Sl.current & 2) !== 0), f && (e = !0, t.flags &= -129), f = (t.flags & 32) !== 0, t.flags &= -33, l === null) {
      if ($) {
        if (e ? nu(t) : fu(), (l = ol) ? (l = fy(
          l,
          dt
        ), l = l !== null && l.data !== "&" ? l : null, l !== null && (t.memoizedState = {
          dehydrated: l,
          treeContext: It !== null ? { id: _t, overflow: Ot } : null,
          retryLane: 536870912,
          hydrationErrors: null
        }, u = Ns(l), u.return = t, t.child = u, pl = t, ol = null)) : l = null, l === null) throw lu(t);
        return ai(l) ? t.lanes = 32 : t.lanes = 536870912, null;
      }
      var c = a.children;
      return a = a.fallback, e ? (fu(), e = t.mode, c = hn(
        { mode: "hidden", children: c },
        e
      ), a = Nu(
        a,
        e,
        u,
        null
      ), c.return = t, a.return = t, c.sibling = a, t.child = c, a = t.child, a.memoizedState = Tc(u), a.childLanes = Ec(
        l,
        f,
        u
      ), t.memoizedState = bc, ae(null, a)) : (nu(t), zc(t, c));
    }
    var i = l.memoizedState;
    if (i !== null && (c = i.dehydrated, c !== null)) {
      if (n)
        t.flags & 256 ? (nu(t), t.flags &= -257, t = Ac(
          l,
          t,
          u
        )) : t.memoizedState !== null ? (fu(), t.child = l.child, t.flags |= 128, t = null) : (fu(), c = a.fallback, e = t.mode, a = hn(
          { mode: "visible", children: a.children },
          e
        ), c = Nu(
          c,
          e,
          u,
          null
        ), c.flags |= 2, a.return = t, c.return = t, a.sibling = c, t.child = a, Gu(
          t,
          l.child,
          null,
          u
        ), a = t.child, a.memoizedState = Tc(u), a.childLanes = Ec(
          l,
          f,
          u
        ), t.memoizedState = bc, t = ae(null, a));
      else if (nu(t), ai(c)) {
        if (f = c.nextSibling && c.nextSibling.dataset, f) var h = f.dgst;
        f = h, a = Error(d(419)), a.stack = "", a.digest = f, Ka({ value: a, source: null, stack: null }), t = Ac(
          l,
          t,
          u
        );
      } else if (El || fa(l, t, u, !1), f = (u & l.childLanes) !== 0, El || f) {
        if (f = sl, f !== null && (a = Gi(f, u), a !== 0 && a !== i.retryLane))
          throw i.retryLane = a, Uu(l, a), Jl(f, l, a), gc;
        ui(c) || _n(), t = Ac(
          l,
          t,
          u
        );
      } else
        ui(c) ? (t.flags |= 192, t.child = l.child, t = null) : (l = i.treeContext, ol = ht(
          c.nextSibling
        ), pl = t, $ = !0, Pt = null, dt = !1, l !== null && Cs(t, l), t = zc(
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
    ) : (c = Nu(
      c,
      e,
      u,
      null
    ), c.flags |= 2), c.return = t, a.return = t, a.sibling = c, t.child = a, ae(null, a), a = t.child, c = l.child.memoizedState, c === null ? c = Tc(u) : (e = c.cachePool, e !== null ? (i = bl._currentValue, e = e.parent !== i ? { parent: i, pool: i } : e) : e = Xs(), c = {
      baseLanes: c.baseLanes | u,
      cachePool: e
    }), a.memoizedState = c, a.childLanes = Ec(
      l,
      f,
      u
    ), t.memoizedState = bc, ae(l.child, a)) : (nu(t), u = l.child, l = u.sibling, u = Ct(u, {
      mode: "visible",
      children: a.children
    }), u.return = t, u.sibling = null, l !== null && (f = t.deletions, f === null ? (t.deletions = [l], t.flags |= 16) : f.push(l)), t.child = u, t.memoizedState = null, u);
  }
  function zc(l, t) {
    return t = hn(
      { mode: "visible", children: t },
      l.mode
    ), t.return = l, l.child = t;
  }
  function hn(l, t) {
    return l = lt(22, l, null, t), l.lanes = 0, l;
  }
  function Ac(l, t, u) {
    return Gu(t, l.child, null, u), l = zc(
      t,
      t.pendingProps.children
    ), l.flags |= 2, t.memoizedState = null, l;
  }
  function Po(l, t, u) {
    l.lanes |= t;
    var a = l.alternate;
    a !== null && (a.lanes |= t), jf(l.return, t, u);
  }
  function _c(l, t, u, a, e, n) {
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
  function l0(l, t, u) {
    var a = t.pendingProps, e = a.revealOrder, n = a.tail;
    a = a.children;
    var f = Sl.current, c = (f & 2) !== 0;
    if (c ? (f = f & 1 | 2, t.flags |= 128) : f &= 1, p(Sl, f), Ul(l, t, a, u), a = $ ? Va : 0, !c && l !== null && (l.flags & 128) !== 0)
      l: for (l = t.child; l !== null; ) {
        if (l.tag === 13)
          l.memoizedState !== null && Po(l, u, t);
        else if (l.tag === 19)
          Po(l, u, t);
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
        u = e, u === null ? (e = t.child, t.child = null) : (e = u.sibling, u.sibling = null), _c(
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
        _c(
          t,
          !0,
          u,
          null,
          n,
          a
        );
        break;
      case "together":
        _c(
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
      throw Error(d(153));
    if (t.child !== null) {
      for (l = t.child, u = Ct(l, l.pendingProps), t.child = u, u.return = t; l.sibling !== null; )
        l = l.sibling, u = u.sibling = Ct(l, l.pendingProps), u.return = t;
      u.sibling = null;
    }
    return t.child;
  }
  function Oc(l, t) {
    return (l.lanes & t) !== 0 ? !0 : (l = l.dependencies, !!(l !== null && We(l)));
  }
  function Dd(l, t, u) {
    switch (t.tag) {
      case 3:
        ql(t, t.stateNode.containerInfo), tu(t, bl, l.memoizedState.cache), Ru();
        break;
      case 27:
      case 5:
        Ua(t);
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
          return t.flags |= 128, Ff(t), null;
        break;
      case 13:
        var a = t.memoizedState;
        if (a !== null)
          return a.dehydrated !== null ? (nu(t), t.flags |= 128, null) : (u & t.child.childLanes) !== 0 ? Io(l, t, u) : (nu(t), l = Xt(
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
            return l0(
              l,
              t,
              u
            );
          t.flags |= 128;
        }
        if (e = t.memoizedState, e !== null && (e.rendering = null, e.tail = null, e.lastEffect = null), p(Sl, Sl.current), a) break;
        return null;
      case 22:
        return t.lanes = 0, Jo(
          l,
          t,
          u,
          t.pendingProps
        );
      case 24:
        tu(t, bl, l.memoizedState.cache);
    }
    return Xt(l, t, u);
  }
  function t0(l, t, u) {
    if (l !== null)
      if (l.memoizedProps !== t.pendingProps)
        El = !0;
      else {
        if (!Oc(l, u) && (t.flags & 128) === 0)
          return El = !1, Dd(
            l,
            t,
            u
          );
        El = (l.flags & 131072) !== 0;
      }
    else
      El = !1, $ && (t.flags & 1048576) !== 0 && Hs(t, Va, t.index);
    switch (t.lanes = 0, t.tag) {
      case 16:
        l: {
          var a = t.pendingProps;
          if (l = Bu(t.elementType), t.type = l, typeof l == "function")
            Nf(l) ? (a = Xu(l, a), t.tag = 1, t = Fo(
              null,
              t,
              l,
              a,
              u
            )) : (t.tag = 0, t = rc(
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
                t.tag = 11, t = xo(
                  null,
                  t,
                  l,
                  a,
                  u
                );
                break l;
              } else if (e === W) {
                t.tag = 14, t = Vo(
                  null,
                  t,
                  l,
                  a,
                  u
                );
                break l;
              }
            }
            throw t = Ut(l) || l, Error(d(306, t, ""));
          }
        }
        return t;
      case 0:
        return rc(
          l,
          t,
          t.type,
          t.pendingProps,
          u
        );
      case 1:
        return a = t.type, e = Xu(
          a,
          t.pendingProps
        ), Fo(
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
          ), l === null) throw Error(d(387));
          a = t.pendingProps;
          var n = t.memoizedState;
          e = n.element, Kf(l, t), Ia(t, a, null, u);
          var f = t.memoizedState;
          if (a = f.cache, tu(t, bl, a), a !== n.cache && Xf(
            t,
            [bl],
            u,
            !0
          ), ka(), a = f.element, n.isDehydrated)
            if (n = {
              element: a,
              isDehydrated: !1,
              cache: f.cache
            }, t.updateQueue.baseState = n, t.memoizedState = n, t.flags & 256) {
              t = ko(
                l,
                t,
                a,
                u
              );
              break l;
            } else if (a !== e) {
              e = ot(
                Error(d(424)),
                t
              ), Ka(e), t = ko(
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
              for (ol = ht(l.firstChild), pl = t, $ = !0, Pt = null, dt = !0, u = Ks(
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
        return mn(l, t), l === null ? (u = vy(
          t.type,
          null,
          t.pendingProps,
          null
        )) ? t.memoizedState = u : $ || (u = t.type, l = t.pendingProps, a = Rn(
          L.current
        ).createElement(u), a[Ml] = t, a[Ql] = l, Nl(a, u, l), _l(a), t.stateNode = a) : t.memoizedState = vy(
          t.type,
          l.memoizedProps,
          t.pendingProps,
          l.memoizedState
        ), null;
      case 27:
        return Ua(t), l === null && $ && (a = t.stateNode = sy(
          t.type,
          t.pendingProps,
          L.current
        ), pl = t, dt = !0, e = ol, mu(t.type) ? (ei = e, ol = ht(a.firstChild)) : ol = e), Ul(
          l,
          t,
          t.pendingProps.children,
          u
        ), mn(l, t), l === null && (t.flags |= 4194304), t.child;
      case 5:
        return l === null && $ && ((e = a = ol) && (a = em(
          a,
          t.type,
          t.pendingProps,
          dt
        ), a !== null ? (t.stateNode = a, pl = t, ol = ht(a.firstChild), dt = !1, e = !0) : e = !1), e || lu(t)), Ua(t), e = t.type, n = t.pendingProps, f = l !== null ? l.memoizedProps : null, a = n.children, Pc(e, n) ? a = null : f !== null && Pc(e, f) && (t.flags |= 32), t.memoizedState !== null && (e = If(
          l,
          t,
          bd,
          null,
          null,
          u
        ), re._currentValue = e), mn(l, t), Ul(l, t, a, u), t.child;
      case 6:
        return l === null && $ && ((l = u = ol) && (u = nm(
          u,
          t.pendingProps,
          dt
        ), u !== null ? (t.stateNode = u, pl = t, ol = null, l = !0) : l = !1), l || lu(t)), null;
      case 13:
        return Io(l, t, u);
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
        return xo(
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
        return Vo(
          l,
          t,
          t.type,
          t.pendingProps,
          u
        );
      case 15:
        return Ko(
          l,
          t,
          t.type,
          t.pendingProps,
          u
        );
      case 19:
        return l0(l, t, u);
      case 31:
        return pd(l, t, u);
      case 22:
        return Jo(
          l,
          t,
          u,
          t.pendingProps
        );
      case 24:
        return Cu(t), a = Dl(bl), l === null ? (e = Lf(), e === null && (e = sl, n = Qf(), e.pooledCache = n, n.refCount++, n !== null && (e.pooledCacheLanes |= u), e = n), t.memoizedState = { parent: a, cache: e }, Vf(t), tu(t, bl, e)) : ((l.lanes & u) !== 0 && (Kf(l, t), Ia(t, null, null, u), ka()), e = l.memoizedState, n = t.memoizedState, e.parent !== a ? (e = { parent: a, cache: a }, t.memoizedState = e, t.lanes === 0 && (t.memoizedState = t.updateQueue.baseState = e), tu(t, bl, a)) : (a = n.cache, tu(t, bl, a), a !== e.cache && Xf(
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
    throw Error(d(156, t.tag));
  }
  function Qt(l) {
    l.flags |= 4;
  }
  function Mc(l, t, u, a, e) {
    if ((t = (l.mode & 32) !== 0) && (t = !1), t) {
      if (l.flags |= 16777216, (e & 335544128) === e)
        if (l.stateNode.complete) l.flags |= 8192;
        else if (D0()) l.flags |= 8192;
        else
          throw Yu = Ie, xf;
    } else l.flags &= -16777217;
  }
  function u0(l, t) {
    if (t.type !== "stylesheet" || (t.state.loading & 4) !== 0)
      l.flags &= -16777217;
    else if (l.flags |= 16777216, !gy(t))
      if (D0()) l.flags |= 8192;
      else
        throw Yu = Ie, xf;
  }
  function Sn(l, t) {
    t !== null && (l.flags |= 4), l.flags & 16384 && (t = l.tag !== 22 ? qi() : 536870912, l.lanes |= t, ra |= t);
  }
  function ee(l, t) {
    if (!$)
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
  function Ud(l, t, u) {
    var a = t.pendingProps;
    switch (qf(t), t.tag) {
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
        return u = t.stateNode, a = null, l !== null && (a = l.memoizedState.cache), t.memoizedState.cache !== a && (t.flags |= 2048), Yt(bl), hl(), u.pendingContext && (u.context = u.pendingContext, u.pendingContext = null), (l === null || l.child === null) && (na(t) ? Qt(t) : l === null || l.memoizedState.isDehydrated && (t.flags & 256) === 0 || (t.flags |= 1024, Yf())), yl(t), null;
      case 26:
        var e = t.type, n = t.memoizedState;
        return l === null ? (Qt(t), n !== null ? (yl(t), u0(t, n)) : (yl(t), Mc(
          t,
          e,
          null,
          a,
          u
        ))) : n ? n !== l.memoizedState ? (Qt(t), yl(t), u0(t, n)) : (yl(t), t.flags &= -16777217) : (l = l.memoizedProps, l !== a && Qt(t), yl(t), Mc(
          t,
          e,
          l,
          a,
          u
        )), null;
      case 27:
        if (Me(t), u = L.current, e = t.type, l !== null && t.stateNode != null)
          l.memoizedProps !== a && Qt(t);
        else {
          if (!a) {
            if (t.stateNode === null)
              throw Error(d(166));
            return yl(t), null;
          }
          l = N.current, na(t) ? qs(t) : (l = sy(e, a, u), t.stateNode = l, Qt(t));
        }
        return yl(t), null;
      case 5:
        if (Me(t), e = t.type, l !== null && t.stateNode != null)
          l.memoizedProps !== a && Qt(t);
        else {
          if (!a) {
            if (t.stateNode === null)
              throw Error(d(166));
            return yl(t), null;
          }
          if (n = N.current, na(t))
            qs(t);
          else {
            var f = Rn(
              L.current
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
        return yl(t), Mc(
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
            throw Error(d(166));
          if (l = L.current, na(t)) {
            if (l = t.stateNode, u = t.memoizedProps, a = null, e = pl, e !== null)
              switch (e.tag) {
                case 27:
                case 5:
                  a = e.memoizedProps;
              }
            l[Ml] = t, l = !!(l.nodeValue === u || a !== null && a.suppressHydrationWarning === !0 || I0(l.nodeValue, u)), l || lu(t, !0);
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
              if (!a) throw Error(d(318));
              if (l = t.memoizedState, l = l !== null ? l.dehydrated : null, !l) throw Error(d(557));
              l[Ml] = t;
            } else
              Ru(), (t.flags & 128) === 0 && (t.memoizedState = null), t.flags |= 4;
            yl(t), l = !1;
          } else
            u = Yf(), l !== null && l.memoizedState !== null && (l.memoizedState.hydrationErrors = u), l = !0;
          if (!l)
            return t.flags & 256 ? (ut(t), t) : (ut(t), null);
          if ((t.flags & 128) !== 0)
            throw Error(d(558));
        }
        return yl(t), null;
      case 13:
        if (a = t.memoizedState, l === null || l.memoizedState !== null && l.memoizedState.dehydrated !== null) {
          if (e = na(t), a !== null && a.dehydrated !== null) {
            if (l === null) {
              if (!e) throw Error(d(318));
              if (e = t.memoizedState, e = e !== null ? e.dehydrated : null, !e) throw Error(d(317));
              e[Ml] = t;
            } else
              Ru(), (t.flags & 128) === 0 && (t.memoizedState = null), t.flags |= 4;
            yl(t), e = !1;
          } else
            e = Yf(), l !== null && l.memoizedState !== null && (l.memoizedState.hydrationErrors = e), e = !0;
          if (!e)
            return t.flags & 256 ? (ut(t), t) : (ut(t), null);
        }
        return ut(t), (t.flags & 128) !== 0 ? (t.lanes = u, t) : (u = a !== null, l = l !== null && l.memoizedState !== null, u && (a = t.child, e = null, a.alternate !== null && a.alternate.memoizedState !== null && a.alternate.memoizedState.cachePool !== null && (e = a.alternate.memoizedState.cachePool.pool), n = null, a.memoizedState !== null && a.memoizedState.cachePool !== null && (n = a.memoizedState.cachePool.pool), n !== e && (a.flags |= 2048)), u !== l && u && (t.child.flags |= 8192), Sn(t, t.updateQueue), yl(t), null);
      case 4:
        return hl(), l === null && Wc(t.stateNode.containerInfo), yl(t), null;
      case 10:
        return Yt(t.type), yl(t), null;
      case 19:
        if (z(Sl), a = t.memoizedState, a === null) return yl(t), null;
        if (e = (t.flags & 128) !== 0, n = a.rendering, n === null)
          if (e) ee(a, !1);
          else {
            if (ml !== 0 || l !== null && (l.flags & 128) !== 0)
              for (l = t.child; l !== null; ) {
                if (n = un(l), n !== null) {
                  for (t.flags |= 128, ee(a, !1), l = n.updateQueue, t.updateQueue = l, Sn(t, l), t.subtreeFlags = 0, l = u, u = t.child; u !== null; )
                    Us(u, l), u = u.sibling;
                  return p(
                    Sl,
                    Sl.current & 1 | 2
                  ), $ && qt(t, a.treeForkCount), t.child;
                }
                l = l.sibling;
              }
            a.tail !== null && Fl() > En && (t.flags |= 128, e = !0, ee(a, !1), t.lanes = 4194304);
          }
        else {
          if (!e)
            if (l = un(n), l !== null) {
              if (t.flags |= 128, e = !0, l = l.updateQueue, t.updateQueue = l, Sn(t, l), ee(a, !0), a.tail === null && a.tailMode === "hidden" && !n.alternate && !$)
                return yl(t), null;
            } else
              2 * Fl() - a.renderingStartTime > En && u !== 536870912 && (t.flags |= 128, e = !0, ee(a, !1), t.lanes = 4194304);
          a.isBackwards ? (n.sibling = t.child, t.child = n) : (l = a.last, l !== null ? l.sibling = n : t.child = n, a.last = n);
        }
        return a.tail !== null ? (l = a.tail, a.rendering = l, a.tail = l.sibling, a.renderingStartTime = Fl(), l.sibling = null, u = Sl.current, p(
          Sl,
          e ? u & 1 | 2 : u & 1
        ), $ && qt(t, a.treeForkCount), l) : (yl(t), null);
      case 22:
      case 23:
        return ut(t), $f(), a = t.memoizedState !== null, l !== null ? l.memoizedState !== null !== a && (t.flags |= 8192) : a && (t.flags |= 8192), a ? (u & 536870912) !== 0 && (t.flags & 128) === 0 && (yl(t), t.subtreeFlags & 6 && (t.flags |= 8192)) : yl(t), u = t.updateQueue, u !== null && Sn(t, u.retryQueue), u = null, l !== null && l.memoizedState !== null && l.memoizedState.cachePool !== null && (u = l.memoizedState.cachePool.pool), a = null, t.memoizedState !== null && t.memoizedState.cachePool !== null && (a = t.memoizedState.cachePool.pool), a !== u && (t.flags |= 2048), l !== null && z(qu), null;
      case 24:
        return u = null, l !== null && (u = l.memoizedState.cache), t.memoizedState.cache !== u && (t.flags |= 2048), Yt(bl), yl(t), null;
      case 25:
        return null;
      case 30:
        return null;
    }
    throw Error(d(156, t.tag));
  }
  function Nd(l, t) {
    switch (qf(t), t.tag) {
      case 1:
        return l = t.flags, l & 65536 ? (t.flags = l & -65537 | 128, t) : null;
      case 3:
        return Yt(bl), hl(), l = t.flags, (l & 65536) !== 0 && (l & 128) === 0 ? (t.flags = l & -65537 | 128, t) : null;
      case 26:
      case 27:
      case 5:
        return Me(t), null;
      case 31:
        if (t.memoizedState !== null) {
          if (ut(t), t.alternate === null)
            throw Error(d(340));
          Ru();
        }
        return l = t.flags, l & 65536 ? (t.flags = l & -65537 | 128, t) : null;
      case 13:
        if (ut(t), l = t.memoizedState, l !== null && l.dehydrated !== null) {
          if (t.alternate === null)
            throw Error(d(340));
          Ru();
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
        return ut(t), $f(), l !== null && z(qu), l = t.flags, l & 65536 ? (t.flags = l & -65537 | 128, t) : null;
      case 24:
        return Yt(bl), null;
      case 25:
        return null;
      default:
        return null;
    }
  }
  function a0(l, t) {
    switch (qf(t), t.tag) {
      case 3:
        Yt(bl), hl();
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
        t.memoizedState !== null && ut(t);
        break;
      case 13:
        ut(t);
        break;
      case 19:
        z(Sl);
        break;
      case 10:
        Yt(t.type);
        break;
      case 22:
      case 23:
        ut(t), $f(), l !== null && z(qu);
        break;
      case 24:
        Yt(bl);
    }
  }
  function ne(l, t) {
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
  function e0(l) {
    var t = l.updateQueue;
    if (t !== null) {
      var u = l.stateNode;
      try {
        ws(t, u);
      } catch (a) {
        al(l, l.return, a);
      }
    }
  }
  function n0(l, t, u) {
    u.props = Xu(
      l.type,
      l.memoizedProps
    ), u.state = l.memoizedState;
    try {
      u.componentWillUnmount();
    } catch (a) {
      al(l, t, a);
    }
  }
  function fe(l, t) {
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
  function f0(l) {
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
  function pc(l, t, u) {
    try {
      var a = l.stateNode;
      Id(a, l.type, u, t), a[Ql] = t;
    } catch (e) {
      al(l, l.return, e);
    }
  }
  function c0(l) {
    return l.tag === 5 || l.tag === 3 || l.tag === 26 || l.tag === 27 && mu(l.type) || l.tag === 4;
  }
  function Dc(l) {
    l: for (; ; ) {
      for (; l.sibling === null; ) {
        if (l.return === null || c0(l.return)) return null;
        l = l.return;
      }
      for (l.sibling.return = l.return, l = l.sibling; l.tag !== 5 && l.tag !== 6 && l.tag !== 18; ) {
        if (l.tag === 27 && mu(l.type) || l.flags & 2 || l.child === null || l.tag === 4) continue l;
        l.child.return = l, l = l.child;
      }
      if (!(l.flags & 2)) return l.stateNode;
    }
  }
  function Uc(l, t, u) {
    var a = l.tag;
    if (a === 5 || a === 6)
      l = l.stateNode, t ? (u.nodeType === 9 ? u.body : u.nodeName === "HTML" ? u.ownerDocument.body : u).insertBefore(l, t) : (t = u.nodeType === 9 ? u.body : u.nodeName === "HTML" ? u.ownerDocument.body : u, t.appendChild(l), u = u._reactRootContainer, u != null || t.onclick !== null || (t.onclick = Rt));
    else if (a !== 4 && (a === 27 && mu(l.type) && (u = l.stateNode, t = null), l = l.child, l !== null))
      for (Uc(l, t, u), l = l.sibling; l !== null; )
        Uc(l, t, u), l = l.sibling;
  }
  function gn(l, t, u) {
    var a = l.tag;
    if (a === 5 || a === 6)
      l = l.stateNode, t ? u.insertBefore(l, t) : u.appendChild(l);
    else if (a !== 4 && (a === 27 && mu(l.type) && (u = l.stateNode), l = l.child, l !== null))
      for (gn(l, t, u), l = l.sibling; l !== null; )
        gn(l, t, u), l = l.sibling;
  }
  function i0(l) {
    var t = l.stateNode, u = l.memoizedProps;
    try {
      for (var a = l.type, e = t.attributes; e.length; )
        t.removeAttributeNode(e[0]);
      Nl(t, a, u), t[Ml] = l, t[Ql] = u;
    } catch (n) {
      al(l, l.return, n);
    }
  }
  var Zt = !1, zl = !1, Nc = !1, s0 = typeof WeakSet == "function" ? WeakSet : Set, Ol = null;
  function Rd(l, t) {
    if (l = l.containerInfo, kc = jn, l = Ts(l), Af(l)) {
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
    for (Ic = { focusedElem: l, selectionRange: u }, jn = !1, Ol = t; Ol !== null; )
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
                  var D = Xu(
                    u.type,
                    e
                  );
                  l = a.getSnapshotBeforeUpdate(
                    D,
                    n
                  ), a.__reactInternalSnapshotBeforeUpdate = l;
                } catch (q) {
                  al(
                    u,
                    u.return,
                    q
                  );
                }
              }
              break;
            case 3:
              if ((l & 1024) !== 0) {
                if (l = t.stateNode.containerInfo, u = l.nodeType, u === 9)
                  ti(l);
                else if (u === 1)
                  switch (l.nodeName) {
                    case "HEAD":
                    case "HTML":
                    case "BODY":
                      ti(l);
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
              if ((l & 1024) !== 0) throw Error(d(163));
          }
          if (l = t.sibling, l !== null) {
            l.return = t.return, Ol = l;
            break;
          }
          Ol = t.return;
        }
  }
  function o0(l, t, u) {
    var a = u.flags;
    switch (u.tag) {
      case 0:
      case 11:
      case 15:
        xt(l, u), a & 4 && ne(5, u);
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
            var e = Xu(
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
        a & 64 && e0(u), a & 512 && fe(u, u.return);
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
            ws(l, t);
          } catch (f) {
            al(u, u.return, f);
          }
        }
        break;
      case 27:
        t === null && a & 4 && i0(u);
      case 26:
      case 5:
        xt(l, u), t === null && a & 4 && f0(u), a & 512 && fe(u, u.return);
        break;
      case 12:
        xt(l, u);
        break;
      case 31:
        xt(l, u), a & 4 && d0(l, u);
        break;
      case 13:
        xt(l, u), a & 4 && m0(l, u), a & 64 && (l = u.memoizedState, l !== null && (l = l.dehydrated, l !== null && (u = Qd.bind(
          null,
          u
        ), fm(l, u))));
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
  function y0(l) {
    var t = l.alternate;
    t !== null && (l.alternate = null, y0(t)), l.child = null, l.deletions = null, l.sibling = null, l.tag === 5 && (t = l.stateNode, t !== null && nf(t)), l.stateNode = null, l.return = null, l.dependencies = null, l.memoizedProps = null, l.memoizedState = null, l.pendingProps = null, l.stateNode = null, l.updateQueue = null;
  }
  var vl = null, Ll = !1;
  function Lt(l, t, u) {
    for (u = u.child; u !== null; )
      v0(l, t, u), u = u.sibling;
  }
  function v0(l, t, u) {
    if (kl && typeof kl.onCommitFiberUnmount == "function")
      try {
        kl.onCommitFiberUnmount(Na, u);
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
        mu(u.type) && (vl = u.stateNode, Ll = !1), Lt(
          l,
          t,
          u
        ), he(u.stateNode), vl = a, Ll = e;
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
        vl !== null && (Ll ? (l = vl, ey(
          l.nodeType === 9 ? l.body : l.nodeName === "HTML" ? l.ownerDocument.body : l,
          u.stateNode
        ), Ma(l)) : ey(vl, u.stateNode));
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
        zl || (Mt(u, t), a = u.stateNode, typeof a.componentWillUnmount == "function" && n0(
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
  function d0(l, t) {
    if (t.memoizedState === null && (l = t.alternate, l !== null && (l = l.memoizedState, l !== null))) {
      l = l.dehydrated;
      try {
        Ma(l);
      } catch (u) {
        al(t, t.return, u);
      }
    }
  }
  function m0(l, t) {
    if (t.memoizedState === null && (l = t.alternate, l !== null && (l = l.memoizedState, l !== null && (l = l.dehydrated, l !== null))))
      try {
        Ma(l);
      } catch (u) {
        al(t, t.return, u);
      }
  }
  function Hd(l) {
    switch (l.tag) {
      case 31:
      case 13:
      case 19:
        var t = l.stateNode;
        return t === null && (t = l.stateNode = new s0()), t;
      case 22:
        return l = l.stateNode, t = l._retryCache, t === null && (t = l._retryCache = new s0()), t;
      default:
        throw Error(d(435, l.tag));
    }
  }
  function rn(l, t) {
    var u = Hd(l);
    t.forEach(function(a) {
      if (!u.has(a)) {
        u.add(a);
        var e = Zd.bind(null, l, a);
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
              if (mu(c.type)) {
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
        if (vl === null) throw Error(d(160));
        v0(n, f, e), vl = null, Ll = !1, n = e.alternate, n !== null && (n.return = null), e.return = null;
      }
    if (t.subtreeFlags & 13886)
      for (t = t.child; t !== null; )
        h0(t, l), t = t.sibling;
  }
  var bt = null;
  function h0(l, t) {
    var u = l.alternate, a = l.flags;
    switch (l.tag) {
      case 0:
      case 11:
      case 14:
      case 15:
        xl(t, l), Vl(l), a & 4 && (cu(3, l, l.return), ne(3, l), cu(5, l, l.return));
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
                      n = e.getElementsByTagName("title")[0], (!n || n[Ca] || n[Ml] || n.namespaceURI === "http://www.w3.org/2000/svg" || n.hasAttribute("itemprop")) && (n = e.createElement(a), e.head.insertBefore(
                        n,
                        e.querySelector("head > title")
                      )), Nl(n, a, u), n[Ml] = l, _l(n), a = n;
                      break l;
                    case "link":
                      var f = hy(
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
                      if (f = hy(
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
                      throw Error(d(468, a));
                  }
                  n[Ml] = l, _l(n), a = n;
                }
                l.stateNode = a;
              } else
                Sy(
                  e,
                  l.type,
                  l.stateNode
                );
            else
              l.stateNode = my(
                e,
                a,
                l.memoizedProps
              );
          else
            n !== a ? (n === null ? u.stateNode !== null && (u = u.stateNode, u.parentNode.removeChild(u)) : n.count--, a === null ? Sy(
              e,
              l.type,
              l.stateNode
            ) : my(
              e,
              a,
              l.memoizedProps
            )) : a === null && l.stateNode !== null && pc(
              l,
              l.memoizedProps,
              u.memoizedProps
            );
        }
        break;
      case 27:
        xl(t, l), Vl(l), a & 512 && (zl || u === null || Mt(u, u.return)), u !== null && a & 4 && pc(
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
            al(l, l.return, D);
          }
        }
        a & 4 && l.stateNode != null && (e = l.memoizedProps, pc(
          l,
          e,
          u !== null ? u.memoizedProps : e
        )), a & 1024 && (Nc = !0);
        break;
      case 6:
        if (xl(t, l), Vl(l), a & 4) {
          if (l.stateNode === null)
            throw Error(d(162));
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
        Nc && (Nc = !1, S0(l));
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
        xl(t, l), Vl(l), l.child.flags & 8192 && l.memoizedState !== null != (u !== null && u.memoizedState !== null) && (Tn = Fl()), a & 4 && (a = l.updateQueue, a !== null && (l.updateQueue = null, rn(l, a)));
        break;
      case 22:
        e = l.memoizedState !== null;
        var i = u !== null && u.memoizedState !== null, h = Zt, r = zl;
        if (Zt = h || e, zl = r || i, xl(t, l), zl = r, Zt = h, Vl(l), a & 8192)
          l: for (t = l.stateNode, t._visibility = e ? t._visibility & -2 : t._visibility | 1, e && (u === null || i || Zt || zl || Qu(l)), u = null, t = l; ; ) {
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
                  e ? ny(g, !0) : ny(i.stateNode, !1);
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
          if (c0(a)) {
            u = a;
            break;
          }
          a = a.return;
        }
        if (u == null) throw Error(d(160));
        switch (u.tag) {
          case 27:
            var e = u.stateNode, n = Dc(l);
            gn(l, n, e);
            break;
          case 5:
            var f = u.stateNode;
            u.flags & 32 && ($u(f, ""), u.flags &= -33);
            var c = Dc(l);
            gn(l, c, f);
            break;
          case 3:
          case 4:
            var i = u.stateNode.containerInfo, h = Dc(l);
            Uc(
              l,
              h,
              i
            );
            break;
          default:
            throw Error(d(161));
        }
      } catch (r) {
        al(l, l.return, r);
      }
      l.flags &= -3;
    }
    t & 4096 && (l.flags &= -4097);
  }
  function S0(l) {
    if (l.subtreeFlags & 1024)
      for (l = l.child; l !== null; ) {
        var t = l;
        S0(t), t.tag === 5 && t.flags & 1024 && t.stateNode.reset(), l = l.sibling;
      }
  }
  function xt(l, t) {
    if (t.subtreeFlags & 8772)
      for (t = t.child; t !== null; )
        o0(l, t.alternate, t), t = t.sibling;
  }
  function Qu(l) {
    for (l = l.child; l !== null; ) {
      var t = l;
      switch (t.tag) {
        case 0:
        case 11:
        case 14:
        case 15:
          cu(4, t, t.return), Qu(t);
          break;
        case 1:
          Mt(t, t.return);
          var u = t.stateNode;
          typeof u.componentWillUnmount == "function" && n0(
            t,
            t.return,
            u
          ), Qu(t);
          break;
        case 27:
          he(t.stateNode);
        case 26:
        case 5:
          Mt(t, t.return), Qu(t);
          break;
        case 22:
          t.memoizedState === null && Qu(t);
          break;
        case 30:
          Qu(t);
          break;
        default:
          Qu(t);
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
          ), ne(4, n);
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
                  Js(i[e], c);
            } catch (h) {
              al(a, a.return, h);
            }
          }
          u && f & 64 && e0(n), fe(n, n.return);
          break;
        case 27:
          i0(n);
        case 26:
        case 5:
          Vt(
            e,
            n,
            u
          ), u && a === null && f & 4 && f0(n), fe(n, n.return);
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
          ), u && f & 4 && d0(e, n);
          break;
        case 13:
          Vt(
            e,
            n,
            u
          ), u && f & 4 && m0(e, n);
          break;
        case 22:
          n.memoizedState === null && Vt(
            e,
            n,
            u
          ), fe(n, n.return);
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
  function Rc(l, t) {
    var u = null;
    l !== null && l.memoizedState !== null && l.memoizedState.cachePool !== null && (u = l.memoizedState.cachePool.pool), l = null, t.memoizedState !== null && t.memoizedState.cachePool !== null && (l = t.memoizedState.cachePool.pool), l !== u && (l != null && l.refCount++, u != null && Ja(u));
  }
  function Hc(l, t) {
    l = null, t.alternate !== null && (l = t.alternate.memoizedState.cache), t = t.memoizedState.cache, t !== l && (t.refCount++, l != null && Ja(l));
  }
  function Tt(l, t, u, a) {
    if (t.subtreeFlags & 10256)
      for (t = t.child; t !== null; )
        g0(
          l,
          t,
          u,
          a
        ), t = t.sibling;
  }
  function g0(l, t, u, a) {
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
        ), e & 2048 && ne(9, t);
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
        ), e & 2048 && (l = null, t.alternate !== null && (l = t.alternate.memoizedState.cache), t = t.memoizedState.cache, t !== l && (t.refCount++, l != null && Ja(l)));
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
        ) : ce(l, t) : n._visibility & 2 ? Tt(
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
        )), e & 2048 && Rc(f, t);
        break;
      case 24:
        Tt(
          l,
          t,
          u,
          a
        ), e & 2048 && Hc(t.alternate, t);
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
          ), ne(8, f);
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
          ) : ce(
            n,
            f
          ) : (r._visibility |= 2, ha(
            n,
            f,
            c,
            i,
            e
          )), e && h & 2048 && Rc(
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
          ), e && h & 2048 && Hc(f.alternate, f);
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
  function ce(l, t) {
    if (t.subtreeFlags & 10256)
      for (t = t.child; t !== null; ) {
        var u = l, a = t, e = a.flags;
        switch (a.tag) {
          case 22:
            ce(u, a), e & 2048 && Rc(
              a.alternate,
              a
            );
            break;
          case 24:
            ce(u, a), e & 2048 && Hc(a.alternate, a);
            break;
          default:
            ce(u, a);
        }
        t = t.sibling;
      }
  }
  var ie = 8192;
  function Sa(l, t, u) {
    if (l.subtreeFlags & ie)
      for (l = l.child; l !== null; )
        r0(
          l,
          t,
          u
        ), l = l.sibling;
  }
  function r0(l, t, u) {
    switch (l.tag) {
      case 26:
        Sa(
          l,
          t,
          u
        ), l.flags & ie && l.memoizedState !== null && rm(
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
        l.memoizedState === null && (a = l.alternate, a !== null && a.memoizedState !== null ? (a = ie, ie = 16777216, Sa(
          l,
          t,
          u
        ), ie = a) : Sa(
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
  function b0(l) {
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
        for (var u = 0; u < t.length; u++) {
          var a = t[u];
          Ol = a, E0(
            a,
            l
          );
        }
      b0(l);
    }
    if (l.subtreeFlags & 10256)
      for (l = l.child; l !== null; )
        T0(l), l = l.sibling;
  }
  function T0(l) {
    switch (l.tag) {
      case 0:
      case 11:
      case 15:
        se(l), l.flags & 2048 && cu(9, l, l.return);
        break;
      case 3:
        se(l);
        break;
      case 12:
        se(l);
        break;
      case 22:
        var t = l.stateNode;
        l.memoizedState !== null && t._visibility & 2 && (l.return === null || l.return.tag !== 13) ? (t._visibility &= -3, bn(l)) : se(l);
        break;
      default:
        se(l);
    }
  }
  function bn(l) {
    var t = l.deletions;
    if ((l.flags & 16) !== 0) {
      if (t !== null)
        for (var u = 0; u < t.length; u++) {
          var a = t[u];
          Ol = a, E0(
            a,
            l
          );
        }
      b0(l);
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
  function E0(l, t) {
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
          Ja(u.memoizedState.cache);
      }
      if (a = u.child, a !== null) a.return = u, Ol = a;
      else
        l: for (u = l; Ol !== null; ) {
          a = Ol;
          var e = a.sibling, n = a.return;
          if (y0(a), a === u) {
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
  var Cd = {
    getCacheForType: function(l) {
      var t = Dl(bl), u = t.data.get(l);
      return u === void 0 && (u = l(), t.data.set(l, u)), u;
    },
    cacheSignal: function() {
      return Dl(bl).controller.signal;
    }
  }, qd = typeof WeakMap == "function" ? WeakMap : Map, P = 0, sl = null, x = null, J = 0, ul = 0, at = null, iu = !1, ga = !1, Cc = !1, Kt = 0, ml = 0, su = 0, Zu = 0, qc = 0, et = 0, ra = 0, oe = null, Kl = null, Bc = !1, Tn = 0, z0 = 0, En = 1 / 0, zn = null, ou = null, Al = 0, yu = null, ba = null, Jt = 0, Yc = 0, Gc = null, A0 = null, ye = 0, jc = null;
  function nt() {
    return (P & 2) !== 0 && J !== 0 ? J & -J : b.T !== null ? Vc() : ji();
  }
  function _0() {
    if (et === 0)
      if ((J & 536870912) === 0 || $) {
        var l = Ue;
        Ue <<= 1, (Ue & 3932160) === 0 && (Ue = 262144), et = l;
      } else et = 536870912;
    return l = tt.current, l !== null && (l.flags |= 32), et;
  }
  function Jl(l, t, u) {
    (l === sl && (ul === 2 || ul === 9) || l.cancelPendingCommit !== null) && (Ta(l, 0), vu(
      l,
      J,
      et,
      !1
    )), Ha(l, u), ((P & 2) === 0 || l !== sl) && (l === sl && ((P & 2) === 0 && (Zu |= u), ml === 4 && vu(
      l,
      J,
      et,
      !1
    )), pt(l));
  }
  function O0(l, t, u) {
    if ((P & 6) !== 0) throw Error(d(327));
    var a = !u && (t & 127) === 0 && (t & l.expiredLanes) === 0 || Ra(l, t), e = a ? Gd(l, t) : Qc(l, t, !0), n = a;
    do {
      if (e === 0) {
        ga && !a && vu(l, t, 0, !1);
        break;
      } else {
        if (u = l.current.alternate, n && !Bd(u)) {
          e = Qc(l, t, !1), n = !1;
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
              e = oe;
              var i = c.current.memoizedState.isDehydrated;
              if (i && (Ta(c, f).flags |= 256), f = Qc(
                c,
                f,
                !1
              ), f !== 2) {
                if (Cc && !i) {
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
              throw Error(d(345));
            case 4:
              if ((t & 4194048) !== t) break;
            case 6:
              vu(
                a,
                t,
                et,
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
              throw Error(d(329));
          }
          if ((t & 62914560) === t && (e = Tn + 300 - Fl(), 10 < e)) {
            if (vu(
              a,
              t,
              et,
              !iu
            ), Re(a, 0, !0) !== 0) break l;
            Jt = t, a.timeoutHandle = uy(
              M0.bind(
                null,
                a,
                u,
                Kl,
                zn,
                Bc,
                t,
                et,
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
          M0(
            a,
            u,
            Kl,
            zn,
            Bc,
            t,
            et,
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
  function M0(l, t, u, a, e, n, f, c, i, h, r, E, S, g) {
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
      }, r0(
        t,
        n,
        E
      );
      var D = (n & 62914560) === n ? Tn - Fl() : (n & 4194048) === n ? z0 - Fl() : 0;
      if (D = bm(
        E,
        D
      ), D !== null) {
        Jt = n, l.cancelPendingCommit = D(
          q0.bind(
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
    q0(
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
  function Bd(l) {
    for (var t = l; ; ) {
      var u = t.tag;
      if ((u === 0 || u === 11 || u === 15) && t.flags & 16384 && (u = t.updateQueue, u !== null && (u = u.stores, u !== null)))
        for (var a = 0; a < u.length; a++) {
          var e = u[a], n = e.getSnapshot;
          e = e.value;
          try {
            if (!Pl(n(), e)) return !1;
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
    t &= ~qc, t &= ~Zu, l.suspendedLanes |= t, l.pingedLanes &= ~t, a && (l.warmLanes |= t), a = l.expirationTimes;
    for (var e = t; 0 < e; ) {
      var n = 31 - Il(e), f = 1 << n;
      a[n] = -1, e &= ~f;
    }
    u !== 0 && Bi(l, u, t);
  }
  function An() {
    return (P & 6) === 0 ? (ve(0), !1) : !0;
  }
  function Xc() {
    if (x !== null) {
      if (ul === 0)
        var l = x.return;
      else
        l = x, Bt = Hu = null, tc(l), oa = null, Wa = 0, l = x;
      for (; l !== null; )
        a0(l.alternate, l), l = l.return;
      x = null;
    }
  }
  function Ta(l, t) {
    var u = l.timeoutHandle;
    u !== -1 && (l.timeoutHandle = -1, tm(u)), u = l.cancelPendingCommit, u !== null && (l.cancelPendingCommit = null, u()), Jt = 0, Xc(), sl = l, x = u = Ct(l.current, null), J = t, ul = 0, at = null, iu = !1, ga = Ra(l, t), Cc = !1, ra = et = qc = Zu = su = ml = 0, Kl = oe = null, Bc = !1, (t & 8) !== 0 && (t |= t & 32);
    var a = l.entangledLanes;
    if (a !== 0)
      for (l = l.entanglements, a &= t; 0 < a; ) {
        var e = 31 - Il(a), n = 1 << e;
        t |= l[e], a &= ~n;
      }
    return Kt = t, xe(), u;
  }
  function p0(l, t) {
    X = null, b.H = ue, t === sa || t === ke ? (t = Ls(), ul = 3) : t === xf ? (t = Ls(), ul = 4) : ul = t === gc ? 8 : t !== null && typeof t == "object" && typeof t.then == "function" ? 6 : 1, at = t, x === null && (ml = 1, vn(
      l,
      ot(t, l.current)
    ));
  }
  function D0() {
    var l = tt.current;
    return l === null ? !0 : (J & 4194048) === J ? mt === null : (J & 62914560) === J || (J & 536870912) !== 0 ? l === mt : !1;
  }
  function U0() {
    var l = b.H;
    return b.H = ue, l === null ? ue : l;
  }
  function N0() {
    var l = b.A;
    return b.A = Cd, l;
  }
  function _n() {
    ml = 4, iu || (J & 4194048) !== J && tt.current !== null || (ga = !0), (su & 134217727) === 0 && (Zu & 134217727) === 0 || sl === null || vu(
      sl,
      J,
      et,
      !1
    );
  }
  function Qc(l, t, u) {
    var a = P;
    P |= 2;
    var e = U0(), n = N0();
    (sl !== l || J !== t) && (zn = null, Ta(l, t)), t = !1;
    var f = ml;
    l: do
      try {
        if (ul !== 0 && x !== null) {
          var c = x, i = at;
          switch (ul) {
            case 8:
              Xc(), f = 6;
              break l;
            case 3:
            case 2:
            case 9:
            case 6:
              tt.current === null && (t = !0);
              var h = ul;
              if (ul = 0, at = null, Ea(l, c, i, h), u && ga) {
                f = 0;
                break l;
              }
              break;
            default:
              h = ul, ul = 0, at = null, Ea(l, c, i, h);
          }
        }
        Yd(), f = ml;
        break;
      } catch (r) {
        p0(l, r);
      }
    while (!0);
    return t && l.shellSuspendCounter++, Bt = Hu = null, P = a, b.H = e, b.A = n, x === null && (sl = null, J = 0, xe()), f;
  }
  function Yd() {
    for (; x !== null; ) R0(x);
  }
  function Gd(l, t) {
    var u = P;
    P |= 2;
    var a = U0(), e = N0();
    sl !== l || J !== t ? (zn = null, En = Fl() + 500, Ta(l, t)) : ga = Ra(
      l,
      t
    );
    l: do
      try {
        if (ul !== 0 && x !== null) {
          t = x;
          var n = at;
          t: switch (ul) {
            case 1:
              ul = 0, at = null, Ea(l, t, n, 1);
              break;
            case 2:
            case 9:
              if (Qs(n)) {
                ul = 0, at = null, H0(t);
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
              Qs(n) ? (ul = 0, at = null, H0(t)) : (ul = 0, at = null, Ea(l, t, n, 7));
              break;
            case 5:
              var f = null;
              switch (x.tag) {
                case 26:
                  f = x.memoizedState;
                case 5:
                case 27:
                  var c = x;
                  if (f ? gy(f) : c.stateNode.complete) {
                    ul = 0, at = null;
                    var i = c.sibling;
                    if (i !== null) x = i;
                    else {
                      var h = c.return;
                      h !== null ? (x = h, On(h)) : x = null;
                    }
                    break t;
                  }
              }
              ul = 0, at = null, Ea(l, t, n, 5);
              break;
            case 6:
              ul = 0, at = null, Ea(l, t, n, 6);
              break;
            case 8:
              Xc(), ml = 6;
              break l;
            default:
              throw Error(d(462));
          }
        }
        jd();
        break;
      } catch (r) {
        p0(l, r);
      }
    while (!0);
    return Bt = Hu = null, b.H = a, b.A = e, P = u, x !== null ? 0 : (sl = null, J = 0, xe(), ml);
  }
  function jd() {
    for (; x !== null && !cv(); )
      R0(x);
  }
  function R0(l) {
    var t = t0(l.alternate, l, Kt);
    l.memoizedProps = l.pendingProps, t === null ? On(l) : x = t;
  }
  function H0(l) {
    var t = l, u = t.alternate;
    switch (t.tag) {
      case 15:
      case 0:
        t = $o(
          u,
          t,
          t.pendingProps,
          t.type,
          void 0,
          J
        );
        break;
      case 11:
        t = $o(
          u,
          t,
          t.pendingProps,
          t.type.render,
          t.ref,
          J
        );
        break;
      case 5:
        tc(t);
      default:
        a0(u, t), t = x = Us(t, Kt), t = t0(u, t, Kt);
    }
    l.memoizedProps = l.pendingProps, t === null ? On(l) : x = t;
  }
  function Ea(l, t, u, a) {
    Bt = Hu = null, tc(t), oa = null, Wa = 0;
    var e = t.return;
    try {
      if (Md(
        l,
        e,
        t,
        u,
        J
      )) {
        ml = 1, vn(
          l,
          ot(u, l.current)
        ), x = null;
        return;
      }
    } catch (n) {
      if (e !== null) throw x = e, n;
      ml = 1, vn(
        l,
        ot(u, l.current)
      ), x = null;
      return;
    }
    t.flags & 32768 ? ($ || a === 1 ? l = !0 : ga || (J & 536870912) !== 0 ? l = !1 : (iu = l = !0, (a === 2 || a === 9 || a === 3 || a === 6) && (a = tt.current, a !== null && a.tag === 13 && (a.flags |= 16384))), C0(t, l)) : On(t);
  }
  function On(l) {
    var t = l;
    do {
      if ((t.flags & 32768) !== 0) {
        C0(
          t,
          iu
        );
        return;
      }
      l = t.return;
      var u = Ud(
        t.alternate,
        t,
        Kt
      );
      if (u !== null) {
        x = u;
        return;
      }
      if (t = t.sibling, t !== null) {
        x = t;
        return;
      }
      x = t = l;
    } while (t !== null);
    ml === 0 && (ml = 5);
  }
  function C0(l, t) {
    do {
      var u = Nd(l.alternate, l);
      if (u !== null) {
        u.flags &= 32767, x = u;
        return;
      }
      if (u = l.return, u !== null && (u.flags |= 32768, u.subtreeFlags = 0, u.deletions = null), !t && (l = l.sibling, l !== null)) {
        x = l;
        return;
      }
      x = l = u;
    } while (l !== null);
    ml = 6, x = null;
  }
  function q0(l, t, u, a, e, n, f, c, i) {
    l.cancelPendingCommit = null;
    do
      Mn();
    while (Al !== 0);
    if ((P & 6) !== 0) throw Error(d(327));
    if (t !== null) {
      if (t === l.current) throw Error(d(177));
      if (n = t.lanes | t.childLanes, n |= Df, gv(
        l,
        u,
        n,
        f,
        c,
        i
      ), l === sl && (x = sl = null, J = 0), ba = t, yu = l, Jt = u, Yc = n, Gc = e, A0 = a, (t.subtreeFlags & 10256) !== 0 || (t.flags & 10256) !== 0 ? (l.callbackNode = null, l.callbackPriority = 0, Ld(pe, function() {
        return X0(), null;
      })) : (l.callbackNode = null, l.callbackPriority = 0), a = (t.flags & 13878) !== 0, (t.subtreeFlags & 13878) !== 0 || a) {
        a = b.T, b.T = null, e = M.p, M.p = 2, f = P, P |= 4;
        try {
          Rd(l, t, u);
        } finally {
          P = f, M.p = e, b.T = a;
        }
      }
      Al = 1, B0(), Y0(), G0();
    }
  }
  function B0() {
    if (Al === 1) {
      Al = 0;
      var l = yu, t = ba, u = (t.flags & 13878) !== 0;
      if ((t.subtreeFlags & 13878) !== 0 || u) {
        u = b.T, b.T = null;
        var a = M.p;
        M.p = 2;
        var e = P;
        P |= 4;
        try {
          h0(t, l);
          var n = Ic, f = Ts(l.containerInfo), c = n.focusedElem, i = n.selectionRange;
          if (f !== c && c && c.ownerDocument && bs(
            c.ownerDocument.documentElement,
            c
          )) {
            if (i !== null && Af(c)) {
              var h = i.start, r = i.end;
              if (r === void 0 && (r = h), "selectionStart" in c)
                c.selectionStart = h, c.selectionEnd = Math.min(
                  r,
                  c.value.length
                );
              else {
                var E = c.ownerDocument || document, S = E && E.defaultView || window;
                if (S.getSelection) {
                  var g = S.getSelection(), D = c.textContent.length, q = Math.min(i.start, D), cl = i.end === void 0 ? q : Math.min(i.end, D);
                  !g.extend && q > cl && (f = cl, cl = q, q = f);
                  var v = rs(
                    c,
                    q
                  ), s = rs(
                    c,
                    cl
                  );
                  if (v && s && (g.rangeCount !== 1 || g.anchorNode !== v.node || g.anchorOffset !== v.offset || g.focusNode !== s.node || g.focusOffset !== s.offset)) {
                    var m = E.createRange();
                    m.setStart(v.node, v.offset), g.removeAllRanges(), q > cl ? (g.addRange(m), g.extend(s.node, s.offset)) : (m.setEnd(s.node, s.offset), g.addRange(m));
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
          jn = !!kc, Ic = kc = null;
        } finally {
          P = e, M.p = a, b.T = u;
        }
      }
      l.current = t, Al = 2;
    }
  }
  function Y0() {
    if (Al === 2) {
      Al = 0;
      var l = yu, t = ba, u = (t.flags & 8772) !== 0;
      if ((t.subtreeFlags & 8772) !== 0 || u) {
        u = b.T, b.T = null;
        var a = M.p;
        M.p = 2;
        var e = P;
        P |= 4;
        try {
          o0(l, t.alternate, t);
        } finally {
          P = e, M.p = a, b.T = u;
        }
      }
      Al = 3;
    }
  }
  function G0() {
    if (Al === 4 || Al === 3) {
      Al = 0, iv();
      var l = yu, t = ba, u = Jt, a = A0;
      (t.subtreeFlags & 10256) !== 0 || (t.flags & 10256) !== 0 ? Al = 5 : (Al = 0, ba = yu = null, j0(l, l.pendingLanes));
      var e = l.pendingLanes;
      if (e === 0 && (ou = null), af(u), t = t.stateNode, kl && typeof kl.onCommitFiberRoot == "function")
        try {
          kl.onCommitFiberRoot(
            Na,
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
      (Jt & 3) !== 0 && Mn(), pt(l), e = l.pendingLanes, (u & 261930) !== 0 && (e & 42) !== 0 ? l === jc ? ye++ : (ye = 0, jc = l) : ye = 0, ve(0);
    }
  }
  function j0(l, t) {
    (l.pooledCacheLanes &= t) === 0 && (t = l.pooledCache, t != null && (l.pooledCache = null, Ja(t)));
  }
  function Mn() {
    return B0(), Y0(), G0(), X0();
  }
  function X0() {
    if (Al !== 5) return !1;
    var l = yu, t = Yc;
    Yc = 0;
    var u = af(Jt), a = b.T, e = M.p;
    try {
      M.p = 32 > u ? 32 : u, b.T = null, u = Gc, Gc = null;
      var n = yu, f = Jt;
      if (Al = 0, ba = yu = null, Jt = 0, (P & 6) !== 0) throw Error(d(331));
      var c = P;
      if (P |= 4, T0(n.current), g0(
        n,
        n.current,
        f,
        u
      ), P = c, ve(0, !1), kl && typeof kl.onPostCommitFiberRoot == "function")
        try {
          kl.onPostCommitFiberRoot(Na, n);
        } catch {
        }
      return !0;
    } finally {
      M.p = e, b.T = a, j0(l, t);
    }
  }
  function Q0(l, t, u) {
    t = ot(u, t), t = Sc(l.stateNode, t, 2), l = eu(l, t, 2), l !== null && (Ha(l, 2), pt(l));
  }
  function al(l, t, u) {
    if (l.tag === 3)
      Q0(l, l, u);
    else
      for (; t !== null; ) {
        if (t.tag === 3) {
          Q0(
            t,
            l,
            u
          );
          break;
        } else if (t.tag === 1) {
          var a = t.stateNode;
          if (typeof t.type.getDerivedStateFromError == "function" || typeof a.componentDidCatch == "function" && (ou === null || !ou.has(a))) {
            l = ot(u, l), u = Zo(2), a = eu(t, u, 2), a !== null && (Lo(
              u,
              a,
              t,
              l
            ), Ha(a, 2), pt(a));
            break;
          }
        }
        t = t.return;
      }
  }
  function Zc(l, t, u) {
    var a = l.pingCache;
    if (a === null) {
      a = l.pingCache = new qd();
      var e = /* @__PURE__ */ new Set();
      a.set(t, e);
    } else
      e = a.get(t), e === void 0 && (e = /* @__PURE__ */ new Set(), a.set(t, e));
    e.has(u) || (Cc = !0, e.add(u), l = Xd.bind(null, l, t, u), t.then(l, l));
  }
  function Xd(l, t, u) {
    var a = l.pingCache;
    a !== null && a.delete(t), l.pingedLanes |= l.suspendedLanes & u, l.warmLanes &= ~u, sl === l && (J & u) === u && (ml === 4 || ml === 3 && (J & 62914560) === J && 300 > Fl() - Tn ? (P & 2) === 0 && Ta(l, 0) : qc |= u, ra === J && (ra = 0)), pt(l);
  }
  function Z0(l, t) {
    t === 0 && (t = qi()), l = Uu(l, t), l !== null && (Ha(l, t), pt(l));
  }
  function Qd(l) {
    var t = l.memoizedState, u = 0;
    t !== null && (u = t.retryLane), Z0(l, u);
  }
  function Zd(l, t) {
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
        throw Error(d(314));
    }
    a !== null && a.delete(t), Z0(l, u);
  }
  function Ld(l, t) {
    return Pn(l, t);
  }
  var pn = null, za = null, Lc = !1, Dn = !1, xc = !1, du = 0;
  function pt(l) {
    l !== za && l.next === null && (za === null ? pn = za = l : za = za.next = l), Dn = !0, Lc || (Lc = !0, Vd());
  }
  function ve(l, t) {
    if (!xc && Dn) {
      xc = !0;
      do
        for (var u = !1, a = pn; a !== null; ) {
          if (l !== 0) {
            var e = a.pendingLanes;
            if (e === 0) var n = 0;
            else {
              var f = a.suspendedLanes, c = a.pingedLanes;
              n = (1 << 31 - Il(42 | l) + 1) - 1, n &= e & ~(f & ~c), n = n & 201326741 ? n & 201326741 | 1 : n ? n | 2 : 0;
            }
            n !== 0 && (u = !0, K0(a, n));
          } else
            n = J, n = Re(
              a,
              a === sl ? n : 0,
              a.cancelPendingCommit !== null || a.timeoutHandle !== -1
            ), (n & 3) === 0 || Ra(a, n) || (u = !0, K0(a, n));
          a = a.next;
        }
      while (u);
      xc = !1;
    }
  }
  function xd() {
    L0();
  }
  function L0() {
    Dn = Lc = !1;
    var l = 0;
    du !== 0 && lm() && (l = du);
    for (var t = Fl(), u = null, a = pn; a !== null; ) {
      var e = a.next, n = x0(a, t);
      n === 0 ? (a.next = null, u === null ? pn = e : u.next = e, e === null && (za = u)) : (u = a, (l !== 0 || (n & 3) !== 0) && (Dn = !0)), a = e;
    }
    Al !== 0 && Al !== 5 || ve(l), du !== 0 && (du = 0);
  }
  function x0(l, t) {
    for (var u = l.suspendedLanes, a = l.pingedLanes, e = l.expirationTimes, n = l.pendingLanes & -62914561; 0 < n; ) {
      var f = 31 - Il(n), c = 1 << f, i = e[f];
      i === -1 ? ((c & u) === 0 || (c & a) !== 0) && (e[f] = Sv(c, t)) : i <= t && (l.expiredLanes |= c), n &= ~c;
    }
    if (t = sl, u = J, u = Re(
      l,
      l === t ? u : 0,
      l.cancelPendingCommit !== null || l.timeoutHandle !== -1
    ), a = l.callbackNode, u === 0 || l === t && (ul === 2 || ul === 9) || l.cancelPendingCommit !== null)
      return a !== null && a !== null && lf(a), l.callbackNode = null, l.callbackPriority = 0;
    if ((u & 3) === 0 || Ra(l, u)) {
      if (t = u & -u, t === l.callbackPriority) return t;
      switch (a !== null && lf(a), af(u)) {
        case 2:
        case 8:
          u = Hi;
          break;
        case 32:
          u = pe;
          break;
        case 268435456:
          u = Ci;
          break;
        default:
          u = pe;
      }
      return a = V0.bind(null, l), u = Pn(u, a), l.callbackPriority = t, l.callbackNode = u, t;
    }
    return a !== null && a !== null && lf(a), l.callbackPriority = 2, l.callbackNode = null, 2;
  }
  function V0(l, t) {
    if (Al !== 0 && Al !== 5)
      return l.callbackNode = null, l.callbackPriority = 0, null;
    var u = l.callbackNode;
    if (Mn() && l.callbackNode !== u)
      return null;
    var a = J;
    return a = Re(
      l,
      l === sl ? a : 0,
      l.cancelPendingCommit !== null || l.timeoutHandle !== -1
    ), a === 0 ? null : (O0(l, a, t), x0(l, Fl()), l.callbackNode != null && l.callbackNode === u ? V0.bind(null, l) : null);
  }
  function K0(l, t) {
    if (Mn()) return null;
    O0(l, t, !0);
  }
  function Vd() {
    um(function() {
      (P & 6) !== 0 ? Pn(
        Ri,
        xd
      ) : L0();
    });
  }
  function Vc() {
    if (du === 0) {
      var l = ca;
      l === 0 && (l = De, De <<= 1, (De & 261888) === 0 && (De = 256)), du = l;
    }
    return du;
  }
  function J0(l) {
    return l == null || typeof l == "symbol" || typeof l == "boolean" ? null : typeof l == "function" ? l : Be("" + l);
  }
  function w0(l, t) {
    var u = t.ownerDocument.createElement("input");
    return u.name = t.name, u.value = t.value, l.id && u.setAttribute("form", l.id), t.parentNode.insertBefore(u, t), l = new FormData(l), u.parentNode.removeChild(u), l;
  }
  function Kd(l, t, u, a, e) {
    if (t === "submit" && u && u.stateNode === e) {
      var n = J0(
        (e[Ql] || null).action
      ), f = a.submitter;
      f && (t = (t = f[Ql] || null) ? J0(t.formAction) : f.getAttribute("formAction"), t !== null && (n = t, f = null));
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
                if (du !== 0) {
                  var i = f ? w0(e, f) : new FormData(e);
                  oc(
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
                typeof n == "function" && (c.preventDefault(), i = f ? w0(e, f) : new FormData(e), oc(
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
  for (var Kc = 0; Kc < pf.length; Kc++) {
    var Jc = pf[Kc], Jd = Jc.toLowerCase(), wd = Jc[0].toUpperCase() + Jc.slice(1);
    rt(
      Jd,
      "on" + wd
    );
  }
  rt(As, "onAnimationEnd"), rt(_s, "onAnimationIteration"), rt(Os, "onAnimationStart"), rt("dblclick", "onDoubleClick"), rt("focusin", "onFocus"), rt("focusout", "onBlur"), rt(sd, "onTransitionRun"), rt(od, "onTransitionStart"), rt(yd, "onTransitionCancel"), rt(Ms, "onTransitionEnd"), wu("onMouseEnter", ["mouseout", "mouseover"]), wu("onMouseLeave", ["mouseout", "mouseover"]), wu("onPointerEnter", ["pointerout", "pointerover"]), wu("onPointerLeave", ["pointerout", "pointerover"]), Ou(
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
  var de = "abort canplay canplaythrough durationchange emptied encrypted ended error loadeddata loadedmetadata loadstart pause play playing progress ratechange resize seeked seeking stalled suspend timeupdate volumechange waiting".split(
    " "
  ), Wd = new Set(
    "beforetoggle cancel close invalid load scroll scrollend toggle".split(" ").concat(de)
  );
  function W0(l, t) {
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
  function V(l, t) {
    var u = t[ef];
    u === void 0 && (u = t[ef] = /* @__PURE__ */ new Set());
    var a = l + "__bubble";
    u.has(a) || ($0(t, l, 2, !1), u.add(a));
  }
  function wc(l, t, u) {
    var a = 0;
    t && (a |= 4), $0(
      u,
      l,
      a,
      t
    );
  }
  var Un = "_reactListening" + Math.random().toString(36).slice(2);
  function Wc(l) {
    if (!l[Un]) {
      l[Un] = !0, Zi.forEach(function(u) {
        u !== "selectionchange" && (Wd.has(u) || wc(u, !1, l), wc(u, !0, l));
      });
      var t = l.nodeType === 9 ? l : l.ownerDocument;
      t === null || t[Un] || (t[Un] = !0, wc("selectionchange", !1, t));
    }
  }
  function $0(l, t, u, a) {
    switch (_y(t)) {
      case 2:
        var e = zm;
        break;
      case 8:
        e = Am;
        break;
      default:
        e = si;
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
  function $c(l, t, u, a, e) {
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
    Pi(function() {
      var h = n, r = vf(u), E = [];
      l: {
        var S = ps.get(l);
        if (S !== void 0) {
          var g = Xe, D = l;
          switch (l) {
            case "keypress":
              if (Ge(u) === 0) break l;
            case "keydown":
            case "keyup":
              g = Zv;
              break;
            case "focusin":
              D = "focus", g = rf;
              break;
            case "focusout":
              D = "blur", g = rf;
              break;
            case "beforeblur":
            case "afterblur":
              g = rf;
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
              g = us;
              break;
            case "drag":
            case "dragend":
            case "dragenter":
            case "dragexit":
            case "dragleave":
            case "dragover":
            case "dragstart":
            case "drop":
              g = Uv;
              break;
            case "touchcancel":
            case "touchend":
            case "touchmove":
            case "touchstart":
              g = Vv;
              break;
            case As:
            case _s:
            case Os:
              g = Hv;
              break;
            case Ms:
              g = Jv;
              break;
            case "scroll":
            case "scrollend":
              g = pv;
              break;
            case "wheel":
              g = Wv;
              break;
            case "copy":
            case "cut":
            case "paste":
              g = qv;
              break;
            case "gotpointercapture":
            case "lostpointercapture":
            case "pointercancel":
            case "pointerdown":
            case "pointermove":
            case "pointerout":
            case "pointerover":
            case "pointerup":
              g = es;
              break;
            case "toggle":
            case "beforetoggle":
              g = Fv;
          }
          var q = (t & 4) !== 0, cl = !q && (l === "scroll" || l === "scrollend"), v = q ? S !== null ? S + "Capture" : null : S;
          q = [];
          for (var s = h, m; s !== null; ) {
            var T = s;
            if (m = T.stateNode, T = T.tag, T !== 5 && T !== 26 && T !== 27 || m === null || v === null || (T = Ba(s, v), T != null && q.push(
              me(s, T, m)
            )), cl) break;
            s = s.return;
          }
          0 < q.length && (S = new g(
            S,
            D,
            null,
            u,
            r
          ), E.push({ event: S, listeners: q }));
        }
      }
      if ((t & 7) === 0) {
        l: {
          if (S = l === "mouseover" || l === "pointerover", g = l === "mouseout" || l === "pointerout", S && u !== yf && (D = u.relatedTarget || u.fromElement) && (Vu(D) || D[xu]))
            break l;
          if ((g || S) && (S = r.window === r ? r : (S = r.ownerDocument) ? S.defaultView || S.parentWindow : window, g ? (D = u.relatedTarget || u.toElement, g = h, D = D ? Vu(D) : null, D !== null && (cl = Z(D), q = D.tag, D !== cl || q !== 5 && q !== 27 && q !== 6) && (D = null)) : (g = null, D = h), g !== D)) {
            if (q = us, T = "onMouseLeave", v = "onMouseEnter", s = "mouse", (l === "pointerout" || l === "pointerover") && (q = es, T = "onPointerLeave", v = "onPointerEnter", s = "pointer"), cl = g == null ? S : qa(g), m = D == null ? S : qa(D), S = new q(
              T,
              s + "leave",
              g,
              u,
              r
            ), S.target = cl, S.relatedTarget = m, T = null, Vu(r) === h && (q = new q(
              v,
              s + "enter",
              D,
              u,
              r
            ), q.target = m, q.relatedTarget = cl, T = q), cl = T, g && D)
              t: {
                for (q = $d, v = g, s = D, m = 0, T = v; T; T = q(T))
                  m++;
                T = 0;
                for (var C = s; C; C = q(C))
                  T++;
                for (; 0 < m - T; )
                  v = q(v), m--;
                for (; 0 < T - m; )
                  s = q(s), T--;
                for (; m--; ) {
                  if (v === s || s !== null && v === s.alternate) {
                    q = v;
                    break t;
                  }
                  v = q(v), s = q(s);
                }
                q = null;
              }
            else q = null;
            g !== null && F0(
              E,
              S,
              g,
              q,
              !1
            ), D !== null && cl !== null && F0(
              E,
              cl,
              D,
              q,
              !0
            );
          }
        }
        l: {
          if (S = h ? qa(h) : window, g = S.nodeName && S.nodeName.toLowerCase(), g === "select" || g === "input" && S.type === "file")
            var k = vs;
          else if (os(S))
            if (ds)
              k = fd;
            else {
              k = ed;
              var R = ad;
            }
          else
            g = S.nodeName, !g || g.toLowerCase() !== "input" || S.type !== "checkbox" && S.type !== "radio" ? h && of(h.elementType) && (k = vs) : k = nd;
          if (k && (k = k(l, h))) {
            ys(
              E,
              k,
              u,
              r
            );
            break l;
          }
          R && R(l, S, h), l === "focusout" && h && S.type === "number" && h.memoizedProps.value != null && sf(S, "number", S.value);
        }
        switch (R = h ? qa(h) : window, l) {
          case "focusin":
            (os(R) || R.contentEditable === "true") && (Pu = R, _f = h, xa = null);
            break;
          case "focusout":
            xa = _f = Pu = null;
            break;
          case "mousedown":
            Of = !0;
            break;
          case "contextmenu":
          case "mouseup":
          case "dragend":
            Of = !1, Es(E, u, r);
            break;
          case "selectionchange":
            if (id) break;
          case "keydown":
          case "keyup":
            Es(E, u, r);
        }
        var Q;
        if (Tf)
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
          Iu ? is(l, u) && (w = "onCompositionEnd") : l === "keydown" && u.keyCode === 229 && (w = "onCompositionStart");
        w && (ns && u.locale !== "ko" && (Iu || w !== "onCompositionStart" ? w === "onCompositionEnd" && Iu && (Q = ls()) : (kt = r, hf = "value" in kt ? kt.value : kt.textContent, Iu = !0)), R = Nn(h, w), 0 < R.length && (w = new as(
          w,
          l,
          null,
          u,
          r
        ), E.push({ event: w, listeners: R }), Q ? w.data = Q : (Q = ss(u), Q !== null && (w.data = Q)))), (Q = Iv ? Pv(l, u) : ld(l, u)) && (w = Nn(h, "onBeforeInput"), 0 < w.length && (R = new as(
          "onBeforeInput",
          "beforeinput",
          null,
          u,
          r
        ), E.push({
          event: R,
          listeners: w
        }), R.data = Q)), Kd(
          E,
          l,
          h,
          u,
          r
        );
      }
      W0(E, t);
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
      if (e = e.tag, e !== 5 && e !== 26 && e !== 27 || n === null || (e = Ba(l, u), e != null && a.unshift(
        me(l, e, n)
      ), e = Ba(l, t), e != null && a.push(
        me(l, e, n)
      )), l.tag === 3) return a;
      l = l.return;
    }
    return [];
  }
  function $d(l) {
    if (l === null) return null;
    do
      l = l.return;
    while (l && l.tag !== 5 && l.tag !== 27);
    return l || null;
  }
  function F0(l, t, u, a, e) {
    for (var n = t._reactName, f = []; u !== null && u !== a; ) {
      var c = u, i = c.alternate, h = c.stateNode;
      if (c = c.tag, i !== null && i === a) break;
      c !== 5 && c !== 26 && c !== 27 || h === null || (i = h, e ? (h = Ba(u, n), h != null && f.unshift(
        me(u, h, i)
      )) : e || (h = Ba(u, n), h != null && f.push(
        me(u, h, i)
      ))), u = u.return;
    }
    f.length !== 0 && l.push({ event: t, listeners: f });
  }
  var Fd = /\r\n?/g, kd = /\u0000|\uFFFD/g;
  function k0(l) {
    return (typeof l == "string" ? l : "" + l).replace(Fd, `
`).replace(kd, "");
  }
  function I0(l, t) {
    return t = k0(t), k0(l) === t;
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
        ki(l, a, n);
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
        a != null && V("scroll", l);
        break;
      case "onScrollEnd":
        a != null && V("scrollend", l);
        break;
      case "dangerouslySetInnerHTML":
        if (a != null) {
          if (typeof a != "object" || !("__html" in a))
            throw Error(d(61));
          if (u = a.__html, u != null) {
            if (e.children != null) throw Error(d(60));
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
        V("beforetoggle", l), V("toggle", l), He(l, "popover", a);
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
        (!(2 < u.length) || u[0] !== "o" && u[0] !== "O" || u[1] !== "n" && u[1] !== "N") && (u = Ov.get(u) || u, He(l, u, a));
    }
  }
  function Fc(l, t, u, a, e, n) {
    switch (u) {
      case "style":
        ki(l, a, n);
        break;
      case "dangerouslySetInnerHTML":
        if (a != null) {
          if (typeof a != "object" || !("__html" in a))
            throw Error(d(61));
          if (u = a.__html, u != null) {
            if (e.children != null) throw Error(d(60));
            l.innerHTML = u;
          }
        }
        break;
      case "children":
        typeof a == "string" ? $u(l, a) : (typeof a == "number" || typeof a == "bigint") && $u(l, "" + a);
        break;
      case "onScroll":
        a != null && V("scroll", l);
        break;
      case "onScrollEnd":
        a != null && V("scrollend", l);
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
        if (!Li.hasOwnProperty(u))
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
        V("error", l), V("load", l);
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
                  throw Error(d(137, t));
                default:
                  fl(l, t, n, f, u, null);
              }
          }
        e && fl(l, t, "srcSet", u.srcSet, u, null), a && fl(l, t, "src", u.src, u, null);
        return;
      case "input":
        V("invalid", l);
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
                    throw Error(d(137, t));
                  break;
                default:
                  fl(l, t, a, r, u, null);
              }
          }
        wi(
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
        V("invalid", l), a = f = n = null;
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
        V("invalid", l), n = e = a = null;
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
                if (c != null) throw Error(d(91));
                break;
              default:
                fl(l, t, f, c, u, null);
            }
        $i(l, a, e, n);
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
        V("beforetoggle", l), V("toggle", l), V("cancel", l), V("close", l);
        break;
      case "iframe":
      case "object":
        V("load", l);
        break;
      case "video":
      case "audio":
        for (a = 0; a < de.length; a++)
          V(de[a], l);
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
        for (h in u)
          if (u.hasOwnProperty(h) && (a = u[h], a != null))
            switch (h) {
              case "children":
              case "dangerouslySetInnerHTML":
                throw Error(d(137, t));
              default:
                fl(l, t, h, a, u, null);
            }
        return;
      default:
        if (of(t)) {
          for (r in u)
            u.hasOwnProperty(r) && (a = u[r], a !== void 0 && Fc(
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
  function Id(l, t, u, a) {
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
                  throw Error(d(137, t));
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
        cf(
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
                if (e != null) throw Error(d(91));
                break;
              default:
                e !== n && fl(l, t, f, e, a, n);
            }
        Wi(l, S, g);
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
        for (var q in u)
          S = u[q], u.hasOwnProperty(q) && S != null && !a.hasOwnProperty(q) && fl(l, t, q, null, a, S);
        for (h in a)
          if (S = a[h], g = u[h], a.hasOwnProperty(h) && S !== g && (S != null || g != null))
            switch (h) {
              case "children":
              case "dangerouslySetInnerHTML":
                if (S != null)
                  throw Error(d(137, t));
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
        if (of(t)) {
          for (var cl in u)
            S = u[cl], u.hasOwnProperty(cl) && S !== void 0 && !a.hasOwnProperty(cl) && Fc(
              l,
              t,
              cl,
              void 0,
              a,
              S
            );
          for (r in a)
            S = a[r], g = u[r], !a.hasOwnProperty(r) || S === g || S === void 0 && g === void 0 || Fc(
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
    for (var v in u)
      S = u[v], u.hasOwnProperty(v) && S != null && !a.hasOwnProperty(v) && fl(l, t, v, null, a, S);
    for (E in a)
      S = a[E], g = u[E], !a.hasOwnProperty(E) || S === g || S == null && g == null || fl(l, t, E, S, a, g);
  }
  function P0(l) {
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
  function Pd() {
    if (typeof performance.getEntriesByType == "function") {
      for (var l = 0, t = 0, u = performance.getEntriesByType("resource"), a = 0; a < u.length; a++) {
        var e = u[a], n = e.transferSize, f = e.initiatorType, c = e.duration;
        if (n && c && P0(f)) {
          for (f = 0, c = e.responseEnd, a += 1; a < u.length; a++) {
            var i = u[a], h = i.startTime;
            if (h > c) break;
            var r = i.transferSize, E = i.initiatorType;
            r && P0(E) && (i = i.responseEnd, f += r * (i < c ? 1 : (c - h) / (i - h)));
          }
          if (--a, t += 8 * (n + f) / (e.duration / 1e3), l++, 10 < l) break;
        }
      }
      if (0 < l) return t / l / 1e6;
    }
    return navigator.connection && (l = navigator.connection.downlink, typeof l == "number") ? l : 5;
  }
  var kc = null, Ic = null;
  function Rn(l) {
    return l.nodeType === 9 ? l : l.ownerDocument;
  }
  function ly(l) {
    switch (l) {
      case "http://www.w3.org/2000/svg":
        return 1;
      case "http://www.w3.org/1998/Math/MathML":
        return 2;
      default:
        return 0;
    }
  }
  function ty(l, t) {
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
  function Pc(l, t) {
    return l === "textarea" || l === "noscript" || typeof t.children == "string" || typeof t.children == "number" || typeof t.children == "bigint" || typeof t.dangerouslySetInnerHTML == "object" && t.dangerouslySetInnerHTML !== null && t.dangerouslySetInnerHTML.__html != null;
  }
  var li = null;
  function lm() {
    var l = window.event;
    return l && l.type === "popstate" ? l === li ? !1 : (li = l, !0) : (li = null, !1);
  }
  var uy = typeof setTimeout == "function" ? setTimeout : void 0, tm = typeof clearTimeout == "function" ? clearTimeout : void 0, ay = typeof Promise == "function" ? Promise : void 0, um = typeof queueMicrotask == "function" ? queueMicrotask : typeof ay < "u" ? function(l) {
    return ay.resolve(null).then(l).catch(am);
  } : uy;
  function am(l) {
    setTimeout(function() {
      throw l;
    });
  }
  function mu(l) {
    return l === "head";
  }
  function ey(l, t) {
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
          he(l.ownerDocument.documentElement);
        else if (u === "head") {
          u = l.ownerDocument.head, he(u);
          for (var n = u.firstChild; n; ) {
            var f = n.nextSibling, c = n.nodeName;
            n[Ca] || c === "SCRIPT" || c === "STYLE" || c === "LINK" && n.rel.toLowerCase() === "stylesheet" || u.removeChild(n), n = f;
          }
        } else
          u === "body" && he(l.ownerDocument.body);
      u = e;
    } while (u);
    Ma(t);
  }
  function ny(l, t) {
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
  function ti(l) {
    var t = l.firstChild;
    for (t && t.nodeType === 10 && (t = t.nextSibling); t; ) {
      var u = t;
      switch (t = t.nextSibling, u.nodeName) {
        case "HTML":
        case "HEAD":
        case "BODY":
          ti(u), nf(u);
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
  function em(l, t, u, a) {
    for (; l.nodeType === 1; ) {
      var e = u;
      if (l.nodeName.toLowerCase() !== t.toLowerCase()) {
        if (!a && (l.nodeName !== "INPUT" || l.type !== "hidden"))
          break;
      } else if (a) {
        if (!l[Ca])
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
  function nm(l, t, u) {
    if (t === "") return null;
    for (; l.nodeType !== 3; )
      if ((l.nodeType !== 1 || l.nodeName !== "INPUT" || l.type !== "hidden") && !u || (l = ht(l.nextSibling), l === null)) return null;
    return l;
  }
  function fy(l, t) {
    for (; l.nodeType !== 8; )
      if ((l.nodeType !== 1 || l.nodeName !== "INPUT" || l.type !== "hidden") && !t || (l = ht(l.nextSibling), l === null)) return null;
    return l;
  }
  function ui(l) {
    return l.data === "$?" || l.data === "$~";
  }
  function ai(l) {
    return l.data === "$!" || l.data === "$?" && l.ownerDocument.readyState !== "loading";
  }
  function fm(l, t) {
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
  var ei = null;
  function cy(l) {
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
  function iy(l) {
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
  function sy(l, t, u) {
    switch (t = Rn(u), l) {
      case "html":
        if (l = t.documentElement, !l) throw Error(d(452));
        return l;
      case "head":
        if (l = t.head, !l) throw Error(d(453));
        return l;
      case "body":
        if (l = t.body, !l) throw Error(d(454));
        return l;
      default:
        throw Error(d(451));
    }
  }
  function he(l) {
    for (var t = l.attributes; t.length; )
      l.removeAttributeNode(t[0]);
    nf(l);
  }
  var St = /* @__PURE__ */ new Map(), oy = /* @__PURE__ */ new Set();
  function Hn(l) {
    return typeof l.getRootNode == "function" ? l.getRootNode() : l.nodeType === 9 ? l : l.ownerDocument;
  }
  var wt = M.d;
  M.d = {
    f: cm,
    r: im,
    D: sm,
    C: om,
    L: ym,
    m: vm,
    X: mm,
    S: dm,
    M: hm
  };
  function cm() {
    var l = wt.f(), t = An();
    return l || t;
  }
  function im(l) {
    var t = Ku(l);
    t !== null && t.tag === 5 && t.type === "form" ? po(t) : wt.r(l);
  }
  var Aa = typeof document > "u" ? null : document;
  function yy(l, t, u) {
    var a = Aa;
    if (a && typeof t == "string" && t) {
      var e = it(t);
      e = 'link[rel="' + l + '"][href="' + e + '"]', typeof u == "string" && (e += '[crossorigin="' + u + '"]'), oy.has(e) || (oy.add(e), l = { rel: l, crossOrigin: u, href: t }, a.querySelector(e) === null && (t = a.createElement("link"), Nl(t, "link", l), _l(t), a.head.appendChild(t)));
    }
  }
  function sm(l) {
    wt.D(l), yy("dns-prefetch", l, null);
  }
  function om(l, t) {
    wt.C(l, t), yy("preconnect", l, t);
  }
  function ym(l, t, u) {
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
      ), St.set(n, l), a.querySelector(e) !== null || t === "style" && a.querySelector(Se(n)) || t === "script" && a.querySelector(ge(n)) || (t = a.createElement("link"), Nl(t, "link", l), _l(t), a.head.appendChild(t)));
    }
  }
  function vm(l, t) {
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
            if (u.querySelector(ge(n)))
              return;
        }
        a = u.createElement("link"), Nl(a, "link", l), _l(a), u.head.appendChild(a);
      }
    }
  }
  function dm(l, t, u) {
    wt.S(l, t, u);
    var a = Aa;
    if (a && l) {
      var e = Ju(a).hoistableStyles, n = _a(l);
      t = t || "default";
      var f = e.get(n);
      if (!f) {
        var c = { loading: 0, preload: null };
        if (f = a.querySelector(
          Se(n)
        ))
          c.loading = 5;
        else {
          l = B(
            { rel: "stylesheet", href: l, "data-precedence": t },
            u
          ), (u = St.get(n)) && ni(l, u);
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
  function mm(l, t) {
    wt.X(l, t);
    var u = Aa;
    if (u && l) {
      var a = Ju(u).hoistableScripts, e = Oa(l), n = a.get(e);
      n || (n = u.querySelector(ge(e)), n || (l = B({ src: l, async: !0 }, t), (t = St.get(e)) && fi(l, t), n = u.createElement("script"), _l(n), Nl(n, "link", l), u.head.appendChild(n)), n = {
        type: "script",
        instance: n,
        count: 1,
        state: null
      }, a.set(e, n));
    }
  }
  function hm(l, t) {
    wt.M(l, t);
    var u = Aa;
    if (u && l) {
      var a = Ju(u).hoistableScripts, e = Oa(l), n = a.get(e);
      n || (n = u.querySelector(ge(e)), n || (l = B({ src: l, async: !0, type: "module" }, t), (t = St.get(e)) && fi(l, t), n = u.createElement("script"), _l(n), Nl(n, "link", l), u.head.appendChild(n)), n = {
        type: "script",
        instance: n,
        count: 1,
        state: null
      }, a.set(e, n));
    }
  }
  function vy(l, t, u, a) {
    var e = (e = L.current) ? Hn(e) : null;
    if (!e) throw Error(d(446));
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
            Se(l)
          )) && !n._p && (f.instance = n, f.state.loading = 5), St.has(l) || (u = {
            rel: "preload",
            as: "style",
            href: u.href,
            crossOrigin: u.crossOrigin,
            integrity: u.integrity,
            media: u.media,
            hrefLang: u.hrefLang,
            referrerPolicy: u.referrerPolicy
          }, St.set(l, u), n || Sm(
            e,
            l,
            u,
            f.state
          ))), t && a === null)
            throw Error(d(528, ""));
          return f;
        }
        if (t && a !== null)
          throw Error(d(529, ""));
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
        throw Error(d(444, l));
    }
  }
  function _a(l) {
    return 'href="' + it(l) + '"';
  }
  function Se(l) {
    return 'link[rel="stylesheet"][' + l + "]";
  }
  function dy(l) {
    return B({}, l, {
      "data-precedence": l.precedence,
      precedence: null
    });
  }
  function Sm(l, t, u, a) {
    l.querySelector('link[rel="preload"][as="style"][' + t + "]") ? a.loading = 1 : (t = l.createElement("link"), a.preload = t, t.addEventListener("load", function() {
      return a.loading |= 1;
    }), t.addEventListener("error", function() {
      return a.loading |= 2;
    }), Nl(t, "link", u), _l(t), l.head.appendChild(t));
  }
  function Oa(l) {
    return '[src="' + it(l) + '"]';
  }
  function ge(l) {
    return "script[async]" + l;
  }
  function my(l, t, u) {
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
            Se(e)
          );
          if (n)
            return t.state.loading |= 4, t.instance = n, _l(n), n;
          a = dy(u), (e = St.get(e)) && ni(a, e), n = (l.ownerDocument || l).createElement("link"), _l(n);
          var f = n;
          return f._p = new Promise(function(c, i) {
            f.onload = c, f.onerror = i;
          }), Nl(n, "link", a), t.state.loading |= 4, Cn(n, u.precedence, l), t.instance = n;
        case "script":
          return n = Oa(u.src), (e = l.querySelector(
            ge(n)
          )) ? (t.instance = e, _l(e), e) : (a = u, (e = St.get(n)) && (a = B({}, u), fi(a, e)), l = l.ownerDocument || l, e = l.createElement("script"), _l(e), Nl(e, "link", a), l.head.appendChild(e), t.instance = e);
        case "void":
          return null;
        default:
          throw Error(d(443, t.type));
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
  function ni(l, t) {
    l.crossOrigin == null && (l.crossOrigin = t.crossOrigin), l.referrerPolicy == null && (l.referrerPolicy = t.referrerPolicy), l.title == null && (l.title = t.title);
  }
  function fi(l, t) {
    l.crossOrigin == null && (l.crossOrigin = t.crossOrigin), l.referrerPolicy == null && (l.referrerPolicy = t.referrerPolicy), l.integrity == null && (l.integrity = t.integrity);
  }
  var qn = null;
  function hy(l, t, u) {
    if (qn === null) {
      var a = /* @__PURE__ */ new Map(), e = qn = /* @__PURE__ */ new Map();
      e.set(u, a);
    } else
      e = qn, a = e.get(u), a || (a = /* @__PURE__ */ new Map(), e.set(u, a));
    if (a.has(l)) return a;
    for (a.set(l, null), u = u.getElementsByTagName(l), e = 0; e < u.length; e++) {
      var n = u[e];
      if (!(n[Ca] || n[Ml] || l === "link" && n.getAttribute("rel") === "stylesheet") && n.namespaceURI !== "http://www.w3.org/2000/svg") {
        var f = n.getAttribute(t) || "";
        f = l + f;
        var c = a.get(f);
        c ? c.push(n) : a.set(f, [n]);
      }
    }
    return a;
  }
  function Sy(l, t, u) {
    l = l.ownerDocument || l, l.head.insertBefore(
      u,
      t === "title" ? l.querySelector("head > title") : null
    );
  }
  function gm(l, t, u) {
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
  function gy(l) {
    return !(l.type === "stylesheet" && (l.state.loading & 3) === 0);
  }
  function rm(l, t, u, a) {
    if (u.type === "stylesheet" && (typeof a.media != "string" || matchMedia(a.media).matches !== !1) && (u.state.loading & 4) === 0) {
      if (u.instance === null) {
        var e = _a(a.href), n = t.querySelector(
          Se(e)
        );
        if (n) {
          t = n._p, t !== null && typeof t == "object" && typeof t.then == "function" && (l.count++, l = Bn.bind(l), t.then(l, l)), u.state.loading |= 4, u.instance = n, _l(n);
          return;
        }
        n = t.ownerDocument || t, a = dy(a), (e = St.get(e)) && ni(a, e), n = n.createElement("link"), _l(n);
        var f = n;
        f._p = new Promise(function(c, i) {
          f.onload = c, f.onerror = i;
        }), Nl(n, "link", a), u.instance = n;
      }
      l.stylesheets === null && (l.stylesheets = /* @__PURE__ */ new Map()), l.stylesheets.set(u, t), (t = u.state.preload) && (u.state.loading & 3) === 0 && (l.count++, u = Bn.bind(l), t.addEventListener("load", u), t.addEventListener("error", u));
    }
  }
  var ci = 0;
  function bm(l, t) {
    return l.stylesheets && l.count === 0 && Gn(l, l.stylesheets), 0 < l.count || 0 < l.imgCount ? function(u) {
      var a = setTimeout(function() {
        if (l.stylesheets && Gn(l, l.stylesheets), l.unsuspend) {
          var n = l.unsuspend;
          l.unsuspend = null, n();
        }
      }, 6e4 + t);
      0 < l.imgBytes && ci === 0 && (ci = 62500 * Pd());
      var e = setTimeout(
        function() {
          if (l.waitingForImages = !1, l.count === 0 && (l.stylesheets && Gn(l, l.stylesheets), l.unsuspend)) {
            var n = l.unsuspend;
            l.unsuspend = null, n();
          }
        },
        (l.imgBytes > ci ? 50 : 800) + t
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
    l.stylesheets = null, l.unsuspend !== null && (l.count++, Yn = /* @__PURE__ */ new Map(), t.forEach(Tm, l), Yn = null, Bn.call(l));
  }
  function Tm(l, t) {
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
  var re = {
    $$typeof: Rl,
    Provider: null,
    Consumer: null,
    _currentValue: Y,
    _currentValue2: Y,
    _threadCount: 0
  };
  function Em(l, t, u, a, e, n, f, c, i) {
    this.tag = 1, this.containerInfo = l, this.pingCache = this.current = this.pendingChildren = null, this.timeoutHandle = -1, this.callbackNode = this.next = this.pendingContext = this.context = this.cancelPendingCommit = null, this.callbackPriority = 0, this.expirationTimes = tf(-1), this.entangledLanes = this.shellSuspendCounter = this.errorRecoveryDisabledLanes = this.expiredLanes = this.warmLanes = this.pingedLanes = this.suspendedLanes = this.pendingLanes = 0, this.entanglements = tf(0), this.hiddenUpdates = tf(null), this.identifierPrefix = a, this.onUncaughtError = e, this.onCaughtError = n, this.onRecoverableError = f, this.pooledCache = null, this.pooledCacheLanes = 0, this.formState = i, this.incompleteTransitions = /* @__PURE__ */ new Map();
  }
  function ry(l, t, u, a, e, n, f, c, i, h, r, E) {
    return l = new Em(
      l,
      t,
      u,
      f,
      i,
      h,
      r,
      E,
      c
    ), t = 1, n === !0 && (t |= 24), n = lt(3, null, null, t), l.current = n, n.stateNode = l, t = Qf(), t.refCount++, l.pooledCache = t, t.refCount++, n.memoizedState = {
      element: a,
      isDehydrated: u,
      cache: t
    }, Vf(n), l;
  }
  function by(l) {
    return l ? (l = ua, l) : ua;
  }
  function Ty(l, t, u, a, e, n) {
    e = by(e), a.context === null ? a.context = e : a.pendingContext = e, a = au(t), a.payload = { element: u }, n = n === void 0 ? null : n, n !== null && (a.callback = n), u = eu(l, a, t), u !== null && (Jl(u, l, t), Fa(u, l, t));
  }
  function Ey(l, t) {
    if (l = l.memoizedState, l !== null && l.dehydrated !== null) {
      var u = l.retryLane;
      l.retryLane = u !== 0 && u < t ? u : t;
    }
  }
  function ii(l, t) {
    Ey(l, t), (l = l.alternate) && Ey(l, t);
  }
  function zy(l) {
    if (l.tag === 13 || l.tag === 31) {
      var t = Uu(l, 67108864);
      t !== null && Jl(t, l, 67108864), ii(l, 67108864);
    }
  }
  function Ay(l) {
    if (l.tag === 13 || l.tag === 31) {
      var t = nt();
      t = uf(t);
      var u = Uu(l, t);
      u !== null && Jl(u, l, t), ii(l, t);
    }
  }
  var jn = !0;
  function zm(l, t, u, a) {
    var e = b.T;
    b.T = null;
    var n = M.p;
    try {
      M.p = 2, si(l, t, u, a);
    } finally {
      M.p = n, b.T = e;
    }
  }
  function Am(l, t, u, a) {
    var e = b.T;
    b.T = null;
    var n = M.p;
    try {
      M.p = 8, si(l, t, u, a);
    } finally {
      M.p = n, b.T = e;
    }
  }
  function si(l, t, u, a) {
    if (jn) {
      var e = oi(a);
      if (e === null)
        $c(
          l,
          t,
          a,
          Xn,
          u
        ), Oy(l, a);
      else if (Om(
        e,
        l,
        t,
        u,
        a
      ))
        a.stopPropagation();
      else if (Oy(l, a), t & 4 && -1 < _m.indexOf(l)) {
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
                      var i = 1 << 31 - Il(f);
                      c.entanglements[1] |= i, f &= ~i;
                    }
                    pt(n), (P & 6) === 0 && (En = Fl() + 500, ve(0));
                  }
                }
                break;
              case 31:
              case 13:
                c = Uu(n, 2), c !== null && Jl(c, n, 2), An(), ii(n, 2);
            }
          if (n = oi(a), n === null && $c(
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
        $c(
          l,
          t,
          a,
          null,
          u
        );
    }
  }
  function oi(l) {
    return l = vf(l), yi(l);
  }
  var Xn = null;
  function yi(l) {
    if (Xn = null, l = Vu(l), l !== null) {
      var t = Z(l);
      if (t === null) l = null;
      else {
        var u = t.tag;
        if (u === 13) {
          if (l = K(t), l !== null) return l;
          l = null;
        } else if (u === 31) {
          if (l = rl(t), l !== null) return l;
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
  function _y(l) {
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
        switch (sv()) {
          case Ri:
            return 2;
          case Hi:
            return 8;
          case pe:
          case ov:
            return 32;
          case Ci:
            return 268435456;
          default:
            return 32;
        }
      default:
        return 32;
    }
  }
  var vi = !1, hu = null, Su = null, gu = null, be = /* @__PURE__ */ new Map(), Te = /* @__PURE__ */ new Map(), ru = [], _m = "mousedown mouseup touchcancel touchend touchstart auxclick dblclick pointercancel pointerdown pointerup dragend dragstart drop compositionend compositionstart keydown keypress keyup input textInput copy cut paste click change contextmenu reset".split(
    " "
  );
  function Oy(l, t) {
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
        be.delete(t.pointerId);
        break;
      case "gotpointercapture":
      case "lostpointercapture":
        Te.delete(t.pointerId);
    }
  }
  function Ee(l, t, u, a, e, n) {
    return l === null || l.nativeEvent !== n ? (l = {
      blockedOn: t,
      domEventName: u,
      eventSystemFlags: a,
      nativeEvent: n,
      targetContainers: [e]
    }, t !== null && (t = Ku(t), t !== null && zy(t)), l) : (l.eventSystemFlags |= a, t = l.targetContainers, e !== null && t.indexOf(e) === -1 && t.push(e), l);
  }
  function Om(l, t, u, a, e) {
    switch (t) {
      case "focusin":
        return hu = Ee(
          hu,
          l,
          t,
          u,
          a,
          e
        ), !0;
      case "dragenter":
        return Su = Ee(
          Su,
          l,
          t,
          u,
          a,
          e
        ), !0;
      case "mouseover":
        return gu = Ee(
          gu,
          l,
          t,
          u,
          a,
          e
        ), !0;
      case "pointerover":
        var n = e.pointerId;
        return be.set(
          n,
          Ee(
            be.get(n) || null,
            l,
            t,
            u,
            a,
            e
          )
        ), !0;
      case "gotpointercapture":
        return n = e.pointerId, Te.set(
          n,
          Ee(
            Te.get(n) || null,
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
  function My(l) {
    var t = Vu(l.target);
    if (t !== null) {
      var u = Z(t);
      if (u !== null) {
        if (t = u.tag, t === 13) {
          if (t = K(u), t !== null) {
            l.blockedOn = t, Xi(l.priority, function() {
              Ay(u);
            });
            return;
          }
        } else if (t === 31) {
          if (t = rl(u), t !== null) {
            l.blockedOn = t, Xi(l.priority, function() {
              Ay(u);
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
      var u = oi(l.nativeEvent);
      if (u === null) {
        u = l.nativeEvent;
        var a = new u.constructor(
          u.type,
          u
        );
        yf = a, u.target.dispatchEvent(a), yf = null;
      } else
        return t = Ku(u), t !== null && zy(t), l.blockedOn = u, !1;
      t.shift();
    }
    return !0;
  }
  function py(l, t, u) {
    Qn(l) && u.delete(t);
  }
  function Mm() {
    vi = !1, hu !== null && Qn(hu) && (hu = null), Su !== null && Qn(Su) && (Su = null), gu !== null && Qn(gu) && (gu = null), be.forEach(py), Te.forEach(py);
  }
  function Zn(l, t) {
    l.blockedOn === t && (l.blockedOn = null, vi || (vi = !0, y.unstable_scheduleCallback(
      y.unstable_NormalPriority,
      Mm
    )));
  }
  var Ln = null;
  function Dy(l) {
    Ln !== l && (Ln = l, y.unstable_scheduleCallback(
      y.unstable_NormalPriority,
      function() {
        Ln === l && (Ln = null);
        for (var t = 0; t < l.length; t += 3) {
          var u = l[t], a = l[t + 1], e = l[t + 2];
          if (typeof a != "function") {
            if (yi(a || u) === null)
              continue;
            break;
          }
          var n = Ku(u);
          n !== null && (l.splice(t, 3), t -= 3, oc(
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
    hu !== null && Zn(hu, l), Su !== null && Zn(Su, l), gu !== null && Zn(gu, l), be.forEach(t), Te.forEach(t);
    for (var u = 0; u < ru.length; u++) {
      var a = ru[u];
      a.blockedOn === l && (a.blockedOn = null);
    }
    for (; 0 < ru.length && (u = ru[0], u.blockedOn === null); )
      My(u), u.blockedOn === null && ru.shift();
    if (u = (l.ownerDocument || l).$$reactFormReplay, u != null)
      for (a = 0; a < u.length; a += 3) {
        var e = u[a], n = u[a + 1], f = e[Ql] || null;
        if (typeof n == "function")
          f || Dy(u);
        else if (f) {
          var c = null;
          if (n && n.hasAttribute("formAction")) {
            if (e = n, f = n[Ql] || null)
              c = f.formAction;
            else if (yi(e) !== null) continue;
          } else c = f.action;
          typeof c == "function" ? u[a + 1] = c : (u.splice(a, 3), a -= 3), Dy(u);
        }
      }
  }
  function Uy() {
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
  function di(l) {
    this._internalRoot = l;
  }
  xn.prototype.render = di.prototype.render = function(l) {
    var t = this._internalRoot;
    if (t === null) throw Error(d(409));
    var u = t.current, a = nt();
    Ty(u, a, l, t, null, null);
  }, xn.prototype.unmount = di.prototype.unmount = function() {
    var l = this._internalRoot;
    if (l !== null) {
      this._internalRoot = null;
      var t = l.containerInfo;
      Ty(l.current, 2, null, l, null, null), An(), t[xu] = null;
    }
  };
  function xn(l) {
    this._internalRoot = l;
  }
  xn.prototype.unstable_scheduleHydration = function(l) {
    if (l) {
      var t = ji();
      l = { blockedOn: null, target: l, priority: t };
      for (var u = 0; u < ru.length && t !== 0 && t < ru[u].priority; u++) ;
      ru.splice(u, 0, l), u === 0 && My(l);
    }
  };
  var Ny = A.version;
  if (Ny !== "19.2.4")
    throw Error(
      d(
        527,
        Ny,
        "19.2.4"
      )
    );
  M.findDOMNode = function(l) {
    var t = l._reactInternals;
    if (t === void 0)
      throw typeof l.render == "function" ? Error(d(188)) : (l = Object.keys(l).join(","), Error(d(268, l)));
    return l = O(t), l = l !== null ? F(l) : null, l = l === null ? null : l.stateNode, l;
  };
  var pm = {
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
        Na = Vn.inject(
          pm
        ), kl = Vn;
      } catch {
      }
  }
  return Ae.createRoot = function(l, t) {
    if (!U(l)) throw Error(d(299));
    var u = !1, a = "", e = Go, n = jo, f = Xo;
    return t != null && (t.unstable_strictMode === !0 && (u = !0), t.identifierPrefix !== void 0 && (a = t.identifierPrefix), t.onUncaughtError !== void 0 && (e = t.onUncaughtError), t.onCaughtError !== void 0 && (n = t.onCaughtError), t.onRecoverableError !== void 0 && (f = t.onRecoverableError)), t = ry(
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
      Uy
    ), l[xu] = t.current, Wc(l), new di(t);
  }, Ae.hydrateRoot = function(l, t, u) {
    if (!U(l)) throw Error(d(299));
    var a = !1, e = "", n = Go, f = jo, c = Xo, i = null;
    return u != null && (u.unstable_strictMode === !0 && (a = !0), u.identifierPrefix !== void 0 && (e = u.identifierPrefix), u.onUncaughtError !== void 0 && (n = u.onUncaughtError), u.onCaughtError !== void 0 && (f = u.onCaughtError), u.onRecoverableError !== void 0 && (c = u.onRecoverableError), u.formState !== void 0 && (i = u.formState)), t = ry(
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
      Uy
    ), t.context = by(null), u = t.current, a = nt(), a = uf(a), e = au(a), e.callback = null, eu(u, e, a), u = a, t.current.lanes = u, Ha(t, u), pt(t), l[xu] = t.current, Wc(l), new xn(t);
  }, Ae.version = "19.2.4", Ae;
}
var Zy;
function Jm() {
  if (Zy) return Si.exports;
  Zy = 1;
  function y() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(y);
      } catch (A) {
        console.error(A);
      }
  }
  return y(), Si.exports = Km(), Si.exports;
}
var wm = Jm(), uv = tv();
const u1 = /* @__PURE__ */ Wy(uv);
class av {
  constructor(A) {
    this._subscribers = /* @__PURE__ */ new Set(), this.getSnapshot = () => this._state, this.subscribeStore = (_) => (this._subscribers.add(_), () => this._subscribers.delete(_)), this._state = { ...A };
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
const Da = wl.createContext(null), Eu = /* @__PURE__ */ new Map();
let ev = "", Ly = !1;
function pa() {
  return ev + "/";
}
function Di(y, A, _, d, U) {
  U !== void 0 && (ev = U), Ly ? U !== void 0 && qy(pa() + "react-api/events") : (Ly = !0, Nm(pa()), qy(pa() + "react-api/events"));
  const Z = d ?? "";
  Rm(Z);
  const K = document.getElementById(y);
  if (!K) {
    console.error("[TLReact] Mount point not found:", y);
    return;
  }
  const rl = wy(A);
  if (!rl) {
    console.error("[TLReact] Component not registered:", A);
    return;
  }
  _i(y);
  const H = new av(_);
  _.hidden === !0 && (K.style.display = "none");
  const O = (ll) => {
    H.applyPatch(ll);
  };
  Py(y, O);
  const F = wm.createRoot(K);
  Eu.set(y, { root: F, store: H, sseListener: O }), nv = Z;
  const B = () => {
    const ll = wl.useSyncExternalStore(H.subscribeStore, H.getSnapshot);
    return wl.useLayoutEffect(() => {
      K.style.display = ll.hidden === !0 ? "none" : "";
    }, [ll.hidden]), Kn.createElement(
      Da.Provider,
      { value: { controlId: y, windowName: Z, store: H } },
      Kn.createElement(rl, { controlId: y, state: ll })
    );
  };
  uv.flushSync(() => {
    F.render(Kn.createElement(B));
  });
}
function Wm(y, A, _) {
  Di(y, A, _);
}
function _i(y) {
  const A = Eu.get(y);
  A && (jm(y, A.sseListener), A.root && A.root.unmount(), Eu.delete(y));
}
function $m(y, A) {
  let _ = Eu.get(y);
  if (!_) {
    const U = new av(A), Z = (K) => {
      U.applyPatch(K);
    };
    Py(y, Z), _ = { root: null, store: U, sseListener: Z }, Eu.set(y, _);
  }
  return { controlId: y, windowName: nv, store: _.store };
}
let nv = "";
function Fm() {
  const y = wl.useContext(Da);
  if (!y)
    throw new Error("useTLState must be used inside a TLReact-mounted component.");
  return wl.useSyncExternalStore(y.store.subscribeStore, y.store.getSnapshot);
}
function km() {
  const y = wl.useContext(Da);
  if (!y)
    throw new Error("useTLCommand must be used inside a TLReact-mounted component.");
  const A = y.controlId, _ = y.windowName;
  return wl.useCallback(
    async (d, U) => {
      const Z = JSON.stringify({
        controlId: A,
        command: d,
        windowName: _,
        arguments: U ?? {}
      });
      try {
        const K = await fetch(pa() + "react-api/command", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: Z
        });
        K.ok || console.error("[TLReact] Command failed:", K.status, await K.text());
      } catch (K) {
        console.error("[TLReact] Command error:", K);
      }
    },
    [A, _]
  );
}
function a1() {
  const y = wl.useContext(Da);
  if (!y)
    throw new Error("useTLUpload must be used inside a TLReact-mounted component.");
  const A = y.controlId, _ = y.windowName;
  return wl.useCallback(
    async (d) => {
      d.append("controlId", A), d.append("windowName", _);
      try {
        const U = await fetch(pa() + "react-api/upload", {
          method: "POST",
          body: d
        });
        U.ok || console.error("[TLReact] Upload failed:", U.status, await U.text());
      } catch (U) {
        console.error("[TLReact] Upload error:", U);
      }
    },
    [A, _]
  );
}
function e1() {
  const y = wl.useContext(Da);
  if (!y)
    throw new Error("useTLDataUrl must be used inside a TLReact-mounted component.");
  return pa() + "react-api/data?controlId=" + encodeURIComponent(y.controlId);
}
function n1() {
  const y = Fm(), A = km(), _ = wl.useCallback(
    (d) => {
      A("valueChanged", { value: d });
    },
    [A]
  );
  return [y.value, _];
}
function Wn(y = document.body) {
  const A = y.querySelectorAll("[data-react-module]");
  for (const _ of A) {
    if (!_.id || Eu.has(_.id))
      continue;
    const d = _.dataset.reactModule, U = _.dataset.windowName, Z = _.dataset.contextPath;
    if (!d || U === void 0 || Z === void 0)
      continue;
    const K = _.dataset.reactState, rl = K ? JSON.parse(K) : {};
    Di(_.id, d, rl, U, Z);
  }
}
function xy() {
  new MutationObserver((A) => {
    var _;
    for (const d of A)
      for (const U of d.removedNodes)
        if (U instanceof HTMLElement) {
          const Z = U.id;
          Z && Eu.has(Z) && _i(Z);
          for (const [K] of Eu)
            U.querySelector("#" + CSS.escape(K)) && _i(K);
        }
    for (const d of A)
      for (const U of d.addedNodes)
        U instanceof HTMLElement && ((_ = U.dataset) != null && _.reactModule ? Wn(U.parentElement ?? document.body) : U.querySelector("[data-react-module]") && Wn(U));
  }).observe(document.body, { childList: !0, subtree: !0 });
}
document.readyState === "loading" ? document.addEventListener("DOMContentLoaded", xy) : xy();
window.addEventListener("load", () => Wn());
var Ti = { exports: {} }, _e = {};
/**
 * @license React
 * react-jsx-runtime.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var Vy;
function Im() {
  if (Vy) return _e;
  Vy = 1;
  var y = Symbol.for("react.transitional.element"), A = Symbol.for("react.fragment");
  function _(d, U, Z) {
    var K = null;
    if (Z !== void 0 && (K = "" + Z), U.key !== void 0 && (K = "" + U.key), "key" in U) {
      Z = {};
      for (var rl in U)
        rl !== "key" && (Z[rl] = U[rl]);
    } else Z = U;
    return U = Z.ref, {
      $$typeof: y,
      type: d,
      key: K,
      ref: U !== void 0 ? U : null,
      props: Z
    };
  }
  return _e.Fragment = A, _e.jsx = _, _e.jsxs = _, _e;
}
var Ky;
function Pm() {
  return Ky || (Ky = 1, Ti.exports = Im()), Ti.exports;
}
var Ei = Pm();
const f1 = ({ control: y }) => {
  const A = y, _ = wy(A.module), d = wl.useMemo(
    () => $m(A.controlId, A.state),
    [A.controlId]
  ), U = wl.useSyncExternalStore(d.store.subscribeStore, d.store.getSnapshot);
  return _ ? /* @__PURE__ */ Ei.jsx(Da.Provider, { value: d, children: /* @__PURE__ */ Ei.jsx(_, { controlId: A.controlId, state: U }) }) : /* @__PURE__ */ Ei.jsxs("span", { children: [
    "[Component not registered: ",
    A.module,
    "]"
  ] });
};
window.TLReact = { mount: Di, mountField: Wm, discoverAndMount: Wn };
export {
  Kn as React,
  u1 as ReactDOM,
  f1 as TLChild,
  Da as TLControlContext,
  qy as connect,
  $m as createChildContext,
  Wn as discoverAndMount,
  wy as getComponent,
  Di as mount,
  Wm as mountField,
  l1 as register,
  Py as subscribe,
  _i as unmount,
  jm as unsubscribe,
  t1 as useI18N,
  km as useTLCommand,
  e1 as useTLDataUrl,
  n1 as useTLFieldValue,
  Fm as useTLState,
  a1 as useTLUpload
};
