import { React as e, register as g } from "tl-react-bridge";
const T = 1e4, m = 50;
function y({ controlId: o, state: r }) {
  const i = e.useRef(null), t = e.useRef(null), a = typeof r.diagram == "string" ? r.diagram : JSON.stringify(r.diagram);
  return e.useEffect(() => {
    const c = i.current;
    if (!c) return;
    let u = !1, n, d = 0;
    const l = () => {
      if (u) return;
      const s = window.GWT_FlowDiagram;
      if (!s) {
        if (d >= T) {
          console.error("[TLFlowDiagram] GWT_FlowDiagram not loaded");
          return;
        }
        d += m, n = window.setTimeout(l, m);
        return;
      }
      const w = document.body.dataset.windowName || "main", f = document.body.dataset.contextPath || "";
      t.current = s.mount(
        c,
        o,
        w,
        f,
        a
      );
    };
    return l(), () => {
      u = !0, n !== void 0 && window.clearTimeout(n), t.current && (t.current.destroy(), t.current = null);
    };
  }, [o, a]), /* @__PURE__ */ e.createElement("div", { ref: i, style: { width: "100%", height: "100%" } });
}
g("TLFlowDiagram", y);
