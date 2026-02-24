import { React as e, useTLFieldValue as i, getComponent as h, useTLState as m, useTLCommand as p, register as o } from "tl-react-bridge";
const { useCallback: C } = e, k = ({ state: a }) => {
  const [t, l] = i(), c = C(
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
      onChange: c,
      disabled: a.disabled === !0 || a.editable === !1,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: x } = e, v = ({ state: a, config: t }) => {
  const [l, c] = i(), n = x(
    (s) => {
      const u = s.target.value, d = u === "" ? null : Number(u);
      c(d);
    },
    [c]
  ), r = t != null && t.decimal ? "0.01" : "1";
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      value: l != null ? String(l) : "",
      onChange: n,
      step: r,
      disabled: a.disabled === !0 || a.editable === !1,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: T } = e, E = ({ state: a }) => {
  const [t, l] = i(), c = T(
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
      onChange: c,
      disabled: a.disabled === !0 || a.editable === !1,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: g } = e, y = ({ state: a, config: t }) => {
  const [l, c] = i(), n = g(
    (s) => {
      c(s.target.value || null);
    },
    [c]
  ), r = a.options ?? (t == null ? void 0 : t.options) ?? [];
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      value: l ?? "",
      onChange: n,
      disabled: a.disabled === !0 || a.editable === !1,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    r.map((s) => /* @__PURE__ */ e.createElement("option", { key: s.value, value: s.value }, s.label))
  );
}, { useCallback: L } = e, S = ({ state: a }) => {
  const [t, l] = i(), c = L(
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
      onChange: c,
      disabled: a.disabled === !0 || a.editable === !1,
      className: "tlReactCheckbox"
    }
  );
}, R = ({ controlId: a, state: t }) => {
  const l = t.columns ?? [], c = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, l.map((n) => /* @__PURE__ */ e.createElement("th", { key: n.name }, n.label)))), /* @__PURE__ */ e.createElement("tbody", null, c.map((n, r) => /* @__PURE__ */ e.createElement("tr", { key: r }, l.map((s) => {
    const u = s.cellModule ? h(s.cellModule) : void 0, d = n[s.name];
    if (u) {
      const b = { value: d, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: s.name }, /* @__PURE__ */ e.createElement(
        u,
        {
          controlId: a + "-" + r + "-" + s.name,
          state: b
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: s.name }, d != null ? String(d) : "");
  })))));
}, { useCallback: N } = e, I = ({ command: a, label: t, disabled: l }) => {
  const c = m(), n = p(), r = a ?? "click", s = t ?? c.label, u = l ?? c.disabled === !0, d = N(() => {
    n(r);
  }, [n, r]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: d,
      disabled: u,
      className: "tlReactButton",
      style: { padding: "4px 8px", marginRight: "4px", cursor: "pointer" }
    },
    s
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
o("TLButton", I);
o("TLTextInput", k);
o("TLNumberInput", v);
o("TLDatePicker", E);
o("TLSelect", y);
o("TLCheckbox", S);
o("TLTable", R);
o("TLCounter", V);
