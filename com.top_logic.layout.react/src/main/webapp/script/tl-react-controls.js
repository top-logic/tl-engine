import { React as e, useTLFieldValue as m, getComponent as k, useTLState as i, useTLCommand as b, TLChild as v, register as r } from "tl-react-bridge";
const { useCallback: p } = e, E = ({ state: t }) => {
  const [a, n] = m(), l = p(
    (c) => {
      n(c.target.value);
    },
    [n]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: a ?? "",
      onChange: l,
      disabled: t.disabled === !0 || t.editable === !1,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: h } = e, L = ({ state: t, config: a }) => {
  const [n, l] = m(), c = h(
    (o) => {
      const u = o.target.value, d = u === "" ? null : Number(u);
      l(d);
    },
    [l]
  ), s = a != null && a.decimal ? "0.01" : "1";
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      value: n != null ? String(n) : "",
      onChange: c,
      step: s,
      disabled: t.disabled === !0 || t.editable === !1,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: N } = e, _ = ({ state: t }) => {
  const [a, n] = m(), l = N(
    (c) => {
      n(c.target.value || null);
    },
    [n]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: a ?? "",
      onChange: l,
      disabled: t.disabled === !0 || t.editable === !1,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: g } = e, R = ({ state: t, config: a }) => {
  const [n, l] = m(), c = g(
    (o) => {
      l(o.target.value || null);
    },
    [l]
  ), s = t.options ?? (a == null ? void 0 : a.options) ?? [];
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      value: n ?? "",
      onChange: c,
      disabled: t.disabled === !0 || t.editable === !1,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    s.map((o) => /* @__PURE__ */ e.createElement("option", { key: o.value, value: o.value }, o.label))
  );
}, { useCallback: y } = e, B = ({ state: t }) => {
  const [a, n] = m(), l = y(
    (c) => {
      n(c.target.checked);
    },
    [n]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      checked: a === !0,
      onChange: l,
      disabled: t.disabled === !0 || t.editable === !1,
      className: "tlReactCheckbox"
    }
  );
}, S = ({ controlId: t, state: a }) => {
  const n = a.columns ?? [], l = a.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, n.map((c) => /* @__PURE__ */ e.createElement("th", { key: c.name }, c.label)))), /* @__PURE__ */ e.createElement("tbody", null, l.map((c, s) => /* @__PURE__ */ e.createElement("tr", { key: s }, n.map((o) => {
    const u = o.cellModule ? k(o.cellModule) : void 0, d = c[o.name];
    if (u) {
      const C = { value: d, editable: a.editable };
      return /* @__PURE__ */ e.createElement("td", { key: o.name }, /* @__PURE__ */ e.createElement(
        u,
        {
          controlId: t + "-" + s + "-" + o.name,
          state: C
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: o.name }, d != null ? String(d) : "");
  })))));
}, { useCallback: x } = e, F = ({ command: t, label: a, disabled: n }) => {
  const l = i(), c = b(), s = t ?? "click", o = a ?? l.label, u = n ?? l.disabled === !0, d = x(() => {
    c(s);
  }, [c, s]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: d,
      disabled: u,
      className: "tlReactButton"
    },
    o
  );
}, { useCallback: I } = e, V = ({ command: t, label: a, active: n, disabled: l }) => {
  const c = i(), s = b(), o = t ?? "click", u = a ?? c.label, d = n ?? c.active === !0, C = l ?? c.disabled === !0, T = I(() => {
    s(o);
  }, [s, o]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: T,
      disabled: C,
      className: "tlReactButton" + (d ? " tlReactButtonActive" : "")
    },
    u
  );
}, $ = () => {
  const t = i(), a = b(), n = t.count ?? 0, l = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, l), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => a("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, n), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => a("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: D } = e, P = () => {
  const t = i(), a = b(), n = t.tabs ?? [], l = t.activeTabId, c = D((s) => {
    s !== l && a("selectTab", { tabId: s });
  }, [a, l]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, n.map((s) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: s.id,
      role: "tab",
      "aria-selected": s.id === l,
      className: "tlReactTabBar__tab" + (s.id === l ? " tlReactTabBar__tab--active" : ""),
      onClick: () => c(s.id)
    },
    s.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(v, { control: t.activeContent })));
}, f = () => {
  const t = i(), a = t.title, n = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { className: "tlFieldList" }, a && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, a), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, n.map((l, c) => /* @__PURE__ */ e.createElement("div", { key: c, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(v, { control: l })))));
};
r("TLButton", F);
r("TLToggleButton", V);
r("TLTextInput", E);
r("TLNumberInput", L);
r("TLDatePicker", _);
r("TLSelect", R);
r("TLCheckbox", B);
r("TLTable", S);
r("TLCounter", $);
r("TLTabBar", P);
r("TLFieldList", f);
