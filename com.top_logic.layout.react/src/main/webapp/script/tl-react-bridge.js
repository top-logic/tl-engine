function Ag(i, c) {
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
const Fv = /* @__PURE__ */ new Map();
function YS(i, c) {
  Fv.set(i, c);
}
function $v(i) {
  return Fv.get(i);
}
function Iv(i) {
  return i && i.__esModule && Object.prototype.hasOwnProperty.call(i, "default") ? i.default : i;
}
var Eo = { exports: {} }, ut = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var iv;
function Rg() {
  if (iv) return ut;
  iv = 1;
  var i = Symbol.for("react.transitional.element"), c = Symbol.for("react.portal"), o = Symbol.for("react.fragment"), f = Symbol.for("react.strict_mode"), r = Symbol.for("react.profiler"), m = Symbol.for("react.consumer"), v = Symbol.for("react.context"), p = Symbol.for("react.forward_ref"), g = Symbol.for("react.suspense"), h = Symbol.for("react.memo"), z = Symbol.for("react.lazy"), _ = Symbol.for("react.activity"), w = Symbol.iterator;
  function C(b) {
    return b === null || typeof b != "object" ? null : (b = w && b[w] || b["@@iterator"], typeof b == "function" ? b : null);
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
  function X(b, U, j) {
    this.props = b, this.context = U, this.refs = V, this.updater = j || Y;
  }
  X.prototype.isReactComponent = {}, X.prototype.setState = function(b, U) {
    if (typeof b != "object" && typeof b != "function" && b != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, b, U, "setState");
  }, X.prototype.forceUpdate = function(b) {
    this.updater.enqueueForceUpdate(this, b, "forceUpdate");
  };
  function k() {
  }
  k.prototype = X.prototype;
  function G(b, U, j) {
    this.props = b, this.context = U, this.refs = V, this.updater = j || Y;
  }
  var $ = G.prototype = new k();
  $.constructor = G, q($, X.prototype), $.isPureReactComponent = !0;
  var at = Array.isArray;
  function lt() {
  }
  var Z = { H: null, A: null, T: null, S: null }, it = Object.prototype.hasOwnProperty;
  function _t(b, U, j) {
    var K = j.ref;
    return {
      $$typeof: i,
      type: b,
      key: U,
      ref: K !== void 0 ? K : null,
      props: j
    };
  }
  function Tt(b, U) {
    return _t(b.type, U, b.props);
  }
  function zt(b) {
    return typeof b == "object" && b !== null && b.$$typeof === i;
  }
  function dt(b) {
    var U = { "=": "=0", ":": "=2" };
    return "$" + b.replace(/[=:]/g, function(j) {
      return U[j];
    });
  }
  var Ct = /\/+/g;
  function Q(b, U) {
    return typeof b == "object" && b !== null && b.key != null ? dt("" + b.key) : U.toString(36);
  }
  function ft(b) {
    switch (b.status) {
      case "fulfilled":
        return b.value;
      case "rejected":
        throw b.reason;
      default:
        switch (typeof b.status == "string" ? b.then(lt, lt) : (b.status = "pending", b.then(
          function(U) {
            b.status === "pending" && (b.status = "fulfilled", b.value = U);
          },
          function(U) {
            b.status === "pending" && (b.status = "rejected", b.reason = U);
          }
        )), b.status) {
          case "fulfilled":
            return b.value;
          case "rejected":
            throw b.reason;
        }
    }
    throw b;
  }
  function R(b, U, j, K, I) {
    var nt = typeof b;
    (nt === "undefined" || nt === "boolean") && (b = null);
    var mt = !1;
    if (b === null) mt = !0;
    else
      switch (nt) {
        case "bigint":
        case "string":
        case "number":
          mt = !0;
          break;
        case "object":
          switch (b.$$typeof) {
            case i:
            case c:
              mt = !0;
              break;
            case z:
              return mt = b._init, R(
                mt(b._payload),
                U,
                j,
                K,
                I
              );
          }
      }
    if (mt)
      return I = I(b), mt = K === "" ? "." + Q(b, 0) : K, at(I) ? (j = "", mt != null && (j = mt.replace(Ct, "$&/") + "/"), R(I, U, j, "", function(Gl) {
        return Gl;
      })) : I != null && (zt(I) && (I = Tt(
        I,
        j + (I.key == null || b && b.key === I.key ? "" : ("" + I.key).replace(
          Ct,
          "$&/"
        ) + "/") + mt
      )), U.push(I)), 1;
    mt = 0;
    var kt = K === "" ? "." : K + ":";
    if (at(b))
      for (var Nt = 0; Nt < b.length; Nt++)
        K = b[Nt], nt = kt + Q(K, Nt), mt += R(
          K,
          U,
          j,
          nt,
          I
        );
    else if (Nt = C(b), typeof Nt == "function")
      for (b = Nt.call(b), Nt = 0; !(K = b.next()).done; )
        K = K.value, nt = kt + Q(K, Nt++), mt += R(
          K,
          U,
          j,
          nt,
          I
        );
    else if (nt === "object") {
      if (typeof b.then == "function")
        return R(
          ft(b),
          U,
          j,
          K,
          I
        );
      throw U = String(b), Error(
        "Objects are not valid as a React child (found: " + (U === "[object Object]" ? "object with keys {" + Object.keys(b).join(", ") + "}" : U) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return mt;
  }
  function L(b, U, j) {
    if (b == null) return b;
    var K = [], I = 0;
    return R(b, K, "", "", function(nt) {
      return U.call(j, nt, I++);
    }), K;
  }
  function H(b) {
    if (b._status === -1) {
      var U = b._result;
      U = U(), U.then(
        function(j) {
          (b._status === 0 || b._status === -1) && (b._status = 1, b._result = j);
        },
        function(j) {
          (b._status === 0 || b._status === -1) && (b._status = 2, b._result = j);
        }
      ), b._status === -1 && (b._status = 0, b._result = U);
    }
    if (b._status === 1) return b._result.default;
    throw b._result;
  }
  var F = typeof reportError == "function" ? reportError : function(b) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var U = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof b == "object" && b !== null && typeof b.message == "string" ? String(b.message) : String(b),
        error: b
      });
      if (!window.dispatchEvent(U)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", b);
      return;
    }
    console.error(b);
  }, et = {
    map: L,
    forEach: function(b, U, j) {
      L(
        b,
        function() {
          U.apply(this, arguments);
        },
        j
      );
    },
    count: function(b) {
      var U = 0;
      return L(b, function() {
        U++;
      }), U;
    },
    toArray: function(b) {
      return L(b, function(U) {
        return U;
      }) || [];
    },
    only: function(b) {
      if (!zt(b))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return b;
    }
  };
  return ut.Activity = _, ut.Children = et, ut.Component = X, ut.Fragment = o, ut.Profiler = r, ut.PureComponent = G, ut.StrictMode = f, ut.Suspense = g, ut.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = Z, ut.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(b) {
      return Z.H.useMemoCache(b);
    }
  }, ut.cache = function(b) {
    return function() {
      return b.apply(null, arguments);
    };
  }, ut.cacheSignal = function() {
    return null;
  }, ut.cloneElement = function(b, U, j) {
    if (b == null)
      throw Error(
        "The argument must be a React element, but you passed " + b + "."
      );
    var K = q({}, b.props), I = b.key;
    if (U != null)
      for (nt in U.key !== void 0 && (I = "" + U.key), U)
        !it.call(U, nt) || nt === "key" || nt === "__self" || nt === "__source" || nt === "ref" && U.ref === void 0 || (K[nt] = U[nt]);
    var nt = arguments.length - 2;
    if (nt === 1) K.children = j;
    else if (1 < nt) {
      for (var mt = Array(nt), kt = 0; kt < nt; kt++)
        mt[kt] = arguments[kt + 2];
      K.children = mt;
    }
    return _t(b.type, I, K);
  }, ut.createContext = function(b) {
    return b = {
      $$typeof: v,
      _currentValue: b,
      _currentValue2: b,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, b.Provider = b, b.Consumer = {
      $$typeof: m,
      _context: b
    }, b;
  }, ut.createElement = function(b, U, j) {
    var K, I = {}, nt = null;
    if (U != null)
      for (K in U.key !== void 0 && (nt = "" + U.key), U)
        it.call(U, K) && K !== "key" && K !== "__self" && K !== "__source" && (I[K] = U[K]);
    var mt = arguments.length - 2;
    if (mt === 1) I.children = j;
    else if (1 < mt) {
      for (var kt = Array(mt), Nt = 0; Nt < mt; Nt++)
        kt[Nt] = arguments[Nt + 2];
      I.children = kt;
    }
    if (b && b.defaultProps)
      for (K in mt = b.defaultProps, mt)
        I[K] === void 0 && (I[K] = mt[K]);
    return _t(b, nt, I);
  }, ut.createRef = function() {
    return { current: null };
  }, ut.forwardRef = function(b) {
    return { $$typeof: p, render: b };
  }, ut.isValidElement = zt, ut.lazy = function(b) {
    return {
      $$typeof: z,
      _payload: { _status: -1, _result: b },
      _init: H
    };
  }, ut.memo = function(b, U) {
    return {
      $$typeof: h,
      type: b,
      compare: U === void 0 ? null : U
    };
  }, ut.startTransition = function(b) {
    var U = Z.T, j = {};
    Z.T = j;
    try {
      var K = b(), I = Z.S;
      I !== null && I(j, K), typeof K == "object" && K !== null && typeof K.then == "function" && K.then(lt, F);
    } catch (nt) {
      F(nt);
    } finally {
      U !== null && j.types !== null && (U.types = j.types), Z.T = U;
    }
  }, ut.unstable_useCacheRefresh = function() {
    return Z.H.useCacheRefresh();
  }, ut.use = function(b) {
    return Z.H.use(b);
  }, ut.useActionState = function(b, U, j) {
    return Z.H.useActionState(b, U, j);
  }, ut.useCallback = function(b, U) {
    return Z.H.useCallback(b, U);
  }, ut.useContext = function(b) {
    return Z.H.useContext(b);
  }, ut.useDebugValue = function() {
  }, ut.useDeferredValue = function(b, U) {
    return Z.H.useDeferredValue(b, U);
  }, ut.useEffect = function(b, U) {
    return Z.H.useEffect(b, U);
  }, ut.useEffectEvent = function(b) {
    return Z.H.useEffectEvent(b);
  }, ut.useId = function() {
    return Z.H.useId();
  }, ut.useImperativeHandle = function(b, U, j) {
    return Z.H.useImperativeHandle(b, U, j);
  }, ut.useInsertionEffect = function(b, U) {
    return Z.H.useInsertionEffect(b, U);
  }, ut.useLayoutEffect = function(b, U) {
    return Z.H.useLayoutEffect(b, U);
  }, ut.useMemo = function(b, U) {
    return Z.H.useMemo(b, U);
  }, ut.useOptimistic = function(b, U) {
    return Z.H.useOptimistic(b, U);
  }, ut.useReducer = function(b, U, j) {
    return Z.H.useReducer(b, U, j);
  }, ut.useRef = function(b) {
    return Z.H.useRef(b);
  }, ut.useState = function(b) {
    return Z.H.useState(b);
  }, ut.useSyncExternalStore = function(b, U, j) {
    return Z.H.useSyncExternalStore(
      b,
      U,
      j
    );
  }, ut.useTransition = function() {
    return Z.H.useTransition();
  }, ut.version = "19.2.4", ut;
}
var cv;
function Vo() {
  return cv || (cv = 1, Eo.exports = Rg()), Eo.exports;
}
var B = Vo();
const Jt = /* @__PURE__ */ Iv(B), Pv = /* @__PURE__ */ Ag({
  __proto__: null,
  default: Jt
}, [B]), Zo = /* @__PURE__ */ new Map(), xi = /* @__PURE__ */ new Set();
let No = !1, Ko = 0;
const Uo = /* @__PURE__ */ new Set();
let th = "", eh = "";
function Og(i) {
  th = i;
}
function _g(i) {
  eh = i;
}
function lh() {
  for (const i of Uo)
    i();
}
function zg(i) {
  return Uo.add(i), () => Uo.delete(i);
}
function Mg() {
  return Ko;
}
function Cg(i) {
  xi.add(i), No || (No = !0, queueMicrotask(xg));
}
async function xg() {
  if (No = !1, xi.size === 0)
    return;
  const i = Array.from(xi);
  xi.clear();
  try {
    const c = th + "react-api/i18n?keys=" + encodeURIComponent(i.join(",")) + "&windowName=" + encodeURIComponent(eh), o = await fetch(c);
    if (!o.ok) {
      console.error("[TLReact] i18n fetch failed:", o.status);
      return;
    }
    const f = await o.json();
    for (const [r, m] of Object.entries(f))
      Zo.set(r, m);
    Ko++, lh();
  } catch (c) {
    console.error("[TLReact] i18n fetch error:", c);
  }
}
function jS(i) {
  Jt.useSyncExternalStore(zg, Mg);
  const c = {};
  for (const [o, f] of Object.entries(i)) {
    const r = Zo.get(o);
    r !== void 0 ? c[o] = r : (c[o] = f, Cg(o));
  }
  return c;
}
function nh() {
  Zo.clear(), Ko++, lh();
}
const ah = "valueChanged", Dg = "submit", wg = "stale-ui", fv = "tlReactStaleUiReloadTime", Ng = 1e4;
let ov = Promise.resolve();
const Bi = /* @__PURE__ */ new Set();
async function Ug(i, c) {
  try {
    const o = await fetch(i, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(c)
    });
    if (!o.ok) {
      const f = await o.text();
      if (Hg(f)) {
        Bg();
        return;
      }
      console.error("[TLReact] Command failed:", o.status, f);
    }
  } catch (o) {
    console.error("[TLReact] Command error:", o);
  }
}
function Hg(i) {
  try {
    return JSON.parse(i).errorCode === wg;
  } catch {
    return !1;
  }
}
function Bg() {
  const i = Number(window.sessionStorage.getItem(fv) ?? "0");
  if (Date.now() - i < Ng) {
    console.error("[TLReact] Server-side UI state still missing right after a reload; not reloading again.");
    return;
  }
  window.sessionStorage.setItem(fv, String(Date.now())), console.warn("[TLReact] Server no longer knows this page (session replaced or window discarded). Reloading."), window.location.reload();
}
function Ki(i, c) {
  c.command !== ah && qg();
  const o = ov.then(() => Ug(i, c));
  return ov = o, o;
}
function sv(i) {
  Bi.add(i);
}
function Lg(i) {
  Bi.delete(i);
}
function qg() {
  if (Bi.size === 0)
    return Promise.resolve();
  const i = Array.from(Bi);
  return Promise.all(i.map((c) => c())).then(() => {
  });
}
const sn = /* @__PURE__ */ new Map();
let $a = null;
function Jo() {
  return document.body.dataset.windowName ?? "";
}
function Ji() {
  return document.body.dataset.contextPath ?? "";
}
function Yg(i) {
  const c = [];
  return i.width > 0 && c.push(`width=${i.width}`), i.height > 0 && c.push(`height=${i.height}`), c.push(`resizable=${i.resizable ? "yes" : "no"}`), c.join(",");
}
function jg() {
  $a === null && ($a = setInterval(() => {
    for (const [i, c] of sn)
      c.closed && (sn.delete(i), Xg(i));
    sn.size === 0 && $a !== null && (clearInterval($a), $a = null);
  }, 2e3));
}
function Xg(i) {
  const c = Ji(), o = Jo();
  Ki(`${c}/react-api/command`, {
    controlId: "",
    command: "windowClosed",
    windowName: o,
    arguments: { windowId: i }
  });
}
function Gg(i) {
  const o = `${Ji()}/view/${i.windowId}/`, f = window.open(o, i.windowId, Yg(i));
  f ? (sn.set(i.windowId, f), jg()) : Zg(i.windowId);
}
function Qg(i) {
  const c = sn.get(i.windowId);
  c && (c.close(), sn.delete(i.windowId));
}
function Vg(i) {
  const c = sn.get(i.windowId);
  c && !c.closed && c.focus();
}
function Zg(i) {
  const c = Ji(), o = Jo();
  Ki(`${c}/react-api/command`, {
    controlId: "",
    command: "windowBlocked",
    windowName: o,
    arguments: { windowId: i }
  });
}
function Kg() {
  window.addEventListener("beforeunload", () => {
    const i = Ji(), c = Jo();
    if (!c) return;
    const o = JSON.stringify({
      controlId: "",
      command: "windowClosed",
      windowName: c,
      arguments: { windowId: c, unload: !0 }
    }), f = new Blob([o], { type: "application/json" });
    navigator.sendBeacon(`${i}/react-api/command`, f);
  });
}
let rv = !1;
function Jg() {
  return document.body.dataset.windowName ?? "";
}
function kg() {
  return document.body.dataset.contextPath ?? "";
}
function Wg() {
  rv || (rv = !0, window.addEventListener("popstate", () => {
    const i = tb(), c = window.location.search;
    Ig(i + c);
  }), Pg());
}
function Fg(i) {
  const o = ko() + i.url;
  i.replace ? history.replaceState(null, "", o) : history.pushState(null, "", o);
}
function $g(i) {
  const c = ko();
  history.replaceState(null, "", c + i.currentUrl);
}
function Ig(i) {
  const c = kg(), o = Jg();
  Ki(`${c}/react-api/command`, {
    controlId: "",
    command: "navigateToRoute",
    windowName: o,
    arguments: { url: i }
  });
}
function Pg() {
  const i = window.location.pathname, c = i.indexOf("/view/");
  if (c < 0) return;
  const o = i.substring(c + 6), f = o.indexOf("/");
  if ((f >= 0 ? o.substring(0, f) : o).match(/^v[0-9a-f]+$/i)) {
    const m = f >= 0 ? o.substring(f + 1) : "", v = i.substring(0, c + 6) + m;
    history.replaceState(null, "", v + window.location.search);
  }
}
function tb() {
  const i = window.location.pathname, c = ko();
  return i.substring(c.length);
}
function ko() {
  const i = window.location.pathname, c = i.indexOf("/view/");
  return c >= 0 ? i.substring(0, c + 6) : "/";
}
var To = { exports: {} }, Ia = {}, Ao = { exports: {} }, Ro = {};
/**
 * @license React
 * scheduler.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var dv;
function eb() {
  return dv || (dv = 1, (function(i) {
    function c(R, L) {
      var H = R.length;
      R.push(L);
      t: for (; 0 < H; ) {
        var F = H - 1 >>> 1, et = R[F];
        if (0 < r(et, L))
          R[F] = L, R[H] = et, H = F;
        else break t;
      }
    }
    function o(R) {
      return R.length === 0 ? null : R[0];
    }
    function f(R) {
      if (R.length === 0) return null;
      var L = R[0], H = R.pop();
      if (H !== L) {
        R[0] = H;
        t: for (var F = 0, et = R.length, b = et >>> 1; F < b; ) {
          var U = 2 * (F + 1) - 1, j = R[U], K = U + 1, I = R[K];
          if (0 > r(j, H))
            K < et && 0 > r(I, j) ? (R[F] = I, R[K] = H, F = K) : (R[F] = j, R[U] = H, F = U);
          else if (K < et && 0 > r(I, H))
            R[F] = I, R[K] = H, F = K;
          else break t;
        }
      }
      return L;
    }
    function r(R, L) {
      var H = R.sortIndex - L.sortIndex;
      return H !== 0 ? H : R.id - L.id;
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
    var g = [], h = [], z = 1, _ = null, w = 3, C = !1, Y = !1, q = !1, V = !1, X = typeof setTimeout == "function" ? setTimeout : null, k = typeof clearTimeout == "function" ? clearTimeout : null, G = typeof setImmediate < "u" ? setImmediate : null;
    function $(R) {
      for (var L = o(h); L !== null; ) {
        if (L.callback === null) f(h);
        else if (L.startTime <= R)
          f(h), L.sortIndex = L.expirationTime, c(g, L);
        else break;
        L = o(h);
      }
    }
    function at(R) {
      if (q = !1, $(R), !Y)
        if (o(g) !== null)
          Y = !0, lt || (lt = !0, dt());
        else {
          var L = o(h);
          L !== null && ft(at, L.startTime - R);
        }
    }
    var lt = !1, Z = -1, it = 5, _t = -1;
    function Tt() {
      return V ? !0 : !(i.unstable_now() - _t < it);
    }
    function zt() {
      if (V = !1, lt) {
        var R = i.unstable_now();
        _t = R;
        var L = !0;
        try {
          t: {
            Y = !1, q && (q = !1, k(Z), Z = -1), C = !0;
            var H = w;
            try {
              e: {
                for ($(R), _ = o(g); _ !== null && !(_.expirationTime > R && Tt()); ) {
                  var F = _.callback;
                  if (typeof F == "function") {
                    _.callback = null, w = _.priorityLevel;
                    var et = F(
                      _.expirationTime <= R
                    );
                    if (R = i.unstable_now(), typeof et == "function") {
                      _.callback = et, $(R), L = !0;
                      break e;
                    }
                    _ === o(g) && f(g), $(R);
                  } else f(g);
                  _ = o(g);
                }
                if (_ !== null) L = !0;
                else {
                  var b = o(h);
                  b !== null && ft(
                    at,
                    b.startTime - R
                  ), L = !1;
                }
              }
              break t;
            } finally {
              _ = null, w = H, C = !1;
            }
            L = void 0;
          }
        } finally {
          L ? dt() : lt = !1;
        }
      }
    }
    var dt;
    if (typeof G == "function")
      dt = function() {
        G(zt);
      };
    else if (typeof MessageChannel < "u") {
      var Ct = new MessageChannel(), Q = Ct.port2;
      Ct.port1.onmessage = zt, dt = function() {
        Q.postMessage(null);
      };
    } else
      dt = function() {
        X(zt, 0);
      };
    function ft(R, L) {
      Z = X(function() {
        R(i.unstable_now());
      }, L);
    }
    i.unstable_IdlePriority = 5, i.unstable_ImmediatePriority = 1, i.unstable_LowPriority = 4, i.unstable_NormalPriority = 3, i.unstable_Profiling = null, i.unstable_UserBlockingPriority = 2, i.unstable_cancelCallback = function(R) {
      R.callback = null;
    }, i.unstable_forceFrameRate = function(R) {
      0 > R || 125 < R ? console.error(
        "forceFrameRate takes a positive int between 0 and 125, forcing frame rates higher than 125 fps is not supported"
      ) : it = 0 < R ? Math.floor(1e3 / R) : 5;
    }, i.unstable_getCurrentPriorityLevel = function() {
      return w;
    }, i.unstable_next = function(R) {
      switch (w) {
        case 1:
        case 2:
        case 3:
          var L = 3;
          break;
        default:
          L = w;
      }
      var H = w;
      w = L;
      try {
        return R();
      } finally {
        w = H;
      }
    }, i.unstable_requestPaint = function() {
      V = !0;
    }, i.unstable_runWithPriority = function(R, L) {
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
      var H = w;
      w = R;
      try {
        return L();
      } finally {
        w = H;
      }
    }, i.unstable_scheduleCallback = function(R, L, H) {
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
        id: z++,
        callback: L,
        priorityLevel: R,
        startTime: H,
        expirationTime: et,
        sortIndex: -1
      }, H > F ? (R.sortIndex = H, c(h, R), o(g) === null && R === o(h) && (q ? (k(Z), Z = -1) : q = !0, ft(at, H - F))) : (R.sortIndex = et, c(g, R), Y || C || (Y = !0, lt || (lt = !0, dt()))), R;
    }, i.unstable_shouldYield = Tt, i.unstable_wrapCallback = function(R) {
      var L = w;
      return function() {
        var H = w;
        w = L;
        try {
          return R.apply(this, arguments);
        } finally {
          w = H;
        }
      };
    };
  })(Ro)), Ro;
}
var mv;
function lb() {
  return mv || (mv = 1, Ao.exports = eb()), Ao.exports;
}
var Oo = { exports: {} }, te = {};
/**
 * @license React
 * react-dom.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var vv;
function nb() {
  if (vv) return te;
  vv = 1;
  var i = Vo();
  function c(g) {
    var h = "https://react.dev/errors/" + g;
    if (1 < arguments.length) {
      h += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var z = 2; z < arguments.length; z++)
        h += "&args[]=" + encodeURIComponent(arguments[z]);
    }
    return "Minified React error #" + g + "; visit " + h + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
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
  function m(g, h, z) {
    var _ = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: r,
      key: _ == null ? null : "" + _,
      children: g,
      containerInfo: h,
      implementation: z
    };
  }
  var v = i.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function p(g, h) {
    if (g === "font") return "";
    if (typeof h == "string")
      return h === "use-credentials" ? h : "";
  }
  return te.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = f, te.createPortal = function(g, h) {
    var z = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!h || h.nodeType !== 1 && h.nodeType !== 9 && h.nodeType !== 11)
      throw Error(c(299));
    return m(g, h, null, z);
  }, te.flushSync = function(g) {
    var h = v.T, z = f.p;
    try {
      if (v.T = null, f.p = 2, g) return g();
    } finally {
      v.T = h, f.p = z, f.d.f();
    }
  }, te.preconnect = function(g, h) {
    typeof g == "string" && (h ? (h = h.crossOrigin, h = typeof h == "string" ? h === "use-credentials" ? h : "" : void 0) : h = null, f.d.C(g, h));
  }, te.prefetchDNS = function(g) {
    typeof g == "string" && f.d.D(g);
  }, te.preinit = function(g, h) {
    if (typeof g == "string" && h && typeof h.as == "string") {
      var z = h.as, _ = p(z, h.crossOrigin), w = typeof h.integrity == "string" ? h.integrity : void 0, C = typeof h.fetchPriority == "string" ? h.fetchPriority : void 0;
      z === "style" ? f.d.S(
        g,
        typeof h.precedence == "string" ? h.precedence : void 0,
        {
          crossOrigin: _,
          integrity: w,
          fetchPriority: C
        }
      ) : z === "script" && f.d.X(g, {
        crossOrigin: _,
        integrity: w,
        fetchPriority: C,
        nonce: typeof h.nonce == "string" ? h.nonce : void 0
      });
    }
  }, te.preinitModule = function(g, h) {
    if (typeof g == "string")
      if (typeof h == "object" && h !== null) {
        if (h.as == null || h.as === "script") {
          var z = p(
            h.as,
            h.crossOrigin
          );
          f.d.M(g, {
            crossOrigin: z,
            integrity: typeof h.integrity == "string" ? h.integrity : void 0,
            nonce: typeof h.nonce == "string" ? h.nonce : void 0
          });
        }
      } else h == null && f.d.M(g);
  }, te.preload = function(g, h) {
    if (typeof g == "string" && typeof h == "object" && h !== null && typeof h.as == "string") {
      var z = h.as, _ = p(z, h.crossOrigin);
      f.d.L(g, z, {
        crossOrigin: _,
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
  }, te.preloadModule = function(g, h) {
    if (typeof g == "string")
      if (h) {
        var z = p(h.as, h.crossOrigin);
        f.d.m(g, {
          as: typeof h.as == "string" && h.as !== "script" ? h.as : void 0,
          crossOrigin: z,
          integrity: typeof h.integrity == "string" ? h.integrity : void 0
        });
      } else f.d.m(g);
  }, te.requestFormReset = function(g) {
    f.d.r(g);
  }, te.unstable_batchedUpdates = function(g, h) {
    return g(h);
  }, te.useFormState = function(g, h, z) {
    return v.H.useFormState(g, h, z);
  }, te.useFormStatus = function() {
    return v.H.useHostTransitionStatus();
  }, te.version = "19.2.4", te;
}
var hv;
function uh() {
  if (hv) return Oo.exports;
  hv = 1;
  function i() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(i);
      } catch (c) {
        console.error(c);
      }
  }
  return i(), Oo.exports = nb(), Oo.exports;
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
var yv;
function ab() {
  if (yv) return Ia;
  yv = 1;
  var i = lb(), c = Vo(), o = uh();
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
  function g(t) {
    if (m(t) !== t)
      throw Error(f(188));
  }
  function h(t) {
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
          if (u === l) return g(a), t;
          if (u === n) return g(a), e;
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
  function z(t) {
    var e = t.tag;
    if (e === 5 || e === 26 || e === 27 || e === 6) return t;
    for (t = t.child; t !== null; ) {
      if (e = z(t), e !== null) return e;
      t = t.sibling;
    }
    return null;
  }
  var _ = Object.assign, w = Symbol.for("react.element"), C = Symbol.for("react.transitional.element"), Y = Symbol.for("react.portal"), q = Symbol.for("react.fragment"), V = Symbol.for("react.strict_mode"), X = Symbol.for("react.profiler"), k = Symbol.for("react.consumer"), G = Symbol.for("react.context"), $ = Symbol.for("react.forward_ref"), at = Symbol.for("react.suspense"), lt = Symbol.for("react.suspense_list"), Z = Symbol.for("react.memo"), it = Symbol.for("react.lazy"), _t = Symbol.for("react.activity"), Tt = Symbol.for("react.memo_cache_sentinel"), zt = Symbol.iterator;
  function dt(t) {
    return t === null || typeof t != "object" ? null : (t = zt && t[zt] || t["@@iterator"], typeof t == "function" ? t : null);
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
      case _t:
        return "Activity";
    }
    if (typeof t == "object")
      switch (t.$$typeof) {
        case Y:
          return "Portal";
        case G:
          return t.displayName || "Context";
        case k:
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
  var ft = Array.isArray, R = c.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE, L = o.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE, H = {
    pending: !1,
    data: null,
    method: null,
    action: null
  }, F = [], et = -1;
  function b(t) {
    return { current: t };
  }
  function U(t) {
    0 > et || (t.current = F[et], F[et] = null, et--);
  }
  function j(t, e) {
    et++, F[et] = t.current, t.current = e;
  }
  var K = b(null), I = b(null), nt = b(null), mt = b(null);
  function kt(t, e) {
    switch (j(nt, e), j(I, t), j(K, null), e.nodeType) {
      case 9:
      case 11:
        t = (t = e.documentElement) && (t = t.namespaceURI) ? Cm(t) : 0;
        break;
      default:
        if (t = e.tagName, e = e.namespaceURI)
          e = Cm(e), t = xm(e, t);
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
  function Nt() {
    U(K), U(I), U(nt);
  }
  function Gl(t) {
    t.memoizedState !== null && j(mt, t);
    var e = K.current, l = xm(e, t.type);
    e !== l && (j(I, t), j(K, l));
  }
  function hn(t) {
    I.current === t && (U(K), U(I)), mt.current === t && (U(mt), Ja._currentValue = H);
  }
  var aa, su;
  function Qe(t) {
    if (aa === void 0)
      try {
        throw Error();
      } catch (l) {
        var e = l.stack.trim().match(/\n( *(at )?)/);
        aa = e && e[1] || "", su = -1 < l.stack.indexOf(`
    at`) ? " (<anonymous>)" : -1 < l.stack.indexOf("@") ? "@unknown:0:0" : "";
      }
    return `
` + aa + t + su;
  }
  var lc = !1;
  function nc(t, e) {
    if (!t || lc) return "";
    lc = !0;
    var l = Error.prepareStackTrace;
    Error.prepareStackTrace = void 0;
    try {
      var n = {
        DetermineComponentFrameRoot: function() {
          try {
            if (e) {
              var N = function() {
                throw Error();
              };
              if (Object.defineProperty(N.prototype, "props", {
                set: function() {
                  throw Error();
                }
              }), typeof Reflect == "object" && Reflect.construct) {
                try {
                  Reflect.construct(N, []);
                } catch (M) {
                  var O = M;
                }
                Reflect.construct(t, [], N);
              } else {
                try {
                  N.call();
                } catch (M) {
                  O = M;
                }
                t.call(N.prototype);
              }
            } else {
              try {
                throw Error();
              } catch (M) {
                O = M;
              }
              (N = t()) && typeof N.catch == "function" && N.catch(function() {
              });
            }
          } catch (M) {
            if (M && O && typeof M.stack == "string")
              return [M.stack, O.stack];
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
                  var x = `
` + y[n].replace(" at new ", " at ");
                  return t.displayName && x.includes("<anonymous>") && (x = x.replace("<anonymous>", t.displayName)), x;
                }
              while (1 <= n && 0 <= a);
            break;
          }
      }
    } finally {
      lc = !1, Error.prepareStackTrace = l;
    }
    return (l = t ? t.displayName || t.name : "") ? Qe(l) : "";
  }
  function ty(t, e) {
    switch (t.tag) {
      case 26:
      case 27:
      case 5:
        return Qe(t.type);
      case 16:
        return Qe("Lazy");
      case 13:
        return t.child !== e && e !== null ? Qe("Suspense Fallback") : Qe("Suspense");
      case 19:
        return Qe("SuspenseList");
      case 0:
      case 15:
        return nc(t.type, !1);
      case 11:
        return nc(t.type.render, !1);
      case 1:
        return nc(t.type, !0);
      case 31:
        return Qe("Activity");
      default:
        return "";
    }
  }
  function is(t) {
    try {
      var e = "", l = null;
      do
        e += ty(t, l), l = t, t = t.return;
      while (t);
      return e;
    } catch (n) {
      return `
Error generating stack: ` + n.message + `
` + n.stack;
    }
  }
  var ac = Object.prototype.hasOwnProperty, uc = i.unstable_scheduleCallback, ic = i.unstable_cancelCallback, ey = i.unstable_shouldYield, ly = i.unstable_requestPaint, se = i.unstable_now, ny = i.unstable_getCurrentPriorityLevel, cs = i.unstable_ImmediatePriority, fs = i.unstable_UserBlockingPriority, ru = i.unstable_NormalPriority, ay = i.unstable_LowPriority, os = i.unstable_IdlePriority, uy = i.log, iy = i.unstable_setDisableYieldValue, ua = null, re = null;
  function rl(t) {
    if (typeof uy == "function" && iy(t), re && typeof re.setStrictMode == "function")
      try {
        re.setStrictMode(ua, t);
      } catch {
      }
  }
  var de = Math.clz32 ? Math.clz32 : oy, cy = Math.log, fy = Math.LN2;
  function oy(t) {
    return t >>>= 0, t === 0 ? 32 : 31 - (cy(t) / fy | 0) | 0;
  }
  var du = 256, mu = 262144, vu = 4194304;
  function Ql(t) {
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
    return d !== 0 ? (n = d & ~u, n !== 0 ? a = Ql(n) : (s &= d, s !== 0 ? a = Ql(s) : l || (l = d & ~t, l !== 0 && (a = Ql(l))))) : (d = n & ~u, d !== 0 ? a = Ql(d) : s !== 0 ? a = Ql(s) : l || (l = n & ~t, l !== 0 && (a = Ql(l)))), a === 0 ? 0 : e !== 0 && e !== a && (e & u) === 0 && (u = a & -a, l = e & -e, u >= l || u === 32 && (l & 4194048) !== 0) ? e : a;
  }
  function ia(t, e) {
    return (t.pendingLanes & ~(t.suspendedLanes & ~t.pingedLanes) & e) === 0;
  }
  function sy(t, e) {
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
  function ss() {
    var t = vu;
    return vu <<= 1, (vu & 62914560) === 0 && (vu = 4194304), t;
  }
  function cc(t) {
    for (var e = [], l = 0; 31 > l; l++) e.push(t);
    return e;
  }
  function ca(t, e) {
    t.pendingLanes |= e, e !== 268435456 && (t.suspendedLanes = 0, t.pingedLanes = 0, t.warmLanes = 0);
  }
  function ry(t, e, l, n, a, u) {
    var s = t.pendingLanes;
    t.pendingLanes = l, t.suspendedLanes = 0, t.pingedLanes = 0, t.warmLanes = 0, t.expiredLanes &= l, t.entangledLanes &= l, t.errorRecoveryDisabledLanes &= l, t.shellSuspendCounter = 0;
    var d = t.entanglements, y = t.expirationTimes, A = t.hiddenUpdates;
    for (l = s & ~l; 0 < l; ) {
      var x = 31 - de(l), N = 1 << x;
      d[x] = 0, y[x] = -1;
      var O = A[x];
      if (O !== null)
        for (A[x] = null, x = 0; x < O.length; x++) {
          var M = O[x];
          M !== null && (M.lane &= -536870913);
        }
      l &= ~N;
    }
    n !== 0 && rs(t, n, 0), u !== 0 && a === 0 && t.tag !== 0 && (t.suspendedLanes |= u & ~(s & ~e));
  }
  function rs(t, e, l) {
    t.pendingLanes |= e, t.suspendedLanes &= ~e;
    var n = 31 - de(e);
    t.entangledLanes |= e, t.entanglements[n] = t.entanglements[n] | 1073741824 | l & 261930;
  }
  function ds(t, e) {
    var l = t.entangledLanes |= e;
    for (t = t.entanglements; l; ) {
      var n = 31 - de(l), a = 1 << n;
      a & e | t[n] & e && (t[n] |= e), l &= ~a;
    }
  }
  function ms(t, e) {
    var l = e & -e;
    return l = (l & 42) !== 0 ? 1 : fc(l), (l & (t.suspendedLanes | e)) !== 0 ? 0 : l;
  }
  function fc(t) {
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
  function oc(t) {
    return t &= -t, 2 < t ? 8 < t ? (t & 134217727) !== 0 ? 32 : 268435456 : 8 : 2;
  }
  function vs() {
    var t = L.p;
    return t !== 0 ? t : (t = window.event, t === void 0 ? 32 : Pm(t.type));
  }
  function hs(t, e) {
    var l = L.p;
    try {
      return L.p = t, e();
    } finally {
      L.p = l;
    }
  }
  var dl = Math.random().toString(36).slice(2), Wt = "__reactFiber$" + dl, ne = "__reactProps$" + dl, yn = "__reactContainer$" + dl, sc = "__reactEvents$" + dl, dy = "__reactListeners$" + dl, my = "__reactHandles$" + dl, ys = "__reactResources$" + dl, fa = "__reactMarker$" + dl;
  function rc(t) {
    delete t[Wt], delete t[ne], delete t[sc], delete t[dy], delete t[my];
  }
  function gn(t) {
    var e = t[Wt];
    if (e) return e;
    for (var l = t.parentNode; l; ) {
      if (e = l[yn] || l[Wt]) {
        if (l = e.alternate, e.child !== null || l !== null && l.child !== null)
          for (t = Lm(t); t !== null; ) {
            if (l = t[Wt]) return l;
            t = Lm(t);
          }
        return e;
      }
      t = l, l = t.parentNode;
    }
    return null;
  }
  function bn(t) {
    if (t = t[Wt] || t[yn]) {
      var e = t.tag;
      if (e === 5 || e === 6 || e === 13 || e === 31 || e === 26 || e === 27 || e === 3)
        return t;
    }
    return null;
  }
  function oa(t) {
    var e = t.tag;
    if (e === 5 || e === 26 || e === 27 || e === 6) return t.stateNode;
    throw Error(f(33));
  }
  function Sn(t) {
    var e = t[ys];
    return e || (e = t[ys] = { hoistableStyles: /* @__PURE__ */ new Map(), hoistableScripts: /* @__PURE__ */ new Map() }), e;
  }
  function Zt(t) {
    t[fa] = !0;
  }
  var gs = /* @__PURE__ */ new Set(), bs = {};
  function Vl(t, e) {
    pn(t, e), pn(t + "Capture", e);
  }
  function pn(t, e) {
    for (bs[t] = e, t = 0; t < e.length; t++)
      gs.add(e[t]);
  }
  var vy = RegExp(
    "^[:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD][:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD\\-.0-9\\u00B7\\u0300-\\u036F\\u203F-\\u2040]*$"
  ), Ss = {}, ps = {};
  function hy(t) {
    return ac.call(ps, t) ? !0 : ac.call(Ss, t) ? !1 : vy.test(t) ? ps[t] = !0 : (Ss[t] = !0, !1);
  }
  function yu(t, e, l) {
    if (hy(e))
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
  function gu(t, e, l) {
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
  function Ve(t, e, l, n) {
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
  function Te(t) {
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
  function Es(t) {
    var e = t.type;
    return (t = t.nodeName) && t.toLowerCase() === "input" && (e === "checkbox" || e === "radio");
  }
  function yy(t, e, l) {
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
  function dc(t) {
    if (!t._valueTracker) {
      var e = Es(t) ? "checked" : "value";
      t._valueTracker = yy(
        t,
        e,
        "" + t[e]
      );
    }
  }
  function Ts(t) {
    if (!t) return !1;
    var e = t._valueTracker;
    if (!e) return !0;
    var l = e.getValue(), n = "";
    return t && (n = Es(t) ? t.checked ? "true" : "false" : t.value), t = n, t !== l ? (e.setValue(t), !0) : !1;
  }
  function bu(t) {
    if (t = t || (typeof document < "u" ? document : void 0), typeof t > "u") return null;
    try {
      return t.activeElement || t.body;
    } catch {
      return t.body;
    }
  }
  var gy = /[\n"\\]/g;
  function Ae(t) {
    return t.replace(
      gy,
      function(e) {
        return "\\" + e.charCodeAt(0).toString(16) + " ";
      }
    );
  }
  function mc(t, e, l, n, a, u, s, d) {
    t.name = "", s != null && typeof s != "function" && typeof s != "symbol" && typeof s != "boolean" ? t.type = s : t.removeAttribute("type"), e != null ? s === "number" ? (e === 0 && t.value === "" || t.value != e) && (t.value = "" + Te(e)) : t.value !== "" + Te(e) && (t.value = "" + Te(e)) : s !== "submit" && s !== "reset" || t.removeAttribute("value"), e != null ? vc(t, s, Te(e)) : l != null ? vc(t, s, Te(l)) : n != null && t.removeAttribute("value"), a == null && u != null && (t.defaultChecked = !!u), a != null && (t.checked = a && typeof a != "function" && typeof a != "symbol"), d != null && typeof d != "function" && typeof d != "symbol" && typeof d != "boolean" ? t.name = "" + Te(d) : t.removeAttribute("name");
  }
  function As(t, e, l, n, a, u, s, d) {
    if (u != null && typeof u != "function" && typeof u != "symbol" && typeof u != "boolean" && (t.type = u), e != null || l != null) {
      if (!(u !== "submit" && u !== "reset" || e != null)) {
        dc(t);
        return;
      }
      l = l != null ? "" + Te(l) : "", e = e != null ? "" + Te(e) : l, d || e === t.value || (t.value = e), t.defaultValue = e;
    }
    n = n ?? a, n = typeof n != "function" && typeof n != "symbol" && !!n, t.checked = d ? t.checked : !!n, t.defaultChecked = !!n, s != null && typeof s != "function" && typeof s != "symbol" && typeof s != "boolean" && (t.name = s), dc(t);
  }
  function vc(t, e, l) {
    e === "number" && bu(t.ownerDocument) === t || t.defaultValue === "" + l || (t.defaultValue = "" + l);
  }
  function En(t, e, l, n) {
    if (t = t.options, e) {
      e = {};
      for (var a = 0; a < l.length; a++)
        e["$" + l[a]] = !0;
      for (l = 0; l < t.length; l++)
        a = e.hasOwnProperty("$" + t[l].value), t[l].selected !== a && (t[l].selected = a), a && n && (t[l].defaultSelected = !0);
    } else {
      for (l = "" + Te(l), e = null, a = 0; a < t.length; a++) {
        if (t[a].value === l) {
          t[a].selected = !0, n && (t[a].defaultSelected = !0);
          return;
        }
        e !== null || t[a].disabled || (e = t[a]);
      }
      e !== null && (e.selected = !0);
    }
  }
  function Rs(t, e, l) {
    if (e != null && (e = "" + Te(e), e !== t.value && (t.value = e), l == null)) {
      t.defaultValue !== e && (t.defaultValue = e);
      return;
    }
    t.defaultValue = l != null ? "" + Te(l) : "";
  }
  function Os(t, e, l, n) {
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
    l = Te(e), t.defaultValue = l, n = t.textContent, n === l && n !== "" && n !== null && (t.value = n), dc(t);
  }
  function Tn(t, e) {
    if (e) {
      var l = t.firstChild;
      if (l && l === t.lastChild && l.nodeType === 3) {
        l.nodeValue = e;
        return;
      }
    }
    t.textContent = e;
  }
  var by = new Set(
    "animationIterationCount aspectRatio borderImageOutset borderImageSlice borderImageWidth boxFlex boxFlexGroup boxOrdinalGroup columnCount columns flex flexGrow flexPositive flexShrink flexNegative flexOrder gridArea gridRow gridRowEnd gridRowSpan gridRowStart gridColumn gridColumnEnd gridColumnSpan gridColumnStart fontWeight lineClamp lineHeight opacity order orphans scale tabSize widows zIndex zoom fillOpacity floodOpacity stopOpacity strokeDasharray strokeDashoffset strokeMiterlimit strokeOpacity strokeWidth MozAnimationIterationCount MozBoxFlex MozBoxFlexGroup MozLineClamp msAnimationIterationCount msFlex msZoom msFlexGrow msFlexNegative msFlexOrder msFlexPositive msFlexShrink msGridColumn msGridColumnSpan msGridRow msGridRowSpan WebkitAnimationIterationCount WebkitBoxFlex WebKitBoxFlexGroup WebkitBoxOrdinalGroup WebkitColumnCount WebkitColumns WebkitFlex WebkitFlexGrow WebkitFlexPositive WebkitFlexShrink WebkitLineClamp".split(
      " "
    )
  );
  function _s(t, e, l) {
    var n = e.indexOf("--") === 0;
    l == null || typeof l == "boolean" || l === "" ? n ? t.setProperty(e, "") : e === "float" ? t.cssFloat = "" : t[e] = "" : n ? t.setProperty(e, l) : typeof l != "number" || l === 0 || by.has(e) ? e === "float" ? t.cssFloat = l : t[e] = ("" + l).trim() : t[e] = l + "px";
  }
  function zs(t, e, l) {
    if (e != null && typeof e != "object")
      throw Error(f(62));
    if (t = t.style, l != null) {
      for (var n in l)
        !l.hasOwnProperty(n) || e != null && e.hasOwnProperty(n) || (n.indexOf("--") === 0 ? t.setProperty(n, "") : n === "float" ? t.cssFloat = "" : t[n] = "");
      for (var a in e)
        n = e[a], e.hasOwnProperty(a) && l[a] !== n && _s(t, a, n);
    } else
      for (var u in e)
        e.hasOwnProperty(u) && _s(t, u, e[u]);
  }
  function hc(t) {
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
  var Sy = /* @__PURE__ */ new Map([
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
  ]), py = /^[\u0000-\u001F ]*j[\r\n\t]*a[\r\n\t]*v[\r\n\t]*a[\r\n\t]*s[\r\n\t]*c[\r\n\t]*r[\r\n\t]*i[\r\n\t]*p[\r\n\t]*t[\r\n\t]*:/i;
  function Su(t) {
    return py.test("" + t) ? "javascript:throw new Error('React has blocked a javascript: URL as a security precaution.')" : t;
  }
  function Ze() {
  }
  var yc = null;
  function gc(t) {
    return t = t.target || t.srcElement || window, t.correspondingUseElement && (t = t.correspondingUseElement), t.nodeType === 3 ? t.parentNode : t;
  }
  var An = null, Rn = null;
  function Ms(t) {
    var e = bn(t);
    if (e && (t = e.stateNode)) {
      var l = t[ne] || null;
      t: switch (t = e.stateNode, e.type) {
        case "input":
          if (mc(
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
              'input[name="' + Ae(
                "" + e
              ) + '"][type="radio"]'
            ), e = 0; e < l.length; e++) {
              var n = l[e];
              if (n !== t && n.form === t.form) {
                var a = n[ne] || null;
                if (!a) throw Error(f(90));
                mc(
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
              n = l[e], n.form === t.form && Ts(n);
          }
          break t;
        case "textarea":
          Rs(t, l.value, l.defaultValue);
          break t;
        case "select":
          e = l.value, e != null && En(t, !!l.multiple, e, !1);
      }
    }
  }
  var bc = !1;
  function Cs(t, e, l) {
    if (bc) return t(e, l);
    bc = !0;
    try {
      var n = t(e);
      return n;
    } finally {
      if (bc = !1, (An !== null || Rn !== null) && (ii(), An && (e = An, t = Rn, Rn = An = null, Ms(e), t)))
        for (e = 0; e < t.length; e++) Ms(t[e]);
    }
  }
  function sa(t, e) {
    var l = t.stateNode;
    if (l === null) return null;
    var n = l[ne] || null;
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
  var Ke = !(typeof window > "u" || typeof window.document > "u" || typeof window.document.createElement > "u"), Sc = !1;
  if (Ke)
    try {
      var ra = {};
      Object.defineProperty(ra, "passive", {
        get: function() {
          Sc = !0;
        }
      }), window.addEventListener("test", ra, ra), window.removeEventListener("test", ra, ra);
    } catch {
      Sc = !1;
    }
  var ml = null, pc = null, pu = null;
  function xs() {
    if (pu) return pu;
    var t, e = pc, l = e.length, n, a = "value" in ml ? ml.value : ml.textContent, u = a.length;
    for (t = 0; t < l && e[t] === a[t]; t++) ;
    var s = l - t;
    for (n = 1; n <= s && e[l - n] === a[u - n]; n++) ;
    return pu = a.slice(t, 1 < n ? 1 - n : void 0);
  }
  function Eu(t) {
    var e = t.keyCode;
    return "charCode" in t ? (t = t.charCode, t === 0 && e === 13 && (t = 13)) : t = e, t === 10 && (t = 13), 32 <= t || t === 13 ? t : 0;
  }
  function Tu() {
    return !0;
  }
  function Ds() {
    return !1;
  }
  function ae(t) {
    function e(l, n, a, u, s) {
      this._reactName = l, this._targetInst = a, this.type = n, this.nativeEvent = u, this.target = s, this.currentTarget = null;
      for (var d in t)
        t.hasOwnProperty(d) && (l = t[d], this[d] = l ? l(u) : u[d]);
      return this.isDefaultPrevented = (u.defaultPrevented != null ? u.defaultPrevented : u.returnValue === !1) ? Tu : Ds, this.isPropagationStopped = Ds, this;
    }
    return _(e.prototype, {
      preventDefault: function() {
        this.defaultPrevented = !0;
        var l = this.nativeEvent;
        l && (l.preventDefault ? l.preventDefault() : typeof l.returnValue != "unknown" && (l.returnValue = !1), this.isDefaultPrevented = Tu);
      },
      stopPropagation: function() {
        var l = this.nativeEvent;
        l && (l.stopPropagation ? l.stopPropagation() : typeof l.cancelBubble != "unknown" && (l.cancelBubble = !0), this.isPropagationStopped = Tu);
      },
      persist: function() {
      },
      isPersistent: Tu
    }), e;
  }
  var Zl = {
    eventPhase: 0,
    bubbles: 0,
    cancelable: 0,
    timeStamp: function(t) {
      return t.timeStamp || Date.now();
    },
    defaultPrevented: 0,
    isTrusted: 0
  }, Au = ae(Zl), da = _({}, Zl, { view: 0, detail: 0 }), Ey = ae(da), Ec, Tc, ma, Ru = _({}, da, {
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
    getModifierState: Rc,
    button: 0,
    buttons: 0,
    relatedTarget: function(t) {
      return t.relatedTarget === void 0 ? t.fromElement === t.srcElement ? t.toElement : t.fromElement : t.relatedTarget;
    },
    movementX: function(t) {
      return "movementX" in t ? t.movementX : (t !== ma && (ma && t.type === "mousemove" ? (Ec = t.screenX - ma.screenX, Tc = t.screenY - ma.screenY) : Tc = Ec = 0, ma = t), Ec);
    },
    movementY: function(t) {
      return "movementY" in t ? t.movementY : Tc;
    }
  }), ws = ae(Ru), Ty = _({}, Ru, { dataTransfer: 0 }), Ay = ae(Ty), Ry = _({}, da, { relatedTarget: 0 }), Ac = ae(Ry), Oy = _({}, Zl, {
    animationName: 0,
    elapsedTime: 0,
    pseudoElement: 0
  }), _y = ae(Oy), zy = _({}, Zl, {
    clipboardData: function(t) {
      return "clipboardData" in t ? t.clipboardData : window.clipboardData;
    }
  }), My = ae(zy), Cy = _({}, Zl, { data: 0 }), Ns = ae(Cy), xy = {
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
  }, Dy = {
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
  }, wy = {
    Alt: "altKey",
    Control: "ctrlKey",
    Meta: "metaKey",
    Shift: "shiftKey"
  };
  function Ny(t) {
    var e = this.nativeEvent;
    return e.getModifierState ? e.getModifierState(t) : (t = wy[t]) ? !!e[t] : !1;
  }
  function Rc() {
    return Ny;
  }
  var Uy = _({}, da, {
    key: function(t) {
      if (t.key) {
        var e = xy[t.key] || t.key;
        if (e !== "Unidentified") return e;
      }
      return t.type === "keypress" ? (t = Eu(t), t === 13 ? "Enter" : String.fromCharCode(t)) : t.type === "keydown" || t.type === "keyup" ? Dy[t.keyCode] || "Unidentified" : "";
    },
    code: 0,
    location: 0,
    ctrlKey: 0,
    shiftKey: 0,
    altKey: 0,
    metaKey: 0,
    repeat: 0,
    locale: 0,
    getModifierState: Rc,
    charCode: function(t) {
      return t.type === "keypress" ? Eu(t) : 0;
    },
    keyCode: function(t) {
      return t.type === "keydown" || t.type === "keyup" ? t.keyCode : 0;
    },
    which: function(t) {
      return t.type === "keypress" ? Eu(t) : t.type === "keydown" || t.type === "keyup" ? t.keyCode : 0;
    }
  }), Hy = ae(Uy), By = _({}, Ru, {
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
  }), Us = ae(By), Ly = _({}, da, {
    touches: 0,
    targetTouches: 0,
    changedTouches: 0,
    altKey: 0,
    metaKey: 0,
    ctrlKey: 0,
    shiftKey: 0,
    getModifierState: Rc
  }), qy = ae(Ly), Yy = _({}, Zl, {
    propertyName: 0,
    elapsedTime: 0,
    pseudoElement: 0
  }), jy = ae(Yy), Xy = _({}, Ru, {
    deltaX: function(t) {
      return "deltaX" in t ? t.deltaX : "wheelDeltaX" in t ? -t.wheelDeltaX : 0;
    },
    deltaY: function(t) {
      return "deltaY" in t ? t.deltaY : "wheelDeltaY" in t ? -t.wheelDeltaY : "wheelDelta" in t ? -t.wheelDelta : 0;
    },
    deltaZ: 0,
    deltaMode: 0
  }), Gy = ae(Xy), Qy = _({}, Zl, {
    newState: 0,
    oldState: 0
  }), Vy = ae(Qy), Zy = [9, 13, 27, 32], Oc = Ke && "CompositionEvent" in window, va = null;
  Ke && "documentMode" in document && (va = document.documentMode);
  var Ky = Ke && "TextEvent" in window && !va, Hs = Ke && (!Oc || va && 8 < va && 11 >= va), Bs = " ", Ls = !1;
  function qs(t, e) {
    switch (t) {
      case "keyup":
        return Zy.indexOf(e.keyCode) !== -1;
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
  function Ys(t) {
    return t = t.detail, typeof t == "object" && "data" in t ? t.data : null;
  }
  var On = !1;
  function Jy(t, e) {
    switch (t) {
      case "compositionend":
        return Ys(e);
      case "keypress":
        return e.which !== 32 ? null : (Ls = !0, Bs);
      case "textInput":
        return t = e.data, t === Bs && Ls ? null : t;
      default:
        return null;
    }
  }
  function ky(t, e) {
    if (On)
      return t === "compositionend" || !Oc && qs(t, e) ? (t = xs(), pu = pc = ml = null, On = !1, t) : null;
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
        return Hs && e.locale !== "ko" ? null : e.data;
      default:
        return null;
    }
  }
  var Wy = {
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
  function js(t) {
    var e = t && t.nodeName && t.nodeName.toLowerCase();
    return e === "input" ? !!Wy[t.type] : e === "textarea";
  }
  function Xs(t, e, l, n) {
    An ? Rn ? Rn.push(n) : Rn = [n] : An = n, e = mi(e, "onChange"), 0 < e.length && (l = new Au(
      "onChange",
      "change",
      null,
      l,
      n
    ), t.push({ event: l, listeners: e }));
  }
  var ha = null, ya = null;
  function Fy(t) {
    Am(t, 0);
  }
  function Ou(t) {
    var e = oa(t);
    if (Ts(e)) return t;
  }
  function Gs(t, e) {
    if (t === "change") return e;
  }
  var Qs = !1;
  if (Ke) {
    var _c;
    if (Ke) {
      var zc = "oninput" in document;
      if (!zc) {
        var Vs = document.createElement("div");
        Vs.setAttribute("oninput", "return;"), zc = typeof Vs.oninput == "function";
      }
      _c = zc;
    } else _c = !1;
    Qs = _c && (!document.documentMode || 9 < document.documentMode);
  }
  function Zs() {
    ha && (ha.detachEvent("onpropertychange", Ks), ya = ha = null);
  }
  function Ks(t) {
    if (t.propertyName === "value" && Ou(ya)) {
      var e = [];
      Xs(
        e,
        ya,
        t,
        gc(t)
      ), Cs(Fy, e);
    }
  }
  function $y(t, e, l) {
    t === "focusin" ? (Zs(), ha = e, ya = l, ha.attachEvent("onpropertychange", Ks)) : t === "focusout" && Zs();
  }
  function Iy(t) {
    if (t === "selectionchange" || t === "keyup" || t === "keydown")
      return Ou(ya);
  }
  function Py(t, e) {
    if (t === "click") return Ou(e);
  }
  function t0(t, e) {
    if (t === "input" || t === "change")
      return Ou(e);
  }
  function e0(t, e) {
    return t === e && (t !== 0 || 1 / t === 1 / e) || t !== t && e !== e;
  }
  var me = typeof Object.is == "function" ? Object.is : e0;
  function ga(t, e) {
    if (me(t, e)) return !0;
    if (typeof t != "object" || t === null || typeof e != "object" || e === null)
      return !1;
    var l = Object.keys(t), n = Object.keys(e);
    if (l.length !== n.length) return !1;
    for (n = 0; n < l.length; n++) {
      var a = l[n];
      if (!ac.call(e, a) || !me(t[a], e[a]))
        return !1;
    }
    return !0;
  }
  function Js(t) {
    for (; t && t.firstChild; ) t = t.firstChild;
    return t;
  }
  function ks(t, e) {
    var l = Js(t);
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
      l = Js(l);
    }
  }
  function Ws(t, e) {
    return t && e ? t === e ? !0 : t && t.nodeType === 3 ? !1 : e && e.nodeType === 3 ? Ws(t, e.parentNode) : "contains" in t ? t.contains(e) : t.compareDocumentPosition ? !!(t.compareDocumentPosition(e) & 16) : !1 : !1;
  }
  function Fs(t) {
    t = t != null && t.ownerDocument != null && t.ownerDocument.defaultView != null ? t.ownerDocument.defaultView : window;
    for (var e = bu(t.document); e instanceof t.HTMLIFrameElement; ) {
      try {
        var l = typeof e.contentWindow.location.href == "string";
      } catch {
        l = !1;
      }
      if (l) t = e.contentWindow;
      else break;
      e = bu(t.document);
    }
    return e;
  }
  function Mc(t) {
    var e = t && t.nodeName && t.nodeName.toLowerCase();
    return e && (e === "input" && (t.type === "text" || t.type === "search" || t.type === "tel" || t.type === "url" || t.type === "password") || e === "textarea" || t.contentEditable === "true");
  }
  var l0 = Ke && "documentMode" in document && 11 >= document.documentMode, _n = null, Cc = null, ba = null, xc = !1;
  function $s(t, e, l) {
    var n = l.window === l ? l.document : l.nodeType === 9 ? l : l.ownerDocument;
    xc || _n == null || _n !== bu(n) || (n = _n, "selectionStart" in n && Mc(n) ? n = { start: n.selectionStart, end: n.selectionEnd } : (n = (n.ownerDocument && n.ownerDocument.defaultView || window).getSelection(), n = {
      anchorNode: n.anchorNode,
      anchorOffset: n.anchorOffset,
      focusNode: n.focusNode,
      focusOffset: n.focusOffset
    }), ba && ga(ba, n) || (ba = n, n = mi(Cc, "onSelect"), 0 < n.length && (e = new Au(
      "onSelect",
      "select",
      null,
      e,
      l
    ), t.push({ event: e, listeners: n }), e.target = _n)));
  }
  function Kl(t, e) {
    var l = {};
    return l[t.toLowerCase()] = e.toLowerCase(), l["Webkit" + t] = "webkit" + e, l["Moz" + t] = "moz" + e, l;
  }
  var zn = {
    animationend: Kl("Animation", "AnimationEnd"),
    animationiteration: Kl("Animation", "AnimationIteration"),
    animationstart: Kl("Animation", "AnimationStart"),
    transitionrun: Kl("Transition", "TransitionRun"),
    transitionstart: Kl("Transition", "TransitionStart"),
    transitioncancel: Kl("Transition", "TransitionCancel"),
    transitionend: Kl("Transition", "TransitionEnd")
  }, Dc = {}, Is = {};
  Ke && (Is = document.createElement("div").style, "AnimationEvent" in window || (delete zn.animationend.animation, delete zn.animationiteration.animation, delete zn.animationstart.animation), "TransitionEvent" in window || delete zn.transitionend.transition);
  function Jl(t) {
    if (Dc[t]) return Dc[t];
    if (!zn[t]) return t;
    var e = zn[t], l;
    for (l in e)
      if (e.hasOwnProperty(l) && l in Is)
        return Dc[t] = e[l];
    return t;
  }
  var Ps = Jl("animationend"), tr = Jl("animationiteration"), er = Jl("animationstart"), n0 = Jl("transitionrun"), a0 = Jl("transitionstart"), u0 = Jl("transitioncancel"), lr = Jl("transitionend"), nr = /* @__PURE__ */ new Map(), wc = "abort auxClick beforeToggle cancel canPlay canPlayThrough click close contextMenu copy cut drag dragEnd dragEnter dragExit dragLeave dragOver dragStart drop durationChange emptied encrypted ended error gotPointerCapture input invalid keyDown keyPress keyUp load loadedData loadedMetadata loadStart lostPointerCapture mouseDown mouseMove mouseOut mouseOver mouseUp paste pause play playing pointerCancel pointerDown pointerMove pointerOut pointerOver pointerUp progress rateChange reset resize seeked seeking stalled submit suspend timeUpdate touchCancel touchEnd touchStart volumeChange scroll toggle touchMove waiting wheel".split(
    " "
  );
  wc.push("scrollEnd");
  function we(t, e) {
    nr.set(t, e), Vl(e, [t]);
  }
  var _u = typeof reportError == "function" ? reportError : function(t) {
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
  }, Re = [], Mn = 0, Nc = 0;
  function zu() {
    for (var t = Mn, e = Nc = Mn = 0; e < t; ) {
      var l = Re[e];
      Re[e++] = null;
      var n = Re[e];
      Re[e++] = null;
      var a = Re[e];
      Re[e++] = null;
      var u = Re[e];
      if (Re[e++] = null, n !== null && a !== null) {
        var s = n.pending;
        s === null ? a.next = a : (a.next = s.next, s.next = a), n.pending = a;
      }
      u !== 0 && ar(l, a, u);
    }
  }
  function Mu(t, e, l, n) {
    Re[Mn++] = t, Re[Mn++] = e, Re[Mn++] = l, Re[Mn++] = n, Nc |= n, t.lanes |= n, t = t.alternate, t !== null && (t.lanes |= n);
  }
  function Uc(t, e, l, n) {
    return Mu(t, e, l, n), Cu(t);
  }
  function kl(t, e) {
    return Mu(t, null, null, e), Cu(t);
  }
  function ar(t, e, l) {
    t.lanes |= l;
    var n = t.alternate;
    n !== null && (n.lanes |= l);
    for (var a = !1, u = t.return; u !== null; )
      u.childLanes |= l, n = u.alternate, n !== null && (n.childLanes |= l), u.tag === 22 && (t = u.stateNode, t === null || t._visibility & 1 || (a = !0)), t = u, u = u.return;
    return t.tag === 3 ? (u = t.stateNode, a && e !== null && (a = 31 - de(l), t = u.hiddenUpdates, n = t[a], n === null ? t[a] = [e] : n.push(e), e.lane = l | 536870912), u) : null;
  }
  function Cu(t) {
    if (50 < ja)
      throw ja = 0, Vf = null, Error(f(185));
    for (var e = t.return; e !== null; )
      t = e, e = t.return;
    return t.tag === 3 ? t.stateNode : null;
  }
  var Cn = {};
  function i0(t, e, l, n) {
    this.tag = t, this.key = l, this.sibling = this.child = this.return = this.stateNode = this.type = this.elementType = null, this.index = 0, this.refCleanup = this.ref = null, this.pendingProps = e, this.dependencies = this.memoizedState = this.updateQueue = this.memoizedProps = null, this.mode = n, this.subtreeFlags = this.flags = 0, this.deletions = null, this.childLanes = this.lanes = 0, this.alternate = null;
  }
  function ve(t, e, l, n) {
    return new i0(t, e, l, n);
  }
  function Hc(t) {
    return t = t.prototype, !(!t || !t.isReactComponent);
  }
  function Je(t, e) {
    var l = t.alternate;
    return l === null ? (l = ve(
      t.tag,
      e,
      t.key,
      t.mode
    ), l.elementType = t.elementType, l.type = t.type, l.stateNode = t.stateNode, l.alternate = t, t.alternate = l) : (l.pendingProps = e, l.type = t.type, l.flags = 0, l.subtreeFlags = 0, l.deletions = null), l.flags = t.flags & 65011712, l.childLanes = t.childLanes, l.lanes = t.lanes, l.child = t.child, l.memoizedProps = t.memoizedProps, l.memoizedState = t.memoizedState, l.updateQueue = t.updateQueue, e = t.dependencies, l.dependencies = e === null ? null : { lanes: e.lanes, firstContext: e.firstContext }, l.sibling = t.sibling, l.index = t.index, l.ref = t.ref, l.refCleanup = t.refCleanup, l;
  }
  function ur(t, e) {
    t.flags &= 65011714;
    var l = t.alternate;
    return l === null ? (t.childLanes = 0, t.lanes = e, t.child = null, t.subtreeFlags = 0, t.memoizedProps = null, t.memoizedState = null, t.updateQueue = null, t.dependencies = null, t.stateNode = null) : (t.childLanes = l.childLanes, t.lanes = l.lanes, t.child = l.child, t.subtreeFlags = 0, t.deletions = null, t.memoizedProps = l.memoizedProps, t.memoizedState = l.memoizedState, t.updateQueue = l.updateQueue, t.type = l.type, e = l.dependencies, t.dependencies = e === null ? null : {
      lanes: e.lanes,
      firstContext: e.firstContext
    }), t;
  }
  function xu(t, e, l, n, a, u) {
    var s = 0;
    if (n = t, typeof t == "function") Hc(t) && (s = 1);
    else if (typeof t == "string")
      s = rg(
        t,
        l,
        K.current
      ) ? 26 : t === "html" || t === "head" || t === "body" ? 27 : 5;
    else
      t: switch (t) {
        case _t:
          return t = ve(31, l, e, a), t.elementType = _t, t.lanes = u, t;
        case q:
          return Wl(l.children, a, u, e);
        case V:
          s = 8, a |= 24;
          break;
        case X:
          return t = ve(12, l, e, a | 2), t.elementType = X, t.lanes = u, t;
        case at:
          return t = ve(13, l, e, a), t.elementType = at, t.lanes = u, t;
        case lt:
          return t = ve(19, l, e, a), t.elementType = lt, t.lanes = u, t;
        default:
          if (typeof t == "object" && t !== null)
            switch (t.$$typeof) {
              case G:
                s = 10;
                break t;
              case k:
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
    return e = ve(s, l, e, a), e.elementType = t, e.type = n, e.lanes = u, e;
  }
  function Wl(t, e, l, n) {
    return t = ve(7, t, n, e), t.lanes = l, t;
  }
  function Bc(t, e, l) {
    return t = ve(6, t, null, e), t.lanes = l, t;
  }
  function ir(t) {
    var e = ve(18, null, null, 0);
    return e.stateNode = t, e;
  }
  function Lc(t, e, l) {
    return e = ve(
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
  var cr = /* @__PURE__ */ new WeakMap();
  function Oe(t, e) {
    if (typeof t == "object" && t !== null) {
      var l = cr.get(t);
      return l !== void 0 ? l : (e = {
        value: t,
        source: e,
        stack: is(e)
      }, cr.set(t, e), e);
    }
    return {
      value: t,
      source: e,
      stack: is(e)
    };
  }
  var xn = [], Dn = 0, Du = null, Sa = 0, _e = [], ze = 0, vl = null, He = 1, Be = "";
  function ke(t, e) {
    xn[Dn++] = Sa, xn[Dn++] = Du, Du = t, Sa = e;
  }
  function fr(t, e, l) {
    _e[ze++] = He, _e[ze++] = Be, _e[ze++] = vl, vl = t;
    var n = He;
    t = Be;
    var a = 32 - de(n) - 1;
    n &= ~(1 << a), l += 1;
    var u = 32 - de(e) + a;
    if (30 < u) {
      var s = a - a % 5;
      u = (n & (1 << s) - 1).toString(32), n >>= s, a -= s, He = 1 << 32 - de(e) + a | l << a | n, Be = u + t;
    } else
      He = 1 << u | l << a | n, Be = t;
  }
  function qc(t) {
    t.return !== null && (ke(t, 1), fr(t, 1, 0));
  }
  function Yc(t) {
    for (; t === Du; )
      Du = xn[--Dn], xn[Dn] = null, Sa = xn[--Dn], xn[Dn] = null;
    for (; t === vl; )
      vl = _e[--ze], _e[ze] = null, Be = _e[--ze], _e[ze] = null, He = _e[--ze], _e[ze] = null;
  }
  function or(t, e) {
    _e[ze++] = He, _e[ze++] = Be, _e[ze++] = vl, He = e.id, Be = e.overflow, vl = t;
  }
  var Ft = null, xt = null, yt = !1, hl = null, Me = !1, jc = Error(f(519));
  function yl(t) {
    var e = Error(
      f(
        418,
        1 < arguments.length && arguments[1] !== void 0 && arguments[1] ? "text" : "HTML",
        ""
      )
    );
    throw pa(Oe(e, t)), jc;
  }
  function sr(t) {
    var e = t.stateNode, l = t.type, n = t.memoizedProps;
    switch (e[Wt] = t, e[ne] = n, l) {
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
        for (l = 0; l < Ga.length; l++)
          rt(Ga[l], e);
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
        rt("invalid", e), As(
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
        rt("invalid", e), Os(e, n.value, n.defaultValue, n.children);
    }
    l = n.children, typeof l != "string" && typeof l != "number" && typeof l != "bigint" || e.textContent === "" + l || n.suppressHydrationWarning === !0 || zm(e.textContent, l) ? (n.popover != null && (rt("beforetoggle", e), rt("toggle", e)), n.onScroll != null && rt("scroll", e), n.onScrollEnd != null && rt("scrollend", e), n.onClick != null && (e.onclick = Ze), e = !0) : e = !1, e || yl(t, !0);
  }
  function rr(t) {
    for (Ft = t.return; Ft; )
      switch (Ft.tag) {
        case 5:
        case 31:
        case 13:
          Me = !1;
          return;
        case 27:
        case 3:
          Me = !0;
          return;
        default:
          Ft = Ft.return;
      }
  }
  function wn(t) {
    if (t !== Ft) return !1;
    if (!yt) return rr(t), yt = !0, !1;
    var e = t.tag, l;
    if ((l = e !== 3 && e !== 27) && ((l = e === 5) && (l = t.type, l = !(l !== "form" && l !== "button") || uo(t.type, t.memoizedProps)), l = !l), l && xt && yl(t), rr(t), e === 13) {
      if (t = t.memoizedState, t = t !== null ? t.dehydrated : null, !t) throw Error(f(317));
      xt = Bm(t);
    } else if (e === 31) {
      if (t = t.memoizedState, t = t !== null ? t.dehydrated : null, !t) throw Error(f(317));
      xt = Bm(t);
    } else
      e === 27 ? (e = xt, xl(t.type) ? (t = so, so = null, xt = t) : xt = e) : xt = Ft ? xe(t.stateNode.nextSibling) : null;
    return !0;
  }
  function Fl() {
    xt = Ft = null, yt = !1;
  }
  function Xc() {
    var t = hl;
    return t !== null && (fe === null ? fe = t : fe.push.apply(
      fe,
      t
    ), hl = null), t;
  }
  function pa(t) {
    hl === null ? hl = [t] : hl.push(t);
  }
  var Gc = b(null), $l = null, We = null;
  function gl(t, e, l) {
    j(Gc, e._currentValue), e._currentValue = l;
  }
  function Fe(t) {
    t._currentValue = Gc.current, U(Gc);
  }
  function Qc(t, e, l) {
    for (; t !== null; ) {
      var n = t.alternate;
      if ((t.childLanes & e) !== e ? (t.childLanes |= e, n !== null && (n.childLanes |= e)) : n !== null && (n.childLanes & e) !== e && (n.childLanes |= e), t === l) break;
      t = t.return;
    }
  }
  function Vc(t, e, l, n) {
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
              u.lanes |= l, d = u.alternate, d !== null && (d.lanes |= l), Qc(
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
        s.lanes |= l, u = s.alternate, u !== null && (u.lanes |= l), Qc(s, l, t), s = null;
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
          me(a.pendingProps.value, s.value) || (t !== null ? t.push(d) : t = [d]);
        }
      } else if (a === mt.current) {
        if (s = a.alternate, s === null) throw Error(f(387));
        s.memoizedState.memoizedState !== a.memoizedState.memoizedState && (t !== null ? t.push(Ja) : t = [Ja]);
      }
      a = a.return;
    }
    t !== null && Vc(
      e,
      t,
      l,
      n
    ), e.flags |= 262144;
  }
  function wu(t) {
    for (t = t.firstContext; t !== null; ) {
      if (!me(
        t.context._currentValue,
        t.memoizedValue
      ))
        return !0;
      t = t.next;
    }
    return !1;
  }
  function Il(t) {
    $l = t, We = null, t = t.dependencies, t !== null && (t.firstContext = null);
  }
  function $t(t) {
    return dr($l, t);
  }
  function Nu(t, e) {
    return $l === null && Il(t), dr(t, e);
  }
  function dr(t, e) {
    var l = e._currentValue;
    if (e = { context: e, memoizedValue: l, next: null }, We === null) {
      if (t === null) throw Error(f(308));
      We = e, t.dependencies = { lanes: 0, firstContext: e }, t.flags |= 524288;
    } else We = We.next = e;
    return l;
  }
  var c0 = typeof AbortController < "u" ? AbortController : function() {
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
  }, f0 = i.unstable_scheduleCallback, o0 = i.unstable_NormalPriority, Yt = {
    $$typeof: G,
    Consumer: null,
    Provider: null,
    _currentValue: null,
    _currentValue2: null,
    _threadCount: 0
  };
  function Zc() {
    return {
      controller: new c0(),
      data: /* @__PURE__ */ new Map(),
      refCount: 0
    };
  }
  function Ea(t) {
    t.refCount--, t.refCount === 0 && f0(o0, function() {
      t.controller.abort();
    });
  }
  var Ta = null, Kc = 0, Un = 0, Hn = null;
  function s0(t, e) {
    if (Ta === null) {
      var l = Ta = [];
      Kc = 0, Un = Ff(), Hn = {
        status: "pending",
        value: void 0,
        then: function(n) {
          l.push(n);
        }
      };
    }
    return Kc++, e.then(mr, mr), e;
  }
  function mr() {
    if (--Kc === 0 && Ta !== null) {
      Hn !== null && (Hn.status = "fulfilled");
      var t = Ta;
      Ta = null, Un = 0, Hn = null;
      for (var e = 0; e < t.length; e++) (0, t[e])();
    }
  }
  function r0(t, e) {
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
  var vr = R.S;
  R.S = function(t, e) {
    $d = se(), typeof e == "object" && e !== null && typeof e.then == "function" && s0(t, e), vr !== null && vr(t, e);
  };
  var Pl = b(null);
  function Jc() {
    var t = Pl.current;
    return t !== null ? t : Mt.pooledCache;
  }
  function Uu(t, e) {
    e === null ? j(Pl, Pl.current) : j(Pl, e.pool);
  }
  function hr() {
    var t = Jc();
    return t === null ? null : { parent: Yt._currentValue, pool: t };
  }
  var Bn = Error(f(460)), kc = Error(f(474)), Hu = Error(f(542)), Bu = { then: function() {
  } };
  function yr(t) {
    return t = t.status, t === "fulfilled" || t === "rejected";
  }
  function gr(t, e, l) {
    switch (l = t[l], l === void 0 ? t.push(e) : l !== e && (e.then(Ze, Ze), e = l), e.status) {
      case "fulfilled":
        return e.value;
      case "rejected":
        throw t = e.reason, Sr(t), t;
      default:
        if (typeof e.status == "string") e.then(Ze, Ze);
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
            throw t = e.reason, Sr(t), t;
        }
        throw en = e, Bn;
    }
  }
  function tn(t) {
    try {
      var e = t._init;
      return e(t._payload);
    } catch (l) {
      throw l !== null && typeof l == "object" && typeof l.then == "function" ? (en = l, Bn) : l;
    }
  }
  var en = null;
  function br() {
    if (en === null) throw Error(f(459));
    var t = en;
    return en = null, t;
  }
  function Sr(t) {
    if (t === Bn || t === Hu)
      throw Error(f(483));
  }
  var Ln = null, Aa = 0;
  function Lu(t) {
    var e = Aa;
    return Aa += 1, Ln === null && (Ln = []), gr(Ln, t, e);
  }
  function Ra(t, e) {
    e = e.props.ref, t.ref = e !== void 0 ? e : null;
  }
  function qu(t, e) {
    throw e.$$typeof === w ? Error(f(525)) : (t = Object.prototype.toString.call(e), Error(
      f(
        31,
        t === "[object Object]" ? "object with keys {" + Object.keys(e).join(", ") + "}" : t
      )
    ));
  }
  function pr(t) {
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
      return E = Je(E, S), E.index = 0, E.sibling = null, E;
    }
    function u(E, S, T) {
      return E.index = T, t ? (T = E.alternate, T !== null ? (T = T.index, T < S ? (E.flags |= 67108866, S) : T) : (E.flags |= 67108866, S)) : (E.flags |= 1048576, S);
    }
    function s(E) {
      return t && E.alternate === null && (E.flags |= 67108866), E;
    }
    function d(E, S, T, D) {
      return S === null || S.tag !== 6 ? (S = Bc(T, E.mode, D), S.return = E, S) : (S = a(S, T), S.return = E, S);
    }
    function y(E, S, T, D) {
      var P = T.type;
      return P === q ? x(
        E,
        S,
        T.props.children,
        D,
        T.key
      ) : S !== null && (S.elementType === P || typeof P == "object" && P !== null && P.$$typeof === it && tn(P) === S.type) ? (S = a(S, T.props), Ra(S, T), S.return = E, S) : (S = xu(
        T.type,
        T.key,
        T.props,
        null,
        E.mode,
        D
      ), Ra(S, T), S.return = E, S);
    }
    function A(E, S, T, D) {
      return S === null || S.tag !== 4 || S.stateNode.containerInfo !== T.containerInfo || S.stateNode.implementation !== T.implementation ? (S = Lc(T, E.mode, D), S.return = E, S) : (S = a(S, T.children || []), S.return = E, S);
    }
    function x(E, S, T, D, P) {
      return S === null || S.tag !== 7 ? (S = Wl(
        T,
        E.mode,
        D,
        P
      ), S.return = E, S) : (S = a(S, T), S.return = E, S);
    }
    function N(E, S, T) {
      if (typeof S == "string" && S !== "" || typeof S == "number" || typeof S == "bigint")
        return S = Bc(
          "" + S,
          E.mode,
          T
        ), S.return = E, S;
      if (typeof S == "object" && S !== null) {
        switch (S.$$typeof) {
          case C:
            return T = xu(
              S.type,
              S.key,
              S.props,
              null,
              E.mode,
              T
            ), Ra(T, S), T.return = E, T;
          case Y:
            return S = Lc(
              S,
              E.mode,
              T
            ), S.return = E, S;
          case it:
            return S = tn(S), N(E, S, T);
        }
        if (ft(S) || dt(S))
          return S = Wl(
            S,
            E.mode,
            T,
            null
          ), S.return = E, S;
        if (typeof S.then == "function")
          return N(E, Lu(S), T);
        if (S.$$typeof === G)
          return N(
            E,
            Nu(E, S),
            T
          );
        qu(E, S);
      }
      return null;
    }
    function O(E, S, T, D) {
      var P = S !== null ? S.key : null;
      if (typeof T == "string" && T !== "" || typeof T == "number" || typeof T == "bigint")
        return P !== null ? null : d(E, S, "" + T, D);
      if (typeof T == "object" && T !== null) {
        switch (T.$$typeof) {
          case C:
            return T.key === P ? y(E, S, T, D) : null;
          case Y:
            return T.key === P ? A(E, S, T, D) : null;
          case it:
            return T = tn(T), O(E, S, T, D);
        }
        if (ft(T) || dt(T))
          return P !== null ? null : x(E, S, T, D, null);
        if (typeof T.then == "function")
          return O(
            E,
            S,
            Lu(T),
            D
          );
        if (T.$$typeof === G)
          return O(
            E,
            S,
            Nu(E, T),
            D
          );
        qu(E, T);
      }
      return null;
    }
    function M(E, S, T, D, P) {
      if (typeof D == "string" && D !== "" || typeof D == "number" || typeof D == "bigint")
        return E = E.get(T) || null, d(S, E, "" + D, P);
      if (typeof D == "object" && D !== null) {
        switch (D.$$typeof) {
          case C:
            return E = E.get(
              D.key === null ? T : D.key
            ) || null, y(S, E, D, P);
          case Y:
            return E = E.get(
              D.key === null ? T : D.key
            ) || null, A(S, E, D, P);
          case it:
            return D = tn(D), M(
              E,
              S,
              T,
              D,
              P
            );
        }
        if (ft(D) || dt(D))
          return E = E.get(T) || null, x(S, E, D, P, null);
        if (typeof D.then == "function")
          return M(
            E,
            S,
            T,
            Lu(D),
            P
          );
        if (D.$$typeof === G)
          return M(
            E,
            S,
            T,
            Nu(S, D),
            P
          );
        qu(S, D);
      }
      return null;
    }
    function J(E, S, T, D) {
      for (var P = null, gt = null, W = S, ot = S = 0, ht = null; W !== null && ot < T.length; ot++) {
        W.index > ot ? (ht = W, W = null) : ht = W.sibling;
        var bt = O(
          E,
          W,
          T[ot],
          D
        );
        if (bt === null) {
          W === null && (W = ht);
          break;
        }
        t && W && bt.alternate === null && e(E, W), S = u(bt, S, ot), gt === null ? P = bt : gt.sibling = bt, gt = bt, W = ht;
      }
      if (ot === T.length)
        return l(E, W), yt && ke(E, ot), P;
      if (W === null) {
        for (; ot < T.length; ot++)
          W = N(E, T[ot], D), W !== null && (S = u(
            W,
            S,
            ot
          ), gt === null ? P = W : gt.sibling = W, gt = W);
        return yt && ke(E, ot), P;
      }
      for (W = n(W); ot < T.length; ot++)
        ht = M(
          W,
          E,
          ot,
          T[ot],
          D
        ), ht !== null && (t && ht.alternate !== null && W.delete(
          ht.key === null ? ot : ht.key
        ), S = u(
          ht,
          S,
          ot
        ), gt === null ? P = ht : gt.sibling = ht, gt = ht);
      return t && W.forEach(function(Hl) {
        return e(E, Hl);
      }), yt && ke(E, ot), P;
    }
    function tt(E, S, T, D) {
      if (T == null) throw Error(f(151));
      for (var P = null, gt = null, W = S, ot = S = 0, ht = null, bt = T.next(); W !== null && !bt.done; ot++, bt = T.next()) {
        W.index > ot ? (ht = W, W = null) : ht = W.sibling;
        var Hl = O(E, W, bt.value, D);
        if (Hl === null) {
          W === null && (W = ht);
          break;
        }
        t && W && Hl.alternate === null && e(E, W), S = u(Hl, S, ot), gt === null ? P = Hl : gt.sibling = Hl, gt = Hl, W = ht;
      }
      if (bt.done)
        return l(E, W), yt && ke(E, ot), P;
      if (W === null) {
        for (; !bt.done; ot++, bt = T.next())
          bt = N(E, bt.value, D), bt !== null && (S = u(bt, S, ot), gt === null ? P = bt : gt.sibling = bt, gt = bt);
        return yt && ke(E, ot), P;
      }
      for (W = n(W); !bt.done; ot++, bt = T.next())
        bt = M(W, E, ot, bt.value, D), bt !== null && (t && bt.alternate !== null && W.delete(bt.key === null ? ot : bt.key), S = u(bt, S, ot), gt === null ? P = bt : gt.sibling = bt, gt = bt);
      return t && W.forEach(function(Tg) {
        return e(E, Tg);
      }), yt && ke(E, ot), P;
    }
    function Ot(E, S, T, D) {
      if (typeof T == "object" && T !== null && T.type === q && T.key === null && (T = T.props.children), typeof T == "object" && T !== null) {
        switch (T.$$typeof) {
          case C:
            t: {
              for (var P = T.key; S !== null; ) {
                if (S.key === P) {
                  if (P = T.type, P === q) {
                    if (S.tag === 7) {
                      l(
                        E,
                        S.sibling
                      ), D = a(
                        S,
                        T.props.children
                      ), D.return = E, E = D;
                      break t;
                    }
                  } else if (S.elementType === P || typeof P == "object" && P !== null && P.$$typeof === it && tn(P) === S.type) {
                    l(
                      E,
                      S.sibling
                    ), D = a(S, T.props), Ra(D, T), D.return = E, E = D;
                    break t;
                  }
                  l(E, S);
                  break;
                } else e(E, S);
                S = S.sibling;
              }
              T.type === q ? (D = Wl(
                T.props.children,
                E.mode,
                D,
                T.key
              ), D.return = E, E = D) : (D = xu(
                T.type,
                T.key,
                T.props,
                null,
                E.mode,
                D
              ), Ra(D, T), D.return = E, E = D);
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
                    ), D = a(S, T.children || []), D.return = E, E = D;
                    break t;
                  } else {
                    l(E, S);
                    break;
                  }
                else e(E, S);
                S = S.sibling;
              }
              D = Lc(T, E.mode, D), D.return = E, E = D;
            }
            return s(E);
          case it:
            return T = tn(T), Ot(
              E,
              S,
              T,
              D
            );
        }
        if (ft(T))
          return J(
            E,
            S,
            T,
            D
          );
        if (dt(T)) {
          if (P = dt(T), typeof P != "function") throw Error(f(150));
          return T = P.call(T), tt(
            E,
            S,
            T,
            D
          );
        }
        if (typeof T.then == "function")
          return Ot(
            E,
            S,
            Lu(T),
            D
          );
        if (T.$$typeof === G)
          return Ot(
            E,
            S,
            Nu(E, T),
            D
          );
        qu(E, T);
      }
      return typeof T == "string" && T !== "" || typeof T == "number" || typeof T == "bigint" ? (T = "" + T, S !== null && S.tag === 6 ? (l(E, S.sibling), D = a(S, T), D.return = E, E = D) : (l(E, S), D = Bc(T, E.mode, D), D.return = E, E = D), s(E)) : l(E, S);
    }
    return function(E, S, T, D) {
      try {
        Aa = 0;
        var P = Ot(
          E,
          S,
          T,
          D
        );
        return Ln = null, P;
      } catch (W) {
        if (W === Bn || W === Hu) throw W;
        var gt = ve(29, W, null, E.mode);
        return gt.lanes = D, gt.return = E, gt;
      } finally {
      }
    };
  }
  var ln = pr(!0), Er = pr(!1), bl = !1;
  function Wc(t) {
    t.updateQueue = {
      baseState: t.memoizedState,
      firstBaseUpdate: null,
      lastBaseUpdate: null,
      shared: { pending: null, lanes: 0, hiddenCallbacks: null },
      callbacks: null
    };
  }
  function Fc(t, e) {
    t = t.updateQueue, e.updateQueue === t && (e.updateQueue = {
      baseState: t.baseState,
      firstBaseUpdate: t.firstBaseUpdate,
      lastBaseUpdate: t.lastBaseUpdate,
      shared: t.shared,
      callbacks: null
    });
  }
  function Sl(t) {
    return { lane: t, tag: 0, payload: null, callback: null, next: null };
  }
  function pl(t, e, l) {
    var n = t.updateQueue;
    if (n === null) return null;
    if (n = n.shared, (St & 2) !== 0) {
      var a = n.pending;
      return a === null ? e.next = e : (e.next = a.next, a.next = e), n.pending = e, e = Cu(t), ar(t, null, l), e;
    }
    return Mu(t, n, e, l), Cu(t);
  }
  function Oa(t, e, l) {
    if (e = e.updateQueue, e !== null && (e = e.shared, (l & 4194048) !== 0)) {
      var n = e.lanes;
      n &= t.pendingLanes, l |= n, e.lanes = l, ds(t, l);
    }
  }
  function $c(t, e) {
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
  var Ic = !1;
  function _a() {
    if (Ic) {
      var t = Hn;
      if (t !== null) throw t;
    }
  }
  function za(t, e, l, n) {
    Ic = !1;
    var a = t.updateQueue;
    bl = !1;
    var u = a.firstBaseUpdate, s = a.lastBaseUpdate, d = a.shared.pending;
    if (d !== null) {
      a.shared.pending = null;
      var y = d, A = y.next;
      y.next = null, s === null ? u = A : s.next = A, s = y;
      var x = t.alternate;
      x !== null && (x = x.updateQueue, d = x.lastBaseUpdate, d !== s && (d === null ? x.firstBaseUpdate = A : d.next = A, x.lastBaseUpdate = y));
    }
    if (u !== null) {
      var N = a.baseState;
      s = 0, x = A = y = null, d = u;
      do {
        var O = d.lane & -536870913, M = O !== d.lane;
        if (M ? (vt & O) === O : (n & O) === O) {
          O !== 0 && O === Un && (Ic = !0), x !== null && (x = x.next = {
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
                  N = J.call(Ot, N, O);
                  break t;
                }
                N = J;
                break t;
              case 3:
                J.flags = J.flags & -65537 | 128;
              case 0:
                if (J = tt.payload, O = typeof J == "function" ? J.call(Ot, N, O) : J, O == null) break t;
                N = _({}, N, O);
                break t;
              case 2:
                bl = !0;
            }
          }
          O = d.callback, O !== null && (t.flags |= 64, M && (t.flags |= 8192), M = a.callbacks, M === null ? a.callbacks = [O] : M.push(O));
        } else
          M = {
            lane: O,
            tag: d.tag,
            payload: d.payload,
            callback: d.callback,
            next: null
          }, x === null ? (A = x = M, y = N) : x = x.next = M, s |= O;
        if (d = d.next, d === null) {
          if (d = a.shared.pending, d === null)
            break;
          M = d, d = M.next, M.next = null, a.lastBaseUpdate = M, a.shared.pending = null;
        }
      } while (!0);
      x === null && (y = N), a.baseState = y, a.firstBaseUpdate = A, a.lastBaseUpdate = x, u === null && (a.shared.lanes = 0), Ol |= s, t.lanes = s, t.memoizedState = N;
    }
  }
  function Tr(t, e) {
    if (typeof t != "function")
      throw Error(f(191, t));
    t.call(e);
  }
  function Ar(t, e) {
    var l = t.callbacks;
    if (l !== null)
      for (t.callbacks = null, t = 0; t < l.length; t++)
        Tr(l[t], e);
  }
  var qn = b(null), Yu = b(0);
  function Rr(t, e) {
    t = ul, j(Yu, t), j(qn, e), ul = t | e.baseLanes;
  }
  function Pc() {
    j(Yu, ul), j(qn, qn.current);
  }
  function tf() {
    ul = Yu.current, U(qn), U(Yu);
  }
  var he = b(null), Ce = null;
  function El(t) {
    var e = t.alternate;
    j(Bt, Bt.current & 1), j(he, t), Ce === null && (e === null || qn.current !== null || e.memoizedState !== null) && (Ce = t);
  }
  function ef(t) {
    j(Bt, Bt.current), j(he, t), Ce === null && (Ce = t);
  }
  function Or(t) {
    t.tag === 22 ? (j(Bt, Bt.current), j(he, t), Ce === null && (Ce = t)) : Tl();
  }
  function Tl() {
    j(Bt, Bt.current), j(he, he.current);
  }
  function ye(t) {
    U(he), Ce === t && (Ce = null), U(Bt);
  }
  var Bt = b(0);
  function ju(t) {
    for (var e = t; e !== null; ) {
      if (e.tag === 13) {
        var l = e.memoizedState;
        if (l !== null && (l = l.dehydrated, l === null || fo(l) || oo(l)))
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
  var $e = 0, ct = null, At = null, jt = null, Xu = !1, Yn = !1, nn = !1, Gu = 0, Ma = 0, jn = null, d0 = 0;
  function Ut() {
    throw Error(f(321));
  }
  function lf(t, e) {
    if (e === null) return !1;
    for (var l = 0; l < e.length && l < t.length; l++)
      if (!me(t[l], e[l])) return !1;
    return !0;
  }
  function nf(t, e, l, n, a, u) {
    return $e = u, ct = e, e.memoizedState = null, e.updateQueue = null, e.lanes = 0, R.H = t === null || t.memoizedState === null ? fd : Sf, nn = !1, u = l(n, a), nn = !1, Yn && (u = zr(
      e,
      l,
      n,
      a
    )), _r(t), u;
  }
  function _r(t) {
    R.H = Da;
    var e = At !== null && At.next !== null;
    if ($e = 0, jt = At = ct = null, Xu = !1, Ma = 0, jn = null, e) throw Error(f(300));
    t === null || Xt || (t = t.dependencies, t !== null && wu(t) && (Xt = !0));
  }
  function zr(t, e, l, n) {
    ct = t;
    var a = 0;
    do {
      if (Yn && (jn = null), Ma = 0, Yn = !1, 25 <= a) throw Error(f(301));
      if (a += 1, jt = At = null, t.updateQueue != null) {
        var u = t.updateQueue;
        u.lastEffect = null, u.events = null, u.stores = null, u.memoCache != null && (u.memoCache.index = 0);
      }
      R.H = od, u = e(l, n);
    } while (Yn);
    return u;
  }
  function m0() {
    var t = R.H, e = t.useState()[0];
    return e = typeof e.then == "function" ? Ca(e) : e, t = t.useState()[0], (At !== null ? At.memoizedState : null) !== t && (ct.flags |= 1024), e;
  }
  function af() {
    var t = Gu !== 0;
    return Gu = 0, t;
  }
  function uf(t, e, l) {
    e.updateQueue = t.updateQueue, e.flags &= -2053, t.lanes &= ~l;
  }
  function cf(t) {
    if (Xu) {
      for (t = t.memoizedState; t !== null; ) {
        var e = t.queue;
        e !== null && (e.pending = null), t = t.next;
      }
      Xu = !1;
    }
    $e = 0, jt = At = ct = null, Yn = !1, Ma = Gu = 0, jn = null;
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
  function Qu() {
    return { lastEffect: null, events: null, stores: null, memoCache: null };
  }
  function Ca(t) {
    var e = Ma;
    return Ma += 1, jn === null && (jn = []), t = gr(jn, t, e), e = ct, (jt === null ? e.memoizedState : jt.next) === null && (e = e.alternate, R.H = e === null || e.memoizedState === null ? fd : Sf), t;
  }
  function Vu(t) {
    if (t !== null && typeof t == "object") {
      if (typeof t.then == "function") return Ca(t);
      if (t.$$typeof === G) return $t(t);
    }
    throw Error(f(438, String(t)));
  }
  function ff(t) {
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
    if (e == null && (e = { data: [], index: 0 }), l === null && (l = Qu(), ct.updateQueue = l), l.memoCache = e, l = e.data[e.index], l === void 0)
      for (l = e.data[e.index] = Array(t), n = 0; n < t; n++)
        l[n] = Tt;
    return e.index++, l;
  }
  function Ie(t, e) {
    return typeof e == "function" ? e(t) : e;
  }
  function Zu(t) {
    var e = Lt();
    return of(e, At, t);
  }
  function of(t, e, l) {
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
      var d = s = null, y = null, A = e, x = !1;
      do {
        var N = A.lane & -536870913;
        if (N !== A.lane ? (vt & N) === N : ($e & N) === N) {
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
            }), N === Un && (x = !0);
          else if (($e & O) === O) {
            A = A.next, O === Un && (x = !0);
            continue;
          } else
            N = {
              lane: 0,
              revertLane: A.revertLane,
              gesture: null,
              action: A.action,
              hasEagerState: A.hasEagerState,
              eagerState: A.eagerState,
              next: null
            }, y === null ? (d = y = N, s = u) : y = y.next = N, ct.lanes |= O, Ol |= O;
          N = A.action, nn && l(u, N), u = A.hasEagerState ? A.eagerState : l(u, N);
        } else
          O = {
            lane: N,
            revertLane: A.revertLane,
            gesture: A.gesture,
            action: A.action,
            hasEagerState: A.hasEagerState,
            eagerState: A.eagerState,
            next: null
          }, y === null ? (d = y = O, s = u) : y = y.next = O, ct.lanes |= N, Ol |= N;
        A = A.next;
      } while (A !== null && A !== e);
      if (y === null ? s = u : y.next = d, !me(u, t.memoizedState) && (Xt = !0, x && (l = Hn, l !== null)))
        throw l;
      t.memoizedState = u, t.baseState = s, t.baseQueue = y, n.lastRenderedState = u;
    }
    return a === null && (n.lanes = 0), [t.memoizedState, n.dispatch];
  }
  function sf(t) {
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
      me(u, e.memoizedState) || (Xt = !0), e.memoizedState = u, e.baseQueue === null && (e.baseState = u), l.lastRenderedState = u;
    }
    return [u, n];
  }
  function Mr(t, e, l) {
    var n = ct, a = Lt(), u = yt;
    if (u) {
      if (l === void 0) throw Error(f(407));
      l = l();
    } else l = e();
    var s = !me(
      (At || a).memoizedState,
      l
    );
    if (s && (a.memoizedState = l, Xt = !0), a = a.queue, mf(Dr.bind(null, n, a, t), [
      t
    ]), a.getSnapshot !== e || s || jt !== null && jt.memoizedState.tag & 1) {
      if (n.flags |= 2048, Xn(
        9,
        { destroy: void 0 },
        xr.bind(
          null,
          n,
          a,
          l,
          e
        ),
        null
      ), Mt === null) throw Error(f(349));
      u || ($e & 127) !== 0 || Cr(n, e, l);
    }
    return l;
  }
  function Cr(t, e, l) {
    t.flags |= 16384, t = { getSnapshot: e, value: l }, e = ct.updateQueue, e === null ? (e = Qu(), ct.updateQueue = e, e.stores = [t]) : (l = e.stores, l === null ? e.stores = [t] : l.push(t));
  }
  function xr(t, e, l, n) {
    e.value = l, e.getSnapshot = n, wr(e) && Nr(t);
  }
  function Dr(t, e, l) {
    return l(function() {
      wr(e) && Nr(t);
    });
  }
  function wr(t) {
    var e = t.getSnapshot;
    t = t.value;
    try {
      var l = e();
      return !me(t, l);
    } catch {
      return !0;
    }
  }
  function Nr(t) {
    var e = kl(t, 2);
    e !== null && oe(e, t, 2);
  }
  function rf(t) {
    var e = ee();
    if (typeof t == "function") {
      var l = t;
      if (t = l(), nn) {
        rl(!0);
        try {
          l();
        } finally {
          rl(!1);
        }
      }
    }
    return e.memoizedState = e.baseState = t, e.queue = {
      pending: null,
      lanes: 0,
      dispatch: null,
      lastRenderedReducer: Ie,
      lastRenderedState: t
    }, e;
  }
  function Ur(t, e, l, n) {
    return t.baseState = l, of(
      t,
      At,
      typeof n == "function" ? n : Ie
    );
  }
  function v0(t, e, l, n, a) {
    if (ku(t)) throw Error(f(485));
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
      R.T !== null ? l(!0) : u.isTransition = !1, n(u), l = e.pending, l === null ? (u.next = e.pending = u, Hr(e, u)) : (u.next = l.next, e.pending = l.next = u);
    }
  }
  function Hr(t, e) {
    var l = e.action, n = e.payload, a = t.state;
    if (e.isTransition) {
      var u = R.T, s = {};
      R.T = s;
      try {
        var d = l(a, n), y = R.S;
        y !== null && y(s, d), Br(t, e, d);
      } catch (A) {
        df(t, e, A);
      } finally {
        u !== null && s.types !== null && (u.types = s.types), R.T = u;
      }
    } else
      try {
        u = l(a, n), Br(t, e, u);
      } catch (A) {
        df(t, e, A);
      }
  }
  function Br(t, e, l) {
    l !== null && typeof l == "object" && typeof l.then == "function" ? l.then(
      function(n) {
        Lr(t, e, n);
      },
      function(n) {
        return df(t, e, n);
      }
    ) : Lr(t, e, l);
  }
  function Lr(t, e, l) {
    e.status = "fulfilled", e.value = l, qr(e), t.state = l, e = t.pending, e !== null && (l = e.next, l === e ? t.pending = null : (l = l.next, e.next = l, Hr(t, l)));
  }
  function df(t, e, l) {
    var n = t.pending;
    if (t.pending = null, n !== null) {
      n = n.next;
      do
        e.status = "rejected", e.reason = l, qr(e), e = e.next;
      while (e !== n);
    }
    t.action = null;
  }
  function qr(t) {
    t = t.listeners;
    for (var e = 0; e < t.length; e++) (0, t[e])();
  }
  function Yr(t, e) {
    return e;
  }
  function jr(t, e) {
    if (yt) {
      var l = Mt.formState;
      if (l !== null) {
        t: {
          var n = ct;
          if (yt) {
            if (xt) {
              e: {
                for (var a = xt, u = Me; a.nodeType !== 8; ) {
                  if (!u) {
                    a = null;
                    break e;
                  }
                  if (a = xe(
                    a.nextSibling
                  ), a === null) {
                    a = null;
                    break e;
                  }
                }
                u = a.data, a = u === "F!" || u === "F" ? a : null;
              }
              if (a) {
                xt = xe(
                  a.nextSibling
                ), n = a.data === "F!";
                break t;
              }
            }
            yl(n);
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
      lastRenderedReducer: Yr,
      lastRenderedState: e
    }, l.queue = n, l = ud.bind(
      null,
      ct,
      n
    ), n.dispatch = l, n = rf(!1), u = bf.bind(
      null,
      ct,
      !1,
      n.queue
    ), n = ee(), a = {
      state: e,
      dispatch: null,
      action: t,
      pending: null
    }, n.queue = a, l = v0.bind(
      null,
      ct,
      a,
      u,
      l
    ), a.dispatch = l, n.memoizedState = t, [e, l, !1];
  }
  function Xr(t) {
    var e = Lt();
    return Gr(e, At, t);
  }
  function Gr(t, e, l) {
    if (e = of(
      t,
      e,
      Yr
    )[0], t = Zu(Ie)[0], typeof e == "object" && e !== null && typeof e.then == "function")
      try {
        var n = Ca(e);
      } catch (s) {
        throw s === Bn ? Hu : s;
      }
    else n = e;
    e = Lt();
    var a = e.queue, u = a.dispatch;
    return l !== e.memoizedState && (ct.flags |= 2048, Xn(
      9,
      { destroy: void 0 },
      h0.bind(null, a, l),
      null
    )), [n, u, t];
  }
  function h0(t, e) {
    t.action = e;
  }
  function Qr(t) {
    var e = Lt(), l = At;
    if (l !== null)
      return Gr(e, l, t);
    Lt(), e = e.memoizedState, l = Lt();
    var n = l.queue.dispatch;
    return l.memoizedState = t, [e, n, !1];
  }
  function Xn(t, e, l, n) {
    return t = { tag: t, create: l, deps: n, inst: e, next: null }, e = ct.updateQueue, e === null && (e = Qu(), ct.updateQueue = e), l = e.lastEffect, l === null ? e.lastEffect = t.next = t : (n = l.next, l.next = t, t.next = n, e.lastEffect = t), t;
  }
  function Vr() {
    return Lt().memoizedState;
  }
  function Ku(t, e, l, n) {
    var a = ee();
    ct.flags |= t, a.memoizedState = Xn(
      1 | e,
      { destroy: void 0 },
      l,
      n === void 0 ? null : n
    );
  }
  function Ju(t, e, l, n) {
    var a = Lt();
    n = n === void 0 ? null : n;
    var u = a.memoizedState.inst;
    At !== null && n !== null && lf(n, At.memoizedState.deps) ? a.memoizedState = Xn(e, u, l, n) : (ct.flags |= t, a.memoizedState = Xn(
      1 | e,
      u,
      l,
      n
    ));
  }
  function Zr(t, e) {
    Ku(8390656, 8, t, e);
  }
  function mf(t, e) {
    Ju(2048, 8, t, e);
  }
  function y0(t) {
    ct.flags |= 4;
    var e = ct.updateQueue;
    if (e === null)
      e = Qu(), ct.updateQueue = e, e.events = [t];
    else {
      var l = e.events;
      l === null ? e.events = [t] : l.push(t);
    }
  }
  function Kr(t) {
    var e = Lt().memoizedState;
    return y0({ ref: e, nextImpl: t }), function() {
      if ((St & 2) !== 0) throw Error(f(440));
      return e.impl.apply(void 0, arguments);
    };
  }
  function Jr(t, e) {
    return Ju(4, 2, t, e);
  }
  function kr(t, e) {
    return Ju(4, 4, t, e);
  }
  function Wr(t, e) {
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
  function Fr(t, e, l) {
    l = l != null ? l.concat([t]) : null, Ju(4, 4, Wr.bind(null, e, t), l);
  }
  function vf() {
  }
  function $r(t, e) {
    var l = Lt();
    e = e === void 0 ? null : e;
    var n = l.memoizedState;
    return e !== null && lf(e, n[1]) ? n[0] : (l.memoizedState = [t, e], t);
  }
  function Ir(t, e) {
    var l = Lt();
    e = e === void 0 ? null : e;
    var n = l.memoizedState;
    if (e !== null && lf(e, n[1]))
      return n[0];
    if (n = t(), nn) {
      rl(!0);
      try {
        t();
      } finally {
        rl(!1);
      }
    }
    return l.memoizedState = [n, e], n;
  }
  function hf(t, e, l) {
    return l === void 0 || ($e & 1073741824) !== 0 && (vt & 261930) === 0 ? t.memoizedState = e : (t.memoizedState = l, t = Pd(), ct.lanes |= t, Ol |= t, l);
  }
  function Pr(t, e, l, n) {
    return me(l, e) ? l : qn.current !== null ? (t = hf(t, l, n), me(t, e) || (Xt = !0), t) : ($e & 42) === 0 || ($e & 1073741824) !== 0 && (vt & 261930) === 0 ? (Xt = !0, t.memoizedState = l) : (t = Pd(), ct.lanes |= t, Ol |= t, e);
  }
  function td(t, e, l, n, a) {
    var u = L.p;
    L.p = u !== 0 && 8 > u ? u : 8;
    var s = R.T, d = {};
    R.T = d, bf(t, !1, e, l);
    try {
      var y = a(), A = R.S;
      if (A !== null && A(d, y), y !== null && typeof y == "object" && typeof y.then == "function") {
        var x = r0(
          y,
          n
        );
        xa(
          t,
          e,
          x,
          Se(t)
        );
      } else
        xa(
          t,
          e,
          n,
          Se(t)
        );
    } catch (N) {
      xa(
        t,
        e,
        { then: function() {
        }, status: "rejected", reason: N },
        Se()
      );
    } finally {
      L.p = u, s !== null && d.types !== null && (s.types = d.types), R.T = s;
    }
  }
  function g0() {
  }
  function yf(t, e, l, n) {
    if (t.tag !== 5) throw Error(f(476));
    var a = ed(t).queue;
    td(
      t,
      a,
      e,
      H,
      l === null ? g0 : function() {
        return ld(t), l(n);
      }
    );
  }
  function ed(t) {
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
        lastRenderedReducer: Ie,
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
        lastRenderedReducer: Ie,
        lastRenderedState: l
      },
      next: null
    }, t.memoizedState = e, t = t.alternate, t !== null && (t.memoizedState = e), e;
  }
  function ld(t) {
    var e = ed(t);
    e.next === null && (e = t.alternate.memoizedState), xa(
      t,
      e.next.queue,
      {},
      Se()
    );
  }
  function gf() {
    return $t(Ja);
  }
  function nd() {
    return Lt().memoizedState;
  }
  function ad() {
    return Lt().memoizedState;
  }
  function b0(t) {
    for (var e = t.return; e !== null; ) {
      switch (e.tag) {
        case 24:
        case 3:
          var l = Se();
          t = Sl(l);
          var n = pl(e, t, l);
          n !== null && (oe(n, e, l), Oa(n, e, l)), e = { cache: Zc() }, t.payload = e;
          return;
      }
      e = e.return;
    }
  }
  function S0(t, e, l) {
    var n = Se();
    l = {
      lane: n,
      revertLane: 0,
      gesture: null,
      action: l,
      hasEagerState: !1,
      eagerState: null,
      next: null
    }, ku(t) ? id(e, l) : (l = Uc(t, e, l, n), l !== null && (oe(l, t, n), cd(l, e, n)));
  }
  function ud(t, e, l) {
    var n = Se();
    xa(t, e, l, n);
  }
  function xa(t, e, l, n) {
    var a = {
      lane: n,
      revertLane: 0,
      gesture: null,
      action: l,
      hasEagerState: !1,
      eagerState: null,
      next: null
    };
    if (ku(t)) id(e, a);
    else {
      var u = t.alternate;
      if (t.lanes === 0 && (u === null || u.lanes === 0) && (u = e.lastRenderedReducer, u !== null))
        try {
          var s = e.lastRenderedState, d = u(s, l);
          if (a.hasEagerState = !0, a.eagerState = d, me(d, s))
            return Mu(t, e, a, 0), Mt === null && zu(), !1;
        } catch {
        } finally {
        }
      if (l = Uc(t, e, a, n), l !== null)
        return oe(l, t, n), cd(l, e, n), !0;
    }
    return !1;
  }
  function bf(t, e, l, n) {
    if (n = {
      lane: 2,
      revertLane: Ff(),
      gesture: null,
      action: n,
      hasEagerState: !1,
      eagerState: null,
      next: null
    }, ku(t)) {
      if (e) throw Error(f(479));
    } else
      e = Uc(
        t,
        l,
        n,
        2
      ), e !== null && oe(e, t, 2);
  }
  function ku(t) {
    var e = t.alternate;
    return t === ct || e !== null && e === ct;
  }
  function id(t, e) {
    Yn = Xu = !0;
    var l = t.pending;
    l === null ? e.next = e : (e.next = l.next, l.next = e), t.pending = e;
  }
  function cd(t, e, l) {
    if ((l & 4194048) !== 0) {
      var n = e.lanes;
      n &= t.pendingLanes, l |= n, e.lanes = l, ds(t, l);
    }
  }
  var Da = {
    readContext: $t,
    use: Vu,
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
  Da.useEffectEvent = Ut;
  var fd = {
    readContext: $t,
    use: Vu,
    useCallback: function(t, e) {
      return ee().memoizedState = [
        t,
        e === void 0 ? null : e
      ], t;
    },
    useContext: $t,
    useEffect: Zr,
    useImperativeHandle: function(t, e, l) {
      l = l != null ? l.concat([t]) : null, Ku(
        4194308,
        4,
        Wr.bind(null, e, t),
        l
      );
    },
    useLayoutEffect: function(t, e) {
      return Ku(4194308, 4, t, e);
    },
    useInsertionEffect: function(t, e) {
      Ku(4, 2, t, e);
    },
    useMemo: function(t, e) {
      var l = ee();
      e = e === void 0 ? null : e;
      var n = t();
      if (nn) {
        rl(!0);
        try {
          t();
        } finally {
          rl(!1);
        }
      }
      return l.memoizedState = [n, e], n;
    },
    useReducer: function(t, e, l) {
      var n = ee();
      if (l !== void 0) {
        var a = l(e);
        if (nn) {
          rl(!0);
          try {
            l(e);
          } finally {
            rl(!1);
          }
        }
      } else a = e;
      return n.memoizedState = n.baseState = a, t = {
        pending: null,
        lanes: 0,
        dispatch: null,
        lastRenderedReducer: t,
        lastRenderedState: a
      }, n.queue = t, t = t.dispatch = S0.bind(
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
      t = rf(t);
      var e = t.queue, l = ud.bind(null, ct, e);
      return e.dispatch = l, [t.memoizedState, l];
    },
    useDebugValue: vf,
    useDeferredValue: function(t, e) {
      var l = ee();
      return hf(l, t, e);
    },
    useTransition: function() {
      var t = rf(!1);
      return t = td.bind(
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
        (vt & 127) !== 0 || Cr(n, e, l);
      }
      a.memoizedState = l;
      var u = { value: l, getSnapshot: e };
      return a.queue = u, Zr(Dr.bind(null, n, u, t), [
        t
      ]), n.flags |= 2048, Xn(
        9,
        { destroy: void 0 },
        xr.bind(
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
        var l = Be, n = He;
        l = (n & ~(1 << 32 - de(n) - 1)).toString(32) + l, e = "_" + e + "R_" + l, l = Gu++, 0 < l && (e += "H" + l.toString(32)), e += "_";
      } else
        l = d0++, e = "_" + e + "r_" + l.toString(32) + "_";
      return t.memoizedState = e;
    },
    useHostTransitionStatus: gf,
    useFormState: jr,
    useActionState: jr,
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
      return e.queue = l, e = bf.bind(
        null,
        ct,
        !0,
        l
      ), l.dispatch = e, [t, e];
    },
    useMemoCache: ff,
    useCacheRefresh: function() {
      return ee().memoizedState = b0.bind(
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
  }, Sf = {
    readContext: $t,
    use: Vu,
    useCallback: $r,
    useContext: $t,
    useEffect: mf,
    useImperativeHandle: Fr,
    useInsertionEffect: Jr,
    useLayoutEffect: kr,
    useMemo: Ir,
    useReducer: Zu,
    useRef: Vr,
    useState: function() {
      return Zu(Ie);
    },
    useDebugValue: vf,
    useDeferredValue: function(t, e) {
      var l = Lt();
      return Pr(
        l,
        At.memoizedState,
        t,
        e
      );
    },
    useTransition: function() {
      var t = Zu(Ie)[0], e = Lt().memoizedState;
      return [
        typeof t == "boolean" ? t : Ca(t),
        e
      ];
    },
    useSyncExternalStore: Mr,
    useId: nd,
    useHostTransitionStatus: gf,
    useFormState: Xr,
    useActionState: Xr,
    useOptimistic: function(t, e) {
      var l = Lt();
      return Ur(l, At, t, e);
    },
    useMemoCache: ff,
    useCacheRefresh: ad
  };
  Sf.useEffectEvent = Kr;
  var od = {
    readContext: $t,
    use: Vu,
    useCallback: $r,
    useContext: $t,
    useEffect: mf,
    useImperativeHandle: Fr,
    useInsertionEffect: Jr,
    useLayoutEffect: kr,
    useMemo: Ir,
    useReducer: sf,
    useRef: Vr,
    useState: function() {
      return sf(Ie);
    },
    useDebugValue: vf,
    useDeferredValue: function(t, e) {
      var l = Lt();
      return At === null ? hf(l, t, e) : Pr(
        l,
        At.memoizedState,
        t,
        e
      );
    },
    useTransition: function() {
      var t = sf(Ie)[0], e = Lt().memoizedState;
      return [
        typeof t == "boolean" ? t : Ca(t),
        e
      ];
    },
    useSyncExternalStore: Mr,
    useId: nd,
    useHostTransitionStatus: gf,
    useFormState: Qr,
    useActionState: Qr,
    useOptimistic: function(t, e) {
      var l = Lt();
      return At !== null ? Ur(l, At, t, e) : (l.baseState = t, [t, l.queue.dispatch]);
    },
    useMemoCache: ff,
    useCacheRefresh: ad
  };
  od.useEffectEvent = Kr;
  function pf(t, e, l, n) {
    e = t.memoizedState, l = l(n, e), l = l == null ? e : _({}, e, l), t.memoizedState = l, t.lanes === 0 && (t.updateQueue.baseState = l);
  }
  var Ef = {
    enqueueSetState: function(t, e, l) {
      t = t._reactInternals;
      var n = Se(), a = Sl(n);
      a.payload = e, l != null && (a.callback = l), e = pl(t, a, n), e !== null && (oe(e, t, n), Oa(e, t, n));
    },
    enqueueReplaceState: function(t, e, l) {
      t = t._reactInternals;
      var n = Se(), a = Sl(n);
      a.tag = 1, a.payload = e, l != null && (a.callback = l), e = pl(t, a, n), e !== null && (oe(e, t, n), Oa(e, t, n));
    },
    enqueueForceUpdate: function(t, e) {
      t = t._reactInternals;
      var l = Se(), n = Sl(l);
      n.tag = 2, e != null && (n.callback = e), e = pl(t, n, l), e !== null && (oe(e, t, l), Oa(e, t, l));
    }
  };
  function sd(t, e, l, n, a, u, s) {
    return t = t.stateNode, typeof t.shouldComponentUpdate == "function" ? t.shouldComponentUpdate(n, u, s) : e.prototype && e.prototype.isPureReactComponent ? !ga(l, n) || !ga(a, u) : !0;
  }
  function rd(t, e, l, n) {
    t = e.state, typeof e.componentWillReceiveProps == "function" && e.componentWillReceiveProps(l, n), typeof e.UNSAFE_componentWillReceiveProps == "function" && e.UNSAFE_componentWillReceiveProps(l, n), e.state !== t && Ef.enqueueReplaceState(e, e.state, null);
  }
  function an(t, e) {
    var l = e;
    if ("ref" in e) {
      l = {};
      for (var n in e)
        n !== "ref" && (l[n] = e[n]);
    }
    if (t = t.defaultProps) {
      l === e && (l = _({}, l));
      for (var a in t)
        l[a] === void 0 && (l[a] = t[a]);
    }
    return l;
  }
  function dd(t) {
    _u(t);
  }
  function md(t) {
    console.error(t);
  }
  function vd(t) {
    _u(t);
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
  function hd(t, e, l) {
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
  function Tf(t, e, l) {
    return l = Sl(l), l.tag = 3, l.payload = { element: null }, l.callback = function() {
      Wu(t, e);
    }, l;
  }
  function yd(t) {
    return t = Sl(t), t.tag = 3, t;
  }
  function gd(t, e, l, n) {
    var a = l.type.getDerivedStateFromError;
    if (typeof a == "function") {
      var u = n.value;
      t.payload = function() {
        return a(u);
      }, t.callback = function() {
        hd(e, l, n);
      };
    }
    var s = l.stateNode;
    s !== null && typeof s.componentDidCatch == "function" && (t.callback = function() {
      hd(e, l, n), typeof a != "function" && (_l === null ? _l = /* @__PURE__ */ new Set([this]) : _l.add(this));
      var d = n.stack;
      this.componentDidCatch(n.value, {
        componentStack: d !== null ? d : ""
      });
    });
  }
  function p0(t, e, l, n, a) {
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
            return Ce === null ? ci() : l.alternate === null && Ht === 0 && (Ht = 3), l.flags &= -257, l.flags |= 65536, l.lanes = a, n === Bu ? l.flags |= 16384 : (e = l.updateQueue, e === null ? l.updateQueue = /* @__PURE__ */ new Set([n]) : e.add(n), Jf(t, n, a)), !1;
          case 22:
            return l.flags |= 65536, n === Bu ? l.flags |= 16384 : (e = l.updateQueue, e === null ? (e = {
              transitions: null,
              markerInstances: null,
              retryQueue: /* @__PURE__ */ new Set([n])
            }, l.updateQueue = e) : (l = e.retryQueue, l === null ? e.retryQueue = /* @__PURE__ */ new Set([n]) : l.add(n)), Jf(t, n, a)), !1;
        }
        throw Error(f(435, l.tag));
      }
      return Jf(t, n, a), ci(), !1;
    }
    if (yt)
      return e = he.current, e !== null ? ((e.flags & 65536) === 0 && (e.flags |= 256), e.flags |= 65536, e.lanes = a, n !== jc && (t = Error(f(422), { cause: n }), pa(Oe(t, l)))) : (n !== jc && (e = Error(f(423), {
        cause: n
      }), pa(
        Oe(e, l)
      )), t = t.current.alternate, t.flags |= 65536, a &= -a, t.lanes |= a, n = Oe(n, l), a = Tf(
        t.stateNode,
        n,
        a
      ), $c(t, a), Ht !== 4 && (Ht = 2)), !1;
    var u = Error(f(520), { cause: n });
    if (u = Oe(u, l), Ya === null ? Ya = [u] : Ya.push(u), Ht !== 4 && (Ht = 2), e === null) return !0;
    n = Oe(n, l), l = e;
    do {
      switch (l.tag) {
        case 3:
          return l.flags |= 65536, t = a & -a, l.lanes |= t, t = Tf(l.stateNode, n, t), $c(l, t), !1;
        case 1:
          if (e = l.type, u = l.stateNode, (l.flags & 128) === 0 && (typeof e.getDerivedStateFromError == "function" || u !== null && typeof u.componentDidCatch == "function" && (_l === null || !_l.has(u))))
            return l.flags |= 65536, a &= -a, l.lanes |= a, a = yd(a), gd(
              a,
              t,
              l,
              n
            ), $c(l, a), !1;
      }
      l = l.return;
    } while (l !== null);
    return !1;
  }
  var Af = Error(f(461)), Xt = !1;
  function It(t, e, l, n) {
    e.child = t === null ? Er(e, null, l, n) : ln(
      e,
      t.child,
      l,
      n
    );
  }
  function bd(t, e, l, n, a) {
    l = l.render;
    var u = e.ref;
    if ("ref" in n) {
      var s = {};
      for (var d in n)
        d !== "ref" && (s[d] = n[d]);
    } else s = n;
    return Il(e), n = nf(
      t,
      e,
      l,
      s,
      u,
      a
    ), d = af(), t !== null && !Xt ? (uf(t, e, a), Pe(t, e, a)) : (yt && d && qc(e), e.flags |= 1, It(t, e, n, a), e.child);
  }
  function Sd(t, e, l, n, a) {
    if (t === null) {
      var u = l.type;
      return typeof u == "function" && !Hc(u) && u.defaultProps === void 0 && l.compare === null ? (e.tag = 15, e.type = u, pd(
        t,
        e,
        u,
        n,
        a
      )) : (t = xu(
        l.type,
        null,
        n,
        e,
        e.mode,
        a
      ), t.ref = e.ref, t.return = e, e.child = t);
    }
    if (u = t.child, !Df(t, a)) {
      var s = u.memoizedProps;
      if (l = l.compare, l = l !== null ? l : ga, l(s, n) && t.ref === e.ref)
        return Pe(t, e, a);
    }
    return e.flags |= 1, t = Je(u, n), t.ref = e.ref, t.return = e, e.child = t;
  }
  function pd(t, e, l, n, a) {
    if (t !== null) {
      var u = t.memoizedProps;
      if (ga(u, n) && t.ref === e.ref)
        if (Xt = !1, e.pendingProps = n = u, Df(t, a))
          (t.flags & 131072) !== 0 && (Xt = !0);
        else
          return e.lanes = t.lanes, Pe(t, e, a);
    }
    return Rf(
      t,
      e,
      l,
      n,
      a
    );
  }
  function Ed(t, e, l, n) {
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
        return Td(
          t,
          e,
          u,
          l,
          n
        );
      }
      if ((l & 536870912) !== 0)
        e.memoizedState = { baseLanes: 0, cachePool: null }, t !== null && Uu(
          e,
          u !== null ? u.cachePool : null
        ), u !== null ? Rr(e, u) : Pc(), Or(e);
      else
        return n = e.lanes = 536870912, Td(
          t,
          e,
          u !== null ? u.baseLanes | l : l,
          l,
          n
        );
    } else
      u !== null ? (Uu(e, u.cachePool), Rr(e, u), Tl(), e.memoizedState = null) : (t !== null && Uu(e, null), Pc(), Tl());
    return It(t, e, a, l), e.child;
  }
  function wa(t, e) {
    return t !== null && t.tag === 22 || e.stateNode !== null || (e.stateNode = {
      _visibility: 1,
      _pendingMarkers: null,
      _retryCache: null,
      _transitions: null
    }), e.sibling;
  }
  function Td(t, e, l, n, a) {
    var u = Jc();
    return u = u === null ? null : { parent: Yt._currentValue, pool: u }, e.memoizedState = {
      baseLanes: l,
      cachePool: u
    }, t !== null && Uu(e, null), Pc(), Or(e), t !== null && Nn(t, e, n, !0), e.childLanes = a, null;
  }
  function Fu(t, e) {
    return e = Iu(
      { mode: e.mode, children: e.children },
      t.mode
    ), e.ref = t.ref, t.child = e, e.return = t, e;
  }
  function Ad(t, e, l) {
    return ln(e, t.child, null, l), t = Fu(e, e.pendingProps), t.flags |= 2, ye(e), e.memoizedState = null, t;
  }
  function E0(t, e, l) {
    var n = e.pendingProps, a = (e.flags & 128) !== 0;
    if (e.flags &= -129, t === null) {
      if (yt) {
        if (n.mode === "hidden")
          return t = Fu(e, n), e.lanes = 536870912, wa(null, t);
        if (ef(e), (t = xt) ? (t = Hm(
          t,
          Me
        ), t = t !== null && t.data === "&" ? t : null, t !== null && (e.memoizedState = {
          dehydrated: t,
          treeContext: vl !== null ? { id: He, overflow: Be } : null,
          retryLane: 536870912,
          hydrationErrors: null
        }, l = ir(t), l.return = e, e.child = l, Ft = e, xt = null)) : t = null, t === null) throw yl(e);
        return e.lanes = 536870912, null;
      }
      return Fu(e, n);
    }
    var u = t.memoizedState;
    if (u !== null) {
      var s = u.dehydrated;
      if (ef(e), a)
        if (e.flags & 256)
          e.flags &= -257, e = Ad(
            t,
            e,
            l
          );
        else if (e.memoizedState !== null)
          e.child = t.child, e.flags |= 128, e = null;
        else throw Error(f(558));
      else if (Xt || Nn(t, e, l, !1), a = (l & t.childLanes) !== 0, Xt || a) {
        if (n = Mt, n !== null && (s = ms(n, l), s !== 0 && s !== u.retryLane))
          throw u.retryLane = s, kl(t, s), oe(n, t, s), Af;
        ci(), e = Ad(
          t,
          e,
          l
        );
      } else
        t = u.treeContext, xt = xe(s.nextSibling), Ft = e, yt = !0, hl = null, Me = !1, t !== null && or(e, t), e = Fu(e, n), e.flags |= 4096;
      return e;
    }
    return t = Je(t.child, {
      mode: n.mode,
      children: n.children
    }), t.ref = e.ref, e.child = t, t.return = e, t;
  }
  function $u(t, e) {
    var l = e.ref;
    if (l === null)
      t !== null && t.ref !== null && (e.flags |= 4194816);
    else {
      if (typeof l != "function" && typeof l != "object")
        throw Error(f(284));
      (t === null || t.ref !== l) && (e.flags |= 4194816);
    }
  }
  function Rf(t, e, l, n, a) {
    return Il(e), l = nf(
      t,
      e,
      l,
      n,
      void 0,
      a
    ), n = af(), t !== null && !Xt ? (uf(t, e, a), Pe(t, e, a)) : (yt && n && qc(e), e.flags |= 1, It(t, e, l, a), e.child);
  }
  function Rd(t, e, l, n, a, u) {
    return Il(e), e.updateQueue = null, l = zr(
      e,
      n,
      l,
      a
    ), _r(t), n = af(), t !== null && !Xt ? (uf(t, e, u), Pe(t, e, u)) : (yt && n && qc(e), e.flags |= 1, It(t, e, l, u), e.child);
  }
  function Od(t, e, l, n, a) {
    if (Il(e), e.stateNode === null) {
      var u = Cn, s = l.contextType;
      typeof s == "object" && s !== null && (u = $t(s)), u = new l(n, u), e.memoizedState = u.state !== null && u.state !== void 0 ? u.state : null, u.updater = Ef, e.stateNode = u, u._reactInternals = e, u = e.stateNode, u.props = n, u.state = e.memoizedState, u.refs = {}, Wc(e), s = l.contextType, u.context = typeof s == "object" && s !== null ? $t(s) : Cn, u.state = e.memoizedState, s = l.getDerivedStateFromProps, typeof s == "function" && (pf(
        e,
        l,
        s,
        n
      ), u.state = e.memoizedState), typeof l.getDerivedStateFromProps == "function" || typeof u.getSnapshotBeforeUpdate == "function" || typeof u.UNSAFE_componentWillMount != "function" && typeof u.componentWillMount != "function" || (s = u.state, typeof u.componentWillMount == "function" && u.componentWillMount(), typeof u.UNSAFE_componentWillMount == "function" && u.UNSAFE_componentWillMount(), s !== u.state && Ef.enqueueReplaceState(u, u.state, null), za(e, n, u, a), _a(), u.state = e.memoizedState), typeof u.componentDidMount == "function" && (e.flags |= 4194308), n = !0;
    } else if (t === null) {
      u = e.stateNode;
      var d = e.memoizedProps, y = an(l, d);
      u.props = y;
      var A = u.context, x = l.contextType;
      s = Cn, typeof x == "object" && x !== null && (s = $t(x));
      var N = l.getDerivedStateFromProps;
      x = typeof N == "function" || typeof u.getSnapshotBeforeUpdate == "function", d = e.pendingProps !== d, x || typeof u.UNSAFE_componentWillReceiveProps != "function" && typeof u.componentWillReceiveProps != "function" || (d || A !== s) && rd(
        e,
        u,
        n,
        s
      ), bl = !1;
      var O = e.memoizedState;
      u.state = O, za(e, n, u, a), _a(), A = e.memoizedState, d || O !== A || bl ? (typeof N == "function" && (pf(
        e,
        l,
        N,
        n
      ), A = e.memoizedState), (y = bl || sd(
        e,
        l,
        y,
        n,
        O,
        A,
        s
      )) ? (x || typeof u.UNSAFE_componentWillMount != "function" && typeof u.componentWillMount != "function" || (typeof u.componentWillMount == "function" && u.componentWillMount(), typeof u.UNSAFE_componentWillMount == "function" && u.UNSAFE_componentWillMount()), typeof u.componentDidMount == "function" && (e.flags |= 4194308)) : (typeof u.componentDidMount == "function" && (e.flags |= 4194308), e.memoizedProps = n, e.memoizedState = A), u.props = n, u.state = A, u.context = s, n = y) : (typeof u.componentDidMount == "function" && (e.flags |= 4194308), n = !1);
    } else {
      u = e.stateNode, Fc(t, e), s = e.memoizedProps, x = an(l, s), u.props = x, N = e.pendingProps, O = u.context, A = l.contextType, y = Cn, typeof A == "object" && A !== null && (y = $t(A)), d = l.getDerivedStateFromProps, (A = typeof d == "function" || typeof u.getSnapshotBeforeUpdate == "function") || typeof u.UNSAFE_componentWillReceiveProps != "function" && typeof u.componentWillReceiveProps != "function" || (s !== N || O !== y) && rd(
        e,
        u,
        n,
        y
      ), bl = !1, O = e.memoizedState, u.state = O, za(e, n, u, a), _a();
      var M = e.memoizedState;
      s !== N || O !== M || bl || t !== null && t.dependencies !== null && wu(t.dependencies) ? (typeof d == "function" && (pf(
        e,
        l,
        d,
        n
      ), M = e.memoizedState), (x = bl || sd(
        e,
        l,
        x,
        n,
        O,
        M,
        y
      ) || t !== null && t.dependencies !== null && wu(t.dependencies)) ? (A || typeof u.UNSAFE_componentWillUpdate != "function" && typeof u.componentWillUpdate != "function" || (typeof u.componentWillUpdate == "function" && u.componentWillUpdate(n, M, y), typeof u.UNSAFE_componentWillUpdate == "function" && u.UNSAFE_componentWillUpdate(
        n,
        M,
        y
      )), typeof u.componentDidUpdate == "function" && (e.flags |= 4), typeof u.getSnapshotBeforeUpdate == "function" && (e.flags |= 1024)) : (typeof u.componentDidUpdate != "function" || s === t.memoizedProps && O === t.memoizedState || (e.flags |= 4), typeof u.getSnapshotBeforeUpdate != "function" || s === t.memoizedProps && O === t.memoizedState || (e.flags |= 1024), e.memoizedProps = n, e.memoizedState = M), u.props = n, u.state = M, u.context = y, n = x) : (typeof u.componentDidUpdate != "function" || s === t.memoizedProps && O === t.memoizedState || (e.flags |= 4), typeof u.getSnapshotBeforeUpdate != "function" || s === t.memoizedProps && O === t.memoizedState || (e.flags |= 1024), n = !1);
    }
    return u = n, $u(t, e), n = (e.flags & 128) !== 0, u || n ? (u = e.stateNode, l = n && typeof l.getDerivedStateFromError != "function" ? null : u.render(), e.flags |= 1, t !== null && n ? (e.child = ln(
      e,
      t.child,
      null,
      a
    ), e.child = ln(
      e,
      null,
      l,
      a
    )) : It(t, e, l, a), e.memoizedState = u.state, t = e.child) : t = Pe(
      t,
      e,
      a
    ), t;
  }
  function _d(t, e, l, n) {
    return Fl(), e.flags |= 256, It(t, e, l, n), e.child;
  }
  var Of = {
    dehydrated: null,
    treeContext: null,
    retryLane: 0,
    hydrationErrors: null
  };
  function _f(t) {
    return { baseLanes: t, cachePool: hr() };
  }
  function zf(t, e, l) {
    return t = t !== null ? t.childLanes & ~l : 0, e && (t |= be), t;
  }
  function zd(t, e, l) {
    var n = e.pendingProps, a = !1, u = (e.flags & 128) !== 0, s;
    if ((s = u) || (s = t !== null && t.memoizedState === null ? !1 : (Bt.current & 2) !== 0), s && (a = !0, e.flags &= -129), s = (e.flags & 32) !== 0, e.flags &= -33, t === null) {
      if (yt) {
        if (a ? El(e) : Tl(), (t = xt) ? (t = Hm(
          t,
          Me
        ), t = t !== null && t.data !== "&" ? t : null, t !== null && (e.memoizedState = {
          dehydrated: t,
          treeContext: vl !== null ? { id: He, overflow: Be } : null,
          retryLane: 536870912,
          hydrationErrors: null
        }, l = ir(t), l.return = e, e.child = l, Ft = e, xt = null)) : t = null, t === null) throw yl(e);
        return oo(t) ? e.lanes = 32 : e.lanes = 536870912, null;
      }
      var d = n.children;
      return n = n.fallback, a ? (Tl(), a = e.mode, d = Iu(
        { mode: "hidden", children: d },
        a
      ), n = Wl(
        n,
        a,
        l,
        null
      ), d.return = e, n.return = e, d.sibling = n, e.child = d, n = e.child, n.memoizedState = _f(l), n.childLanes = zf(
        t,
        s,
        l
      ), e.memoizedState = Of, wa(null, n)) : (El(e), Mf(e, d));
    }
    var y = t.memoizedState;
    if (y !== null && (d = y.dehydrated, d !== null)) {
      if (u)
        e.flags & 256 ? (El(e), e.flags &= -257, e = Cf(
          t,
          e,
          l
        )) : e.memoizedState !== null ? (Tl(), e.child = t.child, e.flags |= 128, e = null) : (Tl(), d = n.fallback, a = e.mode, n = Iu(
          { mode: "visible", children: n.children },
          a
        ), d = Wl(
          d,
          a,
          l,
          null
        ), d.flags |= 2, n.return = e, d.return = e, n.sibling = d, e.child = n, ln(
          e,
          t.child,
          null,
          l
        ), n = e.child, n.memoizedState = _f(l), n.childLanes = zf(
          t,
          s,
          l
        ), e.memoizedState = Of, e = wa(null, n));
      else if (El(e), oo(d)) {
        if (s = d.nextSibling && d.nextSibling.dataset, s) var A = s.dgst;
        s = A, n = Error(f(419)), n.stack = "", n.digest = s, pa({ value: n, source: null, stack: null }), e = Cf(
          t,
          e,
          l
        );
      } else if (Xt || Nn(t, e, l, !1), s = (l & t.childLanes) !== 0, Xt || s) {
        if (s = Mt, s !== null && (n = ms(s, l), n !== 0 && n !== y.retryLane))
          throw y.retryLane = n, kl(t, n), oe(s, t, n), Af;
        fo(d) || ci(), e = Cf(
          t,
          e,
          l
        );
      } else
        fo(d) ? (e.flags |= 192, e.child = t.child, e = null) : (t = y.treeContext, xt = xe(
          d.nextSibling
        ), Ft = e, yt = !0, hl = null, Me = !1, t !== null && or(e, t), e = Mf(
          e,
          n.children
        ), e.flags |= 4096);
      return e;
    }
    return a ? (Tl(), d = n.fallback, a = e.mode, y = t.child, A = y.sibling, n = Je(y, {
      mode: "hidden",
      children: n.children
    }), n.subtreeFlags = y.subtreeFlags & 65011712, A !== null ? d = Je(
      A,
      d
    ) : (d = Wl(
      d,
      a,
      l,
      null
    ), d.flags |= 2), d.return = e, n.return = e, n.sibling = d, e.child = n, wa(null, n), n = e.child, d = t.child.memoizedState, d === null ? d = _f(l) : (a = d.cachePool, a !== null ? (y = Yt._currentValue, a = a.parent !== y ? { parent: y, pool: y } : a) : a = hr(), d = {
      baseLanes: d.baseLanes | l,
      cachePool: a
    }), n.memoizedState = d, n.childLanes = zf(
      t,
      s,
      l
    ), e.memoizedState = Of, wa(t.child, n)) : (El(e), l = t.child, t = l.sibling, l = Je(l, {
      mode: "visible",
      children: n.children
    }), l.return = e, l.sibling = null, t !== null && (s = e.deletions, s === null ? (e.deletions = [t], e.flags |= 16) : s.push(t)), e.child = l, e.memoizedState = null, l);
  }
  function Mf(t, e) {
    return e = Iu(
      { mode: "visible", children: e },
      t.mode
    ), e.return = t, t.child = e;
  }
  function Iu(t, e) {
    return t = ve(22, t, null, e), t.lanes = 0, t;
  }
  function Cf(t, e, l) {
    return ln(e, t.child, null, l), t = Mf(
      e,
      e.pendingProps.children
    ), t.flags |= 2, e.memoizedState = null, t;
  }
  function Md(t, e, l) {
    t.lanes |= e;
    var n = t.alternate;
    n !== null && (n.lanes |= e), Qc(t.return, e, l);
  }
  function xf(t, e, l, n, a, u) {
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
  function Cd(t, e, l) {
    var n = e.pendingProps, a = n.revealOrder, u = n.tail;
    n = n.children;
    var s = Bt.current, d = (s & 2) !== 0;
    if (d ? (s = s & 1 | 2, e.flags |= 128) : s &= 1, j(Bt, s), It(t, e, n, l), n = yt ? Sa : 0, !d && t !== null && (t.flags & 128) !== 0)
      t: for (t = e.child; t !== null; ) {
        if (t.tag === 13)
          t.memoizedState !== null && Md(t, l, e);
        else if (t.tag === 19)
          Md(t, l, e);
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
          t = l.alternate, t !== null && ju(t) === null && (a = l), l = l.sibling;
        l = a, l === null ? (a = e.child, e.child = null) : (a = l.sibling, l.sibling = null), xf(
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
          if (t = a.alternate, t !== null && ju(t) === null) {
            e.child = a;
            break;
          }
          t = a.sibling, a.sibling = l, l = a, a = t;
        }
        xf(
          e,
          !0,
          l,
          null,
          u,
          n
        );
        break;
      case "together":
        xf(
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
  function Pe(t, e, l) {
    if (t !== null && (e.dependencies = t.dependencies), Ol |= e.lanes, (l & e.childLanes) === 0)
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
      for (t = e.child, l = Je(t, t.pendingProps), e.child = l, l.return = e; t.sibling !== null; )
        t = t.sibling, l = l.sibling = Je(t, t.pendingProps), l.return = e;
      l.sibling = null;
    }
    return e.child;
  }
  function Df(t, e) {
    return (t.lanes & e) !== 0 ? !0 : (t = t.dependencies, !!(t !== null && wu(t)));
  }
  function T0(t, e, l) {
    switch (e.tag) {
      case 3:
        kt(e, e.stateNode.containerInfo), gl(e, Yt, t.memoizedState.cache), Fl();
        break;
      case 27:
      case 5:
        Gl(e);
        break;
      case 4:
        kt(e, e.stateNode.containerInfo);
        break;
      case 10:
        gl(
          e,
          e.type,
          e.memoizedProps.value
        );
        break;
      case 31:
        if (e.memoizedState !== null)
          return e.flags |= 128, ef(e), null;
        break;
      case 13:
        var n = e.memoizedState;
        if (n !== null)
          return n.dehydrated !== null ? (El(e), e.flags |= 128, null) : (l & e.child.childLanes) !== 0 ? zd(t, e, l) : (El(e), t = Pe(
            t,
            e,
            l
          ), t !== null ? t.sibling : null);
        El(e);
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
            return Cd(
              t,
              e,
              l
            );
          e.flags |= 128;
        }
        if (a = e.memoizedState, a !== null && (a.rendering = null, a.tail = null, a.lastEffect = null), j(Bt, Bt.current), n) break;
        return null;
      case 22:
        return e.lanes = 0, Ed(
          t,
          e,
          l,
          e.pendingProps
        );
      case 24:
        gl(e, Yt, t.memoizedState.cache);
    }
    return Pe(t, e, l);
  }
  function xd(t, e, l) {
    if (t !== null)
      if (t.memoizedProps !== e.pendingProps)
        Xt = !0;
      else {
        if (!Df(t, l) && (e.flags & 128) === 0)
          return Xt = !1, T0(
            t,
            e,
            l
          );
        Xt = (t.flags & 131072) !== 0;
      }
    else
      Xt = !1, yt && (e.flags & 1048576) !== 0 && fr(e, Sa, e.index);
    switch (e.lanes = 0, e.tag) {
      case 16:
        t: {
          var n = e.pendingProps;
          if (t = tn(e.elementType), e.type = t, typeof t == "function")
            Hc(t) ? (n = an(t, n), e.tag = 1, e = Od(
              null,
              e,
              t,
              n,
              l
            )) : (e.tag = 0, e = Rf(
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
                e.tag = 11, e = bd(
                  null,
                  e,
                  t,
                  n,
                  l
                );
                break t;
              } else if (a === Z) {
                e.tag = 14, e = Sd(
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
        return Rf(
          t,
          e,
          e.type,
          e.pendingProps,
          l
        );
      case 1:
        return n = e.type, a = an(
          n,
          e.pendingProps
        ), Od(
          t,
          e,
          n,
          a,
          l
        );
      case 3:
        t: {
          if (kt(
            e,
            e.stateNode.containerInfo
          ), t === null) throw Error(f(387));
          n = e.pendingProps;
          var u = e.memoizedState;
          a = u.element, Fc(t, e), za(e, n, null, l);
          var s = e.memoizedState;
          if (n = s.cache, gl(e, Yt, n), n !== u.cache && Vc(
            e,
            [Yt],
            l,
            !0
          ), _a(), n = s.element, u.isDehydrated)
            if (u = {
              element: n,
              isDehydrated: !1,
              cache: s.cache
            }, e.updateQueue.baseState = u, e.memoizedState = u, e.flags & 256) {
              e = _d(
                t,
                e,
                n,
                l
              );
              break t;
            } else if (n !== a) {
              a = Oe(
                Error(f(424)),
                e
              ), pa(a), e = _d(
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
              for (xt = xe(t.firstChild), Ft = e, yt = !0, hl = null, Me = !0, l = Er(
                e,
                null,
                n,
                l
              ), e.child = l; l; )
                l.flags = l.flags & -3 | 4096, l = l.sibling;
            }
          else {
            if (Fl(), n === a) {
              e = Pe(
                t,
                e,
                l
              );
              break t;
            }
            It(t, e, n, l);
          }
          e = e.child;
        }
        return e;
      case 26:
        return $u(t, e), t === null ? (l = Xm(
          e.type,
          null,
          e.pendingProps,
          null
        )) ? e.memoizedState = l : yt || (l = e.type, t = e.pendingProps, n = vi(
          nt.current
        ).createElement(l), n[Wt] = e, n[ne] = t, Pt(n, l, t), Zt(n), e.stateNode = n) : e.memoizedState = Xm(
          e.type,
          t.memoizedProps,
          e.pendingProps,
          t.memoizedState
        ), null;
      case 27:
        return Gl(e), t === null && yt && (n = e.stateNode = qm(
          e.type,
          e.pendingProps,
          nt.current
        ), Ft = e, Me = !0, a = xt, xl(e.type) ? (so = a, xt = xe(n.firstChild)) : xt = a), It(
          t,
          e,
          e.pendingProps.children,
          l
        ), $u(t, e), t === null && (e.flags |= 4194304), e.child;
      case 5:
        return t === null && yt && ((a = n = xt) && (n = I0(
          n,
          e.type,
          e.pendingProps,
          Me
        ), n !== null ? (e.stateNode = n, Ft = e, xt = xe(n.firstChild), Me = !1, a = !0) : a = !1), a || yl(e)), Gl(e), a = e.type, u = e.pendingProps, s = t !== null ? t.memoizedProps : null, n = u.children, uo(a, u) ? n = null : s !== null && uo(a, s) && (e.flags |= 32), e.memoizedState !== null && (a = nf(
          t,
          e,
          m0,
          null,
          null,
          l
        ), Ja._currentValue = a), $u(t, e), It(t, e, n, l), e.child;
      case 6:
        return t === null && yt && ((t = l = xt) && (l = P0(
          l,
          e.pendingProps,
          Me
        ), l !== null ? (e.stateNode = l, Ft = e, xt = null, t = !0) : t = !1), t || yl(e)), null;
      case 13:
        return zd(t, e, l);
      case 4:
        return kt(
          e,
          e.stateNode.containerInfo
        ), n = e.pendingProps, t === null ? e.child = ln(
          e,
          null,
          n,
          l
        ) : It(t, e, n, l), e.child;
      case 11:
        return bd(
          t,
          e,
          e.type,
          e.pendingProps,
          l
        );
      case 7:
        return It(
          t,
          e,
          e.pendingProps,
          l
        ), e.child;
      case 8:
        return It(
          t,
          e,
          e.pendingProps.children,
          l
        ), e.child;
      case 12:
        return It(
          t,
          e,
          e.pendingProps.children,
          l
        ), e.child;
      case 10:
        return n = e.pendingProps, gl(e, e.type, n.value), It(t, e, n.children, l), e.child;
      case 9:
        return a = e.type._context, n = e.pendingProps.children, Il(e), a = $t(a), n = n(a), e.flags |= 1, It(t, e, n, l), e.child;
      case 14:
        return Sd(
          t,
          e,
          e.type,
          e.pendingProps,
          l
        );
      case 15:
        return pd(
          t,
          e,
          e.type,
          e.pendingProps,
          l
        );
      case 19:
        return Cd(t, e, l);
      case 31:
        return E0(t, e, l);
      case 22:
        return Ed(
          t,
          e,
          l,
          e.pendingProps
        );
      case 24:
        return Il(e), n = $t(Yt), t === null ? (a = Jc(), a === null && (a = Mt, u = Zc(), a.pooledCache = u, u.refCount++, u !== null && (a.pooledCacheLanes |= l), a = u), e.memoizedState = { parent: n, cache: a }, Wc(e), gl(e, Yt, a)) : ((t.lanes & l) !== 0 && (Fc(t, e), za(e, null, null, l), _a()), a = t.memoizedState, u = e.memoizedState, a.parent !== n ? (a = { parent: n, cache: n }, e.memoizedState = a, e.lanes === 0 && (e.memoizedState = e.updateQueue.baseState = a), gl(e, Yt, n)) : (n = u.cache, gl(e, Yt, n), n !== a.cache && Vc(
          e,
          [Yt],
          l,
          !0
        ))), It(
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
  function tl(t) {
    t.flags |= 4;
  }
  function wf(t, e, l, n, a) {
    if ((e = (t.mode & 32) !== 0) && (e = !1), e) {
      if (t.flags |= 16777216, (a & 335544128) === a)
        if (t.stateNode.complete) t.flags |= 8192;
        else if (nm()) t.flags |= 8192;
        else
          throw en = Bu, kc;
    } else t.flags &= -16777217;
  }
  function Dd(t, e) {
    if (e.type !== "stylesheet" || (e.state.loading & 4) !== 0)
      t.flags &= -16777217;
    else if (t.flags |= 16777216, !Km(e))
      if (nm()) t.flags |= 8192;
      else
        throw en = Bu, kc;
  }
  function Pu(t, e) {
    e !== null && (t.flags |= 4), t.flags & 16384 && (e = t.tag !== 22 ? ss() : 536870912, t.lanes |= e, Zn |= e);
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
  function A0(t, e, l) {
    var n = e.pendingProps;
    switch (Yc(e), e.tag) {
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
        return l = e.stateNode, n = null, t !== null && (n = t.memoizedState.cache), e.memoizedState.cache !== n && (e.flags |= 2048), Fe(Yt), Nt(), l.pendingContext && (l.context = l.pendingContext, l.pendingContext = null), (t === null || t.child === null) && (wn(e) ? tl(e) : t === null || t.memoizedState.isDehydrated && (e.flags & 256) === 0 || (e.flags |= 1024, Xc())), Dt(e), null;
      case 26:
        var a = e.type, u = e.memoizedState;
        return t === null ? (tl(e), u !== null ? (Dt(e), Dd(e, u)) : (Dt(e), wf(
          e,
          a,
          null,
          n,
          l
        ))) : u ? u !== t.memoizedState ? (tl(e), Dt(e), Dd(e, u)) : (Dt(e), e.flags &= -16777217) : (t = t.memoizedProps, t !== n && tl(e), Dt(e), wf(
          e,
          a,
          t,
          n,
          l
        )), null;
      case 27:
        if (hn(e), l = nt.current, a = e.type, t !== null && e.stateNode != null)
          t.memoizedProps !== n && tl(e);
        else {
          if (!n) {
            if (e.stateNode === null)
              throw Error(f(166));
            return Dt(e), null;
          }
          t = K.current, wn(e) ? sr(e) : (t = qm(a, n, l), e.stateNode = t, tl(e));
        }
        return Dt(e), null;
      case 5:
        if (hn(e), a = e.type, t !== null && e.stateNode != null)
          t.memoizedProps !== n && tl(e);
        else {
          if (!n) {
            if (e.stateNode === null)
              throw Error(f(166));
            return Dt(e), null;
          }
          if (u = K.current, wn(e))
            sr(e);
          else {
            var s = vi(
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
            u[Wt] = e, u[ne] = n;
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
            t: switch (Pt(u, a, n), a) {
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
            n && tl(e);
          }
        }
        return Dt(e), wf(
          e,
          e.type,
          t === null ? null : t.memoizedProps,
          e.pendingProps,
          l
        ), null;
      case 6:
        if (t && e.stateNode != null)
          t.memoizedProps !== n && tl(e);
        else {
          if (typeof n != "string" && e.stateNode === null)
            throw Error(f(166));
          if (t = nt.current, wn(e)) {
            if (t = e.stateNode, l = e.memoizedProps, n = null, a = Ft, a !== null)
              switch (a.tag) {
                case 27:
                case 5:
                  n = a.memoizedProps;
              }
            t[Wt] = e, t = !!(t.nodeValue === l || n !== null && n.suppressHydrationWarning === !0 || zm(t.nodeValue, l)), t || yl(e, !0);
          } else
            t = vi(t).createTextNode(
              n
            ), t[Wt] = e, e.stateNode = t;
        }
        return Dt(e), null;
      case 31:
        if (l = e.memoizedState, t === null || t.memoizedState !== null) {
          if (n = wn(e), l !== null) {
            if (t === null) {
              if (!n) throw Error(f(318));
              if (t = e.memoizedState, t = t !== null ? t.dehydrated : null, !t) throw Error(f(557));
              t[Wt] = e;
            } else
              Fl(), (e.flags & 128) === 0 && (e.memoizedState = null), e.flags |= 4;
            Dt(e), t = !1;
          } else
            l = Xc(), t !== null && t.memoizedState !== null && (t.memoizedState.hydrationErrors = l), t = !0;
          if (!t)
            return e.flags & 256 ? (ye(e), e) : (ye(e), null);
          if ((e.flags & 128) !== 0)
            throw Error(f(558));
        }
        return Dt(e), null;
      case 13:
        if (n = e.memoizedState, t === null || t.memoizedState !== null && t.memoizedState.dehydrated !== null) {
          if (a = wn(e), n !== null && n.dehydrated !== null) {
            if (t === null) {
              if (!a) throw Error(f(318));
              if (a = e.memoizedState, a = a !== null ? a.dehydrated : null, !a) throw Error(f(317));
              a[Wt] = e;
            } else
              Fl(), (e.flags & 128) === 0 && (e.memoizedState = null), e.flags |= 4;
            Dt(e), a = !1;
          } else
            a = Xc(), t !== null && t.memoizedState !== null && (t.memoizedState.hydrationErrors = a), a = !0;
          if (!a)
            return e.flags & 256 ? (ye(e), e) : (ye(e), null);
        }
        return ye(e), (e.flags & 128) !== 0 ? (e.lanes = l, e) : (l = n !== null, t = t !== null && t.memoizedState !== null, l && (n = e.child, a = null, n.alternate !== null && n.alternate.memoizedState !== null && n.alternate.memoizedState.cachePool !== null && (a = n.alternate.memoizedState.cachePool.pool), u = null, n.memoizedState !== null && n.memoizedState.cachePool !== null && (u = n.memoizedState.cachePool.pool), u !== a && (n.flags |= 2048)), l !== t && l && (e.child.flags |= 8192), Pu(e, e.updateQueue), Dt(e), null);
      case 4:
        return Nt(), t === null && to(e.stateNode.containerInfo), Dt(e), null;
      case 10:
        return Fe(e.type), Dt(e), null;
      case 19:
        if (U(Bt), n = e.memoizedState, n === null) return Dt(e), null;
        if (a = (e.flags & 128) !== 0, u = n.rendering, u === null)
          if (a) Na(n, !1);
          else {
            if (Ht !== 0 || t !== null && (t.flags & 128) !== 0)
              for (t = e.child; t !== null; ) {
                if (u = ju(t), u !== null) {
                  for (e.flags |= 128, Na(n, !1), t = u.updateQueue, e.updateQueue = t, Pu(e, t), e.subtreeFlags = 0, t = l, l = e.child; l !== null; )
                    ur(l, t), l = l.sibling;
                  return j(
                    Bt,
                    Bt.current & 1 | 2
                  ), yt && ke(e, n.treeForkCount), e.child;
                }
                t = t.sibling;
              }
            n.tail !== null && se() > ai && (e.flags |= 128, a = !0, Na(n, !1), e.lanes = 4194304);
          }
        else {
          if (!a)
            if (t = ju(u), t !== null) {
              if (e.flags |= 128, a = !0, t = t.updateQueue, e.updateQueue = t, Pu(e, t), Na(n, !0), n.tail === null && n.tailMode === "hidden" && !u.alternate && !yt)
                return Dt(e), null;
            } else
              2 * se() - n.renderingStartTime > ai && l !== 536870912 && (e.flags |= 128, a = !0, Na(n, !1), e.lanes = 4194304);
          n.isBackwards ? (u.sibling = e.child, e.child = u) : (t = n.last, t !== null ? t.sibling = u : e.child = u, n.last = u);
        }
        return n.tail !== null ? (t = n.tail, n.rendering = t, n.tail = t.sibling, n.renderingStartTime = se(), t.sibling = null, l = Bt.current, j(
          Bt,
          a ? l & 1 | 2 : l & 1
        ), yt && ke(e, n.treeForkCount), t) : (Dt(e), null);
      case 22:
      case 23:
        return ye(e), tf(), n = e.memoizedState !== null, t !== null ? t.memoizedState !== null !== n && (e.flags |= 8192) : n && (e.flags |= 8192), n ? (l & 536870912) !== 0 && (e.flags & 128) === 0 && (Dt(e), e.subtreeFlags & 6 && (e.flags |= 8192)) : Dt(e), l = e.updateQueue, l !== null && Pu(e, l.retryQueue), l = null, t !== null && t.memoizedState !== null && t.memoizedState.cachePool !== null && (l = t.memoizedState.cachePool.pool), n = null, e.memoizedState !== null && e.memoizedState.cachePool !== null && (n = e.memoizedState.cachePool.pool), n !== l && (e.flags |= 2048), t !== null && U(Pl), null;
      case 24:
        return l = null, t !== null && (l = t.memoizedState.cache), e.memoizedState.cache !== l && (e.flags |= 2048), Fe(Yt), Dt(e), null;
      case 25:
        return null;
      case 30:
        return null;
    }
    throw Error(f(156, e.tag));
  }
  function R0(t, e) {
    switch (Yc(e), e.tag) {
      case 1:
        return t = e.flags, t & 65536 ? (e.flags = t & -65537 | 128, e) : null;
      case 3:
        return Fe(Yt), Nt(), t = e.flags, (t & 65536) !== 0 && (t & 128) === 0 ? (e.flags = t & -65537 | 128, e) : null;
      case 26:
      case 27:
      case 5:
        return hn(e), null;
      case 31:
        if (e.memoizedState !== null) {
          if (ye(e), e.alternate === null)
            throw Error(f(340));
          Fl();
        }
        return t = e.flags, t & 65536 ? (e.flags = t & -65537 | 128, e) : null;
      case 13:
        if (ye(e), t = e.memoizedState, t !== null && t.dehydrated !== null) {
          if (e.alternate === null)
            throw Error(f(340));
          Fl();
        }
        return t = e.flags, t & 65536 ? (e.flags = t & -65537 | 128, e) : null;
      case 19:
        return U(Bt), null;
      case 4:
        return Nt(), null;
      case 10:
        return Fe(e.type), null;
      case 22:
      case 23:
        return ye(e), tf(), t !== null && U(Pl), t = e.flags, t & 65536 ? (e.flags = t & -65537 | 128, e) : null;
      case 24:
        return Fe(Yt), null;
      case 25:
        return null;
      default:
        return null;
    }
  }
  function wd(t, e) {
    switch (Yc(e), e.tag) {
      case 3:
        Fe(Yt), Nt();
        break;
      case 26:
      case 27:
      case 5:
        hn(e);
        break;
      case 4:
        Nt();
        break;
      case 31:
        e.memoizedState !== null && ye(e);
        break;
      case 13:
        ye(e);
        break;
      case 19:
        U(Bt);
        break;
      case 10:
        Fe(e.type);
        break;
      case 22:
      case 23:
        ye(e), tf(), t !== null && U(Pl);
        break;
      case 24:
        Fe(Yt);
    }
  }
  function Ua(t, e) {
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
  function Al(t, e, l) {
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
              } catch (x) {
                Et(
                  a,
                  y,
                  x
                );
              }
            }
          }
          n = n.next;
        } while (n !== u);
      }
    } catch (x) {
      Et(e, e.return, x);
    }
  }
  function Nd(t) {
    var e = t.updateQueue;
    if (e !== null) {
      var l = t.stateNode;
      try {
        Ar(e, l);
      } catch (n) {
        Et(t, t.return, n);
      }
    }
  }
  function Ud(t, e, l) {
    l.props = an(
      t.type,
      t.memoizedProps
    ), l.state = t.memoizedState;
    try {
      l.componentWillUnmount();
    } catch (n) {
      Et(t, e, n);
    }
  }
  function Ha(t, e) {
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
  function Le(t, e) {
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
  function Hd(t) {
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
  function Nf(t, e, l) {
    try {
      var n = t.stateNode;
      K0(n, t.type, l, e), n[ne] = e;
    } catch (a) {
      Et(t, t.return, a);
    }
  }
  function Bd(t) {
    return t.tag === 5 || t.tag === 3 || t.tag === 26 || t.tag === 27 && xl(t.type) || t.tag === 4;
  }
  function Uf(t) {
    t: for (; ; ) {
      for (; t.sibling === null; ) {
        if (t.return === null || Bd(t.return)) return null;
        t = t.return;
      }
      for (t.sibling.return = t.return, t = t.sibling; t.tag !== 5 && t.tag !== 6 && t.tag !== 18; ) {
        if (t.tag === 27 && xl(t.type) || t.flags & 2 || t.child === null || t.tag === 4) continue t;
        t.child.return = t, t = t.child;
      }
      if (!(t.flags & 2)) return t.stateNode;
    }
  }
  function Hf(t, e, l) {
    var n = t.tag;
    if (n === 5 || n === 6)
      t = t.stateNode, e ? (l.nodeType === 9 ? l.body : l.nodeName === "HTML" ? l.ownerDocument.body : l).insertBefore(t, e) : (e = l.nodeType === 9 ? l.body : l.nodeName === "HTML" ? l.ownerDocument.body : l, e.appendChild(t), l = l._reactRootContainer, l != null || e.onclick !== null || (e.onclick = Ze));
    else if (n !== 4 && (n === 27 && xl(t.type) && (l = t.stateNode, e = null), t = t.child, t !== null))
      for (Hf(t, e, l), t = t.sibling; t !== null; )
        Hf(t, e, l), t = t.sibling;
  }
  function ti(t, e, l) {
    var n = t.tag;
    if (n === 5 || n === 6)
      t = t.stateNode, e ? l.insertBefore(t, e) : l.appendChild(t);
    else if (n !== 4 && (n === 27 && xl(t.type) && (l = t.stateNode), t = t.child, t !== null))
      for (ti(t, e, l), t = t.sibling; t !== null; )
        ti(t, e, l), t = t.sibling;
  }
  function Ld(t) {
    var e = t.stateNode, l = t.memoizedProps;
    try {
      for (var n = t.type, a = e.attributes; a.length; )
        e.removeAttributeNode(a[0]);
      Pt(e, n, l), e[Wt] = t, e[ne] = l;
    } catch (u) {
      Et(t, t.return, u);
    }
  }
  var el = !1, Gt = !1, Bf = !1, qd = typeof WeakSet == "function" ? WeakSet : Set, Kt = null;
  function O0(t, e) {
    if (t = t.containerInfo, no = Ei, t = Fs(t), Mc(t)) {
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
            var s = 0, d = -1, y = -1, A = 0, x = 0, N = t, O = null;
            e: for (; ; ) {
              for (var M; N !== l || a !== 0 && N.nodeType !== 3 || (d = s + a), N !== u || n !== 0 && N.nodeType !== 3 || (y = s + n), N.nodeType === 3 && (s += N.nodeValue.length), (M = N.firstChild) !== null; )
                O = N, N = M;
              for (; ; ) {
                if (N === t) break e;
                if (O === l && ++A === a && (d = s), O === u && ++x === n && (y = s), (M = N.nextSibling) !== null) break;
                N = O, O = N.parentNode;
              }
              N = M;
            }
            l = d === -1 || y === -1 ? null : { start: d, end: y };
          } else l = null;
        }
      l = l || { start: 0, end: 0 };
    } else l = null;
    for (ao = { focusedElem: t, selectionRange: l }, Ei = !1, Kt = e; Kt !== null; )
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
                  var J = an(
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
                  co(t);
                else if (l === 1)
                  switch (t.nodeName) {
                    case "HEAD":
                    case "HTML":
                    case "BODY":
                      co(t);
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
  function Yd(t, e, l) {
    var n = l.flags;
    switch (l.tag) {
      case 0:
      case 11:
      case 15:
        nl(t, l), n & 4 && Ua(5, l);
        break;
      case 1:
        if (nl(t, l), n & 4)
          if (t = l.stateNode, e === null)
            try {
              t.componentDidMount();
            } catch (s) {
              Et(l, l.return, s);
            }
          else {
            var a = an(
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
        n & 64 && Nd(l), n & 512 && Ha(l, l.return);
        break;
      case 3:
        if (nl(t, l), n & 64 && (t = l.updateQueue, t !== null)) {
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
            Ar(t, e);
          } catch (s) {
            Et(l, l.return, s);
          }
        }
        break;
      case 27:
        e === null && n & 4 && Ld(l);
      case 26:
      case 5:
        nl(t, l), e === null && n & 4 && Hd(l), n & 512 && Ha(l, l.return);
        break;
      case 12:
        nl(t, l);
        break;
      case 31:
        nl(t, l), n & 4 && Gd(t, l);
        break;
      case 13:
        nl(t, l), n & 4 && Qd(t, l), n & 64 && (t = l.memoizedState, t !== null && (t = t.dehydrated, t !== null && (l = U0.bind(
          null,
          l
        ), tg(t, l))));
        break;
      case 22:
        if (n = l.memoizedState !== null || el, !n) {
          e = e !== null && e.memoizedState !== null || Gt, a = el;
          var u = Gt;
          el = n, (Gt = e) && !u ? al(
            t,
            l,
            (l.subtreeFlags & 8772) !== 0
          ) : nl(t, l), el = a, Gt = u;
        }
        break;
      case 30:
        break;
      default:
        nl(t, l);
    }
  }
  function jd(t) {
    var e = t.alternate;
    e !== null && (t.alternate = null, jd(e)), t.child = null, t.deletions = null, t.sibling = null, t.tag === 5 && (e = t.stateNode, e !== null && rc(e)), t.stateNode = null, t.return = null, t.dependencies = null, t.memoizedProps = null, t.memoizedState = null, t.pendingProps = null, t.stateNode = null, t.updateQueue = null;
  }
  var wt = null, ue = !1;
  function ll(t, e, l) {
    for (l = l.child; l !== null; )
      Xd(t, e, l), l = l.sibling;
  }
  function Xd(t, e, l) {
    if (re && typeof re.onCommitFiberUnmount == "function")
      try {
        re.onCommitFiberUnmount(ua, l);
      } catch {
      }
    switch (l.tag) {
      case 26:
        Gt || Le(l, e), ll(
          t,
          e,
          l
        ), l.memoizedState ? l.memoizedState.count-- : l.stateNode && (l = l.stateNode, l.parentNode.removeChild(l));
        break;
      case 27:
        Gt || Le(l, e);
        var n = wt, a = ue;
        xl(l.type) && (wt = l.stateNode, ue = !1), ll(
          t,
          e,
          l
        ), Va(l.stateNode), wt = n, ue = a;
        break;
      case 5:
        Gt || Le(l, e);
      case 6:
        if (n = wt, a = ue, wt = null, ll(
          t,
          e,
          l
        ), wt = n, ue = a, wt !== null)
          if (ue)
            try {
              (wt.nodeType === 9 ? wt.body : wt.nodeName === "HTML" ? wt.ownerDocument.body : wt).removeChild(l.stateNode);
            } catch (u) {
              Et(
                l,
                e,
                u
              );
            }
          else
            try {
              wt.removeChild(l.stateNode);
            } catch (u) {
              Et(
                l,
                e,
                u
              );
            }
        break;
      case 18:
        wt !== null && (ue ? (t = wt, Nm(
          t.nodeType === 9 ? t.body : t.nodeName === "HTML" ? t.ownerDocument.body : t,
          l.stateNode
        ), Pn(t)) : Nm(wt, l.stateNode));
        break;
      case 4:
        n = wt, a = ue, wt = l.stateNode.containerInfo, ue = !0, ll(
          t,
          e,
          l
        ), wt = n, ue = a;
        break;
      case 0:
      case 11:
      case 14:
      case 15:
        Al(2, l, e), Gt || Al(4, l, e), ll(
          t,
          e,
          l
        );
        break;
      case 1:
        Gt || (Le(l, e), n = l.stateNode, typeof n.componentWillUnmount == "function" && Ud(
          l,
          e,
          n
        )), ll(
          t,
          e,
          l
        );
        break;
      case 21:
        ll(
          t,
          e,
          l
        );
        break;
      case 22:
        Gt = (n = Gt) || l.memoizedState !== null, ll(
          t,
          e,
          l
        ), Gt = n;
        break;
      default:
        ll(
          t,
          e,
          l
        );
    }
  }
  function Gd(t, e) {
    if (e.memoizedState === null && (t = e.alternate, t !== null && (t = t.memoizedState, t !== null))) {
      t = t.dehydrated;
      try {
        Pn(t);
      } catch (l) {
        Et(e, e.return, l);
      }
    }
  }
  function Qd(t, e) {
    if (e.memoizedState === null && (t = e.alternate, t !== null && (t = t.memoizedState, t !== null && (t = t.dehydrated, t !== null))))
      try {
        Pn(t);
      } catch (l) {
        Et(e, e.return, l);
      }
  }
  function _0(t) {
    switch (t.tag) {
      case 31:
      case 13:
      case 19:
        var e = t.stateNode;
        return e === null && (e = t.stateNode = new qd()), e;
      case 22:
        return t = t.stateNode, e = t._retryCache, e === null && (e = t._retryCache = new qd()), e;
      default:
        throw Error(f(435, t.tag));
    }
  }
  function ei(t, e) {
    var l = _0(t);
    e.forEach(function(n) {
      if (!l.has(n)) {
        l.add(n);
        var a = H0.bind(null, t, n);
        n.then(a, a);
      }
    });
  }
  function ie(t, e) {
    var l = e.deletions;
    if (l !== null)
      for (var n = 0; n < l.length; n++) {
        var a = l[n], u = t, s = e, d = s;
        t: for (; d !== null; ) {
          switch (d.tag) {
            case 27:
              if (xl(d.type)) {
                wt = d.stateNode, ue = !1;
                break t;
              }
              break;
            case 5:
              wt = d.stateNode, ue = !1;
              break t;
            case 3:
            case 4:
              wt = d.stateNode.containerInfo, ue = !0;
              break t;
          }
          d = d.return;
        }
        if (wt === null) throw Error(f(160));
        Xd(u, s, a), wt = null, ue = !1, u = a.alternate, u !== null && (u.return = null), a.return = null;
      }
    if (e.subtreeFlags & 13886)
      for (e = e.child; e !== null; )
        Vd(e, t), e = e.sibling;
  }
  var Ne = null;
  function Vd(t, e) {
    var l = t.alternate, n = t.flags;
    switch (t.tag) {
      case 0:
      case 11:
      case 14:
      case 15:
        ie(e, t), ce(t), n & 4 && (Al(3, t, t.return), Ua(3, t), Al(5, t, t.return));
        break;
      case 1:
        ie(e, t), ce(t), n & 512 && (Gt || l === null || Le(l, l.return)), n & 64 && el && (t = t.updateQueue, t !== null && (n = t.callbacks, n !== null && (l = t.shared.hiddenCallbacks, t.shared.hiddenCallbacks = l === null ? n : l.concat(n))));
        break;
      case 26:
        var a = Ne;
        if (ie(e, t), ce(t), n & 512 && (Gt || l === null || Le(l, l.return)), n & 4) {
          var u = l !== null ? l.memoizedState : null;
          if (n = t.memoizedState, l === null)
            if (n === null)
              if (t.stateNode === null) {
                t: {
                  n = t.type, l = t.memoizedProps, a = a.ownerDocument || a;
                  e: switch (n) {
                    case "title":
                      u = a.getElementsByTagName("title")[0], (!u || u[fa] || u[Wt] || u.namespaceURI === "http://www.w3.org/2000/svg" || u.hasAttribute("itemprop")) && (u = a.createElement(n), a.head.insertBefore(
                        u,
                        a.querySelector("head > title")
                      )), Pt(u, n, l), u[Wt] = t, Zt(u), n = u;
                      break t;
                    case "link":
                      var s = Vm(
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
                      u = a.createElement(n), Pt(u, n, l), a.head.appendChild(u);
                      break;
                    case "meta":
                      if (s = Vm(
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
                      u = a.createElement(n), Pt(u, n, l), a.head.appendChild(u);
                      break;
                    default:
                      throw Error(f(468, n));
                  }
                  u[Wt] = t, Zt(u), n = u;
                }
                t.stateNode = n;
              } else
                Zm(
                  a,
                  t.type,
                  t.stateNode
                );
            else
              t.stateNode = Qm(
                a,
                n,
                t.memoizedProps
              );
          else
            u !== n ? (u === null ? l.stateNode !== null && (l = l.stateNode, l.parentNode.removeChild(l)) : u.count--, n === null ? Zm(
              a,
              t.type,
              t.stateNode
            ) : Qm(
              a,
              n,
              t.memoizedProps
            )) : n === null && t.stateNode !== null && Nf(
              t,
              t.memoizedProps,
              l.memoizedProps
            );
        }
        break;
      case 27:
        ie(e, t), ce(t), n & 512 && (Gt || l === null || Le(l, l.return)), l !== null && n & 4 && Nf(
          t,
          t.memoizedProps,
          l.memoizedProps
        );
        break;
      case 5:
        if (ie(e, t), ce(t), n & 512 && (Gt || l === null || Le(l, l.return)), t.flags & 32) {
          a = t.stateNode;
          try {
            Tn(a, "");
          } catch (J) {
            Et(t, t.return, J);
          }
        }
        n & 4 && t.stateNode != null && (a = t.memoizedProps, Nf(
          t,
          a,
          l !== null ? l.memoizedProps : a
        )), n & 1024 && (Bf = !0);
        break;
      case 6:
        if (ie(e, t), ce(t), n & 4) {
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
        if (gi = null, a = Ne, Ne = hi(e.containerInfo), ie(e, t), Ne = a, ce(t), n & 4 && l !== null && l.memoizedState.isDehydrated)
          try {
            Pn(e.containerInfo);
          } catch (J) {
            Et(t, t.return, J);
          }
        Bf && (Bf = !1, Zd(t));
        break;
      case 4:
        n = Ne, Ne = hi(
          t.stateNode.containerInfo
        ), ie(e, t), ce(t), Ne = n;
        break;
      case 12:
        ie(e, t), ce(t);
        break;
      case 31:
        ie(e, t), ce(t), n & 4 && (n = t.updateQueue, n !== null && (t.updateQueue = null, ei(t, n)));
        break;
      case 13:
        ie(e, t), ce(t), t.child.flags & 8192 && t.memoizedState !== null != (l !== null && l.memoizedState !== null) && (ni = se()), n & 4 && (n = t.updateQueue, n !== null && (t.updateQueue = null, ei(t, n)));
        break;
      case 22:
        a = t.memoizedState !== null;
        var y = l !== null && l.memoizedState !== null, A = el, x = Gt;
        if (el = A || a, Gt = x || y, ie(e, t), Gt = x, el = A, ce(t), n & 8192)
          t: for (e = t.stateNode, e._visibility = a ? e._visibility & -2 : e._visibility | 1, a && (l === null || y || el || Gt || un(t)), l = null, e = t; ; ) {
            if (e.tag === 5 || e.tag === 26) {
              if (l === null) {
                y = l = e;
                try {
                  if (u = y.stateNode, a)
                    s = u.style, typeof s.setProperty == "function" ? s.setProperty("display", "none", "important") : s.display = "none";
                  else {
                    d = y.stateNode;
                    var N = y.memoizedProps.style, O = N != null && N.hasOwnProperty("display") ? N.display : null;
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
                  var M = y.stateNode;
                  a ? Um(M, !0) : Um(y.stateNode, !1);
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
        n & 4 && (n = t.updateQueue, n !== null && (l = n.retryQueue, l !== null && (n.retryQueue = null, ei(t, l))));
        break;
      case 19:
        ie(e, t), ce(t), n & 4 && (n = t.updateQueue, n !== null && (t.updateQueue = null, ei(t, n)));
        break;
      case 30:
        break;
      case 21:
        break;
      default:
        ie(e, t), ce(t);
    }
  }
  function ce(t) {
    var e = t.flags;
    if (e & 2) {
      try {
        for (var l, n = t.return; n !== null; ) {
          if (Bd(n)) {
            l = n;
            break;
          }
          n = n.return;
        }
        if (l == null) throw Error(f(160));
        switch (l.tag) {
          case 27:
            var a = l.stateNode, u = Uf(t);
            ti(t, u, a);
            break;
          case 5:
            var s = l.stateNode;
            l.flags & 32 && (Tn(s, ""), l.flags &= -33);
            var d = Uf(t);
            ti(t, d, s);
            break;
          case 3:
          case 4:
            var y = l.stateNode.containerInfo, A = Uf(t);
            Hf(
              t,
              A,
              y
            );
            break;
          default:
            throw Error(f(161));
        }
      } catch (x) {
        Et(t, t.return, x);
      }
      t.flags &= -3;
    }
    e & 4096 && (t.flags &= -4097);
  }
  function Zd(t) {
    if (t.subtreeFlags & 1024)
      for (t = t.child; t !== null; ) {
        var e = t;
        Zd(e), e.tag === 5 && e.flags & 1024 && e.stateNode.reset(), t = t.sibling;
      }
  }
  function nl(t, e) {
    if (e.subtreeFlags & 8772)
      for (e = e.child; e !== null; )
        Yd(t, e.alternate, e), e = e.sibling;
  }
  function un(t) {
    for (t = t.child; t !== null; ) {
      var e = t;
      switch (e.tag) {
        case 0:
        case 11:
        case 14:
        case 15:
          Al(4, e, e.return), un(e);
          break;
        case 1:
          Le(e, e.return);
          var l = e.stateNode;
          typeof l.componentWillUnmount == "function" && Ud(
            e,
            e.return,
            l
          ), un(e);
          break;
        case 27:
          Va(e.stateNode);
        case 26:
        case 5:
          Le(e, e.return), un(e);
          break;
        case 22:
          e.memoizedState === null && un(e);
          break;
        case 30:
          un(e);
          break;
        default:
          un(e);
      }
      t = t.sibling;
    }
  }
  function al(t, e, l) {
    for (l = l && (e.subtreeFlags & 8772) !== 0, e = e.child; e !== null; ) {
      var n = e.alternate, a = t, u = e, s = u.flags;
      switch (u.tag) {
        case 0:
        case 11:
        case 15:
          al(
            a,
            u,
            l
          ), Ua(4, u);
          break;
        case 1:
          if (al(
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
                  Tr(y[a], d);
            } catch (A) {
              Et(n, n.return, A);
            }
          }
          l && s & 64 && Nd(u), Ha(u, u.return);
          break;
        case 27:
          Ld(u);
        case 26:
        case 5:
          al(
            a,
            u,
            l
          ), l && n === null && s & 4 && Hd(u), Ha(u, u.return);
          break;
        case 12:
          al(
            a,
            u,
            l
          );
          break;
        case 31:
          al(
            a,
            u,
            l
          ), l && s & 4 && Gd(a, u);
          break;
        case 13:
          al(
            a,
            u,
            l
          ), l && s & 4 && Qd(a, u);
          break;
        case 22:
          u.memoizedState === null && al(
            a,
            u,
            l
          ), Ha(u, u.return);
          break;
        case 30:
          break;
        default:
          al(
            a,
            u,
            l
          );
      }
      e = e.sibling;
    }
  }
  function Lf(t, e) {
    var l = null;
    t !== null && t.memoizedState !== null && t.memoizedState.cachePool !== null && (l = t.memoizedState.cachePool.pool), t = null, e.memoizedState !== null && e.memoizedState.cachePool !== null && (t = e.memoizedState.cachePool.pool), t !== l && (t != null && t.refCount++, l != null && Ea(l));
  }
  function qf(t, e) {
    t = null, e.alternate !== null && (t = e.alternate.memoizedState.cache), e = e.memoizedState.cache, e !== t && (e.refCount++, t != null && Ea(t));
  }
  function Ue(t, e, l, n) {
    if (e.subtreeFlags & 10256)
      for (e = e.child; e !== null; )
        Kd(
          t,
          e,
          l,
          n
        ), e = e.sibling;
  }
  function Kd(t, e, l, n) {
    var a = e.flags;
    switch (e.tag) {
      case 0:
      case 11:
      case 15:
        Ue(
          t,
          e,
          l,
          n
        ), a & 2048 && Ua(9, e);
        break;
      case 1:
        Ue(
          t,
          e,
          l,
          n
        );
        break;
      case 3:
        Ue(
          t,
          e,
          l,
          n
        ), a & 2048 && (t = null, e.alternate !== null && (t = e.alternate.memoizedState.cache), e = e.memoizedState.cache, e !== t && (e.refCount++, t != null && Ea(t)));
        break;
      case 12:
        if (a & 2048) {
          Ue(
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
          Ue(
            t,
            e,
            l,
            n
          );
        break;
      case 31:
        Ue(
          t,
          e,
          l,
          n
        );
        break;
      case 13:
        Ue(
          t,
          e,
          l,
          n
        );
        break;
      case 23:
        break;
      case 22:
        u = e.stateNode, s = e.alternate, e.memoizedState !== null ? u._visibility & 2 ? Ue(
          t,
          e,
          l,
          n
        ) : Ba(t, e) : u._visibility & 2 ? Ue(
          t,
          e,
          l,
          n
        ) : (u._visibility |= 2, Gn(
          t,
          e,
          l,
          n,
          (e.subtreeFlags & 10256) !== 0 || !1
        )), a & 2048 && Lf(s, e);
        break;
      case 24:
        Ue(
          t,
          e,
          l,
          n
        ), a & 2048 && qf(e.alternate, e);
        break;
      default:
        Ue(
          t,
          e,
          l,
          n
        );
    }
  }
  function Gn(t, e, l, n, a) {
    for (a = a && ((e.subtreeFlags & 10256) !== 0 || !1), e = e.child; e !== null; ) {
      var u = t, s = e, d = l, y = n, A = s.flags;
      switch (s.tag) {
        case 0:
        case 11:
        case 15:
          Gn(
            u,
            s,
            d,
            y,
            a
          ), Ua(8, s);
          break;
        case 23:
          break;
        case 22:
          var x = s.stateNode;
          s.memoizedState !== null ? x._visibility & 2 ? Gn(
            u,
            s,
            d,
            y,
            a
          ) : Ba(
            u,
            s
          ) : (x._visibility |= 2, Gn(
            u,
            s,
            d,
            y,
            a
          )), a && A & 2048 && Lf(
            s.alternate,
            s
          );
          break;
        case 24:
          Gn(
            u,
            s,
            d,
            y,
            a
          ), a && A & 2048 && qf(s.alternate, s);
          break;
        default:
          Gn(
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
  function Ba(t, e) {
    if (e.subtreeFlags & 10256)
      for (e = e.child; e !== null; ) {
        var l = t, n = e, a = n.flags;
        switch (n.tag) {
          case 22:
            Ba(l, n), a & 2048 && Lf(
              n.alternate,
              n
            );
            break;
          case 24:
            Ba(l, n), a & 2048 && qf(n.alternate, n);
            break;
          default:
            Ba(l, n);
        }
        e = e.sibling;
      }
  }
  var La = 8192;
  function Qn(t, e, l) {
    if (t.subtreeFlags & La)
      for (t = t.child; t !== null; )
        Jd(
          t,
          e,
          l
        ), t = t.sibling;
  }
  function Jd(t, e, l) {
    switch (t.tag) {
      case 26:
        Qn(
          t,
          e,
          l
        ), t.flags & La && t.memoizedState !== null && dg(
          l,
          Ne,
          t.memoizedState,
          t.memoizedProps
        );
        break;
      case 5:
        Qn(
          t,
          e,
          l
        );
        break;
      case 3:
      case 4:
        var n = Ne;
        Ne = hi(t.stateNode.containerInfo), Qn(
          t,
          e,
          l
        ), Ne = n;
        break;
      case 22:
        t.memoizedState === null && (n = t.alternate, n !== null && n.memoizedState !== null ? (n = La, La = 16777216, Qn(
          t,
          e,
          l
        ), La = n) : Qn(
          t,
          e,
          l
        ));
        break;
      default:
        Qn(
          t,
          e,
          l
        );
    }
  }
  function kd(t) {
    var e = t.alternate;
    if (e !== null && (t = e.child, t !== null)) {
      e.child = null;
      do
        e = t.sibling, t.sibling = null, t = e;
      while (t !== null);
    }
  }
  function qa(t) {
    var e = t.deletions;
    if ((t.flags & 16) !== 0) {
      if (e !== null)
        for (var l = 0; l < e.length; l++) {
          var n = e[l];
          Kt = n, Fd(
            n,
            t
          );
        }
      kd(t);
    }
    if (t.subtreeFlags & 10256)
      for (t = t.child; t !== null; )
        Wd(t), t = t.sibling;
  }
  function Wd(t) {
    switch (t.tag) {
      case 0:
      case 11:
      case 15:
        qa(t), t.flags & 2048 && Al(9, t, t.return);
        break;
      case 3:
        qa(t);
        break;
      case 12:
        qa(t);
        break;
      case 22:
        var e = t.stateNode;
        t.memoizedState !== null && e._visibility & 2 && (t.return === null || t.return.tag !== 13) ? (e._visibility &= -3, li(t)) : qa(t);
        break;
      default:
        qa(t);
    }
  }
  function li(t) {
    var e = t.deletions;
    if ((t.flags & 16) !== 0) {
      if (e !== null)
        for (var l = 0; l < e.length; l++) {
          var n = e[l];
          Kt = n, Fd(
            n,
            t
          );
        }
      kd(t);
    }
    for (t = t.child; t !== null; ) {
      switch (e = t, e.tag) {
        case 0:
        case 11:
        case 15:
          Al(8, e, e.return), li(e);
          break;
        case 22:
          l = e.stateNode, l._visibility & 2 && (l._visibility &= -3, li(e));
          break;
        default:
          li(e);
      }
      t = t.sibling;
    }
  }
  function Fd(t, e) {
    for (; Kt !== null; ) {
      var l = Kt;
      switch (l.tag) {
        case 0:
        case 11:
        case 15:
          Al(8, l, e);
          break;
        case 23:
        case 22:
          if (l.memoizedState !== null && l.memoizedState.cachePool !== null) {
            var n = l.memoizedState.cachePool.pool;
            n != null && n.refCount++;
          }
          break;
        case 24:
          Ea(l.memoizedState.cache);
      }
      if (n = l.child, n !== null) n.return = l, Kt = n;
      else
        t: for (l = t; Kt !== null; ) {
          n = Kt;
          var a = n.sibling, u = n.return;
          if (jd(n), n === l) {
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
  var z0 = {
    getCacheForType: function(t) {
      var e = $t(Yt), l = e.data.get(t);
      return l === void 0 && (l = t(), e.data.set(t, l)), l;
    },
    cacheSignal: function() {
      return $t(Yt).controller.signal;
    }
  }, M0 = typeof WeakMap == "function" ? WeakMap : Map, St = 0, Mt = null, st = null, vt = 0, pt = 0, ge = null, Rl = !1, Vn = !1, Yf = !1, ul = 0, Ht = 0, Ol = 0, cn = 0, jf = 0, be = 0, Zn = 0, Ya = null, fe = null, Xf = !1, ni = 0, $d = 0, ai = 1 / 0, ui = null, _l = null, Qt = 0, zl = null, Kn = null, il = 0, Gf = 0, Qf = null, Id = null, ja = 0, Vf = null;
  function Se() {
    return (St & 2) !== 0 && vt !== 0 ? vt & -vt : R.T !== null ? Ff() : vs();
  }
  function Pd() {
    if (be === 0)
      if ((vt & 536870912) === 0 || yt) {
        var t = mu;
        mu <<= 1, (mu & 3932160) === 0 && (mu = 262144), be = t;
      } else be = 536870912;
    return t = he.current, t !== null && (t.flags |= 32), be;
  }
  function oe(t, e, l) {
    (t === Mt && (pt === 2 || pt === 9) || t.cancelPendingCommit !== null) && (Jn(t, 0), Ml(
      t,
      vt,
      be,
      !1
    )), ca(t, l), ((St & 2) === 0 || t !== Mt) && (t === Mt && ((St & 2) === 0 && (cn |= l), Ht === 4 && Ml(
      t,
      vt,
      be,
      !1
    )), qe(t));
  }
  function tm(t, e, l) {
    if ((St & 6) !== 0) throw Error(f(327));
    var n = !l && (e & 127) === 0 && (e & t.expiredLanes) === 0 || ia(t, e), a = n ? D0(t, e) : Kf(t, e, !0), u = n;
    do {
      if (a === 0) {
        Vn && !n && Ml(t, e, 0, !1);
        break;
      } else {
        if (l = t.current.alternate, u && !C0(l)) {
          a = Kf(t, e, !1), u = !1;
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
              a = Ya;
              var y = d.current.memoizedState.isDehydrated;
              if (y && (Jn(d, s).flags |= 256), s = Kf(
                d,
                s,
                !1
              ), s !== 2) {
                if (Yf && !y) {
                  d.errorRecoveryDisabledLanes |= u, cn |= u, a = 4;
                  break t;
                }
                u = fe, fe = a, u !== null && (fe === null ? fe = u : fe.push.apply(
                  fe,
                  u
                ));
              }
              a = s;
            }
            if (u = !1, a !== 2) continue;
          }
        }
        if (a === 1) {
          Jn(t, 0), Ml(t, e, 0, !0);
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
              Ml(
                n,
                e,
                be,
                !Rl
              );
              break t;
            case 2:
              fe = null;
              break;
            case 3:
            case 5:
              break;
            default:
              throw Error(f(329));
          }
          if ((e & 62914560) === e && (a = ni + 300 - se(), 10 < a)) {
            if (Ml(
              n,
              e,
              be,
              !Rl
            ), hu(n, 0, !0) !== 0) break t;
            il = e, n.timeoutHandle = Dm(
              em.bind(
                null,
                n,
                l,
                fe,
                ui,
                Xf,
                e,
                be,
                cn,
                Zn,
                Rl,
                u,
                "Throttled",
                -0,
                0
              ),
              a
            );
            break t;
          }
          em(
            n,
            l,
            fe,
            ui,
            Xf,
            e,
            be,
            cn,
            Zn,
            Rl,
            u,
            null,
            -0,
            0
          );
        }
      }
      break;
    } while (!0);
    qe(t);
  }
  function em(t, e, l, n, a, u, s, d, y, A, x, N, O, M) {
    if (t.timeoutHandle = -1, N = e.subtreeFlags, N & 8192 || (N & 16785408) === 16785408) {
      N = {
        stylesheets: null,
        count: 0,
        imgCount: 0,
        imgBytes: 0,
        suspenseyImages: [],
        waitingForImages: !0,
        waitingForViewTransition: !1,
        unsuspend: Ze
      }, Jd(
        e,
        u,
        N
      );
      var J = (u & 62914560) === u ? ni - se() : (u & 4194048) === u ? $d - se() : 0;
      if (J = mg(
        N,
        J
      ), J !== null) {
        il = u, t.cancelPendingCommit = J(
          om.bind(
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
            x,
            N,
            null,
            O,
            M
          )
        ), Ml(t, u, s, !A);
        return;
      }
    }
    om(
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
  function C0(t) {
    for (var e = t; ; ) {
      var l = e.tag;
      if ((l === 0 || l === 11 || l === 15) && e.flags & 16384 && (l = e.updateQueue, l !== null && (l = l.stores, l !== null)))
        for (var n = 0; n < l.length; n++) {
          var a = l[n], u = a.getSnapshot;
          a = a.value;
          try {
            if (!me(u(), a)) return !1;
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
  function Ml(t, e, l, n) {
    e &= ~jf, e &= ~cn, t.suspendedLanes |= e, t.pingedLanes &= ~e, n && (t.warmLanes |= e), n = t.expirationTimes;
    for (var a = e; 0 < a; ) {
      var u = 31 - de(a), s = 1 << u;
      n[u] = -1, a &= ~s;
    }
    l !== 0 && rs(t, l, e);
  }
  function ii() {
    return (St & 6) === 0 ? (Xa(0), !1) : !0;
  }
  function Zf() {
    if (st !== null) {
      if (pt === 0)
        var t = st.return;
      else
        t = st, We = $l = null, cf(t), Ln = null, Aa = 0, t = st;
      for (; t !== null; )
        wd(t.alternate, t), t = t.return;
      st = null;
    }
  }
  function Jn(t, e) {
    var l = t.timeoutHandle;
    l !== -1 && (t.timeoutHandle = -1, W0(l)), l = t.cancelPendingCommit, l !== null && (t.cancelPendingCommit = null, l()), il = 0, Zf(), Mt = t, st = l = Je(t.current, null), vt = e, pt = 0, ge = null, Rl = !1, Vn = ia(t, e), Yf = !1, Zn = be = jf = cn = Ol = Ht = 0, fe = Ya = null, Xf = !1, (e & 8) !== 0 && (e |= e & 32);
    var n = t.entangledLanes;
    if (n !== 0)
      for (t = t.entanglements, n &= e; 0 < n; ) {
        var a = 31 - de(n), u = 1 << a;
        e |= t[a], n &= ~u;
      }
    return ul = e, zu(), l;
  }
  function lm(t, e) {
    ct = null, R.H = Da, e === Bn || e === Hu ? (e = br(), pt = 3) : e === kc ? (e = br(), pt = 4) : pt = e === Af ? 8 : e !== null && typeof e == "object" && typeof e.then == "function" ? 6 : 1, ge = e, st === null && (Ht = 1, Wu(
      t,
      Oe(e, t.current)
    ));
  }
  function nm() {
    var t = he.current;
    return t === null ? !0 : (vt & 4194048) === vt ? Ce === null : (vt & 62914560) === vt || (vt & 536870912) !== 0 ? t === Ce : !1;
  }
  function am() {
    var t = R.H;
    return R.H = Da, t === null ? Da : t;
  }
  function um() {
    var t = R.A;
    return R.A = z0, t;
  }
  function ci() {
    Ht = 4, Rl || (vt & 4194048) !== vt && he.current !== null || (Vn = !0), (Ol & 134217727) === 0 && (cn & 134217727) === 0 || Mt === null || Ml(
      Mt,
      vt,
      be,
      !1
    );
  }
  function Kf(t, e, l) {
    var n = St;
    St |= 2;
    var a = am(), u = um();
    (Mt !== t || vt !== e) && (ui = null, Jn(t, e)), e = !1;
    var s = Ht;
    t: do
      try {
        if (pt !== 0 && st !== null) {
          var d = st, y = ge;
          switch (pt) {
            case 8:
              Zf(), s = 6;
              break t;
            case 3:
            case 2:
            case 9:
            case 6:
              he.current === null && (e = !0);
              var A = pt;
              if (pt = 0, ge = null, kn(t, d, y, A), l && Vn) {
                s = 0;
                break t;
              }
              break;
            default:
              A = pt, pt = 0, ge = null, kn(t, d, y, A);
          }
        }
        x0(), s = Ht;
        break;
      } catch (x) {
        lm(t, x);
      }
    while (!0);
    return e && t.shellSuspendCounter++, We = $l = null, St = n, R.H = a, R.A = u, st === null && (Mt = null, vt = 0, zu()), s;
  }
  function x0() {
    for (; st !== null; ) im(st);
  }
  function D0(t, e) {
    var l = St;
    St |= 2;
    var n = am(), a = um();
    Mt !== t || vt !== e ? (ui = null, ai = se() + 500, Jn(t, e)) : Vn = ia(
      t,
      e
    );
    t: do
      try {
        if (pt !== 0 && st !== null) {
          e = st;
          var u = ge;
          e: switch (pt) {
            case 1:
              pt = 0, ge = null, kn(t, e, u, 1);
              break;
            case 2:
            case 9:
              if (yr(u)) {
                pt = 0, ge = null, cm(e);
                break;
              }
              e = function() {
                pt !== 2 && pt !== 9 || Mt !== t || (pt = 7), qe(t);
              }, u.then(e, e);
              break t;
            case 3:
              pt = 7;
              break t;
            case 4:
              pt = 5;
              break t;
            case 7:
              yr(u) ? (pt = 0, ge = null, cm(e)) : (pt = 0, ge = null, kn(t, e, u, 7));
              break;
            case 5:
              var s = null;
              switch (st.tag) {
                case 26:
                  s = st.memoizedState;
                case 5:
                case 27:
                  var d = st;
                  if (s ? Km(s) : d.stateNode.complete) {
                    pt = 0, ge = null;
                    var y = d.sibling;
                    if (y !== null) st = y;
                    else {
                      var A = d.return;
                      A !== null ? (st = A, fi(A)) : st = null;
                    }
                    break e;
                  }
              }
              pt = 0, ge = null, kn(t, e, u, 5);
              break;
            case 6:
              pt = 0, ge = null, kn(t, e, u, 6);
              break;
            case 8:
              Zf(), Ht = 6;
              break t;
            default:
              throw Error(f(462));
          }
        }
        w0();
        break;
      } catch (x) {
        lm(t, x);
      }
    while (!0);
    return We = $l = null, R.H = n, R.A = a, St = l, st !== null ? 0 : (Mt = null, vt = 0, zu(), Ht);
  }
  function w0() {
    for (; st !== null && !ey(); )
      im(st);
  }
  function im(t) {
    var e = xd(t.alternate, t, ul);
    t.memoizedProps = t.pendingProps, e === null ? fi(t) : st = e;
  }
  function cm(t) {
    var e = t, l = e.alternate;
    switch (e.tag) {
      case 15:
      case 0:
        e = Rd(
          l,
          e,
          e.pendingProps,
          e.type,
          void 0,
          vt
        );
        break;
      case 11:
        e = Rd(
          l,
          e,
          e.pendingProps,
          e.type.render,
          e.ref,
          vt
        );
        break;
      case 5:
        cf(e);
      default:
        wd(l, e), e = st = ur(e, ul), e = xd(l, e, ul);
    }
    t.memoizedProps = t.pendingProps, e === null ? fi(t) : st = e;
  }
  function kn(t, e, l, n) {
    We = $l = null, cf(e), Ln = null, Aa = 0;
    var a = e.return;
    try {
      if (p0(
        t,
        a,
        e,
        l,
        vt
      )) {
        Ht = 1, Wu(
          t,
          Oe(l, t.current)
        ), st = null;
        return;
      }
    } catch (u) {
      if (a !== null) throw st = a, u;
      Ht = 1, Wu(
        t,
        Oe(l, t.current)
      ), st = null;
      return;
    }
    e.flags & 32768 ? (yt || n === 1 ? t = !0 : Vn || (vt & 536870912) !== 0 ? t = !1 : (Rl = t = !0, (n === 2 || n === 9 || n === 3 || n === 6) && (n = he.current, n !== null && n.tag === 13 && (n.flags |= 16384))), fm(e, t)) : fi(e);
  }
  function fi(t) {
    var e = t;
    do {
      if ((e.flags & 32768) !== 0) {
        fm(
          e,
          Rl
        );
        return;
      }
      t = e.return;
      var l = A0(
        e.alternate,
        e,
        ul
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
  function fm(t, e) {
    do {
      var l = R0(t.alternate, t);
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
  function om(t, e, l, n, a, u, s, d, y) {
    t.cancelPendingCommit = null;
    do
      oi();
    while (Qt !== 0);
    if ((St & 6) !== 0) throw Error(f(327));
    if (e !== null) {
      if (e === t.current) throw Error(f(177));
      if (u = e.lanes | e.childLanes, u |= Nc, ry(
        t,
        l,
        u,
        s,
        d,
        y
      ), t === Mt && (st = Mt = null, vt = 0), Kn = e, zl = t, il = l, Gf = u, Qf = a, Id = n, (e.subtreeFlags & 10256) !== 0 || (e.flags & 10256) !== 0 ? (t.callbackNode = null, t.callbackPriority = 0, B0(ru, function() {
        return vm(), null;
      })) : (t.callbackNode = null, t.callbackPriority = 0), n = (e.flags & 13878) !== 0, (e.subtreeFlags & 13878) !== 0 || n) {
        n = R.T, R.T = null, a = L.p, L.p = 2, s = St, St |= 4;
        try {
          O0(t, e, l);
        } finally {
          St = s, L.p = a, R.T = n;
        }
      }
      Qt = 1, sm(), rm(), dm();
    }
  }
  function sm() {
    if (Qt === 1) {
      Qt = 0;
      var t = zl, e = Kn, l = (e.flags & 13878) !== 0;
      if ((e.subtreeFlags & 13878) !== 0 || l) {
        l = R.T, R.T = null;
        var n = L.p;
        L.p = 2;
        var a = St;
        St |= 4;
        try {
          Vd(e, t);
          var u = ao, s = Fs(t.containerInfo), d = u.focusedElem, y = u.selectionRange;
          if (s !== d && d && d.ownerDocument && Ws(
            d.ownerDocument.documentElement,
            d
          )) {
            if (y !== null && Mc(d)) {
              var A = y.start, x = y.end;
              if (x === void 0 && (x = A), "selectionStart" in d)
                d.selectionStart = A, d.selectionEnd = Math.min(
                  x,
                  d.value.length
                );
              else {
                var N = d.ownerDocument || document, O = N && N.defaultView || window;
                if (O.getSelection) {
                  var M = O.getSelection(), J = d.textContent.length, tt = Math.min(y.start, J), Ot = y.end === void 0 ? tt : Math.min(y.end, J);
                  !M.extend && tt > Ot && (s = Ot, Ot = tt, tt = s);
                  var E = ks(
                    d,
                    tt
                  ), S = ks(
                    d,
                    Ot
                  );
                  if (E && S && (M.rangeCount !== 1 || M.anchorNode !== E.node || M.anchorOffset !== E.offset || M.focusNode !== S.node || M.focusOffset !== S.offset)) {
                    var T = N.createRange();
                    T.setStart(E.node, E.offset), M.removeAllRanges(), tt > Ot ? (M.addRange(T), M.extend(S.node, S.offset)) : (T.setEnd(S.node, S.offset), M.addRange(T));
                  }
                }
              }
            }
            for (N = [], M = d; M = M.parentNode; )
              M.nodeType === 1 && N.push({
                element: M,
                left: M.scrollLeft,
                top: M.scrollTop
              });
            for (typeof d.focus == "function" && d.focus(), d = 0; d < N.length; d++) {
              var D = N[d];
              D.element.scrollLeft = D.left, D.element.scrollTop = D.top;
            }
          }
          Ei = !!no, ao = no = null;
        } finally {
          St = a, L.p = n, R.T = l;
        }
      }
      t.current = e, Qt = 2;
    }
  }
  function rm() {
    if (Qt === 2) {
      Qt = 0;
      var t = zl, e = Kn, l = (e.flags & 8772) !== 0;
      if ((e.subtreeFlags & 8772) !== 0 || l) {
        l = R.T, R.T = null;
        var n = L.p;
        L.p = 2;
        var a = St;
        St |= 4;
        try {
          Yd(t, e.alternate, e);
        } finally {
          St = a, L.p = n, R.T = l;
        }
      }
      Qt = 3;
    }
  }
  function dm() {
    if (Qt === 4 || Qt === 3) {
      Qt = 0, ly();
      var t = zl, e = Kn, l = il, n = Id;
      (e.subtreeFlags & 10256) !== 0 || (e.flags & 10256) !== 0 ? Qt = 5 : (Qt = 0, Kn = zl = null, mm(t, t.pendingLanes));
      var a = t.pendingLanes;
      if (a === 0 && (_l = null), oc(l), e = e.stateNode, re && typeof re.onCommitFiberRoot == "function")
        try {
          re.onCommitFiberRoot(
            ua,
            e,
            void 0,
            (e.current.flags & 128) === 128
          );
        } catch {
        }
      if (n !== null) {
        e = R.T, a = L.p, L.p = 2, R.T = null;
        try {
          for (var u = t.onRecoverableError, s = 0; s < n.length; s++) {
            var d = n[s];
            u(d.value, {
              componentStack: d.stack
            });
          }
        } finally {
          R.T = e, L.p = a;
        }
      }
      (il & 3) !== 0 && oi(), qe(t), a = t.pendingLanes, (l & 261930) !== 0 && (a & 42) !== 0 ? t === Vf ? ja++ : (ja = 0, Vf = t) : ja = 0, Xa(0);
    }
  }
  function mm(t, e) {
    (t.pooledCacheLanes &= e) === 0 && (e = t.pooledCache, e != null && (t.pooledCache = null, Ea(e)));
  }
  function oi() {
    return sm(), rm(), dm(), vm();
  }
  function vm() {
    if (Qt !== 5) return !1;
    var t = zl, e = Gf;
    Gf = 0;
    var l = oc(il), n = R.T, a = L.p;
    try {
      L.p = 32 > l ? 32 : l, R.T = null, l = Qf, Qf = null;
      var u = zl, s = il;
      if (Qt = 0, Kn = zl = null, il = 0, (St & 6) !== 0) throw Error(f(331));
      var d = St;
      if (St |= 4, Wd(u.current), Kd(
        u,
        u.current,
        s,
        l
      ), St = d, Xa(0, !1), re && typeof re.onPostCommitFiberRoot == "function")
        try {
          re.onPostCommitFiberRoot(ua, u);
        } catch {
        }
      return !0;
    } finally {
      L.p = a, R.T = n, mm(t, e);
    }
  }
  function hm(t, e, l) {
    e = Oe(l, e), e = Tf(t.stateNode, e, 2), t = pl(t, e, 2), t !== null && (ca(t, 2), qe(t));
  }
  function Et(t, e, l) {
    if (t.tag === 3)
      hm(t, t, l);
    else
      for (; e !== null; ) {
        if (e.tag === 3) {
          hm(
            e,
            t,
            l
          );
          break;
        } else if (e.tag === 1) {
          var n = e.stateNode;
          if (typeof e.type.getDerivedStateFromError == "function" || typeof n.componentDidCatch == "function" && (_l === null || !_l.has(n))) {
            t = Oe(l, t), l = yd(2), n = pl(e, l, 2), n !== null && (gd(
              l,
              n,
              e,
              t
            ), ca(n, 2), qe(n));
            break;
          }
        }
        e = e.return;
      }
  }
  function Jf(t, e, l) {
    var n = t.pingCache;
    if (n === null) {
      n = t.pingCache = new M0();
      var a = /* @__PURE__ */ new Set();
      n.set(e, a);
    } else
      a = n.get(e), a === void 0 && (a = /* @__PURE__ */ new Set(), n.set(e, a));
    a.has(l) || (Yf = !0, a.add(l), t = N0.bind(null, t, e, l), e.then(t, t));
  }
  function N0(t, e, l) {
    var n = t.pingCache;
    n !== null && n.delete(e), t.pingedLanes |= t.suspendedLanes & l, t.warmLanes &= ~l, Mt === t && (vt & l) === l && (Ht === 4 || Ht === 3 && (vt & 62914560) === vt && 300 > se() - ni ? (St & 2) === 0 && Jn(t, 0) : jf |= l, Zn === vt && (Zn = 0)), qe(t);
  }
  function ym(t, e) {
    e === 0 && (e = ss()), t = kl(t, e), t !== null && (ca(t, e), qe(t));
  }
  function U0(t) {
    var e = t.memoizedState, l = 0;
    e !== null && (l = e.retryLane), ym(t, l);
  }
  function H0(t, e) {
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
    n !== null && n.delete(e), ym(t, l);
  }
  function B0(t, e) {
    return uc(t, e);
  }
  var si = null, Wn = null, kf = !1, ri = !1, Wf = !1, Cl = 0;
  function qe(t) {
    t !== Wn && t.next === null && (Wn === null ? si = Wn = t : Wn = Wn.next = t), ri = !0, kf || (kf = !0, q0());
  }
  function Xa(t, e) {
    if (!Wf && ri) {
      Wf = !0;
      do
        for (var l = !1, n = si; n !== null; ) {
          if (t !== 0) {
            var a = n.pendingLanes;
            if (a === 0) var u = 0;
            else {
              var s = n.suspendedLanes, d = n.pingedLanes;
              u = (1 << 31 - de(42 | t) + 1) - 1, u &= a & ~(s & ~d), u = u & 201326741 ? u & 201326741 | 1 : u ? u | 2 : 0;
            }
            u !== 0 && (l = !0, pm(n, u));
          } else
            u = vt, u = hu(
              n,
              n === Mt ? u : 0,
              n.cancelPendingCommit !== null || n.timeoutHandle !== -1
            ), (u & 3) === 0 || ia(n, u) || (l = !0, pm(n, u));
          n = n.next;
        }
      while (l);
      Wf = !1;
    }
  }
  function L0() {
    gm();
  }
  function gm() {
    ri = kf = !1;
    var t = 0;
    Cl !== 0 && k0() && (t = Cl);
    for (var e = se(), l = null, n = si; n !== null; ) {
      var a = n.next, u = bm(n, e);
      u === 0 ? (n.next = null, l === null ? si = a : l.next = a, a === null && (Wn = l)) : (l = n, (t !== 0 || (u & 3) !== 0) && (ri = !0)), n = a;
    }
    Qt !== 0 && Qt !== 5 || Xa(t), Cl !== 0 && (Cl = 0);
  }
  function bm(t, e) {
    for (var l = t.suspendedLanes, n = t.pingedLanes, a = t.expirationTimes, u = t.pendingLanes & -62914561; 0 < u; ) {
      var s = 31 - de(u), d = 1 << s, y = a[s];
      y === -1 ? ((d & l) === 0 || (d & n) !== 0) && (a[s] = sy(d, e)) : y <= e && (t.expiredLanes |= d), u &= ~d;
    }
    if (e = Mt, l = vt, l = hu(
      t,
      t === e ? l : 0,
      t.cancelPendingCommit !== null || t.timeoutHandle !== -1
    ), n = t.callbackNode, l === 0 || t === e && (pt === 2 || pt === 9) || t.cancelPendingCommit !== null)
      return n !== null && n !== null && ic(n), t.callbackNode = null, t.callbackPriority = 0;
    if ((l & 3) === 0 || ia(t, l)) {
      if (e = l & -l, e === t.callbackPriority) return e;
      switch (n !== null && ic(n), oc(l)) {
        case 2:
        case 8:
          l = fs;
          break;
        case 32:
          l = ru;
          break;
        case 268435456:
          l = os;
          break;
        default:
          l = ru;
      }
      return n = Sm.bind(null, t), l = uc(l, n), t.callbackPriority = e, t.callbackNode = l, e;
    }
    return n !== null && n !== null && ic(n), t.callbackPriority = 2, t.callbackNode = null, 2;
  }
  function Sm(t, e) {
    if (Qt !== 0 && Qt !== 5)
      return t.callbackNode = null, t.callbackPriority = 0, null;
    var l = t.callbackNode;
    if (oi() && t.callbackNode !== l)
      return null;
    var n = vt;
    return n = hu(
      t,
      t === Mt ? n : 0,
      t.cancelPendingCommit !== null || t.timeoutHandle !== -1
    ), n === 0 ? null : (tm(t, n, e), bm(t, se()), t.callbackNode != null && t.callbackNode === l ? Sm.bind(null, t) : null);
  }
  function pm(t, e) {
    if (oi()) return null;
    tm(t, e, !0);
  }
  function q0() {
    F0(function() {
      (St & 6) !== 0 ? uc(
        cs,
        L0
      ) : gm();
    });
  }
  function Ff() {
    if (Cl === 0) {
      var t = Un;
      t === 0 && (t = du, du <<= 1, (du & 261888) === 0 && (du = 256)), Cl = t;
    }
    return Cl;
  }
  function Em(t) {
    return t == null || typeof t == "symbol" || typeof t == "boolean" ? null : typeof t == "function" ? t : Su("" + t);
  }
  function Tm(t, e) {
    var l = e.ownerDocument.createElement("input");
    return l.name = e.name, l.value = e.value, t.id && l.setAttribute("form", t.id), e.parentNode.insertBefore(l, e), t = new FormData(t), l.parentNode.removeChild(l), t;
  }
  function Y0(t, e, l, n, a) {
    if (e === "submit" && l && l.stateNode === a) {
      var u = Em(
        (a[ne] || null).action
      ), s = n.submitter;
      s && (e = (e = s[ne] || null) ? Em(e.formAction) : s.getAttribute("formAction"), e !== null && (u = e, s = null));
      var d = new Au(
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
                if (Cl !== 0) {
                  var y = s ? Tm(a, s) : new FormData(a);
                  yf(
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
                typeof u == "function" && (d.preventDefault(), y = s ? Tm(a, s) : new FormData(a), yf(
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
  for (var $f = 0; $f < wc.length; $f++) {
    var If = wc[$f], j0 = If.toLowerCase(), X0 = If[0].toUpperCase() + If.slice(1);
    we(
      j0,
      "on" + X0
    );
  }
  we(Ps, "onAnimationEnd"), we(tr, "onAnimationIteration"), we(er, "onAnimationStart"), we("dblclick", "onDoubleClick"), we("focusin", "onFocus"), we("focusout", "onBlur"), we(n0, "onTransitionRun"), we(a0, "onTransitionStart"), we(u0, "onTransitionCancel"), we(lr, "onTransitionEnd"), pn("onMouseEnter", ["mouseout", "mouseover"]), pn("onMouseLeave", ["mouseout", "mouseover"]), pn("onPointerEnter", ["pointerout", "pointerover"]), pn("onPointerLeave", ["pointerout", "pointerover"]), Vl(
    "onChange",
    "change click focusin focusout input keydown keyup selectionchange".split(" ")
  ), Vl(
    "onSelect",
    "focusout contextmenu dragend focusin keydown keyup mousedown mouseup selectionchange".split(
      " "
    )
  ), Vl("onBeforeInput", [
    "compositionend",
    "keypress",
    "textInput",
    "paste"
  ]), Vl(
    "onCompositionEnd",
    "compositionend focusout keydown keypress keyup mousedown".split(" ")
  ), Vl(
    "onCompositionStart",
    "compositionstart focusout keydown keypress keyup mousedown".split(" ")
  ), Vl(
    "onCompositionUpdate",
    "compositionupdate focusout keydown keypress keyup mousedown".split(" ")
  );
  var Ga = "abort canplay canplaythrough durationchange emptied encrypted ended error loadeddata loadedmetadata loadstart pause play playing progress ratechange resize seeked seeking stalled suspend timeupdate volumechange waiting".split(
    " "
  ), G0 = new Set(
    "beforetoggle cancel close invalid load scroll scrollend toggle".split(" ").concat(Ga)
  );
  function Am(t, e) {
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
            } catch (x) {
              _u(x);
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
            } catch (x) {
              _u(x);
            }
            a.currentTarget = null, u = y;
          }
      }
    }
  }
  function rt(t, e) {
    var l = e[sc];
    l === void 0 && (l = e[sc] = /* @__PURE__ */ new Set());
    var n = t + "__bubble";
    l.has(n) || (Rm(e, t, 2, !1), l.add(n));
  }
  function Pf(t, e, l) {
    var n = 0;
    e && (n |= 4), Rm(
      l,
      t,
      n,
      e
    );
  }
  var di = "_reactListening" + Math.random().toString(36).slice(2);
  function to(t) {
    if (!t[di]) {
      t[di] = !0, gs.forEach(function(l) {
        l !== "selectionchange" && (G0.has(l) || Pf(l, !1, t), Pf(l, !0, t));
      });
      var e = t.nodeType === 9 ? t : t.ownerDocument;
      e === null || e[di] || (e[di] = !0, Pf("selectionchange", !1, e));
    }
  }
  function Rm(t, e, l, n) {
    switch (Pm(e)) {
      case 2:
        var a = yg;
        break;
      case 8:
        a = gg;
        break;
      default:
        a = yo;
    }
    l = a.bind(
      null,
      e,
      l,
      t
    ), a = void 0, !Sc || e !== "touchstart" && e !== "touchmove" && e !== "wheel" || (a = !0), n ? a !== void 0 ? t.addEventListener(e, l, {
      capture: !0,
      passive: a
    }) : t.addEventListener(e, l, !0) : a !== void 0 ? t.addEventListener(e, l, {
      passive: a
    }) : t.addEventListener(e, l, !1);
  }
  function eo(t, e, l, n, a) {
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
            if (s = gn(d), s === null) return;
            if (y = s.tag, y === 5 || y === 6 || y === 26 || y === 27) {
              n = u = s;
              continue t;
            }
            d = d.parentNode;
          }
        }
        n = n.return;
      }
    Cs(function() {
      var A = u, x = gc(l), N = [];
      t: {
        var O = nr.get(t);
        if (O !== void 0) {
          var M = Au, J = t;
          switch (t) {
            case "keypress":
              if (Eu(l) === 0) break t;
            case "keydown":
            case "keyup":
              M = Hy;
              break;
            case "focusin":
              J = "focus", M = Ac;
              break;
            case "focusout":
              J = "blur", M = Ac;
              break;
            case "beforeblur":
            case "afterblur":
              M = Ac;
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
              M = ws;
              break;
            case "drag":
            case "dragend":
            case "dragenter":
            case "dragexit":
            case "dragleave":
            case "dragover":
            case "dragstart":
            case "drop":
              M = Ay;
              break;
            case "touchcancel":
            case "touchend":
            case "touchmove":
            case "touchstart":
              M = qy;
              break;
            case Ps:
            case tr:
            case er:
              M = _y;
              break;
            case lr:
              M = jy;
              break;
            case "scroll":
            case "scrollend":
              M = Ey;
              break;
            case "wheel":
              M = Gy;
              break;
            case "copy":
            case "cut":
            case "paste":
              M = My;
              break;
            case "gotpointercapture":
            case "lostpointercapture":
            case "pointercancel":
            case "pointerdown":
            case "pointermove":
            case "pointerout":
            case "pointerover":
            case "pointerup":
              M = Us;
              break;
            case "toggle":
            case "beforetoggle":
              M = Vy;
          }
          var tt = (e & 4) !== 0, Ot = !tt && (t === "scroll" || t === "scrollend"), E = tt ? O !== null ? O + "Capture" : null : O;
          tt = [];
          for (var S = A, T; S !== null; ) {
            var D = S;
            if (T = D.stateNode, D = D.tag, D !== 5 && D !== 26 && D !== 27 || T === null || E === null || (D = sa(S, E), D != null && tt.push(
              Qa(S, D, T)
            )), Ot) break;
            S = S.return;
          }
          0 < tt.length && (O = new M(
            O,
            J,
            null,
            l,
            x
          ), N.push({ event: O, listeners: tt }));
        }
      }
      if ((e & 7) === 0) {
        t: {
          if (O = t === "mouseover" || t === "pointerover", M = t === "mouseout" || t === "pointerout", O && l !== yc && (J = l.relatedTarget || l.fromElement) && (gn(J) || J[yn]))
            break t;
          if ((M || O) && (O = x.window === x ? x : (O = x.ownerDocument) ? O.defaultView || O.parentWindow : window, M ? (J = l.relatedTarget || l.toElement, M = A, J = J ? gn(J) : null, J !== null && (Ot = m(J), tt = J.tag, J !== Ot || tt !== 5 && tt !== 27 && tt !== 6) && (J = null)) : (M = null, J = A), M !== J)) {
            if (tt = ws, D = "onMouseLeave", E = "onMouseEnter", S = "mouse", (t === "pointerout" || t === "pointerover") && (tt = Us, D = "onPointerLeave", E = "onPointerEnter", S = "pointer"), Ot = M == null ? O : oa(M), T = J == null ? O : oa(J), O = new tt(
              D,
              S + "leave",
              M,
              l,
              x
            ), O.target = Ot, O.relatedTarget = T, D = null, gn(x) === A && (tt = new tt(
              E,
              S + "enter",
              J,
              l,
              x
            ), tt.target = T, tt.relatedTarget = Ot, D = tt), Ot = D, M && J)
              e: {
                for (tt = Q0, E = M, S = J, T = 0, D = E; D; D = tt(D))
                  T++;
                D = 0;
                for (var P = S; P; P = tt(P))
                  D++;
                for (; 0 < T - D; )
                  E = tt(E), T--;
                for (; 0 < D - T; )
                  S = tt(S), D--;
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
            M !== null && Om(
              N,
              O,
              M,
              tt,
              !1
            ), J !== null && Ot !== null && Om(
              N,
              Ot,
              J,
              tt,
              !0
            );
          }
        }
        t: {
          if (O = A ? oa(A) : window, M = O.nodeName && O.nodeName.toLowerCase(), M === "select" || M === "input" && O.type === "file")
            var gt = Gs;
          else if (js(O))
            if (Qs)
              gt = t0;
            else {
              gt = Iy;
              var W = $y;
            }
          else
            M = O.nodeName, !M || M.toLowerCase() !== "input" || O.type !== "checkbox" && O.type !== "radio" ? A && hc(A.elementType) && (gt = Gs) : gt = Py;
          if (gt && (gt = gt(t, A))) {
            Xs(
              N,
              gt,
              l,
              x
            );
            break t;
          }
          W && W(t, O, A), t === "focusout" && A && O.type === "number" && A.memoizedProps.value != null && vc(O, "number", O.value);
        }
        switch (W = A ? oa(A) : window, t) {
          case "focusin":
            (js(W) || W.contentEditable === "true") && (_n = W, Cc = A, ba = null);
            break;
          case "focusout":
            ba = Cc = _n = null;
            break;
          case "mousedown":
            xc = !0;
            break;
          case "contextmenu":
          case "mouseup":
          case "dragend":
            xc = !1, $s(N, l, x);
            break;
          case "selectionchange":
            if (l0) break;
          case "keydown":
          case "keyup":
            $s(N, l, x);
        }
        var ot;
        if (Oc)
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
          On ? qs(t, l) && (ht = "onCompositionEnd") : t === "keydown" && l.keyCode === 229 && (ht = "onCompositionStart");
        ht && (Hs && l.locale !== "ko" && (On || ht !== "onCompositionStart" ? ht === "onCompositionEnd" && On && (ot = xs()) : (ml = x, pc = "value" in ml ? ml.value : ml.textContent, On = !0)), W = mi(A, ht), 0 < W.length && (ht = new Ns(
          ht,
          t,
          null,
          l,
          x
        ), N.push({ event: ht, listeners: W }), ot ? ht.data = ot : (ot = Ys(l), ot !== null && (ht.data = ot)))), (ot = Ky ? Jy(t, l) : ky(t, l)) && (ht = mi(A, "onBeforeInput"), 0 < ht.length && (W = new Ns(
          "onBeforeInput",
          "beforeinput",
          null,
          l,
          x
        ), N.push({
          event: W,
          listeners: ht
        }), W.data = ot)), Y0(
          N,
          t,
          A,
          l,
          x
        );
      }
      Am(N, e);
    });
  }
  function Qa(t, e, l) {
    return {
      instance: t,
      listener: e,
      currentTarget: l
    };
  }
  function mi(t, e) {
    for (var l = e + "Capture", n = []; t !== null; ) {
      var a = t, u = a.stateNode;
      if (a = a.tag, a !== 5 && a !== 26 && a !== 27 || u === null || (a = sa(t, l), a != null && n.unshift(
        Qa(t, a, u)
      ), a = sa(t, e), a != null && n.push(
        Qa(t, a, u)
      )), t.tag === 3) return n;
      t = t.return;
    }
    return [];
  }
  function Q0(t) {
    if (t === null) return null;
    do
      t = t.return;
    while (t && t.tag !== 5 && t.tag !== 27);
    return t || null;
  }
  function Om(t, e, l, n, a) {
    for (var u = e._reactName, s = []; l !== null && l !== n; ) {
      var d = l, y = d.alternate, A = d.stateNode;
      if (d = d.tag, y !== null && y === n) break;
      d !== 5 && d !== 26 && d !== 27 || A === null || (y = A, a ? (A = sa(l, u), A != null && s.unshift(
        Qa(l, A, y)
      )) : a || (A = sa(l, u), A != null && s.push(
        Qa(l, A, y)
      ))), l = l.return;
    }
    s.length !== 0 && t.push({ event: e, listeners: s });
  }
  var V0 = /\r\n?/g, Z0 = /\u0000|\uFFFD/g;
  function _m(t) {
    return (typeof t == "string" ? t : "" + t).replace(V0, `
`).replace(Z0, "");
  }
  function zm(t, e) {
    return e = _m(e), _m(t) === e;
  }
  function Rt(t, e, l, n, a, u) {
    switch (l) {
      case "children":
        typeof n == "string" ? e === "body" || e === "textarea" && n === "" || Tn(t, n) : (typeof n == "number" || typeof n == "bigint") && e !== "body" && Tn(t, "" + n);
        break;
      case "className":
        gu(t, "class", n);
        break;
      case "tabIndex":
        gu(t, "tabindex", n);
        break;
      case "dir":
      case "role":
      case "viewBox":
      case "width":
      case "height":
        gu(t, l, n);
        break;
      case "style":
        zs(t, n, u);
        break;
      case "data":
        if (e !== "object") {
          gu(t, "data", n);
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
        n = Su("" + n), t.setAttribute(l, n);
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
        n = Su("" + n), t.setAttribute(l, n);
        break;
      case "onClick":
        n != null && (t.onclick = Ze);
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
        l = Su("" + n), t.setAttributeNS(
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
        rt("beforetoggle", t), rt("toggle", t), yu(t, "popover", n);
        break;
      case "xlinkActuate":
        Ve(
          t,
          "http://www.w3.org/1999/xlink",
          "xlink:actuate",
          n
        );
        break;
      case "xlinkArcrole":
        Ve(
          t,
          "http://www.w3.org/1999/xlink",
          "xlink:arcrole",
          n
        );
        break;
      case "xlinkRole":
        Ve(
          t,
          "http://www.w3.org/1999/xlink",
          "xlink:role",
          n
        );
        break;
      case "xlinkShow":
        Ve(
          t,
          "http://www.w3.org/1999/xlink",
          "xlink:show",
          n
        );
        break;
      case "xlinkTitle":
        Ve(
          t,
          "http://www.w3.org/1999/xlink",
          "xlink:title",
          n
        );
        break;
      case "xlinkType":
        Ve(
          t,
          "http://www.w3.org/1999/xlink",
          "xlink:type",
          n
        );
        break;
      case "xmlBase":
        Ve(
          t,
          "http://www.w3.org/XML/1998/namespace",
          "xml:base",
          n
        );
        break;
      case "xmlLang":
        Ve(
          t,
          "http://www.w3.org/XML/1998/namespace",
          "xml:lang",
          n
        );
        break;
      case "xmlSpace":
        Ve(
          t,
          "http://www.w3.org/XML/1998/namespace",
          "xml:space",
          n
        );
        break;
      case "is":
        yu(t, "is", n);
        break;
      case "innerText":
      case "textContent":
        break;
      default:
        (!(2 < l.length) || l[0] !== "o" && l[0] !== "O" || l[1] !== "n" && l[1] !== "N") && (l = Sy.get(l) || l, yu(t, l, n));
    }
  }
  function lo(t, e, l, n, a, u) {
    switch (l) {
      case "style":
        zs(t, n, u);
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
        typeof n == "string" ? Tn(t, n) : (typeof n == "number" || typeof n == "bigint") && Tn(t, "" + n);
        break;
      case "onScroll":
        n != null && rt("scroll", t);
        break;
      case "onScrollEnd":
        n != null && rt("scrollend", t);
        break;
      case "onClick":
        n != null && (t.onclick = Ze);
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
        if (!bs.hasOwnProperty(l))
          t: {
            if (l[0] === "o" && l[1] === "n" && (a = l.endsWith("Capture"), e = l.slice(2, a ? l.length - 7 : void 0), u = t[ne] || null, u = u != null ? u[l] : null, typeof u == "function" && t.removeEventListener(e, u, a), typeof n == "function")) {
              typeof u != "function" && u !== null && (l in t ? t[l] = null : t.hasAttribute(l) && t.removeAttribute(l)), t.addEventListener(e, n, a);
              break t;
            }
            l in t ? t[l] = n : n === !0 ? t.setAttribute(l, "") : yu(t, l, n);
          }
    }
  }
  function Pt(t, e, l) {
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
            var x = l[n];
            if (x != null)
              switch (n) {
                case "name":
                  a = x;
                  break;
                case "type":
                  s = x;
                  break;
                case "checked":
                  y = x;
                  break;
                case "defaultChecked":
                  A = x;
                  break;
                case "value":
                  u = x;
                  break;
                case "defaultValue":
                  d = x;
                  break;
                case "children":
                case "dangerouslySetInnerHTML":
                  if (x != null)
                    throw Error(f(137, e));
                  break;
                default:
                  Rt(t, e, n, x, l, null);
              }
          }
        As(
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
        e = u, l = s, t.multiple = !!n, e != null ? En(t, !!n, e, !1) : l != null && En(t, !!n, l, !0);
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
        Os(t, n, a, u);
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
        for (n = 0; n < Ga.length; n++)
          rt(Ga[n], t);
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
        if (hc(e)) {
          for (x in l)
            l.hasOwnProperty(x) && (n = l[x], n !== void 0 && lo(
              t,
              e,
              x,
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
  function K0(t, e, l, n) {
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
        var a = null, u = null, s = null, d = null, y = null, A = null, x = null;
        for (M in l) {
          var N = l[M];
          if (l.hasOwnProperty(M) && N != null)
            switch (M) {
              case "checked":
                break;
              case "value":
                break;
              case "defaultValue":
                y = N;
              default:
                n.hasOwnProperty(M) || Rt(t, e, M, null, n, N);
            }
        }
        for (var O in n) {
          var M = n[O];
          if (N = l[O], n.hasOwnProperty(O) && (M != null || N != null))
            switch (O) {
              case "type":
                u = M;
                break;
              case "name":
                a = M;
                break;
              case "checked":
                A = M;
                break;
              case "defaultChecked":
                x = M;
                break;
              case "value":
                s = M;
                break;
              case "defaultValue":
                d = M;
                break;
              case "children":
              case "dangerouslySetInnerHTML":
                if (M != null)
                  throw Error(f(137, e));
                break;
              default:
                M !== N && Rt(
                  t,
                  e,
                  O,
                  M,
                  n,
                  N
                );
            }
        }
        mc(
          t,
          s,
          d,
          y,
          A,
          x,
          u,
          a
        );
        return;
      case "select":
        M = s = d = O = null;
        for (u in l)
          if (y = l[u], l.hasOwnProperty(u) && y != null)
            switch (u) {
              case "value":
                break;
              case "multiple":
                M = y;
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
        e = d, l = s, n = M, O != null ? En(t, !!l, O, !1) : !!n != !!l && (e != null ? En(t, !!l, e, !0) : En(t, !!l, l ? [] : "", !1));
        return;
      case "textarea":
        M = O = null;
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
                M = a;
                break;
              case "children":
                break;
              case "dangerouslySetInnerHTML":
                if (a != null) throw Error(f(91));
                break;
              default:
                a !== u && Rt(t, e, s, a, n, u);
            }
        Rs(t, O, M);
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
          if (O = n[y], M = l[y], n.hasOwnProperty(y) && O !== M && (O != null || M != null))
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
                  M
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
          if (O = n[A], M = l[A], n.hasOwnProperty(A) && O !== M && (O != null || M != null))
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
                  M
                );
            }
        return;
      default:
        if (hc(e)) {
          for (var Ot in l)
            O = l[Ot], l.hasOwnProperty(Ot) && O !== void 0 && !n.hasOwnProperty(Ot) && lo(
              t,
              e,
              Ot,
              void 0,
              n,
              O
            );
          for (x in n)
            O = n[x], M = l[x], !n.hasOwnProperty(x) || O === M || O === void 0 && M === void 0 || lo(
              t,
              e,
              x,
              O,
              n,
              M
            );
          return;
        }
    }
    for (var E in l)
      O = l[E], l.hasOwnProperty(E) && O != null && !n.hasOwnProperty(E) && Rt(t, e, E, null, n, O);
    for (N in n)
      O = n[N], M = l[N], !n.hasOwnProperty(N) || O === M || O == null && M == null || Rt(t, e, N, O, n, M);
  }
  function Mm(t) {
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
  function J0() {
    if (typeof performance.getEntriesByType == "function") {
      for (var t = 0, e = 0, l = performance.getEntriesByType("resource"), n = 0; n < l.length; n++) {
        var a = l[n], u = a.transferSize, s = a.initiatorType, d = a.duration;
        if (u && d && Mm(s)) {
          for (s = 0, d = a.responseEnd, n += 1; n < l.length; n++) {
            var y = l[n], A = y.startTime;
            if (A > d) break;
            var x = y.transferSize, N = y.initiatorType;
            x && Mm(N) && (y = y.responseEnd, s += x * (y < d ? 1 : (d - A) / (y - A)));
          }
          if (--n, e += 8 * (u + s) / (a.duration / 1e3), t++, 10 < t) break;
        }
      }
      if (0 < t) return e / t / 1e6;
    }
    return navigator.connection && (t = navigator.connection.downlink, typeof t == "number") ? t : 5;
  }
  var no = null, ao = null;
  function vi(t) {
    return t.nodeType === 9 ? t : t.ownerDocument;
  }
  function Cm(t) {
    switch (t) {
      case "http://www.w3.org/2000/svg":
        return 1;
      case "http://www.w3.org/1998/Math/MathML":
        return 2;
      default:
        return 0;
    }
  }
  function xm(t, e) {
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
  function uo(t, e) {
    return t === "textarea" || t === "noscript" || typeof e.children == "string" || typeof e.children == "number" || typeof e.children == "bigint" || typeof e.dangerouslySetInnerHTML == "object" && e.dangerouslySetInnerHTML !== null && e.dangerouslySetInnerHTML.__html != null;
  }
  var io = null;
  function k0() {
    var t = window.event;
    return t && t.type === "popstate" ? t === io ? !1 : (io = t, !0) : (io = null, !1);
  }
  var Dm = typeof setTimeout == "function" ? setTimeout : void 0, W0 = typeof clearTimeout == "function" ? clearTimeout : void 0, wm = typeof Promise == "function" ? Promise : void 0, F0 = typeof queueMicrotask == "function" ? queueMicrotask : typeof wm < "u" ? function(t) {
    return wm.resolve(null).then(t).catch($0);
  } : Dm;
  function $0(t) {
    setTimeout(function() {
      throw t;
    });
  }
  function xl(t) {
    return t === "head";
  }
  function Nm(t, e) {
    var l = e, n = 0;
    do {
      var a = l.nextSibling;
      if (t.removeChild(l), a && a.nodeType === 8)
        if (l = a.data, l === "/$" || l === "/&") {
          if (n === 0) {
            t.removeChild(a), Pn(e);
            return;
          }
          n--;
        } else if (l === "$" || l === "$?" || l === "$~" || l === "$!" || l === "&")
          n++;
        else if (l === "html")
          Va(t.ownerDocument.documentElement);
        else if (l === "head") {
          l = t.ownerDocument.head, Va(l);
          for (var u = l.firstChild; u; ) {
            var s = u.nextSibling, d = u.nodeName;
            u[fa] || d === "SCRIPT" || d === "STYLE" || d === "LINK" && u.rel.toLowerCase() === "stylesheet" || l.removeChild(u), u = s;
          }
        } else
          l === "body" && Va(t.ownerDocument.body);
      l = a;
    } while (l);
    Pn(e);
  }
  function Um(t, e) {
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
  function co(t) {
    var e = t.firstChild;
    for (e && e.nodeType === 10 && (e = e.nextSibling); e; ) {
      var l = e;
      switch (e = e.nextSibling, l.nodeName) {
        case "HTML":
        case "HEAD":
        case "BODY":
          co(l), rc(l);
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
  function I0(t, e, l, n) {
    for (; t.nodeType === 1; ) {
      var a = l;
      if (t.nodeName.toLowerCase() !== e.toLowerCase()) {
        if (!n && (t.nodeName !== "INPUT" || t.type !== "hidden"))
          break;
      } else if (n) {
        if (!t[fa])
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
      if (t = xe(t.nextSibling), t === null) break;
    }
    return null;
  }
  function P0(t, e, l) {
    if (e === "") return null;
    for (; t.nodeType !== 3; )
      if ((t.nodeType !== 1 || t.nodeName !== "INPUT" || t.type !== "hidden") && !l || (t = xe(t.nextSibling), t === null)) return null;
    return t;
  }
  function Hm(t, e) {
    for (; t.nodeType !== 8; )
      if ((t.nodeType !== 1 || t.nodeName !== "INPUT" || t.type !== "hidden") && !e || (t = xe(t.nextSibling), t === null)) return null;
    return t;
  }
  function fo(t) {
    return t.data === "$?" || t.data === "$~";
  }
  function oo(t) {
    return t.data === "$!" || t.data === "$?" && t.ownerDocument.readyState !== "loading";
  }
  function tg(t, e) {
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
  function xe(t) {
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
  var so = null;
  function Bm(t) {
    t = t.nextSibling;
    for (var e = 0; t; ) {
      if (t.nodeType === 8) {
        var l = t.data;
        if (l === "/$" || l === "/&") {
          if (e === 0)
            return xe(t.nextSibling);
          e--;
        } else
          l !== "$" && l !== "$!" && l !== "$?" && l !== "$~" && l !== "&" || e++;
      }
      t = t.nextSibling;
    }
    return null;
  }
  function Lm(t) {
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
  function qm(t, e, l) {
    switch (e = vi(l), t) {
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
  function Va(t) {
    for (var e = t.attributes; e.length; )
      t.removeAttributeNode(e[0]);
    rc(t);
  }
  var De = /* @__PURE__ */ new Map(), Ym = /* @__PURE__ */ new Set();
  function hi(t) {
    return typeof t.getRootNode == "function" ? t.getRootNode() : t.nodeType === 9 ? t : t.ownerDocument;
  }
  var cl = L.d;
  L.d = {
    f: eg,
    r: lg,
    D: ng,
    C: ag,
    L: ug,
    m: ig,
    X: fg,
    S: cg,
    M: og
  };
  function eg() {
    var t = cl.f(), e = ii();
    return t || e;
  }
  function lg(t) {
    var e = bn(t);
    e !== null && e.tag === 5 && e.type === "form" ? ld(e) : cl.r(t);
  }
  var Fn = typeof document > "u" ? null : document;
  function jm(t, e, l) {
    var n = Fn;
    if (n && typeof e == "string" && e) {
      var a = Ae(e);
      a = 'link[rel="' + t + '"][href="' + a + '"]', typeof l == "string" && (a += '[crossorigin="' + l + '"]'), Ym.has(a) || (Ym.add(a), t = { rel: t, crossOrigin: l, href: e }, n.querySelector(a) === null && (e = n.createElement("link"), Pt(e, "link", t), Zt(e), n.head.appendChild(e)));
    }
  }
  function ng(t) {
    cl.D(t), jm("dns-prefetch", t, null);
  }
  function ag(t, e) {
    cl.C(t, e), jm("preconnect", t, e);
  }
  function ug(t, e, l) {
    cl.L(t, e, l);
    var n = Fn;
    if (n && t && e) {
      var a = 'link[rel="preload"][as="' + Ae(e) + '"]';
      e === "image" && l && l.imageSrcSet ? (a += '[imagesrcset="' + Ae(
        l.imageSrcSet
      ) + '"]', typeof l.imageSizes == "string" && (a += '[imagesizes="' + Ae(
        l.imageSizes
      ) + '"]')) : a += '[href="' + Ae(t) + '"]';
      var u = a;
      switch (e) {
        case "style":
          u = $n(t);
          break;
        case "script":
          u = In(t);
      }
      De.has(u) || (t = _(
        {
          rel: "preload",
          href: e === "image" && l && l.imageSrcSet ? void 0 : t,
          as: e
        },
        l
      ), De.set(u, t), n.querySelector(a) !== null || e === "style" && n.querySelector(Za(u)) || e === "script" && n.querySelector(Ka(u)) || (e = n.createElement("link"), Pt(e, "link", t), Zt(e), n.head.appendChild(e)));
    }
  }
  function ig(t, e) {
    cl.m(t, e);
    var l = Fn;
    if (l && t) {
      var n = e && typeof e.as == "string" ? e.as : "script", a = 'link[rel="modulepreload"][as="' + Ae(n) + '"][href="' + Ae(t) + '"]', u = a;
      switch (n) {
        case "audioworklet":
        case "paintworklet":
        case "serviceworker":
        case "sharedworker":
        case "worker":
        case "script":
          u = In(t);
      }
      if (!De.has(u) && (t = _({ rel: "modulepreload", href: t }, e), De.set(u, t), l.querySelector(a) === null)) {
        switch (n) {
          case "audioworklet":
          case "paintworklet":
          case "serviceworker":
          case "sharedworker":
          case "worker":
          case "script":
            if (l.querySelector(Ka(u)))
              return;
        }
        n = l.createElement("link"), Pt(n, "link", t), Zt(n), l.head.appendChild(n);
      }
    }
  }
  function cg(t, e, l) {
    cl.S(t, e, l);
    var n = Fn;
    if (n && t) {
      var a = Sn(n).hoistableStyles, u = $n(t);
      e = e || "default";
      var s = a.get(u);
      if (!s) {
        var d = { loading: 0, preload: null };
        if (s = n.querySelector(
          Za(u)
        ))
          d.loading = 5;
        else {
          t = _(
            { rel: "stylesheet", href: t, "data-precedence": e },
            l
          ), (l = De.get(u)) && ro(t, l);
          var y = s = n.createElement("link");
          Zt(y), Pt(y, "link", t), y._p = new Promise(function(A, x) {
            y.onload = A, y.onerror = x;
          }), y.addEventListener("load", function() {
            d.loading |= 1;
          }), y.addEventListener("error", function() {
            d.loading |= 2;
          }), d.loading |= 4, yi(s, e, n);
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
  function fg(t, e) {
    cl.X(t, e);
    var l = Fn;
    if (l && t) {
      var n = Sn(l).hoistableScripts, a = In(t), u = n.get(a);
      u || (u = l.querySelector(Ka(a)), u || (t = _({ src: t, async: !0 }, e), (e = De.get(a)) && mo(t, e), u = l.createElement("script"), Zt(u), Pt(u, "link", t), l.head.appendChild(u)), u = {
        type: "script",
        instance: u,
        count: 1,
        state: null
      }, n.set(a, u));
    }
  }
  function og(t, e) {
    cl.M(t, e);
    var l = Fn;
    if (l && t) {
      var n = Sn(l).hoistableScripts, a = In(t), u = n.get(a);
      u || (u = l.querySelector(Ka(a)), u || (t = _({ src: t, async: !0, type: "module" }, e), (e = De.get(a)) && mo(t, e), u = l.createElement("script"), Zt(u), Pt(u, "link", t), l.head.appendChild(u)), u = {
        type: "script",
        instance: u,
        count: 1,
        state: null
      }, n.set(a, u));
    }
  }
  function Xm(t, e, l, n) {
    var a = (a = nt.current) ? hi(a) : null;
    if (!a) throw Error(f(446));
    switch (t) {
      case "meta":
      case "title":
        return null;
      case "style":
        return typeof l.precedence == "string" && typeof l.href == "string" ? (e = $n(l.href), l = Sn(
          a
        ).hoistableStyles, n = l.get(e), n || (n = {
          type: "style",
          instance: null,
          count: 0,
          state: null
        }, l.set(e, n)), n) : { type: "void", instance: null, count: 0, state: null };
      case "link":
        if (l.rel === "stylesheet" && typeof l.href == "string" && typeof l.precedence == "string") {
          t = $n(l.href);
          var u = Sn(
            a
          ).hoistableStyles, s = u.get(t);
          if (s || (a = a.ownerDocument || a, s = {
            type: "stylesheet",
            instance: null,
            count: 0,
            state: { loading: 0, preload: null }
          }, u.set(t, s), (u = a.querySelector(
            Za(t)
          )) && !u._p && (s.instance = u, s.state.loading = 5), De.has(t) || (l = {
            rel: "preload",
            as: "style",
            href: l.href,
            crossOrigin: l.crossOrigin,
            integrity: l.integrity,
            media: l.media,
            hrefLang: l.hrefLang,
            referrerPolicy: l.referrerPolicy
          }, De.set(t, l), u || sg(
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
        return e = l.async, l = l.src, typeof l == "string" && e && typeof e != "function" && typeof e != "symbol" ? (e = In(l), l = Sn(
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
  function $n(t) {
    return 'href="' + Ae(t) + '"';
  }
  function Za(t) {
    return 'link[rel="stylesheet"][' + t + "]";
  }
  function Gm(t) {
    return _({}, t, {
      "data-precedence": t.precedence,
      precedence: null
    });
  }
  function sg(t, e, l, n) {
    t.querySelector('link[rel="preload"][as="style"][' + e + "]") ? n.loading = 1 : (e = t.createElement("link"), n.preload = e, e.addEventListener("load", function() {
      return n.loading |= 1;
    }), e.addEventListener("error", function() {
      return n.loading |= 2;
    }), Pt(e, "link", l), Zt(e), t.head.appendChild(e));
  }
  function In(t) {
    return '[src="' + Ae(t) + '"]';
  }
  function Ka(t) {
    return "script[async]" + t;
  }
  function Qm(t, e, l) {
    if (e.count++, e.instance === null)
      switch (e.type) {
        case "style":
          var n = t.querySelector(
            'style[data-href~="' + Ae(l.href) + '"]'
          );
          if (n)
            return e.instance = n, Zt(n), n;
          var a = _({}, l, {
            "data-href": l.href,
            "data-precedence": l.precedence,
            href: null,
            precedence: null
          });
          return n = (t.ownerDocument || t).createElement(
            "style"
          ), Zt(n), Pt(n, "style", a), yi(n, l.precedence, t), e.instance = n;
        case "stylesheet":
          a = $n(l.href);
          var u = t.querySelector(
            Za(a)
          );
          if (u)
            return e.state.loading |= 4, e.instance = u, Zt(u), u;
          n = Gm(l), (a = De.get(a)) && ro(n, a), u = (t.ownerDocument || t).createElement("link"), Zt(u);
          var s = u;
          return s._p = new Promise(function(d, y) {
            s.onload = d, s.onerror = y;
          }), Pt(u, "link", n), e.state.loading |= 4, yi(u, l.precedence, t), e.instance = u;
        case "script":
          return u = In(l.src), (a = t.querySelector(
            Ka(u)
          )) ? (e.instance = a, Zt(a), a) : (n = l, (a = De.get(u)) && (n = _({}, l), mo(n, a)), t = t.ownerDocument || t, a = t.createElement("script"), Zt(a), Pt(a, "link", n), t.head.appendChild(a), e.instance = a);
        case "void":
          return null;
        default:
          throw Error(f(443, e.type));
      }
    else
      e.type === "stylesheet" && (e.state.loading & 4) === 0 && (n = e.instance, e.state.loading |= 4, yi(n, l.precedence, t));
    return e.instance;
  }
  function yi(t, e, l) {
    for (var n = l.querySelectorAll(
      'link[rel="stylesheet"][data-precedence],style[data-precedence]'
    ), a = n.length ? n[n.length - 1] : null, u = a, s = 0; s < n.length; s++) {
      var d = n[s];
      if (d.dataset.precedence === e) u = d;
      else if (u !== a) break;
    }
    u ? u.parentNode.insertBefore(t, u.nextSibling) : (e = l.nodeType === 9 ? l.head : l, e.insertBefore(t, e.firstChild));
  }
  function ro(t, e) {
    t.crossOrigin == null && (t.crossOrigin = e.crossOrigin), t.referrerPolicy == null && (t.referrerPolicy = e.referrerPolicy), t.title == null && (t.title = e.title);
  }
  function mo(t, e) {
    t.crossOrigin == null && (t.crossOrigin = e.crossOrigin), t.referrerPolicy == null && (t.referrerPolicy = e.referrerPolicy), t.integrity == null && (t.integrity = e.integrity);
  }
  var gi = null;
  function Vm(t, e, l) {
    if (gi === null) {
      var n = /* @__PURE__ */ new Map(), a = gi = /* @__PURE__ */ new Map();
      a.set(l, n);
    } else
      a = gi, n = a.get(l), n || (n = /* @__PURE__ */ new Map(), a.set(l, n));
    if (n.has(t)) return n;
    for (n.set(t, null), l = l.getElementsByTagName(t), a = 0; a < l.length; a++) {
      var u = l[a];
      if (!(u[fa] || u[Wt] || t === "link" && u.getAttribute("rel") === "stylesheet") && u.namespaceURI !== "http://www.w3.org/2000/svg") {
        var s = u.getAttribute(e) || "";
        s = t + s;
        var d = n.get(s);
        d ? d.push(u) : n.set(s, [u]);
      }
    }
    return n;
  }
  function Zm(t, e, l) {
    t = t.ownerDocument || t, t.head.insertBefore(
      l,
      e === "title" ? t.querySelector("head > title") : null
    );
  }
  function rg(t, e, l) {
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
  function Km(t) {
    return !(t.type === "stylesheet" && (t.state.loading & 3) === 0);
  }
  function dg(t, e, l, n) {
    if (l.type === "stylesheet" && (typeof n.media != "string" || matchMedia(n.media).matches !== !1) && (l.state.loading & 4) === 0) {
      if (l.instance === null) {
        var a = $n(n.href), u = e.querySelector(
          Za(a)
        );
        if (u) {
          e = u._p, e !== null && typeof e == "object" && typeof e.then == "function" && (t.count++, t = bi.bind(t), e.then(t, t)), l.state.loading |= 4, l.instance = u, Zt(u);
          return;
        }
        u = e.ownerDocument || e, n = Gm(n), (a = De.get(a)) && ro(n, a), u = u.createElement("link"), Zt(u);
        var s = u;
        s._p = new Promise(function(d, y) {
          s.onload = d, s.onerror = y;
        }), Pt(u, "link", n), l.instance = u;
      }
      t.stylesheets === null && (t.stylesheets = /* @__PURE__ */ new Map()), t.stylesheets.set(l, e), (e = l.state.preload) && (l.state.loading & 3) === 0 && (t.count++, l = bi.bind(t), e.addEventListener("load", l), e.addEventListener("error", l));
    }
  }
  var vo = 0;
  function mg(t, e) {
    return t.stylesheets && t.count === 0 && pi(t, t.stylesheets), 0 < t.count || 0 < t.imgCount ? function(l) {
      var n = setTimeout(function() {
        if (t.stylesheets && pi(t, t.stylesheets), t.unsuspend) {
          var u = t.unsuspend;
          t.unsuspend = null, u();
        }
      }, 6e4 + e);
      0 < t.imgBytes && vo === 0 && (vo = 62500 * J0());
      var a = setTimeout(
        function() {
          if (t.waitingForImages = !1, t.count === 0 && (t.stylesheets && pi(t, t.stylesheets), t.unsuspend)) {
            var u = t.unsuspend;
            t.unsuspend = null, u();
          }
        },
        (t.imgBytes > vo ? 50 : 800) + e
      );
      return t.unsuspend = l, function() {
        t.unsuspend = null, clearTimeout(n), clearTimeout(a);
      };
    } : null;
  }
  function bi() {
    if (this.count--, this.count === 0 && (this.imgCount === 0 || !this.waitingForImages)) {
      if (this.stylesheets) pi(this, this.stylesheets);
      else if (this.unsuspend) {
        var t = this.unsuspend;
        this.unsuspend = null, t();
      }
    }
  }
  var Si = null;
  function pi(t, e) {
    t.stylesheets = null, t.unsuspend !== null && (t.count++, Si = /* @__PURE__ */ new Map(), e.forEach(vg, t), Si = null, bi.call(t));
  }
  function vg(t, e) {
    if (!(e.state.loading & 4)) {
      var l = Si.get(t);
      if (l) var n = l.get(null);
      else {
        l = /* @__PURE__ */ new Map(), Si.set(t, l);
        for (var a = t.querySelectorAll(
          "link[data-precedence],style[data-precedence]"
        ), u = 0; u < a.length; u++) {
          var s = a[u];
          (s.nodeName === "LINK" || s.getAttribute("media") !== "not all") && (l.set(s.dataset.precedence, s), n = s);
        }
        n && l.set(null, n);
      }
      a = e.instance, s = a.getAttribute("data-precedence"), u = l.get(s) || n, u === n && l.set(null, a), l.set(s, a), this.count++, n = bi.bind(this), a.addEventListener("load", n), a.addEventListener("error", n), u ? u.parentNode.insertBefore(a, u.nextSibling) : (t = t.nodeType === 9 ? t.head : t, t.insertBefore(a, t.firstChild)), e.state.loading |= 4;
    }
  }
  var Ja = {
    $$typeof: G,
    Provider: null,
    Consumer: null,
    _currentValue: H,
    _currentValue2: H,
    _threadCount: 0
  };
  function hg(t, e, l, n, a, u, s, d, y) {
    this.tag = 1, this.containerInfo = t, this.pingCache = this.current = this.pendingChildren = null, this.timeoutHandle = -1, this.callbackNode = this.next = this.pendingContext = this.context = this.cancelPendingCommit = null, this.callbackPriority = 0, this.expirationTimes = cc(-1), this.entangledLanes = this.shellSuspendCounter = this.errorRecoveryDisabledLanes = this.expiredLanes = this.warmLanes = this.pingedLanes = this.suspendedLanes = this.pendingLanes = 0, this.entanglements = cc(0), this.hiddenUpdates = cc(null), this.identifierPrefix = n, this.onUncaughtError = a, this.onCaughtError = u, this.onRecoverableError = s, this.pooledCache = null, this.pooledCacheLanes = 0, this.formState = y, this.incompleteTransitions = /* @__PURE__ */ new Map();
  }
  function Jm(t, e, l, n, a, u, s, d, y, A, x, N) {
    return t = new hg(
      t,
      e,
      l,
      s,
      y,
      A,
      x,
      N,
      d
    ), e = 1, u === !0 && (e |= 24), u = ve(3, null, null, e), t.current = u, u.stateNode = t, e = Zc(), e.refCount++, t.pooledCache = e, e.refCount++, u.memoizedState = {
      element: n,
      isDehydrated: l,
      cache: e
    }, Wc(u), t;
  }
  function km(t) {
    return t ? (t = Cn, t) : Cn;
  }
  function Wm(t, e, l, n, a, u) {
    a = km(a), n.context === null ? n.context = a : n.pendingContext = a, n = Sl(e), n.payload = { element: l }, u = u === void 0 ? null : u, u !== null && (n.callback = u), l = pl(t, n, e), l !== null && (oe(l, t, e), Oa(l, t, e));
  }
  function Fm(t, e) {
    if (t = t.memoizedState, t !== null && t.dehydrated !== null) {
      var l = t.retryLane;
      t.retryLane = l !== 0 && l < e ? l : e;
    }
  }
  function ho(t, e) {
    Fm(t, e), (t = t.alternate) && Fm(t, e);
  }
  function $m(t) {
    if (t.tag === 13 || t.tag === 31) {
      var e = kl(t, 67108864);
      e !== null && oe(e, t, 67108864), ho(t, 67108864);
    }
  }
  function Im(t) {
    if (t.tag === 13 || t.tag === 31) {
      var e = Se();
      e = fc(e);
      var l = kl(t, e);
      l !== null && oe(l, t, e), ho(t, e);
    }
  }
  var Ei = !0;
  function yg(t, e, l, n) {
    var a = R.T;
    R.T = null;
    var u = L.p;
    try {
      L.p = 2, yo(t, e, l, n);
    } finally {
      L.p = u, R.T = a;
    }
  }
  function gg(t, e, l, n) {
    var a = R.T;
    R.T = null;
    var u = L.p;
    try {
      L.p = 8, yo(t, e, l, n);
    } finally {
      L.p = u, R.T = a;
    }
  }
  function yo(t, e, l, n) {
    if (Ei) {
      var a = go(n);
      if (a === null)
        eo(
          t,
          e,
          n,
          Ti,
          l
        ), tv(t, n);
      else if (Sg(
        a,
        t,
        e,
        l,
        n
      ))
        n.stopPropagation();
      else if (tv(t, n), e & 4 && -1 < bg.indexOf(t)) {
        for (; a !== null; ) {
          var u = bn(a);
          if (u !== null)
            switch (u.tag) {
              case 3:
                if (u = u.stateNode, u.current.memoizedState.isDehydrated) {
                  var s = Ql(u.pendingLanes);
                  if (s !== 0) {
                    var d = u;
                    for (d.pendingLanes |= 2, d.entangledLanes |= 2; s; ) {
                      var y = 1 << 31 - de(s);
                      d.entanglements[1] |= y, s &= ~y;
                    }
                    qe(u), (St & 6) === 0 && (ai = se() + 500, Xa(0));
                  }
                }
                break;
              case 31:
              case 13:
                d = kl(u, 2), d !== null && oe(d, u, 2), ii(), ho(u, 2);
            }
          if (u = go(n), u === null && eo(
            t,
            e,
            n,
            Ti,
            l
          ), u === a) break;
          a = u;
        }
        a !== null && n.stopPropagation();
      } else
        eo(
          t,
          e,
          n,
          null,
          l
        );
    }
  }
  function go(t) {
    return t = gc(t), bo(t);
  }
  var Ti = null;
  function bo(t) {
    if (Ti = null, t = gn(t), t !== null) {
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
    return Ti = t, null;
  }
  function Pm(t) {
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
        switch (ny()) {
          case cs:
            return 2;
          case fs:
            return 8;
          case ru:
          case ay:
            return 32;
          case os:
            return 268435456;
          default:
            return 32;
        }
      default:
        return 32;
    }
  }
  var So = !1, Dl = null, wl = null, Nl = null, ka = /* @__PURE__ */ new Map(), Wa = /* @__PURE__ */ new Map(), Ul = [], bg = "mousedown mouseup touchcancel touchend touchstart auxclick dblclick pointercancel pointerdown pointerup dragend dragstart drop compositionend compositionstart keydown keypress keyup input textInput copy cut paste click change contextmenu reset".split(
    " "
  );
  function tv(t, e) {
    switch (t) {
      case "focusin":
      case "focusout":
        Dl = null;
        break;
      case "dragenter":
      case "dragleave":
        wl = null;
        break;
      case "mouseover":
      case "mouseout":
        Nl = null;
        break;
      case "pointerover":
      case "pointerout":
        ka.delete(e.pointerId);
        break;
      case "gotpointercapture":
      case "lostpointercapture":
        Wa.delete(e.pointerId);
    }
  }
  function Fa(t, e, l, n, a, u) {
    return t === null || t.nativeEvent !== u ? (t = {
      blockedOn: e,
      domEventName: l,
      eventSystemFlags: n,
      nativeEvent: u,
      targetContainers: [a]
    }, e !== null && (e = bn(e), e !== null && $m(e)), t) : (t.eventSystemFlags |= n, e = t.targetContainers, a !== null && e.indexOf(a) === -1 && e.push(a), t);
  }
  function Sg(t, e, l, n, a) {
    switch (e) {
      case "focusin":
        return Dl = Fa(
          Dl,
          t,
          e,
          l,
          n,
          a
        ), !0;
      case "dragenter":
        return wl = Fa(
          wl,
          t,
          e,
          l,
          n,
          a
        ), !0;
      case "mouseover":
        return Nl = Fa(
          Nl,
          t,
          e,
          l,
          n,
          a
        ), !0;
      case "pointerover":
        var u = a.pointerId;
        return ka.set(
          u,
          Fa(
            ka.get(u) || null,
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
          Fa(
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
  function ev(t) {
    var e = gn(t.target);
    if (e !== null) {
      var l = m(e);
      if (l !== null) {
        if (e = l.tag, e === 13) {
          if (e = v(l), e !== null) {
            t.blockedOn = e, hs(t.priority, function() {
              Im(l);
            });
            return;
          }
        } else if (e === 31) {
          if (e = p(l), e !== null) {
            t.blockedOn = e, hs(t.priority, function() {
              Im(l);
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
  function Ai(t) {
    if (t.blockedOn !== null) return !1;
    for (var e = t.targetContainers; 0 < e.length; ) {
      var l = go(t.nativeEvent);
      if (l === null) {
        l = t.nativeEvent;
        var n = new l.constructor(
          l.type,
          l
        );
        yc = n, l.target.dispatchEvent(n), yc = null;
      } else
        return e = bn(l), e !== null && $m(e), t.blockedOn = l, !1;
      e.shift();
    }
    return !0;
  }
  function lv(t, e, l) {
    Ai(t) && l.delete(e);
  }
  function pg() {
    So = !1, Dl !== null && Ai(Dl) && (Dl = null), wl !== null && Ai(wl) && (wl = null), Nl !== null && Ai(Nl) && (Nl = null), ka.forEach(lv), Wa.forEach(lv);
  }
  function Ri(t, e) {
    t.blockedOn === e && (t.blockedOn = null, So || (So = !0, i.unstable_scheduleCallback(
      i.unstable_NormalPriority,
      pg
    )));
  }
  var Oi = null;
  function nv(t) {
    Oi !== t && (Oi = t, i.unstable_scheduleCallback(
      i.unstable_NormalPriority,
      function() {
        Oi === t && (Oi = null);
        for (var e = 0; e < t.length; e += 3) {
          var l = t[e], n = t[e + 1], a = t[e + 2];
          if (typeof n != "function") {
            if (bo(n || l) === null)
              continue;
            break;
          }
          var u = bn(l);
          u !== null && (t.splice(e, 3), e -= 3, yf(
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
  function Pn(t) {
    function e(y) {
      return Ri(y, t);
    }
    Dl !== null && Ri(Dl, t), wl !== null && Ri(wl, t), Nl !== null && Ri(Nl, t), ka.forEach(e), Wa.forEach(e);
    for (var l = 0; l < Ul.length; l++) {
      var n = Ul[l];
      n.blockedOn === t && (n.blockedOn = null);
    }
    for (; 0 < Ul.length && (l = Ul[0], l.blockedOn === null); )
      ev(l), l.blockedOn === null && Ul.shift();
    if (l = (t.ownerDocument || t).$$reactFormReplay, l != null)
      for (n = 0; n < l.length; n += 3) {
        var a = l[n], u = l[n + 1], s = a[ne] || null;
        if (typeof u == "function")
          s || nv(l);
        else if (s) {
          var d = null;
          if (u && u.hasAttribute("formAction")) {
            if (a = u, s = u[ne] || null)
              d = s.formAction;
            else if (bo(a) !== null) continue;
          } else d = s.action;
          typeof d == "function" ? l[n + 1] = d : (l.splice(n, 3), n -= 3), nv(l);
        }
      }
  }
  function av() {
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
  function po(t) {
    this._internalRoot = t;
  }
  _i.prototype.render = po.prototype.render = function(t) {
    var e = this._internalRoot;
    if (e === null) throw Error(f(409));
    var l = e.current, n = Se();
    Wm(l, n, t, e, null, null);
  }, _i.prototype.unmount = po.prototype.unmount = function() {
    var t = this._internalRoot;
    if (t !== null) {
      this._internalRoot = null;
      var e = t.containerInfo;
      Wm(t.current, 2, null, t, null, null), ii(), e[yn] = null;
    }
  };
  function _i(t) {
    this._internalRoot = t;
  }
  _i.prototype.unstable_scheduleHydration = function(t) {
    if (t) {
      var e = vs();
      t = { blockedOn: null, target: t, priority: e };
      for (var l = 0; l < Ul.length && e !== 0 && e < Ul[l].priority; l++) ;
      Ul.splice(l, 0, t), l === 0 && ev(t);
    }
  };
  var uv = c.version;
  if (uv !== "19.2.4")
    throw Error(
      f(
        527,
        uv,
        "19.2.4"
      )
    );
  L.findDOMNode = function(t) {
    var e = t._reactInternals;
    if (e === void 0)
      throw typeof t.render == "function" ? Error(f(188)) : (t = Object.keys(t).join(","), Error(f(268, t)));
    return t = h(e), t = t !== null ? z(t) : null, t = t === null ? null : t.stateNode, t;
  };
  var Eg = {
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
        ua = zi.inject(
          Eg
        ), re = zi;
      } catch {
      }
  }
  return Ia.createRoot = function(t, e) {
    if (!r(t)) throw Error(f(299));
    var l = !1, n = "", a = dd, u = md, s = vd;
    return e != null && (e.unstable_strictMode === !0 && (l = !0), e.identifierPrefix !== void 0 && (n = e.identifierPrefix), e.onUncaughtError !== void 0 && (a = e.onUncaughtError), e.onCaughtError !== void 0 && (u = e.onCaughtError), e.onRecoverableError !== void 0 && (s = e.onRecoverableError)), e = Jm(
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
      av
    ), t[yn] = e.current, to(t), new po(e);
  }, Ia.hydrateRoot = function(t, e, l) {
    if (!r(t)) throw Error(f(299));
    var n = !1, a = "", u = dd, s = md, d = vd, y = null;
    return l != null && (l.unstable_strictMode === !0 && (n = !0), l.identifierPrefix !== void 0 && (a = l.identifierPrefix), l.onUncaughtError !== void 0 && (u = l.onUncaughtError), l.onCaughtError !== void 0 && (s = l.onCaughtError), l.onRecoverableError !== void 0 && (d = l.onRecoverableError), l.formState !== void 0 && (y = l.formState)), e = Jm(
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
      av
    ), e.context = km(null), l = e.current, n = Se(), n = fc(n), a = Sl(n), a.callback = null, pl(l, a, n), l = n, e.current.lanes = l, ca(e, l), qe(e), t[yn] = e.current, to(t), new _i(e);
  }, Ia.version = "19.2.4", Ia;
}
var gv;
function ub() {
  if (gv) return To.exports;
  gv = 1;
  function i() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(i);
      } catch (c) {
        console.error(c);
      }
  }
  return i(), To.exports = ab(), To.exports;
}
var ih = ub(), ki = uh();
const XS = /* @__PURE__ */ Iv(ki), Di = [];
let ib = 1, bv = !1;
function ch(i, c = !1) {
  return { id: ib++, isActive: i, bindings: /* @__PURE__ */ new Map(), modal: c };
}
function fh(i) {
  return Di.push(i), () => {
    const c = Di.indexOf(i);
    c >= 0 && Di.splice(c, 1);
  };
}
function oh(i, c, o) {
  const f = ob(c);
  let r = i.bindings.get(f);
  return r || (r = [], i.bindings.set(f, r)), r.push(o), () => {
    const m = i.bindings.get(f);
    if (!m)
      return;
    const v = m.lastIndexOf(o);
    v >= 0 && m.splice(v, 1), m.length === 0 && i.bindings.delete(f);
  };
}
const cb = {
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
function sh(i) {
  const c = cb[i.toLowerCase()];
  return c || (i.length === 1 ? i.toUpperCase() : i);
}
function rh(i, c, o, f, r) {
  return (c ? "Ctrl+" : "") + (o ? "Alt+" : "") + (f ? "Shift+" : "") + (r ? "Meta+" : "") + i;
}
function fb(i) {
  return rh(sh(i.key), i.ctrlKey, i.altKey, i.shiftKey, i.metaKey);
}
function ob(i) {
  const c = i.split("+").map((p) => p.trim()).filter(Boolean);
  let o = !1, f = !1, r = !1, m = !1, v = "";
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
        v = p;
    }
  return rh(sh(v), o, f, r, m);
}
const sb = /* @__PURE__ */ new Set([
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
function Sv(i) {
  if (!i)
    return !1;
  const c = i;
  return c.isContentEditable || c.tagName === "TEXTAREA" ? !0 : c.tagName === "INPUT" ? sb.has(c.type) : !1;
}
function rb(i) {
  if (!i)
    return !1;
  const c = i;
  return c.isContentEditable || c.tagName === "TEXTAREA";
}
const db = /* @__PURE__ */ new Set(["ArrowUp", "ArrowDown", "ArrowLeft", "ArrowRight", "Home", "End", "PageUp", "PageDown"]);
function mb(i) {
  if (!i)
    return !1;
  const c = i;
  return c.tagName === "BUTTON" || c.getAttribute("role") === "button";
}
function vb(i) {
  if (i.defaultPrevented)
    return;
  const c = document.activeElement;
  if (i.key === "Enter" && rb(c) || i.key === "Enter" && mb(c) || (db.has(i.key) || i.key === " ") && Sv(c) || (i.ctrlKey || i.metaKey) && (i.key === "a" || i.key === "A") && Sv(c))
    return;
  const o = fb(i), f = Di.slice().sort((r, m) => m.id - r.id);
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
function hb() {
  bv || (bv = !0, document.addEventListener("keydown", vb, !1));
}
const wi = [];
let yb = 1, pv = !1;
const dh = "data-tl-anchored-overlay", GS = { [dh]: "" }, gb = [
  "a[href]",
  "button:not([disabled])",
  "input:not([disabled]):not([type=hidden])",
  "select:not([disabled])",
  "textarea:not([disabled])",
  '[tabindex]:not([tabindex="-1"])'
].join(","), bb = "input:not([disabled]):not([type=hidden]),select:not([disabled]),textarea:not([disabled])";
function Ho(i, c = !1) {
  const o = i.querySelectorAll(c ? bb : gb);
  for (const f of o)
    if (f.getClientRects().length > 0 || f === document.activeElement)
      return f;
  return null;
}
function Sb() {
  let i = null;
  for (const c of wi)
    c.getElement() && (!i || c.id > i.id) && (i = c);
  return i;
}
function pb(i) {
  const c = Sb();
  if (!c)
    return;
  const o = c.getElement();
  if (!o)
    return;
  const f = i.target;
  if (f && o.contains(f) || f && f.contains(o) || f instanceof Element && f.closest(`[${dh}]`))
    return;
  const r = (c.getFocusTarget && c.getFocusTarget()) ?? Ho(o) ?? o;
  r && r !== document.activeElement && r.focus();
}
function Eb(i, c) {
  const o = { id: yb++, getElement: i, getFocusTarget: c };
  return wi.push(o), () => {
    const f = wi.indexOf(o);
    f >= 0 && wi.splice(f, 1);
  };
}
function Tb() {
  pv || (pv = !0, document.addEventListener("focusin", pb, !0));
}
class mh {
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
const vn = B.createContext(null), Ye = /* @__PURE__ */ new Map();
let vh = "", Ev = !1;
function Yl() {
  return vh + "/";
}
function Wo(i, c, o, f, r) {
  r !== void 0 && (vh = r);
  const m = f ?? "";
  Ev ? r !== void 0 && Ov(Yl() + "react-api/events?windowName=" + encodeURIComponent(m)) : (Ev = !0, Og(Yl()), Ov(Yl() + "react-api/events?windowName=" + encodeURIComponent(m))), _g(m);
  const v = document.getElementById(i);
  if (!v) {
    console.error("[TLReact] Mount point not found:", i);
    return;
  }
  const p = $v(c);
  if (!p) {
    console.error("[TLReact] Component not registered:", c);
    return;
  }
  Li(i);
  const g = new mh(o);
  o.hidden === !0 && (v.style.display = "none");
  const h = (w) => {
    g.applyPatch(w);
  };
  Io(i, h);
  const z = ih.createRoot(v);
  Ye.set(i, { root: z, store: g, sseListener: h, refs: 1 }), hh = m;
  const _ = () => {
    const w = B.useSyncExternalStore(g.subscribeStore, g.getSnapshot);
    return B.useLayoutEffect(() => {
      v.style.display = w.hidden === !0 ? "none" : "";
    }, [w.hidden]), Jt.createElement(
      vn.Provider,
      { value: { controlId: i, windowName: m, store: g } },
      Jt.createElement(p, { controlId: i, state: w })
    );
  };
  ki.flushSync(() => {
    z.render(Jt.createElement(_));
  });
}
function Ab(i, c, o) {
  Wo(i, c, o);
}
function Li(i) {
  const c = Ye.get(i);
  if (c) {
    if (c.refs > 1) {
      c.refs--;
      return;
    }
    Rh(i, c.sseListener), c.root && c.root.unmount(), Ye.delete(i);
  }
}
function Rb(i) {
  return Ye.has(i);
}
function Ob(i, c) {
  let o = Ye.get(i);
  if (!o) {
    const r = new mh(c), m = (v) => {
      r.applyPatch(v);
    };
    Io(i, m), o = { root: null, store: r, sseListener: m, refs: 0 }, Ye.set(i, o);
  }
  return o.refs++, { controlId: i, windowName: hh, store: o.store };
}
let hh = "";
function yh() {
  const i = B.useContext(vn);
  if (!i)
    throw new Error("useTLState must be used inside a TLReact-mounted component.");
  return B.useSyncExternalStore(i.store.subscribeStore, i.store.getSnapshot);
}
function gh() {
  const i = B.useContext(vn);
  if (!i)
    throw new Error("useTLCommand must be used inside a TLReact-mounted component.");
  const c = i.controlId, o = i.windowName;
  return B.useCallback(
    (f, r) => Ki(Yl() + "react-api/command", {
      controlId: c,
      command: f,
      windowName: o,
      arguments: r ?? {}
    }),
    [c, o]
  );
}
function QS() {
  const i = B.useContext(vn);
  if (!i)
    throw new Error("useTLUpload must be used inside a TLReact-mounted component.");
  const c = i.controlId, o = i.windowName;
  return B.useCallback(
    async (f) => {
      f.append("controlId", c), f.append("windowName", o);
      try {
        const r = await fetch(Yl() + "react-api/upload", {
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
function VS() {
  const i = B.useContext(vn);
  if (!i)
    throw new Error("useTLDataUrl must be used inside a TLReact-mounted component.");
  return Yl() + "react-api/data?controlId=" + encodeURIComponent(i.controlId) + "&windowName=" + encodeURIComponent(i.windowName);
}
function ZS(i) {
  const c = yh(), o = gh(), f = B.useContext(vn), r = (i == null ? void 0 : i.sendOnBlur) === !0, m = r ? 0 : (i == null ? void 0 : i.debounceMs) ?? 0, v = B.useRef(null), p = B.useRef(null), g = B.useRef(null), h = B.useCallback(
    (C) => o(ah, { value: C }),
    [o]
  ), z = B.useCallback(() => {
    g.current !== null && Lg(g.current), v.current !== null && (clearTimeout(v.current), v.current = null);
    const C = p.current;
    return C !== null ? (p.current = null, h(C.value)) : Promise.resolve();
  }, [h]), _ = B.useRef(z);
  _.current = z, g.current === null && (g.current = () => _.current());
  const w = B.useCallback(
    (C) => {
      f == null || f.store.applyPatch({ value: C }), r ? (p.current = { value: C }, sv(g.current)) : m > 0 ? (p.current = { value: C }, sv(g.current), v.current !== null && clearTimeout(v.current), v.current = setTimeout(() => {
        v.current = null, _.current();
      }, m)) : h(C);
    },
    [h, f, m, r]
  );
  return B.useEffect(() => () => {
    _.current();
  }, []), [c.value, w, z];
}
function KS() {
  const i = yh(), c = gh(), o = B.useCallback(
    (f) => {
      if (f.key !== "Enter" || f.shiftKey || f.altKey || f.ctrlKey || f.metaKey)
        return;
      const r = f.currentTarget.value;
      f.preventDefault(), c(Dg, { value: r });
    },
    [c]
  );
  return i.submitOnEnter === !0 ? o : void 0;
}
const bh = B.createContext(null), JS = ({ active: i, modal: c, children: o }) => {
  const f = Jt.useRef(i);
  f.current = i;
  const r = Jt.useMemo(
    () => ch(() => f.current ? f.current() : !0, c === !0),
    []
  );
  return Jt.useEffect(() => fh(r), [r]), Jt.createElement(bh.Provider, { value: r }, o);
};
function kS(i, c) {
  const o = B.useContext(bh), f = Jt.useRef(c);
  f.current = c;
  const r = i == null ? [] : Array.isArray(i) ? i : [i], m = r.join("|");
  Jt.useEffect(() => {
    if (!o || r.length === 0)
      return;
    const v = () => f.current(), p = r.map((g) => oh(o, g, v));
    return () => p.forEach((g) => g());
  }, [o, m]);
}
function WS(i, c) {
  const o = Jt.useRef(c);
  o.current = c, Jt.useEffect(() => {
    if (!i)
      return;
    const f = ch(() => !0);
    for (const r of Object.keys(o.current))
      oh(f, r, () => {
        var m, v;
        return (v = (m = o.current)[r]) == null ? void 0 : v.call(m);
      });
    return fh(f);
  }, [i]);
}
function FS(i, c, o = "container") {
  Jt.useEffect(() => {
    if (!i || !c.current)
      return;
    const r = document.activeElement, m = () => {
      const g = c.current;
      if (!g)
        return null;
      const h = o === "field" ? Ho(g, !0) : o === "first" ? Ho(g, !1) : null;
      return h || (g.hasAttribute("tabindex") || g.setAttribute("tabindex", "-1"), g);
    }, v = Eb(() => c.current, m), p = m();
    return p && p !== document.activeElement && p.focus(), () => {
      v(), r && r.isConnected && typeof r.focus == "function" && r.focus();
    };
  }, [i]);
}
function qi(i = document.body) {
  const c = document.body.dataset.windowName, o = document.body.dataset.contextPath, f = i.querySelectorAll("[data-react-module]");
  for (const r of f) {
    if (!r.id || Ye.has(r.id))
      continue;
    const m = r.dataset.reactModule, v = c ?? r.dataset.windowName, p = o ?? r.dataset.contextPath;
    if (!m || v === void 0 || p === void 0)
      continue;
    const g = r.dataset.reactState, h = g ? JSON.parse(g) : {};
    Wo(r.id, m, h, v, p);
  }
}
function Tv() {
  new MutationObserver((c) => {
    var o;
    for (const f of c)
      for (const r of f.removedNodes)
        if (r instanceof HTMLElement) {
          const m = r.id;
          m && Ye.has(m) && Ye.get(m).root !== null && Li(m);
          for (const [v, p] of Ye.entries())
            p.root !== null && r.querySelector("#" + CSS.escape(v)) && Li(v);
        }
    for (const f of c)
      for (const r of f.addedNodes)
        r instanceof HTMLElement && ((o = r.dataset) != null && o.reactModule ? qi(r.parentElement ?? document.body) : r.querySelector("[data-react-module]") && qi(r));
  }).observe(document.body, { childList: !0, subtree: !0 });
}
document.readyState === "loading" ? document.addEventListener("DOMContentLoaded", Tv) : Tv();
window.addEventListener("load", () => qi());
const _b = "tl-view-pick-highlight";
let Bo = !1, Fo = null, le = null;
function Sh(i) {
  let c = i;
  for (; c; ) {
    if (c instanceof HTMLElement && c.dataset.viewSource)
      return c;
    c = c.parentElement;
  }
  return null;
}
function zb(i) {
  if (!le) return;
  const c = i.getBoundingClientRect();
  le.style.display = "block", le.style.left = `${c.left}px`, le.style.top = `${c.top}px`, le.style.width = `${c.width}px`, le.style.height = `${c.height}px`;
}
function ph(i) {
  const c = Sh(i.target);
  c ? zb(c) : le && (le.style.display = "none");
}
function Eh(i) {
  i.preventDefault(), i.stopPropagation();
  const c = Sh(i.target), o = Fo;
  if ($o(), c && o) {
    const f = c.dataset.viewSource;
    fetch(Yl() + "react-api/view-pick", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ token: o, path: f })
    }).catch((r) => console.error("[TLReact] view-pick failed:", r));
  }
}
function Th(i) {
  i.key === "Escape" && (i.preventDefault(), i.stopPropagation(), $o());
}
function Mb(i) {
  Bo && $o(), Bo = !0, Fo = i, le = document.createElement("div"), le.id = _b, le.style.display = "none", document.body.appendChild(le), document.body.classList.add("tlViewPick--active"), document.addEventListener("pointermove", ph, !0), document.addEventListener("click", Eh, !0), document.addEventListener("keydown", Th, !0);
}
function $o() {
  Bo = !1, Fo = null, document.body.classList.remove("tlViewPick--active"), document.removeEventListener("pointermove", ph, !0), document.removeEventListener("click", Eh, !0), document.removeEventListener("keydown", Th, !0), le && (le.remove(), le = null);
}
function Cb(i) {
  const c = document.body.dataset.windowName ?? "";
  i.targetWindowId && i.targetWindowId !== c || Mb(i.token);
}
const au = /* @__PURE__ */ new Map();
let fn = null, Av = null, Yi = 0, eu = null, Ah = !1, Rv = !1;
const xb = 45e3, Db = 15e3;
function Ov(i) {
  if (Av = i, !Rv) {
    Rv = !0;
    const c = () => {
      Ah = !0;
    };
    window.addEventListener("beforeunload", c), window.addEventListener("pagehide", c);
  }
  eu && clearInterval(eu), _v(i), eu = setInterval(() => {
    Yi > 0 && Date.now() - Yi > xb && (console.warn("[TLReact] No heartbeat received, reconnecting SSE."), _v(Av));
  }, Db);
}
function _v(i) {
  fn && fn.close(), fn = new EventSource(i), Yi = Date.now(), nh(), fn.onmessage = (c) => {
    Yi = Date.now();
    try {
      const o = JSON.parse(c.data);
      wb(o);
    } catch (o) {
      console.error("[TLReact] Failed to parse SSE event:", o);
    }
  }, fn.onerror = () => {
    if (!Ah) {
      if (fn && fn.readyState === EventSource.CLOSED) {
        console.warn("[TLReact] SSE connection permanently closed (session lost). Reloading page."), eu && clearInterval(eu), window.location.reload();
        return;
      }
      console.warn("[TLReact] SSE connection error, will reconnect automatically.");
    }
  };
}
function Io(i, c) {
  let o = au.get(i);
  o || (o = /* @__PURE__ */ new Set(), au.set(i, o)), o.add(c);
}
function Rh(i, c) {
  const o = au.get(i);
  o && (o.delete(c), o.size === 0 && au.delete(i));
}
function Oh(i, c) {
  const o = au.get(i);
  if (o)
    for (const f of o)
      f(c);
}
function wb(i) {
  if (!Array.isArray(i) || i.length < 2) {
    console.warn("[TLReact] Unexpected SSE event format:", i);
    return;
  }
  const c = i[0], o = i[1];
  switch (c !== "Heartbeat" && console.log("[SSE] dispatch", c, o), c) {
    case "Heartbeat":
      break;
    case "StateEvent":
      Nb(o);
      break;
    case "PatchEvent":
      Ub(o);
      break;
    case "ContentReplacement":
      Bl.contentReplacement(o);
      break;
    case "ElementReplacement":
      Bl.elementReplacement(o);
      break;
    case "PropertyUpdate":
      Bl.propertyUpdate(o);
      break;
    case "CssClassUpdate":
      Bl.cssClassUpdate(o);
      break;
    case "FragmentInsertion":
      Bl.fragmentInsertion(o);
      break;
    case "RangeReplacement":
      Bl.rangeReplacement(o);
      break;
    case "JSSnipplet":
      Bl.jsSnipplet(o);
      break;
    case "FunctionCall":
      Bl.functionCall(o);
      break;
    case "I18NCacheInvalidation":
      nh();
      break;
    case "WindowOpenEvent":
      Gg(o);
      break;
    case "WindowCloseEvent":
      Qg(o);
      break;
    case "WindowFocusEvent":
      Vg(o);
      break;
    case "RouteChangeEvent":
      Fg(o);
      break;
    case "RouteVetoEvent":
      $g(o);
      break;
    case "ViewPickEvent":
      Cb(o);
      break;
    default:
      console.warn("[TLReact] Unknown SSE event type:", c);
  }
}
function Nb(i) {
  const c = JSON.parse(i.state);
  Oh(i.controlId, c);
}
function Ub(i) {
  const c = JSON.parse(i.patch);
  Oh(i.controlId, c);
}
const Bl = {
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
function Hb(i) {
  i && requestAnimationFrame(() => {
    const c = document.querySelector('[data-tl-anchor="' + CSS.escape(i) + '"]');
    c && (c.scrollIntoView({ behavior: "smooth", block: "center" }), c.classList.remove("tlAnchor--flash"), c.offsetWidth, c.classList.add("tlAnchor--flash"));
  });
}
const _h = "tlFill", Po = B.createContext(null);
function zh(i) {
  const c = B.useContext(Po), o = B.useMemo(() => ({}), []);
  B.useEffect(() => {
    if (c !== null)
      return c.setChildFill(o, i), () => c.setChildFill(o, !1);
  }, [c, i, o]);
}
function $S(i) {
  return zh(i), i ? _h : "";
}
function IS(i = !1) {
  const c = B.useRef(null), [o, f] = B.useState(!1), r = B.useMemo(() => ({
    setChildFill(v, p) {
      let g = c.current;
      g === null && (g = /* @__PURE__ */ new Set(), c.current = g), p ? g.add(v) : g.delete(v), f(g.size > 0);
    }
  }), []), m = i || o;
  return zh(m), [m ? _h : "", r];
}
const PS = ({ host: i, children: c }) => Jt.createElement(Po.Provider, { value: i }, c), tp = ({ children: i }) => Jt.createElement(Po.Provider, { value: null }, i);
var _o = { exports: {} }, Pa = {};
/**
 * @license React
 * react-jsx-runtime.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var zv;
function Bb() {
  if (zv) return Pa;
  zv = 1;
  var i = Symbol.for("react.transitional.element"), c = Symbol.for("react.fragment");
  function o(f, r, m) {
    var v = null;
    if (m !== void 0 && (v = "" + m), r.key !== void 0 && (v = "" + r.key), "key" in r) {
      m = {};
      for (var p in r)
        p !== "key" && (m[p] = r[p]);
    } else m = r;
    return r = m.ref, {
      $$typeof: i,
      type: f,
      key: v,
      ref: r !== void 0 ? r : null,
      props: m
    };
  }
  return Pa.Fragment = c, Pa.jsx = o, Pa.jsxs = o, Pa;
}
var Mv;
function Lb() {
  return Mv || (Mv = 1, _o.exports = Bb()), _o.exports;
}
var Vt = Lb();
const ep = ({ control: i }) => {
  const c = i, o = $v(c.module), f = B.useMemo(
    () => Ob(c.controlId, c.state),
    [c.controlId]
  );
  B.useEffect(() => () => Li(c.controlId), [c.controlId]);
  const r = B.useSyncExternalStore(f.store.subscribeStore, f.store.getSnapshot), m = JSON.stringify(c.state);
  return B.useEffect(() => {
    f.store.applyPatch(c.state);
  }, [m]), B.useEffect(() => {
    if (!c.viewSource)
      return;
    const v = document.getElementById(c.controlId);
    v && v.setAttribute("data-view-source", c.viewSource);
  }, [c.controlId, c.viewSource, r]), o ? /* @__PURE__ */ Vt.jsx(vn.Provider, { value: f, children: /* @__PURE__ */ Vt.jsx(o, { controlId: c.controlId, state: r }) }) : /* @__PURE__ */ Vt.jsxs("span", { children: [
    "[Component not registered: ",
    c.module,
    "]"
  ] });
};
function Wi() {
  return typeof window < "u";
}
function na(i) {
  return ts(i) ? (i.nodeName || "").toLowerCase() : "#document";
}
function pe(i) {
  var c;
  return (i == null || (c = i.ownerDocument) == null ? void 0 : c.defaultView) || window;
}
function Xe(i) {
  var c;
  return (c = (ts(i) ? i.ownerDocument : i.document) || window.document) == null ? void 0 : c.documentElement;
}
function ts(i) {
  return Wi() ? i instanceof Node || i instanceof pe(i).Node : !1;
}
function qt(i) {
  return Wi() ? i instanceof Element || i instanceof pe(i).Element : !1;
}
function Ge(i) {
  return Wi() ? i instanceof HTMLElement || i instanceof pe(i).HTMLElement : !1;
}
function Lo(i) {
  return !Wi() || typeof ShadowRoot > "u" ? !1 : i instanceof ShadowRoot || i instanceof pe(i).ShadowRoot;
}
function cu(i) {
  const {
    overflow: c,
    overflowX: o,
    overflowY: f,
    display: r
  } = Ee(i);
  return /auto|scroll|overlay|hidden|clip/.test(c + f + o) && r !== "inline" && r !== "contents";
}
function qb(i) {
  return /^(table|td|th)$/.test(na(i));
}
function Fi(i) {
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
const Yb = /transform|translate|scale|rotate|perspective|filter/, jb = /paint|layout|strict|content/, on = (i) => !!i && i !== "none";
let zo;
function es(i) {
  const c = qt(i) ? Ee(i) : i;
  return on(c.transform) || on(c.translate) || on(c.scale) || on(c.rotate) || on(c.perspective) || !$i() && (on(c.backdropFilter) || on(c.filter)) || Yb.test(c.willChange || "") || jb.test(c.contain || "");
}
function Xb(i) {
  let c = sl(i);
  for (; Ge(c) && !ol(c); ) {
    if (es(c))
      return c;
    if (Fi(c))
      return null;
    c = sl(c);
  }
  return null;
}
function $i() {
  return zo == null && (zo = typeof CSS < "u" && CSS.supports && CSS.supports("-webkit-backdrop-filter", "none")), zo;
}
function ol(i) {
  return /^(html|body|#document)$/.test(na(i));
}
function Ee(i) {
  return pe(i).getComputedStyle(i);
}
function Ii(i) {
  return qt(i) ? {
    scrollLeft: i.scrollLeft,
    scrollTop: i.scrollTop
  } : {
    scrollLeft: i.scrollX,
    scrollTop: i.scrollY
  };
}
function sl(i) {
  if (na(i) === "html")
    return i;
  const c = (
    // Step into the shadow DOM of the parent of a slotted node.
    i.assignedSlot || // DOM Element detected.
    i.parentNode || // ShadowRoot detected.
    Lo(i) && i.host || // Fallback.
    Xe(i)
  );
  return Lo(c) ? c.host : c;
}
function Mh(i) {
  const c = sl(i);
  return ol(c) ? i.ownerDocument ? i.ownerDocument.body : i.body : Ge(c) && cu(c) ? c : Mh(c);
}
function jl(i, c, o) {
  var f;
  c === void 0 && (c = []), o === void 0 && (o = !0);
  const r = Mh(i), m = r === ((f = i.ownerDocument) == null ? void 0 : f.body), v = pe(r);
  if (m) {
    const p = qo(v);
    return c.concat(v, v.visualViewport || [], cu(r) ? r : [], p && o ? jl(p) : []);
  } else
    return c.concat(r, jl(r, [], o));
}
function qo(i) {
  return i.parent && Object.getPrototypeOf(i.parent) ? i.frameElement : null;
}
const la = Math.min, rn = Math.max, ji = Math.round, Mi = Math.floor, je = (i) => ({
  x: i,
  y: i
}), Gb = {
  left: "right",
  right: "left",
  bottom: "top",
  top: "bottom"
};
function Yo(i, c, o) {
  return rn(i, la(c, o));
}
function fu(i, c) {
  return typeof i == "function" ? i(c) : i;
}
function dn(i) {
  return i.split("-")[0];
}
function ou(i) {
  return i.split("-")[1];
}
function Ch(i) {
  return i === "x" ? "y" : "x";
}
function ls(i) {
  return i === "y" ? "height" : "width";
}
function Ll(i) {
  const c = i[0];
  return c === "t" || c === "b" ? "y" : "x";
}
function ns(i) {
  return Ch(Ll(i));
}
function Qb(i, c, o) {
  o === void 0 && (o = !1);
  const f = ou(i), r = ns(i), m = ls(r);
  let v = r === "x" ? f === (o ? "end" : "start") ? "right" : "left" : f === "start" ? "bottom" : "top";
  return c.reference[m] > c.floating[m] && (v = Xi(v)), [v, Xi(v)];
}
function Vb(i) {
  const c = Xi(i);
  return [jo(i), c, jo(c)];
}
function jo(i) {
  return i.includes("start") ? i.replace("start", "end") : i.replace("end", "start");
}
const Cv = ["left", "right"], xv = ["right", "left"], Zb = ["top", "bottom"], Kb = ["bottom", "top"];
function Jb(i, c, o) {
  switch (i) {
    case "top":
    case "bottom":
      return o ? c ? xv : Cv : c ? Cv : xv;
    case "left":
    case "right":
      return c ? Zb : Kb;
    default:
      return [];
  }
}
function kb(i, c, o, f) {
  const r = ou(i);
  let m = Jb(dn(i), o === "start", f);
  return r && (m = m.map((v) => v + "-" + r), c && (m = m.concat(m.map(jo)))), m;
}
function Xi(i) {
  const c = dn(i);
  return Gb[c] + i.slice(c.length);
}
function Wb(i) {
  return {
    top: 0,
    right: 0,
    bottom: 0,
    left: 0,
    ...i
  };
}
function xh(i) {
  return typeof i != "number" ? Wb(i) : {
    top: i,
    right: i,
    bottom: i,
    left: i
  };
}
function Gi(i) {
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
var Fb = ["input:not([inert]):not([inert] *)", "select:not([inert]):not([inert] *)", "textarea:not([inert]):not([inert] *)", "a[href]:not([inert]):not([inert] *)", "button:not([inert]):not([inert] *)", "[tabindex]:not(slot):not([inert]):not([inert] *)", "audio[controls]:not([inert]):not([inert] *)", "video[controls]:not([inert]):not([inert] *)", '[contenteditable]:not([contenteditable="false"]):not([inert]):not([inert] *)', "details>summary:first-of-type:not([inert]):not([inert] *)", "details:not([inert]):not([inert] *)"], Xo = /* @__PURE__ */ Fb.join(","), Dh = typeof Element > "u", uu = Dh ? function() {
} : Element.prototype.matches || Element.prototype.msMatchesSelector || Element.prototype.webkitMatchesSelector, Qi = !Dh && Element.prototype.getRootNode ? function(i) {
  var c;
  return i == null || (c = i.getRootNode) === null || c === void 0 ? void 0 : c.call(i);
} : function(i) {
  return i == null ? void 0 : i.ownerDocument;
}, Vi = function(c, o) {
  var f;
  o === void 0 && (o = !0);
  var r = c == null || (f = c.getAttribute) === null || f === void 0 ? void 0 : f.call(c, "inert"), m = r === "" || r === "true", v = m || o && c && // closest does not exist on shadow roots, so we fall back to a manual
  // lookup upward, in case it is not defined.
  (typeof c.closest == "function" ? c.closest("[inert]") : Vi(c.parentNode));
  return v;
}, $b = function(c) {
  var o, f = c == null || (o = c.getAttribute) === null || o === void 0 ? void 0 : o.call(c, "contenteditable");
  return f === "" || f === "true";
}, Ib = function(c, o, f) {
  if (Vi(c))
    return [];
  var r = Array.prototype.slice.apply(c.querySelectorAll(Xo));
  return o && uu.call(c, Xo) && r.unshift(c), r = r.filter(f), r;
}, Go = function(c, o, f) {
  for (var r = [], m = Array.from(c); m.length; ) {
    var v = m.shift();
    if (!Vi(v, !1))
      if (v.tagName === "SLOT") {
        var p = v.assignedElements(), g = p.length ? p : v.children, h = Go(g, !0, f);
        f.flatten ? r.push.apply(r, h) : r.push({
          scopeParent: v,
          candidates: h
        });
      } else {
        var z = uu.call(v, Xo);
        z && f.filter(v) && (o || !c.includes(v)) && r.push(v);
        var _ = v.shadowRoot || // check for an undisclosed shadow
        typeof f.getShadowRoot == "function" && f.getShadowRoot(v), w = !Vi(_, !1) && (!f.shadowRootFilter || f.shadowRootFilter(v));
        if (_ && w) {
          var C = Go(_ === !0 ? v.children : _.children, !0, f);
          f.flatten ? r.push.apply(r, C) : r.push({
            scopeParent: v,
            candidates: C
          });
        } else
          m.unshift.apply(m, v.children);
      }
  }
  return r;
}, wh = function(c) {
  return !isNaN(parseInt(c.getAttribute("tabindex"), 10));
}, Nh = function(c) {
  if (!c)
    throw new Error("No node provided");
  return c.tabIndex < 0 && (/^(AUDIO|VIDEO|DETAILS)$/.test(c.tagName) || $b(c)) && !wh(c) ? 0 : c.tabIndex;
}, Pb = function(c, o) {
  var f = Nh(c);
  return f < 0 && o && !wh(c) ? 0 : f;
}, t1 = function(c, o) {
  return c.tabIndex === o.tabIndex ? c.documentOrder - o.documentOrder : c.tabIndex - o.tabIndex;
}, Uh = function(c) {
  return c.tagName === "INPUT";
}, e1 = function(c) {
  return Uh(c) && c.type === "hidden";
}, l1 = function(c) {
  var o = c.tagName === "DETAILS" && Array.prototype.slice.apply(c.children).some(function(f) {
    return f.tagName === "SUMMARY";
  });
  return o;
}, n1 = function(c, o) {
  for (var f = 0; f < c.length; f++)
    if (c[f].checked && c[f].form === o)
      return c[f];
}, a1 = function(c) {
  if (!c.name)
    return !0;
  var o = c.form || Qi(c), f = function(p) {
    return o.querySelectorAll('input[type="radio"][name="' + p + '"]');
  }, r;
  if (typeof window < "u" && typeof window.CSS < "u" && typeof window.CSS.escape == "function")
    r = f(window.CSS.escape(c.name));
  else
    try {
      r = f(c.name);
    } catch (v) {
      return console.error("Looks like you have a radio button with a name attribute containing invalid CSS selector characters and need the CSS.escape polyfill: %s", v.message), !1;
    }
  var m = n1(r, c.form);
  return !m || m === c;
}, u1 = function(c) {
  return Uh(c) && c.type === "radio";
}, i1 = function(c) {
  return u1(c) && !a1(c);
}, c1 = function(c) {
  var o, f = c && Qi(c), r = (o = f) === null || o === void 0 ? void 0 : o.host, m = !1;
  if (f && f !== c) {
    var v, p, g;
    for (m = !!((v = r) !== null && v !== void 0 && (p = v.ownerDocument) !== null && p !== void 0 && p.contains(r) || c != null && (g = c.ownerDocument) !== null && g !== void 0 && g.contains(c)); !m && r; ) {
      var h, z, _;
      f = Qi(r), r = (h = f) === null || h === void 0 ? void 0 : h.host, m = !!((z = r) !== null && z !== void 0 && (_ = z.ownerDocument) !== null && _ !== void 0 && _.contains(r));
    }
  }
  return m;
}, Dv = function(c) {
  var o = c.getBoundingClientRect(), f = o.width, r = o.height;
  return f === 0 && r === 0;
}, f1 = function(c, o) {
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
  var v = uu.call(c, "details>summary:first-of-type"), p = v ? c.parentElement : c;
  if (uu.call(p, "details:not([open]) *"))
    return !0;
  if (!f || f === "full" || // full-native can run this branch when it falls through in case
  // Element#checkVisibility is unsupported
  f === "full-native" || f === "legacy-full") {
    if (typeof r == "function") {
      for (var g = c; c; ) {
        var h = c.parentElement, z = Qi(c);
        if (h && !h.shadowRoot && r(h) === !0)
          return Dv(c);
        c.assignedSlot ? c = c.assignedSlot : !h && z !== c.ownerDocument ? c = z.host : c = h;
      }
      c = g;
    }
    if (c1(c))
      return !c.getClientRects().length;
    if (f !== "legacy-full")
      return !0;
  } else if (f === "non-zero-area")
    return Dv(c);
  return !1;
}, o1 = function(c) {
  if (/^(INPUT|BUTTON|SELECT|TEXTAREA)$/.test(c.tagName))
    for (var o = c.parentElement; o; ) {
      if (o.tagName === "FIELDSET" && o.disabled) {
        for (var f = 0; f < o.children.length; f++) {
          var r = o.children.item(f);
          if (r.tagName === "LEGEND")
            return uu.call(o, "fieldset[disabled] *") ? !0 : !r.contains(c);
        }
        return !0;
      }
      o = o.parentElement;
    }
  return !1;
}, s1 = function(c, o) {
  return !(o.disabled || e1(o) || f1(o, c) || // For a details element with a summary, the summary element gets the focus
  l1(o) || o1(o));
}, wv = function(c, o) {
  return !(i1(o) || Nh(o) < 0 || !s1(c, o));
}, r1 = function(c) {
  var o = parseInt(c.getAttribute("tabindex"), 10);
  return !!(isNaN(o) || o >= 0);
}, Hh = function(c) {
  var o = [], f = [];
  return c.forEach(function(r, m) {
    var v = !!r.scopeParent, p = v ? r.scopeParent : r, g = Pb(p, v), h = v ? Hh(r.candidates) : p;
    g === 0 ? v ? o.push.apply(o, h) : o.push(p) : f.push({
      documentOrder: m,
      tabIndex: g,
      item: r,
      isScope: v,
      content: h
    });
  }), f.sort(t1).reduce(function(r, m) {
    return m.isScope ? r.push.apply(r, m.content) : r.push(m.content), r;
  }, []).concat(o);
}, Bh = function(c, o) {
  o = o || {};
  var f;
  return o.getShadowRoot ? f = Go([c], o.includeContainer, {
    filter: wv.bind(null, o),
    flatten: !1,
    getShadowRoot: o.getShadowRoot,
    shadowRootFilter: r1
  }) : f = Ib(c, o.includeContainer, wv.bind(null, o)), Hh(f);
};
function d1() {
  return /apple/i.test(navigator.vendor);
}
const Nv = "data-floating-ui-focusable";
function m1(i) {
  let c = i.activeElement;
  for (; ((o = c) == null || (o = o.shadowRoot) == null ? void 0 : o.activeElement) != null; ) {
    var o;
    c = c.shadowRoot.activeElement;
  }
  return c;
}
function Qo(i, c) {
  if (!i || !c)
    return !1;
  const o = c.getRootNode == null ? void 0 : c.getRootNode();
  if (i.contains(c))
    return !0;
  if (o && Lo(o)) {
    let f = c;
    for (; f; ) {
      if (i === f)
        return !0;
      f = f.parentNode || f.host;
    }
  }
  return !1;
}
function tu(i) {
  return "composedPath" in i ? i.composedPath()[0] : i.target;
}
function Mo(i, c) {
  if (c == null)
    return !1;
  if ("composedPath" in i)
    return i.composedPath().includes(c);
  const o = i;
  return o.target != null && c.contains(o.target);
}
function v1(i) {
  return i.matches("html,body");
}
function iu(i) {
  return (i == null ? void 0 : i.ownerDocument) || document;
}
function h1(i) {
  return i ? i.hasAttribute(Nv) ? i : i.querySelector("[" + Nv + "]") || i : null;
}
function Ni(i, c, o) {
  return o === void 0 && (o = !0), i.filter((r) => {
    var m;
    return r.parentId === c && (!o || ((m = r.context) == null ? void 0 : m.open));
  }).flatMap((r) => [r, ...Ni(i, r.id, o)]);
}
function y1(i) {
  return "nativeEvent" in i;
}
var g1 = typeof document < "u", b1 = function() {
}, Xl = g1 ? B.useLayoutEffect : b1;
const S1 = {
  ...Pv
}, p1 = S1.useInsertionEffect, E1 = p1 || ((i) => i());
function ta(i) {
  const c = B.useRef(() => {
  });
  return E1(() => {
    c.current = i;
  }), B.useCallback(function() {
    for (var o = arguments.length, f = new Array(o), r = 0; r < o; r++)
      f[r] = arguments[r];
    return c.current == null ? void 0 : c.current(...f);
  }, []);
}
const Lh = () => ({
  getShadowRoot: !0,
  displayCheck: (
    // JSDOM does not support the `tabbable` library. To solve this we can
    // check if `ResizeObserver` is a real function (not polyfilled), which
    // determines if the current environment is JSDOM-like.
    typeof ResizeObserver == "function" && ResizeObserver.toString().includes("[native code]") ? "full" : "none"
  )
});
function qh(i, c) {
  const o = Bh(i, Lh()), f = o.length;
  if (f === 0) return;
  const r = m1(iu(i)), m = o.indexOf(r), v = m === -1 ? c === 1 ? 0 : f - 1 : m + c;
  return o[v];
}
function T1(i) {
  return qh(iu(i).body, 1) || i;
}
function A1(i) {
  return qh(iu(i).body, -1) || i;
}
function Co(i, c) {
  const o = c || i.currentTarget, f = i.relatedTarget;
  return !f || !Qo(o, f);
}
function R1(i) {
  Bh(i, Lh()).forEach((o) => {
    o.dataset.tabindex = o.getAttribute("tabindex") || "", o.setAttribute("tabindex", "-1");
  });
}
function Uv(i) {
  i.querySelectorAll("[data-tabindex]").forEach((o) => {
    const f = o.dataset.tabindex;
    delete o.dataset.tabindex, f ? o.setAttribute("tabindex", f) : o.removeAttribute("tabindex");
  });
}
function Hv(i, c, o) {
  let {
    reference: f,
    floating: r
  } = i;
  const m = Ll(c), v = ns(c), p = ls(v), g = dn(c), h = m === "y", z = f.x + f.width / 2 - r.width / 2, _ = f.y + f.height / 2 - r.height / 2, w = f[p] / 2 - r[p] / 2;
  let C;
  switch (g) {
    case "top":
      C = {
        x: z,
        y: f.y - r.height
      };
      break;
    case "bottom":
      C = {
        x: z,
        y: f.y + f.height
      };
      break;
    case "right":
      C = {
        x: f.x + f.width,
        y: _
      };
      break;
    case "left":
      C = {
        x: f.x - r.width,
        y: _
      };
      break;
    default:
      C = {
        x: f.x,
        y: f.y
      };
  }
  switch (ou(c)) {
    case "start":
      C[v] -= w * (o && h ? -1 : 1);
      break;
    case "end":
      C[v] += w * (o && h ? -1 : 1);
      break;
  }
  return C;
}
async function O1(i, c) {
  var o;
  c === void 0 && (c = {});
  const {
    x: f,
    y: r,
    platform: m,
    rects: v,
    elements: p,
    strategy: g
  } = i, {
    boundary: h = "clippingAncestors",
    rootBoundary: z = "viewport",
    elementContext: _ = "floating",
    altBoundary: w = !1,
    padding: C = 0
  } = fu(c, i), Y = xh(C), V = p[w ? _ === "floating" ? "reference" : "floating" : _], X = Gi(await m.getClippingRect({
    element: (o = await (m.isElement == null ? void 0 : m.isElement(V))) == null || o ? V : V.contextElement || await (m.getDocumentElement == null ? void 0 : m.getDocumentElement(p.floating)),
    boundary: h,
    rootBoundary: z,
    strategy: g
  })), k = _ === "floating" ? {
    x: f,
    y: r,
    width: v.floating.width,
    height: v.floating.height
  } : v.reference, G = await (m.getOffsetParent == null ? void 0 : m.getOffsetParent(p.floating)), $ = await (m.isElement == null ? void 0 : m.isElement(G)) ? await (m.getScale == null ? void 0 : m.getScale(G)) || {
    x: 1,
    y: 1
  } : {
    x: 1,
    y: 1
  }, at = Gi(m.convertOffsetParentRelativeRectToViewportRelativeRect ? await m.convertOffsetParentRelativeRectToViewportRelativeRect({
    elements: p,
    rect: k,
    offsetParent: G,
    strategy: g
  }) : k);
  return {
    top: (X.top - at.top + Y.top) / $.y,
    bottom: (at.bottom - X.bottom + Y.bottom) / $.y,
    left: (X.left - at.left + Y.left) / $.x,
    right: (at.right - X.right + Y.right) / $.x
  };
}
const _1 = 50, z1 = async (i, c, o) => {
  const {
    placement: f = "bottom",
    strategy: r = "absolute",
    middleware: m = [],
    platform: v
  } = o, p = v.detectOverflow ? v : {
    ...v,
    detectOverflow: O1
  }, g = await (v.isRTL == null ? void 0 : v.isRTL(c));
  let h = await v.getElementRects({
    reference: i,
    floating: c,
    strategy: r
  }), {
    x: z,
    y: _
  } = Hv(h, f, g), w = f, C = 0;
  const Y = {};
  for (let q = 0; q < m.length; q++) {
    const V = m[q];
    if (!V)
      continue;
    const {
      name: X,
      fn: k
    } = V, {
      x: G,
      y: $,
      data: at,
      reset: lt
    } = await k({
      x: z,
      y: _,
      initialPlacement: f,
      placement: w,
      strategy: r,
      middlewareData: Y,
      rects: h,
      platform: p,
      elements: {
        reference: i,
        floating: c
      }
    });
    z = G ?? z, _ = $ ?? _, Y[X] = {
      ...Y[X],
      ...at
    }, lt && C < _1 && (C++, typeof lt == "object" && (lt.placement && (w = lt.placement), lt.rects && (h = lt.rects === !0 ? await v.getElementRects({
      reference: i,
      floating: c,
      strategy: r
    }) : lt.rects), {
      x: z,
      y: _
    } = Hv(h, w, g)), q = -1);
  }
  return {
    x: z,
    y: _,
    placement: w,
    strategy: r,
    middlewareData: Y
  };
}, M1 = (i) => ({
  name: "arrow",
  options: i,
  async fn(c) {
    const {
      x: o,
      y: f,
      placement: r,
      rects: m,
      platform: v,
      elements: p,
      middlewareData: g
    } = c, {
      element: h,
      padding: z = 0
    } = fu(i, c) || {};
    if (h == null)
      return {};
    const _ = xh(z), w = {
      x: o,
      y: f
    }, C = ns(r), Y = ls(C), q = await v.getDimensions(h), V = C === "y", X = V ? "top" : "left", k = V ? "bottom" : "right", G = V ? "clientHeight" : "clientWidth", $ = m.reference[Y] + m.reference[C] - w[C] - m.floating[Y], at = w[C] - m.reference[C], lt = await (v.getOffsetParent == null ? void 0 : v.getOffsetParent(h));
    let Z = lt ? lt[G] : 0;
    (!Z || !await (v.isElement == null ? void 0 : v.isElement(lt))) && (Z = p.floating[G] || m.floating[Y]);
    const it = $ / 2 - at / 2, _t = Z / 2 - q[Y] / 2 - 1, Tt = la(_[X], _t), zt = la(_[k], _t), dt = Tt, Ct = Z - q[Y] - zt, Q = Z / 2 - q[Y] / 2 + it, ft = Yo(dt, Q, Ct), R = !g.arrow && ou(r) != null && Q !== ft && m.reference[Y] / 2 - (Q < dt ? Tt : zt) - q[Y] / 2 < 0, L = R ? Q < dt ? Q - dt : Q - Ct : 0;
    return {
      [C]: w[C] + L,
      data: {
        [C]: ft,
        centerOffset: Q - ft - L,
        ...R && {
          alignmentOffset: L
        }
      },
      reset: R
    };
  }
}), C1 = function(i) {
  return i === void 0 && (i = {}), {
    name: "flip",
    options: i,
    async fn(c) {
      var o, f;
      const {
        placement: r,
        middlewareData: m,
        rects: v,
        initialPlacement: p,
        platform: g,
        elements: h
      } = c, {
        mainAxis: z = !0,
        crossAxis: _ = !0,
        fallbackPlacements: w,
        fallbackStrategy: C = "bestFit",
        fallbackAxisSideDirection: Y = "none",
        flipAlignment: q = !0,
        ...V
      } = fu(i, c);
      if ((o = m.arrow) != null && o.alignmentOffset)
        return {};
      const X = dn(r), k = Ll(p), G = dn(p) === p, $ = await (g.isRTL == null ? void 0 : g.isRTL(h.floating)), at = w || (G || !q ? [Xi(p)] : Vb(p)), lt = Y !== "none";
      !w && lt && at.push(...kb(p, q, Y, $));
      const Z = [p, ...at], it = await g.detectOverflow(c, V), _t = [];
      let Tt = ((f = m.flip) == null ? void 0 : f.overflows) || [];
      if (z && _t.push(it[X]), _) {
        const Q = Qb(r, v, $);
        _t.push(it[Q[0]], it[Q[1]]);
      }
      if (Tt = [...Tt, {
        placement: r,
        overflows: _t
      }], !_t.every((Q) => Q <= 0)) {
        var zt, dt;
        const Q = (((zt = m.flip) == null ? void 0 : zt.index) || 0) + 1, ft = Z[Q];
        if (ft && (!(_ === "alignment" ? k !== Ll(ft) : !1) || // We leave the current main axis only if every placement on that axis
        // overflows the main axis.
        Tt.every((H) => Ll(H.placement) === k ? H.overflows[0] > 0 : !0)))
          return {
            data: {
              index: Q,
              overflows: Tt
            },
            reset: {
              placement: ft
            }
          };
        let R = (dt = Tt.filter((L) => L.overflows[0] <= 0).sort((L, H) => L.overflows[1] - H.overflows[1])[0]) == null ? void 0 : dt.placement;
        if (!R)
          switch (C) {
            case "bestFit": {
              var Ct;
              const L = (Ct = Tt.filter((H) => {
                if (lt) {
                  const F = Ll(H.placement);
                  return F === k || // Create a bias to the `y` side axis due to horizontal
                  // reading directions favoring greater width.
                  F === "y";
                }
                return !0;
              }).map((H) => [H.placement, H.overflows.filter((F) => F > 0).reduce((F, et) => F + et, 0)]).sort((H, F) => H[1] - F[1])[0]) == null ? void 0 : Ct[0];
              L && (R = L);
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
}, x1 = /* @__PURE__ */ new Set(["left", "top"]);
async function D1(i, c) {
  const {
    placement: o,
    platform: f,
    elements: r
  } = i, m = await (f.isRTL == null ? void 0 : f.isRTL(r.floating)), v = dn(o), p = ou(o), g = Ll(o) === "y", h = x1.has(v) ? -1 : 1, z = m && g ? -1 : 1, _ = fu(c, i);
  let {
    mainAxis: w,
    crossAxis: C,
    alignmentAxis: Y
  } = typeof _ == "number" ? {
    mainAxis: _,
    crossAxis: 0,
    alignmentAxis: null
  } : {
    mainAxis: _.mainAxis || 0,
    crossAxis: _.crossAxis || 0,
    alignmentAxis: _.alignmentAxis
  };
  return p && typeof Y == "number" && (C = p === "end" ? Y * -1 : Y), g ? {
    x: C * z,
    y: w * h
  } : {
    x: w * h,
    y: C * z
  };
}
const w1 = function(i) {
  return i === void 0 && (i = 0), {
    name: "offset",
    options: i,
    async fn(c) {
      var o, f;
      const {
        x: r,
        y: m,
        placement: v,
        middlewareData: p
      } = c, g = await D1(c, i);
      return v === ((o = p.offset) == null ? void 0 : o.placement) && (f = p.arrow) != null && f.alignmentOffset ? {} : {
        x: r + g.x,
        y: m + g.y,
        data: {
          ...g,
          placement: v
        }
      };
    }
  };
}, N1 = function(i) {
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
        mainAxis: v = !0,
        crossAxis: p = !1,
        limiter: g = {
          fn: (X) => {
            let {
              x: k,
              y: G
            } = X;
            return {
              x: k,
              y: G
            };
          }
        },
        ...h
      } = fu(i, c), z = {
        x: o,
        y: f
      }, _ = await m.detectOverflow(c, h), w = Ll(dn(r)), C = Ch(w);
      let Y = z[C], q = z[w];
      if (v) {
        const X = C === "y" ? "top" : "left", k = C === "y" ? "bottom" : "right", G = Y + _[X], $ = Y - _[k];
        Y = Yo(G, Y, $);
      }
      if (p) {
        const X = w === "y" ? "top" : "left", k = w === "y" ? "bottom" : "right", G = q + _[X], $ = q - _[k];
        q = Yo(G, q, $);
      }
      const V = g.fn({
        ...c,
        [C]: Y,
        [w]: q
      });
      return {
        ...V,
        data: {
          x: V.x - o,
          y: V.y - f,
          enabled: {
            [C]: v,
            [w]: p
          }
        }
      };
    }
  };
};
function Yh(i) {
  const c = Ee(i);
  let o = parseFloat(c.width) || 0, f = parseFloat(c.height) || 0;
  const r = Ge(i), m = r ? i.offsetWidth : o, v = r ? i.offsetHeight : f, p = ji(o) !== m || ji(f) !== v;
  return p && (o = m, f = v), {
    width: o,
    height: f,
    $: p
  };
}
function as(i) {
  return qt(i) ? i : i.contextElement;
}
function ea(i) {
  const c = as(i);
  if (!Ge(c))
    return je(1);
  const o = c.getBoundingClientRect(), {
    width: f,
    height: r,
    $: m
  } = Yh(c);
  let v = (m ? ji(o.width) : o.width) / f, p = (m ? ji(o.height) : o.height) / r;
  return (!v || !Number.isFinite(v)) && (v = 1), (!p || !Number.isFinite(p)) && (p = 1), {
    x: v,
    y: p
  };
}
const U1 = /* @__PURE__ */ je(0);
function jh(i) {
  const c = pe(i);
  return !$i() || !c.visualViewport ? U1 : {
    x: c.visualViewport.offsetLeft,
    y: c.visualViewport.offsetTop
  };
}
function H1(i, c, o) {
  return c === void 0 && (c = !1), !o || c && o !== pe(i) ? !1 : c;
}
function mn(i, c, o, f) {
  c === void 0 && (c = !1), o === void 0 && (o = !1);
  const r = i.getBoundingClientRect(), m = as(i);
  let v = je(1);
  c && (f ? qt(f) && (v = ea(f)) : v = ea(i));
  const p = H1(m, o, f) ? jh(m) : je(0);
  let g = (r.left + p.x) / v.x, h = (r.top + p.y) / v.y, z = r.width / v.x, _ = r.height / v.y;
  if (m) {
    const w = pe(m), C = f && qt(f) ? pe(f) : f;
    let Y = w, q = qo(Y);
    for (; q && f && C !== Y; ) {
      const V = ea(q), X = q.getBoundingClientRect(), k = Ee(q), G = X.left + (q.clientLeft + parseFloat(k.paddingLeft)) * V.x, $ = X.top + (q.clientTop + parseFloat(k.paddingTop)) * V.y;
      g *= V.x, h *= V.y, z *= V.x, _ *= V.y, g += G, h += $, Y = pe(q), q = qo(Y);
    }
  }
  return Gi({
    width: z,
    height: _,
    x: g,
    y: h
  });
}
function Pi(i, c) {
  const o = Ii(i).scrollLeft;
  return c ? c.left + o : mn(Xe(i)).left + o;
}
function Xh(i, c) {
  const o = i.getBoundingClientRect(), f = o.left + c.scrollLeft - Pi(i, o), r = o.top + c.scrollTop;
  return {
    x: f,
    y: r
  };
}
function B1(i) {
  let {
    elements: c,
    rect: o,
    offsetParent: f,
    strategy: r
  } = i;
  const m = r === "fixed", v = Xe(f), p = c ? Fi(c.floating) : !1;
  if (f === v || p && m)
    return o;
  let g = {
    scrollLeft: 0,
    scrollTop: 0
  }, h = je(1);
  const z = je(0), _ = Ge(f);
  if ((_ || !_ && !m) && ((na(f) !== "body" || cu(v)) && (g = Ii(f)), _)) {
    const C = mn(f);
    h = ea(f), z.x = C.x + f.clientLeft, z.y = C.y + f.clientTop;
  }
  const w = v && !_ && !m ? Xh(v, g) : je(0);
  return {
    width: o.width * h.x,
    height: o.height * h.y,
    x: o.x * h.x - g.scrollLeft * h.x + z.x + w.x,
    y: o.y * h.y - g.scrollTop * h.y + z.y + w.y
  };
}
function L1(i) {
  return Array.from(i.getClientRects());
}
function q1(i) {
  const c = Xe(i), o = Ii(i), f = i.ownerDocument.body, r = rn(c.scrollWidth, c.clientWidth, f.scrollWidth, f.clientWidth), m = rn(c.scrollHeight, c.clientHeight, f.scrollHeight, f.clientHeight);
  let v = -o.scrollLeft + Pi(i);
  const p = -o.scrollTop;
  return Ee(f).direction === "rtl" && (v += rn(c.clientWidth, f.clientWidth) - r), {
    width: r,
    height: m,
    x: v,
    y: p
  };
}
const Bv = 25;
function Y1(i, c) {
  const o = pe(i), f = Xe(i), r = o.visualViewport;
  let m = f.clientWidth, v = f.clientHeight, p = 0, g = 0;
  if (r) {
    m = r.width, v = r.height;
    const z = $i();
    (!z || z && c === "fixed") && (p = r.offsetLeft, g = r.offsetTop);
  }
  const h = Pi(f);
  if (h <= 0) {
    const z = f.ownerDocument, _ = z.body, w = getComputedStyle(_), C = z.compatMode === "CSS1Compat" && parseFloat(w.marginLeft) + parseFloat(w.marginRight) || 0, Y = Math.abs(f.clientWidth - _.clientWidth - C);
    Y <= Bv && (m -= Y);
  } else h <= Bv && (m += h);
  return {
    width: m,
    height: v,
    x: p,
    y: g
  };
}
function j1(i, c) {
  const o = mn(i, !0, c === "fixed"), f = o.top + i.clientTop, r = o.left + i.clientLeft, m = Ge(i) ? ea(i) : je(1), v = i.clientWidth * m.x, p = i.clientHeight * m.y, g = r * m.x, h = f * m.y;
  return {
    width: v,
    height: p,
    x: g,
    y: h
  };
}
function Lv(i, c, o) {
  let f;
  if (c === "viewport")
    f = Y1(i, o);
  else if (c === "document")
    f = q1(Xe(i));
  else if (qt(c))
    f = j1(c, o);
  else {
    const r = jh(i);
    f = {
      x: c.x - r.x,
      y: c.y - r.y,
      width: c.width,
      height: c.height
    };
  }
  return Gi(f);
}
function Gh(i, c) {
  const o = sl(i);
  return o === c || !qt(o) || ol(o) ? !1 : Ee(o).position === "fixed" || Gh(o, c);
}
function X1(i, c) {
  const o = c.get(i);
  if (o)
    return o;
  let f = jl(i, [], !1).filter((p) => qt(p) && na(p) !== "body"), r = null;
  const m = Ee(i).position === "fixed";
  let v = m ? sl(i) : i;
  for (; qt(v) && !ol(v); ) {
    const p = Ee(v), g = es(v);
    !g && p.position === "fixed" && (r = null), (m ? !g && !r : !g && p.position === "static" && !!r && (r.position === "absolute" || r.position === "fixed") || cu(v) && !g && Gh(i, v)) ? f = f.filter((z) => z !== v) : r = p, v = sl(v);
  }
  return c.set(i, f), f;
}
function G1(i) {
  let {
    element: c,
    boundary: o,
    rootBoundary: f,
    strategy: r
  } = i;
  const v = [...o === "clippingAncestors" ? Fi(c) ? [] : X1(c, this._c) : [].concat(o), f], p = Lv(c, v[0], r);
  let g = p.top, h = p.right, z = p.bottom, _ = p.left;
  for (let w = 1; w < v.length; w++) {
    const C = Lv(c, v[w], r);
    g = rn(C.top, g), h = la(C.right, h), z = la(C.bottom, z), _ = rn(C.left, _);
  }
  return {
    width: h - _,
    height: z - g,
    x: _,
    y: g
  };
}
function Q1(i) {
  const {
    width: c,
    height: o
  } = Yh(i);
  return {
    width: c,
    height: o
  };
}
function V1(i, c, o) {
  const f = Ge(c), r = Xe(c), m = o === "fixed", v = mn(i, !0, m, c);
  let p = {
    scrollLeft: 0,
    scrollTop: 0
  };
  const g = je(0);
  function h() {
    g.x = Pi(r);
  }
  if (f || !f && !m)
    if ((na(c) !== "body" || cu(r)) && (p = Ii(c)), f) {
      const C = mn(c, !0, m, c);
      g.x = C.x + c.clientLeft, g.y = C.y + c.clientTop;
    } else r && h();
  m && !f && r && h();
  const z = r && !f && !m ? Xh(r, p) : je(0), _ = v.left + p.scrollLeft - g.x - z.x, w = v.top + p.scrollTop - g.y - z.y;
  return {
    x: _,
    y: w,
    width: v.width,
    height: v.height
  };
}
function xo(i) {
  return Ee(i).position === "static";
}
function qv(i, c) {
  if (!Ge(i) || Ee(i).position === "fixed")
    return null;
  if (c)
    return c(i);
  let o = i.offsetParent;
  return Xe(i) === o && (o = o.ownerDocument.body), o;
}
function Qh(i, c) {
  const o = pe(i);
  if (Fi(i))
    return o;
  if (!Ge(i)) {
    let r = sl(i);
    for (; r && !ol(r); ) {
      if (qt(r) && !xo(r))
        return r;
      r = sl(r);
    }
    return o;
  }
  let f = qv(i, c);
  for (; f && qb(f) && xo(f); )
    f = qv(f, c);
  return f && ol(f) && xo(f) && !es(f) ? o : f || Xb(i) || o;
}
const Z1 = async function(i) {
  const c = this.getOffsetParent || Qh, o = this.getDimensions, f = await o(i.floating);
  return {
    reference: V1(i.reference, await c(i.floating), i.strategy),
    floating: {
      x: 0,
      y: 0,
      width: f.width,
      height: f.height
    }
  };
};
function K1(i) {
  return Ee(i).direction === "rtl";
}
const J1 = {
  convertOffsetParentRelativeRectToViewportRelativeRect: B1,
  getDocumentElement: Xe,
  getClippingRect: G1,
  getOffsetParent: Qh,
  getElementRects: Z1,
  getClientRects: L1,
  getDimensions: Q1,
  getScale: ea,
  isElement: qt,
  isRTL: K1
};
function Vh(i, c) {
  return i.x === c.x && i.y === c.y && i.width === c.width && i.height === c.height;
}
function k1(i, c) {
  let o = null, f;
  const r = Xe(i);
  function m() {
    var p;
    clearTimeout(f), (p = o) == null || p.disconnect(), o = null;
  }
  function v(p, g) {
    p === void 0 && (p = !1), g === void 0 && (g = 1), m();
    const h = i.getBoundingClientRect(), {
      left: z,
      top: _,
      width: w,
      height: C
    } = h;
    if (p || c(), !w || !C)
      return;
    const Y = Mi(_), q = Mi(r.clientWidth - (z + w)), V = Mi(r.clientHeight - (_ + C)), X = Mi(z), G = {
      rootMargin: -Y + "px " + -q + "px " + -V + "px " + -X + "px",
      threshold: rn(0, la(1, g)) || 1
    };
    let $ = !0;
    function at(lt) {
      const Z = lt[0].intersectionRatio;
      if (Z !== g) {
        if (!$)
          return v();
        Z ? v(!1, Z) : f = setTimeout(() => {
          v(!1, 1e-7);
        }, 1e3);
      }
      Z === 1 && !Vh(h, i.getBoundingClientRect()) && v(), $ = !1;
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
  return v(!0), m;
}
function W1(i, c, o, f) {
  f === void 0 && (f = {});
  const {
    ancestorScroll: r = !0,
    ancestorResize: m = !0,
    elementResize: v = typeof ResizeObserver == "function",
    layoutShift: p = typeof IntersectionObserver == "function",
    animationFrame: g = !1
  } = f, h = as(i), z = r || m ? [...h ? jl(h) : [], ...c ? jl(c) : []] : [];
  z.forEach((X) => {
    r && X.addEventListener("scroll", o, {
      passive: !0
    }), m && X.addEventListener("resize", o);
  });
  const _ = h && p ? k1(h, o) : null;
  let w = -1, C = null;
  v && (C = new ResizeObserver((X) => {
    let [k] = X;
    k && k.target === h && C && c && (C.unobserve(c), cancelAnimationFrame(w), w = requestAnimationFrame(() => {
      var G;
      (G = C) == null || G.observe(c);
    })), o();
  }), h && !g && C.observe(h), c && C.observe(c));
  let Y, q = g ? mn(i) : null;
  g && V();
  function V() {
    const X = mn(i);
    q && !Vh(q, X) && o(), q = X, Y = requestAnimationFrame(V);
  }
  return o(), () => {
    var X;
    z.forEach((k) => {
      r && k.removeEventListener("scroll", o), m && k.removeEventListener("resize", o);
    }), _ == null || _(), (X = C) == null || X.disconnect(), C = null, g && cancelAnimationFrame(Y);
  };
}
const F1 = w1, $1 = N1, I1 = C1, Yv = M1, P1 = (i, c, o) => {
  const f = /* @__PURE__ */ new Map(), r = {
    platform: J1,
    ...o
  }, m = {
    ...r.platform,
    _c: f
  };
  return z1(i, c, {
    ...r,
    platform: m
  });
};
var tS = typeof document < "u", eS = function() {
}, Ui = tS ? B.useLayoutEffect : eS;
function Zi(i, c) {
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
        if (!Zi(i[f], c[f]))
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
      if (!(m === "_owner" && i.$$typeof) && !Zi(i[m], c[m]))
        return !1;
    }
    return !0;
  }
  return i !== i && c !== c;
}
function Zh(i) {
  return typeof window > "u" ? 1 : (i.ownerDocument.defaultView || window).devicePixelRatio || 1;
}
function jv(i, c) {
  const o = Zh(i);
  return Math.round(c * o) / o;
}
function Do(i) {
  const c = B.useRef(i);
  return Ui(() => {
    c.current = i;
  }), c;
}
function lS(i) {
  i === void 0 && (i = {});
  const {
    placement: c = "bottom",
    strategy: o = "absolute",
    middleware: f = [],
    platform: r,
    elements: {
      reference: m,
      floating: v
    } = {},
    transform: p = !0,
    whileElementsMounted: g,
    open: h
  } = i, [z, _] = B.useState({
    x: 0,
    y: 0,
    strategy: o,
    placement: c,
    middlewareData: {},
    isPositioned: !1
  }), [w, C] = B.useState(f);
  Zi(w, f) || C(f);
  const [Y, q] = B.useState(null), [V, X] = B.useState(null), k = B.useCallback((H) => {
    H !== lt.current && (lt.current = H, q(H));
  }, []), G = B.useCallback((H) => {
    H !== Z.current && (Z.current = H, X(H));
  }, []), $ = m || Y, at = v || V, lt = B.useRef(null), Z = B.useRef(null), it = B.useRef(z), _t = g != null, Tt = Do(g), zt = Do(r), dt = Do(h), Ct = B.useCallback(() => {
    if (!lt.current || !Z.current)
      return;
    const H = {
      placement: c,
      strategy: o,
      middleware: w
    };
    zt.current && (H.platform = zt.current), P1(lt.current, Z.current, H).then((F) => {
      const et = {
        ...F,
        // The floating element's position may be recomputed while it's closed
        // but still mounted (such as when transitioning out). To ensure
        // `isPositioned` will be `false` initially on the next open, avoid
        // setting it to `true` when `open === false` (must be specified).
        isPositioned: dt.current !== !1
      };
      Q.current && !Zi(it.current, et) && (it.current = et, ki.flushSync(() => {
        _(et);
      }));
    });
  }, [w, c, o, zt, dt]);
  Ui(() => {
    h === !1 && it.current.isPositioned && (it.current.isPositioned = !1, _((H) => ({
      ...H,
      isPositioned: !1
    })));
  }, [h]);
  const Q = B.useRef(!1);
  Ui(() => (Q.current = !0, () => {
    Q.current = !1;
  }), []), Ui(() => {
    if ($ && (lt.current = $), at && (Z.current = at), $ && at) {
      if (Tt.current)
        return Tt.current($, at, Ct);
      Ct();
    }
  }, [$, at, Ct, Tt, _t]);
  const ft = B.useMemo(() => ({
    reference: lt,
    floating: Z,
    setReference: k,
    setFloating: G
  }), [k, G]), R = B.useMemo(() => ({
    reference: $,
    floating: at
  }), [$, at]), L = B.useMemo(() => {
    const H = {
      position: o,
      left: 0,
      top: 0
    };
    if (!R.floating)
      return H;
    const F = jv(R.floating, z.x), et = jv(R.floating, z.y);
    return p ? {
      ...H,
      transform: "translate(" + F + "px, " + et + "px)",
      ...Zh(R.floating) >= 1.5 && {
        willChange: "transform"
      }
    } : {
      position: o,
      left: F,
      top: et
    };
  }, [o, p, R.floating, z.x, z.y]);
  return B.useMemo(() => ({
    ...z,
    update: Ct,
    refs: ft,
    elements: R,
    floatingStyles: L
  }), [z, Ct, ft, R, L]);
}
const nS = (i) => {
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
      return f && c(f) ? f.current != null ? Yv({
        element: f.current,
        padding: r
      }).fn(o) : {} : f ? Yv({
        element: f,
        padding: r
      }).fn(o) : {};
    }
  };
}, aS = (i, c) => {
  const o = F1(i);
  return {
    name: o.name,
    fn: o.fn,
    options: [i, c]
  };
}, uS = (i, c) => {
  const o = $1(i);
  return {
    name: o.name,
    fn: o.fn,
    options: [i, c]
  };
}, iS = (i, c) => {
  const o = I1(i);
  return {
    name: o.name,
    fn: o.fn,
    options: [i, c]
  };
}, cS = (i, c) => {
  const o = nS(i);
  return {
    name: o.name,
    fn: o.fn,
    options: [i, c]
  };
}, fS = "data-floating-ui-focusable", Xv = "active", Gv = "selected", oS = {
  ...Pv
};
let Qv = !1, sS = 0;
const Vv = () => (
  // Ensure the id is unique with multiple independent versions of Floating UI
  // on <React 18
  "floating-ui-" + Math.random().toString(36).slice(2, 6) + sS++
);
function rS() {
  const [i, c] = B.useState(() => Qv ? Vv() : void 0);
  return Xl(() => {
    i == null && c(Vv());
  }, []), B.useEffect(() => {
    Qv = !0;
  }, []), i;
}
const dS = oS.useId, tc = dS || rS, mS = /* @__PURE__ */ B.forwardRef(function(c, o) {
  const {
    context: {
      placement: f,
      elements: {
        floating: r
      },
      middlewareData: {
        arrow: m,
        shift: v
      }
    },
    width: p = 14,
    height: g = 7,
    tipRadius: h = 0,
    strokeWidth: z = 0,
    staticOffset: _,
    stroke: w,
    d: C,
    style: {
      transform: Y,
      ...q
    } = {},
    ...V
  } = c, X = tc(), [k, G] = B.useState(!1);
  if (Xl(() => {
    if (!r) return;
    Ee(r).direction === "rtl" && G(!0);
  }, [r]), !r)
    return null;
  const [$, at] = f.split("-"), lt = $ === "top" || $ === "bottom";
  let Z = _;
  (lt && v != null && v.x || !lt && v != null && v.y) && (Z = null);
  const it = z * 2, _t = it / 2, Tt = p / 2 * (h / -8 + 1), zt = g / 2 * h / 4, dt = !!C, Ct = Z && at === "end" ? "bottom" : "top";
  let Q = Z && at === "end" ? "right" : "left";
  Z && k && (Q = at === "end" ? "left" : "right");
  const ft = (m == null ? void 0 : m.x) != null ? Z || m.x : "", R = (m == null ? void 0 : m.y) != null ? Z || m.y : "", L = C || "M0,0" + (" H" + p) + (" L" + (p - Tt) + "," + (g - zt)) + (" Q" + p / 2 + "," + g + " " + Tt + "," + (g - zt)) + " Z", H = {
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
    viewBox: "0 0 " + p + " " + (g > p ? g : p),
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
      stroke: w,
      strokeWidth: it + (C ? 0 : 1),
      d: L
    }), /* @__PURE__ */ Vt.jsx("path", {
      stroke: it && !C ? V.fill : "none",
      d: L
    }), /* @__PURE__ */ Vt.jsx("clipPath", {
      id: X,
      children: /* @__PURE__ */ Vt.jsx("rect", {
        x: -_t,
        y: _t * (dt ? -1 : 1),
        width: p + it,
        height: p
      })
    })]
  });
});
function vS() {
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
const hS = /* @__PURE__ */ B.createContext(null), yS = /* @__PURE__ */ B.createContext(null), Kh = () => {
  var i;
  return ((i = B.useContext(hS)) == null ? void 0 : i.id) || null;
}, Jh = () => B.useContext(yS);
function us(i) {
  return "data-floating-ui-" + i;
}
const gS = {
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
}, Zv = /* @__PURE__ */ B.forwardRef(function(c, o) {
  const [f, r] = B.useState();
  Xl(() => {
    d1() && r("button");
  }, []);
  const m = {
    ref: o,
    tabIndex: 0,
    // Role is only for VoiceOver
    role: f,
    "aria-hidden": f ? void 0 : !0,
    [us("focus-guard")]: "",
    style: gS
  };
  return /* @__PURE__ */ Vt.jsx("span", {
    ...c,
    ...m
  });
}), bS = {
  clipPath: "inset(50%)",
  position: "fixed",
  top: 0,
  left: 0
}, kh = /* @__PURE__ */ B.createContext(null), Kv = /* @__PURE__ */ us("portal");
function SS(i) {
  i === void 0 && (i = {});
  const {
    id: c,
    root: o
  } = i, f = tc(), r = ES(), [m, v] = B.useState(null), p = B.useRef(null);
  return Xl(() => () => {
    m == null || m.remove(), queueMicrotask(() => {
      p.current = null;
    });
  }, [m]), Xl(() => {
    if (!f || p.current) return;
    const g = c ? document.getElementById(c) : null;
    if (!g) return;
    const h = document.createElement("div");
    h.id = f, h.setAttribute(Kv, ""), g.appendChild(h), p.current = h, v(h);
  }, [c, f]), Xl(() => {
    if (o === null || !f || p.current) return;
    let g = o || (r == null ? void 0 : r.portalNode);
    g && !ts(g) && (g = g.current), g = g || document.body;
    let h = null;
    c && (h = document.createElement("div"), h.id = c, g.appendChild(h));
    const z = document.createElement("div");
    z.id = f, z.setAttribute(Kv, ""), g = h || g, g.appendChild(z), p.current = z, v(z);
  }, [c, o, f, r]), m;
}
function pS(i) {
  const {
    children: c,
    id: o,
    root: f,
    preserveTabOrder: r = !0
  } = i, m = SS({
    id: o,
    root: f
  }), [v, p] = B.useState(null), g = B.useRef(null), h = B.useRef(null), z = B.useRef(null), _ = B.useRef(null), w = v == null ? void 0 : v.modal, C = v == null ? void 0 : v.open, Y = (
    // The FocusManager and therefore floating element are currently open/
    // rendered.
    !!v && // Guards are only for non-modal focus management.
    !v.modal && // Don't render if unmount is transitioning.
    v.open && r && !!(f || m)
  );
  return B.useEffect(() => {
    if (!m || !r || w)
      return;
    function q(V) {
      m && Co(V) && (V.type === "focusin" ? Uv : R1)(m);
    }
    return m.addEventListener("focusin", q, !0), m.addEventListener("focusout", q, !0), () => {
      m.removeEventListener("focusin", q, !0), m.removeEventListener("focusout", q, !0);
    };
  }, [m, r, w]), B.useEffect(() => {
    m && (C || Uv(m));
  }, [C, m]), /* @__PURE__ */ Vt.jsxs(kh.Provider, {
    value: B.useMemo(() => ({
      preserveTabOrder: r,
      beforeOutsideRef: g,
      afterOutsideRef: h,
      beforeInsideRef: z,
      afterInsideRef: _,
      portalNode: m,
      setFocusManagerState: p
    }), [r, m]),
    children: [Y && m && /* @__PURE__ */ Vt.jsx(Zv, {
      "data-type": "outside",
      ref: g,
      onFocus: (q) => {
        if (Co(q, m)) {
          var V;
          (V = z.current) == null || V.focus();
        } else {
          const X = v ? v.domReference : null, k = A1(X);
          k == null || k.focus();
        }
      }
    }), Y && m && /* @__PURE__ */ Vt.jsx("span", {
      "aria-owns": m.id,
      style: bS
    }), m && /* @__PURE__ */ ki.createPortal(c, m), Y && m && /* @__PURE__ */ Vt.jsx(Zv, {
      "data-type": "outside",
      ref: h,
      onFocus: (q) => {
        if (Co(q, m)) {
          var V;
          (V = _.current) == null || V.focus();
        } else {
          const X = v ? v.domReference : null, k = T1(X);
          k == null || k.focus(), v != null && v.closeOnFocusOut && (v == null || v.onOpenChange(!1, q.nativeEvent, "focus-out"));
        }
      }
    })]
  });
}
const ES = () => B.useContext(kh), TS = {
  pointerdown: "onPointerDown",
  mousedown: "onMouseDown",
  click: "onClick"
}, AS = {
  pointerdown: "onPointerDownCapture",
  mousedown: "onMouseDownCapture",
  click: "onClickCapture"
}, Jv = (i) => {
  var c, o;
  return {
    escapeKey: typeof i == "boolean" ? i : (c = i == null ? void 0 : i.escapeKey) != null ? c : !1,
    outsidePress: typeof i == "boolean" ? i : (o = i == null ? void 0 : i.outsidePress) != null ? o : !0
  };
};
function RS(i, c) {
  c === void 0 && (c = {});
  const {
    open: o,
    onOpenChange: f,
    elements: r,
    dataRef: m
  } = i, {
    enabled: v = !0,
    escapeKey: p = !0,
    outsidePress: g = !0,
    outsidePressEvent: h = "pointerdown",
    referencePress: z = !1,
    referencePressEvent: _ = "pointerdown",
    ancestorScroll: w = !1,
    bubbles: C,
    capture: Y
  } = c, q = Jh(), V = ta(typeof g == "function" ? g : () => !1), X = typeof g == "function" ? V : g, k = B.useRef(!1), {
    escapeKey: G,
    outsidePress: $
  } = Jv(C), {
    escapeKey: at,
    outsidePress: lt
  } = Jv(Y), Z = B.useRef(!1), it = ta((Q) => {
    var ft;
    if (!o || !v || !p || Q.key !== "Escape" || Z.current)
      return;
    const R = (ft = m.current.floatingContext) == null ? void 0 : ft.nodeId, L = q ? Ni(q.nodesRef.current, R) : [];
    if (!G && (Q.stopPropagation(), L.length > 0)) {
      let H = !0;
      if (L.forEach((F) => {
        var et;
        if ((et = F.context) != null && et.open && !F.context.dataRef.current.__escapeKeyBubbles) {
          H = !1;
          return;
        }
      }), !H)
        return;
    }
    f(!1, y1(Q) ? Q.nativeEvent : Q, "escape-key");
  }), _t = ta((Q) => {
    var ft;
    const R = () => {
      var L;
      it(Q), (L = tu(Q)) == null || L.removeEventListener("keydown", R);
    };
    (ft = tu(Q)) == null || ft.addEventListener("keydown", R);
  }), Tt = ta((Q) => {
    var ft;
    const R = m.current.insideReactTree;
    m.current.insideReactTree = !1;
    const L = k.current;
    if (k.current = !1, h === "click" && L || R || typeof X == "function" && !X(Q))
      return;
    const H = tu(Q), F = "[" + us("inert") + "]", et = iu(r.floating).querySelectorAll(F);
    let b = qt(H) ? H : null;
    for (; b && !ol(b); ) {
      const I = sl(b);
      if (ol(I) || !qt(I))
        break;
      b = I;
    }
    if (et.length && qt(H) && !v1(H) && // Clicked on a direct ancestor (e.g. FloatingOverlay).
    !Qo(H, r.floating) && // If the target root element contains none of the markers, then the
    // element was injected after the floating element rendered.
    Array.from(et).every((I) => !Qo(b, I)))
      return;
    if (Ge(H) && Ct) {
      const I = ol(H), nt = Ee(H), mt = /auto|scroll/, kt = I || mt.test(nt.overflowX), Nt = I || mt.test(nt.overflowY), Gl = kt && H.clientWidth > 0 && H.scrollWidth > H.clientWidth, hn = Nt && H.clientHeight > 0 && H.scrollHeight > H.clientHeight, aa = nt.direction === "rtl", su = hn && (aa ? Q.offsetX <= H.offsetWidth - H.clientWidth : Q.offsetX > H.clientWidth), Qe = Gl && Q.offsetY > H.clientHeight;
      if (su || Qe)
        return;
    }
    const U = (ft = m.current.floatingContext) == null ? void 0 : ft.nodeId, j = q && Ni(q.nodesRef.current, U).some((I) => {
      var nt;
      return Mo(Q, (nt = I.context) == null ? void 0 : nt.elements.floating);
    });
    if (Mo(Q, r.floating) || Mo(Q, r.domReference) || j)
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
  }), zt = ta((Q) => {
    var ft;
    const R = () => {
      var L;
      Tt(Q), (L = tu(Q)) == null || L.removeEventListener(h, R);
    };
    (ft = tu(Q)) == null || ft.addEventListener(h, R);
  });
  B.useEffect(() => {
    if (!o || !v)
      return;
    m.current.__escapeKeyBubbles = G, m.current.__outsidePressBubbles = $;
    let Q = -1;
    function ft(et) {
      f(!1, et, "ancestor-scroll");
    }
    function R() {
      window.clearTimeout(Q), Z.current = !0;
    }
    function L() {
      Q = window.setTimeout(
        () => {
          Z.current = !1;
        },
        // 0ms or 1ms don't work in Safari. 5ms appears to consistently work.
        // Only apply to WebKit for the test to remain 0ms.
        $i() ? 5 : 0
      );
    }
    const H = iu(r.floating);
    p && (H.addEventListener("keydown", at ? _t : it, at), H.addEventListener("compositionstart", R), H.addEventListener("compositionend", L)), X && H.addEventListener(h, lt ? zt : Tt, lt);
    let F = [];
    return w && (qt(r.domReference) && (F = jl(r.domReference)), qt(r.floating) && (F = F.concat(jl(r.floating))), !qt(r.reference) && r.reference && r.reference.contextElement && (F = F.concat(jl(r.reference.contextElement)))), F = F.filter((et) => {
      var b;
      return et !== ((b = H.defaultView) == null ? void 0 : b.visualViewport);
    }), F.forEach((et) => {
      et.addEventListener("scroll", ft, {
        passive: !0
      });
    }), () => {
      p && (H.removeEventListener("keydown", at ? _t : it, at), H.removeEventListener("compositionstart", R), H.removeEventListener("compositionend", L)), X && H.removeEventListener(h, lt ? zt : Tt, lt), F.forEach((et) => {
        et.removeEventListener("scroll", ft);
      }), window.clearTimeout(Q);
    };
  }, [m, r, p, X, h, o, f, w, v, G, $, it, at, _t, Tt, lt, zt]), B.useEffect(() => {
    m.current.insideReactTree = !1;
  }, [m, X, h]);
  const dt = B.useMemo(() => ({
    onKeyDown: it,
    ...z && {
      [TS[_]]: (Q) => {
        f(!1, Q.nativeEvent, "reference-press");
      },
      ..._ !== "click" && {
        onClick(Q) {
          f(!1, Q.nativeEvent, "reference-press");
        }
      }
    }
  }), [it, f, z, _]), Ct = B.useMemo(() => {
    function Q(ft) {
      ft.button === 0 && (k.current = !0);
    }
    return {
      onKeyDown: it,
      onMouseDown: Q,
      onMouseUp: Q,
      [AS[h]]: () => {
        m.current.insideReactTree = !0;
      }
    };
  }, [it, h, m]);
  return B.useMemo(() => v ? {
    reference: dt,
    floating: Ct
  } : {}, [v, dt, Ct]);
}
function OS(i) {
  const {
    open: c = !1,
    onOpenChange: o,
    elements: f
  } = i, r = tc(), m = B.useRef({}), [v] = B.useState(() => vS()), p = Kh() != null, [g, h] = B.useState(f.reference), z = ta((C, Y, q) => {
    m.current.openEvent = C ? Y : void 0, v.emit("openchange", {
      open: C,
      event: Y,
      reason: q,
      nested: p
    }), o == null || o(C, Y, q);
  }), _ = B.useMemo(() => ({
    setPositionReference: h
  }), []), w = B.useMemo(() => ({
    reference: g || f.reference || null,
    floating: f.floating || null,
    domReference: f.reference
  }), [g, f.reference, f.floating]);
  return B.useMemo(() => ({
    dataRef: m,
    open: c,
    onOpenChange: z,
    elements: w,
    events: v,
    floatingId: r,
    refs: _
  }), [c, z, w, v, r, _]);
}
function _S(i) {
  i === void 0 && (i = {});
  const {
    nodeId: c
  } = i, o = OS({
    ...i,
    elements: {
      reference: null,
      floating: null,
      ...i.elements
    }
  }), f = i.rootContext || o, r = f.elements, [m, v] = B.useState(null), [p, g] = B.useState(null), z = (r == null ? void 0 : r.domReference) || m, _ = B.useRef(null), w = Jh();
  Xl(() => {
    z && (_.current = z);
  }, [z]);
  const C = lS({
    ...i,
    elements: {
      ...r,
      ...p && {
        reference: p
      }
    }
  }), Y = B.useCallback((G) => {
    const $ = qt(G) ? {
      getBoundingClientRect: () => G.getBoundingClientRect(),
      getClientRects: () => G.getClientRects(),
      contextElement: G
    } : G;
    g($), C.refs.setReference($);
  }, [C.refs]), q = B.useCallback((G) => {
    (qt(G) || G === null) && (_.current = G, v(G)), (qt(C.refs.reference.current) || C.refs.reference.current === null || // Don't allow setting virtual elements using the old technique back to
    // `null` to support `positionReference` + an unstable `reference`
    // callback ref.
    G !== null && !qt(G)) && C.refs.setReference(G);
  }, [C.refs]), V = B.useMemo(() => ({
    ...C.refs,
    setReference: q,
    setPositionReference: Y,
    domReference: _
  }), [C.refs, q, Y]), X = B.useMemo(() => ({
    ...C.elements,
    domReference: z
  }), [C.elements, z]), k = B.useMemo(() => ({
    ...C,
    ...f,
    refs: V,
    elements: X,
    nodeId: c
  }), [C, V, X, c, f]);
  return Xl(() => {
    f.dataRef.current.floatingContext = k;
    const G = w == null ? void 0 : w.nodesRef.current.find(($) => $.id === c);
    G && (G.context = k);
  }), B.useMemo(() => ({
    ...C,
    context: k,
    refs: V,
    elements: X
  }), [C, V, X, k]);
}
function wo(i, c, o) {
  const f = /* @__PURE__ */ new Map(), r = o === "item";
  let m = i;
  if (r && i) {
    const {
      [Xv]: v,
      [Gv]: p,
      ...g
    } = i;
    m = g;
  }
  return {
    ...o === "floating" && {
      tabIndex: -1,
      [fS]: ""
    },
    ...m,
    ...c.map((v) => {
      const p = v ? v[o] : null;
      return typeof p == "function" ? i ? p(i) : null : p;
    }).concat(i).reduce((v, p) => (p && Object.entries(p).forEach((g) => {
      let [h, z] = g;
      if (!(r && [Xv, Gv].includes(h)))
        if (h.indexOf("on") === 0) {
          if (f.has(h) || f.set(h, []), typeof z == "function") {
            var _;
            (_ = f.get(h)) == null || _.push(z), v[h] = function() {
              for (var w, C = arguments.length, Y = new Array(C), q = 0; q < C; q++)
                Y[q] = arguments[q];
              return (w = f.get(h)) == null ? void 0 : w.map((V) => V(...Y)).find((V) => V !== void 0);
            };
          }
        } else
          v[h] = z;
    }), v), {})
  };
}
function zS(i) {
  i === void 0 && (i = []);
  const c = i.map((p) => p == null ? void 0 : p.reference), o = i.map((p) => p == null ? void 0 : p.floating), f = i.map((p) => p == null ? void 0 : p.item), r = B.useCallback(
    (p) => wo(p, i, "reference"),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    c
  ), m = B.useCallback(
    (p) => wo(p, i, "floating"),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    o
  ), v = B.useCallback(
    (p) => wo(p, i, "item"),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    f
  );
  return B.useMemo(() => ({
    getReferenceProps: r,
    getFloatingProps: m,
    getItemProps: v
  }), [r, m, v]);
}
const MS = /* @__PURE__ */ new Map([["select", "listbox"], ["combobox", "listbox"], ["label", !1]]);
function CS(i, c) {
  var o, f;
  c === void 0 && (c = {});
  const {
    open: r,
    elements: m,
    floatingId: v
  } = i, {
    enabled: p = !0,
    role: g = "dialog"
  } = c, h = tc(), z = ((o = m.domReference) == null ? void 0 : o.id) || h, _ = B.useMemo(() => {
    var k;
    return ((k = h1(m.floating)) == null ? void 0 : k.id) || v;
  }, [m.floating, v]), w = (f = MS.get(g)) != null ? f : g, Y = Kh() != null, q = B.useMemo(() => w === "tooltip" || g === "label" ? {
    ["aria-" + (g === "label" ? "labelledby" : "describedby")]: r ? _ : void 0
  } : {
    "aria-expanded": r ? "true" : "false",
    "aria-haspopup": w === "alertdialog" ? "dialog" : w,
    "aria-controls": r ? _ : void 0,
    ...w === "listbox" && {
      role: "combobox"
    },
    ...w === "menu" && {
      id: z
    },
    ...w === "menu" && Y && {
      role: "menuitem"
    },
    ...g === "select" && {
      "aria-autocomplete": "none"
    },
    ...g === "combobox" && {
      "aria-autocomplete": "list"
    }
  }, [w, _, Y, r, z, g]), V = B.useMemo(() => {
    const k = {
      id: _,
      ...w && {
        role: w
      }
    };
    return w === "tooltip" || g === "label" ? k : {
      ...k,
      ...w === "menu" && {
        "aria-labelledby": z
      }
    };
  }, [w, _, z, g]), X = B.useCallback((k) => {
    let {
      active: G,
      selected: $
    } = k;
    const at = {
      role: "option",
      ...G && {
        id: _ + "-fui-option"
      }
    };
    switch (g) {
      case "select":
      case "combobox":
        return {
          ...at,
          "aria-selected": $
        };
    }
    return {};
  }, [_, g]);
  return B.useMemo(() => p ? {
    reference: q,
    floating: V,
    item: X
  } : {}, [p, q, V, X]);
}
function xS(i) {
  const { anchor: c, data: o, portalRoot: f, onClose: r, onEnter: m, onLeave: v } = i, p = Jt.useRef(null), { refs: g, floatingStyles: h, context: z } = _S({
    open: !0,
    onOpenChange: (Y) => {
      Y || r();
    },
    placement: "top",
    elements: { reference: c },
    middleware: [aS(10), iS(), uS({ padding: 8 }), cS({ element: p })],
    whileElementsMounted: W1
  }), _ = RS(z, { outsidePress: !0, escapeKey: !0 }), w = CS(z, { role: "tooltip" }), { getFloatingProps: C } = zS([_, w]);
  return /* @__PURE__ */ Vt.jsx(pS, { root: f, children: /* @__PURE__ */ Vt.jsxs(
    "div",
    {
      ref: g.setFloating,
      style: o.interactive ? h : { ...h, pointerEvents: "none" },
      className: "tl-tooltip-popover" + (o.interactive ? " tl-tooltip-popover--interactive" : ""),
      ...C(),
      onPointerEnter: m,
      onPointerLeave: v,
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
          mS,
          {
            ref: p,
            context: z,
            className: "tl-tooltip-arrow",
            width: 12,
            height: 6
          }
        )
      ]
    }
  ) });
}
const DS = 400, wS = 150, NS = 400, Ci = /* @__PURE__ */ new Map();
let fl = null, Hi = null, lu = null, nu = null, ql = null;
function kv() {
  fl || (fl = document.createElement("div"), fl.id = "tl-tooltip-host", document.body.appendChild(fl), Hi = ih.createRoot(fl), ec(), document.addEventListener("pointerover", US, !0), document.addEventListener("pointerout", HS, !0));
}
function US(i) {
  const c = i.target;
  if (!c) return;
  const o = Wh(c);
  if (!o) return;
  Ph(), Ih();
  const f = LS(o, c);
  if (!f) return;
  const r = o.kind === "dynamic" ? c : o.el;
  qS(r, f);
}
function HS(i) {
  const c = i.relatedTarget;
  c && fl && fl.contains(c) || c && Wh(c) || (Ih(), $h());
}
function Wh(i) {
  var o;
  let c = i;
  for (; c; ) {
    const f = (o = c.getAttribute) == null ? void 0 : o.call(c, "data-tooltip");
    if (f != null) {
      const r = BS(f, c);
      if (r) return r;
    }
    c = c.parentElement;
  }
  return null;
}
function BS(i, c) {
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
      const m = Fh(c);
      return m ? { kind: "key", controlId: m, key: r, el: c } : null;
    }
    default:
      return null;
  }
}
function Fh(i) {
  let c = i;
  for (; c; ) {
    const o = c.id;
    if (o && Rb(o)) return o;
    c = c.parentElement;
  }
  return null;
}
function LS(i, c) {
  switch (i.kind) {
    case "text":
      return Promise.resolve({ text: i.text });
    case "html":
      return Promise.resolve({ html: i.html });
    case "key": {
      const o = i.controlId + "\0" + i.key;
      let f = Ci.get(o);
      return f || (f = Wv(i.controlId, i.key), Ci.set(o, f)), f;
    }
    case "dynamic": {
      const o = { target: c, resolved: null };
      i.host.dispatchEvent(new CustomEvent("tl-tooltip-resolve", { detail: o, bubbles: !1 }));
      const f = o.resolved;
      if (!f) return null;
      if ("inline" in f) return Promise.resolve(f.inline);
      const r = Fh(i.host);
      if (!r) return null;
      const m = r + "\0" + f.key;
      let v = Ci.get(m);
      return v || (v = Wv(r, f.key), Ci.set(m, v)), v;
    }
  }
}
function qS(i, c) {
  lu = window.setTimeout(async () => {
    lu = null;
    const o = await c;
    o && document.contains(i) && (ql = { anchor: i, data: o }, ec());
  }, DS);
}
function $h() {
  const i = ql != null && ql.data.interactive ? NS : wS;
  nu = window.setTimeout(() => {
    nu = null, ql = null, ec();
  }, i);
}
function Ih() {
  lu != null && (window.clearTimeout(lu), lu = null);
}
function Ph() {
  nu != null && (window.clearTimeout(nu), nu = null);
}
function ec() {
  if (!Hi || !fl) return;
  if (!ql) {
    Hi.render(null);
    return;
  }
  const { anchor: i, data: c } = ql;
  Hi.render(
    Jt.createElement(xS, {
      anchor: i,
      data: c,
      portalRoot: fl,
      onClose: () => {
        ql = null, ec();
      },
      onEnter: Ph,
      onLeave: $h
    })
  );
}
async function Wv(i, c) {
  const o = document.body.dataset.windowName ?? "", f = Yl() + `react-api/tooltip?controlId=${encodeURIComponent(i)}&key=${encodeURIComponent(c)}&windowName=${encodeURIComponent(o)}`, r = await fetch(f, { credentials: "same-origin" });
  return r.ok ? await r.json() : null;
}
window.TLReact = {
  mount: Wo,
  mountField: Ab,
  discoverAndMount: qi,
  subscribe: Io,
  unsubscribe: Rh,
  scrollToAnchor: Hb
};
Kg();
Wg();
document.readyState === "loading" ? window.addEventListener("DOMContentLoaded", () => kv(), { once: !0 }) : kv();
hb();
Tb();
export {
  dh as ANCHORED_OVERLAY_ATTR,
  Dg as CMD_SUBMIT,
  ah as CMD_VALUE_CHANGED,
  _h as FILL_CLASS,
  tp as FillBarrier,
  PS as FillProvider,
  JS as KeyboardScopeProvider,
  Jt as React,
  XS as ReactDOM,
  ep as TLChild,
  vn as TLControlContext,
  GS as anchoredOverlayProps,
  Ov as connect,
  Ob as createChildContext,
  qi as discoverAndMount,
  $v as getComponent,
  Rb as isMountedControl,
  Wo as mount,
  Ab as mountField,
  YS as register,
  Hb as scrollToAnchor,
  Io as subscribe,
  Li as unmount,
  Rh as unsubscribe,
  $S as useFill,
  IS as useFillHost,
  FS as useFocusTrap,
  jS as useI18N,
  kS as useKeyboardBinding,
  WS as useStandaloneKeyboardScope,
  gh as useTLCommand,
  VS as useTLDataUrl,
  ZS as useTLFieldValue,
  yh as useTLState,
  KS as useTLSubmitOnEnter,
  QS as useTLUpload
};
