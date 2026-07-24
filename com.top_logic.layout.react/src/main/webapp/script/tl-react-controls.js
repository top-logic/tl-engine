import { React as e, useTLFieldValue as Te, useTLCommand as le, useTLState as X, useKeyboardBinding as ue, useTLUpload as Ue, TLChild as Y, useI18N as se, useTLDataUrl as ze, scrollToAnchor as ln, useStandaloneKeyboardScope as Me, KeyboardScopeProvider as yt, useFocusTrap as wt, CMD_VALUE_CHANGED as Qe, anchoredOverlayProps as an, register as W } from "tl-react-bridge";
const { useCallback: Tt, useRef: rn } = e, on = 300, sn = ({ controlId: l, state: t }) => {
  const [n, a, s] = Te({ debounceMs: on }), c = le(), i = rn(!1), u = Tt(
    (v) => {
      i.current = !0, a(v.target.value);
    },
    [a]
  ), r = t.commitOnBlur === !0, o = Tt(async () => {
    await s(), r && i.current && (i.current = !1, c("commit"));
  }, [s, r, c]), d = t.multiline === !0;
  if (t.editable === !1) {
    const v = "tlReactTextInput tlReactTextInput--immutable" + (d ? " tlReactTextInput--multiline" : "");
    return /* @__PURE__ */ e.createElement(
      "span",
      {
        id: l,
        className: v,
        style: d ? { whiteSpace: "pre-wrap" } : void 0
      },
      n ?? ""
    );
  }
  const m = t.hasError === !0, f = t.hasWarnings === !0, E = t.errorMessage, h = [
    "tlReactTextInput",
    d ? "tlReactTextInput--multiline" : "",
    m ? "tlReactTextInput--error" : "",
    !m && f ? "tlReactTextInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, d ? /* @__PURE__ */ e.createElement(
    "textarea",
    {
      rows: t.rows ?? 3,
      value: n ?? "",
      placeholder: t.placeholder ?? void 0,
      onChange: u,
      onBlur: o,
      disabled: t.disabled === !0,
      className: h,
      "aria-invalid": m || void 0,
      title: m && E ? E : void 0
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
      className: h,
      "aria-invalid": m || void 0,
      title: m && E ? E : void 0
    }
  ));
}, { useCallback: Rt } = e, cn = 300, un = ({ controlId: l, state: t }) => {
  const [n, a, s] = Te({ debounceMs: cn }), c = Rt(
    (m) => {
      a(m.target.value);
    },
    [a]
  ), i = Rt(() => {
    s();
  }, [s]);
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, "••••••••");
  const u = t.hasError === !0, r = t.hasWarnings === !0, o = t.errorMessage, d = [
    "tlReactTextInput",
    u ? "tlReactTextInput--error" : "",
    !u && r ? "tlReactTextInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "password",
      value: n ?? "",
      onChange: c,
      onBlur: i,
      disabled: t.disabled === !0,
      className: d,
      "aria-invalid": u || void 0,
      title: u && o ? o : void 0
    }
  ));
}, { useCallback: Lt } = e, dn = 300, mn = ({ controlId: l, state: t, config: n }) => {
  const [a, s, c] = Te({ debounceMs: dn }), i = Lt(
    (f) => {
      const E = f.target.value;
      s(E === "" ? null : E);
    },
    [s]
  ), u = Lt(() => {
    c();
  }, [c]);
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, a != null ? String(a) : "");
  const r = t.hasError === !0, o = t.hasWarnings === !0, d = t.errorMessage, m = [
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
      className: m,
      "aria-invalid": r || void 0,
      title: r && d ? d : void 0
    }
  ));
}, { useCallback: pn } = e, fn = ({ controlId: l, state: t }) => {
  const [n, a] = Te(), s = pn(
    (r) => {
      a(r.target.value || null);
    },
    [a]
  );
  if (t.editable === !1) {
    const r = t.displayValue ?? n ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, r);
  }
  const c = t.hasError === !0, i = t.hasWarnings === !0, u = [
    "tlReactDatePicker",
    c ? "tlReactDatePicker--error" : "",
    !c && i ? "tlReactDatePicker--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: n ?? "",
      onChange: s,
      disabled: t.disabled === !0,
      className: u,
      "aria-invalid": c || void 0
    }
  ));
}, { useCallback: hn } = e, bn = ({ controlId: l, state: t, config: n }) => {
  var d;
  const [a, s] = Te(), c = hn(
    (m) => {
      s(m.target.value || null);
    },
    [s]
  ), i = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const m = ((d = i.find((f) => f.value === a)) == null ? void 0 : d.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, m);
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
      onChange: c,
      disabled: t.disabled === !0,
      className: o,
      "aria-invalid": u || void 0
    },
    t.nullable !== !1 && /* @__PURE__ */ e.createElement("option", { value: "" }),
    i.map((m) => /* @__PURE__ */ e.createElement("option", { key: m.value, value: m.value }, m.label))
  ));
}, { useCallback: gn } = e, En = ({ controlId: l, state: t }) => {
  const [n, a] = Te(), s = gn(
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
  const c = t.hasError === !0, i = t.hasWarnings === !0, u = [
    "tlReactCheckbox",
    c ? "tlReactCheckbox--error" : "",
    !c && i ? "tlReactCheckbox--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: l,
      checked: n === !0,
      onChange: s,
      disabled: t.disabled === !0,
      className: u,
      "aria-invalid": c || void 0
    }
  );
};
function Ce({ encoded: l, className: t }) {
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
const { useCallback: vn } = e, _n = ({ controlId: l, command: t, label: n, image: a, disabled: s, displayMode: c }) => {
  const i = X(), u = le(), r = t ?? "click", o = n ?? i.label, d = a ?? i.image, m = s ?? i.disabled === !0, f = c ?? i.displayMode ?? "label-only", E = i.hidden === !0, h = i.tooltip, v = i.appearance, _ = i.size, g = i.navigateUrl, y = vn(() => {
    if (g) {
      window.location.assign(g);
      return;
    }
    u(r);
  }, [u, r, g]), L = i.keyGesture;
  ue(L, () => m || E ? !1 : (y(), !0));
  const T = f === "icon-only", w = f === "label-only" || f === "icon-label" || T && !d, k = h ?? (T ? o : void 0), b = k ? `text:${k}` : void 0;
  return E ? null : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: y,
      disabled: m,
      className: "tlReactButton" + (T ? " tlReactButton--iconOnly" : "") + (f === "label-only" ? " tlReactButton--labelOnly" : "") + (v === "link" ? " tlReactButton--link" : "") + (v === "primary" ? " tlReactButton--primary" : "") + (_ === "small" ? " tlReactButton--small" : "") + (_ === "large" ? " tlReactButton--large" : ""),
      "data-tooltip": b,
      "aria-label": d || T ? o : void 0
    },
    d && /* @__PURE__ */ e.createElement(Ce, { encoded: d, className: "tlReactButton__image" }),
    w && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, o)
  );
}, Cn = ({ controlId: l }) => {
  const t = X(), n = Ue(), a = e.useRef(null), [s, c] = e.useState(!1), i = t.label ?? "", u = t.image, r = t.disabled === !0, o = t.hidden === !0, d = t.displayMode ?? "label-only", m = t.appearance, f = t.accept, E = t.multiple === !0, h = e.useCallback(() => {
    var T;
    r || s || (T = a.current) == null || T.click();
  }, [r, s]), v = e.useCallback(async (T) => {
    const w = T.target.files;
    if (!w || w.length === 0) return;
    const k = new FormData();
    for (let b = 0; b < w.length; b++)
      k.append("file", w[b], w[b].name);
    T.target.value = "", c(!0);
    try {
      await n(k);
    } finally {
      c(!1);
    }
  }, [n]), _ = d === "icon-only", g = d === "icon-only" || d === "icon-label", y = d === "label-only" || d === "icon-label" || _ && !u, L = r || s;
  return /* @__PURE__ */ e.createElement("span", { id: l, style: { display: "contents" } }, /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: a,
      type: "file",
      accept: f && f !== "*" ? f : void 0,
      multiple: E || void 0,
      onChange: v,
      style: { display: "none" }
    }
  ), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: h,
      disabled: L,
      style: o ? { display: "none" } : void 0,
      className: "tlReactButton" + (_ ? " tlReactButton--iconOnly" : "") + (m === "link" ? " tlReactButton--link" : "") + (m === "primary" ? " tlReactButton--primary" : ""),
      "aria-label": _ ? i : void 0
    },
    g && u && /* @__PURE__ */ e.createElement(Ce, { encoded: u, className: "tlReactButton__image" }),
    y && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, i)
  ));
}, { useCallback: yn } = e, wn = ({ controlId: l, command: t, label: n, active: a, disabled: s }) => {
  const c = X(), i = le(), u = t ?? "click", r = n ?? c.label, o = a ?? c.active === !0, d = s ?? c.disabled === !0, m = yn(() => {
    i(u);
  }, [i, u]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: m,
      disabled: d,
      className: "tlReactButton" + (o ? " tlReactButtonActive" : "")
    },
    r
  );
}, kn = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.count ?? 0, s = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, s), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Nn } = e, Sn = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.tabs ?? [], s = t.activeTabId, c = Nn((i) => {
    i !== s && n("selectTab", { tabId: i });
  }, [n, s]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, a.map((i) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: i.id,
      role: "tab",
      "aria-selected": i.id === s,
      className: "tlReactTabBar__tab" + (i.id === s ? " tlReactTabBar__tab--active" : ""),
      onClick: () => c(i.id)
    },
    i.icon && /* @__PURE__ */ e.createElement(Ce, { encoded: i.icon, className: "tlReactTabBar__tabIcon" }),
    i.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(Y, { control: t.activeContent })));
}, Dn = ({ controlId: l }) => {
  const t = X(), n = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((s, c) => /* @__PURE__ */ e.createElement("div", { key: c, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(Y, { control: s })))));
}, Tn = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Rn = ({ controlId: l }) => {
  const t = X(), n = Ue(), [a, s] = e.useState("idle"), [c, i] = e.useState(null), u = e.useRef(null), r = e.useRef([]), o = e.useRef(null), d = t.status ?? "idle", m = t.error, f = d === "received" ? "idle" : a !== "idle" ? a : d, E = e.useCallback(async () => {
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
        const L = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", T = new MediaRecorder(y, L ? { mimeType: L } : void 0);
        u.current = T, T.ondataavailable = (w) => {
          w.data.size > 0 && r.current.push(w.data);
        }, T.onstop = async () => {
          y.getTracks().forEach((b) => b.stop()), o.current = null;
          const w = new Blob(r.current, { type: T.mimeType || "audio/webm" });
          if (r.current = [], w.size === 0) {
            s("idle");
            return;
          }
          s("uploading");
          const k = new FormData();
          k.append("audio", w, "recording.webm"), await n(k), s("idle");
        }, T.start(), s("recording");
      } catch (y) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", y), i("js.audioRecorder.error.denied"), s("idle");
      }
    }
  }, [a, n]), h = se(Tn), v = f === "recording" ? h["js.audioRecorder.stop"] : f === "uploading" ? h["js.uploading"] : h["js.audioRecorder.record"], _ = f === "uploading", g = ["tlAudioRecorder__button"];
  return f === "recording" && g.push("tlAudioRecorder__button--recording"), f === "uploading" && g.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: g.join(" "),
      onClick: E,
      disabled: _,
      title: v,
      "aria-label": v
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${f === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, h[c]), m && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, m));
}, Ln = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, xn = ({ controlId: l }) => {
  const t = X(), n = ze(), a = !!t.hasAudio, s = t.dataRevision ?? 0, [c, i] = e.useState(a ? "idle" : "disabled"), u = e.useRef(null), r = e.useRef(null), o = e.useRef(s);
  e.useEffect(() => {
    a ? c === "disabled" && i("idle") : (u.current && (u.current.pause(), u.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null), i("disabled"));
  }, [a]), e.useEffect(() => {
    s !== o.current && (o.current = s, u.current && (u.current.pause(), u.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null), (c === "playing" || c === "paused" || c === "loading") && i("idle"));
  }, [s]), e.useEffect(() => () => {
    u.current && (u.current.pause(), u.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null);
  }, []);
  const d = e.useCallback(async () => {
    if (c === "disabled" || c === "loading")
      return;
    if (c === "playing") {
      u.current && u.current.pause(), i("paused");
      return;
    }
    if (c === "paused" && u.current) {
      u.current.play(), i("playing");
      return;
    }
    if (!r.current) {
      i("loading");
      try {
        const _ = await fetch(n);
        if (!_.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", _.status), i("idle");
          return;
        }
        const g = await _.blob();
        r.current = URL.createObjectURL(g);
      } catch (_) {
        console.error("[TLAudioPlayer] Fetch error:", _), i("idle");
        return;
      }
    }
    const v = new Audio(r.current);
    u.current = v, v.onended = () => {
      i("idle");
    }, v.play(), i("playing");
  }, [c, n]), m = se(Ln), f = c === "loading" ? m["js.loading"] : c === "playing" ? m["js.audioPlayer.pause"] : c === "disabled" ? m["js.audioPlayer.noAudio"] : m["js.audioPlayer.play"], E = c === "disabled" || c === "loading", h = ["tlAudioPlayer__button"];
  return c === "playing" && h.push("tlAudioPlayer__button--playing"), c === "loading" && h.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: h.join(" "),
      onClick: d,
      disabled: E,
      title: f,
      "aria-label": f
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${c === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Mn = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, In = ({ controlId: l }) => {
  const t = X(), n = Ue(), [a, s] = e.useState("idle"), [c, i] = e.useState(!1), u = e.useRef(null), r = t.status ?? "idle", o = t.error, d = t.accept ?? "", m = r === "received" ? "idle" : a !== "idle" ? a : r, f = e.useCallback(async (w) => {
    s("uploading");
    const k = new FormData();
    k.append("file", w, w.name), await n(k), s("idle");
  }, [n]), E = e.useCallback((w) => {
    var b;
    const k = (b = w.target.files) == null ? void 0 : b[0];
    k && f(k);
  }, [f]), h = e.useCallback(() => {
    var w;
    a !== "uploading" && ((w = u.current) == null || w.click());
  }, [a]), v = e.useCallback((w) => {
    w.preventDefault(), w.stopPropagation(), i(!0);
  }, []), _ = e.useCallback((w) => {
    w.preventDefault(), w.stopPropagation(), i(!1);
  }, []), g = e.useCallback((w) => {
    var b;
    if (w.preventDefault(), w.stopPropagation(), i(!1), a === "uploading") return;
    const k = (b = w.dataTransfer.files) == null ? void 0 : b[0];
    k && f(k);
  }, [a, f]), y = m === "uploading", L = se(Mn), T = m === "uploading" ? L["js.uploading"] : L["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${c ? " tlFileUpload--dragover" : ""}`,
      onDragOver: v,
      onDragLeave: _,
      onDrop: g
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: u,
        type: "file",
        accept: d || void 0,
        onChange: E,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (m === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: h,
        disabled: y,
        title: T,
        "aria-label": T
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    o && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, o)
  );
}, jn = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, Pn = ({ controlId: l, state: t }) => {
  const a = X() ?? t ?? {}, s = Ue(), c = ze(), i = se(jn), u = a.editable !== !1, r = !!a.hasData, o = a.fileName ?? "download", d = a.dataRevision ?? 0, m = a.accept ?? "", f = a.status ?? "idle", E = a.error ?? null, [h, v] = e.useState("idle"), [_, g] = e.useState(!1), [y, L] = e.useState(!1), T = e.useRef(null), w = e.useCallback(async () => {
    if (!(!r || y)) {
      L(!0);
      try {
        const M = c + (c.includes("?") ? "&" : "?") + "rev=" + d, B = await fetch(M);
        if (!B.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", B.status);
          return;
        }
        const O = await B.blob(), K = URL.createObjectURL(O), p = document.createElement("a");
        p.href = K, p.download = o, p.style.display = "none", document.body.appendChild(p), p.click(), document.body.removeChild(p), URL.revokeObjectURL(K);
      } catch (M) {
        console.error("[TLBinaryField] Fetch error:", M);
      } finally {
        L(!1);
      }
    }
  }, [r, y, c, d, o]), k = e.useCallback(async (M) => {
    v("uploading");
    const B = new FormData();
    B.append("file", M, M.name), await s(B), v("idle");
  }, [s]), b = (f === "received" ? "idle" : h !== "idle" ? h : f) === "uploading", j = e.useCallback((M) => {
    var O;
    const B = (O = M.target.files) == null ? void 0 : O[0];
    B && k(B);
  }, [k]), S = e.useCallback(() => {
    var M;
    b || (M = T.current) == null || M.click();
  }, [b]), R = e.useCallback((M) => {
    M.preventDefault(), M.stopPropagation(), g(!0);
  }, []), U = e.useCallback((M) => {
    M.preventDefault(), M.stopPropagation(), g(!1);
  }, []), H = e.useCallback((M) => {
    var O;
    if (M.preventDefault(), M.stopPropagation(), g(!1), b) return;
    const B = (O = M.dataTransfer.files) == null ? void 0 : O[0];
    B && k(B);
  }, [b, k]), C = y ? i["js.downloading"] : i["js.download.file"].replace("{0}", o), x = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (y ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: w,
      disabled: y,
      title: C,
      "aria-label": C
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o));
  if (!u)
    return r ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, x) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, i["js.download.noFile"]));
  const P = b, I = b ? i["js.uploading"] : i["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlBinaryField tlFileUpload${_ ? " tlFileUpload--dragover" : ""}`,
      onDragOver: R,
      onDragLeave: U,
      onDrop: H
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: T,
        type: "file",
        accept: m || void 0,
        onChange: j,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (P ? " tlFileUpload__button--uploading" : ""),
        onClick: S,
        disabled: P,
        title: I,
        "aria-label": I
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    r && x,
    E && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, E)
  );
}, An = {
  "js.fileChips.add": "Add file",
  "js.fileChips.remove": "Remove {0}",
  "js.uploading": "Uploading…",
  "js.download.file": "Download {0}"
};
function Bn(l) {
  return l < 1024 ? l + " B" : l < 1024 * 1024 ? (l / 1024).toFixed(l < 10 * 1024 ? 1 : 0) + " KB" : (l / (1024 * 1024)).toFixed(1) + " MB";
}
const On = ({ controlId: l }) => {
  const t = X(), n = le(), a = Ue(), s = ze(), c = se(An), i = t.chips ?? [], u = t.editable === !0, [r, o] = e.useState(!1), [d, m] = e.useState(!1), f = e.useRef(null), E = e.useCallback(async (w) => {
    const k = Array.from(w);
    if (k.length !== 0) {
      o(!0);
      try {
        const b = new FormData();
        for (const j of k)
          b.append("file", j, j.name);
        await a(b);
      } finally {
        o(!1);
      }
    }
  }, [a]), h = e.useCallback(async (w) => {
    if (w.hasData)
      try {
        const k = s + "&key=" + encodeURIComponent(w.key), b = await fetch(k);
        if (!b.ok) {
          console.error("[TLFileChips] Failed to fetch data:", b.status);
          return;
        }
        const j = await b.blob(), S = URL.createObjectURL(j), R = document.createElement("a");
        R.href = S, R.download = w.name, R.style.display = "none", document.body.appendChild(R), R.click(), document.body.removeChild(R), URL.revokeObjectURL(S);
      } catch (k) {
        console.error("[TLFileChips] Fetch error:", k);
      }
  }, [s]), v = e.useCallback((w) => {
    w.target.files && E(w.target.files), w.target.value = "";
  }, [E]), _ = e.useCallback(() => {
    var w;
    r || (w = f.current) == null || w.click();
  }, [r]), g = e.useCallback((w) => {
    u && (w.preventDefault(), w.stopPropagation(), m(!0));
  }, [u]), y = e.useCallback((w) => {
    u && (w.preventDefault(), w.stopPropagation(), m(!1));
  }, [u]), L = e.useCallback((w) => {
    u && (w.preventDefault(), w.stopPropagation(), m(!1), !r && w.dataTransfer.files && E(w.dataTransfer.files));
  }, [u, r, E]), T = [
    "tlFileChips",
    u ? "tlFileChips--editable" : "",
    d ? "tlFileChips--dragover" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: T,
      onDragOver: g,
      onDragLeave: y,
      onDrop: L
    },
    i.map((w) => {
      const k = c["js.download.file"].replace("{0}", w.name), b = c["js.fileChips.remove"].replace("{0}", w.name);
      return /* @__PURE__ */ e.createElement("span", { key: w.key, className: "tlFileChip" }, /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__main",
          onClick: () => h(w),
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
        w.size != null && /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__size" }, Bn(w.size))
      ), u && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__remove",
          onClick: () => n("removeChip", { key: w.key }),
          title: b,
          "aria-label": b
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
        onChange: v,
        style: { display: "none" }
      }
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileChips__add" + (r ? " tlFileChips__add--uploading" : ""),
        onClick: _,
        disabled: r,
        title: r ? c["js.uploading"] : c["js.fileChips.add"]
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
      /* @__PURE__ */ e.createElement("span", null, r ? c["js.uploading"] : c["js.fileChips.add"])
    ))
  );
}, Fn = 3e4;
function $n(l, t) {
  const n = Math.round((l - Date.now()) / 1e3), a = Math.abs(n), s = new Intl.RelativeTimeFormat(t, { numeric: "auto" });
  return a < 60 ? s.format(Math.trunc(n / 1), "second") : a < 3600 ? s.format(Math.trunc(n / 60), "minute") : a < 86400 ? s.format(Math.trunc(n / 3600), "hour") : a < 7 * 86400 ? s.format(Math.trunc(n / 86400), "day") : new Date(l).toLocaleDateString(t);
}
const Hn = ({ controlId: l }) => {
  const t = X(), n = t.timestamp, a = t.label ?? void 0, s = t.locale || navigator.language, [, c] = e.useState(0);
  return e.useEffect(() => {
    const i = setInterval(() => c((u) => u + 1), Fn);
    return () => clearInterval(i);
  }, []), n == null ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime tlRelativeTime--empty" }) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime", title: a }, $n(n, s));
}, Wn = ({ controlId: l }) => {
  const t = X(), n = t.anchor ?? void 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAnchor", "data-tl-anchor": n }, t.child && /* @__PURE__ */ e.createElement(Y, { control: t.child }));
}, Un = ({ controlId: l }) => {
  const t = X(), n = t.target, a = t.label ?? "";
  if (n == null)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlScrollLink tlScrollLink--empty" });
  const s = (c) => {
    c.preventDefault(), ln(n);
  };
  return /* @__PURE__ */ e.createElement("a", { id: l, className: "tlScrollLink", href: "#", onClick: s }, a);
};
function zn(l) {
  const t = l.trim().split(/\s+/).filter(Boolean);
  return t.length === 0 ? "?" : t.length === 1 ? t[0].slice(0, 2).toUpperCase() : (t[0][0] + t[t.length - 1][0]).toUpperCase();
}
function Vn(l) {
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return Math.abs(t) % 360;
}
const Kn = ({ controlId: l }) => {
  const n = X().name;
  return n ? /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlAvatar",
      style: { backgroundColor: `hsl(${Vn(n)}, 45%, 45%)` },
      title: n,
      "aria-label": n
    },
    zn(n)
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlAvatar tlAvatar--empty" });
}, Yn = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Gn = ({ controlId: l }) => {
  const t = X(), n = ze(), a = le(), s = !!t.hasData, c = t.dataRevision ?? 0, i = t.fileName ?? "download", u = !!t.clearable, [r, o] = e.useState(!1), d = e.useCallback(async () => {
    if (!(!s || r)) {
      o(!0);
      try {
        const h = n + (n.includes("?") ? "&" : "?") + "rev=" + c, v = await fetch(h);
        if (!v.ok) {
          console.error("[TLDownload] Failed to fetch data:", v.status);
          return;
        }
        const _ = await v.blob(), g = URL.createObjectURL(_), y = document.createElement("a");
        y.href = g, y.download = i, y.style.display = "none", document.body.appendChild(y), y.click(), document.body.removeChild(y), URL.revokeObjectURL(g);
      } catch (h) {
        console.error("[TLDownload] Fetch error:", h);
      } finally {
        o(!1);
      }
    }
  }, [s, r, n, c, i]), m = e.useCallback(async () => {
    s && await a("clear");
  }, [s, a]), f = se(Yn);
  if (!s)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, f["js.download.noFile"]));
  const E = r ? f["js.downloading"] : f["js.download.file"].replace("{0}", i);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (r ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: d,
      disabled: r,
      title: E,
      "aria-label": E
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: i }, i), u && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: m,
      title: f["js.download.clear"],
      "aria-label": f["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, Xn = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, qn = ({ controlId: l }) => {
  const t = X(), n = Ue(), [a, s] = e.useState("idle"), [c, i] = e.useState(null), [u, r] = e.useState(!1), o = e.useRef(null), d = e.useRef(null), m = e.useRef(null), f = e.useRef(null), E = e.useRef(null), h = t.error, v = e.useMemo(
    () => {
      var R;
      return !!(window.isSecureContext && ((R = navigator.mediaDevices) != null && R.getUserMedia));
    },
    []
  ), _ = e.useCallback(() => {
    d.current && (d.current.getTracks().forEach((R) => R.stop()), d.current = null), o.current && (o.current.srcObject = null);
  }, []), g = e.useCallback(() => {
    _(), s("idle");
  }, [_]), y = e.useCallback(async () => {
    var R;
    if (a !== "uploading") {
      if (i(null), !v) {
        (R = f.current) == null || R.click();
        return;
      }
      try {
        const U = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        d.current = U, s("overlayOpen");
      } catch (U) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", U), i("js.photoCapture.error.denied"), s("idle");
      }
    }
  }, [a, v]), L = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const R = o.current, U = m.current;
    if (!R || !U)
      return;
    U.width = R.videoWidth, U.height = R.videoHeight;
    const H = U.getContext("2d");
    H && (H.drawImage(R, 0, 0), _(), s("uploading"), U.toBlob(async (C) => {
      if (!C) {
        s("idle");
        return;
      }
      const x = new FormData();
      x.append("photo", C, "capture.jpg"), await n(x), s("idle");
    }, "image/jpeg", 0.85));
  }, [a, n, _]), T = e.useCallback(async (R) => {
    var C;
    const U = (C = R.target.files) == null ? void 0 : C[0];
    if (!U) return;
    s("uploading");
    const H = new FormData();
    H.append("photo", U, U.name), await n(H), s("idle"), f.current && (f.current.value = "");
  }, [n]);
  e.useEffect(() => {
    a === "overlayOpen" && o.current && d.current && (o.current.srcObject = d.current);
  }, [a]), e.useEffect(() => {
    var U;
    if (a !== "overlayOpen") return;
    (U = E.current) == null || U.focus();
    const R = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = R;
    };
  }, [a]), Me(a === "overlayOpen", { ESCAPE: g }), e.useEffect(() => () => {
    d.current && (d.current.getTracks().forEach((R) => R.stop()), d.current = null);
  }, []);
  const w = se(Xn), k = a === "uploading" ? w["js.uploading"] : w["js.photoCapture.open"], b = ["tlPhotoCapture__cameraBtn"];
  a === "uploading" && b.push("tlPhotoCapture__cameraBtn--uploading");
  const j = ["tlPhotoCapture__overlayVideo"];
  u && j.push("tlPhotoCapture__overlayVideo--mirrored");
  const S = ["tlPhotoCapture__mirrorBtn"];
  return u && S.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: b.join(" "),
      onClick: y,
      disabled: a === "uploading",
      title: k,
      "aria-label": k
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !v && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: f,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: T
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: m, style: { display: "none" } }), a === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: E,
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
        className: j.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: S.join(" "),
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
        onClick: L,
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
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, w[c]), h && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, h));
}, Zn = {
  "js.photoViewer.alt": "Captured photo"
}, Qn = ({ controlId: l }) => {
  const t = X(), n = ze(), a = !!t.hasPhoto, s = t.dataRevision ?? 0, [c, i] = e.useState(null), u = e.useRef(s);
  e.useEffect(() => {
    if (!a) {
      c && (URL.revokeObjectURL(c), i(null));
      return;
    }
    if (s === u.current && c)
      return;
    u.current = s, c && (URL.revokeObjectURL(c), i(null));
    let o = !1;
    return (async () => {
      try {
        const d = await fetch(n);
        if (!d.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", d.status);
          return;
        }
        const m = await d.blob();
        o || i(URL.createObjectURL(m));
      } catch (d) {
        console.error("[TLPhotoViewer] Fetch error:", d);
      }
    })(), () => {
      o = !0;
    };
  }, [a, s, n]), e.useEffect(() => () => {
    c && URL.revokeObjectURL(c);
  }, []);
  const r = se(Zn);
  return !a || !c ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: c,
      alt: r["js.photoViewer.alt"]
    }
  ));
}, Jn = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, el = ({ controlId: l }) => {
  const t = X(), n = ze(), a = !!t.hasPdf, s = t.dataRevision ?? 0, c = se(Jn), u = n.indexOf("react-api/"), r = u >= 0 ? n.slice(0, u) : n, o = n + "&rev=" + s, d = r + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(o);
  return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: d,
      title: c["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, c["js.pdfViewer.noDocument"]));
}, { useCallback: xt, useRef: ct } = e, tl = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.orientation, s = t.resizable === !0, c = t.children ?? [], i = a === "horizontal", u = c.length > 0 && c.every((_) => _.collapsed), r = !u && c.some((_) => _.collapsed), o = u ? !i : i, d = ct(null), m = ct(null), f = ct(null), E = xt((_, g) => {
    const y = {
      overflow: _.scrolling || "auto"
    };
    return _.collapsed ? u && !o ? y.flex = "1 0 0%" : y.flex = "0 0 auto" : g !== void 0 ? y.flex = `0 0 ${g}px` : y.flex = `${_.size} 1 0%`, _.minSize > 0 && !_.collapsed && (y.minWidth = i ? _.minSize : void 0, y.minHeight = i ? void 0 : _.minSize), y;
  }, [i, u, r, o]), h = xt((_, g) => {
    _.preventDefault();
    const y = d.current;
    if (!y) return;
    const L = c[g], T = c[g + 1], w = y.querySelectorAll(":scope > .tlSplitPanel__child"), k = [];
    w.forEach((S) => {
      k.push(i ? S.offsetWidth : S.offsetHeight);
    }), f.current = k, m.current = {
      splitterIndex: g,
      startPos: i ? _.clientX : _.clientY,
      startSizeBefore: k[g],
      startSizeAfter: k[g + 1],
      childBefore: L,
      childAfter: T
    };
    const b = (S) => {
      const R = m.current;
      if (!R || !f.current) return;
      const H = (i ? S.clientX : S.clientY) - R.startPos, C = R.childBefore.minSize || 0, x = R.childAfter.minSize || 0;
      let P = R.startSizeBefore + H, I = R.startSizeAfter - H;
      P < C && (I += P - C, P = C), I < x && (P += I - x, I = x), f.current[R.splitterIndex] = P, f.current[R.splitterIndex + 1] = I;
      const M = y.querySelectorAll(":scope > .tlSplitPanel__child"), B = M[R.splitterIndex], O = M[R.splitterIndex + 1];
      B && (B.style.flex = `0 0 ${P}px`), O && (O.style.flex = `0 0 ${I}px`);
    }, j = () => {
      if (document.removeEventListener("mousemove", b), document.removeEventListener("mouseup", j), document.body.style.cursor = "", document.body.style.userSelect = "", f.current) {
        const S = {};
        c.forEach((R, U) => {
          const H = R.control;
          H != null && H.controlId && f.current && (S[H.controlId] = f.current[U]);
        }), n("updateSizes", { sizes: S });
      }
      f.current = null, m.current = null;
    };
    document.addEventListener("mousemove", b), document.addEventListener("mouseup", j), document.body.style.cursor = i ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [c, i, n]), v = [];
  return c.forEach((_, g) => {
    if (v.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${g}`,
          className: `tlSplitPanel__child${_.collapsed && o ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: E(_)
        },
        /* @__PURE__ */ e.createElement(Y, { control: _.control })
      )
    ), s && g < c.length - 1) {
      const y = c[g + 1];
      !_.collapsed && !y.collapsed && v.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${g}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${a}`,
            onMouseDown: (T) => h(T, g)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: d,
      id: l,
      className: `tlSplitPanel tlSplitPanel--${a}${u ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: o ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    v
  );
}, { useCallback: it } = e, nl = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, ll = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), al = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), rl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), ol = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), sl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), cl = ({ controlId: l }) => {
  const t = X(), n = le(), a = se(nl), s = t.title, c = t.expansionState ?? "NORMALIZED", i = t.showMinimize === !0, u = t.showMaximize === !0, r = t.showPopOut === !0, o = t.fullLine === !0, d = t.fill === !0, m = t.hoverActions === !0, f = t.appearance === "card", E = c === "MINIMIZED", h = c === "MAXIMIZED", v = c === "HIDDEN", _ = it(() => {
    n("toggleMinimize");
  }, [n]), g = it(() => {
    n("toggleMaximize");
  }, [n]), y = it(() => {
    n("popOut");
  }, [n]);
  if (v)
    return null;
  const L = h ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" }, T = i && !h || u && !E || r, w = !!s && s.trim() !== "" || !!t.titleContent || !!t.toolbar || T;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${c.toLowerCase()}${o ? " tlPanel--fullLine" : ""}${d ? " tlPanel--fill" : ""}${m ? " tlPanel--hoverActions" : ""}${f ? " tlPanel--card" : ""}`,
      style: L
    },
    w && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, !!s && s.trim() !== "" && /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, s), t.titleContent && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__titleContent" }, /* @__PURE__ */ e.createElement(Y, { control: t.titleContent })), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(Y, { control: t.toolbar }), i && !h && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: _,
        title: E ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      E ? /* @__PURE__ */ e.createElement(al, null) : /* @__PURE__ */ e.createElement(ll, null)
    ), u && !E && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: g,
        title: h ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      h ? /* @__PURE__ */ e.createElement(ol, null) : /* @__PURE__ */ e.createElement(rl, null)
    ), r && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: y,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(sl, null)
    ))),
    !E && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(Y, { control: t.child })),
    !E && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(Y, { control: t.buttonBar }))
  );
}, il = ({ controlId: l }) => {
  const t = X();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(Y, { control: t.child })
  );
}, ul = ({ controlId: l }) => {
  const t = X();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(Y, { control: t.activeChild }));
}, { useCallback: he, useState: at, useEffect: Et, useRef: rt } = e, dl = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function vt(l, t, n, a) {
  const s = [];
  for (const c of l)
    if (c.type === "nav") {
      if (c.hidden) continue;
      s.push({ id: c.id, type: "nav", groupId: a });
    } else c.type === "command" ? s.push({ id: c.id, type: "command", groupId: a }) : c.type === "group" && (s.push({ id: c.id, type: "group" }), (n.get(c.id) ?? c.expanded) && !t && s.push(...vt(c.children, t, n, c.id)));
  return s;
}
const We = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(Ce, { encoded: l, className: "tlSidebar__icon" }) : null, ml = ({ item: l, active: t, collapsed: n, onSelect: a, tabIndex: s, itemRef: c, onFocus: i }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => a(l.id),
    title: n ? l.label : void 0,
    tabIndex: s,
    ref: c,
    onFocus: () => i(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(We, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(We, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), pl = ({ item: l, collapsed: t, onExecute: n, tabIndex: a, itemRef: s, onFocus: c }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: a,
    ref: s,
    onFocus: () => c(l.id)
  },
  /* @__PURE__ */ e.createElement(We, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), fl = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(We, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), hl = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), bl = ({ item: l, activeItemId: t, anchorRect: n, onSelect: a, onExecute: s, onClose: c }) => {
  const i = rt(null);
  Et(() => {
    const o = (d) => {
      i.current && !i.current.contains(d.target) && setTimeout(() => c(), 0);
    };
    return document.addEventListener("mousedown", o), () => document.removeEventListener("mousedown", o);
  }, [c]), Me(!0, { ESCAPE: c });
  const u = he((o) => {
    o.type === "nav" ? (a(o.id), c()) : o.type === "command" && (s(o.id), c());
  }, [a, s, c]), r = {};
  return n && (r.left = n.right, r.top = n.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: i, role: "menu", style: r }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((o) => {
    if (o.type === "nav" && o.hidden) return null;
    if (o.type === "nav" || o.type === "command") {
      const d = o.type === "nav" && o.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: o.id,
          className: "tlSidebar__flyoutItem" + (d ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => u(o)
        },
        /* @__PURE__ */ e.createElement(We, { icon: o.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, o.label),
        o.type === "nav" && o.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, o.badge)
      );
    }
    return o.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: o.id, className: "tlSidebar__flyoutSectionHeader" }, o.label) : o.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: o.id, className: "tlSidebar__separator" }) : null;
  }));
}, gl = ({
  item: l,
  expanded: t,
  activeItemId: n,
  collapsed: a,
  onSelect: s,
  onExecute: c,
  onToggleGroup: i,
  tabIndex: u,
  itemRef: r,
  onFocus: o,
  focusedId: d,
  setItemRef: m,
  onItemFocus: f,
  flyoutGroupId: E,
  onOpenFlyout: h,
  onCloseFlyout: v
}) => {
  const _ = rt(null), [g, y] = at(null), L = he(() => {
    a ? E === l.id ? v() : (_.current && y(_.current.getBoundingClientRect()), h(l.id)) : i(l.id);
  }, [a, E, l.id, i, h, v]), T = he((k) => {
    _.current = k, r(k);
  }, [r]), w = a && E === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (w ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: L,
      title: a ? l.label : void 0,
      "aria-expanded": a ? w : t,
      tabIndex: u,
      ref: T,
      onFocus: () => o(l.id)
    },
    /* @__PURE__ */ e.createElement(We, { icon: l.icon }),
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
    bl,
    {
      item: l,
      activeItemId: n,
      anchorRect: g,
      onSelect: s,
      onExecute: c,
      onClose: v
    }
  ), t && !a && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((k) => /* @__PURE__ */ e.createElement(
    Xt,
    {
      key: k.id,
      item: k,
      activeItemId: n,
      collapsed: a,
      onSelect: s,
      onExecute: c,
      onToggleGroup: i,
      focusedId: d,
      setItemRef: m,
      onItemFocus: f,
      groupStates: null,
      flyoutGroupId: E,
      onOpenFlyout: h,
      onCloseFlyout: v
    }
  ))));
}, Xt = ({
  item: l,
  activeItemId: t,
  collapsed: n,
  onSelect: a,
  onExecute: s,
  onToggleGroup: c,
  focusedId: i,
  setItemRef: u,
  onItemFocus: r,
  groupStates: o,
  flyoutGroupId: d,
  onOpenFlyout: m,
  onCloseFlyout: f
}) => {
  switch (l.type) {
    case "nav":
      return l.hidden ? null : /* @__PURE__ */ e.createElement(
        ml,
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
        pl,
        {
          item: l,
          collapsed: n,
          onExecute: s,
          tabIndex: i === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: r
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(fl, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(hl, null);
    case "group": {
      const E = o ? o.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        gl,
        {
          item: l,
          expanded: E,
          activeItemId: t,
          collapsed: n,
          onSelect: a,
          onExecute: s,
          onToggleGroup: c,
          tabIndex: i === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: r,
          focusedId: i,
          setItemRef: u,
          onItemFocus: r,
          flyoutGroupId: d,
          onOpenFlyout: m,
          onCloseFlyout: f
        }
      );
    }
    default:
      return null;
  }
}, El = ({ controlId: l }) => {
  const t = X(), n = le(), a = se(dl), s = t.items ?? [], c = t.activeItemId, i = t.collapsed, u = t.drawerOpen, r = u ? !1 : i, [o, d] = at(() => {
    const C = /* @__PURE__ */ new Map(), x = (P) => {
      for (const I of P)
        I.type === "group" && (C.set(I.id, I.expanded), x(I.children));
    };
    return x(s), C;
  }), m = he((C) => {
    d((x) => {
      const P = new Map(x), I = P.get(C) ?? !1;
      return P.set(C, !I), n("toggleGroup", { itemId: C, expanded: !I }), P;
    });
  }, [n]), f = he((C) => {
    C !== c && n("selectItem", { itemId: C });
  }, [n, c]), E = he((C) => {
    n("executeCommand", { itemId: C });
  }, [n]), h = he(() => {
    n("toggleCollapse", {});
  }, [n]), v = he(() => {
    n("toggleDrawer", {});
  }, [n]), [_, g] = at(null), y = he((C) => {
    g(C);
  }, []), L = he(() => {
    g(null);
  }, []);
  Et(() => {
    r || g(null);
  }, [r]);
  const [T, w] = at(() => {
    const C = vt(s, r, o);
    return C.length > 0 ? C[0].id : "";
  }), k = rt(/* @__PURE__ */ new Map()), b = he((C) => (x) => {
    x ? k.current.set(C, x) : k.current.delete(C);
  }, []), j = he((C) => {
    w(C);
  }, []), S = rt(0), R = he((C) => {
    w(C), S.current++;
  }, []);
  Et(() => {
    const C = k.current.get(T);
    C && document.activeElement !== C && C.focus();
  }, [T, S.current]);
  const U = he((C) => {
    if (C.key === "Escape" && _ !== null) {
      C.preventDefault(), L();
      return;
    }
    const x = vt(s, r, o);
    if (x.length === 0) return;
    const P = x.findIndex((M) => M.id === T);
    if (P < 0) return;
    const I = x[P];
    switch (C.key) {
      case "ArrowDown": {
        C.preventDefault();
        const M = (P + 1) % x.length;
        R(x[M].id);
        break;
      }
      case "ArrowUp": {
        C.preventDefault();
        const M = (P - 1 + x.length) % x.length;
        R(x[M].id);
        break;
      }
      case "Home": {
        C.preventDefault(), R(x[0].id);
        break;
      }
      case "End": {
        C.preventDefault(), R(x[x.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        C.preventDefault(), I.type === "nav" ? f(I.id) : I.type === "command" ? E(I.id) : I.type === "group" && (r ? _ === I.id ? L() : y(I.id) : m(I.id));
        break;
      }
      case "ArrowRight": {
        I.type === "group" && !r && ((o.get(I.id) ?? !1) || (C.preventDefault(), m(I.id)));
        break;
      }
      case "ArrowLeft": {
        I.type === "group" && !r && (o.get(I.id) ?? !1) && (C.preventDefault(), m(I.id));
        break;
      }
    }
  }, [
    s,
    r,
    o,
    T,
    _,
    R,
    f,
    E,
    m,
    y,
    L
  ]), H = "tlSidebar" + (r ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: H }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(Y, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: v, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": a["js.sidebar.ariaLabel"] }, r ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(Y, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(Y, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: U }, s.map((C) => /* @__PURE__ */ e.createElement(
    Xt,
    {
      key: C.id,
      item: C,
      activeItemId: c,
      collapsed: r,
      onSelect: f,
      onExecute: E,
      onToggleGroup: m,
      focusedId: T,
      setItemRef: b,
      onItemFocus: j,
      groupStates: o,
      flyoutGroupId: _,
      onOpenFlyout: y,
      onCloseFlyout: L
    }
  ))), r ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(Y, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(Y, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: h,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(Y, { control: t.activeContent })));
}, vl = ({ controlId: l }) => {
  const t = X(), n = t.direction ?? "column", a = t.gap ?? "default", s = t.align ?? "stretch", c = t.wrap === !0, i = t.growFirst === !0, u = t.children ?? [], r = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${a}`,
    `tlStack--align-${s}`,
    c ? "tlStack--wrap" : "",
    i ? "tlStack--grow-first" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: r }, u.map((o, d) => /* @__PURE__ */ e.createElement(Y, { key: d, control: o })));
}, _l = ({ controlId: l }) => {
  const t = X();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlInset" }, t.child && /* @__PURE__ */ e.createElement(Y, { control: t.child }));
}, Cl = ({ controlId: l }) => {
  const t = X(), n = t.columns, a = t.minColumnWidth, s = t.gap ?? "default", c = t.children ?? [], i = {};
  return a ? i.gridTemplateColumns = `repeat(auto-fit, minmax(min(${a}, 100%), 1fr))` : n && (i.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${s}`, style: i }, c.map((u, r) => /* @__PURE__ */ e.createElement(Y, { key: r, control: u })));
}, yl = ({ controlId: l }) => {
  const t = X(), n = t.title, a = t.variant ?? "outlined", s = t.padding ?? "default", c = t.headerActions ?? [], i = t.child, u = n != null || c.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${a}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, c.map((r, o) => /* @__PURE__ */ e.createElement(Y, { key: o, control: r })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${s}` }, /* @__PURE__ */ e.createElement(Y, { control: i })));
}, wl = ({ controlId: l }) => {
  const t = X(), n = t.title ?? "", a = t.leading, s = t.children ?? [], c = t.actions ?? [], i = t.variant ?? "flat", r = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    i === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: r }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(Y, { control: a })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, s.map((o, d) => /* @__PURE__ */ e.createElement(Y, { key: d, control: o }))), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, c.map((o, d) => /* @__PURE__ */ e.createElement(Y, { key: d, control: o }))));
}, { useCallback: kl } = e, Nl = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.items ?? [], s = kl((c) => {
    n("navigate", { itemId: c });
  }, [n]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, a.map((c, i) => {
    const u = i === a.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: c.id, className: "tlBreadcrumb__entry" }, i > 0 && /* @__PURE__ */ e.createElement(
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
        onClick: () => s(c.id)
      },
      c.label
    ));
  })));
}, { useCallback: Sl } = e, Dl = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.items ?? [], s = t.activeItemId, c = Sl((i) => {
    i !== s && n("selectItem", { itemId: i });
  }, [n, s]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, a.map((i) => {
    const u = i.id === s;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: i.id,
        type: "button",
        className: "tlBottomBar__item" + (u ? " tlBottomBar__item--active" : ""),
        onClick: () => c(i.id),
        "aria-current": u ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + i.icon, "aria-hidden": "true" }), i.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, i.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, i.label)
    );
  }));
}, { useCallback: Mt, useRef: Tl } = e, Rl = ({ onClose: l }) => (ue("ESCAPE", () => (l(), !0)), null), Ll = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.open === !0, s = t.closeOnBackdrop !== !1, c = t.child, i = Tl(null), u = Mt(() => {
    n("close");
  }, [n]), r = Mt((o) => {
    s && o.target === o.currentTarget && u();
  }, [s, u]);
  return a ? /* @__PURE__ */ e.createElement(yt, null, /* @__PURE__ */ e.createElement(Rl, { onClose: u }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: r,
      ref: i,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(Y, { control: c })
  )) : null;
}, { useEffect: xl, useRef: Ml } = e, Il = ({ controlId: l }) => {
  const n = X().dialogs ?? [], a = Ml(n.length);
  return xl(() => {
    n.length < a.current && n.length > 0, a.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((s) => /* @__PURE__ */ e.createElement(Y, { key: s.controlId, control: s })));
}, { useCallback: Je, useRef: Ae, useState: et } = e, jl = ({ onClose: l }) => (ue("ESCAPE", () => (l(), !0)), null), Pl = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Al = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Bl = ({ controlId: l }) => {
  const t = X(), n = le(), a = se(Pl), s = t.title ?? "", c = t.width ?? "32rem", i = t.height ?? null, u = t.minHeight ?? null, r = t.resizable === !0, o = t.child, d = t.actions ?? [], m = t.toolbar, f = t.buttonBar, [E, h] = et(null), [v, _] = et(null), [g, y] = et(null), L = Ae(null), [T, w] = et(!1), k = Ae(null), b = Ae(null), j = Ae(null), S = Ae(null), R = Ae(null), U = Je(() => {
    n("close");
  }, [n]);
  wt(!0, S, "field");
  const H = Je((M, B) => {
    B.preventDefault();
    const O = S.current;
    if (!O) return;
    const K = O.getBoundingClientRect(), p = !L.current, D = L.current ?? { x: K.left, y: K.top };
    p && (L.current = D, y(D)), R.current = {
      dir: M,
      startX: B.clientX,
      startY: B.clientY,
      startW: K.width,
      startH: K.height,
      startPos: { ...D },
      symmetric: p
    };
    const V = (Z) => {
      const $ = R.current;
      if (!$) return;
      const J = Z.clientX - $.startX, ae = Z.clientY - $.startY;
      let ne = $.startW, pe = $.startH, ge = 0, Ee = 0;
      $.symmetric ? ($.dir.includes("e") && (ne = $.startW + 2 * J), $.dir.includes("w") && (ne = $.startW - 2 * J), $.dir.includes("s") && (pe = $.startH + 2 * ae), $.dir.includes("n") && (pe = $.startH - 2 * ae)) : ($.dir.includes("e") && (ne = $.startW + J), $.dir.includes("w") && (ne = $.startW - J, ge = J), $.dir.includes("s") && (pe = $.startH + ae), $.dir.includes("n") && (pe = $.startH - ae, Ee = ae));
      const we = Math.max(200, ne), ke = Math.max(100, pe);
      $.symmetric ? (ge = ($.startW - we) / 2, Ee = ($.startH - ke) / 2) : ($.dir.includes("w") && we === 200 && (ge = $.startW - 200), $.dir.includes("n") && ke === 100 && (Ee = $.startH - 100)), b.current = we, j.current = ke, h(we), _(ke);
      const Ie = {
        x: $.startPos.x + ge,
        y: $.startPos.y + Ee
      };
      L.current = Ie, y(Ie);
    }, z = () => {
      document.removeEventListener("mousemove", V), document.removeEventListener("mouseup", z);
      const Z = b.current, $ = j.current;
      (Z != null || $ != null) && n("resize", {
        ...Z != null ? { width: Math.round(Z) } : {},
        ...$ != null ? { height: Math.round($) } : {}
      }), R.current = null;
    };
    document.addEventListener("mousemove", V), document.addEventListener("mouseup", z);
  }, [n]), C = Je((M) => {
    if (M.button !== 0 || M.target.closest("button")) return;
    M.preventDefault();
    const B = S.current;
    if (!B) return;
    const O = B.getBoundingClientRect(), K = L.current ?? { x: O.left, y: O.top }, p = M.clientX - K.x, D = M.clientY - K.y, V = (Z) => {
      const $ = window.innerWidth, J = window.innerHeight;
      let ae = Z.clientX - p, ne = Z.clientY - D;
      const pe = B.offsetWidth, ge = B.offsetHeight;
      ae + pe > $ && (ae = $ - pe), ne + ge > J && (ne = J - ge), ae < 0 && (ae = 0), ne < 0 && (ne = 0);
      const Ee = { x: ae, y: ne };
      L.current = Ee, y(Ee);
    }, z = () => {
      document.removeEventListener("mousemove", V), document.removeEventListener("mouseup", z);
    };
    document.addEventListener("mousemove", V), document.addEventListener("mouseup", z);
  }, []), x = Je(() => {
    var M, B;
    if (T) {
      const O = k.current;
      O && (y(O.x !== -1 ? { x: O.x, y: O.y } : null), h(O.w), _(O.h)), w(!1);
    } else {
      const O = S.current, K = O == null ? void 0 : O.getBoundingClientRect();
      k.current = {
        x: ((M = L.current) == null ? void 0 : M.x) ?? (K == null ? void 0 : K.left) ?? -1,
        y: ((B = L.current) == null ? void 0 : B.y) ?? (K == null ? void 0 : K.top) ?? -1,
        w: E ?? (K == null ? void 0 : K.width) ?? null,
        h: v ?? null
      }, w(!0), y({ x: 0, y: 0 }), h(null), _(null);
    }
  }, [T, E, v]), P = T ? { position: "absolute", top: 0, left: 0, width: "100vw", maxWidth: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: E != null ? E + "px" : c,
    ...v != null ? { height: v + "px" } : i != null ? { height: i } : {},
    ...u != null && v == null ? { minHeight: u } : {},
    maxHeight: g ? "100vh" : "80vh",
    ...g ? { position: "absolute", left: g.x + "px", top: g.y + "px" } : {}
  }, I = l + "-title";
  return /* @__PURE__ */ e.createElement(yt, { modal: !0 }, /* @__PURE__ */ e.createElement(jl, { onClose: U }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: P,
      ref: S,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": I
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${T ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: T ? void 0 : C,
        onDoubleClick: r ? x : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: I }, s),
      m && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(Y, { control: m })),
      r && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: x,
          title: T ? a["js.window.restore"] : a["js.window.maximize"]
        },
        T ? (
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(Y, { control: o })),
    (d.length > 0 || f) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, f && /* @__PURE__ */ e.createElement(Y, { control: f }), d.map((M, B) => /* @__PURE__ */ e.createElement(Y, { key: B, control: M }))),
    r && !T && Al.map((M) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: M,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${M}`,
        onMouseDown: (B) => H(M, B)
      }
    ))
  ));
}, { useCallback: Ol } = e, Fl = {
  "js.drawer.close": "Close"
}, $l = ({ controlId: l }) => {
  const t = X(), n = le(), a = se(Fl), s = t.open === !0, c = t.position ?? "right", i = t.size ?? "medium", u = t.title ?? null, r = t.child, o = Ol(() => {
    n("close");
  }, [n]);
  Me(s, { ESCAPE: o });
  const d = [
    "tlDrawer",
    `tlDrawer--${c}`,
    `tlDrawer--${i}`,
    s ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: l, className: d, "aria-hidden": !s }, u !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, u), /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, r && /* @__PURE__ */ e.createElement(Y, { control: r })));
}, { useCallback: Hl } = e, Wl = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.child, s = Hl((c) => {
    c.preventDefault(), c.stopPropagation(), n("openContextMenu", { x: c.clientX, y: c.clientY });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tl-context-menu-region", onContextMenu: s }, a && /* @__PURE__ */ e.createElement(Y, { control: a }));
}, { useCallback: Ul, useEffect: zl, useState: Vl } = e, Kl = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.message ?? "", s = t.content ?? "", c = t.variant ?? "info", i = t.duration ?? 5e3, u = t.visible === !0, r = t.generation ?? 0, [o, d] = Vl(!1), m = Ul(() => {
    d(!0), setTimeout(() => {
      n("dismiss", { generation: r }), d(!1);
    }, 200);
  }, [n, r]);
  return zl(() => {
    if (!u || i === 0) return;
    const f = setTimeout(m, i);
    return () => clearTimeout(f);
  }, [u, i, m]), !u && !o ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${c}${o ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    s ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: s } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, a)
  );
}, { useCallback: ut, useEffect: It, useRef: Yl, useState: jt } = e, Gl = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.open === !0, s = t.anchorId, c = t.anchorX, i = t.anchorY, u = t.items ?? [], r = Yl(null), [o, d] = jt({ top: 0, left: 0 }), [m, f] = jt(0), E = u.filter((g) => g.type === "item" && !g.disabled);
  It(() => {
    var b, j;
    if (!a) return;
    const g = ((b = r.current) == null ? void 0 : b.offsetHeight) ?? 200, y = ((j = r.current) == null ? void 0 : j.offsetWidth) ?? 200;
    if (c != null && i != null) {
      let S = i, R = c;
      S + g > window.innerHeight && (S = Math.max(0, window.innerHeight - g)), R + y > window.innerWidth && (R = Math.max(0, window.innerWidth - y)), d({ top: S, left: R }), f(0);
      return;
    }
    if (!s) return;
    const L = document.getElementById(s);
    if (!L) return;
    const T = L.getBoundingClientRect();
    let w = T.bottom + 4, k = T.left;
    w + g > window.innerHeight && (w = T.top - g - 4), k + y > window.innerWidth && (k = T.right - y), d({ top: w, left: k }), f(0);
  }, [a, s, c, i]);
  const h = ut(() => {
    n("close");
  }, [n]), v = ut((g) => {
    n("selectItem", { itemId: g });
  }, [n]);
  It(() => {
    if (!a) return;
    const g = (y) => {
      r.current && !r.current.contains(y.target) && h();
    };
    return document.addEventListener("mousedown", g), () => document.removeEventListener("mousedown", g);
  }, [a, h]);
  const _ = ut((g) => {
    if (g.key === "Escape") {
      g.preventDefault(), h();
      return;
    }
    if (g.key === "ArrowDown")
      g.preventDefault(), f((y) => (y + 1) % E.length);
    else if (g.key === "ArrowUp")
      g.preventDefault(), f((y) => (y - 1 + E.length) % E.length);
    else if (g.key === "Enter" || g.key === " ") {
      g.preventDefault();
      const y = E[m];
      y && v(y.id);
    }
  }, [h, v, E, m]);
  return wt(a, r), a ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: r,
      tabIndex: -1,
      style: { position: "fixed", top: o.top, left: o.left },
      onKeyDown: _
    },
    u.map((g, y) => {
      if (g.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: y, className: "tlMenu__separator" });
      const T = E.indexOf(g) === m;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: g.id,
          type: "button",
          className: "tlMenu__item" + (T ? " tlMenu__item--focused" : "") + (g.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: g.disabled,
          tabIndex: T ? 0 : -1,
          onClick: () => v(g.id)
        },
        g.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + g.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, g.label)
      );
    })
  ) : null;
}, Xl = 768, ql = ({ controlId: l }) => {
  const t = X(), n = le();
  e.useEffect(() => {
    const o = window.matchMedia(`(max-width: ${Xl}px)`), d = (f) => {
      n("reportDisplayClass", { displayClass: f ? "COMPACT" : "REGULAR" });
    };
    d(o.matches);
    const m = (f) => d(f.matches);
    return o.addEventListener("change", m), () => o.removeEventListener("change", m);
  }, [n]);
  const a = t.header, s = t.content, c = t.footer, i = t.snackbar, u = t.dialogManager, r = t.menuOverlay;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(Y, { control: a })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(Y, { control: s })), c && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(Y, { control: c })), /* @__PURE__ */ e.createElement(Y, { control: i }), u && /* @__PURE__ */ e.createElement(Y, { control: u }), r && /* @__PURE__ */ e.createElement(Y, { control: r }));
}, Zl = ({ controlId: l }) => {
  const t = X(), n = t.text ?? "", a = t.cssClass ?? "", s = t.hasTooltip === !0, c = a ? `tlText ${a}` : "tlText";
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: c,
      "data-tooltip": s ? "key:tooltip" : void 0
    },
    n
  );
}, Ql = ({ isMulti: l, cursorIndex: t, onMove: n, onToggle: a, onSelectAll: s }) => (ue("ArrowUp", () => (n("up", !1, !1), !0)), ue("ArrowDown", () => (n("down", !1, !1), !0)), ue("Home", () => (n("home", !1, !1), !0)), ue("End", () => (n("end", !1, !1), !0)), ue("PageUp", () => (n("pageUp", !1, !1), !0)), ue("PageDown", () => (n("pageDown", !1, !1), !0)), ue("Shift+ArrowUp", () => (n("up", l, !1), !0)), ue("Shift+ArrowDown", () => (n("down", l, !1), !0)), ue("Shift+Home", () => (n("home", l, !1), !0)), ue("Shift+End", () => (n("end", l, !1), !0)), ue("Shift+PageUp", () => (n("pageUp", l, !1), !0)), ue("Shift+PageDown", () => (n("pageDown", l, !1), !0)), ue("Ctrl+ArrowUp", () => (n("up", !1, l), !0)), ue("Ctrl+ArrowDown", () => (n("down", !1, l), !0)), ue("Space", () => t < 0 ? !1 : (a(), !0)), ue("Ctrl+A", () => l ? (s(), !0) : !1), null), Jl = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all",
  "js.table.filter": "Filter"
}, Pt = 50;
function At(l) {
  var n;
  const t = l.target;
  return !!((n = t == null ? void 0 : t.closest) != null && n.call(t, 'input, textarea, select, button, a, [contenteditable="true"]'));
}
const ea = ({ controlId: l }) => {
  const t = X(), n = le(), a = se(Jl), s = e.useRef(null);
  e.useEffect(() => {
    const N = s.current;
    if (!N) return;
    const A = (q) => {
      const ee = q.detail;
      let re = ee.target;
      for (; re && re !== N; ) {
        const fe = re.dataset.row, ce = re.dataset.col;
        if (fe != null && ce != null) {
          ee.resolved = { key: fe + "|" + ce };
          return;
        }
        re = re.parentElement;
      }
    };
    return N.addEventListener("tl-tooltip-resolve", A), () => N.removeEventListener("tl-tooltip-resolve", A);
  }, []);
  const c = t.columns ?? [], i = t.totalRowCount ?? 0, u = t.rows ?? [], r = t.rowHeight ?? 36, o = t.selectionMode ?? "single", d = t.selectedCount ?? 0, m = t.cursorIndex ?? -1, f = t.frozenColumnCount ?? 0, E = t.treeMode ?? !1, h = e.useMemo(
    () => c.filter((N) => N.sortPriority && N.sortPriority > 0).length,
    [c]
  ), v = o === "multi", _ = 40, g = 20, y = e.useRef(null), L = e.useRef(null), T = e.useRef(null), [w, k] = e.useState({}), b = e.useRef(null), j = e.useRef(!1), S = e.useRef(null), [R, U] = e.useState(null), [H, C] = e.useState(null);
  e.useEffect(() => {
    b.current || k({});
  }, [c]);
  const x = e.useCallback((N) => w[N.name] ?? N.width, [w]), P = e.useMemo(() => {
    const N = [];
    let A = v && f > 0 ? _ : 0;
    for (let q = 0; q < f && q < c.length; q++)
      N.push(A), A += x(c[q]);
    return N;
  }, [c, f, v, _, x]), I = i * r, M = e.useRef(null), B = e.useCallback((N, A, q) => {
    q.preventDefault(), q.stopPropagation(), b.current = { column: N, startX: q.clientX, startWidth: A };
    let ee = q.clientX, re = 0;
    const fe = () => {
      const ie = b.current;
      if (!ie) return;
      const ve = Math.max(Pt, ie.startWidth + (ee - ie.startX) + re);
      k((Pe) => ({ ...Pe, [ie.column]: ve }));
    }, ce = () => {
      const ie = L.current, ve = y.current;
      if (!ie || !b.current) return;
      const Pe = ie.getBoundingClientRect(), Nt = 40, St = 8, nn = ie.scrollLeft;
      ee > Pe.right - Nt ? ie.scrollLeft += St : ee < Pe.left + Nt && (ie.scrollLeft = Math.max(0, ie.scrollLeft - St));
      const Dt = ie.scrollLeft - nn;
      Dt !== 0 && (ve && (ve.scrollLeft = ie.scrollLeft), re += Dt, fe()), M.current = requestAnimationFrame(ce);
    };
    M.current = requestAnimationFrame(ce);
    const je = (ie) => {
      ee = ie.clientX, fe();
    }, Ze = (ie) => {
      document.removeEventListener("mousemove", je), document.removeEventListener("mouseup", Ze), M.current !== null && (cancelAnimationFrame(M.current), M.current = null);
      const ve = b.current;
      if (ve) {
        const Pe = Math.max(Pt, ve.startWidth + (ie.clientX - ve.startX) + re);
        n("columnResize", { column: ve.column, width: Pe }), b.current = null, j.current = !0, requestAnimationFrame(() => {
          j.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", je), document.addEventListener("mouseup", Ze);
  }, [n]), O = e.useCallback(() => {
    y.current && L.current && (y.current.scrollLeft = L.current.scrollLeft), T.current !== null && clearTimeout(T.current), T.current = window.setTimeout(() => {
      const N = L.current;
      if (!N) return;
      const A = N.scrollTop, q = Math.ceil(N.clientHeight / r), ee = Math.floor(A / r);
      n("scroll", { start: ee, count: q });
    }, 80);
  }, [n, r]), K = e.useCallback((N, A, q) => {
    if (j.current) return;
    let ee;
    !A || A === "desc" ? ee = "asc" : ee = "desc";
    const re = q.shiftKey ? "add" : "replace";
    n("sort", { column: N, direction: ee, mode: re });
  }, [n]), p = e.useCallback((N, A) => {
    S.current = N, A.dataTransfer.effectAllowed = "move", A.dataTransfer.setData("text/plain", N);
  }, []), D = e.useCallback((N, A) => {
    if (!S.current || S.current === N) {
      U(null);
      return;
    }
    A.preventDefault(), A.dataTransfer.dropEffect = "move";
    const q = A.currentTarget.getBoundingClientRect(), ee = A.clientX < q.left + q.width / 2 ? "left" : "right";
    U({ column: N, side: ee });
  }, []), V = e.useCallback((N) => {
    N.preventDefault(), N.stopPropagation();
    const A = S.current;
    if (!A || !R) {
      S.current = null, U(null);
      return;
    }
    let q = c.findIndex((re) => re.name === R.column);
    if (q < 0) {
      S.current = null, U(null);
      return;
    }
    const ee = c.findIndex((re) => re.name === A);
    R.side === "right" && q++, ee < q && q--, n("columnReorder", { column: A, targetIndex: q }), S.current = null, U(null);
  }, [c, R, n]), z = e.useCallback(() => {
    S.current = null, U(null);
  }, []), Z = e.useCallback((N, A) => {
    var ee;
    const q = window.getSelection();
    q && !q.isCollapsed && A.currentTarget.contains(q.anchorNode) || (At(A) || (ee = L.current) == null || ee.focus({ preventScroll: !0 }), n("select", {
      rowIndex: N,
      ctrlKey: A.ctrlKey || A.metaKey,
      shiftKey: A.shiftKey
    }));
  }, [n]), $ = e.useCallback((N, A, q) => {
    n("moveSelection", { direction: N, extend: A, move: q });
  }, [n]), J = e.useCallback(() => {
    m < 0 || n("select", { rowIndex: m, ctrlKey: v, shiftKey: !1 });
  }, [n, m, v]), ae = e.useCallback(() => {
    n("selectAll", { selected: !0 });
  }, [n]), ne = e.useCallback(
    () => !!s.current && s.current.contains(document.activeElement),
    []
  );
  e.useEffect(() => {
    if (m < 0)
      return;
    const N = L.current;
    if (!N)
      return;
    const A = m * r, q = A + r;
    A < N.scrollTop ? N.scrollTop = A : q > N.scrollTop + N.clientHeight && (N.scrollTop = q - N.clientHeight);
  }, [m, r]);
  const pe = e.useCallback((N, A) => {
    A.stopPropagation(), n("select", { rowIndex: N, ctrlKey: !0, shiftKey: !1 });
  }, [n]), ge = e.useCallback(() => {
    const N = d === i && i > 0;
    n("selectAll", { selected: !N });
  }, [n, d, i]), Ee = e.useCallback((N, A, q) => {
    q.stopPropagation(), n("expand", { rowIndex: N, expanded: A });
  }, [n]), we = e.useCallback((N, A) => {
    A.preventDefault(), C({ x: A.clientX, y: A.clientY, colIdx: N });
  }, []), ke = e.useCallback(() => {
    H && (n("setFrozenColumnCount", { count: H.colIdx + 1 }), C(null));
  }, [H, n]), Ie = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), C(null);
  }, [n]);
  e.useEffect(() => {
    if (!H) return;
    const N = () => C(null);
    return document.addEventListener("mousedown", N), () => document.removeEventListener("mousedown", N);
  }, [H]), Me(!!H, { ESCAPE: () => C(null) });
  const qe = e.useCallback((N, A) => {
    A.stopPropagation(), A.preventDefault(), n("openFilter", { column: N });
  }, [n]), F = c.reduce((N, A) => N + x(A), 0) + (v ? _ : 0), G = d === i && i > 0, te = d > 0 && d < i, oe = e.useCallback((N) => {
    N && (N.indeterminate = te);
  }, [te]);
  return /* @__PURE__ */ e.createElement(yt, { active: ne }, /* @__PURE__ */ e.createElement(
    Ql,
    {
      isMulti: v,
      cursorIndex: m,
      onMove: $,
      onToggle: J,
      onSelectAll: ae
    }
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: s,
      id: l,
      className: "tlTableView",
      "data-tooltip": "dynamic",
      onDragOver: (N) => {
        if (!S.current) return;
        N.preventDefault();
        const A = L.current, q = y.current;
        if (!A) return;
        const ee = A.getBoundingClientRect(), re = 40, fe = 8;
        N.clientX < ee.left + re ? A.scrollLeft = Math.max(0, A.scrollLeft - fe) : N.clientX > ee.right - re && (A.scrollLeft += fe), q && (q.scrollLeft = A.scrollLeft);
      },
      onDrop: V
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: y }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { width: F } }, v && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (f > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: _,
          minWidth: _,
          ...f > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (N) => {
          S.current && (N.preventDefault(), N.dataTransfer.dropEffect = "move", c.length > 0 && c[0].name !== S.current && U({ column: c[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: oe,
          className: "tlTableView__checkbox",
          checked: G,
          onChange: ge
        }
      )
    ), c.map((N, A) => {
      const q = x(N);
      c.length - 1;
      let ee = "tlTableView__headerCell";
      N.sortable && (ee += " tlTableView__headerCell--sortable"), R && R.column === N.name && (ee += " tlTableView__headerCell--dragOver-" + R.side);
      const re = A < f, fe = A === f - 1;
      return re && (ee += " tlTableView__headerCell--frozen"), fe && (ee += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: N.name,
          className: ee,
          style: {
            width: q,
            minWidth: q,
            position: re ? "sticky" : "relative",
            ...re ? { left: P[A], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: N.sortable ? (ce) => K(N.name, N.sortDirection, ce) : void 0,
          onContextMenu: (ce) => we(A, ce),
          onDragStart: (ce) => p(N.name, ce),
          onDragOver: (ce) => D(N.name, ce),
          onDrop: V,
          onDragEnd: z
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, N.label),
        N.filterable && /* @__PURE__ */ e.createElement(
          "button",
          {
            type: "button",
            className: "tlTableView__filterButton" + (N.filterActive ? " tlTableView__filterButton--active" : ""),
            title: a["js.table.filter"],
            style: {
              border: "none",
              background: "transparent",
              cursor: "pointer",
              padding: "0 4px",
              color: N.filterActive ? "#1565c0" : "inherit"
            },
            onMouseDown: (ce) => ce.stopPropagation(),
            onClick: (ce) => qe(N.name, ce)
          },
          /* @__PURE__ */ e.createElement("i", { className: N.filterActive ? "bi bi-funnel-fill" : "bi bi-funnel" })
        ),
        N.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, N.sortDirection === "asc" ? "▲" : "▼", h > 1 && N.sortPriority != null && N.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, N.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (ce) => B(N.name, q, ce)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (N) => {
          if (S.current && c.length > 0) {
            const A = c[c.length - 1];
            A.name !== S.current && (N.preventDefault(), N.dataTransfer.dropEffect = "move", U({ column: A.name, side: "right" }));
          }
        },
        onDrop: V
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: L,
        className: "tlTableView__body",
        onScroll: O,
        tabIndex: 0
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: I, position: "relative", width: F } }, u.map((N) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: N.id,
          className: "tlTableView__row" + (N.selected ? " tlTableView__row--selected" : "") + (N.index === m ? " tlTableView__row--cursor" : ""),
          style: {
            position: "absolute",
            top: N.index * r,
            height: r,
            width: F,
            ...N.index === m ? { outline: "2px solid var(--color-primary, #1a73e8)", outlineOffset: "-2px" } : {}
          },
          onMouseDown: (A) => {
            (A.shiftKey || A.ctrlKey || A.metaKey || A.detail > 1) && !At(A) && A.preventDefault();
          },
          onClick: (A) => Z(N.index, A)
        },
        v && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (f > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: _,
              minWidth: _,
              ...f > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (A) => A.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: N.selected,
              onChange: () => {
              },
              onClick: (A) => pe(N.index, A),
              tabIndex: -1
            }
          )
        ),
        c.map((A, q) => {
          const ee = x(A), re = q === c.length - 1, fe = q < f, ce = q === f - 1;
          let je = "tlTableView__cell";
          fe && (je += " tlTableView__cell--frozen"), ce && (je += " tlTableView__cell--frozenLast");
          const Ze = E && q === 0, ie = N.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: A.name,
              className: je,
              "data-row": N.id,
              "data-col": A.name,
              style: {
                ...re && !fe ? { flex: "1 0 auto", minWidth: ee } : { width: ee, minWidth: ee },
                ...fe ? { position: "sticky", left: P[q], zIndex: 2 } : {}
              }
            },
            Ze ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ie * g } }, N.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (ve) => Ee(N.index, !N.expanded, ve)
              },
              N.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(Y, { control: N.cells[A.name] })) : /* @__PURE__ */ e.createElement(Y, { control: N.cells[A.name] })
          );
        })
      )))
    ),
    H && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: H.y, left: H.x, zIndex: 1e4 },
        onMouseDown: (N) => N.stopPropagation()
      },
      H.colIdx + 1 !== f && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: ke }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      f > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Ie }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
    )
  ));
}, { useState: Bt, useRef: dt, useCallback: ta, useMemo: $e, useEffect: Ot } = e, na = {
  "js.calendar.today": "Today",
  "js.calendar.previous": "Previous",
  "js.calendar.next": "Next",
  "js.calendar.day": "Day",
  "js.calendar.workWeek": "Work week",
  "js.calendar.week": "Week",
  "js.calendar.month": "Month",
  "js.calendar.year": "Year",
  "js.calendar.allDay": "All day",
  "js.calendar.more": "more"
}, be = 44, ot = 15, Se = 6e4, la = 36e5, De = 864e5, aa = 8;
function ye(l) {
  const t = new Date(l);
  return t.setHours(0, 0, 0, 0), t.getTime();
}
function He(l, t) {
  const n = new Date(l);
  return n.setDate(n.getDate() + t), n.getTime();
}
function ra(l) {
  return ye(l);
}
function Xe(l, t) {
  return ye(l) === ye(t);
}
function Le(l) {
  return (l - ye(l)) / Se;
}
function Ve(l) {
  return Math.round(l / ot) * ot;
}
function Ke(l, t, n) {
  return Math.max(t, Math.min(n, l));
}
function st(l) {
  if (!l)
    return "tlCalEvent--default";
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return "tlCalEvent--c" + Math.abs(t) % aa;
}
function oa(l) {
  return Array.isArray(l) ? l.map((t) => ({
    id: t.id,
    start: t.start,
    end: t.end,
    allDay: t.allDay === !0,
    title: t.title ?? "",
    tooltip: t.tooltip,
    category: t.category,
    movable: t.movable === !0,
    resizable: t.resizable === !0,
    selected: t.selected === !0
  })) : [];
}
function xe(l, t, n) {
  return new Intl.DateTimeFormat(l, t).format(new Date(n));
}
function sa(l, t) {
  const n = { hour: "numeric", minute: "2-digit" };
  return xe(l, n, t.start) + "–" + xe(l, n, t.end);
}
const ca = [
  { key: "DAY", label: "js.calendar.day" },
  { key: "WORK_WEEK", label: "js.calendar.workWeek" },
  { key: "WEEK", label: "js.calendar.week" },
  { key: "MONTH", label: "js.calendar.month" },
  { key: "YEAR", label: "js.calendar.year" }
], ia = ({ title: l, granularity: t, i18n: n, send: a }) => /* @__PURE__ */ e.createElement("div", { className: "tlCalToolbar" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalNav" }, /* @__PURE__ */ e.createElement("button", { className: "tlCalBtn", onClick: () => a("navigate", { direction: "TODAY" }) }, n["js.calendar.today"]), /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlCalBtn tlCalBtn--icon",
    "aria-label": n["js.calendar.previous"],
    onClick: () => a("navigate", { direction: "PREV" })
  },
  /* @__PURE__ */ e.createElement("span", { className: "bi bi-chevron-left" })
), /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlCalBtn tlCalBtn--icon",
    "aria-label": n["js.calendar.next"],
    onClick: () => a("navigate", { direction: "NEXT" })
  },
  /* @__PURE__ */ e.createElement("span", { className: "bi bi-chevron-right" })
)), /* @__PURE__ */ e.createElement("div", { className: "tlCalTitle" }, l), /* @__PURE__ */ e.createElement("div", { className: "tlCalGranularity" }, ca.map((s) => /* @__PURE__ */ e.createElement(
  "button",
  {
    key: s.key,
    className: "tlCalBtn" + (s.key === t ? " tlCalBtn--active" : ""),
    onClick: () => a("switchGranularity", { granularity: s.key })
  },
  n[s.label]
))));
function ua(l) {
  const t = [...l].sort((i, u) => i.start - u.start || u.end - i.end), n = [];
  let a = [], s = -1;
  const c = () => {
    const i = a.reduce((u, r) => Math.max(u, r.col + 1), 0);
    for (const u of a)
      u.cols = i;
    n.push(...a), a = [], s = -1;
  };
  for (const i of t) {
    a.length > 0 && i.start >= s && c();
    const u = new Set(a.filter((o) => o.ev.end > i.start).map((o) => o.col));
    let r = 0;
    for (; u.has(r); )
      r++;
    a.push({
      ev: i,
      topMin: Le(i.start),
      botMin: Le(i.start) + Math.max(15, (i.end - i.start) / Se),
      col: r,
      cols: 1
    }), s = Math.max(s, i.end);
  }
  return a.length > 0 && c(), n;
}
const da = ({
  ctx: l,
  rangeStart: t,
  granularity: n
}) => {
  const { events: a, locale: s, nonWorkingDays: c, dayStartHour: i, dayEndHour: u, now: r, send: o, editable: d, i18n: m } = l, f = $e(() => {
    const C = n === "DAY" ? 1 : 7, x = [];
    for (let P = 0; P < C; P++) {
      const I = He(t, P);
      n === "WORK_WEEK" && c.includes(new Date(I).getDay()) || x.push(I);
    }
    return x;
  }, [t, n, c]), E = dt(null), h = dt(null), [v, _] = Bt(null), g = dt(null);
  g.current = v;
  const [y, L] = Bt(Date.now());
  Ot(() => {
    const C = window.setInterval(() => L(Date.now()), 6e4);
    return () => window.clearInterval(C);
  }, []);
  const T = ta(
    (C, x) => {
      const P = E.current;
      if (!P)
        return { dayIndex: 0, min: 0 };
      const I = P.getBoundingClientRect(), M = I.width / f.length, B = Ke(Math.floor((C - I.left) / M), 0, f.length - 1), O = x - I.top + P.scrollTop, K = Ke(O / be * 60, 0, 1440);
      return { dayIndex: B, min: K };
    },
    [f.length]
  );
  Ot(() => {
    if (!v)
      return;
    const C = (P) => {
      const I = g.current;
      if (!I)
        return;
      const { dayIndex: M, min: B } = T(P.clientX, P.clientY);
      I.mode === "move" ? _({ ...I, dayStart: f[M], startMin: Ke(Ve(B - I.grabMin), 0, 1440 - I.dur) }) : I.mode === "resize" ? _({ ...I, endMin: Ke(Ve(B), I.startMin + ot, 1440) }) : _({ ...I, toMin: Ke(Ve(B), 0, 1440) });
    }, x = () => {
      const P = g.current;
      if (_(null), !!P)
        if (P.mode === "move") {
          const I = P.dayStart + P.startMin * Se;
          I !== P.origStartMs && o("moveEvent", { eventId: P.id, start: I, end: I + P.dur * Se });
        } else if (P.mode === "resize") {
          const I = P.dayStart + P.endMin * Se;
          I !== P.origEndMs && o("resizeEvent", { eventId: P.id, end: I });
        } else {
          const I = Math.min(P.fromMin, P.toMin), M = Math.max(P.fromMin, P.toMin);
          M - I >= ot && o("createSlot", { start: P.dayStart + I * Se, end: P.dayStart + M * Se, allDay: !1 });
        }
    };
    return window.addEventListener("pointermove", C), window.addEventListener("pointerup", x, { once: !0 }), () => {
      window.removeEventListener("pointermove", C), window.removeEventListener("pointerup", x);
    };
  }, [v, f, T, o]);
  const w = (C, x, P) => {
    if (!d || !x.movable)
      return;
    C.stopPropagation();
    const { min: I } = T(C.clientX, C.clientY), M = (x.end - x.start) / Se;
    _({
      mode: "move",
      id: x.id,
      grabMin: I - Le(x.start),
      dur: M,
      dayStart: P,
      startMin: Le(x.start),
      origStartMs: x.start
    });
  }, k = (C, x, P) => {
    !d || !x.resizable || (C.stopPropagation(), _({
      mode: "resize",
      id: x.id,
      dayStart: P,
      startMin: Le(x.start),
      endMin: Le(x.end),
      origEndMs: x.end
    }));
  }, b = (C, x) => {
    if (!d || C.button !== 0)
      return;
    const { min: P } = T(C.clientX, C.clientY);
    _({ mode: "create", dayStart: x, fromMin: Ve(P), toMin: Ve(P) });
  }, j = Array.from({ length: 24 }, (C, x) => x), S = $e(() => f.map(
    (C) => ua(
      a.filter((x) => !x.allDay && x.start < C + De && x.end > C)
    )
  ), [f, a]), R = $e(() => f.map((C) => a.filter((x) => x.allDay && x.start < C + De && x.end > C)), [f, a]), U = i * be, H = u * be;
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeGrid" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeHeader" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalGutter" }), f.map((C) => {
    const x = c.includes(new Date(C).getDay()), P = Xe(C, l.now);
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: C,
        className: "tlCalDayHead" + (x ? " tlCalDayHead--nonworking" : "") + (P ? " tlCalDayHead--today" : ""),
        onClick: () => o("goto", { date: C, granularity: "DAY" })
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlCalDayName" }, xe(s, { weekday: "short" }, C)),
      /* @__PURE__ */ e.createElement("span", { className: "tlCalDayNum" }, new Date(C).getDate())
    );
  })), /* @__PURE__ */ e.createElement("div", { className: "tlCalAllDayRow" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalGutter tlCalAllDayLabel" }, m["js.calendar.allDay"]), f.map((C, x) => /* @__PURE__ */ e.createElement(
    "div",
    {
      key: C,
      className: "tlCalAllDayCell",
      onClick: () => d && o("createSlot", { start: C, end: C + De, allDay: !0 })
    },
    R[x].map((P) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: P.id,
        className: "tlCalAllDayEvent " + st(P.category) + (P.selected ? " tlCalEvent--selected" : ""),
        title: P.tooltip,
        onClick: (I) => {
          I.stopPropagation(), o("selectEvent", { eventId: P.id });
        }
      },
      P.title
    ))
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlCalScroll", ref: h }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeBody", style: { height: 24 * be } }, /* @__PURE__ */ e.createElement("div", { className: "tlCalHourAxis" }, j.map((C) => /* @__PURE__ */ e.createElement("div", { key: C, className: "tlCalHourLabel", style: { top: C * be } }, C === 0 ? "" : xe(s, { hour: "numeric" }, ye(t) + C * la)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalColumns", ref: E, style: { gridTemplateColumns: `repeat(${f.length}, 1fr)` } }, f.map((C, x) => {
    const P = c.includes(new Date(C).getDay()), I = v && ("dayStart" in v && v.dayStart === C) ? v : null;
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: C,
        className: "tlCalCol" + (P ? " tlCalCol--nonworking" : ""),
        onPointerDown: (M) => b(M, C)
      },
      j.map((M) => /* @__PURE__ */ e.createElement("div", { key: M, className: "tlCalHourLine", style: { top: M * be } })),
      /* @__PURE__ */ e.createElement("div", { className: "tlCalWorkBand", style: { top: U, height: H - U } }),
      Xe(C, y) && /* @__PURE__ */ e.createElement("div", { className: "tlCalNowLine", style: { top: Le(Date.now()) / 60 * be } }),
      S[x].map((M) => {
        const B = v && "id" in v && v.id === M.ev.id;
        let O = M.topMin / 60 * be, K = (M.botMin - M.topMin) / 60 * be;
        B && v && (v.mode === "move" && v.dayStart === C ? O = v.startMin / 60 * be : v.mode === "resize" && (K = (v.endMin - v.startMin) / 60 * be));
        const p = 100 / M.cols;
        return /* @__PURE__ */ e.createElement(
          "div",
          {
            key: M.ev.id,
            className: "tlCalEvent " + st(M.ev.category) + (M.ev.selected ? " tlCalEvent--selected" : "") + (B ? " tlCalEvent--dragging" : ""),
            style: { top: O, height: K, left: `${M.col * p}%`, width: `calc(${p}% - 2px)` },
            title: M.ev.tooltip,
            onPointerDown: (D) => w(D, M.ev, C),
            onClick: (D) => {
              D.stopPropagation(), o("selectEvent", { eventId: M.ev.id });
            }
          },
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTime" }, sa(s, M.ev)),
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTitle" }, M.ev.title),
          d && M.ev.resizable && /* @__PURE__ */ e.createElement("span", { className: "tlCalResizeHandle", onPointerDown: (D) => k(D, M.ev, C) })
        );
      }),
      I && I.mode === "create" && /* @__PURE__ */ e.createElement(
        "div",
        {
          className: "tlCalEvent tlCalEvent--preview",
          style: {
            top: Math.min(I.fromMin, I.toMin) / 60 * be,
            height: Math.abs(I.toMin - I.fromMin) / 60 * be
          }
        }
      )
    );
  })))));
}, ma = 3, pa = ({ ctx: l, rangeStart: t, anchorMonth: n }) => {
  const { events: a, locale: s, nonWorkingDays: c, send: i, editable: u, now: r } = l, o = $e(() => {
    const m = [];
    for (let f = 0; f < 6; f++) {
      const E = [];
      for (let h = 0; h < 7; h++)
        E.push(He(t, f * 7 + h));
      m.push(E);
    }
    return m;
  }, [t]), d = (m, f) => {
    m.preventDefault();
    const E = m.dataTransfer.getData("text/plain"), h = a.find((_) => _.id === E);
    if (!h || !u || !h.movable)
      return;
    const v = f - ye(h.start);
    i("moveEvent", { eventId: E, start: h.start + v, end: h.end + v });
  };
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalMonth" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthHead" }, o[0].map((m) => /* @__PURE__ */ e.createElement("div", { key: m, className: "tlCalMonthWeekday" }, xe(s, { weekday: "short" }, m)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthBody" }, o.map((m, f) => {
    const E = m[0], h = He(E, 7), v = a.filter((g) => (g.allDay || g.end - g.start >= De) && g.start < h && g.end > E).sort((g, y) => g.start - y.start).slice(0, 3), _ = v.length;
    return /* @__PURE__ */ e.createElement("div", { key: f, className: "tlCalMonthWeek" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthDays" }, m.map((g) => {
      const y = new Date(g).getMonth() === new Date(n).getMonth(), L = c.includes(new Date(g).getDay()), T = Xe(g, r);
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: g,
          className: "tlCalMonthCell" + (y ? "" : " tlCalMonthCell--other") + (L ? " tlCalMonthCell--nonworking" : ""),
          onDragOver: (w) => w.preventDefault(),
          onDrop: (w) => d(w, g),
          onClick: () => u && i("createSlot", { start: g, end: g + De, allDay: !0 })
        },
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlCalMonthDayNum" + (T ? " tlCalMonthDayNum--today" : ""),
            onClick: (w) => {
              w.stopPropagation(), i("goto", { date: g, granularity: "DAY" });
            }
          },
          new Date(g).getDate()
        )
      );
    })), /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthOverlay" }, v.map((g, y) => {
      const L = Math.max(0, Math.floor((ye(Math.max(g.start, E)) - E) / De)), T = Math.min(7, Math.ceil((g.end - E) / De));
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: g.id,
          className: "tlCalMonthBar " + st(g.category) + (g.selected ? " tlCalEvent--selected" : ""),
          style: { gridColumn: `${L + 1} / ${Math.max(L + 1, T) + 1}`, gridRow: y + 1 },
          draggable: u && g.movable,
          onDragStart: (w) => w.dataTransfer.setData("text/plain", g.id),
          title: g.tooltip,
          onClick: (w) => {
            w.stopPropagation(), i("selectEvent", { eventId: g.id });
          }
        },
        g.title
      );
    }), m.map((g, y) => {
      const L = a.filter((k) => !k.allDay && k.end - k.start < De && Xe(k.start, g)).sort((k, b) => k.start - b.start), T = L.slice(0, ma), w = L.length - T.length;
      return T.map((k, b) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: k.id,
          className: "tlCalChip " + st(k.category) + (k.selected ? " tlCalEvent--selected" : ""),
          style: { gridColumn: y + 1, gridRow: _ + 1 + b },
          draggable: u && k.movable,
          onDragStart: (j) => j.dataTransfer.setData("text/plain", k.id),
          title: k.tooltip,
          onClick: (j) => {
            j.stopPropagation(), i("selectEvent", { eventId: k.id });
          }
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipDot" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipTime" }, xe(s, { hour: "numeric", minute: "2-digit" }, k.start)),
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipTitle" }, k.title)
      )).concat(
        w > 0 ? [
          /* @__PURE__ */ e.createElement(
            "div",
            {
              key: "more-" + g,
              className: "tlCalMore",
              style: { gridColumn: y + 1, gridRow: _ + 1 + T.length },
              onClick: () => i("goto", { date: g, granularity: "DAY" })
            },
            "+",
            w,
            " ",
            l.i18n["js.calendar.more"]
          )
        ] : []
      );
    })));
  })));
}, fa = ({ ctx: l, rangeStart: t }) => {
  const { events: n, locale: a, firstDayOfWeek: s, nonWorkingDays: c, send: i, now: u } = l, r = $e(() => {
    const f = /* @__PURE__ */ new Set();
    for (const E of n) {
      let h = ye(E.start);
      const v = E.end;
      for (; h < v; )
        f.add(h), h = He(h, 1);
    }
    return f;
  }, [n]), o = new Date(t).getFullYear(), d = Array.from({ length: 12 }, (f, E) => new Date(o, E, 1).getTime()), m = $e(() => {
    const f = new Date(2023, 0, 1);
    return Array.from({ length: 7 }, (E, h) => {
      const v = new Date(f);
      return v.setDate(f.getDate() + (s + h) % 7), new Intl.DateTimeFormat(a, { weekday: "narrow" }).format(v);
    });
  }, [a, s]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalYear" }, d.map((f) => {
    const E = new Date(f), h = ye(He(f, -((E.getDay() - s + 7) % 7))), v = Array.from({ length: 42 }, (_, g) => He(h, g));
    return /* @__PURE__ */ e.createElement("div", { key: f, className: "tlCalMini" }, /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlCalMiniTitle",
        onClick: () => i("goto", { date: f, granularity: "MONTH" })
      },
      xe(a, { month: "long" }, f)
    ), /* @__PURE__ */ e.createElement("div", { className: "tlCalMiniGrid" }, m.map((_, g) => /* @__PURE__ */ e.createElement("div", { key: "h" + g, className: "tlCalMiniWd" }, _)), v.map((_) => {
      const g = new Date(_).getMonth() === E.getMonth(), y = c.includes(new Date(_).getDay()), L = Xe(_, u), T = r.has(ra(_));
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: _,
          className: "tlCalMiniDay" + (g ? "" : " tlCalMiniDay--other") + (y ? " tlCalMiniDay--nonworking" : "") + (L ? " tlCalMiniDay--today" : "") + (T ? " tlCalMiniDay--event" : ""),
          onClick: () => i("goto", { date: _, granularity: "DAY" })
        },
        new Date(_).getDate()
      );
    })));
  }));
}, ha = ({ controlId: l }) => {
  const t = X(), n = le(), a = se(na), s = t.granularity ?? "WEEK", c = t.rangeStart ?? Date.now(), i = t.anchor ?? c, u = t.title ?? "", r = {
    locale: t.locale ?? "en",
    firstDayOfWeek: t.firstDayOfWeek ?? 0,
    nonWorkingDays: t.nonWorkingDays ?? [0, 6],
    dayStartHour: t.dayStartHour ?? 8,
    dayEndHour: t.dayEndHour ?? 18,
    editable: t.editable !== !1,
    now: t.now ?? Date.now(),
    events: oa(t.events),
    send: n,
    i18n: a
  };
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCalendar" }, /* @__PURE__ */ e.createElement(ia, { title: u, granularity: s, i18n: a, send: n }), /* @__PURE__ */ e.createElement("div", { className: "tlCalBody" }, s === "MONTH" ? /* @__PURE__ */ e.createElement(pa, { ctx: r, rangeStart: c, anchorMonth: i }) : s === "YEAR" ? /* @__PURE__ */ e.createElement(fa, { ctx: r, rangeStart: c }) : /* @__PURE__ */ e.createElement(da, { ctx: r, rangeStart: c, granularity: s })));
}, ba = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, qt = e.createContext(ba), { useMemo: ga, useRef: Ea, useState: va, useEffect: _a } = e, Ca = 320, ya = ({ controlId: l }) => {
  const t = X(), n = t.maxColumns ?? 3, a = t.labelPosition ?? "auto", s = t.readOnly === !0, c = t.children ?? [], i = t.noModelMessage, u = Ea(null), [r, o] = va(
    a === "top" ? "top" : "side"
  );
  _a(() => {
    if (a !== "auto") {
      o(a);
      return;
    }
    const h = u.current;
    if (!h) return;
    const v = new ResizeObserver((_) => {
      for (const g of _) {
        const L = g.contentRect.width / n;
        o(L < Ca ? "top" : "side");
      }
    });
    return v.observe(h), () => v.disconnect();
  }, [a, n]);
  const d = ga(() => ({
    readOnly: s,
    resolvedLabelPosition: r
  }), [s, r]), f = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, E = [
    "tlFormLayout",
    s ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return i ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, i)) : /* @__PURE__ */ e.createElement(qt.Provider, { value: d }, /* @__PURE__ */ e.createElement("div", { id: l, className: E, style: f, ref: u }, c.map((h, v) => /* @__PURE__ */ e.createElement(Y, { key: v, control: h }))));
}, { useCallback: wa } = e, ka = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, Na = ({ controlId: l }) => {
  const t = X(), n = le(), a = se(ka), s = t.headerControl ?? null, c = t.headerActions ?? [], i = t.collapsible === !0, u = t.collapsed === !0, r = t.border ?? "none", o = t.fullLine === !0, d = t.children ?? [], m = s != null || c.length > 0 || i, f = wa(() => {
    n("toggleCollapse");
  }, [n]), E = [
    "tlFormGroup",
    `tlFormGroup--border-${r}`,
    o ? "tlFormGroup--fullLine" : "",
    u ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: E }, m && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, i && /* @__PURE__ */ e.createElement(
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
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(Y, { control: s })), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, c.map((h, v) => /* @__PURE__ */ e.createElement(Y, { key: v, control: h })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, d.map((h, v) => /* @__PURE__ */ e.createElement(Y, { key: v, control: h }))));
}, { useContext: Sa, useState: Da, useCallback: Ta } = e, Ra = ({ controlId: l }) => {
  const t = X(), n = Sa(qt), a = t.label ?? "", s = t.required === !0, c = t.error, i = t.warnings, u = t.helpText, r = t.dirty === !0, o = t.labelPosition ?? n.resolvedLabelPosition, d = t.fullLine === !0, m = t.visible !== !1, f = t.hasTooltip === !0, E = t.field, h = n.readOnly, [v, _] = Da(!1), g = Ta(() => _((k) => !k), []), y = o === "hidden", L = c != null, T = i != null && i.length > 0, w = [
    "tlFormField",
    `tlFormField--${o}`,
    h ? "tlFormField--readonly" : "",
    d ? "tlFormField--fullLine" : "",
    L ? "tlFormField--error" : "",
    !L && T ? "tlFormField--warning" : "",
    r ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: w, style: m ? void 0 : { display: "none" } }, !y && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": f ? "key:tooltip" : void 0
    },
    a
  ), s && !h && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), r && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), u && !h && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormField__helpIcon",
      onClick: g,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(Y, { control: E })), !h && L && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, c)), !h && !L && T && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, i.map((k, b) => /* @__PURE__ */ e.createElement("div", { key: b, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, k)))), !h && u && v && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, u));
}, La = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.iconCss, s = t.iconSrc, c = t.label, i = t.cssClass, u = t.hasTooltip === !0, r = t.hasLink, o = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : s ? /* @__PURE__ */ e.createElement("img", { src: s, className: "tlTypeIcon", alt: "" }) : null, d = /* @__PURE__ */ e.createElement(e.Fragment, null, o, c && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, c)), m = e.useCallback((h) => {
    h.preventDefault(), n("goto", {});
  }, [n]), f = ["tlResourceCell", i].filter(Boolean).join(" "), E = u ? "key:tooltip" : void 0;
  return r ? /* @__PURE__ */ e.createElement(
    "a",
    {
      id: l,
      className: f,
      href: "#",
      onClick: m,
      "data-tooltip": E
    },
    d
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: f, "data-tooltip": E }, d);
}, xa = 20, Ma = () => {
  var k;
  const l = X(), t = le(), n = l.nodes ?? [], a = l.selectionMode ?? "single", s = l.dragEnabled ?? !1, c = l.dropEnabled ?? !1, i = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [r, o] = e.useState(-1), d = e.useRef(null), m = ((k = n.find((b) => b.selected)) == null ? void 0 : k.id) ?? null;
  e.useEffect(() => {
    var j;
    if (m == null)
      return;
    const b = (j = d.current) == null ? void 0 : j.querySelector(".tlTreeView__node--selected");
    b && b.scrollIntoView({ block: "nearest" });
  }, [m]);
  const f = e.useCallback((b, j) => {
    t(j ? "collapse" : "expand", { nodeId: b });
  }, [t]), E = e.useCallback((b, j) => {
    var R;
    const S = window.getSelection();
    S && !S.isCollapsed && j.currentTarget.contains(S.anchorNode) || ((R = d.current) == null || R.focus({ preventScroll: !0 }), t("select", {
      nodeId: b,
      ctrlKey: j.ctrlKey || j.metaKey,
      shiftKey: j.shiftKey
    }));
  }, [t]), h = e.useCallback((b, j) => {
    j.preventDefault(), t("contextMenu", { nodeId: b, x: j.clientX, y: j.clientY });
  }, [t]), v = e.useRef(null), _ = e.useCallback((b, j) => {
    const S = j.getBoundingClientRect(), R = b.clientY - S.top, U = S.height / 3;
    return R < U ? "above" : R > U * 2 ? "below" : "within";
  }, []), g = e.useCallback((b, j) => {
    j.dataTransfer.effectAllowed = "move", j.dataTransfer.setData("text/plain", b);
  }, []), y = e.useCallback((b, j) => {
    j.preventDefault(), j.dataTransfer.dropEffect = "move";
    const S = _(j, j.currentTarget);
    v.current != null && window.clearTimeout(v.current), v.current = window.setTimeout(() => {
      t("dragOver", { nodeId: b, position: S }), v.current = null;
    }, 50);
  }, [t, _]), L = e.useCallback((b, j) => {
    j.preventDefault(), v.current != null && (window.clearTimeout(v.current), v.current = null);
    const S = _(j, j.currentTarget);
    t("drop", { nodeId: b, position: S });
  }, [t, _]), T = e.useCallback(() => {
    v.current != null && (window.clearTimeout(v.current), v.current = null), t("dragEnd");
  }, [t]), w = e.useCallback((b) => {
    if (n.length === 0) return;
    let j = r;
    switch (b.key) {
      case "ArrowDown":
        b.preventDefault(), j = Math.min(r + 1, n.length - 1);
        break;
      case "ArrowUp":
        b.preventDefault(), j = Math.max(r - 1, 0);
        break;
      case "ArrowRight":
        if (b.preventDefault(), r >= 0 && r < n.length) {
          const S = n[r];
          if (S.expandable && !S.expanded) {
            t("expand", { nodeId: S.id });
            return;
          } else S.expanded && (j = r + 1);
        }
        break;
      case "ArrowLeft":
        if (b.preventDefault(), r >= 0 && r < n.length) {
          const S = n[r];
          if (S.expanded) {
            t("collapse", { nodeId: S.id });
            return;
          } else {
            const R = S.depth;
            for (let U = r - 1; U >= 0; U--)
              if (n[U].depth < R) {
                j = U;
                break;
              }
          }
        }
        break;
      case "Enter":
        b.preventDefault(), r >= 0 && r < n.length && t("select", {
          nodeId: n[r].id,
          ctrlKey: b.ctrlKey || b.metaKey,
          shiftKey: b.shiftKey
        });
        return;
      case " ":
        b.preventDefault(), a === "multi" && r >= 0 && r < n.length && t("select", {
          nodeId: n[r].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        b.preventDefault(), j = 0;
        break;
      case "End":
        b.preventDefault(), j = n.length - 1;
        break;
      default:
        return;
    }
    j !== r && o(j);
  }, [r, n, t, a]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: d,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: w
    },
    n.map((b, j) => /* @__PURE__ */ e.createElement(
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
          j === r ? "tlTreeView__node--focused" : "",
          i === b.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          i === b.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          i === b.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: b.depth * xa },
        draggable: s,
        onMouseDown: (S) => {
          (S.shiftKey || S.ctrlKey || S.metaKey || S.detail > 1) && S.preventDefault();
        },
        onClick: (S) => E(b.id, S),
        onContextMenu: (S) => h(b.id, S),
        onDragStart: (S) => g(b.id, S),
        onDragOver: c ? (S) => y(b.id, S) : void 0,
        onDrop: c ? (S) => L(b.id, S) : void 0,
        onDragEnd: T
      },
      b.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (S) => {
            S.stopPropagation(), f(b.id, b.expanded);
          },
          tabIndex: -1,
          "aria-label": b.expanded ? "Collapse" : "Expand"
        },
        b.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: b.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(Y, { control: b.content }))
    ))
  );
};
var mt = { exports: {} }, de = {}, pt = { exports: {} }, Q = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var Ft;
function Ia() {
  if (Ft) return Q;
  Ft = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), a = Symbol.for("react.strict_mode"), s = Symbol.for("react.profiler"), c = Symbol.for("react.consumer"), i = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), r = Symbol.for("react.suspense"), o = Symbol.for("react.memo"), d = Symbol.for("react.lazy"), m = Symbol.for("react.activity"), f = Symbol.iterator;
  function E(p) {
    return p === null || typeof p != "object" ? null : (p = f && p[f] || p["@@iterator"], typeof p == "function" ? p : null);
  }
  var h = {
    isMounted: function() {
      return !1;
    },
    enqueueForceUpdate: function() {
    },
    enqueueReplaceState: function() {
    },
    enqueueSetState: function() {
    }
  }, v = Object.assign, _ = {};
  function g(p, D, V) {
    this.props = p, this.context = D, this.refs = _, this.updater = V || h;
  }
  g.prototype.isReactComponent = {}, g.prototype.setState = function(p, D) {
    if (typeof p != "object" && typeof p != "function" && p != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, p, D, "setState");
  }, g.prototype.forceUpdate = function(p) {
    this.updater.enqueueForceUpdate(this, p, "forceUpdate");
  };
  function y() {
  }
  y.prototype = g.prototype;
  function L(p, D, V) {
    this.props = p, this.context = D, this.refs = _, this.updater = V || h;
  }
  var T = L.prototype = new y();
  T.constructor = L, v(T, g.prototype), T.isPureReactComponent = !0;
  var w = Array.isArray;
  function k() {
  }
  var b = { H: null, A: null, T: null, S: null }, j = Object.prototype.hasOwnProperty;
  function S(p, D, V) {
    var z = V.ref;
    return {
      $$typeof: l,
      type: p,
      key: D,
      ref: z !== void 0 ? z : null,
      props: V
    };
  }
  function R(p, D) {
    return S(p.type, D, p.props);
  }
  function U(p) {
    return typeof p == "object" && p !== null && p.$$typeof === l;
  }
  function H(p) {
    var D = { "=": "=0", ":": "=2" };
    return "$" + p.replace(/[=:]/g, function(V) {
      return D[V];
    });
  }
  var C = /\/+/g;
  function x(p, D) {
    return typeof p == "object" && p !== null && p.key != null ? H("" + p.key) : D.toString(36);
  }
  function P(p) {
    switch (p.status) {
      case "fulfilled":
        return p.value;
      case "rejected":
        throw p.reason;
      default:
        switch (typeof p.status == "string" ? p.then(k, k) : (p.status = "pending", p.then(
          function(D) {
            p.status === "pending" && (p.status = "fulfilled", p.value = D);
          },
          function(D) {
            p.status === "pending" && (p.status = "rejected", p.reason = D);
          }
        )), p.status) {
          case "fulfilled":
            return p.value;
          case "rejected":
            throw p.reason;
        }
    }
    throw p;
  }
  function I(p, D, V, z, Z) {
    var $ = typeof p;
    ($ === "undefined" || $ === "boolean") && (p = null);
    var J = !1;
    if (p === null) J = !0;
    else
      switch ($) {
        case "bigint":
        case "string":
        case "number":
          J = !0;
          break;
        case "object":
          switch (p.$$typeof) {
            case l:
            case t:
              J = !0;
              break;
            case d:
              return J = p._init, I(
                J(p._payload),
                D,
                V,
                z,
                Z
              );
          }
      }
    if (J)
      return Z = Z(p), J = z === "" ? "." + x(p, 0) : z, w(Z) ? (V = "", J != null && (V = J.replace(C, "$&/") + "/"), I(Z, D, V, "", function(pe) {
        return pe;
      })) : Z != null && (U(Z) && (Z = R(
        Z,
        V + (Z.key == null || p && p.key === Z.key ? "" : ("" + Z.key).replace(
          C,
          "$&/"
        ) + "/") + J
      )), D.push(Z)), 1;
    J = 0;
    var ae = z === "" ? "." : z + ":";
    if (w(p))
      for (var ne = 0; ne < p.length; ne++)
        z = p[ne], $ = ae + x(z, ne), J += I(
          z,
          D,
          V,
          $,
          Z
        );
    else if (ne = E(p), typeof ne == "function")
      for (p = ne.call(p), ne = 0; !(z = p.next()).done; )
        z = z.value, $ = ae + x(z, ne++), J += I(
          z,
          D,
          V,
          $,
          Z
        );
    else if ($ === "object") {
      if (typeof p.then == "function")
        return I(
          P(p),
          D,
          V,
          z,
          Z
        );
      throw D = String(p), Error(
        "Objects are not valid as a React child (found: " + (D === "[object Object]" ? "object with keys {" + Object.keys(p).join(", ") + "}" : D) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return J;
  }
  function M(p, D, V) {
    if (p == null) return p;
    var z = [], Z = 0;
    return I(p, z, "", "", function($) {
      return D.call(V, $, Z++);
    }), z;
  }
  function B(p) {
    if (p._status === -1) {
      var D = p._result;
      D = D(), D.then(
        function(V) {
          (p._status === 0 || p._status === -1) && (p._status = 1, p._result = V);
        },
        function(V) {
          (p._status === 0 || p._status === -1) && (p._status = 2, p._result = V);
        }
      ), p._status === -1 && (p._status = 0, p._result = D);
    }
    if (p._status === 1) return p._result.default;
    throw p._result;
  }
  var O = typeof reportError == "function" ? reportError : function(p) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var D = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof p == "object" && p !== null && typeof p.message == "string" ? String(p.message) : String(p),
        error: p
      });
      if (!window.dispatchEvent(D)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", p);
      return;
    }
    console.error(p);
  }, K = {
    map: M,
    forEach: function(p, D, V) {
      M(
        p,
        function() {
          D.apply(this, arguments);
        },
        V
      );
    },
    count: function(p) {
      var D = 0;
      return M(p, function() {
        D++;
      }), D;
    },
    toArray: function(p) {
      return M(p, function(D) {
        return D;
      }) || [];
    },
    only: function(p) {
      if (!U(p))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return p;
    }
  };
  return Q.Activity = m, Q.Children = K, Q.Component = g, Q.Fragment = n, Q.Profiler = s, Q.PureComponent = L, Q.StrictMode = a, Q.Suspense = r, Q.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = b, Q.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(p) {
      return b.H.useMemoCache(p);
    }
  }, Q.cache = function(p) {
    return function() {
      return p.apply(null, arguments);
    };
  }, Q.cacheSignal = function() {
    return null;
  }, Q.cloneElement = function(p, D, V) {
    if (p == null)
      throw Error(
        "The argument must be a React element, but you passed " + p + "."
      );
    var z = v({}, p.props), Z = p.key;
    if (D != null)
      for ($ in D.key !== void 0 && (Z = "" + D.key), D)
        !j.call(D, $) || $ === "key" || $ === "__self" || $ === "__source" || $ === "ref" && D.ref === void 0 || (z[$] = D[$]);
    var $ = arguments.length - 2;
    if ($ === 1) z.children = V;
    else if (1 < $) {
      for (var J = Array($), ae = 0; ae < $; ae++)
        J[ae] = arguments[ae + 2];
      z.children = J;
    }
    return S(p.type, Z, z);
  }, Q.createContext = function(p) {
    return p = {
      $$typeof: i,
      _currentValue: p,
      _currentValue2: p,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, p.Provider = p, p.Consumer = {
      $$typeof: c,
      _context: p
    }, p;
  }, Q.createElement = function(p, D, V) {
    var z, Z = {}, $ = null;
    if (D != null)
      for (z in D.key !== void 0 && ($ = "" + D.key), D)
        j.call(D, z) && z !== "key" && z !== "__self" && z !== "__source" && (Z[z] = D[z]);
    var J = arguments.length - 2;
    if (J === 1) Z.children = V;
    else if (1 < J) {
      for (var ae = Array(J), ne = 0; ne < J; ne++)
        ae[ne] = arguments[ne + 2];
      Z.children = ae;
    }
    if (p && p.defaultProps)
      for (z in J = p.defaultProps, J)
        Z[z] === void 0 && (Z[z] = J[z]);
    return S(p, $, Z);
  }, Q.createRef = function() {
    return { current: null };
  }, Q.forwardRef = function(p) {
    return { $$typeof: u, render: p };
  }, Q.isValidElement = U, Q.lazy = function(p) {
    return {
      $$typeof: d,
      _payload: { _status: -1, _result: p },
      _init: B
    };
  }, Q.memo = function(p, D) {
    return {
      $$typeof: o,
      type: p,
      compare: D === void 0 ? null : D
    };
  }, Q.startTransition = function(p) {
    var D = b.T, V = {};
    b.T = V;
    try {
      var z = p(), Z = b.S;
      Z !== null && Z(V, z), typeof z == "object" && z !== null && typeof z.then == "function" && z.then(k, O);
    } catch ($) {
      O($);
    } finally {
      D !== null && V.types !== null && (D.types = V.types), b.T = D;
    }
  }, Q.unstable_useCacheRefresh = function() {
    return b.H.useCacheRefresh();
  }, Q.use = function(p) {
    return b.H.use(p);
  }, Q.useActionState = function(p, D, V) {
    return b.H.useActionState(p, D, V);
  }, Q.useCallback = function(p, D) {
    return b.H.useCallback(p, D);
  }, Q.useContext = function(p) {
    return b.H.useContext(p);
  }, Q.useDebugValue = function() {
  }, Q.useDeferredValue = function(p, D) {
    return b.H.useDeferredValue(p, D);
  }, Q.useEffect = function(p, D) {
    return b.H.useEffect(p, D);
  }, Q.useEffectEvent = function(p) {
    return b.H.useEffectEvent(p);
  }, Q.useId = function() {
    return b.H.useId();
  }, Q.useImperativeHandle = function(p, D, V) {
    return b.H.useImperativeHandle(p, D, V);
  }, Q.useInsertionEffect = function(p, D) {
    return b.H.useInsertionEffect(p, D);
  }, Q.useLayoutEffect = function(p, D) {
    return b.H.useLayoutEffect(p, D);
  }, Q.useMemo = function(p, D) {
    return b.H.useMemo(p, D);
  }, Q.useOptimistic = function(p, D) {
    return b.H.useOptimistic(p, D);
  }, Q.useReducer = function(p, D, V) {
    return b.H.useReducer(p, D, V);
  }, Q.useRef = function(p) {
    return b.H.useRef(p);
  }, Q.useState = function(p) {
    return b.H.useState(p);
  }, Q.useSyncExternalStore = function(p, D, V) {
    return b.H.useSyncExternalStore(
      p,
      D,
      V
    );
  }, Q.useTransition = function() {
    return b.H.useTransition();
  }, Q.version = "19.2.4", Q;
}
var $t;
function ja() {
  return $t || ($t = 1, pt.exports = Ia()), pt.exports;
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
var Ht;
function Pa() {
  if (Ht) return de;
  Ht = 1;
  var l = ja();
  function t(r) {
    var o = "https://react.dev/errors/" + r;
    if (1 < arguments.length) {
      o += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var d = 2; d < arguments.length; d++)
        o += "&args[]=" + encodeURIComponent(arguments[d]);
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
  }, s = Symbol.for("react.portal");
  function c(r, o, d) {
    var m = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: s,
      key: m == null ? null : "" + m,
      children: r,
      containerInfo: o,
      implementation: d
    };
  }
  var i = l.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function u(r, o) {
    if (r === "font") return "";
    if (typeof o == "string")
      return o === "use-credentials" ? o : "";
  }
  return de.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = a, de.createPortal = function(r, o) {
    var d = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!o || o.nodeType !== 1 && o.nodeType !== 9 && o.nodeType !== 11)
      throw Error(t(299));
    return c(r, o, null, d);
  }, de.flushSync = function(r) {
    var o = i.T, d = a.p;
    try {
      if (i.T = null, a.p = 2, r) return r();
    } finally {
      i.T = o, a.p = d, a.d.f();
    }
  }, de.preconnect = function(r, o) {
    typeof r == "string" && (o ? (o = o.crossOrigin, o = typeof o == "string" ? o === "use-credentials" ? o : "" : void 0) : o = null, a.d.C(r, o));
  }, de.prefetchDNS = function(r) {
    typeof r == "string" && a.d.D(r);
  }, de.preinit = function(r, o) {
    if (typeof r == "string" && o && typeof o.as == "string") {
      var d = o.as, m = u(d, o.crossOrigin), f = typeof o.integrity == "string" ? o.integrity : void 0, E = typeof o.fetchPriority == "string" ? o.fetchPriority : void 0;
      d === "style" ? a.d.S(
        r,
        typeof o.precedence == "string" ? o.precedence : void 0,
        {
          crossOrigin: m,
          integrity: f,
          fetchPriority: E
        }
      ) : d === "script" && a.d.X(r, {
        crossOrigin: m,
        integrity: f,
        fetchPriority: E,
        nonce: typeof o.nonce == "string" ? o.nonce : void 0
      });
    }
  }, de.preinitModule = function(r, o) {
    if (typeof r == "string")
      if (typeof o == "object" && o !== null) {
        if (o.as == null || o.as === "script") {
          var d = u(
            o.as,
            o.crossOrigin
          );
          a.d.M(r, {
            crossOrigin: d,
            integrity: typeof o.integrity == "string" ? o.integrity : void 0,
            nonce: typeof o.nonce == "string" ? o.nonce : void 0
          });
        }
      } else o == null && a.d.M(r);
  }, de.preload = function(r, o) {
    if (typeof r == "string" && typeof o == "object" && o !== null && typeof o.as == "string") {
      var d = o.as, m = u(d, o.crossOrigin);
      a.d.L(r, d, {
        crossOrigin: m,
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
        var d = u(o.as, o.crossOrigin);
        a.d.m(r, {
          as: typeof o.as == "string" && o.as !== "script" ? o.as : void 0,
          crossOrigin: d,
          integrity: typeof o.integrity == "string" ? o.integrity : void 0
        });
      } else a.d.m(r);
  }, de.requestFormReset = function(r) {
    a.d.r(r);
  }, de.unstable_batchedUpdates = function(r, o) {
    return r(o);
  }, de.useFormState = function(r, o, d) {
    return i.H.useFormState(r, o, d);
  }, de.useFormStatus = function() {
    return i.H.useHostTransitionStatus();
  }, de.version = "19.2.4", de;
}
var Wt;
function Aa() {
  if (Wt) return mt.exports;
  Wt = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), mt.exports = Pa(), mt.exports;
}
var Zt = Aa();
const { useState: Ne, useCallback: me, useRef: Ye, useEffect: Be, useMemo: _t } = e;
function kt({ image: l }) {
  if (!l) return null;
  if (l.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function Ba({
  option: l,
  removable: t,
  onRemove: n,
  removeLabel: a,
  draggable: s,
  onDragStart: c,
  onDragOver: i,
  onDrop: u,
  onDragEnd: r,
  dragClassName: o
}) {
  const d = me(
    (m) => {
      m.stopPropagation(), n(l.value);
    },
    [n, l.value]
  );
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlDropdownSelect__chip" + (o ? " " + o : ""),
      draggable: s || void 0,
      onDragStart: c,
      onDragOver: i,
      onDrop: u,
      onDragEnd: r
    },
    s && /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__dragHandle", "aria-hidden": "true" }, "⋮⋮"),
    /* @__PURE__ */ e.createElement(kt, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__chipLabel" }, l.label),
    t && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__chipRemove",
        onClick: d,
        "aria-label": a
      },
      "×"
    )
  );
}
function Oa({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: a,
  onMouseEnter: s,
  id: c
}) {
  const i = me(() => a(l.value), [a, l.value]), u = _t(() => {
    if (!n) return l.label;
    const r = l.label.toLowerCase().indexOf(n.toLowerCase());
    return r < 0 ? l.label : /* @__PURE__ */ e.createElement(e.Fragment, null, l.label.substring(0, r), /* @__PURE__ */ e.createElement("strong", null, l.label.substring(r, r + n.length)), l.label.substring(r + n.length));
  }, [l.label, n]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: c,
      role: "option",
      "aria-selected": t,
      className: "tlDropdownSelect__option" + (t ? " tlDropdownSelect__option--highlighted" : ""),
      onClick: i,
      onMouseEnter: s
    },
    /* @__PURE__ */ e.createElement(kt, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const Fa = ({ controlId: l, state: t }) => {
  const n = le(), a = t.value ?? [], s = t.multiSelect === !0, c = t.customOrder === !0, i = t.mandatory === !0, u = t.disabled === !0, r = t.editable !== !1, o = t.optionsLoaded === !0, d = t.options ?? [], m = t.emptyOptionLabel ?? "", f = c && s && !u && r, E = se({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), h = E["js.dropdownSelect.nothingFound"], v = me(
    (F) => E["js.dropdownSelect.removeChip"].replace("{0}", F),
    [E]
  ), [_, g] = Ne(!1), [y, L] = Ne(""), [T, w] = Ne(-1), [k, b] = Ne(!1), [j, S] = Ne({}), [R, U] = Ne(null), [H, C] = Ne(null), [x, P] = Ne(null), I = Ye(null), M = Ye(null), B = Ye(null), O = Ye(a);
  O.current = a;
  const K = Ye(-1), p = _t(
    () => new Set(a.map((F) => F.value)),
    [a]
  ), D = _t(() => {
    let F = d.filter((G) => !p.has(G.value));
    if (y) {
      const G = y.toLowerCase();
      F = F.filter((te) => te.label.toLowerCase().includes(G));
    }
    return F;
  }, [d, p, y]);
  Be(() => {
    y && D.length === 1 ? w(0) : w(-1);
  }, [D.length, y]), Be(() => {
    _ && o && M.current && M.current.focus();
  }, [_, o, a]), Be(() => {
    var te, oe;
    if (K.current < 0) return;
    const F = K.current;
    K.current = -1;
    const G = (te = I.current) == null ? void 0 : te.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    G && G.length > 0 ? G[Math.min(F, G.length - 1)].focus() : (oe = I.current) == null || oe.focus();
  }, [a]), Be(() => {
    if (!_) return;
    const F = (G) => {
      I.current && !I.current.contains(G.target) && B.current && !B.current.contains(G.target) && (g(!1), L(""));
    };
    return document.addEventListener("mousedown", F), () => document.removeEventListener("mousedown", F);
  }, [_]), Be(() => {
    if (!_ || !I.current) return;
    const F = I.current.getBoundingClientRect(), G = window.innerHeight - F.bottom, oe = G < 300 && F.top > G;
    S({
      left: F.left,
      width: F.width,
      ...oe ? { bottom: window.innerHeight - F.top } : { top: F.bottom }
    });
  }, [_]);
  const V = me(async () => {
    if (!(u || !r) && (g(!0), L(""), w(-1), b(!1), !o))
      try {
        await n("loadOptions");
      } catch {
        b(!0);
      }
  }, [u, r, o, n]), z = me(() => {
    var F;
    g(!1), L(""), w(-1), (F = I.current) == null || F.focus();
  }, []), Z = me(
    (F) => {
      let G;
      if (s) {
        const te = d.find((oe) => oe.value === F);
        if (te)
          G = [...O.current, te];
        else
          return;
      } else {
        const te = d.find((oe) => oe.value === F);
        if (te)
          G = [te];
        else
          return;
      }
      O.current = G, n(Qe, { value: G.map((te) => te.value) }), s ? (L(""), w(-1)) : z();
    },
    [s, d, n, z]
  ), $ = me(
    (F) => {
      K.current = O.current.findIndex((te) => te.value === F);
      const G = O.current.filter((te) => te.value !== F);
      O.current = G, n(Qe, { value: G.map((te) => te.value) });
    },
    [n]
  ), J = me(
    (F) => {
      F.stopPropagation(), n(Qe, { value: [] }), z();
    },
    [n, z]
  ), ae = me((F) => {
    L(F.target.value);
  }, []), ne = me(
    (F) => {
      if (!_) {
        if (F.key === "ArrowDown" || F.key === "ArrowUp" || F.key === "Enter" || F.key === " ") {
          if (F.target.tagName === "BUTTON") return;
          F.preventDefault(), F.stopPropagation(), V();
        }
        return;
      }
      switch (F.key) {
        case "ArrowDown":
          F.preventDefault(), F.stopPropagation(), w(
            (G) => G < D.length - 1 ? G + 1 : 0
          );
          break;
        case "ArrowUp":
          F.preventDefault(), F.stopPropagation(), w(
            (G) => G > 0 ? G - 1 : D.length - 1
          );
          break;
        case "Enter":
          F.preventDefault(), F.stopPropagation(), T >= 0 && T < D.length && Z(D[T].value);
          break;
        case "Escape":
          F.preventDefault(), F.stopPropagation(), z();
          break;
        case "Tab":
          z();
          break;
        case "Backspace":
          y === "" && s && a.length > 0 && $(a[a.length - 1].value);
          break;
      }
    },
    [
      _,
      V,
      z,
      D,
      T,
      Z,
      y,
      s,
      a,
      $
    ]
  ), pe = me(
    async (F) => {
      F.preventDefault(), b(!1);
      try {
        await n("loadOptions");
      } catch {
        b(!0);
      }
    },
    [n]
  ), ge = me(
    (F, G) => {
      U(F), G.dataTransfer.effectAllowed = "move", G.dataTransfer.setData("text/plain", String(F));
    },
    []
  ), Ee = me(
    (F, G) => {
      if (G.preventDefault(), G.dataTransfer.dropEffect = "move", R === null || R === F) {
        C(null), P(null);
        return;
      }
      const te = G.currentTarget.getBoundingClientRect(), oe = te.left + te.width / 2, N = G.clientX < oe ? "before" : "after";
      C(F), P(N);
    },
    [R]
  ), we = me(
    (F) => {
      if (F.preventDefault(), R === null || H === null || x === null || R === H) return;
      const G = [...O.current], [te] = G.splice(R, 1);
      let oe = H;
      R < H ? oe = x === "before" ? oe - 1 : oe : oe = x === "before" ? oe : oe + 1, G.splice(oe, 0, te), O.current = G, n(Qe, { value: G.map((N) => N.value) }), U(null), C(null), P(null);
    },
    [R, H, x, n]
  ), ke = me(() => {
    U(null), C(null), P(null);
  }, []);
  if (Be(() => {
    if (T < 0 || !B.current) return;
    const F = B.current.querySelector(
      `[id="${l}-opt-${T}"]`
    );
    F && F.scrollIntoView({ block: "nearest" });
  }, [T, l]), !r)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, a.map((F) => /* @__PURE__ */ e.createElement("span", { key: F.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(kt, { image: F.image }), /* @__PURE__ */ e.createElement("span", null, F.label))));
  const Ie = !i && a.length > 0 && !u, qe = _ ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: B,
      className: "tlDropdownSelect__dropdown",
      style: j,
      ...an
    },
    (o || k) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: M,
        type: "text",
        className: "tlDropdownSelect__search",
        value: y,
        onChange: ae,
        onKeyDown: ne,
        placeholder: E["js.dropdownSelect.filterPlaceholder"],
        "aria-label": E["js.dropdownSelect.filterPlaceholder"],
        "aria-activedescendant": T >= 0 ? `${l}-opt-${T}` : void 0,
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
      k && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: pe }, E["js.dropdownSelect.error"])),
      o && D.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, h),
      o && D.map((F, G) => /* @__PURE__ */ e.createElement(
        Oa,
        {
          key: F.value,
          id: `${l}-opt-${G}`,
          option: F,
          highlighted: G === T,
          searchTerm: y,
          onSelect: Z,
          onMouseEnter: () => w(G)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: I,
      className: "tlDropdownSelect" + (_ ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": _,
      "aria-haspopup": "listbox",
      "aria-owns": _ ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: _ ? void 0 : V,
      onKeyDown: ne
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, a.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, m) : a.map((F, G) => {
      let te = "";
      return R === G ? te = "tlDropdownSelect__chip--dragging" : H === G && x === "before" ? te = "tlDropdownSelect__chip--dropBefore" : H === G && x === "after" && (te = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        Ba,
        {
          key: F.value,
          option: F,
          removable: !u && (s || !i),
          onRemove: $,
          removeLabel: v(F.label),
          draggable: f,
          onDragStart: f ? (oe) => ge(G, oe) : void 0,
          onDragOver: f ? (oe) => Ee(G, oe) : void 0,
          onDrop: f ? we : void 0,
          onDragEnd: f ? ke : void 0,
          dragClassName: f ? te : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, Ie && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: J,
        "aria-label": E["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, _ ? "▲" : "▼"))
  ), qe && Zt.createPortal(qe, document.body));
}, { useCallback: ft, useRef: $a } = e, Qt = "application/x-tl-color", Ha = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: a,
  onSwap: s,
  onReplace: c
}) => {
  const i = $a(null), u = ft(
    (d) => (m) => {
      i.current = d, m.dataTransfer.effectAllowed = "move";
    },
    []
  ), r = ft((d) => {
    d.preventDefault(), d.dataTransfer.dropEffect = "move";
  }, []), o = ft(
    (d) => (m) => {
      m.preventDefault();
      const f = m.dataTransfer.getData(Qt);
      f ? c(d, f) : i.current !== null && i.current !== d && s(i.current, d), i.current = null;
    },
    [s, c]
  );
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__palette",
      style: { gridTemplateColumns: `repeat(${t}, 1fr)` }
    },
    l.map((d, m) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: m,
        className: "tlColorInput__paletteCell" + (d == null ? " tlColorInput__paletteCell--empty" : ""),
        style: d != null ? { backgroundColor: d } : void 0,
        title: d ?? "",
        draggable: d != null,
        onClick: d != null ? () => n(d) : void 0,
        onDoubleClick: d != null ? () => a(d) : void 0,
        onDragStart: d != null ? u(m) : void 0,
        onDragOver: r,
        onDrop: o(m)
      }
    ))
  );
};
function Jt(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function Ct(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function en(l) {
  if (!Ct(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function tn(l, t, n) {
  const a = (s) => Jt(s).toString(16).padStart(2, "0");
  return "#" + a(l) + a(t) + a(n);
}
function Wa(l, t, n) {
  const a = l / 255, s = t / 255, c = n / 255, i = Math.max(a, s, c), u = Math.min(a, s, c), r = i - u;
  let o = 0;
  r !== 0 && (i === a ? o = (s - c) / r % 6 : i === s ? o = (c - a) / r + 2 : o = (a - s) / r + 4, o *= 60, o < 0 && (o += 360));
  const d = i === 0 ? 0 : r / i;
  return [o, d, i];
}
function Ua(l, t, n) {
  const a = n * t, s = a * (1 - Math.abs(l / 60 % 2 - 1)), c = n - a;
  let i = 0, u = 0, r = 0;
  return l < 60 ? (i = a, u = s, r = 0) : l < 120 ? (i = s, u = a, r = 0) : l < 180 ? (i = 0, u = a, r = s) : l < 240 ? (i = 0, u = s, r = a) : l < 300 ? (i = s, u = 0, r = a) : (i = a, u = 0, r = s), [
    Math.round((i + c) * 255),
    Math.round((u + c) * 255),
    Math.round((r + c) * 255)
  ];
}
function za(l) {
  return Wa(...en(l));
}
function ht(l, t, n) {
  return tn(...Ua(l, t, n));
}
const { useCallback: Oe, useRef: Ut } = e, Va = ({ color: l, onColorChange: t }) => {
  const [n, a, s] = za(l), c = Ut(null), i = Ut(null), u = Oe(
    (h, v) => {
      var L;
      const _ = (L = c.current) == null ? void 0 : L.getBoundingClientRect();
      if (!_) return;
      const g = Math.max(0, Math.min(1, (h - _.left) / _.width)), y = Math.max(0, Math.min(1, 1 - (v - _.top) / _.height));
      t(ht(n, g, y));
    },
    [n, t]
  ), r = Oe(
    (h) => {
      h.preventDefault(), h.target.setPointerCapture(h.pointerId), u(h.clientX, h.clientY);
    },
    [u]
  ), o = Oe(
    (h) => {
      h.buttons !== 0 && u(h.clientX, h.clientY);
    },
    [u]
  ), d = Oe(
    (h) => {
      var y;
      const v = (y = i.current) == null ? void 0 : y.getBoundingClientRect();
      if (!v) return;
      const g = Math.max(0, Math.min(1, (h - v.top) / v.height)) * 360;
      t(ht(g, a, s));
    },
    [a, s, t]
  ), m = Oe(
    (h) => {
      h.preventDefault(), h.target.setPointerCapture(h.pointerId), d(h.clientY);
    },
    [d]
  ), f = Oe(
    (h) => {
      h.buttons !== 0 && d(h.clientY);
    },
    [d]
  ), E = ht(n, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: c,
      className: "tlColorInput__svField",
      style: { backgroundColor: E },
      onPointerDown: r,
      onPointerMove: o
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__svHandle",
        style: { left: `${a * 100}%`, top: `${(1 - s) * 100}%` }
      }
    )
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: i,
      className: "tlColorInput__hueSlider",
      onPointerDown: m,
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
function Ka(l, t) {
  const n = t.toUpperCase();
  return l.some((a) => a != null && a.toUpperCase() === n);
}
const Ya = {
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
}, { useState: tt, useCallback: _e, useEffect: zt, useRef: Ga, useLayoutEffect: Xa } = e, qa = ({
  anchorRef: l,
  currentColor: t,
  palette: n,
  paletteColumns: a,
  defaultPalette: s,
  canReset: c,
  onConfirm: i,
  onCancel: u,
  onPaletteChange: r
}) => {
  const [o, d] = tt("palette"), [m, f] = tt(t), E = Ga(null), h = se(Ya), [v, _] = tt(null);
  Xa(() => {
    if (!l.current || !E.current) return;
    const B = l.current.getBoundingClientRect(), O = E.current.getBoundingClientRect();
    let K = B.bottom + 4, p = B.left;
    K + O.height > window.innerHeight && (K = B.top - O.height - 4), p + O.width > window.innerWidth && (p = Math.max(0, B.right - O.width)), _({ top: K, left: p });
  }, [l]);
  const g = m != null, [y, L, T] = g ? en(m) : [0, 0, 0], [w, k] = tt((m == null ? void 0 : m.toUpperCase()) ?? "");
  zt(() => {
    k((m == null ? void 0 : m.toUpperCase()) ?? "");
  }, [m]), Me(!0, { ESCAPE: u }), zt(() => {
    const B = (K) => {
      E.current && !E.current.contains(K.target) && u();
    }, O = setTimeout(() => document.addEventListener("mousedown", B), 0);
    return () => {
      clearTimeout(O), document.removeEventListener("mousedown", B);
    };
  }, [u]);
  const b = _e(
    (B) => (O) => {
      const K = parseInt(O.target.value, 10);
      if (isNaN(K)) return;
      const p = Jt(K);
      f(tn(B === "r" ? p : y, B === "g" ? p : L, B === "b" ? p : T));
    },
    [y, L, T]
  ), j = _e(
    (B) => {
      if (m != null) {
        B.dataTransfer.setData(Qt, m.toUpperCase()), B.dataTransfer.effectAllowed = "move";
        const O = document.createElement("div");
        O.style.width = "33px", O.style.height = "33px", O.style.backgroundColor = m, O.style.borderRadius = "3px", O.style.border = "1px solid rgba(0,0,0,0.1)", O.style.position = "absolute", O.style.top = "-9999px", document.body.appendChild(O), B.dataTransfer.setDragImage(O, 16, 16), requestAnimationFrame(() => document.body.removeChild(O));
      }
    },
    [m]
  ), S = _e((B) => {
    const O = B.target.value;
    k(O), Ct(O) && f(O);
  }, []), R = _e(() => {
    f(null);
  }, []), U = _e((B) => {
    f(B);
  }, []), H = _e(
    (B) => {
      i(B);
    },
    [i]
  ), C = _e(
    (B, O) => {
      const K = [...n], p = K[B];
      K[B] = K[O], K[O] = p, r(K);
    },
    [n, r]
  ), x = _e(
    (B, O) => {
      const K = [...n];
      K[B] = O, r(K);
    },
    [n, r]
  ), P = _e(() => {
    r([...s]);
  }, [s, r]), I = _e(
    (B) => {
      if (Ka(n, B)) return;
      const O = n.indexOf(null);
      if (O < 0) return;
      const K = [...n];
      K[O] = B.toUpperCase(), r(K);
    },
    [n, r]
  ), M = _e(() => {
    m != null && I(m), i(m);
  }, [m, i, I]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: E,
      style: v ? { top: v.top, left: v.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (o === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => d("palette")
      },
      h["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (o === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => d("mixer")
      },
      h["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, o === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      Ha,
      {
        colors: n,
        columns: a,
        onSelect: U,
        onConfirm: H,
        onSwap: C,
        onReplace: x
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: P }, h["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(Va, { color: m ?? "#000000", onColorChange: f }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, h["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, h["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (g ? "" : " tlColorInput--noColor"),
        style: g ? { backgroundColor: m } : void 0,
        draggable: g,
        onDragStart: g ? j : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: g ? y : "",
        onChange: b("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: g ? L : "",
        onChange: b("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: g ? T : "",
        onChange: b("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (w !== "" && !Ct(w) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: w,
        onChange: S
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, c && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: R }, h["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, h["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: M }, h["js.colorInput.ok"]))
  );
}, Za = { "js.colorInput.chooseColor": "Choose color" }, { useState: Qa, useCallback: nt, useRef: Ja } = e, er = ({ controlId: l, state: t }) => {
  const [n, a] = Te(), s = le(), c = se(Za), [i, u] = Qa(!1), r = Ja(null), o = n, d = t.editable !== !1, m = t.palette ?? [], f = t.paletteColumns ?? 6, E = t.defaultPalette ?? m, h = nt(() => {
    d && u(!0);
  }, [d]), v = nt(
    (y) => {
      u(!1), a(y);
    },
    [a]
  ), _ = nt(() => {
    u(!1);
  }, []), g = nt(
    (y) => {
      s("paletteChanged", { palette: y });
    },
    [s]
  );
  return d ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: r,
      className: "tlColorInput__swatch" + (o == null ? " tlColorInput__swatch--noColor" : ""),
      style: o != null ? { backgroundColor: o } : void 0,
      onClick: h,
      disabled: t.disabled === !0,
      title: o ?? "",
      "aria-label": c["js.colorInput.chooseColor"]
    }
  ), i && /* @__PURE__ */ e.createElement(
    qa,
    {
      anchorRef: r,
      currentColor: o,
      palette: m,
      paletteColumns: f,
      defaultPalette: E,
      canReset: t.canReset !== !1,
      onConfirm: v,
      onCancel: _,
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
}, { useState: Ge, useCallback: Re, useEffect: bt, useRef: Vt, useLayoutEffect: tr, useMemo: nr } = e, lr = {
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
}, ar = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: a,
  onSelect: s,
  onCancel: c,
  onLoadIcons: i
}) => {
  const u = se(lr), [r, o] = Ge("simple"), [d, m] = Ge(""), [f, E] = Ge(t ?? ""), [h, v] = Ge(!1), [_, g] = Ge(null), y = Vt(null), L = Vt(null);
  tr(() => {
    if (!l.current || !y.current) return;
    const H = l.current.getBoundingClientRect(), C = y.current.getBoundingClientRect();
    let x = H.bottom + 4, P = H.left;
    x + C.height > window.innerHeight && (x = H.top - C.height - 4), P + C.width > window.innerWidth && (P = Math.max(0, H.right - C.width)), g({ top: x, left: P });
  }, [l]), bt(() => {
    !a && !h && i().catch(() => v(!0));
  }, [a, h, i]), bt(() => {
    a && L.current && L.current.focus();
  }, [a]), Me(!0, { ESCAPE: c }), bt(() => {
    const H = (x) => {
      y.current && !y.current.contains(x.target) && c();
    }, C = setTimeout(() => document.addEventListener("mousedown", H), 0);
    return () => {
      clearTimeout(C), document.removeEventListener("mousedown", H);
    };
  }, [c]);
  const T = nr(() => {
    if (!d) return n;
    const H = d.toLowerCase();
    return n.filter(
      (C) => C.prefix.toLowerCase().includes(H) || C.label.toLowerCase().includes(H) || C.terms != null && C.terms.some((x) => x.includes(H))
    );
  }, [n, d]), w = Re((H) => {
    m(H.target.value);
  }, []), k = Re(
    (H) => {
      s(H);
    },
    [s]
  ), b = Re((H) => {
    E(H);
  }, []), j = Re((H) => {
    E(H.target.value);
  }, []), S = Re(() => {
    s(f || null);
  }, [f, s]), R = Re(() => {
    s(null);
  }, [s]), U = Re(async (H) => {
    H.preventDefault(), v(!1);
    try {
      await i();
    } catch {
      v(!0);
    }
  }, [i]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlIconSelect__popup",
      ref: y,
      style: _ ? { top: _.top, left: _.left, visibility: "visible" } : { visibility: "hidden" }
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
        value: d,
        onChange: w,
        placeholder: u["js.iconSelect.filterPlaceholder"],
        "aria-label": u["js.iconSelect.filterPlaceholder"]
      }
    ), d && /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__resetBtn",
        onClick: () => m(""),
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
      !a && !h && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      h && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: U }, u["js.iconSelect.loadError"])),
      a && T.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      a && T.map(
        (H) => H.variants.map((C) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: C.encoded,
            className: "tlIconSelect__iconCell" + (C.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": C.encoded === t,
            tabIndex: 0,
            title: H.label,
            onClick: () => r === "simple" ? k(C.encoded) : b(C.encoded),
            onKeyDown: (x) => {
              (x.key === "Enter" || x.key === " ") && (x.preventDefault(), r === "simple" ? k(C.encoded) : b(C.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(Ce, { encoded: C.encoded })
        ))
      )
    ),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: f,
        onChange: j
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, f && /* @__PURE__ */ e.createElement(Ce, { encoded: f })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, f ? f.startsWith("css:") ? f.substring(4) : f : ""))),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: c }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: R }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: S }, u["js.iconSelect.ok"]))
  );
}, rr = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: or, useCallback: lt, useRef: sr } = e, cr = ({ controlId: l, state: t }) => {
  const [n, a] = Te(), s = le(), c = se(rr), [i, u] = or(!1), r = sr(null), o = n, d = t.editable !== !1, m = t.disabled === !0, f = t.icons ?? [], E = t.iconsLoaded === !0, h = lt(() => {
    d && !m && u(!0);
  }, [d, m]), v = lt(
    (y) => {
      u(!1), a(y);
    },
    [a]
  ), _ = lt(() => {
    u(!1);
  }, []), g = lt(async () => {
    await s("loadIcons");
  }, [s]);
  return d ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: r,
      className: "tlIconSelect__swatch" + (o == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: h,
      disabled: m,
      title: o ?? "",
      "aria-label": c["js.iconSelect.chooseIcon"]
    },
    o ? /* @__PURE__ */ e.createElement(Ce, { encoded: o }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), i && /* @__PURE__ */ e.createElement(
    ar,
    {
      anchorRef: r,
      currentValue: o,
      icons: f,
      iconsLoaded: E,
      onSelect: v,
      onCancel: _,
      onLoadIcons: g
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, o ? /* @__PURE__ */ e.createElement(Ce, { encoded: o }) : null));
}, { useCallback: Fe, useEffect: ir, useMemo: Kt, useRef: ur, useState: gt } = e, dr = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, mr = [1, 2, 3, 4];
function pr(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const a = parseFloat(n[1]), s = n[2] || "px";
  return s === "rem" || s === "em" ? a * t : a;
}
function fr(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let a = 1;
  for (const s of mr)
    n >= s && (a = s);
  return a;
}
function hr(l, t) {
  const n = dr[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function br(l, t) {
  const n = Math.max(1, t), a = {}, s = (m, f) => !!(a[m] && a[m][f]), c = (m, f) => {
    a[m] || (a[m] = {}), a[m][f] = !0;
  }, i = [];
  let u = 0, r = 0;
  const o = (m) => {
    let f = null;
    for (const h of i) h.rowStart === m && (f = h);
    if (!f) return;
    let E = f.colEnd;
    for (; E < n && !s(m, E); ) E++;
    if (E !== f.colEnd) {
      for (let h = f.rowStart; h < f.rowEnd; h++)
        for (let v = f.colEnd; v < E; v++) c(h, v);
      f.colEnd = E;
    }
  };
  for (const m of l) {
    const f = n <= 1 ? 1 : Math.max(1, m.rowSpan || 1);
    let E = Math.min(hr(m.width, n), n);
    for (; s(u, r); )
      r++, r >= n && (r = 0, u++);
    let h = 0;
    for (let L = r; L < n && !s(u, L); L++)
      h++;
    if (E > h) {
      for (o(u), r = 0, u++; s(u, r); )
        r++, r >= n && (r = 0, u++);
      h = 0;
      for (let L = r; L < n && !s(u, L); L++)
        h++;
      E = Math.min(E, h);
    }
    const v = r, _ = r + E, g = u, y = u + f;
    i.push({ id: m.id, colStart: v, colEnd: _, rowStart: g, rowEnd: y });
    for (let L = g; L < y; L++)
      for (let T = v; T < _; T++) c(L, T);
    r = _, r >= n && (r = 0, u++);
  }
  o(u);
  let d = 0;
  for (const m of i) m.rowEnd > d && (d = m.rowEnd);
  for (let m = 1; m < d; m++)
    for (let f = 0; f < n; f++) {
      if (s(m, f)) continue;
      const E = i.find((h) => h.rowEnd === m && h.colStart <= f && f < h.colEnd);
      if (E) {
        E.rowEnd = m + 1;
        for (let h = E.colStart; h < E.colEnd; h++) c(m, h);
      }
    }
  return i;
}
const gr = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.minColWidth ?? "16rem", s = (t.children ?? []).filter((k) => k && k.id), c = ur(null), [i, u] = gt(1), r = t.editMode === !0;
  ir(() => {
    const k = c.current;
    if (!k) return;
    const b = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, j = pr(a, b), S = () => u(fr(k.clientWidth, j));
    S();
    const R = new ResizeObserver(S);
    return R.observe(k), () => R.disconnect();
  }, [a]);
  const o = Kt(() => br(s, i), [s, i]), d = Kt(() => {
    const k = {};
    for (const b of o) k[b.id] = b;
    return k;
  }, [o]), [m, f] = gt(null), [E, h] = gt(null), v = Fe((k, b) => {
    if (!r) {
      k.preventDefault();
      return;
    }
    f(b), k.dataTransfer.effectAllowed = "move", k.dataTransfer.setData("text/plain", b);
  }, [r]), _ = Fe((k, b) => {
    if (!r || !m || m === b) return;
    k.preventDefault(), k.dataTransfer.dropEffect = "move";
    const j = k.currentTarget.getBoundingClientRect(), S = k.clientX < j.left + j.width / 2;
    h((R) => R && R.id === b && R.before === S ? R : { id: b, before: S });
  }, [r, m]), g = Fe(() => {
  }, []), y = Fe((k, b, j) => {
    const S = s.map((C) => C.id), R = S.indexOf(k);
    if (R < 0) return;
    S.splice(R, 1);
    const U = S.indexOf(b);
    if (U < 0) {
      S.splice(R, 0, k);
      return;
    }
    const H = j ? U : U + 1;
    S.splice(H, 0, k), n("reorder", { order: S });
  }, [s, n]), L = Fe((k, b) => {
    if (!r || !m || m === b) return;
    k.preventDefault();
    const j = k.currentTarget.getBoundingClientRect(), S = k.clientX < j.left + j.width / 2;
    y(m, b, S), f(null), h(null);
  }, [r, m, y]), T = Fe(() => {
    f(null), h(null);
  }, []), w = {
    display: "grid",
    gridTemplateColumns: `repeat(${i}, 1fr)`,
    gap: "1rem"
  };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: c,
      className: "tlDashboard" + (r ? " tlDashboard--edit" : "")
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: w }, s.map((k) => {
      const b = d[k.id];
      if (!b) return null;
      const j = {
        gridColumn: `${b.colStart + 1} / ${b.colEnd + 1}`,
        gridRow: `${b.rowStart + 1} / ${b.rowEnd + 1}`
      }, S = ["tlDashboard__tile"];
      return m === k.id && S.push("tlDashboard__tile--dragging"), E && E.id === k.id && S.push(E.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: k.id,
          className: S.join(" "),
          style: j,
          draggable: r,
          onDragStart: (R) => v(R, k.id),
          onDragOver: (R) => _(R, k.id),
          onDragLeave: g,
          onDrop: (R) => L(R, k.id),
          onDragEnd: T
        },
        /* @__PURE__ */ e.createElement(Y, { control: k.control }),
        r && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: Er, useRef: Yt, useState: Gt, useEffect: vr, useLayoutEffect: _r } = e, Cr = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, a) => /* @__PURE__ */ e.createElement("span", { key: a, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(Y, { control: n }))));
}, yr = ({ group: l }) => {
  var m, f;
  const [t, n] = Gt(!1), [a, s] = Gt({}), c = Yt(null), i = Yt(null), u = Er(() => {
    n((E) => !E);
  }, []);
  _r(() => {
    if (!t) return;
    const E = () => {
      const h = c.current;
      if (!h) return;
      const v = h.getBoundingClientRect();
      s({
        position: "fixed",
        top: v.bottom + 4,
        right: Math.max(8, window.innerWidth - v.right),
        left: "auto"
      });
    };
    return E(), window.addEventListener("resize", E), window.addEventListener("scroll", E, !0), () => {
      window.removeEventListener("resize", E), window.removeEventListener("scroll", E, !0);
    };
  }, [t]), vr(() => {
    if (!t) return;
    const E = (h) => {
      i.current && !i.current.contains(h.target) && c.current && !c.current.contains(h.target) && n(!1);
    };
    return document.addEventListener("mousedown", E), () => document.removeEventListener("mousedown", E);
  }, [t]), Me(t, { ESCAPE: () => n(!1) }), wt(t, i, "first");
  const r = l.items.filter((E) => E != null);
  if (r.length === 0) return null;
  if (r.length === 1 && !((m = l.subGroups) != null && m.length) && !l.icon)
    return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(Y, { control: r[0] })));
  const o = l.label ?? l.name, d = !!l.icon;
  return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--menu" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: c,
      type: "button",
      className: "tlToolbar__menuTrigger" + (d ? " tlToolbar__menuTrigger--icon" : ""),
      onMouseDown: (E) => E.preventDefault(),
      onClick: u,
      "aria-expanded": t,
      "aria-haspopup": "true",
      "aria-label": d ? o : void 0,
      title: d ? o : void 0
    },
    d ? /* @__PURE__ */ e.createElement(Ce, { encoded: l.icon, className: "tlToolbar__menuIcon" }) : /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement("span", null, o), /* @__PURE__ */ e.createElement("svg", { className: "tlToolbar__chevron", viewBox: "0 0 24 24", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("polyline", { points: "6,9 12,15 18,9" })))
  ), Zt.createPortal(
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
      r.map((E, h) => /* @__PURE__ */ e.createElement("div", { key: h, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(Y, { control: E }))),
      (f = l.subGroups) == null ? void 0 : f.map((E, h) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${h}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), E.items.map((v, _) => /* @__PURE__ */ e.createElement("div", { key: _, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(Y, { control: v })))))
    ),
    document.body
  ));
}, wr = ({ controlId: l }) => {
  const a = (X().groups ?? []).filter((s) => s.items.some((c) => c != null));
  return a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, a.map((s, c) => /* @__PURE__ */ e.createElement(e.Fragment, { key: s.name }, c > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), s.display === "menu" ? /* @__PURE__ */ e.createElement(yr, { group: s }) : /* @__PURE__ */ e.createElement(Cr, { group: s }))));
}, kr = ({ controlId: l }) => {
  const t = X();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlTileStack", style: { width: "100%", height: "100%" } }, t.frame && /* @__PURE__ */ e.createElement(Y, { control: t.frame }));
}, Nr = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.content, s = t.breadcrumb ?? null;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAdaptiveDetail" }, s && s.length > 0 && /* @__PURE__ */ e.createElement("nav", { className: "tlAdaptiveDetail__breadcrumb", "aria-label": "Breadcrumb" }, s.map((c, i) => {
    const u = i === s.length - 1;
    return /* @__PURE__ */ e.createElement(e.Fragment, { key: c.depth }, i > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__sep" }, "›"), u ? /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__crumb tlAdaptiveDetail__crumb--current" }, c.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlAdaptiveDetail__crumb",
        onClick: () => n("navigate", { depth: c.depth })
      },
      c.label
    ));
  })), /* @__PURE__ */ e.createElement("div", { className: "tlAdaptiveDetail__content" }, a && /* @__PURE__ */ e.createElement(Y, { control: a })));
}, Sr = ({ controlId: l }) => {
  const n = X().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((a, s) => /* @__PURE__ */ e.createElement(Y, { key: s, control: a })));
}, Dr = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), Tr = {
  "js.sidebar.openDrawer": "Open navigation"
}, Rr = ({ controlId: l }) => {
  const t = le(), n = se(Tr);
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
W("TLButton", _n);
W("TLUploadButton", Cn);
W("TLToggleButton", wn);
W("TLTextInput", sn);
W("TLPasswordInput", un);
W("TLNumberInput", mn);
W("TLDatePicker", fn);
W("TLSelect", bn);
W("TLCheckbox", En);
W("TLCounter", kn);
W("TLTabBar", Sn);
W("TLFieldList", Dn);
W("TLAudioRecorder", Rn);
W("TLAudioPlayer", xn);
W("TLFileUpload", In);
W("TLBinaryField", Pn);
W("TLFileChips", On);
W("TLRelativeTime", Hn);
W("TLAnchor", Wn);
W("TLScrollLink", Un);
W("TLAvatar", Kn);
W("TLDownload", Gn);
W("TLPhotoCapture", qn);
W("TLPhotoViewer", Qn);
W("TLPdfViewer", el);
W("TLSplitPanel", tl);
W("TLPanel", cl);
W("TLInset", _l);
W("TLMaximizeRoot", il);
W("TLDeckPane", ul);
W("TLSidebar", El);
W("TLStack", vl);
W("TLGrid", Cl);
W("TLCard", yl);
W("TLAppBar", wl);
W("TLBreadcrumb", Nl);
W("TLBottomBar", Dl);
W("TLDialog", Ll);
W("TLDialogManager", Il);
W("TLWindow", Bl);
W("TLDrawer", $l);
W("TLContextMenuRegion", Wl);
W("TLSnackbar", Kl);
W("TLMenu", Gl);
W("TLAppShell", ql);
W("TLText", Zl);
W("TLTableView", ea);
W("TLCalendar", ha);
W("TLFormLayout", ya);
W("TLFormGroup", Na);
W("TLFormField", Ra);
W("TLResourceCell", La);
W("TLTreeView", Ma);
W("TLDropdownSelect", Fa);
W("TLColorInput", er);
W("TLIconSelect", cr);
W("TLDashboard", gr);
W("TLToolbar", wr);
W("TLTileStack", kr);
W("TLAdaptiveDetail", Nr);
W("TLSlot", Sr);
W("TLSlotContent", Dr);
W("TLDrawerToggle", Rr);
