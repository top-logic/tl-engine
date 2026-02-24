import { React as e, useTLFieldValue as i, getComponent as x, useTLState as m, useTLCommand as p, register as u } from "tl-react-bridge";
const { useCallback: g } = e, v = ({ state: a }) => {
  const [t, l] = i(), s = g(
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
  const [l, s] = i(), n = T(
    (c) => {
      const d = c.target.value, r = d === "" ? null : Number(d);
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
}, { useCallback: y } = e, L = ({ state: a }) => {
  const [t, l] = i(), s = y(
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
}, { useCallback: R } = e, S = ({ state: a, config: t }) => {
  const [l, s] = i(), n = R(
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
}, { useCallback: f } = e, N = ({ state: a }) => {
  const [t, l] = i(), s = f(
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
}, B = ({ controlId: a, state: t }) => {
  const l = t.columns ?? [], s = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, l.map((n) => /* @__PURE__ */ e.createElement("th", { key: n.name }, n.label)))), /* @__PURE__ */ e.createElement("tbody", null, s.map((n, o) => /* @__PURE__ */ e.createElement("tr", { key: o }, l.map((c) => {
    const d = c.cellModule ? x(c.cellModule) : void 0, r = n[c.name];
    if (d) {
      const b = { value: r, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: c.name }, /* @__PURE__ */ e.createElement(
        d,
        {
          controlId: a + "-" + o + "-" + c.name,
          state: b
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: c.name }, r != null ? String(r) : "");
  })))));
}, { useCallback: I } = e, V = ({ command: a, label: t, disabled: l }) => {
  const s = m(), n = p(), o = a ?? "click", c = t ?? s.label, d = l ?? s.disabled === !0, r = I(() => {
    n(o);
  }, [n, o]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: r,
      disabled: d,
      className: "tlReactButton",
      style: { padding: "4px 8px", marginRight: "4px", cursor: "pointer" }
    },
    c
  );
}, { useCallback: $ } = e, D = ({ command: a, label: t, active: l, disabled: s }) => {
  const n = m(), o = p(), c = a ?? "click", d = t ?? n.label, r = l ?? n.active === !0, b = s ?? n.disabled === !0, C = $(() => {
    o(c);
  }, [o, c]), h = {
    padding: "4px 8px",
    marginRight: "4px",
    cursor: "pointer",
    fontWeight: "bold",
    backgroundColor: "#2563eb",
    borderColor: "#2563eb",
    color: "#ffffff",
    border: "1px solid #2563eb",
    borderRadius: "3px"
  }, k = {
    padding: "4px 8px",
    marginRight: "4px",
    cursor: "pointer",
    fontWeight: "normal",
    backgroundColor: "#e5e7eb",
    borderColor: "#d1d5db",
    color: "#374151",
    border: "1px solid #d1d5db",
    borderRadius: "3px"
  };
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: C,
      disabled: b,
      className: "tlReactButton" + (r ? " tlReactButtonActive" : ""),
      style: r ? h : k
    },
    d
  );
}, w = () => {
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
u("TLButton", V);
u("TLToggleButton", D);
u("TLTextInput", v);
u("TLNumberInput", E);
u("TLDatePicker", L);
u("TLSelect", S);
u("TLCheckbox", N);
u("TLTable", B);
u("TLCounter", w);
