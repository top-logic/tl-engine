import { React as e, useTLFieldValue as Se, useTLCommand as le, useTLState as q, useKeyboardBinding as ue, useTLUpload as Ae, TLChild as z, useI18N as ce, useTLDataUrl as Oe, useStandaloneKeyboardScope as Ne, KeyboardScopeProvider as st, useFocusTrap as ct, anchoredOverlayProps as $t, register as U } from "tl-react-bridge";
const { useCallback: pt, useRef: Ft } = e, Ut = 300, Ht = ({ controlId: l, state: t }) => {
  const [n, r, c] = Se({ debounceMs: Ut }), s = le(), i = Ft(!1), u = pt(
    (S) => {
      i.current = !0, r(S.target.value);
    },
    [r]
  ), a = t.commitOnBlur === !0, o = pt(async () => {
    await c(), a && i.current && (i.current = !1, s("commit"));
  }, [c, a, s]), m = t.multiline === !0;
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
  const p = t.hasError === !0, f = t.hasWarnings === !0, v = t.errorMessage, h = [
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
      className: h,
      "aria-invalid": p || void 0,
      title: p && v ? v : void 0
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
      "aria-invalid": p || void 0,
      title: p && v ? v : void 0
    }
  ));
}, { useCallback: ft } = e, Wt = 300, zt = ({ controlId: l, state: t }) => {
  const [n, r, c] = Se({ debounceMs: Wt }), s = ft(
    (p) => {
      r(p.target.value);
    },
    [r]
  ), i = ft(() => {
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
}, { useCallback: ht } = e, Vt = 300, Kt = ({ controlId: l, state: t, config: n }) => {
  const [r, c, s] = Se({ debounceMs: Vt }), i = ht(
    (f) => {
      const v = f.target.value;
      c(v === "" ? null : v);
    },
    [c]
  ), u = ht(() => {
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
}, { useCallback: Yt } = e, Gt = ({ controlId: l, state: t }) => {
  const [n, r] = Se(), c = Yt(
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
}, { useCallback: Xt } = e, qt = ({ controlId: l, state: t, config: n }) => {
  var m;
  const [r, c] = Se(), s = Xt(
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
}, { useCallback: Zt } = e, Qt = ({ controlId: l, state: t }) => {
  const [n, r] = Se(), c = Zt(
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
const { useCallback: Jt } = e, en = ({ controlId: l, command: t, label: n, image: r, disabled: c, displayMode: s }) => {
  const i = q(), u = le(), a = t ?? "click", o = n ?? i.label, m = r ?? i.image, p = c ?? i.disabled === !0, f = s ?? i.displayMode ?? "label-only", v = i.hidden === !0, h = i.tooltip, S = v ? { display: "none" } : void 0, w = i.appearance, C = i.size, E = i.navigateUrl, T = Jt(() => {
    if (E) {
      window.location.assign(E);
      return;
    }
    u(a);
  }, [u, a, E]), D = i.keyGesture;
  ue(D, () => p || v ? !1 : (T(), !0));
  const _ = f === "icon-only", g = f === "icon-only" || f === "icon-label", b = f === "label-only" || f === "icon-label" || _ && !m, V = h ?? (_ ? o : void 0), L = V ? `text:${V}` : void 0;
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: T,
      disabled: p,
      style: S,
      className: "tlReactButton" + (_ ? " tlReactButton--iconOnly" : "") + (w === "link" ? " tlReactButton--link" : "") + (w === "primary" ? " tlReactButton--primary" : "") + (C === "small" ? " tlReactButton--small" : "") + (C === "large" ? " tlReactButton--large" : ""),
      "data-tooltip": L,
      "aria-label": _ ? o : void 0
    },
    g && m && /* @__PURE__ */ e.createElement(Ee, { encoded: m, className: "tlReactButton__image" }),
    b && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, o)
  );
}, tn = ({ controlId: l }) => {
  const t = q(), n = Ae(), r = e.useRef(null), [c, s] = e.useState(!1), i = t.label ?? "", u = t.image, a = t.disabled === !0, o = t.hidden === !0, m = t.displayMode ?? "label-only", p = t.appearance, f = t.accept, v = t.multiple === !0, h = e.useCallback(() => {
    var D;
    a || c || (D = r.current) == null || D.click();
  }, [a, c]), S = e.useCallback(async (D) => {
    const _ = D.target.files;
    if (!_ || _.length === 0) return;
    const g = new FormData();
    for (let b = 0; b < _.length; b++)
      g.append("file", _[b], _[b].name);
    D.target.value = "", s(!0);
    try {
      await n(g);
    } finally {
      s(!1);
    }
  }, [n]), w = m === "icon-only", C = m === "icon-only" || m === "icon-label", E = m === "label-only" || m === "icon-label" || w && !u, T = a || c;
  return /* @__PURE__ */ e.createElement("span", { id: l, style: { display: "contents" } }, /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: r,
      type: "file",
      accept: f && f !== "*" ? f : void 0,
      multiple: v || void 0,
      onChange: S,
      style: { display: "none" }
    }
  ), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: h,
      disabled: T,
      style: o ? { display: "none" } : void 0,
      className: "tlReactButton" + (w ? " tlReactButton--iconOnly" : "") + (p === "link" ? " tlReactButton--link" : "") + (p === "primary" ? " tlReactButton--primary" : ""),
      "aria-label": w ? i : void 0
    },
    C && u && /* @__PURE__ */ e.createElement(Ee, { encoded: u, className: "tlReactButton__image" }),
    E && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, i)
  ));
}, { useCallback: nn } = e, ln = ({ controlId: l, command: t, label: n, active: r, disabled: c }) => {
  const s = q(), i = le(), u = t ?? "click", a = n ?? s.label, o = r ?? s.active === !0, m = c ?? s.disabled === !0, p = nn(() => {
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
}, rn = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.count ?? 0, c = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, r), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: an } = e, on = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.tabs ?? [], c = t.activeTabId, s = an((i) => {
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
    i.icon && /* @__PURE__ */ e.createElement(Ee, { encoded: i.icon, className: "tlReactTabBar__tabIcon" }),
    i.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(z, { control: t.activeContent })));
}, sn = ({ controlId: l }) => {
  const t = q(), n = t.title, r = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, r.map((c, s) => /* @__PURE__ */ e.createElement("div", { key: s, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(z, { control: c })))));
}, cn = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, un = ({ controlId: l }) => {
  const t = q(), n = Ae(), [r, c] = e.useState("idle"), [s, i] = e.useState(null), u = e.useRef(null), a = e.useRef([]), o = e.useRef(null), m = t.status ?? "idle", p = t.error, f = m === "received" ? "idle" : r !== "idle" ? r : m, v = e.useCallback(async () => {
    if (r === "recording") {
      const E = u.current;
      E && E.state !== "inactive" && E.stop();
      return;
    }
    if (r !== "uploading") {
      if (i(null), !window.isSecureContext || !navigator.mediaDevices) {
        i("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const E = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        o.current = E, a.current = [];
        const T = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", D = new MediaRecorder(E, T ? { mimeType: T } : void 0);
        u.current = D, D.ondataavailable = (_) => {
          _.data.size > 0 && a.current.push(_.data);
        }, D.onstop = async () => {
          E.getTracks().forEach((b) => b.stop()), o.current = null;
          const _ = new Blob(a.current, { type: D.mimeType || "audio/webm" });
          if (a.current = [], _.size === 0) {
            c("idle");
            return;
          }
          c("uploading");
          const g = new FormData();
          g.append("audio", _, "recording.webm"), await n(g), c("idle");
        }, D.start(), c("recording");
      } catch (E) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", E), i("js.audioRecorder.error.denied"), c("idle");
      }
    }
  }, [r, n]), h = ce(cn), S = f === "recording" ? h["js.audioRecorder.stop"] : f === "uploading" ? h["js.uploading"] : h["js.audioRecorder.record"], w = f === "uploading", C = ["tlAudioRecorder__button"];
  return f === "recording" && C.push("tlAudioRecorder__button--recording"), f === "uploading" && C.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: C.join(" "),
      onClick: v,
      disabled: w,
      title: S,
      "aria-label": S
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${f === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, h[s]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, dn = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, mn = ({ controlId: l }) => {
  const t = q(), n = Oe(), r = !!t.hasAudio, c = t.dataRevision ?? 0, [s, i] = e.useState(r ? "idle" : "disabled"), u = e.useRef(null), a = e.useRef(null), o = e.useRef(c);
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
        const w = await fetch(n);
        if (!w.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", w.status), i("idle");
          return;
        }
        const C = await w.blob();
        a.current = URL.createObjectURL(C);
      } catch (w) {
        console.error("[TLAudioPlayer] Fetch error:", w), i("idle");
        return;
      }
    }
    const S = new Audio(a.current);
    u.current = S, S.onended = () => {
      i("idle");
    }, S.play(), i("playing");
  }, [s, n]), p = ce(dn), f = s === "loading" ? p["js.loading"] : s === "playing" ? p["js.audioPlayer.pause"] : s === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], v = s === "disabled" || s === "loading", h = ["tlAudioPlayer__button"];
  return s === "playing" && h.push("tlAudioPlayer__button--playing"), s === "loading" && h.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: h.join(" "),
      onClick: m,
      disabled: v,
      title: f,
      "aria-label": f
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${s === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, pn = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, fn = ({ controlId: l }) => {
  const t = q(), n = Ae(), [r, c] = e.useState("idle"), [s, i] = e.useState(!1), u = e.useRef(null), a = t.status ?? "idle", o = t.error, m = t.accept ?? "", p = a === "received" ? "idle" : r !== "idle" ? r : a, f = e.useCallback(async (_) => {
    c("uploading");
    const g = new FormData();
    g.append("file", _, _.name), await n(g), c("idle");
  }, [n]), v = e.useCallback((_) => {
    var b;
    const g = (b = _.target.files) == null ? void 0 : b[0];
    g && f(g);
  }, [f]), h = e.useCallback(() => {
    var _;
    r !== "uploading" && ((_ = u.current) == null || _.click());
  }, [r]), S = e.useCallback((_) => {
    _.preventDefault(), _.stopPropagation(), i(!0);
  }, []), w = e.useCallback((_) => {
    _.preventDefault(), _.stopPropagation(), i(!1);
  }, []), C = e.useCallback((_) => {
    var b;
    if (_.preventDefault(), _.stopPropagation(), i(!1), r === "uploading") return;
    const g = (b = _.dataTransfer.files) == null ? void 0 : b[0];
    g && f(g);
  }, [r, f]), E = p === "uploading", T = ce(pn), D = p === "uploading" ? T["js.uploading"] : T["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${s ? " tlFileUpload--dragover" : ""}`,
      onDragOver: S,
      onDragLeave: w,
      onDrop: C
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: u,
        type: "file",
        accept: m || void 0,
        onChange: v,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (p === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: h,
        disabled: E,
        title: D,
        "aria-label": D
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    o && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, o)
  );
}, hn = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, bn = ({ controlId: l, state: t }) => {
  const r = q() ?? t ?? {}, c = Ae(), s = Oe(), i = ce(hn), u = r.editable !== !1, a = !!r.hasData, o = r.fileName ?? "download", m = r.dataRevision ?? 0, p = r.accept ?? "", f = r.status ?? "idle", v = r.error ?? null, [h, S] = e.useState("idle"), [w, C] = e.useState(!1), [E, T] = e.useState(!1), D = e.useRef(null), _ = e.useCallback(async () => {
    if (!(!a || E)) {
      T(!0);
      try {
        const A = s + (s.includes("?") ? "&" : "?") + "rev=" + m, j = await fetch(A);
        if (!j.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", j.status);
          return;
        }
        const P = await j.blob(), Y = URL.createObjectURL(P), d = document.createElement("a");
        d.href = Y, d.download = o, d.style.display = "none", document.body.appendChild(d), d.click(), document.body.removeChild(d), URL.revokeObjectURL(Y);
      } catch (A) {
        console.error("[TLBinaryField] Fetch error:", A);
      } finally {
        T(!1);
      }
    }
  }, [a, E, s, m, o]), g = e.useCallback(async (A) => {
    S("uploading");
    const j = new FormData();
    j.append("file", A, A.name), await c(j), S("idle");
  }, [c]), b = (f === "received" ? "idle" : h !== "idle" ? h : f) === "uploading", V = e.useCallback((A) => {
    var P;
    const j = (P = A.target.files) == null ? void 0 : P[0];
    j && g(j);
  }, [g]), L = e.useCallback(() => {
    var A;
    b || (A = D.current) == null || A.click();
  }, [b]), R = e.useCallback((A) => {
    A.preventDefault(), A.stopPropagation(), C(!0);
  }, []), K = e.useCallback((A) => {
    A.preventDefault(), A.stopPropagation(), C(!1);
  }, []), B = e.useCallback((A) => {
    var P;
    if (A.preventDefault(), A.stopPropagation(), C(!1), b) return;
    const j = (P = A.dataTransfer.files) == null ? void 0 : P[0];
    j && g(j);
  }, [b, g]), N = E ? i["js.downloading"] : i["js.download.file"].replace("{0}", o), O = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (E ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: _,
      disabled: E,
      title: N,
      "aria-label": N
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o));
  if (!u)
    return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, O) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, i["js.download.noFile"]));
  const Q = b, H = b ? i["js.uploading"] : i["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlBinaryField tlFileUpload${w ? " tlFileUpload--dragover" : ""}`,
      onDragOver: R,
      onDragLeave: K,
      onDrop: B
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: D,
        type: "file",
        accept: p || void 0,
        onChange: V,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (Q ? " tlFileUpload__button--uploading" : ""),
        onClick: L,
        disabled: Q,
        title: H,
        "aria-label": H
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    a && O,
    v && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, v)
  );
}, _n = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, gn = ({ controlId: l }) => {
  const t = q(), n = Oe(), r = le(), c = !!t.hasData, s = t.dataRevision ?? 0, i = t.fileName ?? "download", u = !!t.clearable, [a, o] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!c || a)) {
      o(!0);
      try {
        const h = n + (n.includes("?") ? "&" : "?") + "rev=" + s, S = await fetch(h);
        if (!S.ok) {
          console.error("[TLDownload] Failed to fetch data:", S.status);
          return;
        }
        const w = await S.blob(), C = URL.createObjectURL(w), E = document.createElement("a");
        E.href = C, E.download = i, E.style.display = "none", document.body.appendChild(E), E.click(), document.body.removeChild(E), URL.revokeObjectURL(C);
      } catch (h) {
        console.error("[TLDownload] Fetch error:", h);
      } finally {
        o(!1);
      }
    }
  }, [c, a, n, s, i]), p = e.useCallback(async () => {
    c && await r("clear");
  }, [c, r]), f = ce(_n);
  if (!c)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, f["js.download.noFile"]));
  const v = a ? f["js.downloading"] : f["js.download.file"].replace("{0}", i);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (a ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: m,
      disabled: a,
      title: v,
      "aria-label": v
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
}, vn = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, En = ({ controlId: l }) => {
  const t = q(), n = Ae(), [r, c] = e.useState("idle"), [s, i] = e.useState(null), [u, a] = e.useState(!1), o = e.useRef(null), m = e.useRef(null), p = e.useRef(null), f = e.useRef(null), v = e.useRef(null), h = t.error, S = e.useMemo(
    () => {
      var R;
      return !!(window.isSecureContext && ((R = navigator.mediaDevices) != null && R.getUserMedia));
    },
    []
  ), w = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((R) => R.stop()), m.current = null), o.current && (o.current.srcObject = null);
  }, []), C = e.useCallback(() => {
    w(), c("idle");
  }, [w]), E = e.useCallback(async () => {
    var R;
    if (r !== "uploading") {
      if (i(null), !S) {
        (R = f.current) == null || R.click();
        return;
      }
      try {
        const K = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        m.current = K, c("overlayOpen");
      } catch (K) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", K), i("js.photoCapture.error.denied"), c("idle");
      }
    }
  }, [r, S]), T = e.useCallback(async () => {
    if (r !== "overlayOpen")
      return;
    const R = o.current, K = p.current;
    if (!R || !K)
      return;
    K.width = R.videoWidth, K.height = R.videoHeight;
    const B = K.getContext("2d");
    B && (B.drawImage(R, 0, 0), w(), c("uploading"), K.toBlob(async (N) => {
      if (!N) {
        c("idle");
        return;
      }
      const O = new FormData();
      O.append("photo", N, "capture.jpg"), await n(O), c("idle");
    }, "image/jpeg", 0.85));
  }, [r, n, w]), D = e.useCallback(async (R) => {
    var N;
    const K = (N = R.target.files) == null ? void 0 : N[0];
    if (!K) return;
    c("uploading");
    const B = new FormData();
    B.append("photo", K, K.name), await n(B), c("idle"), f.current && (f.current.value = "");
  }, [n]);
  e.useEffect(() => {
    r === "overlayOpen" && o.current && m.current && (o.current.srcObject = m.current);
  }, [r]), e.useEffect(() => {
    var K;
    if (r !== "overlayOpen") return;
    (K = v.current) == null || K.focus();
    const R = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = R;
    };
  }, [r]), Ne(r === "overlayOpen", { ESCAPE: C }), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((R) => R.stop()), m.current = null);
  }, []);
  const _ = ce(vn), g = r === "uploading" ? _["js.uploading"] : _["js.photoCapture.open"], b = ["tlPhotoCapture__cameraBtn"];
  r === "uploading" && b.push("tlPhotoCapture__cameraBtn--uploading");
  const V = ["tlPhotoCapture__overlayVideo"];
  u && V.push("tlPhotoCapture__overlayVideo--mirrored");
  const L = ["tlPhotoCapture__mirrorBtn"];
  return u && L.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: b.join(" "),
      onClick: E,
      disabled: r === "uploading",
      title: g,
      "aria-label": g
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
      onChange: D
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: p, style: { display: "none" } }), r === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: v,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: C }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: o,
        className: V.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: L.join(" "),
        onClick: () => a((R) => !R),
        title: _["js.photoCapture.mirror"],
        "aria-label": _["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: T,
        title: _["js.photoCapture.capture"],
        "aria-label": _["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: C,
        title: _["js.photoCapture.close"],
        "aria-label": _["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, _[s]), h && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, h));
}, Cn = {
  "js.photoViewer.alt": "Captured photo"
}, wn = ({ controlId: l }) => {
  const t = q(), n = Oe(), r = !!t.hasPhoto, c = t.dataRevision ?? 0, [s, i] = e.useState(null), u = e.useRef(c);
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
  const a = ce(Cn);
  return !r || !s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: s,
      alt: a["js.photoViewer.alt"]
    }
  ));
}, yn = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, Sn = ({ controlId: l }) => {
  const t = q(), n = Oe(), r = !!t.hasPdf, c = t.dataRevision ?? 0, s = ce(yn), u = n.indexOf("react-api/"), a = u >= 0 ? n.slice(0, u) : n, o = n + "&rev=" + c, m = a + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(o);
  return r ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: m,
      title: s["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, s["js.pdfViewer.noDocument"]));
}, { useCallback: bt, useRef: Ge } = e, kn = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.orientation, c = t.resizable === !0, s = t.children ?? [], i = r === "horizontal", u = s.length > 0 && s.every((w) => w.collapsed), a = !u && s.some((w) => w.collapsed), o = u ? !i : i, m = Ge(null), p = Ge(null), f = Ge(null), v = bt((w, C) => {
    const E = {
      overflow: w.scrolling || "auto"
    };
    return w.collapsed ? u && !o ? E.flex = "1 0 0%" : E.flex = "0 0 auto" : C !== void 0 ? E.flex = `0 0 ${C}px` : E.flex = `${w.size} 1 0%`, w.minSize > 0 && !w.collapsed && (E.minWidth = i ? w.minSize : void 0, E.minHeight = i ? void 0 : w.minSize), E;
  }, [i, u, a, o]), h = bt((w, C) => {
    w.preventDefault();
    const E = m.current;
    if (!E) return;
    const T = s[C], D = s[C + 1], _ = E.querySelectorAll(":scope > .tlSplitPanel__child"), g = [];
    _.forEach((L) => {
      g.push(i ? L.offsetWidth : L.offsetHeight);
    }), f.current = g, p.current = {
      splitterIndex: C,
      startPos: i ? w.clientX : w.clientY,
      startSizeBefore: g[C],
      startSizeAfter: g[C + 1],
      childBefore: T,
      childAfter: D
    };
    const b = (L) => {
      const R = p.current;
      if (!R || !f.current) return;
      const B = (i ? L.clientX : L.clientY) - R.startPos, N = R.childBefore.minSize || 0, O = R.childAfter.minSize || 0;
      let Q = R.startSizeBefore + B, H = R.startSizeAfter - B;
      Q < N && (H += Q - N, Q = N), H < O && (Q += H - O, H = O), f.current[R.splitterIndex] = Q, f.current[R.splitterIndex + 1] = H;
      const A = E.querySelectorAll(":scope > .tlSplitPanel__child"), j = A[R.splitterIndex], P = A[R.splitterIndex + 1];
      j && (j.style.flex = `0 0 ${Q}px`), P && (P.style.flex = `0 0 ${H}px`);
    }, V = () => {
      if (document.removeEventListener("mousemove", b), document.removeEventListener("mouseup", V), document.body.style.cursor = "", document.body.style.userSelect = "", f.current) {
        const L = {};
        s.forEach((R, K) => {
          const B = R.control;
          B != null && B.controlId && f.current && (L[B.controlId] = f.current[K]);
        }), n("updateSizes", { sizes: L });
      }
      f.current = null, p.current = null;
    };
    document.addEventListener("mousemove", b), document.addEventListener("mouseup", V), document.body.style.cursor = i ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [s, i, n]), S = [];
  return s.forEach((w, C) => {
    if (S.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${C}`,
          className: `tlSplitPanel__child${w.collapsed && o ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: v(w)
        },
        /* @__PURE__ */ e.createElement(z, { control: w.control })
      )
    ), c && C < s.length - 1) {
      const E = s[C + 1];
      !w.collapsed && !E.collapsed && S.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${C}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${r}`,
            onMouseDown: (D) => h(D, C)
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
    S
  );
}, { useCallback: Xe } = e, Nn = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Tn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Rn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), Dn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), xn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), Ln = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), In = ({ controlId: l }) => {
  const t = q(), n = le(), r = ce(Nn), c = t.title, s = t.expansionState ?? "NORMALIZED", i = t.showMinimize === !0, u = t.showMaximize === !0, a = t.showPopOut === !0, o = t.fullLine === !0, m = t.fill === !0, p = s === "MINIMIZED", f = s === "MAXIMIZED", v = s === "HIDDEN", h = Xe(() => {
    n("toggleMinimize");
  }, [n]), S = Xe(() => {
    n("toggleMaximize");
  }, [n]), w = Xe(() => {
    n("popOut");
  }, [n]);
  if (v)
    return null;
  const C = f ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" }, E = i && !f || u && !p || a, T = !!c && c.trim() !== "" || !!t.toolbar || E;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${s.toLowerCase()}${o ? " tlPanel--fullLine" : ""}${m ? " tlPanel--fill" : ""}`,
      style: C
    },
    T && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(z, { control: t.toolbar }), i && !f && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: h,
        title: p ? r["js.panel.restore"] : r["js.panel.minimize"]
      },
      p ? /* @__PURE__ */ e.createElement(Rn, null) : /* @__PURE__ */ e.createElement(Tn, null)
    ), u && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: S,
        title: f ? r["js.panel.restore"] : r["js.panel.maximize"]
      },
      f ? /* @__PURE__ */ e.createElement(xn, null) : /* @__PURE__ */ e.createElement(Dn, null)
    ), a && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: w,
        title: r["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(Ln, null)
    ))),
    !p && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(z, { control: t.child })),
    !p && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(z, { control: t.buttonBar }))
  );
}, Pn = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(z, { control: t.child })
  );
}, Mn = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(z, { control: t.activeChild }));
}, { useCallback: he, useState: Ke, useEffect: lt, useRef: Ye } = e, jn = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function rt(l, t, n, r) {
  const c = [];
  for (const s of l)
    if (s.type === "nav") {
      if (s.hidden) continue;
      c.push({ id: s.id, type: "nav", groupId: r });
    } else s.type === "command" ? c.push({ id: s.id, type: "command", groupId: r }) : s.type === "group" && (c.push({ id: s.id, type: "group" }), (n.get(s.id) ?? s.expanded) && !t && c.push(...rt(s.children, t, n, s.id)));
  return c;
}
const Me = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(Ee, { encoded: l, className: "tlSidebar__icon" }) : null, Bn = ({ item: l, active: t, collapsed: n, onSelect: r, tabIndex: c, itemRef: s, onFocus: i }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => r(l.id),
    title: n ? l.label : void 0,
    tabIndex: c,
    ref: s,
    onFocus: () => i(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Me, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Me, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), An = ({ item: l, collapsed: t, onExecute: n, tabIndex: r, itemRef: c, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: r,
    ref: c,
    onFocus: () => s(l.id)
  },
  /* @__PURE__ */ e.createElement(Me, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), On = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Me, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), $n = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), Fn = ({ item: l, activeItemId: t, anchorRect: n, onSelect: r, onExecute: c, onClose: s }) => {
  const i = Ye(null);
  lt(() => {
    const o = (m) => {
      i.current && !i.current.contains(m.target) && setTimeout(() => s(), 0);
    };
    return document.addEventListener("mousedown", o), () => document.removeEventListener("mousedown", o);
  }, [s]), Ne(!0, { ESCAPE: s });
  const u = he((o) => {
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
        /* @__PURE__ */ e.createElement(Me, { icon: o.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, o.label),
        o.type === "nav" && o.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, o.badge)
      );
    }
    return o.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: o.id, className: "tlSidebar__flyoutSectionHeader" }, o.label) : o.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: o.id, className: "tlSidebar__separator" }) : null;
  }));
}, Un = ({
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
  flyoutGroupId: v,
  onOpenFlyout: h,
  onCloseFlyout: S
}) => {
  const w = Ye(null), [C, E] = Ke(null), T = he(() => {
    r ? v === l.id ? S() : (w.current && E(w.current.getBoundingClientRect()), h(l.id)) : i(l.id);
  }, [r, v, l.id, i, h, S]), D = he((g) => {
    w.current = g, a(g);
  }, [a]), _ = r && v === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (_ ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: T,
      title: r ? l.label : void 0,
      "aria-expanded": r ? _ : t,
      tabIndex: u,
      ref: D,
      onFocus: () => o(l.id)
    },
    /* @__PURE__ */ e.createElement(Me, { icon: l.icon }),
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
  ), _ && /* @__PURE__ */ e.createElement(
    Fn,
    {
      item: l,
      activeItemId: n,
      anchorRect: C,
      onSelect: c,
      onExecute: s,
      onClose: S
    }
  ), t && !r && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((g) => /* @__PURE__ */ e.createElement(
    Lt,
    {
      key: g.id,
      item: g,
      activeItemId: n,
      collapsed: r,
      onSelect: c,
      onExecute: s,
      onToggleGroup: i,
      focusedId: m,
      setItemRef: p,
      onItemFocus: f,
      groupStates: null,
      flyoutGroupId: v,
      onOpenFlyout: h,
      onCloseFlyout: S
    }
  ))));
}, Lt = ({
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
        Bn,
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
        An,
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
      return /* @__PURE__ */ e.createElement(On, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement($n, null);
    case "group": {
      const v = o ? o.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        Un,
        {
          item: l,
          expanded: v,
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
}, Hn = ({ controlId: l }) => {
  const t = q(), n = le(), r = ce(jn), c = t.items ?? [], s = t.activeItemId, i = t.collapsed, u = t.drawerOpen, a = u ? !1 : i, [o, m] = Ke(() => {
    const N = /* @__PURE__ */ new Map(), O = (Q) => {
      for (const H of Q)
        H.type === "group" && (N.set(H.id, H.expanded), O(H.children));
    };
    return O(c), N;
  }), p = he((N) => {
    m((O) => {
      const Q = new Map(O), H = Q.get(N) ?? !1;
      return Q.set(N, !H), n("toggleGroup", { itemId: N, expanded: !H }), Q;
    });
  }, [n]), f = he((N) => {
    N !== s && n("selectItem", { itemId: N });
  }, [n, s]), v = he((N) => {
    n("executeCommand", { itemId: N });
  }, [n]), h = he(() => {
    n("toggleCollapse", {});
  }, [n]), S = he(() => {
    n("toggleDrawer", {});
  }, [n]), [w, C] = Ke(null), E = he((N) => {
    C(N);
  }, []), T = he(() => {
    C(null);
  }, []);
  lt(() => {
    a || C(null);
  }, [a]);
  const [D, _] = Ke(() => {
    const N = rt(c, a, o);
    return N.length > 0 ? N[0].id : "";
  }), g = Ye(/* @__PURE__ */ new Map()), b = he((N) => (O) => {
    O ? g.current.set(N, O) : g.current.delete(N);
  }, []), V = he((N) => {
    _(N);
  }, []), L = Ye(0), R = he((N) => {
    _(N), L.current++;
  }, []);
  lt(() => {
    const N = g.current.get(D);
    N && document.activeElement !== N && N.focus();
  }, [D, L.current]);
  const K = he((N) => {
    if (N.key === "Escape" && w !== null) {
      N.preventDefault(), T();
      return;
    }
    const O = rt(c, a, o);
    if (O.length === 0) return;
    const Q = O.findIndex((A) => A.id === D);
    if (Q < 0) return;
    const H = O[Q];
    switch (N.key) {
      case "ArrowDown": {
        N.preventDefault();
        const A = (Q + 1) % O.length;
        R(O[A].id);
        break;
      }
      case "ArrowUp": {
        N.preventDefault();
        const A = (Q - 1 + O.length) % O.length;
        R(O[A].id);
        break;
      }
      case "Home": {
        N.preventDefault(), R(O[0].id);
        break;
      }
      case "End": {
        N.preventDefault(), R(O[O.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        N.preventDefault(), H.type === "nav" ? f(H.id) : H.type === "command" ? v(H.id) : H.type === "group" && (a ? w === H.id ? T() : E(H.id) : p(H.id));
        break;
      }
      case "ArrowRight": {
        H.type === "group" && !a && ((o.get(H.id) ?? !1) || (N.preventDefault(), p(H.id)));
        break;
      }
      case "ArrowLeft": {
        H.type === "group" && !a && (o.get(H.id) ?? !1) && (N.preventDefault(), p(H.id));
        break;
      }
    }
  }, [
    c,
    a,
    o,
    D,
    w,
    R,
    f,
    v,
    p,
    E,
    T
  ]), B = "tlSidebar" + (a ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: B }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(z, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: S, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": r["js.sidebar.ariaLabel"] }, a ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(z, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(z, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: K }, c.map((N) => /* @__PURE__ */ e.createElement(
    Lt,
    {
      key: N.id,
      item: N,
      activeItemId: s,
      collapsed: a,
      onSelect: f,
      onExecute: v,
      onToggleGroup: p,
      focusedId: D,
      setItemRef: b,
      onItemFocus: V,
      groupStates: o,
      flyoutGroupId: w,
      onOpenFlyout: E,
      onCloseFlyout: T
    }
  ))), a ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(z, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(z, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: h,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(z, { control: t.activeContent })));
}, Wn = ({ controlId: l }) => {
  const t = q(), n = t.direction ?? "column", r = t.gap ?? "default", c = t.align ?? "stretch", s = t.wrap === !0, i = t.growFirst === !0, u = t.children ?? [], a = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${r}`,
    `tlStack--align-${c}`,
    s ? "tlStack--wrap" : "",
    i ? "tlStack--grow-first" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: a }, u.map((o, m) => /* @__PURE__ */ e.createElement(z, { key: m, control: o })));
}, zn = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlInset" }, t.child && /* @__PURE__ */ e.createElement(z, { control: t.child }));
}, Vn = ({ controlId: l }) => {
  const t = q(), n = t.columns, r = t.minColumnWidth, c = t.gap ?? "default", s = t.children ?? [], i = {};
  return r ? i.gridTemplateColumns = `repeat(auto-fit, minmax(${r}, 1fr))` : n && (i.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${c}`, style: i }, s.map((u, a) => /* @__PURE__ */ e.createElement(z, { key: a, control: u })));
}, Kn = ({ controlId: l }) => {
  const t = q(), n = t.title, r = t.variant ?? "outlined", c = t.padding ?? "default", s = t.headerActions ?? [], i = t.child, u = n != null || s.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${r}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, s.map((a, o) => /* @__PURE__ */ e.createElement(z, { key: o, control: a })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${c}` }, /* @__PURE__ */ e.createElement(z, { control: i })));
}, Yn = ({ controlId: l }) => {
  const t = q(), n = t.title ?? "", r = t.leading, c = t.children ?? [], s = t.actions ?? [], i = t.variant ?? "flat", a = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    i === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: a }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(z, { control: r })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, c.map((o, m) => /* @__PURE__ */ e.createElement(z, { key: m, control: o }))), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((o, m) => /* @__PURE__ */ e.createElement(z, { key: m, control: o }))));
}, { useCallback: Gn } = e, Xn = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.items ?? [], c = Gn((s) => {
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
}, { useCallback: qn } = e, Zn = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.items ?? [], c = t.activeItemId, s = qn((i) => {
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
}, { useCallback: _t, useRef: Qn } = e, Jn = ({ onClose: l }) => (ue("ESCAPE", () => (l(), !0)), null), el = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.open === !0, c = t.closeOnBackdrop !== !1, s = t.child, i = Qn(null), u = _t(() => {
    n("close");
  }, [n]), a = _t((o) => {
    c && o.target === o.currentTarget && u();
  }, [c, u]);
  return r ? /* @__PURE__ */ e.createElement(st, null, /* @__PURE__ */ e.createElement(Jn, { onClose: u }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: a,
      ref: i,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(z, { control: s })
  )) : null;
}, { useEffect: tl, useRef: nl } = e, ll = ({ controlId: l }) => {
  const n = q().dialogs ?? [], r = nl(n.length);
  return tl(() => {
    n.length < r.current && n.length > 0, r.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((c) => /* @__PURE__ */ e.createElement(z, { key: c.controlId, control: c })));
}, { useCallback: Ue, useRef: xe, useState: He } = e, rl = ({ onClose: l }) => (ue("ESCAPE", () => (l(), !0)), null), al = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, ol = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], sl = ({ controlId: l }) => {
  const t = q(), n = le(), r = ce(al), c = t.title ?? "", s = t.width ?? "32rem", i = t.height ?? null, u = t.minHeight ?? null, a = t.resizable === !0, o = t.child, m = t.actions ?? [], p = t.toolbar, f = t.buttonBar, [v, h] = He(null), [S, w] = He(null), [C, E] = He(null), T = xe(null), [D, _] = He(!1), g = xe(null), b = xe(null), V = xe(null), L = xe(null), R = xe(null), K = Ue(() => {
    n("close");
  }, [n]);
  ct(!0, L, "field");
  const B = Ue((A, j) => {
    j.preventDefault();
    const P = L.current;
    if (!P) return;
    const Y = P.getBoundingClientRect(), d = !T.current, k = T.current ?? { x: Y.left, y: Y.top };
    d && (T.current = k, E(k)), R.current = {
      dir: A,
      startX: j.clientX,
      startY: j.clientY,
      startW: Y.width,
      startH: Y.height,
      startPos: { ...k },
      symmetric: d
    };
    const F = (X) => {
      const M = R.current;
      if (!M) return;
      const J = X.clientX - M.startX, re = X.clientY - M.startY;
      let ne = M.startW, pe = M.startH, be = 0, _e = 0;
      M.symmetric ? (M.dir.includes("e") && (ne = M.startW + 2 * J), M.dir.includes("w") && (ne = M.startW - 2 * J), M.dir.includes("s") && (pe = M.startH + 2 * re), M.dir.includes("n") && (pe = M.startH - 2 * re)) : (M.dir.includes("e") && (ne = M.startW + J), M.dir.includes("w") && (ne = M.startW - J, be = J), M.dir.includes("s") && (pe = M.startH + re), M.dir.includes("n") && (pe = M.startH - re, _e = re));
      const Ce = Math.max(200, ne), we = Math.max(100, pe);
      M.symmetric ? (be = (M.startW - Ce) / 2, _e = (M.startH - we) / 2) : (M.dir.includes("w") && Ce === 200 && (be = M.startW - 200), M.dir.includes("n") && we === 100 && (_e = M.startH - 100)), b.current = Ce, V.current = we, h(Ce), w(we);
      const Te = {
        x: M.startPos.x + be,
        y: M.startPos.y + _e
      };
      T.current = Te, E(Te);
    }, $ = () => {
      document.removeEventListener("mousemove", F), document.removeEventListener("mouseup", $);
      const X = b.current, M = V.current;
      (X != null || M != null) && n("resize", {
        ...X != null ? { width: Math.round(X) } : {},
        ...M != null ? { height: Math.round(M) } : {}
      }), R.current = null;
    };
    document.addEventListener("mousemove", F), document.addEventListener("mouseup", $);
  }, [n]), N = Ue((A) => {
    if (A.button !== 0 || A.target.closest("button")) return;
    A.preventDefault();
    const j = L.current;
    if (!j) return;
    const P = j.getBoundingClientRect(), Y = T.current ?? { x: P.left, y: P.top }, d = A.clientX - Y.x, k = A.clientY - Y.y, F = (X) => {
      const M = window.innerWidth, J = window.innerHeight;
      let re = X.clientX - d, ne = X.clientY - k;
      const pe = j.offsetWidth, be = j.offsetHeight;
      re + pe > M && (re = M - pe), ne + be > J && (ne = J - be), re < 0 && (re = 0), ne < 0 && (ne = 0);
      const _e = { x: re, y: ne };
      T.current = _e, E(_e);
    }, $ = () => {
      document.removeEventListener("mousemove", F), document.removeEventListener("mouseup", $);
    };
    document.addEventListener("mousemove", F), document.addEventListener("mouseup", $);
  }, []), O = Ue(() => {
    var A, j;
    if (D) {
      const P = g.current;
      P && (E(P.x !== -1 ? { x: P.x, y: P.y } : null), h(P.w), w(P.h)), _(!1);
    } else {
      const P = L.current, Y = P == null ? void 0 : P.getBoundingClientRect();
      g.current = {
        x: ((A = T.current) == null ? void 0 : A.x) ?? (Y == null ? void 0 : Y.left) ?? -1,
        y: ((j = T.current) == null ? void 0 : j.y) ?? (Y == null ? void 0 : Y.top) ?? -1,
        w: v ?? (Y == null ? void 0 : Y.width) ?? null,
        h: S ?? null
      }, _(!0), E({ x: 0, y: 0 }), h(null), w(null);
    }
  }, [D, v, S]), Q = D ? { position: "absolute", top: 0, left: 0, width: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: v != null ? v + "px" : s,
    ...S != null ? { height: S + "px" } : i != null ? { height: i } : {},
    ...u != null && S == null ? { minHeight: u } : {},
    maxHeight: C ? "100vh" : "80vh",
    ...C ? { position: "absolute", left: C.x + "px", top: C.y + "px" } : {}
  }, H = l + "-title";
  return /* @__PURE__ */ e.createElement(st, { modal: !0 }, /* @__PURE__ */ e.createElement(rl, { onClose: K }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: Q,
      ref: L,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": H
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${D ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: D ? void 0 : N,
        onDoubleClick: a ? O : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: H }, c),
      p && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(z, { control: p })),
      a && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: O,
          title: D ? r["js.window.restore"] : r["js.window.maximize"]
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
          onClick: K,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(z, { control: o })),
    (m.length > 0 || f) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, f && /* @__PURE__ */ e.createElement(z, { control: f }), m.map((A, j) => /* @__PURE__ */ e.createElement(z, { key: j, control: A }))),
    a && !D && ol.map((A) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: A,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${A}`,
        onMouseDown: (j) => B(A, j)
      }
    ))
  ));
}, { useCallback: cl } = e, il = {
  "js.drawer.close": "Close"
}, ul = ({ controlId: l }) => {
  const t = q(), n = le(), r = ce(il), c = t.open === !0, s = t.position ?? "right", i = t.size ?? "medium", u = t.title ?? null, a = t.child, o = cl(() => {
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, a && /* @__PURE__ */ e.createElement(z, { control: a })));
}, { useCallback: dl } = e, ml = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.child, c = dl((s) => {
    s.preventDefault(), s.stopPropagation(), n("openContextMenu", { x: s.clientX, y: s.clientY });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tl-context-menu-region", onContextMenu: c }, r && /* @__PURE__ */ e.createElement(z, { control: r }));
}, { useCallback: pl, useEffect: fl, useState: hl } = e, bl = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.message ?? "", c = t.content ?? "", s = t.variant ?? "info", i = t.duration ?? 5e3, u = t.visible === !0, a = t.generation ?? 0, [o, m] = hl(!1), p = pl(() => {
    m(!0), setTimeout(() => {
      n("dismiss", { generation: a }), m(!1);
    }, 200);
  }, [n, a]);
  return fl(() => {
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
    c ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: c } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, r)
  );
}, { useCallback: qe, useEffect: gt, useRef: _l, useState: vt } = e, gl = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.open === !0, c = t.anchorId, s = t.anchorX, i = t.anchorY, u = t.items ?? [], a = _l(null), [o, m] = vt({ top: 0, left: 0 }), [p, f] = vt(0), v = u.filter((C) => C.type === "item" && !C.disabled);
  gt(() => {
    var b, V;
    if (!r) return;
    const C = ((b = a.current) == null ? void 0 : b.offsetHeight) ?? 200, E = ((V = a.current) == null ? void 0 : V.offsetWidth) ?? 200;
    if (s != null && i != null) {
      let L = i, R = s;
      L + C > window.innerHeight && (L = Math.max(0, window.innerHeight - C)), R + E > window.innerWidth && (R = Math.max(0, window.innerWidth - E)), m({ top: L, left: R }), f(0);
      return;
    }
    if (!c) return;
    const T = document.getElementById(c);
    if (!T) return;
    const D = T.getBoundingClientRect();
    let _ = D.bottom + 4, g = D.left;
    _ + C > window.innerHeight && (_ = D.top - C - 4), g + E > window.innerWidth && (g = D.right - E), m({ top: _, left: g }), f(0);
  }, [r, c, s, i]);
  const h = qe(() => {
    n("close");
  }, [n]), S = qe((C) => {
    n("selectItem", { itemId: C });
  }, [n]);
  gt(() => {
    if (!r) return;
    const C = (E) => {
      a.current && !a.current.contains(E.target) && h();
    };
    return document.addEventListener("mousedown", C), () => document.removeEventListener("mousedown", C);
  }, [r, h]);
  const w = qe((C) => {
    if (C.key === "Escape") {
      C.preventDefault(), h();
      return;
    }
    if (C.key === "ArrowDown")
      C.preventDefault(), f((E) => (E + 1) % v.length);
    else if (C.key === "ArrowUp")
      C.preventDefault(), f((E) => (E - 1 + v.length) % v.length);
    else if (C.key === "Enter" || C.key === " ") {
      C.preventDefault();
      const E = v[p];
      E && S(E.id);
    }
  }, [h, S, v, p]);
  return ct(r, a), r ? /* @__PURE__ */ e.createElement(
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
    u.map((C, E) => {
      if (C.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: E, className: "tlMenu__separator" });
      const D = v.indexOf(C) === p;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: C.id,
          type: "button",
          className: "tlMenu__item" + (D ? " tlMenu__item--focused" : "") + (C.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: C.disabled,
          tabIndex: D ? 0 : -1,
          onClick: () => S(C.id)
        },
        C.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + C.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, C.label)
      );
    })
  ) : null;
}, vl = 768, El = ({ controlId: l }) => {
  const t = q(), n = le();
  e.useEffect(() => {
    const o = window.matchMedia(`(max-width: ${vl}px)`), m = (f) => {
      n("reportDisplayClass", { displayClass: f ? "COMPACT" : "REGULAR" });
    };
    m(o.matches);
    const p = (f) => m(f.matches);
    return o.addEventListener("change", p), () => o.removeEventListener("change", p);
  }, [n]);
  const r = t.header, c = t.content, s = t.footer, i = t.snackbar, u = t.dialogManager, a = t.menuOverlay;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(z, { control: r })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(z, { control: c })), s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(z, { control: s })), /* @__PURE__ */ e.createElement(z, { control: i }), u && /* @__PURE__ */ e.createElement(z, { control: u }), a && /* @__PURE__ */ e.createElement(z, { control: a }));
}, Cl = ({ controlId: l }) => {
  const t = q(), n = t.text ?? "", r = t.cssClass ?? "", c = t.hasTooltip === !0, s = r ? `tlText ${r}` : "tlText";
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: s,
      "data-tooltip": c ? "key:tooltip" : void 0
    },
    n
  );
}, wl = ({ isMulti: l, cursorIndex: t, onMove: n, onToggle: r, onSelectAll: c }) => (ue("ArrowUp", () => (n("up", !1, !1), !0)), ue("ArrowDown", () => (n("down", !1, !1), !0)), ue("Home", () => (n("home", !1, !1), !0)), ue("End", () => (n("end", !1, !1), !0)), ue("PageUp", () => (n("pageUp", !1, !1), !0)), ue("PageDown", () => (n("pageDown", !1, !1), !0)), ue("Shift+ArrowUp", () => (n("up", l, !1), !0)), ue("Shift+ArrowDown", () => (n("down", l, !1), !0)), ue("Shift+Home", () => (n("home", l, !1), !0)), ue("Shift+End", () => (n("end", l, !1), !0)), ue("Shift+PageUp", () => (n("pageUp", l, !1), !0)), ue("Shift+PageDown", () => (n("pageDown", l, !1), !0)), ue("Ctrl+ArrowUp", () => (n("up", !1, l), !0)), ue("Ctrl+ArrowDown", () => (n("down", !1, l), !0)), ue("Space", () => t < 0 ? !1 : (r(), !0)), ue("Ctrl+A", () => l ? (c(), !0) : !1), null), yl = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all",
  "js.table.filter": "Filter"
}, Et = 50, Sl = ({ controlId: l }) => {
  const t = q(), n = le(), r = ce(yl), c = e.useRef(null);
  e.useEffect(() => {
    const y = c.current;
    if (!y) return;
    const x = (G) => {
      const ee = G.detail;
      let ae = ee.target;
      for (; ae && ae !== y; ) {
        const fe = ae.dataset.row, se = ae.dataset.col;
        if (fe != null && se != null) {
          ee.resolved = { key: fe + "|" + se };
          return;
        }
        ae = ae.parentElement;
      }
    };
    return y.addEventListener("tl-tooltip-resolve", x), () => y.removeEventListener("tl-tooltip-resolve", x);
  }, []);
  const s = t.columns ?? [], i = t.totalRowCount ?? 0, u = t.rows ?? [], a = t.rowHeight ?? 36, o = t.selectionMode ?? "single", m = t.selectedCount ?? 0, p = t.cursorIndex ?? -1, f = t.frozenColumnCount ?? 0, v = t.treeMode ?? !1, h = e.useMemo(
    () => s.filter((y) => y.sortPriority && y.sortPriority > 0).length,
    [s]
  ), S = o === "multi", w = 40, C = 20, E = e.useRef(null), T = e.useRef(null), D = e.useRef(null), [_, g] = e.useState({}), b = e.useRef(null), V = e.useRef(!1), L = e.useRef(null), [R, K] = e.useState(null), [B, N] = e.useState(null);
  e.useEffect(() => {
    b.current || g({});
  }, [s]);
  const O = e.useCallback((y) => _[y.name] ?? y.width, [_]), Q = e.useMemo(() => {
    const y = [];
    let x = S && f > 0 ? w : 0;
    for (let G = 0; G < f && G < s.length; G++)
      y.push(x), x += O(s[G]);
    return y;
  }, [s, f, S, w, O]), H = i * a, A = e.useRef(null), j = e.useCallback((y, x, G) => {
    G.preventDefault(), G.stopPropagation(), b.current = { column: y, startX: G.clientX, startWidth: x };
    let ee = G.clientX, ae = 0;
    const fe = () => {
      const ie = b.current;
      if (!ie) return;
      const ge = Math.max(Et, ie.startWidth + (ee - ie.startX) + ae);
      g((De) => ({ ...De, [ie.column]: ge }));
    }, se = () => {
      const ie = T.current, ge = E.current;
      if (!ie || !b.current) return;
      const De = ie.getBoundingClientRect(), ut = 40, dt = 8, Ot = ie.scrollLeft;
      ee > De.right - ut ? ie.scrollLeft += dt : ee < De.left + ut && (ie.scrollLeft = Math.max(0, ie.scrollLeft - dt));
      const mt = ie.scrollLeft - Ot;
      mt !== 0 && (ge && (ge.scrollLeft = ie.scrollLeft), ae += mt, fe()), A.current = requestAnimationFrame(se);
    };
    A.current = requestAnimationFrame(se);
    const Re = (ie) => {
      ee = ie.clientX, fe();
    }, Fe = (ie) => {
      document.removeEventListener("mousemove", Re), document.removeEventListener("mouseup", Fe), A.current !== null && (cancelAnimationFrame(A.current), A.current = null);
      const ge = b.current;
      if (ge) {
        const De = Math.max(Et, ge.startWidth + (ie.clientX - ge.startX) + ae);
        n("columnResize", { column: ge.column, width: De }), b.current = null, V.current = !0, requestAnimationFrame(() => {
          V.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", Re), document.addEventListener("mouseup", Fe);
  }, [n]), P = e.useCallback(() => {
    E.current && T.current && (E.current.scrollLeft = T.current.scrollLeft), D.current !== null && clearTimeout(D.current), D.current = window.setTimeout(() => {
      const y = T.current;
      if (!y) return;
      const x = y.scrollTop, G = Math.ceil(y.clientHeight / a), ee = Math.floor(x / a);
      n("scroll", { start: ee, count: G });
    }, 80);
  }, [n, a]), Y = e.useCallback((y, x, G) => {
    if (V.current) return;
    let ee;
    !x || x === "desc" ? ee = "asc" : ee = "desc";
    const ae = G.shiftKey ? "add" : "replace";
    n("sort", { column: y, direction: ee, mode: ae });
  }, [n]), d = e.useCallback((y, x) => {
    L.current = y, x.dataTransfer.effectAllowed = "move", x.dataTransfer.setData("text/plain", y);
  }, []), k = e.useCallback((y, x) => {
    if (!L.current || L.current === y) {
      K(null);
      return;
    }
    x.preventDefault(), x.dataTransfer.dropEffect = "move";
    const G = x.currentTarget.getBoundingClientRect(), ee = x.clientX < G.left + G.width / 2 ? "left" : "right";
    K({ column: y, side: ee });
  }, []), F = e.useCallback((y) => {
    y.preventDefault(), y.stopPropagation();
    const x = L.current;
    if (!x || !R) {
      L.current = null, K(null);
      return;
    }
    let G = s.findIndex((ae) => ae.name === R.column);
    if (G < 0) {
      L.current = null, K(null);
      return;
    }
    const ee = s.findIndex((ae) => ae.name === x);
    R.side === "right" && G++, ee < G && G--, n("columnReorder", { column: x, targetIndex: G }), L.current = null, K(null);
  }, [s, R, n]), $ = e.useCallback(() => {
    L.current = null, K(null);
  }, []), X = e.useCallback((y, x) => {
    var ee;
    const G = window.getSelection();
    G && !G.isCollapsed && x.currentTarget.contains(G.anchorNode) || ((ee = T.current) == null || ee.focus({ preventScroll: !0 }), n("select", {
      rowIndex: y,
      ctrlKey: x.ctrlKey || x.metaKey,
      shiftKey: x.shiftKey
    }));
  }, [n]), M = e.useCallback((y, x, G) => {
    n("moveSelection", { direction: y, extend: x, move: G });
  }, [n]), J = e.useCallback(() => {
    p < 0 || n("select", { rowIndex: p, ctrlKey: S, shiftKey: !1 });
  }, [n, p, S]), re = e.useCallback(() => {
    n("selectAll", { selected: !0 });
  }, [n]), ne = e.useCallback(
    () => !!c.current && c.current.contains(document.activeElement),
    []
  );
  e.useEffect(() => {
    if (p < 0)
      return;
    const y = T.current;
    if (!y)
      return;
    const x = p * a, G = x + a;
    x < y.scrollTop ? y.scrollTop = x : G > y.scrollTop + y.clientHeight && (y.scrollTop = G - y.clientHeight);
  }, [p, a]);
  const pe = e.useCallback((y, x) => {
    x.stopPropagation(), n("select", { rowIndex: y, ctrlKey: !0, shiftKey: !1 });
  }, [n]), be = e.useCallback(() => {
    const y = m === i && i > 0;
    n("selectAll", { selected: !y });
  }, [n, m, i]), _e = e.useCallback((y, x, G) => {
    G.stopPropagation(), n("expand", { rowIndex: y, expanded: x });
  }, [n]), Ce = e.useCallback((y, x) => {
    x.preventDefault(), N({ x: x.clientX, y: x.clientY, colIdx: y });
  }, []), we = e.useCallback(() => {
    B && (n("setFrozenColumnCount", { count: B.colIdx + 1 }), N(null));
  }, [B, n]), Te = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), N(null);
  }, [n]);
  e.useEffect(() => {
    if (!B) return;
    const y = () => N(null);
    return document.addEventListener("mousedown", y), () => document.removeEventListener("mousedown", y);
  }, [B]), Ne(!!B, { ESCAPE: () => N(null) });
  const $e = e.useCallback((y, x) => {
    x.stopPropagation(), x.preventDefault(), n("openFilter", { column: y });
  }, [n]), I = s.reduce((y, x) => y + O(x), 0) + (S ? w : 0), W = m === i && i > 0, te = m > 0 && m < i, oe = e.useCallback((y) => {
    y && (y.indeterminate = te);
  }, [te]);
  return /* @__PURE__ */ e.createElement(st, { active: ne }, /* @__PURE__ */ e.createElement(
    wl,
    {
      isMulti: S,
      cursorIndex: p,
      onMove: M,
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
      onDragOver: (y) => {
        if (!L.current) return;
        y.preventDefault();
        const x = T.current, G = E.current;
        if (!x) return;
        const ee = x.getBoundingClientRect(), ae = 40, fe = 8;
        y.clientX < ee.left + ae ? x.scrollLeft = Math.max(0, x.scrollLeft - fe) : y.clientX > ee.right - ae && (x.scrollLeft += fe), G && (G.scrollLeft = x.scrollLeft);
      },
      onDrop: F
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: E }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { width: I } }, S && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (f > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: w,
          minWidth: w,
          ...f > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (y) => {
          L.current && (y.preventDefault(), y.dataTransfer.dropEffect = "move", s.length > 0 && s[0].name !== L.current && K({ column: s[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: oe,
          className: "tlTableView__checkbox",
          checked: W,
          onChange: be
        }
      )
    ), s.map((y, x) => {
      const G = O(y);
      s.length - 1;
      let ee = "tlTableView__headerCell";
      y.sortable && (ee += " tlTableView__headerCell--sortable"), R && R.column === y.name && (ee += " tlTableView__headerCell--dragOver-" + R.side);
      const ae = x < f, fe = x === f - 1;
      return ae && (ee += " tlTableView__headerCell--frozen"), fe && (ee += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: y.name,
          className: ee,
          style: {
            width: G,
            minWidth: G,
            position: ae ? "sticky" : "relative",
            ...ae ? { left: Q[x], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: y.sortable ? (se) => Y(y.name, y.sortDirection, se) : void 0,
          onContextMenu: (se) => Ce(x, se),
          onDragStart: (se) => d(y.name, se),
          onDragOver: (se) => k(y.name, se),
          onDrop: F,
          onDragEnd: $
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
            onMouseDown: (se) => se.stopPropagation(),
            onClick: (se) => $e(y.name, se)
          },
          /* @__PURE__ */ e.createElement("i", { className: y.filterActive ? "bi bi-funnel-fill" : "bi bi-funnel" })
        ),
        y.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, y.sortDirection === "asc" ? "▲" : "▼", h > 1 && y.sortPriority != null && y.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, y.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (se) => j(y.name, G, se)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (y) => {
          if (L.current && s.length > 0) {
            const x = s[s.length - 1];
            x.name !== L.current && (y.preventDefault(), y.dataTransfer.dropEffect = "move", K({ column: x.name, side: "right" }));
          }
        },
        onDrop: F
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: T,
        className: "tlTableView__body",
        onScroll: P,
        tabIndex: 0
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: H, position: "relative", width: I } }, u.map((y) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: y.id,
          className: "tlTableView__row" + (y.selected ? " tlTableView__row--selected" : "") + (y.index === p ? " tlTableView__row--cursor" : ""),
          style: {
            position: "absolute",
            top: y.index * a,
            height: a,
            width: I,
            ...y.index === p ? { outline: "2px solid var(--color-primary, #1a73e8)", outlineOffset: "-2px" } : {}
          },
          onMouseDown: (x) => {
            (x.shiftKey || x.ctrlKey || x.metaKey || x.detail > 1) && x.preventDefault();
          },
          onClick: (x) => X(y.index, x)
        },
        S && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (f > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: w,
              minWidth: w,
              ...f > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (x) => x.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: y.selected,
              onChange: () => {
              },
              onClick: (x) => pe(y.index, x),
              tabIndex: -1
            }
          )
        ),
        s.map((x, G) => {
          const ee = O(x), ae = G === s.length - 1, fe = G < f, se = G === f - 1;
          let Re = "tlTableView__cell";
          fe && (Re += " tlTableView__cell--frozen"), se && (Re += " tlTableView__cell--frozenLast");
          const Fe = v && G === 0, ie = y.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: x.name,
              className: Re,
              "data-row": y.id,
              "data-col": x.name,
              style: {
                ...ae && !fe ? { flex: "1 0 auto", minWidth: ee } : { width: ee, minWidth: ee },
                ...fe ? { position: "sticky", left: Q[G], zIndex: 2 } : {}
              }
            },
            Fe ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ie * C } }, y.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (ge) => _e(y.index, !y.expanded, ge)
              },
              y.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(z, { control: y.cells[x.name] })) : /* @__PURE__ */ e.createElement(z, { control: y.cells[x.name] })
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
      B.colIdx + 1 !== f && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: we }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.freezeUpTo"])),
      f > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Te }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.unfreezeAll"]))
    )
  ));
}, kl = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, It = e.createContext(kl), { useMemo: Nl, useRef: Tl, useState: Rl, useEffect: Dl } = e, xl = 320, Ll = ({ controlId: l }) => {
  const t = q(), n = t.maxColumns ?? 3, r = t.labelPosition ?? "auto", c = t.readOnly === !0, s = t.children ?? [], i = t.noModelMessage, u = Tl(null), [a, o] = Rl(
    r === "top" ? "top" : "side"
  );
  Dl(() => {
    if (r !== "auto") {
      o(r);
      return;
    }
    const h = u.current;
    if (!h) return;
    const S = new ResizeObserver((w) => {
      for (const C of w) {
        const T = C.contentRect.width / n;
        o(T < xl ? "top" : "side");
      }
    });
    return S.observe(h), () => S.disconnect();
  }, [r, n]);
  const m = Nl(() => ({
    readOnly: c,
    resolvedLabelPosition: a
  }), [c, a]), f = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, v = [
    "tlFormLayout",
    c ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return i ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, i)) : /* @__PURE__ */ e.createElement(It.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: l, className: v, style: f, ref: u }, s.map((h, S) => /* @__PURE__ */ e.createElement(z, { key: S, control: h }))));
}, { useCallback: Il } = e, Pl = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, Ml = ({ controlId: l }) => {
  const t = q(), n = le(), r = ce(Pl), c = t.headerControl ?? null, s = t.headerActions ?? [], i = t.collapsible === !0, u = t.collapsed === !0, a = t.border ?? "none", o = t.fullLine === !0, m = t.children ?? [], p = c != null || s.length > 0 || i, f = Il(() => {
    n("toggleCollapse");
  }, [n]), v = [
    "tlFormGroup",
    `tlFormGroup--border-${a}`,
    o ? "tlFormGroup--fullLine" : "",
    u ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: v }, p && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, i && /* @__PURE__ */ e.createElement(
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
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(z, { control: c })), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, s.map((h, S) => /* @__PURE__ */ e.createElement(z, { key: S, control: h })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((h, S) => /* @__PURE__ */ e.createElement(z, { key: S, control: h }))));
}, { useContext: jl, useState: Bl, useCallback: Al } = e, Ol = ({ controlId: l }) => {
  const t = q(), n = jl(It), r = t.label ?? "", c = t.required === !0, s = t.error, i = t.warnings, u = t.helpText, a = t.dirty === !0, o = t.labelPosition ?? n.resolvedLabelPosition, m = t.fullLine === !0, p = t.visible !== !1, f = t.hasTooltip === !0, v = t.field, h = n.readOnly, [S, w] = Bl(!1), C = Al(() => w((_) => !_), []);
  if (!p) return null;
  const E = s != null, T = i != null && i.length > 0, D = [
    "tlFormField",
    `tlFormField--${o}`,
    h ? "tlFormField--readonly" : "",
    m ? "tlFormField--fullLine" : "",
    E ? "tlFormField--error" : "",
    !E && T ? "tlFormField--warning" : "",
    a ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: D }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": f ? "key:tooltip" : void 0
    },
    r
  ), c && !h && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), a && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), u && !h && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormField__helpIcon",
      onClick: C,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(z, { control: v })), !h && E && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, s)), !h && !E && T && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, i.map((_, g) => /* @__PURE__ */ e.createElement("div", { key: g, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, _)))), !h && u && S && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, u));
}, $l = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.iconCss, c = t.iconSrc, s = t.label, i = t.cssClass, u = t.hasTooltip === !0, a = t.hasLink, o = r ? /* @__PURE__ */ e.createElement("i", { className: r }) : c ? /* @__PURE__ */ e.createElement("img", { src: c, className: "tlTypeIcon", alt: "" }) : null, m = /* @__PURE__ */ e.createElement(e.Fragment, null, o, s && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, s)), p = e.useCallback((h) => {
    h.preventDefault(), n("goto", {});
  }, [n]), f = ["tlResourceCell", i].filter(Boolean).join(" "), v = u ? "key:tooltip" : void 0;
  return a ? /* @__PURE__ */ e.createElement(
    "a",
    {
      id: l,
      className: f,
      href: "#",
      onClick: p,
      "data-tooltip": v
    },
    m
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: f, "data-tooltip": v }, m);
}, Fl = 20, Ul = () => {
  const l = q(), t = le(), n = l.nodes ?? [], r = l.selectionMode ?? "single", c = l.dragEnabled ?? !1, s = l.dropEnabled ?? !1, i = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [a, o] = e.useState(-1), m = e.useRef(null), p = e.useCallback((_, g) => {
    t(g ? "collapse" : "expand", { nodeId: _ });
  }, [t]), f = e.useCallback((_, g) => {
    var V;
    const b = window.getSelection();
    b && !b.isCollapsed && g.currentTarget.contains(b.anchorNode) || ((V = m.current) == null || V.focus({ preventScroll: !0 }), t("select", {
      nodeId: _,
      ctrlKey: g.ctrlKey || g.metaKey,
      shiftKey: g.shiftKey
    }));
  }, [t]), v = e.useCallback((_, g) => {
    g.preventDefault(), t("contextMenu", { nodeId: _, x: g.clientX, y: g.clientY });
  }, [t]), h = e.useRef(null), S = e.useCallback((_, g) => {
    const b = g.getBoundingClientRect(), V = _.clientY - b.top, L = b.height / 3;
    return V < L ? "above" : V > L * 2 ? "below" : "within";
  }, []), w = e.useCallback((_, g) => {
    g.dataTransfer.effectAllowed = "move", g.dataTransfer.setData("text/plain", _);
  }, []), C = e.useCallback((_, g) => {
    g.preventDefault(), g.dataTransfer.dropEffect = "move";
    const b = S(g, g.currentTarget);
    h.current != null && window.clearTimeout(h.current), h.current = window.setTimeout(() => {
      t("dragOver", { nodeId: _, position: b }), h.current = null;
    }, 50);
  }, [t, S]), E = e.useCallback((_, g) => {
    g.preventDefault(), h.current != null && (window.clearTimeout(h.current), h.current = null);
    const b = S(g, g.currentTarget);
    t("drop", { nodeId: _, position: b });
  }, [t, S]), T = e.useCallback(() => {
    h.current != null && (window.clearTimeout(h.current), h.current = null), t("dragEnd");
  }, [t]), D = e.useCallback((_) => {
    if (n.length === 0) return;
    let g = a;
    switch (_.key) {
      case "ArrowDown":
        _.preventDefault(), g = Math.min(a + 1, n.length - 1);
        break;
      case "ArrowUp":
        _.preventDefault(), g = Math.max(a - 1, 0);
        break;
      case "ArrowRight":
        if (_.preventDefault(), a >= 0 && a < n.length) {
          const b = n[a];
          if (b.expandable && !b.expanded) {
            t("expand", { nodeId: b.id });
            return;
          } else b.expanded && (g = a + 1);
        }
        break;
      case "ArrowLeft":
        if (_.preventDefault(), a >= 0 && a < n.length) {
          const b = n[a];
          if (b.expanded) {
            t("collapse", { nodeId: b.id });
            return;
          } else {
            const V = b.depth;
            for (let L = a - 1; L >= 0; L--)
              if (n[L].depth < V) {
                g = L;
                break;
              }
          }
        }
        break;
      case "Enter":
        _.preventDefault(), a >= 0 && a < n.length && t("select", {
          nodeId: n[a].id,
          ctrlKey: _.ctrlKey || _.metaKey,
          shiftKey: _.shiftKey
        });
        return;
      case " ":
        _.preventDefault(), r === "multi" && a >= 0 && a < n.length && t("select", {
          nodeId: n[a].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        _.preventDefault(), g = 0;
        break;
      case "End":
        _.preventDefault(), g = n.length - 1;
        break;
      default:
        return;
    }
    g !== a && o(g);
  }, [a, n, t, r]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: m,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: D
    },
    n.map((_, g) => /* @__PURE__ */ e.createElement(
      "li",
      {
        key: _.id,
        role: "treeitem",
        "aria-expanded": _.expandable ? _.expanded : void 0,
        "aria-selected": _.selected,
        "aria-level": _.depth + 1,
        className: [
          "tlTreeView__node",
          _.selected ? "tlTreeView__node--selected" : "",
          g === a ? "tlTreeView__node--focused" : "",
          i === _.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          i === _.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          i === _.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: _.depth * Fl },
        draggable: c,
        onMouseDown: (b) => {
          (b.shiftKey || b.ctrlKey || b.metaKey || b.detail > 1) && b.preventDefault();
        },
        onClick: (b) => f(_.id, b),
        onContextMenu: (b) => v(_.id, b),
        onDragStart: (b) => w(_.id, b),
        onDragOver: s ? (b) => C(_.id, b) : void 0,
        onDrop: s ? (b) => E(_.id, b) : void 0,
        onDragEnd: T
      },
      _.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (b) => {
            b.stopPropagation(), p(_.id, _.expanded);
          },
          tabIndex: -1,
          "aria-label": _.expanded ? "Collapse" : "Expand"
        },
        _.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: _.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(z, { control: _.content }))
    ))
  );
};
var Ze = { exports: {} }, de = {}, Qe = { exports: {} }, Z = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var Ct;
function Hl() {
  if (Ct) return Z;
  Ct = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), r = Symbol.for("react.strict_mode"), c = Symbol.for("react.profiler"), s = Symbol.for("react.consumer"), i = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), a = Symbol.for("react.suspense"), o = Symbol.for("react.memo"), m = Symbol.for("react.lazy"), p = Symbol.for("react.activity"), f = Symbol.iterator;
  function v(d) {
    return d === null || typeof d != "object" ? null : (d = f && d[f] || d["@@iterator"], typeof d == "function" ? d : null);
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
  }, S = Object.assign, w = {};
  function C(d, k, F) {
    this.props = d, this.context = k, this.refs = w, this.updater = F || h;
  }
  C.prototype.isReactComponent = {}, C.prototype.setState = function(d, k) {
    if (typeof d != "object" && typeof d != "function" && d != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, d, k, "setState");
  }, C.prototype.forceUpdate = function(d) {
    this.updater.enqueueForceUpdate(this, d, "forceUpdate");
  };
  function E() {
  }
  E.prototype = C.prototype;
  function T(d, k, F) {
    this.props = d, this.context = k, this.refs = w, this.updater = F || h;
  }
  var D = T.prototype = new E();
  D.constructor = T, S(D, C.prototype), D.isPureReactComponent = !0;
  var _ = Array.isArray;
  function g() {
  }
  var b = { H: null, A: null, T: null, S: null }, V = Object.prototype.hasOwnProperty;
  function L(d, k, F) {
    var $ = F.ref;
    return {
      $$typeof: l,
      type: d,
      key: k,
      ref: $ !== void 0 ? $ : null,
      props: F
    };
  }
  function R(d, k) {
    return L(d.type, k, d.props);
  }
  function K(d) {
    return typeof d == "object" && d !== null && d.$$typeof === l;
  }
  function B(d) {
    var k = { "=": "=0", ":": "=2" };
    return "$" + d.replace(/[=:]/g, function(F) {
      return k[F];
    });
  }
  var N = /\/+/g;
  function O(d, k) {
    return typeof d == "object" && d !== null && d.key != null ? B("" + d.key) : k.toString(36);
  }
  function Q(d) {
    switch (d.status) {
      case "fulfilled":
        return d.value;
      case "rejected":
        throw d.reason;
      default:
        switch (typeof d.status == "string" ? d.then(g, g) : (d.status = "pending", d.then(
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
  function H(d, k, F, $, X) {
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
              return J = d._init, H(
                J(d._payload),
                k,
                F,
                $,
                X
              );
          }
      }
    if (J)
      return X = X(d), J = $ === "" ? "." + O(d, 0) : $, _(X) ? (F = "", J != null && (F = J.replace(N, "$&/") + "/"), H(X, k, F, "", function(pe) {
        return pe;
      })) : X != null && (K(X) && (X = R(
        X,
        F + (X.key == null || d && d.key === X.key ? "" : ("" + X.key).replace(
          N,
          "$&/"
        ) + "/") + J
      )), k.push(X)), 1;
    J = 0;
    var re = $ === "" ? "." : $ + ":";
    if (_(d))
      for (var ne = 0; ne < d.length; ne++)
        $ = d[ne], M = re + O($, ne), J += H(
          $,
          k,
          F,
          M,
          X
        );
    else if (ne = v(d), typeof ne == "function")
      for (d = ne.call(d), ne = 0; !($ = d.next()).done; )
        $ = $.value, M = re + O($, ne++), J += H(
          $,
          k,
          F,
          M,
          X
        );
    else if (M === "object") {
      if (typeof d.then == "function")
        return H(
          Q(d),
          k,
          F,
          $,
          X
        );
      throw k = String(d), Error(
        "Objects are not valid as a React child (found: " + (k === "[object Object]" ? "object with keys {" + Object.keys(d).join(", ") + "}" : k) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return J;
  }
  function A(d, k, F) {
    if (d == null) return d;
    var $ = [], X = 0;
    return H(d, $, "", "", function(M) {
      return k.call(F, M, X++);
    }), $;
  }
  function j(d) {
    if (d._status === -1) {
      var k = d._result;
      k = k(), k.then(
        function(F) {
          (d._status === 0 || d._status === -1) && (d._status = 1, d._result = F);
        },
        function(F) {
          (d._status === 0 || d._status === -1) && (d._status = 2, d._result = F);
        }
      ), d._status === -1 && (d._status = 0, d._result = k);
    }
    if (d._status === 1) return d._result.default;
    throw d._result;
  }
  var P = typeof reportError == "function" ? reportError : function(d) {
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
    map: A,
    forEach: function(d, k, F) {
      A(
        d,
        function() {
          k.apply(this, arguments);
        },
        F
      );
    },
    count: function(d) {
      var k = 0;
      return A(d, function() {
        k++;
      }), k;
    },
    toArray: function(d) {
      return A(d, function(k) {
        return k;
      }) || [];
    },
    only: function(d) {
      if (!K(d))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return d;
    }
  };
  return Z.Activity = p, Z.Children = Y, Z.Component = C, Z.Fragment = n, Z.Profiler = c, Z.PureComponent = T, Z.StrictMode = r, Z.Suspense = a, Z.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = b, Z.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(d) {
      return b.H.useMemoCache(d);
    }
  }, Z.cache = function(d) {
    return function() {
      return d.apply(null, arguments);
    };
  }, Z.cacheSignal = function() {
    return null;
  }, Z.cloneElement = function(d, k, F) {
    if (d == null)
      throw Error(
        "The argument must be a React element, but you passed " + d + "."
      );
    var $ = S({}, d.props), X = d.key;
    if (k != null)
      for (M in k.key !== void 0 && (X = "" + k.key), k)
        !V.call(k, M) || M === "key" || M === "__self" || M === "__source" || M === "ref" && k.ref === void 0 || ($[M] = k[M]);
    var M = arguments.length - 2;
    if (M === 1) $.children = F;
    else if (1 < M) {
      for (var J = Array(M), re = 0; re < M; re++)
        J[re] = arguments[re + 2];
      $.children = J;
    }
    return L(d.type, X, $);
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
  }, Z.createElement = function(d, k, F) {
    var $, X = {}, M = null;
    if (k != null)
      for ($ in k.key !== void 0 && (M = "" + k.key), k)
        V.call(k, $) && $ !== "key" && $ !== "__self" && $ !== "__source" && (X[$] = k[$]);
    var J = arguments.length - 2;
    if (J === 1) X.children = F;
    else if (1 < J) {
      for (var re = Array(J), ne = 0; ne < J; ne++)
        re[ne] = arguments[ne + 2];
      X.children = re;
    }
    if (d && d.defaultProps)
      for ($ in J = d.defaultProps, J)
        X[$] === void 0 && (X[$] = J[$]);
    return L(d, M, X);
  }, Z.createRef = function() {
    return { current: null };
  }, Z.forwardRef = function(d) {
    return { $$typeof: u, render: d };
  }, Z.isValidElement = K, Z.lazy = function(d) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: d },
      _init: j
    };
  }, Z.memo = function(d, k) {
    return {
      $$typeof: o,
      type: d,
      compare: k === void 0 ? null : k
    };
  }, Z.startTransition = function(d) {
    var k = b.T, F = {};
    b.T = F;
    try {
      var $ = d(), X = b.S;
      X !== null && X(F, $), typeof $ == "object" && $ !== null && typeof $.then == "function" && $.then(g, P);
    } catch (M) {
      P(M);
    } finally {
      k !== null && F.types !== null && (k.types = F.types), b.T = k;
    }
  }, Z.unstable_useCacheRefresh = function() {
    return b.H.useCacheRefresh();
  }, Z.use = function(d) {
    return b.H.use(d);
  }, Z.useActionState = function(d, k, F) {
    return b.H.useActionState(d, k, F);
  }, Z.useCallback = function(d, k) {
    return b.H.useCallback(d, k);
  }, Z.useContext = function(d) {
    return b.H.useContext(d);
  }, Z.useDebugValue = function() {
  }, Z.useDeferredValue = function(d, k) {
    return b.H.useDeferredValue(d, k);
  }, Z.useEffect = function(d, k) {
    return b.H.useEffect(d, k);
  }, Z.useEffectEvent = function(d) {
    return b.H.useEffectEvent(d);
  }, Z.useId = function() {
    return b.H.useId();
  }, Z.useImperativeHandle = function(d, k, F) {
    return b.H.useImperativeHandle(d, k, F);
  }, Z.useInsertionEffect = function(d, k) {
    return b.H.useInsertionEffect(d, k);
  }, Z.useLayoutEffect = function(d, k) {
    return b.H.useLayoutEffect(d, k);
  }, Z.useMemo = function(d, k) {
    return b.H.useMemo(d, k);
  }, Z.useOptimistic = function(d, k) {
    return b.H.useOptimistic(d, k);
  }, Z.useReducer = function(d, k, F) {
    return b.H.useReducer(d, k, F);
  }, Z.useRef = function(d) {
    return b.H.useRef(d);
  }, Z.useState = function(d) {
    return b.H.useState(d);
  }, Z.useSyncExternalStore = function(d, k, F) {
    return b.H.useSyncExternalStore(
      d,
      k,
      F
    );
  }, Z.useTransition = function() {
    return b.H.useTransition();
  }, Z.version = "19.2.4", Z;
}
var wt;
function Wl() {
  return wt || (wt = 1, Qe.exports = Hl()), Qe.exports;
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
var yt;
function zl() {
  if (yt) return de;
  yt = 1;
  var l = Wl();
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
  return de.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = r, de.createPortal = function(a, o) {
    var m = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!o || o.nodeType !== 1 && o.nodeType !== 9 && o.nodeType !== 11)
      throw Error(t(299));
    return s(a, o, null, m);
  }, de.flushSync = function(a) {
    var o = i.T, m = r.p;
    try {
      if (i.T = null, r.p = 2, a) return a();
    } finally {
      i.T = o, r.p = m, r.d.f();
    }
  }, de.preconnect = function(a, o) {
    typeof a == "string" && (o ? (o = o.crossOrigin, o = typeof o == "string" ? o === "use-credentials" ? o : "" : void 0) : o = null, r.d.C(a, o));
  }, de.prefetchDNS = function(a) {
    typeof a == "string" && r.d.D(a);
  }, de.preinit = function(a, o) {
    if (typeof a == "string" && o && typeof o.as == "string") {
      var m = o.as, p = u(m, o.crossOrigin), f = typeof o.integrity == "string" ? o.integrity : void 0, v = typeof o.fetchPriority == "string" ? o.fetchPriority : void 0;
      m === "style" ? r.d.S(
        a,
        typeof o.precedence == "string" ? o.precedence : void 0,
        {
          crossOrigin: p,
          integrity: f,
          fetchPriority: v
        }
      ) : m === "script" && r.d.X(a, {
        crossOrigin: p,
        integrity: f,
        fetchPriority: v,
        nonce: typeof o.nonce == "string" ? o.nonce : void 0
      });
    }
  }, de.preinitModule = function(a, o) {
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
  }, de.preload = function(a, o) {
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
  }, de.preloadModule = function(a, o) {
    if (typeof a == "string")
      if (o) {
        var m = u(o.as, o.crossOrigin);
        r.d.m(a, {
          as: typeof o.as == "string" && o.as !== "script" ? o.as : void 0,
          crossOrigin: m,
          integrity: typeof o.integrity == "string" ? o.integrity : void 0
        });
      } else r.d.m(a);
  }, de.requestFormReset = function(a) {
    r.d.r(a);
  }, de.unstable_batchedUpdates = function(a, o) {
    return a(o);
  }, de.useFormState = function(a, o, m) {
    return i.H.useFormState(a, o, m);
  }, de.useFormStatus = function() {
    return i.H.useHostTransitionStatus();
  }, de.version = "19.2.4", de;
}
var St;
function Vl() {
  if (St) return Ze.exports;
  St = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), Ze.exports = zl(), Ze.exports;
}
var Pt = Vl();
const { useState: ye, useCallback: me, useRef: je, useEffect: Le, useMemo: at } = e;
function it({ image: l }) {
  if (!l) return null;
  if (l.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function Kl({
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
      onDragEnd: a
    },
    c && /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__dragHandle", "aria-hidden": "true" }, "⋮⋮"),
    /* @__PURE__ */ e.createElement(it, { image: l.image }),
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
function Yl({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: r,
  onMouseEnter: c,
  id: s
}) {
  const i = me(() => r(l.value), [r, l.value]), u = at(() => {
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
    /* @__PURE__ */ e.createElement(it, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const Gl = ({ controlId: l, state: t }) => {
  const n = le(), r = t.value ?? [], c = t.multiSelect === !0, s = t.customOrder === !0, i = t.mandatory === !0, u = t.disabled === !0, a = t.editable !== !1, o = t.optionsLoaded === !0, m = t.options ?? [], p = t.emptyOptionLabel ?? "", f = s && c && !u && a, v = ce({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), h = v["js.dropdownSelect.nothingFound"], S = me(
    (I) => v["js.dropdownSelect.removeChip"].replace("{0}", I),
    [v]
  ), [w, C] = ye(!1), [E, T] = ye(""), [D, _] = ye(-1), [g, b] = ye(!1), [V, L] = ye({}), [R, K] = ye(null), [B, N] = ye(null), [O, Q] = ye(null), H = je(null), A = je(null), j = je(null), P = je(r);
  P.current = r;
  const Y = je(-1), d = at(
    () => new Set(r.map((I) => I.value)),
    [r]
  ), k = at(() => {
    let I = m.filter((W) => !d.has(W.value));
    if (E) {
      const W = E.toLowerCase();
      I = I.filter((te) => te.label.toLowerCase().includes(W));
    }
    return I;
  }, [m, d, E]);
  Le(() => {
    E && k.length === 1 ? _(0) : _(-1);
  }, [k.length, E]), Le(() => {
    w && o && A.current && A.current.focus();
  }, [w, o, r]), Le(() => {
    var te, oe;
    if (Y.current < 0) return;
    const I = Y.current;
    Y.current = -1;
    const W = (te = H.current) == null ? void 0 : te.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    W && W.length > 0 ? W[Math.min(I, W.length - 1)].focus() : (oe = H.current) == null || oe.focus();
  }, [r]), Le(() => {
    if (!w) return;
    const I = (W) => {
      H.current && !H.current.contains(W.target) && j.current && !j.current.contains(W.target) && (C(!1), T(""));
    };
    return document.addEventListener("mousedown", I), () => document.removeEventListener("mousedown", I);
  }, [w]), Le(() => {
    if (!w || !H.current) return;
    const I = H.current.getBoundingClientRect(), W = window.innerHeight - I.bottom, oe = W < 300 && I.top > W;
    L({
      left: I.left,
      width: I.width,
      ...oe ? { bottom: window.innerHeight - I.top } : { top: I.bottom }
    });
  }, [w]);
  const F = me(async () => {
    if (!(u || !a) && (C(!0), T(""), _(-1), b(!1), !o))
      try {
        await n("loadOptions");
      } catch {
        b(!0);
      }
  }, [u, a, o, n]), $ = me(() => {
    var I;
    C(!1), T(""), _(-1), (I = H.current) == null || I.focus();
  }, []), X = me(
    (I) => {
      let W;
      if (c) {
        const te = m.find((oe) => oe.value === I);
        if (te)
          W = [...P.current, te];
        else
          return;
      } else {
        const te = m.find((oe) => oe.value === I);
        if (te)
          W = [te];
        else
          return;
      }
      P.current = W, n("valueChanged", { value: W.map((te) => te.value) }), c ? (T(""), _(-1)) : $();
    },
    [c, m, n, $]
  ), M = me(
    (I) => {
      Y.current = P.current.findIndex((te) => te.value === I);
      const W = P.current.filter((te) => te.value !== I);
      P.current = W, n("valueChanged", { value: W.map((te) => te.value) });
    },
    [n]
  ), J = me(
    (I) => {
      I.stopPropagation(), n("valueChanged", { value: [] }), $();
    },
    [n, $]
  ), re = me((I) => {
    T(I.target.value);
  }, []), ne = me(
    (I) => {
      if (!w) {
        if (I.key === "ArrowDown" || I.key === "ArrowUp" || I.key === "Enter" || I.key === " ") {
          if (I.target.tagName === "BUTTON") return;
          I.preventDefault(), I.stopPropagation(), F();
        }
        return;
      }
      switch (I.key) {
        case "ArrowDown":
          I.preventDefault(), I.stopPropagation(), _(
            (W) => W < k.length - 1 ? W + 1 : 0
          );
          break;
        case "ArrowUp":
          I.preventDefault(), I.stopPropagation(), _(
            (W) => W > 0 ? W - 1 : k.length - 1
          );
          break;
        case "Enter":
          I.preventDefault(), I.stopPropagation(), D >= 0 && D < k.length && X(k[D].value);
          break;
        case "Escape":
          I.preventDefault(), I.stopPropagation(), $();
          break;
        case "Tab":
          $();
          break;
        case "Backspace":
          E === "" && c && r.length > 0 && M(r[r.length - 1].value);
          break;
      }
    },
    [
      w,
      F,
      $,
      k,
      D,
      X,
      E,
      c,
      r,
      M
    ]
  ), pe = me(
    async (I) => {
      I.preventDefault(), b(!1);
      try {
        await n("loadOptions");
      } catch {
        b(!0);
      }
    },
    [n]
  ), be = me(
    (I, W) => {
      K(I), W.dataTransfer.effectAllowed = "move", W.dataTransfer.setData("text/plain", String(I));
    },
    []
  ), _e = me(
    (I, W) => {
      if (W.preventDefault(), W.dataTransfer.dropEffect = "move", R === null || R === I) {
        N(null), Q(null);
        return;
      }
      const te = W.currentTarget.getBoundingClientRect(), oe = te.left + te.width / 2, y = W.clientX < oe ? "before" : "after";
      N(I), Q(y);
    },
    [R]
  ), Ce = me(
    (I) => {
      if (I.preventDefault(), R === null || B === null || O === null || R === B) return;
      const W = [...P.current], [te] = W.splice(R, 1);
      let oe = B;
      R < B ? oe = O === "before" ? oe - 1 : oe : oe = O === "before" ? oe : oe + 1, W.splice(oe, 0, te), P.current = W, n("valueChanged", { value: W.map((y) => y.value) }), K(null), N(null), Q(null);
    },
    [R, B, O, n]
  ), we = me(() => {
    K(null), N(null), Q(null);
  }, []);
  if (Le(() => {
    if (D < 0 || !j.current) return;
    const I = j.current.querySelector(
      `[id="${l}-opt-${D}"]`
    );
    I && I.scrollIntoView({ block: "nearest" });
  }, [D, l]), !a)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, p) : r.map((I) => /* @__PURE__ */ e.createElement("span", { key: I.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(it, { image: I.image }), /* @__PURE__ */ e.createElement("span", null, I.label))));
  const Te = !i && r.length > 0 && !u, $e = w ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: j,
      className: "tlDropdownSelect__dropdown",
      style: V,
      ...$t
    },
    (o || g) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: A,
        type: "text",
        className: "tlDropdownSelect__search",
        value: E,
        onChange: re,
        onKeyDown: ne,
        placeholder: v["js.dropdownSelect.filterPlaceholder"],
        "aria-label": v["js.dropdownSelect.filterPlaceholder"],
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
      !o && !g && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      g && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: pe }, v["js.dropdownSelect.error"])),
      o && k.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, h),
      o && k.map((I, W) => /* @__PURE__ */ e.createElement(
        Yl,
        {
          key: I.value,
          id: `${l}-opt-${W}`,
          option: I,
          highlighted: W === D,
          searchTerm: E,
          onSelect: X,
          onMouseEnter: () => _(W)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: H,
      className: "tlDropdownSelect" + (w ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": w,
      "aria-haspopup": "listbox",
      "aria-owns": w ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: w ? void 0 : F,
      onKeyDown: ne
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : r.map((I, W) => {
      let te = "";
      return R === W ? te = "tlDropdownSelect__chip--dragging" : B === W && O === "before" ? te = "tlDropdownSelect__chip--dropBefore" : B === W && O === "after" && (te = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        Kl,
        {
          key: I.value,
          option: I,
          removable: !u && (c || !i),
          onRemove: M,
          removeLabel: S(I.label),
          draggable: f,
          onDragStart: f ? (oe) => be(W, oe) : void 0,
          onDragOver: f ? (oe) => _e(W, oe) : void 0,
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
        "aria-label": v["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, w ? "▲" : "▼"))
  ), $e && Pt.createPortal($e, document.body));
}, { useCallback: Je, useRef: Xl } = e, Mt = "application/x-tl-color", ql = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: r,
  onSwap: c,
  onReplace: s
}) => {
  const i = Xl(null), u = Je(
    (m) => (p) => {
      i.current = m, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), a = Je((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), o = Je(
    (m) => (p) => {
      p.preventDefault();
      const f = p.dataTransfer.getData(Mt);
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
function jt(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function ot(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function Bt(l) {
  if (!ot(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function At(l, t, n) {
  const r = (c) => jt(c).toString(16).padStart(2, "0");
  return "#" + r(l) + r(t) + r(n);
}
function Zl(l, t, n) {
  const r = l / 255, c = t / 255, s = n / 255, i = Math.max(r, c, s), u = Math.min(r, c, s), a = i - u;
  let o = 0;
  a !== 0 && (i === r ? o = (c - s) / a % 6 : i === c ? o = (s - r) / a + 2 : o = (r - c) / a + 4, o *= 60, o < 0 && (o += 360));
  const m = i === 0 ? 0 : a / i;
  return [o, m, i];
}
function Ql(l, t, n) {
  const r = n * t, c = r * (1 - Math.abs(l / 60 % 2 - 1)), s = n - r;
  let i = 0, u = 0, a = 0;
  return l < 60 ? (i = r, u = c, a = 0) : l < 120 ? (i = c, u = r, a = 0) : l < 180 ? (i = 0, u = r, a = c) : l < 240 ? (i = 0, u = c, a = r) : l < 300 ? (i = c, u = 0, a = r) : (i = r, u = 0, a = c), [
    Math.round((i + s) * 255),
    Math.round((u + s) * 255),
    Math.round((a + s) * 255)
  ];
}
function Jl(l) {
  return Zl(...Bt(l));
}
function et(l, t, n) {
  return At(...Ql(l, t, n));
}
const { useCallback: Ie, useRef: kt } = e, er = ({ color: l, onColorChange: t }) => {
  const [n, r, c] = Jl(l), s = kt(null), i = kt(null), u = Ie(
    (h, S) => {
      var T;
      const w = (T = s.current) == null ? void 0 : T.getBoundingClientRect();
      if (!w) return;
      const C = Math.max(0, Math.min(1, (h - w.left) / w.width)), E = Math.max(0, Math.min(1, 1 - (S - w.top) / w.height));
      t(et(n, C, E));
    },
    [n, t]
  ), a = Ie(
    (h) => {
      h.preventDefault(), h.target.setPointerCapture(h.pointerId), u(h.clientX, h.clientY);
    },
    [u]
  ), o = Ie(
    (h) => {
      h.buttons !== 0 && u(h.clientX, h.clientY);
    },
    [u]
  ), m = Ie(
    (h) => {
      var E;
      const S = (E = i.current) == null ? void 0 : E.getBoundingClientRect();
      if (!S) return;
      const C = Math.max(0, Math.min(1, (h - S.top) / S.height)) * 360;
      t(et(C, r, c));
    },
    [r, c, t]
  ), p = Ie(
    (h) => {
      h.preventDefault(), h.target.setPointerCapture(h.pointerId), m(h.clientY);
    },
    [m]
  ), f = Ie(
    (h) => {
      h.buttons !== 0 && m(h.clientY);
    },
    [m]
  ), v = et(n, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: s,
      className: "tlColorInput__svField",
      style: { backgroundColor: v },
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
function tr(l, t) {
  const n = t.toUpperCase();
  return l.some((r) => r != null && r.toUpperCase() === n);
}
const nr = {
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
}, { useState: We, useCallback: ve, useEffect: Nt, useRef: lr, useLayoutEffect: rr } = e, ar = ({
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
  const [o, m] = We("palette"), [p, f] = We(t), v = lr(null), h = ce(nr), [S, w] = We(null);
  rr(() => {
    if (!l.current || !v.current) return;
    const j = l.current.getBoundingClientRect(), P = v.current.getBoundingClientRect();
    let Y = j.bottom + 4, d = j.left;
    Y + P.height > window.innerHeight && (Y = j.top - P.height - 4), d + P.width > window.innerWidth && (d = Math.max(0, j.right - P.width)), w({ top: Y, left: d });
  }, [l]);
  const C = p != null, [E, T, D] = C ? Bt(p) : [0, 0, 0], [_, g] = We((p == null ? void 0 : p.toUpperCase()) ?? "");
  Nt(() => {
    g((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), Ne(!0, { ESCAPE: u }), Nt(() => {
    const j = (Y) => {
      v.current && !v.current.contains(Y.target) && u();
    }, P = setTimeout(() => document.addEventListener("mousedown", j), 0);
    return () => {
      clearTimeout(P), document.removeEventListener("mousedown", j);
    };
  }, [u]);
  const b = ve(
    (j) => (P) => {
      const Y = parseInt(P.target.value, 10);
      if (isNaN(Y)) return;
      const d = jt(Y);
      f(At(j === "r" ? d : E, j === "g" ? d : T, j === "b" ? d : D));
    },
    [E, T, D]
  ), V = ve(
    (j) => {
      if (p != null) {
        j.dataTransfer.setData(Mt, p.toUpperCase()), j.dataTransfer.effectAllowed = "move";
        const P = document.createElement("div");
        P.style.width = "33px", P.style.height = "33px", P.style.backgroundColor = p, P.style.borderRadius = "3px", P.style.border = "1px solid rgba(0,0,0,0.1)", P.style.position = "absolute", P.style.top = "-9999px", document.body.appendChild(P), j.dataTransfer.setDragImage(P, 16, 16), requestAnimationFrame(() => document.body.removeChild(P));
      }
    },
    [p]
  ), L = ve((j) => {
    const P = j.target.value;
    g(P), ot(P) && f(P);
  }, []), R = ve(() => {
    f(null);
  }, []), K = ve((j) => {
    f(j);
  }, []), B = ve(
    (j) => {
      i(j);
    },
    [i]
  ), N = ve(
    (j, P) => {
      const Y = [...n], d = Y[j];
      Y[j] = Y[P], Y[P] = d, a(Y);
    },
    [n, a]
  ), O = ve(
    (j, P) => {
      const Y = [...n];
      Y[j] = P, a(Y);
    },
    [n, a]
  ), Q = ve(() => {
    a([...c]);
  }, [c, a]), H = ve(
    (j) => {
      if (tr(n, j)) return;
      const P = n.indexOf(null);
      if (P < 0) return;
      const Y = [...n];
      Y[P] = j.toUpperCase(), a(Y);
    },
    [n, a]
  ), A = ve(() => {
    p != null && H(p), i(p);
  }, [p, i, H]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: v,
      style: S ? { top: S.top, left: S.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (o === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("palette")
      },
      h["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (o === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("mixer")
      },
      h["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, o === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      ql,
      {
        colors: n,
        columns: r,
        onSelect: K,
        onConfirm: B,
        onSwap: N,
        onReplace: O
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: Q }, h["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(er, { color: p ?? "#000000", onColorChange: f }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, h["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, h["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (C ? "" : " tlColorInput--noColor"),
        style: C ? { backgroundColor: p } : void 0,
        draggable: C,
        onDragStart: C ? V : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: C ? E : "",
        onChange: b("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: C ? T : "",
        onChange: b("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: C ? D : "",
        onChange: b("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (_ !== "" && !ot(_) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: _,
        onChange: L
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, s && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: R }, h["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, h["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: A }, h["js.colorInput.ok"]))
  );
}, or = { "js.colorInput.chooseColor": "Choose color" }, { useState: sr, useCallback: ze, useRef: cr } = e, ir = ({ controlId: l, state: t }) => {
  const [n, r] = Se(), c = le(), s = ce(or), [i, u] = sr(!1), a = cr(null), o = n, m = t.editable !== !1, p = t.palette ?? [], f = t.paletteColumns ?? 6, v = t.defaultPalette ?? p, h = ze(() => {
    m && u(!0);
  }, [m]), S = ze(
    (E) => {
      u(!1), r(E);
    },
    [r]
  ), w = ze(() => {
    u(!1);
  }, []), C = ze(
    (E) => {
      c("paletteChanged", { palette: E });
    },
    [c]
  );
  return m ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: a,
      className: "tlColorInput__swatch" + (o == null ? " tlColorInput__swatch--noColor" : ""),
      style: o != null ? { backgroundColor: o } : void 0,
      onClick: h,
      disabled: t.disabled === !0,
      title: o ?? "",
      "aria-label": s["js.colorInput.chooseColor"]
    }
  ), i && /* @__PURE__ */ e.createElement(
    ar,
    {
      anchorRef: a,
      currentColor: o,
      palette: p,
      paletteColumns: f,
      defaultPalette: v,
      canReset: t.canReset !== !1,
      onConfirm: S,
      onCancel: w,
      onPaletteChange: C
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
}, { useState: Be, useCallback: ke, useEffect: tt, useRef: Tt, useLayoutEffect: ur, useMemo: dr } = e, mr = {
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
}, pr = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: r,
  onSelect: c,
  onCancel: s,
  onLoadIcons: i
}) => {
  const u = ce(mr), [a, o] = Be("simple"), [m, p] = Be(""), [f, v] = Be(t ?? ""), [h, S] = Be(!1), [w, C] = Be(null), E = Tt(null), T = Tt(null);
  ur(() => {
    if (!l.current || !E.current) return;
    const B = l.current.getBoundingClientRect(), N = E.current.getBoundingClientRect();
    let O = B.bottom + 4, Q = B.left;
    O + N.height > window.innerHeight && (O = B.top - N.height - 4), Q + N.width > window.innerWidth && (Q = Math.max(0, B.right - N.width)), C({ top: O, left: Q });
  }, [l]), tt(() => {
    !r && !h && i().catch(() => S(!0));
  }, [r, h, i]), tt(() => {
    r && T.current && T.current.focus();
  }, [r]), Ne(!0, { ESCAPE: s }), tt(() => {
    const B = (O) => {
      E.current && !E.current.contains(O.target) && s();
    }, N = setTimeout(() => document.addEventListener("mousedown", B), 0);
    return () => {
      clearTimeout(N), document.removeEventListener("mousedown", B);
    };
  }, [s]);
  const D = dr(() => {
    if (!m) return n;
    const B = m.toLowerCase();
    return n.filter(
      (N) => N.prefix.toLowerCase().includes(B) || N.label.toLowerCase().includes(B) || N.terms != null && N.terms.some((O) => O.includes(B))
    );
  }, [n, m]), _ = ke((B) => {
    p(B.target.value);
  }, []), g = ke(
    (B) => {
      c(B);
    },
    [c]
  ), b = ke((B) => {
    v(B);
  }, []), V = ke((B) => {
    v(B.target.value);
  }, []), L = ke(() => {
    c(f || null);
  }, [f, c]), R = ke(() => {
    c(null);
  }, [c]), K = ke(async (B) => {
    B.preventDefault(), S(!1);
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
      ref: E,
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
        ref: T,
        type: "text",
        className: "tlIconSelect__search",
        value: m,
        onChange: _,
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
      !r && !h && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      h && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: K }, u["js.iconSelect.loadError"])),
      r && D.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      r && D.map(
        (B) => B.variants.map((N) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: N.encoded,
            className: "tlIconSelect__iconCell" + (N.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": N.encoded === t,
            tabIndex: 0,
            title: B.label,
            onClick: () => a === "simple" ? g(N.encoded) : b(N.encoded),
            onKeyDown: (O) => {
              (O.key === "Enter" || O.key === " ") && (O.preventDefault(), a === "simple" ? g(N.encoded) : b(N.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(Ee, { encoded: N.encoded })
        ))
      )
    ),
    a === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: f,
        onChange: V
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, f && /* @__PURE__ */ e.createElement(Ee, { encoded: f })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, f ? f.startsWith("css:") ? f.substring(4) : f : ""))),
    a === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: s }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: R }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: L }, u["js.iconSelect.ok"]))
  );
}, fr = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: hr, useCallback: Ve, useRef: br } = e, _r = ({ controlId: l, state: t }) => {
  const [n, r] = Se(), c = le(), s = ce(fr), [i, u] = hr(!1), a = br(null), o = n, m = t.editable !== !1, p = t.disabled === !0, f = t.icons ?? [], v = t.iconsLoaded === !0, h = Ve(() => {
    m && !p && u(!0);
  }, [m, p]), S = Ve(
    (E) => {
      u(!1), r(E);
    },
    [r]
  ), w = Ve(() => {
    u(!1);
  }, []), C = Ve(async () => {
    await c("loadIcons");
  }, [c]);
  return m ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: a,
      className: "tlIconSelect__swatch" + (o == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: h,
      disabled: p,
      title: o ?? "",
      "aria-label": s["js.iconSelect.chooseIcon"]
    },
    o ? /* @__PURE__ */ e.createElement(Ee, { encoded: o }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), i && /* @__PURE__ */ e.createElement(
    pr,
    {
      anchorRef: a,
      currentValue: o,
      icons: f,
      iconsLoaded: v,
      onSelect: S,
      onCancel: w,
      onLoadIcons: C
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, o ? /* @__PURE__ */ e.createElement(Ee, { encoded: o }) : null));
}, { useCallback: Pe, useEffect: gr, useMemo: Rt, useRef: vr, useState: nt } = e, Er = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, Cr = [1, 2, 3, 4];
function wr(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const r = parseFloat(n[1]), c = n[2] || "px";
  return c === "rem" || c === "em" ? r * t : r;
}
function yr(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let r = 1;
  for (const c of Cr)
    n >= c && (r = c);
  return r;
}
function Sr(l, t) {
  const n = Er[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function kr(l, t) {
  const n = Math.max(1, t), r = {}, c = (p, f) => !!(r[p] && r[p][f]), s = (p, f) => {
    r[p] || (r[p] = {}), r[p][f] = !0;
  }, i = [];
  let u = 0, a = 0;
  const o = (p) => {
    let f = null;
    for (const h of i) h.rowStart === p && (f = h);
    if (!f) return;
    let v = f.colEnd;
    for (; v < n && !c(p, v); ) v++;
    if (v !== f.colEnd) {
      for (let h = f.rowStart; h < f.rowEnd; h++)
        for (let S = f.colEnd; S < v; S++) s(h, S);
      f.colEnd = v;
    }
  };
  for (const p of l) {
    const f = n <= 1 ? 1 : Math.max(1, p.rowSpan || 1);
    let v = Math.min(Sr(p.width, n), n);
    for (; c(u, a); )
      a++, a >= n && (a = 0, u++);
    let h = 0;
    for (let T = a; T < n && !c(u, T); T++)
      h++;
    if (v > h) {
      for (o(u), a = 0, u++; c(u, a); )
        a++, a >= n && (a = 0, u++);
      h = 0;
      for (let T = a; T < n && !c(u, T); T++)
        h++;
      v = Math.min(v, h);
    }
    const S = a, w = a + v, C = u, E = u + f;
    i.push({ id: p.id, colStart: S, colEnd: w, rowStart: C, rowEnd: E });
    for (let T = C; T < E; T++)
      for (let D = S; D < w; D++) s(T, D);
    a = w, a >= n && (a = 0, u++);
  }
  o(u);
  let m = 0;
  for (const p of i) p.rowEnd > m && (m = p.rowEnd);
  for (let p = 1; p < m; p++)
    for (let f = 0; f < n; f++) {
      if (c(p, f)) continue;
      const v = i.find((h) => h.rowEnd === p && h.colStart <= f && f < h.colEnd);
      if (v) {
        v.rowEnd = p + 1;
        for (let h = v.colStart; h < v.colEnd; h++) s(p, h);
      }
    }
  return i;
}
const Nr = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.minColWidth ?? "16rem", c = (t.children ?? []).filter((g) => g && g.id), s = vr(null), [i, u] = nt(1), a = t.editMode === !0;
  gr(() => {
    const g = s.current;
    if (!g) return;
    const b = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, V = wr(r, b), L = () => u(yr(g.clientWidth, V));
    L();
    const R = new ResizeObserver(L);
    return R.observe(g), () => R.disconnect();
  }, [r]);
  const o = Rt(() => kr(c, i), [c, i]), m = Rt(() => {
    const g = {};
    for (const b of o) g[b.id] = b;
    return g;
  }, [o]), [p, f] = nt(null), [v, h] = nt(null), S = Pe((g, b) => {
    if (!a) {
      g.preventDefault();
      return;
    }
    f(b), g.dataTransfer.effectAllowed = "move", g.dataTransfer.setData("text/plain", b);
  }, [a]), w = Pe((g, b) => {
    if (!a || !p || p === b) return;
    g.preventDefault(), g.dataTransfer.dropEffect = "move";
    const V = g.currentTarget.getBoundingClientRect(), L = g.clientX < V.left + V.width / 2;
    h((R) => R && R.id === b && R.before === L ? R : { id: b, before: L });
  }, [a, p]), C = Pe(() => {
  }, []), E = Pe((g, b, V) => {
    const L = c.map((N) => N.id), R = L.indexOf(g);
    if (R < 0) return;
    L.splice(R, 1);
    const K = L.indexOf(b);
    if (K < 0) {
      L.splice(R, 0, g);
      return;
    }
    const B = V ? K : K + 1;
    L.splice(B, 0, g), n("reorder", { order: L });
  }, [c, n]), T = Pe((g, b) => {
    if (!a || !p || p === b) return;
    g.preventDefault();
    const V = g.currentTarget.getBoundingClientRect(), L = g.clientX < V.left + V.width / 2;
    E(p, b, L), f(null), h(null);
  }, [a, p, E]), D = Pe(() => {
    f(null), h(null);
  }, []), _ = {
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: _ }, c.map((g) => {
      const b = m[g.id];
      if (!b) return null;
      const V = {
        gridColumn: `${b.colStart + 1} / ${b.colEnd + 1}`,
        gridRow: `${b.rowStart + 1} / ${b.rowEnd + 1}`
      }, L = ["tlDashboard__tile"];
      return p === g.id && L.push("tlDashboard__tile--dragging"), v && v.id === g.id && L.push(v.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: g.id,
          className: L.join(" "),
          style: V,
          draggable: a,
          onDragStart: (R) => S(R, g.id),
          onDragOver: (R) => w(R, g.id),
          onDragLeave: C,
          onDrop: (R) => T(R, g.id),
          onDragEnd: D
        },
        /* @__PURE__ */ e.createElement(z, { control: g.control }),
        a && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: Tr, useRef: Dt, useState: xt, useEffect: Rr, useLayoutEffect: Dr } = e, xr = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, r) => /* @__PURE__ */ e.createElement("span", { key: r, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(z, { control: n }))));
}, Lr = ({ group: l }) => {
  var p, f;
  const [t, n] = xt(!1), [r, c] = xt({}), s = Dt(null), i = Dt(null), u = Tr(() => {
    n((v) => !v);
  }, []);
  Dr(() => {
    if (!t) return;
    const v = () => {
      const h = s.current;
      if (!h) return;
      const S = h.getBoundingClientRect();
      c({
        position: "fixed",
        top: S.bottom + 4,
        right: Math.max(8, window.innerWidth - S.right),
        left: "auto"
      });
    };
    return v(), window.addEventListener("resize", v), window.addEventListener("scroll", v, !0), () => {
      window.removeEventListener("resize", v), window.removeEventListener("scroll", v, !0);
    };
  }, [t]), Rr(() => {
    if (!t) return;
    const v = (h) => {
      i.current && !i.current.contains(h.target) && s.current && !s.current.contains(h.target) && n(!1);
    };
    return document.addEventListener("mousedown", v), () => document.removeEventListener("mousedown", v);
  }, [t]), Ne(t, { ESCAPE: () => n(!1) }), ct(t, i, "first");
  const a = l.items.filter((v) => v != null);
  if (a.length === 0) return null;
  if (a.length === 1 && !((p = l.subGroups) != null && p.length) && !l.icon)
    return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(z, { control: a[0] })));
  const o = l.label ?? l.name, m = !!l.icon;
  return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--menu" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: s,
      type: "button",
      className: "tlToolbar__menuTrigger" + (m ? " tlToolbar__menuTrigger--icon" : ""),
      onMouseDown: (v) => v.preventDefault(),
      onClick: u,
      "aria-expanded": t,
      "aria-haspopup": "true",
      "aria-label": m ? o : void 0,
      title: m ? o : void 0
    },
    m ? /* @__PURE__ */ e.createElement(Ee, { encoded: l.icon, className: "tlToolbar__menuIcon" }) : /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement("span", null, o), /* @__PURE__ */ e.createElement("svg", { className: "tlToolbar__chevron", viewBox: "0 0 24 24", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("polyline", { points: "6,9 12,15 18,9" })))
  ), Pt.createPortal(
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
      a.map((v, h) => /* @__PURE__ */ e.createElement("div", { key: h, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(z, { control: v }))),
      (f = l.subGroups) == null ? void 0 : f.map((v, h) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${h}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), v.items.map((S, w) => /* @__PURE__ */ e.createElement("div", { key: w, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(z, { control: S })))))
    ),
    document.body
  ));
}, Ir = ({ controlId: l }) => {
  const r = (q().groups ?? []).filter((c) => c.items.some((s) => s != null));
  return r.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, r.map((c, s) => /* @__PURE__ */ e.createElement(e.Fragment, { key: c.name }, s > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), c.display === "menu" ? /* @__PURE__ */ e.createElement(Lr, { group: c }) : /* @__PURE__ */ e.createElement(xr, { group: c }))));
}, Pr = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlTileStack", style: { width: "100%", height: "100%" } }, t.frame && /* @__PURE__ */ e.createElement(z, { control: t.frame }));
}, Mr = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.content, c = t.breadcrumb ?? null;
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
  })), /* @__PURE__ */ e.createElement("div", { className: "tlAdaptiveDetail__content" }, r && /* @__PURE__ */ e.createElement(z, { control: r })));
}, jr = ({ controlId: l }) => {
  const n = q().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((r, c) => /* @__PURE__ */ e.createElement(z, { key: c, control: r })));
}, Br = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), Ar = {
  "js.sidebar.openDrawer": "Open navigation"
}, Or = ({ controlId: l }) => {
  const t = le(), n = ce(Ar);
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
U("TLButton", en);
U("TLUploadButton", tn);
U("TLToggleButton", ln);
U("TLTextInput", Ht);
U("TLPasswordInput", zt);
U("TLNumberInput", Kt);
U("TLDatePicker", Gt);
U("TLSelect", qt);
U("TLCheckbox", Qt);
U("TLCounter", rn);
U("TLTabBar", on);
U("TLFieldList", sn);
U("TLAudioRecorder", un);
U("TLAudioPlayer", mn);
U("TLFileUpload", fn);
U("TLBinaryField", bn);
U("TLDownload", gn);
U("TLPhotoCapture", En);
U("TLPhotoViewer", wn);
U("TLPdfViewer", Sn);
U("TLSplitPanel", kn);
U("TLPanel", In);
U("TLInset", zn);
U("TLMaximizeRoot", Pn);
U("TLDeckPane", Mn);
U("TLSidebar", Hn);
U("TLStack", Wn);
U("TLGrid", Vn);
U("TLCard", Kn);
U("TLAppBar", Yn);
U("TLBreadcrumb", Xn);
U("TLBottomBar", Zn);
U("TLDialog", el);
U("TLDialogManager", ll);
U("TLWindow", sl);
U("TLDrawer", ul);
U("TLContextMenuRegion", ml);
U("TLSnackbar", bl);
U("TLMenu", gl);
U("TLAppShell", El);
U("TLText", Cl);
U("TLTableView", Sl);
U("TLFormLayout", Ll);
U("TLFormGroup", Ml);
U("TLFormField", Ol);
U("TLResourceCell", $l);
U("TLTreeView", Ul);
U("TLDropdownSelect", Gl);
U("TLColorInput", ir);
U("TLIconSelect", _r);
U("TLDashboard", Nr);
U("TLToolbar", Ir);
U("TLTileStack", Pr);
U("TLAdaptiveDetail", Mr);
U("TLSlot", jr);
U("TLSlotContent", Br);
U("TLDrawerToggle", Or);
