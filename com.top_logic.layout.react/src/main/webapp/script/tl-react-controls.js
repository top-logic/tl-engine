import { React as e, useTLFieldValue as m, getComponent as v, useTLState as i, useTLCommand as C, register as d } from "tl-react-bridge";
const { useCallback: p } = e, h = ({ state: a }) => {
  const [t, l] = m(), s = p(
    (n) => {
      l(n.target.value);
    },
    [l]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: t ?? "",
      onChange: s,
      disabled: a.disabled === !0 || a.editable === !1,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: T } = e, E = ({ state: a, config: t }) => {
  const [l, s] = m(), n = T(
    (c) => {
      const u = c.target.value, r = u === "" ? null : Number(u);
      s(r);
    },
    [s]
  ), o = t != null && t.decimal ? "0.01" : "1";
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      value: l != null ? String(l) : "",
      onChange: n,
      step: o,
      disabled: a.disabled === !0 || a.editable === !1,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: L } = e, N = ({ state: a }) => {
  const [t, l] = m(), s = L(
    (n) => {
      l(n.target.value || null);
    },
    [l]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: t ?? "",
      onChange: s,
      disabled: a.disabled === !0 || a.editable === !1,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: g } = e, y = ({ state: a, config: t }) => {
  const [l, s] = m(), n = g(
    (c) => {
      s(c.target.value || null);
    },
    [s]
  ), o = a.options ?? (t == null ? void 0 : t.options) ?? [];
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      value: l ?? "",
      onChange: n,
      disabled: a.disabled === !0 || a.editable === !1,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    o.map((c) => /* @__PURE__ */ e.createElement("option", { key: c.value, value: c.value }, c.label))
  );
}, { useCallback: _ } = e, R = ({ state: a }) => {
  const [t, l] = m(), s = _(
    (n) => {
      l(n.target.checked);
    },
    [l]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      checked: t === !0,
      onChange: s,
      disabled: a.disabled === !0 || a.editable === !1,
      className: "tlReactCheckbox"
    }
  );
}, S = ({ controlId: a, state: t }) => {
  const l = t.columns ?? [], s = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, l.map((n) => /* @__PURE__ */ e.createElement("th", { key: n.name }, n.label)))), /* @__PURE__ */ e.createElement("tbody", null, s.map((n, o) => /* @__PURE__ */ e.createElement("tr", { key: o }, l.map((c) => {
    const u = c.cellModule ? v(c.cellModule) : void 0, r = n[c.name];
    if (u) {
      const b = { value: r, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: c.name }, /* @__PURE__ */ e.createElement(
        u,
        {
          controlId: a + "-" + o + "-" + c.name,
          state: b
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: c.name }, r != null ? String(r) : "");
  })))));
}, { useCallback: x } = e, B = ({ command: a, label: t, disabled: l }) => {
  const s = i(), n = C(), o = a ?? "click", c = t ?? s.label, u = l ?? s.disabled === !0, r = x(() => {
    n(o);
  }, [n, o]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: r,
      disabled: u,
      className: "tlReactButton"
    },
    c
  );
}, { useCallback: V } = e, I = ({ command: a, label: t, active: l, disabled: s }) => {
  const n = i(), o = C(), c = a ?? "click", u = t ?? n.label, r = l ?? n.active === !0, b = s ?? n.disabled === !0, k = V(() => {
    o(c);
  }, [o, c]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: k,
      disabled: b,
      className: "tlReactButton" + (r ? " tlReactButtonActive" : "")
    },
    u
  );
}, $ = () => {
  const a = i(), t = C(), l = a.count ?? 0;
  return /* @__PURE__ */ e.createElement("div", { className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, "React Counter"), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => t("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, l), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => t("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
};
d("TLButton", B);
d("TLToggleButton", I);
d("TLTextInput", h);
d("TLNumberInput", E);
d("TLDatePicker", N);
d("TLSelect", y);
d("TLCheckbox", R);
d("TLTable", S);
d("TLCounter", $);
