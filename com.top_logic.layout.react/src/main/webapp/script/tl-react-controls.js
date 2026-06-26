import { React as e, useTLFieldValue as je, useTLCommand as le, useTLState as q, useKeyboardBinding as ue, useTLUpload as Ae, TLChild as z, useI18N as ce, useTLDataUrl as Oe, useStandaloneKeyboardScope as Se, KeyboardScopeProvider as st, useFocusTrap as ct, register as U } from "tl-react-bridge";
const { useCallback: pt, useRef: $t } = e, Ft = 300, Ut = ({ controlId: l, state: t }) => {
  const [n, a, c] = je({ debounceMs: Ft }), s = le(), i = $t(!1), u = pt(
    (k) => {
      i.current = !0, a(k.target.value);
    },
    [a]
  ), r = t.commitOnBlur === !0, o = pt(async () => {
    await c(), r && i.current && (i.current = !1, s("commit"));
  }, [c, r, s]), m = t.multiline === !0;
  if (t.editable === !1) {
    const k = "tlReactTextInput tlReactTextInput--immutable" + (m ? " tlReactTextInput--multiline" : "");
    return /* @__PURE__ */ e.createElement(
      "span",
      {
        id: l,
        className: k,
        style: m ? { whiteSpace: "pre-wrap" } : void 0
      },
      n ?? ""
    );
  }
  const p = t.hasError === !0, f = t.hasWarnings === !0, g = t.errorMessage, h = [
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
      className: h,
      "aria-invalid": p || void 0,
      title: p && g ? g : void 0
    }
  ));
}, { useCallback: ft } = e, Ht = 300, Wt = ({ controlId: l, state: t }) => {
  const [n, a, c] = je({ debounceMs: Ht }), s = ft(
    (p) => {
      a(p.target.value);
    },
    [a]
  ), i = ft(() => {
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
}, { useCallback: ht } = e, zt = 300, Vt = ({ controlId: l, state: t, config: n }) => {
  const [a, c, s] = je({ debounceMs: zt }), i = ht(
    (f) => {
      const g = f.target.value;
      c(g === "" ? null : g);
    },
    [c]
  ), u = ht(() => {
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
}, { useCallback: Kt } = e, Yt = ({ controlId: l, state: t }) => {
  const [n, a] = je(), c = Kt(
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
}, { useCallback: Gt } = e, Xt = ({ controlId: l, state: t, config: n }) => {
  var m;
  const [a, c] = je(), s = Gt(
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
}, { useCallback: qt } = e, Zt = ({ controlId: l, state: t }) => {
  const [n, a] = je(), c = qt(
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
const { useCallback: Qt } = e, Jt = ({ controlId: l, command: t, label: n, image: a, disabled: c, displayMode: s }) => {
  const i = q(), u = le(), r = t ?? "click", o = n ?? i.label, m = a ?? i.image, p = c ?? i.disabled === !0, f = s ?? i.displayMode ?? "label-only", g = i.hidden === !0, h = i.tooltip, k = g ? { display: "none" } : void 0, w = i.appearance, C = i.size, E = i.navigateUrl, T = Qt(() => {
    if (E) {
      window.location.assign(E);
      return;
    }
    u(r);
  }, [u, r, E]), D = i.keyGesture;
  ue(D, () => p || g ? !1 : (T(), !0));
  const b = f === "icon-only", _ = f === "icon-only" || f === "icon-label", v = f === "label-only" || f === "icon-label" || b && !m, Y = h ?? (b ? o : void 0), x = Y ? `text:${Y}` : void 0;
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: T,
      disabled: p,
      style: k,
      className: "tlReactButton" + (b ? " tlReactButton--iconOnly" : "") + (w === "link" ? " tlReactButton--link" : "") + (w === "primary" ? " tlReactButton--primary" : "") + (C === "small" ? " tlReactButton--small" : "") + (C === "large" ? " tlReactButton--large" : ""),
      "data-tooltip": x,
      "aria-label": b ? o : void 0
    },
    _ && m && /* @__PURE__ */ e.createElement(Ee, { encoded: m, className: "tlReactButton__image" }),
    v && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, o)
  );
}, en = ({ controlId: l }) => {
  const t = q(), n = Ae(), a = e.useRef(null), [c, s] = e.useState(!1), i = t.label ?? "", u = t.image, r = t.disabled === !0, o = t.hidden === !0, m = t.displayMode ?? "label-only", p = t.appearance, f = t.accept, g = t.multiple === !0, h = e.useCallback(() => {
    var D;
    r || c || (D = a.current) == null || D.click();
  }, [r, c]), k = e.useCallback(async (D) => {
    const b = D.target.files;
    if (!b || b.length === 0) return;
    const _ = new FormData();
    for (let v = 0; v < b.length; v++)
      _.append("file", b[v], b[v].name);
    D.target.value = "", s(!0);
    try {
      await n(_);
    } finally {
      s(!1);
    }
  }, [n]), w = m === "icon-only", C = m === "icon-only" || m === "icon-label", E = m === "label-only" || m === "icon-label" || w && !u, T = r || c;
  return /* @__PURE__ */ e.createElement("span", { id: l, style: { display: "contents" } }, /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: a,
      type: "file",
      accept: f && f !== "*" ? f : void 0,
      multiple: g || void 0,
      onChange: k,
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
}, { useCallback: tn } = e, nn = ({ controlId: l, command: t, label: n, active: a, disabled: c }) => {
  const s = q(), i = le(), u = t ?? "click", r = n ?? s.label, o = a ?? s.active === !0, m = c ?? s.disabled === !0, p = tn(() => {
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
}, ln = ({ controlId: l }) => {
  const t = q(), n = le(), a = t.count ?? 0, c = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: rn } = e, an = ({ controlId: l }) => {
  const t = q(), n = le(), a = t.tabs ?? [], c = t.activeTabId, s = rn((i) => {
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
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(z, { control: t.activeContent })));
}, on = ({ controlId: l }) => {
  const t = q(), n = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((c, s) => /* @__PURE__ */ e.createElement("div", { key: s, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(z, { control: c })))));
}, sn = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, cn = ({ controlId: l }) => {
  const t = q(), n = Ae(), [a, c] = e.useState("idle"), [s, i] = e.useState(null), u = e.useRef(null), r = e.useRef([]), o = e.useRef(null), m = t.status ?? "idle", p = t.error, f = m === "received" ? "idle" : a !== "idle" ? a : m, g = e.useCallback(async () => {
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
        const T = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", D = new MediaRecorder(E, T ? { mimeType: T } : void 0);
        u.current = D, D.ondataavailable = (b) => {
          b.data.size > 0 && r.current.push(b.data);
        }, D.onstop = async () => {
          E.getTracks().forEach((v) => v.stop()), o.current = null;
          const b = new Blob(r.current, { type: D.mimeType || "audio/webm" });
          if (r.current = [], b.size === 0) {
            c("idle");
            return;
          }
          c("uploading");
          const _ = new FormData();
          _.append("audio", b, "recording.webm"), await n(_), c("idle");
        }, D.start(), c("recording");
      } catch (E) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", E), i("js.audioRecorder.error.denied"), c("idle");
      }
    }
  }, [a, n]), h = ce(sn), k = f === "recording" ? h["js.audioRecorder.stop"] : f === "uploading" ? h["js.uploading"] : h["js.audioRecorder.record"], w = f === "uploading", C = ["tlAudioRecorder__button"];
  return f === "recording" && C.push("tlAudioRecorder__button--recording"), f === "uploading" && C.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: C.join(" "),
      onClick: g,
      disabled: w,
      title: k,
      "aria-label": k
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${f === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, h[s]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, un = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, dn = ({ controlId: l }) => {
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
        const w = await fetch(n);
        if (!w.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", w.status), i("idle");
          return;
        }
        const C = await w.blob();
        r.current = URL.createObjectURL(C);
      } catch (w) {
        console.error("[TLAudioPlayer] Fetch error:", w), i("idle");
        return;
      }
    }
    const k = new Audio(r.current);
    u.current = k, k.onended = () => {
      i("idle");
    }, k.play(), i("playing");
  }, [s, n]), p = ce(un), f = s === "loading" ? p["js.loading"] : s === "playing" ? p["js.audioPlayer.pause"] : s === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], g = s === "disabled" || s === "loading", h = ["tlAudioPlayer__button"];
  return s === "playing" && h.push("tlAudioPlayer__button--playing"), s === "loading" && h.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: h.join(" "),
      onClick: m,
      disabled: g,
      title: f,
      "aria-label": f
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${s === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, mn = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, pn = ({ controlId: l }) => {
  const t = q(), n = Ae(), [a, c] = e.useState("idle"), [s, i] = e.useState(!1), u = e.useRef(null), r = t.status ?? "idle", o = t.error, m = t.accept ?? "", p = r === "received" ? "idle" : a !== "idle" ? a : r, f = e.useCallback(async (b) => {
    c("uploading");
    const _ = new FormData();
    _.append("file", b, b.name), await n(_), c("idle");
  }, [n]), g = e.useCallback((b) => {
    var v;
    const _ = (v = b.target.files) == null ? void 0 : v[0];
    _ && f(_);
  }, [f]), h = e.useCallback(() => {
    var b;
    a !== "uploading" && ((b = u.current) == null || b.click());
  }, [a]), k = e.useCallback((b) => {
    b.preventDefault(), b.stopPropagation(), i(!0);
  }, []), w = e.useCallback((b) => {
    b.preventDefault(), b.stopPropagation(), i(!1);
  }, []), C = e.useCallback((b) => {
    var v;
    if (b.preventDefault(), b.stopPropagation(), i(!1), a === "uploading") return;
    const _ = (v = b.dataTransfer.files) == null ? void 0 : v[0];
    _ && f(_);
  }, [a, f]), E = p === "uploading", T = ce(mn), D = p === "uploading" ? T["js.uploading"] : T["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${s ? " tlFileUpload--dragover" : ""}`,
      onDragOver: k,
      onDragLeave: w,
      onDrop: C
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
        onClick: h,
        disabled: E,
        title: D,
        "aria-label": D
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    o && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, o)
  );
}, fn = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, hn = ({ controlId: l, state: t }) => {
  const a = q() ?? t ?? {}, c = Ae(), s = Oe(), i = ce(fn), u = a.editable !== !1, r = !!a.hasData, o = a.fileName ?? "download", m = a.dataRevision ?? 0, p = a.accept ?? "", f = a.status ?? "idle", g = a.error ?? null, [h, k] = e.useState("idle"), [w, C] = e.useState(!1), [E, T] = e.useState(!1), D = e.useRef(null), b = e.useCallback(async () => {
    if (!(!r || E)) {
      T(!0);
      try {
        const A = s + (s.includes("?") ? "&" : "?") + "rev=" + m, M = await fetch(A);
        if (!M.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", M.status);
          return;
        }
        const P = await M.blob(), K = URL.createObjectURL(P), d = document.createElement("a");
        d.href = K, d.download = o, d.style.display = "none", document.body.appendChild(d), d.click(), document.body.removeChild(d), URL.revokeObjectURL(K);
      } catch (A) {
        console.error("[TLBinaryField] Fetch error:", A);
      } finally {
        T(!1);
      }
    }
  }, [r, E, s, m, o]), _ = e.useCallback(async (A) => {
    k("uploading");
    const M = new FormData();
    M.append("file", A, A.name), await c(M), k("idle");
  }, [c]), v = (f === "received" ? "idle" : h !== "idle" ? h : f) === "uploading", Y = e.useCallback((A) => {
    var P;
    const M = (P = A.target.files) == null ? void 0 : P[0];
    M && _(M);
  }, [_]), x = e.useCallback(() => {
    var A;
    v || (A = D.current) == null || A.click();
  }, [v]), R = e.useCallback((A) => {
    A.preventDefault(), A.stopPropagation(), C(!0);
  }, []), V = e.useCallback((A) => {
    A.preventDefault(), A.stopPropagation(), C(!1);
  }, []), B = e.useCallback((A) => {
    var P;
    if (A.preventDefault(), A.stopPropagation(), C(!1), v) return;
    const M = (P = A.dataTransfer.files) == null ? void 0 : P[0];
    M && _(M);
  }, [v, _]), N = E ? i["js.downloading"] : i["js.download.file"].replace("{0}", o), O = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (E ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: b,
      disabled: E,
      title: N,
      "aria-label": N
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o));
  if (!u)
    return r ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, O) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, i["js.download.noFile"]));
  const Q = v, H = v ? i["js.uploading"] : i["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlBinaryField tlFileUpload${w ? " tlFileUpload--dragover" : ""}`,
      onDragOver: R,
      onDragLeave: V,
      onDrop: B
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: D,
        type: "file",
        accept: p || void 0,
        onChange: Y,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (Q ? " tlFileUpload__button--uploading" : ""),
        onClick: x,
        disabled: Q,
        title: H,
        "aria-label": H
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    r && O,
    g && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, g)
  );
}, bn = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, _n = ({ controlId: l }) => {
  const t = q(), n = Oe(), a = le(), c = !!t.hasData, s = t.dataRevision ?? 0, i = t.fileName ?? "download", u = !!t.clearable, [r, o] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!c || r)) {
      o(!0);
      try {
        const h = n + (n.includes("?") ? "&" : "?") + "rev=" + s, k = await fetch(h);
        if (!k.ok) {
          console.error("[TLDownload] Failed to fetch data:", k.status);
          return;
        }
        const w = await k.blob(), C = URL.createObjectURL(w), E = document.createElement("a");
        E.href = C, E.download = i, E.style.display = "none", document.body.appendChild(E), E.click(), document.body.removeChild(E), URL.revokeObjectURL(C);
      } catch (h) {
        console.error("[TLDownload] Fetch error:", h);
      } finally {
        o(!1);
      }
    }
  }, [c, r, n, s, i]), p = e.useCallback(async () => {
    c && await a("clear");
  }, [c, a]), f = ce(bn);
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
}, gn = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, vn = ({ controlId: l }) => {
  const t = q(), n = Ae(), [a, c] = e.useState("idle"), [s, i] = e.useState(null), [u, r] = e.useState(!1), o = e.useRef(null), m = e.useRef(null), p = e.useRef(null), f = e.useRef(null), g = e.useRef(null), h = t.error, k = e.useMemo(
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
    if (a !== "uploading") {
      if (i(null), !k) {
        (R = f.current) == null || R.click();
        return;
      }
      try {
        const V = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        m.current = V, c("overlayOpen");
      } catch (V) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", V), i("js.photoCapture.error.denied"), c("idle");
      }
    }
  }, [a, k]), T = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const R = o.current, V = p.current;
    if (!R || !V)
      return;
    V.width = R.videoWidth, V.height = R.videoHeight;
    const B = V.getContext("2d");
    B && (B.drawImage(R, 0, 0), w(), c("uploading"), V.toBlob(async (N) => {
      if (!N) {
        c("idle");
        return;
      }
      const O = new FormData();
      O.append("photo", N, "capture.jpg"), await n(O), c("idle");
    }, "image/jpeg", 0.85));
  }, [a, n, w]), D = e.useCallback(async (R) => {
    var N;
    const V = (N = R.target.files) == null ? void 0 : N[0];
    if (!V) return;
    c("uploading");
    const B = new FormData();
    B.append("photo", V, V.name), await n(B), c("idle"), f.current && (f.current.value = "");
  }, [n]);
  e.useEffect(() => {
    a === "overlayOpen" && o.current && m.current && (o.current.srcObject = m.current);
  }, [a]), e.useEffect(() => {
    var V;
    if (a !== "overlayOpen") return;
    (V = g.current) == null || V.focus();
    const R = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = R;
    };
  }, [a]), Se(a === "overlayOpen", { ESCAPE: C }), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((R) => R.stop()), m.current = null);
  }, []);
  const b = ce(gn), _ = a === "uploading" ? b["js.uploading"] : b["js.photoCapture.open"], v = ["tlPhotoCapture__cameraBtn"];
  a === "uploading" && v.push("tlPhotoCapture__cameraBtn--uploading");
  const Y = ["tlPhotoCapture__overlayVideo"];
  u && Y.push("tlPhotoCapture__overlayVideo--mirrored");
  const x = ["tlPhotoCapture__mirrorBtn"];
  return u && x.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: v.join(" "),
      onClick: E,
      disabled: a === "uploading",
      title: _,
      "aria-label": _
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !k && /* @__PURE__ */ e.createElement(
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
      ref: g,
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
        className: Y.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: x.join(" "),
        onClick: () => r((R) => !R),
        title: b["js.photoCapture.mirror"],
        "aria-label": b["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: T,
        title: b["js.photoCapture.capture"],
        "aria-label": b["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: C,
        title: b["js.photoCapture.close"],
        "aria-label": b["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, b[s]), h && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, h));
}, En = {
  "js.photoViewer.alt": "Captured photo"
}, Cn = ({ controlId: l }) => {
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
  const r = ce(En);
  return !a || !s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: s,
      alt: r["js.photoViewer.alt"]
    }
  ));
}, wn = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, yn = ({ controlId: l }) => {
  const t = q(), n = Oe(), a = !!t.hasPdf, c = t.dataRevision ?? 0, s = ce(wn), u = n.indexOf("react-api/"), r = u >= 0 ? n.slice(0, u) : n, o = n + "&rev=" + c, m = r + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(o);
  return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: m,
      title: s["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, s["js.pdfViewer.noDocument"]));
}, { useCallback: bt, useRef: Ge } = e, kn = ({ controlId: l }) => {
  const t = q(), n = le(), a = t.orientation, c = t.resizable === !0, s = t.children ?? [], i = a === "horizontal", u = s.length > 0 && s.every((w) => w.collapsed), r = !u && s.some((w) => w.collapsed), o = u ? !i : i, m = Ge(null), p = Ge(null), f = Ge(null), g = bt((w, C) => {
    const E = {
      overflow: w.scrolling || "auto"
    };
    return w.collapsed ? u && !o ? E.flex = "1 0 0%" : E.flex = "0 0 auto" : C !== void 0 ? E.flex = `0 0 ${C}px` : E.flex = `${w.size} 1 0%`, w.minSize > 0 && !w.collapsed && (E.minWidth = i ? w.minSize : void 0, E.minHeight = i ? void 0 : w.minSize), E;
  }, [i, u, r, o]), h = bt((w, C) => {
    w.preventDefault();
    const E = m.current;
    if (!E) return;
    const T = s[C], D = s[C + 1], b = E.querySelectorAll(":scope > .tlSplitPanel__child"), _ = [];
    b.forEach((x) => {
      _.push(i ? x.offsetWidth : x.offsetHeight);
    }), f.current = _, p.current = {
      splitterIndex: C,
      startPos: i ? w.clientX : w.clientY,
      startSizeBefore: _[C],
      startSizeAfter: _[C + 1],
      childBefore: T,
      childAfter: D
    };
    const v = (x) => {
      const R = p.current;
      if (!R || !f.current) return;
      const B = (i ? x.clientX : x.clientY) - R.startPos, N = R.childBefore.minSize || 0, O = R.childAfter.minSize || 0;
      let Q = R.startSizeBefore + B, H = R.startSizeAfter - B;
      Q < N && (H += Q - N, Q = N), H < O && (Q += H - O, H = O), f.current[R.splitterIndex] = Q, f.current[R.splitterIndex + 1] = H;
      const A = E.querySelectorAll(":scope > .tlSplitPanel__child"), M = A[R.splitterIndex], P = A[R.splitterIndex + 1];
      M && (M.style.flex = `0 0 ${Q}px`), P && (P.style.flex = `0 0 ${H}px`);
    }, Y = () => {
      if (document.removeEventListener("mousemove", v), document.removeEventListener("mouseup", Y), document.body.style.cursor = "", document.body.style.userSelect = "", f.current) {
        const x = {};
        s.forEach((R, V) => {
          const B = R.control;
          B != null && B.controlId && f.current && (x[B.controlId] = f.current[V]);
        }), n("updateSizes", { sizes: x });
      }
      f.current = null, p.current = null;
    };
    document.addEventListener("mousemove", v), document.addEventListener("mouseup", Y), document.body.style.cursor = i ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [s, i, n]), k = [];
  return s.forEach((w, C) => {
    if (k.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${C}`,
          className: `tlSplitPanel__child${w.collapsed && o ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: g(w)
        },
        /* @__PURE__ */ e.createElement(z, { control: w.control })
      )
    ), c && C < s.length - 1) {
      const E = s[C + 1];
      !w.collapsed && !E.collapsed && k.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${C}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${a}`,
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
      className: `tlSplitPanel tlSplitPanel--${a}${u ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: o ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    k
  );
}, { useCallback: Xe } = e, Sn = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Nn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Tn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), Rn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), Dn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), xn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), Ln = ({ controlId: l }) => {
  const t = q(), n = le(), a = ce(Sn), c = t.title, s = t.expansionState ?? "NORMALIZED", i = t.showMinimize === !0, u = t.showMaximize === !0, r = t.showPopOut === !0, o = t.fullLine === !0, m = t.fill === !0, p = s === "MINIMIZED", f = s === "MAXIMIZED", g = s === "HIDDEN", h = Xe(() => {
    n("toggleMinimize");
  }, [n]), k = Xe(() => {
    n("toggleMaximize");
  }, [n]), w = Xe(() => {
    n("popOut");
  }, [n]);
  if (g)
    return null;
  const C = f ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" }, E = i && !f || u && !p || r, T = !!c && c.trim() !== "" || !!t.toolbar || E;
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
        title: p ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      p ? /* @__PURE__ */ e.createElement(Tn, null) : /* @__PURE__ */ e.createElement(Nn, null)
    ), u && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: k,
        title: f ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      f ? /* @__PURE__ */ e.createElement(Dn, null) : /* @__PURE__ */ e.createElement(Rn, null)
    ), r && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: w,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(xn, null)
    ))),
    !p && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(z, { control: t.child })),
    !p && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(z, { control: t.buttonBar }))
  );
}, In = ({ controlId: l }) => {
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
}, Pn = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(z, { control: t.activeChild }));
}, { useCallback: he, useState: Ke, useEffect: lt, useRef: Ye } = e, jn = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function rt(l, t, n, a) {
  const c = [];
  for (const s of l)
    if (s.type === "nav") {
      if (s.hidden) continue;
      c.push({ id: s.id, type: "nav", groupId: a });
    } else s.type === "command" ? c.push({ id: s.id, type: "command", groupId: a }) : s.type === "group" && (c.push({ id: s.id, type: "group" }), (n.get(s.id) ?? s.expanded) && !t && c.push(...rt(s.children, t, n, s.id)));
  return c;
}
const Pe = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(Ee, { encoded: l, className: "tlSidebar__icon" }) : null, Mn = ({ item: l, active: t, collapsed: n, onSelect: a, tabIndex: c, itemRef: s, onFocus: i }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => a(l.id),
    title: n ? l.label : void 0,
    tabIndex: c,
    ref: s,
    onFocus: () => i(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Pe, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Pe, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), Bn = ({ item: l, collapsed: t, onExecute: n, tabIndex: a, itemRef: c, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: a,
    ref: c,
    onFocus: () => s(l.id)
  },
  /* @__PURE__ */ e.createElement(Pe, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), An = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Pe, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), On = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), $n = ({ item: l, activeItemId: t, anchorRect: n, onSelect: a, onExecute: c, onClose: s }) => {
  const i = Ye(null);
  lt(() => {
    const o = (m) => {
      i.current && !i.current.contains(m.target) && setTimeout(() => s(), 0);
    };
    return document.addEventListener("mousedown", o), () => document.removeEventListener("mousedown", o);
  }, [s]), Se(!0, { ESCAPE: s });
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
        /* @__PURE__ */ e.createElement(Pe, { icon: o.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, o.label),
        o.type === "nav" && o.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, o.badge)
      );
    }
    return o.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: o.id, className: "tlSidebar__flyoutSectionHeader" }, o.label) : o.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: o.id, className: "tlSidebar__separator" }) : null;
  }));
}, Fn = ({
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
  onOpenFlyout: h,
  onCloseFlyout: k
}) => {
  const w = Ye(null), [C, E] = Ke(null), T = he(() => {
    a ? g === l.id ? k() : (w.current && E(w.current.getBoundingClientRect()), h(l.id)) : i(l.id);
  }, [a, g, l.id, i, h, k]), D = he((_) => {
    w.current = _, r(_);
  }, [r]), b = a && g === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (b ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: T,
      title: a ? l.label : void 0,
      "aria-expanded": a ? b : t,
      tabIndex: u,
      ref: D,
      onFocus: () => o(l.id)
    },
    /* @__PURE__ */ e.createElement(Pe, { icon: l.icon }),
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
    $n,
    {
      item: l,
      activeItemId: n,
      anchorRect: C,
      onSelect: c,
      onExecute: s,
      onClose: k
    }
  ), t && !a && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((_) => /* @__PURE__ */ e.createElement(
    Lt,
    {
      key: _.id,
      item: _,
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
      onOpenFlyout: h,
      onCloseFlyout: k
    }
  ))));
}, Lt = ({
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
        Mn,
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
        Bn,
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
      return /* @__PURE__ */ e.createElement(An, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(On, null);
    case "group": {
      const g = o ? o.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        Fn,
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
}, Un = ({ controlId: l }) => {
  const t = q(), n = le(), a = ce(jn), c = t.items ?? [], s = t.activeItemId, i = t.collapsed, u = t.drawerOpen, r = u ? !1 : i, [o, m] = Ke(() => {
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
  }, [n, s]), g = he((N) => {
    n("executeCommand", { itemId: N });
  }, [n]), h = he(() => {
    n("toggleCollapse", {});
  }, [n]), k = he(() => {
    n("toggleDrawer", {});
  }, [n]), [w, C] = Ke(null), E = he((N) => {
    C(N);
  }, []), T = he(() => {
    C(null);
  }, []);
  lt(() => {
    r || C(null);
  }, [r]);
  const [D, b] = Ke(() => {
    const N = rt(c, r, o);
    return N.length > 0 ? N[0].id : "";
  }), _ = Ye(/* @__PURE__ */ new Map()), v = he((N) => (O) => {
    O ? _.current.set(N, O) : _.current.delete(N);
  }, []), Y = he((N) => {
    b(N);
  }, []), x = Ye(0), R = he((N) => {
    b(N), x.current++;
  }, []);
  lt(() => {
    const N = _.current.get(D);
    N && document.activeElement !== N && N.focus();
  }, [D, x.current]);
  const V = he((N) => {
    if (N.key === "Escape" && w !== null) {
      N.preventDefault(), T();
      return;
    }
    const O = rt(c, r, o);
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
        N.preventDefault(), H.type === "nav" ? f(H.id) : H.type === "command" ? g(H.id) : H.type === "group" && (r ? w === H.id ? T() : E(H.id) : p(H.id));
        break;
      }
      case "ArrowRight": {
        H.type === "group" && !r && ((o.get(H.id) ?? !1) || (N.preventDefault(), p(H.id)));
        break;
      }
      case "ArrowLeft": {
        H.type === "group" && !r && (o.get(H.id) ?? !1) && (N.preventDefault(), p(H.id));
        break;
      }
    }
  }, [
    c,
    r,
    o,
    D,
    w,
    R,
    f,
    g,
    p,
    E,
    T
  ]), B = "tlSidebar" + (r ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: B }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(z, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: k, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": a["js.sidebar.ariaLabel"] }, r ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(z, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(z, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: V }, c.map((N) => /* @__PURE__ */ e.createElement(
    Lt,
    {
      key: N.id,
      item: N,
      activeItemId: s,
      collapsed: r,
      onSelect: f,
      onExecute: g,
      onToggleGroup: p,
      focusedId: D,
      setItemRef: v,
      onItemFocus: Y,
      groupStates: o,
      flyoutGroupId: w,
      onOpenFlyout: E,
      onCloseFlyout: T
    }
  ))), r ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(z, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(z, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(z, { control: t.activeContent })));
}, Hn = ({ controlId: l }) => {
  const t = q(), n = t.direction ?? "column", a = t.gap ?? "default", c = t.align ?? "stretch", s = t.wrap === !0, i = t.growFirst === !0, u = t.children ?? [], r = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${a}`,
    `tlStack--align-${c}`,
    s ? "tlStack--wrap" : "",
    i ? "tlStack--grow-first" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: r }, u.map((o, m) => /* @__PURE__ */ e.createElement(z, { key: m, control: o })));
}, Wn = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlInset" }, t.child && /* @__PURE__ */ e.createElement(z, { control: t.child }));
}, zn = ({ controlId: l }) => {
  const t = q(), n = t.columns, a = t.minColumnWidth, c = t.gap ?? "default", s = t.children ?? [], i = {};
  return a ? i.gridTemplateColumns = `repeat(auto-fit, minmax(${a}, 1fr))` : n && (i.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${c}`, style: i }, s.map((u, r) => /* @__PURE__ */ e.createElement(z, { key: r, control: u })));
}, Vn = ({ controlId: l }) => {
  const t = q(), n = t.title, a = t.variant ?? "outlined", c = t.padding ?? "default", s = t.headerActions ?? [], i = t.child, u = n != null || s.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${a}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, s.map((r, o) => /* @__PURE__ */ e.createElement(z, { key: o, control: r })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${c}` }, /* @__PURE__ */ e.createElement(z, { control: i })));
}, Kn = ({ controlId: l }) => {
  const t = q(), n = t.title ?? "", a = t.leading, c = t.children ?? [], s = t.actions ?? [], i = t.variant ?? "flat", r = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    i === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: r }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(z, { control: a })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, c.map((o, m) => /* @__PURE__ */ e.createElement(z, { key: m, control: o }))), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((o, m) => /* @__PURE__ */ e.createElement(z, { key: m, control: o }))));
}, { useCallback: Yn } = e, Gn = ({ controlId: l }) => {
  const t = q(), n = le(), a = t.items ?? [], c = Yn((s) => {
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
}, { useCallback: Xn } = e, qn = ({ controlId: l }) => {
  const t = q(), n = le(), a = t.items ?? [], c = t.activeItemId, s = Xn((i) => {
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
}, { useCallback: _t, useRef: Zn } = e, Qn = ({ onClose: l }) => (ue("ESCAPE", () => (l(), !0)), null), Jn = ({ controlId: l }) => {
  const t = q(), n = le(), a = t.open === !0, c = t.closeOnBackdrop !== !1, s = t.child, i = Zn(null), u = _t(() => {
    n("close");
  }, [n]), r = _t((o) => {
    c && o.target === o.currentTarget && u();
  }, [c, u]);
  return a ? /* @__PURE__ */ e.createElement(st, null, /* @__PURE__ */ e.createElement(Qn, { onClose: u }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: r,
      ref: i,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(z, { control: s })
  )) : null;
}, { useEffect: el, useRef: tl } = e, nl = ({ controlId: l }) => {
  const n = q().dialogs ?? [], a = tl(n.length);
  return el(() => {
    n.length < a.current && n.length > 0, a.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((c) => /* @__PURE__ */ e.createElement(z, { key: c.controlId, control: c })));
}, { useCallback: Ue, useRef: De, useState: He } = e, ll = ({ onClose: l }) => (ue("ESCAPE", () => (l(), !0)), null), rl = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, al = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], ol = ({ controlId: l }) => {
  const t = q(), n = le(), a = ce(rl), c = t.title ?? "", s = t.width ?? "32rem", i = t.height ?? null, u = t.minHeight ?? null, r = t.resizable === !0, o = t.child, m = t.actions ?? [], p = t.toolbar, f = t.buttonBar, [g, h] = He(null), [k, w] = He(null), [C, E] = He(null), T = De(null), [D, b] = He(!1), _ = De(null), v = De(null), Y = De(null), x = De(null), R = De(null), V = Ue(() => {
    n("close");
  }, [n]);
  ct(!0, x, "field");
  const B = Ue((A, M) => {
    M.preventDefault();
    const P = x.current;
    if (!P) return;
    const K = P.getBoundingClientRect(), d = !T.current, S = T.current ?? { x: K.left, y: K.top };
    d && (T.current = S, E(S)), R.current = {
      dir: A,
      startX: M.clientX,
      startY: M.clientY,
      startW: K.width,
      startH: K.height,
      startPos: { ...S },
      symmetric: d
    };
    const F = (G) => {
      const j = R.current;
      if (!j) return;
      const J = G.clientX - j.startX, re = G.clientY - j.startY;
      let te = j.startW, pe = j.startH, be = 0, _e = 0;
      j.symmetric ? (j.dir.includes("e") && (te = j.startW + 2 * J), j.dir.includes("w") && (te = j.startW - 2 * J), j.dir.includes("s") && (pe = j.startH + 2 * re), j.dir.includes("n") && (pe = j.startH - 2 * re)) : (j.dir.includes("e") && (te = j.startW + J), j.dir.includes("w") && (te = j.startW - J, be = J), j.dir.includes("s") && (pe = j.startH + re), j.dir.includes("n") && (pe = j.startH - re, _e = re));
      const Ce = Math.max(200, te), we = Math.max(100, pe);
      j.symmetric ? (be = (j.startW - Ce) / 2, _e = (j.startH - we) / 2) : (j.dir.includes("w") && Ce === 200 && (be = j.startW - 200), j.dir.includes("n") && we === 100 && (_e = j.startH - 100)), v.current = Ce, Y.current = we, h(Ce), w(we);
      const Ne = {
        x: j.startPos.x + be,
        y: j.startPos.y + _e
      };
      T.current = Ne, E(Ne);
    }, $ = () => {
      document.removeEventListener("mousemove", F), document.removeEventListener("mouseup", $);
      const G = v.current, j = Y.current;
      (G != null || j != null) && n("resize", {
        ...G != null ? { width: Math.round(G) + "px" } : {},
        ...j != null ? { height: Math.round(j) + "px" } : {}
      }), R.current = null;
    };
    document.addEventListener("mousemove", F), document.addEventListener("mouseup", $);
  }, [n]), N = Ue((A) => {
    if (A.button !== 0 || A.target.closest("button")) return;
    A.preventDefault();
    const M = x.current;
    if (!M) return;
    const P = M.getBoundingClientRect(), K = T.current ?? { x: P.left, y: P.top }, d = A.clientX - K.x, S = A.clientY - K.y, F = (G) => {
      const j = window.innerWidth, J = window.innerHeight;
      let re = G.clientX - d, te = G.clientY - S;
      const pe = M.offsetWidth, be = M.offsetHeight;
      re + pe > j && (re = j - pe), te + be > J && (te = J - be), re < 0 && (re = 0), te < 0 && (te = 0);
      const _e = { x: re, y: te };
      T.current = _e, E(_e);
    }, $ = () => {
      document.removeEventListener("mousemove", F), document.removeEventListener("mouseup", $);
    };
    document.addEventListener("mousemove", F), document.addEventListener("mouseup", $);
  }, []), O = Ue(() => {
    var A, M;
    if (D) {
      const P = _.current;
      P && (E(P.x !== -1 ? { x: P.x, y: P.y } : null), h(P.w), w(P.h)), b(!1);
    } else {
      const P = x.current, K = P == null ? void 0 : P.getBoundingClientRect();
      _.current = {
        x: ((A = T.current) == null ? void 0 : A.x) ?? (K == null ? void 0 : K.left) ?? -1,
        y: ((M = T.current) == null ? void 0 : M.y) ?? (K == null ? void 0 : K.top) ?? -1,
        w: g ?? (K == null ? void 0 : K.width) ?? null,
        h: k ?? null
      }, b(!0), E({ x: 0, y: 0 }), h(null), w(null);
    }
  }, [D, g, k]), Q = D ? { position: "absolute", top: 0, left: 0, width: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: g != null ? g + "px" : s,
    ...k != null ? { height: k + "px" } : i != null ? { height: i } : {},
    ...u != null && k == null ? { minHeight: u } : {},
    maxHeight: C ? "100vh" : "80vh",
    ...C ? { position: "absolute", left: C.x + "px", top: C.y + "px" } : {}
  }, H = l + "-title";
  return /* @__PURE__ */ e.createElement(st, { modal: !0 }, /* @__PURE__ */ e.createElement(ll, { onClose: V }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: Q,
      ref: x,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": H
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${D ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: D ? void 0 : N,
        onDoubleClick: r ? O : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: H }, c),
      p && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(z, { control: p })),
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
          onClick: V,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(z, { control: o })),
    (m.length > 0 || f) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, f && /* @__PURE__ */ e.createElement(z, { control: f }), m.map((A, M) => /* @__PURE__ */ e.createElement(z, { key: M, control: A }))),
    r && !D && al.map((A) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: A,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${A}`,
        onMouseDown: (M) => B(A, M)
      }
    ))
  ));
}, { useCallback: sl } = e, cl = {
  "js.drawer.close": "Close"
}, il = ({ controlId: l }) => {
  const t = q(), n = le(), a = ce(cl), c = t.open === !0, s = t.position ?? "right", i = t.size ?? "medium", u = t.title ?? null, r = t.child, o = sl(() => {
    n("close");
  }, [n]);
  Se(c, { ESCAPE: o });
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, r && /* @__PURE__ */ e.createElement(z, { control: r })));
}, { useCallback: ul } = e, dl = ({ controlId: l }) => {
  const t = q(), n = le(), a = t.child, c = ul((s) => {
    s.preventDefault(), s.stopPropagation(), n("openContextMenu", { x: s.clientX, y: s.clientY });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tl-context-menu-region", onContextMenu: c }, a && /* @__PURE__ */ e.createElement(z, { control: a }));
}, { useCallback: ml, useEffect: pl, useState: fl } = e, hl = ({ controlId: l }) => {
  const t = q(), n = le(), a = t.message ?? "", c = t.content ?? "", s = t.variant ?? "info", i = t.duration ?? 5e3, u = t.visible === !0, r = t.generation ?? 0, [o, m] = fl(!1), p = ml(() => {
    m(!0), setTimeout(() => {
      n("dismiss", { generation: r }), m(!1);
    }, 200);
  }, [n, r]);
  return pl(() => {
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
}, { useCallback: qe, useEffect: gt, useRef: bl, useState: vt } = e, _l = ({ controlId: l }) => {
  const t = q(), n = le(), a = t.open === !0, c = t.anchorId, s = t.anchorX, i = t.anchorY, u = t.items ?? [], r = bl(null), [o, m] = vt({ top: 0, left: 0 }), [p, f] = vt(0), g = u.filter((C) => C.type === "item" && !C.disabled);
  gt(() => {
    var v, Y;
    if (!a) return;
    const C = ((v = r.current) == null ? void 0 : v.offsetHeight) ?? 200, E = ((Y = r.current) == null ? void 0 : Y.offsetWidth) ?? 200;
    if (s != null && i != null) {
      let x = i, R = s;
      x + C > window.innerHeight && (x = Math.max(0, window.innerHeight - C)), R + E > window.innerWidth && (R = Math.max(0, window.innerWidth - E)), m({ top: x, left: R }), f(0);
      return;
    }
    if (!c) return;
    const T = document.getElementById(c);
    if (!T) return;
    const D = T.getBoundingClientRect();
    let b = D.bottom + 4, _ = D.left;
    b + C > window.innerHeight && (b = D.top - C - 4), _ + E > window.innerWidth && (_ = D.right - E), m({ top: b, left: _ }), f(0);
  }, [a, c, s, i]);
  const h = qe(() => {
    n("close");
  }, [n]), k = qe((C) => {
    n("selectItem", { itemId: C });
  }, [n]);
  gt(() => {
    if (!a) return;
    const C = (E) => {
      r.current && !r.current.contains(E.target) && h();
    };
    return document.addEventListener("mousedown", C), () => document.removeEventListener("mousedown", C);
  }, [a, h]);
  const w = qe((C) => {
    if (C.key === "Escape") {
      C.preventDefault(), h();
      return;
    }
    if (C.key === "ArrowDown")
      C.preventDefault(), f((E) => (E + 1) % g.length);
    else if (C.key === "ArrowUp")
      C.preventDefault(), f((E) => (E - 1 + g.length) % g.length);
    else if (C.key === "Enter" || C.key === " ") {
      C.preventDefault();
      const E = g[p];
      E && k(E.id);
    }
  }, [h, k, g, p]);
  return ct(a, r), a ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: r,
      tabIndex: -1,
      style: { position: "fixed", top: o.top, left: o.left },
      onKeyDown: w
    },
    u.map((C, E) => {
      if (C.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: E, className: "tlMenu__separator" });
      const D = g.indexOf(C) === p;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: C.id,
          type: "button",
          className: "tlMenu__item" + (D ? " tlMenu__item--focused" : "") + (C.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: C.disabled,
          tabIndex: D ? 0 : -1,
          onClick: () => k(C.id)
        },
        C.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + C.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, C.label)
      );
    })
  ) : null;
}, gl = 768, vl = ({ controlId: l }) => {
  const t = q(), n = le();
  e.useEffect(() => {
    const o = window.matchMedia(`(max-width: ${gl}px)`), m = (f) => {
      n("reportDisplayClass", { displayClass: f ? "COMPACT" : "REGULAR" });
    };
    m(o.matches);
    const p = (f) => m(f.matches);
    return o.addEventListener("change", p), () => o.removeEventListener("change", p);
  }, [n]);
  const a = t.header, c = t.content, s = t.footer, i = t.snackbar, u = t.dialogManager, r = t.menuOverlay;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(z, { control: a })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(z, { control: c })), s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(z, { control: s })), /* @__PURE__ */ e.createElement(z, { control: i }), u && /* @__PURE__ */ e.createElement(z, { control: u }), r && /* @__PURE__ */ e.createElement(z, { control: r }));
}, El = ({ controlId: l }) => {
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
}, Cl = ({ isMulti: l, cursorIndex: t, onMove: n, onToggle: a, onSelectAll: c }) => (ue("ArrowUp", () => (n("up", !1, !1), !0)), ue("ArrowDown", () => (n("down", !1, !1), !0)), ue("Home", () => (n("home", !1, !1), !0)), ue("End", () => (n("end", !1, !1), !0)), ue("PageUp", () => (n("pageUp", !1, !1), !0)), ue("PageDown", () => (n("pageDown", !1, !1), !0)), ue("Shift+ArrowUp", () => (n("up", l, !1), !0)), ue("Shift+ArrowDown", () => (n("down", l, !1), !0)), ue("Shift+Home", () => (n("home", l, !1), !0)), ue("Shift+End", () => (n("end", l, !1), !0)), ue("Shift+PageUp", () => (n("pageUp", l, !1), !0)), ue("Shift+PageDown", () => (n("pageDown", l, !1), !0)), ue("Ctrl+ArrowUp", () => (n("up", !1, l), !0)), ue("Ctrl+ArrowDown", () => (n("down", !1, l), !0)), ue("Space", () => t < 0 ? !1 : (a(), !0)), ue("Ctrl+A", () => l ? (c(), !0) : !1), null), wl = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all",
  "js.table.filter": "Filter"
}, Et = 50, yl = ({ controlId: l }) => {
  const t = q(), n = le(), a = ce(wl), c = e.useRef(null);
  e.useEffect(() => {
    const y = c.current;
    if (!y) return;
    const I = (X) => {
      const ne = X.detail;
      let ae = ne.target;
      for (; ae && ae !== y; ) {
        const fe = ae.dataset.row, se = ae.dataset.col;
        if (fe != null && se != null) {
          ne.resolved = { key: fe + "|" + se };
          return;
        }
        ae = ae.parentElement;
      }
    };
    return y.addEventListener("tl-tooltip-resolve", I), () => y.removeEventListener("tl-tooltip-resolve", I);
  }, []);
  const s = t.columns ?? [], i = t.totalRowCount ?? 0, u = t.rows ?? [], r = t.rowHeight ?? 36, o = t.selectionMode ?? "single", m = t.selectedCount ?? 0, p = t.cursorIndex ?? -1, f = t.frozenColumnCount ?? 0, g = t.treeMode ?? !1, h = e.useMemo(
    () => s.filter((y) => y.sortPriority && y.sortPriority > 0).length,
    [s]
  ), k = o === "multi", w = 40, C = 20, E = e.useRef(null), T = e.useRef(null), D = e.useRef(null), [b, _] = e.useState({}), v = e.useRef(null), Y = e.useRef(!1), x = e.useRef(null), [R, V] = e.useState(null), [B, N] = e.useState(null);
  e.useEffect(() => {
    v.current || _({});
  }, [s]);
  const O = e.useCallback((y) => b[y.name] ?? y.width, [b]), Q = e.useMemo(() => {
    const y = [];
    let I = k && f > 0 ? w : 0;
    for (let X = 0; X < f && X < s.length; X++)
      y.push(I), I += O(s[X]);
    return y;
  }, [s, f, k, w, O]), H = i * r, A = e.useRef(null), M = e.useCallback((y, I, X) => {
    X.preventDefault(), X.stopPropagation(), v.current = { column: y, startX: X.clientX, startWidth: I };
    let ne = X.clientX, ae = 0;
    const fe = () => {
      const ie = v.current;
      if (!ie) return;
      const ge = Math.max(Et, ie.startWidth + (ne - ie.startX) + ae);
      _((Re) => ({ ...Re, [ie.column]: ge }));
    }, se = () => {
      const ie = T.current, ge = E.current;
      if (!ie || !v.current) return;
      const Re = ie.getBoundingClientRect(), ut = 40, dt = 8, Ot = ie.scrollLeft;
      ne > Re.right - ut ? ie.scrollLeft += dt : ne < Re.left + ut && (ie.scrollLeft = Math.max(0, ie.scrollLeft - dt));
      const mt = ie.scrollLeft - Ot;
      mt !== 0 && (ge && (ge.scrollLeft = ie.scrollLeft), ae += mt, fe()), A.current = requestAnimationFrame(se);
    };
    A.current = requestAnimationFrame(se);
    const Te = (ie) => {
      ne = ie.clientX, fe();
    }, Fe = (ie) => {
      document.removeEventListener("mousemove", Te), document.removeEventListener("mouseup", Fe), A.current !== null && (cancelAnimationFrame(A.current), A.current = null);
      const ge = v.current;
      if (ge) {
        const Re = Math.max(Et, ge.startWidth + (ie.clientX - ge.startX) + ae);
        n("columnResize", { column: ge.column, width: Re }), v.current = null, Y.current = !0, requestAnimationFrame(() => {
          Y.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", Te), document.addEventListener("mouseup", Fe);
  }, [n]), P = e.useCallback(() => {
    E.current && T.current && (E.current.scrollLeft = T.current.scrollLeft), D.current !== null && clearTimeout(D.current), D.current = window.setTimeout(() => {
      const y = T.current;
      if (!y) return;
      const I = y.scrollTop, X = Math.ceil(y.clientHeight / r), ne = Math.floor(I / r);
      n("scroll", { start: ne, count: X });
    }, 80);
  }, [n, r]), K = e.useCallback((y, I, X) => {
    if (Y.current) return;
    let ne;
    !I || I === "desc" ? ne = "asc" : ne = "desc";
    const ae = X.shiftKey ? "add" : "replace";
    n("sort", { column: y, direction: ne, mode: ae });
  }, [n]), d = e.useCallback((y, I) => {
    x.current = y, I.dataTransfer.effectAllowed = "move", I.dataTransfer.setData("text/plain", y);
  }, []), S = e.useCallback((y, I) => {
    if (!x.current || x.current === y) {
      V(null);
      return;
    }
    I.preventDefault(), I.dataTransfer.dropEffect = "move";
    const X = I.currentTarget.getBoundingClientRect(), ne = I.clientX < X.left + X.width / 2 ? "left" : "right";
    V({ column: y, side: ne });
  }, []), F = e.useCallback((y) => {
    y.preventDefault(), y.stopPropagation();
    const I = x.current;
    if (!I || !R) {
      x.current = null, V(null);
      return;
    }
    let X = s.findIndex((ae) => ae.name === R.column);
    if (X < 0) {
      x.current = null, V(null);
      return;
    }
    const ne = s.findIndex((ae) => ae.name === I);
    R.side === "right" && X++, ne < X && X--, n("columnReorder", { column: I, targetIndex: X }), x.current = null, V(null);
  }, [s, R, n]), $ = e.useCallback(() => {
    x.current = null, V(null);
  }, []), G = e.useCallback((y, I) => {
    var X;
    I.shiftKey && I.preventDefault(), (X = T.current) == null || X.focus({ preventScroll: !0 }), n("select", {
      rowIndex: y,
      ctrlKey: I.ctrlKey || I.metaKey,
      shiftKey: I.shiftKey
    });
  }, [n]), j = e.useCallback((y, I, X) => {
    n("moveSelection", { direction: y, extend: I, move: X });
  }, [n]), J = e.useCallback(() => {
    p < 0 || n("select", { rowIndex: p, ctrlKey: k, shiftKey: !1 });
  }, [n, p, k]), re = e.useCallback(() => {
    n("selectAll", { selected: !0 });
  }, [n]), te = e.useCallback(
    () => !!c.current && c.current.contains(document.activeElement),
    []
  );
  e.useEffect(() => {
    if (p < 0)
      return;
    const y = T.current;
    if (!y)
      return;
    const I = p * r, X = I + r;
    I < y.scrollTop ? y.scrollTop = I : X > y.scrollTop + y.clientHeight && (y.scrollTop = X - y.clientHeight);
  }, [p, r]);
  const pe = e.useCallback((y, I) => {
    I.stopPropagation(), n("select", { rowIndex: y, ctrlKey: !0, shiftKey: !1 });
  }, [n]), be = e.useCallback(() => {
    const y = m === i && i > 0;
    n("selectAll", { selected: !y });
  }, [n, m, i]), _e = e.useCallback((y, I, X) => {
    X.stopPropagation(), n("expand", { rowIndex: y, expanded: I });
  }, [n]), Ce = e.useCallback((y, I) => {
    I.preventDefault(), N({ x: I.clientX, y: I.clientY, colIdx: y });
  }, []), we = e.useCallback(() => {
    B && (n("setFrozenColumnCount", { count: B.colIdx + 1 }), N(null));
  }, [B, n]), Ne = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), N(null);
  }, [n]);
  e.useEffect(() => {
    if (!B) return;
    const y = () => N(null);
    return document.addEventListener("mousedown", y), () => document.removeEventListener("mousedown", y);
  }, [B]), Se(!!B, { ESCAPE: () => N(null) });
  const $e = e.useCallback((y, I) => {
    I.stopPropagation(), I.preventDefault(), n("openFilter", { column: y });
  }, [n]), L = s.reduce((y, I) => y + O(I), 0) + (k ? w : 0), W = m === i && i > 0, ee = m > 0 && m < i, oe = e.useCallback((y) => {
    y && (y.indeterminate = ee);
  }, [ee]);
  return /* @__PURE__ */ e.createElement(st, { active: te }, /* @__PURE__ */ e.createElement(
    Cl,
    {
      isMulti: k,
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
      onDragOver: (y) => {
        if (!x.current) return;
        y.preventDefault();
        const I = T.current, X = E.current;
        if (!I) return;
        const ne = I.getBoundingClientRect(), ae = 40, fe = 8;
        y.clientX < ne.left + ae ? I.scrollLeft = Math.max(0, I.scrollLeft - fe) : y.clientX > ne.right - ae && (I.scrollLeft += fe), X && (X.scrollLeft = I.scrollLeft);
      },
      onDrop: F
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: E }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { width: L } }, k && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (f > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: w,
          minWidth: w,
          ...f > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (y) => {
          x.current && (y.preventDefault(), y.dataTransfer.dropEffect = "move", s.length > 0 && s[0].name !== x.current && V({ column: s[0].name, side: "left" }));
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
    ), s.map((y, I) => {
      const X = O(y);
      s.length - 1;
      let ne = "tlTableView__headerCell";
      y.sortable && (ne += " tlTableView__headerCell--sortable"), R && R.column === y.name && (ne += " tlTableView__headerCell--dragOver-" + R.side);
      const ae = I < f, fe = I === f - 1;
      return ae && (ne += " tlTableView__headerCell--frozen"), fe && (ne += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: y.name,
          className: ne,
          style: {
            width: X,
            minWidth: X,
            position: ae ? "sticky" : "relative",
            ...ae ? { left: Q[I], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: y.sortable ? (se) => K(y.name, y.sortDirection, se) : void 0,
          onContextMenu: (se) => Ce(I, se),
          onDragStart: (se) => d(y.name, se),
          onDragOver: (se) => S(y.name, se),
          onDrop: F,
          onDragEnd: $
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
            onMouseDown: (se) => M(y.name, X, se)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (y) => {
          if (x.current && s.length > 0) {
            const I = s[s.length - 1];
            I.name !== x.current && (y.preventDefault(), y.dataTransfer.dropEffect = "move", V({ column: I.name, side: "right" }));
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
      /* @__PURE__ */ e.createElement("div", { style: { height: H, position: "relative", width: L } }, u.map((y) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: y.id,
          className: "tlTableView__row" + (y.selected ? " tlTableView__row--selected" : "") + (y.index === p ? " tlTableView__row--cursor" : ""),
          style: {
            position: "absolute",
            top: y.index * r,
            height: r,
            width: L,
            ...y.index === p ? { outline: "2px solid var(--color-primary, #1a73e8)", outlineOffset: "-2px" } : {}
          },
          onClick: (I) => G(y.index, I)
        },
        k && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (f > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: w,
              minWidth: w,
              ...f > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (I) => I.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: y.selected,
              onChange: () => {
              },
              onClick: (I) => pe(y.index, I),
              tabIndex: -1
            }
          )
        ),
        s.map((I, X) => {
          const ne = O(I), ae = X === s.length - 1, fe = X < f, se = X === f - 1;
          let Te = "tlTableView__cell";
          fe && (Te += " tlTableView__cell--frozen"), se && (Te += " tlTableView__cell--frozenLast");
          const Fe = g && X === 0, ie = y.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: I.name,
              className: Te,
              "data-row": y.id,
              "data-col": I.name,
              style: {
                ...ae && !fe ? { flex: "1 0 auto", minWidth: ne } : { width: ne, minWidth: ne },
                ...fe ? { position: "sticky", left: Q[X], zIndex: 2 } : {}
              }
            },
            Fe ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ie * C } }, y.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (ge) => _e(y.index, !y.expanded, ge)
              },
              y.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(z, { control: y.cells[I.name] })) : /* @__PURE__ */ e.createElement(z, { control: y.cells[I.name] })
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
      B.colIdx + 1 !== f && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: we }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      f > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Ne }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
    )
  ));
}, kl = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, It = e.createContext(kl), { useMemo: Sl, useRef: Nl, useState: Tl, useEffect: Rl } = e, Dl = 320, xl = ({ controlId: l }) => {
  const t = q(), n = t.maxColumns ?? 3, a = t.labelPosition ?? "auto", c = t.readOnly === !0, s = t.children ?? [], i = t.noModelMessage, u = Nl(null), [r, o] = Tl(
    a === "top" ? "top" : "side"
  );
  Rl(() => {
    if (a !== "auto") {
      o(a);
      return;
    }
    const h = u.current;
    if (!h) return;
    const k = new ResizeObserver((w) => {
      for (const C of w) {
        const T = C.contentRect.width / n;
        o(T < Dl ? "top" : "side");
      }
    });
    return k.observe(h), () => k.disconnect();
  }, [a, n]);
  const m = Sl(() => ({
    readOnly: c,
    resolvedLabelPosition: r
  }), [c, r]), f = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, g = [
    "tlFormLayout",
    c ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return i ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, i)) : /* @__PURE__ */ e.createElement(It.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: l, className: g, style: f, ref: u }, s.map((h, k) => /* @__PURE__ */ e.createElement(z, { key: k, control: h }))));
}, { useCallback: Ll } = e, Il = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, Pl = ({ controlId: l }) => {
  const t = q(), n = le(), a = ce(Il), c = t.headerControl ?? null, s = t.headerActions ?? [], i = t.collapsible === !0, u = t.collapsed === !0, r = t.border ?? "none", o = t.fullLine === !0, m = t.children ?? [], p = c != null || s.length > 0 || i, f = Ll(() => {
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
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(z, { control: c })), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, s.map((h, k) => /* @__PURE__ */ e.createElement(z, { key: k, control: h })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((h, k) => /* @__PURE__ */ e.createElement(z, { key: k, control: h }))));
}, { useContext: jl, useState: Ml, useCallback: Bl } = e, Al = ({ controlId: l }) => {
  const t = q(), n = jl(It), a = t.label ?? "", c = t.required === !0, s = t.error, i = t.warnings, u = t.helpText, r = t.dirty === !0, o = t.labelPosition ?? n.resolvedLabelPosition, m = t.fullLine === !0, p = t.visible !== !1, f = t.hasTooltip === !0, g = t.field, h = n.readOnly, [k, w] = Ml(!1), C = Bl(() => w((b) => !b), []);
  if (!p) return null;
  const E = s != null, T = i != null && i.length > 0, D = [
    "tlFormField",
    `tlFormField--${o}`,
    h ? "tlFormField--readonly" : "",
    m ? "tlFormField--fullLine" : "",
    E ? "tlFormField--error" : "",
    !E && T ? "tlFormField--warning" : "",
    r ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: D }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": f ? "key:tooltip" : void 0
    },
    a
  ), c && !h && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), r && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), u && !h && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(z, { control: g })), !h && E && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, s)), !h && !E && T && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, i.map((b, _) => /* @__PURE__ */ e.createElement("div", { key: _, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, b)))), !h && u && k && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, u));
}, Ol = ({ controlId: l }) => {
  const t = q(), n = le(), a = t.iconCss, c = t.iconSrc, s = t.label, i = t.cssClass, u = t.hasTooltip === !0, r = t.hasLink, o = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : c ? /* @__PURE__ */ e.createElement("img", { src: c, className: "tlTypeIcon", alt: "" }) : null, m = /* @__PURE__ */ e.createElement(e.Fragment, null, o, s && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, s)), p = e.useCallback((h) => {
    h.preventDefault(), n("goto", {});
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
}, $l = 20, Fl = () => {
  const l = q(), t = le(), n = l.nodes ?? [], a = l.selectionMode ?? "single", c = l.dragEnabled ?? !1, s = l.dropEnabled ?? !1, i = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [r, o] = e.useState(-1), m = e.useRef(null), p = e.useCallback((b, _) => {
    t(_ ? "collapse" : "expand", { nodeId: b });
  }, [t]), f = e.useCallback((b, _) => {
    t("select", {
      nodeId: b,
      ctrlKey: _.ctrlKey || _.metaKey,
      shiftKey: _.shiftKey
    });
  }, [t]), g = e.useCallback((b, _) => {
    _.preventDefault(), t("contextMenu", { nodeId: b, x: _.clientX, y: _.clientY });
  }, [t]), h = e.useRef(null), k = e.useCallback((b, _) => {
    const v = _.getBoundingClientRect(), Y = b.clientY - v.top, x = v.height / 3;
    return Y < x ? "above" : Y > x * 2 ? "below" : "within";
  }, []), w = e.useCallback((b, _) => {
    _.dataTransfer.effectAllowed = "move", _.dataTransfer.setData("text/plain", b);
  }, []), C = e.useCallback((b, _) => {
    _.preventDefault(), _.dataTransfer.dropEffect = "move";
    const v = k(_, _.currentTarget);
    h.current != null && window.clearTimeout(h.current), h.current = window.setTimeout(() => {
      t("dragOver", { nodeId: b, position: v }), h.current = null;
    }, 50);
  }, [t, k]), E = e.useCallback((b, _) => {
    _.preventDefault(), h.current != null && (window.clearTimeout(h.current), h.current = null);
    const v = k(_, _.currentTarget);
    t("drop", { nodeId: b, position: v });
  }, [t, k]), T = e.useCallback(() => {
    h.current != null && (window.clearTimeout(h.current), h.current = null), t("dragEnd");
  }, [t]), D = e.useCallback((b) => {
    if (n.length === 0) return;
    let _ = r;
    switch (b.key) {
      case "ArrowDown":
        b.preventDefault(), _ = Math.min(r + 1, n.length - 1);
        break;
      case "ArrowUp":
        b.preventDefault(), _ = Math.max(r - 1, 0);
        break;
      case "ArrowRight":
        if (b.preventDefault(), r >= 0 && r < n.length) {
          const v = n[r];
          if (v.expandable && !v.expanded) {
            t("expand", { nodeId: v.id });
            return;
          } else v.expanded && (_ = r + 1);
        }
        break;
      case "ArrowLeft":
        if (b.preventDefault(), r >= 0 && r < n.length) {
          const v = n[r];
          if (v.expanded) {
            t("collapse", { nodeId: v.id });
            return;
          } else {
            const Y = v.depth;
            for (let x = r - 1; x >= 0; x--)
              if (n[x].depth < Y) {
                _ = x;
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
        b.preventDefault(), _ = 0;
        break;
      case "End":
        b.preventDefault(), _ = n.length - 1;
        break;
      default:
        return;
    }
    _ !== r && o(_);
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
          _ === r ? "tlTreeView__node--focused" : "",
          i === b.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          i === b.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          i === b.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: b.depth * $l },
        draggable: c,
        onClick: (v) => f(b.id, v),
        onContextMenu: (v) => g(b.id, v),
        onDragStart: (v) => w(b.id, v),
        onDragOver: s ? (v) => C(b.id, v) : void 0,
        onDrop: s ? (v) => E(b.id, v) : void 0,
        onDragEnd: T
      },
      b.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (v) => {
            v.stopPropagation(), p(b.id, b.expanded);
          },
          tabIndex: -1,
          "aria-label": b.expanded ? "Collapse" : "Expand"
        },
        b.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: b.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(z, { control: b.content }))
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
function Ul() {
  if (Ct) return Z;
  Ct = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), a = Symbol.for("react.strict_mode"), c = Symbol.for("react.profiler"), s = Symbol.for("react.consumer"), i = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), r = Symbol.for("react.suspense"), o = Symbol.for("react.memo"), m = Symbol.for("react.lazy"), p = Symbol.for("react.activity"), f = Symbol.iterator;
  function g(d) {
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
  }, k = Object.assign, w = {};
  function C(d, S, F) {
    this.props = d, this.context = S, this.refs = w, this.updater = F || h;
  }
  C.prototype.isReactComponent = {}, C.prototype.setState = function(d, S) {
    if (typeof d != "object" && typeof d != "function" && d != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, d, S, "setState");
  }, C.prototype.forceUpdate = function(d) {
    this.updater.enqueueForceUpdate(this, d, "forceUpdate");
  };
  function E() {
  }
  E.prototype = C.prototype;
  function T(d, S, F) {
    this.props = d, this.context = S, this.refs = w, this.updater = F || h;
  }
  var D = T.prototype = new E();
  D.constructor = T, k(D, C.prototype), D.isPureReactComponent = !0;
  var b = Array.isArray;
  function _() {
  }
  var v = { H: null, A: null, T: null, S: null }, Y = Object.prototype.hasOwnProperty;
  function x(d, S, F) {
    var $ = F.ref;
    return {
      $$typeof: l,
      type: d,
      key: S,
      ref: $ !== void 0 ? $ : null,
      props: F
    };
  }
  function R(d, S) {
    return x(d.type, S, d.props);
  }
  function V(d) {
    return typeof d == "object" && d !== null && d.$$typeof === l;
  }
  function B(d) {
    var S = { "=": "=0", ":": "=2" };
    return "$" + d.replace(/[=:]/g, function(F) {
      return S[F];
    });
  }
  var N = /\/+/g;
  function O(d, S) {
    return typeof d == "object" && d !== null && d.key != null ? B("" + d.key) : S.toString(36);
  }
  function Q(d) {
    switch (d.status) {
      case "fulfilled":
        return d.value;
      case "rejected":
        throw d.reason;
      default:
        switch (typeof d.status == "string" ? d.then(_, _) : (d.status = "pending", d.then(
          function(S) {
            d.status === "pending" && (d.status = "fulfilled", d.value = S);
          },
          function(S) {
            d.status === "pending" && (d.status = "rejected", d.reason = S);
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
  function H(d, S, F, $, G) {
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
              return J = d._init, H(
                J(d._payload),
                S,
                F,
                $,
                G
              );
          }
      }
    if (J)
      return G = G(d), J = $ === "" ? "." + O(d, 0) : $, b(G) ? (F = "", J != null && (F = J.replace(N, "$&/") + "/"), H(G, S, F, "", function(pe) {
        return pe;
      })) : G != null && (V(G) && (G = R(
        G,
        F + (G.key == null || d && d.key === G.key ? "" : ("" + G.key).replace(
          N,
          "$&/"
        ) + "/") + J
      )), S.push(G)), 1;
    J = 0;
    var re = $ === "" ? "." : $ + ":";
    if (b(d))
      for (var te = 0; te < d.length; te++)
        $ = d[te], j = re + O($, te), J += H(
          $,
          S,
          F,
          j,
          G
        );
    else if (te = g(d), typeof te == "function")
      for (d = te.call(d), te = 0; !($ = d.next()).done; )
        $ = $.value, j = re + O($, te++), J += H(
          $,
          S,
          F,
          j,
          G
        );
    else if (j === "object") {
      if (typeof d.then == "function")
        return H(
          Q(d),
          S,
          F,
          $,
          G
        );
      throw S = String(d), Error(
        "Objects are not valid as a React child (found: " + (S === "[object Object]" ? "object with keys {" + Object.keys(d).join(", ") + "}" : S) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return J;
  }
  function A(d, S, F) {
    if (d == null) return d;
    var $ = [], G = 0;
    return H(d, $, "", "", function(j) {
      return S.call(F, j, G++);
    }), $;
  }
  function M(d) {
    if (d._status === -1) {
      var S = d._result;
      S = S(), S.then(
        function(F) {
          (d._status === 0 || d._status === -1) && (d._status = 1, d._result = F);
        },
        function(F) {
          (d._status === 0 || d._status === -1) && (d._status = 2, d._result = F);
        }
      ), d._status === -1 && (d._status = 0, d._result = S);
    }
    if (d._status === 1) return d._result.default;
    throw d._result;
  }
  var P = typeof reportError == "function" ? reportError : function(d) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var S = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof d == "object" && d !== null && typeof d.message == "string" ? String(d.message) : String(d),
        error: d
      });
      if (!window.dispatchEvent(S)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", d);
      return;
    }
    console.error(d);
  }, K = {
    map: A,
    forEach: function(d, S, F) {
      A(
        d,
        function() {
          S.apply(this, arguments);
        },
        F
      );
    },
    count: function(d) {
      var S = 0;
      return A(d, function() {
        S++;
      }), S;
    },
    toArray: function(d) {
      return A(d, function(S) {
        return S;
      }) || [];
    },
    only: function(d) {
      if (!V(d))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return d;
    }
  };
  return Z.Activity = p, Z.Children = K, Z.Component = C, Z.Fragment = n, Z.Profiler = c, Z.PureComponent = T, Z.StrictMode = a, Z.Suspense = r, Z.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = v, Z.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(d) {
      return v.H.useMemoCache(d);
    }
  }, Z.cache = function(d) {
    return function() {
      return d.apply(null, arguments);
    };
  }, Z.cacheSignal = function() {
    return null;
  }, Z.cloneElement = function(d, S, F) {
    if (d == null)
      throw Error(
        "The argument must be a React element, but you passed " + d + "."
      );
    var $ = k({}, d.props), G = d.key;
    if (S != null)
      for (j in S.key !== void 0 && (G = "" + S.key), S)
        !Y.call(S, j) || j === "key" || j === "__self" || j === "__source" || j === "ref" && S.ref === void 0 || ($[j] = S[j]);
    var j = arguments.length - 2;
    if (j === 1) $.children = F;
    else if (1 < j) {
      for (var J = Array(j), re = 0; re < j; re++)
        J[re] = arguments[re + 2];
      $.children = J;
    }
    return x(d.type, G, $);
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
  }, Z.createElement = function(d, S, F) {
    var $, G = {}, j = null;
    if (S != null)
      for ($ in S.key !== void 0 && (j = "" + S.key), S)
        Y.call(S, $) && $ !== "key" && $ !== "__self" && $ !== "__source" && (G[$] = S[$]);
    var J = arguments.length - 2;
    if (J === 1) G.children = F;
    else if (1 < J) {
      for (var re = Array(J), te = 0; te < J; te++)
        re[te] = arguments[te + 2];
      G.children = re;
    }
    if (d && d.defaultProps)
      for ($ in J = d.defaultProps, J)
        G[$] === void 0 && (G[$] = J[$]);
    return x(d, j, G);
  }, Z.createRef = function() {
    return { current: null };
  }, Z.forwardRef = function(d) {
    return { $$typeof: u, render: d };
  }, Z.isValidElement = V, Z.lazy = function(d) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: d },
      _init: M
    };
  }, Z.memo = function(d, S) {
    return {
      $$typeof: o,
      type: d,
      compare: S === void 0 ? null : S
    };
  }, Z.startTransition = function(d) {
    var S = v.T, F = {};
    v.T = F;
    try {
      var $ = d(), G = v.S;
      G !== null && G(F, $), typeof $ == "object" && $ !== null && typeof $.then == "function" && $.then(_, P);
    } catch (j) {
      P(j);
    } finally {
      S !== null && F.types !== null && (S.types = F.types), v.T = S;
    }
  }, Z.unstable_useCacheRefresh = function() {
    return v.H.useCacheRefresh();
  }, Z.use = function(d) {
    return v.H.use(d);
  }, Z.useActionState = function(d, S, F) {
    return v.H.useActionState(d, S, F);
  }, Z.useCallback = function(d, S) {
    return v.H.useCallback(d, S);
  }, Z.useContext = function(d) {
    return v.H.useContext(d);
  }, Z.useDebugValue = function() {
  }, Z.useDeferredValue = function(d, S) {
    return v.H.useDeferredValue(d, S);
  }, Z.useEffect = function(d, S) {
    return v.H.useEffect(d, S);
  }, Z.useEffectEvent = function(d) {
    return v.H.useEffectEvent(d);
  }, Z.useId = function() {
    return v.H.useId();
  }, Z.useImperativeHandle = function(d, S, F) {
    return v.H.useImperativeHandle(d, S, F);
  }, Z.useInsertionEffect = function(d, S) {
    return v.H.useInsertionEffect(d, S);
  }, Z.useLayoutEffect = function(d, S) {
    return v.H.useLayoutEffect(d, S);
  }, Z.useMemo = function(d, S) {
    return v.H.useMemo(d, S);
  }, Z.useOptimistic = function(d, S) {
    return v.H.useOptimistic(d, S);
  }, Z.useReducer = function(d, S, F) {
    return v.H.useReducer(d, S, F);
  }, Z.useRef = function(d) {
    return v.H.useRef(d);
  }, Z.useState = function(d) {
    return v.H.useState(d);
  }, Z.useSyncExternalStore = function(d, S, F) {
    return v.H.useSyncExternalStore(
      d,
      S,
      F
    );
  }, Z.useTransition = function() {
    return v.H.useTransition();
  }, Z.version = "19.2.4", Z;
}
var wt;
function Hl() {
  return wt || (wt = 1, Qe.exports = Ul()), Qe.exports;
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
function Wl() {
  if (yt) return de;
  yt = 1;
  var l = Hl();
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
var kt;
function zl() {
  if (kt) return Ze.exports;
  kt = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), Ze.exports = Wl(), Ze.exports;
}
var Pt = zl();
const { useState: ye, useCallback: me, useRef: Me, useEffect: xe, useMemo: at } = e;
function it({ image: l }) {
  if (!l) return null;
  if (l.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function Vl({
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
    /* @__PURE__ */ e.createElement(it, { image: l.image }),
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
function Kl({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: a,
  onMouseEnter: c,
  id: s
}) {
  const i = me(() => a(l.value), [a, l.value]), u = at(() => {
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
    /* @__PURE__ */ e.createElement(it, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const Yl = ({ controlId: l, state: t }) => {
  const n = le(), a = t.value ?? [], c = t.multiSelect === !0, s = t.customOrder === !0, i = t.mandatory === !0, u = t.disabled === !0, r = t.editable !== !1, o = t.optionsLoaded === !0, m = t.options ?? [], p = t.emptyOptionLabel ?? "", f = s && c && !u && r, g = ce({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), h = g["js.dropdownSelect.nothingFound"], k = me(
    (L) => g["js.dropdownSelect.removeChip"].replace("{0}", L),
    [g]
  ), [w, C] = ye(!1), [E, T] = ye(""), [D, b] = ye(-1), [_, v] = ye(!1), [Y, x] = ye({}), [R, V] = ye(null), [B, N] = ye(null), [O, Q] = ye(null), H = Me(null), A = Me(null), M = Me(null), P = Me(a);
  P.current = a;
  const K = Me(-1), d = at(
    () => new Set(a.map((L) => L.value)),
    [a]
  ), S = at(() => {
    let L = m.filter((W) => !d.has(W.value));
    if (E) {
      const W = E.toLowerCase();
      L = L.filter((ee) => ee.label.toLowerCase().includes(W));
    }
    return L;
  }, [m, d, E]);
  xe(() => {
    E && S.length === 1 ? b(0) : b(-1);
  }, [S.length, E]), xe(() => {
    w && o && A.current && A.current.focus();
  }, [w, o, a]), xe(() => {
    var ee, oe;
    if (K.current < 0) return;
    const L = K.current;
    K.current = -1;
    const W = (ee = H.current) == null ? void 0 : ee.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    W && W.length > 0 ? W[Math.min(L, W.length - 1)].focus() : (oe = H.current) == null || oe.focus();
  }, [a]), xe(() => {
    if (!w) return;
    const L = (W) => {
      H.current && !H.current.contains(W.target) && M.current && !M.current.contains(W.target) && (C(!1), T(""));
    };
    return document.addEventListener("mousedown", L), () => document.removeEventListener("mousedown", L);
  }, [w]), xe(() => {
    if (!w || !H.current) return;
    const L = H.current.getBoundingClientRect(), W = window.innerHeight - L.bottom, oe = W < 300 && L.top > W;
    x({
      left: L.left,
      width: L.width,
      ...oe ? { bottom: window.innerHeight - L.top } : { top: L.bottom }
    });
  }, [w]);
  const F = me(async () => {
    if (!(u || !r) && (C(!0), T(""), b(-1), v(!1), !o))
      try {
        await n("loadOptions");
      } catch {
        v(!0);
      }
  }, [u, r, o, n]), $ = me(() => {
    var L;
    C(!1), T(""), b(-1), (L = H.current) == null || L.focus();
  }, []), G = me(
    (L) => {
      let W;
      if (c) {
        const ee = m.find((oe) => oe.value === L);
        if (ee)
          W = [...P.current, ee];
        else
          return;
      } else {
        const ee = m.find((oe) => oe.value === L);
        if (ee)
          W = [ee];
        else
          return;
      }
      P.current = W, n("valueChanged", { value: W.map((ee) => ee.value) }), c ? (T(""), b(-1)) : $();
    },
    [c, m, n, $]
  ), j = me(
    (L) => {
      K.current = P.current.findIndex((ee) => ee.value === L);
      const W = P.current.filter((ee) => ee.value !== L);
      P.current = W, n("valueChanged", { value: W.map((ee) => ee.value) });
    },
    [n]
  ), J = me(
    (L) => {
      L.stopPropagation(), n("valueChanged", { value: [] }), $();
    },
    [n, $]
  ), re = me((L) => {
    T(L.target.value);
  }, []), te = me(
    (L) => {
      if (!w) {
        if (L.key === "ArrowDown" || L.key === "ArrowUp" || L.key === "Enter" || L.key === " ") {
          if (L.target.tagName === "BUTTON") return;
          L.preventDefault(), L.stopPropagation(), F();
        }
        return;
      }
      switch (L.key) {
        case "ArrowDown":
          L.preventDefault(), L.stopPropagation(), b(
            (W) => W < S.length - 1 ? W + 1 : 0
          );
          break;
        case "ArrowUp":
          L.preventDefault(), L.stopPropagation(), b(
            (W) => W > 0 ? W - 1 : S.length - 1
          );
          break;
        case "Enter":
          L.preventDefault(), L.stopPropagation(), D >= 0 && D < S.length && G(S[D].value);
          break;
        case "Escape":
          L.preventDefault(), L.stopPropagation(), $();
          break;
        case "Tab":
          $();
          break;
        case "Backspace":
          E === "" && c && a.length > 0 && j(a[a.length - 1].value);
          break;
      }
    },
    [
      w,
      F,
      $,
      S,
      D,
      G,
      E,
      c,
      a,
      j
    ]
  ), pe = me(
    async (L) => {
      L.preventDefault(), v(!1);
      try {
        await n("loadOptions");
      } catch {
        v(!0);
      }
    },
    [n]
  ), be = me(
    (L, W) => {
      V(L), W.dataTransfer.effectAllowed = "move", W.dataTransfer.setData("text/plain", String(L));
    },
    []
  ), _e = me(
    (L, W) => {
      if (W.preventDefault(), W.dataTransfer.dropEffect = "move", R === null || R === L) {
        N(null), Q(null);
        return;
      }
      const ee = W.currentTarget.getBoundingClientRect(), oe = ee.left + ee.width / 2, y = W.clientX < oe ? "before" : "after";
      N(L), Q(y);
    },
    [R]
  ), Ce = me(
    (L) => {
      if (L.preventDefault(), R === null || B === null || O === null || R === B) return;
      const W = [...P.current], [ee] = W.splice(R, 1);
      let oe = B;
      R < B ? oe = O === "before" ? oe - 1 : oe : oe = O === "before" ? oe : oe + 1, W.splice(oe, 0, ee), P.current = W, n("valueChanged", { value: W.map((y) => y.value) }), V(null), N(null), Q(null);
    },
    [R, B, O, n]
  ), we = me(() => {
    V(null), N(null), Q(null);
  }, []);
  if (xe(() => {
    if (D < 0 || !M.current) return;
    const L = M.current.querySelector(
      `[id="${l}-opt-${D}"]`
    );
    L && L.scrollIntoView({ block: "nearest" });
  }, [D, l]), !r)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, a.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, p) : a.map((L) => /* @__PURE__ */ e.createElement("span", { key: L.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(it, { image: L.image }), /* @__PURE__ */ e.createElement("span", null, L.label))));
  const Ne = !i && a.length > 0 && !u, $e = w ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: M,
      className: "tlDropdownSelect__dropdown",
      style: Y
    },
    (o || _) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: A,
        type: "text",
        className: "tlDropdownSelect__search",
        value: E,
        onChange: re,
        onKeyDown: te,
        placeholder: g["js.dropdownSelect.filterPlaceholder"],
        "aria-label": g["js.dropdownSelect.filterPlaceholder"],
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
      !o && !_ && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      _ && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: pe }, g["js.dropdownSelect.error"])),
      o && S.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, h),
      o && S.map((L, W) => /* @__PURE__ */ e.createElement(
        Kl,
        {
          key: L.value,
          id: `${l}-opt-${W}`,
          option: L,
          highlighted: W === D,
          searchTerm: E,
          onSelect: G,
          onMouseEnter: () => b(W)
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
      onKeyDown: te
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, a.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : a.map((L, W) => {
      let ee = "";
      return R === W ? ee = "tlDropdownSelect__chip--dragging" : B === W && O === "before" ? ee = "tlDropdownSelect__chip--dropBefore" : B === W && O === "after" && (ee = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        Vl,
        {
          key: L.value,
          option: L,
          removable: !u && (c || !i),
          onRemove: j,
          removeLabel: k(L.label),
          draggable: f,
          onDragStart: f ? (oe) => be(W, oe) : void 0,
          onDragOver: f ? (oe) => _e(W, oe) : void 0,
          onDrop: f ? Ce : void 0,
          onDragEnd: f ? we : void 0,
          dragClassName: f ? ee : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, Ne && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: J,
        "aria-label": g["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, w ? "▲" : "▼"))
  ), $e && Pt.createPortal($e, document.body));
}, { useCallback: Je, useRef: Gl } = e, jt = "application/x-tl-color", Xl = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: a,
  onSwap: c,
  onReplace: s
}) => {
  const i = Gl(null), u = Je(
    (m) => (p) => {
      i.current = m, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), r = Je((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), o = Je(
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
function Mt(l) {
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
  const a = (c) => Mt(c).toString(16).padStart(2, "0");
  return "#" + a(l) + a(t) + a(n);
}
function ql(l, t, n) {
  const a = l / 255, c = t / 255, s = n / 255, i = Math.max(a, c, s), u = Math.min(a, c, s), r = i - u;
  let o = 0;
  r !== 0 && (i === a ? o = (c - s) / r % 6 : i === c ? o = (s - a) / r + 2 : o = (a - c) / r + 4, o *= 60, o < 0 && (o += 360));
  const m = i === 0 ? 0 : r / i;
  return [o, m, i];
}
function Zl(l, t, n) {
  const a = n * t, c = a * (1 - Math.abs(l / 60 % 2 - 1)), s = n - a;
  let i = 0, u = 0, r = 0;
  return l < 60 ? (i = a, u = c, r = 0) : l < 120 ? (i = c, u = a, r = 0) : l < 180 ? (i = 0, u = a, r = c) : l < 240 ? (i = 0, u = c, r = a) : l < 300 ? (i = c, u = 0, r = a) : (i = a, u = 0, r = c), [
    Math.round((i + s) * 255),
    Math.round((u + s) * 255),
    Math.round((r + s) * 255)
  ];
}
function Ql(l) {
  return ql(...Bt(l));
}
function et(l, t, n) {
  return At(...Zl(l, t, n));
}
const { useCallback: Le, useRef: St } = e, Jl = ({ color: l, onColorChange: t }) => {
  const [n, a, c] = Ql(l), s = St(null), i = St(null), u = Le(
    (h, k) => {
      var T;
      const w = (T = s.current) == null ? void 0 : T.getBoundingClientRect();
      if (!w) return;
      const C = Math.max(0, Math.min(1, (h - w.left) / w.width)), E = Math.max(0, Math.min(1, 1 - (k - w.top) / w.height));
      t(et(n, C, E));
    },
    [n, t]
  ), r = Le(
    (h) => {
      h.preventDefault(), h.target.setPointerCapture(h.pointerId), u(h.clientX, h.clientY);
    },
    [u]
  ), o = Le(
    (h) => {
      h.buttons !== 0 && u(h.clientX, h.clientY);
    },
    [u]
  ), m = Le(
    (h) => {
      var E;
      const k = (E = i.current) == null ? void 0 : E.getBoundingClientRect();
      if (!k) return;
      const C = Math.max(0, Math.min(1, (h - k.top) / k.height)) * 360;
      t(et(C, a, c));
    },
    [a, c, t]
  ), p = Le(
    (h) => {
      h.preventDefault(), h.target.setPointerCapture(h.pointerId), m(h.clientY);
    },
    [m]
  ), f = Le(
    (h) => {
      h.buttons !== 0 && m(h.clientY);
    },
    [m]
  ), g = et(n, 1, 1);
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
function er(l, t) {
  const n = t.toUpperCase();
  return l.some((a) => a != null && a.toUpperCase() === n);
}
const tr = {
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
}, { useState: We, useCallback: ve, useEffect: Nt, useRef: nr, useLayoutEffect: lr } = e, rr = ({
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
  const [o, m] = We("palette"), [p, f] = We(t), g = nr(null), h = ce(tr), [k, w] = We(null);
  lr(() => {
    if (!l.current || !g.current) return;
    const M = l.current.getBoundingClientRect(), P = g.current.getBoundingClientRect();
    let K = M.bottom + 4, d = M.left;
    K + P.height > window.innerHeight && (K = M.top - P.height - 4), d + P.width > window.innerWidth && (d = Math.max(0, M.right - P.width)), w({ top: K, left: d });
  }, [l]);
  const C = p != null, [E, T, D] = C ? Bt(p) : [0, 0, 0], [b, _] = We((p == null ? void 0 : p.toUpperCase()) ?? "");
  Nt(() => {
    _((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), Se(!0, { ESCAPE: u }), Nt(() => {
    const M = (K) => {
      g.current && !g.current.contains(K.target) && u();
    }, P = setTimeout(() => document.addEventListener("mousedown", M), 0);
    return () => {
      clearTimeout(P), document.removeEventListener("mousedown", M);
    };
  }, [u]);
  const v = ve(
    (M) => (P) => {
      const K = parseInt(P.target.value, 10);
      if (isNaN(K)) return;
      const d = Mt(K);
      f(At(M === "r" ? d : E, M === "g" ? d : T, M === "b" ? d : D));
    },
    [E, T, D]
  ), Y = ve(
    (M) => {
      if (p != null) {
        M.dataTransfer.setData(jt, p.toUpperCase()), M.dataTransfer.effectAllowed = "move";
        const P = document.createElement("div");
        P.style.width = "33px", P.style.height = "33px", P.style.backgroundColor = p, P.style.borderRadius = "3px", P.style.border = "1px solid rgba(0,0,0,0.1)", P.style.position = "absolute", P.style.top = "-9999px", document.body.appendChild(P), M.dataTransfer.setDragImage(P, 16, 16), requestAnimationFrame(() => document.body.removeChild(P));
      }
    },
    [p]
  ), x = ve((M) => {
    const P = M.target.value;
    _(P), ot(P) && f(P);
  }, []), R = ve(() => {
    f(null);
  }, []), V = ve((M) => {
    f(M);
  }, []), B = ve(
    (M) => {
      i(M);
    },
    [i]
  ), N = ve(
    (M, P) => {
      const K = [...n], d = K[M];
      K[M] = K[P], K[P] = d, r(K);
    },
    [n, r]
  ), O = ve(
    (M, P) => {
      const K = [...n];
      K[M] = P, r(K);
    },
    [n, r]
  ), Q = ve(() => {
    r([...c]);
  }, [c, r]), H = ve(
    (M) => {
      if (er(n, M)) return;
      const P = n.indexOf(null);
      if (P < 0) return;
      const K = [...n];
      K[P] = M.toUpperCase(), r(K);
    },
    [n, r]
  ), A = ve(() => {
    p != null && H(p), i(p);
  }, [p, i, H]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: g,
      style: k ? { top: k.top, left: k.left, visibility: "visible" } : { visibility: "hidden" }
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
      Xl,
      {
        colors: n,
        columns: a,
        onSelect: V,
        onConfirm: B,
        onSwap: N,
        onReplace: O
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: Q }, h["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(Jl, { color: p ?? "#000000", onColorChange: f }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, h["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
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
        onDragStart: C ? Y : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: C ? E : "",
        onChange: v("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: C ? T : "",
        onChange: v("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: C ? D : "",
        onChange: v("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (b !== "" && !ot(b) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: b,
        onChange: x
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, s && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: R }, h["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, h["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: A }, h["js.colorInput.ok"]))
  );
}, ar = { "js.colorInput.chooseColor": "Choose color" }, { useState: or, useCallback: ze, useRef: sr } = e, cr = ({ controlId: l, state: t }) => {
  const n = le(), a = ce(ar), [c, s] = or(!1), i = sr(null), u = t.value, r = t.editable !== !1, o = t.palette ?? [], m = t.paletteColumns ?? 6, p = t.defaultPalette ?? o, f = ze(() => {
    r && s(!0);
  }, [r]), g = ze(
    (w) => {
      s(!1), n("valueChanged", { value: w });
    },
    [n]
  ), h = ze(() => {
    s(!1);
  }, []), k = ze(
    (w) => {
      n("paletteChanged", { palette: w });
    },
    [n]
  );
  return r ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: i,
      className: "tlColorInput__swatch" + (u == null ? " tlColorInput__swatch--noColor" : ""),
      style: u != null ? { backgroundColor: u } : void 0,
      onClick: f,
      disabled: t.disabled === !0,
      title: u ?? "",
      "aria-label": a["js.colorInput.chooseColor"]
    }
  ), c && /* @__PURE__ */ e.createElement(
    rr,
    {
      anchorRef: i,
      currentColor: u,
      palette: o,
      paletteColumns: m,
      defaultPalette: p,
      canReset: t.canReset !== !1,
      onConfirm: g,
      onCancel: h,
      onPaletteChange: k
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
}, { useState: Be, useCallback: ke, useEffect: tt, useRef: Tt, useLayoutEffect: ir, useMemo: ur } = e, dr = {
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
}, mr = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: a,
  onSelect: c,
  onCancel: s,
  onLoadIcons: i
}) => {
  const u = ce(dr), [r, o] = Be("simple"), [m, p] = Be(""), [f, g] = Be(t ?? ""), [h, k] = Be(!1), [w, C] = Be(null), E = Tt(null), T = Tt(null);
  ir(() => {
    if (!l.current || !E.current) return;
    const B = l.current.getBoundingClientRect(), N = E.current.getBoundingClientRect();
    let O = B.bottom + 4, Q = B.left;
    O + N.height > window.innerHeight && (O = B.top - N.height - 4), Q + N.width > window.innerWidth && (Q = Math.max(0, B.right - N.width)), C({ top: O, left: Q });
  }, [l]), tt(() => {
    !a && !h && i().catch(() => k(!0));
  }, [a, h, i]), tt(() => {
    a && T.current && T.current.focus();
  }, [a]), Se(!0, { ESCAPE: s }), tt(() => {
    const B = (O) => {
      E.current && !E.current.contains(O.target) && s();
    }, N = setTimeout(() => document.addEventListener("mousedown", B), 0);
    return () => {
      clearTimeout(N), document.removeEventListener("mousedown", B);
    };
  }, [s]);
  const D = ur(() => {
    if (!m) return n;
    const B = m.toLowerCase();
    return n.filter(
      (N) => N.prefix.toLowerCase().includes(B) || N.label.toLowerCase().includes(B) || N.terms != null && N.terms.some((O) => O.includes(B))
    );
  }, [n, m]), b = ke((B) => {
    p(B.target.value);
  }, []), _ = ke(
    (B) => {
      c(B);
    },
    [c]
  ), v = ke((B) => {
    g(B);
  }, []), Y = ke((B) => {
    g(B.target.value);
  }, []), x = ke(() => {
    c(f || null);
  }, [f, c]), R = ke(() => {
    c(null);
  }, [c]), V = ke(async (B) => {
    B.preventDefault(), k(!1);
    try {
      await i();
    } catch {
      k(!0);
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
        ref: T,
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
      !a && !h && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      h && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: V }, u["js.iconSelect.loadError"])),
      a && D.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      a && D.map(
        (B) => B.variants.map((N) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: N.encoded,
            className: "tlIconSelect__iconCell" + (N.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": N.encoded === t,
            tabIndex: 0,
            title: B.label,
            onClick: () => r === "simple" ? _(N.encoded) : v(N.encoded),
            onKeyDown: (O) => {
              (O.key === "Enter" || O.key === " ") && (O.preventDefault(), r === "simple" ? _(N.encoded) : v(N.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(Ee, { encoded: N.encoded })
        ))
      )
    ),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: f,
        onChange: Y
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, f && /* @__PURE__ */ e.createElement(Ee, { encoded: f })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, f ? f.startsWith("css:") ? f.substring(4) : f : ""))),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: s }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: R }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: x }, u["js.iconSelect.ok"]))
  );
}, pr = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: fr, useCallback: Ve, useRef: hr } = e, br = ({ controlId: l, state: t }) => {
  const n = le(), a = ce(pr), [c, s] = fr(!1), i = hr(null), u = t.value, r = t.editable !== !1, o = t.disabled === !0, m = t.icons ?? [], p = t.iconsLoaded === !0, f = Ve(() => {
    r && !o && s(!0);
  }, [r, o]), g = Ve(
    (w) => {
      s(!1), n("valueChanged", { value: w });
    },
    [n]
  ), h = Ve(() => {
    s(!1);
  }, []), k = Ve(async () => {
    await n("loadIcons");
  }, [n]);
  return r ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: i,
      className: "tlIconSelect__swatch" + (u == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: f,
      disabled: o,
      title: u ?? "",
      "aria-label": a["js.iconSelect.chooseIcon"]
    },
    u ? /* @__PURE__ */ e.createElement(Ee, { encoded: u }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), c && /* @__PURE__ */ e.createElement(
    mr,
    {
      anchorRef: i,
      currentValue: u,
      icons: m,
      iconsLoaded: p,
      onSelect: g,
      onCancel: h,
      onLoadIcons: k
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, u ? /* @__PURE__ */ e.createElement(Ee, { encoded: u }) : null));
}, { useCallback: Ie, useEffect: _r, useMemo: Rt, useRef: gr, useState: nt } = e, vr = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, Er = [1, 2, 3, 4];
function Cr(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const a = parseFloat(n[1]), c = n[2] || "px";
  return c === "rem" || c === "em" ? a * t : a;
}
function wr(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let a = 1;
  for (const c of Er)
    n >= c && (a = c);
  return a;
}
function yr(l, t) {
  const n = vr[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function kr(l, t) {
  const n = Math.max(1, t), a = {}, c = (p, f) => !!(a[p] && a[p][f]), s = (p, f) => {
    a[p] || (a[p] = {}), a[p][f] = !0;
  }, i = [];
  let u = 0, r = 0;
  const o = (p) => {
    let f = null;
    for (const h of i) h.rowStart === p && (f = h);
    if (!f) return;
    let g = f.colEnd;
    for (; g < n && !c(p, g); ) g++;
    if (g !== f.colEnd) {
      for (let h = f.rowStart; h < f.rowEnd; h++)
        for (let k = f.colEnd; k < g; k++) s(h, k);
      f.colEnd = g;
    }
  };
  for (const p of l) {
    const f = n <= 1 ? 1 : Math.max(1, p.rowSpan || 1);
    let g = Math.min(yr(p.width, n), n);
    for (; c(u, r); )
      r++, r >= n && (r = 0, u++);
    let h = 0;
    for (let T = r; T < n && !c(u, T); T++)
      h++;
    if (g > h) {
      for (o(u), r = 0, u++; c(u, r); )
        r++, r >= n && (r = 0, u++);
      h = 0;
      for (let T = r; T < n && !c(u, T); T++)
        h++;
      g = Math.min(g, h);
    }
    const k = r, w = r + g, C = u, E = u + f;
    i.push({ id: p.id, colStart: k, colEnd: w, rowStart: C, rowEnd: E });
    for (let T = C; T < E; T++)
      for (let D = k; D < w; D++) s(T, D);
    r = w, r >= n && (r = 0, u++);
  }
  o(u);
  let m = 0;
  for (const p of i) p.rowEnd > m && (m = p.rowEnd);
  for (let p = 1; p < m; p++)
    for (let f = 0; f < n; f++) {
      if (c(p, f)) continue;
      const g = i.find((h) => h.rowEnd === p && h.colStart <= f && f < h.colEnd);
      if (g) {
        g.rowEnd = p + 1;
        for (let h = g.colStart; h < g.colEnd; h++) s(p, h);
      }
    }
  return i;
}
const Sr = ({ controlId: l }) => {
  const t = q(), n = le(), a = t.minColWidth ?? "16rem", c = (t.children ?? []).filter((_) => _ && _.id), s = gr(null), [i, u] = nt(1), r = t.editMode === !0;
  _r(() => {
    const _ = s.current;
    if (!_) return;
    const v = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, Y = Cr(a, v), x = () => u(wr(_.clientWidth, Y));
    x();
    const R = new ResizeObserver(x);
    return R.observe(_), () => R.disconnect();
  }, [a]);
  const o = Rt(() => kr(c, i), [c, i]), m = Rt(() => {
    const _ = {};
    for (const v of o) _[v.id] = v;
    return _;
  }, [o]), [p, f] = nt(null), [g, h] = nt(null), k = Ie((_, v) => {
    if (!r) {
      _.preventDefault();
      return;
    }
    f(v), _.dataTransfer.effectAllowed = "move", _.dataTransfer.setData("text/plain", v);
  }, [r]), w = Ie((_, v) => {
    if (!r || !p || p === v) return;
    _.preventDefault(), _.dataTransfer.dropEffect = "move";
    const Y = _.currentTarget.getBoundingClientRect(), x = _.clientX < Y.left + Y.width / 2;
    h((R) => R && R.id === v && R.before === x ? R : { id: v, before: x });
  }, [r, p]), C = Ie(() => {
  }, []), E = Ie((_, v, Y) => {
    const x = c.map((N) => N.id), R = x.indexOf(_);
    if (R < 0) return;
    x.splice(R, 1);
    const V = x.indexOf(v);
    if (V < 0) {
      x.splice(R, 0, _);
      return;
    }
    const B = Y ? V : V + 1;
    x.splice(B, 0, _), n("reorder", { order: x });
  }, [c, n]), T = Ie((_, v) => {
    if (!r || !p || p === v) return;
    _.preventDefault();
    const Y = _.currentTarget.getBoundingClientRect(), x = _.clientX < Y.left + Y.width / 2;
    E(p, v, x), f(null), h(null);
  }, [r, p, E]), D = Ie(() => {
    f(null), h(null);
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: b }, c.map((_) => {
      const v = m[_.id];
      if (!v) return null;
      const Y = {
        gridColumn: `${v.colStart + 1} / ${v.colEnd + 1}`,
        gridRow: `${v.rowStart + 1} / ${v.rowEnd + 1}`
      }, x = ["tlDashboard__tile"];
      return p === _.id && x.push("tlDashboard__tile--dragging"), g && g.id === _.id && x.push(g.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: _.id,
          className: x.join(" "),
          style: Y,
          draggable: r,
          onDragStart: (R) => k(R, _.id),
          onDragOver: (R) => w(R, _.id),
          onDragLeave: C,
          onDrop: (R) => T(R, _.id),
          onDragEnd: D
        },
        /* @__PURE__ */ e.createElement(z, { control: _.control }),
        r && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: Nr, useRef: Dt, useState: xt, useEffect: Tr, useLayoutEffect: Rr } = e, Dr = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, a) => /* @__PURE__ */ e.createElement("span", { key: a, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(z, { control: n }))));
}, xr = ({ group: l }) => {
  var p, f;
  const [t, n] = xt(!1), [a, c] = xt({}), s = Dt(null), i = Dt(null), u = Nr(() => {
    n((g) => !g);
  }, []);
  Rr(() => {
    if (!t) return;
    const g = () => {
      const h = s.current;
      if (!h) return;
      const k = h.getBoundingClientRect();
      c({
        position: "fixed",
        top: k.bottom + 4,
        right: Math.max(8, window.innerWidth - k.right),
        left: "auto"
      });
    };
    return g(), window.addEventListener("resize", g), window.addEventListener("scroll", g, !0), () => {
      window.removeEventListener("resize", g), window.removeEventListener("scroll", g, !0);
    };
  }, [t]), Tr(() => {
    if (!t) return;
    const g = (h) => {
      i.current && !i.current.contains(h.target) && s.current && !s.current.contains(h.target) && n(!1);
    };
    return document.addEventListener("mousedown", g), () => document.removeEventListener("mousedown", g);
  }, [t]), Se(t, { ESCAPE: () => n(!1) }), ct(t, i, "first");
  const r = l.items.filter((g) => g != null);
  if (r.length === 0) return null;
  if (r.length === 1 && !((p = l.subGroups) != null && p.length) && !l.icon)
    return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(z, { control: r[0] })));
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
    m ? /* @__PURE__ */ e.createElement(Ee, { encoded: l.icon, className: "tlToolbar__menuIcon" }) : /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement("span", null, o), /* @__PURE__ */ e.createElement("svg", { className: "tlToolbar__chevron", viewBox: "0 0 24 24", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("polyline", { points: "6,9 12,15 18,9" })))
  ), Pt.createPortal(
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
      r.map((g, h) => /* @__PURE__ */ e.createElement("div", { key: h, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(z, { control: g }))),
      (f = l.subGroups) == null ? void 0 : f.map((g, h) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${h}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), g.items.map((k, w) => /* @__PURE__ */ e.createElement("div", { key: w, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(z, { control: k })))))
    ),
    document.body
  ));
}, Lr = ({ controlId: l }) => {
  const a = (q().groups ?? []).filter((c) => c.items.some((s) => s != null));
  return a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, a.map((c, s) => /* @__PURE__ */ e.createElement(e.Fragment, { key: c.name }, s > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), c.display === "menu" ? /* @__PURE__ */ e.createElement(xr, { group: c }) : /* @__PURE__ */ e.createElement(Dr, { group: c }))));
}, Ir = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlTileStack", style: { width: "100%", height: "100%" } }, t.frame && /* @__PURE__ */ e.createElement(z, { control: t.frame }));
}, Pr = ({ controlId: l }) => {
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
  })), /* @__PURE__ */ e.createElement("div", { className: "tlAdaptiveDetail__content" }, a && /* @__PURE__ */ e.createElement(z, { control: a })));
}, jr = ({ controlId: l }) => {
  const n = q().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((a, c) => /* @__PURE__ */ e.createElement(z, { key: c, control: a })));
}, Mr = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), Br = {
  "js.sidebar.openDrawer": "Open navigation"
}, Ar = ({ controlId: l }) => {
  const t = le(), n = ce(Br);
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
U("TLButton", Jt);
U("TLUploadButton", en);
U("TLToggleButton", nn);
U("TLTextInput", Ut);
U("TLPasswordInput", Wt);
U("TLNumberInput", Vt);
U("TLDatePicker", Yt);
U("TLSelect", Xt);
U("TLCheckbox", Zt);
U("TLCounter", ln);
U("TLTabBar", an);
U("TLFieldList", on);
U("TLAudioRecorder", cn);
U("TLAudioPlayer", dn);
U("TLFileUpload", pn);
U("TLBinaryField", hn);
U("TLDownload", _n);
U("TLPhotoCapture", vn);
U("TLPhotoViewer", Cn);
U("TLPdfViewer", yn);
U("TLSplitPanel", kn);
U("TLPanel", Ln);
U("TLInset", Wn);
U("TLMaximizeRoot", In);
U("TLDeckPane", Pn);
U("TLSidebar", Un);
U("TLStack", Hn);
U("TLGrid", zn);
U("TLCard", Vn);
U("TLAppBar", Kn);
U("TLBreadcrumb", Gn);
U("TLBottomBar", qn);
U("TLDialog", Jn);
U("TLDialogManager", nl);
U("TLWindow", ol);
U("TLDrawer", il);
U("TLContextMenuRegion", dl);
U("TLSnackbar", hl);
U("TLMenu", _l);
U("TLAppShell", vl);
U("TLText", El);
U("TLTableView", yl);
U("TLFormLayout", xl);
U("TLFormGroup", Pl);
U("TLFormField", Al);
U("TLResourceCell", Ol);
U("TLTreeView", Fl);
U("TLDropdownSelect", Yl);
U("TLColorInput", cr);
U("TLIconSelect", br);
U("TLDashboard", Sr);
U("TLToolbar", Lr);
U("TLTileStack", Ir);
U("TLAdaptiveDetail", Pr);
U("TLSlot", jr);
U("TLSlotContent", Mr);
U("TLDrawerToggle", Ar);
