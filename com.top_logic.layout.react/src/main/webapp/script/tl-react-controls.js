import { React as e, useTLFieldValue as i, getComponent as h, useTLState as m, useTLCommand as p, register as r } from "tl-react-bridge";
const { useCallback: C } = e, k = ({ state: a }) => {
  const [t, l] = i(), s = C(
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
}, { useCallback: x } = e, T = ({ state: a, config: t }) => {
  const [l, s] = i(), n = x(
    (c) => {
      const o = c.target.value, d = o === "" ? null : Number(o);
      s(d);
    },
    [s]
  ), u = t != null && t.decimal ? "0.01" : "1";
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      value: l != null ? String(l) : "",
      onChange: n,
      step: u,
      disabled: a.disabled === !0 || a.editable === !1,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: E } = e, v = ({ state: a }) => {
  const [t, l] = i(), s = E(
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
  const [l, s] = i(), n = g(
    (c) => {
      s(c.target.value || null);
    },
    [s]
  ), u = a.options ?? (t == null ? void 0 : t.options) ?? [];
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      value: l ?? "",
      onChange: n,
      disabled: a.disabled === !0 || a.editable === !1,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    u.map((c) => /* @__PURE__ */ e.createElement("option", { key: c.value, value: c.value }, c.label))
  );
}, { useCallback: L } = e, S = ({ state: a }) => {
  const [t, l] = i(), s = L(
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
}, R = ({ controlId: a, state: t }) => {
  const l = t.columns ?? [], s = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, l.map((n) => /* @__PURE__ */ e.createElement("th", { key: n.name }, n.label)))), /* @__PURE__ */ e.createElement("tbody", null, s.map((n, u) => /* @__PURE__ */ e.createElement("tr", { key: u }, l.map((c) => {
    const o = c.cellModule ? h(c.cellModule) : void 0, d = n[c.name];
    if (o) {
      const b = { value: d, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: c.name }, /* @__PURE__ */ e.createElement(
        o,
        {
          controlId: a + "-" + u + "-" + c.name,
          state: b
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: c.name }, d != null ? String(d) : "");
  })))));
}, { useCallback: N } = e, I = () => {
  const a = m(), t = p(), l = N(() => {
    t("click");
  }, [t]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: l,
      disabled: a.disabled === !0,
      className: "tlReactButton",
      style: { padding: "4px 8px", marginRight: "4px", cursor: "pointer" }
    },
    a.label
  );
}, V = () => {
  const a = m(), t = p(), l = a.count ?? 0;
  return /* @__PURE__ */ e.createElement("div", { style: { padding: "24px", fontFamily: "sans-serif" } }, /* @__PURE__ */ e.createElement("h3", { style: { margin: "0 0 16px 0" } }, "React Counter"), /* @__PURE__ */ e.createElement("div", { style: { display: "flex", alignItems: "center", gap: "16px" } }, /* @__PURE__ */ e.createElement(
    "button",
    {
      onClick: () => t("decrement"),
      style: {
        width: "40px",
        height: "40px",
        fontSize: "20px",
        cursor: "pointer",
        borderRadius: "4px",
        border: "1px solid #ccc"
      }
    },
    "−"
  ), /* @__PURE__ */ e.createElement("span", { style: { fontSize: "32px", minWidth: "60px", textAlign: "center" } }, l), /* @__PURE__ */ e.createElement(
    "button",
    {
      onClick: () => t("increment"),
      style: {
        width: "40px",
        height: "40px",
        fontSize: "20px",
        cursor: "pointer",
        borderRadius: "4px",
        border: "1px solid #ccc"
      }
    },
    "+"
  )), /* @__PURE__ */ e.createElement("p", { style: { marginTop: "16px", color: "#666", fontSize: "13px" } }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
};
r("TLButton", I);
r("TLTextInput", k);
r("TLNumberInput", T);
r("TLDatePicker", v);
r("TLSelect", y);
r("TLCheckbox", S);
r("TLTable", R);
r("TLCounter", V);
