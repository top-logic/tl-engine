import { React as r, register as m } from "tl-react-bridge";
function s({ controlId: n, state: e }) {
  const o = r.useRef(null), t = r.useRef(null), a = typeof e.diagram == "string" ? e.diagram : JSON.stringify(e.diagram);
  return r.useEffect(() => {
    const i = o.current;
    if (!i) return;
    const c = window.GWT_FlowDiagram;
    if (!c) {
      console.error("[TLFlowDiagram] GWT_FlowDiagram not loaded");
      return;
    }
    const u = document.body.dataset.windowName || "main", d = document.body.dataset.contextPath || "";
    return t.current = c.mount(
      i,
      n,
      u,
      d,
      a
    ), () => {
      t.current && (t.current.destroy(), t.current = null);
    };
  }, [n, a]), /* @__PURE__ */ r.createElement("div", { ref: o, style: { width: "100%", height: "100%" } });
}
m("TLFlowDiagram", s);
