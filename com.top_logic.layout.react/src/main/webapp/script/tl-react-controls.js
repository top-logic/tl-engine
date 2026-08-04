import { React as e, useTLFieldValue as Re, useTLCommand as ae, useTLState as G, useKeyboardBinding as de, useTLUpload as Oe, TLChild as K, useI18N as ie, useTLDataUrl as Fe, scrollToAnchor as Zt, useStandaloneKeyboardScope as xe, KeyboardScopeProvider as mt, useFocusTrap as pt, CMD_VALUE_CHANGED as We, anchoredOverlayProps as Qt, register as U } from "tl-react-bridge";
const { useCallback: gt, useRef: Jt } = e, en = 300, tn = ({ controlId: l, state: t }) => {
  const [n, a, c] = Re({ debounceMs: en }), s = ae(), i = Jt(!1), u = gt(
    (E) => {
      i.current = !0, a(E.target.value);
    },
    [a]
  ), r = t.commitOnBlur === !0, o = gt(async () => {
    await c(), r && i.current && (i.current = !1, s("commit"));
  }, [c, r, s]), m = t.multiline === !0;
  if (t.editable === !1) {
    const E = "tlReactTextInput tlReactTextInput--immutable" + (m ? " tlReactTextInput--multiline" : "");
    return /* @__PURE__ */ e.createElement(
      "span",
      {
        id: l,
        className: E,
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
}, { useCallback: vt } = e, nn = 300, ln = ({ controlId: l, state: t }) => {
  const [n, a, c] = Re({ debounceMs: nn }), s = vt(
    (p) => {
      a(p.target.value);
    },
    [a]
  ), i = vt(() => {
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
}, { useCallback: Et } = e, an = 300, rn = ({ controlId: l, state: t, config: n }) => {
  const [a, c, s] = Re({ debounceMs: an }), i = Et(
    (f) => {
      const _ = f.target.value;
      c(_ === "" ? null : _);
    },
    [c]
  ), u = Et(() => {
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
}, { useCallback: on } = e, sn = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), c = on(
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
}, { useCallback: cn } = e, un = ({ controlId: l, state: t, config: n }) => {
  var m;
  const [a, c] = Re(), s = cn(
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
}, { useCallback: dn } = e, mn = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), c = dn(
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
const { useCallback: pn } = e, fn = ({ controlId: l, command: t, label: n, image: a, disabled: c, displayMode: s }) => {
  const i = G(), u = ae(), r = t ?? "click", o = n ?? i.label, m = a ?? i.image, p = c ?? i.disabled === !0, f = s ?? i.displayMode ?? "label-only", _ = i.hidden === !0, b = i.tooltip, E = i.appearance, v = i.size, g = i.navigateUrl, C = pn(() => {
    if (g) {
      window.location.assign(g);
      return;
    }
    u(r);
  }, [u, r, g]), x = i.keyGesture;
  de(x, () => p || _ ? !1 : (C(), !0));
  const S = f === "icon-only", w = f === "label-only" || f === "icon-label" || S && !m, k = b ?? (S ? o : void 0), h = k ? `text:${k}` : void 0;
  return _ ? null : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: C,
      disabled: p,
      className: "tlReactButton" + (S ? " tlReactButton--iconOnly" : "") + (f === "label-only" ? " tlReactButton--labelOnly" : "") + (E === "link" ? " tlReactButton--link" : "") + (E === "primary" ? " tlReactButton--primary" : "") + (v === "small" ? " tlReactButton--small" : "") + (v === "large" ? " tlReactButton--large" : ""),
      "data-tooltip": h,
      "aria-label": m || S ? o : void 0
    },
    m && /* @__PURE__ */ e.createElement(we, { encoded: m, className: "tlReactButton__image" }),
    w && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, o)
  );
}, hn = ({ controlId: l }) => {
  const t = G(), n = Oe(), a = e.useRef(null), [c, s] = e.useState(!1), i = t.label ?? "", u = t.image, r = t.disabled === !0, o = t.hidden === !0, m = t.displayMode ?? "label-only", p = t.appearance, f = t.accept, _ = t.multiple === !0, b = e.useCallback(() => {
    var S;
    r || c || (S = a.current) == null || S.click();
  }, [r, c]), E = e.useCallback(async (S) => {
    const w = S.target.files;
    if (!w || w.length === 0) return;
    const k = new FormData();
    for (let h = 0; h < w.length; h++)
      k.append("file", w[h], w[h].name);
    S.target.value = "", s(!0);
    try {
      await n(k);
    } finally {
      s(!1);
    }
  }, [n]), v = m === "icon-only", g = m === "icon-only" || m === "icon-label", C = m === "label-only" || m === "icon-label" || v && !u, x = r || c;
  return /* @__PURE__ */ e.createElement("span", { id: l, style: { display: "contents" } }, /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: a,
      type: "file",
      accept: f && f !== "*" ? f : void 0,
      multiple: _ || void 0,
      onChange: E,
      style: { display: "none" }
    }
  ), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: b,
      disabled: x,
      style: o ? { display: "none" } : void 0,
      className: "tlReactButton" + (v ? " tlReactButton--iconOnly" : "") + (p === "link" ? " tlReactButton--link" : "") + (p === "primary" ? " tlReactButton--primary" : ""),
      "aria-label": v ? i : void 0
    },
    g && u && /* @__PURE__ */ e.createElement(we, { encoded: u, className: "tlReactButton__image" }),
    C && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, i)
  ));
}, { useCallback: bn } = e, _n = ({ controlId: l, command: t, label: n, active: a, disabled: c }) => {
  const s = G(), i = ae(), u = t ?? "click", r = n ?? s.label, o = a ?? s.active === !0, m = c ?? s.disabled === !0, p = bn(() => {
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
}, gn = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.count ?? 0, c = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: vn } = e, En = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.tabs ?? [], c = t.activeTabId, s = vn((i) => {
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
}, Cn = ({ controlId: l }) => {
  const t = G(), n = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((c, s) => /* @__PURE__ */ e.createElement("div", { key: s, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(K, { control: c })))));
}, wn = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, yn = ({ controlId: l }) => {
  const t = G(), n = Oe(), [a, c] = e.useState("idle"), [s, i] = e.useState(null), u = e.useRef(null), r = e.useRef([]), o = e.useRef(null), m = t.status ?? "idle", p = t.error, f = m === "received" ? "idle" : a !== "idle" ? a : m, _ = e.useCallback(async () => {
    if (a === "recording") {
      const C = u.current;
      C && C.state !== "inactive" && C.stop();
      return;
    }
    if (a !== "uploading") {
      if (i(null), !window.isSecureContext || !navigator.mediaDevices) {
        i("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const C = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        o.current = C, r.current = [];
        const x = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", S = new MediaRecorder(C, x ? { mimeType: x } : void 0);
        u.current = S, S.ondataavailable = (w) => {
          w.data.size > 0 && r.current.push(w.data);
        }, S.onstop = async () => {
          C.getTracks().forEach((h) => h.stop()), o.current = null;
          const w = new Blob(r.current, { type: S.mimeType || "audio/webm" });
          if (r.current = [], w.size === 0) {
            c("idle");
            return;
          }
          c("uploading");
          const k = new FormData();
          k.append("audio", w, "recording.webm"), await n(k), c("idle");
        }, S.start(), c("recording");
      } catch (C) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", C), i("js.audioRecorder.error.denied"), c("idle");
      }
    }
  }, [a, n]), b = ie(wn), E = f === "recording" ? b["js.audioRecorder.stop"] : f === "uploading" ? b["js.uploading"] : b["js.audioRecorder.record"], v = f === "uploading", g = ["tlAudioRecorder__button"];
  return f === "recording" && g.push("tlAudioRecorder__button--recording"), f === "uploading" && g.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: g.join(" "),
      onClick: _,
      disabled: v,
      title: E,
      "aria-label": E
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${f === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, b[s]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, kn = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Sn = ({ controlId: l }) => {
  const t = G(), n = Fe(), a = !!t.hasAudio, c = t.dataRevision ?? 0, [s, i] = e.useState(a ? "idle" : "disabled"), u = e.useRef(null), r = e.useRef(null), o = e.useRef(c);
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
        const v = await fetch(n);
        if (!v.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", v.status), i("idle");
          return;
        }
        const g = await v.blob();
        r.current = URL.createObjectURL(g);
      } catch (v) {
        console.error("[TLAudioPlayer] Fetch error:", v), i("idle");
        return;
      }
    }
    const E = new Audio(r.current);
    u.current = E, E.onended = () => {
      i("idle");
    }, E.play(), i("playing");
  }, [s, n]), p = ie(kn), f = s === "loading" ? p["js.loading"] : s === "playing" ? p["js.audioPlayer.pause"] : s === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], _ = s === "disabled" || s === "loading", b = ["tlAudioPlayer__button"];
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
}, Nn = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Tn = ({ controlId: l }) => {
  const t = G(), n = Oe(), [a, c] = e.useState("idle"), [s, i] = e.useState(!1), u = e.useRef(null), r = t.status ?? "idle", o = t.error, m = t.accept ?? "", p = r === "received" ? "idle" : a !== "idle" ? a : r, f = e.useCallback(async (w) => {
    c("uploading");
    const k = new FormData();
    k.append("file", w, w.name), await n(k), c("idle");
  }, [n]), _ = e.useCallback((w) => {
    var h;
    const k = (h = w.target.files) == null ? void 0 : h[0];
    k && f(k);
  }, [f]), b = e.useCallback(() => {
    var w;
    a !== "uploading" && ((w = u.current) == null || w.click());
  }, [a]), E = e.useCallback((w) => {
    w.preventDefault(), w.stopPropagation(), i(!0);
  }, []), v = e.useCallback((w) => {
    w.preventDefault(), w.stopPropagation(), i(!1);
  }, []), g = e.useCallback((w) => {
    var h;
    if (w.preventDefault(), w.stopPropagation(), i(!1), a === "uploading") return;
    const k = (h = w.dataTransfer.files) == null ? void 0 : h[0];
    k && f(k);
  }, [a, f]), C = p === "uploading", x = ie(Nn), S = p === "uploading" ? x["js.uploading"] : x["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${s ? " tlFileUpload--dragover" : ""}`,
      onDragOver: E,
      onDragLeave: v,
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
        disabled: C,
        title: S,
        "aria-label": S
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    o && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, o)
  );
}, Rn = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, Dn = ({ controlId: l, state: t }) => {
  const a = G() ?? t ?? {}, c = Oe(), s = Fe(), i = ie(Rn), u = a.editable !== !1, r = !!a.hasData, o = a.fileName ?? "download", m = a.dataRevision ?? 0, p = a.accept ?? "", f = a.status ?? "idle", _ = a.error ?? null, [b, E] = e.useState("idle"), [v, g] = e.useState(!1), [C, x] = e.useState(!1), S = e.useRef(null), w = e.useCallback(async () => {
    if (!(!r || C)) {
      x(!0);
      try {
        const $ = s + (s.includes("?") ? "&" : "?") + "rev=" + m, A = await fetch($);
        if (!A.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", A.status);
          return;
        }
        const P = await A.blob(), X = URL.createObjectURL(P), d = document.createElement("a");
        d.href = X, d.download = o, d.style.display = "none", document.body.appendChild(d), d.click(), document.body.removeChild(d), URL.revokeObjectURL(X);
      } catch ($) {
        console.error("[TLBinaryField] Fetch error:", $);
      } finally {
        x(!1);
      }
    }
  }, [r, C, s, m, o]), k = e.useCallback(async ($) => {
    E("uploading");
    const A = new FormData();
    A.append("file", $, $.name), await c(A), E("idle");
  }, [c]), h = (f === "received" ? "idle" : b !== "idle" ? b : f) === "uploading", I = e.useCallback(($) => {
    var P;
    const A = (P = $.target.files) == null ? void 0 : P[0];
    A && k(A);
  }, [k]), T = e.useCallback(() => {
    var $;
    h || ($ = S.current) == null || $.click();
  }, [h]), R = e.useCallback(($) => {
    $.preventDefault(), $.stopPropagation(), g(!0);
  }, []), F = e.useCallback(($) => {
    $.preventDefault(), $.stopPropagation(), g(!1);
  }, []), O = e.useCallback(($) => {
    var P;
    if ($.preventDefault(), $.stopPropagation(), g(!1), h) return;
    const A = (P = $.dataTransfer.files) == null ? void 0 : P[0];
    A && k(A);
  }, [h, k]), D = C ? i["js.downloading"] : i["js.download.file"].replace("{0}", o), B = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (C ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: w,
      disabled: C,
      title: D,
      "aria-label": D
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o));
  if (!u)
    return r ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, B) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, i["js.download.noFile"]));
  const Q = h, H = h ? i["js.uploading"] : i["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlBinaryField tlFileUpload${v ? " tlFileUpload--dragover" : ""}`,
      onDragOver: R,
      onDragLeave: F,
      onDrop: O
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: S,
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
        title: H,
        "aria-label": H
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    r && B,
    _ && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, _)
  );
}, Ln = {
  "js.fileChips.add": "Add file",
  "js.fileChips.remove": "Remove {0}",
  "js.uploading": "Uploading…",
  "js.download.file": "Download {0}"
};
function xn(l) {
  return l < 1024 ? l + " B" : l < 1024 * 1024 ? (l / 1024).toFixed(l < 10 * 1024 ? 1 : 0) + " KB" : (l / (1024 * 1024)).toFixed(1) + " MB";
}
const In = ({ controlId: l }) => {
  const t = G(), n = ae(), a = Oe(), c = Fe(), s = ie(Ln), i = t.chips ?? [], u = t.editable === !0, [r, o] = e.useState(!1), [m, p] = e.useState(!1), f = e.useRef(null), _ = e.useCallback(async (w) => {
    const k = Array.from(w);
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
  }, [a]), b = e.useCallback(async (w) => {
    if (w.hasData)
      try {
        const k = c + "&key=" + encodeURIComponent(w.key), h = await fetch(k);
        if (!h.ok) {
          console.error("[TLFileChips] Failed to fetch data:", h.status);
          return;
        }
        const I = await h.blob(), T = URL.createObjectURL(I), R = document.createElement("a");
        R.href = T, R.download = w.name, R.style.display = "none", document.body.appendChild(R), R.click(), document.body.removeChild(R), URL.revokeObjectURL(T);
      } catch (k) {
        console.error("[TLFileChips] Fetch error:", k);
      }
  }, [c]), E = e.useCallback((w) => {
    w.target.files && _(w.target.files), w.target.value = "";
  }, [_]), v = e.useCallback(() => {
    var w;
    r || (w = f.current) == null || w.click();
  }, [r]), g = e.useCallback((w) => {
    u && (w.preventDefault(), w.stopPropagation(), p(!0));
  }, [u]), C = e.useCallback((w) => {
    u && (w.preventDefault(), w.stopPropagation(), p(!1));
  }, [u]), x = e.useCallback((w) => {
    u && (w.preventDefault(), w.stopPropagation(), p(!1), !r && w.dataTransfer.files && _(w.dataTransfer.files));
  }, [u, r, _]), S = [
    "tlFileChips",
    u ? "tlFileChips--editable" : "",
    m ? "tlFileChips--dragover" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: S,
      onDragOver: g,
      onDragLeave: C,
      onDrop: x
    },
    i.map((w) => {
      const k = s["js.download.file"].replace("{0}", w.name), h = s["js.fileChips.remove"].replace("{0}", w.name);
      return /* @__PURE__ */ e.createElement("span", { key: w.key, className: "tlFileChip" }, /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__main",
          onClick: () => b(w),
          disabled: !w.hasData,
          title: w.hasData ? k : w.name
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
        /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__name" }, w.name),
        w.size != null && /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__size" }, xn(w.size))
      ), u && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__remove",
          onClick: () => n("removeChip", { key: w.key }),
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
        onChange: E,
        style: { display: "none" }
      }
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileChips__add" + (r ? " tlFileChips__add--uploading" : ""),
        onClick: v,
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
}, Pn = 3e4;
function Mn(l, t) {
  const n = Math.round((l - Date.now()) / 1e3), a = Math.abs(n), c = new Intl.RelativeTimeFormat(t, { numeric: "auto" });
  return a < 60 ? c.format(Math.trunc(n / 1), "second") : a < 3600 ? c.format(Math.trunc(n / 60), "minute") : a < 86400 ? c.format(Math.trunc(n / 3600), "hour") : a < 7 * 86400 ? c.format(Math.trunc(n / 86400), "day") : new Date(l).toLocaleDateString(t);
}
const jn = ({ controlId: l }) => {
  const t = G(), n = t.timestamp, a = t.label ?? void 0, c = t.locale || navigator.language, [, s] = e.useState(0);
  return e.useEffect(() => {
    const i = setInterval(() => s((u) => u + 1), Pn);
    return () => clearInterval(i);
  }, []), n == null ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime tlRelativeTime--empty" }) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime", title: a }, Mn(n, c));
}, An = ({ controlId: l }) => {
  const t = G(), n = t.anchor ?? void 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAnchor", "data-tl-anchor": n }, t.child && /* @__PURE__ */ e.createElement(K, { control: t.child }));
}, Bn = ({ controlId: l }) => {
  const t = G(), n = t.target, a = t.label ?? "";
  if (n == null)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlScrollLink tlScrollLink--empty" });
  const c = (s) => {
    s.preventDefault(), Zt(n);
  };
  return /* @__PURE__ */ e.createElement("a", { id: l, className: "tlScrollLink", href: "#", onClick: c }, a);
};
function On(l) {
  const t = l.trim().split(/\s+/).filter(Boolean);
  return t.length === 0 ? "?" : t.length === 1 ? t[0].slice(0, 2).toUpperCase() : (t[0][0] + t[t.length - 1][0]).toUpperCase();
}
function Fn(l) {
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return Math.abs(t) % 360;
}
const $n = ({ controlId: l }) => {
  const n = G().name;
  return n ? /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlAvatar",
      style: { backgroundColor: `hsl(${Fn(n)}, 45%, 45%)` },
      title: n,
      "aria-label": n
    },
    On(n)
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlAvatar tlAvatar--empty" });
}, Un = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Hn = ({ controlId: l }) => {
  const t = G(), n = Fe(), a = ae(), c = !!t.hasData, s = t.dataRevision ?? 0, i = t.fileName ?? "download", u = !!t.clearable, [r, o] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!c || r)) {
      o(!0);
      try {
        const b = n + (n.includes("?") ? "&" : "?") + "rev=" + s, E = await fetch(b);
        if (!E.ok) {
          console.error("[TLDownload] Failed to fetch data:", E.status);
          return;
        }
        const v = await E.blob(), g = URL.createObjectURL(v), C = document.createElement("a");
        C.href = g, C.download = i, C.style.display = "none", document.body.appendChild(C), C.click(), document.body.removeChild(C), URL.revokeObjectURL(g);
      } catch (b) {
        console.error("[TLDownload] Fetch error:", b);
      } finally {
        o(!1);
      }
    }
  }, [c, r, n, s, i]), p = e.useCallback(async () => {
    c && await a("clear");
  }, [c, a]), f = ie(Un);
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
}, Wn = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, zn = ({ controlId: l }) => {
  const t = G(), n = Oe(), [a, c] = e.useState("idle"), [s, i] = e.useState(null), [u, r] = e.useState(!1), o = e.useRef(null), m = e.useRef(null), p = e.useRef(null), f = e.useRef(null), _ = e.useRef(null), b = t.error, E = e.useMemo(
    () => {
      var R;
      return !!(window.isSecureContext && ((R = navigator.mediaDevices) != null && R.getUserMedia));
    },
    []
  ), v = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((R) => R.stop()), m.current = null), o.current && (o.current.srcObject = null);
  }, []), g = e.useCallback(() => {
    v(), c("idle");
  }, [v]), C = e.useCallback(async () => {
    var R;
    if (a !== "uploading") {
      if (i(null), !E) {
        (R = f.current) == null || R.click();
        return;
      }
      try {
        const F = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        m.current = F, c("overlayOpen");
      } catch (F) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", F), i("js.photoCapture.error.denied"), c("idle");
      }
    }
  }, [a, E]), x = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const R = o.current, F = p.current;
    if (!R || !F)
      return;
    F.width = R.videoWidth, F.height = R.videoHeight;
    const O = F.getContext("2d");
    O && (O.drawImage(R, 0, 0), v(), c("uploading"), F.toBlob(async (D) => {
      if (!D) {
        c("idle");
        return;
      }
      const B = new FormData();
      B.append("photo", D, "capture.jpg"), await n(B), c("idle");
    }, "image/jpeg", 0.85));
  }, [a, n, v]), S = e.useCallback(async (R) => {
    var D;
    const F = (D = R.target.files) == null ? void 0 : D[0];
    if (!F) return;
    c("uploading");
    const O = new FormData();
    O.append("photo", F, F.name), await n(O), c("idle"), f.current && (f.current.value = "");
  }, [n]);
  e.useEffect(() => {
    a === "overlayOpen" && o.current && m.current && (o.current.srcObject = m.current);
  }, [a]), e.useEffect(() => {
    var F;
    if (a !== "overlayOpen") return;
    (F = _.current) == null || F.focus();
    const R = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = R;
    };
  }, [a]), xe(a === "overlayOpen", { ESCAPE: g }), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((R) => R.stop()), m.current = null);
  }, []);
  const w = ie(Wn), k = a === "uploading" ? w["js.uploading"] : w["js.photoCapture.open"], h = ["tlPhotoCapture__cameraBtn"];
  a === "uploading" && h.push("tlPhotoCapture__cameraBtn--uploading");
  const I = ["tlPhotoCapture__overlayVideo"];
  u && I.push("tlPhotoCapture__overlayVideo--mirrored");
  const T = ["tlPhotoCapture__mirrorBtn"];
  return u && T.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: h.join(" "),
      onClick: C,
      disabled: a === "uploading",
      title: k,
      "aria-label": k
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !E && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: f,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: S
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
        onClick: () => r((R) => !R),
        title: w["js.photoCapture.mirror"],
        "aria-label": w["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: x,
        title: w["js.photoCapture.capture"],
        "aria-label": w["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: g,
        title: w["js.photoCapture.close"],
        "aria-label": w["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, w[s]), b && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, b));
}, Vn = {
  "js.photoViewer.alt": "Captured photo"
}, Kn = ({ controlId: l }) => {
  const t = G(), n = Fe(), a = !!t.hasPhoto, c = t.dataRevision ?? 0, [s, i] = e.useState(null), u = e.useRef(c);
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
  const r = ie(Vn);
  return !a || !s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: s,
      alt: r["js.photoViewer.alt"]
    }
  ));
}, Yn = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, Gn = ({ controlId: l }) => {
  const t = G(), n = Fe(), a = !!t.hasPdf, c = t.dataRevision ?? 0, s = ie(Yn), u = n.indexOf("react-api/"), r = u >= 0 ? n.slice(0, u) : n, o = n + "&rev=" + c, m = r + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(o);
  return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: m,
      title: s["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, s["js.pdfViewer.noDocument"]));
}, { useCallback: Ct, useRef: Qe } = e, Xn = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.orientation, c = t.resizable === !0, s = t.children ?? [], i = a === "horizontal", u = s.length > 0 && s.every((v) => v.collapsed), r = !u && s.some((v) => v.collapsed), o = u ? !i : i, m = Qe(null), p = Qe(null), f = Qe(null), _ = Ct((v, g) => {
    const C = {
      overflow: v.scrolling || "auto"
    };
    return v.collapsed ? u && !o ? C.flex = "1 0 0%" : C.flex = "0 0 auto" : g !== void 0 ? C.flex = `0 0 ${g}px` : C.flex = `${v.size} 1 0%`, v.minSize > 0 && !v.collapsed && (C.minWidth = i ? v.minSize : void 0, C.minHeight = i ? void 0 : v.minSize), C;
  }, [i, u, r, o]), b = Ct((v, g) => {
    v.preventDefault();
    const C = m.current;
    if (!C) return;
    const x = s[g], S = s[g + 1], w = C.querySelectorAll(":scope > .tlSplitPanel__child"), k = [];
    w.forEach((T) => {
      k.push(i ? T.offsetWidth : T.offsetHeight);
    }), f.current = k, p.current = {
      splitterIndex: g,
      startPos: i ? v.clientX : v.clientY,
      startSizeBefore: k[g],
      startSizeAfter: k[g + 1],
      childBefore: x,
      childAfter: S
    };
    const h = (T) => {
      const R = p.current;
      if (!R || !f.current) return;
      const O = (i ? T.clientX : T.clientY) - R.startPos, D = R.childBefore.minSize || 0, B = R.childAfter.minSize || 0;
      let Q = R.startSizeBefore + O, H = R.startSizeAfter - O;
      Q < D && (H += Q - D, Q = D), H < B && (Q += H - B, H = B), f.current[R.splitterIndex] = Q, f.current[R.splitterIndex + 1] = H;
      const $ = C.querySelectorAll(":scope > .tlSplitPanel__child"), A = $[R.splitterIndex], P = $[R.splitterIndex + 1];
      A && (A.style.flex = `0 0 ${Q}px`), P && (P.style.flex = `0 0 ${H}px`);
    }, I = () => {
      if (document.removeEventListener("mousemove", h), document.removeEventListener("mouseup", I), document.body.style.cursor = "", document.body.style.userSelect = "", f.current) {
        const T = {};
        s.forEach((R, F) => {
          const O = R.control;
          O != null && O.controlId && f.current && (T[O.controlId] = f.current[F]);
        }), n("updateSizes", { sizes: T });
      }
      f.current = null, p.current = null;
    };
    document.addEventListener("mousemove", h), document.addEventListener("mouseup", I), document.body.style.cursor = i ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [s, i, n]), E = [];
  return s.forEach((v, g) => {
    if (E.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${g}`,
          className: `tlSplitPanel__child${v.collapsed && o ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: _(v)
        },
        /* @__PURE__ */ e.createElement(K, { control: v.control })
      )
    ), c && g < s.length - 1) {
      const C = s[g + 1];
      !v.collapsed && !C.collapsed && E.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${g}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${a}`,
            onMouseDown: (S) => b(S, g)
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
    E
  );
}, qe = ({ image: l, className: t }) => {
  if (!l) return null;
  const n = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `${t ? t + " " : ""}${n}`, "aria-hidden": "true" });
}, { useCallback: Je } = e, qn = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Zn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Qn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), Jn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), el = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), tl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), nl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ie(qn), c = t.title, s = t.expansionState ?? "NORMALIZED", i = t.showMinimize === !0, u = t.showMaximize === !0, r = t.showPopOut === !0, o = t.fullLine === !0, m = t.fill === !0, p = t.hoverActions === !0, f = t.appearance === "card", _ = t.errorMessage, b = s === "MINIMIZED", E = s === "MAXIMIZED", v = s === "HIDDEN", g = Je(() => {
    n("toggleMinimize");
  }, [n]), C = Je(() => {
    n("toggleMaximize");
  }, [n]), x = Je(() => {
    n("popOut");
  }, [n]);
  if (v)
    return null;
  const S = E ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" }, w = i && !E || u && !b || r, k = !!c && c.trim() !== "" || !!t.titleContent || !!t.toolbar || w;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${s.toLowerCase()}${o ? " tlPanel--fullLine" : ""}${m ? " tlPanel--fill" : ""}${p ? " tlPanel--hoverActions" : ""}${f ? " tlPanel--card" : ""}`,
      style: S
    },
    k && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, !!c && c.trim() !== "" && /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, c), t.titleContent && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__titleContent" }, /* @__PURE__ */ e.createElement(K, { control: t.titleContent })), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(K, { control: t.toolbar }), i && !E && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: g,
        title: b ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      b ? /* @__PURE__ */ e.createElement(Qn, null) : /* @__PURE__ */ e.createElement(Zn, null)
    ), u && !b && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: C,
        title: E ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      E ? /* @__PURE__ */ e.createElement(el, null) : /* @__PURE__ */ e.createElement(Jn, null)
    ), r && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: x,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(tl, null)
    ))),
    !b && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(K, { control: t.child })),
    !b && _ && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error tlPanel__error", role: "alert" }, /* @__PURE__ */ e.createElement(qe, { image: t.errorIcon, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, _)),
    !b && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(K, { control: t.buttonBar }))
  );
}, ll = ({ controlId: l }) => {
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
}, al = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(K, { control: t.activeChild }));
}, { useCallback: be, useState: Xe, useEffect: st, useRef: Ze } = e, rl = {
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
const Be = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(we, { encoded: l, className: "tlSidebar__icon" }) : null, ol = ({ item: l, active: t, collapsed: n, onSelect: a, tabIndex: c, itemRef: s, onFocus: i }) => /* @__PURE__ */ e.createElement(
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
), sl = ({ item: l, collapsed: t, onExecute: n, tabIndex: a, itemRef: c, onFocus: s }) => /* @__PURE__ */ e.createElement(
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
), cl = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Be, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), il = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), ul = ({ item: l, activeItemId: t, anchorRect: n, onSelect: a, onExecute: c, onClose: s }) => {
  const i = Ze(null);
  st(() => {
    const o = (m) => {
      i.current && !i.current.contains(m.target) && setTimeout(() => s(), 0);
    };
    return document.addEventListener("mousedown", o), () => document.removeEventListener("mousedown", o);
  }, [s]), xe(!0, { ESCAPE: s });
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
}, dl = ({
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
  onCloseFlyout: E
}) => {
  const v = Ze(null), [g, C] = Xe(null), x = be(() => {
    a ? _ === l.id ? E() : (v.current && C(v.current.getBoundingClientRect()), b(l.id)) : i(l.id);
  }, [a, _, l.id, i, b, E]), S = be((k) => {
    v.current = k, r(k);
  }, [r]), w = a && _ === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (w ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: x,
      title: a ? l.label : void 0,
      "aria-expanded": a ? w : t,
      tabIndex: u,
      ref: S,
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
  ), w && /* @__PURE__ */ e.createElement(
    ul,
    {
      item: l,
      activeItemId: n,
      anchorRect: g,
      onSelect: c,
      onExecute: s,
      onClose: E
    }
  ), t && !a && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((k) => /* @__PURE__ */ e.createElement(
    $t,
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
      onCloseFlyout: E
    }
  ))));
}, $t = ({
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
        ol,
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
        sl,
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
      return /* @__PURE__ */ e.createElement(cl, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(il, null);
    case "group": {
      const _ = o ? o.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        dl,
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
}, ml = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ie(rl), c = t.items ?? [], s = t.activeItemId, i = t.collapsed, u = t.drawerOpen, r = u ? !1 : i, [o, m] = Xe(() => {
    const D = /* @__PURE__ */ new Map(), B = (Q) => {
      for (const H of Q)
        H.type === "group" && (D.set(H.id, H.expanded), B(H.children));
    };
    return B(c), D;
  }), p = be((D) => {
    m((B) => {
      const Q = new Map(B), H = Q.get(D) ?? !1;
      return Q.set(D, !H), n("toggleGroup", { itemId: D, expanded: !H }), Q;
    });
  }, [n]), f = be((D) => {
    D !== s && n("selectItem", { itemId: D });
  }, [n, s]), _ = be((D) => {
    n("executeCommand", { itemId: D });
  }, [n]), b = be(() => {
    n("toggleCollapse", {});
  }, [n]), E = be(() => {
    n("toggleDrawer", {});
  }, [n]), [v, g] = Xe(null), C = be((D) => {
    g(D);
  }, []), x = be(() => {
    g(null);
  }, []);
  st(() => {
    r || g(null);
  }, [r]);
  const [S, w] = Xe(() => {
    const D = ct(c, r, o);
    return D.length > 0 ? D[0].id : "";
  }), k = Ze(/* @__PURE__ */ new Map()), h = be((D) => (B) => {
    B ? k.current.set(D, B) : k.current.delete(D);
  }, []), I = be((D) => {
    w(D);
  }, []), T = Ze(0), R = be((D) => {
    w(D), T.current++;
  }, []);
  st(() => {
    const D = k.current.get(S);
    D && document.activeElement !== D && D.focus();
  }, [S, T.current]);
  const F = be((D) => {
    if (D.key === "Escape" && v !== null) {
      D.preventDefault(), x();
      return;
    }
    const B = ct(c, r, o);
    if (B.length === 0) return;
    const Q = B.findIndex(($) => $.id === S);
    if (Q < 0) return;
    const H = B[Q];
    switch (D.key) {
      case "ArrowDown": {
        D.preventDefault();
        const $ = (Q + 1) % B.length;
        R(B[$].id);
        break;
      }
      case "ArrowUp": {
        D.preventDefault();
        const $ = (Q - 1 + B.length) % B.length;
        R(B[$].id);
        break;
      }
      case "Home": {
        D.preventDefault(), R(B[0].id);
        break;
      }
      case "End": {
        D.preventDefault(), R(B[B.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        D.preventDefault(), H.type === "nav" ? f(H.id) : H.type === "command" ? _(H.id) : H.type === "group" && (r ? v === H.id ? x() : C(H.id) : p(H.id));
        break;
      }
      case "ArrowRight": {
        H.type === "group" && !r && ((o.get(H.id) ?? !1) || (D.preventDefault(), p(H.id)));
        break;
      }
      case "ArrowLeft": {
        H.type === "group" && !r && (o.get(H.id) ?? !1) && (D.preventDefault(), p(H.id));
        break;
      }
    }
  }, [
    c,
    r,
    o,
    S,
    v,
    R,
    f,
    _,
    p,
    C,
    x
  ]), O = "tlSidebar" + (r ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: O }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(K, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: E, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": a["js.sidebar.ariaLabel"] }, r ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: F }, c.map((D) => /* @__PURE__ */ e.createElement(
    $t,
    {
      key: D.id,
      item: D,
      activeItemId: s,
      collapsed: r,
      onSelect: f,
      onExecute: _,
      onToggleGroup: p,
      focusedId: S,
      setItemRef: h,
      onItemFocus: I,
      groupStates: o,
      flyoutGroupId: v,
      onOpenFlyout: C,
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
}, pl = ({ controlId: l }) => {
  const t = G(), n = t.direction ?? "column", a = t.gap ?? "default", c = t.align ?? "stretch", s = t.wrap === !0, i = t.growFirst === !0, u = t.children ?? [], r = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${a}`,
    `tlStack--align-${c}`,
    s ? "tlStack--wrap" : "",
    i ? "tlStack--grow-first" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: r }, u.map((o, m) => /* @__PURE__ */ e.createElement(K, { key: m, control: o })));
}, fl = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlInset" }, t.child && /* @__PURE__ */ e.createElement(K, { control: t.child }));
}, hl = ({ controlId: l }) => {
  const t = G(), n = t.columns, a = t.minColumnWidth, c = t.gap ?? "default", s = t.children ?? [], i = {};
  return a ? i.gridTemplateColumns = `repeat(auto-fit, minmax(min(${a}, 100%), 1fr))` : n && (i.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${c}`, style: i }, s.map((u, r) => /* @__PURE__ */ e.createElement(K, { key: r, control: u })));
}, bl = ({ controlId: l }) => {
  const t = G(), n = t.title, a = t.variant ?? "outlined", c = t.padding ?? "default", s = t.headerActions ?? [], i = t.child, u = n != null || s.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${a}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, s.map((r, o) => /* @__PURE__ */ e.createElement(K, { key: o, control: r })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${c}` }, /* @__PURE__ */ e.createElement(K, { control: i })));
}, _l = ({ controlId: l }) => {
  const t = G(), n = t.title ?? "", a = t.leading, c = t.children ?? [], s = t.actions ?? [], i = t.variant ?? "flat", r = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    i === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: r }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(K, { control: a })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, c.map((o, m) => /* @__PURE__ */ e.createElement(K, { key: m, control: o }))), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((o, m) => /* @__PURE__ */ e.createElement(K, { key: m, control: o }))));
}, { useCallback: gl } = e, vl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.items ?? [], c = gl((s) => {
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
}, { useCallback: El } = e, Cl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.items ?? [], c = t.activeItemId, s = El((i) => {
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
}, { useCallback: wt, useRef: wl } = e, yl = ({ onClose: l }) => (de("ESCAPE", () => (l(), !0)), null), kl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.open === !0, c = t.closeOnBackdrop !== !1, s = t.child, i = wl(null), u = wt(() => {
    n("close");
  }, [n]), r = wt((o) => {
    c && o.target === o.currentTarget && u();
  }, [c, u]);
  return a ? /* @__PURE__ */ e.createElement(mt, null, /* @__PURE__ */ e.createElement(yl, { onClose: u }), /* @__PURE__ */ e.createElement(
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
}, { useEffect: Sl, useRef: Nl } = e, Tl = ({ controlId: l }) => {
  const n = G().dialogs ?? [], a = Nl(n.length);
  return Sl(() => {
    n.length < a.current && n.length > 0, a.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((c) => /* @__PURE__ */ e.createElement(K, { key: c.controlId, control: c })));
}, { useCallback: ze, useRef: Pe, useState: Ve } = e, Rl = ({ onClose: l }) => (de("ESCAPE", () => (l(), !0)), null), Dl = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Ll = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], xl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ie(Dl), c = t.title ?? "", s = t.width ?? "32rem", i = t.height ?? null, u = t.minHeight ?? null, r = t.resizable === !0, o = t.child, m = t.actions ?? [], p = t.toolbar, f = t.buttonBar, [_, b] = Ve(null), [E, v] = Ve(null), [g, C] = Ve(null), x = Pe(null), [S, w] = Ve(!1), k = Pe(null), h = Pe(null), I = Pe(null), T = Pe(null), R = Pe(null), F = ze(() => {
    n("close");
  }, [n]);
  pt(!0, T, "field");
  const O = ze(($, A) => {
    A.preventDefault();
    const P = T.current;
    if (!P) return;
    const X = P.getBoundingClientRect(), d = !x.current, N = x.current ?? { x: X.left, y: X.top };
    d && (x.current = N, C(N)), R.current = {
      dir: $,
      startX: A.clientX,
      startY: A.clientY,
      startW: X.width,
      startH: X.height,
      startPos: { ...N },
      symmetric: d
    };
    const V = (q) => {
      const j = R.current;
      if (!j) return;
      const ee = q.clientX - j.startX, oe = q.clientY - j.startY;
      let ne = j.startW, he = j.startH, ge = 0, ve = 0;
      j.symmetric ? (j.dir.includes("e") && (ne = j.startW + 2 * ee), j.dir.includes("w") && (ne = j.startW - 2 * ee), j.dir.includes("s") && (he = j.startH + 2 * oe), j.dir.includes("n") && (he = j.startH - 2 * oe)) : (j.dir.includes("e") && (ne = j.startW + ee), j.dir.includes("w") && (ne = j.startW - ee, ge = ee), j.dir.includes("s") && (he = j.startH + oe), j.dir.includes("n") && (he = j.startH - oe, ve = oe));
      const ye = Math.max(200, ne), ke = Math.max(100, he);
      j.symmetric ? (ge = (j.startW - ye) / 2, ve = (j.startH - ke) / 2) : (j.dir.includes("w") && ye === 200 && (ge = j.startW - 200), j.dir.includes("n") && ke === 100 && (ve = j.startH - 100)), h.current = ye, I.current = ke, b(ye), v(ke);
      const Ie = {
        x: j.startPos.x + ge,
        y: j.startPos.y + ve
      };
      x.current = Ie, C(Ie);
    }, W = () => {
      document.removeEventListener("mousemove", V), document.removeEventListener("mouseup", W);
      const q = h.current, j = I.current;
      (q != null || j != null) && n("resize", {
        ...q != null ? { width: Math.round(q) } : {},
        ...j != null ? { height: Math.round(j) } : {}
      }), R.current = null;
    };
    document.addEventListener("mousemove", V), document.addEventListener("mouseup", W);
  }, [n]), D = ze(($) => {
    if ($.button !== 0 || $.target.closest("button")) return;
    $.preventDefault();
    const A = T.current;
    if (!A) return;
    const P = A.getBoundingClientRect(), X = x.current ?? { x: P.left, y: P.top }, d = $.clientX - X.x, N = $.clientY - X.y, V = (q) => {
      const j = window.innerWidth, ee = window.innerHeight;
      let oe = q.clientX - d, ne = q.clientY - N;
      const he = A.offsetWidth, ge = A.offsetHeight;
      oe + he > j && (oe = j - he), ne + ge > ee && (ne = ee - ge), oe < 0 && (oe = 0), ne < 0 && (ne = 0);
      const ve = { x: oe, y: ne };
      x.current = ve, C(ve);
    }, W = () => {
      document.removeEventListener("mousemove", V), document.removeEventListener("mouseup", W);
    };
    document.addEventListener("mousemove", V), document.addEventListener("mouseup", W);
  }, []), B = ze(() => {
    var $, A;
    if (S) {
      const P = k.current;
      P && (C(P.x !== -1 ? { x: P.x, y: P.y } : null), b(P.w), v(P.h)), w(!1);
    } else {
      const P = T.current, X = P == null ? void 0 : P.getBoundingClientRect();
      k.current = {
        x: (($ = x.current) == null ? void 0 : $.x) ?? (X == null ? void 0 : X.left) ?? -1,
        y: ((A = x.current) == null ? void 0 : A.y) ?? (X == null ? void 0 : X.top) ?? -1,
        w: _ ?? (X == null ? void 0 : X.width) ?? null,
        h: E ?? null
      }, w(!0), C({ x: 0, y: 0 }), b(null), v(null);
    }
  }, [S, _, E]), Q = S ? { position: "absolute", top: 0, left: 0, width: "100vw", maxWidth: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: _ != null ? _ + "px" : s,
    ...E != null ? { height: E + "px" } : i != null ? { height: i } : {},
    ...u != null && E == null ? { minHeight: u } : {},
    maxHeight: g ? "100vh" : "80vh",
    ...g ? { position: "absolute", left: g.x + "px", top: g.y + "px" } : {}
  }, H = l + "-title";
  return /* @__PURE__ */ e.createElement(mt, { modal: !0 }, /* @__PURE__ */ e.createElement(Rl, { onClose: F }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: Q,
      ref: T,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": H
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${S ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: S ? void 0 : D,
        onDoubleClick: r ? B : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: H }, c),
      p && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(K, { control: p })),
      r && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: B,
          title: S ? a["js.window.restore"] : a["js.window.maximize"]
        },
        S ? (
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
          onClick: F,
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
    (m.length > 0 || f) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, f && /* @__PURE__ */ e.createElement(K, { control: f }), m.map(($, A) => /* @__PURE__ */ e.createElement(K, { key: A, control: $ }))),
    r && !S && Ll.map(($) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: $,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${$}`,
        onMouseDown: (A) => O($, A)
      }
    ))
  ));
}, { useCallback: Il } = e, Pl = {
  "js.drawer.close": "Close"
}, Ml = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ie(Pl), c = t.open === !0, s = t.position ?? "right", i = t.size ?? "medium", u = t.title ?? null, r = t.child, o = Il(() => {
    n("close");
  }, [n]);
  xe(c, { ESCAPE: o });
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
}, { useCallback: jl } = e, Al = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.child, c = jl((s) => {
    s.preventDefault(), s.stopPropagation(), n("openContextMenu", { x: s.clientX, y: s.clientY });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tl-context-menu-region", onContextMenu: c }, a && /* @__PURE__ */ e.createElement(K, { control: a }));
}, { useCallback: Bl, useEffect: yt, useRef: Ol, useState: kt } = e, Fl = 250, $l = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.message ?? "", c = t.content ?? "", s = t.variant ?? "info", i = t.duration ?? 5e3, u = t.visible === !0, r = t.generation ?? 0, [o, m] = kt(!1), [p, f] = kt(!1), _ = Ol(!1);
  yt(() => {
    _.current = !1;
  }, [r]);
  const b = Bl(() => {
    m(!0), setTimeout(() => {
      n("dismiss", { generation: r }), m(!1);
    }, 200);
  }, [n, r]);
  return yt(() => {
    if (!u || i === 0 || p) return;
    const E = setTimeout(b, _.current ? Fl : i);
    return () => clearTimeout(E);
  }, [u, i, p, b]), !u && !o ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${s}${o ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite",
      onMouseEnter: () => {
        _.current = !0, f(!0);
      },
      onMouseLeave: () => f(!1)
    },
    c ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: c } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, a)
  );
}, { useCallback: et, useEffect: St, useRef: Ul, useState: Nt } = e, Hl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.open === !0, c = t.anchorId, s = t.anchorX, i = t.anchorY, u = t.items ?? [], r = Ul(null), [o, m] = Nt({ top: 0, left: 0 }), [p, f] = Nt(0), _ = u.filter((g) => g.type === "item" && !g.disabled);
  St(() => {
    var h, I;
    if (!a) return;
    const g = ((h = r.current) == null ? void 0 : h.offsetHeight) ?? 200, C = ((I = r.current) == null ? void 0 : I.offsetWidth) ?? 200;
    if (s != null && i != null) {
      let T = i, R = s;
      T + g > window.innerHeight && (T = Math.max(0, window.innerHeight - g)), R + C > window.innerWidth && (R = Math.max(0, window.innerWidth - C)), m({ top: T, left: R }), f(0);
      return;
    }
    if (!c) return;
    const x = document.getElementById(c);
    if (!x) return;
    const S = x.getBoundingClientRect();
    let w = S.bottom + 4, k = S.left;
    w + g > window.innerHeight && (w = S.top - g - 4), k + C > window.innerWidth && (k = S.right - C), m({ top: w, left: k }), f(0);
  }, [a, c, s, i]);
  const b = et(() => {
    n("close");
  }, [n]), E = et((g) => {
    n("selectItem", { itemId: g });
  }, [n]);
  St(() => {
    if (!a) return;
    const g = (C) => {
      r.current && !r.current.contains(C.target) && b();
    };
    return document.addEventListener("mousedown", g), () => document.removeEventListener("mousedown", g);
  }, [a, b]);
  const v = et((g) => {
    if (g.key === "Escape") {
      g.preventDefault(), b();
      return;
    }
    if (g.key === "ArrowDown")
      g.preventDefault(), f((C) => (C + 1) % _.length);
    else if (g.key === "ArrowUp")
      g.preventDefault(), f((C) => (C - 1 + _.length) % _.length);
    else if (g.key === "Enter" || g.key === " ") {
      g.preventDefault();
      const C = _[p];
      C && E(C.id);
    }
  }, [b, E, _, p]);
  return pt(a, r), a ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: r,
      tabIndex: -1,
      style: { position: "fixed", top: o.top, left: o.left },
      onKeyDown: v
    },
    u.map((g, C) => {
      if (g.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: C, className: "tlMenu__separator" });
      const S = _.indexOf(g) === p;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: g.id,
          type: "button",
          className: "tlMenu__item" + (S ? " tlMenu__item--focused" : "") + (g.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: g.disabled,
          tabIndex: S ? 0 : -1,
          onClick: () => E(g.id)
        },
        g.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + g.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, g.label)
      );
    })
  ) : null;
}, Wl = 768, zl = ({ controlId: l }) => {
  const t = G(), n = ae();
  e.useEffect(() => {
    const o = window.matchMedia(`(max-width: ${Wl}px)`), m = (f) => {
      n("reportDisplayClass", { displayClass: f ? "COMPACT" : "REGULAR" });
    };
    m(o.matches);
    const p = (f) => m(f.matches);
    return o.addEventListener("change", p), () => o.removeEventListener("change", p);
  }, [n]);
  const a = t.header, c = t.content, s = t.footer, i = t.snackbar, u = t.dialogManager, r = t.menuOverlay;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(K, { control: a })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(K, { control: c })), s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(K, { control: s })), /* @__PURE__ */ e.createElement(K, { control: i }), u && /* @__PURE__ */ e.createElement(K, { control: u }), r && /* @__PURE__ */ e.createElement(K, { control: r }));
}, Vl = ({ controlId: l }) => {
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
}, Kl = ({ isMulti: l, cursorIndex: t, onMove: n, onToggle: a, onSelectAll: c }) => (de("ArrowUp", () => (n("up", !1, !1), !0)), de("ArrowDown", () => (n("down", !1, !1), !0)), de("Home", () => (n("home", !1, !1), !0)), de("End", () => (n("end", !1, !1), !0)), de("PageUp", () => (n("pageUp", !1, !1), !0)), de("PageDown", () => (n("pageDown", !1, !1), !0)), de("Shift+ArrowUp", () => (n("up", l, !1), !0)), de("Shift+ArrowDown", () => (n("down", l, !1), !0)), de("Shift+Home", () => (n("home", l, !1), !0)), de("Shift+End", () => (n("end", l, !1), !0)), de("Shift+PageUp", () => (n("pageUp", l, !1), !0)), de("Shift+PageDown", () => (n("pageDown", l, !1), !0)), de("Ctrl+ArrowUp", () => (n("up", !1, l), !0)), de("Ctrl+ArrowDown", () => (n("down", !1, l), !0)), de("Space", () => t < 0 ? !1 : (a(), !0)), de("Ctrl+A", () => l ? (c(), !0) : !1), null), Yl = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all",
  "js.table.filter": "Filter",
  "js.table.columns": "Columns"
}, Tt = 50;
function Rt(l) {
  var n;
  const t = l.target;
  return !!((n = t == null ? void 0 : t.closest) != null && n.call(t, 'input, textarea, select, button, a, [contenteditable="true"]'));
}
const it = 'input:not([disabled]):not([readonly]), textarea:not([disabled]):not([readonly]), select:not([disabled]), [contenteditable="true"]', Gl = it + ", button:not([disabled]), a[href]";
function Ut(l, t) {
  return Array.from(l.querySelectorAll("[data-row][data-col]")).filter((n) => n.dataset.row === t);
}
function Dt(l, t, n = {}) {
  const a = Ut(l, t);
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
const Xl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ie(Yl), c = e.useRef(null);
  e.useEffect(() => {
    const y = c.current;
    if (!y) return;
    const L = (z) => {
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
    return y.addEventListener("tl-tooltip-resolve", L), () => y.removeEventListener("tl-tooltip-resolve", L);
  }, []);
  const s = t.columns ?? [], i = t.totalRowCount ?? 0, u = t.rows ?? [], r = t.rowHeight ?? 36, o = t.selectionMode ?? "single", m = t.selectedCount ?? 0, p = t.cursorIndex ?? -1, f = t.frozenColumnCount ?? 0, _ = t.treeMode ?? !1, b = t.columnSelect ?? !1, E = e.useMemo(
    () => s.filter((y) => y.sortPriority && y.sortPriority > 0).length,
    [s]
  ), v = o === "multi", g = 40, C = 20, x = e.useRef(null), S = e.useRef(null), w = e.useRef(null), k = e.useRef(null), [h, I] = e.useState({}), T = e.useRef(null), R = e.useRef(!1), F = e.useRef(null), [O, D] = e.useState(null), [B, Q] = e.useState(null);
  e.useEffect(() => {
    T.current || I({});
  }, [s]);
  const H = e.useCallback((y) => h[y.name] ?? y.width, [h]), $ = e.useMemo(() => {
    const y = [];
    let L = v && f > 0 ? g : 0;
    for (let z = 0; z < f && z < s.length; z++)
      y.push(L), L += H(s[z]);
    return y;
  }, [s, f, v, g, H]), A = i * r, P = e.useRef(null), X = e.useCallback((y, L, z) => {
    z.preventDefault(), z.stopPropagation(), T.current = { column: y, startX: z.clientX, startWidth: L };
    let J = z.clientX, te = 0;
    const ce = () => {
      const ue = T.current;
      if (!ue) return;
      const me = Math.max(Tt, ue.startWidth + (J - ue.startX) + te);
      I((Ce) => ({ ...Ce, [ue.column]: me }));
    }, re = () => {
      const ue = S.current, me = x.current;
      if (!ue || !T.current) return;
      const Ce = ue.getBoundingClientRect(), Ne = 40, bt = 8, qt = ue.scrollLeft;
      J > Ce.right - Ne ? ue.scrollLeft += bt : J < Ce.left + Ne && (ue.scrollLeft = Math.max(0, ue.scrollLeft - bt));
      const _t = ue.scrollLeft - qt;
      _t !== 0 && (me && (me.scrollLeft = ue.scrollLeft), te += _t, ce()), P.current = requestAnimationFrame(re);
    };
    P.current = requestAnimationFrame(re);
    const _e = (ue) => {
      J = ue.clientX, ce();
    }, Se = (ue) => {
      document.removeEventListener("mousemove", _e), document.removeEventListener("mouseup", Se), P.current !== null && (cancelAnimationFrame(P.current), P.current = null);
      const me = T.current;
      if (me) {
        const Ce = Math.max(Tt, me.startWidth + (ue.clientX - me.startX) + te);
        n("columnResize", { column: me.column, width: Ce }), T.current = null, R.current = !0, requestAnimationFrame(() => {
          R.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", _e), document.addEventListener("mouseup", Se);
  }, [n]), d = e.useCallback(() => {
    x.current && S.current && (x.current.scrollLeft = S.current.scrollLeft), w.current !== null && clearTimeout(w.current), w.current = window.setTimeout(() => {
      const y = S.current;
      if (!y) return;
      const L = y.scrollTop, z = Math.ceil(y.clientHeight / r), J = Math.floor(L / r);
      n("scroll", { start: J, count: z });
    }, 80);
  }, [n, r]), N = e.useCallback((y, L, z) => {
    if (R.current) return;
    let J;
    !L || L === "desc" ? J = "asc" : J = "desc";
    const te = z.shiftKey ? "add" : "replace";
    n("sort", { column: y, direction: J, mode: te });
  }, [n]), V = e.useCallback((y, L) => {
    F.current = y, L.dataTransfer.effectAllowed = "move", L.dataTransfer.setData("text/plain", y);
  }, []), W = e.useCallback((y, L) => {
    if (!F.current || F.current === y) {
      D(null);
      return;
    }
    L.preventDefault(), L.dataTransfer.dropEffect = "move";
    const z = L.currentTarget.getBoundingClientRect(), J = L.clientX < z.left + z.width / 2 ? "left" : "right";
    D({ column: y, side: J });
  }, []), q = e.useCallback((y) => {
    y.preventDefault(), y.stopPropagation();
    const L = F.current;
    if (!L || !O) {
      F.current = null, D(null);
      return;
    }
    let z = s.findIndex((te) => te.name === O.column);
    if (z < 0) {
      F.current = null, D(null);
      return;
    }
    const J = s.findIndex((te) => te.name === L);
    O.side === "right" && z++, J < z && z--, n("columnReorder", { column: L, targetIndex: z }), F.current = null, D(null);
  }, [s, O, n]), j = e.useCallback(() => {
    F.current = null, D(null);
  }, []), ee = e.useCallback((y, L) => {
    var J, te, ce, re;
    const z = window.getSelection();
    if (!(z && !z.isCollapsed && L.currentTarget.contains(z.anchorNode))) {
      if (!Rt(L) && ((J = S.current) == null || J.focus({ preventScroll: !0 }), !L.ctrlKey && !L.metaKey && !L.shiftKey)) {
        const _e = (re = (ce = (te = L.target) == null ? void 0 : te.closest) == null ? void 0 : ce.call(te, "[data-col]")) == null ? void 0 : re.getAttribute("data-col");
        k.current = { index: y, col: _e ?? void 0 };
      }
      n("select", {
        rowIndex: y,
        ctrlKey: L.ctrlKey || L.metaKey,
        shiftKey: L.shiftKey
      });
    }
  }, [n]), oe = e.useCallback((y, L, z) => {
    n("moveSelection", { direction: y, extend: L, move: z });
  }, [n]), ne = e.useCallback(() => {
    p < 0 || n("select", { rowIndex: p, ctrlKey: v, shiftKey: !1 });
  }, [n, p, v]), he = e.useCallback(() => {
    n("selectAll", { selected: !0 });
  }, [n]), ge = e.useCallback(
    () => !!c.current && c.current.contains(document.activeElement),
    []
  );
  e.useEffect(() => {
    if (p < 0)
      return;
    const y = S.current;
    if (!y)
      return;
    const L = p * r, z = L + r;
    L < y.scrollTop ? y.scrollTop = L : z > y.scrollTop + y.clientHeight && (y.scrollTop = z - y.clientHeight);
  }, [p, r]), e.useEffect(() => {
    const y = k.current, L = S.current;
    if (!y || !L)
      return;
    const z = u.find((te) => te.index === y.index);
    if (!z)
      return;
    const J = Dt(L, z.id, { col: y.col, last: y.last });
    J && (k.current = null, J.focus({ preventScroll: !1 }), J instanceof HTMLInputElement && J.select());
  }, [u]);
  const ve = e.useCallback((y) => {
    if (y.key !== "Tab")
      return;
    const L = S.current, z = document.activeElement;
    if (!L || !z || !L.contains(z))
      return;
    const J = z.closest("[data-row][data-col]");
    if (!J)
      return;
    const te = J.dataset.row, ce = u.find((Ne) => Ne.id === te);
    if (!ce)
      return;
    const re = Ut(L, te).flatMap((Ne) => Array.from(Ne.querySelectorAll(Gl))), _e = re.indexOf(z);
    if (_e < 0)
      return;
    const Se = !y.shiftKey;
    if (!(Se ? _e === re.length - 1 : _e === 0))
      return;
    const me = Se ? ce.index + 1 : ce.index - 1;
    if (me < 0 || me >= i)
      return;
    const Ce = u.find((Ne) => Ne.index === me);
    Ce && Dt(L, Ce.id) || (y.preventDefault(), k.current = { index: me, last: !Se }, n("select", { rowIndex: me, ctrlKey: !1, shiftKey: !1 }));
  }, [u, i, n]), ye = e.useCallback((y, L) => {
    L.stopPropagation(), n("select", { rowIndex: y, ctrlKey: !0, shiftKey: !1 });
  }, [n]), ke = e.useCallback(() => {
    const y = m === i && i > 0;
    n("selectAll", { selected: !y });
  }, [n, m, i]), Ie = e.useCallback((y, L, z) => {
    z.stopPropagation(), n("expand", { rowIndex: y, expanded: L });
  }, [n]), He = e.useCallback((y, L) => {
    L.preventDefault(), Q({ x: L.clientX, y: L.clientY, colIdx: y });
  }, []), M = e.useCallback(() => {
    B && (n("setFrozenColumnCount", { count: B.colIdx + 1 }), Q(null));
  }, [B, n]), Y = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), Q(null);
  }, [n]);
  e.useEffect(() => {
    if (!B) return;
    const y = () => Q(null);
    return document.addEventListener("mousedown", y), () => document.removeEventListener("mousedown", y);
  }, [B]), xe(!!B, { ESCAPE: () => Q(null) });
  const le = e.useCallback((y, L) => {
    L.stopPropagation(), L.preventDefault(), n("openFilter", { column: y });
  }, [n]), se = e.useCallback((y) => {
    y.stopPropagation(), y.preventDefault(), n("openColumnSelect", {});
  }, [n]), De = s.reduce((y, L) => y + H(L), 0) + (v ? g : 0), Gt = m === i && i > 0, ht = m > 0 && m < i, Xt = e.useCallback((y) => {
    y && (y.indeterminate = ht);
  }, [ht]);
  return /* @__PURE__ */ e.createElement(mt, { active: ge }, /* @__PURE__ */ e.createElement(
    Kl,
    {
      isMulti: v,
      cursorIndex: p,
      onMove: oe,
      onToggle: ne,
      onSelectAll: he
    }
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: c,
      id: l,
      className: "tlTableView",
      "data-tooltip": "dynamic",
      onDragOver: (y) => {
        if (!F.current) return;
        y.preventDefault();
        const L = S.current, z = x.current;
        if (!L) return;
        const J = L.getBoundingClientRect(), te = 40, ce = 8;
        y.clientX < J.left + te ? L.scrollLeft = Math.max(0, L.scrollLeft - ce) : y.clientX > J.right - te && (L.scrollLeft += ce), z && (z.scrollLeft = L.scrollLeft);
      },
      onDrop: q
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: x }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { width: De } }, v && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (f > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: g,
          minWidth: g,
          ...f > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (y) => {
          F.current && (y.preventDefault(), y.dataTransfer.dropEffect = "move", s.length > 0 && s[0].name !== F.current && D({ column: s[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: Xt,
          className: "tlTableView__checkbox",
          checked: Gt,
          onChange: ke
        }
      )
    ), s.map((y, L) => {
      const z = H(y);
      s.length - 1;
      let J = "tlTableView__headerCell";
      y.sortable && (J += " tlTableView__headerCell--sortable"), O && O.column === y.name && (J += " tlTableView__headerCell--dragOver-" + O.side);
      const te = L < f, ce = L === f - 1;
      return te && (J += " tlTableView__headerCell--frozen"), ce && (J += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: y.name,
          className: J,
          style: {
            width: z,
            minWidth: z,
            position: te ? "sticky" : "relative",
            ...te ? { left: $[L], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: y.sortable ? (re) => N(y.name, y.sortDirection, re) : void 0,
          onContextMenu: (re) => He(L, re),
          onDragStart: (re) => V(y.name, re),
          onDragOver: (re) => W(y.name, re),
          onDrop: q,
          onDragEnd: j
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
            onClick: (re) => le(y.name, re)
          },
          /* @__PURE__ */ e.createElement("i", { className: y.filterActive ? "bi bi-funnel-fill" : "bi bi-funnel" })
        ),
        y.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, y.sortDirection === "asc" ? "▲" : "▼", E > 1 && y.sortPriority != null && y.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, y.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (re) => X(y.name, z, re)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (y) => {
          if (F.current && s.length > 0) {
            const L = s[s.length - 1];
            L.name !== F.current && (y.preventDefault(), y.dataTransfer.dropEffect = "move", D({ column: L.name, side: "right" }));
          }
        },
        onDrop: q
      }
    ))), b && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__columnsButton",
        title: a["js.table.columns"],
        "aria-label": a["js.table.columns"],
        onClick: se
      },
      /* @__PURE__ */ e.createElement("i", { className: "bi bi-gear" })
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: S,
        className: "tlTableView__body",
        onScroll: d,
        onKeyDown: ve,
        tabIndex: 0
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: A, position: "relative", width: De } }, u.map((y) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: y.id,
          className: "tlTableView__row" + (y.selected ? " tlTableView__row--selected" : "") + (y.index === p ? " tlTableView__row--cursor" : ""),
          style: {
            position: "absolute",
            top: y.index * r,
            height: r,
            width: De,
            ...y.index === p ? { outline: "2px solid var(--color-primary, #1a73e8)", outlineOffset: "-2px" } : {}
          },
          onMouseDown: (L) => {
            (L.shiftKey || L.ctrlKey || L.metaKey || L.detail > 1) && !Rt(L) && L.preventDefault();
          },
          onClick: (L) => ee(y.index, L)
        },
        v && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (f > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: g,
              minWidth: g,
              ...f > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (L) => L.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: y.selected,
              onChange: () => {
              },
              onClick: (L) => ye(y.index, L),
              tabIndex: -1
            }
          )
        ),
        s.map((L, z) => {
          const J = H(L), te = z === s.length - 1, ce = z < f, re = z === f - 1;
          let _e = "tlTableView__cell";
          ce && (_e += " tlTableView__cell--frozen"), re && (_e += " tlTableView__cell--frozenLast");
          const Se = _ && z === 0, ue = y.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: L.name,
              className: _e,
              "data-row": y.id,
              "data-col": L.name,
              style: {
                ...te && !ce ? { flex: "1 0 auto", minWidth: J } : { width: J, minWidth: J },
                ...ce ? { position: "sticky", left: $[z], zIndex: 2 } : {}
              }
            },
            Se ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ue * C } }, y.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (me) => Ie(y.index, !y.expanded, me)
              },
              y.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), y.cells[L.name] && /* @__PURE__ */ e.createElement(K, { control: y.cells[L.name] })) : y.cells[L.name] && /* @__PURE__ */ e.createElement(K, { control: y.cells[L.name] })
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
        onMouseDown: (y) => y.stopPropagation()
      },
      B.colIdx + 1 !== f && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: M }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      f > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Y }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
    )
  ));
}, ql = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.entries ?? [], c = a.filter((E) => E.visible).length, s = e.useRef(null), i = e.useRef(null), [u, r] = e.useState(null), o = e.useCallback((E) => {
    i.current = E, r(E);
  }, []), m = e.useCallback((E, v) => {
    n("columnVisible", { column: E, visible: v });
  }, [n]), p = e.useCallback((E, v) => {
    s.current = E, v.dataTransfer.effectAllowed = "move", v.dataTransfer.setData("text/plain", E);
  }, []), f = e.useCallback((E, v) => {
    if (!s.current || s.current === E) {
      o(null);
      return;
    }
    v.preventDefault(), v.dataTransfer.dropEffect = "move";
    const g = v.currentTarget.getBoundingClientRect(), C = v.clientY < g.top + g.height / 2 ? "top" : "bottom";
    o({ name: E, side: C });
  }, [o]), _ = e.useCallback(() => {
    s.current = null, o(null);
  }, [o]), b = e.useCallback((E) => {
    E.preventDefault();
    const v = s.current, g = i.current;
    if (s.current = null, o(null), !v || !g)
      return;
    const C = a.findIndex((w) => w.name === g.name), x = a.findIndex((w) => w.name === v);
    if (C < 0 || x < 0)
      return;
    let S = g.side === "top" ? C : C + 1;
    x < S && S--, S !== x && n("columnReorder", { column: v, targetIndex: S });
  }, [a, n, o]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlColumnSelect", onDrop: b }, a.map((E) => {
    const v = E.visible && c <= 1;
    let g = "tlColumnSelect__row";
    return u && u.name === E.name && (g += " tlColumnSelect__row--dragOver-" + u.side), /* @__PURE__ */ e.createElement(
      "div",
      {
        key: E.name,
        className: g,
        draggable: !0,
        onDragStart: (C) => p(E.name, C),
        onDragOver: (C) => f(E.name, C),
        onDrop: b,
        onDragEnd: _
      },
      /* @__PURE__ */ e.createElement("i", { className: "tlColumnSelect__handle bi bi-grip-vertical", "aria-hidden": "true" }),
      /* @__PURE__ */ e.createElement("label", { className: "tlColumnSelect__label" }, /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          className: "tlReactCheckbox",
          checked: E.visible,
          disabled: v,
          onChange: (C) => m(E.name, C.target.checked)
        }
      ), /* @__PURE__ */ e.createElement("span", null, E.label))
    );
  }));
}, Zl = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, Ht = e.createContext(Zl), { useMemo: Ql, useRef: Jl, useState: ea, useEffect: ta } = e, na = 320, la = "TLTableView", aa = "TLPanel", ra = ({ controlId: l }) => {
  var v;
  const t = G(), n = t.maxColumns ?? 3, a = t.labelPosition ?? "auto", c = t.readOnly === !0, s = t.children ?? [], i = t.noModelMessage, u = Jl(null), [r, o] = ea(
    a === "top" ? "top" : "side"
  );
  ta(() => {
    if (a !== "auto") {
      o(a);
      return;
    }
    const g = u.current;
    if (!g) return;
    const C = new ResizeObserver((x) => {
      for (const S of x) {
        const k = S.contentRect.width / n;
        o(k < na ? "top" : "side");
      }
    });
    return C.observe(g), () => C.disconnect();
  }, [a, n]);
  const m = Ql(() => ({
    readOnly: c,
    resolvedLabelPosition: r
  }), [c, r]), f = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, _ = s.length === 1 ? s[0] : void 0, b = !!_ && (_.module === la || _.module === aa && ((v = _.state) == null ? void 0 : v.bare) === !0), E = [
    "tlFormLayout",
    c ? "tlFormLayout--readonly" : "",
    b ? "tlFormLayout--flush" : ""
  ].filter(Boolean).join(" ");
  return i ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, i)) : /* @__PURE__ */ e.createElement(Ht.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: l, className: E, style: f, ref: u }, s.map((g, C) => /* @__PURE__ */ e.createElement(K, { key: C, control: g }))));
}, { useCallback: oa } = e, sa = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, ca = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ie(sa), c = t.headerControl ?? null, s = t.headerActions ?? [], i = t.collapsible === !0, u = t.collapsed === !0, r = t.border ?? "none", o = t.fullLine === !0, m = t.children ?? [], p = c != null || s.length > 0 || i, f = oa(() => {
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
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(K, { control: c })), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, s.map((b, E) => /* @__PURE__ */ e.createElement(K, { key: E, control: b })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((b, E) => /* @__PURE__ */ e.createElement(K, { key: E, control: b }))));
}, { useContext: ia, useState: ua, useCallback: da } = e, ma = ({ controlId: l }) => {
  const t = G(), n = ia(Ht), a = t.label ?? "", c = t.required === !0, s = t.error, i = t.errorIcon, u = t.warnings, r = t.warningIcon, o = t.helpText, m = t.dirty === !0, p = t.labelPosition ?? n.resolvedLabelPosition, f = t.fullLine === !0, _ = t.visible !== !1, b = t.hasTooltip === !0, E = t.field, v = n.readOnly, [g, C] = ua(!1), x = da(() => C((I) => !I), []), S = p === "hidden", w = s != null, k = u != null && u.length > 0, h = [
    "tlFormField",
    `tlFormField--${p}`,
    v ? "tlFormField--readonly" : "",
    f ? "tlFormField--fullLine" : "",
    w ? "tlFormField--error" : "",
    !w && k ? "tlFormField--warning" : "",
    m ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: h, style: _ ? void 0 : { display: "none" } }, !S && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": b ? "key:tooltip" : void 0
    },
    a
  ), c && !v && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), m && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), o && !v && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(K, { control: E })), !v && w && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(qe, { image: i, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, s)), !v && !w && k && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, u.map((I, T) => /* @__PURE__ */ e.createElement("div", { key: T, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(qe, { image: r, className: "tlFormField__warningIcon" }), /* @__PURE__ */ e.createElement("span", null, I)))), !v && o && g && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, o));
}, pa = ({ controlId: l }) => {
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
}, fa = 20, ha = () => {
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
    var R;
    const T = window.getSelection();
    T && !T.isCollapsed && I.currentTarget.contains(T.anchorNode) || ((R = m.current) == null || R.focus({ preventScroll: !0 }), t("select", {
      nodeId: h,
      ctrlKey: I.ctrlKey || I.metaKey,
      shiftKey: I.shiftKey
    }));
  }, [t]), b = e.useCallback((h, I) => {
    I.preventDefault(), t("contextMenu", { nodeId: h, x: I.clientX, y: I.clientY });
  }, [t]), E = e.useRef(null), v = e.useCallback((h, I) => {
    const T = I.getBoundingClientRect(), R = h.clientY - T.top, F = T.height / 3;
    return R < F ? "above" : R > F * 2 ? "below" : "within";
  }, []), g = e.useCallback((h, I) => {
    I.dataTransfer.effectAllowed = "move", I.dataTransfer.setData("text/plain", h);
  }, []), C = e.useCallback((h, I) => {
    I.preventDefault(), I.dataTransfer.dropEffect = "move";
    const T = v(I, I.currentTarget);
    E.current != null && window.clearTimeout(E.current), E.current = window.setTimeout(() => {
      t("dragOver", { nodeId: h, position: T }), E.current = null;
    }, 50);
  }, [t, v]), x = e.useCallback((h, I) => {
    I.preventDefault(), E.current != null && (window.clearTimeout(E.current), E.current = null);
    const T = v(I, I.currentTarget);
    t("drop", { nodeId: h, position: T });
  }, [t, v]), S = e.useCallback(() => {
    E.current != null && (window.clearTimeout(E.current), E.current = null), t("dragEnd");
  }, [t]), w = e.useCallback((h) => {
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
            const R = T.depth;
            for (let F = r - 1; F >= 0; F--)
              if (n[F].depth < R) {
                I = F;
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
      onKeyDown: w
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
        style: { paddingLeft: h.depth * fa },
        draggable: c,
        onMouseDown: (T) => {
          (T.shiftKey || T.ctrlKey || T.metaKey || T.detail > 1) && T.preventDefault();
        },
        onClick: (T) => _(h.id, T),
        onContextMenu: (T) => b(h.id, T),
        onDragStart: (T) => g(h.id, T),
        onDragOver: s ? (T) => C(h.id, T) : void 0,
        onDrop: s ? (T) => x(h.id, T) : void 0,
        onDragEnd: S
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
var Lt;
function ba() {
  if (Lt) return Z;
  Lt = 1;
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
  }, E = Object.assign, v = {};
  function g(d, N, V) {
    this.props = d, this.context = N, this.refs = v, this.updater = V || b;
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
  function C() {
  }
  C.prototype = g.prototype;
  function x(d, N, V) {
    this.props = d, this.context = N, this.refs = v, this.updater = V || b;
  }
  var S = x.prototype = new C();
  S.constructor = x, E(S, g.prototype), S.isPureReactComponent = !0;
  var w = Array.isArray;
  function k() {
  }
  var h = { H: null, A: null, T: null, S: null }, I = Object.prototype.hasOwnProperty;
  function T(d, N, V) {
    var W = V.ref;
    return {
      $$typeof: l,
      type: d,
      key: N,
      ref: W !== void 0 ? W : null,
      props: V
    };
  }
  function R(d, N) {
    return T(d.type, N, d.props);
  }
  function F(d) {
    return typeof d == "object" && d !== null && d.$$typeof === l;
  }
  function O(d) {
    var N = { "=": "=0", ":": "=2" };
    return "$" + d.replace(/[=:]/g, function(V) {
      return N[V];
    });
  }
  var D = /\/+/g;
  function B(d, N) {
    return typeof d == "object" && d !== null && d.key != null ? O("" + d.key) : N.toString(36);
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
  function H(d, N, V, W, q) {
    var j = typeof d;
    (j === "undefined" || j === "boolean") && (d = null);
    var ee = !1;
    if (d === null) ee = !0;
    else
      switch (j) {
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
              return ee = d._init, H(
                ee(d._payload),
                N,
                V,
                W,
                q
              );
          }
      }
    if (ee)
      return q = q(d), ee = W === "" ? "." + B(d, 0) : W, w(q) ? (V = "", ee != null && (V = ee.replace(D, "$&/") + "/"), H(q, N, V, "", function(he) {
        return he;
      })) : q != null && (F(q) && (q = R(
        q,
        V + (q.key == null || d && d.key === q.key ? "" : ("" + q.key).replace(
          D,
          "$&/"
        ) + "/") + ee
      )), N.push(q)), 1;
    ee = 0;
    var oe = W === "" ? "." : W + ":";
    if (w(d))
      for (var ne = 0; ne < d.length; ne++)
        W = d[ne], j = oe + B(W, ne), ee += H(
          W,
          N,
          V,
          j,
          q
        );
    else if (ne = _(d), typeof ne == "function")
      for (d = ne.call(d), ne = 0; !(W = d.next()).done; )
        W = W.value, j = oe + B(W, ne++), ee += H(
          W,
          N,
          V,
          j,
          q
        );
    else if (j === "object") {
      if (typeof d.then == "function")
        return H(
          Q(d),
          N,
          V,
          W,
          q
        );
      throw N = String(d), Error(
        "Objects are not valid as a React child (found: " + (N === "[object Object]" ? "object with keys {" + Object.keys(d).join(", ") + "}" : N) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return ee;
  }
  function $(d, N, V) {
    if (d == null) return d;
    var W = [], q = 0;
    return H(d, W, "", "", function(j) {
      return N.call(V, j, q++);
    }), W;
  }
  function A(d) {
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
  var P = typeof reportError == "function" ? reportError : function(d) {
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
    map: $,
    forEach: function(d, N, V) {
      $(
        d,
        function() {
          N.apply(this, arguments);
        },
        V
      );
    },
    count: function(d) {
      var N = 0;
      return $(d, function() {
        N++;
      }), N;
    },
    toArray: function(d) {
      return $(d, function(N) {
        return N;
      }) || [];
    },
    only: function(d) {
      if (!F(d))
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
    var W = E({}, d.props), q = d.key;
    if (N != null)
      for (j in N.key !== void 0 && (q = "" + N.key), N)
        !I.call(N, j) || j === "key" || j === "__self" || j === "__source" || j === "ref" && N.ref === void 0 || (W[j] = N[j]);
    var j = arguments.length - 2;
    if (j === 1) W.children = V;
    else if (1 < j) {
      for (var ee = Array(j), oe = 0; oe < j; oe++)
        ee[oe] = arguments[oe + 2];
      W.children = ee;
    }
    return T(d.type, q, W);
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
    var W, q = {}, j = null;
    if (N != null)
      for (W in N.key !== void 0 && (j = "" + N.key), N)
        I.call(N, W) && W !== "key" && W !== "__self" && W !== "__source" && (q[W] = N[W]);
    var ee = arguments.length - 2;
    if (ee === 1) q.children = V;
    else if (1 < ee) {
      for (var oe = Array(ee), ne = 0; ne < ee; ne++)
        oe[ne] = arguments[ne + 2];
      q.children = oe;
    }
    if (d && d.defaultProps)
      for (W in ee = d.defaultProps, ee)
        q[W] === void 0 && (q[W] = ee[W]);
    return T(d, j, q);
  }, Z.createRef = function() {
    return { current: null };
  }, Z.forwardRef = function(d) {
    return { $$typeof: u, render: d };
  }, Z.isValidElement = F, Z.lazy = function(d) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: d },
      _init: A
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
      var W = d(), q = h.S;
      q !== null && q(V, W), typeof W == "object" && W !== null && typeof W.then == "function" && W.then(k, P);
    } catch (j) {
      P(j);
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
var xt;
function _a() {
  return xt || (xt = 1, nt.exports = ba()), nt.exports;
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
var It;
function ga() {
  if (It) return pe;
  It = 1;
  var l = _a();
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
var Pt;
function va() {
  if (Pt) return tt.exports;
  Pt = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), tt.exports = ga(), tt.exports;
}
var Wt = va();
const { useState: Te, useCallback: fe, useRef: $e, useEffect: Me, useMemo: ut } = e;
function ft({ image: l }) {
  return l ? l.startsWith("/") ? /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" }) : /* @__PURE__ */ e.createElement(qe, { image: l, className: "tlDropdownSelect__optionIcon" }) : null;
}
function Ea({
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
function Ca({
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
const wa = ({ controlId: l, state: t }) => {
  const n = ae(), a = t.value ?? [], c = t.multiSelect === !0, s = t.customOrder === !0, i = t.mandatory === !0, u = t.disabled === !0, r = t.editable !== !1, o = t.optionsLoaded === !0, m = t.options ?? [], p = t.emptyOptionLabel ?? "", f = s && c && !u && r, _ = ie({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), b = _["js.dropdownSelect.nothingFound"], E = fe(
    (M) => _["js.dropdownSelect.removeChip"].replace("{0}", M),
    [_]
  ), [v, g] = Te(!1), [C, x] = Te(""), [S, w] = Te(-1), [k, h] = Te(!1), [I, T] = Te({}), [R, F] = Te(null), [O, D] = Te(null), [B, Q] = Te(null), H = $e(null), $ = $e(null), A = $e(null), P = $e(a);
  P.current = a;
  const X = $e(-1), d = ut(
    () => new Set(a.map((M) => M.value)),
    [a]
  ), N = ut(() => {
    let M = m.filter((Y) => !d.has(Y.value));
    if (C) {
      const Y = C.toLowerCase();
      M = M.filter((le) => le.label.toLowerCase().includes(Y));
    }
    return M;
  }, [m, d, C]);
  Me(() => {
    C && N.length === 1 ? w(0) : w(-1);
  }, [N.length, C]), Me(() => {
    v && o && $.current && $.current.focus();
  }, [v, o, a]), Me(() => {
    var le, se;
    if (X.current < 0) return;
    const M = X.current;
    X.current = -1;
    const Y = (le = H.current) == null ? void 0 : le.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    Y && Y.length > 0 ? Y[Math.min(M, Y.length - 1)].focus() : (se = H.current) == null || se.focus();
  }, [a]), Me(() => {
    if (!v) return;
    const M = (Y) => {
      H.current && !H.current.contains(Y.target) && A.current && !A.current.contains(Y.target) && (g(!1), x(""));
    };
    return document.addEventListener("mousedown", M), () => document.removeEventListener("mousedown", M);
  }, [v]), Me(() => {
    if (!v || !H.current) return;
    const M = H.current.getBoundingClientRect(), Y = window.innerHeight - M.bottom, se = Y < 300 && M.top > Y;
    T({
      left: M.left,
      width: M.width,
      ...se ? { bottom: window.innerHeight - M.top } : { top: M.bottom }
    });
  }, [v]);
  const V = fe(async () => {
    if (!(u || !r) && (g(!0), x(""), w(-1), h(!1), !o))
      try {
        await n("loadOptions");
      } catch {
        h(!0);
      }
  }, [u, r, o, n]), W = fe(() => {
    var M;
    g(!1), x(""), w(-1), (M = H.current) == null || M.focus();
  }, []), q = fe(
    (M) => {
      let Y;
      if (c) {
        const le = m.find((se) => se.value === M);
        if (le)
          Y = [...P.current, le];
        else
          return;
      } else {
        const le = m.find((se) => se.value === M);
        if (le)
          Y = [le];
        else
          return;
      }
      P.current = Y, n(We, { value: Y.map((le) => le.value) }), c ? (x(""), w(-1)) : W();
    },
    [c, m, n, W]
  ), j = fe(
    (M) => {
      X.current = P.current.findIndex((le) => le.value === M);
      const Y = P.current.filter((le) => le.value !== M);
      P.current = Y, n(We, { value: Y.map((le) => le.value) });
    },
    [n]
  ), ee = fe(
    (M) => {
      M.stopPropagation(), n(We, { value: [] }), W();
    },
    [n, W]
  ), oe = fe((M) => {
    x(M.target.value);
  }, []), ne = fe(
    (M) => {
      if (!v) {
        if (M.key === "ArrowDown" || M.key === "ArrowUp" || M.key === "Enter" || M.key === " ") {
          if (M.target.tagName === "BUTTON") return;
          M.preventDefault(), M.stopPropagation(), V();
        }
        return;
      }
      switch (M.key) {
        case "ArrowDown":
          M.preventDefault(), M.stopPropagation(), w(
            (Y) => Y < N.length - 1 ? Y + 1 : 0
          );
          break;
        case "ArrowUp":
          M.preventDefault(), M.stopPropagation(), w(
            (Y) => Y > 0 ? Y - 1 : N.length - 1
          );
          break;
        case "Enter":
          M.preventDefault(), M.stopPropagation(), S >= 0 && S < N.length && q(N[S].value);
          break;
        case "Escape":
          M.preventDefault(), M.stopPropagation(), W();
          break;
        case "Tab":
          W();
          break;
        case "Backspace":
          C === "" && c && a.length > 0 && j(a[a.length - 1].value);
          break;
      }
    },
    [
      v,
      V,
      W,
      N,
      S,
      q,
      C,
      c,
      a,
      j
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
      F(M), Y.dataTransfer.effectAllowed = "move", Y.dataTransfer.setData("text/plain", String(M));
    },
    []
  ), ve = fe(
    (M, Y) => {
      if (Y.preventDefault(), Y.dataTransfer.dropEffect = "move", R === null || R === M) {
        D(null), Q(null);
        return;
      }
      const le = Y.currentTarget.getBoundingClientRect(), se = le.left + le.width / 2, De = Y.clientX < se ? "before" : "after";
      D(M), Q(De);
    },
    [R]
  ), ye = fe(
    (M) => {
      if (M.preventDefault(), R === null || O === null || B === null || R === O) return;
      const Y = [...P.current], [le] = Y.splice(R, 1);
      let se = O;
      R < O ? se = B === "before" ? se - 1 : se : se = B === "before" ? se : se + 1, Y.splice(se, 0, le), P.current = Y, n(We, { value: Y.map((De) => De.value) }), F(null), D(null), Q(null);
    },
    [R, O, B, n]
  ), ke = fe(() => {
    F(null), D(null), Q(null);
  }, []);
  if (Me(() => {
    if (S < 0 || !A.current) return;
    const M = A.current.querySelector(
      `[id="${l}-opt-${S}"]`
    );
    M && M.scrollIntoView({ block: "nearest" });
  }, [S, l]), !r)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, a.map((M) => /* @__PURE__ */ e.createElement("span", { key: M.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(ft, { image: M.image }), /* @__PURE__ */ e.createElement("span", null, M.label))));
  const Ie = !i && a.length > 0 && !u, He = v ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: A,
      className: "tlDropdownSelect__dropdown",
      style: I,
      ...Qt
    },
    (o || k) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: $,
        type: "text",
        className: "tlDropdownSelect__search",
        value: C,
        onChange: oe,
        onKeyDown: ne,
        placeholder: _["js.dropdownSelect.filterPlaceholder"],
        "aria-label": _["js.dropdownSelect.filterPlaceholder"],
        "aria-activedescendant": S >= 0 ? `${l}-opt-${S}` : void 0,
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
        Ca,
        {
          key: M.value,
          id: `${l}-opt-${Y}`,
          option: M,
          highlighted: Y === S,
          searchTerm: C,
          onSelect: q,
          onMouseEnter: () => w(Y)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: H,
      className: "tlDropdownSelect" + (v ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": v,
      "aria-haspopup": "listbox",
      "aria-owns": v ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: v ? void 0 : V,
      onKeyDown: ne
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, a.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : a.map((M, Y) => {
      let le = "";
      return R === Y ? le = "tlDropdownSelect__chip--dragging" : O === Y && B === "before" ? le = "tlDropdownSelect__chip--dropBefore" : O === Y && B === "after" && (le = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        Ea,
        {
          key: M.value,
          option: M,
          removable: !u && (c || !i),
          onRemove: j,
          removeLabel: E(M.label),
          draggable: f,
          onDragStart: f ? (se) => ge(Y, se) : void 0,
          onDragOver: f ? (se) => ve(Y, se) : void 0,
          onDrop: f ? ye : void 0,
          onDragEnd: f ? ke : void 0,
          dragClassName: f ? le : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, Ie && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: ee,
        "aria-label": _["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, v ? "▲" : "▼"))
  ), He && Wt.createPortal(He, document.body));
}, { useCallback: lt, useRef: ya } = e, zt = "application/x-tl-color", ka = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: a,
  onSwap: c,
  onReplace: s
}) => {
  const i = ya(null), u = lt(
    (m) => (p) => {
      i.current = m, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), r = lt((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), o = lt(
    (m) => (p) => {
      p.preventDefault();
      const f = p.dataTransfer.getData(zt);
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
function Vt(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function dt(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function Kt(l) {
  if (!dt(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function Yt(l, t, n) {
  const a = (c) => Vt(c).toString(16).padStart(2, "0");
  return "#" + a(l) + a(t) + a(n);
}
function Sa(l, t, n) {
  const a = l / 255, c = t / 255, s = n / 255, i = Math.max(a, c, s), u = Math.min(a, c, s), r = i - u;
  let o = 0;
  r !== 0 && (i === a ? o = (c - s) / r % 6 : i === c ? o = (s - a) / r + 2 : o = (a - c) / r + 4, o *= 60, o < 0 && (o += 360));
  const m = i === 0 ? 0 : r / i;
  return [o, m, i];
}
function Na(l, t, n) {
  const a = n * t, c = a * (1 - Math.abs(l / 60 % 2 - 1)), s = n - a;
  let i = 0, u = 0, r = 0;
  return l < 60 ? (i = a, u = c, r = 0) : l < 120 ? (i = c, u = a, r = 0) : l < 180 ? (i = 0, u = a, r = c) : l < 240 ? (i = 0, u = c, r = a) : l < 300 ? (i = c, u = 0, r = a) : (i = a, u = 0, r = c), [
    Math.round((i + s) * 255),
    Math.round((u + s) * 255),
    Math.round((r + s) * 255)
  ];
}
function Ta(l) {
  return Sa(...Kt(l));
}
function at(l, t, n) {
  return Yt(...Na(l, t, n));
}
const { useCallback: je, useRef: Mt } = e, Ra = ({ color: l, onColorChange: t }) => {
  const [n, a, c] = Ta(l), s = Mt(null), i = Mt(null), u = je(
    (b, E) => {
      var x;
      const v = (x = s.current) == null ? void 0 : x.getBoundingClientRect();
      if (!v) return;
      const g = Math.max(0, Math.min(1, (b - v.left) / v.width)), C = Math.max(0, Math.min(1, 1 - (E - v.top) / v.height));
      t(at(n, g, C));
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
      var C;
      const E = (C = i.current) == null ? void 0 : C.getBoundingClientRect();
      if (!E) return;
      const g = Math.max(0, Math.min(1, (b - E.top) / E.height)) * 360;
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
function Da(l, t) {
  const n = t.toUpperCase();
  return l.some((a) => a != null && a.toUpperCase() === n);
}
const La = {
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
}, { useState: Ke, useCallback: Ee, useEffect: jt, useRef: xa, useLayoutEffect: Ia } = e, Pa = ({
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
  const [o, m] = Ke("palette"), [p, f] = Ke(t), _ = xa(null), b = ie(La), [E, v] = Ke(null);
  Ia(() => {
    if (!l.current || !_.current) return;
    const A = l.current.getBoundingClientRect(), P = _.current.getBoundingClientRect();
    let X = A.bottom + 4, d = A.left;
    X + P.height > window.innerHeight && (X = A.top - P.height - 4), d + P.width > window.innerWidth && (d = Math.max(0, A.right - P.width)), v({ top: X, left: d });
  }, [l]);
  const g = p != null, [C, x, S] = g ? Kt(p) : [0, 0, 0], [w, k] = Ke((p == null ? void 0 : p.toUpperCase()) ?? "");
  jt(() => {
    k((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), xe(!0, { ESCAPE: u }), jt(() => {
    const A = (X) => {
      _.current && !_.current.contains(X.target) && u();
    }, P = setTimeout(() => document.addEventListener("mousedown", A), 0);
    return () => {
      clearTimeout(P), document.removeEventListener("mousedown", A);
    };
  }, [u]);
  const h = Ee(
    (A) => (P) => {
      const X = parseInt(P.target.value, 10);
      if (isNaN(X)) return;
      const d = Vt(X);
      f(Yt(A === "r" ? d : C, A === "g" ? d : x, A === "b" ? d : S));
    },
    [C, x, S]
  ), I = Ee(
    (A) => {
      if (p != null) {
        A.dataTransfer.setData(zt, p.toUpperCase()), A.dataTransfer.effectAllowed = "move";
        const P = document.createElement("div");
        P.style.width = "33px", P.style.height = "33px", P.style.backgroundColor = p, P.style.borderRadius = "3px", P.style.border = "1px solid rgba(0,0,0,0.1)", P.style.position = "absolute", P.style.top = "-9999px", document.body.appendChild(P), A.dataTransfer.setDragImage(P, 16, 16), requestAnimationFrame(() => document.body.removeChild(P));
      }
    },
    [p]
  ), T = Ee((A) => {
    const P = A.target.value;
    k(P), dt(P) && f(P);
  }, []), R = Ee(() => {
    f(null);
  }, []), F = Ee((A) => {
    f(A);
  }, []), O = Ee(
    (A) => {
      i(A);
    },
    [i]
  ), D = Ee(
    (A, P) => {
      const X = [...n], d = X[A];
      X[A] = X[P], X[P] = d, r(X);
    },
    [n, r]
  ), B = Ee(
    (A, P) => {
      const X = [...n];
      X[A] = P, r(X);
    },
    [n, r]
  ), Q = Ee(() => {
    r([...c]);
  }, [c, r]), H = Ee(
    (A) => {
      if (Da(n, A)) return;
      const P = n.indexOf(null);
      if (P < 0) return;
      const X = [...n];
      X[P] = A.toUpperCase(), r(X);
    },
    [n, r]
  ), $ = Ee(() => {
    p != null && H(p), i(p);
  }, [p, i, H]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: _,
      style: E ? { top: E.top, left: E.left, visibility: "visible" } : { visibility: "hidden" }
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
      ka,
      {
        colors: n,
        columns: a,
        onSelect: F,
        onConfirm: O,
        onSwap: D,
        onReplace: B
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: Q }, b["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(Ra, { color: p ?? "#000000", onColorChange: f }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
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
        value: g ? C : "",
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
        value: g ? S : "",
        onChange: h("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (w !== "" && !dt(w) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: w,
        onChange: T
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, s && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: R }, b["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, b["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: $ }, b["js.colorInput.ok"]))
  );
}, Ma = { "js.colorInput.chooseColor": "Choose color" }, { useState: ja, useCallback: Ye, useRef: Aa } = e, Ba = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), c = ae(), s = ie(Ma), [i, u] = ja(!1), r = Aa(null), o = n, m = t.editable !== !1, p = t.palette ?? [], f = t.paletteColumns ?? 6, _ = t.defaultPalette ?? p, b = Ye(() => {
    m && u(!0);
  }, [m]), E = Ye(
    (C) => {
      u(!1), a(C);
    },
    [a]
  ), v = Ye(() => {
    u(!1);
  }, []), g = Ye(
    (C) => {
      c("paletteChanged", { palette: C });
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
    Pa,
    {
      anchorRef: r,
      currentColor: o,
      palette: p,
      paletteColumns: f,
      defaultPalette: _,
      canReset: t.canReset !== !1,
      onConfirm: E,
      onCancel: v,
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
}, { useState: Ue, useCallback: Le, useEffect: rt, useRef: At, useLayoutEffect: Oa, useMemo: Fa } = e, $a = {
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
}, Ua = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: a,
  onSelect: c,
  onCancel: s,
  onLoadIcons: i
}) => {
  const u = ie($a), [r, o] = Ue("simple"), [m, p] = Ue(""), [f, _] = Ue(t ?? ""), [b, E] = Ue(!1), [v, g] = Ue(null), C = At(null), x = At(null);
  Oa(() => {
    if (!l.current || !C.current) return;
    const O = l.current.getBoundingClientRect(), D = C.current.getBoundingClientRect();
    let B = O.bottom + 4, Q = O.left;
    B + D.height > window.innerHeight && (B = O.top - D.height - 4), Q + D.width > window.innerWidth && (Q = Math.max(0, O.right - D.width)), g({ top: B, left: Q });
  }, [l]), rt(() => {
    !a && !b && i().catch(() => E(!0));
  }, [a, b, i]), rt(() => {
    a && x.current && x.current.focus();
  }, [a]), xe(!0, { ESCAPE: s }), rt(() => {
    const O = (B) => {
      C.current && !C.current.contains(B.target) && s();
    }, D = setTimeout(() => document.addEventListener("mousedown", O), 0);
    return () => {
      clearTimeout(D), document.removeEventListener("mousedown", O);
    };
  }, [s]);
  const S = Fa(() => {
    if (!m) return n;
    const O = m.toLowerCase();
    return n.filter(
      (D) => D.prefix.toLowerCase().includes(O) || D.label.toLowerCase().includes(O) || D.terms != null && D.terms.some((B) => B.includes(O))
    );
  }, [n, m]), w = Le((O) => {
    p(O.target.value);
  }, []), k = Le(
    (O) => {
      c(O);
    },
    [c]
  ), h = Le((O) => {
    _(O);
  }, []), I = Le((O) => {
    _(O.target.value);
  }, []), T = Le(() => {
    c(f || null);
  }, [f, c]), R = Le(() => {
    c(null);
  }, [c]), F = Le(async (O) => {
    O.preventDefault(), E(!1);
    try {
      await i();
    } catch {
      E(!0);
    }
  }, [i]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlIconSelect__popup",
      ref: C,
      style: v ? { top: v.top, left: v.left, visibility: "visible" } : { visibility: "hidden" }
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
        onChange: w,
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
      b && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: F }, u["js.iconSelect.loadError"])),
      a && S.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      a && S.map(
        (O) => O.variants.map((D) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: D.encoded,
            className: "tlIconSelect__iconCell" + (D.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": D.encoded === t,
            tabIndex: 0,
            title: O.label,
            onClick: () => r === "simple" ? k(D.encoded) : h(D.encoded),
            onKeyDown: (B) => {
              (B.key === "Enter" || B.key === " ") && (B.preventDefault(), r === "simple" ? k(D.encoded) : h(D.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(we, { encoded: D.encoded })
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
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: s }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: R }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: T }, u["js.iconSelect.ok"]))
  );
}, Ha = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: Wa, useCallback: Ge, useRef: za } = e, Va = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), c = ae(), s = ie(Ha), [i, u] = Wa(!1), r = za(null), o = n, m = t.editable !== !1, p = t.disabled === !0, f = t.icons ?? [], _ = t.iconsLoaded === !0, b = Ge(() => {
    m && !p && u(!0);
  }, [m, p]), E = Ge(
    (C) => {
      u(!1), a(C);
    },
    [a]
  ), v = Ge(() => {
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
    Ua,
    {
      anchorRef: r,
      currentValue: o,
      icons: f,
      iconsLoaded: _,
      onSelect: E,
      onCancel: v,
      onLoadIcons: g
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, o ? /* @__PURE__ */ e.createElement(we, { encoded: o }) : null));
}, { useCallback: Ae, useEffect: Ka, useMemo: Bt, useRef: Ya, useState: ot } = e, Ga = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, Xa = [1, 2, 3, 4];
function qa(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const a = parseFloat(n[1]), c = n[2] || "px";
  return c === "rem" || c === "em" ? a * t : a;
}
function Za(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let a = 1;
  for (const c of Xa)
    n >= c && (a = c);
  return a;
}
function Qa(l, t) {
  const n = Ga[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function Ja(l, t) {
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
        for (let E = f.colEnd; E < _; E++) s(b, E);
      f.colEnd = _;
    }
  };
  for (const p of l) {
    const f = n <= 1 ? 1 : Math.max(1, p.rowSpan || 1);
    let _ = Math.min(Qa(p.width, n), n);
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
    const E = r, v = r + _, g = u, C = u + f;
    i.push({ id: p.id, colStart: E, colEnd: v, rowStart: g, rowEnd: C });
    for (let x = g; x < C; x++)
      for (let S = E; S < v; S++) s(x, S);
    r = v, r >= n && (r = 0, u++);
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
const er = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.minColWidth ?? "16rem", c = (t.children ?? []).filter((k) => k && k.id), s = Ya(null), [i, u] = ot(1), r = t.editMode === !0;
  Ka(() => {
    const k = s.current;
    if (!k) return;
    const h = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, I = qa(a, h), T = () => u(Za(k.clientWidth, I));
    T();
    const R = new ResizeObserver(T);
    return R.observe(k), () => R.disconnect();
  }, [a]);
  const o = Bt(() => Ja(c, i), [c, i]), m = Bt(() => {
    const k = {};
    for (const h of o) k[h.id] = h;
    return k;
  }, [o]), [p, f] = ot(null), [_, b] = ot(null), E = Ae((k, h) => {
    if (!r) {
      k.preventDefault();
      return;
    }
    f(h), k.dataTransfer.effectAllowed = "move", k.dataTransfer.setData("text/plain", h);
  }, [r]), v = Ae((k, h) => {
    if (!r || !p || p === h) return;
    k.preventDefault(), k.dataTransfer.dropEffect = "move";
    const I = k.currentTarget.getBoundingClientRect(), T = k.clientX < I.left + I.width / 2;
    b((R) => R && R.id === h && R.before === T ? R : { id: h, before: T });
  }, [r, p]), g = Ae(() => {
  }, []), C = Ae((k, h, I) => {
    const T = c.map((D) => D.id), R = T.indexOf(k);
    if (R < 0) return;
    T.splice(R, 1);
    const F = T.indexOf(h);
    if (F < 0) {
      T.splice(R, 0, k);
      return;
    }
    const O = I ? F : F + 1;
    T.splice(O, 0, k), n("reorder", { order: T });
  }, [c, n]), x = Ae((k, h) => {
    if (!r || !p || p === h) return;
    k.preventDefault();
    const I = k.currentTarget.getBoundingClientRect(), T = k.clientX < I.left + I.width / 2;
    C(p, h, T), f(null), b(null);
  }, [r, p, C]), S = Ae(() => {
    f(null), b(null);
  }, []), w = {
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: w }, c.map((k) => {
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
          onDragStart: (R) => E(R, k.id),
          onDragOver: (R) => v(R, k.id),
          onDragLeave: g,
          onDrop: (R) => x(R, k.id),
          onDragEnd: S
        },
        /* @__PURE__ */ e.createElement(K, { control: k.control }),
        r && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: tr, useRef: Ot, useState: Ft, useEffect: nr, useLayoutEffect: lr } = e, ar = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, a) => /* @__PURE__ */ e.createElement("span", { key: a, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(K, { control: n }))));
}, rr = ({ group: l }) => {
  var p, f;
  const [t, n] = Ft(!1), [a, c] = Ft({}), s = Ot(null), i = Ot(null), u = tr(() => {
    n((_) => !_);
  }, []);
  lr(() => {
    if (!t) return;
    const _ = () => {
      const b = s.current;
      if (!b) return;
      const E = b.getBoundingClientRect();
      c({
        position: "fixed",
        top: E.bottom + 4,
        right: Math.max(8, window.innerWidth - E.right),
        left: "auto"
      });
    };
    return _(), window.addEventListener("resize", _), window.addEventListener("scroll", _, !0), () => {
      window.removeEventListener("resize", _), window.removeEventListener("scroll", _, !0);
    };
  }, [t]), nr(() => {
    if (!t) return;
    const _ = (b) => {
      i.current && !i.current.contains(b.target) && s.current && !s.current.contains(b.target) && n(!1);
    };
    return document.addEventListener("mousedown", _), () => document.removeEventListener("mousedown", _);
  }, [t]), xe(t, { ESCAPE: () => n(!1) }), pt(t, i, "first");
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
  ), Wt.createPortal(
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
      (f = l.subGroups) == null ? void 0 : f.map((_, b) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${b}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), _.items.map((E, v) => /* @__PURE__ */ e.createElement("div", { key: v, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(K, { control: E })))))
    ),
    document.body
  ));
}, or = ({ controlId: l }) => {
  const a = (G().groups ?? []).filter((c) => c.items.some((s) => s != null));
  return a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, a.map((c, s) => /* @__PURE__ */ e.createElement(e.Fragment, { key: c.name }, s > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), c.display === "menu" ? /* @__PURE__ */ e.createElement(rr, { group: c }) : /* @__PURE__ */ e.createElement(ar, { group: c }))));
}, sr = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlTileStack", style: { width: "100%", height: "100%" } }, t.frame && /* @__PURE__ */ e.createElement(K, { control: t.frame }));
}, cr = ({ controlId: l }) => {
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
}, ir = ({ controlId: l }) => {
  const n = G().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((a, c) => /* @__PURE__ */ e.createElement(K, { key: c, control: a })));
}, ur = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), dr = {
  "js.sidebar.openDrawer": "Open navigation"
}, mr = ({ controlId: l }) => {
  const t = ae(), n = ie(dr);
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
U("TLButton", fn);
U("TLUploadButton", hn);
U("TLToggleButton", _n);
U("TLTextInput", tn);
U("TLPasswordInput", ln);
U("TLNumberInput", rn);
U("TLDatePicker", sn);
U("TLSelect", un);
U("TLCheckbox", mn);
U("TLCounter", gn);
U("TLTabBar", En);
U("TLFieldList", Cn);
U("TLAudioRecorder", yn);
U("TLAudioPlayer", Sn);
U("TLFileUpload", Tn);
U("TLBinaryField", Dn);
U("TLFileChips", In);
U("TLRelativeTime", jn);
U("TLAnchor", An);
U("TLScrollLink", Bn);
U("TLAvatar", $n);
U("TLDownload", Hn);
U("TLPhotoCapture", zn);
U("TLPhotoViewer", Kn);
U("TLPdfViewer", Gn);
U("TLSplitPanel", Xn);
U("TLPanel", nl);
U("TLInset", fl);
U("TLMaximizeRoot", ll);
U("TLDeckPane", al);
U("TLSidebar", ml);
U("TLStack", pl);
U("TLGrid", hl);
U("TLCard", bl);
U("TLAppBar", _l);
U("TLBreadcrumb", vl);
U("TLBottomBar", Cl);
U("TLDialog", kl);
U("TLDialogManager", Tl);
U("TLWindow", xl);
U("TLDrawer", Ml);
U("TLContextMenuRegion", Al);
U("TLSnackbar", $l);
U("TLMenu", Hl);
U("TLAppShell", zl);
U("TLText", Vl);
U("TLTableView", Xl);
U("TLColumnSelect", ql);
U("TLFormLayout", ra);
U("TLFormGroup", ca);
U("TLFormField", ma);
U("TLResourceCell", pa);
U("TLTreeView", ha);
U("TLDropdownSelect", wa);
U("TLColorInput", Ba);
U("TLIconSelect", Va);
U("TLDashboard", er);
U("TLToolbar", or);
U("TLTileStack", sr);
U("TLAdaptiveDetail", cr);
U("TLSlot", ir);
U("TLSlotContent", ur);
U("TLDrawerToggle", mr);
