import { useTLState as a, React as e, TLChild as r, register as l } from "tl-react-bridge";
const o = () => {
  const t = a();
  return /* @__PURE__ */ e.createElement("div", { style: { marginTop: "0.5em", display: "flex", gap: "4px" } }, /* @__PURE__ */ e.createElement(r, { descriptor: t.disabledButton }), /* @__PURE__ */ e.createElement(r, { descriptor: t.immutableButton }), /* @__PURE__ */ e.createElement(r, { descriptor: t.mandatoryButton }));
};
l("TLFieldToggles", o);
