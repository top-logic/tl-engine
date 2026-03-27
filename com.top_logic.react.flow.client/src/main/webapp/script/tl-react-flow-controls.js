import { React as r, register as m } from "tl-react-bridge";
function s({ controlId: n, state: e }) {
  const o = r.useRef(null), t = r.useRef(null);
  return r.useEffect(() => {
    const a = o.current;
    if (!a) return;
    const i = window.GWT_FlowDiagram;
    if (!i) {
      console.error("[TLFlowDiagram] GWT_FlowDiagram not loaded");
      return;
    }
    const c = document.body.dataset.windowName || "main", u = document.body.dataset.contextPath || "", d = typeof e.diagram == "string" ? e.diagram : JSON.stringify(e.diagram);
    return t.current = i.mount(
      a,
      n,
      c,
      u,
      d
    ), () => {
      t.current && (t.current.destroy(), t.current = null);
    };
  }, [n]), /* @__PURE__ */ r.createElement("div", { ref: o, style: { width: "100%", height: "100%" } });
}
m("TLFlowDiagram", s);
