import { React as e, useTLFieldValue as m, getComponent as k, useTLState as i, useTLCommand as b, TLChild as v, register as r } from "tl-react-bridge";
const { useCallback: p } = e, E = ({ state: t }) => {
  const [a, l] = m(), n = p(
    (c) => {
      l(c.target.value);
    },
    [l]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: a ?? "",
      onChange: n,
      disabled: t.disabled === !0 || t.editable === !1,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: h } = e, L = ({ state: t, config: a }) => {
  const [l, n] = m(), c = h(
    (o) => {
      const u = o.target.value, d = u === "" ? null : Number(u);
      n(d);
    },
    [n]
  ), s = a != null && a.decimal ? "0.01" : "1";
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      value: l != null ? String(l) : "",
      onChange: c,
      step: s,
      disabled: t.disabled === !0 || t.editable === !1,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: N } = e, _ = ({ state: t }) => {
  const [a, l] = m(), n = N(
    (c) => {
      l(c.target.value || null);
    },
    [l]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: a ?? "",
      onChange: n,
      disabled: t.disabled === !0 || t.editable === !1,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: g } = e, R = ({ state: t, config: a }) => {
  const [l, n] = m(), c = g(
    (o) => {
      n(o.target.value || null);
    },
    [n]
  ), s = t.options ?? (a == null ? void 0 : a.options) ?? [];
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      value: l ?? "",
      onChange: c,
      disabled: t.disabled === !0 || t.editable === !1,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    s.map((o) => /* @__PURE__ */ e.createElement("option", { key: o.value, value: o.value }, o.label))
  );
}, { useCallback: y } = e, B = ({ state: t }) => {
  const [a, l] = m(), n = y(
    (c) => {
      l(c.target.checked);
    },
    [l]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      checked: a === !0,
      onChange: n,
      disabled: t.disabled === !0 || t.editable === !1,
      className: "tlReactCheckbox"
    }
  );
}, S = ({ controlId: t, state: a }) => {
  const l = a.columns ?? [], n = a.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, l.map((c) => /* @__PURE__ */ e.createElement("th", { key: c.name }, c.label)))), /* @__PURE__ */ e.createElement("tbody", null, n.map((c, s) => /* @__PURE__ */ e.createElement("tr", { key: s }, l.map((o) => {
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
}, { useCallback: x } = e, F = ({ command: t, label: a, disabled: l }) => {
  const n = i(), c = b(), s = t ?? "click", o = a ?? n.label, u = l ?? n.disabled === !0, d = x(() => {
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
}, { useCallback: I } = e, V = ({ command: t, label: a, active: l, disabled: n }) => {
  const c = i(), s = b(), o = t ?? "click", u = a ?? c.label, d = l ?? c.active === !0, C = n ?? c.disabled === !0, T = I(() => {
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
  const t = i(), a = b(), l = t.count ?? 0;
  return /* @__PURE__ */ e.createElement("div", { className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, "React Counter"), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => a("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, l), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => a("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: D } = e, P = () => {
  const t = i(), a = b(), l = t.tabs ?? [], n = t.activeTabId, c = D((s) => {
    s !== n && a("selectTab", { tabId: s });
  }, [a, n]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, l.map((s) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: s.id,
      role: "tab",
      "aria-selected": s.id === n,
      className: "tlReactTabBar__tab" + (s.id === n ? " tlReactTabBar__tab--active" : ""),
      onClick: () => c(s.id)
    },
    s.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(v, { control: t.activeContent })));
}, f = () => {
  const t = i(), a = t.title, l = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { className: "tlFieldList" }, a && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, a), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, l.map((n, c) => /* @__PURE__ */ e.createElement("div", { key: c, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(v, { control: n })))));
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
