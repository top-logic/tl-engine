import { React as e, useTLFieldValue as Te, useTLCommand as ae, useTLState as X, useKeyboardBinding as pe, useTLUpload as Ye, useFill as kt, FillBarrier as Ae, TLChild as G, useI18N as me, useTLDataUrl as Ge, scrollToAnchor as ul, useStandaloneKeyboardScope as Fe, useFillHost as rt, FillProvider as ot, KeyboardScopeProvider as Yt, useFocusTrap as Gt, CMD_VALUE_CHANGED as it, anchoredOverlayProps as dl, register as K } from "tl-react-bridge";
const { useCallback: nn, useRef: ml } = e, pl = 300, fl = ({ controlId: l, state: t }) => {
  const [n, a, r] = Te({
    debounceMs: pl,
    sendOnBlur: t.sendValueOnBlur === !0
  }), o = ae(), s = ml(!1), u = nn(
    (T) => {
      s.current = !0, a(T.target.value);
    },
    [a]
  ), c = t.commitOnBlur === !0, i = nn(async () => {
    await r(), c && s.current && (s.current = !1, o("commit"));
  }, [r, c, o]), d = t.multiline === !0;
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
}, { useCallback: ln } = e, hl = 300, bl = ({ controlId: l, state: t }) => {
  const [n, a, r] = Te({ debounceMs: hl }), o = ln(
    (m) => {
      a(m.target.value);
    },
    [a]
  ), s = ln(() => {
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
      onChange: o,
      onBlur: s,
      disabled: t.disabled === !0,
      className: d,
      "aria-invalid": u || void 0,
      title: u && i ? i : void 0
    }
  ));
}, { useCallback: an } = e, gl = 300, El = ({ controlId: l, state: t }) => {
  const [n, a, r] = Te({
    debounceMs: gl,
    sendOnBlur: t.sendValueOnBlur === !0
  }), o = an(
    (p) => {
      const h = p.target.value;
      a(h === "" ? null : h);
    },
    [a]
  ), s = an(() => {
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
      onChange: o,
      onBlur: s,
      disabled: t.disabled === !0,
      className: m,
      "aria-invalid": c || void 0,
      title: c && d ? d : void 0
    }
  ));
}, { useCallback: vl } = e, _l = ({ controlId: l, state: t }) => {
  const [n, a] = Te(), r = vl(
    (c) => {
      a(c.target.value || null);
    },
    [a]
  );
  if (t.editable === !1) {
    const c = t.displayValue ?? n ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, c);
  }
  const o = t.hasError === !0, s = t.hasWarnings === !0, u = [
    "tlReactDatePicker",
    o ? "tlReactDatePicker--error" : "",
    !o && s ? "tlReactDatePicker--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: t.inputType ?? "date",
      value: n ?? "",
      onChange: r,
      disabled: t.disabled === !0,
      className: u,
      "aria-invalid": o || void 0
    }
  ));
}, { useCallback: Cl } = e, yl = ({ controlId: l, state: t, config: n }) => {
  var d;
  const [a, r] = Te(), o = Cl(
    (m) => {
      r(m.target.value || null);
    },
    [r]
  ), s = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const m = ((d = s.find((p) => p.value === a)) == null ? void 0 : d.label) ?? "";
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
      onChange: o,
      disabled: t.disabled === !0,
      className: i,
      "aria-invalid": u || void 0
    },
    t.nullable !== !1 && /* @__PURE__ */ e.createElement("option", { value: "" }),
    s.map((m) => /* @__PURE__ */ e.createElement("option", { key: m.value, value: m.value }, m.label))
  ));
}, { useCallback: wl } = e, kl = ({ controlId: l, state: t }) => {
  const [n, a] = Te(), r = t.options ?? [], o = t.presentation === "select", s = t.disabled === !0, u = t.hasError === !0, c = t.hasWarnings === !0, i = wl(
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
  return o ? /* @__PURE__ */ e.createElement(
    "select",
    {
      id: l,
      className: m + " tlReactSelect",
      value: d >= 0 ? String(d) : "",
      disabled: s,
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
        disabled: s,
        onChange: () => i(h)
      }
    ), /* @__PURE__ */ e.createElement("span", { className: "tlBooleanChoice__label" }, p.label)))
  );
}, { useCallback: Nl, useRef: Sl, useEffect: Dl } = e, Tl = ({ controlId: l, state: t }) => {
  const [n, a] = Te(), r = t.triState === !0, o = Sl(null);
  Dl(() => {
    o.current && (o.current.indeterminate = r && n !== !0 && n !== !1);
  }, [r, n]);
  const s = Nl(
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
        ref: o,
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
      ref: o,
      checked: n === !0,
      onChange: s,
      disabled: t.disabled === !0,
      className: i,
      "aria-invalid": u || void 0,
      "aria-checked": r && n !== !0 && n !== !1 ? "mixed" : n === !0
    }
  );
};
function Se({ encoded: l, className: t }) {
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
const { useCallback: Rl } = e, Ll = ({ controlId: l, command: t, label: n, image: a, disabled: r, displayMode: o }) => {
  const s = X(), u = ae(), c = t ?? "click", i = n ?? s.label, d = a ?? s.image, m = r ?? s.disabled === !0, p = o ?? s.displayMode ?? "label-only", h = s.hidden === !0, g = s.tooltip, T = s.appearance, y = s.size, E = s.cssClasses, _ = s.navigateUrl, k = Rl(() => {
    if (_) {
      window.location.assign(_);
      return;
    }
    u(c);
  }, [u, c, _]), I = s.keyGesture;
  pe(I, () => m || h ? !1 : (k(), !0));
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
    d && /* @__PURE__ */ e.createElement(Se, { encoded: d, className: "tlReactButton__image" }),
    v && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, i)
  );
}, xl = ({ controlId: l }) => {
  const t = X(), n = Ye(), a = e.useRef(null), [r, o] = e.useState(!1), s = t.label ?? "", u = t.image, c = t.disabled === !0, i = t.hidden === !0, d = t.displayMode ?? "label-only", m = t.appearance, p = t.accept, h = t.multiple === !0, g = e.useCallback(() => {
    var I;
    c || r || (I = a.current) == null || I.click();
  }, [c, r]), T = e.useCallback(async (I) => {
    const w = I.target.files;
    if (!w || w.length === 0) return;
    const v = new FormData();
    for (let C = 0; C < w.length; C++)
      v.append("file", w[C], w[C].name);
    I.target.value = "", o(!0);
    try {
      await n(v);
    } finally {
      o(!1);
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
      "aria-label": y ? s : void 0
    },
    E && u && /* @__PURE__ */ e.createElement(Se, { encoded: u, className: "tlReactButton__image" }),
    _ && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, s)
  ));
}, { useCallback: Ml } = e, Il = ({ controlId: l, command: t, label: n, active: a, disabled: r }) => {
  const o = X(), s = ae(), u = t ?? "click", c = n ?? o.label, i = a ?? o.active === !0, d = r ?? o.disabled === !0, m = Ml(() => {
    s(u);
  }, [s, u]);
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
}, jl = ({ controlId: l }) => {
  const t = X(), n = ae(), a = t.count ?? 0, r = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Al } = e, Pl = ({ controlId: l }) => {
  const t = X(), n = ae(), a = kt(!0), r = t.tabs ?? [], o = t.activeTabId, s = Al((u) => {
    u !== o && n("selectTab", { tabId: u });
  }, [n, o]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar " + a }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, r.map((u) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: u.id,
      role: "tab",
      "aria-selected": u.id === o,
      className: "tlReactTabBar__tab" + (u.id === o ? " tlReactTabBar__tab--active" : ""),
      onClick: () => s(u.id)
    },
    u.icon && /* @__PURE__ */ e.createElement(Se, { encoded: u.icon, className: "tlReactTabBar__tabIcon" }),
    u.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, /* @__PURE__ */ e.createElement(Ae, null, t.activeContent && /* @__PURE__ */ e.createElement(G, { control: t.activeContent }))));
}, Bl = ({ controlId: l }) => {
  const t = X(), n = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((r, o) => /* @__PURE__ */ e.createElement("div", { key: o, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(G, { control: r })))));
}, Ol = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Fl = ({ controlId: l }) => {
  const t = X(), n = Ye(), [a, r] = e.useState("idle"), [o, s] = e.useState(null), u = e.useRef(null), c = e.useRef([]), i = e.useRef(null), d = t.status ?? "idle", m = t.error, p = d === "received" ? "idle" : a !== "idle" ? a : d, h = e.useCallback(async () => {
    if (a === "recording") {
      const _ = u.current;
      _ && _.state !== "inactive" && _.stop();
      return;
    }
    if (a !== "uploading") {
      if (s(null), !window.isSecureContext || !navigator.mediaDevices) {
        s("js.audioRecorder.error.insecure");
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
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", _), s("js.audioRecorder.error.denied"), r("idle");
      }
    }
  }, [a, n]), g = me(Ol), T = p === "recording" ? g["js.audioRecorder.stop"] : p === "uploading" ? g["js.uploading"] : g["js.audioRecorder.record"], y = p === "uploading", E = ["tlAudioRecorder__button"];
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
  ), o && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, g[o]), m && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, m));
}, $l = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Hl = ({ controlId: l }) => {
  const t = X(), n = Ge(), a = !!t.hasAudio, r = t.dataRevision ?? 0, [o, s] = e.useState(a ? "idle" : "disabled"), u = e.useRef(null), c = e.useRef(null), i = e.useRef(r);
  e.useEffect(() => {
    a ? o === "disabled" && s("idle") : (u.current && (u.current.pause(), u.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null), s("disabled"));
  }, [a]), e.useEffect(() => {
    r !== i.current && (i.current = r, u.current && (u.current.pause(), u.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null), (o === "playing" || o === "paused" || o === "loading") && s("idle"));
  }, [r]), e.useEffect(() => () => {
    u.current && (u.current.pause(), u.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null);
  }, []);
  const d = e.useCallback(async () => {
    if (o === "disabled" || o === "loading")
      return;
    if (o === "playing") {
      u.current && u.current.pause(), s("paused");
      return;
    }
    if (o === "paused" && u.current) {
      u.current.play(), s("playing");
      return;
    }
    if (!c.current) {
      s("loading");
      try {
        const y = await fetch(n);
        if (!y.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", y.status), s("idle");
          return;
        }
        const E = await y.blob();
        c.current = URL.createObjectURL(E);
      } catch (y) {
        console.error("[TLAudioPlayer] Fetch error:", y), s("idle");
        return;
      }
    }
    const T = new Audio(c.current);
    u.current = T, T.onended = () => {
      s("idle");
    }, T.play(), s("playing");
  }, [o, n]), m = me($l), p = o === "loading" ? m["js.loading"] : o === "playing" ? m["js.audioPlayer.pause"] : o === "disabled" ? m["js.audioPlayer.noAudio"] : m["js.audioPlayer.play"], h = o === "disabled" || o === "loading", g = ["tlAudioPlayer__button"];
  return o === "playing" && g.push("tlAudioPlayer__button--playing"), o === "loading" && g.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: g.join(" "),
      onClick: d,
      disabled: h,
      title: p,
      "aria-label": p
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${o === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Wl = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Ul = ({ controlId: l }) => {
  const t = X(), n = Ye(), [a, r] = e.useState("idle"), [o, s] = e.useState(!1), u = e.useRef(null), c = t.status ?? "idle", i = t.error, d = t.accept ?? "", m = c === "received" ? "idle" : a !== "idle" ? a : c, p = e.useCallback(async (w) => {
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
    w.preventDefault(), w.stopPropagation(), s(!0);
  }, []), y = e.useCallback((w) => {
    w.preventDefault(), w.stopPropagation(), s(!1);
  }, []), E = e.useCallback((w) => {
    var C;
    if (w.preventDefault(), w.stopPropagation(), s(!1), a === "uploading") return;
    const v = (C = w.dataTransfer.files) == null ? void 0 : C[0];
    v && p(v);
  }, [a, p]), _ = m === "uploading", k = me(Wl), I = m === "uploading" ? k["js.uploading"] : k["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${o ? " tlFileUpload--dragover" : ""}`,
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
}, Vl = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, zl = ({ controlId: l, state: t }) => {
  const a = X() ?? t ?? {}, r = Ye(), o = Ge(), s = me(Vl), u = a.editable !== !1, c = !!a.hasData, i = a.fileName ?? "download", d = a.dataRevision ?? 0, m = a.accept ?? "", p = a.status ?? "idle", h = a.error ?? null, [g, T] = e.useState("idle"), [y, E] = e.useState(!1), [_, k] = e.useState(!1), I = e.useRef(null), w = e.useCallback(async () => {
    if (!(!c || _)) {
      k(!0);
      try {
        const B = o + (o.includes("?") ? "&" : "?") + "rev=" + d, L = await fetch(B);
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
  }, [c, _, o, d, i]), v = e.useCallback(async (B) => {
    T("uploading");
    const L = new FormData();
    L.append("file", B, B.name), await r(L), T("idle");
  }, [r]), C = (p === "received" ? "idle" : g !== "idle" ? g : p) === "uploading", O = e.useCallback((B) => {
    var R;
    const L = (R = B.target.files) == null ? void 0 : R[0];
    L && v(L);
  }, [v]), F = e.useCallback(() => {
    var B;
    C || (B = I.current) == null || B.click();
  }, [C]), A = e.useCallback((B) => {
    B.preventDefault(), B.stopPropagation(), E(!0);
  }, []), D = e.useCallback((B) => {
    B.preventDefault(), B.stopPropagation(), E(!1);
  }, []), x = e.useCallback((B) => {
    var R;
    if (B.preventDefault(), B.stopPropagation(), E(!1), C) return;
    const L = (R = B.dataTransfer.files) == null ? void 0 : R[0];
    L && v(L);
  }, [C, v]), N = _ ? s["js.downloading"] : s["js.download.file"].replace("{0}", i), W = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: i }, i));
  if (!u)
    return c ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, W) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, s["js.download.noFile"]));
  const P = C, j = C ? s["js.uploading"] : s["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlBinaryField tlFileUpload${y ? " tlFileUpload--dragover" : ""}`,
      onDragOver: A,
      onDragLeave: D,
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
        className: "tlFileUpload__button" + (P ? " tlFileUpload__button--uploading" : ""),
        onClick: F,
        disabled: P,
        title: j,
        "aria-label": j
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    c && W,
    h && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, h)
  );
}, Kl = {
  "js.fileChips.add": "Add file",
  "js.fileChips.remove": "Remove {0}",
  "js.uploading": "Uploading…",
  "js.download.file": "Download {0}"
};
function Yl(l) {
  return l < 1024 ? l + " B" : l < 1024 * 1024 ? (l / 1024).toFixed(l < 10 * 1024 ? 1 : 0) + " KB" : (l / (1024 * 1024)).toFixed(1) + " MB";
}
const Gl = ({ controlId: l }) => {
  const t = X(), n = ae(), a = Ye(), r = Ge(), o = me(Kl), s = t.chips ?? [], u = t.editable === !0, [c, i] = e.useState(!1), [d, m] = e.useState(!1), p = e.useRef(null), h = e.useCallback(async (w) => {
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
        const O = await C.blob(), F = URL.createObjectURL(O), A = document.createElement("a");
        A.href = F, A.download = w.name, A.style.display = "none", document.body.appendChild(A), A.click(), document.body.removeChild(A), URL.revokeObjectURL(F);
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
    s.map((w) => {
      const v = o["js.download.file"].replace("{0}", w.name), C = o["js.fileChips.remove"].replace("{0}", w.name);
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
        w.size != null && /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__size" }, Yl(w.size))
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
        title: c ? o["js.uploading"] : o["js.fileChips.add"]
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
      /* @__PURE__ */ e.createElement("span", null, c ? o["js.uploading"] : o["js.fileChips.add"])
    ))
  );
}, Xl = 3e4;
function ql(l, t) {
  const n = Math.round((l - Date.now()) / 1e3), a = Math.abs(n), r = new Intl.RelativeTimeFormat(t, { numeric: "auto" });
  return a < 60 ? r.format(Math.trunc(n / 1), "second") : a < 3600 ? r.format(Math.trunc(n / 60), "minute") : a < 86400 ? r.format(Math.trunc(n / 3600), "hour") : a < 7 * 86400 ? r.format(Math.trunc(n / 86400), "day") : new Date(l).toLocaleDateString(t);
}
const Zl = ({ controlId: l }) => {
  const t = X(), n = t.timestamp, a = t.label ?? void 0, r = t.locale || navigator.language, [, o] = e.useState(0);
  return e.useEffect(() => {
    const s = setInterval(() => o((u) => u + 1), Xl);
    return () => clearInterval(s);
  }, []), n == null ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime tlRelativeTime--empty" }) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime", title: a }, ql(n, r));
}, Ql = ({ controlId: l }) => {
  const t = X(), n = t.anchor ?? void 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAnchor", "data-tl-anchor": n }, t.child && /* @__PURE__ */ e.createElement(G, { control: t.child }));
}, Jl = ({ controlId: l }) => {
  const t = X(), n = t.target, a = t.label ?? "";
  if (n == null)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlScrollLink tlScrollLink--empty" });
  const r = (o) => {
    o.preventDefault(), ul(n);
  };
  return /* @__PURE__ */ e.createElement("a", { id: l, className: "tlScrollLink", href: "#", onClick: r }, a);
};
function ea(l) {
  const t = l.trim().split(/\s+/).filter(Boolean);
  return t.length === 0 ? "?" : t.length === 1 ? t[0].slice(0, 2).toUpperCase() : (t[0][0] + t[t.length - 1][0]).toUpperCase();
}
function ta(l) {
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return Math.abs(t) % 360;
}
const na = ({ controlId: l }) => {
  const n = X().name;
  return n ? /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlAvatar",
      style: { backgroundColor: `hsl(${ta(n)}, 45%, 45%)` },
      title: n,
      "aria-label": n
    },
    ea(n)
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlAvatar tlAvatar--empty" });
}, la = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, aa = ({ controlId: l }) => {
  const t = X(), n = Ge(), a = ae(), r = !!t.hasData, o = t.dataRevision ?? 0, s = t.fileName ?? "download", u = !!t.clearable, [c, i] = e.useState(!1), d = e.useCallback(async () => {
    if (!(!r || c)) {
      i(!0);
      try {
        const g = n + (n.includes("?") ? "&" : "?") + "rev=" + o, T = await fetch(g);
        if (!T.ok) {
          console.error("[TLDownload] Failed to fetch data:", T.status);
          return;
        }
        const y = await T.blob(), E = URL.createObjectURL(y), _ = document.createElement("a");
        _.href = E, _.download = s, _.style.display = "none", document.body.appendChild(_), _.click(), document.body.removeChild(_), URL.revokeObjectURL(E);
      } catch (g) {
        console.error("[TLDownload] Fetch error:", g);
      } finally {
        i(!1);
      }
    }
  }, [r, c, n, o, s]), m = e.useCallback(async () => {
    r && await a("clear");
  }, [r, a]), p = me(la);
  if (!r)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, p["js.download.noFile"]));
  const h = c ? p["js.downloading"] : p["js.download.file"].replace("{0}", s);
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
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: s }, s), u && /* @__PURE__ */ e.createElement(
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
}, ra = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, oa = ({ controlId: l }) => {
  const t = X(), n = Ye(), [a, r] = e.useState("idle"), [o, s] = e.useState(null), [u, c] = e.useState(!1), i = e.useRef(null), d = e.useRef(null), m = e.useRef(null), p = e.useRef(null), h = e.useRef(null), g = t.error, T = e.useMemo(
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
      if (s(null), !T) {
        (A = p.current) == null || A.click();
        return;
      }
      try {
        const D = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        d.current = D, r("overlayOpen");
      } catch (D) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", D), s("js.photoCapture.error.denied"), r("idle");
      }
    }
  }, [a, T]), k = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const A = i.current, D = m.current;
    if (!A || !D)
      return;
    D.width = A.videoWidth, D.height = A.videoHeight;
    const x = D.getContext("2d");
    x && (x.drawImage(A, 0, 0), y(), r("uploading"), D.toBlob(async (N) => {
      if (!N) {
        r("idle");
        return;
      }
      const W = new FormData();
      W.append("photo", N, "capture.jpg"), await n(W), r("idle");
    }, "image/jpeg", 0.85));
  }, [a, n, y]), I = e.useCallback(async (A) => {
    var N;
    const D = (N = A.target.files) == null ? void 0 : N[0];
    if (!D) return;
    r("uploading");
    const x = new FormData();
    x.append("photo", D, D.name), await n(x), r("idle"), p.current && (p.current.value = "");
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
  }, [a]), Fe(a === "overlayOpen", { ESCAPE: E }), e.useEffect(() => () => {
    d.current && (d.current.getTracks().forEach((A) => A.stop()), d.current = null);
  }, []);
  const w = me(ra), v = a === "uploading" ? w["js.uploading"] : w["js.photoCapture.open"], C = ["tlPhotoCapture__cameraBtn"];
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
        className: F.join(" "),
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
  ), o && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, w[o]), g && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, g));
}, sa = {
  "js.photoViewer.alt": "Captured photo"
}, ca = ({ controlId: l }) => {
  const t = X(), n = Ge(), a = !!t.hasPhoto, r = t.dataRevision ?? 0, [o, s] = e.useState(null), u = e.useRef(r);
  e.useEffect(() => {
    if (!a) {
      o && (URL.revokeObjectURL(o), s(null));
      return;
    }
    if (r === u.current && o)
      return;
    u.current = r, o && (URL.revokeObjectURL(o), s(null));
    let i = !1;
    return (async () => {
      try {
        const d = await fetch(n);
        if (!d.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", d.status);
          return;
        }
        const m = await d.blob();
        i || s(URL.createObjectURL(m));
      } catch (d) {
        console.error("[TLPhotoViewer] Fetch error:", d);
      }
    })(), () => {
      i = !0;
    };
  }, [a, r, n]), e.useEffect(() => () => {
    o && URL.revokeObjectURL(o);
  }, []);
  const c = me(sa);
  return !a || !o ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: o,
      alt: t.alt || c["js.photoViewer.alt"]
    }
  ));
}, ia = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, ua = ({ controlId: l }) => {
  const t = X(), n = Ge(), a = !!t.hasPdf, r = t.dataRevision ?? 0, o = me(ia), u = n.indexOf("react-api/"), c = u >= 0 ? n.slice(0, u) : n, i = n + "&rev=" + r, d = c + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(i);
  return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: d,
      title: o["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, o["js.pdfViewer.noDocument"]));
}, { useCallback: rn, useRef: Tt } = e, da = ({ controlId: l }) => {
  const t = X(), n = ae(), a = kt(!0), r = t.orientation, o = t.resizable === !0, s = t.children ?? [], u = r === "horizontal", c = s.length > 0 && s.every((E) => E.collapsed), i = !c && s.some((E) => E.collapsed), d = c ? !u : u, m = Tt(null), p = Tt(null), h = Tt(null), g = rn((E, _) => {
    const k = {
      overflow: E.scrolling || "auto"
    };
    return E.collapsed ? c && !d ? k.flex = "1 0 0%" : k.flex = "0 0 auto" : _ !== void 0 ? k.flex = `0 0 ${_}px` : k.flex = `${E.size} 1 0%`, E.minSize > 0 && !E.collapsed && (k.minWidth = u ? E.minSize : void 0, k.minHeight = u ? void 0 : E.minSize), k;
  }, [u, c, i, d]), T = rn((E, _) => {
    E.preventDefault();
    const k = m.current;
    if (!k) return;
    const I = s[_], w = s[_ + 1], v = k.querySelectorAll(":scope > .tlSplitPanel__child"), C = [];
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
      const N = (u ? A.clientX : A.clientY) - D.startPos, W = D.childBefore.minSize || 0, P = D.childAfter.minSize || 0;
      let j = D.startSizeBefore + N, B = D.startSizeAfter - N;
      j < W && (B += j - W, j = W), B < P && (j += B - P, B = P), h.current[D.splitterIndex] = j, h.current[D.splitterIndex + 1] = B;
      const L = k.querySelectorAll(":scope > .tlSplitPanel__child"), R = L[D.splitterIndex], V = L[D.splitterIndex + 1];
      R && (R.style.flex = `0 0 ${j}px`), V && (V.style.flex = `0 0 ${B}px`);
    }, F = () => {
      if (document.removeEventListener("mousemove", O), document.removeEventListener("mouseup", F), document.body.style.cursor = "", document.body.style.userSelect = "", h.current) {
        const A = {};
        s.forEach((D, x) => {
          const N = D.control;
          N != null && N.controlId && h.current && (A[N.controlId] = h.current[x]);
        }), n("updateSizes", { sizes: A });
      }
      h.current = null, p.current = null;
    };
    document.addEventListener("mousemove", O), document.addEventListener("mouseup", F), document.body.style.cursor = u ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [s, u, n]), y = [];
  return s.forEach((E, _) => {
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
    ), o && _ < s.length - 1) {
      const k = s[_ + 1];
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
}, Ft = ({ image: l, className: t }) => {
  if (!l || l === "none") return null;
  const n = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `${t ? t + " " : ""}${n}`, "aria-hidden": "true" });
}, { useCallback: Rt } = e, ma = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, pa = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), fa = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), ha = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), ba = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), ga = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), Ea = ({ controlId: l }) => {
  const t = X(), n = ae(), a = me(ma), r = t.title, o = t.expansionState ?? "NORMALIZED", s = t.showMinimize === !0, u = t.showMaximize === !0, c = t.showPopOut === !0, i = t.fullLine === !0, d = t.fill === !0, m = t.hoverActions === !0, p = t.appearance === "card", h = t.errorMessage, g = o === "MINIMIZED", T = o === "MAXIMIZED", y = o === "HIDDEN", E = Rt(() => {
    n("toggleMinimize");
  }, [n]), _ = Rt(() => {
    n("toggleMaximize");
  }, [n]), k = Rt(() => {
    n("popOut");
  }, [n]), I = kt(d && !y);
  if (y)
    return null;
  const w = T ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" }, v = s && !T || u && !g || c, C = !!r && r.trim() !== "" || !!t.titleContent || !!t.toolbar || v;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${o.toLowerCase()}${i ? " tlPanel--fullLine" : ""}${I ? " " + I : ""}${m ? " tlPanel--hoverActions" : ""}${p ? " tlPanel--card" : ""}`,
      style: w
    },
    C && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, !!r && r.trim() !== "" && /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, r), t.titleContent && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__titleContent" }, /* @__PURE__ */ e.createElement(G, { control: t.titleContent })), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(G, { control: t.toolbar }), s && !T && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: E,
        title: g ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      g ? /* @__PURE__ */ e.createElement(fa, null) : /* @__PURE__ */ e.createElement(pa, null)
    ), u && !g && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: _,
        title: T ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      T ? /* @__PURE__ */ e.createElement(ba, null) : /* @__PURE__ */ e.createElement(ha, null)
    ), c && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: k,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(ga, null)
    ))),
    !g && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(Ae, null, /* @__PURE__ */ e.createElement(G, { control: t.child }))),
    !g && h && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error tlPanel__error", role: "alert" }, /* @__PURE__ */ e.createElement(Ft, { image: t.errorIcon, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, h)),
    !g && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(G, { control: t.buttonBar }))
  );
}, va = ({ controlId: l }) => {
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
}, _a = ({ controlId: l }) => {
  const t = X(), [n, a] = rt();
  return /* @__PURE__ */ e.createElement(ot, { host: a }, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: n ? "tlDeckPane " + n : "tlDeckPane",
      style: { width: "100%", height: "100%" }
    },
    t.activeChild && /* @__PURE__ */ e.createElement(G, { control: t.activeChild })
  ));
}, { useCallback: ve, useState: Et, useEffect: $t, useRef: _t } = e, Ca = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function Ht(l, t, n, a) {
  const r = [];
  for (const o of l)
    if (o.type === "nav") {
      if (o.hidden) continue;
      r.push({ id: o.id, type: "nav", groupId: a });
    } else o.type === "command" ? r.push({ id: o.id, type: "command", groupId: a }) : o.type === "group" && (r.push({ id: o.id, type: "group" }), (n.get(o.id) ?? o.expanded) && !t && r.push(...Ht(o.children, t, n, o.id)));
  return r;
}
const Ke = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(Se, { encoded: l, className: "tlSidebar__icon" }) : null, ya = ({ item: l, active: t, collapsed: n, onSelect: a, tabIndex: r, itemRef: o, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => a(l.id),
    title: n ? l.label : void 0,
    tabIndex: r,
    ref: o,
    onFocus: () => s(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Ke, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Ke, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), wa = ({ item: l, collapsed: t, onExecute: n, tabIndex: a, itemRef: r, onFocus: o }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: a,
    ref: r,
    onFocus: () => o(l.id)
  },
  /* @__PURE__ */ e.createElement(Ke, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), ka = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Ke, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), Na = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), Sa = ({ item: l, activeItemId: t, anchorRect: n, onSelect: a, onExecute: r, onClose: o }) => {
  const s = _t(null);
  $t(() => {
    const i = (d) => {
      s.current && !s.current.contains(d.target) && setTimeout(() => o(), 0);
    };
    return document.addEventListener("mousedown", i), () => document.removeEventListener("mousedown", i);
  }, [o]), Fe(!0, { ESCAPE: o });
  const u = ve((i) => {
    i.type === "nav" ? (a(i.id), o()) : i.type === "command" && (r(i.id), o());
  }, [a, r, o]), c = {};
  return n && (c.left = n.right, c.top = n.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: s, role: "menu", style: c }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((i) => {
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
        /* @__PURE__ */ e.createElement(Ke, { icon: i.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, i.label),
        i.type === "nav" && i.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, i.badge)
      );
    }
    return i.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: i.id, className: "tlSidebar__flyoutSectionHeader" }, i.label) : i.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: i.id, className: "tlSidebar__separator" }) : null;
  }));
}, Da = ({
  item: l,
  expanded: t,
  activeItemId: n,
  collapsed: a,
  onSelect: r,
  onExecute: o,
  onToggleGroup: s,
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
  const y = _t(null), [E, _] = Et(null), k = ve(() => {
    a ? h === l.id ? T() : (y.current && _(y.current.getBoundingClientRect()), g(l.id)) : s(l.id);
  }, [a, h, l.id, s, g, T]), I = ve((v) => {
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
    Sa,
    {
      item: l,
      activeItemId: n,
      anchorRect: E,
      onSelect: r,
      onExecute: o,
      onClose: T
    }
  ), t && !a && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((v) => /* @__PURE__ */ e.createElement(
    Tn,
    {
      key: v.id,
      item: v,
      activeItemId: n,
      collapsed: a,
      onSelect: r,
      onExecute: o,
      onToggleGroup: s,
      focusedId: d,
      setItemRef: m,
      onItemFocus: p,
      groupStates: null,
      flyoutGroupId: h,
      onOpenFlyout: g,
      onCloseFlyout: T
    }
  ))));
}, Tn = ({
  item: l,
  activeItemId: t,
  collapsed: n,
  onSelect: a,
  onExecute: r,
  onToggleGroup: o,
  focusedId: s,
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
        ya,
        {
          item: l,
          active: l.id === t,
          collapsed: n,
          onSelect: a,
          tabIndex: s === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: c
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        wa,
        {
          item: l,
          collapsed: n,
          onExecute: r,
          tabIndex: s === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: c
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(ka, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(Na, null);
    case "group": {
      const h = i ? i.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        Da,
        {
          item: l,
          expanded: h,
          activeItemId: t,
          collapsed: n,
          onSelect: a,
          onExecute: r,
          onToggleGroup: o,
          tabIndex: s === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: c,
          focusedId: s,
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
}, Ta = ({ controlId: l }) => {
  const t = X(), n = ae(), a = me(Ca), r = t.items ?? [], o = t.activeItemId, s = t.collapsed, u = t.drawerOpen, c = u ? !1 : s, [i, d] = Et(() => {
    const N = /* @__PURE__ */ new Map(), W = (P) => {
      for (const j of P)
        j.type === "group" && (N.set(j.id, j.expanded), W(j.children));
    };
    return W(r), N;
  }), m = ve((N) => {
    d((W) => {
      const P = new Map(W), j = P.get(N) ?? !1;
      return P.set(N, !j), n("toggleGroup", { itemId: N, expanded: !j }), P;
    });
  }, [n]), p = ve((N) => {
    N !== o && n("selectItem", { itemId: N });
  }, [n, o]), h = ve((N) => {
    n("executeCommand", { itemId: N });
  }, [n]), g = ve(() => {
    n("toggleCollapse", {});
  }, [n]), T = ve(() => {
    n("toggleDrawer", {});
  }, [n]), [y, E] = Et(null), _ = ve((N) => {
    E(N);
  }, []), k = ve(() => {
    E(null);
  }, []);
  $t(() => {
    c || E(null);
  }, [c]);
  const [I, w] = Et(() => {
    const N = Ht(r, c, i);
    return N.length > 0 ? N[0].id : "";
  }), v = _t(/* @__PURE__ */ new Map()), C = ve((N) => (W) => {
    W ? v.current.set(N, W) : v.current.delete(N);
  }, []), O = ve((N) => {
    w(N);
  }, []), F = _t(0), A = ve((N) => {
    w(N), F.current++;
  }, []);
  $t(() => {
    const N = v.current.get(I);
    N && document.activeElement !== N && N.focus();
  }, [I, F.current]);
  const D = ve((N) => {
    if (N.key === "Escape" && y !== null) {
      N.preventDefault(), k();
      return;
    }
    const W = Ht(r, c, i);
    if (W.length === 0) return;
    const P = W.findIndex((B) => B.id === I);
    if (P < 0) return;
    const j = W[P];
    switch (N.key) {
      case "ArrowDown": {
        N.preventDefault();
        const B = (P + 1) % W.length;
        A(W[B].id);
        break;
      }
      case "ArrowUp": {
        N.preventDefault();
        const B = (P - 1 + W.length) % W.length;
        A(W[B].id);
        break;
      }
      case "Home": {
        N.preventDefault(), A(W[0].id);
        break;
      }
      case "End": {
        N.preventDefault(), A(W[W.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        N.preventDefault(), j.type === "nav" ? p(j.id) : j.type === "command" ? h(j.id) : j.type === "group" && (c ? y === j.id ? k() : _(j.id) : m(j.id));
        break;
      }
      case "ArrowRight": {
        j.type === "group" && !c && ((i.get(j.id) ?? !1) || (N.preventDefault(), m(j.id)));
        break;
      }
      case "ArrowLeft": {
        j.type === "group" && !c && (i.get(j.id) ?? !1) && (N.preventDefault(), m(j.id));
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
  ]), x = "tlSidebar" + (c ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: x }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(G, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: T, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": a["js.sidebar.ariaLabel"] }, c ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(G, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(G, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: D }, r.map((N) => /* @__PURE__ */ e.createElement(
    Tn,
    {
      key: N.id,
      item: N,
      activeItemId: o,
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
}, Ra = ({ controlId: l }) => {
  const t = X(), n = t.direction ?? "column", a = t.gap ?? "default", r = t.align ?? "stretch", o = t.wrap === !0, s = t.growFirst === !0, u = t.children ?? [], [c, i] = rt(), d = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${a}`,
    `tlStack--align-${r}`,
    o ? "tlStack--wrap" : "",
    s ? "tlStack--grow-first" : "",
    c,
    t.cssClass ?? ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(ot, { host: i }, /* @__PURE__ */ e.createElement("div", { id: l, className: d }, u.map((m, p) => /* @__PURE__ */ e.createElement(G, { key: p, control: m }))));
}, La = ({ controlId: l }) => {
  const t = X(), [n, a] = rt();
  return /* @__PURE__ */ e.createElement(ot, { host: a }, /* @__PURE__ */ e.createElement("div", { id: l, className: n ? "tlInset " + n : "tlInset" }, t.child && /* @__PURE__ */ e.createElement(G, { control: t.child })));
}, xa = ({ controlId: l }) => {
  const t = X(), n = t.columns, a = t.minColumnWidth, r = t.gap ?? "default", o = t.children ?? [], s = {};
  return a ? s.gridTemplateColumns = `repeat(auto-fit, minmax(min(${a}, 100%), 1fr))` : n && (s.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${r}`, style: s }, o.map((u, c) => /* @__PURE__ */ e.createElement(G, { key: c, control: u })));
}, Ma = ({ controlId: l }) => {
  const t = X(), n = t.title, a = t.variant ?? "outlined", r = t.padding ?? "default", o = t.headerActions ?? [], s = t.child, u = n != null || o.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${a}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), o.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, o.map((c, i) => /* @__PURE__ */ e.createElement(G, { key: i, control: c })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${r}` }, /* @__PURE__ */ e.createElement(Ae, null, /* @__PURE__ */ e.createElement(G, { control: s }))));
}, Ia = ({ controlId: l }) => {
  const t = X(), n = t.title ?? "", a = t.leading, r = t.trailing, o = t.children ?? [], s = t.actions ?? [], u = t.variant ?? "flat", i = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    u === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: i }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(G, { control: a })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), o.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, o.map((d, m) => /* @__PURE__ */ e.createElement(G, { key: m, control: d }))), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((d, m) => /* @__PURE__ */ e.createElement(G, { key: m, control: d }))), r && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__trailing" }, /* @__PURE__ */ e.createElement(G, { control: r })));
}, { useCallback: ja } = e, Aa = ({ controlId: l }) => {
  const t = X(), n = ae(), a = t.items ?? [], r = ja((o) => {
    n("navigate", { itemId: o });
  }, [n]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, a.map((o, s) => {
    const u = s === a.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: o.id, className: "tlBreadcrumb__entry" }, s > 0 && /* @__PURE__ */ e.createElement(
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
    ), u ? /* @__PURE__ */ e.createElement("span", { className: "tlBreadcrumb__current", "aria-current": "page" }, o.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlBreadcrumb__item",
        onClick: () => r(o.id)
      },
      o.label
    ));
  })));
}, { useCallback: Pa } = e, Ba = ({ controlId: l }) => {
  const t = X(), n = ae(), a = t.items ?? [], r = t.activeItemId, o = Pa((s) => {
    s !== r && n("selectItem", { itemId: s });
  }, [n, r]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, a.map((s) => {
    const u = s.id === r;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: s.id,
        type: "button",
        className: "tlBottomBar__item" + (u ? " tlBottomBar__item--active" : ""),
        onClick: () => o(s.id),
        "aria-current": u ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + s.icon, "aria-hidden": "true" }), s.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, s.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, s.label)
    );
  }));
}, { useCallback: on, useRef: Oa } = e, Fa = ({ onClose: l }) => (pe("ESCAPE", () => (l(), !0)), null), $a = ({ controlId: l }) => {
  const t = X(), n = ae(), a = t.open === !0, r = t.closeOnBackdrop !== !1, o = t.child, s = Oa(null), u = on(() => {
    n("close");
  }, [n]), c = on((i) => {
    r && i.target === i.currentTarget && u();
  }, [r, u]);
  return a ? /* @__PURE__ */ e.createElement(Yt, null, /* @__PURE__ */ e.createElement(Fa, { onClose: u }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: c,
      ref: s,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(Ae, null, /* @__PURE__ */ e.createElement(G, { control: o }))
  )) : null;
}, { useEffect: Ha, useRef: Wa } = e, Ua = ({ controlId: l }) => {
  const n = X().dialogs ?? [], a = Wa(n.length);
  return Ha(() => {
    n.length < a.current && n.length > 0, a.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((r) => /* @__PURE__ */ e.createElement(G, { key: r.controlId, control: r })));
}, { useCallback: ut, useRef: He, useState: dt } = e, Va = ({ onClose: l }) => (pe("ESCAPE", () => (l(), !0)), null), za = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Ka = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Ya = ({ controlId: l }) => {
  const t = X(), n = ae(), a = me(za), r = t.title ?? "", o = t.width ?? "32rem", s = t.height ?? null, u = t.minHeight ?? null, c = t.resizable === !0, i = t.child, d = t.actions ?? [], m = t.toolbar, p = t.buttonBar, [h, g] = dt(null), [T, y] = dt(null), [E, _] = dt(null), k = He(null), [I, w] = dt(!1), v = He(null), C = He(null), O = He(null), F = He(null), A = He(null), D = ut(() => {
    n("close");
  }, [n]);
  Gt(!0, F, "field");
  const x = ut((B, L) => {
    L.preventDefault();
    const R = F.current;
    if (!R) return;
    const V = R.getBoundingClientRect(), f = !k.current, M = k.current ?? { x: V.left, y: V.top };
    f && (k.current = M, _(M)), A.current = {
      dir: B,
      startX: L.clientX,
      startY: L.clientY,
      startW: V.width,
      startH: V.height,
      startPos: { ...M },
      symmetric: f
    };
    const Y = (Q) => {
      const U = A.current;
      if (!U) return;
      const ee = Q.clientX - U.startX, se = Q.clientY - U.startY;
      let ne = U.startW, be = U.startH, _e = 0, ke = 0;
      U.symmetric ? (U.dir.includes("e") && (ne = U.startW + 2 * ee), U.dir.includes("w") && (ne = U.startW - 2 * ee), U.dir.includes("s") && (be = U.startH + 2 * se), U.dir.includes("n") && (be = U.startH - 2 * se)) : (U.dir.includes("e") && (ne = U.startW + ee), U.dir.includes("w") && (ne = U.startW - ee, _e = ee), U.dir.includes("s") && (be = U.startH + se), U.dir.includes("n") && (be = U.startH - se, ke = se));
      const ye = Math.max(200, ne), Re = Math.max(100, be);
      U.symmetric ? (_e = (U.startW - ye) / 2, ke = (U.startH - Re) / 2) : (U.dir.includes("w") && ye === 200 && (_e = U.startW - 200), U.dir.includes("n") && Re === 100 && (ke = U.startH - 100)), C.current = ye, O.current = Re, g(ye), y(Re);
      const $e = {
        x: U.startPos.x + _e,
        y: U.startPos.y + ke
      };
      k.current = $e, _($e);
    }, z = () => {
      document.removeEventListener("mousemove", Y), document.removeEventListener("mouseup", z);
      const Q = C.current, U = O.current;
      (Q != null || U != null) && n("resize", {
        ...Q != null ? { width: Math.round(Q) } : {},
        ...U != null ? { height: Math.round(U) } : {}
      }), A.current = null;
    };
    document.addEventListener("mousemove", Y), document.addEventListener("mouseup", z);
  }, [n]), N = ut((B) => {
    if (B.button !== 0 || B.target.closest("button")) return;
    B.preventDefault();
    const L = F.current;
    if (!L) return;
    const R = L.getBoundingClientRect(), V = k.current ?? { x: R.left, y: R.top }, f = B.clientX - V.x, M = B.clientY - V.y, Y = (Q) => {
      const U = window.innerWidth, ee = window.innerHeight;
      let se = Q.clientX - f, ne = Q.clientY - M;
      const be = L.offsetWidth, _e = L.offsetHeight;
      se + be > U && (se = U - be), ne + _e > ee && (ne = ee - _e), se < 0 && (se = 0), ne < 0 && (ne = 0);
      const ke = { x: se, y: ne };
      k.current = ke, _(ke);
    }, z = () => {
      document.removeEventListener("mousemove", Y), document.removeEventListener("mouseup", z);
    };
    document.addEventListener("mousemove", Y), document.addEventListener("mouseup", z);
  }, []), W = ut(() => {
    var B, L;
    if (I) {
      const R = v.current;
      R && (_(R.x !== -1 ? { x: R.x, y: R.y } : null), g(R.w), y(R.h)), w(!1);
    } else {
      const R = F.current, V = R == null ? void 0 : R.getBoundingClientRect();
      v.current = {
        x: ((B = k.current) == null ? void 0 : B.x) ?? (V == null ? void 0 : V.left) ?? -1,
        y: ((L = k.current) == null ? void 0 : L.y) ?? (V == null ? void 0 : V.top) ?? -1,
        w: h ?? (V == null ? void 0 : V.width) ?? null,
        h: T ?? null
      }, w(!0), _({ x: 0, y: 0 }), g(null), y(null);
    }
  }, [I, h, T]), P = I ? { position: "absolute", top: 0, left: 0, width: "100vw", maxWidth: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: h != null ? h + "px" : o,
    ...T != null ? { height: T + "px" } : s != null ? { height: s } : {},
    ...u != null && T == null ? { minHeight: u } : {},
    maxHeight: E ? "100vh" : "80vh",
    ...E ? { position: "absolute", left: E.x + "px", top: E.y + "px" } : {}
  }, j = l + "-title";
  return /* @__PURE__ */ e.createElement(Yt, { modal: !0 }, /* @__PURE__ */ e.createElement(Va, { onClose: D }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: P,
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
    c && !I && Ka.map((B) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: B,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${B}`,
        onMouseDown: (L) => x(B, L)
      }
    ))
  ));
}, { useCallback: Ga } = e, Xa = {
  "js.drawer.close": "Close"
}, qa = ({ controlId: l }) => {
  const t = X(), n = ae(), a = me(Xa), r = t.open === !0, o = t.position ?? "right", s = t.size ?? "medium", u = t.title ?? null, c = t.child, i = Ga(() => {
    n("close");
  }, [n]);
  Fe(r, { ESCAPE: i });
  const d = [
    "tlDrawer",
    `tlDrawer--${o}`,
    `tlDrawer--${s}`,
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
}, { useCallback: mt, useRef: Za } = e, Qa = ({ controlId: l }) => {
  const t = X(), n = ae(), a = Za(null), r = t.child, s = (t.trigger ?? "contextmenu") === "click", u = mt((m) => {
    m.preventDefault(), m.stopPropagation(), n("openMenu", { x: m.clientX, y: m.clientY });
  }, [n]), c = mt(() => {
    var p;
    const m = (p = a.current) == null ? void 0 : p.getBoundingClientRect();
    n("openMenu", {
      x: Math.round(m ? m.left : 0),
      y: Math.round(m ? m.bottom : 0)
    });
  }, [n]), i = mt((m) => {
    m.preventDefault(), m.stopPropagation(), c();
  }, [c]), d = mt((m) => {
    (m.key === "Enter" || m.key === " ") && (m.preventDefault(), c());
  }, [c]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenuRegion" + (s ? " tlMenuRegion--click" : ""),
      ref: a,
      onContextMenu: s ? void 0 : u,
      onClick: s ? i : void 0,
      role: s ? "button" : void 0,
      tabIndex: s ? 0 : void 0,
      "aria-haspopup": s ? "menu" : void 0,
      onKeyDown: s ? d : void 0
    },
    r && /* @__PURE__ */ e.createElement(G, { control: r })
  );
}, { useCallback: Ja, useEffect: sn, useRef: er, useState: cn } = e, tr = 250, nr = ({ controlId: l }) => {
  const t = X(), n = ae(), a = t.message ?? "", r = t.content ?? "", o = t.variant ?? "info", s = t.duration ?? 5e3, u = t.visible === !0, c = t.generation ?? 0, [i, d] = cn(!1), [m, p] = cn(!1), h = er(!1);
  sn(() => {
    h.current = !1;
  }, [c]);
  const g = Ja(() => {
    d(!0), setTimeout(() => {
      n("dismiss", { generation: c }), d(!1);
    }, 200);
  }, [n, c]);
  return sn(() => {
    if (!u || s === 0 || m) return;
    const T = setTimeout(g, h.current ? tr : s);
    return () => clearTimeout(T);
  }, [u, s, m, g]), !u && !i ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${o}${i ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite",
      onMouseEnter: () => {
        h.current = !0, p(!0);
      },
      onMouseLeave: () => p(!1)
    },
    r ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: r } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, a)
  );
}, { useCallback: lr, useEffect: un, useMemo: ar, useRef: rr, useState: or } = e, sr = 1e3;
function cr(l) {
  const t = Math.max(0, Math.floor(l / 1e3)), n = t % 60, a = Math.floor(t / 60) % 60, r = Math.floor(t / 3600), o = (s) => s < 10 ? `0${s}` : `${s}`;
  return r > 0 ? `${r}:${o(a)}:${o(n)}` : `${a}:${o(n)}`;
}
const ir = ({ controlId: l }) => {
  const t = X(), n = ae(), a = t.visible === !0, r = t.severity ?? "info", o = t.text ?? "", s = t.deadline ?? null, u = t.serverNow ?? null, c = t.leadMs ?? null, i = t.actionLabel ?? null, d = t.pingGraceMs ?? null, m = ar(
    () => u != null ? u - Date.now() : 0,
    [u]
  ), [p, h] = or(0), g = a && s != null;
  un(() => {
    if (!g) return;
    const I = setInterval(() => h((w) => w + 1), sr);
    return () => clearInterval(I);
  }, [g, s]);
  const T = rr(null);
  un(() => {
    !g || d == null || s == null || T.current !== s && (Date.now() + m < s + d || (T.current = s, n("deadlinePassed", {})));
  }, [p, g, s, d, m, n]);
  const y = lr(() => {
    i != null && n("action", {});
  }, [n, i]);
  if (!a) return null;
  const E = s != null ? s - (Date.now() + m) : null;
  if (c != null && E != null && E > c) return null;
  const _ = E != null ? cr(E) : null, k = i != null;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlNoticeBar tlNoticeBar--${r}${k ? " tlNoticeBar--clickable" : ""}`,
      role: k ? "button" : "status",
      "aria-live": "polite",
      tabIndex: k ? 0 : void 0,
      title: i ?? void 0,
      "aria-label": k ? `${o} ${i}` : void 0,
      onClick: k ? y : void 0,
      onKeyDown: k ? (I) => {
        (I.key === "Enter" || I.key === " ") && (I.preventDefault(), y());
      } : void 0
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__text" }, o),
    _ !== null && /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__countdown" }, _)
  );
}, { useCallback: Lt, useEffect: dn, useRef: ur, useState: mn } = e, dr = ({ controlId: l }) => {
  const t = X(), n = ae(), a = t.open === !0, r = t.anchorId, o = t.anchorX, s = t.anchorY, u = t.items ?? [], c = ur(null), [i, d] = mn({ top: 0, left: 0 }), [m, p] = mn(0), h = u.filter((E) => E.type === "item" && !E.disabled);
  dn(() => {
    var C, O;
    if (!a) return;
    const E = ((C = c.current) == null ? void 0 : C.offsetHeight) ?? 200, _ = ((O = c.current) == null ? void 0 : O.offsetWidth) ?? 200;
    if (o != null && s != null) {
      let F = s, A = o;
      F + E > window.innerHeight && (F = Math.max(0, window.innerHeight - E)), A + _ > window.innerWidth && (A = Math.max(0, window.innerWidth - _)), d({ top: F, left: A }), p(0);
      return;
    }
    if (!r) return;
    const k = document.getElementById(r);
    if (!k) return;
    const I = k.getBoundingClientRect();
    let w = I.bottom + 4, v = I.left;
    w + E > window.innerHeight && (w = I.top - E - 4), v + _ > window.innerWidth && (v = I.right - _), d({ top: w, left: v }), p(0);
  }, [a, r, o, s]);
  const g = Lt(() => {
    n("close");
  }, [n]), T = Lt((E) => {
    n("selectItem", { itemId: E });
  }, [n]);
  dn(() => {
    if (!a) return;
    const E = (_) => {
      c.current && !c.current.contains(_.target) && g();
    };
    return document.addEventListener("mousedown", E), () => document.removeEventListener("mousedown", E);
  }, [a, g]);
  const y = Lt((E) => {
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
  return Gt(a, c), a ? /* @__PURE__ */ e.createElement(
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
        E.icon && /* @__PURE__ */ e.createElement(Se, { encoded: E.icon, className: "tlMenu__icon" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, E.label)
      );
    })
  ) : null;
}, mr = 768, pr = ({ controlId: l }) => {
  const t = X(), n = ae(), a = kt(!0);
  e.useEffect(() => {
    const i = window.matchMedia(`(max-width: ${mr}px)`), d = (p) => {
      n("reportDisplayClass", { displayClass: p ? "COMPACT" : "REGULAR" });
    };
    d(i.matches);
    const m = (p) => d(p.matches);
    return i.addEventListener("change", m), () => i.removeEventListener("change", m);
  }, [n]);
  const r = t.header, o = t.notices, s = t.content, u = t.footer, c = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell " + a }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(G, { control: r })), o && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__notices" }, /* @__PURE__ */ e.createElement(G, { control: o })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(Ae, null, /* @__PURE__ */ e.createElement(G, { control: s }))), u && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(G, { control: u })), /* @__PURE__ */ e.createElement(G, { control: c }));
}, fr = ({ controlId: l }) => {
  const t = X(), n = t.text ?? "", a = t.cssClass ?? "", r = t.hasTooltip === !0, o = t.role || void 0, s = a ? `tlText ${a}` : "tlText";
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: s,
      role: o,
      "data-tooltip": r ? "key:tooltip" : void 0
    },
    n
  );
}, hr = ({ isMulti: l, cursorIndex: t, onMove: n, onToggle: a, onSelectAll: r, onActivate: o }) => (pe("ArrowUp", () => (n("up", !1, !1), !0)), pe("ArrowDown", () => (n("down", !1, !1), !0)), pe("Home", () => (n("home", !1, !1), !0)), pe("End", () => (n("end", !1, !1), !0)), pe("PageUp", () => (n("pageUp", !1, !1), !0)), pe("PageDown", () => (n("pageDown", !1, !1), !0)), pe("Shift+ArrowUp", () => (n("up", l, !1), !0)), pe("Shift+ArrowDown", () => (n("down", l, !1), !0)), pe("Shift+Home", () => (n("home", l, !1), !0)), pe("Shift+End", () => (n("end", l, !1), !0)), pe("Shift+PageUp", () => (n("pageUp", l, !1), !0)), pe("Shift+PageDown", () => (n("pageDown", l, !1), !0)), pe("Ctrl+ArrowUp", () => (n("up", !1, l), !0)), pe("Ctrl+ArrowDown", () => (n("down", !1, l), !0)), pe("Space", () => t < 0 ? !1 : (a(), !0)), pe("Ctrl+A", () => l ? (r(), !0) : !1), pe("Enter", () => o()), null), br = {
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
}, gr = 300, pn = 50, Er = 'input, textarea, select, button, a, [contenteditable="true"], [role="combobox"], [role="listbox"], [role="option"], [role="button"], [role="link"], [role="checkbox"], [role="radio"], [role="switch"], [role="textbox"], [role="spinbutton"], [role="slider"], [role="menu"], [role="menuitem"]';
function pt(l) {
  var n;
  const t = l.target;
  return !!((n = t == null ? void 0 : t.closest) != null && n.call(t, Er));
}
const Wt = 'input:not([disabled]):not([readonly]), textarea:not([disabled]):not([readonly]), select:not([disabled]), [contenteditable="true"]', fn = Wt + ", button:not([disabled]), a[href]";
function Rn(l, t) {
  return Array.from(l.querySelectorAll("[data-row][data-col]")).filter((n) => n.dataset.row === t);
}
function xt(l, t, n = {}) {
  const a = Rn(l, t);
  if (n.col) {
    const o = a.find((u) => u.dataset.col === n.col), s = o == null ? void 0 : o.querySelector(Wt);
    if (s) return s;
  }
  if (n.col)
    return null;
  const r = n.last ? [...a].reverse() : a;
  for (const o of r) {
    const s = o.querySelector(Wt);
    if (s) return s;
  }
  return null;
}
const vr = ({ controlId: l }) => {
  var Qt, Jt;
  const t = X(), n = ae(), a = me(br), r = e.useRef(null);
  e.useEffect(() => {
    const b = r.current;
    if (!b) return;
    const S = ($) => {
      const Z = $.detail;
      let J = Z.target;
      for (; J && J !== b; ) {
        const re = J.dataset.row, ce = J.dataset.col;
        if (re != null && ce != null) {
          Z.resolved = { key: re + "|" + ce };
          return;
        }
        J = J.parentElement;
      }
    };
    return b.addEventListener("tl-tooltip-resolve", S), () => b.removeEventListener("tl-tooltip-resolve", S);
  }, []);
  const o = t.columns ?? [], s = t.totalRowCount ?? 0, u = t.rows ?? [], c = t.rowHeight ?? 36, i = t.selectionMode ?? "single", d = t.selectedCount ?? 0, m = t.cursorIndex ?? -1, p = t.frozenColumnCount ?? 0, h = t.treeMode ?? !1, g = t.grouping ?? "", T = t.columnSelect ?? !1, y = t.filterBar ?? !1, E = t.namedFilters ?? [], _ = t.activeNamedFilter ?? "", k = t.search ?? "", I = t.filterSaving ?? !1, w = e.useMemo(
    () => o.filter((b) => b.sortPriority && b.sortPriority > 0).length,
    [o]
  ), v = i === "multi", C = 40, O = 20, F = e.useRef(null), A = e.useRef(null), D = e.useRef(null), x = e.useRef(null), N = e.useRef(null), [W, P] = e.useState({}), j = e.useRef(null), B = e.useRef(!1), L = e.useRef(null), [R, V] = e.useState(null), [f, M] = e.useState(null), [Y, z] = e.useState(null), [Q, U] = e.useState(0);
  e.useEffect(() => {
    const b = D.current;
    if (!b)
      return;
    const S = () => {
      const Z = b.offsetWidth - b.clientWidth;
      U((J) => J === Z ? J : Z);
    };
    S();
    const $ = new ResizeObserver(S);
    return $.observe(b), () => $.disconnect();
  }, []), e.useEffect(() => {
    j.current || P({});
  }, [o]);
  const ee = e.useCallback((b) => W[b.name] ?? b.width, [W]), se = e.useMemo(() => {
    const b = [];
    let S = v && p > 0 ? C : 0;
    for (let $ = 0; $ < p && $ < o.length; $++)
      b.push(S), S += ee(o[$]);
    return b;
  }, [o, p, v, C, ee]), ne = e.useMemo(
    () => o.reduce((b, S, $) => S.pinnedEnd ? b : $, -1),
    [o]
  ), be = e.useMemo(() => {
    const b = o.map(() => 0);
    let S = 0;
    for (let $ = o.length - 1; $ >= 0; $--)
      o[$].pinnedEnd && (b[$] = S, S += ee(o[$]));
    return b;
  }, [o, ee]), _e = e.useMemo(() => {
    if (p <= 0)
      return 0;
    let b = v ? C : 0;
    for (let S = 0; S < p && S < o.length; S++)
      b += ee(o[S]);
    return b;
  }, [o, p, v, C, ee]), ke = s * c, ye = e.useRef(null), Re = e.useCallback((b, S, $) => {
    $.preventDefault(), $.stopPropagation(), j.current = { column: b, startX: $.clientX, startWidth: S };
    let Z = $.clientX, J = 0;
    const re = () => {
      const ie = j.current;
      if (!ie) return;
      const de = Math.max(pn, ie.startWidth + (Z - ie.startX) + J);
      P((ge) => ({ ...ge, [ie.column]: de }));
    }, ce = () => {
      const ie = D.current, de = F.current;
      if (!ie || !j.current) return;
      const ge = ie.getBoundingClientRect(), xe = 40, en = 8, il = ie.scrollLeft;
      Z > ge.right - xe ? ie.scrollLeft += en : Z < ge.left + xe && (ie.scrollLeft = Math.max(0, ie.scrollLeft - en));
      const tn = ie.scrollLeft - il;
      tn !== 0 && (de && (de.scrollLeft = ie.scrollLeft), J += tn, re()), ye.current = requestAnimationFrame(ce);
    };
    ye.current = requestAnimationFrame(ce);
    const oe = (ie) => {
      Z = ie.clientX, re();
    }, fe = (ie) => {
      document.removeEventListener("mousemove", oe), document.removeEventListener("mouseup", fe), ye.current !== null && (cancelAnimationFrame(ye.current), ye.current = null);
      const de = j.current;
      if (de) {
        const ge = Math.max(pn, de.startWidth + (ie.clientX - de.startX) + J);
        n("columnResize", { column: de.column, width: ge }), j.current = null, B.current = !0, requestAnimationFrame(() => {
          B.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", oe), document.addEventListener("mouseup", fe);
  }, [n]), $e = e.useCallback(() => {
    F.current && D.current && (F.current.scrollLeft = D.current.scrollLeft), x.current !== null && clearTimeout(x.current), x.current = window.setTimeout(() => {
      const b = D.current;
      if (!b) return;
      const S = b.scrollTop, $ = Math.ceil(b.clientHeight / c), Z = Math.floor(S / c);
      n("scroll", { start: Z, count: $ });
    }, 80);
  }, [n, c]), st = e.useCallback((b, S, $) => {
    if (B.current) return;
    let Z;
    !S || S === "desc" ? Z = "asc" : Z = "desc";
    const J = $.shiftKey ? "add" : "replace";
    n("sort", { column: b, direction: Z, mode: J });
  }, [n]), H = e.useCallback((b, S) => {
    L.current = b, S.dataTransfer.effectAllowed = "move", S.dataTransfer.setData("text/plain", b);
  }, []), q = e.useCallback((b, S) => {
    var J;
    if (!L.current || L.current === b || (J = o.find((re) => re.name === b)) != null && J.pinnedEnd) {
      V(null);
      return;
    }
    S.preventDefault(), S.dataTransfer.dropEffect = "move";
    const $ = S.currentTarget.getBoundingClientRect(), Z = S.clientX < $.left + $.width / 2 ? "left" : "right";
    V({ column: b, side: Z });
  }, [o]), le = e.useCallback((b) => {
    b.preventDefault(), b.stopPropagation();
    const S = L.current;
    if (!S || !R) {
      L.current = null, V(null);
      return;
    }
    let $ = o.findIndex((J) => J.name === R.column);
    if ($ < 0) {
      L.current = null, V(null);
      return;
    }
    const Z = o.findIndex((J) => J.name === S);
    R.side === "right" && $++, Z < $ && $--, n("columnReorder", { column: S, targetIndex: $ }), L.current = null, V(null);
  }, [o, R, n]), ue = e.useCallback(() => {
    L.current = null, V(null);
  }, []), Xe = e.useCallback((b, S) => {
    var J, re, ce, oe;
    const $ = window.getSelection();
    if ($ && !$.isCollapsed && S.currentTarget.contains($.anchorNode))
      return;
    if (!pt(S) && ((J = D.current) == null || J.focus({ preventScroll: !0 }), !S.ctrlKey && !S.metaKey && !S.shiftKey)) {
      const fe = (oe = (ce = (re = S.target) == null ? void 0 : re.closest) == null ? void 0 : ce.call(re, "[data-col]")) == null ? void 0 : oe.getAttribute("data-col");
      N.current = { index: b, col: fe ?? void 0 };
    }
    const Z = u.find((fe) => fe.index === b);
    pt(S) && (Z != null && Z.selected) && !S.ctrlKey && !S.metaKey && !S.shiftKey || n("select", {
      rowIndex: b,
      ctrlKey: S.ctrlKey || S.metaKey,
      shiftKey: S.shiftKey
    });
  }, [n, u]), Bn = e.useCallback((b, S) => {
    var $;
    pt(S) || (($ = u.find((Z) => Z.index === b)) == null ? void 0 : $.groupCount) == null && n("activate", { rowIndex: b });
  }, [n, u]), On = e.useCallback((b, S, $) => {
    n("moveSelection", { direction: b, extend: S, move: $ });
  }, [n]), Fn = e.useCallback(() => {
    m < 0 || n("select", { rowIndex: m, ctrlKey: v, shiftKey: !1 });
  }, [n, m, v]), $n = e.useCallback(() => {
    n("selectAll", { selected: !0 });
  }, [n]), Hn = e.useCallback(() => {
    var S;
    if (m < 0)
      return !1;
    const b = document.activeElement;
    return (S = b == null ? void 0 : b.closest) != null && S.call(b, fn) ? !1 : (n("activate", { rowIndex: m }), !0);
  }, [n, m]), Wn = e.useCallback(
    () => !!r.current && r.current.contains(document.activeElement),
    []
  );
  e.useEffect(() => {
    if (m < 0)
      return;
    const b = D.current;
    if (!b)
      return;
    const S = m * c, $ = S + c;
    S < b.scrollTop ? b.scrollTop = S : $ > b.scrollTop + b.clientHeight && (b.scrollTop = $ - b.clientHeight);
  }, [m, c]), e.useEffect(() => {
    const b = N.current, S = D.current;
    if (!b || !S)
      return;
    const $ = u.find((re) => re.index === b.index);
    if (!$ || !xt(S, $.id))
      return;
    N.current = null;
    const Z = document.activeElement;
    if (Z && Z !== document.body && !S.contains(Z))
      return;
    const J = xt(S, $.id, { col: b.col, last: b.last });
    J && (J.focus({ preventScroll: !0 }), J instanceof HTMLInputElement && J.select());
  }, [u]);
  const Un = e.useCallback((b) => {
    if (b.key !== "Tab")
      return;
    const S = D.current, $ = document.activeElement;
    if (!S || !$ || !S.contains($))
      return;
    const Z = $.closest("[data-row][data-col]");
    if (!Z)
      return;
    const J = Z.dataset.row, re = u.find((xe) => xe.id === J);
    if (!re)
      return;
    const ce = Rn(S, J).flatMap((xe) => Array.from(xe.querySelectorAll(fn))), oe = ce.indexOf($);
    if (oe < 0)
      return;
    const fe = !b.shiftKey;
    if (!(fe ? oe === ce.length - 1 : oe === 0))
      return;
    const de = fe ? re.index + 1 : re.index - 1;
    if (de < 0 || de >= s)
      return;
    const ge = u.find((xe) => xe.index === de);
    ge && xt(S, ge.id) || (b.preventDefault(), N.current = { index: de, last: !fe }, n("select", { rowIndex: de, ctrlKey: !1, shiftKey: !1 }));
  }, [u, s, n]), Vn = e.useCallback((b, S) => {
    S.stopPropagation(), n("select", { rowIndex: b, ctrlKey: !0, shiftKey: !1 });
  }, [n]), zn = e.useCallback(() => {
    const b = d === s && s > 0;
    n("selectAll", { selected: !b });
  }, [n, d, s]), Kn = e.useCallback((b, S, $) => {
    $.stopPropagation(), n("expand", { rowIndex: b, expanded: S });
  }, [n]), Yn = e.useCallback((b, S) => {
    S.preventDefault(), M({ x: S.clientX, y: S.clientY, colIdx: b });
  }, []), Gn = e.useCallback(() => {
    f && (n("setFrozenColumnCount", { count: f.colIdx + 1 }), M(null));
  }, [f, n]), Xn = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), M(null);
  }, [n]), qn = e.useCallback((b) => {
    n("group", { column: b }), M(null);
  }, [n]), Zn = e.useCallback(() => {
    n("group", { column: "" }), M(null);
  }, [n]), Qn = e.useCallback((b) => {
    b.preventDefault(), b.stopPropagation();
    const S = A.current, $ = F.current;
    if (!S || !$)
      return;
    const Z = S.clientWidth, J = [{ x: 0, count: 0 }];
    $.querySelectorAll("[data-col-idx]").forEach((fe) => {
      var ge;
      const ie = Number(fe.dataset.colIdx);
      if ((ge = o[ie]) != null && ge.pinnedEnd)
        return;
      const de = fe.getBoundingClientRect().right - S.getBoundingClientRect().left;
      de > 0 && de <= Z && J.push({ x: de, count: ie + 1 });
    });
    let re = { x: _e, count: p };
    const ce = (fe) => {
      const ie = fe.clientX - S.getBoundingClientRect().left;
      re = J.reduce(
        (de, ge) => Math.abs(ge.x - ie) < Math.abs(de.x - ie) ? ge : de,
        J[0]
      ), z(re);
    }, oe = () => {
      document.removeEventListener("mousemove", ce), document.removeEventListener("mouseup", oe), z(null), re.count !== p && n("setFrozenColumnCount", { count: re.count });
    };
    document.addEventListener("mousemove", ce), document.addEventListener("mouseup", oe);
  }, [o, _e, p, n]);
  e.useEffect(() => {
    if (!f) return;
    const b = () => M(null);
    return document.addEventListener("mousedown", b), () => document.removeEventListener("mousedown", b);
  }, [f]), Fe(!!f, { ESCAPE: () => M(null) });
  const Jn = e.useCallback((b, S) => {
    S.stopPropagation(), S.preventDefault(), n("openFilter", { column: b });
  }, [n]), el = e.useCallback((b) => {
    b.stopPropagation(), b.preventDefault(), n("openColumnSelect", {});
  }, [n]), [tl, qt] = e.useState(k), Nt = e.useRef(!1), Le = e.useRef(null);
  e.useEffect(() => {
    Nt.current || qt(k);
  }, [k]), e.useEffect(() => () => {
    Le.current !== null && clearTimeout(Le.current);
  }, []);
  const ct = e.useCallback((b) => {
    Le.current !== null && (clearTimeout(Le.current), Le.current = null), Nt.current = !1, n("search", { term: b });
  }, [n]), nl = e.useCallback((b) => {
    qt(b), Nt.current = !0, Le.current !== null && clearTimeout(Le.current), Le.current = window.setTimeout(() => ct(b), gr);
  }, [ct]), ll = e.useCallback((b) => {
    b.key === "Enter" && (b.preventDefault(), ct(b.currentTarget.value));
  }, [ct]), al = e.useCallback((b) => {
    b === _ ? n("clearFilter", {}) : n("applyNamedFilter", { id: b });
  }, [_, n]), rl = e.useCallback((b, S) => {
    S.stopPropagation(), n("deleteNamedFilter", { id: b });
  }, [n]), [qe, Ze] = e.useState(null), St = e.useCallback(() => {
    const b = (qe ?? "").trim();
    b && (n("saveNamedFilter", { filterName: b }), Ze(null));
  }, [qe, n]), ol = e.useCallback((b) => {
    b.key === "Enter" ? (b.preventDefault(), St()) : b.key === "Escape" && (b.preventDefault(), Ze(null));
  }, [St]), Dt = o.reduce((b, S) => b + ee(S), 0) + (v ? C : 0), Qe = T ? 32 : 0, sl = d === s && s > 0, Zt = d > 0 && d < s, cl = e.useCallback((b) => {
    b && (b.indeterminate = Zt);
  }, [Zt]);
  return /* @__PURE__ */ e.createElement(Yt, { active: Wn }, /* @__PURE__ */ e.createElement(
    hr,
    {
      isMulti: v,
      cursorIndex: m,
      onMove: On,
      onToggle: Fn,
      onSelectAll: $n,
      onActivate: Hn
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
        const S = D.current, $ = F.current;
        if (!S) return;
        const Z = S.getBoundingClientRect(), J = 40, re = 8;
        b.clientX < Z.left + J ? S.scrollLeft = Math.max(0, S.scrollLeft - re) : b.clientX > Z.right - J && (S.scrollLeft += re), $ && ($.scrollLeft = S.scrollLeft);
      },
      onDrop: le
    },
    y && /* @__PURE__ */ e.createElement("div", { className: "tlTableView__filterBar" }, E.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlTableView__filterChips" }, E.map((b) => {
      const S = b.id === _;
      return /* @__PURE__ */ e.createElement(
        "span",
        {
          key: b.id,
          className: "tlTableView__chip" + (S ? " tlTableView__chip--active" : "")
        },
        /* @__PURE__ */ e.createElement(
          "button",
          {
            type: "button",
            className: "tlTableView__chipLabel",
            "aria-pressed": S,
            title: S ? a["js.table.clearFilter"] : b.label,
            onClick: () => al(b.id)
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
            onClick: ($) => rl(b.id, $)
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
        value: tl,
        onChange: (b) => nl(b.target.value),
        onKeyDown: ll
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
        onChange: (b) => Ze(b.target.value),
        onKeyDown: ol
      }
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__barButton",
        title: a["js.table.saveFilter"],
        "aria-label": a["js.table.saveFilter"],
        disabled: !qe.trim(),
        onClick: St
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
        style: { width: Dt, paddingRight: Qe + Q }
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
            L.current && (b.preventDefault(), b.dataTransfer.dropEffect = "move", o.length > 0 && o[0].name !== L.current && V({ column: o[0].name, side: "left" }));
          }
        },
        /* @__PURE__ */ e.createElement(
          "input",
          {
            type: "checkbox",
            ref: cl,
            className: "tlTableView__checkbox",
            checked: sl,
            onChange: zn
          }
        )
      ),
      o.map((b, S) => {
        const $ = ee(b);
        let Z = "tlTableView__headerCell";
        b.sortable && (Z += " tlTableView__headerCell--sortable"), R && R.column === b.name && (Z += " tlTableView__headerCell--dragOver-" + R.side);
        const J = S < p, re = S === p - 1;
        J && (Z += " tlTableView__headerCell--frozen"), re && (Z += " tlTableView__headerCell--frozenLast");
        const ce = !!b.pinnedEnd;
        return ce && (Z += " tlTableView__headerCell--pinnedEnd"), ce && S === ne + 1 && (Z += " tlTableView__headerCell--pinnedEndFirst"), /* @__PURE__ */ e.createElement(
          "div",
          {
            key: b.name,
            className: Z,
            "data-col-idx": S,
            style: {
              width: $,
              minWidth: $,
              position: J || ce ? "sticky" : "relative",
              ...J ? { left: se[S], zIndex: 2 } : {},
              // The header ends with the reserve the body's scrollbar and the column button
              // take, so its cells stick that much further from the right edge than the body's
              // - which is what puts a heading above its column at every scroll position.
              ...ce ? {
                right: be[S] + Qe + Q,
                zIndex: 2
              } : {}
            },
            draggable: !ce,
            onClick: b.sortable ? (oe) => st(b.name, b.sortDirection, oe) : void 0,
            onContextMenu: (oe) => Yn(S, oe),
            onDragStart: (oe) => H(b.name, oe),
            onDragOver: (oe) => q(b.name, oe),
            onDrop: le,
            onDragEnd: ue
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
              onMouseDown: (oe) => oe.stopPropagation(),
              onClick: (oe) => Jn(b.name, oe)
            },
            /* @__PURE__ */ e.createElement("i", { className: b.filterActive ? "bi bi-funnel-fill" : "bi bi-funnel" })
          ),
          b.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, b.sortDirection === "asc" ? "▲" : "▼", w > 1 && b.sortPriority != null && b.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, b.sortPriority)),
          !ce && /* @__PURE__ */ e.createElement(
            "div",
            {
              className: "tlTableView__resizeHandle",
              onMouseDown: (oe) => Re(b.name, $, oe)
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
              const S = o[ne];
              S.name !== L.current && (b.preventDefault(), b.dataTransfer.dropEffect = "move", V({ column: S.name, side: "right" }));
            }
          },
          onDrop: le
        }
      )
    )), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__frozenSplitter" + (Y ? " tlTableView__frozenSplitter--active" : ""),
        style: { left: _e },
        title: a["js.table.freezeSplitter"],
        onMouseDown: Qn
      }
    ), T && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__columnsButton",
        title: a["js.table.columns"],
        "aria-label": a["js.table.columns"],
        onClick: el
      },
      /* @__PURE__ */ e.createElement("i", { className: "bi bi-gear" })
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: D,
        className: "tlTableView__body",
        onScroll: $e,
        onKeyDown: Un,
        tabIndex: 0
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: ke, position: "relative", width: Dt, paddingRight: Qe } }, u.map((b) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: b.id,
          className: "tlTableView__row" + (b.selected ? " tlTableView__row--selected" : "") + (b.index === m ? " tlTableView__row--cursor" : "") + (b.groupCount != null ? " tlTableView__row--group" : ""),
          style: {
            position: "absolute",
            top: b.index * c,
            height: c,
            width: Dt,
            paddingRight: Qe,
            ...b.index === m ? { outline: "2px solid var(--color-primary, #1a73e8)", outlineOffset: "-2px" } : {}
          },
          onMouseDown: (S) => {
            (S.shiftKey || S.ctrlKey || S.metaKey || S.detail > 1) && !pt(S) && S.preventDefault();
          },
          onClick: (S) => Xe(b.index, S),
          onDoubleClick: (S) => Bn(b.index, S)
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
            onClick: (S) => S.stopPropagation()
          },
          b.groupCount == null && /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: b.selected,
              onChange: () => {
              },
              onClick: (S) => Vn(b.index, S),
              tabIndex: -1
            }
          )
        ),
        o.map((S, $) => {
          const Z = ee(S), J = $ < p, re = $ === p - 1;
          let ce = "tlTableView__cell";
          J && (ce += " tlTableView__cell--frozen"), re && (ce += " tlTableView__cell--frozenLast");
          const oe = !!S.pinnedEnd;
          oe && (ce += " tlTableView__cell--pinnedEnd"), oe && $ === ne + 1 && (ce += " tlTableView__cell--pinnedEndFirst");
          const fe = h && $ === 0, ie = b.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: S.name,
              className: ce,
              "data-row": b.id,
              "data-col": S.name,
              style: {
                // The last column the user arranges takes the space left over; a pinned
                // column keeps its width, so the space stays in front of it.
                ...$ === ne && !J ? { flex: "1 0 auto", minWidth: Z } : { width: Z, minWidth: Z },
                ...J ? { position: "sticky", left: se[$], zIndex: 2 } : {},
                ...oe ? {
                  position: "sticky",
                  right: be[$] + Qe,
                  zIndex: 2
                } : {}
              }
            },
            fe ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ie * O } }, b.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (de) => Kn(b.index, !b.expanded, de)
              },
              b.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), b.cells[S.name] && /* @__PURE__ */ e.createElement(G, { control: b.cells[S.name] }), b.groupCount != null && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__groupCount" }, "(", b.groupCount, ")")) : b.cells[S.name] && /* @__PURE__ */ e.createElement(G, { control: b.cells[S.name] })
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
      f.colIdx + 1 !== p && !((Qt = o[f.colIdx]) != null && Qt.pinnedEnd) && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Gn }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      p > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Xn }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"])),
      ((Jt = o[f.colIdx]) == null ? void 0 : Jt.groupable) && o[f.colIdx].name !== g && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlMenu__item",
          role: "menuitem",
          onClick: () => qn(o[f.colIdx].name)
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.groupBy"])
      ),
      g !== "" && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Zn }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.ungroup"]))
    )
  ));
}, _r = {
  "js.table.columnSearch": "Find column",
  "js.table.groupBy": "Group by this column",
  "js.table.ungroup": "Remove grouping"
}, Cr = ({ controlId: l }) => {
  const t = X(), n = ae(), a = me(_r), r = t.entries ?? [], o = r.filter((v) => v.visible).length, [s, u] = e.useState(""), c = s.trim().toLowerCase(), i = c ? r.filter((v) => v.label.toLowerCase().includes(c)) : r, d = e.useRef(null), m = e.useRef(null), [p, h] = e.useState(null), g = e.useCallback((v) => {
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
    const O = C.currentTarget.getBoundingClientRect(), F = C.clientY < O.top + O.height / 2 ? "top" : "bottom";
    g({ name: v, side: F });
  }, [g]), k = e.useCallback(() => {
    d.current = null, g(null);
  }, [g]), I = e.useCallback((v) => {
    v.preventDefault();
    const C = d.current, O = m.current;
    if (d.current = null, g(null), !C || !O)
      return;
    const F = r.findIndex((x) => x.name === O.name), A = r.findIndex((x) => x.name === C);
    if (F < 0 || A < 0)
      return;
    let D = O.side === "top" ? F : F + 1;
    A < D && D--, D !== A && n("columnReorder", { column: C, targetIndex: D });
  }, [r, n, g]), w = r.length > 10;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlColumnSelect", onDrop: I }, w && /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__search" }, /* @__PURE__ */ e.createElement("i", { className: "bi bi-search", "aria-hidden": "true" }), /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "search",
      className: "tlColumnSelect__searchInput",
      placeholder: a["js.table.columnSearch"],
      "aria-label": a["js.table.columnSearch"],
      value: s,
      onChange: (v) => u(v.target.value)
    }
  )), /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__list" + (w ? " tlColumnSelect__list--fixed" : "") }, i.map((v) => {
    const C = v.visible && o <= 1;
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
          onChange: (F) => T(v.name, F.target.checked)
        }
      ), /* @__PURE__ */ e.createElement("span", null, v.label))
    );
  })));
}, { useState: Ut, useRef: lt, useCallback: vt, useMemo: Be, useEffect: hn } = e, yr = {
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
}, Ce = 44, Ct = 15, we = 6e4, wr = 36e5, je = 864e5, kr = 8;
function De(l) {
  const t = new Date(l);
  return t.setHours(0, 0, 0, 0), t.getTime();
}
function ze(l, t) {
  const n = new Date(l);
  return n.setDate(n.getDate() + t), n.getTime();
}
function Nr(l) {
  return De(l);
}
function at(l, t) {
  return De(l) === De(t);
}
function Ie(l) {
  return (l - De(l)) / we;
}
function Je(l) {
  return Math.round(l / Ct) * Ct;
}
function et(l, t, n) {
  return Math.max(t, Math.min(n, l));
}
function yt(l) {
  if (!l)
    return "tlCalEvent--default";
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return "tlCalEvent--c" + Math.abs(t) % kr;
}
function wt(l, t) {
  return l.color ? { ...t, "--cal-ev-bg": l.color } : t;
}
function Sr(l) {
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
function Dr(l, t) {
  const n = { hour: "numeric", minute: "2-digit" };
  return Oe(l, n, t.start) + "–" + Oe(l, n, t.end);
}
const Tr = [
  { key: "DAY", label: "js.calendar.day" },
  { key: "WORK_WEEK", label: "js.calendar.workWeek" },
  { key: "WEEK", label: "js.calendar.week" },
  { key: "MONTH", label: "js.calendar.month" },
  { key: "YEAR", label: "js.calendar.year" }
], Rr = ({ title: l, granularity: t, i18n: n, send: a }) => /* @__PURE__ */ e.createElement("div", { className: "tlCalToolbar" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalNav" }, /* @__PURE__ */ e.createElement("button", { className: "tlCalBtn", onClick: () => a("navigate", { direction: "TODAY" }) }, n["js.calendar.today"]), /* @__PURE__ */ e.createElement(
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
)), /* @__PURE__ */ e.createElement("div", { className: "tlCalTitle" }, l), /* @__PURE__ */ e.createElement("div", { className: "tlCalGranularity" }, Tr.map((r) => /* @__PURE__ */ e.createElement(
  "button",
  {
    key: r.key,
    className: "tlCalBtn" + (r.key === t ? " tlCalBtn--active" : ""),
    onClick: () => a("switchGranularity", { granularity: r.key })
  },
  n[r.label]
))));
function Lr(l) {
  const t = [...l].sort((s, u) => s.start - u.start || u.end - s.end), n = [];
  let a = [], r = -1;
  const o = () => {
    const s = a.reduce((u, c) => Math.max(u, c.col + 1), 0);
    for (const u of a)
      u.cols = s;
    n.push(...a), a = [], r = -1;
  };
  for (const s of t) {
    a.length > 0 && s.start >= r && o();
    const u = new Set(a.filter((i) => i.ev.end > s.start).map((i) => i.col));
    let c = 0;
    for (; u.has(c); )
      c++;
    a.push({
      ev: s,
      topMin: Ie(s.start),
      botMin: Ie(s.start) + Math.max(15, (s.end - s.start) / we),
      col: c,
      cols: 1
    }), r = Math.max(r, s.end);
  }
  return a.length > 0 && o(), n;
}
const Mt = (l) => {
  l.preventDefault(), l.currentTarget.setPointerCapture(l.pointerId);
}, Vt = ({ className: l, placeholder: t, style: n, onCommit: a, onDiscard: r }) => {
  const o = lt(!1), s = (u) => {
    o.current || (o.current = !0, u === null ? r() : a(u));
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
        u.stopPropagation(), u.key === "Enter" ? s(u.currentTarget.value) : u.key === "Escape" && s(null);
      },
      onBlur: () => s(null)
    }
  ));
}, Ln = (l) => {
  const [t, n] = Ut(null), a = lt(null);
  a.current = t;
  const r = vt((u) => n(u), []), o = vt(() => n(null), []), s = vt(
    (u) => {
      const c = a.current;
      c && l("createSlot", { ...c, title: u }), n(null);
    },
    [l]
  );
  return { pending: t, open: r, commit: s, discard: o };
}, xr = ({
  ctx: l,
  rangeStart: t,
  granularity: n
}) => {
  const { events: a, locale: r, nonWorkingDays: o, dayStartHour: s, dayEndHour: u, now: c, send: i, editable: d, i18n: m } = l, p = Be(() => {
    const P = n === "DAY" ? 1 : 7, j = [];
    for (let B = 0; B < P; B++) {
      const L = ze(t, B);
      n === "WORK_WEEK" && o.includes(new Date(L).getDay()) || j.push(L);
    }
    return j;
  }, [t, n, o]), h = Ln(i), g = lt(null), T = lt(null), [y, E] = Ut(null), _ = lt(null);
  _.current = y;
  const [k, I] = Ut(Date.now());
  hn(() => {
    const P = window.setInterval(() => I(Date.now()), 6e4);
    return () => window.clearInterval(P);
  }, []);
  const w = vt(
    (P, j) => {
      const B = g.current;
      if (!B)
        return { dayIndex: 0, min: 0 };
      const L = B.getBoundingClientRect(), R = L.width / p.length, V = et(Math.floor((P - L.left) / R), 0, p.length - 1), f = j - L.top + B.scrollTop, M = et(f / Ce * 60, 0, 1440);
      return { dayIndex: V, min: M };
    },
    [p.length]
  );
  hn(() => {
    if (!y)
      return;
    const P = (L) => {
      const R = _.current;
      if (!R)
        return;
      const { dayIndex: V, min: f } = w(L.clientX, L.clientY);
      R.mode === "move" ? E({ ...R, dayStart: p[V], startMin: et(Je(f - R.grabMin), 0, 1440 - R.dur) }) : R.mode === "resize" ? E({ ...R, endMin: et(Je(f), R.startMin + Ct, 1440) }) : E({ ...R, toMin: et(Je(f), 0, 1440) });
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
          V - R >= Ct && h.open({ start: L.dayStart + R * we, end: L.dayStart + V * we, allDay: !1 });
        }
    }, B = () => E(null);
    return window.addEventListener("pointermove", P), window.addEventListener("pointerup", j, { once: !0 }), window.addEventListener("pointercancel", B), () => {
      window.removeEventListener("pointermove", P), window.removeEventListener("pointerup", j), window.removeEventListener("pointercancel", B);
    };
  }, [y, p, w, i, h.open]);
  const v = (P, j, B) => {
    if (!d || !j.movable)
      return;
    P.stopPropagation(), Mt(P), h.discard();
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
    !d || !j.resizable || (P.stopPropagation(), Mt(P), h.discard(), E({
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
    Mt(P), h.discard();
    const { min: B } = w(P.clientX, P.clientY);
    E({ mode: "create", dayStart: j, fromMin: Je(B), toMin: Je(B) });
  }, F = Array.from({ length: 24 }, (P, j) => j), A = Be(() => {
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
  }, [a, y]), D = Be(() => p.map(
    (P) => Lr(
      A.filter((j) => !j.allDay && j.start < P + je && j.end > P)
    )
  ), [p, A]), x = Be(() => p.map((P) => A.filter((j) => j.allDay && j.start < P + je && j.end > P)), [p, A]), N = s * Ce, W = u * Ce;
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeGrid" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeHeader" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalGutter" }), p.map((P) => {
    const j = o.includes(new Date(P).getDay()), B = at(P, l.now);
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
      Vt,
      {
        className: "tlCalAllDayEvent tlCalEvent--preview",
        placeholder: m["js.calendar.newEventTitle"],
        onCommit: h.commit,
        onDiscard: h.discard
      }
    ),
    x[j].map((B) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: B.id,
        className: "tlCalAllDayEvent " + yt(B.category) + (B.selected ? " tlCalEvent--selected" : ""),
        style: wt(B),
        title: B.tooltip,
        onClick: (L) => {
          L.stopPropagation(), i("selectEvent", { eventId: B.id });
        }
      },
      B.title
    ))
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlCalScroll", ref: T }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeBody", style: { height: 24 * Ce } }, /* @__PURE__ */ e.createElement("div", { className: "tlCalHourAxis" }, F.map((P) => /* @__PURE__ */ e.createElement("div", { key: P, className: "tlCalHourLabel", style: { top: P * Ce } }, P === 0 ? "" : Oe(r, { hour: "numeric" }, De(t) + P * wr)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalColumns", ref: g, style: { gridTemplateColumns: `repeat(${p.length}, 1fr)` } }, p.map((P, j) => {
    const B = o.includes(new Date(P).getDay()), L = y && ("dayStart" in y && y.dayStart === P) ? y : null;
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: P,
        className: "tlCalCol" + (B ? " tlCalCol--nonworking" : ""),
        onPointerDown: (R) => O(R, P)
      },
      F.map((R) => /* @__PURE__ */ e.createElement("div", { key: R, className: "tlCalHourLine", style: { top: R * Ce } })),
      /* @__PURE__ */ e.createElement("div", { className: "tlCalWorkBand", style: { top: N, height: W - N } }),
      at(P, k) && /* @__PURE__ */ e.createElement("div", { className: "tlCalNowLine", style: { top: Ie(Date.now()) / 60 * Ce } }),
      D[j].map((R) => {
        const V = y !== null && "id" in y && y.id === R.ev.id, f = R.topMin / 60 * Ce, M = (R.botMin - R.topMin) / 60 * Ce, Y = 100 / R.cols;
        return /* @__PURE__ */ e.createElement(
          "div",
          {
            key: R.ev.id,
            className: "tlCalEvent " + yt(R.ev.category) + (R.ev.selected ? " tlCalEvent--selected" : "") + (V ? " tlCalEvent--dragging" : ""),
            style: wt(R.ev, {
              top: f,
              height: M,
              left: `${R.col * Y}%`,
              width: `calc(${Y}% - 2px)`
            }),
            title: R.ev.tooltip,
            onPointerDown: (z) => v(z, R.ev, P),
            onClick: (z) => {
              z.stopPropagation(), i("selectEvent", { eventId: R.ev.id });
            }
          },
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTime" }, Dr(r, R.ev)),
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTitle" }, R.ev.title),
          d && R.ev.resizable && /* @__PURE__ */ e.createElement("span", { className: "tlCalResizeHandle", onPointerDown: (z) => C(z, R.ev, P) })
        );
      }),
      h.pending && !h.pending.allDay && De(h.pending.start) === P && /* @__PURE__ */ e.createElement(
        Vt,
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
}, Mr = 3, Ir = ({ ctx: l, rangeStart: t, anchorMonth: n }) => {
  const { events: a, locale: r, nonWorkingDays: o, send: s, editable: u, now: c, i18n: i } = l, d = Ln(s), m = Be(() => {
    const h = [];
    for (let g = 0; g < 6; g++) {
      const T = [];
      for (let y = 0; y < 7; y++)
        T.push(ze(t, g * 7 + y));
      h.push(T);
    }
    return h;
  }, [t]), p = (h, g) => {
    h.preventDefault();
    const T = h.dataTransfer.getData("text/plain"), y = a.find((_) => _.id === T);
    if (!y || !u || !y.movable)
      return;
    const E = g - De(y.start);
    s("moveEvent", { eventId: T, start: y.start + E, end: y.end + E });
  };
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalMonth" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthHead" }, m[0].map((h) => /* @__PURE__ */ e.createElement("div", { key: h, className: "tlCalMonthWeekday" }, Oe(r, { weekday: "short" }, h)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthBody" }, m.map((h, g) => {
    const T = h[0], y = ze(T, 7), E = a.filter((k) => (k.allDay || k.end - k.start >= je) && k.start < y && k.end > T).sort((k, I) => k.start - I.start).slice(0, 3), _ = E.length;
    return /* @__PURE__ */ e.createElement("div", { key: g, className: "tlCalMonthWeek" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthDays" }, h.map((k) => {
      const I = new Date(k).getMonth() === new Date(n).getMonth(), w = o.includes(new Date(k).getDay()), v = at(k, c);
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
              C.stopPropagation(), s("goto", { date: k, granularity: "DAY" });
            }
          },
          new Date(k).getDate()
        ),
        d.pending && d.pending.start === k && /* @__PURE__ */ e.createElement(
          Vt,
          {
            className: "tlCalMonthBar tlCalEvent--preview tlCalMonthCreate",
            placeholder: i["js.calendar.newEventTitle"],
            onCommit: d.commit,
            onDiscard: d.discard
          }
        )
      );
    })), /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthOverlay" }, E.map((k, I) => {
      const w = Math.max(0, Math.floor((De(Math.max(k.start, T)) - T) / je)), v = Math.min(7, Math.ceil((k.end - T) / je));
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: k.id,
          className: "tlCalMonthBar " + yt(k.category) + (k.selected ? " tlCalEvent--selected" : ""),
          style: wt(k, {
            gridColumn: `${w + 1} / ${Math.max(w + 1, v) + 1}`,
            gridRow: I + 1
          }),
          draggable: u && k.movable,
          onDragStart: (C) => C.dataTransfer.setData("text/plain", k.id),
          title: k.tooltip,
          onClick: (C) => {
            C.stopPropagation(), s("selectEvent", { eventId: k.id });
          }
        },
        k.title
      );
    }), h.map((k, I) => {
      const w = a.filter((O) => !O.allDay && O.end - O.start < je && at(O.start, k)).sort((O, F) => O.start - F.start), v = w.slice(0, Mr), C = w.length - v.length;
      return v.map((O, F) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: O.id,
          className: "tlCalChip " + yt(O.category) + (O.selected ? " tlCalEvent--selected" : ""),
          style: wt(O, { gridColumn: I + 1, gridRow: _ + 1 + F }),
          draggable: u && O.movable,
          onDragStart: (A) => A.dataTransfer.setData("text/plain", O.id),
          title: O.tooltip,
          onClick: (A) => {
            A.stopPropagation(), s("selectEvent", { eventId: O.id });
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
              onClick: () => s("goto", { date: k, granularity: "DAY" })
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
}, jr = ({ ctx: l, rangeStart: t }) => {
  const { events: n, locale: a, firstDayOfWeek: r, nonWorkingDays: o, send: s, now: u } = l, c = Be(() => {
    const p = /* @__PURE__ */ new Set();
    for (const h of n) {
      let g = De(h.start);
      const T = h.end;
      for (; g < T; )
        p.add(g), g = ze(g, 1);
    }
    return p;
  }, [n]), i = new Date(t).getFullYear(), d = Array.from({ length: 12 }, (p, h) => new Date(i, h, 1).getTime()), m = Be(() => {
    const p = new Date(2023, 0, 1);
    return Array.from({ length: 7 }, (h, g) => {
      const T = new Date(p);
      return T.setDate(p.getDate() + (r + g) % 7), new Intl.DateTimeFormat(a, { weekday: "narrow" }).format(T);
    });
  }, [a, r]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalYear" }, d.map((p) => {
    const h = new Date(p), g = De(ze(p, -((h.getDay() - r + 7) % 7))), T = Array.from({ length: 42 }, (y, E) => ze(g, E));
    return /* @__PURE__ */ e.createElement("div", { key: p, className: "tlCalMini" }, /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlCalMiniTitle",
        onClick: () => s("goto", { date: p, granularity: "MONTH" })
      },
      Oe(a, { month: "long" }, p)
    ), /* @__PURE__ */ e.createElement("div", { className: "tlCalMiniGrid" }, m.map((y, E) => /* @__PURE__ */ e.createElement("div", { key: "h" + E, className: "tlCalMiniWd" }, y)), T.map((y) => {
      const E = new Date(y).getMonth() === h.getMonth(), _ = o.includes(new Date(y).getDay()), k = at(y, u), I = c.has(Nr(y));
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: y,
          className: "tlCalMiniDay" + (E ? "" : " tlCalMiniDay--other") + (_ ? " tlCalMiniDay--nonworking" : "") + (k ? " tlCalMiniDay--today" : "") + (I ? " tlCalMiniDay--event" : ""),
          onClick: () => s("goto", { date: y, granularity: "DAY" })
        },
        new Date(y).getDate()
      );
    })));
  }));
}, Ar = ({ controlId: l }) => {
  const t = X(), n = ae(), a = me(yr), r = t.granularity ?? "WEEK", o = t.rangeStart ?? Date.now(), s = t.anchor ?? o, u = t.title ?? "", c = {
    locale: t.locale ?? "en",
    firstDayOfWeek: t.firstDayOfWeek ?? 0,
    nonWorkingDays: t.nonWorkingDays ?? [0, 6],
    dayStartHour: t.dayStartHour ?? 8,
    dayEndHour: t.dayEndHour ?? 18,
    editable: t.editable !== !1,
    now: t.now ?? Date.now(),
    events: Sr(t.events),
    send: n,
    i18n: a
  };
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCalendar" }, /* @__PURE__ */ e.createElement(Rr, { title: u, granularity: r, i18n: a, send: n }), /* @__PURE__ */ e.createElement("div", { className: "tlCalBody" }, r === "MONTH" ? /* @__PURE__ */ e.createElement(Ir, { ctx: c, rangeStart: o, anchorMonth: s }) : r === "YEAR" ? /* @__PURE__ */ e.createElement(jr, { ctx: c, rangeStart: o }) : /* @__PURE__ */ e.createElement(xr, { ctx: c, rangeStart: o, granularity: r })));
}, Pr = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, xn = e.createContext(Pr), { useMemo: Br, useRef: Or, useState: Fr, useEffect: $r } = e, Hr = 320, Wr = "TLTableView", Ur = "TLPanel", Vr = ({ controlId: l }) => {
  var y;
  const t = X(), n = t.maxColumns ?? 3, a = t.labelPosition ?? "auto", r = t.readOnly === !0, o = t.children ?? [], s = t.noModelMessage, u = Or(null), [c, i] = Fr(
    a === "top" ? "top" : "side"
  );
  $r(() => {
    if (a !== "auto") {
      i(a);
      return;
    }
    const E = u.current;
    if (!E) return;
    const _ = new ResizeObserver((k) => {
      for (const I of k) {
        const v = I.contentRect.width / n;
        i(v < Hr ? "top" : "side");
      }
    });
    return _.observe(E), () => _.disconnect();
  }, [a, n]);
  const d = Br(() => ({
    readOnly: r,
    resolvedLabelPosition: c
  }), [r, c]), p = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, h = o.length === 1 ? o[0] : void 0, g = !!h && (h.module === Wr || h.module === Ur && ((y = h.state) == null ? void 0 : y.bare) === !0), T = [
    "tlFormLayout",
    r ? "tlFormLayout--readonly" : "",
    g ? "tlFormLayout--flush" : ""
  ].filter(Boolean).join(" ");
  return s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, s)) : /* @__PURE__ */ e.createElement(xn.Provider, { value: d }, /* @__PURE__ */ e.createElement("div", { id: l, className: T, style: p, ref: u }, o.map((E, _) => /* @__PURE__ */ e.createElement(G, { key: _, control: E }))));
}, { useCallback: zr } = e, Kr = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, Yr = ({ controlId: l }) => {
  const t = X(), n = ae(), a = me(Kr), r = t.headerControl ?? null, o = t.headerActions ?? [], s = t.collapsible === !0, u = t.collapsed === !0, c = t.border ?? "none", i = t.fullLine === !0, d = t.children ?? [], m = r != null || o.length > 0 || s, p = zr(() => {
    n("toggleCollapse");
  }, [n]), h = [
    "tlFormGroup",
    `tlFormGroup--border-${c}`,
    i ? "tlFormGroup--fullLine" : "",
    u ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: h }, m && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, s && /* @__PURE__ */ e.createElement(
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
  ), r && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(G, { control: r })), o.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, o.map((g, T) => /* @__PURE__ */ e.createElement(G, { key: T, control: g })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, d.map((g, T) => /* @__PURE__ */ e.createElement(G, { key: T, control: g }))));
}, { useContext: Gr, useState: Xr, useCallback: qr } = e, Zr = ({ controlId: l }) => {
  const t = X(), n = Gr(xn), a = t.label ?? "", r = t.required === !0, o = t.error, s = t.errorIcon, u = t.warnings, c = t.warningIcon, i = t.helpText, d = t.dirty === !0, m = t.labelPosition ?? n.resolvedLabelPosition, p = t.fullLine === !0, h = t.visible !== !1, g = t.hasTooltip === !0, T = t.field, y = n.readOnly, [E, _] = Xr(!1), k = qr(() => _((O) => !O), []), I = m === "hidden", w = o != null, v = u != null && u.length > 0, C = [
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(G, { control: T })), !y && w && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(Ft, { image: s, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, o)), !y && !w && v && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, u.map((O, F) => /* @__PURE__ */ e.createElement("div", { key: F, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(Ft, { image: c, className: "tlFormField__warningIcon" }), /* @__PURE__ */ e.createElement("span", null, O)))), !y && i && E && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, i));
}, Qr = ({ controlId: l }) => {
  const t = X(), n = ae(), a = t.iconCss, r = t.iconSrc, o = t.label, s = t.cssClass, u = t.hasTooltip === !0, c = t.hasLink, i = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : r ? /* @__PURE__ */ e.createElement("img", { src: r, className: "tlTypeIcon", alt: "" }) : null, d = /* @__PURE__ */ e.createElement(e.Fragment, null, i, o && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, o)), m = e.useCallback((g) => {
    g.preventDefault(), n("goto", {});
  }, [n]), p = ["tlResourceCell", s].filter(Boolean).join(" "), h = u ? "key:tooltip" : void 0;
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
}, Jr = 20, bn = "expand", gn = "collapse", ft = "select", eo = "activate", to = "contextMenu", no = "dragOver", lo = "drop", ao = "dragEnd", ro = "single", oo = "multi", so = () => {
  var A;
  const l = X(), t = ae(), n = l.nodes ?? [], a = l.selectionMode ?? ro, r = l.dragEnabled ?? !1, o = l.dropEnabled ?? !1, s = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, c = a === oo, [i, d] = e.useState(null), m = e.useRef(null), p = e.useMemo(() => {
    const D = i == null ? -1 : n.findIndex((x) => x.id === i);
    return D >= 0 ? D : n.findIndex((x) => x.selected);
  }, [n, i]), h = ((A = n.find((D) => D.selected)) == null ? void 0 : A.id) ?? null;
  e.useEffect(() => {
    var x;
    if (h == null)
      return;
    const D = (x = m.current) == null ? void 0 : x.querySelector(".tlTreeView__node--selected");
    D && D.scrollIntoView({ block: "nearest" });
  }, [h]), e.useEffect(() => {
    var D, x;
    i != null && ((x = (D = m.current) == null ? void 0 : D.querySelector(".tlTreeView__node--focused")) == null || x.scrollIntoView({ block: "nearest" }));
  }, [i]);
  const g = e.useCallback((D, x) => {
    t(x ? gn : bn, { nodeId: D });
  }, [t]), T = e.useCallback((D, x) => {
    var W;
    const N = window.getSelection();
    N && !N.isCollapsed && x.currentTarget.contains(N.anchorNode) || ((W = m.current) == null || W.focus({ preventScroll: !0 }), d(D), t(ft, {
      nodeId: D,
      ctrlKey: x.ctrlKey || x.metaKey,
      shiftKey: x.shiftKey
    }));
  }, [t]), y = e.useCallback((D) => {
    d(D), t(eo, { nodeId: D });
  }, [t]), E = e.useCallback((D, x) => {
    x.preventDefault(), t(to, { nodeId: D, x: x.clientX, y: x.clientY });
  }, [t]), _ = e.useRef(null), k = e.useCallback((D, x) => {
    const N = x.getBoundingClientRect(), W = D.clientY - N.top, P = N.height / 3;
    return W < P ? "above" : W > P * 2 ? "below" : "within";
  }, []), I = e.useCallback((D, x) => {
    x.dataTransfer.effectAllowed = "move", x.dataTransfer.setData("text/plain", D);
  }, []), w = e.useCallback((D, x) => {
    x.preventDefault(), x.dataTransfer.dropEffect = "move";
    const N = k(x, x.currentTarget);
    _.current != null && window.clearTimeout(_.current), _.current = window.setTimeout(() => {
      t(no, { nodeId: D, position: N }), _.current = null;
    }, 50);
  }, [t, k]), v = e.useCallback((D, x) => {
    x.preventDefault(), _.current != null && (window.clearTimeout(_.current), _.current = null);
    const N = k(x, x.currentTarget);
    t(lo, { nodeId: D, position: N });
  }, [t, k]), C = e.useCallback(() => {
    _.current != null && (window.clearTimeout(_.current), _.current = null), t(ao);
  }, [t]), O = e.useCallback((D, x) => {
    const N = n[D];
    N != null && (d(N.id), c ? x && t(ft, { nodeId: N.id, ctrlKey: !1, shiftKey: !0 }) : t(ft, { nodeId: N.id, ctrlKey: !1, shiftKey: !1 }));
  }, [n, c, t]), F = e.useCallback((D) => {
    if (n.length === 0)
      return;
    const x = p >= 0 ? n[p] : null;
    let N = p;
    switch (D.key) {
      case "ArrowDown":
        D.preventDefault(), N = Math.min(p + 1, n.length - 1);
        break;
      case "ArrowUp":
        D.preventDefault(), N = Math.max(p - 1, 0);
        break;
      case "ArrowRight":
        if (D.preventDefault(), x == null)
          break;
        if (x.expandable && !x.expanded) {
          t(bn, { nodeId: x.id });
          return;
        }
        x.expanded && (N = p + 1);
        break;
      case "ArrowLeft":
        if (D.preventDefault(), x == null)
          break;
        if (x.expanded) {
          t(gn, { nodeId: x.id });
          return;
        }
        for (let W = p - 1; W >= 0; W--)
          if (n[W].depth < x.depth) {
            N = W;
            break;
          }
        break;
      case "Home":
        D.preventDefault(), N = 0;
        break;
      case "End":
        D.preventDefault(), N = n.length - 1;
        break;
      case "Enter":
        D.preventDefault(), x != null && y(x.id);
        return;
      case " ":
        D.preventDefault(), x != null && t(ft, { nodeId: x.id, ctrlKey: c, shiftKey: !1 });
        return;
      default:
        return;
    }
    N !== p && O(N, D.shiftKey);
  }, [p, n, t, c, y, O]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: m,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: F
    },
    n.map((D, x) => /* @__PURE__ */ e.createElement(
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
          x === p ? "tlTreeView__node--focused" : "",
          s === D.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          s === D.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          s === D.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: D.depth * Jr },
        draggable: r,
        onMouseDown: (N) => {
          (N.shiftKey || N.ctrlKey || N.metaKey || N.detail > 1) && N.preventDefault();
        },
        onClick: (N) => T(D.id, N),
        onDoubleClick: () => y(D.id),
        onContextMenu: (N) => E(D.id, N),
        onDragStart: (N) => I(D.id, N),
        onDragOver: o ? (N) => w(D.id, N) : void 0,
        onDrop: o ? (N) => v(D.id, N) : void 0,
        onDragEnd: C
      },
      D.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (N) => {
            N.stopPropagation(), g(D.id, D.expanded);
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
var It = { exports: {} }, he = {}, jt = { exports: {} }, te = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var En;
function co() {
  if (En) return te;
  En = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), a = Symbol.for("react.strict_mode"), r = Symbol.for("react.profiler"), o = Symbol.for("react.consumer"), s = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), c = Symbol.for("react.suspense"), i = Symbol.for("react.memo"), d = Symbol.for("react.lazy"), m = Symbol.for("react.activity"), p = Symbol.iterator;
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
  function E(f, M, Y) {
    this.props = f, this.context = M, this.refs = y, this.updater = Y || g;
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
    this.props = f, this.context = M, this.refs = y, this.updater = Y || g;
  }
  var I = k.prototype = new _();
  I.constructor = k, T(I, E.prototype), I.isPureReactComponent = !0;
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
  function D(f) {
    return typeof f == "object" && f !== null && f.$$typeof === l;
  }
  function x(f) {
    var M = { "=": "=0", ":": "=2" };
    return "$" + f.replace(/[=:]/g, function(Y) {
      return M[Y];
    });
  }
  var N = /\/+/g;
  function W(f, M) {
    return typeof f == "object" && f !== null && f.key != null ? x("" + f.key) : M.toString(36);
  }
  function P(f) {
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
                M,
                Y,
                z,
                Q
              );
          }
      }
    if (ee)
      return Q = Q(f), ee = z === "" ? "." + W(f, 0) : z, w(Q) ? (Y = "", ee != null && (Y = ee.replace(N, "$&/") + "/"), j(Q, M, Y, "", function(be) {
        return be;
      })) : Q != null && (D(Q) && (Q = A(
        Q,
        Y + (Q.key == null || f && f.key === Q.key ? "" : ("" + Q.key).replace(
          N,
          "$&/"
        ) + "/") + ee
      )), M.push(Q)), 1;
    ee = 0;
    var se = z === "" ? "." : z + ":";
    if (w(f))
      for (var ne = 0; ne < f.length; ne++)
        z = f[ne], U = se + W(z, ne), ee += j(
          z,
          M,
          Y,
          U,
          Q
        );
    else if (ne = h(f), typeof ne == "function")
      for (f = ne.call(f), ne = 0; !(z = f.next()).done; )
        z = z.value, U = se + W(z, ne++), ee += j(
          z,
          M,
          Y,
          U,
          Q
        );
    else if (U === "object") {
      if (typeof f.then == "function")
        return j(
          P(f),
          M,
          Y,
          z,
          Q
        );
      throw M = String(f), Error(
        "Objects are not valid as a React child (found: " + (M === "[object Object]" ? "object with keys {" + Object.keys(f).join(", ") + "}" : M) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return ee;
  }
  function B(f, M, Y) {
    if (f == null) return f;
    var z = [], Q = 0;
    return j(f, z, "", "", function(U) {
      return M.call(Y, U, Q++);
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
  }, V = {
    map: B,
    forEach: function(f, M, Y) {
      B(
        f,
        function() {
          M.apply(this, arguments);
        },
        Y
      );
    },
    count: function(f) {
      var M = 0;
      return B(f, function() {
        M++;
      }), M;
    },
    toArray: function(f) {
      return B(f, function(M) {
        return M;
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
  }, te.cloneElement = function(f, M, Y) {
    if (f == null)
      throw Error(
        "The argument must be a React element, but you passed " + f + "."
      );
    var z = T({}, f.props), Q = f.key;
    if (M != null)
      for (U in M.key !== void 0 && (Q = "" + M.key), M)
        !O.call(M, U) || U === "key" || U === "__self" || U === "__source" || U === "ref" && M.ref === void 0 || (z[U] = M[U]);
    var U = arguments.length - 2;
    if (U === 1) z.children = Y;
    else if (1 < U) {
      for (var ee = Array(U), se = 0; se < U; se++)
        ee[se] = arguments[se + 2];
      z.children = ee;
    }
    return F(f.type, Q, z);
  }, te.createContext = function(f) {
    return f = {
      $$typeof: s,
      _currentValue: f,
      _currentValue2: f,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, f.Provider = f, f.Consumer = {
      $$typeof: o,
      _context: f
    }, f;
  }, te.createElement = function(f, M, Y) {
    var z, Q = {}, U = null;
    if (M != null)
      for (z in M.key !== void 0 && (U = "" + M.key), M)
        O.call(M, z) && z !== "key" && z !== "__self" && z !== "__source" && (Q[z] = M[z]);
    var ee = arguments.length - 2;
    if (ee === 1) Q.children = Y;
    else if (1 < ee) {
      for (var se = Array(ee), ne = 0; ne < ee; ne++)
        se[ne] = arguments[ne + 2];
      Q.children = se;
    }
    if (f && f.defaultProps)
      for (z in ee = f.defaultProps, ee)
        Q[z] === void 0 && (Q[z] = ee[z]);
    return F(f, U, Q);
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
  }, te.memo = function(f, M) {
    return {
      $$typeof: i,
      type: f,
      compare: M === void 0 ? null : M
    };
  }, te.startTransition = function(f) {
    var M = C.T, Y = {};
    C.T = Y;
    try {
      var z = f(), Q = C.S;
      Q !== null && Q(Y, z), typeof z == "object" && z !== null && typeof z.then == "function" && z.then(v, R);
    } catch (U) {
      R(U);
    } finally {
      M !== null && Y.types !== null && (M.types = Y.types), C.T = M;
    }
  }, te.unstable_useCacheRefresh = function() {
    return C.H.useCacheRefresh();
  }, te.use = function(f) {
    return C.H.use(f);
  }, te.useActionState = function(f, M, Y) {
    return C.H.useActionState(f, M, Y);
  }, te.useCallback = function(f, M) {
    return C.H.useCallback(f, M);
  }, te.useContext = function(f) {
    return C.H.useContext(f);
  }, te.useDebugValue = function() {
  }, te.useDeferredValue = function(f, M) {
    return C.H.useDeferredValue(f, M);
  }, te.useEffect = function(f, M) {
    return C.H.useEffect(f, M);
  }, te.useEffectEvent = function(f) {
    return C.H.useEffectEvent(f);
  }, te.useId = function() {
    return C.H.useId();
  }, te.useImperativeHandle = function(f, M, Y) {
    return C.H.useImperativeHandle(f, M, Y);
  }, te.useInsertionEffect = function(f, M) {
    return C.H.useInsertionEffect(f, M);
  }, te.useLayoutEffect = function(f, M) {
    return C.H.useLayoutEffect(f, M);
  }, te.useMemo = function(f, M) {
    return C.H.useMemo(f, M);
  }, te.useOptimistic = function(f, M) {
    return C.H.useOptimistic(f, M);
  }, te.useReducer = function(f, M, Y) {
    return C.H.useReducer(f, M, Y);
  }, te.useRef = function(f) {
    return C.H.useRef(f);
  }, te.useState = function(f) {
    return C.H.useState(f);
  }, te.useSyncExternalStore = function(f, M, Y) {
    return C.H.useSyncExternalStore(
      f,
      M,
      Y
    );
  }, te.useTransition = function() {
    return C.H.useTransition();
  }, te.version = "19.2.4", te;
}
var vn;
function io() {
  return vn || (vn = 1, jt.exports = co()), jt.exports;
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
var _n;
function uo() {
  if (_n) return he;
  _n = 1;
  var l = io();
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
  function o(c, i, d) {
    var m = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: r,
      key: m == null ? null : "" + m,
      children: c,
      containerInfo: i,
      implementation: d
    };
  }
  var s = l.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function u(c, i) {
    if (c === "font") return "";
    if (typeof i == "string")
      return i === "use-credentials" ? i : "";
  }
  return he.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = a, he.createPortal = function(c, i) {
    var d = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!i || i.nodeType !== 1 && i.nodeType !== 9 && i.nodeType !== 11)
      throw Error(t(299));
    return o(c, i, null, d);
  }, he.flushSync = function(c) {
    var i = s.T, d = a.p;
    try {
      if (s.T = null, a.p = 2, c) return c();
    } finally {
      s.T = i, a.p = d, a.d.f();
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
    return s.H.useFormState(c, i, d);
  }, he.useFormStatus = function() {
    return s.H.useHostTransitionStatus();
  }, he.version = "19.2.4", he;
}
var Cn;
function mo() {
  if (Cn) return It.exports;
  Cn = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), It.exports = uo(), It.exports;
}
var Mn = mo();
const { useState: Me, useCallback: Ee, useRef: tt, useEffect: We, useMemo: zt } = e;
function Xt({ image: l }) {
  return l ? l.startsWith("/") ? /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" }) : /* @__PURE__ */ e.createElement(Se, { encoded: l, className: "tlDropdownSelect__optionIcon" }) : null;
}
function po({
  option: l,
  removable: t,
  onRemove: n,
  removeLabel: a,
  draggable: r,
  onDragStart: o,
  onDragOver: s,
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
      onDragStart: o,
      onDragOver: s,
      onDrop: u,
      onDragEnd: c
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
function fo({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: a,
  onMouseEnter: r,
  id: o
}) {
  const s = Ee(() => a(l.value), [a, l.value]), u = zt(() => {
    if (!n) return l.label;
    const c = l.label.toLowerCase().indexOf(n.toLowerCase());
    return c < 0 ? l.label : /* @__PURE__ */ e.createElement(e.Fragment, null, l.label.substring(0, c), /* @__PURE__ */ e.createElement("strong", null, l.label.substring(c, c + n.length)), l.label.substring(c + n.length));
  }, [l.label, n]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: o,
      role: "option",
      "aria-selected": t,
      className: "tlDropdownSelect__option" + (t ? " tlDropdownSelect__option--highlighted" : ""),
      onClick: s,
      onMouseEnter: r
    },
    /* @__PURE__ */ e.createElement(Xt, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const ho = ({ controlId: l, state: t }) => {
  const n = ae(), a = t.value ?? [], r = t.multiSelect === !0, o = t.customOrder === !0, s = t.mandatory === !0, u = t.disabled === !0, c = t.editable !== !1, i = t.optionsLoaded === !0, d = t.options ?? [], m = t.emptyOptionLabel ?? "", p = o && r && !u && c, h = me({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), g = h["js.dropdownSelect.nothingFound"], T = Ee(
    (H) => h["js.dropdownSelect.removeChip"].replace("{0}", H),
    [h]
  ), [y, E] = Me(!1), [_, k] = Me(""), [I, w] = Me(-1), [v, C] = Me(!1), [O, F] = Me({}), [A, D] = Me(null), [x, N] = Me(null), [W, P] = Me(null), j = tt(null), B = tt(null), L = tt(null), R = tt(a);
  R.current = a;
  const V = tt(-1), f = zt(
    () => new Set(a.map((H) => H.value)),
    [a]
  ), M = zt(() => {
    let H = d.filter((q) => !f.has(q.value));
    if (_) {
      const q = _.toLowerCase();
      H = H.filter((le) => le.label.toLowerCase().includes(q));
    }
    return H;
  }, [d, f, _]);
  We(() => {
    _ && M.length === 1 ? w(0) : w(-1);
  }, [M.length, _]), We(() => {
    y && i && B.current && B.current.focus();
  }, [y, i, a]), We(() => {
    var le, ue;
    if (V.current < 0) return;
    const H = V.current;
    V.current = -1;
    const q = (le = j.current) == null ? void 0 : le.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    q && q.length > 0 ? q[Math.min(H, q.length - 1)].focus() : (ue = j.current) == null || ue.focus();
  }, [a]), We(() => {
    if (!y) return;
    const H = (q) => {
      j.current && !j.current.contains(q.target) && L.current && !L.current.contains(q.target) && (E(!1), k(""));
    };
    return document.addEventListener("mousedown", H), () => document.removeEventListener("mousedown", H);
  }, [y]), We(() => {
    if (!y || !j.current) return;
    const H = j.current.getBoundingClientRect(), q = window.innerHeight - H.bottom, ue = q < 300 && H.top > q;
    F({
      left: H.left,
      width: H.width,
      ...ue ? { bottom: window.innerHeight - H.top } : { top: H.bottom }
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
        const le = d.find((ue) => ue.value === H);
        if (le)
          q = [...R.current, le];
        else
          return;
      } else {
        const le = d.find((ue) => ue.value === H);
        if (le)
          q = [le];
        else
          return;
      }
      R.current = q, n(it, { value: q.map((le) => le.value) }), r ? (k(""), w(-1)) : z();
    },
    [r, d, n, z]
  ), U = Ee(
    (H) => {
      V.current = R.current.findIndex((le) => le.value === H);
      const q = R.current.filter((le) => le.value !== H);
      R.current = q, n(it, { value: q.map((le) => le.value) });
    },
    [n]
  ), ee = Ee(
    (H) => {
      H.stopPropagation(), n(it, { value: [] }), z();
    },
    [n, z]
  ), se = Ee((H) => {
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
            (q) => q < M.length - 1 ? q + 1 : 0
          );
          break;
        case "ArrowUp":
          H.preventDefault(), H.stopPropagation(), w(
            (q) => q > 0 ? q - 1 : M.length - 1
          );
          break;
        case "Enter":
          H.preventDefault(), H.stopPropagation(), I >= 0 && I < M.length && Q(M[I].value);
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
      M,
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
        N(null), P(null);
        return;
      }
      const le = q.currentTarget.getBoundingClientRect(), ue = le.left + le.width / 2, Xe = q.clientX < ue ? "before" : "after";
      N(H), P(Xe);
    },
    [A]
  ), ye = Ee(
    (H) => {
      if (H.preventDefault(), A === null || x === null || W === null || A === x) return;
      const q = [...R.current], [le] = q.splice(A, 1);
      let ue = x;
      A < x ? ue = W === "before" ? ue - 1 : ue : ue = W === "before" ? ue : ue + 1, q.splice(ue, 0, le), R.current = q, n(it, { value: q.map((Xe) => Xe.value) }), D(null), N(null), P(null);
    },
    [A, x, W, n]
  ), Re = Ee(() => {
    D(null), N(null), P(null);
  }, []);
  if (We(() => {
    if (I < 0 || !L.current) return;
    const H = L.current.querySelector(
      `[id="${l}-opt-${I}"]`
    );
    H && H.scrollIntoView({ block: "nearest" });
  }, [I, l]), !c)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, a.map((H) => /* @__PURE__ */ e.createElement("span", { key: H.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(Xt, { image: H.image }), /* @__PURE__ */ e.createElement("span", null, H.label))));
  const $e = !s && a.length > 0 && !u, st = y ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: L,
      className: "tlDropdownSelect__dropdown",
      style: O,
      ...dl
    },
    (i || v) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: B,
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
      !i && !v && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      v && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: be }, h["js.dropdownSelect.error"])),
      i && M.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, g),
      i && M.map((H, q) => /* @__PURE__ */ e.createElement(
        fo,
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
      let le = "";
      return A === q ? le = "tlDropdownSelect__chip--dragging" : x === q && W === "before" ? le = "tlDropdownSelect__chip--dropBefore" : x === q && W === "after" && (le = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        po,
        {
          key: H.value,
          option: H,
          removable: !u && (r || !s),
          onRemove: U,
          removeLabel: T(H.label),
          draggable: p,
          onDragStart: p ? (ue) => _e(q, ue) : void 0,
          onDragOver: p ? (ue) => ke(q, ue) : void 0,
          onDrop: p ? ye : void 0,
          onDragEnd: p ? Re : void 0,
          dragClassName: p ? le : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, $e && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: ee,
        "aria-label": h["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, y ? "▲" : "▼"))
  ), st && Mn.createPortal(st, document.body));
}, { useCallback: At, useRef: bo } = e, In = "application/x-tl-color", go = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: a,
  onSwap: r,
  onReplace: o
}) => {
  const s = bo(null), u = At(
    (d) => (m) => {
      s.current = d, m.dataTransfer.effectAllowed = "move";
    },
    []
  ), c = At((d) => {
    d.preventDefault(), d.dataTransfer.dropEffect = "move";
  }, []), i = At(
    (d) => (m) => {
      m.preventDefault();
      const p = m.dataTransfer.getData(In);
      p ? o(d, p) : s.current !== null && s.current !== d && r(s.current, d), s.current = null;
    },
    [r, o]
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
function jn(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function Kt(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function An(l) {
  if (!Kt(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function Pn(l, t, n) {
  const a = (r) => jn(r).toString(16).padStart(2, "0");
  return "#" + a(l) + a(t) + a(n);
}
function Eo(l, t, n) {
  const a = l / 255, r = t / 255, o = n / 255, s = Math.max(a, r, o), u = Math.min(a, r, o), c = s - u;
  let i = 0;
  c !== 0 && (s === a ? i = (r - o) / c % 6 : s === r ? i = (o - a) / c + 2 : i = (a - r) / c + 4, i *= 60, i < 0 && (i += 360));
  const d = s === 0 ? 0 : c / s;
  return [i, d, s];
}
function vo(l, t, n) {
  const a = n * t, r = a * (1 - Math.abs(l / 60 % 2 - 1)), o = n - a;
  let s = 0, u = 0, c = 0;
  return l < 60 ? (s = a, u = r, c = 0) : l < 120 ? (s = r, u = a, c = 0) : l < 180 ? (s = 0, u = a, c = r) : l < 240 ? (s = 0, u = r, c = a) : l < 300 ? (s = r, u = 0, c = a) : (s = a, u = 0, c = r), [
    Math.round((s + o) * 255),
    Math.round((u + o) * 255),
    Math.round((c + o) * 255)
  ];
}
function _o(l) {
  return Eo(...An(l));
}
function Pt(l, t, n) {
  return Pn(...vo(l, t, n));
}
const { useCallback: Ue, useRef: yn } = e, Co = ({ color: l, onColorChange: t }) => {
  const [n, a, r] = _o(l), o = yn(null), s = yn(null), u = Ue(
    (g, T) => {
      var k;
      const y = (k = o.current) == null ? void 0 : k.getBoundingClientRect();
      if (!y) return;
      const E = Math.max(0, Math.min(1, (g - y.left) / y.width)), _ = Math.max(0, Math.min(1, 1 - (T - y.top) / y.height));
      t(Pt(n, E, _));
    },
    [n, t]
  ), c = Ue(
    (g) => {
      g.preventDefault(), g.target.setPointerCapture(g.pointerId), u(g.clientX, g.clientY);
    },
    [u]
  ), i = Ue(
    (g) => {
      g.buttons !== 0 && u(g.clientX, g.clientY);
    },
    [u]
  ), d = Ue(
    (g) => {
      var _;
      const T = (_ = s.current) == null ? void 0 : _.getBoundingClientRect();
      if (!T) return;
      const E = Math.max(0, Math.min(1, (g - T.top) / T.height)) * 360;
      t(Pt(E, a, r));
    },
    [a, r, t]
  ), m = Ue(
    (g) => {
      g.preventDefault(), g.target.setPointerCapture(g.pointerId), d(g.clientY);
    },
    [d]
  ), p = Ue(
    (g) => {
      g.buttons !== 0 && d(g.clientY);
    },
    [d]
  ), h = Pt(n, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: o,
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
      ref: s,
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
function yo(l, t) {
  const n = t.toUpperCase();
  return l.some((a) => a != null && a.toUpperCase() === n);
}
const wo = {
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
}, { useState: ht, useCallback: Ne, useEffect: wn, useRef: ko, useLayoutEffect: No } = e, So = ({
  anchorRef: l,
  currentColor: t,
  palette: n,
  paletteColumns: a,
  defaultPalette: r,
  canReset: o,
  onConfirm: s,
  onCancel: u,
  onPaletteChange: c
}) => {
  const [i, d] = ht("palette"), [m, p] = ht(t), h = ko(null), g = me(wo), [T, y] = ht(null);
  No(() => {
    if (!l.current || !h.current) return;
    const L = l.current.getBoundingClientRect(), R = h.current.getBoundingClientRect();
    let V = L.bottom + 4, f = L.left;
    V + R.height > window.innerHeight && (V = L.top - R.height - 4), f + R.width > window.innerWidth && (f = Math.max(0, L.right - R.width)), y({ top: V, left: f });
  }, [l]);
  const E = m != null, [_, k, I] = E ? An(m) : [0, 0, 0], [w, v] = ht((m == null ? void 0 : m.toUpperCase()) ?? "");
  wn(() => {
    v((m == null ? void 0 : m.toUpperCase()) ?? "");
  }, [m]), Fe(!0, { ESCAPE: u }), wn(() => {
    const L = (V) => {
      h.current && !h.current.contains(V.target) && u();
    }, R = setTimeout(() => document.addEventListener("mousedown", L), 0);
    return () => {
      clearTimeout(R), document.removeEventListener("mousedown", L);
    };
  }, [u]);
  const C = Ne(
    (L) => (R) => {
      const V = parseInt(R.target.value, 10);
      if (isNaN(V)) return;
      const f = jn(V);
      p(Pn(L === "r" ? f : _, L === "g" ? f : k, L === "b" ? f : I));
    },
    [_, k, I]
  ), O = Ne(
    (L) => {
      if (m != null) {
        L.dataTransfer.setData(In, m.toUpperCase()), L.dataTransfer.effectAllowed = "move";
        const R = document.createElement("div");
        R.style.width = "33px", R.style.height = "33px", R.style.backgroundColor = m, R.style.borderRadius = "3px", R.style.border = "1px solid rgba(0,0,0,0.1)", R.style.position = "absolute", R.style.top = "-9999px", document.body.appendChild(R), L.dataTransfer.setDragImage(R, 16, 16), requestAnimationFrame(() => document.body.removeChild(R));
      }
    },
    [m]
  ), F = Ne((L) => {
    const R = L.target.value;
    v(R), Kt(R) && p(R);
  }, []), A = Ne(() => {
    p(null);
  }, []), D = Ne((L) => {
    p(L);
  }, []), x = Ne(
    (L) => {
      s(L);
    },
    [s]
  ), N = Ne(
    (L, R) => {
      const V = [...n], f = V[L];
      V[L] = V[R], V[R] = f, c(V);
    },
    [n, c]
  ), W = Ne(
    (L, R) => {
      const V = [...n];
      V[L] = R, c(V);
    },
    [n, c]
  ), P = Ne(() => {
    c([...r]);
  }, [r, c]), j = Ne(
    (L) => {
      if (yo(n, L)) return;
      const R = n.indexOf(null);
      if (R < 0) return;
      const V = [...n];
      V[R] = L.toUpperCase(), c(V);
    },
    [n, c]
  ), B = Ne(() => {
    m != null && j(m), s(m);
  }, [m, s, j]);
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
      go,
      {
        colors: n,
        columns: a,
        onSelect: D,
        onConfirm: x,
        onSwap: N,
        onReplace: W
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: P }, g["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(Co, { color: m ?? "#000000", onColorChange: p }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, g["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
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
        className: "tlColorInput__input" + (w !== "" && !Kt(w) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: w,
        onChange: F
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, o && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: A }, g["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, g["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: B }, g["js.colorInput.ok"]))
  );
}, Do = { "js.colorInput.chooseColor": "Choose color" }, { useState: To, useCallback: bt, useRef: Ro } = e, Lo = ({ controlId: l, state: t }) => {
  const [n, a] = Te(), r = ae(), o = me(Do), [s, u] = To(!1), c = Ro(null), i = n, d = t.editable !== !1, m = t.palette ?? [], p = t.paletteColumns ?? 6, h = t.defaultPalette ?? m, g = bt(() => {
    d && u(!0);
  }, [d]), T = bt(
    (_) => {
      u(!1), a(_);
    },
    [a]
  ), y = bt(() => {
    u(!1);
  }, []), E = bt(
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
      "aria-label": o["js.colorInput.chooseColor"]
    }
  ), s && /* @__PURE__ */ e.createElement(
    So,
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
}, { useState: nt, useCallback: Pe, useEffect: Bt, useRef: kn, useLayoutEffect: xo, useMemo: Mo } = e, Io = {
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
}, jo = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: a,
  onSelect: r,
  onCancel: o,
  onLoadIcons: s
}) => {
  const u = me(Io), [c, i] = nt("simple"), [d, m] = nt(""), [p, h] = nt(t ?? ""), [g, T] = nt(!1), [y, E] = nt(null), _ = kn(null), k = kn(null);
  xo(() => {
    if (!l.current || !_.current) return;
    const x = l.current.getBoundingClientRect(), N = _.current.getBoundingClientRect();
    let W = x.bottom + 4, P = x.left;
    W + N.height > window.innerHeight && (W = x.top - N.height - 4), P + N.width > window.innerWidth && (P = Math.max(0, x.right - N.width)), E({ top: W, left: P });
  }, [l]), Bt(() => {
    !a && !g && s().catch(() => T(!0));
  }, [a, g, s]), Bt(() => {
    a && k.current && k.current.focus();
  }, [a]), Fe(!0, { ESCAPE: o }), Bt(() => {
    const x = (W) => {
      _.current && !_.current.contains(W.target) && o();
    }, N = setTimeout(() => document.addEventListener("mousedown", x), 0);
    return () => {
      clearTimeout(N), document.removeEventListener("mousedown", x);
    };
  }, [o]);
  const I = Mo(() => {
    if (!d) return n;
    const x = d.toLowerCase();
    return n.filter(
      (N) => N.prefix.toLowerCase().includes(x) || N.label.toLowerCase().includes(x) || N.terms != null && N.terms.some((W) => W.includes(x))
    );
  }, [n, d]), w = Pe((x) => {
    m(x.target.value);
  }, []), v = Pe(
    (x) => {
      r(x);
    },
    [r]
  ), C = Pe((x) => {
    h(x);
  }, []), O = Pe((x) => {
    h(x.target.value);
  }, []), F = Pe(() => {
    r(p || null);
  }, [p, r]), A = Pe(() => {
    r(null);
  }, [r]), D = Pe(async (x) => {
    x.preventDefault(), T(!1);
    try {
      await s();
    } catch {
      T(!0);
    }
  }, [s]);
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
        (x) => x.variants.map((N) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: N.encoded,
            className: "tlIconSelect__iconCell" + (N.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": N.encoded === t,
            tabIndex: 0,
            title: x.label,
            onClick: () => c === "simple" ? v(N.encoded) : C(N.encoded),
            onKeyDown: (W) => {
              (W.key === "Enter" || W.key === " ") && (W.preventDefault(), c === "simple" ? v(N.encoded) : C(N.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(Se, { encoded: N.encoded })
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
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, p && /* @__PURE__ */ e.createElement(Se, { encoded: p })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, p ? p.startsWith("css:") ? p.substring(4) : p : ""))),
    c === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: o }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: A }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: F }, u["js.iconSelect.ok"]))
  );
}, Ao = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: Po, useCallback: gt, useRef: Bo } = e, Oo = ({ controlId: l, state: t }) => {
  const [n, a] = Te(), r = ae(), o = me(Ao), [s, u] = Po(!1), c = Bo(null), i = n, d = t.editable !== !1, m = t.disabled === !0, p = t.icons ?? [], h = t.iconsLoaded === !0, g = gt(() => {
    d && !m && u(!0);
  }, [d, m]), T = gt(
    (_) => {
      u(!1), a(_);
    },
    [a]
  ), y = gt(() => {
    u(!1);
  }, []), E = gt(async () => {
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
      "aria-label": o["js.iconSelect.chooseIcon"]
    },
    i ? /* @__PURE__ */ e.createElement(Se, { encoded: i }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), s && /* @__PURE__ */ e.createElement(
    jo,
    {
      anchorRef: c,
      currentValue: i,
      icons: p,
      iconsLoaded: h,
      onSelect: T,
      onCancel: y,
      onLoadIcons: E
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, i ? /* @__PURE__ */ e.createElement(Se, { encoded: i }) : null));
}, { useCallback: Ve, useEffect: Fo, useMemo: Nn, useRef: $o, useState: Ot } = e, Ho = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, Wo = [1, 2, 3, 4];
function Uo(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const a = parseFloat(n[1]), r = n[2] || "px";
  return r === "rem" || r === "em" ? a * t : a;
}
function Vo(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let a = 1;
  for (const r of Wo)
    n >= r && (a = r);
  return a;
}
function zo(l, t) {
  const n = Ho[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function Ko(l, t) {
  const n = Math.max(1, t), a = {}, r = (m, p) => !!(a[m] && a[m][p]), o = (m, p) => {
    a[m] || (a[m] = {}), a[m][p] = !0;
  }, s = [];
  let u = 0, c = 0;
  const i = (m) => {
    let p = null;
    for (const g of s) g.rowStart === m && (p = g);
    if (!p) return;
    let h = p.colEnd;
    for (; h < n && !r(m, h); ) h++;
    if (h !== p.colEnd) {
      for (let g = p.rowStart; g < p.rowEnd; g++)
        for (let T = p.colEnd; T < h; T++) o(g, T);
      p.colEnd = h;
    }
  };
  for (const m of l) {
    const p = n <= 1 ? 1 : Math.max(1, m.rowSpan || 1);
    let h = Math.min(zo(m.width, n), n);
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
    s.push({ id: m.id, colStart: T, colEnd: y, rowStart: E, rowEnd: _ });
    for (let k = E; k < _; k++)
      for (let I = T; I < y; I++) o(k, I);
    c = y, c >= n && (c = 0, u++);
  }
  i(u);
  let d = 0;
  for (const m of s) m.rowEnd > d && (d = m.rowEnd);
  for (let m = 1; m < d; m++)
    for (let p = 0; p < n; p++) {
      if (r(m, p)) continue;
      const h = s.find((g) => g.rowEnd === m && g.colStart <= p && p < g.colEnd);
      if (h) {
        h.rowEnd = m + 1;
        for (let g = h.colStart; g < h.colEnd; g++) o(m, g);
      }
    }
  return s;
}
const Yo = ({ controlId: l }) => {
  const t = X(), n = ae(), a = t.minColWidth ?? "16rem", r = (t.children ?? []).filter((v) => v && v.id), o = $o(null), [s, u] = Ot(1), c = t.editMode === !0;
  Fo(() => {
    const v = o.current;
    if (!v) return;
    const C = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, O = Uo(a, C), F = () => u(Vo(v.clientWidth, O));
    F();
    const A = new ResizeObserver(F);
    return A.observe(v), () => A.disconnect();
  }, [a]);
  const i = Nn(() => Ko(r, s), [r, s]), d = Nn(() => {
    const v = {};
    for (const C of i) v[C.id] = C;
    return v;
  }, [i]), [m, p] = Ot(null), [h, g] = Ot(null), T = Ve((v, C) => {
    if (!c) {
      v.preventDefault();
      return;
    }
    p(C), v.dataTransfer.effectAllowed = "move", v.dataTransfer.setData("text/plain", C);
  }, [c]), y = Ve((v, C) => {
    if (!c || !m || m === C) return;
    v.preventDefault(), v.dataTransfer.dropEffect = "move";
    const O = v.currentTarget.getBoundingClientRect(), F = v.clientX < O.left + O.width / 2;
    g((A) => A && A.id === C && A.before === F ? A : { id: C, before: F });
  }, [c, m]), E = Ve(() => {
  }, []), _ = Ve((v, C, O) => {
    const F = r.map((N) => N.id), A = F.indexOf(v);
    if (A < 0) return;
    F.splice(A, 1);
    const D = F.indexOf(C);
    if (D < 0) {
      F.splice(A, 0, v);
      return;
    }
    const x = O ? D : D + 1;
    F.splice(x, 0, v), n("reorder", { order: F });
  }, [r, n]), k = Ve((v, C) => {
    if (!c || !m || m === C) return;
    v.preventDefault();
    const O = v.currentTarget.getBoundingClientRect(), F = v.clientX < O.left + O.width / 2;
    _(m, C, F), p(null), g(null);
  }, [c, m, _]), I = Ve(() => {
    p(null), g(null);
  }, []), w = {
    display: "grid",
    gridTemplateColumns: `repeat(${s}, 1fr)`,
    gap: "1rem"
  };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: o,
      className: "tlDashboard" + (c ? " tlDashboard--edit" : "")
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
}, { useCallback: Go, useRef: Sn, useState: Dn, useEffect: Xo, useLayoutEffect: qo } = e, Zo = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, a) => /* @__PURE__ */ e.createElement("span", { key: a, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(G, { control: n }))));
}, Qo = ({ group: l }) => {
  var m, p;
  const [t, n] = Dn(!1), [a, r] = Dn({}), o = Sn(null), s = Sn(null), u = Go(() => {
    n((h) => !h);
  }, []);
  qo(() => {
    if (!t) return;
    const h = () => {
      const g = o.current;
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
  }, [t]), Xo(() => {
    if (!t) return;
    const h = (g) => {
      s.current && !s.current.contains(g.target) && o.current && !o.current.contains(g.target) && n(!1);
    };
    return document.addEventListener("mousedown", h), () => document.removeEventListener("mousedown", h);
  }, [t]), Fe(t, { ESCAPE: () => n(!1) }), Gt(t, s, "first");
  const c = l.items.filter((h) => h != null);
  if (c.length === 0) return null;
  if (c.length === 1 && !((m = l.subGroups) != null && m.length) && !l.icon)
    return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(G, { control: c[0] })));
  const i = l.label ?? l.name, d = !!l.icon;
  return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--menu" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: o,
      type: "button",
      className: "tlToolbar__menuTrigger" + (d ? " tlToolbar__menuTrigger--icon" : ""),
      onMouseDown: (h) => h.preventDefault(),
      onClick: u,
      "aria-expanded": t,
      "aria-haspopup": "true",
      "aria-label": d ? i : void 0,
      title: d ? i : void 0
    },
    d ? /* @__PURE__ */ e.createElement(Se, { encoded: l.icon, className: "tlToolbar__menuIcon" }) : /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement("span", null, i), /* @__PURE__ */ e.createElement("svg", { className: "tlToolbar__chevron", viewBox: "0 0 24 24", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("polyline", { points: "6,9 12,15 18,9" })))
  ), Mn.createPortal(
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: s,
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
}, Jo = ({ controlId: l }) => {
  const a = (X().groups ?? []).filter((r) => r.items.some((o) => o != null));
  return a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, a.map((r, o) => /* @__PURE__ */ e.createElement(e.Fragment, { key: r.name }, o > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), r.display === "menu" ? /* @__PURE__ */ e.createElement(Qo, { group: r }) : /* @__PURE__ */ e.createElement(Zo, { group: r }))));
}, es = ({ frame: l, covered: t }) => {
  const [n, a] = rt(), r = [
    "tlTileStack__frame",
    t ? "tlTileStack__frame--covered" : "",
    n
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(ot, { host: a }, /* @__PURE__ */ e.createElement("div", { className: r }, /* @__PURE__ */ e.createElement(G, { control: l })));
}, ts = ({ controlId: l }) => {
  const t = X(), [n, a] = rt(), r = t.frames ?? [], o = t.activeIndex ?? 0;
  return /* @__PURE__ */ e.createElement(ot, { host: a }, /* @__PURE__ */ e.createElement("div", { id: l, className: n ? "tlTileStack " + n : "tlTileStack" }, r.map((s, u) => /* @__PURE__ */ e.createElement(es, { key: s.controlId, frame: s, covered: u !== o }))));
}, ns = ({ controlId: l }) => {
  const t = X(), n = ae(), a = t.content, r = t.breadcrumb ?? null;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAdaptiveDetail" }, r && r.length > 0 && /* @__PURE__ */ e.createElement("nav", { className: "tlAdaptiveDetail__breadcrumb", "aria-label": "Breadcrumb" }, r.map((o, s) => {
    const u = s === r.length - 1;
    return /* @__PURE__ */ e.createElement(e.Fragment, { key: o.depth }, s > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__sep" }, "›"), u ? /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__crumb tlAdaptiveDetail__crumb--current" }, o.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlAdaptiveDetail__crumb",
        onClick: () => n("navigate", { depth: o.depth })
      },
      o.label
    ));
  })), /* @__PURE__ */ e.createElement("div", { className: "tlAdaptiveDetail__content" }, a && /* @__PURE__ */ e.createElement(G, { control: a })));
}, ls = ({ controlId: l }) => {
  const n = X().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((a, r) => /* @__PURE__ */ e.createElement(G, { key: r, control: a })));
}, as = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), rs = {
  "js.sidebar.openDrawer": "Open navigation"
}, os = ({ controlId: l }) => {
  const t = ae(), n = me(rs);
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
K("TLButton", Ll);
K("TLUploadButton", xl);
K("TLToggleButton", Il);
K("TLTextInput", fl);
K("TLPasswordInput", bl);
K("TLNumberInput", El);
K("TLDatePicker", _l);
K("TLSelect", yl);
K("TLBooleanChoice", kl);
K("TLCheckbox", Tl);
K("TLCounter", jl);
K("TLTabBar", Pl);
K("TLFieldList", Bl);
K("TLAudioRecorder", Fl);
K("TLAudioPlayer", Hl);
K("TLFileUpload", Ul);
K("TLBinaryField", zl);
K("TLFileChips", Gl);
K("TLRelativeTime", Zl);
K("TLAnchor", Ql);
K("TLScrollLink", Jl);
K("TLAvatar", na);
K("TLDownload", aa);
K("TLPhotoCapture", oa);
K("TLPhotoViewer", ca);
K("TLPdfViewer", ua);
K("TLSplitPanel", da);
K("TLPanel", Ea);
K("TLInset", La);
K("TLMaximizeRoot", va);
K("TLDeckPane", _a);
K("TLSidebar", Ta);
K("TLStack", Ra);
K("TLGrid", xa);
K("TLCard", Ma);
K("TLAppBar", Ia);
K("TLBreadcrumb", Aa);
K("TLBottomBar", Ba);
K("TLDialog", $a);
K("TLDialogManager", Ua);
K("TLWindow", Ya);
K("TLDrawer", qa);
K("TLMenuRegion", Qa);
K("TLSnackbar", nr);
K("TLNoticeBar", ir);
K("TLMenu", dr);
K("TLAppShell", pr);
K("TLText", fr);
K("TLTableView", vr);
K("TLColumnSelect", Cr);
K("TLCalendar", Ar);
K("TLFormLayout", Vr);
K("TLFormGroup", Yr);
K("TLFormField", Zr);
K("TLResourceCell", Qr);
K("TLTreeView", so);
K("TLDropdownSelect", ho);
K("TLColorInput", Lo);
K("TLIconSelect", Oo);
K("TLDashboard", Yo);
K("TLToolbar", Jo);
K("TLTileStack", ts);
K("TLAdaptiveDetail", ns);
K("TLSlot", ls);
K("TLSlotContent", as);
K("TLDrawerToggle", os);
