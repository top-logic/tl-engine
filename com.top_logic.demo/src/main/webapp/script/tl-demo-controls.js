import { useTLState as i, React as e, TLChild as n, register as c } from "tl-react-bridge";
const s = () => {
  const t = i();
  return /* @__PURE__ */ e.createElement("div", { className: "tlFieldToggles" }, /* @__PURE__ */ e.createElement(n, { control: t.disabledButton }), /* @__PURE__ */ e.createElement(n, { control: t.immutableButton }), /* @__PURE__ */ e.createElement(n, { control: t.mandatoryButton }), /* @__PURE__ */ e.createElement(n, { control: t.hiddenButton }));
}, m = ({ controlId: t, state: o }) => {
  const r = o.count ?? 0, a = o.title ?? "", d = [];
  for (let l = 1; l <= r; l++)
    d.push(
      /* @__PURE__ */ e.createElement("tr", { key: l }, /* @__PURE__ */ e.createElement("td", { style: { padding: "4px 12px", borderBottom: "1px solid #e2e8f0" } }, l), /* @__PURE__ */ e.createElement("td", { style: { padding: "4px 12px", borderBottom: "1px solid #e2e8f0" } }, "Wert ", l))
    );
  return /* @__PURE__ */ e.createElement("div", { id: t, style: { padding: "0.5rem" } }, a && /* @__PURE__ */ e.createElement("h4", { style: { margin: "0 0 0.5rem 0" } }, a), r > 0 ? /* @__PURE__ */ e.createElement("table", { style: { borderCollapse: "collapse", width: "100%" } }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, /* @__PURE__ */ e.createElement("th", { style: { padding: "4px 12px", borderBottom: "2px solid #cbd5e1", textAlign: "left" } }, "#"), /* @__PURE__ */ e.createElement("th", { style: { padding: "4px 12px", borderBottom: "2px solid #cbd5e1", textAlign: "left" } }, "Eintrag"))), /* @__PURE__ */ e.createElement("tbody", null, d)) : /* @__PURE__ */ e.createElement("p", { style: { color: "#64748b", fontStyle: "italic" } }, "Keine Einträge"));
};
c("TLFieldToggles", s);
c("TLValueList", m);
