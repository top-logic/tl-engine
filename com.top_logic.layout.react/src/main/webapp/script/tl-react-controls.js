import { React as e, useTLFieldValue as Re, useTLCommand as ae, useTLState as G, useKeyboardBinding as de, useTLUpload as Fe, TLChild as K, useI18N as ie, useTLDataUrl as Oe, scrollToAnchor as Yt, useStandaloneKeyboardScope as Le, KeyboardScopeProvider as mt, useFocusTrap as pt, CMD_VALUE_CHANGED as We, anchoredOverlayProps as Gt, register as U } from "tl-react-bridge";
const { useCallback: _t, useRef: Xt } = e, qt = 300, Zt = ({ controlId: l, state: t }) => {
  const [n, a, c] = Re({ debounceMs: qt }), s = ae(), i = Xt(!1), u = _t(
    (w) => {
      i.current = !0, a(w.target.value);
    },
    [a]
  ), r = t.commitOnBlur === !0, o = _t(async () => {
    await c(), r && i.current && (i.current = !1, s("commit"));
  }, [c, r, s]), m = t.multiline === !0;
  if (t.editable === !1) {
    const w = "tlReactTextInput tlReactTextInput--immutable" + (m ? " tlReactTextInput--multiline" : "");
    return /* @__PURE__ */ e.createElement(
      "span",
      {
        id: l,
        className: w,
        style: m ? { whiteSpace: "pre-wrap" } : void 0
      },
      n ?? ""
    );
  }
  const p = t.hasError === !0, f = t.hasWarnings === !0, _ = t.errorMessage, b = [
    "tlReactTextInput",
    m ? "tlReactTextInput--multiline" : "",
    p ? "tlReactTextInput--error" : "",
    !p && f ? "tlReactTextInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, m ? /* @__PURE__ */ e.createElement(
    "textarea",
    {
      rows: t.rows ?? 3,
      value: n ?? "",
      placeholder: t.placeholder ?? void 0,
      onChange: u,
      onBlur: o,
      disabled: t.disabled === !0,
      className: b,
      "aria-invalid": p || void 0,
      title: p && _ ? _ : void 0
    }
  ) : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: n ?? "",
      placeholder: t.placeholder ?? void 0,
      onChange: u,
      onBlur: o,
      disabled: t.disabled === !0,
      className: b,
      "aria-invalid": p || void 0,
      title: p && _ ? _ : void 0
    }
  ));
}, { useCallback: gt } = e, Qt = 300, Jt = ({ controlId: l, state: t }) => {
  const [n, a, c] = Re({ debounceMs: Qt }), s = gt(
    (p) => {
      a(p.target.value);
    },
    [a]
  ), i = gt(() => {
    c();
  }, [c]);
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, "••••••••");
  const u = t.hasError === !0, r = t.hasWarnings === !0, o = t.errorMessage, m = [
    "tlReactTextInput",
    u ? "tlReactTextInput--error" : "",
    !u && r ? "tlReactTextInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "password",
      value: n ?? "",
      onChange: s,
      onBlur: i,
      disabled: t.disabled === !0,
      className: m,
      "aria-invalid": u || void 0,
      title: u && o ? o : void 0
    }
  ));
}, { useCallback: vt } = e, en = 300, tn = ({ controlId: l, state: t, config: n }) => {
  const [a, c, s] = Re({ debounceMs: en }), i = vt(
    (f) => {
      const _ = f.target.value;
      c(_ === "" ? null : _);
    },
    [c]
  ), u = vt(() => {
    s();
  }, [s]);
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, a != null ? String(a) : "");
  const r = t.hasError === !0, o = t.hasWarnings === !0, m = t.errorMessage, p = [
    "tlReactNumberInput",
    r ? "tlReactNumberInput--error" : "",
    !r && o ? "tlReactNumberInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      inputMode: n != null && n.decimal ? "decimal" : "numeric",
      value: a != null ? String(a) : "",
      onChange: i,
      onBlur: u,
      disabled: t.disabled === !0,
      className: p,
      "aria-invalid": r || void 0,
      title: r && m ? m : void 0
    }
  ));
}, { useCallback: nn } = e, ln = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), c = nn(
    (r) => {
      a(r.target.value || null);
    },
    [a]
  );
  if (t.editable === !1) {
    const r = t.displayValue ?? n ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, r);
  }
  const s = t.hasError === !0, i = t.hasWarnings === !0, u = [
    "tlReactDatePicker",
    s ? "tlReactDatePicker--error" : "",
    !s && i ? "tlReactDatePicker--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: n ?? "",
      onChange: c,
      disabled: t.disabled === !0,
      className: u,
      "aria-invalid": s || void 0
    }
  ));
}, { useCallback: an } = e, rn = ({ controlId: l, state: t, config: n }) => {
  var m;
  const [a, c] = Re(), s = an(
    (p) => {
      c(p.target.value || null);
    },
    [c]
  ), i = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const p = ((m = i.find((f) => f.value === a)) == null ? void 0 : m.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, p);
  }
  const u = t.hasError === !0, r = t.hasWarnings === !0, o = [
    "tlReactSelect",
    u ? "tlReactSelect--error" : "",
    !u && r ? "tlReactSelect--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "select",
    {
      value: a ?? "",
      onChange: s,
      disabled: t.disabled === !0,
      className: o,
      "aria-invalid": u || void 0
    },
    t.nullable !== !1 && /* @__PURE__ */ e.createElement("option", { value: "" }),
    i.map((p) => /* @__PURE__ */ e.createElement("option", { key: p.value, value: p.value }, p.label))
  ));
}, { useCallback: on } = e, sn = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), c = on(
    (r) => {
      a(r.target.checked);
    },
    [a]
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
  const s = t.hasError === !0, i = t.hasWarnings === !0, u = [
    "tlReactCheckbox",
    s ? "tlReactCheckbox--error" : "",
    !s && i ? "tlReactCheckbox--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: l,
      checked: n === !0,
      onChange: c,
      disabled: t.disabled === !0,
      className: u,
      "aria-invalid": s || void 0
    }
  );
};
function we({ encoded: l, className: t }) {
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
const { useCallback: cn } = e, un = ({ controlId: l, command: t, label: n, image: a, disabled: c, displayMode: s }) => {
  const i = G(), u = ae(), r = t ?? "click", o = n ?? i.label, m = a ?? i.image, p = c ?? i.disabled === !0, f = s ?? i.displayMode ?? "label-only", _ = i.hidden === !0, b = i.tooltip, w = i.appearance, E = i.size, g = i.navigateUrl, v = cn(() => {
    if (g) {
      window.location.assign(g);
      return;
    }
    u(r);
  }, [u, r, g]), x = i.keyGesture;
  de(x, () => p || _ ? !1 : (v(), !0));
  const L = f === "icon-only", C = f === "label-only" || f === "icon-label" || L && !m, k = b ?? (L ? o : void 0), h = k ? `text:${k}` : void 0;
  return _ ? null : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: v,
      disabled: p,
      className: "tlReactButton" + (L ? " tlReactButton--iconOnly" : "") + (f === "label-only" ? " tlReactButton--labelOnly" : "") + (w === "link" ? " tlReactButton--link" : "") + (w === "primary" ? " tlReactButton--primary" : "") + (E === "small" ? " tlReactButton--small" : "") + (E === "large" ? " tlReactButton--large" : ""),
      "data-tooltip": h,
      "aria-label": m || L ? o : void 0
    },
    m && /* @__PURE__ */ e.createElement(we, { encoded: m, className: "tlReactButton__image" }),
    C && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, o)
  );
}, dn = ({ controlId: l }) => {
  const t = G(), n = Fe(), a = e.useRef(null), [c, s] = e.useState(!1), i = t.label ?? "", u = t.image, r = t.disabled === !0, o = t.hidden === !0, m = t.displayMode ?? "label-only", p = t.appearance, f = t.accept, _ = t.multiple === !0, b = e.useCallback(() => {
    var L;
    r || c || (L = a.current) == null || L.click();
  }, [r, c]), w = e.useCallback(async (L) => {
    const C = L.target.files;
    if (!C || C.length === 0) return;
    const k = new FormData();
    for (let h = 0; h < C.length; h++)
      k.append("file", C[h], C[h].name);
    L.target.value = "", s(!0);
    try {
      await n(k);
    } finally {
      s(!1);
    }
  }, [n]), E = m === "icon-only", g = m === "icon-only" || m === "icon-label", v = m === "label-only" || m === "icon-label" || E && !u, x = r || c;
  return /* @__PURE__ */ e.createElement("span", { id: l, style: { display: "contents" } }, /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: a,
      type: "file",
      accept: f && f !== "*" ? f : void 0,
      multiple: _ || void 0,
      onChange: w,
      style: { display: "none" }
    }
  ), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: b,
      disabled: x,
      style: o ? { display: "none" } : void 0,
      className: "tlReactButton" + (E ? " tlReactButton--iconOnly" : "") + (p === "link" ? " tlReactButton--link" : "") + (p === "primary" ? " tlReactButton--primary" : ""),
      "aria-label": E ? i : void 0
    },
    g && u && /* @__PURE__ */ e.createElement(we, { encoded: u, className: "tlReactButton__image" }),
    v && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, i)
  ));
}, { useCallback: mn } = e, pn = ({ controlId: l, command: t, label: n, active: a, disabled: c }) => {
  const s = G(), i = ae(), u = t ?? "click", r = n ?? s.label, o = a ?? s.active === !0, m = c ?? s.disabled === !0, p = mn(() => {
    i(u);
  }, [i, u]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: p,
      disabled: m,
      className: "tlReactButton" + (o ? " tlReactButtonActive" : "")
    },
    r
  );
}, fn = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.count ?? 0, c = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: hn } = e, bn = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.tabs ?? [], c = t.activeTabId, s = hn((i) => {
    i !== c && n("selectTab", { tabId: i });
  }, [n, c]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, a.map((i) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: i.id,
      role: "tab",
      "aria-selected": i.id === c,
      className: "tlReactTabBar__tab" + (i.id === c ? " tlReactTabBar__tab--active" : ""),
      onClick: () => s(i.id)
    },
    i.icon && /* @__PURE__ */ e.createElement(we, { encoded: i.icon, className: "tlReactTabBar__tabIcon" }),
    i.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(K, { control: t.activeContent })));
}, _n = ({ controlId: l }) => {
  const t = G(), n = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((c, s) => /* @__PURE__ */ e.createElement("div", { key: s, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(K, { control: c })))));
}, gn = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, vn = ({ controlId: l }) => {
  const t = G(), n = Fe(), [a, c] = e.useState("idle"), [s, i] = e.useState(null), u = e.useRef(null), r = e.useRef([]), o = e.useRef(null), m = t.status ?? "idle", p = t.error, f = m === "received" ? "idle" : a !== "idle" ? a : m, _ = e.useCallback(async () => {
    if (a === "recording") {
      const v = u.current;
      v && v.state !== "inactive" && v.stop();
      return;
    }
    if (a !== "uploading") {
      if (i(null), !window.isSecureContext || !navigator.mediaDevices) {
        i("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const v = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        o.current = v, r.current = [];
        const x = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", L = new MediaRecorder(v, x ? { mimeType: x } : void 0);
        u.current = L, L.ondataavailable = (C) => {
          C.data.size > 0 && r.current.push(C.data);
        }, L.onstop = async () => {
          v.getTracks().forEach((h) => h.stop()), o.current = null;
          const C = new Blob(r.current, { type: L.mimeType || "audio/webm" });
          if (r.current = [], C.size === 0) {
            c("idle");
            return;
          }
          c("uploading");
          const k = new FormData();
          k.append("audio", C, "recording.webm"), await n(k), c("idle");
        }, L.start(), c("recording");
      } catch (v) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", v), i("js.audioRecorder.error.denied"), c("idle");
      }
    }
  }, [a, n]), b = ie(gn), w = f === "recording" ? b["js.audioRecorder.stop"] : f === "uploading" ? b["js.uploading"] : b["js.audioRecorder.record"], E = f === "uploading", g = ["tlAudioRecorder__button"];
  return f === "recording" && g.push("tlAudioRecorder__button--recording"), f === "uploading" && g.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: g.join(" "),
      onClick: _,
      disabled: E,
      title: w,
      "aria-label": w
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${f === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, b[s]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, En = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Cn = ({ controlId: l }) => {
  const t = G(), n = Oe(), a = !!t.hasAudio, c = t.dataRevision ?? 0, [s, i] = e.useState(a ? "idle" : "disabled"), u = e.useRef(null), r = e.useRef(null), o = e.useRef(c);
  e.useEffect(() => {
    a ? s === "disabled" && i("idle") : (u.current && (u.current.pause(), u.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null), i("disabled"));
  }, [a]), e.useEffect(() => {
    c !== o.current && (o.current = c, u.current && (u.current.pause(), u.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null), (s === "playing" || s === "paused" || s === "loading") && i("idle"));
  }, [c]), e.useEffect(() => () => {
    u.current && (u.current.pause(), u.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null);
  }, []);
  const m = e.useCallback(async () => {
    if (s === "disabled" || s === "loading")
      return;
    if (s === "playing") {
      u.current && u.current.pause(), i("paused");
      return;
    }
    if (s === "paused" && u.current) {
      u.current.play(), i("playing");
      return;
    }
    if (!r.current) {
      i("loading");
      try {
        const E = await fetch(n);
        if (!E.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", E.status), i("idle");
          return;
        }
        const g = await E.blob();
        r.current = URL.createObjectURL(g);
      } catch (E) {
        console.error("[TLAudioPlayer] Fetch error:", E), i("idle");
        return;
      }
    }
    const w = new Audio(r.current);
    u.current = w, w.onended = () => {
      i("idle");
    }, w.play(), i("playing");
  }, [s, n]), p = ie(En), f = s === "loading" ? p["js.loading"] : s === "playing" ? p["js.audioPlayer.pause"] : s === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], _ = s === "disabled" || s === "loading", b = ["tlAudioPlayer__button"];
  return s === "playing" && b.push("tlAudioPlayer__button--playing"), s === "loading" && b.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: b.join(" "),
      onClick: m,
      disabled: _,
      title: f,
      "aria-label": f
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${s === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, wn = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, yn = ({ controlId: l }) => {
  const t = G(), n = Fe(), [a, c] = e.useState("idle"), [s, i] = e.useState(!1), u = e.useRef(null), r = t.status ?? "idle", o = t.error, m = t.accept ?? "", p = r === "received" ? "idle" : a !== "idle" ? a : r, f = e.useCallback(async (C) => {
    c("uploading");
    const k = new FormData();
    k.append("file", C, C.name), await n(k), c("idle");
  }, [n]), _ = e.useCallback((C) => {
    var h;
    const k = (h = C.target.files) == null ? void 0 : h[0];
    k && f(k);
  }, [f]), b = e.useCallback(() => {
    var C;
    a !== "uploading" && ((C = u.current) == null || C.click());
  }, [a]), w = e.useCallback((C) => {
    C.preventDefault(), C.stopPropagation(), i(!0);
  }, []), E = e.useCallback((C) => {
    C.preventDefault(), C.stopPropagation(), i(!1);
  }, []), g = e.useCallback((C) => {
    var h;
    if (C.preventDefault(), C.stopPropagation(), i(!1), a === "uploading") return;
    const k = (h = C.dataTransfer.files) == null ? void 0 : h[0];
    k && f(k);
  }, [a, f]), v = p === "uploading", x = ie(wn), L = p === "uploading" ? x["js.uploading"] : x["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${s ? " tlFileUpload--dragover" : ""}`,
      onDragOver: w,
      onDragLeave: E,
      onDrop: g
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: u,
        type: "file",
        accept: m || void 0,
        onChange: _,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (p === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: b,
        disabled: v,
        title: L,
        "aria-label": L
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    o && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, o)
  );
}, kn = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, Sn = ({ controlId: l, state: t }) => {
  const a = G() ?? t ?? {}, c = Fe(), s = Oe(), i = ie(kn), u = a.editable !== !1, r = !!a.hasData, o = a.fileName ?? "download", m = a.dataRevision ?? 0, p = a.accept ?? "", f = a.status ?? "idle", _ = a.error ?? null, [b, w] = e.useState("idle"), [E, g] = e.useState(!1), [v, x] = e.useState(!1), L = e.useRef(null), C = e.useCallback(async () => {
    if (!(!r || v)) {
      x(!0);
      try {
        const O = s + (s.includes("?") ? "&" : "?") + "rev=" + m, P = await fetch(O);
        if (!P.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", P.status);
          return;
        }
        const j = await P.blob(), X = URL.createObjectURL(j), d = document.createElement("a");
        d.href = X, d.download = o, d.style.display = "none", document.body.appendChild(d), d.click(), document.body.removeChild(d), URL.revokeObjectURL(X);
      } catch (O) {
        console.error("[TLBinaryField] Fetch error:", O);
      } finally {
        x(!1);
      }
    }
  }, [r, v, s, m, o]), k = e.useCallback(async (O) => {
    w("uploading");
    const P = new FormData();
    P.append("file", O, O.name), await c(P), w("idle");
  }, [c]), h = (f === "received" ? "idle" : b !== "idle" ? b : f) === "uploading", I = e.useCallback((O) => {
    var j;
    const P = (j = O.target.files) == null ? void 0 : j[0];
    P && k(P);
  }, [k]), T = e.useCallback(() => {
    var O;
    h || (O = L.current) == null || O.click();
  }, [h]), S = e.useCallback((O) => {
    O.preventDefault(), O.stopPropagation(), g(!0);
  }, []), H = e.useCallback((O) => {
    O.preventDefault(), O.stopPropagation(), g(!1);
  }, []), B = e.useCallback((O) => {
    var j;
    if (O.preventDefault(), O.stopPropagation(), g(!1), h) return;
    const P = (j = O.dataTransfer.files) == null ? void 0 : j[0];
    P && k(P);
  }, [h, k]), R = v ? i["js.downloading"] : i["js.download.file"].replace("{0}", o), F = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (v ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: C,
      disabled: v,
      title: R,
      "aria-label": R
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o));
  if (!u)
    return r ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, F) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, i["js.download.noFile"]));
  const Q = h, W = h ? i["js.uploading"] : i["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlBinaryField tlFileUpload${E ? " tlFileUpload--dragover" : ""}`,
      onDragOver: S,
      onDragLeave: H,
      onDrop: B
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: L,
        type: "file",
        accept: p || void 0,
        onChange: I,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (Q ? " tlFileUpload__button--uploading" : ""),
        onClick: T,
        disabled: Q,
        title: W,
        "aria-label": W
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    r && F,
    _ && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, _)
  );
}, Nn = {
  "js.fileChips.add": "Add file",
  "js.fileChips.remove": "Remove {0}",
  "js.uploading": "Uploading…",
  "js.download.file": "Download {0}"
};
function Tn(l) {
  return l < 1024 ? l + " B" : l < 1024 * 1024 ? (l / 1024).toFixed(l < 10 * 1024 ? 1 : 0) + " KB" : (l / (1024 * 1024)).toFixed(1) + " MB";
}
const Rn = ({ controlId: l }) => {
  const t = G(), n = ae(), a = Fe(), c = Oe(), s = ie(Nn), i = t.chips ?? [], u = t.editable === !0, [r, o] = e.useState(!1), [m, p] = e.useState(!1), f = e.useRef(null), _ = e.useCallback(async (C) => {
    const k = Array.from(C);
    if (k.length !== 0) {
      o(!0);
      try {
        const h = new FormData();
        for (const I of k)
          h.append("file", I, I.name);
        await a(h);
      } finally {
        o(!1);
      }
    }
  }, [a]), b = e.useCallback(async (C) => {
    if (C.hasData)
      try {
        const k = c + "&key=" + encodeURIComponent(C.key), h = await fetch(k);
        if (!h.ok) {
          console.error("[TLFileChips] Failed to fetch data:", h.status);
          return;
        }
        const I = await h.blob(), T = URL.createObjectURL(I), S = document.createElement("a");
        S.href = T, S.download = C.name, S.style.display = "none", document.body.appendChild(S), S.click(), document.body.removeChild(S), URL.revokeObjectURL(T);
      } catch (k) {
        console.error("[TLFileChips] Fetch error:", k);
      }
  }, [c]), w = e.useCallback((C) => {
    C.target.files && _(C.target.files), C.target.value = "";
  }, [_]), E = e.useCallback(() => {
    var C;
    r || (C = f.current) == null || C.click();
  }, [r]), g = e.useCallback((C) => {
    u && (C.preventDefault(), C.stopPropagation(), p(!0));
  }, [u]), v = e.useCallback((C) => {
    u && (C.preventDefault(), C.stopPropagation(), p(!1));
  }, [u]), x = e.useCallback((C) => {
    u && (C.preventDefault(), C.stopPropagation(), p(!1), !r && C.dataTransfer.files && _(C.dataTransfer.files));
  }, [u, r, _]), L = [
    "tlFileChips",
    u ? "tlFileChips--editable" : "",
    m ? "tlFileChips--dragover" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: L,
      onDragOver: g,
      onDragLeave: v,
      onDrop: x
    },
    i.map((C) => {
      const k = s["js.download.file"].replace("{0}", C.name), h = s["js.fileChips.remove"].replace("{0}", C.name);
      return /* @__PURE__ */ e.createElement("span", { key: C.key, className: "tlFileChip" }, /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__main",
          onClick: () => b(C),
          disabled: !C.hasData,
          title: C.hasData ? k : C.name
        },
        /* @__PURE__ */ e.createElement("svg", { className: "tlFileChip__icon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement(
          "path",
          {
            d: "M9.5 1H4a1 1 0 0 0-1 1v12a1 1 0 0 0 1 1h8a1 1 0 0 0 1-1V4.5L9.5 1z",
            fill: "none",
            stroke: "currentColor",
            strokeWidth: "1.2",
            strokeLinejoin: "round"
          }
        ), /* @__PURE__ */ e.createElement(
          "path",
          {
            d: "M9.5 1v3.5H13",
            fill: "none",
            stroke: "currentColor",
            strokeWidth: "1.2",
            strokeLinejoin: "round"
          }
        )),
        /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__name" }, C.name),
        C.size != null && /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__size" }, Tn(C.size))
      ), u && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__remove",
          onClick: () => n("removeChip", { key: C.key }),
          title: h,
          "aria-label": h
        },
        /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 16 16", width: "12", height: "12", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement(
          "path",
          {
            d: "M4 4l8 8M12 4l-8 8",
            stroke: "currentColor",
            strokeWidth: "1.5",
            strokeLinecap: "round"
          }
        ))
      ));
    }),
    u && /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: f,
        type: "file",
        multiple: !0,
        onChange: w,
        style: { display: "none" }
      }
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileChips__add" + (r ? " tlFileChips__add--uploading" : ""),
        onClick: E,
        disabled: r,
        title: r ? s["js.uploading"] : s["js.fileChips.add"]
      },
      /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement(
        "path",
        {
          d: "M13.5 7.5l-5.6 5.6a3.3 3.3 0 0 1-4.7-4.7l6-6a2.2 2.2 0 0 1 3.1 3.1l-5.8 5.8a1.1 1.1 0 0 1-1.6-1.6l5.2-5.2",
          fill: "none",
          stroke: "currentColor",
          strokeWidth: "1.2",
          strokeLinecap: "round",
          strokeLinejoin: "round"
        }
      )),
      /* @__PURE__ */ e.createElement("span", null, r ? s["js.uploading"] : s["js.fileChips.add"])
    ))
  );
}, Dn = 3e4;
function Ln(l, t) {
  const n = Math.round((l - Date.now()) / 1e3), a = Math.abs(n), c = new Intl.RelativeTimeFormat(t, { numeric: "auto" });
  return a < 60 ? c.format(Math.trunc(n / 1), "second") : a < 3600 ? c.format(Math.trunc(n / 60), "minute") : a < 86400 ? c.format(Math.trunc(n / 3600), "hour") : a < 7 * 86400 ? c.format(Math.trunc(n / 86400), "day") : new Date(l).toLocaleDateString(t);
}
const xn = ({ controlId: l }) => {
  const t = G(), n = t.timestamp, a = t.label ?? void 0, c = t.locale || navigator.language, [, s] = e.useState(0);
  return e.useEffect(() => {
    const i = setInterval(() => s((u) => u + 1), Dn);
    return () => clearInterval(i);
  }, []), n == null ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime tlRelativeTime--empty" }) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime", title: a }, Ln(n, c));
}, In = ({ controlId: l }) => {
  const t = G(), n = t.anchor ?? void 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAnchor", "data-tl-anchor": n }, t.child && /* @__PURE__ */ e.createElement(K, { control: t.child }));
}, Pn = ({ controlId: l }) => {
  const t = G(), n = t.target, a = t.label ?? "";
  if (n == null)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlScrollLink tlScrollLink--empty" });
  const c = (s) => {
    s.preventDefault(), Yt(n);
  };
  return /* @__PURE__ */ e.createElement("a", { id: l, className: "tlScrollLink", href: "#", onClick: c }, a);
};
function Mn(l) {
  const t = l.trim().split(/\s+/).filter(Boolean);
  return t.length === 0 ? "?" : t.length === 1 ? t[0].slice(0, 2).toUpperCase() : (t[0][0] + t[t.length - 1][0]).toUpperCase();
}
function jn(l) {
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return Math.abs(t) % 360;
}
const An = ({ controlId: l }) => {
  const n = G().name;
  return n ? /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlAvatar",
      style: { backgroundColor: `hsl(${jn(n)}, 45%, 45%)` },
      title: n,
      "aria-label": n
    },
    Mn(n)
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlAvatar tlAvatar--empty" });
}, Bn = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Fn = ({ controlId: l }) => {
  const t = G(), n = Oe(), a = ae(), c = !!t.hasData, s = t.dataRevision ?? 0, i = t.fileName ?? "download", u = !!t.clearable, [r, o] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!c || r)) {
      o(!0);
      try {
        const b = n + (n.includes("?") ? "&" : "?") + "rev=" + s, w = await fetch(b);
        if (!w.ok) {
          console.error("[TLDownload] Failed to fetch data:", w.status);
          return;
        }
        const E = await w.blob(), g = URL.createObjectURL(E), v = document.createElement("a");
        v.href = g, v.download = i, v.style.display = "none", document.body.appendChild(v), v.click(), document.body.removeChild(v), URL.revokeObjectURL(g);
      } catch (b) {
        console.error("[TLDownload] Fetch error:", b);
      } finally {
        o(!1);
      }
    }
  }, [c, r, n, s, i]), p = e.useCallback(async () => {
    c && await a("clear");
  }, [c, a]), f = ie(Bn);
  if (!c)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, f["js.download.noFile"]));
  const _ = r ? f["js.downloading"] : f["js.download.file"].replace("{0}", i);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (r ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: m,
      disabled: r,
      title: _,
      "aria-label": _
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: i }, i), u && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: p,
      title: f["js.download.clear"],
      "aria-label": f["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, On = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, $n = ({ controlId: l }) => {
  const t = G(), n = Fe(), [a, c] = e.useState("idle"), [s, i] = e.useState(null), [u, r] = e.useState(!1), o = e.useRef(null), m = e.useRef(null), p = e.useRef(null), f = e.useRef(null), _ = e.useRef(null), b = t.error, w = e.useMemo(
    () => {
      var S;
      return !!(window.isSecureContext && ((S = navigator.mediaDevices) != null && S.getUserMedia));
    },
    []
  ), E = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((S) => S.stop()), m.current = null), o.current && (o.current.srcObject = null);
  }, []), g = e.useCallback(() => {
    E(), c("idle");
  }, [E]), v = e.useCallback(async () => {
    var S;
    if (a !== "uploading") {
      if (i(null), !w) {
        (S = f.current) == null || S.click();
        return;
      }
      try {
        const H = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        m.current = H, c("overlayOpen");
      } catch (H) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", H), i("js.photoCapture.error.denied"), c("idle");
      }
    }
  }, [a, w]), x = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const S = o.current, H = p.current;
    if (!S || !H)
      return;
    H.width = S.videoWidth, H.height = S.videoHeight;
    const B = H.getContext("2d");
    B && (B.drawImage(S, 0, 0), E(), c("uploading"), H.toBlob(async (R) => {
      if (!R) {
        c("idle");
        return;
      }
      const F = new FormData();
      F.append("photo", R, "capture.jpg"), await n(F), c("idle");
    }, "image/jpeg", 0.85));
  }, [a, n, E]), L = e.useCallback(async (S) => {
    var R;
    const H = (R = S.target.files) == null ? void 0 : R[0];
    if (!H) return;
    c("uploading");
    const B = new FormData();
    B.append("photo", H, H.name), await n(B), c("idle"), f.current && (f.current.value = "");
  }, [n]);
  e.useEffect(() => {
    a === "overlayOpen" && o.current && m.current && (o.current.srcObject = m.current);
  }, [a]), e.useEffect(() => {
    var H;
    if (a !== "overlayOpen") return;
    (H = _.current) == null || H.focus();
    const S = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = S;
    };
  }, [a]), Le(a === "overlayOpen", { ESCAPE: g }), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((S) => S.stop()), m.current = null);
  }, []);
  const C = ie(On), k = a === "uploading" ? C["js.uploading"] : C["js.photoCapture.open"], h = ["tlPhotoCapture__cameraBtn"];
  a === "uploading" && h.push("tlPhotoCapture__cameraBtn--uploading");
  const I = ["tlPhotoCapture__overlayVideo"];
  u && I.push("tlPhotoCapture__overlayVideo--mirrored");
  const T = ["tlPhotoCapture__mirrorBtn"];
  return u && T.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: h.join(" "),
      onClick: v,
      disabled: a === "uploading",
      title: k,
      "aria-label": k
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !w && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: f,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: L
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: p, style: { display: "none" } }), a === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: _,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: g }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: o,
        className: I.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: T.join(" "),
        onClick: () => r((S) => !S),
        title: C["js.photoCapture.mirror"],
        "aria-label": C["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: x,
        title: C["js.photoCapture.capture"],
        "aria-label": C["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: g,
        title: C["js.photoCapture.close"],
        "aria-label": C["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, C[s]), b && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, b));
}, Un = {
  "js.photoViewer.alt": "Captured photo"
}, Hn = ({ controlId: l }) => {
  const t = G(), n = Oe(), a = !!t.hasPhoto, c = t.dataRevision ?? 0, [s, i] = e.useState(null), u = e.useRef(c);
  e.useEffect(() => {
    if (!a) {
      s && (URL.revokeObjectURL(s), i(null));
      return;
    }
    if (c === u.current && s)
      return;
    u.current = c, s && (URL.revokeObjectURL(s), i(null));
    let o = !1;
    return (async () => {
      try {
        const m = await fetch(n);
        if (!m.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", m.status);
          return;
        }
        const p = await m.blob();
        o || i(URL.createObjectURL(p));
      } catch (m) {
        console.error("[TLPhotoViewer] Fetch error:", m);
      }
    })(), () => {
      o = !0;
    };
  }, [a, c, n]), e.useEffect(() => () => {
    s && URL.revokeObjectURL(s);
  }, []);
  const r = ie(Un);
  return !a || !s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: s,
      alt: r["js.photoViewer.alt"]
    }
  ));
}, Wn = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, zn = ({ controlId: l }) => {
  const t = G(), n = Oe(), a = !!t.hasPdf, c = t.dataRevision ?? 0, s = ie(Wn), u = n.indexOf("react-api/"), r = u >= 0 ? n.slice(0, u) : n, o = n + "&rev=" + c, m = r + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(o);
  return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: m,
      title: s["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, s["js.pdfViewer.noDocument"]));
}, { useCallback: Et, useRef: Qe } = e, Vn = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.orientation, c = t.resizable === !0, s = t.children ?? [], i = a === "horizontal", u = s.length > 0 && s.every((E) => E.collapsed), r = !u && s.some((E) => E.collapsed), o = u ? !i : i, m = Qe(null), p = Qe(null), f = Qe(null), _ = Et((E, g) => {
    const v = {
      overflow: E.scrolling || "auto"
    };
    return E.collapsed ? u && !o ? v.flex = "1 0 0%" : v.flex = "0 0 auto" : g !== void 0 ? v.flex = `0 0 ${g}px` : v.flex = `${E.size} 1 0%`, E.minSize > 0 && !E.collapsed && (v.minWidth = i ? E.minSize : void 0, v.minHeight = i ? void 0 : E.minSize), v;
  }, [i, u, r, o]), b = Et((E, g) => {
    E.preventDefault();
    const v = m.current;
    if (!v) return;
    const x = s[g], L = s[g + 1], C = v.querySelectorAll(":scope > .tlSplitPanel__child"), k = [];
    C.forEach((T) => {
      k.push(i ? T.offsetWidth : T.offsetHeight);
    }), f.current = k, p.current = {
      splitterIndex: g,
      startPos: i ? E.clientX : E.clientY,
      startSizeBefore: k[g],
      startSizeAfter: k[g + 1],
      childBefore: x,
      childAfter: L
    };
    const h = (T) => {
      const S = p.current;
      if (!S || !f.current) return;
      const B = (i ? T.clientX : T.clientY) - S.startPos, R = S.childBefore.minSize || 0, F = S.childAfter.minSize || 0;
      let Q = S.startSizeBefore + B, W = S.startSizeAfter - B;
      Q < R && (W += Q - R, Q = R), W < F && (Q += W - F, W = F), f.current[S.splitterIndex] = Q, f.current[S.splitterIndex + 1] = W;
      const O = v.querySelectorAll(":scope > .tlSplitPanel__child"), P = O[S.splitterIndex], j = O[S.splitterIndex + 1];
      P && (P.style.flex = `0 0 ${Q}px`), j && (j.style.flex = `0 0 ${W}px`);
    }, I = () => {
      if (document.removeEventListener("mousemove", h), document.removeEventListener("mouseup", I), document.body.style.cursor = "", document.body.style.userSelect = "", f.current) {
        const T = {};
        s.forEach((S, H) => {
          const B = S.control;
          B != null && B.controlId && f.current && (T[B.controlId] = f.current[H]);
        }), n("updateSizes", { sizes: T });
      }
      f.current = null, p.current = null;
    };
    document.addEventListener("mousemove", h), document.addEventListener("mouseup", I), document.body.style.cursor = i ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [s, i, n]), w = [];
  return s.forEach((E, g) => {
    if (w.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${g}`,
          className: `tlSplitPanel__child${E.collapsed && o ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: _(E)
        },
        /* @__PURE__ */ e.createElement(K, { control: E.control })
      )
    ), c && g < s.length - 1) {
      const v = s[g + 1];
      !E.collapsed && !v.collapsed && w.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${g}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${a}`,
            onMouseDown: (L) => b(L, g)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: m,
      id: l,
      className: `tlSplitPanel tlSplitPanel--${a}${u ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: o ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    w
  );
}, qe = ({ image: l, className: t }) => {
  if (!l) return null;
  const n = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `${t ? t + " " : ""}${n}`, "aria-hidden": "true" });
}, { useCallback: Je } = e, Kn = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Yn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Gn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), Xn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), qn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), Zn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), Qn = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ie(Kn), c = t.title, s = t.expansionState ?? "NORMALIZED", i = t.showMinimize === !0, u = t.showMaximize === !0, r = t.showPopOut === !0, o = t.fullLine === !0, m = t.fill === !0, p = t.hoverActions === !0, f = t.appearance === "card", _ = t.errorMessage, b = s === "MINIMIZED", w = s === "MAXIMIZED", E = s === "HIDDEN", g = Je(() => {
    n("toggleMinimize");
  }, [n]), v = Je(() => {
    n("toggleMaximize");
  }, [n]), x = Je(() => {
    n("popOut");
  }, [n]);
  if (E)
    return null;
  const L = w ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" }, C = i && !w || u && !b || r, k = !!c && c.trim() !== "" || !!t.titleContent || !!t.toolbar || C;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${s.toLowerCase()}${o ? " tlPanel--fullLine" : ""}${m ? " tlPanel--fill" : ""}${p ? " tlPanel--hoverActions" : ""}${f ? " tlPanel--card" : ""}`,
      style: L
    },
    k && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, !!c && c.trim() !== "" && /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, c), t.titleContent && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__titleContent" }, /* @__PURE__ */ e.createElement(K, { control: t.titleContent })), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(K, { control: t.toolbar }), i && !w && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: g,
        title: b ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      b ? /* @__PURE__ */ e.createElement(Gn, null) : /* @__PURE__ */ e.createElement(Yn, null)
    ), u && !b && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
        title: w ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      w ? /* @__PURE__ */ e.createElement(qn, null) : /* @__PURE__ */ e.createElement(Xn, null)
    ), r && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: x,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(Zn, null)
    ))),
    !b && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(K, { control: t.child })),
    !b && _ && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error tlPanel__error", role: "alert" }, /* @__PURE__ */ e.createElement(qe, { image: t.errorIcon, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, _)),
    !b && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(K, { control: t.buttonBar }))
  );
}, Jn = ({ controlId: l }) => {
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
}, el = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(K, { control: t.activeChild }));
}, { useCallback: be, useState: Xe, useEffect: st, useRef: Ze } = e, tl = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function ct(l, t, n, a) {
  const c = [];
  for (const s of l)
    if (s.type === "nav") {
      if (s.hidden) continue;
      c.push({ id: s.id, type: "nav", groupId: a });
    } else s.type === "command" ? c.push({ id: s.id, type: "command", groupId: a }) : s.type === "group" && (c.push({ id: s.id, type: "group" }), (n.get(s.id) ?? s.expanded) && !t && c.push(...ct(s.children, t, n, s.id)));
  return c;
}
const Be = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(we, { encoded: l, className: "tlSidebar__icon" }) : null, nl = ({ item: l, active: t, collapsed: n, onSelect: a, tabIndex: c, itemRef: s, onFocus: i }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => a(l.id),
    title: n ? l.label : void 0,
    tabIndex: c,
    ref: s,
    onFocus: () => i(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Be, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Be, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), ll = ({ item: l, collapsed: t, onExecute: n, tabIndex: a, itemRef: c, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: a,
    ref: c,
    onFocus: () => s(l.id)
  },
  /* @__PURE__ */ e.createElement(Be, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), al = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Be, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), rl = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), ol = ({ item: l, activeItemId: t, anchorRect: n, onSelect: a, onExecute: c, onClose: s }) => {
  const i = Ze(null);
  st(() => {
    const o = (m) => {
      i.current && !i.current.contains(m.target) && setTimeout(() => s(), 0);
    };
    return document.addEventListener("mousedown", o), () => document.removeEventListener("mousedown", o);
  }, [s]), Le(!0, { ESCAPE: s });
  const u = be((o) => {
    o.type === "nav" ? (a(o.id), s()) : o.type === "command" && (c(o.id), s());
  }, [a, c, s]), r = {};
  return n && (r.left = n.right, r.top = n.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: i, role: "menu", style: r }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((o) => {
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
        /* @__PURE__ */ e.createElement(Be, { icon: o.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, o.label),
        o.type === "nav" && o.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, o.badge)
      );
    }
    return o.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: o.id, className: "tlSidebar__flyoutSectionHeader" }, o.label) : o.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: o.id, className: "tlSidebar__separator" }) : null;
  }));
}, sl = ({
  item: l,
  expanded: t,
  activeItemId: n,
  collapsed: a,
  onSelect: c,
  onExecute: s,
  onToggleGroup: i,
  tabIndex: u,
  itemRef: r,
  onFocus: o,
  focusedId: m,
  setItemRef: p,
  onItemFocus: f,
  flyoutGroupId: _,
  onOpenFlyout: b,
  onCloseFlyout: w
}) => {
  const E = Ze(null), [g, v] = Xe(null), x = be(() => {
    a ? _ === l.id ? w() : (E.current && v(E.current.getBoundingClientRect()), b(l.id)) : i(l.id);
  }, [a, _, l.id, i, b, w]), L = be((k) => {
    E.current = k, r(k);
  }, [r]), C = a && _ === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (C ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: x,
      title: a ? l.label : void 0,
      "aria-expanded": a ? C : t,
      tabIndex: u,
      ref: L,
      onFocus: () => o(l.id)
    },
    /* @__PURE__ */ e.createElement(Be, { icon: l.icon }),
    !a && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
    !a && /* @__PURE__ */ e.createElement(
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
  ), C && /* @__PURE__ */ e.createElement(
    ol,
    {
      item: l,
      activeItemId: n,
      anchorRect: g,
      onSelect: c,
      onExecute: s,
      onClose: w
    }
  ), t && !a && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((k) => /* @__PURE__ */ e.createElement(
    Bt,
    {
      key: k.id,
      item: k,
      activeItemId: n,
      collapsed: a,
      onSelect: c,
      onExecute: s,
      onToggleGroup: i,
      focusedId: m,
      setItemRef: p,
      onItemFocus: f,
      groupStates: null,
      flyoutGroupId: _,
      onOpenFlyout: b,
      onCloseFlyout: w
    }
  ))));
}, Bt = ({
  item: l,
  activeItemId: t,
  collapsed: n,
  onSelect: a,
  onExecute: c,
  onToggleGroup: s,
  focusedId: i,
  setItemRef: u,
  onItemFocus: r,
  groupStates: o,
  flyoutGroupId: m,
  onOpenFlyout: p,
  onCloseFlyout: f
}) => {
  switch (l.type) {
    case "nav":
      return l.hidden ? null : /* @__PURE__ */ e.createElement(
        nl,
        {
          item: l,
          active: l.id === t,
          collapsed: n,
          onSelect: a,
          tabIndex: i === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: r
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        ll,
        {
          item: l,
          collapsed: n,
          onExecute: c,
          tabIndex: i === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: r
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(al, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(rl, null);
    case "group": {
      const _ = o ? o.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        sl,
        {
          item: l,
          expanded: _,
          activeItemId: t,
          collapsed: n,
          onSelect: a,
          onExecute: c,
          onToggleGroup: s,
          tabIndex: i === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: r,
          focusedId: i,
          setItemRef: u,
          onItemFocus: r,
          flyoutGroupId: m,
          onOpenFlyout: p,
          onCloseFlyout: f
        }
      );
    }
    default:
      return null;
  }
}, cl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ie(tl), c = t.items ?? [], s = t.activeItemId, i = t.collapsed, u = t.drawerOpen, r = u ? !1 : i, [o, m] = Xe(() => {
    const R = /* @__PURE__ */ new Map(), F = (Q) => {
      for (const W of Q)
        W.type === "group" && (R.set(W.id, W.expanded), F(W.children));
    };
    return F(c), R;
  }), p = be((R) => {
    m((F) => {
      const Q = new Map(F), W = Q.get(R) ?? !1;
      return Q.set(R, !W), n("toggleGroup", { itemId: R, expanded: !W }), Q;
    });
  }, [n]), f = be((R) => {
    R !== s && n("selectItem", { itemId: R });
  }, [n, s]), _ = be((R) => {
    n("executeCommand", { itemId: R });
  }, [n]), b = be(() => {
    n("toggleCollapse", {});
  }, [n]), w = be(() => {
    n("toggleDrawer", {});
  }, [n]), [E, g] = Xe(null), v = be((R) => {
    g(R);
  }, []), x = be(() => {
    g(null);
  }, []);
  st(() => {
    r || g(null);
  }, [r]);
  const [L, C] = Xe(() => {
    const R = ct(c, r, o);
    return R.length > 0 ? R[0].id : "";
  }), k = Ze(/* @__PURE__ */ new Map()), h = be((R) => (F) => {
    F ? k.current.set(R, F) : k.current.delete(R);
  }, []), I = be((R) => {
    C(R);
  }, []), T = Ze(0), S = be((R) => {
    C(R), T.current++;
  }, []);
  st(() => {
    const R = k.current.get(L);
    R && document.activeElement !== R && R.focus();
  }, [L, T.current]);
  const H = be((R) => {
    if (R.key === "Escape" && E !== null) {
      R.preventDefault(), x();
      return;
    }
    const F = ct(c, r, o);
    if (F.length === 0) return;
    const Q = F.findIndex((O) => O.id === L);
    if (Q < 0) return;
    const W = F[Q];
    switch (R.key) {
      case "ArrowDown": {
        R.preventDefault();
        const O = (Q + 1) % F.length;
        S(F[O].id);
        break;
      }
      case "ArrowUp": {
        R.preventDefault();
        const O = (Q - 1 + F.length) % F.length;
        S(F[O].id);
        break;
      }
      case "Home": {
        R.preventDefault(), S(F[0].id);
        break;
      }
      case "End": {
        R.preventDefault(), S(F[F.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        R.preventDefault(), W.type === "nav" ? f(W.id) : W.type === "command" ? _(W.id) : W.type === "group" && (r ? E === W.id ? x() : v(W.id) : p(W.id));
        break;
      }
      case "ArrowRight": {
        W.type === "group" && !r && ((o.get(W.id) ?? !1) || (R.preventDefault(), p(W.id)));
        break;
      }
      case "ArrowLeft": {
        W.type === "group" && !r && (o.get(W.id) ?? !1) && (R.preventDefault(), p(W.id));
        break;
      }
    }
  }, [
    c,
    r,
    o,
    L,
    E,
    S,
    f,
    _,
    p,
    v,
    x
  ]), B = "tlSidebar" + (r ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: B }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(K, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: w, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": a["js.sidebar.ariaLabel"] }, r ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: H }, c.map((R) => /* @__PURE__ */ e.createElement(
    Bt,
    {
      key: R.id,
      item: R,
      activeItemId: s,
      collapsed: r,
      onSelect: f,
      onExecute: _,
      onToggleGroup: p,
      focusedId: L,
      setItemRef: h,
      onItemFocus: I,
      groupStates: o,
      flyoutGroupId: E,
      onOpenFlyout: v,
      onCloseFlyout: x
    }
  ))), r ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: b,
      title: r ? a["js.sidebar.expand"] : a["js.sidebar.collapse"]
    },
    /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement(
      "path",
      {
        d: r ? "M6 4l4 4-4 4" : "M10 4l-4 4 4 4",
        fill: "none",
        stroke: "currentColor",
        strokeWidth: "2",
        strokeLinecap: "round",
        strokeLinejoin: "round"
      }
    ))
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(K, { control: t.activeContent })));
}, il = ({ controlId: l }) => {
  const t = G(), n = t.direction ?? "column", a = t.gap ?? "default", c = t.align ?? "stretch", s = t.wrap === !0, i = t.growFirst === !0, u = t.children ?? [], r = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${a}`,
    `tlStack--align-${c}`,
    s ? "tlStack--wrap" : "",
    i ? "tlStack--grow-first" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: r }, u.map((o, m) => /* @__PURE__ */ e.createElement(K, { key: m, control: o })));
}, ul = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlInset" }, t.child && /* @__PURE__ */ e.createElement(K, { control: t.child }));
}, dl = ({ controlId: l }) => {
  const t = G(), n = t.columns, a = t.minColumnWidth, c = t.gap ?? "default", s = t.children ?? [], i = {};
  return a ? i.gridTemplateColumns = `repeat(auto-fit, minmax(min(${a}, 100%), 1fr))` : n && (i.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${c}`, style: i }, s.map((u, r) => /* @__PURE__ */ e.createElement(K, { key: r, control: u })));
}, ml = ({ controlId: l }) => {
  const t = G(), n = t.title, a = t.variant ?? "outlined", c = t.padding ?? "default", s = t.headerActions ?? [], i = t.child, u = n != null || s.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${a}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, s.map((r, o) => /* @__PURE__ */ e.createElement(K, { key: o, control: r })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${c}` }, /* @__PURE__ */ e.createElement(K, { control: i })));
}, pl = ({ controlId: l }) => {
  const t = G(), n = t.title ?? "", a = t.leading, c = t.children ?? [], s = t.actions ?? [], i = t.variant ?? "flat", r = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    i === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: r }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(K, { control: a })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, c.map((o, m) => /* @__PURE__ */ e.createElement(K, { key: m, control: o }))), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((o, m) => /* @__PURE__ */ e.createElement(K, { key: m, control: o }))));
}, { useCallback: fl } = e, hl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.items ?? [], c = fl((s) => {
    n("navigate", { itemId: s });
  }, [n]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, a.map((s, i) => {
    const u = i === a.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: s.id, className: "tlBreadcrumb__entry" }, i > 0 && /* @__PURE__ */ e.createElement(
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
    ), u ? /* @__PURE__ */ e.createElement("span", { className: "tlBreadcrumb__current", "aria-current": "page" }, s.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlBreadcrumb__item",
        onClick: () => c(s.id)
      },
      s.label
    ));
  })));
}, { useCallback: bl } = e, _l = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.items ?? [], c = t.activeItemId, s = bl((i) => {
    i !== c && n("selectItem", { itemId: i });
  }, [n, c]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, a.map((i) => {
    const u = i.id === c;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: i.id,
        type: "button",
        className: "tlBottomBar__item" + (u ? " tlBottomBar__item--active" : ""),
        onClick: () => s(i.id),
        "aria-current": u ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + i.icon, "aria-hidden": "true" }), i.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, i.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, i.label)
    );
  }));
}, { useCallback: Ct, useRef: gl } = e, vl = ({ onClose: l }) => (de("ESCAPE", () => (l(), !0)), null), El = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.open === !0, c = t.closeOnBackdrop !== !1, s = t.child, i = gl(null), u = Ct(() => {
    n("close");
  }, [n]), r = Ct((o) => {
    c && o.target === o.currentTarget && u();
  }, [c, u]);
  return a ? /* @__PURE__ */ e.createElement(mt, null, /* @__PURE__ */ e.createElement(vl, { onClose: u }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: r,
      ref: i,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(K, { control: s })
  )) : null;
}, { useEffect: Cl, useRef: wl } = e, yl = ({ controlId: l }) => {
  const n = G().dialogs ?? [], a = wl(n.length);
  return Cl(() => {
    n.length < a.current && n.length > 0, a.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((c) => /* @__PURE__ */ e.createElement(K, { key: c.controlId, control: c })));
}, { useCallback: ze, useRef: Pe, useState: Ve } = e, kl = ({ onClose: l }) => (de("ESCAPE", () => (l(), !0)), null), Sl = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Nl = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Tl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ie(Sl), c = t.title ?? "", s = t.width ?? "32rem", i = t.height ?? null, u = t.minHeight ?? null, r = t.resizable === !0, o = t.child, m = t.actions ?? [], p = t.toolbar, f = t.buttonBar, [_, b] = Ve(null), [w, E] = Ve(null), [g, v] = Ve(null), x = Pe(null), [L, C] = Ve(!1), k = Pe(null), h = Pe(null), I = Pe(null), T = Pe(null), S = Pe(null), H = ze(() => {
    n("close");
  }, [n]);
  pt(!0, T, "field");
  const B = ze((O, P) => {
    P.preventDefault();
    const j = T.current;
    if (!j) return;
    const X = j.getBoundingClientRect(), d = !x.current, N = x.current ?? { x: X.left, y: X.top };
    d && (x.current = N, v(N)), S.current = {
      dir: O,
      startX: P.clientX,
      startY: P.clientY,
      startW: X.width,
      startH: X.height,
      startPos: { ...N },
      symmetric: d
    };
    const V = (q) => {
      const A = S.current;
      if (!A) return;
      const ee = q.clientX - A.startX, oe = q.clientY - A.startY;
      let le = A.startW, he = A.startH, ge = 0, ve = 0;
      A.symmetric ? (A.dir.includes("e") && (le = A.startW + 2 * ee), A.dir.includes("w") && (le = A.startW - 2 * ee), A.dir.includes("s") && (he = A.startH + 2 * oe), A.dir.includes("n") && (he = A.startH - 2 * oe)) : (A.dir.includes("e") && (le = A.startW + ee), A.dir.includes("w") && (le = A.startW - ee, ge = ee), A.dir.includes("s") && (he = A.startH + oe), A.dir.includes("n") && (he = A.startH - oe, ve = oe));
      const ye = Math.max(200, le), ke = Math.max(100, he);
      A.symmetric ? (ge = (A.startW - ye) / 2, ve = (A.startH - ke) / 2) : (A.dir.includes("w") && ye === 200 && (ge = A.startW - 200), A.dir.includes("n") && ke === 100 && (ve = A.startH - 100)), h.current = ye, I.current = ke, b(ye), E(ke);
      const xe = {
        x: A.startPos.x + ge,
        y: A.startPos.y + ve
      };
      x.current = xe, v(xe);
    }, $ = () => {
      document.removeEventListener("mousemove", V), document.removeEventListener("mouseup", $);
      const q = h.current, A = I.current;
      (q != null || A != null) && n("resize", {
        ...q != null ? { width: Math.round(q) } : {},
        ...A != null ? { height: Math.round(A) } : {}
      }), S.current = null;
    };
    document.addEventListener("mousemove", V), document.addEventListener("mouseup", $);
  }, [n]), R = ze((O) => {
    if (O.button !== 0 || O.target.closest("button")) return;
    O.preventDefault();
    const P = T.current;
    if (!P) return;
    const j = P.getBoundingClientRect(), X = x.current ?? { x: j.left, y: j.top }, d = O.clientX - X.x, N = O.clientY - X.y, V = (q) => {
      const A = window.innerWidth, ee = window.innerHeight;
      let oe = q.clientX - d, le = q.clientY - N;
      const he = P.offsetWidth, ge = P.offsetHeight;
      oe + he > A && (oe = A - he), le + ge > ee && (le = ee - ge), oe < 0 && (oe = 0), le < 0 && (le = 0);
      const ve = { x: oe, y: le };
      x.current = ve, v(ve);
    }, $ = () => {
      document.removeEventListener("mousemove", V), document.removeEventListener("mouseup", $);
    };
    document.addEventListener("mousemove", V), document.addEventListener("mouseup", $);
  }, []), F = ze(() => {
    var O, P;
    if (L) {
      const j = k.current;
      j && (v(j.x !== -1 ? { x: j.x, y: j.y } : null), b(j.w), E(j.h)), C(!1);
    } else {
      const j = T.current, X = j == null ? void 0 : j.getBoundingClientRect();
      k.current = {
        x: ((O = x.current) == null ? void 0 : O.x) ?? (X == null ? void 0 : X.left) ?? -1,
        y: ((P = x.current) == null ? void 0 : P.y) ?? (X == null ? void 0 : X.top) ?? -1,
        w: _ ?? (X == null ? void 0 : X.width) ?? null,
        h: w ?? null
      }, C(!0), v({ x: 0, y: 0 }), b(null), E(null);
    }
  }, [L, _, w]), Q = L ? { position: "absolute", top: 0, left: 0, width: "100vw", maxWidth: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: _ != null ? _ + "px" : s,
    ...w != null ? { height: w + "px" } : i != null ? { height: i } : {},
    ...u != null && w == null ? { minHeight: u } : {},
    maxHeight: g ? "100vh" : "80vh",
    ...g ? { position: "absolute", left: g.x + "px", top: g.y + "px" } : {}
  }, W = l + "-title";
  return /* @__PURE__ */ e.createElement(mt, { modal: !0 }, /* @__PURE__ */ e.createElement(kl, { onClose: H }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: Q,
      ref: T,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": W
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${L ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: L ? void 0 : R,
        onDoubleClick: r ? F : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: W }, c),
      p && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(K, { control: p })),
      r && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: F,
          title: L ? a["js.window.restore"] : a["js.window.maximize"]
        },
        L ? (
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
          onClick: H,
          title: a["js.window.close"]
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
    (m.length > 0 || f) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, f && /* @__PURE__ */ e.createElement(K, { control: f }), m.map((O, P) => /* @__PURE__ */ e.createElement(K, { key: P, control: O }))),
    r && !L && Nl.map((O) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: O,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${O}`,
        onMouseDown: (P) => B(O, P)
      }
    ))
  ));
}, { useCallback: Rl } = e, Dl = {
  "js.drawer.close": "Close"
}, Ll = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ie(Dl), c = t.open === !0, s = t.position ?? "right", i = t.size ?? "medium", u = t.title ?? null, r = t.child, o = Rl(() => {
    n("close");
  }, [n]);
  Le(c, { ESCAPE: o });
  const m = [
    "tlDrawer",
    `tlDrawer--${s}`,
    `tlDrawer--${i}`,
    c ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: l, className: m, "aria-hidden": !c }, u !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, u), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: o,
      title: a["js.drawer.close"]
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, r && /* @__PURE__ */ e.createElement(K, { control: r })));
}, { useCallback: xl } = e, Il = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.child, c = xl((s) => {
    s.preventDefault(), s.stopPropagation(), n("openContextMenu", { x: s.clientX, y: s.clientY });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tl-context-menu-region", onContextMenu: c }, a && /* @__PURE__ */ e.createElement(K, { control: a }));
}, { useCallback: Pl, useEffect: Ml, useState: jl } = e, Al = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.message ?? "", c = t.content ?? "", s = t.variant ?? "info", i = t.duration ?? 5e3, u = t.visible === !0, r = t.generation ?? 0, [o, m] = jl(!1), p = Pl(() => {
    m(!0), setTimeout(() => {
      n("dismiss", { generation: r }), m(!1);
    }, 200);
  }, [n, r]);
  return Ml(() => {
    if (!u || i === 0) return;
    const f = setTimeout(p, i);
    return () => clearTimeout(f);
  }, [u, i, p]), !u && !o ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${s}${o ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    c ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: c } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, a)
  );
}, { useCallback: et, useEffect: wt, useRef: Bl, useState: yt } = e, Fl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.open === !0, c = t.anchorId, s = t.anchorX, i = t.anchorY, u = t.items ?? [], r = Bl(null), [o, m] = yt({ top: 0, left: 0 }), [p, f] = yt(0), _ = u.filter((g) => g.type === "item" && !g.disabled);
  wt(() => {
    var h, I;
    if (!a) return;
    const g = ((h = r.current) == null ? void 0 : h.offsetHeight) ?? 200, v = ((I = r.current) == null ? void 0 : I.offsetWidth) ?? 200;
    if (s != null && i != null) {
      let T = i, S = s;
      T + g > window.innerHeight && (T = Math.max(0, window.innerHeight - g)), S + v > window.innerWidth && (S = Math.max(0, window.innerWidth - v)), m({ top: T, left: S }), f(0);
      return;
    }
    if (!c) return;
    const x = document.getElementById(c);
    if (!x) return;
    const L = x.getBoundingClientRect();
    let C = L.bottom + 4, k = L.left;
    C + g > window.innerHeight && (C = L.top - g - 4), k + v > window.innerWidth && (k = L.right - v), m({ top: C, left: k }), f(0);
  }, [a, c, s, i]);
  const b = et(() => {
    n("close");
  }, [n]), w = et((g) => {
    n("selectItem", { itemId: g });
  }, [n]);
  wt(() => {
    if (!a) return;
    const g = (v) => {
      r.current && !r.current.contains(v.target) && b();
    };
    return document.addEventListener("mousedown", g), () => document.removeEventListener("mousedown", g);
  }, [a, b]);
  const E = et((g) => {
    if (g.key === "Escape") {
      g.preventDefault(), b();
      return;
    }
    if (g.key === "ArrowDown")
      g.preventDefault(), f((v) => (v + 1) % _.length);
    else if (g.key === "ArrowUp")
      g.preventDefault(), f((v) => (v - 1 + _.length) % _.length);
    else if (g.key === "Enter" || g.key === " ") {
      g.preventDefault();
      const v = _[p];
      v && w(v.id);
    }
  }, [b, w, _, p]);
  return pt(a, r), a ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: r,
      tabIndex: -1,
      style: { position: "fixed", top: o.top, left: o.left },
      onKeyDown: E
    },
    u.map((g, v) => {
      if (g.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: v, className: "tlMenu__separator" });
      const L = _.indexOf(g) === p;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: g.id,
          type: "button",
          className: "tlMenu__item" + (L ? " tlMenu__item--focused" : "") + (g.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: g.disabled,
          tabIndex: L ? 0 : -1,
          onClick: () => w(g.id)
        },
        g.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + g.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, g.label)
      );
    })
  ) : null;
}, Ol = 768, $l = ({ controlId: l }) => {
  const t = G(), n = ae();
  e.useEffect(() => {
    const o = window.matchMedia(`(max-width: ${Ol}px)`), m = (f) => {
      n("reportDisplayClass", { displayClass: f ? "COMPACT" : "REGULAR" });
    };
    m(o.matches);
    const p = (f) => m(f.matches);
    return o.addEventListener("change", p), () => o.removeEventListener("change", p);
  }, [n]);
  const a = t.header, c = t.content, s = t.footer, i = t.snackbar, u = t.dialogManager, r = t.menuOverlay;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(K, { control: a })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(K, { control: c })), s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(K, { control: s })), /* @__PURE__ */ e.createElement(K, { control: i }), u && /* @__PURE__ */ e.createElement(K, { control: u }), r && /* @__PURE__ */ e.createElement(K, { control: r }));
}, Ul = ({ controlId: l }) => {
  const t = G(), n = t.text ?? "", a = t.cssClass ?? "", c = t.hasTooltip === !0, s = a ? `tlText ${a}` : "tlText";
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: s,
      "data-tooltip": c ? "key:tooltip" : void 0
    },
    n
  );
}, Hl = ({ isMulti: l, cursorIndex: t, onMove: n, onToggle: a, onSelectAll: c }) => (de("ArrowUp", () => (n("up", !1, !1), !0)), de("ArrowDown", () => (n("down", !1, !1), !0)), de("Home", () => (n("home", !1, !1), !0)), de("End", () => (n("end", !1, !1), !0)), de("PageUp", () => (n("pageUp", !1, !1), !0)), de("PageDown", () => (n("pageDown", !1, !1), !0)), de("Shift+ArrowUp", () => (n("up", l, !1), !0)), de("Shift+ArrowDown", () => (n("down", l, !1), !0)), de("Shift+Home", () => (n("home", l, !1), !0)), de("Shift+End", () => (n("end", l, !1), !0)), de("Shift+PageUp", () => (n("pageUp", l, !1), !0)), de("Shift+PageDown", () => (n("pageDown", l, !1), !0)), de("Ctrl+ArrowUp", () => (n("up", !1, l), !0)), de("Ctrl+ArrowDown", () => (n("down", !1, l), !0)), de("Space", () => t < 0 ? !1 : (a(), !0)), de("Ctrl+A", () => l ? (c(), !0) : !1), null), Wl = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all",
  "js.table.filter": "Filter"
}, kt = 50;
function St(l) {
  var n;
  const t = l.target;
  return !!((n = t == null ? void 0 : t.closest) != null && n.call(t, 'input, textarea, select, button, a, [contenteditable="true"]'));
}
const it = 'input:not([disabled]):not([readonly]), textarea:not([disabled]):not([readonly]), select:not([disabled]), [contenteditable="true"]', zl = it + ", button:not([disabled]), a[href]";
function Ft(l, t) {
  return Array.from(l.querySelectorAll("[data-row][data-col]")).filter((n) => n.dataset.row === t);
}
function Nt(l, t, n = {}) {
  const a = Ft(l, t);
  if (n.col) {
    const s = a.find((u) => u.dataset.col === n.col), i = s == null ? void 0 : s.querySelector(it);
    if (i) return i;
  }
  const c = n.last ? [...a].reverse() : a;
  for (const s of c) {
    const i = s.querySelector(it);
    if (i) return i;
  }
  return null;
}
const Vl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ie(Wl), c = e.useRef(null);
  e.useEffect(() => {
    const y = c.current;
    if (!y) return;
    const D = (z) => {
      const J = z.detail;
      let te = J.target;
      for (; te && te !== y; ) {
        const ce = te.dataset.row, re = te.dataset.col;
        if (ce != null && re != null) {
          J.resolved = { key: ce + "|" + re };
          return;
        }
        te = te.parentElement;
      }
    };
    return y.addEventListener("tl-tooltip-resolve", D), () => y.removeEventListener("tl-tooltip-resolve", D);
  }, []);
  const s = t.columns ?? [], i = t.totalRowCount ?? 0, u = t.rows ?? [], r = t.rowHeight ?? 36, o = t.selectionMode ?? "single", m = t.selectedCount ?? 0, p = t.cursorIndex ?? -1, f = t.frozenColumnCount ?? 0, _ = t.treeMode ?? !1, b = e.useMemo(
    () => s.filter((y) => y.sortPriority && y.sortPriority > 0).length,
    [s]
  ), w = o === "multi", E = 40, g = 20, v = e.useRef(null), x = e.useRef(null), L = e.useRef(null), C = e.useRef(null), [k, h] = e.useState({}), I = e.useRef(null), T = e.useRef(!1), S = e.useRef(null), [H, B] = e.useState(null), [R, F] = e.useState(null);
  e.useEffect(() => {
    I.current || h({});
  }, [s]);
  const Q = e.useCallback((y) => k[y.name] ?? y.width, [k]), W = e.useMemo(() => {
    const y = [];
    let D = w && f > 0 ? E : 0;
    for (let z = 0; z < f && z < s.length; z++)
      y.push(D), D += Q(s[z]);
    return y;
  }, [s, f, w, E, Q]), O = i * r, P = e.useRef(null), j = e.useCallback((y, D, z) => {
    z.preventDefault(), z.stopPropagation(), I.current = { column: y, startX: z.clientX, startWidth: D };
    let J = z.clientX, te = 0;
    const ce = () => {
      const ue = I.current;
      if (!ue) return;
      const me = Math.max(kt, ue.startWidth + (J - ue.startX) + te);
      h((Ce) => ({ ...Ce, [ue.column]: me }));
    }, re = () => {
      const ue = x.current, me = v.current;
      if (!ue || !I.current) return;
      const Ce = ue.getBoundingClientRect(), Ne = 40, ht = 8, Kt = ue.scrollLeft;
      J > Ce.right - Ne ? ue.scrollLeft += ht : J < Ce.left + Ne && (ue.scrollLeft = Math.max(0, ue.scrollLeft - ht));
      const bt = ue.scrollLeft - Kt;
      bt !== 0 && (me && (me.scrollLeft = ue.scrollLeft), te += bt, ce()), P.current = requestAnimationFrame(re);
    };
    P.current = requestAnimationFrame(re);
    const _e = (ue) => {
      J = ue.clientX, ce();
    }, Se = (ue) => {
      document.removeEventListener("mousemove", _e), document.removeEventListener("mouseup", Se), P.current !== null && (cancelAnimationFrame(P.current), P.current = null);
      const me = I.current;
      if (me) {
        const Ce = Math.max(kt, me.startWidth + (ue.clientX - me.startX) + te);
        n("columnResize", { column: me.column, width: Ce }), I.current = null, T.current = !0, requestAnimationFrame(() => {
          T.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", _e), document.addEventListener("mouseup", Se);
  }, [n]), X = e.useCallback(() => {
    v.current && x.current && (v.current.scrollLeft = x.current.scrollLeft), L.current !== null && clearTimeout(L.current), L.current = window.setTimeout(() => {
      const y = x.current;
      if (!y) return;
      const D = y.scrollTop, z = Math.ceil(y.clientHeight / r), J = Math.floor(D / r);
      n("scroll", { start: J, count: z });
    }, 80);
  }, [n, r]), d = e.useCallback((y, D, z) => {
    if (T.current) return;
    let J;
    !D || D === "desc" ? J = "asc" : J = "desc";
    const te = z.shiftKey ? "add" : "replace";
    n("sort", { column: y, direction: J, mode: te });
  }, [n]), N = e.useCallback((y, D) => {
    S.current = y, D.dataTransfer.effectAllowed = "move", D.dataTransfer.setData("text/plain", y);
  }, []), V = e.useCallback((y, D) => {
    if (!S.current || S.current === y) {
      B(null);
      return;
    }
    D.preventDefault(), D.dataTransfer.dropEffect = "move";
    const z = D.currentTarget.getBoundingClientRect(), J = D.clientX < z.left + z.width / 2 ? "left" : "right";
    B({ column: y, side: J });
  }, []), $ = e.useCallback((y) => {
    y.preventDefault(), y.stopPropagation();
    const D = S.current;
    if (!D || !H) {
      S.current = null, B(null);
      return;
    }
    let z = s.findIndex((te) => te.name === H.column);
    if (z < 0) {
      S.current = null, B(null);
      return;
    }
    const J = s.findIndex((te) => te.name === D);
    H.side === "right" && z++, J < z && z--, n("columnReorder", { column: D, targetIndex: z }), S.current = null, B(null);
  }, [s, H, n]), q = e.useCallback(() => {
    S.current = null, B(null);
  }, []), A = e.useCallback((y, D) => {
    var J, te, ce, re;
    const z = window.getSelection();
    if (!(z && !z.isCollapsed && D.currentTarget.contains(z.anchorNode))) {
      if (!St(D) && ((J = x.current) == null || J.focus({ preventScroll: !0 }), !D.ctrlKey && !D.metaKey && !D.shiftKey)) {
        const _e = (re = (ce = (te = D.target) == null ? void 0 : te.closest) == null ? void 0 : ce.call(te, "[data-col]")) == null ? void 0 : re.getAttribute("data-col");
        C.current = { index: y, col: _e ?? void 0 };
      }
      n("select", {
        rowIndex: y,
        ctrlKey: D.ctrlKey || D.metaKey,
        shiftKey: D.shiftKey
      });
    }
  }, [n]), ee = e.useCallback((y, D, z) => {
    n("moveSelection", { direction: y, extend: D, move: z });
  }, [n]), oe = e.useCallback(() => {
    p < 0 || n("select", { rowIndex: p, ctrlKey: w, shiftKey: !1 });
  }, [n, p, w]), le = e.useCallback(() => {
    n("selectAll", { selected: !0 });
  }, [n]), he = e.useCallback(
    () => !!c.current && c.current.contains(document.activeElement),
    []
  );
  e.useEffect(() => {
    if (p < 0)
      return;
    const y = x.current;
    if (!y)
      return;
    const D = p * r, z = D + r;
    D < y.scrollTop ? y.scrollTop = D : z > y.scrollTop + y.clientHeight && (y.scrollTop = z - y.clientHeight);
  }, [p, r]), e.useEffect(() => {
    const y = C.current, D = x.current;
    if (!y || !D)
      return;
    const z = u.find((te) => te.index === y.index);
    if (!z)
      return;
    const J = Nt(D, z.id, { col: y.col, last: y.last });
    J && (C.current = null, J.focus({ preventScroll: !1 }), J instanceof HTMLInputElement && J.select());
  }, [u]);
  const ge = e.useCallback((y) => {
    if (y.key !== "Tab")
      return;
    const D = x.current, z = document.activeElement;
    if (!D || !z || !D.contains(z))
      return;
    const J = z.closest("[data-row][data-col]");
    if (!J)
      return;
    const te = J.dataset.row, ce = u.find((Ne) => Ne.id === te);
    if (!ce)
      return;
    const re = Ft(D, te).flatMap((Ne) => Array.from(Ne.querySelectorAll(zl))), _e = re.indexOf(z);
    if (_e < 0)
      return;
    const Se = !y.shiftKey;
    if (!(Se ? _e === re.length - 1 : _e === 0))
      return;
    const me = Se ? ce.index + 1 : ce.index - 1;
    if (me < 0 || me >= i)
      return;
    const Ce = u.find((Ne) => Ne.index === me);
    Ce && Nt(D, Ce.id) || (y.preventDefault(), C.current = { index: me, last: !Se }, n("select", { rowIndex: me, ctrlKey: !1, shiftKey: !1 }));
  }, [u, i, n]), ve = e.useCallback((y, D) => {
    D.stopPropagation(), n("select", { rowIndex: y, ctrlKey: !0, shiftKey: !1 });
  }, [n]), ye = e.useCallback(() => {
    const y = m === i && i > 0;
    n("selectAll", { selected: !y });
  }, [n, m, i]), ke = e.useCallback((y, D, z) => {
    z.stopPropagation(), n("expand", { rowIndex: y, expanded: D });
  }, [n]), xe = e.useCallback((y, D) => {
    D.preventDefault(), F({ x: D.clientX, y: D.clientY, colIdx: y });
  }, []), He = e.useCallback(() => {
    R && (n("setFrozenColumnCount", { count: R.colIdx + 1 }), F(null));
  }, [R, n]), M = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), F(null);
  }, [n]);
  e.useEffect(() => {
    if (!R) return;
    const y = () => F(null);
    return document.addEventListener("mousedown", y), () => document.removeEventListener("mousedown", y);
  }, [R]), Le(!!R, { ESCAPE: () => F(null) });
  const Y = e.useCallback((y, D) => {
    D.stopPropagation(), D.preventDefault(), n("openFilter", { column: y });
  }, [n]), ne = s.reduce((y, D) => y + Q(D), 0) + (w ? E : 0), se = m === i && i > 0, Ie = m > 0 && m < i, Vt = e.useCallback((y) => {
    y && (y.indeterminate = Ie);
  }, [Ie]);
  return /* @__PURE__ */ e.createElement(mt, { active: he }, /* @__PURE__ */ e.createElement(
    Hl,
    {
      isMulti: w,
      cursorIndex: p,
      onMove: ee,
      onToggle: oe,
      onSelectAll: le
    }
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: c,
      id: l,
      className: "tlTableView",
      "data-tooltip": "dynamic",
      onDragOver: (y) => {
        if (!S.current) return;
        y.preventDefault();
        const D = x.current, z = v.current;
        if (!D) return;
        const J = D.getBoundingClientRect(), te = 40, ce = 8;
        y.clientX < J.left + te ? D.scrollLeft = Math.max(0, D.scrollLeft - ce) : y.clientX > J.right - te && (D.scrollLeft += ce), z && (z.scrollLeft = D.scrollLeft);
      },
      onDrop: $
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: v }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { width: ne } }, w && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (f > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: E,
          minWidth: E,
          ...f > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (y) => {
          S.current && (y.preventDefault(), y.dataTransfer.dropEffect = "move", s.length > 0 && s[0].name !== S.current && B({ column: s[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: Vt,
          className: "tlTableView__checkbox",
          checked: se,
          onChange: ye
        }
      )
    ), s.map((y, D) => {
      const z = Q(y);
      s.length - 1;
      let J = "tlTableView__headerCell";
      y.sortable && (J += " tlTableView__headerCell--sortable"), H && H.column === y.name && (J += " tlTableView__headerCell--dragOver-" + H.side);
      const te = D < f, ce = D === f - 1;
      return te && (J += " tlTableView__headerCell--frozen"), ce && (J += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: y.name,
          className: J,
          style: {
            width: z,
            minWidth: z,
            position: te ? "sticky" : "relative",
            ...te ? { left: W[D], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: y.sortable ? (re) => d(y.name, y.sortDirection, re) : void 0,
          onContextMenu: (re) => xe(D, re),
          onDragStart: (re) => N(y.name, re),
          onDragOver: (re) => V(y.name, re),
          onDrop: $,
          onDragEnd: q
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, y.label),
        y.filterable && /* @__PURE__ */ e.createElement(
          "button",
          {
            type: "button",
            className: "tlTableView__filterButton" + (y.filterActive ? " tlTableView__filterButton--active" : ""),
            title: a["js.table.filter"],
            style: {
              border: "none",
              background: "transparent",
              cursor: "pointer",
              padding: "0 4px",
              color: y.filterActive ? "#1565c0" : "inherit"
            },
            onMouseDown: (re) => re.stopPropagation(),
            onClick: (re) => Y(y.name, re)
          },
          /* @__PURE__ */ e.createElement("i", { className: y.filterActive ? "bi bi-funnel-fill" : "bi bi-funnel" })
        ),
        y.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, y.sortDirection === "asc" ? "▲" : "▼", b > 1 && y.sortPriority != null && y.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, y.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (re) => j(y.name, z, re)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (y) => {
          if (S.current && s.length > 0) {
            const D = s[s.length - 1];
            D.name !== S.current && (y.preventDefault(), y.dataTransfer.dropEffect = "move", B({ column: D.name, side: "right" }));
          }
        },
        onDrop: $
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: x,
        className: "tlTableView__body",
        onScroll: X,
        onKeyDown: ge,
        tabIndex: 0
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: O, position: "relative", width: ne } }, u.map((y) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: y.id,
          className: "tlTableView__row" + (y.selected ? " tlTableView__row--selected" : "") + (y.index === p ? " tlTableView__row--cursor" : ""),
          style: {
            position: "absolute",
            top: y.index * r,
            height: r,
            width: ne,
            ...y.index === p ? { outline: "2px solid var(--color-primary, #1a73e8)", outlineOffset: "-2px" } : {}
          },
          onMouseDown: (D) => {
            (D.shiftKey || D.ctrlKey || D.metaKey || D.detail > 1) && !St(D) && D.preventDefault();
          },
          onClick: (D) => A(y.index, D)
        },
        w && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (f > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: E,
              minWidth: E,
              ...f > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (D) => D.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: y.selected,
              onChange: () => {
              },
              onClick: (D) => ve(y.index, D),
              tabIndex: -1
            }
          )
        ),
        s.map((D, z) => {
          const J = Q(D), te = z === s.length - 1, ce = z < f, re = z === f - 1;
          let _e = "tlTableView__cell";
          ce && (_e += " tlTableView__cell--frozen"), re && (_e += " tlTableView__cell--frozenLast");
          const Se = _ && z === 0, ue = y.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: D.name,
              className: _e,
              "data-row": y.id,
              "data-col": D.name,
              style: {
                ...te && !ce ? { flex: "1 0 auto", minWidth: J } : { width: J, minWidth: J },
                ...ce ? { position: "sticky", left: W[z], zIndex: 2 } : {}
              }
            },
            Se ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ue * g } }, y.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (me) => ke(y.index, !y.expanded, me)
              },
              y.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(K, { control: y.cells[D.name] })) : /* @__PURE__ */ e.createElement(K, { control: y.cells[D.name] })
          );
        })
      )))
    ),
    R && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: R.y, left: R.x, zIndex: 1e4 },
        onMouseDown: (y) => y.stopPropagation()
      },
      R.colIdx + 1 !== f && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: He }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      f > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: M }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
    )
  ));
}, Kl = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, Ot = e.createContext(Kl), { useMemo: Yl, useRef: Gl, useState: Xl, useEffect: ql } = e, Zl = 320, Ql = "TLTableView", Jl = "TLPanel", ea = ({ controlId: l }) => {
  var E;
  const t = G(), n = t.maxColumns ?? 3, a = t.labelPosition ?? "auto", c = t.readOnly === !0, s = t.children ?? [], i = t.noModelMessage, u = Gl(null), [r, o] = Xl(
    a === "top" ? "top" : "side"
  );
  ql(() => {
    if (a !== "auto") {
      o(a);
      return;
    }
    const g = u.current;
    if (!g) return;
    const v = new ResizeObserver((x) => {
      for (const L of x) {
        const k = L.contentRect.width / n;
        o(k < Zl ? "top" : "side");
      }
    });
    return v.observe(g), () => v.disconnect();
  }, [a, n]);
  const m = Yl(() => ({
    readOnly: c,
    resolvedLabelPosition: r
  }), [c, r]), f = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, _ = s.length === 1 ? s[0] : void 0, b = !!_ && (_.module === Ql || _.module === Jl && ((E = _.state) == null ? void 0 : E.bare) === !0), w = [
    "tlFormLayout",
    c ? "tlFormLayout--readonly" : "",
    b ? "tlFormLayout--flush" : ""
  ].filter(Boolean).join(" ");
  return i ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, i)) : /* @__PURE__ */ e.createElement(Ot.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: l, className: w, style: f, ref: u }, s.map((g, v) => /* @__PURE__ */ e.createElement(K, { key: v, control: g }))));
}, { useCallback: ta } = e, na = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, la = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ie(na), c = t.headerControl ?? null, s = t.headerActions ?? [], i = t.collapsible === !0, u = t.collapsed === !0, r = t.border ?? "none", o = t.fullLine === !0, m = t.children ?? [], p = c != null || s.length > 0 || i, f = ta(() => {
    n("toggleCollapse");
  }, [n]), _ = [
    "tlFormGroup",
    `tlFormGroup--border-${r}`,
    o ? "tlFormGroup--fullLine" : "",
    u ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: _ }, p && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, i && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: f,
      "aria-expanded": !u,
      title: u ? a["js.formGroup.expand"] : a["js.formGroup.collapse"]
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
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(K, { control: c })), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, s.map((b, w) => /* @__PURE__ */ e.createElement(K, { key: w, control: b })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((b, w) => /* @__PURE__ */ e.createElement(K, { key: w, control: b }))));
}, { useContext: aa, useState: ra, useCallback: oa } = e, sa = ({ controlId: l }) => {
  const t = G(), n = aa(Ot), a = t.label ?? "", c = t.required === !0, s = t.error, i = t.errorIcon, u = t.warnings, r = t.warningIcon, o = t.helpText, m = t.dirty === !0, p = t.labelPosition ?? n.resolvedLabelPosition, f = t.fullLine === !0, _ = t.visible !== !1, b = t.hasTooltip === !0, w = t.field, E = n.readOnly, [g, v] = ra(!1), x = oa(() => v((I) => !I), []), L = p === "hidden", C = s != null, k = u != null && u.length > 0, h = [
    "tlFormField",
    `tlFormField--${p}`,
    E ? "tlFormField--readonly" : "",
    f ? "tlFormField--fullLine" : "",
    C ? "tlFormField--error" : "",
    !C && k ? "tlFormField--warning" : "",
    m ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: h, style: _ ? void 0 : { display: "none" } }, !L && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": b ? "key:tooltip" : void 0
    },
    a
  ), c && !E && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), m && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), o && !E && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormField__helpIcon",
      onClick: x,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(K, { control: w })), !E && C && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(qe, { image: i, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, s)), !E && !C && k && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, u.map((I, T) => /* @__PURE__ */ e.createElement("div", { key: T, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(qe, { image: r, className: "tlFormField__warningIcon" }), /* @__PURE__ */ e.createElement("span", null, I)))), !E && o && g && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, o));
}, ca = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.iconCss, c = t.iconSrc, s = t.label, i = t.cssClass, u = t.hasTooltip === !0, r = t.hasLink, o = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : c ? /* @__PURE__ */ e.createElement("img", { src: c, className: "tlTypeIcon", alt: "" }) : null, m = /* @__PURE__ */ e.createElement(e.Fragment, null, o, s && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, s)), p = e.useCallback((b) => {
    b.preventDefault(), n("goto", {});
  }, [n]), f = ["tlResourceCell", i].filter(Boolean).join(" "), _ = u ? "key:tooltip" : void 0;
  return r ? /* @__PURE__ */ e.createElement(
    "a",
    {
      id: l,
      className: f,
      href: "#",
      onClick: p,
      "data-tooltip": _
    },
    m
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: f, "data-tooltip": _ }, m);
}, ia = 20, ua = () => {
  var k;
  const l = G(), t = ae(), n = l.nodes ?? [], a = l.selectionMode ?? "single", c = l.dragEnabled ?? !1, s = l.dropEnabled ?? !1, i = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [r, o] = e.useState(-1), m = e.useRef(null), p = ((k = n.find((h) => h.selected)) == null ? void 0 : k.id) ?? null;
  e.useEffect(() => {
    var I;
    if (p == null)
      return;
    const h = (I = m.current) == null ? void 0 : I.querySelector(".tlTreeView__node--selected");
    h && h.scrollIntoView({ block: "nearest" });
  }, [p]);
  const f = e.useCallback((h, I) => {
    t(I ? "collapse" : "expand", { nodeId: h });
  }, [t]), _ = e.useCallback((h, I) => {
    var S;
    const T = window.getSelection();
    T && !T.isCollapsed && I.currentTarget.contains(T.anchorNode) || ((S = m.current) == null || S.focus({ preventScroll: !0 }), t("select", {
      nodeId: h,
      ctrlKey: I.ctrlKey || I.metaKey,
      shiftKey: I.shiftKey
    }));
  }, [t]), b = e.useCallback((h, I) => {
    I.preventDefault(), t("contextMenu", { nodeId: h, x: I.clientX, y: I.clientY });
  }, [t]), w = e.useRef(null), E = e.useCallback((h, I) => {
    const T = I.getBoundingClientRect(), S = h.clientY - T.top, H = T.height / 3;
    return S < H ? "above" : S > H * 2 ? "below" : "within";
  }, []), g = e.useCallback((h, I) => {
    I.dataTransfer.effectAllowed = "move", I.dataTransfer.setData("text/plain", h);
  }, []), v = e.useCallback((h, I) => {
    I.preventDefault(), I.dataTransfer.dropEffect = "move";
    const T = E(I, I.currentTarget);
    w.current != null && window.clearTimeout(w.current), w.current = window.setTimeout(() => {
      t("dragOver", { nodeId: h, position: T }), w.current = null;
    }, 50);
  }, [t, E]), x = e.useCallback((h, I) => {
    I.preventDefault(), w.current != null && (window.clearTimeout(w.current), w.current = null);
    const T = E(I, I.currentTarget);
    t("drop", { nodeId: h, position: T });
  }, [t, E]), L = e.useCallback(() => {
    w.current != null && (window.clearTimeout(w.current), w.current = null), t("dragEnd");
  }, [t]), C = e.useCallback((h) => {
    if (n.length === 0) return;
    let I = r;
    switch (h.key) {
      case "ArrowDown":
        h.preventDefault(), I = Math.min(r + 1, n.length - 1);
        break;
      case "ArrowUp":
        h.preventDefault(), I = Math.max(r - 1, 0);
        break;
      case "ArrowRight":
        if (h.preventDefault(), r >= 0 && r < n.length) {
          const T = n[r];
          if (T.expandable && !T.expanded) {
            t("expand", { nodeId: T.id });
            return;
          } else T.expanded && (I = r + 1);
        }
        break;
      case "ArrowLeft":
        if (h.preventDefault(), r >= 0 && r < n.length) {
          const T = n[r];
          if (T.expanded) {
            t("collapse", { nodeId: T.id });
            return;
          } else {
            const S = T.depth;
            for (let H = r - 1; H >= 0; H--)
              if (n[H].depth < S) {
                I = H;
                break;
              }
          }
        }
        break;
      case "Enter":
        h.preventDefault(), r >= 0 && r < n.length && t("select", {
          nodeId: n[r].id,
          ctrlKey: h.ctrlKey || h.metaKey,
          shiftKey: h.shiftKey
        });
        return;
      case " ":
        h.preventDefault(), a === "multi" && r >= 0 && r < n.length && t("select", {
          nodeId: n[r].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        h.preventDefault(), I = 0;
        break;
      case "End":
        h.preventDefault(), I = n.length - 1;
        break;
      default:
        return;
    }
    I !== r && o(I);
  }, [r, n, t, a]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: m,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: C
    },
    n.map((h, I) => /* @__PURE__ */ e.createElement(
      "li",
      {
        key: h.id,
        role: "treeitem",
        "aria-expanded": h.expandable ? h.expanded : void 0,
        "aria-selected": h.selected,
        "aria-level": h.depth + 1,
        className: [
          "tlTreeView__node",
          h.selected ? "tlTreeView__node--selected" : "",
          I === r ? "tlTreeView__node--focused" : "",
          i === h.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          i === h.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          i === h.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: h.depth * ia },
        draggable: c,
        onMouseDown: (T) => {
          (T.shiftKey || T.ctrlKey || T.metaKey || T.detail > 1) && T.preventDefault();
        },
        onClick: (T) => _(h.id, T),
        onContextMenu: (T) => b(h.id, T),
        onDragStart: (T) => g(h.id, T),
        onDragOver: s ? (T) => v(h.id, T) : void 0,
        onDrop: s ? (T) => x(h.id, T) : void 0,
        onDragEnd: L
      },
      h.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (T) => {
            T.stopPropagation(), f(h.id, h.expanded);
          },
          tabIndex: -1,
          "aria-label": h.expanded ? "Collapse" : "Expand"
        },
        h.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: h.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(K, { control: h.content }))
    ))
  );
};
var tt = { exports: {} }, pe = {}, nt = { exports: {} }, Z = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var Tt;
function da() {
  if (Tt) return Z;
  Tt = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), a = Symbol.for("react.strict_mode"), c = Symbol.for("react.profiler"), s = Symbol.for("react.consumer"), i = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), r = Symbol.for("react.suspense"), o = Symbol.for("react.memo"), m = Symbol.for("react.lazy"), p = Symbol.for("react.activity"), f = Symbol.iterator;
  function _(d) {
    return d === null || typeof d != "object" ? null : (d = f && d[f] || d["@@iterator"], typeof d == "function" ? d : null);
  }
  var b = {
    isMounted: function() {
      return !1;
    },
    enqueueForceUpdate: function() {
    },
    enqueueReplaceState: function() {
    },
    enqueueSetState: function() {
    }
  }, w = Object.assign, E = {};
  function g(d, N, V) {
    this.props = d, this.context = N, this.refs = E, this.updater = V || b;
  }
  g.prototype.isReactComponent = {}, g.prototype.setState = function(d, N) {
    if (typeof d != "object" && typeof d != "function" && d != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, d, N, "setState");
  }, g.prototype.forceUpdate = function(d) {
    this.updater.enqueueForceUpdate(this, d, "forceUpdate");
  };
  function v() {
  }
  v.prototype = g.prototype;
  function x(d, N, V) {
    this.props = d, this.context = N, this.refs = E, this.updater = V || b;
  }
  var L = x.prototype = new v();
  L.constructor = x, w(L, g.prototype), L.isPureReactComponent = !0;
  var C = Array.isArray;
  function k() {
  }
  var h = { H: null, A: null, T: null, S: null }, I = Object.prototype.hasOwnProperty;
  function T(d, N, V) {
    var $ = V.ref;
    return {
      $$typeof: l,
      type: d,
      key: N,
      ref: $ !== void 0 ? $ : null,
      props: V
    };
  }
  function S(d, N) {
    return T(d.type, N, d.props);
  }
  function H(d) {
    return typeof d == "object" && d !== null && d.$$typeof === l;
  }
  function B(d) {
    var N = { "=": "=0", ":": "=2" };
    return "$" + d.replace(/[=:]/g, function(V) {
      return N[V];
    });
  }
  var R = /\/+/g;
  function F(d, N) {
    return typeof d == "object" && d !== null && d.key != null ? B("" + d.key) : N.toString(36);
  }
  function Q(d) {
    switch (d.status) {
      case "fulfilled":
        return d.value;
      case "rejected":
        throw d.reason;
      default:
        switch (typeof d.status == "string" ? d.then(k, k) : (d.status = "pending", d.then(
          function(N) {
            d.status === "pending" && (d.status = "fulfilled", d.value = N);
          },
          function(N) {
            d.status === "pending" && (d.status = "rejected", d.reason = N);
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
  function W(d, N, V, $, q) {
    var A = typeof d;
    (A === "undefined" || A === "boolean") && (d = null);
    var ee = !1;
    if (d === null) ee = !0;
    else
      switch (A) {
        case "bigint":
        case "string":
        case "number":
          ee = !0;
          break;
        case "object":
          switch (d.$$typeof) {
            case l:
            case t:
              ee = !0;
              break;
            case m:
              return ee = d._init, W(
                ee(d._payload),
                N,
                V,
                $,
                q
              );
          }
      }
    if (ee)
      return q = q(d), ee = $ === "" ? "." + F(d, 0) : $, C(q) ? (V = "", ee != null && (V = ee.replace(R, "$&/") + "/"), W(q, N, V, "", function(he) {
        return he;
      })) : q != null && (H(q) && (q = S(
        q,
        V + (q.key == null || d && d.key === q.key ? "" : ("" + q.key).replace(
          R,
          "$&/"
        ) + "/") + ee
      )), N.push(q)), 1;
    ee = 0;
    var oe = $ === "" ? "." : $ + ":";
    if (C(d))
      for (var le = 0; le < d.length; le++)
        $ = d[le], A = oe + F($, le), ee += W(
          $,
          N,
          V,
          A,
          q
        );
    else if (le = _(d), typeof le == "function")
      for (d = le.call(d), le = 0; !($ = d.next()).done; )
        $ = $.value, A = oe + F($, le++), ee += W(
          $,
          N,
          V,
          A,
          q
        );
    else if (A === "object") {
      if (typeof d.then == "function")
        return W(
          Q(d),
          N,
          V,
          $,
          q
        );
      throw N = String(d), Error(
        "Objects are not valid as a React child (found: " + (N === "[object Object]" ? "object with keys {" + Object.keys(d).join(", ") + "}" : N) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return ee;
  }
  function O(d, N, V) {
    if (d == null) return d;
    var $ = [], q = 0;
    return W(d, $, "", "", function(A) {
      return N.call(V, A, q++);
    }), $;
  }
  function P(d) {
    if (d._status === -1) {
      var N = d._result;
      N = N(), N.then(
        function(V) {
          (d._status === 0 || d._status === -1) && (d._status = 1, d._result = V);
        },
        function(V) {
          (d._status === 0 || d._status === -1) && (d._status = 2, d._result = V);
        }
      ), d._status === -1 && (d._status = 0, d._result = N);
    }
    if (d._status === 1) return d._result.default;
    throw d._result;
  }
  var j = typeof reportError == "function" ? reportError : function(d) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var N = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof d == "object" && d !== null && typeof d.message == "string" ? String(d.message) : String(d),
        error: d
      });
      if (!window.dispatchEvent(N)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", d);
      return;
    }
    console.error(d);
  }, X = {
    map: O,
    forEach: function(d, N, V) {
      O(
        d,
        function() {
          N.apply(this, arguments);
        },
        V
      );
    },
    count: function(d) {
      var N = 0;
      return O(d, function() {
        N++;
      }), N;
    },
    toArray: function(d) {
      return O(d, function(N) {
        return N;
      }) || [];
    },
    only: function(d) {
      if (!H(d))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return d;
    }
  };
  return Z.Activity = p, Z.Children = X, Z.Component = g, Z.Fragment = n, Z.Profiler = c, Z.PureComponent = x, Z.StrictMode = a, Z.Suspense = r, Z.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = h, Z.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(d) {
      return h.H.useMemoCache(d);
    }
  }, Z.cache = function(d) {
    return function() {
      return d.apply(null, arguments);
    };
  }, Z.cacheSignal = function() {
    return null;
  }, Z.cloneElement = function(d, N, V) {
    if (d == null)
      throw Error(
        "The argument must be a React element, but you passed " + d + "."
      );
    var $ = w({}, d.props), q = d.key;
    if (N != null)
      for (A in N.key !== void 0 && (q = "" + N.key), N)
        !I.call(N, A) || A === "key" || A === "__self" || A === "__source" || A === "ref" && N.ref === void 0 || ($[A] = N[A]);
    var A = arguments.length - 2;
    if (A === 1) $.children = V;
    else if (1 < A) {
      for (var ee = Array(A), oe = 0; oe < A; oe++)
        ee[oe] = arguments[oe + 2];
      $.children = ee;
    }
    return T(d.type, q, $);
  }, Z.createContext = function(d) {
    return d = {
      $$typeof: i,
      _currentValue: d,
      _currentValue2: d,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, d.Provider = d, d.Consumer = {
      $$typeof: s,
      _context: d
    }, d;
  }, Z.createElement = function(d, N, V) {
    var $, q = {}, A = null;
    if (N != null)
      for ($ in N.key !== void 0 && (A = "" + N.key), N)
        I.call(N, $) && $ !== "key" && $ !== "__self" && $ !== "__source" && (q[$] = N[$]);
    var ee = arguments.length - 2;
    if (ee === 1) q.children = V;
    else if (1 < ee) {
      for (var oe = Array(ee), le = 0; le < ee; le++)
        oe[le] = arguments[le + 2];
      q.children = oe;
    }
    if (d && d.defaultProps)
      for ($ in ee = d.defaultProps, ee)
        q[$] === void 0 && (q[$] = ee[$]);
    return T(d, A, q);
  }, Z.createRef = function() {
    return { current: null };
  }, Z.forwardRef = function(d) {
    return { $$typeof: u, render: d };
  }, Z.isValidElement = H, Z.lazy = function(d) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: d },
      _init: P
    };
  }, Z.memo = function(d, N) {
    return {
      $$typeof: o,
      type: d,
      compare: N === void 0 ? null : N
    };
  }, Z.startTransition = function(d) {
    var N = h.T, V = {};
    h.T = V;
    try {
      var $ = d(), q = h.S;
      q !== null && q(V, $), typeof $ == "object" && $ !== null && typeof $.then == "function" && $.then(k, j);
    } catch (A) {
      j(A);
    } finally {
      N !== null && V.types !== null && (N.types = V.types), h.T = N;
    }
  }, Z.unstable_useCacheRefresh = function() {
    return h.H.useCacheRefresh();
  }, Z.use = function(d) {
    return h.H.use(d);
  }, Z.useActionState = function(d, N, V) {
    return h.H.useActionState(d, N, V);
  }, Z.useCallback = function(d, N) {
    return h.H.useCallback(d, N);
  }, Z.useContext = function(d) {
    return h.H.useContext(d);
  }, Z.useDebugValue = function() {
  }, Z.useDeferredValue = function(d, N) {
    return h.H.useDeferredValue(d, N);
  }, Z.useEffect = function(d, N) {
    return h.H.useEffect(d, N);
  }, Z.useEffectEvent = function(d) {
    return h.H.useEffectEvent(d);
  }, Z.useId = function() {
    return h.H.useId();
  }, Z.useImperativeHandle = function(d, N, V) {
    return h.H.useImperativeHandle(d, N, V);
  }, Z.useInsertionEffect = function(d, N) {
    return h.H.useInsertionEffect(d, N);
  }, Z.useLayoutEffect = function(d, N) {
    return h.H.useLayoutEffect(d, N);
  }, Z.useMemo = function(d, N) {
    return h.H.useMemo(d, N);
  }, Z.useOptimistic = function(d, N) {
    return h.H.useOptimistic(d, N);
  }, Z.useReducer = function(d, N, V) {
    return h.H.useReducer(d, N, V);
  }, Z.useRef = function(d) {
    return h.H.useRef(d);
  }, Z.useState = function(d) {
    return h.H.useState(d);
  }, Z.useSyncExternalStore = function(d, N, V) {
    return h.H.useSyncExternalStore(
      d,
      N,
      V
    );
  }, Z.useTransition = function() {
    return h.H.useTransition();
  }, Z.version = "19.2.4", Z;
}
var Rt;
function ma() {
  return Rt || (Rt = 1, nt.exports = da()), nt.exports;
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
var Dt;
function pa() {
  if (Dt) return pe;
  Dt = 1;
  var l = ma();
  function t(r) {
    var o = "https://react.dev/errors/" + r;
    if (1 < arguments.length) {
      o += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var m = 2; m < arguments.length; m++)
        o += "&args[]=" + encodeURIComponent(arguments[m]);
    }
    return "Minified React error #" + r + "; visit " + o + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function n() {
  }
  var a = {
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
  }, c = Symbol.for("react.portal");
  function s(r, o, m) {
    var p = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: c,
      key: p == null ? null : "" + p,
      children: r,
      containerInfo: o,
      implementation: m
    };
  }
  var i = l.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function u(r, o) {
    if (r === "font") return "";
    if (typeof o == "string")
      return o === "use-credentials" ? o : "";
  }
  return pe.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = a, pe.createPortal = function(r, o) {
    var m = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!o || o.nodeType !== 1 && o.nodeType !== 9 && o.nodeType !== 11)
      throw Error(t(299));
    return s(r, o, null, m);
  }, pe.flushSync = function(r) {
    var o = i.T, m = a.p;
    try {
      if (i.T = null, a.p = 2, r) return r();
    } finally {
      i.T = o, a.p = m, a.d.f();
    }
  }, pe.preconnect = function(r, o) {
    typeof r == "string" && (o ? (o = o.crossOrigin, o = typeof o == "string" ? o === "use-credentials" ? o : "" : void 0) : o = null, a.d.C(r, o));
  }, pe.prefetchDNS = function(r) {
    typeof r == "string" && a.d.D(r);
  }, pe.preinit = function(r, o) {
    if (typeof r == "string" && o && typeof o.as == "string") {
      var m = o.as, p = u(m, o.crossOrigin), f = typeof o.integrity == "string" ? o.integrity : void 0, _ = typeof o.fetchPriority == "string" ? o.fetchPriority : void 0;
      m === "style" ? a.d.S(
        r,
        typeof o.precedence == "string" ? o.precedence : void 0,
        {
          crossOrigin: p,
          integrity: f,
          fetchPriority: _
        }
      ) : m === "script" && a.d.X(r, {
        crossOrigin: p,
        integrity: f,
        fetchPriority: _,
        nonce: typeof o.nonce == "string" ? o.nonce : void 0
      });
    }
  }, pe.preinitModule = function(r, o) {
    if (typeof r == "string")
      if (typeof o == "object" && o !== null) {
        if (o.as == null || o.as === "script") {
          var m = u(
            o.as,
            o.crossOrigin
          );
          a.d.M(r, {
            crossOrigin: m,
            integrity: typeof o.integrity == "string" ? o.integrity : void 0,
            nonce: typeof o.nonce == "string" ? o.nonce : void 0
          });
        }
      } else o == null && a.d.M(r);
  }, pe.preload = function(r, o) {
    if (typeof r == "string" && typeof o == "object" && o !== null && typeof o.as == "string") {
      var m = o.as, p = u(m, o.crossOrigin);
      a.d.L(r, m, {
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
  }, pe.preloadModule = function(r, o) {
    if (typeof r == "string")
      if (o) {
        var m = u(o.as, o.crossOrigin);
        a.d.m(r, {
          as: typeof o.as == "string" && o.as !== "script" ? o.as : void 0,
          crossOrigin: m,
          integrity: typeof o.integrity == "string" ? o.integrity : void 0
        });
      } else a.d.m(r);
  }, pe.requestFormReset = function(r) {
    a.d.r(r);
  }, pe.unstable_batchedUpdates = function(r, o) {
    return r(o);
  }, pe.useFormState = function(r, o, m) {
    return i.H.useFormState(r, o, m);
  }, pe.useFormStatus = function() {
    return i.H.useHostTransitionStatus();
  }, pe.version = "19.2.4", pe;
}
var Lt;
function fa() {
  if (Lt) return tt.exports;
  Lt = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), tt.exports = pa(), tt.exports;
}
var $t = fa();
const { useState: Te, useCallback: fe, useRef: $e, useEffect: Me, useMemo: ut } = e;
function ft({ image: l }) {
  return l ? l.startsWith("/") ? /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" }) : /* @__PURE__ */ e.createElement(qe, { image: l, className: "tlDropdownSelect__optionIcon" }) : null;
}
function ha({
  option: l,
  removable: t,
  onRemove: n,
  removeLabel: a,
  draggable: c,
  onDragStart: s,
  onDragOver: i,
  onDrop: u,
  onDragEnd: r,
  dragClassName: o
}) {
  const m = fe(
    (p) => {
      p.stopPropagation(), n(l.value);
    },
    [n, l.value]
  );
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlDropdownSelect__chip" + (o ? " " + o : ""),
      draggable: c || void 0,
      onDragStart: s,
      onDragOver: i,
      onDrop: u,
      onDragEnd: r
    },
    c && /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__dragHandle", "aria-hidden": "true" }, "⋮⋮"),
    /* @__PURE__ */ e.createElement(ft, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__chipLabel" }, l.label),
    t && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__chipRemove",
        onClick: m,
        "aria-label": a
      },
      "×"
    )
  );
}
function ba({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: a,
  onMouseEnter: c,
  id: s
}) {
  const i = fe(() => a(l.value), [a, l.value]), u = ut(() => {
    if (!n) return l.label;
    const r = l.label.toLowerCase().indexOf(n.toLowerCase());
    return r < 0 ? l.label : /* @__PURE__ */ e.createElement(e.Fragment, null, l.label.substring(0, r), /* @__PURE__ */ e.createElement("strong", null, l.label.substring(r, r + n.length)), l.label.substring(r + n.length));
  }, [l.label, n]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: s,
      role: "option",
      "aria-selected": t,
      className: "tlDropdownSelect__option" + (t ? " tlDropdownSelect__option--highlighted" : ""),
      onClick: i,
      onMouseEnter: c
    },
    /* @__PURE__ */ e.createElement(ft, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const _a = ({ controlId: l, state: t }) => {
  const n = ae(), a = t.value ?? [], c = t.multiSelect === !0, s = t.customOrder === !0, i = t.mandatory === !0, u = t.disabled === !0, r = t.editable !== !1, o = t.optionsLoaded === !0, m = t.options ?? [], p = t.emptyOptionLabel ?? "", f = s && c && !u && r, _ = ie({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), b = _["js.dropdownSelect.nothingFound"], w = fe(
    (M) => _["js.dropdownSelect.removeChip"].replace("{0}", M),
    [_]
  ), [E, g] = Te(!1), [v, x] = Te(""), [L, C] = Te(-1), [k, h] = Te(!1), [I, T] = Te({}), [S, H] = Te(null), [B, R] = Te(null), [F, Q] = Te(null), W = $e(null), O = $e(null), P = $e(null), j = $e(a);
  j.current = a;
  const X = $e(-1), d = ut(
    () => new Set(a.map((M) => M.value)),
    [a]
  ), N = ut(() => {
    let M = m.filter((Y) => !d.has(Y.value));
    if (v) {
      const Y = v.toLowerCase();
      M = M.filter((ne) => ne.label.toLowerCase().includes(Y));
    }
    return M;
  }, [m, d, v]);
  Me(() => {
    v && N.length === 1 ? C(0) : C(-1);
  }, [N.length, v]), Me(() => {
    E && o && O.current && O.current.focus();
  }, [E, o, a]), Me(() => {
    var ne, se;
    if (X.current < 0) return;
    const M = X.current;
    X.current = -1;
    const Y = (ne = W.current) == null ? void 0 : ne.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    Y && Y.length > 0 ? Y[Math.min(M, Y.length - 1)].focus() : (se = W.current) == null || se.focus();
  }, [a]), Me(() => {
    if (!E) return;
    const M = (Y) => {
      W.current && !W.current.contains(Y.target) && P.current && !P.current.contains(Y.target) && (g(!1), x(""));
    };
    return document.addEventListener("mousedown", M), () => document.removeEventListener("mousedown", M);
  }, [E]), Me(() => {
    if (!E || !W.current) return;
    const M = W.current.getBoundingClientRect(), Y = window.innerHeight - M.bottom, se = Y < 300 && M.top > Y;
    T({
      left: M.left,
      width: M.width,
      ...se ? { bottom: window.innerHeight - M.top } : { top: M.bottom }
    });
  }, [E]);
  const V = fe(async () => {
    if (!(u || !r) && (g(!0), x(""), C(-1), h(!1), !o))
      try {
        await n("loadOptions");
      } catch {
        h(!0);
      }
  }, [u, r, o, n]), $ = fe(() => {
    var M;
    g(!1), x(""), C(-1), (M = W.current) == null || M.focus();
  }, []), q = fe(
    (M) => {
      let Y;
      if (c) {
        const ne = m.find((se) => se.value === M);
        if (ne)
          Y = [...j.current, ne];
        else
          return;
      } else {
        const ne = m.find((se) => se.value === M);
        if (ne)
          Y = [ne];
        else
          return;
      }
      j.current = Y, n(We, { value: Y.map((ne) => ne.value) }), c ? (x(""), C(-1)) : $();
    },
    [c, m, n, $]
  ), A = fe(
    (M) => {
      X.current = j.current.findIndex((ne) => ne.value === M);
      const Y = j.current.filter((ne) => ne.value !== M);
      j.current = Y, n(We, { value: Y.map((ne) => ne.value) });
    },
    [n]
  ), ee = fe(
    (M) => {
      M.stopPropagation(), n(We, { value: [] }), $();
    },
    [n, $]
  ), oe = fe((M) => {
    x(M.target.value);
  }, []), le = fe(
    (M) => {
      if (!E) {
        if (M.key === "ArrowDown" || M.key === "ArrowUp" || M.key === "Enter" || M.key === " ") {
          if (M.target.tagName === "BUTTON") return;
          M.preventDefault(), M.stopPropagation(), V();
        }
        return;
      }
      switch (M.key) {
        case "ArrowDown":
          M.preventDefault(), M.stopPropagation(), C(
            (Y) => Y < N.length - 1 ? Y + 1 : 0
          );
          break;
        case "ArrowUp":
          M.preventDefault(), M.stopPropagation(), C(
            (Y) => Y > 0 ? Y - 1 : N.length - 1
          );
          break;
        case "Enter":
          M.preventDefault(), M.stopPropagation(), L >= 0 && L < N.length && q(N[L].value);
          break;
        case "Escape":
          M.preventDefault(), M.stopPropagation(), $();
          break;
        case "Tab":
          $();
          break;
        case "Backspace":
          v === "" && c && a.length > 0 && A(a[a.length - 1].value);
          break;
      }
    },
    [
      E,
      V,
      $,
      N,
      L,
      q,
      v,
      c,
      a,
      A
    ]
  ), he = fe(
    async (M) => {
      M.preventDefault(), h(!1);
      try {
        await n("loadOptions");
      } catch {
        h(!0);
      }
    },
    [n]
  ), ge = fe(
    (M, Y) => {
      H(M), Y.dataTransfer.effectAllowed = "move", Y.dataTransfer.setData("text/plain", String(M));
    },
    []
  ), ve = fe(
    (M, Y) => {
      if (Y.preventDefault(), Y.dataTransfer.dropEffect = "move", S === null || S === M) {
        R(null), Q(null);
        return;
      }
      const ne = Y.currentTarget.getBoundingClientRect(), se = ne.left + ne.width / 2, Ie = Y.clientX < se ? "before" : "after";
      R(M), Q(Ie);
    },
    [S]
  ), ye = fe(
    (M) => {
      if (M.preventDefault(), S === null || B === null || F === null || S === B) return;
      const Y = [...j.current], [ne] = Y.splice(S, 1);
      let se = B;
      S < B ? se = F === "before" ? se - 1 : se : se = F === "before" ? se : se + 1, Y.splice(se, 0, ne), j.current = Y, n(We, { value: Y.map((Ie) => Ie.value) }), H(null), R(null), Q(null);
    },
    [S, B, F, n]
  ), ke = fe(() => {
    H(null), R(null), Q(null);
  }, []);
  if (Me(() => {
    if (L < 0 || !P.current) return;
    const M = P.current.querySelector(
      `[id="${l}-opt-${L}"]`
    );
    M && M.scrollIntoView({ block: "nearest" });
  }, [L, l]), !r)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, a.map((M) => /* @__PURE__ */ e.createElement("span", { key: M.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(ft, { image: M.image }), /* @__PURE__ */ e.createElement("span", null, M.label))));
  const xe = !i && a.length > 0 && !u, He = E ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: P,
      className: "tlDropdownSelect__dropdown",
      style: I,
      ...Gt
    },
    (o || k) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: O,
        type: "text",
        className: "tlDropdownSelect__search",
        value: v,
        onChange: oe,
        onKeyDown: le,
        placeholder: _["js.dropdownSelect.filterPlaceholder"],
        "aria-label": _["js.dropdownSelect.filterPlaceholder"],
        "aria-activedescendant": L >= 0 ? `${l}-opt-${L}` : void 0,
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
      !o && !k && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      k && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: he }, _["js.dropdownSelect.error"])),
      o && N.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, b),
      o && N.map((M, Y) => /* @__PURE__ */ e.createElement(
        ba,
        {
          key: M.value,
          id: `${l}-opt-${Y}`,
          option: M,
          highlighted: Y === L,
          searchTerm: v,
          onSelect: q,
          onMouseEnter: () => C(Y)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: W,
      className: "tlDropdownSelect" + (E ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": E,
      "aria-haspopup": "listbox",
      "aria-owns": E ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: E ? void 0 : V,
      onKeyDown: le
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, a.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : a.map((M, Y) => {
      let ne = "";
      return S === Y ? ne = "tlDropdownSelect__chip--dragging" : B === Y && F === "before" ? ne = "tlDropdownSelect__chip--dropBefore" : B === Y && F === "after" && (ne = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        ha,
        {
          key: M.value,
          option: M,
          removable: !u && (c || !i),
          onRemove: A,
          removeLabel: w(M.label),
          draggable: f,
          onDragStart: f ? (se) => ge(Y, se) : void 0,
          onDragOver: f ? (se) => ve(Y, se) : void 0,
          onDrop: f ? ye : void 0,
          onDragEnd: f ? ke : void 0,
          dragClassName: f ? ne : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, xe && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: ee,
        "aria-label": _["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, E ? "▲" : "▼"))
  ), He && $t.createPortal(He, document.body));
}, { useCallback: lt, useRef: ga } = e, Ut = "application/x-tl-color", va = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: a,
  onSwap: c,
  onReplace: s
}) => {
  const i = ga(null), u = lt(
    (m) => (p) => {
      i.current = m, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), r = lt((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), o = lt(
    (m) => (p) => {
      p.preventDefault();
      const f = p.dataTransfer.getData(Ut);
      f ? s(m, f) : i.current !== null && i.current !== m && c(i.current, m), i.current = null;
    },
    [c, s]
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
        onDoubleClick: m != null ? () => a(m) : void 0,
        onDragStart: m != null ? u(p) : void 0,
        onDragOver: r,
        onDrop: o(p)
      }
    ))
  );
};
function Ht(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function dt(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function Wt(l) {
  if (!dt(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function zt(l, t, n) {
  const a = (c) => Ht(c).toString(16).padStart(2, "0");
  return "#" + a(l) + a(t) + a(n);
}
function Ea(l, t, n) {
  const a = l / 255, c = t / 255, s = n / 255, i = Math.max(a, c, s), u = Math.min(a, c, s), r = i - u;
  let o = 0;
  r !== 0 && (i === a ? o = (c - s) / r % 6 : i === c ? o = (s - a) / r + 2 : o = (a - c) / r + 4, o *= 60, o < 0 && (o += 360));
  const m = i === 0 ? 0 : r / i;
  return [o, m, i];
}
function Ca(l, t, n) {
  const a = n * t, c = a * (1 - Math.abs(l / 60 % 2 - 1)), s = n - a;
  let i = 0, u = 0, r = 0;
  return l < 60 ? (i = a, u = c, r = 0) : l < 120 ? (i = c, u = a, r = 0) : l < 180 ? (i = 0, u = a, r = c) : l < 240 ? (i = 0, u = c, r = a) : l < 300 ? (i = c, u = 0, r = a) : (i = a, u = 0, r = c), [
    Math.round((i + s) * 255),
    Math.round((u + s) * 255),
    Math.round((r + s) * 255)
  ];
}
function wa(l) {
  return Ea(...Wt(l));
}
function at(l, t, n) {
  return zt(...Ca(l, t, n));
}
const { useCallback: je, useRef: xt } = e, ya = ({ color: l, onColorChange: t }) => {
  const [n, a, c] = wa(l), s = xt(null), i = xt(null), u = je(
    (b, w) => {
      var x;
      const E = (x = s.current) == null ? void 0 : x.getBoundingClientRect();
      if (!E) return;
      const g = Math.max(0, Math.min(1, (b - E.left) / E.width)), v = Math.max(0, Math.min(1, 1 - (w - E.top) / E.height));
      t(at(n, g, v));
    },
    [n, t]
  ), r = je(
    (b) => {
      b.preventDefault(), b.target.setPointerCapture(b.pointerId), u(b.clientX, b.clientY);
    },
    [u]
  ), o = je(
    (b) => {
      b.buttons !== 0 && u(b.clientX, b.clientY);
    },
    [u]
  ), m = je(
    (b) => {
      var v;
      const w = (v = i.current) == null ? void 0 : v.getBoundingClientRect();
      if (!w) return;
      const g = Math.max(0, Math.min(1, (b - w.top) / w.height)) * 360;
      t(at(g, a, c));
    },
    [a, c, t]
  ), p = je(
    (b) => {
      b.preventDefault(), b.target.setPointerCapture(b.pointerId), m(b.clientY);
    },
    [m]
  ), f = je(
    (b) => {
      b.buttons !== 0 && m(b.clientY);
    },
    [m]
  ), _ = at(n, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: s,
      className: "tlColorInput__svField",
      style: { backgroundColor: _ },
      onPointerDown: r,
      onPointerMove: o
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__svHandle",
        style: { left: `${a * 100}%`, top: `${(1 - c) * 100}%` }
      }
    )
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: i,
      className: "tlColorInput__hueSlider",
      onPointerDown: p,
      onPointerMove: f
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
function ka(l, t) {
  const n = t.toUpperCase();
  return l.some((a) => a != null && a.toUpperCase() === n);
}
const Sa = {
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
}, { useState: Ke, useCallback: Ee, useEffect: It, useRef: Na, useLayoutEffect: Ta } = e, Ra = ({
  anchorRef: l,
  currentColor: t,
  palette: n,
  paletteColumns: a,
  defaultPalette: c,
  canReset: s,
  onConfirm: i,
  onCancel: u,
  onPaletteChange: r
}) => {
  const [o, m] = Ke("palette"), [p, f] = Ke(t), _ = Na(null), b = ie(Sa), [w, E] = Ke(null);
  Ta(() => {
    if (!l.current || !_.current) return;
    const P = l.current.getBoundingClientRect(), j = _.current.getBoundingClientRect();
    let X = P.bottom + 4, d = P.left;
    X + j.height > window.innerHeight && (X = P.top - j.height - 4), d + j.width > window.innerWidth && (d = Math.max(0, P.right - j.width)), E({ top: X, left: d });
  }, [l]);
  const g = p != null, [v, x, L] = g ? Wt(p) : [0, 0, 0], [C, k] = Ke((p == null ? void 0 : p.toUpperCase()) ?? "");
  It(() => {
    k((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), Le(!0, { ESCAPE: u }), It(() => {
    const P = (X) => {
      _.current && !_.current.contains(X.target) && u();
    }, j = setTimeout(() => document.addEventListener("mousedown", P), 0);
    return () => {
      clearTimeout(j), document.removeEventListener("mousedown", P);
    };
  }, [u]);
  const h = Ee(
    (P) => (j) => {
      const X = parseInt(j.target.value, 10);
      if (isNaN(X)) return;
      const d = Ht(X);
      f(zt(P === "r" ? d : v, P === "g" ? d : x, P === "b" ? d : L));
    },
    [v, x, L]
  ), I = Ee(
    (P) => {
      if (p != null) {
        P.dataTransfer.setData(Ut, p.toUpperCase()), P.dataTransfer.effectAllowed = "move";
        const j = document.createElement("div");
        j.style.width = "33px", j.style.height = "33px", j.style.backgroundColor = p, j.style.borderRadius = "3px", j.style.border = "1px solid rgba(0,0,0,0.1)", j.style.position = "absolute", j.style.top = "-9999px", document.body.appendChild(j), P.dataTransfer.setDragImage(j, 16, 16), requestAnimationFrame(() => document.body.removeChild(j));
      }
    },
    [p]
  ), T = Ee((P) => {
    const j = P.target.value;
    k(j), dt(j) && f(j);
  }, []), S = Ee(() => {
    f(null);
  }, []), H = Ee((P) => {
    f(P);
  }, []), B = Ee(
    (P) => {
      i(P);
    },
    [i]
  ), R = Ee(
    (P, j) => {
      const X = [...n], d = X[P];
      X[P] = X[j], X[j] = d, r(X);
    },
    [n, r]
  ), F = Ee(
    (P, j) => {
      const X = [...n];
      X[P] = j, r(X);
    },
    [n, r]
  ), Q = Ee(() => {
    r([...c]);
  }, [c, r]), W = Ee(
    (P) => {
      if (ka(n, P)) return;
      const j = n.indexOf(null);
      if (j < 0) return;
      const X = [...n];
      X[j] = P.toUpperCase(), r(X);
    },
    [n, r]
  ), O = Ee(() => {
    p != null && W(p), i(p);
  }, [p, i, W]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: _,
      style: w ? { top: w.top, left: w.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (o === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("palette")
      },
      b["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (o === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("mixer")
      },
      b["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, o === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      va,
      {
        colors: n,
        columns: a,
        onSelect: H,
        onConfirm: B,
        onSwap: R,
        onReplace: F
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: Q }, b["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(ya, { color: p ?? "#000000", onColorChange: f }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (g ? "" : " tlColorInput--noColor"),
        style: g ? { backgroundColor: p } : void 0,
        draggable: g,
        onDragStart: g ? I : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: g ? v : "",
        onChange: h("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: g ? x : "",
        onChange: h("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: g ? L : "",
        onChange: h("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (C !== "" && !dt(C) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: C,
        onChange: T
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, s && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: S }, b["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, b["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: O }, b["js.colorInput.ok"]))
  );
}, Da = { "js.colorInput.chooseColor": "Choose color" }, { useState: La, useCallback: Ye, useRef: xa } = e, Ia = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), c = ae(), s = ie(Da), [i, u] = La(!1), r = xa(null), o = n, m = t.editable !== !1, p = t.palette ?? [], f = t.paletteColumns ?? 6, _ = t.defaultPalette ?? p, b = Ye(() => {
    m && u(!0);
  }, [m]), w = Ye(
    (v) => {
      u(!1), a(v);
    },
    [a]
  ), E = Ye(() => {
    u(!1);
  }, []), g = Ye(
    (v) => {
      c("paletteChanged", { palette: v });
    },
    [c]
  );
  return m ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: r,
      className: "tlColorInput__swatch" + (o == null ? " tlColorInput__swatch--noColor" : ""),
      style: o != null ? { backgroundColor: o } : void 0,
      onClick: b,
      disabled: t.disabled === !0,
      title: o ?? "",
      "aria-label": s["js.colorInput.chooseColor"]
    }
  ), i && /* @__PURE__ */ e.createElement(
    Ra,
    {
      anchorRef: r,
      currentColor: o,
      palette: p,
      paletteColumns: f,
      defaultPalette: _,
      canReset: t.canReset !== !1,
      onConfirm: w,
      onCancel: E,
      onPaletteChange: g
    }
  )) : /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlColorInput tlColorInput--immutable" + (o == null ? " tlColorInput--noColor" : ""),
      style: o != null ? { backgroundColor: o } : void 0,
      title: o ?? ""
    }
  );
}, { useState: Ue, useCallback: De, useEffect: rt, useRef: Pt, useLayoutEffect: Pa, useMemo: Ma } = e, ja = {
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
}, Aa = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: a,
  onSelect: c,
  onCancel: s,
  onLoadIcons: i
}) => {
  const u = ie(ja), [r, o] = Ue("simple"), [m, p] = Ue(""), [f, _] = Ue(t ?? ""), [b, w] = Ue(!1), [E, g] = Ue(null), v = Pt(null), x = Pt(null);
  Pa(() => {
    if (!l.current || !v.current) return;
    const B = l.current.getBoundingClientRect(), R = v.current.getBoundingClientRect();
    let F = B.bottom + 4, Q = B.left;
    F + R.height > window.innerHeight && (F = B.top - R.height - 4), Q + R.width > window.innerWidth && (Q = Math.max(0, B.right - R.width)), g({ top: F, left: Q });
  }, [l]), rt(() => {
    !a && !b && i().catch(() => w(!0));
  }, [a, b, i]), rt(() => {
    a && x.current && x.current.focus();
  }, [a]), Le(!0, { ESCAPE: s }), rt(() => {
    const B = (F) => {
      v.current && !v.current.contains(F.target) && s();
    }, R = setTimeout(() => document.addEventListener("mousedown", B), 0);
    return () => {
      clearTimeout(R), document.removeEventListener("mousedown", B);
    };
  }, [s]);
  const L = Ma(() => {
    if (!m) return n;
    const B = m.toLowerCase();
    return n.filter(
      (R) => R.prefix.toLowerCase().includes(B) || R.label.toLowerCase().includes(B) || R.terms != null && R.terms.some((F) => F.includes(B))
    );
  }, [n, m]), C = De((B) => {
    p(B.target.value);
  }, []), k = De(
    (B) => {
      c(B);
    },
    [c]
  ), h = De((B) => {
    _(B);
  }, []), I = De((B) => {
    _(B.target.value);
  }, []), T = De(() => {
    c(f || null);
  }, [f, c]), S = De(() => {
    c(null);
  }, [c]), H = De(async (B) => {
    B.preventDefault(), w(!1);
    try {
      await i();
    } catch {
      w(!0);
    }
  }, [i]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlIconSelect__popup",
      ref: v,
      style: E ? { top: E.top, left: E.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (r === "simple" ? " tlIconSelect__tab--active" : ""),
        onClick: () => o("simple")
      },
      u["js.iconSelect.simpleTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (r === "advanced" ? " tlIconSelect__tab--active" : ""),
        onClick: () => o("advanced")
      },
      u["js.iconSelect.advancedTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__searchIcon", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-magnifying-glass" })), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: x,
        type: "text",
        className: "tlIconSelect__search",
        value: m,
        onChange: C,
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
      !a && !b && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      b && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: H }, u["js.iconSelect.loadError"])),
      a && L.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      a && L.map(
        (B) => B.variants.map((R) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: R.encoded,
            className: "tlIconSelect__iconCell" + (R.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": R.encoded === t,
            tabIndex: 0,
            title: B.label,
            onClick: () => r === "simple" ? k(R.encoded) : h(R.encoded),
            onKeyDown: (F) => {
              (F.key === "Enter" || F.key === " ") && (F.preventDefault(), r === "simple" ? k(R.encoded) : h(R.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(we, { encoded: R.encoded })
        ))
      )
    ),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: f,
        onChange: I
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, f && /* @__PURE__ */ e.createElement(we, { encoded: f })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, f ? f.startsWith("css:") ? f.substring(4) : f : ""))),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: s }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: S }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: T }, u["js.iconSelect.ok"]))
  );
}, Ba = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: Fa, useCallback: Ge, useRef: Oa } = e, $a = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), c = ae(), s = ie(Ba), [i, u] = Fa(!1), r = Oa(null), o = n, m = t.editable !== !1, p = t.disabled === !0, f = t.icons ?? [], _ = t.iconsLoaded === !0, b = Ge(() => {
    m && !p && u(!0);
  }, [m, p]), w = Ge(
    (v) => {
      u(!1), a(v);
    },
    [a]
  ), E = Ge(() => {
    u(!1);
  }, []), g = Ge(async () => {
    await c("loadIcons");
  }, [c]);
  return m ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: r,
      className: "tlIconSelect__swatch" + (o == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: b,
      disabled: p,
      title: o ?? "",
      "aria-label": s["js.iconSelect.chooseIcon"]
    },
    o ? /* @__PURE__ */ e.createElement(we, { encoded: o }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), i && /* @__PURE__ */ e.createElement(
    Aa,
    {
      anchorRef: r,
      currentValue: o,
      icons: f,
      iconsLoaded: _,
      onSelect: w,
      onCancel: E,
      onLoadIcons: g
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, o ? /* @__PURE__ */ e.createElement(we, { encoded: o }) : null));
}, { useCallback: Ae, useEffect: Ua, useMemo: Mt, useRef: Ha, useState: ot } = e, Wa = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, za = [1, 2, 3, 4];
function Va(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const a = parseFloat(n[1]), c = n[2] || "px";
  return c === "rem" || c === "em" ? a * t : a;
}
function Ka(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let a = 1;
  for (const c of za)
    n >= c && (a = c);
  return a;
}
function Ya(l, t) {
  const n = Wa[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function Ga(l, t) {
  const n = Math.max(1, t), a = {}, c = (p, f) => !!(a[p] && a[p][f]), s = (p, f) => {
    a[p] || (a[p] = {}), a[p][f] = !0;
  }, i = [];
  let u = 0, r = 0;
  const o = (p) => {
    let f = null;
    for (const b of i) b.rowStart === p && (f = b);
    if (!f) return;
    let _ = f.colEnd;
    for (; _ < n && !c(p, _); ) _++;
    if (_ !== f.colEnd) {
      for (let b = f.rowStart; b < f.rowEnd; b++)
        for (let w = f.colEnd; w < _; w++) s(b, w);
      f.colEnd = _;
    }
  };
  for (const p of l) {
    const f = n <= 1 ? 1 : Math.max(1, p.rowSpan || 1);
    let _ = Math.min(Ya(p.width, n), n);
    for (; c(u, r); )
      r++, r >= n && (r = 0, u++);
    let b = 0;
    for (let x = r; x < n && !c(u, x); x++)
      b++;
    if (_ > b) {
      for (o(u), r = 0, u++; c(u, r); )
        r++, r >= n && (r = 0, u++);
      b = 0;
      for (let x = r; x < n && !c(u, x); x++)
        b++;
      _ = Math.min(_, b);
    }
    const w = r, E = r + _, g = u, v = u + f;
    i.push({ id: p.id, colStart: w, colEnd: E, rowStart: g, rowEnd: v });
    for (let x = g; x < v; x++)
      for (let L = w; L < E; L++) s(x, L);
    r = E, r >= n && (r = 0, u++);
  }
  o(u);
  let m = 0;
  for (const p of i) p.rowEnd > m && (m = p.rowEnd);
  for (let p = 1; p < m; p++)
    for (let f = 0; f < n; f++) {
      if (c(p, f)) continue;
      const _ = i.find((b) => b.rowEnd === p && b.colStart <= f && f < b.colEnd);
      if (_) {
        _.rowEnd = p + 1;
        for (let b = _.colStart; b < _.colEnd; b++) s(p, b);
      }
    }
  return i;
}
const Xa = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.minColWidth ?? "16rem", c = (t.children ?? []).filter((k) => k && k.id), s = Ha(null), [i, u] = ot(1), r = t.editMode === !0;
  Ua(() => {
    const k = s.current;
    if (!k) return;
    const h = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, I = Va(a, h), T = () => u(Ka(k.clientWidth, I));
    T();
    const S = new ResizeObserver(T);
    return S.observe(k), () => S.disconnect();
  }, [a]);
  const o = Mt(() => Ga(c, i), [c, i]), m = Mt(() => {
    const k = {};
    for (const h of o) k[h.id] = h;
    return k;
  }, [o]), [p, f] = ot(null), [_, b] = ot(null), w = Ae((k, h) => {
    if (!r) {
      k.preventDefault();
      return;
    }
    f(h), k.dataTransfer.effectAllowed = "move", k.dataTransfer.setData("text/plain", h);
  }, [r]), E = Ae((k, h) => {
    if (!r || !p || p === h) return;
    k.preventDefault(), k.dataTransfer.dropEffect = "move";
    const I = k.currentTarget.getBoundingClientRect(), T = k.clientX < I.left + I.width / 2;
    b((S) => S && S.id === h && S.before === T ? S : { id: h, before: T });
  }, [r, p]), g = Ae(() => {
  }, []), v = Ae((k, h, I) => {
    const T = c.map((R) => R.id), S = T.indexOf(k);
    if (S < 0) return;
    T.splice(S, 1);
    const H = T.indexOf(h);
    if (H < 0) {
      T.splice(S, 0, k);
      return;
    }
    const B = I ? H : H + 1;
    T.splice(B, 0, k), n("reorder", { order: T });
  }, [c, n]), x = Ae((k, h) => {
    if (!r || !p || p === h) return;
    k.preventDefault();
    const I = k.currentTarget.getBoundingClientRect(), T = k.clientX < I.left + I.width / 2;
    v(p, h, T), f(null), b(null);
  }, [r, p, v]), L = Ae(() => {
    f(null), b(null);
  }, []), C = {
    display: "grid",
    gridTemplateColumns: `repeat(${i}, 1fr)`,
    gap: "1rem"
  };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: s,
      className: "tlDashboard" + (r ? " tlDashboard--edit" : "")
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: C }, c.map((k) => {
      const h = m[k.id];
      if (!h) return null;
      const I = {
        gridColumn: `${h.colStart + 1} / ${h.colEnd + 1}`,
        gridRow: `${h.rowStart + 1} / ${h.rowEnd + 1}`
      }, T = ["tlDashboard__tile"];
      return p === k.id && T.push("tlDashboard__tile--dragging"), _ && _.id === k.id && T.push(_.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: k.id,
          className: T.join(" "),
          style: I,
          draggable: r,
          onDragStart: (S) => w(S, k.id),
          onDragOver: (S) => E(S, k.id),
          onDragLeave: g,
          onDrop: (S) => x(S, k.id),
          onDragEnd: L
        },
        /* @__PURE__ */ e.createElement(K, { control: k.control }),
        r && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: qa, useRef: jt, useState: At, useEffect: Za, useLayoutEffect: Qa } = e, Ja = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, a) => /* @__PURE__ */ e.createElement("span", { key: a, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(K, { control: n }))));
}, er = ({ group: l }) => {
  var p, f;
  const [t, n] = At(!1), [a, c] = At({}), s = jt(null), i = jt(null), u = qa(() => {
    n((_) => !_);
  }, []);
  Qa(() => {
    if (!t) return;
    const _ = () => {
      const b = s.current;
      if (!b) return;
      const w = b.getBoundingClientRect();
      c({
        position: "fixed",
        top: w.bottom + 4,
        right: Math.max(8, window.innerWidth - w.right),
        left: "auto"
      });
    };
    return _(), window.addEventListener("resize", _), window.addEventListener("scroll", _, !0), () => {
      window.removeEventListener("resize", _), window.removeEventListener("scroll", _, !0);
    };
  }, [t]), Za(() => {
    if (!t) return;
    const _ = (b) => {
      i.current && !i.current.contains(b.target) && s.current && !s.current.contains(b.target) && n(!1);
    };
    return document.addEventListener("mousedown", _), () => document.removeEventListener("mousedown", _);
  }, [t]), Le(t, { ESCAPE: () => n(!1) }), pt(t, i, "first");
  const r = l.items.filter((_) => _ != null);
  if (r.length === 0) return null;
  if (r.length === 1 && !((p = l.subGroups) != null && p.length) && !l.icon)
    return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(K, { control: r[0] })));
  const o = l.label ?? l.name, m = !!l.icon;
  return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--menu" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: s,
      type: "button",
      className: "tlToolbar__menuTrigger" + (m ? " tlToolbar__menuTrigger--icon" : ""),
      onMouseDown: (_) => _.preventDefault(),
      onClick: u,
      "aria-expanded": t,
      "aria-haspopup": "true",
      "aria-label": m ? o : void 0,
      title: m ? o : void 0
    },
    m ? /* @__PURE__ */ e.createElement(we, { encoded: l.icon, className: "tlToolbar__menuIcon" }) : /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement("span", null, o), /* @__PURE__ */ e.createElement("svg", { className: "tlToolbar__chevron", viewBox: "0 0 24 24", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("polyline", { points: "6,9 12,15 18,9" })))
  ), $t.createPortal(
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: i,
        className: "tlToolbar__dropdown",
        role: "menu",
        hidden: !t,
        style: t ? a : void 0,
        onClick: () => n(!1)
      },
      r.map((_, b) => /* @__PURE__ */ e.createElement("div", { key: b, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(K, { control: _ }))),
      (f = l.subGroups) == null ? void 0 : f.map((_, b) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${b}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), _.items.map((w, E) => /* @__PURE__ */ e.createElement("div", { key: E, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(K, { control: w })))))
    ),
    document.body
  ));
}, tr = ({ controlId: l }) => {
  const a = (G().groups ?? []).filter((c) => c.items.some((s) => s != null));
  return a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, a.map((c, s) => /* @__PURE__ */ e.createElement(e.Fragment, { key: c.name }, s > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), c.display === "menu" ? /* @__PURE__ */ e.createElement(er, { group: c }) : /* @__PURE__ */ e.createElement(Ja, { group: c }))));
}, nr = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlTileStack", style: { width: "100%", height: "100%" } }, t.frame && /* @__PURE__ */ e.createElement(K, { control: t.frame }));
}, lr = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.content, c = t.breadcrumb ?? null;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAdaptiveDetail" }, c && c.length > 0 && /* @__PURE__ */ e.createElement("nav", { className: "tlAdaptiveDetail__breadcrumb", "aria-label": "Breadcrumb" }, c.map((s, i) => {
    const u = i === c.length - 1;
    return /* @__PURE__ */ e.createElement(e.Fragment, { key: s.depth }, i > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__sep" }, "›"), u ? /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__crumb tlAdaptiveDetail__crumb--current" }, s.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlAdaptiveDetail__crumb",
        onClick: () => n("navigate", { depth: s.depth })
      },
      s.label
    ));
  })), /* @__PURE__ */ e.createElement("div", { className: "tlAdaptiveDetail__content" }, a && /* @__PURE__ */ e.createElement(K, { control: a })));
}, ar = ({ controlId: l }) => {
  const n = G().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((a, c) => /* @__PURE__ */ e.createElement(K, { key: c, control: a })));
}, rr = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), or = {
  "js.sidebar.openDrawer": "Open navigation"
}, sr = ({ controlId: l }) => {
  const t = ae(), n = ie(or);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      id: l,
      type: "button",
      className: "tlDrawerToggle",
      "aria-label": n["js.sidebar.openDrawer"],
      onClick: () => t("toggle", {})
    },
    /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 16 16", width: "20", height: "20", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement(
      "path",
      {
        d: "M2 4h12M2 8h12M2 12h12",
        fill: "none",
        stroke: "currentColor",
        strokeWidth: "2",
        strokeLinecap: "round"
      }
    ))
  );
};
U("TLButton", un);
U("TLUploadButton", dn);
U("TLToggleButton", pn);
U("TLTextInput", Zt);
U("TLPasswordInput", Jt);
U("TLNumberInput", tn);
U("TLDatePicker", ln);
U("TLSelect", rn);
U("TLCheckbox", sn);
U("TLCounter", fn);
U("TLTabBar", bn);
U("TLFieldList", _n);
U("TLAudioRecorder", vn);
U("TLAudioPlayer", Cn);
U("TLFileUpload", yn);
U("TLBinaryField", Sn);
U("TLFileChips", Rn);
U("TLRelativeTime", xn);
U("TLAnchor", In);
U("TLScrollLink", Pn);
U("TLAvatar", An);
U("TLDownload", Fn);
U("TLPhotoCapture", $n);
U("TLPhotoViewer", Hn);
U("TLPdfViewer", zn);
U("TLSplitPanel", Vn);
U("TLPanel", Qn);
U("TLInset", ul);
U("TLMaximizeRoot", Jn);
U("TLDeckPane", el);
U("TLSidebar", cl);
U("TLStack", il);
U("TLGrid", dl);
U("TLCard", ml);
U("TLAppBar", pl);
U("TLBreadcrumb", hl);
U("TLBottomBar", _l);
U("TLDialog", El);
U("TLDialogManager", yl);
U("TLWindow", Tl);
U("TLDrawer", Ll);
U("TLContextMenuRegion", Il);
U("TLSnackbar", Al);
U("TLMenu", Fl);
U("TLAppShell", $l);
U("TLText", Ul);
U("TLTableView", Vl);
U("TLFormLayout", ea);
U("TLFormGroup", la);
U("TLFormField", sa);
U("TLResourceCell", ca);
U("TLTreeView", ua);
U("TLDropdownSelect", _a);
U("TLColorInput", Ia);
U("TLIconSelect", $a);
U("TLDashboard", Xa);
U("TLToolbar", tr);
U("TLTileStack", nr);
U("TLAdaptiveDetail", lr);
U("TLSlot", ar);
U("TLSlotContent", rr);
U("TLDrawerToggle", sr);
