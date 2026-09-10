import { React as e, useTLFieldValue as De, useTLCommand as le, useTLState as X, useKeyboardBinding as me, useTLUpload as Ye, useFill as yt, FillBarrier as Be, TLChild as G, useI18N as ue, useTLDataUrl as Ge, scrollToAnchor as al, useStandaloneKeyboardScope as Oe, useFillHost as at, FillProvider as rt, KeyboardScopeProvider as Kt, useFocusTrap as Yt, CMD_VALUE_CHANGED as ct, anchoredOverlayProps as rl, register as z } from "tl-react-bridge";
const { useCallback: en, useRef: ol } = e, sl = 300, cl = ({ controlId: l, state: t }) => {
  const [n, a, o] = De({
    debounceMs: sl,
    sendOnBlur: t.sendValueOnBlur === !0
  }), i = le(), s = ol(!1), u = en(
    (D) => {
      s.current = !0, a(D.target.value);
    },
    [a]
  ), r = t.commitOnBlur === !0, c = en(async () => {
    await o(), r && s.current && (s.current = !1, i("commit"));
  }, [o, r, i]), d = t.multiline === !0;
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
  const m = t.hasError === !0, f = t.hasWarnings === !0, h = t.errorMessage, b = [
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
}, { useCallback: tn } = e, il = 300, ul = ({ controlId: l, state: t }) => {
  const [n, a, o] = De({ debounceMs: il }), i = tn(
    (m) => {
      a(m.target.value);
    },
    [a]
  ), s = tn(() => {
    o();
  }, [o]);
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, "••••••••");
  const u = t.hasError === !0, r = t.hasWarnings === !0, c = t.errorMessage, d = [
    "tlReactTextInput",
    u ? "tlReactTextInput--error" : "",
    !u && r ? "tlReactTextInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "password",
      value: n ?? "",
      onChange: i,
      onBlur: s,
      disabled: t.disabled === !0,
      className: d,
      "aria-invalid": u || void 0,
      title: u && c ? c : void 0
    }
  ));
}, { useCallback: nn } = e, dl = 300, ml = ({ controlId: l, state: t }) => {
  const [n, a, o] = De({
    debounceMs: dl,
    sendOnBlur: t.sendValueOnBlur === !0
  }), i = nn(
    (f) => {
      const h = f.target.value;
      a(h === "" ? null : h);
    },
    [a]
  ), s = nn(() => {
    o();
  }, [o]), u = n == null ? "" : String(n);
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, u);
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
      value: u,
      onChange: i,
      onBlur: s,
      disabled: t.disabled === !0,
      className: m,
      "aria-invalid": r || void 0,
      title: r && d ? d : void 0
    }
  ));
}, { useCallback: pl } = e, fl = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = pl(
    (r) => {
      a(r.target.value || null);
    },
    [a]
  );
  if (t.editable === !1) {
    const r = t.displayValue ?? n ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, r);
  }
  const i = t.hasError === !0, s = t.hasWarnings === !0, u = [
    "tlReactDatePicker",
    i ? "tlReactDatePicker--error" : "",
    !i && s ? "tlReactDatePicker--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: t.inputType ?? "date",
      value: n ?? "",
      onChange: o,
      disabled: t.disabled === !0,
      className: u,
      "aria-invalid": i || void 0
    }
  ));
}, { useCallback: hl } = e, bl = ({ controlId: l, state: t, config: n }) => {
  var d;
  const [a, o] = De(), i = hl(
    (m) => {
      o(m.target.value || null);
    },
    [o]
  ), s = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const m = ((d = s.find((f) => f.value === a)) == null ? void 0 : d.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, m);
  }
  const u = t.hasError === !0, r = t.hasWarnings === !0, c = [
    "tlReactSelect",
    u ? "tlReactSelect--error" : "",
    !u && r ? "tlReactSelect--warning" : ""
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
    s.map((m) => /* @__PURE__ */ e.createElement("option", { key: m.value, value: m.value }, m.label))
  ));
}, { useCallback: gl } = e, El = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = t.options ?? [], i = t.presentation === "select", s = t.disabled === !0, u = t.hasError === !0, r = t.hasWarnings === !0, c = gl(
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
    u ? "tlBooleanChoice--error" : "",
    !u && r ? "tlBooleanChoice--warning" : ""
  ].filter(Boolean).join(" ");
  return i ? /* @__PURE__ */ e.createElement(
    "select",
    {
      id: l,
      className: m + " tlReactSelect",
      value: d >= 0 ? String(d) : "",
      disabled: s,
      "aria-invalid": u || void 0,
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
      "aria-invalid": u || void 0
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
}, { useCallback: vl, useRef: _l, useEffect: Cl } = e, yl = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = t.triState === !0, i = _l(null);
  Cl(() => {
    i.current && (i.current.indeterminate = o && n !== !0 && n !== !1);
  }, [o, n]);
  const s = vl(
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
        ref: i,
        checked: n === !0,
        disabled: !0,
        className: "tlReactCheckbox tlReactCheckbox--immutable"
      }
    );
  const u = t.hasError === !0, r = t.hasWarnings === !0, c = [
    "tlReactCheckbox",
    u ? "tlReactCheckbox--error" : "",
    !u && r ? "tlReactCheckbox--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: l,
      ref: i,
      checked: n === !0,
      onChange: s,
      disabled: t.disabled === !0,
      className: c,
      "aria-invalid": u || void 0,
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
const { useCallback: wl } = e, kl = ({ controlId: l, command: t, label: n, image: a, disabled: o, displayMode: i }) => {
  const s = X(), u = le(), r = t ?? "click", c = n ?? s.label, d = a ?? s.image, m = o ?? s.disabled === !0, f = i ?? s.displayMode ?? "label-only", h = s.hidden === !0, b = s.tooltip, D = s.appearance, _ = s.size, E = s.cssClasses, w = s.navigateUrl, k = wl(() => {
    if (w) {
      window.location.assign(w);
      return;
    }
    u(r);
  }, [u, r, w]), I = s.keyGesture;
  me(I, () => m || h ? !1 : (k(), !0));
  const y = f === "icon-only", v = f === "label-only" || f === "icon-label" || y && !d, C = b ?? (y ? c : void 0), N = C ? `text:${C}` : void 0;
  return h ? null : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: k,
      disabled: m,
      className: "tlReactButton" + (y ? " tlReactButton--iconOnly" : "") + (f === "label-only" ? " tlReactButton--labelOnly" : "") + (D === "link" ? " tlReactButton--link" : "") + (D === "primary" ? " tlReactButton--primary" : "") + (_ === "small" ? " tlReactButton--small" : "") + (_ === "large" ? " tlReactButton--large" : "") + (E ? " " + E : ""),
      "data-tooltip": N,
      "aria-label": d || y ? c : void 0
    },
    d && /* @__PURE__ */ e.createElement(Ne, { encoded: d, className: "tlReactButton__image" }),
    v && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, c)
  );
}, Nl = ({ controlId: l }) => {
  const t = X(), n = Ye(), a = e.useRef(null), [o, i] = e.useState(!1), s = t.label ?? "", u = t.image, r = t.disabled === !0, c = t.hidden === !0, d = t.displayMode ?? "label-only", m = t.appearance, f = t.accept, h = t.multiple === !0, b = e.useCallback(() => {
    var I;
    r || o || (I = a.current) == null || I.click();
  }, [r, o]), D = e.useCallback(async (I) => {
    const y = I.target.files;
    if (!y || y.length === 0) return;
    const v = new FormData();
    for (let C = 0; C < y.length; C++)
      v.append("file", y[C], y[C].name);
    I.target.value = "", i(!0);
    try {
      await n(v);
    } finally {
      i(!1);
    }
  }, [n]), _ = d === "icon-only", E = d === "icon-only" || d === "icon-label", w = d === "label-only" || d === "icon-label" || _ && !u, k = r || o;
  return /* @__PURE__ */ e.createElement("span", { id: l, style: { display: "contents" } }, /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: a,
      type: "file",
      accept: f && f !== "*" ? f : void 0,
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
      className: "tlReactButton" + (_ ? " tlReactButton--iconOnly" : "") + (m === "link" ? " tlReactButton--link" : "") + (m === "primary" ? " tlReactButton--primary" : ""),
      "aria-label": _ ? s : void 0
    },
    E && u && /* @__PURE__ */ e.createElement(Ne, { encoded: u, className: "tlReactButton__image" }),
    w && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, s)
  ));
}, { useCallback: Sl } = e, Dl = ({ controlId: l, command: t, label: n, active: a, disabled: o }) => {
  const i = X(), s = le(), u = t ?? "click", r = n ?? i.label, c = a ?? i.active === !0, d = o ?? i.disabled === !0, m = Sl(() => {
    s(u);
  }, [s, u]);
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
}, Tl = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.count ?? 0, o = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Rl } = e, Ll = ({ controlId: l }) => {
  const t = X(), n = le(), a = yt(!0), o = t.tabs ?? [], i = t.activeTabId, s = Rl((u) => {
    u !== i && n("selectTab", { tabId: u });
  }, [n, i]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar " + a }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, o.map((u) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: u.id,
      role: "tab",
      "aria-selected": u.id === i,
      className: "tlReactTabBar__tab" + (u.id === i ? " tlReactTabBar__tab--active" : ""),
      onClick: () => s(u.id)
    },
    u.icon && /* @__PURE__ */ e.createElement(Ne, { encoded: u.icon, className: "tlReactTabBar__tabIcon" }),
    u.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, /* @__PURE__ */ e.createElement(Be, null, t.activeContent && /* @__PURE__ */ e.createElement(G, { control: t.activeContent }))));
}, xl = ({ controlId: l }) => {
  const t = X(), n = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((o, i) => /* @__PURE__ */ e.createElement("div", { key: i, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(G, { control: o })))));
}, Ml = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Il = ({ controlId: l }) => {
  const t = X(), n = Ye(), [a, o] = e.useState("idle"), [i, s] = e.useState(null), u = e.useRef(null), r = e.useRef([]), c = e.useRef(null), d = t.status ?? "idle", m = t.error, f = d === "received" ? "idle" : a !== "idle" ? a : d, h = e.useCallback(async () => {
    if (a === "recording") {
      const w = u.current;
      w && w.state !== "inactive" && w.stop();
      return;
    }
    if (a !== "uploading") {
      if (s(null), !window.isSecureContext || !navigator.mediaDevices) {
        s("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const w = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        c.current = w, r.current = [];
        const k = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", I = new MediaRecorder(w, k ? { mimeType: k } : void 0);
        u.current = I, I.ondataavailable = (y) => {
          y.data.size > 0 && r.current.push(y.data);
        }, I.onstop = async () => {
          w.getTracks().forEach((C) => C.stop()), c.current = null;
          const y = new Blob(r.current, { type: I.mimeType || "audio/webm" });
          if (r.current = [], y.size === 0) {
            o("idle");
            return;
          }
          o("uploading");
          const v = new FormData();
          v.append("audio", y, "recording.webm"), await n(v), o("idle");
        }, I.start(), o("recording");
      } catch (w) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", w), s("js.audioRecorder.error.denied"), o("idle");
      }
    }
  }, [a, n]), b = ue(Ml), D = f === "recording" ? b["js.audioRecorder.stop"] : f === "uploading" ? b["js.uploading"] : b["js.audioRecorder.record"], _ = f === "uploading", E = ["tlAudioRecorder__button"];
  return f === "recording" && E.push("tlAudioRecorder__button--recording"), f === "uploading" && E.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: E.join(" "),
      onClick: h,
      disabled: _,
      title: D,
      "aria-label": D
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${f === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, b[i]), m && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, m));
}, jl = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Bl = ({ controlId: l }) => {
  const t = X(), n = Ge(), a = !!t.hasAudio, o = t.dataRevision ?? 0, [i, s] = e.useState(a ? "idle" : "disabled"), u = e.useRef(null), r = e.useRef(null), c = e.useRef(o);
  e.useEffect(() => {
    a ? i === "disabled" && s("idle") : (u.current && (u.current.pause(), u.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null), s("disabled"));
  }, [a]), e.useEffect(() => {
    o !== c.current && (c.current = o, u.current && (u.current.pause(), u.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null), (i === "playing" || i === "paused" || i === "loading") && s("idle"));
  }, [o]), e.useEffect(() => () => {
    u.current && (u.current.pause(), u.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null);
  }, []);
  const d = e.useCallback(async () => {
    if (i === "disabled" || i === "loading")
      return;
    if (i === "playing") {
      u.current && u.current.pause(), s("paused");
      return;
    }
    if (i === "paused" && u.current) {
      u.current.play(), s("playing");
      return;
    }
    if (!r.current) {
      s("loading");
      try {
        const _ = await fetch(n);
        if (!_.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", _.status), s("idle");
          return;
        }
        const E = await _.blob();
        r.current = URL.createObjectURL(E);
      } catch (_) {
        console.error("[TLAudioPlayer] Fetch error:", _), s("idle");
        return;
      }
    }
    const D = new Audio(r.current);
    u.current = D, D.onended = () => {
      s("idle");
    }, D.play(), s("playing");
  }, [i, n]), m = ue(jl), f = i === "loading" ? m["js.loading"] : i === "playing" ? m["js.audioPlayer.pause"] : i === "disabled" ? m["js.audioPlayer.noAudio"] : m["js.audioPlayer.play"], h = i === "disabled" || i === "loading", b = ["tlAudioPlayer__button"];
  return i === "playing" && b.push("tlAudioPlayer__button--playing"), i === "loading" && b.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: b.join(" "),
      onClick: d,
      disabled: h,
      title: f,
      "aria-label": f
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${i === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Pl = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Al = ({ controlId: l }) => {
  const t = X(), n = Ye(), [a, o] = e.useState("idle"), [i, s] = e.useState(!1), u = e.useRef(null), r = t.status ?? "idle", c = t.error, d = t.accept ?? "", m = r === "received" ? "idle" : a !== "idle" ? a : r, f = e.useCallback(async (y) => {
    o("uploading");
    const v = new FormData();
    v.append("file", y, y.name), await n(v), o("idle");
  }, [n]), h = e.useCallback((y) => {
    var C;
    const v = (C = y.target.files) == null ? void 0 : C[0];
    v && f(v);
  }, [f]), b = e.useCallback(() => {
    var y;
    a !== "uploading" && ((y = u.current) == null || y.click());
  }, [a]), D = e.useCallback((y) => {
    y.preventDefault(), y.stopPropagation(), s(!0);
  }, []), _ = e.useCallback((y) => {
    y.preventDefault(), y.stopPropagation(), s(!1);
  }, []), E = e.useCallback((y) => {
    var C;
    if (y.preventDefault(), y.stopPropagation(), s(!1), a === "uploading") return;
    const v = (C = y.dataTransfer.files) == null ? void 0 : C[0];
    v && f(v);
  }, [a, f]), w = m === "uploading", k = ue(Pl), I = m === "uploading" ? k["js.uploading"] : k["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${i ? " tlFileUpload--dragover" : ""}`,
      onDragOver: D,
      onDragLeave: _,
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
        disabled: w,
        title: I,
        "aria-label": I
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    c && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, c)
  );
}, Fl = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, Ol = ({ controlId: l, state: t }) => {
  const a = X() ?? t ?? {}, o = Ye(), i = Ge(), s = ue(Fl), u = a.editable !== !1, r = !!a.hasData, c = a.fileName ?? "download", d = a.dataRevision ?? 0, m = a.accept ?? "", f = a.status ?? "idle", h = a.error ?? null, [b, D] = e.useState("idle"), [_, E] = e.useState(!1), [w, k] = e.useState(!1), I = e.useRef(null), y = e.useCallback(async () => {
    if (!(!r || w)) {
      k(!0);
      try {
        const P = i + (i.includes("?") ? "&" : "?") + "rev=" + d, L = await fetch(P);
        if (!L.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", L.status);
          return;
        }
        const R = await L.blob(), W = URL.createObjectURL(R), p = document.createElement("a");
        p.href = W, p.download = c, p.style.display = "none", document.body.appendChild(p), p.click(), document.body.removeChild(p), URL.revokeObjectURL(W);
      } catch (P) {
        console.error("[TLBinaryField] Fetch error:", P);
      } finally {
        k(!1);
      }
    }
  }, [r, w, i, d, c]), v = e.useCallback(async (P) => {
    D("uploading");
    const L = new FormData();
    L.append("file", P, P.name), await o(L), D("idle");
  }, [o]), C = (f === "received" ? "idle" : b !== "idle" ? b : f) === "uploading", N = e.useCallback((P) => {
    var R;
    const L = (R = P.target.files) == null ? void 0 : R[0];
    L && v(L);
  }, [v]), x = e.useCallback(() => {
    var P;
    C || (P = I.current) == null || P.click();
  }, [C]), S = e.useCallback((P) => {
    P.preventDefault(), P.stopPropagation(), E(!0);
  }, []), F = e.useCallback((P) => {
    P.preventDefault(), P.stopPropagation(), E(!1);
  }, []), H = e.useCallback((P) => {
    var R;
    if (P.preventDefault(), P.stopPropagation(), E(!1), C) return;
    const L = (R = P.dataTransfer.files) == null ? void 0 : R[0];
    L && v(L);
  }, [C, v]), B = w ? s["js.downloading"] : s["js.download.file"].replace("{0}", c), K = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (w ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: y,
      disabled: w,
      title: B,
      "aria-label": B
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: c }, c));
  if (!u)
    return r ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, K) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, s["js.download.noFile"]));
  const A = C, j = C ? s["js.uploading"] : s["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlBinaryField tlFileUpload${_ ? " tlFileUpload--dragover" : ""}`,
      onDragOver: S,
      onDragLeave: F,
      onDrop: H
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: I,
        type: "file",
        accept: m || void 0,
        onChange: N,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (A ? " tlFileUpload__button--uploading" : ""),
        onClick: x,
        disabled: A,
        title: j,
        "aria-label": j
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    r && K,
    h && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, h)
  );
}, $l = {
  "js.fileChips.add": "Add file",
  "js.fileChips.remove": "Remove {0}",
  "js.uploading": "Uploading…",
  "js.download.file": "Download {0}"
};
function Hl(l) {
  return l < 1024 ? l + " B" : l < 1024 * 1024 ? (l / 1024).toFixed(l < 10 * 1024 ? 1 : 0) + " KB" : (l / (1024 * 1024)).toFixed(1) + " MB";
}
const Wl = ({ controlId: l }) => {
  const t = X(), n = le(), a = Ye(), o = Ge(), i = ue($l), s = t.chips ?? [], u = t.editable === !0, [r, c] = e.useState(!1), [d, m] = e.useState(!1), f = e.useRef(null), h = e.useCallback(async (y) => {
    const v = Array.from(y);
    if (v.length !== 0) {
      c(!0);
      try {
        const C = new FormData();
        for (const N of v)
          C.append("file", N, N.name);
        await a(C);
      } finally {
        c(!1);
      }
    }
  }, [a]), b = e.useCallback(async (y) => {
    if (y.hasData)
      try {
        const v = o + "&key=" + encodeURIComponent(y.key), C = await fetch(v);
        if (!C.ok) {
          console.error("[TLFileChips] Failed to fetch data:", C.status);
          return;
        }
        const N = await C.blob(), x = URL.createObjectURL(N), S = document.createElement("a");
        S.href = x, S.download = y.name, S.style.display = "none", document.body.appendChild(S), S.click(), document.body.removeChild(S), URL.revokeObjectURL(x);
      } catch (v) {
        console.error("[TLFileChips] Fetch error:", v);
      }
  }, [o]), D = e.useCallback((y) => {
    y.target.files && h(y.target.files), y.target.value = "";
  }, [h]), _ = e.useCallback(() => {
    var y;
    r || (y = f.current) == null || y.click();
  }, [r]), E = e.useCallback((y) => {
    u && (y.preventDefault(), y.stopPropagation(), m(!0));
  }, [u]), w = e.useCallback((y) => {
    u && (y.preventDefault(), y.stopPropagation(), m(!1));
  }, [u]), k = e.useCallback((y) => {
    u && (y.preventDefault(), y.stopPropagation(), m(!1), !r && y.dataTransfer.files && h(y.dataTransfer.files));
  }, [u, r, h]), I = [
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
      onDragLeave: w,
      onDrop: k
    },
    s.map((y) => {
      const v = i["js.download.file"].replace("{0}", y.name), C = i["js.fileChips.remove"].replace("{0}", y.name);
      return /* @__PURE__ */ e.createElement("span", { key: y.key, className: "tlFileChip" }, /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__main",
          onClick: () => b(y),
          disabled: !y.hasData,
          title: y.hasData ? v : y.name
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
        /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__name" }, y.name),
        y.size != null && /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__size" }, Hl(y.size))
      ), u && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__remove",
          onClick: () => n("removeChip", { key: y.key }),
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
        ref: f,
        type: "file",
        multiple: !0,
        onChange: D,
        style: { display: "none" }
      }
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileChips__add" + (r ? " tlFileChips__add--uploading" : ""),
        onClick: _,
        disabled: r,
        title: r ? i["js.uploading"] : i["js.fileChips.add"]
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
      /* @__PURE__ */ e.createElement("span", null, r ? i["js.uploading"] : i["js.fileChips.add"])
    ))
  );
}, Ul = 3e4;
function Vl(l, t) {
  const n = Math.round((l - Date.now()) / 1e3), a = Math.abs(n), o = new Intl.RelativeTimeFormat(t, { numeric: "auto" });
  return a < 60 ? o.format(Math.trunc(n / 1), "second") : a < 3600 ? o.format(Math.trunc(n / 60), "minute") : a < 86400 ? o.format(Math.trunc(n / 3600), "hour") : a < 7 * 86400 ? o.format(Math.trunc(n / 86400), "day") : new Date(l).toLocaleDateString(t);
}
const zl = ({ controlId: l }) => {
  const t = X(), n = t.timestamp, a = t.label ?? void 0, o = t.locale || navigator.language, [, i] = e.useState(0);
  return e.useEffect(() => {
    const s = setInterval(() => i((u) => u + 1), Ul);
    return () => clearInterval(s);
  }, []), n == null ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime tlRelativeTime--empty" }) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime", title: a }, Vl(n, o));
}, Kl = ({ controlId: l }) => {
  const t = X(), n = t.anchor ?? void 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAnchor", "data-tl-anchor": n }, t.child && /* @__PURE__ */ e.createElement(G, { control: t.child }));
}, Yl = ({ controlId: l }) => {
  const t = X(), n = t.target, a = t.label ?? "";
  if (n == null)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlScrollLink tlScrollLink--empty" });
  const o = (i) => {
    i.preventDefault(), al(n);
  };
  return /* @__PURE__ */ e.createElement("a", { id: l, className: "tlScrollLink", href: "#", onClick: o }, a);
};
function Gl(l) {
  const t = l.trim().split(/\s+/).filter(Boolean);
  return t.length === 0 ? "?" : t.length === 1 ? t[0].slice(0, 2).toUpperCase() : (t[0][0] + t[t.length - 1][0]).toUpperCase();
}
function Xl(l) {
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return Math.abs(t) % 360;
}
const ql = ({ controlId: l }) => {
  const n = X().name;
  return n ? /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlAvatar",
      style: { backgroundColor: `hsl(${Xl(n)}, 45%, 45%)` },
      title: n,
      "aria-label": n
    },
    Gl(n)
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlAvatar tlAvatar--empty" });
}, Zl = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Ql = ({ controlId: l }) => {
  const t = X(), n = Ge(), a = le(), o = !!t.hasData, i = t.dataRevision ?? 0, s = t.fileName ?? "download", u = !!t.clearable, [r, c] = e.useState(!1), d = e.useCallback(async () => {
    if (!(!o || r)) {
      c(!0);
      try {
        const b = n + (n.includes("?") ? "&" : "?") + "rev=" + i, D = await fetch(b);
        if (!D.ok) {
          console.error("[TLDownload] Failed to fetch data:", D.status);
          return;
        }
        const _ = await D.blob(), E = URL.createObjectURL(_), w = document.createElement("a");
        w.href = E, w.download = s, w.style.display = "none", document.body.appendChild(w), w.click(), document.body.removeChild(w), URL.revokeObjectURL(E);
      } catch (b) {
        console.error("[TLDownload] Fetch error:", b);
      } finally {
        c(!1);
      }
    }
  }, [o, r, n, i, s]), m = e.useCallback(async () => {
    o && await a("clear");
  }, [o, a]), f = ue(Zl);
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
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: s }, s), u && /* @__PURE__ */ e.createElement(
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
}, Jl = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, ea = ({ controlId: l }) => {
  const t = X(), n = Ye(), [a, o] = e.useState("idle"), [i, s] = e.useState(null), [u, r] = e.useState(!1), c = e.useRef(null), d = e.useRef(null), m = e.useRef(null), f = e.useRef(null), h = e.useRef(null), b = t.error, D = e.useMemo(
    () => {
      var S;
      return !!(window.isSecureContext && ((S = navigator.mediaDevices) != null && S.getUserMedia));
    },
    []
  ), _ = e.useCallback(() => {
    d.current && (d.current.getTracks().forEach((S) => S.stop()), d.current = null), c.current && (c.current.srcObject = null);
  }, []), E = e.useCallback(() => {
    _(), o("idle");
  }, [_]), w = e.useCallback(async () => {
    var S;
    if (a !== "uploading") {
      if (s(null), !D) {
        (S = f.current) == null || S.click();
        return;
      }
      try {
        const F = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        d.current = F, o("overlayOpen");
      } catch (F) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", F), s("js.photoCapture.error.denied"), o("idle");
      }
    }
  }, [a, D]), k = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const S = c.current, F = m.current;
    if (!S || !F)
      return;
    F.width = S.videoWidth, F.height = S.videoHeight;
    const H = F.getContext("2d");
    H && (H.drawImage(S, 0, 0), _(), o("uploading"), F.toBlob(async (B) => {
      if (!B) {
        o("idle");
        return;
      }
      const K = new FormData();
      K.append("photo", B, "capture.jpg"), await n(K), o("idle");
    }, "image/jpeg", 0.85));
  }, [a, n, _]), I = e.useCallback(async (S) => {
    var B;
    const F = (B = S.target.files) == null ? void 0 : B[0];
    if (!F) return;
    o("uploading");
    const H = new FormData();
    H.append("photo", F, F.name), await n(H), o("idle"), f.current && (f.current.value = "");
  }, [n]);
  e.useEffect(() => {
    a === "overlayOpen" && c.current && d.current && (c.current.srcObject = d.current);
  }, [a]), e.useEffect(() => {
    var F;
    if (a !== "overlayOpen") return;
    (F = h.current) == null || F.focus();
    const S = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = S;
    };
  }, [a]), Oe(a === "overlayOpen", { ESCAPE: E }), e.useEffect(() => () => {
    d.current && (d.current.getTracks().forEach((S) => S.stop()), d.current = null);
  }, []);
  const y = ue(Jl), v = a === "uploading" ? y["js.uploading"] : y["js.photoCapture.open"], C = ["tlPhotoCapture__cameraBtn"];
  a === "uploading" && C.push("tlPhotoCapture__cameraBtn--uploading");
  const N = ["tlPhotoCapture__overlayVideo"];
  u && N.push("tlPhotoCapture__overlayVideo--mirrored");
  const x = ["tlPhotoCapture__mirrorBtn"];
  return u && x.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: C.join(" "),
      onClick: w,
      disabled: a === "uploading",
      title: v,
      "aria-label": v
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !D && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: f,
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
        className: N.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: x.join(" "),
        onClick: () => r((S) => !S),
        title: y["js.photoCapture.mirror"],
        "aria-label": y["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: k,
        title: y["js.photoCapture.capture"],
        "aria-label": y["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: E,
        title: y["js.photoCapture.close"],
        "aria-label": y["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, y[i]), b && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, b));
}, ta = {
  "js.photoViewer.alt": "Captured photo"
}, na = ({ controlId: l }) => {
  const t = X(), n = Ge(), a = !!t.hasPhoto, o = t.dataRevision ?? 0, [i, s] = e.useState(null), u = e.useRef(o);
  e.useEffect(() => {
    if (!a) {
      i && (URL.revokeObjectURL(i), s(null));
      return;
    }
    if (o === u.current && i)
      return;
    u.current = o, i && (URL.revokeObjectURL(i), s(null));
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
    i && URL.revokeObjectURL(i);
  }, []);
  const r = ue(ta);
  return !a || !i ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: i,
      alt: t.alt || r["js.photoViewer.alt"]
    }
  ));
}, la = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, aa = ({ controlId: l }) => {
  const t = X(), n = Ge(), a = !!t.hasPdf, o = t.dataRevision ?? 0, i = ue(la), u = n.indexOf("react-api/"), r = u >= 0 ? n.slice(0, u) : n, c = n + "&rev=" + o, d = r + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(c);
  return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: d,
      title: i["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, i["js.pdfViewer.noDocument"]));
}, { useCallback: ln, useRef: Dt } = e, ra = ({ controlId: l }) => {
  const t = X(), n = le(), a = yt(!0), o = t.orientation, i = t.resizable === !0, s = t.children ?? [], u = o === "horizontal", r = s.length > 0 && s.every((E) => E.collapsed), c = !r && s.some((E) => E.collapsed), d = r ? !u : u, m = Dt(null), f = Dt(null), h = Dt(null), b = ln((E, w) => {
    const k = {
      overflow: E.scrolling || "auto"
    };
    return E.collapsed ? r && !d ? k.flex = "1 0 0%" : k.flex = "0 0 auto" : w !== void 0 ? k.flex = `0 0 ${w}px` : k.flex = `${E.size} 1 0%`, E.minSize > 0 && !E.collapsed && (k.minWidth = u ? E.minSize : void 0, k.minHeight = u ? void 0 : E.minSize), k;
  }, [u, r, c, d]), D = ln((E, w) => {
    E.preventDefault();
    const k = m.current;
    if (!k) return;
    const I = s[w], y = s[w + 1], v = k.querySelectorAll(":scope > .tlSplitPanel__child"), C = [];
    v.forEach((S) => {
      C.push(u ? S.offsetWidth : S.offsetHeight);
    }), h.current = C, f.current = {
      splitterIndex: w,
      startPos: u ? E.clientX : E.clientY,
      startSizeBefore: C[w],
      startSizeAfter: C[w + 1],
      childBefore: I,
      childAfter: y
    };
    const N = (S) => {
      const F = f.current;
      if (!F || !h.current) return;
      const B = (u ? S.clientX : S.clientY) - F.startPos, K = F.childBefore.minSize || 0, A = F.childAfter.minSize || 0;
      let j = F.startSizeBefore + B, P = F.startSizeAfter - B;
      j < K && (P += j - K, j = K), P < A && (j += P - A, P = A), h.current[F.splitterIndex] = j, h.current[F.splitterIndex + 1] = P;
      const L = k.querySelectorAll(":scope > .tlSplitPanel__child"), R = L[F.splitterIndex], W = L[F.splitterIndex + 1];
      R && (R.style.flex = `0 0 ${j}px`), W && (W.style.flex = `0 0 ${P}px`);
    }, x = () => {
      if (document.removeEventListener("mousemove", N), document.removeEventListener("mouseup", x), document.body.style.cursor = "", document.body.style.userSelect = "", h.current) {
        const S = {};
        s.forEach((F, H) => {
          const B = F.control;
          B != null && B.controlId && h.current && (S[B.controlId] = h.current[H]);
        }), n("updateSizes", { sizes: S });
      }
      h.current = null, f.current = null;
    };
    document.addEventListener("mousemove", N), document.addEventListener("mouseup", x), document.body.style.cursor = u ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [s, u, n]), _ = [];
  return s.forEach((E, w) => {
    if (_.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${w}`,
          className: `tlSplitPanel__child${E.collapsed && d ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: b(E)
        },
        /* @__PURE__ */ e.createElement(G, { control: E.control })
      )
    ), i && w < s.length - 1) {
      const k = s[w + 1];
      !E.collapsed && !k.collapsed && _.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${w}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${o}`,
            onMouseDown: (y) => D(y, w)
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
    _
  );
}, Ft = ({ image: l, className: t }) => {
  if (!l || l === "none") return null;
  const n = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `${t ? t + " " : ""}${n}`, "aria-hidden": "true" });
}, { useCallback: Tt } = e, oa = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, sa = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), ca = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), ia = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), ua = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), da = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), ma = ({ controlId: l }) => {
  const t = X(), n = le(), a = ue(oa), o = t.title, i = t.expansionState ?? "NORMALIZED", s = t.showMinimize === !0, u = t.showMaximize === !0, r = t.showPopOut === !0, c = t.fullLine === !0, d = t.fill === !0, m = t.hoverActions === !0, f = t.appearance === "card", h = t.errorMessage, b = i === "MINIMIZED", D = i === "MAXIMIZED", _ = i === "HIDDEN", E = Tt(() => {
    n("toggleMinimize");
  }, [n]), w = Tt(() => {
    n("toggleMaximize");
  }, [n]), k = Tt(() => {
    n("popOut");
  }, [n]), I = yt(d && !_);
  if (_)
    return null;
  const y = D ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" }, v = s && !D || u && !b || r, C = !!o && o.trim() !== "" || !!t.titleContent || !!t.toolbar || v;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${i.toLowerCase()}${c ? " tlPanel--fullLine" : ""}${I ? " " + I : ""}${m ? " tlPanel--hoverActions" : ""}${f ? " tlPanel--card" : ""}`,
      style: y
    },
    C && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, !!o && o.trim() !== "" && /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, o), t.titleContent && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__titleContent" }, /* @__PURE__ */ e.createElement(G, { control: t.titleContent })), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(G, { control: t.toolbar }), s && !D && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: E,
        title: b ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      b ? /* @__PURE__ */ e.createElement(ca, null) : /* @__PURE__ */ e.createElement(sa, null)
    ), u && !b && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: w,
        title: D ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      D ? /* @__PURE__ */ e.createElement(ua, null) : /* @__PURE__ */ e.createElement(ia, null)
    ), r && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: k,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(da, null)
    ))),
    !b && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(Be, null, /* @__PURE__ */ e.createElement(G, { control: t.child }))),
    !b && h && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error tlPanel__error", role: "alert" }, /* @__PURE__ */ e.createElement(Ft, { image: t.errorIcon, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, h)),
    !b && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(G, { control: t.buttonBar }))
  );
}, pa = ({ controlId: l }) => {
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
}, fa = ({ controlId: l }) => {
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
}, { useCallback: ve, useState: bt, useEffect: Ot, useRef: Et } = e, ha = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function $t(l, t, n, a) {
  const o = [];
  for (const i of l)
    if (i.type === "nav") {
      if (i.hidden) continue;
      o.push({ id: i.id, type: "nav", groupId: a });
    } else i.type === "command" ? o.push({ id: i.id, type: "command", groupId: a }) : i.type === "group" && (o.push({ id: i.id, type: "group" }), (n.get(i.id) ?? i.expanded) && !t && o.push(...$t(i.children, t, n, i.id)));
  return o;
}
const Ke = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(Ne, { encoded: l, className: "tlSidebar__icon" }) : null, ba = ({ item: l, active: t, collapsed: n, onSelect: a, tabIndex: o, itemRef: i, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => a(l.id),
    title: n ? l.label : void 0,
    tabIndex: o,
    ref: i,
    onFocus: () => s(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Ke, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Ke, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), ga = ({ item: l, collapsed: t, onExecute: n, tabIndex: a, itemRef: o, onFocus: i }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: a,
    ref: o,
    onFocus: () => i(l.id)
  },
  /* @__PURE__ */ e.createElement(Ke, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), Ea = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Ke, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), va = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), _a = ({ item: l, activeItemId: t, anchorRect: n, onSelect: a, onExecute: o, onClose: i }) => {
  const s = Et(null);
  Ot(() => {
    const c = (d) => {
      s.current && !s.current.contains(d.target) && setTimeout(() => i(), 0);
    };
    return document.addEventListener("mousedown", c), () => document.removeEventListener("mousedown", c);
  }, [i]), Oe(!0, { ESCAPE: i });
  const u = ve((c) => {
    c.type === "nav" ? (a(c.id), i()) : c.type === "command" && (o(c.id), i());
  }, [a, o, i]), r = {};
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
          onClick: () => u(c)
        },
        /* @__PURE__ */ e.createElement(Ke, { icon: c.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, c.label),
        c.type === "nav" && c.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, c.badge)
      );
    }
    return c.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: c.id, className: "tlSidebar__flyoutSectionHeader" }, c.label) : c.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: c.id, className: "tlSidebar__separator" }) : null;
  }));
}, Ca = ({
  item: l,
  expanded: t,
  activeItemId: n,
  collapsed: a,
  onSelect: o,
  onExecute: i,
  onToggleGroup: s,
  tabIndex: u,
  itemRef: r,
  onFocus: c,
  focusedId: d,
  setItemRef: m,
  onItemFocus: f,
  flyoutGroupId: h,
  onOpenFlyout: b,
  onCloseFlyout: D
}) => {
  const _ = Et(null), [E, w] = bt(null), k = ve(() => {
    a ? h === l.id ? D() : (_.current && w(_.current.getBoundingClientRect()), b(l.id)) : s(l.id);
  }, [a, h, l.id, s, b, D]), I = ve((v) => {
    _.current = v, r(v);
  }, [r]), y = a && h === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (y ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: k,
      title: a ? l.label : void 0,
      "aria-expanded": a ? y : t,
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
  ), y && /* @__PURE__ */ e.createElement(
    _a,
    {
      item: l,
      activeItemId: n,
      anchorRect: E,
      onSelect: o,
      onExecute: i,
      onClose: D
    }
  ), t && !a && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((v) => /* @__PURE__ */ e.createElement(
    kn,
    {
      key: v.id,
      item: v,
      activeItemId: n,
      collapsed: a,
      onSelect: o,
      onExecute: i,
      onToggleGroup: s,
      focusedId: d,
      setItemRef: m,
      onItemFocus: f,
      groupStates: null,
      flyoutGroupId: h,
      onOpenFlyout: b,
      onCloseFlyout: D
    }
  ))));
}, kn = ({
  item: l,
  activeItemId: t,
  collapsed: n,
  onSelect: a,
  onExecute: o,
  onToggleGroup: i,
  focusedId: s,
  setItemRef: u,
  onItemFocus: r,
  groupStates: c,
  flyoutGroupId: d,
  onOpenFlyout: m,
  onCloseFlyout: f
}) => {
  switch (l.type) {
    case "nav":
      return l.hidden ? null : /* @__PURE__ */ e.createElement(
        ba,
        {
          item: l,
          active: l.id === t,
          collapsed: n,
          onSelect: a,
          tabIndex: s === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: r
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        ga,
        {
          item: l,
          collapsed: n,
          onExecute: o,
          tabIndex: s === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: r
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(Ea, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(va, null);
    case "group": {
      const h = c ? c.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        Ca,
        {
          item: l,
          expanded: h,
          activeItemId: t,
          collapsed: n,
          onSelect: a,
          onExecute: o,
          onToggleGroup: i,
          tabIndex: s === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: r,
          focusedId: s,
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
}, ya = ({ controlId: l }) => {
  const t = X(), n = le(), a = ue(ha), o = t.items ?? [], i = t.activeItemId, s = t.collapsed, u = t.drawerOpen, r = u ? !1 : s, [c, d] = bt(() => {
    const B = /* @__PURE__ */ new Map(), K = (A) => {
      for (const j of A)
        j.type === "group" && (B.set(j.id, j.expanded), K(j.children));
    };
    return K(o), B;
  }), m = ve((B) => {
    d((K) => {
      const A = new Map(K), j = A.get(B) ?? !1;
      return A.set(B, !j), n("toggleGroup", { itemId: B, expanded: !j }), A;
    });
  }, [n]), f = ve((B) => {
    B !== i && n("selectItem", { itemId: B });
  }, [n, i]), h = ve((B) => {
    n("executeCommand", { itemId: B });
  }, [n]), b = ve(() => {
    n("toggleCollapse", {});
  }, [n]), D = ve(() => {
    n("toggleDrawer", {});
  }, [n]), [_, E] = bt(null), w = ve((B) => {
    E(B);
  }, []), k = ve(() => {
    E(null);
  }, []);
  Ot(() => {
    r || E(null);
  }, [r]);
  const [I, y] = bt(() => {
    const B = $t(o, r, c);
    return B.length > 0 ? B[0].id : "";
  }), v = Et(/* @__PURE__ */ new Map()), C = ve((B) => (K) => {
    K ? v.current.set(B, K) : v.current.delete(B);
  }, []), N = ve((B) => {
    y(B);
  }, []), x = Et(0), S = ve((B) => {
    y(B), x.current++;
  }, []);
  Ot(() => {
    const B = v.current.get(I);
    B && document.activeElement !== B && B.focus();
  }, [I, x.current]);
  const F = ve((B) => {
    if (B.key === "Escape" && _ !== null) {
      B.preventDefault(), k();
      return;
    }
    const K = $t(o, r, c);
    if (K.length === 0) return;
    const A = K.findIndex((P) => P.id === I);
    if (A < 0) return;
    const j = K[A];
    switch (B.key) {
      case "ArrowDown": {
        B.preventDefault();
        const P = (A + 1) % K.length;
        S(K[P].id);
        break;
      }
      case "ArrowUp": {
        B.preventDefault();
        const P = (A - 1 + K.length) % K.length;
        S(K[P].id);
        break;
      }
      case "Home": {
        B.preventDefault(), S(K[0].id);
        break;
      }
      case "End": {
        B.preventDefault(), S(K[K.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        B.preventDefault(), j.type === "nav" ? f(j.id) : j.type === "command" ? h(j.id) : j.type === "group" && (r ? _ === j.id ? k() : w(j.id) : m(j.id));
        break;
      }
      case "ArrowRight": {
        j.type === "group" && !r && ((c.get(j.id) ?? !1) || (B.preventDefault(), m(j.id)));
        break;
      }
      case "ArrowLeft": {
        j.type === "group" && !r && (c.get(j.id) ?? !1) && (B.preventDefault(), m(j.id));
        break;
      }
    }
  }, [
    o,
    r,
    c,
    I,
    _,
    S,
    f,
    h,
    m,
    w,
    k
  ]), H = "tlSidebar" + (r ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: H }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(G, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: D, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": a["js.sidebar.ariaLabel"] }, r ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(G, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(G, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: F }, o.map((B) => /* @__PURE__ */ e.createElement(
    kn,
    {
      key: B.id,
      item: B,
      activeItemId: i,
      collapsed: r,
      onSelect: f,
      onExecute: h,
      onToggleGroup: m,
      focusedId: I,
      setItemRef: C,
      onItemFocus: N,
      groupStates: c,
      flyoutGroupId: _,
      onOpenFlyout: w,
      onCloseFlyout: k
    }
  ))), r ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(G, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(G, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, /* @__PURE__ */ e.createElement(Be, null, t.activeContent && /* @__PURE__ */ e.createElement(G, { control: t.activeContent }))));
}, wa = ({ controlId: l }) => {
  const t = X(), n = t.direction ?? "column", a = t.gap ?? "default", o = t.align ?? "stretch", i = t.wrap === !0, s = t.growFirst === !0, u = t.children ?? [], [r, c] = at(), d = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${a}`,
    `tlStack--align-${o}`,
    i ? "tlStack--wrap" : "",
    s ? "tlStack--grow-first" : "",
    r,
    t.cssClass ?? ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(rt, { host: c }, /* @__PURE__ */ e.createElement("div", { id: l, className: d }, u.map((m, f) => /* @__PURE__ */ e.createElement(G, { key: f, control: m }))));
}, ka = ({ controlId: l }) => {
  const t = X(), [n, a] = at();
  return /* @__PURE__ */ e.createElement(rt, { host: a }, /* @__PURE__ */ e.createElement("div", { id: l, className: n ? "tlInset " + n : "tlInset" }, t.child && /* @__PURE__ */ e.createElement(G, { control: t.child })));
}, Na = ({ controlId: l }) => {
  const t = X(), n = t.columns, a = t.minColumnWidth, o = t.gap ?? "default", i = t.children ?? [], s = {};
  return a ? s.gridTemplateColumns = `repeat(auto-fit, minmax(min(${a}, 100%), 1fr))` : n && (s.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${o}`, style: s }, i.map((u, r) => /* @__PURE__ */ e.createElement(G, { key: r, control: u })));
}, Sa = ({ controlId: l }) => {
  const t = X(), n = t.title, a = t.variant ?? "outlined", o = t.padding ?? "default", i = t.headerActions ?? [], s = t.child, u = n != null || i.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${a}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, i.map((r, c) => /* @__PURE__ */ e.createElement(G, { key: c, control: r })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${o}` }, /* @__PURE__ */ e.createElement(Be, null, /* @__PURE__ */ e.createElement(G, { control: s }))));
}, Da = ({ controlId: l }) => {
  const t = X(), n = t.title ?? "", a = t.leading, o = t.trailing, i = t.children ?? [], s = t.actions ?? [], u = t.variant ?? "flat", c = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    u === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: c }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(G, { control: a })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, i.map((d, m) => /* @__PURE__ */ e.createElement(G, { key: m, control: d }))), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((d, m) => /* @__PURE__ */ e.createElement(G, { key: m, control: d }))), o && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__trailing" }, /* @__PURE__ */ e.createElement(G, { control: o })));
}, { useCallback: Ta } = e, Ra = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.items ?? [], o = Ta((i) => {
    n("navigate", { itemId: i });
  }, [n]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, a.map((i, s) => {
    const u = s === a.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: i.id, className: "tlBreadcrumb__entry" }, s > 0 && /* @__PURE__ */ e.createElement(
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
        onClick: () => o(i.id)
      },
      i.label
    ));
  })));
}, { useCallback: La } = e, xa = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.items ?? [], o = t.activeItemId, i = La((s) => {
    s !== o && n("selectItem", { itemId: s });
  }, [n, o]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, a.map((s) => {
    const u = s.id === o;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: s.id,
        type: "button",
        className: "tlBottomBar__item" + (u ? " tlBottomBar__item--active" : ""),
        onClick: () => i(s.id),
        "aria-current": u ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + s.icon, "aria-hidden": "true" }), s.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, s.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, s.label)
    );
  }));
}, { useCallback: an, useRef: Ma } = e, Ia = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), ja = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.open === !0, o = t.closeOnBackdrop !== !1, i = t.child, s = Ma(null), u = an(() => {
    n("close");
  }, [n]), r = an((c) => {
    o && c.target === c.currentTarget && u();
  }, [o, u]);
  return a ? /* @__PURE__ */ e.createElement(Kt, null, /* @__PURE__ */ e.createElement(Ia, { onClose: u }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: r,
      ref: s,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(Be, null, /* @__PURE__ */ e.createElement(G, { control: i }))
  )) : null;
}, { useEffect: Ba, useRef: Pa } = e, Aa = ({ controlId: l }) => {
  const n = X().dialogs ?? [], a = Pa(n.length);
  return Ba(() => {
    n.length < a.current && n.length > 0, a.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((o) => /* @__PURE__ */ e.createElement(G, { key: o.controlId, control: o })));
}, { useCallback: it, useRef: He, useState: ut } = e, Fa = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), Oa = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, $a = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Ha = ({ controlId: l }) => {
  const t = X(), n = le(), a = ue(Oa), o = t.title ?? "", i = t.width ?? "32rem", s = t.height ?? null, u = t.minHeight ?? null, r = t.resizable === !0, c = t.child, d = t.actions ?? [], m = t.toolbar, f = t.buttonBar, [h, b] = ut(null), [D, _] = ut(null), [E, w] = ut(null), k = He(null), [I, y] = ut(!1), v = He(null), C = He(null), N = He(null), x = He(null), S = He(null), F = it(() => {
    n("close");
  }, [n]);
  Yt(!0, x, "field");
  const H = it((P, L) => {
    L.preventDefault();
    const R = x.current;
    if (!R) return;
    const W = R.getBoundingClientRect(), p = !k.current, M = k.current ?? { x: W.left, y: W.top };
    p && (k.current = M, w(M)), S.current = {
      dir: P,
      startX: L.clientX,
      startY: L.clientY,
      startW: W.width,
      startH: W.height,
      startPos: { ...M },
      symmetric: p
    };
    const Y = (Q) => {
      const $ = S.current;
      if (!$) return;
      const J = Q.clientX - $.startX, se = Q.clientY - $.startY;
      let ne = $.startW, Ee = $.startH, be = 0, we = 0;
      $.symmetric ? ($.dir.includes("e") && (ne = $.startW + 2 * J), $.dir.includes("w") && (ne = $.startW - 2 * J), $.dir.includes("s") && (Ee = $.startH + 2 * se), $.dir.includes("n") && (Ee = $.startH - 2 * se)) : ($.dir.includes("e") && (ne = $.startW + J), $.dir.includes("w") && (ne = $.startW - J, be = J), $.dir.includes("s") && (Ee = $.startH + se), $.dir.includes("n") && (Ee = $.startH - se, we = se));
      const Te = Math.max(200, ne), Re = Math.max(100, Ee);
      $.symmetric ? (be = ($.startW - Te) / 2, we = ($.startH - Re) / 2) : ($.dir.includes("w") && Te === 200 && (be = $.startW - 200), $.dir.includes("n") && Re === 100 && (we = $.startH - 100)), C.current = Te, N.current = Re, b(Te), _(Re);
      const $e = {
        x: $.startPos.x + be,
        y: $.startPos.y + we
      };
      k.current = $e, w($e);
    }, V = () => {
      document.removeEventListener("mousemove", Y), document.removeEventListener("mouseup", V);
      const Q = C.current, $ = N.current;
      (Q != null || $ != null) && n("resize", {
        ...Q != null ? { width: Math.round(Q) } : {},
        ...$ != null ? { height: Math.round($) } : {}
      }), S.current = null;
    };
    document.addEventListener("mousemove", Y), document.addEventListener("mouseup", V);
  }, [n]), B = it((P) => {
    if (P.button !== 0 || P.target.closest("button")) return;
    P.preventDefault();
    const L = x.current;
    if (!L) return;
    const R = L.getBoundingClientRect(), W = k.current ?? { x: R.left, y: R.top }, p = P.clientX - W.x, M = P.clientY - W.y, Y = (Q) => {
      const $ = window.innerWidth, J = window.innerHeight;
      let se = Q.clientX - p, ne = Q.clientY - M;
      const Ee = L.offsetWidth, be = L.offsetHeight;
      se + Ee > $ && (se = $ - Ee), ne + be > J && (ne = J - be), se < 0 && (se = 0), ne < 0 && (ne = 0);
      const we = { x: se, y: ne };
      k.current = we, w(we);
    }, V = () => {
      document.removeEventListener("mousemove", Y), document.removeEventListener("mouseup", V);
    };
    document.addEventListener("mousemove", Y), document.addEventListener("mouseup", V);
  }, []), K = it(() => {
    var P, L;
    if (I) {
      const R = v.current;
      R && (w(R.x !== -1 ? { x: R.x, y: R.y } : null), b(R.w), _(R.h)), y(!1);
    } else {
      const R = x.current, W = R == null ? void 0 : R.getBoundingClientRect();
      v.current = {
        x: ((P = k.current) == null ? void 0 : P.x) ?? (W == null ? void 0 : W.left) ?? -1,
        y: ((L = k.current) == null ? void 0 : L.y) ?? (W == null ? void 0 : W.top) ?? -1,
        w: h ?? (W == null ? void 0 : W.width) ?? null,
        h: D ?? null
      }, y(!0), w({ x: 0, y: 0 }), b(null), _(null);
    }
  }, [I, h, D]), A = I ? { position: "absolute", top: 0, left: 0, width: "100vw", maxWidth: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: h != null ? h + "px" : i,
    ...D != null ? { height: D + "px" } : s != null ? { height: s } : {},
    ...u != null && D == null ? { minHeight: u } : {},
    maxHeight: E ? "100vh" : "80vh",
    ...E ? { position: "absolute", left: E.x + "px", top: E.y + "px" } : {}
  }, j = l + "-title";
  return /* @__PURE__ */ e.createElement(Kt, { modal: !0 }, /* @__PURE__ */ e.createElement(Fa, { onClose: F }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: A,
      ref: x,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": j
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${I ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: I ? void 0 : B,
        onDoubleClick: r ? K : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: j }, o),
      m && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(G, { control: m })),
      r && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: K,
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
          onClick: F,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(Be, null, /* @__PURE__ */ e.createElement(G, { control: c }))),
    (d.length > 0 || f) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, f && /* @__PURE__ */ e.createElement(G, { control: f }), d.map((P, L) => /* @__PURE__ */ e.createElement(G, { key: L, control: P }))),
    r && !I && $a.map((P) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: P,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${P}`,
        onMouseDown: (L) => H(P, L)
      }
    ))
  ));
}, { useCallback: Wa } = e, Ua = {
  "js.drawer.close": "Close"
}, Va = ({ controlId: l }) => {
  const t = X(), n = le(), a = ue(Ua), o = t.open === !0, i = t.position ?? "right", s = t.size ?? "medium", u = t.title ?? null, r = t.child, c = Wa(() => {
    n("close");
  }, [n]);
  Oe(o, { ESCAPE: c });
  const d = [
    "tlDrawer",
    `tlDrawer--${i}`,
    `tlDrawer--${s}`,
    o ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: l, className: d, "aria-hidden": !o }, u !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, u), /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, /* @__PURE__ */ e.createElement(Be, null, r && /* @__PURE__ */ e.createElement(G, { control: r }))));
}, { useCallback: dt, useRef: za } = e, Ka = ({ controlId: l }) => {
  const t = X(), n = le(), a = za(null), o = t.child, s = (t.trigger ?? "contextmenu") === "click", u = dt((m) => {
    m.preventDefault(), m.stopPropagation(), n("openMenu", { x: m.clientX, y: m.clientY });
  }, [n]), r = dt(() => {
    var f;
    const m = (f = a.current) == null ? void 0 : f.getBoundingClientRect();
    n("openMenu", {
      x: Math.round(m ? m.left : 0),
      y: Math.round(m ? m.bottom : 0)
    });
  }, [n]), c = dt((m) => {
    m.preventDefault(), m.stopPropagation(), r();
  }, [r]), d = dt((m) => {
    (m.key === "Enter" || m.key === " ") && (m.preventDefault(), r());
  }, [r]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenuRegion" + (s ? " tlMenuRegion--click" : ""),
      ref: a,
      onContextMenu: s ? void 0 : u,
      onClick: s ? c : void 0,
      role: s ? "button" : void 0,
      tabIndex: s ? 0 : void 0,
      "aria-haspopup": s ? "menu" : void 0,
      onKeyDown: s ? d : void 0
    },
    o && /* @__PURE__ */ e.createElement(G, { control: o })
  );
}, { useCallback: Ya, useEffect: rn, useRef: Ga, useState: on } = e, Xa = 250, qa = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.message ?? "", o = t.content ?? "", i = t.variant ?? "info", s = t.duration ?? 5e3, u = t.visible === !0, r = t.generation ?? 0, [c, d] = on(!1), [m, f] = on(!1), h = Ga(!1);
  rn(() => {
    h.current = !1;
  }, [r]);
  const b = Ya(() => {
    d(!0), setTimeout(() => {
      n("dismiss", { generation: r }), d(!1);
    }, 200);
  }, [n, r]);
  return rn(() => {
    if (!u || s === 0 || m) return;
    const D = setTimeout(b, h.current ? Xa : s);
    return () => clearTimeout(D);
  }, [u, s, m, b]), !u && !c ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${i}${c ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite",
      onMouseEnter: () => {
        h.current = !0, f(!0);
      },
      onMouseLeave: () => f(!1)
    },
    o ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: o } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, a)
  );
}, { useCallback: Za, useEffect: sn, useMemo: Qa, useRef: Ja, useState: er } = e, tr = 1e3;
function nr(l) {
  const t = Math.max(0, Math.floor(l / 1e3)), n = t % 60, a = Math.floor(t / 60) % 60, o = Math.floor(t / 3600), i = (s) => s < 10 ? `0${s}` : `${s}`;
  return o > 0 ? `${o}:${i(a)}:${i(n)}` : `${a}:${i(n)}`;
}
const lr = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.visible === !0, o = t.severity ?? "info", i = t.text ?? "", s = t.deadline ?? null, u = t.serverNow ?? null, r = t.leadMs ?? null, c = t.actionLabel ?? null, d = t.pingGraceMs ?? null, m = Qa(
    () => u != null ? u - Date.now() : 0,
    [u]
  ), [f, h] = er(0), b = a && s != null;
  sn(() => {
    if (!b) return;
    const I = setInterval(() => h((y) => y + 1), tr);
    return () => clearInterval(I);
  }, [b, s]);
  const D = Ja(null);
  sn(() => {
    !b || d == null || s == null || D.current !== s && (Date.now() + m < s + d || (D.current = s, n("deadlinePassed", {})));
  }, [f, b, s, d, m, n]);
  const _ = Za(() => {
    c != null && n("action", {});
  }, [n, c]);
  if (!a) return null;
  const E = s != null ? s - (Date.now() + m) : null;
  if (r != null && E != null && E > r) return null;
  const w = E != null ? nr(E) : null, k = c != null;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlNoticeBar tlNoticeBar--${o}${k ? " tlNoticeBar--clickable" : ""}`,
      role: k ? "button" : "status",
      "aria-live": "polite",
      tabIndex: k ? 0 : void 0,
      title: c ?? void 0,
      "aria-label": k ? `${i} ${c}` : void 0,
      onClick: k ? _ : void 0,
      onKeyDown: k ? (I) => {
        (I.key === "Enter" || I.key === " ") && (I.preventDefault(), _());
      } : void 0
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__text" }, i),
    w !== null && /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__countdown" }, w)
  );
}, { useCallback: Rt, useEffect: cn, useRef: ar, useState: un } = e, rr = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.open === !0, o = t.anchorId, i = t.anchorX, s = t.anchorY, u = t.items ?? [], r = ar(null), [c, d] = un({ top: 0, left: 0 }), [m, f] = un(0), h = u.filter((E) => E.type === "item" && !E.disabled);
  cn(() => {
    var C, N;
    if (!a) return;
    const E = ((C = r.current) == null ? void 0 : C.offsetHeight) ?? 200, w = ((N = r.current) == null ? void 0 : N.offsetWidth) ?? 200;
    if (i != null && s != null) {
      let x = s, S = i;
      x + E > window.innerHeight && (x = Math.max(0, window.innerHeight - E)), S + w > window.innerWidth && (S = Math.max(0, window.innerWidth - w)), d({ top: x, left: S }), f(0);
      return;
    }
    if (!o) return;
    const k = document.getElementById(o);
    if (!k) return;
    const I = k.getBoundingClientRect();
    let y = I.bottom + 4, v = I.left;
    y + E > window.innerHeight && (y = I.top - E - 4), v + w > window.innerWidth && (v = I.right - w), d({ top: y, left: v }), f(0);
  }, [a, o, i, s]);
  const b = Rt(() => {
    n("close");
  }, [n]), D = Rt((E) => {
    n("selectItem", { itemId: E });
  }, [n]);
  cn(() => {
    if (!a) return;
    const E = (w) => {
      r.current && !r.current.contains(w.target) && b();
    };
    return document.addEventListener("mousedown", E), () => document.removeEventListener("mousedown", E);
  }, [a, b]);
  const _ = Rt((E) => {
    if (E.key === "Escape") {
      E.preventDefault(), b();
      return;
    }
    if (E.key === "ArrowDown")
      E.preventDefault(), f((w) => (w + 1) % h.length);
    else if (E.key === "ArrowUp")
      E.preventDefault(), f((w) => (w - 1 + h.length) % h.length);
    else if (E.key === "Enter" || E.key === " ") {
      E.preventDefault();
      const w = h[m];
      w && D(w.id);
    }
  }, [b, D, h, m]);
  return Yt(a, r), a ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: r,
      tabIndex: -1,
      style: { position: "fixed", top: c.top, left: c.left },
      onKeyDown: _
    },
    u.map((E, w) => {
      if (E.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: w, className: "tlMenu__separator" });
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
}, or = 768, sr = ({ controlId: l }) => {
  const t = X(), n = le(), a = yt(!0);
  e.useEffect(() => {
    const c = window.matchMedia(`(max-width: ${or}px)`), d = (f) => {
      n("reportDisplayClass", { displayClass: f ? "COMPACT" : "REGULAR" });
    };
    d(c.matches);
    const m = (f) => d(f.matches);
    return c.addEventListener("change", m), () => c.removeEventListener("change", m);
  }, [n]);
  const o = t.header, i = t.notices, s = t.content, u = t.footer, r = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell " + a }, o && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(G, { control: o })), i && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__notices" }, /* @__PURE__ */ e.createElement(G, { control: i })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(Be, null, /* @__PURE__ */ e.createElement(G, { control: s }))), u && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(G, { control: u })), /* @__PURE__ */ e.createElement(G, { control: r }));
}, cr = ({ controlId: l }) => {
  const t = X(), n = t.text ?? "", a = t.cssClass ?? "", o = t.hasTooltip === !0, i = t.role || void 0, s = a ? `tlText ${a}` : "tlText";
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: s,
      role: i,
      "data-tooltip": o ? "key:tooltip" : void 0
    },
    n
  );
}, ir = ({ isMulti: l, cursorIndex: t, onMove: n, onToggle: a, onSelectAll: o, onActivate: i }) => (me("ArrowUp", () => (n("up", !1, !1), !0)), me("ArrowDown", () => (n("down", !1, !1), !0)), me("Home", () => (n("home", !1, !1), !0)), me("End", () => (n("end", !1, !1), !0)), me("PageUp", () => (n("pageUp", !1, !1), !0)), me("PageDown", () => (n("pageDown", !1, !1), !0)), me("Shift+ArrowUp", () => (n("up", l, !1), !0)), me("Shift+ArrowDown", () => (n("down", l, !1), !0)), me("Shift+Home", () => (n("home", l, !1), !0)), me("Shift+End", () => (n("end", l, !1), !0)), me("Shift+PageUp", () => (n("pageUp", l, !1), !0)), me("Shift+PageDown", () => (n("pageDown", l, !1), !0)), me("Ctrl+ArrowUp", () => (n("up", !1, l), !0)), me("Ctrl+ArrowDown", () => (n("down", !1, l), !0)), me("Space", () => t < 0 ? !1 : (a(), !0)), me("Ctrl+A", () => l ? (o(), !0) : !1), me("Enter", () => i()), null), ur = {
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
}, dr = 300, dn = 50, mr = 'input, textarea, select, button, a, [contenteditable="true"], [role="combobox"], [role="listbox"], [role="option"], [role="button"], [role="link"], [role="checkbox"], [role="radio"], [role="switch"], [role="textbox"], [role="spinbutton"], [role="slider"], [role="menu"], [role="menuitem"]';
function mt(l) {
  var n;
  const t = l.target;
  return !!((n = t == null ? void 0 : t.closest) != null && n.call(t, mr));
}
const Ht = 'input:not([disabled]):not([readonly]), textarea:not([disabled]):not([readonly]), select:not([disabled]), [contenteditable="true"]', mn = Ht + ", button:not([disabled]), a[href]";
function Nn(l, t) {
  return Array.from(l.querySelectorAll("[data-row][data-col]")).filter((n) => n.dataset.row === t);
}
function Lt(l, t, n = {}) {
  const a = Nn(l, t);
  if (n.col) {
    const i = a.find((u) => u.dataset.col === n.col), s = i == null ? void 0 : i.querySelector(Ht);
    if (s) return s;
  }
  if (n.col)
    return null;
  const o = n.last ? [...a].reverse() : a;
  for (const i of o) {
    const s = i.querySelector(Ht);
    if (s) return s;
  }
  return null;
}
const pr = ({ controlId: l }) => {
  var Zt;
  const t = X(), n = le(), a = ue(ur), o = e.useRef(null);
  e.useEffect(() => {
    const g = o.current;
    if (!g) return;
    const T = (U) => {
      const Z = U.detail;
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
  const i = t.columns ?? [], s = t.totalRowCount ?? 0, u = t.rows ?? [], r = t.rowHeight ?? 36, c = t.selectionMode ?? "single", d = t.selectedCount ?? 0, m = t.cursorIndex ?? -1, f = t.frozenColumnCount ?? 0, h = t.treeMode ?? !1, b = t.grouping ?? "", D = t.columnSelect ?? !1, _ = t.filterBar ?? !1, E = t.namedFilters ?? [], w = t.activeNamedFilter ?? "", k = t.search ?? "", I = t.filterSaving ?? !1, y = e.useMemo(
    () => i.filter((g) => g.sortPriority && g.sortPriority > 0).length,
    [i]
  ), v = c === "multi", C = 40, N = 20, x = e.useRef(null), S = e.useRef(null), F = e.useRef(null), H = e.useRef(null), B = e.useRef(null), [K, A] = e.useState({}), j = e.useRef(null), P = e.useRef(!1), L = e.useRef(null), [R, W] = e.useState(null), [p, M] = e.useState(null), [Y, V] = e.useState(null), [Q, $] = e.useState(0);
  e.useEffect(() => {
    const g = F.current;
    if (!g)
      return;
    const T = () => {
      const Z = g.offsetWidth - g.clientWidth;
      $((te) => te === Z ? te : Z);
    };
    T();
    const U = new ResizeObserver(T);
    return U.observe(g), () => U.disconnect();
  }, []), e.useEffect(() => {
    j.current || A({});
  }, [i]);
  const J = e.useCallback((g) => K[g.name] ?? g.width, [K]), se = e.useMemo(() => {
    const g = [];
    let T = v && f > 0 ? C : 0;
    for (let U = 0; U < f && U < i.length; U++)
      g.push(T), T += J(i[U]);
    return g;
  }, [i, f, v, C, J]), ne = e.useMemo(() => {
    if (f <= 0)
      return 0;
    let g = v ? C : 0;
    for (let T = 0; T < f && T < i.length; T++)
      g += J(i[T]);
    return g;
  }, [i, f, v, C, J]), Ee = s * r, be = e.useRef(null), we = e.useCallback((g, T, U) => {
    U.preventDefault(), U.stopPropagation(), j.current = { column: g, startX: U.clientX, startWidth: T };
    let Z = U.clientX, te = 0;
    const re = () => {
      const ce = j.current;
      if (!ce) return;
      const de = Math.max(dn, ce.startWidth + (Z - ce.startX) + te);
      A((Ce) => ({ ...Ce, [ce.column]: de }));
    }, oe = () => {
      const ce = F.current, de = x.current;
      if (!ce || !j.current) return;
      const Ce = ce.getBoundingClientRect(), xe = 40, Qt = 8, ll = ce.scrollLeft;
      Z > Ce.right - xe ? ce.scrollLeft += Qt : Z < Ce.left + xe && (ce.scrollLeft = Math.max(0, ce.scrollLeft - Qt));
      const Jt = ce.scrollLeft - ll;
      Jt !== 0 && (de && (de.scrollLeft = ce.scrollLeft), te += Jt, re()), be.current = requestAnimationFrame(oe);
    };
    be.current = requestAnimationFrame(oe);
    const fe = (ce) => {
      Z = ce.clientX, re();
    }, pe = (ce) => {
      document.removeEventListener("mousemove", fe), document.removeEventListener("mouseup", pe), be.current !== null && (cancelAnimationFrame(be.current), be.current = null);
      const de = j.current;
      if (de) {
        const Ce = Math.max(dn, de.startWidth + (ce.clientX - de.startX) + te);
        n("columnResize", { column: de.column, width: Ce }), j.current = null, P.current = !0, requestAnimationFrame(() => {
          P.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", fe), document.addEventListener("mouseup", pe);
  }, [n]), Te = e.useCallback(() => {
    x.current && F.current && (x.current.scrollLeft = F.current.scrollLeft), H.current !== null && clearTimeout(H.current), H.current = window.setTimeout(() => {
      const g = F.current;
      if (!g) return;
      const T = g.scrollTop, U = Math.ceil(g.clientHeight / r), Z = Math.floor(T / r);
      n("scroll", { start: Z, count: U });
    }, 80);
  }, [n, r]), Re = e.useCallback((g, T, U) => {
    if (P.current) return;
    let Z;
    !T || T === "desc" ? Z = "asc" : Z = "desc";
    const te = U.shiftKey ? "add" : "replace";
    n("sort", { column: g, direction: Z, mode: te });
  }, [n]), $e = e.useCallback((g, T) => {
    L.current = g, T.dataTransfer.effectAllowed = "move", T.dataTransfer.setData("text/plain", g);
  }, []), ot = e.useCallback((g, T) => {
    if (!L.current || L.current === g) {
      W(null);
      return;
    }
    T.preventDefault(), T.dataTransfer.dropEffect = "move";
    const U = T.currentTarget.getBoundingClientRect(), Z = T.clientX < U.left + U.width / 2 ? "left" : "right";
    W({ column: g, side: Z });
  }, []), O = e.useCallback((g) => {
    g.preventDefault(), g.stopPropagation();
    const T = L.current;
    if (!T || !R) {
      L.current = null, W(null);
      return;
    }
    let U = i.findIndex((te) => te.name === R.column);
    if (U < 0) {
      L.current = null, W(null);
      return;
    }
    const Z = i.findIndex((te) => te.name === T);
    R.side === "right" && U++, Z < U && U--, n("columnReorder", { column: T, targetIndex: U }), L.current = null, W(null);
  }, [i, R, n]), q = e.useCallback(() => {
    L.current = null, W(null);
  }, []), ae = e.useCallback((g, T) => {
    var te, re, oe, fe;
    const U = window.getSelection();
    if (U && !U.isCollapsed && T.currentTarget.contains(U.anchorNode))
      return;
    if (!mt(T) && ((te = F.current) == null || te.focus({ preventScroll: !0 }), !T.ctrlKey && !T.metaKey && !T.shiftKey)) {
      const pe = (fe = (oe = (re = T.target) == null ? void 0 : re.closest) == null ? void 0 : oe.call(re, "[data-col]")) == null ? void 0 : fe.getAttribute("data-col");
      B.current = { index: g, col: pe ?? void 0 };
    }
    const Z = u.find((pe) => pe.index === g);
    mt(T) && (Z != null && Z.selected) && !T.ctrlKey && !T.metaKey && !T.shiftKey || n("select", {
      rowIndex: g,
      ctrlKey: T.ctrlKey || T.metaKey,
      shiftKey: T.shiftKey
    });
  }, [n, u]), ie = e.useCallback((g, T) => {
    var U;
    mt(T) || ((U = u.find((Z) => Z.index === g)) == null ? void 0 : U.groupCount) == null && n("activate", { rowIndex: g });
  }, [n, u]), Xe = e.useCallback((g, T, U) => {
    n("moveSelection", { direction: g, extend: T, move: U });
  }, [n]), In = e.useCallback(() => {
    m < 0 || n("select", { rowIndex: m, ctrlKey: v, shiftKey: !1 });
  }, [n, m, v]), jn = e.useCallback(() => {
    n("selectAll", { selected: !0 });
  }, [n]), Bn = e.useCallback(() => {
    var T;
    if (m < 0)
      return !1;
    const g = document.activeElement;
    return (T = g == null ? void 0 : g.closest) != null && T.call(g, mn) ? !1 : (n("activate", { rowIndex: m }), !0);
  }, [n, m]), Pn = e.useCallback(
    () => !!o.current && o.current.contains(document.activeElement),
    []
  );
  e.useEffect(() => {
    if (m < 0)
      return;
    const g = F.current;
    if (!g)
      return;
    const T = m * r, U = T + r;
    T < g.scrollTop ? g.scrollTop = T : U > g.scrollTop + g.clientHeight && (g.scrollTop = U - g.clientHeight);
  }, [m, r]), e.useEffect(() => {
    const g = B.current, T = F.current;
    if (!g || !T)
      return;
    const U = u.find((re) => re.index === g.index);
    if (!U || !Lt(T, U.id))
      return;
    B.current = null;
    const Z = document.activeElement;
    if (Z && Z !== document.body && !T.contains(Z))
      return;
    const te = Lt(T, U.id, { col: g.col, last: g.last });
    te && (te.focus({ preventScroll: !0 }), te instanceof HTMLInputElement && te.select());
  }, [u]);
  const An = e.useCallback((g) => {
    if (g.key !== "Tab")
      return;
    const T = F.current, U = document.activeElement;
    if (!T || !U || !T.contains(U))
      return;
    const Z = U.closest("[data-row][data-col]");
    if (!Z)
      return;
    const te = Z.dataset.row, re = u.find((xe) => xe.id === te);
    if (!re)
      return;
    const oe = Nn(T, te).flatMap((xe) => Array.from(xe.querySelectorAll(mn))), fe = oe.indexOf(U);
    if (fe < 0)
      return;
    const pe = !g.shiftKey;
    if (!(pe ? fe === oe.length - 1 : fe === 0))
      return;
    const de = pe ? re.index + 1 : re.index - 1;
    if (de < 0 || de >= s)
      return;
    const Ce = u.find((xe) => xe.index === de);
    Ce && Lt(T, Ce.id) || (g.preventDefault(), B.current = { index: de, last: !pe }, n("select", { rowIndex: de, ctrlKey: !1, shiftKey: !1 }));
  }, [u, s, n]), Fn = e.useCallback((g, T) => {
    T.stopPropagation(), n("select", { rowIndex: g, ctrlKey: !0, shiftKey: !1 });
  }, [n]), On = e.useCallback(() => {
    const g = d === s && s > 0;
    n("selectAll", { selected: !g });
  }, [n, d, s]), $n = e.useCallback((g, T, U) => {
    U.stopPropagation(), n("expand", { rowIndex: g, expanded: T });
  }, [n]), Hn = e.useCallback((g, T) => {
    T.preventDefault(), M({ x: T.clientX, y: T.clientY, colIdx: g });
  }, []), Wn = e.useCallback(() => {
    p && (n("setFrozenColumnCount", { count: p.colIdx + 1 }), M(null));
  }, [p, n]), Un = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), M(null);
  }, [n]), Vn = e.useCallback((g) => {
    n("group", { column: g }), M(null);
  }, [n]), zn = e.useCallback(() => {
    n("group", { column: "" }), M(null);
  }, [n]), Kn = e.useCallback((g) => {
    g.preventDefault(), g.stopPropagation();
    const T = S.current, U = x.current;
    if (!T || !U)
      return;
    const Z = T.clientWidth, te = [{ x: 0, count: 0 }];
    U.querySelectorAll("[data-col-idx]").forEach((pe) => {
      const ce = pe.getBoundingClientRect().right - T.getBoundingClientRect().left;
      ce > 0 && ce <= Z && te.push({ x: ce, count: Number(pe.dataset.colIdx) + 1 });
    });
    let re = { x: ne, count: f };
    const oe = (pe) => {
      const ce = pe.clientX - T.getBoundingClientRect().left;
      re = te.reduce(
        (de, Ce) => Math.abs(Ce.x - ce) < Math.abs(de.x - ce) ? Ce : de,
        te[0]
      ), V(re);
    }, fe = () => {
      document.removeEventListener("mousemove", oe), document.removeEventListener("mouseup", fe), V(null), re.count !== f && n("setFrozenColumnCount", { count: re.count });
    };
    document.addEventListener("mousemove", oe), document.addEventListener("mouseup", fe);
  }, [ne, f, n]);
  e.useEffect(() => {
    if (!p) return;
    const g = () => M(null);
    return document.addEventListener("mousedown", g), () => document.removeEventListener("mousedown", g);
  }, [p]), Oe(!!p, { ESCAPE: () => M(null) });
  const Yn = e.useCallback((g, T) => {
    T.stopPropagation(), T.preventDefault(), n("openFilter", { column: g });
  }, [n]), Gn = e.useCallback((g) => {
    g.stopPropagation(), g.preventDefault(), n("openColumnSelect", {});
  }, [n]), [Xn, Xt] = e.useState(k), wt = e.useRef(!1), Le = e.useRef(null);
  e.useEffect(() => {
    wt.current || Xt(k);
  }, [k]), e.useEffect(() => () => {
    Le.current !== null && clearTimeout(Le.current);
  }, []);
  const st = e.useCallback((g) => {
    Le.current !== null && (clearTimeout(Le.current), Le.current = null), wt.current = !1, n("search", { term: g });
  }, [n]), qn = e.useCallback((g) => {
    Xt(g), wt.current = !0, Le.current !== null && clearTimeout(Le.current), Le.current = window.setTimeout(() => st(g), dr);
  }, [st]), Zn = e.useCallback((g) => {
    g.key === "Enter" && (g.preventDefault(), st(g.currentTarget.value));
  }, [st]), Qn = e.useCallback((g) => {
    g === w ? n("clearFilter", {}) : n("applyNamedFilter", { id: g });
  }, [w, n]), Jn = e.useCallback((g, T) => {
    T.stopPropagation(), n("deleteNamedFilter", { id: g });
  }, [n]), [qe, Ze] = e.useState(null), kt = e.useCallback(() => {
    const g = (qe ?? "").trim();
    g && (n("saveNamedFilter", { filterName: g }), Ze(null));
  }, [qe, n]), el = e.useCallback((g) => {
    g.key === "Enter" ? (g.preventDefault(), kt()) : g.key === "Escape" && (g.preventDefault(), Ze(null));
  }, [kt]), Nt = i.reduce((g, T) => g + J(T), 0) + (v ? C : 0), St = D ? 32 : 0, tl = d === s && s > 0, qt = d > 0 && d < s, nl = e.useCallback((g) => {
    g && (g.indeterminate = qt);
  }, [qt]);
  return /* @__PURE__ */ e.createElement(Kt, { active: Pn }, /* @__PURE__ */ e.createElement(
    ir,
    {
      isMulti: v,
      cursorIndex: m,
      onMove: Xe,
      onToggle: In,
      onSelectAll: jn,
      onActivate: Bn
    }
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: o,
      id: l,
      className: "tlTableView",
      "data-tooltip": "dynamic",
      onDragOver: (g) => {
        if (!L.current) return;
        g.preventDefault();
        const T = F.current, U = x.current;
        if (!T) return;
        const Z = T.getBoundingClientRect(), te = 40, re = 8;
        g.clientX < Z.left + te ? T.scrollLeft = Math.max(0, T.scrollLeft - re) : g.clientX > Z.right - te && (T.scrollLeft += re), U && (U.scrollLeft = T.scrollLeft);
      },
      onDrop: O
    },
    _ && /* @__PURE__ */ e.createElement("div", { className: "tlTableView__filterBar" }, E.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlTableView__filterChips" }, E.map((g) => {
      const T = g.id === w;
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
            onClick: () => Qn(g.id)
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
            onClick: (U) => Jn(g.id, U)
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
        value: Xn,
        onChange: (g) => qn(g.target.value),
        onKeyDown: Zn
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
        onKeyDown: el
      }
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__barButton",
        title: a["js.table.saveFilter"],
        "aria-label": a["js.table.saveFilter"],
        disabled: !qe.trim(),
        onClick: kt
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
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerArea", ref: S }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: x }, /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerRow",
        style: { width: Nt, paddingRight: St + Q }
      },
      v && /* @__PURE__ */ e.createElement(
        "div",
        {
          className: "tlTableView__headerCell tlTableView__checkboxCell" + (f > 0 ? " tlTableView__headerCell--frozen" : ""),
          style: {
            width: C,
            minWidth: C,
            ...f > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
          },
          onDragOver: (g) => {
            L.current && (g.preventDefault(), g.dataTransfer.dropEffect = "move", i.length > 0 && i[0].name !== L.current && W({ column: i[0].name, side: "left" }));
          }
        },
        /* @__PURE__ */ e.createElement(
          "input",
          {
            type: "checkbox",
            ref: nl,
            className: "tlTableView__checkbox",
            checked: tl,
            onChange: On
          }
        )
      ),
      i.map((g, T) => {
        const U = J(g);
        i.length - 1;
        let Z = "tlTableView__headerCell";
        g.sortable && (Z += " tlTableView__headerCell--sortable"), R && R.column === g.name && (Z += " tlTableView__headerCell--dragOver-" + R.side);
        const te = T < f, re = T === f - 1;
        return te && (Z += " tlTableView__headerCell--frozen"), re && (Z += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
          "div",
          {
            key: g.name,
            className: Z,
            "data-col-idx": T,
            style: {
              width: U,
              minWidth: U,
              position: te ? "sticky" : "relative",
              ...te ? { left: se[T], zIndex: 2 } : {}
            },
            draggable: !0,
            onClick: g.sortable ? (oe) => Re(g.name, g.sortDirection, oe) : void 0,
            onContextMenu: (oe) => Hn(T, oe),
            onDragStart: (oe) => $e(g.name, oe),
            onDragOver: (oe) => ot(g.name, oe),
            onDrop: O,
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
              onClick: (oe) => Yn(g.name, oe)
            },
            /* @__PURE__ */ e.createElement("i", { className: g.filterActive ? "bi bi-funnel-fill" : "bi bi-funnel" })
          ),
          g.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, g.sortDirection === "asc" ? "▲" : "▼", y > 1 && g.sortPriority != null && g.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, g.sortPriority)),
          /* @__PURE__ */ e.createElement(
            "div",
            {
              className: "tlTableView__resizeHandle",
              onMouseDown: (oe) => we(g.name, U, oe)
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
              T.name !== L.current && (g.preventDefault(), g.dataTransfer.dropEffect = "move", W({ column: T.name, side: "right" }));
            }
          },
          onDrop: O
        }
      )
    )), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__frozenSplitter" + (Y ? " tlTableView__frozenSplitter--active" : ""),
        style: { left: ne },
        title: a["js.table.freezeSplitter"],
        onMouseDown: Kn
      }
    ), D && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__columnsButton",
        title: a["js.table.columns"],
        "aria-label": a["js.table.columns"],
        onClick: Gn
      },
      /* @__PURE__ */ e.createElement("i", { className: "bi bi-gear" })
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: F,
        className: "tlTableView__body",
        onScroll: Te,
        onKeyDown: An,
        tabIndex: 0
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: Ee, position: "relative", width: Nt, paddingRight: St } }, u.map((g) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: g.id,
          className: "tlTableView__row" + (g.selected ? " tlTableView__row--selected" : "") + (g.index === m ? " tlTableView__row--cursor" : "") + (g.groupCount != null ? " tlTableView__row--group" : ""),
          style: {
            position: "absolute",
            top: g.index * r,
            height: r,
            width: Nt,
            paddingRight: St,
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
            className: "tlTableView__cell tlTableView__checkboxCell" + (f > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: C,
              minWidth: C,
              ...f > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
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
              onClick: (T) => Fn(g.index, T),
              tabIndex: -1
            }
          )
        ),
        i.map((T, U) => {
          const Z = J(T), te = U === i.length - 1, re = U < f, oe = U === f - 1;
          let fe = "tlTableView__cell";
          re && (fe += " tlTableView__cell--frozen"), oe && (fe += " tlTableView__cell--frozenLast");
          const pe = h && U === 0, ce = g.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: T.name,
              className: fe,
              "data-row": g.id,
              "data-col": T.name,
              style: {
                ...te && !re ? { flex: "1 0 auto", minWidth: Z } : { width: Z, minWidth: Z },
                ...re ? { position: "sticky", left: se[U], zIndex: 2 } : {}
              }
            },
            pe ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ce * N } }, g.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (de) => $n(g.index, !g.expanded, de)
              },
              g.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), g.cells[T.name] && /* @__PURE__ */ e.createElement(G, { control: g.cells[T.name] }), g.groupCount != null && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__groupCount" }, "(", g.groupCount, ")")) : g.cells[T.name] && /* @__PURE__ */ e.createElement(G, { control: g.cells[T.name] })
          );
        })
      )))
    ),
    Y && /* @__PURE__ */ e.createElement("div", { className: "tlTableView__frozenPreview", style: { left: Y.x } }),
    p && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: p.y, left: p.x, zIndex: 1e4 },
        onMouseDown: (g) => g.stopPropagation()
      },
      p.colIdx + 1 !== f && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Wn }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      f > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Un }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"])),
      ((Zt = i[p.colIdx]) == null ? void 0 : Zt.groupable) && i[p.colIdx].name !== b && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlMenu__item",
          role: "menuitem",
          onClick: () => Vn(i[p.colIdx].name)
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.groupBy"])
      ),
      b !== "" && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: zn }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.ungroup"]))
    )
  ));
}, fr = {
  "js.table.columnSearch": "Find column",
  "js.table.groupBy": "Group by this column",
  "js.table.ungroup": "Remove grouping"
}, hr = ({ controlId: l }) => {
  const t = X(), n = le(), a = ue(fr), o = t.entries ?? [], i = o.filter((v) => v.visible).length, [s, u] = e.useState(""), r = s.trim().toLowerCase(), c = r ? o.filter((v) => v.label.toLowerCase().includes(r)) : o, d = e.useRef(null), m = e.useRef(null), [f, h] = e.useState(null), b = e.useCallback((v) => {
    m.current = v, h(v);
  }, []), D = e.useCallback((v, C) => {
    n("columnVisible", { column: v, visible: C });
  }, [n]), _ = e.useCallback((v) => {
    n("groupBy", { column: v });
  }, [n]), E = e.useCallback((v, C) => {
    d.current = v, C.dataTransfer.effectAllowed = "move", C.dataTransfer.setData("text/plain", v);
  }, []), w = e.useCallback((v, C) => {
    if (!d.current || d.current === v) {
      b(null);
      return;
    }
    C.preventDefault(), C.dataTransfer.dropEffect = "move";
    const N = C.currentTarget.getBoundingClientRect(), x = C.clientY < N.top + N.height / 2 ? "top" : "bottom";
    b({ name: v, side: x });
  }, [b]), k = e.useCallback(() => {
    d.current = null, b(null);
  }, [b]), I = e.useCallback((v) => {
    v.preventDefault();
    const C = d.current, N = m.current;
    if (d.current = null, b(null), !C || !N)
      return;
    const x = o.findIndex((H) => H.name === N.name), S = o.findIndex((H) => H.name === C);
    if (x < 0 || S < 0)
      return;
    let F = N.side === "top" ? x : x + 1;
    S < F && F--, F !== S && n("columnReorder", { column: C, targetIndex: F });
  }, [o, n, b]), y = o.length > 10;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlColumnSelect", onDrop: I }, y && /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__search" }, /* @__PURE__ */ e.createElement("i", { className: "bi bi-search", "aria-hidden": "true" }), /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "search",
      className: "tlColumnSelect__searchInput",
      placeholder: a["js.table.columnSearch"],
      "aria-label": a["js.table.columnSearch"],
      value: s,
      onChange: (v) => u(v.target.value)
    }
  )), /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__list" + (y ? " tlColumnSelect__list--fixed" : "") }, c.map((v) => {
    const C = v.visible && i <= 1;
    let N = "tlColumnSelect__row";
    return f && f.name === v.name && (N += " tlColumnSelect__row--dragOver-" + f.side), /* @__PURE__ */ e.createElement(
      "div",
      {
        key: v.name,
        className: N,
        draggable: !0,
        onDragStart: (x) => E(v.name, x),
        onDragOver: (x) => w(v.name, x),
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
          onClick: () => _(v.name)
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
          onChange: (x) => D(v.name, x.target.checked)
        }
      ), /* @__PURE__ */ e.createElement("span", null, v.label))
    );
  })));
}, { useState: Wt, useRef: nt, useCallback: gt, useMemo: Ae, useEffect: pn } = e, br = {
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
}, _e = 44, vt = 15, ye = 6e4, gr = 36e5, je = 864e5, Er = 8;
function Se(l) {
  const t = new Date(l);
  return t.setHours(0, 0, 0, 0), t.getTime();
}
function ze(l, t) {
  const n = new Date(l);
  return n.setDate(n.getDate() + t), n.getTime();
}
function vr(l) {
  return Se(l);
}
function lt(l, t) {
  return Se(l) === Se(t);
}
function Ie(l) {
  return (l - Se(l)) / ye;
}
function Qe(l) {
  return Math.round(l / vt) * vt;
}
function Je(l, t, n) {
  return Math.max(t, Math.min(n, l));
}
function _t(l) {
  if (!l)
    return "tlCalEvent--default";
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return "tlCalEvent--c" + Math.abs(t) % Er;
}
function Ct(l, t) {
  return l.color ? { ...t, "--cal-ev-bg": l.color } : t;
}
function _r(l) {
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
function Fe(l, t, n) {
  return new Intl.DateTimeFormat(l, t).format(new Date(n));
}
function Cr(l, t) {
  const n = { hour: "numeric", minute: "2-digit" };
  return Fe(l, n, t.start) + "–" + Fe(l, n, t.end);
}
const yr = [
  { key: "DAY", label: "js.calendar.day" },
  { key: "WORK_WEEK", label: "js.calendar.workWeek" },
  { key: "WEEK", label: "js.calendar.week" },
  { key: "MONTH", label: "js.calendar.month" },
  { key: "YEAR", label: "js.calendar.year" }
], wr = ({ title: l, granularity: t, i18n: n, send: a }) => /* @__PURE__ */ e.createElement("div", { className: "tlCalToolbar" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalNav" }, /* @__PURE__ */ e.createElement("button", { className: "tlCalBtn", onClick: () => a("navigate", { direction: "TODAY" }) }, n["js.calendar.today"]), /* @__PURE__ */ e.createElement(
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
)), /* @__PURE__ */ e.createElement("div", { className: "tlCalTitle" }, l), /* @__PURE__ */ e.createElement("div", { className: "tlCalGranularity" }, yr.map((o) => /* @__PURE__ */ e.createElement(
  "button",
  {
    key: o.key,
    className: "tlCalBtn" + (o.key === t ? " tlCalBtn--active" : ""),
    onClick: () => a("switchGranularity", { granularity: o.key })
  },
  n[o.label]
))));
function kr(l) {
  const t = [...l].sort((s, u) => s.start - u.start || u.end - s.end), n = [];
  let a = [], o = -1;
  const i = () => {
    const s = a.reduce((u, r) => Math.max(u, r.col + 1), 0);
    for (const u of a)
      u.cols = s;
    n.push(...a), a = [], o = -1;
  };
  for (const s of t) {
    a.length > 0 && s.start >= o && i();
    const u = new Set(a.filter((c) => c.ev.end > s.start).map((c) => c.col));
    let r = 0;
    for (; u.has(r); )
      r++;
    a.push({
      ev: s,
      topMin: Ie(s.start),
      botMin: Ie(s.start) + Math.max(15, (s.end - s.start) / ye),
      col: r,
      cols: 1
    }), o = Math.max(o, s.end);
  }
  return a.length > 0 && i(), n;
}
const xt = (l) => {
  l.preventDefault(), l.currentTarget.setPointerCapture(l.pointerId);
}, Ut = ({ className: l, placeholder: t, style: n, onCommit: a, onDiscard: o }) => {
  const i = nt(!1), s = (u) => {
    i.current || (i.current = !0, u === null ? o() : a(u));
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
}, Sn = (l) => {
  const [t, n] = Wt(null), a = nt(null);
  a.current = t;
  const o = gt((u) => n(u), []), i = gt(() => n(null), []), s = gt(
    (u) => {
      const r = a.current;
      r && l("createSlot", { ...r, title: u }), n(null);
    },
    [l]
  );
  return { pending: t, open: o, commit: s, discard: i };
}, Nr = ({
  ctx: l,
  rangeStart: t,
  granularity: n
}) => {
  const { events: a, locale: o, nonWorkingDays: i, dayStartHour: s, dayEndHour: u, now: r, send: c, editable: d, i18n: m } = l, f = Ae(() => {
    const A = n === "DAY" ? 1 : 7, j = [];
    for (let P = 0; P < A; P++) {
      const L = ze(t, P);
      n === "WORK_WEEK" && i.includes(new Date(L).getDay()) || j.push(L);
    }
    return j;
  }, [t, n, i]), h = Sn(c), b = nt(null), D = nt(null), [_, E] = Wt(null), w = nt(null);
  w.current = _;
  const [k, I] = Wt(Date.now());
  pn(() => {
    const A = window.setInterval(() => I(Date.now()), 6e4);
    return () => window.clearInterval(A);
  }, []);
  const y = gt(
    (A, j) => {
      const P = b.current;
      if (!P)
        return { dayIndex: 0, min: 0 };
      const L = P.getBoundingClientRect(), R = L.width / f.length, W = Je(Math.floor((A - L.left) / R), 0, f.length - 1), p = j - L.top + P.scrollTop, M = Je(p / _e * 60, 0, 1440);
      return { dayIndex: W, min: M };
    },
    [f.length]
  );
  pn(() => {
    if (!_)
      return;
    const A = (L) => {
      const R = w.current;
      if (!R)
        return;
      const { dayIndex: W, min: p } = y(L.clientX, L.clientY);
      R.mode === "move" ? E({ ...R, dayStart: f[W], startMin: Je(Qe(p - R.grabMin), 0, 1440 - R.dur) }) : R.mode === "resize" ? E({ ...R, endMin: Je(Qe(p), R.startMin + vt, 1440) }) : E({ ...R, toMin: Je(Qe(p), 0, 1440) });
    }, j = () => {
      const L = w.current;
      if (E(null), !!L)
        if (L.mode === "move") {
          const R = L.dayStart + L.startMin * ye;
          R !== L.origStartMs && c("moveEvent", { eventId: L.id, start: R, end: R + L.dur * ye });
        } else if (L.mode === "resize") {
          const R = L.dayStart + L.endMin * ye;
          R !== L.origEndMs && c("resizeEvent", { eventId: L.id, end: R });
        } else {
          const R = Math.min(L.fromMin, L.toMin), W = Math.max(L.fromMin, L.toMin);
          W - R >= vt && h.open({ start: L.dayStart + R * ye, end: L.dayStart + W * ye, allDay: !1 });
        }
    }, P = () => E(null);
    return window.addEventListener("pointermove", A), window.addEventListener("pointerup", j, { once: !0 }), window.addEventListener("pointercancel", P), () => {
      window.removeEventListener("pointermove", A), window.removeEventListener("pointerup", j), window.removeEventListener("pointercancel", P);
    };
  }, [_, f, y, c, h.open]);
  const v = (A, j, P) => {
    if (!d || !j.movable)
      return;
    A.stopPropagation(), xt(A), h.discard();
    const { min: L } = y(A.clientX, A.clientY), R = (j.end - j.start) / ye;
    E({
      mode: "move",
      id: j.id,
      grabMin: L - Ie(j.start),
      dur: R,
      dayStart: P,
      startMin: Ie(j.start),
      origStartMs: j.start
    });
  }, C = (A, j, P) => {
    !d || !j.resizable || (A.stopPropagation(), xt(A), h.discard(), E({
      mode: "resize",
      id: j.id,
      dayStart: P,
      startMin: Ie(j.start),
      endMin: Ie(j.end),
      origEndMs: j.end
    }));
  }, N = (A, j) => {
    if (!d || A.button !== 0)
      return;
    xt(A), h.discard();
    const { min: P } = y(A.clientX, A.clientY);
    E({ mode: "create", dayStart: j, fromMin: Qe(P), toMin: Qe(P) });
  }, x = Array.from({ length: 24 }, (A, j) => j), S = Ae(() => {
    if (_ === null || !("id" in _))
      return a;
    const A = _;
    return a.map((j) => {
      if (j.id !== A.id)
        return j;
      if (A.mode === "move") {
        const P = A.dayStart + A.startMin * ye;
        return { ...j, start: P, end: P + A.dur * ye };
      }
      return { ...j, end: A.dayStart + A.endMin * ye };
    });
  }, [a, _]), F = Ae(() => f.map(
    (A) => kr(
      S.filter((j) => !j.allDay && j.start < A + je && j.end > A)
    )
  ), [f, S]), H = Ae(() => f.map((A) => S.filter((j) => j.allDay && j.start < A + je && j.end > A)), [f, S]), B = s * _e, K = u * _e;
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeGrid" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeHeader" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalGutter" }), f.map((A) => {
    const j = i.includes(new Date(A).getDay()), P = lt(A, l.now);
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: A,
        className: "tlCalDayHead" + (j ? " tlCalDayHead--nonworking" : "") + (P ? " tlCalDayHead--today" : ""),
        onClick: () => c("goto", { date: A, granularity: "DAY" })
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlCalDayName" }, Fe(o, { weekday: "short" }, A)),
      /* @__PURE__ */ e.createElement("span", { className: "tlCalDayNum" }, new Date(A).getDate())
    );
  })), /* @__PURE__ */ e.createElement("div", { className: "tlCalAllDayRow" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalGutter tlCalAllDayLabel" }, m["js.calendar.allDay"]), f.map((A, j) => /* @__PURE__ */ e.createElement(
    "div",
    {
      key: A,
      className: "tlCalAllDayCell",
      onClick: () => d && h.open({ start: A, end: A + je, allDay: !0 })
    },
    h.pending && h.pending.allDay && h.pending.start === A && /* @__PURE__ */ e.createElement(
      Ut,
      {
        className: "tlCalAllDayEvent tlCalEvent--preview",
        placeholder: m["js.calendar.newEventTitle"],
        onCommit: h.commit,
        onDiscard: h.discard
      }
    ),
    H[j].map((P) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: P.id,
        className: "tlCalAllDayEvent " + _t(P.category) + (P.selected ? " tlCalEvent--selected" : ""),
        style: Ct(P),
        title: P.tooltip,
        onClick: (L) => {
          L.stopPropagation(), c("selectEvent", { eventId: P.id });
        }
      },
      P.title
    ))
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlCalScroll", ref: D }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeBody", style: { height: 24 * _e } }, /* @__PURE__ */ e.createElement("div", { className: "tlCalHourAxis" }, x.map((A) => /* @__PURE__ */ e.createElement("div", { key: A, className: "tlCalHourLabel", style: { top: A * _e } }, A === 0 ? "" : Fe(o, { hour: "numeric" }, Se(t) + A * gr)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalColumns", ref: b, style: { gridTemplateColumns: `repeat(${f.length}, 1fr)` } }, f.map((A, j) => {
    const P = i.includes(new Date(A).getDay()), L = _ && ("dayStart" in _ && _.dayStart === A) ? _ : null;
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: A,
        className: "tlCalCol" + (P ? " tlCalCol--nonworking" : ""),
        onPointerDown: (R) => N(R, A)
      },
      x.map((R) => /* @__PURE__ */ e.createElement("div", { key: R, className: "tlCalHourLine", style: { top: R * _e } })),
      /* @__PURE__ */ e.createElement("div", { className: "tlCalWorkBand", style: { top: B, height: K - B } }),
      lt(A, k) && /* @__PURE__ */ e.createElement("div", { className: "tlCalNowLine", style: { top: Ie(Date.now()) / 60 * _e } }),
      F[j].map((R) => {
        const W = _ !== null && "id" in _ && _.id === R.ev.id, p = R.topMin / 60 * _e, M = (R.botMin - R.topMin) / 60 * _e, Y = 100 / R.cols;
        return /* @__PURE__ */ e.createElement(
          "div",
          {
            key: R.ev.id,
            className: "tlCalEvent " + _t(R.ev.category) + (R.ev.selected ? " tlCalEvent--selected" : "") + (W ? " tlCalEvent--dragging" : ""),
            style: Ct(R.ev, {
              top: p,
              height: M,
              left: `${R.col * Y}%`,
              width: `calc(${Y}% - 2px)`
            }),
            title: R.ev.tooltip,
            onPointerDown: (V) => v(V, R.ev, A),
            onClick: (V) => {
              V.stopPropagation(), c("selectEvent", { eventId: R.ev.id });
            }
          },
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTime" }, Cr(o, R.ev)),
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTitle" }, R.ev.title),
          d && R.ev.resizable && /* @__PURE__ */ e.createElement("span", { className: "tlCalResizeHandle", onPointerDown: (V) => C(V, R.ev, A) })
        );
      }),
      h.pending && !h.pending.allDay && Se(h.pending.start) === A && /* @__PURE__ */ e.createElement(
        Ut,
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
}, Sr = 3, Dr = ({ ctx: l, rangeStart: t, anchorMonth: n }) => {
  const { events: a, locale: o, nonWorkingDays: i, send: s, editable: u, now: r, i18n: c } = l, d = Sn(s), m = Ae(() => {
    const h = [];
    for (let b = 0; b < 6; b++) {
      const D = [];
      for (let _ = 0; _ < 7; _++)
        D.push(ze(t, b * 7 + _));
      h.push(D);
    }
    return h;
  }, [t]), f = (h, b) => {
    h.preventDefault();
    const D = h.dataTransfer.getData("text/plain"), _ = a.find((w) => w.id === D);
    if (!_ || !u || !_.movable)
      return;
    const E = b - Se(_.start);
    s("moveEvent", { eventId: D, start: _.start + E, end: _.end + E });
  };
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalMonth" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthHead" }, m[0].map((h) => /* @__PURE__ */ e.createElement("div", { key: h, className: "tlCalMonthWeekday" }, Fe(o, { weekday: "short" }, h)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthBody" }, m.map((h, b) => {
    const D = h[0], _ = ze(D, 7), E = a.filter((k) => (k.allDay || k.end - k.start >= je) && k.start < _ && k.end > D).sort((k, I) => k.start - I.start).slice(0, 3), w = E.length;
    return /* @__PURE__ */ e.createElement("div", { key: b, className: "tlCalMonthWeek" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthDays" }, h.map((k) => {
      const I = new Date(k).getMonth() === new Date(n).getMonth(), y = i.includes(new Date(k).getDay()), v = lt(k, r);
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: k,
          className: "tlCalMonthCell" + (I ? "" : " tlCalMonthCell--other") + (y ? " tlCalMonthCell--nonworking" : ""),
          onDragOver: (C) => C.preventDefault(),
          onDrop: (C) => f(C, k),
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
          Ut,
          {
            className: "tlCalMonthBar tlCalEvent--preview tlCalMonthCreate",
            placeholder: c["js.calendar.newEventTitle"],
            onCommit: d.commit,
            onDiscard: d.discard
          }
        )
      );
    })), /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthOverlay" }, E.map((k, I) => {
      const y = Math.max(0, Math.floor((Se(Math.max(k.start, D)) - D) / je)), v = Math.min(7, Math.ceil((k.end - D) / je));
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: k.id,
          className: "tlCalMonthBar " + _t(k.category) + (k.selected ? " tlCalEvent--selected" : ""),
          style: Ct(k, {
            gridColumn: `${y + 1} / ${Math.max(y + 1, v) + 1}`,
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
      const y = a.filter((N) => !N.allDay && N.end - N.start < je && lt(N.start, k)).sort((N, x) => N.start - x.start), v = y.slice(0, Sr), C = y.length - v.length;
      return v.map((N, x) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: N.id,
          className: "tlCalChip " + _t(N.category) + (N.selected ? " tlCalEvent--selected" : ""),
          style: Ct(N, { gridColumn: I + 1, gridRow: w + 1 + x }),
          draggable: u && N.movable,
          onDragStart: (S) => S.dataTransfer.setData("text/plain", N.id),
          title: N.tooltip,
          onClick: (S) => {
            S.stopPropagation(), s("selectEvent", { eventId: N.id });
          }
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipDot" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipTime" }, Fe(o, { hour: "numeric", minute: "2-digit" }, N.start)),
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipTitle" }, N.title)
      )).concat(
        C > 0 ? [
          /* @__PURE__ */ e.createElement(
            "div",
            {
              key: "more-" + k,
              className: "tlCalMore",
              style: { gridColumn: I + 1, gridRow: w + 1 + v.length },
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
}, Tr = ({ ctx: l, rangeStart: t }) => {
  const { events: n, locale: a, firstDayOfWeek: o, nonWorkingDays: i, send: s, now: u } = l, r = Ae(() => {
    const f = /* @__PURE__ */ new Set();
    for (const h of n) {
      let b = Se(h.start);
      const D = h.end;
      for (; b < D; )
        f.add(b), b = ze(b, 1);
    }
    return f;
  }, [n]), c = new Date(t).getFullYear(), d = Array.from({ length: 12 }, (f, h) => new Date(c, h, 1).getTime()), m = Ae(() => {
    const f = new Date(2023, 0, 1);
    return Array.from({ length: 7 }, (h, b) => {
      const D = new Date(f);
      return D.setDate(f.getDate() + (o + b) % 7), new Intl.DateTimeFormat(a, { weekday: "narrow" }).format(D);
    });
  }, [a, o]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalYear" }, d.map((f) => {
    const h = new Date(f), b = Se(ze(f, -((h.getDay() - o + 7) % 7))), D = Array.from({ length: 42 }, (_, E) => ze(b, E));
    return /* @__PURE__ */ e.createElement("div", { key: f, className: "tlCalMini" }, /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlCalMiniTitle",
        onClick: () => s("goto", { date: f, granularity: "MONTH" })
      },
      Fe(a, { month: "long" }, f)
    ), /* @__PURE__ */ e.createElement("div", { className: "tlCalMiniGrid" }, m.map((_, E) => /* @__PURE__ */ e.createElement("div", { key: "h" + E, className: "tlCalMiniWd" }, _)), D.map((_) => {
      const E = new Date(_).getMonth() === h.getMonth(), w = i.includes(new Date(_).getDay()), k = lt(_, u), I = r.has(vr(_));
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: _,
          className: "tlCalMiniDay" + (E ? "" : " tlCalMiniDay--other") + (w ? " tlCalMiniDay--nonworking" : "") + (k ? " tlCalMiniDay--today" : "") + (I ? " tlCalMiniDay--event" : ""),
          onClick: () => s("goto", { date: _, granularity: "DAY" })
        },
        new Date(_).getDate()
      );
    })));
  }));
}, Rr = ({ controlId: l }) => {
  const t = X(), n = le(), a = ue(br), o = t.granularity ?? "WEEK", i = t.rangeStart ?? Date.now(), s = t.anchor ?? i, u = t.title ?? "", r = {
    locale: t.locale ?? "en",
    firstDayOfWeek: t.firstDayOfWeek ?? 0,
    nonWorkingDays: t.nonWorkingDays ?? [0, 6],
    dayStartHour: t.dayStartHour ?? 8,
    dayEndHour: t.dayEndHour ?? 18,
    editable: t.editable !== !1,
    now: t.now ?? Date.now(),
    events: _r(t.events),
    send: n,
    i18n: a
  };
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCalendar" }, /* @__PURE__ */ e.createElement(wr, { title: u, granularity: o, i18n: a, send: n }), /* @__PURE__ */ e.createElement("div", { className: "tlCalBody" }, o === "MONTH" ? /* @__PURE__ */ e.createElement(Dr, { ctx: r, rangeStart: i, anchorMonth: s }) : o === "YEAR" ? /* @__PURE__ */ e.createElement(Tr, { ctx: r, rangeStart: i }) : /* @__PURE__ */ e.createElement(Nr, { ctx: r, rangeStart: i, granularity: o })));
}, Lr = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, Dn = e.createContext(Lr), { useMemo: xr, useRef: Mr, useState: Ir, useEffect: jr } = e, Br = 320, Pr = "TLTableView", Ar = "TLPanel", Fr = ({ controlId: l }) => {
  var _;
  const t = X(), n = t.maxColumns ?? 3, a = t.labelPosition ?? "auto", o = t.readOnly === !0, i = t.children ?? [], s = t.noModelMessage, u = Mr(null), [r, c] = Ir(
    a === "top" ? "top" : "side"
  );
  jr(() => {
    if (a !== "auto") {
      c(a);
      return;
    }
    const E = u.current;
    if (!E) return;
    const w = new ResizeObserver((k) => {
      for (const I of k) {
        const v = I.contentRect.width / n;
        c(v < Br ? "top" : "side");
      }
    });
    return w.observe(E), () => w.disconnect();
  }, [a, n]);
  const d = xr(() => ({
    readOnly: o,
    resolvedLabelPosition: r
  }), [o, r]), f = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, h = i.length === 1 ? i[0] : void 0, b = !!h && (h.module === Pr || h.module === Ar && ((_ = h.state) == null ? void 0 : _.bare) === !0), D = [
    "tlFormLayout",
    o ? "tlFormLayout--readonly" : "",
    b ? "tlFormLayout--flush" : ""
  ].filter(Boolean).join(" ");
  return s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, s)) : /* @__PURE__ */ e.createElement(Dn.Provider, { value: d }, /* @__PURE__ */ e.createElement("div", { id: l, className: D, style: f, ref: u }, i.map((E, w) => /* @__PURE__ */ e.createElement(G, { key: w, control: E }))));
}, { useCallback: Or } = e, $r = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, Hr = ({ controlId: l }) => {
  const t = X(), n = le(), a = ue($r), o = t.headerControl ?? null, i = t.headerActions ?? [], s = t.collapsible === !0, u = t.collapsed === !0, r = t.border ?? "none", c = t.fullLine === !0, d = t.children ?? [], m = o != null || i.length > 0 || s, f = Or(() => {
    n("toggleCollapse");
  }, [n]), h = [
    "tlFormGroup",
    `tlFormGroup--border-${r}`,
    c ? "tlFormGroup--fullLine" : "",
    u ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: h }, m && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, s && /* @__PURE__ */ e.createElement(
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
  ), o && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(G, { control: o })), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, i.map((b, D) => /* @__PURE__ */ e.createElement(G, { key: D, control: b })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, d.map((b, D) => /* @__PURE__ */ e.createElement(G, { key: D, control: b }))));
}, { useContext: Wr, useState: Ur, useCallback: Vr } = e, zr = ({ controlId: l }) => {
  const t = X(), n = Wr(Dn), a = t.label ?? "", o = t.required === !0, i = t.error, s = t.errorIcon, u = t.warnings, r = t.warningIcon, c = t.helpText, d = t.dirty === !0, m = t.labelPosition ?? n.resolvedLabelPosition, f = t.fullLine === !0, h = t.visible !== !1, b = t.hasTooltip === !0, D = t.field, _ = n.readOnly, [E, w] = Ur(!1), k = Vr(() => w((N) => !N), []), I = m === "hidden", y = i != null, v = u != null && u.length > 0, C = [
    "tlFormField",
    `tlFormField--${m}`,
    _ ? "tlFormField--readonly" : "",
    f ? "tlFormField--fullLine" : "",
    y ? "tlFormField--error" : "",
    !y && v ? "tlFormField--warning" : "",
    d ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: C, style: h ? void 0 : { display: "none" } }, !I && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": b ? "key:tooltip" : void 0
    },
    a
  ), o && !_ && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), d && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), c && !_ && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(G, { control: D })), !_ && y && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(Ft, { image: s, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, i)), !_ && !y && v && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, u.map((N, x) => /* @__PURE__ */ e.createElement("div", { key: x, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(Ft, { image: r, className: "tlFormField__warningIcon" }), /* @__PURE__ */ e.createElement("span", null, N)))), !_ && c && E && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, c));
}, Kr = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.iconCss, o = t.iconSrc, i = t.label, s = t.cssClass, u = t.hasTooltip === !0, r = t.hasLink, c = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : o ? /* @__PURE__ */ e.createElement("img", { src: o, className: "tlTypeIcon", alt: "" }) : null, d = /* @__PURE__ */ e.createElement(e.Fragment, null, c, i && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, i)), m = e.useCallback((b) => {
    b.preventDefault(), n("goto", {});
  }, [n]), f = ["tlResourceCell", s].filter(Boolean).join(" "), h = u ? "key:tooltip" : void 0;
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
}, Yr = 20, Gr = () => {
  var C;
  const l = X(), t = le(), n = l.nodes ?? [], a = l.selectionMode ?? "single", o = l.dragEnabled ?? !1, i = l.dropEnabled ?? !1, s = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [r, c] = e.useState(-1), d = e.useRef(null), m = ((C = n.find((N) => N.selected)) == null ? void 0 : C.id) ?? null;
  e.useEffect(() => {
    var x;
    if (m == null)
      return;
    const N = (x = d.current) == null ? void 0 : x.querySelector(".tlTreeView__node--selected");
    N && N.scrollIntoView({ block: "nearest" });
  }, [m]);
  const f = e.useCallback((N, x) => {
    t(x ? "collapse" : "expand", { nodeId: N });
  }, [t]), h = e.useCallback((N, x) => {
    var F;
    const S = window.getSelection();
    S && !S.isCollapsed && x.currentTarget.contains(S.anchorNode) || ((F = d.current) == null || F.focus({ preventScroll: !0 }), t("select", {
      nodeId: N,
      ctrlKey: x.ctrlKey || x.metaKey,
      shiftKey: x.shiftKey
    }));
  }, [t]), b = e.useCallback((N) => {
    t("activate", { nodeId: N });
  }, [t]), D = e.useCallback((N, x) => {
    x.preventDefault(), t("contextMenu", { nodeId: N, x: x.clientX, y: x.clientY });
  }, [t]), _ = e.useRef(null), E = e.useCallback((N, x) => {
    const S = x.getBoundingClientRect(), F = N.clientY - S.top, H = S.height / 3;
    return F < H ? "above" : F > H * 2 ? "below" : "within";
  }, []), w = e.useCallback((N, x) => {
    x.dataTransfer.effectAllowed = "move", x.dataTransfer.setData("text/plain", N);
  }, []), k = e.useCallback((N, x) => {
    x.preventDefault(), x.dataTransfer.dropEffect = "move";
    const S = E(x, x.currentTarget);
    _.current != null && window.clearTimeout(_.current), _.current = window.setTimeout(() => {
      t("dragOver", { nodeId: N, position: S }), _.current = null;
    }, 50);
  }, [t, E]), I = e.useCallback((N, x) => {
    x.preventDefault(), _.current != null && (window.clearTimeout(_.current), _.current = null);
    const S = E(x, x.currentTarget);
    t("drop", { nodeId: N, position: S });
  }, [t, E]), y = e.useCallback(() => {
    _.current != null && (window.clearTimeout(_.current), _.current = null), t("dragEnd");
  }, [t]), v = e.useCallback((N) => {
    if (n.length === 0) return;
    let x = r;
    switch (N.key) {
      case "ArrowDown":
        N.preventDefault(), x = Math.min(r + 1, n.length - 1);
        break;
      case "ArrowUp":
        N.preventDefault(), x = Math.max(r - 1, 0);
        break;
      case "ArrowRight":
        if (N.preventDefault(), r >= 0 && r < n.length) {
          const S = n[r];
          if (S.expandable && !S.expanded) {
            t("expand", { nodeId: S.id });
            return;
          } else S.expanded && (x = r + 1);
        }
        break;
      case "ArrowLeft":
        if (N.preventDefault(), r >= 0 && r < n.length) {
          const S = n[r];
          if (S.expanded) {
            t("collapse", { nodeId: S.id });
            return;
          } else {
            const F = S.depth;
            for (let H = r - 1; H >= 0; H--)
              if (n[H].depth < F) {
                x = H;
                break;
              }
          }
        }
        break;
      case "Enter":
        N.preventDefault(), r >= 0 && r < n.length && (N.ctrlKey || N.metaKey || N.shiftKey ? t("select", {
          nodeId: n[r].id,
          ctrlKey: N.ctrlKey || N.metaKey,
          shiftKey: N.shiftKey
        }) : b(n[r].id));
        return;
      case " ":
        N.preventDefault(), a === "multi" && r >= 0 && r < n.length && t("select", {
          nodeId: n[r].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        N.preventDefault(), x = 0;
        break;
      case "End":
        N.preventDefault(), x = n.length - 1;
        break;
      default:
        return;
    }
    x !== r && c(x);
  }, [r, n, t, a, b]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: d,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: v
    },
    n.map((N, x) => /* @__PURE__ */ e.createElement(
      "li",
      {
        key: N.id,
        role: "treeitem",
        "aria-expanded": N.expandable ? N.expanded : void 0,
        "aria-selected": N.selected,
        "aria-level": N.depth + 1,
        className: [
          "tlTreeView__node",
          N.selected ? "tlTreeView__node--selected" : "",
          x === r ? "tlTreeView__node--focused" : "",
          s === N.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          s === N.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          s === N.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: N.depth * Yr },
        draggable: o,
        onMouseDown: (S) => {
          (S.shiftKey || S.ctrlKey || S.metaKey || S.detail > 1) && S.preventDefault();
        },
        onClick: (S) => h(N.id, S),
        onDoubleClick: () => b(N.id),
        onContextMenu: (S) => D(N.id, S),
        onDragStart: (S) => w(N.id, S),
        onDragOver: i ? (S) => k(N.id, S) : void 0,
        onDrop: i ? (S) => I(N.id, S) : void 0,
        onDragEnd: y
      },
      N.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (S) => {
            S.stopPropagation(), f(N.id, N.expanded);
          },
          tabIndex: -1,
          "aria-label": N.expanded ? "Collapse" : "Expand"
        },
        N.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: N.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(G, { control: N.content }))
    ))
  );
};
var Mt = { exports: {} }, he = {}, It = { exports: {} }, ee = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var fn;
function Xr() {
  if (fn) return ee;
  fn = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), a = Symbol.for("react.strict_mode"), o = Symbol.for("react.profiler"), i = Symbol.for("react.consumer"), s = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), r = Symbol.for("react.suspense"), c = Symbol.for("react.memo"), d = Symbol.for("react.lazy"), m = Symbol.for("react.activity"), f = Symbol.iterator;
  function h(p) {
    return p === null || typeof p != "object" ? null : (p = f && p[f] || p["@@iterator"], typeof p == "function" ? p : null);
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
  }, D = Object.assign, _ = {};
  function E(p, M, Y) {
    this.props = p, this.context = M, this.refs = _, this.updater = Y || b;
  }
  E.prototype.isReactComponent = {}, E.prototype.setState = function(p, M) {
    if (typeof p != "object" && typeof p != "function" && p != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, p, M, "setState");
  }, E.prototype.forceUpdate = function(p) {
    this.updater.enqueueForceUpdate(this, p, "forceUpdate");
  };
  function w() {
  }
  w.prototype = E.prototype;
  function k(p, M, Y) {
    this.props = p, this.context = M, this.refs = _, this.updater = Y || b;
  }
  var I = k.prototype = new w();
  I.constructor = k, D(I, E.prototype), I.isPureReactComponent = !0;
  var y = Array.isArray;
  function v() {
  }
  var C = { H: null, A: null, T: null, S: null }, N = Object.prototype.hasOwnProperty;
  function x(p, M, Y) {
    var V = Y.ref;
    return {
      $$typeof: l,
      type: p,
      key: M,
      ref: V !== void 0 ? V : null,
      props: Y
    };
  }
  function S(p, M) {
    return x(p.type, M, p.props);
  }
  function F(p) {
    return typeof p == "object" && p !== null && p.$$typeof === l;
  }
  function H(p) {
    var M = { "=": "=0", ":": "=2" };
    return "$" + p.replace(/[=:]/g, function(Y) {
      return M[Y];
    });
  }
  var B = /\/+/g;
  function K(p, M) {
    return typeof p == "object" && p !== null && p.key != null ? H("" + p.key) : M.toString(36);
  }
  function A(p) {
    switch (p.status) {
      case "fulfilled":
        return p.value;
      case "rejected":
        throw p.reason;
      default:
        switch (typeof p.status == "string" ? p.then(v, v) : (p.status = "pending", p.then(
          function(M) {
            p.status === "pending" && (p.status = "fulfilled", p.value = M);
          },
          function(M) {
            p.status === "pending" && (p.status = "rejected", p.reason = M);
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
  function j(p, M, Y, V, Q) {
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
              return J = p._init, j(
                J(p._payload),
                M,
                Y,
                V,
                Q
              );
          }
      }
    if (J)
      return Q = Q(p), J = V === "" ? "." + K(p, 0) : V, y(Q) ? (Y = "", J != null && (Y = J.replace(B, "$&/") + "/"), j(Q, M, Y, "", function(Ee) {
        return Ee;
      })) : Q != null && (F(Q) && (Q = S(
        Q,
        Y + (Q.key == null || p && p.key === Q.key ? "" : ("" + Q.key).replace(
          B,
          "$&/"
        ) + "/") + J
      )), M.push(Q)), 1;
    J = 0;
    var se = V === "" ? "." : V + ":";
    if (y(p))
      for (var ne = 0; ne < p.length; ne++)
        V = p[ne], $ = se + K(V, ne), J += j(
          V,
          M,
          Y,
          $,
          Q
        );
    else if (ne = h(p), typeof ne == "function")
      for (p = ne.call(p), ne = 0; !(V = p.next()).done; )
        V = V.value, $ = se + K(V, ne++), J += j(
          V,
          M,
          Y,
          $,
          Q
        );
    else if ($ === "object") {
      if (typeof p.then == "function")
        return j(
          A(p),
          M,
          Y,
          V,
          Q
        );
      throw M = String(p), Error(
        "Objects are not valid as a React child (found: " + (M === "[object Object]" ? "object with keys {" + Object.keys(p).join(", ") + "}" : M) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return J;
  }
  function P(p, M, Y) {
    if (p == null) return p;
    var V = [], Q = 0;
    return j(p, V, "", "", function($) {
      return M.call(Y, $, Q++);
    }), V;
  }
  function L(p) {
    if (p._status === -1) {
      var M = p._result;
      M = M(), M.then(
        function(Y) {
          (p._status === 0 || p._status === -1) && (p._status = 1, p._result = Y);
        },
        function(Y) {
          (p._status === 0 || p._status === -1) && (p._status = 2, p._result = Y);
        }
      ), p._status === -1 && (p._status = 0, p._result = M);
    }
    if (p._status === 1) return p._result.default;
    throw p._result;
  }
  var R = typeof reportError == "function" ? reportError : function(p) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var M = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof p == "object" && p !== null && typeof p.message == "string" ? String(p.message) : String(p),
        error: p
      });
      if (!window.dispatchEvent(M)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", p);
      return;
    }
    console.error(p);
  }, W = {
    map: P,
    forEach: function(p, M, Y) {
      P(
        p,
        function() {
          M.apply(this, arguments);
        },
        Y
      );
    },
    count: function(p) {
      var M = 0;
      return P(p, function() {
        M++;
      }), M;
    },
    toArray: function(p) {
      return P(p, function(M) {
        return M;
      }) || [];
    },
    only: function(p) {
      if (!F(p))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return p;
    }
  };
  return ee.Activity = m, ee.Children = W, ee.Component = E, ee.Fragment = n, ee.Profiler = o, ee.PureComponent = k, ee.StrictMode = a, ee.Suspense = r, ee.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = C, ee.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(p) {
      return C.H.useMemoCache(p);
    }
  }, ee.cache = function(p) {
    return function() {
      return p.apply(null, arguments);
    };
  }, ee.cacheSignal = function() {
    return null;
  }, ee.cloneElement = function(p, M, Y) {
    if (p == null)
      throw Error(
        "The argument must be a React element, but you passed " + p + "."
      );
    var V = D({}, p.props), Q = p.key;
    if (M != null)
      for ($ in M.key !== void 0 && (Q = "" + M.key), M)
        !N.call(M, $) || $ === "key" || $ === "__self" || $ === "__source" || $ === "ref" && M.ref === void 0 || (V[$] = M[$]);
    var $ = arguments.length - 2;
    if ($ === 1) V.children = Y;
    else if (1 < $) {
      for (var J = Array($), se = 0; se < $; se++)
        J[se] = arguments[se + 2];
      V.children = J;
    }
    return x(p.type, Q, V);
  }, ee.createContext = function(p) {
    return p = {
      $$typeof: s,
      _currentValue: p,
      _currentValue2: p,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, p.Provider = p, p.Consumer = {
      $$typeof: i,
      _context: p
    }, p;
  }, ee.createElement = function(p, M, Y) {
    var V, Q = {}, $ = null;
    if (M != null)
      for (V in M.key !== void 0 && ($ = "" + M.key), M)
        N.call(M, V) && V !== "key" && V !== "__self" && V !== "__source" && (Q[V] = M[V]);
    var J = arguments.length - 2;
    if (J === 1) Q.children = Y;
    else if (1 < J) {
      for (var se = Array(J), ne = 0; ne < J; ne++)
        se[ne] = arguments[ne + 2];
      Q.children = se;
    }
    if (p && p.defaultProps)
      for (V in J = p.defaultProps, J)
        Q[V] === void 0 && (Q[V] = J[V]);
    return x(p, $, Q);
  }, ee.createRef = function() {
    return { current: null };
  }, ee.forwardRef = function(p) {
    return { $$typeof: u, render: p };
  }, ee.isValidElement = F, ee.lazy = function(p) {
    return {
      $$typeof: d,
      _payload: { _status: -1, _result: p },
      _init: L
    };
  }, ee.memo = function(p, M) {
    return {
      $$typeof: c,
      type: p,
      compare: M === void 0 ? null : M
    };
  }, ee.startTransition = function(p) {
    var M = C.T, Y = {};
    C.T = Y;
    try {
      var V = p(), Q = C.S;
      Q !== null && Q(Y, V), typeof V == "object" && V !== null && typeof V.then == "function" && V.then(v, R);
    } catch ($) {
      R($);
    } finally {
      M !== null && Y.types !== null && (M.types = Y.types), C.T = M;
    }
  }, ee.unstable_useCacheRefresh = function() {
    return C.H.useCacheRefresh();
  }, ee.use = function(p) {
    return C.H.use(p);
  }, ee.useActionState = function(p, M, Y) {
    return C.H.useActionState(p, M, Y);
  }, ee.useCallback = function(p, M) {
    return C.H.useCallback(p, M);
  }, ee.useContext = function(p) {
    return C.H.useContext(p);
  }, ee.useDebugValue = function() {
  }, ee.useDeferredValue = function(p, M) {
    return C.H.useDeferredValue(p, M);
  }, ee.useEffect = function(p, M) {
    return C.H.useEffect(p, M);
  }, ee.useEffectEvent = function(p) {
    return C.H.useEffectEvent(p);
  }, ee.useId = function() {
    return C.H.useId();
  }, ee.useImperativeHandle = function(p, M, Y) {
    return C.H.useImperativeHandle(p, M, Y);
  }, ee.useInsertionEffect = function(p, M) {
    return C.H.useInsertionEffect(p, M);
  }, ee.useLayoutEffect = function(p, M) {
    return C.H.useLayoutEffect(p, M);
  }, ee.useMemo = function(p, M) {
    return C.H.useMemo(p, M);
  }, ee.useOptimistic = function(p, M) {
    return C.H.useOptimistic(p, M);
  }, ee.useReducer = function(p, M, Y) {
    return C.H.useReducer(p, M, Y);
  }, ee.useRef = function(p) {
    return C.H.useRef(p);
  }, ee.useState = function(p) {
    return C.H.useState(p);
  }, ee.useSyncExternalStore = function(p, M, Y) {
    return C.H.useSyncExternalStore(
      p,
      M,
      Y
    );
  }, ee.useTransition = function() {
    return C.H.useTransition();
  }, ee.version = "19.2.4", ee;
}
var hn;
function qr() {
  return hn || (hn = 1, It.exports = Xr()), It.exports;
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
var bn;
function Zr() {
  if (bn) return he;
  bn = 1;
  var l = qr();
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
  function i(r, c, d) {
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
  function u(r, c) {
    if (r === "font") return "";
    if (typeof c == "string")
      return c === "use-credentials" ? c : "";
  }
  return he.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = a, he.createPortal = function(r, c) {
    var d = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!c || c.nodeType !== 1 && c.nodeType !== 9 && c.nodeType !== 11)
      throw Error(t(299));
    return i(r, c, null, d);
  }, he.flushSync = function(r) {
    var c = s.T, d = a.p;
    try {
      if (s.T = null, a.p = 2, r) return r();
    } finally {
      s.T = c, a.p = d, a.d.f();
    }
  }, he.preconnect = function(r, c) {
    typeof r == "string" && (c ? (c = c.crossOrigin, c = typeof c == "string" ? c === "use-credentials" ? c : "" : void 0) : c = null, a.d.C(r, c));
  }, he.prefetchDNS = function(r) {
    typeof r == "string" && a.d.D(r);
  }, he.preinit = function(r, c) {
    if (typeof r == "string" && c && typeof c.as == "string") {
      var d = c.as, m = u(d, c.crossOrigin), f = typeof c.integrity == "string" ? c.integrity : void 0, h = typeof c.fetchPriority == "string" ? c.fetchPriority : void 0;
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
  }, he.preinitModule = function(r, c) {
    if (typeof r == "string")
      if (typeof c == "object" && c !== null) {
        if (c.as == null || c.as === "script") {
          var d = u(
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
  }, he.preload = function(r, c) {
    if (typeof r == "string" && typeof c == "object" && c !== null && typeof c.as == "string") {
      var d = c.as, m = u(d, c.crossOrigin);
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
  }, he.preloadModule = function(r, c) {
    if (typeof r == "string")
      if (c) {
        var d = u(c.as, c.crossOrigin);
        a.d.m(r, {
          as: typeof c.as == "string" && c.as !== "script" ? c.as : void 0,
          crossOrigin: d,
          integrity: typeof c.integrity == "string" ? c.integrity : void 0
        });
      } else a.d.m(r);
  }, he.requestFormReset = function(r) {
    a.d.r(r);
  }, he.unstable_batchedUpdates = function(r, c) {
    return r(c);
  }, he.useFormState = function(r, c, d) {
    return s.H.useFormState(r, c, d);
  }, he.useFormStatus = function() {
    return s.H.useHostTransitionStatus();
  }, he.version = "19.2.4", he;
}
var gn;
function Qr() {
  if (gn) return Mt.exports;
  gn = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), Mt.exports = Zr(), Mt.exports;
}
var Tn = Qr();
const { useState: Me, useCallback: ge, useRef: et, useEffect: We, useMemo: Vt } = e;
function Gt({ image: l }) {
  return l ? l.startsWith("/") ? /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" }) : /* @__PURE__ */ e.createElement(Ne, { encoded: l, className: "tlDropdownSelect__optionIcon" }) : null;
}
function Jr({
  option: l,
  removable: t,
  onRemove: n,
  removeLabel: a,
  draggable: o,
  onDragStart: i,
  onDragOver: s,
  onDrop: u,
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
      onDragStart: i,
      onDragOver: s,
      onDrop: u,
      onDragEnd: r
    },
    o && /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__dragHandle", "aria-hidden": "true" }, "⋮⋮"),
    /* @__PURE__ */ e.createElement(Gt, { image: l.image }),
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
function eo({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: a,
  onMouseEnter: o,
  id: i
}) {
  const s = ge(() => a(l.value), [a, l.value]), u = Vt(() => {
    if (!n) return l.label;
    const r = l.label.toLowerCase().indexOf(n.toLowerCase());
    return r < 0 ? l.label : /* @__PURE__ */ e.createElement(e.Fragment, null, l.label.substring(0, r), /* @__PURE__ */ e.createElement("strong", null, l.label.substring(r, r + n.length)), l.label.substring(r + n.length));
  }, [l.label, n]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: i,
      role: "option",
      "aria-selected": t,
      className: "tlDropdownSelect__option" + (t ? " tlDropdownSelect__option--highlighted" : ""),
      onClick: s,
      onMouseEnter: o
    },
    /* @__PURE__ */ e.createElement(Gt, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const to = ({ controlId: l, state: t }) => {
  const n = le(), a = t.value ?? [], o = t.multiSelect === !0, i = t.customOrder === !0, s = t.mandatory === !0, u = t.disabled === !0, r = t.editable !== !1, c = t.optionsLoaded === !0, d = t.options ?? [], m = t.emptyOptionLabel ?? "", f = i && o && !u && r, h = ue({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), b = h["js.dropdownSelect.nothingFound"], D = ge(
    (O) => h["js.dropdownSelect.removeChip"].replace("{0}", O),
    [h]
  ), [_, E] = Me(!1), [w, k] = Me(""), [I, y] = Me(-1), [v, C] = Me(!1), [N, x] = Me({}), [S, F] = Me(null), [H, B] = Me(null), [K, A] = Me(null), j = et(null), P = et(null), L = et(null), R = et(a);
  R.current = a;
  const W = et(-1), p = Vt(
    () => new Set(a.map((O) => O.value)),
    [a]
  ), M = Vt(() => {
    let O = d.filter((q) => !p.has(q.value));
    if (w) {
      const q = w.toLowerCase();
      O = O.filter((ae) => ae.label.toLowerCase().includes(q));
    }
    return O;
  }, [d, p, w]);
  We(() => {
    w && M.length === 1 ? y(0) : y(-1);
  }, [M.length, w]), We(() => {
    _ && c && P.current && P.current.focus();
  }, [_, c, a]), We(() => {
    var ae, ie;
    if (W.current < 0) return;
    const O = W.current;
    W.current = -1;
    const q = (ae = j.current) == null ? void 0 : ae.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    q && q.length > 0 ? q[Math.min(O, q.length - 1)].focus() : (ie = j.current) == null || ie.focus();
  }, [a]), We(() => {
    if (!_) return;
    const O = (q) => {
      j.current && !j.current.contains(q.target) && L.current && !L.current.contains(q.target) && (E(!1), k(""));
    };
    return document.addEventListener("mousedown", O), () => document.removeEventListener("mousedown", O);
  }, [_]), We(() => {
    if (!_ || !j.current) return;
    const O = j.current.getBoundingClientRect(), q = window.innerHeight - O.bottom, ie = q < 300 && O.top > q;
    x({
      left: O.left,
      width: O.width,
      ...ie ? { bottom: window.innerHeight - O.top } : { top: O.bottom }
    });
  }, [_]);
  const Y = ge(async () => {
    if (!(u || !r) && (E(!0), k(""), y(-1), C(!1), !c))
      try {
        await n("loadOptions");
      } catch {
        C(!0);
      }
  }, [u, r, c, n]), V = ge(() => {
    var O;
    E(!1), k(""), y(-1), (O = j.current) == null || O.focus();
  }, []), Q = ge(
    (O) => {
      let q;
      if (o) {
        const ae = d.find((ie) => ie.value === O);
        if (ae)
          q = [...R.current, ae];
        else
          return;
      } else {
        const ae = d.find((ie) => ie.value === O);
        if (ae)
          q = [ae];
        else
          return;
      }
      R.current = q, n(ct, { value: q.map((ae) => ae.value) }), o ? (k(""), y(-1)) : V();
    },
    [o, d, n, V]
  ), $ = ge(
    (O) => {
      W.current = R.current.findIndex((ae) => ae.value === O);
      const q = R.current.filter((ae) => ae.value !== O);
      R.current = q, n(ct, { value: q.map((ae) => ae.value) });
    },
    [n]
  ), J = ge(
    (O) => {
      O.stopPropagation(), n(ct, { value: [] }), V();
    },
    [n, V]
  ), se = ge((O) => {
    k(O.target.value);
  }, []), ne = ge(
    (O) => {
      if (!_) {
        if (O.key === "ArrowDown" || O.key === "ArrowUp" || O.key === "Enter" || O.key === " ") {
          if (O.target.tagName === "BUTTON") return;
          O.preventDefault(), O.stopPropagation(), Y();
        }
        return;
      }
      switch (O.key) {
        case "ArrowDown":
          O.preventDefault(), O.stopPropagation(), y(
            (q) => q < M.length - 1 ? q + 1 : 0
          );
          break;
        case "ArrowUp":
          O.preventDefault(), O.stopPropagation(), y(
            (q) => q > 0 ? q - 1 : M.length - 1
          );
          break;
        case "Enter":
          O.preventDefault(), O.stopPropagation(), I >= 0 && I < M.length && Q(M[I].value);
          break;
        case "Escape":
          O.preventDefault(), O.stopPropagation(), V();
          break;
        case "Tab":
          V();
          break;
        case "Backspace":
          w === "" && o && a.length > 0 && $(a[a.length - 1].value);
          break;
      }
    },
    [
      _,
      Y,
      V,
      M,
      I,
      Q,
      w,
      o,
      a,
      $
    ]
  ), Ee = ge(
    async (O) => {
      O.preventDefault(), C(!1);
      try {
        await n("loadOptions");
      } catch {
        C(!0);
      }
    },
    [n]
  ), be = ge(
    (O, q) => {
      F(O), q.dataTransfer.effectAllowed = "move", q.dataTransfer.setData("text/plain", String(O));
    },
    []
  ), we = ge(
    (O, q) => {
      if (q.preventDefault(), q.dataTransfer.dropEffect = "move", S === null || S === O) {
        B(null), A(null);
        return;
      }
      const ae = q.currentTarget.getBoundingClientRect(), ie = ae.left + ae.width / 2, Xe = q.clientX < ie ? "before" : "after";
      B(O), A(Xe);
    },
    [S]
  ), Te = ge(
    (O) => {
      if (O.preventDefault(), S === null || H === null || K === null || S === H) return;
      const q = [...R.current], [ae] = q.splice(S, 1);
      let ie = H;
      S < H ? ie = K === "before" ? ie - 1 : ie : ie = K === "before" ? ie : ie + 1, q.splice(ie, 0, ae), R.current = q, n(ct, { value: q.map((Xe) => Xe.value) }), F(null), B(null), A(null);
    },
    [S, H, K, n]
  ), Re = ge(() => {
    F(null), B(null), A(null);
  }, []);
  if (We(() => {
    if (I < 0 || !L.current) return;
    const O = L.current.querySelector(
      `[id="${l}-opt-${I}"]`
    );
    O && O.scrollIntoView({ block: "nearest" });
  }, [I, l]), !r)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, a.map((O) => /* @__PURE__ */ e.createElement("span", { key: O.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(Gt, { image: O.image }), /* @__PURE__ */ e.createElement("span", null, O.label))));
  const $e = !s && a.length > 0 && !u, ot = _ ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: L,
      className: "tlDropdownSelect__dropdown",
      style: N,
      ...rl
    },
    (c || v) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: P,
        type: "text",
        className: "tlDropdownSelect__search",
        value: w,
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
      c && M.map((O, q) => /* @__PURE__ */ e.createElement(
        eo,
        {
          key: O.value,
          id: `${l}-opt-${q}`,
          option: O,
          highlighted: q === I,
          searchTerm: w,
          onSelect: Q,
          onMouseEnter: () => y(q)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: j,
      className: "tlDropdownSelect" + (_ ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": _,
      "aria-haspopup": "listbox",
      "aria-owns": _ ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: _ ? void 0 : Y,
      onKeyDown: ne
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, a.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, m) : a.map((O, q) => {
      let ae = "";
      return S === q ? ae = "tlDropdownSelect__chip--dragging" : H === q && K === "before" ? ae = "tlDropdownSelect__chip--dropBefore" : H === q && K === "after" && (ae = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        Jr,
        {
          key: O.value,
          option: O,
          removable: !u && (o || !s),
          onRemove: $,
          removeLabel: D(O.label),
          draggable: f,
          onDragStart: f ? (ie) => be(q, ie) : void 0,
          onDragOver: f ? (ie) => we(q, ie) : void 0,
          onDrop: f ? Te : void 0,
          onDragEnd: f ? Re : void 0,
          dragClassName: f ? ae : void 0
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
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, _ ? "▲" : "▼"))
  ), ot && Tn.createPortal(ot, document.body));
}, { useCallback: jt, useRef: no } = e, Rn = "application/x-tl-color", lo = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: a,
  onSwap: o,
  onReplace: i
}) => {
  const s = no(null), u = jt(
    (d) => (m) => {
      s.current = d, m.dataTransfer.effectAllowed = "move";
    },
    []
  ), r = jt((d) => {
    d.preventDefault(), d.dataTransfer.dropEffect = "move";
  }, []), c = jt(
    (d) => (m) => {
      m.preventDefault();
      const f = m.dataTransfer.getData(Rn);
      f ? i(d, f) : s.current !== null && s.current !== d && o(s.current, d), s.current = null;
    },
    [o, i]
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
        onDrop: c(m)
      }
    ))
  );
};
function Ln(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function zt(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function xn(l) {
  if (!zt(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function Mn(l, t, n) {
  const a = (o) => Ln(o).toString(16).padStart(2, "0");
  return "#" + a(l) + a(t) + a(n);
}
function ao(l, t, n) {
  const a = l / 255, o = t / 255, i = n / 255, s = Math.max(a, o, i), u = Math.min(a, o, i), r = s - u;
  let c = 0;
  r !== 0 && (s === a ? c = (o - i) / r % 6 : s === o ? c = (i - a) / r + 2 : c = (a - o) / r + 4, c *= 60, c < 0 && (c += 360));
  const d = s === 0 ? 0 : r / s;
  return [c, d, s];
}
function ro(l, t, n) {
  const a = n * t, o = a * (1 - Math.abs(l / 60 % 2 - 1)), i = n - a;
  let s = 0, u = 0, r = 0;
  return l < 60 ? (s = a, u = o, r = 0) : l < 120 ? (s = o, u = a, r = 0) : l < 180 ? (s = 0, u = a, r = o) : l < 240 ? (s = 0, u = o, r = a) : l < 300 ? (s = o, u = 0, r = a) : (s = a, u = 0, r = o), [
    Math.round((s + i) * 255),
    Math.round((u + i) * 255),
    Math.round((r + i) * 255)
  ];
}
function oo(l) {
  return ao(...xn(l));
}
function Bt(l, t, n) {
  return Mn(...ro(l, t, n));
}
const { useCallback: Ue, useRef: En } = e, so = ({ color: l, onColorChange: t }) => {
  const [n, a, o] = oo(l), i = En(null), s = En(null), u = Ue(
    (b, D) => {
      var k;
      const _ = (k = i.current) == null ? void 0 : k.getBoundingClientRect();
      if (!_) return;
      const E = Math.max(0, Math.min(1, (b - _.left) / _.width)), w = Math.max(0, Math.min(1, 1 - (D - _.top) / _.height));
      t(Bt(n, E, w));
    },
    [n, t]
  ), r = Ue(
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
      var w;
      const D = (w = s.current) == null ? void 0 : w.getBoundingClientRect();
      if (!D) return;
      const E = Math.max(0, Math.min(1, (b - D.top) / D.height)) * 360;
      t(Bt(E, a, o));
    },
    [a, o, t]
  ), m = Ue(
    (b) => {
      b.preventDefault(), b.target.setPointerCapture(b.pointerId), d(b.clientY);
    },
    [d]
  ), f = Ue(
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
function co(l, t) {
  const n = t.toUpperCase();
  return l.some((a) => a != null && a.toUpperCase() === n);
}
const io = {
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
}, { useState: pt, useCallback: ke, useEffect: vn, useRef: uo, useLayoutEffect: mo } = e, po = ({
  anchorRef: l,
  currentColor: t,
  palette: n,
  paletteColumns: a,
  defaultPalette: o,
  canReset: i,
  onConfirm: s,
  onCancel: u,
  onPaletteChange: r
}) => {
  const [c, d] = pt("palette"), [m, f] = pt(t), h = uo(null), b = ue(io), [D, _] = pt(null);
  mo(() => {
    if (!l.current || !h.current) return;
    const L = l.current.getBoundingClientRect(), R = h.current.getBoundingClientRect();
    let W = L.bottom + 4, p = L.left;
    W + R.height > window.innerHeight && (W = L.top - R.height - 4), p + R.width > window.innerWidth && (p = Math.max(0, L.right - R.width)), _({ top: W, left: p });
  }, [l]);
  const E = m != null, [w, k, I] = E ? xn(m) : [0, 0, 0], [y, v] = pt((m == null ? void 0 : m.toUpperCase()) ?? "");
  vn(() => {
    v((m == null ? void 0 : m.toUpperCase()) ?? "");
  }, [m]), Oe(!0, { ESCAPE: u }), vn(() => {
    const L = (W) => {
      h.current && !h.current.contains(W.target) && u();
    }, R = setTimeout(() => document.addEventListener("mousedown", L), 0);
    return () => {
      clearTimeout(R), document.removeEventListener("mousedown", L);
    };
  }, [u]);
  const C = ke(
    (L) => (R) => {
      const W = parseInt(R.target.value, 10);
      if (isNaN(W)) return;
      const p = Ln(W);
      f(Mn(L === "r" ? p : w, L === "g" ? p : k, L === "b" ? p : I));
    },
    [w, k, I]
  ), N = ke(
    (L) => {
      if (m != null) {
        L.dataTransfer.setData(Rn, m.toUpperCase()), L.dataTransfer.effectAllowed = "move";
        const R = document.createElement("div");
        R.style.width = "33px", R.style.height = "33px", R.style.backgroundColor = m, R.style.borderRadius = "3px", R.style.border = "1px solid rgba(0,0,0,0.1)", R.style.position = "absolute", R.style.top = "-9999px", document.body.appendChild(R), L.dataTransfer.setDragImage(R, 16, 16), requestAnimationFrame(() => document.body.removeChild(R));
      }
    },
    [m]
  ), x = ke((L) => {
    const R = L.target.value;
    v(R), zt(R) && f(R);
  }, []), S = ke(() => {
    f(null);
  }, []), F = ke((L) => {
    f(L);
  }, []), H = ke(
    (L) => {
      s(L);
    },
    [s]
  ), B = ke(
    (L, R) => {
      const W = [...n], p = W[L];
      W[L] = W[R], W[R] = p, r(W);
    },
    [n, r]
  ), K = ke(
    (L, R) => {
      const W = [...n];
      W[L] = R, r(W);
    },
    [n, r]
  ), A = ke(() => {
    r([...o]);
  }, [o, r]), j = ke(
    (L) => {
      if (co(n, L)) return;
      const R = n.indexOf(null);
      if (R < 0) return;
      const W = [...n];
      W[R] = L.toUpperCase(), r(W);
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
      lo,
      {
        colors: n,
        columns: a,
        onSelect: F,
        onConfirm: H,
        onSwap: B,
        onReplace: K
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: A }, b["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(so, { color: m ?? "#000000", onColorChange: f }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
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
        onDragStart: E ? N : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: E ? w : "",
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
        className: "tlColorInput__input" + (y !== "" && !zt(y) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: y,
        onChange: x
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, i && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: S }, b["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, b["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: P }, b["js.colorInput.ok"]))
  );
}, fo = { "js.colorInput.chooseColor": "Choose color" }, { useState: ho, useCallback: ft, useRef: bo } = e, go = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = le(), i = ue(fo), [s, u] = ho(!1), r = bo(null), c = n, d = t.editable !== !1, m = t.palette ?? [], f = t.paletteColumns ?? 6, h = t.defaultPalette ?? m, b = ft(() => {
    d && u(!0);
  }, [d]), D = ft(
    (w) => {
      u(!1), a(w);
    },
    [a]
  ), _ = ft(() => {
    u(!1);
  }, []), E = ft(
    (w) => {
      o("paletteChanged", { palette: w });
    },
    [o]
  );
  return d ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: r,
      className: "tlColorInput__swatch" + (c == null ? " tlColorInput__swatch--noColor" : ""),
      style: c != null ? { backgroundColor: c } : void 0,
      onClick: b,
      disabled: t.disabled === !0,
      title: c ?? "",
      "aria-label": i["js.colorInput.chooseColor"]
    }
  ), s && /* @__PURE__ */ e.createElement(
    po,
    {
      anchorRef: r,
      currentColor: c,
      palette: m,
      paletteColumns: f,
      defaultPalette: h,
      canReset: t.canReset !== !1,
      onConfirm: D,
      onCancel: _,
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
}, { useState: tt, useCallback: Pe, useEffect: Pt, useRef: _n, useLayoutEffect: Eo, useMemo: vo } = e, _o = {
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
}, Co = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: a,
  onSelect: o,
  onCancel: i,
  onLoadIcons: s
}) => {
  const u = ue(_o), [r, c] = tt("simple"), [d, m] = tt(""), [f, h] = tt(t ?? ""), [b, D] = tt(!1), [_, E] = tt(null), w = _n(null), k = _n(null);
  Eo(() => {
    if (!l.current || !w.current) return;
    const H = l.current.getBoundingClientRect(), B = w.current.getBoundingClientRect();
    let K = H.bottom + 4, A = H.left;
    K + B.height > window.innerHeight && (K = H.top - B.height - 4), A + B.width > window.innerWidth && (A = Math.max(0, H.right - B.width)), E({ top: K, left: A });
  }, [l]), Pt(() => {
    !a && !b && s().catch(() => D(!0));
  }, [a, b, s]), Pt(() => {
    a && k.current && k.current.focus();
  }, [a]), Oe(!0, { ESCAPE: i }), Pt(() => {
    const H = (K) => {
      w.current && !w.current.contains(K.target) && i();
    }, B = setTimeout(() => document.addEventListener("mousedown", H), 0);
    return () => {
      clearTimeout(B), document.removeEventListener("mousedown", H);
    };
  }, [i]);
  const I = vo(() => {
    if (!d) return n;
    const H = d.toLowerCase();
    return n.filter(
      (B) => B.prefix.toLowerCase().includes(H) || B.label.toLowerCase().includes(H) || B.terms != null && B.terms.some((K) => K.includes(H))
    );
  }, [n, d]), y = Pe((H) => {
    m(H.target.value);
  }, []), v = Pe(
    (H) => {
      o(H);
    },
    [o]
  ), C = Pe((H) => {
    h(H);
  }, []), N = Pe((H) => {
    h(H.target.value);
  }, []), x = Pe(() => {
    o(f || null);
  }, [f, o]), S = Pe(() => {
    o(null);
  }, [o]), F = Pe(async (H) => {
    H.preventDefault(), D(!1);
    try {
      await s();
    } catch {
      D(!0);
    }
  }, [s]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlIconSelect__popup",
      ref: w,
      style: _ ? { top: _.top, left: _.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (r === "simple" ? " tlIconSelect__tab--active" : ""),
        onClick: () => c("simple")
      },
      u["js.iconSelect.simpleTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (r === "advanced" ? " tlIconSelect__tab--active" : ""),
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
        onChange: y,
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
      b && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: F }, u["js.iconSelect.loadError"])),
      a && I.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      a && I.map(
        (H) => H.variants.map((B) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: B.encoded,
            className: "tlIconSelect__iconCell" + (B.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": B.encoded === t,
            tabIndex: 0,
            title: H.label,
            onClick: () => r === "simple" ? v(B.encoded) : C(B.encoded),
            onKeyDown: (K) => {
              (K.key === "Enter" || K.key === " ") && (K.preventDefault(), r === "simple" ? v(B.encoded) : C(B.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(Ne, { encoded: B.encoded })
        ))
      )
    ),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: f,
        onChange: N
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, f && /* @__PURE__ */ e.createElement(Ne, { encoded: f })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, f ? f.startsWith("css:") ? f.substring(4) : f : ""))),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: i }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: S }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: x }, u["js.iconSelect.ok"]))
  );
}, yo = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: wo, useCallback: ht, useRef: ko } = e, No = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = le(), i = ue(yo), [s, u] = wo(!1), r = ko(null), c = n, d = t.editable !== !1, m = t.disabled === !0, f = t.icons ?? [], h = t.iconsLoaded === !0, b = ht(() => {
    d && !m && u(!0);
  }, [d, m]), D = ht(
    (w) => {
      u(!1), a(w);
    },
    [a]
  ), _ = ht(() => {
    u(!1);
  }, []), E = ht(async () => {
    await o("loadIcons");
  }, [o]);
  return d ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: r,
      className: "tlIconSelect__swatch" + (c == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: b,
      disabled: m,
      title: c ?? "",
      "aria-label": i["js.iconSelect.chooseIcon"]
    },
    c ? /* @__PURE__ */ e.createElement(Ne, { encoded: c }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), s && /* @__PURE__ */ e.createElement(
    Co,
    {
      anchorRef: r,
      currentValue: c,
      icons: f,
      iconsLoaded: h,
      onSelect: D,
      onCancel: _,
      onLoadIcons: E
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, c ? /* @__PURE__ */ e.createElement(Ne, { encoded: c }) : null));
}, { useCallback: Ve, useEffect: So, useMemo: Cn, useRef: Do, useState: At } = e, To = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, Ro = [1, 2, 3, 4];
function Lo(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const a = parseFloat(n[1]), o = n[2] || "px";
  return o === "rem" || o === "em" ? a * t : a;
}
function xo(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let a = 1;
  for (const o of Ro)
    n >= o && (a = o);
  return a;
}
function Mo(l, t) {
  const n = To[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function Io(l, t) {
  const n = Math.max(1, t), a = {}, o = (m, f) => !!(a[m] && a[m][f]), i = (m, f) => {
    a[m] || (a[m] = {}), a[m][f] = !0;
  }, s = [];
  let u = 0, r = 0;
  const c = (m) => {
    let f = null;
    for (const b of s) b.rowStart === m && (f = b);
    if (!f) return;
    let h = f.colEnd;
    for (; h < n && !o(m, h); ) h++;
    if (h !== f.colEnd) {
      for (let b = f.rowStart; b < f.rowEnd; b++)
        for (let D = f.colEnd; D < h; D++) i(b, D);
      f.colEnd = h;
    }
  };
  for (const m of l) {
    const f = n <= 1 ? 1 : Math.max(1, m.rowSpan || 1);
    let h = Math.min(Mo(m.width, n), n);
    for (; o(u, r); )
      r++, r >= n && (r = 0, u++);
    let b = 0;
    for (let k = r; k < n && !o(u, k); k++)
      b++;
    if (h > b) {
      for (c(u), r = 0, u++; o(u, r); )
        r++, r >= n && (r = 0, u++);
      b = 0;
      for (let k = r; k < n && !o(u, k); k++)
        b++;
      h = Math.min(h, b);
    }
    const D = r, _ = r + h, E = u, w = u + f;
    s.push({ id: m.id, colStart: D, colEnd: _, rowStart: E, rowEnd: w });
    for (let k = E; k < w; k++)
      for (let I = D; I < _; I++) i(k, I);
    r = _, r >= n && (r = 0, u++);
  }
  c(u);
  let d = 0;
  for (const m of s) m.rowEnd > d && (d = m.rowEnd);
  for (let m = 1; m < d; m++)
    for (let f = 0; f < n; f++) {
      if (o(m, f)) continue;
      const h = s.find((b) => b.rowEnd === m && b.colStart <= f && f < b.colEnd);
      if (h) {
        h.rowEnd = m + 1;
        for (let b = h.colStart; b < h.colEnd; b++) i(m, b);
      }
    }
  return s;
}
const jo = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.minColWidth ?? "16rem", o = (t.children ?? []).filter((v) => v && v.id), i = Do(null), [s, u] = At(1), r = t.editMode === !0;
  So(() => {
    const v = i.current;
    if (!v) return;
    const C = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, N = Lo(a, C), x = () => u(xo(v.clientWidth, N));
    x();
    const S = new ResizeObserver(x);
    return S.observe(v), () => S.disconnect();
  }, [a]);
  const c = Cn(() => Io(o, s), [o, s]), d = Cn(() => {
    const v = {};
    for (const C of c) v[C.id] = C;
    return v;
  }, [c]), [m, f] = At(null), [h, b] = At(null), D = Ve((v, C) => {
    if (!r) {
      v.preventDefault();
      return;
    }
    f(C), v.dataTransfer.effectAllowed = "move", v.dataTransfer.setData("text/plain", C);
  }, [r]), _ = Ve((v, C) => {
    if (!r || !m || m === C) return;
    v.preventDefault(), v.dataTransfer.dropEffect = "move";
    const N = v.currentTarget.getBoundingClientRect(), x = v.clientX < N.left + N.width / 2;
    b((S) => S && S.id === C && S.before === x ? S : { id: C, before: x });
  }, [r, m]), E = Ve(() => {
  }, []), w = Ve((v, C, N) => {
    const x = o.map((B) => B.id), S = x.indexOf(v);
    if (S < 0) return;
    x.splice(S, 1);
    const F = x.indexOf(C);
    if (F < 0) {
      x.splice(S, 0, v);
      return;
    }
    const H = N ? F : F + 1;
    x.splice(H, 0, v), n("reorder", { order: x });
  }, [o, n]), k = Ve((v, C) => {
    if (!r || !m || m === C) return;
    v.preventDefault();
    const N = v.currentTarget.getBoundingClientRect(), x = v.clientX < N.left + N.width / 2;
    w(m, C, x), f(null), b(null);
  }, [r, m, w]), I = Ve(() => {
    f(null), b(null);
  }, []), y = {
    display: "grid",
    gridTemplateColumns: `repeat(${s}, 1fr)`,
    gap: "1rem"
  };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: i,
      className: "tlDashboard" + (r ? " tlDashboard--edit" : "")
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: y }, o.map((v) => {
      const C = d[v.id];
      if (!C) return null;
      const N = {
        gridColumn: `${C.colStart + 1} / ${C.colEnd + 1}`,
        gridRow: `${C.rowStart + 1} / ${C.rowEnd + 1}`
      }, x = ["tlDashboard__tile"];
      return m === v.id && x.push("tlDashboard__tile--dragging"), h && h.id === v.id && x.push(h.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: v.id,
          className: x.join(" "),
          style: N,
          draggable: r,
          onDragStart: (S) => D(S, v.id),
          onDragOver: (S) => _(S, v.id),
          onDragLeave: E,
          onDrop: (S) => k(S, v.id),
          onDragEnd: I
        },
        /* @__PURE__ */ e.createElement(G, { control: v.control }),
        r && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: Bo, useRef: yn, useState: wn, useEffect: Po, useLayoutEffect: Ao } = e, Fo = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, a) => /* @__PURE__ */ e.createElement("span", { key: a, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(G, { control: n }))));
}, Oo = ({ group: l }) => {
  var m, f;
  const [t, n] = wn(!1), [a, o] = wn({}), i = yn(null), s = yn(null), u = Bo(() => {
    n((h) => !h);
  }, []);
  Ao(() => {
    if (!t) return;
    const h = () => {
      const b = i.current;
      if (!b) return;
      const D = b.getBoundingClientRect();
      o({
        position: "fixed",
        top: D.bottom + 4,
        right: Math.max(8, window.innerWidth - D.right),
        left: "auto"
      });
    };
    return h(), window.addEventListener("resize", h), window.addEventListener("scroll", h, !0), () => {
      window.removeEventListener("resize", h), window.removeEventListener("scroll", h, !0);
    };
  }, [t]), Po(() => {
    if (!t) return;
    const h = (b) => {
      s.current && !s.current.contains(b.target) && i.current && !i.current.contains(b.target) && n(!1);
    };
    return document.addEventListener("mousedown", h), () => document.removeEventListener("mousedown", h);
  }, [t]), Oe(t, { ESCAPE: () => n(!1) }), Yt(t, s, "first");
  const r = l.items.filter((h) => h != null);
  if (r.length === 0) return null;
  if (r.length === 1 && !((m = l.subGroups) != null && m.length) && !l.icon)
    return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(G, { control: r[0] })));
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
  ), Tn.createPortal(
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
      r.map((h, b) => /* @__PURE__ */ e.createElement("div", { key: b, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(G, { control: h }))),
      (f = l.subGroups) == null ? void 0 : f.map((h, b) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${b}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), h.items.map((D, _) => /* @__PURE__ */ e.createElement("div", { key: _, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(G, { control: D })))))
    ),
    document.body
  ));
}, $o = ({ controlId: l }) => {
  const a = (X().groups ?? []).filter((o) => o.items.some((i) => i != null));
  return a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, a.map((o, i) => /* @__PURE__ */ e.createElement(e.Fragment, { key: o.name }, i > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), o.display === "menu" ? /* @__PURE__ */ e.createElement(Oo, { group: o }) : /* @__PURE__ */ e.createElement(Fo, { group: o }))));
}, Ho = ({ frame: l, covered: t }) => {
  const [n, a] = at(), o = [
    "tlTileStack__frame",
    t ? "tlTileStack__frame--covered" : "",
    n
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(rt, { host: a }, /* @__PURE__ */ e.createElement("div", { className: o }, /* @__PURE__ */ e.createElement(G, { control: l })));
}, Wo = ({ controlId: l }) => {
  const t = X(), [n, a] = at(), o = t.frames ?? [], i = t.activeIndex ?? 0;
  return /* @__PURE__ */ e.createElement(rt, { host: a }, /* @__PURE__ */ e.createElement("div", { id: l, className: n ? "tlTileStack " + n : "tlTileStack" }, o.map((s, u) => /* @__PURE__ */ e.createElement(Ho, { key: s.controlId, frame: s, covered: u !== i }))));
}, Uo = ({ controlId: l }) => {
  const t = X(), n = le(), a = t.content, o = t.breadcrumb ?? null;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAdaptiveDetail" }, o && o.length > 0 && /* @__PURE__ */ e.createElement("nav", { className: "tlAdaptiveDetail__breadcrumb", "aria-label": "Breadcrumb" }, o.map((i, s) => {
    const u = s === o.length - 1;
    return /* @__PURE__ */ e.createElement(e.Fragment, { key: i.depth }, s > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__sep" }, "›"), u ? /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__crumb tlAdaptiveDetail__crumb--current" }, i.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlAdaptiveDetail__crumb",
        onClick: () => n("navigate", { depth: i.depth })
      },
      i.label
    ));
  })), /* @__PURE__ */ e.createElement("div", { className: "tlAdaptiveDetail__content" }, a && /* @__PURE__ */ e.createElement(G, { control: a })));
}, Vo = ({ controlId: l }) => {
  const n = X().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((a, o) => /* @__PURE__ */ e.createElement(G, { key: o, control: a })));
}, zo = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), Ko = {
  "js.sidebar.openDrawer": "Open navigation"
}, Yo = ({ controlId: l }) => {
  const t = le(), n = ue(Ko);
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
z("TLButton", kl);
z("TLUploadButton", Nl);
z("TLToggleButton", Dl);
z("TLTextInput", cl);
z("TLPasswordInput", ul);
z("TLNumberInput", ml);
z("TLDatePicker", fl);
z("TLSelect", bl);
z("TLBooleanChoice", El);
z("TLCheckbox", yl);
z("TLCounter", Tl);
z("TLTabBar", Ll);
z("TLFieldList", xl);
z("TLAudioRecorder", Il);
z("TLAudioPlayer", Bl);
z("TLFileUpload", Al);
z("TLBinaryField", Ol);
z("TLFileChips", Wl);
z("TLRelativeTime", zl);
z("TLAnchor", Kl);
z("TLScrollLink", Yl);
z("TLAvatar", ql);
z("TLDownload", Ql);
z("TLPhotoCapture", ea);
z("TLPhotoViewer", na);
z("TLPdfViewer", aa);
z("TLSplitPanel", ra);
z("TLPanel", ma);
z("TLInset", ka);
z("TLMaximizeRoot", pa);
z("TLDeckPane", fa);
z("TLSidebar", ya);
z("TLStack", wa);
z("TLGrid", Na);
z("TLCard", Sa);
z("TLAppBar", Da);
z("TLBreadcrumb", Ra);
z("TLBottomBar", xa);
z("TLDialog", ja);
z("TLDialogManager", Aa);
z("TLWindow", Ha);
z("TLDrawer", Va);
z("TLMenuRegion", Ka);
z("TLSnackbar", qa);
z("TLNoticeBar", lr);
z("TLMenu", rr);
z("TLAppShell", sr);
z("TLText", cr);
z("TLTableView", pr);
z("TLColumnSelect", hr);
z("TLCalendar", Rr);
z("TLFormLayout", Fr);
z("TLFormGroup", Hr);
z("TLFormField", zr);
z("TLResourceCell", Kr);
z("TLTreeView", Gr);
z("TLDropdownSelect", to);
z("TLColorInput", go);
z("TLIconSelect", No);
z("TLDashboard", jo);
z("TLToolbar", $o);
z("TLTileStack", Wo);
z("TLAdaptiveDetail", Uo);
z("TLSlot", Vo);
z("TLSlotContent", zo);
z("TLDrawerToggle", Yo);
