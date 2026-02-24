import { useTLState as a, getComponent as r, React as e, register as m } from "tl-react-bridge";
const s = () => {
  const t = a(), l = r("TLButton");
  if (!l)
    return e.createElement("span", null, "[TLButton not registered]");
  const n = (o) => ({
    fontWeight: o ? "bold" : "normal",
    backgroundColor: o ? "#e0e0e0" : ""
  });
  return /* @__PURE__ */ e.createElement("div", { style: { marginTop: "0.5em" } }, /* @__PURE__ */ e.createElement("span", { style: n(t.disabled === !0) }, /* @__PURE__ */ e.createElement(l, { controlId: "", state: t, command: "toggleDisabled", label: "Disabled" })), /* @__PURE__ */ e.createElement("span", { style: n(t.immutable === !0) }, /* @__PURE__ */ e.createElement(l, { controlId: "", state: t, command: "toggleImmutable", label: "Immutable" })), /* @__PURE__ */ e.createElement("span", { style: n(t.mandatory === !0) }, /* @__PURE__ */ e.createElement(l, { controlId: "", state: t, command: "toggleMandatory", label: "Mandatory" })));
};
m("TLFieldToggles", s);
