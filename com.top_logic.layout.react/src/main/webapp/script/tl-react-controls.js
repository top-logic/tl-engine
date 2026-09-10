import { React as e, useTLFieldValue as Re, useTLCommand as ae, useTLState as X, useKeyboardBinding as me, useTLUpload as Ge, useFill as kt, FillBarrier as Ae, TLChild as G, useI18N as ue, useTLDataUrl as Xe, scrollToAnchor as il, useStandaloneKeyboardScope as $e, useFillHost as rt, FillProvider as ot, KeyboardScopeProvider as Gt, useFocusTrap as Xt, writeDragPayload as ul, dropPositionAt as dl, dragTypeAccepted as tn, readDragPayload as ml, CMD_VALUE_CHANGED as ut, anchoredOverlayProps as pl, register as V } from "tl-react-bridge";
const { useCallback: nn, useRef: fl } = e, hl = 300, bl = ({ controlId: l, state: t }) => {
  const [n, a, o] = Re({
    debounceMs: hl,
    sendOnBlur: t.sendValueOnBlur === !0
  }), u = ae(), s = fl(!1), i = nn(
    (N) => {
      s.current = !0, a(N.target.value);
    },
    [a]
  ), r = t.commitOnBlur === !0, c = nn(async () => {
    await o(), r && s.current && (s.current = !1, u("commit"));
  }, [o, r, u]), d = t.multiline === !0;
  if (t.editable === !1) {
    const N = "tlReactTextInput tlReactTextInput--immutable" + (d ? " tlReactTextInput--multiline" : "");
    return /* @__PURE__ */ e.createElement(
      "span",
      {
        id: l,
        className: N,
        style: d ? { whiteSpace: "pre-wrap" } : void 0
      },
      n ?? ""
    );
  }
  const m = t.hasError === !0, f = t.hasWarnings === !0, h = t.errorMessage, E = [
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
      onChange: i,
      onBlur: c,
      disabled: t.disabled === !0,
      className: E,
      "aria-invalid": m || void 0,
      title: m && h ? h : void 0
    }
  ) : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: n ?? "",
      placeholder: t.placeholder ?? void 0,
      onChange: i,
      onBlur: c,
      disabled: t.disabled === !0,
      className: E,
      "aria-invalid": m || void 0,
      title: m && h ? h : void 0
    }
  ));
}, { useCallback: ln } = e, gl = 300, El = ({ controlId: l, state: t }) => {
  const [n, a, o] = Re({ debounceMs: gl }), u = ln(
    (m) => {
      a(m.target.value);
    },
    [a]
  ), s = ln(() => {
    o();
  }, [o]);
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, "••••••••");
  const i = t.hasError === !0, r = t.hasWarnings === !0, c = t.errorMessage, d = [
    "tlReactTextInput",
    i ? "tlReactTextInput--error" : "",
    !i && r ? "tlReactTextInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "password",
      value: n ?? "",
      onChange: u,
      onBlur: s,
      disabled: t.disabled === !0,
      className: d,
      "aria-invalid": i || void 0,
      title: i && c ? c : void 0
    }
  ));
}, { useCallback: an } = e, vl = 300, _l = ({ controlId: l, state: t }) => {
  const [n, a, o] = Re({
    debounceMs: vl,
    sendOnBlur: t.sendValueOnBlur === !0
  }), u = an(
    (f) => {
      const h = f.target.value;
      a(h === "" ? null : h);
    },
    [a]
  ), s = an(() => {
    o();
  }, [o]), i = n == null ? "" : String(n);
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, i);
  const r = t.hasError === !0, c = t.hasWarnings === !0, d = t.errorMessage, m = [
    "tlReactNumberInput",
    r ? "tlReactNumberInput--error" : "",
    !r && c ? "tlReactNumberInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      inputMode: t.inputMode ?? "numeric",
      value: i,
      onChange: u,
      onBlur: s,
      disabled: t.disabled === !0,
      className: m,
      "aria-invalid": r || void 0,
      title: r && d ? d : void 0
    }
  ));
}, { useCallback: Cl } = e, yl = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), o = Cl(
    (r) => {
      a(r.target.value || null);
    },
    [a]
  );
  if (t.editable === !1) {
    const r = t.displayValue ?? n ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, r);
  }
  const u = t.hasError === !0, s = t.hasWarnings === !0, i = [
    "tlReactDatePicker",
    u ? "tlReactDatePicker--error" : "",
    !u && s ? "tlReactDatePicker--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: t.inputType ?? "date",
      value: n ?? "",
      onChange: o,
      disabled: t.disabled === !0,
      className: i,
      "aria-invalid": u || void 0
    }
  ));
}, { useCallback: wl } = e, kl = ({ controlId: l, state: t, config: n }) => {
  var d;
  const [a, o] = Re(), u = wl(
    (m) => {
      o(m.target.value || null);
    },
    [o]
  ), s = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const m = ((d = s.find((f) => f.value === a)) == null ? void 0 : d.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, m);
  }
  const i = t.hasError === !0, r = t.hasWarnings === !0, c = [
    "tlReactSelect",
    i ? "tlReactSelect--error" : "",
    !i && r ? "tlReactSelect--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "select",
    {
      value: a ?? "",
      onChange: u,
      disabled: t.disabled === !0,
      className: c,
      "aria-invalid": i || void 0
    },
    t.nullable !== !1 && /* @__PURE__ */ e.createElement("option", { value: "" }),
    s.map((m) => /* @__PURE__ */ e.createElement("option", { key: m.value, value: m.value }, m.label))
  ));
}, { useCallback: Nl } = e, Sl = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), o = t.options ?? [], u = t.presentation === "select", s = t.disabled === !0, i = t.hasError === !0, r = t.hasWarnings === !0, c = Nl(
    (f) => {
      const h = o[f];
      a(h ? h.value : null);
    },
    [o, a]
  ), d = o.findIndex((f) => f.value === (n ?? null));
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlBooleanChoice tlBooleanChoice--immutable" }, d >= 0 ? o[d].label : "");
  const m = [
    "tlBooleanChoice",
    i ? "tlBooleanChoice--error" : "",
    !i && r ? "tlBooleanChoice--warning" : ""
  ].filter(Boolean).join(" ");
  return u ? /* @__PURE__ */ e.createElement(
    "select",
    {
      id: l,
      className: m + " tlReactSelect",
      value: d >= 0 ? String(d) : "",
      disabled: s,
      "aria-invalid": i || void 0,
      onChange: (f) => c(Number(f.target.value))
    },
    d < 0 && /* @__PURE__ */ e.createElement("option", { value: "" }),
    o.map((f, h) => /* @__PURE__ */ e.createElement("option", { key: h, value: String(h) }, f.label))
  ) : /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: m + " tlBooleanChoice--radio",
      role: "radiogroup",
      "aria-invalid": i || void 0
    },
    o.map((f, h) => /* @__PURE__ */ e.createElement("label", { key: h, className: "tlBooleanChoice__option" }, /* @__PURE__ */ e.createElement(
      "input",
      {
        type: "radio",
        name: l,
        checked: d === h,
        disabled: s,
        onChange: () => c(h)
      }
    ), /* @__PURE__ */ e.createElement("span", { className: "tlBooleanChoice__label" }, f.label)))
  );
}, { useCallback: Dl, useRef: Tl, useEffect: Rl } = e, Ll = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), o = t.triState === !0, u = Tl(null);
  Rl(() => {
    u.current && (u.current.indeterminate = o && n !== !0 && n !== !1);
  }, [o, n]);
  const s = Dl(
    (d) => {
      if (!o) {
        a(d.target.checked);
        return;
      }
      a(n === !0 ? !1 : n === !1 ? null : !0);
    },
    [a, o, n]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement(
      "input",
      {
        type: "checkbox",
        id: l,
        ref: u,
        checked: n === !0,
        disabled: !0,
        className: "tlReactCheckbox tlReactCheckbox--immutable"
      }
    );
  const i = t.hasError === !0, r = t.hasWarnings === !0, c = [
    "tlReactCheckbox",
    i ? "tlReactCheckbox--error" : "",
    !i && r ? "tlReactCheckbox--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: l,
      ref: u,
      checked: n === !0,
      onChange: s,
      disabled: t.disabled === !0,
      className: c,
      "aria-invalid": i || void 0,
      "aria-checked": o && n !== !0 && n !== !1 ? "mixed" : n === !0
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
const { useCallback: xl } = e, Ml = ({ controlId: l, command: t, label: n, image: a, disabled: o, displayMode: u }) => {
  const s = X(), i = ae(), r = t ?? "click", c = n ?? s.label, d = a ?? s.image, m = o ?? s.disabled === !0, f = u ?? s.displayMode ?? "label-only", h = s.hidden === !0, E = s.tooltip, N = s.appearance, C = s.size, _ = s.cssClasses, y = s.navigateUrl, w = xl(() => {
    if (y) {
      window.location.assign(y);
      return;
    }
    i(r);
  }, [i, r, y]), x = s.keyGesture;
  me(x, () => m || h ? !1 : (w(), !0));
  const v = f === "icon-only", k = f === "label-only" || f === "icon-label" || v && !d, b = E ?? (v ? c : void 0), T = b ? `text:${b}` : void 0;
  return h ? null : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: w,
      disabled: m,
      className: "tlReactButton" + (v ? " tlReactButton--iconOnly" : "") + (f === "label-only" ? " tlReactButton--labelOnly" : "") + (N === "link" ? " tlReactButton--link" : "") + (N === "primary" ? " tlReactButton--primary" : "") + (C === "small" ? " tlReactButton--small" : "") + (C === "large" ? " tlReactButton--large" : "") + (_ ? " " + _ : ""),
      "data-tooltip": T,
      "aria-label": d || v ? c : void 0
    },
    d && /* @__PURE__ */ e.createElement(Ne, { encoded: d, className: "tlReactButton__image" }),
    k && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, c)
  );
}, Il = ({ controlId: l }) => {
  const t = X(), n = Ge(), a = e.useRef(null), [o, u] = e.useState(!1), s = t.label ?? "", i = t.image, r = t.disabled === !0, c = t.hidden === !0, d = t.displayMode ?? "label-only", m = t.appearance, f = t.accept, h = t.multiple === !0, E = e.useCallback(() => {
    var x;
    r || o || (x = a.current) == null || x.click();
  }, [r, o]), N = e.useCallback(async (x) => {
    const v = x.target.files;
    if (!v || v.length === 0) return;
    const k = new FormData();
    for (let b = 0; b < v.length; b++)
      k.append("file", v[b], v[b].name);
    x.target.value = "", u(!0);
    try {
      await n(k);
    } finally {
      u(!1);
    }
  }, [n]), C = d === "icon-only", _ = d === "icon-only" || d === "icon-label", y = d === "label-only" || d === "icon-label" || C && !i, w = r || o;
  return /* @__PURE__ */ e.createElement("span", { id: l, style: { display: "contents" } }, /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: a,
      type: "file",
      accept: f && f !== "*" ? f : void 0,
      multiple: h || void 0,
      onChange: N,
      style: { display: "none" }
    }
  ), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: E,
      disabled: w,
      style: c ? { display: "none" } : void 0,
      className: "tlReactButton" + (C ? " tlReactButton--iconOnly" : "") + (m === "link" ? " tlReactButton--link" : "") + (m === "primary" ? " tlReactButton--primary" : ""),
      "aria-label": C ? s : void 0
    },
    _ && i && /* @__PURE__ */ e.createElement(Ne, { encoded: i, className: "tlReactButton__image" }),
    y && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, s)
  ));
}, { useCallback: jl } = e, Pl = ({ controlId: l, command: t, label: n, active: a, disabled: o }) => {
  const u = X(), s = ae(), i = t ?? "click", r = n ?? u.label, c = a ?? u.active === !0, d = o ?? u.disabled === !0, m = jl(() => {
    s(i);
  }, [s, i]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: m,
      disabled: d,
      className: "tlReactButton" + (c ? " tlReactButtonActive" : "")
    },
    r
  );
}, Al = ({ controlId: l }) => {
  const t = X(), n = ae(), a = t.count ?? 0, o = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Bl } = e, Fl = ({ controlId: l }) => {
  const t = X(), n = ae(), a = kt(!0), o = t.tabs ?? [], u = t.activeTabId, s = Bl((i) => {
    i !== u && n("selectTab", { tabId: i });
  }, [n, u]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar " + a }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, o.map((i) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: i.id,
      role: "tab",
      "aria-selected": i.id === u,
      className: "tlReactTabBar__tab" + (i.id === u ? " tlReactTabBar__tab--active" : ""),
      onClick: () => s(i.id)
    },
    i.icon && /* @__PURE__ */ e.createElement(Ne, { encoded: i.icon, className: "tlReactTabBar__tabIcon" }),
    i.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, /* @__PURE__ */ e.createElement(Ae, null, t.activeContent && /* @__PURE__ */ e.createElement(G, { control: t.activeContent }))));
}, Ol = ({ controlId: l }) => {
  const t = X(), n = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((o, u) => /* @__PURE__ */ e.createElement("div", { key: u, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(G, { control: o })))));
}, $l = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Hl = ({ controlId: l }) => {
  const t = X(), n = Ge(), [a, o] = e.useState("idle"), [u, s] = e.useState(null), i = e.useRef(null), r = e.useRef([]), c = e.useRef(null), d = t.status ?? "idle", m = t.error, f = d === "received" ? "idle" : a !== "idle" ? a : d, h = e.useCallback(async () => {
    if (a === "recording") {
      const y = i.current;
      y && y.state !== "inactive" && y.stop();
      return;
    }
    if (a !== "uploading") {
      if (s(null), !window.isSecureContext || !navigator.mediaDevices) {
        s("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const y = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        c.current = y, r.current = [];
        const w = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", x = new MediaRecorder(y, w ? { mimeType: w } : void 0);
        i.current = x, x.ondataavailable = (v) => {
          v.data.size > 0 && r.current.push(v.data);
        }, x.onstop = async () => {
          y.getTracks().forEach((b) => b.stop()), c.current = null;
          const v = new Blob(r.current, { type: x.mimeType || "audio/webm" });
          if (r.current = [], v.size === 0) {
            o("idle");
            return;
          }
          o("uploading");
          const k = new FormData();
          k.append("audio", v, "recording.webm"), await n(k), o("idle");
        }, x.start(), o("recording");
      } catch (y) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", y), s("js.audioRecorder.error.denied"), o("idle");
      }
    }
  }, [a, n]), E = ue($l), N = f === "recording" ? E["js.audioRecorder.stop"] : f === "uploading" ? E["js.uploading"] : E["js.audioRecorder.record"], C = f === "uploading", _ = ["tlAudioRecorder__button"];
  return f === "recording" && _.push("tlAudioRecorder__button--recording"), f === "uploading" && _.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: _.join(" "),
      onClick: h,
      disabled: C,
      title: N,
      "aria-label": N
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${f === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), u && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, E[u]), m && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, m));
}, Wl = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Ul = ({ controlId: l }) => {
  const t = X(), n = Xe(), a = !!t.hasAudio, o = t.dataRevision ?? 0, [u, s] = e.useState(a ? "idle" : "disabled"), i = e.useRef(null), r = e.useRef(null), c = e.useRef(o);
  e.useEffect(() => {
    a ? u === "disabled" && s("idle") : (i.current && (i.current.pause(), i.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null), s("disabled"));
  }, [a]), e.useEffect(() => {
    o !== c.current && (c.current = o, i.current && (i.current.pause(), i.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null), (u === "playing" || u === "paused" || u === "loading") && s("idle"));
  }, [o]), e.useEffect(() => () => {
    i.current && (i.current.pause(), i.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null);
  }, []);
  const d = e.useCallback(async () => {
    if (u === "disabled" || u === "loading")
      return;
    if (u === "playing") {
      i.current && i.current.pause(), s("paused");
      return;
    }
    if (u === "paused" && i.current) {
      i.current.play(), s("playing");
      return;
    }
    if (!r.current) {
      s("loading");
      try {
        const C = await fetch(n);
        if (!C.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", C.status), s("idle");
          return;
        }
        const _ = await C.blob();
        r.current = URL.createObjectURL(_);
      } catch (C) {
        console.error("[TLAudioPlayer] Fetch error:", C), s("idle");
        return;
      }
    }
    const N = new Audio(r.current);
    i.current = N, N.onended = () => {
      s("idle");
    }, N.play(), s("playing");
  }, [u, n]), m = ue(Wl), f = u === "loading" ? m["js.loading"] : u === "playing" ? m["js.audioPlayer.pause"] : u === "disabled" ? m["js.audioPlayer.noAudio"] : m["js.audioPlayer.play"], h = u === "disabled" || u === "loading", E = ["tlAudioPlayer__button"];
  return u === "playing" && E.push("tlAudioPlayer__button--playing"), u === "loading" && E.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: E.join(" "),
      onClick: d,
      disabled: h,
      title: f,
      "aria-label": f
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${u === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Vl = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, zl = ({ controlId: l }) => {
  const t = X(), n = Ge(), [a, o] = e.useState("idle"), [u, s] = e.useState(!1), i = e.useRef(null), r = t.status ?? "idle", c = t.error, d = t.accept ?? "", m = r === "received" ? "idle" : a !== "idle" ? a : r, f = e.useCallback(async (v) => {
    o("uploading");
    const k = new FormData();
    k.append("file", v, v.name), await n(k), o("idle");
  }, [n]), h = e.useCallback((v) => {
    var b;
    const k = (b = v.target.files) == null ? void 0 : b[0];
    k && f(k);
  }, [f]), E = e.useCallback(() => {
    var v;
    a !== "uploading" && ((v = i.current) == null || v.click());
  }, [a]), N = e.useCallback((v) => {
    v.preventDefault(), v.stopPropagation(), s(!0);
  }, []), C = e.useCallback((v) => {
    v.preventDefault(), v.stopPropagation(), s(!1);
  }, []), _ = e.useCallback((v) => {
    var b;
    if (v.preventDefault(), v.stopPropagation(), s(!1), a === "uploading") return;
    const k = (b = v.dataTransfer.files) == null ? void 0 : b[0];
    k && f(k);
  }, [a, f]), y = m === "uploading", w = ue(Vl), x = m === "uploading" ? w["js.uploading"] : w["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${u ? " tlFileUpload--dragover" : ""}`,
      onDragOver: N,
      onDragLeave: C,
      onDrop: _
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: i,
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
        onClick: E,
        disabled: y,
        title: x,
        "aria-label": x
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    c && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, c)
  );
}, Kl = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, Yl = ({ controlId: l, state: t }) => {
  const a = X() ?? t ?? {}, o = Ge(), u = Xe(), s = ue(Kl), i = a.editable !== !1, r = !!a.hasData, c = a.fileName ?? "download", d = a.dataRevision ?? 0, m = a.accept ?? "", f = a.status ?? "idle", h = a.error ?? null, [E, N] = e.useState("idle"), [C, _] = e.useState(!1), [y, w] = e.useState(!1), x = e.useRef(null), v = e.useCallback(async () => {
    if (!(!r || y)) {
      w(!0);
      try {
        const P = u + (u.includes("?") ? "&" : "?") + "rev=" + d, I = await fetch(P);
        if (!I.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", I.status);
          return;
        }
        const D = await I.blob(), K = URL.createObjectURL(D), p = document.createElement("a");
        p.href = K, p.download = c, p.style.display = "none", document.body.appendChild(p), p.click(), document.body.removeChild(p), URL.revokeObjectURL(K);
      } catch (P) {
        console.error("[TLBinaryField] Fetch error:", P);
      } finally {
        w(!1);
      }
    }
  }, [r, y, u, d, c]), k = e.useCallback(async (P) => {
    N("uploading");
    const I = new FormData();
    I.append("file", P, P.name), await o(I), N("idle");
  }, [o]), b = (f === "received" ? "idle" : E !== "idle" ? E : f) === "uploading", T = e.useCallback((P) => {
    var D;
    const I = (D = P.target.files) == null ? void 0 : D[0];
    I && k(I);
  }, [k]), M = e.useCallback(() => {
    var P;
    b || (P = x.current) == null || P.click();
  }, [b]), R = e.useCallback((P) => {
    P.preventDefault(), P.stopPropagation(), _(!0);
  }, []), U = e.useCallback((P) => {
    P.preventDefault(), P.stopPropagation(), _(!1);
  }, []), Y = e.useCallback((P) => {
    var D;
    if (P.preventDefault(), P.stopPropagation(), _(!1), b) return;
    const I = (D = P.dataTransfer.files) == null ? void 0 : D[0];
    I && k(I);
  }, [b, k]), A = y ? s["js.downloading"] : s["js.download.file"].replace("{0}", c), H = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (y ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: v,
      disabled: y,
      title: A,
      "aria-label": A
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: c }, c));
  if (!i)
    return r ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, H) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, s["js.download.noFile"]));
  const B = b, j = b ? s["js.uploading"] : s["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlBinaryField tlFileUpload${C ? " tlFileUpload--dragover" : ""}`,
      onDragOver: R,
      onDragLeave: U,
      onDrop: Y
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: x,
        type: "file",
        accept: m || void 0,
        onChange: T,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (B ? " tlFileUpload__button--uploading" : ""),
        onClick: M,
        disabled: B,
        title: j,
        "aria-label": j
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    r && H,
    h && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, h)
  );
}, Gl = {
  "js.fileChips.add": "Add file",
  "js.fileChips.remove": "Remove {0}",
  "js.uploading": "Uploading…",
  "js.download.file": "Download {0}"
};
function Xl(l) {
  return l < 1024 ? l + " B" : l < 1024 * 1024 ? (l / 1024).toFixed(l < 10 * 1024 ? 1 : 0) + " KB" : (l / (1024 * 1024)).toFixed(1) + " MB";
}
const ql = ({ controlId: l }) => {
  const t = X(), n = ae(), a = Ge(), o = Xe(), u = ue(Gl), s = t.chips ?? [], i = t.editable === !0, [r, c] = e.useState(!1), [d, m] = e.useState(!1), f = e.useRef(null), h = e.useCallback(async (v) => {
    const k = Array.from(v);
    if (k.length !== 0) {
      c(!0);
      try {
        const b = new FormData();
        for (const T of k)
          b.append("file", T, T.name);
        await a(b);
      } finally {
        c(!1);
      }
    }
  }, [a]), E = e.useCallback(async (v) => {
    if (v.hasData)
      try {
        const k = o + "&key=" + encodeURIComponent(v.key), b = await fetch(k);
        if (!b.ok) {
          console.error("[TLFileChips] Failed to fetch data:", b.status);
          return;
        }
        const T = await b.blob(), M = URL.createObjectURL(T), R = document.createElement("a");
        R.href = M, R.download = v.name, R.style.display = "none", document.body.appendChild(R), R.click(), document.body.removeChild(R), URL.revokeObjectURL(M);
      } catch (k) {
        console.error("[TLFileChips] Fetch error:", k);
      }
  }, [o]), N = e.useCallback((v) => {
    v.target.files && h(v.target.files), v.target.value = "";
  }, [h]), C = e.useCallback(() => {
    var v;
    r || (v = f.current) == null || v.click();
  }, [r]), _ = e.useCallback((v) => {
    i && (v.preventDefault(), v.stopPropagation(), m(!0));
  }, [i]), y = e.useCallback((v) => {
    i && (v.preventDefault(), v.stopPropagation(), m(!1));
  }, [i]), w = e.useCallback((v) => {
    i && (v.preventDefault(), v.stopPropagation(), m(!1), !r && v.dataTransfer.files && h(v.dataTransfer.files));
  }, [i, r, h]), x = [
    "tlFileChips",
    i ? "tlFileChips--editable" : "",
    d ? "tlFileChips--dragover" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: x,
      onDragOver: _,
      onDragLeave: y,
      onDrop: w
    },
    s.map((v) => {
      const k = u["js.download.file"].replace("{0}", v.name), b = u["js.fileChips.remove"].replace("{0}", v.name);
      return /* @__PURE__ */ e.createElement("span", { key: v.key, className: "tlFileChip" }, /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__main",
          onClick: () => E(v),
          disabled: !v.hasData,
          title: v.hasData ? k : v.name
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
        /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__name" }, v.name),
        v.size != null && /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__size" }, Xl(v.size))
      ), i && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__remove",
          onClick: () => n("removeChip", { key: v.key }),
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
    i && /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: f,
        type: "file",
        multiple: !0,
        onChange: N,
        style: { display: "none" }
      }
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileChips__add" + (r ? " tlFileChips__add--uploading" : ""),
        onClick: C,
        disabled: r,
        title: r ? u["js.uploading"] : u["js.fileChips.add"]
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
      /* @__PURE__ */ e.createElement("span", null, r ? u["js.uploading"] : u["js.fileChips.add"])
    ))
  );
}, Zl = 3e4;
function Ql(l, t) {
  const n = Math.round((l - Date.now()) / 1e3), a = Math.abs(n), o = new Intl.RelativeTimeFormat(t, { numeric: "auto" });
  return a < 60 ? o.format(Math.trunc(n / 1), "second") : a < 3600 ? o.format(Math.trunc(n / 60), "minute") : a < 86400 ? o.format(Math.trunc(n / 3600), "hour") : a < 7 * 86400 ? o.format(Math.trunc(n / 86400), "day") : new Date(l).toLocaleDateString(t);
}
const Jl = ({ controlId: l }) => {
  const t = X(), n = t.timestamp, a = t.label ?? void 0, o = t.locale || navigator.language, [, u] = e.useState(0);
  return e.useEffect(() => {
    const s = setInterval(() => u((i) => i + 1), Zl);
    return () => clearInterval(s);
  }, []), n == null ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime tlRelativeTime--empty" }) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime", title: a }, Ql(n, o));
}, ea = ({ controlId: l }) => {
  const t = X(), n = t.anchor ?? void 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAnchor", "data-tl-anchor": n }, t.child && /* @__PURE__ */ e.createElement(G, { control: t.child }));
}, ta = ({ controlId: l }) => {
  const t = X(), n = t.target, a = t.label ?? "";
  if (n == null)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlScrollLink tlScrollLink--empty" });
  const o = (u) => {
    u.preventDefault(), il(n);
  };
  return /* @__PURE__ */ e.createElement("a", { id: l, className: "tlScrollLink", href: "#", onClick: o }, a);
};
function na(l) {
  const t = l.trim().split(/\s+/).filter(Boolean);
  return t.length === 0 ? "?" : t.length === 1 ? t[0].slice(0, 2).toUpperCase() : (t[0][0] + t[t.length - 1][0]).toUpperCase();
}
function la(l) {
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return Math.abs(t) % 360;
}
const aa = ({ controlId: l }) => {
  const n = X().name;
  return n ? /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlAvatar",
      style: { backgroundColor: `hsl(${la(n)}, 45%, 45%)` },
      title: n,
      "aria-label": n
    },
    na(n)
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlAvatar tlAvatar--empty" });
}, ra = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, oa = ({ controlId: l }) => {
  const t = X(), n = Xe(), a = ae(), o = !!t.hasData, u = t.dataRevision ?? 0, s = t.fileName ?? "download", i = !!t.clearable, [r, c] = e.useState(!1), d = e.useCallback(async () => {
    if (!(!o || r)) {
      c(!0);
      try {
        const E = n + (n.includes("?") ? "&" : "?") + "rev=" + u, N = await fetch(E);
        if (!N.ok) {
          console.error("[TLDownload] Failed to fetch data:", N.status);
          return;
        }
        const C = await N.blob(), _ = URL.createObjectURL(C), y = document.createElement("a");
        y.href = _, y.download = s, y.style.display = "none", document.body.appendChild(y), y.click(), document.body.removeChild(y), URL.revokeObjectURL(_);
      } catch (E) {
        console.error("[TLDownload] Fetch error:", E);
      } finally {
        c(!1);
      }
    }
  }, [o, r, n, u, s]), m = e.useCallback(async () => {
    o && await a("clear");
  }, [o, a]), f = ue(ra);
  if (!o)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, f["js.download.noFile"]));
  const h = r ? f["js.downloading"] : f["js.download.file"].replace("{0}", s);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (r ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: d,
      disabled: r,
      title: h,
      "aria-label": h
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: s }, s), i && /* @__PURE__ */ e.createElement(
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
}, sa = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, ca = ({ controlId: l }) => {
  const t = X(), n = Ge(), [a, o] = e.useState("idle"), [u, s] = e.useState(null), [i, r] = e.useState(!1), c = e.useRef(null), d = e.useRef(null), m = e.useRef(null), f = e.useRef(null), h = e.useRef(null), E = t.error, N = e.useMemo(
    () => {
      var R;
      return !!(window.isSecureContext && ((R = navigator.mediaDevices) != null && R.getUserMedia));
    },
    []
  ), C = e.useCallback(() => {
    d.current && (d.current.getTracks().forEach((R) => R.stop()), d.current = null), c.current && (c.current.srcObject = null);
  }, []), _ = e.useCallback(() => {
    C(), o("idle");
  }, [C]), y = e.useCallback(async () => {
    var R;
    if (a !== "uploading") {
      if (s(null), !N) {
        (R = f.current) == null || R.click();
        return;
      }
      try {
        const U = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        d.current = U, o("overlayOpen");
      } catch (U) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", U), s("js.photoCapture.error.denied"), o("idle");
      }
    }
  }, [a, N]), w = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const R = c.current, U = m.current;
    if (!R || !U)
      return;
    U.width = R.videoWidth, U.height = R.videoHeight;
    const Y = U.getContext("2d");
    Y && (Y.drawImage(R, 0, 0), C(), o("uploading"), U.toBlob(async (A) => {
      if (!A) {
        o("idle");
        return;
      }
      const H = new FormData();
      H.append("photo", A, "capture.jpg"), await n(H), o("idle");
    }, "image/jpeg", 0.85));
  }, [a, n, C]), x = e.useCallback(async (R) => {
    var A;
    const U = (A = R.target.files) == null ? void 0 : A[0];
    if (!U) return;
    o("uploading");
    const Y = new FormData();
    Y.append("photo", U, U.name), await n(Y), o("idle"), f.current && (f.current.value = "");
  }, [n]);
  e.useEffect(() => {
    a === "overlayOpen" && c.current && d.current && (c.current.srcObject = d.current);
  }, [a]), e.useEffect(() => {
    var U;
    if (a !== "overlayOpen") return;
    (U = h.current) == null || U.focus();
    const R = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = R;
    };
  }, [a]), $e(a === "overlayOpen", { ESCAPE: _ }), e.useEffect(() => () => {
    d.current && (d.current.getTracks().forEach((R) => R.stop()), d.current = null);
  }, []);
  const v = ue(sa), k = a === "uploading" ? v["js.uploading"] : v["js.photoCapture.open"], b = ["tlPhotoCapture__cameraBtn"];
  a === "uploading" && b.push("tlPhotoCapture__cameraBtn--uploading");
  const T = ["tlPhotoCapture__overlayVideo"];
  i && T.push("tlPhotoCapture__overlayVideo--mirrored");
  const M = ["tlPhotoCapture__mirrorBtn"];
  return i && M.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
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
  )), !N && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: f,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: x
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
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: _ }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: c,
        className: T.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: M.join(" "),
        onClick: () => r((R) => !R),
        title: v["js.photoCapture.mirror"],
        "aria-label": v["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: w,
        title: v["js.photoCapture.capture"],
        "aria-label": v["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: _,
        title: v["js.photoCapture.close"],
        "aria-label": v["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), u && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, v[u]), E && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, E));
}, ia = {
  "js.photoViewer.alt": "Captured photo"
}, ua = ({ controlId: l }) => {
  const t = X(), n = Xe(), a = !!t.hasPhoto, o = t.dataRevision ?? 0, [u, s] = e.useState(null), i = e.useRef(o);
  e.useEffect(() => {
    if (!a) {
      u && (URL.revokeObjectURL(u), s(null));
      return;
    }
    if (o === i.current && u)
      return;
    i.current = o, u && (URL.revokeObjectURL(u), s(null));
    let c = !1;
    return (async () => {
      try {
        const d = await fetch(n);
        if (!d.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", d.status);
          return;
        }
        const m = await d.blob();
        c || s(URL.createObjectURL(m));
      } catch (d) {
        console.error("[TLPhotoViewer] Fetch error:", d);
      }
    })(), () => {
      c = !0;
    };
  }, [a, o, n]), e.useEffect(() => () => {
    u && URL.revokeObjectURL(u);
  }, []);
  const r = ue(ia);
  return !a || !u ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: u,
      alt: t.alt || r["js.photoViewer.alt"]
    }
  ));
}, da = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, ma = ({ controlId: l }) => {
  const t = X(), n = Xe(), a = !!t.hasPdf, o = t.dataRevision ?? 0, u = ue(da), i = n.indexOf("react-api/"), r = i >= 0 ? n.slice(0, i) : n, c = n + "&rev=" + o, d = r + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(c);
  return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: d,
      title: u["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, u["js.pdfViewer.noDocument"]));
}, { useCallback: rn, useRef: Rt } = e, pa = ({ controlId: l }) => {
  const t = X(), n = ae(), a = kt(!0), o = t.orientation, u = t.resizable === !0, s = t.children ?? [], i = o === "horizontal", r = s.length > 0 && s.every((_) => _.collapsed), c = !r && s.some((_) => _.collapsed), d = r ? !i : i, m = Rt(null), f = Rt(null), h = Rt(null), E = rn((_, y) => {
    const w = {
      overflow: _.scrolling || "auto"
    };
    return _.collapsed ? r && !d ? w.flex = "1 0 0%" : w.flex = "0 0 auto" : y !== void 0 ? w.flex = `0 0 ${y}px` : w.flex = `${_.size} 1 0%`, _.minSize > 0 && !_.collapsed && (w.minWidth = i ? _.minSize : void 0, w.minHeight = i ? void 0 : _.minSize), w;
  }, [i, r, c, d]), N = rn((_, y) => {
    _.preventDefault();
    const w = m.current;
    if (!w) return;
    const x = s[y], v = s[y + 1], k = w.querySelectorAll(":scope > .tlSplitPanel__child"), b = [];
    k.forEach((R) => {
      b.push(i ? R.offsetWidth : R.offsetHeight);
    }), h.current = b, f.current = {
      splitterIndex: y,
      startPos: i ? _.clientX : _.clientY,
      startSizeBefore: b[y],
      startSizeAfter: b[y + 1],
      childBefore: x,
      childAfter: v
    };
    const T = (R) => {
      const U = f.current;
      if (!U || !h.current) return;
      const A = (i ? R.clientX : R.clientY) - U.startPos, H = U.childBefore.minSize || 0, B = U.childAfter.minSize || 0;
      let j = U.startSizeBefore + A, P = U.startSizeAfter - A;
      j < H && (P += j - H, j = H), P < B && (j += P - B, P = B), h.current[U.splitterIndex] = j, h.current[U.splitterIndex + 1] = P;
      const I = w.querySelectorAll(":scope > .tlSplitPanel__child"), D = I[U.splitterIndex], K = I[U.splitterIndex + 1];
      D && (D.style.flex = `0 0 ${j}px`), K && (K.style.flex = `0 0 ${P}px`);
    }, M = () => {
      if (document.removeEventListener("mousemove", T), document.removeEventListener("mouseup", M), document.body.style.cursor = "", document.body.style.userSelect = "", h.current) {
        const R = {};
        s.forEach((U, Y) => {
          const A = U.control;
          A != null && A.controlId && h.current && (R[A.controlId] = h.current[Y]);
        }), n("updateSizes", { sizes: R });
      }
      h.current = null, f.current = null;
    };
    document.addEventListener("mousemove", T), document.addEventListener("mouseup", M), document.body.style.cursor = i ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [s, i, n]), C = [];
  return s.forEach((_, y) => {
    if (C.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${y}`,
          className: `tlSplitPanel__child${_.collapsed && d ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: E(_)
        },
        /* @__PURE__ */ e.createElement(G, { control: _.control })
      )
    ), u && y < s.length - 1) {
      const w = s[y + 1];
      !_.collapsed && !w.collapsed && C.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${y}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${o}`,
            onMouseDown: (v) => N(v, y)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: m,
      id: l,
      className: `tlSplitPanel tlSplitPanel--${o}${r ? " tlSplitPanel--allCollapsed" : ""} ${a}`,
      style: {
        display: "flex",
        flexDirection: d ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    C
  );
}, $t = ({ image: l, className: t }) => {
  if (!l || l === "none") return null;
  const n = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `${t ? t + " " : ""}${n}`, "aria-hidden": "true" });
}, { useCallback: Lt } = e, fa = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, ha = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), ba = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), ga = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), Ea = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), va = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), _a = ({ controlId: l }) => {
  const t = X(), n = ae(), a = ue(fa), o = t.title, u = t.expansionState ?? "NORMALIZED", s = t.showMinimize === !0, i = t.showMaximize === !0, r = t.showPopOut === !0, c = t.fullLine === !0, d = t.fill === !0, m = t.hoverActions === !0, f = t.appearance === "card", h = t.errorMessage, E = u === "MINIMIZED", N = u === "MAXIMIZED", C = u === "HIDDEN", _ = Lt(() => {
    n("toggleMinimize");
  }, [n]), y = Lt(() => {
    n("toggleMaximize");
  }, [n]), w = Lt(() => {
    n("popOut");
  }, [n]), x = kt(d && !C);
  if (C)
    return null;
  const v = N ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" }, k = s && !N || i && !E || r, b = !!o && o.trim() !== "" || !!t.titleContent || !!t.toolbar || k;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${u.toLowerCase()}${c ? " tlPanel--fullLine" : ""}${x ? " " + x : ""}${m ? " tlPanel--hoverActions" : ""}${f ? " tlPanel--card" : ""}`,
      style: v
    },
    b && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, !!o && o.trim() !== "" && /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, o), t.titleContent && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__titleContent" }, /* @__PURE__ */ e.createElement(G, { control: t.titleContent })), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(G, { control: t.toolbar }), s && !N && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: _,
        title: E ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      E ? /* @__PURE__ */ e.createElement(ba, null) : /* @__PURE__ */ e.createElement(ha, null)
    ), i && !E && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: y,
        title: N ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      N ? /* @__PURE__ */ e.createElement(Ea, null) : /* @__PURE__ */ e.createElement(ga, null)
    ), r && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: w,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(va, null)
    ))),
    !E && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(Ae, null, /* @__PURE__ */ e.createElement(G, { control: t.child }))),
    !E && h && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error tlPanel__error", role: "alert" }, /* @__PURE__ */ e.createElement($t, { image: t.errorIcon, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, h)),
    !E && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(G, { control: t.buttonBar }))
  );
}, Ca = ({ controlId: l }) => {
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
}, ya = ({ controlId: l }) => {
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
}, { useCallback: ve, useState: Et, useEffect: Ht, useRef: _t } = e, wa = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function Wt(l, t, n, a) {
  const o = [];
  for (const u of l)
    if (u.type === "nav") {
      if (u.hidden) continue;
      o.push({ id: u.id, type: "nav", groupId: a });
    } else u.type === "command" ? o.push({ id: u.id, type: "command", groupId: a }) : u.type === "group" && (o.push({ id: u.id, type: "group" }), (n.get(u.id) ?? u.expanded) && !t && o.push(...Wt(u.children, t, n, u.id)));
  return o;
}
const Ye = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(Ne, { encoded: l, className: "tlSidebar__icon" }) : null, ka = ({ item: l, active: t, collapsed: n, onSelect: a, tabIndex: o, itemRef: u, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => a(l.id),
    title: n ? l.label : void 0,
    tabIndex: o,
    ref: u,
    onFocus: () => s(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Ye, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Ye, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), Na = ({ item: l, collapsed: t, onExecute: n, tabIndex: a, itemRef: o, onFocus: u }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: a,
    ref: o,
    onFocus: () => u(l.id)
  },
  /* @__PURE__ */ e.createElement(Ye, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), Sa = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Ye, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), Da = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), Ta = ({ item: l, activeItemId: t, anchorRect: n, onSelect: a, onExecute: o, onClose: u }) => {
  const s = _t(null);
  Ht(() => {
    const c = (d) => {
      s.current && !s.current.contains(d.target) && setTimeout(() => u(), 0);
    };
    return document.addEventListener("mousedown", c), () => document.removeEventListener("mousedown", c);
  }, [u]), $e(!0, { ESCAPE: u });
  const i = ve((c) => {
    c.type === "nav" ? (a(c.id), u()) : c.type === "command" && (o(c.id), u());
  }, [a, o, u]), r = {};
  return n && (r.left = n.right, r.top = n.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: s, role: "menu", style: r }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((c) => {
    if (c.type === "nav" && c.hidden) return null;
    if (c.type === "nav" || c.type === "command") {
      const d = c.type === "nav" && c.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: c.id,
          className: "tlSidebar__flyoutItem" + (d ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => i(c)
        },
        /* @__PURE__ */ e.createElement(Ye, { icon: c.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, c.label),
        c.type === "nav" && c.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, c.badge)
      );
    }
    return c.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: c.id, className: "tlSidebar__flyoutSectionHeader" }, c.label) : c.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: c.id, className: "tlSidebar__separator" }) : null;
  }));
}, Ra = ({
  item: l,
  expanded: t,
  activeItemId: n,
  collapsed: a,
  onSelect: o,
  onExecute: u,
  onToggleGroup: s,
  tabIndex: i,
  itemRef: r,
  onFocus: c,
  focusedId: d,
  setItemRef: m,
  onItemFocus: f,
  flyoutGroupId: h,
  onOpenFlyout: E,
  onCloseFlyout: N
}) => {
  const C = _t(null), [_, y] = Et(null), w = ve(() => {
    a ? h === l.id ? N() : (C.current && y(C.current.getBoundingClientRect()), E(l.id)) : s(l.id);
  }, [a, h, l.id, s, E, N]), x = ve((k) => {
    C.current = k, r(k);
  }, [r]), v = a && h === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (v ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: w,
      title: a ? l.label : void 0,
      "aria-expanded": a ? v : t,
      tabIndex: i,
      ref: x,
      onFocus: () => c(l.id)
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
  ), v && /* @__PURE__ */ e.createElement(
    Ta,
    {
      item: l,
      activeItemId: n,
      anchorRect: _,
      onSelect: o,
      onExecute: u,
      onClose: N
    }
  ), t && !a && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((k) => /* @__PURE__ */ e.createElement(
    Nn,
    {
      key: k.id,
      item: k,
      activeItemId: n,
      collapsed: a,
      onSelect: o,
      onExecute: u,
      onToggleGroup: s,
      focusedId: d,
      setItemRef: m,
      onItemFocus: f,
      groupStates: null,
      flyoutGroupId: h,
      onOpenFlyout: E,
      onCloseFlyout: N
    }
  ))));
}, Nn = ({
  item: l,
  activeItemId: t,
  collapsed: n,
  onSelect: a,
  onExecute: o,
  onToggleGroup: u,
  focusedId: s,
  setItemRef: i,
  onItemFocus: r,
  groupStates: c,
  flyoutGroupId: d,
  onOpenFlyout: m,
  onCloseFlyout: f
}) => {
  switch (l.type) {
    case "nav":
      return l.hidden ? null : /* @__PURE__ */ e.createElement(
        ka,
        {
          item: l,
          active: l.id === t,
          collapsed: n,
          onSelect: a,
          tabIndex: s === l.id ? 0 : -1,
          itemRef: i(l.id),
          onFocus: r
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        Na,
        {
          item: l,
          collapsed: n,
          onExecute: o,
          tabIndex: s === l.id ? 0 : -1,
          itemRef: i(l.id),
          onFocus: r
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(Sa, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(Da, null);
    case "group": {
      const h = c ? c.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        Ra,
        {
          item: l,
          expanded: h,
          activeItemId: t,
          collapsed: n,
          onSelect: a,
          onExecute: o,
          onToggleGroup: u,
          tabIndex: s === l.id ? 0 : -1,
          itemRef: i(l.id),
          onFocus: r,
          focusedId: s,
          setItemRef: i,
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
}, La = ({ controlId: l }) => {
  const t = X(), n = ae(), a = ue(wa), o = t.items ?? [], u = t.activeItemId, s = t.collapsed, i = t.drawerOpen, r = i ? !1 : s, [c, d] = Et(() => {
    const A = /* @__PURE__ */ new Map(), H = (B) => {
      for (const j of B)
        j.type === "group" && (A.set(j.id, j.expanded), H(j.children));
    };
    return H(o), A;
  }), m = ve((A) => {
    d((H) => {
      const B = new Map(H), j = B.get(A) ?? !1;
      return B.set(A, !j), n("toggleGroup", { itemId: A, expanded: !j }), B;
    });
  }, [n]), f = ve((A) => {
    A !== u && n("selectItem", { itemId: A });
  }, [n, u]), h = ve((A) => {
    n("executeCommand", { itemId: A });
  }, [n]), E = ve(() => {
    n("toggleCollapse", {});
  }, [n]), N = ve(() => {
    n("toggleDrawer", {});
  }, [n]), [C, _] = Et(null), y = ve((A) => {
    _(A);
  }, []), w = ve(() => {
    _(null);
  }, []);
  Ht(() => {
    r || _(null);
  }, [r]);
  const [x, v] = Et(() => {
    const A = Wt(o, r, c);
    return A.length > 0 ? A[0].id : "";
  }), k = _t(/* @__PURE__ */ new Map()), b = ve((A) => (H) => {
    H ? k.current.set(A, H) : k.current.delete(A);
  }, []), T = ve((A) => {
    v(A);
  }, []), M = _t(0), R = ve((A) => {
    v(A), M.current++;
  }, []);
  Ht(() => {
    const A = k.current.get(x);
    A && document.activeElement !== A && A.focus();
  }, [x, M.current]);
  const U = ve((A) => {
    if (A.key === "Escape" && C !== null) {
      A.preventDefault(), w();
      return;
    }
    const H = Wt(o, r, c);
    if (H.length === 0) return;
    const B = H.findIndex((P) => P.id === x);
    if (B < 0) return;
    const j = H[B];
    switch (A.key) {
      case "ArrowDown": {
        A.preventDefault();
        const P = (B + 1) % H.length;
        R(H[P].id);
        break;
      }
      case "ArrowUp": {
        A.preventDefault();
        const P = (B - 1 + H.length) % H.length;
        R(H[P].id);
        break;
      }
      case "Home": {
        A.preventDefault(), R(H[0].id);
        break;
      }
      case "End": {
        A.preventDefault(), R(H[H.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        A.preventDefault(), j.type === "nav" ? f(j.id) : j.type === "command" ? h(j.id) : j.type === "group" && (r ? C === j.id ? w() : y(j.id) : m(j.id));
        break;
      }
      case "ArrowRight": {
        j.type === "group" && !r && ((c.get(j.id) ?? !1) || (A.preventDefault(), m(j.id)));
        break;
      }
      case "ArrowLeft": {
        j.type === "group" && !r && (c.get(j.id) ?? !1) && (A.preventDefault(), m(j.id));
        break;
      }
    }
  }, [
    o,
    r,
    c,
    x,
    C,
    R,
    f,
    h,
    m,
    y,
    w
  ]), Y = "tlSidebar" + (r ? " tlSidebar--collapsed" : "") + (i ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: Y }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(G, { control: t.drawerToggleContribution }), i && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: N, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": a["js.sidebar.ariaLabel"] }, r ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(G, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(G, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: U }, o.map((A) => /* @__PURE__ */ e.createElement(
    Nn,
    {
      key: A.id,
      item: A,
      activeItemId: u,
      collapsed: r,
      onSelect: f,
      onExecute: h,
      onToggleGroup: m,
      focusedId: x,
      setItemRef: b,
      onItemFocus: T,
      groupStates: c,
      flyoutGroupId: C,
      onOpenFlyout: y,
      onCloseFlyout: w
    }
  ))), r ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(G, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(G, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: E,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, /* @__PURE__ */ e.createElement(Ae, null, t.activeContent && /* @__PURE__ */ e.createElement(G, { control: t.activeContent }))));
}, xa = ({ controlId: l }) => {
  const t = X(), n = t.direction ?? "column", a = t.gap ?? "default", o = t.align ?? "stretch", u = t.wrap === !0, s = t.growFirst === !0, i = t.children ?? [], [r, c] = rt(), d = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${a}`,
    `tlStack--align-${o}`,
    u ? "tlStack--wrap" : "",
    s ? "tlStack--grow-first" : "",
    r,
    t.cssClass ?? ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(ot, { host: c }, /* @__PURE__ */ e.createElement("div", { id: l, className: d }, i.map((m, f) => /* @__PURE__ */ e.createElement(G, { key: f, control: m }))));
}, Ma = ({ controlId: l }) => {
  const t = X(), [n, a] = rt();
  return /* @__PURE__ */ e.createElement(ot, { host: a }, /* @__PURE__ */ e.createElement("div", { id: l, className: n ? "tlInset " + n : "tlInset" }, t.child && /* @__PURE__ */ e.createElement(G, { control: t.child })));
}, Ia = ({ controlId: l }) => {
  const t = X(), n = t.columns, a = t.minColumnWidth, o = t.gap ?? "default", u = t.children ?? [], s = {};
  return a ? s.gridTemplateColumns = `repeat(auto-fit, minmax(min(${a}, 100%), 1fr))` : n && (s.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${o}`, style: s }, u.map((i, r) => /* @__PURE__ */ e.createElement(G, { key: r, control: i })));
}, ja = ({ controlId: l }) => {
  const t = X(), n = t.title, a = t.variant ?? "outlined", o = t.padding ?? "default", u = t.headerActions ?? [], s = t.child, i = n != null || u.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${a}` }, i && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), u.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, u.map((r, c) => /* @__PURE__ */ e.createElement(G, { key: c, control: r })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${o}` }, /* @__PURE__ */ e.createElement(Ae, null, /* @__PURE__ */ e.createElement(G, { control: s }))));
}, Pa = ({ controlId: l }) => {
  const t = X(), n = t.title ?? "", a = t.leading, o = t.trailing, u = t.children ?? [], s = t.actions ?? [], i = t.variant ?? "flat", c = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    i === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: c }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(G, { control: a })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), u.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, u.map((d, m) => /* @__PURE__ */ e.createElement(G, { key: m, control: d }))), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((d, m) => /* @__PURE__ */ e.createElement(G, { key: m, control: d }))), o && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__trailing" }, /* @__PURE__ */ e.createElement(G, { control: o })));
}, { useCallback: Aa } = e, Ba = ({ controlId: l }) => {
  const t = X(), n = ae(), a = t.items ?? [], o = Aa((u) => {
    n("navigate", { itemId: u });
  }, [n]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, a.map((u, s) => {
    const i = s === a.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: u.id, className: "tlBreadcrumb__entry" }, s > 0 && /* @__PURE__ */ e.createElement(
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
    ), i ? /* @__PURE__ */ e.createElement("span", { className: "tlBreadcrumb__current", "aria-current": "page" }, u.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlBreadcrumb__item",
        onClick: () => o(u.id)
      },
      u.label
    ));
  })));
}, { useCallback: Fa } = e, Oa = ({ controlId: l }) => {
  const t = X(), n = ae(), a = t.items ?? [], o = t.activeItemId, u = Fa((s) => {
    s !== o && n("selectItem", { itemId: s });
  }, [n, o]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, a.map((s) => {
    const i = s.id === o;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: s.id,
        type: "button",
        className: "tlBottomBar__item" + (i ? " tlBottomBar__item--active" : ""),
        onClick: () => u(s.id),
        "aria-current": i ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + s.icon, "aria-hidden": "true" }), s.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, s.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, s.label)
    );
  }));
}, { useCallback: on, useRef: $a } = e, Ha = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), Wa = ({ controlId: l }) => {
  const t = X(), n = ae(), a = t.open === !0, o = t.closeOnBackdrop !== !1, u = t.child, s = $a(null), i = on(() => {
    n("close");
  }, [n]), r = on((c) => {
    o && c.target === c.currentTarget && i();
  }, [o, i]);
  return a ? /* @__PURE__ */ e.createElement(Gt, null, /* @__PURE__ */ e.createElement(Ha, { onClose: i }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: r,
      ref: s,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(Ae, null, /* @__PURE__ */ e.createElement(G, { control: u }))
  )) : null;
}, { useEffect: Ua, useRef: Va } = e, za = ({ controlId: l }) => {
  const n = X().dialogs ?? [], a = Va(n.length);
  return Ua(() => {
    n.length < a.current && n.length > 0, a.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((o) => /* @__PURE__ */ e.createElement(G, { key: o.controlId, control: o })));
}, { useCallback: dt, useRef: We, useState: mt } = e, Ka = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), Ya = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Ga = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Xa = ({ controlId: l }) => {
  const t = X(), n = ae(), a = ue(Ya), o = t.title ?? "", u = t.width ?? "32rem", s = t.height ?? null, i = t.minHeight ?? null, r = t.resizable === !0, c = t.child, d = t.actions ?? [], m = t.toolbar, f = t.buttonBar, [h, E] = mt(null), [N, C] = mt(null), [_, y] = mt(null), w = We(null), [x, v] = mt(!1), k = We(null), b = We(null), T = We(null), M = We(null), R = We(null), U = dt(() => {
    n("close");
  }, [n]);
  Xt(!0, M, "field");
  const Y = dt((P, I) => {
    I.preventDefault();
    const D = M.current;
    if (!D) return;
    const K = D.getBoundingClientRect(), p = !w.current, L = w.current ?? { x: K.left, y: K.top };
    p && (w.current = L, y(L)), R.current = {
      dir: P,
      startX: I.clientX,
      startY: I.clientY,
      startW: K.width,
      startH: K.height,
      startPos: { ...L },
      symmetric: p
    };
    const z = (Z) => {
      const F = R.current;
      if (!F) return;
      const te = Z.clientX - F.startX, se = Z.clientY - F.startY;
      let le = F.startW, Ee = F.startH, we = 0, pe = 0;
      F.symmetric ? (F.dir.includes("e") && (le = F.startW + 2 * te), F.dir.includes("w") && (le = F.startW - 2 * te), F.dir.includes("s") && (Ee = F.startH + 2 * se), F.dir.includes("n") && (Ee = F.startH - 2 * se)) : (F.dir.includes("e") && (le = F.startW + te), F.dir.includes("w") && (le = F.startW - te, we = te), F.dir.includes("s") && (Ee = F.startH + se), F.dir.includes("n") && (Ee = F.startH - se, pe = se));
      const Te = Math.max(200, le), Se = Math.max(100, Ee);
      F.symmetric ? (we = (F.startW - Te) / 2, pe = (F.startH - Se) / 2) : (F.dir.includes("w") && Te === 200 && (we = F.startW - 200), F.dir.includes("n") && Se === 100 && (pe = F.startH - 100)), b.current = Te, T.current = Se, E(Te), C(Se);
      const He = {
        x: F.startPos.x + we,
        y: F.startPos.y + pe
      };
      w.current = He, y(He);
    }, W = () => {
      document.removeEventListener("mousemove", z), document.removeEventListener("mouseup", W);
      const Z = b.current, F = T.current;
      (Z != null || F != null) && n("resize", {
        ...Z != null ? { width: Math.round(Z) } : {},
        ...F != null ? { height: Math.round(F) } : {}
      }), R.current = null;
    };
    document.addEventListener("mousemove", z), document.addEventListener("mouseup", W);
  }, [n]), A = dt((P) => {
    if (P.button !== 0 || P.target.closest("button")) return;
    P.preventDefault();
    const I = M.current;
    if (!I) return;
    const D = I.getBoundingClientRect(), K = w.current ?? { x: D.left, y: D.top }, p = P.clientX - K.x, L = P.clientY - K.y, z = (Z) => {
      const F = window.innerWidth, te = window.innerHeight;
      let se = Z.clientX - p, le = Z.clientY - L;
      const Ee = I.offsetWidth, we = I.offsetHeight;
      se + Ee > F && (se = F - Ee), le + we > te && (le = te - we), se < 0 && (se = 0), le < 0 && (le = 0);
      const pe = { x: se, y: le };
      w.current = pe, y(pe);
    }, W = () => {
      document.removeEventListener("mousemove", z), document.removeEventListener("mouseup", W);
    };
    document.addEventListener("mousemove", z), document.addEventListener("mouseup", W);
  }, []), H = dt(() => {
    var P, I;
    if (x) {
      const D = k.current;
      D && (y(D.x !== -1 ? { x: D.x, y: D.y } : null), E(D.w), C(D.h)), v(!1);
    } else {
      const D = M.current, K = D == null ? void 0 : D.getBoundingClientRect();
      k.current = {
        x: ((P = w.current) == null ? void 0 : P.x) ?? (K == null ? void 0 : K.left) ?? -1,
        y: ((I = w.current) == null ? void 0 : I.y) ?? (K == null ? void 0 : K.top) ?? -1,
        w: h ?? (K == null ? void 0 : K.width) ?? null,
        h: N ?? null
      }, v(!0), y({ x: 0, y: 0 }), E(null), C(null);
    }
  }, [x, h, N]), B = x ? { position: "absolute", top: 0, left: 0, width: "100vw", maxWidth: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: h != null ? h + "px" : u,
    ...N != null ? { height: N + "px" } : s != null ? { height: s } : {},
    ...i != null && N == null ? { minHeight: i } : {},
    maxHeight: _ ? "100vh" : "80vh",
    ..._ ? { position: "absolute", left: _.x + "px", top: _.y + "px" } : {}
  }, j = l + "-title";
  return /* @__PURE__ */ e.createElement(Gt, { modal: !0 }, /* @__PURE__ */ e.createElement(Ka, { onClose: U }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: B,
      ref: M,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": j
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${x ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: x ? void 0 : A,
        onDoubleClick: r ? H : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: j }, o),
      m && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(G, { control: m })),
      r && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: H,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(Ae, null, /* @__PURE__ */ e.createElement(G, { control: c }))),
    (d.length > 0 || f) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, f && /* @__PURE__ */ e.createElement(G, { control: f }), d.map((P, I) => /* @__PURE__ */ e.createElement(G, { key: I, control: P }))),
    r && !x && Ga.map((P) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: P,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${P}`,
        onMouseDown: (I) => Y(P, I)
      }
    ))
  ));
}, { useCallback: qa } = e, Za = {
  "js.drawer.close": "Close"
}, Qa = ({ controlId: l }) => {
  const t = X(), n = ae(), a = ue(Za), o = t.open === !0, u = t.position ?? "right", s = t.size ?? "medium", i = t.title ?? null, r = t.child, c = qa(() => {
    n("close");
  }, [n]);
  $e(o, { ESCAPE: c });
  const d = [
    "tlDrawer",
    `tlDrawer--${u}`,
    `tlDrawer--${s}`,
    o ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: l, className: d, "aria-hidden": !o }, i !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, i), /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, /* @__PURE__ */ e.createElement(Ae, null, r && /* @__PURE__ */ e.createElement(G, { control: r }))));
}, { useCallback: pt, useRef: Ja } = e, er = ({ controlId: l }) => {
  const t = X(), n = ae(), a = Ja(null), o = t.child, s = (t.trigger ?? "contextmenu") === "click", i = pt((m) => {
    m.preventDefault(), m.stopPropagation(), n("openMenu", { x: m.clientX, y: m.clientY });
  }, [n]), r = pt(() => {
    var f;
    const m = (f = a.current) == null ? void 0 : f.getBoundingClientRect();
    n("openMenu", {
      x: Math.round(m ? m.left : 0),
      y: Math.round(m ? m.bottom : 0)
    });
  }, [n]), c = pt((m) => {
    m.preventDefault(), m.stopPropagation(), r();
  }, [r]), d = pt((m) => {
    (m.key === "Enter" || m.key === " ") && (m.preventDefault(), r());
  }, [r]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenuRegion" + (s ? " tlMenuRegion--click" : ""),
      ref: a,
      onContextMenu: s ? void 0 : i,
      onClick: s ? c : void 0,
      role: s ? "button" : void 0,
      tabIndex: s ? 0 : void 0,
      "aria-haspopup": s ? "menu" : void 0,
      onKeyDown: s ? d : void 0
    },
    o && /* @__PURE__ */ e.createElement(G, { control: o })
  );
}, { useCallback: tr, useEffect: sn, useRef: nr, useState: cn } = e, lr = 250, ar = ({ controlId: l }) => {
  const t = X(), n = ae(), a = t.message ?? "", o = t.content ?? "", u = t.variant ?? "info", s = t.duration ?? 5e3, i = t.visible === !0, r = t.generation ?? 0, [c, d] = cn(!1), [m, f] = cn(!1), h = nr(!1);
  sn(() => {
    h.current = !1;
  }, [r]);
  const E = tr(() => {
    d(!0), setTimeout(() => {
      n("dismiss", { generation: r }), d(!1);
    }, 200);
  }, [n, r]);
  return sn(() => {
    if (!i || s === 0 || m) return;
    const N = setTimeout(E, h.current ? lr : s);
    return () => clearTimeout(N);
  }, [i, s, m, E]), !i && !c ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${u}${c ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite",
      onMouseEnter: () => {
        h.current = !0, f(!0);
      },
      onMouseLeave: () => f(!1)
    },
    o ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: o } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, a)
  );
}, { useCallback: rr, useEffect: un, useMemo: or, useRef: sr, useState: cr } = e, ir = 1e3;
function ur(l) {
  const t = Math.max(0, Math.floor(l / 1e3)), n = t % 60, a = Math.floor(t / 60) % 60, o = Math.floor(t / 3600), u = (s) => s < 10 ? `0${s}` : `${s}`;
  return o > 0 ? `${o}:${u(a)}:${u(n)}` : `${a}:${u(n)}`;
}
const dr = ({ controlId: l }) => {
  const t = X(), n = ae(), a = t.visible === !0, o = t.severity ?? "info", u = t.text ?? "", s = t.deadline ?? null, i = t.serverNow ?? null, r = t.leadMs ?? null, c = t.actionLabel ?? null, d = t.pingGraceMs ?? null, m = or(
    () => i != null ? i - Date.now() : 0,
    [i]
  ), [f, h] = cr(0), E = a && s != null;
  un(() => {
    if (!E) return;
    const x = setInterval(() => h((v) => v + 1), ir);
    return () => clearInterval(x);
  }, [E, s]);
  const N = sr(null);
  un(() => {
    !E || d == null || s == null || N.current !== s && (Date.now() + m < s + d || (N.current = s, n("deadlinePassed", {})));
  }, [f, E, s, d, m, n]);
  const C = rr(() => {
    c != null && n("action", {});
  }, [n, c]);
  if (!a) return null;
  const _ = s != null ? s - (Date.now() + m) : null;
  if (r != null && _ != null && _ > r) return null;
  const y = _ != null ? ur(_) : null, w = c != null;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlNoticeBar tlNoticeBar--${o}${w ? " tlNoticeBar--clickable" : ""}`,
      role: w ? "button" : "status",
      "aria-live": "polite",
      tabIndex: w ? 0 : void 0,
      title: c ?? void 0,
      "aria-label": w ? `${u} ${c}` : void 0,
      onClick: w ? C : void 0,
      onKeyDown: w ? (x) => {
        (x.key === "Enter" || x.key === " ") && (x.preventDefault(), C());
      } : void 0
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__text" }, u),
    y !== null && /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__countdown" }, y)
  );
}, { useCallback: xt, useEffect: dn, useRef: mr, useState: mn } = e, pr = ({ controlId: l }) => {
  const t = X(), n = ae(), a = t.open === !0, o = t.anchorId, u = t.anchorX, s = t.anchorY, i = t.items ?? [], r = mr(null), [c, d] = mn({ top: 0, left: 0 }), [m, f] = mn(0), h = i.filter((_) => _.type === "item" && !_.disabled);
  dn(() => {
    var b, T;
    if (!a) return;
    const _ = ((b = r.current) == null ? void 0 : b.offsetHeight) ?? 200, y = ((T = r.current) == null ? void 0 : T.offsetWidth) ?? 200;
    if (u != null && s != null) {
      let M = s, R = u;
      M + _ > window.innerHeight && (M = Math.max(0, window.innerHeight - _)), R + y > window.innerWidth && (R = Math.max(0, window.innerWidth - y)), d({ top: M, left: R }), f(0);
      return;
    }
    if (!o) return;
    const w = document.getElementById(o);
    if (!w) return;
    const x = w.getBoundingClientRect();
    let v = x.bottom + 4, k = x.left;
    v + _ > window.innerHeight && (v = x.top - _ - 4), k + y > window.innerWidth && (k = x.right - y), d({ top: v, left: k }), f(0);
  }, [a, o, u, s]);
  const E = xt(() => {
    n("close");
  }, [n]), N = xt((_) => {
    n("selectItem", { itemId: _ });
  }, [n]);
  dn(() => {
    if (!a) return;
    const _ = (y) => {
      r.current && !r.current.contains(y.target) && E();
    };
    return document.addEventListener("mousedown", _), () => document.removeEventListener("mousedown", _);
  }, [a, E]);
  const C = xt((_) => {
    if (_.key === "Escape") {
      _.preventDefault(), E();
      return;
    }
    if (_.key === "ArrowDown")
      _.preventDefault(), f((y) => (y + 1) % h.length);
    else if (_.key === "ArrowUp")
      _.preventDefault(), f((y) => (y - 1 + h.length) % h.length);
    else if (_.key === "Enter" || _.key === " ") {
      _.preventDefault();
      const y = h[m];
      y && N(y.id);
    }
  }, [E, N, h, m]);
  return Xt(a, r), a ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: r,
      tabIndex: -1,
      style: { position: "fixed", top: c.top, left: c.left },
      onKeyDown: C
    },
    i.map((_, y) => {
      if (_.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: y, className: "tlMenu__separator" });
      const x = h.indexOf(_) === m;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: _.id,
          type: "button",
          className: "tlMenu__item" + (x ? " tlMenu__item--focused" : "") + (_.disabled ? " tlMenu__item--disabled" : "") + (_.cssClasses ? " " + _.cssClasses : ""),
          role: "menuitem",
          disabled: _.disabled,
          tabIndex: x ? 0 : -1,
          onClick: () => N(_.id)
        },
        _.icon && /* @__PURE__ */ e.createElement(Ne, { encoded: _.icon, className: "tlMenu__icon" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, _.label)
      );
    })
  ) : null;
}, fr = 768, hr = ({ controlId: l }) => {
  const t = X(), n = ae(), a = kt(!0);
  e.useEffect(() => {
    const c = window.matchMedia(`(max-width: ${fr}px)`), d = (f) => {
      n("reportDisplayClass", { displayClass: f ? "COMPACT" : "REGULAR" });
    };
    d(c.matches);
    const m = (f) => d(f.matches);
    return c.addEventListener("change", m), () => c.removeEventListener("change", m);
  }, [n]);
  const o = t.header, u = t.notices, s = t.content, i = t.footer, r = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell " + a }, o && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(G, { control: o })), u && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__notices" }, /* @__PURE__ */ e.createElement(G, { control: u })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(Ae, null, /* @__PURE__ */ e.createElement(G, { control: s }))), i && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(G, { control: i })), /* @__PURE__ */ e.createElement(G, { control: r }));
}, br = ({ controlId: l }) => {
  const t = X(), n = t.text ?? "", a = t.cssClass ?? "", o = t.hasTooltip === !0, u = t.role || void 0, s = a ? `tlText ${a}` : "tlText";
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: s,
      role: u,
      "data-tooltip": o ? "key:tooltip" : void 0
    },
    n
  );
}, gr = ({ isMulti: l, cursorIndex: t, onMove: n, onToggle: a, onSelectAll: o }) => (me("ArrowUp", () => (n("up", !1, !1), !0)), me("ArrowDown", () => (n("down", !1, !1), !0)), me("Home", () => (n("home", !1, !1), !0)), me("End", () => (n("end", !1, !1), !0)), me("PageUp", () => (n("pageUp", !1, !1), !0)), me("PageDown", () => (n("pageDown", !1, !1), !0)), me("Shift+ArrowUp", () => (n("up", l, !1), !0)), me("Shift+ArrowDown", () => (n("down", l, !1), !0)), me("Shift+Home", () => (n("home", l, !1), !0)), me("Shift+End", () => (n("end", l, !1), !0)), me("Shift+PageUp", () => (n("pageUp", l, !1), !0)), me("Shift+PageDown", () => (n("pageDown", l, !1), !0)), me("Ctrl+ArrowUp", () => (n("up", !1, l), !0)), me("Ctrl+ArrowDown", () => (n("down", !1, l), !0)), me("Space", () => t < 0 ? !1 : (a(), !0)), me("Ctrl+A", () => l ? (o(), !0) : !1), null), Er = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all",
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
}, vr = 300, pn = 50, _r = 'input, textarea, select, button, a, [contenteditable="true"], [role="combobox"], [role="listbox"], [role="option"], [role="button"], [role="link"], [role="checkbox"], [role="radio"], [role="switch"], [role="textbox"], [role="spinbutton"], [role="slider"], [role="menu"], [role="menuitem"]';
function ft(l) {
  var n;
  const t = l.target;
  return !!((n = t == null ? void 0 : t.closest) != null && n.call(t, _r));
}
const Ut = 'input:not([disabled]):not([readonly]), textarea:not([disabled]):not([readonly]), select:not([disabled]), [contenteditable="true"]', Cr = Ut + ", button:not([disabled]), a[href]";
function Sn(l, t) {
  return Array.from(l.querySelectorAll("[data-row][data-col]")).filter((n) => n.dataset.row === t);
}
function Mt(l, t, n = {}) {
  const a = Sn(l, t);
  if (n.col) {
    const u = a.find((i) => i.dataset.col === n.col), s = u == null ? void 0 : u.querySelector(Ut);
    if (s) return s;
  }
  if (n.col)
    return null;
  const o = n.last ? [...a].reverse() : a;
  for (const u of o) {
    const s = u.querySelector(Ut);
    if (s) return s;
  }
  return null;
}
const yr = ({ controlId: l }) => {
  const t = X(), n = ae(), a = ue(Er), o = e.useRef(null);
  e.useEffect(() => {
    const g = o.current;
    if (!g) return;
    const S = (O) => {
      const Q = O.detail;
      let ee = Q.target;
      for (; ee && ee !== g; ) {
        const oe = ee.dataset.row, ne = ee.dataset.col;
        if (oe != null && ne != null) {
          Q.resolved = { key: oe + "|" + ne };
          return;
        }
        ee = ee.parentElement;
      }
    };
    return g.addEventListener("tl-tooltip-resolve", S), () => g.removeEventListener("tl-tooltip-resolve", S);
  }, []);
  const u = t.columns ?? [], s = t.totalRowCount ?? 0, i = t.rows ?? [], r = t.rowHeight ?? 36, c = t.selectionMode ?? "single", d = t.selectedCount ?? 0, m = t.cursorIndex ?? -1, f = t.frozenColumnCount ?? 0, h = t.treeMode ?? !1, E = t.columnSelect ?? !1, N = t.filterBar ?? !1, C = t.namedFilters ?? [], _ = t.activeNamedFilter ?? "", y = t.search ?? "", w = t.filterSaving ?? !1, x = t.dragEnabled ?? !1, v = t.dragType ?? "", k = t.dropAccepts ?? [], b = t.dropOnRows ?? !1, T = e.useMemo(
    () => u.filter((g) => g.sortPriority && g.sortPriority > 0).length,
    [u]
  ), M = c === "multi", R = 40, U = 20, Y = e.useRef(null), A = e.useRef(null), H = e.useRef(null), B = e.useRef(null), j = e.useRef(null), [P, I] = e.useState({}), D = e.useRef(null), K = e.useRef(!1), p = e.useRef(null), [L, z] = e.useState(null), [W, Z] = e.useState(null), [F, te] = e.useState(null), [se, le] = e.useState(null), [Ee, we] = e.useState(0);
  e.useEffect(() => {
    const g = H.current;
    if (!g)
      return;
    const S = () => {
      const Q = g.offsetWidth - g.clientWidth;
      we((ee) => ee === Q ? ee : Q);
    };
    S();
    const O = new ResizeObserver(S);
    return O.observe(g), () => O.disconnect();
  }, []), e.useEffect(() => {
    D.current || I({});
  }, [u]);
  const pe = e.useCallback((g) => P[g.name] ?? g.width, [P]), Te = e.useMemo(() => {
    const g = [];
    let S = M && f > 0 ? R : 0;
    for (let O = 0; O < f && O < u.length; O++)
      g.push(S), S += pe(u[O]);
    return g;
  }, [u, f, M, R, pe]), Se = e.useMemo(() => {
    if (f <= 0)
      return 0;
    let g = M ? R : 0;
    for (let S = 0; S < f && S < u.length; S++)
      g += pe(u[S]);
    return g;
  }, [u, f, M, R, pe]), He = s * r, Le = e.useRef(null), $ = e.useCallback((g, S, O) => {
    O.preventDefault(), O.stopPropagation(), D.current = { column: g, startX: O.clientX, startWidth: S };
    let Q = O.clientX, ee = 0;
    const oe = () => {
      const ce = D.current;
      if (!ce) return;
      const de = Math.max(pn, ce.startWidth + (Q - ce.startX) + ee);
      I((Ce) => ({ ...Ce, [ce.column]: de }));
    }, ne = () => {
      const ce = H.current, de = Y.current;
      if (!ce || !D.current) return;
      const Ce = ce.getBoundingClientRect(), Me = 40, Jt = 8, cl = ce.scrollLeft;
      Q > Ce.right - Me ? ce.scrollLeft += Jt : Q < Ce.left + Me && (ce.scrollLeft = Math.max(0, ce.scrollLeft - Jt));
      const en = ce.scrollLeft - cl;
      en !== 0 && (de && (de.scrollLeft = ce.scrollLeft), ee += en, oe()), Le.current = requestAnimationFrame(ne);
    };
    Le.current = requestAnimationFrame(ne);
    const he = (ce) => {
      Q = ce.clientX, oe();
    }, fe = (ce) => {
      document.removeEventListener("mousemove", he), document.removeEventListener("mouseup", fe), Le.current !== null && (cancelAnimationFrame(Le.current), Le.current = null);
      const de = D.current;
      if (de) {
        const Ce = Math.max(pn, de.startWidth + (ce.clientX - de.startX) + ee);
        n("columnResize", { column: de.column, width: Ce }), D.current = null, K.current = !0, requestAnimationFrame(() => {
          K.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", he), document.addEventListener("mouseup", fe);
  }, [n]), q = e.useCallback(() => {
    Y.current && H.current && (Y.current.scrollLeft = H.current.scrollLeft), B.current !== null && clearTimeout(B.current), B.current = window.setTimeout(() => {
      const g = H.current;
      if (!g) return;
      const S = g.scrollTop, O = Math.ceil(g.clientHeight / r), Q = Math.floor(S / r);
      n("scroll", { start: Q, count: O });
    }, 80);
  }, [n, r]), re = e.useCallback((g, S, O) => {
    if (K.current) return;
    let Q;
    !S || S === "desc" ? Q = "asc" : Q = "desc";
    const ee = O.shiftKey ? "add" : "replace";
    n("sort", { column: g, direction: Q, mode: ee });
  }, [n]), ie = e.useCallback((g, S) => {
    p.current = g, S.dataTransfer.effectAllowed = "move", S.dataTransfer.setData("text/plain", g);
  }, []), qe = e.useCallback((g, S) => {
    if (!p.current || p.current === g) {
      z(null);
      return;
    }
    S.preventDefault(), S.dataTransfer.dropEffect = "move";
    const O = S.currentTarget.getBoundingClientRect(), Q = S.clientX < O.left + O.width / 2 ? "left" : "right";
    z({ column: g, side: Q });
  }, []), st = e.useCallback((g) => {
    g.preventDefault(), g.stopPropagation();
    const S = p.current;
    if (!S || !L) {
      p.current = null, z(null);
      return;
    }
    let O = u.findIndex((ee) => ee.name === L.column);
    if (O < 0) {
      p.current = null, z(null);
      return;
    }
    const Q = u.findIndex((ee) => ee.name === S);
    L.side === "right" && O++, Q < O && O--, n("columnReorder", { column: S, targetIndex: O }), p.current = null, z(null);
  }, [u, L, n]), jn = e.useCallback(() => {
    p.current = null, z(null);
  }, []), Pn = e.useCallback((g, S) => {
    if (ft(S)) {
      S.preventDefault();
      return;
    }
    ul(S.dataTransfer, {
      source: l,
      keys: [g.id],
      selection: g.selected,
      type: v
    });
  }, [l, v]), ct = e.useCallback(
    (g) => {
      if (b && g.target instanceof Element) {
        const S = g.target.closest(".tlTableView__row"), O = S == null ? void 0 : S.dataset.dropRow;
        if (S && O)
          return { row: O, position: dl(g.clientY, S) };
      }
      return { row: null, position: "none" };
    },
    [b]
  ), An = e.useCallback((g) => {
    if (p.current) {
      g.preventDefault();
      const O = H.current, Q = Y.current;
      if (!O) return;
      const ee = O.getBoundingClientRect(), oe = 40, ne = 8;
      g.clientX < ee.left + oe ? O.scrollLeft = Math.max(0, O.scrollLeft - ne) : g.clientX > ee.right - oe && (O.scrollLeft += ne), Q && (Q.scrollLeft = O.scrollLeft);
      return;
    }
    if (!tn(g.dataTransfer, k))
      return;
    g.preventDefault(), g.dataTransfer.dropEffect = "move";
    const S = ct(g);
    Z((O) => O && O.row === S.row && O.position === S.position ? O : S);
  }, [k, ct]), Bn = e.useCallback((g) => {
    g.currentTarget.contains(g.relatedTarget) || Z(null);
  }, []), Fn = e.useCallback((g) => {
    if (!tn(g.dataTransfer, k)) {
      st(g);
      return;
    }
    g.preventDefault(), g.stopPropagation();
    const S = ml(g.dataTransfer), O = ct(g);
    if (Z(null), S) {
      const Q = {
        source: S.source,
        // Comma-separated: the command argument is a formatted string list, and a row key holds
        // no comma.
        keys: S.keys.join(","),
        selection: S.selection,
        position: O.position
      };
      O.row && (Q.targetKey = O.row), n("drop", Q);
    }
  }, [k, ct, st, n]), On = e.useCallback((g, S) => {
    var ee, oe, ne, he;
    const O = window.getSelection();
    if (O && !O.isCollapsed && S.currentTarget.contains(O.anchorNode))
      return;
    if (!ft(S) && ((ee = H.current) == null || ee.focus({ preventScroll: !0 }), !S.ctrlKey && !S.metaKey && !S.shiftKey)) {
      const fe = (he = (ne = (oe = S.target) == null ? void 0 : oe.closest) == null ? void 0 : ne.call(oe, "[data-col]")) == null ? void 0 : he.getAttribute("data-col");
      j.current = { index: g, col: fe ?? void 0 };
    }
    const Q = i.find((fe) => fe.index === g);
    ft(S) && (Q != null && Q.selected) && !S.ctrlKey && !S.metaKey && !S.shiftKey || n("select", {
      rowIndex: g,
      ctrlKey: S.ctrlKey || S.metaKey,
      shiftKey: S.shiftKey
    });
  }, [n, i]), $n = e.useCallback((g, S, O) => {
    n("moveSelection", { direction: g, extend: S, move: O });
  }, [n]), Hn = e.useCallback(() => {
    m < 0 || n("select", { rowIndex: m, ctrlKey: M, shiftKey: !1 });
  }, [n, m, M]), Wn = e.useCallback(() => {
    n("selectAll", { selected: !0 });
  }, [n]), Un = e.useCallback(
    () => !!o.current && o.current.contains(document.activeElement),
    []
  );
  e.useEffect(() => {
    if (m < 0)
      return;
    const g = H.current;
    if (!g)
      return;
    const S = m * r, O = S + r;
    S < g.scrollTop ? g.scrollTop = S : O > g.scrollTop + g.clientHeight && (g.scrollTop = O - g.clientHeight);
  }, [m, r]), e.useEffect(() => {
    const g = j.current, S = H.current;
    if (!g || !S)
      return;
    const O = i.find((oe) => oe.index === g.index);
    if (!O || !Mt(S, O.id))
      return;
    j.current = null;
    const Q = document.activeElement;
    if (Q && Q !== document.body && !S.contains(Q))
      return;
    const ee = Mt(S, O.id, { col: g.col, last: g.last });
    ee && (ee.focus({ preventScroll: !0 }), ee instanceof HTMLInputElement && ee.select());
  }, [i]);
  const Vn = e.useCallback((g) => {
    if (g.key !== "Tab")
      return;
    const S = H.current, O = document.activeElement;
    if (!S || !O || !S.contains(O))
      return;
    const Q = O.closest("[data-row][data-col]");
    if (!Q)
      return;
    const ee = Q.dataset.row, oe = i.find((Me) => Me.id === ee);
    if (!oe)
      return;
    const ne = Sn(S, ee).flatMap((Me) => Array.from(Me.querySelectorAll(Cr))), he = ne.indexOf(O);
    if (he < 0)
      return;
    const fe = !g.shiftKey;
    if (!(fe ? he === ne.length - 1 : he === 0))
      return;
    const de = fe ? oe.index + 1 : oe.index - 1;
    if (de < 0 || de >= s)
      return;
    const Ce = i.find((Me) => Me.index === de);
    Ce && Mt(S, Ce.id) || (g.preventDefault(), j.current = { index: de, last: !fe }, n("select", { rowIndex: de, ctrlKey: !1, shiftKey: !1 }));
  }, [i, s, n]), zn = e.useCallback((g, S) => {
    S.stopPropagation(), n("select", { rowIndex: g, ctrlKey: !0, shiftKey: !1 });
  }, [n]), Kn = e.useCallback(() => {
    const g = d === s && s > 0;
    n("selectAll", { selected: !g });
  }, [n, d, s]), Yn = e.useCallback((g, S, O) => {
    O.stopPropagation(), n("expand", { rowIndex: g, expanded: S });
  }, [n]), Gn = e.useCallback((g, S) => {
    S.preventDefault(), te({ x: S.clientX, y: S.clientY, colIdx: g });
  }, []), Xn = e.useCallback(() => {
    F && (n("setFrozenColumnCount", { count: F.colIdx + 1 }), te(null));
  }, [F, n]), qn = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), te(null);
  }, [n]), Zn = e.useCallback((g) => {
    g.preventDefault(), g.stopPropagation();
    const S = A.current, O = Y.current;
    if (!S || !O)
      return;
    const Q = S.clientWidth, ee = [{ x: 0, count: 0 }];
    O.querySelectorAll("[data-col-idx]").forEach((fe) => {
      const ce = fe.getBoundingClientRect().right - S.getBoundingClientRect().left;
      ce > 0 && ce <= Q && ee.push({ x: ce, count: Number(fe.dataset.colIdx) + 1 });
    });
    let oe = { x: Se, count: f };
    const ne = (fe) => {
      const ce = fe.clientX - S.getBoundingClientRect().left;
      oe = ee.reduce(
        (de, Ce) => Math.abs(Ce.x - ce) < Math.abs(de.x - ce) ? Ce : de,
        ee[0]
      ), le(oe);
    }, he = () => {
      document.removeEventListener("mousemove", ne), document.removeEventListener("mouseup", he), le(null), oe.count !== f && n("setFrozenColumnCount", { count: oe.count });
    };
    document.addEventListener("mousemove", ne), document.addEventListener("mouseup", he);
  }, [Se, f, n]);
  e.useEffect(() => {
    if (!F) return;
    const g = () => te(null);
    return document.addEventListener("mousedown", g), () => document.removeEventListener("mousedown", g);
  }, [F]), $e(!!F, { ESCAPE: () => te(null) });
  const Qn = e.useCallback((g, S) => {
    S.stopPropagation(), S.preventDefault(), n("openFilter", { column: g });
  }, [n]), Jn = e.useCallback((g) => {
    g.stopPropagation(), g.preventDefault(), n("openColumnSelect", {});
  }, [n]), [el, Zt] = e.useState(y), Nt = e.useRef(!1), xe = e.useRef(null);
  e.useEffect(() => {
    Nt.current || Zt(y);
  }, [y]), e.useEffect(() => () => {
    xe.current !== null && clearTimeout(xe.current);
  }, []);
  const it = e.useCallback((g) => {
    xe.current !== null && (clearTimeout(xe.current), xe.current = null), Nt.current = !1, n("search", { term: g });
  }, [n]), tl = e.useCallback((g) => {
    Zt(g), Nt.current = !0, xe.current !== null && clearTimeout(xe.current), xe.current = window.setTimeout(() => it(g), vr);
  }, [it]), nl = e.useCallback((g) => {
    g.key === "Enter" && (g.preventDefault(), it(g.currentTarget.value));
  }, [it]), ll = e.useCallback((g) => {
    g === _ ? n("clearFilter", {}) : n("applyNamedFilter", { id: g });
  }, [_, n]), al = e.useCallback((g, S) => {
    S.stopPropagation(), n("deleteNamedFilter", { id: g });
  }, [n]), [Ze, Qe] = e.useState(null), St = e.useCallback(() => {
    const g = (Ze ?? "").trim();
    g && (n("saveNamedFilter", { filterName: g }), Qe(null));
  }, [Ze, n]), rl = e.useCallback((g) => {
    g.key === "Enter" ? (g.preventDefault(), St()) : g.key === "Escape" && (g.preventDefault(), Qe(null));
  }, [St]), Dt = u.reduce((g, S) => g + pe(S), 0) + (M ? R : 0), Tt = E ? 32 : 0, ol = d === s && s > 0, Qt = d > 0 && d < s, sl = e.useCallback((g) => {
    g && (g.indeterminate = Qt);
  }, [Qt]);
  return /* @__PURE__ */ e.createElement(Gt, { active: Un }, /* @__PURE__ */ e.createElement(
    gr,
    {
      isMulti: M,
      cursorIndex: m,
      onMove: $n,
      onToggle: Hn,
      onSelectAll: Wn
    }
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: o,
      id: l,
      className: "tlTableView" + (W && W.row === null ? " tlTableView--dragover" : ""),
      "data-tooltip": "dynamic",
      onDragOver: An,
      onDragLeave: Bn,
      onDrop: Fn
    },
    N && /* @__PURE__ */ e.createElement("div", { className: "tlTableView__filterBar" }, C.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlTableView__filterChips" }, C.map((g) => {
      const S = g.id === _;
      return /* @__PURE__ */ e.createElement(
        "span",
        {
          key: g.id,
          className: "tlTableView__chip" + (S ? " tlTableView__chip--active" : "")
        },
        /* @__PURE__ */ e.createElement(
          "button",
          {
            type: "button",
            className: "tlTableView__chipLabel",
            "aria-pressed": S,
            title: S ? a["js.table.clearFilter"] : g.label,
            onClick: () => ll(g.id)
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
            onClick: (O) => al(g.id, O)
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
        value: el,
        onChange: (g) => tl(g.target.value),
        onKeyDown: nl
      }
    )), w && (Ze === null ? /* @__PURE__ */ e.createElement(
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
        onChange: (g) => Qe(g.target.value),
        onKeyDown: rl
      }
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__barButton",
        title: a["js.table.saveFilter"],
        "aria-label": a["js.table.saveFilter"],
        disabled: !Ze.trim(),
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
        onClick: () => Qe(null)
      },
      /* @__PURE__ */ e.createElement("i", { className: "bi bi-x-lg" })
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerArea", ref: A }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: Y }, /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerRow",
        style: { width: Dt, paddingRight: Tt + Ee }
      },
      M && /* @__PURE__ */ e.createElement(
        "div",
        {
          className: "tlTableView__headerCell tlTableView__checkboxCell" + (f > 0 ? " tlTableView__headerCell--frozen" : ""),
          style: {
            width: R,
            minWidth: R,
            ...f > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
          },
          onDragOver: (g) => {
            p.current && (g.preventDefault(), g.dataTransfer.dropEffect = "move", u.length > 0 && u[0].name !== p.current && z({ column: u[0].name, side: "left" }));
          }
        },
        /* @__PURE__ */ e.createElement(
          "input",
          {
            type: "checkbox",
            ref: sl,
            className: "tlTableView__checkbox",
            checked: ol,
            onChange: Kn
          }
        )
      ),
      u.map((g, S) => {
        const O = pe(g);
        u.length - 1;
        let Q = "tlTableView__headerCell";
        g.sortable && (Q += " tlTableView__headerCell--sortable"), L && L.column === g.name && (Q += " tlTableView__headerCell--dragOver-" + L.side);
        const ee = S < f, oe = S === f - 1;
        return ee && (Q += " tlTableView__headerCell--frozen"), oe && (Q += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
          "div",
          {
            key: g.name,
            className: Q,
            "data-col-idx": S,
            style: {
              width: O,
              minWidth: O,
              position: ee ? "sticky" : "relative",
              ...ee ? { left: Te[S], zIndex: 2 } : {}
            },
            draggable: !0,
            onClick: g.sortable ? (ne) => re(g.name, g.sortDirection, ne) : void 0,
            onContextMenu: (ne) => Gn(S, ne),
            onDragStart: (ne) => ie(g.name, ne),
            onDragOver: (ne) => qe(g.name, ne),
            onDrop: st,
            onDragEnd: jn
          },
          /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, g.label),
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
              onMouseDown: (ne) => ne.stopPropagation(),
              onClick: (ne) => Qn(g.name, ne)
            },
            /* @__PURE__ */ e.createElement("i", { className: g.filterActive ? "bi bi-funnel-fill" : "bi bi-funnel" })
          ),
          g.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, g.sortDirection === "asc" ? "▲" : "▼", T > 1 && g.sortPriority != null && g.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, g.sortPriority)),
          /* @__PURE__ */ e.createElement(
            "div",
            {
              className: "tlTableView__resizeHandle",
              onMouseDown: (ne) => $(g.name, O, ne)
            }
          )
        );
      }),
      /* @__PURE__ */ e.createElement(
        "div",
        {
          style: { flex: "0 0 0", minHeight: "100%" },
          onDragOver: (g) => {
            if (p.current && u.length > 0) {
              const S = u[u.length - 1];
              S.name !== p.current && (g.preventDefault(), g.dataTransfer.dropEffect = "move", z({ column: S.name, side: "right" }));
            }
          },
          onDrop: st
        }
      )
    )), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__frozenSplitter" + (se ? " tlTableView__frozenSplitter--active" : ""),
        style: { left: Se },
        title: a["js.table.freezeSplitter"],
        onMouseDown: Zn
      }
    ), E && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__columnsButton",
        title: a["js.table.columns"],
        "aria-label": a["js.table.columns"],
        onClick: Jn
      },
      /* @__PURE__ */ e.createElement("i", { className: "bi bi-gear" })
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: H,
        className: "tlTableView__body",
        onScroll: q,
        onKeyDown: Vn,
        tabIndex: 0
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: He, position: "relative", width: Dt, paddingRight: Tt } }, i.map((g) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: g.id,
          "data-drop-row": g.id,
          draggable: x,
          className: "tlTableView__row" + (g.selected ? " tlTableView__row--selected" : "") + (g.index === m ? " tlTableView__row--cursor" : "") + (W && W.row === g.id ? " tlTableView__row--dragOver-" + W.position : ""),
          style: {
            position: "absolute",
            top: g.index * r,
            height: r,
            width: Dt,
            paddingRight: Tt,
            ...g.index === m ? { outline: "2px solid var(--color-primary, #1a73e8)", outlineOffset: "-2px" } : {}
          },
          onMouseDown: (S) => {
            (S.shiftKey || S.ctrlKey || S.metaKey || S.detail > 1) && !ft(S) && S.preventDefault();
          },
          onClick: (S) => On(g.index, S),
          onDragStart: x ? (S) => Pn(g, S) : void 0,
          onDragEnd: x ? () => Z(null) : void 0
        },
        M && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (f > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: R,
              minWidth: R,
              ...f > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (S) => S.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: g.selected,
              onChange: () => {
              },
              onClick: (S) => zn(g.index, S),
              tabIndex: -1
            }
          )
        ),
        u.map((S, O) => {
          const Q = pe(S), ee = O === u.length - 1, oe = O < f, ne = O === f - 1;
          let he = "tlTableView__cell";
          oe && (he += " tlTableView__cell--frozen"), ne && (he += " tlTableView__cell--frozenLast");
          const fe = h && O === 0, ce = g.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: S.name,
              className: he,
              "data-row": g.id,
              "data-col": S.name,
              style: {
                ...ee && !oe ? { flex: "1 0 auto", minWidth: Q } : { width: Q, minWidth: Q },
                ...oe ? { position: "sticky", left: Te[O], zIndex: 2 } : {}
              }
            },
            fe ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ce * U } }, g.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (de) => Yn(g.index, !g.expanded, de)
              },
              g.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), g.cells[S.name] && /* @__PURE__ */ e.createElement(G, { control: g.cells[S.name] })) : g.cells[S.name] && /* @__PURE__ */ e.createElement(G, { control: g.cells[S.name] })
          );
        })
      )))
    ),
    se && /* @__PURE__ */ e.createElement("div", { className: "tlTableView__frozenPreview", style: { left: se.x } }),
    F && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: F.y, left: F.x, zIndex: 1e4 },
        onMouseDown: (g) => g.stopPropagation()
      },
      F.colIdx + 1 !== f && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Xn }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      f > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: qn }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
    )
  ));
}, wr = {
  "js.table.columnSearch": "Find column"
}, kr = ({ controlId: l }) => {
  const t = X(), n = ae(), a = ue(wr), o = t.entries ?? [], u = o.filter((v) => v.visible).length, [s, i] = e.useState(""), r = s.trim().toLowerCase(), c = r ? o.filter((v) => v.label.toLowerCase().includes(r)) : o, d = e.useRef(null), m = e.useRef(null), [f, h] = e.useState(null), E = e.useCallback((v) => {
    m.current = v, h(v);
  }, []), N = e.useCallback((v, k) => {
    n("columnVisible", { column: v, visible: k });
  }, [n]), C = e.useCallback((v, k) => {
    d.current = v, k.dataTransfer.effectAllowed = "move", k.dataTransfer.setData("text/plain", v);
  }, []), _ = e.useCallback((v, k) => {
    if (!d.current || d.current === v) {
      E(null);
      return;
    }
    k.preventDefault(), k.dataTransfer.dropEffect = "move";
    const b = k.currentTarget.getBoundingClientRect(), T = k.clientY < b.top + b.height / 2 ? "top" : "bottom";
    E({ name: v, side: T });
  }, [E]), y = e.useCallback(() => {
    d.current = null, E(null);
  }, [E]), w = e.useCallback((v) => {
    v.preventDefault();
    const k = d.current, b = m.current;
    if (d.current = null, E(null), !k || !b)
      return;
    const T = o.findIndex((U) => U.name === b.name), M = o.findIndex((U) => U.name === k);
    if (T < 0 || M < 0)
      return;
    let R = b.side === "top" ? T : T + 1;
    M < R && R--, R !== M && n("columnReorder", { column: k, targetIndex: R });
  }, [o, n, E]), x = o.length > 10;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlColumnSelect", onDrop: w }, x && /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__search" }, /* @__PURE__ */ e.createElement("i", { className: "bi bi-search", "aria-hidden": "true" }), /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "search",
      className: "tlColumnSelect__searchInput",
      placeholder: a["js.table.columnSearch"],
      "aria-label": a["js.table.columnSearch"],
      value: s,
      onChange: (v) => i(v.target.value)
    }
  )), /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__list" + (x ? " tlColumnSelect__list--fixed" : "") }, c.map((v) => {
    const k = v.visible && u <= 1;
    let b = "tlColumnSelect__row";
    return f && f.name === v.name && (b += " tlColumnSelect__row--dragOver-" + f.side), /* @__PURE__ */ e.createElement(
      "div",
      {
        key: v.name,
        className: b,
        draggable: !0,
        onDragStart: (T) => C(v.name, T),
        onDragOver: (T) => _(v.name, T),
        onDrop: w,
        onDragEnd: y
      },
      /* @__PURE__ */ e.createElement("i", { className: "tlColumnSelect__handle bi bi-grip-vertical", "aria-hidden": "true" }),
      /* @__PURE__ */ e.createElement("label", { className: "tlColumnSelect__label" }, /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          className: "tlReactCheckbox",
          checked: v.visible,
          disabled: k,
          onChange: (T) => N(v.name, T.target.checked)
        }
      ), /* @__PURE__ */ e.createElement("span", null, v.label))
    );
  })));
}, { useState: Vt, useRef: lt, useCallback: vt, useMemo: Fe, useEffect: fn } = e, Nr = {
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
}, _e = 44, Ct = 15, ye = 6e4, Sr = 36e5, Pe = 864e5, Dr = 8;
function De(l) {
  const t = new Date(l);
  return t.setHours(0, 0, 0, 0), t.getTime();
}
function Ke(l, t) {
  const n = new Date(l);
  return n.setDate(n.getDate() + t), n.getTime();
}
function Tr(l) {
  return De(l);
}
function at(l, t) {
  return De(l) === De(t);
}
function je(l) {
  return (l - De(l)) / ye;
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
  return "tlCalEvent--c" + Math.abs(t) % Dr;
}
function wt(l, t) {
  return l.color ? { ...t, "--cal-ev-bg": l.color } : t;
}
function Rr(l) {
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
function Lr(l, t) {
  const n = { hour: "numeric", minute: "2-digit" };
  return Oe(l, n, t.start) + "–" + Oe(l, n, t.end);
}
const xr = [
  { key: "DAY", label: "js.calendar.day" },
  { key: "WORK_WEEK", label: "js.calendar.workWeek" },
  { key: "WEEK", label: "js.calendar.week" },
  { key: "MONTH", label: "js.calendar.month" },
  { key: "YEAR", label: "js.calendar.year" }
], Mr = ({ title: l, granularity: t, i18n: n, send: a }) => /* @__PURE__ */ e.createElement("div", { className: "tlCalToolbar" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalNav" }, /* @__PURE__ */ e.createElement("button", { className: "tlCalBtn", onClick: () => a("navigate", { direction: "TODAY" }) }, n["js.calendar.today"]), /* @__PURE__ */ e.createElement(
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
)), /* @__PURE__ */ e.createElement("div", { className: "tlCalTitle" }, l), /* @__PURE__ */ e.createElement("div", { className: "tlCalGranularity" }, xr.map((o) => /* @__PURE__ */ e.createElement(
  "button",
  {
    key: o.key,
    className: "tlCalBtn" + (o.key === t ? " tlCalBtn--active" : ""),
    onClick: () => a("switchGranularity", { granularity: o.key })
  },
  n[o.label]
))));
function Ir(l) {
  const t = [...l].sort((s, i) => s.start - i.start || i.end - s.end), n = [];
  let a = [], o = -1;
  const u = () => {
    const s = a.reduce((i, r) => Math.max(i, r.col + 1), 0);
    for (const i of a)
      i.cols = s;
    n.push(...a), a = [], o = -1;
  };
  for (const s of t) {
    a.length > 0 && s.start >= o && u();
    const i = new Set(a.filter((c) => c.ev.end > s.start).map((c) => c.col));
    let r = 0;
    for (; i.has(r); )
      r++;
    a.push({
      ev: s,
      topMin: je(s.start),
      botMin: je(s.start) + Math.max(15, (s.end - s.start) / ye),
      col: r,
      cols: 1
    }), o = Math.max(o, s.end);
  }
  return a.length > 0 && u(), n;
}
const It = (l) => {
  l.preventDefault(), l.currentTarget.setPointerCapture(l.pointerId);
}, zt = ({ className: l, placeholder: t, style: n, onCommit: a, onDiscard: o }) => {
  const u = lt(!1), s = (i) => {
    u.current || (u.current = !0, i === null ? o() : a(i));
  };
  return /* @__PURE__ */ e.createElement("div", { className: l, style: n }, /* @__PURE__ */ e.createElement(
    "input",
    {
      className: "tlCalCreateInput",
      autoFocus: !0,
      placeholder: t,
      onPointerDown: (i) => i.stopPropagation(),
      onClick: (i) => i.stopPropagation(),
      onKeyDown: (i) => {
        i.stopPropagation(), i.key === "Enter" ? s(i.currentTarget.value) : i.key === "Escape" && s(null);
      },
      onBlur: () => s(null)
    }
  ));
}, Dn = (l) => {
  const [t, n] = Vt(null), a = lt(null);
  a.current = t;
  const o = vt((i) => n(i), []), u = vt(() => n(null), []), s = vt(
    (i) => {
      const r = a.current;
      r && l("createSlot", { ...r, title: i }), n(null);
    },
    [l]
  );
  return { pending: t, open: o, commit: s, discard: u };
}, jr = ({
  ctx: l,
  rangeStart: t,
  granularity: n
}) => {
  const { events: a, locale: o, nonWorkingDays: u, dayStartHour: s, dayEndHour: i, now: r, send: c, editable: d, i18n: m } = l, f = Fe(() => {
    const B = n === "DAY" ? 1 : 7, j = [];
    for (let P = 0; P < B; P++) {
      const I = Ke(t, P);
      n === "WORK_WEEK" && u.includes(new Date(I).getDay()) || j.push(I);
    }
    return j;
  }, [t, n, u]), h = Dn(c), E = lt(null), N = lt(null), [C, _] = Vt(null), y = lt(null);
  y.current = C;
  const [w, x] = Vt(Date.now());
  fn(() => {
    const B = window.setInterval(() => x(Date.now()), 6e4);
    return () => window.clearInterval(B);
  }, []);
  const v = vt(
    (B, j) => {
      const P = E.current;
      if (!P)
        return { dayIndex: 0, min: 0 };
      const I = P.getBoundingClientRect(), D = I.width / f.length, K = et(Math.floor((B - I.left) / D), 0, f.length - 1), p = j - I.top + P.scrollTop, L = et(p / _e * 60, 0, 1440);
      return { dayIndex: K, min: L };
    },
    [f.length]
  );
  fn(() => {
    if (!C)
      return;
    const B = (I) => {
      const D = y.current;
      if (!D)
        return;
      const { dayIndex: K, min: p } = v(I.clientX, I.clientY);
      D.mode === "move" ? _({ ...D, dayStart: f[K], startMin: et(Je(p - D.grabMin), 0, 1440 - D.dur) }) : D.mode === "resize" ? _({ ...D, endMin: et(Je(p), D.startMin + Ct, 1440) }) : _({ ...D, toMin: et(Je(p), 0, 1440) });
    }, j = () => {
      const I = y.current;
      if (_(null), !!I)
        if (I.mode === "move") {
          const D = I.dayStart + I.startMin * ye;
          D !== I.origStartMs && c("moveEvent", { eventId: I.id, start: D, end: D + I.dur * ye });
        } else if (I.mode === "resize") {
          const D = I.dayStart + I.endMin * ye;
          D !== I.origEndMs && c("resizeEvent", { eventId: I.id, end: D });
        } else {
          const D = Math.min(I.fromMin, I.toMin), K = Math.max(I.fromMin, I.toMin);
          K - D >= Ct && h.open({ start: I.dayStart + D * ye, end: I.dayStart + K * ye, allDay: !1 });
        }
    }, P = () => _(null);
    return window.addEventListener("pointermove", B), window.addEventListener("pointerup", j, { once: !0 }), window.addEventListener("pointercancel", P), () => {
      window.removeEventListener("pointermove", B), window.removeEventListener("pointerup", j), window.removeEventListener("pointercancel", P);
    };
  }, [C, f, v, c, h.open]);
  const k = (B, j, P) => {
    if (!d || !j.movable)
      return;
    B.stopPropagation(), It(B), h.discard();
    const { min: I } = v(B.clientX, B.clientY), D = (j.end - j.start) / ye;
    _({
      mode: "move",
      id: j.id,
      grabMin: I - je(j.start),
      dur: D,
      dayStart: P,
      startMin: je(j.start),
      origStartMs: j.start
    });
  }, b = (B, j, P) => {
    !d || !j.resizable || (B.stopPropagation(), It(B), h.discard(), _({
      mode: "resize",
      id: j.id,
      dayStart: P,
      startMin: je(j.start),
      endMin: je(j.end),
      origEndMs: j.end
    }));
  }, T = (B, j) => {
    if (!d || B.button !== 0)
      return;
    It(B), h.discard();
    const { min: P } = v(B.clientX, B.clientY);
    _({ mode: "create", dayStart: j, fromMin: Je(P), toMin: Je(P) });
  }, M = Array.from({ length: 24 }, (B, j) => j), R = Fe(() => {
    if (C === null || !("id" in C))
      return a;
    const B = C;
    return a.map((j) => {
      if (j.id !== B.id)
        return j;
      if (B.mode === "move") {
        const P = B.dayStart + B.startMin * ye;
        return { ...j, start: P, end: P + B.dur * ye };
      }
      return { ...j, end: B.dayStart + B.endMin * ye };
    });
  }, [a, C]), U = Fe(() => f.map(
    (B) => Ir(
      R.filter((j) => !j.allDay && j.start < B + Pe && j.end > B)
    )
  ), [f, R]), Y = Fe(() => f.map((B) => R.filter((j) => j.allDay && j.start < B + Pe && j.end > B)), [f, R]), A = s * _e, H = i * _e;
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeGrid" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeHeader" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalGutter" }), f.map((B) => {
    const j = u.includes(new Date(B).getDay()), P = at(B, l.now);
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: B,
        className: "tlCalDayHead" + (j ? " tlCalDayHead--nonworking" : "") + (P ? " tlCalDayHead--today" : ""),
        onClick: () => c("goto", { date: B, granularity: "DAY" })
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlCalDayName" }, Oe(o, { weekday: "short" }, B)),
      /* @__PURE__ */ e.createElement("span", { className: "tlCalDayNum" }, new Date(B).getDate())
    );
  })), /* @__PURE__ */ e.createElement("div", { className: "tlCalAllDayRow" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalGutter tlCalAllDayLabel" }, m["js.calendar.allDay"]), f.map((B, j) => /* @__PURE__ */ e.createElement(
    "div",
    {
      key: B,
      className: "tlCalAllDayCell",
      onClick: () => d && h.open({ start: B, end: B + Pe, allDay: !0 })
    },
    h.pending && h.pending.allDay && h.pending.start === B && /* @__PURE__ */ e.createElement(
      zt,
      {
        className: "tlCalAllDayEvent tlCalEvent--preview",
        placeholder: m["js.calendar.newEventTitle"],
        onCommit: h.commit,
        onDiscard: h.discard
      }
    ),
    Y[j].map((P) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: P.id,
        className: "tlCalAllDayEvent " + yt(P.category) + (P.selected ? " tlCalEvent--selected" : ""),
        style: wt(P),
        title: P.tooltip,
        onClick: (I) => {
          I.stopPropagation(), c("selectEvent", { eventId: P.id });
        }
      },
      P.title
    ))
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlCalScroll", ref: N }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeBody", style: { height: 24 * _e } }, /* @__PURE__ */ e.createElement("div", { className: "tlCalHourAxis" }, M.map((B) => /* @__PURE__ */ e.createElement("div", { key: B, className: "tlCalHourLabel", style: { top: B * _e } }, B === 0 ? "" : Oe(o, { hour: "numeric" }, De(t) + B * Sr)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalColumns", ref: E, style: { gridTemplateColumns: `repeat(${f.length}, 1fr)` } }, f.map((B, j) => {
    const P = u.includes(new Date(B).getDay()), I = C && ("dayStart" in C && C.dayStart === B) ? C : null;
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: B,
        className: "tlCalCol" + (P ? " tlCalCol--nonworking" : ""),
        onPointerDown: (D) => T(D, B)
      },
      M.map((D) => /* @__PURE__ */ e.createElement("div", { key: D, className: "tlCalHourLine", style: { top: D * _e } })),
      /* @__PURE__ */ e.createElement("div", { className: "tlCalWorkBand", style: { top: A, height: H - A } }),
      at(B, w) && /* @__PURE__ */ e.createElement("div", { className: "tlCalNowLine", style: { top: je(Date.now()) / 60 * _e } }),
      U[j].map((D) => {
        const K = C !== null && "id" in C && C.id === D.ev.id, p = D.topMin / 60 * _e, L = (D.botMin - D.topMin) / 60 * _e, z = 100 / D.cols;
        return /* @__PURE__ */ e.createElement(
          "div",
          {
            key: D.ev.id,
            className: "tlCalEvent " + yt(D.ev.category) + (D.ev.selected ? " tlCalEvent--selected" : "") + (K ? " tlCalEvent--dragging" : ""),
            style: wt(D.ev, {
              top: p,
              height: L,
              left: `${D.col * z}%`,
              width: `calc(${z}% - 2px)`
            }),
            title: D.ev.tooltip,
            onPointerDown: (W) => k(W, D.ev, B),
            onClick: (W) => {
              W.stopPropagation(), c("selectEvent", { eventId: D.ev.id });
            }
          },
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTime" }, Lr(o, D.ev)),
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTitle" }, D.ev.title),
          d && D.ev.resizable && /* @__PURE__ */ e.createElement("span", { className: "tlCalResizeHandle", onPointerDown: (W) => b(W, D.ev, B) })
        );
      }),
      h.pending && !h.pending.allDay && De(h.pending.start) === B && /* @__PURE__ */ e.createElement(
        zt,
        {
          className: "tlCalEvent tlCalEvent--preview",
          placeholder: m["js.calendar.newEventTitle"],
          style: {
            top: je(h.pending.start) / 60 * _e,
            height: (h.pending.end - h.pending.start) / ye / 60 * _e
          },
          onCommit: h.commit,
          onDiscard: h.discard
        }
      ),
      I && I.mode === "create" && /* @__PURE__ */ e.createElement(
        "div",
        {
          className: "tlCalEvent tlCalEvent--preview",
          style: {
            top: Math.min(I.fromMin, I.toMin) / 60 * _e,
            height: Math.abs(I.toMin - I.fromMin) / 60 * _e
          }
        }
      )
    );
  })))));
}, Pr = 3, Ar = ({ ctx: l, rangeStart: t, anchorMonth: n }) => {
  const { events: a, locale: o, nonWorkingDays: u, send: s, editable: i, now: r, i18n: c } = l, d = Dn(s), m = Fe(() => {
    const h = [];
    for (let E = 0; E < 6; E++) {
      const N = [];
      for (let C = 0; C < 7; C++)
        N.push(Ke(t, E * 7 + C));
      h.push(N);
    }
    return h;
  }, [t]), f = (h, E) => {
    h.preventDefault();
    const N = h.dataTransfer.getData("text/plain"), C = a.find((y) => y.id === N);
    if (!C || !i || !C.movable)
      return;
    const _ = E - De(C.start);
    s("moveEvent", { eventId: N, start: C.start + _, end: C.end + _ });
  };
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalMonth" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthHead" }, m[0].map((h) => /* @__PURE__ */ e.createElement("div", { key: h, className: "tlCalMonthWeekday" }, Oe(o, { weekday: "short" }, h)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthBody" }, m.map((h, E) => {
    const N = h[0], C = Ke(N, 7), _ = a.filter((w) => (w.allDay || w.end - w.start >= Pe) && w.start < C && w.end > N).sort((w, x) => w.start - x.start).slice(0, 3), y = _.length;
    return /* @__PURE__ */ e.createElement("div", { key: E, className: "tlCalMonthWeek" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthDays" }, h.map((w) => {
      const x = new Date(w).getMonth() === new Date(n).getMonth(), v = u.includes(new Date(w).getDay()), k = at(w, r);
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: w,
          className: "tlCalMonthCell" + (x ? "" : " tlCalMonthCell--other") + (v ? " tlCalMonthCell--nonworking" : ""),
          onDragOver: (b) => b.preventDefault(),
          onDrop: (b) => f(b, w),
          onClick: () => i && d.open({ start: w, end: w + Pe, allDay: !0 })
        },
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlCalMonthDayNum" + (k ? " tlCalMonthDayNum--today" : ""),
            onClick: (b) => {
              b.stopPropagation(), s("goto", { date: w, granularity: "DAY" });
            }
          },
          new Date(w).getDate()
        ),
        d.pending && d.pending.start === w && /* @__PURE__ */ e.createElement(
          zt,
          {
            className: "tlCalMonthBar tlCalEvent--preview tlCalMonthCreate",
            placeholder: c["js.calendar.newEventTitle"],
            onCommit: d.commit,
            onDiscard: d.discard
          }
        )
      );
    })), /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthOverlay" }, _.map((w, x) => {
      const v = Math.max(0, Math.floor((De(Math.max(w.start, N)) - N) / Pe)), k = Math.min(7, Math.ceil((w.end - N) / Pe));
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: w.id,
          className: "tlCalMonthBar " + yt(w.category) + (w.selected ? " tlCalEvent--selected" : ""),
          style: wt(w, {
            gridColumn: `${v + 1} / ${Math.max(v + 1, k) + 1}`,
            gridRow: x + 1
          }),
          draggable: i && w.movable,
          onDragStart: (b) => b.dataTransfer.setData("text/plain", w.id),
          title: w.tooltip,
          onClick: (b) => {
            b.stopPropagation(), s("selectEvent", { eventId: w.id });
          }
        },
        w.title
      );
    }), h.map((w, x) => {
      const v = a.filter((T) => !T.allDay && T.end - T.start < Pe && at(T.start, w)).sort((T, M) => T.start - M.start), k = v.slice(0, Pr), b = v.length - k.length;
      return k.map((T, M) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: T.id,
          className: "tlCalChip " + yt(T.category) + (T.selected ? " tlCalEvent--selected" : ""),
          style: wt(T, { gridColumn: x + 1, gridRow: y + 1 + M }),
          draggable: i && T.movable,
          onDragStart: (R) => R.dataTransfer.setData("text/plain", T.id),
          title: T.tooltip,
          onClick: (R) => {
            R.stopPropagation(), s("selectEvent", { eventId: T.id });
          }
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipDot" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipTime" }, Oe(o, { hour: "numeric", minute: "2-digit" }, T.start)),
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipTitle" }, T.title)
      )).concat(
        b > 0 ? [
          /* @__PURE__ */ e.createElement(
            "div",
            {
              key: "more-" + w,
              className: "tlCalMore",
              style: { gridColumn: x + 1, gridRow: y + 1 + k.length },
              onClick: () => s("goto", { date: w, granularity: "DAY" })
            },
            "+",
            b,
            " ",
            l.i18n["js.calendar.more"]
          )
        ] : []
      );
    })));
  })));
}, Br = ({ ctx: l, rangeStart: t }) => {
  const { events: n, locale: a, firstDayOfWeek: o, nonWorkingDays: u, send: s, now: i } = l, r = Fe(() => {
    const f = /* @__PURE__ */ new Set();
    for (const h of n) {
      let E = De(h.start);
      const N = h.end;
      for (; E < N; )
        f.add(E), E = Ke(E, 1);
    }
    return f;
  }, [n]), c = new Date(t).getFullYear(), d = Array.from({ length: 12 }, (f, h) => new Date(c, h, 1).getTime()), m = Fe(() => {
    const f = new Date(2023, 0, 1);
    return Array.from({ length: 7 }, (h, E) => {
      const N = new Date(f);
      return N.setDate(f.getDate() + (o + E) % 7), new Intl.DateTimeFormat(a, { weekday: "narrow" }).format(N);
    });
  }, [a, o]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalYear" }, d.map((f) => {
    const h = new Date(f), E = De(Ke(f, -((h.getDay() - o + 7) % 7))), N = Array.from({ length: 42 }, (C, _) => Ke(E, _));
    return /* @__PURE__ */ e.createElement("div", { key: f, className: "tlCalMini" }, /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlCalMiniTitle",
        onClick: () => s("goto", { date: f, granularity: "MONTH" })
      },
      Oe(a, { month: "long" }, f)
    ), /* @__PURE__ */ e.createElement("div", { className: "tlCalMiniGrid" }, m.map((C, _) => /* @__PURE__ */ e.createElement("div", { key: "h" + _, className: "tlCalMiniWd" }, C)), N.map((C) => {
      const _ = new Date(C).getMonth() === h.getMonth(), y = u.includes(new Date(C).getDay()), w = at(C, i), x = r.has(Tr(C));
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: C,
          className: "tlCalMiniDay" + (_ ? "" : " tlCalMiniDay--other") + (y ? " tlCalMiniDay--nonworking" : "") + (w ? " tlCalMiniDay--today" : "") + (x ? " tlCalMiniDay--event" : ""),
          onClick: () => s("goto", { date: C, granularity: "DAY" })
        },
        new Date(C).getDate()
      );
    })));
  }));
}, Fr = ({ controlId: l }) => {
  const t = X(), n = ae(), a = ue(Nr), o = t.granularity ?? "WEEK", u = t.rangeStart ?? Date.now(), s = t.anchor ?? u, i = t.title ?? "", r = {
    locale: t.locale ?? "en",
    firstDayOfWeek: t.firstDayOfWeek ?? 0,
    nonWorkingDays: t.nonWorkingDays ?? [0, 6],
    dayStartHour: t.dayStartHour ?? 8,
    dayEndHour: t.dayEndHour ?? 18,
    editable: t.editable !== !1,
    now: t.now ?? Date.now(),
    events: Rr(t.events),
    send: n,
    i18n: a
  };
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCalendar" }, /* @__PURE__ */ e.createElement(Mr, { title: i, granularity: o, i18n: a, send: n }), /* @__PURE__ */ e.createElement("div", { className: "tlCalBody" }, o === "MONTH" ? /* @__PURE__ */ e.createElement(Ar, { ctx: r, rangeStart: u, anchorMonth: s }) : o === "YEAR" ? /* @__PURE__ */ e.createElement(Br, { ctx: r, rangeStart: u }) : /* @__PURE__ */ e.createElement(jr, { ctx: r, rangeStart: u, granularity: o })));
}, Or = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, Tn = e.createContext(Or), { useMemo: $r, useRef: Hr, useState: Wr, useEffect: Ur } = e, Vr = 320, zr = "TLTableView", Kr = "TLPanel", Yr = ({ controlId: l }) => {
  var C;
  const t = X(), n = t.maxColumns ?? 3, a = t.labelPosition ?? "auto", o = t.readOnly === !0, u = t.children ?? [], s = t.noModelMessage, i = Hr(null), [r, c] = Wr(
    a === "top" ? "top" : "side"
  );
  Ur(() => {
    if (a !== "auto") {
      c(a);
      return;
    }
    const _ = i.current;
    if (!_) return;
    const y = new ResizeObserver((w) => {
      for (const x of w) {
        const k = x.contentRect.width / n;
        c(k < Vr ? "top" : "side");
      }
    });
    return y.observe(_), () => y.disconnect();
  }, [a, n]);
  const d = $r(() => ({
    readOnly: o,
    resolvedLabelPosition: r
  }), [o, r]), f = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, h = u.length === 1 ? u[0] : void 0, E = !!h && (h.module === zr || h.module === Kr && ((C = h.state) == null ? void 0 : C.bare) === !0), N = [
    "tlFormLayout",
    o ? "tlFormLayout--readonly" : "",
    E ? "tlFormLayout--flush" : ""
  ].filter(Boolean).join(" ");
  return s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: i }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, s)) : /* @__PURE__ */ e.createElement(Tn.Provider, { value: d }, /* @__PURE__ */ e.createElement("div", { id: l, className: N, style: f, ref: i }, u.map((_, y) => /* @__PURE__ */ e.createElement(G, { key: y, control: _ }))));
}, { useCallback: Gr } = e, Xr = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, qr = ({ controlId: l }) => {
  const t = X(), n = ae(), a = ue(Xr), o = t.headerControl ?? null, u = t.headerActions ?? [], s = t.collapsible === !0, i = t.collapsed === !0, r = t.border ?? "none", c = t.fullLine === !0, d = t.children ?? [], m = o != null || u.length > 0 || s, f = Gr(() => {
    n("toggleCollapse");
  }, [n]), h = [
    "tlFormGroup",
    `tlFormGroup--border-${r}`,
    c ? "tlFormGroup--fullLine" : "",
    i ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: h }, m && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, s && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: f,
      "aria-expanded": !i,
      title: i ? a["js.formGroup.expand"] : a["js.formGroup.collapse"]
    },
    /* @__PURE__ */ e.createElement(
      "svg",
      {
        viewBox: "0 0 16 16",
        width: "14",
        height: "14",
        "aria-hidden": "true",
        className: i ? "tlFormGroup__chevron--collapsed" : "tlFormGroup__chevron"
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
  ), o && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(G, { control: o })), u.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, u.map((E, N) => /* @__PURE__ */ e.createElement(G, { key: N, control: E })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, d.map((E, N) => /* @__PURE__ */ e.createElement(G, { key: N, control: E }))));
}, { useContext: Zr, useState: Qr, useCallback: Jr } = e, eo = ({ controlId: l }) => {
  const t = X(), n = Zr(Tn), a = t.label ?? "", o = t.required === !0, u = t.error, s = t.errorIcon, i = t.warnings, r = t.warningIcon, c = t.helpText, d = t.dirty === !0, m = t.labelPosition ?? n.resolvedLabelPosition, f = t.fullLine === !0, h = t.visible !== !1, E = t.hasTooltip === !0, N = t.field, C = n.readOnly, [_, y] = Qr(!1), w = Jr(() => y((T) => !T), []), x = m === "hidden", v = u != null, k = i != null && i.length > 0, b = [
    "tlFormField",
    `tlFormField--${m}`,
    C ? "tlFormField--readonly" : "",
    f ? "tlFormField--fullLine" : "",
    v ? "tlFormField--error" : "",
    !v && k ? "tlFormField--warning" : "",
    d ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: b, style: h ? void 0 : { display: "none" } }, !x && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": E ? "key:tooltip" : void 0
    },
    a
  ), o && !C && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), d && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), c && !C && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(G, { control: N })), !C && v && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement($t, { image: s, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, u)), !C && !v && k && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, i.map((T, M) => /* @__PURE__ */ e.createElement("div", { key: M, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement($t, { image: r, className: "tlFormField__warningIcon" }), /* @__PURE__ */ e.createElement("span", null, T)))), !C && c && _ && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, c));
}, to = ({ controlId: l }) => {
  const t = X(), n = ae(), a = t.iconCss, o = t.iconSrc, u = t.label, s = t.cssClass, i = t.hasTooltip === !0, r = t.hasLink, c = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : o ? /* @__PURE__ */ e.createElement("img", { src: o, className: "tlTypeIcon", alt: "" }) : null, d = /* @__PURE__ */ e.createElement(e.Fragment, null, c, u && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, u)), m = e.useCallback((E) => {
    E.preventDefault(), n("goto", {});
  }, [n]), f = ["tlResourceCell", s].filter(Boolean).join(" "), h = i ? "key:tooltip" : void 0;
  return r ? /* @__PURE__ */ e.createElement(
    "a",
    {
      id: l,
      className: f,
      href: "#",
      onClick: m,
      "data-tooltip": h
    },
    d
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: f, "data-tooltip": h }, d);
}, no = 20, lo = () => {
  var k;
  const l = X(), t = ae(), n = l.nodes ?? [], a = l.selectionMode ?? "single", o = l.dragEnabled ?? !1, u = l.dropEnabled ?? !1, s = l.dropIndicatorNodeId ?? null, i = l.dropIndicatorPosition ?? null, [r, c] = e.useState(-1), d = e.useRef(null), m = ((k = n.find((b) => b.selected)) == null ? void 0 : k.id) ?? null;
  e.useEffect(() => {
    var T;
    if (m == null)
      return;
    const b = (T = d.current) == null ? void 0 : T.querySelector(".tlTreeView__node--selected");
    b && b.scrollIntoView({ block: "nearest" });
  }, [m]);
  const f = e.useCallback((b, T) => {
    t(T ? "collapse" : "expand", { nodeId: b });
  }, [t]), h = e.useCallback((b, T) => {
    var R;
    const M = window.getSelection();
    M && !M.isCollapsed && T.currentTarget.contains(M.anchorNode) || ((R = d.current) == null || R.focus({ preventScroll: !0 }), t("select", {
      nodeId: b,
      ctrlKey: T.ctrlKey || T.metaKey,
      shiftKey: T.shiftKey
    }));
  }, [t]), E = e.useCallback((b, T) => {
    T.preventDefault(), t("contextMenu", { nodeId: b, x: T.clientX, y: T.clientY });
  }, [t]), N = e.useRef(null), C = e.useCallback((b, T) => {
    const M = T.getBoundingClientRect(), R = b.clientY - M.top, U = M.height / 3;
    return R < U ? "above" : R > U * 2 ? "below" : "within";
  }, []), _ = e.useCallback((b, T) => {
    T.dataTransfer.effectAllowed = "move", T.dataTransfer.setData("text/plain", b);
  }, []), y = e.useCallback((b, T) => {
    T.preventDefault(), T.dataTransfer.dropEffect = "move";
    const M = C(T, T.currentTarget);
    N.current != null && window.clearTimeout(N.current), N.current = window.setTimeout(() => {
      t("dragOver", { nodeId: b, position: M }), N.current = null;
    }, 50);
  }, [t, C]), w = e.useCallback((b, T) => {
    T.preventDefault(), N.current != null && (window.clearTimeout(N.current), N.current = null);
    const M = C(T, T.currentTarget);
    t("drop", { nodeId: b, position: M });
  }, [t, C]), x = e.useCallback(() => {
    N.current != null && (window.clearTimeout(N.current), N.current = null), t("dragEnd");
  }, [t]), v = e.useCallback((b) => {
    if (n.length === 0) return;
    let T = r;
    switch (b.key) {
      case "ArrowDown":
        b.preventDefault(), T = Math.min(r + 1, n.length - 1);
        break;
      case "ArrowUp":
        b.preventDefault(), T = Math.max(r - 1, 0);
        break;
      case "ArrowRight":
        if (b.preventDefault(), r >= 0 && r < n.length) {
          const M = n[r];
          if (M.expandable && !M.expanded) {
            t("expand", { nodeId: M.id });
            return;
          } else M.expanded && (T = r + 1);
        }
        break;
      case "ArrowLeft":
        if (b.preventDefault(), r >= 0 && r < n.length) {
          const M = n[r];
          if (M.expanded) {
            t("collapse", { nodeId: M.id });
            return;
          } else {
            const R = M.depth;
            for (let U = r - 1; U >= 0; U--)
              if (n[U].depth < R) {
                T = U;
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
        b.preventDefault(), T = 0;
        break;
      case "End":
        b.preventDefault(), T = n.length - 1;
        break;
      default:
        return;
    }
    T !== r && c(T);
  }, [r, n, t, a]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: d,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: v
    },
    n.map((b, T) => /* @__PURE__ */ e.createElement(
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
          T === r ? "tlTreeView__node--focused" : "",
          s === b.id && i === "above" ? "tlTreeView__node--drop-above" : "",
          s === b.id && i === "within" ? "tlTreeView__node--drop-within" : "",
          s === b.id && i === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: b.depth * no },
        draggable: o,
        onMouseDown: (M) => {
          (M.shiftKey || M.ctrlKey || M.metaKey || M.detail > 1) && M.preventDefault();
        },
        onClick: (M) => h(b.id, M),
        onContextMenu: (M) => E(b.id, M),
        onDragStart: (M) => _(b.id, M),
        onDragOver: u ? (M) => y(b.id, M) : void 0,
        onDrop: u ? (M) => w(b.id, M) : void 0,
        onDragEnd: x
      },
      b.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (M) => {
            M.stopPropagation(), f(b.id, b.expanded);
          },
          tabIndex: -1,
          "aria-label": b.expanded ? "Collapse" : "Expand"
        },
        b.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: b.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(G, { control: b.content }))
    ))
  );
};
var jt = { exports: {} }, be = {}, Pt = { exports: {} }, J = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var hn;
function ao() {
  if (hn) return J;
  hn = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), a = Symbol.for("react.strict_mode"), o = Symbol.for("react.profiler"), u = Symbol.for("react.consumer"), s = Symbol.for("react.context"), i = Symbol.for("react.forward_ref"), r = Symbol.for("react.suspense"), c = Symbol.for("react.memo"), d = Symbol.for("react.lazy"), m = Symbol.for("react.activity"), f = Symbol.iterator;
  function h(p) {
    return p === null || typeof p != "object" ? null : (p = f && p[f] || p["@@iterator"], typeof p == "function" ? p : null);
  }
  var E = {
    isMounted: function() {
      return !1;
    },
    enqueueForceUpdate: function() {
    },
    enqueueReplaceState: function() {
    },
    enqueueSetState: function() {
    }
  }, N = Object.assign, C = {};
  function _(p, L, z) {
    this.props = p, this.context = L, this.refs = C, this.updater = z || E;
  }
  _.prototype.isReactComponent = {}, _.prototype.setState = function(p, L) {
    if (typeof p != "object" && typeof p != "function" && p != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, p, L, "setState");
  }, _.prototype.forceUpdate = function(p) {
    this.updater.enqueueForceUpdate(this, p, "forceUpdate");
  };
  function y() {
  }
  y.prototype = _.prototype;
  function w(p, L, z) {
    this.props = p, this.context = L, this.refs = C, this.updater = z || E;
  }
  var x = w.prototype = new y();
  x.constructor = w, N(x, _.prototype), x.isPureReactComponent = !0;
  var v = Array.isArray;
  function k() {
  }
  var b = { H: null, A: null, T: null, S: null }, T = Object.prototype.hasOwnProperty;
  function M(p, L, z) {
    var W = z.ref;
    return {
      $$typeof: l,
      type: p,
      key: L,
      ref: W !== void 0 ? W : null,
      props: z
    };
  }
  function R(p, L) {
    return M(p.type, L, p.props);
  }
  function U(p) {
    return typeof p == "object" && p !== null && p.$$typeof === l;
  }
  function Y(p) {
    var L = { "=": "=0", ":": "=2" };
    return "$" + p.replace(/[=:]/g, function(z) {
      return L[z];
    });
  }
  var A = /\/+/g;
  function H(p, L) {
    return typeof p == "object" && p !== null && p.key != null ? Y("" + p.key) : L.toString(36);
  }
  function B(p) {
    switch (p.status) {
      case "fulfilled":
        return p.value;
      case "rejected":
        throw p.reason;
      default:
        switch (typeof p.status == "string" ? p.then(k, k) : (p.status = "pending", p.then(
          function(L) {
            p.status === "pending" && (p.status = "fulfilled", p.value = L);
          },
          function(L) {
            p.status === "pending" && (p.status = "rejected", p.reason = L);
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
  function j(p, L, z, W, Z) {
    var F = typeof p;
    (F === "undefined" || F === "boolean") && (p = null);
    var te = !1;
    if (p === null) te = !0;
    else
      switch (F) {
        case "bigint":
        case "string":
        case "number":
          te = !0;
          break;
        case "object":
          switch (p.$$typeof) {
            case l:
            case t:
              te = !0;
              break;
            case d:
              return te = p._init, j(
                te(p._payload),
                L,
                z,
                W,
                Z
              );
          }
      }
    if (te)
      return Z = Z(p), te = W === "" ? "." + H(p, 0) : W, v(Z) ? (z = "", te != null && (z = te.replace(A, "$&/") + "/"), j(Z, L, z, "", function(Ee) {
        return Ee;
      })) : Z != null && (U(Z) && (Z = R(
        Z,
        z + (Z.key == null || p && p.key === Z.key ? "" : ("" + Z.key).replace(
          A,
          "$&/"
        ) + "/") + te
      )), L.push(Z)), 1;
    te = 0;
    var se = W === "" ? "." : W + ":";
    if (v(p))
      for (var le = 0; le < p.length; le++)
        W = p[le], F = se + H(W, le), te += j(
          W,
          L,
          z,
          F,
          Z
        );
    else if (le = h(p), typeof le == "function")
      for (p = le.call(p), le = 0; !(W = p.next()).done; )
        W = W.value, F = se + H(W, le++), te += j(
          W,
          L,
          z,
          F,
          Z
        );
    else if (F === "object") {
      if (typeof p.then == "function")
        return j(
          B(p),
          L,
          z,
          W,
          Z
        );
      throw L = String(p), Error(
        "Objects are not valid as a React child (found: " + (L === "[object Object]" ? "object with keys {" + Object.keys(p).join(", ") + "}" : L) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return te;
  }
  function P(p, L, z) {
    if (p == null) return p;
    var W = [], Z = 0;
    return j(p, W, "", "", function(F) {
      return L.call(z, F, Z++);
    }), W;
  }
  function I(p) {
    if (p._status === -1) {
      var L = p._result;
      L = L(), L.then(
        function(z) {
          (p._status === 0 || p._status === -1) && (p._status = 1, p._result = z);
        },
        function(z) {
          (p._status === 0 || p._status === -1) && (p._status = 2, p._result = z);
        }
      ), p._status === -1 && (p._status = 0, p._result = L);
    }
    if (p._status === 1) return p._result.default;
    throw p._result;
  }
  var D = typeof reportError == "function" ? reportError : function(p) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var L = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof p == "object" && p !== null && typeof p.message == "string" ? String(p.message) : String(p),
        error: p
      });
      if (!window.dispatchEvent(L)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", p);
      return;
    }
    console.error(p);
  }, K = {
    map: P,
    forEach: function(p, L, z) {
      P(
        p,
        function() {
          L.apply(this, arguments);
        },
        z
      );
    },
    count: function(p) {
      var L = 0;
      return P(p, function() {
        L++;
      }), L;
    },
    toArray: function(p) {
      return P(p, function(L) {
        return L;
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
  return J.Activity = m, J.Children = K, J.Component = _, J.Fragment = n, J.Profiler = o, J.PureComponent = w, J.StrictMode = a, J.Suspense = r, J.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = b, J.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(p) {
      return b.H.useMemoCache(p);
    }
  }, J.cache = function(p) {
    return function() {
      return p.apply(null, arguments);
    };
  }, J.cacheSignal = function() {
    return null;
  }, J.cloneElement = function(p, L, z) {
    if (p == null)
      throw Error(
        "The argument must be a React element, but you passed " + p + "."
      );
    var W = N({}, p.props), Z = p.key;
    if (L != null)
      for (F in L.key !== void 0 && (Z = "" + L.key), L)
        !T.call(L, F) || F === "key" || F === "__self" || F === "__source" || F === "ref" && L.ref === void 0 || (W[F] = L[F]);
    var F = arguments.length - 2;
    if (F === 1) W.children = z;
    else if (1 < F) {
      for (var te = Array(F), se = 0; se < F; se++)
        te[se] = arguments[se + 2];
      W.children = te;
    }
    return M(p.type, Z, W);
  }, J.createContext = function(p) {
    return p = {
      $$typeof: s,
      _currentValue: p,
      _currentValue2: p,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, p.Provider = p, p.Consumer = {
      $$typeof: u,
      _context: p
    }, p;
  }, J.createElement = function(p, L, z) {
    var W, Z = {}, F = null;
    if (L != null)
      for (W in L.key !== void 0 && (F = "" + L.key), L)
        T.call(L, W) && W !== "key" && W !== "__self" && W !== "__source" && (Z[W] = L[W]);
    var te = arguments.length - 2;
    if (te === 1) Z.children = z;
    else if (1 < te) {
      for (var se = Array(te), le = 0; le < te; le++)
        se[le] = arguments[le + 2];
      Z.children = se;
    }
    if (p && p.defaultProps)
      for (W in te = p.defaultProps, te)
        Z[W] === void 0 && (Z[W] = te[W]);
    return M(p, F, Z);
  }, J.createRef = function() {
    return { current: null };
  }, J.forwardRef = function(p) {
    return { $$typeof: i, render: p };
  }, J.isValidElement = U, J.lazy = function(p) {
    return {
      $$typeof: d,
      _payload: { _status: -1, _result: p },
      _init: I
    };
  }, J.memo = function(p, L) {
    return {
      $$typeof: c,
      type: p,
      compare: L === void 0 ? null : L
    };
  }, J.startTransition = function(p) {
    var L = b.T, z = {};
    b.T = z;
    try {
      var W = p(), Z = b.S;
      Z !== null && Z(z, W), typeof W == "object" && W !== null && typeof W.then == "function" && W.then(k, D);
    } catch (F) {
      D(F);
    } finally {
      L !== null && z.types !== null && (L.types = z.types), b.T = L;
    }
  }, J.unstable_useCacheRefresh = function() {
    return b.H.useCacheRefresh();
  }, J.use = function(p) {
    return b.H.use(p);
  }, J.useActionState = function(p, L, z) {
    return b.H.useActionState(p, L, z);
  }, J.useCallback = function(p, L) {
    return b.H.useCallback(p, L);
  }, J.useContext = function(p) {
    return b.H.useContext(p);
  }, J.useDebugValue = function() {
  }, J.useDeferredValue = function(p, L) {
    return b.H.useDeferredValue(p, L);
  }, J.useEffect = function(p, L) {
    return b.H.useEffect(p, L);
  }, J.useEffectEvent = function(p) {
    return b.H.useEffectEvent(p);
  }, J.useId = function() {
    return b.H.useId();
  }, J.useImperativeHandle = function(p, L, z) {
    return b.H.useImperativeHandle(p, L, z);
  }, J.useInsertionEffect = function(p, L) {
    return b.H.useInsertionEffect(p, L);
  }, J.useLayoutEffect = function(p, L) {
    return b.H.useLayoutEffect(p, L);
  }, J.useMemo = function(p, L) {
    return b.H.useMemo(p, L);
  }, J.useOptimistic = function(p, L) {
    return b.H.useOptimistic(p, L);
  }, J.useReducer = function(p, L, z) {
    return b.H.useReducer(p, L, z);
  }, J.useRef = function(p) {
    return b.H.useRef(p);
  }, J.useState = function(p) {
    return b.H.useState(p);
  }, J.useSyncExternalStore = function(p, L, z) {
    return b.H.useSyncExternalStore(
      p,
      L,
      z
    );
  }, J.useTransition = function() {
    return b.H.useTransition();
  }, J.version = "19.2.4", J;
}
var bn;
function ro() {
  return bn || (bn = 1, Pt.exports = ao()), Pt.exports;
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
var gn;
function oo() {
  if (gn) return be;
  gn = 1;
  var l = ro();
  function t(r) {
    var c = "https://react.dev/errors/" + r;
    if (1 < arguments.length) {
      c += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var d = 2; d < arguments.length; d++)
        c += "&args[]=" + encodeURIComponent(arguments[d]);
    }
    return "Minified React error #" + r + "; visit " + c + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
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
  }, o = Symbol.for("react.portal");
  function u(r, c, d) {
    var m = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: o,
      key: m == null ? null : "" + m,
      children: r,
      containerInfo: c,
      implementation: d
    };
  }
  var s = l.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function i(r, c) {
    if (r === "font") return "";
    if (typeof c == "string")
      return c === "use-credentials" ? c : "";
  }
  return be.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = a, be.createPortal = function(r, c) {
    var d = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!c || c.nodeType !== 1 && c.nodeType !== 9 && c.nodeType !== 11)
      throw Error(t(299));
    return u(r, c, null, d);
  }, be.flushSync = function(r) {
    var c = s.T, d = a.p;
    try {
      if (s.T = null, a.p = 2, r) return r();
    } finally {
      s.T = c, a.p = d, a.d.f();
    }
  }, be.preconnect = function(r, c) {
    typeof r == "string" && (c ? (c = c.crossOrigin, c = typeof c == "string" ? c === "use-credentials" ? c : "" : void 0) : c = null, a.d.C(r, c));
  }, be.prefetchDNS = function(r) {
    typeof r == "string" && a.d.D(r);
  }, be.preinit = function(r, c) {
    if (typeof r == "string" && c && typeof c.as == "string") {
      var d = c.as, m = i(d, c.crossOrigin), f = typeof c.integrity == "string" ? c.integrity : void 0, h = typeof c.fetchPriority == "string" ? c.fetchPriority : void 0;
      d === "style" ? a.d.S(
        r,
        typeof c.precedence == "string" ? c.precedence : void 0,
        {
          crossOrigin: m,
          integrity: f,
          fetchPriority: h
        }
      ) : d === "script" && a.d.X(r, {
        crossOrigin: m,
        integrity: f,
        fetchPriority: h,
        nonce: typeof c.nonce == "string" ? c.nonce : void 0
      });
    }
  }, be.preinitModule = function(r, c) {
    if (typeof r == "string")
      if (typeof c == "object" && c !== null) {
        if (c.as == null || c.as === "script") {
          var d = i(
            c.as,
            c.crossOrigin
          );
          a.d.M(r, {
            crossOrigin: d,
            integrity: typeof c.integrity == "string" ? c.integrity : void 0,
            nonce: typeof c.nonce == "string" ? c.nonce : void 0
          });
        }
      } else c == null && a.d.M(r);
  }, be.preload = function(r, c) {
    if (typeof r == "string" && typeof c == "object" && c !== null && typeof c.as == "string") {
      var d = c.as, m = i(d, c.crossOrigin);
      a.d.L(r, d, {
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
  }, be.preloadModule = function(r, c) {
    if (typeof r == "string")
      if (c) {
        var d = i(c.as, c.crossOrigin);
        a.d.m(r, {
          as: typeof c.as == "string" && c.as !== "script" ? c.as : void 0,
          crossOrigin: d,
          integrity: typeof c.integrity == "string" ? c.integrity : void 0
        });
      } else a.d.m(r);
  }, be.requestFormReset = function(r) {
    a.d.r(r);
  }, be.unstable_batchedUpdates = function(r, c) {
    return r(c);
  }, be.useFormState = function(r, c, d) {
    return s.H.useFormState(r, c, d);
  }, be.useFormStatus = function() {
    return s.H.useHostTransitionStatus();
  }, be.version = "19.2.4", be;
}
var En;
function so() {
  if (En) return jt.exports;
  En = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), jt.exports = oo(), jt.exports;
}
var Rn = so();
const { useState: Ie, useCallback: ge, useRef: tt, useEffect: Ue, useMemo: Kt } = e;
function qt({ image: l }) {
  return l ? l.startsWith("/") ? /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" }) : /* @__PURE__ */ e.createElement(Ne, { encoded: l, className: "tlDropdownSelect__optionIcon" }) : null;
}
function co({
  option: l,
  removable: t,
  onRemove: n,
  removeLabel: a,
  draggable: o,
  onDragStart: u,
  onDragOver: s,
  onDrop: i,
  onDragEnd: r,
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
      draggable: o || void 0,
      onDragStart: u,
      onDragOver: s,
      onDrop: i,
      onDragEnd: r
    },
    o && /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__dragHandle", "aria-hidden": "true" }, "⋮⋮"),
    /* @__PURE__ */ e.createElement(qt, { image: l.image }),
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
function io({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: a,
  onMouseEnter: o,
  id: u
}) {
  const s = ge(() => a(l.value), [a, l.value]), i = Kt(() => {
    if (!n) return l.label;
    const r = l.label.toLowerCase().indexOf(n.toLowerCase());
    return r < 0 ? l.label : /* @__PURE__ */ e.createElement(e.Fragment, null, l.label.substring(0, r), /* @__PURE__ */ e.createElement("strong", null, l.label.substring(r, r + n.length)), l.label.substring(r + n.length));
  }, [l.label, n]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: u,
      role: "option",
      "aria-selected": t,
      className: "tlDropdownSelect__option" + (t ? " tlDropdownSelect__option--highlighted" : ""),
      onClick: s,
      onMouseEnter: o
    },
    /* @__PURE__ */ e.createElement(qt, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, i)
  );
}
const uo = ({ controlId: l, state: t }) => {
  const n = ae(), a = t.value ?? [], o = t.multiSelect === !0, u = t.customOrder === !0, s = t.mandatory === !0, i = t.disabled === !0, r = t.editable !== !1, c = t.optionsLoaded === !0, d = t.options ?? [], m = t.emptyOptionLabel ?? "", f = u && o && !i && r, h = ue({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), E = h["js.dropdownSelect.nothingFound"], N = ge(
    ($) => h["js.dropdownSelect.removeChip"].replace("{0}", $),
    [h]
  ), [C, _] = Ie(!1), [y, w] = Ie(""), [x, v] = Ie(-1), [k, b] = Ie(!1), [T, M] = Ie({}), [R, U] = Ie(null), [Y, A] = Ie(null), [H, B] = Ie(null), j = tt(null), P = tt(null), I = tt(null), D = tt(a);
  D.current = a;
  const K = tt(-1), p = Kt(
    () => new Set(a.map(($) => $.value)),
    [a]
  ), L = Kt(() => {
    let $ = d.filter((q) => !p.has(q.value));
    if (y) {
      const q = y.toLowerCase();
      $ = $.filter((re) => re.label.toLowerCase().includes(q));
    }
    return $;
  }, [d, p, y]);
  Ue(() => {
    y && L.length === 1 ? v(0) : v(-1);
  }, [L.length, y]), Ue(() => {
    C && c && P.current && P.current.focus();
  }, [C, c, a]), Ue(() => {
    var re, ie;
    if (K.current < 0) return;
    const $ = K.current;
    K.current = -1;
    const q = (re = j.current) == null ? void 0 : re.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    q && q.length > 0 ? q[Math.min($, q.length - 1)].focus() : (ie = j.current) == null || ie.focus();
  }, [a]), Ue(() => {
    if (!C) return;
    const $ = (q) => {
      j.current && !j.current.contains(q.target) && I.current && !I.current.contains(q.target) && (_(!1), w(""));
    };
    return document.addEventListener("mousedown", $), () => document.removeEventListener("mousedown", $);
  }, [C]), Ue(() => {
    if (!C || !j.current) return;
    const $ = j.current.getBoundingClientRect(), q = window.innerHeight - $.bottom, ie = q < 300 && $.top > q;
    M({
      left: $.left,
      width: $.width,
      ...ie ? { bottom: window.innerHeight - $.top } : { top: $.bottom }
    });
  }, [C]);
  const z = ge(async () => {
    if (!(i || !r) && (_(!0), w(""), v(-1), b(!1), !c))
      try {
        await n("loadOptions");
      } catch {
        b(!0);
      }
  }, [i, r, c, n]), W = ge(() => {
    var $;
    _(!1), w(""), v(-1), ($ = j.current) == null || $.focus();
  }, []), Z = ge(
    ($) => {
      let q;
      if (o) {
        const re = d.find((ie) => ie.value === $);
        if (re)
          q = [...D.current, re];
        else
          return;
      } else {
        const re = d.find((ie) => ie.value === $);
        if (re)
          q = [re];
        else
          return;
      }
      D.current = q, n(ut, { value: q.map((re) => re.value) }), o ? (w(""), v(-1)) : W();
    },
    [o, d, n, W]
  ), F = ge(
    ($) => {
      K.current = D.current.findIndex((re) => re.value === $);
      const q = D.current.filter((re) => re.value !== $);
      D.current = q, n(ut, { value: q.map((re) => re.value) });
    },
    [n]
  ), te = ge(
    ($) => {
      $.stopPropagation(), n(ut, { value: [] }), W();
    },
    [n, W]
  ), se = ge(($) => {
    w($.target.value);
  }, []), le = ge(
    ($) => {
      if (!C) {
        if ($.key === "ArrowDown" || $.key === "ArrowUp" || $.key === "Enter" || $.key === " ") {
          if ($.target.tagName === "BUTTON") return;
          $.preventDefault(), $.stopPropagation(), z();
        }
        return;
      }
      switch ($.key) {
        case "ArrowDown":
          $.preventDefault(), $.stopPropagation(), v(
            (q) => q < L.length - 1 ? q + 1 : 0
          );
          break;
        case "ArrowUp":
          $.preventDefault(), $.stopPropagation(), v(
            (q) => q > 0 ? q - 1 : L.length - 1
          );
          break;
        case "Enter":
          $.preventDefault(), $.stopPropagation(), x >= 0 && x < L.length && Z(L[x].value);
          break;
        case "Escape":
          $.preventDefault(), $.stopPropagation(), W();
          break;
        case "Tab":
          W();
          break;
        case "Backspace":
          y === "" && o && a.length > 0 && F(a[a.length - 1].value);
          break;
      }
    },
    [
      C,
      z,
      W,
      L,
      x,
      Z,
      y,
      o,
      a,
      F
    ]
  ), Ee = ge(
    async ($) => {
      $.preventDefault(), b(!1);
      try {
        await n("loadOptions");
      } catch {
        b(!0);
      }
    },
    [n]
  ), we = ge(
    ($, q) => {
      U($), q.dataTransfer.effectAllowed = "move", q.dataTransfer.setData("text/plain", String($));
    },
    []
  ), pe = ge(
    ($, q) => {
      if (q.preventDefault(), q.dataTransfer.dropEffect = "move", R === null || R === $) {
        A(null), B(null);
        return;
      }
      const re = q.currentTarget.getBoundingClientRect(), ie = re.left + re.width / 2, qe = q.clientX < ie ? "before" : "after";
      A($), B(qe);
    },
    [R]
  ), Te = ge(
    ($) => {
      if ($.preventDefault(), R === null || Y === null || H === null || R === Y) return;
      const q = [...D.current], [re] = q.splice(R, 1);
      let ie = Y;
      R < Y ? ie = H === "before" ? ie - 1 : ie : ie = H === "before" ? ie : ie + 1, q.splice(ie, 0, re), D.current = q, n(ut, { value: q.map((qe) => qe.value) }), U(null), A(null), B(null);
    },
    [R, Y, H, n]
  ), Se = ge(() => {
    U(null), A(null), B(null);
  }, []);
  if (Ue(() => {
    if (x < 0 || !I.current) return;
    const $ = I.current.querySelector(
      `[id="${l}-opt-${x}"]`
    );
    $ && $.scrollIntoView({ block: "nearest" });
  }, [x, l]), !r)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, a.map(($) => /* @__PURE__ */ e.createElement("span", { key: $.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(qt, { image: $.image }), /* @__PURE__ */ e.createElement("span", null, $.label))));
  const He = !s && a.length > 0 && !i, Le = C ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: I,
      className: "tlDropdownSelect__dropdown",
      style: T,
      ...pl
    },
    (c || k) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: P,
        type: "text",
        className: "tlDropdownSelect__search",
        value: y,
        onChange: se,
        onKeyDown: le,
        placeholder: h["js.dropdownSelect.filterPlaceholder"],
        "aria-label": h["js.dropdownSelect.filterPlaceholder"],
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
      !c && !k && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      k && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: Ee }, h["js.dropdownSelect.error"])),
      c && L.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, E),
      c && L.map(($, q) => /* @__PURE__ */ e.createElement(
        io,
        {
          key: $.value,
          id: `${l}-opt-${q}`,
          option: $,
          highlighted: q === x,
          searchTerm: y,
          onSelect: Z,
          onMouseEnter: () => v(q)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: j,
      className: "tlDropdownSelect" + (C ? " tlDropdownSelect--open" : "") + (i ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": C,
      "aria-haspopup": "listbox",
      "aria-owns": C ? `${l}-listbox` : void 0,
      tabIndex: i ? -1 : 0,
      onClick: C ? void 0 : z,
      onKeyDown: le
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, a.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, m) : a.map(($, q) => {
      let re = "";
      return R === q ? re = "tlDropdownSelect__chip--dragging" : Y === q && H === "before" ? re = "tlDropdownSelect__chip--dropBefore" : Y === q && H === "after" && (re = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        co,
        {
          key: $.value,
          option: $,
          removable: !i && (o || !s),
          onRemove: F,
          removeLabel: N($.label),
          draggable: f,
          onDragStart: f ? (ie) => we(q, ie) : void 0,
          onDragOver: f ? (ie) => pe(q, ie) : void 0,
          onDrop: f ? Te : void 0,
          onDragEnd: f ? Se : void 0,
          dragClassName: f ? re : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, He && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: te,
        "aria-label": h["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, C ? "▲" : "▼"))
  ), Le && Rn.createPortal(Le, document.body));
}, { useCallback: At, useRef: mo } = e, Ln = "application/x-tl-color", po = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: a,
  onSwap: o,
  onReplace: u
}) => {
  const s = mo(null), i = At(
    (d) => (m) => {
      s.current = d, m.dataTransfer.effectAllowed = "move";
    },
    []
  ), r = At((d) => {
    d.preventDefault(), d.dataTransfer.dropEffect = "move";
  }, []), c = At(
    (d) => (m) => {
      m.preventDefault();
      const f = m.dataTransfer.getData(Ln);
      f ? u(d, f) : s.current !== null && s.current !== d && o(s.current, d), s.current = null;
    },
    [o, u]
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
        onDragStart: d != null ? i(m) : void 0,
        onDragOver: r,
        onDrop: c(m)
      }
    ))
  );
};
function xn(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function Yt(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function Mn(l) {
  if (!Yt(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function In(l, t, n) {
  const a = (o) => xn(o).toString(16).padStart(2, "0");
  return "#" + a(l) + a(t) + a(n);
}
function fo(l, t, n) {
  const a = l / 255, o = t / 255, u = n / 255, s = Math.max(a, o, u), i = Math.min(a, o, u), r = s - i;
  let c = 0;
  r !== 0 && (s === a ? c = (o - u) / r % 6 : s === o ? c = (u - a) / r + 2 : c = (a - o) / r + 4, c *= 60, c < 0 && (c += 360));
  const d = s === 0 ? 0 : r / s;
  return [c, d, s];
}
function ho(l, t, n) {
  const a = n * t, o = a * (1 - Math.abs(l / 60 % 2 - 1)), u = n - a;
  let s = 0, i = 0, r = 0;
  return l < 60 ? (s = a, i = o, r = 0) : l < 120 ? (s = o, i = a, r = 0) : l < 180 ? (s = 0, i = a, r = o) : l < 240 ? (s = 0, i = o, r = a) : l < 300 ? (s = o, i = 0, r = a) : (s = a, i = 0, r = o), [
    Math.round((s + u) * 255),
    Math.round((i + u) * 255),
    Math.round((r + u) * 255)
  ];
}
function bo(l) {
  return fo(...Mn(l));
}
function Bt(l, t, n) {
  return In(...ho(l, t, n));
}
const { useCallback: Ve, useRef: vn } = e, go = ({ color: l, onColorChange: t }) => {
  const [n, a, o] = bo(l), u = vn(null), s = vn(null), i = Ve(
    (E, N) => {
      var w;
      const C = (w = u.current) == null ? void 0 : w.getBoundingClientRect();
      if (!C) return;
      const _ = Math.max(0, Math.min(1, (E - C.left) / C.width)), y = Math.max(0, Math.min(1, 1 - (N - C.top) / C.height));
      t(Bt(n, _, y));
    },
    [n, t]
  ), r = Ve(
    (E) => {
      E.preventDefault(), E.target.setPointerCapture(E.pointerId), i(E.clientX, E.clientY);
    },
    [i]
  ), c = Ve(
    (E) => {
      E.buttons !== 0 && i(E.clientX, E.clientY);
    },
    [i]
  ), d = Ve(
    (E) => {
      var y;
      const N = (y = s.current) == null ? void 0 : y.getBoundingClientRect();
      if (!N) return;
      const _ = Math.max(0, Math.min(1, (E - N.top) / N.height)) * 360;
      t(Bt(_, a, o));
    },
    [a, o, t]
  ), m = Ve(
    (E) => {
      E.preventDefault(), E.target.setPointerCapture(E.pointerId), d(E.clientY);
    },
    [d]
  ), f = Ve(
    (E) => {
      E.buttons !== 0 && d(E.clientY);
    },
    [d]
  ), h = Bt(n, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: u,
      className: "tlColorInput__svField",
      style: { backgroundColor: h },
      onPointerDown: r,
      onPointerMove: c
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__svHandle",
        style: { left: `${a * 100}%`, top: `${(1 - o) * 100}%` }
      }
    )
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: s,
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
function Eo(l, t) {
  const n = t.toUpperCase();
  return l.some((a) => a != null && a.toUpperCase() === n);
}
const vo = {
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
}, { useState: ht, useCallback: ke, useEffect: _n, useRef: _o, useLayoutEffect: Co } = e, yo = ({
  anchorRef: l,
  currentColor: t,
  palette: n,
  paletteColumns: a,
  defaultPalette: o,
  canReset: u,
  onConfirm: s,
  onCancel: i,
  onPaletteChange: r
}) => {
  const [c, d] = ht("palette"), [m, f] = ht(t), h = _o(null), E = ue(vo), [N, C] = ht(null);
  Co(() => {
    if (!l.current || !h.current) return;
    const I = l.current.getBoundingClientRect(), D = h.current.getBoundingClientRect();
    let K = I.bottom + 4, p = I.left;
    K + D.height > window.innerHeight && (K = I.top - D.height - 4), p + D.width > window.innerWidth && (p = Math.max(0, I.right - D.width)), C({ top: K, left: p });
  }, [l]);
  const _ = m != null, [y, w, x] = _ ? Mn(m) : [0, 0, 0], [v, k] = ht((m == null ? void 0 : m.toUpperCase()) ?? "");
  _n(() => {
    k((m == null ? void 0 : m.toUpperCase()) ?? "");
  }, [m]), $e(!0, { ESCAPE: i }), _n(() => {
    const I = (K) => {
      h.current && !h.current.contains(K.target) && i();
    }, D = setTimeout(() => document.addEventListener("mousedown", I), 0);
    return () => {
      clearTimeout(D), document.removeEventListener("mousedown", I);
    };
  }, [i]);
  const b = ke(
    (I) => (D) => {
      const K = parseInt(D.target.value, 10);
      if (isNaN(K)) return;
      const p = xn(K);
      f(In(I === "r" ? p : y, I === "g" ? p : w, I === "b" ? p : x));
    },
    [y, w, x]
  ), T = ke(
    (I) => {
      if (m != null) {
        I.dataTransfer.setData(Ln, m.toUpperCase()), I.dataTransfer.effectAllowed = "move";
        const D = document.createElement("div");
        D.style.width = "33px", D.style.height = "33px", D.style.backgroundColor = m, D.style.borderRadius = "3px", D.style.border = "1px solid rgba(0,0,0,0.1)", D.style.position = "absolute", D.style.top = "-9999px", document.body.appendChild(D), I.dataTransfer.setDragImage(D, 16, 16), requestAnimationFrame(() => document.body.removeChild(D));
      }
    },
    [m]
  ), M = ke((I) => {
    const D = I.target.value;
    k(D), Yt(D) && f(D);
  }, []), R = ke(() => {
    f(null);
  }, []), U = ke((I) => {
    f(I);
  }, []), Y = ke(
    (I) => {
      s(I);
    },
    [s]
  ), A = ke(
    (I, D) => {
      const K = [...n], p = K[I];
      K[I] = K[D], K[D] = p, r(K);
    },
    [n, r]
  ), H = ke(
    (I, D) => {
      const K = [...n];
      K[I] = D, r(K);
    },
    [n, r]
  ), B = ke(() => {
    r([...o]);
  }, [o, r]), j = ke(
    (I) => {
      if (Eo(n, I)) return;
      const D = n.indexOf(null);
      if (D < 0) return;
      const K = [...n];
      K[D] = I.toUpperCase(), r(K);
    },
    [n, r]
  ), P = ke(() => {
    m != null && j(m), s(m);
  }, [m, s, j]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: h,
      style: N ? { top: N.top, left: N.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (c === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => d("palette")
      },
      E["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (c === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => d("mixer")
      },
      E["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, c === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      po,
      {
        colors: n,
        columns: a,
        onSelect: U,
        onConfirm: Y,
        onSwap: A,
        onReplace: H
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: B }, E["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(go, { color: m ?? "#000000", onColorChange: f }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, E["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, E["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (_ ? "" : " tlColorInput--noColor"),
        style: _ ? { backgroundColor: m } : void 0,
        draggable: _,
        onDragStart: _ ? T : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, E["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: _ ? y : "",
        onChange: b("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, E["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: _ ? w : "",
        onChange: b("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, E["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: _ ? x : "",
        onChange: b("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, E["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (v !== "" && !Yt(v) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: v,
        onChange: M
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, u && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: R }, E["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: i }, E["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: P }, E["js.colorInput.ok"]))
  );
}, wo = { "js.colorInput.chooseColor": "Choose color" }, { useState: ko, useCallback: bt, useRef: No } = e, So = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), o = ae(), u = ue(wo), [s, i] = ko(!1), r = No(null), c = n, d = t.editable !== !1, m = t.palette ?? [], f = t.paletteColumns ?? 6, h = t.defaultPalette ?? m, E = bt(() => {
    d && i(!0);
  }, [d]), N = bt(
    (y) => {
      i(!1), a(y);
    },
    [a]
  ), C = bt(() => {
    i(!1);
  }, []), _ = bt(
    (y) => {
      o("paletteChanged", { palette: y });
    },
    [o]
  );
  return d ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: r,
      className: "tlColorInput__swatch" + (c == null ? " tlColorInput__swatch--noColor" : ""),
      style: c != null ? { backgroundColor: c } : void 0,
      onClick: E,
      disabled: t.disabled === !0,
      title: c ?? "",
      "aria-label": u["js.colorInput.chooseColor"]
    }
  ), s && /* @__PURE__ */ e.createElement(
    yo,
    {
      anchorRef: r,
      currentColor: c,
      palette: m,
      paletteColumns: f,
      defaultPalette: h,
      canReset: t.canReset !== !1,
      onConfirm: N,
      onCancel: C,
      onPaletteChange: _
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
}, { useState: nt, useCallback: Be, useEffect: Ft, useRef: Cn, useLayoutEffect: Do, useMemo: To } = e, Ro = {
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
}, Lo = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: a,
  onSelect: o,
  onCancel: u,
  onLoadIcons: s
}) => {
  const i = ue(Ro), [r, c] = nt("simple"), [d, m] = nt(""), [f, h] = nt(t ?? ""), [E, N] = nt(!1), [C, _] = nt(null), y = Cn(null), w = Cn(null);
  Do(() => {
    if (!l.current || !y.current) return;
    const Y = l.current.getBoundingClientRect(), A = y.current.getBoundingClientRect();
    let H = Y.bottom + 4, B = Y.left;
    H + A.height > window.innerHeight && (H = Y.top - A.height - 4), B + A.width > window.innerWidth && (B = Math.max(0, Y.right - A.width)), _({ top: H, left: B });
  }, [l]), Ft(() => {
    !a && !E && s().catch(() => N(!0));
  }, [a, E, s]), Ft(() => {
    a && w.current && w.current.focus();
  }, [a]), $e(!0, { ESCAPE: u }), Ft(() => {
    const Y = (H) => {
      y.current && !y.current.contains(H.target) && u();
    }, A = setTimeout(() => document.addEventListener("mousedown", Y), 0);
    return () => {
      clearTimeout(A), document.removeEventListener("mousedown", Y);
    };
  }, [u]);
  const x = To(() => {
    if (!d) return n;
    const Y = d.toLowerCase();
    return n.filter(
      (A) => A.prefix.toLowerCase().includes(Y) || A.label.toLowerCase().includes(Y) || A.terms != null && A.terms.some((H) => H.includes(Y))
    );
  }, [n, d]), v = Be((Y) => {
    m(Y.target.value);
  }, []), k = Be(
    (Y) => {
      o(Y);
    },
    [o]
  ), b = Be((Y) => {
    h(Y);
  }, []), T = Be((Y) => {
    h(Y.target.value);
  }, []), M = Be(() => {
    o(f || null);
  }, [f, o]), R = Be(() => {
    o(null);
  }, [o]), U = Be(async (Y) => {
    Y.preventDefault(), N(!1);
    try {
      await s();
    } catch {
      N(!0);
    }
  }, [s]);
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
        onClick: () => c("simple")
      },
      i["js.iconSelect.simpleTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (r === "advanced" ? " tlIconSelect__tab--active" : ""),
        onClick: () => c("advanced")
      },
      i["js.iconSelect.advancedTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__searchIcon", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-magnifying-glass" })), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: w,
        type: "text",
        className: "tlIconSelect__search",
        value: d,
        onChange: v,
        placeholder: i["js.iconSelect.filterPlaceholder"],
        "aria-label": i["js.iconSelect.filterPlaceholder"]
      }
    ), d && /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__resetBtn",
        onClick: () => m(""),
        title: i["js.iconSelect.clearFilter"]
      },
      "×"
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlIconSelect__grid",
        role: "listbox"
      },
      !a && !E && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      E && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: U }, i["js.iconSelect.loadError"])),
      a && x.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, i["js.iconSelect.noResults"]),
      a && x.map(
        (Y) => Y.variants.map((A) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: A.encoded,
            className: "tlIconSelect__iconCell" + (A.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": A.encoded === t,
            tabIndex: 0,
            title: Y.label,
            onClick: () => r === "simple" ? k(A.encoded) : b(A.encoded),
            onKeyDown: (H) => {
              (H.key === "Enter" || H.key === " ") && (H.preventDefault(), r === "simple" ? k(A.encoded) : b(A.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(Ne, { encoded: A.encoded })
        ))
      )
    ),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, i["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: f,
        onChange: T
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, i["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, f && /* @__PURE__ */ e.createElement(Ne, { encoded: f })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, f ? f.startsWith("css:") ? f.substring(4) : f : ""))),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: u }, i["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: R }, i["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: M }, i["js.iconSelect.ok"]))
  );
}, xo = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: Mo, useCallback: gt, useRef: Io } = e, jo = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), o = ae(), u = ue(xo), [s, i] = Mo(!1), r = Io(null), c = n, d = t.editable !== !1, m = t.disabled === !0, f = t.icons ?? [], h = t.iconsLoaded === !0, E = gt(() => {
    d && !m && i(!0);
  }, [d, m]), N = gt(
    (y) => {
      i(!1), a(y);
    },
    [a]
  ), C = gt(() => {
    i(!1);
  }, []), _ = gt(async () => {
    await o("loadIcons");
  }, [o]);
  return d ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: r,
      className: "tlIconSelect__swatch" + (c == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: E,
      disabled: m,
      title: c ?? "",
      "aria-label": u["js.iconSelect.chooseIcon"]
    },
    c ? /* @__PURE__ */ e.createElement(Ne, { encoded: c }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), s && /* @__PURE__ */ e.createElement(
    Lo,
    {
      anchorRef: r,
      currentValue: c,
      icons: f,
      iconsLoaded: h,
      onSelect: N,
      onCancel: C,
      onLoadIcons: _
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, c ? /* @__PURE__ */ e.createElement(Ne, { encoded: c }) : null));
}, { useCallback: ze, useEffect: Po, useMemo: yn, useRef: Ao, useState: Ot } = e, Bo = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, Fo = [1, 2, 3, 4];
function Oo(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const a = parseFloat(n[1]), o = n[2] || "px";
  return o === "rem" || o === "em" ? a * t : a;
}
function $o(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let a = 1;
  for (const o of Fo)
    n >= o && (a = o);
  return a;
}
function Ho(l, t) {
  const n = Bo[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function Wo(l, t) {
  const n = Math.max(1, t), a = {}, o = (m, f) => !!(a[m] && a[m][f]), u = (m, f) => {
    a[m] || (a[m] = {}), a[m][f] = !0;
  }, s = [];
  let i = 0, r = 0;
  const c = (m) => {
    let f = null;
    for (const E of s) E.rowStart === m && (f = E);
    if (!f) return;
    let h = f.colEnd;
    for (; h < n && !o(m, h); ) h++;
    if (h !== f.colEnd) {
      for (let E = f.rowStart; E < f.rowEnd; E++)
        for (let N = f.colEnd; N < h; N++) u(E, N);
      f.colEnd = h;
    }
  };
  for (const m of l) {
    const f = n <= 1 ? 1 : Math.max(1, m.rowSpan || 1);
    let h = Math.min(Ho(m.width, n), n);
    for (; o(i, r); )
      r++, r >= n && (r = 0, i++);
    let E = 0;
    for (let w = r; w < n && !o(i, w); w++)
      E++;
    if (h > E) {
      for (c(i), r = 0, i++; o(i, r); )
        r++, r >= n && (r = 0, i++);
      E = 0;
      for (let w = r; w < n && !o(i, w); w++)
        E++;
      h = Math.min(h, E);
    }
    const N = r, C = r + h, _ = i, y = i + f;
    s.push({ id: m.id, colStart: N, colEnd: C, rowStart: _, rowEnd: y });
    for (let w = _; w < y; w++)
      for (let x = N; x < C; x++) u(w, x);
    r = C, r >= n && (r = 0, i++);
  }
  c(i);
  let d = 0;
  for (const m of s) m.rowEnd > d && (d = m.rowEnd);
  for (let m = 1; m < d; m++)
    for (let f = 0; f < n; f++) {
      if (o(m, f)) continue;
      const h = s.find((E) => E.rowEnd === m && E.colStart <= f && f < E.colEnd);
      if (h) {
        h.rowEnd = m + 1;
        for (let E = h.colStart; E < h.colEnd; E++) u(m, E);
      }
    }
  return s;
}
const Uo = ({ controlId: l }) => {
  const t = X(), n = ae(), a = t.minColWidth ?? "16rem", o = (t.children ?? []).filter((k) => k && k.id), u = Ao(null), [s, i] = Ot(1), r = t.editMode === !0;
  Po(() => {
    const k = u.current;
    if (!k) return;
    const b = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, T = Oo(a, b), M = () => i($o(k.clientWidth, T));
    M();
    const R = new ResizeObserver(M);
    return R.observe(k), () => R.disconnect();
  }, [a]);
  const c = yn(() => Wo(o, s), [o, s]), d = yn(() => {
    const k = {};
    for (const b of c) k[b.id] = b;
    return k;
  }, [c]), [m, f] = Ot(null), [h, E] = Ot(null), N = ze((k, b) => {
    if (!r) {
      k.preventDefault();
      return;
    }
    f(b), k.dataTransfer.effectAllowed = "move", k.dataTransfer.setData("text/plain", b);
  }, [r]), C = ze((k, b) => {
    if (!r || !m || m === b) return;
    k.preventDefault(), k.dataTransfer.dropEffect = "move";
    const T = k.currentTarget.getBoundingClientRect(), M = k.clientX < T.left + T.width / 2;
    E((R) => R && R.id === b && R.before === M ? R : { id: b, before: M });
  }, [r, m]), _ = ze(() => {
  }, []), y = ze((k, b, T) => {
    const M = o.map((A) => A.id), R = M.indexOf(k);
    if (R < 0) return;
    M.splice(R, 1);
    const U = M.indexOf(b);
    if (U < 0) {
      M.splice(R, 0, k);
      return;
    }
    const Y = T ? U : U + 1;
    M.splice(Y, 0, k), n("reorder", { order: M });
  }, [o, n]), w = ze((k, b) => {
    if (!r || !m || m === b) return;
    k.preventDefault();
    const T = k.currentTarget.getBoundingClientRect(), M = k.clientX < T.left + T.width / 2;
    y(m, b, M), f(null), E(null);
  }, [r, m, y]), x = ze(() => {
    f(null), E(null);
  }, []), v = {
    display: "grid",
    gridTemplateColumns: `repeat(${s}, 1fr)`,
    gap: "1rem"
  };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: u,
      className: "tlDashboard" + (r ? " tlDashboard--edit" : "")
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: v }, o.map((k) => {
      const b = d[k.id];
      if (!b) return null;
      const T = {
        gridColumn: `${b.colStart + 1} / ${b.colEnd + 1}`,
        gridRow: `${b.rowStart + 1} / ${b.rowEnd + 1}`
      }, M = ["tlDashboard__tile"];
      return m === k.id && M.push("tlDashboard__tile--dragging"), h && h.id === k.id && M.push(h.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: k.id,
          className: M.join(" "),
          style: T,
          draggable: r,
          onDragStart: (R) => N(R, k.id),
          onDragOver: (R) => C(R, k.id),
          onDragLeave: _,
          onDrop: (R) => w(R, k.id),
          onDragEnd: x
        },
        /* @__PURE__ */ e.createElement(G, { control: k.control }),
        r && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: Vo, useRef: wn, useState: kn, useEffect: zo, useLayoutEffect: Ko } = e, Yo = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, a) => /* @__PURE__ */ e.createElement("span", { key: a, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(G, { control: n }))));
}, Go = ({ group: l }) => {
  var m, f;
  const [t, n] = kn(!1), [a, o] = kn({}), u = wn(null), s = wn(null), i = Vo(() => {
    n((h) => !h);
  }, []);
  Ko(() => {
    if (!t) return;
    const h = () => {
      const E = u.current;
      if (!E) return;
      const N = E.getBoundingClientRect();
      o({
        position: "fixed",
        top: N.bottom + 4,
        right: Math.max(8, window.innerWidth - N.right),
        left: "auto"
      });
    };
    return h(), window.addEventListener("resize", h), window.addEventListener("scroll", h, !0), () => {
      window.removeEventListener("resize", h), window.removeEventListener("scroll", h, !0);
    };
  }, [t]), zo(() => {
    if (!t) return;
    const h = (E) => {
      s.current && !s.current.contains(E.target) && u.current && !u.current.contains(E.target) && n(!1);
    };
    return document.addEventListener("mousedown", h), () => document.removeEventListener("mousedown", h);
  }, [t]), $e(t, { ESCAPE: () => n(!1) }), Xt(t, s, "first");
  const r = l.items.filter((h) => h != null);
  if (r.length === 0) return null;
  if (r.length === 1 && !((m = l.subGroups) != null && m.length) && !l.icon)
    return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(G, { control: r[0] })));
  const c = l.label ?? l.name, d = !!l.icon;
  return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--menu" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: u,
      type: "button",
      className: "tlToolbar__menuTrigger" + (d ? " tlToolbar__menuTrigger--icon" : ""),
      onMouseDown: (h) => h.preventDefault(),
      onClick: i,
      "aria-expanded": t,
      "aria-haspopup": "true",
      "aria-label": d ? c : void 0,
      title: d ? c : void 0
    },
    d ? /* @__PURE__ */ e.createElement(Ne, { encoded: l.icon, className: "tlToolbar__menuIcon" }) : /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement("span", null, c), /* @__PURE__ */ e.createElement("svg", { className: "tlToolbar__chevron", viewBox: "0 0 24 24", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("polyline", { points: "6,9 12,15 18,9" })))
  ), Rn.createPortal(
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
      r.map((h, E) => /* @__PURE__ */ e.createElement("div", { key: E, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(G, { control: h }))),
      (f = l.subGroups) == null ? void 0 : f.map((h, E) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${E}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), h.items.map((N, C) => /* @__PURE__ */ e.createElement("div", { key: C, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(G, { control: N })))))
    ),
    document.body
  ));
}, Xo = ({ controlId: l }) => {
  const a = (X().groups ?? []).filter((o) => o.items.some((u) => u != null));
  return a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, a.map((o, u) => /* @__PURE__ */ e.createElement(e.Fragment, { key: o.name }, u > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), o.display === "menu" ? /* @__PURE__ */ e.createElement(Go, { group: o }) : /* @__PURE__ */ e.createElement(Yo, { group: o }))));
}, qo = ({ frame: l, covered: t }) => {
  const [n, a] = rt(), o = [
    "tlTileStack__frame",
    t ? "tlTileStack__frame--covered" : "",
    n
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(ot, { host: a }, /* @__PURE__ */ e.createElement("div", { className: o }, /* @__PURE__ */ e.createElement(G, { control: l })));
}, Zo = ({ controlId: l }) => {
  const t = X(), [n, a] = rt(), o = t.frames ?? [], u = t.activeIndex ?? 0;
  return /* @__PURE__ */ e.createElement(ot, { host: a }, /* @__PURE__ */ e.createElement("div", { id: l, className: n ? "tlTileStack " + n : "tlTileStack" }, o.map((s, i) => /* @__PURE__ */ e.createElement(qo, { key: s.controlId, frame: s, covered: i !== u }))));
}, Qo = ({ controlId: l }) => {
  const t = X(), n = ae(), a = t.content, o = t.breadcrumb ?? null;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAdaptiveDetail" }, o && o.length > 0 && /* @__PURE__ */ e.createElement("nav", { className: "tlAdaptiveDetail__breadcrumb", "aria-label": "Breadcrumb" }, o.map((u, s) => {
    const i = s === o.length - 1;
    return /* @__PURE__ */ e.createElement(e.Fragment, { key: u.depth }, s > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__sep" }, "›"), i ? /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__crumb tlAdaptiveDetail__crumb--current" }, u.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlAdaptiveDetail__crumb",
        onClick: () => n("navigate", { depth: u.depth })
      },
      u.label
    ));
  })), /* @__PURE__ */ e.createElement("div", { className: "tlAdaptiveDetail__content" }, a && /* @__PURE__ */ e.createElement(G, { control: a })));
}, Jo = ({ controlId: l }) => {
  const n = X().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((a, o) => /* @__PURE__ */ e.createElement(G, { key: o, control: a })));
}, es = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), ts = {
  "js.sidebar.openDrawer": "Open navigation"
}, ns = ({ controlId: l }) => {
  const t = ae(), n = ue(ts);
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
V("TLButton", Ml);
V("TLUploadButton", Il);
V("TLToggleButton", Pl);
V("TLTextInput", bl);
V("TLPasswordInput", El);
V("TLNumberInput", _l);
V("TLDatePicker", yl);
V("TLSelect", kl);
V("TLBooleanChoice", Sl);
V("TLCheckbox", Ll);
V("TLCounter", Al);
V("TLTabBar", Fl);
V("TLFieldList", Ol);
V("TLAudioRecorder", Hl);
V("TLAudioPlayer", Ul);
V("TLFileUpload", zl);
V("TLBinaryField", Yl);
V("TLFileChips", ql);
V("TLRelativeTime", Jl);
V("TLAnchor", ea);
V("TLScrollLink", ta);
V("TLAvatar", aa);
V("TLDownload", oa);
V("TLPhotoCapture", ca);
V("TLPhotoViewer", ua);
V("TLPdfViewer", ma);
V("TLSplitPanel", pa);
V("TLPanel", _a);
V("TLInset", Ma);
V("TLMaximizeRoot", Ca);
V("TLDeckPane", ya);
V("TLSidebar", La);
V("TLStack", xa);
V("TLGrid", Ia);
V("TLCard", ja);
V("TLAppBar", Pa);
V("TLBreadcrumb", Ba);
V("TLBottomBar", Oa);
V("TLDialog", Wa);
V("TLDialogManager", za);
V("TLWindow", Xa);
V("TLDrawer", Qa);
V("TLMenuRegion", er);
V("TLSnackbar", ar);
V("TLNoticeBar", dr);
V("TLMenu", pr);
V("TLAppShell", hr);
V("TLText", br);
V("TLTableView", yr);
V("TLColumnSelect", kr);
V("TLCalendar", Fr);
V("TLFormLayout", Yr);
V("TLFormGroup", qr);
V("TLFormField", eo);
V("TLResourceCell", to);
V("TLTreeView", lo);
V("TLDropdownSelect", uo);
V("TLColorInput", So);
V("TLIconSelect", jo);
V("TLDashboard", Uo);
V("TLToolbar", Xo);
V("TLTileStack", Zo);
V("TLAdaptiveDetail", Qo);
V("TLSlot", Jo);
V("TLSlotContent", es);
V("TLDrawerToggle", ns);
