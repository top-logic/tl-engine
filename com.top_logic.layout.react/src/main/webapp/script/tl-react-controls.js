import { React as e, useTLFieldValue as De, useTLCommand as ne, useTLState as G, useKeyboardBinding as me, useTLUpload as Ge, useFill as Ct, FillBarrier as Pe, TLChild as X, useI18N as ue, useTLDataUrl as Xe, scrollToAnchor as Jn, useStandaloneKeyboardScope as Oe, useFillHost as rt, FillProvider as ot, KeyboardScopeProvider as Kt, useFocusTrap as Yt, CMD_VALUE_CHANGED as ct, anchoredOverlayProps as el, register as W } from "tl-react-bridge";
const { useCallback: tn, useRef: tl } = e, nl = 300, ll = ({ controlId: l, state: t }) => {
  const [n, a, o] = De({
    debounceMs: nl,
    sendOnBlur: t.sendValueOnBlur === !0
  }), u = ne(), s = tl(!1), i = tn(
    (N) => {
      s.current = !0, a(N.target.value);
    },
    [a]
  ), r = t.commitOnBlur === !0, c = tn(async () => {
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
      onChange: i,
      onBlur: c,
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
      onChange: i,
      onBlur: c,
      disabled: t.disabled === !0,
      className: g,
      "aria-invalid": m || void 0,
      title: m && h ? h : void 0
    }
  ));
}, { useCallback: nn } = e, al = 300, rl = ({ controlId: l, state: t }) => {
  const [n, a, o] = De({ debounceMs: al }), u = nn(
    (m) => {
      a(m.target.value);
    },
    [a]
  ), s = nn(() => {
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
}, { useCallback: ln } = e, ol = 300, sl = ({ controlId: l, state: t }) => {
  const [n, a, o] = De({
    debounceMs: ol,
    sendOnBlur: t.sendValueOnBlur === !0
  }), u = ln(
    (p) => {
      const h = p.target.value;
      a(h === "" ? null : h);
    },
    [a]
  ), s = ln(() => {
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
}, { useCallback: cl } = e, il = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = cl(
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
}, { useCallback: ul } = e, dl = ({ controlId: l, state: t, config: n }) => {
  var d;
  const [a, o] = De(), u = ul(
    (m) => {
      o(m.target.value || null);
    },
    [o]
  ), s = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const m = ((d = s.find((p) => p.value === a)) == null ? void 0 : d.label) ?? "";
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
}, { useCallback: ml } = e, pl = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = t.options ?? [], u = t.presentation === "select", s = t.disabled === !0, i = t.hasError === !0, r = t.hasWarnings === !0, c = ml(
    (p) => {
      const h = o[p];
      a(h ? h.value : null);
    },
    [o, a]
  ), d = o.findIndex((p) => p.value === (n ?? null));
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
      onChange: (p) => c(Number(p.target.value))
    },
    d < 0 && /* @__PURE__ */ e.createElement("option", { value: "" }),
    o.map((p, h) => /* @__PURE__ */ e.createElement("option", { key: h, value: String(h) }, p.label))
  ) : /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: m + " tlBooleanChoice--radio",
      role: "radiogroup",
      "aria-invalid": i || void 0
    },
    o.map((p, h) => /* @__PURE__ */ e.createElement("label", { key: h, className: "tlBooleanChoice__option" }, /* @__PURE__ */ e.createElement(
      "input",
      {
        type: "radio",
        name: l,
        checked: d === h,
        disabled: s,
        onChange: () => c(h)
      }
    ), /* @__PURE__ */ e.createElement("span", { className: "tlBooleanChoice__label" }, p.label)))
  );
}, { useCallback: fl, useRef: hl, useEffect: bl } = e, gl = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = t.triState === !0, u = hl(null);
  bl(() => {
    u.current && (u.current.indeterminate = o && n !== !0 && n !== !1);
  }, [o, n]);
  const s = fl(
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
const { useCallback: El } = e, vl = ({ controlId: l, command: t, label: n, image: a, disabled: o, displayMode: u }) => {
  const s = G(), i = ne(), r = t ?? "click", c = n ?? s.label, d = a ?? s.image, m = o ?? s.disabled === !0, p = u ?? s.displayMode ?? "label-only", h = s.hidden === !0, g = s.tooltip, N = s.appearance, C = s.size, _ = s.cssClasses, y = s.navigateUrl, k = El(() => {
    if (y) {
      window.location.assign(y);
      return;
    }
    i(r);
  }, [i, r, y]), x = s.keyGesture;
  me(x, () => m || h ? !1 : (k(), !0));
  const E = p === "icon-only", w = p === "label-only" || p === "icon-label" || E && !d, b = g ?? (E ? c : void 0), D = b ? `text:${b}` : void 0;
  return h ? null : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: k,
      disabled: m,
      className: "tlReactButton" + (E ? " tlReactButton--iconOnly" : "") + (p === "label-only" ? " tlReactButton--labelOnly" : "") + (N === "link" ? " tlReactButton--link" : "") + (N === "primary" ? " tlReactButton--primary" : "") + (C === "small" ? " tlReactButton--small" : "") + (C === "large" ? " tlReactButton--large" : "") + (_ ? " " + _ : ""),
      "data-tooltip": D,
      "aria-label": d || E ? c : void 0
    },
    d && /* @__PURE__ */ e.createElement(Ne, { encoded: d, className: "tlReactButton__image" }),
    w && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, c)
  );
}, _l = ({ controlId: l }) => {
  const t = G(), n = Ge(), a = e.useRef(null), [o, u] = e.useState(!1), s = t.label ?? "", i = t.image, r = t.disabled === !0, c = t.hidden === !0, d = t.displayMode ?? "label-only", m = t.appearance, p = t.accept, h = t.multiple === !0, g = e.useCallback(() => {
    var x;
    r || o || (x = a.current) == null || x.click();
  }, [r, o]), N = e.useCallback(async (x) => {
    const E = x.target.files;
    if (!E || E.length === 0) return;
    const w = new FormData();
    for (let b = 0; b < E.length; b++)
      w.append("file", E[b], E[b].name);
    x.target.value = "", u(!0);
    try {
      await n(w);
    } finally {
      u(!1);
    }
  }, [n]), C = d === "icon-only", _ = d === "icon-only" || d === "icon-label", y = d === "label-only" || d === "icon-label" || C && !i, k = r || o;
  return /* @__PURE__ */ e.createElement("span", { id: l, style: { display: "contents" } }, /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: a,
      type: "file",
      accept: p && p !== "*" ? p : void 0,
      multiple: h || void 0,
      onChange: N,
      style: { display: "none" }
    }
  ), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: g,
      disabled: k,
      style: c ? { display: "none" } : void 0,
      className: "tlReactButton" + (C ? " tlReactButton--iconOnly" : "") + (m === "link" ? " tlReactButton--link" : "") + (m === "primary" ? " tlReactButton--primary" : ""),
      "aria-label": C ? s : void 0
    },
    _ && i && /* @__PURE__ */ e.createElement(Ne, { encoded: i, className: "tlReactButton__image" }),
    y && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, s)
  ));
}, { useCallback: Cl } = e, yl = ({ controlId: l, command: t, label: n, active: a, disabled: o }) => {
  const u = G(), s = ne(), i = t ?? "click", r = n ?? u.label, c = a ?? u.active === !0, d = o ?? u.disabled === !0, m = Cl(() => {
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
}, wl = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.count ?? 0, o = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: kl } = e, Nl = ({ controlId: l }) => {
  const t = G(), n = ne(), a = Ct(!0), o = t.tabs ?? [], u = t.activeTabId, s = kl((i) => {
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
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, /* @__PURE__ */ e.createElement(Pe, null, t.activeContent && /* @__PURE__ */ e.createElement(X, { control: t.activeContent }))));
}, Sl = ({ controlId: l }) => {
  const t = G(), n = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((o, u) => /* @__PURE__ */ e.createElement("div", { key: u, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(X, { control: o })))));
}, Dl = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Tl = ({ controlId: l }) => {
  const t = G(), n = Ge(), [a, o] = e.useState("idle"), [u, s] = e.useState(null), i = e.useRef(null), r = e.useRef([]), c = e.useRef(null), d = t.status ?? "idle", m = t.error, p = d === "received" ? "idle" : a !== "idle" ? a : d, h = e.useCallback(async () => {
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
        const k = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", x = new MediaRecorder(y, k ? { mimeType: k } : void 0);
        i.current = x, x.ondataavailable = (E) => {
          E.data.size > 0 && r.current.push(E.data);
        }, x.onstop = async () => {
          y.getTracks().forEach((b) => b.stop()), c.current = null;
          const E = new Blob(r.current, { type: x.mimeType || "audio/webm" });
          if (r.current = [], E.size === 0) {
            o("idle");
            return;
          }
          o("uploading");
          const w = new FormData();
          w.append("audio", E, "recording.webm"), await n(w), o("idle");
        }, x.start(), o("recording");
      } catch (y) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", y), s("js.audioRecorder.error.denied"), o("idle");
      }
    }
  }, [a, n]), g = ue(Dl), N = p === "recording" ? g["js.audioRecorder.stop"] : p === "uploading" ? g["js.uploading"] : g["js.audioRecorder.record"], C = p === "uploading", _ = ["tlAudioRecorder__button"];
  return p === "recording" && _.push("tlAudioRecorder__button--recording"), p === "uploading" && _.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: _.join(" "),
      onClick: h,
      disabled: C,
      title: N,
      "aria-label": N
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${p === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), u && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, g[u]), m && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, m));
}, Rl = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Ll = ({ controlId: l }) => {
  const t = G(), n = Xe(), a = !!t.hasAudio, o = t.dataRevision ?? 0, [u, s] = e.useState(a ? "idle" : "disabled"), i = e.useRef(null), r = e.useRef(null), c = e.useRef(o);
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
  }, [u, n]), m = ue(Rl), p = u === "loading" ? m["js.loading"] : u === "playing" ? m["js.audioPlayer.pause"] : u === "disabled" ? m["js.audioPlayer.noAudio"] : m["js.audioPlayer.play"], h = u === "disabled" || u === "loading", g = ["tlAudioPlayer__button"];
  return u === "playing" && g.push("tlAudioPlayer__button--playing"), u === "loading" && g.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: g.join(" "),
      onClick: d,
      disabled: h,
      title: p,
      "aria-label": p
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${u === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, xl = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Ml = ({ controlId: l }) => {
  const t = G(), n = Ge(), [a, o] = e.useState("idle"), [u, s] = e.useState(!1), i = e.useRef(null), r = t.status ?? "idle", c = t.error, d = t.accept ?? "", m = r === "received" ? "idle" : a !== "idle" ? a : r, p = e.useCallback(async (E) => {
    o("uploading");
    const w = new FormData();
    w.append("file", E, E.name), await n(w), o("idle");
  }, [n]), h = e.useCallback((E) => {
    var b;
    const w = (b = E.target.files) == null ? void 0 : b[0];
    w && p(w);
  }, [p]), g = e.useCallback(() => {
    var E;
    a !== "uploading" && ((E = i.current) == null || E.click());
  }, [a]), N = e.useCallback((E) => {
    E.preventDefault(), E.stopPropagation(), s(!0);
  }, []), C = e.useCallback((E) => {
    E.preventDefault(), E.stopPropagation(), s(!1);
  }, []), _ = e.useCallback((E) => {
    var b;
    if (E.preventDefault(), E.stopPropagation(), s(!1), a === "uploading") return;
    const w = (b = E.dataTransfer.files) == null ? void 0 : b[0];
    w && p(w);
  }, [a, p]), y = m === "uploading", k = ue(xl), x = m === "uploading" ? k["js.uploading"] : k["js.fileUpload.choose"];
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
        onClick: g,
        disabled: y,
        title: x,
        "aria-label": x
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    c && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, c)
  );
}, Il = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, jl = ({ controlId: l, state: t }) => {
  const a = G() ?? t ?? {}, o = Ge(), u = Xe(), s = ue(Il), i = a.editable !== !1, r = !!a.hasData, c = a.fileName ?? "download", d = a.dataRevision ?? 0, m = a.accept ?? "", p = a.status ?? "idle", h = a.error ?? null, [g, N] = e.useState("idle"), [C, _] = e.useState(!1), [y, k] = e.useState(!1), x = e.useRef(null), E = e.useCallback(async () => {
    if (!(!r || y)) {
      k(!0);
      try {
        const I = u + (u.includes("?") ? "&" : "?") + "rev=" + d, L = await fetch(I);
        if (!L.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", L.status);
          return;
        }
        const S = await L.blob(), $ = URL.createObjectURL(S), f = document.createElement("a");
        f.href = $, f.download = c, f.style.display = "none", document.body.appendChild(f), f.click(), document.body.removeChild(f), URL.revokeObjectURL($);
      } catch (I) {
        console.error("[TLBinaryField] Fetch error:", I);
      } finally {
        k(!1);
      }
    }
  }, [r, y, u, d, c]), w = e.useCallback(async (I) => {
    N("uploading");
    const L = new FormData();
    L.append("file", I, I.name), await o(L), N("idle");
  }, [o]), b = (p === "received" ? "idle" : g !== "idle" ? g : p) === "uploading", D = e.useCallback((I) => {
    var S;
    const L = (S = I.target.files) == null ? void 0 : S[0];
    L && w(L);
  }, [w]), j = e.useCallback(() => {
    var I;
    b || (I = x.current) == null || I.click();
  }, [b]), R = e.useCallback((I) => {
    I.preventDefault(), I.stopPropagation(), _(!0);
  }, []), H = e.useCallback((I) => {
    I.preventDefault(), I.stopPropagation(), _(!1);
  }, []), K = e.useCallback((I) => {
    var S;
    if (I.preventDefault(), I.stopPropagation(), _(!1), b) return;
    const L = (S = I.dataTransfer.files) == null ? void 0 : S[0];
    L && w(L);
  }, [b, w]), A = y ? s["js.downloading"] : s["js.download.file"].replace("{0}", c), V = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (y ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: E,
      disabled: y,
      title: A,
      "aria-label": A
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: c }, c));
  if (!i)
    return r ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, V) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, s["js.download.noFile"]));
  const B = b, P = b ? s["js.uploading"] : s["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlBinaryField tlFileUpload${C ? " tlFileUpload--dragover" : ""}`,
      onDragOver: R,
      onDragLeave: H,
      onDrop: K
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: x,
        type: "file",
        accept: m || void 0,
        onChange: D,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (B ? " tlFileUpload__button--uploading" : ""),
        onClick: j,
        disabled: B,
        title: P,
        "aria-label": P
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    r && V,
    h && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, h)
  );
}, Pl = {
  "js.fileChips.add": "Add file",
  "js.fileChips.remove": "Remove {0}",
  "js.uploading": "Uploading…",
  "js.download.file": "Download {0}"
};
function Bl(l) {
  return l < 1024 ? l + " B" : l < 1024 * 1024 ? (l / 1024).toFixed(l < 10 * 1024 ? 1 : 0) + " KB" : (l / (1024 * 1024)).toFixed(1) + " MB";
}
const Al = ({ controlId: l }) => {
  const t = G(), n = ne(), a = Ge(), o = Xe(), u = ue(Pl), s = t.chips ?? [], i = t.editable === !0, [r, c] = e.useState(!1), [d, m] = e.useState(!1), p = e.useRef(null), h = e.useCallback(async (E) => {
    const w = Array.from(E);
    if (w.length !== 0) {
      c(!0);
      try {
        const b = new FormData();
        for (const D of w)
          b.append("file", D, D.name);
        await a(b);
      } finally {
        c(!1);
      }
    }
  }, [a]), g = e.useCallback(async (E) => {
    if (E.hasData)
      try {
        const w = o + "&key=" + encodeURIComponent(E.key), b = await fetch(w);
        if (!b.ok) {
          console.error("[TLFileChips] Failed to fetch data:", b.status);
          return;
        }
        const D = await b.blob(), j = URL.createObjectURL(D), R = document.createElement("a");
        R.href = j, R.download = E.name, R.style.display = "none", document.body.appendChild(R), R.click(), document.body.removeChild(R), URL.revokeObjectURL(j);
      } catch (w) {
        console.error("[TLFileChips] Fetch error:", w);
      }
  }, [o]), N = e.useCallback((E) => {
    E.target.files && h(E.target.files), E.target.value = "";
  }, [h]), C = e.useCallback(() => {
    var E;
    r || (E = p.current) == null || E.click();
  }, [r]), _ = e.useCallback((E) => {
    i && (E.preventDefault(), E.stopPropagation(), m(!0));
  }, [i]), y = e.useCallback((E) => {
    i && (E.preventDefault(), E.stopPropagation(), m(!1));
  }, [i]), k = e.useCallback((E) => {
    i && (E.preventDefault(), E.stopPropagation(), m(!1), !r && E.dataTransfer.files && h(E.dataTransfer.files));
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
      onDrop: k
    },
    s.map((E) => {
      const w = u["js.download.file"].replace("{0}", E.name), b = u["js.fileChips.remove"].replace("{0}", E.name);
      return /* @__PURE__ */ e.createElement("span", { key: E.key, className: "tlFileChip" }, /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__main",
          onClick: () => g(E),
          disabled: !E.hasData,
          title: E.hasData ? w : E.name
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
        /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__name" }, E.name),
        E.size != null && /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__size" }, Bl(E.size))
      ), i && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__remove",
          onClick: () => n("removeChip", { key: E.key }),
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
        ref: p,
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
}, Fl = 3e4;
function Ol(l, t) {
  const n = Math.round((l - Date.now()) / 1e3), a = Math.abs(n), o = new Intl.RelativeTimeFormat(t, { numeric: "auto" });
  return a < 60 ? o.format(Math.trunc(n / 1), "second") : a < 3600 ? o.format(Math.trunc(n / 60), "minute") : a < 86400 ? o.format(Math.trunc(n / 3600), "hour") : a < 7 * 86400 ? o.format(Math.trunc(n / 86400), "day") : new Date(l).toLocaleDateString(t);
}
const $l = ({ controlId: l }) => {
  const t = G(), n = t.timestamp, a = t.label ?? void 0, o = t.locale || navigator.language, [, u] = e.useState(0);
  return e.useEffect(() => {
    const s = setInterval(() => u((i) => i + 1), Fl);
    return () => clearInterval(s);
  }, []), n == null ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime tlRelativeTime--empty" }) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime", title: a }, Ol(n, o));
}, Hl = ({ controlId: l }) => {
  const t = G(), n = typeof t.fraction == "number" ? t.fraction : 0, a = t.label || void 0, o = Math.round(n * 100);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlProgress",
      role: "progressbar",
      "aria-valuemin": 0,
      "aria-valuemax": 100,
      "aria-valuenow": o,
      "aria-valuetext": a
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlProgress__track" }, /* @__PURE__ */ e.createElement("span", { className: "tlProgress__fill", style: { width: o + "%" } })),
    a && /* @__PURE__ */ e.createElement("span", { className: "tlProgress__label" }, a)
  );
}, Wl = ({ controlId: l }) => {
  const t = G(), n = t.anchor ?? void 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAnchor", "data-tl-anchor": n }, t.child && /* @__PURE__ */ e.createElement(X, { control: t.child }));
}, Ul = ({ controlId: l }) => {
  const t = G(), n = t.target, a = t.label ?? "";
  if (n == null)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlScrollLink tlScrollLink--empty" });
  const o = (u) => {
    u.preventDefault(), Jn(n);
  };
  return /* @__PURE__ */ e.createElement("a", { id: l, className: "tlScrollLink", href: "#", onClick: o }, a);
};
function zl(l) {
  const t = l.trim().split(/\s+/).filter(Boolean);
  return t.length === 0 ? "?" : t.length === 1 ? t[0].slice(0, 2).toUpperCase() : (t[0][0] + t[t.length - 1][0]).toUpperCase();
}
function Vl(l) {
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return Math.abs(t) % 360;
}
const Kl = ({ controlId: l }) => {
  const n = G().name;
  return n ? /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlAvatar",
      style: { backgroundColor: `hsl(${Vl(n)}, 45%, 45%)` },
      title: n,
      "aria-label": n
    },
    zl(n)
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlAvatar tlAvatar--empty" });
}, Yl = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Gl = ({ controlId: l }) => {
  const t = G(), n = Xe(), a = ne(), o = !!t.hasData, u = t.dataRevision ?? 0, s = t.fileName ?? "download", i = !!t.clearable, [r, c] = e.useState(!1), d = e.useCallback(async () => {
    if (!(!o || r)) {
      c(!0);
      try {
        const g = n + (n.includes("?") ? "&" : "?") + "rev=" + u, N = await fetch(g);
        if (!N.ok) {
          console.error("[TLDownload] Failed to fetch data:", N.status);
          return;
        }
        const C = await N.blob(), _ = URL.createObjectURL(C), y = document.createElement("a");
        y.href = _, y.download = s, y.style.display = "none", document.body.appendChild(y), y.click(), document.body.removeChild(y), URL.revokeObjectURL(_);
      } catch (g) {
        console.error("[TLDownload] Fetch error:", g);
      } finally {
        c(!1);
      }
    }
  }, [o, r, n, u, s]), m = e.useCallback(async () => {
    o && await a("clear");
  }, [o, a]), p = ue(Yl);
  if (!o)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, p["js.download.noFile"]));
  const h = r ? p["js.downloading"] : p["js.download.file"].replace("{0}", s);
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
      title: p["js.download.clear"],
      "aria-label": p["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, Xl = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, ql = ({ controlId: l }) => {
  const t = G(), n = Ge(), [a, o] = e.useState("idle"), [u, s] = e.useState(null), [i, r] = e.useState(!1), c = e.useRef(null), d = e.useRef(null), m = e.useRef(null), p = e.useRef(null), h = e.useRef(null), g = t.error, N = e.useMemo(
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
        (R = p.current) == null || R.click();
        return;
      }
      try {
        const H = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        d.current = H, o("overlayOpen");
      } catch (H) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", H), s("js.photoCapture.error.denied"), o("idle");
      }
    }
  }, [a, N]), k = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const R = c.current, H = m.current;
    if (!R || !H)
      return;
    H.width = R.videoWidth, H.height = R.videoHeight;
    const K = H.getContext("2d");
    K && (K.drawImage(R, 0, 0), C(), o("uploading"), H.toBlob(async (A) => {
      if (!A) {
        o("idle");
        return;
      }
      const V = new FormData();
      V.append("photo", A, "capture.jpg"), await n(V), o("idle");
    }, "image/jpeg", 0.85));
  }, [a, n, C]), x = e.useCallback(async (R) => {
    var A;
    const H = (A = R.target.files) == null ? void 0 : A[0];
    if (!H) return;
    o("uploading");
    const K = new FormData();
    K.append("photo", H, H.name), await n(K), o("idle"), p.current && (p.current.value = "");
  }, [n]);
  e.useEffect(() => {
    a === "overlayOpen" && c.current && d.current && (c.current.srcObject = d.current);
  }, [a]), e.useEffect(() => {
    var H;
    if (a !== "overlayOpen") return;
    (H = h.current) == null || H.focus();
    const R = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = R;
    };
  }, [a]), Oe(a === "overlayOpen", { ESCAPE: _ }), e.useEffect(() => () => {
    d.current && (d.current.getTracks().forEach((R) => R.stop()), d.current = null);
  }, []);
  const E = ue(Xl), w = a === "uploading" ? E["js.uploading"] : E["js.photoCapture.open"], b = ["tlPhotoCapture__cameraBtn"];
  a === "uploading" && b.push("tlPhotoCapture__cameraBtn--uploading");
  const D = ["tlPhotoCapture__overlayVideo"];
  i && D.push("tlPhotoCapture__overlayVideo--mirrored");
  const j = ["tlPhotoCapture__mirrorBtn"];
  return i && j.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: b.join(" "),
      onClick: y,
      disabled: a === "uploading",
      title: w,
      "aria-label": w
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !N && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: p,
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
        className: D.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: j.join(" "),
        onClick: () => r((R) => !R),
        title: E["js.photoCapture.mirror"],
        "aria-label": E["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: k,
        title: E["js.photoCapture.capture"],
        "aria-label": E["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: _,
        title: E["js.photoCapture.close"],
        "aria-label": E["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), u && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, E[u]), g && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, g));
}, Zl = {
  "js.photoViewer.alt": "Captured photo"
}, Ql = ({ controlId: l }) => {
  const t = G(), n = Xe(), a = !!t.hasPhoto, o = t.dataRevision ?? 0, [u, s] = e.useState(null), i = e.useRef(o);
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
  const r = ue(Zl);
  return !a || !u ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: u,
      alt: t.alt || r["js.photoViewer.alt"]
    }
  ));
}, Jl = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, ea = ({ controlId: l }) => {
  const t = G(), n = Xe(), a = !!t.hasPdf, o = t.dataRevision ?? 0, u = ue(Jl), i = n.indexOf("react-api/"), r = i >= 0 ? n.slice(0, i) : n, c = n + "&rev=" + o, d = r + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(c);
  return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: d,
      title: u["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, u["js.pdfViewer.noDocument"]));
}, { useCallback: an, useRef: St } = e, ta = ({ controlId: l }) => {
  const t = G(), n = ne(), a = Ct(!0), o = t.orientation, u = t.resizable === !0, s = t.children ?? [], i = o === "horizontal", r = s.length > 0 && s.every((_) => _.collapsed), c = !r && s.some((_) => _.collapsed), d = r ? !i : i, m = St(null), p = St(null), h = St(null), g = an((_, y) => {
    const k = {
      overflow: _.scrolling || "auto"
    };
    return _.collapsed ? r && !d ? k.flex = "1 0 0%" : k.flex = "0 0 auto" : y !== void 0 ? k.flex = `0 0 ${y}px` : k.flex = `${_.size} 1 0%`, _.minSize > 0 && !_.collapsed && (k.minWidth = i ? _.minSize : void 0, k.minHeight = i ? void 0 : _.minSize), k;
  }, [i, r, c, d]), N = an((_, y) => {
    _.preventDefault();
    const k = m.current;
    if (!k) return;
    const x = s[y], E = s[y + 1], w = k.querySelectorAll(":scope > .tlSplitPanel__child"), b = [];
    w.forEach((R) => {
      b.push(i ? R.offsetWidth : R.offsetHeight);
    }), h.current = b, p.current = {
      splitterIndex: y,
      startPos: i ? _.clientX : _.clientY,
      startSizeBefore: b[y],
      startSizeAfter: b[y + 1],
      childBefore: x,
      childAfter: E
    };
    const D = (R) => {
      const H = p.current;
      if (!H || !h.current) return;
      const A = (i ? R.clientX : R.clientY) - H.startPos, V = H.childBefore.minSize || 0, B = H.childAfter.minSize || 0;
      let P = H.startSizeBefore + A, I = H.startSizeAfter - A;
      P < V && (I += P - V, P = V), I < B && (P += I - B, I = B), h.current[H.splitterIndex] = P, h.current[H.splitterIndex + 1] = I;
      const L = k.querySelectorAll(":scope > .tlSplitPanel__child"), S = L[H.splitterIndex], $ = L[H.splitterIndex + 1];
      S && (S.style.flex = `0 0 ${P}px`), $ && ($.style.flex = `0 0 ${I}px`);
    }, j = () => {
      if (document.removeEventListener("mousemove", D), document.removeEventListener("mouseup", j), document.body.style.cursor = "", document.body.style.userSelect = "", h.current) {
        const R = {};
        s.forEach((H, K) => {
          const A = H.control;
          A != null && A.controlId && h.current && (R[A.controlId] = h.current[K]);
        }), n("updateSizes", { sizes: R });
      }
      h.current = null, p.current = null;
    };
    document.addEventListener("mousemove", D), document.addEventListener("mouseup", j), document.body.style.cursor = i ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [s, i, n]), C = [];
  return s.forEach((_, y) => {
    if (C.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${y}`,
          className: `tlSplitPanel__child${_.collapsed && d ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: g(_)
        },
        /* @__PURE__ */ e.createElement(X, { control: _.control })
      )
    ), u && y < s.length - 1) {
      const k = s[y + 1];
      !_.collapsed && !k.collapsed && C.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${y}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${o}`,
            onMouseDown: (E) => N(E, y)
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
}, Ft = ({ image: l, className: t }) => {
  if (!l || l === "none") return null;
  const n = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `${t ? t + " " : ""}${n}`, "aria-hidden": "true" });
}, { useCallback: Dt } = e, na = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, la = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), aa = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), ra = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), oa = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), sa = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), ca = ({ controlId: l }) => {
  const t = G(), n = ne(), a = ue(na), o = t.title, u = t.expansionState ?? "NORMALIZED", s = t.showMinimize === !0, i = t.showMaximize === !0, r = t.showPopOut === !0, c = t.fullLine === !0, d = t.fill === !0, m = t.hoverActions === !0, p = t.appearance === "card", h = t.errorMessage, g = u === "MINIMIZED", N = u === "MAXIMIZED", C = u === "HIDDEN", _ = Dt(() => {
    n("toggleMinimize");
  }, [n]), y = Dt(() => {
    n("toggleMaximize");
  }, [n]), k = Dt(() => {
    n("popOut");
  }, [n]), x = Ct(d && !C);
  if (C)
    return null;
  const E = N ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" }, w = s && !N || i && !g || r, b = !!o && o.trim() !== "" || !!t.titleContent || !!t.toolbar || w;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${u.toLowerCase()}${c ? " tlPanel--fullLine" : ""}${x ? " " + x : ""}${m ? " tlPanel--hoverActions" : ""}${p ? " tlPanel--card" : ""}`,
      style: E
    },
    b && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, !!o && o.trim() !== "" && /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, o), t.titleContent && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__titleContent" }, /* @__PURE__ */ e.createElement(X, { control: t.titleContent })), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(X, { control: t.toolbar }), s && !N && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: _,
        title: g ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      g ? /* @__PURE__ */ e.createElement(aa, null) : /* @__PURE__ */ e.createElement(la, null)
    ), i && !g && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: y,
        title: N ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      N ? /* @__PURE__ */ e.createElement(oa, null) : /* @__PURE__ */ e.createElement(ra, null)
    ), r && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: k,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(sa, null)
    ))),
    !g && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(Pe, null, /* @__PURE__ */ e.createElement(X, { control: t.child }))),
    !g && h && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error tlPanel__error", role: "alert" }, /* @__PURE__ */ e.createElement(Ft, { image: t.errorIcon, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, h)),
    !g && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(X, { control: t.buttonBar }))
  );
}, ia = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(X, { control: t.child })
  );
}, ua = ({ controlId: l }) => {
  const t = G(), [n, a] = rt();
  return /* @__PURE__ */ e.createElement(ot, { host: a }, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: n ? "tlDeckPane " + n : "tlDeckPane",
      style: { width: "100%", height: "100%" }
    },
    t.activeChild && /* @__PURE__ */ e.createElement(X, { control: t.activeChild })
  ));
}, { useCallback: Ee, useState: ht, useEffect: Ot, useRef: gt } = e, da = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function $t(l, t, n, a) {
  const o = [];
  for (const u of l)
    if (u.type === "nav") {
      if (u.hidden) continue;
      o.push({ id: u.id, type: "nav", groupId: a });
    } else u.type === "command" ? o.push({ id: u.id, type: "command", groupId: a }) : u.type === "group" && (o.push({ id: u.id, type: "group" }), (n.get(u.id) ?? u.expanded) && !t && o.push(...$t(u.children, t, n, u.id)));
  return o;
}
const Ye = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(Ne, { encoded: l, className: "tlSidebar__icon" }) : null, ma = ({ item: l, active: t, collapsed: n, onSelect: a, tabIndex: o, itemRef: u, onFocus: s }) => /* @__PURE__ */ e.createElement(
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
), pa = ({ item: l, collapsed: t, onExecute: n, tabIndex: a, itemRef: o, onFocus: u }) => /* @__PURE__ */ e.createElement(
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
), fa = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Ye, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), ha = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), ba = ({ item: l, activeItemId: t, anchorRect: n, onSelect: a, onExecute: o, onClose: u }) => {
  const s = gt(null);
  Ot(() => {
    const c = (d) => {
      s.current && !s.current.contains(d.target) && setTimeout(() => u(), 0);
    };
    return document.addEventListener("mousedown", c), () => document.removeEventListener("mousedown", c);
  }, [u]), Oe(!0, { ESCAPE: u });
  const i = Ee((c) => {
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
}, ga = ({
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
  onItemFocus: p,
  flyoutGroupId: h,
  onOpenFlyout: g,
  onCloseFlyout: N
}) => {
  const C = gt(null), [_, y] = ht(null), k = Ee(() => {
    a ? h === l.id ? N() : (C.current && y(C.current.getBoundingClientRect()), g(l.id)) : s(l.id);
  }, [a, h, l.id, s, g, N]), x = Ee((w) => {
    C.current = w, r(w);
  }, [r]), E = a && h === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (E ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: k,
      title: a ? l.label : void 0,
      "aria-expanded": a ? E : t,
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
  ), E && /* @__PURE__ */ e.createElement(
    ba,
    {
      item: l,
      activeItemId: n,
      anchorRect: _,
      onSelect: o,
      onExecute: u,
      onClose: N
    }
  ), t && !a && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((w) => /* @__PURE__ */ e.createElement(
    kn,
    {
      key: w.id,
      item: w,
      activeItemId: n,
      collapsed: a,
      onSelect: o,
      onExecute: u,
      onToggleGroup: s,
      focusedId: d,
      setItemRef: m,
      onItemFocus: p,
      groupStates: null,
      flyoutGroupId: h,
      onOpenFlyout: g,
      onCloseFlyout: N
    }
  ))));
}, kn = ({
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
  onCloseFlyout: p
}) => {
  switch (l.type) {
    case "nav":
      return l.hidden ? null : /* @__PURE__ */ e.createElement(
        ma,
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
        pa,
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
      return /* @__PURE__ */ e.createElement(fa, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(ha, null);
    case "group": {
      const h = c ? c.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        ga,
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
          onCloseFlyout: p
        }
      );
    }
    default:
      return null;
  }
}, Ea = ({ controlId: l }) => {
  const t = G(), n = ne(), a = ue(da), o = t.items ?? [], u = t.activeItemId, s = t.collapsed, i = t.drawerOpen, r = i ? !1 : s, [c, d] = ht(() => {
    const A = /* @__PURE__ */ new Map(), V = (B) => {
      for (const P of B)
        P.type === "group" && (A.set(P.id, P.expanded), V(P.children));
    };
    return V(o), A;
  }), m = Ee((A) => {
    d((V) => {
      const B = new Map(V), P = B.get(A) ?? !1;
      return B.set(A, !P), n("toggleGroup", { itemId: A, expanded: !P }), B;
    });
  }, [n]), p = Ee((A) => {
    A !== u && n("selectItem", { itemId: A });
  }, [n, u]), h = Ee((A) => {
    n("executeCommand", { itemId: A });
  }, [n]), g = Ee(() => {
    n("toggleCollapse", {});
  }, [n]), N = Ee(() => {
    n("toggleDrawer", {});
  }, [n]), [C, _] = ht(null), y = Ee((A) => {
    _(A);
  }, []), k = Ee(() => {
    _(null);
  }, []);
  Ot(() => {
    r || _(null);
  }, [r]);
  const [x, E] = ht(() => {
    const A = $t(o, r, c);
    return A.length > 0 ? A[0].id : "";
  }), w = gt(/* @__PURE__ */ new Map()), b = Ee((A) => (V) => {
    V ? w.current.set(A, V) : w.current.delete(A);
  }, []), D = Ee((A) => {
    E(A);
  }, []), j = gt(0), R = Ee((A) => {
    E(A), j.current++;
  }, []);
  Ot(() => {
    const A = w.current.get(x);
    A && document.activeElement !== A && A.focus();
  }, [x, j.current]);
  const H = Ee((A) => {
    if (A.key === "Escape" && C !== null) {
      A.preventDefault(), k();
      return;
    }
    const V = $t(o, r, c);
    if (V.length === 0) return;
    const B = V.findIndex((I) => I.id === x);
    if (B < 0) return;
    const P = V[B];
    switch (A.key) {
      case "ArrowDown": {
        A.preventDefault();
        const I = (B + 1) % V.length;
        R(V[I].id);
        break;
      }
      case "ArrowUp": {
        A.preventDefault();
        const I = (B - 1 + V.length) % V.length;
        R(V[I].id);
        break;
      }
      case "Home": {
        A.preventDefault(), R(V[0].id);
        break;
      }
      case "End": {
        A.preventDefault(), R(V[V.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        A.preventDefault(), P.type === "nav" ? p(P.id) : P.type === "command" ? h(P.id) : P.type === "group" && (r ? C === P.id ? k() : y(P.id) : m(P.id));
        break;
      }
      case "ArrowRight": {
        P.type === "group" && !r && ((c.get(P.id) ?? !1) || (A.preventDefault(), m(P.id)));
        break;
      }
      case "ArrowLeft": {
        P.type === "group" && !r && (c.get(P.id) ?? !1) && (A.preventDefault(), m(P.id));
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
    p,
    h,
    m,
    y,
    k
  ]), K = "tlSidebar" + (r ? " tlSidebar--collapsed" : "") + (i ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: K }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(X, { control: t.drawerToggleContribution }), i && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: N, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": a["js.sidebar.ariaLabel"] }, r ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(X, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(X, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: H }, o.map((A) => /* @__PURE__ */ e.createElement(
    kn,
    {
      key: A.id,
      item: A,
      activeItemId: u,
      collapsed: r,
      onSelect: p,
      onExecute: h,
      onToggleGroup: m,
      focusedId: x,
      setItemRef: b,
      onItemFocus: D,
      groupStates: c,
      flyoutGroupId: C,
      onOpenFlyout: y,
      onCloseFlyout: k
    }
  ))), r ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(X, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(X, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: g,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, /* @__PURE__ */ e.createElement(Pe, null, t.activeContent && /* @__PURE__ */ e.createElement(X, { control: t.activeContent }))));
}, va = ({ controlId: l }) => {
  const t = G(), n = t.direction ?? "column", a = t.gap ?? "default", o = t.align ?? "stretch", u = t.wrap === !0, s = t.growFirst === !0, i = t.children ?? [], [r, c] = rt(), d = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${a}`,
    `tlStack--align-${o}`,
    u ? "tlStack--wrap" : "",
    s ? "tlStack--grow-first" : "",
    r,
    t.cssClass ?? ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(ot, { host: c }, /* @__PURE__ */ e.createElement("div", { id: l, className: d }, i.map((m, p) => /* @__PURE__ */ e.createElement(X, { key: p, control: m }))));
}, _a = ({ controlId: l }) => {
  const t = G(), [n, a] = rt();
  return /* @__PURE__ */ e.createElement(ot, { host: a }, /* @__PURE__ */ e.createElement("div", { id: l, className: n ? "tlInset " + n : "tlInset" }, t.child && /* @__PURE__ */ e.createElement(X, { control: t.child })));
}, Ca = ({ controlId: l }) => {
  const t = G(), n = t.columns, a = t.minColumnWidth, o = t.gap ?? "default", u = t.children ?? [], s = {};
  return a ? s.gridTemplateColumns = `repeat(auto-fit, minmax(min(${a}, 100%), 1fr))` : n && (s.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${o}`, style: s }, u.map((i, r) => /* @__PURE__ */ e.createElement(X, { key: r, control: i })));
}, ya = ({ controlId: l }) => {
  const t = G(), n = t.title, a = t.variant ?? "outlined", o = t.padding ?? "default", u = t.headerActions ?? [], s = t.child, i = n != null || u.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${a}` }, i && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), u.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, u.map((r, c) => /* @__PURE__ */ e.createElement(X, { key: c, control: r })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${o}` }, /* @__PURE__ */ e.createElement(Pe, null, /* @__PURE__ */ e.createElement(X, { control: s }))));
}, wa = ({ controlId: l }) => {
  const t = G(), n = t.title ?? "", a = t.leading, o = t.trailing, u = t.children ?? [], s = t.actions ?? [], i = t.variant ?? "flat", c = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    i === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: c }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(X, { control: a })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), u.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, u.map((d, m) => /* @__PURE__ */ e.createElement(X, { key: m, control: d }))), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((d, m) => /* @__PURE__ */ e.createElement(X, { key: m, control: d }))), o && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__trailing" }, /* @__PURE__ */ e.createElement(X, { control: o })));
}, { useCallback: ka } = e, Na = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.items ?? [], o = ka((u) => {
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
}, { useCallback: Sa } = e, Da = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.items ?? [], o = t.activeItemId, u = Sa((s) => {
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
}, { useCallback: rn, useRef: Ta } = e, Ra = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), La = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.open === !0, o = t.closeOnBackdrop !== !1, u = t.child, s = Ta(null), i = rn(() => {
    n("close");
  }, [n]), r = rn((c) => {
    o && c.target === c.currentTarget && i();
  }, [o, i]);
  return a ? /* @__PURE__ */ e.createElement(Kt, null, /* @__PURE__ */ e.createElement(Ra, { onClose: i }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: r,
      ref: s,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(Pe, null, /* @__PURE__ */ e.createElement(X, { control: u }))
  )) : null;
}, { useEffect: xa, useRef: Ma } = e, Ia = ({ controlId: l }) => {
  const n = G().dialogs ?? [], a = Ma(n.length);
  return xa(() => {
    n.length < a.current && n.length > 0, a.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((o) => /* @__PURE__ */ e.createElement(X, { key: o.controlId, control: o })));
}, { useCallback: it, useRef: We, useState: ut } = e, ja = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), Pa = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Ba = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Aa = ({ controlId: l }) => {
  const t = G(), n = ne(), a = ue(Pa), o = t.title ?? "", u = t.width ?? "32rem", s = t.height ?? null, i = t.minHeight ?? null, r = t.resizable === !0, c = t.child, d = t.actions ?? [], m = t.toolbar, p = t.buttonBar, [h, g] = ut(null), [N, C] = ut(null), [_, y] = ut(null), k = We(null), [x, E] = ut(!1), w = We(null), b = We(null), D = We(null), j = We(null), R = We(null), H = it(() => {
    n("close");
  }, [n]);
  Yt(!0, j, "field");
  const K = it((I, L) => {
    L.preventDefault();
    const S = j.current;
    if (!S) return;
    const $ = S.getBoundingClientRect(), f = !k.current, M = k.current ?? { x: $.left, y: $.top };
    f && (k.current = M, y(M)), R.current = {
      dir: I,
      startX: L.clientX,
      startY: L.clientY,
      startW: $.width,
      startH: $.height,
      startPos: { ...M },
      symmetric: f
    };
    const Y = (Z) => {
      const F = R.current;
      if (!F) return;
      const te = Z.clientX - F.startX, se = Z.clientY - F.startY;
      let le = F.startW, pe = F.startH, ye = 0, we = 0;
      F.symmetric ? (F.dir.includes("e") && (le = F.startW + 2 * te), F.dir.includes("w") && (le = F.startW - 2 * te), F.dir.includes("s") && (pe = F.startH + 2 * se), F.dir.includes("n") && (pe = F.startH - 2 * se)) : (F.dir.includes("e") && (le = F.startW + te), F.dir.includes("w") && (le = F.startW - te, ye = te), F.dir.includes("s") && (pe = F.startH + se), F.dir.includes("n") && (pe = F.startH - se, we = se));
      const Te = Math.max(200, le), Re = Math.max(100, pe);
      F.symmetric ? (ye = (F.startW - Te) / 2, we = (F.startH - Re) / 2) : (F.dir.includes("w") && Te === 200 && (ye = F.startW - 200), F.dir.includes("n") && Re === 100 && (we = F.startH - 100)), b.current = Te, D.current = Re, g(Te), C(Re);
      const $e = {
        x: F.startPos.x + ye,
        y: F.startPos.y + we
      };
      k.current = $e, y($e);
    }, U = () => {
      document.removeEventListener("mousemove", Y), document.removeEventListener("mouseup", U);
      const Z = b.current, F = D.current;
      (Z != null || F != null) && n("resize", {
        ...Z != null ? { width: Math.round(Z) } : {},
        ...F != null ? { height: Math.round(F) } : {}
      }), R.current = null;
    };
    document.addEventListener("mousemove", Y), document.addEventListener("mouseup", U);
  }, [n]), A = it((I) => {
    if (I.button !== 0 || I.target.closest("button")) return;
    I.preventDefault();
    const L = j.current;
    if (!L) return;
    const S = L.getBoundingClientRect(), $ = k.current ?? { x: S.left, y: S.top }, f = I.clientX - $.x, M = I.clientY - $.y, Y = (Z) => {
      const F = window.innerWidth, te = window.innerHeight;
      let se = Z.clientX - f, le = Z.clientY - M;
      const pe = L.offsetWidth, ye = L.offsetHeight;
      se + pe > F && (se = F - pe), le + ye > te && (le = te - ye), se < 0 && (se = 0), le < 0 && (le = 0);
      const we = { x: se, y: le };
      k.current = we, y(we);
    }, U = () => {
      document.removeEventListener("mousemove", Y), document.removeEventListener("mouseup", U);
    };
    document.addEventListener("mousemove", Y), document.addEventListener("mouseup", U);
  }, []), V = it(() => {
    var I, L;
    if (x) {
      const S = w.current;
      S && (y(S.x !== -1 ? { x: S.x, y: S.y } : null), g(S.w), C(S.h)), E(!1);
    } else {
      const S = j.current, $ = S == null ? void 0 : S.getBoundingClientRect();
      w.current = {
        x: ((I = k.current) == null ? void 0 : I.x) ?? ($ == null ? void 0 : $.left) ?? -1,
        y: ((L = k.current) == null ? void 0 : L.y) ?? ($ == null ? void 0 : $.top) ?? -1,
        w: h ?? ($ == null ? void 0 : $.width) ?? null,
        h: N ?? null
      }, E(!0), y({ x: 0, y: 0 }), g(null), C(null);
    }
  }, [x, h, N]), B = x ? { position: "absolute", top: 0, left: 0, width: "100vw", maxWidth: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: h != null ? h + "px" : u,
    ...N != null ? { height: N + "px" } : s != null ? { height: s } : {},
    ...i != null && N == null ? { minHeight: i } : {},
    maxHeight: _ ? "100vh" : "80vh",
    ..._ ? { position: "absolute", left: _.x + "px", top: _.y + "px" } : {}
  }, P = l + "-title";
  return /* @__PURE__ */ e.createElement(Kt, { modal: !0 }, /* @__PURE__ */ e.createElement(ja, { onClose: H }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: B,
      ref: j,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": P
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${x ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: x ? void 0 : A,
        onDoubleClick: r ? V : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: P }, o),
      m && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(X, { control: m })),
      r && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: V,
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
          onClick: H,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(Pe, null, /* @__PURE__ */ e.createElement(X, { control: c }))),
    (d.length > 0 || p) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, p && /* @__PURE__ */ e.createElement(X, { control: p }), d.map((I, L) => /* @__PURE__ */ e.createElement(X, { key: L, control: I }))),
    r && !x && Ba.map((I) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: I,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${I}`,
        onMouseDown: (L) => K(I, L)
      }
    ))
  ));
}, { useCallback: Fa } = e, Oa = {
  "js.drawer.close": "Close"
}, $a = ({ controlId: l }) => {
  const t = G(), n = ne(), a = ue(Oa), o = t.open === !0, u = t.position ?? "right", s = t.size ?? "medium", i = t.title ?? null, r = t.child, c = Fa(() => {
    n("close");
  }, [n]);
  Oe(o, { ESCAPE: c });
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, /* @__PURE__ */ e.createElement(Pe, null, r && /* @__PURE__ */ e.createElement(X, { control: r }))));
}, { useCallback: dt, useRef: Ha } = e, Wa = ({ controlId: l }) => {
  const t = G(), n = ne(), a = Ha(null), o = t.child, s = (t.trigger ?? "contextmenu") === "click", i = dt((m) => {
    m.preventDefault(), m.stopPropagation(), n("openMenu", { x: m.clientX, y: m.clientY });
  }, [n]), r = dt(() => {
    var p;
    const m = (p = a.current) == null ? void 0 : p.getBoundingClientRect();
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
      onContextMenu: s ? void 0 : i,
      onClick: s ? c : void 0,
      role: s ? "button" : void 0,
      tabIndex: s ? 0 : void 0,
      "aria-haspopup": s ? "menu" : void 0,
      onKeyDown: s ? d : void 0
    },
    o && /* @__PURE__ */ e.createElement(X, { control: o })
  );
}, { useCallback: Ua, useEffect: on, useRef: za, useState: sn } = e, Va = 250, Ka = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.message ?? "", o = t.content ?? "", u = t.variant ?? "info", s = t.duration ?? 5e3, i = t.visible === !0, r = t.generation ?? 0, [c, d] = sn(!1), [m, p] = sn(!1), h = za(!1);
  on(() => {
    h.current = !1;
  }, [r]);
  const g = Ua(() => {
    d(!0), setTimeout(() => {
      n("dismiss", { generation: r }), d(!1);
    }, 200);
  }, [n, r]);
  return on(() => {
    if (!i || s === 0 || m) return;
    const N = setTimeout(g, h.current ? Va : s);
    return () => clearTimeout(N);
  }, [i, s, m, g]), !i && !c ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${u}${c ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite",
      onMouseEnter: () => {
        h.current = !0, p(!0);
      },
      onMouseLeave: () => p(!1)
    },
    o ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: o } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, a)
  );
}, { useCallback: Ya, useEffect: cn, useMemo: Ga, useRef: Xa, useState: qa } = e, Za = 1e3;
function Qa(l) {
  const t = Math.max(0, Math.floor(l / 1e3)), n = t % 60, a = Math.floor(t / 60) % 60, o = Math.floor(t / 3600), u = (s) => s < 10 ? `0${s}` : `${s}`;
  return o > 0 ? `${o}:${u(a)}:${u(n)}` : `${a}:${u(n)}`;
}
const Ja = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.visible === !0, o = t.severity ?? "info", u = t.text ?? "", s = t.deadline ?? null, i = t.serverNow ?? null, r = t.leadMs ?? null, c = t.actionLabel ?? null, d = t.pingGraceMs ?? null, m = Ga(
    () => i != null ? i - Date.now() : 0,
    [i]
  ), [p, h] = qa(0), g = a && s != null;
  cn(() => {
    if (!g) return;
    const x = setInterval(() => h((E) => E + 1), Za);
    return () => clearInterval(x);
  }, [g, s]);
  const N = Xa(null);
  cn(() => {
    !g || d == null || s == null || N.current !== s && (Date.now() + m < s + d || (N.current = s, n("deadlinePassed", {})));
  }, [p, g, s, d, m, n]);
  const C = Ya(() => {
    c != null && n("action", {});
  }, [n, c]);
  if (!a) return null;
  const _ = s != null ? s - (Date.now() + m) : null;
  if (r != null && _ != null && _ > r) return null;
  const y = _ != null ? Qa(_) : null, k = c != null;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlNoticeBar tlNoticeBar--${o}${k ? " tlNoticeBar--clickable" : ""}`,
      role: k ? "button" : "status",
      "aria-live": "polite",
      tabIndex: k ? 0 : void 0,
      title: c ?? void 0,
      "aria-label": k ? `${u} ${c}` : void 0,
      onClick: k ? C : void 0,
      onKeyDown: k ? (x) => {
        (x.key === "Enter" || x.key === " ") && (x.preventDefault(), C());
      } : void 0
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__text" }, u),
    y !== null && /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__countdown" }, y)
  );
}, { useCallback: Tt, useEffect: un, useRef: er, useState: dn } = e, tr = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.open === !0, o = t.anchorId, u = t.anchorX, s = t.anchorY, i = t.items ?? [], r = er(null), [c, d] = dn({ top: 0, left: 0 }), [m, p] = dn(0), h = i.filter((_) => _.type === "item" && !_.disabled);
  un(() => {
    var b, D;
    if (!a) return;
    const _ = ((b = r.current) == null ? void 0 : b.offsetHeight) ?? 200, y = ((D = r.current) == null ? void 0 : D.offsetWidth) ?? 200;
    if (u != null && s != null) {
      let j = s, R = u;
      j + _ > window.innerHeight && (j = Math.max(0, window.innerHeight - _)), R + y > window.innerWidth && (R = Math.max(0, window.innerWidth - y)), d({ top: j, left: R }), p(0);
      return;
    }
    if (!o) return;
    const k = document.getElementById(o);
    if (!k) return;
    const x = k.getBoundingClientRect();
    let E = x.bottom + 4, w = x.left;
    E + _ > window.innerHeight && (E = x.top - _ - 4), w + y > window.innerWidth && (w = x.right - y), d({ top: E, left: w }), p(0);
  }, [a, o, u, s]);
  const g = Tt(() => {
    n("close");
  }, [n]), N = Tt((_) => {
    n("selectItem", { itemId: _ });
  }, [n]);
  un(() => {
    if (!a) return;
    const _ = (y) => {
      r.current && !r.current.contains(y.target) && g();
    };
    return document.addEventListener("mousedown", _), () => document.removeEventListener("mousedown", _);
  }, [a, g]);
  const C = Tt((_) => {
    if (_.key === "Escape") {
      _.preventDefault(), g();
      return;
    }
    if (_.key === "ArrowDown")
      _.preventDefault(), p((y) => (y + 1) % h.length);
    else if (_.key === "ArrowUp")
      _.preventDefault(), p((y) => (y - 1 + h.length) % h.length);
    else if (_.key === "Enter" || _.key === " ") {
      _.preventDefault();
      const y = h[m];
      y && N(y.id);
    }
  }, [g, N, h, m]);
  return Yt(a, r), a ? /* @__PURE__ */ e.createElement(
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
}, nr = 768, lr = ({ controlId: l }) => {
  const t = G(), n = ne(), a = Ct(!0);
  e.useEffect(() => {
    const c = window.matchMedia(`(max-width: ${nr}px)`), d = (p) => {
      n("reportDisplayClass", { displayClass: p ? "COMPACT" : "REGULAR" });
    };
    d(c.matches);
    const m = (p) => d(p.matches);
    return c.addEventListener("change", m), () => c.removeEventListener("change", m);
  }, [n]);
  const o = t.header, u = t.notices, s = t.content, i = t.footer, r = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell " + a }, o && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(X, { control: o })), u && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__notices" }, /* @__PURE__ */ e.createElement(X, { control: u })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(Pe, null, /* @__PURE__ */ e.createElement(X, { control: s }))), i && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(X, { control: i })), /* @__PURE__ */ e.createElement(X, { control: r }));
};
function Gt({
  color: l,
  className: t,
  children: n
}) {
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      className: t ? "tlPill " + t : "tlPill",
      style: { "--tlPill-color": l }
    },
    n
  );
}
const ar = ({ controlId: l }) => {
  const t = G(), n = t.text ?? "", a = t.cssClass ?? "", o = t.hasTooltip === !0, u = t.role || void 0, s = t.color || void 0, i = a ? `tlText ${a}` : "tlText";
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: i,
      role: u,
      "data-tooltip": o ? "key:tooltip" : void 0
    },
    s ? /* @__PURE__ */ e.createElement(Gt, { color: s }, n) : n
  );
}, rr = ({ isMulti: l, cursorIndex: t, onMove: n, onToggle: a, onSelectAll: o }) => (me("ArrowUp", () => (n("up", !1, !1), !0)), me("ArrowDown", () => (n("down", !1, !1), !0)), me("Home", () => (n("home", !1, !1), !0)), me("End", () => (n("end", !1, !1), !0)), me("PageUp", () => (n("pageUp", !1, !1), !0)), me("PageDown", () => (n("pageDown", !1, !1), !0)), me("Shift+ArrowUp", () => (n("up", l, !1), !0)), me("Shift+ArrowDown", () => (n("down", l, !1), !0)), me("Shift+Home", () => (n("home", l, !1), !0)), me("Shift+End", () => (n("end", l, !1), !0)), me("Shift+PageUp", () => (n("pageUp", l, !1), !0)), me("Shift+PageDown", () => (n("pageDown", l, !1), !0)), me("Ctrl+ArrowUp", () => (n("up", !1, l), !0)), me("Ctrl+ArrowDown", () => (n("down", !1, l), !0)), me("Space", () => t < 0 ? !1 : (a(), !0)), me("Ctrl+A", () => l ? (o(), !0) : !1), null), or = {
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
}, sr = 300, mn = 50, cr = 'input, textarea, select, button, a, [contenteditable="true"], [role="combobox"], [role="listbox"], [role="option"], [role="button"], [role="link"], [role="checkbox"], [role="radio"], [role="switch"], [role="textbox"], [role="spinbutton"], [role="slider"], [role="menu"], [role="menuitem"]';
function Rt(l) {
  var n;
  const t = l.target;
  return !!((n = t == null ? void 0 : t.closest) != null && n.call(t, cr));
}
const Ht = 'input:not([disabled]):not([readonly]), textarea:not([disabled]):not([readonly]), select:not([disabled]), [contenteditable="true"]', ir = Ht + ", button:not([disabled]), a[href]";
function Nn(l, t) {
  return Array.from(l.querySelectorAll("[data-row][data-col]")).filter((n) => n.dataset.row === t);
}
function Lt(l, t, n = {}) {
  const a = Nn(l, t);
  if (n.col) {
    const u = a.find((i) => i.dataset.col === n.col), s = u == null ? void 0 : u.querySelector(Ht);
    if (s) return s;
  }
  if (n.col)
    return null;
  const o = n.last ? [...a].reverse() : a;
  for (const u of o) {
    const s = u.querySelector(Ht);
    if (s) return s;
  }
  return null;
}
const ur = ({ controlId: l }) => {
  const t = G(), n = ne(), a = ue(or), o = e.useRef(null);
  e.useEffect(() => {
    const v = o.current;
    if (!v) return;
    const T = (z) => {
      const Q = z.detail;
      let ee = Q.target;
      for (; ee && ee !== v; ) {
        const re = ee.dataset.row, oe = ee.dataset.col;
        if (re != null && oe != null) {
          Q.resolved = { key: re + "|" + oe };
          return;
        }
        ee = ee.parentElement;
      }
    };
    return v.addEventListener("tl-tooltip-resolve", T), () => v.removeEventListener("tl-tooltip-resolve", T);
  }, []);
  const u = t.columns ?? [], s = t.totalRowCount ?? 0, i = t.rows ?? [], r = t.rowHeight ?? 36, c = t.selectionMode ?? "single", d = t.selectedCount ?? 0, m = t.cursorIndex ?? -1, p = t.frozenColumnCount ?? 0, h = t.treeMode ?? !1, g = t.columnSelect ?? !1, N = t.filterBar ?? !1, C = t.namedFilters ?? [], _ = t.activeNamedFilter ?? "", y = t.search ?? "", k = t.filterSaving ?? !1, x = e.useMemo(
    () => u.filter((v) => v.sortPriority && v.sortPriority > 0).length,
    [u]
  ), E = c === "multi", w = 40, b = 20, D = e.useRef(null), j = e.useRef(null), R = e.useRef(null), H = e.useRef(null), K = e.useRef(null), [A, V] = e.useState({}), B = e.useRef(null), P = e.useRef(!1), I = e.useRef(null), [L, S] = e.useState(null), [$, f] = e.useState(null), [M, Y] = e.useState(null), [U, Z] = e.useState(0);
  e.useEffect(() => {
    const v = R.current;
    if (!v)
      return;
    const T = () => {
      const Q = v.offsetWidth - v.clientWidth;
      Z((ee) => ee === Q ? ee : Q);
    };
    T();
    const z = new ResizeObserver(T);
    return z.observe(v), () => z.disconnect();
  }, []), e.useEffect(() => {
    B.current || V({});
  }, [u]);
  const F = e.useCallback((v) => A[v.name] ?? v.width, [A]), te = e.useMemo(() => {
    const v = [];
    let T = E && p > 0 ? w : 0;
    for (let z = 0; z < p && z < u.length; z++)
      v.push(T), T += F(u[z]);
    return v;
  }, [u, p, E, w, F]), se = e.useMemo(() => {
    if (p <= 0)
      return 0;
    let v = E ? w : 0;
    for (let T = 0; T < p && T < u.length; T++)
      v += F(u[T]);
    return v;
  }, [u, p, E, w, F]), le = s * r, pe = e.useRef(null), ye = e.useCallback((v, T, z) => {
    z.preventDefault(), z.stopPropagation(), B.current = { column: v, startX: z.clientX, startWidth: T };
    let Q = z.clientX, ee = 0;
    const re = () => {
      const ce = B.current;
      if (!ce) return;
      const de = Math.max(mn, ce.startWidth + (Q - ce.startX) + ee);
      V((_e) => ({ ..._e, [ce.column]: de }));
    }, oe = () => {
      const ce = R.current, de = D.current;
      if (!ce || !B.current) return;
      const _e = ce.getBoundingClientRect(), xe = 40, Jt = 8, Qn = ce.scrollLeft;
      Q > _e.right - xe ? ce.scrollLeft += Jt : Q < _e.left + xe && (ce.scrollLeft = Math.max(0, ce.scrollLeft - Jt));
      const en = ce.scrollLeft - Qn;
      en !== 0 && (de && (de.scrollLeft = ce.scrollLeft), ee += en, re()), pe.current = requestAnimationFrame(oe);
    };
    pe.current = requestAnimationFrame(oe);
    const he = (ce) => {
      Q = ce.clientX, re();
    }, fe = (ce) => {
      document.removeEventListener("mousemove", he), document.removeEventListener("mouseup", fe), pe.current !== null && (cancelAnimationFrame(pe.current), pe.current = null);
      const de = B.current;
      if (de) {
        const _e = Math.max(mn, de.startWidth + (ce.clientX - de.startX) + ee);
        n("columnResize", { column: de.column, width: _e }), B.current = null, P.current = !0, requestAnimationFrame(() => {
          P.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", he), document.addEventListener("mouseup", fe);
  }, [n]), we = e.useCallback(() => {
    D.current && R.current && (D.current.scrollLeft = R.current.scrollLeft), H.current !== null && clearTimeout(H.current), H.current = window.setTimeout(() => {
      const v = R.current;
      if (!v) return;
      const T = v.scrollTop, z = Math.ceil(v.clientHeight / r), Q = Math.floor(T / r);
      n("scroll", { start: Q, count: z });
    }, 80);
  }, [n, r]), Te = e.useCallback((v, T, z) => {
    if (P.current) return;
    let Q;
    !T || T === "desc" ? Q = "asc" : Q = "desc";
    const ee = z.shiftKey ? "add" : "replace";
    n("sort", { column: v, direction: Q, mode: ee });
  }, [n]), Re = e.useCallback((v, T) => {
    I.current = v, T.dataTransfer.effectAllowed = "move", T.dataTransfer.setData("text/plain", v);
  }, []), $e = e.useCallback((v, T) => {
    if (!I.current || I.current === v) {
      S(null);
      return;
    }
    T.preventDefault(), T.dataTransfer.dropEffect = "move";
    const z = T.currentTarget.getBoundingClientRect(), Q = T.clientX < z.left + z.width / 2 ? "left" : "right";
    S({ column: v, side: Q });
  }, []), He = e.useCallback((v) => {
    v.preventDefault(), v.stopPropagation();
    const T = I.current;
    if (!T || !L) {
      I.current = null, S(null);
      return;
    }
    let z = u.findIndex((ee) => ee.name === L.column);
    if (z < 0) {
      I.current = null, S(null);
      return;
    }
    const Q = u.findIndex((ee) => ee.name === T);
    L.side === "right" && z++, Q < z && z--, n("columnReorder", { column: T, targetIndex: z }), I.current = null, S(null);
  }, [u, L, n]), O = e.useCallback(() => {
    I.current = null, S(null);
  }, []), q = e.useCallback((v, T) => {
    var ee, re, oe, he;
    const z = window.getSelection();
    if (z && !z.isCollapsed && T.currentTarget.contains(z.anchorNode))
      return;
    if (!Rt(T) && ((ee = R.current) == null || ee.focus({ preventScroll: !0 }), !T.ctrlKey && !T.metaKey && !T.shiftKey)) {
      const fe = (he = (oe = (re = T.target) == null ? void 0 : re.closest) == null ? void 0 : oe.call(re, "[data-col]")) == null ? void 0 : he.getAttribute("data-col");
      K.current = { index: v, col: fe ?? void 0 };
    }
    const Q = i.find((fe) => fe.index === v);
    Rt(T) && (Q != null && Q.selected) && !T.ctrlKey && !T.metaKey && !T.shiftKey || n("select", {
      rowIndex: v,
      ctrlKey: T.ctrlKey || T.metaKey,
      shiftKey: T.shiftKey
    });
  }, [n, i]), ae = e.useCallback((v, T, z) => {
    n("moveSelection", { direction: v, extend: T, move: z });
  }, [n]), ie = e.useCallback(() => {
    m < 0 || n("select", { rowIndex: m, ctrlKey: E, shiftKey: !1 });
  }, [n, m, E]), qe = e.useCallback(() => {
    n("selectAll", { selected: !0 });
  }, [n]), In = e.useCallback(
    () => !!o.current && o.current.contains(document.activeElement),
    []
  );
  e.useEffect(() => {
    if (m < 0)
      return;
    const v = R.current;
    if (!v)
      return;
    const T = m * r, z = T + r;
    T < v.scrollTop ? v.scrollTop = T : z > v.scrollTop + v.clientHeight && (v.scrollTop = z - v.clientHeight);
  }, [m, r]), e.useEffect(() => {
    const v = K.current, T = R.current;
    if (!v || !T)
      return;
    const z = i.find((re) => re.index === v.index);
    if (!z || !Lt(T, z.id))
      return;
    K.current = null;
    const Q = document.activeElement;
    if (Q && Q !== document.body && !T.contains(Q))
      return;
    const ee = Lt(T, z.id, { col: v.col, last: v.last });
    ee && (ee.focus({ preventScroll: !0 }), ee instanceof HTMLInputElement && ee.select());
  }, [i]);
  const jn = e.useCallback((v) => {
    if (v.key !== "Tab")
      return;
    const T = R.current, z = document.activeElement;
    if (!T || !z || !T.contains(z))
      return;
    const Q = z.closest("[data-row][data-col]");
    if (!Q)
      return;
    const ee = Q.dataset.row, re = i.find((xe) => xe.id === ee);
    if (!re)
      return;
    const oe = Nn(T, ee).flatMap((xe) => Array.from(xe.querySelectorAll(ir))), he = oe.indexOf(z);
    if (he < 0)
      return;
    const fe = !v.shiftKey;
    if (!(fe ? he === oe.length - 1 : he === 0))
      return;
    const de = fe ? re.index + 1 : re.index - 1;
    if (de < 0 || de >= s)
      return;
    const _e = i.find((xe) => xe.index === de);
    _e && Lt(T, _e.id) || (v.preventDefault(), K.current = { index: de, last: !fe }, n("select", { rowIndex: de, ctrlKey: !1, shiftKey: !1 }));
  }, [i, s, n]), Pn = e.useCallback((v, T) => {
    T.stopPropagation(), n("select", { rowIndex: v, ctrlKey: !0, shiftKey: !1 });
  }, [n]), Bn = e.useCallback(() => {
    const v = d === s && s > 0;
    n("selectAll", { selected: !v });
  }, [n, d, s]), An = e.useCallback((v, T, z) => {
    z.stopPropagation(), n("expand", { rowIndex: v, expanded: T });
  }, [n]), Fn = e.useCallback((v, T) => {
    T.preventDefault(), f({ x: T.clientX, y: T.clientY, colIdx: v });
  }, []), On = e.useCallback(() => {
    $ && (n("setFrozenColumnCount", { count: $.colIdx + 1 }), f(null));
  }, [$, n]), $n = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), f(null);
  }, [n]), Hn = e.useCallback((v) => {
    v.preventDefault(), v.stopPropagation();
    const T = j.current, z = D.current;
    if (!T || !z)
      return;
    const Q = T.clientWidth, ee = [{ x: 0, count: 0 }];
    z.querySelectorAll("[data-col-idx]").forEach((fe) => {
      const ce = fe.getBoundingClientRect().right - T.getBoundingClientRect().left;
      ce > 0 && ce <= Q && ee.push({ x: ce, count: Number(fe.dataset.colIdx) + 1 });
    });
    let re = { x: se, count: p };
    const oe = (fe) => {
      const ce = fe.clientX - T.getBoundingClientRect().left;
      re = ee.reduce(
        (de, _e) => Math.abs(_e.x - ce) < Math.abs(de.x - ce) ? _e : de,
        ee[0]
      ), Y(re);
    }, he = () => {
      document.removeEventListener("mousemove", oe), document.removeEventListener("mouseup", he), Y(null), re.count !== p && n("setFrozenColumnCount", { count: re.count });
    };
    document.addEventListener("mousemove", oe), document.addEventListener("mouseup", he);
  }, [se, p, n]);
  e.useEffect(() => {
    if (!$) return;
    const v = () => f(null);
    return document.addEventListener("mousedown", v), () => document.removeEventListener("mousedown", v);
  }, [$]), Oe(!!$, { ESCAPE: () => f(null) });
  const Wn = e.useCallback((v, T) => {
    T.stopPropagation(), T.preventDefault(), n("openFilter", { column: v });
  }, [n]), Un = e.useCallback((v) => {
    v.stopPropagation(), v.preventDefault(), n("openColumnSelect", {});
  }, [n]), [zn, Zt] = e.useState(y), yt = e.useRef(!1), Le = e.useRef(null);
  e.useEffect(() => {
    yt.current || Zt(y);
  }, [y]), e.useEffect(() => () => {
    Le.current !== null && clearTimeout(Le.current);
  }, []);
  const st = e.useCallback((v) => {
    Le.current !== null && (clearTimeout(Le.current), Le.current = null), yt.current = !1, n("search", { term: v });
  }, [n]), Vn = e.useCallback((v) => {
    Zt(v), yt.current = !0, Le.current !== null && clearTimeout(Le.current), Le.current = window.setTimeout(() => st(v), sr);
  }, [st]), Kn = e.useCallback((v) => {
    v.key === "Enter" && (v.preventDefault(), st(v.currentTarget.value));
  }, [st]), Yn = e.useCallback((v) => {
    v === _ ? n("clearFilter", {}) : n("applyNamedFilter", { id: v });
  }, [_, n]), Gn = e.useCallback((v, T) => {
    T.stopPropagation(), n("deleteNamedFilter", { id: v });
  }, [n]), [Ze, Qe] = e.useState(null), wt = e.useCallback(() => {
    const v = (Ze ?? "").trim();
    v && (n("saveNamedFilter", { filterName: v }), Qe(null));
  }, [Ze, n]), Xn = e.useCallback((v) => {
    v.key === "Enter" ? (v.preventDefault(), wt()) : v.key === "Escape" && (v.preventDefault(), Qe(null));
  }, [wt]), kt = u.reduce((v, T) => v + F(T), 0) + (E ? w : 0), Nt = g ? 32 : 0, qn = d === s && s > 0, Qt = d > 0 && d < s, Zn = e.useCallback((v) => {
    v && (v.indeterminate = Qt);
  }, [Qt]);
  return /* @__PURE__ */ e.createElement(Kt, { active: In }, /* @__PURE__ */ e.createElement(
    rr,
    {
      isMulti: E,
      cursorIndex: m,
      onMove: ae,
      onToggle: ie,
      onSelectAll: qe
    }
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: o,
      id: l,
      className: "tlTableView",
      "data-tooltip": "dynamic",
      onDragOver: (v) => {
        if (!I.current) return;
        v.preventDefault();
        const T = R.current, z = D.current;
        if (!T) return;
        const Q = T.getBoundingClientRect(), ee = 40, re = 8;
        v.clientX < Q.left + ee ? T.scrollLeft = Math.max(0, T.scrollLeft - re) : v.clientX > Q.right - ee && (T.scrollLeft += re), z && (z.scrollLeft = T.scrollLeft);
      },
      onDrop: He
    },
    N && /* @__PURE__ */ e.createElement("div", { className: "tlTableView__filterBar" }, C.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlTableView__filterChips" }, C.map((v) => {
      const T = v.id === _;
      return /* @__PURE__ */ e.createElement(
        "span",
        {
          key: v.id,
          className: "tlTableView__chip" + (T ? " tlTableView__chip--active" : "")
        },
        /* @__PURE__ */ e.createElement(
          "button",
          {
            type: "button",
            className: "tlTableView__chipLabel",
            "aria-pressed": T,
            title: T ? a["js.table.clearFilter"] : v.label,
            onClick: () => Yn(v.id)
          },
          v.label
        ),
        v.deletable && /* @__PURE__ */ e.createElement(
          "button",
          {
            type: "button",
            className: "tlTableView__chipRemove",
            title: a["js.table.deleteFilter"],
            "aria-label": a["js.table.deleteFilter"],
            onClick: (z) => Gn(v.id, z)
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
        value: zn,
        onChange: (v) => Vn(v.target.value),
        onKeyDown: Kn
      }
    )), k && (Ze === null ? /* @__PURE__ */ e.createElement(
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
        onChange: (v) => Qe(v.target.value),
        onKeyDown: Xn
      }
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__barButton",
        title: a["js.table.saveFilter"],
        "aria-label": a["js.table.saveFilter"],
        disabled: !Ze.trim(),
        onClick: wt
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
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerArea", ref: j }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: D }, /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerRow",
        style: { width: kt, paddingRight: Nt + U }
      },
      E && /* @__PURE__ */ e.createElement(
        "div",
        {
          className: "tlTableView__headerCell tlTableView__checkboxCell" + (p > 0 ? " tlTableView__headerCell--frozen" : ""),
          style: {
            width: w,
            minWidth: w,
            ...p > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
          },
          onDragOver: (v) => {
            I.current && (v.preventDefault(), v.dataTransfer.dropEffect = "move", u.length > 0 && u[0].name !== I.current && S({ column: u[0].name, side: "left" }));
          }
        },
        /* @__PURE__ */ e.createElement(
          "input",
          {
            type: "checkbox",
            ref: Zn,
            className: "tlTableView__checkbox",
            checked: qn,
            onChange: Bn
          }
        )
      ),
      u.map((v, T) => {
        const z = F(v);
        u.length - 1;
        let Q = "tlTableView__headerCell";
        v.sortable && (Q += " tlTableView__headerCell--sortable"), L && L.column === v.name && (Q += " tlTableView__headerCell--dragOver-" + L.side);
        const ee = T < p, re = T === p - 1;
        return ee && (Q += " tlTableView__headerCell--frozen"), re && (Q += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
          "div",
          {
            key: v.name,
            className: Q,
            "data-col-idx": T,
            style: {
              width: z,
              minWidth: z,
              position: ee ? "sticky" : "relative",
              ...ee ? { left: te[T], zIndex: 2 } : {}
            },
            draggable: !0,
            onClick: v.sortable ? (oe) => Te(v.name, v.sortDirection, oe) : void 0,
            onContextMenu: (oe) => Fn(T, oe),
            onDragStart: (oe) => Re(v.name, oe),
            onDragOver: (oe) => $e(v.name, oe),
            onDrop: He,
            onDragEnd: O
          },
          /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, v.label),
          v.filterable && /* @__PURE__ */ e.createElement(
            "button",
            {
              type: "button",
              className: "tlTableView__filterButton" + (v.filterActive ? " tlTableView__filterButton--active" : ""),
              title: a["js.table.filter"],
              style: {
                border: "none",
                background: "transparent",
                cursor: "pointer",
                padding: "0 4px",
                color: v.filterActive ? "#1565c0" : "inherit"
              },
              onMouseDown: (oe) => oe.stopPropagation(),
              onClick: (oe) => Wn(v.name, oe)
            },
            /* @__PURE__ */ e.createElement("i", { className: v.filterActive ? "bi bi-funnel-fill" : "bi bi-funnel" })
          ),
          v.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, v.sortDirection === "asc" ? "▲" : "▼", x > 1 && v.sortPriority != null && v.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, v.sortPriority)),
          /* @__PURE__ */ e.createElement(
            "div",
            {
              className: "tlTableView__resizeHandle",
              onMouseDown: (oe) => ye(v.name, z, oe)
            }
          )
        );
      }),
      /* @__PURE__ */ e.createElement(
        "div",
        {
          style: { flex: "0 0 0", minHeight: "100%" },
          onDragOver: (v) => {
            if (I.current && u.length > 0) {
              const T = u[u.length - 1];
              T.name !== I.current && (v.preventDefault(), v.dataTransfer.dropEffect = "move", S({ column: T.name, side: "right" }));
            }
          },
          onDrop: He
        }
      )
    )), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__frozenSplitter" + (M ? " tlTableView__frozenSplitter--active" : ""),
        style: { left: se },
        title: a["js.table.freezeSplitter"],
        onMouseDown: Hn
      }
    ), g && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__columnsButton",
        title: a["js.table.columns"],
        "aria-label": a["js.table.columns"],
        onClick: Un
      },
      /* @__PURE__ */ e.createElement("i", { className: "bi bi-gear" })
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: R,
        className: "tlTableView__body",
        onScroll: we,
        onKeyDown: jn,
        tabIndex: 0
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: le, position: "relative", width: kt, paddingRight: Nt } }, i.map((v) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: v.id,
          className: "tlTableView__row" + (v.selected ? " tlTableView__row--selected" : "") + (v.index === m ? " tlTableView__row--cursor" : ""),
          style: {
            position: "absolute",
            top: v.index * r,
            height: r,
            width: kt,
            paddingRight: Nt,
            ...v.index === m ? { outline: "2px solid var(--color-primary, #1a73e8)", outlineOffset: "-2px" } : {}
          },
          onMouseDown: (T) => {
            (T.shiftKey || T.ctrlKey || T.metaKey || T.detail > 1) && !Rt(T) && T.preventDefault();
          },
          onClick: (T) => q(v.index, T)
        },
        E && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (p > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: w,
              minWidth: w,
              ...p > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (T) => T.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: v.selected,
              onChange: () => {
              },
              onClick: (T) => Pn(v.index, T),
              tabIndex: -1
            }
          )
        ),
        u.map((T, z) => {
          const Q = F(T), ee = z === u.length - 1, re = z < p, oe = z === p - 1;
          let he = "tlTableView__cell";
          re && (he += " tlTableView__cell--frozen"), oe && (he += " tlTableView__cell--frozenLast");
          const fe = h && z === 0, ce = v.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: T.name,
              className: he,
              "data-row": v.id,
              "data-col": T.name,
              style: {
                ...ee && !re ? { flex: "1 0 auto", minWidth: Q } : { width: Q, minWidth: Q },
                ...re ? { position: "sticky", left: te[z], zIndex: 2 } : {}
              }
            },
            fe ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ce * b } }, v.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (de) => An(v.index, !v.expanded, de)
              },
              v.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), v.cells[T.name] && /* @__PURE__ */ e.createElement(X, { control: v.cells[T.name] })) : v.cells[T.name] && /* @__PURE__ */ e.createElement(X, { control: v.cells[T.name] })
          );
        })
      )))
    ),
    M && /* @__PURE__ */ e.createElement("div", { className: "tlTableView__frozenPreview", style: { left: M.x } }),
    $ && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: $.y, left: $.x, zIndex: 1e4 },
        onMouseDown: (v) => v.stopPropagation()
      },
      $.colIdx + 1 !== p && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: On }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      p > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: $n }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
    )
  ));
}, dr = {
  "js.table.columnSearch": "Find column"
}, mr = ({ controlId: l }) => {
  const t = G(), n = ne(), a = ue(dr), o = t.entries ?? [], u = o.filter((E) => E.visible).length, [s, i] = e.useState(""), r = s.trim().toLowerCase(), c = r ? o.filter((E) => E.label.toLowerCase().includes(r)) : o, d = e.useRef(null), m = e.useRef(null), [p, h] = e.useState(null), g = e.useCallback((E) => {
    m.current = E, h(E);
  }, []), N = e.useCallback((E, w) => {
    n("columnVisible", { column: E, visible: w });
  }, [n]), C = e.useCallback((E, w) => {
    d.current = E, w.dataTransfer.effectAllowed = "move", w.dataTransfer.setData("text/plain", E);
  }, []), _ = e.useCallback((E, w) => {
    if (!d.current || d.current === E) {
      g(null);
      return;
    }
    w.preventDefault(), w.dataTransfer.dropEffect = "move";
    const b = w.currentTarget.getBoundingClientRect(), D = w.clientY < b.top + b.height / 2 ? "top" : "bottom";
    g({ name: E, side: D });
  }, [g]), y = e.useCallback(() => {
    d.current = null, g(null);
  }, [g]), k = e.useCallback((E) => {
    E.preventDefault();
    const w = d.current, b = m.current;
    if (d.current = null, g(null), !w || !b)
      return;
    const D = o.findIndex((H) => H.name === b.name), j = o.findIndex((H) => H.name === w);
    if (D < 0 || j < 0)
      return;
    let R = b.side === "top" ? D : D + 1;
    j < R && R--, R !== j && n("columnReorder", { column: w, targetIndex: R });
  }, [o, n, g]), x = o.length > 10;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlColumnSelect", onDrop: k }, x && /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__search" }, /* @__PURE__ */ e.createElement("i", { className: "bi bi-search", "aria-hidden": "true" }), /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "search",
      className: "tlColumnSelect__searchInput",
      placeholder: a["js.table.columnSearch"],
      "aria-label": a["js.table.columnSearch"],
      value: s,
      onChange: (E) => i(E.target.value)
    }
  )), /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__list" + (x ? " tlColumnSelect__list--fixed" : "") }, c.map((E) => {
    const w = E.visible && u <= 1;
    let b = "tlColumnSelect__row";
    return p && p.name === E.name && (b += " tlColumnSelect__row--dragOver-" + p.side), /* @__PURE__ */ e.createElement(
      "div",
      {
        key: E.name,
        className: b,
        draggable: !0,
        onDragStart: (D) => C(E.name, D),
        onDragOver: (D) => _(E.name, D),
        onDrop: k,
        onDragEnd: y
      },
      /* @__PURE__ */ e.createElement("i", { className: "tlColumnSelect__handle bi bi-grip-vertical", "aria-hidden": "true" }),
      /* @__PURE__ */ e.createElement("label", { className: "tlColumnSelect__label" }, /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          className: "tlReactCheckbox",
          checked: E.visible,
          disabled: w,
          onChange: (D) => N(E.name, D.target.checked)
        }
      ), /* @__PURE__ */ e.createElement("span", null, E.label))
    );
  })));
}, { useState: Wt, useRef: lt, useCallback: bt, useMemo: Ae, useEffect: pn } = e, pr = {
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
}, ve = 44, Et = 15, Ce = 6e4, fr = 36e5, je = 864e5, hr = 8;
function Se(l) {
  const t = new Date(l);
  return t.setHours(0, 0, 0, 0), t.getTime();
}
function Ke(l, t) {
  const n = new Date(l);
  return n.setDate(n.getDate() + t), n.getTime();
}
function br(l) {
  return Se(l);
}
function at(l, t) {
  return Se(l) === Se(t);
}
function Ie(l) {
  return (l - Se(l)) / Ce;
}
function Je(l) {
  return Math.round(l / Et) * Et;
}
function et(l, t, n) {
  return Math.max(t, Math.min(n, l));
}
function vt(l) {
  if (!l)
    return "tlCalEvent--default";
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return "tlCalEvent--c" + Math.abs(t) % hr;
}
function _t(l, t) {
  return l.color ? { ...t, "--cal-ev-bg": l.color } : t;
}
function gr(l) {
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
function Er(l, t) {
  const n = { hour: "numeric", minute: "2-digit" };
  return Fe(l, n, t.start) + "–" + Fe(l, n, t.end);
}
const vr = [
  { key: "DAY", label: "js.calendar.day" },
  { key: "WORK_WEEK", label: "js.calendar.workWeek" },
  { key: "WEEK", label: "js.calendar.week" },
  { key: "MONTH", label: "js.calendar.month" },
  { key: "YEAR", label: "js.calendar.year" }
], _r = ({ title: l, granularity: t, i18n: n, send: a }) => /* @__PURE__ */ e.createElement("div", { className: "tlCalToolbar" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalNav" }, /* @__PURE__ */ e.createElement("button", { className: "tlCalBtn", onClick: () => a("navigate", { direction: "TODAY" }) }, n["js.calendar.today"]), /* @__PURE__ */ e.createElement(
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
)), /* @__PURE__ */ e.createElement("div", { className: "tlCalTitle" }, l), /* @__PURE__ */ e.createElement("div", { className: "tlCalGranularity" }, vr.map((o) => /* @__PURE__ */ e.createElement(
  "button",
  {
    key: o.key,
    className: "tlCalBtn" + (o.key === t ? " tlCalBtn--active" : ""),
    onClick: () => a("switchGranularity", { granularity: o.key })
  },
  n[o.label]
))));
function Cr(l) {
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
      topMin: Ie(s.start),
      botMin: Ie(s.start) + Math.max(15, (s.end - s.start) / Ce),
      col: r,
      cols: 1
    }), o = Math.max(o, s.end);
  }
  return a.length > 0 && u(), n;
}
const xt = (l) => {
  l.preventDefault(), l.currentTarget.setPointerCapture(l.pointerId);
}, Ut = ({ className: l, placeholder: t, style: n, onCommit: a, onDiscard: o }) => {
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
}, Sn = (l) => {
  const [t, n] = Wt(null), a = lt(null);
  a.current = t;
  const o = bt((i) => n(i), []), u = bt(() => n(null), []), s = bt(
    (i) => {
      const r = a.current;
      r && l("createSlot", { ...r, title: i }), n(null);
    },
    [l]
  );
  return { pending: t, open: o, commit: s, discard: u };
}, yr = ({
  ctx: l,
  rangeStart: t,
  granularity: n
}) => {
  const { events: a, locale: o, nonWorkingDays: u, dayStartHour: s, dayEndHour: i, now: r, send: c, editable: d, i18n: m } = l, p = Ae(() => {
    const B = n === "DAY" ? 1 : 7, P = [];
    for (let I = 0; I < B; I++) {
      const L = Ke(t, I);
      n === "WORK_WEEK" && u.includes(new Date(L).getDay()) || P.push(L);
    }
    return P;
  }, [t, n, u]), h = Sn(c), g = lt(null), N = lt(null), [C, _] = Wt(null), y = lt(null);
  y.current = C;
  const [k, x] = Wt(Date.now());
  pn(() => {
    const B = window.setInterval(() => x(Date.now()), 6e4);
    return () => window.clearInterval(B);
  }, []);
  const E = bt(
    (B, P) => {
      const I = g.current;
      if (!I)
        return { dayIndex: 0, min: 0 };
      const L = I.getBoundingClientRect(), S = L.width / p.length, $ = et(Math.floor((B - L.left) / S), 0, p.length - 1), f = P - L.top + I.scrollTop, M = et(f / ve * 60, 0, 1440);
      return { dayIndex: $, min: M };
    },
    [p.length]
  );
  pn(() => {
    if (!C)
      return;
    const B = (L) => {
      const S = y.current;
      if (!S)
        return;
      const { dayIndex: $, min: f } = E(L.clientX, L.clientY);
      S.mode === "move" ? _({ ...S, dayStart: p[$], startMin: et(Je(f - S.grabMin), 0, 1440 - S.dur) }) : S.mode === "resize" ? _({ ...S, endMin: et(Je(f), S.startMin + Et, 1440) }) : _({ ...S, toMin: et(Je(f), 0, 1440) });
    }, P = () => {
      const L = y.current;
      if (_(null), !!L)
        if (L.mode === "move") {
          const S = L.dayStart + L.startMin * Ce;
          S !== L.origStartMs && c("moveEvent", { eventId: L.id, start: S, end: S + L.dur * Ce });
        } else if (L.mode === "resize") {
          const S = L.dayStart + L.endMin * Ce;
          S !== L.origEndMs && c("resizeEvent", { eventId: L.id, end: S });
        } else {
          const S = Math.min(L.fromMin, L.toMin), $ = Math.max(L.fromMin, L.toMin);
          $ - S >= Et && h.open({ start: L.dayStart + S * Ce, end: L.dayStart + $ * Ce, allDay: !1 });
        }
    }, I = () => _(null);
    return window.addEventListener("pointermove", B), window.addEventListener("pointerup", P, { once: !0 }), window.addEventListener("pointercancel", I), () => {
      window.removeEventListener("pointermove", B), window.removeEventListener("pointerup", P), window.removeEventListener("pointercancel", I);
    };
  }, [C, p, E, c, h.open]);
  const w = (B, P, I) => {
    if (!d || !P.movable)
      return;
    B.stopPropagation(), xt(B), h.discard();
    const { min: L } = E(B.clientX, B.clientY), S = (P.end - P.start) / Ce;
    _({
      mode: "move",
      id: P.id,
      grabMin: L - Ie(P.start),
      dur: S,
      dayStart: I,
      startMin: Ie(P.start),
      origStartMs: P.start
    });
  }, b = (B, P, I) => {
    !d || !P.resizable || (B.stopPropagation(), xt(B), h.discard(), _({
      mode: "resize",
      id: P.id,
      dayStart: I,
      startMin: Ie(P.start),
      endMin: Ie(P.end),
      origEndMs: P.end
    }));
  }, D = (B, P) => {
    if (!d || B.button !== 0)
      return;
    xt(B), h.discard();
    const { min: I } = E(B.clientX, B.clientY);
    _({ mode: "create", dayStart: P, fromMin: Je(I), toMin: Je(I) });
  }, j = Array.from({ length: 24 }, (B, P) => P), R = Ae(() => {
    if (C === null || !("id" in C))
      return a;
    const B = C;
    return a.map((P) => {
      if (P.id !== B.id)
        return P;
      if (B.mode === "move") {
        const I = B.dayStart + B.startMin * Ce;
        return { ...P, start: I, end: I + B.dur * Ce };
      }
      return { ...P, end: B.dayStart + B.endMin * Ce };
    });
  }, [a, C]), H = Ae(() => p.map(
    (B) => Cr(
      R.filter((P) => !P.allDay && P.start < B + je && P.end > B)
    )
  ), [p, R]), K = Ae(() => p.map((B) => R.filter((P) => P.allDay && P.start < B + je && P.end > B)), [p, R]), A = s * ve, V = i * ve;
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeGrid" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeHeader" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalGutter" }), p.map((B) => {
    const P = u.includes(new Date(B).getDay()), I = at(B, l.now);
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: B,
        className: "tlCalDayHead" + (P ? " tlCalDayHead--nonworking" : "") + (I ? " tlCalDayHead--today" : ""),
        onClick: () => c("goto", { date: B, granularity: "DAY" })
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlCalDayName" }, Fe(o, { weekday: "short" }, B)),
      /* @__PURE__ */ e.createElement("span", { className: "tlCalDayNum" }, new Date(B).getDate())
    );
  })), /* @__PURE__ */ e.createElement("div", { className: "tlCalAllDayRow" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalGutter tlCalAllDayLabel" }, m["js.calendar.allDay"]), p.map((B, P) => /* @__PURE__ */ e.createElement(
    "div",
    {
      key: B,
      className: "tlCalAllDayCell",
      onClick: () => d && h.open({ start: B, end: B + je, allDay: !0 })
    },
    h.pending && h.pending.allDay && h.pending.start === B && /* @__PURE__ */ e.createElement(
      Ut,
      {
        className: "tlCalAllDayEvent tlCalEvent--preview",
        placeholder: m["js.calendar.newEventTitle"],
        onCommit: h.commit,
        onDiscard: h.discard
      }
    ),
    K[P].map((I) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: I.id,
        className: "tlCalAllDayEvent " + vt(I.category) + (I.selected ? " tlCalEvent--selected" : ""),
        style: _t(I),
        title: I.tooltip,
        onClick: (L) => {
          L.stopPropagation(), c("selectEvent", { eventId: I.id });
        }
      },
      I.title
    ))
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlCalScroll", ref: N }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeBody", style: { height: 24 * ve } }, /* @__PURE__ */ e.createElement("div", { className: "tlCalHourAxis" }, j.map((B) => /* @__PURE__ */ e.createElement("div", { key: B, className: "tlCalHourLabel", style: { top: B * ve } }, B === 0 ? "" : Fe(o, { hour: "numeric" }, Se(t) + B * fr)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalColumns", ref: g, style: { gridTemplateColumns: `repeat(${p.length}, 1fr)` } }, p.map((B, P) => {
    const I = u.includes(new Date(B).getDay()), L = C && ("dayStart" in C && C.dayStart === B) ? C : null;
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: B,
        className: "tlCalCol" + (I ? " tlCalCol--nonworking" : ""),
        onPointerDown: (S) => D(S, B)
      },
      j.map((S) => /* @__PURE__ */ e.createElement("div", { key: S, className: "tlCalHourLine", style: { top: S * ve } })),
      /* @__PURE__ */ e.createElement("div", { className: "tlCalWorkBand", style: { top: A, height: V - A } }),
      at(B, k) && /* @__PURE__ */ e.createElement("div", { className: "tlCalNowLine", style: { top: Ie(Date.now()) / 60 * ve } }),
      H[P].map((S) => {
        const $ = C !== null && "id" in C && C.id === S.ev.id, f = S.topMin / 60 * ve, M = (S.botMin - S.topMin) / 60 * ve, Y = 100 / S.cols;
        return /* @__PURE__ */ e.createElement(
          "div",
          {
            key: S.ev.id,
            className: "tlCalEvent " + vt(S.ev.category) + (S.ev.selected ? " tlCalEvent--selected" : "") + ($ ? " tlCalEvent--dragging" : ""),
            style: _t(S.ev, {
              top: f,
              height: M,
              left: `${S.col * Y}%`,
              width: `calc(${Y}% - 2px)`
            }),
            title: S.ev.tooltip,
            onPointerDown: (U) => w(U, S.ev, B),
            onClick: (U) => {
              U.stopPropagation(), c("selectEvent", { eventId: S.ev.id });
            }
          },
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTime" }, Er(o, S.ev)),
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTitle" }, S.ev.title),
          d && S.ev.resizable && /* @__PURE__ */ e.createElement("span", { className: "tlCalResizeHandle", onPointerDown: (U) => b(U, S.ev, B) })
        );
      }),
      h.pending && !h.pending.allDay && Se(h.pending.start) === B && /* @__PURE__ */ e.createElement(
        Ut,
        {
          className: "tlCalEvent tlCalEvent--preview",
          placeholder: m["js.calendar.newEventTitle"],
          style: {
            top: Ie(h.pending.start) / 60 * ve,
            height: (h.pending.end - h.pending.start) / Ce / 60 * ve
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
            top: Math.min(L.fromMin, L.toMin) / 60 * ve,
            height: Math.abs(L.toMin - L.fromMin) / 60 * ve
          }
        }
      )
    );
  })))));
}, wr = 3, kr = ({ ctx: l, rangeStart: t, anchorMonth: n }) => {
  const { events: a, locale: o, nonWorkingDays: u, send: s, editable: i, now: r, i18n: c } = l, d = Sn(s), m = Ae(() => {
    const h = [];
    for (let g = 0; g < 6; g++) {
      const N = [];
      for (let C = 0; C < 7; C++)
        N.push(Ke(t, g * 7 + C));
      h.push(N);
    }
    return h;
  }, [t]), p = (h, g) => {
    h.preventDefault();
    const N = h.dataTransfer.getData("text/plain"), C = a.find((y) => y.id === N);
    if (!C || !i || !C.movable)
      return;
    const _ = g - Se(C.start);
    s("moveEvent", { eventId: N, start: C.start + _, end: C.end + _ });
  };
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalMonth" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthHead" }, m[0].map((h) => /* @__PURE__ */ e.createElement("div", { key: h, className: "tlCalMonthWeekday" }, Fe(o, { weekday: "short" }, h)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthBody" }, m.map((h, g) => {
    const N = h[0], C = Ke(N, 7), _ = a.filter((k) => (k.allDay || k.end - k.start >= je) && k.start < C && k.end > N).sort((k, x) => k.start - x.start).slice(0, 3), y = _.length;
    return /* @__PURE__ */ e.createElement("div", { key: g, className: "tlCalMonthWeek" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthDays" }, h.map((k) => {
      const x = new Date(k).getMonth() === new Date(n).getMonth(), E = u.includes(new Date(k).getDay()), w = at(k, r);
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: k,
          className: "tlCalMonthCell" + (x ? "" : " tlCalMonthCell--other") + (E ? " tlCalMonthCell--nonworking" : ""),
          onDragOver: (b) => b.preventDefault(),
          onDrop: (b) => p(b, k),
          onClick: () => i && d.open({ start: k, end: k + je, allDay: !0 })
        },
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlCalMonthDayNum" + (w ? " tlCalMonthDayNum--today" : ""),
            onClick: (b) => {
              b.stopPropagation(), s("goto", { date: k, granularity: "DAY" });
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
    })), /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthOverlay" }, _.map((k, x) => {
      const E = Math.max(0, Math.floor((Se(Math.max(k.start, N)) - N) / je)), w = Math.min(7, Math.ceil((k.end - N) / je));
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: k.id,
          className: "tlCalMonthBar " + vt(k.category) + (k.selected ? " tlCalEvent--selected" : ""),
          style: _t(k, {
            gridColumn: `${E + 1} / ${Math.max(E + 1, w) + 1}`,
            gridRow: x + 1
          }),
          draggable: i && k.movable,
          onDragStart: (b) => b.dataTransfer.setData("text/plain", k.id),
          title: k.tooltip,
          onClick: (b) => {
            b.stopPropagation(), s("selectEvent", { eventId: k.id });
          }
        },
        k.title
      );
    }), h.map((k, x) => {
      const E = a.filter((D) => !D.allDay && D.end - D.start < je && at(D.start, k)).sort((D, j) => D.start - j.start), w = E.slice(0, wr), b = E.length - w.length;
      return w.map((D, j) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: D.id,
          className: "tlCalChip " + vt(D.category) + (D.selected ? " tlCalEvent--selected" : ""),
          style: _t(D, { gridColumn: x + 1, gridRow: y + 1 + j }),
          draggable: i && D.movable,
          onDragStart: (R) => R.dataTransfer.setData("text/plain", D.id),
          title: D.tooltip,
          onClick: (R) => {
            R.stopPropagation(), s("selectEvent", { eventId: D.id });
          }
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipDot" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipTime" }, Fe(o, { hour: "numeric", minute: "2-digit" }, D.start)),
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipTitle" }, D.title)
      )).concat(
        b > 0 ? [
          /* @__PURE__ */ e.createElement(
            "div",
            {
              key: "more-" + k,
              className: "tlCalMore",
              style: { gridColumn: x + 1, gridRow: y + 1 + w.length },
              onClick: () => s("goto", { date: k, granularity: "DAY" })
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
}, Nr = ({ ctx: l, rangeStart: t }) => {
  const { events: n, locale: a, firstDayOfWeek: o, nonWorkingDays: u, send: s, now: i } = l, r = Ae(() => {
    const p = /* @__PURE__ */ new Set();
    for (const h of n) {
      let g = Se(h.start);
      const N = h.end;
      for (; g < N; )
        p.add(g), g = Ke(g, 1);
    }
    return p;
  }, [n]), c = new Date(t).getFullYear(), d = Array.from({ length: 12 }, (p, h) => new Date(c, h, 1).getTime()), m = Ae(() => {
    const p = new Date(2023, 0, 1);
    return Array.from({ length: 7 }, (h, g) => {
      const N = new Date(p);
      return N.setDate(p.getDate() + (o + g) % 7), new Intl.DateTimeFormat(a, { weekday: "narrow" }).format(N);
    });
  }, [a, o]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalYear" }, d.map((p) => {
    const h = new Date(p), g = Se(Ke(p, -((h.getDay() - o + 7) % 7))), N = Array.from({ length: 42 }, (C, _) => Ke(g, _));
    return /* @__PURE__ */ e.createElement("div", { key: p, className: "tlCalMini" }, /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlCalMiniTitle",
        onClick: () => s("goto", { date: p, granularity: "MONTH" })
      },
      Fe(a, { month: "long" }, p)
    ), /* @__PURE__ */ e.createElement("div", { className: "tlCalMiniGrid" }, m.map((C, _) => /* @__PURE__ */ e.createElement("div", { key: "h" + _, className: "tlCalMiniWd" }, C)), N.map((C) => {
      const _ = new Date(C).getMonth() === h.getMonth(), y = u.includes(new Date(C).getDay()), k = at(C, i), x = r.has(br(C));
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: C,
          className: "tlCalMiniDay" + (_ ? "" : " tlCalMiniDay--other") + (y ? " tlCalMiniDay--nonworking" : "") + (k ? " tlCalMiniDay--today" : "") + (x ? " tlCalMiniDay--event" : ""),
          onClick: () => s("goto", { date: C, granularity: "DAY" })
        },
        new Date(C).getDate()
      );
    })));
  }));
}, Sr = ({ controlId: l }) => {
  const t = G(), n = ne(), a = ue(pr), o = t.granularity ?? "WEEK", u = t.rangeStart ?? Date.now(), s = t.anchor ?? u, i = t.title ?? "", r = {
    locale: t.locale ?? "en",
    firstDayOfWeek: t.firstDayOfWeek ?? 0,
    nonWorkingDays: t.nonWorkingDays ?? [0, 6],
    dayStartHour: t.dayStartHour ?? 8,
    dayEndHour: t.dayEndHour ?? 18,
    editable: t.editable !== !1,
    now: t.now ?? Date.now(),
    events: gr(t.events),
    send: n,
    i18n: a
  };
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCalendar" }, /* @__PURE__ */ e.createElement(_r, { title: i, granularity: o, i18n: a, send: n }), /* @__PURE__ */ e.createElement("div", { className: "tlCalBody" }, o === "MONTH" ? /* @__PURE__ */ e.createElement(kr, { ctx: r, rangeStart: u, anchorMonth: s }) : o === "YEAR" ? /* @__PURE__ */ e.createElement(Nr, { ctx: r, rangeStart: u }) : /* @__PURE__ */ e.createElement(yr, { ctx: r, rangeStart: u, granularity: o })));
}, Dr = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, Dn = e.createContext(Dr), { useMemo: Tr, useRef: Rr, useState: Lr, useEffect: xr } = e, Mr = 320, Ir = "TLTableView", jr = "TLPanel", Pr = ({ controlId: l }) => {
  var C;
  const t = G(), n = t.maxColumns ?? 3, a = t.labelPosition ?? "auto", o = t.readOnly === !0, u = t.children ?? [], s = t.noModelMessage, i = Rr(null), [r, c] = Lr(
    a === "top" ? "top" : "side"
  );
  xr(() => {
    if (a !== "auto") {
      c(a);
      return;
    }
    const _ = i.current;
    if (!_) return;
    const y = new ResizeObserver((k) => {
      for (const x of k) {
        const w = x.contentRect.width / n;
        c(w < Mr ? "top" : "side");
      }
    });
    return y.observe(_), () => y.disconnect();
  }, [a, n]);
  const d = Tr(() => ({
    readOnly: o,
    resolvedLabelPosition: r
  }), [o, r]), p = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, h = u.length === 1 ? u[0] : void 0, g = !!h && (h.module === Ir || h.module === jr && ((C = h.state) == null ? void 0 : C.bare) === !0), N = [
    "tlFormLayout",
    o ? "tlFormLayout--readonly" : "",
    g ? "tlFormLayout--flush" : ""
  ].filter(Boolean).join(" ");
  return s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: i }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, s)) : /* @__PURE__ */ e.createElement(Dn.Provider, { value: d }, /* @__PURE__ */ e.createElement("div", { id: l, className: N, style: p, ref: i }, u.map((_, y) => /* @__PURE__ */ e.createElement(X, { key: y, control: _ }))));
}, { useCallback: Br } = e, Ar = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, Fr = ({ controlId: l }) => {
  const t = G(), n = ne(), a = ue(Ar), o = t.headerControl ?? null, u = t.headerActions ?? [], s = t.collapsible === !0, i = t.collapsed === !0, r = t.border ?? "none", c = t.fullLine === !0, d = t.children ?? [], m = o != null || u.length > 0 || s, p = Br(() => {
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
      onClick: p,
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
  ), o && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(X, { control: o })), u.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, u.map((g, N) => /* @__PURE__ */ e.createElement(X, { key: N, control: g })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, d.map((g, N) => /* @__PURE__ */ e.createElement(X, { key: N, control: g }))));
}, { useContext: Or, useState: $r, useCallback: Hr } = e, Wr = ({ controlId: l }) => {
  const t = G(), n = Or(Dn), a = t.label ?? "", o = t.required === !0, u = t.error, s = t.errorIcon, i = t.warnings, r = t.warningIcon, c = t.helpText, d = t.dirty === !0, m = t.labelPosition ?? n.resolvedLabelPosition, p = t.fullLine === !0, h = t.visible !== !1, g = t.hasTooltip === !0, N = t.field, C = n.readOnly, [_, y] = $r(!1), k = Hr(() => y((D) => !D), []), x = m === "hidden", E = u != null, w = i != null && i.length > 0, b = [
    "tlFormField",
    `tlFormField--${m}`,
    C ? "tlFormField--readonly" : "",
    p ? "tlFormField--fullLine" : "",
    E ? "tlFormField--error" : "",
    !E && w ? "tlFormField--warning" : "",
    d ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: b, style: h ? void 0 : { display: "none" } }, !x && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": g ? "key:tooltip" : void 0
    },
    a
  ), o && !C && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), d && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), c && !C && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(X, { control: N })), !C && E && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(Ft, { image: s, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, u)), !C && !E && w && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, i.map((D, j) => /* @__PURE__ */ e.createElement("div", { key: j, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(Ft, { image: r, className: "tlFormField__warningIcon" }), /* @__PURE__ */ e.createElement("span", null, D)))), !C && c && _ && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, c));
}, Ur = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.iconCss, o = t.iconSrc, u = t.label, s = t.cssClass, i = t.hasTooltip === !0, r = t.hasLink, c = t.color || void 0, d = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : o ? /* @__PURE__ */ e.createElement("img", { src: o, className: "tlTypeIcon", alt: "" }) : null, m = /* @__PURE__ */ e.createElement(e.Fragment, null, d, u && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, u)), p = c ? /* @__PURE__ */ e.createElement(Gt, { color: c }, m) : m, h = e.useCallback((C) => {
    C.preventDefault(), n("goto", {});
  }, [n]), g = ["tlResourceCell", s].filter(Boolean).join(" "), N = i ? "key:tooltip" : void 0;
  return r ? /* @__PURE__ */ e.createElement(
    "a",
    {
      id: l,
      className: g,
      href: "#",
      onClick: h,
      "data-tooltip": N
    },
    p
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: g, "data-tooltip": N }, p);
}, zr = 20, Vr = () => {
  var w;
  const l = G(), t = ne(), n = l.nodes ?? [], a = l.selectionMode ?? "single", o = l.dragEnabled ?? !1, u = l.dropEnabled ?? !1, s = l.dropIndicatorNodeId ?? null, i = l.dropIndicatorPosition ?? null, [r, c] = e.useState(-1), d = e.useRef(null), m = ((w = n.find((b) => b.selected)) == null ? void 0 : w.id) ?? null;
  e.useEffect(() => {
    var D;
    if (m == null)
      return;
    const b = (D = d.current) == null ? void 0 : D.querySelector(".tlTreeView__node--selected");
    b && b.scrollIntoView({ block: "nearest" });
  }, [m]);
  const p = e.useCallback((b, D) => {
    t(D ? "collapse" : "expand", { nodeId: b });
  }, [t]), h = e.useCallback((b, D) => {
    var R;
    const j = window.getSelection();
    j && !j.isCollapsed && D.currentTarget.contains(j.anchorNode) || ((R = d.current) == null || R.focus({ preventScroll: !0 }), t("select", {
      nodeId: b,
      ctrlKey: D.ctrlKey || D.metaKey,
      shiftKey: D.shiftKey
    }));
  }, [t]), g = e.useCallback((b, D) => {
    D.preventDefault(), t("contextMenu", { nodeId: b, x: D.clientX, y: D.clientY });
  }, [t]), N = e.useRef(null), C = e.useCallback((b, D) => {
    const j = D.getBoundingClientRect(), R = b.clientY - j.top, H = j.height / 3;
    return R < H ? "above" : R > H * 2 ? "below" : "within";
  }, []), _ = e.useCallback((b, D) => {
    D.dataTransfer.effectAllowed = "move", D.dataTransfer.setData("text/plain", b);
  }, []), y = e.useCallback((b, D) => {
    D.preventDefault(), D.dataTransfer.dropEffect = "move";
    const j = C(D, D.currentTarget);
    N.current != null && window.clearTimeout(N.current), N.current = window.setTimeout(() => {
      t("dragOver", { nodeId: b, position: j }), N.current = null;
    }, 50);
  }, [t, C]), k = e.useCallback((b, D) => {
    D.preventDefault(), N.current != null && (window.clearTimeout(N.current), N.current = null);
    const j = C(D, D.currentTarget);
    t("drop", { nodeId: b, position: j });
  }, [t, C]), x = e.useCallback(() => {
    N.current != null && (window.clearTimeout(N.current), N.current = null), t("dragEnd");
  }, [t]), E = e.useCallback((b) => {
    if (n.length === 0) return;
    let D = r;
    switch (b.key) {
      case "ArrowDown":
        b.preventDefault(), D = Math.min(r + 1, n.length - 1);
        break;
      case "ArrowUp":
        b.preventDefault(), D = Math.max(r - 1, 0);
        break;
      case "ArrowRight":
        if (b.preventDefault(), r >= 0 && r < n.length) {
          const j = n[r];
          if (j.expandable && !j.expanded) {
            t("expand", { nodeId: j.id });
            return;
          } else j.expanded && (D = r + 1);
        }
        break;
      case "ArrowLeft":
        if (b.preventDefault(), r >= 0 && r < n.length) {
          const j = n[r];
          if (j.expanded) {
            t("collapse", { nodeId: j.id });
            return;
          } else {
            const R = j.depth;
            for (let H = r - 1; H >= 0; H--)
              if (n[H].depth < R) {
                D = H;
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
        b.preventDefault(), D = 0;
        break;
      case "End":
        b.preventDefault(), D = n.length - 1;
        break;
      default:
        return;
    }
    D !== r && c(D);
  }, [r, n, t, a]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: d,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: E
    },
    n.map((b, D) => /* @__PURE__ */ e.createElement(
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
          D === r ? "tlTreeView__node--focused" : "",
          s === b.id && i === "above" ? "tlTreeView__node--drop-above" : "",
          s === b.id && i === "within" ? "tlTreeView__node--drop-within" : "",
          s === b.id && i === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: b.depth * zr },
        draggable: o,
        onMouseDown: (j) => {
          (j.shiftKey || j.ctrlKey || j.metaKey || j.detail > 1) && j.preventDefault();
        },
        onClick: (j) => h(b.id, j),
        onContextMenu: (j) => g(b.id, j),
        onDragStart: (j) => _(b.id, j),
        onDragOver: u ? (j) => y(b.id, j) : void 0,
        onDrop: u ? (j) => k(b.id, j) : void 0,
        onDragEnd: x
      },
      b.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (j) => {
            j.stopPropagation(), p(b.id, b.expanded);
          },
          tabIndex: -1,
          "aria-label": b.expanded ? "Collapse" : "Expand"
        },
        b.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: b.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(X, { control: b.content }))
    ))
  );
};
var Mt = { exports: {} }, be = {}, It = { exports: {} }, J = {};
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
function Kr() {
  if (fn) return J;
  fn = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), a = Symbol.for("react.strict_mode"), o = Symbol.for("react.profiler"), u = Symbol.for("react.consumer"), s = Symbol.for("react.context"), i = Symbol.for("react.forward_ref"), r = Symbol.for("react.suspense"), c = Symbol.for("react.memo"), d = Symbol.for("react.lazy"), m = Symbol.for("react.activity"), p = Symbol.iterator;
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
  }, N = Object.assign, C = {};
  function _(f, M, Y) {
    this.props = f, this.context = M, this.refs = C, this.updater = Y || g;
  }
  _.prototype.isReactComponent = {}, _.prototype.setState = function(f, M) {
    if (typeof f != "object" && typeof f != "function" && f != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, f, M, "setState");
  }, _.prototype.forceUpdate = function(f) {
    this.updater.enqueueForceUpdate(this, f, "forceUpdate");
  };
  function y() {
  }
  y.prototype = _.prototype;
  function k(f, M, Y) {
    this.props = f, this.context = M, this.refs = C, this.updater = Y || g;
  }
  var x = k.prototype = new y();
  x.constructor = k, N(x, _.prototype), x.isPureReactComponent = !0;
  var E = Array.isArray;
  function w() {
  }
  var b = { H: null, A: null, T: null, S: null }, D = Object.prototype.hasOwnProperty;
  function j(f, M, Y) {
    var U = Y.ref;
    return {
      $$typeof: l,
      type: f,
      key: M,
      ref: U !== void 0 ? U : null,
      props: Y
    };
  }
  function R(f, M) {
    return j(f.type, M, f.props);
  }
  function H(f) {
    return typeof f == "object" && f !== null && f.$$typeof === l;
  }
  function K(f) {
    var M = { "=": "=0", ":": "=2" };
    return "$" + f.replace(/[=:]/g, function(Y) {
      return M[Y];
    });
  }
  var A = /\/+/g;
  function V(f, M) {
    return typeof f == "object" && f !== null && f.key != null ? K("" + f.key) : M.toString(36);
  }
  function B(f) {
    switch (f.status) {
      case "fulfilled":
        return f.value;
      case "rejected":
        throw f.reason;
      default:
        switch (typeof f.status == "string" ? f.then(w, w) : (f.status = "pending", f.then(
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
  function P(f, M, Y, U, Z) {
    var F = typeof f;
    (F === "undefined" || F === "boolean") && (f = null);
    var te = !1;
    if (f === null) te = !0;
    else
      switch (F) {
        case "bigint":
        case "string":
        case "number":
          te = !0;
          break;
        case "object":
          switch (f.$$typeof) {
            case l:
            case t:
              te = !0;
              break;
            case d:
              return te = f._init, P(
                te(f._payload),
                M,
                Y,
                U,
                Z
              );
          }
      }
    if (te)
      return Z = Z(f), te = U === "" ? "." + V(f, 0) : U, E(Z) ? (Y = "", te != null && (Y = te.replace(A, "$&/") + "/"), P(Z, M, Y, "", function(pe) {
        return pe;
      })) : Z != null && (H(Z) && (Z = R(
        Z,
        Y + (Z.key == null || f && f.key === Z.key ? "" : ("" + Z.key).replace(
          A,
          "$&/"
        ) + "/") + te
      )), M.push(Z)), 1;
    te = 0;
    var se = U === "" ? "." : U + ":";
    if (E(f))
      for (var le = 0; le < f.length; le++)
        U = f[le], F = se + V(U, le), te += P(
          U,
          M,
          Y,
          F,
          Z
        );
    else if (le = h(f), typeof le == "function")
      for (f = le.call(f), le = 0; !(U = f.next()).done; )
        U = U.value, F = se + V(U, le++), te += P(
          U,
          M,
          Y,
          F,
          Z
        );
    else if (F === "object") {
      if (typeof f.then == "function")
        return P(
          B(f),
          M,
          Y,
          U,
          Z
        );
      throw M = String(f), Error(
        "Objects are not valid as a React child (found: " + (M === "[object Object]" ? "object with keys {" + Object.keys(f).join(", ") + "}" : M) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return te;
  }
  function I(f, M, Y) {
    if (f == null) return f;
    var U = [], Z = 0;
    return P(f, U, "", "", function(F) {
      return M.call(Y, F, Z++);
    }), U;
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
  var S = typeof reportError == "function" ? reportError : function(f) {
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
  }, $ = {
    map: I,
    forEach: function(f, M, Y) {
      I(
        f,
        function() {
          M.apply(this, arguments);
        },
        Y
      );
    },
    count: function(f) {
      var M = 0;
      return I(f, function() {
        M++;
      }), M;
    },
    toArray: function(f) {
      return I(f, function(M) {
        return M;
      }) || [];
    },
    only: function(f) {
      if (!H(f))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return f;
    }
  };
  return J.Activity = m, J.Children = $, J.Component = _, J.Fragment = n, J.Profiler = o, J.PureComponent = k, J.StrictMode = a, J.Suspense = r, J.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = b, J.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(f) {
      return b.H.useMemoCache(f);
    }
  }, J.cache = function(f) {
    return function() {
      return f.apply(null, arguments);
    };
  }, J.cacheSignal = function() {
    return null;
  }, J.cloneElement = function(f, M, Y) {
    if (f == null)
      throw Error(
        "The argument must be a React element, but you passed " + f + "."
      );
    var U = N({}, f.props), Z = f.key;
    if (M != null)
      for (F in M.key !== void 0 && (Z = "" + M.key), M)
        !D.call(M, F) || F === "key" || F === "__self" || F === "__source" || F === "ref" && M.ref === void 0 || (U[F] = M[F]);
    var F = arguments.length - 2;
    if (F === 1) U.children = Y;
    else if (1 < F) {
      for (var te = Array(F), se = 0; se < F; se++)
        te[se] = arguments[se + 2];
      U.children = te;
    }
    return j(f.type, Z, U);
  }, J.createContext = function(f) {
    return f = {
      $$typeof: s,
      _currentValue: f,
      _currentValue2: f,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, f.Provider = f, f.Consumer = {
      $$typeof: u,
      _context: f
    }, f;
  }, J.createElement = function(f, M, Y) {
    var U, Z = {}, F = null;
    if (M != null)
      for (U in M.key !== void 0 && (F = "" + M.key), M)
        D.call(M, U) && U !== "key" && U !== "__self" && U !== "__source" && (Z[U] = M[U]);
    var te = arguments.length - 2;
    if (te === 1) Z.children = Y;
    else if (1 < te) {
      for (var se = Array(te), le = 0; le < te; le++)
        se[le] = arguments[le + 2];
      Z.children = se;
    }
    if (f && f.defaultProps)
      for (U in te = f.defaultProps, te)
        Z[U] === void 0 && (Z[U] = te[U]);
    return j(f, F, Z);
  }, J.createRef = function() {
    return { current: null };
  }, J.forwardRef = function(f) {
    return { $$typeof: i, render: f };
  }, J.isValidElement = H, J.lazy = function(f) {
    return {
      $$typeof: d,
      _payload: { _status: -1, _result: f },
      _init: L
    };
  }, J.memo = function(f, M) {
    return {
      $$typeof: c,
      type: f,
      compare: M === void 0 ? null : M
    };
  }, J.startTransition = function(f) {
    var M = b.T, Y = {};
    b.T = Y;
    try {
      var U = f(), Z = b.S;
      Z !== null && Z(Y, U), typeof U == "object" && U !== null && typeof U.then == "function" && U.then(w, S);
    } catch (F) {
      S(F);
    } finally {
      M !== null && Y.types !== null && (M.types = Y.types), b.T = M;
    }
  }, J.unstable_useCacheRefresh = function() {
    return b.H.useCacheRefresh();
  }, J.use = function(f) {
    return b.H.use(f);
  }, J.useActionState = function(f, M, Y) {
    return b.H.useActionState(f, M, Y);
  }, J.useCallback = function(f, M) {
    return b.H.useCallback(f, M);
  }, J.useContext = function(f) {
    return b.H.useContext(f);
  }, J.useDebugValue = function() {
  }, J.useDeferredValue = function(f, M) {
    return b.H.useDeferredValue(f, M);
  }, J.useEffect = function(f, M) {
    return b.H.useEffect(f, M);
  }, J.useEffectEvent = function(f) {
    return b.H.useEffectEvent(f);
  }, J.useId = function() {
    return b.H.useId();
  }, J.useImperativeHandle = function(f, M, Y) {
    return b.H.useImperativeHandle(f, M, Y);
  }, J.useInsertionEffect = function(f, M) {
    return b.H.useInsertionEffect(f, M);
  }, J.useLayoutEffect = function(f, M) {
    return b.H.useLayoutEffect(f, M);
  }, J.useMemo = function(f, M) {
    return b.H.useMemo(f, M);
  }, J.useOptimistic = function(f, M) {
    return b.H.useOptimistic(f, M);
  }, J.useReducer = function(f, M, Y) {
    return b.H.useReducer(f, M, Y);
  }, J.useRef = function(f) {
    return b.H.useRef(f);
  }, J.useState = function(f) {
    return b.H.useState(f);
  }, J.useSyncExternalStore = function(f, M, Y) {
    return b.H.useSyncExternalStore(
      f,
      M,
      Y
    );
  }, J.useTransition = function() {
    return b.H.useTransition();
  }, J.version = "19.2.4", J;
}
var hn;
function Yr() {
  return hn || (hn = 1, It.exports = Kr()), It.exports;
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
function Gr() {
  if (bn) return be;
  bn = 1;
  var l = Yr();
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
      var d = c.as, m = i(d, c.crossOrigin), p = typeof c.integrity == "string" ? c.integrity : void 0, h = typeof c.fetchPriority == "string" ? c.fetchPriority : void 0;
      d === "style" ? a.d.S(
        r,
        typeof c.precedence == "string" ? c.precedence : void 0,
        {
          crossOrigin: m,
          integrity: p,
          fetchPriority: h
        }
      ) : d === "script" && a.d.X(r, {
        crossOrigin: m,
        integrity: p,
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
var gn;
function Xr() {
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
  return l(), Mt.exports = Gr(), Mt.exports;
}
var Tn = Xr();
const { useState: Me, useCallback: ge, useRef: tt, useEffect: Ue, useMemo: zt } = e;
function Xt(l, t) {
  return l ? /* @__PURE__ */ e.createElement(Gt, { color: l }, t) : t;
}
function qt({ image: l }) {
  return l ? l.startsWith("/") ? /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" }) : /* @__PURE__ */ e.createElement(Ne, { encoded: l, className: "tlDropdownSelect__optionIcon" }) : null;
}
function qr({
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
    Xt(l.color, /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(qt, { image: l.image }), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__chipLabel" }, l.label))),
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
function Zr({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: a,
  onMouseEnter: o,
  id: u
}) {
  const s = ge(() => a(l.value), [a, l.value]), i = zt(() => {
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
    Xt(l.color, /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(qt, { image: l.image }), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, i)))
  );
}
const Qr = ({ controlId: l, state: t }) => {
  const n = ne(), a = t.value ?? [], o = t.multiSelect === !0, u = t.customOrder === !0, s = t.mandatory === !0, i = t.disabled === !0, r = t.editable !== !1, c = t.optionsLoaded === !0, d = t.options ?? [], m = t.emptyOptionLabel ?? "", p = u && o && !i && r, h = ue({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), g = h["js.dropdownSelect.nothingFound"], N = ge(
    (O) => h["js.dropdownSelect.removeChip"].replace("{0}", O),
    [h]
  ), [C, _] = Me(!1), [y, k] = Me(""), [x, E] = Me(-1), [w, b] = Me(!1), [D, j] = Me({}), [R, H] = Me(null), [K, A] = Me(null), [V, B] = Me(null), P = tt(null), I = tt(null), L = tt(null), S = tt(a);
  S.current = a;
  const $ = tt(-1), f = zt(
    () => new Set(a.map((O) => O.value)),
    [a]
  ), M = zt(() => {
    let O = d.filter((q) => !f.has(q.value));
    if (y) {
      const q = y.toLowerCase();
      O = O.filter((ae) => ae.label.toLowerCase().includes(q));
    }
    return O;
  }, [d, f, y]);
  Ue(() => {
    y && M.length === 1 ? E(0) : E(-1);
  }, [M.length, y]), Ue(() => {
    C && c && I.current && I.current.focus();
  }, [C, c, a]), Ue(() => {
    var ae, ie;
    if ($.current < 0) return;
    const O = $.current;
    $.current = -1;
    const q = (ae = P.current) == null ? void 0 : ae.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    q && q.length > 0 ? q[Math.min(O, q.length - 1)].focus() : (ie = P.current) == null || ie.focus();
  }, [a]), Ue(() => {
    if (!C) return;
    const O = (q) => {
      P.current && !P.current.contains(q.target) && L.current && !L.current.contains(q.target) && (_(!1), k(""));
    };
    return document.addEventListener("mousedown", O), () => document.removeEventListener("mousedown", O);
  }, [C]), Ue(() => {
    if (!C || !P.current) return;
    const O = P.current.getBoundingClientRect(), q = window.innerHeight - O.bottom, ie = q < 300 && O.top > q;
    j({
      left: O.left,
      width: O.width,
      ...ie ? { bottom: window.innerHeight - O.top } : { top: O.bottom }
    });
  }, [C]);
  const Y = ge(async () => {
    if (!(i || !r) && (_(!0), k(""), E(-1), b(!1), !c))
      try {
        await n("loadOptions");
      } catch {
        b(!0);
      }
  }, [i, r, c, n]), U = ge(() => {
    var O;
    _(!1), k(""), E(-1), (O = P.current) == null || O.focus();
  }, []), Z = ge(
    (O) => {
      let q;
      if (o) {
        const ae = d.find((ie) => ie.value === O);
        if (ae)
          q = [...S.current, ae];
        else
          return;
      } else {
        const ae = d.find((ie) => ie.value === O);
        if (ae)
          q = [ae];
        else
          return;
      }
      S.current = q, n(ct, { value: q.map((ae) => ae.value) }), o ? (k(""), E(-1)) : U();
    },
    [o, d, n, U]
  ), F = ge(
    (O) => {
      $.current = S.current.findIndex((ae) => ae.value === O);
      const q = S.current.filter((ae) => ae.value !== O);
      S.current = q, n(ct, { value: q.map((ae) => ae.value) });
    },
    [n]
  ), te = ge(
    (O) => {
      O.stopPropagation(), n(ct, { value: [] }), U();
    },
    [n, U]
  ), se = ge((O) => {
    k(O.target.value);
  }, []), le = ge(
    (O) => {
      if (!C) {
        if (O.key === "ArrowDown" || O.key === "ArrowUp" || O.key === "Enter" || O.key === " ") {
          if (O.target.tagName === "BUTTON") return;
          O.preventDefault(), O.stopPropagation(), Y();
        }
        return;
      }
      switch (O.key) {
        case "ArrowDown":
          O.preventDefault(), O.stopPropagation(), E(
            (q) => q < M.length - 1 ? q + 1 : 0
          );
          break;
        case "ArrowUp":
          O.preventDefault(), O.stopPropagation(), E(
            (q) => q > 0 ? q - 1 : M.length - 1
          );
          break;
        case "Enter":
          O.preventDefault(), O.stopPropagation(), x >= 0 && x < M.length && Z(M[x].value);
          break;
        case "Escape":
          O.preventDefault(), O.stopPropagation(), U();
          break;
        case "Tab":
          U();
          break;
        case "Backspace":
          y === "" && o && a.length > 0 && F(a[a.length - 1].value);
          break;
      }
    },
    [
      C,
      Y,
      U,
      M,
      x,
      Z,
      y,
      o,
      a,
      F
    ]
  ), pe = ge(
    async (O) => {
      O.preventDefault(), b(!1);
      try {
        await n("loadOptions");
      } catch {
        b(!0);
      }
    },
    [n]
  ), ye = ge(
    (O, q) => {
      H(O), q.dataTransfer.effectAllowed = "move", q.dataTransfer.setData("text/plain", String(O));
    },
    []
  ), we = ge(
    (O, q) => {
      if (q.preventDefault(), q.dataTransfer.dropEffect = "move", R === null || R === O) {
        A(null), B(null);
        return;
      }
      const ae = q.currentTarget.getBoundingClientRect(), ie = ae.left + ae.width / 2, qe = q.clientX < ie ? "before" : "after";
      A(O), B(qe);
    },
    [R]
  ), Te = ge(
    (O) => {
      if (O.preventDefault(), R === null || K === null || V === null || R === K) return;
      const q = [...S.current], [ae] = q.splice(R, 1);
      let ie = K;
      R < K ? ie = V === "before" ? ie - 1 : ie : ie = V === "before" ? ie : ie + 1, q.splice(ie, 0, ae), S.current = q, n(ct, { value: q.map((qe) => qe.value) }), H(null), A(null), B(null);
    },
    [R, K, V, n]
  ), Re = ge(() => {
    H(null), A(null), B(null);
  }, []);
  if (Ue(() => {
    if (x < 0 || !L.current) return;
    const O = L.current.querySelector(
      `[id="${l}-opt-${x}"]`
    );
    O && O.scrollIntoView({ block: "nearest" });
  }, [x, l]), !r)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, a.map((O) => /* @__PURE__ */ e.createElement("span", { key: O.value, className: "tlDropdownSelect__readonlyValue" }, Xt(O.color, /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(qt, { image: O.image }), /* @__PURE__ */ e.createElement("span", null, O.label))))));
  const $e = !s && a.length > 0 && !i, He = C ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: L,
      className: "tlDropdownSelect__dropdown",
      style: D,
      ...el
    },
    (c || w) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: I,
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
      !c && !w && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      w && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: pe }, h["js.dropdownSelect.error"])),
      c && M.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, g),
      c && M.map((O, q) => /* @__PURE__ */ e.createElement(
        Zr,
        {
          key: O.value,
          id: `${l}-opt-${q}`,
          option: O,
          highlighted: q === x,
          searchTerm: y,
          onSelect: Z,
          onMouseEnter: () => E(q)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: P,
      className: "tlDropdownSelect" + (C ? " tlDropdownSelect--open" : "") + (i ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": C,
      "aria-haspopup": "listbox",
      "aria-owns": C ? `${l}-listbox` : void 0,
      tabIndex: i ? -1 : 0,
      onClick: C ? void 0 : Y,
      onKeyDown: le
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, a.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, m) : a.map((O, q) => {
      let ae = "";
      return R === q ? ae = "tlDropdownSelect__chip--dragging" : K === q && V === "before" ? ae = "tlDropdownSelect__chip--dropBefore" : K === q && V === "after" && (ae = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        qr,
        {
          key: O.value,
          option: O,
          removable: !i && (o || !s),
          onRemove: F,
          removeLabel: N(O.label),
          draggable: p,
          onDragStart: p ? (ie) => ye(q, ie) : void 0,
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
        onClick: te,
        "aria-label": h["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, C ? "▲" : "▼"))
  ), He && Tn.createPortal(He, document.body));
}, { useCallback: jt, useRef: Jr } = e, Rn = "application/x-tl-color", eo = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: a,
  onSwap: o,
  onReplace: u
}) => {
  const s = Jr(null), i = jt(
    (d) => (m) => {
      s.current = d, m.dataTransfer.effectAllowed = "move";
    },
    []
  ), r = jt((d) => {
    d.preventDefault(), d.dataTransfer.dropEffect = "move";
  }, []), c = jt(
    (d) => (m) => {
      m.preventDefault();
      const p = m.dataTransfer.getData(Rn);
      p ? u(d, p) : s.current !== null && s.current !== d && o(s.current, d), s.current = null;
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
function Ln(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function Vt(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function xn(l) {
  if (!Vt(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function Mn(l, t, n) {
  const a = (o) => Ln(o).toString(16).padStart(2, "0");
  return "#" + a(l) + a(t) + a(n);
}
function to(l, t, n) {
  const a = l / 255, o = t / 255, u = n / 255, s = Math.max(a, o, u), i = Math.min(a, o, u), r = s - i;
  let c = 0;
  r !== 0 && (s === a ? c = (o - u) / r % 6 : s === o ? c = (u - a) / r + 2 : c = (a - o) / r + 4, c *= 60, c < 0 && (c += 360));
  const d = s === 0 ? 0 : r / s;
  return [c, d, s];
}
function no(l, t, n) {
  const a = n * t, o = a * (1 - Math.abs(l / 60 % 2 - 1)), u = n - a;
  let s = 0, i = 0, r = 0;
  return l < 60 ? (s = a, i = o, r = 0) : l < 120 ? (s = o, i = a, r = 0) : l < 180 ? (s = 0, i = a, r = o) : l < 240 ? (s = 0, i = o, r = a) : l < 300 ? (s = o, i = 0, r = a) : (s = a, i = 0, r = o), [
    Math.round((s + u) * 255),
    Math.round((i + u) * 255),
    Math.round((r + u) * 255)
  ];
}
function lo(l) {
  return to(...xn(l));
}
function Pt(l, t, n) {
  return Mn(...no(l, t, n));
}
const { useCallback: ze, useRef: En } = e, ao = ({ color: l, onColorChange: t }) => {
  const [n, a, o] = lo(l), u = En(null), s = En(null), i = ze(
    (g, N) => {
      var k;
      const C = (k = u.current) == null ? void 0 : k.getBoundingClientRect();
      if (!C) return;
      const _ = Math.max(0, Math.min(1, (g - C.left) / C.width)), y = Math.max(0, Math.min(1, 1 - (N - C.top) / C.height));
      t(Pt(n, _, y));
    },
    [n, t]
  ), r = ze(
    (g) => {
      g.preventDefault(), g.target.setPointerCapture(g.pointerId), i(g.clientX, g.clientY);
    },
    [i]
  ), c = ze(
    (g) => {
      g.buttons !== 0 && i(g.clientX, g.clientY);
    },
    [i]
  ), d = ze(
    (g) => {
      var y;
      const N = (y = s.current) == null ? void 0 : y.getBoundingClientRect();
      if (!N) return;
      const _ = Math.max(0, Math.min(1, (g - N.top) / N.height)) * 360;
      t(Pt(_, a, o));
    },
    [a, o, t]
  ), m = ze(
    (g) => {
      g.preventDefault(), g.target.setPointerCapture(g.pointerId), d(g.clientY);
    },
    [d]
  ), p = ze(
    (g) => {
      g.buttons !== 0 && d(g.clientY);
    },
    [d]
  ), h = Pt(n, 1, 1);
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
function ro(l, t) {
  const n = t.toUpperCase();
  return l.some((a) => a != null && a.toUpperCase() === n);
}
const oo = {
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
}, { useState: mt, useCallback: ke, useEffect: vn, useRef: so, useLayoutEffect: co } = e, io = ({
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
  const [c, d] = mt("palette"), [m, p] = mt(t), h = so(null), g = ue(oo), [N, C] = mt(null);
  co(() => {
    if (!l.current || !h.current) return;
    const L = l.current.getBoundingClientRect(), S = h.current.getBoundingClientRect();
    let $ = L.bottom + 4, f = L.left;
    $ + S.height > window.innerHeight && ($ = L.top - S.height - 4), f + S.width > window.innerWidth && (f = Math.max(0, L.right - S.width)), C({ top: $, left: f });
  }, [l]);
  const _ = m != null, [y, k, x] = _ ? xn(m) : [0, 0, 0], [E, w] = mt((m == null ? void 0 : m.toUpperCase()) ?? "");
  vn(() => {
    w((m == null ? void 0 : m.toUpperCase()) ?? "");
  }, [m]), Oe(!0, { ESCAPE: i }), vn(() => {
    const L = ($) => {
      h.current && !h.current.contains($.target) && i();
    }, S = setTimeout(() => document.addEventListener("mousedown", L), 0);
    return () => {
      clearTimeout(S), document.removeEventListener("mousedown", L);
    };
  }, [i]);
  const b = ke(
    (L) => (S) => {
      const $ = parseInt(S.target.value, 10);
      if (isNaN($)) return;
      const f = Ln($);
      p(Mn(L === "r" ? f : y, L === "g" ? f : k, L === "b" ? f : x));
    },
    [y, k, x]
  ), D = ke(
    (L) => {
      if (m != null) {
        L.dataTransfer.setData(Rn, m.toUpperCase()), L.dataTransfer.effectAllowed = "move";
        const S = document.createElement("div");
        S.style.width = "33px", S.style.height = "33px", S.style.backgroundColor = m, S.style.borderRadius = "3px", S.style.border = "1px solid rgba(0,0,0,0.1)", S.style.position = "absolute", S.style.top = "-9999px", document.body.appendChild(S), L.dataTransfer.setDragImage(S, 16, 16), requestAnimationFrame(() => document.body.removeChild(S));
      }
    },
    [m]
  ), j = ke((L) => {
    const S = L.target.value;
    w(S), Vt(S) && p(S);
  }, []), R = ke(() => {
    p(null);
  }, []), H = ke((L) => {
    p(L);
  }, []), K = ke(
    (L) => {
      s(L);
    },
    [s]
  ), A = ke(
    (L, S) => {
      const $ = [...n], f = $[L];
      $[L] = $[S], $[S] = f, r($);
    },
    [n, r]
  ), V = ke(
    (L, S) => {
      const $ = [...n];
      $[L] = S, r($);
    },
    [n, r]
  ), B = ke(() => {
    r([...o]);
  }, [o, r]), P = ke(
    (L) => {
      if (ro(n, L)) return;
      const S = n.indexOf(null);
      if (S < 0) return;
      const $ = [...n];
      $[S] = L.toUpperCase(), r($);
    },
    [n, r]
  ), I = ke(() => {
    m != null && P(m), s(m);
  }, [m, s, P]);
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
      g["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (c === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => d("mixer")
      },
      g["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, c === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      eo,
      {
        colors: n,
        columns: a,
        onSelect: H,
        onConfirm: K,
        onSwap: A,
        onReplace: V
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: B }, g["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(ao, { color: m ?? "#000000", onColorChange: p }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, g["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, g["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (_ ? "" : " tlColorInput--noColor"),
        style: _ ? { backgroundColor: m } : void 0,
        draggable: _,
        onDragStart: _ ? D : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, g["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: _ ? y : "",
        onChange: b("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, g["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: _ ? k : "",
        onChange: b("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, g["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: _ ? x : "",
        onChange: b("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, g["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (E !== "" && !Vt(E) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: E,
        onChange: j
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, u && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: R }, g["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: i }, g["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: I }, g["js.colorInput.ok"]))
  );
}, uo = { "js.colorInput.chooseColor": "Choose color" }, { useState: mo, useCallback: pt, useRef: po } = e, fo = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = ne(), u = ue(uo), [s, i] = mo(!1), r = po(null), c = n, d = t.editable !== !1, m = t.palette ?? [], p = t.paletteColumns ?? 6, h = t.defaultPalette ?? m, g = pt(() => {
    d && i(!0);
  }, [d]), N = pt(
    (y) => {
      i(!1), a(y);
    },
    [a]
  ), C = pt(() => {
    i(!1);
  }, []), _ = pt(
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
      onClick: g,
      disabled: t.disabled === !0,
      title: c ?? "",
      "aria-label": u["js.colorInput.chooseColor"]
    }
  ), s && /* @__PURE__ */ e.createElement(
    io,
    {
      anchorRef: r,
      currentColor: c,
      palette: m,
      paletteColumns: p,
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
}, { useState: nt, useCallback: Be, useEffect: Bt, useRef: _n, useLayoutEffect: ho, useMemo: bo } = e, go = {
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
}, Eo = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: a,
  onSelect: o,
  onCancel: u,
  onLoadIcons: s
}) => {
  const i = ue(go), [r, c] = nt("simple"), [d, m] = nt(""), [p, h] = nt(t ?? ""), [g, N] = nt(!1), [C, _] = nt(null), y = _n(null), k = _n(null);
  ho(() => {
    if (!l.current || !y.current) return;
    const K = l.current.getBoundingClientRect(), A = y.current.getBoundingClientRect();
    let V = K.bottom + 4, B = K.left;
    V + A.height > window.innerHeight && (V = K.top - A.height - 4), B + A.width > window.innerWidth && (B = Math.max(0, K.right - A.width)), _({ top: V, left: B });
  }, [l]), Bt(() => {
    !a && !g && s().catch(() => N(!0));
  }, [a, g, s]), Bt(() => {
    a && k.current && k.current.focus();
  }, [a]), Oe(!0, { ESCAPE: u }), Bt(() => {
    const K = (V) => {
      y.current && !y.current.contains(V.target) && u();
    }, A = setTimeout(() => document.addEventListener("mousedown", K), 0);
    return () => {
      clearTimeout(A), document.removeEventListener("mousedown", K);
    };
  }, [u]);
  const x = bo(() => {
    if (!d) return n;
    const K = d.toLowerCase();
    return n.filter(
      (A) => A.prefix.toLowerCase().includes(K) || A.label.toLowerCase().includes(K) || A.terms != null && A.terms.some((V) => V.includes(K))
    );
  }, [n, d]), E = Be((K) => {
    m(K.target.value);
  }, []), w = Be(
    (K) => {
      o(K);
    },
    [o]
  ), b = Be((K) => {
    h(K);
  }, []), D = Be((K) => {
    h(K.target.value);
  }, []), j = Be(() => {
    o(p || null);
  }, [p, o]), R = Be(() => {
    o(null);
  }, [o]), H = Be(async (K) => {
    K.preventDefault(), N(!1);
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
        ref: k,
        type: "text",
        className: "tlIconSelect__search",
        value: d,
        onChange: E,
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
      !a && !g && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      g && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: H }, i["js.iconSelect.loadError"])),
      a && x.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, i["js.iconSelect.noResults"]),
      a && x.map(
        (K) => K.variants.map((A) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: A.encoded,
            className: "tlIconSelect__iconCell" + (A.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": A.encoded === t,
            tabIndex: 0,
            title: K.label,
            onClick: () => r === "simple" ? w(A.encoded) : b(A.encoded),
            onKeyDown: (V) => {
              (V.key === "Enter" || V.key === " ") && (V.preventDefault(), r === "simple" ? w(A.encoded) : b(A.encoded));
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
        value: p,
        onChange: D
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, i["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, p && /* @__PURE__ */ e.createElement(Ne, { encoded: p })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, p ? p.startsWith("css:") ? p.substring(4) : p : ""))),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: u }, i["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: R }, i["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: j }, i["js.iconSelect.ok"]))
  );
}, vo = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: _o, useCallback: ft, useRef: Co } = e, yo = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = ne(), u = ue(vo), [s, i] = _o(!1), r = Co(null), c = n, d = t.editable !== !1, m = t.disabled === !0, p = t.icons ?? [], h = t.iconsLoaded === !0, g = ft(() => {
    d && !m && i(!0);
  }, [d, m]), N = ft(
    (y) => {
      i(!1), a(y);
    },
    [a]
  ), C = ft(() => {
    i(!1);
  }, []), _ = ft(async () => {
    await o("loadIcons");
  }, [o]);
  return d ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: r,
      className: "tlIconSelect__swatch" + (c == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: g,
      disabled: m,
      title: c ?? "",
      "aria-label": u["js.iconSelect.chooseIcon"]
    },
    c ? /* @__PURE__ */ e.createElement(Ne, { encoded: c }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), s && /* @__PURE__ */ e.createElement(
    Eo,
    {
      anchorRef: r,
      currentValue: c,
      icons: p,
      iconsLoaded: h,
      onSelect: N,
      onCancel: C,
      onLoadIcons: _
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, c ? /* @__PURE__ */ e.createElement(Ne, { encoded: c }) : null));
}, { useCallback: Ve, useEffect: wo, useMemo: Cn, useRef: ko, useState: At } = e, No = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, So = [1, 2, 3, 4];
function Do(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const a = parseFloat(n[1]), o = n[2] || "px";
  return o === "rem" || o === "em" ? a * t : a;
}
function To(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let a = 1;
  for (const o of So)
    n >= o && (a = o);
  return a;
}
function Ro(l, t) {
  const n = No[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function Lo(l, t) {
  const n = Math.max(1, t), a = {}, o = (m, p) => !!(a[m] && a[m][p]), u = (m, p) => {
    a[m] || (a[m] = {}), a[m][p] = !0;
  }, s = [];
  let i = 0, r = 0;
  const c = (m) => {
    let p = null;
    for (const g of s) g.rowStart === m && (p = g);
    if (!p) return;
    let h = p.colEnd;
    for (; h < n && !o(m, h); ) h++;
    if (h !== p.colEnd) {
      for (let g = p.rowStart; g < p.rowEnd; g++)
        for (let N = p.colEnd; N < h; N++) u(g, N);
      p.colEnd = h;
    }
  };
  for (const m of l) {
    const p = n <= 1 ? 1 : Math.max(1, m.rowSpan || 1);
    let h = Math.min(Ro(m.width, n), n);
    for (; o(i, r); )
      r++, r >= n && (r = 0, i++);
    let g = 0;
    for (let k = r; k < n && !o(i, k); k++)
      g++;
    if (h > g) {
      for (c(i), r = 0, i++; o(i, r); )
        r++, r >= n && (r = 0, i++);
      g = 0;
      for (let k = r; k < n && !o(i, k); k++)
        g++;
      h = Math.min(h, g);
    }
    const N = r, C = r + h, _ = i, y = i + p;
    s.push({ id: m.id, colStart: N, colEnd: C, rowStart: _, rowEnd: y });
    for (let k = _; k < y; k++)
      for (let x = N; x < C; x++) u(k, x);
    r = C, r >= n && (r = 0, i++);
  }
  c(i);
  let d = 0;
  for (const m of s) m.rowEnd > d && (d = m.rowEnd);
  for (let m = 1; m < d; m++)
    for (let p = 0; p < n; p++) {
      if (o(m, p)) continue;
      const h = s.find((g) => g.rowEnd === m && g.colStart <= p && p < g.colEnd);
      if (h) {
        h.rowEnd = m + 1;
        for (let g = h.colStart; g < h.colEnd; g++) u(m, g);
      }
    }
  return s;
}
const xo = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.minColWidth ?? "16rem", o = (t.children ?? []).filter((w) => w && w.id), u = ko(null), [s, i] = At(1), r = t.editMode === !0;
  wo(() => {
    const w = u.current;
    if (!w) return;
    const b = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, D = Do(a, b), j = () => i(To(w.clientWidth, D));
    j();
    const R = new ResizeObserver(j);
    return R.observe(w), () => R.disconnect();
  }, [a]);
  const c = Cn(() => Lo(o, s), [o, s]), d = Cn(() => {
    const w = {};
    for (const b of c) w[b.id] = b;
    return w;
  }, [c]), [m, p] = At(null), [h, g] = At(null), N = Ve((w, b) => {
    if (!r) {
      w.preventDefault();
      return;
    }
    p(b), w.dataTransfer.effectAllowed = "move", w.dataTransfer.setData("text/plain", b);
  }, [r]), C = Ve((w, b) => {
    if (!r || !m || m === b) return;
    w.preventDefault(), w.dataTransfer.dropEffect = "move";
    const D = w.currentTarget.getBoundingClientRect(), j = w.clientX < D.left + D.width / 2;
    g((R) => R && R.id === b && R.before === j ? R : { id: b, before: j });
  }, [r, m]), _ = Ve(() => {
  }, []), y = Ve((w, b, D) => {
    const j = o.map((A) => A.id), R = j.indexOf(w);
    if (R < 0) return;
    j.splice(R, 1);
    const H = j.indexOf(b);
    if (H < 0) {
      j.splice(R, 0, w);
      return;
    }
    const K = D ? H : H + 1;
    j.splice(K, 0, w), n("reorder", { order: j });
  }, [o, n]), k = Ve((w, b) => {
    if (!r || !m || m === b) return;
    w.preventDefault();
    const D = w.currentTarget.getBoundingClientRect(), j = w.clientX < D.left + D.width / 2;
    y(m, b, j), p(null), g(null);
  }, [r, m, y]), x = Ve(() => {
    p(null), g(null);
  }, []), E = {
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: E }, o.map((w) => {
      const b = d[w.id];
      if (!b) return null;
      const D = {
        gridColumn: `${b.colStart + 1} / ${b.colEnd + 1}`,
        gridRow: `${b.rowStart + 1} / ${b.rowEnd + 1}`
      }, j = ["tlDashboard__tile"];
      return m === w.id && j.push("tlDashboard__tile--dragging"), h && h.id === w.id && j.push(h.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: w.id,
          className: j.join(" "),
          style: D,
          draggable: r,
          onDragStart: (R) => N(R, w.id),
          onDragOver: (R) => C(R, w.id),
          onDragLeave: _,
          onDrop: (R) => k(R, w.id),
          onDragEnd: x
        },
        /* @__PURE__ */ e.createElement(X, { control: w.control }),
        r && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: Mo, useRef: yn, useState: wn, useEffect: Io, useLayoutEffect: jo } = e, Po = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, a) => /* @__PURE__ */ e.createElement("span", { key: a, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(X, { control: n }))));
}, Bo = ({ group: l }) => {
  var m, p;
  const [t, n] = wn(!1), [a, o] = wn({}), u = yn(null), s = yn(null), i = Mo(() => {
    n((h) => !h);
  }, []);
  jo(() => {
    if (!t) return;
    const h = () => {
      const g = u.current;
      if (!g) return;
      const N = g.getBoundingClientRect();
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
  }, [t]), Io(() => {
    if (!t) return;
    const h = (g) => {
      s.current && !s.current.contains(g.target) && u.current && !u.current.contains(g.target) && n(!1);
    };
    return document.addEventListener("mousedown", h), () => document.removeEventListener("mousedown", h);
  }, [t]), Oe(t, { ESCAPE: () => n(!1) }), Yt(t, s, "first");
  const r = l.items.filter((h) => h != null);
  if (r.length === 0) return null;
  if (r.length === 1 && !((m = l.subGroups) != null && m.length) && !l.icon)
    return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(X, { control: r[0] })));
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
      r.map((h, g) => /* @__PURE__ */ e.createElement("div", { key: g, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(X, { control: h }))),
      (p = l.subGroups) == null ? void 0 : p.map((h, g) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${g}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), h.items.map((N, C) => /* @__PURE__ */ e.createElement("div", { key: C, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(X, { control: N })))))
    ),
    document.body
  ));
}, Ao = ({ controlId: l }) => {
  const a = (G().groups ?? []).filter((o) => o.items.some((u) => u != null));
  return a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, a.map((o, u) => /* @__PURE__ */ e.createElement(e.Fragment, { key: o.name }, u > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), o.display === "menu" ? /* @__PURE__ */ e.createElement(Bo, { group: o }) : /* @__PURE__ */ e.createElement(Po, { group: o }))));
}, Fo = ({ frame: l, covered: t }) => {
  const [n, a] = rt(), o = [
    "tlTileStack__frame",
    t ? "tlTileStack__frame--covered" : "",
    n
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(ot, { host: a }, /* @__PURE__ */ e.createElement("div", { className: o }, /* @__PURE__ */ e.createElement(X, { control: l })));
}, Oo = ({ controlId: l }) => {
  const t = G(), [n, a] = rt(), o = t.frames ?? [], u = t.activeIndex ?? 0;
  return /* @__PURE__ */ e.createElement(ot, { host: a }, /* @__PURE__ */ e.createElement("div", { id: l, className: n ? "tlTileStack " + n : "tlTileStack" }, o.map((s, i) => /* @__PURE__ */ e.createElement(Fo, { key: s.controlId, frame: s, covered: i !== u }))));
}, $o = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.content, o = t.breadcrumb ?? null;
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
  })), /* @__PURE__ */ e.createElement("div", { className: "tlAdaptiveDetail__content" }, a && /* @__PURE__ */ e.createElement(X, { control: a })));
}, Ho = ({ controlId: l }) => {
  const n = G().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((a, o) => /* @__PURE__ */ e.createElement(X, { key: o, control: a })));
}, Wo = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), Uo = {
  "js.sidebar.openDrawer": "Open navigation"
}, zo = ({ controlId: l }) => {
  const t = ne(), n = ue(Uo);
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
W("TLButton", vl);
W("TLUploadButton", _l);
W("TLToggleButton", yl);
W("TLTextInput", ll);
W("TLPasswordInput", rl);
W("TLNumberInput", sl);
W("TLDatePicker", il);
W("TLSelect", dl);
W("TLBooleanChoice", pl);
W("TLCheckbox", gl);
W("TLCounter", wl);
W("TLTabBar", Nl);
W("TLFieldList", Sl);
W("TLAudioRecorder", Tl);
W("TLAudioPlayer", Ll);
W("TLFileUpload", Ml);
W("TLBinaryField", jl);
W("TLFileChips", Al);
W("TLRelativeTime", $l);
W("TLProgress", Hl);
W("TLAnchor", Wl);
W("TLScrollLink", Ul);
W("TLAvatar", Kl);
W("TLDownload", Gl);
W("TLPhotoCapture", ql);
W("TLPhotoViewer", Ql);
W("TLPdfViewer", ea);
W("TLSplitPanel", ta);
W("TLPanel", ca);
W("TLInset", _a);
W("TLMaximizeRoot", ia);
W("TLDeckPane", ua);
W("TLSidebar", Ea);
W("TLStack", va);
W("TLGrid", Ca);
W("TLCard", ya);
W("TLAppBar", wa);
W("TLBreadcrumb", Na);
W("TLBottomBar", Da);
W("TLDialog", La);
W("TLDialogManager", Ia);
W("TLWindow", Aa);
W("TLDrawer", $a);
W("TLMenuRegion", Wa);
W("TLSnackbar", Ka);
W("TLNoticeBar", Ja);
W("TLMenu", tr);
W("TLAppShell", lr);
W("TLText", ar);
W("TLTableView", ur);
W("TLColumnSelect", mr);
W("TLCalendar", Sr);
W("TLFormLayout", Pr);
W("TLFormGroup", Fr);
W("TLFormField", Wr);
W("TLResourceCell", Ur);
W("TLTreeView", Vr);
W("TLDropdownSelect", Qr);
W("TLColorInput", fo);
W("TLIconSelect", yo);
W("TLDashboard", xo);
W("TLToolbar", Ao);
W("TLTileStack", Oo);
W("TLAdaptiveDetail", $o);
W("TLSlot", Ho);
W("TLSlotContent", Wo);
W("TLDrawerToggle", zo);
