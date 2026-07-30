import { React as e, useTLFieldValue as Re, useTLCommand as re, useTLState as G, useKeyboardBinding as de, useTLUpload as Fe, TLChild as K, useI18N as ie, useTLDataUrl as Oe, scrollToAnchor as Xt, useStandaloneKeyboardScope as Le, KeyboardScopeProvider as mt, useFocusTrap as pt, CMD_VALUE_CHANGED as We, anchoredOverlayProps as qt, register as U } from "tl-react-bridge";
const { useCallback: _t, useRef: Zt } = e, Qt = 300, Jt = ({ controlId: l, state: t }) => {
  const [n, r, c] = Re({ debounceMs: Qt }), s = re(), i = Zt(!1), u = _t(
    (w) => {
      i.current = !0, r(w.target.value);
    },
    [r]
  ), a = t.commitOnBlur === !0, o = _t(async () => {
    await c(), a && i.current && (i.current = !1, s("commit"));
  }, [c, a, s]), m = t.multiline === !0;
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
}, { useCallback: vt } = e, en = 300, tn = ({ controlId: l, state: t }) => {
  const [n, r, c] = Re({ debounceMs: en }), s = vt(
    (p) => {
      r(p.target.value);
    },
    [r]
  ), i = vt(() => {
    c();
  }, [c]);
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, "••••••••");
  const u = t.hasError === !0, a = t.hasWarnings === !0, o = t.errorMessage, m = [
    "tlReactTextInput",
    u ? "tlReactTextInput--error" : "",
    !u && a ? "tlReactTextInput--warning" : ""
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
}, { useCallback: gt } = e, nn = 300, ln = ({ controlId: l, state: t, config: n }) => {
  const [r, c, s] = Re({ debounceMs: nn }), i = gt(
    (f) => {
      const _ = f.target.value;
      c(_ === "" ? null : _);
    },
    [c]
  ), u = gt(() => {
    s();
  }, [s]);
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, r != null ? String(r) : "");
  const a = t.hasError === !0, o = t.hasWarnings === !0, m = t.errorMessage, p = [
    "tlReactNumberInput",
    a ? "tlReactNumberInput--error" : "",
    !a && o ? "tlReactNumberInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      inputMode: n != null && n.decimal ? "decimal" : "numeric",
      value: r != null ? String(r) : "",
      onChange: i,
      onBlur: u,
      disabled: t.disabled === !0,
      className: p,
      "aria-invalid": a || void 0,
      title: a && m ? m : void 0
    }
  ));
}, { useCallback: rn } = e, an = ({ controlId: l, state: t }) => {
  const [n, r] = Re(), c = rn(
    (a) => {
      r(a.target.value || null);
    },
    [r]
  );
  if (t.editable === !1) {
    const a = t.displayValue ?? n ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, a);
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
}, { useCallback: on } = e, sn = ({ controlId: l, state: t, config: n }) => {
  var m;
  const [r, c] = Re(), s = on(
    (p) => {
      c(p.target.value || null);
    },
    [c]
  ), i = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const p = ((m = i.find((f) => f.value === r)) == null ? void 0 : m.label) ?? "";
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
      onChange: s,
      disabled: t.disabled === !0,
      className: o,
      "aria-invalid": u || void 0
    },
    t.nullable !== !1 && /* @__PURE__ */ e.createElement("option", { value: "" }),
    i.map((p) => /* @__PURE__ */ e.createElement("option", { key: p.value, value: p.value }, p.label))
  ));
}, { useCallback: cn } = e, un = ({ controlId: l, state: t }) => {
  const [n, r] = Re(), c = cn(
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
const { useCallback: dn } = e, mn = ({ controlId: l, command: t, label: n, image: r, disabled: c, displayMode: s }) => {
  const i = G(), u = re(), a = t ?? "click", o = n ?? i.label, m = r ?? i.image, p = c ?? i.disabled === !0, f = s ?? i.displayMode ?? "label-only", _ = i.hidden === !0, b = i.tooltip, w = i.appearance, E = i.size, v = i.navigateUrl, g = dn(() => {
    if (v) {
      window.location.assign(v);
      return;
    }
    u(a);
  }, [u, a, v]), x = i.keyGesture;
  de(x, () => p || _ ? !1 : (g(), !0));
  const L = f === "icon-only", C = f === "label-only" || f === "icon-label" || L && !m, k = b ?? (L ? o : void 0), h = k ? `text:${k}` : void 0;
  return _ ? null : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: g,
      disabled: p,
      className: "tlReactButton" + (L ? " tlReactButton--iconOnly" : "") + (f === "label-only" ? " tlReactButton--labelOnly" : "") + (w === "link" ? " tlReactButton--link" : "") + (w === "primary" ? " tlReactButton--primary" : "") + (E === "small" ? " tlReactButton--small" : "") + (E === "large" ? " tlReactButton--large" : ""),
      "data-tooltip": h,
      "aria-label": m || L ? o : void 0
    },
    m && /* @__PURE__ */ e.createElement(we, { encoded: m, className: "tlReactButton__image" }),
    C && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, o)
  );
}, pn = ({ controlId: l }) => {
  const t = G(), n = Fe(), r = e.useRef(null), [c, s] = e.useState(!1), i = t.label ?? "", u = t.image, a = t.disabled === !0, o = t.hidden === !0, m = t.displayMode ?? "label-only", p = t.appearance, f = t.accept, _ = t.multiple === !0, b = e.useCallback(() => {
    var L;
    a || c || (L = r.current) == null || L.click();
  }, [a, c]), w = e.useCallback(async (L) => {
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
  }, [n]), E = m === "icon-only", v = m === "icon-only" || m === "icon-label", g = m === "label-only" || m === "icon-label" || E && !u, x = a || c;
  return /* @__PURE__ */ e.createElement("span", { id: l, style: { display: "contents" } }, /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: r,
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
    v && u && /* @__PURE__ */ e.createElement(we, { encoded: u, className: "tlReactButton__image" }),
    g && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, i)
  ));
}, { useCallback: fn } = e, hn = ({ controlId: l, command: t, label: n, active: r, disabled: c }) => {
  const s = G(), i = re(), u = t ?? "click", a = n ?? s.label, o = r ?? s.active === !0, m = c ?? s.disabled === !0, p = fn(() => {
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
    a
  );
}, bn = ({ controlId: l }) => {
  const t = G(), n = re(), r = t.count ?? 0, c = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, r), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: _n } = e, vn = ({ controlId: l }) => {
  const t = G(), n = re(), r = t.tabs ?? [], c = t.activeTabId, s = _n((i) => {
    i !== c && n("selectTab", { tabId: i });
  }, [n, c]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, r.map((i) => /* @__PURE__ */ e.createElement(
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
}, gn = ({ controlId: l }) => {
  const t = G(), n = t.title, r = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, r.map((c, s) => /* @__PURE__ */ e.createElement("div", { key: s, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(K, { control: c })))));
}, En = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Cn = ({ controlId: l }) => {
  const t = G(), n = Fe(), [r, c] = e.useState("idle"), [s, i] = e.useState(null), u = e.useRef(null), a = e.useRef([]), o = e.useRef(null), m = t.status ?? "idle", p = t.error, f = m === "received" ? "idle" : r !== "idle" ? r : m, _ = e.useCallback(async () => {
    if (r === "recording") {
      const g = u.current;
      g && g.state !== "inactive" && g.stop();
      return;
    }
    if (r !== "uploading") {
      if (i(null), !window.isSecureContext || !navigator.mediaDevices) {
        i("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const g = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        o.current = g, a.current = [];
        const x = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", L = new MediaRecorder(g, x ? { mimeType: x } : void 0);
        u.current = L, L.ondataavailable = (C) => {
          C.data.size > 0 && a.current.push(C.data);
        }, L.onstop = async () => {
          g.getTracks().forEach((h) => h.stop()), o.current = null;
          const C = new Blob(a.current, { type: L.mimeType || "audio/webm" });
          if (a.current = [], C.size === 0) {
            c("idle");
            return;
          }
          c("uploading");
          const k = new FormData();
          k.append("audio", C, "recording.webm"), await n(k), c("idle");
        }, L.start(), c("recording");
      } catch (g) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", g), i("js.audioRecorder.error.denied"), c("idle");
      }
    }
  }, [r, n]), b = ie(En), w = f === "recording" ? b["js.audioRecorder.stop"] : f === "uploading" ? b["js.uploading"] : b["js.audioRecorder.record"], E = f === "uploading", v = ["tlAudioRecorder__button"];
  return f === "recording" && v.push("tlAudioRecorder__button--recording"), f === "uploading" && v.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: v.join(" "),
      onClick: _,
      disabled: E,
      title: w,
      "aria-label": w
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${f === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, b[s]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, wn = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, yn = ({ controlId: l }) => {
  const t = G(), n = Oe(), r = !!t.hasAudio, c = t.dataRevision ?? 0, [s, i] = e.useState(r ? "idle" : "disabled"), u = e.useRef(null), a = e.useRef(null), o = e.useRef(c);
  e.useEffect(() => {
    r ? s === "disabled" && i("idle") : (u.current && (u.current.pause(), u.current = null), a.current && (URL.revokeObjectURL(a.current), a.current = null), i("disabled"));
  }, [r]), e.useEffect(() => {
    c !== o.current && (o.current = c, u.current && (u.current.pause(), u.current = null), a.current && (URL.revokeObjectURL(a.current), a.current = null), (s === "playing" || s === "paused" || s === "loading") && i("idle"));
  }, [c]), e.useEffect(() => () => {
    u.current && (u.current.pause(), u.current = null), a.current && (URL.revokeObjectURL(a.current), a.current = null);
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
    if (!a.current) {
      i("loading");
      try {
        const E = await fetch(n);
        if (!E.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", E.status), i("idle");
          return;
        }
        const v = await E.blob();
        a.current = URL.createObjectURL(v);
      } catch (E) {
        console.error("[TLAudioPlayer] Fetch error:", E), i("idle");
        return;
      }
    }
    const w = new Audio(a.current);
    u.current = w, w.onended = () => {
      i("idle");
    }, w.play(), i("playing");
  }, [s, n]), p = ie(wn), f = s === "loading" ? p["js.loading"] : s === "playing" ? p["js.audioPlayer.pause"] : s === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], _ = s === "disabled" || s === "loading", b = ["tlAudioPlayer__button"];
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
}, kn = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Sn = ({ controlId: l }) => {
  const t = G(), n = Fe(), [r, c] = e.useState("idle"), [s, i] = e.useState(!1), u = e.useRef(null), a = t.status ?? "idle", o = t.error, m = t.accept ?? "", p = a === "received" ? "idle" : r !== "idle" ? r : a, f = e.useCallback(async (C) => {
    c("uploading");
    const k = new FormData();
    k.append("file", C, C.name), await n(k), c("idle");
  }, [n]), _ = e.useCallback((C) => {
    var h;
    const k = (h = C.target.files) == null ? void 0 : h[0];
    k && f(k);
  }, [f]), b = e.useCallback(() => {
    var C;
    r !== "uploading" && ((C = u.current) == null || C.click());
  }, [r]), w = e.useCallback((C) => {
    C.preventDefault(), C.stopPropagation(), i(!0);
  }, []), E = e.useCallback((C) => {
    C.preventDefault(), C.stopPropagation(), i(!1);
  }, []), v = e.useCallback((C) => {
    var h;
    if (C.preventDefault(), C.stopPropagation(), i(!1), r === "uploading") return;
    const k = (h = C.dataTransfer.files) == null ? void 0 : h[0];
    k && f(k);
  }, [r, f]), g = p === "uploading", x = ie(kn), L = p === "uploading" ? x["js.uploading"] : x["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${s ? " tlFileUpload--dragover" : ""}`,
      onDragOver: w,
      onDragLeave: E,
      onDrop: v
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
        disabled: g,
        title: L,
        "aria-label": L
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    o && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, o)
  );
}, Nn = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, Tn = ({ controlId: l, state: t }) => {
  const r = G() ?? t ?? {}, c = Fe(), s = Oe(), i = ie(Nn), u = r.editable !== !1, a = !!r.hasData, o = r.fileName ?? "download", m = r.dataRevision ?? 0, p = r.accept ?? "", f = r.status ?? "idle", _ = r.error ?? null, [b, w] = e.useState("idle"), [E, v] = e.useState(!1), [g, x] = e.useState(!1), L = e.useRef(null), C = e.useCallback(async () => {
    if (!(!a || g)) {
      x(!0);
      try {
        const O = s + (s.includes("?") ? "&" : "?") + "rev=" + m, M = await fetch(O);
        if (!M.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", M.status);
          return;
        }
        const j = await M.blob(), X = URL.createObjectURL(j), d = document.createElement("a");
        d.href = X, d.download = o, d.style.display = "none", document.body.appendChild(d), d.click(), document.body.removeChild(d), URL.revokeObjectURL(X);
      } catch (O) {
        console.error("[TLBinaryField] Fetch error:", O);
      } finally {
        x(!1);
      }
    }
  }, [a, g, s, m, o]), k = e.useCallback(async (O) => {
    w("uploading");
    const M = new FormData();
    M.append("file", O, O.name), await c(M), w("idle");
  }, [c]), h = (f === "received" ? "idle" : b !== "idle" ? b : f) === "uploading", I = e.useCallback((O) => {
    var j;
    const M = (j = O.target.files) == null ? void 0 : j[0];
    M && k(M);
  }, [k]), T = e.useCallback(() => {
    var O;
    h || (O = L.current) == null || O.click();
  }, [h]), S = e.useCallback((O) => {
    O.preventDefault(), O.stopPropagation(), v(!0);
  }, []), H = e.useCallback((O) => {
    O.preventDefault(), O.stopPropagation(), v(!1);
  }, []), B = e.useCallback((O) => {
    var j;
    if (O.preventDefault(), O.stopPropagation(), v(!1), h) return;
    const M = (j = O.dataTransfer.files) == null ? void 0 : j[0];
    M && k(M);
  }, [h, k]), R = g ? i["js.downloading"] : i["js.download.file"].replace("{0}", o), F = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (g ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: C,
      disabled: g,
      title: R,
      "aria-label": R
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o));
  if (!u)
    return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, F) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, i["js.download.noFile"]));
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
    a && F,
    _ && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, _)
  );
}, Rn = {
  "js.fileChips.add": "Add file",
  "js.fileChips.remove": "Remove {0}",
  "js.uploading": "Uploading…",
  "js.download.file": "Download {0}"
};
function Dn(l) {
  return l < 1024 ? l + " B" : l < 1024 * 1024 ? (l / 1024).toFixed(l < 10 * 1024 ? 1 : 0) + " KB" : (l / (1024 * 1024)).toFixed(1) + " MB";
}
const Ln = ({ controlId: l }) => {
  const t = G(), n = re(), r = Fe(), c = Oe(), s = ie(Rn), i = t.chips ?? [], u = t.editable === !0, [a, o] = e.useState(!1), [m, p] = e.useState(!1), f = e.useRef(null), _ = e.useCallback(async (C) => {
    const k = Array.from(C);
    if (k.length !== 0) {
      o(!0);
      try {
        const h = new FormData();
        for (const I of k)
          h.append("file", I, I.name);
        await r(h);
      } finally {
        o(!1);
      }
    }
  }, [r]), b = e.useCallback(async (C) => {
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
    a || (C = f.current) == null || C.click();
  }, [a]), v = e.useCallback((C) => {
    u && (C.preventDefault(), C.stopPropagation(), p(!0));
  }, [u]), g = e.useCallback((C) => {
    u && (C.preventDefault(), C.stopPropagation(), p(!1));
  }, [u]), x = e.useCallback((C) => {
    u && (C.preventDefault(), C.stopPropagation(), p(!1), !a && C.dataTransfer.files && _(C.dataTransfer.files));
  }, [u, a, _]), L = [
    "tlFileChips",
    u ? "tlFileChips--editable" : "",
    m ? "tlFileChips--dragover" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: L,
      onDragOver: v,
      onDragLeave: g,
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
        C.size != null && /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__size" }, Dn(C.size))
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
        className: "tlFileChips__add" + (a ? " tlFileChips__add--uploading" : ""),
        onClick: E,
        disabled: a,
        title: a ? s["js.uploading"] : s["js.fileChips.add"]
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
      /* @__PURE__ */ e.createElement("span", null, a ? s["js.uploading"] : s["js.fileChips.add"])
    ))
  );
}, xn = 3e4;
function In(l, t) {
  const n = Math.round((l - Date.now()) / 1e3), r = Math.abs(n), c = new Intl.RelativeTimeFormat(t, { numeric: "auto" });
  return r < 60 ? c.format(Math.trunc(n / 1), "second") : r < 3600 ? c.format(Math.trunc(n / 60), "minute") : r < 86400 ? c.format(Math.trunc(n / 3600), "hour") : r < 7 * 86400 ? c.format(Math.trunc(n / 86400), "day") : new Date(l).toLocaleDateString(t);
}
const Mn = ({ controlId: l }) => {
  const t = G(), n = t.timestamp, r = t.label ?? void 0, c = t.locale || navigator.language, [, s] = e.useState(0);
  return e.useEffect(() => {
    const i = setInterval(() => s((u) => u + 1), xn);
    return () => clearInterval(i);
  }, []), n == null ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime tlRelativeTime--empty" }) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime", title: r }, In(n, c));
}, Pn = ({ controlId: l }) => {
  const t = G(), n = t.anchor ?? void 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAnchor", "data-tl-anchor": n }, t.child && /* @__PURE__ */ e.createElement(K, { control: t.child }));
}, jn = ({ controlId: l }) => {
  const t = G(), n = t.target, r = t.label ?? "";
  if (n == null)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlScrollLink tlScrollLink--empty" });
  const c = (s) => {
    s.preventDefault(), Xt(n);
  };
  return /* @__PURE__ */ e.createElement("a", { id: l, className: "tlScrollLink", href: "#", onClick: c }, r);
};
function An(l) {
  const t = l.trim().split(/\s+/).filter(Boolean);
  return t.length === 0 ? "?" : t.length === 1 ? t[0].slice(0, 2).toUpperCase() : (t[0][0] + t[t.length - 1][0]).toUpperCase();
}
function Bn(l) {
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return Math.abs(t) % 360;
}
const Fn = ({ controlId: l }) => {
  const n = G().name;
  return n ? /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlAvatar",
      style: { backgroundColor: `hsl(${Bn(n)}, 45%, 45%)` },
      title: n,
      "aria-label": n
    },
    An(n)
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlAvatar tlAvatar--empty" });
}, On = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, $n = ({ controlId: l }) => {
  const t = G(), n = Oe(), r = re(), c = !!t.hasData, s = t.dataRevision ?? 0, i = t.fileName ?? "download", u = !!t.clearable, [a, o] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!c || a)) {
      o(!0);
      try {
        const b = n + (n.includes("?") ? "&" : "?") + "rev=" + s, w = await fetch(b);
        if (!w.ok) {
          console.error("[TLDownload] Failed to fetch data:", w.status);
          return;
        }
        const E = await w.blob(), v = URL.createObjectURL(E), g = document.createElement("a");
        g.href = v, g.download = i, g.style.display = "none", document.body.appendChild(g), g.click(), document.body.removeChild(g), URL.revokeObjectURL(v);
      } catch (b) {
        console.error("[TLDownload] Fetch error:", b);
      } finally {
        o(!1);
      }
    }
  }, [c, a, n, s, i]), p = e.useCallback(async () => {
    c && await r("clear");
  }, [c, r]), f = ie(On);
  if (!c)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, f["js.download.noFile"]));
  const _ = a ? f["js.downloading"] : f["js.download.file"].replace("{0}", i);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (a ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: m,
      disabled: a,
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
}, Un = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Hn = ({ controlId: l }) => {
  const t = G(), n = Fe(), [r, c] = e.useState("idle"), [s, i] = e.useState(null), [u, a] = e.useState(!1), o = e.useRef(null), m = e.useRef(null), p = e.useRef(null), f = e.useRef(null), _ = e.useRef(null), b = t.error, w = e.useMemo(
    () => {
      var S;
      return !!(window.isSecureContext && ((S = navigator.mediaDevices) != null && S.getUserMedia));
    },
    []
  ), E = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((S) => S.stop()), m.current = null), o.current && (o.current.srcObject = null);
  }, []), v = e.useCallback(() => {
    E(), c("idle");
  }, [E]), g = e.useCallback(async () => {
    var S;
    if (r !== "uploading") {
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
  }, [r, w]), x = e.useCallback(async () => {
    if (r !== "overlayOpen")
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
  }, [r, n, E]), L = e.useCallback(async (S) => {
    var R;
    const H = (R = S.target.files) == null ? void 0 : R[0];
    if (!H) return;
    c("uploading");
    const B = new FormData();
    B.append("photo", H, H.name), await n(B), c("idle"), f.current && (f.current.value = "");
  }, [n]);
  e.useEffect(() => {
    r === "overlayOpen" && o.current && m.current && (o.current.srcObject = m.current);
  }, [r]), e.useEffect(() => {
    var H;
    if (r !== "overlayOpen") return;
    (H = _.current) == null || H.focus();
    const S = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = S;
    };
  }, [r]), Le(r === "overlayOpen", { ESCAPE: v }), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((S) => S.stop()), m.current = null);
  }, []);
  const C = ie(Un), k = r === "uploading" ? C["js.uploading"] : C["js.photoCapture.open"], h = ["tlPhotoCapture__cameraBtn"];
  r === "uploading" && h.push("tlPhotoCapture__cameraBtn--uploading");
  const I = ["tlPhotoCapture__overlayVideo"];
  u && I.push("tlPhotoCapture__overlayVideo--mirrored");
  const T = ["tlPhotoCapture__mirrorBtn"];
  return u && T.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: h.join(" "),
      onClick: g,
      disabled: r === "uploading",
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
  ), /* @__PURE__ */ e.createElement("canvas", { ref: p, style: { display: "none" } }), r === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: _,
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
        onClick: () => a((S) => !S),
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
        onClick: v,
        title: C["js.photoCapture.close"],
        "aria-label": C["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, C[s]), b && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, b));
}, Wn = {
  "js.photoViewer.alt": "Captured photo"
}, zn = ({ controlId: l }) => {
  const t = G(), n = Oe(), r = !!t.hasPhoto, c = t.dataRevision ?? 0, [s, i] = e.useState(null), u = e.useRef(c);
  e.useEffect(() => {
    if (!r) {
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
  }, [r, c, n]), e.useEffect(() => () => {
    s && URL.revokeObjectURL(s);
  }, []);
  const a = ie(Wn);
  return !r || !s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: s,
      alt: a["js.photoViewer.alt"]
    }
  ));
}, Vn = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, Kn = ({ controlId: l }) => {
  const t = G(), n = Oe(), r = !!t.hasPdf, c = t.dataRevision ?? 0, s = ie(Vn), u = n.indexOf("react-api/"), a = u >= 0 ? n.slice(0, u) : n, o = n + "&rev=" + c, m = a + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(o);
  return r ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: m,
      title: s["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, s["js.pdfViewer.noDocument"]));
}, { useCallback: Et, useRef: Qe } = e, Yn = ({ controlId: l }) => {
  const t = G(), n = re(), r = t.orientation, c = t.resizable === !0, s = t.children ?? [], i = r === "horizontal", u = s.length > 0 && s.every((E) => E.collapsed), a = !u && s.some((E) => E.collapsed), o = u ? !i : i, m = Qe(null), p = Qe(null), f = Qe(null), _ = Et((E, v) => {
    const g = {
      overflow: E.scrolling || "auto"
    };
    return E.collapsed ? u && !o ? g.flex = "1 0 0%" : g.flex = "0 0 auto" : v !== void 0 ? g.flex = `0 0 ${v}px` : g.flex = `${E.size} 1 0%`, E.minSize > 0 && !E.collapsed && (g.minWidth = i ? E.minSize : void 0, g.minHeight = i ? void 0 : E.minSize), g;
  }, [i, u, a, o]), b = Et((E, v) => {
    E.preventDefault();
    const g = m.current;
    if (!g) return;
    const x = s[v], L = s[v + 1], C = g.querySelectorAll(":scope > .tlSplitPanel__child"), k = [];
    C.forEach((T) => {
      k.push(i ? T.offsetWidth : T.offsetHeight);
    }), f.current = k, p.current = {
      splitterIndex: v,
      startPos: i ? E.clientX : E.clientY,
      startSizeBefore: k[v],
      startSizeAfter: k[v + 1],
      childBefore: x,
      childAfter: L
    };
    const h = (T) => {
      const S = p.current;
      if (!S || !f.current) return;
      const B = (i ? T.clientX : T.clientY) - S.startPos, R = S.childBefore.minSize || 0, F = S.childAfter.minSize || 0;
      let Q = S.startSizeBefore + B, W = S.startSizeAfter - B;
      Q < R && (W += Q - R, Q = R), W < F && (Q += W - F, W = F), f.current[S.splitterIndex] = Q, f.current[S.splitterIndex + 1] = W;
      const O = g.querySelectorAll(":scope > .tlSplitPanel__child"), M = O[S.splitterIndex], j = O[S.splitterIndex + 1];
      M && (M.style.flex = `0 0 ${Q}px`), j && (j.style.flex = `0 0 ${W}px`);
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
  return s.forEach((E, v) => {
    if (w.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${v}`,
          className: `tlSplitPanel__child${E.collapsed && o ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: _(E)
        },
        /* @__PURE__ */ e.createElement(K, { control: E.control })
      )
    ), c && v < s.length - 1) {
      const g = s[v + 1];
      !E.collapsed && !g.collapsed && w.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${v}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${r}`,
            onMouseDown: (L) => b(L, v)
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
    w
  );
}, qe = ({ image: l, className: t }) => {
  if (!l) return null;
  const n = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `${t ? t + " " : ""}${n}`, "aria-hidden": "true" });
}, { useCallback: Je } = e, Gn = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Xn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), qn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), Zn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), Qn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), Jn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), el = ({ controlId: l }) => {
  const t = G(), n = re(), r = ie(Gn), c = t.title, s = t.expansionState ?? "NORMALIZED", i = t.showMinimize === !0, u = t.showMaximize === !0, a = t.showPopOut === !0, o = t.fullLine === !0, m = t.fill === !0, p = t.hoverActions === !0, f = t.appearance === "card", _ = t.errorMessage, b = s === "MINIMIZED", w = s === "MAXIMIZED", E = s === "HIDDEN", v = Je(() => {
    n("toggleMinimize");
  }, [n]), g = Je(() => {
    n("toggleMaximize");
  }, [n]), x = Je(() => {
    n("popOut");
  }, [n]);
  if (E)
    return null;
  const L = w ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" }, C = i && !w || u && !b || a, k = !!c && c.trim() !== "" || !!t.titleContent || !!t.toolbar || C;
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
        onClick: v,
        title: b ? r["js.panel.restore"] : r["js.panel.minimize"]
      },
      b ? /* @__PURE__ */ e.createElement(qn, null) : /* @__PURE__ */ e.createElement(Xn, null)
    ), u && !b && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: g,
        title: w ? r["js.panel.restore"] : r["js.panel.maximize"]
      },
      w ? /* @__PURE__ */ e.createElement(Qn, null) : /* @__PURE__ */ e.createElement(Zn, null)
    ), a && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: x,
        title: r["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(Jn, null)
    ))),
    !b && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(K, { control: t.child })),
    !b && _ && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error tlPanel__error", role: "alert" }, /* @__PURE__ */ e.createElement(qe, { image: t.errorIcon, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, _)),
    !b && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(K, { control: t.buttonBar }))
  );
}, tl = ({ controlId: l }) => {
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
}, nl = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(K, { control: t.activeChild }));
}, { useCallback: be, useState: Xe, useEffect: st, useRef: Ze } = e, ll = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function ct(l, t, n, r) {
  const c = [];
  for (const s of l)
    if (s.type === "nav") {
      if (s.hidden) continue;
      c.push({ id: s.id, type: "nav", groupId: r });
    } else s.type === "command" ? c.push({ id: s.id, type: "command", groupId: r }) : s.type === "group" && (c.push({ id: s.id, type: "group" }), (n.get(s.id) ?? s.expanded) && !t && c.push(...ct(s.children, t, n, s.id)));
  return c;
}
const Be = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(we, { encoded: l, className: "tlSidebar__icon" }) : null, rl = ({ item: l, active: t, collapsed: n, onSelect: r, tabIndex: c, itemRef: s, onFocus: i }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => r(l.id),
    title: n ? l.label : void 0,
    tabIndex: c,
    ref: s,
    onFocus: () => i(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Be, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Be, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), al = ({ item: l, collapsed: t, onExecute: n, tabIndex: r, itemRef: c, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: r,
    ref: c,
    onFocus: () => s(l.id)
  },
  /* @__PURE__ */ e.createElement(Be, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), ol = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Be, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), sl = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), cl = ({ item: l, activeItemId: t, anchorRect: n, onSelect: r, onExecute: c, onClose: s }) => {
  const i = Ze(null);
  st(() => {
    const o = (m) => {
      i.current && !i.current.contains(m.target) && setTimeout(() => s(), 0);
    };
    return document.addEventListener("mousedown", o), () => document.removeEventListener("mousedown", o);
  }, [s]), Le(!0, { ESCAPE: s });
  const u = be((o) => {
    o.type === "nav" ? (r(o.id), s()) : o.type === "command" && (c(o.id), s());
  }, [r, c, s]), a = {};
  return n && (a.left = n.right, a.top = n.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: i, role: "menu", style: a }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((o) => {
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
}, il = ({
  item: l,
  expanded: t,
  activeItemId: n,
  collapsed: r,
  onSelect: c,
  onExecute: s,
  onToggleGroup: i,
  tabIndex: u,
  itemRef: a,
  onFocus: o,
  focusedId: m,
  setItemRef: p,
  onItemFocus: f,
  flyoutGroupId: _,
  onOpenFlyout: b,
  onCloseFlyout: w
}) => {
  const E = Ze(null), [v, g] = Xe(null), x = be(() => {
    r ? _ === l.id ? w() : (E.current && g(E.current.getBoundingClientRect()), b(l.id)) : i(l.id);
  }, [r, _, l.id, i, b, w]), L = be((k) => {
    E.current = k, a(k);
  }, [a]), C = r && _ === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (C ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: x,
      title: r ? l.label : void 0,
      "aria-expanded": r ? C : t,
      tabIndex: u,
      ref: L,
      onFocus: () => o(l.id)
    },
    /* @__PURE__ */ e.createElement(Be, { icon: l.icon }),
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
  ), C && /* @__PURE__ */ e.createElement(
    cl,
    {
      item: l,
      activeItemId: n,
      anchorRect: v,
      onSelect: c,
      onExecute: s,
      onClose: w
    }
  ), t && !r && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((k) => /* @__PURE__ */ e.createElement(
    Ot,
    {
      key: k.id,
      item: k,
      activeItemId: n,
      collapsed: r,
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
}, Ot = ({
  item: l,
  activeItemId: t,
  collapsed: n,
  onSelect: r,
  onExecute: c,
  onToggleGroup: s,
  focusedId: i,
  setItemRef: u,
  onItemFocus: a,
  groupStates: o,
  flyoutGroupId: m,
  onOpenFlyout: p,
  onCloseFlyout: f
}) => {
  switch (l.type) {
    case "nav":
      return l.hidden ? null : /* @__PURE__ */ e.createElement(
        rl,
        {
          item: l,
          active: l.id === t,
          collapsed: n,
          onSelect: r,
          tabIndex: i === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: a
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        al,
        {
          item: l,
          collapsed: n,
          onExecute: c,
          tabIndex: i === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: a
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(ol, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(sl, null);
    case "group": {
      const _ = o ? o.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        il,
        {
          item: l,
          expanded: _,
          activeItemId: t,
          collapsed: n,
          onSelect: r,
          onExecute: c,
          onToggleGroup: s,
          tabIndex: i === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: a,
          focusedId: i,
          setItemRef: u,
          onItemFocus: a,
          flyoutGroupId: m,
          onOpenFlyout: p,
          onCloseFlyout: f
        }
      );
    }
    default:
      return null;
  }
}, ul = ({ controlId: l }) => {
  const t = G(), n = re(), r = ie(ll), c = t.items ?? [], s = t.activeItemId, i = t.collapsed, u = t.drawerOpen, a = u ? !1 : i, [o, m] = Xe(() => {
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
  }, [n]), [E, v] = Xe(null), g = be((R) => {
    v(R);
  }, []), x = be(() => {
    v(null);
  }, []);
  st(() => {
    a || v(null);
  }, [a]);
  const [L, C] = Xe(() => {
    const R = ct(c, a, o);
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
    const F = ct(c, a, o);
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
        R.preventDefault(), W.type === "nav" ? f(W.id) : W.type === "command" ? _(W.id) : W.type === "group" && (a ? E === W.id ? x() : g(W.id) : p(W.id));
        break;
      }
      case "ArrowRight": {
        W.type === "group" && !a && ((o.get(W.id) ?? !1) || (R.preventDefault(), p(W.id)));
        break;
      }
      case "ArrowLeft": {
        W.type === "group" && !a && (o.get(W.id) ?? !1) && (R.preventDefault(), p(W.id));
        break;
      }
    }
  }, [
    c,
    a,
    o,
    L,
    E,
    S,
    f,
    _,
    p,
    g,
    x
  ]), B = "tlSidebar" + (a ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: B }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(K, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: w, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": r["js.sidebar.ariaLabel"] }, a ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: H }, c.map((R) => /* @__PURE__ */ e.createElement(
    Ot,
    {
      key: R.id,
      item: R,
      activeItemId: s,
      collapsed: a,
      onSelect: f,
      onExecute: _,
      onToggleGroup: p,
      focusedId: L,
      setItemRef: h,
      onItemFocus: I,
      groupStates: o,
      flyoutGroupId: E,
      onOpenFlyout: g,
      onCloseFlyout: x
    }
  ))), a ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: b,
      title: a ? r["js.sidebar.expand"] : r["js.sidebar.collapse"]
    },
    /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement(
      "path",
      {
        d: a ? "M6 4l4 4-4 4" : "M10 4l-4 4 4 4",
        fill: "none",
        stroke: "currentColor",
        strokeWidth: "2",
        strokeLinecap: "round",
        strokeLinejoin: "round"
      }
    ))
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(K, { control: t.activeContent })));
}, dl = ({ controlId: l }) => {
  const t = G(), n = t.direction ?? "column", r = t.gap ?? "default", c = t.align ?? "stretch", s = t.wrap === !0, i = t.growFirst === !0, u = t.children ?? [], a = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${r}`,
    `tlStack--align-${c}`,
    s ? "tlStack--wrap" : "",
    i ? "tlStack--grow-first" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: a }, u.map((o, m) => /* @__PURE__ */ e.createElement(K, { key: m, control: o })));
}, ml = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlInset" }, t.child && /* @__PURE__ */ e.createElement(K, { control: t.child }));
}, pl = ({ controlId: l }) => {
  const t = G(), n = t.columns, r = t.minColumnWidth, c = t.gap ?? "default", s = t.children ?? [], i = {};
  return r ? i.gridTemplateColumns = `repeat(auto-fit, minmax(min(${r}, 100%), 1fr))` : n && (i.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${c}`, style: i }, s.map((u, a) => /* @__PURE__ */ e.createElement(K, { key: a, control: u })));
}, fl = ({ controlId: l }) => {
  const t = G(), n = t.title, r = t.variant ?? "outlined", c = t.padding ?? "default", s = t.headerActions ?? [], i = t.child, u = n != null || s.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${r}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, s.map((a, o) => /* @__PURE__ */ e.createElement(K, { key: o, control: a })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${c}` }, /* @__PURE__ */ e.createElement(K, { control: i })));
}, hl = ({ controlId: l }) => {
  const t = G(), n = t.title ?? "", r = t.leading, c = t.children ?? [], s = t.actions ?? [], i = t.variant ?? "flat", a = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    i === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: a }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(K, { control: r })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, c.map((o, m) => /* @__PURE__ */ e.createElement(K, { key: m, control: o }))), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((o, m) => /* @__PURE__ */ e.createElement(K, { key: m, control: o }))));
}, { useCallback: bl } = e, _l = ({ controlId: l }) => {
  const t = G(), n = re(), r = t.items ?? [], c = bl((s) => {
    n("navigate", { itemId: s });
  }, [n]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, r.map((s, i) => {
    const u = i === r.length - 1;
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
}, { useCallback: vl } = e, gl = ({ controlId: l }) => {
  const t = G(), n = re(), r = t.items ?? [], c = t.activeItemId, s = vl((i) => {
    i !== c && n("selectItem", { itemId: i });
  }, [n, c]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, r.map((i) => {
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
}, { useCallback: Ct, useRef: El } = e, Cl = ({ onClose: l }) => (de("ESCAPE", () => (l(), !0)), null), wl = ({ controlId: l }) => {
  const t = G(), n = re(), r = t.open === !0, c = t.closeOnBackdrop !== !1, s = t.child, i = El(null), u = Ct(() => {
    n("close");
  }, [n]), a = Ct((o) => {
    c && o.target === o.currentTarget && u();
  }, [c, u]);
  return r ? /* @__PURE__ */ e.createElement(mt, null, /* @__PURE__ */ e.createElement(Cl, { onClose: u }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: a,
      ref: i,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(K, { control: s })
  )) : null;
}, { useEffect: yl, useRef: kl } = e, Sl = ({ controlId: l }) => {
  const n = G().dialogs ?? [], r = kl(n.length);
  return yl(() => {
    n.length < r.current && n.length > 0, r.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((c) => /* @__PURE__ */ e.createElement(K, { key: c.controlId, control: c })));
}, { useCallback: ze, useRef: Me, useState: Ve } = e, Nl = ({ onClose: l }) => (de("ESCAPE", () => (l(), !0)), null), Tl = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Rl = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Dl = ({ controlId: l }) => {
  const t = G(), n = re(), r = ie(Tl), c = t.title ?? "", s = t.width ?? "32rem", i = t.height ?? null, u = t.minHeight ?? null, a = t.resizable === !0, o = t.child, m = t.actions ?? [], p = t.toolbar, f = t.buttonBar, [_, b] = Ve(null), [w, E] = Ve(null), [v, g] = Ve(null), x = Me(null), [L, C] = Ve(!1), k = Me(null), h = Me(null), I = Me(null), T = Me(null), S = Me(null), H = ze(() => {
    n("close");
  }, [n]);
  pt(!0, T, "field");
  const B = ze((O, M) => {
    M.preventDefault();
    const j = T.current;
    if (!j) return;
    const X = j.getBoundingClientRect(), d = !x.current, N = x.current ?? { x: X.left, y: X.top };
    d && (x.current = N, g(N)), S.current = {
      dir: O,
      startX: M.clientX,
      startY: M.clientY,
      startW: X.width,
      startH: X.height,
      startPos: { ...N },
      symmetric: d
    };
    const V = (q) => {
      const A = S.current;
      if (!A) return;
      const ee = q.clientX - A.startX, oe = q.clientY - A.startY;
      let le = A.startW, he = A.startH, ve = 0, ge = 0;
      A.symmetric ? (A.dir.includes("e") && (le = A.startW + 2 * ee), A.dir.includes("w") && (le = A.startW - 2 * ee), A.dir.includes("s") && (he = A.startH + 2 * oe), A.dir.includes("n") && (he = A.startH - 2 * oe)) : (A.dir.includes("e") && (le = A.startW + ee), A.dir.includes("w") && (le = A.startW - ee, ve = ee), A.dir.includes("s") && (he = A.startH + oe), A.dir.includes("n") && (he = A.startH - oe, ge = oe));
      const ye = Math.max(200, le), ke = Math.max(100, he);
      A.symmetric ? (ve = (A.startW - ye) / 2, ge = (A.startH - ke) / 2) : (A.dir.includes("w") && ye === 200 && (ve = A.startW - 200), A.dir.includes("n") && ke === 100 && (ge = A.startH - 100)), h.current = ye, I.current = ke, b(ye), E(ke);
      const xe = {
        x: A.startPos.x + ve,
        y: A.startPos.y + ge
      };
      x.current = xe, g(xe);
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
    const M = T.current;
    if (!M) return;
    const j = M.getBoundingClientRect(), X = x.current ?? { x: j.left, y: j.top }, d = O.clientX - X.x, N = O.clientY - X.y, V = (q) => {
      const A = window.innerWidth, ee = window.innerHeight;
      let oe = q.clientX - d, le = q.clientY - N;
      const he = M.offsetWidth, ve = M.offsetHeight;
      oe + he > A && (oe = A - he), le + ve > ee && (le = ee - ve), oe < 0 && (oe = 0), le < 0 && (le = 0);
      const ge = { x: oe, y: le };
      x.current = ge, g(ge);
    }, $ = () => {
      document.removeEventListener("mousemove", V), document.removeEventListener("mouseup", $);
    };
    document.addEventListener("mousemove", V), document.addEventListener("mouseup", $);
  }, []), F = ze(() => {
    var O, M;
    if (L) {
      const j = k.current;
      j && (g(j.x !== -1 ? { x: j.x, y: j.y } : null), b(j.w), E(j.h)), C(!1);
    } else {
      const j = T.current, X = j == null ? void 0 : j.getBoundingClientRect();
      k.current = {
        x: ((O = x.current) == null ? void 0 : O.x) ?? (X == null ? void 0 : X.left) ?? -1,
        y: ((M = x.current) == null ? void 0 : M.y) ?? (X == null ? void 0 : X.top) ?? -1,
        w: _ ?? (X == null ? void 0 : X.width) ?? null,
        h: w ?? null
      }, C(!0), g({ x: 0, y: 0 }), b(null), E(null);
    }
  }, [L, _, w]), Q = L ? { position: "absolute", top: 0, left: 0, width: "100vw", maxWidth: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: _ != null ? _ + "px" : s,
    ...w != null ? { height: w + "px" } : i != null ? { height: i } : {},
    ...u != null && w == null ? { minHeight: u } : {},
    maxHeight: v ? "100vh" : "80vh",
    ...v ? { position: "absolute", left: v.x + "px", top: v.y + "px" } : {}
  }, W = l + "-title";
  return /* @__PURE__ */ e.createElement(mt, { modal: !0 }, /* @__PURE__ */ e.createElement(Nl, { onClose: H }), /* @__PURE__ */ e.createElement(
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
        onDoubleClick: a ? F : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: W }, c),
      p && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(K, { control: p })),
      a && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: F,
          title: L ? r["js.window.restore"] : r["js.window.maximize"]
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
    (m.length > 0 || f) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, f && /* @__PURE__ */ e.createElement(K, { control: f }), m.map((O, M) => /* @__PURE__ */ e.createElement(K, { key: M, control: O }))),
    a && !L && Rl.map((O) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: O,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${O}`,
        onMouseDown: (M) => B(O, M)
      }
    ))
  ));
}, { useCallback: Ll } = e, xl = {
  "js.drawer.close": "Close"
}, Il = ({ controlId: l }) => {
  const t = G(), n = re(), r = ie(xl), c = t.open === !0, s = t.position ?? "right", i = t.size ?? "medium", u = t.title ?? null, a = t.child, o = Ll(() => {
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
}, { useCallback: Ml } = e, Pl = ({ controlId: l }) => {
  const t = G(), n = re(), r = t.child, c = Ml((s) => {
    s.preventDefault(), s.stopPropagation(), n("openContextMenu", { x: s.clientX, y: s.clientY });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tl-context-menu-region", onContextMenu: c }, r && /* @__PURE__ */ e.createElement(K, { control: r }));
}, { useCallback: jl, useEffect: wt, useRef: Al, useState: yt } = e, Bl = 250, Fl = ({ controlId: l }) => {
  const t = G(), n = re(), r = t.message ?? "", c = t.content ?? "", s = t.variant ?? "info", i = t.duration ?? 5e3, u = t.visible === !0, a = t.generation ?? 0, [o, m] = yt(!1), [p, f] = yt(!1), _ = Al(!1);
  wt(() => {
    _.current = !1;
  }, [a]);
  const b = jl(() => {
    m(!0), setTimeout(() => {
      n("dismiss", { generation: a }), m(!1);
    }, 200);
  }, [n, a]);
  return wt(() => {
    if (!u || i === 0 || p) return;
    const w = setTimeout(b, _.current ? Bl : i);
    return () => clearTimeout(w);
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
    c ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: c } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, r)
  );
}, { useCallback: et, useEffect: kt, useRef: Ol, useState: St } = e, $l = ({ controlId: l }) => {
  const t = G(), n = re(), r = t.open === !0, c = t.anchorId, s = t.anchorX, i = t.anchorY, u = t.items ?? [], a = Ol(null), [o, m] = St({ top: 0, left: 0 }), [p, f] = St(0), _ = u.filter((v) => v.type === "item" && !v.disabled);
  kt(() => {
    var h, I;
    if (!r) return;
    const v = ((h = a.current) == null ? void 0 : h.offsetHeight) ?? 200, g = ((I = a.current) == null ? void 0 : I.offsetWidth) ?? 200;
    if (s != null && i != null) {
      let T = i, S = s;
      T + v > window.innerHeight && (T = Math.max(0, window.innerHeight - v)), S + g > window.innerWidth && (S = Math.max(0, window.innerWidth - g)), m({ top: T, left: S }), f(0);
      return;
    }
    if (!c) return;
    const x = document.getElementById(c);
    if (!x) return;
    const L = x.getBoundingClientRect();
    let C = L.bottom + 4, k = L.left;
    C + v > window.innerHeight && (C = L.top - v - 4), k + g > window.innerWidth && (k = L.right - g), m({ top: C, left: k }), f(0);
  }, [r, c, s, i]);
  const b = et(() => {
    n("close");
  }, [n]), w = et((v) => {
    n("selectItem", { itemId: v });
  }, [n]);
  kt(() => {
    if (!r) return;
    const v = (g) => {
      a.current && !a.current.contains(g.target) && b();
    };
    return document.addEventListener("mousedown", v), () => document.removeEventListener("mousedown", v);
  }, [r, b]);
  const E = et((v) => {
    if (v.key === "Escape") {
      v.preventDefault(), b();
      return;
    }
    if (v.key === "ArrowDown")
      v.preventDefault(), f((g) => (g + 1) % _.length);
    else if (v.key === "ArrowUp")
      v.preventDefault(), f((g) => (g - 1 + _.length) % _.length);
    else if (v.key === "Enter" || v.key === " ") {
      v.preventDefault();
      const g = _[p];
      g && w(g.id);
    }
  }, [b, w, _, p]);
  return pt(r, a), r ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: a,
      tabIndex: -1,
      style: { position: "fixed", top: o.top, left: o.left },
      onKeyDown: E
    },
    u.map((v, g) => {
      if (v.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: g, className: "tlMenu__separator" });
      const L = _.indexOf(v) === p;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: v.id,
          type: "button",
          className: "tlMenu__item" + (L ? " tlMenu__item--focused" : "") + (v.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: v.disabled,
          tabIndex: L ? 0 : -1,
          onClick: () => w(v.id)
        },
        v.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + v.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, v.label)
      );
    })
  ) : null;
}, Ul = 768, Hl = ({ controlId: l }) => {
  const t = G(), n = re();
  e.useEffect(() => {
    const o = window.matchMedia(`(max-width: ${Ul}px)`), m = (f) => {
      n("reportDisplayClass", { displayClass: f ? "COMPACT" : "REGULAR" });
    };
    m(o.matches);
    const p = (f) => m(f.matches);
    return o.addEventListener("change", p), () => o.removeEventListener("change", p);
  }, [n]);
  const r = t.header, c = t.content, s = t.footer, i = t.snackbar, u = t.dialogManager, a = t.menuOverlay;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(K, { control: r })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(K, { control: c })), s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(K, { control: s })), /* @__PURE__ */ e.createElement(K, { control: i }), u && /* @__PURE__ */ e.createElement(K, { control: u }), a && /* @__PURE__ */ e.createElement(K, { control: a }));
}, Wl = ({ controlId: l }) => {
  const t = G(), n = t.text ?? "", r = t.cssClass ?? "", c = t.hasTooltip === !0, s = r ? `tlText ${r}` : "tlText";
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: s,
      "data-tooltip": c ? "key:tooltip" : void 0
    },
    n
  );
}, zl = ({ isMulti: l, cursorIndex: t, onMove: n, onToggle: r, onSelectAll: c }) => (de("ArrowUp", () => (n("up", !1, !1), !0)), de("ArrowDown", () => (n("down", !1, !1), !0)), de("Home", () => (n("home", !1, !1), !0)), de("End", () => (n("end", !1, !1), !0)), de("PageUp", () => (n("pageUp", !1, !1), !0)), de("PageDown", () => (n("pageDown", !1, !1), !0)), de("Shift+ArrowUp", () => (n("up", l, !1), !0)), de("Shift+ArrowDown", () => (n("down", l, !1), !0)), de("Shift+Home", () => (n("home", l, !1), !0)), de("Shift+End", () => (n("end", l, !1), !0)), de("Shift+PageUp", () => (n("pageUp", l, !1), !0)), de("Shift+PageDown", () => (n("pageDown", l, !1), !0)), de("Ctrl+ArrowUp", () => (n("up", !1, l), !0)), de("Ctrl+ArrowDown", () => (n("down", !1, l), !0)), de("Space", () => t < 0 ? !1 : (r(), !0)), de("Ctrl+A", () => l ? (c(), !0) : !1), null), Vl = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all",
  "js.table.filter": "Filter"
}, Nt = 50;
function Tt(l) {
  var n;
  const t = l.target;
  return !!((n = t == null ? void 0 : t.closest) != null && n.call(t, 'input, textarea, select, button, a, [contenteditable="true"]'));
}
const it = 'input:not([disabled]):not([readonly]), textarea:not([disabled]):not([readonly]), select:not([disabled]), [contenteditable="true"]', Kl = it + ", button:not([disabled]), a[href]";
function $t(l, t) {
  return Array.from(l.querySelectorAll("[data-row][data-col]")).filter((n) => n.dataset.row === t);
}
function Rt(l, t, n = {}) {
  const r = $t(l, t);
  if (n.col) {
    const s = r.find((u) => u.dataset.col === n.col), i = s == null ? void 0 : s.querySelector(it);
    if (i) return i;
  }
  const c = n.last ? [...r].reverse() : r;
  for (const s of c) {
    const i = s.querySelector(it);
    if (i) return i;
  }
  return null;
}
const Yl = ({ controlId: l }) => {
  const t = G(), n = re(), r = ie(Vl), c = e.useRef(null);
  e.useEffect(() => {
    const y = c.current;
    if (!y) return;
    const D = (z) => {
      const J = z.detail;
      let te = J.target;
      for (; te && te !== y; ) {
        const ce = te.dataset.row, ae = te.dataset.col;
        if (ce != null && ae != null) {
          J.resolved = { key: ce + "|" + ae };
          return;
        }
        te = te.parentElement;
      }
    };
    return y.addEventListener("tl-tooltip-resolve", D), () => y.removeEventListener("tl-tooltip-resolve", D);
  }, []);
  const s = t.columns ?? [], i = t.totalRowCount ?? 0, u = t.rows ?? [], a = t.rowHeight ?? 36, o = t.selectionMode ?? "single", m = t.selectedCount ?? 0, p = t.cursorIndex ?? -1, f = t.frozenColumnCount ?? 0, _ = t.treeMode ?? !1, b = e.useMemo(
    () => s.filter((y) => y.sortPriority && y.sortPriority > 0).length,
    [s]
  ), w = o === "multi", E = 40, v = 20, g = e.useRef(null), x = e.useRef(null), L = e.useRef(null), C = e.useRef(null), [k, h] = e.useState({}), I = e.useRef(null), T = e.useRef(!1), S = e.useRef(null), [H, B] = e.useState(null), [R, F] = e.useState(null);
  e.useEffect(() => {
    I.current || h({});
  }, [s]);
  const Q = e.useCallback((y) => k[y.name] ?? y.width, [k]), W = e.useMemo(() => {
    const y = [];
    let D = w && f > 0 ? E : 0;
    for (let z = 0; z < f && z < s.length; z++)
      y.push(D), D += Q(s[z]);
    return y;
  }, [s, f, w, E, Q]), O = i * a, M = e.useRef(null), j = e.useCallback((y, D, z) => {
    z.preventDefault(), z.stopPropagation(), I.current = { column: y, startX: z.clientX, startWidth: D };
    let J = z.clientX, te = 0;
    const ce = () => {
      const ue = I.current;
      if (!ue) return;
      const me = Math.max(Nt, ue.startWidth + (J - ue.startX) + te);
      h((Ce) => ({ ...Ce, [ue.column]: me }));
    }, ae = () => {
      const ue = x.current, me = g.current;
      if (!ue || !I.current) return;
      const Ce = ue.getBoundingClientRect(), Ne = 40, ht = 8, Gt = ue.scrollLeft;
      J > Ce.right - Ne ? ue.scrollLeft += ht : J < Ce.left + Ne && (ue.scrollLeft = Math.max(0, ue.scrollLeft - ht));
      const bt = ue.scrollLeft - Gt;
      bt !== 0 && (me && (me.scrollLeft = ue.scrollLeft), te += bt, ce()), M.current = requestAnimationFrame(ae);
    };
    M.current = requestAnimationFrame(ae);
    const _e = (ue) => {
      J = ue.clientX, ce();
    }, Se = (ue) => {
      document.removeEventListener("mousemove", _e), document.removeEventListener("mouseup", Se), M.current !== null && (cancelAnimationFrame(M.current), M.current = null);
      const me = I.current;
      if (me) {
        const Ce = Math.max(Nt, me.startWidth + (ue.clientX - me.startX) + te);
        n("columnResize", { column: me.column, width: Ce }), I.current = null, T.current = !0, requestAnimationFrame(() => {
          T.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", _e), document.addEventListener("mouseup", Se);
  }, [n]), X = e.useCallback(() => {
    g.current && x.current && (g.current.scrollLeft = x.current.scrollLeft), L.current !== null && clearTimeout(L.current), L.current = window.setTimeout(() => {
      const y = x.current;
      if (!y) return;
      const D = y.scrollTop, z = Math.ceil(y.clientHeight / a), J = Math.floor(D / a);
      n("scroll", { start: J, count: z });
    }, 80);
  }, [n, a]), d = e.useCallback((y, D, z) => {
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
    var J, te, ce, ae;
    const z = window.getSelection();
    if (!(z && !z.isCollapsed && D.currentTarget.contains(z.anchorNode))) {
      if (!Tt(D) && ((J = x.current) == null || J.focus({ preventScroll: !0 }), !D.ctrlKey && !D.metaKey && !D.shiftKey)) {
        const _e = (ae = (ce = (te = D.target) == null ? void 0 : te.closest) == null ? void 0 : ce.call(te, "[data-col]")) == null ? void 0 : ae.getAttribute("data-col");
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
    const D = p * a, z = D + a;
    D < y.scrollTop ? y.scrollTop = D : z > y.scrollTop + y.clientHeight && (y.scrollTop = z - y.clientHeight);
  }, [p, a]), e.useEffect(() => {
    const y = C.current, D = x.current;
    if (!y || !D)
      return;
    const z = u.find((te) => te.index === y.index);
    if (!z)
      return;
    const J = Rt(D, z.id, { col: y.col, last: y.last });
    J && (C.current = null, J.focus({ preventScroll: !1 }), J instanceof HTMLInputElement && J.select());
  }, [u]);
  const ve = e.useCallback((y) => {
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
    const ae = $t(D, te).flatMap((Ne) => Array.from(Ne.querySelectorAll(Kl))), _e = ae.indexOf(z);
    if (_e < 0)
      return;
    const Se = !y.shiftKey;
    if (!(Se ? _e === ae.length - 1 : _e === 0))
      return;
    const me = Se ? ce.index + 1 : ce.index - 1;
    if (me < 0 || me >= i)
      return;
    const Ce = u.find((Ne) => Ne.index === me);
    Ce && Rt(D, Ce.id) || (y.preventDefault(), C.current = { index: me, last: !Se }, n("select", { rowIndex: me, ctrlKey: !1, shiftKey: !1 }));
  }, [u, i, n]), ge = e.useCallback((y, D) => {
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
  }, [R, n]), P = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), F(null);
  }, [n]);
  e.useEffect(() => {
    if (!R) return;
    const y = () => F(null);
    return document.addEventListener("mousedown", y), () => document.removeEventListener("mousedown", y);
  }, [R]), Le(!!R, { ESCAPE: () => F(null) });
  const Y = e.useCallback((y, D) => {
    D.stopPropagation(), D.preventDefault(), n("openFilter", { column: y });
  }, [n]), ne = s.reduce((y, D) => y + Q(D), 0) + (w ? E : 0), se = m === i && i > 0, Ie = m > 0 && m < i, Yt = e.useCallback((y) => {
    y && (y.indeterminate = Ie);
  }, [Ie]);
  return /* @__PURE__ */ e.createElement(mt, { active: he }, /* @__PURE__ */ e.createElement(
    zl,
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
        const D = x.current, z = g.current;
        if (!D) return;
        const J = D.getBoundingClientRect(), te = 40, ce = 8;
        y.clientX < J.left + te ? D.scrollLeft = Math.max(0, D.scrollLeft - ce) : y.clientX > J.right - te && (D.scrollLeft += ce), z && (z.scrollLeft = D.scrollLeft);
      },
      onDrop: $
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: g }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { width: ne } }, w && /* @__PURE__ */ e.createElement(
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
          ref: Yt,
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
          onClick: y.sortable ? (ae) => d(y.name, y.sortDirection, ae) : void 0,
          onContextMenu: (ae) => xe(D, ae),
          onDragStart: (ae) => N(y.name, ae),
          onDragOver: (ae) => V(y.name, ae),
          onDrop: $,
          onDragEnd: q
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, y.label),
        y.filterable && /* @__PURE__ */ e.createElement(
          "button",
          {
            type: "button",
            className: "tlTableView__filterButton" + (y.filterActive ? " tlTableView__filterButton--active" : ""),
            title: r["js.table.filter"],
            style: {
              border: "none",
              background: "transparent",
              cursor: "pointer",
              padding: "0 4px",
              color: y.filterActive ? "#1565c0" : "inherit"
            },
            onMouseDown: (ae) => ae.stopPropagation(),
            onClick: (ae) => Y(y.name, ae)
          },
          /* @__PURE__ */ e.createElement("i", { className: y.filterActive ? "bi bi-funnel-fill" : "bi bi-funnel" })
        ),
        y.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, y.sortDirection === "asc" ? "▲" : "▼", b > 1 && y.sortPriority != null && y.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, y.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (ae) => j(y.name, z, ae)
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
        onKeyDown: ve,
        tabIndex: 0
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: O, position: "relative", width: ne } }, u.map((y) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: y.id,
          className: "tlTableView__row" + (y.selected ? " tlTableView__row--selected" : "") + (y.index === p ? " tlTableView__row--cursor" : ""),
          style: {
            position: "absolute",
            top: y.index * a,
            height: a,
            width: ne,
            ...y.index === p ? { outline: "2px solid var(--color-primary, #1a73e8)", outlineOffset: "-2px" } : {}
          },
          onMouseDown: (D) => {
            (D.shiftKey || D.ctrlKey || D.metaKey || D.detail > 1) && !Tt(D) && D.preventDefault();
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
              onClick: (D) => ge(y.index, D),
              tabIndex: -1
            }
          )
        ),
        s.map((D, z) => {
          const J = Q(D), te = z === s.length - 1, ce = z < f, ae = z === f - 1;
          let _e = "tlTableView__cell";
          ce && (_e += " tlTableView__cell--frozen"), ae && (_e += " tlTableView__cell--frozenLast");
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
            Se ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ue * v } }, y.expandable ? /* @__PURE__ */ e.createElement(
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
      R.colIdx + 1 !== f && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: He }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.freezeUpTo"])),
      f > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: P }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.unfreezeAll"]))
    )
  ));
}, Gl = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, Ut = e.createContext(Gl), { useMemo: Xl, useRef: ql, useState: Zl, useEffect: Ql } = e, Jl = 320, er = "TLTableView", tr = "TLPanel", nr = ({ controlId: l }) => {
  var E;
  const t = G(), n = t.maxColumns ?? 3, r = t.labelPosition ?? "auto", c = t.readOnly === !0, s = t.children ?? [], i = t.noModelMessage, u = ql(null), [a, o] = Zl(
    r === "top" ? "top" : "side"
  );
  Ql(() => {
    if (r !== "auto") {
      o(r);
      return;
    }
    const v = u.current;
    if (!v) return;
    const g = new ResizeObserver((x) => {
      for (const L of x) {
        const k = L.contentRect.width / n;
        o(k < Jl ? "top" : "side");
      }
    });
    return g.observe(v), () => g.disconnect();
  }, [r, n]);
  const m = Xl(() => ({
    readOnly: c,
    resolvedLabelPosition: a
  }), [c, a]), f = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, _ = s.length === 1 ? s[0] : void 0, b = !!_ && (_.module === er || _.module === tr && ((E = _.state) == null ? void 0 : E.bare) === !0), w = [
    "tlFormLayout",
    c ? "tlFormLayout--readonly" : "",
    b ? "tlFormLayout--flush" : ""
  ].filter(Boolean).join(" ");
  return i ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, i)) : /* @__PURE__ */ e.createElement(Ut.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: l, className: w, style: f, ref: u }, s.map((v, g) => /* @__PURE__ */ e.createElement(K, { key: g, control: v }))));
}, { useCallback: lr } = e, rr = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, ar = ({ controlId: l }) => {
  const t = G(), n = re(), r = ie(rr), c = t.headerControl ?? null, s = t.headerActions ?? [], i = t.collapsible === !0, u = t.collapsed === !0, a = t.border ?? "none", o = t.fullLine === !0, m = t.children ?? [], p = c != null || s.length > 0 || i, f = lr(() => {
    n("toggleCollapse");
  }, [n]), _ = [
    "tlFormGroup",
    `tlFormGroup--border-${a}`,
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
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(K, { control: c })), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, s.map((b, w) => /* @__PURE__ */ e.createElement(K, { key: w, control: b })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((b, w) => /* @__PURE__ */ e.createElement(K, { key: w, control: b }))));
}, { useContext: or, useState: sr, useCallback: cr } = e, ir = ({ controlId: l }) => {
  const t = G(), n = or(Ut), r = t.label ?? "", c = t.required === !0, s = t.error, i = t.errorIcon, u = t.warnings, a = t.warningIcon, o = t.helpText, m = t.dirty === !0, p = t.labelPosition ?? n.resolvedLabelPosition, f = t.fullLine === !0, _ = t.visible !== !1, b = t.hasTooltip === !0, w = t.field, E = n.readOnly, [v, g] = sr(!1), x = cr(() => g((I) => !I), []), L = p === "hidden", C = s != null, k = u != null && u.length > 0, h = [
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
    r
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(K, { control: w })), !E && C && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(qe, { image: i, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, s)), !E && !C && k && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, u.map((I, T) => /* @__PURE__ */ e.createElement("div", { key: T, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(qe, { image: a, className: "tlFormField__warningIcon" }), /* @__PURE__ */ e.createElement("span", null, I)))), !E && o && v && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, o));
}, ur = ({ controlId: l }) => {
  const t = G(), n = re(), r = t.iconCss, c = t.iconSrc, s = t.label, i = t.cssClass, u = t.hasTooltip === !0, a = t.hasLink, o = r ? /* @__PURE__ */ e.createElement("i", { className: r }) : c ? /* @__PURE__ */ e.createElement("img", { src: c, className: "tlTypeIcon", alt: "" }) : null, m = /* @__PURE__ */ e.createElement(e.Fragment, null, o, s && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, s)), p = e.useCallback((b) => {
    b.preventDefault(), n("goto", {});
  }, [n]), f = ["tlResourceCell", i].filter(Boolean).join(" "), _ = u ? "key:tooltip" : void 0;
  return a ? /* @__PURE__ */ e.createElement(
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
}, dr = 20, mr = () => {
  var k;
  const l = G(), t = re(), n = l.nodes ?? [], r = l.selectionMode ?? "single", c = l.dragEnabled ?? !1, s = l.dropEnabled ?? !1, i = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [a, o] = e.useState(-1), m = e.useRef(null), p = ((k = n.find((h) => h.selected)) == null ? void 0 : k.id) ?? null;
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
  }, []), v = e.useCallback((h, I) => {
    I.dataTransfer.effectAllowed = "move", I.dataTransfer.setData("text/plain", h);
  }, []), g = e.useCallback((h, I) => {
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
    let I = a;
    switch (h.key) {
      case "ArrowDown":
        h.preventDefault(), I = Math.min(a + 1, n.length - 1);
        break;
      case "ArrowUp":
        h.preventDefault(), I = Math.max(a - 1, 0);
        break;
      case "ArrowRight":
        if (h.preventDefault(), a >= 0 && a < n.length) {
          const T = n[a];
          if (T.expandable && !T.expanded) {
            t("expand", { nodeId: T.id });
            return;
          } else T.expanded && (I = a + 1);
        }
        break;
      case "ArrowLeft":
        if (h.preventDefault(), a >= 0 && a < n.length) {
          const T = n[a];
          if (T.expanded) {
            t("collapse", { nodeId: T.id });
            return;
          } else {
            const S = T.depth;
            for (let H = a - 1; H >= 0; H--)
              if (n[H].depth < S) {
                I = H;
                break;
              }
          }
        }
        break;
      case "Enter":
        h.preventDefault(), a >= 0 && a < n.length && t("select", {
          nodeId: n[a].id,
          ctrlKey: h.ctrlKey || h.metaKey,
          shiftKey: h.shiftKey
        });
        return;
      case " ":
        h.preventDefault(), r === "multi" && a >= 0 && a < n.length && t("select", {
          nodeId: n[a].id,
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
    I !== a && o(I);
  }, [a, n, t, r]);
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
          I === a ? "tlTreeView__node--focused" : "",
          i === h.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          i === h.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          i === h.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: h.depth * dr },
        draggable: c,
        onMouseDown: (T) => {
          (T.shiftKey || T.ctrlKey || T.metaKey || T.detail > 1) && T.preventDefault();
        },
        onClick: (T) => _(h.id, T),
        onContextMenu: (T) => b(h.id, T),
        onDragStart: (T) => v(h.id, T),
        onDragOver: s ? (T) => g(h.id, T) : void 0,
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
var Dt;
function pr() {
  if (Dt) return Z;
  Dt = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), r = Symbol.for("react.strict_mode"), c = Symbol.for("react.profiler"), s = Symbol.for("react.consumer"), i = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), a = Symbol.for("react.suspense"), o = Symbol.for("react.memo"), m = Symbol.for("react.lazy"), p = Symbol.for("react.activity"), f = Symbol.iterator;
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
  function v(d, N, V) {
    this.props = d, this.context = N, this.refs = E, this.updater = V || b;
  }
  v.prototype.isReactComponent = {}, v.prototype.setState = function(d, N) {
    if (typeof d != "object" && typeof d != "function" && d != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, d, N, "setState");
  }, v.prototype.forceUpdate = function(d) {
    this.updater.enqueueForceUpdate(this, d, "forceUpdate");
  };
  function g() {
  }
  g.prototype = v.prototype;
  function x(d, N, V) {
    this.props = d, this.context = N, this.refs = E, this.updater = V || b;
  }
  var L = x.prototype = new g();
  L.constructor = x, w(L, v.prototype), L.isPureReactComponent = !0;
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
  function M(d) {
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
  return Z.Activity = p, Z.Children = X, Z.Component = v, Z.Fragment = n, Z.Profiler = c, Z.PureComponent = x, Z.StrictMode = r, Z.Suspense = a, Z.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = h, Z.__COMPILER_RUNTIME = {
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
      _init: M
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
var Lt;
function fr() {
  return Lt || (Lt = 1, nt.exports = pr()), nt.exports;
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
var xt;
function hr() {
  if (xt) return pe;
  xt = 1;
  var l = fr();
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
  }, c = Symbol.for("react.portal");
  function s(a, o, m) {
    var p = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: c,
      key: p == null ? null : "" + p,
      children: a,
      containerInfo: o,
      implementation: m
    };
  }
  var i = l.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function u(a, o) {
    if (a === "font") return "";
    if (typeof o == "string")
      return o === "use-credentials" ? o : "";
  }
  return pe.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = r, pe.createPortal = function(a, o) {
    var m = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!o || o.nodeType !== 1 && o.nodeType !== 9 && o.nodeType !== 11)
      throw Error(t(299));
    return s(a, o, null, m);
  }, pe.flushSync = function(a) {
    var o = i.T, m = r.p;
    try {
      if (i.T = null, r.p = 2, a) return a();
    } finally {
      i.T = o, r.p = m, r.d.f();
    }
  }, pe.preconnect = function(a, o) {
    typeof a == "string" && (o ? (o = o.crossOrigin, o = typeof o == "string" ? o === "use-credentials" ? o : "" : void 0) : o = null, r.d.C(a, o));
  }, pe.prefetchDNS = function(a) {
    typeof a == "string" && r.d.D(a);
  }, pe.preinit = function(a, o) {
    if (typeof a == "string" && o && typeof o.as == "string") {
      var m = o.as, p = u(m, o.crossOrigin), f = typeof o.integrity == "string" ? o.integrity : void 0, _ = typeof o.fetchPriority == "string" ? o.fetchPriority : void 0;
      m === "style" ? r.d.S(
        a,
        typeof o.precedence == "string" ? o.precedence : void 0,
        {
          crossOrigin: p,
          integrity: f,
          fetchPriority: _
        }
      ) : m === "script" && r.d.X(a, {
        crossOrigin: p,
        integrity: f,
        fetchPriority: _,
        nonce: typeof o.nonce == "string" ? o.nonce : void 0
      });
    }
  }, pe.preinitModule = function(a, o) {
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
  }, pe.preload = function(a, o) {
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
  }, pe.preloadModule = function(a, o) {
    if (typeof a == "string")
      if (o) {
        var m = u(o.as, o.crossOrigin);
        r.d.m(a, {
          as: typeof o.as == "string" && o.as !== "script" ? o.as : void 0,
          crossOrigin: m,
          integrity: typeof o.integrity == "string" ? o.integrity : void 0
        });
      } else r.d.m(a);
  }, pe.requestFormReset = function(a) {
    r.d.r(a);
  }, pe.unstable_batchedUpdates = function(a, o) {
    return a(o);
  }, pe.useFormState = function(a, o, m) {
    return i.H.useFormState(a, o, m);
  }, pe.useFormStatus = function() {
    return i.H.useHostTransitionStatus();
  }, pe.version = "19.2.4", pe;
}
var It;
function br() {
  if (It) return tt.exports;
  It = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), tt.exports = hr(), tt.exports;
}
var Ht = br();
const { useState: Te, useCallback: fe, useRef: $e, useEffect: Pe, useMemo: ut } = e;
function ft({ image: l }) {
  return l ? l.startsWith("/") ? /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" }) : /* @__PURE__ */ e.createElement(qe, { image: l, className: "tlDropdownSelect__optionIcon" }) : null;
}
function _r({
  option: l,
  removable: t,
  onRemove: n,
  removeLabel: r,
  draggable: c,
  onDragStart: s,
  onDragOver: i,
  onDrop: u,
  onDragEnd: a,
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
      onDragEnd: a
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
        "aria-label": r
      },
      "×"
    )
  );
}
function vr({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: r,
  onMouseEnter: c,
  id: s
}) {
  const i = fe(() => r(l.value), [r, l.value]), u = ut(() => {
    if (!n) return l.label;
    const a = l.label.toLowerCase().indexOf(n.toLowerCase());
    return a < 0 ? l.label : /* @__PURE__ */ e.createElement(e.Fragment, null, l.label.substring(0, a), /* @__PURE__ */ e.createElement("strong", null, l.label.substring(a, a + n.length)), l.label.substring(a + n.length));
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
const gr = ({ controlId: l, state: t }) => {
  const n = re(), r = t.value ?? [], c = t.multiSelect === !0, s = t.customOrder === !0, i = t.mandatory === !0, u = t.disabled === !0, a = t.editable !== !1, o = t.optionsLoaded === !0, m = t.options ?? [], p = t.emptyOptionLabel ?? "", f = s && c && !u && a, _ = ie({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), b = _["js.dropdownSelect.nothingFound"], w = fe(
    (P) => _["js.dropdownSelect.removeChip"].replace("{0}", P),
    [_]
  ), [E, v] = Te(!1), [g, x] = Te(""), [L, C] = Te(-1), [k, h] = Te(!1), [I, T] = Te({}), [S, H] = Te(null), [B, R] = Te(null), [F, Q] = Te(null), W = $e(null), O = $e(null), M = $e(null), j = $e(r);
  j.current = r;
  const X = $e(-1), d = ut(
    () => new Set(r.map((P) => P.value)),
    [r]
  ), N = ut(() => {
    let P = m.filter((Y) => !d.has(Y.value));
    if (g) {
      const Y = g.toLowerCase();
      P = P.filter((ne) => ne.label.toLowerCase().includes(Y));
    }
    return P;
  }, [m, d, g]);
  Pe(() => {
    g && N.length === 1 ? C(0) : C(-1);
  }, [N.length, g]), Pe(() => {
    E && o && O.current && O.current.focus();
  }, [E, o, r]), Pe(() => {
    var ne, se;
    if (X.current < 0) return;
    const P = X.current;
    X.current = -1;
    const Y = (ne = W.current) == null ? void 0 : ne.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    Y && Y.length > 0 ? Y[Math.min(P, Y.length - 1)].focus() : (se = W.current) == null || se.focus();
  }, [r]), Pe(() => {
    if (!E) return;
    const P = (Y) => {
      W.current && !W.current.contains(Y.target) && M.current && !M.current.contains(Y.target) && (v(!1), x(""));
    };
    return document.addEventListener("mousedown", P), () => document.removeEventListener("mousedown", P);
  }, [E]), Pe(() => {
    if (!E || !W.current) return;
    const P = W.current.getBoundingClientRect(), Y = window.innerHeight - P.bottom, se = Y < 300 && P.top > Y;
    T({
      left: P.left,
      width: P.width,
      ...se ? { bottom: window.innerHeight - P.top } : { top: P.bottom }
    });
  }, [E]);
  const V = fe(async () => {
    if (!(u || !a) && (v(!0), x(""), C(-1), h(!1), !o))
      try {
        await n("loadOptions");
      } catch {
        h(!0);
      }
  }, [u, a, o, n]), $ = fe(() => {
    var P;
    v(!1), x(""), C(-1), (P = W.current) == null || P.focus();
  }, []), q = fe(
    (P) => {
      let Y;
      if (c) {
        const ne = m.find((se) => se.value === P);
        if (ne)
          Y = [...j.current, ne];
        else
          return;
      } else {
        const ne = m.find((se) => se.value === P);
        if (ne)
          Y = [ne];
        else
          return;
      }
      j.current = Y, n(We, { value: Y.map((ne) => ne.value) }), c ? (x(""), C(-1)) : $();
    },
    [c, m, n, $]
  ), A = fe(
    (P) => {
      X.current = j.current.findIndex((ne) => ne.value === P);
      const Y = j.current.filter((ne) => ne.value !== P);
      j.current = Y, n(We, { value: Y.map((ne) => ne.value) });
    },
    [n]
  ), ee = fe(
    (P) => {
      P.stopPropagation(), n(We, { value: [] }), $();
    },
    [n, $]
  ), oe = fe((P) => {
    x(P.target.value);
  }, []), le = fe(
    (P) => {
      if (!E) {
        if (P.key === "ArrowDown" || P.key === "ArrowUp" || P.key === "Enter" || P.key === " ") {
          if (P.target.tagName === "BUTTON") return;
          P.preventDefault(), P.stopPropagation(), V();
        }
        return;
      }
      switch (P.key) {
        case "ArrowDown":
          P.preventDefault(), P.stopPropagation(), C(
            (Y) => Y < N.length - 1 ? Y + 1 : 0
          );
          break;
        case "ArrowUp":
          P.preventDefault(), P.stopPropagation(), C(
            (Y) => Y > 0 ? Y - 1 : N.length - 1
          );
          break;
        case "Enter":
          P.preventDefault(), P.stopPropagation(), L >= 0 && L < N.length && q(N[L].value);
          break;
        case "Escape":
          P.preventDefault(), P.stopPropagation(), $();
          break;
        case "Tab":
          $();
          break;
        case "Backspace":
          g === "" && c && r.length > 0 && A(r[r.length - 1].value);
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
      g,
      c,
      r,
      A
    ]
  ), he = fe(
    async (P) => {
      P.preventDefault(), h(!1);
      try {
        await n("loadOptions");
      } catch {
        h(!0);
      }
    },
    [n]
  ), ve = fe(
    (P, Y) => {
      H(P), Y.dataTransfer.effectAllowed = "move", Y.dataTransfer.setData("text/plain", String(P));
    },
    []
  ), ge = fe(
    (P, Y) => {
      if (Y.preventDefault(), Y.dataTransfer.dropEffect = "move", S === null || S === P) {
        R(null), Q(null);
        return;
      }
      const ne = Y.currentTarget.getBoundingClientRect(), se = ne.left + ne.width / 2, Ie = Y.clientX < se ? "before" : "after";
      R(P), Q(Ie);
    },
    [S]
  ), ye = fe(
    (P) => {
      if (P.preventDefault(), S === null || B === null || F === null || S === B) return;
      const Y = [...j.current], [ne] = Y.splice(S, 1);
      let se = B;
      S < B ? se = F === "before" ? se - 1 : se : se = F === "before" ? se : se + 1, Y.splice(se, 0, ne), j.current = Y, n(We, { value: Y.map((Ie) => Ie.value) }), H(null), R(null), Q(null);
    },
    [S, B, F, n]
  ), ke = fe(() => {
    H(null), R(null), Q(null);
  }, []);
  if (Pe(() => {
    if (L < 0 || !M.current) return;
    const P = M.current.querySelector(
      `[id="${l}-opt-${L}"]`
    );
    P && P.scrollIntoView({ block: "nearest" });
  }, [L, l]), !a)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, r.map((P) => /* @__PURE__ */ e.createElement("span", { key: P.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(ft, { image: P.image }), /* @__PURE__ */ e.createElement("span", null, P.label))));
  const xe = !i && r.length > 0 && !u, He = E ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: M,
      className: "tlDropdownSelect__dropdown",
      style: I,
      ...qt
    },
    (o || k) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: O,
        type: "text",
        className: "tlDropdownSelect__search",
        value: g,
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
      o && N.map((P, Y) => /* @__PURE__ */ e.createElement(
        vr,
        {
          key: P.value,
          id: `${l}-opt-${Y}`,
          option: P,
          highlighted: Y === L,
          searchTerm: g,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : r.map((P, Y) => {
      let ne = "";
      return S === Y ? ne = "tlDropdownSelect__chip--dragging" : B === Y && F === "before" ? ne = "tlDropdownSelect__chip--dropBefore" : B === Y && F === "after" && (ne = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        _r,
        {
          key: P.value,
          option: P,
          removable: !u && (c || !i),
          onRemove: A,
          removeLabel: w(P.label),
          draggable: f,
          onDragStart: f ? (se) => ve(Y, se) : void 0,
          onDragOver: f ? (se) => ge(Y, se) : void 0,
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
  ), He && Ht.createPortal(He, document.body));
}, { useCallback: lt, useRef: Er } = e, Wt = "application/x-tl-color", Cr = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: r,
  onSwap: c,
  onReplace: s
}) => {
  const i = Er(null), u = lt(
    (m) => (p) => {
      i.current = m, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), a = lt((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), o = lt(
    (m) => (p) => {
      p.preventDefault();
      const f = p.dataTransfer.getData(Wt);
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
        onDoubleClick: m != null ? () => r(m) : void 0,
        onDragStart: m != null ? u(p) : void 0,
        onDragOver: a,
        onDrop: o(p)
      }
    ))
  );
};
function zt(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function dt(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function Vt(l) {
  if (!dt(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function Kt(l, t, n) {
  const r = (c) => zt(c).toString(16).padStart(2, "0");
  return "#" + r(l) + r(t) + r(n);
}
function wr(l, t, n) {
  const r = l / 255, c = t / 255, s = n / 255, i = Math.max(r, c, s), u = Math.min(r, c, s), a = i - u;
  let o = 0;
  a !== 0 && (i === r ? o = (c - s) / a % 6 : i === c ? o = (s - r) / a + 2 : o = (r - c) / a + 4, o *= 60, o < 0 && (o += 360));
  const m = i === 0 ? 0 : a / i;
  return [o, m, i];
}
function yr(l, t, n) {
  const r = n * t, c = r * (1 - Math.abs(l / 60 % 2 - 1)), s = n - r;
  let i = 0, u = 0, a = 0;
  return l < 60 ? (i = r, u = c, a = 0) : l < 120 ? (i = c, u = r, a = 0) : l < 180 ? (i = 0, u = r, a = c) : l < 240 ? (i = 0, u = c, a = r) : l < 300 ? (i = c, u = 0, a = r) : (i = r, u = 0, a = c), [
    Math.round((i + s) * 255),
    Math.round((u + s) * 255),
    Math.round((a + s) * 255)
  ];
}
function kr(l) {
  return wr(...Vt(l));
}
function rt(l, t, n) {
  return Kt(...yr(l, t, n));
}
const { useCallback: je, useRef: Mt } = e, Sr = ({ color: l, onColorChange: t }) => {
  const [n, r, c] = kr(l), s = Mt(null), i = Mt(null), u = je(
    (b, w) => {
      var x;
      const E = (x = s.current) == null ? void 0 : x.getBoundingClientRect();
      if (!E) return;
      const v = Math.max(0, Math.min(1, (b - E.left) / E.width)), g = Math.max(0, Math.min(1, 1 - (w - E.top) / E.height));
      t(rt(n, v, g));
    },
    [n, t]
  ), a = je(
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
      var g;
      const w = (g = i.current) == null ? void 0 : g.getBoundingClientRect();
      if (!w) return;
      const v = Math.max(0, Math.min(1, (b - w.top) / w.height)) * 360;
      t(rt(v, r, c));
    },
    [r, c, t]
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
  ), _ = rt(n, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: s,
      className: "tlColorInput__svField",
      style: { backgroundColor: _ },
      onPointerDown: a,
      onPointerMove: o
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__svHandle",
        style: { left: `${r * 100}%`, top: `${(1 - c) * 100}%` }
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
function Nr(l, t) {
  const n = t.toUpperCase();
  return l.some((r) => r != null && r.toUpperCase() === n);
}
const Tr = {
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
}, { useState: Ke, useCallback: Ee, useEffect: Pt, useRef: Rr, useLayoutEffect: Dr } = e, Lr = ({
  anchorRef: l,
  currentColor: t,
  palette: n,
  paletteColumns: r,
  defaultPalette: c,
  canReset: s,
  onConfirm: i,
  onCancel: u,
  onPaletteChange: a
}) => {
  const [o, m] = Ke("palette"), [p, f] = Ke(t), _ = Rr(null), b = ie(Tr), [w, E] = Ke(null);
  Dr(() => {
    if (!l.current || !_.current) return;
    const M = l.current.getBoundingClientRect(), j = _.current.getBoundingClientRect();
    let X = M.bottom + 4, d = M.left;
    X + j.height > window.innerHeight && (X = M.top - j.height - 4), d + j.width > window.innerWidth && (d = Math.max(0, M.right - j.width)), E({ top: X, left: d });
  }, [l]);
  const v = p != null, [g, x, L] = v ? Vt(p) : [0, 0, 0], [C, k] = Ke((p == null ? void 0 : p.toUpperCase()) ?? "");
  Pt(() => {
    k((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), Le(!0, { ESCAPE: u }), Pt(() => {
    const M = (X) => {
      _.current && !_.current.contains(X.target) && u();
    }, j = setTimeout(() => document.addEventListener("mousedown", M), 0);
    return () => {
      clearTimeout(j), document.removeEventListener("mousedown", M);
    };
  }, [u]);
  const h = Ee(
    (M) => (j) => {
      const X = parseInt(j.target.value, 10);
      if (isNaN(X)) return;
      const d = zt(X);
      f(Kt(M === "r" ? d : g, M === "g" ? d : x, M === "b" ? d : L));
    },
    [g, x, L]
  ), I = Ee(
    (M) => {
      if (p != null) {
        M.dataTransfer.setData(Wt, p.toUpperCase()), M.dataTransfer.effectAllowed = "move";
        const j = document.createElement("div");
        j.style.width = "33px", j.style.height = "33px", j.style.backgroundColor = p, j.style.borderRadius = "3px", j.style.border = "1px solid rgba(0,0,0,0.1)", j.style.position = "absolute", j.style.top = "-9999px", document.body.appendChild(j), M.dataTransfer.setDragImage(j, 16, 16), requestAnimationFrame(() => document.body.removeChild(j));
      }
    },
    [p]
  ), T = Ee((M) => {
    const j = M.target.value;
    k(j), dt(j) && f(j);
  }, []), S = Ee(() => {
    f(null);
  }, []), H = Ee((M) => {
    f(M);
  }, []), B = Ee(
    (M) => {
      i(M);
    },
    [i]
  ), R = Ee(
    (M, j) => {
      const X = [...n], d = X[M];
      X[M] = X[j], X[j] = d, a(X);
    },
    [n, a]
  ), F = Ee(
    (M, j) => {
      const X = [...n];
      X[M] = j, a(X);
    },
    [n, a]
  ), Q = Ee(() => {
    a([...c]);
  }, [c, a]), W = Ee(
    (M) => {
      if (Nr(n, M)) return;
      const j = n.indexOf(null);
      if (j < 0) return;
      const X = [...n];
      X[j] = M.toUpperCase(), a(X);
    },
    [n, a]
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
      Cr,
      {
        colors: n,
        columns: r,
        onSelect: H,
        onConfirm: B,
        onSwap: R,
        onReplace: F
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: Q }, b["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(Sr, { color: p ?? "#000000", onColorChange: f }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (v ? "" : " tlColorInput--noColor"),
        style: v ? { backgroundColor: p } : void 0,
        draggable: v,
        onDragStart: v ? I : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? g : "",
        onChange: h("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? x : "",
        onChange: h("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? L : "",
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
}, xr = { "js.colorInput.chooseColor": "Choose color" }, { useState: Ir, useCallback: Ye, useRef: Mr } = e, Pr = ({ controlId: l, state: t }) => {
  const [n, r] = Re(), c = re(), s = ie(xr), [i, u] = Ir(!1), a = Mr(null), o = n, m = t.editable !== !1, p = t.palette ?? [], f = t.paletteColumns ?? 6, _ = t.defaultPalette ?? p, b = Ye(() => {
    m && u(!0);
  }, [m]), w = Ye(
    (g) => {
      u(!1), r(g);
    },
    [r]
  ), E = Ye(() => {
    u(!1);
  }, []), v = Ye(
    (g) => {
      c("paletteChanged", { palette: g });
    },
    [c]
  );
  return m ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: a,
      className: "tlColorInput__swatch" + (o == null ? " tlColorInput__swatch--noColor" : ""),
      style: o != null ? { backgroundColor: o } : void 0,
      onClick: b,
      disabled: t.disabled === !0,
      title: o ?? "",
      "aria-label": s["js.colorInput.chooseColor"]
    }
  ), i && /* @__PURE__ */ e.createElement(
    Lr,
    {
      anchorRef: a,
      currentColor: o,
      palette: p,
      paletteColumns: f,
      defaultPalette: _,
      canReset: t.canReset !== !1,
      onConfirm: w,
      onCancel: E,
      onPaletteChange: v
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
}, { useState: Ue, useCallback: De, useEffect: at, useRef: jt, useLayoutEffect: jr, useMemo: Ar } = e, Br = {
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
}, Fr = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: r,
  onSelect: c,
  onCancel: s,
  onLoadIcons: i
}) => {
  const u = ie(Br), [a, o] = Ue("simple"), [m, p] = Ue(""), [f, _] = Ue(t ?? ""), [b, w] = Ue(!1), [E, v] = Ue(null), g = jt(null), x = jt(null);
  jr(() => {
    if (!l.current || !g.current) return;
    const B = l.current.getBoundingClientRect(), R = g.current.getBoundingClientRect();
    let F = B.bottom + 4, Q = B.left;
    F + R.height > window.innerHeight && (F = B.top - R.height - 4), Q + R.width > window.innerWidth && (Q = Math.max(0, B.right - R.width)), v({ top: F, left: Q });
  }, [l]), at(() => {
    !r && !b && i().catch(() => w(!0));
  }, [r, b, i]), at(() => {
    r && x.current && x.current.focus();
  }, [r]), Le(!0, { ESCAPE: s }), at(() => {
    const B = (F) => {
      g.current && !g.current.contains(F.target) && s();
    }, R = setTimeout(() => document.addEventListener("mousedown", B), 0);
    return () => {
      clearTimeout(R), document.removeEventListener("mousedown", B);
    };
  }, [s]);
  const L = Ar(() => {
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
      ref: g,
      style: E ? { top: E.top, left: E.left, visibility: "visible" } : { visibility: "hidden" }
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
      !r && !b && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      b && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: H }, u["js.iconSelect.loadError"])),
      r && L.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      r && L.map(
        (B) => B.variants.map((R) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: R.encoded,
            className: "tlIconSelect__iconCell" + (R.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": R.encoded === t,
            tabIndex: 0,
            title: B.label,
            onClick: () => a === "simple" ? k(R.encoded) : h(R.encoded),
            onKeyDown: (F) => {
              (F.key === "Enter" || F.key === " ") && (F.preventDefault(), a === "simple" ? k(R.encoded) : h(R.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(we, { encoded: R.encoded })
        ))
      )
    ),
    a === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: f,
        onChange: I
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, f && /* @__PURE__ */ e.createElement(we, { encoded: f })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, f ? f.startsWith("css:") ? f.substring(4) : f : ""))),
    a === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: s }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: S }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: T }, u["js.iconSelect.ok"]))
  );
}, Or = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: $r, useCallback: Ge, useRef: Ur } = e, Hr = ({ controlId: l, state: t }) => {
  const [n, r] = Re(), c = re(), s = ie(Or), [i, u] = $r(!1), a = Ur(null), o = n, m = t.editable !== !1, p = t.disabled === !0, f = t.icons ?? [], _ = t.iconsLoaded === !0, b = Ge(() => {
    m && !p && u(!0);
  }, [m, p]), w = Ge(
    (g) => {
      u(!1), r(g);
    },
    [r]
  ), E = Ge(() => {
    u(!1);
  }, []), v = Ge(async () => {
    await c("loadIcons");
  }, [c]);
  return m ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: a,
      className: "tlIconSelect__swatch" + (o == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: b,
      disabled: p,
      title: o ?? "",
      "aria-label": s["js.iconSelect.chooseIcon"]
    },
    o ? /* @__PURE__ */ e.createElement(we, { encoded: o }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), i && /* @__PURE__ */ e.createElement(
    Fr,
    {
      anchorRef: a,
      currentValue: o,
      icons: f,
      iconsLoaded: _,
      onSelect: w,
      onCancel: E,
      onLoadIcons: v
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, o ? /* @__PURE__ */ e.createElement(we, { encoded: o }) : null));
}, { useCallback: Ae, useEffect: Wr, useMemo: At, useRef: zr, useState: ot } = e, Vr = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, Kr = [1, 2, 3, 4];
function Yr(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const r = parseFloat(n[1]), c = n[2] || "px";
  return c === "rem" || c === "em" ? r * t : r;
}
function Gr(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let r = 1;
  for (const c of Kr)
    n >= c && (r = c);
  return r;
}
function Xr(l, t) {
  const n = Vr[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function qr(l, t) {
  const n = Math.max(1, t), r = {}, c = (p, f) => !!(r[p] && r[p][f]), s = (p, f) => {
    r[p] || (r[p] = {}), r[p][f] = !0;
  }, i = [];
  let u = 0, a = 0;
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
    let _ = Math.min(Xr(p.width, n), n);
    for (; c(u, a); )
      a++, a >= n && (a = 0, u++);
    let b = 0;
    for (let x = a; x < n && !c(u, x); x++)
      b++;
    if (_ > b) {
      for (o(u), a = 0, u++; c(u, a); )
        a++, a >= n && (a = 0, u++);
      b = 0;
      for (let x = a; x < n && !c(u, x); x++)
        b++;
      _ = Math.min(_, b);
    }
    const w = a, E = a + _, v = u, g = u + f;
    i.push({ id: p.id, colStart: w, colEnd: E, rowStart: v, rowEnd: g });
    for (let x = v; x < g; x++)
      for (let L = w; L < E; L++) s(x, L);
    a = E, a >= n && (a = 0, u++);
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
const Zr = ({ controlId: l }) => {
  const t = G(), n = re(), r = t.minColWidth ?? "16rem", c = (t.children ?? []).filter((k) => k && k.id), s = zr(null), [i, u] = ot(1), a = t.editMode === !0;
  Wr(() => {
    const k = s.current;
    if (!k) return;
    const h = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, I = Yr(r, h), T = () => u(Gr(k.clientWidth, I));
    T();
    const S = new ResizeObserver(T);
    return S.observe(k), () => S.disconnect();
  }, [r]);
  const o = At(() => qr(c, i), [c, i]), m = At(() => {
    const k = {};
    for (const h of o) k[h.id] = h;
    return k;
  }, [o]), [p, f] = ot(null), [_, b] = ot(null), w = Ae((k, h) => {
    if (!a) {
      k.preventDefault();
      return;
    }
    f(h), k.dataTransfer.effectAllowed = "move", k.dataTransfer.setData("text/plain", h);
  }, [a]), E = Ae((k, h) => {
    if (!a || !p || p === h) return;
    k.preventDefault(), k.dataTransfer.dropEffect = "move";
    const I = k.currentTarget.getBoundingClientRect(), T = k.clientX < I.left + I.width / 2;
    b((S) => S && S.id === h && S.before === T ? S : { id: h, before: T });
  }, [a, p]), v = Ae(() => {
  }, []), g = Ae((k, h, I) => {
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
    if (!a || !p || p === h) return;
    k.preventDefault();
    const I = k.currentTarget.getBoundingClientRect(), T = k.clientX < I.left + I.width / 2;
    g(p, h, T), f(null), b(null);
  }, [a, p, g]), L = Ae(() => {
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
      className: "tlDashboard" + (a ? " tlDashboard--edit" : "")
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
          draggable: a,
          onDragStart: (S) => w(S, k.id),
          onDragOver: (S) => E(S, k.id),
          onDragLeave: v,
          onDrop: (S) => x(S, k.id),
          onDragEnd: L
        },
        /* @__PURE__ */ e.createElement(K, { control: k.control }),
        a && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: Qr, useRef: Bt, useState: Ft, useEffect: Jr, useLayoutEffect: ea } = e, ta = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, r) => /* @__PURE__ */ e.createElement("span", { key: r, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(K, { control: n }))));
}, na = ({ group: l }) => {
  var p, f;
  const [t, n] = Ft(!1), [r, c] = Ft({}), s = Bt(null), i = Bt(null), u = Qr(() => {
    n((_) => !_);
  }, []);
  ea(() => {
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
  }, [t]), Jr(() => {
    if (!t) return;
    const _ = (b) => {
      i.current && !i.current.contains(b.target) && s.current && !s.current.contains(b.target) && n(!1);
    };
    return document.addEventListener("mousedown", _), () => document.removeEventListener("mousedown", _);
  }, [t]), Le(t, { ESCAPE: () => n(!1) }), pt(t, i, "first");
  const a = l.items.filter((_) => _ != null);
  if (a.length === 0) return null;
  if (a.length === 1 && !((p = l.subGroups) != null && p.length) && !l.icon)
    return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(K, { control: a[0] })));
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
  ), Ht.createPortal(
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: i,
        className: "tlToolbar__dropdown",
        role: "menu",
        hidden: !t,
        style: t ? r : void 0,
        onClick: () => n(!1)
      },
      a.map((_, b) => /* @__PURE__ */ e.createElement("div", { key: b, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(K, { control: _ }))),
      (f = l.subGroups) == null ? void 0 : f.map((_, b) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${b}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), _.items.map((w, E) => /* @__PURE__ */ e.createElement("div", { key: E, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(K, { control: w })))))
    ),
    document.body
  ));
}, la = ({ controlId: l }) => {
  const r = (G().groups ?? []).filter((c) => c.items.some((s) => s != null));
  return r.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, r.map((c, s) => /* @__PURE__ */ e.createElement(e.Fragment, { key: c.name }, s > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), c.display === "menu" ? /* @__PURE__ */ e.createElement(na, { group: c }) : /* @__PURE__ */ e.createElement(ta, { group: c }))));
}, ra = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlTileStack", style: { width: "100%", height: "100%" } }, t.frame && /* @__PURE__ */ e.createElement(K, { control: t.frame }));
}, aa = ({ controlId: l }) => {
  const t = G(), n = re(), r = t.content, c = t.breadcrumb ?? null;
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
  })), /* @__PURE__ */ e.createElement("div", { className: "tlAdaptiveDetail__content" }, r && /* @__PURE__ */ e.createElement(K, { control: r })));
}, oa = ({ controlId: l }) => {
  const n = G().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((r, c) => /* @__PURE__ */ e.createElement(K, { key: c, control: r })));
}, sa = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), ca = {
  "js.sidebar.openDrawer": "Open navigation"
}, ia = ({ controlId: l }) => {
  const t = re(), n = ie(ca);
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
U("TLButton", mn);
U("TLUploadButton", pn);
U("TLToggleButton", hn);
U("TLTextInput", Jt);
U("TLPasswordInput", tn);
U("TLNumberInput", ln);
U("TLDatePicker", an);
U("TLSelect", sn);
U("TLCheckbox", un);
U("TLCounter", bn);
U("TLTabBar", vn);
U("TLFieldList", gn);
U("TLAudioRecorder", Cn);
U("TLAudioPlayer", yn);
U("TLFileUpload", Sn);
U("TLBinaryField", Tn);
U("TLFileChips", Ln);
U("TLRelativeTime", Mn);
U("TLAnchor", Pn);
U("TLScrollLink", jn);
U("TLAvatar", Fn);
U("TLDownload", $n);
U("TLPhotoCapture", Hn);
U("TLPhotoViewer", zn);
U("TLPdfViewer", Kn);
U("TLSplitPanel", Yn);
U("TLPanel", el);
U("TLInset", ml);
U("TLMaximizeRoot", tl);
U("TLDeckPane", nl);
U("TLSidebar", ul);
U("TLStack", dl);
U("TLGrid", pl);
U("TLCard", fl);
U("TLAppBar", hl);
U("TLBreadcrumb", _l);
U("TLBottomBar", gl);
U("TLDialog", wl);
U("TLDialogManager", Sl);
U("TLWindow", Dl);
U("TLDrawer", Il);
U("TLContextMenuRegion", Pl);
U("TLSnackbar", Fl);
U("TLMenu", $l);
U("TLAppShell", Hl);
U("TLText", Wl);
U("TLTableView", Yl);
U("TLFormLayout", nr);
U("TLFormGroup", ar);
U("TLFormField", ir);
U("TLResourceCell", ur);
U("TLTreeView", mr);
U("TLDropdownSelect", gr);
U("TLColorInput", Pr);
U("TLIconSelect", Hr);
U("TLDashboard", Zr);
U("TLToolbar", la);
U("TLTileStack", ra);
U("TLAdaptiveDetail", aa);
U("TLSlot", oa);
U("TLSlotContent", sa);
U("TLDrawerToggle", ia);
