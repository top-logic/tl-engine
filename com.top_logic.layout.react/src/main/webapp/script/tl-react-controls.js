import { React as e, useTLFieldValue as Re, useTLCommand as oe, useTLState as G, useKeyboardBinding as me, useTLUpload as Be, TLChild as K, useI18N as ue, useTLDataUrl as Oe, scrollToAnchor as rn, useStandaloneKeyboardScope as Le, KeyboardScopeProvider as bt, useFocusTrap as _t, CMD_VALUE_CHANGED as We, anchoredOverlayProps as on, register as U } from "tl-react-bridge";
const { useCallback: wt, useRef: sn } = e, cn = 300, un = ({ controlId: l, state: t }) => {
  const [n, a, c] = Re({ debounceMs: cn }), s = oe(), i = sn(!1), u = wt(
    (S) => {
      i.current = !0, a(S.target.value);
    },
    [a]
  ), r = t.commitOnBlur === !0, o = wt(async () => {
    await c(), r && i.current && (i.current = !1, s("commit"));
  }, [c, r, s]), m = t.multiline === !0;
  if (t.editable === !1) {
    const S = "tlReactTextInput tlReactTextInput--immutable" + (m ? " tlReactTextInput--multiline" : "");
    return /* @__PURE__ */ e.createElement(
      "span",
      {
        id: l,
        className: S,
        style: m ? { whiteSpace: "pre-wrap" } : void 0
      },
      n ?? ""
    );
  }
  const p = t.hasError === !0, f = t.hasWarnings === !0, g = t.errorMessage, _ = [
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
      className: _,
      "aria-invalid": p || void 0,
      title: p && g ? g : void 0
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
      className: _,
      "aria-invalid": p || void 0,
      title: p && g ? g : void 0
    }
  ));
}, { useCallback: yt } = e, dn = 300, mn = ({ controlId: l, state: t }) => {
  const [n, a, c] = Re({ debounceMs: dn }), s = yt(
    (p) => {
      a(p.target.value);
    },
    [a]
  ), i = yt(() => {
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
}, { useCallback: St } = e, pn = 300, fn = ({ controlId: l, state: t, config: n }) => {
  const [a, c, s] = Re({ debounceMs: pn }), i = St(
    (f) => {
      const g = f.target.value;
      c(g === "" ? null : g);
    },
    [c]
  ), u = St(() => {
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
}, { useCallback: hn } = e, bn = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), c = hn(
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
}, { useCallback: _n } = e, gn = ({ controlId: l, state: t, config: n }) => {
  var m;
  const [a, c] = Re(), s = _n(
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
}, { useCallback: vn, useRef: En, useEffect: Cn } = e, wn = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), c = t.triState === !0, s = En(null);
  Cn(() => {
    s.current && (s.current.indeterminate = c && n !== !0 && n !== !1);
  }, [c, n]);
  const i = vn(
    (m) => {
      if (!c) {
        a(m.target.checked);
        return;
      }
      a(n === !0 ? !1 : n === !1 ? null : !0);
    },
    [a, c, n]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement(
      "input",
      {
        type: "checkbox",
        id: l,
        ref: s,
        checked: n === !0,
        disabled: !0,
        className: "tlReactCheckbox tlReactCheckbox--immutable"
      }
    );
  const u = t.hasError === !0, r = t.hasWarnings === !0, o = [
    "tlReactCheckbox",
    u ? "tlReactCheckbox--error" : "",
    !u && r ? "tlReactCheckbox--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: l,
      ref: s,
      checked: n === !0,
      onChange: i,
      disabled: t.disabled === !0,
      className: o,
      "aria-invalid": u || void 0,
      "aria-checked": c && n !== !0 && n !== !1 ? "mixed" : n === !0
    }
  );
};
function ye({ encoded: l, className: t }) {
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
const { useCallback: yn } = e, Sn = ({ controlId: l, command: t, label: n, image: a, disabled: c, displayMode: s }) => {
  const i = G(), u = oe(), r = t ?? "click", o = n ?? i.label, m = a ?? i.image, p = c ?? i.disabled === !0, f = s ?? i.displayMode ?? "label-only", g = i.hidden === !0, _ = i.tooltip, S = i.appearance, C = i.size, v = i.navigateUrl, y = yn(() => {
    if (v) {
      window.location.assign(v);
      return;
    }
    u(r);
  }, [u, r, v]), I = i.keyGesture;
  me(I, () => p || g ? !1 : (y(), !0));
  const L = f === "icon-only", b = f === "label-only" || f === "icon-label" || L && !m, w = _ ?? (L ? o : void 0), h = w ? `text:${w}` : void 0;
  return g ? null : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: y,
      disabled: p,
      className: "tlReactButton" + (L ? " tlReactButton--iconOnly" : "") + (f === "label-only" ? " tlReactButton--labelOnly" : "") + (S === "link" ? " tlReactButton--link" : "") + (S === "primary" ? " tlReactButton--primary" : "") + (C === "small" ? " tlReactButton--small" : "") + (C === "large" ? " tlReactButton--large" : ""),
      "data-tooltip": h,
      "aria-label": m || L ? o : void 0
    },
    m && /* @__PURE__ */ e.createElement(ye, { encoded: m, className: "tlReactButton__image" }),
    b && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, o)
  );
}, kn = ({ controlId: l }) => {
  const t = G(), n = Be(), a = e.useRef(null), [c, s] = e.useState(!1), i = t.label ?? "", u = t.image, r = t.disabled === !0, o = t.hidden === !0, m = t.displayMode ?? "label-only", p = t.appearance, f = t.accept, g = t.multiple === !0, _ = e.useCallback(() => {
    var L;
    r || c || (L = a.current) == null || L.click();
  }, [r, c]), S = e.useCallback(async (L) => {
    const b = L.target.files;
    if (!b || b.length === 0) return;
    const w = new FormData();
    for (let h = 0; h < b.length; h++)
      w.append("file", b[h], b[h].name);
    L.target.value = "", s(!0);
    try {
      await n(w);
    } finally {
      s(!1);
    }
  }, [n]), C = m === "icon-only", v = m === "icon-only" || m === "icon-label", y = m === "label-only" || m === "icon-label" || C && !u, I = r || c;
  return /* @__PURE__ */ e.createElement("span", { id: l, style: { display: "contents" } }, /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: a,
      type: "file",
      accept: f && f !== "*" ? f : void 0,
      multiple: g || void 0,
      onChange: S,
      style: { display: "none" }
    }
  ), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: _,
      disabled: I,
      style: o ? { display: "none" } : void 0,
      className: "tlReactButton" + (C ? " tlReactButton--iconOnly" : "") + (p === "link" ? " tlReactButton--link" : "") + (p === "primary" ? " tlReactButton--primary" : ""),
      "aria-label": C ? i : void 0
    },
    v && u && /* @__PURE__ */ e.createElement(ye, { encoded: u, className: "tlReactButton__image" }),
    y && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, i)
  ));
}, { useCallback: Nn } = e, Tn = ({ controlId: l, command: t, label: n, active: a, disabled: c }) => {
  const s = G(), i = oe(), u = t ?? "click", r = n ?? s.label, o = a ?? s.active === !0, m = c ?? s.disabled === !0, p = Nn(() => {
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
}, Rn = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.count ?? 0, c = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Dn } = e, Ln = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.tabs ?? [], c = t.activeTabId, s = Dn((i) => {
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
    i.icon && /* @__PURE__ */ e.createElement(ye, { encoded: i.icon, className: "tlReactTabBar__tabIcon" }),
    i.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(K, { control: t.activeContent })));
}, xn = ({ controlId: l }) => {
  const t = G(), n = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((c, s) => /* @__PURE__ */ e.createElement("div", { key: s, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(K, { control: c })))));
}, In = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Pn = ({ controlId: l }) => {
  const t = G(), n = Be(), [a, c] = e.useState("idle"), [s, i] = e.useState(null), u = e.useRef(null), r = e.useRef([]), o = e.useRef(null), m = t.status ?? "idle", p = t.error, f = m === "received" ? "idle" : a !== "idle" ? a : m, g = e.useCallback(async () => {
    if (a === "recording") {
      const y = u.current;
      y && y.state !== "inactive" && y.stop();
      return;
    }
    if (a !== "uploading") {
      if (i(null), !window.isSecureContext || !navigator.mediaDevices) {
        i("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const y = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        o.current = y, r.current = [];
        const I = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", L = new MediaRecorder(y, I ? { mimeType: I } : void 0);
        u.current = L, L.ondataavailable = (b) => {
          b.data.size > 0 && r.current.push(b.data);
        }, L.onstop = async () => {
          y.getTracks().forEach((h) => h.stop()), o.current = null;
          const b = new Blob(r.current, { type: L.mimeType || "audio/webm" });
          if (r.current = [], b.size === 0) {
            c("idle");
            return;
          }
          c("uploading");
          const w = new FormData();
          w.append("audio", b, "recording.webm"), await n(w), c("idle");
        }, L.start(), c("recording");
      } catch (y) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", y), i("js.audioRecorder.error.denied"), c("idle");
      }
    }
  }, [a, n]), _ = ue(In), S = f === "recording" ? _["js.audioRecorder.stop"] : f === "uploading" ? _["js.uploading"] : _["js.audioRecorder.record"], C = f === "uploading", v = ["tlAudioRecorder__button"];
  return f === "recording" && v.push("tlAudioRecorder__button--recording"), f === "uploading" && v.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: v.join(" "),
      onClick: g,
      disabled: C,
      title: S,
      "aria-label": S
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${f === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, _[s]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, Mn = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, jn = ({ controlId: l }) => {
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
        const C = await fetch(n);
        if (!C.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", C.status), i("idle");
          return;
        }
        const v = await C.blob();
        r.current = URL.createObjectURL(v);
      } catch (C) {
        console.error("[TLAudioPlayer] Fetch error:", C), i("idle");
        return;
      }
    }
    const S = new Audio(r.current);
    u.current = S, S.onended = () => {
      i("idle");
    }, S.play(), i("playing");
  }, [s, n]), p = ue(Mn), f = s === "loading" ? p["js.loading"] : s === "playing" ? p["js.audioPlayer.pause"] : s === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], g = s === "disabled" || s === "loading", _ = ["tlAudioPlayer__button"];
  return s === "playing" && _.push("tlAudioPlayer__button--playing"), s === "loading" && _.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: _.join(" "),
      onClick: m,
      disabled: g,
      title: f,
      "aria-label": f
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${s === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, An = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Bn = ({ controlId: l }) => {
  const t = G(), n = Be(), [a, c] = e.useState("idle"), [s, i] = e.useState(!1), u = e.useRef(null), r = t.status ?? "idle", o = t.error, m = t.accept ?? "", p = r === "received" ? "idle" : a !== "idle" ? a : r, f = e.useCallback(async (b) => {
    c("uploading");
    const w = new FormData();
    w.append("file", b, b.name), await n(w), c("idle");
  }, [n]), g = e.useCallback((b) => {
    var h;
    const w = (h = b.target.files) == null ? void 0 : h[0];
    w && f(w);
  }, [f]), _ = e.useCallback(() => {
    var b;
    a !== "uploading" && ((b = u.current) == null || b.click());
  }, [a]), S = e.useCallback((b) => {
    b.preventDefault(), b.stopPropagation(), i(!0);
  }, []), C = e.useCallback((b) => {
    b.preventDefault(), b.stopPropagation(), i(!1);
  }, []), v = e.useCallback((b) => {
    var h;
    if (b.preventDefault(), b.stopPropagation(), i(!1), a === "uploading") return;
    const w = (h = b.dataTransfer.files) == null ? void 0 : h[0];
    w && f(w);
  }, [a, f]), y = p === "uploading", I = ue(An), L = p === "uploading" ? I["js.uploading"] : I["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${s ? " tlFileUpload--dragover" : ""}`,
      onDragOver: S,
      onDragLeave: C,
      onDrop: v
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: u,
        type: "file",
        accept: m || void 0,
        onChange: g,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (p === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: _,
        disabled: y,
        title: L,
        "aria-label": L
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    o && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, o)
  );
}, On = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, Fn = ({ controlId: l, state: t }) => {
  const a = G() ?? t ?? {}, c = Be(), s = Oe(), i = ue(On), u = a.editable !== !1, r = !!a.hasData, o = a.fileName ?? "download", m = a.dataRevision ?? 0, p = a.accept ?? "", f = a.status ?? "idle", g = a.error ?? null, [_, S] = e.useState("idle"), [C, v] = e.useState(!1), [y, I] = e.useState(!1), L = e.useRef(null), b = e.useCallback(async () => {
    if (!(!r || y)) {
      I(!0);
      try {
        const F = s + (s.includes("?") ? "&" : "?") + "rev=" + m, B = await fetch(F);
        if (!B.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", B.status);
          return;
        }
        const M = await B.blob(), q = URL.createObjectURL(M), d = document.createElement("a");
        d.href = q, d.download = o, d.style.display = "none", document.body.appendChild(d), d.click(), document.body.removeChild(d), URL.revokeObjectURL(q);
      } catch (F) {
        console.error("[TLBinaryField] Fetch error:", F);
      } finally {
        I(!1);
      }
    }
  }, [r, y, s, m, o]), w = e.useCallback(async (F) => {
    S("uploading");
    const B = new FormData();
    B.append("file", F, F.name), await c(B), S("idle");
  }, [c]), h = (f === "received" ? "idle" : _ !== "idle" ? _ : f) === "uploading", D = e.useCallback((F) => {
    var M;
    const B = (M = F.target.files) == null ? void 0 : M[0];
    B && w(B);
  }, [w]), R = e.useCallback(() => {
    var F;
    h || (F = L.current) == null || F.click();
  }, [h]), N = e.useCallback((F) => {
    F.preventDefault(), F.stopPropagation(), v(!0);
  }, []), z = e.useCallback((F) => {
    F.preventDefault(), F.stopPropagation(), v(!1);
  }, []), A = e.useCallback((F) => {
    var M;
    if (F.preventDefault(), F.stopPropagation(), v(!1), h) return;
    const B = (M = F.dataTransfer.files) == null ? void 0 : M[0];
    B && w(B);
  }, [h, w]), x = y ? i["js.downloading"] : i["js.download.file"].replace("{0}", o), O = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (y ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: b,
      disabled: y,
      title: x,
      "aria-label": x
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o));
  if (!u)
    return r ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, O) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, i["js.download.noFile"]));
  const Z = h, H = h ? i["js.uploading"] : i["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlBinaryField tlFileUpload${C ? " tlFileUpload--dragover" : ""}`,
      onDragOver: N,
      onDragLeave: z,
      onDrop: A
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: L,
        type: "file",
        accept: p || void 0,
        onChange: D,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (Z ? " tlFileUpload__button--uploading" : ""),
        onClick: R,
        disabled: Z,
        title: H,
        "aria-label": H
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    r && O,
    g && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, g)
  );
}, $n = {
  "js.fileChips.add": "Add file",
  "js.fileChips.remove": "Remove {0}",
  "js.uploading": "Uploading…",
  "js.download.file": "Download {0}"
};
function Un(l) {
  return l < 1024 ? l + " B" : l < 1024 * 1024 ? (l / 1024).toFixed(l < 10 * 1024 ? 1 : 0) + " KB" : (l / (1024 * 1024)).toFixed(1) + " MB";
}
const Hn = ({ controlId: l }) => {
  const t = G(), n = oe(), a = Be(), c = Oe(), s = ue($n), i = t.chips ?? [], u = t.editable === !0, [r, o] = e.useState(!1), [m, p] = e.useState(!1), f = e.useRef(null), g = e.useCallback(async (b) => {
    const w = Array.from(b);
    if (w.length !== 0) {
      o(!0);
      try {
        const h = new FormData();
        for (const D of w)
          h.append("file", D, D.name);
        await a(h);
      } finally {
        o(!1);
      }
    }
  }, [a]), _ = e.useCallback(async (b) => {
    if (b.hasData)
      try {
        const w = c + "&key=" + encodeURIComponent(b.key), h = await fetch(w);
        if (!h.ok) {
          console.error("[TLFileChips] Failed to fetch data:", h.status);
          return;
        }
        const D = await h.blob(), R = URL.createObjectURL(D), N = document.createElement("a");
        N.href = R, N.download = b.name, N.style.display = "none", document.body.appendChild(N), N.click(), document.body.removeChild(N), URL.revokeObjectURL(R);
      } catch (w) {
        console.error("[TLFileChips] Fetch error:", w);
      }
  }, [c]), S = e.useCallback((b) => {
    b.target.files && g(b.target.files), b.target.value = "";
  }, [g]), C = e.useCallback(() => {
    var b;
    r || (b = f.current) == null || b.click();
  }, [r]), v = e.useCallback((b) => {
    u && (b.preventDefault(), b.stopPropagation(), p(!0));
  }, [u]), y = e.useCallback((b) => {
    u && (b.preventDefault(), b.stopPropagation(), p(!1));
  }, [u]), I = e.useCallback((b) => {
    u && (b.preventDefault(), b.stopPropagation(), p(!1), !r && b.dataTransfer.files && g(b.dataTransfer.files));
  }, [u, r, g]), L = [
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
      onDragLeave: y,
      onDrop: I
    },
    i.map((b) => {
      const w = s["js.download.file"].replace("{0}", b.name), h = s["js.fileChips.remove"].replace("{0}", b.name);
      return /* @__PURE__ */ e.createElement("span", { key: b.key, className: "tlFileChip" }, /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__main",
          onClick: () => _(b),
          disabled: !b.hasData,
          title: b.hasData ? w : b.name
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
        /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__name" }, b.name),
        b.size != null && /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__size" }, Un(b.size))
      ), u && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__remove",
          onClick: () => n("removeChip", { key: b.key }),
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
        onChange: S,
        style: { display: "none" }
      }
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileChips__add" + (r ? " tlFileChips__add--uploading" : ""),
        onClick: C,
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
}, Wn = 3e4;
function zn(l, t) {
  const n = Math.round((l - Date.now()) / 1e3), a = Math.abs(n), c = new Intl.RelativeTimeFormat(t, { numeric: "auto" });
  return a < 60 ? c.format(Math.trunc(n / 1), "second") : a < 3600 ? c.format(Math.trunc(n / 60), "minute") : a < 86400 ? c.format(Math.trunc(n / 3600), "hour") : a < 7 * 86400 ? c.format(Math.trunc(n / 86400), "day") : new Date(l).toLocaleDateString(t);
}
const Vn = ({ controlId: l }) => {
  const t = G(), n = t.timestamp, a = t.label ?? void 0, c = t.locale || navigator.language, [, s] = e.useState(0);
  return e.useEffect(() => {
    const i = setInterval(() => s((u) => u + 1), Wn);
    return () => clearInterval(i);
  }, []), n == null ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime tlRelativeTime--empty" }) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime", title: a }, zn(n, c));
}, Kn = ({ controlId: l }) => {
  const t = G(), n = t.anchor ?? void 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAnchor", "data-tl-anchor": n }, t.child && /* @__PURE__ */ e.createElement(K, { control: t.child }));
}, Yn = ({ controlId: l }) => {
  const t = G(), n = t.target, a = t.label ?? "";
  if (n == null)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlScrollLink tlScrollLink--empty" });
  const c = (s) => {
    s.preventDefault(), rn(n);
  };
  return /* @__PURE__ */ e.createElement("a", { id: l, className: "tlScrollLink", href: "#", onClick: c }, a);
};
function Gn(l) {
  const t = l.trim().split(/\s+/).filter(Boolean);
  return t.length === 0 ? "?" : t.length === 1 ? t[0].slice(0, 2).toUpperCase() : (t[0][0] + t[t.length - 1][0]).toUpperCase();
}
function Xn(l) {
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return Math.abs(t) % 360;
}
const qn = ({ controlId: l }) => {
  const n = G().name;
  return n ? /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlAvatar",
      style: { backgroundColor: `hsl(${Xn(n)}, 45%, 45%)` },
      title: n,
      "aria-label": n
    },
    Gn(n)
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlAvatar tlAvatar--empty" });
}, Zn = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Qn = ({ controlId: l }) => {
  const t = G(), n = Oe(), a = oe(), c = !!t.hasData, s = t.dataRevision ?? 0, i = t.fileName ?? "download", u = !!t.clearable, [r, o] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!c || r)) {
      o(!0);
      try {
        const _ = n + (n.includes("?") ? "&" : "?") + "rev=" + s, S = await fetch(_);
        if (!S.ok) {
          console.error("[TLDownload] Failed to fetch data:", S.status);
          return;
        }
        const C = await S.blob(), v = URL.createObjectURL(C), y = document.createElement("a");
        y.href = v, y.download = i, y.style.display = "none", document.body.appendChild(y), y.click(), document.body.removeChild(y), URL.revokeObjectURL(v);
      } catch (_) {
        console.error("[TLDownload] Fetch error:", _);
      } finally {
        o(!1);
      }
    }
  }, [c, r, n, s, i]), p = e.useCallback(async () => {
    c && await a("clear");
  }, [c, a]), f = ue(Zn);
  if (!c)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, f["js.download.noFile"]));
  const g = r ? f["js.downloading"] : f["js.download.file"].replace("{0}", i);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (r ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: m,
      disabled: r,
      title: g,
      "aria-label": g
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
}, Jn = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, el = ({ controlId: l }) => {
  const t = G(), n = Be(), [a, c] = e.useState("idle"), [s, i] = e.useState(null), [u, r] = e.useState(!1), o = e.useRef(null), m = e.useRef(null), p = e.useRef(null), f = e.useRef(null), g = e.useRef(null), _ = t.error, S = e.useMemo(
    () => {
      var N;
      return !!(window.isSecureContext && ((N = navigator.mediaDevices) != null && N.getUserMedia));
    },
    []
  ), C = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((N) => N.stop()), m.current = null), o.current && (o.current.srcObject = null);
  }, []), v = e.useCallback(() => {
    C(), c("idle");
  }, [C]), y = e.useCallback(async () => {
    var N;
    if (a !== "uploading") {
      if (i(null), !S) {
        (N = f.current) == null || N.click();
        return;
      }
      try {
        const z = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        m.current = z, c("overlayOpen");
      } catch (z) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", z), i("js.photoCapture.error.denied"), c("idle");
      }
    }
  }, [a, S]), I = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const N = o.current, z = p.current;
    if (!N || !z)
      return;
    z.width = N.videoWidth, z.height = N.videoHeight;
    const A = z.getContext("2d");
    A && (A.drawImage(N, 0, 0), C(), c("uploading"), z.toBlob(async (x) => {
      if (!x) {
        c("idle");
        return;
      }
      const O = new FormData();
      O.append("photo", x, "capture.jpg"), await n(O), c("idle");
    }, "image/jpeg", 0.85));
  }, [a, n, C]), L = e.useCallback(async (N) => {
    var x;
    const z = (x = N.target.files) == null ? void 0 : x[0];
    if (!z) return;
    c("uploading");
    const A = new FormData();
    A.append("photo", z, z.name), await n(A), c("idle"), f.current && (f.current.value = "");
  }, [n]);
  e.useEffect(() => {
    a === "overlayOpen" && o.current && m.current && (o.current.srcObject = m.current);
  }, [a]), e.useEffect(() => {
    var z;
    if (a !== "overlayOpen") return;
    (z = g.current) == null || z.focus();
    const N = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = N;
    };
  }, [a]), Le(a === "overlayOpen", { ESCAPE: v }), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((N) => N.stop()), m.current = null);
  }, []);
  const b = ue(Jn), w = a === "uploading" ? b["js.uploading"] : b["js.photoCapture.open"], h = ["tlPhotoCapture__cameraBtn"];
  a === "uploading" && h.push("tlPhotoCapture__cameraBtn--uploading");
  const D = ["tlPhotoCapture__overlayVideo"];
  u && D.push("tlPhotoCapture__overlayVideo--mirrored");
  const R = ["tlPhotoCapture__mirrorBtn"];
  return u && R.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: h.join(" "),
      onClick: y,
      disabled: a === "uploading",
      title: w,
      "aria-label": w
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !S && /* @__PURE__ */ e.createElement(
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
      ref: g,
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
        className: D.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: R.join(" "),
        onClick: () => r((N) => !N),
        title: b["js.photoCapture.mirror"],
        "aria-label": b["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: I,
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
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, b[s]), _ && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, _));
}, tl = {
  "js.photoViewer.alt": "Captured photo"
}, nl = ({ controlId: l }) => {
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
  const r = ue(tl);
  return !a || !s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: s,
      alt: r["js.photoViewer.alt"]
    }
  ));
}, ll = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, al = ({ controlId: l }) => {
  const t = G(), n = Oe(), a = !!t.hasPdf, c = t.dataRevision ?? 0, s = ue(ll), u = n.indexOf("react-api/"), r = u >= 0 ? n.slice(0, u) : n, o = n + "&rev=" + c, m = r + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(o);
  return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: m,
      title: s["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, s["js.pdfViewer.noDocument"]));
}, { useCallback: kt, useRef: et } = e, rl = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.orientation, c = t.resizable === !0, s = t.children ?? [], i = a === "horizontal", u = s.length > 0 && s.every((C) => C.collapsed), r = !u && s.some((C) => C.collapsed), o = u ? !i : i, m = et(null), p = et(null), f = et(null), g = kt((C, v) => {
    const y = {
      overflow: C.scrolling || "auto"
    };
    return C.collapsed ? u && !o ? y.flex = "1 0 0%" : y.flex = "0 0 auto" : v !== void 0 ? y.flex = `0 0 ${v}px` : y.flex = `${C.size} 1 0%`, C.minSize > 0 && !C.collapsed && (y.minWidth = i ? C.minSize : void 0, y.minHeight = i ? void 0 : C.minSize), y;
  }, [i, u, r, o]), _ = kt((C, v) => {
    C.preventDefault();
    const y = m.current;
    if (!y) return;
    const I = s[v], L = s[v + 1], b = y.querySelectorAll(":scope > .tlSplitPanel__child"), w = [];
    b.forEach((R) => {
      w.push(i ? R.offsetWidth : R.offsetHeight);
    }), f.current = w, p.current = {
      splitterIndex: v,
      startPos: i ? C.clientX : C.clientY,
      startSizeBefore: w[v],
      startSizeAfter: w[v + 1],
      childBefore: I,
      childAfter: L
    };
    const h = (R) => {
      const N = p.current;
      if (!N || !f.current) return;
      const A = (i ? R.clientX : R.clientY) - N.startPos, x = N.childBefore.minSize || 0, O = N.childAfter.minSize || 0;
      let Z = N.startSizeBefore + A, H = N.startSizeAfter - A;
      Z < x && (H += Z - x, Z = x), H < O && (Z += H - O, H = O), f.current[N.splitterIndex] = Z, f.current[N.splitterIndex + 1] = H;
      const F = y.querySelectorAll(":scope > .tlSplitPanel__child"), B = F[N.splitterIndex], M = F[N.splitterIndex + 1];
      B && (B.style.flex = `0 0 ${Z}px`), M && (M.style.flex = `0 0 ${H}px`);
    }, D = () => {
      if (document.removeEventListener("mousemove", h), document.removeEventListener("mouseup", D), document.body.style.cursor = "", document.body.style.userSelect = "", f.current) {
        const R = {};
        s.forEach((N, z) => {
          const A = N.control;
          A != null && A.controlId && f.current && (R[A.controlId] = f.current[z]);
        }), n("updateSizes", { sizes: R });
      }
      f.current = null, p.current = null;
    };
    document.addEventListener("mousemove", h), document.addEventListener("mouseup", D), document.body.style.cursor = i ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [s, i, n]), S = [];
  return s.forEach((C, v) => {
    if (S.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${v}`,
          className: `tlSplitPanel__child${C.collapsed && o ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: g(C)
        },
        /* @__PURE__ */ e.createElement(K, { control: C.control })
      )
    ), c && v < s.length - 1) {
      const y = s[v + 1];
      !C.collapsed && !y.collapsed && S.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${v}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${a}`,
            onMouseDown: (L) => _(L, v)
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
    S
  );
}, qe = ({ image: l, className: t }) => {
  if (!l) return null;
  const n = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `${t ? t + " " : ""}${n}`, "aria-hidden": "true" });
}, { useCallback: tt } = e, ol = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, sl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), cl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), il = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), ul = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), dl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), ml = ({ controlId: l }) => {
  const t = G(), n = oe(), a = ue(ol), c = t.title, s = t.expansionState ?? "NORMALIZED", i = t.showMinimize === !0, u = t.showMaximize === !0, r = t.showPopOut === !0, o = t.fullLine === !0, m = t.fill === !0, p = t.hoverActions === !0, f = t.appearance === "card", g = t.errorMessage, _ = s === "MINIMIZED", S = s === "MAXIMIZED", C = s === "HIDDEN", v = tt(() => {
    n("toggleMinimize");
  }, [n]), y = tt(() => {
    n("toggleMaximize");
  }, [n]), I = tt(() => {
    n("popOut");
  }, [n]);
  if (C)
    return null;
  const L = S ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" }, b = i && !S || u && !_ || r, w = !!c && c.trim() !== "" || !!t.titleContent || !!t.toolbar || b;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${s.toLowerCase()}${o ? " tlPanel--fullLine" : ""}${m ? " tlPanel--fill" : ""}${p ? " tlPanel--hoverActions" : ""}${f ? " tlPanel--card" : ""}`,
      style: L
    },
    w && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, !!c && c.trim() !== "" && /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, c), t.titleContent && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__titleContent" }, /* @__PURE__ */ e.createElement(K, { control: t.titleContent })), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(K, { control: t.toolbar }), i && !S && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
        title: _ ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      _ ? /* @__PURE__ */ e.createElement(cl, null) : /* @__PURE__ */ e.createElement(sl, null)
    ), u && !_ && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: y,
        title: S ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      S ? /* @__PURE__ */ e.createElement(ul, null) : /* @__PURE__ */ e.createElement(il, null)
    ), r && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: I,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(dl, null)
    ))),
    !_ && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(K, { control: t.child })),
    !_ && g && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error tlPanel__error", role: "alert" }, /* @__PURE__ */ e.createElement(qe, { image: t.errorIcon, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, g)),
    !_ && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(K, { control: t.buttonBar }))
  );
}, pl = ({ controlId: l }) => {
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
}, fl = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(K, { control: t.activeChild }));
}, { useCallback: ge, useState: Xe, useEffect: dt, useRef: Ze } = e, hl = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function mt(l, t, n, a) {
  const c = [];
  for (const s of l)
    if (s.type === "nav") {
      if (s.hidden) continue;
      c.push({ id: s.id, type: "nav", groupId: a });
    } else s.type === "command" ? c.push({ id: s.id, type: "command", groupId: a }) : s.type === "group" && (c.push({ id: s.id, type: "group" }), (n.get(s.id) ?? s.expanded) && !t && c.push(...mt(s.children, t, n, s.id)));
  return c;
}
const Ae = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(ye, { encoded: l, className: "tlSidebar__icon" }) : null, bl = ({ item: l, active: t, collapsed: n, onSelect: a, tabIndex: c, itemRef: s, onFocus: i }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => a(l.id),
    title: n ? l.label : void 0,
    tabIndex: c,
    ref: s,
    onFocus: () => i(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Ae, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Ae, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), _l = ({ item: l, collapsed: t, onExecute: n, tabIndex: a, itemRef: c, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: a,
    ref: c,
    onFocus: () => s(l.id)
  },
  /* @__PURE__ */ e.createElement(Ae, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), gl = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Ae, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), vl = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), El = ({ item: l, activeItemId: t, anchorRect: n, onSelect: a, onExecute: c, onClose: s }) => {
  const i = Ze(null);
  dt(() => {
    const o = (m) => {
      i.current && !i.current.contains(m.target) && setTimeout(() => s(), 0);
    };
    return document.addEventListener("mousedown", o), () => document.removeEventListener("mousedown", o);
  }, [s]), Le(!0, { ESCAPE: s });
  const u = ge((o) => {
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
        /* @__PURE__ */ e.createElement(Ae, { icon: o.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, o.label),
        o.type === "nav" && o.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, o.badge)
      );
    }
    return o.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: o.id, className: "tlSidebar__flyoutSectionHeader" }, o.label) : o.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: o.id, className: "tlSidebar__separator" }) : null;
  }));
}, Cl = ({
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
  flyoutGroupId: g,
  onOpenFlyout: _,
  onCloseFlyout: S
}) => {
  const C = Ze(null), [v, y] = Xe(null), I = ge(() => {
    a ? g === l.id ? S() : (C.current && y(C.current.getBoundingClientRect()), _(l.id)) : i(l.id);
  }, [a, g, l.id, i, _, S]), L = ge((w) => {
    C.current = w, r(w);
  }, [r]), b = a && g === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (b ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: I,
      title: a ? l.label : void 0,
      "aria-expanded": a ? b : t,
      tabIndex: u,
      ref: L,
      onFocus: () => o(l.id)
    },
    /* @__PURE__ */ e.createElement(Ae, { icon: l.icon }),
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
  ), b && /* @__PURE__ */ e.createElement(
    El,
    {
      item: l,
      activeItemId: n,
      anchorRect: v,
      onSelect: c,
      onExecute: s,
      onClose: S
    }
  ), t && !a && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((w) => /* @__PURE__ */ e.createElement(
    Ht,
    {
      key: w.id,
      item: w,
      activeItemId: n,
      collapsed: a,
      onSelect: c,
      onExecute: s,
      onToggleGroup: i,
      focusedId: m,
      setItemRef: p,
      onItemFocus: f,
      groupStates: null,
      flyoutGroupId: g,
      onOpenFlyout: _,
      onCloseFlyout: S
    }
  ))));
}, Ht = ({
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
        bl,
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
        _l,
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
      return /* @__PURE__ */ e.createElement(gl, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(vl, null);
    case "group": {
      const g = o ? o.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        Cl,
        {
          item: l,
          expanded: g,
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
}, wl = ({ controlId: l }) => {
  const t = G(), n = oe(), a = ue(hl), c = t.items ?? [], s = t.activeItemId, i = t.collapsed, u = t.drawerOpen, r = u ? !1 : i, [o, m] = Xe(() => {
    const x = /* @__PURE__ */ new Map(), O = (Z) => {
      for (const H of Z)
        H.type === "group" && (x.set(H.id, H.expanded), O(H.children));
    };
    return O(c), x;
  }), p = ge((x) => {
    m((O) => {
      const Z = new Map(O), H = Z.get(x) ?? !1;
      return Z.set(x, !H), n("toggleGroup", { itemId: x, expanded: !H }), Z;
    });
  }, [n]), f = ge((x) => {
    x !== s && n("selectItem", { itemId: x });
  }, [n, s]), g = ge((x) => {
    n("executeCommand", { itemId: x });
  }, [n]), _ = ge(() => {
    n("toggleCollapse", {});
  }, [n]), S = ge(() => {
    n("toggleDrawer", {});
  }, [n]), [C, v] = Xe(null), y = ge((x) => {
    v(x);
  }, []), I = ge(() => {
    v(null);
  }, []);
  dt(() => {
    r || v(null);
  }, [r]);
  const [L, b] = Xe(() => {
    const x = mt(c, r, o);
    return x.length > 0 ? x[0].id : "";
  }), w = Ze(/* @__PURE__ */ new Map()), h = ge((x) => (O) => {
    O ? w.current.set(x, O) : w.current.delete(x);
  }, []), D = ge((x) => {
    b(x);
  }, []), R = Ze(0), N = ge((x) => {
    b(x), R.current++;
  }, []);
  dt(() => {
    const x = w.current.get(L);
    x && document.activeElement !== x && x.focus();
  }, [L, R.current]);
  const z = ge((x) => {
    if (x.key === "Escape" && C !== null) {
      x.preventDefault(), I();
      return;
    }
    const O = mt(c, r, o);
    if (O.length === 0) return;
    const Z = O.findIndex((F) => F.id === L);
    if (Z < 0) return;
    const H = O[Z];
    switch (x.key) {
      case "ArrowDown": {
        x.preventDefault();
        const F = (Z + 1) % O.length;
        N(O[F].id);
        break;
      }
      case "ArrowUp": {
        x.preventDefault();
        const F = (Z - 1 + O.length) % O.length;
        N(O[F].id);
        break;
      }
      case "Home": {
        x.preventDefault(), N(O[0].id);
        break;
      }
      case "End": {
        x.preventDefault(), N(O[O.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        x.preventDefault(), H.type === "nav" ? f(H.id) : H.type === "command" ? g(H.id) : H.type === "group" && (r ? C === H.id ? I() : y(H.id) : p(H.id));
        break;
      }
      case "ArrowRight": {
        H.type === "group" && !r && ((o.get(H.id) ?? !1) || (x.preventDefault(), p(H.id)));
        break;
      }
      case "ArrowLeft": {
        H.type === "group" && !r && (o.get(H.id) ?? !1) && (x.preventDefault(), p(H.id));
        break;
      }
    }
  }, [
    c,
    r,
    o,
    L,
    C,
    N,
    f,
    g,
    p,
    y,
    I
  ]), A = "tlSidebar" + (r ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: A }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(K, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: S, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": a["js.sidebar.ariaLabel"] }, r ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: z }, c.map((x) => /* @__PURE__ */ e.createElement(
    Ht,
    {
      key: x.id,
      item: x,
      activeItemId: s,
      collapsed: r,
      onSelect: f,
      onExecute: g,
      onToggleGroup: p,
      focusedId: L,
      setItemRef: h,
      onItemFocus: D,
      groupStates: o,
      flyoutGroupId: C,
      onOpenFlyout: y,
      onCloseFlyout: I
    }
  ))), r ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: _,
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
}, yl = ({ controlId: l }) => {
  const t = G(), n = t.direction ?? "column", a = t.gap ?? "default", c = t.align ?? "stretch", s = t.wrap === !0, i = t.growFirst === !0, u = t.children ?? [], r = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${a}`,
    `tlStack--align-${c}`,
    s ? "tlStack--wrap" : "",
    i ? "tlStack--grow-first" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: r }, u.map((o, m) => /* @__PURE__ */ e.createElement(K, { key: m, control: o })));
}, Sl = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlInset" }, t.child && /* @__PURE__ */ e.createElement(K, { control: t.child }));
}, kl = ({ controlId: l }) => {
  const t = G(), n = t.columns, a = t.minColumnWidth, c = t.gap ?? "default", s = t.children ?? [], i = {};
  return a ? i.gridTemplateColumns = `repeat(auto-fit, minmax(min(${a}, 100%), 1fr))` : n && (i.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${c}`, style: i }, s.map((u, r) => /* @__PURE__ */ e.createElement(K, { key: r, control: u })));
}, Nl = ({ controlId: l }) => {
  const t = G(), n = t.title, a = t.variant ?? "outlined", c = t.padding ?? "default", s = t.headerActions ?? [], i = t.child, u = n != null || s.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${a}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, s.map((r, o) => /* @__PURE__ */ e.createElement(K, { key: o, control: r })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${c}` }, /* @__PURE__ */ e.createElement(K, { control: i })));
}, Tl = ({ controlId: l }) => {
  const t = G(), n = t.title ?? "", a = t.leading, c = t.children ?? [], s = t.actions ?? [], i = t.variant ?? "flat", r = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    i === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: r }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(K, { control: a })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, c.map((o, m) => /* @__PURE__ */ e.createElement(K, { key: m, control: o }))), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((o, m) => /* @__PURE__ */ e.createElement(K, { key: m, control: o }))));
}, { useCallback: Rl } = e, Dl = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.items ?? [], c = Rl((s) => {
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
}, { useCallback: Ll } = e, xl = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.items ?? [], c = t.activeItemId, s = Ll((i) => {
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
}, { useCallback: Nt, useRef: Il } = e, Pl = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), Ml = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.open === !0, c = t.closeOnBackdrop !== !1, s = t.child, i = Il(null), u = Nt(() => {
    n("close");
  }, [n]), r = Nt((o) => {
    c && o.target === o.currentTarget && u();
  }, [c, u]);
  return a ? /* @__PURE__ */ e.createElement(bt, null, /* @__PURE__ */ e.createElement(Pl, { onClose: u }), /* @__PURE__ */ e.createElement(
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
}, { useEffect: jl, useRef: Al } = e, Bl = ({ controlId: l }) => {
  const n = G().dialogs ?? [], a = Al(n.length);
  return jl(() => {
    n.length < a.current && n.length > 0, a.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((c) => /* @__PURE__ */ e.createElement(K, { key: c.controlId, control: c })));
}, { useCallback: ze, useRef: Ie, useState: Ve } = e, Ol = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), Fl = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, $l = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Ul = ({ controlId: l }) => {
  const t = G(), n = oe(), a = ue(Fl), c = t.title ?? "", s = t.width ?? "32rem", i = t.height ?? null, u = t.minHeight ?? null, r = t.resizable === !0, o = t.child, m = t.actions ?? [], p = t.toolbar, f = t.buttonBar, [g, _] = Ve(null), [S, C] = Ve(null), [v, y] = Ve(null), I = Ie(null), [L, b] = Ve(!1), w = Ie(null), h = Ie(null), D = Ie(null), R = Ie(null), N = Ie(null), z = ze(() => {
    n("close");
  }, [n]);
  _t(!0, R, "field");
  const A = ze((F, B) => {
    B.preventDefault();
    const M = R.current;
    if (!M) return;
    const q = M.getBoundingClientRect(), d = !I.current, T = I.current ?? { x: q.left, y: q.top };
    d && (I.current = T, y(T)), N.current = {
      dir: F,
      startX: B.clientX,
      startY: B.clientY,
      startW: q.width,
      startH: q.height,
      startPos: { ...T },
      symmetric: d
    };
    const V = (X) => {
      const j = N.current;
      if (!j) return;
      const te = X.clientX - j.startX, ce = X.clientY - j.startY;
      let ne = j.startW, _e = j.startH, ve = 0, Ce = 0;
      j.symmetric ? (j.dir.includes("e") && (ne = j.startW + 2 * te), j.dir.includes("w") && (ne = j.startW - 2 * te), j.dir.includes("s") && (_e = j.startH + 2 * ce), j.dir.includes("n") && (_e = j.startH - 2 * ce)) : (j.dir.includes("e") && (ne = j.startW + te), j.dir.includes("w") && (ne = j.startW - te, ve = te), j.dir.includes("s") && (_e = j.startH + ce), j.dir.includes("n") && (_e = j.startH - ce, Ce = ce));
      const Se = Math.max(200, ne), ke = Math.max(100, _e);
      j.symmetric ? (ve = (j.startW - Se) / 2, Ce = (j.startH - ke) / 2) : (j.dir.includes("w") && Se === 200 && (ve = j.startW - 200), j.dir.includes("n") && ke === 100 && (Ce = j.startH - 100)), h.current = Se, D.current = ke, _(Se), C(ke);
      const xe = {
        x: j.startPos.x + ve,
        y: j.startPos.y + Ce
      };
      I.current = xe, y(xe);
    }, W = () => {
      document.removeEventListener("mousemove", V), document.removeEventListener("mouseup", W);
      const X = h.current, j = D.current;
      (X != null || j != null) && n("resize", {
        ...X != null ? { width: Math.round(X) } : {},
        ...j != null ? { height: Math.round(j) } : {}
      }), N.current = null;
    };
    document.addEventListener("mousemove", V), document.addEventListener("mouseup", W);
  }, [n]), x = ze((F) => {
    if (F.button !== 0 || F.target.closest("button")) return;
    F.preventDefault();
    const B = R.current;
    if (!B) return;
    const M = B.getBoundingClientRect(), q = I.current ?? { x: M.left, y: M.top }, d = F.clientX - q.x, T = F.clientY - q.y, V = (X) => {
      const j = window.innerWidth, te = window.innerHeight;
      let ce = X.clientX - d, ne = X.clientY - T;
      const _e = B.offsetWidth, ve = B.offsetHeight;
      ce + _e > j && (ce = j - _e), ne + ve > te && (ne = te - ve), ce < 0 && (ce = 0), ne < 0 && (ne = 0);
      const Ce = { x: ce, y: ne };
      I.current = Ce, y(Ce);
    }, W = () => {
      document.removeEventListener("mousemove", V), document.removeEventListener("mouseup", W);
    };
    document.addEventListener("mousemove", V), document.addEventListener("mouseup", W);
  }, []), O = ze(() => {
    var F, B;
    if (L) {
      const M = w.current;
      M && (y(M.x !== -1 ? { x: M.x, y: M.y } : null), _(M.w), C(M.h)), b(!1);
    } else {
      const M = R.current, q = M == null ? void 0 : M.getBoundingClientRect();
      w.current = {
        x: ((F = I.current) == null ? void 0 : F.x) ?? (q == null ? void 0 : q.left) ?? -1,
        y: ((B = I.current) == null ? void 0 : B.y) ?? (q == null ? void 0 : q.top) ?? -1,
        w: g ?? (q == null ? void 0 : q.width) ?? null,
        h: S ?? null
      }, b(!0), y({ x: 0, y: 0 }), _(null), C(null);
    }
  }, [L, g, S]), Z = L ? { position: "absolute", top: 0, left: 0, width: "100vw", maxWidth: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: g != null ? g + "px" : s,
    ...S != null ? { height: S + "px" } : i != null ? { height: i } : {},
    ...u != null && S == null ? { minHeight: u } : {},
    maxHeight: v ? "100vh" : "80vh",
    ...v ? { position: "absolute", left: v.x + "px", top: v.y + "px" } : {}
  }, H = l + "-title";
  return /* @__PURE__ */ e.createElement(bt, { modal: !0 }, /* @__PURE__ */ e.createElement(Ol, { onClose: z }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: Z,
      ref: R,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": H
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${L ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: L ? void 0 : x,
        onDoubleClick: r ? O : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: H }, c),
      p && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(K, { control: p })),
      r && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: O,
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
          onClick: z,
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
    (m.length > 0 || f) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, f && /* @__PURE__ */ e.createElement(K, { control: f }), m.map((F, B) => /* @__PURE__ */ e.createElement(K, { key: B, control: F }))),
    r && !L && $l.map((F) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: F,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${F}`,
        onMouseDown: (B) => A(F, B)
      }
    ))
  ));
}, { useCallback: Hl } = e, Wl = {
  "js.drawer.close": "Close"
}, zl = ({ controlId: l }) => {
  const t = G(), n = oe(), a = ue(Wl), c = t.open === !0, s = t.position ?? "right", i = t.size ?? "medium", u = t.title ?? null, r = t.child, o = Hl(() => {
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
}, { useCallback: Vl } = e, Kl = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.child, c = Vl((s) => {
    s.preventDefault(), s.stopPropagation(), n("openContextMenu", { x: s.clientX, y: s.clientY });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tl-context-menu-region", onContextMenu: c }, a && /* @__PURE__ */ e.createElement(K, { control: a }));
}, { useCallback: Yl, useEffect: Tt, useRef: Gl, useState: Rt } = e, Xl = 250, ql = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.message ?? "", c = t.content ?? "", s = t.variant ?? "info", i = t.duration ?? 5e3, u = t.visible === !0, r = t.generation ?? 0, [o, m] = Rt(!1), [p, f] = Rt(!1), g = Gl(!1);
  Tt(() => {
    g.current = !1;
  }, [r]);
  const _ = Yl(() => {
    m(!0), setTimeout(() => {
      n("dismiss", { generation: r }), m(!1);
    }, 200);
  }, [n, r]);
  return Tt(() => {
    if (!u || i === 0 || p) return;
    const S = setTimeout(_, g.current ? Xl : i);
    return () => clearTimeout(S);
  }, [u, i, p, _]), !u && !o ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${s}${o ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite",
      onMouseEnter: () => {
        g.current = !0, f(!0);
      },
      onMouseLeave: () => f(!1)
    },
    c ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: c } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, a)
  );
}, { useCallback: nt, useEffect: Dt, useRef: Zl, useState: Lt } = e, Ql = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.open === !0, c = t.anchorId, s = t.anchorX, i = t.anchorY, u = t.items ?? [], r = Zl(null), [o, m] = Lt({ top: 0, left: 0 }), [p, f] = Lt(0), g = u.filter((v) => v.type === "item" && !v.disabled);
  Dt(() => {
    var h, D;
    if (!a) return;
    const v = ((h = r.current) == null ? void 0 : h.offsetHeight) ?? 200, y = ((D = r.current) == null ? void 0 : D.offsetWidth) ?? 200;
    if (s != null && i != null) {
      let R = i, N = s;
      R + v > window.innerHeight && (R = Math.max(0, window.innerHeight - v)), N + y > window.innerWidth && (N = Math.max(0, window.innerWidth - y)), m({ top: R, left: N }), f(0);
      return;
    }
    if (!c) return;
    const I = document.getElementById(c);
    if (!I) return;
    const L = I.getBoundingClientRect();
    let b = L.bottom + 4, w = L.left;
    b + v > window.innerHeight && (b = L.top - v - 4), w + y > window.innerWidth && (w = L.right - y), m({ top: b, left: w }), f(0);
  }, [a, c, s, i]);
  const _ = nt(() => {
    n("close");
  }, [n]), S = nt((v) => {
    n("selectItem", { itemId: v });
  }, [n]);
  Dt(() => {
    if (!a) return;
    const v = (y) => {
      r.current && !r.current.contains(y.target) && _();
    };
    return document.addEventListener("mousedown", v), () => document.removeEventListener("mousedown", v);
  }, [a, _]);
  const C = nt((v) => {
    if (v.key === "Escape") {
      v.preventDefault(), _();
      return;
    }
    if (v.key === "ArrowDown")
      v.preventDefault(), f((y) => (y + 1) % g.length);
    else if (v.key === "ArrowUp")
      v.preventDefault(), f((y) => (y - 1 + g.length) % g.length);
    else if (v.key === "Enter" || v.key === " ") {
      v.preventDefault();
      const y = g[p];
      y && S(y.id);
    }
  }, [_, S, g, p]);
  return _t(a, r), a ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: r,
      tabIndex: -1,
      style: { position: "fixed", top: o.top, left: o.left },
      onKeyDown: C
    },
    u.map((v, y) => {
      if (v.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: y, className: "tlMenu__separator" });
      const L = g.indexOf(v) === p;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: v.id,
          type: "button",
          className: "tlMenu__item" + (L ? " tlMenu__item--focused" : "") + (v.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: v.disabled,
          tabIndex: L ? 0 : -1,
          onClick: () => S(v.id)
        },
        v.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + v.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, v.label)
      );
    })
  ) : null;
}, Jl = 768, ea = ({ controlId: l }) => {
  const t = G(), n = oe();
  e.useEffect(() => {
    const o = window.matchMedia(`(max-width: ${Jl}px)`), m = (f) => {
      n("reportDisplayClass", { displayClass: f ? "COMPACT" : "REGULAR" });
    };
    m(o.matches);
    const p = (f) => m(f.matches);
    return o.addEventListener("change", p), () => o.removeEventListener("change", p);
  }, [n]);
  const a = t.header, c = t.content, s = t.footer, i = t.snackbar, u = t.dialogManager, r = t.menuOverlay;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(K, { control: a })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(K, { control: c })), s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(K, { control: s })), /* @__PURE__ */ e.createElement(K, { control: i }), u && /* @__PURE__ */ e.createElement(K, { control: u }), r && /* @__PURE__ */ e.createElement(K, { control: r }));
}, ta = ({ controlId: l }) => {
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
}, na = ({ isMulti: l, cursorIndex: t, onMove: n, onToggle: a, onSelectAll: c }) => (me("ArrowUp", () => (n("up", !1, !1), !0)), me("ArrowDown", () => (n("down", !1, !1), !0)), me("Home", () => (n("home", !1, !1), !0)), me("End", () => (n("end", !1, !1), !0)), me("PageUp", () => (n("pageUp", !1, !1), !0)), me("PageDown", () => (n("pageDown", !1, !1), !0)), me("Shift+ArrowUp", () => (n("up", l, !1), !0)), me("Shift+ArrowDown", () => (n("down", l, !1), !0)), me("Shift+Home", () => (n("home", l, !1), !0)), me("Shift+End", () => (n("end", l, !1), !0)), me("Shift+PageUp", () => (n("pageUp", l, !1), !0)), me("Shift+PageDown", () => (n("pageDown", l, !1), !0)), me("Ctrl+ArrowUp", () => (n("up", !1, l), !0)), me("Ctrl+ArrowDown", () => (n("down", !1, l), !0)), me("Space", () => t < 0 ? !1 : (a(), !0)), me("Ctrl+A", () => l ? (c(), !0) : !1), null), la = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all",
  "js.table.freezeSplitter": "Drag to choose the columns that stay in place while scrolling",
  "js.table.filter": "Filter",
  "js.table.columns": "Columns"
}, xt = 50, aa = 'input, textarea, select, button, a, [contenteditable="true"], [role="combobox"], [role="listbox"], [role="option"], [role="button"], [role="link"], [role="checkbox"], [role="radio"], [role="switch"], [role="textbox"], [role="spinbutton"], [role="slider"], [role="menu"], [role="menuitem"]';
function lt(l) {
  var n;
  const t = l.target;
  return !!((n = t == null ? void 0 : t.closest) != null && n.call(t, aa));
}
const pt = 'input:not([disabled]):not([readonly]), textarea:not([disabled]):not([readonly]), select:not([disabled]), [contenteditable="true"]', ra = pt + ", button:not([disabled]), a[href]";
function Wt(l, t) {
  return Array.from(l.querySelectorAll("[data-row][data-col]")).filter((n) => n.dataset.row === t);
}
function at(l, t, n = {}) {
  const a = Wt(l, t);
  if (n.col) {
    const s = a.find((u) => u.dataset.col === n.col), i = s == null ? void 0 : s.querySelector(pt);
    if (i) return i;
  }
  if (n.col)
    return null;
  const c = n.last ? [...a].reverse() : a;
  for (const s of c) {
    const i = s.querySelector(pt);
    if (i) return i;
  }
  return null;
}
const oa = ({ controlId: l }) => {
  const t = G(), n = oe(), a = ue(la), c = e.useRef(null);
  e.useEffect(() => {
    const E = c.current;
    if (!E) return;
    const k = ($) => {
      const Q = $.detail;
      let ee = Q.target;
      for (; ee && ee !== E; ) {
        const ae = ee.dataset.row, re = ee.dataset.col;
        if (ae != null && re != null) {
          Q.resolved = { key: ae + "|" + re };
          return;
        }
        ee = ee.parentElement;
      }
    };
    return E.addEventListener("tl-tooltip-resolve", k), () => E.removeEventListener("tl-tooltip-resolve", k);
  }, []);
  const s = t.columns ?? [], i = t.totalRowCount ?? 0, u = t.rows ?? [], r = t.rowHeight ?? 36, o = t.selectionMode ?? "single", m = t.selectedCount ?? 0, p = t.cursorIndex ?? -1, f = t.frozenColumnCount ?? 0, g = t.treeMode ?? !1, _ = t.columnSelect ?? !1, S = e.useMemo(
    () => s.filter((E) => E.sortPriority && E.sortPriority > 0).length,
    [s]
  ), C = o === "multi", v = 40, y = 20, I = e.useRef(null), L = e.useRef(null), b = e.useRef(null), w = e.useRef(null), h = e.useRef(null), [D, R] = e.useState({}), N = e.useRef(null), z = e.useRef(!1), A = e.useRef(null), [x, O] = e.useState(null), [Z, H] = e.useState(null), [F, B] = e.useState(null), [M, q] = e.useState(0);
  e.useEffect(() => {
    const E = b.current;
    if (!E)
      return;
    const k = () => {
      const Q = E.offsetWidth - E.clientWidth;
      q((ee) => ee === Q ? ee : Q);
    };
    k();
    const $ = new ResizeObserver(k);
    return $.observe(E), () => $.disconnect();
  }, []), e.useEffect(() => {
    N.current || R({});
  }, [s]);
  const d = e.useCallback((E) => D[E.name] ?? E.width, [D]), T = e.useMemo(() => {
    const E = [];
    let k = C && f > 0 ? v : 0;
    for (let $ = 0; $ < f && $ < s.length; $++)
      E.push(k), k += d(s[$]);
    return E;
  }, [s, f, C, v, d]), V = e.useMemo(() => {
    if (f <= 0)
      return 0;
    let E = C ? v : 0;
    for (let k = 0; k < f && k < s.length; k++)
      E += d(s[k]);
    return E;
  }, [s, f, C, v, d]), W = i * r, X = e.useRef(null), j = e.useCallback((E, k, $) => {
    $.preventDefault(), $.stopPropagation(), N.current = { column: E, startX: $.clientX, startWidth: k };
    let Q = $.clientX, ee = 0;
    const ae = () => {
      const se = N.current;
      if (!se) return;
      const de = Math.max(xt, se.startWidth + (Q - se.startX) + ee);
      R((Ee) => ({ ...Ee, [se.column]: de }));
    }, re = () => {
      const se = b.current, de = I.current;
      if (!se || !N.current) return;
      const Ee = se.getBoundingClientRect(), Ne = 40, Et = 8, an = se.scrollLeft;
      Q > Ee.right - Ne ? se.scrollLeft += Et : Q < Ee.left + Ne && (se.scrollLeft = Math.max(0, se.scrollLeft - Et));
      const Ct = se.scrollLeft - an;
      Ct !== 0 && (de && (de.scrollLeft = se.scrollLeft), ee += Ct, ae()), X.current = requestAnimationFrame(re);
    };
    X.current = requestAnimationFrame(re);
    const fe = (se) => {
      Q = se.clientX, ae();
    }, pe = (se) => {
      document.removeEventListener("mousemove", fe), document.removeEventListener("mouseup", pe), X.current !== null && (cancelAnimationFrame(X.current), X.current = null);
      const de = N.current;
      if (de) {
        const Ee = Math.max(xt, de.startWidth + (se.clientX - de.startX) + ee);
        n("columnResize", { column: de.column, width: Ee }), N.current = null, z.current = !0, requestAnimationFrame(() => {
          z.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", fe), document.addEventListener("mouseup", pe);
  }, [n]), te = e.useCallback(() => {
    I.current && b.current && (I.current.scrollLeft = b.current.scrollLeft), w.current !== null && clearTimeout(w.current), w.current = window.setTimeout(() => {
      const E = b.current;
      if (!E) return;
      const k = E.scrollTop, $ = Math.ceil(E.clientHeight / r), Q = Math.floor(k / r);
      n("scroll", { start: Q, count: $ });
    }, 80);
  }, [n, r]), ce = e.useCallback((E, k, $) => {
    if (z.current) return;
    let Q;
    !k || k === "desc" ? Q = "asc" : Q = "desc";
    const ee = $.shiftKey ? "add" : "replace";
    n("sort", { column: E, direction: Q, mode: ee });
  }, [n]), ne = e.useCallback((E, k) => {
    A.current = E, k.dataTransfer.effectAllowed = "move", k.dataTransfer.setData("text/plain", E);
  }, []), _e = e.useCallback((E, k) => {
    if (!A.current || A.current === E) {
      O(null);
      return;
    }
    k.preventDefault(), k.dataTransfer.dropEffect = "move";
    const $ = k.currentTarget.getBoundingClientRect(), Q = k.clientX < $.left + $.width / 2 ? "left" : "right";
    O({ column: E, side: Q });
  }, []), ve = e.useCallback((E) => {
    E.preventDefault(), E.stopPropagation();
    const k = A.current;
    if (!k || !x) {
      A.current = null, O(null);
      return;
    }
    let $ = s.findIndex((ee) => ee.name === x.column);
    if ($ < 0) {
      A.current = null, O(null);
      return;
    }
    const Q = s.findIndex((ee) => ee.name === k);
    x.side === "right" && $++, Q < $ && $--, n("columnReorder", { column: k, targetIndex: $ }), A.current = null, O(null);
  }, [s, x, n]), Ce = e.useCallback(() => {
    A.current = null, O(null);
  }, []), Se = e.useCallback((E, k) => {
    var ee, ae, re, fe;
    const $ = window.getSelection();
    if ($ && !$.isCollapsed && k.currentTarget.contains($.anchorNode))
      return;
    if (!lt(k) && ((ee = b.current) == null || ee.focus({ preventScroll: !0 }), !k.ctrlKey && !k.metaKey && !k.shiftKey)) {
      const pe = (fe = (re = (ae = k.target) == null ? void 0 : ae.closest) == null ? void 0 : re.call(ae, "[data-col]")) == null ? void 0 : fe.getAttribute("data-col");
      h.current = { index: E, col: pe ?? void 0 };
    }
    const Q = u.find((pe) => pe.index === E);
    lt(k) && (Q != null && Q.selected) && !k.ctrlKey && !k.metaKey && !k.shiftKey || n("select", {
      rowIndex: E,
      ctrlKey: k.ctrlKey || k.metaKey,
      shiftKey: k.shiftKey
    });
  }, [n, u]), ke = e.useCallback((E, k, $) => {
    n("moveSelection", { direction: E, extend: k, move: $ });
  }, [n]), xe = e.useCallback(() => {
    p < 0 || n("select", { rowIndex: p, ctrlKey: C, shiftKey: !1 });
  }, [n, p, C]), He = e.useCallback(() => {
    n("selectAll", { selected: !0 });
  }, [n]), P = e.useCallback(
    () => !!c.current && c.current.contains(document.activeElement),
    []
  );
  e.useEffect(() => {
    if (p < 0)
      return;
    const E = b.current;
    if (!E)
      return;
    const k = p * r, $ = k + r;
    k < E.scrollTop ? E.scrollTop = k : $ > E.scrollTop + E.clientHeight && (E.scrollTop = $ - E.clientHeight);
  }, [p, r]), e.useEffect(() => {
    const E = h.current, k = b.current;
    if (!E || !k)
      return;
    const $ = u.find((ae) => ae.index === E.index);
    if (!$ || !at(k, $.id))
      return;
    h.current = null;
    const Q = document.activeElement;
    if (Q && Q !== document.body && !k.contains(Q))
      return;
    const ee = at(k, $.id, { col: E.col, last: E.last });
    ee && (ee.focus({ preventScroll: !0 }), ee instanceof HTMLInputElement && ee.select());
  }, [u]);
  const Y = e.useCallback((E) => {
    if (E.key !== "Tab")
      return;
    const k = b.current, $ = document.activeElement;
    if (!k || !$ || !k.contains($))
      return;
    const Q = $.closest("[data-row][data-col]");
    if (!Q)
      return;
    const ee = Q.dataset.row, ae = u.find((Ne) => Ne.id === ee);
    if (!ae)
      return;
    const re = Wt(k, ee).flatMap((Ne) => Array.from(Ne.querySelectorAll(ra))), fe = re.indexOf($);
    if (fe < 0)
      return;
    const pe = !E.shiftKey;
    if (!(pe ? fe === re.length - 1 : fe === 0))
      return;
    const de = pe ? ae.index + 1 : ae.index - 1;
    if (de < 0 || de >= i)
      return;
    const Ee = u.find((Ne) => Ne.index === de);
    Ee && at(k, Ee.id) || (E.preventDefault(), h.current = { index: de, last: !pe }, n("select", { rowIndex: de, ctrlKey: !1, shiftKey: !1 }));
  }, [u, i, n]), le = e.useCallback((E, k) => {
    k.stopPropagation(), n("select", { rowIndex: E, ctrlKey: !0, shiftKey: !1 });
  }, [n]), ie = e.useCallback(() => {
    const E = m === i && i > 0;
    n("selectAll", { selected: !E });
  }, [n, m, i]), Fe = e.useCallback((E, k, $) => {
    $.stopPropagation(), n("expand", { rowIndex: E, expanded: k });
  }, [n]), qt = e.useCallback((E, k) => {
    k.preventDefault(), H({ x: k.clientX, y: k.clientY, colIdx: E });
  }, []), Zt = e.useCallback(() => {
    Z && (n("setFrozenColumnCount", { count: Z.colIdx + 1 }), H(null));
  }, [Z, n]), Qt = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), H(null);
  }, [n]), Jt = e.useCallback((E) => {
    E.preventDefault(), E.stopPropagation();
    const k = L.current, $ = I.current;
    if (!k || !$)
      return;
    const Q = k.clientWidth, ee = [{ x: 0, count: 0 }];
    $.querySelectorAll("[data-col-idx]").forEach((pe) => {
      const se = pe.getBoundingClientRect().right - k.getBoundingClientRect().left;
      se > 0 && se <= Q && ee.push({ x: se, count: Number(pe.dataset.colIdx) + 1 });
    });
    let ae = { x: V, count: f };
    const re = (pe) => {
      const se = pe.clientX - k.getBoundingClientRect().left;
      ae = ee.reduce(
        (de, Ee) => Math.abs(Ee.x - se) < Math.abs(de.x - se) ? Ee : de,
        ee[0]
      ), B(ae);
    }, fe = () => {
      document.removeEventListener("mousemove", re), document.removeEventListener("mouseup", fe), B(null), ae.count !== f && n("setFrozenColumnCount", { count: ae.count });
    };
    document.addEventListener("mousemove", re), document.addEventListener("mouseup", fe);
  }, [V, f, n]);
  e.useEffect(() => {
    if (!Z) return;
    const E = () => H(null);
    return document.addEventListener("mousedown", E), () => document.removeEventListener("mousedown", E);
  }, [Z]), Le(!!Z, { ESCAPE: () => H(null) });
  const en = e.useCallback((E, k) => {
    k.stopPropagation(), k.preventDefault(), n("openFilter", { column: E });
  }, [n]), tn = e.useCallback((E) => {
    E.stopPropagation(), E.preventDefault(), n("openColumnSelect", {});
  }, [n]), Qe = s.reduce((E, k) => E + d(k), 0) + (C ? v : 0), Je = _ ? 32 : 0, nn = m === i && i > 0, vt = m > 0 && m < i, ln = e.useCallback((E) => {
    E && (E.indeterminate = vt);
  }, [vt]);
  return /* @__PURE__ */ e.createElement(bt, { active: P }, /* @__PURE__ */ e.createElement(
    na,
    {
      isMulti: C,
      cursorIndex: p,
      onMove: ke,
      onToggle: xe,
      onSelectAll: He
    }
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: c,
      id: l,
      className: "tlTableView",
      "data-tooltip": "dynamic",
      onDragOver: (E) => {
        if (!A.current) return;
        E.preventDefault();
        const k = b.current, $ = I.current;
        if (!k) return;
        const Q = k.getBoundingClientRect(), ee = 40, ae = 8;
        E.clientX < Q.left + ee ? k.scrollLeft = Math.max(0, k.scrollLeft - ae) : E.clientX > Q.right - ee && (k.scrollLeft += ae), $ && ($.scrollLeft = k.scrollLeft);
      },
      onDrop: ve
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerArea", ref: L }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: I }, /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerRow",
        style: { width: Qe, paddingRight: Je + M }
      },
      C && /* @__PURE__ */ e.createElement(
        "div",
        {
          className: "tlTableView__headerCell tlTableView__checkboxCell" + (f > 0 ? " tlTableView__headerCell--frozen" : ""),
          style: {
            width: v,
            minWidth: v,
            ...f > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
          },
          onDragOver: (E) => {
            A.current && (E.preventDefault(), E.dataTransfer.dropEffect = "move", s.length > 0 && s[0].name !== A.current && O({ column: s[0].name, side: "left" }));
          }
        },
        /* @__PURE__ */ e.createElement(
          "input",
          {
            type: "checkbox",
            ref: ln,
            className: "tlTableView__checkbox",
            checked: nn,
            onChange: ie
          }
        )
      ),
      s.map((E, k) => {
        const $ = d(E);
        s.length - 1;
        let Q = "tlTableView__headerCell";
        E.sortable && (Q += " tlTableView__headerCell--sortable"), x && x.column === E.name && (Q += " tlTableView__headerCell--dragOver-" + x.side);
        const ee = k < f, ae = k === f - 1;
        return ee && (Q += " tlTableView__headerCell--frozen"), ae && (Q += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
          "div",
          {
            key: E.name,
            className: Q,
            "data-col-idx": k,
            style: {
              width: $,
              minWidth: $,
              position: ee ? "sticky" : "relative",
              ...ee ? { left: T[k], zIndex: 2 } : {}
            },
            draggable: !0,
            onClick: E.sortable ? (re) => ce(E.name, E.sortDirection, re) : void 0,
            onContextMenu: (re) => qt(k, re),
            onDragStart: (re) => ne(E.name, re),
            onDragOver: (re) => _e(E.name, re),
            onDrop: ve,
            onDragEnd: Ce
          },
          /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, E.label),
          E.filterable && /* @__PURE__ */ e.createElement(
            "button",
            {
              type: "button",
              className: "tlTableView__filterButton" + (E.filterActive ? " tlTableView__filterButton--active" : ""),
              title: a["js.table.filter"],
              style: {
                border: "none",
                background: "transparent",
                cursor: "pointer",
                padding: "0 4px",
                color: E.filterActive ? "#1565c0" : "inherit"
              },
              onMouseDown: (re) => re.stopPropagation(),
              onClick: (re) => en(E.name, re)
            },
            /* @__PURE__ */ e.createElement("i", { className: E.filterActive ? "bi bi-funnel-fill" : "bi bi-funnel" })
          ),
          E.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, E.sortDirection === "asc" ? "▲" : "▼", S > 1 && E.sortPriority != null && E.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, E.sortPriority)),
          /* @__PURE__ */ e.createElement(
            "div",
            {
              className: "tlTableView__resizeHandle",
              onMouseDown: (re) => j(E.name, $, re)
            }
          )
        );
      }),
      /* @__PURE__ */ e.createElement(
        "div",
        {
          style: { flex: "0 0 0", minHeight: "100%" },
          onDragOver: (E) => {
            if (A.current && s.length > 0) {
              const k = s[s.length - 1];
              k.name !== A.current && (E.preventDefault(), E.dataTransfer.dropEffect = "move", O({ column: k.name, side: "right" }));
            }
          },
          onDrop: ve
        }
      )
    )), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__frozenSplitter" + (F ? " tlTableView__frozenSplitter--active" : ""),
        style: { left: V },
        title: a["js.table.freezeSplitter"],
        onMouseDown: Jt
      }
    ), _ && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__columnsButton",
        title: a["js.table.columns"],
        "aria-label": a["js.table.columns"],
        onClick: tn
      },
      /* @__PURE__ */ e.createElement("i", { className: "bi bi-gear" })
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: b,
        className: "tlTableView__body",
        onScroll: te,
        onKeyDown: Y,
        tabIndex: 0
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: W, position: "relative", width: Qe, paddingRight: Je } }, u.map((E) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: E.id,
          className: "tlTableView__row" + (E.selected ? " tlTableView__row--selected" : "") + (E.index === p ? " tlTableView__row--cursor" : ""),
          style: {
            position: "absolute",
            top: E.index * r,
            height: r,
            width: Qe,
            paddingRight: Je,
            ...E.index === p ? { outline: "2px solid var(--color-primary, #1a73e8)", outlineOffset: "-2px" } : {}
          },
          onMouseDown: (k) => {
            (k.shiftKey || k.ctrlKey || k.metaKey || k.detail > 1) && !lt(k) && k.preventDefault();
          },
          onClick: (k) => Se(E.index, k)
        },
        C && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (f > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: v,
              minWidth: v,
              ...f > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (k) => k.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: E.selected,
              onChange: () => {
              },
              onClick: (k) => le(E.index, k),
              tabIndex: -1
            }
          )
        ),
        s.map((k, $) => {
          const Q = d(k), ee = $ === s.length - 1, ae = $ < f, re = $ === f - 1;
          let fe = "tlTableView__cell";
          ae && (fe += " tlTableView__cell--frozen"), re && (fe += " tlTableView__cell--frozenLast");
          const pe = g && $ === 0, se = E.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: k.name,
              className: fe,
              "data-row": E.id,
              "data-col": k.name,
              style: {
                ...ee && !ae ? { flex: "1 0 auto", minWidth: Q } : { width: Q, minWidth: Q },
                ...ae ? { position: "sticky", left: T[$], zIndex: 2 } : {}
              }
            },
            pe ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: se * y } }, E.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (de) => Fe(E.index, !E.expanded, de)
              },
              E.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), E.cells[k.name] && /* @__PURE__ */ e.createElement(K, { control: E.cells[k.name] })) : E.cells[k.name] && /* @__PURE__ */ e.createElement(K, { control: E.cells[k.name] })
          );
        })
      )))
    ),
    F && /* @__PURE__ */ e.createElement("div", { className: "tlTableView__frozenPreview", style: { left: F.x } }),
    Z && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: Z.y, left: Z.x, zIndex: 1e4 },
        onMouseDown: (E) => E.stopPropagation()
      },
      Z.colIdx + 1 !== f && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Zt }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      f > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Qt }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
    )
  ));
}, sa = {
  "js.table.columnSearch": "Find column"
}, ca = ({ controlId: l }) => {
  const t = G(), n = oe(), a = ue(sa), c = t.entries ?? [], s = c.filter((b) => b.visible).length, [i, u] = e.useState(""), r = i.trim().toLowerCase(), o = r ? c.filter((b) => b.label.toLowerCase().includes(r)) : c, m = e.useRef(null), p = e.useRef(null), [f, g] = e.useState(null), _ = e.useCallback((b) => {
    p.current = b, g(b);
  }, []), S = e.useCallback((b, w) => {
    n("columnVisible", { column: b, visible: w });
  }, [n]), C = e.useCallback((b, w) => {
    m.current = b, w.dataTransfer.effectAllowed = "move", w.dataTransfer.setData("text/plain", b);
  }, []), v = e.useCallback((b, w) => {
    if (!m.current || m.current === b) {
      _(null);
      return;
    }
    w.preventDefault(), w.dataTransfer.dropEffect = "move";
    const h = w.currentTarget.getBoundingClientRect(), D = w.clientY < h.top + h.height / 2 ? "top" : "bottom";
    _({ name: b, side: D });
  }, [_]), y = e.useCallback(() => {
    m.current = null, _(null);
  }, [_]), I = e.useCallback((b) => {
    b.preventDefault();
    const w = m.current, h = p.current;
    if (m.current = null, _(null), !w || !h)
      return;
    const D = c.findIndex((z) => z.name === h.name), R = c.findIndex((z) => z.name === w);
    if (D < 0 || R < 0)
      return;
    let N = h.side === "top" ? D : D + 1;
    R < N && N--, N !== R && n("columnReorder", { column: w, targetIndex: N });
  }, [c, n, _]), L = c.length > 10;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlColumnSelect", onDrop: I }, L && /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__search" }, /* @__PURE__ */ e.createElement("i", { className: "bi bi-search", "aria-hidden": "true" }), /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "search",
      className: "tlColumnSelect__searchInput",
      placeholder: a["js.table.columnSearch"],
      "aria-label": a["js.table.columnSearch"],
      value: i,
      onChange: (b) => u(b.target.value)
    }
  )), /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__list" + (L ? " tlColumnSelect__list--fixed" : "") }, o.map((b) => {
    const w = b.visible && s <= 1;
    let h = "tlColumnSelect__row";
    return f && f.name === b.name && (h += " tlColumnSelect__row--dragOver-" + f.side), /* @__PURE__ */ e.createElement(
      "div",
      {
        key: b.name,
        className: h,
        draggable: !0,
        onDragStart: (D) => C(b.name, D),
        onDragOver: (D) => v(b.name, D),
        onDrop: I,
        onDragEnd: y
      },
      /* @__PURE__ */ e.createElement("i", { className: "tlColumnSelect__handle bi bi-grip-vertical", "aria-hidden": "true" }),
      /* @__PURE__ */ e.createElement("label", { className: "tlColumnSelect__label" }, /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          className: "tlReactCheckbox",
          checked: b.visible,
          disabled: w,
          onChange: (D) => S(b.name, D.target.checked)
        }
      ), /* @__PURE__ */ e.createElement("span", null, b.label))
    );
  })));
}, ia = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, zt = e.createContext(ia), { useMemo: ua, useRef: da, useState: ma, useEffect: pa } = e, fa = 320, ha = "TLTableView", ba = "TLPanel", _a = ({ controlId: l }) => {
  var C;
  const t = G(), n = t.maxColumns ?? 3, a = t.labelPosition ?? "auto", c = t.readOnly === !0, s = t.children ?? [], i = t.noModelMessage, u = da(null), [r, o] = ma(
    a === "top" ? "top" : "side"
  );
  pa(() => {
    if (a !== "auto") {
      o(a);
      return;
    }
    const v = u.current;
    if (!v) return;
    const y = new ResizeObserver((I) => {
      for (const L of I) {
        const w = L.contentRect.width / n;
        o(w < fa ? "top" : "side");
      }
    });
    return y.observe(v), () => y.disconnect();
  }, [a, n]);
  const m = ua(() => ({
    readOnly: c,
    resolvedLabelPosition: r
  }), [c, r]), f = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, g = s.length === 1 ? s[0] : void 0, _ = !!g && (g.module === ha || g.module === ba && ((C = g.state) == null ? void 0 : C.bare) === !0), S = [
    "tlFormLayout",
    c ? "tlFormLayout--readonly" : "",
    _ ? "tlFormLayout--flush" : ""
  ].filter(Boolean).join(" ");
  return i ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, i)) : /* @__PURE__ */ e.createElement(zt.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: l, className: S, style: f, ref: u }, s.map((v, y) => /* @__PURE__ */ e.createElement(K, { key: y, control: v }))));
}, { useCallback: ga } = e, va = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, Ea = ({ controlId: l }) => {
  const t = G(), n = oe(), a = ue(va), c = t.headerControl ?? null, s = t.headerActions ?? [], i = t.collapsible === !0, u = t.collapsed === !0, r = t.border ?? "none", o = t.fullLine === !0, m = t.children ?? [], p = c != null || s.length > 0 || i, f = ga(() => {
    n("toggleCollapse");
  }, [n]), g = [
    "tlFormGroup",
    `tlFormGroup--border-${r}`,
    o ? "tlFormGroup--fullLine" : "",
    u ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: g }, p && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, i && /* @__PURE__ */ e.createElement(
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
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(K, { control: c })), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, s.map((_, S) => /* @__PURE__ */ e.createElement(K, { key: S, control: _ })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((_, S) => /* @__PURE__ */ e.createElement(K, { key: S, control: _ }))));
}, { useContext: Ca, useState: wa, useCallback: ya } = e, Sa = ({ controlId: l }) => {
  const t = G(), n = Ca(zt), a = t.label ?? "", c = t.required === !0, s = t.error, i = t.errorIcon, u = t.warnings, r = t.warningIcon, o = t.helpText, m = t.dirty === !0, p = t.labelPosition ?? n.resolvedLabelPosition, f = t.fullLine === !0, g = t.visible !== !1, _ = t.hasTooltip === !0, S = t.field, C = n.readOnly, [v, y] = wa(!1), I = ya(() => y((D) => !D), []), L = p === "hidden", b = s != null, w = u != null && u.length > 0, h = [
    "tlFormField",
    `tlFormField--${p}`,
    C ? "tlFormField--readonly" : "",
    f ? "tlFormField--fullLine" : "",
    b ? "tlFormField--error" : "",
    !b && w ? "tlFormField--warning" : "",
    m ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: h, style: g ? void 0 : { display: "none" } }, !L && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": _ ? "key:tooltip" : void 0
    },
    a
  ), c && !C && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), m && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), o && !C && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormField__helpIcon",
      onClick: I,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(K, { control: S })), !C && b && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(qe, { image: i, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, s)), !C && !b && w && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, u.map((D, R) => /* @__PURE__ */ e.createElement("div", { key: R, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(qe, { image: r, className: "tlFormField__warningIcon" }), /* @__PURE__ */ e.createElement("span", null, D)))), !C && o && v && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, o));
}, ka = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.iconCss, c = t.iconSrc, s = t.label, i = t.cssClass, u = t.hasTooltip === !0, r = t.hasLink, o = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : c ? /* @__PURE__ */ e.createElement("img", { src: c, className: "tlTypeIcon", alt: "" }) : null, m = /* @__PURE__ */ e.createElement(e.Fragment, null, o, s && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, s)), p = e.useCallback((_) => {
    _.preventDefault(), n("goto", {});
  }, [n]), f = ["tlResourceCell", i].filter(Boolean).join(" "), g = u ? "key:tooltip" : void 0;
  return r ? /* @__PURE__ */ e.createElement(
    "a",
    {
      id: l,
      className: f,
      href: "#",
      onClick: p,
      "data-tooltip": g
    },
    m
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: f, "data-tooltip": g }, m);
}, Na = 20, Ta = () => {
  var w;
  const l = G(), t = oe(), n = l.nodes ?? [], a = l.selectionMode ?? "single", c = l.dragEnabled ?? !1, s = l.dropEnabled ?? !1, i = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [r, o] = e.useState(-1), m = e.useRef(null), p = ((w = n.find((h) => h.selected)) == null ? void 0 : w.id) ?? null;
  e.useEffect(() => {
    var D;
    if (p == null)
      return;
    const h = (D = m.current) == null ? void 0 : D.querySelector(".tlTreeView__node--selected");
    h && h.scrollIntoView({ block: "nearest" });
  }, [p]);
  const f = e.useCallback((h, D) => {
    t(D ? "collapse" : "expand", { nodeId: h });
  }, [t]), g = e.useCallback((h, D) => {
    var N;
    const R = window.getSelection();
    R && !R.isCollapsed && D.currentTarget.contains(R.anchorNode) || ((N = m.current) == null || N.focus({ preventScroll: !0 }), t("select", {
      nodeId: h,
      ctrlKey: D.ctrlKey || D.metaKey,
      shiftKey: D.shiftKey
    }));
  }, [t]), _ = e.useCallback((h, D) => {
    D.preventDefault(), t("contextMenu", { nodeId: h, x: D.clientX, y: D.clientY });
  }, [t]), S = e.useRef(null), C = e.useCallback((h, D) => {
    const R = D.getBoundingClientRect(), N = h.clientY - R.top, z = R.height / 3;
    return N < z ? "above" : N > z * 2 ? "below" : "within";
  }, []), v = e.useCallback((h, D) => {
    D.dataTransfer.effectAllowed = "move", D.dataTransfer.setData("text/plain", h);
  }, []), y = e.useCallback((h, D) => {
    D.preventDefault(), D.dataTransfer.dropEffect = "move";
    const R = C(D, D.currentTarget);
    S.current != null && window.clearTimeout(S.current), S.current = window.setTimeout(() => {
      t("dragOver", { nodeId: h, position: R }), S.current = null;
    }, 50);
  }, [t, C]), I = e.useCallback((h, D) => {
    D.preventDefault(), S.current != null && (window.clearTimeout(S.current), S.current = null);
    const R = C(D, D.currentTarget);
    t("drop", { nodeId: h, position: R });
  }, [t, C]), L = e.useCallback(() => {
    S.current != null && (window.clearTimeout(S.current), S.current = null), t("dragEnd");
  }, [t]), b = e.useCallback((h) => {
    if (n.length === 0) return;
    let D = r;
    switch (h.key) {
      case "ArrowDown":
        h.preventDefault(), D = Math.min(r + 1, n.length - 1);
        break;
      case "ArrowUp":
        h.preventDefault(), D = Math.max(r - 1, 0);
        break;
      case "ArrowRight":
        if (h.preventDefault(), r >= 0 && r < n.length) {
          const R = n[r];
          if (R.expandable && !R.expanded) {
            t("expand", { nodeId: R.id });
            return;
          } else R.expanded && (D = r + 1);
        }
        break;
      case "ArrowLeft":
        if (h.preventDefault(), r >= 0 && r < n.length) {
          const R = n[r];
          if (R.expanded) {
            t("collapse", { nodeId: R.id });
            return;
          } else {
            const N = R.depth;
            for (let z = r - 1; z >= 0; z--)
              if (n[z].depth < N) {
                D = z;
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
        h.preventDefault(), D = 0;
        break;
      case "End":
        h.preventDefault(), D = n.length - 1;
        break;
      default:
        return;
    }
    D !== r && o(D);
  }, [r, n, t, a]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: m,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: b
    },
    n.map((h, D) => /* @__PURE__ */ e.createElement(
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
          D === r ? "tlTreeView__node--focused" : "",
          i === h.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          i === h.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          i === h.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: h.depth * Na },
        draggable: c,
        onMouseDown: (R) => {
          (R.shiftKey || R.ctrlKey || R.metaKey || R.detail > 1) && R.preventDefault();
        },
        onClick: (R) => g(h.id, R),
        onContextMenu: (R) => _(h.id, R),
        onDragStart: (R) => v(h.id, R),
        onDragOver: s ? (R) => y(h.id, R) : void 0,
        onDrop: s ? (R) => I(h.id, R) : void 0,
        onDragEnd: L
      },
      h.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (R) => {
            R.stopPropagation(), f(h.id, h.expanded);
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
var rt = { exports: {} }, he = {}, ot = { exports: {} }, J = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var It;
function Ra() {
  if (It) return J;
  It = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), a = Symbol.for("react.strict_mode"), c = Symbol.for("react.profiler"), s = Symbol.for("react.consumer"), i = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), r = Symbol.for("react.suspense"), o = Symbol.for("react.memo"), m = Symbol.for("react.lazy"), p = Symbol.for("react.activity"), f = Symbol.iterator;
  function g(d) {
    return d === null || typeof d != "object" ? null : (d = f && d[f] || d["@@iterator"], typeof d == "function" ? d : null);
  }
  var _ = {
    isMounted: function() {
      return !1;
    },
    enqueueForceUpdate: function() {
    },
    enqueueReplaceState: function() {
    },
    enqueueSetState: function() {
    }
  }, S = Object.assign, C = {};
  function v(d, T, V) {
    this.props = d, this.context = T, this.refs = C, this.updater = V || _;
  }
  v.prototype.isReactComponent = {}, v.prototype.setState = function(d, T) {
    if (typeof d != "object" && typeof d != "function" && d != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, d, T, "setState");
  }, v.prototype.forceUpdate = function(d) {
    this.updater.enqueueForceUpdate(this, d, "forceUpdate");
  };
  function y() {
  }
  y.prototype = v.prototype;
  function I(d, T, V) {
    this.props = d, this.context = T, this.refs = C, this.updater = V || _;
  }
  var L = I.prototype = new y();
  L.constructor = I, S(L, v.prototype), L.isPureReactComponent = !0;
  var b = Array.isArray;
  function w() {
  }
  var h = { H: null, A: null, T: null, S: null }, D = Object.prototype.hasOwnProperty;
  function R(d, T, V) {
    var W = V.ref;
    return {
      $$typeof: l,
      type: d,
      key: T,
      ref: W !== void 0 ? W : null,
      props: V
    };
  }
  function N(d, T) {
    return R(d.type, T, d.props);
  }
  function z(d) {
    return typeof d == "object" && d !== null && d.$$typeof === l;
  }
  function A(d) {
    var T = { "=": "=0", ":": "=2" };
    return "$" + d.replace(/[=:]/g, function(V) {
      return T[V];
    });
  }
  var x = /\/+/g;
  function O(d, T) {
    return typeof d == "object" && d !== null && d.key != null ? A("" + d.key) : T.toString(36);
  }
  function Z(d) {
    switch (d.status) {
      case "fulfilled":
        return d.value;
      case "rejected":
        throw d.reason;
      default:
        switch (typeof d.status == "string" ? d.then(w, w) : (d.status = "pending", d.then(
          function(T) {
            d.status === "pending" && (d.status = "fulfilled", d.value = T);
          },
          function(T) {
            d.status === "pending" && (d.status = "rejected", d.reason = T);
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
  function H(d, T, V, W, X) {
    var j = typeof d;
    (j === "undefined" || j === "boolean") && (d = null);
    var te = !1;
    if (d === null) te = !0;
    else
      switch (j) {
        case "bigint":
        case "string":
        case "number":
          te = !0;
          break;
        case "object":
          switch (d.$$typeof) {
            case l:
            case t:
              te = !0;
              break;
            case m:
              return te = d._init, H(
                te(d._payload),
                T,
                V,
                W,
                X
              );
          }
      }
    if (te)
      return X = X(d), te = W === "" ? "." + O(d, 0) : W, b(X) ? (V = "", te != null && (V = te.replace(x, "$&/") + "/"), H(X, T, V, "", function(_e) {
        return _e;
      })) : X != null && (z(X) && (X = N(
        X,
        V + (X.key == null || d && d.key === X.key ? "" : ("" + X.key).replace(
          x,
          "$&/"
        ) + "/") + te
      )), T.push(X)), 1;
    te = 0;
    var ce = W === "" ? "." : W + ":";
    if (b(d))
      for (var ne = 0; ne < d.length; ne++)
        W = d[ne], j = ce + O(W, ne), te += H(
          W,
          T,
          V,
          j,
          X
        );
    else if (ne = g(d), typeof ne == "function")
      for (d = ne.call(d), ne = 0; !(W = d.next()).done; )
        W = W.value, j = ce + O(W, ne++), te += H(
          W,
          T,
          V,
          j,
          X
        );
    else if (j === "object") {
      if (typeof d.then == "function")
        return H(
          Z(d),
          T,
          V,
          W,
          X
        );
      throw T = String(d), Error(
        "Objects are not valid as a React child (found: " + (T === "[object Object]" ? "object with keys {" + Object.keys(d).join(", ") + "}" : T) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return te;
  }
  function F(d, T, V) {
    if (d == null) return d;
    var W = [], X = 0;
    return H(d, W, "", "", function(j) {
      return T.call(V, j, X++);
    }), W;
  }
  function B(d) {
    if (d._status === -1) {
      var T = d._result;
      T = T(), T.then(
        function(V) {
          (d._status === 0 || d._status === -1) && (d._status = 1, d._result = V);
        },
        function(V) {
          (d._status === 0 || d._status === -1) && (d._status = 2, d._result = V);
        }
      ), d._status === -1 && (d._status = 0, d._result = T);
    }
    if (d._status === 1) return d._result.default;
    throw d._result;
  }
  var M = typeof reportError == "function" ? reportError : function(d) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var T = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof d == "object" && d !== null && typeof d.message == "string" ? String(d.message) : String(d),
        error: d
      });
      if (!window.dispatchEvent(T)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", d);
      return;
    }
    console.error(d);
  }, q = {
    map: F,
    forEach: function(d, T, V) {
      F(
        d,
        function() {
          T.apply(this, arguments);
        },
        V
      );
    },
    count: function(d) {
      var T = 0;
      return F(d, function() {
        T++;
      }), T;
    },
    toArray: function(d) {
      return F(d, function(T) {
        return T;
      }) || [];
    },
    only: function(d) {
      if (!z(d))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return d;
    }
  };
  return J.Activity = p, J.Children = q, J.Component = v, J.Fragment = n, J.Profiler = c, J.PureComponent = I, J.StrictMode = a, J.Suspense = r, J.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = h, J.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(d) {
      return h.H.useMemoCache(d);
    }
  }, J.cache = function(d) {
    return function() {
      return d.apply(null, arguments);
    };
  }, J.cacheSignal = function() {
    return null;
  }, J.cloneElement = function(d, T, V) {
    if (d == null)
      throw Error(
        "The argument must be a React element, but you passed " + d + "."
      );
    var W = S({}, d.props), X = d.key;
    if (T != null)
      for (j in T.key !== void 0 && (X = "" + T.key), T)
        !D.call(T, j) || j === "key" || j === "__self" || j === "__source" || j === "ref" && T.ref === void 0 || (W[j] = T[j]);
    var j = arguments.length - 2;
    if (j === 1) W.children = V;
    else if (1 < j) {
      for (var te = Array(j), ce = 0; ce < j; ce++)
        te[ce] = arguments[ce + 2];
      W.children = te;
    }
    return R(d.type, X, W);
  }, J.createContext = function(d) {
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
  }, J.createElement = function(d, T, V) {
    var W, X = {}, j = null;
    if (T != null)
      for (W in T.key !== void 0 && (j = "" + T.key), T)
        D.call(T, W) && W !== "key" && W !== "__self" && W !== "__source" && (X[W] = T[W]);
    var te = arguments.length - 2;
    if (te === 1) X.children = V;
    else if (1 < te) {
      for (var ce = Array(te), ne = 0; ne < te; ne++)
        ce[ne] = arguments[ne + 2];
      X.children = ce;
    }
    if (d && d.defaultProps)
      for (W in te = d.defaultProps, te)
        X[W] === void 0 && (X[W] = te[W]);
    return R(d, j, X);
  }, J.createRef = function() {
    return { current: null };
  }, J.forwardRef = function(d) {
    return { $$typeof: u, render: d };
  }, J.isValidElement = z, J.lazy = function(d) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: d },
      _init: B
    };
  }, J.memo = function(d, T) {
    return {
      $$typeof: o,
      type: d,
      compare: T === void 0 ? null : T
    };
  }, J.startTransition = function(d) {
    var T = h.T, V = {};
    h.T = V;
    try {
      var W = d(), X = h.S;
      X !== null && X(V, W), typeof W == "object" && W !== null && typeof W.then == "function" && W.then(w, M);
    } catch (j) {
      M(j);
    } finally {
      T !== null && V.types !== null && (T.types = V.types), h.T = T;
    }
  }, J.unstable_useCacheRefresh = function() {
    return h.H.useCacheRefresh();
  }, J.use = function(d) {
    return h.H.use(d);
  }, J.useActionState = function(d, T, V) {
    return h.H.useActionState(d, T, V);
  }, J.useCallback = function(d, T) {
    return h.H.useCallback(d, T);
  }, J.useContext = function(d) {
    return h.H.useContext(d);
  }, J.useDebugValue = function() {
  }, J.useDeferredValue = function(d, T) {
    return h.H.useDeferredValue(d, T);
  }, J.useEffect = function(d, T) {
    return h.H.useEffect(d, T);
  }, J.useEffectEvent = function(d) {
    return h.H.useEffectEvent(d);
  }, J.useId = function() {
    return h.H.useId();
  }, J.useImperativeHandle = function(d, T, V) {
    return h.H.useImperativeHandle(d, T, V);
  }, J.useInsertionEffect = function(d, T) {
    return h.H.useInsertionEffect(d, T);
  }, J.useLayoutEffect = function(d, T) {
    return h.H.useLayoutEffect(d, T);
  }, J.useMemo = function(d, T) {
    return h.H.useMemo(d, T);
  }, J.useOptimistic = function(d, T) {
    return h.H.useOptimistic(d, T);
  }, J.useReducer = function(d, T, V) {
    return h.H.useReducer(d, T, V);
  }, J.useRef = function(d) {
    return h.H.useRef(d);
  }, J.useState = function(d) {
    return h.H.useState(d);
  }, J.useSyncExternalStore = function(d, T, V) {
    return h.H.useSyncExternalStore(
      d,
      T,
      V
    );
  }, J.useTransition = function() {
    return h.H.useTransition();
  }, J.version = "19.2.4", J;
}
var Pt;
function Da() {
  return Pt || (Pt = 1, ot.exports = Ra()), ot.exports;
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
var Mt;
function La() {
  if (Mt) return he;
  Mt = 1;
  var l = Da();
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
  return he.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = a, he.createPortal = function(r, o) {
    var m = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!o || o.nodeType !== 1 && o.nodeType !== 9 && o.nodeType !== 11)
      throw Error(t(299));
    return s(r, o, null, m);
  }, he.flushSync = function(r) {
    var o = i.T, m = a.p;
    try {
      if (i.T = null, a.p = 2, r) return r();
    } finally {
      i.T = o, a.p = m, a.d.f();
    }
  }, he.preconnect = function(r, o) {
    typeof r == "string" && (o ? (o = o.crossOrigin, o = typeof o == "string" ? o === "use-credentials" ? o : "" : void 0) : o = null, a.d.C(r, o));
  }, he.prefetchDNS = function(r) {
    typeof r == "string" && a.d.D(r);
  }, he.preinit = function(r, o) {
    if (typeof r == "string" && o && typeof o.as == "string") {
      var m = o.as, p = u(m, o.crossOrigin), f = typeof o.integrity == "string" ? o.integrity : void 0, g = typeof o.fetchPriority == "string" ? o.fetchPriority : void 0;
      m === "style" ? a.d.S(
        r,
        typeof o.precedence == "string" ? o.precedence : void 0,
        {
          crossOrigin: p,
          integrity: f,
          fetchPriority: g
        }
      ) : m === "script" && a.d.X(r, {
        crossOrigin: p,
        integrity: f,
        fetchPriority: g,
        nonce: typeof o.nonce == "string" ? o.nonce : void 0
      });
    }
  }, he.preinitModule = function(r, o) {
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
  }, he.preload = function(r, o) {
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
  }, he.preloadModule = function(r, o) {
    if (typeof r == "string")
      if (o) {
        var m = u(o.as, o.crossOrigin);
        a.d.m(r, {
          as: typeof o.as == "string" && o.as !== "script" ? o.as : void 0,
          crossOrigin: m,
          integrity: typeof o.integrity == "string" ? o.integrity : void 0
        });
      } else a.d.m(r);
  }, he.requestFormReset = function(r) {
    a.d.r(r);
  }, he.unstable_batchedUpdates = function(r, o) {
    return r(o);
  }, he.useFormState = function(r, o, m) {
    return i.H.useFormState(r, o, m);
  }, he.useFormStatus = function() {
    return i.H.useHostTransitionStatus();
  }, he.version = "19.2.4", he;
}
var jt;
function xa() {
  if (jt) return rt.exports;
  jt = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), rt.exports = La(), rt.exports;
}
var Vt = xa();
const { useState: Te, useCallback: be, useRef: $e, useEffect: Pe, useMemo: ft } = e;
function gt({ image: l }) {
  return l ? l.startsWith("/") ? /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" }) : /* @__PURE__ */ e.createElement(qe, { image: l, className: "tlDropdownSelect__optionIcon" }) : null;
}
function Ia({
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
  const m = be(
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
    /* @__PURE__ */ e.createElement(gt, { image: l.image }),
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
function Pa({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: a,
  onMouseEnter: c,
  id: s
}) {
  const i = be(() => a(l.value), [a, l.value]), u = ft(() => {
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
    /* @__PURE__ */ e.createElement(gt, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const Ma = ({ controlId: l, state: t }) => {
  const n = oe(), a = t.value ?? [], c = t.multiSelect === !0, s = t.customOrder === !0, i = t.mandatory === !0, u = t.disabled === !0, r = t.editable !== !1, o = t.optionsLoaded === !0, m = t.options ?? [], p = t.emptyOptionLabel ?? "", f = s && c && !u && r, g = ue({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), _ = g["js.dropdownSelect.nothingFound"], S = be(
    (P) => g["js.dropdownSelect.removeChip"].replace("{0}", P),
    [g]
  ), [C, v] = Te(!1), [y, I] = Te(""), [L, b] = Te(-1), [w, h] = Te(!1), [D, R] = Te({}), [N, z] = Te(null), [A, x] = Te(null), [O, Z] = Te(null), H = $e(null), F = $e(null), B = $e(null), M = $e(a);
  M.current = a;
  const q = $e(-1), d = ft(
    () => new Set(a.map((P) => P.value)),
    [a]
  ), T = ft(() => {
    let P = m.filter((Y) => !d.has(Y.value));
    if (y) {
      const Y = y.toLowerCase();
      P = P.filter((le) => le.label.toLowerCase().includes(Y));
    }
    return P;
  }, [m, d, y]);
  Pe(() => {
    y && T.length === 1 ? b(0) : b(-1);
  }, [T.length, y]), Pe(() => {
    C && o && F.current && F.current.focus();
  }, [C, o, a]), Pe(() => {
    var le, ie;
    if (q.current < 0) return;
    const P = q.current;
    q.current = -1;
    const Y = (le = H.current) == null ? void 0 : le.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    Y && Y.length > 0 ? Y[Math.min(P, Y.length - 1)].focus() : (ie = H.current) == null || ie.focus();
  }, [a]), Pe(() => {
    if (!C) return;
    const P = (Y) => {
      H.current && !H.current.contains(Y.target) && B.current && !B.current.contains(Y.target) && (v(!1), I(""));
    };
    return document.addEventListener("mousedown", P), () => document.removeEventListener("mousedown", P);
  }, [C]), Pe(() => {
    if (!C || !H.current) return;
    const P = H.current.getBoundingClientRect(), Y = window.innerHeight - P.bottom, ie = Y < 300 && P.top > Y;
    R({
      left: P.left,
      width: P.width,
      ...ie ? { bottom: window.innerHeight - P.top } : { top: P.bottom }
    });
  }, [C]);
  const V = be(async () => {
    if (!(u || !r) && (v(!0), I(""), b(-1), h(!1), !o))
      try {
        await n("loadOptions");
      } catch {
        h(!0);
      }
  }, [u, r, o, n]), W = be(() => {
    var P;
    v(!1), I(""), b(-1), (P = H.current) == null || P.focus();
  }, []), X = be(
    (P) => {
      let Y;
      if (c) {
        const le = m.find((ie) => ie.value === P);
        if (le)
          Y = [...M.current, le];
        else
          return;
      } else {
        const le = m.find((ie) => ie.value === P);
        if (le)
          Y = [le];
        else
          return;
      }
      M.current = Y, n(We, { value: Y.map((le) => le.value) }), c ? (I(""), b(-1)) : W();
    },
    [c, m, n, W]
  ), j = be(
    (P) => {
      q.current = M.current.findIndex((le) => le.value === P);
      const Y = M.current.filter((le) => le.value !== P);
      M.current = Y, n(We, { value: Y.map((le) => le.value) });
    },
    [n]
  ), te = be(
    (P) => {
      P.stopPropagation(), n(We, { value: [] }), W();
    },
    [n, W]
  ), ce = be((P) => {
    I(P.target.value);
  }, []), ne = be(
    (P) => {
      if (!C) {
        if (P.key === "ArrowDown" || P.key === "ArrowUp" || P.key === "Enter" || P.key === " ") {
          if (P.target.tagName === "BUTTON") return;
          P.preventDefault(), P.stopPropagation(), V();
        }
        return;
      }
      switch (P.key) {
        case "ArrowDown":
          P.preventDefault(), P.stopPropagation(), b(
            (Y) => Y < T.length - 1 ? Y + 1 : 0
          );
          break;
        case "ArrowUp":
          P.preventDefault(), P.stopPropagation(), b(
            (Y) => Y > 0 ? Y - 1 : T.length - 1
          );
          break;
        case "Enter":
          P.preventDefault(), P.stopPropagation(), L >= 0 && L < T.length && X(T[L].value);
          break;
        case "Escape":
          P.preventDefault(), P.stopPropagation(), W();
          break;
        case "Tab":
          W();
          break;
        case "Backspace":
          y === "" && c && a.length > 0 && j(a[a.length - 1].value);
          break;
      }
    },
    [
      C,
      V,
      W,
      T,
      L,
      X,
      y,
      c,
      a,
      j
    ]
  ), _e = be(
    async (P) => {
      P.preventDefault(), h(!1);
      try {
        await n("loadOptions");
      } catch {
        h(!0);
      }
    },
    [n]
  ), ve = be(
    (P, Y) => {
      z(P), Y.dataTransfer.effectAllowed = "move", Y.dataTransfer.setData("text/plain", String(P));
    },
    []
  ), Ce = be(
    (P, Y) => {
      if (Y.preventDefault(), Y.dataTransfer.dropEffect = "move", N === null || N === P) {
        x(null), Z(null);
        return;
      }
      const le = Y.currentTarget.getBoundingClientRect(), ie = le.left + le.width / 2, Fe = Y.clientX < ie ? "before" : "after";
      x(P), Z(Fe);
    },
    [N]
  ), Se = be(
    (P) => {
      if (P.preventDefault(), N === null || A === null || O === null || N === A) return;
      const Y = [...M.current], [le] = Y.splice(N, 1);
      let ie = A;
      N < A ? ie = O === "before" ? ie - 1 : ie : ie = O === "before" ? ie : ie + 1, Y.splice(ie, 0, le), M.current = Y, n(We, { value: Y.map((Fe) => Fe.value) }), z(null), x(null), Z(null);
    },
    [N, A, O, n]
  ), ke = be(() => {
    z(null), x(null), Z(null);
  }, []);
  if (Pe(() => {
    if (L < 0 || !B.current) return;
    const P = B.current.querySelector(
      `[id="${l}-opt-${L}"]`
    );
    P && P.scrollIntoView({ block: "nearest" });
  }, [L, l]), !r)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, a.map((P) => /* @__PURE__ */ e.createElement("span", { key: P.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(gt, { image: P.image }), /* @__PURE__ */ e.createElement("span", null, P.label))));
  const xe = !i && a.length > 0 && !u, He = C ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: B,
      className: "tlDropdownSelect__dropdown",
      style: D,
      ...on
    },
    (o || w) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: F,
        type: "text",
        className: "tlDropdownSelect__search",
        value: y,
        onChange: ce,
        onKeyDown: ne,
        placeholder: g["js.dropdownSelect.filterPlaceholder"],
        "aria-label": g["js.dropdownSelect.filterPlaceholder"],
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
      !o && !w && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      w && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: _e }, g["js.dropdownSelect.error"])),
      o && T.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, _),
      o && T.map((P, Y) => /* @__PURE__ */ e.createElement(
        Pa,
        {
          key: P.value,
          id: `${l}-opt-${Y}`,
          option: P,
          highlighted: Y === L,
          searchTerm: y,
          onSelect: X,
          onMouseEnter: () => b(Y)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: H,
      className: "tlDropdownSelect" + (C ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": C,
      "aria-haspopup": "listbox",
      "aria-owns": C ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: C ? void 0 : V,
      onKeyDown: ne
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, a.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : a.map((P, Y) => {
      let le = "";
      return N === Y ? le = "tlDropdownSelect__chip--dragging" : A === Y && O === "before" ? le = "tlDropdownSelect__chip--dropBefore" : A === Y && O === "after" && (le = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        Ia,
        {
          key: P.value,
          option: P,
          removable: !u && (c || !i),
          onRemove: j,
          removeLabel: S(P.label),
          draggable: f,
          onDragStart: f ? (ie) => ve(Y, ie) : void 0,
          onDragOver: f ? (ie) => Ce(Y, ie) : void 0,
          onDrop: f ? Se : void 0,
          onDragEnd: f ? ke : void 0,
          dragClassName: f ? le : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, xe && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: te,
        "aria-label": g["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, C ? "▲" : "▼"))
  ), He && Vt.createPortal(He, document.body));
}, { useCallback: st, useRef: ja } = e, Kt = "application/x-tl-color", Aa = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: a,
  onSwap: c,
  onReplace: s
}) => {
  const i = ja(null), u = st(
    (m) => (p) => {
      i.current = m, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), r = st((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), o = st(
    (m) => (p) => {
      p.preventDefault();
      const f = p.dataTransfer.getData(Kt);
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
function Yt(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function ht(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function Gt(l) {
  if (!ht(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function Xt(l, t, n) {
  const a = (c) => Yt(c).toString(16).padStart(2, "0");
  return "#" + a(l) + a(t) + a(n);
}
function Ba(l, t, n) {
  const a = l / 255, c = t / 255, s = n / 255, i = Math.max(a, c, s), u = Math.min(a, c, s), r = i - u;
  let o = 0;
  r !== 0 && (i === a ? o = (c - s) / r % 6 : i === c ? o = (s - a) / r + 2 : o = (a - c) / r + 4, o *= 60, o < 0 && (o += 360));
  const m = i === 0 ? 0 : r / i;
  return [o, m, i];
}
function Oa(l, t, n) {
  const a = n * t, c = a * (1 - Math.abs(l / 60 % 2 - 1)), s = n - a;
  let i = 0, u = 0, r = 0;
  return l < 60 ? (i = a, u = c, r = 0) : l < 120 ? (i = c, u = a, r = 0) : l < 180 ? (i = 0, u = a, r = c) : l < 240 ? (i = 0, u = c, r = a) : l < 300 ? (i = c, u = 0, r = a) : (i = a, u = 0, r = c), [
    Math.round((i + s) * 255),
    Math.round((u + s) * 255),
    Math.round((r + s) * 255)
  ];
}
function Fa(l) {
  return Ba(...Gt(l));
}
function ct(l, t, n) {
  return Xt(...Oa(l, t, n));
}
const { useCallback: Me, useRef: At } = e, $a = ({ color: l, onColorChange: t }) => {
  const [n, a, c] = Fa(l), s = At(null), i = At(null), u = Me(
    (_, S) => {
      var I;
      const C = (I = s.current) == null ? void 0 : I.getBoundingClientRect();
      if (!C) return;
      const v = Math.max(0, Math.min(1, (_ - C.left) / C.width)), y = Math.max(0, Math.min(1, 1 - (S - C.top) / C.height));
      t(ct(n, v, y));
    },
    [n, t]
  ), r = Me(
    (_) => {
      _.preventDefault(), _.target.setPointerCapture(_.pointerId), u(_.clientX, _.clientY);
    },
    [u]
  ), o = Me(
    (_) => {
      _.buttons !== 0 && u(_.clientX, _.clientY);
    },
    [u]
  ), m = Me(
    (_) => {
      var y;
      const S = (y = i.current) == null ? void 0 : y.getBoundingClientRect();
      if (!S) return;
      const v = Math.max(0, Math.min(1, (_ - S.top) / S.height)) * 360;
      t(ct(v, a, c));
    },
    [a, c, t]
  ), p = Me(
    (_) => {
      _.preventDefault(), _.target.setPointerCapture(_.pointerId), m(_.clientY);
    },
    [m]
  ), f = Me(
    (_) => {
      _.buttons !== 0 && m(_.clientY);
    },
    [m]
  ), g = ct(n, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: s,
      className: "tlColorInput__svField",
      style: { backgroundColor: g },
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
function Ua(l, t) {
  const n = t.toUpperCase();
  return l.some((a) => a != null && a.toUpperCase() === n);
}
const Ha = {
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
}, { useState: Ke, useCallback: we, useEffect: Bt, useRef: Wa, useLayoutEffect: za } = e, Va = ({
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
  const [o, m] = Ke("palette"), [p, f] = Ke(t), g = Wa(null), _ = ue(Ha), [S, C] = Ke(null);
  za(() => {
    if (!l.current || !g.current) return;
    const B = l.current.getBoundingClientRect(), M = g.current.getBoundingClientRect();
    let q = B.bottom + 4, d = B.left;
    q + M.height > window.innerHeight && (q = B.top - M.height - 4), d + M.width > window.innerWidth && (d = Math.max(0, B.right - M.width)), C({ top: q, left: d });
  }, [l]);
  const v = p != null, [y, I, L] = v ? Gt(p) : [0, 0, 0], [b, w] = Ke((p == null ? void 0 : p.toUpperCase()) ?? "");
  Bt(() => {
    w((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), Le(!0, { ESCAPE: u }), Bt(() => {
    const B = (q) => {
      g.current && !g.current.contains(q.target) && u();
    }, M = setTimeout(() => document.addEventListener("mousedown", B), 0);
    return () => {
      clearTimeout(M), document.removeEventListener("mousedown", B);
    };
  }, [u]);
  const h = we(
    (B) => (M) => {
      const q = parseInt(M.target.value, 10);
      if (isNaN(q)) return;
      const d = Yt(q);
      f(Xt(B === "r" ? d : y, B === "g" ? d : I, B === "b" ? d : L));
    },
    [y, I, L]
  ), D = we(
    (B) => {
      if (p != null) {
        B.dataTransfer.setData(Kt, p.toUpperCase()), B.dataTransfer.effectAllowed = "move";
        const M = document.createElement("div");
        M.style.width = "33px", M.style.height = "33px", M.style.backgroundColor = p, M.style.borderRadius = "3px", M.style.border = "1px solid rgba(0,0,0,0.1)", M.style.position = "absolute", M.style.top = "-9999px", document.body.appendChild(M), B.dataTransfer.setDragImage(M, 16, 16), requestAnimationFrame(() => document.body.removeChild(M));
      }
    },
    [p]
  ), R = we((B) => {
    const M = B.target.value;
    w(M), ht(M) && f(M);
  }, []), N = we(() => {
    f(null);
  }, []), z = we((B) => {
    f(B);
  }, []), A = we(
    (B) => {
      i(B);
    },
    [i]
  ), x = we(
    (B, M) => {
      const q = [...n], d = q[B];
      q[B] = q[M], q[M] = d, r(q);
    },
    [n, r]
  ), O = we(
    (B, M) => {
      const q = [...n];
      q[B] = M, r(q);
    },
    [n, r]
  ), Z = we(() => {
    r([...c]);
  }, [c, r]), H = we(
    (B) => {
      if (Ua(n, B)) return;
      const M = n.indexOf(null);
      if (M < 0) return;
      const q = [...n];
      q[M] = B.toUpperCase(), r(q);
    },
    [n, r]
  ), F = we(() => {
    p != null && H(p), i(p);
  }, [p, i, H]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: g,
      style: S ? { top: S.top, left: S.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (o === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("palette")
      },
      _["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (o === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("mixer")
      },
      _["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, o === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      Aa,
      {
        colors: n,
        columns: a,
        onSelect: z,
        onConfirm: A,
        onSwap: x,
        onReplace: O
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: Z }, _["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement($a, { color: p ?? "#000000", onColorChange: f }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, _["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, _["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (v ? "" : " tlColorInput--noColor"),
        style: v ? { backgroundColor: p } : void 0,
        draggable: v,
        onDragStart: v ? D : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, _["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? y : "",
        onChange: h("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, _["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? I : "",
        onChange: h("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, _["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? L : "",
        onChange: h("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, _["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (b !== "" && !ht(b) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: b,
        onChange: R
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, s && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: N }, _["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, _["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: F }, _["js.colorInput.ok"]))
  );
}, Ka = { "js.colorInput.chooseColor": "Choose color" }, { useState: Ya, useCallback: Ye, useRef: Ga } = e, Xa = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), c = oe(), s = ue(Ka), [i, u] = Ya(!1), r = Ga(null), o = n, m = t.editable !== !1, p = t.palette ?? [], f = t.paletteColumns ?? 6, g = t.defaultPalette ?? p, _ = Ye(() => {
    m && u(!0);
  }, [m]), S = Ye(
    (y) => {
      u(!1), a(y);
    },
    [a]
  ), C = Ye(() => {
    u(!1);
  }, []), v = Ye(
    (y) => {
      c("paletteChanged", { palette: y });
    },
    [c]
  );
  return m ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: r,
      className: "tlColorInput__swatch" + (o == null ? " tlColorInput__swatch--noColor" : ""),
      style: o != null ? { backgroundColor: o } : void 0,
      onClick: _,
      disabled: t.disabled === !0,
      title: o ?? "",
      "aria-label": s["js.colorInput.chooseColor"]
    }
  ), i && /* @__PURE__ */ e.createElement(
    Va,
    {
      anchorRef: r,
      currentColor: o,
      palette: p,
      paletteColumns: f,
      defaultPalette: g,
      canReset: t.canReset !== !1,
      onConfirm: S,
      onCancel: C,
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
}, { useState: Ue, useCallback: De, useEffect: it, useRef: Ot, useLayoutEffect: qa, useMemo: Za } = e, Qa = {
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
}, Ja = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: a,
  onSelect: c,
  onCancel: s,
  onLoadIcons: i
}) => {
  const u = ue(Qa), [r, o] = Ue("simple"), [m, p] = Ue(""), [f, g] = Ue(t ?? ""), [_, S] = Ue(!1), [C, v] = Ue(null), y = Ot(null), I = Ot(null);
  qa(() => {
    if (!l.current || !y.current) return;
    const A = l.current.getBoundingClientRect(), x = y.current.getBoundingClientRect();
    let O = A.bottom + 4, Z = A.left;
    O + x.height > window.innerHeight && (O = A.top - x.height - 4), Z + x.width > window.innerWidth && (Z = Math.max(0, A.right - x.width)), v({ top: O, left: Z });
  }, [l]), it(() => {
    !a && !_ && i().catch(() => S(!0));
  }, [a, _, i]), it(() => {
    a && I.current && I.current.focus();
  }, [a]), Le(!0, { ESCAPE: s }), it(() => {
    const A = (O) => {
      y.current && !y.current.contains(O.target) && s();
    }, x = setTimeout(() => document.addEventListener("mousedown", A), 0);
    return () => {
      clearTimeout(x), document.removeEventListener("mousedown", A);
    };
  }, [s]);
  const L = Za(() => {
    if (!m) return n;
    const A = m.toLowerCase();
    return n.filter(
      (x) => x.prefix.toLowerCase().includes(A) || x.label.toLowerCase().includes(A) || x.terms != null && x.terms.some((O) => O.includes(A))
    );
  }, [n, m]), b = De((A) => {
    p(A.target.value);
  }, []), w = De(
    (A) => {
      c(A);
    },
    [c]
  ), h = De((A) => {
    g(A);
  }, []), D = De((A) => {
    g(A.target.value);
  }, []), R = De(() => {
    c(f || null);
  }, [f, c]), N = De(() => {
    c(null);
  }, [c]), z = De(async (A) => {
    A.preventDefault(), S(!1);
    try {
      await i();
    } catch {
      S(!0);
    }
  }, [i]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlIconSelect__popup",
      ref: y,
      style: C ? { top: C.top, left: C.left, visibility: "visible" } : { visibility: "hidden" }
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
        ref: I,
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
      !a && !_ && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      _ && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: z }, u["js.iconSelect.loadError"])),
      a && L.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      a && L.map(
        (A) => A.variants.map((x) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: x.encoded,
            className: "tlIconSelect__iconCell" + (x.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": x.encoded === t,
            tabIndex: 0,
            title: A.label,
            onClick: () => r === "simple" ? w(x.encoded) : h(x.encoded),
            onKeyDown: (O) => {
              (O.key === "Enter" || O.key === " ") && (O.preventDefault(), r === "simple" ? w(x.encoded) : h(x.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(ye, { encoded: x.encoded })
        ))
      )
    ),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: f,
        onChange: D
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, f && /* @__PURE__ */ e.createElement(ye, { encoded: f })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, f ? f.startsWith("css:") ? f.substring(4) : f : ""))),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: s }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: N }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: R }, u["js.iconSelect.ok"]))
  );
}, er = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: tr, useCallback: Ge, useRef: nr } = e, lr = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), c = oe(), s = ue(er), [i, u] = tr(!1), r = nr(null), o = n, m = t.editable !== !1, p = t.disabled === !0, f = t.icons ?? [], g = t.iconsLoaded === !0, _ = Ge(() => {
    m && !p && u(!0);
  }, [m, p]), S = Ge(
    (y) => {
      u(!1), a(y);
    },
    [a]
  ), C = Ge(() => {
    u(!1);
  }, []), v = Ge(async () => {
    await c("loadIcons");
  }, [c]);
  return m ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: r,
      className: "tlIconSelect__swatch" + (o == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: _,
      disabled: p,
      title: o ?? "",
      "aria-label": s["js.iconSelect.chooseIcon"]
    },
    o ? /* @__PURE__ */ e.createElement(ye, { encoded: o }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), i && /* @__PURE__ */ e.createElement(
    Ja,
    {
      anchorRef: r,
      currentValue: o,
      icons: f,
      iconsLoaded: g,
      onSelect: S,
      onCancel: C,
      onLoadIcons: v
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, o ? /* @__PURE__ */ e.createElement(ye, { encoded: o }) : null));
}, { useCallback: je, useEffect: ar, useMemo: Ft, useRef: rr, useState: ut } = e, or = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, sr = [1, 2, 3, 4];
function cr(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const a = parseFloat(n[1]), c = n[2] || "px";
  return c === "rem" || c === "em" ? a * t : a;
}
function ir(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let a = 1;
  for (const c of sr)
    n >= c && (a = c);
  return a;
}
function ur(l, t) {
  const n = or[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function dr(l, t) {
  const n = Math.max(1, t), a = {}, c = (p, f) => !!(a[p] && a[p][f]), s = (p, f) => {
    a[p] || (a[p] = {}), a[p][f] = !0;
  }, i = [];
  let u = 0, r = 0;
  const o = (p) => {
    let f = null;
    for (const _ of i) _.rowStart === p && (f = _);
    if (!f) return;
    let g = f.colEnd;
    for (; g < n && !c(p, g); ) g++;
    if (g !== f.colEnd) {
      for (let _ = f.rowStart; _ < f.rowEnd; _++)
        for (let S = f.colEnd; S < g; S++) s(_, S);
      f.colEnd = g;
    }
  };
  for (const p of l) {
    const f = n <= 1 ? 1 : Math.max(1, p.rowSpan || 1);
    let g = Math.min(ur(p.width, n), n);
    for (; c(u, r); )
      r++, r >= n && (r = 0, u++);
    let _ = 0;
    for (let I = r; I < n && !c(u, I); I++)
      _++;
    if (g > _) {
      for (o(u), r = 0, u++; c(u, r); )
        r++, r >= n && (r = 0, u++);
      _ = 0;
      for (let I = r; I < n && !c(u, I); I++)
        _++;
      g = Math.min(g, _);
    }
    const S = r, C = r + g, v = u, y = u + f;
    i.push({ id: p.id, colStart: S, colEnd: C, rowStart: v, rowEnd: y });
    for (let I = v; I < y; I++)
      for (let L = S; L < C; L++) s(I, L);
    r = C, r >= n && (r = 0, u++);
  }
  o(u);
  let m = 0;
  for (const p of i) p.rowEnd > m && (m = p.rowEnd);
  for (let p = 1; p < m; p++)
    for (let f = 0; f < n; f++) {
      if (c(p, f)) continue;
      const g = i.find((_) => _.rowEnd === p && _.colStart <= f && f < _.colEnd);
      if (g) {
        g.rowEnd = p + 1;
        for (let _ = g.colStart; _ < g.colEnd; _++) s(p, _);
      }
    }
  return i;
}
const mr = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.minColWidth ?? "16rem", c = (t.children ?? []).filter((w) => w && w.id), s = rr(null), [i, u] = ut(1), r = t.editMode === !0;
  ar(() => {
    const w = s.current;
    if (!w) return;
    const h = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, D = cr(a, h), R = () => u(ir(w.clientWidth, D));
    R();
    const N = new ResizeObserver(R);
    return N.observe(w), () => N.disconnect();
  }, [a]);
  const o = Ft(() => dr(c, i), [c, i]), m = Ft(() => {
    const w = {};
    for (const h of o) w[h.id] = h;
    return w;
  }, [o]), [p, f] = ut(null), [g, _] = ut(null), S = je((w, h) => {
    if (!r) {
      w.preventDefault();
      return;
    }
    f(h), w.dataTransfer.effectAllowed = "move", w.dataTransfer.setData("text/plain", h);
  }, [r]), C = je((w, h) => {
    if (!r || !p || p === h) return;
    w.preventDefault(), w.dataTransfer.dropEffect = "move";
    const D = w.currentTarget.getBoundingClientRect(), R = w.clientX < D.left + D.width / 2;
    _((N) => N && N.id === h && N.before === R ? N : { id: h, before: R });
  }, [r, p]), v = je(() => {
  }, []), y = je((w, h, D) => {
    const R = c.map((x) => x.id), N = R.indexOf(w);
    if (N < 0) return;
    R.splice(N, 1);
    const z = R.indexOf(h);
    if (z < 0) {
      R.splice(N, 0, w);
      return;
    }
    const A = D ? z : z + 1;
    R.splice(A, 0, w), n("reorder", { order: R });
  }, [c, n]), I = je((w, h) => {
    if (!r || !p || p === h) return;
    w.preventDefault();
    const D = w.currentTarget.getBoundingClientRect(), R = w.clientX < D.left + D.width / 2;
    y(p, h, R), f(null), _(null);
  }, [r, p, y]), L = je(() => {
    f(null), _(null);
  }, []), b = {
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: b }, c.map((w) => {
      const h = m[w.id];
      if (!h) return null;
      const D = {
        gridColumn: `${h.colStart + 1} / ${h.colEnd + 1}`,
        gridRow: `${h.rowStart + 1} / ${h.rowEnd + 1}`
      }, R = ["tlDashboard__tile"];
      return p === w.id && R.push("tlDashboard__tile--dragging"), g && g.id === w.id && R.push(g.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: w.id,
          className: R.join(" "),
          style: D,
          draggable: r,
          onDragStart: (N) => S(N, w.id),
          onDragOver: (N) => C(N, w.id),
          onDragLeave: v,
          onDrop: (N) => I(N, w.id),
          onDragEnd: L
        },
        /* @__PURE__ */ e.createElement(K, { control: w.control }),
        r && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: pr, useRef: $t, useState: Ut, useEffect: fr, useLayoutEffect: hr } = e, br = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, a) => /* @__PURE__ */ e.createElement("span", { key: a, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(K, { control: n }))));
}, _r = ({ group: l }) => {
  var p, f;
  const [t, n] = Ut(!1), [a, c] = Ut({}), s = $t(null), i = $t(null), u = pr(() => {
    n((g) => !g);
  }, []);
  hr(() => {
    if (!t) return;
    const g = () => {
      const _ = s.current;
      if (!_) return;
      const S = _.getBoundingClientRect();
      c({
        position: "fixed",
        top: S.bottom + 4,
        right: Math.max(8, window.innerWidth - S.right),
        left: "auto"
      });
    };
    return g(), window.addEventListener("resize", g), window.addEventListener("scroll", g, !0), () => {
      window.removeEventListener("resize", g), window.removeEventListener("scroll", g, !0);
    };
  }, [t]), fr(() => {
    if (!t) return;
    const g = (_) => {
      i.current && !i.current.contains(_.target) && s.current && !s.current.contains(_.target) && n(!1);
    };
    return document.addEventListener("mousedown", g), () => document.removeEventListener("mousedown", g);
  }, [t]), Le(t, { ESCAPE: () => n(!1) }), _t(t, i, "first");
  const r = l.items.filter((g) => g != null);
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
      onMouseDown: (g) => g.preventDefault(),
      onClick: u,
      "aria-expanded": t,
      "aria-haspopup": "true",
      "aria-label": m ? o : void 0,
      title: m ? o : void 0
    },
    m ? /* @__PURE__ */ e.createElement(ye, { encoded: l.icon, className: "tlToolbar__menuIcon" }) : /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement("span", null, o), /* @__PURE__ */ e.createElement("svg", { className: "tlToolbar__chevron", viewBox: "0 0 24 24", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("polyline", { points: "6,9 12,15 18,9" })))
  ), Vt.createPortal(
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
      r.map((g, _) => /* @__PURE__ */ e.createElement("div", { key: _, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(K, { control: g }))),
      (f = l.subGroups) == null ? void 0 : f.map((g, _) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${_}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), g.items.map((S, C) => /* @__PURE__ */ e.createElement("div", { key: C, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(K, { control: S })))))
    ),
    document.body
  ));
}, gr = ({ controlId: l }) => {
  const a = (G().groups ?? []).filter((c) => c.items.some((s) => s != null));
  return a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, a.map((c, s) => /* @__PURE__ */ e.createElement(e.Fragment, { key: c.name }, s > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), c.display === "menu" ? /* @__PURE__ */ e.createElement(_r, { group: c }) : /* @__PURE__ */ e.createElement(br, { group: c }))));
}, vr = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlTileStack", style: { width: "100%", height: "100%" } }, t.frame && /* @__PURE__ */ e.createElement(K, { control: t.frame }));
}, Er = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.content, c = t.breadcrumb ?? null;
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
}, Cr = ({ controlId: l }) => {
  const n = G().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((a, c) => /* @__PURE__ */ e.createElement(K, { key: c, control: a })));
}, wr = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), yr = {
  "js.sidebar.openDrawer": "Open navigation"
}, Sr = ({ controlId: l }) => {
  const t = oe(), n = ue(yr);
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
U("TLButton", Sn);
U("TLUploadButton", kn);
U("TLToggleButton", Tn);
U("TLTextInput", un);
U("TLPasswordInput", mn);
U("TLNumberInput", fn);
U("TLDatePicker", bn);
U("TLSelect", gn);
U("TLCheckbox", wn);
U("TLCounter", Rn);
U("TLTabBar", Ln);
U("TLFieldList", xn);
U("TLAudioRecorder", Pn);
U("TLAudioPlayer", jn);
U("TLFileUpload", Bn);
U("TLBinaryField", Fn);
U("TLFileChips", Hn);
U("TLRelativeTime", Vn);
U("TLAnchor", Kn);
U("TLScrollLink", Yn);
U("TLAvatar", qn);
U("TLDownload", Qn);
U("TLPhotoCapture", el);
U("TLPhotoViewer", nl);
U("TLPdfViewer", al);
U("TLSplitPanel", rl);
U("TLPanel", ml);
U("TLInset", Sl);
U("TLMaximizeRoot", pl);
U("TLDeckPane", fl);
U("TLSidebar", wl);
U("TLStack", yl);
U("TLGrid", kl);
U("TLCard", Nl);
U("TLAppBar", Tl);
U("TLBreadcrumb", Dl);
U("TLBottomBar", xl);
U("TLDialog", Ml);
U("TLDialogManager", Bl);
U("TLWindow", Ul);
U("TLDrawer", zl);
U("TLContextMenuRegion", Kl);
U("TLSnackbar", ql);
U("TLMenu", Ql);
U("TLAppShell", ea);
U("TLText", ta);
U("TLTableView", oa);
U("TLColumnSelect", ca);
U("TLFormLayout", _a);
U("TLFormGroup", Ea);
U("TLFormField", Sa);
U("TLResourceCell", ka);
U("TLTreeView", Ta);
U("TLDropdownSelect", Ma);
U("TLColorInput", Xa);
U("TLIconSelect", lr);
U("TLDashboard", mr);
U("TLToolbar", gr);
U("TLTileStack", vr);
U("TLAdaptiveDetail", Er);
U("TLSlot", Cr);
U("TLSlotContent", wr);
U("TLDrawerToggle", Sr);
