import { useTLState as o, React as e, TLChild as l, register as n } from "tl-react-bridge";
const r = () => {
  const t = o();
  return /* @__PURE__ */ e.createElement("div", { className: "tlFieldToggles" }, /* @__PURE__ */ e.createElement(l, { control: t.disabledButton }), /* @__PURE__ */ e.createElement(l, { control: t.immutableButton }), /* @__PURE__ */ e.createElement(l, { control: t.mandatoryButton }));
};
n("TLFieldToggles", r);
