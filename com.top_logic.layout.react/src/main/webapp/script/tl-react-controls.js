import { React as e, useTLFieldValue as ke, useTLCommand as le, useTLState as Y, useKeyboardBinding as ue, useTLUpload as Me, TLChild as V, useI18N as ce, useTLDataUrl as Ae, scrollToAnchor as Ht, useStandaloneKeyboardScope as Ne, KeyboardScopeProvider as it, useFocusTrap as ut, CMD_VALUE_CHANGED as Ue, anchoredOverlayProps as Wt, register as $ } from "tl-react-bridge";
const { useCallback: ht, useRef: zt } = e, Vt = 300, Kt = ({ controlId: l, state: t }) => {
  const [n, a, c] = ke({ debounceMs: Vt }), s = le(), i = zt(!1), u = ht(
    (w) => {
      i.current = !0, a(w.target.value);
    },
    [a]
  ), r = t.commitOnBlur === !0, o = ht(async () => {
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
}, { useCallback: bt } = e, Yt = 300, Gt = ({ controlId: l, state: t }) => {
  const [n, a, c] = ke({ debounceMs: Yt }), s = bt(
    (p) => {
      a(p.target.value);
    },
    [a]
  ), i = bt(() => {
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
}, { useCallback: _t } = e, Xt = 300, qt = ({ controlId: l, state: t, config: n }) => {
  const [a, c, s] = ke({ debounceMs: Xt }), i = _t(
    (f) => {
      const _ = f.target.value;
      c(_ === "" ? null : _);
    },
    [c]
  ), u = _t(() => {
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
}, { useCallback: Zt } = e, Qt = ({ controlId: l, state: t }) => {
  const [n, a] = ke(), c = Zt(
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
}, { useCallback: Jt } = e, en = ({ controlId: l, state: t, config: n }) => {
  var m;
  const [a, c] = ke(), s = Jt(
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
}, { useCallback: tn } = e, nn = ({ controlId: l, state: t }) => {
  const [n, a] = ke(), c = tn(
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
function Ee({ encoded: l, className: t }) {
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
const { useCallback: ln } = e, an = ({ controlId: l, command: t, label: n, image: a, disabled: c, displayMode: s }) => {
  const i = Y(), u = le(), r = t ?? "click", o = n ?? i.label, m = a ?? i.image, p = c ?? i.disabled === !0, f = s ?? i.displayMode ?? "label-only", _ = i.hidden === !0, b = i.tooltip, w = i.appearance, v = i.size, g = i.navigateUrl, E = ln(() => {
    if (g) {
      window.location.assign(g);
      return;
    }
    u(r);
  }, [u, r, g]), L = i.keyGesture;
  ue(L, () => p || _ ? !1 : (E(), !0));
  const D = f === "icon-only", C = f === "label-only" || f === "icon-label" || D && !m, y = b ?? (D ? o : void 0), h = y ? `text:${y}` : void 0;
  return _ ? null : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: E,
      disabled: p,
      className: "tlReactButton" + (D ? " tlReactButton--iconOnly" : "") + (f === "label-only" ? " tlReactButton--labelOnly" : "") + (w === "link" ? " tlReactButton--link" : "") + (w === "primary" ? " tlReactButton--primary" : "") + (v === "small" ? " tlReactButton--small" : "") + (v === "large" ? " tlReactButton--large" : ""),
      "data-tooltip": h,
      "aria-label": m || D ? o : void 0
    },
    m && /* @__PURE__ */ e.createElement(Ee, { encoded: m, className: "tlReactButton__image" }),
    C && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, o)
  );
}, rn = ({ controlId: l }) => {
  const t = Y(), n = Me(), a = e.useRef(null), [c, s] = e.useState(!1), i = t.label ?? "", u = t.image, r = t.disabled === !0, o = t.hidden === !0, m = t.displayMode ?? "label-only", p = t.appearance, f = t.accept, _ = t.multiple === !0, b = e.useCallback(() => {
    var D;
    r || c || (D = a.current) == null || D.click();
  }, [r, c]), w = e.useCallback(async (D) => {
    const C = D.target.files;
    if (!C || C.length === 0) return;
    const y = new FormData();
    for (let h = 0; h < C.length; h++)
      y.append("file", C[h], C[h].name);
    D.target.value = "", s(!0);
    try {
      await n(y);
    } finally {
      s(!1);
    }
  }, [n]), v = m === "icon-only", g = m === "icon-only" || m === "icon-label", E = m === "label-only" || m === "icon-label" || v && !u, L = r || c;
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
      disabled: L,
      style: o ? { display: "none" } : void 0,
      className: "tlReactButton" + (v ? " tlReactButton--iconOnly" : "") + (p === "link" ? " tlReactButton--link" : "") + (p === "primary" ? " tlReactButton--primary" : ""),
      "aria-label": v ? i : void 0
    },
    g && u && /* @__PURE__ */ e.createElement(Ee, { encoded: u, className: "tlReactButton__image" }),
    E && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, i)
  ));
}, { useCallback: on } = e, sn = ({ controlId: l, command: t, label: n, active: a, disabled: c }) => {
  const s = Y(), i = le(), u = t ?? "click", r = n ?? s.label, o = a ?? s.active === !0, m = c ?? s.disabled === !0, p = on(() => {
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
}, cn = ({ controlId: l }) => {
  const t = Y(), n = le(), a = t.count ?? 0, c = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: un } = e, dn = ({ controlId: l }) => {
  const t = Y(), n = le(), a = t.tabs ?? [], c = t.activeTabId, s = un((i) => {
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
    i.icon && /* @__PURE__ */ e.createElement(Ee, { encoded: i.icon, className: "tlReactTabBar__tabIcon" }),
    i.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(V, { control: t.activeContent })));
}, mn = ({ controlId: l }) => {
  const t = Y(), n = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((c, s) => /* @__PURE__ */ e.createElement("div", { key: s, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(V, { control: c })))));
}, pn = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, fn = ({ controlId: l }) => {
  const t = Y(), n = Me(), [a, c] = e.useState("idle"), [s, i] = e.useState(null), u = e.useRef(null), r = e.useRef([]), o = e.useRef(null), m = t.status ?? "idle", p = t.error, f = m === "received" ? "idle" : a !== "idle" ? a : m, _ = e.useCallback(async () => {
    if (a === "recording") {
      const E = u.current;
      E && E.state !== "inactive" && E.stop();
      return;
    }
    if (a !== "uploading") {
      if (i(null), !window.isSecureContext || !navigator.mediaDevices) {
        i("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const E = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        o.current = E, r.current = [];
        const L = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", D = new MediaRecorder(E, L ? { mimeType: L } : void 0);
        u.current = D, D.ondataavailable = (C) => {
          C.data.size > 0 && r.current.push(C.data);
        }, D.onstop = async () => {
          E.getTracks().forEach((h) => h.stop()), o.current = null;
          const C = new Blob(r.current, { type: D.mimeType || "audio/webm" });
          if (r.current = [], C.size === 0) {
            c("idle");
            return;
          }
          c("uploading");
          const y = new FormData();
          y.append("audio", C, "recording.webm"), await n(y), c("idle");
        }, D.start(), c("recording");
      } catch (E) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", E), i("js.audioRecorder.error.denied"), c("idle");
      }
    }
  }, [a, n]), b = ce(pn), w = f === "recording" ? b["js.audioRecorder.stop"] : f === "uploading" ? b["js.uploading"] : b["js.audioRecorder.record"], v = f === "uploading", g = ["tlAudioRecorder__button"];
  return f === "recording" && g.push("tlAudioRecorder__button--recording"), f === "uploading" && g.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: g.join(" "),
      onClick: _,
      disabled: v,
      title: w,
      "aria-label": w
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${f === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, b[s]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, hn = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, bn = ({ controlId: l }) => {
  const t = Y(), n = Ae(), a = !!t.hasAudio, c = t.dataRevision ?? 0, [s, i] = e.useState(a ? "idle" : "disabled"), u = e.useRef(null), r = e.useRef(null), o = e.useRef(c);
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
    const w = new Audio(r.current);
    u.current = w, w.onended = () => {
      i("idle");
    }, w.play(), i("playing");
  }, [s, n]), p = ce(hn), f = s === "loading" ? p["js.loading"] : s === "playing" ? p["js.audioPlayer.pause"] : s === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], _ = s === "disabled" || s === "loading", b = ["tlAudioPlayer__button"];
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
}, _n = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, vn = ({ controlId: l }) => {
  const t = Y(), n = Me(), [a, c] = e.useState("idle"), [s, i] = e.useState(!1), u = e.useRef(null), r = t.status ?? "idle", o = t.error, m = t.accept ?? "", p = r === "received" ? "idle" : a !== "idle" ? a : r, f = e.useCallback(async (C) => {
    c("uploading");
    const y = new FormData();
    y.append("file", C, C.name), await n(y), c("idle");
  }, [n]), _ = e.useCallback((C) => {
    var h;
    const y = (h = C.target.files) == null ? void 0 : h[0];
    y && f(y);
  }, [f]), b = e.useCallback(() => {
    var C;
    a !== "uploading" && ((C = u.current) == null || C.click());
  }, [a]), w = e.useCallback((C) => {
    C.preventDefault(), C.stopPropagation(), i(!0);
  }, []), v = e.useCallback((C) => {
    C.preventDefault(), C.stopPropagation(), i(!1);
  }, []), g = e.useCallback((C) => {
    var h;
    if (C.preventDefault(), C.stopPropagation(), i(!1), a === "uploading") return;
    const y = (h = C.dataTransfer.files) == null ? void 0 : h[0];
    y && f(y);
  }, [a, f]), E = p === "uploading", L = ce(_n), D = p === "uploading" ? L["js.uploading"] : L["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${s ? " tlFileUpload--dragover" : ""}`,
      onDragOver: w,
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
        disabled: E,
        title: D,
        "aria-label": D
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    o && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, o)
  );
}, gn = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, En = ({ controlId: l, state: t }) => {
  const a = Y() ?? t ?? {}, c = Me(), s = Ae(), i = ce(gn), u = a.editable !== !1, r = !!a.hasData, o = a.fileName ?? "download", m = a.dataRevision ?? 0, p = a.accept ?? "", f = a.status ?? "idle", _ = a.error ?? null, [b, w] = e.useState("idle"), [v, g] = e.useState(!1), [E, L] = e.useState(!1), D = e.useRef(null), C = e.useCallback(async () => {
    if (!(!r || E)) {
      L(!0);
      try {
        const F = s + (s.includes("?") ? "&" : "?") + "rev=" + m, A = await fetch(F);
        if (!A.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", A.status);
          return;
        }
        const j = await A.blob(), G = URL.createObjectURL(j), d = document.createElement("a");
        d.href = G, d.download = o, d.style.display = "none", document.body.appendChild(d), d.click(), document.body.removeChild(d), URL.revokeObjectURL(G);
      } catch (F) {
        console.error("[TLBinaryField] Fetch error:", F);
      } finally {
        L(!1);
      }
    }
  }, [r, E, s, m, o]), y = e.useCallback(async (F) => {
    w("uploading");
    const A = new FormData();
    A.append("file", F, F.name), await c(A), w("idle");
  }, [c]), h = (f === "received" ? "idle" : b !== "idle" ? b : f) === "uploading", x = e.useCallback((F) => {
    var j;
    const A = (j = F.target.files) == null ? void 0 : j[0];
    A && y(A);
  }, [y]), S = e.useCallback(() => {
    var F;
    h || (F = D.current) == null || F.click();
  }, [h]), N = e.useCallback((F) => {
    F.preventDefault(), F.stopPropagation(), g(!0);
  }, []), H = e.useCallback((F) => {
    F.preventDefault(), F.stopPropagation(), g(!1);
  }, []), B = e.useCallback((F) => {
    var j;
    if (F.preventDefault(), F.stopPropagation(), g(!1), h) return;
    const A = (j = F.dataTransfer.files) == null ? void 0 : j[0];
    A && y(A);
  }, [h, y]), R = E ? i["js.downloading"] : i["js.download.file"].replace("{0}", o), O = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (E ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: C,
      disabled: E,
      title: R,
      "aria-label": R
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o));
  if (!u)
    return r ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, O) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, i["js.download.noFile"]));
  const Q = h, z = h ? i["js.uploading"] : i["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlBinaryField tlFileUpload${v ? " tlFileUpload--dragover" : ""}`,
      onDragOver: N,
      onDragLeave: H,
      onDrop: B
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: D,
        type: "file",
        accept: p || void 0,
        onChange: x,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (Q ? " tlFileUpload__button--uploading" : ""),
        onClick: S,
        disabled: Q,
        title: z,
        "aria-label": z
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    r && O,
    _ && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, _)
  );
}, Cn = {
  "js.fileChips.add": "Add file",
  "js.fileChips.remove": "Remove {0}",
  "js.uploading": "Uploading…",
  "js.download.file": "Download {0}"
};
function wn(l) {
  return l < 1024 ? l + " B" : l < 1024 * 1024 ? (l / 1024).toFixed(l < 10 * 1024 ? 1 : 0) + " KB" : (l / (1024 * 1024)).toFixed(1) + " MB";
}
const yn = ({ controlId: l }) => {
  const t = Y(), n = le(), a = Me(), c = Ae(), s = ce(Cn), i = t.chips ?? [], u = t.editable === !0, [r, o] = e.useState(!1), [m, p] = e.useState(!1), f = e.useRef(null), _ = e.useCallback(async (C) => {
    const y = Array.from(C);
    if (y.length !== 0) {
      o(!0);
      try {
        const h = new FormData();
        for (const x of y)
          h.append("file", x, x.name);
        await a(h);
      } finally {
        o(!1);
      }
    }
  }, [a]), b = e.useCallback(async (C) => {
    if (C.hasData)
      try {
        const y = c + "&key=" + encodeURIComponent(C.key), h = await fetch(y);
        if (!h.ok) {
          console.error("[TLFileChips] Failed to fetch data:", h.status);
          return;
        }
        const x = await h.blob(), S = URL.createObjectURL(x), N = document.createElement("a");
        N.href = S, N.download = C.name, N.style.display = "none", document.body.appendChild(N), N.click(), document.body.removeChild(N), URL.revokeObjectURL(S);
      } catch (y) {
        console.error("[TLFileChips] Fetch error:", y);
      }
  }, [c]), w = e.useCallback((C) => {
    C.target.files && _(C.target.files), C.target.value = "";
  }, [_]), v = e.useCallback(() => {
    var C;
    r || (C = f.current) == null || C.click();
  }, [r]), g = e.useCallback((C) => {
    u && (C.preventDefault(), C.stopPropagation(), p(!0));
  }, [u]), E = e.useCallback((C) => {
    u && (C.preventDefault(), C.stopPropagation(), p(!1));
  }, [u]), L = e.useCallback((C) => {
    u && (C.preventDefault(), C.stopPropagation(), p(!1), !r && C.dataTransfer.files && _(C.dataTransfer.files));
  }, [u, r, _]), D = [
    "tlFileChips",
    u ? "tlFileChips--editable" : "",
    m ? "tlFileChips--dragover" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: D,
      onDragOver: g,
      onDragLeave: E,
      onDrop: L
    },
    i.map((C) => {
      const y = s["js.download.file"].replace("{0}", C.name), h = s["js.fileChips.remove"].replace("{0}", C.name);
      return /* @__PURE__ */ e.createElement("span", { key: C.key, className: "tlFileChip" }, /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__main",
          onClick: () => b(C),
          disabled: !C.hasData,
          title: C.hasData ? y : C.name
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
        C.size != null && /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__size" }, wn(C.size))
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
}, kn = 3e4;
function Sn(l, t) {
  const n = Math.round((l - Date.now()) / 1e3), a = Math.abs(n), c = new Intl.RelativeTimeFormat(t, { numeric: "auto" });
  return a < 60 ? c.format(Math.trunc(n / 1), "second") : a < 3600 ? c.format(Math.trunc(n / 60), "minute") : a < 86400 ? c.format(Math.trunc(n / 3600), "hour") : a < 7 * 86400 ? c.format(Math.trunc(n / 86400), "day") : new Date(l).toLocaleDateString(t);
}
const Nn = ({ controlId: l }) => {
  const t = Y(), n = t.timestamp, a = t.label ?? void 0, c = t.locale || navigator.language, [, s] = e.useState(0);
  return e.useEffect(() => {
    const i = setInterval(() => s((u) => u + 1), kn);
    return () => clearInterval(i);
  }, []), n == null ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime tlRelativeTime--empty" }) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime", title: a }, Sn(n, c));
}, Tn = ({ controlId: l }) => {
  const t = Y(), n = t.anchor ?? void 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAnchor", "data-tl-anchor": n }, t.child && /* @__PURE__ */ e.createElement(V, { control: t.child }));
}, Rn = ({ controlId: l }) => {
  const t = Y(), n = t.target, a = t.label ?? "";
  if (n == null)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlScrollLink tlScrollLink--empty" });
  const c = (s) => {
    s.preventDefault(), Ht(n);
  };
  return /* @__PURE__ */ e.createElement("a", { id: l, className: "tlScrollLink", href: "#", onClick: c }, a);
};
function Dn(l) {
  const t = l.trim().split(/\s+/).filter(Boolean);
  return t.length === 0 ? "?" : t.length === 1 ? t[0].slice(0, 2).toUpperCase() : (t[0][0] + t[t.length - 1][0]).toUpperCase();
}
function Ln(l) {
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return Math.abs(t) % 360;
}
const xn = ({ controlId: l }) => {
  const n = Y().name;
  return n ? /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlAvatar",
      style: { backgroundColor: `hsl(${Ln(n)}, 45%, 45%)` },
      title: n,
      "aria-label": n
    },
    Dn(n)
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlAvatar tlAvatar--empty" });
}, In = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Pn = ({ controlId: l }) => {
  const t = Y(), n = Ae(), a = le(), c = !!t.hasData, s = t.dataRevision ?? 0, i = t.fileName ?? "download", u = !!t.clearable, [r, o] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!c || r)) {
      o(!0);
      try {
        const b = n + (n.includes("?") ? "&" : "?") + "rev=" + s, w = await fetch(b);
        if (!w.ok) {
          console.error("[TLDownload] Failed to fetch data:", w.status);
          return;
        }
        const v = await w.blob(), g = URL.createObjectURL(v), E = document.createElement("a");
        E.href = g, E.download = i, E.style.display = "none", document.body.appendChild(E), E.click(), document.body.removeChild(E), URL.revokeObjectURL(g);
      } catch (b) {
        console.error("[TLDownload] Fetch error:", b);
      } finally {
        o(!1);
      }
    }
  }, [c, r, n, s, i]), p = e.useCallback(async () => {
    c && await a("clear");
  }, [c, a]), f = ce(In);
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
}, jn = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Mn = ({ controlId: l }) => {
  const t = Y(), n = Me(), [a, c] = e.useState("idle"), [s, i] = e.useState(null), [u, r] = e.useState(!1), o = e.useRef(null), m = e.useRef(null), p = e.useRef(null), f = e.useRef(null), _ = e.useRef(null), b = t.error, w = e.useMemo(
    () => {
      var N;
      return !!(window.isSecureContext && ((N = navigator.mediaDevices) != null && N.getUserMedia));
    },
    []
  ), v = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((N) => N.stop()), m.current = null), o.current && (o.current.srcObject = null);
  }, []), g = e.useCallback(() => {
    v(), c("idle");
  }, [v]), E = e.useCallback(async () => {
    var N;
    if (a !== "uploading") {
      if (i(null), !w) {
        (N = f.current) == null || N.click();
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
  }, [a, w]), L = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const N = o.current, H = p.current;
    if (!N || !H)
      return;
    H.width = N.videoWidth, H.height = N.videoHeight;
    const B = H.getContext("2d");
    B && (B.drawImage(N, 0, 0), v(), c("uploading"), H.toBlob(async (R) => {
      if (!R) {
        c("idle");
        return;
      }
      const O = new FormData();
      O.append("photo", R, "capture.jpg"), await n(O), c("idle");
    }, "image/jpeg", 0.85));
  }, [a, n, v]), D = e.useCallback(async (N) => {
    var R;
    const H = (R = N.target.files) == null ? void 0 : R[0];
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
    const N = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = N;
    };
  }, [a]), Ne(a === "overlayOpen", { ESCAPE: g }), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((N) => N.stop()), m.current = null);
  }, []);
  const C = ce(jn), y = a === "uploading" ? C["js.uploading"] : C["js.photoCapture.open"], h = ["tlPhotoCapture__cameraBtn"];
  a === "uploading" && h.push("tlPhotoCapture__cameraBtn--uploading");
  const x = ["tlPhotoCapture__overlayVideo"];
  u && x.push("tlPhotoCapture__overlayVideo--mirrored");
  const S = ["tlPhotoCapture__mirrorBtn"];
  return u && S.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: h.join(" "),
      onClick: E,
      disabled: a === "uploading",
      title: y,
      "aria-label": y
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
      onChange: D
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
        className: x.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: S.join(" "),
        onClick: () => r((N) => !N),
        title: C["js.photoCapture.mirror"],
        "aria-label": C["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: L,
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
}, An = {
  "js.photoViewer.alt": "Captured photo"
}, Bn = ({ controlId: l }) => {
  const t = Y(), n = Ae(), a = !!t.hasPhoto, c = t.dataRevision ?? 0, [s, i] = e.useState(null), u = e.useRef(c);
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
  const r = ce(An);
  return !a || !s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: s,
      alt: r["js.photoViewer.alt"]
    }
  ));
}, Fn = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, On = ({ controlId: l }) => {
  const t = Y(), n = Ae(), a = !!t.hasPdf, c = t.dataRevision ?? 0, s = ce(Fn), u = n.indexOf("react-api/"), r = u >= 0 ? n.slice(0, u) : n, o = n + "&rev=" + c, m = r + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(o);
  return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: m,
      title: s["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, s["js.pdfViewer.noDocument"]));
}, { useCallback: vt, useRef: qe } = e, $n = ({ controlId: l }) => {
  const t = Y(), n = le(), a = t.orientation, c = t.resizable === !0, s = t.children ?? [], i = a === "horizontal", u = s.length > 0 && s.every((v) => v.collapsed), r = !u && s.some((v) => v.collapsed), o = u ? !i : i, m = qe(null), p = qe(null), f = qe(null), _ = vt((v, g) => {
    const E = {
      overflow: v.scrolling || "auto"
    };
    return v.collapsed ? u && !o ? E.flex = "1 0 0%" : E.flex = "0 0 auto" : g !== void 0 ? E.flex = `0 0 ${g}px` : E.flex = `${v.size} 1 0%`, v.minSize > 0 && !v.collapsed && (E.minWidth = i ? v.minSize : void 0, E.minHeight = i ? void 0 : v.minSize), E;
  }, [i, u, r, o]), b = vt((v, g) => {
    v.preventDefault();
    const E = m.current;
    if (!E) return;
    const L = s[g], D = s[g + 1], C = E.querySelectorAll(":scope > .tlSplitPanel__child"), y = [];
    C.forEach((S) => {
      y.push(i ? S.offsetWidth : S.offsetHeight);
    }), f.current = y, p.current = {
      splitterIndex: g,
      startPos: i ? v.clientX : v.clientY,
      startSizeBefore: y[g],
      startSizeAfter: y[g + 1],
      childBefore: L,
      childAfter: D
    };
    const h = (S) => {
      const N = p.current;
      if (!N || !f.current) return;
      const B = (i ? S.clientX : S.clientY) - N.startPos, R = N.childBefore.minSize || 0, O = N.childAfter.minSize || 0;
      let Q = N.startSizeBefore + B, z = N.startSizeAfter - B;
      Q < R && (z += Q - R, Q = R), z < O && (Q += z - O, z = O), f.current[N.splitterIndex] = Q, f.current[N.splitterIndex + 1] = z;
      const F = E.querySelectorAll(":scope > .tlSplitPanel__child"), A = F[N.splitterIndex], j = F[N.splitterIndex + 1];
      A && (A.style.flex = `0 0 ${Q}px`), j && (j.style.flex = `0 0 ${z}px`);
    }, x = () => {
      if (document.removeEventListener("mousemove", h), document.removeEventListener("mouseup", x), document.body.style.cursor = "", document.body.style.userSelect = "", f.current) {
        const S = {};
        s.forEach((N, H) => {
          const B = N.control;
          B != null && B.controlId && f.current && (S[B.controlId] = f.current[H]);
        }), n("updateSizes", { sizes: S });
      }
      f.current = null, p.current = null;
    };
    document.addEventListener("mousemove", h), document.addEventListener("mouseup", x), document.body.style.cursor = i ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [s, i, n]), w = [];
  return s.forEach((v, g) => {
    if (w.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${g}`,
          className: `tlSplitPanel__child${v.collapsed && o ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: _(v)
        },
        /* @__PURE__ */ e.createElement(V, { control: v.control })
      )
    ), c && g < s.length - 1) {
      const E = s[g + 1];
      !v.collapsed && !E.collapsed && w.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${g}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${a}`,
            onMouseDown: (D) => b(D, g)
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
}, Ge = ({ image: l, className: t }) => {
  if (!l) return null;
  const n = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `${t ? t + " " : ""}${n}`, "aria-hidden": "true" });
}, { useCallback: Ze } = e, Un = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Hn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Wn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), zn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), Vn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), Kn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), Yn = ({ controlId: l }) => {
  const t = Y(), n = le(), a = ce(Un), c = t.title, s = t.expansionState ?? "NORMALIZED", i = t.showMinimize === !0, u = t.showMaximize === !0, r = t.showPopOut === !0, o = t.fullLine === !0, m = t.fill === !0, p = t.hoverActions === !0, f = t.appearance === "card", _ = t.errorMessage, b = s === "MINIMIZED", w = s === "MAXIMIZED", v = s === "HIDDEN", g = Ze(() => {
    n("toggleMinimize");
  }, [n]), E = Ze(() => {
    n("toggleMaximize");
  }, [n]), L = Ze(() => {
    n("popOut");
  }, [n]);
  if (v)
    return null;
  const D = w ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" }, C = i && !w || u && !b || r, y = !!c && c.trim() !== "" || !!t.titleContent || !!t.toolbar || C;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${s.toLowerCase()}${o ? " tlPanel--fullLine" : ""}${m ? " tlPanel--fill" : ""}${p ? " tlPanel--hoverActions" : ""}${f ? " tlPanel--card" : ""}`,
      style: D
    },
    y && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, !!c && c.trim() !== "" && /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, c), t.titleContent && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__titleContent" }, /* @__PURE__ */ e.createElement(V, { control: t.titleContent })), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(V, { control: t.toolbar }), i && !w && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: g,
        title: b ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      b ? /* @__PURE__ */ e.createElement(Wn, null) : /* @__PURE__ */ e.createElement(Hn, null)
    ), u && !b && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: E,
        title: w ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      w ? /* @__PURE__ */ e.createElement(Vn, null) : /* @__PURE__ */ e.createElement(zn, null)
    ), r && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: L,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(Kn, null)
    ))),
    !b && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(V, { control: t.child })),
    !b && _ && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error tlPanel__error", role: "alert" }, /* @__PURE__ */ e.createElement(Ge, { image: t.errorIcon, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, _)),
    !b && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(V, { control: t.buttonBar }))
  );
}, Gn = ({ controlId: l }) => {
  const t = Y();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(V, { control: t.child })
  );
}, Xn = ({ controlId: l }) => {
  const t = Y();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(V, { control: t.activeChild }));
}, { useCallback: he, useState: Ye, useEffect: rt, useRef: Xe } = e, qn = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function ot(l, t, n, a) {
  const c = [];
  for (const s of l)
    if (s.type === "nav") {
      if (s.hidden) continue;
      c.push({ id: s.id, type: "nav", groupId: a });
    } else s.type === "command" ? c.push({ id: s.id, type: "command", groupId: a }) : s.type === "group" && (c.push({ id: s.id, type: "group" }), (n.get(s.id) ?? s.expanded) && !t && c.push(...ot(s.children, t, n, s.id)));
  return c;
}
const je = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(Ee, { encoded: l, className: "tlSidebar__icon" }) : null, Zn = ({ item: l, active: t, collapsed: n, onSelect: a, tabIndex: c, itemRef: s, onFocus: i }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => a(l.id),
    title: n ? l.label : void 0,
    tabIndex: c,
    ref: s,
    onFocus: () => i(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(je, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(je, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), Qn = ({ item: l, collapsed: t, onExecute: n, tabIndex: a, itemRef: c, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: a,
    ref: c,
    onFocus: () => s(l.id)
  },
  /* @__PURE__ */ e.createElement(je, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), Jn = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(je, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), el = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), tl = ({ item: l, activeItemId: t, anchorRect: n, onSelect: a, onExecute: c, onClose: s }) => {
  const i = Xe(null);
  rt(() => {
    const o = (m) => {
      i.current && !i.current.contains(m.target) && setTimeout(() => s(), 0);
    };
    return document.addEventListener("mousedown", o), () => document.removeEventListener("mousedown", o);
  }, [s]), Ne(!0, { ESCAPE: s });
  const u = he((o) => {
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
        /* @__PURE__ */ e.createElement(je, { icon: o.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, o.label),
        o.type === "nav" && o.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, o.badge)
      );
    }
    return o.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: o.id, className: "tlSidebar__flyoutSectionHeader" }, o.label) : o.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: o.id, className: "tlSidebar__separator" }) : null;
  }));
}, nl = ({
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
  const v = Xe(null), [g, E] = Ye(null), L = he(() => {
    a ? _ === l.id ? w() : (v.current && E(v.current.getBoundingClientRect()), b(l.id)) : i(l.id);
  }, [a, _, l.id, i, b, w]), D = he((y) => {
    v.current = y, r(y);
  }, [r]), C = a && _ === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (C ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: L,
      title: a ? l.label : void 0,
      "aria-expanded": a ? C : t,
      tabIndex: u,
      ref: D,
      onFocus: () => o(l.id)
    },
    /* @__PURE__ */ e.createElement(je, { icon: l.icon }),
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
    tl,
    {
      item: l,
      activeItemId: n,
      anchorRect: g,
      onSelect: c,
      onExecute: s,
      onClose: w
    }
  ), t && !a && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((y) => /* @__PURE__ */ e.createElement(
    jt,
    {
      key: y.id,
      item: y,
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
}, jt = ({
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
        Zn,
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
        Qn,
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
      return /* @__PURE__ */ e.createElement(Jn, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(el, null);
    case "group": {
      const _ = o ? o.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        nl,
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
}, ll = ({ controlId: l }) => {
  const t = Y(), n = le(), a = ce(qn), c = t.items ?? [], s = t.activeItemId, i = t.collapsed, u = t.drawerOpen, r = u ? !1 : i, [o, m] = Ye(() => {
    const R = /* @__PURE__ */ new Map(), O = (Q) => {
      for (const z of Q)
        z.type === "group" && (R.set(z.id, z.expanded), O(z.children));
    };
    return O(c), R;
  }), p = he((R) => {
    m((O) => {
      const Q = new Map(O), z = Q.get(R) ?? !1;
      return Q.set(R, !z), n("toggleGroup", { itemId: R, expanded: !z }), Q;
    });
  }, [n]), f = he((R) => {
    R !== s && n("selectItem", { itemId: R });
  }, [n, s]), _ = he((R) => {
    n("executeCommand", { itemId: R });
  }, [n]), b = he(() => {
    n("toggleCollapse", {});
  }, [n]), w = he(() => {
    n("toggleDrawer", {});
  }, [n]), [v, g] = Ye(null), E = he((R) => {
    g(R);
  }, []), L = he(() => {
    g(null);
  }, []);
  rt(() => {
    r || g(null);
  }, [r]);
  const [D, C] = Ye(() => {
    const R = ot(c, r, o);
    return R.length > 0 ? R[0].id : "";
  }), y = Xe(/* @__PURE__ */ new Map()), h = he((R) => (O) => {
    O ? y.current.set(R, O) : y.current.delete(R);
  }, []), x = he((R) => {
    C(R);
  }, []), S = Xe(0), N = he((R) => {
    C(R), S.current++;
  }, []);
  rt(() => {
    const R = y.current.get(D);
    R && document.activeElement !== R && R.focus();
  }, [D, S.current]);
  const H = he((R) => {
    if (R.key === "Escape" && v !== null) {
      R.preventDefault(), L();
      return;
    }
    const O = ot(c, r, o);
    if (O.length === 0) return;
    const Q = O.findIndex((F) => F.id === D);
    if (Q < 0) return;
    const z = O[Q];
    switch (R.key) {
      case "ArrowDown": {
        R.preventDefault();
        const F = (Q + 1) % O.length;
        N(O[F].id);
        break;
      }
      case "ArrowUp": {
        R.preventDefault();
        const F = (Q - 1 + O.length) % O.length;
        N(O[F].id);
        break;
      }
      case "Home": {
        R.preventDefault(), N(O[0].id);
        break;
      }
      case "End": {
        R.preventDefault(), N(O[O.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        R.preventDefault(), z.type === "nav" ? f(z.id) : z.type === "command" ? _(z.id) : z.type === "group" && (r ? v === z.id ? L() : E(z.id) : p(z.id));
        break;
      }
      case "ArrowRight": {
        z.type === "group" && !r && ((o.get(z.id) ?? !1) || (R.preventDefault(), p(z.id)));
        break;
      }
      case "ArrowLeft": {
        z.type === "group" && !r && (o.get(z.id) ?? !1) && (R.preventDefault(), p(z.id));
        break;
      }
    }
  }, [
    c,
    r,
    o,
    D,
    v,
    N,
    f,
    _,
    p,
    E,
    L
  ]), B = "tlSidebar" + (r ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: B }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(V, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: w, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": a["js.sidebar.ariaLabel"] }, r ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(V, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(V, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: H }, c.map((R) => /* @__PURE__ */ e.createElement(
    jt,
    {
      key: R.id,
      item: R,
      activeItemId: s,
      collapsed: r,
      onSelect: f,
      onExecute: _,
      onToggleGroup: p,
      focusedId: D,
      setItemRef: h,
      onItemFocus: x,
      groupStates: o,
      flyoutGroupId: v,
      onOpenFlyout: E,
      onCloseFlyout: L
    }
  ))), r ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(V, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(V, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(V, { control: t.activeContent })));
}, al = ({ controlId: l }) => {
  const t = Y(), n = t.direction ?? "column", a = t.gap ?? "default", c = t.align ?? "stretch", s = t.wrap === !0, i = t.growFirst === !0, u = t.children ?? [], r = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${a}`,
    `tlStack--align-${c}`,
    s ? "tlStack--wrap" : "",
    i ? "tlStack--grow-first" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: r }, u.map((o, m) => /* @__PURE__ */ e.createElement(V, { key: m, control: o })));
}, rl = ({ controlId: l }) => {
  const t = Y();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlInset" }, t.child && /* @__PURE__ */ e.createElement(V, { control: t.child }));
}, ol = ({ controlId: l }) => {
  const t = Y(), n = t.columns, a = t.minColumnWidth, c = t.gap ?? "default", s = t.children ?? [], i = {};
  return a ? i.gridTemplateColumns = `repeat(auto-fit, minmax(min(${a}, 100%), 1fr))` : n && (i.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${c}`, style: i }, s.map((u, r) => /* @__PURE__ */ e.createElement(V, { key: r, control: u })));
}, sl = ({ controlId: l }) => {
  const t = Y(), n = t.title, a = t.variant ?? "outlined", c = t.padding ?? "default", s = t.headerActions ?? [], i = t.child, u = n != null || s.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${a}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, s.map((r, o) => /* @__PURE__ */ e.createElement(V, { key: o, control: r })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${c}` }, /* @__PURE__ */ e.createElement(V, { control: i })));
}, cl = ({ controlId: l }) => {
  const t = Y(), n = t.title ?? "", a = t.leading, c = t.children ?? [], s = t.actions ?? [], i = t.variant ?? "flat", r = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    i === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: r }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(V, { control: a })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, c.map((o, m) => /* @__PURE__ */ e.createElement(V, { key: m, control: o }))), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((o, m) => /* @__PURE__ */ e.createElement(V, { key: m, control: o }))));
}, { useCallback: il } = e, ul = ({ controlId: l }) => {
  const t = Y(), n = le(), a = t.items ?? [], c = il((s) => {
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
}, { useCallback: dl } = e, ml = ({ controlId: l }) => {
  const t = Y(), n = le(), a = t.items ?? [], c = t.activeItemId, s = dl((i) => {
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
}, { useCallback: gt, useRef: pl } = e, fl = ({ onClose: l }) => (ue("ESCAPE", () => (l(), !0)), null), hl = ({ controlId: l }) => {
  const t = Y(), n = le(), a = t.open === !0, c = t.closeOnBackdrop !== !1, s = t.child, i = pl(null), u = gt(() => {
    n("close");
  }, [n]), r = gt((o) => {
    c && o.target === o.currentTarget && u();
  }, [c, u]);
  return a ? /* @__PURE__ */ e.createElement(it, null, /* @__PURE__ */ e.createElement(fl, { onClose: u }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: r,
      ref: i,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(V, { control: s })
  )) : null;
}, { useEffect: bl, useRef: _l } = e, vl = ({ controlId: l }) => {
  const n = Y().dialogs ?? [], a = _l(n.length);
  return bl(() => {
    n.length < a.current && n.length > 0, a.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((c) => /* @__PURE__ */ e.createElement(V, { key: c.controlId, control: c })));
}, { useCallback: He, useRef: Le, useState: We } = e, gl = ({ onClose: l }) => (ue("ESCAPE", () => (l(), !0)), null), El = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Cl = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], wl = ({ controlId: l }) => {
  const t = Y(), n = le(), a = ce(El), c = t.title ?? "", s = t.width ?? "32rem", i = t.height ?? null, u = t.minHeight ?? null, r = t.resizable === !0, o = t.child, m = t.actions ?? [], p = t.toolbar, f = t.buttonBar, [_, b] = We(null), [w, v] = We(null), [g, E] = We(null), L = Le(null), [D, C] = We(!1), y = Le(null), h = Le(null), x = Le(null), S = Le(null), N = Le(null), H = He(() => {
    n("close");
  }, [n]);
  ut(!0, S, "field");
  const B = He((F, A) => {
    A.preventDefault();
    const j = S.current;
    if (!j) return;
    const G = j.getBoundingClientRect(), d = !L.current, T = L.current ?? { x: G.left, y: G.top };
    d && (L.current = T, E(T)), N.current = {
      dir: F,
      startX: A.clientX,
      startY: A.clientY,
      startW: G.width,
      startH: G.height,
      startPos: { ...T },
      symmetric: d
    };
    const W = (q) => {
      const M = N.current;
      if (!M) return;
      const J = q.clientX - M.startX, ae = q.clientY - M.startY;
      let ne = M.startW, pe = M.startH, be = 0, _e = 0;
      M.symmetric ? (M.dir.includes("e") && (ne = M.startW + 2 * J), M.dir.includes("w") && (ne = M.startW - 2 * J), M.dir.includes("s") && (pe = M.startH + 2 * ae), M.dir.includes("n") && (pe = M.startH - 2 * ae)) : (M.dir.includes("e") && (ne = M.startW + J), M.dir.includes("w") && (ne = M.startW - J, be = J), M.dir.includes("s") && (pe = M.startH + ae), M.dir.includes("n") && (pe = M.startH - ae, _e = ae));
      const Ce = Math.max(200, ne), we = Math.max(100, pe);
      M.symmetric ? (be = (M.startW - Ce) / 2, _e = (M.startH - we) / 2) : (M.dir.includes("w") && Ce === 200 && (be = M.startW - 200), M.dir.includes("n") && we === 100 && (_e = M.startH - 100)), h.current = Ce, x.current = we, b(Ce), v(we);
      const Te = {
        x: M.startPos.x + be,
        y: M.startPos.y + _e
      };
      L.current = Te, E(Te);
    }, U = () => {
      document.removeEventListener("mousemove", W), document.removeEventListener("mouseup", U);
      const q = h.current, M = x.current;
      (q != null || M != null) && n("resize", {
        ...q != null ? { width: Math.round(q) } : {},
        ...M != null ? { height: Math.round(M) } : {}
      }), N.current = null;
    };
    document.addEventListener("mousemove", W), document.addEventListener("mouseup", U);
  }, [n]), R = He((F) => {
    if (F.button !== 0 || F.target.closest("button")) return;
    F.preventDefault();
    const A = S.current;
    if (!A) return;
    const j = A.getBoundingClientRect(), G = L.current ?? { x: j.left, y: j.top }, d = F.clientX - G.x, T = F.clientY - G.y, W = (q) => {
      const M = window.innerWidth, J = window.innerHeight;
      let ae = q.clientX - d, ne = q.clientY - T;
      const pe = A.offsetWidth, be = A.offsetHeight;
      ae + pe > M && (ae = M - pe), ne + be > J && (ne = J - be), ae < 0 && (ae = 0), ne < 0 && (ne = 0);
      const _e = { x: ae, y: ne };
      L.current = _e, E(_e);
    }, U = () => {
      document.removeEventListener("mousemove", W), document.removeEventListener("mouseup", U);
    };
    document.addEventListener("mousemove", W), document.addEventListener("mouseup", U);
  }, []), O = He(() => {
    var F, A;
    if (D) {
      const j = y.current;
      j && (E(j.x !== -1 ? { x: j.x, y: j.y } : null), b(j.w), v(j.h)), C(!1);
    } else {
      const j = S.current, G = j == null ? void 0 : j.getBoundingClientRect();
      y.current = {
        x: ((F = L.current) == null ? void 0 : F.x) ?? (G == null ? void 0 : G.left) ?? -1,
        y: ((A = L.current) == null ? void 0 : A.y) ?? (G == null ? void 0 : G.top) ?? -1,
        w: _ ?? (G == null ? void 0 : G.width) ?? null,
        h: w ?? null
      }, C(!0), E({ x: 0, y: 0 }), b(null), v(null);
    }
  }, [D, _, w]), Q = D ? { position: "absolute", top: 0, left: 0, width: "100vw", maxWidth: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: _ != null ? _ + "px" : s,
    ...w != null ? { height: w + "px" } : i != null ? { height: i } : {},
    ...u != null && w == null ? { minHeight: u } : {},
    maxHeight: g ? "100vh" : "80vh",
    ...g ? { position: "absolute", left: g.x + "px", top: g.y + "px" } : {}
  }, z = l + "-title";
  return /* @__PURE__ */ e.createElement(it, { modal: !0 }, /* @__PURE__ */ e.createElement(gl, { onClose: H }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: Q,
      ref: S,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": z
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${D ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: D ? void 0 : R,
        onDoubleClick: r ? O : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: z }, c),
      p && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(V, { control: p })),
      r && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: O,
          title: D ? a["js.window.restore"] : a["js.window.maximize"]
        },
        D ? (
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(V, { control: o })),
    (m.length > 0 || f) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, f && /* @__PURE__ */ e.createElement(V, { control: f }), m.map((F, A) => /* @__PURE__ */ e.createElement(V, { key: A, control: F }))),
    r && !D && Cl.map((F) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: F,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${F}`,
        onMouseDown: (A) => B(F, A)
      }
    ))
  ));
}, { useCallback: yl } = e, kl = {
  "js.drawer.close": "Close"
}, Sl = ({ controlId: l }) => {
  const t = Y(), n = le(), a = ce(kl), c = t.open === !0, s = t.position ?? "right", i = t.size ?? "medium", u = t.title ?? null, r = t.child, o = yl(() => {
    n("close");
  }, [n]);
  Ne(c, { ESCAPE: o });
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, r && /* @__PURE__ */ e.createElement(V, { control: r })));
}, { useCallback: Nl } = e, Tl = ({ controlId: l }) => {
  const t = Y(), n = le(), a = t.child, c = Nl((s) => {
    s.preventDefault(), s.stopPropagation(), n("openContextMenu", { x: s.clientX, y: s.clientY });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tl-context-menu-region", onContextMenu: c }, a && /* @__PURE__ */ e.createElement(V, { control: a }));
}, { useCallback: Rl, useEffect: Dl, useState: Ll } = e, xl = ({ controlId: l }) => {
  const t = Y(), n = le(), a = t.message ?? "", c = t.content ?? "", s = t.variant ?? "info", i = t.duration ?? 5e3, u = t.visible === !0, r = t.generation ?? 0, [o, m] = Ll(!1), p = Rl(() => {
    m(!0), setTimeout(() => {
      n("dismiss", { generation: r }), m(!1);
    }, 200);
  }, [n, r]);
  return Dl(() => {
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
}, { useCallback: Qe, useEffect: Et, useRef: Il, useState: Ct } = e, Pl = ({ controlId: l }) => {
  const t = Y(), n = le(), a = t.open === !0, c = t.anchorId, s = t.anchorX, i = t.anchorY, u = t.items ?? [], r = Il(null), [o, m] = Ct({ top: 0, left: 0 }), [p, f] = Ct(0), _ = u.filter((g) => g.type === "item" && !g.disabled);
  Et(() => {
    var h, x;
    if (!a) return;
    const g = ((h = r.current) == null ? void 0 : h.offsetHeight) ?? 200, E = ((x = r.current) == null ? void 0 : x.offsetWidth) ?? 200;
    if (s != null && i != null) {
      let S = i, N = s;
      S + g > window.innerHeight && (S = Math.max(0, window.innerHeight - g)), N + E > window.innerWidth && (N = Math.max(0, window.innerWidth - E)), m({ top: S, left: N }), f(0);
      return;
    }
    if (!c) return;
    const L = document.getElementById(c);
    if (!L) return;
    const D = L.getBoundingClientRect();
    let C = D.bottom + 4, y = D.left;
    C + g > window.innerHeight && (C = D.top - g - 4), y + E > window.innerWidth && (y = D.right - E), m({ top: C, left: y }), f(0);
  }, [a, c, s, i]);
  const b = Qe(() => {
    n("close");
  }, [n]), w = Qe((g) => {
    n("selectItem", { itemId: g });
  }, [n]);
  Et(() => {
    if (!a) return;
    const g = (E) => {
      r.current && !r.current.contains(E.target) && b();
    };
    return document.addEventListener("mousedown", g), () => document.removeEventListener("mousedown", g);
  }, [a, b]);
  const v = Qe((g) => {
    if (g.key === "Escape") {
      g.preventDefault(), b();
      return;
    }
    if (g.key === "ArrowDown")
      g.preventDefault(), f((E) => (E + 1) % _.length);
    else if (g.key === "ArrowUp")
      g.preventDefault(), f((E) => (E - 1 + _.length) % _.length);
    else if (g.key === "Enter" || g.key === " ") {
      g.preventDefault();
      const E = _[p];
      E && w(E.id);
    }
  }, [b, w, _, p]);
  return ut(a, r), a ? /* @__PURE__ */ e.createElement(
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
    u.map((g, E) => {
      if (g.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: E, className: "tlMenu__separator" });
      const D = _.indexOf(g) === p;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: g.id,
          type: "button",
          className: "tlMenu__item" + (D ? " tlMenu__item--focused" : "") + (g.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: g.disabled,
          tabIndex: D ? 0 : -1,
          onClick: () => w(g.id)
        },
        g.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + g.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, g.label)
      );
    })
  ) : null;
}, jl = 768, Ml = ({ controlId: l }) => {
  const t = Y(), n = le();
  e.useEffect(() => {
    const o = window.matchMedia(`(max-width: ${jl}px)`), m = (f) => {
      n("reportDisplayClass", { displayClass: f ? "COMPACT" : "REGULAR" });
    };
    m(o.matches);
    const p = (f) => m(f.matches);
    return o.addEventListener("change", p), () => o.removeEventListener("change", p);
  }, [n]);
  const a = t.header, c = t.content, s = t.footer, i = t.snackbar, u = t.dialogManager, r = t.menuOverlay;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(V, { control: a })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(V, { control: c })), s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(V, { control: s })), /* @__PURE__ */ e.createElement(V, { control: i }), u && /* @__PURE__ */ e.createElement(V, { control: u }), r && /* @__PURE__ */ e.createElement(V, { control: r }));
}, Al = ({ controlId: l }) => {
  const t = Y(), n = t.text ?? "", a = t.cssClass ?? "", c = t.hasTooltip === !0, s = a ? `tlText ${a}` : "tlText";
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: s,
      "data-tooltip": c ? "key:tooltip" : void 0
    },
    n
  );
}, Bl = ({ isMulti: l, cursorIndex: t, onMove: n, onToggle: a, onSelectAll: c }) => (ue("ArrowUp", () => (n("up", !1, !1), !0)), ue("ArrowDown", () => (n("down", !1, !1), !0)), ue("Home", () => (n("home", !1, !1), !0)), ue("End", () => (n("end", !1, !1), !0)), ue("PageUp", () => (n("pageUp", !1, !1), !0)), ue("PageDown", () => (n("pageDown", !1, !1), !0)), ue("Shift+ArrowUp", () => (n("up", l, !1), !0)), ue("Shift+ArrowDown", () => (n("down", l, !1), !0)), ue("Shift+Home", () => (n("home", l, !1), !0)), ue("Shift+End", () => (n("end", l, !1), !0)), ue("Shift+PageUp", () => (n("pageUp", l, !1), !0)), ue("Shift+PageDown", () => (n("pageDown", l, !1), !0)), ue("Ctrl+ArrowUp", () => (n("up", !1, l), !0)), ue("Ctrl+ArrowDown", () => (n("down", !1, l), !0)), ue("Space", () => t < 0 ? !1 : (a(), !0)), ue("Ctrl+A", () => l ? (c(), !0) : !1), null), Fl = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all",
  "js.table.filter": "Filter"
}, wt = 50;
function yt(l) {
  var n;
  const t = l.target;
  return !!((n = t == null ? void 0 : t.closest) != null && n.call(t, 'input, textarea, select, button, a, [contenteditable="true"]'));
}
const Ol = ({ controlId: l }) => {
  const t = Y(), n = le(), a = ce(Fl), c = e.useRef(null);
  e.useEffect(() => {
    const k = c.current;
    if (!k) return;
    const I = (X) => {
      const ee = X.detail;
      let re = ee.target;
      for (; re && re !== k; ) {
        const fe = re.dataset.row, se = re.dataset.col;
        if (fe != null && se != null) {
          ee.resolved = { key: fe + "|" + se };
          return;
        }
        re = re.parentElement;
      }
    };
    return k.addEventListener("tl-tooltip-resolve", I), () => k.removeEventListener("tl-tooltip-resolve", I);
  }, []);
  const s = t.columns ?? [], i = t.totalRowCount ?? 0, u = t.rows ?? [], r = t.rowHeight ?? 36, o = t.selectionMode ?? "single", m = t.selectedCount ?? 0, p = t.cursorIndex ?? -1, f = t.frozenColumnCount ?? 0, _ = t.treeMode ?? !1, b = e.useMemo(
    () => s.filter((k) => k.sortPriority && k.sortPriority > 0).length,
    [s]
  ), w = o === "multi", v = 40, g = 20, E = e.useRef(null), L = e.useRef(null), D = e.useRef(null), [C, y] = e.useState({}), h = e.useRef(null), x = e.useRef(!1), S = e.useRef(null), [N, H] = e.useState(null), [B, R] = e.useState(null);
  e.useEffect(() => {
    h.current || y({});
  }, [s]);
  const O = e.useCallback((k) => C[k.name] ?? k.width, [C]), Q = e.useMemo(() => {
    const k = [];
    let I = w && f > 0 ? v : 0;
    for (let X = 0; X < f && X < s.length; X++)
      k.push(I), I += O(s[X]);
    return k;
  }, [s, f, w, v, O]), z = i * r, F = e.useRef(null), A = e.useCallback((k, I, X) => {
    X.preventDefault(), X.stopPropagation(), h.current = { column: k, startX: X.clientX, startWidth: I };
    let ee = X.clientX, re = 0;
    const fe = () => {
      const ie = h.current;
      if (!ie) return;
      const ve = Math.max(wt, ie.startWidth + (ee - ie.startX) + re);
      y((De) => ({ ...De, [ie.column]: ve }));
    }, se = () => {
      const ie = L.current, ve = E.current;
      if (!ie || !h.current) return;
      const De = ie.getBoundingClientRect(), mt = 40, pt = 8, Ut = ie.scrollLeft;
      ee > De.right - mt ? ie.scrollLeft += pt : ee < De.left + mt && (ie.scrollLeft = Math.max(0, ie.scrollLeft - pt));
      const ft = ie.scrollLeft - Ut;
      ft !== 0 && (ve && (ve.scrollLeft = ie.scrollLeft), re += ft, fe()), F.current = requestAnimationFrame(se);
    };
    F.current = requestAnimationFrame(se);
    const Re = (ie) => {
      ee = ie.clientX, fe();
    }, $e = (ie) => {
      document.removeEventListener("mousemove", Re), document.removeEventListener("mouseup", $e), F.current !== null && (cancelAnimationFrame(F.current), F.current = null);
      const ve = h.current;
      if (ve) {
        const De = Math.max(wt, ve.startWidth + (ie.clientX - ve.startX) + re);
        n("columnResize", { column: ve.column, width: De }), h.current = null, x.current = !0, requestAnimationFrame(() => {
          x.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", Re), document.addEventListener("mouseup", $e);
  }, [n]), j = e.useCallback(() => {
    E.current && L.current && (E.current.scrollLeft = L.current.scrollLeft), D.current !== null && clearTimeout(D.current), D.current = window.setTimeout(() => {
      const k = L.current;
      if (!k) return;
      const I = k.scrollTop, X = Math.ceil(k.clientHeight / r), ee = Math.floor(I / r);
      n("scroll", { start: ee, count: X });
    }, 80);
  }, [n, r]), G = e.useCallback((k, I, X) => {
    if (x.current) return;
    let ee;
    !I || I === "desc" ? ee = "asc" : ee = "desc";
    const re = X.shiftKey ? "add" : "replace";
    n("sort", { column: k, direction: ee, mode: re });
  }, [n]), d = e.useCallback((k, I) => {
    S.current = k, I.dataTransfer.effectAllowed = "move", I.dataTransfer.setData("text/plain", k);
  }, []), T = e.useCallback((k, I) => {
    if (!S.current || S.current === k) {
      H(null);
      return;
    }
    I.preventDefault(), I.dataTransfer.dropEffect = "move";
    const X = I.currentTarget.getBoundingClientRect(), ee = I.clientX < X.left + X.width / 2 ? "left" : "right";
    H({ column: k, side: ee });
  }, []), W = e.useCallback((k) => {
    k.preventDefault(), k.stopPropagation();
    const I = S.current;
    if (!I || !N) {
      S.current = null, H(null);
      return;
    }
    let X = s.findIndex((re) => re.name === N.column);
    if (X < 0) {
      S.current = null, H(null);
      return;
    }
    const ee = s.findIndex((re) => re.name === I);
    N.side === "right" && X++, ee < X && X--, n("columnReorder", { column: I, targetIndex: X }), S.current = null, H(null);
  }, [s, N, n]), U = e.useCallback(() => {
    S.current = null, H(null);
  }, []), q = e.useCallback((k, I) => {
    var ee;
    const X = window.getSelection();
    X && !X.isCollapsed && I.currentTarget.contains(X.anchorNode) || (yt(I) || (ee = L.current) == null || ee.focus({ preventScroll: !0 }), n("select", {
      rowIndex: k,
      ctrlKey: I.ctrlKey || I.metaKey,
      shiftKey: I.shiftKey
    }));
  }, [n]), M = e.useCallback((k, I, X) => {
    n("moveSelection", { direction: k, extend: I, move: X });
  }, [n]), J = e.useCallback(() => {
    p < 0 || n("select", { rowIndex: p, ctrlKey: w, shiftKey: !1 });
  }, [n, p, w]), ae = e.useCallback(() => {
    n("selectAll", { selected: !0 });
  }, [n]), ne = e.useCallback(
    () => !!c.current && c.current.contains(document.activeElement),
    []
  );
  e.useEffect(() => {
    if (p < 0)
      return;
    const k = L.current;
    if (!k)
      return;
    const I = p * r, X = I + r;
    I < k.scrollTop ? k.scrollTop = I : X > k.scrollTop + k.clientHeight && (k.scrollTop = X - k.clientHeight);
  }, [p, r]);
  const pe = e.useCallback((k, I) => {
    I.stopPropagation(), n("select", { rowIndex: k, ctrlKey: !0, shiftKey: !1 });
  }, [n]), be = e.useCallback(() => {
    const k = m === i && i > 0;
    n("selectAll", { selected: !k });
  }, [n, m, i]), _e = e.useCallback((k, I, X) => {
    X.stopPropagation(), n("expand", { rowIndex: k, expanded: I });
  }, [n]), Ce = e.useCallback((k, I) => {
    I.preventDefault(), R({ x: I.clientX, y: I.clientY, colIdx: k });
  }, []), we = e.useCallback(() => {
    B && (n("setFrozenColumnCount", { count: B.colIdx + 1 }), R(null));
  }, [B, n]), Te = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), R(null);
  }, [n]);
  e.useEffect(() => {
    if (!B) return;
    const k = () => R(null);
    return document.addEventListener("mousedown", k), () => document.removeEventListener("mousedown", k);
  }, [B]), Ne(!!B, { ESCAPE: () => R(null) });
  const Oe = e.useCallback((k, I) => {
    I.stopPropagation(), I.preventDefault(), n("openFilter", { column: k });
  }, [n]), P = s.reduce((k, I) => k + O(I), 0) + (w ? v : 0), K = m === i && i > 0, te = m > 0 && m < i, oe = e.useCallback((k) => {
    k && (k.indeterminate = te);
  }, [te]);
  return /* @__PURE__ */ e.createElement(it, { active: ne }, /* @__PURE__ */ e.createElement(
    Bl,
    {
      isMulti: w,
      cursorIndex: p,
      onMove: M,
      onToggle: J,
      onSelectAll: ae
    }
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: c,
      id: l,
      className: "tlTableView",
      "data-tooltip": "dynamic",
      onDragOver: (k) => {
        if (!S.current) return;
        k.preventDefault();
        const I = L.current, X = E.current;
        if (!I) return;
        const ee = I.getBoundingClientRect(), re = 40, fe = 8;
        k.clientX < ee.left + re ? I.scrollLeft = Math.max(0, I.scrollLeft - fe) : k.clientX > ee.right - re && (I.scrollLeft += fe), X && (X.scrollLeft = I.scrollLeft);
      },
      onDrop: W
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: E }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { width: P } }, w && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (f > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: v,
          minWidth: v,
          ...f > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (k) => {
          S.current && (k.preventDefault(), k.dataTransfer.dropEffect = "move", s.length > 0 && s[0].name !== S.current && H({ column: s[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: oe,
          className: "tlTableView__checkbox",
          checked: K,
          onChange: be
        }
      )
    ), s.map((k, I) => {
      const X = O(k);
      s.length - 1;
      let ee = "tlTableView__headerCell";
      k.sortable && (ee += " tlTableView__headerCell--sortable"), N && N.column === k.name && (ee += " tlTableView__headerCell--dragOver-" + N.side);
      const re = I < f, fe = I === f - 1;
      return re && (ee += " tlTableView__headerCell--frozen"), fe && (ee += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: k.name,
          className: ee,
          style: {
            width: X,
            minWidth: X,
            position: re ? "sticky" : "relative",
            ...re ? { left: Q[I], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: k.sortable ? (se) => G(k.name, k.sortDirection, se) : void 0,
          onContextMenu: (se) => Ce(I, se),
          onDragStart: (se) => d(k.name, se),
          onDragOver: (se) => T(k.name, se),
          onDrop: W,
          onDragEnd: U
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, k.label),
        k.filterable && /* @__PURE__ */ e.createElement(
          "button",
          {
            type: "button",
            className: "tlTableView__filterButton" + (k.filterActive ? " tlTableView__filterButton--active" : ""),
            title: a["js.table.filter"],
            style: {
              border: "none",
              background: "transparent",
              cursor: "pointer",
              padding: "0 4px",
              color: k.filterActive ? "#1565c0" : "inherit"
            },
            onMouseDown: (se) => se.stopPropagation(),
            onClick: (se) => Oe(k.name, se)
          },
          /* @__PURE__ */ e.createElement("i", { className: k.filterActive ? "bi bi-funnel-fill" : "bi bi-funnel" })
        ),
        k.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, k.sortDirection === "asc" ? "▲" : "▼", b > 1 && k.sortPriority != null && k.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, k.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (se) => A(k.name, X, se)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (k) => {
          if (S.current && s.length > 0) {
            const I = s[s.length - 1];
            I.name !== S.current && (k.preventDefault(), k.dataTransfer.dropEffect = "move", H({ column: I.name, side: "right" }));
          }
        },
        onDrop: W
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: L,
        className: "tlTableView__body",
        onScroll: j,
        tabIndex: 0
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: z, position: "relative", width: P } }, u.map((k) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: k.id,
          className: "tlTableView__row" + (k.selected ? " tlTableView__row--selected" : "") + (k.index === p ? " tlTableView__row--cursor" : ""),
          style: {
            position: "absolute",
            top: k.index * r,
            height: r,
            width: P,
            ...k.index === p ? { outline: "2px solid var(--color-primary, #1a73e8)", outlineOffset: "-2px" } : {}
          },
          onMouseDown: (I) => {
            (I.shiftKey || I.ctrlKey || I.metaKey || I.detail > 1) && !yt(I) && I.preventDefault();
          },
          onClick: (I) => q(k.index, I)
        },
        w && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (f > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: v,
              minWidth: v,
              ...f > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (I) => I.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: k.selected,
              onChange: () => {
              },
              onClick: (I) => pe(k.index, I),
              tabIndex: -1
            }
          )
        ),
        s.map((I, X) => {
          const ee = O(I), re = X === s.length - 1, fe = X < f, se = X === f - 1;
          let Re = "tlTableView__cell";
          fe && (Re += " tlTableView__cell--frozen"), se && (Re += " tlTableView__cell--frozenLast");
          const $e = _ && X === 0, ie = k.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: I.name,
              className: Re,
              "data-row": k.id,
              "data-col": I.name,
              style: {
                ...re && !fe ? { flex: "1 0 auto", minWidth: ee } : { width: ee, minWidth: ee },
                ...fe ? { position: "sticky", left: Q[X], zIndex: 2 } : {}
              }
            },
            $e ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ie * g } }, k.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (ve) => _e(k.index, !k.expanded, ve)
              },
              k.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(V, { control: k.cells[I.name] })) : /* @__PURE__ */ e.createElement(V, { control: k.cells[I.name] })
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
        onMouseDown: (k) => k.stopPropagation()
      },
      B.colIdx + 1 !== f && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: we }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      f > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Te }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
    )
  ));
}, $l = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, Mt = e.createContext($l), { useMemo: Ul, useRef: Hl, useState: Wl, useEffect: zl } = e, Vl = 320, Kl = ({ controlId: l }) => {
  const t = Y(), n = t.maxColumns ?? 3, a = t.labelPosition ?? "auto", c = t.readOnly === !0, s = t.children ?? [], i = t.noModelMessage, u = Hl(null), [r, o] = Wl(
    a === "top" ? "top" : "side"
  );
  zl(() => {
    if (a !== "auto") {
      o(a);
      return;
    }
    const b = u.current;
    if (!b) return;
    const w = new ResizeObserver((v) => {
      for (const g of v) {
        const L = g.contentRect.width / n;
        o(L < Vl ? "top" : "side");
      }
    });
    return w.observe(b), () => w.disconnect();
  }, [a, n]);
  const m = Ul(() => ({
    readOnly: c,
    resolvedLabelPosition: r
  }), [c, r]), f = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, _ = [
    "tlFormLayout",
    c ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return i ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, i)) : /* @__PURE__ */ e.createElement(Mt.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: l, className: _, style: f, ref: u }, s.map((b, w) => /* @__PURE__ */ e.createElement(V, { key: w, control: b }))));
}, { useCallback: Yl } = e, Gl = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, Xl = ({ controlId: l }) => {
  const t = Y(), n = le(), a = ce(Gl), c = t.headerControl ?? null, s = t.headerActions ?? [], i = t.collapsible === !0, u = t.collapsed === !0, r = t.border ?? "none", o = t.fullLine === !0, m = t.children ?? [], p = c != null || s.length > 0 || i, f = Yl(() => {
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
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(V, { control: c })), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, s.map((b, w) => /* @__PURE__ */ e.createElement(V, { key: w, control: b })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((b, w) => /* @__PURE__ */ e.createElement(V, { key: w, control: b }))));
}, { useContext: ql, useState: Zl, useCallback: Ql } = e, Jl = ({ controlId: l }) => {
  const t = Y(), n = ql(Mt), a = t.label ?? "", c = t.required === !0, s = t.error, i = t.errorIcon, u = t.warnings, r = t.warningIcon, o = t.helpText, m = t.dirty === !0, p = t.labelPosition ?? n.resolvedLabelPosition, f = t.fullLine === !0, _ = t.visible !== !1, b = t.hasTooltip === !0, w = t.field, v = n.readOnly, [g, E] = Zl(!1), L = Ql(() => E((x) => !x), []), D = p === "hidden", C = s != null, y = u != null && u.length > 0, h = [
    "tlFormField",
    `tlFormField--${p}`,
    v ? "tlFormField--readonly" : "",
    f ? "tlFormField--fullLine" : "",
    C ? "tlFormField--error" : "",
    !C && y ? "tlFormField--warning" : "",
    m ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: h, style: _ ? void 0 : { display: "none" } }, !D && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
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
      onClick: L,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(V, { control: w })), !v && C && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(Ge, { image: i, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, s)), !v && !C && y && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, u.map((x, S) => /* @__PURE__ */ e.createElement("div", { key: S, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(Ge, { image: r, className: "tlFormField__warningIcon" }), /* @__PURE__ */ e.createElement("span", null, x)))), !v && o && g && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, o));
}, ea = ({ controlId: l }) => {
  const t = Y(), n = le(), a = t.iconCss, c = t.iconSrc, s = t.label, i = t.cssClass, u = t.hasTooltip === !0, r = t.hasLink, o = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : c ? /* @__PURE__ */ e.createElement("img", { src: c, className: "tlTypeIcon", alt: "" }) : null, m = /* @__PURE__ */ e.createElement(e.Fragment, null, o, s && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, s)), p = e.useCallback((b) => {
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
}, ta = 20, na = () => {
  var y;
  const l = Y(), t = le(), n = l.nodes ?? [], a = l.selectionMode ?? "single", c = l.dragEnabled ?? !1, s = l.dropEnabled ?? !1, i = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [r, o] = e.useState(-1), m = e.useRef(null), p = ((y = n.find((h) => h.selected)) == null ? void 0 : y.id) ?? null;
  e.useEffect(() => {
    var x;
    if (p == null)
      return;
    const h = (x = m.current) == null ? void 0 : x.querySelector(".tlTreeView__node--selected");
    h && h.scrollIntoView({ block: "nearest" });
  }, [p]);
  const f = e.useCallback((h, x) => {
    t(x ? "collapse" : "expand", { nodeId: h });
  }, [t]), _ = e.useCallback((h, x) => {
    var N;
    const S = window.getSelection();
    S && !S.isCollapsed && x.currentTarget.contains(S.anchorNode) || ((N = m.current) == null || N.focus({ preventScroll: !0 }), t("select", {
      nodeId: h,
      ctrlKey: x.ctrlKey || x.metaKey,
      shiftKey: x.shiftKey
    }));
  }, [t]), b = e.useCallback((h, x) => {
    x.preventDefault(), t("contextMenu", { nodeId: h, x: x.clientX, y: x.clientY });
  }, [t]), w = e.useRef(null), v = e.useCallback((h, x) => {
    const S = x.getBoundingClientRect(), N = h.clientY - S.top, H = S.height / 3;
    return N < H ? "above" : N > H * 2 ? "below" : "within";
  }, []), g = e.useCallback((h, x) => {
    x.dataTransfer.effectAllowed = "move", x.dataTransfer.setData("text/plain", h);
  }, []), E = e.useCallback((h, x) => {
    x.preventDefault(), x.dataTransfer.dropEffect = "move";
    const S = v(x, x.currentTarget);
    w.current != null && window.clearTimeout(w.current), w.current = window.setTimeout(() => {
      t("dragOver", { nodeId: h, position: S }), w.current = null;
    }, 50);
  }, [t, v]), L = e.useCallback((h, x) => {
    x.preventDefault(), w.current != null && (window.clearTimeout(w.current), w.current = null);
    const S = v(x, x.currentTarget);
    t("drop", { nodeId: h, position: S });
  }, [t, v]), D = e.useCallback(() => {
    w.current != null && (window.clearTimeout(w.current), w.current = null), t("dragEnd");
  }, [t]), C = e.useCallback((h) => {
    if (n.length === 0) return;
    let x = r;
    switch (h.key) {
      case "ArrowDown":
        h.preventDefault(), x = Math.min(r + 1, n.length - 1);
        break;
      case "ArrowUp":
        h.preventDefault(), x = Math.max(r - 1, 0);
        break;
      case "ArrowRight":
        if (h.preventDefault(), r >= 0 && r < n.length) {
          const S = n[r];
          if (S.expandable && !S.expanded) {
            t("expand", { nodeId: S.id });
            return;
          } else S.expanded && (x = r + 1);
        }
        break;
      case "ArrowLeft":
        if (h.preventDefault(), r >= 0 && r < n.length) {
          const S = n[r];
          if (S.expanded) {
            t("collapse", { nodeId: S.id });
            return;
          } else {
            const N = S.depth;
            for (let H = r - 1; H >= 0; H--)
              if (n[H].depth < N) {
                x = H;
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
        h.preventDefault(), x = 0;
        break;
      case "End":
        h.preventDefault(), x = n.length - 1;
        break;
      default:
        return;
    }
    x !== r && o(x);
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
    n.map((h, x) => /* @__PURE__ */ e.createElement(
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
          x === r ? "tlTreeView__node--focused" : "",
          i === h.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          i === h.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          i === h.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: h.depth * ta },
        draggable: c,
        onMouseDown: (S) => {
          (S.shiftKey || S.ctrlKey || S.metaKey || S.detail > 1) && S.preventDefault();
        },
        onClick: (S) => _(h.id, S),
        onContextMenu: (S) => b(h.id, S),
        onDragStart: (S) => g(h.id, S),
        onDragOver: s ? (S) => E(h.id, S) : void 0,
        onDrop: s ? (S) => L(h.id, S) : void 0,
        onDragEnd: D
      },
      h.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (S) => {
            S.stopPropagation(), f(h.id, h.expanded);
          },
          tabIndex: -1,
          "aria-label": h.expanded ? "Collapse" : "Expand"
        },
        h.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: h.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(V, { control: h.content }))
    ))
  );
};
var Je = { exports: {} }, de = {}, et = { exports: {} }, Z = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var kt;
function la() {
  if (kt) return Z;
  kt = 1;
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
  }, w = Object.assign, v = {};
  function g(d, T, W) {
    this.props = d, this.context = T, this.refs = v, this.updater = W || b;
  }
  g.prototype.isReactComponent = {}, g.prototype.setState = function(d, T) {
    if (typeof d != "object" && typeof d != "function" && d != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, d, T, "setState");
  }, g.prototype.forceUpdate = function(d) {
    this.updater.enqueueForceUpdate(this, d, "forceUpdate");
  };
  function E() {
  }
  E.prototype = g.prototype;
  function L(d, T, W) {
    this.props = d, this.context = T, this.refs = v, this.updater = W || b;
  }
  var D = L.prototype = new E();
  D.constructor = L, w(D, g.prototype), D.isPureReactComponent = !0;
  var C = Array.isArray;
  function y() {
  }
  var h = { H: null, A: null, T: null, S: null }, x = Object.prototype.hasOwnProperty;
  function S(d, T, W) {
    var U = W.ref;
    return {
      $$typeof: l,
      type: d,
      key: T,
      ref: U !== void 0 ? U : null,
      props: W
    };
  }
  function N(d, T) {
    return S(d.type, T, d.props);
  }
  function H(d) {
    return typeof d == "object" && d !== null && d.$$typeof === l;
  }
  function B(d) {
    var T = { "=": "=0", ":": "=2" };
    return "$" + d.replace(/[=:]/g, function(W) {
      return T[W];
    });
  }
  var R = /\/+/g;
  function O(d, T) {
    return typeof d == "object" && d !== null && d.key != null ? B("" + d.key) : T.toString(36);
  }
  function Q(d) {
    switch (d.status) {
      case "fulfilled":
        return d.value;
      case "rejected":
        throw d.reason;
      default:
        switch (typeof d.status == "string" ? d.then(y, y) : (d.status = "pending", d.then(
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
  function z(d, T, W, U, q) {
    var M = typeof d;
    (M === "undefined" || M === "boolean") && (d = null);
    var J = !1;
    if (d === null) J = !0;
    else
      switch (M) {
        case "bigint":
        case "string":
        case "number":
          J = !0;
          break;
        case "object":
          switch (d.$$typeof) {
            case l:
            case t:
              J = !0;
              break;
            case m:
              return J = d._init, z(
                J(d._payload),
                T,
                W,
                U,
                q
              );
          }
      }
    if (J)
      return q = q(d), J = U === "" ? "." + O(d, 0) : U, C(q) ? (W = "", J != null && (W = J.replace(R, "$&/") + "/"), z(q, T, W, "", function(pe) {
        return pe;
      })) : q != null && (H(q) && (q = N(
        q,
        W + (q.key == null || d && d.key === q.key ? "" : ("" + q.key).replace(
          R,
          "$&/"
        ) + "/") + J
      )), T.push(q)), 1;
    J = 0;
    var ae = U === "" ? "." : U + ":";
    if (C(d))
      for (var ne = 0; ne < d.length; ne++)
        U = d[ne], M = ae + O(U, ne), J += z(
          U,
          T,
          W,
          M,
          q
        );
    else if (ne = _(d), typeof ne == "function")
      for (d = ne.call(d), ne = 0; !(U = d.next()).done; )
        U = U.value, M = ae + O(U, ne++), J += z(
          U,
          T,
          W,
          M,
          q
        );
    else if (M === "object") {
      if (typeof d.then == "function")
        return z(
          Q(d),
          T,
          W,
          U,
          q
        );
      throw T = String(d), Error(
        "Objects are not valid as a React child (found: " + (T === "[object Object]" ? "object with keys {" + Object.keys(d).join(", ") + "}" : T) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return J;
  }
  function F(d, T, W) {
    if (d == null) return d;
    var U = [], q = 0;
    return z(d, U, "", "", function(M) {
      return T.call(W, M, q++);
    }), U;
  }
  function A(d) {
    if (d._status === -1) {
      var T = d._result;
      T = T(), T.then(
        function(W) {
          (d._status === 0 || d._status === -1) && (d._status = 1, d._result = W);
        },
        function(W) {
          (d._status === 0 || d._status === -1) && (d._status = 2, d._result = W);
        }
      ), d._status === -1 && (d._status = 0, d._result = T);
    }
    if (d._status === 1) return d._result.default;
    throw d._result;
  }
  var j = typeof reportError == "function" ? reportError : function(d) {
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
  }, G = {
    map: F,
    forEach: function(d, T, W) {
      F(
        d,
        function() {
          T.apply(this, arguments);
        },
        W
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
      if (!H(d))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return d;
    }
  };
  return Z.Activity = p, Z.Children = G, Z.Component = g, Z.Fragment = n, Z.Profiler = c, Z.PureComponent = L, Z.StrictMode = a, Z.Suspense = r, Z.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = h, Z.__COMPILER_RUNTIME = {
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
  }, Z.cloneElement = function(d, T, W) {
    if (d == null)
      throw Error(
        "The argument must be a React element, but you passed " + d + "."
      );
    var U = w({}, d.props), q = d.key;
    if (T != null)
      for (M in T.key !== void 0 && (q = "" + T.key), T)
        !x.call(T, M) || M === "key" || M === "__self" || M === "__source" || M === "ref" && T.ref === void 0 || (U[M] = T[M]);
    var M = arguments.length - 2;
    if (M === 1) U.children = W;
    else if (1 < M) {
      for (var J = Array(M), ae = 0; ae < M; ae++)
        J[ae] = arguments[ae + 2];
      U.children = J;
    }
    return S(d.type, q, U);
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
  }, Z.createElement = function(d, T, W) {
    var U, q = {}, M = null;
    if (T != null)
      for (U in T.key !== void 0 && (M = "" + T.key), T)
        x.call(T, U) && U !== "key" && U !== "__self" && U !== "__source" && (q[U] = T[U]);
    var J = arguments.length - 2;
    if (J === 1) q.children = W;
    else if (1 < J) {
      for (var ae = Array(J), ne = 0; ne < J; ne++)
        ae[ne] = arguments[ne + 2];
      q.children = ae;
    }
    if (d && d.defaultProps)
      for (U in J = d.defaultProps, J)
        q[U] === void 0 && (q[U] = J[U]);
    return S(d, M, q);
  }, Z.createRef = function() {
    return { current: null };
  }, Z.forwardRef = function(d) {
    return { $$typeof: u, render: d };
  }, Z.isValidElement = H, Z.lazy = function(d) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: d },
      _init: A
    };
  }, Z.memo = function(d, T) {
    return {
      $$typeof: o,
      type: d,
      compare: T === void 0 ? null : T
    };
  }, Z.startTransition = function(d) {
    var T = h.T, W = {};
    h.T = W;
    try {
      var U = d(), q = h.S;
      q !== null && q(W, U), typeof U == "object" && U !== null && typeof U.then == "function" && U.then(y, j);
    } catch (M) {
      j(M);
    } finally {
      T !== null && W.types !== null && (T.types = W.types), h.T = T;
    }
  }, Z.unstable_useCacheRefresh = function() {
    return h.H.useCacheRefresh();
  }, Z.use = function(d) {
    return h.H.use(d);
  }, Z.useActionState = function(d, T, W) {
    return h.H.useActionState(d, T, W);
  }, Z.useCallback = function(d, T) {
    return h.H.useCallback(d, T);
  }, Z.useContext = function(d) {
    return h.H.useContext(d);
  }, Z.useDebugValue = function() {
  }, Z.useDeferredValue = function(d, T) {
    return h.H.useDeferredValue(d, T);
  }, Z.useEffect = function(d, T) {
    return h.H.useEffect(d, T);
  }, Z.useEffectEvent = function(d) {
    return h.H.useEffectEvent(d);
  }, Z.useId = function() {
    return h.H.useId();
  }, Z.useImperativeHandle = function(d, T, W) {
    return h.H.useImperativeHandle(d, T, W);
  }, Z.useInsertionEffect = function(d, T) {
    return h.H.useInsertionEffect(d, T);
  }, Z.useLayoutEffect = function(d, T) {
    return h.H.useLayoutEffect(d, T);
  }, Z.useMemo = function(d, T) {
    return h.H.useMemo(d, T);
  }, Z.useOptimistic = function(d, T) {
    return h.H.useOptimistic(d, T);
  }, Z.useReducer = function(d, T, W) {
    return h.H.useReducer(d, T, W);
  }, Z.useRef = function(d) {
    return h.H.useRef(d);
  }, Z.useState = function(d) {
    return h.H.useState(d);
  }, Z.useSyncExternalStore = function(d, T, W) {
    return h.H.useSyncExternalStore(
      d,
      T,
      W
    );
  }, Z.useTransition = function() {
    return h.H.useTransition();
  }, Z.version = "19.2.4", Z;
}
var St;
function aa() {
  return St || (St = 1, et.exports = la()), et.exports;
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
var Nt;
function ra() {
  if (Nt) return de;
  Nt = 1;
  var l = aa();
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
  return de.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = a, de.createPortal = function(r, o) {
    var m = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!o || o.nodeType !== 1 && o.nodeType !== 9 && o.nodeType !== 11)
      throw Error(t(299));
    return s(r, o, null, m);
  }, de.flushSync = function(r) {
    var o = i.T, m = a.p;
    try {
      if (i.T = null, a.p = 2, r) return r();
    } finally {
      i.T = o, a.p = m, a.d.f();
    }
  }, de.preconnect = function(r, o) {
    typeof r == "string" && (o ? (o = o.crossOrigin, o = typeof o == "string" ? o === "use-credentials" ? o : "" : void 0) : o = null, a.d.C(r, o));
  }, de.prefetchDNS = function(r) {
    typeof r == "string" && a.d.D(r);
  }, de.preinit = function(r, o) {
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
  }, de.preinitModule = function(r, o) {
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
  }, de.preload = function(r, o) {
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
  }, de.preloadModule = function(r, o) {
    if (typeof r == "string")
      if (o) {
        var m = u(o.as, o.crossOrigin);
        a.d.m(r, {
          as: typeof o.as == "string" && o.as !== "script" ? o.as : void 0,
          crossOrigin: m,
          integrity: typeof o.integrity == "string" ? o.integrity : void 0
        });
      } else a.d.m(r);
  }, de.requestFormReset = function(r) {
    a.d.r(r);
  }, de.unstable_batchedUpdates = function(r, o) {
    return r(o);
  }, de.useFormState = function(r, o, m) {
    return i.H.useFormState(r, o, m);
  }, de.useFormStatus = function() {
    return i.H.useHostTransitionStatus();
  }, de.version = "19.2.4", de;
}
var Tt;
function oa() {
  if (Tt) return Je.exports;
  Tt = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), Je.exports = ra(), Je.exports;
}
var At = oa();
const { useState: ye, useCallback: me, useRef: Be, useEffect: xe, useMemo: st } = e;
function dt({ image: l }) {
  return l ? l.startsWith("/") ? /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" }) : /* @__PURE__ */ e.createElement(Ge, { image: l, className: "tlDropdownSelect__optionIcon" }) : null;
}
function sa({
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
  const m = me(
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
    /* @__PURE__ */ e.createElement(dt, { image: l.image }),
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
function ca({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: a,
  onMouseEnter: c,
  id: s
}) {
  const i = me(() => a(l.value), [a, l.value]), u = st(() => {
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
    /* @__PURE__ */ e.createElement(dt, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const ia = ({ controlId: l, state: t }) => {
  const n = le(), a = t.value ?? [], c = t.multiSelect === !0, s = t.customOrder === !0, i = t.mandatory === !0, u = t.disabled === !0, r = t.editable !== !1, o = t.optionsLoaded === !0, m = t.options ?? [], p = t.emptyOptionLabel ?? "", f = s && c && !u && r, _ = ce({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), b = _["js.dropdownSelect.nothingFound"], w = me(
    (P) => _["js.dropdownSelect.removeChip"].replace("{0}", P),
    [_]
  ), [v, g] = ye(!1), [E, L] = ye(""), [D, C] = ye(-1), [y, h] = ye(!1), [x, S] = ye({}), [N, H] = ye(null), [B, R] = ye(null), [O, Q] = ye(null), z = Be(null), F = Be(null), A = Be(null), j = Be(a);
  j.current = a;
  const G = Be(-1), d = st(
    () => new Set(a.map((P) => P.value)),
    [a]
  ), T = st(() => {
    let P = m.filter((K) => !d.has(K.value));
    if (E) {
      const K = E.toLowerCase();
      P = P.filter((te) => te.label.toLowerCase().includes(K));
    }
    return P;
  }, [m, d, E]);
  xe(() => {
    E && T.length === 1 ? C(0) : C(-1);
  }, [T.length, E]), xe(() => {
    v && o && F.current && F.current.focus();
  }, [v, o, a]), xe(() => {
    var te, oe;
    if (G.current < 0) return;
    const P = G.current;
    G.current = -1;
    const K = (te = z.current) == null ? void 0 : te.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    K && K.length > 0 ? K[Math.min(P, K.length - 1)].focus() : (oe = z.current) == null || oe.focus();
  }, [a]), xe(() => {
    if (!v) return;
    const P = (K) => {
      z.current && !z.current.contains(K.target) && A.current && !A.current.contains(K.target) && (g(!1), L(""));
    };
    return document.addEventListener("mousedown", P), () => document.removeEventListener("mousedown", P);
  }, [v]), xe(() => {
    if (!v || !z.current) return;
    const P = z.current.getBoundingClientRect(), K = window.innerHeight - P.bottom, oe = K < 300 && P.top > K;
    S({
      left: P.left,
      width: P.width,
      ...oe ? { bottom: window.innerHeight - P.top } : { top: P.bottom }
    });
  }, [v]);
  const W = me(async () => {
    if (!(u || !r) && (g(!0), L(""), C(-1), h(!1), !o))
      try {
        await n("loadOptions");
      } catch {
        h(!0);
      }
  }, [u, r, o, n]), U = me(() => {
    var P;
    g(!1), L(""), C(-1), (P = z.current) == null || P.focus();
  }, []), q = me(
    (P) => {
      let K;
      if (c) {
        const te = m.find((oe) => oe.value === P);
        if (te)
          K = [...j.current, te];
        else
          return;
      } else {
        const te = m.find((oe) => oe.value === P);
        if (te)
          K = [te];
        else
          return;
      }
      j.current = K, n(Ue, { value: K.map((te) => te.value) }), c ? (L(""), C(-1)) : U();
    },
    [c, m, n, U]
  ), M = me(
    (P) => {
      G.current = j.current.findIndex((te) => te.value === P);
      const K = j.current.filter((te) => te.value !== P);
      j.current = K, n(Ue, { value: K.map((te) => te.value) });
    },
    [n]
  ), J = me(
    (P) => {
      P.stopPropagation(), n(Ue, { value: [] }), U();
    },
    [n, U]
  ), ae = me((P) => {
    L(P.target.value);
  }, []), ne = me(
    (P) => {
      if (!v) {
        if (P.key === "ArrowDown" || P.key === "ArrowUp" || P.key === "Enter" || P.key === " ") {
          if (P.target.tagName === "BUTTON") return;
          P.preventDefault(), P.stopPropagation(), W();
        }
        return;
      }
      switch (P.key) {
        case "ArrowDown":
          P.preventDefault(), P.stopPropagation(), C(
            (K) => K < T.length - 1 ? K + 1 : 0
          );
          break;
        case "ArrowUp":
          P.preventDefault(), P.stopPropagation(), C(
            (K) => K > 0 ? K - 1 : T.length - 1
          );
          break;
        case "Enter":
          P.preventDefault(), P.stopPropagation(), D >= 0 && D < T.length && q(T[D].value);
          break;
        case "Escape":
          P.preventDefault(), P.stopPropagation(), U();
          break;
        case "Tab":
          U();
          break;
        case "Backspace":
          E === "" && c && a.length > 0 && M(a[a.length - 1].value);
          break;
      }
    },
    [
      v,
      W,
      U,
      T,
      D,
      q,
      E,
      c,
      a,
      M
    ]
  ), pe = me(
    async (P) => {
      P.preventDefault(), h(!1);
      try {
        await n("loadOptions");
      } catch {
        h(!0);
      }
    },
    [n]
  ), be = me(
    (P, K) => {
      H(P), K.dataTransfer.effectAllowed = "move", K.dataTransfer.setData("text/plain", String(P));
    },
    []
  ), _e = me(
    (P, K) => {
      if (K.preventDefault(), K.dataTransfer.dropEffect = "move", N === null || N === P) {
        R(null), Q(null);
        return;
      }
      const te = K.currentTarget.getBoundingClientRect(), oe = te.left + te.width / 2, k = K.clientX < oe ? "before" : "after";
      R(P), Q(k);
    },
    [N]
  ), Ce = me(
    (P) => {
      if (P.preventDefault(), N === null || B === null || O === null || N === B) return;
      const K = [...j.current], [te] = K.splice(N, 1);
      let oe = B;
      N < B ? oe = O === "before" ? oe - 1 : oe : oe = O === "before" ? oe : oe + 1, K.splice(oe, 0, te), j.current = K, n(Ue, { value: K.map((k) => k.value) }), H(null), R(null), Q(null);
    },
    [N, B, O, n]
  ), we = me(() => {
    H(null), R(null), Q(null);
  }, []);
  if (xe(() => {
    if (D < 0 || !A.current) return;
    const P = A.current.querySelector(
      `[id="${l}-opt-${D}"]`
    );
    P && P.scrollIntoView({ block: "nearest" });
  }, [D, l]), !r)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, a.map((P) => /* @__PURE__ */ e.createElement("span", { key: P.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(dt, { image: P.image }), /* @__PURE__ */ e.createElement("span", null, P.label))));
  const Te = !i && a.length > 0 && !u, Oe = v ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: A,
      className: "tlDropdownSelect__dropdown",
      style: x,
      ...Wt
    },
    (o || y) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: F,
        type: "text",
        className: "tlDropdownSelect__search",
        value: E,
        onChange: ae,
        onKeyDown: ne,
        placeholder: _["js.dropdownSelect.filterPlaceholder"],
        "aria-label": _["js.dropdownSelect.filterPlaceholder"],
        "aria-activedescendant": D >= 0 ? `${l}-opt-${D}` : void 0,
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
      !o && !y && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      y && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: pe }, _["js.dropdownSelect.error"])),
      o && T.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, b),
      o && T.map((P, K) => /* @__PURE__ */ e.createElement(
        ca,
        {
          key: P.value,
          id: `${l}-opt-${K}`,
          option: P,
          highlighted: K === D,
          searchTerm: E,
          onSelect: q,
          onMouseEnter: () => C(K)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: z,
      className: "tlDropdownSelect" + (v ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": v,
      "aria-haspopup": "listbox",
      "aria-owns": v ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: v ? void 0 : W,
      onKeyDown: ne
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, a.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : a.map((P, K) => {
      let te = "";
      return N === K ? te = "tlDropdownSelect__chip--dragging" : B === K && O === "before" ? te = "tlDropdownSelect__chip--dropBefore" : B === K && O === "after" && (te = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        sa,
        {
          key: P.value,
          option: P,
          removable: !u && (c || !i),
          onRemove: M,
          removeLabel: w(P.label),
          draggable: f,
          onDragStart: f ? (oe) => be(K, oe) : void 0,
          onDragOver: f ? (oe) => _e(K, oe) : void 0,
          onDrop: f ? Ce : void 0,
          onDragEnd: f ? we : void 0,
          dragClassName: f ? te : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, Te && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: J,
        "aria-label": _["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, v ? "▲" : "▼"))
  ), Oe && At.createPortal(Oe, document.body));
}, { useCallback: tt, useRef: ua } = e, Bt = "application/x-tl-color", da = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: a,
  onSwap: c,
  onReplace: s
}) => {
  const i = ua(null), u = tt(
    (m) => (p) => {
      i.current = m, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), r = tt((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), o = tt(
    (m) => (p) => {
      p.preventDefault();
      const f = p.dataTransfer.getData(Bt);
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
function Ft(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function ct(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function Ot(l) {
  if (!ct(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function $t(l, t, n) {
  const a = (c) => Ft(c).toString(16).padStart(2, "0");
  return "#" + a(l) + a(t) + a(n);
}
function ma(l, t, n) {
  const a = l / 255, c = t / 255, s = n / 255, i = Math.max(a, c, s), u = Math.min(a, c, s), r = i - u;
  let o = 0;
  r !== 0 && (i === a ? o = (c - s) / r % 6 : i === c ? o = (s - a) / r + 2 : o = (a - c) / r + 4, o *= 60, o < 0 && (o += 360));
  const m = i === 0 ? 0 : r / i;
  return [o, m, i];
}
function pa(l, t, n) {
  const a = n * t, c = a * (1 - Math.abs(l / 60 % 2 - 1)), s = n - a;
  let i = 0, u = 0, r = 0;
  return l < 60 ? (i = a, u = c, r = 0) : l < 120 ? (i = c, u = a, r = 0) : l < 180 ? (i = 0, u = a, r = c) : l < 240 ? (i = 0, u = c, r = a) : l < 300 ? (i = c, u = 0, r = a) : (i = a, u = 0, r = c), [
    Math.round((i + s) * 255),
    Math.round((u + s) * 255),
    Math.round((r + s) * 255)
  ];
}
function fa(l) {
  return ma(...Ot(l));
}
function nt(l, t, n) {
  return $t(...pa(l, t, n));
}
const { useCallback: Ie, useRef: Rt } = e, ha = ({ color: l, onColorChange: t }) => {
  const [n, a, c] = fa(l), s = Rt(null), i = Rt(null), u = Ie(
    (b, w) => {
      var L;
      const v = (L = s.current) == null ? void 0 : L.getBoundingClientRect();
      if (!v) return;
      const g = Math.max(0, Math.min(1, (b - v.left) / v.width)), E = Math.max(0, Math.min(1, 1 - (w - v.top) / v.height));
      t(nt(n, g, E));
    },
    [n, t]
  ), r = Ie(
    (b) => {
      b.preventDefault(), b.target.setPointerCapture(b.pointerId), u(b.clientX, b.clientY);
    },
    [u]
  ), o = Ie(
    (b) => {
      b.buttons !== 0 && u(b.clientX, b.clientY);
    },
    [u]
  ), m = Ie(
    (b) => {
      var E;
      const w = (E = i.current) == null ? void 0 : E.getBoundingClientRect();
      if (!w) return;
      const g = Math.max(0, Math.min(1, (b - w.top) / w.height)) * 360;
      t(nt(g, a, c));
    },
    [a, c, t]
  ), p = Ie(
    (b) => {
      b.preventDefault(), b.target.setPointerCapture(b.pointerId), m(b.clientY);
    },
    [m]
  ), f = Ie(
    (b) => {
      b.buttons !== 0 && m(b.clientY);
    },
    [m]
  ), _ = nt(n, 1, 1);
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
function ba(l, t) {
  const n = t.toUpperCase();
  return l.some((a) => a != null && a.toUpperCase() === n);
}
const _a = {
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
}, { useState: ze, useCallback: ge, useEffect: Dt, useRef: va, useLayoutEffect: ga } = e, Ea = ({
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
  const [o, m] = ze("palette"), [p, f] = ze(t), _ = va(null), b = ce(_a), [w, v] = ze(null);
  ga(() => {
    if (!l.current || !_.current) return;
    const A = l.current.getBoundingClientRect(), j = _.current.getBoundingClientRect();
    let G = A.bottom + 4, d = A.left;
    G + j.height > window.innerHeight && (G = A.top - j.height - 4), d + j.width > window.innerWidth && (d = Math.max(0, A.right - j.width)), v({ top: G, left: d });
  }, [l]);
  const g = p != null, [E, L, D] = g ? Ot(p) : [0, 0, 0], [C, y] = ze((p == null ? void 0 : p.toUpperCase()) ?? "");
  Dt(() => {
    y((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), Ne(!0, { ESCAPE: u }), Dt(() => {
    const A = (G) => {
      _.current && !_.current.contains(G.target) && u();
    }, j = setTimeout(() => document.addEventListener("mousedown", A), 0);
    return () => {
      clearTimeout(j), document.removeEventListener("mousedown", A);
    };
  }, [u]);
  const h = ge(
    (A) => (j) => {
      const G = parseInt(j.target.value, 10);
      if (isNaN(G)) return;
      const d = Ft(G);
      f($t(A === "r" ? d : E, A === "g" ? d : L, A === "b" ? d : D));
    },
    [E, L, D]
  ), x = ge(
    (A) => {
      if (p != null) {
        A.dataTransfer.setData(Bt, p.toUpperCase()), A.dataTransfer.effectAllowed = "move";
        const j = document.createElement("div");
        j.style.width = "33px", j.style.height = "33px", j.style.backgroundColor = p, j.style.borderRadius = "3px", j.style.border = "1px solid rgba(0,0,0,0.1)", j.style.position = "absolute", j.style.top = "-9999px", document.body.appendChild(j), A.dataTransfer.setDragImage(j, 16, 16), requestAnimationFrame(() => document.body.removeChild(j));
      }
    },
    [p]
  ), S = ge((A) => {
    const j = A.target.value;
    y(j), ct(j) && f(j);
  }, []), N = ge(() => {
    f(null);
  }, []), H = ge((A) => {
    f(A);
  }, []), B = ge(
    (A) => {
      i(A);
    },
    [i]
  ), R = ge(
    (A, j) => {
      const G = [...n], d = G[A];
      G[A] = G[j], G[j] = d, r(G);
    },
    [n, r]
  ), O = ge(
    (A, j) => {
      const G = [...n];
      G[A] = j, r(G);
    },
    [n, r]
  ), Q = ge(() => {
    r([...c]);
  }, [c, r]), z = ge(
    (A) => {
      if (ba(n, A)) return;
      const j = n.indexOf(null);
      if (j < 0) return;
      const G = [...n];
      G[j] = A.toUpperCase(), r(G);
    },
    [n, r]
  ), F = ge(() => {
    p != null && z(p), i(p);
  }, [p, i, z]);
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
      da,
      {
        colors: n,
        columns: a,
        onSelect: H,
        onConfirm: B,
        onSwap: R,
        onReplace: O
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: Q }, b["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(ha, { color: p ?? "#000000", onColorChange: f }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
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
        onDragStart: g ? x : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: g ? E : "",
        onChange: h("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: g ? L : "",
        onChange: h("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: g ? D : "",
        onChange: h("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (C !== "" && !ct(C) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: C,
        onChange: S
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, s && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: N }, b["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, b["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: F }, b["js.colorInput.ok"]))
  );
}, Ca = { "js.colorInput.chooseColor": "Choose color" }, { useState: wa, useCallback: Ve, useRef: ya } = e, ka = ({ controlId: l, state: t }) => {
  const [n, a] = ke(), c = le(), s = ce(Ca), [i, u] = wa(!1), r = ya(null), o = n, m = t.editable !== !1, p = t.palette ?? [], f = t.paletteColumns ?? 6, _ = t.defaultPalette ?? p, b = Ve(() => {
    m && u(!0);
  }, [m]), w = Ve(
    (E) => {
      u(!1), a(E);
    },
    [a]
  ), v = Ve(() => {
    u(!1);
  }, []), g = Ve(
    (E) => {
      c("paletteChanged", { palette: E });
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
    Ea,
    {
      anchorRef: r,
      currentColor: o,
      palette: p,
      paletteColumns: f,
      defaultPalette: _,
      canReset: t.canReset !== !1,
      onConfirm: w,
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
}, { useState: Fe, useCallback: Se, useEffect: lt, useRef: Lt, useLayoutEffect: Sa, useMemo: Na } = e, Ta = {
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
}, Ra = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: a,
  onSelect: c,
  onCancel: s,
  onLoadIcons: i
}) => {
  const u = ce(Ta), [r, o] = Fe("simple"), [m, p] = Fe(""), [f, _] = Fe(t ?? ""), [b, w] = Fe(!1), [v, g] = Fe(null), E = Lt(null), L = Lt(null);
  Sa(() => {
    if (!l.current || !E.current) return;
    const B = l.current.getBoundingClientRect(), R = E.current.getBoundingClientRect();
    let O = B.bottom + 4, Q = B.left;
    O + R.height > window.innerHeight && (O = B.top - R.height - 4), Q + R.width > window.innerWidth && (Q = Math.max(0, B.right - R.width)), g({ top: O, left: Q });
  }, [l]), lt(() => {
    !a && !b && i().catch(() => w(!0));
  }, [a, b, i]), lt(() => {
    a && L.current && L.current.focus();
  }, [a]), Ne(!0, { ESCAPE: s }), lt(() => {
    const B = (O) => {
      E.current && !E.current.contains(O.target) && s();
    }, R = setTimeout(() => document.addEventListener("mousedown", B), 0);
    return () => {
      clearTimeout(R), document.removeEventListener("mousedown", B);
    };
  }, [s]);
  const D = Na(() => {
    if (!m) return n;
    const B = m.toLowerCase();
    return n.filter(
      (R) => R.prefix.toLowerCase().includes(B) || R.label.toLowerCase().includes(B) || R.terms != null && R.terms.some((O) => O.includes(B))
    );
  }, [n, m]), C = Se((B) => {
    p(B.target.value);
  }, []), y = Se(
    (B) => {
      c(B);
    },
    [c]
  ), h = Se((B) => {
    _(B);
  }, []), x = Se((B) => {
    _(B.target.value);
  }, []), S = Se(() => {
    c(f || null);
  }, [f, c]), N = Se(() => {
    c(null);
  }, [c]), H = Se(async (B) => {
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
      ref: E,
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
        ref: L,
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
      a && D.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      a && D.map(
        (B) => B.variants.map((R) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: R.encoded,
            className: "tlIconSelect__iconCell" + (R.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": R.encoded === t,
            tabIndex: 0,
            title: B.label,
            onClick: () => r === "simple" ? y(R.encoded) : h(R.encoded),
            onKeyDown: (O) => {
              (O.key === "Enter" || O.key === " ") && (O.preventDefault(), r === "simple" ? y(R.encoded) : h(R.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(Ee, { encoded: R.encoded })
        ))
      )
    ),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: f,
        onChange: x
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, f && /* @__PURE__ */ e.createElement(Ee, { encoded: f })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, f ? f.startsWith("css:") ? f.substring(4) : f : ""))),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: s }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: N }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: S }, u["js.iconSelect.ok"]))
  );
}, Da = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: La, useCallback: Ke, useRef: xa } = e, Ia = ({ controlId: l, state: t }) => {
  const [n, a] = ke(), c = le(), s = ce(Da), [i, u] = La(!1), r = xa(null), o = n, m = t.editable !== !1, p = t.disabled === !0, f = t.icons ?? [], _ = t.iconsLoaded === !0, b = Ke(() => {
    m && !p && u(!0);
  }, [m, p]), w = Ke(
    (E) => {
      u(!1), a(E);
    },
    [a]
  ), v = Ke(() => {
    u(!1);
  }, []), g = Ke(async () => {
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
    o ? /* @__PURE__ */ e.createElement(Ee, { encoded: o }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), i && /* @__PURE__ */ e.createElement(
    Ra,
    {
      anchorRef: r,
      currentValue: o,
      icons: f,
      iconsLoaded: _,
      onSelect: w,
      onCancel: v,
      onLoadIcons: g
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, o ? /* @__PURE__ */ e.createElement(Ee, { encoded: o }) : null));
}, { useCallback: Pe, useEffect: Pa, useMemo: xt, useRef: ja, useState: at } = e, Ma = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, Aa = [1, 2, 3, 4];
function Ba(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const a = parseFloat(n[1]), c = n[2] || "px";
  return c === "rem" || c === "em" ? a * t : a;
}
function Fa(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let a = 1;
  for (const c of Aa)
    n >= c && (a = c);
  return a;
}
function Oa(l, t) {
  const n = Ma[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function $a(l, t) {
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
    let _ = Math.min(Oa(p.width, n), n);
    for (; c(u, r); )
      r++, r >= n && (r = 0, u++);
    let b = 0;
    for (let L = r; L < n && !c(u, L); L++)
      b++;
    if (_ > b) {
      for (o(u), r = 0, u++; c(u, r); )
        r++, r >= n && (r = 0, u++);
      b = 0;
      for (let L = r; L < n && !c(u, L); L++)
        b++;
      _ = Math.min(_, b);
    }
    const w = r, v = r + _, g = u, E = u + f;
    i.push({ id: p.id, colStart: w, colEnd: v, rowStart: g, rowEnd: E });
    for (let L = g; L < E; L++)
      for (let D = w; D < v; D++) s(L, D);
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
const Ua = ({ controlId: l }) => {
  const t = Y(), n = le(), a = t.minColWidth ?? "16rem", c = (t.children ?? []).filter((y) => y && y.id), s = ja(null), [i, u] = at(1), r = t.editMode === !0;
  Pa(() => {
    const y = s.current;
    if (!y) return;
    const h = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, x = Ba(a, h), S = () => u(Fa(y.clientWidth, x));
    S();
    const N = new ResizeObserver(S);
    return N.observe(y), () => N.disconnect();
  }, [a]);
  const o = xt(() => $a(c, i), [c, i]), m = xt(() => {
    const y = {};
    for (const h of o) y[h.id] = h;
    return y;
  }, [o]), [p, f] = at(null), [_, b] = at(null), w = Pe((y, h) => {
    if (!r) {
      y.preventDefault();
      return;
    }
    f(h), y.dataTransfer.effectAllowed = "move", y.dataTransfer.setData("text/plain", h);
  }, [r]), v = Pe((y, h) => {
    if (!r || !p || p === h) return;
    y.preventDefault(), y.dataTransfer.dropEffect = "move";
    const x = y.currentTarget.getBoundingClientRect(), S = y.clientX < x.left + x.width / 2;
    b((N) => N && N.id === h && N.before === S ? N : { id: h, before: S });
  }, [r, p]), g = Pe(() => {
  }, []), E = Pe((y, h, x) => {
    const S = c.map((R) => R.id), N = S.indexOf(y);
    if (N < 0) return;
    S.splice(N, 1);
    const H = S.indexOf(h);
    if (H < 0) {
      S.splice(N, 0, y);
      return;
    }
    const B = x ? H : H + 1;
    S.splice(B, 0, y), n("reorder", { order: S });
  }, [c, n]), L = Pe((y, h) => {
    if (!r || !p || p === h) return;
    y.preventDefault();
    const x = y.currentTarget.getBoundingClientRect(), S = y.clientX < x.left + x.width / 2;
    E(p, h, S), f(null), b(null);
  }, [r, p, E]), D = Pe(() => {
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: C }, c.map((y) => {
      const h = m[y.id];
      if (!h) return null;
      const x = {
        gridColumn: `${h.colStart + 1} / ${h.colEnd + 1}`,
        gridRow: `${h.rowStart + 1} / ${h.rowEnd + 1}`
      }, S = ["tlDashboard__tile"];
      return p === y.id && S.push("tlDashboard__tile--dragging"), _ && _.id === y.id && S.push(_.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: y.id,
          className: S.join(" "),
          style: x,
          draggable: r,
          onDragStart: (N) => w(N, y.id),
          onDragOver: (N) => v(N, y.id),
          onDragLeave: g,
          onDrop: (N) => L(N, y.id),
          onDragEnd: D
        },
        /* @__PURE__ */ e.createElement(V, { control: y.control }),
        r && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: Ha, useRef: It, useState: Pt, useEffect: Wa, useLayoutEffect: za } = e, Va = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, a) => /* @__PURE__ */ e.createElement("span", { key: a, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(V, { control: n }))));
}, Ka = ({ group: l }) => {
  var p, f;
  const [t, n] = Pt(!1), [a, c] = Pt({}), s = It(null), i = It(null), u = Ha(() => {
    n((_) => !_);
  }, []);
  za(() => {
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
  }, [t]), Wa(() => {
    if (!t) return;
    const _ = (b) => {
      i.current && !i.current.contains(b.target) && s.current && !s.current.contains(b.target) && n(!1);
    };
    return document.addEventListener("mousedown", _), () => document.removeEventListener("mousedown", _);
  }, [t]), Ne(t, { ESCAPE: () => n(!1) }), ut(t, i, "first");
  const r = l.items.filter((_) => _ != null);
  if (r.length === 0) return null;
  if (r.length === 1 && !((p = l.subGroups) != null && p.length) && !l.icon)
    return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(V, { control: r[0] })));
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
    m ? /* @__PURE__ */ e.createElement(Ee, { encoded: l.icon, className: "tlToolbar__menuIcon" }) : /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement("span", null, o), /* @__PURE__ */ e.createElement("svg", { className: "tlToolbar__chevron", viewBox: "0 0 24 24", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("polyline", { points: "6,9 12,15 18,9" })))
  ), At.createPortal(
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
      r.map((_, b) => /* @__PURE__ */ e.createElement("div", { key: b, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(V, { control: _ }))),
      (f = l.subGroups) == null ? void 0 : f.map((_, b) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${b}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), _.items.map((w, v) => /* @__PURE__ */ e.createElement("div", { key: v, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(V, { control: w })))))
    ),
    document.body
  ));
}, Ya = ({ controlId: l }) => {
  const a = (Y().groups ?? []).filter((c) => c.items.some((s) => s != null));
  return a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, a.map((c, s) => /* @__PURE__ */ e.createElement(e.Fragment, { key: c.name }, s > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), c.display === "menu" ? /* @__PURE__ */ e.createElement(Ka, { group: c }) : /* @__PURE__ */ e.createElement(Va, { group: c }))));
}, Ga = ({ controlId: l }) => {
  const t = Y();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlTileStack", style: { width: "100%", height: "100%" } }, t.frame && /* @__PURE__ */ e.createElement(V, { control: t.frame }));
}, Xa = ({ controlId: l }) => {
  const t = Y(), n = le(), a = t.content, c = t.breadcrumb ?? null;
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
  })), /* @__PURE__ */ e.createElement("div", { className: "tlAdaptiveDetail__content" }, a && /* @__PURE__ */ e.createElement(V, { control: a })));
}, qa = ({ controlId: l }) => {
  const n = Y().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((a, c) => /* @__PURE__ */ e.createElement(V, { key: c, control: a })));
}, Za = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), Qa = {
  "js.sidebar.openDrawer": "Open navigation"
}, Ja = ({ controlId: l }) => {
  const t = le(), n = ce(Qa);
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
$("TLButton", an);
$("TLUploadButton", rn);
$("TLToggleButton", sn);
$("TLTextInput", Kt);
$("TLPasswordInput", Gt);
$("TLNumberInput", qt);
$("TLDatePicker", Qt);
$("TLSelect", en);
$("TLCheckbox", nn);
$("TLCounter", cn);
$("TLTabBar", dn);
$("TLFieldList", mn);
$("TLAudioRecorder", fn);
$("TLAudioPlayer", bn);
$("TLFileUpload", vn);
$("TLBinaryField", En);
$("TLFileChips", yn);
$("TLRelativeTime", Nn);
$("TLAnchor", Tn);
$("TLScrollLink", Rn);
$("TLAvatar", xn);
$("TLDownload", Pn);
$("TLPhotoCapture", Mn);
$("TLPhotoViewer", Bn);
$("TLPdfViewer", On);
$("TLSplitPanel", $n);
$("TLPanel", Yn);
$("TLInset", rl);
$("TLMaximizeRoot", Gn);
$("TLDeckPane", Xn);
$("TLSidebar", ll);
$("TLStack", al);
$("TLGrid", ol);
$("TLCard", sl);
$("TLAppBar", cl);
$("TLBreadcrumb", ul);
$("TLBottomBar", ml);
$("TLDialog", hl);
$("TLDialogManager", vl);
$("TLWindow", wl);
$("TLDrawer", Sl);
$("TLContextMenuRegion", Tl);
$("TLSnackbar", xl);
$("TLMenu", Pl);
$("TLAppShell", Ml);
$("TLText", Al);
$("TLTableView", Ol);
$("TLFormLayout", Kl);
$("TLFormGroup", Xl);
$("TLFormField", Jl);
$("TLResourceCell", ea);
$("TLTreeView", na);
$("TLDropdownSelect", ia);
$("TLColorInput", ka);
$("TLIconSelect", Ia);
$("TLDashboard", Ua);
$("TLToolbar", Ya);
$("TLTileStack", Ga);
$("TLAdaptiveDetail", Xa);
$("TLSlot", qa);
$("TLSlotContent", Za);
$("TLDrawerToggle", Ja);
