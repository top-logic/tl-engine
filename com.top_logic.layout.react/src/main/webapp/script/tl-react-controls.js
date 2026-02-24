import { React as Z, useTLFieldValue as Ee, getComponent as Fd, useTLState as Zn, useTLCommand as Ln, register as Ut } from "tl-react-bridge";
const { useCallback: Id } = Z, Pd = ({ state: A }) => {
  const [M, D] = Ee(), m = Id(
    (G) => {
      D(G.target.value);
    },
    [D]
  );
  return /* @__PURE__ */ Z.createElement(
    "input",
    {
      type: "text",
      value: M ?? "",
      onChange: m,
      disabled: A.disabled === !0 || A.editable === !1,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: lo } = Z, to = ({ state: A, config: M }) => {
  const [D, m] = Ee(), G = lo(
    (W) => {
      const Sl = W.target.value, p = Sl === "" ? null : Number(Sl);
      m(p);
    },
    [m]
  ), L = M != null && M.decimal ? "0.01" : "1";
  return /* @__PURE__ */ Z.createElement(
    "input",
    {
      type: "number",
      value: D != null ? String(D) : "",
      onChange: G,
      step: L,
      disabled: A.disabled === !0 || A.editable === !1,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: ao } = Z, uo = ({ state: A }) => {
  const [M, D] = Ee(), m = ao(
    (G) => {
      D(G.target.value || null);
    },
    [D]
  );
  return /* @__PURE__ */ Z.createElement(
    "input",
    {
      type: "date",
      value: M ?? "",
      onChange: m,
      disabled: A.disabled === !0 || A.editable === !1,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: eo } = Z, no = ({ state: A, config: M }) => {
  const [D, m] = Ee(), G = eo(
    (W) => {
      m(W.target.value || null);
    },
    [m]
  ), L = A.options ?? (M == null ? void 0 : M.options) ?? [];
  return /* @__PURE__ */ Z.createElement(
    "select",
    {
      value: D ?? "",
      onChange: G,
      disabled: A.disabled === !0 || A.editable === !1,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ Z.createElement("option", { value: "" }),
    L.map((W) => /* @__PURE__ */ Z.createElement("option", { key: W.value, value: W.value }, W.label))
  );
}, { useCallback: fo } = Z, co = ({ state: A }) => {
  const [M, D] = Ee(), m = fo(
    (G) => {
      D(G.target.checked);
    },
    [D]
  );
  return /* @__PURE__ */ Z.createElement(
    "input",
    {
      type: "checkbox",
      checked: M === !0,
      onChange: m,
      disabled: A.disabled === !0 || A.editable === !1,
      className: "tlReactCheckbox"
    }
  );
}, io = ({ controlId: A, state: M }) => {
  const D = M.columns ?? [], m = M.rows ?? [];
  return /* @__PURE__ */ Z.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ Z.createElement("thead", null, /* @__PURE__ */ Z.createElement("tr", null, D.map((G) => /* @__PURE__ */ Z.createElement("th", { key: G.name }, G.label)))), /* @__PURE__ */ Z.createElement("tbody", null, m.map((G, L) => /* @__PURE__ */ Z.createElement("tr", { key: L }, D.map((W) => {
    const Sl = W.cellModule ? Fd(W.cellModule) : void 0, p = G[W.name];
    if (Sl) {
      const r = { value: p, editable: M.editable };
      return /* @__PURE__ */ Z.createElement("td", { key: W.name }, /* @__PURE__ */ Z.createElement(
        Sl,
        {
          controlId: A + "-" + L + "-" + W.name,
          state: r
        }
      ));
    }
    return /* @__PURE__ */ Z.createElement("td", { key: W.name }, p != null ? String(p) : "");
  })))));
}, { useCallback: so } = Z, vo = ({ command: A, label: M, disabled: D }) => {
  const m = Zn(), G = Ln(), L = A ?? "click", W = M ?? m.label, Sl = D ?? m.disabled === !0, p = so(() => {
    G(L);
  }, [G, L]);
  return /* @__PURE__ */ Z.createElement(
    "button",
    {
      type: "button",
      onClick: p,
      disabled: Sl,
      className: "tlReactButton"
    },
    W
  );
}, { useCallback: yo } = Z, mo = ({ command: A, label: M, active: D, disabled: m }) => {
  const G = Zn(), L = Ln(), W = A ?? "click", Sl = M ?? G.label, p = D ?? G.active === !0, r = m ?? G.disabled === !0, F = yo(() => {
    L(W);
  }, [L, W]);
  return /* @__PURE__ */ Z.createElement(
    "button",
    {
      type: "button",
      onClick: F,
      disabled: r,
      className: "tlReactButton" + (p ? " tlReactButtonActive" : "")
    },
    Sl
  );
}, oo = () => {
  const A = Zn(), M = Ln(), D = A.count ?? 0;
  return /* @__PURE__ */ Z.createElement("div", { className: "tlCounter" }, /* @__PURE__ */ Z.createElement("h3", { className: "tlCounter__title" }, "React Counter"), /* @__PURE__ */ Z.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ Z.createElement("button", { className: "tlCounter__button", onClick: () => M("decrement") }, "−"), /* @__PURE__ */ Z.createElement("span", { className: "tlCounter__value" }, D), /* @__PURE__ */ Z.createElement("button", { className: "tlCounter__button", onClick: () => M("increment") }, "+")), /* @__PURE__ */ Z.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
};
function ho(A) {
  return A && A.__esModule && Object.prototype.hasOwnProperty.call(A, "default") ? A.default : A;
}
var ii = { exports: {} }, Y = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var gy;
function So() {
  if (gy) return Y;
  gy = 1;
  var A = Symbol.for("react.transitional.element"), M = Symbol.for("react.portal"), D = Symbol.for("react.fragment"), m = Symbol.for("react.strict_mode"), G = Symbol.for("react.profiler"), L = Symbol.for("react.consumer"), W = Symbol.for("react.context"), Sl = Symbol.for("react.forward_ref"), p = Symbol.for("react.suspense"), r = Symbol.for("react.memo"), F = Symbol.for("react.lazy"), B = Symbol.for("react.activity"), ml = Symbol.iterator;
  function Wl(v) {
    return v === null || typeof v != "object" ? null : (v = ml && v[ml] || v["@@iterator"], typeof v == "function" ? v : null);
  }
  var Gl = {
    isMounted: function() {
      return !1;
    },
    enqueueForceUpdate: function() {
    },
    enqueueReplaceState: function() {
    },
    enqueueSetState: function() {
    }
  }, Bl = Object.assign, pt = {};
  function $l(v, E, O) {
    this.props = v, this.context = E, this.refs = pt, this.updater = O || Gl;
  }
  $l.prototype.isReactComponent = {}, $l.prototype.setState = function(v, E) {
    if (typeof v != "object" && typeof v != "function" && v != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, v, E, "setState");
  }, $l.prototype.forceUpdate = function(v) {
    this.updater.enqueueForceUpdate(this, v, "forceUpdate");
  };
  function $t() {
  }
  $t.prototype = $l.prototype;
  function Hl(v, E, O) {
    this.props = v, this.context = E, this.refs = pt, this.updater = O || Gl;
  }
  var ft = Hl.prototype = new $t();
  ft.constructor = Hl, Bl(ft, $l.prototype), ft.isPureReactComponent = !0;
  var Et = Array.isArray;
  function Xl() {
  }
  var $ = { H: null, A: null, T: null, S: null }, Ql = Object.prototype.hasOwnProperty;
  function At(v, E, O) {
    var C = O.ref;
    return {
      $$typeof: A,
      type: v,
      key: E,
      ref: C !== void 0 ? C : null,
      props: O
    };
  }
  function La(v, E) {
    return At(v.type, E, v.props);
  }
  function rt(v) {
    return typeof v == "object" && v !== null && v.$$typeof === A;
  }
  function jl(v) {
    var E = { "=": "=0", ":": "=2" };
    return "$" + v.replace(/[=:]/g, function(O) {
      return E[O];
    });
  }
  var Ea = /\/+/g;
  function Ct(v, E) {
    return typeof v == "object" && v !== null && v.key != null ? jl("" + v.key) : E.toString(36);
  }
  function gt(v) {
    switch (v.status) {
      case "fulfilled":
        return v.value;
      case "rejected":
        throw v.reason;
      default:
        switch (typeof v.status == "string" ? v.then(Xl, Xl) : (v.status = "pending", v.then(
          function(E) {
            v.status === "pending" && (v.status = "fulfilled", v.value = E);
          },
          function(E) {
            v.status === "pending" && (v.status = "rejected", v.reason = E);
          }
        )), v.status) {
          case "fulfilled":
            return v.value;
          case "rejected":
            throw v.reason;
        }
    }
    throw v;
  }
  function b(v, E, O, C, X) {
    var V = typeof v;
    (V === "undefined" || V === "boolean") && (v = null);
    var tl = !1;
    if (v === null) tl = !0;
    else
      switch (V) {
        case "bigint":
        case "string":
        case "number":
          tl = !0;
          break;
        case "object":
          switch (v.$$typeof) {
            case A:
            case M:
              tl = !0;
              break;
            case F:
              return tl = v._init, b(
                tl(v._payload),
                E,
                O,
                C,
                X
              );
          }
      }
    if (tl)
      return X = X(v), tl = C === "" ? "." + Ct(v, 0) : C, Et(X) ? (O = "", tl != null && (O = tl.replace(Ea, "$&/") + "/"), b(X, E, O, "", function(Du) {
        return Du;
      })) : X != null && (rt(X) && (X = La(
        X,
        O + (X.key == null || v && v.key === X.key ? "" : ("" + X.key).replace(
          Ea,
          "$&/"
        ) + "/") + tl
      )), E.push(X)), 1;
    tl = 0;
    var ql = C === "" ? "." : C + ":";
    if (Et(v))
      for (var gl = 0; gl < v.length; gl++)
        C = v[gl], V = ql + Ct(C, gl), tl += b(
          C,
          E,
          O,
          V,
          X
        );
    else if (gl = Wl(v), typeof gl == "function")
      for (v = gl.call(v), gl = 0; !(C = v.next()).done; )
        C = C.value, V = ql + Ct(C, gl++), tl += b(
          C,
          E,
          O,
          V,
          X
        );
    else if (V === "object") {
      if (typeof v.then == "function")
        return b(
          gt(v),
          E,
          O,
          C,
          X
        );
      throw E = String(v), Error(
        "Objects are not valid as a React child (found: " + (E === "[object Object]" ? "object with keys {" + Object.keys(v).join(", ") + "}" : E) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return tl;
  }
  function _(v, E, O) {
    if (v == null) return v;
    var C = [], X = 0;
    return b(v, C, "", "", function(V) {
      return E.call(O, V, X++);
    }), C;
  }
  function q(v) {
    if (v._status === -1) {
      var E = v._result;
      E = E(), E.then(
        function(O) {
          (v._status === 0 || v._status === -1) && (v._status = 1, v._result = O);
        },
        function(O) {
          (v._status === 0 || v._status === -1) && (v._status = 2, v._result = O);
        }
      ), v._status === -1 && (v._status = 0, v._result = E);
    }
    if (v._status === 1) return v._result.default;
    throw v._result;
  }
  var el = typeof reportError == "function" ? reportError : function(v) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var E = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof v == "object" && v !== null && typeof v.message == "string" ? String(v.message) : String(v),
        error: v
      });
      if (!window.dispatchEvent(E)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", v);
      return;
    }
    console.error(v);
  }, il = {
    map: _,
    forEach: function(v, E, O) {
      _(
        v,
        function() {
          E.apply(this, arguments);
        },
        O
      );
    },
    count: function(v) {
      var E = 0;
      return _(v, function() {
        E++;
      }), E;
    },
    toArray: function(v) {
      return _(v, function(E) {
        return E;
      }) || [];
    },
    only: function(v) {
      if (!rt(v))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return v;
    }
  };
  return Y.Activity = B, Y.Children = il, Y.Component = $l, Y.Fragment = D, Y.Profiler = G, Y.PureComponent = Hl, Y.StrictMode = m, Y.Suspense = p, Y.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = $, Y.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(v) {
      return $.H.useMemoCache(v);
    }
  }, Y.cache = function(v) {
    return function() {
      return v.apply(null, arguments);
    };
  }, Y.cacheSignal = function() {
    return null;
  }, Y.cloneElement = function(v, E, O) {
    if (v == null)
      throw Error(
        "The argument must be a React element, but you passed " + v + "."
      );
    var C = Bl({}, v.props), X = v.key;
    if (E != null)
      for (V in E.key !== void 0 && (X = "" + E.key), E)
        !Ql.call(E, V) || V === "key" || V === "__self" || V === "__source" || V === "ref" && E.ref === void 0 || (C[V] = E[V]);
    var V = arguments.length - 2;
    if (V === 1) C.children = O;
    else if (1 < V) {
      for (var tl = Array(V), ql = 0; ql < V; ql++)
        tl[ql] = arguments[ql + 2];
      C.children = tl;
    }
    return At(v.type, X, C);
  }, Y.createContext = function(v) {
    return v = {
      $$typeof: W,
      _currentValue: v,
      _currentValue2: v,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, v.Provider = v, v.Consumer = {
      $$typeof: L,
      _context: v
    }, v;
  }, Y.createElement = function(v, E, O) {
    var C, X = {}, V = null;
    if (E != null)
      for (C in E.key !== void 0 && (V = "" + E.key), E)
        Ql.call(E, C) && C !== "key" && C !== "__self" && C !== "__source" && (X[C] = E[C]);
    var tl = arguments.length - 2;
    if (tl === 1) X.children = O;
    else if (1 < tl) {
      for (var ql = Array(tl), gl = 0; gl < tl; gl++)
        ql[gl] = arguments[gl + 2];
      X.children = ql;
    }
    if (v && v.defaultProps)
      for (C in tl = v.defaultProps, tl)
        X[C] === void 0 && (X[C] = tl[C]);
    return At(v, V, X);
  }, Y.createRef = function() {
    return { current: null };
  }, Y.forwardRef = function(v) {
    return { $$typeof: Sl, render: v };
  }, Y.isValidElement = rt, Y.lazy = function(v) {
    return {
      $$typeof: F,
      _payload: { _status: -1, _result: v },
      _init: q
    };
  }, Y.memo = function(v, E) {
    return {
      $$typeof: r,
      type: v,
      compare: E === void 0 ? null : E
    };
  }, Y.startTransition = function(v) {
    var E = $.T, O = {};
    $.T = O;
    try {
      var C = v(), X = $.S;
      X !== null && X(O, C), typeof C == "object" && C !== null && typeof C.then == "function" && C.then(Xl, el);
    } catch (V) {
      el(V);
    } finally {
      E !== null && O.types !== null && (E.types = O.types), $.T = E;
    }
  }, Y.unstable_useCacheRefresh = function() {
    return $.H.useCacheRefresh();
  }, Y.use = function(v) {
    return $.H.use(v);
  }, Y.useActionState = function(v, E, O) {
    return $.H.useActionState(v, E, O);
  }, Y.useCallback = function(v, E) {
    return $.H.useCallback(v, E);
  }, Y.useContext = function(v) {
    return $.H.useContext(v);
  }, Y.useDebugValue = function() {
  }, Y.useDeferredValue = function(v, E) {
    return $.H.useDeferredValue(v, E);
  }, Y.useEffect = function(v, E) {
    return $.H.useEffect(v, E);
  }, Y.useEffectEvent = function(v) {
    return $.H.useEffectEvent(v);
  }, Y.useId = function() {
    return $.H.useId();
  }, Y.useImperativeHandle = function(v, E, O) {
    return $.H.useImperativeHandle(v, E, O);
  }, Y.useInsertionEffect = function(v, E) {
    return $.H.useInsertionEffect(v, E);
  }, Y.useLayoutEffect = function(v, E) {
    return $.H.useLayoutEffect(v, E);
  }, Y.useMemo = function(v, E) {
    return $.H.useMemo(v, E);
  }, Y.useOptimistic = function(v, E) {
    return $.H.useOptimistic(v, E);
  }, Y.useReducer = function(v, E, O) {
    return $.H.useReducer(v, E, O);
  }, Y.useRef = function(v) {
    return $.H.useRef(v);
  }, Y.useState = function(v) {
    return $.H.useState(v);
  }, Y.useSyncExternalStore = function(v, E, O) {
    return $.H.useSyncExternalStore(
      v,
      E,
      O
    );
  }, Y.useTransition = function() {
    return $.H.useTransition();
  }, Y.version = "19.2.4", Y;
}
var by;
function oi() {
  return by || (by = 1, ii.exports = So()), ii.exports;
}
var hi = oi();
const si = /* @__PURE__ */ ho(hi), go = /* @__PURE__ */ new Map();
function bo(A) {
  return go.get(A);
}
var vi = { exports: {} }, ze = {}, yi = { exports: {} }, mi = {};
/**
 * @license React
 * scheduler.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var Ty;
function To() {
  return Ty || (Ty = 1, (function(A) {
    function M(b, _) {
      var q = b.length;
      b.push(_);
      l: for (; 0 < q; ) {
        var el = q - 1 >>> 1, il = b[el];
        if (0 < G(il, _))
          b[el] = _, b[q] = il, q = el;
        else break l;
      }
    }
    function D(b) {
      return b.length === 0 ? null : b[0];
    }
    function m(b) {
      if (b.length === 0) return null;
      var _ = b[0], q = b.pop();
      if (q !== _) {
        b[0] = q;
        l: for (var el = 0, il = b.length, v = il >>> 1; el < v; ) {
          var E = 2 * (el + 1) - 1, O = b[E], C = E + 1, X = b[C];
          if (0 > G(O, q))
            C < il && 0 > G(X, O) ? (b[el] = X, b[C] = q, el = C) : (b[el] = O, b[E] = q, el = E);
          else if (C < il && 0 > G(X, q))
            b[el] = X, b[C] = q, el = C;
          else break l;
        }
      }
      return _;
    }
    function G(b, _) {
      var q = b.sortIndex - _.sortIndex;
      return q !== 0 ? q : b.id - _.id;
    }
    if (A.unstable_now = void 0, typeof performance == "object" && typeof performance.now == "function") {
      var L = performance;
      A.unstable_now = function() {
        return L.now();
      };
    } else {
      var W = Date, Sl = W.now();
      A.unstable_now = function() {
        return W.now() - Sl;
      };
    }
    var p = [], r = [], F = 1, B = null, ml = 3, Wl = !1, Gl = !1, Bl = !1, pt = !1, $l = typeof setTimeout == "function" ? setTimeout : null, $t = typeof clearTimeout == "function" ? clearTimeout : null, Hl = typeof setImmediate < "u" ? setImmediate : null;
    function ft(b) {
      for (var _ = D(r); _ !== null; ) {
        if (_.callback === null) m(r);
        else if (_.startTime <= b)
          m(r), _.sortIndex = _.expirationTime, M(p, _);
        else break;
        _ = D(r);
      }
    }
    function Et(b) {
      if (Bl = !1, ft(b), !Gl)
        if (D(p) !== null)
          Gl = !0, Xl || (Xl = !0, jl());
        else {
          var _ = D(r);
          _ !== null && gt(Et, _.startTime - b);
        }
    }
    var Xl = !1, $ = -1, Ql = 5, At = -1;
    function La() {
      return pt ? !0 : !(A.unstable_now() - At < Ql);
    }
    function rt() {
      if (pt = !1, Xl) {
        var b = A.unstable_now();
        At = b;
        var _ = !0;
        try {
          l: {
            Gl = !1, Bl && (Bl = !1, $t($), $ = -1), Wl = !0;
            var q = ml;
            try {
              t: {
                for (ft(b), B = D(p); B !== null && !(B.expirationTime > b && La()); ) {
                  var el = B.callback;
                  if (typeof el == "function") {
                    B.callback = null, ml = B.priorityLevel;
                    var il = el(
                      B.expirationTime <= b
                    );
                    if (b = A.unstable_now(), typeof il == "function") {
                      B.callback = il, ft(b), _ = !0;
                      break t;
                    }
                    B === D(p) && m(p), ft(b);
                  } else m(p);
                  B = D(p);
                }
                if (B !== null) _ = !0;
                else {
                  var v = D(r);
                  v !== null && gt(
                    Et,
                    v.startTime - b
                  ), _ = !1;
                }
              }
              break l;
            } finally {
              B = null, ml = q, Wl = !1;
            }
            _ = void 0;
          }
        } finally {
          _ ? jl() : Xl = !1;
        }
      }
    }
    var jl;
    if (typeof Hl == "function")
      jl = function() {
        Hl(rt);
      };
    else if (typeof MessageChannel < "u") {
      var Ea = new MessageChannel(), Ct = Ea.port2;
      Ea.port1.onmessage = rt, jl = function() {
        Ct.postMessage(null);
      };
    } else
      jl = function() {
        $l(rt, 0);
      };
    function gt(b, _) {
      $ = $l(function() {
        b(A.unstable_now());
      }, _);
    }
    A.unstable_IdlePriority = 5, A.unstable_ImmediatePriority = 1, A.unstable_LowPriority = 4, A.unstable_NormalPriority = 3, A.unstable_Profiling = null, A.unstable_UserBlockingPriority = 2, A.unstable_cancelCallback = function(b) {
      b.callback = null;
    }, A.unstable_forceFrameRate = function(b) {
      0 > b || 125 < b ? console.error(
        "forceFrameRate takes a positive int between 0 and 125, forcing frame rates higher than 125 fps is not supported"
      ) : Ql = 0 < b ? Math.floor(1e3 / b) : 5;
    }, A.unstable_getCurrentPriorityLevel = function() {
      return ml;
    }, A.unstable_next = function(b) {
      switch (ml) {
        case 1:
        case 2:
        case 3:
          var _ = 3;
          break;
        default:
          _ = ml;
      }
      var q = ml;
      ml = _;
      try {
        return b();
      } finally {
        ml = q;
      }
    }, A.unstable_requestPaint = function() {
      pt = !0;
    }, A.unstable_runWithPriority = function(b, _) {
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
      var q = ml;
      ml = b;
      try {
        return _();
      } finally {
        ml = q;
      }
    }, A.unstable_scheduleCallback = function(b, _, q) {
      var el = A.unstable_now();
      switch (typeof q == "object" && q !== null ? (q = q.delay, q = typeof q == "number" && 0 < q ? el + q : el) : q = el, b) {
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
      return il = q + il, b = {
        id: F++,
        callback: _,
        priorityLevel: b,
        startTime: q,
        expirationTime: il,
        sortIndex: -1
      }, q > el ? (b.sortIndex = q, M(r, b), D(p) === null && b === D(r) && (Bl ? ($t($), $ = -1) : Bl = !0, gt(Et, q - el))) : (b.sortIndex = il, M(p, b), Gl || Wl || (Gl = !0, Xl || (Xl = !0, jl()))), b;
    }, A.unstable_shouldYield = La, A.unstable_wrapCallback = function(b) {
      var _ = ml;
      return function() {
        var q = ml;
        ml = _;
        try {
          return b.apply(this, arguments);
        } finally {
          ml = q;
        }
      };
    };
  })(mi)), mi;
}
var zy;
function zo() {
  return zy || (zy = 1, yi.exports = To()), yi.exports;
}
var di = { exports: {} }, Rl = {};
/**
 * @license React
 * react-dom.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var Ey;
function Eo() {
  if (Ey) return Rl;
  Ey = 1;
  var A = oi();
  function M(p) {
    var r = "https://react.dev/errors/" + p;
    if (1 < arguments.length) {
      r += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var F = 2; F < arguments.length; F++)
        r += "&args[]=" + encodeURIComponent(arguments[F]);
    }
    return "Minified React error #" + p + "; visit " + r + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function D() {
  }
  var m = {
    d: {
      f: D,
      r: function() {
        throw Error(M(522));
      },
      D,
      C: D,
      L: D,
      m: D,
      X: D,
      S: D,
      M: D
    },
    p: 0,
    findDOMNode: null
  }, G = Symbol.for("react.portal");
  function L(p, r, F) {
    var B = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: G,
      key: B == null ? null : "" + B,
      children: p,
      containerInfo: r,
      implementation: F
    };
  }
  var W = A.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function Sl(p, r) {
    if (p === "font") return "";
    if (typeof r == "string")
      return r === "use-credentials" ? r : "";
  }
  return Rl.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = m, Rl.createPortal = function(p, r) {
    var F = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!r || r.nodeType !== 1 && r.nodeType !== 9 && r.nodeType !== 11)
      throw Error(M(299));
    return L(p, r, null, F);
  }, Rl.flushSync = function(p) {
    var r = W.T, F = m.p;
    try {
      if (W.T = null, m.p = 2, p) return p();
    } finally {
      W.T = r, m.p = F, m.d.f();
    }
  }, Rl.preconnect = function(p, r) {
    typeof p == "string" && (r ? (r = r.crossOrigin, r = typeof r == "string" ? r === "use-credentials" ? r : "" : void 0) : r = null, m.d.C(p, r));
  }, Rl.prefetchDNS = function(p) {
    typeof p == "string" && m.d.D(p);
  }, Rl.preinit = function(p, r) {
    if (typeof p == "string" && r && typeof r.as == "string") {
      var F = r.as, B = Sl(F, r.crossOrigin), ml = typeof r.integrity == "string" ? r.integrity : void 0, Wl = typeof r.fetchPriority == "string" ? r.fetchPriority : void 0;
      F === "style" ? m.d.S(
        p,
        typeof r.precedence == "string" ? r.precedence : void 0,
        {
          crossOrigin: B,
          integrity: ml,
          fetchPriority: Wl
        }
      ) : F === "script" && m.d.X(p, {
        crossOrigin: B,
        integrity: ml,
        fetchPriority: Wl,
        nonce: typeof r.nonce == "string" ? r.nonce : void 0
      });
    }
  }, Rl.preinitModule = function(p, r) {
    if (typeof p == "string")
      if (typeof r == "object" && r !== null) {
        if (r.as == null || r.as === "script") {
          var F = Sl(
            r.as,
            r.crossOrigin
          );
          m.d.M(p, {
            crossOrigin: F,
            integrity: typeof r.integrity == "string" ? r.integrity : void 0,
            nonce: typeof r.nonce == "string" ? r.nonce : void 0
          });
        }
      } else r == null && m.d.M(p);
  }, Rl.preload = function(p, r) {
    if (typeof p == "string" && typeof r == "object" && r !== null && typeof r.as == "string") {
      var F = r.as, B = Sl(F, r.crossOrigin);
      m.d.L(p, F, {
        crossOrigin: B,
        integrity: typeof r.integrity == "string" ? r.integrity : void 0,
        nonce: typeof r.nonce == "string" ? r.nonce : void 0,
        type: typeof r.type == "string" ? r.type : void 0,
        fetchPriority: typeof r.fetchPriority == "string" ? r.fetchPriority : void 0,
        referrerPolicy: typeof r.referrerPolicy == "string" ? r.referrerPolicy : void 0,
        imageSrcSet: typeof r.imageSrcSet == "string" ? r.imageSrcSet : void 0,
        imageSizes: typeof r.imageSizes == "string" ? r.imageSizes : void 0,
        media: typeof r.media == "string" ? r.media : void 0
      });
    }
  }, Rl.preloadModule = function(p, r) {
    if (typeof p == "string")
      if (r) {
        var F = Sl(r.as, r.crossOrigin);
        m.d.m(p, {
          as: typeof r.as == "string" && r.as !== "script" ? r.as : void 0,
          crossOrigin: F,
          integrity: typeof r.integrity == "string" ? r.integrity : void 0
        });
      } else m.d.m(p);
  }, Rl.requestFormReset = function(p) {
    m.d.r(p);
  }, Rl.unstable_batchedUpdates = function(p, r) {
    return p(r);
  }, Rl.useFormState = function(p, r, F) {
    return W.H.useFormState(p, r, F);
  }, Rl.useFormStatus = function() {
    return W.H.useHostTransitionStatus();
  }, Rl.version = "19.2.4", Rl;
}
var Ay;
function Ao() {
  if (Ay) return di.exports;
  Ay = 1;
  function A() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(A);
      } catch (M) {
        console.error(M);
      }
  }
  return A(), di.exports = Eo(), di.exports;
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
var ry;
function ro() {
  if (ry) return ze;
  ry = 1;
  var A = zo(), M = oi(), D = Ao();
  function m(l) {
    var t = "https://react.dev/errors/" + l;
    if (1 < arguments.length) {
      t += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var a = 2; a < arguments.length; a++)
        t += "&args[]=" + encodeURIComponent(arguments[a]);
    }
    return "Minified React error #" + l + "; visit " + t + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function G(l) {
    return !(!l || l.nodeType !== 1 && l.nodeType !== 9 && l.nodeType !== 11);
  }
  function L(l) {
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
  function W(l) {
    if (l.tag === 13) {
      var t = l.memoizedState;
      if (t === null && (l = l.alternate, l !== null && (t = l.memoizedState)), t !== null) return t.dehydrated;
    }
    return null;
  }
  function Sl(l) {
    if (l.tag === 31) {
      var t = l.memoizedState;
      if (t === null && (l = l.alternate, l !== null && (t = l.memoizedState)), t !== null) return t.dehydrated;
    }
    return null;
  }
  function p(l) {
    if (L(l) !== l)
      throw Error(m(188));
  }
  function r(l) {
    var t = l.alternate;
    if (!t) {
      if (t = L(l), t === null) throw Error(m(188));
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
          if (n === a) return p(e), l;
          if (n === u) return p(e), t;
          n = n.sibling;
        }
        throw Error(m(188));
      }
      if (a.return !== u.return) a = e, u = n;
      else {
        for (var f = !1, c = e.child; c; ) {
          if (c === a) {
            f = !0, a = e, u = n;
            break;
          }
          if (c === u) {
            f = !0, u = e, a = n;
            break;
          }
          c = c.sibling;
        }
        if (!f) {
          for (c = n.child; c; ) {
            if (c === a) {
              f = !0, a = n, u = e;
              break;
            }
            if (c === u) {
              f = !0, u = n, a = e;
              break;
            }
            c = c.sibling;
          }
          if (!f) throw Error(m(189));
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
  var B = Object.assign, ml = Symbol.for("react.element"), Wl = Symbol.for("react.transitional.element"), Gl = Symbol.for("react.portal"), Bl = Symbol.for("react.fragment"), pt = Symbol.for("react.strict_mode"), $l = Symbol.for("react.profiler"), $t = Symbol.for("react.consumer"), Hl = Symbol.for("react.context"), ft = Symbol.for("react.forward_ref"), Et = Symbol.for("react.suspense"), Xl = Symbol.for("react.suspense_list"), $ = Symbol.for("react.memo"), Ql = Symbol.for("react.lazy"), At = Symbol.for("react.activity"), La = Symbol.for("react.memo_cache_sentinel"), rt = Symbol.iterator;
  function jl(l) {
    return l === null || typeof l != "object" ? null : (l = rt && l[rt] || l["@@iterator"], typeof l == "function" ? l : null);
  }
  var Ea = Symbol.for("react.client.reference");
  function Ct(l) {
    if (l == null) return null;
    if (typeof l == "function")
      return l.$$typeof === Ea ? null : l.displayName || l.name || null;
    if (typeof l == "string") return l;
    switch (l) {
      case Bl:
        return "Fragment";
      case $l:
        return "Profiler";
      case pt:
        return "StrictMode";
      case Et:
        return "Suspense";
      case Xl:
        return "SuspenseList";
      case At:
        return "Activity";
    }
    if (typeof l == "object")
      switch (l.$$typeof) {
        case Gl:
          return "Portal";
        case Hl:
          return l.displayName || "Context";
        case $t:
          return (l._context.displayName || "Context") + ".Consumer";
        case ft:
          var t = l.render;
          return l = l.displayName, l || (l = t.displayName || t.name || "", l = l !== "" ? "ForwardRef(" + l + ")" : "ForwardRef"), l;
        case $:
          return t = l.displayName || null, t !== null ? t : Ct(l.type) || "Memo";
        case Ql:
          t = l._payload, l = l._init;
          try {
            return Ct(l(t));
          } catch {
          }
      }
    return null;
  }
  var gt = Array.isArray, b = M.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE, _ = D.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE, q = {
    pending: !1,
    data: null,
    method: null,
    action: null
  }, el = [], il = -1;
  function v(l) {
    return { current: l };
  }
  function E(l) {
    0 > il || (l.current = el[il], el[il] = null, il--);
  }
  function O(l, t) {
    il++, el[il] = l.current, l.current = t;
  }
  var C = v(null), X = v(null), V = v(null), tl = v(null);
  function ql(l, t) {
    switch (O(V, t), O(X, l), O(C, null), t.nodeType) {
      case 9:
      case 11:
        l = (l = t.documentElement) && (l = l.namespaceURI) ? jv(l) : 0;
        break;
      default:
        if (l = t.tagName, t = t.namespaceURI)
          t = jv(t), l = Zv(t, l);
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
    E(C), O(C, l);
  }
  function gl() {
    E(C), E(X), E(V);
  }
  function Du(l) {
    l.memoizedState !== null && O(tl, l);
    var t = C.current, a = Zv(t, l.type);
    t !== a && (O(X, l), O(C, a));
  }
  function Ae(l) {
    X.current === l && (E(C), E(X)), tl.current === l && (E(tl), Se._currentValue = q);
  }
  var Vn, Si;
  function Aa(l) {
    if (Vn === void 0)
      try {
        throw Error();
      } catch (a) {
        var t = a.stack.trim().match(/\n( *(at )?)/);
        Vn = t && t[1] || "", Si = -1 < a.stack.indexOf(`
    at`) ? " (<anonymous>)" : -1 < a.stack.indexOf("@") ? "@unknown:0:0" : "";
      }
    return `
` + Vn + l + Si;
  }
  var xn = !1;
  function Kn(l, t) {
    if (!l || xn) return "";
    xn = !0;
    var a = Error.prepareStackTrace;
    Error.prepareStackTrace = void 0;
    try {
      var u = {
        DetermineComponentFrameRoot: function() {
          try {
            if (t) {
              var z = function() {
                throw Error();
              };
              if (Object.defineProperty(z.prototype, "props", {
                set: function() {
                  throw Error();
                }
              }), typeof Reflect == "object" && Reflect.construct) {
                try {
                  Reflect.construct(z, []);
                } catch (S) {
                  var h = S;
                }
                Reflect.construct(l, [], z);
              } else {
                try {
                  z.call();
                } catch (S) {
                  h = S;
                }
                l.call(z.prototype);
              }
            } else {
              try {
                throw Error();
              } catch (S) {
                h = S;
              }
              (z = l()) && typeof z.catch == "function" && z.catch(function() {
              });
            }
          } catch (S) {
            if (S && h && typeof S.stack == "string")
              return [S.stack, h.stack];
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
      var n = u.DetermineComponentFrameRoot(), f = n[0], c = n[1];
      if (f && c) {
        var i = f.split(`
`), o = c.split(`
`);
        for (e = u = 0; u < i.length && !i[u].includes("DetermineComponentFrameRoot"); )
          u++;
        for (; e < o.length && !o[e].includes(
          "DetermineComponentFrameRoot"
        ); )
          e++;
        if (u === i.length || e === o.length)
          for (u = i.length - 1, e = o.length - 1; 1 <= u && 0 <= e && i[u] !== o[e]; )
            e--;
        for (; 1 <= u && 0 <= e; u--, e--)
          if (i[u] !== o[e]) {
            if (u !== 1 || e !== 1)
              do
                if (u--, e--, 0 > e || i[u] !== o[e]) {
                  var g = `
` + i[u].replace(" at new ", " at ");
                  return l.displayName && g.includes("<anonymous>") && (g = g.replace("<anonymous>", l.displayName)), g;
                }
              while (1 <= u && 0 <= e);
            break;
          }
      }
    } finally {
      xn = !1, Error.prepareStackTrace = a;
    }
    return (a = l ? l.displayName || l.name : "") ? Aa(a) : "";
  }
  function Uy(l, t) {
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
        return Kn(l.type, !1);
      case 11:
        return Kn(l.type.render, !1);
      case 1:
        return Kn(l.type, !0);
      case 31:
        return Aa("Activity");
      default:
        return "";
    }
  }
  function gi(l) {
    try {
      var t = "", a = null;
      do
        t += Uy(l, a), a = l, l = l.return;
      while (l);
      return t;
    } catch (u) {
      return `
Error generating stack: ` + u.message + `
` + u.stack;
    }
  }
  var Jn = Object.prototype.hasOwnProperty, wn = A.unstable_scheduleCallback, Wn = A.unstable_cancelCallback, py = A.unstable_shouldYield, Cy = A.unstable_requestPaint, kl = A.unstable_now, Ny = A.unstable_getCurrentPriorityLevel, bi = A.unstable_ImmediatePriority, Ti = A.unstable_UserBlockingPriority, re = A.unstable_NormalPriority, Hy = A.unstable_LowPriority, zi = A.unstable_IdlePriority, Ry = A.log, By = A.unstable_setDisableYieldValue, Uu = null, Fl = null;
  function kt(l) {
    if (typeof Ry == "function" && By(l), Fl && typeof Fl.setStrictMode == "function")
      try {
        Fl.setStrictMode(Uu, l);
      } catch {
      }
  }
  var Il = Math.clz32 ? Math.clz32 : Gy, qy = Math.log, Yy = Math.LN2;
  function Gy(l) {
    return l >>>= 0, l === 0 ? 32 : 31 - (qy(l) / Yy | 0) | 0;
  }
  var _e = 256, Oe = 262144, Me = 4194304;
  function ra(l) {
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
  function De(l, t, a) {
    var u = l.pendingLanes;
    if (u === 0) return 0;
    var e = 0, n = l.suspendedLanes, f = l.pingedLanes;
    l = l.warmLanes;
    var c = u & 134217727;
    return c !== 0 ? (u = c & ~n, u !== 0 ? e = ra(u) : (f &= c, f !== 0 ? e = ra(f) : a || (a = c & ~l, a !== 0 && (e = ra(a))))) : (c = u & ~n, c !== 0 ? e = ra(c) : f !== 0 ? e = ra(f) : a || (a = u & ~l, a !== 0 && (e = ra(a)))), e === 0 ? 0 : t !== 0 && t !== e && (t & n) === 0 && (n = e & -e, a = t & -t, n >= a || n === 32 && (a & 4194048) !== 0) ? t : e;
  }
  function pu(l, t) {
    return (l.pendingLanes & ~(l.suspendedLanes & ~l.pingedLanes) & t) === 0;
  }
  function Xy(l, t) {
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
  function Ei() {
    var l = Me;
    return Me <<= 1, (Me & 62914560) === 0 && (Me = 4194304), l;
  }
  function $n(l) {
    for (var t = [], a = 0; 31 > a; a++) t.push(l);
    return t;
  }
  function Cu(l, t) {
    l.pendingLanes |= t, t !== 268435456 && (l.suspendedLanes = 0, l.pingedLanes = 0, l.warmLanes = 0);
  }
  function Qy(l, t, a, u, e, n) {
    var f = l.pendingLanes;
    l.pendingLanes = a, l.suspendedLanes = 0, l.pingedLanes = 0, l.warmLanes = 0, l.expiredLanes &= a, l.entangledLanes &= a, l.errorRecoveryDisabledLanes &= a, l.shellSuspendCounter = 0;
    var c = l.entanglements, i = l.expirationTimes, o = l.hiddenUpdates;
    for (a = f & ~a; 0 < a; ) {
      var g = 31 - Il(a), z = 1 << g;
      c[g] = 0, i[g] = -1;
      var h = o[g];
      if (h !== null)
        for (o[g] = null, g = 0; g < h.length; g++) {
          var S = h[g];
          S !== null && (S.lane &= -536870913);
        }
      a &= ~z;
    }
    u !== 0 && Ai(l, u, 0), n !== 0 && e === 0 && l.tag !== 0 && (l.suspendedLanes |= n & ~(f & ~t));
  }
  function Ai(l, t, a) {
    l.pendingLanes |= t, l.suspendedLanes &= ~t;
    var u = 31 - Il(t);
    l.entangledLanes |= t, l.entanglements[u] = l.entanglements[u] | 1073741824 | a & 261930;
  }
  function ri(l, t) {
    var a = l.entangledLanes |= t;
    for (l = l.entanglements; a; ) {
      var u = 31 - Il(a), e = 1 << u;
      e & t | l[u] & t && (l[u] |= t), a &= ~e;
    }
  }
  function _i(l, t) {
    var a = t & -t;
    return a = (a & 42) !== 0 ? 1 : kn(a), (a & (l.suspendedLanes | t)) !== 0 ? 0 : a;
  }
  function kn(l) {
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
  function Fn(l) {
    return l &= -l, 2 < l ? 8 < l ? (l & 134217727) !== 0 ? 32 : 268435456 : 8 : 2;
  }
  function Oi() {
    var l = _.p;
    return l !== 0 ? l : (l = window.event, l === void 0 ? 32 : vy(l.type));
  }
  function Mi(l, t) {
    var a = _.p;
    try {
      return _.p = l, t();
    } finally {
      _.p = a;
    }
  }
  var Ft = Math.random().toString(36).slice(2), Dl = "__reactFiber$" + Ft, Zl = "__reactProps$" + Ft, Va = "__reactContainer$" + Ft, In = "__reactEvents$" + Ft, jy = "__reactListeners$" + Ft, Zy = "__reactHandles$" + Ft, Di = "__reactResources$" + Ft, Nu = "__reactMarker$" + Ft;
  function Pn(l) {
    delete l[Dl], delete l[Zl], delete l[In], delete l[jy], delete l[Zy];
  }
  function xa(l) {
    var t = l[Dl];
    if (t) return t;
    for (var a = l.parentNode; a; ) {
      if (t = a[Va] || a[Dl]) {
        if (a = t.alternate, t.child !== null || a !== null && a.child !== null)
          for (l = Wv(l); l !== null; ) {
            if (a = l[Dl]) return a;
            l = Wv(l);
          }
        return t;
      }
      l = a, a = l.parentNode;
    }
    return null;
  }
  function Ka(l) {
    if (l = l[Dl] || l[Va]) {
      var t = l.tag;
      if (t === 5 || t === 6 || t === 13 || t === 31 || t === 26 || t === 27 || t === 3)
        return l;
    }
    return null;
  }
  function Hu(l) {
    var t = l.tag;
    if (t === 5 || t === 26 || t === 27 || t === 6) return l.stateNode;
    throw Error(m(33));
  }
  function Ja(l) {
    var t = l[Di];
    return t || (t = l[Di] = { hoistableStyles: /* @__PURE__ */ new Map(), hoistableScripts: /* @__PURE__ */ new Map() }), t;
  }
  function Ol(l) {
    l[Nu] = !0;
  }
  var Ui = /* @__PURE__ */ new Set(), pi = {};
  function _a(l, t) {
    wa(l, t), wa(l + "Capture", t);
  }
  function wa(l, t) {
    for (pi[l] = t, l = 0; l < t.length; l++)
      Ui.add(t[l]);
  }
  var Ly = RegExp(
    "^[:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD][:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD\\-.0-9\\u00B7\\u0300-\\u036F\\u203F-\\u2040]*$"
  ), Ci = {}, Ni = {};
  function Vy(l) {
    return Jn.call(Ni, l) ? !0 : Jn.call(Ci, l) ? !1 : Ly.test(l) ? Ni[l] = !0 : (Ci[l] = !0, !1);
  }
  function Ue(l, t, a) {
    if (Vy(t))
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
  function pe(l, t, a) {
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
  function Hi(l) {
    var t = l.type;
    return (l = l.nodeName) && l.toLowerCase() === "input" && (t === "checkbox" || t === "radio");
  }
  function xy(l, t, a) {
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
        set: function(f) {
          a = "" + f, n.call(this, f);
        }
      }), Object.defineProperty(l, t, {
        enumerable: u.enumerable
      }), {
        getValue: function() {
          return a;
        },
        setValue: function(f) {
          a = "" + f;
        },
        stopTracking: function() {
          l._valueTracker = null, delete l[t];
        }
      };
    }
  }
  function lf(l) {
    if (!l._valueTracker) {
      var t = Hi(l) ? "checked" : "value";
      l._valueTracker = xy(
        l,
        t,
        "" + l[t]
      );
    }
  }
  function Ri(l) {
    if (!l) return !1;
    var t = l._valueTracker;
    if (!t) return !0;
    var a = t.getValue(), u = "";
    return l && (u = Hi(l) ? l.checked ? "true" : "false" : l.value), l = u, l !== a ? (t.setValue(l), !0) : !1;
  }
  function Ce(l) {
    if (l = l || (typeof document < "u" ? document : void 0), typeof l > "u") return null;
    try {
      return l.activeElement || l.body;
    } catch {
      return l.body;
    }
  }
  var Ky = /[\n"\\]/g;
  function it(l) {
    return l.replace(
      Ky,
      function(t) {
        return "\\" + t.charCodeAt(0).toString(16) + " ";
      }
    );
  }
  function tf(l, t, a, u, e, n, f, c) {
    l.name = "", f != null && typeof f != "function" && typeof f != "symbol" && typeof f != "boolean" ? l.type = f : l.removeAttribute("type"), t != null ? f === "number" ? (t === 0 && l.value === "" || l.value != t) && (l.value = "" + ct(t)) : l.value !== "" + ct(t) && (l.value = "" + ct(t)) : f !== "submit" && f !== "reset" || l.removeAttribute("value"), t != null ? af(l, f, ct(t)) : a != null ? af(l, f, ct(a)) : u != null && l.removeAttribute("value"), e == null && n != null && (l.defaultChecked = !!n), e != null && (l.checked = e && typeof e != "function" && typeof e != "symbol"), c != null && typeof c != "function" && typeof c != "symbol" && typeof c != "boolean" ? l.name = "" + ct(c) : l.removeAttribute("name");
  }
  function Bi(l, t, a, u, e, n, f, c) {
    if (n != null && typeof n != "function" && typeof n != "symbol" && typeof n != "boolean" && (l.type = n), t != null || a != null) {
      if (!(n !== "submit" && n !== "reset" || t != null)) {
        lf(l);
        return;
      }
      a = a != null ? "" + ct(a) : "", t = t != null ? "" + ct(t) : a, c || t === l.value || (l.value = t), l.defaultValue = t;
    }
    u = u ?? e, u = typeof u != "function" && typeof u != "symbol" && !!u, l.checked = c ? l.checked : !!u, l.defaultChecked = !!u, f != null && typeof f != "function" && typeof f != "symbol" && typeof f != "boolean" && (l.name = f), lf(l);
  }
  function af(l, t, a) {
    t === "number" && Ce(l.ownerDocument) === l || l.defaultValue === "" + a || (l.defaultValue = "" + a);
  }
  function Wa(l, t, a, u) {
    if (l = l.options, t) {
      t = {};
      for (var e = 0; e < a.length; e++)
        t["$" + a[e]] = !0;
      for (a = 0; a < l.length; a++)
        e = t.hasOwnProperty("$" + l[a].value), l[a].selected !== e && (l[a].selected = e), e && u && (l[a].defaultSelected = !0);
    } else {
      for (a = "" + ct(a), t = null, e = 0; e < l.length; e++) {
        if (l[e].value === a) {
          l[e].selected = !0, u && (l[e].defaultSelected = !0);
          return;
        }
        t !== null || l[e].disabled || (t = l[e]);
      }
      t !== null && (t.selected = !0);
    }
  }
  function qi(l, t, a) {
    if (t != null && (t = "" + ct(t), t !== l.value && (l.value = t), a == null)) {
      l.defaultValue !== t && (l.defaultValue = t);
      return;
    }
    l.defaultValue = a != null ? "" + ct(a) : "";
  }
  function Yi(l, t, a, u) {
    if (t == null) {
      if (u != null) {
        if (a != null) throw Error(m(92));
        if (gt(u)) {
          if (1 < u.length) throw Error(m(93));
          u = u[0];
        }
        a = u;
      }
      a == null && (a = ""), t = a;
    }
    a = ct(t), l.defaultValue = a, u = l.textContent, u === a && u !== "" && u !== null && (l.value = u), lf(l);
  }
  function $a(l, t) {
    if (t) {
      var a = l.firstChild;
      if (a && a === l.lastChild && a.nodeType === 3) {
        a.nodeValue = t;
        return;
      }
    }
    l.textContent = t;
  }
  var Jy = new Set(
    "animationIterationCount aspectRatio borderImageOutset borderImageSlice borderImageWidth boxFlex boxFlexGroup boxOrdinalGroup columnCount columns flex flexGrow flexPositive flexShrink flexNegative flexOrder gridArea gridRow gridRowEnd gridRowSpan gridRowStart gridColumn gridColumnEnd gridColumnSpan gridColumnStart fontWeight lineClamp lineHeight opacity order orphans scale tabSize widows zIndex zoom fillOpacity floodOpacity stopOpacity strokeDasharray strokeDashoffset strokeMiterlimit strokeOpacity strokeWidth MozAnimationIterationCount MozBoxFlex MozBoxFlexGroup MozLineClamp msAnimationIterationCount msFlex msZoom msFlexGrow msFlexNegative msFlexOrder msFlexPositive msFlexShrink msGridColumn msGridColumnSpan msGridRow msGridRowSpan WebkitAnimationIterationCount WebkitBoxFlex WebKitBoxFlexGroup WebkitBoxOrdinalGroup WebkitColumnCount WebkitColumns WebkitFlex WebkitFlexGrow WebkitFlexPositive WebkitFlexShrink WebkitLineClamp".split(
      " "
    )
  );
  function Gi(l, t, a) {
    var u = t.indexOf("--") === 0;
    a == null || typeof a == "boolean" || a === "" ? u ? l.setProperty(t, "") : t === "float" ? l.cssFloat = "" : l[t] = "" : u ? l.setProperty(t, a) : typeof a != "number" || a === 0 || Jy.has(t) ? t === "float" ? l.cssFloat = a : l[t] = ("" + a).trim() : l[t] = a + "px";
  }
  function Xi(l, t, a) {
    if (t != null && typeof t != "object")
      throw Error(m(62));
    if (l = l.style, a != null) {
      for (var u in a)
        !a.hasOwnProperty(u) || t != null && t.hasOwnProperty(u) || (u.indexOf("--") === 0 ? l.setProperty(u, "") : u === "float" ? l.cssFloat = "" : l[u] = "");
      for (var e in t)
        u = t[e], t.hasOwnProperty(e) && a[e] !== u && Gi(l, e, u);
    } else
      for (var n in t)
        t.hasOwnProperty(n) && Gi(l, n, t[n]);
  }
  function uf(l) {
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
  var wy = /* @__PURE__ */ new Map([
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
  ]), Wy = /^[\u0000-\u001F ]*j[\r\n\t]*a[\r\n\t]*v[\r\n\t]*a[\r\n\t]*s[\r\n\t]*c[\r\n\t]*r[\r\n\t]*i[\r\n\t]*p[\r\n\t]*t[\r\n\t]*:/i;
  function Ne(l) {
    return Wy.test("" + l) ? "javascript:throw new Error('React has blocked a javascript: URL as a security precaution.')" : l;
  }
  function Ht() {
  }
  var ef = null;
  function nf(l) {
    return l = l.target || l.srcElement || window, l.correspondingUseElement && (l = l.correspondingUseElement), l.nodeType === 3 ? l.parentNode : l;
  }
  var ka = null, Fa = null;
  function Qi(l) {
    var t = Ka(l);
    if (t && (l = t.stateNode)) {
      var a = l[Zl] || null;
      l: switch (l = t.stateNode, t.type) {
        case "input":
          if (tf(
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
                tf(
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
              u = a[t], u.form === l.form && Ri(u);
          }
          break l;
        case "textarea":
          qi(l, a.value, a.defaultValue);
          break l;
        case "select":
          t = a.value, t != null && Wa(l, !!a.multiple, t, !1);
      }
    }
  }
  var ff = !1;
  function ji(l, t, a) {
    if (ff) return l(t, a);
    ff = !0;
    try {
      var u = l(t);
      return u;
    } finally {
      if (ff = !1, (ka !== null || Fa !== null) && (Tn(), ka && (t = ka, l = Fa, Fa = ka = null, Qi(t), l)))
        for (t = 0; t < l.length; t++) Qi(l[t]);
    }
  }
  function Ru(l, t) {
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
  var Rt = !(typeof window > "u" || typeof window.document > "u" || typeof window.document.createElement > "u"), cf = !1;
  if (Rt)
    try {
      var Bu = {};
      Object.defineProperty(Bu, "passive", {
        get: function() {
          cf = !0;
        }
      }), window.addEventListener("test", Bu, Bu), window.removeEventListener("test", Bu, Bu);
    } catch {
      cf = !1;
    }
  var It = null, sf = null, He = null;
  function Zi() {
    if (He) return He;
    var l, t = sf, a = t.length, u, e = "value" in It ? It.value : It.textContent, n = e.length;
    for (l = 0; l < a && t[l] === e[l]; l++) ;
    var f = a - l;
    for (u = 1; u <= f && t[a - u] === e[n - u]; u++) ;
    return He = e.slice(l, 1 < u ? 1 - u : void 0);
  }
  function Re(l) {
    var t = l.keyCode;
    return "charCode" in l ? (l = l.charCode, l === 0 && t === 13 && (l = 13)) : l = t, l === 10 && (l = 13), 32 <= l || l === 13 ? l : 0;
  }
  function Be() {
    return !0;
  }
  function Li() {
    return !1;
  }
  function Ll(l) {
    function t(a, u, e, n, f) {
      this._reactName = a, this._targetInst = e, this.type = u, this.nativeEvent = n, this.target = f, this.currentTarget = null;
      for (var c in l)
        l.hasOwnProperty(c) && (a = l[c], this[c] = a ? a(n) : n[c]);
      return this.isDefaultPrevented = (n.defaultPrevented != null ? n.defaultPrevented : n.returnValue === !1) ? Be : Li, this.isPropagationStopped = Li, this;
    }
    return B(t.prototype, {
      preventDefault: function() {
        this.defaultPrevented = !0;
        var a = this.nativeEvent;
        a && (a.preventDefault ? a.preventDefault() : typeof a.returnValue != "unknown" && (a.returnValue = !1), this.isDefaultPrevented = Be);
      },
      stopPropagation: function() {
        var a = this.nativeEvent;
        a && (a.stopPropagation ? a.stopPropagation() : typeof a.cancelBubble != "unknown" && (a.cancelBubble = !0), this.isPropagationStopped = Be);
      },
      persist: function() {
      },
      isPersistent: Be
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
  }, qe = Ll(Oa), qu = B({}, Oa, { view: 0, detail: 0 }), $y = Ll(qu), vf, yf, Yu, Ye = B({}, qu, {
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
    getModifierState: df,
    button: 0,
    buttons: 0,
    relatedTarget: function(l) {
      return l.relatedTarget === void 0 ? l.fromElement === l.srcElement ? l.toElement : l.fromElement : l.relatedTarget;
    },
    movementX: function(l) {
      return "movementX" in l ? l.movementX : (l !== Yu && (Yu && l.type === "mousemove" ? (vf = l.screenX - Yu.screenX, yf = l.screenY - Yu.screenY) : yf = vf = 0, Yu = l), vf);
    },
    movementY: function(l) {
      return "movementY" in l ? l.movementY : yf;
    }
  }), Vi = Ll(Ye), ky = B({}, Ye, { dataTransfer: 0 }), Fy = Ll(ky), Iy = B({}, qu, { relatedTarget: 0 }), mf = Ll(Iy), Py = B({}, Oa, {
    animationName: 0,
    elapsedTime: 0,
    pseudoElement: 0
  }), lm = Ll(Py), tm = B({}, Oa, {
    clipboardData: function(l) {
      return "clipboardData" in l ? l.clipboardData : window.clipboardData;
    }
  }), am = Ll(tm), um = B({}, Oa, { data: 0 }), xi = Ll(um), em = {
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
  }, nm = {
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
  }, fm = {
    Alt: "altKey",
    Control: "ctrlKey",
    Meta: "metaKey",
    Shift: "shiftKey"
  };
  function cm(l) {
    var t = this.nativeEvent;
    return t.getModifierState ? t.getModifierState(l) : (l = fm[l]) ? !!t[l] : !1;
  }
  function df() {
    return cm;
  }
  var im = B({}, qu, {
    key: function(l) {
      if (l.key) {
        var t = em[l.key] || l.key;
        if (t !== "Unidentified") return t;
      }
      return l.type === "keypress" ? (l = Re(l), l === 13 ? "Enter" : String.fromCharCode(l)) : l.type === "keydown" || l.type === "keyup" ? nm[l.keyCode] || "Unidentified" : "";
    },
    code: 0,
    location: 0,
    ctrlKey: 0,
    shiftKey: 0,
    altKey: 0,
    metaKey: 0,
    repeat: 0,
    locale: 0,
    getModifierState: df,
    charCode: function(l) {
      return l.type === "keypress" ? Re(l) : 0;
    },
    keyCode: function(l) {
      return l.type === "keydown" || l.type === "keyup" ? l.keyCode : 0;
    },
    which: function(l) {
      return l.type === "keypress" ? Re(l) : l.type === "keydown" || l.type === "keyup" ? l.keyCode : 0;
    }
  }), sm = Ll(im), vm = B({}, Ye, {
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
  }), Ki = Ll(vm), ym = B({}, qu, {
    touches: 0,
    targetTouches: 0,
    changedTouches: 0,
    altKey: 0,
    metaKey: 0,
    ctrlKey: 0,
    shiftKey: 0,
    getModifierState: df
  }), mm = Ll(ym), dm = B({}, Oa, {
    propertyName: 0,
    elapsedTime: 0,
    pseudoElement: 0
  }), om = Ll(dm), hm = B({}, Ye, {
    deltaX: function(l) {
      return "deltaX" in l ? l.deltaX : "wheelDeltaX" in l ? -l.wheelDeltaX : 0;
    },
    deltaY: function(l) {
      return "deltaY" in l ? l.deltaY : "wheelDeltaY" in l ? -l.wheelDeltaY : "wheelDelta" in l ? -l.wheelDelta : 0;
    },
    deltaZ: 0,
    deltaMode: 0
  }), Sm = Ll(hm), gm = B({}, Oa, {
    newState: 0,
    oldState: 0
  }), bm = Ll(gm), Tm = [9, 13, 27, 32], of = Rt && "CompositionEvent" in window, Gu = null;
  Rt && "documentMode" in document && (Gu = document.documentMode);
  var zm = Rt && "TextEvent" in window && !Gu, Ji = Rt && (!of || Gu && 8 < Gu && 11 >= Gu), wi = " ", Wi = !1;
  function $i(l, t) {
    switch (l) {
      case "keyup":
        return Tm.indexOf(t.keyCode) !== -1;
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
  function ki(l) {
    return l = l.detail, typeof l == "object" && "data" in l ? l.data : null;
  }
  var Ia = !1;
  function Em(l, t) {
    switch (l) {
      case "compositionend":
        return ki(t);
      case "keypress":
        return t.which !== 32 ? null : (Wi = !0, wi);
      case "textInput":
        return l = t.data, l === wi && Wi ? null : l;
      default:
        return null;
    }
  }
  function Am(l, t) {
    if (Ia)
      return l === "compositionend" || !of && $i(l, t) ? (l = Zi(), He = sf = It = null, Ia = !1, l) : null;
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
        return Ji && t.locale !== "ko" ? null : t.data;
      default:
        return null;
    }
  }
  var rm = {
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
  function Fi(l) {
    var t = l && l.nodeName && l.nodeName.toLowerCase();
    return t === "input" ? !!rm[l.type] : t === "textarea";
  }
  function Ii(l, t, a, u) {
    ka ? Fa ? Fa.push(u) : Fa = [u] : ka = u, t = Mn(t, "onChange"), 0 < t.length && (a = new qe(
      "onChange",
      "change",
      null,
      a,
      u
    ), l.push({ event: a, listeners: t }));
  }
  var Xu = null, Qu = null;
  function _m(l) {
    Bv(l, 0);
  }
  function Ge(l) {
    var t = Hu(l);
    if (Ri(t)) return l;
  }
  function Pi(l, t) {
    if (l === "change") return t;
  }
  var l0 = !1;
  if (Rt) {
    var hf;
    if (Rt) {
      var Sf = "oninput" in document;
      if (!Sf) {
        var t0 = document.createElement("div");
        t0.setAttribute("oninput", "return;"), Sf = typeof t0.oninput == "function";
      }
      hf = Sf;
    } else hf = !1;
    l0 = hf && (!document.documentMode || 9 < document.documentMode);
  }
  function a0() {
    Xu && (Xu.detachEvent("onpropertychange", u0), Qu = Xu = null);
  }
  function u0(l) {
    if (l.propertyName === "value" && Ge(Qu)) {
      var t = [];
      Ii(
        t,
        Qu,
        l,
        nf(l)
      ), ji(_m, t);
    }
  }
  function Om(l, t, a) {
    l === "focusin" ? (a0(), Xu = t, Qu = a, Xu.attachEvent("onpropertychange", u0)) : l === "focusout" && a0();
  }
  function Mm(l) {
    if (l === "selectionchange" || l === "keyup" || l === "keydown")
      return Ge(Qu);
  }
  function Dm(l, t) {
    if (l === "click") return Ge(t);
  }
  function Um(l, t) {
    if (l === "input" || l === "change")
      return Ge(t);
  }
  function pm(l, t) {
    return l === t && (l !== 0 || 1 / l === 1 / t) || l !== l && t !== t;
  }
  var Pl = typeof Object.is == "function" ? Object.is : pm;
  function ju(l, t) {
    if (Pl(l, t)) return !0;
    if (typeof l != "object" || l === null || typeof t != "object" || t === null)
      return !1;
    var a = Object.keys(l), u = Object.keys(t);
    if (a.length !== u.length) return !1;
    for (u = 0; u < a.length; u++) {
      var e = a[u];
      if (!Jn.call(t, e) || !Pl(l[e], t[e]))
        return !1;
    }
    return !0;
  }
  function e0(l) {
    for (; l && l.firstChild; ) l = l.firstChild;
    return l;
  }
  function n0(l, t) {
    var a = e0(l);
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
      a = e0(a);
    }
  }
  function f0(l, t) {
    return l && t ? l === t ? !0 : l && l.nodeType === 3 ? !1 : t && t.nodeType === 3 ? f0(l, t.parentNode) : "contains" in l ? l.contains(t) : l.compareDocumentPosition ? !!(l.compareDocumentPosition(t) & 16) : !1 : !1;
  }
  function c0(l) {
    l = l != null && l.ownerDocument != null && l.ownerDocument.defaultView != null ? l.ownerDocument.defaultView : window;
    for (var t = Ce(l.document); t instanceof l.HTMLIFrameElement; ) {
      try {
        var a = typeof t.contentWindow.location.href == "string";
      } catch {
        a = !1;
      }
      if (a) l = t.contentWindow;
      else break;
      t = Ce(l.document);
    }
    return t;
  }
  function gf(l) {
    var t = l && l.nodeName && l.nodeName.toLowerCase();
    return t && (t === "input" && (l.type === "text" || l.type === "search" || l.type === "tel" || l.type === "url" || l.type === "password") || t === "textarea" || l.contentEditable === "true");
  }
  var Cm = Rt && "documentMode" in document && 11 >= document.documentMode, Pa = null, bf = null, Zu = null, Tf = !1;
  function i0(l, t, a) {
    var u = a.window === a ? a.document : a.nodeType === 9 ? a : a.ownerDocument;
    Tf || Pa == null || Pa !== Ce(u) || (u = Pa, "selectionStart" in u && gf(u) ? u = { start: u.selectionStart, end: u.selectionEnd } : (u = (u.ownerDocument && u.ownerDocument.defaultView || window).getSelection(), u = {
      anchorNode: u.anchorNode,
      anchorOffset: u.anchorOffset,
      focusNode: u.focusNode,
      focusOffset: u.focusOffset
    }), Zu && ju(Zu, u) || (Zu = u, u = Mn(bf, "onSelect"), 0 < u.length && (t = new qe(
      "onSelect",
      "select",
      null,
      t,
      a
    ), l.push({ event: t, listeners: u }), t.target = Pa)));
  }
  function Ma(l, t) {
    var a = {};
    return a[l.toLowerCase()] = t.toLowerCase(), a["Webkit" + l] = "webkit" + t, a["Moz" + l] = "moz" + t, a;
  }
  var lu = {
    animationend: Ma("Animation", "AnimationEnd"),
    animationiteration: Ma("Animation", "AnimationIteration"),
    animationstart: Ma("Animation", "AnimationStart"),
    transitionrun: Ma("Transition", "TransitionRun"),
    transitionstart: Ma("Transition", "TransitionStart"),
    transitioncancel: Ma("Transition", "TransitionCancel"),
    transitionend: Ma("Transition", "TransitionEnd")
  }, zf = {}, s0 = {};
  Rt && (s0 = document.createElement("div").style, "AnimationEvent" in window || (delete lu.animationend.animation, delete lu.animationiteration.animation, delete lu.animationstart.animation), "TransitionEvent" in window || delete lu.transitionend.transition);
  function Da(l) {
    if (zf[l]) return zf[l];
    if (!lu[l]) return l;
    var t = lu[l], a;
    for (a in t)
      if (t.hasOwnProperty(a) && a in s0)
        return zf[l] = t[a];
    return l;
  }
  var v0 = Da("animationend"), y0 = Da("animationiteration"), m0 = Da("animationstart"), Nm = Da("transitionrun"), Hm = Da("transitionstart"), Rm = Da("transitioncancel"), d0 = Da("transitionend"), o0 = /* @__PURE__ */ new Map(), Ef = "abort auxClick beforeToggle cancel canPlay canPlayThrough click close contextMenu copy cut drag dragEnd dragEnter dragExit dragLeave dragOver dragStart drop durationChange emptied encrypted ended error gotPointerCapture input invalid keyDown keyPress keyUp load loadedData loadedMetadata loadStart lostPointerCapture mouseDown mouseMove mouseOut mouseOver mouseUp paste pause play playing pointerCancel pointerDown pointerMove pointerOut pointerOver pointerUp progress rateChange reset resize seeked seeking stalled submit suspend timeUpdate touchCancel touchEnd touchStart volumeChange scroll toggle touchMove waiting wheel".split(
    " "
  );
  Ef.push("scrollEnd");
  function bt(l, t) {
    o0.set(l, t), _a(t, [l]);
  }
  var Xe = typeof reportError == "function" ? reportError : function(l) {
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
  }, st = [], tu = 0, Af = 0;
  function Qe() {
    for (var l = tu, t = Af = tu = 0; t < l; ) {
      var a = st[t];
      st[t++] = null;
      var u = st[t];
      st[t++] = null;
      var e = st[t];
      st[t++] = null;
      var n = st[t];
      if (st[t++] = null, u !== null && e !== null) {
        var f = u.pending;
        f === null ? e.next = e : (e.next = f.next, f.next = e), u.pending = e;
      }
      n !== 0 && h0(a, e, n);
    }
  }
  function je(l, t, a, u) {
    st[tu++] = l, st[tu++] = t, st[tu++] = a, st[tu++] = u, Af |= u, l.lanes |= u, l = l.alternate, l !== null && (l.lanes |= u);
  }
  function rf(l, t, a, u) {
    return je(l, t, a, u), Ze(l);
  }
  function Ua(l, t) {
    return je(l, null, null, t), Ze(l);
  }
  function h0(l, t, a) {
    l.lanes |= a;
    var u = l.alternate;
    u !== null && (u.lanes |= a);
    for (var e = !1, n = l.return; n !== null; )
      n.childLanes |= a, u = n.alternate, u !== null && (u.childLanes |= a), n.tag === 22 && (l = n.stateNode, l === null || l._visibility & 1 || (e = !0)), l = n, n = n.return;
    return l.tag === 3 ? (n = l.stateNode, e && t !== null && (e = 31 - Il(a), l = n.hiddenUpdates, u = l[e], u === null ? l[e] = [t] : u.push(t), t.lane = a | 536870912), n) : null;
  }
  function Ze(l) {
    if (50 < se)
      throw se = 0, Hc = null, Error(m(185));
    for (var t = l.return; t !== null; )
      l = t, t = l.return;
    return l.tag === 3 ? l.stateNode : null;
  }
  var au = {};
  function Bm(l, t, a, u) {
    this.tag = l, this.key = a, this.sibling = this.child = this.return = this.stateNode = this.type = this.elementType = null, this.index = 0, this.refCleanup = this.ref = null, this.pendingProps = t, this.dependencies = this.memoizedState = this.updateQueue = this.memoizedProps = null, this.mode = u, this.subtreeFlags = this.flags = 0, this.deletions = null, this.childLanes = this.lanes = 0, this.alternate = null;
  }
  function lt(l, t, a, u) {
    return new Bm(l, t, a, u);
  }
  function _f(l) {
    return l = l.prototype, !(!l || !l.isReactComponent);
  }
  function Bt(l, t) {
    var a = l.alternate;
    return a === null ? (a = lt(
      l.tag,
      t,
      l.key,
      l.mode
    ), a.elementType = l.elementType, a.type = l.type, a.stateNode = l.stateNode, a.alternate = l, l.alternate = a) : (a.pendingProps = t, a.type = l.type, a.flags = 0, a.subtreeFlags = 0, a.deletions = null), a.flags = l.flags & 65011712, a.childLanes = l.childLanes, a.lanes = l.lanes, a.child = l.child, a.memoizedProps = l.memoizedProps, a.memoizedState = l.memoizedState, a.updateQueue = l.updateQueue, t = l.dependencies, a.dependencies = t === null ? null : { lanes: t.lanes, firstContext: t.firstContext }, a.sibling = l.sibling, a.index = l.index, a.ref = l.ref, a.refCleanup = l.refCleanup, a;
  }
  function S0(l, t) {
    l.flags &= 65011714;
    var a = l.alternate;
    return a === null ? (l.childLanes = 0, l.lanes = t, l.child = null, l.subtreeFlags = 0, l.memoizedProps = null, l.memoizedState = null, l.updateQueue = null, l.dependencies = null, l.stateNode = null) : (l.childLanes = a.childLanes, l.lanes = a.lanes, l.child = a.child, l.subtreeFlags = 0, l.deletions = null, l.memoizedProps = a.memoizedProps, l.memoizedState = a.memoizedState, l.updateQueue = a.updateQueue, l.type = a.type, t = a.dependencies, l.dependencies = t === null ? null : {
      lanes: t.lanes,
      firstContext: t.firstContext
    }), l;
  }
  function Le(l, t, a, u, e, n) {
    var f = 0;
    if (u = l, typeof l == "function") _f(l) && (f = 1);
    else if (typeof l == "string")
      f = Qd(
        l,
        a,
        C.current
      ) ? 26 : l === "html" || l === "head" || l === "body" ? 27 : 5;
    else
      l: switch (l) {
        case At:
          return l = lt(31, a, t, e), l.elementType = At, l.lanes = n, l;
        case Bl:
          return pa(a.children, e, n, t);
        case pt:
          f = 8, e |= 24;
          break;
        case $l:
          return l = lt(12, a, t, e | 2), l.elementType = $l, l.lanes = n, l;
        case Et:
          return l = lt(13, a, t, e), l.elementType = Et, l.lanes = n, l;
        case Xl:
          return l = lt(19, a, t, e), l.elementType = Xl, l.lanes = n, l;
        default:
          if (typeof l == "object" && l !== null)
            switch (l.$$typeof) {
              case Hl:
                f = 10;
                break l;
              case $t:
                f = 9;
                break l;
              case ft:
                f = 11;
                break l;
              case $:
                f = 14;
                break l;
              case Ql:
                f = 16, u = null;
                break l;
            }
          f = 29, a = Error(
            m(130, l === null ? "null" : typeof l, "")
          ), u = null;
      }
    return t = lt(f, a, t, e), t.elementType = l, t.type = u, t.lanes = n, t;
  }
  function pa(l, t, a, u) {
    return l = lt(7, l, u, t), l.lanes = a, l;
  }
  function Of(l, t, a) {
    return l = lt(6, l, null, t), l.lanes = a, l;
  }
  function g0(l) {
    var t = lt(18, null, null, 0);
    return t.stateNode = l, t;
  }
  function Mf(l, t, a) {
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
  var b0 = /* @__PURE__ */ new WeakMap();
  function vt(l, t) {
    if (typeof l == "object" && l !== null) {
      var a = b0.get(l);
      return a !== void 0 ? a : (t = {
        value: l,
        source: t,
        stack: gi(t)
      }, b0.set(l, t), t);
    }
    return {
      value: l,
      source: t,
      stack: gi(t)
    };
  }
  var uu = [], eu = 0, Ve = null, Lu = 0, yt = [], mt = 0, Pt = null, _t = 1, Ot = "";
  function qt(l, t) {
    uu[eu++] = Lu, uu[eu++] = Ve, Ve = l, Lu = t;
  }
  function T0(l, t, a) {
    yt[mt++] = _t, yt[mt++] = Ot, yt[mt++] = Pt, Pt = l;
    var u = _t;
    l = Ot;
    var e = 32 - Il(u) - 1;
    u &= ~(1 << e), a += 1;
    var n = 32 - Il(t) + e;
    if (30 < n) {
      var f = e - e % 5;
      n = (u & (1 << f) - 1).toString(32), u >>= f, e -= f, _t = 1 << 32 - Il(t) + e | a << e | u, Ot = n + l;
    } else
      _t = 1 << n | a << e | u, Ot = l;
  }
  function Df(l) {
    l.return !== null && (qt(l, 1), T0(l, 1, 0));
  }
  function Uf(l) {
    for (; l === Ve; )
      Ve = uu[--eu], uu[eu] = null, Lu = uu[--eu], uu[eu] = null;
    for (; l === Pt; )
      Pt = yt[--mt], yt[mt] = null, Ot = yt[--mt], yt[mt] = null, _t = yt[--mt], yt[mt] = null;
  }
  function z0(l, t) {
    yt[mt++] = _t, yt[mt++] = Ot, yt[mt++] = Pt, _t = t.id, Ot = t.overflow, Pt = l;
  }
  var Ul = null, vl = null, k = !1, la = null, dt = !1, pf = Error(m(519));
  function ta(l) {
    var t = Error(
      m(
        418,
        1 < arguments.length && arguments[1] !== void 0 && arguments[1] ? "text" : "HTML",
        ""
      )
    );
    throw Vu(vt(t, l)), pf;
  }
  function E0(l) {
    var t = l.stateNode, a = l.type, u = l.memoizedProps;
    switch (t[Dl] = l, t[Zl] = u, a) {
      case "dialog":
        K("cancel", t), K("close", t);
        break;
      case "iframe":
      case "object":
      case "embed":
        K("load", t);
        break;
      case "video":
      case "audio":
        for (a = 0; a < ye.length; a++)
          K(ye[a], t);
        break;
      case "source":
        K("error", t);
        break;
      case "img":
      case "image":
      case "link":
        K("error", t), K("load", t);
        break;
      case "details":
        K("toggle", t);
        break;
      case "input":
        K("invalid", t), Bi(
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
        K("invalid", t);
        break;
      case "textarea":
        K("invalid", t), Yi(t, u.value, u.defaultValue, u.children);
    }
    a = u.children, typeof a != "string" && typeof a != "number" && typeof a != "bigint" || t.textContent === "" + a || u.suppressHydrationWarning === !0 || Xv(t.textContent, a) ? (u.popover != null && (K("beforetoggle", t), K("toggle", t)), u.onScroll != null && K("scroll", t), u.onScrollEnd != null && K("scrollend", t), u.onClick != null && (t.onclick = Ht), t = !0) : t = !1, t || ta(l, !0);
  }
  function A0(l) {
    for (Ul = l.return; Ul; )
      switch (Ul.tag) {
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
          Ul = Ul.return;
      }
  }
  function nu(l) {
    if (l !== Ul) return !1;
    if (!k) return A0(l), k = !0, !1;
    var t = l.tag, a;
    if ((a = t !== 3 && t !== 27) && ((a = t === 5) && (a = l.type, a = !(a !== "form" && a !== "button") || wc(l.type, l.memoizedProps)), a = !a), a && vl && ta(l), A0(l), t === 13) {
      if (l = l.memoizedState, l = l !== null ? l.dehydrated : null, !l) throw Error(m(317));
      vl = wv(l);
    } else if (t === 31) {
      if (l = l.memoizedState, l = l !== null ? l.dehydrated : null, !l) throw Error(m(317));
      vl = wv(l);
    } else
      t === 27 ? (t = vl, ha(l.type) ? (l = Ic, Ic = null, vl = l) : vl = t) : vl = Ul ? ht(l.stateNode.nextSibling) : null;
    return !0;
  }
  function Ca() {
    vl = Ul = null, k = !1;
  }
  function Cf() {
    var l = la;
    return l !== null && (Jl === null ? Jl = l : Jl.push.apply(
      Jl,
      l
    ), la = null), l;
  }
  function Vu(l) {
    la === null ? la = [l] : la.push(l);
  }
  var Nf = v(null), Na = null, Yt = null;
  function aa(l, t, a) {
    O(Nf, t._currentValue), t._currentValue = a;
  }
  function Gt(l) {
    l._currentValue = Nf.current, E(Nf);
  }
  function Hf(l, t, a) {
    for (; l !== null; ) {
      var u = l.alternate;
      if ((l.childLanes & t) !== t ? (l.childLanes |= t, u !== null && (u.childLanes |= t)) : u !== null && (u.childLanes & t) !== t && (u.childLanes |= t), l === a) break;
      l = l.return;
    }
  }
  function Rf(l, t, a, u) {
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
              n.lanes |= a, c = n.alternate, c !== null && (c.lanes |= a), Hf(
                n.return,
                a,
                l
              ), u || (f = null);
              break l;
            }
          n = c.next;
        }
      } else if (e.tag === 18) {
        if (f = e.return, f === null) throw Error(m(341));
        f.lanes |= a, n = f.alternate, n !== null && (n.lanes |= a), Hf(f, a, l), f = null;
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
  function fu(l, t, a, u) {
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
          Pl(e.pendingProps.value, f.value) || (l !== null ? l.push(c) : l = [c]);
        }
      } else if (e === tl.current) {
        if (f = e.alternate, f === null) throw Error(m(387));
        f.memoizedState.memoizedState !== e.memoizedState.memoizedState && (l !== null ? l.push(Se) : l = [Se]);
      }
      e = e.return;
    }
    l !== null && Rf(
      t,
      l,
      a,
      u
    ), t.flags |= 262144;
  }
  function xe(l) {
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
    Na = l, Yt = null, l = l.dependencies, l !== null && (l.firstContext = null);
  }
  function pl(l) {
    return r0(Na, l);
  }
  function Ke(l, t) {
    return Na === null && Ha(l), r0(l, t);
  }
  function r0(l, t) {
    var a = t._currentValue;
    if (t = { context: t, memoizedValue: a, next: null }, Yt === null) {
      if (l === null) throw Error(m(308));
      Yt = t, l.dependencies = { lanes: 0, firstContext: t }, l.flags |= 524288;
    } else Yt = Yt.next = t;
    return a;
  }
  var qm = typeof AbortController < "u" ? AbortController : function() {
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
  }, Ym = A.unstable_scheduleCallback, Gm = A.unstable_NormalPriority, zl = {
    $$typeof: Hl,
    Consumer: null,
    Provider: null,
    _currentValue: null,
    _currentValue2: null,
    _threadCount: 0
  };
  function Bf() {
    return {
      controller: new qm(),
      data: /* @__PURE__ */ new Map(),
      refCount: 0
    };
  }
  function xu(l) {
    l.refCount--, l.refCount === 0 && Ym(Gm, function() {
      l.controller.abort();
    });
  }
  var Ku = null, qf = 0, cu = 0, iu = null;
  function Xm(l, t) {
    if (Ku === null) {
      var a = Ku = [];
      qf = 0, cu = Xc(), iu = {
        status: "pending",
        value: void 0,
        then: function(u) {
          a.push(u);
        }
      };
    }
    return qf++, t.then(_0, _0), t;
  }
  function _0() {
    if (--qf === 0 && Ku !== null) {
      iu !== null && (iu.status = "fulfilled");
      var l = Ku;
      Ku = null, cu = 0, iu = null;
      for (var t = 0; t < l.length; t++) (0, l[t])();
    }
  }
  function Qm(l, t) {
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
  var O0 = b.S;
  b.S = function(l, t) {
    iv = kl(), typeof t == "object" && t !== null && typeof t.then == "function" && Xm(l, t), O0 !== null && O0(l, t);
  };
  var Ra = v(null);
  function Yf() {
    var l = Ra.current;
    return l !== null ? l : sl.pooledCache;
  }
  function Je(l, t) {
    t === null ? O(Ra, Ra.current) : O(Ra, t.pool);
  }
  function M0() {
    var l = Yf();
    return l === null ? null : { parent: zl._currentValue, pool: l };
  }
  var su = Error(m(460)), Gf = Error(m(474)), we = Error(m(542)), We = { then: function() {
  } };
  function D0(l) {
    return l = l.status, l === "fulfilled" || l === "rejected";
  }
  function U0(l, t, a) {
    switch (a = l[a], a === void 0 ? l.push(t) : a !== t && (t.then(Ht, Ht), t = a), t.status) {
      case "fulfilled":
        return t.value;
      case "rejected":
        throw l = t.reason, C0(l), l;
      default:
        if (typeof t.status == "string") t.then(Ht, Ht);
        else {
          if (l = sl, l !== null && 100 < l.shellSuspendCounter)
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
            throw l = t.reason, C0(l), l;
        }
        throw qa = t, su;
    }
  }
  function Ba(l) {
    try {
      var t = l._init;
      return t(l._payload);
    } catch (a) {
      throw a !== null && typeof a == "object" && typeof a.then == "function" ? (qa = a, su) : a;
    }
  }
  var qa = null;
  function p0() {
    if (qa === null) throw Error(m(459));
    var l = qa;
    return qa = null, l;
  }
  function C0(l) {
    if (l === su || l === we)
      throw Error(m(483));
  }
  var vu = null, Ju = 0;
  function $e(l) {
    var t = Ju;
    return Ju += 1, vu === null && (vu = []), U0(vu, l, t);
  }
  function wu(l, t) {
    t = t.props.ref, l.ref = t !== void 0 ? t : null;
  }
  function ke(l, t) {
    throw t.$$typeof === ml ? Error(m(525)) : (l = Object.prototype.toString.call(t), Error(
      m(
        31,
        l === "[object Object]" ? "object with keys {" + Object.keys(t).join(", ") + "}" : l
      )
    ));
  }
  function N0(l) {
    function t(y, s) {
      if (l) {
        var d = y.deletions;
        d === null ? (y.deletions = [s], y.flags |= 16) : d.push(s);
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
      return y = Bt(y, s), y.index = 0, y.sibling = null, y;
    }
    function n(y, s, d) {
      return y.index = d, l ? (d = y.alternate, d !== null ? (d = d.index, d < s ? (y.flags |= 67108866, s) : d) : (y.flags |= 67108866, s)) : (y.flags |= 1048576, s);
    }
    function f(y) {
      return l && y.alternate === null && (y.flags |= 67108866), y;
    }
    function c(y, s, d, T) {
      return s === null || s.tag !== 6 ? (s = Of(d, y.mode, T), s.return = y, s) : (s = e(s, d), s.return = y, s);
    }
    function i(y, s, d, T) {
      var H = d.type;
      return H === Bl ? g(
        y,
        s,
        d.props.children,
        T,
        d.key
      ) : s !== null && (s.elementType === H || typeof H == "object" && H !== null && H.$$typeof === Ql && Ba(H) === s.type) ? (s = e(s, d.props), wu(s, d), s.return = y, s) : (s = Le(
        d.type,
        d.key,
        d.props,
        null,
        y.mode,
        T
      ), wu(s, d), s.return = y, s);
    }
    function o(y, s, d, T) {
      return s === null || s.tag !== 4 || s.stateNode.containerInfo !== d.containerInfo || s.stateNode.implementation !== d.implementation ? (s = Mf(d, y.mode, T), s.return = y, s) : (s = e(s, d.children || []), s.return = y, s);
    }
    function g(y, s, d, T, H) {
      return s === null || s.tag !== 7 ? (s = pa(
        d,
        y.mode,
        T,
        H
      ), s.return = y, s) : (s = e(s, d), s.return = y, s);
    }
    function z(y, s, d) {
      if (typeof s == "string" && s !== "" || typeof s == "number" || typeof s == "bigint")
        return s = Of(
          "" + s,
          y.mode,
          d
        ), s.return = y, s;
      if (typeof s == "object" && s !== null) {
        switch (s.$$typeof) {
          case Wl:
            return d = Le(
              s.type,
              s.key,
              s.props,
              null,
              y.mode,
              d
            ), wu(d, s), d.return = y, d;
          case Gl:
            return s = Mf(
              s,
              y.mode,
              d
            ), s.return = y, s;
          case Ql:
            return s = Ba(s), z(y, s, d);
        }
        if (gt(s) || jl(s))
          return s = pa(
            s,
            y.mode,
            d,
            null
          ), s.return = y, s;
        if (typeof s.then == "function")
          return z(y, $e(s), d);
        if (s.$$typeof === Hl)
          return z(
            y,
            Ke(y, s),
            d
          );
        ke(y, s);
      }
      return null;
    }
    function h(y, s, d, T) {
      var H = s !== null ? s.key : null;
      if (typeof d == "string" && d !== "" || typeof d == "number" || typeof d == "bigint")
        return H !== null ? null : c(y, s, "" + d, T);
      if (typeof d == "object" && d !== null) {
        switch (d.$$typeof) {
          case Wl:
            return d.key === H ? i(y, s, d, T) : null;
          case Gl:
            return d.key === H ? o(y, s, d, T) : null;
          case Ql:
            return d = Ba(d), h(y, s, d, T);
        }
        if (gt(d) || jl(d))
          return H !== null ? null : g(y, s, d, T, null);
        if (typeof d.then == "function")
          return h(
            y,
            s,
            $e(d),
            T
          );
        if (d.$$typeof === Hl)
          return h(
            y,
            s,
            Ke(y, d),
            T
          );
        ke(y, d);
      }
      return null;
    }
    function S(y, s, d, T, H) {
      if (typeof T == "string" && T !== "" || typeof T == "number" || typeof T == "bigint")
        return y = y.get(d) || null, c(s, y, "" + T, H);
      if (typeof T == "object" && T !== null) {
        switch (T.$$typeof) {
          case Wl:
            return y = y.get(
              T.key === null ? d : T.key
            ) || null, i(s, y, T, H);
          case Gl:
            return y = y.get(
              T.key === null ? d : T.key
            ) || null, o(s, y, T, H);
          case Ql:
            return T = Ba(T), S(
              y,
              s,
              d,
              T,
              H
            );
        }
        if (gt(T) || jl(T))
          return y = y.get(d) || null, g(s, y, T, H, null);
        if (typeof T.then == "function")
          return S(
            y,
            s,
            d,
            $e(T),
            H
          );
        if (T.$$typeof === Hl)
          return S(
            y,
            s,
            d,
            Ke(s, T),
            H
          );
        ke(s, T);
      }
      return null;
    }
    function U(y, s, d, T) {
      for (var H = null, I = null, N = s, j = s = 0, w = null; N !== null && j < d.length; j++) {
        N.index > j ? (w = N, N = null) : w = N.sibling;
        var P = h(
          y,
          N,
          d[j],
          T
        );
        if (P === null) {
          N === null && (N = w);
          break;
        }
        l && N && P.alternate === null && t(y, N), s = n(P, s, j), I === null ? H = P : I.sibling = P, I = P, N = w;
      }
      if (j === d.length)
        return a(y, N), k && qt(y, j), H;
      if (N === null) {
        for (; j < d.length; j++)
          N = z(y, d[j], T), N !== null && (s = n(
            N,
            s,
            j
          ), I === null ? H = N : I.sibling = N, I = N);
        return k && qt(y, j), H;
      }
      for (N = u(N); j < d.length; j++)
        w = S(
          N,
          y,
          j,
          d[j],
          T
        ), w !== null && (l && w.alternate !== null && N.delete(
          w.key === null ? j : w.key
        ), s = n(
          w,
          s,
          j
        ), I === null ? H = w : I.sibling = w, I = w);
      return l && N.forEach(function(za) {
        return t(y, za);
      }), k && qt(y, j), H;
    }
    function R(y, s, d, T) {
      if (d == null) throw Error(m(151));
      for (var H = null, I = null, N = s, j = s = 0, w = null, P = d.next(); N !== null && !P.done; j++, P = d.next()) {
        N.index > j ? (w = N, N = null) : w = N.sibling;
        var za = h(y, N, P.value, T);
        if (za === null) {
          N === null && (N = w);
          break;
        }
        l && N && za.alternate === null && t(y, N), s = n(za, s, j), I === null ? H = za : I.sibling = za, I = za, N = w;
      }
      if (P.done)
        return a(y, N), k && qt(y, j), H;
      if (N === null) {
        for (; !P.done; j++, P = d.next())
          P = z(y, P.value, T), P !== null && (s = n(P, s, j), I === null ? H = P : I.sibling = P, I = P);
        return k && qt(y, j), H;
      }
      for (N = u(N); !P.done; j++, P = d.next())
        P = S(N, y, j, P.value, T), P !== null && (l && P.alternate !== null && N.delete(P.key === null ? j : P.key), s = n(P, s, j), I === null ? H = P : I.sibling = P, I = P);
      return l && N.forEach(function(kd) {
        return t(y, kd);
      }), k && qt(y, j), H;
    }
    function cl(y, s, d, T) {
      if (typeof d == "object" && d !== null && d.type === Bl && d.key === null && (d = d.props.children), typeof d == "object" && d !== null) {
        switch (d.$$typeof) {
          case Wl:
            l: {
              for (var H = d.key; s !== null; ) {
                if (s.key === H) {
                  if (H = d.type, H === Bl) {
                    if (s.tag === 7) {
                      a(
                        y,
                        s.sibling
                      ), T = e(
                        s,
                        d.props.children
                      ), T.return = y, y = T;
                      break l;
                    }
                  } else if (s.elementType === H || typeof H == "object" && H !== null && H.$$typeof === Ql && Ba(H) === s.type) {
                    a(
                      y,
                      s.sibling
                    ), T = e(s, d.props), wu(T, d), T.return = y, y = T;
                    break l;
                  }
                  a(y, s);
                  break;
                } else t(y, s);
                s = s.sibling;
              }
              d.type === Bl ? (T = pa(
                d.props.children,
                y.mode,
                T,
                d.key
              ), T.return = y, y = T) : (T = Le(
                d.type,
                d.key,
                d.props,
                null,
                y.mode,
                T
              ), wu(T, d), T.return = y, y = T);
            }
            return f(y);
          case Gl:
            l: {
              for (H = d.key; s !== null; ) {
                if (s.key === H)
                  if (s.tag === 4 && s.stateNode.containerInfo === d.containerInfo && s.stateNode.implementation === d.implementation) {
                    a(
                      y,
                      s.sibling
                    ), T = e(s, d.children || []), T.return = y, y = T;
                    break l;
                  } else {
                    a(y, s);
                    break;
                  }
                else t(y, s);
                s = s.sibling;
              }
              T = Mf(d, y.mode, T), T.return = y, y = T;
            }
            return f(y);
          case Ql:
            return d = Ba(d), cl(
              y,
              s,
              d,
              T
            );
        }
        if (gt(d))
          return U(
            y,
            s,
            d,
            T
          );
        if (jl(d)) {
          if (H = jl(d), typeof H != "function") throw Error(m(150));
          return d = H.call(d), R(
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
            $e(d),
            T
          );
        if (d.$$typeof === Hl)
          return cl(
            y,
            s,
            Ke(y, d),
            T
          );
        ke(y, d);
      }
      return typeof d == "string" && d !== "" || typeof d == "number" || typeof d == "bigint" ? (d = "" + d, s !== null && s.tag === 6 ? (a(y, s.sibling), T = e(s, d), T.return = y, y = T) : (a(y, s), T = Of(d, y.mode, T), T.return = y, y = T), f(y)) : a(y, s);
    }
    return function(y, s, d, T) {
      try {
        Ju = 0;
        var H = cl(
          y,
          s,
          d,
          T
        );
        return vu = null, H;
      } catch (N) {
        if (N === su || N === we) throw N;
        var I = lt(29, N, null, y.mode);
        return I.lanes = T, I.return = y, I;
      } finally {
      }
    };
  }
  var Ya = N0(!0), H0 = N0(!1), ua = !1;
  function Xf(l) {
    l.updateQueue = {
      baseState: l.memoizedState,
      firstBaseUpdate: null,
      lastBaseUpdate: null,
      shared: { pending: null, lanes: 0, hiddenCallbacks: null },
      callbacks: null
    };
  }
  function Qf(l, t) {
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
    if (u = u.shared, (ll & 2) !== 0) {
      var e = u.pending;
      return e === null ? t.next = t : (t.next = e.next, e.next = t), u.pending = t, t = Ze(l), h0(l, null, a), t;
    }
    return je(l, u, t, a), Ze(l);
  }
  function Wu(l, t, a) {
    if (t = t.updateQueue, t !== null && (t = t.shared, (a & 4194048) !== 0)) {
      var u = t.lanes;
      u &= l.pendingLanes, a |= u, t.lanes = a, ri(l, a);
    }
  }
  function jf(l, t) {
    var a = l.updateQueue, u = l.alternate;
    if (u !== null && (u = u.updateQueue, a === u)) {
      var e = null, n = null;
      if (a = a.firstBaseUpdate, a !== null) {
        do {
          var f = {
            lane: a.lane,
            tag: a.tag,
            payload: a.payload,
            callback: null,
            next: null
          };
          n === null ? e = n = f : n = n.next = f, a = a.next;
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
  var Zf = !1;
  function $u() {
    if (Zf) {
      var l = iu;
      if (l !== null) throw l;
    }
  }
  function ku(l, t, a, u) {
    Zf = !1;
    var e = l.updateQueue;
    ua = !1;
    var n = e.firstBaseUpdate, f = e.lastBaseUpdate, c = e.shared.pending;
    if (c !== null) {
      e.shared.pending = null;
      var i = c, o = i.next;
      i.next = null, f === null ? n = o : f.next = o, f = i;
      var g = l.alternate;
      g !== null && (g = g.updateQueue, c = g.lastBaseUpdate, c !== f && (c === null ? g.firstBaseUpdate = o : c.next = o, g.lastBaseUpdate = i));
    }
    if (n !== null) {
      var z = e.baseState;
      f = 0, g = o = i = null, c = n;
      do {
        var h = c.lane & -536870913, S = h !== c.lane;
        if (S ? (J & h) === h : (u & h) === h) {
          h !== 0 && h === cu && (Zf = !0), g !== null && (g = g.next = {
            lane: 0,
            tag: c.tag,
            payload: c.payload,
            callback: null,
            next: null
          });
          l: {
            var U = l, R = c;
            h = t;
            var cl = a;
            switch (R.tag) {
              case 1:
                if (U = R.payload, typeof U == "function") {
                  z = U.call(cl, z, h);
                  break l;
                }
                z = U;
                break l;
              case 3:
                U.flags = U.flags & -65537 | 128;
              case 0:
                if (U = R.payload, h = typeof U == "function" ? U.call(cl, z, h) : U, h == null) break l;
                z = B({}, z, h);
                break l;
              case 2:
                ua = !0;
            }
          }
          h = c.callback, h !== null && (l.flags |= 64, S && (l.flags |= 8192), S = e.callbacks, S === null ? e.callbacks = [h] : S.push(h));
        } else
          S = {
            lane: h,
            tag: c.tag,
            payload: c.payload,
            callback: c.callback,
            next: null
          }, g === null ? (o = g = S, i = z) : g = g.next = S, f |= h;
        if (c = c.next, c === null) {
          if (c = e.shared.pending, c === null)
            break;
          S = c, c = S.next, S.next = null, e.lastBaseUpdate = S, e.shared.pending = null;
        }
      } while (!0);
      g === null && (i = z), e.baseState = i, e.firstBaseUpdate = o, e.lastBaseUpdate = g, n === null && (e.shared.lanes = 0), va |= f, l.lanes = f, l.memoizedState = z;
    }
  }
  function R0(l, t) {
    if (typeof l != "function")
      throw Error(m(191, l));
    l.call(t);
  }
  function B0(l, t) {
    var a = l.callbacks;
    if (a !== null)
      for (l.callbacks = null, l = 0; l < a.length; l++)
        R0(a[l], t);
  }
  var yu = v(null), Fe = v(0);
  function q0(l, t) {
    l = Jt, O(Fe, l), O(yu, t), Jt = l | t.baseLanes;
  }
  function Lf() {
    O(Fe, Jt), O(yu, yu.current);
  }
  function Vf() {
    Jt = Fe.current, E(yu), E(Fe);
  }
  var tt = v(null), ot = null;
  function fa(l) {
    var t = l.alternate;
    O(bl, bl.current & 1), O(tt, l), ot === null && (t === null || yu.current !== null || t.memoizedState !== null) && (ot = l);
  }
  function xf(l) {
    O(bl, bl.current), O(tt, l), ot === null && (ot = l);
  }
  function Y0(l) {
    l.tag === 22 ? (O(bl, bl.current), O(tt, l), ot === null && (ot = l)) : ca();
  }
  function ca() {
    O(bl, bl.current), O(tt, tt.current);
  }
  function at(l) {
    E(tt), ot === l && (ot = null), E(bl);
  }
  var bl = v(0);
  function Ie(l) {
    for (var t = l; t !== null; ) {
      if (t.tag === 13) {
        var a = t.memoizedState;
        if (a !== null && (a = a.dehydrated, a === null || kc(a) || Fc(a)))
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
  var Xt = 0, Q = null, nl = null, El = null, Pe = !1, mu = !1, Ga = !1, ln = 0, Fu = 0, du = null, jm = 0;
  function ol() {
    throw Error(m(321));
  }
  function Kf(l, t) {
    if (t === null) return !1;
    for (var a = 0; a < t.length && a < l.length; a++)
      if (!Pl(l[a], t[a])) return !1;
    return !0;
  }
  function Jf(l, t, a, u, e, n) {
    return Xt = n, Q = t, t.memoizedState = null, t.updateQueue = null, t.lanes = 0, b.H = l === null || l.memoizedState === null ? Ts : cc, Ga = !1, n = a(u, e), Ga = !1, mu && (n = X0(
      t,
      a,
      u,
      e
    )), G0(l), n;
  }
  function G0(l) {
    b.H = le;
    var t = nl !== null && nl.next !== null;
    if (Xt = 0, El = nl = Q = null, Pe = !1, Fu = 0, du = null, t) throw Error(m(300));
    l === null || Al || (l = l.dependencies, l !== null && xe(l) && (Al = !0));
  }
  function X0(l, t, a, u) {
    Q = l;
    var e = 0;
    do {
      if (mu && (du = null), Fu = 0, mu = !1, 25 <= e) throw Error(m(301));
      if (e += 1, El = nl = null, l.updateQueue != null) {
        var n = l.updateQueue;
        n.lastEffect = null, n.events = null, n.stores = null, n.memoCache != null && (n.memoCache.index = 0);
      }
      b.H = zs, n = t(a, u);
    } while (mu);
    return n;
  }
  function Zm() {
    var l = b.H, t = l.useState()[0];
    return t = typeof t.then == "function" ? Iu(t) : t, l = l.useState()[0], (nl !== null ? nl.memoizedState : null) !== l && (Q.flags |= 1024), t;
  }
  function wf() {
    var l = ln !== 0;
    return ln = 0, l;
  }
  function Wf(l, t, a) {
    t.updateQueue = l.updateQueue, t.flags &= -2053, l.lanes &= ~a;
  }
  function $f(l) {
    if (Pe) {
      for (l = l.memoizedState; l !== null; ) {
        var t = l.queue;
        t !== null && (t.pending = null), l = l.next;
      }
      Pe = !1;
    }
    Xt = 0, El = nl = Q = null, mu = !1, Fu = ln = 0, du = null;
  }
  function Yl() {
    var l = {
      memoizedState: null,
      baseState: null,
      baseQueue: null,
      queue: null,
      next: null
    };
    return El === null ? Q.memoizedState = El = l : El = El.next = l, El;
  }
  function Tl() {
    if (nl === null) {
      var l = Q.alternate;
      l = l !== null ? l.memoizedState : null;
    } else l = nl.next;
    var t = El === null ? Q.memoizedState : El.next;
    if (t !== null)
      El = t, nl = l;
    else {
      if (l === null)
        throw Q.alternate === null ? Error(m(467)) : Error(m(310));
      nl = l, l = {
        memoizedState: nl.memoizedState,
        baseState: nl.baseState,
        baseQueue: nl.baseQueue,
        queue: nl.queue,
        next: null
      }, El === null ? Q.memoizedState = El = l : El = El.next = l;
    }
    return El;
  }
  function tn() {
    return { lastEffect: null, events: null, stores: null, memoCache: null };
  }
  function Iu(l) {
    var t = Fu;
    return Fu += 1, du === null && (du = []), l = U0(du, l, t), t = Q, (El === null ? t.memoizedState : El.next) === null && (t = t.alternate, b.H = t === null || t.memoizedState === null ? Ts : cc), l;
  }
  function an(l) {
    if (l !== null && typeof l == "object") {
      if (typeof l.then == "function") return Iu(l);
      if (l.$$typeof === Hl) return pl(l);
    }
    throw Error(m(438, String(l)));
  }
  function kf(l) {
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
    if (t == null && (t = { data: [], index: 0 }), a === null && (a = tn(), Q.updateQueue = a), a.memoCache = t, a = t.data[t.index], a === void 0)
      for (a = t.data[t.index] = Array(l), u = 0; u < l; u++)
        a[u] = La;
    return t.index++, a;
  }
  function Qt(l, t) {
    return typeof t == "function" ? t(l) : t;
  }
  function un(l) {
    var t = Tl();
    return Ff(t, nl, l);
  }
  function Ff(l, t, a) {
    var u = l.queue;
    if (u === null) throw Error(m(311));
    u.lastRenderedReducer = a;
    var e = l.baseQueue, n = u.pending;
    if (n !== null) {
      if (e !== null) {
        var f = e.next;
        e.next = n.next, n.next = f;
      }
      t.baseQueue = e = n, u.pending = null;
    }
    if (n = l.baseState, e === null) l.memoizedState = n;
    else {
      t = e.next;
      var c = f = null, i = null, o = t, g = !1;
      do {
        var z = o.lane & -536870913;
        if (z !== o.lane ? (J & z) === z : (Xt & z) === z) {
          var h = o.revertLane;
          if (h === 0)
            i !== null && (i = i.next = {
              lane: 0,
              revertLane: 0,
              gesture: null,
              action: o.action,
              hasEagerState: o.hasEagerState,
              eagerState: o.eagerState,
              next: null
            }), z === cu && (g = !0);
          else if ((Xt & h) === h) {
            o = o.next, h === cu && (g = !0);
            continue;
          } else
            z = {
              lane: 0,
              revertLane: o.revertLane,
              gesture: null,
              action: o.action,
              hasEagerState: o.hasEagerState,
              eagerState: o.eagerState,
              next: null
            }, i === null ? (c = i = z, f = n) : i = i.next = z, Q.lanes |= h, va |= h;
          z = o.action, Ga && a(n, z), n = o.hasEagerState ? o.eagerState : a(n, z);
        } else
          h = {
            lane: z,
            revertLane: o.revertLane,
            gesture: o.gesture,
            action: o.action,
            hasEagerState: o.hasEagerState,
            eagerState: o.eagerState,
            next: null
          }, i === null ? (c = i = h, f = n) : i = i.next = h, Q.lanes |= z, va |= z;
        o = o.next;
      } while (o !== null && o !== t);
      if (i === null ? f = n : i.next = c, !Pl(n, l.memoizedState) && (Al = !0, g && (a = iu, a !== null)))
        throw a;
      l.memoizedState = n, l.baseState = f, l.baseQueue = i, u.lastRenderedState = n;
    }
    return e === null && (u.lanes = 0), [l.memoizedState, u.dispatch];
  }
  function If(l) {
    var t = Tl(), a = t.queue;
    if (a === null) throw Error(m(311));
    a.lastRenderedReducer = l;
    var u = a.dispatch, e = a.pending, n = t.memoizedState;
    if (e !== null) {
      a.pending = null;
      var f = e = e.next;
      do
        n = l(n, f.action), f = f.next;
      while (f !== e);
      Pl(n, t.memoizedState) || (Al = !0), t.memoizedState = n, t.baseQueue === null && (t.baseState = n), a.lastRenderedState = n;
    }
    return [n, u];
  }
  function Q0(l, t, a) {
    var u = Q, e = Tl(), n = k;
    if (n) {
      if (a === void 0) throw Error(m(407));
      a = a();
    } else a = t();
    var f = !Pl(
      (nl || e).memoizedState,
      a
    );
    if (f && (e.memoizedState = a, Al = !0), e = e.queue, tc(L0.bind(null, u, e, l), [
      l
    ]), e.getSnapshot !== t || f || El !== null && El.memoizedState.tag & 1) {
      if (u.flags |= 2048, ou(
        9,
        { destroy: void 0 },
        Z0.bind(
          null,
          u,
          e,
          a,
          t
        ),
        null
      ), sl === null) throw Error(m(349));
      n || (Xt & 127) !== 0 || j0(u, t, a);
    }
    return a;
  }
  function j0(l, t, a) {
    l.flags |= 16384, l = { getSnapshot: t, value: a }, t = Q.updateQueue, t === null ? (t = tn(), Q.updateQueue = t, t.stores = [l]) : (a = t.stores, a === null ? t.stores = [l] : a.push(l));
  }
  function Z0(l, t, a, u) {
    t.value = a, t.getSnapshot = u, V0(t) && x0(l);
  }
  function L0(l, t, a) {
    return a(function() {
      V0(t) && x0(l);
    });
  }
  function V0(l) {
    var t = l.getSnapshot;
    l = l.value;
    try {
      var a = t();
      return !Pl(l, a);
    } catch {
      return !0;
    }
  }
  function x0(l) {
    var t = Ua(l, 2);
    t !== null && wl(t, l, 2);
  }
  function Pf(l) {
    var t = Yl();
    if (typeof l == "function") {
      var a = l;
      if (l = a(), Ga) {
        kt(!0);
        try {
          a();
        } finally {
          kt(!1);
        }
      }
    }
    return t.memoizedState = t.baseState = l, t.queue = {
      pending: null,
      lanes: 0,
      dispatch: null,
      lastRenderedReducer: Qt,
      lastRenderedState: l
    }, t;
  }
  function K0(l, t, a, u) {
    return l.baseState = a, Ff(
      l,
      nl,
      typeof u == "function" ? u : Qt
    );
  }
  function Lm(l, t, a, u, e) {
    if (fn(l)) throw Error(m(485));
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
      b.T !== null ? a(!0) : n.isTransition = !1, u(n), a = t.pending, a === null ? (n.next = t.pending = n, J0(t, n)) : (n.next = a.next, t.pending = a.next = n);
    }
  }
  function J0(l, t) {
    var a = t.action, u = t.payload, e = l.state;
    if (t.isTransition) {
      var n = b.T, f = {};
      b.T = f;
      try {
        var c = a(e, u), i = b.S;
        i !== null && i(f, c), w0(l, t, c);
      } catch (o) {
        lc(l, t, o);
      } finally {
        n !== null && f.types !== null && (n.types = f.types), b.T = n;
      }
    } else
      try {
        n = a(e, u), w0(l, t, n);
      } catch (o) {
        lc(l, t, o);
      }
  }
  function w0(l, t, a) {
    a !== null && typeof a == "object" && typeof a.then == "function" ? a.then(
      function(u) {
        W0(l, t, u);
      },
      function(u) {
        return lc(l, t, u);
      }
    ) : W0(l, t, a);
  }
  function W0(l, t, a) {
    t.status = "fulfilled", t.value = a, $0(t), l.state = a, t = l.pending, t !== null && (a = t.next, a === t ? l.pending = null : (a = a.next, t.next = a, J0(l, a)));
  }
  function lc(l, t, a) {
    var u = l.pending;
    if (l.pending = null, u !== null) {
      u = u.next;
      do
        t.status = "rejected", t.reason = a, $0(t), t = t.next;
      while (t !== u);
    }
    l.action = null;
  }
  function $0(l) {
    l = l.listeners;
    for (var t = 0; t < l.length; t++) (0, l[t])();
  }
  function k0(l, t) {
    return t;
  }
  function F0(l, t) {
    if (k) {
      var a = sl.formState;
      if (a !== null) {
        l: {
          var u = Q;
          if (k) {
            if (vl) {
              t: {
                for (var e = vl, n = dt; e.nodeType !== 8; ) {
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
                vl = ht(
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
      lastRenderedReducer: k0,
      lastRenderedState: t
    }, a.queue = u, a = Ss.bind(
      null,
      Q,
      u
    ), u.dispatch = a, u = Pf(!1), n = fc.bind(
      null,
      Q,
      !1,
      u.queue
    ), u = Yl(), e = {
      state: t,
      dispatch: null,
      action: l,
      pending: null
    }, u.queue = e, a = Lm.bind(
      null,
      Q,
      e,
      n,
      a
    ), e.dispatch = a, u.memoizedState = l, [t, a, !1];
  }
  function I0(l) {
    var t = Tl();
    return P0(t, nl, l);
  }
  function P0(l, t, a) {
    if (t = Ff(
      l,
      t,
      k0
    )[0], l = un(Qt)[0], typeof t == "object" && t !== null && typeof t.then == "function")
      try {
        var u = Iu(t);
      } catch (f) {
        throw f === su ? we : f;
      }
    else u = t;
    t = Tl();
    var e = t.queue, n = e.dispatch;
    return a !== t.memoizedState && (Q.flags |= 2048, ou(
      9,
      { destroy: void 0 },
      Vm.bind(null, e, a),
      null
    )), [u, n, l];
  }
  function Vm(l, t) {
    l.action = t;
  }
  function ls(l) {
    var t = Tl(), a = nl;
    if (a !== null)
      return P0(t, a, l);
    Tl(), t = t.memoizedState, a = Tl();
    var u = a.queue.dispatch;
    return a.memoizedState = l, [t, u, !1];
  }
  function ou(l, t, a, u) {
    return l = { tag: l, create: a, deps: u, inst: t, next: null }, t = Q.updateQueue, t === null && (t = tn(), Q.updateQueue = t), a = t.lastEffect, a === null ? t.lastEffect = l.next = l : (u = a.next, a.next = l, l.next = u, t.lastEffect = l), l;
  }
  function ts() {
    return Tl().memoizedState;
  }
  function en(l, t, a, u) {
    var e = Yl();
    Q.flags |= l, e.memoizedState = ou(
      1 | t,
      { destroy: void 0 },
      a,
      u === void 0 ? null : u
    );
  }
  function nn(l, t, a, u) {
    var e = Tl();
    u = u === void 0 ? null : u;
    var n = e.memoizedState.inst;
    nl !== null && u !== null && Kf(u, nl.memoizedState.deps) ? e.memoizedState = ou(t, n, a, u) : (Q.flags |= l, e.memoizedState = ou(
      1 | t,
      n,
      a,
      u
    ));
  }
  function as(l, t) {
    en(8390656, 8, l, t);
  }
  function tc(l, t) {
    nn(2048, 8, l, t);
  }
  function xm(l) {
    Q.flags |= 4;
    var t = Q.updateQueue;
    if (t === null)
      t = tn(), Q.updateQueue = t, t.events = [l];
    else {
      var a = t.events;
      a === null ? t.events = [l] : a.push(l);
    }
  }
  function us(l) {
    var t = Tl().memoizedState;
    return xm({ ref: t, nextImpl: l }), function() {
      if ((ll & 2) !== 0) throw Error(m(440));
      return t.impl.apply(void 0, arguments);
    };
  }
  function es(l, t) {
    return nn(4, 2, l, t);
  }
  function ns(l, t) {
    return nn(4, 4, l, t);
  }
  function fs(l, t) {
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
  function cs(l, t, a) {
    a = a != null ? a.concat([l]) : null, nn(4, 4, fs.bind(null, t, l), a);
  }
  function ac() {
  }
  function is(l, t) {
    var a = Tl();
    t = t === void 0 ? null : t;
    var u = a.memoizedState;
    return t !== null && Kf(t, u[1]) ? u[0] : (a.memoizedState = [l, t], l);
  }
  function ss(l, t) {
    var a = Tl();
    t = t === void 0 ? null : t;
    var u = a.memoizedState;
    if (t !== null && Kf(t, u[1]))
      return u[0];
    if (u = l(), Ga) {
      kt(!0);
      try {
        l();
      } finally {
        kt(!1);
      }
    }
    return a.memoizedState = [u, t], u;
  }
  function uc(l, t, a) {
    return a === void 0 || (Xt & 1073741824) !== 0 && (J & 261930) === 0 ? l.memoizedState = t : (l.memoizedState = a, l = vv(), Q.lanes |= l, va |= l, a);
  }
  function vs(l, t, a, u) {
    return Pl(a, t) ? a : yu.current !== null ? (l = uc(l, a, u), Pl(l, t) || (Al = !0), l) : (Xt & 42) === 0 || (Xt & 1073741824) !== 0 && (J & 261930) === 0 ? (Al = !0, l.memoizedState = a) : (l = vv(), Q.lanes |= l, va |= l, t);
  }
  function ys(l, t, a, u, e) {
    var n = _.p;
    _.p = n !== 0 && 8 > n ? n : 8;
    var f = b.T, c = {};
    b.T = c, fc(l, !1, t, a);
    try {
      var i = e(), o = b.S;
      if (o !== null && o(c, i), i !== null && typeof i == "object" && typeof i.then == "function") {
        var g = Qm(
          i,
          u
        );
        Pu(
          l,
          t,
          g,
          nt(l)
        );
      } else
        Pu(
          l,
          t,
          u,
          nt(l)
        );
    } catch (z) {
      Pu(
        l,
        t,
        { then: function() {
        }, status: "rejected", reason: z },
        nt()
      );
    } finally {
      _.p = n, f !== null && c.types !== null && (f.types = c.types), b.T = f;
    }
  }
  function Km() {
  }
  function ec(l, t, a, u) {
    if (l.tag !== 5) throw Error(m(476));
    var e = ms(l).queue;
    ys(
      l,
      e,
      t,
      q,
      a === null ? Km : function() {
        return ds(l), a(u);
      }
    );
  }
  function ms(l) {
    var t = l.memoizedState;
    if (t !== null) return t;
    t = {
      memoizedState: q,
      baseState: q,
      baseQueue: null,
      queue: {
        pending: null,
        lanes: 0,
        dispatch: null,
        lastRenderedReducer: Qt,
        lastRenderedState: q
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
        lastRenderedReducer: Qt,
        lastRenderedState: a
      },
      next: null
    }, l.memoizedState = t, l = l.alternate, l !== null && (l.memoizedState = t), t;
  }
  function ds(l) {
    var t = ms(l);
    t.next === null && (t = l.alternate.memoizedState), Pu(
      l,
      t.next.queue,
      {},
      nt()
    );
  }
  function nc() {
    return pl(Se);
  }
  function os() {
    return Tl().memoizedState;
  }
  function hs() {
    return Tl().memoizedState;
  }
  function Jm(l) {
    for (var t = l.return; t !== null; ) {
      switch (t.tag) {
        case 24:
        case 3:
          var a = nt();
          l = ea(a);
          var u = na(t, l, a);
          u !== null && (wl(u, t, a), Wu(u, t, a)), t = { cache: Bf() }, l.payload = t;
          return;
      }
      t = t.return;
    }
  }
  function wm(l, t, a) {
    var u = nt();
    a = {
      lane: u,
      revertLane: 0,
      gesture: null,
      action: a,
      hasEagerState: !1,
      eagerState: null,
      next: null
    }, fn(l) ? gs(t, a) : (a = rf(l, t, a, u), a !== null && (wl(a, l, u), bs(a, t, u)));
  }
  function Ss(l, t, a) {
    var u = nt();
    Pu(l, t, a, u);
  }
  function Pu(l, t, a, u) {
    var e = {
      lane: u,
      revertLane: 0,
      gesture: null,
      action: a,
      hasEagerState: !1,
      eagerState: null,
      next: null
    };
    if (fn(l)) gs(t, e);
    else {
      var n = l.alternate;
      if (l.lanes === 0 && (n === null || n.lanes === 0) && (n = t.lastRenderedReducer, n !== null))
        try {
          var f = t.lastRenderedState, c = n(f, a);
          if (e.hasEagerState = !0, e.eagerState = c, Pl(c, f))
            return je(l, t, e, 0), sl === null && Qe(), !1;
        } catch {
        } finally {
        }
      if (a = rf(l, t, e, u), a !== null)
        return wl(a, l, u), bs(a, t, u), !0;
    }
    return !1;
  }
  function fc(l, t, a, u) {
    if (u = {
      lane: 2,
      revertLane: Xc(),
      gesture: null,
      action: u,
      hasEagerState: !1,
      eagerState: null,
      next: null
    }, fn(l)) {
      if (t) throw Error(m(479));
    } else
      t = rf(
        l,
        a,
        u,
        2
      ), t !== null && wl(t, l, 2);
  }
  function fn(l) {
    var t = l.alternate;
    return l === Q || t !== null && t === Q;
  }
  function gs(l, t) {
    mu = Pe = !0;
    var a = l.pending;
    a === null ? t.next = t : (t.next = a.next, a.next = t), l.pending = t;
  }
  function bs(l, t, a) {
    if ((a & 4194048) !== 0) {
      var u = t.lanes;
      u &= l.pendingLanes, a |= u, t.lanes = a, ri(l, a);
    }
  }
  var le = {
    readContext: pl,
    use: an,
    useCallback: ol,
    useContext: ol,
    useEffect: ol,
    useImperativeHandle: ol,
    useLayoutEffect: ol,
    useInsertionEffect: ol,
    useMemo: ol,
    useReducer: ol,
    useRef: ol,
    useState: ol,
    useDebugValue: ol,
    useDeferredValue: ol,
    useTransition: ol,
    useSyncExternalStore: ol,
    useId: ol,
    useHostTransitionStatus: ol,
    useFormState: ol,
    useActionState: ol,
    useOptimistic: ol,
    useMemoCache: ol,
    useCacheRefresh: ol
  };
  le.useEffectEvent = ol;
  var Ts = {
    readContext: pl,
    use: an,
    useCallback: function(l, t) {
      return Yl().memoizedState = [
        l,
        t === void 0 ? null : t
      ], l;
    },
    useContext: pl,
    useEffect: as,
    useImperativeHandle: function(l, t, a) {
      a = a != null ? a.concat([l]) : null, en(
        4194308,
        4,
        fs.bind(null, t, l),
        a
      );
    },
    useLayoutEffect: function(l, t) {
      return en(4194308, 4, l, t);
    },
    useInsertionEffect: function(l, t) {
      en(4, 2, l, t);
    },
    useMemo: function(l, t) {
      var a = Yl();
      t = t === void 0 ? null : t;
      var u = l();
      if (Ga) {
        kt(!0);
        try {
          l();
        } finally {
          kt(!1);
        }
      }
      return a.memoizedState = [u, t], u;
    },
    useReducer: function(l, t, a) {
      var u = Yl();
      if (a !== void 0) {
        var e = a(t);
        if (Ga) {
          kt(!0);
          try {
            a(t);
          } finally {
            kt(!1);
          }
        }
      } else e = t;
      return u.memoizedState = u.baseState = e, l = {
        pending: null,
        lanes: 0,
        dispatch: null,
        lastRenderedReducer: l,
        lastRenderedState: e
      }, u.queue = l, l = l.dispatch = wm.bind(
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
      l = Pf(l);
      var t = l.queue, a = Ss.bind(null, Q, t);
      return t.dispatch = a, [l.memoizedState, a];
    },
    useDebugValue: ac,
    useDeferredValue: function(l, t) {
      var a = Yl();
      return uc(a, l, t);
    },
    useTransition: function() {
      var l = Pf(!1);
      return l = ys.bind(
        null,
        Q,
        l.queue,
        !0,
        !1
      ), Yl().memoizedState = l, [!1, l];
    },
    useSyncExternalStore: function(l, t, a) {
      var u = Q, e = Yl();
      if (k) {
        if (a === void 0)
          throw Error(m(407));
        a = a();
      } else {
        if (a = t(), sl === null)
          throw Error(m(349));
        (J & 127) !== 0 || j0(u, t, a);
      }
      e.memoizedState = a;
      var n = { value: a, getSnapshot: t };
      return e.queue = n, as(L0.bind(null, u, n, l), [
        l
      ]), u.flags |= 2048, ou(
        9,
        { destroy: void 0 },
        Z0.bind(
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
      var l = Yl(), t = sl.identifierPrefix;
      if (k) {
        var a = Ot, u = _t;
        a = (u & ~(1 << 32 - Il(u) - 1)).toString(32) + a, t = "_" + t + "R_" + a, a = ln++, 0 < a && (t += "H" + a.toString(32)), t += "_";
      } else
        a = jm++, t = "_" + t + "r_" + a.toString(32) + "_";
      return l.memoizedState = t;
    },
    useHostTransitionStatus: nc,
    useFormState: F0,
    useActionState: F0,
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
      return t.queue = a, t = fc.bind(
        null,
        Q,
        !0,
        a
      ), a.dispatch = t, [l, t];
    },
    useMemoCache: kf,
    useCacheRefresh: function() {
      return Yl().memoizedState = Jm.bind(
        null,
        Q
      );
    },
    useEffectEvent: function(l) {
      var t = Yl(), a = { impl: l };
      return t.memoizedState = a, function() {
        if ((ll & 2) !== 0)
          throw Error(m(440));
        return a.impl.apply(void 0, arguments);
      };
    }
  }, cc = {
    readContext: pl,
    use: an,
    useCallback: is,
    useContext: pl,
    useEffect: tc,
    useImperativeHandle: cs,
    useInsertionEffect: es,
    useLayoutEffect: ns,
    useMemo: ss,
    useReducer: un,
    useRef: ts,
    useState: function() {
      return un(Qt);
    },
    useDebugValue: ac,
    useDeferredValue: function(l, t) {
      var a = Tl();
      return vs(
        a,
        nl.memoizedState,
        l,
        t
      );
    },
    useTransition: function() {
      var l = un(Qt)[0], t = Tl().memoizedState;
      return [
        typeof l == "boolean" ? l : Iu(l),
        t
      ];
    },
    useSyncExternalStore: Q0,
    useId: os,
    useHostTransitionStatus: nc,
    useFormState: I0,
    useActionState: I0,
    useOptimistic: function(l, t) {
      var a = Tl();
      return K0(a, nl, l, t);
    },
    useMemoCache: kf,
    useCacheRefresh: hs
  };
  cc.useEffectEvent = us;
  var zs = {
    readContext: pl,
    use: an,
    useCallback: is,
    useContext: pl,
    useEffect: tc,
    useImperativeHandle: cs,
    useInsertionEffect: es,
    useLayoutEffect: ns,
    useMemo: ss,
    useReducer: If,
    useRef: ts,
    useState: function() {
      return If(Qt);
    },
    useDebugValue: ac,
    useDeferredValue: function(l, t) {
      var a = Tl();
      return nl === null ? uc(a, l, t) : vs(
        a,
        nl.memoizedState,
        l,
        t
      );
    },
    useTransition: function() {
      var l = If(Qt)[0], t = Tl().memoizedState;
      return [
        typeof l == "boolean" ? l : Iu(l),
        t
      ];
    },
    useSyncExternalStore: Q0,
    useId: os,
    useHostTransitionStatus: nc,
    useFormState: ls,
    useActionState: ls,
    useOptimistic: function(l, t) {
      var a = Tl();
      return nl !== null ? K0(a, nl, l, t) : (a.baseState = l, [l, a.queue.dispatch]);
    },
    useMemoCache: kf,
    useCacheRefresh: hs
  };
  zs.useEffectEvent = us;
  function ic(l, t, a, u) {
    t = l.memoizedState, a = a(u, t), a = a == null ? t : B({}, t, a), l.memoizedState = a, l.lanes === 0 && (l.updateQueue.baseState = a);
  }
  var sc = {
    enqueueSetState: function(l, t, a) {
      l = l._reactInternals;
      var u = nt(), e = ea(u);
      e.payload = t, a != null && (e.callback = a), t = na(l, e, u), t !== null && (wl(t, l, u), Wu(t, l, u));
    },
    enqueueReplaceState: function(l, t, a) {
      l = l._reactInternals;
      var u = nt(), e = ea(u);
      e.tag = 1, e.payload = t, a != null && (e.callback = a), t = na(l, e, u), t !== null && (wl(t, l, u), Wu(t, l, u));
    },
    enqueueForceUpdate: function(l, t) {
      l = l._reactInternals;
      var a = nt(), u = ea(a);
      u.tag = 2, t != null && (u.callback = t), t = na(l, u, a), t !== null && (wl(t, l, a), Wu(t, l, a));
    }
  };
  function Es(l, t, a, u, e, n, f) {
    return l = l.stateNode, typeof l.shouldComponentUpdate == "function" ? l.shouldComponentUpdate(u, n, f) : t.prototype && t.prototype.isPureReactComponent ? !ju(a, u) || !ju(e, n) : !0;
  }
  function As(l, t, a, u) {
    l = t.state, typeof t.componentWillReceiveProps == "function" && t.componentWillReceiveProps(a, u), typeof t.UNSAFE_componentWillReceiveProps == "function" && t.UNSAFE_componentWillReceiveProps(a, u), t.state !== l && sc.enqueueReplaceState(t, t.state, null);
  }
  function Xa(l, t) {
    var a = t;
    if ("ref" in t) {
      a = {};
      for (var u in t)
        u !== "ref" && (a[u] = t[u]);
    }
    if (l = l.defaultProps) {
      a === t && (a = B({}, a));
      for (var e in l)
        a[e] === void 0 && (a[e] = l[e]);
    }
    return a;
  }
  function rs(l) {
    Xe(l);
  }
  function _s(l) {
    console.error(l);
  }
  function Os(l) {
    Xe(l);
  }
  function cn(l, t) {
    try {
      var a = l.onUncaughtError;
      a(t.value, { componentStack: t.stack });
    } catch (u) {
      setTimeout(function() {
        throw u;
      });
    }
  }
  function Ms(l, t, a) {
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
  function vc(l, t, a) {
    return a = ea(a), a.tag = 3, a.payload = { element: null }, a.callback = function() {
      cn(l, t);
    }, a;
  }
  function Ds(l) {
    return l = ea(l), l.tag = 3, l;
  }
  function Us(l, t, a, u) {
    var e = a.type.getDerivedStateFromError;
    if (typeof e == "function") {
      var n = u.value;
      l.payload = function() {
        return e(n);
      }, l.callback = function() {
        Ms(t, a, u);
      };
    }
    var f = a.stateNode;
    f !== null && typeof f.componentDidCatch == "function" && (l.callback = function() {
      Ms(t, a, u), typeof e != "function" && (ya === null ? ya = /* @__PURE__ */ new Set([this]) : ya.add(this));
      var c = u.stack;
      this.componentDidCatch(u.value, {
        componentStack: c !== null ? c : ""
      });
    });
  }
  function Wm(l, t, a, u, e) {
    if (a.flags |= 32768, u !== null && typeof u == "object" && typeof u.then == "function") {
      if (t = a.alternate, t !== null && fu(
        t,
        a,
        e,
        !0
      ), a = tt.current, a !== null) {
        switch (a.tag) {
          case 31:
          case 13:
            return ot === null ? zn() : a.alternate === null && hl === 0 && (hl = 3), a.flags &= -257, a.flags |= 65536, a.lanes = e, u === We ? a.flags |= 16384 : (t = a.updateQueue, t === null ? a.updateQueue = /* @__PURE__ */ new Set([u]) : t.add(u), qc(l, u, e)), !1;
          case 22:
            return a.flags |= 65536, u === We ? a.flags |= 16384 : (t = a.updateQueue, t === null ? (t = {
              transitions: null,
              markerInstances: null,
              retryQueue: /* @__PURE__ */ new Set([u])
            }, a.updateQueue = t) : (a = t.retryQueue, a === null ? t.retryQueue = /* @__PURE__ */ new Set([u]) : a.add(u)), qc(l, u, e)), !1;
        }
        throw Error(m(435, a.tag));
      }
      return qc(l, u, e), zn(), !1;
    }
    if (k)
      return t = tt.current, t !== null ? ((t.flags & 65536) === 0 && (t.flags |= 256), t.flags |= 65536, t.lanes = e, u !== pf && (l = Error(m(422), { cause: u }), Vu(vt(l, a)))) : (u !== pf && (t = Error(m(423), {
        cause: u
      }), Vu(
        vt(t, a)
      )), l = l.current.alternate, l.flags |= 65536, e &= -e, l.lanes |= e, u = vt(u, a), e = vc(
        l.stateNode,
        u,
        e
      ), jf(l, e), hl !== 4 && (hl = 2)), !1;
    var n = Error(m(520), { cause: u });
    if (n = vt(n, a), ie === null ? ie = [n] : ie.push(n), hl !== 4 && (hl = 2), t === null) return !0;
    u = vt(u, a), a = t;
    do {
      switch (a.tag) {
        case 3:
          return a.flags |= 65536, l = e & -e, a.lanes |= l, l = vc(a.stateNode, u, l), jf(a, l), !1;
        case 1:
          if (t = a.type, n = a.stateNode, (a.flags & 128) === 0 && (typeof t.getDerivedStateFromError == "function" || n !== null && typeof n.componentDidCatch == "function" && (ya === null || !ya.has(n))))
            return a.flags |= 65536, e &= -e, a.lanes |= e, e = Ds(e), Us(
              e,
              l,
              a,
              u
            ), jf(a, e), !1;
      }
      a = a.return;
    } while (a !== null);
    return !1;
  }
  var yc = Error(m(461)), Al = !1;
  function Cl(l, t, a, u) {
    t.child = l === null ? H0(t, null, a, u) : Ya(
      t,
      l.child,
      a,
      u
    );
  }
  function ps(l, t, a, u, e) {
    a = a.render;
    var n = t.ref;
    if ("ref" in u) {
      var f = {};
      for (var c in u)
        c !== "ref" && (f[c] = u[c]);
    } else f = u;
    return Ha(t), u = Jf(
      l,
      t,
      a,
      f,
      n,
      e
    ), c = wf(), l !== null && !Al ? (Wf(l, t, e), jt(l, t, e)) : (k && c && Df(t), t.flags |= 1, Cl(l, t, u, e), t.child);
  }
  function Cs(l, t, a, u, e) {
    if (l === null) {
      var n = a.type;
      return typeof n == "function" && !_f(n) && n.defaultProps === void 0 && a.compare === null ? (t.tag = 15, t.type = n, Ns(
        l,
        t,
        n,
        u,
        e
      )) : (l = Le(
        a.type,
        null,
        u,
        t,
        t.mode,
        e
      ), l.ref = t.ref, l.return = t, t.child = l);
    }
    if (n = l.child, !Tc(l, e)) {
      var f = n.memoizedProps;
      if (a = a.compare, a = a !== null ? a : ju, a(f, u) && l.ref === t.ref)
        return jt(l, t, e);
    }
    return t.flags |= 1, l = Bt(n, u), l.ref = t.ref, l.return = t, t.child = l;
  }
  function Ns(l, t, a, u, e) {
    if (l !== null) {
      var n = l.memoizedProps;
      if (ju(n, u) && l.ref === t.ref)
        if (Al = !1, t.pendingProps = u = n, Tc(l, e))
          (l.flags & 131072) !== 0 && (Al = !0);
        else
          return t.lanes = l.lanes, jt(l, t, e);
    }
    return mc(
      l,
      t,
      a,
      u,
      e
    );
  }
  function Hs(l, t, a, u) {
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
        return Rs(
          l,
          t,
          n,
          a,
          u
        );
      }
      if ((a & 536870912) !== 0)
        t.memoizedState = { baseLanes: 0, cachePool: null }, l !== null && Je(
          t,
          n !== null ? n.cachePool : null
        ), n !== null ? q0(t, n) : Lf(), Y0(t);
      else
        return u = t.lanes = 536870912, Rs(
          l,
          t,
          n !== null ? n.baseLanes | a : a,
          a,
          u
        );
    } else
      n !== null ? (Je(t, n.cachePool), q0(t, n), ca(), t.memoizedState = null) : (l !== null && Je(t, null), Lf(), ca());
    return Cl(l, t, e, a), t.child;
  }
  function te(l, t) {
    return l !== null && l.tag === 22 || t.stateNode !== null || (t.stateNode = {
      _visibility: 1,
      _pendingMarkers: null,
      _retryCache: null,
      _transitions: null
    }), t.sibling;
  }
  function Rs(l, t, a, u, e) {
    var n = Yf();
    return n = n === null ? null : { parent: zl._currentValue, pool: n }, t.memoizedState = {
      baseLanes: a,
      cachePool: n
    }, l !== null && Je(t, null), Lf(), Y0(t), l !== null && fu(l, t, u, !0), t.childLanes = e, null;
  }
  function sn(l, t) {
    return t = yn(
      { mode: t.mode, children: t.children },
      l.mode
    ), t.ref = l.ref, l.child = t, t.return = l, t;
  }
  function Bs(l, t, a) {
    return Ya(t, l.child, null, a), l = sn(t, t.pendingProps), l.flags |= 2, at(t), t.memoizedState = null, l;
  }
  function $m(l, t, a) {
    var u = t.pendingProps, e = (t.flags & 128) !== 0;
    if (t.flags &= -129, l === null) {
      if (k) {
        if (u.mode === "hidden")
          return l = sn(t, u), t.lanes = 536870912, te(null, l);
        if (xf(t), (l = vl) ? (l = Jv(
          l,
          dt
        ), l = l !== null && l.data === "&" ? l : null, l !== null && (t.memoizedState = {
          dehydrated: l,
          treeContext: Pt !== null ? { id: _t, overflow: Ot } : null,
          retryLane: 536870912,
          hydrationErrors: null
        }, a = g0(l), a.return = t, t.child = a, Ul = t, vl = null)) : l = null, l === null) throw ta(t);
        return t.lanes = 536870912, null;
      }
      return sn(t, u);
    }
    var n = l.memoizedState;
    if (n !== null) {
      var f = n.dehydrated;
      if (xf(t), e)
        if (t.flags & 256)
          t.flags &= -257, t = Bs(
            l,
            t,
            a
          );
        else if (t.memoizedState !== null)
          t.child = l.child, t.flags |= 128, t = null;
        else throw Error(m(558));
      else if (Al || fu(l, t, a, !1), e = (a & l.childLanes) !== 0, Al || e) {
        if (u = sl, u !== null && (f = _i(u, a), f !== 0 && f !== n.retryLane))
          throw n.retryLane = f, Ua(l, f), wl(u, l, f), yc;
        zn(), t = Bs(
          l,
          t,
          a
        );
      } else
        l = n.treeContext, vl = ht(f.nextSibling), Ul = t, k = !0, la = null, dt = !1, l !== null && z0(t, l), t = sn(t, u), t.flags |= 4096;
      return t;
    }
    return l = Bt(l.child, {
      mode: u.mode,
      children: u.children
    }), l.ref = t.ref, t.child = l, l.return = t, l;
  }
  function vn(l, t) {
    var a = t.ref;
    if (a === null)
      l !== null && l.ref !== null && (t.flags |= 4194816);
    else {
      if (typeof a != "function" && typeof a != "object")
        throw Error(m(284));
      (l === null || l.ref !== a) && (t.flags |= 4194816);
    }
  }
  function mc(l, t, a, u, e) {
    return Ha(t), a = Jf(
      l,
      t,
      a,
      u,
      void 0,
      e
    ), u = wf(), l !== null && !Al ? (Wf(l, t, e), jt(l, t, e)) : (k && u && Df(t), t.flags |= 1, Cl(l, t, a, e), t.child);
  }
  function qs(l, t, a, u, e, n) {
    return Ha(t), t.updateQueue = null, a = X0(
      t,
      u,
      a,
      e
    ), G0(l), u = wf(), l !== null && !Al ? (Wf(l, t, n), jt(l, t, n)) : (k && u && Df(t), t.flags |= 1, Cl(l, t, a, n), t.child);
  }
  function Ys(l, t, a, u, e) {
    if (Ha(t), t.stateNode === null) {
      var n = au, f = a.contextType;
      typeof f == "object" && f !== null && (n = pl(f)), n = new a(u, n), t.memoizedState = n.state !== null && n.state !== void 0 ? n.state : null, n.updater = sc, t.stateNode = n, n._reactInternals = t, n = t.stateNode, n.props = u, n.state = t.memoizedState, n.refs = {}, Xf(t), f = a.contextType, n.context = typeof f == "object" && f !== null ? pl(f) : au, n.state = t.memoizedState, f = a.getDerivedStateFromProps, typeof f == "function" && (ic(
        t,
        a,
        f,
        u
      ), n.state = t.memoizedState), typeof a.getDerivedStateFromProps == "function" || typeof n.getSnapshotBeforeUpdate == "function" || typeof n.UNSAFE_componentWillMount != "function" && typeof n.componentWillMount != "function" || (f = n.state, typeof n.componentWillMount == "function" && n.componentWillMount(), typeof n.UNSAFE_componentWillMount == "function" && n.UNSAFE_componentWillMount(), f !== n.state && sc.enqueueReplaceState(n, n.state, null), ku(t, u, n, e), $u(), n.state = t.memoizedState), typeof n.componentDidMount == "function" && (t.flags |= 4194308), u = !0;
    } else if (l === null) {
      n = t.stateNode;
      var c = t.memoizedProps, i = Xa(a, c);
      n.props = i;
      var o = n.context, g = a.contextType;
      f = au, typeof g == "object" && g !== null && (f = pl(g));
      var z = a.getDerivedStateFromProps;
      g = typeof z == "function" || typeof n.getSnapshotBeforeUpdate == "function", c = t.pendingProps !== c, g || typeof n.UNSAFE_componentWillReceiveProps != "function" && typeof n.componentWillReceiveProps != "function" || (c || o !== f) && As(
        t,
        n,
        u,
        f
      ), ua = !1;
      var h = t.memoizedState;
      n.state = h, ku(t, u, n, e), $u(), o = t.memoizedState, c || h !== o || ua ? (typeof z == "function" && (ic(
        t,
        a,
        z,
        u
      ), o = t.memoizedState), (i = ua || Es(
        t,
        a,
        i,
        u,
        h,
        o,
        f
      )) ? (g || typeof n.UNSAFE_componentWillMount != "function" && typeof n.componentWillMount != "function" || (typeof n.componentWillMount == "function" && n.componentWillMount(), typeof n.UNSAFE_componentWillMount == "function" && n.UNSAFE_componentWillMount()), typeof n.componentDidMount == "function" && (t.flags |= 4194308)) : (typeof n.componentDidMount == "function" && (t.flags |= 4194308), t.memoizedProps = u, t.memoizedState = o), n.props = u, n.state = o, n.context = f, u = i) : (typeof n.componentDidMount == "function" && (t.flags |= 4194308), u = !1);
    } else {
      n = t.stateNode, Qf(l, t), f = t.memoizedProps, g = Xa(a, f), n.props = g, z = t.pendingProps, h = n.context, o = a.contextType, i = au, typeof o == "object" && o !== null && (i = pl(o)), c = a.getDerivedStateFromProps, (o = typeof c == "function" || typeof n.getSnapshotBeforeUpdate == "function") || typeof n.UNSAFE_componentWillReceiveProps != "function" && typeof n.componentWillReceiveProps != "function" || (f !== z || h !== i) && As(
        t,
        n,
        u,
        i
      ), ua = !1, h = t.memoizedState, n.state = h, ku(t, u, n, e), $u();
      var S = t.memoizedState;
      f !== z || h !== S || ua || l !== null && l.dependencies !== null && xe(l.dependencies) ? (typeof c == "function" && (ic(
        t,
        a,
        c,
        u
      ), S = t.memoizedState), (g = ua || Es(
        t,
        a,
        g,
        u,
        h,
        S,
        i
      ) || l !== null && l.dependencies !== null && xe(l.dependencies)) ? (o || typeof n.UNSAFE_componentWillUpdate != "function" && typeof n.componentWillUpdate != "function" || (typeof n.componentWillUpdate == "function" && n.componentWillUpdate(u, S, i), typeof n.UNSAFE_componentWillUpdate == "function" && n.UNSAFE_componentWillUpdate(
        u,
        S,
        i
      )), typeof n.componentDidUpdate == "function" && (t.flags |= 4), typeof n.getSnapshotBeforeUpdate == "function" && (t.flags |= 1024)) : (typeof n.componentDidUpdate != "function" || f === l.memoizedProps && h === l.memoizedState || (t.flags |= 4), typeof n.getSnapshotBeforeUpdate != "function" || f === l.memoizedProps && h === l.memoizedState || (t.flags |= 1024), t.memoizedProps = u, t.memoizedState = S), n.props = u, n.state = S, n.context = i, u = g) : (typeof n.componentDidUpdate != "function" || f === l.memoizedProps && h === l.memoizedState || (t.flags |= 4), typeof n.getSnapshotBeforeUpdate != "function" || f === l.memoizedProps && h === l.memoizedState || (t.flags |= 1024), u = !1);
    }
    return n = u, vn(l, t), u = (t.flags & 128) !== 0, n || u ? (n = t.stateNode, a = u && typeof a.getDerivedStateFromError != "function" ? null : n.render(), t.flags |= 1, l !== null && u ? (t.child = Ya(
      t,
      l.child,
      null,
      e
    ), t.child = Ya(
      t,
      null,
      a,
      e
    )) : Cl(l, t, a, e), t.memoizedState = n.state, l = t.child) : l = jt(
      l,
      t,
      e
    ), l;
  }
  function Gs(l, t, a, u) {
    return Ca(), t.flags |= 256, Cl(l, t, a, u), t.child;
  }
  var dc = {
    dehydrated: null,
    treeContext: null,
    retryLane: 0,
    hydrationErrors: null
  };
  function oc(l) {
    return { baseLanes: l, cachePool: M0() };
  }
  function hc(l, t, a) {
    return l = l !== null ? l.childLanes & ~a : 0, t && (l |= et), l;
  }
  function Xs(l, t, a) {
    var u = t.pendingProps, e = !1, n = (t.flags & 128) !== 0, f;
    if ((f = n) || (f = l !== null && l.memoizedState === null ? !1 : (bl.current & 2) !== 0), f && (e = !0, t.flags &= -129), f = (t.flags & 32) !== 0, t.flags &= -33, l === null) {
      if (k) {
        if (e ? fa(t) : ca(), (l = vl) ? (l = Jv(
          l,
          dt
        ), l = l !== null && l.data !== "&" ? l : null, l !== null && (t.memoizedState = {
          dehydrated: l,
          treeContext: Pt !== null ? { id: _t, overflow: Ot } : null,
          retryLane: 536870912,
          hydrationErrors: null
        }, a = g0(l), a.return = t, t.child = a, Ul = t, vl = null)) : l = null, l === null) throw ta(t);
        return Fc(l) ? t.lanes = 32 : t.lanes = 536870912, null;
      }
      var c = u.children;
      return u = u.fallback, e ? (ca(), e = t.mode, c = yn(
        { mode: "hidden", children: c },
        e
      ), u = pa(
        u,
        e,
        a,
        null
      ), c.return = t, u.return = t, c.sibling = u, t.child = c, u = t.child, u.memoizedState = oc(a), u.childLanes = hc(
        l,
        f,
        a
      ), t.memoizedState = dc, te(null, u)) : (fa(t), Sc(t, c));
    }
    var i = l.memoizedState;
    if (i !== null && (c = i.dehydrated, c !== null)) {
      if (n)
        t.flags & 256 ? (fa(t), t.flags &= -257, t = gc(
          l,
          t,
          a
        )) : t.memoizedState !== null ? (ca(), t.child = l.child, t.flags |= 128, t = null) : (ca(), c = u.fallback, e = t.mode, u = yn(
          { mode: "visible", children: u.children },
          e
        ), c = pa(
          c,
          e,
          a,
          null
        ), c.flags |= 2, u.return = t, c.return = t, u.sibling = c, t.child = u, Ya(
          t,
          l.child,
          null,
          a
        ), u = t.child, u.memoizedState = oc(a), u.childLanes = hc(
          l,
          f,
          a
        ), t.memoizedState = dc, t = te(null, u));
      else if (fa(t), Fc(c)) {
        if (f = c.nextSibling && c.nextSibling.dataset, f) var o = f.dgst;
        f = o, u = Error(m(419)), u.stack = "", u.digest = f, Vu({ value: u, source: null, stack: null }), t = gc(
          l,
          t,
          a
        );
      } else if (Al || fu(l, t, a, !1), f = (a & l.childLanes) !== 0, Al || f) {
        if (f = sl, f !== null && (u = _i(f, a), u !== 0 && u !== i.retryLane))
          throw i.retryLane = u, Ua(l, u), wl(f, l, u), yc;
        kc(c) || zn(), t = gc(
          l,
          t,
          a
        );
      } else
        kc(c) ? (t.flags |= 192, t.child = l.child, t = null) : (l = i.treeContext, vl = ht(
          c.nextSibling
        ), Ul = t, k = !0, la = null, dt = !1, l !== null && z0(t, l), t = Sc(
          t,
          u.children
        ), t.flags |= 4096);
      return t;
    }
    return e ? (ca(), c = u.fallback, e = t.mode, i = l.child, o = i.sibling, u = Bt(i, {
      mode: "hidden",
      children: u.children
    }), u.subtreeFlags = i.subtreeFlags & 65011712, o !== null ? c = Bt(
      o,
      c
    ) : (c = pa(
      c,
      e,
      a,
      null
    ), c.flags |= 2), c.return = t, u.return = t, u.sibling = c, t.child = u, te(null, u), u = t.child, c = l.child.memoizedState, c === null ? c = oc(a) : (e = c.cachePool, e !== null ? (i = zl._currentValue, e = e.parent !== i ? { parent: i, pool: i } : e) : e = M0(), c = {
      baseLanes: c.baseLanes | a,
      cachePool: e
    }), u.memoizedState = c, u.childLanes = hc(
      l,
      f,
      a
    ), t.memoizedState = dc, te(l.child, u)) : (fa(t), a = l.child, l = a.sibling, a = Bt(a, {
      mode: "visible",
      children: u.children
    }), a.return = t, a.sibling = null, l !== null && (f = t.deletions, f === null ? (t.deletions = [l], t.flags |= 16) : f.push(l)), t.child = a, t.memoizedState = null, a);
  }
  function Sc(l, t) {
    return t = yn(
      { mode: "visible", children: t },
      l.mode
    ), t.return = l, l.child = t;
  }
  function yn(l, t) {
    return l = lt(22, l, null, t), l.lanes = 0, l;
  }
  function gc(l, t, a) {
    return Ya(t, l.child, null, a), l = Sc(
      t,
      t.pendingProps.children
    ), l.flags |= 2, t.memoizedState = null, l;
  }
  function Qs(l, t, a) {
    l.lanes |= t;
    var u = l.alternate;
    u !== null && (u.lanes |= t), Hf(l.return, t, a);
  }
  function bc(l, t, a, u, e, n) {
    var f = l.memoizedState;
    f === null ? l.memoizedState = {
      isBackwards: t,
      rendering: null,
      renderingStartTime: 0,
      last: u,
      tail: a,
      tailMode: e,
      treeForkCount: n
    } : (f.isBackwards = t, f.rendering = null, f.renderingStartTime = 0, f.last = u, f.tail = a, f.tailMode = e, f.treeForkCount = n);
  }
  function js(l, t, a) {
    var u = t.pendingProps, e = u.revealOrder, n = u.tail;
    u = u.children;
    var f = bl.current, c = (f & 2) !== 0;
    if (c ? (f = f & 1 | 2, t.flags |= 128) : f &= 1, O(bl, f), Cl(l, t, u, a), u = k ? Lu : 0, !c && l !== null && (l.flags & 128) !== 0)
      l: for (l = t.child; l !== null; ) {
        if (l.tag === 13)
          l.memoizedState !== null && Qs(l, a, t);
        else if (l.tag === 19)
          Qs(l, a, t);
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
          l = a.alternate, l !== null && Ie(l) === null && (e = a), a = a.sibling;
        a = e, a === null ? (e = t.child, t.child = null) : (e = a.sibling, a.sibling = null), bc(
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
          if (l = e.alternate, l !== null && Ie(l) === null) {
            t.child = e;
            break;
          }
          l = e.sibling, e.sibling = a, a = e, e = l;
        }
        bc(
          t,
          !0,
          a,
          null,
          n,
          u
        );
        break;
      case "together":
        bc(
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
  function jt(l, t, a) {
    if (l !== null && (t.dependencies = l.dependencies), va |= t.lanes, (a & t.childLanes) === 0)
      if (l !== null) {
        if (fu(
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
      for (l = t.child, a = Bt(l, l.pendingProps), t.child = a, a.return = t; l.sibling !== null; )
        l = l.sibling, a = a.sibling = Bt(l, l.pendingProps), a.return = t;
      a.sibling = null;
    }
    return t.child;
  }
  function Tc(l, t) {
    return (l.lanes & t) !== 0 ? !0 : (l = l.dependencies, !!(l !== null && xe(l)));
  }
  function km(l, t, a) {
    switch (t.tag) {
      case 3:
        ql(t, t.stateNode.containerInfo), aa(t, zl, l.memoizedState.cache), Ca();
        break;
      case 27:
      case 5:
        Du(t);
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
          return t.flags |= 128, xf(t), null;
        break;
      case 13:
        var u = t.memoizedState;
        if (u !== null)
          return u.dehydrated !== null ? (fa(t), t.flags |= 128, null) : (a & t.child.childLanes) !== 0 ? Xs(l, t, a) : (fa(t), l = jt(
            l,
            t,
            a
          ), l !== null ? l.sibling : null);
        fa(t);
        break;
      case 19:
        var e = (l.flags & 128) !== 0;
        if (u = (a & t.childLanes) !== 0, u || (fu(
          l,
          t,
          a,
          !1
        ), u = (a & t.childLanes) !== 0), e) {
          if (u)
            return js(
              l,
              t,
              a
            );
          t.flags |= 128;
        }
        if (e = t.memoizedState, e !== null && (e.rendering = null, e.tail = null, e.lastEffect = null), O(bl, bl.current), u) break;
        return null;
      case 22:
        return t.lanes = 0, Hs(
          l,
          t,
          a,
          t.pendingProps
        );
      case 24:
        aa(t, zl, l.memoizedState.cache);
    }
    return jt(l, t, a);
  }
  function Zs(l, t, a) {
    if (l !== null)
      if (l.memoizedProps !== t.pendingProps)
        Al = !0;
      else {
        if (!Tc(l, a) && (t.flags & 128) === 0)
          return Al = !1, km(
            l,
            t,
            a
          );
        Al = (l.flags & 131072) !== 0;
      }
    else
      Al = !1, k && (t.flags & 1048576) !== 0 && T0(t, Lu, t.index);
    switch (t.lanes = 0, t.tag) {
      case 16:
        l: {
          var u = t.pendingProps;
          if (l = Ba(t.elementType), t.type = l, typeof l == "function")
            _f(l) ? (u = Xa(l, u), t.tag = 1, t = Ys(
              null,
              t,
              l,
              u,
              a
            )) : (t.tag = 0, t = mc(
              null,
              t,
              l,
              u,
              a
            ));
          else {
            if (l != null) {
              var e = l.$$typeof;
              if (e === ft) {
                t.tag = 11, t = ps(
                  null,
                  t,
                  l,
                  u,
                  a
                );
                break l;
              } else if (e === $) {
                t.tag = 14, t = Cs(
                  null,
                  t,
                  l,
                  u,
                  a
                );
                break l;
              }
            }
            throw t = Ct(l) || l, Error(m(306, t, ""));
          }
        }
        return t;
      case 0:
        return mc(
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
        ), Ys(
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
          e = n.element, Qf(l, t), ku(t, u, null, a);
          var f = t.memoizedState;
          if (u = f.cache, aa(t, zl, u), u !== n.cache && Rf(
            t,
            [zl],
            a,
            !0
          ), $u(), u = f.element, n.isDehydrated)
            if (n = {
              element: u,
              isDehydrated: !1,
              cache: f.cache
            }, t.updateQueue.baseState = n, t.memoizedState = n, t.flags & 256) {
              t = Gs(
                l,
                t,
                u,
                a
              );
              break l;
            } else if (u !== e) {
              e = vt(
                Error(m(424)),
                t
              ), Vu(e), t = Gs(
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
              for (vl = ht(l.firstChild), Ul = t, k = !0, la = null, dt = !0, a = H0(
                t,
                null,
                u,
                a
              ), t.child = a; a; )
                a.flags = a.flags & -3 | 4096, a = a.sibling;
            }
          else {
            if (Ca(), u === e) {
              t = jt(
                l,
                t,
                a
              );
              break l;
            }
            Cl(l, t, u, a);
          }
          t = t.child;
        }
        return t;
      case 26:
        return vn(l, t), l === null ? (a = Iv(
          t.type,
          null,
          t.pendingProps,
          null
        )) ? t.memoizedState = a : k || (a = t.type, l = t.pendingProps, u = Dn(
          V.current
        ).createElement(a), u[Dl] = t, u[Zl] = l, Nl(u, a, l), Ol(u), t.stateNode = u) : t.memoizedState = Iv(
          t.type,
          l.memoizedProps,
          t.pendingProps,
          l.memoizedState
        ), null;
      case 27:
        return Du(t), l === null && k && (u = t.stateNode = $v(
          t.type,
          t.pendingProps,
          V.current
        ), Ul = t, dt = !0, e = vl, ha(t.type) ? (Ic = e, vl = ht(u.firstChild)) : vl = e), Cl(
          l,
          t,
          t.pendingProps.children,
          a
        ), vn(l, t), l === null && (t.flags |= 4194304), t.child;
      case 5:
        return l === null && k && ((e = u = vl) && (u = Md(
          u,
          t.type,
          t.pendingProps,
          dt
        ), u !== null ? (t.stateNode = u, Ul = t, vl = ht(u.firstChild), dt = !1, e = !0) : e = !1), e || ta(t)), Du(t), e = t.type, n = t.pendingProps, f = l !== null ? l.memoizedProps : null, u = n.children, wc(e, n) ? u = null : f !== null && wc(e, f) && (t.flags |= 32), t.memoizedState !== null && (e = Jf(
          l,
          t,
          Zm,
          null,
          null,
          a
        ), Se._currentValue = e), vn(l, t), Cl(l, t, u, a), t.child;
      case 6:
        return l === null && k && ((l = a = vl) && (a = Dd(
          a,
          t.pendingProps,
          dt
        ), a !== null ? (t.stateNode = a, Ul = t, vl = null, l = !0) : l = !1), l || ta(t)), null;
      case 13:
        return Xs(l, t, a);
      case 4:
        return ql(
          t,
          t.stateNode.containerInfo
        ), u = t.pendingProps, l === null ? t.child = Ya(
          t,
          null,
          u,
          a
        ) : Cl(l, t, u, a), t.child;
      case 11:
        return ps(
          l,
          t,
          t.type,
          t.pendingProps,
          a
        );
      case 7:
        return Cl(
          l,
          t,
          t.pendingProps,
          a
        ), t.child;
      case 8:
        return Cl(
          l,
          t,
          t.pendingProps.children,
          a
        ), t.child;
      case 12:
        return Cl(
          l,
          t,
          t.pendingProps.children,
          a
        ), t.child;
      case 10:
        return u = t.pendingProps, aa(t, t.type, u.value), Cl(l, t, u.children, a), t.child;
      case 9:
        return e = t.type._context, u = t.pendingProps.children, Ha(t), e = pl(e), u = u(e), t.flags |= 1, Cl(l, t, u, a), t.child;
      case 14:
        return Cs(
          l,
          t,
          t.type,
          t.pendingProps,
          a
        );
      case 15:
        return Ns(
          l,
          t,
          t.type,
          t.pendingProps,
          a
        );
      case 19:
        return js(l, t, a);
      case 31:
        return $m(l, t, a);
      case 22:
        return Hs(
          l,
          t,
          a,
          t.pendingProps
        );
      case 24:
        return Ha(t), u = pl(zl), l === null ? (e = Yf(), e === null && (e = sl, n = Bf(), e.pooledCache = n, n.refCount++, n !== null && (e.pooledCacheLanes |= a), e = n), t.memoizedState = { parent: u, cache: e }, Xf(t), aa(t, zl, e)) : ((l.lanes & a) !== 0 && (Qf(l, t), ku(t, null, null, a), $u()), e = l.memoizedState, n = t.memoizedState, e.parent !== u ? (e = { parent: u, cache: u }, t.memoizedState = e, t.lanes === 0 && (t.memoizedState = t.updateQueue.baseState = e), aa(t, zl, u)) : (u = n.cache, aa(t, zl, u), u !== e.cache && Rf(
          t,
          [zl],
          a,
          !0
        ))), Cl(
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
  function Zt(l) {
    l.flags |= 4;
  }
  function zc(l, t, a, u, e) {
    if ((t = (l.mode & 32) !== 0) && (t = !1), t) {
      if (l.flags |= 16777216, (e & 335544128) === e)
        if (l.stateNode.complete) l.flags |= 8192;
        else if (ov()) l.flags |= 8192;
        else
          throw qa = We, Gf;
    } else l.flags &= -16777217;
  }
  function Ls(l, t) {
    if (t.type !== "stylesheet" || (t.state.loading & 4) !== 0)
      l.flags &= -16777217;
    else if (l.flags |= 16777216, !uy(t))
      if (ov()) l.flags |= 8192;
      else
        throw qa = We, Gf;
  }
  function mn(l, t) {
    t !== null && (l.flags |= 4), l.flags & 16384 && (t = l.tag !== 22 ? Ei() : 536870912, l.lanes |= t, bu |= t);
  }
  function ae(l, t) {
    if (!k)
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
  function yl(l) {
    var t = l.alternate !== null && l.alternate.child === l.child, a = 0, u = 0;
    if (t)
      for (var e = l.child; e !== null; )
        a |= e.lanes | e.childLanes, u |= e.subtreeFlags & 65011712, u |= e.flags & 65011712, e.return = l, e = e.sibling;
    else
      for (e = l.child; e !== null; )
        a |= e.lanes | e.childLanes, u |= e.subtreeFlags, u |= e.flags, e.return = l, e = e.sibling;
    return l.subtreeFlags |= u, l.childLanes = a, t;
  }
  function Fm(l, t, a) {
    var u = t.pendingProps;
    switch (Uf(t), t.tag) {
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
        return a = t.stateNode, u = null, l !== null && (u = l.memoizedState.cache), t.memoizedState.cache !== u && (t.flags |= 2048), Gt(zl), gl(), a.pendingContext && (a.context = a.pendingContext, a.pendingContext = null), (l === null || l.child === null) && (nu(t) ? Zt(t) : l === null || l.memoizedState.isDehydrated && (t.flags & 256) === 0 || (t.flags |= 1024, Cf())), yl(t), null;
      case 26:
        var e = t.type, n = t.memoizedState;
        return l === null ? (Zt(t), n !== null ? (yl(t), Ls(t, n)) : (yl(t), zc(
          t,
          e,
          null,
          u,
          a
        ))) : n ? n !== l.memoizedState ? (Zt(t), yl(t), Ls(t, n)) : (yl(t), t.flags &= -16777217) : (l = l.memoizedProps, l !== u && Zt(t), yl(t), zc(
          t,
          e,
          l,
          u,
          a
        )), null;
      case 27:
        if (Ae(t), a = V.current, e = t.type, l !== null && t.stateNode != null)
          l.memoizedProps !== u && Zt(t);
        else {
          if (!u) {
            if (t.stateNode === null)
              throw Error(m(166));
            return yl(t), null;
          }
          l = C.current, nu(t) ? E0(t) : (l = $v(e, u, a), t.stateNode = l, Zt(t));
        }
        return yl(t), null;
      case 5:
        if (Ae(t), e = t.type, l !== null && t.stateNode != null)
          l.memoizedProps !== u && Zt(t);
        else {
          if (!u) {
            if (t.stateNode === null)
              throw Error(m(166));
            return yl(t), null;
          }
          if (n = C.current, nu(t))
            E0(t);
          else {
            var f = Dn(
              V.current
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
                    n = typeof u.is == "string" ? f.createElement("select", {
                      is: u.is
                    }) : f.createElement("select"), u.multiple ? n.multiple = !0 : u.size && (n.size = u.size);
                    break;
                  default:
                    n = typeof u.is == "string" ? f.createElement(e, { is: u.is }) : f.createElement(e);
                }
            }
            n[Dl] = t, n[Zl] = u;
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
            u && Zt(t);
          }
        }
        return yl(t), zc(
          t,
          t.type,
          l === null ? null : l.memoizedProps,
          t.pendingProps,
          a
        ), null;
      case 6:
        if (l && t.stateNode != null)
          l.memoizedProps !== u && Zt(t);
        else {
          if (typeof u != "string" && t.stateNode === null)
            throw Error(m(166));
          if (l = V.current, nu(t)) {
            if (l = t.stateNode, a = t.memoizedProps, u = null, e = Ul, e !== null)
              switch (e.tag) {
                case 27:
                case 5:
                  u = e.memoizedProps;
              }
            l[Dl] = t, l = !!(l.nodeValue === a || u !== null && u.suppressHydrationWarning === !0 || Xv(l.nodeValue, a)), l || ta(t, !0);
          } else
            l = Dn(l).createTextNode(
              u
            ), l[Dl] = t, t.stateNode = l;
        }
        return yl(t), null;
      case 31:
        if (a = t.memoizedState, l === null || l.memoizedState !== null) {
          if (u = nu(t), a !== null) {
            if (l === null) {
              if (!u) throw Error(m(318));
              if (l = t.memoizedState, l = l !== null ? l.dehydrated : null, !l) throw Error(m(557));
              l[Dl] = t;
            } else
              Ca(), (t.flags & 128) === 0 && (t.memoizedState = null), t.flags |= 4;
            yl(t), l = !1;
          } else
            a = Cf(), l !== null && l.memoizedState !== null && (l.memoizedState.hydrationErrors = a), l = !0;
          if (!l)
            return t.flags & 256 ? (at(t), t) : (at(t), null);
          if ((t.flags & 128) !== 0)
            throw Error(m(558));
        }
        return yl(t), null;
      case 13:
        if (u = t.memoizedState, l === null || l.memoizedState !== null && l.memoizedState.dehydrated !== null) {
          if (e = nu(t), u !== null && u.dehydrated !== null) {
            if (l === null) {
              if (!e) throw Error(m(318));
              if (e = t.memoizedState, e = e !== null ? e.dehydrated : null, !e) throw Error(m(317));
              e[Dl] = t;
            } else
              Ca(), (t.flags & 128) === 0 && (t.memoizedState = null), t.flags |= 4;
            yl(t), e = !1;
          } else
            e = Cf(), l !== null && l.memoizedState !== null && (l.memoizedState.hydrationErrors = e), e = !0;
          if (!e)
            return t.flags & 256 ? (at(t), t) : (at(t), null);
        }
        return at(t), (t.flags & 128) !== 0 ? (t.lanes = a, t) : (a = u !== null, l = l !== null && l.memoizedState !== null, a && (u = t.child, e = null, u.alternate !== null && u.alternate.memoizedState !== null && u.alternate.memoizedState.cachePool !== null && (e = u.alternate.memoizedState.cachePool.pool), n = null, u.memoizedState !== null && u.memoizedState.cachePool !== null && (n = u.memoizedState.cachePool.pool), n !== e && (u.flags |= 2048)), a !== l && a && (t.child.flags |= 8192), mn(t, t.updateQueue), yl(t), null);
      case 4:
        return gl(), l === null && Lc(t.stateNode.containerInfo), yl(t), null;
      case 10:
        return Gt(t.type), yl(t), null;
      case 19:
        if (E(bl), u = t.memoizedState, u === null) return yl(t), null;
        if (e = (t.flags & 128) !== 0, n = u.rendering, n === null)
          if (e) ae(u, !1);
          else {
            if (hl !== 0 || l !== null && (l.flags & 128) !== 0)
              for (l = t.child; l !== null; ) {
                if (n = Ie(l), n !== null) {
                  for (t.flags |= 128, ae(u, !1), l = n.updateQueue, t.updateQueue = l, mn(t, l), t.subtreeFlags = 0, l = a, a = t.child; a !== null; )
                    S0(a, l), a = a.sibling;
                  return O(
                    bl,
                    bl.current & 1 | 2
                  ), k && qt(t, u.treeForkCount), t.child;
                }
                l = l.sibling;
              }
            u.tail !== null && kl() > gn && (t.flags |= 128, e = !0, ae(u, !1), t.lanes = 4194304);
          }
        else {
          if (!e)
            if (l = Ie(n), l !== null) {
              if (t.flags |= 128, e = !0, l = l.updateQueue, t.updateQueue = l, mn(t, l), ae(u, !0), u.tail === null && u.tailMode === "hidden" && !n.alternate && !k)
                return yl(t), null;
            } else
              2 * kl() - u.renderingStartTime > gn && a !== 536870912 && (t.flags |= 128, e = !0, ae(u, !1), t.lanes = 4194304);
          u.isBackwards ? (n.sibling = t.child, t.child = n) : (l = u.last, l !== null ? l.sibling = n : t.child = n, u.last = n);
        }
        return u.tail !== null ? (l = u.tail, u.rendering = l, u.tail = l.sibling, u.renderingStartTime = kl(), l.sibling = null, a = bl.current, O(
          bl,
          e ? a & 1 | 2 : a & 1
        ), k && qt(t, u.treeForkCount), l) : (yl(t), null);
      case 22:
      case 23:
        return at(t), Vf(), u = t.memoizedState !== null, l !== null ? l.memoizedState !== null !== u && (t.flags |= 8192) : u && (t.flags |= 8192), u ? (a & 536870912) !== 0 && (t.flags & 128) === 0 && (yl(t), t.subtreeFlags & 6 && (t.flags |= 8192)) : yl(t), a = t.updateQueue, a !== null && mn(t, a.retryQueue), a = null, l !== null && l.memoizedState !== null && l.memoizedState.cachePool !== null && (a = l.memoizedState.cachePool.pool), u = null, t.memoizedState !== null && t.memoizedState.cachePool !== null && (u = t.memoizedState.cachePool.pool), u !== a && (t.flags |= 2048), l !== null && E(Ra), null;
      case 24:
        return a = null, l !== null && (a = l.memoizedState.cache), t.memoizedState.cache !== a && (t.flags |= 2048), Gt(zl), yl(t), null;
      case 25:
        return null;
      case 30:
        return null;
    }
    throw Error(m(156, t.tag));
  }
  function Im(l, t) {
    switch (Uf(t), t.tag) {
      case 1:
        return l = t.flags, l & 65536 ? (t.flags = l & -65537 | 128, t) : null;
      case 3:
        return Gt(zl), gl(), l = t.flags, (l & 65536) !== 0 && (l & 128) === 0 ? (t.flags = l & -65537 | 128, t) : null;
      case 26:
      case 27:
      case 5:
        return Ae(t), null;
      case 31:
        if (t.memoizedState !== null) {
          if (at(t), t.alternate === null)
            throw Error(m(340));
          Ca();
        }
        return l = t.flags, l & 65536 ? (t.flags = l & -65537 | 128, t) : null;
      case 13:
        if (at(t), l = t.memoizedState, l !== null && l.dehydrated !== null) {
          if (t.alternate === null)
            throw Error(m(340));
          Ca();
        }
        return l = t.flags, l & 65536 ? (t.flags = l & -65537 | 128, t) : null;
      case 19:
        return E(bl), null;
      case 4:
        return gl(), null;
      case 10:
        return Gt(t.type), null;
      case 22:
      case 23:
        return at(t), Vf(), l !== null && E(Ra), l = t.flags, l & 65536 ? (t.flags = l & -65537 | 128, t) : null;
      case 24:
        return Gt(zl), null;
      case 25:
        return null;
      default:
        return null;
    }
  }
  function Vs(l, t) {
    switch (Uf(t), t.tag) {
      case 3:
        Gt(zl), gl();
        break;
      case 26:
      case 27:
      case 5:
        Ae(t);
        break;
      case 4:
        gl();
        break;
      case 31:
        t.memoizedState !== null && at(t);
        break;
      case 13:
        at(t);
        break;
      case 19:
        E(bl);
        break;
      case 10:
        Gt(t.type);
        break;
      case 22:
      case 23:
        at(t), Vf(), l !== null && E(Ra);
        break;
      case 24:
        Gt(zl);
    }
  }
  function ue(l, t) {
    try {
      var a = t.updateQueue, u = a !== null ? a.lastEffect : null;
      if (u !== null) {
        var e = u.next;
        a = e;
        do {
          if ((a.tag & l) === l) {
            u = void 0;
            var n = a.create, f = a.inst;
            u = n(), f.destroy = u;
          }
          a = a.next;
        } while (a !== e);
      }
    } catch (c) {
      ul(t, t.return, c);
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
            var f = u.inst, c = f.destroy;
            if (c !== void 0) {
              f.destroy = void 0, e = t;
              var i = a, o = c;
              try {
                o();
              } catch (g) {
                ul(
                  e,
                  i,
                  g
                );
              }
            }
          }
          u = u.next;
        } while (u !== n);
      }
    } catch (g) {
      ul(t, t.return, g);
    }
  }
  function xs(l) {
    var t = l.updateQueue;
    if (t !== null) {
      var a = l.stateNode;
      try {
        B0(t, a);
      } catch (u) {
        ul(l, l.return, u);
      }
    }
  }
  function Ks(l, t, a) {
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
  function ee(l, t) {
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
  function Mt(l, t) {
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
  function Js(l) {
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
  function Ec(l, t, a) {
    try {
      var u = l.stateNode;
      zd(u, l.type, a, t), u[Zl] = t;
    } catch (e) {
      ul(l, l.return, e);
    }
  }
  function ws(l) {
    return l.tag === 5 || l.tag === 3 || l.tag === 26 || l.tag === 27 && ha(l.type) || l.tag === 4;
  }
  function Ac(l) {
    l: for (; ; ) {
      for (; l.sibling === null; ) {
        if (l.return === null || ws(l.return)) return null;
        l = l.return;
      }
      for (l.sibling.return = l.return, l = l.sibling; l.tag !== 5 && l.tag !== 6 && l.tag !== 18; ) {
        if (l.tag === 27 && ha(l.type) || l.flags & 2 || l.child === null || l.tag === 4) continue l;
        l.child.return = l, l = l.child;
      }
      if (!(l.flags & 2)) return l.stateNode;
    }
  }
  function rc(l, t, a) {
    var u = l.tag;
    if (u === 5 || u === 6)
      l = l.stateNode, t ? (a.nodeType === 9 ? a.body : a.nodeName === "HTML" ? a.ownerDocument.body : a).insertBefore(l, t) : (t = a.nodeType === 9 ? a.body : a.nodeName === "HTML" ? a.ownerDocument.body : a, t.appendChild(l), a = a._reactRootContainer, a != null || t.onclick !== null || (t.onclick = Ht));
    else if (u !== 4 && (u === 27 && ha(l.type) && (a = l.stateNode, t = null), l = l.child, l !== null))
      for (rc(l, t, a), l = l.sibling; l !== null; )
        rc(l, t, a), l = l.sibling;
  }
  function dn(l, t, a) {
    var u = l.tag;
    if (u === 5 || u === 6)
      l = l.stateNode, t ? a.insertBefore(l, t) : a.appendChild(l);
    else if (u !== 4 && (u === 27 && ha(l.type) && (a = l.stateNode), l = l.child, l !== null))
      for (dn(l, t, a), l = l.sibling; l !== null; )
        dn(l, t, a), l = l.sibling;
  }
  function Ws(l) {
    var t = l.stateNode, a = l.memoizedProps;
    try {
      for (var u = l.type, e = t.attributes; e.length; )
        t.removeAttributeNode(e[0]);
      Nl(t, u, a), t[Dl] = l, t[Zl] = a;
    } catch (n) {
      ul(l, l.return, n);
    }
  }
  var Lt = !1, rl = !1, _c = !1, $s = typeof WeakSet == "function" ? WeakSet : Set, Ml = null;
  function Pm(l, t) {
    if (l = l.containerInfo, Kc = Bn, l = c0(l), gf(l)) {
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
            var f = 0, c = -1, i = -1, o = 0, g = 0, z = l, h = null;
            t: for (; ; ) {
              for (var S; z !== a || e !== 0 && z.nodeType !== 3 || (c = f + e), z !== n || u !== 0 && z.nodeType !== 3 || (i = f + u), z.nodeType === 3 && (f += z.nodeValue.length), (S = z.firstChild) !== null; )
                h = z, z = S;
              for (; ; ) {
                if (z === l) break t;
                if (h === a && ++o === e && (c = f), h === n && ++g === u && (i = f), (S = z.nextSibling) !== null) break;
                z = h, h = z.parentNode;
              }
              z = S;
            }
            a = c === -1 || i === -1 ? null : { start: c, end: i };
          } else a = null;
        }
      a = a || { start: 0, end: 0 };
    } else a = null;
    for (Jc = { focusedElem: l, selectionRange: a }, Bn = !1, Ml = t; Ml !== null; )
      if (t = Ml, l = t.child, (t.subtreeFlags & 1028) !== 0 && l !== null)
        l.return = t, Ml = l;
      else
        for (; Ml !== null; ) {
          switch (t = Ml, n = t.alternate, l = t.flags, t.tag) {
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
                } catch (R) {
                  ul(
                    a,
                    a.return,
                    R
                  );
                }
              }
              break;
            case 3:
              if ((l & 1024) !== 0) {
                if (l = t.stateNode.containerInfo, a = l.nodeType, a === 9)
                  $c(l);
                else if (a === 1)
                  switch (l.nodeName) {
                    case "HEAD":
                    case "HTML":
                    case "BODY":
                      $c(l);
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
            l.return = t.return, Ml = l;
            break;
          }
          Ml = t.return;
        }
  }
  function ks(l, t, a) {
    var u = a.flags;
    switch (a.tag) {
      case 0:
      case 11:
      case 15:
        xt(l, a), u & 4 && ue(5, a);
        break;
      case 1:
        if (xt(l, a), u & 4)
          if (l = a.stateNode, t === null)
            try {
              l.componentDidMount();
            } catch (f) {
              ul(a, a.return, f);
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
            } catch (f) {
              ul(
                a,
                a.return,
                f
              );
            }
          }
        u & 64 && xs(a), u & 512 && ee(a, a.return);
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
            B0(l, t);
          } catch (f) {
            ul(a, a.return, f);
          }
        }
        break;
      case 27:
        t === null && u & 4 && Ws(a);
      case 26:
      case 5:
        xt(l, a), t === null && u & 4 && Js(a), u & 512 && ee(a, a.return);
        break;
      case 12:
        xt(l, a);
        break;
      case 31:
        xt(l, a), u & 4 && Ps(l, a);
        break;
      case 13:
        xt(l, a), u & 4 && lv(l, a), u & 64 && (l = a.memoizedState, l !== null && (l = l.dehydrated, l !== null && (a = id.bind(
          null,
          a
        ), Ud(l, a))));
        break;
      case 22:
        if (u = a.memoizedState !== null || Lt, !u) {
          t = t !== null && t.memoizedState !== null || rl, e = Lt;
          var n = rl;
          Lt = u, (rl = t) && !n ? Kt(
            l,
            a,
            (a.subtreeFlags & 8772) !== 0
          ) : xt(l, a), Lt = e, rl = n;
        }
        break;
      case 30:
        break;
      default:
        xt(l, a);
    }
  }
  function Fs(l) {
    var t = l.alternate;
    t !== null && (l.alternate = null, Fs(t)), l.child = null, l.deletions = null, l.sibling = null, l.tag === 5 && (t = l.stateNode, t !== null && Pn(t)), l.stateNode = null, l.return = null, l.dependencies = null, l.memoizedProps = null, l.memoizedState = null, l.pendingProps = null, l.stateNode = null, l.updateQueue = null;
  }
  var dl = null, Vl = !1;
  function Vt(l, t, a) {
    for (a = a.child; a !== null; )
      Is(l, t, a), a = a.sibling;
  }
  function Is(l, t, a) {
    if (Fl && typeof Fl.onCommitFiberUnmount == "function")
      try {
        Fl.onCommitFiberUnmount(Uu, a);
      } catch {
      }
    switch (a.tag) {
      case 26:
        rl || Mt(a, t), Vt(
          l,
          t,
          a
        ), a.memoizedState ? a.memoizedState.count-- : a.stateNode && (a = a.stateNode, a.parentNode.removeChild(a));
        break;
      case 27:
        rl || Mt(a, t);
        var u = dl, e = Vl;
        ha(a.type) && (dl = a.stateNode, Vl = !1), Vt(
          l,
          t,
          a
        ), de(a.stateNode), dl = u, Vl = e;
        break;
      case 5:
        rl || Mt(a, t);
      case 6:
        if (u = dl, e = Vl, dl = null, Vt(
          l,
          t,
          a
        ), dl = u, Vl = e, dl !== null)
          if (Vl)
            try {
              (dl.nodeType === 9 ? dl.body : dl.nodeName === "HTML" ? dl.ownerDocument.body : dl).removeChild(a.stateNode);
            } catch (n) {
              ul(
                a,
                t,
                n
              );
            }
          else
            try {
              dl.removeChild(a.stateNode);
            } catch (n) {
              ul(
                a,
                t,
                n
              );
            }
        break;
      case 18:
        dl !== null && (Vl ? (l = dl, xv(
          l.nodeType === 9 ? l.body : l.nodeName === "HTML" ? l.ownerDocument.body : l,
          a.stateNode
        ), Mu(l)) : xv(dl, a.stateNode));
        break;
      case 4:
        u = dl, e = Vl, dl = a.stateNode.containerInfo, Vl = !0, Vt(
          l,
          t,
          a
        ), dl = u, Vl = e;
        break;
      case 0:
      case 11:
      case 14:
      case 15:
        ia(2, a, t), rl || ia(4, a, t), Vt(
          l,
          t,
          a
        );
        break;
      case 1:
        rl || (Mt(a, t), u = a.stateNode, typeof u.componentWillUnmount == "function" && Ks(
          a,
          t,
          u
        )), Vt(
          l,
          t,
          a
        );
        break;
      case 21:
        Vt(
          l,
          t,
          a
        );
        break;
      case 22:
        rl = (u = rl) || a.memoizedState !== null, Vt(
          l,
          t,
          a
        ), rl = u;
        break;
      default:
        Vt(
          l,
          t,
          a
        );
    }
  }
  function Ps(l, t) {
    if (t.memoizedState === null && (l = t.alternate, l !== null && (l = l.memoizedState, l !== null))) {
      l = l.dehydrated;
      try {
        Mu(l);
      } catch (a) {
        ul(t, t.return, a);
      }
    }
  }
  function lv(l, t) {
    if (t.memoizedState === null && (l = t.alternate, l !== null && (l = l.memoizedState, l !== null && (l = l.dehydrated, l !== null))))
      try {
        Mu(l);
      } catch (a) {
        ul(t, t.return, a);
      }
  }
  function ld(l) {
    switch (l.tag) {
      case 31:
      case 13:
      case 19:
        var t = l.stateNode;
        return t === null && (t = l.stateNode = new $s()), t;
      case 22:
        return l = l.stateNode, t = l._retryCache, t === null && (t = l._retryCache = new $s()), t;
      default:
        throw Error(m(435, l.tag));
    }
  }
  function on(l, t) {
    var a = ld(l);
    t.forEach(function(u) {
      if (!a.has(u)) {
        a.add(u);
        var e = sd.bind(null, l, u);
        u.then(e, e);
      }
    });
  }
  function xl(l, t) {
    var a = t.deletions;
    if (a !== null)
      for (var u = 0; u < a.length; u++) {
        var e = a[u], n = l, f = t, c = f;
        l: for (; c !== null; ) {
          switch (c.tag) {
            case 27:
              if (ha(c.type)) {
                dl = c.stateNode, Vl = !1;
                break l;
              }
              break;
            case 5:
              dl = c.stateNode, Vl = !1;
              break l;
            case 3:
            case 4:
              dl = c.stateNode.containerInfo, Vl = !0;
              break l;
          }
          c = c.return;
        }
        if (dl === null) throw Error(m(160));
        Is(n, f, e), dl = null, Vl = !1, n = e.alternate, n !== null && (n.return = null), e.return = null;
      }
    if (t.subtreeFlags & 13886)
      for (t = t.child; t !== null; )
        tv(t, l), t = t.sibling;
  }
  var Tt = null;
  function tv(l, t) {
    var a = l.alternate, u = l.flags;
    switch (l.tag) {
      case 0:
      case 11:
      case 14:
      case 15:
        xl(t, l), Kl(l), u & 4 && (ia(3, l, l.return), ue(3, l), ia(5, l, l.return));
        break;
      case 1:
        xl(t, l), Kl(l), u & 512 && (rl || a === null || Mt(a, a.return)), u & 64 && Lt && (l = l.updateQueue, l !== null && (u = l.callbacks, u !== null && (a = l.shared.hiddenCallbacks, l.shared.hiddenCallbacks = a === null ? u : a.concat(u))));
        break;
      case 26:
        var e = Tt;
        if (xl(t, l), Kl(l), u & 512 && (rl || a === null || Mt(a, a.return)), u & 4) {
          var n = a !== null ? a.memoizedState : null;
          if (u = l.memoizedState, a === null)
            if (u === null)
              if (l.stateNode === null) {
                l: {
                  u = l.type, a = l.memoizedProps, e = e.ownerDocument || e;
                  t: switch (u) {
                    case "title":
                      n = e.getElementsByTagName("title")[0], (!n || n[Nu] || n[Dl] || n.namespaceURI === "http://www.w3.org/2000/svg" || n.hasAttribute("itemprop")) && (n = e.createElement(u), e.head.insertBefore(
                        n,
                        e.querySelector("head > title")
                      )), Nl(n, u, a), n[Dl] = l, Ol(n), u = n;
                      break l;
                    case "link":
                      var f = ty(
                        "link",
                        "href",
                        e
                      ).get(u + (a.href || ""));
                      if (f) {
                        for (var c = 0; c < f.length; c++)
                          if (n = f[c], n.getAttribute("href") === (a.href == null || a.href === "" ? null : a.href) && n.getAttribute("rel") === (a.rel == null ? null : a.rel) && n.getAttribute("title") === (a.title == null ? null : a.title) && n.getAttribute("crossorigin") === (a.crossOrigin == null ? null : a.crossOrigin)) {
                            f.splice(c, 1);
                            break t;
                          }
                      }
                      n = e.createElement(u), Nl(n, u, a), e.head.appendChild(n);
                      break;
                    case "meta":
                      if (f = ty(
                        "meta",
                        "content",
                        e
                      ).get(u + (a.content || ""))) {
                        for (c = 0; c < f.length; c++)
                          if (n = f[c], n.getAttribute("content") === (a.content == null ? null : "" + a.content) && n.getAttribute("name") === (a.name == null ? null : a.name) && n.getAttribute("property") === (a.property == null ? null : a.property) && n.getAttribute("http-equiv") === (a.httpEquiv == null ? null : a.httpEquiv) && n.getAttribute("charset") === (a.charSet == null ? null : a.charSet)) {
                            f.splice(c, 1);
                            break t;
                          }
                      }
                      n = e.createElement(u), Nl(n, u, a), e.head.appendChild(n);
                      break;
                    default:
                      throw Error(m(468, u));
                  }
                  n[Dl] = l, Ol(n), u = n;
                }
                l.stateNode = u;
              } else
                ay(
                  e,
                  l.type,
                  l.stateNode
                );
            else
              l.stateNode = ly(
                e,
                u,
                l.memoizedProps
              );
          else
            n !== u ? (n === null ? a.stateNode !== null && (a = a.stateNode, a.parentNode.removeChild(a)) : n.count--, u === null ? ay(
              e,
              l.type,
              l.stateNode
            ) : ly(
              e,
              u,
              l.memoizedProps
            )) : u === null && l.stateNode !== null && Ec(
              l,
              l.memoizedProps,
              a.memoizedProps
            );
        }
        break;
      case 27:
        xl(t, l), Kl(l), u & 512 && (rl || a === null || Mt(a, a.return)), a !== null && u & 4 && Ec(
          l,
          l.memoizedProps,
          a.memoizedProps
        );
        break;
      case 5:
        if (xl(t, l), Kl(l), u & 512 && (rl || a === null || Mt(a, a.return)), l.flags & 32) {
          e = l.stateNode;
          try {
            $a(e, "");
          } catch (U) {
            ul(l, l.return, U);
          }
        }
        u & 4 && l.stateNode != null && (e = l.memoizedProps, Ec(
          l,
          e,
          a !== null ? a.memoizedProps : e
        )), u & 1024 && (_c = !0);
        break;
      case 6:
        if (xl(t, l), Kl(l), u & 4) {
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
        if (Cn = null, e = Tt, Tt = Un(t.containerInfo), xl(t, l), Tt = e, Kl(l), u & 4 && a !== null && a.memoizedState.isDehydrated)
          try {
            Mu(t.containerInfo);
          } catch (U) {
            ul(l, l.return, U);
          }
        _c && (_c = !1, av(l));
        break;
      case 4:
        u = Tt, Tt = Un(
          l.stateNode.containerInfo
        ), xl(t, l), Kl(l), Tt = u;
        break;
      case 12:
        xl(t, l), Kl(l);
        break;
      case 31:
        xl(t, l), Kl(l), u & 4 && (u = l.updateQueue, u !== null && (l.updateQueue = null, on(l, u)));
        break;
      case 13:
        xl(t, l), Kl(l), l.child.flags & 8192 && l.memoizedState !== null != (a !== null && a.memoizedState !== null) && (Sn = kl()), u & 4 && (u = l.updateQueue, u !== null && (l.updateQueue = null, on(l, u)));
        break;
      case 22:
        e = l.memoizedState !== null;
        var i = a !== null && a.memoizedState !== null, o = Lt, g = rl;
        if (Lt = o || e, rl = g || i, xl(t, l), rl = g, Lt = o, Kl(l), u & 8192)
          l: for (t = l.stateNode, t._visibility = e ? t._visibility & -2 : t._visibility | 1, e && (a === null || i || Lt || rl || Qa(l)), a = null, t = l; ; ) {
            if (t.tag === 5 || t.tag === 26) {
              if (a === null) {
                i = a = t;
                try {
                  if (n = i.stateNode, e)
                    f = n.style, typeof f.setProperty == "function" ? f.setProperty("display", "none", "important") : f.display = "none";
                  else {
                    c = i.stateNode;
                    var z = i.memoizedProps.style, h = z != null && z.hasOwnProperty("display") ? z.display : null;
                    c.style.display = h == null || typeof h == "boolean" ? "" : ("" + h).trim();
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
                  var S = i.stateNode;
                  e ? Kv(S, !0) : Kv(i.stateNode, !1);
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
        u & 4 && (u = l.updateQueue, u !== null && (a = u.retryQueue, a !== null && (u.retryQueue = null, on(l, a))));
        break;
      case 19:
        xl(t, l), Kl(l), u & 4 && (u = l.updateQueue, u !== null && (l.updateQueue = null, on(l, u)));
        break;
      case 30:
        break;
      case 21:
        break;
      default:
        xl(t, l), Kl(l);
    }
  }
  function Kl(l) {
    var t = l.flags;
    if (t & 2) {
      try {
        for (var a, u = l.return; u !== null; ) {
          if (ws(u)) {
            a = u;
            break;
          }
          u = u.return;
        }
        if (a == null) throw Error(m(160));
        switch (a.tag) {
          case 27:
            var e = a.stateNode, n = Ac(l);
            dn(l, n, e);
            break;
          case 5:
            var f = a.stateNode;
            a.flags & 32 && ($a(f, ""), a.flags &= -33);
            var c = Ac(l);
            dn(l, c, f);
            break;
          case 3:
          case 4:
            var i = a.stateNode.containerInfo, o = Ac(l);
            rc(
              l,
              o,
              i
            );
            break;
          default:
            throw Error(m(161));
        }
      } catch (g) {
        ul(l, l.return, g);
      }
      l.flags &= -3;
    }
    t & 4096 && (l.flags &= -4097);
  }
  function av(l) {
    if (l.subtreeFlags & 1024)
      for (l = l.child; l !== null; ) {
        var t = l;
        av(t), t.tag === 5 && t.flags & 1024 && t.stateNode.reset(), l = l.sibling;
      }
  }
  function xt(l, t) {
    if (t.subtreeFlags & 8772)
      for (t = t.child; t !== null; )
        ks(l, t.alternate, t), t = t.sibling;
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
          Mt(t, t.return);
          var a = t.stateNode;
          typeof a.componentWillUnmount == "function" && Ks(
            t,
            t.return,
            a
          ), Qa(t);
          break;
        case 27:
          de(t.stateNode);
        case 26:
        case 5:
          Mt(t, t.return), Qa(t);
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
  function Kt(l, t, a) {
    for (a = a && (t.subtreeFlags & 8772) !== 0, t = t.child; t !== null; ) {
      var u = t.alternate, e = l, n = t, f = n.flags;
      switch (n.tag) {
        case 0:
        case 11:
        case 15:
          Kt(
            e,
            n,
            a
          ), ue(4, n);
          break;
        case 1:
          if (Kt(
            e,
            n,
            a
          ), u = n, e = u.stateNode, typeof e.componentDidMount == "function")
            try {
              e.componentDidMount();
            } catch (o) {
              ul(u, u.return, o);
            }
          if (u = n, e = u.updateQueue, e !== null) {
            var c = u.stateNode;
            try {
              var i = e.shared.hiddenCallbacks;
              if (i !== null)
                for (e.shared.hiddenCallbacks = null, e = 0; e < i.length; e++)
                  R0(i[e], c);
            } catch (o) {
              ul(u, u.return, o);
            }
          }
          a && f & 64 && xs(n), ee(n, n.return);
          break;
        case 27:
          Ws(n);
        case 26:
        case 5:
          Kt(
            e,
            n,
            a
          ), a && u === null && f & 4 && Js(n), ee(n, n.return);
          break;
        case 12:
          Kt(
            e,
            n,
            a
          );
          break;
        case 31:
          Kt(
            e,
            n,
            a
          ), a && f & 4 && Ps(e, n);
          break;
        case 13:
          Kt(
            e,
            n,
            a
          ), a && f & 4 && lv(e, n);
          break;
        case 22:
          n.memoizedState === null && Kt(
            e,
            n,
            a
          ), ee(n, n.return);
          break;
        case 30:
          break;
        default:
          Kt(
            e,
            n,
            a
          );
      }
      t = t.sibling;
    }
  }
  function Oc(l, t) {
    var a = null;
    l !== null && l.memoizedState !== null && l.memoizedState.cachePool !== null && (a = l.memoizedState.cachePool.pool), l = null, t.memoizedState !== null && t.memoizedState.cachePool !== null && (l = t.memoizedState.cachePool.pool), l !== a && (l != null && l.refCount++, a != null && xu(a));
  }
  function Mc(l, t) {
    l = null, t.alternate !== null && (l = t.alternate.memoizedState.cache), t = t.memoizedState.cache, t !== l && (t.refCount++, l != null && xu(l));
  }
  function zt(l, t, a, u) {
    if (t.subtreeFlags & 10256)
      for (t = t.child; t !== null; )
        uv(
          l,
          t,
          a,
          u
        ), t = t.sibling;
  }
  function uv(l, t, a, u) {
    var e = t.flags;
    switch (t.tag) {
      case 0:
      case 11:
      case 15:
        zt(
          l,
          t,
          a,
          u
        ), e & 2048 && ue(9, t);
        break;
      case 1:
        zt(
          l,
          t,
          a,
          u
        );
        break;
      case 3:
        zt(
          l,
          t,
          a,
          u
        ), e & 2048 && (l = null, t.alternate !== null && (l = t.alternate.memoizedState.cache), t = t.memoizedState.cache, t !== l && (t.refCount++, l != null && xu(l)));
        break;
      case 12:
        if (e & 2048) {
          zt(
            l,
            t,
            a,
            u
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
          zt(
            l,
            t,
            a,
            u
          );
        break;
      case 31:
        zt(
          l,
          t,
          a,
          u
        );
        break;
      case 13:
        zt(
          l,
          t,
          a,
          u
        );
        break;
      case 23:
        break;
      case 22:
        n = t.stateNode, f = t.alternate, t.memoizedState !== null ? n._visibility & 2 ? zt(
          l,
          t,
          a,
          u
        ) : ne(l, t) : n._visibility & 2 ? zt(
          l,
          t,
          a,
          u
        ) : (n._visibility |= 2, hu(
          l,
          t,
          a,
          u,
          (t.subtreeFlags & 10256) !== 0 || !1
        )), e & 2048 && Oc(f, t);
        break;
      case 24:
        zt(
          l,
          t,
          a,
          u
        ), e & 2048 && Mc(t.alternate, t);
        break;
      default:
        zt(
          l,
          t,
          a,
          u
        );
    }
  }
  function hu(l, t, a, u, e) {
    for (e = e && ((t.subtreeFlags & 10256) !== 0 || !1), t = t.child; t !== null; ) {
      var n = l, f = t, c = a, i = u, o = f.flags;
      switch (f.tag) {
        case 0:
        case 11:
        case 15:
          hu(
            n,
            f,
            c,
            i,
            e
          ), ue(8, f);
          break;
        case 23:
          break;
        case 22:
          var g = f.stateNode;
          f.memoizedState !== null ? g._visibility & 2 ? hu(
            n,
            f,
            c,
            i,
            e
          ) : ne(
            n,
            f
          ) : (g._visibility |= 2, hu(
            n,
            f,
            c,
            i,
            e
          )), e && o & 2048 && Oc(
            f.alternate,
            f
          );
          break;
        case 24:
          hu(
            n,
            f,
            c,
            i,
            e
          ), e && o & 2048 && Mc(f.alternate, f);
          break;
        default:
          hu(
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
        var a = l, u = t, e = u.flags;
        switch (u.tag) {
          case 22:
            ne(a, u), e & 2048 && Oc(
              u.alternate,
              u
            );
            break;
          case 24:
            ne(a, u), e & 2048 && Mc(u.alternate, u);
            break;
          default:
            ne(a, u);
        }
        t = t.sibling;
      }
  }
  var fe = 8192;
  function Su(l, t, a) {
    if (l.subtreeFlags & fe)
      for (l = l.child; l !== null; )
        ev(
          l,
          t,
          a
        ), l = l.sibling;
  }
  function ev(l, t, a) {
    switch (l.tag) {
      case 26:
        Su(
          l,
          t,
          a
        ), l.flags & fe && l.memoizedState !== null && jd(
          a,
          Tt,
          l.memoizedState,
          l.memoizedProps
        );
        break;
      case 5:
        Su(
          l,
          t,
          a
        );
        break;
      case 3:
      case 4:
        var u = Tt;
        Tt = Un(l.stateNode.containerInfo), Su(
          l,
          t,
          a
        ), Tt = u;
        break;
      case 22:
        l.memoizedState === null && (u = l.alternate, u !== null && u.memoizedState !== null ? (u = fe, fe = 16777216, Su(
          l,
          t,
          a
        ), fe = u) : Su(
          l,
          t,
          a
        ));
        break;
      default:
        Su(
          l,
          t,
          a
        );
    }
  }
  function nv(l) {
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
        for (var a = 0; a < t.length; a++) {
          var u = t[a];
          Ml = u, cv(
            u,
            l
          );
        }
      nv(l);
    }
    if (l.subtreeFlags & 10256)
      for (l = l.child; l !== null; )
        fv(l), l = l.sibling;
  }
  function fv(l) {
    switch (l.tag) {
      case 0:
      case 11:
      case 15:
        ce(l), l.flags & 2048 && ia(9, l, l.return);
        break;
      case 3:
        ce(l);
        break;
      case 12:
        ce(l);
        break;
      case 22:
        var t = l.stateNode;
        l.memoizedState !== null && t._visibility & 2 && (l.return === null || l.return.tag !== 13) ? (t._visibility &= -3, hn(l)) : ce(l);
        break;
      default:
        ce(l);
    }
  }
  function hn(l) {
    var t = l.deletions;
    if ((l.flags & 16) !== 0) {
      if (t !== null)
        for (var a = 0; a < t.length; a++) {
          var u = t[a];
          Ml = u, cv(
            u,
            l
          );
        }
      nv(l);
    }
    for (l = l.child; l !== null; ) {
      switch (t = l, t.tag) {
        case 0:
        case 11:
        case 15:
          ia(8, t, t.return), hn(t);
          break;
        case 22:
          a = t.stateNode, a._visibility & 2 && (a._visibility &= -3, hn(t));
          break;
        default:
          hn(t);
      }
      l = l.sibling;
    }
  }
  function cv(l, t) {
    for (; Ml !== null; ) {
      var a = Ml;
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
          xu(a.memoizedState.cache);
      }
      if (u = a.child, u !== null) u.return = a, Ml = u;
      else
        l: for (a = l; Ml !== null; ) {
          u = Ml;
          var e = u.sibling, n = u.return;
          if (Fs(u), u === a) {
            Ml = null;
            break l;
          }
          if (e !== null) {
            e.return = n, Ml = e;
            break l;
          }
          Ml = n;
        }
    }
  }
  var td = {
    getCacheForType: function(l) {
      var t = pl(zl), a = t.data.get(l);
      return a === void 0 && (a = l(), t.data.set(l, a)), a;
    },
    cacheSignal: function() {
      return pl(zl).controller.signal;
    }
  }, ad = typeof WeakMap == "function" ? WeakMap : Map, ll = 0, sl = null, x = null, J = 0, al = 0, ut = null, sa = !1, gu = !1, Dc = !1, Jt = 0, hl = 0, va = 0, ja = 0, Uc = 0, et = 0, bu = 0, ie = null, Jl = null, pc = !1, Sn = 0, iv = 0, gn = 1 / 0, bn = null, ya = null, _l = 0, ma = null, Tu = null, wt = 0, Cc = 0, Nc = null, sv = null, se = 0, Hc = null;
  function nt() {
    return (ll & 2) !== 0 && J !== 0 ? J & -J : b.T !== null ? Xc() : Oi();
  }
  function vv() {
    if (et === 0)
      if ((J & 536870912) === 0 || k) {
        var l = Oe;
        Oe <<= 1, (Oe & 3932160) === 0 && (Oe = 262144), et = l;
      } else et = 536870912;
    return l = tt.current, l !== null && (l.flags |= 32), et;
  }
  function wl(l, t, a) {
    (l === sl && (al === 2 || al === 9) || l.cancelPendingCommit !== null) && (zu(l, 0), da(
      l,
      J,
      et,
      !1
    )), Cu(l, a), ((ll & 2) === 0 || l !== sl) && (l === sl && ((ll & 2) === 0 && (ja |= a), hl === 4 && da(
      l,
      J,
      et,
      !1
    )), Dt(l));
  }
  function yv(l, t, a) {
    if ((ll & 6) !== 0) throw Error(m(327));
    var u = !a && (t & 127) === 0 && (t & l.expiredLanes) === 0 || pu(l, t), e = u ? nd(l, t) : Bc(l, t, !0), n = u;
    do {
      if (e === 0) {
        gu && !u && da(l, t, 0, !1);
        break;
      } else {
        if (a = l.current.alternate, n && !ud(a)) {
          e = Bc(l, t, !1), n = !1;
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
              if (i && (zu(c, f).flags |= 256), f = Bc(
                c,
                f,
                !1
              ), f !== 2) {
                if (Dc && !i) {
                  c.errorRecoveryDisabledLanes |= n, ja |= n, e = 4;
                  break l;
                }
                n = Jl, Jl = e, n !== null && (Jl === null ? Jl = n : Jl.push.apply(
                  Jl,
                  n
                ));
              }
              e = f;
            }
            if (n = !1, e !== 2) continue;
          }
        }
        if (e === 1) {
          zu(l, 0), da(l, t, 0, !0);
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
              da(
                u,
                t,
                et,
                !sa
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
          if ((t & 62914560) === t && (e = Sn + 300 - kl(), 10 < e)) {
            if (da(
              u,
              t,
              et,
              !sa
            ), De(u, 0, !0) !== 0) break l;
            wt = t, u.timeoutHandle = Lv(
              mv.bind(
                null,
                u,
                a,
                Jl,
                bn,
                pc,
                t,
                et,
                ja,
                bu,
                sa,
                n,
                "Throttled",
                -0,
                0
              ),
              e
            );
            break l;
          }
          mv(
            u,
            a,
            Jl,
            bn,
            pc,
            t,
            et,
            ja,
            bu,
            sa,
            n,
            null,
            -0,
            0
          );
        }
      }
      break;
    } while (!0);
    Dt(l);
  }
  function mv(l, t, a, u, e, n, f, c, i, o, g, z, h, S) {
    if (l.timeoutHandle = -1, z = t.subtreeFlags, z & 8192 || (z & 16785408) === 16785408) {
      z = {
        stylesheets: null,
        count: 0,
        imgCount: 0,
        imgBytes: 0,
        suspenseyImages: [],
        waitingForImages: !0,
        waitingForViewTransition: !1,
        unsuspend: Ht
      }, ev(
        t,
        n,
        z
      );
      var U = (n & 62914560) === n ? Sn - kl() : (n & 4194048) === n ? iv - kl() : 0;
      if (U = Zd(
        z,
        U
      ), U !== null) {
        wt = n, l.cancelPendingCommit = U(
          zv.bind(
            null,
            l,
            t,
            n,
            a,
            u,
            e,
            f,
            c,
            i,
            g,
            z,
            null,
            h,
            S
          )
        ), da(l, n, f, !o);
        return;
      }
    }
    zv(
      l,
      t,
      n,
      a,
      u,
      e,
      f,
      c,
      i
    );
  }
  function ud(l) {
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
  function da(l, t, a, u) {
    t &= ~Uc, t &= ~ja, l.suspendedLanes |= t, l.pingedLanes &= ~t, u && (l.warmLanes |= t), u = l.expirationTimes;
    for (var e = t; 0 < e; ) {
      var n = 31 - Il(e), f = 1 << n;
      u[n] = -1, e &= ~f;
    }
    a !== 0 && Ai(l, a, t);
  }
  function Tn() {
    return (ll & 6) === 0 ? (ve(0), !1) : !0;
  }
  function Rc() {
    if (x !== null) {
      if (al === 0)
        var l = x.return;
      else
        l = x, Yt = Na = null, $f(l), vu = null, Ju = 0, l = x;
      for (; l !== null; )
        Vs(l.alternate, l), l = l.return;
      x = null;
    }
  }
  function zu(l, t) {
    var a = l.timeoutHandle;
    a !== -1 && (l.timeoutHandle = -1, rd(a)), a = l.cancelPendingCommit, a !== null && (l.cancelPendingCommit = null, a()), wt = 0, Rc(), sl = l, x = a = Bt(l.current, null), J = t, al = 0, ut = null, sa = !1, gu = pu(l, t), Dc = !1, bu = et = Uc = ja = va = hl = 0, Jl = ie = null, pc = !1, (t & 8) !== 0 && (t |= t & 32);
    var u = l.entangledLanes;
    if (u !== 0)
      for (l = l.entanglements, u &= t; 0 < u; ) {
        var e = 31 - Il(u), n = 1 << e;
        t |= l[e], u &= ~n;
      }
    return Jt = t, Qe(), a;
  }
  function dv(l, t) {
    Q = null, b.H = le, t === su || t === we ? (t = p0(), al = 3) : t === Gf ? (t = p0(), al = 4) : al = t === yc ? 8 : t !== null && typeof t == "object" && typeof t.then == "function" ? 6 : 1, ut = t, x === null && (hl = 1, cn(
      l,
      vt(t, l.current)
    ));
  }
  function ov() {
    var l = tt.current;
    return l === null ? !0 : (J & 4194048) === J ? ot === null : (J & 62914560) === J || (J & 536870912) !== 0 ? l === ot : !1;
  }
  function hv() {
    var l = b.H;
    return b.H = le, l === null ? le : l;
  }
  function Sv() {
    var l = b.A;
    return b.A = td, l;
  }
  function zn() {
    hl = 4, sa || (J & 4194048) !== J && tt.current !== null || (gu = !0), (va & 134217727) === 0 && (ja & 134217727) === 0 || sl === null || da(
      sl,
      J,
      et,
      !1
    );
  }
  function Bc(l, t, a) {
    var u = ll;
    ll |= 2;
    var e = hv(), n = Sv();
    (sl !== l || J !== t) && (bn = null, zu(l, t)), t = !1;
    var f = hl;
    l: do
      try {
        if (al !== 0 && x !== null) {
          var c = x, i = ut;
          switch (al) {
            case 8:
              Rc(), f = 6;
              break l;
            case 3:
            case 2:
            case 9:
            case 6:
              tt.current === null && (t = !0);
              var o = al;
              if (al = 0, ut = null, Eu(l, c, i, o), a && gu) {
                f = 0;
                break l;
              }
              break;
            default:
              o = al, al = 0, ut = null, Eu(l, c, i, o);
          }
        }
        ed(), f = hl;
        break;
      } catch (g) {
        dv(l, g);
      }
    while (!0);
    return t && l.shellSuspendCounter++, Yt = Na = null, ll = u, b.H = e, b.A = n, x === null && (sl = null, J = 0, Qe()), f;
  }
  function ed() {
    for (; x !== null; ) gv(x);
  }
  function nd(l, t) {
    var a = ll;
    ll |= 2;
    var u = hv(), e = Sv();
    sl !== l || J !== t ? (bn = null, gn = kl() + 500, zu(l, t)) : gu = pu(
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
              al = 0, ut = null, Eu(l, t, n, 1);
              break;
            case 2:
            case 9:
              if (D0(n)) {
                al = 0, ut = null, bv(t);
                break;
              }
              t = function() {
                al !== 2 && al !== 9 || sl !== l || (al = 7), Dt(l);
              }, n.then(t, t);
              break l;
            case 3:
              al = 7;
              break l;
            case 4:
              al = 5;
              break l;
            case 7:
              D0(n) ? (al = 0, ut = null, bv(t)) : (al = 0, ut = null, Eu(l, t, n, 7));
              break;
            case 5:
              var f = null;
              switch (x.tag) {
                case 26:
                  f = x.memoizedState;
                case 5:
                case 27:
                  var c = x;
                  if (f ? uy(f) : c.stateNode.complete) {
                    al = 0, ut = null;
                    var i = c.sibling;
                    if (i !== null) x = i;
                    else {
                      var o = c.return;
                      o !== null ? (x = o, En(o)) : x = null;
                    }
                    break t;
                  }
              }
              al = 0, ut = null, Eu(l, t, n, 5);
              break;
            case 6:
              al = 0, ut = null, Eu(l, t, n, 6);
              break;
            case 8:
              Rc(), hl = 6;
              break l;
            default:
              throw Error(m(462));
          }
        }
        fd();
        break;
      } catch (g) {
        dv(l, g);
      }
    while (!0);
    return Yt = Na = null, b.H = u, b.A = e, ll = a, x !== null ? 0 : (sl = null, J = 0, Qe(), hl);
  }
  function fd() {
    for (; x !== null && !py(); )
      gv(x);
  }
  function gv(l) {
    var t = Zs(l.alternate, l, Jt);
    l.memoizedProps = l.pendingProps, t === null ? En(l) : x = t;
  }
  function bv(l) {
    var t = l, a = t.alternate;
    switch (t.tag) {
      case 15:
      case 0:
        t = qs(
          a,
          t,
          t.pendingProps,
          t.type,
          void 0,
          J
        );
        break;
      case 11:
        t = qs(
          a,
          t,
          t.pendingProps,
          t.type.render,
          t.ref,
          J
        );
        break;
      case 5:
        $f(t);
      default:
        Vs(a, t), t = x = S0(t, Jt), t = Zs(a, t, Jt);
    }
    l.memoizedProps = l.pendingProps, t === null ? En(l) : x = t;
  }
  function Eu(l, t, a, u) {
    Yt = Na = null, $f(t), vu = null, Ju = 0;
    var e = t.return;
    try {
      if (Wm(
        l,
        e,
        t,
        a,
        J
      )) {
        hl = 1, cn(
          l,
          vt(a, l.current)
        ), x = null;
        return;
      }
    } catch (n) {
      if (e !== null) throw x = e, n;
      hl = 1, cn(
        l,
        vt(a, l.current)
      ), x = null;
      return;
    }
    t.flags & 32768 ? (k || u === 1 ? l = !0 : gu || (J & 536870912) !== 0 ? l = !1 : (sa = l = !0, (u === 2 || u === 9 || u === 3 || u === 6) && (u = tt.current, u !== null && u.tag === 13 && (u.flags |= 16384))), Tv(t, l)) : En(t);
  }
  function En(l) {
    var t = l;
    do {
      if ((t.flags & 32768) !== 0) {
        Tv(
          t,
          sa
        );
        return;
      }
      l = t.return;
      var a = Fm(
        t.alternate,
        t,
        Jt
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
  function Tv(l, t) {
    do {
      var a = Im(l.alternate, l);
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
  function zv(l, t, a, u, e, n, f, c, i) {
    l.cancelPendingCommit = null;
    do
      An();
    while (_l !== 0);
    if ((ll & 6) !== 0) throw Error(m(327));
    if (t !== null) {
      if (t === l.current) throw Error(m(177));
      if (n = t.lanes | t.childLanes, n |= Af, Qy(
        l,
        a,
        n,
        f,
        c,
        i
      ), l === sl && (x = sl = null, J = 0), Tu = t, ma = l, wt = a, Cc = n, Nc = e, sv = u, (t.subtreeFlags & 10256) !== 0 || (t.flags & 10256) !== 0 ? (l.callbackNode = null, l.callbackPriority = 0, vd(re, function() {
        return Ov(), null;
      })) : (l.callbackNode = null, l.callbackPriority = 0), u = (t.flags & 13878) !== 0, (t.subtreeFlags & 13878) !== 0 || u) {
        u = b.T, b.T = null, e = _.p, _.p = 2, f = ll, ll |= 4;
        try {
          Pm(l, t, a);
        } finally {
          ll = f, _.p = e, b.T = u;
        }
      }
      _l = 1, Ev(), Av(), rv();
    }
  }
  function Ev() {
    if (_l === 1) {
      _l = 0;
      var l = ma, t = Tu, a = (t.flags & 13878) !== 0;
      if ((t.subtreeFlags & 13878) !== 0 || a) {
        a = b.T, b.T = null;
        var u = _.p;
        _.p = 2;
        var e = ll;
        ll |= 4;
        try {
          tv(t, l);
          var n = Jc, f = c0(l.containerInfo), c = n.focusedElem, i = n.selectionRange;
          if (f !== c && c && c.ownerDocument && f0(
            c.ownerDocument.documentElement,
            c
          )) {
            if (i !== null && gf(c)) {
              var o = i.start, g = i.end;
              if (g === void 0 && (g = o), "selectionStart" in c)
                c.selectionStart = o, c.selectionEnd = Math.min(
                  g,
                  c.value.length
                );
              else {
                var z = c.ownerDocument || document, h = z && z.defaultView || window;
                if (h.getSelection) {
                  var S = h.getSelection(), U = c.textContent.length, R = Math.min(i.start, U), cl = i.end === void 0 ? R : Math.min(i.end, U);
                  !S.extend && R > cl && (f = cl, cl = R, R = f);
                  var y = n0(
                    c,
                    R
                  ), s = n0(
                    c,
                    cl
                  );
                  if (y && s && (S.rangeCount !== 1 || S.anchorNode !== y.node || S.anchorOffset !== y.offset || S.focusNode !== s.node || S.focusOffset !== s.offset)) {
                    var d = z.createRange();
                    d.setStart(y.node, y.offset), S.removeAllRanges(), R > cl ? (S.addRange(d), S.extend(s.node, s.offset)) : (d.setEnd(s.node, s.offset), S.addRange(d));
                  }
                }
              }
            }
            for (z = [], S = c; S = S.parentNode; )
              S.nodeType === 1 && z.push({
                element: S,
                left: S.scrollLeft,
                top: S.scrollTop
              });
            for (typeof c.focus == "function" && c.focus(), c = 0; c < z.length; c++) {
              var T = z[c];
              T.element.scrollLeft = T.left, T.element.scrollTop = T.top;
            }
          }
          Bn = !!Kc, Jc = Kc = null;
        } finally {
          ll = e, _.p = u, b.T = a;
        }
      }
      l.current = t, _l = 2;
    }
  }
  function Av() {
    if (_l === 2) {
      _l = 0;
      var l = ma, t = Tu, a = (t.flags & 8772) !== 0;
      if ((t.subtreeFlags & 8772) !== 0 || a) {
        a = b.T, b.T = null;
        var u = _.p;
        _.p = 2;
        var e = ll;
        ll |= 4;
        try {
          ks(l, t.alternate, t);
        } finally {
          ll = e, _.p = u, b.T = a;
        }
      }
      _l = 3;
    }
  }
  function rv() {
    if (_l === 4 || _l === 3) {
      _l = 0, Cy();
      var l = ma, t = Tu, a = wt, u = sv;
      (t.subtreeFlags & 10256) !== 0 || (t.flags & 10256) !== 0 ? _l = 5 : (_l = 0, Tu = ma = null, _v(l, l.pendingLanes));
      var e = l.pendingLanes;
      if (e === 0 && (ya = null), Fn(a), t = t.stateNode, Fl && typeof Fl.onCommitFiberRoot == "function")
        try {
          Fl.onCommitFiberRoot(
            Uu,
            t,
            void 0,
            (t.current.flags & 128) === 128
          );
        } catch {
        }
      if (u !== null) {
        t = b.T, e = _.p, _.p = 2, b.T = null;
        try {
          for (var n = l.onRecoverableError, f = 0; f < u.length; f++) {
            var c = u[f];
            n(c.value, {
              componentStack: c.stack
            });
          }
        } finally {
          b.T = t, _.p = e;
        }
      }
      (wt & 3) !== 0 && An(), Dt(l), e = l.pendingLanes, (a & 261930) !== 0 && (e & 42) !== 0 ? l === Hc ? se++ : (se = 0, Hc = l) : se = 0, ve(0);
    }
  }
  function _v(l, t) {
    (l.pooledCacheLanes &= t) === 0 && (t = l.pooledCache, t != null && (l.pooledCache = null, xu(t)));
  }
  function An() {
    return Ev(), Av(), rv(), Ov();
  }
  function Ov() {
    if (_l !== 5) return !1;
    var l = ma, t = Cc;
    Cc = 0;
    var a = Fn(wt), u = b.T, e = _.p;
    try {
      _.p = 32 > a ? 32 : a, b.T = null, a = Nc, Nc = null;
      var n = ma, f = wt;
      if (_l = 0, Tu = ma = null, wt = 0, (ll & 6) !== 0) throw Error(m(331));
      var c = ll;
      if (ll |= 4, fv(n.current), uv(
        n,
        n.current,
        f,
        a
      ), ll = c, ve(0, !1), Fl && typeof Fl.onPostCommitFiberRoot == "function")
        try {
          Fl.onPostCommitFiberRoot(Uu, n);
        } catch {
        }
      return !0;
    } finally {
      _.p = e, b.T = u, _v(l, t);
    }
  }
  function Mv(l, t, a) {
    t = vt(a, t), t = vc(l.stateNode, t, 2), l = na(l, t, 2), l !== null && (Cu(l, 2), Dt(l));
  }
  function ul(l, t, a) {
    if (l.tag === 3)
      Mv(l, l, a);
    else
      for (; t !== null; ) {
        if (t.tag === 3) {
          Mv(
            t,
            l,
            a
          );
          break;
        } else if (t.tag === 1) {
          var u = t.stateNode;
          if (typeof t.type.getDerivedStateFromError == "function" || typeof u.componentDidCatch == "function" && (ya === null || !ya.has(u))) {
            l = vt(a, l), a = Ds(2), u = na(t, a, 2), u !== null && (Us(
              a,
              u,
              t,
              l
            ), Cu(u, 2), Dt(u));
            break;
          }
        }
        t = t.return;
      }
  }
  function qc(l, t, a) {
    var u = l.pingCache;
    if (u === null) {
      u = l.pingCache = new ad();
      var e = /* @__PURE__ */ new Set();
      u.set(t, e);
    } else
      e = u.get(t), e === void 0 && (e = /* @__PURE__ */ new Set(), u.set(t, e));
    e.has(a) || (Dc = !0, e.add(a), l = cd.bind(null, l, t, a), t.then(l, l));
  }
  function cd(l, t, a) {
    var u = l.pingCache;
    u !== null && u.delete(t), l.pingedLanes |= l.suspendedLanes & a, l.warmLanes &= ~a, sl === l && (J & a) === a && (hl === 4 || hl === 3 && (J & 62914560) === J && 300 > kl() - Sn ? (ll & 2) === 0 && zu(l, 0) : Uc |= a, bu === J && (bu = 0)), Dt(l);
  }
  function Dv(l, t) {
    t === 0 && (t = Ei()), l = Ua(l, t), l !== null && (Cu(l, t), Dt(l));
  }
  function id(l) {
    var t = l.memoizedState, a = 0;
    t !== null && (a = t.retryLane), Dv(l, a);
  }
  function sd(l, t) {
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
    u !== null && u.delete(t), Dv(l, a);
  }
  function vd(l, t) {
    return wn(l, t);
  }
  var rn = null, Au = null, Yc = !1, _n = !1, Gc = !1, oa = 0;
  function Dt(l) {
    l !== Au && l.next === null && (Au === null ? rn = Au = l : Au = Au.next = l), _n = !0, Yc || (Yc = !0, md());
  }
  function ve(l, t) {
    if (!Gc && _n) {
      Gc = !0;
      do
        for (var a = !1, u = rn; u !== null; ) {
          if (l !== 0) {
            var e = u.pendingLanes;
            if (e === 0) var n = 0;
            else {
              var f = u.suspendedLanes, c = u.pingedLanes;
              n = (1 << 31 - Il(42 | l) + 1) - 1, n &= e & ~(f & ~c), n = n & 201326741 ? n & 201326741 | 1 : n ? n | 2 : 0;
            }
            n !== 0 && (a = !0, Nv(u, n));
          } else
            n = J, n = De(
              u,
              u === sl ? n : 0,
              u.cancelPendingCommit !== null || u.timeoutHandle !== -1
            ), (n & 3) === 0 || pu(u, n) || (a = !0, Nv(u, n));
          u = u.next;
        }
      while (a);
      Gc = !1;
    }
  }
  function yd() {
    Uv();
  }
  function Uv() {
    _n = Yc = !1;
    var l = 0;
    oa !== 0 && Ad() && (l = oa);
    for (var t = kl(), a = null, u = rn; u !== null; ) {
      var e = u.next, n = pv(u, t);
      n === 0 ? (u.next = null, a === null ? rn = e : a.next = e, e === null && (Au = a)) : (a = u, (l !== 0 || (n & 3) !== 0) && (_n = !0)), u = e;
    }
    _l !== 0 && _l !== 5 || ve(l), oa !== 0 && (oa = 0);
  }
  function pv(l, t) {
    for (var a = l.suspendedLanes, u = l.pingedLanes, e = l.expirationTimes, n = l.pendingLanes & -62914561; 0 < n; ) {
      var f = 31 - Il(n), c = 1 << f, i = e[f];
      i === -1 ? ((c & a) === 0 || (c & u) !== 0) && (e[f] = Xy(c, t)) : i <= t && (l.expiredLanes |= c), n &= ~c;
    }
    if (t = sl, a = J, a = De(
      l,
      l === t ? a : 0,
      l.cancelPendingCommit !== null || l.timeoutHandle !== -1
    ), u = l.callbackNode, a === 0 || l === t && (al === 2 || al === 9) || l.cancelPendingCommit !== null)
      return u !== null && u !== null && Wn(u), l.callbackNode = null, l.callbackPriority = 0;
    if ((a & 3) === 0 || pu(l, a)) {
      if (t = a & -a, t === l.callbackPriority) return t;
      switch (u !== null && Wn(u), Fn(a)) {
        case 2:
        case 8:
          a = Ti;
          break;
        case 32:
          a = re;
          break;
        case 268435456:
          a = zi;
          break;
        default:
          a = re;
      }
      return u = Cv.bind(null, l), a = wn(a, u), l.callbackPriority = t, l.callbackNode = a, t;
    }
    return u !== null && u !== null && Wn(u), l.callbackPriority = 2, l.callbackNode = null, 2;
  }
  function Cv(l, t) {
    if (_l !== 0 && _l !== 5)
      return l.callbackNode = null, l.callbackPriority = 0, null;
    var a = l.callbackNode;
    if (An() && l.callbackNode !== a)
      return null;
    var u = J;
    return u = De(
      l,
      l === sl ? u : 0,
      l.cancelPendingCommit !== null || l.timeoutHandle !== -1
    ), u === 0 ? null : (yv(l, u, t), pv(l, kl()), l.callbackNode != null && l.callbackNode === a ? Cv.bind(null, l) : null);
  }
  function Nv(l, t) {
    if (An()) return null;
    yv(l, t, !0);
  }
  function md() {
    _d(function() {
      (ll & 6) !== 0 ? wn(
        bi,
        yd
      ) : Uv();
    });
  }
  function Xc() {
    if (oa === 0) {
      var l = cu;
      l === 0 && (l = _e, _e <<= 1, (_e & 261888) === 0 && (_e = 256)), oa = l;
    }
    return oa;
  }
  function Hv(l) {
    return l == null || typeof l == "symbol" || typeof l == "boolean" ? null : typeof l == "function" ? l : Ne("" + l);
  }
  function Rv(l, t) {
    var a = t.ownerDocument.createElement("input");
    return a.name = t.name, a.value = t.value, l.id && a.setAttribute("form", l.id), t.parentNode.insertBefore(a, t), l = new FormData(l), a.parentNode.removeChild(a), l;
  }
  function dd(l, t, a, u, e) {
    if (t === "submit" && a && a.stateNode === e) {
      var n = Hv(
        (e[Zl] || null).action
      ), f = u.submitter;
      f && (t = (t = f[Zl] || null) ? Hv(t.formAction) : f.getAttribute("formAction"), t !== null && (n = t, f = null));
      var c = new qe(
        "action",
        "action",
        null,
        u,
        e
      );
      l.push({
        event: c,
        listeners: [
          {
            instance: null,
            listener: function() {
              if (u.defaultPrevented) {
                if (oa !== 0) {
                  var i = f ? Rv(e, f) : new FormData(e);
                  ec(
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
                typeof n == "function" && (c.preventDefault(), i = f ? Rv(e, f) : new FormData(e), ec(
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
  for (var Qc = 0; Qc < Ef.length; Qc++) {
    var jc = Ef[Qc], od = jc.toLowerCase(), hd = jc[0].toUpperCase() + jc.slice(1);
    bt(
      od,
      "on" + hd
    );
  }
  bt(v0, "onAnimationEnd"), bt(y0, "onAnimationIteration"), bt(m0, "onAnimationStart"), bt("dblclick", "onDoubleClick"), bt("focusin", "onFocus"), bt("focusout", "onBlur"), bt(Nm, "onTransitionRun"), bt(Hm, "onTransitionStart"), bt(Rm, "onTransitionCancel"), bt(d0, "onTransitionEnd"), wa("onMouseEnter", ["mouseout", "mouseover"]), wa("onMouseLeave", ["mouseout", "mouseover"]), wa("onPointerEnter", ["pointerout", "pointerover"]), wa("onPointerLeave", ["pointerout", "pointerover"]), _a(
    "onChange",
    "change click focusin focusout input keydown keyup selectionchange".split(" ")
  ), _a(
    "onSelect",
    "focusout contextmenu dragend focusin keydown keyup mousedown mouseup selectionchange".split(
      " "
    )
  ), _a("onBeforeInput", [
    "compositionend",
    "keypress",
    "textInput",
    "paste"
  ]), _a(
    "onCompositionEnd",
    "compositionend focusout keydown keypress keyup mousedown".split(" ")
  ), _a(
    "onCompositionStart",
    "compositionstart focusout keydown keypress keyup mousedown".split(" ")
  ), _a(
    "onCompositionUpdate",
    "compositionupdate focusout keydown keypress keyup mousedown".split(" ")
  );
  var ye = "abort canplay canplaythrough durationchange emptied encrypted ended error loadeddata loadedmetadata loadstart pause play playing progress ratechange resize seeked seeking stalled suspend timeupdate volumechange waiting".split(
    " "
  ), Sd = new Set(
    "beforetoggle cancel close invalid load scroll scrollend toggle".split(" ").concat(ye)
  );
  function Bv(l, t) {
    t = (t & 4) !== 0;
    for (var a = 0; a < l.length; a++) {
      var u = l[a], e = u.event;
      u = u.listeners;
      l: {
        var n = void 0;
        if (t)
          for (var f = u.length - 1; 0 <= f; f--) {
            var c = u[f], i = c.instance, o = c.currentTarget;
            if (c = c.listener, i !== n && e.isPropagationStopped())
              break l;
            n = c, e.currentTarget = o;
            try {
              n(e);
            } catch (g) {
              Xe(g);
            }
            e.currentTarget = null, n = i;
          }
        else
          for (f = 0; f < u.length; f++) {
            if (c = u[f], i = c.instance, o = c.currentTarget, c = c.listener, i !== n && e.isPropagationStopped())
              break l;
            n = c, e.currentTarget = o;
            try {
              n(e);
            } catch (g) {
              Xe(g);
            }
            e.currentTarget = null, n = i;
          }
      }
    }
  }
  function K(l, t) {
    var a = t[In];
    a === void 0 && (a = t[In] = /* @__PURE__ */ new Set());
    var u = l + "__bubble";
    a.has(u) || (qv(t, l, 2, !1), a.add(u));
  }
  function Zc(l, t, a) {
    var u = 0;
    t && (u |= 4), qv(
      a,
      l,
      u,
      t
    );
  }
  var On = "_reactListening" + Math.random().toString(36).slice(2);
  function Lc(l) {
    if (!l[On]) {
      l[On] = !0, Ui.forEach(function(a) {
        a !== "selectionchange" && (Sd.has(a) || Zc(a, !1, l), Zc(a, !0, l));
      });
      var t = l.nodeType === 9 ? l : l.ownerDocument;
      t === null || t[On] || (t[On] = !0, Zc("selectionchange", !1, t));
    }
  }
  function qv(l, t, a, u) {
    switch (vy(t)) {
      case 2:
        var e = xd;
        break;
      case 8:
        e = Kd;
        break;
      default:
        e = ui;
    }
    a = e.bind(
      null,
      t,
      a,
      l
    ), e = void 0, !cf || t !== "touchstart" && t !== "touchmove" && t !== "wheel" || (e = !0), u ? e !== void 0 ? l.addEventListener(t, a, {
      capture: !0,
      passive: e
    }) : l.addEventListener(t, a, !0) : e !== void 0 ? l.addEventListener(t, a, {
      passive: e
    }) : l.addEventListener(t, a, !1);
  }
  function Vc(l, t, a, u, e) {
    var n = u;
    if ((t & 1) === 0 && (t & 2) === 0 && u !== null)
      l: for (; ; ) {
        if (u === null) return;
        var f = u.tag;
        if (f === 3 || f === 4) {
          var c = u.stateNode.containerInfo;
          if (c === e) break;
          if (f === 4)
            for (f = u.return; f !== null; ) {
              var i = f.tag;
              if ((i === 3 || i === 4) && f.stateNode.containerInfo === e)
                return;
              f = f.return;
            }
          for (; c !== null; ) {
            if (f = xa(c), f === null) return;
            if (i = f.tag, i === 5 || i === 6 || i === 26 || i === 27) {
              u = n = f;
              continue l;
            }
            c = c.parentNode;
          }
        }
        u = u.return;
      }
    ji(function() {
      var o = n, g = nf(a), z = [];
      l: {
        var h = o0.get(l);
        if (h !== void 0) {
          var S = qe, U = l;
          switch (l) {
            case "keypress":
              if (Re(a) === 0) break l;
            case "keydown":
            case "keyup":
              S = sm;
              break;
            case "focusin":
              U = "focus", S = mf;
              break;
            case "focusout":
              U = "blur", S = mf;
              break;
            case "beforeblur":
            case "afterblur":
              S = mf;
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
              S = Vi;
              break;
            case "drag":
            case "dragend":
            case "dragenter":
            case "dragexit":
            case "dragleave":
            case "dragover":
            case "dragstart":
            case "drop":
              S = Fy;
              break;
            case "touchcancel":
            case "touchend":
            case "touchmove":
            case "touchstart":
              S = mm;
              break;
            case v0:
            case y0:
            case m0:
              S = lm;
              break;
            case d0:
              S = om;
              break;
            case "scroll":
            case "scrollend":
              S = $y;
              break;
            case "wheel":
              S = Sm;
              break;
            case "copy":
            case "cut":
            case "paste":
              S = am;
              break;
            case "gotpointercapture":
            case "lostpointercapture":
            case "pointercancel":
            case "pointerdown":
            case "pointermove":
            case "pointerout":
            case "pointerover":
            case "pointerup":
              S = Ki;
              break;
            case "toggle":
            case "beforetoggle":
              S = bm;
          }
          var R = (t & 4) !== 0, cl = !R && (l === "scroll" || l === "scrollend"), y = R ? h !== null ? h + "Capture" : null : h;
          R = [];
          for (var s = o, d; s !== null; ) {
            var T = s;
            if (d = T.stateNode, T = T.tag, T !== 5 && T !== 26 && T !== 27 || d === null || y === null || (T = Ru(s, y), T != null && R.push(
              me(s, T, d)
            )), cl) break;
            s = s.return;
          }
          0 < R.length && (h = new S(
            h,
            U,
            null,
            a,
            g
          ), z.push({ event: h, listeners: R }));
        }
      }
      if ((t & 7) === 0) {
        l: {
          if (h = l === "mouseover" || l === "pointerover", S = l === "mouseout" || l === "pointerout", h && a !== ef && (U = a.relatedTarget || a.fromElement) && (xa(U) || U[Va]))
            break l;
          if ((S || h) && (h = g.window === g ? g : (h = g.ownerDocument) ? h.defaultView || h.parentWindow : window, S ? (U = a.relatedTarget || a.toElement, S = o, U = U ? xa(U) : null, U !== null && (cl = L(U), R = U.tag, U !== cl || R !== 5 && R !== 27 && R !== 6) && (U = null)) : (S = null, U = o), S !== U)) {
            if (R = Vi, T = "onMouseLeave", y = "onMouseEnter", s = "mouse", (l === "pointerout" || l === "pointerover") && (R = Ki, T = "onPointerLeave", y = "onPointerEnter", s = "pointer"), cl = S == null ? h : Hu(S), d = U == null ? h : Hu(U), h = new R(
              T,
              s + "leave",
              S,
              a,
              g
            ), h.target = cl, h.relatedTarget = d, T = null, xa(g) === o && (R = new R(
              y,
              s + "enter",
              U,
              a,
              g
            ), R.target = d, R.relatedTarget = cl, T = R), cl = T, S && U)
              t: {
                for (R = gd, y = S, s = U, d = 0, T = y; T; T = R(T))
                  d++;
                T = 0;
                for (var H = s; H; H = R(H))
                  T++;
                for (; 0 < d - T; )
                  y = R(y), d--;
                for (; 0 < T - d; )
                  s = R(s), T--;
                for (; d--; ) {
                  if (y === s || s !== null && y === s.alternate) {
                    R = y;
                    break t;
                  }
                  y = R(y), s = R(s);
                }
                R = null;
              }
            else R = null;
            S !== null && Yv(
              z,
              h,
              S,
              R,
              !1
            ), U !== null && cl !== null && Yv(
              z,
              cl,
              U,
              R,
              !0
            );
          }
        }
        l: {
          if (h = o ? Hu(o) : window, S = h.nodeName && h.nodeName.toLowerCase(), S === "select" || S === "input" && h.type === "file")
            var I = Pi;
          else if (Fi(h))
            if (l0)
              I = Um;
            else {
              I = Mm;
              var N = Om;
            }
          else
            S = h.nodeName, !S || S.toLowerCase() !== "input" || h.type !== "checkbox" && h.type !== "radio" ? o && uf(o.elementType) && (I = Pi) : I = Dm;
          if (I && (I = I(l, o))) {
            Ii(
              z,
              I,
              a,
              g
            );
            break l;
          }
          N && N(l, h, o), l === "focusout" && o && h.type === "number" && o.memoizedProps.value != null && af(h, "number", h.value);
        }
        switch (N = o ? Hu(o) : window, l) {
          case "focusin":
            (Fi(N) || N.contentEditable === "true") && (Pa = N, bf = o, Zu = null);
            break;
          case "focusout":
            Zu = bf = Pa = null;
            break;
          case "mousedown":
            Tf = !0;
            break;
          case "contextmenu":
          case "mouseup":
          case "dragend":
            Tf = !1, i0(z, a, g);
            break;
          case "selectionchange":
            if (Cm) break;
          case "keydown":
          case "keyup":
            i0(z, a, g);
        }
        var j;
        if (of)
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
          Ia ? $i(l, a) && (w = "onCompositionEnd") : l === "keydown" && a.keyCode === 229 && (w = "onCompositionStart");
        w && (Ji && a.locale !== "ko" && (Ia || w !== "onCompositionStart" ? w === "onCompositionEnd" && Ia && (j = Zi()) : (It = g, sf = "value" in It ? It.value : It.textContent, Ia = !0)), N = Mn(o, w), 0 < N.length && (w = new xi(
          w,
          l,
          null,
          a,
          g
        ), z.push({ event: w, listeners: N }), j ? w.data = j : (j = ki(a), j !== null && (w.data = j)))), (j = zm ? Em(l, a) : Am(l, a)) && (w = Mn(o, "onBeforeInput"), 0 < w.length && (N = new xi(
          "onBeforeInput",
          "beforeinput",
          null,
          a,
          g
        ), z.push({
          event: N,
          listeners: w
        }), N.data = j)), dd(
          z,
          l,
          o,
          a,
          g
        );
      }
      Bv(z, t);
    });
  }
  function me(l, t, a) {
    return {
      instance: l,
      listener: t,
      currentTarget: a
    };
  }
  function Mn(l, t) {
    for (var a = t + "Capture", u = []; l !== null; ) {
      var e = l, n = e.stateNode;
      if (e = e.tag, e !== 5 && e !== 26 && e !== 27 || n === null || (e = Ru(l, a), e != null && u.unshift(
        me(l, e, n)
      ), e = Ru(l, t), e != null && u.push(
        me(l, e, n)
      )), l.tag === 3) return u;
      l = l.return;
    }
    return [];
  }
  function gd(l) {
    if (l === null) return null;
    do
      l = l.return;
    while (l && l.tag !== 5 && l.tag !== 27);
    return l || null;
  }
  function Yv(l, t, a, u, e) {
    for (var n = t._reactName, f = []; a !== null && a !== u; ) {
      var c = a, i = c.alternate, o = c.stateNode;
      if (c = c.tag, i !== null && i === u) break;
      c !== 5 && c !== 26 && c !== 27 || o === null || (i = o, e ? (o = Ru(a, n), o != null && f.unshift(
        me(a, o, i)
      )) : e || (o = Ru(a, n), o != null && f.push(
        me(a, o, i)
      ))), a = a.return;
    }
    f.length !== 0 && l.push({ event: t, listeners: f });
  }
  var bd = /\r\n?/g, Td = /\u0000|\uFFFD/g;
  function Gv(l) {
    return (typeof l == "string" ? l : "" + l).replace(bd, `
`).replace(Td, "");
  }
  function Xv(l, t) {
    return t = Gv(t), Gv(l) === t;
  }
  function fl(l, t, a, u, e, n) {
    switch (a) {
      case "children":
        typeof u == "string" ? t === "body" || t === "textarea" && u === "" || $a(l, u) : (typeof u == "number" || typeof u == "bigint") && t !== "body" && $a(l, "" + u);
        break;
      case "className":
        pe(l, "class", u);
        break;
      case "tabIndex":
        pe(l, "tabindex", u);
        break;
      case "dir":
      case "role":
      case "viewBox":
      case "width":
      case "height":
        pe(l, a, u);
        break;
      case "style":
        Xi(l, u, n);
        break;
      case "data":
        if (t !== "object") {
          pe(l, "data", u);
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
        u = Ne("" + u), l.setAttribute(a, u);
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
          typeof n == "function" && (a === "formAction" ? (t !== "input" && fl(l, t, "name", e.name, e, null), fl(
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
        if (u == null || typeof u == "symbol" || typeof u == "boolean") {
          l.removeAttribute(a);
          break;
        }
        u = Ne("" + u), l.setAttribute(a, u);
        break;
      case "onClick":
        u != null && (l.onclick = Ht);
        break;
      case "onScroll":
        u != null && K("scroll", l);
        break;
      case "onScrollEnd":
        u != null && K("scrollend", l);
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
        a = Ne("" + u), l.setAttributeNS(
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
        K("beforetoggle", l), K("toggle", l), Ue(l, "popover", u);
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
        Ue(l, "is", u);
        break;
      case "innerText":
      case "textContent":
        break;
      default:
        (!(2 < a.length) || a[0] !== "o" && a[0] !== "O" || a[1] !== "n" && a[1] !== "N") && (a = wy.get(a) || a, Ue(l, a, u));
    }
  }
  function xc(l, t, a, u, e, n) {
    switch (a) {
      case "style":
        Xi(l, u, n);
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
        typeof u == "string" ? $a(l, u) : (typeof u == "number" || typeof u == "bigint") && $a(l, "" + u);
        break;
      case "onScroll":
        u != null && K("scroll", l);
        break;
      case "onScrollEnd":
        u != null && K("scrollend", l);
        break;
      case "onClick":
        u != null && (l.onclick = Ht);
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
        if (!pi.hasOwnProperty(a))
          l: {
            if (a[0] === "o" && a[1] === "n" && (e = a.endsWith("Capture"), t = a.slice(2, e ? a.length - 7 : void 0), n = l[Zl] || null, n = n != null ? n[a] : null, typeof n == "function" && l.removeEventListener(t, n, e), typeof u == "function")) {
              typeof n != "function" && n !== null && (a in l ? l[a] = null : l.hasAttribute(a) && l.removeAttribute(a)), l.addEventListener(t, u, e);
              break l;
            }
            a in l ? l[a] = u : u === !0 ? l.setAttribute(a, "") : Ue(l, a, u);
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
        K("error", l), K("load", l);
        var u = !1, e = !1, n;
        for (n in a)
          if (a.hasOwnProperty(n)) {
            var f = a[n];
            if (f != null)
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
                  fl(l, t, n, f, a, null);
              }
          }
        e && fl(l, t, "srcSet", a.srcSet, a, null), u && fl(l, t, "src", a.src, a, null);
        return;
      case "input":
        K("invalid", l);
        var c = n = f = e = null, i = null, o = null;
        for (u in a)
          if (a.hasOwnProperty(u)) {
            var g = a[u];
            if (g != null)
              switch (u) {
                case "name":
                  e = g;
                  break;
                case "type":
                  f = g;
                  break;
                case "checked":
                  i = g;
                  break;
                case "defaultChecked":
                  o = g;
                  break;
                case "value":
                  n = g;
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
                  fl(l, t, u, g, a, null);
              }
          }
        Bi(
          l,
          n,
          c,
          i,
          o,
          f,
          e,
          !1
        );
        return;
      case "select":
        K("invalid", l), u = f = n = null;
        for (e in a)
          if (a.hasOwnProperty(e) && (c = a[e], c != null))
            switch (e) {
              case "value":
                n = c;
                break;
              case "defaultValue":
                f = c;
                break;
              case "multiple":
                u = c;
              default:
                fl(l, t, e, c, a, null);
            }
        t = n, a = f, l.multiple = !!u, t != null ? Wa(l, !!u, t, !1) : a != null && Wa(l, !!u, a, !0);
        return;
      case "textarea":
        K("invalid", l), n = e = u = null;
        for (f in a)
          if (a.hasOwnProperty(f) && (c = a[f], c != null))
            switch (f) {
              case "value":
                u = c;
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
                fl(l, t, f, c, a, null);
            }
        Yi(l, u, e, n);
        return;
      case "option":
        for (i in a)
          if (a.hasOwnProperty(i) && (u = a[i], u != null))
            switch (i) {
              case "selected":
                l.selected = u && typeof u != "function" && typeof u != "symbol";
                break;
              default:
                fl(l, t, i, u, a, null);
            }
        return;
      case "dialog":
        K("beforetoggle", l), K("toggle", l), K("cancel", l), K("close", l);
        break;
      case "iframe":
      case "object":
        K("load", l);
        break;
      case "video":
      case "audio":
        for (u = 0; u < ye.length; u++)
          K(ye[u], l);
        break;
      case "image":
        K("error", l), K("load", l);
        break;
      case "details":
        K("toggle", l);
        break;
      case "embed":
      case "source":
      case "link":
        K("error", l), K("load", l);
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
        for (o in a)
          if (a.hasOwnProperty(o) && (u = a[o], u != null))
            switch (o) {
              case "children":
              case "dangerouslySetInnerHTML":
                throw Error(m(137, t));
              default:
                fl(l, t, o, u, a, null);
            }
        return;
      default:
        if (uf(t)) {
          for (g in a)
            a.hasOwnProperty(g) && (u = a[g], u !== void 0 && xc(
              l,
              t,
              g,
              u,
              a,
              void 0
            ));
          return;
        }
    }
    for (c in a)
      a.hasOwnProperty(c) && (u = a[c], u != null && fl(l, t, c, u, a, null));
  }
  function zd(l, t, a, u) {
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
        var e = null, n = null, f = null, c = null, i = null, o = null, g = null;
        for (S in a) {
          var z = a[S];
          if (a.hasOwnProperty(S) && z != null)
            switch (S) {
              case "checked":
                break;
              case "value":
                break;
              case "defaultValue":
                i = z;
              default:
                u.hasOwnProperty(S) || fl(l, t, S, null, u, z);
            }
        }
        for (var h in u) {
          var S = u[h];
          if (z = a[h], u.hasOwnProperty(h) && (S != null || z != null))
            switch (h) {
              case "type":
                n = S;
                break;
              case "name":
                e = S;
                break;
              case "checked":
                o = S;
                break;
              case "defaultChecked":
                g = S;
                break;
              case "value":
                f = S;
                break;
              case "defaultValue":
                c = S;
                break;
              case "children":
              case "dangerouslySetInnerHTML":
                if (S != null)
                  throw Error(m(137, t));
                break;
              default:
                S !== z && fl(
                  l,
                  t,
                  h,
                  S,
                  u,
                  z
                );
            }
        }
        tf(
          l,
          f,
          c,
          i,
          o,
          g,
          n,
          e
        );
        return;
      case "select":
        S = f = c = h = null;
        for (n in a)
          if (i = a[n], a.hasOwnProperty(n) && i != null)
            switch (n) {
              case "value":
                break;
              case "multiple":
                S = i;
              default:
                u.hasOwnProperty(n) || fl(
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
                h = n;
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
                  u,
                  i
                );
            }
        t = c, a = f, u = S, h != null ? Wa(l, !!a, h, !1) : !!u != !!a && (t != null ? Wa(l, !!a, t, !0) : Wa(l, !!a, a ? [] : "", !1));
        return;
      case "textarea":
        S = h = null;
        for (c in a)
          if (e = a[c], a.hasOwnProperty(c) && e != null && !u.hasOwnProperty(c))
            switch (c) {
              case "value":
                break;
              case "children":
                break;
              default:
                fl(l, t, c, null, u, e);
            }
        for (f in u)
          if (e = u[f], n = a[f], u.hasOwnProperty(f) && (e != null || n != null))
            switch (f) {
              case "value":
                h = e;
                break;
              case "defaultValue":
                S = e;
                break;
              case "children":
                break;
              case "dangerouslySetInnerHTML":
                if (e != null) throw Error(m(91));
                break;
              default:
                e !== n && fl(l, t, f, e, u, n);
            }
        qi(l, h, S);
        return;
      case "option":
        for (var U in a)
          if (h = a[U], a.hasOwnProperty(U) && h != null && !u.hasOwnProperty(U))
            switch (U) {
              case "selected":
                l.selected = !1;
                break;
              default:
                fl(
                  l,
                  t,
                  U,
                  null,
                  u,
                  h
                );
            }
        for (i in u)
          if (h = u[i], S = a[i], u.hasOwnProperty(i) && h !== S && (h != null || S != null))
            switch (i) {
              case "selected":
                l.selected = h && typeof h != "function" && typeof h != "symbol";
                break;
              default:
                fl(
                  l,
                  t,
                  i,
                  h,
                  u,
                  S
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
        for (var R in a)
          h = a[R], a.hasOwnProperty(R) && h != null && !u.hasOwnProperty(R) && fl(l, t, R, null, u, h);
        for (o in u)
          if (h = u[o], S = a[o], u.hasOwnProperty(o) && h !== S && (h != null || S != null))
            switch (o) {
              case "children":
              case "dangerouslySetInnerHTML":
                if (h != null)
                  throw Error(m(137, t));
                break;
              default:
                fl(
                  l,
                  t,
                  o,
                  h,
                  u,
                  S
                );
            }
        return;
      default:
        if (uf(t)) {
          for (var cl in a)
            h = a[cl], a.hasOwnProperty(cl) && h !== void 0 && !u.hasOwnProperty(cl) && xc(
              l,
              t,
              cl,
              void 0,
              u,
              h
            );
          for (g in u)
            h = u[g], S = a[g], !u.hasOwnProperty(g) || h === S || h === void 0 && S === void 0 || xc(
              l,
              t,
              g,
              h,
              u,
              S
            );
          return;
        }
    }
    for (var y in a)
      h = a[y], a.hasOwnProperty(y) && h != null && !u.hasOwnProperty(y) && fl(l, t, y, null, u, h);
    for (z in u)
      h = u[z], S = a[z], !u.hasOwnProperty(z) || h === S || h == null && S == null || fl(l, t, z, h, u, S);
  }
  function Qv(l) {
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
  function Ed() {
    if (typeof performance.getEntriesByType == "function") {
      for (var l = 0, t = 0, a = performance.getEntriesByType("resource"), u = 0; u < a.length; u++) {
        var e = a[u], n = e.transferSize, f = e.initiatorType, c = e.duration;
        if (n && c && Qv(f)) {
          for (f = 0, c = e.responseEnd, u += 1; u < a.length; u++) {
            var i = a[u], o = i.startTime;
            if (o > c) break;
            var g = i.transferSize, z = i.initiatorType;
            g && Qv(z) && (i = i.responseEnd, f += g * (i < c ? 1 : (c - o) / (i - o)));
          }
          if (--u, t += 8 * (n + f) / (e.duration / 1e3), l++, 10 < l) break;
        }
      }
      if (0 < l) return t / l / 1e6;
    }
    return navigator.connection && (l = navigator.connection.downlink, typeof l == "number") ? l : 5;
  }
  var Kc = null, Jc = null;
  function Dn(l) {
    return l.nodeType === 9 ? l : l.ownerDocument;
  }
  function jv(l) {
    switch (l) {
      case "http://www.w3.org/2000/svg":
        return 1;
      case "http://www.w3.org/1998/Math/MathML":
        return 2;
      default:
        return 0;
    }
  }
  function Zv(l, t) {
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
  function wc(l, t) {
    return l === "textarea" || l === "noscript" || typeof t.children == "string" || typeof t.children == "number" || typeof t.children == "bigint" || typeof t.dangerouslySetInnerHTML == "object" && t.dangerouslySetInnerHTML !== null && t.dangerouslySetInnerHTML.__html != null;
  }
  var Wc = null;
  function Ad() {
    var l = window.event;
    return l && l.type === "popstate" ? l === Wc ? !1 : (Wc = l, !0) : (Wc = null, !1);
  }
  var Lv = typeof setTimeout == "function" ? setTimeout : void 0, rd = typeof clearTimeout == "function" ? clearTimeout : void 0, Vv = typeof Promise == "function" ? Promise : void 0, _d = typeof queueMicrotask == "function" ? queueMicrotask : typeof Vv < "u" ? function(l) {
    return Vv.resolve(null).then(l).catch(Od);
  } : Lv;
  function Od(l) {
    setTimeout(function() {
      throw l;
    });
  }
  function ha(l) {
    return l === "head";
  }
  function xv(l, t) {
    var a = t, u = 0;
    do {
      var e = a.nextSibling;
      if (l.removeChild(a), e && e.nodeType === 8)
        if (a = e.data, a === "/$" || a === "/&") {
          if (u === 0) {
            l.removeChild(e), Mu(t);
            return;
          }
          u--;
        } else if (a === "$" || a === "$?" || a === "$~" || a === "$!" || a === "&")
          u++;
        else if (a === "html")
          de(l.ownerDocument.documentElement);
        else if (a === "head") {
          a = l.ownerDocument.head, de(a);
          for (var n = a.firstChild; n; ) {
            var f = n.nextSibling, c = n.nodeName;
            n[Nu] || c === "SCRIPT" || c === "STYLE" || c === "LINK" && n.rel.toLowerCase() === "stylesheet" || a.removeChild(n), n = f;
          }
        } else
          a === "body" && de(l.ownerDocument.body);
      a = e;
    } while (a);
    Mu(t);
  }
  function Kv(l, t) {
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
  function $c(l) {
    var t = l.firstChild;
    for (t && t.nodeType === 10 && (t = t.nextSibling); t; ) {
      var a = t;
      switch (t = t.nextSibling, a.nodeName) {
        case "HTML":
        case "HEAD":
        case "BODY":
          $c(a), Pn(a);
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
  function Md(l, t, a, u) {
    for (; l.nodeType === 1; ) {
      var e = a;
      if (l.nodeName.toLowerCase() !== t.toLowerCase()) {
        if (!u && (l.nodeName !== "INPUT" || l.type !== "hidden"))
          break;
      } else if (u) {
        if (!l[Nu])
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
  function Dd(l, t, a) {
    if (t === "") return null;
    for (; l.nodeType !== 3; )
      if ((l.nodeType !== 1 || l.nodeName !== "INPUT" || l.type !== "hidden") && !a || (l = ht(l.nextSibling), l === null)) return null;
    return l;
  }
  function Jv(l, t) {
    for (; l.nodeType !== 8; )
      if ((l.nodeType !== 1 || l.nodeName !== "INPUT" || l.type !== "hidden") && !t || (l = ht(l.nextSibling), l === null)) return null;
    return l;
  }
  function kc(l) {
    return l.data === "$?" || l.data === "$~";
  }
  function Fc(l) {
    return l.data === "$!" || l.data === "$?" && l.ownerDocument.readyState !== "loading";
  }
  function Ud(l, t) {
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
  var Ic = null;
  function wv(l) {
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
  function Wv(l) {
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
  function $v(l, t, a) {
    switch (t = Dn(a), l) {
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
    Pn(l);
  }
  var St = /* @__PURE__ */ new Map(), kv = /* @__PURE__ */ new Set();
  function Un(l) {
    return typeof l.getRootNode == "function" ? l.getRootNode() : l.nodeType === 9 ? l : l.ownerDocument;
  }
  var Wt = _.d;
  _.d = {
    f: pd,
    r: Cd,
    D: Nd,
    C: Hd,
    L: Rd,
    m: Bd,
    X: Yd,
    S: qd,
    M: Gd
  };
  function pd() {
    var l = Wt.f(), t = Tn();
    return l || t;
  }
  function Cd(l) {
    var t = Ka(l);
    t !== null && t.tag === 5 && t.type === "form" ? ds(t) : Wt.r(l);
  }
  var ru = typeof document > "u" ? null : document;
  function Fv(l, t, a) {
    var u = ru;
    if (u && typeof t == "string" && t) {
      var e = it(t);
      e = 'link[rel="' + l + '"][href="' + e + '"]', typeof a == "string" && (e += '[crossorigin="' + a + '"]'), kv.has(e) || (kv.add(e), l = { rel: l, crossOrigin: a, href: t }, u.querySelector(e) === null && (t = u.createElement("link"), Nl(t, "link", l), Ol(t), u.head.appendChild(t)));
    }
  }
  function Nd(l) {
    Wt.D(l), Fv("dns-prefetch", l, null);
  }
  function Hd(l, t) {
    Wt.C(l, t), Fv("preconnect", l, t);
  }
  function Rd(l, t, a) {
    Wt.L(l, t, a);
    var u = ru;
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
          n = _u(l);
          break;
        case "script":
          n = Ou(l);
      }
      St.has(n) || (l = B(
        {
          rel: "preload",
          href: t === "image" && a && a.imageSrcSet ? void 0 : l,
          as: t
        },
        a
      ), St.set(n, l), u.querySelector(e) !== null || t === "style" && u.querySelector(oe(n)) || t === "script" && u.querySelector(he(n)) || (t = u.createElement("link"), Nl(t, "link", l), Ol(t), u.head.appendChild(t)));
    }
  }
  function Bd(l, t) {
    Wt.m(l, t);
    var a = ru;
    if (a && l) {
      var u = t && typeof t.as == "string" ? t.as : "script", e = 'link[rel="modulepreload"][as="' + it(u) + '"][href="' + it(l) + '"]', n = e;
      switch (u) {
        case "audioworklet":
        case "paintworklet":
        case "serviceworker":
        case "sharedworker":
        case "worker":
        case "script":
          n = Ou(l);
      }
      if (!St.has(n) && (l = B({ rel: "modulepreload", href: l }, t), St.set(n, l), a.querySelector(e) === null)) {
        switch (u) {
          case "audioworklet":
          case "paintworklet":
          case "serviceworker":
          case "sharedworker":
          case "worker":
          case "script":
            if (a.querySelector(he(n)))
              return;
        }
        u = a.createElement("link"), Nl(u, "link", l), Ol(u), a.head.appendChild(u);
      }
    }
  }
  function qd(l, t, a) {
    Wt.S(l, t, a);
    var u = ru;
    if (u && l) {
      var e = Ja(u).hoistableStyles, n = _u(l);
      t = t || "default";
      var f = e.get(n);
      if (!f) {
        var c = { loading: 0, preload: null };
        if (f = u.querySelector(
          oe(n)
        ))
          c.loading = 5;
        else {
          l = B(
            { rel: "stylesheet", href: l, "data-precedence": t },
            a
          ), (a = St.get(n)) && Pc(l, a);
          var i = f = u.createElement("link");
          Ol(i), Nl(i, "link", l), i._p = new Promise(function(o, g) {
            i.onload = o, i.onerror = g;
          }), i.addEventListener("load", function() {
            c.loading |= 1;
          }), i.addEventListener("error", function() {
            c.loading |= 2;
          }), c.loading |= 4, pn(f, t, u);
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
  function Yd(l, t) {
    Wt.X(l, t);
    var a = ru;
    if (a && l) {
      var u = Ja(a).hoistableScripts, e = Ou(l), n = u.get(e);
      n || (n = a.querySelector(he(e)), n || (l = B({ src: l, async: !0 }, t), (t = St.get(e)) && li(l, t), n = a.createElement("script"), Ol(n), Nl(n, "link", l), a.head.appendChild(n)), n = {
        type: "script",
        instance: n,
        count: 1,
        state: null
      }, u.set(e, n));
    }
  }
  function Gd(l, t) {
    Wt.M(l, t);
    var a = ru;
    if (a && l) {
      var u = Ja(a).hoistableScripts, e = Ou(l), n = u.get(e);
      n || (n = a.querySelector(he(e)), n || (l = B({ src: l, async: !0, type: "module" }, t), (t = St.get(e)) && li(l, t), n = a.createElement("script"), Ol(n), Nl(n, "link", l), a.head.appendChild(n)), n = {
        type: "script",
        instance: n,
        count: 1,
        state: null
      }, u.set(e, n));
    }
  }
  function Iv(l, t, a, u) {
    var e = (e = V.current) ? Un(e) : null;
    if (!e) throw Error(m(446));
    switch (l) {
      case "meta":
      case "title":
        return null;
      case "style":
        return typeof a.precedence == "string" && typeof a.href == "string" ? (t = _u(a.href), a = Ja(
          e
        ).hoistableStyles, u = a.get(t), u || (u = {
          type: "style",
          instance: null,
          count: 0,
          state: null
        }, a.set(t, u)), u) : { type: "void", instance: null, count: 0, state: null };
      case "link":
        if (a.rel === "stylesheet" && typeof a.href == "string" && typeof a.precedence == "string") {
          l = _u(a.href);
          var n = Ja(
            e
          ).hoistableStyles, f = n.get(l);
          if (f || (e = e.ownerDocument || e, f = {
            type: "stylesheet",
            instance: null,
            count: 0,
            state: { loading: 0, preload: null }
          }, n.set(l, f), (n = e.querySelector(
            oe(l)
          )) && !n._p && (f.instance = n, f.state.loading = 5), St.has(l) || (a = {
            rel: "preload",
            as: "style",
            href: a.href,
            crossOrigin: a.crossOrigin,
            integrity: a.integrity,
            media: a.media,
            hrefLang: a.hrefLang,
            referrerPolicy: a.referrerPolicy
          }, St.set(l, a), n || Xd(
            e,
            l,
            a,
            f.state
          ))), t && u === null)
            throw Error(m(528, ""));
          return f;
        }
        if (t && u !== null)
          throw Error(m(529, ""));
        return null;
      case "script":
        return t = a.async, a = a.src, typeof a == "string" && t && typeof t != "function" && typeof t != "symbol" ? (t = Ou(a), a = Ja(
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
  function _u(l) {
    return 'href="' + it(l) + '"';
  }
  function oe(l) {
    return 'link[rel="stylesheet"][' + l + "]";
  }
  function Pv(l) {
    return B({}, l, {
      "data-precedence": l.precedence,
      precedence: null
    });
  }
  function Xd(l, t, a, u) {
    l.querySelector('link[rel="preload"][as="style"][' + t + "]") ? u.loading = 1 : (t = l.createElement("link"), u.preload = t, t.addEventListener("load", function() {
      return u.loading |= 1;
    }), t.addEventListener("error", function() {
      return u.loading |= 2;
    }), Nl(t, "link", a), Ol(t), l.head.appendChild(t));
  }
  function Ou(l) {
    return '[src="' + it(l) + '"]';
  }
  function he(l) {
    return "script[async]" + l;
  }
  function ly(l, t, a) {
    if (t.count++, t.instance === null)
      switch (t.type) {
        case "style":
          var u = l.querySelector(
            'style[data-href~="' + it(a.href) + '"]'
          );
          if (u)
            return t.instance = u, Ol(u), u;
          var e = B({}, a, {
            "data-href": a.href,
            "data-precedence": a.precedence,
            href: null,
            precedence: null
          });
          return u = (l.ownerDocument || l).createElement(
            "style"
          ), Ol(u), Nl(u, "style", e), pn(u, a.precedence, l), t.instance = u;
        case "stylesheet":
          e = _u(a.href);
          var n = l.querySelector(
            oe(e)
          );
          if (n)
            return t.state.loading |= 4, t.instance = n, Ol(n), n;
          u = Pv(a), (e = St.get(e)) && Pc(u, e), n = (l.ownerDocument || l).createElement("link"), Ol(n);
          var f = n;
          return f._p = new Promise(function(c, i) {
            f.onload = c, f.onerror = i;
          }), Nl(n, "link", u), t.state.loading |= 4, pn(n, a.precedence, l), t.instance = n;
        case "script":
          return n = Ou(a.src), (e = l.querySelector(
            he(n)
          )) ? (t.instance = e, Ol(e), e) : (u = a, (e = St.get(n)) && (u = B({}, a), li(u, e)), l = l.ownerDocument || l, e = l.createElement("script"), Ol(e), Nl(e, "link", u), l.head.appendChild(e), t.instance = e);
        case "void":
          return null;
        default:
          throw Error(m(443, t.type));
      }
    else
      t.type === "stylesheet" && (t.state.loading & 4) === 0 && (u = t.instance, t.state.loading |= 4, pn(u, a.precedence, l));
    return t.instance;
  }
  function pn(l, t, a) {
    for (var u = a.querySelectorAll(
      'link[rel="stylesheet"][data-precedence],style[data-precedence]'
    ), e = u.length ? u[u.length - 1] : null, n = e, f = 0; f < u.length; f++) {
      var c = u[f];
      if (c.dataset.precedence === t) n = c;
      else if (n !== e) break;
    }
    n ? n.parentNode.insertBefore(l, n.nextSibling) : (t = a.nodeType === 9 ? a.head : a, t.insertBefore(l, t.firstChild));
  }
  function Pc(l, t) {
    l.crossOrigin == null && (l.crossOrigin = t.crossOrigin), l.referrerPolicy == null && (l.referrerPolicy = t.referrerPolicy), l.title == null && (l.title = t.title);
  }
  function li(l, t) {
    l.crossOrigin == null && (l.crossOrigin = t.crossOrigin), l.referrerPolicy == null && (l.referrerPolicy = t.referrerPolicy), l.integrity == null && (l.integrity = t.integrity);
  }
  var Cn = null;
  function ty(l, t, a) {
    if (Cn === null) {
      var u = /* @__PURE__ */ new Map(), e = Cn = /* @__PURE__ */ new Map();
      e.set(a, u);
    } else
      e = Cn, u = e.get(a), u || (u = /* @__PURE__ */ new Map(), e.set(a, u));
    if (u.has(l)) return u;
    for (u.set(l, null), a = a.getElementsByTagName(l), e = 0; e < a.length; e++) {
      var n = a[e];
      if (!(n[Nu] || n[Dl] || l === "link" && n.getAttribute("rel") === "stylesheet") && n.namespaceURI !== "http://www.w3.org/2000/svg") {
        var f = n.getAttribute(t) || "";
        f = l + f;
        var c = u.get(f);
        c ? c.push(n) : u.set(f, [n]);
      }
    }
    return u;
  }
  function ay(l, t, a) {
    l = l.ownerDocument || l, l.head.insertBefore(
      a,
      t === "title" ? l.querySelector("head > title") : null
    );
  }
  function Qd(l, t, a) {
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
  function uy(l) {
    return !(l.type === "stylesheet" && (l.state.loading & 3) === 0);
  }
  function jd(l, t, a, u) {
    if (a.type === "stylesheet" && (typeof u.media != "string" || matchMedia(u.media).matches !== !1) && (a.state.loading & 4) === 0) {
      if (a.instance === null) {
        var e = _u(u.href), n = t.querySelector(
          oe(e)
        );
        if (n) {
          t = n._p, t !== null && typeof t == "object" && typeof t.then == "function" && (l.count++, l = Nn.bind(l), t.then(l, l)), a.state.loading |= 4, a.instance = n, Ol(n);
          return;
        }
        n = t.ownerDocument || t, u = Pv(u), (e = St.get(e)) && Pc(u, e), n = n.createElement("link"), Ol(n);
        var f = n;
        f._p = new Promise(function(c, i) {
          f.onload = c, f.onerror = i;
        }), Nl(n, "link", u), a.instance = n;
      }
      l.stylesheets === null && (l.stylesheets = /* @__PURE__ */ new Map()), l.stylesheets.set(a, t), (t = a.state.preload) && (a.state.loading & 3) === 0 && (l.count++, a = Nn.bind(l), t.addEventListener("load", a), t.addEventListener("error", a));
    }
  }
  var ti = 0;
  function Zd(l, t) {
    return l.stylesheets && l.count === 0 && Rn(l, l.stylesheets), 0 < l.count || 0 < l.imgCount ? function(a) {
      var u = setTimeout(function() {
        if (l.stylesheets && Rn(l, l.stylesheets), l.unsuspend) {
          var n = l.unsuspend;
          l.unsuspend = null, n();
        }
      }, 6e4 + t);
      0 < l.imgBytes && ti === 0 && (ti = 62500 * Ed());
      var e = setTimeout(
        function() {
          if (l.waitingForImages = !1, l.count === 0 && (l.stylesheets && Rn(l, l.stylesheets), l.unsuspend)) {
            var n = l.unsuspend;
            l.unsuspend = null, n();
          }
        },
        (l.imgBytes > ti ? 50 : 800) + t
      );
      return l.unsuspend = a, function() {
        l.unsuspend = null, clearTimeout(u), clearTimeout(e);
      };
    } : null;
  }
  function Nn() {
    if (this.count--, this.count === 0 && (this.imgCount === 0 || !this.waitingForImages)) {
      if (this.stylesheets) Rn(this, this.stylesheets);
      else if (this.unsuspend) {
        var l = this.unsuspend;
        this.unsuspend = null, l();
      }
    }
  }
  var Hn = null;
  function Rn(l, t) {
    l.stylesheets = null, l.unsuspend !== null && (l.count++, Hn = /* @__PURE__ */ new Map(), t.forEach(Ld, l), Hn = null, Nn.call(l));
  }
  function Ld(l, t) {
    if (!(t.state.loading & 4)) {
      var a = Hn.get(l);
      if (a) var u = a.get(null);
      else {
        a = /* @__PURE__ */ new Map(), Hn.set(l, a);
        for (var e = l.querySelectorAll(
          "link[data-precedence],style[data-precedence]"
        ), n = 0; n < e.length; n++) {
          var f = e[n];
          (f.nodeName === "LINK" || f.getAttribute("media") !== "not all") && (a.set(f.dataset.precedence, f), u = f);
        }
        u && a.set(null, u);
      }
      e = t.instance, f = e.getAttribute("data-precedence"), n = a.get(f) || u, n === u && a.set(null, e), a.set(f, e), this.count++, u = Nn.bind(this), e.addEventListener("load", u), e.addEventListener("error", u), n ? n.parentNode.insertBefore(e, n.nextSibling) : (l = l.nodeType === 9 ? l.head : l, l.insertBefore(e, l.firstChild)), t.state.loading |= 4;
    }
  }
  var Se = {
    $$typeof: Hl,
    Provider: null,
    Consumer: null,
    _currentValue: q,
    _currentValue2: q,
    _threadCount: 0
  };
  function Vd(l, t, a, u, e, n, f, c, i) {
    this.tag = 1, this.containerInfo = l, this.pingCache = this.current = this.pendingChildren = null, this.timeoutHandle = -1, this.callbackNode = this.next = this.pendingContext = this.context = this.cancelPendingCommit = null, this.callbackPriority = 0, this.expirationTimes = $n(-1), this.entangledLanes = this.shellSuspendCounter = this.errorRecoveryDisabledLanes = this.expiredLanes = this.warmLanes = this.pingedLanes = this.suspendedLanes = this.pendingLanes = 0, this.entanglements = $n(0), this.hiddenUpdates = $n(null), this.identifierPrefix = u, this.onUncaughtError = e, this.onCaughtError = n, this.onRecoverableError = f, this.pooledCache = null, this.pooledCacheLanes = 0, this.formState = i, this.incompleteTransitions = /* @__PURE__ */ new Map();
  }
  function ey(l, t, a, u, e, n, f, c, i, o, g, z) {
    return l = new Vd(
      l,
      t,
      a,
      f,
      i,
      o,
      g,
      z,
      c
    ), t = 1, n === !0 && (t |= 24), n = lt(3, null, null, t), l.current = n, n.stateNode = l, t = Bf(), t.refCount++, l.pooledCache = t, t.refCount++, n.memoizedState = {
      element: u,
      isDehydrated: a,
      cache: t
    }, Xf(n), l;
  }
  function ny(l) {
    return l ? (l = au, l) : au;
  }
  function fy(l, t, a, u, e, n) {
    e = ny(e), u.context === null ? u.context = e : u.pendingContext = e, u = ea(t), u.payload = { element: a }, n = n === void 0 ? null : n, n !== null && (u.callback = n), a = na(l, u, t), a !== null && (wl(a, l, t), Wu(a, l, t));
  }
  function cy(l, t) {
    if (l = l.memoizedState, l !== null && l.dehydrated !== null) {
      var a = l.retryLane;
      l.retryLane = a !== 0 && a < t ? a : t;
    }
  }
  function ai(l, t) {
    cy(l, t), (l = l.alternate) && cy(l, t);
  }
  function iy(l) {
    if (l.tag === 13 || l.tag === 31) {
      var t = Ua(l, 67108864);
      t !== null && wl(t, l, 67108864), ai(l, 67108864);
    }
  }
  function sy(l) {
    if (l.tag === 13 || l.tag === 31) {
      var t = nt();
      t = kn(t);
      var a = Ua(l, t);
      a !== null && wl(a, l, t), ai(l, t);
    }
  }
  var Bn = !0;
  function xd(l, t, a, u) {
    var e = b.T;
    b.T = null;
    var n = _.p;
    try {
      _.p = 2, ui(l, t, a, u);
    } finally {
      _.p = n, b.T = e;
    }
  }
  function Kd(l, t, a, u) {
    var e = b.T;
    b.T = null;
    var n = _.p;
    try {
      _.p = 8, ui(l, t, a, u);
    } finally {
      _.p = n, b.T = e;
    }
  }
  function ui(l, t, a, u) {
    if (Bn) {
      var e = ei(u);
      if (e === null)
        Vc(
          l,
          t,
          u,
          qn,
          a
        ), yy(l, u);
      else if (wd(
        e,
        l,
        t,
        a,
        u
      ))
        u.stopPropagation();
      else if (yy(l, u), t & 4 && -1 < Jd.indexOf(l)) {
        for (; e !== null; ) {
          var n = Ka(e);
          if (n !== null)
            switch (n.tag) {
              case 3:
                if (n = n.stateNode, n.current.memoizedState.isDehydrated) {
                  var f = ra(n.pendingLanes);
                  if (f !== 0) {
                    var c = n;
                    for (c.pendingLanes |= 2, c.entangledLanes |= 2; f; ) {
                      var i = 1 << 31 - Il(f);
                      c.entanglements[1] |= i, f &= ~i;
                    }
                    Dt(n), (ll & 6) === 0 && (gn = kl() + 500, ve(0));
                  }
                }
                break;
              case 31:
              case 13:
                c = Ua(n, 2), c !== null && wl(c, n, 2), Tn(), ai(n, 2);
            }
          if (n = ei(u), n === null && Vc(
            l,
            t,
            u,
            qn,
            a
          ), n === e) break;
          e = n;
        }
        e !== null && u.stopPropagation();
      } else
        Vc(
          l,
          t,
          u,
          null,
          a
        );
    }
  }
  function ei(l) {
    return l = nf(l), ni(l);
  }
  var qn = null;
  function ni(l) {
    if (qn = null, l = xa(l), l !== null) {
      var t = L(l);
      if (t === null) l = null;
      else {
        var a = t.tag;
        if (a === 13) {
          if (l = W(t), l !== null) return l;
          l = null;
        } else if (a === 31) {
          if (l = Sl(t), l !== null) return l;
          l = null;
        } else if (a === 3) {
          if (t.stateNode.current.memoizedState.isDehydrated)
            return t.tag === 3 ? t.stateNode.containerInfo : null;
          l = null;
        } else t !== l && (l = null);
      }
    }
    return qn = l, null;
  }
  function vy(l) {
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
        switch (Ny()) {
          case bi:
            return 2;
          case Ti:
            return 8;
          case re:
          case Hy:
            return 32;
          case zi:
            return 268435456;
          default:
            return 32;
        }
      default:
        return 32;
    }
  }
  var fi = !1, Sa = null, ga = null, ba = null, ge = /* @__PURE__ */ new Map(), be = /* @__PURE__ */ new Map(), Ta = [], Jd = "mousedown mouseup touchcancel touchend touchstart auxclick dblclick pointercancel pointerdown pointerup dragend dragstart drop compositionend compositionstart keydown keypress keyup input textInput copy cut paste click change contextmenu reset".split(
    " "
  );
  function yy(l, t) {
    switch (l) {
      case "focusin":
      case "focusout":
        Sa = null;
        break;
      case "dragenter":
      case "dragleave":
        ga = null;
        break;
      case "mouseover":
      case "mouseout":
        ba = null;
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
  function Te(l, t, a, u, e, n) {
    return l === null || l.nativeEvent !== n ? (l = {
      blockedOn: t,
      domEventName: a,
      eventSystemFlags: u,
      nativeEvent: n,
      targetContainers: [e]
    }, t !== null && (t = Ka(t), t !== null && iy(t)), l) : (l.eventSystemFlags |= u, t = l.targetContainers, e !== null && t.indexOf(e) === -1 && t.push(e), l);
  }
  function wd(l, t, a, u, e) {
    switch (t) {
      case "focusin":
        return Sa = Te(
          Sa,
          l,
          t,
          a,
          u,
          e
        ), !0;
      case "dragenter":
        return ga = Te(
          ga,
          l,
          t,
          a,
          u,
          e
        ), !0;
      case "mouseover":
        return ba = Te(
          ba,
          l,
          t,
          a,
          u,
          e
        ), !0;
      case "pointerover":
        var n = e.pointerId;
        return ge.set(
          n,
          Te(
            ge.get(n) || null,
            l,
            t,
            a,
            u,
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
            a,
            u,
            e
          )
        ), !0;
    }
    return !1;
  }
  function my(l) {
    var t = xa(l.target);
    if (t !== null) {
      var a = L(t);
      if (a !== null) {
        if (t = a.tag, t === 13) {
          if (t = W(a), t !== null) {
            l.blockedOn = t, Mi(l.priority, function() {
              sy(a);
            });
            return;
          }
        } else if (t === 31) {
          if (t = Sl(a), t !== null) {
            l.blockedOn = t, Mi(l.priority, function() {
              sy(a);
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
  function Yn(l) {
    if (l.blockedOn !== null) return !1;
    for (var t = l.targetContainers; 0 < t.length; ) {
      var a = ei(l.nativeEvent);
      if (a === null) {
        a = l.nativeEvent;
        var u = new a.constructor(
          a.type,
          a
        );
        ef = u, a.target.dispatchEvent(u), ef = null;
      } else
        return t = Ka(a), t !== null && iy(t), l.blockedOn = a, !1;
      t.shift();
    }
    return !0;
  }
  function dy(l, t, a) {
    Yn(l) && a.delete(t);
  }
  function Wd() {
    fi = !1, Sa !== null && Yn(Sa) && (Sa = null), ga !== null && Yn(ga) && (ga = null), ba !== null && Yn(ba) && (ba = null), ge.forEach(dy), be.forEach(dy);
  }
  function Gn(l, t) {
    l.blockedOn === t && (l.blockedOn = null, fi || (fi = !0, A.unstable_scheduleCallback(
      A.unstable_NormalPriority,
      Wd
    )));
  }
  var Xn = null;
  function oy(l) {
    Xn !== l && (Xn = l, A.unstable_scheduleCallback(
      A.unstable_NormalPriority,
      function() {
        Xn === l && (Xn = null);
        for (var t = 0; t < l.length; t += 3) {
          var a = l[t], u = l[t + 1], e = l[t + 2];
          if (typeof u != "function") {
            if (ni(u || a) === null)
              continue;
            break;
          }
          var n = Ka(a);
          n !== null && (l.splice(t, 3), t -= 3, ec(
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
  function Mu(l) {
    function t(i) {
      return Gn(i, l);
    }
    Sa !== null && Gn(Sa, l), ga !== null && Gn(ga, l), ba !== null && Gn(ba, l), ge.forEach(t), be.forEach(t);
    for (var a = 0; a < Ta.length; a++) {
      var u = Ta[a];
      u.blockedOn === l && (u.blockedOn = null);
    }
    for (; 0 < Ta.length && (a = Ta[0], a.blockedOn === null); )
      my(a), a.blockedOn === null && Ta.shift();
    if (a = (l.ownerDocument || l).$$reactFormReplay, a != null)
      for (u = 0; u < a.length; u += 3) {
        var e = a[u], n = a[u + 1], f = e[Zl] || null;
        if (typeof n == "function")
          f || oy(a);
        else if (f) {
          var c = null;
          if (n && n.hasAttribute("formAction")) {
            if (e = n, f = n[Zl] || null)
              c = f.formAction;
            else if (ni(e) !== null) continue;
          } else c = f.action;
          typeof c == "function" ? a[u + 1] = c : (a.splice(u, 3), u -= 3), oy(a);
        }
      }
  }
  function hy() {
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
  function ci(l) {
    this._internalRoot = l;
  }
  Qn.prototype.render = ci.prototype.render = function(l) {
    var t = this._internalRoot;
    if (t === null) throw Error(m(409));
    var a = t.current, u = nt();
    fy(a, u, l, t, null, null);
  }, Qn.prototype.unmount = ci.prototype.unmount = function() {
    var l = this._internalRoot;
    if (l !== null) {
      this._internalRoot = null;
      var t = l.containerInfo;
      fy(l.current, 2, null, l, null, null), Tn(), t[Va] = null;
    }
  };
  function Qn(l) {
    this._internalRoot = l;
  }
  Qn.prototype.unstable_scheduleHydration = function(l) {
    if (l) {
      var t = Oi();
      l = { blockedOn: null, target: l, priority: t };
      for (var a = 0; a < Ta.length && t !== 0 && t < Ta[a].priority; a++) ;
      Ta.splice(a, 0, l), a === 0 && my(l);
    }
  };
  var Sy = M.version;
  if (Sy !== "19.2.4")
    throw Error(
      m(
        527,
        Sy,
        "19.2.4"
      )
    );
  _.findDOMNode = function(l) {
    var t = l._reactInternals;
    if (t === void 0)
      throw typeof l.render == "function" ? Error(m(188)) : (l = Object.keys(l).join(","), Error(m(268, l)));
    return l = r(t), l = l !== null ? F(l) : null, l = l === null ? null : l.stateNode, l;
  };
  var $d = {
    bundleType: 0,
    version: "19.2.4",
    rendererPackageName: "react-dom",
    currentDispatcherRef: b,
    reconcilerVersion: "19.2.4"
  };
  if (typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ < "u") {
    var jn = __REACT_DEVTOOLS_GLOBAL_HOOK__;
    if (!jn.isDisabled && jn.supportsFiber)
      try {
        Uu = jn.inject(
          $d
        ), Fl = jn;
      } catch {
      }
  }
  return ze.createRoot = function(l, t) {
    if (!G(l)) throw Error(m(299));
    var a = !1, u = "", e = rs, n = _s, f = Os;
    return t != null && (t.unstable_strictMode === !0 && (a = !0), t.identifierPrefix !== void 0 && (u = t.identifierPrefix), t.onUncaughtError !== void 0 && (e = t.onUncaughtError), t.onCaughtError !== void 0 && (n = t.onCaughtError), t.onRecoverableError !== void 0 && (f = t.onRecoverableError)), t = ey(
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
      f,
      hy
    ), l[Va] = t.current, Lc(l), new ci(t);
  }, ze.hydrateRoot = function(l, t, a) {
    if (!G(l)) throw Error(m(299));
    var u = !1, e = "", n = rs, f = _s, c = Os, i = null;
    return a != null && (a.unstable_strictMode === !0 && (u = !0), a.identifierPrefix !== void 0 && (e = a.identifierPrefix), a.onUncaughtError !== void 0 && (n = a.onUncaughtError), a.onCaughtError !== void 0 && (f = a.onCaughtError), a.onRecoverableError !== void 0 && (c = a.onRecoverableError), a.formState !== void 0 && (i = a.formState)), t = ey(
      l,
      1,
      !0,
      t,
      a ?? null,
      u,
      e,
      i,
      n,
      f,
      c,
      hy
    ), t.context = ny(null), a = t.current, u = nt(), u = kn(u), e = ea(u), e.callback = null, na(a, e, u), a = u, t.current.lanes = a, Cu(t, a), Dt(t), l[Va] = t.current, Lc(l), new Qn(t);
  }, ze.version = "19.2.4", ze;
}
var _y;
function _o() {
  if (_y) return vi.exports;
  _y = 1;
  function A() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(A);
      } catch (M) {
        console.error(M);
      }
  }
  return A(), vi.exports = ro(), vi.exports;
}
_o();
const Oy = /* @__PURE__ */ new Map();
function Oo(A, M) {
  let D = Oy.get(A);
  D || (D = /* @__PURE__ */ new Set(), Oy.set(A, D)), D.add(M);
}
class Mo {
  constructor(M) {
    this._subscribers = /* @__PURE__ */ new Set(), this.getSnapshot = () => this._state, this.subscribeStore = (D) => (this._subscribers.add(D), () => this._subscribers.delete(D)), this._state = { ...M };
  }
  replaceState(M) {
    this._state = { ...M }, this._notify();
  }
  applyPatch(M) {
    this._state = { ...this._state, ...M }, this._notify();
  }
  _notify() {
    for (const M of this._subscribers)
      M();
  }
}
const Do = hi.createContext(null), Za = /* @__PURE__ */ new Map();
function My(A) {
  const M = Za.get(A);
  M && (M.root.unmount(), Za.delete(A));
}
function Uo(A, M) {
  let D = Za.get(A);
  if (!D) {
    const G = new Mo(M);
    Oo(A, (W) => {
      G.applyPatch(W);
    }), Za.set(A, { root: null, store: G }), D = Za.get(A);
  }
  return { controlId: A, windowName: po, store: D.store };
}
let po = "";
function Dy() {
  new MutationObserver((M) => {
    for (const D of M)
      for (const m of D.removedNodes)
        if (m instanceof HTMLElement) {
          const G = m.id;
          G && Za.has(G) && My(G);
          for (const [L] of Za)
            m.querySelector("#" + CSS.escape(L)) && My(L);
        }
  }).observe(document.body, { childList: !0, subtree: !0 });
}
document.readyState === "loading" ? document.addEventListener("DOMContentLoaded", () => {
  Dy();
}) : Dy();
const Co = ({ control: A }) => {
  const M = A, D = bo(M.module), m = hi.useMemo(
    () => Uo(M.controlId, M.state),
    [M.controlId]
  );
  return D ? /* @__PURE__ */ si.createElement(Do.Provider, { value: m }, /* @__PURE__ */ si.createElement(D, { controlId: M.controlId, state: M.state })) : /* @__PURE__ */ si.createElement("span", null, "[Component not registered: ", M.module, "]");
}, { useCallback: No } = Z, Ho = () => {
  const A = Zn(), M = Ln(), D = A.tabs ?? [], m = A.activeTabId, G = No((L) => {
    L !== m && M("selectTab", { tabId: L });
  }, [M, m]);
  return /* @__PURE__ */ Z.createElement("div", { className: "tlTabBar" }, /* @__PURE__ */ Z.createElement("div", { className: "tlTabBar__tabs", role: "tablist" }, D.map((L) => /* @__PURE__ */ Z.createElement(
    "button",
    {
      key: L.id,
      role: "tab",
      "aria-selected": L.id === m,
      className: "tlTabBar__tab" + (L.id === m ? " tlTabBar__tab--active" : ""),
      onClick: () => G(L.id)
    },
    L.label
  ))), /* @__PURE__ */ Z.createElement("div", { className: "tlTabBar__content", role: "tabpanel" }, A.activeContent && /* @__PURE__ */ Z.createElement(Co, { control: A.activeContent })));
};
Ut("TLButton", vo);
Ut("TLToggleButton", mo);
Ut("TLTextInput", Pd);
Ut("TLNumberInput", to);
Ut("TLDatePicker", uo);
Ut("TLSelect", no);
Ut("TLCheckbox", co);
Ut("TLTable", io);
Ut("TLCounter", oo);
Ut("TLTabBar", Ho);
