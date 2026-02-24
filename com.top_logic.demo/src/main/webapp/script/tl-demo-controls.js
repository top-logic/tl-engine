import { React as e, useTLState as l, getComponent as a, createChildContext as s, TLControlContext as c, register as m } from "tl-react-bridge";
const { useMemo: d } = e, u = () => {
  const t = l(), n = a("TLButton");
  if (!n)
    return e.createElement("span", null, "[TLButton not registered]");
  const o = t.children ?? [];
  return /* @__PURE__ */ e.createElement("div", { style: { marginTop: "0.5em", display: "flex", gap: "4px" } }, o.map((r) => /* @__PURE__ */ e.createElement(
    T,
    {
      key: r.controlId,
      childDescriptor: r,
      ButtonComponent: n
    }
  )));
}, T = ({ childDescriptor: t, ButtonComponent: n }) => {
  const o = d(
    () => s(t.controlId, t.state),
    [t.controlId]
  );
  return /* @__PURE__ */ e.createElement(c.Provider, { value: o }, /* @__PURE__ */ e.createElement(n, { controlId: t.controlId, state: t.state }));
};
m("TLFieldToggles", u);
