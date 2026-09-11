import { React as e, useTLFieldValue as De, useTLCommand as le, useTLState as X, useKeyboardBinding as me, useTLUpload as Ye, useFill as wt, FillBarrier as Ae, TLChild as G, useI18N as ue, useTLDataUrl as Ge, scrollToAnchor as sl, useStandaloneKeyboardScope as Fe, useFillHost as at, FillProvider as rt, KeyboardScopeProvider as Yt, useFocusTrap as Gt, CMD_VALUE_CHANGED as ct, anchoredOverlayProps as cl, register as K } from "tl-react-bridge";
const { useCallback: tn, useRef: il } = e, ul = 300, dl = ({ controlId: l, state: t }) => {
  const [n, a, r] = De({
    debounceMs: ul,
    sendOnBlur: t.sendValueOnBlur === !0
  }), i = le(), o = il(!1), u = tn(
    (D) => {
      o.current = !0, a(D.target.value);
    },
    [a]
  ), s = t.commitOnBlur === !0, c = tn(async () => {
    await r(), s && o.current && (o.current = !1, i("commit"));
  }, [r, s, i]), d = t.multiline === !0;
  if (t.editable === !1) {
    const D = "tlReactTextInput tlReactTextInput--immutable" + (d ? " tlReactTextInput--multiline" : "");
    return /* @__PURE__ */ e.createElement(
      "span",
      {
        id: l,
        className: D,
        style: d ? { whiteSpace: "pre-wrap" } : void 0
      },
      n ?? ""
    );
  }
  const m = t.hasError === !0, p = t.hasWarnings === !0, h = t.errorMessage, b = [
    "tlReactTextInput",
    d ? "tlReactTextInput--multiline" : "",
    m ? "tlReactTextInput--error" : "",
    !m && p ? "tlReactTextInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, d ? /* @__PURE__ */ e.createElement(
    "textarea",
    {
      rows: t.rows ?? 3,
      value: n ?? "",
      placeholder: t.placeholder ?? void 0,
      onChange: u,
      onBlur: c,
      disabled: t.disabled === !0,
      className: b,
      "aria-invalid": m || void 0,
      title: m && h ? h : void 0
    }
  ) : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: n ?? "",
      placeholder: t.placeholder ?? void 0,
      onChange: u,
      onBlur: c,
      disabled: t.disabled === !0,
      className: b,
      "aria-invalid": m || void 0,
      title: m && h ? h : void 0
    }
  ));
}, { useCallback: nn } = e, ml = 300, pl = ({ controlId: l, state: t }) => {
  const [n, a, r] = De({ debounceMs: ml }), i = nn(
    (m) => {
      a(m.target.value);
    },
    [a]
  ), o = nn(() => {
    r();
  }, [r]);
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, "••••••••");
  const u = t.hasError === !0, s = t.hasWarnings === !0, c = t.errorMessage, d = [
    "tlReactTextInput",
    u ? "tlReactTextInput--error" : "",
    !u && s ? "tlReactTextInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "password",
      value: n ?? "",
      onChange: i,
      onBlur: o,
      disabled: t.disabled === !0,
      className: d,
      "aria-invalid": u || void 0,
      title: u && c ? c : void 0
    }
  ));
}, { useCallback: ln } = e, fl = 300, hl = ({ controlId: l, state: t }) => {
  const [n, a, r] = De({
    debounceMs: fl,
    sendOnBlur: t.sendValueOnBlur === !0
  }), i = ln(
    (p) => {
      const h = p.target.value;
      a(h === "" ? null : h);
    },
    [a]
  ), o = ln(() => {
    r();
  }, [r]), u = n == null ? "" : String(n);
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, u);
  const s = t.hasError === !0, c = t.hasWarnings === !0, d = t.errorMessage, m = [
    "tlReactNumberInput",
    s ? "tlReactNumberInput--error" : "",
    !s && c ? "tlReactNumberInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      inputMode: t.inputMode ?? "numeric",
      value: u,
      onChange: i,
      onBlur: o,
      disabled: t.disabled === !0,
      className: m,
      "aria-invalid": s || void 0,
      title: s && d ? d : void 0
    }
  ));
}, { useCallback: bl } = e, gl = ({ controlId: l, state: t }) => {
  const [n, a] = De(), r = bl(
    (s) => {
      a(s.target.value || null);
    },
    [a]
  );
  if (t.editable === !1) {
    const s = t.displayValue ?? n ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, s);
  }
  const i = t.hasError === !0, o = t.hasWarnings === !0, u = [
    "tlReactDatePicker",
    i ? "tlReactDatePicker--error" : "",
    !i && o ? "tlReactDatePicker--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: t.inputType ?? "date",
      value: n ?? "",
      onChange: r,
      disabled: t.disabled === !0,
      className: u,
      "aria-invalid": i || void 0
    }
  ));
}, { useCallback: El } = e, vl = ({ controlId: l, state: t, config: n }) => {
  var d;
  const [a, r] = De(), i = El(
    (m) => {
      r(m.target.value || null);
    },
    [r]
  ), o = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const m = ((d = o.find((p) => p.value === a)) == null ? void 0 : d.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, m);
  }
  const u = t.hasError === !0, s = t.hasWarnings === !0, c = [
    "tlReactSelect",
    u ? "tlReactSelect--error" : "",
    !u && s ? "tlReactSelect--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "select",
    {
      value: a ?? "",
      onChange: i,
      disabled: t.disabled === !0,
      className: c,
      "aria-invalid": u || void 0
    },
    t.nullable !== !1 && /* @__PURE__ */ e.createElement("option", { value: "" }),
    o.map((m) => /* @__PURE__ */ e.createElement("option", { key: m.value, value: m.value }, m.label))
  ));
}, { useCallback: _l } = e, Cl = ({ controlId: l, state: t }) => {
  const [n, a] = De(), r = t.options ?? [], i = t.presentation === "select", o = t.disabled === !0, u = t.hasError === !0, s = t.hasWarnings === !0, c = _l(
    (p) => {
      const h = r[p];
      a(h ? h.value : null);
    },
    [r, a]
  ), d = r.findIndex((p) => p.value === (n ?? null));
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlBooleanChoice tlBooleanChoice--immutable" }, d >= 0 ? r[d].label : "");
  const m = [
    "tlBooleanChoice",
    u ? "tlBooleanChoice--error" : "",
    !u && s ? "tlBooleanChoice--warning" : ""
  ].filter(Boolean).join(" ");
  return i ? /* @__PURE__ */ e.createElement(
    "select",
    {
      id: l,
      className: m + " tlReactSelect",
      value: d >= 0 ? String(d) : "",
      disabled: o,
      "aria-invalid": u || void 0,
      onChange: (p) => c(Number(p.target.value))
    },
    d < 0 && /* @__PURE__ */ e.createElement("option", { value: "" }),
    r.map((p, h) => /* @__PURE__ */ e.createElement("option", { key: h, value: String(h) }, p.label))
  ) : /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: m + " tlBooleanChoice--radio",
      role: "radiogroup",
      "aria-invalid": u || void 0
    },
    r.map((p, h) => /* @__PURE__ */ e.createElement("label", { key: h, className: "tlBooleanChoice__option" }, /* @__PURE__ */ e.createElement(
      "input",
      {
        type: "radio",
        name: l,
        checked: d === h,
        disabled: o,
        onChange: () => c(h)
      }
    ), /* @__PURE__ */ e.createElement("span", { className: "tlBooleanChoice__label" }, p.label)))
  );
}, { useCallback: yl, useRef: wl, useEffect: kl } = e, Nl = ({ controlId: l, state: t }) => {
  const [n, a] = De(), r = t.triState === !0, i = wl(null);
  kl(() => {
    i.current && (i.current.indeterminate = r && n !== !0 && n !== !1);
  }, [r, n]);
  const o = yl(
    (d) => {
      if (!r) {
        a(d.target.checked);
        return;
      }
      a(n === !0 ? !1 : n === !1 ? null : !0);
    },
    [a, r, n]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement(
      "input",
      {
        type: "checkbox",
        id: l,
        ref: i,
        checked: n === !0,
        disabled: !0,
        className: "tlReactCheckbox tlReactCheckbox--immutable"
      }
    );
  const u = t.hasError === !0, s = t.hasWarnings === !0, c = [
    "tlReactCheckbox",
    u ? "tlReactCheckbox--error" : "",
    !u && s ? "tlReactCheckbox--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: l,
      ref: i,
      checked: n === !0,
      onChange: o,
      disabled: t.disabled === !0,
      className: c,
      "aria-invalid": u || void 0,
      "aria-checked": r && n !== !0 && n !== !1 ? "mixed" : n === !0
    }
  );
};
function Ne({ encoded: l, className: t }) {
  if (!l || l === "none")
    return null;
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
const { useCallback: Sl } = e, Dl = ({ controlId: l, command: t, label: n, image: a, disabled: r, displayMode: i }) => {
  const o = X(), u = le(), s = t ?? "click", c = n ?? o.label, d = a ?? o.image, m = r ?? o.disabled === !0, p = i ?? o.displayMode ?? "label-only", h = o.hidden === !0, b = o.tooltip, D = o.appearance, y = o.size, E = o.cssClasses, _ = o.navigateUrl, k = Sl(() => {
    if (_) {
      window.location.assign(_);
      return;
    }
    u(s);
  }, [u, s, _]), I = o.keyGesture;
  me(I, () => m || h ? !1 : (k(), !0));
  const w = p === "icon-only", v = p === "label-only" || p === "icon-label" || w && !d, C = b ?? (w ? c : void 0), O = C ? `text:${C}` : void 0;
  return h ? null : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: k,
      disabled: m,
      className: "tlReactButton" + (w ? " tlReactButton--iconOnly" : "") + (p === "label-only" ? " tlReactButton--labelOnly" : "") + (D === "link" ? " tlReactButton--link" : "") + (D === "primary" ? " tlReactButton--primary" : "") + (y === "small" ? " tlReactButton--small" : "") + (y === "large" ? " tlReactButton--large" : "") + (E ? " " + E : ""),
      "data-tooltip": O,
      "aria-label": d || w ? c : void 0
    },
    d && /* @__PURE__ */ e.createElement(Ne, { encoded: d, className: "tlReactButton__image" }),
    v && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, c)
  );
}, Tl = ({ controlId: l }) => {
  const t = X(), n = Ye(), a = e.useRef(null), [r, i] = e.useState(!1), o = t.label ?? "", u = t.image, s = t.disabled === !0, c = t.hidden === !0, d = t.displayMode ?? "label-only", m = t.appearance, p = t.accept, h = t.multiple === !0, b = e.useCallback(() => {
    var I;
    s || r || (I = a.current) == null || I.click();
  }, [s, r]), D = e.useCallback(async (I) => {
    const w = I.target.files;
    if (!w || w.length === 0) return;
    const v = new FormData();
    for (let C = 0; C < w.length; C++)
      v.append("file", w[C], w[C].name);
    I.target.value = "", i(!0);
    try {
      await n(v);
    } finally {
      i(!1);
    }
  }, [n]), y = d === "icon-only", E = d === "icon-only" || d === "icon-label", _ = d === "label-only" || d === "icon-label" || y && !u, k = s || r;
  return /* @__PURE__ */ e.createElement("span", { id: l, style: { display: "contents" } }, /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: a,
      type: "file",
      accept: p && p !== "*" ? p : void 0,
      multiple: h || void 0,
      onChange: D,
      style: { display: "none" }
    }
  ), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: b,
      disabled: k,
      style: c ? { display: "none" } : void 0,
      className: "tlReactButton" + (y ? " tlReactButton--iconOnly" : "") + (m === "link" ? " tlReactButton--link" : "") + (m === "primary" ? " tlReactButton--primary" : ""),
      "aria-label": y ? o : void 0
    },
    E && u && /* @__PURE__ */ e.createElement(Ne, { encoded: u, className: "tlReactButton__image" }),
    _ && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, o)
  ));
}, { useCallback: Rl } = e, Ll = ({ controlId: l, command: t, label: n, active: a, disabled: r }) => {
  const i = X(), o = le(), u = t ?? "click", s = n ?? i.label, c = a ?? i.active === !0, d = r ?? i.disabled === !0, m = Rl(() => {
    o(u);
  }, [o, u]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: m,
      disabled: d,
      className: "tlReactButton" + (c ? " tlReactButtonActive" : "")
    },
    s
  );
}, xl = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.count ?? 0, r = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Ml } = e, Il = ({ controlId: l }) => {
  const t = X(), n = le(), a = wt(!0), r = t.tabs ?? [], i = t.activeTabId, o = Ml((u) => {
    u !== i && n("selectTab", { tabId: u });
  }, [n, i]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar " + a }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, r.map((u) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: u.id,
      role: "tab",
      "aria-selected": u.id === i,
      className: "tlReactTabBar__tab" + (u.id === i ? " tlReactTabBar__tab--active" : ""),
      onClick: () => o(u.id)
    },
    u.icon && /* @__PURE__ */ e.createElement(Ne, { encoded: u.icon, className: "tlReactTabBar__tabIcon" }),
    u.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, /* @__PURE__ */ e.createElement(Ae, null, t.activeContent && /* @__PURE__ */ e.createElement(G, { control: t.activeContent }))));
}, jl = ({ controlId: l }) => {
  const t = X(), n = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((r, i) => /* @__PURE__ */ e.createElement("div", { key: i, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(G, { control: r })))));
}, Al = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Bl = ({ controlId: l }) => {
  const t = X(), n = Ye(), [a, r] = e.useState("idle"), [i, o] = e.useState(null), u = e.useRef(null), s = e.useRef([]), c = e.useRef(null), d = t.status ?? "idle", m = t.error, p = d === "received" ? "idle" : a !== "idle" ? a : d, h = e.useCallback(async () => {
    if (a === "recording") {
      const _ = u.current;
      _ && _.state !== "inactive" && _.stop();
      return;
    }
    if (a !== "uploading") {
      if (o(null), !window.isSecureContext || !navigator.mediaDevices) {
        o("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const _ = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        c.current = _, s.current = [];
        const k = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", I = new MediaRecorder(_, k ? { mimeType: k } : void 0);
        u.current = I, I.ondataavailable = (w) => {
          w.data.size > 0 && s.current.push(w.data);
        }, I.onstop = async () => {
          _.getTracks().forEach((C) => C.stop()), c.current = null;
          const w = new Blob(s.current, { type: I.mimeType || "audio/webm" });
          if (s.current = [], w.size === 0) {
            r("idle");
            return;
          }
          r("uploading");
          const v = new FormData();
          v.append("audio", w, "recording.webm"), await n(v), r("idle");
        }, I.start(), r("recording");
      } catch (_) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", _), o("js.audioRecorder.error.denied"), r("idle");
      }
    }
  }, [a, n]), b = ue(Al), D = p === "recording" ? b["js.audioRecorder.stop"] : p === "uploading" ? b["js.uploading"] : b["js.audioRecorder.record"], y = p === "uploading", E = ["tlAudioRecorder__button"];
  return p === "recording" && E.push("tlAudioRecorder__button--recording"), p === "uploading" && E.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: E.join(" "),
      onClick: h,
      disabled: y,
      title: D,
      "aria-label": D
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${p === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, b[i]), m && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, m));
}, Pl = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Ol = ({ controlId: l }) => {
  const t = X(), n = Ge(), a = !!t.hasAudio, r = t.dataRevision ?? 0, [i, o] = e.useState(a ? "idle" : "disabled"), u = e.useRef(null), s = e.useRef(null), c = e.useRef(r);
  e.useEffect(() => {
    a ? i === "disabled" && o("idle") : (u.current && (u.current.pause(), u.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), o("disabled"));
  }, [a]), e.useEffect(() => {
    r !== c.current && (c.current = r, u.current && (u.current.pause(), u.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), (i === "playing" || i === "paused" || i === "loading") && o("idle"));
  }, [r]), e.useEffect(() => () => {
    u.current && (u.current.pause(), u.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null);
  }, []);
  const d = e.useCallback(async () => {
    if (i === "disabled" || i === "loading")
      return;
    if (i === "playing") {
      u.current && u.current.pause(), o("paused");
      return;
    }
    if (i === "paused" && u.current) {
      u.current.play(), o("playing");
      return;
    }
    if (!s.current) {
      o("loading");
      try {
        const y = await fetch(n);
        if (!y.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", y.status), o("idle");
          return;
        }
        const E = await y.blob();
        s.current = URL.createObjectURL(E);
      } catch (y) {
        console.error("[TLAudioPlayer] Fetch error:", y), o("idle");
        return;
      }
    }
    const D = new Audio(s.current);
    u.current = D, D.onended = () => {
      o("idle");
    }, D.play(), o("playing");
  }, [i, n]), m = ue(Pl), p = i === "loading" ? m["js.loading"] : i === "playing" ? m["js.audioPlayer.pause"] : i === "disabled" ? m["js.audioPlayer.noAudio"] : m["js.audioPlayer.play"], h = i === "disabled" || i === "loading", b = ["tlAudioPlayer__button"];
  return i === "playing" && b.push("tlAudioPlayer__button--playing"), i === "loading" && b.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: b.join(" "),
      onClick: d,
      disabled: h,
      title: p,
      "aria-label": p
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${i === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Fl = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, $l = ({ controlId: l }) => {
  const t = X(), n = Ye(), [a, r] = e.useState("idle"), [i, o] = e.useState(!1), u = e.useRef(null), s = t.status ?? "idle", c = t.error, d = t.accept ?? "", m = s === "received" ? "idle" : a !== "idle" ? a : s, p = e.useCallback(async (w) => {
    r("uploading");
    const v = new FormData();
    v.append("file", w, w.name), await n(v), r("idle");
  }, [n]), h = e.useCallback((w) => {
    var C;
    const v = (C = w.target.files) == null ? void 0 : C[0];
    v && p(v);
  }, [p]), b = e.useCallback(() => {
    var w;
    a !== "uploading" && ((w = u.current) == null || w.click());
  }, [a]), D = e.useCallback((w) => {
    w.preventDefault(), w.stopPropagation(), o(!0);
  }, []), y = e.useCallback((w) => {
    w.preventDefault(), w.stopPropagation(), o(!1);
  }, []), E = e.useCallback((w) => {
    var C;
    if (w.preventDefault(), w.stopPropagation(), o(!1), a === "uploading") return;
    const v = (C = w.dataTransfer.files) == null ? void 0 : C[0];
    v && p(v);
  }, [a, p]), _ = m === "uploading", k = ue(Fl), I = m === "uploading" ? k["js.uploading"] : k["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${i ? " tlFileUpload--dragover" : ""}`,
      onDragOver: D,
      onDragLeave: y,
      onDrop: E
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: u,
        type: "file",
        accept: d || void 0,
        onChange: h,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (m === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: b,
        disabled: _,
        title: I,
        "aria-label": I
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    c && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, c)
  );
}, Hl = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, Wl = ({ controlId: l, state: t }) => {
  const a = X() ?? t ?? {}, r = Ye(), i = Ge(), o = ue(Hl), u = a.editable !== !1, s = !!a.hasData, c = a.fileName ?? "download", d = a.dataRevision ?? 0, m = a.accept ?? "", p = a.status ?? "idle", h = a.error ?? null, [b, D] = e.useState("idle"), [y, E] = e.useState(!1), [_, k] = e.useState(!1), I = e.useRef(null), w = e.useCallback(async () => {
    if (!(!s || _)) {
      k(!0);
      try {
        const P = i + (i.includes("?") ? "&" : "?") + "rev=" + d, L = await fetch(P);
        if (!L.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", L.status);
          return;
        }
        const R = await L.blob(), U = URL.createObjectURL(R), f = document.createElement("a");
        f.href = U, f.download = c, f.style.display = "none", document.body.appendChild(f), f.click(), document.body.removeChild(f), URL.revokeObjectURL(U);
      } catch (P) {
        console.error("[TLBinaryField] Fetch error:", P);
      } finally {
        k(!1);
      }
    }
  }, [s, _, i, d, c]), v = e.useCallback(async (P) => {
    D("uploading");
    const L = new FormData();
    L.append("file", P, P.name), await r(L), D("idle");
  }, [r]), C = (p === "received" ? "idle" : b !== "idle" ? b : p) === "uploading", O = e.useCallback((P) => {
    var R;
    const L = (R = P.target.files) == null ? void 0 : R[0];
    L && v(L);
  }, [v]), F = e.useCallback(() => {
    var P;
    C || (P = I.current) == null || P.click();
  }, [C]), A = e.useCallback((P) => {
    P.preventDefault(), P.stopPropagation(), E(!0);
  }, []), S = e.useCallback((P) => {
    P.preventDefault(), P.stopPropagation(), E(!1);
  }, []), x = e.useCallback((P) => {
    var R;
    if (P.preventDefault(), P.stopPropagation(), E(!1), C) return;
    const L = (R = P.dataTransfer.files) == null ? void 0 : R[0];
    L && v(L);
  }, [C, v]), N = _ ? o["js.downloading"] : o["js.download.file"].replace("{0}", c), H = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (_ ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: w,
      disabled: _,
      title: N,
      "aria-label": N
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: c }, c));
  if (!u)
    return s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, H) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, o["js.download.noFile"]));
  const B = C, j = C ? o["js.uploading"] : o["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlBinaryField tlFileUpload${y ? " tlFileUpload--dragover" : ""}`,
      onDragOver: A,
      onDragLeave: S,
      onDrop: x
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: I,
        type: "file",
        accept: m || void 0,
        onChange: O,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (B ? " tlFileUpload__button--uploading" : ""),
        onClick: F,
        disabled: B,
        title: j,
        "aria-label": j
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    s && H,
    h && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, h)
  );
}, Ul = {
  "js.fileChips.add": "Add file",
  "js.fileChips.remove": "Remove {0}",
  "js.uploading": "Uploading…",
  "js.download.file": "Download {0}"
};
function Vl(l) {
  return l < 1024 ? l + " B" : l < 1024 * 1024 ? (l / 1024).toFixed(l < 10 * 1024 ? 1 : 0) + " KB" : (l / (1024 * 1024)).toFixed(1) + " MB";
}
const zl = ({ controlId: l }) => {
  const t = X(), n = le(), a = Ye(), r = Ge(), i = ue(Ul), o = t.chips ?? [], u = t.editable === !0, [s, c] = e.useState(!1), [d, m] = e.useState(!1), p = e.useRef(null), h = e.useCallback(async (w) => {
    const v = Array.from(w);
    if (v.length !== 0) {
      c(!0);
      try {
        const C = new FormData();
        for (const O of v)
          C.append("file", O, O.name);
        await a(C);
      } finally {
        c(!1);
      }
    }
  }, [a]), b = e.useCallback(async (w) => {
    if (w.hasData)
      try {
        const v = r + "&key=" + encodeURIComponent(w.key), C = await fetch(v);
        if (!C.ok) {
          console.error("[TLFileChips] Failed to fetch data:", C.status);
          return;
        }
        const O = await C.blob(), F = URL.createObjectURL(O), A = document.createElement("a");
        A.href = F, A.download = w.name, A.style.display = "none", document.body.appendChild(A), A.click(), document.body.removeChild(A), URL.revokeObjectURL(F);
      } catch (v) {
        console.error("[TLFileChips] Fetch error:", v);
      }
  }, [r]), D = e.useCallback((w) => {
    w.target.files && h(w.target.files), w.target.value = "";
  }, [h]), y = e.useCallback(() => {
    var w;
    s || (w = p.current) == null || w.click();
  }, [s]), E = e.useCallback((w) => {
    u && (w.preventDefault(), w.stopPropagation(), m(!0));
  }, [u]), _ = e.useCallback((w) => {
    u && (w.preventDefault(), w.stopPropagation(), m(!1));
  }, [u]), k = e.useCallback((w) => {
    u && (w.preventDefault(), w.stopPropagation(), m(!1), !s && w.dataTransfer.files && h(w.dataTransfer.files));
  }, [u, s, h]), I = [
    "tlFileChips",
    u ? "tlFileChips--editable" : "",
    d ? "tlFileChips--dragover" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: I,
      onDragOver: E,
      onDragLeave: _,
      onDrop: k
    },
    o.map((w) => {
      const v = i["js.download.file"].replace("{0}", w.name), C = i["js.fileChips.remove"].replace("{0}", w.name);
      return /* @__PURE__ */ e.createElement("span", { key: w.key, className: "tlFileChip" }, /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__main",
          onClick: () => b(w),
          disabled: !w.hasData,
          title: w.hasData ? v : w.name
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
        w.size != null && /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__size" }, Vl(w.size))
      ), u && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__remove",
          onClick: () => n("removeChip", { key: w.key }),
          title: C,
          "aria-label": C
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
        ref: p,
        type: "file",
        multiple: !0,
        onChange: D,
        style: { display: "none" }
      }
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileChips__add" + (s ? " tlFileChips__add--uploading" : ""),
        onClick: y,
        disabled: s,
        title: s ? i["js.uploading"] : i["js.fileChips.add"]
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
      /* @__PURE__ */ e.createElement("span", null, s ? i["js.uploading"] : i["js.fileChips.add"])
    ))
  );
}, Kl = 3e4;
function Yl(l, t) {
  const n = Math.round((l - Date.now()) / 1e3), a = Math.abs(n), r = new Intl.RelativeTimeFormat(t, { numeric: "auto" });
  return a < 60 ? r.format(Math.trunc(n / 1), "second") : a < 3600 ? r.format(Math.trunc(n / 60), "minute") : a < 86400 ? r.format(Math.trunc(n / 3600), "hour") : a < 7 * 86400 ? r.format(Math.trunc(n / 86400), "day") : new Date(l).toLocaleDateString(t);
}
const Gl = ({ controlId: l }) => {
  const t = X(), n = t.timestamp, a = t.label ?? void 0, r = t.locale || navigator.language, [, i] = e.useState(0);
  return e.useEffect(() => {
    const o = setInterval(() => i((u) => u + 1), Kl);
    return () => clearInterval(o);
  }, []), n == null ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime tlRelativeTime--empty" }) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime", title: a }, Yl(n, r));
}, Xl = ({ controlId: l }) => {
  const t = X(), n = t.anchor ?? void 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAnchor", "data-tl-anchor": n }, t.child && /* @__PURE__ */ e.createElement(G, { control: t.child }));
}, ql = ({ controlId: l }) => {
  const t = X(), n = t.target, a = t.label ?? "";
  if (n == null)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlScrollLink tlScrollLink--empty" });
  const r = (i) => {
    i.preventDefault(), sl(n);
  };
  return /* @__PURE__ */ e.createElement("a", { id: l, className: "tlScrollLink", href: "#", onClick: r }, a);
};
function Zl(l) {
  const t = l.trim().split(/\s+/).filter(Boolean);
  return t.length === 0 ? "?" : t.length === 1 ? t[0].slice(0, 2).toUpperCase() : (t[0][0] + t[t.length - 1][0]).toUpperCase();
}
function Ql(l) {
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return Math.abs(t) % 360;
}
const Jl = ({ controlId: l }) => {
  const n = X().name;
  return n ? /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlAvatar",
      style: { backgroundColor: `hsl(${Ql(n)}, 45%, 45%)` },
      title: n,
      "aria-label": n
    },
    Zl(n)
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlAvatar tlAvatar--empty" });
}, ea = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, ta = ({ controlId: l }) => {
  const t = X(), n = Ge(), a = le(), r = !!t.hasData, i = t.dataRevision ?? 0, o = t.fileName ?? "download", u = !!t.clearable, [s, c] = e.useState(!1), d = e.useCallback(async () => {
    if (!(!r || s)) {
      c(!0);
      try {
        const b = n + (n.includes("?") ? "&" : "?") + "rev=" + i, D = await fetch(b);
        if (!D.ok) {
          console.error("[TLDownload] Failed to fetch data:", D.status);
          return;
        }
        const y = await D.blob(), E = URL.createObjectURL(y), _ = document.createElement("a");
        _.href = E, _.download = o, _.style.display = "none", document.body.appendChild(_), _.click(), document.body.removeChild(_), URL.revokeObjectURL(E);
      } catch (b) {
        console.error("[TLDownload] Fetch error:", b);
      } finally {
        c(!1);
      }
    }
  }, [r, s, n, i, o]), m = e.useCallback(async () => {
    r && await a("clear");
  }, [r, a]), p = ue(ea);
  if (!r)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, p["js.download.noFile"]));
  const h = s ? p["js.downloading"] : p["js.download.file"].replace("{0}", o);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (s ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: d,
      disabled: s,
      title: h,
      "aria-label": h
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o), u && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: m,
      title: p["js.download.clear"],
      "aria-label": p["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, na = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, la = ({ controlId: l }) => {
  const t = X(), n = Ye(), [a, r] = e.useState("idle"), [i, o] = e.useState(null), [u, s] = e.useState(!1), c = e.useRef(null), d = e.useRef(null), m = e.useRef(null), p = e.useRef(null), h = e.useRef(null), b = t.error, D = e.useMemo(
    () => {
      var A;
      return !!(window.isSecureContext && ((A = navigator.mediaDevices) != null && A.getUserMedia));
    },
    []
  ), y = e.useCallback(() => {
    d.current && (d.current.getTracks().forEach((A) => A.stop()), d.current = null), c.current && (c.current.srcObject = null);
  }, []), E = e.useCallback(() => {
    y(), r("idle");
  }, [y]), _ = e.useCallback(async () => {
    var A;
    if (a !== "uploading") {
      if (o(null), !D) {
        (A = p.current) == null || A.click();
        return;
      }
      try {
        const S = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        d.current = S, r("overlayOpen");
      } catch (S) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", S), o("js.photoCapture.error.denied"), r("idle");
      }
    }
  }, [a, D]), k = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const A = c.current, S = m.current;
    if (!A || !S)
      return;
    S.width = A.videoWidth, S.height = A.videoHeight;
    const x = S.getContext("2d");
    x && (x.drawImage(A, 0, 0), y(), r("uploading"), S.toBlob(async (N) => {
      if (!N) {
        r("idle");
        return;
      }
      const H = new FormData();
      H.append("photo", N, "capture.jpg"), await n(H), r("idle");
    }, "image/jpeg", 0.85));
  }, [a, n, y]), I = e.useCallback(async (A) => {
    var N;
    const S = (N = A.target.files) == null ? void 0 : N[0];
    if (!S) return;
    r("uploading");
    const x = new FormData();
    x.append("photo", S, S.name), await n(x), r("idle"), p.current && (p.current.value = "");
  }, [n]);
  e.useEffect(() => {
    a === "overlayOpen" && c.current && d.current && (c.current.srcObject = d.current);
  }, [a]), e.useEffect(() => {
    var S;
    if (a !== "overlayOpen") return;
    (S = h.current) == null || S.focus();
    const A = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = A;
    };
  }, [a]), Fe(a === "overlayOpen", { ESCAPE: E }), e.useEffect(() => () => {
    d.current && (d.current.getTracks().forEach((A) => A.stop()), d.current = null);
  }, []);
  const w = ue(na), v = a === "uploading" ? w["js.uploading"] : w["js.photoCapture.open"], C = ["tlPhotoCapture__cameraBtn"];
  a === "uploading" && C.push("tlPhotoCapture__cameraBtn--uploading");
  const O = ["tlPhotoCapture__overlayVideo"];
  u && O.push("tlPhotoCapture__overlayVideo--mirrored");
  const F = ["tlPhotoCapture__mirrorBtn"];
  return u && F.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: C.join(" "),
      onClick: _,
      disabled: a === "uploading",
      title: v,
      "aria-label": v
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !D && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: p,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: I
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: m, style: { display: "none" } }), a === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: h,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: E }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: c,
        className: O.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: F.join(" "),
        onClick: () => s((A) => !A),
        title: w["js.photoCapture.mirror"],
        "aria-label": w["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: k,
        title: w["js.photoCapture.capture"],
        "aria-label": w["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: E,
        title: w["js.photoCapture.close"],
        "aria-label": w["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, w[i]), b && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, b));
}, aa = {
  "js.photoViewer.alt": "Captured photo"
}, ra = ({ controlId: l }) => {
  const t = X(), n = Ge(), a = !!t.hasPhoto, r = t.dataRevision ?? 0, [i, o] = e.useState(null), u = e.useRef(r);
  e.useEffect(() => {
    if (!a) {
      i && (URL.revokeObjectURL(i), o(null));
      return;
    }
    if (r === u.current && i)
      return;
    u.current = r, i && (URL.revokeObjectURL(i), o(null));
    let c = !1;
    return (async () => {
      try {
        const d = await fetch(n);
        if (!d.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", d.status);
          return;
        }
        const m = await d.blob();
        c || o(URL.createObjectURL(m));
      } catch (d) {
        console.error("[TLPhotoViewer] Fetch error:", d);
      }
    })(), () => {
      c = !0;
    };
  }, [a, r, n]), e.useEffect(() => () => {
    i && URL.revokeObjectURL(i);
  }, []);
  const s = ue(aa);
  return !a || !i ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: i,
      alt: t.alt || s["js.photoViewer.alt"]
    }
  ));
}, oa = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, sa = ({ controlId: l }) => {
  const t = X(), n = Ge(), a = !!t.hasPdf, r = t.dataRevision ?? 0, i = ue(oa), u = n.indexOf("react-api/"), s = u >= 0 ? n.slice(0, u) : n, c = n + "&rev=" + r, d = s + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(c);
  return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: d,
      title: i["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, i["js.pdfViewer.noDocument"]));
}, { useCallback: an, useRef: Tt } = e, ca = ({ controlId: l }) => {
  const t = X(), n = le(), a = wt(!0), r = t.orientation, i = t.resizable === !0, o = t.children ?? [], u = r === "horizontal", s = o.length > 0 && o.every((E) => E.collapsed), c = !s && o.some((E) => E.collapsed), d = s ? !u : u, m = Tt(null), p = Tt(null), h = Tt(null), b = an((E, _) => {
    const k = {
      overflow: E.scrolling || "auto"
    };
    return E.collapsed ? s && !d ? k.flex = "1 0 0%" : k.flex = "0 0 auto" : _ !== void 0 ? k.flex = `0 0 ${_}px` : k.flex = `${E.size} 1 0%`, E.minSize > 0 && !E.collapsed && (k.minWidth = u ? E.minSize : void 0, k.minHeight = u ? void 0 : E.minSize), k;
  }, [u, s, c, d]), D = an((E, _) => {
    E.preventDefault();
    const k = m.current;
    if (!k) return;
    const I = o[_], w = o[_ + 1], v = k.querySelectorAll(":scope > .tlSplitPanel__child"), C = [];
    v.forEach((A) => {
      C.push(u ? A.offsetWidth : A.offsetHeight);
    }), h.current = C, p.current = {
      splitterIndex: _,
      startPos: u ? E.clientX : E.clientY,
      startSizeBefore: C[_],
      startSizeAfter: C[_ + 1],
      childBefore: I,
      childAfter: w
    };
    const O = (A) => {
      const S = p.current;
      if (!S || !h.current) return;
      const N = (u ? A.clientX : A.clientY) - S.startPos, H = S.childBefore.minSize || 0, B = S.childAfter.minSize || 0;
      let j = S.startSizeBefore + N, P = S.startSizeAfter - N;
      j < H && (P += j - H, j = H), P < B && (j += P - B, P = B), h.current[S.splitterIndex] = j, h.current[S.splitterIndex + 1] = P;
      const L = k.querySelectorAll(":scope > .tlSplitPanel__child"), R = L[S.splitterIndex], U = L[S.splitterIndex + 1];
      R && (R.style.flex = `0 0 ${j}px`), U && (U.style.flex = `0 0 ${P}px`);
    }, F = () => {
      if (document.removeEventListener("mousemove", O), document.removeEventListener("mouseup", F), document.body.style.cursor = "", document.body.style.userSelect = "", h.current) {
        const A = {};
        o.forEach((S, x) => {
          const N = S.control;
          N != null && N.controlId && h.current && (A[N.controlId] = h.current[x]);
        }), n("updateSizes", { sizes: A });
      }
      h.current = null, p.current = null;
    };
    document.addEventListener("mousemove", O), document.addEventListener("mouseup", F), document.body.style.cursor = u ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [o, u, n]), y = [];
  return o.forEach((E, _) => {
    if (y.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${_}`,
          className: `tlSplitPanel__child${E.collapsed && d ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: b(E)
        },
        /* @__PURE__ */ e.createElement(G, { control: E.control })
      )
    ), i && _ < o.length - 1) {
      const k = o[_ + 1];
      !E.collapsed && !k.collapsed && y.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${_}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${r}`,
            onMouseDown: (w) => D(w, _)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: m,
      id: l,
      className: `tlSplitPanel tlSplitPanel--${r}${s ? " tlSplitPanel--allCollapsed" : ""} ${a}`,
      style: {
        display: "flex",
        flexDirection: d ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    y
  );
}, Ft = ({ image: l, className: t }) => {
  if (!l || l === "none") return null;
  const n = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `${t ? t + " " : ""}${n}`, "aria-hidden": "true" });
}, { useCallback: Rt } = e, ia = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, ua = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), da = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), ma = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), pa = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), fa = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), ha = ({ controlId: l }) => {
  const t = X(), n = le(), a = ue(ia), r = t.title, i = t.expansionState ?? "NORMALIZED", o = t.showMinimize === !0, u = t.showMaximize === !0, s = t.showPopOut === !0, c = t.fullLine === !0, d = t.fill === !0, m = t.hoverActions === !0, p = t.appearance === "card", h = t.errorMessage, b = i === "MINIMIZED", D = i === "MAXIMIZED", y = i === "HIDDEN", E = Rt(() => {
    n("toggleMinimize");
  }, [n]), _ = Rt(() => {
    n("toggleMaximize");
  }, [n]), k = Rt(() => {
    n("popOut");
  }, [n]), I = wt(d && !y);
  if (y)
    return null;
  const w = D ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" }, v = o && !D || u && !b || s, C = !!r && r.trim() !== "" || !!t.titleContent || !!t.toolbar || v;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${i.toLowerCase()}${c ? " tlPanel--fullLine" : ""}${I ? " " + I : ""}${m ? " tlPanel--hoverActions" : ""}${p ? " tlPanel--card" : ""}`,
      style: w
    },
    C && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, !!r && r.trim() !== "" && /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, r), t.titleContent && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__titleContent" }, /* @__PURE__ */ e.createElement(G, { control: t.titleContent })), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(G, { control: t.toolbar }), o && !D && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: E,
        title: b ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      b ? /* @__PURE__ */ e.createElement(da, null) : /* @__PURE__ */ e.createElement(ua, null)
    ), u && !b && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: _,
        title: D ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      D ? /* @__PURE__ */ e.createElement(pa, null) : /* @__PURE__ */ e.createElement(ma, null)
    ), s && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: k,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(fa, null)
    ))),
    !b && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(Ae, null, /* @__PURE__ */ e.createElement(G, { control: t.child }))),
    !b && h && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error tlPanel__error", role: "alert" }, /* @__PURE__ */ e.createElement(Ft, { image: t.errorIcon, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, h)),
    !b && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(G, { control: t.buttonBar }))
  );
}, ba = ({ controlId: l }) => {
  const t = X();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(G, { control: t.child })
  );
}, ga = ({ controlId: l }) => {
  const t = X(), [n, a] = at();
  return /* @__PURE__ */ e.createElement(rt, { host: a }, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: n ? "tlDeckPane " + n : "tlDeckPane",
      style: { width: "100%", height: "100%" }
    },
    t.activeChild && /* @__PURE__ */ e.createElement(G, { control: t.activeChild })
  ));
}, { useCallback: ve, useState: gt, useEffect: $t, useRef: vt } = e, Ea = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function Ht(l, t, n, a) {
  const r = [];
  for (const i of l)
    if (i.type === "nav") {
      if (i.hidden) continue;
      r.push({ id: i.id, type: "nav", groupId: a });
    } else i.type === "command" ? r.push({ id: i.id, type: "command", groupId: a }) : i.type === "group" && (r.push({ id: i.id, type: "group" }), (n.get(i.id) ?? i.expanded) && !t && r.push(...Ht(i.children, t, n, i.id)));
  return r;
}
const Ke = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(Ne, { encoded: l, className: "tlSidebar__icon" }) : null, va = ({ item: l, active: t, collapsed: n, onSelect: a, tabIndex: r, itemRef: i, onFocus: o }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => a(l.id),
    title: n ? l.label : void 0,
    tabIndex: r,
    ref: i,
    onFocus: () => o(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Ke, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Ke, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), _a = ({ item: l, collapsed: t, onExecute: n, tabIndex: a, itemRef: r, onFocus: i }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: a,
    ref: r,
    onFocus: () => i(l.id)
  },
  /* @__PURE__ */ e.createElement(Ke, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), Ca = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Ke, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), ya = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), wa = ({ item: l, activeItemId: t, anchorRect: n, onSelect: a, onExecute: r, onClose: i }) => {
  const o = vt(null);
  $t(() => {
    const c = (d) => {
      o.current && !o.current.contains(d.target) && setTimeout(() => i(), 0);
    };
    return document.addEventListener("mousedown", c), () => document.removeEventListener("mousedown", c);
  }, [i]), Fe(!0, { ESCAPE: i });
  const u = ve((c) => {
    c.type === "nav" ? (a(c.id), i()) : c.type === "command" && (r(c.id), i());
  }, [a, r, i]), s = {};
  return n && (s.left = n.right, s.top = n.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: o, role: "menu", style: s }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((c) => {
    if (c.type === "nav" && c.hidden) return null;
    if (c.type === "nav" || c.type === "command") {
      const d = c.type === "nav" && c.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: c.id,
          className: "tlSidebar__flyoutItem" + (d ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => u(c)
        },
        /* @__PURE__ */ e.createElement(Ke, { icon: c.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, c.label),
        c.type === "nav" && c.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, c.badge)
      );
    }
    return c.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: c.id, className: "tlSidebar__flyoutSectionHeader" }, c.label) : c.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: c.id, className: "tlSidebar__separator" }) : null;
  }));
}, ka = ({
  item: l,
  expanded: t,
  activeItemId: n,
  collapsed: a,
  onSelect: r,
  onExecute: i,
  onToggleGroup: o,
  tabIndex: u,
  itemRef: s,
  onFocus: c,
  focusedId: d,
  setItemRef: m,
  onItemFocus: p,
  flyoutGroupId: h,
  onOpenFlyout: b,
  onCloseFlyout: D
}) => {
  const y = vt(null), [E, _] = gt(null), k = ve(() => {
    a ? h === l.id ? D() : (y.current && _(y.current.getBoundingClientRect()), b(l.id)) : o(l.id);
  }, [a, h, l.id, o, b, D]), I = ve((v) => {
    y.current = v, s(v);
  }, [s]), w = a && h === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (w ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: k,
      title: a ? l.label : void 0,
      "aria-expanded": a ? w : t,
      tabIndex: u,
      ref: I,
      onFocus: () => c(l.id)
    },
    /* @__PURE__ */ e.createElement(Ke, { icon: l.icon }),
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
    wa,
    {
      item: l,
      activeItemId: n,
      anchorRect: E,
      onSelect: r,
      onExecute: i,
      onClose: D
    }
  ), t && !a && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((v) => /* @__PURE__ */ e.createElement(
    Dn,
    {
      key: v.id,
      item: v,
      activeItemId: n,
      collapsed: a,
      onSelect: r,
      onExecute: i,
      onToggleGroup: o,
      focusedId: d,
      setItemRef: m,
      onItemFocus: p,
      groupStates: null,
      flyoutGroupId: h,
      onOpenFlyout: b,
      onCloseFlyout: D
    }
  ))));
}, Dn = ({
  item: l,
  activeItemId: t,
  collapsed: n,
  onSelect: a,
  onExecute: r,
  onToggleGroup: i,
  focusedId: o,
  setItemRef: u,
  onItemFocus: s,
  groupStates: c,
  flyoutGroupId: d,
  onOpenFlyout: m,
  onCloseFlyout: p
}) => {
  switch (l.type) {
    case "nav":
      return l.hidden ? null : /* @__PURE__ */ e.createElement(
        va,
        {
          item: l,
          active: l.id === t,
          collapsed: n,
          onSelect: a,
          tabIndex: o === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: s
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        _a,
        {
          item: l,
          collapsed: n,
          onExecute: r,
          tabIndex: o === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: s
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(Ca, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(ya, null);
    case "group": {
      const h = c ? c.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        ka,
        {
          item: l,
          expanded: h,
          activeItemId: t,
          collapsed: n,
          onSelect: a,
          onExecute: r,
          onToggleGroup: i,
          tabIndex: o === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: s,
          focusedId: o,
          setItemRef: u,
          onItemFocus: s,
          flyoutGroupId: d,
          onOpenFlyout: m,
          onCloseFlyout: p
        }
      );
    }
    default:
      return null;
  }
}, Na = ({ controlId: l }) => {
  const t = X(), n = le(), a = ue(Ea), r = t.items ?? [], i = t.activeItemId, o = t.collapsed, u = t.drawerOpen, s = u ? !1 : o, [c, d] = gt(() => {
    const N = /* @__PURE__ */ new Map(), H = (B) => {
      for (const j of B)
        j.type === "group" && (N.set(j.id, j.expanded), H(j.children));
    };
    return H(r), N;
  }), m = ve((N) => {
    d((H) => {
      const B = new Map(H), j = B.get(N) ?? !1;
      return B.set(N, !j), n("toggleGroup", { itemId: N, expanded: !j }), B;
    });
  }, [n]), p = ve((N) => {
    N !== i && n("selectItem", { itemId: N });
  }, [n, i]), h = ve((N) => {
    n("executeCommand", { itemId: N });
  }, [n]), b = ve(() => {
    n("toggleCollapse", {});
  }, [n]), D = ve(() => {
    n("toggleDrawer", {});
  }, [n]), [y, E] = gt(null), _ = ve((N) => {
    E(N);
  }, []), k = ve(() => {
    E(null);
  }, []);
  $t(() => {
    s || E(null);
  }, [s]);
  const [I, w] = gt(() => {
    const N = Ht(r, s, c);
    return N.length > 0 ? N[0].id : "";
  }), v = vt(/* @__PURE__ */ new Map()), C = ve((N) => (H) => {
    H ? v.current.set(N, H) : v.current.delete(N);
  }, []), O = ve((N) => {
    w(N);
  }, []), F = vt(0), A = ve((N) => {
    w(N), F.current++;
  }, []);
  $t(() => {
    const N = v.current.get(I);
    N && document.activeElement !== N && N.focus();
  }, [I, F.current]);
  const S = ve((N) => {
    if (N.key === "Escape" && y !== null) {
      N.preventDefault(), k();
      return;
    }
    const H = Ht(r, s, c);
    if (H.length === 0) return;
    const B = H.findIndex((P) => P.id === I);
    if (B < 0) return;
    const j = H[B];
    switch (N.key) {
      case "ArrowDown": {
        N.preventDefault();
        const P = (B + 1) % H.length;
        A(H[P].id);
        break;
      }
      case "ArrowUp": {
        N.preventDefault();
        const P = (B - 1 + H.length) % H.length;
        A(H[P].id);
        break;
      }
      case "Home": {
        N.preventDefault(), A(H[0].id);
        break;
      }
      case "End": {
        N.preventDefault(), A(H[H.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        N.preventDefault(), j.type === "nav" ? p(j.id) : j.type === "command" ? h(j.id) : j.type === "group" && (s ? y === j.id ? k() : _(j.id) : m(j.id));
        break;
      }
      case "ArrowRight": {
        j.type === "group" && !s && ((c.get(j.id) ?? !1) || (N.preventDefault(), m(j.id)));
        break;
      }
      case "ArrowLeft": {
        j.type === "group" && !s && (c.get(j.id) ?? !1) && (N.preventDefault(), m(j.id));
        break;
      }
    }
  }, [
    r,
    s,
    c,
    I,
    y,
    A,
    p,
    h,
    m,
    _,
    k
  ]), x = "tlSidebar" + (s ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: x }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(G, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: D, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": a["js.sidebar.ariaLabel"] }, s ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(G, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(G, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: S }, r.map((N) => /* @__PURE__ */ e.createElement(
    Dn,
    {
      key: N.id,
      item: N,
      activeItemId: i,
      collapsed: s,
      onSelect: p,
      onExecute: h,
      onToggleGroup: m,
      focusedId: I,
      setItemRef: C,
      onItemFocus: O,
      groupStates: c,
      flyoutGroupId: y,
      onOpenFlyout: _,
      onCloseFlyout: k
    }
  ))), s ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(G, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(G, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: b,
      title: s ? a["js.sidebar.expand"] : a["js.sidebar.collapse"]
    },
    /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement(
      "path",
      {
        d: s ? "M6 4l4 4-4 4" : "M10 4l-4 4 4 4",
        fill: "none",
        stroke: "currentColor",
        strokeWidth: "2",
        strokeLinecap: "round",
        strokeLinejoin: "round"
      }
    ))
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, /* @__PURE__ */ e.createElement(Ae, null, t.activeContent && /* @__PURE__ */ e.createElement(G, { control: t.activeContent }))));
}, Sa = ({ controlId: l }) => {
  const t = X(), n = t.direction ?? "column", a = t.gap ?? "default", r = t.align ?? "stretch", i = t.wrap === !0, o = t.growFirst === !0, u = t.children ?? [], [s, c] = at(), d = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${a}`,
    `tlStack--align-${r}`,
    i ? "tlStack--wrap" : "",
    o ? "tlStack--grow-first" : "",
    s,
    t.cssClass ?? ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(rt, { host: c }, /* @__PURE__ */ e.createElement("div", { id: l, className: d }, u.map((m, p) => /* @__PURE__ */ e.createElement(G, { key: p, control: m }))));
}, Da = ({ controlId: l }) => {
  const t = X(), [n, a] = at();
  return /* @__PURE__ */ e.createElement(rt, { host: a }, /* @__PURE__ */ e.createElement("div", { id: l, className: n ? "tlInset " + n : "tlInset" }, t.child && /* @__PURE__ */ e.createElement(G, { control: t.child })));
}, Ta = ({ controlId: l }) => {
  const t = X(), n = t.columns, a = t.minColumnWidth, r = t.gap ?? "default", i = t.children ?? [], o = {};
  return a ? o.gridTemplateColumns = `repeat(auto-fit, minmax(min(${a}, 100%), 1fr))` : n && (o.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${r}`, style: o }, i.map((u, s) => /* @__PURE__ */ e.createElement(G, { key: s, control: u })));
}, Ra = ({ controlId: l }) => {
  const t = X(), n = t.title, a = t.variant ?? "outlined", r = t.padding ?? "default", i = t.headerActions ?? [], o = t.child, u = n != null || i.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${a}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, i.map((s, c) => /* @__PURE__ */ e.createElement(G, { key: c, control: s })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${r}` }, /* @__PURE__ */ e.createElement(Ae, null, /* @__PURE__ */ e.createElement(G, { control: o }))));
}, La = ({ controlId: l }) => {
  const t = X(), n = t.title ?? "", a = t.leading, r = t.trailing, i = t.children ?? [], o = t.actions ?? [], u = t.variant ?? "flat", c = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    u === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: c }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(G, { control: a })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, i.map((d, m) => /* @__PURE__ */ e.createElement(G, { key: m, control: d }))), o.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, o.map((d, m) => /* @__PURE__ */ e.createElement(G, { key: m, control: d }))), r && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__trailing" }, /* @__PURE__ */ e.createElement(G, { control: r })));
}, { useCallback: xa } = e, Ma = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.items ?? [], r = xa((i) => {
    n("navigate", { itemId: i });
  }, [n]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, a.map((i, o) => {
    const u = o === a.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: i.id, className: "tlBreadcrumb__entry" }, o > 0 && /* @__PURE__ */ e.createElement(
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
    ), u ? /* @__PURE__ */ e.createElement("span", { className: "tlBreadcrumb__current", "aria-current": "page" }, i.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlBreadcrumb__item",
        onClick: () => r(i.id)
      },
      i.label
    ));
  })));
}, { useCallback: Ia } = e, ja = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.items ?? [], r = t.activeItemId, i = Ia((o) => {
    o !== r && n("selectItem", { itemId: o });
  }, [n, r]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, a.map((o) => {
    const u = o.id === r;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: o.id,
        type: "button",
        className: "tlBottomBar__item" + (u ? " tlBottomBar__item--active" : ""),
        onClick: () => i(o.id),
        "aria-current": u ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + o.icon, "aria-hidden": "true" }), o.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, o.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, o.label)
    );
  }));
}, { useCallback: rn, useRef: Aa } = e, Ba = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), Pa = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.open === !0, r = t.closeOnBackdrop !== !1, i = t.child, o = Aa(null), u = rn(() => {
    n("close");
  }, [n]), s = rn((c) => {
    r && c.target === c.currentTarget && u();
  }, [r, u]);
  return a ? /* @__PURE__ */ e.createElement(Yt, null, /* @__PURE__ */ e.createElement(Ba, { onClose: u }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: s,
      ref: o,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(Ae, null, /* @__PURE__ */ e.createElement(G, { control: i }))
  )) : null;
}, { useEffect: Oa, useRef: Fa } = e, $a = ({ controlId: l }) => {
  const n = X().dialogs ?? [], a = Fa(n.length);
  return Oa(() => {
    n.length < a.current && n.length > 0, a.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((r) => /* @__PURE__ */ e.createElement(G, { key: r.controlId, control: r })));
}, { useCallback: it, useRef: He, useState: ut } = e, Ha = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), Wa = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Ua = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Va = ({ controlId: l }) => {
  const t = X(), n = le(), a = ue(Wa), r = t.title ?? "", i = t.width ?? "32rem", o = t.height ?? null, u = t.minHeight ?? null, s = t.resizable === !0, c = t.child, d = t.actions ?? [], m = t.toolbar, p = t.buttonBar, [h, b] = ut(null), [D, y] = ut(null), [E, _] = ut(null), k = He(null), [I, w] = ut(!1), v = He(null), C = He(null), O = He(null), F = He(null), A = He(null), S = it(() => {
    n("close");
  }, [n]);
  Gt(!0, F, "field");
  const x = it((P, L) => {
    L.preventDefault();
    const R = F.current;
    if (!R) return;
    const U = R.getBoundingClientRect(), f = !k.current, M = k.current ?? { x: U.left, y: U.top };
    f && (k.current = M, _(M)), A.current = {
      dir: P,
      startX: L.clientX,
      startY: L.clientY,
      startW: U.width,
      startH: U.height,
      startPos: { ...M },
      symmetric: f
    };
    const Y = (Q) => {
      const W = A.current;
      if (!W) return;
      const J = Q.clientX - W.startX, se = Q.clientY - W.startY;
      let ne = W.startW, Ee = W.startH, be = 0, we = 0;
      W.symmetric ? (W.dir.includes("e") && (ne = W.startW + 2 * J), W.dir.includes("w") && (ne = W.startW - 2 * J), W.dir.includes("s") && (Ee = W.startH + 2 * se), W.dir.includes("n") && (Ee = W.startH - 2 * se)) : (W.dir.includes("e") && (ne = W.startW + J), W.dir.includes("w") && (ne = W.startW - J, be = J), W.dir.includes("s") && (Ee = W.startH + se), W.dir.includes("n") && (Ee = W.startH - se, we = se));
      const Te = Math.max(200, ne), Re = Math.max(100, Ee);
      W.symmetric ? (be = (W.startW - Te) / 2, we = (W.startH - Re) / 2) : (W.dir.includes("w") && Te === 200 && (be = W.startW - 200), W.dir.includes("n") && Re === 100 && (we = W.startH - 100)), C.current = Te, O.current = Re, b(Te), y(Re);
      const $e = {
        x: W.startPos.x + be,
        y: W.startPos.y + we
      };
      k.current = $e, _($e);
    }, z = () => {
      document.removeEventListener("mousemove", Y), document.removeEventListener("mouseup", z);
      const Q = C.current, W = O.current;
      (Q != null || W != null) && n("resize", {
        ...Q != null ? { width: Math.round(Q) } : {},
        ...W != null ? { height: Math.round(W) } : {}
      }), A.current = null;
    };
    document.addEventListener("mousemove", Y), document.addEventListener("mouseup", z);
  }, [n]), N = it((P) => {
    if (P.button !== 0 || P.target.closest("button")) return;
    P.preventDefault();
    const L = F.current;
    if (!L) return;
    const R = L.getBoundingClientRect(), U = k.current ?? { x: R.left, y: R.top }, f = P.clientX - U.x, M = P.clientY - U.y, Y = (Q) => {
      const W = window.innerWidth, J = window.innerHeight;
      let se = Q.clientX - f, ne = Q.clientY - M;
      const Ee = L.offsetWidth, be = L.offsetHeight;
      se + Ee > W && (se = W - Ee), ne + be > J && (ne = J - be), se < 0 && (se = 0), ne < 0 && (ne = 0);
      const we = { x: se, y: ne };
      k.current = we, _(we);
    }, z = () => {
      document.removeEventListener("mousemove", Y), document.removeEventListener("mouseup", z);
    };
    document.addEventListener("mousemove", Y), document.addEventListener("mouseup", z);
  }, []), H = it(() => {
    var P, L;
    if (I) {
      const R = v.current;
      R && (_(R.x !== -1 ? { x: R.x, y: R.y } : null), b(R.w), y(R.h)), w(!1);
    } else {
      const R = F.current, U = R == null ? void 0 : R.getBoundingClientRect();
      v.current = {
        x: ((P = k.current) == null ? void 0 : P.x) ?? (U == null ? void 0 : U.left) ?? -1,
        y: ((L = k.current) == null ? void 0 : L.y) ?? (U == null ? void 0 : U.top) ?? -1,
        w: h ?? (U == null ? void 0 : U.width) ?? null,
        h: D ?? null
      }, w(!0), _({ x: 0, y: 0 }), b(null), y(null);
    }
  }, [I, h, D]), B = I ? { position: "absolute", top: 0, left: 0, width: "100vw", maxWidth: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: h != null ? h + "px" : i,
    ...D != null ? { height: D + "px" } : o != null ? { height: o } : {},
    ...u != null && D == null ? { minHeight: u } : {},
    maxHeight: E ? "100vh" : "80vh",
    ...E ? { position: "absolute", left: E.x + "px", top: E.y + "px" } : {}
  }, j = l + "-title";
  return /* @__PURE__ */ e.createElement(Yt, { modal: !0 }, /* @__PURE__ */ e.createElement(Ha, { onClose: S }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: B,
      ref: F,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": j
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${I ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: I ? void 0 : N,
        onDoubleClick: s ? H : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: j }, r),
      m && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(G, { control: m })),
      s && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: H,
          title: I ? a["js.window.restore"] : a["js.window.maximize"]
        },
        I ? (
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
          onClick: S,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(Ae, null, /* @__PURE__ */ e.createElement(G, { control: c }))),
    (d.length > 0 || p) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, p && /* @__PURE__ */ e.createElement(G, { control: p }), d.map((P, L) => /* @__PURE__ */ e.createElement(G, { key: L, control: P }))),
    s && !I && Ua.map((P) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: P,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${P}`,
        onMouseDown: (L) => x(P, L)
      }
    ))
  ));
}, { useCallback: za } = e, Ka = {
  "js.drawer.close": "Close"
}, Ya = ({ controlId: l }) => {
  const t = X(), n = le(), a = ue(Ka), r = t.open === !0, i = t.position ?? "right", o = t.size ?? "medium", u = t.title ?? null, s = t.child, c = za(() => {
    n("close");
  }, [n]);
  Fe(r, { ESCAPE: c });
  const d = [
    "tlDrawer",
    `tlDrawer--${i}`,
    `tlDrawer--${o}`,
    r ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: l, className: d, "aria-hidden": !r }, u !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, u), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: c,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, /* @__PURE__ */ e.createElement(Ae, null, s && /* @__PURE__ */ e.createElement(G, { control: s }))));
}, { useCallback: dt, useRef: Ga } = e, Xa = ({ controlId: l }) => {
  const t = X(), n = le(), a = Ga(null), r = t.child, o = (t.trigger ?? "contextmenu") === "click", u = dt((m) => {
    m.preventDefault(), m.stopPropagation(), n("openMenu", { x: m.clientX, y: m.clientY });
  }, [n]), s = dt(() => {
    var p;
    const m = (p = a.current) == null ? void 0 : p.getBoundingClientRect();
    n("openMenu", {
      x: Math.round(m ? m.left : 0),
      y: Math.round(m ? m.bottom : 0)
    });
  }, [n]), c = dt((m) => {
    m.preventDefault(), m.stopPropagation(), s();
  }, [s]), d = dt((m) => {
    (m.key === "Enter" || m.key === " ") && (m.preventDefault(), s());
  }, [s]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenuRegion" + (o ? " tlMenuRegion--click" : ""),
      ref: a,
      onContextMenu: o ? void 0 : u,
      onClick: o ? c : void 0,
      role: o ? "button" : void 0,
      tabIndex: o ? 0 : void 0,
      "aria-haspopup": o ? "menu" : void 0,
      onKeyDown: o ? d : void 0
    },
    r && /* @__PURE__ */ e.createElement(G, { control: r })
  );
}, { useCallback: qa, useEffect: on, useRef: Za, useState: sn } = e, Qa = 250, Ja = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.message ?? "", r = t.content ?? "", i = t.variant ?? "info", o = t.duration ?? 5e3, u = t.visible === !0, s = t.generation ?? 0, [c, d] = sn(!1), [m, p] = sn(!1), h = Za(!1);
  on(() => {
    h.current = !1;
  }, [s]);
  const b = qa(() => {
    d(!0), setTimeout(() => {
      n("dismiss", { generation: s }), d(!1);
    }, 200);
  }, [n, s]);
  return on(() => {
    if (!u || o === 0 || m) return;
    const D = setTimeout(b, h.current ? Qa : o);
    return () => clearTimeout(D);
  }, [u, o, m, b]), !u && !c ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${i}${c ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite",
      onMouseEnter: () => {
        h.current = !0, p(!0);
      },
      onMouseLeave: () => p(!1)
    },
    r ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: r } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, a)
  );
}, { useCallback: er, useEffect: cn, useMemo: tr, useRef: nr, useState: lr } = e, ar = 1e3;
function rr(l) {
  const t = Math.max(0, Math.floor(l / 1e3)), n = t % 60, a = Math.floor(t / 60) % 60, r = Math.floor(t / 3600), i = (o) => o < 10 ? `0${o}` : `${o}`;
  return r > 0 ? `${r}:${i(a)}:${i(n)}` : `${a}:${i(n)}`;
}
const or = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.visible === !0, r = t.severity ?? "info", i = t.text ?? "", o = t.deadline ?? null, u = t.serverNow ?? null, s = t.leadMs ?? null, c = t.actionLabel ?? null, d = t.pingGraceMs ?? null, m = tr(
    () => u != null ? u - Date.now() : 0,
    [u]
  ), [p, h] = lr(0), b = a && o != null;
  cn(() => {
    if (!b) return;
    const I = setInterval(() => h((w) => w + 1), ar);
    return () => clearInterval(I);
  }, [b, o]);
  const D = nr(null);
  cn(() => {
    !b || d == null || o == null || D.current !== o && (Date.now() + m < o + d || (D.current = o, n("deadlinePassed", {})));
  }, [p, b, o, d, m, n]);
  const y = er(() => {
    c != null && n("action", {});
  }, [n, c]);
  if (!a) return null;
  const E = o != null ? o - (Date.now() + m) : null;
  if (s != null && E != null && E > s) return null;
  const _ = E != null ? rr(E) : null, k = c != null;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlNoticeBar tlNoticeBar--${r}${k ? " tlNoticeBar--clickable" : ""}`,
      role: k ? "button" : "status",
      "aria-live": "polite",
      tabIndex: k ? 0 : void 0,
      title: c ?? void 0,
      "aria-label": k ? `${i} ${c}` : void 0,
      onClick: k ? y : void 0,
      onKeyDown: k ? (I) => {
        (I.key === "Enter" || I.key === " ") && (I.preventDefault(), y());
      } : void 0
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__text" }, i),
    _ !== null && /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__countdown" }, _)
  );
}, { useCallback: Lt, useEffect: un, useRef: sr, useState: dn } = e, cr = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.open === !0, r = t.anchorId, i = t.anchorX, o = t.anchorY, u = t.items ?? [], s = sr(null), [c, d] = dn({ top: 0, left: 0 }), [m, p] = dn(0), h = u.filter((E) => E.type === "item" && !E.disabled);
  un(() => {
    var C, O;
    if (!a) return;
    const E = ((C = s.current) == null ? void 0 : C.offsetHeight) ?? 200, _ = ((O = s.current) == null ? void 0 : O.offsetWidth) ?? 200;
    if (i != null && o != null) {
      let F = o, A = i;
      F + E > window.innerHeight && (F = Math.max(0, window.innerHeight - E)), A + _ > window.innerWidth && (A = Math.max(0, window.innerWidth - _)), d({ top: F, left: A }), p(0);
      return;
    }
    if (!r) return;
    const k = document.getElementById(r);
    if (!k) return;
    const I = k.getBoundingClientRect();
    let w = I.bottom + 4, v = I.left;
    w + E > window.innerHeight && (w = I.top - E - 4), v + _ > window.innerWidth && (v = I.right - _), d({ top: w, left: v }), p(0);
  }, [a, r, i, o]);
  const b = Lt(() => {
    n("close");
  }, [n]), D = Lt((E) => {
    n("selectItem", { itemId: E });
  }, [n]);
  un(() => {
    if (!a) return;
    const E = (_) => {
      s.current && !s.current.contains(_.target) && b();
    };
    return document.addEventListener("mousedown", E), () => document.removeEventListener("mousedown", E);
  }, [a, b]);
  const y = Lt((E) => {
    if (E.key === "Escape") {
      E.preventDefault(), b();
      return;
    }
    if (E.key === "ArrowDown")
      E.preventDefault(), p((_) => (_ + 1) % h.length);
    else if (E.key === "ArrowUp")
      E.preventDefault(), p((_) => (_ - 1 + h.length) % h.length);
    else if (E.key === "Enter" || E.key === " ") {
      E.preventDefault();
      const _ = h[m];
      _ && D(_.id);
    }
  }, [b, D, h, m]);
  return Gt(a, s), a ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: s,
      tabIndex: -1,
      style: { position: "fixed", top: c.top, left: c.left },
      onKeyDown: y
    },
    u.map((E, _) => {
      if (E.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: _, className: "tlMenu__separator" });
      const I = h.indexOf(E) === m;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: E.id,
          type: "button",
          className: "tlMenu__item" + (I ? " tlMenu__item--focused" : "") + (E.disabled ? " tlMenu__item--disabled" : "") + (E.cssClasses ? " " + E.cssClasses : ""),
          role: "menuitem",
          disabled: E.disabled,
          tabIndex: I ? 0 : -1,
          onClick: () => D(E.id)
        },
        E.icon && /* @__PURE__ */ e.createElement(Ne, { encoded: E.icon, className: "tlMenu__icon" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, E.label)
      );
    })
  ) : null;
}, ir = 768, ur = ({ controlId: l }) => {
  const t = X(), n = le(), a = wt(!0);
  e.useEffect(() => {
    const c = window.matchMedia(`(max-width: ${ir}px)`), d = (p) => {
      n("reportDisplayClass", { displayClass: p ? "COMPACT" : "REGULAR" });
    };
    d(c.matches);
    const m = (p) => d(p.matches);
    return c.addEventListener("change", m), () => c.removeEventListener("change", m);
  }, [n]);
  const r = t.header, i = t.notices, o = t.content, u = t.footer, s = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell " + a }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(G, { control: r })), i && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__notices" }, /* @__PURE__ */ e.createElement(G, { control: i })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(Ae, null, /* @__PURE__ */ e.createElement(G, { control: o }))), u && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(G, { control: u })), /* @__PURE__ */ e.createElement(G, { control: s }));
}, dr = ({ controlId: l }) => {
  const t = X(), n = t.text ?? "", a = t.cssClass ?? "", r = t.hasTooltip === !0, i = t.role || void 0, o = a ? `tlText ${a}` : "tlText";
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: o,
      role: i,
      "data-tooltip": r ? "key:tooltip" : void 0
    },
    n
  );
}, mr = ({ isMulti: l, cursorIndex: t, onMove: n, onToggle: a, onSelectAll: r, onActivate: i }) => (me("ArrowUp", () => (n("up", !1, !1), !0)), me("ArrowDown", () => (n("down", !1, !1), !0)), me("Home", () => (n("home", !1, !1), !0)), me("End", () => (n("end", !1, !1), !0)), me("PageUp", () => (n("pageUp", !1, !1), !0)), me("PageDown", () => (n("pageDown", !1, !1), !0)), me("Shift+ArrowUp", () => (n("up", l, !1), !0)), me("Shift+ArrowDown", () => (n("down", l, !1), !0)), me("Shift+Home", () => (n("home", l, !1), !0)), me("Shift+End", () => (n("end", l, !1), !0)), me("Shift+PageUp", () => (n("pageUp", l, !1), !0)), me("Shift+PageDown", () => (n("pageDown", l, !1), !0)), me("Ctrl+ArrowUp", () => (n("up", !1, l), !0)), me("Ctrl+ArrowDown", () => (n("down", !1, l), !0)), me("Space", () => t < 0 ? !1 : (a(), !0)), me("Ctrl+A", () => l ? (r(), !0) : !1), me("Enter", () => i()), null), pr = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all",
  "js.table.groupBy": "Group by this column",
  "js.table.ungroup": "Remove grouping",
  "js.table.grouped": "The rows are grouped by this column",
  "js.table.freezeSplitter": "Drag to choose the columns that stay in place while scrolling",
  "js.table.filter": "Filter",
  "js.table.columns": "Columns",
  "js.table.search": "Search",
  "js.table.searchHint": "Search the displayed columns",
  "js.table.clearFilter": "Show all rows again",
  "js.table.saveFilter": "Save this filter",
  "js.table.filterName": "Filter name",
  "js.table.deleteFilter": "Delete this filter",
  "js.table.cancelSave": "Do not save"
}, fr = 300, mn = 50, hr = 'input, textarea, select, button, a, [contenteditable="true"], [role="combobox"], [role="listbox"], [role="option"], [role="button"], [role="link"], [role="checkbox"], [role="radio"], [role="switch"], [role="textbox"], [role="spinbutton"], [role="slider"], [role="menu"], [role="menuitem"]';
function mt(l) {
  var n;
  const t = l.target;
  return !!((n = t == null ? void 0 : t.closest) != null && n.call(t, hr));
}
const Wt = 'input:not([disabled]):not([readonly]), textarea:not([disabled]):not([readonly]), select:not([disabled]), [contenteditable="true"]', pn = Wt + ", button:not([disabled]), a[href]";
function Tn(l, t) {
  return Array.from(l.querySelectorAll("[data-row][data-col]")).filter((n) => n.dataset.row === t);
}
function xt(l, t, n = {}) {
  const a = Tn(l, t);
  if (n.col) {
    const i = a.find((u) => u.dataset.col === n.col), o = i == null ? void 0 : i.querySelector(Wt);
    if (o) return o;
  }
  if (n.col)
    return null;
  const r = n.last ? [...a].reverse() : a;
  for (const i of r) {
    const o = i.querySelector(Wt);
    if (o) return o;
  }
  return null;
}
const br = ({ controlId: l }) => {
  var Qt;
  const t = X(), n = le(), a = ue(pr), r = e.useRef(null);
  e.useEffect(() => {
    const g = r.current;
    if (!g) return;
    const T = (V) => {
      const Z = V.detail;
      let te = Z.target;
      for (; te && te !== g; ) {
        const re = te.dataset.row, oe = te.dataset.col;
        if (re != null && oe != null) {
          Z.resolved = { key: re + "|" + oe };
          return;
        }
        te = te.parentElement;
      }
    };
    return g.addEventListener("tl-tooltip-resolve", T), () => g.removeEventListener("tl-tooltip-resolve", T);
  }, []);
  const i = t.columns ?? [], o = t.totalRowCount ?? 0, u = t.rows ?? [], s = t.rowHeight ?? 36, c = t.selectionMode ?? "single", d = t.selectedCount ?? 0, m = t.cursorIndex ?? -1, p = t.frozenColumnCount ?? 0, h = t.treeMode ?? !1, b = t.grouping ?? "", D = t.columnSelect ?? !1, y = t.filterBar ?? !1, E = t.namedFilters ?? [], _ = t.activeNamedFilter ?? "", k = t.search ?? "", I = t.filterSaving ?? !1, w = e.useMemo(
    () => i.filter((g) => g.sortPriority && g.sortPriority > 0).length,
    [i]
  ), v = c === "multi", C = 40, O = 20, F = e.useRef(null), A = e.useRef(null), S = e.useRef(null), x = e.useRef(null), N = e.useRef(null), [H, B] = e.useState({}), j = e.useRef(null), P = e.useRef(!1), L = e.useRef(null), [R, U] = e.useState(null), [f, M] = e.useState(null), [Y, z] = e.useState(null), [Q, W] = e.useState(0);
  e.useEffect(() => {
    const g = S.current;
    if (!g)
      return;
    const T = () => {
      const Z = g.offsetWidth - g.clientWidth;
      W((te) => te === Z ? te : Z);
    };
    T();
    const V = new ResizeObserver(T);
    return V.observe(g), () => V.disconnect();
  }, []), e.useEffect(() => {
    j.current || B({});
  }, [i]);
  const J = e.useCallback((g) => H[g.name] ?? g.width, [H]), se = e.useMemo(() => {
    const g = [];
    let T = v && p > 0 ? C : 0;
    for (let V = 0; V < p && V < i.length; V++)
      g.push(T), T += J(i[V]);
    return g;
  }, [i, p, v, C, J]), ne = e.useMemo(() => {
    if (p <= 0)
      return 0;
    let g = v ? C : 0;
    for (let T = 0; T < p && T < i.length; T++)
      g += J(i[T]);
    return g;
  }, [i, p, v, C, J]), Ee = o * s, be = e.useRef(null), we = e.useCallback((g, T, V) => {
    V.preventDefault(), V.stopPropagation(), j.current = { column: g, startX: V.clientX, startWidth: T };
    let Z = V.clientX, te = 0;
    const re = () => {
      const ce = j.current;
      if (!ce) return;
      const de = Math.max(mn, ce.startWidth + (Z - ce.startX) + te);
      B((Ce) => ({ ...Ce, [ce.column]: de }));
    }, oe = () => {
      const ce = S.current, de = F.current;
      if (!ce || !j.current) return;
      const Ce = ce.getBoundingClientRect(), xe = 40, Jt = 8, ol = ce.scrollLeft;
      Z > Ce.right - xe ? ce.scrollLeft += Jt : Z < Ce.left + xe && (ce.scrollLeft = Math.max(0, ce.scrollLeft - Jt));
      const en = ce.scrollLeft - ol;
      en !== 0 && (de && (de.scrollLeft = ce.scrollLeft), te += en, re()), be.current = requestAnimationFrame(oe);
    };
    be.current = requestAnimationFrame(oe);
    const fe = (ce) => {
      Z = ce.clientX, re();
    }, pe = (ce) => {
      document.removeEventListener("mousemove", fe), document.removeEventListener("mouseup", pe), be.current !== null && (cancelAnimationFrame(be.current), be.current = null);
      const de = j.current;
      if (de) {
        const Ce = Math.max(mn, de.startWidth + (ce.clientX - de.startX) + te);
        n("columnResize", { column: de.column, width: Ce }), j.current = null, P.current = !0, requestAnimationFrame(() => {
          P.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", fe), document.addEventListener("mouseup", pe);
  }, [n]), Te = e.useCallback(() => {
    F.current && S.current && (F.current.scrollLeft = S.current.scrollLeft), x.current !== null && clearTimeout(x.current), x.current = window.setTimeout(() => {
      const g = S.current;
      if (!g) return;
      const T = g.scrollTop, V = Math.ceil(g.clientHeight / s), Z = Math.floor(T / s);
      n("scroll", { start: Z, count: V });
    }, 80);
  }, [n, s]), Re = e.useCallback((g, T, V) => {
    if (P.current) return;
    let Z;
    !T || T === "desc" ? Z = "asc" : Z = "desc";
    const te = V.shiftKey ? "add" : "replace";
    n("sort", { column: g, direction: Z, mode: te });
  }, [n]), $e = e.useCallback((g, T) => {
    L.current = g, T.dataTransfer.effectAllowed = "move", T.dataTransfer.setData("text/plain", g);
  }, []), ot = e.useCallback((g, T) => {
    if (!L.current || L.current === g) {
      U(null);
      return;
    }
    T.preventDefault(), T.dataTransfer.dropEffect = "move";
    const V = T.currentTarget.getBoundingClientRect(), Z = T.clientX < V.left + V.width / 2 ? "left" : "right";
    U({ column: g, side: Z });
  }, []), $ = e.useCallback((g) => {
    g.preventDefault(), g.stopPropagation();
    const T = L.current;
    if (!T || !R) {
      L.current = null, U(null);
      return;
    }
    let V = i.findIndex((te) => te.name === R.column);
    if (V < 0) {
      L.current = null, U(null);
      return;
    }
    const Z = i.findIndex((te) => te.name === T);
    R.side === "right" && V++, Z < V && V--, n("columnReorder", { column: T, targetIndex: V }), L.current = null, U(null);
  }, [i, R, n]), q = e.useCallback(() => {
    L.current = null, U(null);
  }, []), ae = e.useCallback((g, T) => {
    var te, re, oe, fe;
    const V = window.getSelection();
    if (V && !V.isCollapsed && T.currentTarget.contains(V.anchorNode))
      return;
    if (!mt(T) && ((te = S.current) == null || te.focus({ preventScroll: !0 }), !T.ctrlKey && !T.metaKey && !T.shiftKey)) {
      const pe = (fe = (oe = (re = T.target) == null ? void 0 : re.closest) == null ? void 0 : oe.call(re, "[data-col]")) == null ? void 0 : fe.getAttribute("data-col");
      N.current = { index: g, col: pe ?? void 0 };
    }
    const Z = u.find((pe) => pe.index === g);
    mt(T) && (Z != null && Z.selected) && !T.ctrlKey && !T.metaKey && !T.shiftKey || n("select", {
      rowIndex: g,
      ctrlKey: T.ctrlKey || T.metaKey,
      shiftKey: T.shiftKey
    });
  }, [n, u]), ie = e.useCallback((g, T) => {
    var V;
    mt(T) || ((V = u.find((Z) => Z.index === g)) == null ? void 0 : V.groupCount) == null && n("activate", { rowIndex: g });
  }, [n, u]), Xe = e.useCallback((g, T, V) => {
    n("moveSelection", { direction: g, extend: T, move: V });
  }, [n]), Bn = e.useCallback(() => {
    m < 0 || n("select", { rowIndex: m, ctrlKey: v, shiftKey: !1 });
  }, [n, m, v]), Pn = e.useCallback(() => {
    n("selectAll", { selected: !0 });
  }, [n]), On = e.useCallback(() => {
    var T;
    if (m < 0)
      return !1;
    const g = document.activeElement;
    return (T = g == null ? void 0 : g.closest) != null && T.call(g, pn) ? !1 : (n("activate", { rowIndex: m }), !0);
  }, [n, m]), Fn = e.useCallback(
    () => !!r.current && r.current.contains(document.activeElement),
    []
  );
  e.useEffect(() => {
    if (m < 0)
      return;
    const g = S.current;
    if (!g)
      return;
    const T = m * s, V = T + s;
    T < g.scrollTop ? g.scrollTop = T : V > g.scrollTop + g.clientHeight && (g.scrollTop = V - g.clientHeight);
  }, [m, s]), e.useEffect(() => {
    const g = N.current, T = S.current;
    if (!g || !T)
      return;
    const V = u.find((re) => re.index === g.index);
    if (!V || !xt(T, V.id))
      return;
    N.current = null;
    const Z = document.activeElement;
    if (Z && Z !== document.body && !T.contains(Z))
      return;
    const te = xt(T, V.id, { col: g.col, last: g.last });
    te && (te.focus({ preventScroll: !0 }), te instanceof HTMLInputElement && te.select());
  }, [u]);
  const $n = e.useCallback((g) => {
    if (g.key !== "Tab")
      return;
    const T = S.current, V = document.activeElement;
    if (!T || !V || !T.contains(V))
      return;
    const Z = V.closest("[data-row][data-col]");
    if (!Z)
      return;
    const te = Z.dataset.row, re = u.find((xe) => xe.id === te);
    if (!re)
      return;
    const oe = Tn(T, te).flatMap((xe) => Array.from(xe.querySelectorAll(pn))), fe = oe.indexOf(V);
    if (fe < 0)
      return;
    const pe = !g.shiftKey;
    if (!(pe ? fe === oe.length - 1 : fe === 0))
      return;
    const de = pe ? re.index + 1 : re.index - 1;
    if (de < 0 || de >= o)
      return;
    const Ce = u.find((xe) => xe.index === de);
    Ce && xt(T, Ce.id) || (g.preventDefault(), N.current = { index: de, last: !pe }, n("select", { rowIndex: de, ctrlKey: !1, shiftKey: !1 }));
  }, [u, o, n]), Hn = e.useCallback((g, T) => {
    T.stopPropagation(), n("select", { rowIndex: g, ctrlKey: !0, shiftKey: !1 });
  }, [n]), Wn = e.useCallback(() => {
    const g = d === o && o > 0;
    n("selectAll", { selected: !g });
  }, [n, d, o]), Un = e.useCallback((g, T, V) => {
    V.stopPropagation(), n("expand", { rowIndex: g, expanded: T });
  }, [n]), Vn = e.useCallback((g, T) => {
    T.preventDefault(), M({ x: T.clientX, y: T.clientY, colIdx: g });
  }, []), zn = e.useCallback(() => {
    f && (n("setFrozenColumnCount", { count: f.colIdx + 1 }), M(null));
  }, [f, n]), Kn = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), M(null);
  }, [n]), Yn = e.useCallback((g) => {
    n("group", { column: g }), M(null);
  }, [n]), Gn = e.useCallback(() => {
    n("group", { column: "" }), M(null);
  }, [n]), Xn = e.useCallback((g) => {
    g.preventDefault(), g.stopPropagation();
    const T = A.current, V = F.current;
    if (!T || !V)
      return;
    const Z = T.clientWidth, te = [{ x: 0, count: 0 }];
    V.querySelectorAll("[data-col-idx]").forEach((pe) => {
      const ce = pe.getBoundingClientRect().right - T.getBoundingClientRect().left;
      ce > 0 && ce <= Z && te.push({ x: ce, count: Number(pe.dataset.colIdx) + 1 });
    });
    let re = { x: ne, count: p };
    const oe = (pe) => {
      const ce = pe.clientX - T.getBoundingClientRect().left;
      re = te.reduce(
        (de, Ce) => Math.abs(Ce.x - ce) < Math.abs(de.x - ce) ? Ce : de,
        te[0]
      ), z(re);
    }, fe = () => {
      document.removeEventListener("mousemove", oe), document.removeEventListener("mouseup", fe), z(null), re.count !== p && n("setFrozenColumnCount", { count: re.count });
    };
    document.addEventListener("mousemove", oe), document.addEventListener("mouseup", fe);
  }, [ne, p, n]);
  e.useEffect(() => {
    if (!f) return;
    const g = () => M(null);
    return document.addEventListener("mousedown", g), () => document.removeEventListener("mousedown", g);
  }, [f]), Fe(!!f, { ESCAPE: () => M(null) });
  const qn = e.useCallback((g, T) => {
    T.stopPropagation(), T.preventDefault(), n("openFilter", { column: g });
  }, [n]), Zn = e.useCallback((g) => {
    g.stopPropagation(), g.preventDefault(), n("openColumnSelect", {});
  }, [n]), [Qn, qt] = e.useState(k), kt = e.useRef(!1), Le = e.useRef(null);
  e.useEffect(() => {
    kt.current || qt(k);
  }, [k]), e.useEffect(() => () => {
    Le.current !== null && clearTimeout(Le.current);
  }, []);
  const st = e.useCallback((g) => {
    Le.current !== null && (clearTimeout(Le.current), Le.current = null), kt.current = !1, n("search", { term: g });
  }, [n]), Jn = e.useCallback((g) => {
    qt(g), kt.current = !0, Le.current !== null && clearTimeout(Le.current), Le.current = window.setTimeout(() => st(g), fr);
  }, [st]), el = e.useCallback((g) => {
    g.key === "Enter" && (g.preventDefault(), st(g.currentTarget.value));
  }, [st]), tl = e.useCallback((g) => {
    g === _ ? n("clearFilter", {}) : n("applyNamedFilter", { id: g });
  }, [_, n]), nl = e.useCallback((g, T) => {
    T.stopPropagation(), n("deleteNamedFilter", { id: g });
  }, [n]), [qe, Ze] = e.useState(null), Nt = e.useCallback(() => {
    const g = (qe ?? "").trim();
    g && (n("saveNamedFilter", { filterName: g }), Ze(null));
  }, [qe, n]), ll = e.useCallback((g) => {
    g.key === "Enter" ? (g.preventDefault(), Nt()) : g.key === "Escape" && (g.preventDefault(), Ze(null));
  }, [Nt]), St = i.reduce((g, T) => g + J(T), 0) + (v ? C : 0), Dt = D ? 32 : 0, al = d === o && o > 0, Zt = d > 0 && d < o, rl = e.useCallback((g) => {
    g && (g.indeterminate = Zt);
  }, [Zt]);
  return /* @__PURE__ */ e.createElement(Yt, { active: Fn }, /* @__PURE__ */ e.createElement(
    mr,
    {
      isMulti: v,
      cursorIndex: m,
      onMove: Xe,
      onToggle: Bn,
      onSelectAll: Pn,
      onActivate: On
    }
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: r,
      id: l,
      className: "tlTableView",
      "data-tooltip": "dynamic",
      onDragOver: (g) => {
        if (!L.current) return;
        g.preventDefault();
        const T = S.current, V = F.current;
        if (!T) return;
        const Z = T.getBoundingClientRect(), te = 40, re = 8;
        g.clientX < Z.left + te ? T.scrollLeft = Math.max(0, T.scrollLeft - re) : g.clientX > Z.right - te && (T.scrollLeft += re), V && (V.scrollLeft = T.scrollLeft);
      },
      onDrop: $
    },
    y && /* @__PURE__ */ e.createElement("div", { className: "tlTableView__filterBar" }, E.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlTableView__filterChips" }, E.map((g) => {
      const T = g.id === _;
      return /* @__PURE__ */ e.createElement(
        "span",
        {
          key: g.id,
          className: "tlTableView__chip" + (T ? " tlTableView__chip--active" : "")
        },
        /* @__PURE__ */ e.createElement(
          "button",
          {
            type: "button",
            className: "tlTableView__chipLabel",
            "aria-pressed": T,
            title: T ? a["js.table.clearFilter"] : g.label,
            onClick: () => tl(g.id)
          },
          g.label
        ),
        g.deletable && /* @__PURE__ */ e.createElement(
          "button",
          {
            type: "button",
            className: "tlTableView__chipRemove",
            title: a["js.table.deleteFilter"],
            "aria-label": a["js.table.deleteFilter"],
            onClick: (V) => nl(g.id, V)
          },
          "×"
        )
      );
    })), /* @__PURE__ */ e.createElement("div", { className: "tlTableView__search", title: a["js.table.searchHint"] }, /* @__PURE__ */ e.createElement("i", { className: "bi bi-search", "aria-hidden": "true" }), /* @__PURE__ */ e.createElement(
      "input",
      {
        type: "search",
        className: "tlTableView__searchInput",
        placeholder: a["js.table.search"],
        "aria-label": a["js.table.searchHint"],
        value: Qn,
        onChange: (g) => Jn(g.target.value),
        onKeyDown: el
      }
    )), I && (qe === null ? /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__barButton",
        title: a["js.table.saveFilter"],
        "aria-label": a["js.table.saveFilter"],
        onClick: () => Ze("")
      },
      /* @__PURE__ */ e.createElement("i", { className: "bi bi-bookmark-plus" })
    ) : /* @__PURE__ */ e.createElement("div", { className: "tlTableView__saveForm" }, /* @__PURE__ */ e.createElement(
      "input",
      {
        type: "text",
        className: "tlTableView__saveInput",
        autoFocus: !0,
        placeholder: a["js.table.filterName"],
        "aria-label": a["js.table.filterName"],
        value: qe,
        onChange: (g) => Ze(g.target.value),
        onKeyDown: ll
      }
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__barButton",
        title: a["js.table.saveFilter"],
        "aria-label": a["js.table.saveFilter"],
        disabled: !qe.trim(),
        onClick: Nt
      },
      /* @__PURE__ */ e.createElement("i", { className: "bi bi-check-lg" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__barButton",
        title: a["js.table.cancelSave"],
        "aria-label": a["js.table.cancelSave"],
        onClick: () => Ze(null)
      },
      /* @__PURE__ */ e.createElement("i", { className: "bi bi-x-lg" })
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerArea", ref: A }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: F }, /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerRow",
        style: { width: St, paddingRight: Dt + Q }
      },
      v && /* @__PURE__ */ e.createElement(
        "div",
        {
          className: "tlTableView__headerCell tlTableView__checkboxCell" + (p > 0 ? " tlTableView__headerCell--frozen" : ""),
          style: {
            width: C,
            minWidth: C,
            ...p > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
          },
          onDragOver: (g) => {
            L.current && (g.preventDefault(), g.dataTransfer.dropEffect = "move", i.length > 0 && i[0].name !== L.current && U({ column: i[0].name, side: "left" }));
          }
        },
        /* @__PURE__ */ e.createElement(
          "input",
          {
            type: "checkbox",
            ref: rl,
            className: "tlTableView__checkbox",
            checked: al,
            onChange: Wn
          }
        )
      ),
      i.map((g, T) => {
        const V = J(g);
        i.length - 1;
        let Z = "tlTableView__headerCell";
        g.sortable && (Z += " tlTableView__headerCell--sortable"), R && R.column === g.name && (Z += " tlTableView__headerCell--dragOver-" + R.side);
        const te = T < p, re = T === p - 1;
        return te && (Z += " tlTableView__headerCell--frozen"), re && (Z += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
          "div",
          {
            key: g.name,
            className: Z,
            "data-col-idx": T,
            style: {
              width: V,
              minWidth: V,
              position: te ? "sticky" : "relative",
              ...te ? { left: se[T], zIndex: 2 } : {}
            },
            draggable: !0,
            onClick: g.sortable ? (oe) => Re(g.name, g.sortDirection, oe) : void 0,
            onContextMenu: (oe) => Vn(T, oe),
            onDragStart: (oe) => $e(g.name, oe),
            onDragOver: (oe) => ot(g.name, oe),
            onDrop: $,
            onDragEnd: q
          },
          /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, g.label),
          g.name === b && /* @__PURE__ */ e.createElement(
            "i",
            {
              className: "tlTableView__groupMark bi bi-collection",
              title: a["js.table.grouped"],
              "aria-hidden": "true"
            }
          ),
          g.filterable && /* @__PURE__ */ e.createElement(
            "button",
            {
              type: "button",
              className: "tlTableView__filterButton" + (g.filterActive ? " tlTableView__filterButton--active" : ""),
              title: a["js.table.filter"],
              style: {
                border: "none",
                background: "transparent",
                cursor: "pointer",
                padding: "0 4px",
                color: g.filterActive ? "#1565c0" : "inherit"
              },
              onMouseDown: (oe) => oe.stopPropagation(),
              onClick: (oe) => qn(g.name, oe)
            },
            /* @__PURE__ */ e.createElement("i", { className: g.filterActive ? "bi bi-funnel-fill" : "bi bi-funnel" })
          ),
          g.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, g.sortDirection === "asc" ? "▲" : "▼", w > 1 && g.sortPriority != null && g.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, g.sortPriority)),
          /* @__PURE__ */ e.createElement(
            "div",
            {
              className: "tlTableView__resizeHandle",
              onMouseDown: (oe) => we(g.name, V, oe)
            }
          )
        );
      }),
      /* @__PURE__ */ e.createElement(
        "div",
        {
          style: { flex: "0 0 0", minHeight: "100%" },
          onDragOver: (g) => {
            if (L.current && i.length > 0) {
              const T = i[i.length - 1];
              T.name !== L.current && (g.preventDefault(), g.dataTransfer.dropEffect = "move", U({ column: T.name, side: "right" }));
            }
          },
          onDrop: $
        }
      )
    )), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__frozenSplitter" + (Y ? " tlTableView__frozenSplitter--active" : ""),
        style: { left: ne },
        title: a["js.table.freezeSplitter"],
        onMouseDown: Xn
      }
    ), D && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__columnsButton",
        title: a["js.table.columns"],
        "aria-label": a["js.table.columns"],
        onClick: Zn
      },
      /* @__PURE__ */ e.createElement("i", { className: "bi bi-gear" })
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: S,
        className: "tlTableView__body",
        onScroll: Te,
        onKeyDown: $n,
        tabIndex: 0
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: Ee, position: "relative", width: St, paddingRight: Dt } }, u.map((g) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: g.id,
          className: "tlTableView__row" + (g.selected ? " tlTableView__row--selected" : "") + (g.index === m ? " tlTableView__row--cursor" : "") + (g.groupCount != null ? " tlTableView__row--group" : ""),
          style: {
            position: "absolute",
            top: g.index * s,
            height: s,
            width: St,
            paddingRight: Dt,
            ...g.index === m ? { outline: "2px solid var(--color-primary, #1a73e8)", outlineOffset: "-2px" } : {}
          },
          onMouseDown: (T) => {
            (T.shiftKey || T.ctrlKey || T.metaKey || T.detail > 1) && !mt(T) && T.preventDefault();
          },
          onClick: (T) => ae(g.index, T),
          onDoubleClick: (T) => ie(g.index, T)
        },
        v && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (p > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: C,
              minWidth: C,
              ...p > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (T) => T.stopPropagation()
          },
          g.groupCount == null && /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: g.selected,
              onChange: () => {
              },
              onClick: (T) => Hn(g.index, T),
              tabIndex: -1
            }
          )
        ),
        i.map((T, V) => {
          const Z = J(T), te = V === i.length - 1, re = V < p, oe = V === p - 1;
          let fe = "tlTableView__cell";
          re && (fe += " tlTableView__cell--frozen"), oe && (fe += " tlTableView__cell--frozenLast");
          const pe = h && V === 0, ce = g.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: T.name,
              className: fe,
              "data-row": g.id,
              "data-col": T.name,
              style: {
                ...te && !re ? { flex: "1 0 auto", minWidth: Z } : { width: Z, minWidth: Z },
                ...re ? { position: "sticky", left: se[V], zIndex: 2 } : {}
              }
            },
            pe ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ce * O } }, g.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (de) => Un(g.index, !g.expanded, de)
              },
              g.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), g.cells[T.name] && /* @__PURE__ */ e.createElement(G, { control: g.cells[T.name] }), g.groupCount != null && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__groupCount" }, "(", g.groupCount, ")")) : g.cells[T.name] && /* @__PURE__ */ e.createElement(G, { control: g.cells[T.name] })
          );
        })
      )))
    ),
    Y && /* @__PURE__ */ e.createElement("div", { className: "tlTableView__frozenPreview", style: { left: Y.x } }),
    f && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: f.y, left: f.x, zIndex: 1e4 },
        onMouseDown: (g) => g.stopPropagation()
      },
      f.colIdx + 1 !== p && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: zn }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      p > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Kn }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"])),
      ((Qt = i[f.colIdx]) == null ? void 0 : Qt.groupable) && i[f.colIdx].name !== b && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlMenu__item",
          role: "menuitem",
          onClick: () => Yn(i[f.colIdx].name)
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.groupBy"])
      ),
      b !== "" && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Gn }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.ungroup"]))
    )
  ));
}, gr = {
  "js.table.columnSearch": "Find column",
  "js.table.groupBy": "Group by this column",
  "js.table.ungroup": "Remove grouping"
}, Er = ({ controlId: l }) => {
  const t = X(), n = le(), a = ue(gr), r = t.entries ?? [], i = r.filter((v) => v.visible).length, [o, u] = e.useState(""), s = o.trim().toLowerCase(), c = s ? r.filter((v) => v.label.toLowerCase().includes(s)) : r, d = e.useRef(null), m = e.useRef(null), [p, h] = e.useState(null), b = e.useCallback((v) => {
    m.current = v, h(v);
  }, []), D = e.useCallback((v, C) => {
    n("columnVisible", { column: v, visible: C });
  }, [n]), y = e.useCallback((v) => {
    n("groupBy", { column: v });
  }, [n]), E = e.useCallback((v, C) => {
    d.current = v, C.dataTransfer.effectAllowed = "move", C.dataTransfer.setData("text/plain", v);
  }, []), _ = e.useCallback((v, C) => {
    if (!d.current || d.current === v) {
      b(null);
      return;
    }
    C.preventDefault(), C.dataTransfer.dropEffect = "move";
    const O = C.currentTarget.getBoundingClientRect(), F = C.clientY < O.top + O.height / 2 ? "top" : "bottom";
    b({ name: v, side: F });
  }, [b]), k = e.useCallback(() => {
    d.current = null, b(null);
  }, [b]), I = e.useCallback((v) => {
    v.preventDefault();
    const C = d.current, O = m.current;
    if (d.current = null, b(null), !C || !O)
      return;
    const F = r.findIndex((x) => x.name === O.name), A = r.findIndex((x) => x.name === C);
    if (F < 0 || A < 0)
      return;
    let S = O.side === "top" ? F : F + 1;
    A < S && S--, S !== A && n("columnReorder", { column: C, targetIndex: S });
  }, [r, n, b]), w = r.length > 10;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlColumnSelect", onDrop: I }, w && /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__search" }, /* @__PURE__ */ e.createElement("i", { className: "bi bi-search", "aria-hidden": "true" }), /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "search",
      className: "tlColumnSelect__searchInput",
      placeholder: a["js.table.columnSearch"],
      "aria-label": a["js.table.columnSearch"],
      value: o,
      onChange: (v) => u(v.target.value)
    }
  )), /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__list" + (w ? " tlColumnSelect__list--fixed" : "") }, c.map((v) => {
    const C = v.visible && i <= 1;
    let O = "tlColumnSelect__row";
    return p && p.name === v.name && (O += " tlColumnSelect__row--dragOver-" + p.side), /* @__PURE__ */ e.createElement(
      "div",
      {
        key: v.name,
        className: O,
        draggable: !0,
        onDragStart: (F) => E(v.name, F),
        onDragOver: (F) => _(v.name, F),
        onDrop: I,
        onDragEnd: k
      },
      /* @__PURE__ */ e.createElement("i", { className: "tlColumnSelect__handle bi bi-grip-vertical", "aria-hidden": "true" }),
      /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlColumnSelect__groupBy" + (v.grouped ? " tlColumnSelect__groupBy--active" : ""),
          title: v.grouped ? a["js.table.ungroup"] : a["js.table.groupBy"],
          "aria-pressed": v.grouped,
          onClick: () => y(v.name)
        },
        /* @__PURE__ */ e.createElement("i", { className: v.grouped ? "bi bi-collection-fill" : "bi bi-collection", "aria-hidden": "true" })
      ),
      /* @__PURE__ */ e.createElement("label", { className: "tlColumnSelect__label" }, /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          className: "tlReactCheckbox",
          checked: v.visible,
          disabled: C,
          onChange: (F) => D(v.name, F.target.checked)
        }
      ), /* @__PURE__ */ e.createElement("span", null, v.label))
    );
  })));
}, { useState: Ut, useRef: nt, useCallback: Et, useMemo: Pe, useEffect: fn } = e, vr = {
  "js.calendar.today": "Today",
  "js.calendar.previous": "Previous",
  "js.calendar.next": "Next",
  "js.calendar.day": "Day",
  "js.calendar.workWeek": "Work week",
  "js.calendar.week": "Week",
  "js.calendar.month": "Month",
  "js.calendar.year": "Year",
  "js.calendar.allDay": "All day",
  "js.calendar.more": "more",
  "js.calendar.newEventTitle": "Event title"
}, _e = 44, _t = 15, ye = 6e4, _r = 36e5, je = 864e5, Cr = 8;
function Se(l) {
  const t = new Date(l);
  return t.setHours(0, 0, 0, 0), t.getTime();
}
function ze(l, t) {
  const n = new Date(l);
  return n.setDate(n.getDate() + t), n.getTime();
}
function yr(l) {
  return Se(l);
}
function lt(l, t) {
  return Se(l) === Se(t);
}
function Ie(l) {
  return (l - Se(l)) / ye;
}
function Qe(l) {
  return Math.round(l / _t) * _t;
}
function Je(l, t, n) {
  return Math.max(t, Math.min(n, l));
}
function Ct(l) {
  if (!l)
    return "tlCalEvent--default";
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return "tlCalEvent--c" + Math.abs(t) % Cr;
}
function yt(l, t) {
  return l.color ? { ...t, "--cal-ev-bg": l.color } : t;
}
function wr(l) {
  return Array.isArray(l) ? l.map((t) => ({
    id: t.id,
    start: t.start,
    end: t.end,
    allDay: t.allDay === !0,
    title: t.title ?? "",
    tooltip: t.tooltip,
    category: t.category,
    color: t.color,
    movable: t.movable === !0,
    resizable: t.resizable === !0,
    selected: t.selected === !0
  })) : [];
}
function Oe(l, t, n) {
  return new Intl.DateTimeFormat(l, t).format(new Date(n));
}
function kr(l, t) {
  const n = { hour: "numeric", minute: "2-digit" };
  return Oe(l, n, t.start) + "–" + Oe(l, n, t.end);
}
const Nr = [
  { key: "DAY", label: "js.calendar.day" },
  { key: "WORK_WEEK", label: "js.calendar.workWeek" },
  { key: "WEEK", label: "js.calendar.week" },
  { key: "MONTH", label: "js.calendar.month" },
  { key: "YEAR", label: "js.calendar.year" }
], Sr = ({ title: l, granularity: t, i18n: n, send: a }) => /* @__PURE__ */ e.createElement("div", { className: "tlCalToolbar" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalNav" }, /* @__PURE__ */ e.createElement("button", { className: "tlCalBtn", onClick: () => a("navigate", { direction: "TODAY" }) }, n["js.calendar.today"]), /* @__PURE__ */ e.createElement(
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
)), /* @__PURE__ */ e.createElement("div", { className: "tlCalTitle" }, l), /* @__PURE__ */ e.createElement("div", { className: "tlCalGranularity" }, Nr.map((r) => /* @__PURE__ */ e.createElement(
  "button",
  {
    key: r.key,
    className: "tlCalBtn" + (r.key === t ? " tlCalBtn--active" : ""),
    onClick: () => a("switchGranularity", { granularity: r.key })
  },
  n[r.label]
))));
function Dr(l) {
  const t = [...l].sort((o, u) => o.start - u.start || u.end - o.end), n = [];
  let a = [], r = -1;
  const i = () => {
    const o = a.reduce((u, s) => Math.max(u, s.col + 1), 0);
    for (const u of a)
      u.cols = o;
    n.push(...a), a = [], r = -1;
  };
  for (const o of t) {
    a.length > 0 && o.start >= r && i();
    const u = new Set(a.filter((c) => c.ev.end > o.start).map((c) => c.col));
    let s = 0;
    for (; u.has(s); )
      s++;
    a.push({
      ev: o,
      topMin: Ie(o.start),
      botMin: Ie(o.start) + Math.max(15, (o.end - o.start) / ye),
      col: s,
      cols: 1
    }), r = Math.max(r, o.end);
  }
  return a.length > 0 && i(), n;
}
const Mt = (l) => {
  l.preventDefault(), l.currentTarget.setPointerCapture(l.pointerId);
}, Vt = ({ className: l, placeholder: t, style: n, onCommit: a, onDiscard: r }) => {
  const i = nt(!1), o = (u) => {
    i.current || (i.current = !0, u === null ? r() : a(u));
  };
  return /* @__PURE__ */ e.createElement("div", { className: l, style: n }, /* @__PURE__ */ e.createElement(
    "input",
    {
      className: "tlCalCreateInput",
      autoFocus: !0,
      placeholder: t,
      onPointerDown: (u) => u.stopPropagation(),
      onClick: (u) => u.stopPropagation(),
      onKeyDown: (u) => {
        u.stopPropagation(), u.key === "Enter" ? o(u.currentTarget.value) : u.key === "Escape" && o(null);
      },
      onBlur: () => o(null)
    }
  ));
}, Rn = (l) => {
  const [t, n] = Ut(null), a = nt(null);
  a.current = t;
  const r = Et((u) => n(u), []), i = Et(() => n(null), []), o = Et(
    (u) => {
      const s = a.current;
      s && l("createSlot", { ...s, title: u }), n(null);
    },
    [l]
  );
  return { pending: t, open: r, commit: o, discard: i };
}, Tr = ({
  ctx: l,
  rangeStart: t,
  granularity: n
}) => {
  const { events: a, locale: r, nonWorkingDays: i, dayStartHour: o, dayEndHour: u, now: s, send: c, editable: d, i18n: m } = l, p = Pe(() => {
    const B = n === "DAY" ? 1 : 7, j = [];
    for (let P = 0; P < B; P++) {
      const L = ze(t, P);
      n === "WORK_WEEK" && i.includes(new Date(L).getDay()) || j.push(L);
    }
    return j;
  }, [t, n, i]), h = Rn(c), b = nt(null), D = nt(null), [y, E] = Ut(null), _ = nt(null);
  _.current = y;
  const [k, I] = Ut(Date.now());
  fn(() => {
    const B = window.setInterval(() => I(Date.now()), 6e4);
    return () => window.clearInterval(B);
  }, []);
  const w = Et(
    (B, j) => {
      const P = b.current;
      if (!P)
        return { dayIndex: 0, min: 0 };
      const L = P.getBoundingClientRect(), R = L.width / p.length, U = Je(Math.floor((B - L.left) / R), 0, p.length - 1), f = j - L.top + P.scrollTop, M = Je(f / _e * 60, 0, 1440);
      return { dayIndex: U, min: M };
    },
    [p.length]
  );
  fn(() => {
    if (!y)
      return;
    const B = (L) => {
      const R = _.current;
      if (!R)
        return;
      const { dayIndex: U, min: f } = w(L.clientX, L.clientY);
      R.mode === "move" ? E({ ...R, dayStart: p[U], startMin: Je(Qe(f - R.grabMin), 0, 1440 - R.dur) }) : R.mode === "resize" ? E({ ...R, endMin: Je(Qe(f), R.startMin + _t, 1440) }) : E({ ...R, toMin: Je(Qe(f), 0, 1440) });
    }, j = () => {
      const L = _.current;
      if (E(null), !!L)
        if (L.mode === "move") {
          const R = L.dayStart + L.startMin * ye;
          R !== L.origStartMs && c("moveEvent", { eventId: L.id, start: R, end: R + L.dur * ye });
        } else if (L.mode === "resize") {
          const R = L.dayStart + L.endMin * ye;
          R !== L.origEndMs && c("resizeEvent", { eventId: L.id, end: R });
        } else {
          const R = Math.min(L.fromMin, L.toMin), U = Math.max(L.fromMin, L.toMin);
          U - R >= _t && h.open({ start: L.dayStart + R * ye, end: L.dayStart + U * ye, allDay: !1 });
        }
    }, P = () => E(null);
    return window.addEventListener("pointermove", B), window.addEventListener("pointerup", j, { once: !0 }), window.addEventListener("pointercancel", P), () => {
      window.removeEventListener("pointermove", B), window.removeEventListener("pointerup", j), window.removeEventListener("pointercancel", P);
    };
  }, [y, p, w, c, h.open]);
  const v = (B, j, P) => {
    if (!d || !j.movable)
      return;
    B.stopPropagation(), Mt(B), h.discard();
    const { min: L } = w(B.clientX, B.clientY), R = (j.end - j.start) / ye;
    E({
      mode: "move",
      id: j.id,
      grabMin: L - Ie(j.start),
      dur: R,
      dayStart: P,
      startMin: Ie(j.start),
      origStartMs: j.start
    });
  }, C = (B, j, P) => {
    !d || !j.resizable || (B.stopPropagation(), Mt(B), h.discard(), E({
      mode: "resize",
      id: j.id,
      dayStart: P,
      startMin: Ie(j.start),
      endMin: Ie(j.end),
      origEndMs: j.end
    }));
  }, O = (B, j) => {
    if (!d || B.button !== 0)
      return;
    Mt(B), h.discard();
    const { min: P } = w(B.clientX, B.clientY);
    E({ mode: "create", dayStart: j, fromMin: Qe(P), toMin: Qe(P) });
  }, F = Array.from({ length: 24 }, (B, j) => j), A = Pe(() => {
    if (y === null || !("id" in y))
      return a;
    const B = y;
    return a.map((j) => {
      if (j.id !== B.id)
        return j;
      if (B.mode === "move") {
        const P = B.dayStart + B.startMin * ye;
        return { ...j, start: P, end: P + B.dur * ye };
      }
      return { ...j, end: B.dayStart + B.endMin * ye };
    });
  }, [a, y]), S = Pe(() => p.map(
    (B) => Dr(
      A.filter((j) => !j.allDay && j.start < B + je && j.end > B)
    )
  ), [p, A]), x = Pe(() => p.map((B) => A.filter((j) => j.allDay && j.start < B + je && j.end > B)), [p, A]), N = o * _e, H = u * _e;
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeGrid" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeHeader" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalGutter" }), p.map((B) => {
    const j = i.includes(new Date(B).getDay()), P = lt(B, l.now);
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: B,
        className: "tlCalDayHead" + (j ? " tlCalDayHead--nonworking" : "") + (P ? " tlCalDayHead--today" : ""),
        onClick: () => c("goto", { date: B, granularity: "DAY" })
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlCalDayName" }, Oe(r, { weekday: "short" }, B)),
      /* @__PURE__ */ e.createElement("span", { className: "tlCalDayNum" }, new Date(B).getDate())
    );
  })), /* @__PURE__ */ e.createElement("div", { className: "tlCalAllDayRow" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalGutter tlCalAllDayLabel" }, m["js.calendar.allDay"]), p.map((B, j) => /* @__PURE__ */ e.createElement(
    "div",
    {
      key: B,
      className: "tlCalAllDayCell",
      onClick: () => d && h.open({ start: B, end: B + je, allDay: !0 })
    },
    h.pending && h.pending.allDay && h.pending.start === B && /* @__PURE__ */ e.createElement(
      Vt,
      {
        className: "tlCalAllDayEvent tlCalEvent--preview",
        placeholder: m["js.calendar.newEventTitle"],
        onCommit: h.commit,
        onDiscard: h.discard
      }
    ),
    x[j].map((P) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: P.id,
        className: "tlCalAllDayEvent " + Ct(P.category) + (P.selected ? " tlCalEvent--selected" : ""),
        style: yt(P),
        title: P.tooltip,
        onClick: (L) => {
          L.stopPropagation(), c("selectEvent", { eventId: P.id });
        }
      },
      P.title
    ))
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlCalScroll", ref: D }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeBody", style: { height: 24 * _e } }, /* @__PURE__ */ e.createElement("div", { className: "tlCalHourAxis" }, F.map((B) => /* @__PURE__ */ e.createElement("div", { key: B, className: "tlCalHourLabel", style: { top: B * _e } }, B === 0 ? "" : Oe(r, { hour: "numeric" }, Se(t) + B * _r)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalColumns", ref: b, style: { gridTemplateColumns: `repeat(${p.length}, 1fr)` } }, p.map((B, j) => {
    const P = i.includes(new Date(B).getDay()), L = y && ("dayStart" in y && y.dayStart === B) ? y : null;
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: B,
        className: "tlCalCol" + (P ? " tlCalCol--nonworking" : ""),
        onPointerDown: (R) => O(R, B)
      },
      F.map((R) => /* @__PURE__ */ e.createElement("div", { key: R, className: "tlCalHourLine", style: { top: R * _e } })),
      /* @__PURE__ */ e.createElement("div", { className: "tlCalWorkBand", style: { top: N, height: H - N } }),
      lt(B, k) && /* @__PURE__ */ e.createElement("div", { className: "tlCalNowLine", style: { top: Ie(Date.now()) / 60 * _e } }),
      S[j].map((R) => {
        const U = y !== null && "id" in y && y.id === R.ev.id, f = R.topMin / 60 * _e, M = (R.botMin - R.topMin) / 60 * _e, Y = 100 / R.cols;
        return /* @__PURE__ */ e.createElement(
          "div",
          {
            key: R.ev.id,
            className: "tlCalEvent " + Ct(R.ev.category) + (R.ev.selected ? " tlCalEvent--selected" : "") + (U ? " tlCalEvent--dragging" : ""),
            style: yt(R.ev, {
              top: f,
              height: M,
              left: `${R.col * Y}%`,
              width: `calc(${Y}% - 2px)`
            }),
            title: R.ev.tooltip,
            onPointerDown: (z) => v(z, R.ev, B),
            onClick: (z) => {
              z.stopPropagation(), c("selectEvent", { eventId: R.ev.id });
            }
          },
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTime" }, kr(r, R.ev)),
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTitle" }, R.ev.title),
          d && R.ev.resizable && /* @__PURE__ */ e.createElement("span", { className: "tlCalResizeHandle", onPointerDown: (z) => C(z, R.ev, B) })
        );
      }),
      h.pending && !h.pending.allDay && Se(h.pending.start) === B && /* @__PURE__ */ e.createElement(
        Vt,
        {
          className: "tlCalEvent tlCalEvent--preview",
          placeholder: m["js.calendar.newEventTitle"],
          style: {
            top: Ie(h.pending.start) / 60 * _e,
            height: (h.pending.end - h.pending.start) / ye / 60 * _e
          },
          onCommit: h.commit,
          onDiscard: h.discard
        }
      ),
      L && L.mode === "create" && /* @__PURE__ */ e.createElement(
        "div",
        {
          className: "tlCalEvent tlCalEvent--preview",
          style: {
            top: Math.min(L.fromMin, L.toMin) / 60 * _e,
            height: Math.abs(L.toMin - L.fromMin) / 60 * _e
          }
        }
      )
    );
  })))));
}, Rr = 3, Lr = ({ ctx: l, rangeStart: t, anchorMonth: n }) => {
  const { events: a, locale: r, nonWorkingDays: i, send: o, editable: u, now: s, i18n: c } = l, d = Rn(o), m = Pe(() => {
    const h = [];
    for (let b = 0; b < 6; b++) {
      const D = [];
      for (let y = 0; y < 7; y++)
        D.push(ze(t, b * 7 + y));
      h.push(D);
    }
    return h;
  }, [t]), p = (h, b) => {
    h.preventDefault();
    const D = h.dataTransfer.getData("text/plain"), y = a.find((_) => _.id === D);
    if (!y || !u || !y.movable)
      return;
    const E = b - Se(y.start);
    o("moveEvent", { eventId: D, start: y.start + E, end: y.end + E });
  };
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalMonth" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthHead" }, m[0].map((h) => /* @__PURE__ */ e.createElement("div", { key: h, className: "tlCalMonthWeekday" }, Oe(r, { weekday: "short" }, h)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthBody" }, m.map((h, b) => {
    const D = h[0], y = ze(D, 7), E = a.filter((k) => (k.allDay || k.end - k.start >= je) && k.start < y && k.end > D).sort((k, I) => k.start - I.start).slice(0, 3), _ = E.length;
    return /* @__PURE__ */ e.createElement("div", { key: b, className: "tlCalMonthWeek" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthDays" }, h.map((k) => {
      const I = new Date(k).getMonth() === new Date(n).getMonth(), w = i.includes(new Date(k).getDay()), v = lt(k, s);
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: k,
          className: "tlCalMonthCell" + (I ? "" : " tlCalMonthCell--other") + (w ? " tlCalMonthCell--nonworking" : ""),
          onDragOver: (C) => C.preventDefault(),
          onDrop: (C) => p(C, k),
          onClick: () => u && d.open({ start: k, end: k + je, allDay: !0 })
        },
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlCalMonthDayNum" + (v ? " tlCalMonthDayNum--today" : ""),
            onClick: (C) => {
              C.stopPropagation(), o("goto", { date: k, granularity: "DAY" });
            }
          },
          new Date(k).getDate()
        ),
        d.pending && d.pending.start === k && /* @__PURE__ */ e.createElement(
          Vt,
          {
            className: "tlCalMonthBar tlCalEvent--preview tlCalMonthCreate",
            placeholder: c["js.calendar.newEventTitle"],
            onCommit: d.commit,
            onDiscard: d.discard
          }
        )
      );
    })), /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthOverlay" }, E.map((k, I) => {
      const w = Math.max(0, Math.floor((Se(Math.max(k.start, D)) - D) / je)), v = Math.min(7, Math.ceil((k.end - D) / je));
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: k.id,
          className: "tlCalMonthBar " + Ct(k.category) + (k.selected ? " tlCalEvent--selected" : ""),
          style: yt(k, {
            gridColumn: `${w + 1} / ${Math.max(w + 1, v) + 1}`,
            gridRow: I + 1
          }),
          draggable: u && k.movable,
          onDragStart: (C) => C.dataTransfer.setData("text/plain", k.id),
          title: k.tooltip,
          onClick: (C) => {
            C.stopPropagation(), o("selectEvent", { eventId: k.id });
          }
        },
        k.title
      );
    }), h.map((k, I) => {
      const w = a.filter((O) => !O.allDay && O.end - O.start < je && lt(O.start, k)).sort((O, F) => O.start - F.start), v = w.slice(0, Rr), C = w.length - v.length;
      return v.map((O, F) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: O.id,
          className: "tlCalChip " + Ct(O.category) + (O.selected ? " tlCalEvent--selected" : ""),
          style: yt(O, { gridColumn: I + 1, gridRow: _ + 1 + F }),
          draggable: u && O.movable,
          onDragStart: (A) => A.dataTransfer.setData("text/plain", O.id),
          title: O.tooltip,
          onClick: (A) => {
            A.stopPropagation(), o("selectEvent", { eventId: O.id });
          }
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipDot" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipTime" }, Oe(r, { hour: "numeric", minute: "2-digit" }, O.start)),
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipTitle" }, O.title)
      )).concat(
        C > 0 ? [
          /* @__PURE__ */ e.createElement(
            "div",
            {
              key: "more-" + k,
              className: "tlCalMore",
              style: { gridColumn: I + 1, gridRow: _ + 1 + v.length },
              onClick: () => o("goto", { date: k, granularity: "DAY" })
            },
            "+",
            C,
            " ",
            l.i18n["js.calendar.more"]
          )
        ] : []
      );
    })));
  })));
}, xr = ({ ctx: l, rangeStart: t }) => {
  const { events: n, locale: a, firstDayOfWeek: r, nonWorkingDays: i, send: o, now: u } = l, s = Pe(() => {
    const p = /* @__PURE__ */ new Set();
    for (const h of n) {
      let b = Se(h.start);
      const D = h.end;
      for (; b < D; )
        p.add(b), b = ze(b, 1);
    }
    return p;
  }, [n]), c = new Date(t).getFullYear(), d = Array.from({ length: 12 }, (p, h) => new Date(c, h, 1).getTime()), m = Pe(() => {
    const p = new Date(2023, 0, 1);
    return Array.from({ length: 7 }, (h, b) => {
      const D = new Date(p);
      return D.setDate(p.getDate() + (r + b) % 7), new Intl.DateTimeFormat(a, { weekday: "narrow" }).format(D);
    });
  }, [a, r]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalYear" }, d.map((p) => {
    const h = new Date(p), b = Se(ze(p, -((h.getDay() - r + 7) % 7))), D = Array.from({ length: 42 }, (y, E) => ze(b, E));
    return /* @__PURE__ */ e.createElement("div", { key: p, className: "tlCalMini" }, /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlCalMiniTitle",
        onClick: () => o("goto", { date: p, granularity: "MONTH" })
      },
      Oe(a, { month: "long" }, p)
    ), /* @__PURE__ */ e.createElement("div", { className: "tlCalMiniGrid" }, m.map((y, E) => /* @__PURE__ */ e.createElement("div", { key: "h" + E, className: "tlCalMiniWd" }, y)), D.map((y) => {
      const E = new Date(y).getMonth() === h.getMonth(), _ = i.includes(new Date(y).getDay()), k = lt(y, u), I = s.has(yr(y));
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: y,
          className: "tlCalMiniDay" + (E ? "" : " tlCalMiniDay--other") + (_ ? " tlCalMiniDay--nonworking" : "") + (k ? " tlCalMiniDay--today" : "") + (I ? " tlCalMiniDay--event" : ""),
          onClick: () => o("goto", { date: y, granularity: "DAY" })
        },
        new Date(y).getDate()
      );
    })));
  }));
}, Mr = ({ controlId: l }) => {
  const t = X(), n = le(), a = ue(vr), r = t.granularity ?? "WEEK", i = t.rangeStart ?? Date.now(), o = t.anchor ?? i, u = t.title ?? "", s = {
    locale: t.locale ?? "en",
    firstDayOfWeek: t.firstDayOfWeek ?? 0,
    nonWorkingDays: t.nonWorkingDays ?? [0, 6],
    dayStartHour: t.dayStartHour ?? 8,
    dayEndHour: t.dayEndHour ?? 18,
    editable: t.editable !== !1,
    now: t.now ?? Date.now(),
    events: wr(t.events),
    send: n,
    i18n: a
  };
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCalendar" }, /* @__PURE__ */ e.createElement(Sr, { title: u, granularity: r, i18n: a, send: n }), /* @__PURE__ */ e.createElement("div", { className: "tlCalBody" }, r === "MONTH" ? /* @__PURE__ */ e.createElement(Lr, { ctx: s, rangeStart: i, anchorMonth: o }) : r === "YEAR" ? /* @__PURE__ */ e.createElement(xr, { ctx: s, rangeStart: i }) : /* @__PURE__ */ e.createElement(Tr, { ctx: s, rangeStart: i, granularity: r })));
}, Ir = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, Ln = e.createContext(Ir), { useMemo: jr, useRef: Ar, useState: Br, useEffect: Pr } = e, Or = 320, Fr = "TLTableView", $r = "TLPanel", Hr = ({ controlId: l }) => {
  var y;
  const t = X(), n = t.maxColumns ?? 3, a = t.labelPosition ?? "auto", r = t.readOnly === !0, i = t.children ?? [], o = t.noModelMessage, u = Ar(null), [s, c] = Br(
    a === "top" ? "top" : "side"
  );
  Pr(() => {
    if (a !== "auto") {
      c(a);
      return;
    }
    const E = u.current;
    if (!E) return;
    const _ = new ResizeObserver((k) => {
      for (const I of k) {
        const v = I.contentRect.width / n;
        c(v < Or ? "top" : "side");
      }
    });
    return _.observe(E), () => _.disconnect();
  }, [a, n]);
  const d = jr(() => ({
    readOnly: r,
    resolvedLabelPosition: s
  }), [r, s]), p = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, h = i.length === 1 ? i[0] : void 0, b = !!h && (h.module === Fr || h.module === $r && ((y = h.state) == null ? void 0 : y.bare) === !0), D = [
    "tlFormLayout",
    r ? "tlFormLayout--readonly" : "",
    b ? "tlFormLayout--flush" : ""
  ].filter(Boolean).join(" ");
  return o ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, o)) : /* @__PURE__ */ e.createElement(Ln.Provider, { value: d }, /* @__PURE__ */ e.createElement("div", { id: l, className: D, style: p, ref: u }, i.map((E, _) => /* @__PURE__ */ e.createElement(G, { key: _, control: E }))));
}, { useCallback: Wr } = e, Ur = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, Vr = ({ controlId: l }) => {
  const t = X(), n = le(), a = ue(Ur), r = t.headerControl ?? null, i = t.headerActions ?? [], o = t.collapsible === !0, u = t.collapsed === !0, s = t.border ?? "none", c = t.fullLine === !0, d = t.children ?? [], m = r != null || i.length > 0 || o, p = Wr(() => {
    n("toggleCollapse");
  }, [n]), h = [
    "tlFormGroup",
    `tlFormGroup--border-${s}`,
    c ? "tlFormGroup--fullLine" : "",
    u ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: h }, m && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, o && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: p,
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
  ), r && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(G, { control: r })), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, i.map((b, D) => /* @__PURE__ */ e.createElement(G, { key: D, control: b })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, d.map((b, D) => /* @__PURE__ */ e.createElement(G, { key: D, control: b }))));
}, { useContext: zr, useState: Kr, useCallback: Yr } = e, Gr = ({ controlId: l }) => {
  const t = X(), n = zr(Ln), a = t.label ?? "", r = t.required === !0, i = t.error, o = t.errorIcon, u = t.warnings, s = t.warningIcon, c = t.helpText, d = t.dirty === !0, m = t.labelPosition ?? n.resolvedLabelPosition, p = t.fullLine === !0, h = t.visible !== !1, b = t.hasTooltip === !0, D = t.field, y = n.readOnly, [E, _] = Kr(!1), k = Yr(() => _((O) => !O), []), I = m === "hidden", w = i != null, v = u != null && u.length > 0, C = [
    "tlFormField",
    `tlFormField--${m}`,
    y ? "tlFormField--readonly" : "",
    p ? "tlFormField--fullLine" : "",
    w ? "tlFormField--error" : "",
    !w && v ? "tlFormField--warning" : "",
    d ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: C, style: h ? void 0 : { display: "none" } }, !I && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": b ? "key:tooltip" : void 0
    },
    a
  ), r && !y && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), d && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), c && !y && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormField__helpIcon",
      onClick: k,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(G, { control: D })), !y && w && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(Ft, { image: o, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, i)), !y && !w && v && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, u.map((O, F) => /* @__PURE__ */ e.createElement("div", { key: F, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(Ft, { image: s, className: "tlFormField__warningIcon" }), /* @__PURE__ */ e.createElement("span", null, O)))), !y && c && E && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, c));
}, Xr = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.iconCss, r = t.iconSrc, i = t.label, o = t.cssClass, u = t.hasTooltip === !0, s = t.hasLink, c = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : r ? /* @__PURE__ */ e.createElement("img", { src: r, className: "tlTypeIcon", alt: "" }) : null, d = /* @__PURE__ */ e.createElement(e.Fragment, null, c, i && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, i)), m = e.useCallback((b) => {
    b.preventDefault(), n("goto", {});
  }, [n]), p = ["tlResourceCell", o].filter(Boolean).join(" "), h = u ? "key:tooltip" : void 0;
  return s ? /* @__PURE__ */ e.createElement(
    "a",
    {
      id: l,
      className: p,
      href: "#",
      onClick: m,
      "data-tooltip": h
    },
    d
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: p, "data-tooltip": h }, d);
}, qr = 20, hn = "expand", bn = "collapse", pt = "select", Zr = "activate", Qr = "contextMenu", Jr = "dragOver", eo = "drop", to = "dragEnd", no = "single", lo = "multi", ao = () => {
  var A;
  const l = X(), t = le(), n = l.nodes ?? [], a = l.selectionMode ?? no, r = l.dragEnabled ?? !1, i = l.dropEnabled ?? !1, o = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, s = a === lo, [c, d] = e.useState(null), m = e.useRef(null), p = e.useMemo(() => {
    const S = c == null ? -1 : n.findIndex((x) => x.id === c);
    return S >= 0 ? S : n.findIndex((x) => x.selected);
  }, [n, c]), h = ((A = n.find((S) => S.selected)) == null ? void 0 : A.id) ?? null;
  e.useEffect(() => {
    var x;
    if (h == null)
      return;
    const S = (x = m.current) == null ? void 0 : x.querySelector(".tlTreeView__node--selected");
    S && S.scrollIntoView({ block: "nearest" });
  }, [h]), e.useEffect(() => {
    var S, x;
    c != null && ((x = (S = m.current) == null ? void 0 : S.querySelector(".tlTreeView__node--focused")) == null || x.scrollIntoView({ block: "nearest" }));
  }, [c]);
  const b = e.useCallback((S, x) => {
    t(x ? bn : hn, { nodeId: S });
  }, [t]), D = e.useCallback((S, x) => {
    var H;
    const N = window.getSelection();
    N && !N.isCollapsed && x.currentTarget.contains(N.anchorNode) || ((H = m.current) == null || H.focus({ preventScroll: !0 }), d(S), t(pt, {
      nodeId: S,
      ctrlKey: x.ctrlKey || x.metaKey,
      shiftKey: x.shiftKey
    }));
  }, [t]), y = e.useCallback((S) => {
    d(S), t(Zr, { nodeId: S });
  }, [t]), E = e.useCallback((S, x) => {
    x.preventDefault(), t(Qr, { nodeId: S, x: x.clientX, y: x.clientY });
  }, [t]), _ = e.useRef(null), k = e.useCallback((S, x) => {
    const N = x.getBoundingClientRect(), H = S.clientY - N.top, B = N.height / 3;
    return H < B ? "above" : H > B * 2 ? "below" : "within";
  }, []), I = e.useCallback((S, x) => {
    x.dataTransfer.effectAllowed = "move", x.dataTransfer.setData("text/plain", S);
  }, []), w = e.useCallback((S, x) => {
    x.preventDefault(), x.dataTransfer.dropEffect = "move";
    const N = k(x, x.currentTarget);
    _.current != null && window.clearTimeout(_.current), _.current = window.setTimeout(() => {
      t(Jr, { nodeId: S, position: N }), _.current = null;
    }, 50);
  }, [t, k]), v = e.useCallback((S, x) => {
    x.preventDefault(), _.current != null && (window.clearTimeout(_.current), _.current = null);
    const N = k(x, x.currentTarget);
    t(eo, { nodeId: S, position: N });
  }, [t, k]), C = e.useCallback(() => {
    _.current != null && (window.clearTimeout(_.current), _.current = null), t(to);
  }, [t]), O = e.useCallback((S, x) => {
    const N = n[S];
    N != null && (d(N.id), s ? x && t(pt, { nodeId: N.id, ctrlKey: !1, shiftKey: !0 }) : t(pt, { nodeId: N.id, ctrlKey: !1, shiftKey: !1 }));
  }, [n, s, t]), F = e.useCallback((S) => {
    if (n.length === 0)
      return;
    const x = p >= 0 ? n[p] : null;
    let N = p;
    switch (S.key) {
      case "ArrowDown":
        S.preventDefault(), N = Math.min(p + 1, n.length - 1);
        break;
      case "ArrowUp":
        S.preventDefault(), N = Math.max(p - 1, 0);
        break;
      case "ArrowRight":
        if (S.preventDefault(), x == null)
          break;
        if (x.expandable && !x.expanded) {
          t(hn, { nodeId: x.id });
          return;
        }
        x.expanded && (N = p + 1);
        break;
      case "ArrowLeft":
        if (S.preventDefault(), x == null)
          break;
        if (x.expanded) {
          t(bn, { nodeId: x.id });
          return;
        }
        for (let H = p - 1; H >= 0; H--)
          if (n[H].depth < x.depth) {
            N = H;
            break;
          }
        break;
      case "Home":
        S.preventDefault(), N = 0;
        break;
      case "End":
        S.preventDefault(), N = n.length - 1;
        break;
      case "Enter":
        S.preventDefault(), x != null && y(x.id);
        return;
      case " ":
        S.preventDefault(), x != null && t(pt, { nodeId: x.id, ctrlKey: s, shiftKey: !1 });
        return;
      default:
        return;
    }
    N !== p && O(N, S.shiftKey);
  }, [p, n, t, s, y, O]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: m,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: F
    },
    n.map((S, x) => /* @__PURE__ */ e.createElement(
      "li",
      {
        key: S.id,
        role: "treeitem",
        "aria-expanded": S.expandable ? S.expanded : void 0,
        "aria-selected": S.selected,
        "aria-level": S.depth + 1,
        className: [
          "tlTreeView__node",
          S.selected ? "tlTreeView__node--selected" : "",
          x === p ? "tlTreeView__node--focused" : "",
          o === S.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          o === S.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          o === S.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: S.depth * qr },
        draggable: r,
        onMouseDown: (N) => {
          (N.shiftKey || N.ctrlKey || N.metaKey || N.detail > 1) && N.preventDefault();
        },
        onClick: (N) => D(S.id, N),
        onDoubleClick: () => y(S.id),
        onContextMenu: (N) => E(S.id, N),
        onDragStart: (N) => I(S.id, N),
        onDragOver: i ? (N) => w(S.id, N) : void 0,
        onDrop: i ? (N) => v(S.id, N) : void 0,
        onDragEnd: C
      },
      S.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (N) => {
            N.stopPropagation(), b(S.id, S.expanded);
          },
          tabIndex: -1,
          "aria-label": S.expanded ? "Collapse" : "Expand"
        },
        S.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: S.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(G, { control: S.content }))
    ))
  );
};
var It = { exports: {} }, he = {}, jt = { exports: {} }, ee = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var gn;
function ro() {
  if (gn) return ee;
  gn = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), a = Symbol.for("react.strict_mode"), r = Symbol.for("react.profiler"), i = Symbol.for("react.consumer"), o = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), s = Symbol.for("react.suspense"), c = Symbol.for("react.memo"), d = Symbol.for("react.lazy"), m = Symbol.for("react.activity"), p = Symbol.iterator;
  function h(f) {
    return f === null || typeof f != "object" ? null : (f = p && f[p] || f["@@iterator"], typeof f == "function" ? f : null);
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
  }, D = Object.assign, y = {};
  function E(f, M, Y) {
    this.props = f, this.context = M, this.refs = y, this.updater = Y || b;
  }
  E.prototype.isReactComponent = {}, E.prototype.setState = function(f, M) {
    if (typeof f != "object" && typeof f != "function" && f != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, f, M, "setState");
  }, E.prototype.forceUpdate = function(f) {
    this.updater.enqueueForceUpdate(this, f, "forceUpdate");
  };
  function _() {
  }
  _.prototype = E.prototype;
  function k(f, M, Y) {
    this.props = f, this.context = M, this.refs = y, this.updater = Y || b;
  }
  var I = k.prototype = new _();
  I.constructor = k, D(I, E.prototype), I.isPureReactComponent = !0;
  var w = Array.isArray;
  function v() {
  }
  var C = { H: null, A: null, T: null, S: null }, O = Object.prototype.hasOwnProperty;
  function F(f, M, Y) {
    var z = Y.ref;
    return {
      $$typeof: l,
      type: f,
      key: M,
      ref: z !== void 0 ? z : null,
      props: Y
    };
  }
  function A(f, M) {
    return F(f.type, M, f.props);
  }
  function S(f) {
    return typeof f == "object" && f !== null && f.$$typeof === l;
  }
  function x(f) {
    var M = { "=": "=0", ":": "=2" };
    return "$" + f.replace(/[=:]/g, function(Y) {
      return M[Y];
    });
  }
  var N = /\/+/g;
  function H(f, M) {
    return typeof f == "object" && f !== null && f.key != null ? x("" + f.key) : M.toString(36);
  }
  function B(f) {
    switch (f.status) {
      case "fulfilled":
        return f.value;
      case "rejected":
        throw f.reason;
      default:
        switch (typeof f.status == "string" ? f.then(v, v) : (f.status = "pending", f.then(
          function(M) {
            f.status === "pending" && (f.status = "fulfilled", f.value = M);
          },
          function(M) {
            f.status === "pending" && (f.status = "rejected", f.reason = M);
          }
        )), f.status) {
          case "fulfilled":
            return f.value;
          case "rejected":
            throw f.reason;
        }
    }
    throw f;
  }
  function j(f, M, Y, z, Q) {
    var W = typeof f;
    (W === "undefined" || W === "boolean") && (f = null);
    var J = !1;
    if (f === null) J = !0;
    else
      switch (W) {
        case "bigint":
        case "string":
        case "number":
          J = !0;
          break;
        case "object":
          switch (f.$$typeof) {
            case l:
            case t:
              J = !0;
              break;
            case d:
              return J = f._init, j(
                J(f._payload),
                M,
                Y,
                z,
                Q
              );
          }
      }
    if (J)
      return Q = Q(f), J = z === "" ? "." + H(f, 0) : z, w(Q) ? (Y = "", J != null && (Y = J.replace(N, "$&/") + "/"), j(Q, M, Y, "", function(Ee) {
        return Ee;
      })) : Q != null && (S(Q) && (Q = A(
        Q,
        Y + (Q.key == null || f && f.key === Q.key ? "" : ("" + Q.key).replace(
          N,
          "$&/"
        ) + "/") + J
      )), M.push(Q)), 1;
    J = 0;
    var se = z === "" ? "." : z + ":";
    if (w(f))
      for (var ne = 0; ne < f.length; ne++)
        z = f[ne], W = se + H(z, ne), J += j(
          z,
          M,
          Y,
          W,
          Q
        );
    else if (ne = h(f), typeof ne == "function")
      for (f = ne.call(f), ne = 0; !(z = f.next()).done; )
        z = z.value, W = se + H(z, ne++), J += j(
          z,
          M,
          Y,
          W,
          Q
        );
    else if (W === "object") {
      if (typeof f.then == "function")
        return j(
          B(f),
          M,
          Y,
          z,
          Q
        );
      throw M = String(f), Error(
        "Objects are not valid as a React child (found: " + (M === "[object Object]" ? "object with keys {" + Object.keys(f).join(", ") + "}" : M) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return J;
  }
  function P(f, M, Y) {
    if (f == null) return f;
    var z = [], Q = 0;
    return j(f, z, "", "", function(W) {
      return M.call(Y, W, Q++);
    }), z;
  }
  function L(f) {
    if (f._status === -1) {
      var M = f._result;
      M = M(), M.then(
        function(Y) {
          (f._status === 0 || f._status === -1) && (f._status = 1, f._result = Y);
        },
        function(Y) {
          (f._status === 0 || f._status === -1) && (f._status = 2, f._result = Y);
        }
      ), f._status === -1 && (f._status = 0, f._result = M);
    }
    if (f._status === 1) return f._result.default;
    throw f._result;
  }
  var R = typeof reportError == "function" ? reportError : function(f) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var M = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof f == "object" && f !== null && typeof f.message == "string" ? String(f.message) : String(f),
        error: f
      });
      if (!window.dispatchEvent(M)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", f);
      return;
    }
    console.error(f);
  }, U = {
    map: P,
    forEach: function(f, M, Y) {
      P(
        f,
        function() {
          M.apply(this, arguments);
        },
        Y
      );
    },
    count: function(f) {
      var M = 0;
      return P(f, function() {
        M++;
      }), M;
    },
    toArray: function(f) {
      return P(f, function(M) {
        return M;
      }) || [];
    },
    only: function(f) {
      if (!S(f))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return f;
    }
  };
  return ee.Activity = m, ee.Children = U, ee.Component = E, ee.Fragment = n, ee.Profiler = r, ee.PureComponent = k, ee.StrictMode = a, ee.Suspense = s, ee.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = C, ee.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(f) {
      return C.H.useMemoCache(f);
    }
  }, ee.cache = function(f) {
    return function() {
      return f.apply(null, arguments);
    };
  }, ee.cacheSignal = function() {
    return null;
  }, ee.cloneElement = function(f, M, Y) {
    if (f == null)
      throw Error(
        "The argument must be a React element, but you passed " + f + "."
      );
    var z = D({}, f.props), Q = f.key;
    if (M != null)
      for (W in M.key !== void 0 && (Q = "" + M.key), M)
        !O.call(M, W) || W === "key" || W === "__self" || W === "__source" || W === "ref" && M.ref === void 0 || (z[W] = M[W]);
    var W = arguments.length - 2;
    if (W === 1) z.children = Y;
    else if (1 < W) {
      for (var J = Array(W), se = 0; se < W; se++)
        J[se] = arguments[se + 2];
      z.children = J;
    }
    return F(f.type, Q, z);
  }, ee.createContext = function(f) {
    return f = {
      $$typeof: o,
      _currentValue: f,
      _currentValue2: f,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, f.Provider = f, f.Consumer = {
      $$typeof: i,
      _context: f
    }, f;
  }, ee.createElement = function(f, M, Y) {
    var z, Q = {}, W = null;
    if (M != null)
      for (z in M.key !== void 0 && (W = "" + M.key), M)
        O.call(M, z) && z !== "key" && z !== "__self" && z !== "__source" && (Q[z] = M[z]);
    var J = arguments.length - 2;
    if (J === 1) Q.children = Y;
    else if (1 < J) {
      for (var se = Array(J), ne = 0; ne < J; ne++)
        se[ne] = arguments[ne + 2];
      Q.children = se;
    }
    if (f && f.defaultProps)
      for (z in J = f.defaultProps, J)
        Q[z] === void 0 && (Q[z] = J[z]);
    return F(f, W, Q);
  }, ee.createRef = function() {
    return { current: null };
  }, ee.forwardRef = function(f) {
    return { $$typeof: u, render: f };
  }, ee.isValidElement = S, ee.lazy = function(f) {
    return {
      $$typeof: d,
      _payload: { _status: -1, _result: f },
      _init: L
    };
  }, ee.memo = function(f, M) {
    return {
      $$typeof: c,
      type: f,
      compare: M === void 0 ? null : M
    };
  }, ee.startTransition = function(f) {
    var M = C.T, Y = {};
    C.T = Y;
    try {
      var z = f(), Q = C.S;
      Q !== null && Q(Y, z), typeof z == "object" && z !== null && typeof z.then == "function" && z.then(v, R);
    } catch (W) {
      R(W);
    } finally {
      M !== null && Y.types !== null && (M.types = Y.types), C.T = M;
    }
  }, ee.unstable_useCacheRefresh = function() {
    return C.H.useCacheRefresh();
  }, ee.use = function(f) {
    return C.H.use(f);
  }, ee.useActionState = function(f, M, Y) {
    return C.H.useActionState(f, M, Y);
  }, ee.useCallback = function(f, M) {
    return C.H.useCallback(f, M);
  }, ee.useContext = function(f) {
    return C.H.useContext(f);
  }, ee.useDebugValue = function() {
  }, ee.useDeferredValue = function(f, M) {
    return C.H.useDeferredValue(f, M);
  }, ee.useEffect = function(f, M) {
    return C.H.useEffect(f, M);
  }, ee.useEffectEvent = function(f) {
    return C.H.useEffectEvent(f);
  }, ee.useId = function() {
    return C.H.useId();
  }, ee.useImperativeHandle = function(f, M, Y) {
    return C.H.useImperativeHandle(f, M, Y);
  }, ee.useInsertionEffect = function(f, M) {
    return C.H.useInsertionEffect(f, M);
  }, ee.useLayoutEffect = function(f, M) {
    return C.H.useLayoutEffect(f, M);
  }, ee.useMemo = function(f, M) {
    return C.H.useMemo(f, M);
  }, ee.useOptimistic = function(f, M) {
    return C.H.useOptimistic(f, M);
  }, ee.useReducer = function(f, M, Y) {
    return C.H.useReducer(f, M, Y);
  }, ee.useRef = function(f) {
    return C.H.useRef(f);
  }, ee.useState = function(f) {
    return C.H.useState(f);
  }, ee.useSyncExternalStore = function(f, M, Y) {
    return C.H.useSyncExternalStore(
      f,
      M,
      Y
    );
  }, ee.useTransition = function() {
    return C.H.useTransition();
  }, ee.version = "19.2.4", ee;
}
var En;
function oo() {
  return En || (En = 1, jt.exports = ro()), jt.exports;
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
var vn;
function so() {
  if (vn) return he;
  vn = 1;
  var l = oo();
  function t(s) {
    var c = "https://react.dev/errors/" + s;
    if (1 < arguments.length) {
      c += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var d = 2; d < arguments.length; d++)
        c += "&args[]=" + encodeURIComponent(arguments[d]);
    }
    return "Minified React error #" + s + "; visit " + c + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
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
  }, r = Symbol.for("react.portal");
  function i(s, c, d) {
    var m = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: r,
      key: m == null ? null : "" + m,
      children: s,
      containerInfo: c,
      implementation: d
    };
  }
  var o = l.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function u(s, c) {
    if (s === "font") return "";
    if (typeof c == "string")
      return c === "use-credentials" ? c : "";
  }
  return he.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = a, he.createPortal = function(s, c) {
    var d = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!c || c.nodeType !== 1 && c.nodeType !== 9 && c.nodeType !== 11)
      throw Error(t(299));
    return i(s, c, null, d);
  }, he.flushSync = function(s) {
    var c = o.T, d = a.p;
    try {
      if (o.T = null, a.p = 2, s) return s();
    } finally {
      o.T = c, a.p = d, a.d.f();
    }
  }, he.preconnect = function(s, c) {
    typeof s == "string" && (c ? (c = c.crossOrigin, c = typeof c == "string" ? c === "use-credentials" ? c : "" : void 0) : c = null, a.d.C(s, c));
  }, he.prefetchDNS = function(s) {
    typeof s == "string" && a.d.D(s);
  }, he.preinit = function(s, c) {
    if (typeof s == "string" && c && typeof c.as == "string") {
      var d = c.as, m = u(d, c.crossOrigin), p = typeof c.integrity == "string" ? c.integrity : void 0, h = typeof c.fetchPriority == "string" ? c.fetchPriority : void 0;
      d === "style" ? a.d.S(
        s,
        typeof c.precedence == "string" ? c.precedence : void 0,
        {
          crossOrigin: m,
          integrity: p,
          fetchPriority: h
        }
      ) : d === "script" && a.d.X(s, {
        crossOrigin: m,
        integrity: p,
        fetchPriority: h,
        nonce: typeof c.nonce == "string" ? c.nonce : void 0
      });
    }
  }, he.preinitModule = function(s, c) {
    if (typeof s == "string")
      if (typeof c == "object" && c !== null) {
        if (c.as == null || c.as === "script") {
          var d = u(
            c.as,
            c.crossOrigin
          );
          a.d.M(s, {
            crossOrigin: d,
            integrity: typeof c.integrity == "string" ? c.integrity : void 0,
            nonce: typeof c.nonce == "string" ? c.nonce : void 0
          });
        }
      } else c == null && a.d.M(s);
  }, he.preload = function(s, c) {
    if (typeof s == "string" && typeof c == "object" && c !== null && typeof c.as == "string") {
      var d = c.as, m = u(d, c.crossOrigin);
      a.d.L(s, d, {
        crossOrigin: m,
        integrity: typeof c.integrity == "string" ? c.integrity : void 0,
        nonce: typeof c.nonce == "string" ? c.nonce : void 0,
        type: typeof c.type == "string" ? c.type : void 0,
        fetchPriority: typeof c.fetchPriority == "string" ? c.fetchPriority : void 0,
        referrerPolicy: typeof c.referrerPolicy == "string" ? c.referrerPolicy : void 0,
        imageSrcSet: typeof c.imageSrcSet == "string" ? c.imageSrcSet : void 0,
        imageSizes: typeof c.imageSizes == "string" ? c.imageSizes : void 0,
        media: typeof c.media == "string" ? c.media : void 0
      });
    }
  }, he.preloadModule = function(s, c) {
    if (typeof s == "string")
      if (c) {
        var d = u(c.as, c.crossOrigin);
        a.d.m(s, {
          as: typeof c.as == "string" && c.as !== "script" ? c.as : void 0,
          crossOrigin: d,
          integrity: typeof c.integrity == "string" ? c.integrity : void 0
        });
      } else a.d.m(s);
  }, he.requestFormReset = function(s) {
    a.d.r(s);
  }, he.unstable_batchedUpdates = function(s, c) {
    return s(c);
  }, he.useFormState = function(s, c, d) {
    return o.H.useFormState(s, c, d);
  }, he.useFormStatus = function() {
    return o.H.useHostTransitionStatus();
  }, he.version = "19.2.4", he;
}
var _n;
function co() {
  if (_n) return It.exports;
  _n = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), It.exports = so(), It.exports;
}
var xn = co();
const { useState: Me, useCallback: ge, useRef: et, useEffect: We, useMemo: zt } = e;
function Xt({ image: l }) {
  return l ? l.startsWith("/") ? /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" }) : /* @__PURE__ */ e.createElement(Ne, { encoded: l, className: "tlDropdownSelect__optionIcon" }) : null;
}
function io({
  option: l,
  removable: t,
  onRemove: n,
  removeLabel: a,
  draggable: r,
  onDragStart: i,
  onDragOver: o,
  onDrop: u,
  onDragEnd: s,
  dragClassName: c
}) {
  const d = ge(
    (m) => {
      m.stopPropagation(), n(l.value);
    },
    [n, l.value]
  );
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlDropdownSelect__chip" + (c ? " " + c : ""),
      draggable: r || void 0,
      onDragStart: i,
      onDragOver: o,
      onDrop: u,
      onDragEnd: s
    },
    r && /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__dragHandle", "aria-hidden": "true" }, "⋮⋮"),
    /* @__PURE__ */ e.createElement(Xt, { image: l.image }),
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
function uo({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: a,
  onMouseEnter: r,
  id: i
}) {
  const o = ge(() => a(l.value), [a, l.value]), u = zt(() => {
    if (!n) return l.label;
    const s = l.label.toLowerCase().indexOf(n.toLowerCase());
    return s < 0 ? l.label : /* @__PURE__ */ e.createElement(e.Fragment, null, l.label.substring(0, s), /* @__PURE__ */ e.createElement("strong", null, l.label.substring(s, s + n.length)), l.label.substring(s + n.length));
  }, [l.label, n]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: i,
      role: "option",
      "aria-selected": t,
      className: "tlDropdownSelect__option" + (t ? " tlDropdownSelect__option--highlighted" : ""),
      onClick: o,
      onMouseEnter: r
    },
    /* @__PURE__ */ e.createElement(Xt, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const mo = ({ controlId: l, state: t }) => {
  const n = le(), a = t.value ?? [], r = t.multiSelect === !0, i = t.customOrder === !0, o = t.mandatory === !0, u = t.disabled === !0, s = t.editable !== !1, c = t.optionsLoaded === !0, d = t.options ?? [], m = t.emptyOptionLabel ?? "", p = i && r && !u && s, h = ue({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), b = h["js.dropdownSelect.nothingFound"], D = ge(
    ($) => h["js.dropdownSelect.removeChip"].replace("{0}", $),
    [h]
  ), [y, E] = Me(!1), [_, k] = Me(""), [I, w] = Me(-1), [v, C] = Me(!1), [O, F] = Me({}), [A, S] = Me(null), [x, N] = Me(null), [H, B] = Me(null), j = et(null), P = et(null), L = et(null), R = et(a);
  R.current = a;
  const U = et(-1), f = zt(
    () => new Set(a.map(($) => $.value)),
    [a]
  ), M = zt(() => {
    let $ = d.filter((q) => !f.has(q.value));
    if (_) {
      const q = _.toLowerCase();
      $ = $.filter((ae) => ae.label.toLowerCase().includes(q));
    }
    return $;
  }, [d, f, _]);
  We(() => {
    _ && M.length === 1 ? w(0) : w(-1);
  }, [M.length, _]), We(() => {
    y && c && P.current && P.current.focus();
  }, [y, c, a]), We(() => {
    var ae, ie;
    if (U.current < 0) return;
    const $ = U.current;
    U.current = -1;
    const q = (ae = j.current) == null ? void 0 : ae.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    q && q.length > 0 ? q[Math.min($, q.length - 1)].focus() : (ie = j.current) == null || ie.focus();
  }, [a]), We(() => {
    if (!y) return;
    const $ = (q) => {
      j.current && !j.current.contains(q.target) && L.current && !L.current.contains(q.target) && (E(!1), k(""));
    };
    return document.addEventListener("mousedown", $), () => document.removeEventListener("mousedown", $);
  }, [y]), We(() => {
    if (!y || !j.current) return;
    const $ = j.current.getBoundingClientRect(), q = window.innerHeight - $.bottom, ie = q < 300 && $.top > q;
    F({
      left: $.left,
      width: $.width,
      ...ie ? { bottom: window.innerHeight - $.top } : { top: $.bottom }
    });
  }, [y]);
  const Y = ge(async () => {
    if (!(u || !s) && (E(!0), k(""), w(-1), C(!1), !c))
      try {
        await n("loadOptions");
      } catch {
        C(!0);
      }
  }, [u, s, c, n]), z = ge(() => {
    var $;
    E(!1), k(""), w(-1), ($ = j.current) == null || $.focus();
  }, []), Q = ge(
    ($) => {
      let q;
      if (r) {
        const ae = d.find((ie) => ie.value === $);
        if (ae)
          q = [...R.current, ae];
        else
          return;
      } else {
        const ae = d.find((ie) => ie.value === $);
        if (ae)
          q = [ae];
        else
          return;
      }
      R.current = q, n(ct, { value: q.map((ae) => ae.value) }), r ? (k(""), w(-1)) : z();
    },
    [r, d, n, z]
  ), W = ge(
    ($) => {
      U.current = R.current.findIndex((ae) => ae.value === $);
      const q = R.current.filter((ae) => ae.value !== $);
      R.current = q, n(ct, { value: q.map((ae) => ae.value) });
    },
    [n]
  ), J = ge(
    ($) => {
      $.stopPropagation(), n(ct, { value: [] }), z();
    },
    [n, z]
  ), se = ge(($) => {
    k($.target.value);
  }, []), ne = ge(
    ($) => {
      if (!y) {
        if ($.key === "ArrowDown" || $.key === "ArrowUp" || $.key === "Enter" || $.key === " ") {
          if ($.target.tagName === "BUTTON") return;
          $.preventDefault(), $.stopPropagation(), Y();
        }
        return;
      }
      switch ($.key) {
        case "ArrowDown":
          $.preventDefault(), $.stopPropagation(), w(
            (q) => q < M.length - 1 ? q + 1 : 0
          );
          break;
        case "ArrowUp":
          $.preventDefault(), $.stopPropagation(), w(
            (q) => q > 0 ? q - 1 : M.length - 1
          );
          break;
        case "Enter":
          $.preventDefault(), $.stopPropagation(), I >= 0 && I < M.length && Q(M[I].value);
          break;
        case "Escape":
          $.preventDefault(), $.stopPropagation(), z();
          break;
        case "Tab":
          z();
          break;
        case "Backspace":
          _ === "" && r && a.length > 0 && W(a[a.length - 1].value);
          break;
      }
    },
    [
      y,
      Y,
      z,
      M,
      I,
      Q,
      _,
      r,
      a,
      W
    ]
  ), Ee = ge(
    async ($) => {
      $.preventDefault(), C(!1);
      try {
        await n("loadOptions");
      } catch {
        C(!0);
      }
    },
    [n]
  ), be = ge(
    ($, q) => {
      S($), q.dataTransfer.effectAllowed = "move", q.dataTransfer.setData("text/plain", String($));
    },
    []
  ), we = ge(
    ($, q) => {
      if (q.preventDefault(), q.dataTransfer.dropEffect = "move", A === null || A === $) {
        N(null), B(null);
        return;
      }
      const ae = q.currentTarget.getBoundingClientRect(), ie = ae.left + ae.width / 2, Xe = q.clientX < ie ? "before" : "after";
      N($), B(Xe);
    },
    [A]
  ), Te = ge(
    ($) => {
      if ($.preventDefault(), A === null || x === null || H === null || A === x) return;
      const q = [...R.current], [ae] = q.splice(A, 1);
      let ie = x;
      A < x ? ie = H === "before" ? ie - 1 : ie : ie = H === "before" ? ie : ie + 1, q.splice(ie, 0, ae), R.current = q, n(ct, { value: q.map((Xe) => Xe.value) }), S(null), N(null), B(null);
    },
    [A, x, H, n]
  ), Re = ge(() => {
    S(null), N(null), B(null);
  }, []);
  if (We(() => {
    if (I < 0 || !L.current) return;
    const $ = L.current.querySelector(
      `[id="${l}-opt-${I}"]`
    );
    $ && $.scrollIntoView({ block: "nearest" });
  }, [I, l]), !s)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, a.map(($) => /* @__PURE__ */ e.createElement("span", { key: $.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(Xt, { image: $.image }), /* @__PURE__ */ e.createElement("span", null, $.label))));
  const $e = !o && a.length > 0 && !u, ot = y ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: L,
      className: "tlDropdownSelect__dropdown",
      style: O,
      ...cl
    },
    (c || v) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: P,
        type: "text",
        className: "tlDropdownSelect__search",
        value: _,
        onChange: se,
        onKeyDown: ne,
        placeholder: h["js.dropdownSelect.filterPlaceholder"],
        "aria-label": h["js.dropdownSelect.filterPlaceholder"],
        "aria-activedescendant": I >= 0 ? `${l}-opt-${I}` : void 0,
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
      !c && !v && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      v && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: Ee }, h["js.dropdownSelect.error"])),
      c && M.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, b),
      c && M.map(($, q) => /* @__PURE__ */ e.createElement(
        uo,
        {
          key: $.value,
          id: `${l}-opt-${q}`,
          option: $,
          highlighted: q === I,
          searchTerm: _,
          onSelect: Q,
          onMouseEnter: () => w(q)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: j,
      className: "tlDropdownSelect" + (y ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": y,
      "aria-haspopup": "listbox",
      "aria-owns": y ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: y ? void 0 : Y,
      onKeyDown: ne
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, a.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, m) : a.map(($, q) => {
      let ae = "";
      return A === q ? ae = "tlDropdownSelect__chip--dragging" : x === q && H === "before" ? ae = "tlDropdownSelect__chip--dropBefore" : x === q && H === "after" && (ae = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        io,
        {
          key: $.value,
          option: $,
          removable: !u && (r || !o),
          onRemove: W,
          removeLabel: D($.label),
          draggable: p,
          onDragStart: p ? (ie) => be(q, ie) : void 0,
          onDragOver: p ? (ie) => we(q, ie) : void 0,
          onDrop: p ? Te : void 0,
          onDragEnd: p ? Re : void 0,
          dragClassName: p ? ae : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, $e && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: J,
        "aria-label": h["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, y ? "▲" : "▼"))
  ), ot && xn.createPortal(ot, document.body));
}, { useCallback: At, useRef: po } = e, Mn = "application/x-tl-color", fo = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: a,
  onSwap: r,
  onReplace: i
}) => {
  const o = po(null), u = At(
    (d) => (m) => {
      o.current = d, m.dataTransfer.effectAllowed = "move";
    },
    []
  ), s = At((d) => {
    d.preventDefault(), d.dataTransfer.dropEffect = "move";
  }, []), c = At(
    (d) => (m) => {
      m.preventDefault();
      const p = m.dataTransfer.getData(Mn);
      p ? i(d, p) : o.current !== null && o.current !== d && r(o.current, d), o.current = null;
    },
    [r, i]
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
        onDragOver: s,
        onDrop: c(m)
      }
    ))
  );
};
function In(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function Kt(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function jn(l) {
  if (!Kt(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function An(l, t, n) {
  const a = (r) => In(r).toString(16).padStart(2, "0");
  return "#" + a(l) + a(t) + a(n);
}
function ho(l, t, n) {
  const a = l / 255, r = t / 255, i = n / 255, o = Math.max(a, r, i), u = Math.min(a, r, i), s = o - u;
  let c = 0;
  s !== 0 && (o === a ? c = (r - i) / s % 6 : o === r ? c = (i - a) / s + 2 : c = (a - r) / s + 4, c *= 60, c < 0 && (c += 360));
  const d = o === 0 ? 0 : s / o;
  return [c, d, o];
}
function bo(l, t, n) {
  const a = n * t, r = a * (1 - Math.abs(l / 60 % 2 - 1)), i = n - a;
  let o = 0, u = 0, s = 0;
  return l < 60 ? (o = a, u = r, s = 0) : l < 120 ? (o = r, u = a, s = 0) : l < 180 ? (o = 0, u = a, s = r) : l < 240 ? (o = 0, u = r, s = a) : l < 300 ? (o = r, u = 0, s = a) : (o = a, u = 0, s = r), [
    Math.round((o + i) * 255),
    Math.round((u + i) * 255),
    Math.round((s + i) * 255)
  ];
}
function go(l) {
  return ho(...jn(l));
}
function Bt(l, t, n) {
  return An(...bo(l, t, n));
}
const { useCallback: Ue, useRef: Cn } = e, Eo = ({ color: l, onColorChange: t }) => {
  const [n, a, r] = go(l), i = Cn(null), o = Cn(null), u = Ue(
    (b, D) => {
      var k;
      const y = (k = i.current) == null ? void 0 : k.getBoundingClientRect();
      if (!y) return;
      const E = Math.max(0, Math.min(1, (b - y.left) / y.width)), _ = Math.max(0, Math.min(1, 1 - (D - y.top) / y.height));
      t(Bt(n, E, _));
    },
    [n, t]
  ), s = Ue(
    (b) => {
      b.preventDefault(), b.target.setPointerCapture(b.pointerId), u(b.clientX, b.clientY);
    },
    [u]
  ), c = Ue(
    (b) => {
      b.buttons !== 0 && u(b.clientX, b.clientY);
    },
    [u]
  ), d = Ue(
    (b) => {
      var _;
      const D = (_ = o.current) == null ? void 0 : _.getBoundingClientRect();
      if (!D) return;
      const E = Math.max(0, Math.min(1, (b - D.top) / D.height)) * 360;
      t(Bt(E, a, r));
    },
    [a, r, t]
  ), m = Ue(
    (b) => {
      b.preventDefault(), b.target.setPointerCapture(b.pointerId), d(b.clientY);
    },
    [d]
  ), p = Ue(
    (b) => {
      b.buttons !== 0 && d(b.clientY);
    },
    [d]
  ), h = Bt(n, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: i,
      className: "tlColorInput__svField",
      style: { backgroundColor: h },
      onPointerDown: s,
      onPointerMove: c
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__svHandle",
        style: { left: `${a * 100}%`, top: `${(1 - r) * 100}%` }
      }
    )
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: o,
      className: "tlColorInput__hueSlider",
      onPointerDown: m,
      onPointerMove: p
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
function vo(l, t) {
  const n = t.toUpperCase();
  return l.some((a) => a != null && a.toUpperCase() === n);
}
const _o = {
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
}, { useState: ft, useCallback: ke, useEffect: yn, useRef: Co, useLayoutEffect: yo } = e, wo = ({
  anchorRef: l,
  currentColor: t,
  palette: n,
  paletteColumns: a,
  defaultPalette: r,
  canReset: i,
  onConfirm: o,
  onCancel: u,
  onPaletteChange: s
}) => {
  const [c, d] = ft("palette"), [m, p] = ft(t), h = Co(null), b = ue(_o), [D, y] = ft(null);
  yo(() => {
    if (!l.current || !h.current) return;
    const L = l.current.getBoundingClientRect(), R = h.current.getBoundingClientRect();
    let U = L.bottom + 4, f = L.left;
    U + R.height > window.innerHeight && (U = L.top - R.height - 4), f + R.width > window.innerWidth && (f = Math.max(0, L.right - R.width)), y({ top: U, left: f });
  }, [l]);
  const E = m != null, [_, k, I] = E ? jn(m) : [0, 0, 0], [w, v] = ft((m == null ? void 0 : m.toUpperCase()) ?? "");
  yn(() => {
    v((m == null ? void 0 : m.toUpperCase()) ?? "");
  }, [m]), Fe(!0, { ESCAPE: u }), yn(() => {
    const L = (U) => {
      h.current && !h.current.contains(U.target) && u();
    }, R = setTimeout(() => document.addEventListener("mousedown", L), 0);
    return () => {
      clearTimeout(R), document.removeEventListener("mousedown", L);
    };
  }, [u]);
  const C = ke(
    (L) => (R) => {
      const U = parseInt(R.target.value, 10);
      if (isNaN(U)) return;
      const f = In(U);
      p(An(L === "r" ? f : _, L === "g" ? f : k, L === "b" ? f : I));
    },
    [_, k, I]
  ), O = ke(
    (L) => {
      if (m != null) {
        L.dataTransfer.setData(Mn, m.toUpperCase()), L.dataTransfer.effectAllowed = "move";
        const R = document.createElement("div");
        R.style.width = "33px", R.style.height = "33px", R.style.backgroundColor = m, R.style.borderRadius = "3px", R.style.border = "1px solid rgba(0,0,0,0.1)", R.style.position = "absolute", R.style.top = "-9999px", document.body.appendChild(R), L.dataTransfer.setDragImage(R, 16, 16), requestAnimationFrame(() => document.body.removeChild(R));
      }
    },
    [m]
  ), F = ke((L) => {
    const R = L.target.value;
    v(R), Kt(R) && p(R);
  }, []), A = ke(() => {
    p(null);
  }, []), S = ke((L) => {
    p(L);
  }, []), x = ke(
    (L) => {
      o(L);
    },
    [o]
  ), N = ke(
    (L, R) => {
      const U = [...n], f = U[L];
      U[L] = U[R], U[R] = f, s(U);
    },
    [n, s]
  ), H = ke(
    (L, R) => {
      const U = [...n];
      U[L] = R, s(U);
    },
    [n, s]
  ), B = ke(() => {
    s([...r]);
  }, [r, s]), j = ke(
    (L) => {
      if (vo(n, L)) return;
      const R = n.indexOf(null);
      if (R < 0) return;
      const U = [...n];
      U[R] = L.toUpperCase(), s(U);
    },
    [n, s]
  ), P = ke(() => {
    m != null && j(m), o(m);
  }, [m, o, j]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: h,
      style: D ? { top: D.top, left: D.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (c === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => d("palette")
      },
      b["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (c === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => d("mixer")
      },
      b["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, c === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      fo,
      {
        colors: n,
        columns: a,
        onSelect: S,
        onConfirm: x,
        onSwap: N,
        onReplace: H
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: B }, b["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(Eo, { color: m ?? "#000000", onColorChange: p }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (E ? "" : " tlColorInput--noColor"),
        style: E ? { backgroundColor: m } : void 0,
        draggable: E,
        onDragStart: E ? O : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: E ? _ : "",
        onChange: C("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: E ? k : "",
        onChange: C("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: E ? I : "",
        onChange: C("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (w !== "" && !Kt(w) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: w,
        onChange: F
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, i && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: A }, b["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, b["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: P }, b["js.colorInput.ok"]))
  );
}, ko = { "js.colorInput.chooseColor": "Choose color" }, { useState: No, useCallback: ht, useRef: So } = e, Do = ({ controlId: l, state: t }) => {
  const [n, a] = De(), r = le(), i = ue(ko), [o, u] = No(!1), s = So(null), c = n, d = t.editable !== !1, m = t.palette ?? [], p = t.paletteColumns ?? 6, h = t.defaultPalette ?? m, b = ht(() => {
    d && u(!0);
  }, [d]), D = ht(
    (_) => {
      u(!1), a(_);
    },
    [a]
  ), y = ht(() => {
    u(!1);
  }, []), E = ht(
    (_) => {
      r("paletteChanged", { palette: _ });
    },
    [r]
  );
  return d ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: s,
      className: "tlColorInput__swatch" + (c == null ? " tlColorInput__swatch--noColor" : ""),
      style: c != null ? { backgroundColor: c } : void 0,
      onClick: b,
      disabled: t.disabled === !0,
      title: c ?? "",
      "aria-label": i["js.colorInput.chooseColor"]
    }
  ), o && /* @__PURE__ */ e.createElement(
    wo,
    {
      anchorRef: s,
      currentColor: c,
      palette: m,
      paletteColumns: p,
      defaultPalette: h,
      canReset: t.canReset !== !1,
      onConfirm: D,
      onCancel: y,
      onPaletteChange: E
    }
  )) : /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlColorInput tlColorInput--immutable" + (c == null ? " tlColorInput--noColor" : ""),
      style: c != null ? { backgroundColor: c } : void 0,
      title: c ?? ""
    }
  );
}, { useState: tt, useCallback: Be, useEffect: Pt, useRef: wn, useLayoutEffect: To, useMemo: Ro } = e, Lo = {
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
}, xo = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: a,
  onSelect: r,
  onCancel: i,
  onLoadIcons: o
}) => {
  const u = ue(Lo), [s, c] = tt("simple"), [d, m] = tt(""), [p, h] = tt(t ?? ""), [b, D] = tt(!1), [y, E] = tt(null), _ = wn(null), k = wn(null);
  To(() => {
    if (!l.current || !_.current) return;
    const x = l.current.getBoundingClientRect(), N = _.current.getBoundingClientRect();
    let H = x.bottom + 4, B = x.left;
    H + N.height > window.innerHeight && (H = x.top - N.height - 4), B + N.width > window.innerWidth && (B = Math.max(0, x.right - N.width)), E({ top: H, left: B });
  }, [l]), Pt(() => {
    !a && !b && o().catch(() => D(!0));
  }, [a, b, o]), Pt(() => {
    a && k.current && k.current.focus();
  }, [a]), Fe(!0, { ESCAPE: i }), Pt(() => {
    const x = (H) => {
      _.current && !_.current.contains(H.target) && i();
    }, N = setTimeout(() => document.addEventListener("mousedown", x), 0);
    return () => {
      clearTimeout(N), document.removeEventListener("mousedown", x);
    };
  }, [i]);
  const I = Ro(() => {
    if (!d) return n;
    const x = d.toLowerCase();
    return n.filter(
      (N) => N.prefix.toLowerCase().includes(x) || N.label.toLowerCase().includes(x) || N.terms != null && N.terms.some((H) => H.includes(x))
    );
  }, [n, d]), w = Be((x) => {
    m(x.target.value);
  }, []), v = Be(
    (x) => {
      r(x);
    },
    [r]
  ), C = Be((x) => {
    h(x);
  }, []), O = Be((x) => {
    h(x.target.value);
  }, []), F = Be(() => {
    r(p || null);
  }, [p, r]), A = Be(() => {
    r(null);
  }, [r]), S = Be(async (x) => {
    x.preventDefault(), D(!1);
    try {
      await o();
    } catch {
      D(!0);
    }
  }, [o]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlIconSelect__popup",
      ref: _,
      style: y ? { top: y.top, left: y.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (s === "simple" ? " tlIconSelect__tab--active" : ""),
        onClick: () => c("simple")
      },
      u["js.iconSelect.simpleTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (s === "advanced" ? " tlIconSelect__tab--active" : ""),
        onClick: () => c("advanced")
      },
      u["js.iconSelect.advancedTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__searchIcon", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-magnifying-glass" })), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: k,
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
      !a && !b && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      b && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: S }, u["js.iconSelect.loadError"])),
      a && I.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      a && I.map(
        (x) => x.variants.map((N) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: N.encoded,
            className: "tlIconSelect__iconCell" + (N.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": N.encoded === t,
            tabIndex: 0,
            title: x.label,
            onClick: () => s === "simple" ? v(N.encoded) : C(N.encoded),
            onKeyDown: (H) => {
              (H.key === "Enter" || H.key === " ") && (H.preventDefault(), s === "simple" ? v(N.encoded) : C(N.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(Ne, { encoded: N.encoded })
        ))
      )
    ),
    s === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: p,
        onChange: O
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, p && /* @__PURE__ */ e.createElement(Ne, { encoded: p })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, p ? p.startsWith("css:") ? p.substring(4) : p : ""))),
    s === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: i }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: A }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: F }, u["js.iconSelect.ok"]))
  );
}, Mo = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: Io, useCallback: bt, useRef: jo } = e, Ao = ({ controlId: l, state: t }) => {
  const [n, a] = De(), r = le(), i = ue(Mo), [o, u] = Io(!1), s = jo(null), c = n, d = t.editable !== !1, m = t.disabled === !0, p = t.icons ?? [], h = t.iconsLoaded === !0, b = bt(() => {
    d && !m && u(!0);
  }, [d, m]), D = bt(
    (_) => {
      u(!1), a(_);
    },
    [a]
  ), y = bt(() => {
    u(!1);
  }, []), E = bt(async () => {
    await r("loadIcons");
  }, [r]);
  return d ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: s,
      className: "tlIconSelect__swatch" + (c == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: b,
      disabled: m,
      title: c ?? "",
      "aria-label": i["js.iconSelect.chooseIcon"]
    },
    c ? /* @__PURE__ */ e.createElement(Ne, { encoded: c }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), o && /* @__PURE__ */ e.createElement(
    xo,
    {
      anchorRef: s,
      currentValue: c,
      icons: p,
      iconsLoaded: h,
      onSelect: D,
      onCancel: y,
      onLoadIcons: E
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, c ? /* @__PURE__ */ e.createElement(Ne, { encoded: c }) : null));
}, { useCallback: Ve, useEffect: Bo, useMemo: kn, useRef: Po, useState: Ot } = e, Oo = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, Fo = [1, 2, 3, 4];
function $o(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const a = parseFloat(n[1]), r = n[2] || "px";
  return r === "rem" || r === "em" ? a * t : a;
}
function Ho(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let a = 1;
  for (const r of Fo)
    n >= r && (a = r);
  return a;
}
function Wo(l, t) {
  const n = Oo[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function Uo(l, t) {
  const n = Math.max(1, t), a = {}, r = (m, p) => !!(a[m] && a[m][p]), i = (m, p) => {
    a[m] || (a[m] = {}), a[m][p] = !0;
  }, o = [];
  let u = 0, s = 0;
  const c = (m) => {
    let p = null;
    for (const b of o) b.rowStart === m && (p = b);
    if (!p) return;
    let h = p.colEnd;
    for (; h < n && !r(m, h); ) h++;
    if (h !== p.colEnd) {
      for (let b = p.rowStart; b < p.rowEnd; b++)
        for (let D = p.colEnd; D < h; D++) i(b, D);
      p.colEnd = h;
    }
  };
  for (const m of l) {
    const p = n <= 1 ? 1 : Math.max(1, m.rowSpan || 1);
    let h = Math.min(Wo(m.width, n), n);
    for (; r(u, s); )
      s++, s >= n && (s = 0, u++);
    let b = 0;
    for (let k = s; k < n && !r(u, k); k++)
      b++;
    if (h > b) {
      for (c(u), s = 0, u++; r(u, s); )
        s++, s >= n && (s = 0, u++);
      b = 0;
      for (let k = s; k < n && !r(u, k); k++)
        b++;
      h = Math.min(h, b);
    }
    const D = s, y = s + h, E = u, _ = u + p;
    o.push({ id: m.id, colStart: D, colEnd: y, rowStart: E, rowEnd: _ });
    for (let k = E; k < _; k++)
      for (let I = D; I < y; I++) i(k, I);
    s = y, s >= n && (s = 0, u++);
  }
  c(u);
  let d = 0;
  for (const m of o) m.rowEnd > d && (d = m.rowEnd);
  for (let m = 1; m < d; m++)
    for (let p = 0; p < n; p++) {
      if (r(m, p)) continue;
      const h = o.find((b) => b.rowEnd === m && b.colStart <= p && p < b.colEnd);
      if (h) {
        h.rowEnd = m + 1;
        for (let b = h.colStart; b < h.colEnd; b++) i(m, b);
      }
    }
  return o;
}
const Vo = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.minColWidth ?? "16rem", r = (t.children ?? []).filter((v) => v && v.id), i = Po(null), [o, u] = Ot(1), s = t.editMode === !0;
  Bo(() => {
    const v = i.current;
    if (!v) return;
    const C = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, O = $o(a, C), F = () => u(Ho(v.clientWidth, O));
    F();
    const A = new ResizeObserver(F);
    return A.observe(v), () => A.disconnect();
  }, [a]);
  const c = kn(() => Uo(r, o), [r, o]), d = kn(() => {
    const v = {};
    for (const C of c) v[C.id] = C;
    return v;
  }, [c]), [m, p] = Ot(null), [h, b] = Ot(null), D = Ve((v, C) => {
    if (!s) {
      v.preventDefault();
      return;
    }
    p(C), v.dataTransfer.effectAllowed = "move", v.dataTransfer.setData("text/plain", C);
  }, [s]), y = Ve((v, C) => {
    if (!s || !m || m === C) return;
    v.preventDefault(), v.dataTransfer.dropEffect = "move";
    const O = v.currentTarget.getBoundingClientRect(), F = v.clientX < O.left + O.width / 2;
    b((A) => A && A.id === C && A.before === F ? A : { id: C, before: F });
  }, [s, m]), E = Ve(() => {
  }, []), _ = Ve((v, C, O) => {
    const F = r.map((N) => N.id), A = F.indexOf(v);
    if (A < 0) return;
    F.splice(A, 1);
    const S = F.indexOf(C);
    if (S < 0) {
      F.splice(A, 0, v);
      return;
    }
    const x = O ? S : S + 1;
    F.splice(x, 0, v), n("reorder", { order: F });
  }, [r, n]), k = Ve((v, C) => {
    if (!s || !m || m === C) return;
    v.preventDefault();
    const O = v.currentTarget.getBoundingClientRect(), F = v.clientX < O.left + O.width / 2;
    _(m, C, F), p(null), b(null);
  }, [s, m, _]), I = Ve(() => {
    p(null), b(null);
  }, []), w = {
    display: "grid",
    gridTemplateColumns: `repeat(${o}, 1fr)`,
    gap: "1rem"
  };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: i,
      className: "tlDashboard" + (s ? " tlDashboard--edit" : "")
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: w }, r.map((v) => {
      const C = d[v.id];
      if (!C) return null;
      const O = {
        gridColumn: `${C.colStart + 1} / ${C.colEnd + 1}`,
        gridRow: `${C.rowStart + 1} / ${C.rowEnd + 1}`
      }, F = ["tlDashboard__tile"];
      return m === v.id && F.push("tlDashboard__tile--dragging"), h && h.id === v.id && F.push(h.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: v.id,
          className: F.join(" "),
          style: O,
          draggable: s,
          onDragStart: (A) => D(A, v.id),
          onDragOver: (A) => y(A, v.id),
          onDragLeave: E,
          onDrop: (A) => k(A, v.id),
          onDragEnd: I
        },
        /* @__PURE__ */ e.createElement(G, { control: v.control }),
        s && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: zo, useRef: Nn, useState: Sn, useEffect: Ko, useLayoutEffect: Yo } = e, Go = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, a) => /* @__PURE__ */ e.createElement("span", { key: a, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(G, { control: n }))));
}, Xo = ({ group: l }) => {
  var m, p;
  const [t, n] = Sn(!1), [a, r] = Sn({}), i = Nn(null), o = Nn(null), u = zo(() => {
    n((h) => !h);
  }, []);
  Yo(() => {
    if (!t) return;
    const h = () => {
      const b = i.current;
      if (!b) return;
      const D = b.getBoundingClientRect();
      r({
        position: "fixed",
        top: D.bottom + 4,
        right: Math.max(8, window.innerWidth - D.right),
        left: "auto"
      });
    };
    return h(), window.addEventListener("resize", h), window.addEventListener("scroll", h, !0), () => {
      window.removeEventListener("resize", h), window.removeEventListener("scroll", h, !0);
    };
  }, [t]), Ko(() => {
    if (!t) return;
    const h = (b) => {
      o.current && !o.current.contains(b.target) && i.current && !i.current.contains(b.target) && n(!1);
    };
    return document.addEventListener("mousedown", h), () => document.removeEventListener("mousedown", h);
  }, [t]), Fe(t, { ESCAPE: () => n(!1) }), Gt(t, o, "first");
  const s = l.items.filter((h) => h != null);
  if (s.length === 0) return null;
  if (s.length === 1 && !((m = l.subGroups) != null && m.length) && !l.icon)
    return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(G, { control: s[0] })));
  const c = l.label ?? l.name, d = !!l.icon;
  return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--menu" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: i,
      type: "button",
      className: "tlToolbar__menuTrigger" + (d ? " tlToolbar__menuTrigger--icon" : ""),
      onMouseDown: (h) => h.preventDefault(),
      onClick: u,
      "aria-expanded": t,
      "aria-haspopup": "true",
      "aria-label": d ? c : void 0,
      title: d ? c : void 0
    },
    d ? /* @__PURE__ */ e.createElement(Ne, { encoded: l.icon, className: "tlToolbar__menuIcon" }) : /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement("span", null, c), /* @__PURE__ */ e.createElement("svg", { className: "tlToolbar__chevron", viewBox: "0 0 24 24", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("polyline", { points: "6,9 12,15 18,9" })))
  ), xn.createPortal(
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: o,
        className: "tlToolbar__dropdown",
        role: "menu",
        hidden: !t,
        style: t ? a : void 0,
        onClick: () => n(!1)
      },
      s.map((h, b) => /* @__PURE__ */ e.createElement("div", { key: b, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(G, { control: h }))),
      (p = l.subGroups) == null ? void 0 : p.map((h, b) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${b}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), h.items.map((D, y) => /* @__PURE__ */ e.createElement("div", { key: y, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(G, { control: D })))))
    ),
    document.body
  ));
}, qo = ({ controlId: l }) => {
  const a = (X().groups ?? []).filter((r) => r.items.some((i) => i != null));
  return a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, a.map((r, i) => /* @__PURE__ */ e.createElement(e.Fragment, { key: r.name }, i > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), r.display === "menu" ? /* @__PURE__ */ e.createElement(Xo, { group: r }) : /* @__PURE__ */ e.createElement(Go, { group: r }))));
}, Zo = ({ frame: l, covered: t }) => {
  const [n, a] = at(), r = [
    "tlTileStack__frame",
    t ? "tlTileStack__frame--covered" : "",
    n
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(rt, { host: a }, /* @__PURE__ */ e.createElement("div", { className: r }, /* @__PURE__ */ e.createElement(G, { control: l })));
}, Qo = ({ controlId: l }) => {
  const t = X(), [n, a] = at(), r = t.frames ?? [], i = t.activeIndex ?? 0;
  return /* @__PURE__ */ e.createElement(rt, { host: a }, /* @__PURE__ */ e.createElement("div", { id: l, className: n ? "tlTileStack " + n : "tlTileStack" }, r.map((o, u) => /* @__PURE__ */ e.createElement(Zo, { key: o.controlId, frame: o, covered: u !== i }))));
}, Jo = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.content, r = t.breadcrumb ?? null;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAdaptiveDetail" }, r && r.length > 0 && /* @__PURE__ */ e.createElement("nav", { className: "tlAdaptiveDetail__breadcrumb", "aria-label": "Breadcrumb" }, r.map((i, o) => {
    const u = o === r.length - 1;
    return /* @__PURE__ */ e.createElement(e.Fragment, { key: i.depth }, o > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__sep" }, "›"), u ? /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__crumb tlAdaptiveDetail__crumb--current" }, i.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlAdaptiveDetail__crumb",
        onClick: () => n("navigate", { depth: i.depth })
      },
      i.label
    ));
  })), /* @__PURE__ */ e.createElement("div", { className: "tlAdaptiveDetail__content" }, a && /* @__PURE__ */ e.createElement(G, { control: a })));
}, es = ({ controlId: l }) => {
  const n = X().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((a, r) => /* @__PURE__ */ e.createElement(G, { key: r, control: a })));
}, ts = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), ns = {
  "js.sidebar.openDrawer": "Open navigation"
}, ls = ({ controlId: l }) => {
  const t = le(), n = ue(ns);
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
K("TLButton", Dl);
K("TLUploadButton", Tl);
K("TLToggleButton", Ll);
K("TLTextInput", dl);
K("TLPasswordInput", pl);
K("TLNumberInput", hl);
K("TLDatePicker", gl);
K("TLSelect", vl);
K("TLBooleanChoice", Cl);
K("TLCheckbox", Nl);
K("TLCounter", xl);
K("TLTabBar", Il);
K("TLFieldList", jl);
K("TLAudioRecorder", Bl);
K("TLAudioPlayer", Ol);
K("TLFileUpload", $l);
K("TLBinaryField", Wl);
K("TLFileChips", zl);
K("TLRelativeTime", Gl);
K("TLAnchor", Xl);
K("TLScrollLink", ql);
K("TLAvatar", Jl);
K("TLDownload", ta);
K("TLPhotoCapture", la);
K("TLPhotoViewer", ra);
K("TLPdfViewer", sa);
K("TLSplitPanel", ca);
K("TLPanel", ha);
K("TLInset", Da);
K("TLMaximizeRoot", ba);
K("TLDeckPane", ga);
K("TLSidebar", Na);
K("TLStack", Sa);
K("TLGrid", Ta);
K("TLCard", Ra);
K("TLAppBar", La);
K("TLBreadcrumb", Ma);
K("TLBottomBar", ja);
K("TLDialog", Pa);
K("TLDialogManager", $a);
K("TLWindow", Va);
K("TLDrawer", Ya);
K("TLMenuRegion", Xa);
K("TLSnackbar", Ja);
K("TLNoticeBar", or);
K("TLMenu", cr);
K("TLAppShell", ur);
K("TLText", dr);
K("TLTableView", br);
K("TLColumnSelect", Er);
K("TLCalendar", Mr);
K("TLFormLayout", Hr);
K("TLFormGroup", Vr);
K("TLFormField", Gr);
K("TLResourceCell", Xr);
K("TLTreeView", ao);
K("TLDropdownSelect", mo);
K("TLColorInput", Do);
K("TLIconSelect", Ao);
K("TLDashboard", Vo);
K("TLToolbar", qo);
K("TLTileStack", Qo);
K("TLAdaptiveDetail", Jo);
K("TLSlot", es);
K("TLSlotContent", ts);
K("TLDrawerToggle", ls);
