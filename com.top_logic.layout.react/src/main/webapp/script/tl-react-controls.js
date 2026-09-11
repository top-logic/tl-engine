import { React as e, useTLFieldValue as Re, useTLCommand as re, useTLState as X, useKeyboardBinding as me, useTLUpload as Ge, useFill as Nt, FillBarrier as Ae, TLChild as G, useI18N as de, useTLDataUrl as Xe, scrollToAnchor as bl, useStandaloneKeyboardScope as $e, useFillHost as ot, FillProvider as st, KeyboardScopeProvider as Xt, useFocusTrap as qt, CMD_VALUE_CHANGED as ut, anchoredOverlayProps as gl, register as K } from "tl-react-bridge";
const { useCallback: cn, useRef: El } = e, vl = 300, _l = ({ controlId: l, state: t }) => {
  const [n, a, r] = Re({
    debounceMs: vl,
    sendOnBlur: t.sendValueOnBlur === !0
  }), s = re(), o = El(!1), u = cn(
    (T) => {
      o.current = !0, a(T.target.value);
    },
    [a]
  ), c = t.commitOnBlur === !0, i = cn(async () => {
    await r(), c && o.current && (o.current = !1, s("commit"));
  }, [r, c, s]), d = t.multiline === !0;
  if (t.editable === !1) {
    const T = "tlReactTextInput tlReactTextInput--immutable" + (d ? " tlReactTextInput--multiline" : "");
    return /* @__PURE__ */ e.createElement(
      "span",
      {
        id: l,
        className: T,
        style: d ? { whiteSpace: "pre-wrap" } : void 0
      },
      n ?? ""
    );
  }
  const m = t.hasError === !0, p = t.hasWarnings === !0, h = t.errorMessage, g = [
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
      onBlur: i,
      disabled: t.disabled === !0,
      className: g,
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
      onBlur: i,
      disabled: t.disabled === !0,
      className: g,
      "aria-invalid": m || void 0,
      title: m && h ? h : void 0
    }
  ));
}, { useCallback: un } = e, Cl = 300, yl = ({ controlId: l, state: t }) => {
  const [n, a, r] = Re({ debounceMs: Cl }), s = un(
    (m) => {
      a(m.target.value);
    },
    [a]
  ), o = un(() => {
    r();
  }, [r]);
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, "••••••••");
  const u = t.hasError === !0, c = t.hasWarnings === !0, i = t.errorMessage, d = [
    "tlReactTextInput",
    u ? "tlReactTextInput--error" : "",
    !u && c ? "tlReactTextInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "password",
      value: n ?? "",
      onChange: s,
      onBlur: o,
      disabled: t.disabled === !0,
      className: d,
      "aria-invalid": u || void 0,
      title: u && i ? i : void 0
    }
  ));
}, { useCallback: dn } = e, wl = 300, kl = ({ controlId: l, state: t }) => {
  const [n, a, r] = Re({
    debounceMs: wl,
    sendOnBlur: t.sendValueOnBlur === !0
  }), s = dn(
    (p) => {
      const h = p.target.value;
      a(h === "" ? null : h);
    },
    [a]
  ), o = dn(() => {
    r();
  }, [r]), u = n == null ? "" : String(n);
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, u);
  const c = t.hasError === !0, i = t.hasWarnings === !0, d = t.errorMessage, m = [
    "tlReactNumberInput",
    c ? "tlReactNumberInput--error" : "",
    !c && i ? "tlReactNumberInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      inputMode: t.inputMode ?? "numeric",
      value: u,
      onChange: s,
      onBlur: o,
      disabled: t.disabled === !0,
      className: m,
      "aria-invalid": c || void 0,
      title: c && d ? d : void 0
    }
  ));
}, { useCallback: Nl } = e, Sl = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), r = Nl(
    (c) => {
      a(c.target.value || null);
    },
    [a]
  );
  if (t.editable === !1) {
    const c = t.displayValue ?? n ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, c);
  }
  const s = t.hasError === !0, o = t.hasWarnings === !0, u = [
    "tlReactDatePicker",
    s ? "tlReactDatePicker--error" : "",
    !s && o ? "tlReactDatePicker--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: t.inputType ?? "date",
      value: n ?? "",
      onChange: r,
      disabled: t.disabled === !0,
      className: u,
      "aria-invalid": s || void 0
    }
  ));
}, { useCallback: Dl } = e, Tl = ({ controlId: l, state: t, config: n }) => {
  var d;
  const [a, r] = Re(), s = Dl(
    (m) => {
      r(m.target.value || null);
    },
    [r]
  ), o = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const m = ((d = o.find((p) => p.value === a)) == null ? void 0 : d.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, m);
  }
  const u = t.hasError === !0, c = t.hasWarnings === !0, i = [
    "tlReactSelect",
    u ? "tlReactSelect--error" : "",
    !u && c ? "tlReactSelect--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "select",
    {
      value: a ?? "",
      onChange: s,
      disabled: t.disabled === !0,
      className: i,
      "aria-invalid": u || void 0
    },
    t.nullable !== !1 && /* @__PURE__ */ e.createElement("option", { value: "" }),
    o.map((m) => /* @__PURE__ */ e.createElement("option", { key: m.value, value: m.value }, m.label))
  ));
}, { useCallback: Rl } = e, Ll = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), r = t.options ?? [], s = t.presentation === "select", o = t.disabled === !0, u = t.hasError === !0, c = t.hasWarnings === !0, i = Rl(
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
    !u && c ? "tlBooleanChoice--warning" : ""
  ].filter(Boolean).join(" ");
  return s ? /* @__PURE__ */ e.createElement(
    "select",
    {
      id: l,
      className: m + " tlReactSelect",
      value: d >= 0 ? String(d) : "",
      disabled: o,
      "aria-invalid": u || void 0,
      onChange: (p) => i(Number(p.target.value))
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
        onChange: () => i(h)
      }
    ), /* @__PURE__ */ e.createElement("span", { className: "tlBooleanChoice__label" }, p.label)))
  );
}, { useCallback: xl, useRef: Ml, useEffect: Il } = e, jl = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), r = t.triState === !0, s = Ml(null);
  Il(() => {
    s.current && (s.current.indeterminate = r && n !== !0 && n !== !1);
  }, [r, n]);
  const o = xl(
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
        ref: s,
        checked: n === !0,
        disabled: !0,
        className: "tlReactCheckbox tlReactCheckbox--immutable"
      }
    );
  const u = t.hasError === !0, c = t.hasWarnings === !0, i = [
    "tlReactCheckbox",
    u ? "tlReactCheckbox--error" : "",
    !u && c ? "tlReactCheckbox--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: l,
      ref: s,
      checked: n === !0,
      onChange: o,
      disabled: t.disabled === !0,
      className: i,
      "aria-invalid": u || void 0,
      "aria-checked": r && n !== !0 && n !== !1 ? "mixed" : n === !0
    }
  );
};
function De({ encoded: l, className: t }) {
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
const { useCallback: Al } = e, Pl = ({ controlId: l, command: t, label: n, image: a, disabled: r, displayMode: s }) => {
  const o = X(), u = re(), c = t ?? "click", i = n ?? o.label, d = a ?? o.image, m = r ?? o.disabled === !0, p = s ?? o.displayMode ?? "label-only", h = o.hidden === !0, g = o.tooltip, T = o.appearance, y = o.size, E = o.cssClasses, _ = o.navigateUrl, k = Al(() => {
    if (_) {
      window.location.assign(_);
      return;
    }
    u(c);
  }, [u, c, _]), I = o.keyGesture;
  me(I, () => m || h ? !1 : (k(), !0));
  const w = p === "icon-only", v = p === "label-only" || p === "icon-label" || w && !d, C = g ?? (w ? i : void 0), O = C ? `text:${C}` : void 0;
  return h ? null : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: k,
      disabled: m,
      className: "tlReactButton" + (w ? " tlReactButton--iconOnly" : "") + (p === "label-only" ? " tlReactButton--labelOnly" : "") + (T === "link" ? " tlReactButton--link" : "") + (T === "primary" ? " tlReactButton--primary" : "") + (y === "small" ? " tlReactButton--small" : "") + (y === "large" ? " tlReactButton--large" : "") + (E ? " " + E : ""),
      "data-tooltip": O,
      "aria-label": d || w ? i : void 0
    },
    d && /* @__PURE__ */ e.createElement(De, { encoded: d, className: "tlReactButton__image" }),
    v && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, i)
  );
}, Bl = ({ controlId: l }) => {
  const t = X(), n = Ge(), a = e.useRef(null), [r, s] = e.useState(!1), o = t.label ?? "", u = t.image, c = t.disabled === !0, i = t.hidden === !0, d = t.displayMode ?? "label-only", m = t.appearance, p = t.accept, h = t.multiple === !0, g = e.useCallback(() => {
    var I;
    c || r || (I = a.current) == null || I.click();
  }, [c, r]), T = e.useCallback(async (I) => {
    const w = I.target.files;
    if (!w || w.length === 0) return;
    const v = new FormData();
    for (let C = 0; C < w.length; C++)
      v.append("file", w[C], w[C].name);
    I.target.value = "", s(!0);
    try {
      await n(v);
    } finally {
      s(!1);
    }
  }, [n]), y = d === "icon-only", E = d === "icon-only" || d === "icon-label", _ = d === "label-only" || d === "icon-label" || y && !u, k = c || r;
  return /* @__PURE__ */ e.createElement("span", { id: l, style: { display: "contents" } }, /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: a,
      type: "file",
      accept: p && p !== "*" ? p : void 0,
      multiple: h || void 0,
      onChange: T,
      style: { display: "none" }
    }
  ), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: g,
      disabled: k,
      style: i ? { display: "none" } : void 0,
      className: "tlReactButton" + (y ? " tlReactButton--iconOnly" : "") + (m === "link" ? " tlReactButton--link" : "") + (m === "primary" ? " tlReactButton--primary" : ""),
      "aria-label": y ? o : void 0
    },
    E && u && /* @__PURE__ */ e.createElement(De, { encoded: u, className: "tlReactButton__image" }),
    _ && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, o)
  ));
}, { useCallback: Fl } = e, Ol = ({ controlId: l, command: t, label: n, active: a, disabled: r }) => {
  const s = X(), o = re(), u = t ?? "click", c = n ?? s.label, i = a ?? s.active === !0, d = r ?? s.disabled === !0, m = Fl(() => {
    o(u);
  }, [o, u]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: m,
      disabled: d,
      className: "tlReactButton" + (i ? " tlReactButtonActive" : "")
    },
    c
  );
}, $l = ({ controlId: l }) => {
  const t = X(), n = re(), a = t.count ?? 0, r = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Hl } = e, Wl = ({ controlId: l }) => {
  const t = X(), n = re(), a = Nt(!0), r = t.tabs ?? [], s = t.activeTabId, o = Hl((u) => {
    u !== s && n("selectTab", { tabId: u });
  }, [n, s]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar " + a }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, r.map((u) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: u.id,
      role: "tab",
      "aria-selected": u.id === s,
      className: "tlReactTabBar__tab" + (u.id === s ? " tlReactTabBar__tab--active" : ""),
      onClick: () => o(u.id)
    },
    u.icon && /* @__PURE__ */ e.createElement(De, { encoded: u.icon, className: "tlReactTabBar__tabIcon" }),
    u.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, /* @__PURE__ */ e.createElement(Ae, null, t.activeContent && /* @__PURE__ */ e.createElement(G, { control: t.activeContent }))));
}, Ul = ({ controlId: l }) => {
  const t = X(), n = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((r, s) => /* @__PURE__ */ e.createElement("div", { key: s, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(G, { control: r })))));
}, Vl = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, zl = ({ controlId: l }) => {
  const t = X(), n = Ge(), [a, r] = e.useState("idle"), [s, o] = e.useState(null), u = e.useRef(null), c = e.useRef([]), i = e.useRef(null), d = t.status ?? "idle", m = t.error, p = d === "received" ? "idle" : a !== "idle" ? a : d, h = e.useCallback(async () => {
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
        i.current = _, c.current = [];
        const k = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", I = new MediaRecorder(_, k ? { mimeType: k } : void 0);
        u.current = I, I.ondataavailable = (w) => {
          w.data.size > 0 && c.current.push(w.data);
        }, I.onstop = async () => {
          _.getTracks().forEach((C) => C.stop()), i.current = null;
          const w = new Blob(c.current, { type: I.mimeType || "audio/webm" });
          if (c.current = [], w.size === 0) {
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
  }, [a, n]), g = de(Vl), T = p === "recording" ? g["js.audioRecorder.stop"] : p === "uploading" ? g["js.uploading"] : g["js.audioRecorder.record"], y = p === "uploading", E = ["tlAudioRecorder__button"];
  return p === "recording" && E.push("tlAudioRecorder__button--recording"), p === "uploading" && E.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: E.join(" "),
      onClick: h,
      disabled: y,
      title: T,
      "aria-label": T
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${p === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, g[s]), m && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, m));
}, Kl = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Yl = ({ controlId: l }) => {
  const t = X(), n = Xe(), a = !!t.hasAudio, r = t.dataRevision ?? 0, [s, o] = e.useState(a ? "idle" : "disabled"), u = e.useRef(null), c = e.useRef(null), i = e.useRef(r);
  e.useEffect(() => {
    a ? s === "disabled" && o("idle") : (u.current && (u.current.pause(), u.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null), o("disabled"));
  }, [a]), e.useEffect(() => {
    r !== i.current && (i.current = r, u.current && (u.current.pause(), u.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null), (s === "playing" || s === "paused" || s === "loading") && o("idle"));
  }, [r]), e.useEffect(() => () => {
    u.current && (u.current.pause(), u.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null);
  }, []);
  const d = e.useCallback(async () => {
    if (s === "disabled" || s === "loading")
      return;
    if (s === "playing") {
      u.current && u.current.pause(), o("paused");
      return;
    }
    if (s === "paused" && u.current) {
      u.current.play(), o("playing");
      return;
    }
    if (!c.current) {
      o("loading");
      try {
        const y = await fetch(n);
        if (!y.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", y.status), o("idle");
          return;
        }
        const E = await y.blob();
        c.current = URL.createObjectURL(E);
      } catch (y) {
        console.error("[TLAudioPlayer] Fetch error:", y), o("idle");
        return;
      }
    }
    const T = new Audio(c.current);
    u.current = T, T.onended = () => {
      o("idle");
    }, T.play(), o("playing");
  }, [s, n]), m = de(Kl), p = s === "loading" ? m["js.loading"] : s === "playing" ? m["js.audioPlayer.pause"] : s === "disabled" ? m["js.audioPlayer.noAudio"] : m["js.audioPlayer.play"], h = s === "disabled" || s === "loading", g = ["tlAudioPlayer__button"];
  return s === "playing" && g.push("tlAudioPlayer__button--playing"), s === "loading" && g.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: g.join(" "),
      onClick: d,
      disabled: h,
      title: p,
      "aria-label": p
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${s === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Gl = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Xl = ({ controlId: l }) => {
  const t = X(), n = Ge(), [a, r] = e.useState("idle"), [s, o] = e.useState(!1), u = e.useRef(null), c = t.status ?? "idle", i = t.error, d = t.accept ?? "", m = c === "received" ? "idle" : a !== "idle" ? a : c, p = e.useCallback(async (w) => {
    r("uploading");
    const v = new FormData();
    v.append("file", w, w.name), await n(v), r("idle");
  }, [n]), h = e.useCallback((w) => {
    var C;
    const v = (C = w.target.files) == null ? void 0 : C[0];
    v && p(v);
  }, [p]), g = e.useCallback(() => {
    var w;
    a !== "uploading" && ((w = u.current) == null || w.click());
  }, [a]), T = e.useCallback((w) => {
    w.preventDefault(), w.stopPropagation(), o(!0);
  }, []), y = e.useCallback((w) => {
    w.preventDefault(), w.stopPropagation(), o(!1);
  }, []), E = e.useCallback((w) => {
    var C;
    if (w.preventDefault(), w.stopPropagation(), o(!1), a === "uploading") return;
    const v = (C = w.dataTransfer.files) == null ? void 0 : C[0];
    v && p(v);
  }, [a, p]), _ = m === "uploading", k = de(Gl), I = m === "uploading" ? k["js.uploading"] : k["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${s ? " tlFileUpload--dragover" : ""}`,
      onDragOver: T,
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
        onClick: g,
        disabled: _,
        title: I,
        "aria-label": I
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    i && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, i)
  );
}, ql = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, Zl = ({ controlId: l, state: t }) => {
  const a = X() ?? t ?? {}, r = Ge(), s = Xe(), o = de(ql), u = a.editable !== !1, c = !!a.hasData, i = a.fileName ?? "download", d = a.dataRevision ?? 0, m = a.accept ?? "", p = a.status ?? "idle", h = a.error ?? null, [g, T] = e.useState("idle"), [y, E] = e.useState(!1), [_, k] = e.useState(!1), I = e.useRef(null), w = e.useCallback(async () => {
    if (!(!c || _)) {
      k(!0);
      try {
        const B = s + (s.includes("?") ? "&" : "?") + "rev=" + d, L = await fetch(B);
        if (!L.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", L.status);
          return;
        }
        const R = await L.blob(), V = URL.createObjectURL(R), f = document.createElement("a");
        f.href = V, f.download = i, f.style.display = "none", document.body.appendChild(f), f.click(), document.body.removeChild(f), URL.revokeObjectURL(V);
      } catch (B) {
        console.error("[TLBinaryField] Fetch error:", B);
      } finally {
        k(!1);
      }
    }
  }, [c, _, s, d, i]), v = e.useCallback(async (B) => {
    T("uploading");
    const L = new FormData();
    L.append("file", B, B.name), await r(L), T("idle");
  }, [r]), C = (p === "received" ? "idle" : g !== "idle" ? g : p) === "uploading", O = e.useCallback((B) => {
    var R;
    const L = (R = B.target.files) == null ? void 0 : R[0];
    L && v(L);
  }, [v]), $ = e.useCallback(() => {
    var B;
    C || (B = I.current) == null || B.click();
  }, [C]), A = e.useCallback((B) => {
    B.preventDefault(), B.stopPropagation(), E(!0);
  }, []), D = e.useCallback((B) => {
    B.preventDefault(), B.stopPropagation(), E(!1);
  }, []), M = e.useCallback((B) => {
    var R;
    if (B.preventDefault(), B.stopPropagation(), E(!1), C) return;
    const L = (R = B.dataTransfer.files) == null ? void 0 : R[0];
    L && v(L);
  }, [C, v]), S = _ ? o["js.downloading"] : o["js.download.file"].replace("{0}", i), W = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (_ ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: w,
      disabled: _,
      title: S,
      "aria-label": S
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: i }, i));
  if (!u)
    return c ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, W) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, o["js.download.noFile"]));
  const P = C, j = C ? o["js.uploading"] : o["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlBinaryField tlFileUpload${y ? " tlFileUpload--dragover" : ""}`,
      onDragOver: A,
      onDragLeave: D,
      onDrop: M
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
        className: "tlFileUpload__button" + (P ? " tlFileUpload__button--uploading" : ""),
        onClick: $,
        disabled: P,
        title: j,
        "aria-label": j
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    c && W,
    h && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, h)
  );
}, Ql = {
  "js.fileChips.add": "Add file",
  "js.fileChips.remove": "Remove {0}",
  "js.uploading": "Uploading…",
  "js.download.file": "Download {0}"
};
function Jl(l) {
  return l < 1024 ? l + " B" : l < 1024 * 1024 ? (l / 1024).toFixed(l < 10 * 1024 ? 1 : 0) + " KB" : (l / (1024 * 1024)).toFixed(1) + " MB";
}
const ea = ({ controlId: l }) => {
  const t = X(), n = re(), a = Ge(), r = Xe(), s = de(Ql), o = t.chips ?? [], u = t.editable === !0, [c, i] = e.useState(!1), [d, m] = e.useState(!1), p = e.useRef(null), h = e.useCallback(async (w) => {
    const v = Array.from(w);
    if (v.length !== 0) {
      i(!0);
      try {
        const C = new FormData();
        for (const O of v)
          C.append("file", O, O.name);
        await a(C);
      } finally {
        i(!1);
      }
    }
  }, [a]), g = e.useCallback(async (w) => {
    if (w.hasData)
      try {
        const v = r + "&key=" + encodeURIComponent(w.key), C = await fetch(v);
        if (!C.ok) {
          console.error("[TLFileChips] Failed to fetch data:", C.status);
          return;
        }
        const O = await C.blob(), $ = URL.createObjectURL(O), A = document.createElement("a");
        A.href = $, A.download = w.name, A.style.display = "none", document.body.appendChild(A), A.click(), document.body.removeChild(A), URL.revokeObjectURL($);
      } catch (v) {
        console.error("[TLFileChips] Fetch error:", v);
      }
  }, [r]), T = e.useCallback((w) => {
    w.target.files && h(w.target.files), w.target.value = "";
  }, [h]), y = e.useCallback(() => {
    var w;
    c || (w = p.current) == null || w.click();
  }, [c]), E = e.useCallback((w) => {
    u && (w.preventDefault(), w.stopPropagation(), m(!0));
  }, [u]), _ = e.useCallback((w) => {
    u && (w.preventDefault(), w.stopPropagation(), m(!1));
  }, [u]), k = e.useCallback((w) => {
    u && (w.preventDefault(), w.stopPropagation(), m(!1), !c && w.dataTransfer.files && h(w.dataTransfer.files));
  }, [u, c, h]), I = [
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
      const v = s["js.download.file"].replace("{0}", w.name), C = s["js.fileChips.remove"].replace("{0}", w.name);
      return /* @__PURE__ */ e.createElement("span", { key: w.key, className: "tlFileChip" }, /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__main",
          onClick: () => g(w),
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
        w.size != null && /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__size" }, Jl(w.size))
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
        onChange: T,
        style: { display: "none" }
      }
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileChips__add" + (c ? " tlFileChips__add--uploading" : ""),
        onClick: y,
        disabled: c,
        title: c ? s["js.uploading"] : s["js.fileChips.add"]
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
      /* @__PURE__ */ e.createElement("span", null, c ? s["js.uploading"] : s["js.fileChips.add"])
    ))
  );
}, ta = 3e4;
function na(l, t) {
  const n = Math.round((l - Date.now()) / 1e3), a = Math.abs(n), r = new Intl.RelativeTimeFormat(t, { numeric: "auto" });
  return a < 60 ? r.format(Math.trunc(n / 1), "second") : a < 3600 ? r.format(Math.trunc(n / 60), "minute") : a < 86400 ? r.format(Math.trunc(n / 3600), "hour") : a < 7 * 86400 ? r.format(Math.trunc(n / 86400), "day") : new Date(l).toLocaleDateString(t);
}
const la = ({ controlId: l }) => {
  const t = X(), n = t.timestamp, a = t.label ?? void 0, r = t.locale || navigator.language, [, s] = e.useState(0);
  return e.useEffect(() => {
    const o = setInterval(() => s((u) => u + 1), ta);
    return () => clearInterval(o);
  }, []), n == null ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime tlRelativeTime--empty" }) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime", title: a }, na(n, r));
}, aa = ({ controlId: l }) => {
  const t = X(), n = t.anchor ?? void 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAnchor", "data-tl-anchor": n }, t.child && /* @__PURE__ */ e.createElement(G, { control: t.child }));
}, ra = ({ controlId: l }) => {
  const t = X(), n = t.target, a = t.label ?? "";
  if (n == null)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlScrollLink tlScrollLink--empty" });
  const r = (s) => {
    s.preventDefault(), bl(n);
  };
  return /* @__PURE__ */ e.createElement("a", { id: l, className: "tlScrollLink", href: "#", onClick: r }, a);
};
function oa(l) {
  const t = l.trim().split(/\s+/).filter(Boolean);
  return t.length === 0 ? "?" : t.length === 1 ? t[0].slice(0, 2).toUpperCase() : (t[0][0] + t[t.length - 1][0]).toUpperCase();
}
function sa(l) {
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return Math.abs(t) % 360;
}
const ca = ({ controlId: l }) => {
  const n = X().name;
  return n ? /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlAvatar",
      style: { backgroundColor: `hsl(${sa(n)}, 45%, 45%)` },
      title: n,
      "aria-label": n
    },
    oa(n)
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlAvatar tlAvatar--empty" });
}, ia = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, ua = ({ controlId: l }) => {
  const t = X(), n = Xe(), a = re(), r = !!t.hasData, s = t.dataRevision ?? 0, o = t.fileName ?? "download", u = !!t.clearable, [c, i] = e.useState(!1), d = e.useCallback(async () => {
    if (!(!r || c)) {
      i(!0);
      try {
        const g = n + (n.includes("?") ? "&" : "?") + "rev=" + s, T = await fetch(g);
        if (!T.ok) {
          console.error("[TLDownload] Failed to fetch data:", T.status);
          return;
        }
        const y = await T.blob(), E = URL.createObjectURL(y), _ = document.createElement("a");
        _.href = E, _.download = o, _.style.display = "none", document.body.appendChild(_), _.click(), document.body.removeChild(_), URL.revokeObjectURL(E);
      } catch (g) {
        console.error("[TLDownload] Fetch error:", g);
      } finally {
        i(!1);
      }
    }
  }, [r, c, n, s, o]), m = e.useCallback(async () => {
    r && await a("clear");
  }, [r, a]), p = de(ia);
  if (!r)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, p["js.download.noFile"]));
  const h = c ? p["js.downloading"] : p["js.download.file"].replace("{0}", o);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (c ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: d,
      disabled: c,
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
}, da = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, ma = ({ controlId: l }) => {
  const t = X(), n = Ge(), [a, r] = e.useState("idle"), [s, o] = e.useState(null), [u, c] = e.useState(!1), i = e.useRef(null), d = e.useRef(null), m = e.useRef(null), p = e.useRef(null), h = e.useRef(null), g = t.error, T = e.useMemo(
    () => {
      var A;
      return !!(window.isSecureContext && ((A = navigator.mediaDevices) != null && A.getUserMedia));
    },
    []
  ), y = e.useCallback(() => {
    d.current && (d.current.getTracks().forEach((A) => A.stop()), d.current = null), i.current && (i.current.srcObject = null);
  }, []), E = e.useCallback(() => {
    y(), r("idle");
  }, [y]), _ = e.useCallback(async () => {
    var A;
    if (a !== "uploading") {
      if (o(null), !T) {
        (A = p.current) == null || A.click();
        return;
      }
      try {
        const D = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        d.current = D, r("overlayOpen");
      } catch (D) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", D), o("js.photoCapture.error.denied"), r("idle");
      }
    }
  }, [a, T]), k = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const A = i.current, D = m.current;
    if (!A || !D)
      return;
    D.width = A.videoWidth, D.height = A.videoHeight;
    const M = D.getContext("2d");
    M && (M.drawImage(A, 0, 0), y(), r("uploading"), D.toBlob(async (S) => {
      if (!S) {
        r("idle");
        return;
      }
      const W = new FormData();
      W.append("photo", S, "capture.jpg"), await n(W), r("idle");
    }, "image/jpeg", 0.85));
  }, [a, n, y]), I = e.useCallback(async (A) => {
    var S;
    const D = (S = A.target.files) == null ? void 0 : S[0];
    if (!D) return;
    r("uploading");
    const M = new FormData();
    M.append("photo", D, D.name), await n(M), r("idle"), p.current && (p.current.value = "");
  }, [n]);
  e.useEffect(() => {
    a === "overlayOpen" && i.current && d.current && (i.current.srcObject = d.current);
  }, [a]), e.useEffect(() => {
    var D;
    if (a !== "overlayOpen") return;
    (D = h.current) == null || D.focus();
    const A = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = A;
    };
  }, [a]), $e(a === "overlayOpen", { ESCAPE: E }), e.useEffect(() => () => {
    d.current && (d.current.getTracks().forEach((A) => A.stop()), d.current = null);
  }, []);
  const w = de(da), v = a === "uploading" ? w["js.uploading"] : w["js.photoCapture.open"], C = ["tlPhotoCapture__cameraBtn"];
  a === "uploading" && C.push("tlPhotoCapture__cameraBtn--uploading");
  const O = ["tlPhotoCapture__overlayVideo"];
  u && O.push("tlPhotoCapture__overlayVideo--mirrored");
  const $ = ["tlPhotoCapture__mirrorBtn"];
  return u && $.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
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
  )), !T && /* @__PURE__ */ e.createElement(
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
        ref: i,
        className: O.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: $.join(" "),
        onClick: () => c((A) => !A),
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
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, w[s]), g && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, g));
}, pa = {
  "js.photoViewer.alt": "Captured photo"
}, fa = ({ controlId: l }) => {
  const t = X(), n = Xe(), a = !!t.hasPhoto, r = t.dataRevision ?? 0, [s, o] = e.useState(null), u = e.useRef(r);
  e.useEffect(() => {
    if (!a) {
      s && (URL.revokeObjectURL(s), o(null));
      return;
    }
    if (r === u.current && s)
      return;
    u.current = r, s && (URL.revokeObjectURL(s), o(null));
    let i = !1;
    return (async () => {
      try {
        const d = await fetch(n);
        if (!d.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", d.status);
          return;
        }
        const m = await d.blob();
        i || o(URL.createObjectURL(m));
      } catch (d) {
        console.error("[TLPhotoViewer] Fetch error:", d);
      }
    })(), () => {
      i = !0;
    };
  }, [a, r, n]), e.useEffect(() => () => {
    s && URL.revokeObjectURL(s);
  }, []);
  const c = de(pa);
  return !a || !s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: s,
      alt: t.alt || c["js.photoViewer.alt"]
    }
  ));
}, ha = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, ba = ({ controlId: l }) => {
  const t = X(), n = Xe(), a = !!t.hasPdf, r = t.dataRevision ?? 0, s = de(ha), u = n.indexOf("react-api/"), c = u >= 0 ? n.slice(0, u) : n, i = n + "&rev=" + r, d = c + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(i);
  return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: d,
      title: s["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, s["js.pdfViewer.noDocument"]));
}, { useCallback: mn, useRef: Rt } = e, ga = ({ controlId: l }) => {
  const t = X(), n = re(), a = Nt(!0), r = t.orientation, s = t.resizable === !0, o = t.children ?? [], u = r === "horizontal", c = o.length > 0 && o.every((E) => E.collapsed), i = !c && o.some((E) => E.collapsed), d = c ? !u : u, m = Rt(null), p = Rt(null), h = Rt(null), g = mn((E, _) => {
    const k = {
      overflow: E.scrolling || "auto"
    };
    return E.collapsed ? c && !d ? k.flex = "1 0 0%" : k.flex = "0 0 auto" : _ !== void 0 ? k.flex = `0 0 ${_}px` : k.flex = `${E.size} 1 0%`, E.minSize > 0 && !E.collapsed && (k.minWidth = u ? E.minSize : void 0, k.minHeight = u ? void 0 : E.minSize), k;
  }, [u, c, i, d]), T = mn((E, _) => {
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
      const D = p.current;
      if (!D || !h.current) return;
      const S = (u ? A.clientX : A.clientY) - D.startPos, W = D.childBefore.minSize || 0, P = D.childAfter.minSize || 0;
      let j = D.startSizeBefore + S, B = D.startSizeAfter - S;
      j < W && (B += j - W, j = W), B < P && (j += B - P, B = P), h.current[D.splitterIndex] = j, h.current[D.splitterIndex + 1] = B;
      const L = k.querySelectorAll(":scope > .tlSplitPanel__child"), R = L[D.splitterIndex], V = L[D.splitterIndex + 1];
      R && (R.style.flex = `0 0 ${j}px`), V && (V.style.flex = `0 0 ${B}px`);
    }, $ = () => {
      if (document.removeEventListener("mousemove", O), document.removeEventListener("mouseup", $), document.body.style.cursor = "", document.body.style.userSelect = "", h.current) {
        const A = {};
        o.forEach((D, M) => {
          const S = D.control;
          S != null && S.controlId && h.current && (A[S.controlId] = h.current[M]);
        }), n("updateSizes", { sizes: A });
      }
      h.current = null, p.current = null;
    };
    document.addEventListener("mousemove", O), document.addEventListener("mouseup", $), document.body.style.cursor = u ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [o, u, n]), y = [];
  return o.forEach((E, _) => {
    if (y.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${_}`,
          className: `tlSplitPanel__child${E.collapsed && d ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: g(E)
        },
        /* @__PURE__ */ e.createElement(G, { control: E.control })
      )
    ), s && _ < o.length - 1) {
      const k = o[_ + 1];
      !E.collapsed && !k.collapsed && y.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${_}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${r}`,
            onMouseDown: (w) => T(w, _)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: m,
      id: l,
      className: `tlSplitPanel tlSplitPanel--${r}${c ? " tlSplitPanel--allCollapsed" : ""} ${a}`,
      style: {
        display: "flex",
        flexDirection: d ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    y
  );
}, Ht = ({ image: l, className: t }) => {
  if (!l || l === "none") return null;
  const n = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `${t ? t + " " : ""}${n}`, "aria-hidden": "true" });
}, { useCallback: Lt } = e, Ea = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, va = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), _a = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), Ca = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), ya = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), wa = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), ka = ({ controlId: l }) => {
  const t = X(), n = re(), a = de(Ea), r = t.title, s = t.expansionState ?? "NORMALIZED", o = t.showMinimize === !0, u = t.showMaximize === !0, c = t.showPopOut === !0, i = t.fullLine === !0, d = t.fill === !0, m = t.hoverActions === !0, p = t.appearance === "card", h = t.errorMessage, g = s === "MINIMIZED", T = s === "MAXIMIZED", y = s === "HIDDEN", E = Lt(() => {
    n("toggleMinimize");
  }, [n]), _ = Lt(() => {
    n("toggleMaximize");
  }, [n]), k = Lt(() => {
    n("popOut");
  }, [n]), I = Nt(d && !y);
  if (y)
    return null;
  const w = T ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" }, v = o && !T || u && !g || c, C = !!r && r.trim() !== "" || !!t.titleContent || !!t.toolbar || v;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${s.toLowerCase()}${i ? " tlPanel--fullLine" : ""}${I ? " " + I : ""}${m ? " tlPanel--hoverActions" : ""}${p ? " tlPanel--card" : ""}`,
      style: w
    },
    C && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, !!r && r.trim() !== "" && /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, r), t.titleContent && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__titleContent" }, /* @__PURE__ */ e.createElement(G, { control: t.titleContent })), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(G, { control: t.toolbar }), o && !T && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: E,
        title: g ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      g ? /* @__PURE__ */ e.createElement(_a, null) : /* @__PURE__ */ e.createElement(va, null)
    ), u && !g && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: _,
        title: T ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      T ? /* @__PURE__ */ e.createElement(ya, null) : /* @__PURE__ */ e.createElement(Ca, null)
    ), c && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: k,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(wa, null)
    ))),
    !g && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(Ae, null, /* @__PURE__ */ e.createElement(G, { control: t.child }))),
    !g && h && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error tlPanel__error", role: "alert" }, /* @__PURE__ */ e.createElement(Ht, { image: t.errorIcon, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, h)),
    !g && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(G, { control: t.buttonBar }))
  );
}, Na = ({ controlId: l }) => {
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
}, Sa = ({ controlId: l }) => {
  const t = X(), [n, a] = ot();
  return /* @__PURE__ */ e.createElement(st, { host: a }, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: n ? "tlDeckPane " + n : "tlDeckPane",
      style: { width: "100%", height: "100%" }
    },
    t.activeChild && /* @__PURE__ */ e.createElement(G, { control: t.activeChild })
  ));
}, { useCallback: ve, useState: vt, useEffect: Wt, useRef: Ct } = e, Da = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function Ut(l, t, n, a) {
  const r = [];
  for (const s of l)
    if (s.type === "nav") {
      if (s.hidden) continue;
      r.push({ id: s.id, type: "nav", groupId: a });
    } else s.type === "command" ? r.push({ id: s.id, type: "command", groupId: a }) : s.type === "group" && (r.push({ id: s.id, type: "group" }), (n.get(s.id) ?? s.expanded) && !t && r.push(...Ut(s.children, t, n, s.id)));
  return r;
}
const Ye = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(De, { encoded: l, className: "tlSidebar__icon" }) : null, Ta = ({ item: l, active: t, collapsed: n, onSelect: a, tabIndex: r, itemRef: s, onFocus: o }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => a(l.id),
    title: n ? l.label : void 0,
    tabIndex: r,
    ref: s,
    onFocus: () => o(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Ye, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Ye, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), Ra = ({ item: l, collapsed: t, onExecute: n, tabIndex: a, itemRef: r, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: a,
    ref: r,
    onFocus: () => s(l.id)
  },
  /* @__PURE__ */ e.createElement(Ye, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), La = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Ye, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), xa = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), Ma = ({ item: l, activeItemId: t, anchorRect: n, onSelect: a, onExecute: r, onClose: s }) => {
  const o = Ct(null);
  Wt(() => {
    const i = (d) => {
      o.current && !o.current.contains(d.target) && setTimeout(() => s(), 0);
    };
    return document.addEventListener("mousedown", i), () => document.removeEventListener("mousedown", i);
  }, [s]), $e(!0, { ESCAPE: s });
  const u = ve((i) => {
    i.type === "nav" ? (a(i.id), s()) : i.type === "command" && (r(i.id), s());
  }, [a, r, s]), c = {};
  return n && (c.left = n.right, c.top = n.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: o, role: "menu", style: c }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((i) => {
    if (i.type === "nav" && i.hidden) return null;
    if (i.type === "nav" || i.type === "command") {
      const d = i.type === "nav" && i.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: i.id,
          className: "tlSidebar__flyoutItem" + (d ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => u(i)
        },
        /* @__PURE__ */ e.createElement(Ye, { icon: i.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, i.label),
        i.type === "nav" && i.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, i.badge)
      );
    }
    return i.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: i.id, className: "tlSidebar__flyoutSectionHeader" }, i.label) : i.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: i.id, className: "tlSidebar__separator" }) : null;
  }));
}, Ia = ({
  item: l,
  expanded: t,
  activeItemId: n,
  collapsed: a,
  onSelect: r,
  onExecute: s,
  onToggleGroup: o,
  tabIndex: u,
  itemRef: c,
  onFocus: i,
  focusedId: d,
  setItemRef: m,
  onItemFocus: p,
  flyoutGroupId: h,
  onOpenFlyout: g,
  onCloseFlyout: T
}) => {
  const y = Ct(null), [E, _] = vt(null), k = ve(() => {
    a ? h === l.id ? T() : (y.current && _(y.current.getBoundingClientRect()), g(l.id)) : o(l.id);
  }, [a, h, l.id, o, g, T]), I = ve((v) => {
    y.current = v, c(v);
  }, [c]), w = a && h === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (w ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: k,
      title: a ? l.label : void 0,
      "aria-expanded": a ? w : t,
      tabIndex: u,
      ref: I,
      onFocus: () => i(l.id)
    },
    /* @__PURE__ */ e.createElement(Ye, { icon: l.icon }),
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
    Ma,
    {
      item: l,
      activeItemId: n,
      anchorRect: E,
      onSelect: r,
      onExecute: s,
      onClose: T
    }
  ), t && !a && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((v) => /* @__PURE__ */ e.createElement(
    jn,
    {
      key: v.id,
      item: v,
      activeItemId: n,
      collapsed: a,
      onSelect: r,
      onExecute: s,
      onToggleGroup: o,
      focusedId: d,
      setItemRef: m,
      onItemFocus: p,
      groupStates: null,
      flyoutGroupId: h,
      onOpenFlyout: g,
      onCloseFlyout: T
    }
  ))));
}, jn = ({
  item: l,
  activeItemId: t,
  collapsed: n,
  onSelect: a,
  onExecute: r,
  onToggleGroup: s,
  focusedId: o,
  setItemRef: u,
  onItemFocus: c,
  groupStates: i,
  flyoutGroupId: d,
  onOpenFlyout: m,
  onCloseFlyout: p
}) => {
  switch (l.type) {
    case "nav":
      return l.hidden ? null : /* @__PURE__ */ e.createElement(
        Ta,
        {
          item: l,
          active: l.id === t,
          collapsed: n,
          onSelect: a,
          tabIndex: o === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: c
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        Ra,
        {
          item: l,
          collapsed: n,
          onExecute: r,
          tabIndex: o === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: c
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(La, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(xa, null);
    case "group": {
      const h = i ? i.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        Ia,
        {
          item: l,
          expanded: h,
          activeItemId: t,
          collapsed: n,
          onSelect: a,
          onExecute: r,
          onToggleGroup: s,
          tabIndex: o === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: c,
          focusedId: o,
          setItemRef: u,
          onItemFocus: c,
          flyoutGroupId: d,
          onOpenFlyout: m,
          onCloseFlyout: p
        }
      );
    }
    default:
      return null;
  }
}, ja = ({ controlId: l }) => {
  const t = X(), n = re(), a = de(Da), r = t.items ?? [], s = t.activeItemId, o = t.collapsed, u = t.drawerOpen, c = u ? !1 : o, [i, d] = vt(() => {
    const S = /* @__PURE__ */ new Map(), W = (P) => {
      for (const j of P)
        j.type === "group" && (S.set(j.id, j.expanded), W(j.children));
    };
    return W(r), S;
  }), m = ve((S) => {
    d((W) => {
      const P = new Map(W), j = P.get(S) ?? !1;
      return P.set(S, !j), n("toggleGroup", { itemId: S, expanded: !j }), P;
    });
  }, [n]), p = ve((S) => {
    S !== s && n("selectItem", { itemId: S });
  }, [n, s]), h = ve((S) => {
    n("executeCommand", { itemId: S });
  }, [n]), g = ve(() => {
    n("toggleCollapse", {});
  }, [n]), T = ve(() => {
    n("toggleDrawer", {});
  }, [n]), [y, E] = vt(null), _ = ve((S) => {
    E(S);
  }, []), k = ve(() => {
    E(null);
  }, []);
  Wt(() => {
    c || E(null);
  }, [c]);
  const [I, w] = vt(() => {
    const S = Ut(r, c, i);
    return S.length > 0 ? S[0].id : "";
  }), v = Ct(/* @__PURE__ */ new Map()), C = ve((S) => (W) => {
    W ? v.current.set(S, W) : v.current.delete(S);
  }, []), O = ve((S) => {
    w(S);
  }, []), $ = Ct(0), A = ve((S) => {
    w(S), $.current++;
  }, []);
  Wt(() => {
    const S = v.current.get(I);
    S && document.activeElement !== S && S.focus();
  }, [I, $.current]);
  const D = ve((S) => {
    if (S.key === "Escape" && y !== null) {
      S.preventDefault(), k();
      return;
    }
    const W = Ut(r, c, i);
    if (W.length === 0) return;
    const P = W.findIndex((B) => B.id === I);
    if (P < 0) return;
    const j = W[P];
    switch (S.key) {
      case "ArrowDown": {
        S.preventDefault();
        const B = (P + 1) % W.length;
        A(W[B].id);
        break;
      }
      case "ArrowUp": {
        S.preventDefault();
        const B = (P - 1 + W.length) % W.length;
        A(W[B].id);
        break;
      }
      case "Home": {
        S.preventDefault(), A(W[0].id);
        break;
      }
      case "End": {
        S.preventDefault(), A(W[W.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        S.preventDefault(), j.type === "nav" ? p(j.id) : j.type === "command" ? h(j.id) : j.type === "group" && (c ? y === j.id ? k() : _(j.id) : m(j.id));
        break;
      }
      case "ArrowRight": {
        j.type === "group" && !c && ((i.get(j.id) ?? !1) || (S.preventDefault(), m(j.id)));
        break;
      }
      case "ArrowLeft": {
        j.type === "group" && !c && (i.get(j.id) ?? !1) && (S.preventDefault(), m(j.id));
        break;
      }
    }
  }, [
    r,
    c,
    i,
    I,
    y,
    A,
    p,
    h,
    m,
    _,
    k
  ]), M = "tlSidebar" + (c ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: M }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(G, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: T, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": a["js.sidebar.ariaLabel"] }, c ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(G, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(G, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: D }, r.map((S) => /* @__PURE__ */ e.createElement(
    jn,
    {
      key: S.id,
      item: S,
      activeItemId: s,
      collapsed: c,
      onSelect: p,
      onExecute: h,
      onToggleGroup: m,
      focusedId: I,
      setItemRef: C,
      onItemFocus: O,
      groupStates: i,
      flyoutGroupId: y,
      onOpenFlyout: _,
      onCloseFlyout: k
    }
  ))), c ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(G, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(G, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: g,
      title: c ? a["js.sidebar.expand"] : a["js.sidebar.collapse"]
    },
    /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement(
      "path",
      {
        d: c ? "M6 4l4 4-4 4" : "M10 4l-4 4 4 4",
        fill: "none",
        stroke: "currentColor",
        strokeWidth: "2",
        strokeLinecap: "round",
        strokeLinejoin: "round"
      }
    ))
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, /* @__PURE__ */ e.createElement(Ae, null, t.activeContent && /* @__PURE__ */ e.createElement(G, { control: t.activeContent }))));
}, Aa = ({ controlId: l }) => {
  const t = X(), n = t.direction ?? "column", a = t.gap ?? "default", r = t.align ?? "stretch", s = t.wrap === !0, o = t.growFirst === !0, u = t.children ?? [], [c, i] = ot(), d = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${a}`,
    `tlStack--align-${r}`,
    s ? "tlStack--wrap" : "",
    o ? "tlStack--grow-first" : "",
    c,
    t.cssClass ?? ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(st, { host: i }, /* @__PURE__ */ e.createElement("div", { id: l, className: d }, u.map((m, p) => /* @__PURE__ */ e.createElement(G, { key: p, control: m }))));
}, Pa = ({ controlId: l }) => {
  const t = X(), [n, a] = ot();
  return /* @__PURE__ */ e.createElement(st, { host: a }, /* @__PURE__ */ e.createElement("div", { id: l, className: n ? "tlInset " + n : "tlInset" }, t.child && /* @__PURE__ */ e.createElement(G, { control: t.child })));
}, Ba = ({ controlId: l }) => {
  const t = X(), n = t.columns, a = t.minColumnWidth, r = t.gap ?? "default", s = t.children ?? [], o = {};
  return a ? o.gridTemplateColumns = `repeat(auto-fit, minmax(min(${a}, 100%), 1fr))` : n && (o.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${r}`, style: o }, s.map((u, c) => /* @__PURE__ */ e.createElement(G, { key: c, control: u })));
}, Fa = ({ controlId: l }) => {
  const t = X(), n = t.title, a = t.variant ?? "outlined", r = t.padding ?? "default", s = t.headerActions ?? [], o = t.child, u = n != null || s.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${a}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, s.map((c, i) => /* @__PURE__ */ e.createElement(G, { key: i, control: c })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${r}` }, /* @__PURE__ */ e.createElement(Ae, null, /* @__PURE__ */ e.createElement(G, { control: o }))));
}, Oa = ({ controlId: l }) => {
  const t = X(), n = t.title ?? "", a = t.leading, r = t.trailing, s = t.children ?? [], o = t.actions ?? [], u = t.variant ?? "flat", i = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    u === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: i }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(G, { control: a })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, s.map((d, m) => /* @__PURE__ */ e.createElement(G, { key: m, control: d }))), o.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, o.map((d, m) => /* @__PURE__ */ e.createElement(G, { key: m, control: d }))), r && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__trailing" }, /* @__PURE__ */ e.createElement(G, { control: r })));
}, { useCallback: $a } = e, Ha = ({ controlId: l }) => {
  const t = X(), n = re(), a = t.items ?? [], r = $a((s) => {
    n("navigate", { itemId: s });
  }, [n]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, a.map((s, o) => {
    const u = o === a.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: s.id, className: "tlBreadcrumb__entry" }, o > 0 && /* @__PURE__ */ e.createElement(
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
        onClick: () => r(s.id)
      },
      s.label
    ));
  })));
}, { useCallback: Wa } = e, Ua = ({ controlId: l }) => {
  const t = X(), n = re(), a = t.items ?? [], r = t.activeItemId, s = Wa((o) => {
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
        onClick: () => s(o.id),
        "aria-current": u ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + o.icon, "aria-hidden": "true" }), o.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, o.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, o.label)
    );
  }));
}, { useCallback: pn, useRef: Va } = e, za = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), Ka = ({ controlId: l }) => {
  const t = X(), n = re(), a = t.open === !0, r = t.closeOnBackdrop !== !1, s = t.child, o = Va(null), u = pn(() => {
    n("close");
  }, [n]), c = pn((i) => {
    r && i.target === i.currentTarget && u();
  }, [r, u]);
  return a ? /* @__PURE__ */ e.createElement(Xt, null, /* @__PURE__ */ e.createElement(za, { onClose: u }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: c,
      ref: o,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(Ae, null, /* @__PURE__ */ e.createElement(G, { control: s }))
  )) : null;
}, { useEffect: Ya, useRef: Ga } = e, Xa = ({ controlId: l }) => {
  const n = X().dialogs ?? [], a = Ga(n.length);
  return Ya(() => {
    n.length < a.current && n.length > 0, a.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((r) => /* @__PURE__ */ e.createElement(G, { key: r.controlId, control: r })));
}, { useCallback: dt, useRef: We, useState: mt } = e, qa = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), Za = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Qa = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Ja = ({ controlId: l }) => {
  const t = X(), n = re(), a = de(Za), r = t.title ?? "", s = t.width ?? "32rem", o = t.height ?? null, u = t.minHeight ?? null, c = t.resizable === !0, i = t.child, d = t.actions ?? [], m = t.toolbar, p = t.buttonBar, [h, g] = mt(null), [T, y] = mt(null), [E, _] = mt(null), k = We(null), [I, w] = mt(!1), v = We(null), C = We(null), O = We(null), $ = We(null), A = We(null), D = dt(() => {
    n("close");
  }, [n]);
  qt(!0, $, "field");
  const M = dt((B, L) => {
    L.preventDefault();
    const R = $.current;
    if (!R) return;
    const V = R.getBoundingClientRect(), f = !k.current, x = k.current ?? { x: V.left, y: V.top };
    f && (k.current = x, _(x)), A.current = {
      dir: B,
      startX: L.clientX,
      startY: L.clientY,
      startW: V.width,
      startH: V.height,
      startPos: { ...x },
      symmetric: f
    };
    const Y = (Q) => {
      const U = A.current;
      if (!U) return;
      const ee = Q.clientX - U.startX, ce = Q.clientY - U.startY;
      let ne = U.startW, be = U.startH, _e = 0, ke = 0;
      U.symmetric ? (U.dir.includes("e") && (ne = U.startW + 2 * ee), U.dir.includes("w") && (ne = U.startW - 2 * ee), U.dir.includes("s") && (be = U.startH + 2 * ce), U.dir.includes("n") && (be = U.startH - 2 * ce)) : (U.dir.includes("e") && (ne = U.startW + ee), U.dir.includes("w") && (ne = U.startW - ee, _e = ee), U.dir.includes("s") && (be = U.startH + ce), U.dir.includes("n") && (be = U.startH - ce, ke = ce));
      const ye = Math.max(200, ne), Le = Math.max(100, be);
      U.symmetric ? (_e = (U.startW - ye) / 2, ke = (U.startH - Le) / 2) : (U.dir.includes("w") && ye === 200 && (_e = U.startW - 200), U.dir.includes("n") && Le === 100 && (ke = U.startH - 100)), C.current = ye, O.current = Le, g(ye), y(Le);
      const Pe = {
        x: U.startPos.x + _e,
        y: U.startPos.y + ke
      };
      k.current = Pe, _(Pe);
    }, z = () => {
      document.removeEventListener("mousemove", Y), document.removeEventListener("mouseup", z);
      const Q = C.current, U = O.current;
      (Q != null || U != null) && n("resize", {
        ...Q != null ? { width: Math.round(Q) } : {},
        ...U != null ? { height: Math.round(U) } : {}
      }), A.current = null;
    };
    document.addEventListener("mousemove", Y), document.addEventListener("mouseup", z);
  }, [n]), S = dt((B) => {
    if (B.button !== 0 || B.target.closest("button")) return;
    B.preventDefault();
    const L = $.current;
    if (!L) return;
    const R = L.getBoundingClientRect(), V = k.current ?? { x: R.left, y: R.top }, f = B.clientX - V.x, x = B.clientY - V.y, Y = (Q) => {
      const U = window.innerWidth, ee = window.innerHeight;
      let ce = Q.clientX - f, ne = Q.clientY - x;
      const be = L.offsetWidth, _e = L.offsetHeight;
      ce + be > U && (ce = U - be), ne + _e > ee && (ne = ee - _e), ce < 0 && (ce = 0), ne < 0 && (ne = 0);
      const ke = { x: ce, y: ne };
      k.current = ke, _(ke);
    }, z = () => {
      document.removeEventListener("mousemove", Y), document.removeEventListener("mouseup", z);
    };
    document.addEventListener("mousemove", Y), document.addEventListener("mouseup", z);
  }, []), W = dt(() => {
    var B, L;
    if (I) {
      const R = v.current;
      R && (_(R.x !== -1 ? { x: R.x, y: R.y } : null), g(R.w), y(R.h)), w(!1);
    } else {
      const R = $.current, V = R == null ? void 0 : R.getBoundingClientRect();
      v.current = {
        x: ((B = k.current) == null ? void 0 : B.x) ?? (V == null ? void 0 : V.left) ?? -1,
        y: ((L = k.current) == null ? void 0 : L.y) ?? (V == null ? void 0 : V.top) ?? -1,
        w: h ?? (V == null ? void 0 : V.width) ?? null,
        h: T ?? null
      }, w(!0), _({ x: 0, y: 0 }), g(null), y(null);
    }
  }, [I, h, T]), P = I ? { position: "absolute", top: 0, left: 0, width: "100vw", maxWidth: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: h != null ? h + "px" : s,
    ...T != null ? { height: T + "px" } : o != null ? { height: o } : {},
    ...u != null && T == null ? { minHeight: u } : {},
    maxHeight: E ? "100vh" : "80vh",
    ...E ? { position: "absolute", left: E.x + "px", top: E.y + "px" } : {}
  }, j = l + "-title";
  return /* @__PURE__ */ e.createElement(Xt, { modal: !0 }, /* @__PURE__ */ e.createElement(qa, { onClose: D }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: P,
      ref: $,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": j
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${I ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: I ? void 0 : S,
        onDoubleClick: c ? W : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: j }, r),
      m && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(G, { control: m })),
      c && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: W,
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
          onClick: D,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(Ae, null, /* @__PURE__ */ e.createElement(G, { control: i }))),
    (d.length > 0 || p) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, p && /* @__PURE__ */ e.createElement(G, { control: p }), d.map((B, L) => /* @__PURE__ */ e.createElement(G, { key: L, control: B }))),
    c && !I && Qa.map((B) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: B,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${B}`,
        onMouseDown: (L) => M(B, L)
      }
    ))
  ));
}, { useCallback: er } = e, tr = {
  "js.drawer.close": "Close"
}, nr = ({ controlId: l }) => {
  const t = X(), n = re(), a = de(tr), r = t.open === !0, s = t.position ?? "right", o = t.size ?? "medium", u = t.title ?? null, c = t.child, i = er(() => {
    n("close");
  }, [n]);
  $e(r, { ESCAPE: i });
  const d = [
    "tlDrawer",
    `tlDrawer--${s}`,
    `tlDrawer--${o}`,
    r ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: l, className: d, "aria-hidden": !r }, u !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, u), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: i,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, /* @__PURE__ */ e.createElement(Ae, null, c && /* @__PURE__ */ e.createElement(G, { control: c }))));
}, { useCallback: pt, useRef: lr } = e, ar = ({ controlId: l }) => {
  const t = X(), n = re(), a = lr(null), r = t.child, o = (t.trigger ?? "contextmenu") === "click", u = pt((m) => {
    m.preventDefault(), m.stopPropagation(), n("openMenu", { x: m.clientX, y: m.clientY });
  }, [n]), c = pt(() => {
    var p;
    const m = (p = a.current) == null ? void 0 : p.getBoundingClientRect();
    n("openMenu", {
      x: Math.round(m ? m.left : 0),
      y: Math.round(m ? m.bottom : 0)
    });
  }, [n]), i = pt((m) => {
    m.preventDefault(), m.stopPropagation(), c();
  }, [c]), d = pt((m) => {
    (m.key === "Enter" || m.key === " ") && (m.preventDefault(), c());
  }, [c]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenuRegion" + (o ? " tlMenuRegion--click" : ""),
      ref: a,
      onContextMenu: o ? void 0 : u,
      onClick: o ? i : void 0,
      role: o ? "button" : void 0,
      tabIndex: o ? 0 : void 0,
      "aria-haspopup": o ? "menu" : void 0,
      onKeyDown: o ? d : void 0
    },
    r && /* @__PURE__ */ e.createElement(G, { control: r })
  );
}, { useCallback: rr, useEffect: fn, useRef: or, useState: hn } = e, sr = 250, cr = ({ controlId: l }) => {
  const t = X(), n = re(), a = t.message ?? "", r = t.content ?? "", s = t.variant ?? "info", o = t.duration ?? 5e3, u = t.visible === !0, c = t.generation ?? 0, [i, d] = hn(!1), [m, p] = hn(!1), h = or(!1);
  fn(() => {
    h.current = !1;
  }, [c]);
  const g = rr(() => {
    d(!0), setTimeout(() => {
      n("dismiss", { generation: c }), d(!1);
    }, 200);
  }, [n, c]);
  return fn(() => {
    if (!u || o === 0 || m) return;
    const T = setTimeout(g, h.current ? sr : o);
    return () => clearTimeout(T);
  }, [u, o, m, g]), !u && !i ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${s}${i ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite",
      onMouseEnter: () => {
        h.current = !0, p(!0);
      },
      onMouseLeave: () => p(!1)
    },
    r ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: r } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, a)
  );
}, { useCallback: ir, useEffect: bn, useMemo: ur, useRef: dr, useState: mr } = e, pr = 1e3;
function fr(l) {
  const t = Math.max(0, Math.floor(l / 1e3)), n = t % 60, a = Math.floor(t / 60) % 60, r = Math.floor(t / 3600), s = (o) => o < 10 ? `0${o}` : `${o}`;
  return r > 0 ? `${r}:${s(a)}:${s(n)}` : `${a}:${s(n)}`;
}
const hr = ({ controlId: l }) => {
  const t = X(), n = re(), a = t.visible === !0, r = t.severity ?? "info", s = t.text ?? "", o = t.deadline ?? null, u = t.serverNow ?? null, c = t.leadMs ?? null, i = t.actionLabel ?? null, d = t.pingGraceMs ?? null, m = ur(
    () => u != null ? u - Date.now() : 0,
    [u]
  ), [p, h] = mr(0), g = a && o != null;
  bn(() => {
    if (!g) return;
    const I = setInterval(() => h((w) => w + 1), pr);
    return () => clearInterval(I);
  }, [g, o]);
  const T = dr(null);
  bn(() => {
    !g || d == null || o == null || T.current !== o && (Date.now() + m < o + d || (T.current = o, n("deadlinePassed", {})));
  }, [p, g, o, d, m, n]);
  const y = ir(() => {
    i != null && n("action", {});
  }, [n, i]);
  if (!a) return null;
  const E = o != null ? o - (Date.now() + m) : null;
  if (c != null && E != null && E > c) return null;
  const _ = E != null ? fr(E) : null, k = i != null;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlNoticeBar tlNoticeBar--${r}${k ? " tlNoticeBar--clickable" : ""}`,
      role: k ? "button" : "status",
      "aria-live": "polite",
      tabIndex: k ? 0 : void 0,
      title: i ?? void 0,
      "aria-label": k ? `${s} ${i}` : void 0,
      onClick: k ? y : void 0,
      onKeyDown: k ? (I) => {
        (I.key === "Enter" || I.key === " ") && (I.preventDefault(), y());
      } : void 0
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__text" }, s),
    _ !== null && /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__countdown" }, _)
  );
}, { useCallback: xt, useEffect: gn, useRef: br, useState: En } = e, gr = ({ controlId: l }) => {
  const t = X(), n = re(), a = t.open === !0, r = t.anchorId, s = t.anchorX, o = t.anchorY, u = t.items ?? [], c = br(null), [i, d] = En({ top: 0, left: 0 }), [m, p] = En(0), h = u.filter((E) => E.type === "item" && !E.disabled);
  gn(() => {
    var C, O;
    if (!a) return;
    const E = ((C = c.current) == null ? void 0 : C.offsetHeight) ?? 200, _ = ((O = c.current) == null ? void 0 : O.offsetWidth) ?? 200;
    if (s != null && o != null) {
      let $ = o, A = s;
      $ + E > window.innerHeight && ($ = Math.max(0, window.innerHeight - E)), A + _ > window.innerWidth && (A = Math.max(0, window.innerWidth - _)), d({ top: $, left: A }), p(0);
      return;
    }
    if (!r) return;
    const k = document.getElementById(r);
    if (!k) return;
    const I = k.getBoundingClientRect();
    let w = I.bottom + 4, v = I.left;
    w + E > window.innerHeight && (w = I.top - E - 4), v + _ > window.innerWidth && (v = I.right - _), d({ top: w, left: v }), p(0);
  }, [a, r, s, o]);
  const g = xt(() => {
    n("close");
  }, [n]), T = xt((E) => {
    n("selectItem", { itemId: E });
  }, [n]);
  gn(() => {
    if (!a) return;
    const E = (_) => {
      c.current && !c.current.contains(_.target) && g();
    };
    return document.addEventListener("mousedown", E), () => document.removeEventListener("mousedown", E);
  }, [a, g]);
  const y = xt((E) => {
    if (E.key === "Escape") {
      E.preventDefault(), g();
      return;
    }
    if (E.key === "ArrowDown")
      E.preventDefault(), p((_) => (_ + 1) % h.length);
    else if (E.key === "ArrowUp")
      E.preventDefault(), p((_) => (_ - 1 + h.length) % h.length);
    else if (E.key === "Enter" || E.key === " ") {
      E.preventDefault();
      const _ = h[m];
      _ && T(_.id);
    }
  }, [g, T, h, m]);
  return qt(a, c), a ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: c,
      tabIndex: -1,
      style: { position: "fixed", top: i.top, left: i.left },
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
          onClick: () => T(E.id)
        },
        E.icon && /* @__PURE__ */ e.createElement(De, { encoded: E.icon, className: "tlMenu__icon" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, E.label)
      );
    })
  ) : null;
}, Er = 768, vr = ({ controlId: l }) => {
  const t = X(), n = re(), a = Nt(!0);
  e.useEffect(() => {
    const i = window.matchMedia(`(max-width: ${Er}px)`), d = (p) => {
      n("reportDisplayClass", { displayClass: p ? "COMPACT" : "REGULAR" });
    };
    d(i.matches);
    const m = (p) => d(p.matches);
    return i.addEventListener("change", m), () => i.removeEventListener("change", m);
  }, [n]);
  const r = t.header, s = t.notices, o = t.content, u = t.footer, c = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell " + a }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(G, { control: r })), s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__notices" }, /* @__PURE__ */ e.createElement(G, { control: s })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(Ae, null, /* @__PURE__ */ e.createElement(G, { control: o }))), u && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(G, { control: u })), /* @__PURE__ */ e.createElement(G, { control: c }));
}, _r = ({ controlId: l }) => {
  const t = X(), n = t.text ?? "", a = t.cssClass ?? "", r = t.hasTooltip === !0, s = t.role || void 0, o = a ? `tlText ${a}` : "tlText";
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: o,
      role: s,
      "data-tooltip": r ? "key:tooltip" : void 0
    },
    n
  );
}, Cr = ({ isMulti: l, cursorIndex: t, onMove: n, onToggle: a, onSelectAll: r, onActivate: s }) => (me("ArrowUp", () => (n("up", !1, !1), !0)), me("ArrowDown", () => (n("down", !1, !1), !0)), me("Home", () => (n("home", !1, !1), !0)), me("End", () => (n("end", !1, !1), !0)), me("PageUp", () => (n("pageUp", !1, !1), !0)), me("PageDown", () => (n("pageDown", !1, !1), !0)), me("Shift+ArrowUp", () => (n("up", l, !1), !0)), me("Shift+ArrowDown", () => (n("down", l, !1), !0)), me("Shift+Home", () => (n("home", l, !1), !0)), me("Shift+End", () => (n("end", l, !1), !0)), me("Shift+PageUp", () => (n("pageUp", l, !1), !0)), me("Shift+PageDown", () => (n("pageDown", l, !1), !0)), me("Ctrl+ArrowUp", () => (n("up", !1, l), !0)), me("Ctrl+ArrowDown", () => (n("down", !1, l), !0)), me("Space", () => t < 0 ? !1 : (a(), !0)), me("Ctrl+A", () => l ? (r(), !0) : !1), me("Enter", () => s()), null), yr = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all",
  "js.table.groupBy": "Group by this column",
  "js.table.ungroup": "Remove grouping",
  "js.table.fitColumn": "Fit width to content",
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
}, wr = 300, Mt = 50, kr = (l, t) => {
  const n = Array.from(
    l.querySelectorAll(".tlTableView__headerCell, .tlTableView__cell")
  ).filter((r) => r.dataset.col === t);
  if (n.length === 0)
    return 0;
  const a = document.createElement("div");
  a.style.cssText = "position:absolute;top:0;left:0;height:0;overflow:hidden;visibility:hidden;pointer-events:none", l.appendChild(a);
  try {
    const r = n.map((s) => {
      const o = s.cloneNode(!0);
      return o.querySelectorAll(".tlTableView__resizeHandle").forEach((u) => u.remove()), o.querySelectorAll("[id]").forEach((u) => u.removeAttribute("id")), o.style.position = "static", o.style.flex = "none", o.style.width = "max-content", o.style.minWidth = "0", o.style.maxWidth = "none", a.appendChild(o), o;
    });
    return a.appendChild(document.createElement("div")), Math.ceil(r.reduce((s, o) => Math.max(s, o.getBoundingClientRect().width), 0));
  } finally {
    a.remove();
  }
}, Nr = 'input, textarea, select, button, a, [contenteditable="true"], [role="combobox"], [role="listbox"], [role="option"], [role="button"], [role="link"], [role="checkbox"], [role="radio"], [role="switch"], [role="textbox"], [role="spinbutton"], [role="slider"], [role="menu"], [role="menuitem"]';
function ft(l) {
  var n;
  const t = l.target;
  return !!((n = t == null ? void 0 : t.closest) != null && n.call(t, Nr));
}
const Vt = 'input:not([disabled]):not([readonly]), textarea:not([disabled]):not([readonly]), select:not([disabled]), [contenteditable="true"]', vn = Vt + ", button:not([disabled]), a[href]";
function An(l, t) {
  return Array.from(l.querySelectorAll("[data-row][data-col]")).filter((n) => n.dataset.row === t);
}
function It(l, t, n = {}) {
  const a = An(l, t);
  if (n.col) {
    const s = a.find((u) => u.dataset.col === n.col), o = s == null ? void 0 : s.querySelector(Vt);
    if (o) return o;
  }
  if (n.col)
    return null;
  const r = n.last ? [...a].reverse() : a;
  for (const s of r) {
    const o = s.querySelector(Vt);
    if (o) return o;
  }
  return null;
}
const _n = ({ title: l, inCell: t, onClick: n }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    type: "button",
    className: "tlTableView__columnsButton" + (t ? " tlTableView__columnsButton--inCell" : ""),
    title: l,
    "aria-label": l,
    onMouseDown: (a) => a.stopPropagation(),
    onClick: n
  },
  /* @__PURE__ */ e.createElement("i", { className: "bi bi-gear" })
), Sr = ({ controlId: l }) => {
  var nn, ln, an;
  const t = X(), n = re(), a = de(yr), r = e.useRef(null);
  e.useEffect(() => {
    const b = r.current;
    if (!b) return;
    const N = (F) => {
      const Z = F.detail;
      let J = Z.target;
      for (; J && J !== b; ) {
        const ae = J.dataset.row, se = J.dataset.col;
        if (ae != null && se != null) {
          Z.resolved = { key: ae + "|" + se };
          return;
        }
        J = J.parentElement;
      }
    };
    return b.addEventListener("tl-tooltip-resolve", N), () => b.removeEventListener("tl-tooltip-resolve", N);
  }, []);
  const s = t.columns ?? [], o = t.totalRowCount ?? 0, u = t.rows ?? [], c = t.rowHeight ?? 36, i = t.selectionMode ?? "single", d = t.selectedCount ?? 0, m = t.cursorIndex ?? -1, p = t.frozenColumnCount ?? 0, h = t.treeMode ?? !1, g = t.grouping ?? "", T = t.columnSelect ?? !1, y = t.filterBar ?? !1, E = t.namedFilters ?? [], _ = t.activeNamedFilter ?? "", k = t.search ?? "", I = t.filterSaving ?? !1, w = e.useMemo(
    () => s.filter((b) => b.sortPriority && b.sortPriority > 0).length,
    [s]
  ), v = i === "multi", C = 40, O = 20, $ = e.useRef(null), A = e.useRef(null), D = e.useRef(null), M = e.useRef(null), S = e.useRef(null), [W, P] = e.useState({}), j = e.useRef(null), B = e.useRef(!1), L = e.useRef(null), [R, V] = e.useState(null), [f, x] = e.useState(null), [Y, z] = e.useState(null), [Q, U] = e.useState(0);
  e.useEffect(() => {
    const b = D.current;
    if (!b)
      return;
    const N = () => {
      const Z = b.offsetWidth - b.clientWidth;
      U((J) => J === Z ? J : Z);
    };
    N();
    const F = new ResizeObserver(N);
    return F.observe(b), () => F.disconnect();
  }, []), e.useEffect(() => {
    j.current || P({});
  }, [s]);
  const ee = e.useCallback((b) => W[b.name] ?? b.width, [W]), ce = e.useMemo(() => {
    const b = [];
    let N = v && p > 0 ? C : 0;
    for (let F = 0; F < p && F < s.length; F++)
      b.push(N), N += ee(s[F]);
    return b;
  }, [s, p, v, C, ee]), ne = e.useMemo(
    () => s.reduce((b, N, F) => N.pinnedEnd ? b : F, -1),
    [s]
  ), be = e.useMemo(() => {
    const b = s.map(() => 0);
    let N = 0;
    for (let F = s.length - 1; F >= 0; F--)
      s[F].pinnedEnd && (b[F] = N, N += ee(s[F]));
    return b;
  }, [s, ee]), _e = e.useMemo(() => {
    if (p <= 0)
      return 0;
    let b = v ? C : 0;
    for (let N = 0; N < p && N < s.length; N++)
      b += ee(s[N]);
    return b;
  }, [s, p, v, C, ee]), ke = o * c, ye = e.useRef(null), Le = e.useCallback((b, N, F) => {
    if (F.preventDefault(), F.stopPropagation(), F.detail > 1)
      return;
    const Z = F.currentTarget.parentElement, J = Z ? Math.round(Z.getBoundingClientRect().width) : N;
    j.current = { column: b, startX: F.clientX, startWidth: J };
    let ae = F.clientX, se = 0;
    const le = () => {
      const ue = j.current;
      if (!ue) return;
      const ge = Math.max(Mt, ue.startWidth + (ae - ue.startX) + se);
      P((He) => ({ ...He, [ue.column]: ge }));
    }, pe = () => {
      const ue = D.current, ge = $.current;
      if (!ue || !j.current) return;
      const He = ue.getBoundingClientRect(), rn = 40, on = 8, hl = ue.scrollLeft;
      ae > He.right - rn ? ue.scrollLeft += on : ae < He.left + rn && (ue.scrollLeft = Math.max(0, ue.scrollLeft - on));
      const sn = ue.scrollLeft - hl;
      sn !== 0 && (ge && (ge.scrollLeft = ue.scrollLeft), se += sn, le()), ye.current = requestAnimationFrame(pe);
    };
    ye.current = requestAnimationFrame(pe);
    const Ne = (ue) => {
      ae = ue.clientX, le();
    }, fe = (ue) => {
      document.removeEventListener("mousemove", Ne), document.removeEventListener("mouseup", fe), ye.current !== null && (cancelAnimationFrame(ye.current), ye.current = null);
      const ge = j.current;
      if (ge) {
        const He = Math.max(Mt, ge.startWidth + (ue.clientX - ge.startX) + se);
        n("columnResize", { column: ge.column, width: He }), j.current = null, B.current = !0, requestAnimationFrame(() => {
          B.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", Ne), document.addEventListener("mouseup", fe);
  }, [n]), Pe = e.useCallback((b) => {
    const N = r.current;
    if (!N)
      return;
    const F = Math.max(Mt, kr(N, b));
    P((Z) => ({ ...Z, [b]: F })), n("columnResize", { column: b, width: F });
  }, [n]), ct = e.useCallback(() => {
    $.current && D.current && ($.current.scrollLeft = D.current.scrollLeft), M.current !== null && clearTimeout(M.current), M.current = window.setTimeout(() => {
      const b = D.current;
      if (!b) return;
      const N = b.scrollTop, F = Math.ceil(b.clientHeight / c), Z = Math.floor(N / c);
      n("scroll", { start: Z, count: F });
    }, 80);
  }, [n, c]), H = e.useCallback((b, N, F) => {
    if (B.current) return;
    let Z;
    !N || N === "desc" ? Z = "asc" : Z = "desc";
    const J = F.shiftKey ? "add" : "replace";
    n("sort", { column: b, direction: Z, mode: J });
  }, [n]), q = e.useCallback((b, N) => {
    L.current = b, N.dataTransfer.effectAllowed = "move", N.dataTransfer.setData("text/plain", b);
  }, []), oe = e.useCallback((b, N) => {
    var J;
    if (!L.current || L.current === b || (J = s.find((ae) => ae.name === b)) != null && J.pinnedEnd) {
      V(null);
      return;
    }
    N.preventDefault(), N.dataTransfer.dropEffect = "move";
    const F = N.currentTarget.getBoundingClientRect(), Z = N.clientX < F.left + F.width / 2 ? "left" : "right";
    V({ column: b, side: Z });
  }, [s]), ie = e.useCallback((b) => {
    b.preventDefault(), b.stopPropagation();
    const N = L.current;
    if (!N || !R) {
      L.current = null, V(null);
      return;
    }
    let F = s.findIndex((J) => J.name === R.column);
    if (F < 0) {
      L.current = null, V(null);
      return;
    }
    const Z = s.findIndex((J) => J.name === N);
    R.side === "right" && F++, Z < F && F--, n("columnReorder", { column: N, targetIndex: F }), L.current = null, V(null);
  }, [s, R, n]), qe = e.useCallback(() => {
    L.current = null, V(null);
  }, []), Un = e.useCallback((b, N) => {
    var J, ae, se, le;
    const F = window.getSelection();
    if (F && !F.isCollapsed && N.currentTarget.contains(F.anchorNode))
      return;
    if (!ft(N) && ((J = D.current) == null || J.focus({ preventScroll: !0 }), !N.ctrlKey && !N.metaKey && !N.shiftKey)) {
      const pe = (le = (se = (ae = N.target) == null ? void 0 : ae.closest) == null ? void 0 : se.call(ae, "[data-col]")) == null ? void 0 : le.getAttribute("data-col");
      S.current = { index: b, col: pe ?? void 0 };
    }
    const Z = u.find((pe) => pe.index === b);
    ft(N) && (Z != null && Z.selected) && !N.ctrlKey && !N.metaKey && !N.shiftKey || n("select", {
      rowIndex: b,
      ctrlKey: N.ctrlKey || N.metaKey,
      shiftKey: N.shiftKey
    });
  }, [n, u]), Vn = e.useCallback((b, N) => {
    var F;
    ft(N) || ((F = u.find((Z) => Z.index === b)) == null ? void 0 : F.groupCount) == null && n("activate", { rowIndex: b });
  }, [n, u]), zn = e.useCallback((b, N, F) => {
    n("moveSelection", { direction: b, extend: N, move: F });
  }, [n]), Kn = e.useCallback(() => {
    m < 0 || n("select", { rowIndex: m, ctrlKey: v, shiftKey: !1 });
  }, [n, m, v]), Yn = e.useCallback(() => {
    n("selectAll", { selected: !0 });
  }, [n]), Gn = e.useCallback(() => {
    var N;
    if (m < 0)
      return !1;
    const b = document.activeElement;
    return (N = b == null ? void 0 : b.closest) != null && N.call(b, vn) ? !1 : (n("activate", { rowIndex: m }), !0);
  }, [n, m]), Xn = e.useCallback(
    () => !!r.current && r.current.contains(document.activeElement),
    []
  );
  e.useEffect(() => {
    if (m < 0)
      return;
    const b = D.current;
    if (!b)
      return;
    const N = m * c, F = N + c;
    N < b.scrollTop ? b.scrollTop = N : F > b.scrollTop + b.clientHeight && (b.scrollTop = F - b.clientHeight);
  }, [m, c]), e.useEffect(() => {
    const b = S.current, N = D.current;
    if (!b || !N)
      return;
    const F = u.find((ae) => ae.index === b.index);
    if (!F || !It(N, F.id))
      return;
    S.current = null;
    const Z = document.activeElement;
    if (Z && Z !== document.body && !N.contains(Z))
      return;
    const J = It(N, F.id, { col: b.col, last: b.last });
    J && (J.focus({ preventScroll: !0 }), J instanceof HTMLInputElement && J.select());
  }, [u]);
  const qn = e.useCallback((b) => {
    if (b.key !== "Tab")
      return;
    const N = D.current, F = document.activeElement;
    if (!N || !F || !N.contains(F))
      return;
    const Z = F.closest("[data-row][data-col]");
    if (!Z)
      return;
    const J = Z.dataset.row, ae = u.find((ge) => ge.id === J);
    if (!ae)
      return;
    const se = An(N, J).flatMap((ge) => Array.from(ge.querySelectorAll(vn))), le = se.indexOf(F);
    if (le < 0)
      return;
    const pe = !b.shiftKey;
    if (!(pe ? le === se.length - 1 : le === 0))
      return;
    const fe = pe ? ae.index + 1 : ae.index - 1;
    if (fe < 0 || fe >= o)
      return;
    const ue = u.find((ge) => ge.index === fe);
    ue && It(N, ue.id) || (b.preventDefault(), S.current = { index: fe, last: !pe }, n("select", { rowIndex: fe, ctrlKey: !1, shiftKey: !1 }));
  }, [u, o, n]), Zn = e.useCallback((b, N) => {
    N.stopPropagation(), n("select", { rowIndex: b, ctrlKey: !0, shiftKey: !1 });
  }, [n]), Qn = e.useCallback(() => {
    const b = d === o && o > 0;
    n("selectAll", { selected: !b });
  }, [n, d, o]), Jn = e.useCallback((b, N, F) => {
    F.stopPropagation(), n("expand", { rowIndex: b, expanded: N });
  }, [n]), el = e.useCallback((b, N) => {
    N.preventDefault(), x({ x: N.clientX, y: N.clientY, colIdx: b });
  }, []), tl = e.useCallback(() => {
    f && (n("setFrozenColumnCount", { count: f.colIdx + 1 }), x(null));
  }, [f, n]), nl = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), x(null);
  }, [n]), ll = e.useCallback((b) => {
    n("group", { column: b }), x(null);
  }, [n]), al = e.useCallback(() => {
    n("group", { column: "" }), x(null);
  }, [n]), rl = e.useCallback((b) => {
    b.preventDefault(), b.stopPropagation();
    const N = A.current, F = $.current;
    if (!N || !F)
      return;
    const Z = N.clientWidth, J = [{ x: 0, count: 0 }];
    F.querySelectorAll("[data-col-idx]").forEach((pe) => {
      var ue;
      const Ne = Number(pe.dataset.colIdx);
      if ((ue = s[Ne]) != null && ue.pinnedEnd)
        return;
      const fe = pe.getBoundingClientRect().right - N.getBoundingClientRect().left;
      fe > 0 && fe <= Z && J.push({ x: fe, count: Ne + 1 });
    });
    let ae = { x: _e, count: p };
    const se = (pe) => {
      const Ne = pe.clientX - N.getBoundingClientRect().left;
      ae = J.reduce(
        (fe, ue) => Math.abs(ue.x - Ne) < Math.abs(fe.x - Ne) ? ue : fe,
        J[0]
      ), z(ae);
    }, le = () => {
      document.removeEventListener("mousemove", se), document.removeEventListener("mouseup", le), z(null), ae.count !== p && n("setFrozenColumnCount", { count: ae.count });
    };
    document.addEventListener("mousemove", se), document.addEventListener("mouseup", le);
  }, [s, _e, p, n]);
  e.useEffect(() => {
    if (!f) return;
    const b = () => x(null);
    return document.addEventListener("mousedown", b), () => document.removeEventListener("mousedown", b);
  }, [f]), $e(!!f, { ESCAPE: () => x(null) });
  const ol = e.useCallback((b, N) => {
    N.stopPropagation(), N.preventDefault(), n("openFilter", { column: b });
  }, [n]), Qt = e.useCallback((b) => {
    b.stopPropagation(), b.preventDefault(), n("openColumnSelect", {});
  }, [n]), [sl, Jt] = e.useState(k), St = e.useRef(!1), xe = e.useRef(null);
  e.useEffect(() => {
    St.current || Jt(k);
  }, [k]), e.useEffect(() => () => {
    xe.current !== null && clearTimeout(xe.current);
  }, []);
  const it = e.useCallback((b) => {
    xe.current !== null && (clearTimeout(xe.current), xe.current = null), St.current = !1, n("search", { term: b });
  }, [n]), cl = e.useCallback((b) => {
    Jt(b), St.current = !0, xe.current !== null && clearTimeout(xe.current), xe.current = window.setTimeout(() => it(b), wr);
  }, [it]), il = e.useCallback((b) => {
    b.key === "Enter" && (b.preventDefault(), it(b.currentTarget.value));
  }, [it]), ul = e.useCallback((b) => {
    b === _ ? n("clearFilter", {}) : n("applyNamedFilter", { id: b });
  }, [_, n]), dl = e.useCallback((b, N) => {
    N.stopPropagation(), n("deleteNamedFilter", { id: b });
  }, [n]), [Ze, Qe] = e.useState(null), Dt = e.useCallback(() => {
    const b = (Ze ?? "").trim();
    b && (n("saveNamedFilter", { filterName: b }), Qe(null));
  }, [Ze, n]), ml = e.useCallback((b) => {
    b.key === "Enter" ? (b.preventDefault(), Dt()) : b.key === "Escape" && (b.preventDefault(), Qe(null));
  }, [Dt]), en = s.reduce((b, N) => b + ee(N), 0) + (v ? C : 0), Tt = T && s.length > 0 && !!s[s.length - 1].pinnedEnd, Je = T && !Tt ? 32 : 0, pl = d === o && o > 0, tn = d > 0 && d < o, fl = e.useCallback((b) => {
    b && (b.indeterminate = tn);
  }, [tn]);
  return /* @__PURE__ */ e.createElement(Xt, { active: Xn }, /* @__PURE__ */ e.createElement(
    Cr,
    {
      isMulti: v,
      cursorIndex: m,
      onMove: zn,
      onToggle: Kn,
      onSelectAll: Yn,
      onActivate: Gn
    }
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: r,
      id: l,
      className: "tlTableView",
      "data-tooltip": "dynamic",
      onDragOver: (b) => {
        if (!L.current) return;
        b.preventDefault();
        const N = D.current, F = $.current;
        if (!N) return;
        const Z = N.getBoundingClientRect(), J = 40, ae = 8;
        b.clientX < Z.left + J ? N.scrollLeft = Math.max(0, N.scrollLeft - ae) : b.clientX > Z.right - J && (N.scrollLeft += ae), F && (F.scrollLeft = N.scrollLeft);
      },
      onDrop: ie
    },
    y && /* @__PURE__ */ e.createElement("div", { className: "tlTableView__filterBar" }, E.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlTableView__filterChips" }, E.map((b) => {
      const N = b.id === _;
      return /* @__PURE__ */ e.createElement(
        "span",
        {
          key: b.id,
          className: "tlTableView__chip" + (N ? " tlTableView__chip--active" : "")
        },
        /* @__PURE__ */ e.createElement(
          "button",
          {
            type: "button",
            className: "tlTableView__chipLabel",
            "aria-pressed": N,
            title: N ? a["js.table.clearFilter"] : b.label,
            onClick: () => ul(b.id)
          },
          b.label
        ),
        b.deletable && /* @__PURE__ */ e.createElement(
          "button",
          {
            type: "button",
            className: "tlTableView__chipRemove",
            title: a["js.table.deleteFilter"],
            "aria-label": a["js.table.deleteFilter"],
            onClick: (F) => dl(b.id, F)
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
        value: sl,
        onChange: (b) => cl(b.target.value),
        onKeyDown: il
      }
    )), I && (Ze === null ? /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__barButton",
        title: a["js.table.saveFilter"],
        "aria-label": a["js.table.saveFilter"],
        onClick: () => Qe("")
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
        value: Ze,
        onChange: (b) => Qe(b.target.value),
        onKeyDown: ml
      }
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__barButton",
        title: a["js.table.saveFilter"],
        "aria-label": a["js.table.saveFilter"],
        disabled: !Ze.trim(),
        onClick: Dt
      },
      /* @__PURE__ */ e.createElement("i", { className: "bi bi-check-lg" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__barButton",
        title: a["js.table.cancelSave"],
        "aria-label": a["js.table.cancelSave"],
        onClick: () => Qe(null)
      },
      /* @__PURE__ */ e.createElement("i", { className: "bi bi-x-lg" })
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerArea", ref: A }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: $ }, /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerRow",
        style: { minWidth: en, paddingRight: Je + Q }
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
          onDragOver: (b) => {
            L.current && (b.preventDefault(), b.dataTransfer.dropEffect = "move", s.length > 0 && s[0].name !== L.current && V({ column: s[0].name, side: "left" }));
          }
        },
        /* @__PURE__ */ e.createElement(
          "input",
          {
            type: "checkbox",
            ref: fl,
            className: "tlTableView__checkbox",
            checked: pl,
            onChange: Qn
          }
        )
      ),
      s.map((b, N) => {
        const F = ee(b);
        let Z = "tlTableView__headerCell";
        b.sortable && (Z += " tlTableView__headerCell--sortable"), R && R.column === b.name && (Z += " tlTableView__headerCell--dragOver-" + R.side);
        const J = N < p, ae = N === p - 1;
        J && (Z += " tlTableView__headerCell--frozen"), ae && (Z += " tlTableView__headerCell--frozenLast");
        const se = !!b.pinnedEnd;
        return se && (Z += " tlTableView__headerCell--pinnedEnd"), se && N === ne + 1 && (Z += " tlTableView__headerCell--pinnedEndFirst"), b.cssClass && (Z += " " + b.cssClass), /* @__PURE__ */ e.createElement(
          "div",
          {
            key: b.name,
            className: Z,
            "data-col": b.name,
            "data-col-idx": N,
            style: {
              // The last column the user arranges takes the space left over, in the heading
              // exactly as in the rows - otherwise the two drift apart as soon as the columns
              // no longer fill the table. The configured width is the flex basis, so the cell
              // is that wide whatever its content measures: a heading whose label, funnel and
              // sort mark need more room than the user gave the column keeps the column's
              // width and clips the label instead of pushing its own tail - the resize handle
              // included - under the column behind it.
              ...N === ne && !J ? { flex: `1 0 ${F}px`, minWidth: F } : { width: F, minWidth: F },
              position: J || se ? "sticky" : "relative",
              ...J ? { left: ce[N], zIndex: 2 } : {},
              // The header ends with the reserve the body's scrollbar and, where it is not in a
              // heading, the column button take; its cells therefore stick that much further
              // from the right edge than the body's - which is what puts a heading above its
              // column at every scroll position.
              ...se ? {
                right: be[N] + Je + Q,
                zIndex: 2
              } : {}
            },
            draggable: !se,
            onClick: b.sortable ? (le) => H(b.name, b.sortDirection, le) : void 0,
            onContextMenu: (le) => el(N, le),
            onDragStart: (le) => q(b.name, le),
            onDragOver: (le) => oe(b.name, le),
            onDrop: ie,
            onDragEnd: qe
          },
          /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, b.label),
          b.name === g && /* @__PURE__ */ e.createElement(
            "i",
            {
              className: "tlTableView__groupMark bi bi-collection",
              title: a["js.table.grouped"],
              "aria-hidden": "true"
            }
          ),
          b.filterable && /* @__PURE__ */ e.createElement(
            "button",
            {
              type: "button",
              className: "tlTableView__filterButton" + (b.filterActive ? " tlTableView__filterButton--active" : ""),
              title: a["js.table.filter"],
              style: {
                border: "none",
                background: "transparent",
                cursor: "pointer",
                padding: "0 4px",
                color: b.filterActive ? "#1565c0" : "inherit"
              },
              onMouseDown: (le) => le.stopPropagation(),
              onClick: (le) => ol(b.name, le)
            },
            /* @__PURE__ */ e.createElement("i", { className: b.filterActive ? "bi bi-funnel-fill" : "bi bi-funnel" })
          ),
          b.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, b.sortDirection === "asc" ? "▲" : "▼", w > 1 && b.sortPriority != null && b.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, b.sortPriority)),
          Tt && N === s.length - 1 && /* @__PURE__ */ e.createElement(_n, { title: a["js.table.columns"], inCell: !0, onClick: Qt }),
          !se && /* @__PURE__ */ e.createElement(
            "div",
            {
              className: "tlTableView__resizeHandle",
              onMouseDown: (le) => Le(b.name, F, le),
              onClick: (le) => le.stopPropagation(),
              onDoubleClick: (le) => {
                le.stopPropagation(), Pe(b.name);
              }
            }
          )
        );
      }),
      /* @__PURE__ */ e.createElement(
        "div",
        {
          style: { flex: "0 0 0", minHeight: "100%" },
          onDragOver: (b) => {
            if (L.current && ne >= 0) {
              const N = s[ne];
              N.name !== L.current && (b.preventDefault(), b.dataTransfer.dropEffect = "move", V({ column: N.name, side: "right" }));
            }
          },
          onDrop: ie
        }
      )
    )), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__frozenSplitter" + (Y ? " tlTableView__frozenSplitter--active" : ""),
        style: { left: _e },
        title: a["js.table.freezeSplitter"],
        onMouseDown: rl
      }
    ), T && !Tt && /* @__PURE__ */ e.createElement(_n, { title: a["js.table.columns"], onClick: Qt })),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: D,
        className: "tlTableView__body",
        onScroll: ct,
        onKeyDown: qn,
        tabIndex: 0
      },
      /* @__PURE__ */ e.createElement("div", { style: {
        height: ke,
        position: "relative",
        minWidth: en,
        paddingRight: Je
      } }, u.map((b) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: b.id,
          className: "tlTableView__row" + (b.selected ? " tlTableView__row--selected" : "") + (b.index === m ? " tlTableView__row--cursor" : "") + (b.groupCount != null ? " tlTableView__row--group" : ""),
          style: {
            position: "absolute",
            top: b.index * c,
            height: c,
            // Spans the spacer, hence the body, so a pinned cell reaches its right edge. The
            // cells stop in front of the reserve, exactly as the header's do.
            left: 0,
            right: 0,
            paddingRight: Je
          },
          onMouseDown: (N) => {
            (N.shiftKey || N.ctrlKey || N.metaKey || N.detail > 1) && !ft(N) && N.preventDefault();
          },
          onClick: (N) => Un(b.index, N),
          onDoubleClick: (N) => Vn(b.index, N)
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
            onClick: (N) => N.stopPropagation()
          },
          b.groupCount == null && /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: b.selected,
              onChange: () => {
              },
              onClick: (N) => Zn(b.index, N),
              tabIndex: -1
            }
          )
        ),
        s.map((N, F) => {
          const Z = ee(N), J = F < p, ae = F === p - 1;
          let se = "tlTableView__cell";
          J && (se += " tlTableView__cell--frozen"), ae && (se += " tlTableView__cell--frozenLast");
          const le = !!N.pinnedEnd;
          le && (se += " tlTableView__cell--pinnedEnd"), le && F === ne + 1 && (se += " tlTableView__cell--pinnedEndFirst"), N.cssClass && (se += " " + N.cssClass);
          const pe = h && F === 0, Ne = b.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: N.name,
              className: se,
              "data-row": b.id,
              "data-col": N.name,
              style: {
                // The last column the user arranges takes the space left over; a pinned
                // column keeps its width, so the space stays in front of it. The configured
                // width is the flex basis, as in the heading, so that cell and heading are
                // the same width whatever either of them holds.
                ...F === ne && !J ? { flex: `1 0 ${Z}px`, minWidth: Z } : { width: Z, minWidth: Z },
                ...J ? { position: "sticky", left: ce[F], zIndex: 2 } : {},
                ...le ? {
                  position: "sticky",
                  right: be[F] + Je,
                  zIndex: 2
                } : {}
              }
            },
            pe ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: Ne * O } }, b.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (fe) => Jn(b.index, !b.expanded, fe)
              },
              b.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), b.cells[N.name] && /* @__PURE__ */ e.createElement(G, { control: b.cells[N.name] }), b.groupCount != null && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__groupCount" }, "(", b.groupCount, ")")) : b.cells[N.name] && /* @__PURE__ */ e.createElement(G, { control: b.cells[N.name] })
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
        onMouseDown: (b) => b.stopPropagation()
      },
      f.colIdx + 1 !== p && !((nn = s[f.colIdx]) != null && nn.pinnedEnd) && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: tl }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      p > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: nl }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"])),
      !((ln = s[f.colIdx]) != null && ln.pinnedEnd) && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlMenu__item",
          role: "menuitem",
          onClick: () => {
            Pe(s[f.colIdx].name), x(null);
          }
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.fitColumn"])
      ),
      ((an = s[f.colIdx]) == null ? void 0 : an.groupable) && s[f.colIdx].name !== g && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlMenu__item",
          role: "menuitem",
          onClick: () => ll(s[f.colIdx].name)
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.groupBy"])
      ),
      g !== "" && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: al }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.ungroup"]))
    )
  ));
}, Dr = {
  "js.table.columnSearch": "Find column",
  "js.table.groupBy": "Group by this column",
  "js.table.ungroup": "Remove grouping"
}, Tr = ({ controlId: l }) => {
  const t = X(), n = re(), a = de(Dr), r = t.entries ?? [], s = r.filter((v) => v.visible).length, [o, u] = e.useState(""), c = o.trim().toLowerCase(), i = c ? r.filter((v) => v.label.toLowerCase().includes(c)) : r, d = e.useRef(null), m = e.useRef(null), [p, h] = e.useState(null), g = e.useCallback((v) => {
    m.current = v, h(v);
  }, []), T = e.useCallback((v, C) => {
    n("columnVisible", { column: v, visible: C });
  }, [n]), y = e.useCallback((v) => {
    n("groupBy", { column: v });
  }, [n]), E = e.useCallback((v, C) => {
    d.current = v, C.dataTransfer.effectAllowed = "move", C.dataTransfer.setData("text/plain", v);
  }, []), _ = e.useCallback((v, C) => {
    if (!d.current || d.current === v) {
      g(null);
      return;
    }
    C.preventDefault(), C.dataTransfer.dropEffect = "move";
    const O = C.currentTarget.getBoundingClientRect(), $ = C.clientY < O.top + O.height / 2 ? "top" : "bottom";
    g({ name: v, side: $ });
  }, [g]), k = e.useCallback(() => {
    d.current = null, g(null);
  }, [g]), I = e.useCallback((v) => {
    v.preventDefault();
    const C = d.current, O = m.current;
    if (d.current = null, g(null), !C || !O)
      return;
    const $ = r.findIndex((M) => M.name === O.name), A = r.findIndex((M) => M.name === C);
    if ($ < 0 || A < 0)
      return;
    let D = O.side === "top" ? $ : $ + 1;
    A < D && D--, D !== A && n("columnReorder", { column: C, targetIndex: D });
  }, [r, n, g]), w = r.length > 10;
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__list" + (w ? " tlColumnSelect__list--fixed" : "") }, i.map((v) => {
    const C = v.visible && s <= 1;
    let O = "tlColumnSelect__row";
    return p && p.name === v.name && (O += " tlColumnSelect__row--dragOver-" + p.side), /* @__PURE__ */ e.createElement(
      "div",
      {
        key: v.name,
        className: O,
        draggable: !0,
        onDragStart: ($) => E(v.name, $),
        onDragOver: ($) => _(v.name, $),
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
          onChange: ($) => T(v.name, $.target.checked)
        }
      ), /* @__PURE__ */ e.createElement("span", null, v.label))
    );
  })));
}, { useState: zt, useRef: at, useCallback: _t, useMemo: Fe, useEffect: Cn } = e, Rr = {
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
}, Ce = 44, yt = 15, we = 6e4, Lr = 36e5, je = 864e5, xr = 8;
function Te(l) {
  const t = new Date(l);
  return t.setHours(0, 0, 0, 0), t.getTime();
}
function Ke(l, t) {
  const n = new Date(l);
  return n.setDate(n.getDate() + t), n.getTime();
}
function Mr(l) {
  return Te(l);
}
function rt(l, t) {
  return Te(l) === Te(t);
}
function Ie(l) {
  return (l - Te(l)) / we;
}
function et(l) {
  return Math.round(l / yt) * yt;
}
function tt(l, t, n) {
  return Math.max(t, Math.min(n, l));
}
function wt(l) {
  if (!l)
    return "tlCalEvent--default";
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return "tlCalEvent--c" + Math.abs(t) % xr;
}
function kt(l, t) {
  return l.color ? { ...t, "--cal-ev-bg": l.color } : t;
}
function Ir(l) {
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
function jr(l, t) {
  const n = { hour: "numeric", minute: "2-digit" };
  return Oe(l, n, t.start) + "–" + Oe(l, n, t.end);
}
const Ar = [
  { key: "DAY", label: "js.calendar.day" },
  { key: "WORK_WEEK", label: "js.calendar.workWeek" },
  { key: "WEEK", label: "js.calendar.week" },
  { key: "MONTH", label: "js.calendar.month" },
  { key: "YEAR", label: "js.calendar.year" }
], Pr = ({ title: l, granularity: t, i18n: n, send: a }) => /* @__PURE__ */ e.createElement("div", { className: "tlCalToolbar" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalNav" }, /* @__PURE__ */ e.createElement("button", { className: "tlCalBtn", onClick: () => a("navigate", { direction: "TODAY" }) }, n["js.calendar.today"]), /* @__PURE__ */ e.createElement(
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
)), /* @__PURE__ */ e.createElement("div", { className: "tlCalTitle" }, l), /* @__PURE__ */ e.createElement("div", { className: "tlCalGranularity" }, Ar.map((r) => /* @__PURE__ */ e.createElement(
  "button",
  {
    key: r.key,
    className: "tlCalBtn" + (r.key === t ? " tlCalBtn--active" : ""),
    onClick: () => a("switchGranularity", { granularity: r.key })
  },
  n[r.label]
))));
function Br(l) {
  const t = [...l].sort((o, u) => o.start - u.start || u.end - o.end), n = [];
  let a = [], r = -1;
  const s = () => {
    const o = a.reduce((u, c) => Math.max(u, c.col + 1), 0);
    for (const u of a)
      u.cols = o;
    n.push(...a), a = [], r = -1;
  };
  for (const o of t) {
    a.length > 0 && o.start >= r && s();
    const u = new Set(a.filter((i) => i.ev.end > o.start).map((i) => i.col));
    let c = 0;
    for (; u.has(c); )
      c++;
    a.push({
      ev: o,
      topMin: Ie(o.start),
      botMin: Ie(o.start) + Math.max(15, (o.end - o.start) / we),
      col: c,
      cols: 1
    }), r = Math.max(r, o.end);
  }
  return a.length > 0 && s(), n;
}
const jt = (l) => {
  l.preventDefault(), l.currentTarget.setPointerCapture(l.pointerId);
}, Kt = ({ className: l, placeholder: t, style: n, onCommit: a, onDiscard: r }) => {
  const s = at(!1), o = (u) => {
    s.current || (s.current = !0, u === null ? r() : a(u));
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
}, Pn = (l) => {
  const [t, n] = zt(null), a = at(null);
  a.current = t;
  const r = _t((u) => n(u), []), s = _t(() => n(null), []), o = _t(
    (u) => {
      const c = a.current;
      c && l("createSlot", { ...c, title: u }), n(null);
    },
    [l]
  );
  return { pending: t, open: r, commit: o, discard: s };
}, Fr = ({
  ctx: l,
  rangeStart: t,
  granularity: n
}) => {
  const { events: a, locale: r, nonWorkingDays: s, dayStartHour: o, dayEndHour: u, now: c, send: i, editable: d, i18n: m } = l, p = Fe(() => {
    const P = n === "DAY" ? 1 : 7, j = [];
    for (let B = 0; B < P; B++) {
      const L = Ke(t, B);
      n === "WORK_WEEK" && s.includes(new Date(L).getDay()) || j.push(L);
    }
    return j;
  }, [t, n, s]), h = Pn(i), g = at(null), T = at(null), [y, E] = zt(null), _ = at(null);
  _.current = y;
  const [k, I] = zt(Date.now());
  Cn(() => {
    const P = window.setInterval(() => I(Date.now()), 6e4);
    return () => window.clearInterval(P);
  }, []);
  const w = _t(
    (P, j) => {
      const B = g.current;
      if (!B)
        return { dayIndex: 0, min: 0 };
      const L = B.getBoundingClientRect(), R = L.width / p.length, V = tt(Math.floor((P - L.left) / R), 0, p.length - 1), f = j - L.top + B.scrollTop, x = tt(f / Ce * 60, 0, 1440);
      return { dayIndex: V, min: x };
    },
    [p.length]
  );
  Cn(() => {
    if (!y)
      return;
    const P = (L) => {
      const R = _.current;
      if (!R)
        return;
      const { dayIndex: V, min: f } = w(L.clientX, L.clientY);
      R.mode === "move" ? E({ ...R, dayStart: p[V], startMin: tt(et(f - R.grabMin), 0, 1440 - R.dur) }) : R.mode === "resize" ? E({ ...R, endMin: tt(et(f), R.startMin + yt, 1440) }) : E({ ...R, toMin: tt(et(f), 0, 1440) });
    }, j = () => {
      const L = _.current;
      if (E(null), !!L)
        if (L.mode === "move") {
          const R = L.dayStart + L.startMin * we;
          R !== L.origStartMs && i("moveEvent", { eventId: L.id, start: R, end: R + L.dur * we });
        } else if (L.mode === "resize") {
          const R = L.dayStart + L.endMin * we;
          R !== L.origEndMs && i("resizeEvent", { eventId: L.id, end: R });
        } else {
          const R = Math.min(L.fromMin, L.toMin), V = Math.max(L.fromMin, L.toMin);
          V - R >= yt && h.open({ start: L.dayStart + R * we, end: L.dayStart + V * we, allDay: !1 });
        }
    }, B = () => E(null);
    return window.addEventListener("pointermove", P), window.addEventListener("pointerup", j, { once: !0 }), window.addEventListener("pointercancel", B), () => {
      window.removeEventListener("pointermove", P), window.removeEventListener("pointerup", j), window.removeEventListener("pointercancel", B);
    };
  }, [y, p, w, i, h.open]);
  const v = (P, j, B) => {
    if (!d || !j.movable)
      return;
    P.stopPropagation(), jt(P), h.discard();
    const { min: L } = w(P.clientX, P.clientY), R = (j.end - j.start) / we;
    E({
      mode: "move",
      id: j.id,
      grabMin: L - Ie(j.start),
      dur: R,
      dayStart: B,
      startMin: Ie(j.start),
      origStartMs: j.start
    });
  }, C = (P, j, B) => {
    !d || !j.resizable || (P.stopPropagation(), jt(P), h.discard(), E({
      mode: "resize",
      id: j.id,
      dayStart: B,
      startMin: Ie(j.start),
      endMin: Ie(j.end),
      origEndMs: j.end
    }));
  }, O = (P, j) => {
    if (!d || P.button !== 0)
      return;
    jt(P), h.discard();
    const { min: B } = w(P.clientX, P.clientY);
    E({ mode: "create", dayStart: j, fromMin: et(B), toMin: et(B) });
  }, $ = Array.from({ length: 24 }, (P, j) => j), A = Fe(() => {
    if (y === null || !("id" in y))
      return a;
    const P = y;
    return a.map((j) => {
      if (j.id !== P.id)
        return j;
      if (P.mode === "move") {
        const B = P.dayStart + P.startMin * we;
        return { ...j, start: B, end: B + P.dur * we };
      }
      return { ...j, end: P.dayStart + P.endMin * we };
    });
  }, [a, y]), D = Fe(() => p.map(
    (P) => Br(
      A.filter((j) => !j.allDay && j.start < P + je && j.end > P)
    )
  ), [p, A]), M = Fe(() => p.map((P) => A.filter((j) => j.allDay && j.start < P + je && j.end > P)), [p, A]), S = o * Ce, W = u * Ce;
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeGrid" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeHeader" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalGutter" }), p.map((P) => {
    const j = s.includes(new Date(P).getDay()), B = rt(P, l.now);
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: P,
        className: "tlCalDayHead" + (j ? " tlCalDayHead--nonworking" : "") + (B ? " tlCalDayHead--today" : ""),
        onClick: () => i("goto", { date: P, granularity: "DAY" })
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlCalDayName" }, Oe(r, { weekday: "short" }, P)),
      /* @__PURE__ */ e.createElement("span", { className: "tlCalDayNum" }, new Date(P).getDate())
    );
  })), /* @__PURE__ */ e.createElement("div", { className: "tlCalAllDayRow" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalGutter tlCalAllDayLabel" }, m["js.calendar.allDay"]), p.map((P, j) => /* @__PURE__ */ e.createElement(
    "div",
    {
      key: P,
      className: "tlCalAllDayCell",
      onClick: () => d && h.open({ start: P, end: P + je, allDay: !0 })
    },
    h.pending && h.pending.allDay && h.pending.start === P && /* @__PURE__ */ e.createElement(
      Kt,
      {
        className: "tlCalAllDayEvent tlCalEvent--preview",
        placeholder: m["js.calendar.newEventTitle"],
        onCommit: h.commit,
        onDiscard: h.discard
      }
    ),
    M[j].map((B) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: B.id,
        className: "tlCalAllDayEvent " + wt(B.category) + (B.selected ? " tlCalEvent--selected" : ""),
        style: kt(B),
        title: B.tooltip,
        onClick: (L) => {
          L.stopPropagation(), i("selectEvent", { eventId: B.id });
        }
      },
      B.title
    ))
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlCalScroll", ref: T }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeBody", style: { height: 24 * Ce } }, /* @__PURE__ */ e.createElement("div", { className: "tlCalHourAxis" }, $.map((P) => /* @__PURE__ */ e.createElement("div", { key: P, className: "tlCalHourLabel", style: { top: P * Ce } }, P === 0 ? "" : Oe(r, { hour: "numeric" }, Te(t) + P * Lr)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalColumns", ref: g, style: { gridTemplateColumns: `repeat(${p.length}, 1fr)` } }, p.map((P, j) => {
    const B = s.includes(new Date(P).getDay()), L = y && ("dayStart" in y && y.dayStart === P) ? y : null;
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: P,
        className: "tlCalCol" + (B ? " tlCalCol--nonworking" : ""),
        onPointerDown: (R) => O(R, P)
      },
      $.map((R) => /* @__PURE__ */ e.createElement("div", { key: R, className: "tlCalHourLine", style: { top: R * Ce } })),
      /* @__PURE__ */ e.createElement("div", { className: "tlCalWorkBand", style: { top: S, height: W - S } }),
      rt(P, k) && /* @__PURE__ */ e.createElement("div", { className: "tlCalNowLine", style: { top: Ie(Date.now()) / 60 * Ce } }),
      D[j].map((R) => {
        const V = y !== null && "id" in y && y.id === R.ev.id, f = R.topMin / 60 * Ce, x = (R.botMin - R.topMin) / 60 * Ce, Y = 100 / R.cols;
        return /* @__PURE__ */ e.createElement(
          "div",
          {
            key: R.ev.id,
            className: "tlCalEvent " + wt(R.ev.category) + (R.ev.selected ? " tlCalEvent--selected" : "") + (V ? " tlCalEvent--dragging" : ""),
            style: kt(R.ev, {
              top: f,
              height: x,
              left: `${R.col * Y}%`,
              width: `calc(${Y}% - 2px)`
            }),
            title: R.ev.tooltip,
            onPointerDown: (z) => v(z, R.ev, P),
            onClick: (z) => {
              z.stopPropagation(), i("selectEvent", { eventId: R.ev.id });
            }
          },
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTime" }, jr(r, R.ev)),
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTitle" }, R.ev.title),
          d && R.ev.resizable && /* @__PURE__ */ e.createElement("span", { className: "tlCalResizeHandle", onPointerDown: (z) => C(z, R.ev, P) })
        );
      }),
      h.pending && !h.pending.allDay && Te(h.pending.start) === P && /* @__PURE__ */ e.createElement(
        Kt,
        {
          className: "tlCalEvent tlCalEvent--preview",
          placeholder: m["js.calendar.newEventTitle"],
          style: {
            top: Ie(h.pending.start) / 60 * Ce,
            height: (h.pending.end - h.pending.start) / we / 60 * Ce
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
            top: Math.min(L.fromMin, L.toMin) / 60 * Ce,
            height: Math.abs(L.toMin - L.fromMin) / 60 * Ce
          }
        }
      )
    );
  })))));
}, Or = 3, $r = ({ ctx: l, rangeStart: t, anchorMonth: n }) => {
  const { events: a, locale: r, nonWorkingDays: s, send: o, editable: u, now: c, i18n: i } = l, d = Pn(o), m = Fe(() => {
    const h = [];
    for (let g = 0; g < 6; g++) {
      const T = [];
      for (let y = 0; y < 7; y++)
        T.push(Ke(t, g * 7 + y));
      h.push(T);
    }
    return h;
  }, [t]), p = (h, g) => {
    h.preventDefault();
    const T = h.dataTransfer.getData("text/plain"), y = a.find((_) => _.id === T);
    if (!y || !u || !y.movable)
      return;
    const E = g - Te(y.start);
    o("moveEvent", { eventId: T, start: y.start + E, end: y.end + E });
  };
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalMonth" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthHead" }, m[0].map((h) => /* @__PURE__ */ e.createElement("div", { key: h, className: "tlCalMonthWeekday" }, Oe(r, { weekday: "short" }, h)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthBody" }, m.map((h, g) => {
    const T = h[0], y = Ke(T, 7), E = a.filter((k) => (k.allDay || k.end - k.start >= je) && k.start < y && k.end > T).sort((k, I) => k.start - I.start).slice(0, 3), _ = E.length;
    return /* @__PURE__ */ e.createElement("div", { key: g, className: "tlCalMonthWeek" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthDays" }, h.map((k) => {
      const I = new Date(k).getMonth() === new Date(n).getMonth(), w = s.includes(new Date(k).getDay()), v = rt(k, c);
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
          Kt,
          {
            className: "tlCalMonthBar tlCalEvent--preview tlCalMonthCreate",
            placeholder: i["js.calendar.newEventTitle"],
            onCommit: d.commit,
            onDiscard: d.discard
          }
        )
      );
    })), /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthOverlay" }, E.map((k, I) => {
      const w = Math.max(0, Math.floor((Te(Math.max(k.start, T)) - T) / je)), v = Math.min(7, Math.ceil((k.end - T) / je));
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: k.id,
          className: "tlCalMonthBar " + wt(k.category) + (k.selected ? " tlCalEvent--selected" : ""),
          style: kt(k, {
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
      const w = a.filter((O) => !O.allDay && O.end - O.start < je && rt(O.start, k)).sort((O, $) => O.start - $.start), v = w.slice(0, Or), C = w.length - v.length;
      return v.map((O, $) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: O.id,
          className: "tlCalChip " + wt(O.category) + (O.selected ? " tlCalEvent--selected" : ""),
          style: kt(O, { gridColumn: I + 1, gridRow: _ + 1 + $ }),
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
}, Hr = ({ ctx: l, rangeStart: t }) => {
  const { events: n, locale: a, firstDayOfWeek: r, nonWorkingDays: s, send: o, now: u } = l, c = Fe(() => {
    const p = /* @__PURE__ */ new Set();
    for (const h of n) {
      let g = Te(h.start);
      const T = h.end;
      for (; g < T; )
        p.add(g), g = Ke(g, 1);
    }
    return p;
  }, [n]), i = new Date(t).getFullYear(), d = Array.from({ length: 12 }, (p, h) => new Date(i, h, 1).getTime()), m = Fe(() => {
    const p = new Date(2023, 0, 1);
    return Array.from({ length: 7 }, (h, g) => {
      const T = new Date(p);
      return T.setDate(p.getDate() + (r + g) % 7), new Intl.DateTimeFormat(a, { weekday: "narrow" }).format(T);
    });
  }, [a, r]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalYear" }, d.map((p) => {
    const h = new Date(p), g = Te(Ke(p, -((h.getDay() - r + 7) % 7))), T = Array.from({ length: 42 }, (y, E) => Ke(g, E));
    return /* @__PURE__ */ e.createElement("div", { key: p, className: "tlCalMini" }, /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlCalMiniTitle",
        onClick: () => o("goto", { date: p, granularity: "MONTH" })
      },
      Oe(a, { month: "long" }, p)
    ), /* @__PURE__ */ e.createElement("div", { className: "tlCalMiniGrid" }, m.map((y, E) => /* @__PURE__ */ e.createElement("div", { key: "h" + E, className: "tlCalMiniWd" }, y)), T.map((y) => {
      const E = new Date(y).getMonth() === h.getMonth(), _ = s.includes(new Date(y).getDay()), k = rt(y, u), I = c.has(Mr(y));
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
}, Wr = ({ controlId: l }) => {
  const t = X(), n = re(), a = de(Rr), r = t.granularity ?? "WEEK", s = t.rangeStart ?? Date.now(), o = t.anchor ?? s, u = t.title ?? "", c = {
    locale: t.locale ?? "en",
    firstDayOfWeek: t.firstDayOfWeek ?? 0,
    nonWorkingDays: t.nonWorkingDays ?? [0, 6],
    dayStartHour: t.dayStartHour ?? 8,
    dayEndHour: t.dayEndHour ?? 18,
    editable: t.editable !== !1,
    now: t.now ?? Date.now(),
    events: Ir(t.events),
    send: n,
    i18n: a
  };
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCalendar" }, /* @__PURE__ */ e.createElement(Pr, { title: u, granularity: r, i18n: a, send: n }), /* @__PURE__ */ e.createElement("div", { className: "tlCalBody" }, r === "MONTH" ? /* @__PURE__ */ e.createElement($r, { ctx: c, rangeStart: s, anchorMonth: o }) : r === "YEAR" ? /* @__PURE__ */ e.createElement(Hr, { ctx: c, rangeStart: s }) : /* @__PURE__ */ e.createElement(Fr, { ctx: c, rangeStart: s, granularity: r })));
}, Ur = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, Bn = e.createContext(Ur), { useMemo: Vr, useRef: zr, useState: Kr, useEffect: Yr } = e, Gr = 320, Xr = "TLTableView", qr = "TLPanel", Zr = ({ controlId: l }) => {
  var y;
  const t = X(), n = t.maxColumns ?? 3, a = t.labelPosition ?? "auto", r = t.readOnly === !0, s = t.children ?? [], o = t.noModelMessage, u = zr(null), [c, i] = Kr(
    a === "top" ? "top" : "side"
  );
  Yr(() => {
    if (a !== "auto") {
      i(a);
      return;
    }
    const E = u.current;
    if (!E) return;
    const _ = new ResizeObserver((k) => {
      for (const I of k) {
        const v = I.contentRect.width / n;
        i(v < Gr ? "top" : "side");
      }
    });
    return _.observe(E), () => _.disconnect();
  }, [a, n]);
  const d = Vr(() => ({
    readOnly: r,
    resolvedLabelPosition: c
  }), [r, c]), p = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, h = s.length === 1 ? s[0] : void 0, g = !!h && (h.module === Xr || h.module === qr && ((y = h.state) == null ? void 0 : y.bare) === !0), T = [
    "tlFormLayout",
    r ? "tlFormLayout--readonly" : "",
    g ? "tlFormLayout--flush" : ""
  ].filter(Boolean).join(" ");
  return o ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, o)) : /* @__PURE__ */ e.createElement(Bn.Provider, { value: d }, /* @__PURE__ */ e.createElement("div", { id: l, className: T, style: p, ref: u }, s.map((E, _) => /* @__PURE__ */ e.createElement(G, { key: _, control: E }))));
}, { useCallback: Qr } = e, Jr = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, eo = ({ controlId: l }) => {
  const t = X(), n = re(), a = de(Jr), r = t.headerControl ?? null, s = t.headerActions ?? [], o = t.collapsible === !0, u = t.collapsed === !0, c = t.border ?? "none", i = t.fullLine === !0, d = t.children ?? [], m = r != null || s.length > 0 || o, p = Qr(() => {
    n("toggleCollapse");
  }, [n]), h = [
    "tlFormGroup",
    `tlFormGroup--border-${c}`,
    i ? "tlFormGroup--fullLine" : "",
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
  ), r && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(G, { control: r })), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, s.map((g, T) => /* @__PURE__ */ e.createElement(G, { key: T, control: g })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, d.map((g, T) => /* @__PURE__ */ e.createElement(G, { key: T, control: g }))));
}, { useContext: to, useState: no, useCallback: lo } = e, ao = ({ controlId: l }) => {
  const t = X(), n = to(Bn), a = t.label ?? "", r = t.required === !0, s = t.error, o = t.errorIcon, u = t.warnings, c = t.warningIcon, i = t.helpText, d = t.dirty === !0, m = t.labelPosition ?? n.resolvedLabelPosition, p = t.fullLine === !0, h = t.visible !== !1, g = t.hasTooltip === !0, T = t.field, y = n.readOnly, [E, _] = no(!1), k = lo(() => _((O) => !O), []), I = m === "hidden", w = s != null, v = u != null && u.length > 0, C = [
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
      "data-tooltip": g ? "key:tooltip" : void 0
    },
    a
  ), r && !y && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), d && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), i && !y && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(G, { control: T })), !y && w && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(Ht, { image: o, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, s)), !y && !w && v && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, u.map((O, $) => /* @__PURE__ */ e.createElement("div", { key: $, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(Ht, { image: c, className: "tlFormField__warningIcon" }), /* @__PURE__ */ e.createElement("span", null, O)))), !y && i && E && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, i));
}, ro = ({ controlId: l }) => {
  const t = X(), n = re(), a = t.iconCss, r = t.iconSrc, s = t.label, o = t.cssClass, u = t.hasTooltip === !0, c = t.hasLink, i = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : r ? /* @__PURE__ */ e.createElement("img", { src: r, className: "tlTypeIcon", alt: "" }) : null, d = /* @__PURE__ */ e.createElement(e.Fragment, null, i, s && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, s)), m = e.useCallback((g) => {
    g.preventDefault(), n("goto", {});
  }, [n]), p = ["tlResourceCell", o].filter(Boolean).join(" "), h = u ? "key:tooltip" : void 0;
  return c ? /* @__PURE__ */ e.createElement(
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
}, oo = 20, yn = "expand", wn = "collapse", ht = "select", so = "activate", co = "contextMenu", io = "dragOver", uo = "drop", mo = "dragEnd", po = "single", fo = "multi", ho = () => {
  var A;
  const l = X(), t = re(), n = l.nodes ?? [], a = l.selectionMode ?? po, r = l.dragEnabled ?? !1, s = l.dropEnabled ?? !1, o = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, c = a === fo, [i, d] = e.useState(null), m = e.useRef(null), p = e.useMemo(() => {
    const D = i == null ? -1 : n.findIndex((M) => M.id === i);
    return D >= 0 ? D : n.findIndex((M) => M.selected);
  }, [n, i]), h = ((A = n.find((D) => D.selected)) == null ? void 0 : A.id) ?? null;
  e.useEffect(() => {
    var M;
    if (h == null)
      return;
    const D = (M = m.current) == null ? void 0 : M.querySelector(".tlTreeView__node--selected");
    D && D.scrollIntoView({ block: "nearest" });
  }, [h]), e.useEffect(() => {
    var D, M;
    i != null && ((M = (D = m.current) == null ? void 0 : D.querySelector(".tlTreeView__node--focused")) == null || M.scrollIntoView({ block: "nearest" }));
  }, [i]);
  const g = e.useCallback((D, M) => {
    t(M ? wn : yn, { nodeId: D });
  }, [t]), T = e.useCallback((D, M) => {
    var W;
    const S = window.getSelection();
    S && !S.isCollapsed && M.currentTarget.contains(S.anchorNode) || ((W = m.current) == null || W.focus({ preventScroll: !0 }), d(D), t(ht, {
      nodeId: D,
      ctrlKey: M.ctrlKey || M.metaKey,
      shiftKey: M.shiftKey
    }));
  }, [t]), y = e.useCallback((D) => {
    d(D), t(so, { nodeId: D });
  }, [t]), E = e.useCallback((D, M) => {
    M.preventDefault(), t(co, { nodeId: D, x: M.clientX, y: M.clientY });
  }, [t]), _ = e.useRef(null), k = e.useCallback((D, M) => {
    const S = M.getBoundingClientRect(), W = D.clientY - S.top, P = S.height / 3;
    return W < P ? "above" : W > P * 2 ? "below" : "within";
  }, []), I = e.useCallback((D, M) => {
    M.dataTransfer.effectAllowed = "move", M.dataTransfer.setData("text/plain", D);
  }, []), w = e.useCallback((D, M) => {
    M.preventDefault(), M.dataTransfer.dropEffect = "move";
    const S = k(M, M.currentTarget);
    _.current != null && window.clearTimeout(_.current), _.current = window.setTimeout(() => {
      t(io, { nodeId: D, position: S }), _.current = null;
    }, 50);
  }, [t, k]), v = e.useCallback((D, M) => {
    M.preventDefault(), _.current != null && (window.clearTimeout(_.current), _.current = null);
    const S = k(M, M.currentTarget);
    t(uo, { nodeId: D, position: S });
  }, [t, k]), C = e.useCallback(() => {
    _.current != null && (window.clearTimeout(_.current), _.current = null), t(mo);
  }, [t]), O = e.useCallback((D, M) => {
    const S = n[D];
    S != null && (d(S.id), c ? M && t(ht, { nodeId: S.id, ctrlKey: !1, shiftKey: !0 }) : t(ht, { nodeId: S.id, ctrlKey: !1, shiftKey: !1 }));
  }, [n, c, t]), $ = e.useCallback((D) => {
    if (n.length === 0)
      return;
    const M = p >= 0 ? n[p] : null;
    let S = p;
    switch (D.key) {
      case "ArrowDown":
        D.preventDefault(), S = Math.min(p + 1, n.length - 1);
        break;
      case "ArrowUp":
        D.preventDefault(), S = Math.max(p - 1, 0);
        break;
      case "ArrowRight":
        if (D.preventDefault(), M == null)
          break;
        if (M.expandable && !M.expanded) {
          t(yn, { nodeId: M.id });
          return;
        }
        M.expanded && (S = p + 1);
        break;
      case "ArrowLeft":
        if (D.preventDefault(), M == null)
          break;
        if (M.expanded) {
          t(wn, { nodeId: M.id });
          return;
        }
        for (let W = p - 1; W >= 0; W--)
          if (n[W].depth < M.depth) {
            S = W;
            break;
          }
        break;
      case "Home":
        D.preventDefault(), S = 0;
        break;
      case "End":
        D.preventDefault(), S = n.length - 1;
        break;
      case "Enter":
        D.preventDefault(), M != null && y(M.id);
        return;
      case " ":
        D.preventDefault(), M != null && t(ht, { nodeId: M.id, ctrlKey: c, shiftKey: !1 });
        return;
      default:
        return;
    }
    S !== p && O(S, D.shiftKey);
  }, [p, n, t, c, y, O]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: m,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: $
    },
    n.map((D, M) => /* @__PURE__ */ e.createElement(
      "li",
      {
        key: D.id,
        role: "treeitem",
        "aria-expanded": D.expandable ? D.expanded : void 0,
        "aria-selected": D.selected,
        "aria-level": D.depth + 1,
        className: [
          "tlTreeView__node",
          D.selected ? "tlTreeView__node--selected" : "",
          M === p ? "tlTreeView__node--focused" : "",
          o === D.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          o === D.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          o === D.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: D.depth * oo },
        draggable: r,
        onMouseDown: (S) => {
          (S.shiftKey || S.ctrlKey || S.metaKey || S.detail > 1) && S.preventDefault();
        },
        onClick: (S) => T(D.id, S),
        onDoubleClick: () => y(D.id),
        onContextMenu: (S) => E(D.id, S),
        onDragStart: (S) => I(D.id, S),
        onDragOver: s ? (S) => w(D.id, S) : void 0,
        onDrop: s ? (S) => v(D.id, S) : void 0,
        onDragEnd: C
      },
      D.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (S) => {
            S.stopPropagation(), g(D.id, D.expanded);
          },
          tabIndex: -1,
          "aria-label": D.expanded ? "Collapse" : "Expand"
        },
        D.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: D.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(G, { control: D.content }))
    ))
  );
};
var At = { exports: {} }, he = {}, Pt = { exports: {} }, te = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var kn;
function bo() {
  if (kn) return te;
  kn = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), a = Symbol.for("react.strict_mode"), r = Symbol.for("react.profiler"), s = Symbol.for("react.consumer"), o = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), c = Symbol.for("react.suspense"), i = Symbol.for("react.memo"), d = Symbol.for("react.lazy"), m = Symbol.for("react.activity"), p = Symbol.iterator;
  function h(f) {
    return f === null || typeof f != "object" ? null : (f = p && f[p] || f["@@iterator"], typeof f == "function" ? f : null);
  }
  var g = {
    isMounted: function() {
      return !1;
    },
    enqueueForceUpdate: function() {
    },
    enqueueReplaceState: function() {
    },
    enqueueSetState: function() {
    }
  }, T = Object.assign, y = {};
  function E(f, x, Y) {
    this.props = f, this.context = x, this.refs = y, this.updater = Y || g;
  }
  E.prototype.isReactComponent = {}, E.prototype.setState = function(f, x) {
    if (typeof f != "object" && typeof f != "function" && f != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, f, x, "setState");
  }, E.prototype.forceUpdate = function(f) {
    this.updater.enqueueForceUpdate(this, f, "forceUpdate");
  };
  function _() {
  }
  _.prototype = E.prototype;
  function k(f, x, Y) {
    this.props = f, this.context = x, this.refs = y, this.updater = Y || g;
  }
  var I = k.prototype = new _();
  I.constructor = k, T(I, E.prototype), I.isPureReactComponent = !0;
  var w = Array.isArray;
  function v() {
  }
  var C = { H: null, A: null, T: null, S: null }, O = Object.prototype.hasOwnProperty;
  function $(f, x, Y) {
    var z = Y.ref;
    return {
      $$typeof: l,
      type: f,
      key: x,
      ref: z !== void 0 ? z : null,
      props: Y
    };
  }
  function A(f, x) {
    return $(f.type, x, f.props);
  }
  function D(f) {
    return typeof f == "object" && f !== null && f.$$typeof === l;
  }
  function M(f) {
    var x = { "=": "=0", ":": "=2" };
    return "$" + f.replace(/[=:]/g, function(Y) {
      return x[Y];
    });
  }
  var S = /\/+/g;
  function W(f, x) {
    return typeof f == "object" && f !== null && f.key != null ? M("" + f.key) : x.toString(36);
  }
  function P(f) {
    switch (f.status) {
      case "fulfilled":
        return f.value;
      case "rejected":
        throw f.reason;
      default:
        switch (typeof f.status == "string" ? f.then(v, v) : (f.status = "pending", f.then(
          function(x) {
            f.status === "pending" && (f.status = "fulfilled", f.value = x);
          },
          function(x) {
            f.status === "pending" && (f.status = "rejected", f.reason = x);
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
  function j(f, x, Y, z, Q) {
    var U = typeof f;
    (U === "undefined" || U === "boolean") && (f = null);
    var ee = !1;
    if (f === null) ee = !0;
    else
      switch (U) {
        case "bigint":
        case "string":
        case "number":
          ee = !0;
          break;
        case "object":
          switch (f.$$typeof) {
            case l:
            case t:
              ee = !0;
              break;
            case d:
              return ee = f._init, j(
                ee(f._payload),
                x,
                Y,
                z,
                Q
              );
          }
      }
    if (ee)
      return Q = Q(f), ee = z === "" ? "." + W(f, 0) : z, w(Q) ? (Y = "", ee != null && (Y = ee.replace(S, "$&/") + "/"), j(Q, x, Y, "", function(be) {
        return be;
      })) : Q != null && (D(Q) && (Q = A(
        Q,
        Y + (Q.key == null || f && f.key === Q.key ? "" : ("" + Q.key).replace(
          S,
          "$&/"
        ) + "/") + ee
      )), x.push(Q)), 1;
    ee = 0;
    var ce = z === "" ? "." : z + ":";
    if (w(f))
      for (var ne = 0; ne < f.length; ne++)
        z = f[ne], U = ce + W(z, ne), ee += j(
          z,
          x,
          Y,
          U,
          Q
        );
    else if (ne = h(f), typeof ne == "function")
      for (f = ne.call(f), ne = 0; !(z = f.next()).done; )
        z = z.value, U = ce + W(z, ne++), ee += j(
          z,
          x,
          Y,
          U,
          Q
        );
    else if (U === "object") {
      if (typeof f.then == "function")
        return j(
          P(f),
          x,
          Y,
          z,
          Q
        );
      throw x = String(f), Error(
        "Objects are not valid as a React child (found: " + (x === "[object Object]" ? "object with keys {" + Object.keys(f).join(", ") + "}" : x) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return ee;
  }
  function B(f, x, Y) {
    if (f == null) return f;
    var z = [], Q = 0;
    return j(f, z, "", "", function(U) {
      return x.call(Y, U, Q++);
    }), z;
  }
  function L(f) {
    if (f._status === -1) {
      var x = f._result;
      x = x(), x.then(
        function(Y) {
          (f._status === 0 || f._status === -1) && (f._status = 1, f._result = Y);
        },
        function(Y) {
          (f._status === 0 || f._status === -1) && (f._status = 2, f._result = Y);
        }
      ), f._status === -1 && (f._status = 0, f._result = x);
    }
    if (f._status === 1) return f._result.default;
    throw f._result;
  }
  var R = typeof reportError == "function" ? reportError : function(f) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var x = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof f == "object" && f !== null && typeof f.message == "string" ? String(f.message) : String(f),
        error: f
      });
      if (!window.dispatchEvent(x)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", f);
      return;
    }
    console.error(f);
  }, V = {
    map: B,
    forEach: function(f, x, Y) {
      B(
        f,
        function() {
          x.apply(this, arguments);
        },
        Y
      );
    },
    count: function(f) {
      var x = 0;
      return B(f, function() {
        x++;
      }), x;
    },
    toArray: function(f) {
      return B(f, function(x) {
        return x;
      }) || [];
    },
    only: function(f) {
      if (!D(f))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return f;
    }
  };
  return te.Activity = m, te.Children = V, te.Component = E, te.Fragment = n, te.Profiler = r, te.PureComponent = k, te.StrictMode = a, te.Suspense = c, te.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = C, te.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(f) {
      return C.H.useMemoCache(f);
    }
  }, te.cache = function(f) {
    return function() {
      return f.apply(null, arguments);
    };
  }, te.cacheSignal = function() {
    return null;
  }, te.cloneElement = function(f, x, Y) {
    if (f == null)
      throw Error(
        "The argument must be a React element, but you passed " + f + "."
      );
    var z = T({}, f.props), Q = f.key;
    if (x != null)
      for (U in x.key !== void 0 && (Q = "" + x.key), x)
        !O.call(x, U) || U === "key" || U === "__self" || U === "__source" || U === "ref" && x.ref === void 0 || (z[U] = x[U]);
    var U = arguments.length - 2;
    if (U === 1) z.children = Y;
    else if (1 < U) {
      for (var ee = Array(U), ce = 0; ce < U; ce++)
        ee[ce] = arguments[ce + 2];
      z.children = ee;
    }
    return $(f.type, Q, z);
  }, te.createContext = function(f) {
    return f = {
      $$typeof: o,
      _currentValue: f,
      _currentValue2: f,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, f.Provider = f, f.Consumer = {
      $$typeof: s,
      _context: f
    }, f;
  }, te.createElement = function(f, x, Y) {
    var z, Q = {}, U = null;
    if (x != null)
      for (z in x.key !== void 0 && (U = "" + x.key), x)
        O.call(x, z) && z !== "key" && z !== "__self" && z !== "__source" && (Q[z] = x[z]);
    var ee = arguments.length - 2;
    if (ee === 1) Q.children = Y;
    else if (1 < ee) {
      for (var ce = Array(ee), ne = 0; ne < ee; ne++)
        ce[ne] = arguments[ne + 2];
      Q.children = ce;
    }
    if (f && f.defaultProps)
      for (z in ee = f.defaultProps, ee)
        Q[z] === void 0 && (Q[z] = ee[z]);
    return $(f, U, Q);
  }, te.createRef = function() {
    return { current: null };
  }, te.forwardRef = function(f) {
    return { $$typeof: u, render: f };
  }, te.isValidElement = D, te.lazy = function(f) {
    return {
      $$typeof: d,
      _payload: { _status: -1, _result: f },
      _init: L
    };
  }, te.memo = function(f, x) {
    return {
      $$typeof: i,
      type: f,
      compare: x === void 0 ? null : x
    };
  }, te.startTransition = function(f) {
    var x = C.T, Y = {};
    C.T = Y;
    try {
      var z = f(), Q = C.S;
      Q !== null && Q(Y, z), typeof z == "object" && z !== null && typeof z.then == "function" && z.then(v, R);
    } catch (U) {
      R(U);
    } finally {
      x !== null && Y.types !== null && (x.types = Y.types), C.T = x;
    }
  }, te.unstable_useCacheRefresh = function() {
    return C.H.useCacheRefresh();
  }, te.use = function(f) {
    return C.H.use(f);
  }, te.useActionState = function(f, x, Y) {
    return C.H.useActionState(f, x, Y);
  }, te.useCallback = function(f, x) {
    return C.H.useCallback(f, x);
  }, te.useContext = function(f) {
    return C.H.useContext(f);
  }, te.useDebugValue = function() {
  }, te.useDeferredValue = function(f, x) {
    return C.H.useDeferredValue(f, x);
  }, te.useEffect = function(f, x) {
    return C.H.useEffect(f, x);
  }, te.useEffectEvent = function(f) {
    return C.H.useEffectEvent(f);
  }, te.useId = function() {
    return C.H.useId();
  }, te.useImperativeHandle = function(f, x, Y) {
    return C.H.useImperativeHandle(f, x, Y);
  }, te.useInsertionEffect = function(f, x) {
    return C.H.useInsertionEffect(f, x);
  }, te.useLayoutEffect = function(f, x) {
    return C.H.useLayoutEffect(f, x);
  }, te.useMemo = function(f, x) {
    return C.H.useMemo(f, x);
  }, te.useOptimistic = function(f, x) {
    return C.H.useOptimistic(f, x);
  }, te.useReducer = function(f, x, Y) {
    return C.H.useReducer(f, x, Y);
  }, te.useRef = function(f) {
    return C.H.useRef(f);
  }, te.useState = function(f) {
    return C.H.useState(f);
  }, te.useSyncExternalStore = function(f, x, Y) {
    return C.H.useSyncExternalStore(
      f,
      x,
      Y
    );
  }, te.useTransition = function() {
    return C.H.useTransition();
  }, te.version = "19.2.4", te;
}
var Nn;
function go() {
  return Nn || (Nn = 1, Pt.exports = bo()), Pt.exports;
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
var Sn;
function Eo() {
  if (Sn) return he;
  Sn = 1;
  var l = go();
  function t(c) {
    var i = "https://react.dev/errors/" + c;
    if (1 < arguments.length) {
      i += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var d = 2; d < arguments.length; d++)
        i += "&args[]=" + encodeURIComponent(arguments[d]);
    }
    return "Minified React error #" + c + "; visit " + i + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
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
  function s(c, i, d) {
    var m = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: r,
      key: m == null ? null : "" + m,
      children: c,
      containerInfo: i,
      implementation: d
    };
  }
  var o = l.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function u(c, i) {
    if (c === "font") return "";
    if (typeof i == "string")
      return i === "use-credentials" ? i : "";
  }
  return he.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = a, he.createPortal = function(c, i) {
    var d = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!i || i.nodeType !== 1 && i.nodeType !== 9 && i.nodeType !== 11)
      throw Error(t(299));
    return s(c, i, null, d);
  }, he.flushSync = function(c) {
    var i = o.T, d = a.p;
    try {
      if (o.T = null, a.p = 2, c) return c();
    } finally {
      o.T = i, a.p = d, a.d.f();
    }
  }, he.preconnect = function(c, i) {
    typeof c == "string" && (i ? (i = i.crossOrigin, i = typeof i == "string" ? i === "use-credentials" ? i : "" : void 0) : i = null, a.d.C(c, i));
  }, he.prefetchDNS = function(c) {
    typeof c == "string" && a.d.D(c);
  }, he.preinit = function(c, i) {
    if (typeof c == "string" && i && typeof i.as == "string") {
      var d = i.as, m = u(d, i.crossOrigin), p = typeof i.integrity == "string" ? i.integrity : void 0, h = typeof i.fetchPriority == "string" ? i.fetchPriority : void 0;
      d === "style" ? a.d.S(
        c,
        typeof i.precedence == "string" ? i.precedence : void 0,
        {
          crossOrigin: m,
          integrity: p,
          fetchPriority: h
        }
      ) : d === "script" && a.d.X(c, {
        crossOrigin: m,
        integrity: p,
        fetchPriority: h,
        nonce: typeof i.nonce == "string" ? i.nonce : void 0
      });
    }
  }, he.preinitModule = function(c, i) {
    if (typeof c == "string")
      if (typeof i == "object" && i !== null) {
        if (i.as == null || i.as === "script") {
          var d = u(
            i.as,
            i.crossOrigin
          );
          a.d.M(c, {
            crossOrigin: d,
            integrity: typeof i.integrity == "string" ? i.integrity : void 0,
            nonce: typeof i.nonce == "string" ? i.nonce : void 0
          });
        }
      } else i == null && a.d.M(c);
  }, he.preload = function(c, i) {
    if (typeof c == "string" && typeof i == "object" && i !== null && typeof i.as == "string") {
      var d = i.as, m = u(d, i.crossOrigin);
      a.d.L(c, d, {
        crossOrigin: m,
        integrity: typeof i.integrity == "string" ? i.integrity : void 0,
        nonce: typeof i.nonce == "string" ? i.nonce : void 0,
        type: typeof i.type == "string" ? i.type : void 0,
        fetchPriority: typeof i.fetchPriority == "string" ? i.fetchPriority : void 0,
        referrerPolicy: typeof i.referrerPolicy == "string" ? i.referrerPolicy : void 0,
        imageSrcSet: typeof i.imageSrcSet == "string" ? i.imageSrcSet : void 0,
        imageSizes: typeof i.imageSizes == "string" ? i.imageSizes : void 0,
        media: typeof i.media == "string" ? i.media : void 0
      });
    }
  }, he.preloadModule = function(c, i) {
    if (typeof c == "string")
      if (i) {
        var d = u(i.as, i.crossOrigin);
        a.d.m(c, {
          as: typeof i.as == "string" && i.as !== "script" ? i.as : void 0,
          crossOrigin: d,
          integrity: typeof i.integrity == "string" ? i.integrity : void 0
        });
      } else a.d.m(c);
  }, he.requestFormReset = function(c) {
    a.d.r(c);
  }, he.unstable_batchedUpdates = function(c, i) {
    return c(i);
  }, he.useFormState = function(c, i, d) {
    return o.H.useFormState(c, i, d);
  }, he.useFormStatus = function() {
    return o.H.useHostTransitionStatus();
  }, he.version = "19.2.4", he;
}
var Dn;
function vo() {
  if (Dn) return At.exports;
  Dn = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), At.exports = Eo(), At.exports;
}
var Fn = vo();
const { useState: Me, useCallback: Ee, useRef: nt, useEffect: Ue, useMemo: Yt } = e;
function Zt({ image: l }) {
  return l ? l.startsWith("/") ? /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" }) : /* @__PURE__ */ e.createElement(De, { encoded: l, className: "tlDropdownSelect__optionIcon" }) : null;
}
function _o({
  option: l,
  removable: t,
  onRemove: n,
  removeLabel: a,
  draggable: r,
  onDragStart: s,
  onDragOver: o,
  onDrop: u,
  onDragEnd: c,
  dragClassName: i
}) {
  const d = Ee(
    (m) => {
      m.stopPropagation(), n(l.value);
    },
    [n, l.value]
  );
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlDropdownSelect__chip" + (i ? " " + i : ""),
      draggable: r || void 0,
      onDragStart: s,
      onDragOver: o,
      onDrop: u,
      onDragEnd: c
    },
    r && /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__dragHandle", "aria-hidden": "true" }, "⋮⋮"),
    /* @__PURE__ */ e.createElement(Zt, { image: l.image }),
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
function Co({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: a,
  onMouseEnter: r,
  id: s
}) {
  const o = Ee(() => a(l.value), [a, l.value]), u = Yt(() => {
    if (!n) return l.label;
    const c = l.label.toLowerCase().indexOf(n.toLowerCase());
    return c < 0 ? l.label : /* @__PURE__ */ e.createElement(e.Fragment, null, l.label.substring(0, c), /* @__PURE__ */ e.createElement("strong", null, l.label.substring(c, c + n.length)), l.label.substring(c + n.length));
  }, [l.label, n]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: s,
      role: "option",
      "aria-selected": t,
      className: "tlDropdownSelect__option" + (t ? " tlDropdownSelect__option--highlighted" : ""),
      onClick: o,
      onMouseEnter: r
    },
    /* @__PURE__ */ e.createElement(Zt, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const yo = ({ controlId: l, state: t }) => {
  const n = re(), a = t.value ?? [], r = t.multiSelect === !0, s = t.customOrder === !0, o = t.mandatory === !0, u = t.disabled === !0, c = t.editable !== !1, i = t.optionsLoaded === !0, d = t.options ?? [], m = t.emptyOptionLabel ?? "", p = s && r && !u && c, h = de({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), g = h["js.dropdownSelect.nothingFound"], T = Ee(
    (H) => h["js.dropdownSelect.removeChip"].replace("{0}", H),
    [h]
  ), [y, E] = Me(!1), [_, k] = Me(""), [I, w] = Me(-1), [v, C] = Me(!1), [O, $] = Me({}), [A, D] = Me(null), [M, S] = Me(null), [W, P] = Me(null), j = nt(null), B = nt(null), L = nt(null), R = nt(a);
  R.current = a;
  const V = nt(-1), f = Yt(
    () => new Set(a.map((H) => H.value)),
    [a]
  ), x = Yt(() => {
    let H = d.filter((q) => !f.has(q.value));
    if (_) {
      const q = _.toLowerCase();
      H = H.filter((oe) => oe.label.toLowerCase().includes(q));
    }
    return H;
  }, [d, f, _]);
  Ue(() => {
    _ && x.length === 1 ? w(0) : w(-1);
  }, [x.length, _]), Ue(() => {
    y && i && B.current && B.current.focus();
  }, [y, i, a]), Ue(() => {
    var oe, ie;
    if (V.current < 0) return;
    const H = V.current;
    V.current = -1;
    const q = (oe = j.current) == null ? void 0 : oe.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    q && q.length > 0 ? q[Math.min(H, q.length - 1)].focus() : (ie = j.current) == null || ie.focus();
  }, [a]), Ue(() => {
    if (!y) return;
    const H = (q) => {
      j.current && !j.current.contains(q.target) && L.current && !L.current.contains(q.target) && (E(!1), k(""));
    };
    return document.addEventListener("mousedown", H), () => document.removeEventListener("mousedown", H);
  }, [y]), Ue(() => {
    if (!y || !j.current) return;
    const H = j.current.getBoundingClientRect(), q = window.innerHeight - H.bottom, ie = q < 300 && H.top > q;
    $({
      left: H.left,
      width: H.width,
      ...ie ? { bottom: window.innerHeight - H.top } : { top: H.bottom }
    });
  }, [y]);
  const Y = Ee(async () => {
    if (!(u || !c) && (E(!0), k(""), w(-1), C(!1), !i))
      try {
        await n("loadOptions");
      } catch {
        C(!0);
      }
  }, [u, c, i, n]), z = Ee(() => {
    var H;
    E(!1), k(""), w(-1), (H = j.current) == null || H.focus();
  }, []), Q = Ee(
    (H) => {
      let q;
      if (r) {
        const oe = d.find((ie) => ie.value === H);
        if (oe)
          q = [...R.current, oe];
        else
          return;
      } else {
        const oe = d.find((ie) => ie.value === H);
        if (oe)
          q = [oe];
        else
          return;
      }
      R.current = q, n(ut, { value: q.map((oe) => oe.value) }), r ? (k(""), w(-1)) : z();
    },
    [r, d, n, z]
  ), U = Ee(
    (H) => {
      V.current = R.current.findIndex((oe) => oe.value === H);
      const q = R.current.filter((oe) => oe.value !== H);
      R.current = q, n(ut, { value: q.map((oe) => oe.value) });
    },
    [n]
  ), ee = Ee(
    (H) => {
      H.stopPropagation(), n(ut, { value: [] }), z();
    },
    [n, z]
  ), ce = Ee((H) => {
    k(H.target.value);
  }, []), ne = Ee(
    (H) => {
      if (!y) {
        if (H.key === "ArrowDown" || H.key === "ArrowUp" || H.key === "Enter" || H.key === " ") {
          if (H.target.tagName === "BUTTON") return;
          H.preventDefault(), H.stopPropagation(), Y();
        }
        return;
      }
      switch (H.key) {
        case "ArrowDown":
          H.preventDefault(), H.stopPropagation(), w(
            (q) => q < x.length - 1 ? q + 1 : 0
          );
          break;
        case "ArrowUp":
          H.preventDefault(), H.stopPropagation(), w(
            (q) => q > 0 ? q - 1 : x.length - 1
          );
          break;
        case "Enter":
          H.preventDefault(), H.stopPropagation(), I >= 0 && I < x.length && Q(x[I].value);
          break;
        case "Escape":
          H.preventDefault(), H.stopPropagation(), z();
          break;
        case "Tab":
          z();
          break;
        case "Backspace":
          _ === "" && r && a.length > 0 && U(a[a.length - 1].value);
          break;
      }
    },
    [
      y,
      Y,
      z,
      x,
      I,
      Q,
      _,
      r,
      a,
      U
    ]
  ), be = Ee(
    async (H) => {
      H.preventDefault(), C(!1);
      try {
        await n("loadOptions");
      } catch {
        C(!0);
      }
    },
    [n]
  ), _e = Ee(
    (H, q) => {
      D(H), q.dataTransfer.effectAllowed = "move", q.dataTransfer.setData("text/plain", String(H));
    },
    []
  ), ke = Ee(
    (H, q) => {
      if (q.preventDefault(), q.dataTransfer.dropEffect = "move", A === null || A === H) {
        S(null), P(null);
        return;
      }
      const oe = q.currentTarget.getBoundingClientRect(), ie = oe.left + oe.width / 2, qe = q.clientX < ie ? "before" : "after";
      S(H), P(qe);
    },
    [A]
  ), ye = Ee(
    (H) => {
      if (H.preventDefault(), A === null || M === null || W === null || A === M) return;
      const q = [...R.current], [oe] = q.splice(A, 1);
      let ie = M;
      A < M ? ie = W === "before" ? ie - 1 : ie : ie = W === "before" ? ie : ie + 1, q.splice(ie, 0, oe), R.current = q, n(ut, { value: q.map((qe) => qe.value) }), D(null), S(null), P(null);
    },
    [A, M, W, n]
  ), Le = Ee(() => {
    D(null), S(null), P(null);
  }, []);
  if (Ue(() => {
    if (I < 0 || !L.current) return;
    const H = L.current.querySelector(
      `[id="${l}-opt-${I}"]`
    );
    H && H.scrollIntoView({ block: "nearest" });
  }, [I, l]), !c)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, a.map((H) => /* @__PURE__ */ e.createElement("span", { key: H.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(Zt, { image: H.image }), /* @__PURE__ */ e.createElement("span", null, H.label))));
  const Pe = !o && a.length > 0 && !u, ct = y ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: L,
      className: "tlDropdownSelect__dropdown",
      style: O,
      ...gl
    },
    (i || v) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: B,
        type: "text",
        className: "tlDropdownSelect__search",
        value: _,
        onChange: ce,
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
      !i && !v && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      v && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: be }, h["js.dropdownSelect.error"])),
      i && x.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, g),
      i && x.map((H, q) => /* @__PURE__ */ e.createElement(
        Co,
        {
          key: H.value,
          id: `${l}-opt-${q}`,
          option: H,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, a.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, m) : a.map((H, q) => {
      let oe = "";
      return A === q ? oe = "tlDropdownSelect__chip--dragging" : M === q && W === "before" ? oe = "tlDropdownSelect__chip--dropBefore" : M === q && W === "after" && (oe = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        _o,
        {
          key: H.value,
          option: H,
          removable: !u && (r || !o),
          onRemove: U,
          removeLabel: T(H.label),
          draggable: p,
          onDragStart: p ? (ie) => _e(q, ie) : void 0,
          onDragOver: p ? (ie) => ke(q, ie) : void 0,
          onDrop: p ? ye : void 0,
          onDragEnd: p ? Le : void 0,
          dragClassName: p ? oe : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, Pe && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: ee,
        "aria-label": h["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, y ? "▲" : "▼"))
  ), ct && Fn.createPortal(ct, document.body));
}, { useCallback: Bt, useRef: wo } = e, On = "application/x-tl-color", ko = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: a,
  onSwap: r,
  onReplace: s
}) => {
  const o = wo(null), u = Bt(
    (d) => (m) => {
      o.current = d, m.dataTransfer.effectAllowed = "move";
    },
    []
  ), c = Bt((d) => {
    d.preventDefault(), d.dataTransfer.dropEffect = "move";
  }, []), i = Bt(
    (d) => (m) => {
      m.preventDefault();
      const p = m.dataTransfer.getData(On);
      p ? s(d, p) : o.current !== null && o.current !== d && r(o.current, d), o.current = null;
    },
    [r, s]
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
        onDragOver: c,
        onDrop: i(m)
      }
    ))
  );
};
function $n(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function Gt(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function Hn(l) {
  if (!Gt(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function Wn(l, t, n) {
  const a = (r) => $n(r).toString(16).padStart(2, "0");
  return "#" + a(l) + a(t) + a(n);
}
function No(l, t, n) {
  const a = l / 255, r = t / 255, s = n / 255, o = Math.max(a, r, s), u = Math.min(a, r, s), c = o - u;
  let i = 0;
  c !== 0 && (o === a ? i = (r - s) / c % 6 : o === r ? i = (s - a) / c + 2 : i = (a - r) / c + 4, i *= 60, i < 0 && (i += 360));
  const d = o === 0 ? 0 : c / o;
  return [i, d, o];
}
function So(l, t, n) {
  const a = n * t, r = a * (1 - Math.abs(l / 60 % 2 - 1)), s = n - a;
  let o = 0, u = 0, c = 0;
  return l < 60 ? (o = a, u = r, c = 0) : l < 120 ? (o = r, u = a, c = 0) : l < 180 ? (o = 0, u = a, c = r) : l < 240 ? (o = 0, u = r, c = a) : l < 300 ? (o = r, u = 0, c = a) : (o = a, u = 0, c = r), [
    Math.round((o + s) * 255),
    Math.round((u + s) * 255),
    Math.round((c + s) * 255)
  ];
}
function Do(l) {
  return No(...Hn(l));
}
function Ft(l, t, n) {
  return Wn(...So(l, t, n));
}
const { useCallback: Ve, useRef: Tn } = e, To = ({ color: l, onColorChange: t }) => {
  const [n, a, r] = Do(l), s = Tn(null), o = Tn(null), u = Ve(
    (g, T) => {
      var k;
      const y = (k = s.current) == null ? void 0 : k.getBoundingClientRect();
      if (!y) return;
      const E = Math.max(0, Math.min(1, (g - y.left) / y.width)), _ = Math.max(0, Math.min(1, 1 - (T - y.top) / y.height));
      t(Ft(n, E, _));
    },
    [n, t]
  ), c = Ve(
    (g) => {
      g.preventDefault(), g.target.setPointerCapture(g.pointerId), u(g.clientX, g.clientY);
    },
    [u]
  ), i = Ve(
    (g) => {
      g.buttons !== 0 && u(g.clientX, g.clientY);
    },
    [u]
  ), d = Ve(
    (g) => {
      var _;
      const T = (_ = o.current) == null ? void 0 : _.getBoundingClientRect();
      if (!T) return;
      const E = Math.max(0, Math.min(1, (g - T.top) / T.height)) * 360;
      t(Ft(E, a, r));
    },
    [a, r, t]
  ), m = Ve(
    (g) => {
      g.preventDefault(), g.target.setPointerCapture(g.pointerId), d(g.clientY);
    },
    [d]
  ), p = Ve(
    (g) => {
      g.buttons !== 0 && d(g.clientY);
    },
    [d]
  ), h = Ft(n, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: s,
      className: "tlColorInput__svField",
      style: { backgroundColor: h },
      onPointerDown: c,
      onPointerMove: i
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
function Ro(l, t) {
  const n = t.toUpperCase();
  return l.some((a) => a != null && a.toUpperCase() === n);
}
const Lo = {
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
}, { useState: bt, useCallback: Se, useEffect: Rn, useRef: xo, useLayoutEffect: Mo } = e, Io = ({
  anchorRef: l,
  currentColor: t,
  palette: n,
  paletteColumns: a,
  defaultPalette: r,
  canReset: s,
  onConfirm: o,
  onCancel: u,
  onPaletteChange: c
}) => {
  const [i, d] = bt("palette"), [m, p] = bt(t), h = xo(null), g = de(Lo), [T, y] = bt(null);
  Mo(() => {
    if (!l.current || !h.current) return;
    const L = l.current.getBoundingClientRect(), R = h.current.getBoundingClientRect();
    let V = L.bottom + 4, f = L.left;
    V + R.height > window.innerHeight && (V = L.top - R.height - 4), f + R.width > window.innerWidth && (f = Math.max(0, L.right - R.width)), y({ top: V, left: f });
  }, [l]);
  const E = m != null, [_, k, I] = E ? Hn(m) : [0, 0, 0], [w, v] = bt((m == null ? void 0 : m.toUpperCase()) ?? "");
  Rn(() => {
    v((m == null ? void 0 : m.toUpperCase()) ?? "");
  }, [m]), $e(!0, { ESCAPE: u }), Rn(() => {
    const L = (V) => {
      h.current && !h.current.contains(V.target) && u();
    }, R = setTimeout(() => document.addEventListener("mousedown", L), 0);
    return () => {
      clearTimeout(R), document.removeEventListener("mousedown", L);
    };
  }, [u]);
  const C = Se(
    (L) => (R) => {
      const V = parseInt(R.target.value, 10);
      if (isNaN(V)) return;
      const f = $n(V);
      p(Wn(L === "r" ? f : _, L === "g" ? f : k, L === "b" ? f : I));
    },
    [_, k, I]
  ), O = Se(
    (L) => {
      if (m != null) {
        L.dataTransfer.setData(On, m.toUpperCase()), L.dataTransfer.effectAllowed = "move";
        const R = document.createElement("div");
        R.style.width = "33px", R.style.height = "33px", R.style.backgroundColor = m, R.style.borderRadius = "3px", R.style.border = "1px solid rgba(0,0,0,0.1)", R.style.position = "absolute", R.style.top = "-9999px", document.body.appendChild(R), L.dataTransfer.setDragImage(R, 16, 16), requestAnimationFrame(() => document.body.removeChild(R));
      }
    },
    [m]
  ), $ = Se((L) => {
    const R = L.target.value;
    v(R), Gt(R) && p(R);
  }, []), A = Se(() => {
    p(null);
  }, []), D = Se((L) => {
    p(L);
  }, []), M = Se(
    (L) => {
      o(L);
    },
    [o]
  ), S = Se(
    (L, R) => {
      const V = [...n], f = V[L];
      V[L] = V[R], V[R] = f, c(V);
    },
    [n, c]
  ), W = Se(
    (L, R) => {
      const V = [...n];
      V[L] = R, c(V);
    },
    [n, c]
  ), P = Se(() => {
    c([...r]);
  }, [r, c]), j = Se(
    (L) => {
      if (Ro(n, L)) return;
      const R = n.indexOf(null);
      if (R < 0) return;
      const V = [...n];
      V[R] = L.toUpperCase(), c(V);
    },
    [n, c]
  ), B = Se(() => {
    m != null && j(m), o(m);
  }, [m, o, j]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: h,
      style: T ? { top: T.top, left: T.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (i === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => d("palette")
      },
      g["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (i === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => d("mixer")
      },
      g["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, i === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      ko,
      {
        colors: n,
        columns: a,
        onSelect: D,
        onConfirm: M,
        onSwap: S,
        onReplace: W
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: P }, g["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(To, { color: m ?? "#000000", onColorChange: p }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, g["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, g["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (E ? "" : " tlColorInput--noColor"),
        style: E ? { backgroundColor: m } : void 0,
        draggable: E,
        onDragStart: E ? O : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, g["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: E ? _ : "",
        onChange: C("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, g["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: E ? k : "",
        onChange: C("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, g["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: E ? I : "",
        onChange: C("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, g["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (w !== "" && !Gt(w) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: w,
        onChange: $
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, s && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: A }, g["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, g["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: B }, g["js.colorInput.ok"]))
  );
}, jo = { "js.colorInput.chooseColor": "Choose color" }, { useState: Ao, useCallback: gt, useRef: Po } = e, Bo = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), r = re(), s = de(jo), [o, u] = Ao(!1), c = Po(null), i = n, d = t.editable !== !1, m = t.palette ?? [], p = t.paletteColumns ?? 6, h = t.defaultPalette ?? m, g = gt(() => {
    d && u(!0);
  }, [d]), T = gt(
    (_) => {
      u(!1), a(_);
    },
    [a]
  ), y = gt(() => {
    u(!1);
  }, []), E = gt(
    (_) => {
      r("paletteChanged", { palette: _ });
    },
    [r]
  );
  return d ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: c,
      className: "tlColorInput__swatch" + (i == null ? " tlColorInput__swatch--noColor" : ""),
      style: i != null ? { backgroundColor: i } : void 0,
      onClick: g,
      disabled: t.disabled === !0,
      title: i ?? "",
      "aria-label": s["js.colorInput.chooseColor"]
    }
  ), o && /* @__PURE__ */ e.createElement(
    Io,
    {
      anchorRef: c,
      currentColor: i,
      palette: m,
      paletteColumns: p,
      defaultPalette: h,
      canReset: t.canReset !== !1,
      onConfirm: T,
      onCancel: y,
      onPaletteChange: E
    }
  )) : /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlColorInput tlColorInput--immutable" + (i == null ? " tlColorInput--noColor" : ""),
      style: i != null ? { backgroundColor: i } : void 0,
      title: i ?? ""
    }
  );
}, { useState: lt, useCallback: Be, useEffect: Ot, useRef: Ln, useLayoutEffect: Fo, useMemo: Oo } = e, $o = {
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
}, Ho = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: a,
  onSelect: r,
  onCancel: s,
  onLoadIcons: o
}) => {
  const u = de($o), [c, i] = lt("simple"), [d, m] = lt(""), [p, h] = lt(t ?? ""), [g, T] = lt(!1), [y, E] = lt(null), _ = Ln(null), k = Ln(null);
  Fo(() => {
    if (!l.current || !_.current) return;
    const M = l.current.getBoundingClientRect(), S = _.current.getBoundingClientRect();
    let W = M.bottom + 4, P = M.left;
    W + S.height > window.innerHeight && (W = M.top - S.height - 4), P + S.width > window.innerWidth && (P = Math.max(0, M.right - S.width)), E({ top: W, left: P });
  }, [l]), Ot(() => {
    !a && !g && o().catch(() => T(!0));
  }, [a, g, o]), Ot(() => {
    a && k.current && k.current.focus();
  }, [a]), $e(!0, { ESCAPE: s }), Ot(() => {
    const M = (W) => {
      _.current && !_.current.contains(W.target) && s();
    }, S = setTimeout(() => document.addEventListener("mousedown", M), 0);
    return () => {
      clearTimeout(S), document.removeEventListener("mousedown", M);
    };
  }, [s]);
  const I = Oo(() => {
    if (!d) return n;
    const M = d.toLowerCase();
    return n.filter(
      (S) => S.prefix.toLowerCase().includes(M) || S.label.toLowerCase().includes(M) || S.terms != null && S.terms.some((W) => W.includes(M))
    );
  }, [n, d]), w = Be((M) => {
    m(M.target.value);
  }, []), v = Be(
    (M) => {
      r(M);
    },
    [r]
  ), C = Be((M) => {
    h(M);
  }, []), O = Be((M) => {
    h(M.target.value);
  }, []), $ = Be(() => {
    r(p || null);
  }, [p, r]), A = Be(() => {
    r(null);
  }, [r]), D = Be(async (M) => {
    M.preventDefault(), T(!1);
    try {
      await o();
    } catch {
      T(!0);
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
        className: "tlIconSelect__tab" + (c === "simple" ? " tlIconSelect__tab--active" : ""),
        onClick: () => i("simple")
      },
      u["js.iconSelect.simpleTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (c === "advanced" ? " tlIconSelect__tab--active" : ""),
        onClick: () => i("advanced")
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
      !a && !g && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      g && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: D }, u["js.iconSelect.loadError"])),
      a && I.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      a && I.map(
        (M) => M.variants.map((S) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: S.encoded,
            className: "tlIconSelect__iconCell" + (S.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": S.encoded === t,
            tabIndex: 0,
            title: M.label,
            onClick: () => c === "simple" ? v(S.encoded) : C(S.encoded),
            onKeyDown: (W) => {
              (W.key === "Enter" || W.key === " ") && (W.preventDefault(), c === "simple" ? v(S.encoded) : C(S.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(De, { encoded: S.encoded })
        ))
      )
    ),
    c === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: p,
        onChange: O
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, p && /* @__PURE__ */ e.createElement(De, { encoded: p })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, p ? p.startsWith("css:") ? p.substring(4) : p : ""))),
    c === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: s }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: A }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: $ }, u["js.iconSelect.ok"]))
  );
}, Wo = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: Uo, useCallback: Et, useRef: Vo } = e, zo = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), r = re(), s = de(Wo), [o, u] = Uo(!1), c = Vo(null), i = n, d = t.editable !== !1, m = t.disabled === !0, p = t.icons ?? [], h = t.iconsLoaded === !0, g = Et(() => {
    d && !m && u(!0);
  }, [d, m]), T = Et(
    (_) => {
      u(!1), a(_);
    },
    [a]
  ), y = Et(() => {
    u(!1);
  }, []), E = Et(async () => {
    await r("loadIcons");
  }, [r]);
  return d ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: c,
      className: "tlIconSelect__swatch" + (i == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: g,
      disabled: m,
      title: i ?? "",
      "aria-label": s["js.iconSelect.chooseIcon"]
    },
    i ? /* @__PURE__ */ e.createElement(De, { encoded: i }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), o && /* @__PURE__ */ e.createElement(
    Ho,
    {
      anchorRef: c,
      currentValue: i,
      icons: p,
      iconsLoaded: h,
      onSelect: T,
      onCancel: y,
      onLoadIcons: E
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, i ? /* @__PURE__ */ e.createElement(De, { encoded: i }) : null));
}, { useCallback: ze, useEffect: Ko, useMemo: xn, useRef: Yo, useState: $t } = e, Go = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, Xo = [1, 2, 3, 4];
function qo(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const a = parseFloat(n[1]), r = n[2] || "px";
  return r === "rem" || r === "em" ? a * t : a;
}
function Zo(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let a = 1;
  for (const r of Xo)
    n >= r && (a = r);
  return a;
}
function Qo(l, t) {
  const n = Go[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function Jo(l, t) {
  const n = Math.max(1, t), a = {}, r = (m, p) => !!(a[m] && a[m][p]), s = (m, p) => {
    a[m] || (a[m] = {}), a[m][p] = !0;
  }, o = [];
  let u = 0, c = 0;
  const i = (m) => {
    let p = null;
    for (const g of o) g.rowStart === m && (p = g);
    if (!p) return;
    let h = p.colEnd;
    for (; h < n && !r(m, h); ) h++;
    if (h !== p.colEnd) {
      for (let g = p.rowStart; g < p.rowEnd; g++)
        for (let T = p.colEnd; T < h; T++) s(g, T);
      p.colEnd = h;
    }
  };
  for (const m of l) {
    const p = n <= 1 ? 1 : Math.max(1, m.rowSpan || 1);
    let h = Math.min(Qo(m.width, n), n);
    for (; r(u, c); )
      c++, c >= n && (c = 0, u++);
    let g = 0;
    for (let k = c; k < n && !r(u, k); k++)
      g++;
    if (h > g) {
      for (i(u), c = 0, u++; r(u, c); )
        c++, c >= n && (c = 0, u++);
      g = 0;
      for (let k = c; k < n && !r(u, k); k++)
        g++;
      h = Math.min(h, g);
    }
    const T = c, y = c + h, E = u, _ = u + p;
    o.push({ id: m.id, colStart: T, colEnd: y, rowStart: E, rowEnd: _ });
    for (let k = E; k < _; k++)
      for (let I = T; I < y; I++) s(k, I);
    c = y, c >= n && (c = 0, u++);
  }
  i(u);
  let d = 0;
  for (const m of o) m.rowEnd > d && (d = m.rowEnd);
  for (let m = 1; m < d; m++)
    for (let p = 0; p < n; p++) {
      if (r(m, p)) continue;
      const h = o.find((g) => g.rowEnd === m && g.colStart <= p && p < g.colEnd);
      if (h) {
        h.rowEnd = m + 1;
        for (let g = h.colStart; g < h.colEnd; g++) s(m, g);
      }
    }
  return o;
}
const es = ({ controlId: l }) => {
  const t = X(), n = re(), a = t.minColWidth ?? "16rem", r = (t.children ?? []).filter((v) => v && v.id), s = Yo(null), [o, u] = $t(1), c = t.editMode === !0;
  Ko(() => {
    const v = s.current;
    if (!v) return;
    const C = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, O = qo(a, C), $ = () => u(Zo(v.clientWidth, O));
    $();
    const A = new ResizeObserver($);
    return A.observe(v), () => A.disconnect();
  }, [a]);
  const i = xn(() => Jo(r, o), [r, o]), d = xn(() => {
    const v = {};
    for (const C of i) v[C.id] = C;
    return v;
  }, [i]), [m, p] = $t(null), [h, g] = $t(null), T = ze((v, C) => {
    if (!c) {
      v.preventDefault();
      return;
    }
    p(C), v.dataTransfer.effectAllowed = "move", v.dataTransfer.setData("text/plain", C);
  }, [c]), y = ze((v, C) => {
    if (!c || !m || m === C) return;
    v.preventDefault(), v.dataTransfer.dropEffect = "move";
    const O = v.currentTarget.getBoundingClientRect(), $ = v.clientX < O.left + O.width / 2;
    g((A) => A && A.id === C && A.before === $ ? A : { id: C, before: $ });
  }, [c, m]), E = ze(() => {
  }, []), _ = ze((v, C, O) => {
    const $ = r.map((S) => S.id), A = $.indexOf(v);
    if (A < 0) return;
    $.splice(A, 1);
    const D = $.indexOf(C);
    if (D < 0) {
      $.splice(A, 0, v);
      return;
    }
    const M = O ? D : D + 1;
    $.splice(M, 0, v), n("reorder", { order: $ });
  }, [r, n]), k = ze((v, C) => {
    if (!c || !m || m === C) return;
    v.preventDefault();
    const O = v.currentTarget.getBoundingClientRect(), $ = v.clientX < O.left + O.width / 2;
    _(m, C, $), p(null), g(null);
  }, [c, m, _]), I = ze(() => {
    p(null), g(null);
  }, []), w = {
    display: "grid",
    gridTemplateColumns: `repeat(${o}, 1fr)`,
    gap: "1rem"
  };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: s,
      className: "tlDashboard" + (c ? " tlDashboard--edit" : "")
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: w }, r.map((v) => {
      const C = d[v.id];
      if (!C) return null;
      const O = {
        gridColumn: `${C.colStart + 1} / ${C.colEnd + 1}`,
        gridRow: `${C.rowStart + 1} / ${C.rowEnd + 1}`
      }, $ = ["tlDashboard__tile"];
      return m === v.id && $.push("tlDashboard__tile--dragging"), h && h.id === v.id && $.push(h.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: v.id,
          className: $.join(" "),
          style: O,
          draggable: c,
          onDragStart: (A) => T(A, v.id),
          onDragOver: (A) => y(A, v.id),
          onDragLeave: E,
          onDrop: (A) => k(A, v.id),
          onDragEnd: I
        },
        /* @__PURE__ */ e.createElement(G, { control: v.control }),
        c && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: ts, useRef: Mn, useState: In, useEffect: ns, useLayoutEffect: ls } = e, as = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, a) => /* @__PURE__ */ e.createElement("span", { key: a, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(G, { control: n }))));
}, rs = ({ group: l }) => {
  var m, p;
  const [t, n] = In(!1), [a, r] = In({}), s = Mn(null), o = Mn(null), u = ts(() => {
    n((h) => !h);
  }, []);
  ls(() => {
    if (!t) return;
    const h = () => {
      const g = s.current;
      if (!g) return;
      const T = g.getBoundingClientRect();
      r({
        position: "fixed",
        top: T.bottom + 4,
        right: Math.max(8, window.innerWidth - T.right),
        left: "auto"
      });
    };
    return h(), window.addEventListener("resize", h), window.addEventListener("scroll", h, !0), () => {
      window.removeEventListener("resize", h), window.removeEventListener("scroll", h, !0);
    };
  }, [t]), ns(() => {
    if (!t) return;
    const h = (g) => {
      o.current && !o.current.contains(g.target) && s.current && !s.current.contains(g.target) && n(!1);
    };
    return document.addEventListener("mousedown", h), () => document.removeEventListener("mousedown", h);
  }, [t]), $e(t, { ESCAPE: () => n(!1) }), qt(t, o, "first");
  const c = l.items.filter((h) => h != null);
  if (c.length === 0) return null;
  if (c.length === 1 && !((m = l.subGroups) != null && m.length) && !l.icon)
    return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(G, { control: c[0] })));
  const i = l.label ?? l.name, d = !!l.icon;
  return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--menu" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: s,
      type: "button",
      className: "tlToolbar__menuTrigger" + (d ? " tlToolbar__menuTrigger--icon" : ""),
      onMouseDown: (h) => h.preventDefault(),
      onClick: u,
      "aria-expanded": t,
      "aria-haspopup": "true",
      "aria-label": d ? i : void 0,
      title: d ? i : void 0
    },
    d ? /* @__PURE__ */ e.createElement(De, { encoded: l.icon, className: "tlToolbar__menuIcon" }) : /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement("span", null, i), /* @__PURE__ */ e.createElement("svg", { className: "tlToolbar__chevron", viewBox: "0 0 24 24", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("polyline", { points: "6,9 12,15 18,9" })))
  ), Fn.createPortal(
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
      c.map((h, g) => /* @__PURE__ */ e.createElement("div", { key: g, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(G, { control: h }))),
      (p = l.subGroups) == null ? void 0 : p.map((h, g) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${g}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), h.items.map((T, y) => /* @__PURE__ */ e.createElement("div", { key: y, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(G, { control: T })))))
    ),
    document.body
  ));
}, os = ({ controlId: l }) => {
  const a = (X().groups ?? []).filter((r) => r.items.some((s) => s != null));
  return a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, a.map((r, s) => /* @__PURE__ */ e.createElement(e.Fragment, { key: r.name }, s > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), r.display === "menu" ? /* @__PURE__ */ e.createElement(rs, { group: r }) : /* @__PURE__ */ e.createElement(as, { group: r }))));
}, ss = ({ frame: l, covered: t }) => {
  const [n, a] = ot(), r = [
    "tlTileStack__frame",
    t ? "tlTileStack__frame--covered" : "",
    n
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(st, { host: a }, /* @__PURE__ */ e.createElement("div", { className: r }, /* @__PURE__ */ e.createElement(G, { control: l })));
}, cs = ({ controlId: l }) => {
  const t = X(), [n, a] = ot(), r = t.frames ?? [], s = t.activeIndex ?? 0;
  return /* @__PURE__ */ e.createElement(st, { host: a }, /* @__PURE__ */ e.createElement("div", { id: l, className: n ? "tlTileStack " + n : "tlTileStack" }, r.map((o, u) => /* @__PURE__ */ e.createElement(ss, { key: o.controlId, frame: o, covered: u !== s }))));
}, is = ({ controlId: l }) => {
  const t = X(), n = re(), a = t.content, r = t.breadcrumb ?? null;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAdaptiveDetail" }, r && r.length > 0 && /* @__PURE__ */ e.createElement("nav", { className: "tlAdaptiveDetail__breadcrumb", "aria-label": "Breadcrumb" }, r.map((s, o) => {
    const u = o === r.length - 1;
    return /* @__PURE__ */ e.createElement(e.Fragment, { key: s.depth }, o > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__sep" }, "›"), u ? /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__crumb tlAdaptiveDetail__crumb--current" }, s.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlAdaptiveDetail__crumb",
        onClick: () => n("navigate", { depth: s.depth })
      },
      s.label
    ));
  })), /* @__PURE__ */ e.createElement("div", { className: "tlAdaptiveDetail__content" }, a && /* @__PURE__ */ e.createElement(G, { control: a })));
}, us = ({ controlId: l }) => {
  const n = X().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((a, r) => /* @__PURE__ */ e.createElement(G, { key: r, control: a })));
}, ds = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), ms = {
  "js.sidebar.openDrawer": "Open navigation"
}, ps = ({ controlId: l }) => {
  const t = re(), n = de(ms);
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
K("TLButton", Pl);
K("TLUploadButton", Bl);
K("TLToggleButton", Ol);
K("TLTextInput", _l);
K("TLPasswordInput", yl);
K("TLNumberInput", kl);
K("TLDatePicker", Sl);
K("TLSelect", Tl);
K("TLBooleanChoice", Ll);
K("TLCheckbox", jl);
K("TLCounter", $l);
K("TLTabBar", Wl);
K("TLFieldList", Ul);
K("TLAudioRecorder", zl);
K("TLAudioPlayer", Yl);
K("TLFileUpload", Xl);
K("TLBinaryField", Zl);
K("TLFileChips", ea);
K("TLRelativeTime", la);
K("TLAnchor", aa);
K("TLScrollLink", ra);
K("TLAvatar", ca);
K("TLDownload", ua);
K("TLPhotoCapture", ma);
K("TLPhotoViewer", fa);
K("TLPdfViewer", ba);
K("TLSplitPanel", ga);
K("TLPanel", ka);
K("TLInset", Pa);
K("TLMaximizeRoot", Na);
K("TLDeckPane", Sa);
K("TLSidebar", ja);
K("TLStack", Aa);
K("TLGrid", Ba);
K("TLCard", Fa);
K("TLAppBar", Oa);
K("TLBreadcrumb", Ha);
K("TLBottomBar", Ua);
K("TLDialog", Ka);
K("TLDialogManager", Xa);
K("TLWindow", Ja);
K("TLDrawer", nr);
K("TLMenuRegion", ar);
K("TLSnackbar", cr);
K("TLNoticeBar", hr);
K("TLMenu", gr);
K("TLAppShell", vr);
K("TLText", _r);
K("TLTableView", Sr);
K("TLColumnSelect", Tr);
K("TLCalendar", Wr);
K("TLFormLayout", Zr);
K("TLFormGroup", eo);
K("TLFormField", ao);
K("TLResourceCell", ro);
K("TLTreeView", ho);
K("TLDropdownSelect", yo);
K("TLColorInput", Bo);
K("TLIconSelect", zo);
K("TLDashboard", es);
K("TLToolbar", os);
K("TLTileStack", cs);
K("TLAdaptiveDetail", is);
K("TLSlot", us);
K("TLSlotContent", ds);
K("TLDrawerToggle", ps);
