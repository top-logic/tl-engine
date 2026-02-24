import { React as e, useTLState as m, useTLCommand as u, register as d } from "tl-react-bridge";
const { useCallback: l } = e, b = () => {
  const o = m(), t = u(), r = l(() => t("toggleDisabled"), [t]), s = l(() => t("toggleImmutable"), [t]), g = l(() => t("toggleMandatory"), [t]), n = (a) => ({
    padding: "4px 8px",
    marginRight: "4px",
    cursor: "pointer",
    fontWeight: a ? "bold" : "normal",
    backgroundColor: a ? "#e0e0e0" : ""
  });
  return /* @__PURE__ */ e.createElement("div", { style: { marginTop: "0.5em" } }, /* @__PURE__ */ e.createElement("button", { type: "button", onClick: r, style: n(o.disabled === !0) }, "Disabled"), /* @__PURE__ */ e.createElement("button", { type: "button", onClick: s, style: n(o.immutable === !0) }, "Immutable"), /* @__PURE__ */ e.createElement("button", { type: "button", onClick: g, style: n(o.mandatory === !0) }, "Mandatory"));
};
d("TLFieldToggles", b);
