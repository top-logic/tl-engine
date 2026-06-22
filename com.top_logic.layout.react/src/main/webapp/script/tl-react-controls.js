import { React as e, useTLFieldValue as je, useTLState as q, useTLCommand as le, useKeyboardBinding as ue, useTLUpload as Be, TLChild as z, useI18N as ce, useTLDataUrl as Oe, useStandaloneKeyboardScope as Se, KeyboardScopeProvider as st, useFocusTrap as ct, register as H } from "tl-react-bridge";
const { useCallback: At } = e, Bt = ({ controlId: l, state: t }) => {
  const [n, r] = je(), i = At(
    (o) => {
      r(o.target.value);
    },
    [r]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, n ?? "");
  const s = t.hasError === !0, c = t.hasWarnings === !0, u = t.errorMessage, a = [
    "tlReactTextInput",
    s ? "tlReactTextInput--error" : "",
    !s && c ? "tlReactTextInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: n ?? "",
      placeholder: t.placeholder ?? void 0,
      onChange: i,
      disabled: t.disabled === !0,
      className: a,
      "aria-invalid": s || void 0,
      title: s && u ? u : void 0
    }
  ));
}, { useCallback: Ot } = e, $t = ({ controlId: l, state: t }) => {
  const [n, r] = je(), i = Ot(
    (o) => {
      r(o.target.value);
    },
    [r]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, "••••••••");
  const s = t.hasError === !0, c = t.hasWarnings === !0, u = t.errorMessage, a = [
    "tlReactTextInput",
    s ? "tlReactTextInput--error" : "",
    !s && c ? "tlReactTextInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "password",
      value: n ?? "",
      onChange: i,
      disabled: t.disabled === !0,
      className: a,
      "aria-invalid": s || void 0,
      title: s && u ? u : void 0
    }
  ));
}, { useCallback: Ft } = e, Ht = ({ controlId: l, state: t, config: n }) => {
  const [r, i] = je(), s = Ft(
    (m) => {
      const p = m.target.value;
      i(p === "" ? null : p);
    },
    [i]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, r != null ? String(r) : "");
  const c = t.hasError === !0, u = t.hasWarnings === !0, a = t.errorMessage, o = [
    "tlReactNumberInput",
    c ? "tlReactNumberInput--error" : "",
    !c && u ? "tlReactNumberInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      inputMode: n != null && n.decimal ? "decimal" : "numeric",
      value: r != null ? String(r) : "",
      onChange: s,
      disabled: t.disabled === !0,
      className: o,
      "aria-invalid": c || void 0,
      title: c && a ? a : void 0
    }
  ));
}, { useCallback: Ut } = e, Wt = ({ controlId: l, state: t }) => {
  const [n, r] = je(), i = Ut(
    (a) => {
      r(a.target.value || null);
    },
    [r]
  );
  if (t.editable === !1) {
    const a = t.displayValue ?? n ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, a);
  }
  const s = t.hasError === !0, c = t.hasWarnings === !0, u = [
    "tlReactDatePicker",
    s ? "tlReactDatePicker--error" : "",
    !s && c ? "tlReactDatePicker--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: n ?? "",
      onChange: i,
      disabled: t.disabled === !0,
      className: u,
      "aria-invalid": s || void 0
    }
  ));
}, { useCallback: zt } = e, Vt = ({ controlId: l, state: t, config: n }) => {
  var m;
  const [r, i] = je(), s = zt(
    (p) => {
      i(p.target.value || null);
    },
    [i]
  ), c = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const p = ((m = c.find((f) => f.value === r)) == null ? void 0 : m.label) ?? "";
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
    c.map((p) => /* @__PURE__ */ e.createElement("option", { key: p.value, value: p.value }, p.label))
  ));
}, { useCallback: Kt } = e, Yt = ({ controlId: l, state: t }) => {
  const [n, r] = je(), i = Kt(
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
  const s = t.hasError === !0, c = t.hasWarnings === !0, u = [
    "tlReactCheckbox",
    s ? "tlReactCheckbox--error" : "",
    !s && c ? "tlReactCheckbox--warning" : ""
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
const { useCallback: Gt } = e, Xt = ({ controlId: l, command: t, label: n, image: r, disabled: i, displayMode: s }) => {
  const c = q(), u = le(), a = t ?? "click", o = n ?? c.label, m = r ?? c.image, p = i ?? c.disabled === !0, f = s ?? c.displayMode ?? "label-only", v = c.hidden === !0, h = c.tooltip, k = v ? { display: "none" } : void 0, E = c.appearance, w = c.size, C = c.navigateUrl, R = Gt(() => {
    if (C) {
      window.location.assign(C);
      return;
    }
    u(a);
  }, [u, a, C]), D = c.keyGesture;
  ue(D, () => p || v ? !1 : (R(), !0));
  const b = f === "icon-only", _ = f === "icon-only" || f === "icon-label", g = f === "label-only" || f === "icon-label" || b && !m, Y = h ?? (b ? o : void 0), x = Y ? `text:${Y}` : void 0;
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: R,
      disabled: p,
      style: k,
      className: "tlReactButton" + (b ? " tlReactButton--iconOnly" : "") + (E === "link" ? " tlReactButton--link" : "") + (E === "primary" ? " tlReactButton--primary" : "") + (w === "small" ? " tlReactButton--small" : "") + (w === "large" ? " tlReactButton--large" : ""),
      "data-tooltip": x,
      "aria-label": b ? o : void 0
    },
    _ && m && /* @__PURE__ */ e.createElement(Ee, { encoded: m, className: "tlReactButton__image" }),
    g && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, o)
  );
}, qt = ({ controlId: l }) => {
  const t = q(), n = Be(), r = e.useRef(null), [i, s] = e.useState(!1), c = t.label ?? "", u = t.image, a = t.disabled === !0, o = t.hidden === !0, m = t.displayMode ?? "label-only", p = t.appearance, f = t.accept, v = t.multiple === !0, h = e.useCallback(() => {
    var D;
    a || i || (D = r.current) == null || D.click();
  }, [a, i]), k = e.useCallback(async (D) => {
    const b = D.target.files;
    if (!b || b.length === 0) return;
    const _ = new FormData();
    for (let g = 0; g < b.length; g++)
      _.append("file", b[g], b[g].name);
    D.target.value = "", s(!0);
    try {
      await n(_);
    } finally {
      s(!1);
    }
  }, [n]), E = m === "icon-only", w = m === "icon-only" || m === "icon-label", C = m === "label-only" || m === "icon-label" || E && !u, R = a || i;
  return /* @__PURE__ */ e.createElement("span", { id: l, style: { display: "contents" } }, /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: r,
      type: "file",
      accept: f && f !== "*" ? f : void 0,
      multiple: v || void 0,
      onChange: k,
      style: { display: "none" }
    }
  ), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: h,
      disabled: R,
      style: o ? { display: "none" } : void 0,
      className: "tlReactButton" + (E ? " tlReactButton--iconOnly" : "") + (p === "link" ? " tlReactButton--link" : "") + (p === "primary" ? " tlReactButton--primary" : ""),
      "aria-label": E ? c : void 0
    },
    w && u && /* @__PURE__ */ e.createElement(Ee, { encoded: u, className: "tlReactButton__image" }),
    C && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, c)
  ));
}, { useCallback: Zt } = e, Qt = ({ controlId: l, command: t, label: n, active: r, disabled: i }) => {
  const s = q(), c = le(), u = t ?? "click", a = n ?? s.label, o = r ?? s.active === !0, m = i ?? s.disabled === !0, p = Zt(() => {
    c(u);
  }, [c, u]);
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
}, Jt = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.count ?? 0, i = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, r), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: en } = e, tn = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.tabs ?? [], i = t.activeTabId, s = en((c) => {
    c !== i && n("selectTab", { tabId: c });
  }, [n, i]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, r.map((c) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: c.id,
      role: "tab",
      "aria-selected": c.id === i,
      className: "tlReactTabBar__tab" + (c.id === i ? " tlReactTabBar__tab--active" : ""),
      onClick: () => s(c.id)
    },
    c.icon && /* @__PURE__ */ e.createElement(Ee, { encoded: c.icon, className: "tlReactTabBar__tabIcon" }),
    c.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(z, { control: t.activeContent })));
}, nn = ({ controlId: l }) => {
  const t = q(), n = t.title, r = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, r.map((i, s) => /* @__PURE__ */ e.createElement("div", { key: s, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(z, { control: i })))));
}, ln = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, rn = ({ controlId: l }) => {
  const t = q(), n = Be(), [r, i] = e.useState("idle"), [s, c] = e.useState(null), u = e.useRef(null), a = e.useRef([]), o = e.useRef(null), m = t.status ?? "idle", p = t.error, f = m === "received" ? "idle" : r !== "idle" ? r : m, v = e.useCallback(async () => {
    if (r === "recording") {
      const C = u.current;
      C && C.state !== "inactive" && C.stop();
      return;
    }
    if (r !== "uploading") {
      if (c(null), !window.isSecureContext || !navigator.mediaDevices) {
        c("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const C = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        o.current = C, a.current = [];
        const R = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", D = new MediaRecorder(C, R ? { mimeType: R } : void 0);
        u.current = D, D.ondataavailable = (b) => {
          b.data.size > 0 && a.current.push(b.data);
        }, D.onstop = async () => {
          C.getTracks().forEach((g) => g.stop()), o.current = null;
          const b = new Blob(a.current, { type: D.mimeType || "audio/webm" });
          if (a.current = [], b.size === 0) {
            i("idle");
            return;
          }
          i("uploading");
          const _ = new FormData();
          _.append("audio", b, "recording.webm"), await n(_), i("idle");
        }, D.start(), i("recording");
      } catch (C) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", C), c("js.audioRecorder.error.denied"), i("idle");
      }
    }
  }, [r, n]), h = ce(ln), k = f === "recording" ? h["js.audioRecorder.stop"] : f === "uploading" ? h["js.uploading"] : h["js.audioRecorder.record"], E = f === "uploading", w = ["tlAudioRecorder__button"];
  return f === "recording" && w.push("tlAudioRecorder__button--recording"), f === "uploading" && w.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: w.join(" "),
      onClick: v,
      disabled: E,
      title: k,
      "aria-label": k
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${f === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, h[s]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, an = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, on = ({ controlId: l }) => {
  const t = q(), n = Oe(), r = !!t.hasAudio, i = t.dataRevision ?? 0, [s, c] = e.useState(r ? "idle" : "disabled"), u = e.useRef(null), a = e.useRef(null), o = e.useRef(i);
  e.useEffect(() => {
    r ? s === "disabled" && c("idle") : (u.current && (u.current.pause(), u.current = null), a.current && (URL.revokeObjectURL(a.current), a.current = null), c("disabled"));
  }, [r]), e.useEffect(() => {
    i !== o.current && (o.current = i, u.current && (u.current.pause(), u.current = null), a.current && (URL.revokeObjectURL(a.current), a.current = null), (s === "playing" || s === "paused" || s === "loading") && c("idle"));
  }, [i]), e.useEffect(() => () => {
    u.current && (u.current.pause(), u.current = null), a.current && (URL.revokeObjectURL(a.current), a.current = null);
  }, []);
  const m = e.useCallback(async () => {
    if (s === "disabled" || s === "loading")
      return;
    if (s === "playing") {
      u.current && u.current.pause(), c("paused");
      return;
    }
    if (s === "paused" && u.current) {
      u.current.play(), c("playing");
      return;
    }
    if (!a.current) {
      c("loading");
      try {
        const E = await fetch(n);
        if (!E.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", E.status), c("idle");
          return;
        }
        const w = await E.blob();
        a.current = URL.createObjectURL(w);
      } catch (E) {
        console.error("[TLAudioPlayer] Fetch error:", E), c("idle");
        return;
      }
    }
    const k = new Audio(a.current);
    u.current = k, k.onended = () => {
      c("idle");
    }, k.play(), c("playing");
  }, [s, n]), p = ce(an), f = s === "loading" ? p["js.loading"] : s === "playing" ? p["js.audioPlayer.pause"] : s === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], v = s === "disabled" || s === "loading", h = ["tlAudioPlayer__button"];
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
}, sn = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, cn = ({ controlId: l }) => {
  const t = q(), n = Be(), [r, i] = e.useState("idle"), [s, c] = e.useState(!1), u = e.useRef(null), a = t.status ?? "idle", o = t.error, m = t.accept ?? "", p = a === "received" ? "idle" : r !== "idle" ? r : a, f = e.useCallback(async (b) => {
    i("uploading");
    const _ = new FormData();
    _.append("file", b, b.name), await n(_), i("idle");
  }, [n]), v = e.useCallback((b) => {
    var g;
    const _ = (g = b.target.files) == null ? void 0 : g[0];
    _ && f(_);
  }, [f]), h = e.useCallback(() => {
    var b;
    r !== "uploading" && ((b = u.current) == null || b.click());
  }, [r]), k = e.useCallback((b) => {
    b.preventDefault(), b.stopPropagation(), c(!0);
  }, []), E = e.useCallback((b) => {
    b.preventDefault(), b.stopPropagation(), c(!1);
  }, []), w = e.useCallback((b) => {
    var g;
    if (b.preventDefault(), b.stopPropagation(), c(!1), r === "uploading") return;
    const _ = (g = b.dataTransfer.files) == null ? void 0 : g[0];
    _ && f(_);
  }, [r, f]), C = p === "uploading", R = ce(sn), D = p === "uploading" ? R["js.uploading"] : R["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${s ? " tlFileUpload--dragover" : ""}`,
      onDragOver: k,
      onDragLeave: E,
      onDrop: w
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
        disabled: C,
        title: D,
        "aria-label": D
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    o && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, o)
  );
}, un = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, dn = ({ controlId: l, state: t }) => {
  const r = q() ?? t ?? {}, i = Be(), s = Oe(), c = ce(un), u = r.editable !== !1, a = !!r.hasData, o = r.fileName ?? "download", m = r.dataRevision ?? 0, p = r.accept ?? "", f = r.status ?? "idle", v = r.error ?? null, [h, k] = e.useState("idle"), [E, w] = e.useState(!1), [C, R] = e.useState(!1), D = e.useRef(null), b = e.useCallback(async () => {
    if (!(!a || C)) {
      R(!0);
      try {
        const B = s + (s.includes("?") ? "&" : "?") + "rev=" + m, M = await fetch(B);
        if (!M.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", M.status);
          return;
        }
        const P = await M.blob(), K = URL.createObjectURL(P), d = document.createElement("a");
        d.href = K, d.download = o, d.style.display = "none", document.body.appendChild(d), d.click(), document.body.removeChild(d), URL.revokeObjectURL(K);
      } catch (B) {
        console.error("[TLBinaryField] Fetch error:", B);
      } finally {
        R(!1);
      }
    }
  }, [a, C, s, m, o]), _ = e.useCallback(async (B) => {
    k("uploading");
    const M = new FormData();
    M.append("file", B, B.name), await i(M), k("idle");
  }, [i]), g = (f === "received" ? "idle" : h !== "idle" ? h : f) === "uploading", Y = e.useCallback((B) => {
    var P;
    const M = (P = B.target.files) == null ? void 0 : P[0];
    M && _(M);
  }, [_]), x = e.useCallback(() => {
    var B;
    g || (B = D.current) == null || B.click();
  }, [g]), T = e.useCallback((B) => {
    B.preventDefault(), B.stopPropagation(), w(!0);
  }, []), V = e.useCallback((B) => {
    B.preventDefault(), B.stopPropagation(), w(!1);
  }, []), A = e.useCallback((B) => {
    var P;
    if (B.preventDefault(), B.stopPropagation(), w(!1), g) return;
    const M = (P = B.dataTransfer.files) == null ? void 0 : P[0];
    M && _(M);
  }, [g, _]), N = C ? c["js.downloading"] : c["js.download.file"].replace("{0}", o), O = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (C ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: b,
      disabled: C,
      title: N,
      "aria-label": N
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o));
  if (!u)
    return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, O) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, c["js.download.noFile"]));
  const Q = g, U = g ? c["js.uploading"] : c["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlBinaryField tlFileUpload${E ? " tlFileUpload--dragover" : ""}`,
      onDragOver: T,
      onDragLeave: V,
      onDrop: A
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
        title: U,
        "aria-label": U
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    a && O,
    v && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, v)
  );
}, mn = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, pn = ({ controlId: l }) => {
  const t = q(), n = Oe(), r = le(), i = !!t.hasData, s = t.dataRevision ?? 0, c = t.fileName ?? "download", u = !!t.clearable, [a, o] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!i || a)) {
      o(!0);
      try {
        const h = n + (n.includes("?") ? "&" : "?") + "rev=" + s, k = await fetch(h);
        if (!k.ok) {
          console.error("[TLDownload] Failed to fetch data:", k.status);
          return;
        }
        const E = await k.blob(), w = URL.createObjectURL(E), C = document.createElement("a");
        C.href = w, C.download = c, C.style.display = "none", document.body.appendChild(C), C.click(), document.body.removeChild(C), URL.revokeObjectURL(w);
      } catch (h) {
        console.error("[TLDownload] Fetch error:", h);
      } finally {
        o(!1);
      }
    }
  }, [i, a, n, s, c]), p = e.useCallback(async () => {
    i && await r("clear");
  }, [i, r]), f = ce(mn);
  if (!i)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, f["js.download.noFile"]));
  const v = a ? f["js.downloading"] : f["js.download.file"].replace("{0}", c);
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
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: c }, c), u && /* @__PURE__ */ e.createElement(
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
}, fn = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, hn = ({ controlId: l }) => {
  const t = q(), n = Be(), [r, i] = e.useState("idle"), [s, c] = e.useState(null), [u, a] = e.useState(!1), o = e.useRef(null), m = e.useRef(null), p = e.useRef(null), f = e.useRef(null), v = e.useRef(null), h = t.error, k = e.useMemo(
    () => {
      var T;
      return !!(window.isSecureContext && ((T = navigator.mediaDevices) != null && T.getUserMedia));
    },
    []
  ), E = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((T) => T.stop()), m.current = null), o.current && (o.current.srcObject = null);
  }, []), w = e.useCallback(() => {
    E(), i("idle");
  }, [E]), C = e.useCallback(async () => {
    var T;
    if (r !== "uploading") {
      if (c(null), !k) {
        (T = f.current) == null || T.click();
        return;
      }
      try {
        const V = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        m.current = V, i("overlayOpen");
      } catch (V) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", V), c("js.photoCapture.error.denied"), i("idle");
      }
    }
  }, [r, k]), R = e.useCallback(async () => {
    if (r !== "overlayOpen")
      return;
    const T = o.current, V = p.current;
    if (!T || !V)
      return;
    V.width = T.videoWidth, V.height = T.videoHeight;
    const A = V.getContext("2d");
    A && (A.drawImage(T, 0, 0), E(), i("uploading"), V.toBlob(async (N) => {
      if (!N) {
        i("idle");
        return;
      }
      const O = new FormData();
      O.append("photo", N, "capture.jpg"), await n(O), i("idle");
    }, "image/jpeg", 0.85));
  }, [r, n, E]), D = e.useCallback(async (T) => {
    var N;
    const V = (N = T.target.files) == null ? void 0 : N[0];
    if (!V) return;
    i("uploading");
    const A = new FormData();
    A.append("photo", V, V.name), await n(A), i("idle"), f.current && (f.current.value = "");
  }, [n]);
  e.useEffect(() => {
    r === "overlayOpen" && o.current && m.current && (o.current.srcObject = m.current);
  }, [r]), e.useEffect(() => {
    var V;
    if (r !== "overlayOpen") return;
    (V = v.current) == null || V.focus();
    const T = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = T;
    };
  }, [r]), Se(r === "overlayOpen", { ESCAPE: w }), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((T) => T.stop()), m.current = null);
  }, []);
  const b = ce(fn), _ = r === "uploading" ? b["js.uploading"] : b["js.photoCapture.open"], g = ["tlPhotoCapture__cameraBtn"];
  r === "uploading" && g.push("tlPhotoCapture__cameraBtn--uploading");
  const Y = ["tlPhotoCapture__overlayVideo"];
  u && Y.push("tlPhotoCapture__overlayVideo--mirrored");
  const x = ["tlPhotoCapture__mirrorBtn"];
  return u && x.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: g.join(" "),
      onClick: C,
      disabled: r === "uploading",
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
  ), /* @__PURE__ */ e.createElement("canvas", { ref: p, style: { display: "none" } }), r === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: v,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: w }),
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
        onClick: () => a((T) => !T),
        title: b["js.photoCapture.mirror"],
        "aria-label": b["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: R,
        title: b["js.photoCapture.capture"],
        "aria-label": b["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: w,
        title: b["js.photoCapture.close"],
        "aria-label": b["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, b[s]), h && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, h));
}, bn = {
  "js.photoViewer.alt": "Captured photo"
}, _n = ({ controlId: l }) => {
  const t = q(), n = Oe(), r = !!t.hasPhoto, i = t.dataRevision ?? 0, [s, c] = e.useState(null), u = e.useRef(i);
  e.useEffect(() => {
    if (!r) {
      s && (URL.revokeObjectURL(s), c(null));
      return;
    }
    if (i === u.current && s)
      return;
    u.current = i, s && (URL.revokeObjectURL(s), c(null));
    let o = !1;
    return (async () => {
      try {
        const m = await fetch(n);
        if (!m.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", m.status);
          return;
        }
        const p = await m.blob();
        o || c(URL.createObjectURL(p));
      } catch (m) {
        console.error("[TLPhotoViewer] Fetch error:", m);
      }
    })(), () => {
      o = !0;
    };
  }, [r, i, n]), e.useEffect(() => () => {
    s && URL.revokeObjectURL(s);
  }, []);
  const a = ce(bn);
  return !r || !s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: s,
      alt: a["js.photoViewer.alt"]
    }
  ));
}, gn = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, vn = ({ controlId: l }) => {
  const t = q(), n = Oe(), r = !!t.hasPdf, i = t.dataRevision ?? 0, s = ce(gn), u = n.indexOf("react-api/"), a = u >= 0 ? n.slice(0, u) : n, o = n + "&rev=" + i, m = a + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(o);
  return r ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: m,
      title: s["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, s["js.pdfViewer.noDocument"]));
}, { useCallback: pt, useRef: Ge } = e, En = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.orientation, i = t.resizable === !0, s = t.children ?? [], c = r === "horizontal", u = s.length > 0 && s.every((E) => E.collapsed), a = !u && s.some((E) => E.collapsed), o = u ? !c : c, m = Ge(null), p = Ge(null), f = Ge(null), v = pt((E, w) => {
    const C = {
      overflow: E.scrolling || "auto"
    };
    return E.collapsed ? u && !o ? C.flex = "1 0 0%" : C.flex = "0 0 auto" : w !== void 0 ? C.flex = `0 0 ${w}px` : C.flex = `${E.size} 1 0%`, E.minSize > 0 && !E.collapsed && (C.minWidth = c ? E.minSize : void 0, C.minHeight = c ? void 0 : E.minSize), C;
  }, [c, u, a, o]), h = pt((E, w) => {
    E.preventDefault();
    const C = m.current;
    if (!C) return;
    const R = s[w], D = s[w + 1], b = C.querySelectorAll(":scope > .tlSplitPanel__child"), _ = [];
    b.forEach((x) => {
      _.push(c ? x.offsetWidth : x.offsetHeight);
    }), f.current = _, p.current = {
      splitterIndex: w,
      startPos: c ? E.clientX : E.clientY,
      startSizeBefore: _[w],
      startSizeAfter: _[w + 1],
      childBefore: R,
      childAfter: D
    };
    const g = (x) => {
      const T = p.current;
      if (!T || !f.current) return;
      const A = (c ? x.clientX : x.clientY) - T.startPos, N = T.childBefore.minSize || 0, O = T.childAfter.minSize || 0;
      let Q = T.startSizeBefore + A, U = T.startSizeAfter - A;
      Q < N && (U += Q - N, Q = N), U < O && (Q += U - O, U = O), f.current[T.splitterIndex] = Q, f.current[T.splitterIndex + 1] = U;
      const B = C.querySelectorAll(":scope > .tlSplitPanel__child"), M = B[T.splitterIndex], P = B[T.splitterIndex + 1];
      M && (M.style.flex = `0 0 ${Q}px`), P && (P.style.flex = `0 0 ${U}px`);
    }, Y = () => {
      if (document.removeEventListener("mousemove", g), document.removeEventListener("mouseup", Y), document.body.style.cursor = "", document.body.style.userSelect = "", f.current) {
        const x = {};
        s.forEach((T, V) => {
          const A = T.control;
          A != null && A.controlId && f.current && (x[A.controlId] = f.current[V]);
        }), n("updateSizes", { sizes: x });
      }
      f.current = null, p.current = null;
    };
    document.addEventListener("mousemove", g), document.addEventListener("mouseup", Y), document.body.style.cursor = c ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [s, c, n]), k = [];
  return s.forEach((E, w) => {
    if (k.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${w}`,
          className: `tlSplitPanel__child${E.collapsed && o ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: v(E)
        },
        /* @__PURE__ */ e.createElement(z, { control: E.control })
      )
    ), i && w < s.length - 1) {
      const C = s[w + 1];
      !E.collapsed && !C.collapsed && k.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${w}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${r}`,
            onMouseDown: (D) => h(D, w)
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
    k
  );
}, { useCallback: Xe } = e, Cn = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, wn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), yn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), kn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), Sn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), Nn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), Tn = ({ controlId: l }) => {
  const t = q(), n = le(), r = ce(Cn), i = t.title, s = t.expansionState ?? "NORMALIZED", c = t.showMinimize === !0, u = t.showMaximize === !0, a = t.showPopOut === !0, o = t.fullLine === !0, m = s === "MINIMIZED", p = s === "MAXIMIZED", f = s === "HIDDEN", v = Xe(() => {
    n("toggleMinimize");
  }, [n]), h = Xe(() => {
    n("toggleMaximize");
  }, [n]), k = Xe(() => {
    n("popOut");
  }, [n]);
  if (f)
    return null;
  const E = p ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${s.toLowerCase()}${o ? " tlPanel--fullLine" : ""}`,
      style: E
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(z, { control: t.toolbar }), c && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
        title: m ? r["js.panel.restore"] : r["js.panel.minimize"]
      },
      m ? /* @__PURE__ */ e.createElement(yn, null) : /* @__PURE__ */ e.createElement(wn, null)
    ), u && !m && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: h,
        title: p ? r["js.panel.restore"] : r["js.panel.maximize"]
      },
      p ? /* @__PURE__ */ e.createElement(Sn, null) : /* @__PURE__ */ e.createElement(kn, null)
    ), a && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: k,
        title: r["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(Nn, null)
    ))),
    !m && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(z, { control: t.child })),
    !m && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(z, { control: t.buttonBar }))
  );
}, Rn = ({ controlId: l }) => {
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
}, Dn = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(z, { control: t.activeChild }));
}, { useCallback: he, useState: Ke, useEffect: lt, useRef: Ye } = e, xn = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function rt(l, t, n, r) {
  const i = [];
  for (const s of l)
    if (s.type === "nav") {
      if (s.hidden) continue;
      i.push({ id: s.id, type: "nav", groupId: r });
    } else s.type === "command" ? i.push({ id: s.id, type: "command", groupId: r }) : s.type === "group" && (i.push({ id: s.id, type: "group" }), (n.get(s.id) ?? s.expanded) && !t && i.push(...rt(s.children, t, n, s.id)));
  return i;
}
const Pe = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(Ee, { encoded: l, className: "tlSidebar__icon" }) : null, Ln = ({ item: l, active: t, collapsed: n, onSelect: r, tabIndex: i, itemRef: s, onFocus: c }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => r(l.id),
    title: n ? l.label : void 0,
    tabIndex: i,
    ref: s,
    onFocus: () => c(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Pe, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Pe, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), In = ({ item: l, collapsed: t, onExecute: n, tabIndex: r, itemRef: i, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: r,
    ref: i,
    onFocus: () => s(l.id)
  },
  /* @__PURE__ */ e.createElement(Pe, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), Pn = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Pe, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), jn = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), Mn = ({ item: l, activeItemId: t, anchorRect: n, onSelect: r, onExecute: i, onClose: s }) => {
  const c = Ye(null);
  lt(() => {
    const o = (m) => {
      c.current && !c.current.contains(m.target) && setTimeout(() => s(), 0);
    };
    return document.addEventListener("mousedown", o), () => document.removeEventListener("mousedown", o);
  }, [s]), Se(!0, { ESCAPE: s });
  const u = he((o) => {
    o.type === "nav" ? (r(o.id), s()) : o.type === "command" && (i(o.id), s());
  }, [r, i, s]), a = {};
  return n && (a.left = n.right, a.top = n.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: c, role: "menu", style: a }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((o) => {
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
}, An = ({
  item: l,
  expanded: t,
  activeItemId: n,
  collapsed: r,
  onSelect: i,
  onExecute: s,
  onToggleGroup: c,
  tabIndex: u,
  itemRef: a,
  onFocus: o,
  focusedId: m,
  setItemRef: p,
  onItemFocus: f,
  flyoutGroupId: v,
  onOpenFlyout: h,
  onCloseFlyout: k
}) => {
  const E = Ye(null), [w, C] = Ke(null), R = he(() => {
    r ? v === l.id ? k() : (E.current && C(E.current.getBoundingClientRect()), h(l.id)) : c(l.id);
  }, [r, v, l.id, c, h, k]), D = he((_) => {
    E.current = _, a(_);
  }, [a]), b = r && v === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (b ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: R,
      title: r ? l.label : void 0,
      "aria-expanded": r ? b : t,
      tabIndex: u,
      ref: D,
      onFocus: () => o(l.id)
    },
    /* @__PURE__ */ e.createElement(Pe, { icon: l.icon }),
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
    Mn,
    {
      item: l,
      activeItemId: n,
      anchorRect: w,
      onSelect: i,
      onExecute: s,
      onClose: k
    }
  ), t && !r && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((_) => /* @__PURE__ */ e.createElement(
    Rt,
    {
      key: _.id,
      item: _,
      activeItemId: n,
      collapsed: r,
      onSelect: i,
      onExecute: s,
      onToggleGroup: c,
      focusedId: m,
      setItemRef: p,
      onItemFocus: f,
      groupStates: null,
      flyoutGroupId: v,
      onOpenFlyout: h,
      onCloseFlyout: k
    }
  ))));
}, Rt = ({
  item: l,
  activeItemId: t,
  collapsed: n,
  onSelect: r,
  onExecute: i,
  onToggleGroup: s,
  focusedId: c,
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
        Ln,
        {
          item: l,
          active: l.id === t,
          collapsed: n,
          onSelect: r,
          tabIndex: c === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: a
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        In,
        {
          item: l,
          collapsed: n,
          onExecute: i,
          tabIndex: c === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: a
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(Pn, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(jn, null);
    case "group": {
      const v = o ? o.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        An,
        {
          item: l,
          expanded: v,
          activeItemId: t,
          collapsed: n,
          onSelect: r,
          onExecute: i,
          onToggleGroup: s,
          tabIndex: c === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: a,
          focusedId: c,
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
}, Bn = ({ controlId: l }) => {
  const t = q(), n = le(), r = ce(xn), i = t.items ?? [], s = t.activeItemId, c = t.collapsed, u = t.drawerOpen, a = u ? !1 : c, [o, m] = Ke(() => {
    const N = /* @__PURE__ */ new Map(), O = (Q) => {
      for (const U of Q)
        U.type === "group" && (N.set(U.id, U.expanded), O(U.children));
    };
    return O(i), N;
  }), p = he((N) => {
    m((O) => {
      const Q = new Map(O), U = Q.get(N) ?? !1;
      return Q.set(N, !U), n("toggleGroup", { itemId: N, expanded: !U }), Q;
    });
  }, [n]), f = he((N) => {
    N !== s && n("selectItem", { itemId: N });
  }, [n, s]), v = he((N) => {
    n("executeCommand", { itemId: N });
  }, [n]), h = he(() => {
    n("toggleCollapse", {});
  }, [n]), k = he(() => {
    n("toggleDrawer", {});
  }, [n]), [E, w] = Ke(null), C = he((N) => {
    w(N);
  }, []), R = he(() => {
    w(null);
  }, []);
  lt(() => {
    a || w(null);
  }, [a]);
  const [D, b] = Ke(() => {
    const N = rt(i, a, o);
    return N.length > 0 ? N[0].id : "";
  }), _ = Ye(/* @__PURE__ */ new Map()), g = he((N) => (O) => {
    O ? _.current.set(N, O) : _.current.delete(N);
  }, []), Y = he((N) => {
    b(N);
  }, []), x = Ye(0), T = he((N) => {
    b(N), x.current++;
  }, []);
  lt(() => {
    const N = _.current.get(D);
    N && document.activeElement !== N && N.focus();
  }, [D, x.current]);
  const V = he((N) => {
    if (N.key === "Escape" && E !== null) {
      N.preventDefault(), R();
      return;
    }
    const O = rt(i, a, o);
    if (O.length === 0) return;
    const Q = O.findIndex((B) => B.id === D);
    if (Q < 0) return;
    const U = O[Q];
    switch (N.key) {
      case "ArrowDown": {
        N.preventDefault();
        const B = (Q + 1) % O.length;
        T(O[B].id);
        break;
      }
      case "ArrowUp": {
        N.preventDefault();
        const B = (Q - 1 + O.length) % O.length;
        T(O[B].id);
        break;
      }
      case "Home": {
        N.preventDefault(), T(O[0].id);
        break;
      }
      case "End": {
        N.preventDefault(), T(O[O.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        N.preventDefault(), U.type === "nav" ? f(U.id) : U.type === "command" ? v(U.id) : U.type === "group" && (a ? E === U.id ? R() : C(U.id) : p(U.id));
        break;
      }
      case "ArrowRight": {
        U.type === "group" && !a && ((o.get(U.id) ?? !1) || (N.preventDefault(), p(U.id)));
        break;
      }
      case "ArrowLeft": {
        U.type === "group" && !a && (o.get(U.id) ?? !1) && (N.preventDefault(), p(U.id));
        break;
      }
    }
  }, [
    i,
    a,
    o,
    D,
    E,
    T,
    f,
    v,
    p,
    C,
    R
  ]), A = "tlSidebar" + (a ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: A }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(z, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: k, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": r["js.sidebar.ariaLabel"] }, a ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(z, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(z, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: V }, i.map((N) => /* @__PURE__ */ e.createElement(
    Rt,
    {
      key: N.id,
      item: N,
      activeItemId: s,
      collapsed: a,
      onSelect: f,
      onExecute: v,
      onToggleGroup: p,
      focusedId: D,
      setItemRef: g,
      onItemFocus: Y,
      groupStates: o,
      flyoutGroupId: E,
      onOpenFlyout: C,
      onCloseFlyout: R
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
}, On = ({ controlId: l }) => {
  const t = q(), n = t.direction ?? "column", r = t.gap ?? "default", i = t.align ?? "stretch", s = t.wrap === !0, c = t.growFirst === !0, u = t.children ?? [], a = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${r}`,
    `tlStack--align-${i}`,
    s ? "tlStack--wrap" : "",
    c ? "tlStack--grow-first" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: a }, u.map((o, m) => /* @__PURE__ */ e.createElement(z, { key: m, control: o })));
}, $n = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlInset" }, t.child && /* @__PURE__ */ e.createElement(z, { control: t.child }));
}, Fn = ({ controlId: l }) => {
  const t = q(), n = t.columns, r = t.minColumnWidth, i = t.gap ?? "default", s = t.children ?? [], c = {};
  return r ? c.gridTemplateColumns = `repeat(auto-fit, minmax(${r}, 1fr))` : n && (c.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${i}`, style: c }, s.map((u, a) => /* @__PURE__ */ e.createElement(z, { key: a, control: u })));
}, Hn = ({ controlId: l }) => {
  const t = q(), n = t.title, r = t.variant ?? "outlined", i = t.padding ?? "default", s = t.headerActions ?? [], c = t.child, u = n != null || s.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${r}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, s.map((a, o) => /* @__PURE__ */ e.createElement(z, { key: o, control: a })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${i}` }, /* @__PURE__ */ e.createElement(z, { control: c })));
}, Un = ({ controlId: l }) => {
  const t = q(), n = t.title ?? "", r = t.leading, i = t.children ?? [], s = t.actions ?? [], c = t.variant ?? "flat", a = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    c === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: a }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(z, { control: r })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, i.map((o, m) => /* @__PURE__ */ e.createElement(z, { key: m, control: o }))), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((o, m) => /* @__PURE__ */ e.createElement(z, { key: m, control: o }))));
}, { useCallback: Wn } = e, zn = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.items ?? [], i = Wn((s) => {
    n("navigate", { itemId: s });
  }, [n]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, r.map((s, c) => {
    const u = c === r.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: s.id, className: "tlBreadcrumb__entry" }, c > 0 && /* @__PURE__ */ e.createElement(
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
        onClick: () => i(s.id)
      },
      s.label
    ));
  })));
}, { useCallback: Vn } = e, Kn = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.items ?? [], i = t.activeItemId, s = Vn((c) => {
    c !== i && n("selectItem", { itemId: c });
  }, [n, i]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, r.map((c) => {
    const u = c.id === i;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: c.id,
        type: "button",
        className: "tlBottomBar__item" + (u ? " tlBottomBar__item--active" : ""),
        onClick: () => s(c.id),
        "aria-current": u ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + c.icon, "aria-hidden": "true" }), c.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, c.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, c.label)
    );
  }));
}, { useCallback: ft, useRef: Yn } = e, Gn = ({ onClose: l }) => (ue("ESCAPE", () => (l(), !0)), null), Xn = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.open === !0, i = t.closeOnBackdrop !== !1, s = t.child, c = Yn(null), u = ft(() => {
    n("close");
  }, [n]), a = ft((o) => {
    i && o.target === o.currentTarget && u();
  }, [i, u]);
  return r ? /* @__PURE__ */ e.createElement(st, null, /* @__PURE__ */ e.createElement(Gn, { onClose: u }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: a,
      ref: c,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(z, { control: s })
  )) : null;
}, { useEffect: qn, useRef: Zn } = e, Qn = ({ controlId: l }) => {
  const n = q().dialogs ?? [], r = Zn(n.length);
  return qn(() => {
    n.length < r.current && n.length > 0, r.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((i) => /* @__PURE__ */ e.createElement(z, { key: i.controlId, control: i })));
}, { useCallback: He, useRef: De, useState: Ue } = e, Jn = ({ onClose: l }) => (ue("ESCAPE", () => (l(), !0)), null), el = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, tl = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], nl = ({ controlId: l }) => {
  const t = q(), n = le(), r = ce(el), i = t.title ?? "", s = t.width ?? "32rem", c = t.height ?? null, u = t.minHeight ?? null, a = t.resizable === !0, o = t.child, m = t.actions ?? [], p = t.toolbar, f = t.buttonBar, [v, h] = Ue(null), [k, E] = Ue(null), [w, C] = Ue(null), R = De(null), [D, b] = Ue(!1), _ = De(null), g = De(null), Y = De(null), x = De(null), T = De(null), V = He(() => {
    n("close");
  }, [n]);
  ct(!0, x, "field");
  const A = He((B, M) => {
    M.preventDefault();
    const P = x.current;
    if (!P) return;
    const K = P.getBoundingClientRect(), d = !R.current, S = R.current ?? { x: K.left, y: K.top };
    d && (R.current = S, C(S)), T.current = {
      dir: B,
      startX: M.clientX,
      startY: M.clientY,
      startW: K.width,
      startH: K.height,
      startPos: { ...S },
      symmetric: d
    };
    const F = (G) => {
      const j = T.current;
      if (!j) return;
      const J = G.clientX - j.startX, re = G.clientY - j.startY;
      let te = j.startW, pe = j.startH, be = 0, _e = 0;
      j.symmetric ? (j.dir.includes("e") && (te = j.startW + 2 * J), j.dir.includes("w") && (te = j.startW - 2 * J), j.dir.includes("s") && (pe = j.startH + 2 * re), j.dir.includes("n") && (pe = j.startH - 2 * re)) : (j.dir.includes("e") && (te = j.startW + J), j.dir.includes("w") && (te = j.startW - J, be = J), j.dir.includes("s") && (pe = j.startH + re), j.dir.includes("n") && (pe = j.startH - re, _e = re));
      const Ce = Math.max(200, te), we = Math.max(100, pe);
      j.symmetric ? (be = (j.startW - Ce) / 2, _e = (j.startH - we) / 2) : (j.dir.includes("w") && Ce === 200 && (be = j.startW - 200), j.dir.includes("n") && we === 100 && (_e = j.startH - 100)), g.current = Ce, Y.current = we, h(Ce), E(we);
      const Ne = {
        x: j.startPos.x + be,
        y: j.startPos.y + _e
      };
      R.current = Ne, C(Ne);
    }, $ = () => {
      document.removeEventListener("mousemove", F), document.removeEventListener("mouseup", $);
      const G = g.current, j = Y.current;
      (G != null || j != null) && n("resize", {
        ...G != null ? { width: Math.round(G) + "px" } : {},
        ...j != null ? { height: Math.round(j) + "px" } : {}
      }), T.current = null;
    };
    document.addEventListener("mousemove", F), document.addEventListener("mouseup", $);
  }, [n]), N = He((B) => {
    if (B.button !== 0 || B.target.closest("button")) return;
    B.preventDefault();
    const M = x.current;
    if (!M) return;
    const P = M.getBoundingClientRect(), K = R.current ?? { x: P.left, y: P.top }, d = B.clientX - K.x, S = B.clientY - K.y, F = (G) => {
      const j = window.innerWidth, J = window.innerHeight;
      let re = G.clientX - d, te = G.clientY - S;
      const pe = M.offsetWidth, be = M.offsetHeight;
      re + pe > j && (re = j - pe), te + be > J && (te = J - be), re < 0 && (re = 0), te < 0 && (te = 0);
      const _e = { x: re, y: te };
      R.current = _e, C(_e);
    }, $ = () => {
      document.removeEventListener("mousemove", F), document.removeEventListener("mouseup", $);
    };
    document.addEventListener("mousemove", F), document.addEventListener("mouseup", $);
  }, []), O = He(() => {
    var B, M;
    if (D) {
      const P = _.current;
      P && (C(P.x !== -1 ? { x: P.x, y: P.y } : null), h(P.w), E(P.h)), b(!1);
    } else {
      const P = x.current, K = P == null ? void 0 : P.getBoundingClientRect();
      _.current = {
        x: ((B = R.current) == null ? void 0 : B.x) ?? (K == null ? void 0 : K.left) ?? -1,
        y: ((M = R.current) == null ? void 0 : M.y) ?? (K == null ? void 0 : K.top) ?? -1,
        w: v ?? (K == null ? void 0 : K.width) ?? null,
        h: k ?? null
      }, b(!0), C({ x: 0, y: 0 }), h(null), E(null);
    }
  }, [D, v, k]), Q = D ? { position: "absolute", top: 0, left: 0, width: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: v != null ? v + "px" : s,
    ...k != null ? { height: k + "px" } : c != null ? { height: c } : {},
    ...u != null && k == null ? { minHeight: u } : {},
    maxHeight: w ? "100vh" : "80vh",
    ...w ? { position: "absolute", left: w.x + "px", top: w.y + "px" } : {}
  }, U = l + "-title";
  return /* @__PURE__ */ e.createElement(st, { modal: !0 }, /* @__PURE__ */ e.createElement(Jn, { onClose: V }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: Q,
      ref: x,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": U
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${D ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: D ? void 0 : N,
        onDoubleClick: a ? O : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: U }, i),
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
          onClick: V,
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
    (m.length > 0 || f) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, f && /* @__PURE__ */ e.createElement(z, { control: f }), m.map((B, M) => /* @__PURE__ */ e.createElement(z, { key: M, control: B }))),
    a && !D && tl.map((B) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: B,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${B}`,
        onMouseDown: (M) => A(B, M)
      }
    ))
  ));
}, { useCallback: ll } = e, rl = {
  "js.drawer.close": "Close"
}, al = ({ controlId: l }) => {
  const t = q(), n = le(), r = ce(rl), i = t.open === !0, s = t.position ?? "right", c = t.size ?? "medium", u = t.title ?? null, a = t.child, o = ll(() => {
    n("close");
  }, [n]);
  Se(i, { ESCAPE: o });
  const m = [
    "tlDrawer",
    `tlDrawer--${s}`,
    `tlDrawer--${c}`,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, a && /* @__PURE__ */ e.createElement(z, { control: a })));
}, { useCallback: ol } = e, sl = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.child, i = ol((s) => {
    s.preventDefault(), s.stopPropagation(), n("openContextMenu", { x: s.clientX, y: s.clientY });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tl-context-menu-region", onContextMenu: i }, r && /* @__PURE__ */ e.createElement(z, { control: r }));
}, { useCallback: cl, useEffect: il, useState: ul } = e, dl = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.message ?? "", i = t.content ?? "", s = t.variant ?? "info", c = t.duration ?? 5e3, u = t.visible === !0, a = t.generation ?? 0, [o, m] = ul(!1), p = cl(() => {
    m(!0), setTimeout(() => {
      n("dismiss", { generation: a }), m(!1);
    }, 200);
  }, [n, a]);
  return il(() => {
    if (!u || c === 0) return;
    const f = setTimeout(p, c);
    return () => clearTimeout(f);
  }, [u, c, p]), !u && !o ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${s}${o ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    i ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: i } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, r)
  );
}, { useCallback: qe, useEffect: ht, useRef: ml, useState: bt } = e, pl = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.open === !0, i = t.anchorId, s = t.anchorX, c = t.anchorY, u = t.items ?? [], a = ml(null), [o, m] = bt({ top: 0, left: 0 }), [p, f] = bt(0), v = u.filter((w) => w.type === "item" && !w.disabled);
  ht(() => {
    var g, Y;
    if (!r) return;
    const w = ((g = a.current) == null ? void 0 : g.offsetHeight) ?? 200, C = ((Y = a.current) == null ? void 0 : Y.offsetWidth) ?? 200;
    if (s != null && c != null) {
      let x = c, T = s;
      x + w > window.innerHeight && (x = Math.max(0, window.innerHeight - w)), T + C > window.innerWidth && (T = Math.max(0, window.innerWidth - C)), m({ top: x, left: T }), f(0);
      return;
    }
    if (!i) return;
    const R = document.getElementById(i);
    if (!R) return;
    const D = R.getBoundingClientRect();
    let b = D.bottom + 4, _ = D.left;
    b + w > window.innerHeight && (b = D.top - w - 4), _ + C > window.innerWidth && (_ = D.right - C), m({ top: b, left: _ }), f(0);
  }, [r, i, s, c]);
  const h = qe(() => {
    n("close");
  }, [n]), k = qe((w) => {
    n("selectItem", { itemId: w });
  }, [n]);
  ht(() => {
    if (!r) return;
    const w = (C) => {
      a.current && !a.current.contains(C.target) && h();
    };
    return document.addEventListener("mousedown", w), () => document.removeEventListener("mousedown", w);
  }, [r, h]);
  const E = qe((w) => {
    if (w.key === "Escape") {
      w.preventDefault(), h();
      return;
    }
    if (w.key === "ArrowDown")
      w.preventDefault(), f((C) => (C + 1) % v.length);
    else if (w.key === "ArrowUp")
      w.preventDefault(), f((C) => (C - 1 + v.length) % v.length);
    else if (w.key === "Enter" || w.key === " ") {
      w.preventDefault();
      const C = v[p];
      C && k(C.id);
    }
  }, [h, k, v, p]);
  return ct(r, a), r ? /* @__PURE__ */ e.createElement(
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
    u.map((w, C) => {
      if (w.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: C, className: "tlMenu__separator" });
      const D = v.indexOf(w) === p;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: w.id,
          type: "button",
          className: "tlMenu__item" + (D ? " tlMenu__item--focused" : "") + (w.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: w.disabled,
          tabIndex: D ? 0 : -1,
          onClick: () => k(w.id)
        },
        w.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + w.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, w.label)
      );
    })
  ) : null;
}, fl = 768, hl = ({ controlId: l }) => {
  const t = q(), n = le();
  e.useEffect(() => {
    const o = window.matchMedia(`(max-width: ${fl}px)`), m = (f) => {
      n("reportDisplayClass", { displayClass: f ? "COMPACT" : "REGULAR" });
    };
    m(o.matches);
    const p = (f) => m(f.matches);
    return o.addEventListener("change", p), () => o.removeEventListener("change", p);
  }, [n]);
  const r = t.header, i = t.content, s = t.footer, c = t.snackbar, u = t.dialogManager, a = t.menuOverlay;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(z, { control: r })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(z, { control: i })), s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(z, { control: s })), /* @__PURE__ */ e.createElement(z, { control: c }), u && /* @__PURE__ */ e.createElement(z, { control: u }), a && /* @__PURE__ */ e.createElement(z, { control: a }));
}, bl = ({ controlId: l }) => {
  const t = q(), n = t.text ?? "", r = t.cssClass ?? "", i = t.hasTooltip === !0, s = r ? `tlText ${r}` : "tlText";
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: s,
      "data-tooltip": i ? "key:tooltip" : void 0
    },
    n
  );
}, _l = ({ isMulti: l, cursorIndex: t, onMove: n, onToggle: r, onSelectAll: i }) => (ue("ArrowUp", () => (n("up", !1, !1), !0)), ue("ArrowDown", () => (n("down", !1, !1), !0)), ue("Home", () => (n("home", !1, !1), !0)), ue("End", () => (n("end", !1, !1), !0)), ue("PageUp", () => (n("pageUp", !1, !1), !0)), ue("PageDown", () => (n("pageDown", !1, !1), !0)), ue("Shift+ArrowUp", () => (n("up", l, !1), !0)), ue("Shift+ArrowDown", () => (n("down", l, !1), !0)), ue("Shift+Home", () => (n("home", l, !1), !0)), ue("Shift+End", () => (n("end", l, !1), !0)), ue("Shift+PageUp", () => (n("pageUp", l, !1), !0)), ue("Shift+PageDown", () => (n("pageDown", l, !1), !0)), ue("Ctrl+ArrowUp", () => (n("up", !1, l), !0)), ue("Ctrl+ArrowDown", () => (n("down", !1, l), !0)), ue("Space", () => t < 0 ? !1 : (r(), !0)), ue("Ctrl+A", () => l ? (i(), !0) : !1), null), gl = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all",
  "js.table.filter": "Filter"
}, _t = 50, vl = ({ controlId: l }) => {
  const t = q(), n = le(), r = ce(gl), i = e.useRef(null);
  e.useEffect(() => {
    const y = i.current;
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
  const s = t.columns ?? [], c = t.totalRowCount ?? 0, u = t.rows ?? [], a = t.rowHeight ?? 36, o = t.selectionMode ?? "single", m = t.selectedCount ?? 0, p = t.cursorIndex ?? -1, f = t.frozenColumnCount ?? 0, v = t.treeMode ?? !1, h = e.useMemo(
    () => s.filter((y) => y.sortPriority && y.sortPriority > 0).length,
    [s]
  ), k = o === "multi", E = 40, w = 20, C = e.useRef(null), R = e.useRef(null), D = e.useRef(null), [b, _] = e.useState({}), g = e.useRef(null), Y = e.useRef(!1), x = e.useRef(null), [T, V] = e.useState(null), [A, N] = e.useState(null);
  e.useEffect(() => {
    g.current || _({});
  }, [s]);
  const O = e.useCallback((y) => b[y.name] ?? y.width, [b]), Q = e.useMemo(() => {
    const y = [];
    let I = k && f > 0 ? E : 0;
    for (let X = 0; X < f && X < s.length; X++)
      y.push(I), I += O(s[X]);
    return y;
  }, [s, f, k, E, O]), U = c * a, B = e.useRef(null), M = e.useCallback((y, I, X) => {
    X.preventDefault(), X.stopPropagation(), g.current = { column: y, startX: X.clientX, startWidth: I };
    let ne = X.clientX, ae = 0;
    const fe = () => {
      const ie = g.current;
      if (!ie) return;
      const ge = Math.max(_t, ie.startWidth + (ne - ie.startX) + ae);
      _((Re) => ({ ...Re, [ie.column]: ge }));
    }, se = () => {
      const ie = R.current, ge = C.current;
      if (!ie || !g.current) return;
      const Re = ie.getBoundingClientRect(), ut = 40, dt = 8, Mt = ie.scrollLeft;
      ne > Re.right - ut ? ie.scrollLeft += dt : ne < Re.left + ut && (ie.scrollLeft = Math.max(0, ie.scrollLeft - dt));
      const mt = ie.scrollLeft - Mt;
      mt !== 0 && (ge && (ge.scrollLeft = ie.scrollLeft), ae += mt, fe()), B.current = requestAnimationFrame(se);
    };
    B.current = requestAnimationFrame(se);
    const Te = (ie) => {
      ne = ie.clientX, fe();
    }, Fe = (ie) => {
      document.removeEventListener("mousemove", Te), document.removeEventListener("mouseup", Fe), B.current !== null && (cancelAnimationFrame(B.current), B.current = null);
      const ge = g.current;
      if (ge) {
        const Re = Math.max(_t, ge.startWidth + (ie.clientX - ge.startX) + ae);
        n("columnResize", { column: ge.column, width: Re }), g.current = null, Y.current = !0, requestAnimationFrame(() => {
          Y.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", Te), document.addEventListener("mouseup", Fe);
  }, [n]), P = e.useCallback(() => {
    C.current && R.current && (C.current.scrollLeft = R.current.scrollLeft), D.current !== null && clearTimeout(D.current), D.current = window.setTimeout(() => {
      const y = R.current;
      if (!y) return;
      const I = y.scrollTop, X = Math.ceil(y.clientHeight / a), ne = Math.floor(I / a);
      n("scroll", { start: ne, count: X });
    }, 80);
  }, [n, a]), K = e.useCallback((y, I, X) => {
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
    if (!I || !T) {
      x.current = null, V(null);
      return;
    }
    let X = s.findIndex((ae) => ae.name === T.column);
    if (X < 0) {
      x.current = null, V(null);
      return;
    }
    const ne = s.findIndex((ae) => ae.name === I);
    T.side === "right" && X++, ne < X && X--, n("columnReorder", { column: I, targetIndex: X }), x.current = null, V(null);
  }, [s, T, n]), $ = e.useCallback(() => {
    x.current = null, V(null);
  }, []), G = e.useCallback((y, I) => {
    var X;
    I.shiftKey && I.preventDefault(), (X = R.current) == null || X.focus({ preventScroll: !0 }), n("select", {
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
    () => !!i.current && i.current.contains(document.activeElement),
    []
  );
  e.useEffect(() => {
    if (p < 0)
      return;
    const y = R.current;
    if (!y)
      return;
    const I = p * a, X = I + a;
    I < y.scrollTop ? y.scrollTop = I : X > y.scrollTop + y.clientHeight && (y.scrollTop = X - y.clientHeight);
  }, [p, a]);
  const pe = e.useCallback((y, I) => {
    I.stopPropagation(), n("select", { rowIndex: y, ctrlKey: !0, shiftKey: !1 });
  }, [n]), be = e.useCallback(() => {
    const y = m === c && c > 0;
    n("selectAll", { selected: !y });
  }, [n, m, c]), _e = e.useCallback((y, I, X) => {
    X.stopPropagation(), n("expand", { rowIndex: y, expanded: I });
  }, [n]), Ce = e.useCallback((y, I) => {
    I.preventDefault(), N({ x: I.clientX, y: I.clientY, colIdx: y });
  }, []), we = e.useCallback(() => {
    A && (n("setFrozenColumnCount", { count: A.colIdx + 1 }), N(null));
  }, [A, n]), Ne = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), N(null);
  }, [n]);
  e.useEffect(() => {
    if (!A) return;
    const y = () => N(null);
    return document.addEventListener("mousedown", y), () => document.removeEventListener("mousedown", y);
  }, [A]), Se(!!A, { ESCAPE: () => N(null) });
  const $e = e.useCallback((y, I) => {
    I.stopPropagation(), I.preventDefault(), n("openFilter", { column: y });
  }, [n]), L = s.reduce((y, I) => y + O(I), 0) + (k ? E : 0), W = m === c && c > 0, ee = m > 0 && m < c, oe = e.useCallback((y) => {
    y && (y.indeterminate = ee);
  }, [ee]);
  return /* @__PURE__ */ e.createElement(st, { active: te }, /* @__PURE__ */ e.createElement(
    _l,
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
      ref: i,
      id: l,
      className: "tlTableView",
      "data-tooltip": "dynamic",
      onDragOver: (y) => {
        if (!x.current) return;
        y.preventDefault();
        const I = R.current, X = C.current;
        if (!I) return;
        const ne = I.getBoundingClientRect(), ae = 40, fe = 8;
        y.clientX < ne.left + ae ? I.scrollLeft = Math.max(0, I.scrollLeft - fe) : y.clientX > ne.right - ae && (I.scrollLeft += fe), X && (X.scrollLeft = I.scrollLeft);
      },
      onDrop: F
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: C }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { width: L } }, k && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (f > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: E,
          minWidth: E,
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
      y.sortable && (ne += " tlTableView__headerCell--sortable"), T && T.column === y.name && (ne += " tlTableView__headerCell--dragOver-" + T.side);
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
        ref: R,
        className: "tlTableView__body",
        onScroll: P,
        tabIndex: 0
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: U, position: "relative", width: L } }, u.map((y) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: y.id,
          className: "tlTableView__row" + (y.selected ? " tlTableView__row--selected" : "") + (y.index === p ? " tlTableView__row--cursor" : ""),
          style: {
            position: "absolute",
            top: y.index * a,
            height: a,
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
              width: E,
              minWidth: E,
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
          const Fe = v && X === 0, ie = y.treeDepth ?? 0;
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
            Fe ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ie * w } }, y.expandable ? /* @__PURE__ */ e.createElement(
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
    A && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: A.y, left: A.x, zIndex: 1e4 },
        onMouseDown: (y) => y.stopPropagation()
      },
      A.colIdx + 1 !== f && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: we }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.freezeUpTo"])),
      f > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Ne }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.unfreezeAll"]))
    )
  ));
}, El = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, Dt = e.createContext(El), { useMemo: Cl, useRef: wl, useState: yl, useEffect: kl } = e, Sl = 320, Nl = ({ controlId: l }) => {
  const t = q(), n = t.maxColumns ?? 3, r = t.labelPosition ?? "auto", i = t.readOnly === !0, s = t.children ?? [], c = t.noModelMessage, u = wl(null), [a, o] = yl(
    r === "top" ? "top" : "side"
  );
  kl(() => {
    if (r !== "auto") {
      o(r);
      return;
    }
    const h = u.current;
    if (!h) return;
    const k = new ResizeObserver((E) => {
      for (const w of E) {
        const R = w.contentRect.width / n;
        o(R < Sl ? "top" : "side");
      }
    });
    return k.observe(h), () => k.disconnect();
  }, [r, n]);
  const m = Cl(() => ({
    readOnly: i,
    resolvedLabelPosition: a
  }), [i, a]), f = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, v = [
    "tlFormLayout",
    i ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return c ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, c)) : /* @__PURE__ */ e.createElement(Dt.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: l, className: v, style: f, ref: u }, s.map((h, k) => /* @__PURE__ */ e.createElement(z, { key: k, control: h }))));
}, { useCallback: Tl } = e, Rl = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, Dl = ({ controlId: l }) => {
  const t = q(), n = le(), r = ce(Rl), i = t.headerControl ?? null, s = t.headerActions ?? [], c = t.collapsible === !0, u = t.collapsed === !0, a = t.border ?? "none", o = t.fullLine === !0, m = t.children ?? [], p = i != null || s.length > 0 || c, f = Tl(() => {
    n("toggleCollapse");
  }, [n]), v = [
    "tlFormGroup",
    `tlFormGroup--border-${a}`,
    o ? "tlFormGroup--fullLine" : "",
    u ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: v }, p && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, c && /* @__PURE__ */ e.createElement(
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
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(z, { control: i })), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, s.map((h, k) => /* @__PURE__ */ e.createElement(z, { key: k, control: h })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((h, k) => /* @__PURE__ */ e.createElement(z, { key: k, control: h }))));
}, { useContext: xl, useState: Ll, useCallback: Il } = e, Pl = ({ controlId: l }) => {
  const t = q(), n = xl(Dt), r = t.label ?? "", i = t.required === !0, s = t.error, c = t.warnings, u = t.helpText, a = t.dirty === !0, o = t.labelPosition ?? n.resolvedLabelPosition, m = t.fullLine === !0, p = t.visible !== !1, f = t.hasTooltip === !0, v = t.field, h = n.readOnly, [k, E] = Ll(!1), w = Il(() => E((b) => !b), []);
  if (!p) return null;
  const C = s != null, R = c != null && c.length > 0, D = [
    "tlFormField",
    `tlFormField--${o}`,
    h ? "tlFormField--readonly" : "",
    m ? "tlFormField--fullLine" : "",
    C ? "tlFormField--error" : "",
    !C && R ? "tlFormField--warning" : "",
    a ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: D }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": f ? "key:tooltip" : void 0
    },
    r
  ), i && !h && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), a && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), u && !h && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormField__helpIcon",
      onClick: w,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(z, { control: v })), !h && C && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, s)), !h && !C && R && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, c.map((b, _) => /* @__PURE__ */ e.createElement("div", { key: _, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(
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
}, jl = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.iconCss, i = t.iconSrc, s = t.label, c = t.cssClass, u = t.hasTooltip === !0, a = t.hasLink, o = r ? /* @__PURE__ */ e.createElement("i", { className: r }) : i ? /* @__PURE__ */ e.createElement("img", { src: i, className: "tlTypeIcon", alt: "" }) : null, m = /* @__PURE__ */ e.createElement(e.Fragment, null, o, s && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, s)), p = e.useCallback((h) => {
    h.preventDefault(), n("goto", {});
  }, [n]), f = ["tlResourceCell", c].filter(Boolean).join(" "), v = u ? "key:tooltip" : void 0;
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
}, Ml = 20, Al = () => {
  const l = q(), t = le(), n = l.nodes ?? [], r = l.selectionMode ?? "single", i = l.dragEnabled ?? !1, s = l.dropEnabled ?? !1, c = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [a, o] = e.useState(-1), m = e.useRef(null), p = e.useCallback((b, _) => {
    t(_ ? "collapse" : "expand", { nodeId: b });
  }, [t]), f = e.useCallback((b, _) => {
    t("select", {
      nodeId: b,
      ctrlKey: _.ctrlKey || _.metaKey,
      shiftKey: _.shiftKey
    });
  }, [t]), v = e.useCallback((b, _) => {
    _.preventDefault(), t("contextMenu", { nodeId: b, x: _.clientX, y: _.clientY });
  }, [t]), h = e.useRef(null), k = e.useCallback((b, _) => {
    const g = _.getBoundingClientRect(), Y = b.clientY - g.top, x = g.height / 3;
    return Y < x ? "above" : Y > x * 2 ? "below" : "within";
  }, []), E = e.useCallback((b, _) => {
    _.dataTransfer.effectAllowed = "move", _.dataTransfer.setData("text/plain", b);
  }, []), w = e.useCallback((b, _) => {
    _.preventDefault(), _.dataTransfer.dropEffect = "move";
    const g = k(_, _.currentTarget);
    h.current != null && window.clearTimeout(h.current), h.current = window.setTimeout(() => {
      t("dragOver", { nodeId: b, position: g }), h.current = null;
    }, 50);
  }, [t, k]), C = e.useCallback((b, _) => {
    _.preventDefault(), h.current != null && (window.clearTimeout(h.current), h.current = null);
    const g = k(_, _.currentTarget);
    t("drop", { nodeId: b, position: g });
  }, [t, k]), R = e.useCallback(() => {
    h.current != null && (window.clearTimeout(h.current), h.current = null), t("dragEnd");
  }, [t]), D = e.useCallback((b) => {
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
          const g = n[a];
          if (g.expandable && !g.expanded) {
            t("expand", { nodeId: g.id });
            return;
          } else g.expanded && (_ = a + 1);
        }
        break;
      case "ArrowLeft":
        if (b.preventDefault(), a >= 0 && a < n.length) {
          const g = n[a];
          if (g.expanded) {
            t("collapse", { nodeId: g.id });
            return;
          } else {
            const Y = g.depth;
            for (let x = a - 1; x >= 0; x--)
              if (n[x].depth < Y) {
                _ = x;
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
          _ === a ? "tlTreeView__node--focused" : "",
          c === b.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          c === b.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          c === b.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: b.depth * Ml },
        draggable: i,
        onClick: (g) => f(b.id, g),
        onContextMenu: (g) => v(b.id, g),
        onDragStart: (g) => E(b.id, g),
        onDragOver: s ? (g) => w(b.id, g) : void 0,
        onDrop: s ? (g) => C(b.id, g) : void 0,
        onDragEnd: R
      },
      b.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (g) => {
            g.stopPropagation(), p(b.id, b.expanded);
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
var gt;
function Bl() {
  if (gt) return Z;
  gt = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), r = Symbol.for("react.strict_mode"), i = Symbol.for("react.profiler"), s = Symbol.for("react.consumer"), c = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), a = Symbol.for("react.suspense"), o = Symbol.for("react.memo"), m = Symbol.for("react.lazy"), p = Symbol.for("react.activity"), f = Symbol.iterator;
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
  }, k = Object.assign, E = {};
  function w(d, S, F) {
    this.props = d, this.context = S, this.refs = E, this.updater = F || h;
  }
  w.prototype.isReactComponent = {}, w.prototype.setState = function(d, S) {
    if (typeof d != "object" && typeof d != "function" && d != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, d, S, "setState");
  }, w.prototype.forceUpdate = function(d) {
    this.updater.enqueueForceUpdate(this, d, "forceUpdate");
  };
  function C() {
  }
  C.prototype = w.prototype;
  function R(d, S, F) {
    this.props = d, this.context = S, this.refs = E, this.updater = F || h;
  }
  var D = R.prototype = new C();
  D.constructor = R, k(D, w.prototype), D.isPureReactComponent = !0;
  var b = Array.isArray;
  function _() {
  }
  var g = { H: null, A: null, T: null, S: null }, Y = Object.prototype.hasOwnProperty;
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
  function T(d, S) {
    return x(d.type, S, d.props);
  }
  function V(d) {
    return typeof d == "object" && d !== null && d.$$typeof === l;
  }
  function A(d) {
    var S = { "=": "=0", ":": "=2" };
    return "$" + d.replace(/[=:]/g, function(F) {
      return S[F];
    });
  }
  var N = /\/+/g;
  function O(d, S) {
    return typeof d == "object" && d !== null && d.key != null ? A("" + d.key) : S.toString(36);
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
  function U(d, S, F, $, G) {
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
              return J = d._init, U(
                J(d._payload),
                S,
                F,
                $,
                G
              );
          }
      }
    if (J)
      return G = G(d), J = $ === "" ? "." + O(d, 0) : $, b(G) ? (F = "", J != null && (F = J.replace(N, "$&/") + "/"), U(G, S, F, "", function(pe) {
        return pe;
      })) : G != null && (V(G) && (G = T(
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
        $ = d[te], j = re + O($, te), J += U(
          $,
          S,
          F,
          j,
          G
        );
    else if (te = v(d), typeof te == "function")
      for (d = te.call(d), te = 0; !($ = d.next()).done; )
        $ = $.value, j = re + O($, te++), J += U(
          $,
          S,
          F,
          j,
          G
        );
    else if (j === "object") {
      if (typeof d.then == "function")
        return U(
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
  function B(d, S, F) {
    if (d == null) return d;
    var $ = [], G = 0;
    return U(d, $, "", "", function(j) {
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
    map: B,
    forEach: function(d, S, F) {
      B(
        d,
        function() {
          S.apply(this, arguments);
        },
        F
      );
    },
    count: function(d) {
      var S = 0;
      return B(d, function() {
        S++;
      }), S;
    },
    toArray: function(d) {
      return B(d, function(S) {
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
  return Z.Activity = p, Z.Children = K, Z.Component = w, Z.Fragment = n, Z.Profiler = i, Z.PureComponent = R, Z.StrictMode = r, Z.Suspense = a, Z.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = g, Z.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(d) {
      return g.H.useMemoCache(d);
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
      $$typeof: c,
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
    var S = g.T, F = {};
    g.T = F;
    try {
      var $ = d(), G = g.S;
      G !== null && G(F, $), typeof $ == "object" && $ !== null && typeof $.then == "function" && $.then(_, P);
    } catch (j) {
      P(j);
    } finally {
      S !== null && F.types !== null && (S.types = F.types), g.T = S;
    }
  }, Z.unstable_useCacheRefresh = function() {
    return g.H.useCacheRefresh();
  }, Z.use = function(d) {
    return g.H.use(d);
  }, Z.useActionState = function(d, S, F) {
    return g.H.useActionState(d, S, F);
  }, Z.useCallback = function(d, S) {
    return g.H.useCallback(d, S);
  }, Z.useContext = function(d) {
    return g.H.useContext(d);
  }, Z.useDebugValue = function() {
  }, Z.useDeferredValue = function(d, S) {
    return g.H.useDeferredValue(d, S);
  }, Z.useEffect = function(d, S) {
    return g.H.useEffect(d, S);
  }, Z.useEffectEvent = function(d) {
    return g.H.useEffectEvent(d);
  }, Z.useId = function() {
    return g.H.useId();
  }, Z.useImperativeHandle = function(d, S, F) {
    return g.H.useImperativeHandle(d, S, F);
  }, Z.useInsertionEffect = function(d, S) {
    return g.H.useInsertionEffect(d, S);
  }, Z.useLayoutEffect = function(d, S) {
    return g.H.useLayoutEffect(d, S);
  }, Z.useMemo = function(d, S) {
    return g.H.useMemo(d, S);
  }, Z.useOptimistic = function(d, S) {
    return g.H.useOptimistic(d, S);
  }, Z.useReducer = function(d, S, F) {
    return g.H.useReducer(d, S, F);
  }, Z.useRef = function(d) {
    return g.H.useRef(d);
  }, Z.useState = function(d) {
    return g.H.useState(d);
  }, Z.useSyncExternalStore = function(d, S, F) {
    return g.H.useSyncExternalStore(
      d,
      S,
      F
    );
  }, Z.useTransition = function() {
    return g.H.useTransition();
  }, Z.version = "19.2.4", Z;
}
var vt;
function Ol() {
  return vt || (vt = 1, Qe.exports = Bl()), Qe.exports;
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
var Et;
function $l() {
  if (Et) return de;
  Et = 1;
  var l = Ol();
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
  function s(a, o, m) {
    var p = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: i,
      key: p == null ? null : "" + p,
      children: a,
      containerInfo: o,
      implementation: m
    };
  }
  var c = l.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
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
    var o = c.T, m = r.p;
    try {
      if (c.T = null, r.p = 2, a) return a();
    } finally {
      c.T = o, r.p = m, r.d.f();
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
    return c.H.useFormState(a, o, m);
  }, de.useFormStatus = function() {
    return c.H.useHostTransitionStatus();
  }, de.version = "19.2.4", de;
}
var Ct;
function Fl() {
  if (Ct) return Ze.exports;
  Ct = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), Ze.exports = $l(), Ze.exports;
}
var xt = Fl();
const { useState: ye, useCallback: me, useRef: Me, useEffect: xe, useMemo: at } = e;
function it({ image: l }) {
  if (!l) return null;
  if (l.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function Hl({
  option: l,
  removable: t,
  onRemove: n,
  removeLabel: r,
  draggable: i,
  onDragStart: s,
  onDragOver: c,
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
      draggable: i || void 0,
      onDragStart: s,
      onDragOver: c,
      onDrop: u,
      onDragEnd: a
    },
    i && /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__dragHandle", "aria-hidden": "true" }, "⋮⋮"),
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
function Ul({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: r,
  onMouseEnter: i,
  id: s
}) {
  const c = me(() => r(l.value), [r, l.value]), u = at(() => {
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
      onClick: c,
      onMouseEnter: i
    },
    /* @__PURE__ */ e.createElement(it, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const Wl = ({ controlId: l, state: t }) => {
  const n = le(), r = t.value ?? [], i = t.multiSelect === !0, s = t.customOrder === !0, c = t.mandatory === !0, u = t.disabled === !0, a = t.editable !== !1, o = t.optionsLoaded === !0, m = t.options ?? [], p = t.emptyOptionLabel ?? "", f = s && i && !u && a, v = ce({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), h = v["js.dropdownSelect.nothingFound"], k = me(
    (L) => v["js.dropdownSelect.removeChip"].replace("{0}", L),
    [v]
  ), [E, w] = ye(!1), [C, R] = ye(""), [D, b] = ye(-1), [_, g] = ye(!1), [Y, x] = ye({}), [T, V] = ye(null), [A, N] = ye(null), [O, Q] = ye(null), U = Me(null), B = Me(null), M = Me(null), P = Me(r);
  P.current = r;
  const K = Me(-1), d = at(
    () => new Set(r.map((L) => L.value)),
    [r]
  ), S = at(() => {
    let L = m.filter((W) => !d.has(W.value));
    if (C) {
      const W = C.toLowerCase();
      L = L.filter((ee) => ee.label.toLowerCase().includes(W));
    }
    return L;
  }, [m, d, C]);
  xe(() => {
    C && S.length === 1 ? b(0) : b(-1);
  }, [S.length, C]), xe(() => {
    E && o && B.current && B.current.focus();
  }, [E, o, r]), xe(() => {
    var ee, oe;
    if (K.current < 0) return;
    const L = K.current;
    K.current = -1;
    const W = (ee = U.current) == null ? void 0 : ee.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    W && W.length > 0 ? W[Math.min(L, W.length - 1)].focus() : (oe = U.current) == null || oe.focus();
  }, [r]), xe(() => {
    if (!E) return;
    const L = (W) => {
      U.current && !U.current.contains(W.target) && M.current && !M.current.contains(W.target) && (w(!1), R(""));
    };
    return document.addEventListener("mousedown", L), () => document.removeEventListener("mousedown", L);
  }, [E]), xe(() => {
    if (!E || !U.current) return;
    const L = U.current.getBoundingClientRect(), W = window.innerHeight - L.bottom, oe = W < 300 && L.top > W;
    x({
      left: L.left,
      width: L.width,
      ...oe ? { bottom: window.innerHeight - L.top } : { top: L.bottom }
    });
  }, [E]);
  const F = me(async () => {
    if (!(u || !a) && (w(!0), R(""), b(-1), g(!1), !o))
      try {
        await n("loadOptions");
      } catch {
        g(!0);
      }
  }, [u, a, o, n]), $ = me(() => {
    var L;
    w(!1), R(""), b(-1), (L = U.current) == null || L.focus();
  }, []), G = me(
    (L) => {
      let W;
      if (i) {
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
      P.current = W, n("valueChanged", { value: W.map((ee) => ee.value) }), i ? (R(""), b(-1)) : $();
    },
    [i, m, n, $]
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
    R(L.target.value);
  }, []), te = me(
    (L) => {
      if (!E) {
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
          C === "" && i && r.length > 0 && j(r[r.length - 1].value);
          break;
      }
    },
    [
      E,
      F,
      $,
      S,
      D,
      G,
      C,
      i,
      r,
      j
    ]
  ), pe = me(
    async (L) => {
      L.preventDefault(), g(!1);
      try {
        await n("loadOptions");
      } catch {
        g(!0);
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
      if (W.preventDefault(), W.dataTransfer.dropEffect = "move", T === null || T === L) {
        N(null), Q(null);
        return;
      }
      const ee = W.currentTarget.getBoundingClientRect(), oe = ee.left + ee.width / 2, y = W.clientX < oe ? "before" : "after";
      N(L), Q(y);
    },
    [T]
  ), Ce = me(
    (L) => {
      if (L.preventDefault(), T === null || A === null || O === null || T === A) return;
      const W = [...P.current], [ee] = W.splice(T, 1);
      let oe = A;
      T < A ? oe = O === "before" ? oe - 1 : oe : oe = O === "before" ? oe : oe + 1, W.splice(oe, 0, ee), P.current = W, n("valueChanged", { value: W.map((y) => y.value) }), V(null), N(null), Q(null);
    },
    [T, A, O, n]
  ), we = me(() => {
    V(null), N(null), Q(null);
  }, []);
  if (xe(() => {
    if (D < 0 || !M.current) return;
    const L = M.current.querySelector(
      `[id="${l}-opt-${D}"]`
    );
    L && L.scrollIntoView({ block: "nearest" });
  }, [D, l]), !a)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, p) : r.map((L) => /* @__PURE__ */ e.createElement("span", { key: L.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(it, { image: L.image }), /* @__PURE__ */ e.createElement("span", null, L.label))));
  const Ne = !c && r.length > 0 && !u, $e = E ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: M,
      className: "tlDropdownSelect__dropdown",
      style: Y
    },
    (o || _) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: B,
        type: "text",
        className: "tlDropdownSelect__search",
        value: C,
        onChange: re,
        onKeyDown: te,
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
      !o && !_ && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      _ && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: pe }, v["js.dropdownSelect.error"])),
      o && S.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, h),
      o && S.map((L, W) => /* @__PURE__ */ e.createElement(
        Ul,
        {
          key: L.value,
          id: `${l}-opt-${W}`,
          option: L,
          highlighted: W === D,
          searchTerm: C,
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
      ref: U,
      className: "tlDropdownSelect" + (E ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": E,
      "aria-haspopup": "listbox",
      "aria-owns": E ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: E ? void 0 : F,
      onKeyDown: te
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : r.map((L, W) => {
      let ee = "";
      return T === W ? ee = "tlDropdownSelect__chip--dragging" : A === W && O === "before" ? ee = "tlDropdownSelect__chip--dropBefore" : A === W && O === "after" && (ee = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        Hl,
        {
          key: L.value,
          option: L,
          removable: !u && (i || !c),
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
        "aria-label": v["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, E ? "▲" : "▼"))
  ), $e && xt.createPortal($e, document.body));
}, { useCallback: Je, useRef: zl } = e, Lt = "application/x-tl-color", Vl = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: r,
  onSwap: i,
  onReplace: s
}) => {
  const c = zl(null), u = Je(
    (m) => (p) => {
      c.current = m, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), a = Je((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), o = Je(
    (m) => (p) => {
      p.preventDefault();
      const f = p.dataTransfer.getData(Lt);
      f ? s(m, f) : c.current !== null && c.current !== m && i(c.current, m), c.current = null;
    },
    [i, s]
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
function It(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function ot(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function Pt(l) {
  if (!ot(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function jt(l, t, n) {
  const r = (i) => It(i).toString(16).padStart(2, "0");
  return "#" + r(l) + r(t) + r(n);
}
function Kl(l, t, n) {
  const r = l / 255, i = t / 255, s = n / 255, c = Math.max(r, i, s), u = Math.min(r, i, s), a = c - u;
  let o = 0;
  a !== 0 && (c === r ? o = (i - s) / a % 6 : c === i ? o = (s - r) / a + 2 : o = (r - i) / a + 4, o *= 60, o < 0 && (o += 360));
  const m = c === 0 ? 0 : a / c;
  return [o, m, c];
}
function Yl(l, t, n) {
  const r = n * t, i = r * (1 - Math.abs(l / 60 % 2 - 1)), s = n - r;
  let c = 0, u = 0, a = 0;
  return l < 60 ? (c = r, u = i, a = 0) : l < 120 ? (c = i, u = r, a = 0) : l < 180 ? (c = 0, u = r, a = i) : l < 240 ? (c = 0, u = i, a = r) : l < 300 ? (c = i, u = 0, a = r) : (c = r, u = 0, a = i), [
    Math.round((c + s) * 255),
    Math.round((u + s) * 255),
    Math.round((a + s) * 255)
  ];
}
function Gl(l) {
  return Kl(...Pt(l));
}
function et(l, t, n) {
  return jt(...Yl(l, t, n));
}
const { useCallback: Le, useRef: wt } = e, Xl = ({ color: l, onColorChange: t }) => {
  const [n, r, i] = Gl(l), s = wt(null), c = wt(null), u = Le(
    (h, k) => {
      var R;
      const E = (R = s.current) == null ? void 0 : R.getBoundingClientRect();
      if (!E) return;
      const w = Math.max(0, Math.min(1, (h - E.left) / E.width)), C = Math.max(0, Math.min(1, 1 - (k - E.top) / E.height));
      t(et(n, w, C));
    },
    [n, t]
  ), a = Le(
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
      var C;
      const k = (C = c.current) == null ? void 0 : C.getBoundingClientRect();
      if (!k) return;
      const w = Math.max(0, Math.min(1, (h - k.top) / k.height)) * 360;
      t(et(w, r, i));
    },
    [r, i, t]
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
        style: { left: `${r * 100}%`, top: `${(1 - i) * 100}%` }
      }
    )
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: c,
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
function ql(l, t) {
  const n = t.toUpperCase();
  return l.some((r) => r != null && r.toUpperCase() === n);
}
const Zl = {
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
}, { useState: We, useCallback: ve, useEffect: yt, useRef: Ql, useLayoutEffect: Jl } = e, er = ({
  anchorRef: l,
  currentColor: t,
  palette: n,
  paletteColumns: r,
  defaultPalette: i,
  canReset: s,
  onConfirm: c,
  onCancel: u,
  onPaletteChange: a
}) => {
  const [o, m] = We("palette"), [p, f] = We(t), v = Ql(null), h = ce(Zl), [k, E] = We(null);
  Jl(() => {
    if (!l.current || !v.current) return;
    const M = l.current.getBoundingClientRect(), P = v.current.getBoundingClientRect();
    let K = M.bottom + 4, d = M.left;
    K + P.height > window.innerHeight && (K = M.top - P.height - 4), d + P.width > window.innerWidth && (d = Math.max(0, M.right - P.width)), E({ top: K, left: d });
  }, [l]);
  const w = p != null, [C, R, D] = w ? Pt(p) : [0, 0, 0], [b, _] = We((p == null ? void 0 : p.toUpperCase()) ?? "");
  yt(() => {
    _((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), Se(!0, { ESCAPE: u }), yt(() => {
    const M = (K) => {
      v.current && !v.current.contains(K.target) && u();
    }, P = setTimeout(() => document.addEventListener("mousedown", M), 0);
    return () => {
      clearTimeout(P), document.removeEventListener("mousedown", M);
    };
  }, [u]);
  const g = ve(
    (M) => (P) => {
      const K = parseInt(P.target.value, 10);
      if (isNaN(K)) return;
      const d = It(K);
      f(jt(M === "r" ? d : C, M === "g" ? d : R, M === "b" ? d : D));
    },
    [C, R, D]
  ), Y = ve(
    (M) => {
      if (p != null) {
        M.dataTransfer.setData(Lt, p.toUpperCase()), M.dataTransfer.effectAllowed = "move";
        const P = document.createElement("div");
        P.style.width = "33px", P.style.height = "33px", P.style.backgroundColor = p, P.style.borderRadius = "3px", P.style.border = "1px solid rgba(0,0,0,0.1)", P.style.position = "absolute", P.style.top = "-9999px", document.body.appendChild(P), M.dataTransfer.setDragImage(P, 16, 16), requestAnimationFrame(() => document.body.removeChild(P));
      }
    },
    [p]
  ), x = ve((M) => {
    const P = M.target.value;
    _(P), ot(P) && f(P);
  }, []), T = ve(() => {
    f(null);
  }, []), V = ve((M) => {
    f(M);
  }, []), A = ve(
    (M) => {
      c(M);
    },
    [c]
  ), N = ve(
    (M, P) => {
      const K = [...n], d = K[M];
      K[M] = K[P], K[P] = d, a(K);
    },
    [n, a]
  ), O = ve(
    (M, P) => {
      const K = [...n];
      K[M] = P, a(K);
    },
    [n, a]
  ), Q = ve(() => {
    a([...i]);
  }, [i, a]), U = ve(
    (M) => {
      if (ql(n, M)) return;
      const P = n.indexOf(null);
      if (P < 0) return;
      const K = [...n];
      K[P] = M.toUpperCase(), a(K);
    },
    [n, a]
  ), B = ve(() => {
    p != null && U(p), c(p);
  }, [p, c, U]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: v,
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
      Vl,
      {
        colors: n,
        columns: r,
        onSelect: V,
        onConfirm: A,
        onSwap: N,
        onReplace: O
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: Q }, h["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(Xl, { color: p ?? "#000000", onColorChange: f }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, h["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, h["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (w ? "" : " tlColorInput--noColor"),
        style: w ? { backgroundColor: p } : void 0,
        draggable: w,
        onDragStart: w ? Y : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: w ? C : "",
        onChange: g("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: w ? R : "",
        onChange: g("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: w ? D : "",
        onChange: g("b")
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
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, s && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: T }, h["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, h["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: B }, h["js.colorInput.ok"]))
  );
}, tr = { "js.colorInput.chooseColor": "Choose color" }, { useState: nr, useCallback: ze, useRef: lr } = e, rr = ({ controlId: l, state: t }) => {
  const n = le(), r = ce(tr), [i, s] = nr(!1), c = lr(null), u = t.value, a = t.editable !== !1, o = t.palette ?? [], m = t.paletteColumns ?? 6, p = t.defaultPalette ?? o, f = ze(() => {
    a && s(!0);
  }, [a]), v = ze(
    (E) => {
      s(!1), n("valueChanged", { value: E });
    },
    [n]
  ), h = ze(() => {
    s(!1);
  }, []), k = ze(
    (E) => {
      n("paletteChanged", { palette: E });
    },
    [n]
  );
  return a ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: c,
      className: "tlColorInput__swatch" + (u == null ? " tlColorInput__swatch--noColor" : ""),
      style: u != null ? { backgroundColor: u } : void 0,
      onClick: f,
      disabled: t.disabled === !0,
      title: u ?? "",
      "aria-label": r["js.colorInput.chooseColor"]
    }
  ), i && /* @__PURE__ */ e.createElement(
    er,
    {
      anchorRef: c,
      currentColor: u,
      palette: o,
      paletteColumns: m,
      defaultPalette: p,
      canReset: t.canReset !== !1,
      onConfirm: v,
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
}, { useState: Ae, useCallback: ke, useEffect: tt, useRef: kt, useLayoutEffect: ar, useMemo: or } = e, sr = {
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
}, cr = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: r,
  onSelect: i,
  onCancel: s,
  onLoadIcons: c
}) => {
  const u = ce(sr), [a, o] = Ae("simple"), [m, p] = Ae(""), [f, v] = Ae(t ?? ""), [h, k] = Ae(!1), [E, w] = Ae(null), C = kt(null), R = kt(null);
  ar(() => {
    if (!l.current || !C.current) return;
    const A = l.current.getBoundingClientRect(), N = C.current.getBoundingClientRect();
    let O = A.bottom + 4, Q = A.left;
    O + N.height > window.innerHeight && (O = A.top - N.height - 4), Q + N.width > window.innerWidth && (Q = Math.max(0, A.right - N.width)), w({ top: O, left: Q });
  }, [l]), tt(() => {
    !r && !h && c().catch(() => k(!0));
  }, [r, h, c]), tt(() => {
    r && R.current && R.current.focus();
  }, [r]), Se(!0, { ESCAPE: s }), tt(() => {
    const A = (O) => {
      C.current && !C.current.contains(O.target) && s();
    }, N = setTimeout(() => document.addEventListener("mousedown", A), 0);
    return () => {
      clearTimeout(N), document.removeEventListener("mousedown", A);
    };
  }, [s]);
  const D = or(() => {
    if (!m) return n;
    const A = m.toLowerCase();
    return n.filter(
      (N) => N.prefix.toLowerCase().includes(A) || N.label.toLowerCase().includes(A) || N.terms != null && N.terms.some((O) => O.includes(A))
    );
  }, [n, m]), b = ke((A) => {
    p(A.target.value);
  }, []), _ = ke(
    (A) => {
      i(A);
    },
    [i]
  ), g = ke((A) => {
    v(A);
  }, []), Y = ke((A) => {
    v(A.target.value);
  }, []), x = ke(() => {
    i(f || null);
  }, [f, i]), T = ke(() => {
    i(null);
  }, [i]), V = ke(async (A) => {
    A.preventDefault(), k(!1);
    try {
      await c();
    } catch {
      k(!0);
    }
  }, [c]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlIconSelect__popup",
      ref: C,
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
        ref: R,
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
      !r && !h && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      h && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: V }, u["js.iconSelect.loadError"])),
      r && D.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      r && D.map(
        (A) => A.variants.map((N) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: N.encoded,
            className: "tlIconSelect__iconCell" + (N.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": N.encoded === t,
            tabIndex: 0,
            title: A.label,
            onClick: () => a === "simple" ? _(N.encoded) : g(N.encoded),
            onKeyDown: (O) => {
              (O.key === "Enter" || O.key === " ") && (O.preventDefault(), a === "simple" ? _(N.encoded) : g(N.encoded));
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
        onChange: Y
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, f && /* @__PURE__ */ e.createElement(Ee, { encoded: f })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, f ? f.startsWith("css:") ? f.substring(4) : f : ""))),
    a === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: s }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: T }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: x }, u["js.iconSelect.ok"]))
  );
}, ir = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: ur, useCallback: Ve, useRef: dr } = e, mr = ({ controlId: l, state: t }) => {
  const n = le(), r = ce(ir), [i, s] = ur(!1), c = dr(null), u = t.value, a = t.editable !== !1, o = t.disabled === !0, m = t.icons ?? [], p = t.iconsLoaded === !0, f = Ve(() => {
    a && !o && s(!0);
  }, [a, o]), v = Ve(
    (E) => {
      s(!1), n("valueChanged", { value: E });
    },
    [n]
  ), h = Ve(() => {
    s(!1);
  }, []), k = Ve(async () => {
    await n("loadIcons");
  }, [n]);
  return a ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: c,
      className: "tlIconSelect__swatch" + (u == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: f,
      disabled: o,
      title: u ?? "",
      "aria-label": r["js.iconSelect.chooseIcon"]
    },
    u ? /* @__PURE__ */ e.createElement(Ee, { encoded: u }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), i && /* @__PURE__ */ e.createElement(
    cr,
    {
      anchorRef: c,
      currentValue: u,
      icons: m,
      iconsLoaded: p,
      onSelect: v,
      onCancel: h,
      onLoadIcons: k
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, u ? /* @__PURE__ */ e.createElement(Ee, { encoded: u }) : null));
}, { useCallback: Ie, useEffect: pr, useMemo: St, useRef: fr, useState: nt } = e, hr = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, br = [1, 2, 3, 4];
function _r(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const r = parseFloat(n[1]), i = n[2] || "px";
  return i === "rem" || i === "em" ? r * t : r;
}
function gr(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let r = 1;
  for (const i of br)
    n >= i && (r = i);
  return r;
}
function vr(l, t) {
  const n = hr[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function Er(l, t) {
  const n = Math.max(1, t), r = {}, i = (p, f) => !!(r[p] && r[p][f]), s = (p, f) => {
    r[p] || (r[p] = {}), r[p][f] = !0;
  }, c = [];
  let u = 0, a = 0;
  const o = (p) => {
    let f = null;
    for (const h of c) h.rowStart === p && (f = h);
    if (!f) return;
    let v = f.colEnd;
    for (; v < n && !i(p, v); ) v++;
    if (v !== f.colEnd) {
      for (let h = f.rowStart; h < f.rowEnd; h++)
        for (let k = f.colEnd; k < v; k++) s(h, k);
      f.colEnd = v;
    }
  };
  for (const p of l) {
    const f = n <= 1 ? 1 : Math.max(1, p.rowSpan || 1);
    let v = Math.min(vr(p.width, n), n);
    for (; i(u, a); )
      a++, a >= n && (a = 0, u++);
    let h = 0;
    for (let R = a; R < n && !i(u, R); R++)
      h++;
    if (v > h) {
      for (o(u), a = 0, u++; i(u, a); )
        a++, a >= n && (a = 0, u++);
      h = 0;
      for (let R = a; R < n && !i(u, R); R++)
        h++;
      v = Math.min(v, h);
    }
    const k = a, E = a + v, w = u, C = u + f;
    c.push({ id: p.id, colStart: k, colEnd: E, rowStart: w, rowEnd: C });
    for (let R = w; R < C; R++)
      for (let D = k; D < E; D++) s(R, D);
    a = E, a >= n && (a = 0, u++);
  }
  o(u);
  let m = 0;
  for (const p of c) p.rowEnd > m && (m = p.rowEnd);
  for (let p = 1; p < m; p++)
    for (let f = 0; f < n; f++) {
      if (i(p, f)) continue;
      const v = c.find((h) => h.rowEnd === p && h.colStart <= f && f < h.colEnd);
      if (v) {
        v.rowEnd = p + 1;
        for (let h = v.colStart; h < v.colEnd; h++) s(p, h);
      }
    }
  return c;
}
const Cr = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.minColWidth ?? "16rem", i = (t.children ?? []).filter((_) => _ && _.id), s = fr(null), [c, u] = nt(1), a = t.editMode === !0;
  pr(() => {
    const _ = s.current;
    if (!_) return;
    const g = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, Y = _r(r, g), x = () => u(gr(_.clientWidth, Y));
    x();
    const T = new ResizeObserver(x);
    return T.observe(_), () => T.disconnect();
  }, [r]);
  const o = St(() => Er(i, c), [i, c]), m = St(() => {
    const _ = {};
    for (const g of o) _[g.id] = g;
    return _;
  }, [o]), [p, f] = nt(null), [v, h] = nt(null), k = Ie((_, g) => {
    if (!a) {
      _.preventDefault();
      return;
    }
    f(g), _.dataTransfer.effectAllowed = "move", _.dataTransfer.setData("text/plain", g);
  }, [a]), E = Ie((_, g) => {
    if (!a || !p || p === g) return;
    _.preventDefault(), _.dataTransfer.dropEffect = "move";
    const Y = _.currentTarget.getBoundingClientRect(), x = _.clientX < Y.left + Y.width / 2;
    h((T) => T && T.id === g && T.before === x ? T : { id: g, before: x });
  }, [a, p]), w = Ie(() => {
  }, []), C = Ie((_, g, Y) => {
    const x = i.map((N) => N.id), T = x.indexOf(_);
    if (T < 0) return;
    x.splice(T, 1);
    const V = x.indexOf(g);
    if (V < 0) {
      x.splice(T, 0, _);
      return;
    }
    const A = Y ? V : V + 1;
    x.splice(A, 0, _), n("reorder", { order: x });
  }, [i, n]), R = Ie((_, g) => {
    if (!a || !p || p === g) return;
    _.preventDefault();
    const Y = _.currentTarget.getBoundingClientRect(), x = _.clientX < Y.left + Y.width / 2;
    C(p, g, x), f(null), h(null);
  }, [a, p, C]), D = Ie(() => {
    f(null), h(null);
  }, []), b = {
    display: "grid",
    gridTemplateColumns: `repeat(${c}, 1fr)`,
    gap: "1rem"
  };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: s,
      className: "tlDashboard" + (a ? " tlDashboard--edit" : "")
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: b }, i.map((_) => {
      const g = m[_.id];
      if (!g) return null;
      const Y = {
        gridColumn: `${g.colStart + 1} / ${g.colEnd + 1}`,
        gridRow: `${g.rowStart + 1} / ${g.rowEnd + 1}`
      }, x = ["tlDashboard__tile"];
      return p === _.id && x.push("tlDashboard__tile--dragging"), v && v.id === _.id && x.push(v.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: _.id,
          className: x.join(" "),
          style: Y,
          draggable: a,
          onDragStart: (T) => k(T, _.id),
          onDragOver: (T) => E(T, _.id),
          onDragLeave: w,
          onDrop: (T) => R(T, _.id),
          onDragEnd: D
        },
        /* @__PURE__ */ e.createElement(z, { control: _.control }),
        a && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: wr, useRef: Nt, useState: Tt, useEffect: yr, useLayoutEffect: kr } = e, Sr = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, r) => /* @__PURE__ */ e.createElement("span", { key: r, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(z, { control: n }))));
}, Nr = ({ group: l }) => {
  var p, f;
  const [t, n] = Tt(!1), [r, i] = Tt({}), s = Nt(null), c = Nt(null), u = wr(() => {
    n((v) => !v);
  }, []);
  kr(() => {
    if (!t) return;
    const v = () => {
      const h = s.current;
      if (!h) return;
      const k = h.getBoundingClientRect();
      i({
        position: "fixed",
        top: k.bottom + 4,
        right: Math.max(8, window.innerWidth - k.right),
        left: "auto"
      });
    };
    return v(), window.addEventListener("resize", v), window.addEventListener("scroll", v, !0), () => {
      window.removeEventListener("resize", v), window.removeEventListener("scroll", v, !0);
    };
  }, [t]), yr(() => {
    if (!t) return;
    const v = (h) => {
      c.current && !c.current.contains(h.target) && s.current && !s.current.contains(h.target) && n(!1);
    };
    return document.addEventListener("mousedown", v), () => document.removeEventListener("mousedown", v);
  }, [t]), Se(t, { ESCAPE: () => n(!1) }), ct(t, c, "first");
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
  ), xt.createPortal(
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: c,
        className: "tlToolbar__dropdown",
        role: "menu",
        hidden: !t,
        style: t ? r : void 0,
        onClick: () => n(!1)
      },
      a.map((v, h) => /* @__PURE__ */ e.createElement("div", { key: h, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(z, { control: v }))),
      (f = l.subGroups) == null ? void 0 : f.map((v, h) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${h}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), v.items.map((k, E) => /* @__PURE__ */ e.createElement("div", { key: E, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(z, { control: k })))))
    ),
    document.body
  ));
}, Tr = ({ controlId: l }) => {
  const r = (q().groups ?? []).filter((i) => i.items.some((s) => s != null));
  return r.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, r.map((i, s) => /* @__PURE__ */ e.createElement(e.Fragment, { key: i.name }, s > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), i.display === "menu" ? /* @__PURE__ */ e.createElement(Nr, { group: i }) : /* @__PURE__ */ e.createElement(Sr, { group: i }))));
}, Rr = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlTileStack", style: { width: "100%", height: "100%" } }, t.frame && /* @__PURE__ */ e.createElement(z, { control: t.frame }));
}, Dr = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.content, i = t.breadcrumb ?? null;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAdaptiveDetail" }, i && i.length > 0 && /* @__PURE__ */ e.createElement("nav", { className: "tlAdaptiveDetail__breadcrumb", "aria-label": "Breadcrumb" }, i.map((s, c) => {
    const u = c === i.length - 1;
    return /* @__PURE__ */ e.createElement(e.Fragment, { key: s.depth }, c > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__sep" }, "›"), u ? /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__crumb tlAdaptiveDetail__crumb--current" }, s.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlAdaptiveDetail__crumb",
        onClick: () => n("navigate", { depth: s.depth })
      },
      s.label
    ));
  })), /* @__PURE__ */ e.createElement("div", { className: "tlAdaptiveDetail__content" }, r && /* @__PURE__ */ e.createElement(z, { control: r })));
}, xr = ({ controlId: l }) => {
  const n = q().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((r, i) => /* @__PURE__ */ e.createElement(z, { key: i, control: r })));
}, Lr = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), Ir = {
  "js.sidebar.openDrawer": "Open navigation"
}, Pr = ({ controlId: l }) => {
  const t = le(), n = ce(Ir);
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
H("TLButton", Xt);
H("TLUploadButton", qt);
H("TLToggleButton", Qt);
H("TLTextInput", Bt);
H("TLPasswordInput", $t);
H("TLNumberInput", Ht);
H("TLDatePicker", Wt);
H("TLSelect", Vt);
H("TLCheckbox", Yt);
H("TLCounter", Jt);
H("TLTabBar", tn);
H("TLFieldList", nn);
H("TLAudioRecorder", rn);
H("TLAudioPlayer", on);
H("TLFileUpload", cn);
H("TLBinaryField", dn);
H("TLDownload", pn);
H("TLPhotoCapture", hn);
H("TLPhotoViewer", _n);
H("TLPdfViewer", vn);
H("TLSplitPanel", En);
H("TLPanel", Tn);
H("TLInset", $n);
H("TLMaximizeRoot", Rn);
H("TLDeckPane", Dn);
H("TLSidebar", Bn);
H("TLStack", On);
H("TLGrid", Fn);
H("TLCard", Hn);
H("TLAppBar", Un);
H("TLBreadcrumb", zn);
H("TLBottomBar", Kn);
H("TLDialog", Xn);
H("TLDialogManager", Qn);
H("TLWindow", nl);
H("TLDrawer", al);
H("TLContextMenuRegion", sl);
H("TLSnackbar", dl);
H("TLMenu", pl);
H("TLAppShell", hl);
H("TLText", bl);
H("TLTableView", vl);
H("TLFormLayout", Nl);
H("TLFormGroup", Dl);
H("TLFormField", Pl);
H("TLResourceCell", jl);
H("TLTreeView", Al);
H("TLDropdownSelect", Wl);
H("TLColorInput", rr);
H("TLIconSelect", mr);
H("TLDashboard", Cr);
H("TLToolbar", Tr);
H("TLTileStack", Rr);
H("TLAdaptiveDetail", Dr);
H("TLSlot", xr);
H("TLSlotContent", Lr);
H("TLDrawerToggle", Pr);
