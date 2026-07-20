import { React as e, useTLFieldValue as ke, useTLCommand as le, useTLState as q, useKeyboardBinding as ue, useTLUpload as Ae, TLChild as K, useI18N as ce, useTLDataUrl as Oe, useStandaloneKeyboardScope as Ne, KeyboardScopeProvider as ct, useFocusTrap as it, CMD_VALUE_CHANGED as Ue, anchoredOverlayProps as Ft, register as W } from "tl-react-bridge";
const { useCallback: ft, useRef: Ut } = e, Ht = 300, Wt = ({ controlId: l, state: t }) => {
  const [n, a, c] = ke({ debounceMs: Ht }), s = le(), i = Ut(!1), u = ft(
    (C) => {
      i.current = !0, a(C.target.value);
    },
    [a]
  ), r = t.commitOnBlur === !0, o = ft(async () => {
    await c(), r && i.current && (i.current = !1, s("commit"));
  }, [c, r, s]), m = t.multiline === !0;
  if (t.editable === !1) {
    const C = "tlReactTextInput tlReactTextInput--immutable" + (m ? " tlReactTextInput--multiline" : "");
    return /* @__PURE__ */ e.createElement(
      "span",
      {
        id: l,
        className: C,
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
}, { useCallback: ht } = e, zt = 300, Vt = ({ controlId: l, state: t }) => {
  const [n, a, c] = ke({ debounceMs: zt }), s = ht(
    (p) => {
      a(p.target.value);
    },
    [a]
  ), i = ht(() => {
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
}, { useCallback: bt } = e, Kt = 300, Yt = ({ controlId: l, state: t, config: n }) => {
  const [a, c, s] = ke({ debounceMs: Kt }), i = bt(
    (f) => {
      const _ = f.target.value;
      c(_ === "" ? null : _);
    },
    [c]
  ), u = bt(() => {
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
}, { useCallback: Gt } = e, Xt = ({ controlId: l, state: t }) => {
  const [n, a] = ke(), c = Gt(
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
}, { useCallback: qt } = e, Zt = ({ controlId: l, state: t, config: n }) => {
  var m;
  const [a, c] = ke(), s = qt(
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
}, { useCallback: Qt } = e, Jt = ({ controlId: l, state: t }) => {
  const [n, a] = ke(), c = Qt(
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
const { useCallback: en } = e, tn = ({ controlId: l, command: t, label: n, image: a, disabled: c, displayMode: s }) => {
  const i = q(), u = le(), r = t ?? "click", o = n ?? i.label, m = a ?? i.image, p = c ?? i.disabled === !0, f = s ?? i.displayMode ?? "label-only", _ = i.hidden === !0, b = i.tooltip, C = _ ? { display: "none" } : void 0, E = i.appearance, v = i.size, g = i.navigateUrl, R = en(() => {
    if (g) {
      window.location.assign(g);
      return;
    }
    u(r);
  }, [u, r, g]), x = i.keyGesture;
  ue(x, () => p || _ ? !1 : (R(), !0));
  const D = f === "icon-only", S = f === "label-only" || f === "icon-label" || D && !m, h = b ?? (D ? o : void 0), I = h ? `text:${h}` : void 0;
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: R,
      disabled: p,
      style: C,
      className: "tlReactButton" + (D ? " tlReactButton--iconOnly" : "") + (f === "label-only" ? " tlReactButton--labelOnly" : "") + (E === "link" ? " tlReactButton--link" : "") + (E === "primary" ? " tlReactButton--primary" : "") + (v === "small" ? " tlReactButton--small" : "") + (v === "large" ? " tlReactButton--large" : ""),
      "data-tooltip": I,
      "aria-label": m || D ? o : void 0
    },
    m && /* @__PURE__ */ e.createElement(Ee, { encoded: m, className: "tlReactButton__image" }),
    S && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, o)
  );
}, nn = ({ controlId: l }) => {
  const t = q(), n = Ae(), a = e.useRef(null), [c, s] = e.useState(!1), i = t.label ?? "", u = t.image, r = t.disabled === !0, o = t.hidden === !0, m = t.displayMode ?? "label-only", p = t.appearance, f = t.accept, _ = t.multiple === !0, b = e.useCallback(() => {
    var x;
    r || c || (x = a.current) == null || x.click();
  }, [r, c]), C = e.useCallback(async (x) => {
    const D = x.target.files;
    if (!D || D.length === 0) return;
    const S = new FormData();
    for (let h = 0; h < D.length; h++)
      S.append("file", D[h], D[h].name);
    x.target.value = "", s(!0);
    try {
      await n(S);
    } finally {
      s(!1);
    }
  }, [n]), E = m === "icon-only", v = m === "icon-only" || m === "icon-label", g = m === "label-only" || m === "icon-label" || E && !u, R = r || c;
  return /* @__PURE__ */ e.createElement("span", { id: l, style: { display: "contents" } }, /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: a,
      type: "file",
      accept: f && f !== "*" ? f : void 0,
      multiple: _ || void 0,
      onChange: C,
      style: { display: "none" }
    }
  ), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: b,
      disabled: R,
      style: o ? { display: "none" } : void 0,
      className: "tlReactButton" + (E ? " tlReactButton--iconOnly" : "") + (p === "link" ? " tlReactButton--link" : "") + (p === "primary" ? " tlReactButton--primary" : ""),
      "aria-label": E ? i : void 0
    },
    v && u && /* @__PURE__ */ e.createElement(Ee, { encoded: u, className: "tlReactButton__image" }),
    g && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, i)
  ));
}, { useCallback: ln } = e, rn = ({ controlId: l, command: t, label: n, active: a, disabled: c }) => {
  const s = q(), i = le(), u = t ?? "click", r = n ?? s.label, o = a ?? s.active === !0, m = c ?? s.disabled === !0, p = ln(() => {
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
}, an = ({ controlId: l }) => {
  const t = q(), n = le(), a = t.count ?? 0, c = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: on } = e, sn = ({ controlId: l }) => {
  const t = q(), n = le(), a = t.tabs ?? [], c = t.activeTabId, s = on((i) => {
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
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(K, { control: t.activeContent })));
}, cn = ({ controlId: l }) => {
  const t = q(), n = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((c, s) => /* @__PURE__ */ e.createElement("div", { key: s, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(K, { control: c })))));
}, un = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, dn = ({ controlId: l }) => {
  const t = q(), n = Ae(), [a, c] = e.useState("idle"), [s, i] = e.useState(null), u = e.useRef(null), r = e.useRef([]), o = e.useRef(null), m = t.status ?? "idle", p = t.error, f = m === "received" ? "idle" : a !== "idle" ? a : m, _ = e.useCallback(async () => {
    if (a === "recording") {
      const g = u.current;
      g && g.state !== "inactive" && g.stop();
      return;
    }
    if (a !== "uploading") {
      if (i(null), !window.isSecureContext || !navigator.mediaDevices) {
        i("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const g = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        o.current = g, r.current = [];
        const R = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", x = new MediaRecorder(g, R ? { mimeType: R } : void 0);
        u.current = x, x.ondataavailable = (D) => {
          D.data.size > 0 && r.current.push(D.data);
        }, x.onstop = async () => {
          g.getTracks().forEach((h) => h.stop()), o.current = null;
          const D = new Blob(r.current, { type: x.mimeType || "audio/webm" });
          if (r.current = [], D.size === 0) {
            c("idle");
            return;
          }
          c("uploading");
          const S = new FormData();
          S.append("audio", D, "recording.webm"), await n(S), c("idle");
        }, x.start(), c("recording");
      } catch (g) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", g), i("js.audioRecorder.error.denied"), c("idle");
      }
    }
  }, [a, n]), b = ce(un), C = f === "recording" ? b["js.audioRecorder.stop"] : f === "uploading" ? b["js.uploading"] : b["js.audioRecorder.record"], E = f === "uploading", v = ["tlAudioRecorder__button"];
  return f === "recording" && v.push("tlAudioRecorder__button--recording"), f === "uploading" && v.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: v.join(" "),
      onClick: _,
      disabled: E,
      title: C,
      "aria-label": C
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${f === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, b[s]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, mn = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, pn = ({ controlId: l }) => {
  const t = q(), n = Oe(), a = !!t.hasAudio, c = t.dataRevision ?? 0, [s, i] = e.useState(a ? "idle" : "disabled"), u = e.useRef(null), r = e.useRef(null), o = e.useRef(c);
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
        const v = await E.blob();
        r.current = URL.createObjectURL(v);
      } catch (E) {
        console.error("[TLAudioPlayer] Fetch error:", E), i("idle");
        return;
      }
    }
    const C = new Audio(r.current);
    u.current = C, C.onended = () => {
      i("idle");
    }, C.play(), i("playing");
  }, [s, n]), p = ce(mn), f = s === "loading" ? p["js.loading"] : s === "playing" ? p["js.audioPlayer.pause"] : s === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], _ = s === "disabled" || s === "loading", b = ["tlAudioPlayer__button"];
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
}, fn = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, hn = ({ controlId: l }) => {
  const t = q(), n = Ae(), [a, c] = e.useState("idle"), [s, i] = e.useState(!1), u = e.useRef(null), r = t.status ?? "idle", o = t.error, m = t.accept ?? "", p = r === "received" ? "idle" : a !== "idle" ? a : r, f = e.useCallback(async (D) => {
    c("uploading");
    const S = new FormData();
    S.append("file", D, D.name), await n(S), c("idle");
  }, [n]), _ = e.useCallback((D) => {
    var h;
    const S = (h = D.target.files) == null ? void 0 : h[0];
    S && f(S);
  }, [f]), b = e.useCallback(() => {
    var D;
    a !== "uploading" && ((D = u.current) == null || D.click());
  }, [a]), C = e.useCallback((D) => {
    D.preventDefault(), D.stopPropagation(), i(!0);
  }, []), E = e.useCallback((D) => {
    D.preventDefault(), D.stopPropagation(), i(!1);
  }, []), v = e.useCallback((D) => {
    var h;
    if (D.preventDefault(), D.stopPropagation(), i(!1), a === "uploading") return;
    const S = (h = D.dataTransfer.files) == null ? void 0 : h[0];
    S && f(S);
  }, [a, f]), g = p === "uploading", R = ce(fn), x = p === "uploading" ? R["js.uploading"] : R["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${s ? " tlFileUpload--dragover" : ""}`,
      onDragOver: C,
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
        title: x,
        "aria-label": x
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    o && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, o)
  );
}, bn = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, _n = ({ controlId: l, state: t }) => {
  const a = q() ?? t ?? {}, c = Ae(), s = Oe(), i = ce(bn), u = a.editable !== !1, r = !!a.hasData, o = a.fileName ?? "download", m = a.dataRevision ?? 0, p = a.accept ?? "", f = a.status ?? "idle", _ = a.error ?? null, [b, C] = e.useState("idle"), [E, v] = e.useState(!1), [g, R] = e.useState(!1), x = e.useRef(null), D = e.useCallback(async () => {
    if (!(!r || g)) {
      R(!0);
      try {
        const O = s + (s.includes("?") ? "&" : "?") + "rev=" + m, B = await fetch(O);
        if (!B.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", B.status);
          return;
        }
        const M = await B.blob(), Y = URL.createObjectURL(M), d = document.createElement("a");
        d.href = Y, d.download = o, d.style.display = "none", document.body.appendChild(d), d.click(), document.body.removeChild(d), URL.revokeObjectURL(Y);
      } catch (O) {
        console.error("[TLBinaryField] Fetch error:", O);
      } finally {
        R(!1);
      }
    }
  }, [r, g, s, m, o]), S = e.useCallback(async (O) => {
    C("uploading");
    const B = new FormData();
    B.append("file", O, O.name), await c(B), C("idle");
  }, [c]), h = (f === "received" ? "idle" : b !== "idle" ? b : f) === "uploading", I = e.useCallback((O) => {
    var M;
    const B = (M = O.target.files) == null ? void 0 : M[0];
    B && S(B);
  }, [S]), y = e.useCallback(() => {
    var O;
    h || (O = x.current) == null || O.click();
  }, [h]), N = e.useCallback((O) => {
    O.preventDefault(), O.stopPropagation(), v(!0);
  }, []), U = e.useCallback((O) => {
    O.preventDefault(), O.stopPropagation(), v(!1);
  }, []), A = e.useCallback((O) => {
    var M;
    if (O.preventDefault(), O.stopPropagation(), v(!1), h) return;
    const B = (M = O.dataTransfer.files) == null ? void 0 : M[0];
    B && S(B);
  }, [h, S]), T = g ? i["js.downloading"] : i["js.download.file"].replace("{0}", o), $ = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (g ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: D,
      disabled: g,
      title: T,
      "aria-label": T
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o));
  if (!u)
    return r ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, $) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, i["js.download.noFile"]));
  const Q = h, z = h ? i["js.uploading"] : i["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlBinaryField tlFileUpload${E ? " tlFileUpload--dragover" : ""}`,
      onDragOver: N,
      onDragLeave: U,
      onDrop: A
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: x,
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
        onClick: y,
        disabled: Q,
        title: z,
        "aria-label": z
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    r && $,
    _ && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, _)
  );
}, gn = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, vn = ({ controlId: l }) => {
  const t = q(), n = Oe(), a = le(), c = !!t.hasData, s = t.dataRevision ?? 0, i = t.fileName ?? "download", u = !!t.clearable, [r, o] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!c || r)) {
      o(!0);
      try {
        const b = n + (n.includes("?") ? "&" : "?") + "rev=" + s, C = await fetch(b);
        if (!C.ok) {
          console.error("[TLDownload] Failed to fetch data:", C.status);
          return;
        }
        const E = await C.blob(), v = URL.createObjectURL(E), g = document.createElement("a");
        g.href = v, g.download = i, g.style.display = "none", document.body.appendChild(g), g.click(), document.body.removeChild(g), URL.revokeObjectURL(v);
      } catch (b) {
        console.error("[TLDownload] Fetch error:", b);
      } finally {
        o(!1);
      }
    }
  }, [c, r, n, s, i]), p = e.useCallback(async () => {
    c && await a("clear");
  }, [c, a]), f = ce(gn);
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
}, En = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Cn = ({ controlId: l }) => {
  const t = q(), n = Ae(), [a, c] = e.useState("idle"), [s, i] = e.useState(null), [u, r] = e.useState(!1), o = e.useRef(null), m = e.useRef(null), p = e.useRef(null), f = e.useRef(null), _ = e.useRef(null), b = t.error, C = e.useMemo(
    () => {
      var N;
      return !!(window.isSecureContext && ((N = navigator.mediaDevices) != null && N.getUserMedia));
    },
    []
  ), E = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((N) => N.stop()), m.current = null), o.current && (o.current.srcObject = null);
  }, []), v = e.useCallback(() => {
    E(), c("idle");
  }, [E]), g = e.useCallback(async () => {
    var N;
    if (a !== "uploading") {
      if (i(null), !C) {
        (N = f.current) == null || N.click();
        return;
      }
      try {
        const U = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        m.current = U, c("overlayOpen");
      } catch (U) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", U), i("js.photoCapture.error.denied"), c("idle");
      }
    }
  }, [a, C]), R = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const N = o.current, U = p.current;
    if (!N || !U)
      return;
    U.width = N.videoWidth, U.height = N.videoHeight;
    const A = U.getContext("2d");
    A && (A.drawImage(N, 0, 0), E(), c("uploading"), U.toBlob(async (T) => {
      if (!T) {
        c("idle");
        return;
      }
      const $ = new FormData();
      $.append("photo", T, "capture.jpg"), await n($), c("idle");
    }, "image/jpeg", 0.85));
  }, [a, n, E]), x = e.useCallback(async (N) => {
    var T;
    const U = (T = N.target.files) == null ? void 0 : T[0];
    if (!U) return;
    c("uploading");
    const A = new FormData();
    A.append("photo", U, U.name), await n(A), c("idle"), f.current && (f.current.value = "");
  }, [n]);
  e.useEffect(() => {
    a === "overlayOpen" && o.current && m.current && (o.current.srcObject = m.current);
  }, [a]), e.useEffect(() => {
    var U;
    if (a !== "overlayOpen") return;
    (U = _.current) == null || U.focus();
    const N = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = N;
    };
  }, [a]), Ne(a === "overlayOpen", { ESCAPE: v }), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((N) => N.stop()), m.current = null);
  }, []);
  const D = ce(En), S = a === "uploading" ? D["js.uploading"] : D["js.photoCapture.open"], h = ["tlPhotoCapture__cameraBtn"];
  a === "uploading" && h.push("tlPhotoCapture__cameraBtn--uploading");
  const I = ["tlPhotoCapture__overlayVideo"];
  u && I.push("tlPhotoCapture__overlayVideo--mirrored");
  const y = ["tlPhotoCapture__mirrorBtn"];
  return u && y.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: h.join(" "),
      onClick: g,
      disabled: a === "uploading",
      title: S,
      "aria-label": S
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !C && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: f,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: x
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
        className: y.join(" "),
        onClick: () => r((N) => !N),
        title: D["js.photoCapture.mirror"],
        "aria-label": D["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: R,
        title: D["js.photoCapture.capture"],
        "aria-label": D["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: v,
        title: D["js.photoCapture.close"],
        "aria-label": D["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, D[s]), b && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, b));
}, wn = {
  "js.photoViewer.alt": "Captured photo"
}, yn = ({ controlId: l }) => {
  const t = q(), n = Oe(), a = !!t.hasPhoto, c = t.dataRevision ?? 0, [s, i] = e.useState(null), u = e.useRef(c);
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
  const r = ce(wn);
  return !a || !s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: s,
      alt: r["js.photoViewer.alt"]
    }
  ));
}, kn = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, Sn = ({ controlId: l }) => {
  const t = q(), n = Oe(), a = !!t.hasPdf, c = t.dataRevision ?? 0, s = ce(kn), u = n.indexOf("react-api/"), r = u >= 0 ? n.slice(0, u) : n, o = n + "&rev=" + c, m = r + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(o);
  return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: m,
      title: s["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, s["js.pdfViewer.noDocument"]));
}, { useCallback: _t, useRef: Xe } = e, Nn = ({ controlId: l }) => {
  const t = q(), n = le(), a = t.orientation, c = t.resizable === !0, s = t.children ?? [], i = a === "horizontal", u = s.length > 0 && s.every((E) => E.collapsed), r = !u && s.some((E) => E.collapsed), o = u ? !i : i, m = Xe(null), p = Xe(null), f = Xe(null), _ = _t((E, v) => {
    const g = {
      overflow: E.scrolling || "auto"
    };
    return E.collapsed ? u && !o ? g.flex = "1 0 0%" : g.flex = "0 0 auto" : v !== void 0 ? g.flex = `0 0 ${v}px` : g.flex = `${E.size} 1 0%`, E.minSize > 0 && !E.collapsed && (g.minWidth = i ? E.minSize : void 0, g.minHeight = i ? void 0 : E.minSize), g;
  }, [i, u, r, o]), b = _t((E, v) => {
    E.preventDefault();
    const g = m.current;
    if (!g) return;
    const R = s[v], x = s[v + 1], D = g.querySelectorAll(":scope > .tlSplitPanel__child"), S = [];
    D.forEach((y) => {
      S.push(i ? y.offsetWidth : y.offsetHeight);
    }), f.current = S, p.current = {
      splitterIndex: v,
      startPos: i ? E.clientX : E.clientY,
      startSizeBefore: S[v],
      startSizeAfter: S[v + 1],
      childBefore: R,
      childAfter: x
    };
    const h = (y) => {
      const N = p.current;
      if (!N || !f.current) return;
      const A = (i ? y.clientX : y.clientY) - N.startPos, T = N.childBefore.minSize || 0, $ = N.childAfter.minSize || 0;
      let Q = N.startSizeBefore + A, z = N.startSizeAfter - A;
      Q < T && (z += Q - T, Q = T), z < $ && (Q += z - $, z = $), f.current[N.splitterIndex] = Q, f.current[N.splitterIndex + 1] = z;
      const O = g.querySelectorAll(":scope > .tlSplitPanel__child"), B = O[N.splitterIndex], M = O[N.splitterIndex + 1];
      B && (B.style.flex = `0 0 ${Q}px`), M && (M.style.flex = `0 0 ${z}px`);
    }, I = () => {
      if (document.removeEventListener("mousemove", h), document.removeEventListener("mouseup", I), document.body.style.cursor = "", document.body.style.userSelect = "", f.current) {
        const y = {};
        s.forEach((N, U) => {
          const A = N.control;
          A != null && A.controlId && f.current && (y[A.controlId] = f.current[U]);
        }), n("updateSizes", { sizes: y });
      }
      f.current = null, p.current = null;
    };
    document.addEventListener("mousemove", h), document.addEventListener("mouseup", I), document.body.style.cursor = i ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [s, i, n]), C = [];
  return s.forEach((E, v) => {
    if (C.push(
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
      !E.collapsed && !g.collapsed && C.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${v}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${a}`,
            onMouseDown: (x) => b(x, v)
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
    C
  );
}, { useCallback: qe } = e, Tn = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Rn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Dn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), xn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), Ln = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), In = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), Pn = ({ controlId: l }) => {
  const t = q(), n = le(), a = ce(Tn), c = t.title, s = t.expansionState ?? "NORMALIZED", i = t.showMinimize === !0, u = t.showMaximize === !0, r = t.showPopOut === !0, o = t.fullLine === !0, m = t.fill === !0, p = s === "MINIMIZED", f = s === "MAXIMIZED", _ = s === "HIDDEN", b = qe(() => {
    n("toggleMinimize");
  }, [n]), C = qe(() => {
    n("toggleMaximize");
  }, [n]), E = qe(() => {
    n("popOut");
  }, [n]);
  if (_)
    return null;
  const v = f ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" }, g = i && !f || u && !p || r, R = !!c && c.trim() !== "" || !!t.toolbar || g;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${s.toLowerCase()}${o ? " tlPanel--fullLine" : ""}${m ? " tlPanel--fill" : ""}`,
      style: v
    },
    R && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(K, { control: t.toolbar }), i && !f && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: b,
        title: p ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      p ? /* @__PURE__ */ e.createElement(Dn, null) : /* @__PURE__ */ e.createElement(Rn, null)
    ), u && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: C,
        title: f ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      f ? /* @__PURE__ */ e.createElement(Ln, null) : /* @__PURE__ */ e.createElement(xn, null)
    ), r && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: E,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(In, null)
    ))),
    !p && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(K, { control: t.child })),
    !p && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(K, { control: t.buttonBar }))
  );
}, Mn = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(K, { control: t.child })
  );
}, jn = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(K, { control: t.activeChild }));
}, { useCallback: he, useState: Ye, useEffect: rt, useRef: Ge } = e, Bn = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function at(l, t, n, a) {
  const c = [];
  for (const s of l)
    if (s.type === "nav") {
      if (s.hidden) continue;
      c.push({ id: s.id, type: "nav", groupId: a });
    } else s.type === "command" ? c.push({ id: s.id, type: "command", groupId: a }) : s.type === "group" && (c.push({ id: s.id, type: "group" }), (n.get(s.id) ?? s.expanded) && !t && c.push(...at(s.children, t, n, s.id)));
  return c;
}
const Me = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(Ee, { encoded: l, className: "tlSidebar__icon" }) : null, An = ({ item: l, active: t, collapsed: n, onSelect: a, tabIndex: c, itemRef: s, onFocus: i }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => a(l.id),
    title: n ? l.label : void 0,
    tabIndex: c,
    ref: s,
    onFocus: () => i(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Me, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Me, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), On = ({ item: l, collapsed: t, onExecute: n, tabIndex: a, itemRef: c, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: a,
    ref: c,
    onFocus: () => s(l.id)
  },
  /* @__PURE__ */ e.createElement(Me, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), $n = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Me, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), Fn = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), Un = ({ item: l, activeItemId: t, anchorRect: n, onSelect: a, onExecute: c, onClose: s }) => {
  const i = Ge(null);
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
        /* @__PURE__ */ e.createElement(Me, { icon: o.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, o.label),
        o.type === "nav" && o.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, o.badge)
      );
    }
    return o.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: o.id, className: "tlSidebar__flyoutSectionHeader" }, o.label) : o.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: o.id, className: "tlSidebar__separator" }) : null;
  }));
}, Hn = ({
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
  onCloseFlyout: C
}) => {
  const E = Ge(null), [v, g] = Ye(null), R = he(() => {
    a ? _ === l.id ? C() : (E.current && g(E.current.getBoundingClientRect()), b(l.id)) : i(l.id);
  }, [a, _, l.id, i, b, C]), x = he((S) => {
    E.current = S, r(S);
  }, [r]), D = a && _ === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (D ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: R,
      title: a ? l.label : void 0,
      "aria-expanded": a ? D : t,
      tabIndex: u,
      ref: x,
      onFocus: () => o(l.id)
    },
    /* @__PURE__ */ e.createElement(Me, { icon: l.icon }),
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
  ), D && /* @__PURE__ */ e.createElement(
    Un,
    {
      item: l,
      activeItemId: n,
      anchorRect: v,
      onSelect: c,
      onExecute: s,
      onClose: C
    }
  ), t && !a && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((S) => /* @__PURE__ */ e.createElement(
    It,
    {
      key: S.id,
      item: S,
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
      onCloseFlyout: C
    }
  ))));
}, It = ({
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
        An,
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
        On,
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
      return /* @__PURE__ */ e.createElement($n, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(Fn, null);
    case "group": {
      const _ = o ? o.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        Hn,
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
}, Wn = ({ controlId: l }) => {
  const t = q(), n = le(), a = ce(Bn), c = t.items ?? [], s = t.activeItemId, i = t.collapsed, u = t.drawerOpen, r = u ? !1 : i, [o, m] = Ye(() => {
    const T = /* @__PURE__ */ new Map(), $ = (Q) => {
      for (const z of Q)
        z.type === "group" && (T.set(z.id, z.expanded), $(z.children));
    };
    return $(c), T;
  }), p = he((T) => {
    m(($) => {
      const Q = new Map($), z = Q.get(T) ?? !1;
      return Q.set(T, !z), n("toggleGroup", { itemId: T, expanded: !z }), Q;
    });
  }, [n]), f = he((T) => {
    T !== s && n("selectItem", { itemId: T });
  }, [n, s]), _ = he((T) => {
    n("executeCommand", { itemId: T });
  }, [n]), b = he(() => {
    n("toggleCollapse", {});
  }, [n]), C = he(() => {
    n("toggleDrawer", {});
  }, [n]), [E, v] = Ye(null), g = he((T) => {
    v(T);
  }, []), R = he(() => {
    v(null);
  }, []);
  rt(() => {
    r || v(null);
  }, [r]);
  const [x, D] = Ye(() => {
    const T = at(c, r, o);
    return T.length > 0 ? T[0].id : "";
  }), S = Ge(/* @__PURE__ */ new Map()), h = he((T) => ($) => {
    $ ? S.current.set(T, $) : S.current.delete(T);
  }, []), I = he((T) => {
    D(T);
  }, []), y = Ge(0), N = he((T) => {
    D(T), y.current++;
  }, []);
  rt(() => {
    const T = S.current.get(x);
    T && document.activeElement !== T && T.focus();
  }, [x, y.current]);
  const U = he((T) => {
    if (T.key === "Escape" && E !== null) {
      T.preventDefault(), R();
      return;
    }
    const $ = at(c, r, o);
    if ($.length === 0) return;
    const Q = $.findIndex((O) => O.id === x);
    if (Q < 0) return;
    const z = $[Q];
    switch (T.key) {
      case "ArrowDown": {
        T.preventDefault();
        const O = (Q + 1) % $.length;
        N($[O].id);
        break;
      }
      case "ArrowUp": {
        T.preventDefault();
        const O = (Q - 1 + $.length) % $.length;
        N($[O].id);
        break;
      }
      case "Home": {
        T.preventDefault(), N($[0].id);
        break;
      }
      case "End": {
        T.preventDefault(), N($[$.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        T.preventDefault(), z.type === "nav" ? f(z.id) : z.type === "command" ? _(z.id) : z.type === "group" && (r ? E === z.id ? R() : g(z.id) : p(z.id));
        break;
      }
      case "ArrowRight": {
        z.type === "group" && !r && ((o.get(z.id) ?? !1) || (T.preventDefault(), p(z.id)));
        break;
      }
      case "ArrowLeft": {
        z.type === "group" && !r && (o.get(z.id) ?? !1) && (T.preventDefault(), p(z.id));
        break;
      }
    }
  }, [
    c,
    r,
    o,
    x,
    E,
    N,
    f,
    _,
    p,
    g,
    R
  ]), A = "tlSidebar" + (r ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: A }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(K, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: C, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": a["js.sidebar.ariaLabel"] }, r ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: U }, c.map((T) => /* @__PURE__ */ e.createElement(
    It,
    {
      key: T.id,
      item: T,
      activeItemId: s,
      collapsed: r,
      onSelect: f,
      onExecute: _,
      onToggleGroup: p,
      focusedId: x,
      setItemRef: h,
      onItemFocus: I,
      groupStates: o,
      flyoutGroupId: E,
      onOpenFlyout: g,
      onCloseFlyout: R
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
}, zn = ({ controlId: l }) => {
  const t = q(), n = t.direction ?? "column", a = t.gap ?? "default", c = t.align ?? "stretch", s = t.wrap === !0, i = t.growFirst === !0, u = t.children ?? [], r = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${a}`,
    `tlStack--align-${c}`,
    s ? "tlStack--wrap" : "",
    i ? "tlStack--grow-first" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: r }, u.map((o, m) => /* @__PURE__ */ e.createElement(K, { key: m, control: o })));
}, Vn = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlInset" }, t.child && /* @__PURE__ */ e.createElement(K, { control: t.child }));
}, Kn = ({ controlId: l }) => {
  const t = q(), n = t.columns, a = t.minColumnWidth, c = t.gap ?? "default", s = t.children ?? [], i = {};
  return a ? i.gridTemplateColumns = `repeat(auto-fit, minmax(min(${a}, 100%), 1fr))` : n && (i.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${c}`, style: i }, s.map((u, r) => /* @__PURE__ */ e.createElement(K, { key: r, control: u })));
}, Yn = ({ controlId: l }) => {
  const t = q(), n = t.title, a = t.variant ?? "outlined", c = t.padding ?? "default", s = t.headerActions ?? [], i = t.child, u = n != null || s.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${a}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, s.map((r, o) => /* @__PURE__ */ e.createElement(K, { key: o, control: r })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${c}` }, /* @__PURE__ */ e.createElement(K, { control: i })));
}, Gn = ({ controlId: l }) => {
  const t = q(), n = t.title ?? "", a = t.leading, c = t.children ?? [], s = t.actions ?? [], i = t.variant ?? "flat", r = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    i === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: r }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(K, { control: a })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, c.map((o, m) => /* @__PURE__ */ e.createElement(K, { key: m, control: o }))), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((o, m) => /* @__PURE__ */ e.createElement(K, { key: m, control: o }))));
}, { useCallback: Xn } = e, qn = ({ controlId: l }) => {
  const t = q(), n = le(), a = t.items ?? [], c = Xn((s) => {
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
}, { useCallback: Zn } = e, Qn = ({ controlId: l }) => {
  const t = q(), n = le(), a = t.items ?? [], c = t.activeItemId, s = Zn((i) => {
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
}, { useCallback: gt, useRef: Jn } = e, el = ({ onClose: l }) => (ue("ESCAPE", () => (l(), !0)), null), tl = ({ controlId: l }) => {
  const t = q(), n = le(), a = t.open === !0, c = t.closeOnBackdrop !== !1, s = t.child, i = Jn(null), u = gt(() => {
    n("close");
  }, [n]), r = gt((o) => {
    c && o.target === o.currentTarget && u();
  }, [c, u]);
  return a ? /* @__PURE__ */ e.createElement(ct, null, /* @__PURE__ */ e.createElement(el, { onClose: u }), /* @__PURE__ */ e.createElement(
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
}, { useEffect: nl, useRef: ll } = e, rl = ({ controlId: l }) => {
  const n = q().dialogs ?? [], a = ll(n.length);
  return nl(() => {
    n.length < a.current && n.length > 0, a.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((c) => /* @__PURE__ */ e.createElement(K, { key: c.controlId, control: c })));
}, { useCallback: He, useRef: xe, useState: We } = e, al = ({ onClose: l }) => (ue("ESCAPE", () => (l(), !0)), null), ol = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, sl = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], cl = ({ controlId: l }) => {
  const t = q(), n = le(), a = ce(ol), c = t.title ?? "", s = t.width ?? "32rem", i = t.height ?? null, u = t.minHeight ?? null, r = t.resizable === !0, o = t.child, m = t.actions ?? [], p = t.toolbar, f = t.buttonBar, [_, b] = We(null), [C, E] = We(null), [v, g] = We(null), R = xe(null), [x, D] = We(!1), S = xe(null), h = xe(null), I = xe(null), y = xe(null), N = xe(null), U = He(() => {
    n("close");
  }, [n]);
  it(!0, y, "field");
  const A = He((O, B) => {
    B.preventDefault();
    const M = y.current;
    if (!M) return;
    const Y = M.getBoundingClientRect(), d = !R.current, k = R.current ?? { x: Y.left, y: Y.top };
    d && (R.current = k, g(k)), N.current = {
      dir: O,
      startX: B.clientX,
      startY: B.clientY,
      startW: Y.width,
      startH: Y.height,
      startPos: { ...k },
      symmetric: d
    };
    const H = (X) => {
      const j = N.current;
      if (!j) return;
      const J = X.clientX - j.startX, re = X.clientY - j.startY;
      let ne = j.startW, pe = j.startH, be = 0, _e = 0;
      j.symmetric ? (j.dir.includes("e") && (ne = j.startW + 2 * J), j.dir.includes("w") && (ne = j.startW - 2 * J), j.dir.includes("s") && (pe = j.startH + 2 * re), j.dir.includes("n") && (pe = j.startH - 2 * re)) : (j.dir.includes("e") && (ne = j.startW + J), j.dir.includes("w") && (ne = j.startW - J, be = J), j.dir.includes("s") && (pe = j.startH + re), j.dir.includes("n") && (pe = j.startH - re, _e = re));
      const Ce = Math.max(200, ne), we = Math.max(100, pe);
      j.symmetric ? (be = (j.startW - Ce) / 2, _e = (j.startH - we) / 2) : (j.dir.includes("w") && Ce === 200 && (be = j.startW - 200), j.dir.includes("n") && we === 100 && (_e = j.startH - 100)), h.current = Ce, I.current = we, b(Ce), E(we);
      const Te = {
        x: j.startPos.x + be,
        y: j.startPos.y + _e
      };
      R.current = Te, g(Te);
    }, F = () => {
      document.removeEventListener("mousemove", H), document.removeEventListener("mouseup", F);
      const X = h.current, j = I.current;
      (X != null || j != null) && n("resize", {
        ...X != null ? { width: Math.round(X) } : {},
        ...j != null ? { height: Math.round(j) } : {}
      }), N.current = null;
    };
    document.addEventListener("mousemove", H), document.addEventListener("mouseup", F);
  }, [n]), T = He((O) => {
    if (O.button !== 0 || O.target.closest("button")) return;
    O.preventDefault();
    const B = y.current;
    if (!B) return;
    const M = B.getBoundingClientRect(), Y = R.current ?? { x: M.left, y: M.top }, d = O.clientX - Y.x, k = O.clientY - Y.y, H = (X) => {
      const j = window.innerWidth, J = window.innerHeight;
      let re = X.clientX - d, ne = X.clientY - k;
      const pe = B.offsetWidth, be = B.offsetHeight;
      re + pe > j && (re = j - pe), ne + be > J && (ne = J - be), re < 0 && (re = 0), ne < 0 && (ne = 0);
      const _e = { x: re, y: ne };
      R.current = _e, g(_e);
    }, F = () => {
      document.removeEventListener("mousemove", H), document.removeEventListener("mouseup", F);
    };
    document.addEventListener("mousemove", H), document.addEventListener("mouseup", F);
  }, []), $ = He(() => {
    var O, B;
    if (x) {
      const M = S.current;
      M && (g(M.x !== -1 ? { x: M.x, y: M.y } : null), b(M.w), E(M.h)), D(!1);
    } else {
      const M = y.current, Y = M == null ? void 0 : M.getBoundingClientRect();
      S.current = {
        x: ((O = R.current) == null ? void 0 : O.x) ?? (Y == null ? void 0 : Y.left) ?? -1,
        y: ((B = R.current) == null ? void 0 : B.y) ?? (Y == null ? void 0 : Y.top) ?? -1,
        w: _ ?? (Y == null ? void 0 : Y.width) ?? null,
        h: C ?? null
      }, D(!0), g({ x: 0, y: 0 }), b(null), E(null);
    }
  }, [x, _, C]), Q = x ? { position: "absolute", top: 0, left: 0, width: "100vw", maxWidth: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: _ != null ? _ + "px" : s,
    ...C != null ? { height: C + "px" } : i != null ? { height: i } : {},
    ...u != null && C == null ? { minHeight: u } : {},
    maxHeight: v ? "100vh" : "80vh",
    ...v ? { position: "absolute", left: v.x + "px", top: v.y + "px" } : {}
  }, z = l + "-title";
  return /* @__PURE__ */ e.createElement(ct, { modal: !0 }, /* @__PURE__ */ e.createElement(al, { onClose: U }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: Q,
      ref: y,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": z
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${x ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: x ? void 0 : T,
        onDoubleClick: r ? $ : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: z }, c),
      p && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(K, { control: p })),
      r && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: $,
          title: x ? a["js.window.restore"] : a["js.window.maximize"]
        },
        x ? (
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
          onClick: U,
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
    (m.length > 0 || f) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, f && /* @__PURE__ */ e.createElement(K, { control: f }), m.map((O, B) => /* @__PURE__ */ e.createElement(K, { key: B, control: O }))),
    r && !x && sl.map((O) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: O,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${O}`,
        onMouseDown: (B) => A(O, B)
      }
    ))
  ));
}, { useCallback: il } = e, ul = {
  "js.drawer.close": "Close"
}, dl = ({ controlId: l }) => {
  const t = q(), n = le(), a = ce(ul), c = t.open === !0, s = t.position ?? "right", i = t.size ?? "medium", u = t.title ?? null, r = t.child, o = il(() => {
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, r && /* @__PURE__ */ e.createElement(K, { control: r })));
}, { useCallback: ml } = e, pl = ({ controlId: l }) => {
  const t = q(), n = le(), a = t.child, c = ml((s) => {
    s.preventDefault(), s.stopPropagation(), n("openContextMenu", { x: s.clientX, y: s.clientY });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tl-context-menu-region", onContextMenu: c }, a && /* @__PURE__ */ e.createElement(K, { control: a }));
}, { useCallback: fl, useEffect: hl, useState: bl } = e, _l = ({ controlId: l }) => {
  const t = q(), n = le(), a = t.message ?? "", c = t.content ?? "", s = t.variant ?? "info", i = t.duration ?? 5e3, u = t.visible === !0, r = t.generation ?? 0, [o, m] = bl(!1), p = fl(() => {
    m(!0), setTimeout(() => {
      n("dismiss", { generation: r }), m(!1);
    }, 200);
  }, [n, r]);
  return hl(() => {
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
}, { useCallback: Ze, useEffect: vt, useRef: gl, useState: Et } = e, vl = ({ controlId: l }) => {
  const t = q(), n = le(), a = t.open === !0, c = t.anchorId, s = t.anchorX, i = t.anchorY, u = t.items ?? [], r = gl(null), [o, m] = Et({ top: 0, left: 0 }), [p, f] = Et(0), _ = u.filter((v) => v.type === "item" && !v.disabled);
  vt(() => {
    var h, I;
    if (!a) return;
    const v = ((h = r.current) == null ? void 0 : h.offsetHeight) ?? 200, g = ((I = r.current) == null ? void 0 : I.offsetWidth) ?? 200;
    if (s != null && i != null) {
      let y = i, N = s;
      y + v > window.innerHeight && (y = Math.max(0, window.innerHeight - v)), N + g > window.innerWidth && (N = Math.max(0, window.innerWidth - g)), m({ top: y, left: N }), f(0);
      return;
    }
    if (!c) return;
    const R = document.getElementById(c);
    if (!R) return;
    const x = R.getBoundingClientRect();
    let D = x.bottom + 4, S = x.left;
    D + v > window.innerHeight && (D = x.top - v - 4), S + g > window.innerWidth && (S = x.right - g), m({ top: D, left: S }), f(0);
  }, [a, c, s, i]);
  const b = Ze(() => {
    n("close");
  }, [n]), C = Ze((v) => {
    n("selectItem", { itemId: v });
  }, [n]);
  vt(() => {
    if (!a) return;
    const v = (g) => {
      r.current && !r.current.contains(g.target) && b();
    };
    return document.addEventListener("mousedown", v), () => document.removeEventListener("mousedown", v);
  }, [a, b]);
  const E = Ze((v) => {
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
      g && C(g.id);
    }
  }, [b, C, _, p]);
  return it(a, r), a ? /* @__PURE__ */ e.createElement(
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
    u.map((v, g) => {
      if (v.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: g, className: "tlMenu__separator" });
      const x = _.indexOf(v) === p;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: v.id,
          type: "button",
          className: "tlMenu__item" + (x ? " tlMenu__item--focused" : "") + (v.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: v.disabled,
          tabIndex: x ? 0 : -1,
          onClick: () => C(v.id)
        },
        v.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + v.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, v.label)
      );
    })
  ) : null;
}, El = 768, Cl = ({ controlId: l }) => {
  const t = q(), n = le();
  e.useEffect(() => {
    const o = window.matchMedia(`(max-width: ${El}px)`), m = (f) => {
      n("reportDisplayClass", { displayClass: f ? "COMPACT" : "REGULAR" });
    };
    m(o.matches);
    const p = (f) => m(f.matches);
    return o.addEventListener("change", p), () => o.removeEventListener("change", p);
  }, [n]);
  const a = t.header, c = t.content, s = t.footer, i = t.snackbar, u = t.dialogManager, r = t.menuOverlay;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(K, { control: a })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(K, { control: c })), s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(K, { control: s })), /* @__PURE__ */ e.createElement(K, { control: i }), u && /* @__PURE__ */ e.createElement(K, { control: u }), r && /* @__PURE__ */ e.createElement(K, { control: r }));
}, wl = ({ controlId: l }) => {
  const t = q(), n = t.text ?? "", a = t.cssClass ?? "", c = t.hasTooltip === !0, s = a ? `tlText ${a}` : "tlText";
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: s,
      "data-tooltip": c ? "key:tooltip" : void 0
    },
    n
  );
}, yl = ({ isMulti: l, cursorIndex: t, onMove: n, onToggle: a, onSelectAll: c }) => (ue("ArrowUp", () => (n("up", !1, !1), !0)), ue("ArrowDown", () => (n("down", !1, !1), !0)), ue("Home", () => (n("home", !1, !1), !0)), ue("End", () => (n("end", !1, !1), !0)), ue("PageUp", () => (n("pageUp", !1, !1), !0)), ue("PageDown", () => (n("pageDown", !1, !1), !0)), ue("Shift+ArrowUp", () => (n("up", l, !1), !0)), ue("Shift+ArrowDown", () => (n("down", l, !1), !0)), ue("Shift+Home", () => (n("home", l, !1), !0)), ue("Shift+End", () => (n("end", l, !1), !0)), ue("Shift+PageUp", () => (n("pageUp", l, !1), !0)), ue("Shift+PageDown", () => (n("pageDown", l, !1), !0)), ue("Ctrl+ArrowUp", () => (n("up", !1, l), !0)), ue("Ctrl+ArrowDown", () => (n("down", !1, l), !0)), ue("Space", () => t < 0 ? !1 : (a(), !0)), ue("Ctrl+A", () => l ? (c(), !0) : !1), null), kl = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all",
  "js.table.filter": "Filter"
}, Ct = 50, Sl = ({ controlId: l }) => {
  const t = q(), n = le(), a = ce(kl), c = e.useRef(null);
  e.useEffect(() => {
    const w = c.current;
    if (!w) return;
    const L = (G) => {
      const ee = G.detail;
      let ae = ee.target;
      for (; ae && ae !== w; ) {
        const fe = ae.dataset.row, se = ae.dataset.col;
        if (fe != null && se != null) {
          ee.resolved = { key: fe + "|" + se };
          return;
        }
        ae = ae.parentElement;
      }
    };
    return w.addEventListener("tl-tooltip-resolve", L), () => w.removeEventListener("tl-tooltip-resolve", L);
  }, []);
  const s = t.columns ?? [], i = t.totalRowCount ?? 0, u = t.rows ?? [], r = t.rowHeight ?? 36, o = t.selectionMode ?? "single", m = t.selectedCount ?? 0, p = t.cursorIndex ?? -1, f = t.frozenColumnCount ?? 0, _ = t.treeMode ?? !1, b = e.useMemo(
    () => s.filter((w) => w.sortPriority && w.sortPriority > 0).length,
    [s]
  ), C = o === "multi", E = 40, v = 20, g = e.useRef(null), R = e.useRef(null), x = e.useRef(null), [D, S] = e.useState({}), h = e.useRef(null), I = e.useRef(!1), y = e.useRef(null), [N, U] = e.useState(null), [A, T] = e.useState(null);
  e.useEffect(() => {
    h.current || S({});
  }, [s]);
  const $ = e.useCallback((w) => D[w.name] ?? w.width, [D]), Q = e.useMemo(() => {
    const w = [];
    let L = C && f > 0 ? E : 0;
    for (let G = 0; G < f && G < s.length; G++)
      w.push(L), L += $(s[G]);
    return w;
  }, [s, f, C, E, $]), z = i * r, O = e.useRef(null), B = e.useCallback((w, L, G) => {
    G.preventDefault(), G.stopPropagation(), h.current = { column: w, startX: G.clientX, startWidth: L };
    let ee = G.clientX, ae = 0;
    const fe = () => {
      const ie = h.current;
      if (!ie) return;
      const ge = Math.max(Ct, ie.startWidth + (ee - ie.startX) + ae);
      S((De) => ({ ...De, [ie.column]: ge }));
    }, se = () => {
      const ie = R.current, ge = g.current;
      if (!ie || !h.current) return;
      const De = ie.getBoundingClientRect(), dt = 40, mt = 8, $t = ie.scrollLeft;
      ee > De.right - dt ? ie.scrollLeft += mt : ee < De.left + dt && (ie.scrollLeft = Math.max(0, ie.scrollLeft - mt));
      const pt = ie.scrollLeft - $t;
      pt !== 0 && (ge && (ge.scrollLeft = ie.scrollLeft), ae += pt, fe()), O.current = requestAnimationFrame(se);
    };
    O.current = requestAnimationFrame(se);
    const Re = (ie) => {
      ee = ie.clientX, fe();
    }, Fe = (ie) => {
      document.removeEventListener("mousemove", Re), document.removeEventListener("mouseup", Fe), O.current !== null && (cancelAnimationFrame(O.current), O.current = null);
      const ge = h.current;
      if (ge) {
        const De = Math.max(Ct, ge.startWidth + (ie.clientX - ge.startX) + ae);
        n("columnResize", { column: ge.column, width: De }), h.current = null, I.current = !0, requestAnimationFrame(() => {
          I.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", Re), document.addEventListener("mouseup", Fe);
  }, [n]), M = e.useCallback(() => {
    g.current && R.current && (g.current.scrollLeft = R.current.scrollLeft), x.current !== null && clearTimeout(x.current), x.current = window.setTimeout(() => {
      const w = R.current;
      if (!w) return;
      const L = w.scrollTop, G = Math.ceil(w.clientHeight / r), ee = Math.floor(L / r);
      n("scroll", { start: ee, count: G });
    }, 80);
  }, [n, r]), Y = e.useCallback((w, L, G) => {
    if (I.current) return;
    let ee;
    !L || L === "desc" ? ee = "asc" : ee = "desc";
    const ae = G.shiftKey ? "add" : "replace";
    n("sort", { column: w, direction: ee, mode: ae });
  }, [n]), d = e.useCallback((w, L) => {
    y.current = w, L.dataTransfer.effectAllowed = "move", L.dataTransfer.setData("text/plain", w);
  }, []), k = e.useCallback((w, L) => {
    if (!y.current || y.current === w) {
      U(null);
      return;
    }
    L.preventDefault(), L.dataTransfer.dropEffect = "move";
    const G = L.currentTarget.getBoundingClientRect(), ee = L.clientX < G.left + G.width / 2 ? "left" : "right";
    U({ column: w, side: ee });
  }, []), H = e.useCallback((w) => {
    w.preventDefault(), w.stopPropagation();
    const L = y.current;
    if (!L || !N) {
      y.current = null, U(null);
      return;
    }
    let G = s.findIndex((ae) => ae.name === N.column);
    if (G < 0) {
      y.current = null, U(null);
      return;
    }
    const ee = s.findIndex((ae) => ae.name === L);
    N.side === "right" && G++, ee < G && G--, n("columnReorder", { column: L, targetIndex: G }), y.current = null, U(null);
  }, [s, N, n]), F = e.useCallback(() => {
    y.current = null, U(null);
  }, []), X = e.useCallback((w, L) => {
    var ee;
    const G = window.getSelection();
    G && !G.isCollapsed && L.currentTarget.contains(G.anchorNode) || ((ee = R.current) == null || ee.focus({ preventScroll: !0 }), n("select", {
      rowIndex: w,
      ctrlKey: L.ctrlKey || L.metaKey,
      shiftKey: L.shiftKey
    }));
  }, [n]), j = e.useCallback((w, L, G) => {
    n("moveSelection", { direction: w, extend: L, move: G });
  }, [n]), J = e.useCallback(() => {
    p < 0 || n("select", { rowIndex: p, ctrlKey: C, shiftKey: !1 });
  }, [n, p, C]), re = e.useCallback(() => {
    n("selectAll", { selected: !0 });
  }, [n]), ne = e.useCallback(
    () => !!c.current && c.current.contains(document.activeElement),
    []
  );
  e.useEffect(() => {
    if (p < 0)
      return;
    const w = R.current;
    if (!w)
      return;
    const L = p * r, G = L + r;
    L < w.scrollTop ? w.scrollTop = L : G > w.scrollTop + w.clientHeight && (w.scrollTop = G - w.clientHeight);
  }, [p, r]);
  const pe = e.useCallback((w, L) => {
    L.stopPropagation(), n("select", { rowIndex: w, ctrlKey: !0, shiftKey: !1 });
  }, [n]), be = e.useCallback(() => {
    const w = m === i && i > 0;
    n("selectAll", { selected: !w });
  }, [n, m, i]), _e = e.useCallback((w, L, G) => {
    G.stopPropagation(), n("expand", { rowIndex: w, expanded: L });
  }, [n]), Ce = e.useCallback((w, L) => {
    L.preventDefault(), T({ x: L.clientX, y: L.clientY, colIdx: w });
  }, []), we = e.useCallback(() => {
    A && (n("setFrozenColumnCount", { count: A.colIdx + 1 }), T(null));
  }, [A, n]), Te = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), T(null);
  }, [n]);
  e.useEffect(() => {
    if (!A) return;
    const w = () => T(null);
    return document.addEventListener("mousedown", w), () => document.removeEventListener("mousedown", w);
  }, [A]), Ne(!!A, { ESCAPE: () => T(null) });
  const $e = e.useCallback((w, L) => {
    L.stopPropagation(), L.preventDefault(), n("openFilter", { column: w });
  }, [n]), P = s.reduce((w, L) => w + $(L), 0) + (C ? E : 0), V = m === i && i > 0, te = m > 0 && m < i, oe = e.useCallback((w) => {
    w && (w.indeterminate = te);
  }, [te]);
  return /* @__PURE__ */ e.createElement(ct, { active: ne }, /* @__PURE__ */ e.createElement(
    yl,
    {
      isMulti: C,
      cursorIndex: p,
      onMove: j,
      onToggle: J,
      onSelectAll: re
    }
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: c,
      id: l,
      className: "tlTableView",
      "data-tooltip": "dynamic",
      onDragOver: (w) => {
        if (!y.current) return;
        w.preventDefault();
        const L = R.current, G = g.current;
        if (!L) return;
        const ee = L.getBoundingClientRect(), ae = 40, fe = 8;
        w.clientX < ee.left + ae ? L.scrollLeft = Math.max(0, L.scrollLeft - fe) : w.clientX > ee.right - ae && (L.scrollLeft += fe), G && (G.scrollLeft = L.scrollLeft);
      },
      onDrop: H
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: g }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { width: P } }, C && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (f > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: E,
          minWidth: E,
          ...f > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (w) => {
          y.current && (w.preventDefault(), w.dataTransfer.dropEffect = "move", s.length > 0 && s[0].name !== y.current && U({ column: s[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: oe,
          className: "tlTableView__checkbox",
          checked: V,
          onChange: be
        }
      )
    ), s.map((w, L) => {
      const G = $(w);
      s.length - 1;
      let ee = "tlTableView__headerCell";
      w.sortable && (ee += " tlTableView__headerCell--sortable"), N && N.column === w.name && (ee += " tlTableView__headerCell--dragOver-" + N.side);
      const ae = L < f, fe = L === f - 1;
      return ae && (ee += " tlTableView__headerCell--frozen"), fe && (ee += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: w.name,
          className: ee,
          style: {
            width: G,
            minWidth: G,
            position: ae ? "sticky" : "relative",
            ...ae ? { left: Q[L], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: w.sortable ? (se) => Y(w.name, w.sortDirection, se) : void 0,
          onContextMenu: (se) => Ce(L, se),
          onDragStart: (se) => d(w.name, se),
          onDragOver: (se) => k(w.name, se),
          onDrop: H,
          onDragEnd: F
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, w.label),
        w.filterable && /* @__PURE__ */ e.createElement(
          "button",
          {
            type: "button",
            className: "tlTableView__filterButton" + (w.filterActive ? " tlTableView__filterButton--active" : ""),
            title: a["js.table.filter"],
            style: {
              border: "none",
              background: "transparent",
              cursor: "pointer",
              padding: "0 4px",
              color: w.filterActive ? "#1565c0" : "inherit"
            },
            onMouseDown: (se) => se.stopPropagation(),
            onClick: (se) => $e(w.name, se)
          },
          /* @__PURE__ */ e.createElement("i", { className: w.filterActive ? "bi bi-funnel-fill" : "bi bi-funnel" })
        ),
        w.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, w.sortDirection === "asc" ? "▲" : "▼", b > 1 && w.sortPriority != null && w.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, w.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (se) => B(w.name, G, se)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (w) => {
          if (y.current && s.length > 0) {
            const L = s[s.length - 1];
            L.name !== y.current && (w.preventDefault(), w.dataTransfer.dropEffect = "move", U({ column: L.name, side: "right" }));
          }
        },
        onDrop: H
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: R,
        className: "tlTableView__body",
        onScroll: M,
        tabIndex: 0
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: z, position: "relative", width: P } }, u.map((w) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: w.id,
          className: "tlTableView__row" + (w.selected ? " tlTableView__row--selected" : "") + (w.index === p ? " tlTableView__row--cursor" : ""),
          style: {
            position: "absolute",
            top: w.index * r,
            height: r,
            width: P,
            ...w.index === p ? { outline: "2px solid var(--color-primary, #1a73e8)", outlineOffset: "-2px" } : {}
          },
          onMouseDown: (L) => {
            (L.shiftKey || L.ctrlKey || L.metaKey || L.detail > 1) && L.preventDefault();
          },
          onClick: (L) => X(w.index, L)
        },
        C && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (f > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: E,
              minWidth: E,
              ...f > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (L) => L.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: w.selected,
              onChange: () => {
              },
              onClick: (L) => pe(w.index, L),
              tabIndex: -1
            }
          )
        ),
        s.map((L, G) => {
          const ee = $(L), ae = G === s.length - 1, fe = G < f, se = G === f - 1;
          let Re = "tlTableView__cell";
          fe && (Re += " tlTableView__cell--frozen"), se && (Re += " tlTableView__cell--frozenLast");
          const Fe = _ && G === 0, ie = w.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: L.name,
              className: Re,
              "data-row": w.id,
              "data-col": L.name,
              style: {
                ...ae && !fe ? { flex: "1 0 auto", minWidth: ee } : { width: ee, minWidth: ee },
                ...fe ? { position: "sticky", left: Q[G], zIndex: 2 } : {}
              }
            },
            Fe ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ie * v } }, w.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (ge) => _e(w.index, !w.expanded, ge)
              },
              w.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(K, { control: w.cells[L.name] })) : /* @__PURE__ */ e.createElement(K, { control: w.cells[L.name] })
          );
        })
      )))
    ),
    A && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: A.y, left: A.x, zIndex: 1e4 },
        onMouseDown: (w) => w.stopPropagation()
      },
      A.colIdx + 1 !== f && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: we }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      f > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Te }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
    )
  ));
}, Nl = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, Pt = e.createContext(Nl), { useMemo: Tl, useRef: Rl, useState: Dl, useEffect: xl } = e, Ll = 320, Il = ({ controlId: l }) => {
  const t = q(), n = t.maxColumns ?? 3, a = t.labelPosition ?? "auto", c = t.readOnly === !0, s = t.children ?? [], i = t.noModelMessage, u = Rl(null), [r, o] = Dl(
    a === "top" ? "top" : "side"
  );
  xl(() => {
    if (a !== "auto") {
      o(a);
      return;
    }
    const b = u.current;
    if (!b) return;
    const C = new ResizeObserver((E) => {
      for (const v of E) {
        const R = v.contentRect.width / n;
        o(R < Ll ? "top" : "side");
      }
    });
    return C.observe(b), () => C.disconnect();
  }, [a, n]);
  const m = Tl(() => ({
    readOnly: c,
    resolvedLabelPosition: r
  }), [c, r]), f = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, _ = [
    "tlFormLayout",
    c ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return i ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, i)) : /* @__PURE__ */ e.createElement(Pt.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: l, className: _, style: f, ref: u }, s.map((b, C) => /* @__PURE__ */ e.createElement(K, { key: C, control: b }))));
}, { useCallback: Pl } = e, Ml = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, jl = ({ controlId: l }) => {
  const t = q(), n = le(), a = ce(Ml), c = t.headerControl ?? null, s = t.headerActions ?? [], i = t.collapsible === !0, u = t.collapsed === !0, r = t.border ?? "none", o = t.fullLine === !0, m = t.children ?? [], p = c != null || s.length > 0 || i, f = Pl(() => {
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
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(K, { control: c })), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, s.map((b, C) => /* @__PURE__ */ e.createElement(K, { key: C, control: b })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((b, C) => /* @__PURE__ */ e.createElement(K, { key: C, control: b }))));
}, { useContext: Bl, useState: Al, useCallback: Ol } = e, $l = ({ controlId: l }) => {
  const t = q(), n = Bl(Pt), a = t.label ?? "", c = t.required === !0, s = t.error, i = t.warnings, u = t.helpText, r = t.dirty === !0, o = t.labelPosition ?? n.resolvedLabelPosition, m = t.fullLine === !0, p = t.visible !== !1, f = t.hasTooltip === !0, _ = t.field, b = n.readOnly, [C, E] = Al(!1), v = Ol(() => E((D) => !D), []);
  if (!p) return null;
  const g = s != null, R = i != null && i.length > 0, x = [
    "tlFormField",
    `tlFormField--${o}`,
    b ? "tlFormField--readonly" : "",
    m ? "tlFormField--fullLine" : "",
    g ? "tlFormField--error" : "",
    !g && R ? "tlFormField--warning" : "",
    r ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: x }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": f ? "key:tooltip" : void 0
    },
    a
  ), c && !b && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), r && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), u && !b && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(K, { control: _ })), !b && g && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, s)), !b && !g && R && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, i.map((D, S) => /* @__PURE__ */ e.createElement("div", { key: S, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, D)))), !b && u && C && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, u));
}, Fl = ({ controlId: l }) => {
  const t = q(), n = le(), a = t.iconCss, c = t.iconSrc, s = t.label, i = t.cssClass, u = t.hasTooltip === !0, r = t.hasLink, o = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : c ? /* @__PURE__ */ e.createElement("img", { src: c, className: "tlTypeIcon", alt: "" }) : null, m = /* @__PURE__ */ e.createElement(e.Fragment, null, o, s && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, s)), p = e.useCallback((b) => {
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
}, Ul = 20, Hl = () => {
  var S;
  const l = q(), t = le(), n = l.nodes ?? [], a = l.selectionMode ?? "single", c = l.dragEnabled ?? !1, s = l.dropEnabled ?? !1, i = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [r, o] = e.useState(-1), m = e.useRef(null), p = ((S = n.find((h) => h.selected)) == null ? void 0 : S.id) ?? null;
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
    var N;
    const y = window.getSelection();
    y && !y.isCollapsed && I.currentTarget.contains(y.anchorNode) || ((N = m.current) == null || N.focus({ preventScroll: !0 }), t("select", {
      nodeId: h,
      ctrlKey: I.ctrlKey || I.metaKey,
      shiftKey: I.shiftKey
    }));
  }, [t]), b = e.useCallback((h, I) => {
    I.preventDefault(), t("contextMenu", { nodeId: h, x: I.clientX, y: I.clientY });
  }, [t]), C = e.useRef(null), E = e.useCallback((h, I) => {
    const y = I.getBoundingClientRect(), N = h.clientY - y.top, U = y.height / 3;
    return N < U ? "above" : N > U * 2 ? "below" : "within";
  }, []), v = e.useCallback((h, I) => {
    I.dataTransfer.effectAllowed = "move", I.dataTransfer.setData("text/plain", h);
  }, []), g = e.useCallback((h, I) => {
    I.preventDefault(), I.dataTransfer.dropEffect = "move";
    const y = E(I, I.currentTarget);
    C.current != null && window.clearTimeout(C.current), C.current = window.setTimeout(() => {
      t("dragOver", { nodeId: h, position: y }), C.current = null;
    }, 50);
  }, [t, E]), R = e.useCallback((h, I) => {
    I.preventDefault(), C.current != null && (window.clearTimeout(C.current), C.current = null);
    const y = E(I, I.currentTarget);
    t("drop", { nodeId: h, position: y });
  }, [t, E]), x = e.useCallback(() => {
    C.current != null && (window.clearTimeout(C.current), C.current = null), t("dragEnd");
  }, [t]), D = e.useCallback((h) => {
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
          const y = n[r];
          if (y.expandable && !y.expanded) {
            t("expand", { nodeId: y.id });
            return;
          } else y.expanded && (I = r + 1);
        }
        break;
      case "ArrowLeft":
        if (h.preventDefault(), r >= 0 && r < n.length) {
          const y = n[r];
          if (y.expanded) {
            t("collapse", { nodeId: y.id });
            return;
          } else {
            const N = y.depth;
            for (let U = r - 1; U >= 0; U--)
              if (n[U].depth < N) {
                I = U;
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
      onKeyDown: D
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
        style: { paddingLeft: h.depth * Ul },
        draggable: c,
        onMouseDown: (y) => {
          (y.shiftKey || y.ctrlKey || y.metaKey || y.detail > 1) && y.preventDefault();
        },
        onClick: (y) => _(h.id, y),
        onContextMenu: (y) => b(h.id, y),
        onDragStart: (y) => v(h.id, y),
        onDragOver: s ? (y) => g(h.id, y) : void 0,
        onDrop: s ? (y) => R(h.id, y) : void 0,
        onDragEnd: x
      },
      h.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (y) => {
            y.stopPropagation(), f(h.id, h.expanded);
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
var Qe = { exports: {} }, de = {}, Je = { exports: {} }, Z = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var wt;
function Wl() {
  if (wt) return Z;
  wt = 1;
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
  }, C = Object.assign, E = {};
  function v(d, k, H) {
    this.props = d, this.context = k, this.refs = E, this.updater = H || b;
  }
  v.prototype.isReactComponent = {}, v.prototype.setState = function(d, k) {
    if (typeof d != "object" && typeof d != "function" && d != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, d, k, "setState");
  }, v.prototype.forceUpdate = function(d) {
    this.updater.enqueueForceUpdate(this, d, "forceUpdate");
  };
  function g() {
  }
  g.prototype = v.prototype;
  function R(d, k, H) {
    this.props = d, this.context = k, this.refs = E, this.updater = H || b;
  }
  var x = R.prototype = new g();
  x.constructor = R, C(x, v.prototype), x.isPureReactComponent = !0;
  var D = Array.isArray;
  function S() {
  }
  var h = { H: null, A: null, T: null, S: null }, I = Object.prototype.hasOwnProperty;
  function y(d, k, H) {
    var F = H.ref;
    return {
      $$typeof: l,
      type: d,
      key: k,
      ref: F !== void 0 ? F : null,
      props: H
    };
  }
  function N(d, k) {
    return y(d.type, k, d.props);
  }
  function U(d) {
    return typeof d == "object" && d !== null && d.$$typeof === l;
  }
  function A(d) {
    var k = { "=": "=0", ":": "=2" };
    return "$" + d.replace(/[=:]/g, function(H) {
      return k[H];
    });
  }
  var T = /\/+/g;
  function $(d, k) {
    return typeof d == "object" && d !== null && d.key != null ? A("" + d.key) : k.toString(36);
  }
  function Q(d) {
    switch (d.status) {
      case "fulfilled":
        return d.value;
      case "rejected":
        throw d.reason;
      default:
        switch (typeof d.status == "string" ? d.then(S, S) : (d.status = "pending", d.then(
          function(k) {
            d.status === "pending" && (d.status = "fulfilled", d.value = k);
          },
          function(k) {
            d.status === "pending" && (d.status = "rejected", d.reason = k);
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
  function z(d, k, H, F, X) {
    var j = typeof d;
    (j === "undefined" || j === "boolean") && (d = null);
    var J = !1;
    if (d === null) J = !0;
    else
      switch (j) {
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
                k,
                H,
                F,
                X
              );
          }
      }
    if (J)
      return X = X(d), J = F === "" ? "." + $(d, 0) : F, D(X) ? (H = "", J != null && (H = J.replace(T, "$&/") + "/"), z(X, k, H, "", function(pe) {
        return pe;
      })) : X != null && (U(X) && (X = N(
        X,
        H + (X.key == null || d && d.key === X.key ? "" : ("" + X.key).replace(
          T,
          "$&/"
        ) + "/") + J
      )), k.push(X)), 1;
    J = 0;
    var re = F === "" ? "." : F + ":";
    if (D(d))
      for (var ne = 0; ne < d.length; ne++)
        F = d[ne], j = re + $(F, ne), J += z(
          F,
          k,
          H,
          j,
          X
        );
    else if (ne = _(d), typeof ne == "function")
      for (d = ne.call(d), ne = 0; !(F = d.next()).done; )
        F = F.value, j = re + $(F, ne++), J += z(
          F,
          k,
          H,
          j,
          X
        );
    else if (j === "object") {
      if (typeof d.then == "function")
        return z(
          Q(d),
          k,
          H,
          F,
          X
        );
      throw k = String(d), Error(
        "Objects are not valid as a React child (found: " + (k === "[object Object]" ? "object with keys {" + Object.keys(d).join(", ") + "}" : k) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return J;
  }
  function O(d, k, H) {
    if (d == null) return d;
    var F = [], X = 0;
    return z(d, F, "", "", function(j) {
      return k.call(H, j, X++);
    }), F;
  }
  function B(d) {
    if (d._status === -1) {
      var k = d._result;
      k = k(), k.then(
        function(H) {
          (d._status === 0 || d._status === -1) && (d._status = 1, d._result = H);
        },
        function(H) {
          (d._status === 0 || d._status === -1) && (d._status = 2, d._result = H);
        }
      ), d._status === -1 && (d._status = 0, d._result = k);
    }
    if (d._status === 1) return d._result.default;
    throw d._result;
  }
  var M = typeof reportError == "function" ? reportError : function(d) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var k = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof d == "object" && d !== null && typeof d.message == "string" ? String(d.message) : String(d),
        error: d
      });
      if (!window.dispatchEvent(k)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", d);
      return;
    }
    console.error(d);
  }, Y = {
    map: O,
    forEach: function(d, k, H) {
      O(
        d,
        function() {
          k.apply(this, arguments);
        },
        H
      );
    },
    count: function(d) {
      var k = 0;
      return O(d, function() {
        k++;
      }), k;
    },
    toArray: function(d) {
      return O(d, function(k) {
        return k;
      }) || [];
    },
    only: function(d) {
      if (!U(d))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return d;
    }
  };
  return Z.Activity = p, Z.Children = Y, Z.Component = v, Z.Fragment = n, Z.Profiler = c, Z.PureComponent = R, Z.StrictMode = a, Z.Suspense = r, Z.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = h, Z.__COMPILER_RUNTIME = {
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
  }, Z.cloneElement = function(d, k, H) {
    if (d == null)
      throw Error(
        "The argument must be a React element, but you passed " + d + "."
      );
    var F = C({}, d.props), X = d.key;
    if (k != null)
      for (j in k.key !== void 0 && (X = "" + k.key), k)
        !I.call(k, j) || j === "key" || j === "__self" || j === "__source" || j === "ref" && k.ref === void 0 || (F[j] = k[j]);
    var j = arguments.length - 2;
    if (j === 1) F.children = H;
    else if (1 < j) {
      for (var J = Array(j), re = 0; re < j; re++)
        J[re] = arguments[re + 2];
      F.children = J;
    }
    return y(d.type, X, F);
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
  }, Z.createElement = function(d, k, H) {
    var F, X = {}, j = null;
    if (k != null)
      for (F in k.key !== void 0 && (j = "" + k.key), k)
        I.call(k, F) && F !== "key" && F !== "__self" && F !== "__source" && (X[F] = k[F]);
    var J = arguments.length - 2;
    if (J === 1) X.children = H;
    else if (1 < J) {
      for (var re = Array(J), ne = 0; ne < J; ne++)
        re[ne] = arguments[ne + 2];
      X.children = re;
    }
    if (d && d.defaultProps)
      for (F in J = d.defaultProps, J)
        X[F] === void 0 && (X[F] = J[F]);
    return y(d, j, X);
  }, Z.createRef = function() {
    return { current: null };
  }, Z.forwardRef = function(d) {
    return { $$typeof: u, render: d };
  }, Z.isValidElement = U, Z.lazy = function(d) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: d },
      _init: B
    };
  }, Z.memo = function(d, k) {
    return {
      $$typeof: o,
      type: d,
      compare: k === void 0 ? null : k
    };
  }, Z.startTransition = function(d) {
    var k = h.T, H = {};
    h.T = H;
    try {
      var F = d(), X = h.S;
      X !== null && X(H, F), typeof F == "object" && F !== null && typeof F.then == "function" && F.then(S, M);
    } catch (j) {
      M(j);
    } finally {
      k !== null && H.types !== null && (k.types = H.types), h.T = k;
    }
  }, Z.unstable_useCacheRefresh = function() {
    return h.H.useCacheRefresh();
  }, Z.use = function(d) {
    return h.H.use(d);
  }, Z.useActionState = function(d, k, H) {
    return h.H.useActionState(d, k, H);
  }, Z.useCallback = function(d, k) {
    return h.H.useCallback(d, k);
  }, Z.useContext = function(d) {
    return h.H.useContext(d);
  }, Z.useDebugValue = function() {
  }, Z.useDeferredValue = function(d, k) {
    return h.H.useDeferredValue(d, k);
  }, Z.useEffect = function(d, k) {
    return h.H.useEffect(d, k);
  }, Z.useEffectEvent = function(d) {
    return h.H.useEffectEvent(d);
  }, Z.useId = function() {
    return h.H.useId();
  }, Z.useImperativeHandle = function(d, k, H) {
    return h.H.useImperativeHandle(d, k, H);
  }, Z.useInsertionEffect = function(d, k) {
    return h.H.useInsertionEffect(d, k);
  }, Z.useLayoutEffect = function(d, k) {
    return h.H.useLayoutEffect(d, k);
  }, Z.useMemo = function(d, k) {
    return h.H.useMemo(d, k);
  }, Z.useOptimistic = function(d, k) {
    return h.H.useOptimistic(d, k);
  }, Z.useReducer = function(d, k, H) {
    return h.H.useReducer(d, k, H);
  }, Z.useRef = function(d) {
    return h.H.useRef(d);
  }, Z.useState = function(d) {
    return h.H.useState(d);
  }, Z.useSyncExternalStore = function(d, k, H) {
    return h.H.useSyncExternalStore(
      d,
      k,
      H
    );
  }, Z.useTransition = function() {
    return h.H.useTransition();
  }, Z.version = "19.2.4", Z;
}
var yt;
function zl() {
  return yt || (yt = 1, Je.exports = Wl()), Je.exports;
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
var kt;
function Vl() {
  if (kt) return de;
  kt = 1;
  var l = zl();
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
var St;
function Kl() {
  if (St) return Qe.exports;
  St = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), Qe.exports = Vl(), Qe.exports;
}
var Mt = Kl();
const { useState: ye, useCallback: me, useRef: je, useEffect: Le, useMemo: ot } = e;
function ut({ image: l }) {
  if (!l) return null;
  if (l.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function Yl({
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
    /* @__PURE__ */ e.createElement(ut, { image: l.image }),
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
function Gl({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: a,
  onMouseEnter: c,
  id: s
}) {
  const i = me(() => a(l.value), [a, l.value]), u = ot(() => {
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
    /* @__PURE__ */ e.createElement(ut, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const Xl = ({ controlId: l, state: t }) => {
  const n = le(), a = t.value ?? [], c = t.multiSelect === !0, s = t.customOrder === !0, i = t.mandatory === !0, u = t.disabled === !0, r = t.editable !== !1, o = t.optionsLoaded === !0, m = t.options ?? [], p = t.emptyOptionLabel ?? "", f = s && c && !u && r, _ = ce({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), b = _["js.dropdownSelect.nothingFound"], C = me(
    (P) => _["js.dropdownSelect.removeChip"].replace("{0}", P),
    [_]
  ), [E, v] = ye(!1), [g, R] = ye(""), [x, D] = ye(-1), [S, h] = ye(!1), [I, y] = ye({}), [N, U] = ye(null), [A, T] = ye(null), [$, Q] = ye(null), z = je(null), O = je(null), B = je(null), M = je(a);
  M.current = a;
  const Y = je(-1), d = ot(
    () => new Set(a.map((P) => P.value)),
    [a]
  ), k = ot(() => {
    let P = m.filter((V) => !d.has(V.value));
    if (g) {
      const V = g.toLowerCase();
      P = P.filter((te) => te.label.toLowerCase().includes(V));
    }
    return P;
  }, [m, d, g]);
  Le(() => {
    g && k.length === 1 ? D(0) : D(-1);
  }, [k.length, g]), Le(() => {
    E && o && O.current && O.current.focus();
  }, [E, o, a]), Le(() => {
    var te, oe;
    if (Y.current < 0) return;
    const P = Y.current;
    Y.current = -1;
    const V = (te = z.current) == null ? void 0 : te.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    V && V.length > 0 ? V[Math.min(P, V.length - 1)].focus() : (oe = z.current) == null || oe.focus();
  }, [a]), Le(() => {
    if (!E) return;
    const P = (V) => {
      z.current && !z.current.contains(V.target) && B.current && !B.current.contains(V.target) && (v(!1), R(""));
    };
    return document.addEventListener("mousedown", P), () => document.removeEventListener("mousedown", P);
  }, [E]), Le(() => {
    if (!E || !z.current) return;
    const P = z.current.getBoundingClientRect(), V = window.innerHeight - P.bottom, oe = V < 300 && P.top > V;
    y({
      left: P.left,
      width: P.width,
      ...oe ? { bottom: window.innerHeight - P.top } : { top: P.bottom }
    });
  }, [E]);
  const H = me(async () => {
    if (!(u || !r) && (v(!0), R(""), D(-1), h(!1), !o))
      try {
        await n("loadOptions");
      } catch {
        h(!0);
      }
  }, [u, r, o, n]), F = me(() => {
    var P;
    v(!1), R(""), D(-1), (P = z.current) == null || P.focus();
  }, []), X = me(
    (P) => {
      let V;
      if (c) {
        const te = m.find((oe) => oe.value === P);
        if (te)
          V = [...M.current, te];
        else
          return;
      } else {
        const te = m.find((oe) => oe.value === P);
        if (te)
          V = [te];
        else
          return;
      }
      M.current = V, n(Ue, { value: V.map((te) => te.value) }), c ? (R(""), D(-1)) : F();
    },
    [c, m, n, F]
  ), j = me(
    (P) => {
      Y.current = M.current.findIndex((te) => te.value === P);
      const V = M.current.filter((te) => te.value !== P);
      M.current = V, n(Ue, { value: V.map((te) => te.value) });
    },
    [n]
  ), J = me(
    (P) => {
      P.stopPropagation(), n(Ue, { value: [] }), F();
    },
    [n, F]
  ), re = me((P) => {
    R(P.target.value);
  }, []), ne = me(
    (P) => {
      if (!E) {
        if (P.key === "ArrowDown" || P.key === "ArrowUp" || P.key === "Enter" || P.key === " ") {
          if (P.target.tagName === "BUTTON") return;
          P.preventDefault(), P.stopPropagation(), H();
        }
        return;
      }
      switch (P.key) {
        case "ArrowDown":
          P.preventDefault(), P.stopPropagation(), D(
            (V) => V < k.length - 1 ? V + 1 : 0
          );
          break;
        case "ArrowUp":
          P.preventDefault(), P.stopPropagation(), D(
            (V) => V > 0 ? V - 1 : k.length - 1
          );
          break;
        case "Enter":
          P.preventDefault(), P.stopPropagation(), x >= 0 && x < k.length && X(k[x].value);
          break;
        case "Escape":
          P.preventDefault(), P.stopPropagation(), F();
          break;
        case "Tab":
          F();
          break;
        case "Backspace":
          g === "" && c && a.length > 0 && j(a[a.length - 1].value);
          break;
      }
    },
    [
      E,
      H,
      F,
      k,
      x,
      X,
      g,
      c,
      a,
      j
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
    (P, V) => {
      U(P), V.dataTransfer.effectAllowed = "move", V.dataTransfer.setData("text/plain", String(P));
    },
    []
  ), _e = me(
    (P, V) => {
      if (V.preventDefault(), V.dataTransfer.dropEffect = "move", N === null || N === P) {
        T(null), Q(null);
        return;
      }
      const te = V.currentTarget.getBoundingClientRect(), oe = te.left + te.width / 2, w = V.clientX < oe ? "before" : "after";
      T(P), Q(w);
    },
    [N]
  ), Ce = me(
    (P) => {
      if (P.preventDefault(), N === null || A === null || $ === null || N === A) return;
      const V = [...M.current], [te] = V.splice(N, 1);
      let oe = A;
      N < A ? oe = $ === "before" ? oe - 1 : oe : oe = $ === "before" ? oe : oe + 1, V.splice(oe, 0, te), M.current = V, n(Ue, { value: V.map((w) => w.value) }), U(null), T(null), Q(null);
    },
    [N, A, $, n]
  ), we = me(() => {
    U(null), T(null), Q(null);
  }, []);
  if (Le(() => {
    if (x < 0 || !B.current) return;
    const P = B.current.querySelector(
      `[id="${l}-opt-${x}"]`
    );
    P && P.scrollIntoView({ block: "nearest" });
  }, [x, l]), !r)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, a.map((P) => /* @__PURE__ */ e.createElement("span", { key: P.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(ut, { image: P.image }), /* @__PURE__ */ e.createElement("span", null, P.label))));
  const Te = !i && a.length > 0 && !u, $e = E ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: B,
      className: "tlDropdownSelect__dropdown",
      style: I,
      ...Ft
    },
    (o || S) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: O,
        type: "text",
        className: "tlDropdownSelect__search",
        value: g,
        onChange: re,
        onKeyDown: ne,
        placeholder: _["js.dropdownSelect.filterPlaceholder"],
        "aria-label": _["js.dropdownSelect.filterPlaceholder"],
        "aria-activedescendant": x >= 0 ? `${l}-opt-${x}` : void 0,
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
      !o && !S && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      S && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: pe }, _["js.dropdownSelect.error"])),
      o && k.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, b),
      o && k.map((P, V) => /* @__PURE__ */ e.createElement(
        Gl,
        {
          key: P.value,
          id: `${l}-opt-${V}`,
          option: P,
          highlighted: V === x,
          searchTerm: g,
          onSelect: X,
          onMouseEnter: () => D(V)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: z,
      className: "tlDropdownSelect" + (E ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": E,
      "aria-haspopup": "listbox",
      "aria-owns": E ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: E ? void 0 : H,
      onKeyDown: ne
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, a.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : a.map((P, V) => {
      let te = "";
      return N === V ? te = "tlDropdownSelect__chip--dragging" : A === V && $ === "before" ? te = "tlDropdownSelect__chip--dropBefore" : A === V && $ === "after" && (te = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        Yl,
        {
          key: P.value,
          option: P,
          removable: !u && (c || !i),
          onRemove: j,
          removeLabel: C(P.label),
          draggable: f,
          onDragStart: f ? (oe) => be(V, oe) : void 0,
          onDragOver: f ? (oe) => _e(V, oe) : void 0,
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
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, E ? "▲" : "▼"))
  ), $e && Mt.createPortal($e, document.body));
}, { useCallback: et, useRef: ql } = e, jt = "application/x-tl-color", Zl = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: a,
  onSwap: c,
  onReplace: s
}) => {
  const i = ql(null), u = et(
    (m) => (p) => {
      i.current = m, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), r = et((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), o = et(
    (m) => (p) => {
      p.preventDefault();
      const f = p.dataTransfer.getData(jt);
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
function Bt(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function st(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function At(l) {
  if (!st(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function Ot(l, t, n) {
  const a = (c) => Bt(c).toString(16).padStart(2, "0");
  return "#" + a(l) + a(t) + a(n);
}
function Ql(l, t, n) {
  const a = l / 255, c = t / 255, s = n / 255, i = Math.max(a, c, s), u = Math.min(a, c, s), r = i - u;
  let o = 0;
  r !== 0 && (i === a ? o = (c - s) / r % 6 : i === c ? o = (s - a) / r + 2 : o = (a - c) / r + 4, o *= 60, o < 0 && (o += 360));
  const m = i === 0 ? 0 : r / i;
  return [o, m, i];
}
function Jl(l, t, n) {
  const a = n * t, c = a * (1 - Math.abs(l / 60 % 2 - 1)), s = n - a;
  let i = 0, u = 0, r = 0;
  return l < 60 ? (i = a, u = c, r = 0) : l < 120 ? (i = c, u = a, r = 0) : l < 180 ? (i = 0, u = a, r = c) : l < 240 ? (i = 0, u = c, r = a) : l < 300 ? (i = c, u = 0, r = a) : (i = a, u = 0, r = c), [
    Math.round((i + s) * 255),
    Math.round((u + s) * 255),
    Math.round((r + s) * 255)
  ];
}
function er(l) {
  return Ql(...At(l));
}
function tt(l, t, n) {
  return Ot(...Jl(l, t, n));
}
const { useCallback: Ie, useRef: Nt } = e, tr = ({ color: l, onColorChange: t }) => {
  const [n, a, c] = er(l), s = Nt(null), i = Nt(null), u = Ie(
    (b, C) => {
      var R;
      const E = (R = s.current) == null ? void 0 : R.getBoundingClientRect();
      if (!E) return;
      const v = Math.max(0, Math.min(1, (b - E.left) / E.width)), g = Math.max(0, Math.min(1, 1 - (C - E.top) / E.height));
      t(tt(n, v, g));
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
      var g;
      const C = (g = i.current) == null ? void 0 : g.getBoundingClientRect();
      if (!C) return;
      const v = Math.max(0, Math.min(1, (b - C.top) / C.height)) * 360;
      t(tt(v, a, c));
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
  ), _ = tt(n, 1, 1);
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
function nr(l, t) {
  const n = t.toUpperCase();
  return l.some((a) => a != null && a.toUpperCase() === n);
}
const lr = {
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
}, { useState: ze, useCallback: ve, useEffect: Tt, useRef: rr, useLayoutEffect: ar } = e, or = ({
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
  const [o, m] = ze("palette"), [p, f] = ze(t), _ = rr(null), b = ce(lr), [C, E] = ze(null);
  ar(() => {
    if (!l.current || !_.current) return;
    const B = l.current.getBoundingClientRect(), M = _.current.getBoundingClientRect();
    let Y = B.bottom + 4, d = B.left;
    Y + M.height > window.innerHeight && (Y = B.top - M.height - 4), d + M.width > window.innerWidth && (d = Math.max(0, B.right - M.width)), E({ top: Y, left: d });
  }, [l]);
  const v = p != null, [g, R, x] = v ? At(p) : [0, 0, 0], [D, S] = ze((p == null ? void 0 : p.toUpperCase()) ?? "");
  Tt(() => {
    S((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), Ne(!0, { ESCAPE: u }), Tt(() => {
    const B = (Y) => {
      _.current && !_.current.contains(Y.target) && u();
    }, M = setTimeout(() => document.addEventListener("mousedown", B), 0);
    return () => {
      clearTimeout(M), document.removeEventListener("mousedown", B);
    };
  }, [u]);
  const h = ve(
    (B) => (M) => {
      const Y = parseInt(M.target.value, 10);
      if (isNaN(Y)) return;
      const d = Bt(Y);
      f(Ot(B === "r" ? d : g, B === "g" ? d : R, B === "b" ? d : x));
    },
    [g, R, x]
  ), I = ve(
    (B) => {
      if (p != null) {
        B.dataTransfer.setData(jt, p.toUpperCase()), B.dataTransfer.effectAllowed = "move";
        const M = document.createElement("div");
        M.style.width = "33px", M.style.height = "33px", M.style.backgroundColor = p, M.style.borderRadius = "3px", M.style.border = "1px solid rgba(0,0,0,0.1)", M.style.position = "absolute", M.style.top = "-9999px", document.body.appendChild(M), B.dataTransfer.setDragImage(M, 16, 16), requestAnimationFrame(() => document.body.removeChild(M));
      }
    },
    [p]
  ), y = ve((B) => {
    const M = B.target.value;
    S(M), st(M) && f(M);
  }, []), N = ve(() => {
    f(null);
  }, []), U = ve((B) => {
    f(B);
  }, []), A = ve(
    (B) => {
      i(B);
    },
    [i]
  ), T = ve(
    (B, M) => {
      const Y = [...n], d = Y[B];
      Y[B] = Y[M], Y[M] = d, r(Y);
    },
    [n, r]
  ), $ = ve(
    (B, M) => {
      const Y = [...n];
      Y[B] = M, r(Y);
    },
    [n, r]
  ), Q = ve(() => {
    r([...c]);
  }, [c, r]), z = ve(
    (B) => {
      if (nr(n, B)) return;
      const M = n.indexOf(null);
      if (M < 0) return;
      const Y = [...n];
      Y[M] = B.toUpperCase(), r(Y);
    },
    [n, r]
  ), O = ve(() => {
    p != null && z(p), i(p);
  }, [p, i, z]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: _,
      style: C ? { top: C.top, left: C.left, visibility: "visible" } : { visibility: "hidden" }
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
      Zl,
      {
        colors: n,
        columns: a,
        onSelect: U,
        onConfirm: A,
        onSwap: T,
        onReplace: $
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: Q }, b["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(tr, { color: p ?? "#000000", onColorChange: f }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
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
        value: v ? R : "",
        onChange: h("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? x : "",
        onChange: h("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (D !== "" && !st(D) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: D,
        onChange: y
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, s && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: N }, b["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, b["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: O }, b["js.colorInput.ok"]))
  );
}, sr = { "js.colorInput.chooseColor": "Choose color" }, { useState: cr, useCallback: Ve, useRef: ir } = e, ur = ({ controlId: l, state: t }) => {
  const [n, a] = ke(), c = le(), s = ce(sr), [i, u] = cr(!1), r = ir(null), o = n, m = t.editable !== !1, p = t.palette ?? [], f = t.paletteColumns ?? 6, _ = t.defaultPalette ?? p, b = Ve(() => {
    m && u(!0);
  }, [m]), C = Ve(
    (g) => {
      u(!1), a(g);
    },
    [a]
  ), E = Ve(() => {
    u(!1);
  }, []), v = Ve(
    (g) => {
      c("paletteChanged", { palette: g });
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
    or,
    {
      anchorRef: r,
      currentColor: o,
      palette: p,
      paletteColumns: f,
      defaultPalette: _,
      canReset: t.canReset !== !1,
      onConfirm: C,
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
}, { useState: Be, useCallback: Se, useEffect: nt, useRef: Rt, useLayoutEffect: dr, useMemo: mr } = e, pr = {
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
}, fr = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: a,
  onSelect: c,
  onCancel: s,
  onLoadIcons: i
}) => {
  const u = ce(pr), [r, o] = Be("simple"), [m, p] = Be(""), [f, _] = Be(t ?? ""), [b, C] = Be(!1), [E, v] = Be(null), g = Rt(null), R = Rt(null);
  dr(() => {
    if (!l.current || !g.current) return;
    const A = l.current.getBoundingClientRect(), T = g.current.getBoundingClientRect();
    let $ = A.bottom + 4, Q = A.left;
    $ + T.height > window.innerHeight && ($ = A.top - T.height - 4), Q + T.width > window.innerWidth && (Q = Math.max(0, A.right - T.width)), v({ top: $, left: Q });
  }, [l]), nt(() => {
    !a && !b && i().catch(() => C(!0));
  }, [a, b, i]), nt(() => {
    a && R.current && R.current.focus();
  }, [a]), Ne(!0, { ESCAPE: s }), nt(() => {
    const A = ($) => {
      g.current && !g.current.contains($.target) && s();
    }, T = setTimeout(() => document.addEventListener("mousedown", A), 0);
    return () => {
      clearTimeout(T), document.removeEventListener("mousedown", A);
    };
  }, [s]);
  const x = mr(() => {
    if (!m) return n;
    const A = m.toLowerCase();
    return n.filter(
      (T) => T.prefix.toLowerCase().includes(A) || T.label.toLowerCase().includes(A) || T.terms != null && T.terms.some(($) => $.includes(A))
    );
  }, [n, m]), D = Se((A) => {
    p(A.target.value);
  }, []), S = Se(
    (A) => {
      c(A);
    },
    [c]
  ), h = Se((A) => {
    _(A);
  }, []), I = Se((A) => {
    _(A.target.value);
  }, []), y = Se(() => {
    c(f || null);
  }, [f, c]), N = Se(() => {
    c(null);
  }, [c]), U = Se(async (A) => {
    A.preventDefault(), C(!1);
    try {
      await i();
    } catch {
      C(!0);
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
        ref: R,
        type: "text",
        className: "tlIconSelect__search",
        value: m,
        onChange: D,
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
      b && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: U }, u["js.iconSelect.loadError"])),
      a && x.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      a && x.map(
        (A) => A.variants.map((T) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: T.encoded,
            className: "tlIconSelect__iconCell" + (T.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": T.encoded === t,
            tabIndex: 0,
            title: A.label,
            onClick: () => r === "simple" ? S(T.encoded) : h(T.encoded),
            onKeyDown: ($) => {
              ($.key === "Enter" || $.key === " ") && ($.preventDefault(), r === "simple" ? S(T.encoded) : h(T.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(Ee, { encoded: T.encoded })
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
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, f && /* @__PURE__ */ e.createElement(Ee, { encoded: f })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, f ? f.startsWith("css:") ? f.substring(4) : f : ""))),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: s }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: N }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: y }, u["js.iconSelect.ok"]))
  );
}, hr = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: br, useCallback: Ke, useRef: _r } = e, gr = ({ controlId: l, state: t }) => {
  const [n, a] = ke(), c = le(), s = ce(hr), [i, u] = br(!1), r = _r(null), o = n, m = t.editable !== !1, p = t.disabled === !0, f = t.icons ?? [], _ = t.iconsLoaded === !0, b = Ke(() => {
    m && !p && u(!0);
  }, [m, p]), C = Ke(
    (g) => {
      u(!1), a(g);
    },
    [a]
  ), E = Ke(() => {
    u(!1);
  }, []), v = Ke(async () => {
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
    fr,
    {
      anchorRef: r,
      currentValue: o,
      icons: f,
      iconsLoaded: _,
      onSelect: C,
      onCancel: E,
      onLoadIcons: v
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, o ? /* @__PURE__ */ e.createElement(Ee, { encoded: o }) : null));
}, { useCallback: Pe, useEffect: vr, useMemo: Dt, useRef: Er, useState: lt } = e, Cr = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, wr = [1, 2, 3, 4];
function yr(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const a = parseFloat(n[1]), c = n[2] || "px";
  return c === "rem" || c === "em" ? a * t : a;
}
function kr(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let a = 1;
  for (const c of wr)
    n >= c && (a = c);
  return a;
}
function Sr(l, t) {
  const n = Cr[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function Nr(l, t) {
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
        for (let C = f.colEnd; C < _; C++) s(b, C);
      f.colEnd = _;
    }
  };
  for (const p of l) {
    const f = n <= 1 ? 1 : Math.max(1, p.rowSpan || 1);
    let _ = Math.min(Sr(p.width, n), n);
    for (; c(u, r); )
      r++, r >= n && (r = 0, u++);
    let b = 0;
    for (let R = r; R < n && !c(u, R); R++)
      b++;
    if (_ > b) {
      for (o(u), r = 0, u++; c(u, r); )
        r++, r >= n && (r = 0, u++);
      b = 0;
      for (let R = r; R < n && !c(u, R); R++)
        b++;
      _ = Math.min(_, b);
    }
    const C = r, E = r + _, v = u, g = u + f;
    i.push({ id: p.id, colStart: C, colEnd: E, rowStart: v, rowEnd: g });
    for (let R = v; R < g; R++)
      for (let x = C; x < E; x++) s(R, x);
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
const Tr = ({ controlId: l }) => {
  const t = q(), n = le(), a = t.minColWidth ?? "16rem", c = (t.children ?? []).filter((S) => S && S.id), s = Er(null), [i, u] = lt(1), r = t.editMode === !0;
  vr(() => {
    const S = s.current;
    if (!S) return;
    const h = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, I = yr(a, h), y = () => u(kr(S.clientWidth, I));
    y();
    const N = new ResizeObserver(y);
    return N.observe(S), () => N.disconnect();
  }, [a]);
  const o = Dt(() => Nr(c, i), [c, i]), m = Dt(() => {
    const S = {};
    for (const h of o) S[h.id] = h;
    return S;
  }, [o]), [p, f] = lt(null), [_, b] = lt(null), C = Pe((S, h) => {
    if (!r) {
      S.preventDefault();
      return;
    }
    f(h), S.dataTransfer.effectAllowed = "move", S.dataTransfer.setData("text/plain", h);
  }, [r]), E = Pe((S, h) => {
    if (!r || !p || p === h) return;
    S.preventDefault(), S.dataTransfer.dropEffect = "move";
    const I = S.currentTarget.getBoundingClientRect(), y = S.clientX < I.left + I.width / 2;
    b((N) => N && N.id === h && N.before === y ? N : { id: h, before: y });
  }, [r, p]), v = Pe(() => {
  }, []), g = Pe((S, h, I) => {
    const y = c.map((T) => T.id), N = y.indexOf(S);
    if (N < 0) return;
    y.splice(N, 1);
    const U = y.indexOf(h);
    if (U < 0) {
      y.splice(N, 0, S);
      return;
    }
    const A = I ? U : U + 1;
    y.splice(A, 0, S), n("reorder", { order: y });
  }, [c, n]), R = Pe((S, h) => {
    if (!r || !p || p === h) return;
    S.preventDefault();
    const I = S.currentTarget.getBoundingClientRect(), y = S.clientX < I.left + I.width / 2;
    g(p, h, y), f(null), b(null);
  }, [r, p, g]), x = Pe(() => {
    f(null), b(null);
  }, []), D = {
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: D }, c.map((S) => {
      const h = m[S.id];
      if (!h) return null;
      const I = {
        gridColumn: `${h.colStart + 1} / ${h.colEnd + 1}`,
        gridRow: `${h.rowStart + 1} / ${h.rowEnd + 1}`
      }, y = ["tlDashboard__tile"];
      return p === S.id && y.push("tlDashboard__tile--dragging"), _ && _.id === S.id && y.push(_.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: S.id,
          className: y.join(" "),
          style: I,
          draggable: r,
          onDragStart: (N) => C(N, S.id),
          onDragOver: (N) => E(N, S.id),
          onDragLeave: v,
          onDrop: (N) => R(N, S.id),
          onDragEnd: x
        },
        /* @__PURE__ */ e.createElement(K, { control: S.control }),
        r && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: Rr, useRef: xt, useState: Lt, useEffect: Dr, useLayoutEffect: xr } = e, Lr = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, a) => /* @__PURE__ */ e.createElement("span", { key: a, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(K, { control: n }))));
}, Ir = ({ group: l }) => {
  var p, f;
  const [t, n] = Lt(!1), [a, c] = Lt({}), s = xt(null), i = xt(null), u = Rr(() => {
    n((_) => !_);
  }, []);
  xr(() => {
    if (!t) return;
    const _ = () => {
      const b = s.current;
      if (!b) return;
      const C = b.getBoundingClientRect();
      c({
        position: "fixed",
        top: C.bottom + 4,
        right: Math.max(8, window.innerWidth - C.right),
        left: "auto"
      });
    };
    return _(), window.addEventListener("resize", _), window.addEventListener("scroll", _, !0), () => {
      window.removeEventListener("resize", _), window.removeEventListener("scroll", _, !0);
    };
  }, [t]), Dr(() => {
    if (!t) return;
    const _ = (b) => {
      i.current && !i.current.contains(b.target) && s.current && !s.current.contains(b.target) && n(!1);
    };
    return document.addEventListener("mousedown", _), () => document.removeEventListener("mousedown", _);
  }, [t]), Ne(t, { ESCAPE: () => n(!1) }), it(t, i, "first");
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
    m ? /* @__PURE__ */ e.createElement(Ee, { encoded: l.icon, className: "tlToolbar__menuIcon" }) : /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement("span", null, o), /* @__PURE__ */ e.createElement("svg", { className: "tlToolbar__chevron", viewBox: "0 0 24 24", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("polyline", { points: "6,9 12,15 18,9" })))
  ), Mt.createPortal(
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
      (f = l.subGroups) == null ? void 0 : f.map((_, b) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${b}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), _.items.map((C, E) => /* @__PURE__ */ e.createElement("div", { key: E, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(K, { control: C })))))
    ),
    document.body
  ));
}, Pr = ({ controlId: l }) => {
  const a = (q().groups ?? []).filter((c) => c.items.some((s) => s != null));
  return a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, a.map((c, s) => /* @__PURE__ */ e.createElement(e.Fragment, { key: c.name }, s > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), c.display === "menu" ? /* @__PURE__ */ e.createElement(Ir, { group: c }) : /* @__PURE__ */ e.createElement(Lr, { group: c }))));
}, Mr = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlTileStack", style: { width: "100%", height: "100%" } }, t.frame && /* @__PURE__ */ e.createElement(K, { control: t.frame }));
}, jr = ({ controlId: l }) => {
  const t = q(), n = le(), a = t.content, c = t.breadcrumb ?? null;
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
}, Br = ({ controlId: l }) => {
  const n = q().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((a, c) => /* @__PURE__ */ e.createElement(K, { key: c, control: a })));
}, Ar = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), Or = {
  "js.sidebar.openDrawer": "Open navigation"
}, $r = ({ controlId: l }) => {
  const t = le(), n = ce(Or);
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
W("TLButton", tn);
W("TLUploadButton", nn);
W("TLToggleButton", rn);
W("TLTextInput", Wt);
W("TLPasswordInput", Vt);
W("TLNumberInput", Yt);
W("TLDatePicker", Xt);
W("TLSelect", Zt);
W("TLCheckbox", Jt);
W("TLCounter", an);
W("TLTabBar", sn);
W("TLFieldList", cn);
W("TLAudioRecorder", dn);
W("TLAudioPlayer", pn);
W("TLFileUpload", hn);
W("TLBinaryField", _n);
W("TLDownload", vn);
W("TLPhotoCapture", Cn);
W("TLPhotoViewer", yn);
W("TLPdfViewer", Sn);
W("TLSplitPanel", Nn);
W("TLPanel", Pn);
W("TLInset", Vn);
W("TLMaximizeRoot", Mn);
W("TLDeckPane", jn);
W("TLSidebar", Wn);
W("TLStack", zn);
W("TLGrid", Kn);
W("TLCard", Yn);
W("TLAppBar", Gn);
W("TLBreadcrumb", qn);
W("TLBottomBar", Qn);
W("TLDialog", tl);
W("TLDialogManager", rl);
W("TLWindow", cl);
W("TLDrawer", dl);
W("TLContextMenuRegion", pl);
W("TLSnackbar", _l);
W("TLMenu", vl);
W("TLAppShell", Cl);
W("TLText", wl);
W("TLTableView", Sl);
W("TLFormLayout", Il);
W("TLFormGroup", jl);
W("TLFormField", $l);
W("TLResourceCell", Fl);
W("TLTreeView", Hl);
W("TLDropdownSelect", Xl);
W("TLColorInput", ur);
W("TLIconSelect", gr);
W("TLDashboard", Tr);
W("TLToolbar", Pr);
W("TLTileStack", Mr);
W("TLAdaptiveDetail", jr);
W("TLSlot", Br);
W("TLSlotContent", Ar);
W("TLDrawerToggle", $r);
