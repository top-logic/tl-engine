import { React as e, useTLFieldValue as xe, getComponent as Nt, useTLState as G, useTLCommand as ne, TLChild as K, useTLUpload as Je, useI18N as ae, useTLDataUrl as et, register as z } from "tl-react-bridge";
const { useCallback: Tt } = e, Rt = ({ controlId: l, state: t }) => {
  const [n, r] = xe(), i = Tt(
    (o) => {
      r(o.target.value);
    },
    [r]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, n ?? "");
  const c = t.hasError === !0, s = t.hasWarnings === !0, u = t.errorMessage, a = [
    "tlReactTextInput",
    c ? "tlReactTextInput--error" : "",
    !c && s ? "tlReactTextInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: n ?? "",
      onChange: i,
      disabled: t.disabled === !0,
      className: a,
      "aria-invalid": c || void 0,
      title: c && u ? u : void 0
    }
  ));
}, { useCallback: xt } = e, Lt = ({ controlId: l, state: t, config: n }) => {
  const [r, i] = xe(), c = xt(
    (m) => {
      const p = m.target.value;
      i(p === "" ? null : p);
    },
    [i]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, r != null ? String(r) : "");
  const s = t.hasError === !0, u = t.hasWarnings === !0, a = t.errorMessage, o = [
    "tlReactNumberInput",
    s ? "tlReactNumberInput--error" : "",
    !s && u ? "tlReactNumberInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      inputMode: n != null && n.decimal ? "decimal" : "numeric",
      value: r != null ? String(r) : "",
      onChange: c,
      disabled: t.disabled === !0,
      className: o,
      "aria-invalid": s || void 0,
      title: s && a ? a : void 0
    }
  ));
}, { useCallback: Dt } = e, It = ({ controlId: l, state: t }) => {
  const [n, r] = xe(), i = Dt(
    (a) => {
      r(a.target.value || null);
    },
    [r]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, n ?? "");
  const c = t.hasError === !0, s = t.hasWarnings === !0, u = [
    "tlReactDatePicker",
    c ? "tlReactDatePicker--error" : "",
    !c && s ? "tlReactDatePicker--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: n ?? "",
      onChange: i,
      disabled: t.disabled === !0,
      className: u,
      "aria-invalid": c || void 0
    }
  ));
}, { useCallback: Mt } = e, Pt = ({ controlId: l, state: t, config: n }) => {
  var m;
  const [r, i] = xe(), c = Mt(
    (p) => {
      i(p.target.value || null);
    },
    [i]
  ), s = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const p = ((m = s.find((h) => h.value === r)) == null ? void 0 : m.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, p);
  }
  const u = t.hasError === !0, a = t.hasWarnings === !0, o = [
    "tlReactSelect",
    u ? "tlReactSelect--error" : "",
    !u && a ? "tlReactSelect--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "select",
    {
      value: r ?? "",
      onChange: c,
      disabled: t.disabled === !0,
      className: o,
      "aria-invalid": u || void 0
    },
    t.nullable !== !1 && /* @__PURE__ */ e.createElement("option", { value: "" }),
    s.map((p) => /* @__PURE__ */ e.createElement("option", { key: p.value, value: p.value }, p.label))
  ));
}, { useCallback: jt } = e, At = ({ controlId: l, state: t }) => {
  const [n, r] = xe(), i = jt(
    (a) => {
      r(a.target.checked);
    },
    [r]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement(
      "input",
      {
        type: "checkbox",
        id: l,
        checked: n === !0,
        disabled: !0,
        className: "tlReactCheckbox tlReactCheckbox--immutable"
      }
    );
  const c = t.hasError === !0, s = t.hasWarnings === !0, u = [
    "tlReactCheckbox",
    c ? "tlReactCheckbox--error" : "",
    !c && s ? "tlReactCheckbox--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: l,
      checked: n === !0,
      onChange: i,
      disabled: t.disabled === !0,
      className: u,
      "aria-invalid": c || void 0
    }
  );
}, Bt = ({ controlId: l, state: t }) => {
  const n = t.columns ?? [], r = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: l, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, n.map((i) => /* @__PURE__ */ e.createElement("th", { key: i.name }, i.label)))), /* @__PURE__ */ e.createElement("tbody", null, r.map((i, c) => /* @__PURE__ */ e.createElement("tr", { key: c }, n.map((s) => {
    const u = s.cellModule ? Nt(s.cellModule) : void 0, a = i[s.name];
    if (u) {
      const o = { value: a, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: s.name }, /* @__PURE__ */ e.createElement(
        u,
        {
          controlId: l + "-" + c + "-" + s.name,
          state: o
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: s.name }, a != null ? String(a) : "");
  })))));
};
function _e({ encoded: l, className: t }) {
  if (l.startsWith("css:")) {
    const n = l.substring(4);
    return /* @__PURE__ */ e.createElement("i", { className: n + (t ? " " + t : "") });
  }
  if (l.startsWith("colored:")) {
    const n = l.substring(8);
    return /* @__PURE__ */ e.createElement("i", { className: n + (t ? " " + t : "") });
  }
  return l.startsWith("/") || l.startsWith("theme:") ? /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: t, style: { width: "1em", height: "1em" } }) : /* @__PURE__ */ e.createElement("i", { className: l + (t ? " " + t : "") });
}
const { useCallback: $t } = e, Ot = ({ controlId: l, command: t, label: n, image: r, disabled: i, displayMode: c }) => {
  const s = G(), u = ne(), a = t ?? "click", o = n ?? s.label, m = r ?? s.image, p = i ?? s.disabled === !0, h = c ?? s.displayMode ?? "label-only", N = s.hidden === !0, f = s.tooltip, T = N ? { display: "none" } : void 0, w = $t(() => {
    u(a);
  }, [u, a]), v = h === "icon-only", C = h === "icon-only" || h === "icon-label", D = h === "label-only" || h === "icon-label" || v && !m, M = f ?? (v ? o : void 0), b = M ? `text:${M}` : void 0;
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: w,
      disabled: p,
      style: T,
      className: "tlReactButton" + (v ? " tlReactButton--iconOnly" : ""),
      "data-tooltip": b,
      "aria-label": v ? o : void 0
    },
    C && m && /* @__PURE__ */ e.createElement(_e, { encoded: m, className: "tlReactButton__image" }),
    D && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, o)
  );
}, { useCallback: Ft } = e, Ht = ({ controlId: l, command: t, label: n, active: r, disabled: i }) => {
  const c = G(), s = ne(), u = t ?? "click", a = n ?? c.label, o = r ?? c.active === !0, m = i ?? c.disabled === !0, p = Ft(() => {
    s(u);
  }, [s, u]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: p,
      disabled: m,
      className: "tlReactButton" + (o ? " tlReactButtonActive" : "")
    },
    a
  );
}, Wt = ({ controlId: l }) => {
  const t = G(), n = ne(), r = t.count ?? 0, i = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, r), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: zt } = e, Ut = ({ controlId: l }) => {
  const t = G(), n = ne(), r = t.tabs ?? [], i = t.activeTabId, c = zt((s) => {
    s !== i && n("selectTab", { tabId: s });
  }, [n, i]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, r.map((s) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: s.id,
      role: "tab",
      "aria-selected": s.id === i,
      className: "tlReactTabBar__tab" + (s.id === i ? " tlReactTabBar__tab--active" : ""),
      onClick: () => c(s.id)
    },
    s.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(K, { control: t.activeContent })));
}, Vt = ({ controlId: l }) => {
  const t = G(), n = t.title, r = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, r.map((i, c) => /* @__PURE__ */ e.createElement("div", { key: c, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(K, { control: i })))));
}, Kt = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Yt = ({ controlId: l }) => {
  const t = G(), n = Je(), [r, i] = e.useState("idle"), [c, s] = e.useState(null), u = e.useRef(null), a = e.useRef([]), o = e.useRef(null), m = t.status ?? "idle", p = t.error, h = m === "received" ? "idle" : r !== "idle" ? r : m, N = e.useCallback(async () => {
    if (r === "recording") {
      const C = u.current;
      C && C.state !== "inactive" && C.stop();
      return;
    }
    if (r !== "uploading") {
      if (s(null), !window.isSecureContext || !navigator.mediaDevices) {
        s("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const C = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        o.current = C, a.current = [];
        const D = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", M = new MediaRecorder(C, D ? { mimeType: D } : void 0);
        u.current = M, M.ondataavailable = (b) => {
          b.data.size > 0 && a.current.push(b.data);
        }, M.onstop = async () => {
          C.getTracks().forEach((E) => E.stop()), o.current = null;
          const b = new Blob(a.current, { type: M.mimeType || "audio/webm" });
          if (a.current = [], b.size === 0) {
            i("idle");
            return;
          }
          i("uploading");
          const _ = new FormData();
          _.append("audio", b, "recording.webm"), await n(_), i("idle");
        }, M.start(), i("recording");
      } catch (C) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", C), s("js.audioRecorder.error.denied"), i("idle");
      }
    }
  }, [r, n]), f = ae(Kt), T = h === "recording" ? f["js.audioRecorder.stop"] : h === "uploading" ? f["js.uploading"] : f["js.audioRecorder.record"], w = h === "uploading", v = ["tlAudioRecorder__button"];
  return h === "recording" && v.push("tlAudioRecorder__button--recording"), h === "uploading" && v.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: v.join(" "),
      onClick: N,
      disabled: w,
      title: T,
      "aria-label": T
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${h === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, f[c]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, Gt = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Xt = ({ controlId: l }) => {
  const t = G(), n = et(), r = !!t.hasAudio, i = t.dataRevision ?? 0, [c, s] = e.useState(r ? "idle" : "disabled"), u = e.useRef(null), a = e.useRef(null), o = e.useRef(i);
  e.useEffect(() => {
    r ? c === "disabled" && s("idle") : (u.current && (u.current.pause(), u.current = null), a.current && (URL.revokeObjectURL(a.current), a.current = null), s("disabled"));
  }, [r]), e.useEffect(() => {
    i !== o.current && (o.current = i, u.current && (u.current.pause(), u.current = null), a.current && (URL.revokeObjectURL(a.current), a.current = null), (c === "playing" || c === "paused" || c === "loading") && s("idle"));
  }, [i]), e.useEffect(() => () => {
    u.current && (u.current.pause(), u.current = null), a.current && (URL.revokeObjectURL(a.current), a.current = null);
  }, []);
  const m = e.useCallback(async () => {
    if (c === "disabled" || c === "loading")
      return;
    if (c === "playing") {
      u.current && u.current.pause(), s("paused");
      return;
    }
    if (c === "paused" && u.current) {
      u.current.play(), s("playing");
      return;
    }
    if (!a.current) {
      s("loading");
      try {
        const w = await fetch(n);
        if (!w.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", w.status), s("idle");
          return;
        }
        const v = await w.blob();
        a.current = URL.createObjectURL(v);
      } catch (w) {
        console.error("[TLAudioPlayer] Fetch error:", w), s("idle");
        return;
      }
    }
    const T = new Audio(a.current);
    u.current = T, T.onended = () => {
      s("idle");
    }, T.play(), s("playing");
  }, [c, n]), p = ae(Gt), h = c === "loading" ? p["js.loading"] : c === "playing" ? p["js.audioPlayer.pause"] : c === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], N = c === "disabled" || c === "loading", f = ["tlAudioPlayer__button"];
  return c === "playing" && f.push("tlAudioPlayer__button--playing"), c === "loading" && f.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: f.join(" "),
      onClick: m,
      disabled: N,
      title: h,
      "aria-label": h
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${c === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, qt = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Zt = ({ controlId: l }) => {
  const t = G(), n = Je(), [r, i] = e.useState("idle"), [c, s] = e.useState(!1), u = e.useRef(null), a = t.status ?? "idle", o = t.error, m = t.accept ?? "", p = a === "received" ? "idle" : r !== "idle" ? r : a, h = e.useCallback(async (b) => {
    i("uploading");
    const _ = new FormData();
    _.append("file", b, b.name), await n(_), i("idle");
  }, [n]), N = e.useCallback((b) => {
    var E;
    const _ = (E = b.target.files) == null ? void 0 : E[0];
    _ && h(_);
  }, [h]), f = e.useCallback(() => {
    var b;
    r !== "uploading" && ((b = u.current) == null || b.click());
  }, [r]), T = e.useCallback((b) => {
    b.preventDefault(), b.stopPropagation(), s(!0);
  }, []), w = e.useCallback((b) => {
    b.preventDefault(), b.stopPropagation(), s(!1);
  }, []), v = e.useCallback((b) => {
    var E;
    if (b.preventDefault(), b.stopPropagation(), s(!1), r === "uploading") return;
    const _ = (E = b.dataTransfer.files) == null ? void 0 : E[0];
    _ && h(_);
  }, [r, h]), C = p === "uploading", D = ae(qt), M = p === "uploading" ? D["js.uploading"] : D["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${c ? " tlFileUpload--dragover" : ""}`,
      onDragOver: T,
      onDragLeave: w,
      onDrop: v
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: u,
        type: "file",
        accept: m || void 0,
        onChange: N,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (p === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: f,
        disabled: C,
        title: M,
        "aria-label": M
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    o && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, o)
  );
}, Qt = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Jt = ({ controlId: l }) => {
  const t = G(), n = et(), r = ne(), i = !!t.hasData, c = t.dataRevision ?? 0, s = t.fileName ?? "download", u = !!t.clearable, [a, o] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!i || a)) {
      o(!0);
      try {
        const f = n + (n.includes("?") ? "&" : "?") + "rev=" + c, T = await fetch(f);
        if (!T.ok) {
          console.error("[TLDownload] Failed to fetch data:", T.status);
          return;
        }
        const w = await T.blob(), v = URL.createObjectURL(w), C = document.createElement("a");
        C.href = v, C.download = s, C.style.display = "none", document.body.appendChild(C), C.click(), document.body.removeChild(C), URL.revokeObjectURL(v);
      } catch (f) {
        console.error("[TLDownload] Fetch error:", f);
      } finally {
        o(!1);
      }
    }
  }, [i, a, n, c, s]), p = e.useCallback(async () => {
    i && await r("clear");
  }, [i, r]), h = ae(Qt);
  if (!i)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, h["js.download.noFile"]));
  const N = a ? h["js.downloading"] : h["js.download.file"].replace("{0}", s);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (a ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: m,
      disabled: a,
      title: N,
      "aria-label": N
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: s }, s), u && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: p,
      title: h["js.download.clear"],
      "aria-label": h["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, en = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, tn = ({ controlId: l }) => {
  const t = G(), n = Je(), [r, i] = e.useState("idle"), [c, s] = e.useState(null), [u, a] = e.useState(!1), o = e.useRef(null), m = e.useRef(null), p = e.useRef(null), h = e.useRef(null), N = e.useRef(null), f = t.error, T = e.useMemo(
    () => {
      var S;
      return !!(window.isSecureContext && ((S = navigator.mediaDevices) != null && S.getUserMedia));
    },
    []
  ), w = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((S) => S.stop()), m.current = null), o.current && (o.current.srcObject = null);
  }, []), v = e.useCallback(() => {
    w(), i("idle");
  }, [w]), C = e.useCallback(async () => {
    var S;
    if (r !== "uploading") {
      if (s(null), !T) {
        (S = h.current) == null || S.click();
        return;
      }
      try {
        const B = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        m.current = B, i("overlayOpen");
      } catch (B) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", B), s("js.photoCapture.error.denied"), i("idle");
      }
    }
  }, [r, T]), D = e.useCallback(async () => {
    if (r !== "overlayOpen")
      return;
    const S = o.current, B = p.current;
    if (!S || !B)
      return;
    B.width = S.videoWidth, B.height = S.videoHeight;
    const x = B.getContext("2d");
    x && (x.drawImage(S, 0, 0), w(), i("uploading"), B.toBlob(async ($) => {
      if (!$) {
        i("idle");
        return;
      }
      const q = new FormData();
      q.append("photo", $, "capture.jpg"), await n(q), i("idle");
    }, "image/jpeg", 0.85));
  }, [r, n, w]), M = e.useCallback(async (S) => {
    var $;
    const B = ($ = S.target.files) == null ? void 0 : $[0];
    if (!B) return;
    i("uploading");
    const x = new FormData();
    x.append("photo", B, B.name), await n(x), i("idle"), h.current && (h.current.value = "");
  }, [n]);
  e.useEffect(() => {
    r === "overlayOpen" && o.current && m.current && (o.current.srcObject = m.current);
  }, [r]), e.useEffect(() => {
    var B;
    if (r !== "overlayOpen") return;
    (B = N.current) == null || B.focus();
    const S = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = S;
    };
  }, [r]), e.useEffect(() => {
    if (r !== "overlayOpen") return;
    const S = (B) => {
      B.key === "Escape" && v();
    };
    return document.addEventListener("keydown", S), () => document.removeEventListener("keydown", S);
  }, [r, v]), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((S) => S.stop()), m.current = null);
  }, []);
  const b = ae(en), _ = r === "uploading" ? b["js.uploading"] : b["js.photoCapture.open"], E = ["tlPhotoCapture__cameraBtn"];
  r === "uploading" && E.push("tlPhotoCapture__cameraBtn--uploading");
  const W = ["tlPhotoCapture__overlayVideo"];
  u && W.push("tlPhotoCapture__overlayVideo--mirrored");
  const k = ["tlPhotoCapture__mirrorBtn"];
  return u && k.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: E.join(" "),
      onClick: C,
      disabled: r === "uploading",
      title: _,
      "aria-label": _
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !T && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: h,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: M
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: p, style: { display: "none" } }), r === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: N,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: v }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: o,
        className: W.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: k.join(" "),
        onClick: () => a((S) => !S),
        title: b["js.photoCapture.mirror"],
        "aria-label": b["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: D,
        title: b["js.photoCapture.capture"],
        "aria-label": b["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: v,
        title: b["js.photoCapture.close"],
        "aria-label": b["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, b[c]), f && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, f));
}, nn = {
  "js.photoViewer.alt": "Captured photo"
}, ln = ({ controlId: l }) => {
  const t = G(), n = et(), r = !!t.hasPhoto, i = t.dataRevision ?? 0, [c, s] = e.useState(null), u = e.useRef(i);
  e.useEffect(() => {
    if (!r) {
      c && (URL.revokeObjectURL(c), s(null));
      return;
    }
    if (i === u.current && c)
      return;
    u.current = i, c && (URL.revokeObjectURL(c), s(null));
    let o = !1;
    return (async () => {
      try {
        const m = await fetch(n);
        if (!m.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", m.status);
          return;
        }
        const p = await m.blob();
        o || s(URL.createObjectURL(p));
      } catch (m) {
        console.error("[TLPhotoViewer] Fetch error:", m);
      }
    })(), () => {
      o = !0;
    };
  }, [r, i, n]), e.useEffect(() => () => {
    c && URL.revokeObjectURL(c);
  }, []);
  const a = ae(nn);
  return !r || !c ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: c,
      alt: a["js.photoViewer.alt"]
    }
  ));
}, { useCallback: ot, useRef: Fe } = e, rn = ({ controlId: l }) => {
  const t = G(), n = ne(), r = t.orientation, i = t.resizable === !0, c = t.children ?? [], s = r === "horizontal", u = c.length > 0 && c.every((w) => w.collapsed), a = !u && c.some((w) => w.collapsed), o = u ? !s : s, m = Fe(null), p = Fe(null), h = Fe(null), N = ot((w, v) => {
    const C = {
      overflow: w.scrolling || "auto"
    };
    return w.collapsed ? u && !o ? C.flex = "1 0 0%" : C.flex = "0 0 auto" : v !== void 0 ? C.flex = `0 0 ${v}px` : C.flex = `${w.size} 1 0%`, w.minSize > 0 && !w.collapsed && (C.minWidth = s ? w.minSize : void 0, C.minHeight = s ? void 0 : w.minSize), C;
  }, [s, u, a, o]), f = ot((w, v) => {
    w.preventDefault();
    const C = m.current;
    if (!C) return;
    const D = c[v], M = c[v + 1], b = C.querySelectorAll(":scope > .tlSplitPanel__child"), _ = [];
    b.forEach((k) => {
      _.push(s ? k.offsetWidth : k.offsetHeight);
    }), h.current = _, p.current = {
      splitterIndex: v,
      startPos: s ? w.clientX : w.clientY,
      startSizeBefore: _[v],
      startSizeAfter: _[v + 1],
      childBefore: D,
      childAfter: M
    };
    const E = (k) => {
      const S = p.current;
      if (!S || !h.current) return;
      const x = (s ? k.clientX : k.clientY) - S.startPos, $ = S.childBefore.minSize || 0, q = S.childAfter.minSize || 0;
      let le = S.startSizeBefore + x, Q = S.startSizeAfter - x;
      le < $ && (Q += le - $, le = $), Q < q && (le += Q - q, Q = q), h.current[S.splitterIndex] = le, h.current[S.splitterIndex + 1] = Q;
      const J = C.querySelectorAll(":scope > .tlSplitPanel__child"), A = J[S.splitterIndex], j = J[S.splitterIndex + 1];
      A && (A.style.flex = `0 0 ${le}px`), j && (j.style.flex = `0 0 ${Q}px`);
    }, W = () => {
      if (document.removeEventListener("mousemove", E), document.removeEventListener("mouseup", W), document.body.style.cursor = "", document.body.style.userSelect = "", h.current) {
        const k = {};
        c.forEach((S, B) => {
          const x = S.control;
          x != null && x.controlId && h.current && (k[x.controlId] = h.current[B]);
        }), n("updateSizes", { sizes: k });
      }
      h.current = null, p.current = null;
    };
    document.addEventListener("mousemove", E), document.addEventListener("mouseup", W), document.body.style.cursor = s ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [c, s, n]), T = [];
  return c.forEach((w, v) => {
    if (T.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${v}`,
          className: `tlSplitPanel__child${w.collapsed && o ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: N(w)
        },
        /* @__PURE__ */ e.createElement(K, { control: w.control })
      )
    ), i && v < c.length - 1) {
      const C = c[v + 1];
      !w.collapsed && !C.collapsed && T.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${v}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${r}`,
            onMouseDown: (M) => f(M, v)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: m,
      id: l,
      className: `tlSplitPanel tlSplitPanel--${r}${u ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: o ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    T
  );
}, { useCallback: He } = e, on = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, an = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), sn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), cn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), un = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), dn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), mn = ({ controlId: l }) => {
  const t = G(), n = ne(), r = ae(on), i = t.title, c = t.expansionState ?? "NORMALIZED", s = t.showMinimize === !0, u = t.showMaximize === !0, a = t.showPopOut === !0, o = t.fullLine === !0, m = c === "MINIMIZED", p = c === "MAXIMIZED", h = c === "HIDDEN", N = He(() => {
    n("toggleMinimize");
  }, [n]), f = He(() => {
    n("toggleMaximize");
  }, [n]), T = He(() => {
    n("popOut");
  }, [n]);
  if (h)
    return null;
  const w = p ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${c.toLowerCase()}${o ? " tlPanel--fullLine" : ""}`,
      style: w
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(K, { control: t.toolbar }), s && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: N,
        title: m ? r["js.panel.restore"] : r["js.panel.minimize"]
      },
      m ? /* @__PURE__ */ e.createElement(sn, null) : /* @__PURE__ */ e.createElement(an, null)
    ), u && !m && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: f,
        title: p ? r["js.panel.restore"] : r["js.panel.maximize"]
      },
      p ? /* @__PURE__ */ e.createElement(un, null) : /* @__PURE__ */ e.createElement(cn, null)
    ), a && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: T,
        title: r["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(dn, null)
    ))),
    !m && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(K, { control: t.child })),
    !m && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(K, { control: t.buttonBar }))
  );
}, pn = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(K, { control: t.child })
  );
}, fn = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(K, { control: t.activeChild }));
}, { useCallback: de, useState: Be, useEffect: $e, useRef: Oe } = e, hn = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function qe(l, t, n, r) {
  const i = [];
  for (const c of l)
    if (c.type === "nav") {
      if (c.hidden) continue;
      i.push({ id: c.id, type: "nav", groupId: r });
    } else c.type === "command" ? i.push({ id: c.id, type: "command", groupId: r }) : c.type === "group" && (i.push({ id: c.id, type: "group" }), (n.get(c.id) ?? c.expanded) && !t && i.push(...qe(c.children, t, n, c.id)));
  return i;
}
const Ne = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(_e, { encoded: l, className: "tlSidebar__icon" }) : null, _n = ({ item: l, active: t, collapsed: n, onSelect: r, tabIndex: i, itemRef: c, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => r(l.id),
    title: n ? l.label : void 0,
    tabIndex: i,
    ref: c,
    onFocus: () => s(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Ne, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Ne, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), bn = ({ item: l, collapsed: t, onExecute: n, tabIndex: r, itemRef: i, onFocus: c }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: r,
    ref: i,
    onFocus: () => c(l.id)
  },
  /* @__PURE__ */ e.createElement(Ne, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), vn = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Ne, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), En = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), gn = ({ item: l, activeItemId: t, anchorRect: n, onSelect: r, onExecute: i, onClose: c }) => {
  const s = Oe(null);
  $e(() => {
    const o = (m) => {
      s.current && !s.current.contains(m.target) && setTimeout(() => c(), 0);
    };
    return document.addEventListener("mousedown", o), () => document.removeEventListener("mousedown", o);
  }, [c]), $e(() => {
    const o = (m) => {
      m.key === "Escape" && c();
    };
    return document.addEventListener("keydown", o), () => document.removeEventListener("keydown", o);
  }, [c]);
  const u = de((o) => {
    o.type === "nav" ? (r(o.id), c()) : o.type === "command" && (i(o.id), c());
  }, [r, i, c]), a = {};
  return n && (a.left = n.right, a.top = n.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: s, role: "menu", style: a }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((o) => {
    if (o.type === "nav" && o.hidden) return null;
    if (o.type === "nav" || o.type === "command") {
      const m = o.type === "nav" && o.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: o.id,
          className: "tlSidebar__flyoutItem" + (m ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => u(o)
        },
        /* @__PURE__ */ e.createElement(Ne, { icon: o.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, o.label),
        o.type === "nav" && o.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, o.badge)
      );
    }
    return o.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: o.id, className: "tlSidebar__flyoutSectionHeader" }, o.label) : o.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: o.id, className: "tlSidebar__separator" }) : null;
  }));
}, Cn = ({
  item: l,
  expanded: t,
  activeItemId: n,
  collapsed: r,
  onSelect: i,
  onExecute: c,
  onToggleGroup: s,
  tabIndex: u,
  itemRef: a,
  onFocus: o,
  focusedId: m,
  setItemRef: p,
  onItemFocus: h,
  flyoutGroupId: N,
  onOpenFlyout: f,
  onCloseFlyout: T
}) => {
  const w = Oe(null), [v, C] = Be(null), D = de(() => {
    r ? N === l.id ? T() : (w.current && C(w.current.getBoundingClientRect()), f(l.id)) : s(l.id);
  }, [r, N, l.id, s, f, T]), M = de((_) => {
    w.current = _, a(_);
  }, [a]), b = r && N === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (b ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: D,
      title: r ? l.label : void 0,
      "aria-expanded": r ? b : t,
      tabIndex: u,
      ref: M,
      onFocus: () => o(l.id)
    },
    /* @__PURE__ */ e.createElement(Ne, { icon: l.icon }),
    !r && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
    !r && /* @__PURE__ */ e.createElement(
      "svg",
      {
        className: "tlSidebar__chevron" + (t ? " tlSidebar__chevron--open" : ""),
        viewBox: "0 0 16 16",
        width: "16",
        height: "16",
        "aria-hidden": "true"
      },
      /* @__PURE__ */ e.createElement(
        "path",
        {
          d: "M4 6l4 4 4-4",
          fill: "none",
          stroke: "currentColor",
          strokeWidth: "2",
          strokeLinecap: "round",
          strokeLinejoin: "round"
        }
      )
    )
  ), b && /* @__PURE__ */ e.createElement(
    gn,
    {
      item: l,
      activeItemId: n,
      anchorRect: v,
      onSelect: i,
      onExecute: c,
      onClose: T
    }
  ), t && !r && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((_) => /* @__PURE__ */ e.createElement(
    Et,
    {
      key: _.id,
      item: _,
      activeItemId: n,
      collapsed: r,
      onSelect: i,
      onExecute: c,
      onToggleGroup: s,
      focusedId: m,
      setItemRef: p,
      onItemFocus: h,
      groupStates: null,
      flyoutGroupId: N,
      onOpenFlyout: f,
      onCloseFlyout: T
    }
  ))));
}, Et = ({
  item: l,
  activeItemId: t,
  collapsed: n,
  onSelect: r,
  onExecute: i,
  onToggleGroup: c,
  focusedId: s,
  setItemRef: u,
  onItemFocus: a,
  groupStates: o,
  flyoutGroupId: m,
  onOpenFlyout: p,
  onCloseFlyout: h
}) => {
  switch (l.type) {
    case "nav":
      return l.hidden ? null : /* @__PURE__ */ e.createElement(
        _n,
        {
          item: l,
          active: l.id === t,
          collapsed: n,
          onSelect: r,
          tabIndex: s === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: a
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        bn,
        {
          item: l,
          collapsed: n,
          onExecute: i,
          tabIndex: s === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: a
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(vn, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(En, null);
    case "group": {
      const N = o ? o.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        Cn,
        {
          item: l,
          expanded: N,
          activeItemId: t,
          collapsed: n,
          onSelect: r,
          onExecute: i,
          onToggleGroup: c,
          tabIndex: s === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: a,
          focusedId: s,
          setItemRef: u,
          onItemFocus: a,
          flyoutGroupId: m,
          onOpenFlyout: p,
          onCloseFlyout: h
        }
      );
    }
    default:
      return null;
  }
}, yn = ({ controlId: l }) => {
  const t = G(), n = ne(), r = ae(hn), i = t.items ?? [], c = t.activeItemId, s = t.collapsed, [u, a] = Be(() => {
    const k = /* @__PURE__ */ new Map(), S = (B) => {
      for (const x of B)
        x.type === "group" && (k.set(x.id, x.expanded), S(x.children));
    };
    return S(i), k;
  }), o = de((k) => {
    a((S) => {
      const B = new Map(S), x = B.get(k) ?? !1;
      return B.set(k, !x), n("toggleGroup", { itemId: k, expanded: !x }), B;
    });
  }, [n]), m = de((k) => {
    k !== c && n("selectItem", { itemId: k });
  }, [n, c]), p = de((k) => {
    n("executeCommand", { itemId: k });
  }, [n]), h = de(() => {
    n("toggleCollapse", {});
  }, [n]), [N, f] = Be(null), T = de((k) => {
    f(k);
  }, []), w = de(() => {
    f(null);
  }, []);
  $e(() => {
    s || f(null);
  }, [s]);
  const [v, C] = Be(() => {
    const k = qe(i, s, u);
    return k.length > 0 ? k[0].id : "";
  }), D = Oe(/* @__PURE__ */ new Map()), M = de((k) => (S) => {
    S ? D.current.set(k, S) : D.current.delete(k);
  }, []), b = de((k) => {
    C(k);
  }, []), _ = Oe(0), E = de((k) => {
    C(k), _.current++;
  }, []);
  $e(() => {
    const k = D.current.get(v);
    k && document.activeElement !== k && k.focus();
  }, [v, _.current]);
  const W = de((k) => {
    if (k.key === "Escape" && N !== null) {
      k.preventDefault(), w();
      return;
    }
    const S = qe(i, s, u);
    if (S.length === 0) return;
    const B = S.findIndex(($) => $.id === v);
    if (B < 0) return;
    const x = S[B];
    switch (k.key) {
      case "ArrowDown": {
        k.preventDefault();
        const $ = (B + 1) % S.length;
        E(S[$].id);
        break;
      }
      case "ArrowUp": {
        k.preventDefault();
        const $ = (B - 1 + S.length) % S.length;
        E(S[$].id);
        break;
      }
      case "Home": {
        k.preventDefault(), E(S[0].id);
        break;
      }
      case "End": {
        k.preventDefault(), E(S[S.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        k.preventDefault(), x.type === "nav" ? m(x.id) : x.type === "command" ? p(x.id) : x.type === "group" && (s ? N === x.id ? w() : T(x.id) : o(x.id));
        break;
      }
      case "ArrowRight": {
        x.type === "group" && !s && ((u.get(x.id) ?? !1) || (k.preventDefault(), o(x.id)));
        break;
      }
      case "ArrowLeft": {
        x.type === "group" && !s && (u.get(x.id) ?? !1) && (k.preventDefault(), o(x.id));
        break;
      }
    }
  }, [
    i,
    s,
    u,
    v,
    N,
    E,
    m,
    p,
    o,
    T,
    w
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSidebar" + (s ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": r["js.sidebar.ariaLabel"] }, s ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: W }, i.map((k) => /* @__PURE__ */ e.createElement(
    Et,
    {
      key: k.id,
      item: k,
      activeItemId: c,
      collapsed: s,
      onSelect: m,
      onExecute: p,
      onToggleGroup: o,
      focusedId: v,
      setItemRef: M,
      onItemFocus: b,
      groupStates: u,
      flyoutGroupId: N,
      onOpenFlyout: T,
      onCloseFlyout: w
    }
  ))), s ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: h,
      title: s ? r["js.sidebar.expand"] : r["js.sidebar.collapse"]
    },
    /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement(
      "path",
      {
        d: s ? "M6 4l4 4-4 4" : "M10 4l-4 4 4 4",
        fill: "none",
        stroke: "currentColor",
        strokeWidth: "2",
        strokeLinecap: "round",
        strokeLinejoin: "round"
      }
    ))
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(K, { control: t.activeContent })));
}, wn = ({ controlId: l }) => {
  const t = G(), n = t.direction ?? "column", r = t.gap ?? "default", i = t.align ?? "stretch", c = t.wrap === !0, s = t.children ?? [], u = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${r}`,
    `tlStack--align-${i}`,
    c ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: u }, s.map((a, o) => /* @__PURE__ */ e.createElement(K, { key: o, control: a })));
}, kn = ({ controlId: l }) => {
  const t = G(), n = t.columns, r = t.minColumnWidth, i = t.gap ?? "default", c = t.children ?? [], s = {};
  return r ? s.gridTemplateColumns = `repeat(auto-fit, minmax(${r}, 1fr))` : n && (s.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${i}`, style: s }, c.map((u, a) => /* @__PURE__ */ e.createElement(K, { key: a, control: u })));
}, Sn = ({ controlId: l }) => {
  const t = G(), n = t.title, r = t.variant ?? "outlined", i = t.padding ?? "default", c = t.headerActions ?? [], s = t.child, u = n != null || c.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${r}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, c.map((a, o) => /* @__PURE__ */ e.createElement(K, { key: o, control: a })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${i}` }, /* @__PURE__ */ e.createElement(K, { control: s })));
}, Nn = ({ controlId: l }) => {
  const t = G(), n = t.title ?? "", r = t.leading, i = t.actions ?? [], c = t.variant ?? "flat", u = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    c === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: u }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(K, { control: r })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, i.map((a, o) => /* @__PURE__ */ e.createElement(K, { key: o, control: a }))));
}, { useCallback: Tn } = e, Rn = ({ controlId: l }) => {
  const t = G(), n = ne(), r = t.items ?? [], i = Tn((c) => {
    n("navigate", { itemId: c });
  }, [n]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, r.map((c, s) => {
    const u = s === r.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: c.id, className: "tlBreadcrumb__entry" }, s > 0 && /* @__PURE__ */ e.createElement(
      "svg",
      {
        className: "tlBreadcrumb__separator",
        viewBox: "0 0 16 16",
        width: "16",
        height: "16",
        "aria-hidden": "true"
      },
      /* @__PURE__ */ e.createElement(
        "path",
        {
          d: "M6 4l4 4-4 4",
          fill: "none",
          stroke: "currentColor",
          strokeWidth: "2",
          strokeLinecap: "round",
          strokeLinejoin: "round"
        }
      )
    ), u ? /* @__PURE__ */ e.createElement("span", { className: "tlBreadcrumb__current", "aria-current": "page" }, c.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlBreadcrumb__item",
        onClick: () => i(c.id)
      },
      c.label
    ));
  })));
}, { useCallback: xn } = e, Ln = ({ controlId: l }) => {
  const t = G(), n = ne(), r = t.items ?? [], i = t.activeItemId, c = xn((s) => {
    s !== i && n("selectItem", { itemId: s });
  }, [n, i]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, r.map((s) => {
    const u = s.id === i;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: s.id,
        type: "button",
        className: "tlBottomBar__item" + (u ? " tlBottomBar__item--active" : ""),
        onClick: () => c(s.id),
        "aria-current": u ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement(_e, { encoded: s.icon, className: "tlBottomBar__icon" }), s.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, s.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, s.label)
    );
  }));
}, { useCallback: at, useEffect: st, useRef: Dn } = e, In = ({ controlId: l }) => {
  const t = G(), n = ne(), r = t.open === !0, i = t.closeOnBackdrop !== !1, c = t.child, s = Dn(null), u = at(() => {
    n("close");
  }, [n]), a = at((o) => {
    i && o.target === o.currentTarget && u();
  }, [i, u]);
  return st(() => {
    if (!r) return;
    const o = (m) => {
      m.key === "Escape" && u();
    };
    return document.addEventListener("keydown", o), () => document.removeEventListener("keydown", o);
  }, [r, u]), st(() => {
    r && s.current && s.current.focus();
  }, [r]), r ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: a,
      ref: s,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(K, { control: c })
  ) : null;
}, { useEffect: Mn, useRef: Pn } = e, jn = ({ controlId: l }) => {
  const n = G().dialogs ?? [], r = Pn(n.length);
  return Mn(() => {
    n.length < r.current && n.length > 0, r.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((i) => /* @__PURE__ */ e.createElement(K, { key: i.controlId, control: i })));
}, { useCallback: De, useRef: ye, useState: Ie } = e, An = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Bn = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], $n = ({ controlId: l }) => {
  const t = G(), n = ne(), r = ae(An), i = t.title ?? "", c = t.width ?? "32rem", s = t.height ?? null, u = t.minHeight ?? null, a = t.resizable === !0, o = t.child, m = t.actions ?? [], p = t.toolbar, h = t.buttonBar, [N, f] = Ie(null), [T, w] = Ie(null), [v, C] = Ie(null), D = ye(null), [M, b] = Ie(!1), _ = ye(null), E = ye(null), W = ye(null), k = ye(null), S = ye(null), B = De(() => {
    n("close");
  }, [n]), x = De((J, A) => {
    A.preventDefault();
    const j = k.current;
    if (!j) return;
    const U = j.getBoundingClientRect(), d = !D.current, y = D.current ?? { x: U.left, y: U.top };
    d && (D.current = y, C(y)), S.current = {
      dir: J,
      startX: A.clientX,
      startY: A.clientY,
      startW: U.width,
      startH: U.height,
      startPos: { ...y },
      symmetric: d
    };
    const F = (V) => {
      const P = S.current;
      if (!P) return;
      const X = V.clientX - P.startX, te = V.clientY - P.startY;
      let ee = P.startW, ie = P.startH, ue = 0, me = 0;
      P.symmetric ? (P.dir.includes("e") && (ee = P.startW + 2 * X), P.dir.includes("w") && (ee = P.startW - 2 * X), P.dir.includes("s") && (ie = P.startH + 2 * te), P.dir.includes("n") && (ie = P.startH - 2 * te)) : (P.dir.includes("e") && (ee = P.startW + X), P.dir.includes("w") && (ee = P.startW - X, ue = X), P.dir.includes("s") && (ie = P.startH + te), P.dir.includes("n") && (ie = P.startH - te, me = te));
      const he = Math.max(200, ee), be = Math.max(100, ie);
      P.symmetric ? (ue = (P.startW - he) / 2, me = (P.startH - be) / 2) : (P.dir.includes("w") && he === 200 && (ue = P.startW - 200), P.dir.includes("n") && be === 100 && (me = P.startH - 100)), E.current = he, W.current = be, f(he), w(be);
      const R = {
        x: P.startPos.x + ue,
        y: P.startPos.y + me
      };
      D.current = R, C(R);
    }, O = () => {
      document.removeEventListener("mousemove", F), document.removeEventListener("mouseup", O);
      const V = E.current, P = W.current;
      (V != null || P != null) && n("resize", {
        ...V != null ? { width: Math.round(V) + "px" } : {},
        ...P != null ? { height: Math.round(P) + "px" } : {}
      }), S.current = null;
    };
    document.addEventListener("mousemove", F), document.addEventListener("mouseup", O);
  }, [n]), $ = De((J) => {
    if (J.button !== 0 || J.target.closest("button")) return;
    J.preventDefault();
    const A = k.current;
    if (!A) return;
    const j = A.getBoundingClientRect(), U = D.current ?? { x: j.left, y: j.top }, d = J.clientX - U.x, y = J.clientY - U.y, F = (V) => {
      const P = window.innerWidth, X = window.innerHeight;
      let te = V.clientX - d, ee = V.clientY - y;
      const ie = A.offsetWidth, ue = A.offsetHeight;
      te + ie > P && (te = P - ie), ee + ue > X && (ee = X - ue), te < 0 && (te = 0), ee < 0 && (ee = 0);
      const me = { x: te, y: ee };
      D.current = me, C(me);
    }, O = () => {
      document.removeEventListener("mousemove", F), document.removeEventListener("mouseup", O);
    };
    document.addEventListener("mousemove", F), document.addEventListener("mouseup", O);
  }, []), q = De(() => {
    var J, A;
    if (M) {
      const j = _.current;
      j && (C(j.x !== -1 ? { x: j.x, y: j.y } : null), f(j.w), w(j.h)), b(!1);
    } else {
      const j = k.current, U = j == null ? void 0 : j.getBoundingClientRect();
      _.current = {
        x: ((J = D.current) == null ? void 0 : J.x) ?? (U == null ? void 0 : U.left) ?? -1,
        y: ((A = D.current) == null ? void 0 : A.y) ?? (U == null ? void 0 : U.top) ?? -1,
        w: N ?? (U == null ? void 0 : U.width) ?? null,
        h: T ?? null
      }, b(!0), C({ x: 0, y: 0 }), f(null), w(null);
    }
  }, [M, N, T]), le = M ? { position: "absolute", top: 0, left: 0, width: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: N != null ? N + "px" : c,
    ...T != null ? { height: T + "px" } : s != null ? { height: s } : {},
    ...u != null && T == null ? { minHeight: u } : {},
    maxHeight: v ? "100vh" : "80vh",
    ...v ? { position: "absolute", left: v.x + "px", top: v.y + "px" } : {}
  }, Q = l + "-title";
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: le,
      ref: k,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": Q
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${M ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: M ? void 0 : $,
        onDoubleClick: q
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: Q }, i),
      p && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(K, { control: p })),
      /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: q,
          title: M ? r["js.window.restore"] : r["js.window.maximize"]
        },
        M ? (
          // Restore icon: two overlapping squares.
          /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24", width: "18", height: "18", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1.5", fill: "none", stroke: "currentColor", strokeWidth: "2" }), /* @__PURE__ */ e.createElement("path", { d: "M8 8V5.5A1.5 1.5 0 0 1 9.5 4H18.5A1.5 1.5 0 0 1 20 5.5V14.5A1.5 1.5 0 0 1 18.5 16H16", fill: "none", stroke: "currentColor", strokeWidth: "2" }))
        ) : (
          // Maximize icon: single square.
          /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24", width: "18", height: "18", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1.5", fill: "none", stroke: "currentColor", strokeWidth: "2" }))
        )
      ),
      /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__closeBtn",
          onClick: B,
          title: r["js.window.close"]
        },
        /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24", width: "20", height: "20", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement(
          "line",
          {
            x1: "6",
            y1: "6",
            x2: "18",
            y2: "18",
            stroke: "currentColor",
            strokeWidth: "2",
            strokeLinecap: "round"
          }
        ), /* @__PURE__ */ e.createElement(
          "line",
          {
            x1: "18",
            y1: "6",
            x2: "6",
            y2: "18",
            stroke: "currentColor",
            strokeWidth: "2",
            strokeLinecap: "round"
          }
        ))
      )
    ),
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(K, { control: o })),
    (m.length > 0 || h) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, h && /* @__PURE__ */ e.createElement(K, { control: h }), m.map((J, A) => /* @__PURE__ */ e.createElement(K, { key: A, control: J }))),
    a && !M && Bn.map((J) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: J,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${J}`,
        onMouseDown: (A) => x(J, A)
      }
    ))
  );
}, { useCallback: On, useEffect: Fn } = e, Hn = {
  "js.drawer.close": "Close"
}, Wn = ({ controlId: l }) => {
  const t = G(), n = ne(), r = ae(Hn), i = t.open === !0, c = t.position ?? "right", s = t.size ?? "medium", u = t.title ?? null, a = t.child, o = On(() => {
    n("close");
  }, [n]);
  Fn(() => {
    if (!i) return;
    const p = (h) => {
      h.key === "Escape" && o();
    };
    return document.addEventListener("keydown", p), () => document.removeEventListener("keydown", p);
  }, [i, o]);
  const m = [
    "tlDrawer",
    `tlDrawer--${c}`,
    `tlDrawer--${s}`,
    i ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: l, className: m, "aria-hidden": !i }, u !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, u), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: o,
      title: r["js.drawer.close"]
    },
    /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24", width: "20", height: "20", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement(
      "line",
      {
        x1: "6",
        y1: "6",
        x2: "18",
        y2: "18",
        stroke: "currentColor",
        strokeWidth: "2",
        strokeLinecap: "round"
      }
    ), /* @__PURE__ */ e.createElement(
      "line",
      {
        x1: "18",
        y1: "6",
        x2: "6",
        y2: "18",
        stroke: "currentColor",
        strokeWidth: "2",
        strokeLinecap: "round"
      }
    ))
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, a && /* @__PURE__ */ e.createElement(K, { control: a })));
}, { useCallback: zn } = e, Un = ({ controlId: l }) => {
  const t = G(), n = ne(), r = t.child, i = zn((c) => {
    c.preventDefault(), c.stopPropagation(), n("openContextMenu", { x: c.clientX, y: c.clientY });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tl-context-menu-region", onContextMenu: i }, r && /* @__PURE__ */ e.createElement(K, { control: r }));
}, { useCallback: Vn, useEffect: Kn, useState: Yn } = e, Gn = ({ controlId: l }) => {
  const t = G(), n = ne(), r = t.message ?? "", i = t.content ?? "", c = t.variant ?? "info", s = t.duration ?? 5e3, u = t.visible === !0, a = t.generation ?? 0, [o, m] = Yn(!1), p = Vn(() => {
    m(!0), setTimeout(() => {
      n("dismiss", { generation: a }), m(!1);
    }, 200);
  }, [n, a]);
  return Kn(() => {
    if (!u || s === 0) return;
    const h = setTimeout(p, s);
    return () => clearTimeout(h);
  }, [u, s, p]), !u && !o ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${c}${o ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    i ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: i } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, r)
  );
}, { useCallback: We, useEffect: ze, useRef: Xn, useState: ct } = e, qn = ({ controlId: l }) => {
  const t = G(), n = ne(), r = t.open === !0, i = t.anchorId, c = t.anchorX, s = t.anchorY, u = t.items ?? [], a = Xn(null), [o, m] = ct({ top: 0, left: 0 }), [p, h] = ct(0), N = u.filter((v) => v.type === "item" && !v.disabled);
  ze(() => {
    var E, W;
    if (!r) return;
    const v = ((E = a.current) == null ? void 0 : E.offsetHeight) ?? 200, C = ((W = a.current) == null ? void 0 : W.offsetWidth) ?? 200;
    if (c != null && s != null) {
      let k = s, S = c;
      k + v > window.innerHeight && (k = Math.max(0, window.innerHeight - v)), S + C > window.innerWidth && (S = Math.max(0, window.innerWidth - C)), m({ top: k, left: S }), h(0);
      return;
    }
    if (!i) return;
    const D = document.getElementById(i);
    if (!D) return;
    const M = D.getBoundingClientRect();
    let b = M.bottom + 4, _ = M.left;
    b + v > window.innerHeight && (b = M.top - v - 4), _ + C > window.innerWidth && (_ = M.right - C), m({ top: b, left: _ }), h(0);
  }, [r, i, c, s]);
  const f = We(() => {
    n("close");
  }, [n]), T = We((v) => {
    n("selectItem", { itemId: v });
  }, [n]);
  ze(() => {
    if (!r) return;
    const v = (C) => {
      a.current && !a.current.contains(C.target) && f();
    };
    return document.addEventListener("mousedown", v), () => document.removeEventListener("mousedown", v);
  }, [r, f]);
  const w = We((v) => {
    if (v.key === "Escape") {
      f();
      return;
    }
    if (v.key === "ArrowDown")
      v.preventDefault(), h((C) => (C + 1) % N.length);
    else if (v.key === "ArrowUp")
      v.preventDefault(), h((C) => (C - 1 + N.length) % N.length);
    else if (v.key === "Enter" || v.key === " ") {
      v.preventDefault();
      const C = N[p];
      C && T(C.id);
    }
  }, [f, T, N, p]);
  return ze(() => {
    r && a.current && a.current.focus();
  }, [r]), r ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: a,
      tabIndex: -1,
      style: { position: "fixed", top: o.top, left: o.left },
      onKeyDown: w
    },
    u.map((v, C) => {
      if (v.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: C, className: "tlMenu__separator" });
      const M = N.indexOf(v) === p;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: v.id,
          type: "button",
          className: "tlMenu__item" + (M ? " tlMenu__item--focused" : "") + (v.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: v.disabled,
          tabIndex: M ? 0 : -1,
          onClick: () => T(v.id)
        },
        v.icon && /* @__PURE__ */ e.createElement(_e, { encoded: v.icon, className: "tlMenu__icon" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, v.label)
      );
    })
  ) : null;
}, Zn = ({ controlId: l }) => {
  const t = G(), n = t.header, r = t.content, i = t.footer, c = t.snackbar, s = t.dialogManager, u = t.menuOverlay;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, n && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(K, { control: n })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(K, { control: r })), i && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(K, { control: i })), /* @__PURE__ */ e.createElement(K, { control: c }), s && /* @__PURE__ */ e.createElement(K, { control: s }), u && /* @__PURE__ */ e.createElement(K, { control: u }));
}, Qn = ({ controlId: l }) => {
  const t = G(), n = t.text ?? "", r = t.cssClass ?? "", i = t.hasTooltip === !0, c = r ? `tlText ${r}` : "tlText";
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: c,
      "data-tooltip": i ? "key:tooltip" : void 0
    },
    n
  );
}, Jn = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, it = 50, el = ({ controlId: l }) => {
  const t = G(), n = ne(), r = ae(Jn), i = e.useRef(null);
  e.useEffect(() => {
    const R = i.current;
    if (!R) return;
    const I = (g) => {
      const L = g.detail;
      let H = L.target;
      for (; H && H !== R; ) {
        const Z = H.dataset.row, re = H.dataset.col;
        if (Z != null && re != null) {
          L.resolved = { key: Z + "|" + re };
          return;
        }
        H = H.parentElement;
      }
    };
    return R.addEventListener("tl-tooltip-resolve", I), () => R.removeEventListener("tl-tooltip-resolve", I);
  }, []);
  const c = t.columns ?? [], s = t.totalRowCount ?? 0, u = t.rows ?? [], a = t.rowHeight ?? 36, o = t.selectionMode ?? "single", m = t.selectedCount ?? 0, p = t.frozenColumnCount ?? 0, h = t.treeMode ?? !1, N = e.useMemo(
    () => c.filter((R) => R.sortPriority && R.sortPriority > 0).length,
    [c]
  ), f = o === "multi", T = 40, w = 20, v = e.useRef(null), C = e.useRef(null), D = e.useRef(null), [M, b] = e.useState({}), _ = e.useRef(null), E = e.useRef(!1), W = e.useRef(null), [k, S] = e.useState(null), [B, x] = e.useState(null);
  e.useEffect(() => {
    _.current || b({});
  }, [c]);
  const $ = e.useCallback((R) => M[R.name] ?? R.width, [M]), q = e.useMemo(() => {
    const R = [];
    let I = f && p > 0 ? T : 0;
    for (let g = 0; g < p && g < c.length; g++)
      R.push(I), I += $(c[g]);
    return R;
  }, [c, p, f, T, $]), le = s * a, Q = e.useRef(null), J = e.useCallback((R, I, g) => {
    g.preventDefault(), g.stopPropagation(), _.current = { column: R, startX: g.clientX, startWidth: I };
    let L = g.clientX, H = 0;
    const Z = () => {
      const oe = _.current;
      if (!oe) return;
      const pe = Math.max(it, oe.startWidth + (L - oe.startX) + H);
      b((Ce) => ({ ...Ce, [oe.column]: pe }));
    }, re = () => {
      const oe = C.current, pe = v.current;
      if (!oe || !_.current) return;
      const Ce = oe.getBoundingClientRect(), nt = 40, lt = 8, St = oe.scrollLeft;
      L > Ce.right - nt ? oe.scrollLeft += lt : L < Ce.left + nt && (oe.scrollLeft = Math.max(0, oe.scrollLeft - lt));
      const rt = oe.scrollLeft - St;
      rt !== 0 && (pe && (pe.scrollLeft = oe.scrollLeft), H += rt, Z()), Q.current = requestAnimationFrame(re);
    };
    Q.current = requestAnimationFrame(re);
    const ge = (oe) => {
      L = oe.clientX, Z();
    }, Le = (oe) => {
      document.removeEventListener("mousemove", ge), document.removeEventListener("mouseup", Le), Q.current !== null && (cancelAnimationFrame(Q.current), Q.current = null);
      const pe = _.current;
      if (pe) {
        const Ce = Math.max(it, pe.startWidth + (oe.clientX - pe.startX) + H);
        n("columnResize", { column: pe.column, width: Ce }), _.current = null, E.current = !0, requestAnimationFrame(() => {
          E.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", ge), document.addEventListener("mouseup", Le);
  }, [n]), A = e.useCallback(() => {
    v.current && C.current && (v.current.scrollLeft = C.current.scrollLeft), D.current !== null && clearTimeout(D.current), D.current = window.setTimeout(() => {
      const R = C.current;
      if (!R) return;
      const I = R.scrollTop, g = Math.ceil(R.clientHeight / a), L = Math.floor(I / a);
      n("scroll", { start: L, count: g });
    }, 80);
  }, [n, a]), j = e.useCallback((R, I, g) => {
    if (E.current) return;
    let L;
    !I || I === "desc" ? L = "asc" : L = "desc";
    const H = g.shiftKey ? "add" : "replace";
    n("sort", { column: R, direction: L, mode: H });
  }, [n]), U = e.useCallback((R, I) => {
    W.current = R, I.dataTransfer.effectAllowed = "move", I.dataTransfer.setData("text/plain", R);
  }, []), d = e.useCallback((R, I) => {
    if (!W.current || W.current === R) {
      S(null);
      return;
    }
    I.preventDefault(), I.dataTransfer.dropEffect = "move";
    const g = I.currentTarget.getBoundingClientRect(), L = I.clientX < g.left + g.width / 2 ? "left" : "right";
    S({ column: R, side: L });
  }, []), y = e.useCallback((R) => {
    R.preventDefault(), R.stopPropagation();
    const I = W.current;
    if (!I || !k) {
      W.current = null, S(null);
      return;
    }
    let g = c.findIndex((H) => H.name === k.column);
    if (g < 0) {
      W.current = null, S(null);
      return;
    }
    const L = c.findIndex((H) => H.name === I);
    k.side === "right" && g++, L < g && g--, n("columnReorder", { column: I, targetIndex: g }), W.current = null, S(null);
  }, [c, k, n]), F = e.useCallback(() => {
    W.current = null, S(null);
  }, []), O = e.useCallback((R, I) => {
    I.shiftKey && I.preventDefault(), n("select", {
      rowIndex: R,
      ctrlKey: I.ctrlKey || I.metaKey,
      shiftKey: I.shiftKey
    });
  }, [n]), V = e.useCallback((R, I) => {
    I.stopPropagation(), n("select", { rowIndex: R, ctrlKey: !0, shiftKey: !1 });
  }, [n]), P = e.useCallback(() => {
    const R = m === s && s > 0;
    n("selectAll", { selected: !R });
  }, [n, m, s]), X = e.useCallback((R, I, g) => {
    g.stopPropagation(), n("expand", { rowIndex: R, expanded: I });
  }, [n]), te = e.useCallback((R, I) => {
    I.preventDefault(), x({ x: I.clientX, y: I.clientY, colIdx: R });
  }, []), ee = e.useCallback(() => {
    B && (n("setFrozenColumnCount", { count: B.colIdx + 1 }), x(null));
  }, [B, n]), ie = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), x(null);
  }, [n]);
  e.useEffect(() => {
    if (!B) return;
    const R = () => x(null), I = (g) => {
      g.key === "Escape" && x(null);
    };
    return document.addEventListener("mousedown", R), document.addEventListener("keydown", I), () => {
      document.removeEventListener("mousedown", R), document.removeEventListener("keydown", I);
    };
  }, [B]);
  const ue = c.reduce((R, I) => R + $(I), 0) + (f ? T : 0), me = m === s && s > 0, he = m > 0 && m < s, be = e.useCallback((R) => {
    R && (R.indeterminate = he);
  }, [he]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: i,
      id: l,
      className: "tlTableView",
      "data-tooltip": "dynamic",
      onDragOver: (R) => {
        if (!W.current) return;
        R.preventDefault();
        const I = C.current, g = v.current;
        if (!I) return;
        const L = I.getBoundingClientRect(), H = 40, Z = 8;
        R.clientX < L.left + H ? I.scrollLeft = Math.max(0, I.scrollLeft - Z) : R.clientX > L.right - H && (I.scrollLeft += Z), g && (g.scrollLeft = I.scrollLeft);
      },
      onDrop: y
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: v }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { width: ue } }, f && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (p > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: T,
          minWidth: T,
          ...p > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (R) => {
          W.current && (R.preventDefault(), R.dataTransfer.dropEffect = "move", c.length > 0 && c[0].name !== W.current && S({ column: c[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: be,
          className: "tlTableView__checkbox",
          checked: me,
          onChange: P
        }
      )
    ), c.map((R, I) => {
      const g = $(R);
      c.length - 1;
      let L = "tlTableView__headerCell";
      R.sortable && (L += " tlTableView__headerCell--sortable"), k && k.column === R.name && (L += " tlTableView__headerCell--dragOver-" + k.side);
      const H = I < p, Z = I === p - 1;
      return H && (L += " tlTableView__headerCell--frozen"), Z && (L += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: R.name,
          className: L,
          style: {
            width: g,
            minWidth: g,
            position: H ? "sticky" : "relative",
            ...H ? { left: q[I], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: R.sortable ? (re) => j(R.name, R.sortDirection, re) : void 0,
          onContextMenu: (re) => te(I, re),
          onDragStart: (re) => U(R.name, re),
          onDragOver: (re) => d(R.name, re),
          onDrop: y,
          onDragEnd: F
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, R.label),
        R.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, R.sortDirection === "asc" ? "▲" : "▼", N > 1 && R.sortPriority != null && R.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, R.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (re) => J(R.name, g, re)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (R) => {
          if (W.current && c.length > 0) {
            const I = c[c.length - 1];
            I.name !== W.current && (R.preventDefault(), R.dataTransfer.dropEffect = "move", S({ column: I.name, side: "right" }));
          }
        },
        onDrop: y
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: C,
        className: "tlTableView__body",
        onScroll: A
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: le, position: "relative", width: ue } }, u.map((R) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: R.id,
          className: "tlTableView__row" + (R.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: R.index * a,
            height: a,
            width: ue
          },
          onClick: (I) => O(R.index, I)
        },
        f && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (p > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: T,
              minWidth: T,
              ...p > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (I) => I.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: R.selected,
              onChange: () => {
              },
              onClick: (I) => V(R.index, I),
              tabIndex: -1
            }
          )
        ),
        c.map((I, g) => {
          const L = $(I), H = g === c.length - 1, Z = g < p, re = g === p - 1;
          let ge = "tlTableView__cell";
          Z && (ge += " tlTableView__cell--frozen"), re && (ge += " tlTableView__cell--frozenLast");
          const Le = h && g === 0, oe = R.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: I.name,
              className: ge,
              "data-row": R.id,
              "data-col": I.name,
              style: {
                ...H && !Z ? { flex: "1 0 auto", minWidth: L } : { width: L, minWidth: L },
                ...Z ? { position: "sticky", left: q[g], zIndex: 2 } : {}
              }
            },
            Le ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: oe * w } }, R.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (pe) => X(R.index, !R.expanded, pe)
              },
              R.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(K, { control: R.cells[I.name] })) : /* @__PURE__ */ e.createElement(K, { control: R.cells[I.name] })
          );
        })
      )))
    ),
    B && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: B.y, left: B.x, zIndex: 1e4 },
        onMouseDown: (R) => R.stopPropagation()
      },
      B.colIdx + 1 !== p && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: ee }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.freezeUpTo"])),
      p > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: ie }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.unfreezeAll"]))
    )
  );
}, tl = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, gt = e.createContext(tl), { useMemo: nl, useRef: ll, useState: rl, useEffect: ol } = e, al = 320, sl = ({ controlId: l }) => {
  const t = G(), n = t.maxColumns ?? 3, r = t.labelPosition ?? "auto", i = t.readOnly === !0, c = t.children ?? [], s = t.noModelMessage, u = ll(null), [a, o] = rl(
    r === "top" ? "top" : "side"
  );
  ol(() => {
    if (r !== "auto") {
      o(r);
      return;
    }
    const f = u.current;
    if (!f) return;
    const T = new ResizeObserver((w) => {
      for (const v of w) {
        const D = v.contentRect.width / n;
        o(D < al ? "top" : "side");
      }
    });
    return T.observe(f), () => T.disconnect();
  }, [r, n]);
  const m = nl(() => ({
    readOnly: i,
    resolvedLabelPosition: a
  }), [i, a]), h = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / n))}rem`}, 1fr))`
  }, N = [
    "tlFormLayout",
    i ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, s)) : /* @__PURE__ */ e.createElement(gt.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: l, className: N, style: h, ref: u }, c.map((f, T) => /* @__PURE__ */ e.createElement(K, { key: T, control: f }))));
}, { useCallback: cl } = e, il = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, ul = ({ controlId: l }) => {
  const t = G(), n = ne(), r = ae(il), i = t.headerControl ?? null, c = t.headerActions ?? [], s = t.collapsible === !0, u = t.collapsed === !0, a = t.border ?? "none", o = t.fullLine === !0, m = t.children ?? [], p = i != null || c.length > 0 || s, h = cl(() => {
    n("toggleCollapse");
  }, [n]), N = [
    "tlFormGroup",
    `tlFormGroup--border-${a}`,
    o ? "tlFormGroup--fullLine" : "",
    u ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: N }, p && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, s && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: h,
      "aria-expanded": !u,
      title: u ? r["js.formGroup.expand"] : r["js.formGroup.collapse"]
    },
    /* @__PURE__ */ e.createElement(
      "svg",
      {
        viewBox: "0 0 16 16",
        width: "14",
        height: "14",
        "aria-hidden": "true",
        className: u ? "tlFormGroup__chevron--collapsed" : "tlFormGroup__chevron"
      },
      /* @__PURE__ */ e.createElement(
        "polyline",
        {
          points: "4,6 8,10 12,6",
          fill: "none",
          stroke: "currentColor",
          strokeWidth: "1.5",
          strokeLinecap: "round",
          strokeLinejoin: "round"
        }
      )
    )
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(K, { control: i })), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, c.map((f, T) => /* @__PURE__ */ e.createElement(K, { key: T, control: f })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((f, T) => /* @__PURE__ */ e.createElement(K, { key: T, control: f }))));
}, { useContext: dl, useState: ml, useCallback: pl } = e, fl = ({ controlId: l }) => {
  const t = G(), n = dl(gt), r = t.label ?? "", i = t.required === !0, c = t.error, s = t.warnings, u = t.helpText, a = t.dirty === !0, o = t.labelPosition ?? n.resolvedLabelPosition, m = t.fullLine === !0, p = t.visible !== !1, h = t.hasTooltip === !0, N = t.field, f = n.readOnly, [T, w] = ml(!1), v = pl(() => w((b) => !b), []);
  if (!p) return null;
  const C = c != null, D = s != null && s.length > 0, M = [
    "tlFormField",
    `tlFormField--${o}`,
    f ? "tlFormField--readonly" : "",
    m ? "tlFormField--fullLine" : "",
    C ? "tlFormField--error" : "",
    !C && D ? "tlFormField--warning" : "",
    a ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: M }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": h ? "key:tooltip" : void 0
    },
    r
  ), i && !f && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), a && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), u && !f && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormField__helpIcon",
      onClick: v,
      "aria-label": "Help"
    },
    /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("circle", { cx: "8", cy: "8", r: "7", fill: "none", stroke: "currentColor", strokeWidth: "1.5" }), /* @__PURE__ */ e.createElement(
      "text",
      {
        x: "8",
        y: "12",
        textAnchor: "middle",
        fontSize: "10",
        fill: "currentColor"
      },
      "?"
    ))
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(K, { control: N })), !f && C && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(
    "svg",
    {
      className: "tlFormField__errorIcon",
      viewBox: "0 0 16 16",
      width: "14",
      height: "14",
      "aria-hidden": "true"
    },
    /* @__PURE__ */ e.createElement("path", { d: "M8 1l7 14H1L8 1z", fill: "none", stroke: "currentColor", strokeWidth: "1.2" }),
    /* @__PURE__ */ e.createElement("line", { x1: "8", y1: "6", x2: "8", y2: "10", stroke: "currentColor", strokeWidth: "1.2" }),
    /* @__PURE__ */ e.createElement("circle", { cx: "8", cy: "12", r: "0.8", fill: "currentColor" })
  ), /* @__PURE__ */ e.createElement("span", null, c)), !f && !C && D && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, s.map((b, _) => /* @__PURE__ */ e.createElement("div", { key: _, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(
    "svg",
    {
      className: "tlFormField__warningIcon",
      viewBox: "0 0 16 16",
      width: "14",
      height: "14",
      "aria-hidden": "true"
    },
    /* @__PURE__ */ e.createElement("path", { d: "M8 1l7 14H1L8 1z", fill: "none", stroke: "currentColor", strokeWidth: "1.2" }),
    /* @__PURE__ */ e.createElement("line", { x1: "8", y1: "6", x2: "8", y2: "10", stroke: "currentColor", strokeWidth: "1.2" }),
    /* @__PURE__ */ e.createElement("circle", { cx: "8", cy: "12", r: "0.8", fill: "currentColor" })
  ), /* @__PURE__ */ e.createElement("span", null, b)))), !f && u && T && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, u));
}, hl = ({ controlId: l }) => {
  const t = G(), n = ne(), r = t.iconCss, i = t.iconSrc, c = t.label, s = t.cssClass, u = t.hasTooltip === !0, a = t.hasLink, o = r ? /* @__PURE__ */ e.createElement("i", { className: r }) : i ? /* @__PURE__ */ e.createElement("img", { src: i, className: "tlTypeIcon", alt: "" }) : null, m = /* @__PURE__ */ e.createElement(e.Fragment, null, o, c && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, c)), p = e.useCallback((f) => {
    f.preventDefault(), n("goto", {});
  }, [n]), h = ["tlResourceCell", s].filter(Boolean).join(" "), N = u ? "key:tooltip" : void 0;
  return a ? /* @__PURE__ */ e.createElement(
    "a",
    {
      id: l,
      className: h,
      href: "#",
      onClick: p,
      "data-tooltip": N
    },
    m
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: h, "data-tooltip": N }, m);
}, _l = 20, bl = () => {
  const l = G(), t = ne(), n = l.nodes ?? [], r = l.selectionMode ?? "single", i = l.dragEnabled ?? !1, c = l.dropEnabled ?? !1, s = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [a, o] = e.useState(-1), m = e.useRef(null), p = e.useCallback((b, _) => {
    t(_ ? "collapse" : "expand", { nodeId: b });
  }, [t]), h = e.useCallback((b, _) => {
    t("select", {
      nodeId: b,
      ctrlKey: _.ctrlKey || _.metaKey,
      shiftKey: _.shiftKey
    });
  }, [t]), N = e.useCallback((b, _) => {
    _.preventDefault(), t("contextMenu", { nodeId: b, x: _.clientX, y: _.clientY });
  }, [t]), f = e.useRef(null), T = e.useCallback((b, _) => {
    const E = _.getBoundingClientRect(), W = b.clientY - E.top, k = E.height / 3;
    return W < k ? "above" : W > k * 2 ? "below" : "within";
  }, []), w = e.useCallback((b, _) => {
    _.dataTransfer.effectAllowed = "move", _.dataTransfer.setData("text/plain", b);
  }, []), v = e.useCallback((b, _) => {
    _.preventDefault(), _.dataTransfer.dropEffect = "move";
    const E = T(_, _.currentTarget);
    f.current != null && window.clearTimeout(f.current), f.current = window.setTimeout(() => {
      t("dragOver", { nodeId: b, position: E }), f.current = null;
    }, 50);
  }, [t, T]), C = e.useCallback((b, _) => {
    _.preventDefault(), f.current != null && (window.clearTimeout(f.current), f.current = null);
    const E = T(_, _.currentTarget);
    t("drop", { nodeId: b, position: E });
  }, [t, T]), D = e.useCallback(() => {
    f.current != null && (window.clearTimeout(f.current), f.current = null), t("dragEnd");
  }, [t]), M = e.useCallback((b) => {
    if (n.length === 0) return;
    let _ = a;
    switch (b.key) {
      case "ArrowDown":
        b.preventDefault(), _ = Math.min(a + 1, n.length - 1);
        break;
      case "ArrowUp":
        b.preventDefault(), _ = Math.max(a - 1, 0);
        break;
      case "ArrowRight":
        if (b.preventDefault(), a >= 0 && a < n.length) {
          const E = n[a];
          if (E.expandable && !E.expanded) {
            t("expand", { nodeId: E.id });
            return;
          } else E.expanded && (_ = a + 1);
        }
        break;
      case "ArrowLeft":
        if (b.preventDefault(), a >= 0 && a < n.length) {
          const E = n[a];
          if (E.expanded) {
            t("collapse", { nodeId: E.id });
            return;
          } else {
            const W = E.depth;
            for (let k = a - 1; k >= 0; k--)
              if (n[k].depth < W) {
                _ = k;
                break;
              }
          }
        }
        break;
      case "Enter":
        b.preventDefault(), a >= 0 && a < n.length && t("select", {
          nodeId: n[a].id,
          ctrlKey: b.ctrlKey || b.metaKey,
          shiftKey: b.shiftKey
        });
        return;
      case " ":
        b.preventDefault(), r === "multi" && a >= 0 && a < n.length && t("select", {
          nodeId: n[a].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        b.preventDefault(), _ = 0;
        break;
      case "End":
        b.preventDefault(), _ = n.length - 1;
        break;
      default:
        return;
    }
    _ !== a && o(_);
  }, [a, n, t, r]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: m,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: M
    },
    n.map((b, _) => /* @__PURE__ */ e.createElement(
      "li",
      {
        key: b.id,
        role: "treeitem",
        "aria-expanded": b.expandable ? b.expanded : void 0,
        "aria-selected": b.selected,
        "aria-level": b.depth + 1,
        className: [
          "tlTreeView__node",
          b.selected ? "tlTreeView__node--selected" : "",
          _ === a ? "tlTreeView__node--focused" : "",
          s === b.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          s === b.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          s === b.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: b.depth * _l },
        draggable: i,
        onClick: (E) => h(b.id, E),
        onContextMenu: (E) => N(b.id, E),
        onDragStart: (E) => w(b.id, E),
        onDragOver: c ? (E) => v(b.id, E) : void 0,
        onDrop: c ? (E) => C(b.id, E) : void 0,
        onDragEnd: D
      },
      b.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (E) => {
            E.stopPropagation(), p(b.id, b.expanded);
          },
          tabIndex: -1,
          "aria-label": b.expanded ? "Collapse" : "Expand"
        },
        b.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: b.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(K, { control: b.content }))
    ))
  );
};
var Ue = { exports: {} }, se = {}, Ve = { exports: {} }, Y = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var ut;
function vl() {
  if (ut) return Y;
  ut = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), r = Symbol.for("react.strict_mode"), i = Symbol.for("react.profiler"), c = Symbol.for("react.consumer"), s = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), a = Symbol.for("react.suspense"), o = Symbol.for("react.memo"), m = Symbol.for("react.lazy"), p = Symbol.for("react.activity"), h = Symbol.iterator;
  function N(d) {
    return d === null || typeof d != "object" ? null : (d = h && d[h] || d["@@iterator"], typeof d == "function" ? d : null);
  }
  var f = {
    isMounted: function() {
      return !1;
    },
    enqueueForceUpdate: function() {
    },
    enqueueReplaceState: function() {
    },
    enqueueSetState: function() {
    }
  }, T = Object.assign, w = {};
  function v(d, y, F) {
    this.props = d, this.context = y, this.refs = w, this.updater = F || f;
  }
  v.prototype.isReactComponent = {}, v.prototype.setState = function(d, y) {
    if (typeof d != "object" && typeof d != "function" && d != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, d, y, "setState");
  }, v.prototype.forceUpdate = function(d) {
    this.updater.enqueueForceUpdate(this, d, "forceUpdate");
  };
  function C() {
  }
  C.prototype = v.prototype;
  function D(d, y, F) {
    this.props = d, this.context = y, this.refs = w, this.updater = F || f;
  }
  var M = D.prototype = new C();
  M.constructor = D, T(M, v.prototype), M.isPureReactComponent = !0;
  var b = Array.isArray;
  function _() {
  }
  var E = { H: null, A: null, T: null, S: null }, W = Object.prototype.hasOwnProperty;
  function k(d, y, F) {
    var O = F.ref;
    return {
      $$typeof: l,
      type: d,
      key: y,
      ref: O !== void 0 ? O : null,
      props: F
    };
  }
  function S(d, y) {
    return k(d.type, y, d.props);
  }
  function B(d) {
    return typeof d == "object" && d !== null && d.$$typeof === l;
  }
  function x(d) {
    var y = { "=": "=0", ":": "=2" };
    return "$" + d.replace(/[=:]/g, function(F) {
      return y[F];
    });
  }
  var $ = /\/+/g;
  function q(d, y) {
    return typeof d == "object" && d !== null && d.key != null ? x("" + d.key) : y.toString(36);
  }
  function le(d) {
    switch (d.status) {
      case "fulfilled":
        return d.value;
      case "rejected":
        throw d.reason;
      default:
        switch (typeof d.status == "string" ? d.then(_, _) : (d.status = "pending", d.then(
          function(y) {
            d.status === "pending" && (d.status = "fulfilled", d.value = y);
          },
          function(y) {
            d.status === "pending" && (d.status = "rejected", d.reason = y);
          }
        )), d.status) {
          case "fulfilled":
            return d.value;
          case "rejected":
            throw d.reason;
        }
    }
    throw d;
  }
  function Q(d, y, F, O, V) {
    var P = typeof d;
    (P === "undefined" || P === "boolean") && (d = null);
    var X = !1;
    if (d === null) X = !0;
    else
      switch (P) {
        case "bigint":
        case "string":
        case "number":
          X = !0;
          break;
        case "object":
          switch (d.$$typeof) {
            case l:
            case t:
              X = !0;
              break;
            case m:
              return X = d._init, Q(
                X(d._payload),
                y,
                F,
                O,
                V
              );
          }
      }
    if (X)
      return V = V(d), X = O === "" ? "." + q(d, 0) : O, b(V) ? (F = "", X != null && (F = X.replace($, "$&/") + "/"), Q(V, y, F, "", function(ie) {
        return ie;
      })) : V != null && (B(V) && (V = S(
        V,
        F + (V.key == null || d && d.key === V.key ? "" : ("" + V.key).replace(
          $,
          "$&/"
        ) + "/") + X
      )), y.push(V)), 1;
    X = 0;
    var te = O === "" ? "." : O + ":";
    if (b(d))
      for (var ee = 0; ee < d.length; ee++)
        O = d[ee], P = te + q(O, ee), X += Q(
          O,
          y,
          F,
          P,
          V
        );
    else if (ee = N(d), typeof ee == "function")
      for (d = ee.call(d), ee = 0; !(O = d.next()).done; )
        O = O.value, P = te + q(O, ee++), X += Q(
          O,
          y,
          F,
          P,
          V
        );
    else if (P === "object") {
      if (typeof d.then == "function")
        return Q(
          le(d),
          y,
          F,
          O,
          V
        );
      throw y = String(d), Error(
        "Objects are not valid as a React child (found: " + (y === "[object Object]" ? "object with keys {" + Object.keys(d).join(", ") + "}" : y) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return X;
  }
  function J(d, y, F) {
    if (d == null) return d;
    var O = [], V = 0;
    return Q(d, O, "", "", function(P) {
      return y.call(F, P, V++);
    }), O;
  }
  function A(d) {
    if (d._status === -1) {
      var y = d._result;
      y = y(), y.then(
        function(F) {
          (d._status === 0 || d._status === -1) && (d._status = 1, d._result = F);
        },
        function(F) {
          (d._status === 0 || d._status === -1) && (d._status = 2, d._result = F);
        }
      ), d._status === -1 && (d._status = 0, d._result = y);
    }
    if (d._status === 1) return d._result.default;
    throw d._result;
  }
  var j = typeof reportError == "function" ? reportError : function(d) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var y = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof d == "object" && d !== null && typeof d.message == "string" ? String(d.message) : String(d),
        error: d
      });
      if (!window.dispatchEvent(y)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", d);
      return;
    }
    console.error(d);
  }, U = {
    map: J,
    forEach: function(d, y, F) {
      J(
        d,
        function() {
          y.apply(this, arguments);
        },
        F
      );
    },
    count: function(d) {
      var y = 0;
      return J(d, function() {
        y++;
      }), y;
    },
    toArray: function(d) {
      return J(d, function(y) {
        return y;
      }) || [];
    },
    only: function(d) {
      if (!B(d))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return d;
    }
  };
  return Y.Activity = p, Y.Children = U, Y.Component = v, Y.Fragment = n, Y.Profiler = i, Y.PureComponent = D, Y.StrictMode = r, Y.Suspense = a, Y.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = E, Y.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(d) {
      return E.H.useMemoCache(d);
    }
  }, Y.cache = function(d) {
    return function() {
      return d.apply(null, arguments);
    };
  }, Y.cacheSignal = function() {
    return null;
  }, Y.cloneElement = function(d, y, F) {
    if (d == null)
      throw Error(
        "The argument must be a React element, but you passed " + d + "."
      );
    var O = T({}, d.props), V = d.key;
    if (y != null)
      for (P in y.key !== void 0 && (V = "" + y.key), y)
        !W.call(y, P) || P === "key" || P === "__self" || P === "__source" || P === "ref" && y.ref === void 0 || (O[P] = y[P]);
    var P = arguments.length - 2;
    if (P === 1) O.children = F;
    else if (1 < P) {
      for (var X = Array(P), te = 0; te < P; te++)
        X[te] = arguments[te + 2];
      O.children = X;
    }
    return k(d.type, V, O);
  }, Y.createContext = function(d) {
    return d = {
      $$typeof: s,
      _currentValue: d,
      _currentValue2: d,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, d.Provider = d, d.Consumer = {
      $$typeof: c,
      _context: d
    }, d;
  }, Y.createElement = function(d, y, F) {
    var O, V = {}, P = null;
    if (y != null)
      for (O in y.key !== void 0 && (P = "" + y.key), y)
        W.call(y, O) && O !== "key" && O !== "__self" && O !== "__source" && (V[O] = y[O]);
    var X = arguments.length - 2;
    if (X === 1) V.children = F;
    else if (1 < X) {
      for (var te = Array(X), ee = 0; ee < X; ee++)
        te[ee] = arguments[ee + 2];
      V.children = te;
    }
    if (d && d.defaultProps)
      for (O in X = d.defaultProps, X)
        V[O] === void 0 && (V[O] = X[O]);
    return k(d, P, V);
  }, Y.createRef = function() {
    return { current: null };
  }, Y.forwardRef = function(d) {
    return { $$typeof: u, render: d };
  }, Y.isValidElement = B, Y.lazy = function(d) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: d },
      _init: A
    };
  }, Y.memo = function(d, y) {
    return {
      $$typeof: o,
      type: d,
      compare: y === void 0 ? null : y
    };
  }, Y.startTransition = function(d) {
    var y = E.T, F = {};
    E.T = F;
    try {
      var O = d(), V = E.S;
      V !== null && V(F, O), typeof O == "object" && O !== null && typeof O.then == "function" && O.then(_, j);
    } catch (P) {
      j(P);
    } finally {
      y !== null && F.types !== null && (y.types = F.types), E.T = y;
    }
  }, Y.unstable_useCacheRefresh = function() {
    return E.H.useCacheRefresh();
  }, Y.use = function(d) {
    return E.H.use(d);
  }, Y.useActionState = function(d, y, F) {
    return E.H.useActionState(d, y, F);
  }, Y.useCallback = function(d, y) {
    return E.H.useCallback(d, y);
  }, Y.useContext = function(d) {
    return E.H.useContext(d);
  }, Y.useDebugValue = function() {
  }, Y.useDeferredValue = function(d, y) {
    return E.H.useDeferredValue(d, y);
  }, Y.useEffect = function(d, y) {
    return E.H.useEffect(d, y);
  }, Y.useEffectEvent = function(d) {
    return E.H.useEffectEvent(d);
  }, Y.useId = function() {
    return E.H.useId();
  }, Y.useImperativeHandle = function(d, y, F) {
    return E.H.useImperativeHandle(d, y, F);
  }, Y.useInsertionEffect = function(d, y) {
    return E.H.useInsertionEffect(d, y);
  }, Y.useLayoutEffect = function(d, y) {
    return E.H.useLayoutEffect(d, y);
  }, Y.useMemo = function(d, y) {
    return E.H.useMemo(d, y);
  }, Y.useOptimistic = function(d, y) {
    return E.H.useOptimistic(d, y);
  }, Y.useReducer = function(d, y, F) {
    return E.H.useReducer(d, y, F);
  }, Y.useRef = function(d) {
    return E.H.useRef(d);
  }, Y.useState = function(d) {
    return E.H.useState(d);
  }, Y.useSyncExternalStore = function(d, y, F) {
    return E.H.useSyncExternalStore(
      d,
      y,
      F
    );
  }, Y.useTransition = function() {
    return E.H.useTransition();
  }, Y.version = "19.2.4", Y;
}
var dt;
function El() {
  return dt || (dt = 1, Ve.exports = vl()), Ve.exports;
}
/**
 * @license React
 * react-dom.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var mt;
function gl() {
  if (mt) return se;
  mt = 1;
  var l = El();
  function t(a) {
    var o = "https://react.dev/errors/" + a;
    if (1 < arguments.length) {
      o += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var m = 2; m < arguments.length; m++)
        o += "&args[]=" + encodeURIComponent(arguments[m]);
    }
    return "Minified React error #" + a + "; visit " + o + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function n() {
  }
  var r = {
    d: {
      f: n,
      r: function() {
        throw Error(t(522));
      },
      D: n,
      C: n,
      L: n,
      m: n,
      X: n,
      S: n,
      M: n
    },
    p: 0,
    findDOMNode: null
  }, i = Symbol.for("react.portal");
  function c(a, o, m) {
    var p = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: i,
      key: p == null ? null : "" + p,
      children: a,
      containerInfo: o,
      implementation: m
    };
  }
  var s = l.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function u(a, o) {
    if (a === "font") return "";
    if (typeof o == "string")
      return o === "use-credentials" ? o : "";
  }
  return se.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = r, se.createPortal = function(a, o) {
    var m = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!o || o.nodeType !== 1 && o.nodeType !== 9 && o.nodeType !== 11)
      throw Error(t(299));
    return c(a, o, null, m);
  }, se.flushSync = function(a) {
    var o = s.T, m = r.p;
    try {
      if (s.T = null, r.p = 2, a) return a();
    } finally {
      s.T = o, r.p = m, r.d.f();
    }
  }, se.preconnect = function(a, o) {
    typeof a == "string" && (o ? (o = o.crossOrigin, o = typeof o == "string" ? o === "use-credentials" ? o : "" : void 0) : o = null, r.d.C(a, o));
  }, se.prefetchDNS = function(a) {
    typeof a == "string" && r.d.D(a);
  }, se.preinit = function(a, o) {
    if (typeof a == "string" && o && typeof o.as == "string") {
      var m = o.as, p = u(m, o.crossOrigin), h = typeof o.integrity == "string" ? o.integrity : void 0, N = typeof o.fetchPriority == "string" ? o.fetchPriority : void 0;
      m === "style" ? r.d.S(
        a,
        typeof o.precedence == "string" ? o.precedence : void 0,
        {
          crossOrigin: p,
          integrity: h,
          fetchPriority: N
        }
      ) : m === "script" && r.d.X(a, {
        crossOrigin: p,
        integrity: h,
        fetchPriority: N,
        nonce: typeof o.nonce == "string" ? o.nonce : void 0
      });
    }
  }, se.preinitModule = function(a, o) {
    if (typeof a == "string")
      if (typeof o == "object" && o !== null) {
        if (o.as == null || o.as === "script") {
          var m = u(
            o.as,
            o.crossOrigin
          );
          r.d.M(a, {
            crossOrigin: m,
            integrity: typeof o.integrity == "string" ? o.integrity : void 0,
            nonce: typeof o.nonce == "string" ? o.nonce : void 0
          });
        }
      } else o == null && r.d.M(a);
  }, se.preload = function(a, o) {
    if (typeof a == "string" && typeof o == "object" && o !== null && typeof o.as == "string") {
      var m = o.as, p = u(m, o.crossOrigin);
      r.d.L(a, m, {
        crossOrigin: p,
        integrity: typeof o.integrity == "string" ? o.integrity : void 0,
        nonce: typeof o.nonce == "string" ? o.nonce : void 0,
        type: typeof o.type == "string" ? o.type : void 0,
        fetchPriority: typeof o.fetchPriority == "string" ? o.fetchPriority : void 0,
        referrerPolicy: typeof o.referrerPolicy == "string" ? o.referrerPolicy : void 0,
        imageSrcSet: typeof o.imageSrcSet == "string" ? o.imageSrcSet : void 0,
        imageSizes: typeof o.imageSizes == "string" ? o.imageSizes : void 0,
        media: typeof o.media == "string" ? o.media : void 0
      });
    }
  }, se.preloadModule = function(a, o) {
    if (typeof a == "string")
      if (o) {
        var m = u(o.as, o.crossOrigin);
        r.d.m(a, {
          as: typeof o.as == "string" && o.as !== "script" ? o.as : void 0,
          crossOrigin: m,
          integrity: typeof o.integrity == "string" ? o.integrity : void 0
        });
      } else r.d.m(a);
  }, se.requestFormReset = function(a) {
    r.d.r(a);
  }, se.unstable_batchedUpdates = function(a, o) {
    return a(o);
  }, se.useFormState = function(a, o, m) {
    return s.H.useFormState(a, o, m);
  }, se.useFormStatus = function() {
    return s.H.useHostTransitionStatus();
  }, se.version = "19.2.4", se;
}
var pt;
function Cl() {
  if (pt) return Ue.exports;
  pt = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), Ue.exports = gl(), Ue.exports;
}
var yl = Cl();
const { useState: ve, useCallback: ce, useRef: Te, useEffect: we, useMemo: Ze } = e;
function tt({ image: l }) {
  if (!l) return null;
  if (l.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function wl({
  option: l,
  removable: t,
  onRemove: n,
  removeLabel: r,
  draggable: i,
  onDragStart: c,
  onDragOver: s,
  onDrop: u,
  onDragEnd: a,
  dragClassName: o
}) {
  const m = ce(
    (p) => {
      p.stopPropagation(), n(l.value);
    },
    [n, l.value]
  );
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlDropdownSelect__chip" + (o ? " " + o : ""),
      draggable: i || void 0,
      onDragStart: c,
      onDragOver: s,
      onDrop: u,
      onDragEnd: a
    },
    i && /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__dragHandle", "aria-hidden": "true" }, "⋮⋮"),
    /* @__PURE__ */ e.createElement(tt, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__chipLabel" }, l.label),
    t && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__chipRemove",
        onClick: m,
        "aria-label": r
      },
      "×"
    )
  );
}
function kl({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: r,
  onMouseEnter: i,
  id: c
}) {
  const s = ce(() => r(l.value), [r, l.value]), u = Ze(() => {
    if (!n) return l.label;
    const a = l.label.toLowerCase().indexOf(n.toLowerCase());
    return a < 0 ? l.label : /* @__PURE__ */ e.createElement(e.Fragment, null, l.label.substring(0, a), /* @__PURE__ */ e.createElement("strong", null, l.label.substring(a, a + n.length)), l.label.substring(a + n.length));
  }, [l.label, n]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: c,
      role: "option",
      "aria-selected": t,
      className: "tlDropdownSelect__option" + (t ? " tlDropdownSelect__option--highlighted" : ""),
      onClick: s,
      onMouseEnter: i
    },
    /* @__PURE__ */ e.createElement(tt, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const Sl = ({ controlId: l, state: t }) => {
  const n = ne(), r = t.value ?? [], i = t.multiSelect === !0, c = t.customOrder === !0, s = t.mandatory === !0, u = t.disabled === !0, a = t.editable !== !1, o = t.optionsLoaded === !0, m = t.options ?? [], p = t.emptyOptionLabel ?? "", h = c && i && !u && a, N = ae({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), f = N["js.dropdownSelect.nothingFound"], T = ce(
    (g) => N["js.dropdownSelect.removeChip"].replace("{0}", g),
    [N]
  ), [w, v] = ve(!1), [C, D] = ve(""), [M, b] = ve(-1), [_, E] = ve(!1), [W, k] = ve({}), [S, B] = ve(null), [x, $] = ve(null), [q, le] = ve(null), Q = Te(null), J = Te(null), A = Te(null), j = Te(r);
  j.current = r;
  const U = Te(-1), d = Ze(
    () => new Set(r.map((g) => g.value)),
    [r]
  ), y = Ze(() => {
    let g = m.filter((L) => !d.has(L.value));
    if (C) {
      const L = C.toLowerCase();
      g = g.filter((H) => H.label.toLowerCase().includes(L));
    }
    return g;
  }, [m, d, C]);
  we(() => {
    C && y.length === 1 ? b(0) : b(-1);
  }, [y.length, C]), we(() => {
    w && o && J.current && J.current.focus();
  }, [w, o, r]), we(() => {
    var H, Z;
    if (U.current < 0) return;
    const g = U.current;
    U.current = -1;
    const L = (H = Q.current) == null ? void 0 : H.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    L && L.length > 0 ? L[Math.min(g, L.length - 1)].focus() : (Z = Q.current) == null || Z.focus();
  }, [r]), we(() => {
    if (!w) return;
    const g = (L) => {
      Q.current && !Q.current.contains(L.target) && A.current && !A.current.contains(L.target) && (v(!1), D(""));
    };
    return document.addEventListener("mousedown", g), () => document.removeEventListener("mousedown", g);
  }, [w]), we(() => {
    if (!w || !Q.current) return;
    const g = Q.current.getBoundingClientRect(), L = window.innerHeight - g.bottom, Z = L < 300 && g.top > L;
    k({
      left: g.left,
      width: g.width,
      ...Z ? { bottom: window.innerHeight - g.top } : { top: g.bottom }
    });
  }, [w]);
  const F = ce(async () => {
    if (!(u || !a) && (v(!0), D(""), b(-1), E(!1), !o))
      try {
        await n("loadOptions");
      } catch {
        E(!0);
      }
  }, [u, a, o, n]), O = ce(() => {
    var g;
    v(!1), D(""), b(-1), (g = Q.current) == null || g.focus();
  }, []), V = ce(
    (g) => {
      let L;
      if (i) {
        const H = m.find((Z) => Z.value === g);
        if (H)
          L = [...j.current, H];
        else
          return;
      } else {
        const H = m.find((Z) => Z.value === g);
        if (H)
          L = [H];
        else
          return;
      }
      j.current = L, n("valueChanged", { value: L.map((H) => H.value) }), i ? (D(""), b(-1)) : O();
    },
    [i, m, n, O]
  ), P = ce(
    (g) => {
      U.current = j.current.findIndex((H) => H.value === g);
      const L = j.current.filter((H) => H.value !== g);
      j.current = L, n("valueChanged", { value: L.map((H) => H.value) });
    },
    [n]
  ), X = ce(
    (g) => {
      g.stopPropagation(), n("valueChanged", { value: [] }), O();
    },
    [n, O]
  ), te = ce((g) => {
    D(g.target.value);
  }, []), ee = ce(
    (g) => {
      if (!w) {
        if (g.key === "ArrowDown" || g.key === "ArrowUp" || g.key === "Enter" || g.key === " ") {
          if (g.target.tagName === "BUTTON") return;
          g.preventDefault(), g.stopPropagation(), F();
        }
        return;
      }
      switch (g.key) {
        case "ArrowDown":
          g.preventDefault(), g.stopPropagation(), b(
            (L) => L < y.length - 1 ? L + 1 : 0
          );
          break;
        case "ArrowUp":
          g.preventDefault(), g.stopPropagation(), b(
            (L) => L > 0 ? L - 1 : y.length - 1
          );
          break;
        case "Enter":
          g.preventDefault(), g.stopPropagation(), M >= 0 && M < y.length && V(y[M].value);
          break;
        case "Escape":
          g.preventDefault(), g.stopPropagation(), O();
          break;
        case "Tab":
          O();
          break;
        case "Backspace":
          C === "" && i && r.length > 0 && P(r[r.length - 1].value);
          break;
      }
    },
    [
      w,
      F,
      O,
      y,
      M,
      V,
      C,
      i,
      r,
      P
    ]
  ), ie = ce(
    async (g) => {
      g.preventDefault(), E(!1);
      try {
        await n("loadOptions");
      } catch {
        E(!0);
      }
    },
    [n]
  ), ue = ce(
    (g, L) => {
      B(g), L.dataTransfer.effectAllowed = "move", L.dataTransfer.setData("text/plain", String(g));
    },
    []
  ), me = ce(
    (g, L) => {
      if (L.preventDefault(), L.dataTransfer.dropEffect = "move", S === null || S === g) {
        $(null), le(null);
        return;
      }
      const H = L.currentTarget.getBoundingClientRect(), Z = H.left + H.width / 2, re = L.clientX < Z ? "before" : "after";
      $(g), le(re);
    },
    [S]
  ), he = ce(
    (g) => {
      if (g.preventDefault(), S === null || x === null || q === null || S === x) return;
      const L = [...j.current], [H] = L.splice(S, 1);
      let Z = x;
      S < x ? Z = q === "before" ? Z - 1 : Z : Z = q === "before" ? Z : Z + 1, L.splice(Z, 0, H), j.current = L, n("valueChanged", { value: L.map((re) => re.value) }), B(null), $(null), le(null);
    },
    [S, x, q, n]
  ), be = ce(() => {
    B(null), $(null), le(null);
  }, []);
  if (we(() => {
    if (M < 0 || !A.current) return;
    const g = A.current.querySelector(
      `[id="${l}-opt-${M}"]`
    );
    g && g.scrollIntoView({ block: "nearest" });
  }, [M, l]), !a)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, p) : r.map((g) => /* @__PURE__ */ e.createElement("span", { key: g.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(tt, { image: g.image }), /* @__PURE__ */ e.createElement("span", null, g.label))));
  const R = !s && r.length > 0 && !u, I = w ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: A,
      className: "tlDropdownSelect__dropdown",
      style: W
    },
    (o || _) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: J,
        type: "text",
        className: "tlDropdownSelect__search",
        value: C,
        onChange: te,
        onKeyDown: ee,
        placeholder: N["js.dropdownSelect.filterPlaceholder"],
        "aria-label": N["js.dropdownSelect.filterPlaceholder"],
        "aria-activedescendant": M >= 0 ? `${l}-opt-${M}` : void 0,
        "aria-controls": `${l}-listbox`
      }
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        id: `${l}-listbox`,
        role: "listbox",
        className: "tlDropdownSelect__list"
      },
      !o && !_ && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      _ && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: ie }, N["js.dropdownSelect.error"])),
      o && y.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, f),
      o && y.map((g, L) => /* @__PURE__ */ e.createElement(
        kl,
        {
          key: g.value,
          id: `${l}-opt-${L}`,
          option: g,
          highlighted: L === M,
          searchTerm: C,
          onSelect: V,
          onMouseEnter: () => b(L)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: Q,
      className: "tlDropdownSelect" + (w ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": w,
      "aria-haspopup": "listbox",
      "aria-owns": w ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: w ? void 0 : F,
      onKeyDown: ee
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : r.map((g, L) => {
      let H = "";
      return S === L ? H = "tlDropdownSelect__chip--dragging" : x === L && q === "before" ? H = "tlDropdownSelect__chip--dropBefore" : x === L && q === "after" && (H = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        wl,
        {
          key: g.value,
          option: g,
          removable: !u && (i || !s),
          onRemove: P,
          removeLabel: T(g.label),
          draggable: h,
          onDragStart: h ? (Z) => ue(L, Z) : void 0,
          onDragOver: h ? (Z) => me(L, Z) : void 0,
          onDrop: h ? he : void 0,
          onDragEnd: h ? be : void 0,
          dragClassName: h ? H : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, R && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: X,
        "aria-label": N["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, w ? "▲" : "▼"))
  ), I && yl.createPortal(I, document.body));
}, { useCallback: Ke, useRef: Nl } = e, Ct = "application/x-tl-color", Tl = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: r,
  onSwap: i,
  onReplace: c
}) => {
  const s = Nl(null), u = Ke(
    (m) => (p) => {
      s.current = m, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), a = Ke((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), o = Ke(
    (m) => (p) => {
      p.preventDefault();
      const h = p.dataTransfer.getData(Ct);
      h ? c(m, h) : s.current !== null && s.current !== m && i(s.current, m), s.current = null;
    },
    [i, c]
  );
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__palette",
      style: { gridTemplateColumns: `repeat(${t}, 1fr)` }
    },
    l.map((m, p) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: p,
        className: "tlColorInput__paletteCell" + (m == null ? " tlColorInput__paletteCell--empty" : ""),
        style: m != null ? { backgroundColor: m } : void 0,
        title: m ?? "",
        draggable: m != null,
        onClick: m != null ? () => n(m) : void 0,
        onDoubleClick: m != null ? () => r(m) : void 0,
        onDragStart: m != null ? u(p) : void 0,
        onDragOver: a,
        onDrop: o(p)
      }
    ))
  );
};
function yt(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function Qe(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function wt(l) {
  if (!Qe(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function kt(l, t, n) {
  const r = (i) => yt(i).toString(16).padStart(2, "0");
  return "#" + r(l) + r(t) + r(n);
}
function Rl(l, t, n) {
  const r = l / 255, i = t / 255, c = n / 255, s = Math.max(r, i, c), u = Math.min(r, i, c), a = s - u;
  let o = 0;
  a !== 0 && (s === r ? o = (i - c) / a % 6 : s === i ? o = (c - r) / a + 2 : o = (r - i) / a + 4, o *= 60, o < 0 && (o += 360));
  const m = s === 0 ? 0 : a / s;
  return [o, m, s];
}
function xl(l, t, n) {
  const r = n * t, i = r * (1 - Math.abs(l / 60 % 2 - 1)), c = n - r;
  let s = 0, u = 0, a = 0;
  return l < 60 ? (s = r, u = i, a = 0) : l < 120 ? (s = i, u = r, a = 0) : l < 180 ? (s = 0, u = r, a = i) : l < 240 ? (s = 0, u = i, a = r) : l < 300 ? (s = i, u = 0, a = r) : (s = r, u = 0, a = i), [
    Math.round((s + c) * 255),
    Math.round((u + c) * 255),
    Math.round((a + c) * 255)
  ];
}
function Ll(l) {
  return Rl(...wt(l));
}
function Ye(l, t, n) {
  return kt(...xl(l, t, n));
}
const { useCallback: ke, useRef: ft } = e, Dl = ({ color: l, onColorChange: t }) => {
  const [n, r, i] = Ll(l), c = ft(null), s = ft(null), u = ke(
    (f, T) => {
      var D;
      const w = (D = c.current) == null ? void 0 : D.getBoundingClientRect();
      if (!w) return;
      const v = Math.max(0, Math.min(1, (f - w.left) / w.width)), C = Math.max(0, Math.min(1, 1 - (T - w.top) / w.height));
      t(Ye(n, v, C));
    },
    [n, t]
  ), a = ke(
    (f) => {
      f.preventDefault(), f.target.setPointerCapture(f.pointerId), u(f.clientX, f.clientY);
    },
    [u]
  ), o = ke(
    (f) => {
      f.buttons !== 0 && u(f.clientX, f.clientY);
    },
    [u]
  ), m = ke(
    (f) => {
      var C;
      const T = (C = s.current) == null ? void 0 : C.getBoundingClientRect();
      if (!T) return;
      const v = Math.max(0, Math.min(1, (f - T.top) / T.height)) * 360;
      t(Ye(v, r, i));
    },
    [r, i, t]
  ), p = ke(
    (f) => {
      f.preventDefault(), f.target.setPointerCapture(f.pointerId), m(f.clientY);
    },
    [m]
  ), h = ke(
    (f) => {
      f.buttons !== 0 && m(f.clientY);
    },
    [m]
  ), N = Ye(n, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: c,
      className: "tlColorInput__svField",
      style: { backgroundColor: N },
      onPointerDown: a,
      onPointerMove: o
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__svHandle",
        style: { left: `${r * 100}%`, top: `${(1 - i) * 100}%` }
      }
    )
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: s,
      className: "tlColorInput__hueSlider",
      onPointerDown: p,
      onPointerMove: h
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__hueHandle",
        style: { top: `${n / 360 * 100}%` }
      }
    )
  ));
};
function Il(l, t) {
  const n = t.toUpperCase();
  return l.some((r) => r != null && r.toUpperCase() === n);
}
const Ml = {
  "js.colorInput.paletteTab": "Color Palette",
  "js.colorInput.mixerTab": "Color Mixer",
  "js.colorInput.current": "Current",
  "js.colorInput.new": "New",
  "js.colorInput.red": "Red",
  "js.colorInput.green": "Green",
  "js.colorInput.blue": "Blue",
  "js.colorInput.hex": "Hex",
  "js.colorInput.clear": "Clear",
  "js.colorInput.reset": "Reset",
  "js.colorInput.cancel": "Cancel",
  "js.colorInput.ok": "OK"
}, { useState: Me, useCallback: fe, useEffect: Ge, useRef: Pl, useLayoutEffect: jl } = e, Al = ({
  anchorRef: l,
  currentColor: t,
  palette: n,
  paletteColumns: r,
  defaultPalette: i,
  canReset: c,
  onConfirm: s,
  onCancel: u,
  onPaletteChange: a
}) => {
  const [o, m] = Me("palette"), [p, h] = Me(t), N = Pl(null), f = ae(Ml), [T, w] = Me(null);
  jl(() => {
    if (!l.current || !N.current) return;
    const A = l.current.getBoundingClientRect(), j = N.current.getBoundingClientRect();
    let U = A.bottom + 4, d = A.left;
    U + j.height > window.innerHeight && (U = A.top - j.height - 4), d + j.width > window.innerWidth && (d = Math.max(0, A.right - j.width)), w({ top: U, left: d });
  }, [l]);
  const v = p != null, [C, D, M] = v ? wt(p) : [0, 0, 0], [b, _] = Me((p == null ? void 0 : p.toUpperCase()) ?? "");
  Ge(() => {
    _((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), Ge(() => {
    const A = (j) => {
      j.key === "Escape" && u();
    };
    return document.addEventListener("keydown", A), () => document.removeEventListener("keydown", A);
  }, [u]), Ge(() => {
    const A = (U) => {
      N.current && !N.current.contains(U.target) && u();
    }, j = setTimeout(() => document.addEventListener("mousedown", A), 0);
    return () => {
      clearTimeout(j), document.removeEventListener("mousedown", A);
    };
  }, [u]);
  const E = fe(
    (A) => (j) => {
      const U = parseInt(j.target.value, 10);
      if (isNaN(U)) return;
      const d = yt(U);
      h(kt(A === "r" ? d : C, A === "g" ? d : D, A === "b" ? d : M));
    },
    [C, D, M]
  ), W = fe(
    (A) => {
      if (p != null) {
        A.dataTransfer.setData(Ct, p.toUpperCase()), A.dataTransfer.effectAllowed = "move";
        const j = document.createElement("div");
        j.style.width = "33px", j.style.height = "33px", j.style.backgroundColor = p, j.style.borderRadius = "3px", j.style.border = "1px solid rgba(0,0,0,0.1)", j.style.position = "absolute", j.style.top = "-9999px", document.body.appendChild(j), A.dataTransfer.setDragImage(j, 16, 16), requestAnimationFrame(() => document.body.removeChild(j));
      }
    },
    [p]
  ), k = fe((A) => {
    const j = A.target.value;
    _(j), Qe(j) && h(j);
  }, []), S = fe(() => {
    h(null);
  }, []), B = fe((A) => {
    h(A);
  }, []), x = fe(
    (A) => {
      s(A);
    },
    [s]
  ), $ = fe(
    (A, j) => {
      const U = [...n], d = U[A];
      U[A] = U[j], U[j] = d, a(U);
    },
    [n, a]
  ), q = fe(
    (A, j) => {
      const U = [...n];
      U[A] = j, a(U);
    },
    [n, a]
  ), le = fe(() => {
    a([...i]);
  }, [i, a]), Q = fe(
    (A) => {
      if (Il(n, A)) return;
      const j = n.indexOf(null);
      if (j < 0) return;
      const U = [...n];
      U[j] = A.toUpperCase(), a(U);
    },
    [n, a]
  ), J = fe(() => {
    p != null && Q(p), s(p);
  }, [p, s, Q]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: N,
      style: T ? { top: T.top, left: T.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (o === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("palette")
      },
      f["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (o === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("mixer")
      },
      f["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, o === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      Tl,
      {
        colors: n,
        columns: r,
        onSelect: B,
        onConfirm: x,
        onSwap: $,
        onReplace: q
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: le }, f["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(Dl, { color: p ?? "#000000", onColorChange: h }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, f["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, f["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (v ? "" : " tlColorInput--noColor"),
        style: v ? { backgroundColor: p } : void 0,
        draggable: v,
        onDragStart: v ? W : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? C : "",
        onChange: E("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? D : "",
        onChange: E("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? M : "",
        onChange: E("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (b !== "" && !Qe(b) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: b,
        onChange: k
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, c && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: S }, f["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, f["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: J }, f["js.colorInput.ok"]))
  );
}, Bl = { "js.colorInput.chooseColor": "Choose color" }, { useState: $l, useCallback: Pe, useRef: Ol } = e, Fl = ({ controlId: l, state: t }) => {
  const n = ne(), r = ae(Bl), [i, c] = $l(!1), s = Ol(null), u = t.value, a = t.editable !== !1, o = t.palette ?? [], m = t.paletteColumns ?? 6, p = t.defaultPalette ?? o, h = Pe(() => {
    a && c(!0);
  }, [a]), N = Pe(
    (w) => {
      c(!1), n("valueChanged", { value: w });
    },
    [n]
  ), f = Pe(() => {
    c(!1);
  }, []), T = Pe(
    (w) => {
      n("paletteChanged", { palette: w });
    },
    [n]
  );
  return a ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: s,
      className: "tlColorInput__swatch" + (u == null ? " tlColorInput__swatch--noColor" : ""),
      style: u != null ? { backgroundColor: u } : void 0,
      onClick: h,
      disabled: t.disabled === !0,
      title: u ?? "",
      "aria-label": r["js.colorInput.chooseColor"]
    }
  ), i && /* @__PURE__ */ e.createElement(
    Al,
    {
      anchorRef: s,
      currentColor: u,
      palette: o,
      paletteColumns: m,
      defaultPalette: p,
      canReset: t.canReset !== !1,
      onConfirm: N,
      onCancel: f,
      onPaletteChange: T
    }
  )) : /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlColorInput tlColorInput--immutable" + (u == null ? " tlColorInput--noColor" : ""),
      style: u != null ? { backgroundColor: u } : void 0,
      title: u ?? ""
    }
  );
}, { useState: Re, useCallback: Ee, useEffect: je, useRef: ht, useLayoutEffect: Hl, useMemo: Wl } = e, zl = {
  "js.iconSelect.simpleTab": "Simple",
  "js.iconSelect.advancedTab": "Advanced",
  "js.iconSelect.filterPlaceholder": "Filter icons…",
  "js.iconSelect.noResults": "No icons found",
  "js.iconSelect.loading": "Loading…",
  "js.iconSelect.loadError": "Failed to load. Click to retry.",
  "js.iconSelect.classLabel": "Class",
  "js.iconSelect.previewLabel": "Preview",
  "js.iconSelect.cancel": "Cancel",
  "js.iconSelect.ok": "OK",
  "js.iconSelect.clear": "Clear icon",
  "js.iconSelect.clearFilter": "Clear filter"
}, Ul = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: r,
  onSelect: i,
  onCancel: c,
  onLoadIcons: s
}) => {
  const u = ae(zl), [a, o] = Re("simple"), [m, p] = Re(""), [h, N] = Re(t ?? ""), [f, T] = Re(!1), [w, v] = Re(null), C = ht(null), D = ht(null);
  Hl(() => {
    if (!l.current || !C.current) return;
    const x = l.current.getBoundingClientRect(), $ = C.current.getBoundingClientRect();
    let q = x.bottom + 4, le = x.left;
    q + $.height > window.innerHeight && (q = x.top - $.height - 4), le + $.width > window.innerWidth && (le = Math.max(0, x.right - $.width)), v({ top: q, left: le });
  }, [l]), je(() => {
    !r && !f && s().catch(() => T(!0));
  }, [r, f, s]), je(() => {
    r && D.current && D.current.focus();
  }, [r]), je(() => {
    const x = ($) => {
      $.key === "Escape" && c();
    };
    return document.addEventListener("keydown", x), () => document.removeEventListener("keydown", x);
  }, [c]), je(() => {
    const x = (q) => {
      C.current && !C.current.contains(q.target) && c();
    }, $ = setTimeout(() => document.addEventListener("mousedown", x), 0);
    return () => {
      clearTimeout($), document.removeEventListener("mousedown", x);
    };
  }, [c]);
  const M = Wl(() => {
    if (!m) return n;
    const x = m.toLowerCase();
    return n.filter(
      ($) => $.prefix.toLowerCase().includes(x) || $.label.toLowerCase().includes(x) || $.terms != null && $.terms.some((q) => q.includes(x))
    );
  }, [n, m]), b = Ee((x) => {
    p(x.target.value);
  }, []), _ = Ee(
    (x) => {
      i(x);
    },
    [i]
  ), E = Ee((x) => {
    N(x);
  }, []), W = Ee((x) => {
    N(x.target.value);
  }, []), k = Ee(() => {
    i(h || null);
  }, [h, i]), S = Ee(() => {
    i(null);
  }, [i]), B = Ee(async (x) => {
    x.preventDefault(), T(!1);
    try {
      await s();
    } catch {
      T(!0);
    }
  }, [s]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlIconSelect__popup",
      ref: C,
      style: w ? { top: w.top, left: w.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (a === "simple" ? " tlIconSelect__tab--active" : ""),
        onClick: () => o("simple")
      },
      u["js.iconSelect.simpleTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (a === "advanced" ? " tlIconSelect__tab--active" : ""),
        onClick: () => o("advanced")
      },
      u["js.iconSelect.advancedTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__searchIcon", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-magnifying-glass" })), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: D,
        type: "text",
        className: "tlIconSelect__search",
        value: m,
        onChange: b,
        placeholder: u["js.iconSelect.filterPlaceholder"],
        "aria-label": u["js.iconSelect.filterPlaceholder"]
      }
    ), m && /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__resetBtn",
        onClick: () => p(""),
        title: u["js.iconSelect.clearFilter"]
      },
      "×"
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlIconSelect__grid",
        role: "listbox"
      },
      !r && !f && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      f && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: B }, u["js.iconSelect.loadError"])),
      r && M.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      r && M.map(
        (x) => x.variants.map(($) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: $.encoded,
            className: "tlIconSelect__iconCell" + ($.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": $.encoded === t,
            tabIndex: 0,
            title: x.label,
            onClick: () => a === "simple" ? _($.encoded) : E($.encoded),
            onKeyDown: (q) => {
              (q.key === "Enter" || q.key === " ") && (q.preventDefault(), a === "simple" ? _($.encoded) : E($.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(_e, { encoded: $.encoded })
        ))
      )
    ),
    a === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: h,
        onChange: W
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, h && /* @__PURE__ */ e.createElement(_e, { encoded: h })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, h ? h.startsWith("css:") ? h.substring(4) : h : ""))),
    a === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: c }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: S }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: k }, u["js.iconSelect.ok"]))
  );
}, Vl = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: Kl, useCallback: Ae, useRef: Yl } = e, Gl = ({ controlId: l, state: t }) => {
  const n = ne(), r = ae(Vl), [i, c] = Kl(!1), s = Yl(null), u = t.value, a = t.editable !== !1, o = t.disabled === !0, m = t.icons ?? [], p = t.iconsLoaded === !0, h = Ae(() => {
    a && !o && c(!0);
  }, [a, o]), N = Ae(
    (w) => {
      c(!1), n("valueChanged", { value: w });
    },
    [n]
  ), f = Ae(() => {
    c(!1);
  }, []), T = Ae(async () => {
    await n("loadIcons");
  }, [n]);
  return a ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: s,
      className: "tlIconSelect__swatch" + (u == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: h,
      disabled: o,
      title: u ?? "",
      "aria-label": r["js.iconSelect.chooseIcon"]
    },
    u ? /* @__PURE__ */ e.createElement(_e, { encoded: u }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), i && /* @__PURE__ */ e.createElement(
    Ul,
    {
      anchorRef: s,
      currentValue: u,
      icons: m,
      iconsLoaded: p,
      onSelect: N,
      onCancel: f,
      onLoadIcons: T
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, u ? /* @__PURE__ */ e.createElement(_e, { encoded: u }) : null));
}, { useCallback: Se, useEffect: Xl, useMemo: _t, useRef: ql, useState: Xe } = e, Zl = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, Ql = [1, 2, 3, 4];
function Jl(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const r = parseFloat(n[1]), i = n[2] || "px";
  return i === "rem" || i === "em" ? r * t : r;
}
function er(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let r = 1;
  for (const i of Ql)
    n >= i && (r = i);
  return r;
}
function tr(l, t) {
  const n = Zl[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function nr(l, t) {
  const n = Math.max(1, t), r = {}, i = (p, h) => !!(r[p] && r[p][h]), c = (p, h) => {
    r[p] || (r[p] = {}), r[p][h] = !0;
  }, s = [];
  let u = 0, a = 0;
  const o = (p) => {
    let h = null;
    for (const f of s) f.rowStart === p && (h = f);
    if (!h) return;
    let N = h.colEnd;
    for (; N < n && !i(p, N); ) N++;
    if (N !== h.colEnd) {
      for (let f = h.rowStart; f < h.rowEnd; f++)
        for (let T = h.colEnd; T < N; T++) c(f, T);
      h.colEnd = N;
    }
  };
  for (const p of l) {
    const h = n <= 1 ? 1 : Math.max(1, p.rowSpan || 1);
    let N = Math.min(tr(p.width, n), n);
    for (; i(u, a); )
      a++, a >= n && (a = 0, u++);
    let f = 0;
    for (let D = a; D < n && !i(u, D); D++)
      f++;
    if (N > f) {
      for (o(u), a = 0, u++; i(u, a); )
        a++, a >= n && (a = 0, u++);
      f = 0;
      for (let D = a; D < n && !i(u, D); D++)
        f++;
      N = Math.min(N, f);
    }
    const T = a, w = a + N, v = u, C = u + h;
    s.push({ id: p.id, colStart: T, colEnd: w, rowStart: v, rowEnd: C });
    for (let D = v; D < C; D++)
      for (let M = T; M < w; M++) c(D, M);
    a = w, a >= n && (a = 0, u++);
  }
  o(u);
  let m = 0;
  for (const p of s) p.rowEnd > m && (m = p.rowEnd);
  for (let p = 1; p < m; p++)
    for (let h = 0; h < n; h++) {
      if (i(p, h)) continue;
      const N = s.find((f) => f.rowEnd === p && f.colStart <= h && h < f.colEnd);
      if (N) {
        N.rowEnd = p + 1;
        for (let f = N.colStart; f < N.colEnd; f++) c(p, f);
      }
    }
  return s;
}
const lr = ({ controlId: l }) => {
  const t = G(), n = ne(), r = t.minColWidth ?? "16rem", i = (t.children ?? []).filter((_) => _ && _.id), c = ql(null), [s, u] = Xe(1), a = t.editMode === !0;
  Xl(() => {
    const _ = c.current;
    if (!_) return;
    const E = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, W = Jl(r, E), k = () => u(er(_.clientWidth, W));
    k();
    const S = new ResizeObserver(k);
    return S.observe(_), () => S.disconnect();
  }, [r]);
  const o = _t(() => nr(i, s), [i, s]), m = _t(() => {
    const _ = {};
    for (const E of o) _[E.id] = E;
    return _;
  }, [o]), [p, h] = Xe(null), [N, f] = Xe(null), T = Se((_, E) => {
    if (!a) {
      _.preventDefault();
      return;
    }
    h(E), _.dataTransfer.effectAllowed = "move", _.dataTransfer.setData("text/plain", E);
  }, [a]), w = Se((_, E) => {
    if (!a || !p || p === E) return;
    _.preventDefault(), _.dataTransfer.dropEffect = "move";
    const W = _.currentTarget.getBoundingClientRect(), k = _.clientX < W.left + W.width / 2;
    f((S) => S && S.id === E && S.before === k ? S : { id: E, before: k });
  }, [a, p]), v = Se(() => {
  }, []), C = Se((_, E, W) => {
    const k = i.map(($) => $.id), S = k.indexOf(_);
    if (S < 0) return;
    k.splice(S, 1);
    const B = k.indexOf(E);
    if (B < 0) {
      k.splice(S, 0, _);
      return;
    }
    const x = W ? B : B + 1;
    k.splice(x, 0, _), n("reorder", { order: k });
  }, [i, n]), D = Se((_, E) => {
    if (!a || !p || p === E) return;
    _.preventDefault();
    const W = _.currentTarget.getBoundingClientRect(), k = _.clientX < W.left + W.width / 2;
    C(p, E, k), h(null), f(null);
  }, [a, p, C]), M = Se(() => {
    h(null), f(null);
  }, []), b = {
    display: "grid",
    gridTemplateColumns: `repeat(${s}, 1fr)`,
    gap: "1rem"
  };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: c,
      className: "tlDashboard" + (a ? " tlDashboard--edit" : "")
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: b }, i.map((_) => {
      const E = m[_.id];
      if (!E) return null;
      const W = {
        gridColumn: `${E.colStart + 1} / ${E.colEnd + 1}`,
        gridRow: `${E.rowStart + 1} / ${E.rowEnd + 1}`
      }, k = ["tlDashboard__tile"];
      return p === _.id && k.push("tlDashboard__tile--dragging"), N && N.id === _.id && k.push(N.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: _.id,
          className: k.join(" "),
          style: W,
          draggable: a,
          onDragStart: (S) => T(S, _.id),
          onDragOver: (S) => w(S, _.id),
          onDragLeave: v,
          onDrop: (S) => D(S, _.id),
          onDragEnd: M
        },
        /* @__PURE__ */ e.createElement(K, { control: _.control }),
        a && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: rr, useRef: bt, useState: or, useEffect: vt } = e, ar = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, r) => /* @__PURE__ */ e.createElement("span", { key: r, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(K, { control: n }))));
}, sr = ({ group: l }) => {
  var u, a;
  const [t, n] = or(!1), r = bt(null), i = bt(null), c = rr(() => {
    n((o) => !o);
  }, []);
  vt(() => {
    if (!t) return;
    const o = (m) => {
      i.current && !i.current.contains(m.target) && r.current && !r.current.contains(m.target) && n(!1);
    };
    return document.addEventListener("mousedown", o), () => document.removeEventListener("mousedown", o);
  }, [t]), vt(() => {
    if (!t) return;
    const o = (m) => {
      m.key === "Escape" && n(!1);
    };
    return document.addEventListener("keydown", o), () => document.removeEventListener("keydown", o);
  }, [t]);
  const s = l.items.filter((o) => o != null);
  return s.length === 0 ? null : s.length === 1 && !((u = l.subGroups) != null && u.length) ? /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(K, { control: s[0] }))) : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--menu" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: r,
      type: "button",
      className: "tlToolbar__menuTrigger",
      onClick: c,
      "aria-expanded": t,
      "aria-haspopup": "true"
    },
    l.icon && /* @__PURE__ */ e.createElement(_e, { encoded: l.icon, className: "tlToolbar__menuIcon" }),
    /* @__PURE__ */ e.createElement("span", null, l.label ?? l.name),
    /* @__PURE__ */ e.createElement("svg", { className: "tlToolbar__chevron", viewBox: "0 0 24 24", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("polyline", { points: "6,9 12,15 18,9" }))
  ), t && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: i,
      className: "tlToolbar__dropdown",
      role: "menu",
      onClick: () => n(!1)
    },
    s.map((o, m) => /* @__PURE__ */ e.createElement("div", { key: m, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(K, { control: o }))),
    (a = l.subGroups) == null ? void 0 : a.map((o, m) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${m}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), o.items.map((p, h) => /* @__PURE__ */ e.createElement("div", { key: h, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(K, { control: p })))))
  ));
}, cr = ({ controlId: l }) => {
  const r = (G().groups ?? []).filter((i) => i.items.some((c) => c != null));
  return r.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, r.map((i, c) => /* @__PURE__ */ e.createElement(e.Fragment, { key: i.name }, c > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), i.display === "menu" ? /* @__PURE__ */ e.createElement(sr, { group: i }) : /* @__PURE__ */ e.createElement(ar, { group: i }))));
};
z("TLButton", Ot);
z("TLToggleButton", Ht);
z("TLTextInput", Rt);
z("TLNumberInput", Lt);
z("TLDatePicker", It);
z("TLSelect", Pt);
z("TLCheckbox", At);
z("TLTable", Bt);
z("TLCounter", Wt);
z("TLTabBar", Ut);
z("TLFieldList", Vt);
z("TLAudioRecorder", Yt);
z("TLAudioPlayer", Xt);
z("TLFileUpload", Zt);
z("TLDownload", Jt);
z("TLPhotoCapture", tn);
z("TLPhotoViewer", ln);
z("TLSplitPanel", rn);
z("TLPanel", mn);
z("TLMaximizeRoot", pn);
z("TLDeckPane", fn);
z("TLSidebar", yn);
z("TLStack", wn);
z("TLGrid", kn);
z("TLCard", Sn);
z("TLAppBar", Nn);
z("TLBreadcrumb", Rn);
z("TLBottomBar", Ln);
z("TLDialog", In);
z("TLDialogManager", jn);
z("TLWindow", $n);
z("TLDrawer", Wn);
z("TLContextMenuRegion", Un);
z("TLSnackbar", Gn);
z("TLMenu", qn);
z("TLAppShell", Zn);
z("TLText", Qn);
z("TLTableView", el);
z("TLFormLayout", sl);
z("TLFormGroup", ul);
z("TLFormField", fl);
z("TLResourceCell", hl);
z("TLTreeView", bl);
z("TLDropdownSelect", Sl);
z("TLColorInput", Fl);
z("TLIconSelect", Gl);
z("TLDashboard", lr);
z("TLToolbar", cr);
